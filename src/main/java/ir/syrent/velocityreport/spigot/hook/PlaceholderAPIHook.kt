package ir.syrent.velocityreport.spigot.hook

import ir.syrent.velocityreport.report.ReportStage
import ir.syrent.velocityreport.spigot.Ruom
import ir.syrent.velocityreport.spigot.VelocityReportSpigot
import ir.syrent.velocityreport.spigot.storage.Database
import ir.syrent.velocityreport.spigot.storage.Settings
import me.clip.placeholderapi.expansion.PlaceholderExpansion
import org.bukkit.OfflinePlayer

class PlaceholderAPIHook constructor(plugin: VelocityReportSpigot, name: String) : Dependency(name) {


    val reportsCache = mutableMapOf<ReportStage, Int>()

    init {
        if (exists && Settings.placeholderHookEnabled) {
            ReportExpansion(plugin).register()
        }

        Ruom.runSync({
            if (!Settings.placeholderHookEnabled) return@runSync
            for (stage in ReportStage.entries) {
                Database.getReports(stage).whenComplete { reports, _ ->
                    reportsCache[stage] = reports.size
                }
            }
        }, 0, Settings.placeholderUpdateTicks)
    }

    override fun features(): List<String> {
        return mutableListOf(
            "Access to all placeholders in all plugin messages.",
            "Add plugin placeholders like %velocityreport_reports_[stage]% to PlaceholderAPI."
        )
    }

    inner class ReportExpansion(
        private val plugin: VelocityReportSpigot
    ) : PlaceholderExpansion() {
        override fun getIdentifier(): String {
            return Ruom.getPlugin().description.name.lowercase()
        }

        override fun getAuthor(): String {
            return Ruom.getPlugin().description.authors.joinToString(", ")
        }

        override fun getVersion(): String {
            return Ruom.getPlugin().description.version
        }

        override fun persist(): Boolean {
            return true
        }

        override fun canRegister(): Boolean {
            return true
        }

        override fun onRequest(player: OfflinePlayer, params: String): String? {
            if (params.startsWith("reports_")) {
                val type = params.substring(8)
                val amount = reportsCache[ReportStage.valueOf(type.uppercase())]
                val amountFormat = if (amount == Settings.databaseFetchLimit) "${Settings.databaseFetchLimit}+" else amount.toString()

                return amountFormat
            }
            return null
        }
    }

}