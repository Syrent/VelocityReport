package ir.syrent.velocityreport.spigot.core

import ir.syrent.velocityreport.spigot.Ruom
import ir.syrent.velocityreport.spigot.adventure.AdventureApi
import ir.syrent.velocityreport.spigot.storage.Settings
import ir.syrent.velocityreport.utils.component

object DependencyChecker {

    private val registeredDependencies = mutableSetOf<String>()

    fun register(name: String) {
        if (Ruom.getServer().pluginManager.getPlugin(name) != null) {
            AdventureApi.get().console().sendMessage("${Settings.getConsolePrefix()} <aqua>$name</aqua> <gradient:dark_green:green>found! enabling hook...</gradient>".component())
            registeredDependencies.add(name)
            AdventureApi.get().console().sendMessage("${Settings.getConsolePrefix()} <aqua>$name</aqua> <gradient:dark_green:green>found! enabling hook...</gradient> <dark_green>[DONE]</dark_green>".component())
        } else {
            AdventureApi.get().console().sendMessage("${Settings.getConsolePrefix()} <aqua>$name</aqua> <gradient:dark_red:red>not found! disabling hook...</gradient> <dark_green>[DONE]</dark_green>".component())
        }
    }

    fun isRegistered(vararg names: String): Boolean {
        var has = true
        for (name in names) {
            if (!registeredDependencies.contains(name)) {
                has = false
            }
        }
        return has
    }
}