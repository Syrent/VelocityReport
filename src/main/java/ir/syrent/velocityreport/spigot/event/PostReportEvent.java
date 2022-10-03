package ir.syrent.velocityreport.spigot.event;

import ir.syrent.velocityreport.report.Report;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class PostReportEvent extends Event {

    private Report report;

    private static final HandlerList HANDLERS = new HandlerList();

    public PostReportEvent(Report report) {
        super(true);
        this.report = report;
    }

    public Report getReport() {
        return report;
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
