package org.saintqd.asurecore.listeners

import io.papermc.paper.event.player.AsyncChatDecorateEvent
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerCommandPreprocessEvent

class ChatListener : Listener {

    @EventHandler(priority = EventPriority.LOW)
    fun onPlayerChat(event: AsyncChatDecorateEvent) {
        val originalMessage = MiniMessage.miniMessage().serialize(event.originalMessage()).replace("\\","")
        event.player()?.let { player ->
            if (!player.hasPermission("asurecore.chat.colors")) {
                val strippedMessage = MiniMessage.miniMessage().stripTags(originalMessage)
                event.result(MiniMessage.miniMessage().deserialize(strippedMessage))
            }
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    fun onPlayerChat(event: PlayerCommandPreprocessEvent) {
        val originalMessage = event.message
        if (!event.player.hasPermission("asurecore.chat.colors")) {
            val strippedMessage = MiniMessage.miniMessage().stripTags(originalMessage)
            event.message = strippedMessage
        }
    }
}