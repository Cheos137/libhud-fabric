package dev.cheos.libhud.api.event;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.vertex.PoseStack;

public class RenderEvent extends Event {
	private final PoseStack poseStack;
	private final float partialTicks;
	private final Window window;
	
	public RenderEvent(PoseStack poseStack, float partialTicks, Window window, EventPhase phase) {
		super(true, phase);
		this.poseStack = poseStack;
		this.partialTicks = partialTicks;
		this.window = window;
	}
	
	public PoseStack getPoseStack() {
		return this.poseStack;
	}
	
	public float getPartialTicks() {
		return this.partialTicks;
	}
	
	public Window getWindow() {
		return this.window;
	}
}
