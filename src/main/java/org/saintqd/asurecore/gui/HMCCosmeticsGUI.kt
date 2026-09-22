package org.saintqd.asurecore.gui

import com.hibiscusmc.hmccosmetics.cosmetic.CosmeticSlot
import com.hibiscusmc.hmccosmetics.cosmetic.Cosmetics
import com.hibiscusmc.hmccosmetics.gui.special.DyeMenuProvider
import com.hibiscusmc.hmccosmetics.user.CosmeticUsers
import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.datacomponent.item.DyedItemColor
import io.papermc.paper.datacomponent.item.ItemLore
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.Color
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.permissions.PermissionAttachmentInfo
import org.intellij.lang.annotations.Subst
import org.saintqd.asurecore.AsureCore
import org.saintqd.asurelib.AsureLib
import org.saintqd.asurelib.gui.AsureGUI
import org.saintqd.asurelib.gui.AsureGUIButton
import org.saintqd.asurelib.gui.holders.AsureGUIHolder
import org.saintqd.asurelib.managers.LangManager
import org.saintqd.asurelib.utils.AsureUtils

class HMCCosmeticsGUI(player : Player) : AsureGUI(player) {

    @Suppress("UnstableApiUsage")
    fun setMainMenu(cosmeticSlot : CosmeticSlot, page : Int) {

        val user = CosmeticUsers.getUser(player) ?: return

        val size = 45
        var loopIndex = 0
        inventory = Bukkit.createInventory(
            AsureGUIHolder(this), size,
            AsureLib.inst().langManager.parseLangString(AsureCore.inst(), "cosmetic_gui_title")
        )
        buttons.clear()

        val fillerItem = ItemStack.of(Material.BLACK_STAINED_GLASS_PANE)
        fillerItem.setData<Component?>(DataComponentTypes.CUSTOM_NAME, Component.empty())
        for (slot in size - 9..<size) {
            inventory.setItem(slot, fillerItem.clone())
        }


        // Создаём список пермишенов на скины, которые есть у игрока
        val cosmeticPermissions: MutableList<String> = ArrayList(
            player.effectivePermissions.stream()
                .map { obj: PermissionAttachmentInfo -> obj.permission }
                .filter { permission: String ->
                    AsureCore.inst().configManager.cosmeticPermissionsToNames.containsKey(permission)
                }.toList()
        )
        if (player.hasPermission("asurecore.admin") || player.isOp) cosmeticPermissions.addAll(
            AsureCore.inst().configManager.cosmeticPermissionsToNames.keys
        )

        // Создаём из списка пермишенов список доступных игроку скинов
        //  True - если есть пермишен
        //  False - если нет
        val availableCosmetics = HashMap<String, Boolean>()
        for (permission in cosmeticPermissions) {
            availableCosmetics[AsureCore.inst().configManager.cosmeticPermissionsToNames[permission]!!] = true
        }

        var slotIndex = 0
        for (cosmeticName in availableCosmetics.keys) {
            if (!availableCosmetics[cosmeticName]!!) continue
            val cosmetic = Cosmetics.getCosmetic(cosmeticName) ?: continue
            if (cosmetic.slot != cosmeticSlot) continue

            loopIndex++
            if (loopIndex <= (page - 1) * (size - 9)) continue  // Проверки для отображения косметики только текущей страницы
            if (loopIndex > page * (size - 9)) break

            val cosmeticItem = if (cosmetic.isDyeable) ItemStack.of(Material.LEATHER_HORSE_ARMOR) else ItemStack.of(Material.STONE)
            //if (cosmetic.isDyeable)
            cosmeticItem.setData(DataComponentTypes.DYED_COLOR, DyedItemColor.dyedItemColor().color(Color.MAROON))
            cosmeticItem.addItemFlags(ItemFlag.HIDE_DYE, ItemFlag.HIDE_ATTRIBUTES)
            val name = cosmetic.config?.node("item","name")?.string ?: cosmetic.id
            val lore = ItemLore.lore()

            cosmeticItem.setData(DataComponentTypes.CUSTOM_NAME, AsureUtils.parseString(name))
            cosmeticItem.setData(DataComponentTypes.ITEM_MODEL, Key.key(cosmetic.material.lowercase()))

            if (user.hasCosmeticInSlot(cosmetic)) {
                lore.addLine { AsureLib.inst().langManager.parseLangString(AsureCore.inst(), "cosmetic_gui_enabled") }
            }
            else {
                lore.addLine { AsureLib.inst().langManager.parseLangString(AsureCore.inst(), "cosmetic_gui_disabled") }
            }
            lore.addLine { Component.empty() }
            val loreLines = LangManager.INSTANCE.getRawLangString(AsureCore.inst(),"cosmetic_gui_hint")
                .split("<newline>")
            for (line in loreLines)
                lore.addLine(AsureUtils.parseString(line))
            cosmeticItem.setData(DataComponentTypes.LORE, lore.build())

            val showButton = AsureGUIButton().consumer { _: InventoryClickEvent ->
                if (!user.hasCosmeticInSlot(cosmetic)) {
                    if (cosmetic.isDyeable) {
                        DyeMenuProvider.openMenu(player, user, cosmetic)
                    } else {
                        user.addCosmetic(cosmetic)
                        player.sendMessage { AsureLib.inst().langManager.parseLangString(AsureCore.inst(), "cosmetic_gui_enable_success") }
                        setMainMenu(cosmeticSlot,page)
                        player.openInventory(inventory)
                    }
                }
                else {
                    user.removeCosmeticSlot(cosmetic)
                    player.sendMessage { AsureLib.inst().langManager.parseLangString(AsureCore.inst(), "cosmetic_gui_disable_success") }
                    setMainMenu(cosmeticSlot,page)
                    player.openInventory(inventory)
                }
            }

            buttons[slotIndex] = showButton
            inventory.setItem(slotIndex, cosmeticItem)
            slotIndex++
        }

        if (slotIndex == 0) {
            val emptyItem = ItemStack.of(Material.PAPER)
            val lore = ItemLore.lore()
            emptyItem.setData(DataComponentTypes.CUSTOM_NAME,
                AsureLib.inst().langManager.parseLangString(AsureCore.inst(), "cosmetic_gui_empty")
            )
            val loreLines = LangManager.INSTANCE.getRawLangString(AsureCore.inst(),"cosmetic_gui_empty_lore")
                .split("<newline>")
            for (line in loreLines) lore.addLine(AsureUtils.parseString(line))

            emptyItem.setData<ItemLore?>(DataComponentTypes.LORE, lore.build())
            inventory.setItem(4, emptyItem)
        }

        if (page > 1) {
            val pageItem = ItemStack.of(Material.PAPER)
            @Subst("minecraft:paper") val modelName =
                AsureCore.inst().suffixManager.menuModels.getOrDefault("PrevPageButton", "paper")
            pageItem.setData(DataComponentTypes.ITEM_MODEL, Key.key(modelName))
            pageItem.setData(
                DataComponentTypes.CUSTOM_NAME,
                AsureLib.inst().langManager.parseLangString(AsureCore.inst(), "menu_prev_page")
            )
            pageItem.setData(
                DataComponentTypes.LORE,
                ItemLore.lore()
                    .addLine(
                        AsureLib.inst().langManager.parseLangString(AsureCore.inst(), "menu_prev_page_lore")
                    )
                    .build()
            )

            val button = AsureGUIButton().consumer { _: InventoryClickEvent ->
                setMainMenu(cosmeticSlot, page - 1)
                player.openInventory(inventory)
            }
            buttons[size - 7] = button

            inventory.setItem(size - 7, pageItem)
        }
        if (loopIndex > page * (size - 9)) {
            val pageItem = ItemStack.of(Material.PAPER)
            @Subst("minecraft:paper") val modelName =
                AsureCore.inst().suffixManager.menuModels.getOrDefault("NextPageButton", "paper")
            pageItem.setData(DataComponentTypes.ITEM_MODEL, Key.key(modelName))
            pageItem.setData<Component>(
                DataComponentTypes.CUSTOM_NAME,
                AsureLib.inst().langManager.parseLangString(AsureCore.inst(), "menu_next_page")
            )
            pageItem.setData(
                DataComponentTypes.LORE,
                ItemLore.lore()
                    .addLine(
                        AsureLib.inst().langManager.parseLangString(AsureCore.inst(), "menu_next_page_lore")
                    )
                    .build()
            )

            val button = AsureGUIButton().consumer { _: InventoryClickEvent ->
                setMainMenu(cosmeticSlot, page + 1)
                player.openInventory(inventory)
            }
            buttons[size - 3] = button

            inventory.setItem(size - 3, pageItem)
        }

        val closeItem = ItemStack.of(Material.PAPER)
        val modelName = AsureCore.inst().suffixManager.menuModels.getOrDefault("CloseButton", "barrier")
        closeItem.setData(DataComponentTypes.ITEM_MODEL, Key.key(modelName))
        closeItem.setData(
            DataComponentTypes.CUSTOM_NAME,
            AsureLib.inst().langManager.parseLangString(AsureCore.inst(), "menu_close")
        )
        closeItem.setData(
            DataComponentTypes.LORE,
            ItemLore.lore()
                .addLine(AsureLib.inst().langManager.parseLangString(AsureCore.inst(), "menu_close_lore"))
                .build()
        )

        val button = AsureGUIButton().consumer { _: InventoryClickEvent? ->
            player.performCommand("asurelib opengui asurecore:cosmetic_menu")
        }
        buttons[size - 5] = button
        inventory.setItem(size - 5, closeItem)

        player.openInventory(inventory)
    }
}