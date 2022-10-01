package ir.syrent.velocityreport.spigot.event

import ir.syrent.velocityreport.report.Report
import org.bukkit.event.Cancellable
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

class PreReportEvent constructor(report: Report) : Event(), Cancellable {

    var report: Report

    private var cancelled = false

    init {
        this.report = report
    }

    override fun getHandlers(): HandlerList {
        return HANDLERS
    }

    override fun isCancelled(): Boolean {
        return cancelled
    }

    override fun setCancelled(cancel: Boolean) {
        this.cancelled = cancel
    }

    companion object {
        private val HANDLERS = HandlerList()

        fun getHandlerList(): HandlerList {
            return HANDLERS
        }
    }

}