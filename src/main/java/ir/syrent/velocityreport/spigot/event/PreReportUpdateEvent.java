package ir.syrent.velocityreport.spigot.event;

import ir.syrent.velocityreport.report.Report;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class PreReportUpdateEvent extends Event implements Cancellable {

    private Report prevReport;
    private Report newReport;

    private Boolean cancelled = false;

    private final static HandlerList HANDLERS = new HandlerList();

    public PreReportUpdateEvent(Report prevReport, Report newReport) {
        super(true);
        this.prevReport = prevReport;
        this.newReport = newReport;
    }

    public Report getPrevReport() {
        return prevReport;
    }

    public Report getNewReport() {
        return newReport;
    }

    public void setNewReport(Report newReport) {
        this.newReport = newReport;
    }


    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
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
