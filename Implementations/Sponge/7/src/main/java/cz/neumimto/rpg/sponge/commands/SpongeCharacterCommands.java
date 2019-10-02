package cz.neumimto.rpg.sponge.commands;


import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Subcommand;
import cz.neumimto.rpg.api.Rpg;
import cz.neumimto.rpg.api.configuration.AttributeConfig;
import cz.neumimto.rpg.api.entity.players.IActiveCharacter;
import cz.neumimto.rpg.api.entity.players.classes.ClassDefinition;
import cz.neumimto.rpg.api.gui.Gui;
import cz.neumimto.rpg.common.commands.CharacterCommandFacade;
import cz.neumimto.rpg.sponge.SpongeRpgPlugin;
import cz.neumimto.rpg.sponge.entities.players.ISpongeCharacter;
import cz.neumimto.rpg.sponge.entities.players.SpongeCharacterService;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.UUID;

@Singleton
@CommandAlias("char|c")
public class SpongeCharacterCommands extends BaseCommand {

    @Inject
    private CharacterCommandFacade characterCommandFacade;

    @Inject
    private SpongeCharacterService characterService;

    @Inject
    private SpongeRpgPlugin plugin;

    @Default
    public void displayCharacterMenuCommand(Player executor) {
        IActiveCharacter character = characterService.getCharacter(executor);
        Gui.displayCharacterMenu(character);
    }

    @Subcommand("list")
    public void characterList(Player executor) {
        IActiveCharacter character = characterService.getCharacter(executor);
        Gui.sendListOfCharacters(character, character.getCharacterBase());
    }

    @Subcommand("create")
    public void createCharacter(Player executor, String name) {
        UUID uuid = executor.getUniqueId();

        characterCommandFacade.commandCreateCharacter(uuid, name, actionResult -> {
            executor.sendMessage(Text.of(actionResult.getMessage()));
        });
    }

    @Subcommand("switch")
    public void deleteCharacter(Player executor, String name) {
        IActiveCharacter character = characterService.getCharacter(executor);
        characterCommandFacade.commandSwitchCharacter(character, name, runnable -> {
            Rpg.get().scheduleSyncLater(runnable);
        });
    }

    @Subcommand("class|c")
    public void chooseClassCommand(Player executor, ClassDefinition classDefinition) {
        IActiveCharacter character = characterService.getCharacter(executor);
        characterCommandFacade.commandChooseClass(character, classDefinition);
    }

    @Subcommand("list")
    public void listCharactersCommand(Player executor) {
        IActiveCharacter character = characterService.getCharacter((executor).getUniqueId());
        Gui.sendListOfCharacters(character, character.getCharacterBase());
    }

    @Subcommand("armor")
    public void displayArmorCommand(Player executor, @Default("0") int page) {
        IActiveCharacter character = characterService.getCharacter(executor);
        Gui.displayCharacterArmor(character, page);
    }

    @Subcommand("weapons")
    public void displayWeaponsCommand(Player executor, @Default("0") int page) {
        IActiveCharacter character = characterService.getCharacter(executor);
        Gui.displayCharacterWeapons(character, page);
    }

    @Subcommand("switch")
    public void switchCharacterCommand(Player executor, String name) {
        IActiveCharacter current = characterService.getCharacter(executor);
        characterCommandFacade.commandSwitchCharacter(current, name, runnable ->
                Sponge.getScheduler()
                        .createTaskBuilder()
                        .name("SetCharacterCallback" + executor.getUniqueId())
                        .execute(runnable::run)
                        .submit(plugin));
    }

    @Subcommand("attributes")
    public void displayAttributesCommand(Player executor) {
        IActiveCharacter character = characterService.getCharacter(executor);
        Gui.displayCharacterAttributes(character);
    }

    @Subcommand("attributes-commit")
    public void attributesCommitCommand(Player executor) {
        IActiveCharacter character = characterService.getCharacter(executor);
        characterCommandFacade.commandCommitAttribute(character);
    }

    @Subcommand("attribute add")
    public void attributesCommitCommand(Player executor, AttributeConfig attribute, @Default("1") int amount) {
        ISpongeCharacter character = characterService.getCharacter(executor);
        character.getAttributesTransaction().put(attribute.getId(), amount);
    }

    @Subcommand("attribute respec")
    public void attributeRespecCommand(Player executor) {
        ISpongeCharacter character = characterService.getCharacter(executor);
        characterService.resetAttributes(character);
        characterService.putInSaveQueue(character.getCharacterBase());
    }

}
