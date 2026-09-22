package org.saintqd.asurecore.mythicmobs.mechanics

import io.lumine.mythic.api.adapters.AbstractEntity
import io.lumine.mythic.api.skills.ITargetedEntitySkill
import io.lumine.mythic.api.skills.SkillMetadata
import io.lumine.mythic.api.skills.SkillResult
import io.lumine.mythic.bukkit.events.MythicMechanicLoadEvent
import io.lumine.mythic.core.utils.annotations.MythicMechanic
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.saintqd.asurelib.utils.AsureUtils

@MythicMechanic(author = "SaintQd", name = "asureremovepdcvalue")
class AsureRemovePdcValueMechanic(event : MythicMechanicLoadEvent) : ITargetedEntitySkill {

    val key = NamespacedKey.fromString(event.config.getPlaceholderString(arrayOf("name", "n"), "").get())!!

    override fun castAtEntity(
        data: SkillMetadata,
        target: AbstractEntity
    ): SkillResult {
        AsureUtils.sendDebugMessage(3,"MythicMobsMechanic: asureremovepdcvalue")

        val bukkitEntity = target.bukkitEntity
        if (bukkitEntity !is Player)
            return SkillResult.INVALID_TARGET

        bukkitEntity.persistentDataContainer.remove(key)

        return SkillResult.SUCCESS
    }
}