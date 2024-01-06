package ir.syrent.velocityreport.spigot.listener

import ir.syrent.velocityreport.spigot.Ruom
import ir.syrent.velocityreport.spigot.VelocityReportSpigot
import ir.syrent.velocityreport.utils.Utils
import ir.syrent.velocityreport.utils.ruom.utils.MilliCounter
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent


class PlayerJoinListener(
    private val plugin: VelocityReportSpigot
) : Listener {

    init {
        Ruom.registerListener(this)
    }

    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        val player = event.player

        plugin.cooldowns[player.uniqueId] = System.currentTimeMillis()

        Utils.sendReportsActionbar(player)
    }

}