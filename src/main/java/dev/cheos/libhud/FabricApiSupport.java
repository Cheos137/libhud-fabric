package dev.cheos.libhud;

import com.mojang.blaze3d.vertex.PoseStack;

import dev.cheos.libhud.api.Component;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;

public class FabricApiSupport implements Component {
	@Override
	public void render(LibhudGui gui, PoseStack poseStack, float partialTicks, int screenWidth, int screenHeight) {
		HudRenderCallback.EVENT.invoker().onHudRender(poseStack, partialTicks);
	}
}
