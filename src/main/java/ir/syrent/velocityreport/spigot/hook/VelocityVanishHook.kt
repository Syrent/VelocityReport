package ir.syrent.velocityreport.spigot.hook

class VelocityVanishHook constructor(name: String) : Dependency(name) {

    override fun features(): List<String> {
        return mutableListOf(
            "Prevent players from reporting vanished players."
        )
    }

}