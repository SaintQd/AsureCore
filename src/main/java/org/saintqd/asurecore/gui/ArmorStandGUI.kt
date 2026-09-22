package org.saintqd.asurecore.gui

import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.datacomponent.item.ItemLore
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import org.saintqd.asurecore.AsureCore
import org.saintqd.asurelib.AsureLib
import org.saintqd.asurelib.gui.AsureGUI
import org.saintqd.asurelib.gui.AsureGUIButton
import org.saintqd.asurelib.gui.holders.AsureGUIHolder
import org.saintqd.asurelib.managers.LangManager
import org.saintqd.asurelib.utils.AsureUtils

class ArmorStandGUI(player: Player) : AsureGUI(player) {

    @Suppress("UnstableApiUsage")
    fun setArmorStandMenu(armorStand : ArmorStand) {
        val size = 36
        inventory = Bukkit.createInventory(
            AsureGUIHolder(this), size,
            AsureLib.inst().langManager.parseLangString(AsureCore.inst(), "armor_stand_gui_title"))
        buttons.clear()

        var guiItem = ItemStack.of(Material.NAME_TAG)
        guiItem.setData(DataComponentTypes.CUSTOM_NAME,
            AsureLib.inst().langManager.parseLangString(AsureCore.inst(), "armor_stand_gui_set_name"))
        if (player.hasPermission("asurecore.armorstandinteractions.setname")) {
            val lore = ItemLore.lore()
            lore.addLine { Component.empty() }
            val loreLines = LangManager.INSTANCE.getRawLangString(AsureCore.inst(),"armor_stand_gui_set_name_lore")
                .split("<newline>")
            loreLines.forEach { line ->
                lore.addLine(AsureUtils.parseString(line))
            }
            guiItem.setData(DataComponentTypes.LORE, lore)
        }
        else {
            guiItem.setData(DataComponentTypes.LORE, ItemLore.lore().addLine
            { AsureLib.inst().langManager.parseLangString(AsureCore.inst(), "armor_stand_gui_no_permission") })
        }
        inventory.setItem(0, guiItem)

        guiItem = ItemStack.of(Material.BONE)
        guiItem.setData(DataComponentTypes.CUSTOM_NAME,
            AsureLib.inst().langManager.parseLangString(AsureCore.inst(), "armor_stand_gui_change_size"))
        if (player.hasPermission("asurecore.armorstandinteractions.changesize")) {
            val lore = ItemLore.lore()
            if (armorStand.isSmall) {
                lore.addLine {
                    AsureLib.inst().langManager.parseLangString(
                        AsureCore.inst(),
                        "armor_stand_gui_state_text_enabled")
                }
            }
            else {
                lore.addLine {
                    AsureLib.inst().langManager.parseLangString(
                        AsureCore.inst(),
                        "armor_stand_gui_state_text_disabled")
                }
            }
            lore.addLine { Component.empty() }
            val loreLines = LangManager.INSTANCE.getRawLangString(AsureCore.inst(),"armor_stand_gui_change_size_lore")
                .split("<newline>")
            loreLines.forEach { line ->
                val parsedLine = AsureUtils.parseString(line)
                lore.addLine(parsedLine)
            }
            guiItem.setData(DataComponentTypes.LORE, lore)
            buttons[1] = AsureGUIButton().consumer { _ ->
                if (!armorStand.isValid) {
                    player.sendMessage{ AsureLib.inst().langManager.parseLangString(AsureCore.inst(), "armor_stand_gui_entity_is_not_valid") }
                    return@consumer
                }
                armorStand.isSmall = !armorStand.isSmall
                setArmorStandMenu(armorStand)
                player.openInventory(inventory)
                return@consumer
            }
        }
        else {
            guiItem.setData(DataComponentTypes.LORE, ItemLore.lore().addLine
            { AsureLib.inst().langManager.parseLangString(AsureCore.inst(), "armor_stand_gui_no_permission") })
        }
        inventory.setItem(1, guiItem)

        guiItem = ItemStack.of(Material.PHANTOM_MEMBRANE)
        guiItem.setData(DataComponentTypes.CUSTOM_NAME,
            AsureLib.inst().langManager.parseLangString(AsureCore.inst(), "armor_stand_gui_toggle_visibility"))
        if (player.hasPermission("asurecore.armorstandinteractions.togglevisibility")) {
            val lore = ItemLore.lore()
            if (armorStand.isVisible) {
                lore.addLine {
                    AsureLib.inst().langManager.parseLangString(
                        AsureCore.inst(),
                        "armor_stand_gui_state_text_enabled")
                }
            }
            else {
                lore.addLine {
                    AsureLib.inst().langManager.parseLangString(
                        AsureCore.inst(),
                        "armor_stand_gui_state_text_disabled")
                }
            }
            lore.addLine { Component.empty() }
            val loreLines = LangManager.INSTANCE.getRawLangString(AsureCore.inst(),"armor_stand_gui_toggle_visibility_lore")
                .split("<newline>")
            loreLines.forEach { line ->
                val parsedLine = AsureUtils.parseString(line)
                lore.addLine(parsedLine)
            }
            guiItem.setData(DataComponentTypes.LORE, lore)
            buttons[2] = AsureGUIButton().consumer { _ ->
                if (!armorStand.isValid) {
                    player.sendMessage{ AsureLib.inst().langManager.parseLangString(AsureCore.inst(), "armor_stand_gui_entity_is_not_valid") }
                    return@consumer
                }
                armorStand.isVisible = !armorStand.isVisible
                setArmorStandMenu(armorStand)
                player.openInventory(inventory)
                return@consumer
            }
        }
        else {
            guiItem.setData(DataComponentTypes.LORE, ItemLore.lore().addLine
            { AsureLib.inst().langManager.parseLangString(AsureCore.inst(), "armor_stand_gui_no_permission") })
        }
        inventory.setItem(2, guiItem)

        guiItem = ItemStack.of(Material.GLOW_INK_SAC)
        guiItem.setData(DataComponentTypes.CUSTOM_NAME,
            AsureLib.inst().langManager.parseLangString(AsureCore.inst(), "armor_stand_gui_toggle_glow"))
        if (player.hasPermission("asurecore.armorstandinteractions.toggleglow")) {
            val lore = ItemLore.lore()
            if (armorStand.isGlowing) {
                lore.addLine {
                    AsureLib.inst().langManager.parseLangString(AsureCore.inst(), "armor_stand_gui_state_text_enabled") }
            }
            else {
                lore.addLine {
                    AsureLib.inst().langManager.parseLangString(AsureCore.inst(), "armor_stand_gui_state_text_disabled") }
            }
            lore.addLine { Component.empty() }
            val loreLines = LangManager.INSTANCE.getRawLangString(AsureCore.inst(),"armor_stand_gui_toggle_glow_lore")
                .split("<newline>")
            loreLines.forEach { line ->
                val parsedLine = AsureUtils.parseString(line)
                lore.addLine(parsedLine)
            }
            guiItem.setData(DataComponentTypes.LORE, lore)
            buttons[3] = AsureGUIButton().consumer { _ ->
                if (!armorStand.isValid) {
                    player.sendMessage{ AsureLib.inst().langManager.parseLangString(AsureCore.inst(), "armor_stand_gui_entity_is_not_valid") }
                    return@consumer
                }
                armorStand.isGlowing = !armorStand.isGlowing
                setArmorStandMenu(armorStand)
                player.openInventory(inventory)
                return@consumer
            }
        }
        else {
            guiItem.setData(DataComponentTypes.LORE, ItemLore.lore().addLine
            { AsureLib.inst().langManager.parseLangString(AsureCore.inst(), "armor_stand_gui_no_permission") })
        }
        inventory.setItem(3, guiItem)

        guiItem = ItemStack.of(Material.STICK)
        guiItem.setData(DataComponentTypes.CUSTOM_NAME,
            AsureLib.inst().langManager.parseLangString(AsureCore.inst(), "armor_stand_gui_toggle_arms"))
        if (player.hasPermission("asurecore.armorstandinteractions.togglearms")) {
            val lore = ItemLore.lore()
            if (armorStand.hasArms()) {
                lore.addLine {
                    AsureLib.inst().langManager.parseLangString(AsureCore.inst(), "armor_stand_gui_state_text_enabled") }
            }
            else {
                lore.addLine {
                    AsureLib.inst().langManager.parseLangString(AsureCore.inst(), "armor_stand_gui_state_text_disabled") }
            }
            lore.addLine { Component.empty() }
            val loreLines = LangManager.INSTANCE.getRawLangString(AsureCore.inst(),"armor_stand_gui_toggle_arms_lore")
                .split("<newline>")
            loreLines.forEach { line ->
                val parsedLine = AsureUtils.parseString(line)
                lore.addLine(parsedLine)
            }
            guiItem.setData(DataComponentTypes.LORE, lore)
            buttons[4] = AsureGUIButton().consumer { _ ->
                if (!armorStand.isValid) {
                    player.sendMessage{ AsureLib.inst().langManager.parseLangString(AsureCore.inst(), "armor_stand_gui_entity_is_not_valid") }
                    return@consumer
                }
                armorStand.setArms(!armorStand.hasArms())
                setArmorStandMenu(armorStand)
                player.openInventory(inventory)
                return@consumer
            }
        }
        else {
            guiItem.setData(DataComponentTypes.LORE, ItemLore.lore().addLine
            { AsureLib.inst().langManager.parseLangString(AsureCore.inst(), "armor_stand_gui_no_permission") })
        }
        inventory.setItem(4, guiItem)

        guiItem = ItemStack.of(Material.SMOOTH_STONE_SLAB)
        guiItem.setData(DataComponentTypes.CUSTOM_NAME,
            AsureLib.inst().langManager.parseLangString(AsureCore.inst(), "armor_stand_gui_toggle_plate"))
        if (player.hasPermission("asurecore.armorstandinteractions.toggleplate")) {
            val lore = ItemLore.lore()
            if (armorStand.hasBasePlate()) {
                lore.addLine {
                    AsureLib.inst().langManager.parseLangString(AsureCore.inst(), "armor_stand_gui_state_text_enabled") }
            }
            else {
                lore.addLine {
                    AsureLib.inst().langManager.parseLangString(AsureCore.inst(), "armor_stand_gui_state_text_disabled") }
            }
            lore.addLine { Component.empty() }
            val loreLines = LangManager.INSTANCE.getRawLangString(AsureCore.inst(),"armor_stand_gui_toggle_plate_lore")
                .split("<newline>")
            loreLines.forEach { line ->
                val parsedLine = AsureUtils.parseString(line)
                lore.addLine(parsedLine)
            }
            guiItem.setData(DataComponentTypes.LORE, lore)
            buttons[5] = AsureGUIButton().consumer { _ ->
                if (!armorStand.isValid) {
                    player.sendMessage{ AsureLib.inst().langManager.parseLangString(AsureCore.inst(), "armor_stand_gui_entity_is_not_valid") }
                    return@consumer
                }
                armorStand.setBasePlate(!armorStand.hasBasePlate())
                setArmorStandMenu(armorStand)
                player.openInventory(inventory)
                return@consumer
            }
        }
        else {
            guiItem.setData(DataComponentTypes.LORE, ItemLore.lore().addLine
            { AsureLib.inst().langManager.parseLangString(AsureCore.inst(), "armor_stand_gui_no_permission") })
        }
        inventory.setItem(5, guiItem)

        guiItem = ItemStack.of(Material.OBSIDIAN)
        guiItem.setData(DataComponentTypes.CUSTOM_NAME,
            AsureLib.inst().langManager.parseLangString(AsureCore.inst(), "armor_stand_gui_toggle_invulnerability"))
        if (player.hasPermission("asurecore.armorstandinteractions.toggleinvulnerability")) {
            val lore = ItemLore.lore()
            if (armorStand.isInvulnerable) {
                lore.addLine {
                    AsureLib.inst().langManager.parseLangString(AsureCore.inst(), "armor_stand_gui_state_text_enabled") }
            }
            else {
                lore.addLine {
                    AsureLib.inst().langManager.parseLangString(AsureCore.inst(), "armor_stand_gui_state_text_disabled") }
            }
            lore.addLine { Component.empty() }
            val loreLines = LangManager.INSTANCE.getRawLangString(AsureCore.inst(),"armor_stand_gui_toggle_invulnerability_lore")
                .split("<newline>")
            loreLines.forEach { line ->
                val parsedLine = AsureUtils.parseString(line)
                lore.addLine(parsedLine)
            }
            guiItem.setData(DataComponentTypes.LORE, lore)
            buttons[6] = AsureGUIButton().consumer { _ ->
                if (!armorStand.isValid) {
                    player.sendMessage{ AsureLib.inst().langManager.parseLangString(AsureCore.inst(), "armor_stand_gui_entity_is_not_valid") }
                    return@consumer
                }
                armorStand.isInvulnerable = !armorStand.isInvulnerable
                setArmorStandMenu(armorStand)
                player.openInventory(inventory)
                return@consumer
            }
        }
        else {
            guiItem.setData(DataComponentTypes.LORE, ItemLore.lore().addLine
            { AsureLib.inst().langManager.parseLangString(AsureCore.inst(), "armor_stand_gui_no_permission") })
        }
        inventory.setItem(6, guiItem)

        guiItem = ItemStack.of(Material.SPYGLASS)
        guiItem.setData(DataComponentTypes.CUSTOM_NAME,
            AsureLib.inst().langManager.parseLangString(AsureCore.inst(), "armor_stand_gui_toggle_interactions"))
        if (player.hasPermission("asurecore.armorstandinteractions.toggleinteractions")) {
            val lore = ItemLore.lore()
            if (armorStand.disabledSlots.isEmpty()) {
                lore.addLine {
                    AsureLib.inst().langManager.parseLangString(AsureCore.inst(), "armor_stand_gui_state_text_enabled") }
            }
            else {
                lore.addLine {
                    AsureLib.inst().langManager.parseLangString(AsureCore.inst(), "armor_stand_gui_state_text_disabled") }
            }
            lore.addLine { Component.empty() }
            val loreLines = LangManager.INSTANCE.getRawLangString(AsureCore.inst(),"armor_stand_gui_toggle_interactions_lore")
                .split("<newline>")
            loreLines.forEach { line ->
                val parsedLine = AsureUtils.parseString(line)
                lore.addLine(parsedLine)
            }
            guiItem.setData(DataComponentTypes.LORE, lore)
            buttons[7] = AsureGUIButton().consumer { _ ->
                if (!armorStand.isValid) {
                    player.sendMessage{ AsureLib.inst().langManager.parseLangString(AsureCore.inst(), "armor_stand_gui_entity_is_not_valid") }
                    return@consumer
                }
                if (armorStand.disabledSlots.isNotEmpty())
                    armorStand.removeDisabledSlots(*EquipmentSlot.entries.toTypedArray())
                else
                    armorStand.addDisabledSlots(*EquipmentSlot.entries.toTypedArray())
                setArmorStandMenu(armorStand)
                player.openInventory(inventory)
                return@consumer
            }
        }
        else {
            guiItem.setData(DataComponentTypes.LORE, ItemLore.lore().addLine
            { AsureLib.inst().langManager.parseLangString(AsureCore.inst(), "armor_stand_gui_no_permission") })
        }
        inventory.setItem(7, guiItem)

        // Значения по X
        generateChangePoseButton(armorStand, Material.BROWN_CONCRETE,armorStand.headPose.x,
            "armor_stand_gui_pose_head","X",9) { newValue ->
            armorStand.headPose = armorStand.headPose.setX(newValue) }

        generateChangePoseButton(armorStand, Material.BROWN_CONCRETE,armorStand.bodyPose.x,
            "armor_stand_gui_pose_body","X",10) { newValue ->
            armorStand.bodyPose = armorStand.bodyPose.setX(newValue) }

        generateChangePoseButton(armorStand, Material.BROWN_CONCRETE,armorStand.leftArmPose.x,
            "armor_stand_gui_pose_left_arm","X",11) { newValue ->
            armorStand.leftArmPose = armorStand.leftArmPose.setX(newValue) }

        generateChangePoseButton(armorStand, Material.BROWN_CONCRETE,armorStand.rightArmPose.x,
            "armor_stand_gui_pose_right_arm","X",12) { newValue ->
            armorStand.rightArmPose = armorStand.rightArmPose.setX(newValue) }

        generateChangePoseButton(armorStand, Material.BROWN_CONCRETE,armorStand.leftLegPose.x,
            "armor_stand_gui_pose_left_leg","X",13) { newValue ->
            armorStand.leftLegPose = armorStand.leftLegPose.setX(newValue) }

        generateChangePoseButton(armorStand, Material.BROWN_CONCRETE,armorStand.rightLegPose.x,
            "armor_stand_gui_pose_right_leg","X",14) { newValue ->
            armorStand.rightLegPose = armorStand.rightLegPose.setX(newValue) }

        generateChangeLocationButton(armorStand, Material.BROWN_CONCRETE,armorStand.location.x,
            "armor_stand_gui_location","X",15) { newValue ->
            armorStand.teleport(Location(armorStand.location.world,newValue,armorStand.location.y,armorStand.location.z)) }

        // Значения по Y
        generateChangePoseButton(armorStand, Material.BLUE_CONCRETE,armorStand.headPose.y,
            "armor_stand_gui_pose_head","Y",18) { newValue ->
            armorStand.headPose = armorStand.headPose.setY(newValue) }

        generateChangePoseButton(armorStand, Material.BLUE_CONCRETE,armorStand.bodyPose.y,
            "armor_stand_gui_pose_body","Y",19) { newValue ->
            armorStand.bodyPose = armorStand.bodyPose.setY(newValue) }

        generateChangePoseButton(armorStand, Material.BLUE_CONCRETE,armorStand.leftArmPose.y,
            "armor_stand_gui_pose_left_arm","Y",20) { newValue ->
            armorStand.leftArmPose = armorStand.leftArmPose.setY(newValue) }

        generateChangePoseButton(armorStand, Material.BLUE_CONCRETE,armorStand.rightArmPose.y,
            "armor_stand_gui_pose_right_arm","Y",21) { newValue ->
            armorStand.rightArmPose = armorStand.rightArmPose.setY(newValue) }

        generateChangePoseButton(armorStand, Material.BLUE_CONCRETE,armorStand.leftLegPose.y,
            "armor_stand_gui_pose_left_leg","Y",22) { newValue ->
            armorStand.leftLegPose = armorStand.leftLegPose.setY(newValue) }

        generateChangePoseButton(armorStand, Material.BLUE_CONCRETE,armorStand.rightLegPose.y,
            "armor_stand_gui_pose_right_leg","Y",23) { newValue ->
            armorStand.rightLegPose = armorStand.rightLegPose.setY(newValue) }

        generateChangeLocationButton(armorStand, Material.BLUE_CONCRETE,armorStand.location.y,
            "armor_stand_gui_location","Y",24) { newValue ->
            armorStand.teleport(Location(armorStand.location.world,armorStand.location.x,newValue,armorStand.location.z)) }

        // Значения по Z
        generateChangePoseButton(armorStand, Material.PURPLE_CONCRETE,armorStand.headPose.z,
            "armor_stand_gui_pose_head","Z",27) { newValue ->
            armorStand.headPose = armorStand.headPose.setZ(newValue) }

        generateChangePoseButton(armorStand, Material.PURPLE_CONCRETE,armorStand.bodyPose.z,
            "armor_stand_gui_pose_body","Z",28) { newValue ->
            armorStand.bodyPose = armorStand.bodyPose.setZ(newValue) }

        generateChangePoseButton(armorStand, Material.PURPLE_CONCRETE,armorStand.leftArmPose.z,
            "armor_stand_gui_pose_left_arm","Z",29) { newValue ->
            armorStand.leftArmPose = armorStand.leftArmPose.setZ(newValue) }

        generateChangePoseButton(armorStand, Material.PURPLE_CONCRETE,armorStand.rightArmPose.z,
            "armor_stand_gui_pose_right_arm","Z",30) { newValue ->
            armorStand.rightArmPose = armorStand.rightArmPose.setZ(newValue) }

        generateChangePoseButton(armorStand, Material.PURPLE_CONCRETE,armorStand.leftLegPose.z,
            "armor_stand_gui_pose_left_leg","Z",31) { newValue ->
            armorStand.leftLegPose = armorStand.leftLegPose.setZ(newValue) }

        generateChangePoseButton(armorStand, Material.PURPLE_CONCRETE,armorStand.rightLegPose.z,
            "armor_stand_gui_pose_right_leg","Z",32) { newValue ->
            armorStand.rightLegPose = armorStand.rightLegPose.setZ(newValue) }

        generateChangeLocationButton(armorStand, Material.PURPLE_CONCRETE,armorStand.location.z,
            "armor_stand_gui_location","Z",33) { newValue ->
            armorStand.teleport(Location(armorStand.location.world,armorStand.location.x,armorStand.location.y,newValue)) }

    }

    @Suppress("UnstableApiUsage")
    private fun generateChangePoseButton(armorStand: ArmorStand, material : Material, originalValue : Double,
                                         langIdentifier : String, coordName : String, slot : Int,
                                         function : (Double) -> Unit) {
        val guiItem = ItemStack.of(material)

        var degreesValue = Math.toDegrees(originalValue)
        val stringResult = String.format("%.1f", degreesValue)

        guiItem.setData(DataComponentTypes.CUSTOM_NAME,
            AsureLib.inst().langManager.parseLangString(AsureCore.inst(), langIdentifier,coordName,stringResult))
        if (player.hasPermission("asurecore.armorstandinteractions.changepose")) {
            val lore = ItemLore.lore()
            lore.addLine { Component.empty() }
            val loreLines = LangManager.INSTANCE.getRawLangString(AsureCore.inst(),"armor_stand_gui_pose_lore")
                .split("<newline>")
            loreLines.forEach { line ->
                val parsedLine = AsureUtils.parseString(line)
                lore.addLine(parsedLine)
            }
            guiItem.setData(DataComponentTypes.LORE, lore)
            buttons[slot] = AsureGUIButton().consumer { event ->
                if (!armorStand.isValid) {
                    player.sendMessage{ AsureLib.inst().langManager.parseLangString(AsureCore.inst(), "armor_stand_gui_entity_is_not_valid") }
                    return@consumer
                }
                when (event.click) {
                    ClickType.LEFT -> {
                        degreesValue += 0.5
                    }
                    ClickType.SHIFT_LEFT -> {
                        degreesValue += 5.0
                    }
                    ClickType.RIGHT -> {
                        degreesValue -= 0.5
                    }
                    ClickType.SHIFT_RIGHT -> {
                        degreesValue -= 5.0
                    }
                    else -> {}
                }
                if (degreesValue > 360)
                    degreesValue -= 360
                if (degreesValue < 0)
                    degreesValue += 360
                val newValue = Math.toRadians(degreesValue)
                function.invoke(newValue)
                setArmorStandMenu(armorStand)
                player.openInventory(inventory)
                return@consumer
            }
        }
        else {
            guiItem.setData(DataComponentTypes.LORE, ItemLore.lore().addLine
            { AsureLib.inst().langManager.parseLangString(AsureCore.inst(), "armor_stand_gui_no_permission") })
        }
        inventory.setItem(slot, guiItem)
    }

    @Suppress("UnstableApiUsage")
    private fun generateChangeLocationButton(armorStand: ArmorStand, material : Material, originalValue : Double,
                                         langIdentifier : String, coordName : String, slot : Int,
                                         function : (Double) -> Unit) {
        val guiItem = ItemStack.of(material)

        var changedValue = originalValue
        val stringResult = String.format("%.2f", changedValue)

        guiItem.setData(DataComponentTypes.CUSTOM_NAME,
            AsureLib.inst().langManager.parseLangString(AsureCore.inst(), langIdentifier,coordName,stringResult))
        if (player.hasPermission("asurecore.armorstandinteractions.changelocation")) {
            val lore = ItemLore.lore()
            lore.addLine { Component.empty() }
            val loreLines = LangManager.INSTANCE.getRawLangString(AsureCore.inst(),"armor_stand_gui_pose_lore")
                .split("<newline>")
            loreLines.forEach { line ->
                val parsedLine = AsureUtils.parseString(line)
                lore.addLine(parsedLine)
            }
            guiItem.setData(DataComponentTypes.LORE, lore)
            buttons[slot] = AsureGUIButton().consumer { event ->
                if (!armorStand.isValid) {
                    player.sendMessage{ AsureLib.inst().langManager.parseLangString(AsureCore.inst(), "armor_stand_gui_entity_is_not_valid") }
                    return@consumer
                }
                if (player.world != armorStand.world || player.location.distance(armorStand.location) > 6.0) {
                    player.sendMessage{ AsureLib.inst().langManager.parseLangString(AsureCore.inst(), "armor_stand_gui_too_far") }
                    return@consumer
                }
                when (event.click) {
                    ClickType.LEFT -> {
                        changedValue += 0.01
                    }
                    ClickType.SHIFT_LEFT -> {
                        changedValue += 0.1
                    }
                    ClickType.RIGHT -> {
                        changedValue -= 0.01
                    }
                    ClickType.SHIFT_RIGHT -> {
                        changedValue -= 0.1
                    }
                    else -> {}
                }
                function.invoke(changedValue)
                setArmorStandMenu(armorStand)
                player.openInventory(inventory)
                return@consumer
            }
        }
        else {
            guiItem.setData(DataComponentTypes.LORE, ItemLore.lore().addLine
            { AsureLib.inst().langManager.parseLangString(AsureCore.inst(), "armor_stand_gui_no_permission") })
        }
        inventory.setItem(slot, guiItem)
    }
}