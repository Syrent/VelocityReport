package ir.syrent.velocityreport.spigot.database;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import ir.syrent.velocityreport.database.Query;
import ir.syrent.velocityreport.database.mysql.MySQLCredentials;
import ir.syrent.velocityreport.database.mysql.MySQLExecutor;
import ir.syrent.velocityreport.spigot.Ruom;
import ir.syrent.velocityreport.utils.ServerVersion;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadFactory;

public class MySQLDatabase extends MySQLExecutor {

    private final static ThreadFactory THREAD_FACTORY = new ThreadFactoryBuilder().setNameFormat(Ruom.getPlugin().getName().toLowerCase() + "-mysql-thread-%d").build();

    private BukkitTask queueTask;

    public MySQLDatabase(MySQLCredentials credentials, int poolingSize) {
        super(credentials, poolingSize, THREAD_FACTORY);
    }

    @Override
    public void connect() {
        super.connect(ServerVersion.supports(13) && ServerVersion.getVersion() != 15 ? "com.mysql.cj.jdbc.Driver" : "com.mysql.jdbc.Driver");
        this.queueTask = startQueue();
    }

    @Override
    public void shutdown() {
        queueTask.cancel();
        queue.clear();
        hikari.close();
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

    public BukkitTask startQueue() {
        return new BukkitRunnable() {
            public void run() {
                if (poolingUsed >= poolingSize) {
                    tick(this);
                    return;
                }

                tick();

                tick(this);
            }
        }.runTask(Ruom.getPlugin());
    }

    protected CompletableFuture<Integer> executeQuery(Query query) {
        CompletableFuture<Integer> completableFuture = new CompletableFuture<>();

        Runnable runnable = () -> {
            Connection connection = createConnection();
            try {
                PreparedStatement preparedStatement = query.createPreparedStatement(connection);
                ResultSet resultSet = null;

                if (query.getStatement().startsWith("INSERT") ||
                        query.getStatement().startsWith("UPDATE") ||
                        query.getStatement().startsWith("DELETE") ||
                        query.getStatement().startsWith("CREATE") ||
                        query.getStatement().startsWith("ALTER"))
                    preparedStatement.executeUpdate();
                else
                    resultSet = preparedStatement.executeQuery();

                query.getCompletableFuture().complete(resultSet);

                closeConnection(connection);
                completableFuture.complete(Query.StatusCode.FINISHED.getCode());
            } catch (SQLException e) {
                Ruom.error("Failed to perform a query in the sqlite database. Stacktrace:");
                Ruom.debug("Statement: " + query.getStatement());
                e.printStackTrace();

                query.increaseFailedAttempts();
                if (query.getFailedAttempts() > failAttemptRemoval) {
                    closeConnection(connection);
                    completableFuture.complete(Query.StatusCode.FINISHED.getCode());
                    Ruom.warn("This query has been removed from the queue as it exceeded the maximum failures." +
                            " It's more likely to see some stuff break because of this failure, Please report" +
                            " this bug to the developers.\n" +
                            "Developer(s) of this plugin: " + Ruom.getPlugin().getDescription().getAuthors());
                }

                closeConnection(connection);
                completableFuture.complete(Query.StatusCode.FAILED.getCode());
            }
        };

        threadPool.submit(runnable);

        return completableFuture;
    }

    public void tick(Runnable runnable) {
        Ruom.runSync(runnable, 1);
    }

    private Connection createConnection() {
        try {
            return hikari.getConnection();
        } catch (SQLException e) {
            Ruom.error("Failed to establish mysql connection!");
            e.printStackTrace();
            return null;
        }
    }

    private void closeConnection(Connection connection) {
        try {
            connection.close();
        } catch (SQLException e) {
            Ruom.error("Failed to close a mysql connection!");
            e.printStackTrace();
        }
    }

    @Override
    protected void onQueryFail(Query query) { }

    @Override
    protected void onQueryRemoveDueToFail(Query query) { }

}
