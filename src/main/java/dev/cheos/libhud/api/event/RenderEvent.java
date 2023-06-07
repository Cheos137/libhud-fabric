package dev.cheos.libhud.api.event;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.gui.GuiGraphics;

public class RenderEvent extends Event {
	private final GuiGraphics graphics;
	private final float partialTicks;
	@Deprecated
	private final Window window;
	
	public RenderEvent(GuiGraphics graphics, float partialTicks, Window window, EventPhase phase) {
		super(true, phase);
		this.graphics = graphics;
		this.partialTicks = partialTicks;
		this.window = window;
	}
	
	public PoseStack getPoseStack() {
		return this.graphics.pose();
	}
	
	public GuiGraphics getGraphics() {
		return this.graphics;
	}
	
	public float getPartialTicks() {
		return this.partialTicks;
	}
	
	/**
	 * @see {@link #getGraphics()}
	 * @deprecated use {@link #getGraphics()} to acquire scaled width/height
	 */
	@Deprecated
	public Window getWindow() {
		return this.window;
	}
}
