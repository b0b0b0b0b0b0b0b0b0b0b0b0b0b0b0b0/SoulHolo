package bm.b0b0b0.soulHolo.core;

import org.bukkit.plugin.java.JavaPlugin;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public final class PluginExecutor {

    private final ExecutorService ioExecutor;

    public PluginExecutor(JavaPlugin plugin, int ioThreads) {
        AtomicInteger counter = new AtomicInteger();
        ThreadFactory factory = runnable -> {
            Thread thread = new Thread(runnable, plugin.getName() + "-io-" + counter.incrementAndGet());
            thread.setDaemon(true);
            return thread;
        };
        int threads = Math.max(1, ioThreads);
        this.ioExecutor = Executors.newFixedThreadPool(threads, factory);
    }

    public ExecutorService io() {
        return ioExecutor;
    }

    public void shutdown() {
        ioExecutor.shutdown();
        try {
            if (!ioExecutor.awaitTermination(10, TimeUnit.SECONDS)) {
                ioExecutor.shutdownNow();
            }
        } catch (InterruptedException exception) {
            ioExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
