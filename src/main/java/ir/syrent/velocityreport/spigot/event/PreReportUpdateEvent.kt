package ir.syrent.velocityreport.spigot.event

import ir.syrent.velocityreport.report.Report
import org.bukkit.event.Cancellable
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

class PreReportUpdateEvent constructor(prevReport: Report, newReport: Report) : Event(), Cancellable {

    var prevReport: Report
    var newReport: Report

    private var cancellable = false

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

    override fun isCancelled(): Boolean {
        return cancellable
    }

    override fun setCancelled(cancel: Boolean) {
        this.cancellable = cancel
    }

}