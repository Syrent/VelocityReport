package ir.syrent.velocityreport.listener

import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.connection.DisconnectEvent
import com.velocitypowered.api.proxy.Player
import ir.syrent.velocityreport.VelocityReport
import ir.syrent.velocityreport.utils.ruom.VRuom
import java.util.concurrent.TimeUnit


class DisconnectListener(
    private val plugin: VelocityReport
) {

    init {
        VRuom.registerListener(this)
    }

    @Subscribe
    fun onDisconnect(event: DisconnectEvent) {
        val player: Player = event.player
        VRuom.runAsync({
                plugin.bridgeManager.sendAllPlayersName()
                plugin.bridgeManager.sendServer(player)
            },
            200,
            TimeUnit.MILLISECONDS
        )
        plugin.cooldowns.remove(player.uniqueId)
    }
}