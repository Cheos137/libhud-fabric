package dev.cheos.libhud.mixin;

import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import dev.cheos.libhud.Libhud;
import dev.cheos.libhud.LibhudGui;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.main.GameConfig;
import net.minecraft.client.renderer.entity.ItemRenderer;

@Mixin(Minecraft.class)
public class MinecraftMixin {
	@Shadow @Mutable private Gui gui;
	@Shadow @Final private ItemRenderer itemRenderer;
	
	@Inject(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;<init>(Lnet/minecraft/client/Minecraft;Lnet/minecraft/client/renderer/entity/ItemRenderer;)V", shift = Shift.BY, by = 2))
	public void libhud$init(GameConfig gameConfig, CallbackInfo ci) {
		Libhud.LOGGER.info("Injecting LibhudGui instance into Minecraft");
		this.gui = new LibhudGui((Minecraft) (Object) this, this.itemRenderer);
	}
}
