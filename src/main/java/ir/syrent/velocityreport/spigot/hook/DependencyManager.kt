package ir.syrent.velocityreport.spigot.hook

import ir.syrent.velocityreport.spigot.VelocityReportSpigot

object DependencyManager {

    var sayanVanishHook: SayanVanishHook
        private set
    var placeholderAPIHook: PlaceholderAPIHook
        private set

    init {
        SayanVanishHook("SayanVanish").apply {
            this.register()
            sayanVanishHook = this
        }
        PlaceholderAPIHook(VelocityReportSpigot.instance, "PlaceholderAPI").apply {
            this.register()
            placeholderAPIHook = this
        }
    }

}