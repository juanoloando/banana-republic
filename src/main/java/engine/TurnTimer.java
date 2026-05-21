package engine;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.IntConsumer;

public class TurnTimer {

    public static final int TURN_DURATION_SECONDS = 90;

    private final ScheduledExecutorService scheduler;
    private ScheduledFuture<?> tickFuture;
    private ScheduledFuture<?> timeoutFuture;
    private final AtomicInteger secondsRemaining;
    private volatile boolean running;

    private Runnable onTimeout;
    private IntConsumer onTick;
    private Runnable uiDispatcher;

    public TurnTimer() {
        this.scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "TurnTimer-thread");
            t.setDaemon(true);
            return t;
        });
        this.secondsRemaining = new AtomicInteger(0);
        this.running = false;
    }

    public void setOnTimeout(Runnable onTimeout) {
        this.onTimeout = onTimeout;
    }

    public void setOnTick(IntConsumer onTick) {
        this.onTick = onTick;
    }

    public void setUiDispatcher(Runnable uiDispatcher) {
        this.uiDispatcher = uiDispatcher;
    }

    public void start() {
        assert onTimeout != null : "onTimeout callback must be set before starting timer";
        stop();

        secondsRemaining.set(TURN_DURATION_SECONDS);
        running = true;

        tickFuture = scheduler.scheduleAtFixedRate(() -> {
            if (!running) return;
            int remaining = secondsRemaining.decrementAndGet();
            if (onTick != null) {
                dispatchToUi(() -> onTick.accept(remaining));
            }
        }, 1, 1, TimeUnit.SECONDS);

        timeoutFuture = scheduler.schedule(() -> {
            if (!running) return;
            running = false;
            cancelTick();
            dispatchToUi(onTimeout);
        }, TURN_DURATION_SECONDS, TimeUnit.SECONDS);
    }

    public void stop() {
        running = false;
        cancelTick();
        if (timeoutFuture != null && !timeoutFuture.isDone()) {
            timeoutFuture.cancel(false);
        }
    }

    public void pause() {
        running = false;
        cancelTick();
        if (timeoutFuture != null && !timeoutFuture.isDone()) {
            timeoutFuture.cancel(false);
        }
    }

    public void resume() {
        if (secondsRemaining.get() <= 0) return;
        running = true;
        int remaining = secondsRemaining.get();

        tickFuture = scheduler.scheduleAtFixedRate(() -> {
            if (!running) return;
            int rem = secondsRemaining.decrementAndGet();
            if (onTick != null) {
                dispatchToUi(() -> onTick.accept(rem));
            }
        }, 1, 1, TimeUnit.SECONDS);

        timeoutFuture = scheduler.schedule(() -> {
            if (!running) return;
            running = false;
            cancelTick();
            dispatchToUi(onTimeout);
        }, remaining, TimeUnit.SECONDS);
    }

    public int getSecondsRemaining() {
        return secondsRemaining.get();
    }

    public boolean isRunning() {
        return running;
    }

    public void shutdown() {
        stop();
        scheduler.shutdownNow();
    }

    private void cancelTick() {
        if (tickFuture != null && !tickFuture.isDone()) {
            tickFuture.cancel(false);
        }
    }

    private void dispatchToUi(Runnable action) {
        if (uiDispatcher != null) {
            uiDispatcher.run();
        } else {
            action.run();
        }
    }

    public static TurnTimer createWithJavaFxDispatch(Runnable onTimeout, IntConsumer onTick) {
        TurnTimer timer = new TurnTimer();
        timer.setOnTimeout(() -> {
            try {
                Class<?> platformClass = Class.forName("javafx.application.Platform");
                java.lang.reflect.Method runLater =
                        platformClass.getMethod("runLater", Runnable.class);
                runLater.invoke(null, onTimeout);
            } catch (Exception e) {
                onTimeout.run();
            }
        });
        timer.setOnTick(remaining -> {
            try {
                Class<?> platformClass = Class.forName("javafx.application.Platform");
                java.lang.reflect.Method runLater =
                        platformClass.getMethod("runLater", Runnable.class);
                runLater.invoke(null, (Runnable) () -> onTick.accept(remaining));
            } catch (Exception e) {
                onTick.accept(remaining);
            }
        });
        return timer;
    }
}
