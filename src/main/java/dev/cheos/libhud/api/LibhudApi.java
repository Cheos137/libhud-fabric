package dev.cheos.libhud.api;

import dev.cheos.libhud.api.event.*;

public interface LibhudApi {
	default void on(Event event) { }
	default void onRegisterComponents(RegisterComponentsEvent event) { }
	default void onRender(RenderEvent event) { }
	default void onRenderComponent(RenderComponentEvent event) { }
}
