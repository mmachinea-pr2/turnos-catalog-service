package ar.edu.programacion2.catalog.catalog.infrastructure.scheduling;

import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;

/** Inicia después del arranque y espera el intervalo desde el fin de cada tarea. */
public final class CatalogSynchronizationSchedule implements ApplicationListener<ApplicationReadyEvent>, DisposableBean {
    private final Runnable task;
    private final Duration interval;
    private final Duration initialDelay;
    private ScheduledExecutorService executor;
    private boolean closed;

    public CatalogSynchronizationSchedule(Runnable task, Duration interval, Duration initialDelay) {
        if (interval.compareTo(Duration.ofSeconds(1)) < 0 || interval.compareTo(Duration.ofDays(1)) > 0) {
            throw new IllegalArgumentException("Catalog check interval must be between 1 second and 1 day");
        }
        if (initialDelay.isNegative() || initialDelay.compareTo(Duration.ofDays(1)) > 0) {
            throw new IllegalArgumentException("Catalog initial delay must be between zero and 1 day");
        }
        this.task = java.util.Objects.requireNonNull(task);
        this.interval = interval;
        this.initialDelay = initialDelay;
    }

    @Override
    public synchronized void onApplicationEvent(ApplicationReadyEvent event) {
        if (executor != null || closed) {
            return;
        }
        executor = Executors.newSingleThreadScheduledExecutor(runnable -> {
            var thread = new Thread(runnable, "catalog-periodic-sync");
            thread.setDaemon(true);
            return thread;
        });
        executor.scheduleWithFixedDelay(task, initialDelay.toMillis(), interval.toMillis(),
                java.util.concurrent.TimeUnit.MILLISECONDS);
    }

    @Override
    public synchronized void destroy() {
        closed = true;
        if (executor != null) {
            executor.shutdownNow();
        }
    }
}
