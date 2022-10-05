package ir.syrent.velocityreport.spigot.event;

import ir.syrent.velocityreport.report.Report;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class PreReportEvent extends Event implements Cancellable {

    private Boolean cancelled = false;
    public Report report;

    private static final HandlerList HANDLERS = new HandlerList();

    public PreReportEvent(Report report) {
        this.report = report;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }

    public Report getReport() {
        return report;
    }

    public void setReport(Report report) {
        this.report = report;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
