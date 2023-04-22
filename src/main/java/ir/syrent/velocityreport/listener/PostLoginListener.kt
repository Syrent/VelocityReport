package ir.syrent.velocityreport.listener

import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.connection.PostLoginEvent
import ir.syrent.velocityreport.VelocityReport
import ir.syrent.velocityreport.utils.ruom.VRuom
import ir.syrent.velocityreport.utils.ruom.utils.MilliCounter
import java.util.concurrent.TimeUnit


class PostLoginListener(
    private val plugin: VelocityReport
) {

    init {
        VRuom.registerListener(this)
    }

    @Subscribe
    fun onPostLogin(event: PostLoginEvent) {
        val player = event.player
        VRuom.runAsync({
            plugin.bridgeManager.sendAllPlayersName()
            plugin.bridgeManager.sendServer(player)
        }, 5, TimeUnit.SECONDS)
        val cooldownCounter = MilliCounter()
        cooldownCounter.start()
        plugin.cooldowns[player.uniqueId] = cooldownCounter
    }
}