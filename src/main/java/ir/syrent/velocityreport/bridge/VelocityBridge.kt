package ir.syrent.velocityreport.bridge

import com.velocitypowered.api.proxy.Player
import me.mohamad82.ruom.VRuom
import me.mohamad82.ruom.messaging.VelocityMessagingChannel

class VelocityBridge: Bridge, VelocityMessagingChannel("velocityreport", "main") {

    override fun sendPluginMessage(sender: Any, messageByte: ByteArray) {
        if (sender !is Player) {
            throw IllegalArgumentException("Given object is not a velocity player")
        }
        sender.sendPluginMessage(name, messageByte)
    }

    override fun sendPluginMessage(messageByte: ByteArray) {
        for (server in VRuom.getServer().allServers) {
            server.sendPluginMessage(name, messageByte)
        }
    }

}