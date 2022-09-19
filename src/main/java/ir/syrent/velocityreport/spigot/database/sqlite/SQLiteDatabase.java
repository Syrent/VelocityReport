package ir.syrent.velocityreport.spigot.database.sqlite;

import ir.syrent.velocityreport.database.Query;
import ir.syrent.velocityreport.database.sqlite.SQLiteExecutor;
import ir.syrent.velocityreport.spigot.Ruom;
import ir.syrent.velocityreport.spigot.database.sqlite.exception.SQLiteException;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.sql.SQLException;
import java.util.concurrent.CompletableFuture;

public class SQLiteDatabase extends SQLiteExecutor {

    private BukkitTask queueTask;

    public SQLiteDatabase(File dbFile) {
        super(dbFile, Ruom.getPlugin().getLogger());
    }

    @Override
    public void connect() {
        super.connect();
        queueTask = startQueue();
    }

    @Override
    public void shutdown() {
        try {
            connection.close();
            queue.clear();
            queueTask.cancel();
        } catch (SQLException e) {
            throw new SQLiteException(e.getMessage());
        }
    }

    @Override
    public CompletableFuture<Void> scheduleShutdown() {
        CompletableFuture<Void> completableFuture = new CompletableFuture<>();
        Ruom.runAsync(() -> {
            if (isQueueEmpty()) {
                shutdown();
                completableFuture.complete(null);
            }
        }, 0, 1);
        return completableFuture;
    }

    protected BukkitTask startQueue() {
        return new BukkitRunnable() {
            public void run() {
                tick();
                tick(this);
            }
        }.runTaskAsynchronously(Ruom.getPlugin());
    }

    private void tick(Runnable runnable) {
        Ruom.runSync(runnable, 1);
    }

    @Override
    protected void onQueryFail(Query query) {
        Ruom.error("Failed to perform a query in the sqlite database. Stacktrace:");
        Ruom.debug("Statement: " + query.getStatement());
    }

    @Override
    protected void onQueryRemoveDueToFail(Query query) {
        Ruom.warn("This query has been removed from the sqlite queue as it exceeded the maximum failures." +
                " It's more likely to see some stuff break because of this failure, Please report" +
                " this bug to the developers.\n" +
                "Developer(s) of this plugin: " + Ruom.getPlugin().getDescription().getAuthors());
    }

}
