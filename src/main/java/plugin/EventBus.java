package plugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.logging.Logger;

// EventBus berguna untuk komunikasi antar komponen game secara
public class EventBus {

    private static final Logger LOGGER = Logger.getLogger(EventBus.class.getName());
    
    private static volatile EventBus instance;

    // Map dari kelas event ke list subscriber 
    private final Map<Class<? extends GameEvent>, List<Consumer<? super GameEvent>>> subscribers;

    private EventBus() {
        this.subscribers = new ConcurrentHashMap<>();
    }

    public static EventBus getInstance() {
        if (instance == null) {
            synchronized (EventBus.class) {
                if (instance == null) {
                    instance = new EventBus();
                }
            }
        }
        return instance;
    }

    public <T extends GameEvent> void subscribe(Class<T> eventType, Consumer<T> subscriber) {
        assert eventType != null : "Event type tidak boleh null";
        assert subscriber != null : "Subscriber tidak boleh null";

        subscribers.computeIfAbsent(eventType, k -> new ArrayList<>())
            .add(event -> subscriber.accept(eventType.cast(event)));
        LOGGER.fine("Subscribed to " + eventType.getSimpleName());
    }



    public void publish(GameEvent event) {
        assert event != null : "Tidak bisa publish null event";

        List<Consumer<? super GameEvent>> handlers = subscribers.get(event.getClass());
        if (handlers == null || handlers.isEmpty()) {
            LOGGER.fine("Tidak ada subscriber untuk event: " + event.getEventName());
            return;
        }
        for (Consumer<? super GameEvent> handler : handlers) {
            try {
                handler.accept(event);
            } catch (Exception e) {
                LOGGER.warning("Subscriber threw exception on event "
                    + event.getEventName() + ": " + e.getMessage());
            }
        }
    }

    public <T extends GameEvent> void clearSubscribers(Class<T> eventType) {
        assert eventType != null : "Event type tidak boleh null";
        subscribers.remove(eventType);
    }

    
    public void clearAll() {
        subscribers.clear();
        LOGGER.info("EventBus menghapus semua subscribers.");
    }
    
    public int getSubscriberCount(Class<? extends GameEvent> eventType) {
        List<Consumer<? super GameEvent>> list = subscribers.get(eventType);
        return list == null ? 0 : list.size();
    }
}
