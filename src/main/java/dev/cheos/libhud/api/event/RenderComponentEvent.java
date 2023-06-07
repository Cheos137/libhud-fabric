package dev.cheos.libhud.api.event;

import com.mojang.blaze3d.platform.Window;

import dev.cheos.libhud.api.Component;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

public class RenderComponentEvent extends RenderEvent {
	private final ResourceLocation componentId;
	private final Component component;
	
	public RenderComponentEvent(GuiGraphics graphics, float partialTicks, Window window, ResourceLocation componentId, Component component, EventPhase phase) {
		super(graphics, partialTicks, window, phase);
		this.componentId = componentId;
		this.component = component;
	}
	
	public ResourceLocation getComponentId() {
		return this.componentId;
	}
	
	public Component getComponent() {
		return this.component;
	}
}
