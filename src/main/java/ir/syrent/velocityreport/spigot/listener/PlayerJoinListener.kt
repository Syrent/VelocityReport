package ir.syrent.velocityreport.spigot.listener

import ir.syrent.velocityreport.spigot.Ruom
import ir.syrent.velocityreport.spigot.VelocityReportSpigot
import ir.syrent.velocityreport.spigot.adventure.AdventureApi
import ir.syrent.velocityreport.utils.component
import me.mohamad82.ruom.utils.MilliCounter
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

        val commandCooldownCounter = MilliCounter()
        commandCooldownCounter.start()
        plugin.cooldowns[player.uniqueId] = commandCooldownCounter

        if (player.hasPermission("velocityreport.admin.notify")) {
            Ruom.runSync({
                AdventureApi.get().player(player).sendActionBar("<gold>⚠ <gradient:dark_purple:blue>There are <gold>${plugin.reportsCount}</gold> reports!".component())
            }, 0, 20)
        }
    }

}