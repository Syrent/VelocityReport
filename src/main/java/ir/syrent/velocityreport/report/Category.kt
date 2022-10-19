package ir.syrent.velocityreport.report

data class Category(
    val id: String,
    val enabled: Boolean,
    val displayName: String,
    val reasons: List<Reason>
)