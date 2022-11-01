package ir.syrent.velocityreport.spigot.hook

object DependencyManager {

    var velocityVanishHook: VelocityVanishHook
        private set

    init {
        VelocityVanishHook("VelocityVanish").apply {
            this.register()
            velocityVanishHook = this
        }
    }

}