package org.saintqd.asurecore.mythicmobs.mechanics

import io.lumine.mythic.api.adapters.AbstractEntity
import io.lumine.mythic.api.skills.ITargetedEntitySkill
import io.lumine.mythic.api.skills.SkillMetadata
import io.lumine.mythic.api.skills.SkillResult
import io.lumine.mythic.bukkit.events.MythicMechanicLoadEvent
import io.lumine.mythic.core.utils.annotations.MythicMechanic
import io.papermc.paper.registry.RegistryAccess
import io.papermc.paper.registry.RegistryKey
import org.bukkit.NamespacedKey
import org.bukkit.attribute.Attributable
import org.saintqd.asurelib.utils.AsureUtils

@MythicMechanic(author = "SaintQd", name = "asureremoveattribute")
class AsureRemoveAttributeMechanic(event : MythicMechanicLoadEvent) : ITargetedEntitySkill {

    val attributeTypeKey = NamespacedKey.fromString(event.config.getPlaceholderString(arrayOf("attribute", "attr", "a"), "").get())!!
    val nameKey = NamespacedKey.fromString(event.config.getPlaceholderString(arrayOf("name", "n"), "").get())!!

    override fun castAtEntity(
        data: SkillMetadata,
        target: AbstractEntity
    ): SkillResult {
        AsureUtils.sendDebugMessage(3,"MythicMobsMechanic: asureremoveattribute")

        val attribute = RegistryAccess.registryAccess().getRegistry(RegistryKey.ATTRIBUTE).get(attributeTypeKey) ?: return SkillResult.INVALID_CONFIG

        val bukkitEntity = target.bukkitEntity
        if (bukkitEntity !is Attributable)
            return SkillResult.INVALID_TARGET

        val attributeInstance = bukkitEntity.getAttribute(attribute) ?: return SkillResult.INVALID_CONFIG
        if (attributeInstance.getModifier(nameKey) == null)
            return SkillResult.SUCCESS
        attributeInstance.removeModifier(nameKey)

        return SkillResult.SUCCESS
    }
}