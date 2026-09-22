package org.saintqd.asurecore.mythicmobs.conditions

import io.lumine.mythic.api.adapters.AbstractEntity
import io.lumine.mythic.api.skills.conditions.IEntityCondition
import io.lumine.mythic.bukkit.events.MythicConditionLoadEvent
import io.lumine.mythic.core.utils.annotations.MythicCondition
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player

@MythicCondition(author = "SaintQd", name = "asurehaspdcvalue")
class AsureHasPdcValueCondition(event : MythicConditionLoadEvent) : IEntityCondition {

    val key = NamespacedKey.fromString(event.config.getPlaceholderString(arrayOf("name", "n"), "").get())!!


    override fun check(abstractEntity: AbstractEntity): Boolean {
        val entity = abstractEntity.bukkitEntity
        if (entity is Player) {
            return entity.persistentDataContainer.has(key)
        }
        return true
    }
}