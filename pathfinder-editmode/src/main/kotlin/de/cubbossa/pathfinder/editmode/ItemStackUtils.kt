package de.cubbossa.pathfinder.editmode

import com.mojang.authlib.GameProfile
import com.mojang.authlib.properties.Property
import de.cubbossa.translations.Message
import de.tr7zw.changeme.nbtapi.NBTItem
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.ComponentLike
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.SkullMeta
import java.util.*
import java.util.stream.Collectors

var HEAD_URL_LETTER_CHECK_MARK: String =
    "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYTkyZTMxZmZiNTljOTBhYjA4ZmM5ZGMxZmUyNjgwMjAzNWEzYTQ3YzQyZmVlNjM0MjNiY2RiNDI2MmVjYjliNiJ9fX0="
var HEAD_URL_LETTER_EXCLAMATION: String =
    "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNjYyNDVmYjM5N2I3YzJiM2EzNmUyYTI0ZDQ5NmJlMjU4ZjFjZGY0MTA1NGY5OWU5YzY1ZTFhNjczYWRkN2I0In19fQ=="

var HEAD_URL_ORANGE: String =
    "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZTc5YWRkM2U1OTM2YTM4MmE4ZjdmZGMzN2ZkNmZhOTY2NTNkNTEwNGViY2FkYjBkNGY3ZTlkNGE2ZWZjNDU0In19fQ=="
var HEAD_URL_BLUE: String =
    "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNGMzNzRhY2VhNzhlZmJlZmE3OThiZTFiMjdlOTcxNGMzNjQxMWUyMDJlZWNkMzdiOGNmY2ZkMjQ5YTg2MmUifX19"
var HEAD_URL_GREEN: String =
    "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOGU5YjI3ZmNjZDgwOTIxYmQyNjNjOTFkYzUxMWQwOWU5YTc0NjU1NWU2YzljYWQ1MmU4NTYyZWQwMTgyYTJmIn19fQ=="

var GSON_SERIALIZER: GsonComponentSerializer = GsonComponentSerializer.builder().build()
var SERIALIZER: LegacyComponentSerializer = LegacyComponentSerializer.builder()
    .character('§')
    .hexColors()
    .useUnusualXRepeatedCharacterHexFormat()
    .hexCharacter('x')
    .build()

fun giveOrDrop(player: Player, itemStack: ItemStack?) {
    giveOrDrop(player, itemStack, player.location)
}

fun giveOrDrop(player: Player, item: ItemStack?, location: Location) {
    if (item == null || item.type == Material.AIR) {
        return
    }
    val leftoverItems: Map<Int, ItemStack> = player.inventory.addItem(item.clone())
    if (leftoverItems.isEmpty()) {
        return
    }
    leftoverItems.forEach { (_: Int, item2: ItemStack) ->
        location.world!!.dropItemNaturally(
            location,
            item2
        )
    }
}

fun addLore(itemStack: ItemStack?, lore: List<Component>): ItemStack {
    val item = NBTItem(itemStack)
    var display = item.getCompound("display")
    if (display == null) {
        display = item.addCompound("display")
    }
    val presentLore: MutableList<String> = display!!.getStringList("Lore")
    presentLore.addAll(lore.stream().map { component: Component ->
        component.decoration(
            TextDecoration.ITALIC,
            if (component.decoration(TextDecoration.ITALIC) == TextDecoration.State.NOT_SET) TextDecoration.State.FALSE else component.decoration(
                TextDecoration.ITALIC
            )
        )
    }.map { component: Component -> GSON_SERIALIZER.serialize(component) }
        .collect(Collectors.toList()))
    return item.item
}

fun setDisplayName(stack: ItemStack?, name: ComponentLike): ItemStack {
    val n = name.asComponent()
    val item = NBTItem(stack)
    var display = item.getCompound("display")
    if (display == null) {
        display = item.addCompound("display")
    }
    val decoration = n.decoration(TextDecoration.ITALIC)
    display!!.setString(
        "Name", GSON_SERIALIZER.serialize(
            n.decoration(
                TextDecoration.ITALIC,
                if (decoration == TextDecoration.State.NOT_SET) TextDecoration.State.FALSE
                else decoration
            )
        )
    )
    return item.item
}

fun setLore(itemStack: ItemStack?, lore: List<ComponentLike>): ItemStack {
    val item = NBTItem(itemStack)
    var display = item.getCompound("display")
    if (display == null) {
        display = item.addCompound("display")
    }
    val presentLore: MutableList<String> = display!!.getStringList("Lore")
    presentLore.clear()
    presentLore.addAll(lore.stream().map { obj: ComponentLike -> obj.asComponent() }
        .map { component: Component ->
            component.decoration(
                TextDecoration.ITALIC,
                if (component.decoration(TextDecoration.ITALIC) == TextDecoration.State.NOT_SET) TextDecoration.State.FALSE else component.decoration(
                    TextDecoration.ITALIC
                )
            )
        }.map { component: Component -> GSON_SERIALIZER.serialize(component) }
        .collect(Collectors.toList()))
    return item.item
}

fun setCustomModelData(itemStack: ItemStack, customModelData: Int): ItemStack {
    var meta = itemStack.itemMeta
    if (meta == null) {
        meta = Bukkit.getItemFactory().getItemMeta(itemStack.type)
    }
    meta!!.setCustomModelData(customModelData)
    itemStack.setItemMeta(meta)
    return itemStack
}

fun createItemStack(material: Material?, name: ComponentLike): ItemStack {
    val stack = ItemStack(material!!)
    var meta = stack.itemMeta
    if (meta == null) {
        meta = Bukkit.getItemFactory().getItemMeta(material)
    }
    checkNotNull(meta) { "Could not create itemstack." }
    meta.setDisplayName(SERIALIZER.serialize(name.asComponent()))
    stack.setItemMeta(meta)
    return stack
}

fun createItemStack(material: Material?, customModelData: Int): ItemStack {
    return setCustomModelData(ItemStack(material!!), customModelData)
}

fun createCustomHead(url: String?): ItemStack {
    return createCustomHead(ItemStack(Material.PLAYER_HEAD, 1), url)
}

fun createCustomHead(itemStack: ItemStack, url: String?): ItemStack {
    val itemMeta = itemStack.itemMeta
    if (itemMeta is SkullMeta) {
        val profile = GameProfile(UUID.randomUUID(), null)
        profile.properties.put("textures", Property("textures", url))

        try {
            val profileField = itemMeta.javaClass.getDeclaredField("profile")
            profileField.isAccessible = true
            profileField[itemMeta] = profile
        } catch (error: IllegalArgumentException) {
            error.printStackTrace()
        } catch (error: NoSuchFieldException) {
            error.printStackTrace()
        } catch (error: SecurityException) {
            error.printStackTrace()
        } catch (error: IllegalAccessException) {
            error.printStackTrace()
        }
        itemStack.setItemMeta(itemMeta)
    } else {
        throw UnsupportedOperationException(
            "Trying to add a skull texture to a non-playerhead item"
        )
    }
    return itemStack
}

fun createInfoItem(name: Message, lore: Message): ItemStack {
    var stack = ItemStack(Material.PAPER, 1)
    stack = setNameAndLore(stack, name, lore)
    stack = setCustomModelData(stack, 7121000)
    return stack
}

fun setFlags(stack: ItemStack): ItemStack {
    val meta = stack.itemMeta
    meta!!.addItemFlags(*ItemFlag.entries.toTypedArray())
    stack.setItemMeta(meta)
    return stack
}

fun setNameAndLore(
    itemStack: ItemStack?, name: ComponentLike,
    lore: List<ComponentLike>
): ItemStack? {
    var itemStack = itemStack
    itemStack = setDisplayName(itemStack, name)
    itemStack = setLore(itemStack, lore)
    return itemStack
}

fun setNameAndLore(itemStack: ItemStack, name: Message, lore: Message): ItemStack {
    var itemStack = itemStack
    itemStack = setDisplayName(itemStack, name)
    itemStack = setLore(itemStack, java.util.List.of(lore.asComponent()))
    return itemStack
}

fun setGlow(item: ItemStack): ItemStack {
    var meta = item.itemMeta
    if (meta == null) {
        meta = Bukkit.getItemFactory().getItemMeta(item.type)
    }
    if (meta != null) {
        meta.addEnchant(Enchantment.FLAME, 1, true)
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS)
        item.setItemMeta(meta)
    }
    return item
}
