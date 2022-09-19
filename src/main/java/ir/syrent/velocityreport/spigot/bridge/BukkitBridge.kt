package ir.syrent.velocityreport.spigot.bridge

import ir.syrent.velocityreport.bridge.Bridge
import ir.syrent.velocityreport.spigot.Ruom
import ir.syrent.velocityreport.spigot.messaging.BukkitMessagingChannel
import org.bukkit.entity.Player

class BukkitBridge : Bridge, BukkitMessagingChannel("velocityreport", "main") {

    override fun sendPluginMessage(sender: Any, messageByte: ByteArray) {
        if (sender !is Player) {
            throw IllegalArgumentException("Given object is not a bukkit player")
        }
        sender.sendPluginMessage(Ruom.getPlugin(), "velocityreport:main", messageByte)
    }

    override fun sendPluginMessage(messageByte: ByteArray) {
        throw IllegalStateException("Only proxies can send plugin message without player instances")
    }

}