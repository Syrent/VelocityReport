package ir.syrent.velocityreport.bridge

import ir.syrent.velocityreport.utils.ruom.VRuom

class VelocityAdapter {
    fun getAllPlayersName(): List<String> {
        return VRuom.getOnlinePlayers().map { it.username }
    }
}