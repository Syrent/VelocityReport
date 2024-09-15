package ir.syrent.velocityreport.spigot.utils

import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.util.*

object BukkitVanishUtils {

    fun canSee(player: Player?, target: UUID): Boolean {
        if (player == null) return true
        return Bukkit.getPlayer(target)?.getMetadata("vanished")?.map { it.asBoolean() }?.contains(true) != true
    }
}