package plugin;

import java.time.Instant;


/**
 * Abstract class buat semua event dalam sistem EventBus, tiap event punya timestamp otomatis
 * 
 */
public abstract class GameEvent {

    private final Instant timestamp;

    protected GameEvent() {
        this.timestamp = Instant.now();
    }

    public abstract String getEventName();

    public Instant getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return getEventName() + "@" + timestamp;
    }
}
