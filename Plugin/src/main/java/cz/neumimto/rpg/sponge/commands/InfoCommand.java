/*
 *     Copyright (c) 2015, NeumimTo https://github.com/NeumimTo
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package cz.neumimto.rpg.sponge.commands;

import com.google.inject.Singleton;
import cz.neumimto.core.localization.TextHelper;
import cz.neumimto.rpg.ResourceLoader;
import cz.neumimto.rpg.api.classes.ClassService;
import cz.neumimto.rpg.api.entity.players.IActiveCharacter;
import cz.neumimto.rpg.api.entity.players.classes.ClassDefinition;
import cz.neumimto.rpg.api.gui.Gui;
import cz.neumimto.rpg.api.localization.LocalizationKeys;
import cz.neumimto.rpg.api.localization.LocalizationService;
import cz.neumimto.rpg.api.persistance.model.CharacterBase;
import cz.neumimto.rpg.api.persistance.model.CharacterClass;
import cz.neumimto.rpg.api.skills.SkillService;
import cz.neumimto.rpg.api.skills.tree.SkillTree;
import cz.neumimto.rpg.common.inventory.runewords.RuneWord;
import cz.neumimto.rpg.sponge.NtRpgPlugin;
import cz.neumimto.rpg.sponge.configuration.CommandLocalization;
import cz.neumimto.rpg.sponge.entities.players.ISpongeCharacter;
import cz.neumimto.rpg.sponge.entities.players.SpongeCharacterServise;
import cz.neumimto.rpg.sponge.gui.SkillTreeViewModel;
import cz.neumimto.rpg.sponge.gui.VanillaMessaging;
import cz.neumimto.rpg.sponge.inventory.runewords.RWService;
import org.spongepowered.api.Game;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColors;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.inject.Inject;

/**
 * Created by NeumimTo on 23.7.2015.
 */
@ResourceLoader.Command
@Singleton
public class InfoCommand extends CommandBase {

    @Inject
    Game game;

    @Inject
    private SpongeCharacterServise characterService;

    @Inject
    private SkillService skillService;

    @Inject
    private LocalizationService localizationService;

    @Inject
    private NtRpgPlugin plugin;

    @Inject
    private ClassService classService;

    @Inject
    private RWService rwService;

    @Inject
    private VanillaMessaging messaging;

    public InfoCommand() {
        setHelp(CommandLocalization.PLAYERINFO_HELP);
        setPermission("*");
        setDescription(CommandLocalization.PLAYERINFO_DESC);
        setUsage(CommandLocalization.PLAYERINFO_USAGE);
        addAlias("show");
    }

    @Override
    public CommandResult process(CommandSource commandSource, String s) {
        final String[] args = s.split(" ");
        if (args.length == 0) {
            commandSource.sendMessage(getUsage(commandSource));
            return CommandResult.empty();
        }
        if (args[0].equalsIgnoreCase("player")) {
            if (args.length != 2) {
                commandSource.sendMessage(Text.of(getUsage(commandSource)));
                return CommandResult.success();
            }
            Optional<Player> o = game.getServer().getPlayer(args[1]);
            if (o.isPresent()) {
                Player player = o.get();
                if (player != commandSource && !player.hasPermission("list.character.others")) {
                    player.sendMessage(translate(LocalizationKeys.NO_PERMISSIONS));
                    return CommandResult.empty();
                }
                printPlayerInfo(commandSource, args, player);
                return CommandResult.success();
            } else {
                commandSource.sendMessage(translate(LocalizationKeys.PLAYER_IS_OFFLINE_MSG));
            }
        } else if (args[0].equalsIgnoreCase("character")) {
            if (commandSource instanceof Player) {
                if (args.length != 2) {
                    Player player = (Player) commandSource;
                    IActiveCharacter target = characterService.getCharacter(player.getUniqueId());
                    Gui.showCharacterInfo(target, target);
                }
            }
        } else if (args[0].equalsIgnoreCase("characters")) {
            if (commandSource instanceof Player) {
                if (args.length != 2) {
                    Player player = (Player) commandSource;
                    IActiveCharacter target = characterService.getCharacter(player.getUniqueId());
                    Gui.sendListOfCharacters(target, target.getCharacterBase());
                }
            }
        } else if (args[0].equalsIgnoreCase("runeword")) {
            Player player = (Player) commandSource;
            if (args.length == 2) {
                RuneWord rw = rwService.getRuneword(args[1]);
                if (rw != null) {
                    ISpongeCharacter character = characterService.getCharacter(player.getUniqueId());
                    messaging.displayRuneword(character, rw, true);
                }
            } else if (args.length == 3) {
                RuneWord rw = rwService.getRuneword(args[1]);
                if (rw != null) {
                    ISpongeCharacter character = characterService.getCharacter(player.getUniqueId());
                    String a = args[2];
                    if (a.equalsIgnoreCase("allowed-items")) {
                        messaging.displayRunewordAllowedItems(character, rw);
                    } else if (a.equalsIgnoreCase("allowed-classes")) {
                        messaging.displayRunewordAllowedGroups(character, rw);
                    } else if (a.equalsIgnoreCase("required-classes")) {
                        messaging.displayRunewordRequiredGroups(character, rw);
                    } else if (a.equalsIgnoreCase("blocked-classes")) {
                        messaging.displayRunewordBlockedGroups(character, rw);
                    }
                }
            }
        } else if (args[0].equalsIgnoreCase("attributes-initial")) {
            ClassDefinition byName = classService.getClassDefinitionByName(args[1]);
            if (byName == null) {
                return CommandResult.empty();
            }
            IActiveCharacter character = characterService.getCharacter((Player) commandSource);
            Gui.displayInitialAttributes(byName, character);
        } else if (args[0].equalsIgnoreCase("properties-initial")) {
            ClassDefinition byName = classService.getClassDefinitionByName(args[1]);
            if (byName == null) {
                return CommandResult.empty();
            }
            IActiveCharacter character = characterService.getCharacter((Player) commandSource);
            Gui.displayInitialProperties(byName, character);
        } else if (args[0].equalsIgnoreCase("stats")) {
            Player player = (Player) commandSource;
            IActiveCharacter character = characterService.getCharacter(player.getUniqueId());
            if (!character.isStub()) {
                Gui.sendStatus(character);
            } else {
                player.sendMessage(translate(LocalizationKeys.CHARACTER_IS_REQUIRED));

            }
        } else if (args[0].equalsIgnoreCase("skilltree")) {
            ISpongeCharacter character = characterService.getCharacter(((Player) commandSource).getUniqueId());
            SkillTree skillTree = character.getPrimaryClass().getClassDefinition().getSkillTree();
            if (args.length == 2) {
                for (ClassDefinition configClass : classService.getClassDefinitions()) {
                    if (configClass.getName().equalsIgnoreCase(args[1])) {
                        skillTree = configClass.getSkillTree();
                    }
                }
            }
            if (skillTree == SkillTree.Default || skillTree == null) {
                commandSource.sendMessage(Text.of(TextColors.RED, "Unknown class, or the class has no skilltree defined"));
                return CommandResult.empty();
            }
            for (SkillTreeViewModel treeViewModel : character.getSkillTreeViewLocation().values()) {
                treeViewModel.setCurrent(false);
            }
            if (character.getSkillTreeViewLocation().get(skillTree.getId()) == null) {
                SkillTreeViewModel skillTreeViewModel = new SkillTreeViewModel();
                character.getSkillTreeViewLocation().put(skillTree.getId(), skillTreeViewModel);
                skillTreeViewModel.setSkillTree(skillTree);
            } else {
                character.getSkillTreeViewLocation().get(skillTree.getId()).setCurrent(true);
            }
            Gui.openSkillTreeMenu(character);
        } else {
            commandSource.sendMessage(getUsage(commandSource));
        }
        return CommandResult.success();
    }


    private void printPlayerInfo(CommandSource commandSource, String[] args, Player player) {
        NtRpgPlugin.asyncExecutor.execute(() -> {
            List<CharacterBase> characters = characterService.getPlayersCharacters(player.getUniqueId());
            if (characters.isEmpty()) {
                commandSource.sendMessage(Text.of("Player has no characters"));
                return;
            }
            for (CharacterBase character : characters) {
                Text build = Text.builder()
                        .append(Text.of(character.getName()))
                        .onHover(TextActions.showText(Text.of(getSmallInfo(character))))
                        .build();
                commandSource.sendMessage(build);
            }
        });
    }

    private String getSmallInfo(CharacterBase character) {
        return TextColors.GOLD + ", C:" + character.getCharacterClasses().stream().map(CharacterClass::getName).collect(Collectors.joining(", "));
    }

    private Text translate(String key) {
        return TextHelper.parse(localizationService.translate(key));
    }


}