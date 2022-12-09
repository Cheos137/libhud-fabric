package dev.cheos.libhud;

import java.util.*;

import org.apache.commons.lang3.tuple.Pair;

import com.google.common.collect.ImmutableList;

import dev.cheos.libhud.api.Component;
import dev.cheos.libhud.api.Component.NamedComponent;
import net.minecraft.resources.ResourceLocation;

public class ComponentRegistry {
	public static final ComponentRegistry INSTANCE = new ComponentRegistry();
	
	static {
		VanillaComponents.register(INSTANCE);
	}
	
	private final Map<ResourceLocation, Component> components = new HashMap<>();
	private final Map<Component, ResourceLocation> componentsReversed = new HashMap<>();
	private final LinkedList<Component> orderedComponents = new LinkedList<>();
	private ImmutableList<Pair<ResourceLocation, Component>> immutableOrderedComponents = ImmutableList.of();
	
	public List<Pair<ResourceLocation, Component>> getComponents() {
		return this.immutableOrderedComponents;
	}
	
	public void registerAbove(ResourceLocation other, ResourceLocation id, Component component) {
		if (!components.containsKey(other))
			registerTop(id, component);
		else {
			register(id, component);
			this.orderedComponents.add(this.orderedComponents.indexOf(this.components.get(other)) + 1, component);
			updateCache();
		}
	}
	
	public void registerAbove(ResourceLocation other, NamedComponent component) {
		registerAbove(other, component.getName(), component);
	}
	
	public void registerBelow(ResourceLocation other, ResourceLocation id, Component component) {
		if (!components.containsKey(other))
			registerBottom(id, component);
		else {
			register(id, component);
			this.orderedComponents.add(this.orderedComponents.indexOf(this.components.get(other)), component);
			updateCache();
		}
	}
	
	public void registerBelow(ResourceLocation other, NamedComponent component) {
		registerBelow(other, component.getName(), component);
	}
	
	public void registerTop(ResourceLocation id, Component component) {
		register(id, component);
		this.orderedComponents.addLast(component);
		updateCache();
	}
	
	public void registerTop(NamedComponent component) {
		registerTop(component.getName(), component);
	}
	
	public void registerBottom(ResourceLocation id, Component component) {
		register(id, component);
		this.orderedComponents.addFirst(component);
		updateCache();
	}
	
	public void registerBottom(NamedComponent component) {
		registerBottom(component.getName(), component);
	}
	
	@Deprecated
	public void replace(ResourceLocation id, Component component) {
		if (!this.components.containsKey(id))
			registerTop(id, component);
		else {
			this.orderedComponents.set(this.orderedComponents.indexOf(this.components.get(id)), component);
			this.components.put(id, component);
			updateCache();
		}
	}
	
	@Deprecated
	public void replace(NamedComponent component) {
		replace(component.getName(), component);
	}
	
	@Deprecated
	public void unregister(ResourceLocation id) {
		if (!this.components.containsKey(id)) return;
		this.orderedComponents.remove(this.components.get(id));
		this.components.remove(id);
		updateCache();
	}
	
	private void register(ResourceLocation id, Component component) {
		if (this.components.containsKey(id))
			throw new IllegalArgumentException("tried to register component " + id + " twice. Use #unregister(ResourceLocation) or replace(ResourceLocation, Component) if it is desired to replace another component.");
		this.components.put(id, component);
	}
	
	private void updateCache() {
		this.immutableOrderedComponents = this.orderedComponents.stream().map(c -> Pair.of(this.componentsReversed.get(c), c)).collect(ImmutableList.toImmutableList());
	}
}
