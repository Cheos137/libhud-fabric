package dev.cheos.libhud.api.event;

import java.util.*;
import java.util.function.Consumer;

import org.apache.commons.lang3.tuple.Pair;

public class EventBus {
	public static final EventBus LIBHUD_BUS = new EventBus();
	
	private final Map<Class<?>, Map<EventPriority, List<Pair<Boolean, Consumer<Event>>>>> listeners = new HashMap<>();
	
	public EventBus() { }
	
	public boolean post(Event event) {
		Class<?> clazz = event.getClass();
		while (clazz != Object.class) {
			Map<EventPriority, List<Pair<Boolean, Consumer<Event>>>> listeners = this.listeners.computeIfAbsent(clazz, k -> new HashMap<>());
			for (EventPriority prio : EventPriority.values())
				if (listeners.containsKey(prio))
					listeners.get(prio).forEach(c -> {
						if (!event.isCancelled() || c.getLeft())
							c.getRight().accept(event);
					});
			clazz = clazz.getSuperclass();
		}
		return event.isCancelled();
	}
	
	@SuppressWarnings("unchecked")
	public <T extends Event> void addListener(Class<T> eventClass, Consumer<? super T> listener, EventPriority prio, boolean receiveCancelled) {
		if (Event.class.isAssignableFrom(eventClass))
			this.listeners.computeIfAbsent(eventClass, k -> new HashMap<>())
					.computeIfAbsent(prio, k -> new LinkedList<>())
							.add(Pair.of(receiveCancelled, (Consumer<Event>) listener));
		else throw new IllegalArgumentException("eventClass must extend Event.class");
	}
	
	
	public static enum EventPriority {
		HIGHEST,
		HIGH,
		NORMAL,
		LOW,
		LOWEST;
	}
}
