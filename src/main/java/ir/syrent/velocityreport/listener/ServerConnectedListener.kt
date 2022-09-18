package ir.syrent.velocityreport.listener

import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.player.ServerConnectedEvent
import ir.syrent.velocityreport.VelocityReport
import me.mohamad82.ruom.VRuom
import java.util.concurrent.TimeUnit


class ServerConnectedListener(
  private val plugin: VelocityReport
) {

    init {
        VRuom.registerListener(this)
    }

    @Subscribe
    fun onServerConnected(event: ServerConnectedEvent) {
        VRuom.runAsync({
            plugin.bridgeManager.sendAllPlayersName()
            plugin.bridgeManager.sendServer(event.player)
        }, 1, TimeUnit.SECONDS)
    }
}