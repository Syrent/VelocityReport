package ir.syrent.velocityreport.spigot.storage

/**
 * This class responsible
 */
enum class Message(val path: String) {
    RAW_PREFIX("general.raw_prefix"),
    PREFIX("general.prefix"),
    CONSOLE_PREFIX("general.console_prefix"),
    SUCCESSFUL_PREFIX("general.successful_prefix"),
    WARN_PREFIX("general.warn_prefix"),
    ERROR_PREFIX("general.error_prefix"),
    ONLY_PLAYERS("general.only_players"),
    VALID_PARAMS("general.valid_parameters"),
    UNKNOWN_MESSAGE("general.unknown_message"),
    NO_PERMISSION("command.no_permission"),
    REPORT_USAGE("command.report.usage"),
    REPORT_USE("command.report.use"),
    NO_TARGET("command.report.no_target"),
    REPORT_COOLDOWN("command.report.cooldown"),
    EMPTY("");
}