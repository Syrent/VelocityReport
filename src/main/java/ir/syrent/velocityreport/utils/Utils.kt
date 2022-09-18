package ir.syrent.velocityreport.utils

import ir.syrent.velocityreport.spigot.Ruom
import ir.syrent.velocityreport.spigot.VelocityReportSpigot
import ir.syrent.velocityreport.spigot.adventure.AdventureApi
import org.bukkit.entity.Player

object Utils {
    fun sendReportsNotification(player: Player) {
        if (player.hasPermission("velocityreport.admin.notify")) {
            Ruom.runSync({
                AdventureApi.get().player(player).sendActionBar("<gold>⚠ <gradient:dark_purple:blue>There are <gold>${VelocityReportSpigot.instance.reportsCount}</gold> reports!".component())
            }, 0, 20)
        }
    }
}