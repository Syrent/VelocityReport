package ir.syrent.velocityreport.utils

import com.velocitypowered.api.proxy.Player
import ir.syrent.velocityreport.spigot.hook.DependencyManager
import org.sayandev.sayanvanish.api.SayanVanishAPI
import java.util.*

object VelocityVanishUtils {

    fun canSee(player: Player?, target: UUID): Boolean {
        if (player == null) return true
        if (DependencyManager.sayanVanishHook.exists) {
            val targetUser = SayanVanishAPI.getInstance().getUser(target) ?: return true
            return SayanVanishAPI.getInstance().canSee(SayanVanishAPI.getInstance().getUser(player.uniqueId), targetUser)
        } else {
            return false
        }
    }
}