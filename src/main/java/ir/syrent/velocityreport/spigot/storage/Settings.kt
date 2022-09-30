package ir.syrent.velocityreport.spigot.storage

import com.cryptomorin.xseries.XSound
import ir.syrent.velocityreport.report.Reason
import ir.syrent.velocityreport.spigot.Ruom
import ir.syrent.velocityreport.spigot.configuration.YamlConfig
import ir.syrent.velocityreport.utils.TextReplacement
import org.bukkit.Sound
import org.bukkit.configuration.file.FileConfiguration

object Settings {

    lateinit var settings: YamlConfig
    lateinit var language: YamlConfig
    private lateinit var settingsConfig: FileConfiguration
    private lateinit var languageConfig: FileConfiguration

    private val messages = mutableMapOf<Message, String>()
    val bookHeader = mutableListOf<String>()
    val bookFooter = mutableListOf<String>()
    val reportsBookHeader = mutableListOf<String>()
    val myReportsBookHeader = mutableListOf<String>()

    lateinit var defaultLanguage: String
    var velocitySupport = false

    lateinit var commandSound: Sound
    lateinit var bookSound: Sound

    var staffActionbarEnabled = true
    var staffActionbarSendZero = false
    var preventSelfReport = true
    var customReason = false
    var reasons = mutableListOf<Reason>()
    var cooldown = 0

    init {
        load()
    }

    fun load() {
        settings = YamlConfig(Ruom.getPlugin().dataFolder, "settings.yml")
        settingsConfig = settings.config

        defaultLanguage = settingsConfig.getString("default_language") ?: "en_US"
        velocitySupport = settingsConfig.getBoolean("velocity_support")

        commandSound = XSound.valueOf(settingsConfig.getString("sounds.command") ?: "ENTITY_EXPERIENCE_ORB_PICKUP").parseSound()!!
        bookSound = XSound.valueOf(settingsConfig.getString("sounds.book") ?: "ENTITY_EXPERIENCE_ORB_PICKUP").parseSound()!!

        staffActionbarEnabled = settingsConfig.getBoolean("report.staff_actionbar.enabled")
        staffActionbarSendZero = settingsConfig.getBoolean("report.staff_actionbar.send_zero")
        preventSelfReport = settingsConfig.getBoolean("report.prevent_self")
        customReason = settingsConfig.getBoolean("report.custom_reason")
        cooldown = settingsConfig.getInt("report.cooldown", 60)

        language = YamlConfig(Ruom.getPlugin().dataFolder, "languages/$defaultLanguage.yml")
        languageConfig = language.config

        reasons.apply {
            this.clear()
            val reasons = settingsConfig.getConfigurationSection("report.reasons") ?: return@apply
            for (reason in reasons.getKeys(false)) {
                val reasonConfig = reasons.getConfigurationSection(reason) ?: return@apply
                this.add(Reason(reason, reasonConfig.getBoolean("enabled"), reasonConfig.getString("displayname", "Unknown")!!, reasonConfig.getString("description", "")!!))
            }
        }

        reportsBookHeader.clear()
        reportsBookHeader.addAll(languageConfig.getStringList("command.reportadmin.reports.book.header"))
        myReportsBookHeader.clear()
        myReportsBookHeader.addAll(languageConfig.getStringList("command.reportadmin.myreports.book.header"))
        bookHeader.clear()
        bookHeader.addAll(languageConfig.getStringList("command.report.book.header"))
        bookFooter.clear()
        bookFooter.addAll(languageConfig.getStringList("command.report.book.footer"))

        messages.apply {
            this.clear()
            for (message in Message.values()) {
                if (message == Message.EMPTY) {
                    this[message] = ""
                    continue
                }

                this[message] = languageConfig.getString(message.path) ?: languageConfig.getString(Message.UNKNOWN_MESSAGE.path) ?: "Cannot find message: ${message.name}"
            }
        }

        settings.saveConfig()
        settings.reloadConfig()
        language.saveConfig()
        language.reloadConfig()
    }


    fun formatMessage(message: String, vararg replacements: TextReplacement): String {
        var formattedMessage = message
            .replace("\$prefix", getMessage(Message.PREFIX))
            .replace("\$successful_prefix", getMessage(Message.SUCCESSFUL_PREFIX))
            .replace("\$warn_prefix", getMessage(Message.WARN_PREFIX))
            .replace("\$error_prefix", getMessage(Message.ERROR_PREFIX))
        for (replacement in replacements) {
            formattedMessage = formattedMessage.replace("\$${replacement.from}", replacement.to)
        }
        return formattedMessage
    }

    fun formatMessage(message: Message, vararg replacements: TextReplacement): String {
        return formatMessage(getMessage(message), *replacements)
    }

    fun formatMessage(messages: List<String>, vararg replacements: TextReplacement): List<String> {
        val messageList = mutableListOf<String>()
        for (message in messages) {
            messageList.add(formatMessage(message, *replacements))
        }

        return messageList
    }

    private fun getMessage(message: Message): String {
        return messages[message] ?: messages[Message.UNKNOWN_MESSAGE]?.replace(
            "\$error_prefix",
            messages[Message.ERROR_PREFIX] ?: ""
        ) ?: "Unknown message ($message)"
    }

    fun getConsolePrefix(): String {
        return getMessage(Message.CONSOLE_PREFIX)
    }
}