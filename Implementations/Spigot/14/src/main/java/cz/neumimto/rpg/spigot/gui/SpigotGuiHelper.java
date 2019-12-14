package cz.neumimto.rpg.spigot.gui;

import cz.neumimto.rpg.api.Rpg;
import cz.neumimto.rpg.api.configuration.ClassTypeDefinition;
import cz.neumimto.rpg.api.entity.players.classes.ClassDefinition;
import de.tr7zw.nbtapi.NBTItem;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class SpigotGuiHelper {

    public static Inventory createMenuInventoryClassTypesView(Player player) {
        Map<String, ClassTypeDefinition> class_types = Rpg.get().getPluginConfig().CLASS_TYPES;
        Inventory classes = createInventoryTemplate(player, "Classes");
        makeBorder(classes, Material.WHITE_STAINED_GLASS_PANE);
        for (Map.Entry<String, ClassTypeDefinition> entry : class_types.entrySet()) {
            ItemStack itemStack = button(Material.CRAFTING_TABLE,
                    ChatColor.valueOf(entry.getValue().getPrimaryColor()) + entry.getKey(),
                    "ninfo classes " + entry.getKey());
            classes.addItem(itemStack);
        }
        return classes;
    }

    public static Inventory createMenuInventoryClassesByTypeView(Player player, String classType) {
        Map<String, ClassTypeDefinition> class_types = Rpg.get().getPluginConfig().CLASS_TYPES;
        ClassTypeDefinition definition = class_types.get(classType);
        Inventory classes = createInventoryTemplate(player, classType);
        DyeColor dyeColor = DyeColor.valueOf(definition.getDyeColor());
        makeBorder(classes, Material.getMaterial(dyeColor.name() + "_STAINED_GLASS_PANE"));

        Rpg.get().getClassService().getClassDefinitions().stream()
                .filter(a -> a.getClassType().equalsIgnoreCase(classType))
                .forEach(a -> classes.addItem(toItemStack(a)));

        return classes;
    }

    private static ItemStack toItemStack(ClassDefinition a) {
        String sItemType = a.getItemType();
        Material material = Material.matchMaterial(sItemType);
        ItemStack itemStack = button(material, ChatColor.valueOf(a.getPreferedColor()) + a.getName(), "ninfo class " + a.getName());

        List<String> lore;
        if (!(a.getCustomLore() == null || a.getCustomLore().isEmpty())) {
            lore = a.getCustomLore().stream().map(SpigotGuiHelper::parseStr).collect(Collectors.toList());
        } else {
            lore = new ArrayList<>();

            String description = a.getDescription();
            lore.add(ChatColor.BOLD.toString() + ChatColor.GRAY + a.getClassType());
            lore.add(" ");
            lore.add(ChatColor.ITALIC.toString() + ChatColor.GOLD + description);
        }
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.setLore(lore);
        itemStack.setItemMeta(itemMeta);
        itemStack = unclickableInterface(itemStack);

        return itemStack;
    }

    private static Inventory createInventoryTemplate(Player player, String title) {
        return Bukkit.createInventory(player, 6 * 9, title);
    }

    public static void makeBorder(Inventory i, Material material) {
        if (i.getType() == InventoryType.CHEST) {
            for (int j = 0; j < 9; j++) {
                ItemStack of = unclickableInterface(material);
                i.setItem(j, of);

                of = unclickableInterface(material);
                i.setItem(j + 45, of);
            }

            for (int j = 1; j < 5; j++) {
                ItemStack of = unclickableInterface(material);
                i.setItem(9 * j, of);

                of = unclickableInterface(material);
                i.setItem(9 * j + 8, of);
            }

        }


    }

    private static ItemStack button(Material material, String name, String command) {
        ItemStack itemStack = new ItemStack(material);
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.setDisplayName(name);
        itemStack.setItemMeta(itemMeta);
        NBTItem nbti = new NBTItem(itemStack);
        nbti.setString("ntrpg.item-command", command);
        return nbti.getItem();
    }

    private static ItemStack unclickableInterface(Material material) {
        ItemStack itemStack = new ItemStack(material);
        return unclickableInterface(itemStack);
    }

    private static ItemStack unclickableInterface(ItemStack itemStack) {
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.setDisplayName(" ");
        itemStack.setItemMeta(itemMeta);
        NBTItem nbti = new NBTItem(itemStack);
        nbti.setBoolean("ntrpg.item-iface", true);
        return nbti.getItem();
    }


        private static String parseStr(String str) {
        return ChatColor.translateAlternateColorCodes('$', str);
    }
}
