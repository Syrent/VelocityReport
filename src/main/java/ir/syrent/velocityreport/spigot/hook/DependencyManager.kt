package ir.syrent.velocityreport.spigot.hook

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
        PlaceholderAPIHook(VelocityVanishSpigot.instance, "PlaceholderAPI").apply {
            this.register()
            placeholderAPIHook = this
        }
    }

}