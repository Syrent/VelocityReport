package ir.syrent.velocityreport.report

data class Reason(
    val id: String,
    val enabled: Boolean,
    val displayName: String,
    val description: String
)