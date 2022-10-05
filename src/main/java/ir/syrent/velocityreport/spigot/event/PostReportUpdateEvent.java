package ir.syrent.velocityreport.spigot.event;

import ir.syrent.velocityreport.report.Report;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PostReportUpdateEvent extends Event {

    private Report prevReport;
    private Report newReport;

    private static final HandlerList HANDLERS = new HandlerList();


    public PostReportUpdateEvent(Report prevReport, Report newReport) {
        this.prevReport = prevReport;
        this.newReport = newReport;
    }

    public Report getPrevReport() {
        return prevReport;
    }

    public Report getNewReport() {
        return newReport;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
