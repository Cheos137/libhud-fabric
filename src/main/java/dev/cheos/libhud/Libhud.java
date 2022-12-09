package dev.cheos.libhud;

import java.util.function.Consumer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import dev.cheos.libhud.api.event.*;
import dev.cheos.libhud.api.event.EventBus.EventPriority;

public class Libhud { // TODO mixin capture magic
	public static final Logger LOGGER = LogManager.getLogger("libhud");
	
	public void client() {
		
	}
	
	
	public static EventBus eventBus() {
		return EventBus.LIBHUD_BUS;
	}
	
	public static void registerRegisterComponentListener(Consumer<RegisterComponentsEvent> listener) {
		eventBus().addListener(RegisterComponentsEvent.class, listener, EventPriority.NORMAL, false);
	}
	
	public static void registerRegisterComponentListener(Consumer<RegisterComponentsEvent> listener, EventPriority prio, boolean receiveCancelled) {
		eventBus().addListener(RegisterComponentsEvent.class, listener, prio == null ? EventPriority.NORMAL : prio, receiveCancelled);
	}
	
	public static void registerRenderListener(Consumer<RenderEvent> listener) {
		eventBus().addListener(RenderEvent.class, listener, EventPriority.NORMAL, false);
	}
	
	public static void registerRenderListener(Consumer<RenderEvent> listener, EventPriority prio, boolean receiveCancelled) {
		eventBus().addListener(RenderEvent.class, listener, prio == null ? EventPriority.NORMAL : prio, receiveCancelled);
	}
	
	public static void registerRenderComponentListener(Consumer<RenderComponentEvent> listener) {
		eventBus().addListener(RenderComponentEvent.class, listener, EventPriority.NORMAL, false);
	}
	
	public static void registerRenderComponentListener(Consumer<RenderComponentEvent> listener, EventPriority prio, boolean receiveCancelled) {
		eventBus().addListener(RenderComponentEvent.class, listener, prio == null ? EventPriority.NORMAL : prio, receiveCancelled);
	}
}
