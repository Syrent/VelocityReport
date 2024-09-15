package ir.syrent.velocityreport.spigot.utils

import ir.syrent.velocityreport.spigot.hook.DependencyManager
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.sayandev.sayanvanish.api.SayanVanishAPI
import java.util.*

object BukkitVanishUtils {

    fun canSee(player: Player?, target: UUID): Boolean {
        if (player == null) return true
        /*if (DependencyManager.sayanVanishHook.exists) {
            val targetUser = SayanVanishAPI.getInstance().getUser(target) ?: return true
            return SayanVanishAPI.getInstance().canSee(SayanVanishAPI.getInstance().getUser(player.uniqueId), targetUser)
        } else {*/
            return Bukkit.getPlayer(target)?.getMetadata("Vanished")?.map { it.asBoolean() }?.contains(true) != true
//        }
    }
}