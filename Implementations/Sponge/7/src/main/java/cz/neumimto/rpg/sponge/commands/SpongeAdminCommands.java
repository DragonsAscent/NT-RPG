package cz.neumimto.rpg.sponge.commands;

import co.aikar.commands.CommandIssuer;
import co.aikar.commands.annotation.*;
import co.aikar.commands.sponge.contexts.OnlinePlayer;
import com.google.inject.Injector;
import cz.neumimto.rpg.api.ResourceLoader;
import cz.neumimto.rpg.api.Rpg;
import cz.neumimto.rpg.api.damage.DamageService;
import cz.neumimto.rpg.api.effects.EffectService;
import cz.neumimto.rpg.api.effects.IGlobalEffect;
import cz.neumimto.rpg.api.entity.EntityService;
import cz.neumimto.rpg.api.entity.players.IActiveCharacter;
import cz.neumimto.rpg.api.entity.players.classes.ClassDefinition;
import cz.neumimto.rpg.api.entity.players.classes.PlayerClassData;
import cz.neumimto.rpg.api.items.ClassItem;
import cz.neumimto.rpg.api.items.ItemClass;
import cz.neumimto.rpg.api.items.RpgItemType;
import cz.neumimto.rpg.api.skills.ISkill;
import cz.neumimto.rpg.api.utils.ActionResult;
import cz.neumimto.rpg.common.commands.AbstractAdminCommand;
import cz.neumimto.rpg.common.commands.CommandProcessingException;
import cz.neumimto.rpg.common.commands.OnlineOtherPlayer;
import cz.neumimto.rpg.common.entity.PropertyServiceImpl;
import cz.neumimto.rpg.sponge.entities.players.SpongeCharacterService;
import cz.neumimto.rpg.sponge.gui.GuiHelper;
import cz.neumimto.rpg.sponge.inventory.SpongeItemService;
import cz.neumimto.rpg.sponge.utils.TextHelper;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.util.Identifiable;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Singleton
@CommandAlias("nadmin|na")
public class SpongeAdminCommands extends AbstractAdminCommand {

    @Inject
    private SpongeCharacterService characterService;

    @Inject
    private PropertyServiceImpl propertyService;

    @Inject
    private EntityService entityService;

    @Inject
    private SpongeItemService itemService;

    @Inject
    private DamageService damageService;

    private Function<ItemClass, List<Text>> TO_TEXT = weaponClass -> {
        List<Text> list = new ArrayList<>();

        list.add(Text.of(TextColors.GOLD, weaponClass.getName()));
        for (Integer property : weaponClass.getProperties()) {
            list.add(Text.of(TextColors.GRAY, " -> ", propertyService.getNameById(property)));
        }
        for (Integer property : weaponClass.getPropertiesMults()) {
            list.add(Text.of(TextColors.GRAY, " -> ", propertyService.getNameById(property)));
        }
        return list;
    };

    @Subcommand("invoke")
    @Description("Forces player to use command")
    public void invokeCommand(CommandSource executor, OnlinePlayer target, String command) {
        try {
            Sponge.getCommandManager().process(target.player, command);
        } catch (Exception e) {
            executor.sendMessage(Text.of(e.getMessage()));
        }
    }

    @Subcommand("inspect property")
    public void inspectPropertyCommand(CommandSource executor, OnlineOtherPlayer target, String property) {
        try {
            int idByName = propertyService.getIdByName(property);
            IActiveCharacter character = target.character;
            executor.sendMessage(Text.of(TextColors.GOLD, "=================="));
            executor.sendMessage(Text.of(TextColors.GREEN, property));

            executor.sendMessage(Text.of(TextColors.GOLD, "Value", TextColors.WHITE, "/",
                    TextColors.AQUA, "Effective Value", TextColors.WHITE, "/",
                    TextColors.GRAY, "Cap",
                    TextColors.DARK_GRAY, " .##"));

            NumberFormat formatter = new DecimalFormat("#0.00");
            executor.sendMessage(Text.of(TextColors.GOLD, formatter.format(character.getProperty(idByName)), TextColors.WHITE, "/",
                    TextColors.AQUA, formatter.format(entityService.getEntityProperty(character, idByName)), TextColors.WHITE, "/",
                    TextColors.GRAY, formatter.format(propertyService.getMaxPropertyValue(idByName))));

            executor.sendMessage(Text.of(TextColors.GOLD, "=================="));
            executor.sendMessage(Text.of(TextColors.GRAY, "Memory/1 player: " + (character.getPrimaryProperties().length * 2 * 4) / 1024.0 + "kb"));

        } catch (Throwable t) {
            executor.sendMessage(Text.of("No such property"));
        }
    }

    @Subcommand("inspect item-damage")
    public void inspectItemDamageCommand(CommandSource executor, OnlinePlayer oplayer) {
        Player player = oplayer.player;
        java.util.Optional<ItemStack> itemInHand = player.getItemInHand(HandTypes.MAIN_HAND);
        if (!itemInHand.isPresent()) {
            executor.sendMessage(Text.of(player.getName() + " has no item in main hand"));
            return;
        }
        ItemStack itemStack = itemInHand.get();
        java.util.Optional<RpgItemType> rpgItemType = itemService.getRpgItemType(itemStack);
        if (!rpgItemType.isPresent()) {
            executor.sendMessage(Text.of(player.getName() + " has no Managed item in main hand"));
            return;
        }
        RpgItemType fromItemStack = rpgItemType.get();
        ItemClass itemClass = fromItemStack.getItemClass();
        List<ItemClass> parents = new LinkedList<>();
        ItemClass parent = itemClass.getParent();
        List<Integer> o = new ArrayList<>();
        o.addAll(itemClass.getProperties());
        o.addAll(itemClass.getPropertiesMults());
        while (parent != null) {
            parents.add(parent);
            o.addAll(parent.getPropertiesMults());
            o.addAll(parent.getProperties());
            parent = parent.getParent();
        }
        parents.add(itemClass);
        Collections.reverse(parents);

        List<Text> a = new ArrayList<>();
        for (ItemClass wc : parents) {
            a.addAll(TO_TEXT.apply(wc));
        }
        for (Text text : a) {
            executor.sendMessage(text);
        }
        executor.sendMessage(Text.of(TextColors.GOLD, "=================="));


        IActiveCharacter character = characterService.getCharacter(player);
        executor.sendMessage(Text.of(TextColors.RED, "Damage: ", damageService.getCharacterItemDamage(character, fromItemStack)));
        executor.sendMessage(Text.of(TextColors.RED, "Details: "));
        executor.sendMessage(Text.of(TextColors.GRAY, " - From Item: ", character.getBaseWeaponDamage(fromItemStack)));

        Collection<PlayerClassData> values = character.getClasses().values();
        for (PlayerClassData value : values) {
            Set<ClassItem> weapons = value.getClassDefinition().getWeapons();
            for (ClassItem weapon : weapons) {
                if (weapon.getType() == fromItemStack) {
                    executor.sendMessage(Text.of(TextColors.GRAY, "  - From Class: " + weapon.getDamage()));
                }
            }
        }


        executor.sendMessage(Text.of(TextColors.GRAY, " - From ItemClass: "));
        Iterator<Integer> iterator = o.iterator();
        while (iterator.hasNext()) {
            int integer = iterator.next();
            String nameById = propertyService.getNameById(integer);

            if (nameById != null && !nameById.endsWith("_mult")) {
                iterator.remove();
            } else continue;

            executor.sendMessage(Text.of(TextColors.GRAY, "   - ", nameById, ":", entityService.getEntityProperty(character, integer)));
        }
        executor.sendMessage(Text.of(TextColors.GRAY, "   - Mult: "));
        iterator = o.iterator();
        while (iterator.hasNext()) {
            int integer = iterator.next();
            String nameById = propertyService.getNameById(integer);
            executor.sendMessage(Text.of(TextColors.GRAY, "   - ", nameById, ":", entityService.getEntityProperty(character, integer)));
        }
    }

    @Override
    public Set<UUID> getAllOnlinePlayers() {
        return Sponge.getServer().getOnlinePlayers().stream().map(Identifiable::getUniqueId).collect(Collectors.toSet());
    }

    @Override
    public void doImplSpecificReload() {
        GuiHelper.initInventories();
    }
}