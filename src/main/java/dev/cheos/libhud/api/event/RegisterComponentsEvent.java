package dev.cheos.libhud.api.event;

import dev.cheos.libhud.ComponentRegistry;
import dev.cheos.libhud.api.Component;
import dev.cheos.libhud.api.Component.NamedComponent;
import net.minecraft.resources.ResourceLocation;

public class RegisterComponentsEvent extends Event {
	private final ComponentRegistry registry;
	
	public RegisterComponentsEvent(ComponentRegistry registry) {
		super(false, EventPhase.NONE);
		this.registry = registry;
	}
	
	public void registerAbove(ResourceLocation other, ResourceLocation id, Component component) {
		this.registry.registerAbove(other, id, component);
	}
	
	public void registerAbove(ResourceLocation other, NamedComponent component) {
		this.registry.registerAbove(other, component);
	}
	
	public void registerBelow(ResourceLocation other, ResourceLocation id, Component component) {
		this.registry.registerBelow(other, id, component);
	}
	
	public void registerBelow(ResourceLocation other, NamedComponent component) {
		this.registry.registerBelow(other, component);
	}
	
	public void registerTop(ResourceLocation id, Component component) {
		this.registry.registerTop(id, component);
	}
	
	public void registerTop(NamedComponent component) {
		this.registry.registerTop(component);
	}
	
	public void registerBottom(ResourceLocation id, Component component) {
		this.registry.registerBottom(id, component);
	}
	
	public void registerBottom(NamedComponent component) {
		this.registry.registerBottom(component);
	}
	
	@Deprecated
	public void replace(ResourceLocation id, Component component) {
		this.registry.replace(id, component);
	}
	
	@Deprecated
	public void replace(NamedComponent component) {
		this.registry.replace(component);
	}
	
	@Deprecated
	public void unregister(ResourceLocation id) {
		this.registry.unregister(id);
	}
	
	@Deprecated
	public void unregister(NamedComponent component) {
		this.registry.unregister(component);
	}
}
