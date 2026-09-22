package org.saintqd.asurecore.mythicmobs.mechanics

import io.lumine.mythic.api.adapters.AbstractEntity
import io.lumine.mythic.api.skills.ITargetedEntitySkill
import io.lumine.mythic.api.skills.SkillMetadata
import io.lumine.mythic.api.skills.SkillResult
import io.lumine.mythic.api.skills.placeholders.PlaceholderDouble
import io.lumine.mythic.bukkit.events.MythicMechanicLoadEvent
import io.lumine.mythic.core.skills.placeholders.PlaceholderContext
import io.lumine.mythic.core.utils.annotations.MythicMechanic
import io.papermc.paper.registry.RegistryAccess
import io.papermc.paper.registry.RegistryKey
import org.bukkit.NamespacedKey
import org.bukkit.attribute.Attributable
import org.bukkit.attribute.AttributeModifier
import org.saintqd.asurelib.utils.AsureUtils

@MythicMechanic(
    name = "asureaddattribute",
    aliases = ["addattribute"],
    author = "SaintQd",
    description = "Adds attribute value to player. May be transparent (permanent) or not."
)
class AsureAddAttributeMechanic(event: MythicMechanicLoadEvent) : ITargetedEntitySkill {
    private val attributeTypeKey: NamespacedKey
    private val nameKey: NamespacedKey
    private val operation: AttributeModifier.Operation
    private val value: PlaceholderDouble
    private val permanent: Boolean

    init {
        val mlc = event.config

        val attributeTypeName = mlc.getPlaceholderString(arrayOf("attribute", "attr", "a"), "").get()
        this.attributeTypeKey = NamespacedKey.fromString(attributeTypeName)!!

        val attributeName = mlc.getPlaceholderString(arrayOf("name", "n"), "").get()
        this.nameKey = NamespacedKey.fromString(attributeName)!!

        this.operation =
            AttributeModifier.Operation.valueOf(mlc.getString(arrayOf("operation", "o"), "ADD_NUMBER"))
        this.value = mlc.getPlaceholderDouble(arrayOf("value", "v"), "1.0")
        this.permanent = mlc.getBoolean(arrayOf("permanent", "p"), false)
    }

    override fun castAtEntity(data: SkillMetadata?, target: AbstractEntity): SkillResult {
        AsureUtils.sendDebugMessage(3, "MythicMobsMechanic: asureaddattribute")

        val attribute = RegistryAccess.registryAccess().getRegistry(RegistryKey.ATTRIBUTE).get(attributeTypeKey)
            ?: return SkillResult.INVALID_CONFIG

        val bukkitEntity = target.bukkitEntity
        if (bukkitEntity !is Attributable) return SkillResult.INVALID_TARGET

        val attributeInstance = bukkitEntity.getAttribute(attribute) ?: return SkillResult.INVALID_TARGET

        val parsedValue = value.get(PlaceholderContext.builder().meta(data).entity(target).build())
        val modifier = AttributeModifier(nameKey, parsedValue, operation)

        if (attributeInstance.getModifier(nameKey) != null) return SkillResult.SUCCESS
        if (permanent) attributeInstance.addModifier(modifier)
        else attributeInstance.addTransientModifier(modifier)

        return SkillResult.SUCCESS
    }
}
