package dev.cheos.libhud.api;

import com.mojang.blaze3d.vertex.PoseStack;

import dev.cheos.libhud.LibhudGui;
import net.minecraft.resources.ResourceLocation;

public interface Component {
	void render(LibhudGui gui, PoseStack poseStack, float partialTicks, int screenWidth, int screenHeight);
	
	public static NamedComponent named(ResourceLocation id, Component component) { return new NamedComponent(id, component); }
	public static NamedComponent named(String id, Component component) { return named(new ResourceLocation(id), component); }
	
	
	public static class NamedComponent implements Component {
		private final ResourceLocation id;
		private final Component component;
		
		private NamedComponent(ResourceLocation id, Component component) {
			this.id = id;
			this.component = component;
		}
		
		public ResourceLocation getName() {
			return this.id;
		}
		
		@Override
		public void render(LibhudGui gui, PoseStack poseStack, float partialTicks, int screenWidth, int screenHeight) {
			this.component.render(gui, poseStack, partialTicks, screenWidth, screenHeight);
		}
	}
}
