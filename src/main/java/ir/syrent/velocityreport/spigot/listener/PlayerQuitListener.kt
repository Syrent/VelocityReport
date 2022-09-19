package ir.syrent.velocityreport.spigot.listener

import ir.syrent.velocityreport.spigot.VelocityReportSpigot
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerQuitEvent

class PlayerQuitListener(
    private val plugin: VelocityReportSpigot
) : Listener {

    @EventHandler
    private fun onPlayerQuit(event: PlayerQuitEvent) {
        val player = event.player
        plugin.cooldowns.remove(player.uniqueId)
    }
}