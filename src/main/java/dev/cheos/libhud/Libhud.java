package dev.cheos.libhud;

import java.lang.reflect.Method;
import java.util.List;
import java.util.function.Consumer;

import org.apache.commons.lang3.tuple.Triple;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.collect.ImmutableList;

import dev.cheos.libhud.api.LibhudApi;
import dev.cheos.libhud.api.event.*;
import dev.cheos.libhud.api.event.EventBus.EventPriority;
import net.fabricmc.loader.api.FabricLoader;

public class Libhud { // TODO mixin capture magic
	public static final Logger LOGGER = LogManager.getLogger("libhud");
	
	private static final List<Triple<String, Class<?>[], Consumer<LibhudApi>>> ENTRYPOINT_ADAPTERS = ImmutableList.of(
			Triple.of("on"                  , new Class[] { Event.class                   }, api -> registerListener(api::on)),
			Triple.of("onRegisterComponents", new Class[] { RegisterComponentsEvent.class }, api -> registerRegisterComponentListener(api::onRegisterComponents)),
			Triple.of("onRender"            , new Class[] { RenderEvent.class             }, api -> registerRenderListener(api::onRender)),
			Triple.of("onRenderComponent"   , new Class[] { RenderComponentEvent.class    }, api -> registerRenderComponentListener(api::onRenderComponent))
	);
	
	public void client() { // "main" fires before "client" -> event listeners should be either registered on <init>, "preLaunch" or "main" or (preferably) "libhud"
		FabricLoader.getInstance().getEntrypointContainers("libhud", LibhudApi.class).forEach(container -> {
			String modId = container.getProvider().getMetadata().getId();
			try {
				LibhudApi api = container.getEntrypoint();
				for (Triple<String, Class<?>[], Consumer<LibhudApi>> adapter : ENTRYPOINT_ADAPTERS)
					try {
						Method m = api.getClass().getDeclaredMethod(adapter.getLeft(), adapter.getMiddle());
						if (m != null) adapter.getRight().accept(api);
					} catch (NoSuchMethodException e) { } // ignore, we just don't add a listener if the method isn't found
			} catch (Throwable t) {
				LOGGER.error("Error initializing LibhudApi implementation of mod '{}'", modId, t);
			}
		});
		
		eventBus().post(new RegisterComponentsEvent(ComponentRegistry.INSTANCE));
	}
	
	
	public static EventBus eventBus() {
		return EventBus.LIBHUD_BUS;
	}
	
	public static void registerListener(Consumer<Event> listener) {
		eventBus().addListener(Event.class, listener, EventPriority.NORMAL, false);
	}
	
	public static void registerListener(Consumer<Event> listener, EventPriority prio, boolean receiveCancelled) {
		eventBus().addListener(Event.class, listener, prio == null ? EventPriority.NORMAL : prio, receiveCancelled);
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
