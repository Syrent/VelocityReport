package ir.syrent.velocityreport.spigot.event

import ir.syrent.velocityreport.report.Report
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

class PostReportUpdateEvent constructor(prevReport: Report, newReport: Report) : Event() {

    var prevReport: Report
    var newReport: Report

    init {
        this.prevReport = prevReport
        this.newReport = newReport
    }

    override fun getHandlers(): HandlerList {
        return HANDLERS
    }

    companion object {
        private val HANDLERS = HandlerList()

        fun getHandlerList(): HandlerList {
            return HANDLERS
        }
    }

}