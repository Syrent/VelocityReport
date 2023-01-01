package ir.syrent.velocityreport.spigot.hook

import ir.syrent.velocityreport.spigot.VelocityReportSpigot
import ir.syrent.velocityreport.spigot.ruom.Ruom
import ir.syrent.velocityreport.spigot.storage.Settings
import me.clip.placeholderapi.expansion.PlaceholderExpansion
import org.bukkit.OfflinePlayer

class PlaceholderAPIHook constructor(plugin: VelocityReportPlugin, name: String) : Dependency(name) {


    val reportsCache = mutableMapOf<ReportStage, Int>
    init {
        if (exists) {
            ReportExpansion(plugin).register()
        }

        Ruom.runSync({
            for (stage in ReportStage.values()) {
                Database.getReports(ReportStage.ACTIVE).whenComplete { reports, _ ->
                    reportsCache[stage] = reports.size
                }
            }
        }, 0, 5 * 20)
    }

    override fun features(): List<String> {
        return mutableListOf(
            "Access to all placeholders in all plugin messages.",
            "Add plugin placeholders like %velocityreport_reports_pending% to PlaceholderAPI."
        )
    }

    class ReportExpansion(
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
            if (params.startsWith("velocityreport_reports_")) {
                val type = params.removeSuffix("velocityreport_reports_")

                return reportsCache[ReportStage.valueOf(type.toUpperCase())].toString()

            return null
        }
    }

}