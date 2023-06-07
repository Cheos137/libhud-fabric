package dev.cheos.libhud;

import dev.cheos.libhud.api.Component;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.gui.GuiGraphics;

public class FabricApiSupport implements Component {
	@Override
	public void render(LibhudGui gui, GuiGraphics graphics, float partialTicks, int screenWidth, int screenHeight) {
		HudRenderCallback.EVENT.invoker().onHudRender(graphics, partialTicks);
	}
}
