package ir.syrent.velocityreport.spigot.storage

import ir.syrent.velocityreport.database.Priority
import ir.syrent.velocityreport.database.Query
import ir.syrent.velocityreport.database.mysql.MySQLCredentials
import ir.syrent.velocityreport.report.Report
import ir.syrent.velocityreport.spigot.Ruom
import ir.syrent.velocityreport.spigot.configuration.YamlConfig
import ir.syrent.velocityreport.spigot.database.MySQLDatabase
import ir.syrent.velocityreport.spigot.database.sqlite.SQLiteDatabase
import org.bukkit.configuration.ConfigurationSection
import java.io.File

object Database {

    private var database: ir.syrent.velocityreport.database.Database? = null
    var type: DBType = DBType.SQLITE

    init {
        val storage = YamlConfig(Ruom.getPlugin().dataFolder, "storage.yml")

        if (storage.config.getString("type").equals("MySQL", true)) {
            val section: ConfigurationSection = storage.config.getConfigurationSection("mysql")!!
            val credentials = MySQLCredentials.mySQLCredentials(
                section.getString("address"),
                section.getInt("port"),
                section.getString("database"),
                section.getBoolean("ssl"),
                section.getString("username")!!,
                section.getString("password")!!
            )
            database = MySQLDatabase(credentials, section.getInt("pooling_size"))
            type = DBType.MYSQL
        } else {
            database = SQLiteDatabase(File(Ruom.getPlugin().dataFolder, "storage.db"))
        }

        database!!.connect()
        database!!.queueQuery(Query.query("CREATE TABLE IF NOT EXISTS velocityreport_reports (report_id VARCHAR(64), reporter_id VARCHAR(64), reporter_name VARCHAR(16), reported_name VARCHAR(16), date BIGINT, reason VARCHAR(128), moderator_id VARCHAR(64), moderator_name VARCHAR(16), stage VARCHAR(64), PRIMARY KEY (report_id));"), Priority.HIGHEST)
    }

    fun shutdown() {
        database!!.shutdown()
    }

    fun saveReport(report: Report) {
        database!!.queueQuery(
            Query.query("SELECT report_id FROM velocityreport_reports WHERE report_id = ?;")
                .setStatementValue(1, report.reportID.toString())).completableFuture.whenComplete { result, _ ->
            if (!result.next()) {
                database!!.queueQuery(
                    Query.query("INSERT ${if (type == DBType.MYSQL) "IGNORE " else ""}INTO velocityreport_reports (report_id, reporter_id, reporter_name, reported_name, date, reason, moderator_id, moderator_name, stage) VALUES (?,?,?,?,?,?,?,?,?);")
                        .setStatementValue(1, report.reportID.toString())
                        .setStatementValue(2, report.reporterID.toString())
                        .setStatementValue(3, report.reporterName)
                        .setStatementValue(4, report.reportedName)
                        .setStatementValue(5, report.date)
                        .setStatementValue(6, report.reason)
                        .setStatementValue(7, report.moderatorUUID.toString())
                        .setStatementValue(8, report.moderatorName ?: "Unknown")
                        .setStatementValue(9, report.stage.name))
            } else {
                database!!.queueQuery(
                    Query.query("UPDATE velocityreport_reports SET stage = ?, moderator_id = ?, moderator_name = ? WHERE report_id = ?;")
                        .setStatementValue(1, report.moderatorUUID.toString())
                        .setStatementValue(2, report.moderatorName ?: "Unknown")
                        .setStatementValue(3, report.reportID.toString()))
            }
        }
    }

    enum class DBType {
        MYSQL,
        SQLITE
    }
}