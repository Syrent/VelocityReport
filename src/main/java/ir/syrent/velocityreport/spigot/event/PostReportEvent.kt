package ir.syrent.velocityreport.spigot.event

import ir.syrent.velocityreport.report.Report
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

class PostReportEvent constructor(report: Report) : Event() {

    var report: Report

    init {
        this.report = report
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