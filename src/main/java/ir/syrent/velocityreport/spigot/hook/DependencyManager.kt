package ir.syrent.velocityreport.spigot.hook

import ir.syrent.velocityreport.spigot.VelocityReportSpigot
import ir.syrent.velocityvanish.spigot.VelocityVanishSpigot

object DependencyManager {

    var velocityVanishHook: VelocityVanishHook
        private set
    var placeholderAPIHook: PlaceholderAPIHook
        private set

    init {
        VelocityVanishHook("VelocityVanish").apply {
            this.register()
            velocityVanishHook = this
        }
        PlaceholderAPIHook(VelocityReportSpigot.instance, "PlaceholderAPI").apply {
            this.register()
            placeholderAPIHook = this
        }
    }

}