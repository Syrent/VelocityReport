package ir.syrent.velocityreport.spigot.listener

import ir.syrent.velocityreport.spigot.Ruom
import ir.syrent.velocityreport.spigot.VelocityReportSpigot
import ir.syrent.velocityreport.utils.Utils
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerQuitEvent

class PlayerQuitListener(
    private val plugin: VelocityReportSpigot
) : Listener {

    init {
        Ruom.registerListener(this)
    }

    @EventHandler
    private fun onPlayerQuit(event: PlayerQuitEvent) {
        val player = event.player
        Utils.actionbarPlayers.remove(player)
        plugin.cooldowns.remove(player.uniqueId)
    }
}