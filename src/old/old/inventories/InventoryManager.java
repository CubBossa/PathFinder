package de.bossascrew.pathfinder.old.inventories;

import java.util.Arrays;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import de.tr7zw.nbtapi.NBTItem;

public class InventoryManager {

    public Inventory inv = null;
    protected String title = "to be configured";
    protected int rows = 1;

    public InventoryManager(int rows, String title) {

        this.title = title;
        this.rows = rows;
        inv = Bukkit.createInventory(null, rows * 9, this.title);
    }

    public InventoryManager(int rows, String title, InventoryType type) {

        this.title = title;
        this.rows = rows;
        inv = Bukkit.createInventory(null, type, this.title);
    }

    public void refresh() {
    }

    public ItemStack createItem(Material m, String displayname, int amount, String... lore) {
        ItemStack i = new ItemStack(m, amount);
        ItemMeta im = i.getItemMeta();
        im.setDisplayName(displayname);
        im.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_UNBREAKABLE);
        im.setLore(Arrays.asList(lore));
        i.setItemMeta(im);
        return i;
    }

    public ItemStack createItem(Material m, String displayname, int amount, List<String> lore) {
        ItemStack i = new ItemStack(m, amount);
        ItemMeta im = i.getItemMeta();
        im.setDisplayName(displayname);
        im.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_UNBREAKABLE);
        im.setLore(lore);
        i.setItemMeta(im);
        return i;
    }

    public ItemStack createItem(Material m, String displayname, int amount, List<String> lore, boolean hideAttributes) {
        ItemStack i = new ItemStack(m, amount);
        ItemMeta im = i.getItemMeta();
        im.setDisplayName(displayname);
        if (hideAttributes) {
            im.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_UNBREAKABLE);
        }
        im.setLore(lore);
        i.setItemMeta(im);
        return i;
    }

    public ItemStack createItem(Material m, String displayname, int amount) {
        ItemStack i = new ItemStack(m, amount);
        ItemMeta im = i.getItemMeta();
        im.setDisplayName(displayname);
        i.setItemMeta(im);
        return i;
    }

    public ItemStack setMetaID(ItemStack i, String key, int value) {
        NBTItem nbt = new NBTItem(i);
        nbt.setInteger(key, value);
        return nbt.getItem();
    }

    public ItemStack setMetaTag(ItemStack i, String key, String value) {
        NBTItem nbt = new NBTItem(i);
        nbt.setString(key, value);
        return nbt.getItem();
    }

    public ItemStack glowItem(ItemStack i) {
        i.addUnsafeEnchantment(Enchantment.DIG_SPEED, 1);
        ItemMeta meta = i.getItemMeta();
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        i.setItemMeta(meta);
        return i;
    }

    public Inventory open(Player p) {
        refresh();
        p.openInventory(this.inv);
        return this.inv;
    }

    public void close() {

    }

    public Inventory getInventory() {
        return this.inv;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
