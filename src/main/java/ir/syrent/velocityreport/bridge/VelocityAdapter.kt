package ir.syrent.velocityreport.bridge

import me.mohamad82.ruom.VRuom

class VelocityAdapter {
    fun getAllPlayersName(): List<String> {
        return VRuom.getOnlinePlayers().map { it.username }
    }
}