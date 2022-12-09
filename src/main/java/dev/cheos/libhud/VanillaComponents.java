package dev.cheos.libhud;

import static dev.cheos.libhud.api.Component.*;

import dev.cheos.libhud.api.Component.NamedComponent;

public class VanillaComponents {
	static final NamedComponent HOTBAR         = named("minecraft:hotbar"        , (gui, poseStack, partialTicks, screenWidth, screenHeight) -> gui.renderHotbar(partialTicks, poseStack));
	static final NamedComponent CROSSHAIR      = named("minecraft:crosshair"     , (gui, poseStack, partialTicks, screenWidth, screenHeight) -> gui.renderCrosshair(poseStack));
	static final NamedComponent BOSS_HEALTH    = named("minecraft:boss_health"   , (gui, poseStack, partialTicks, screenWidth, screenHeight) -> gui.renderBossHealth(poseStack));
	static final NamedComponent ARMOR          = named("minecraft:armor"         , (gui, poseStack, partialTicks, screenWidth, screenHeight) -> gui.renderArmor(poseStack));
	static final NamedComponent HEALTH         = named("minecraft:health"        , (gui, poseStack, partialTicks, screenWidth, screenHeight) -> gui.renderHearts(poseStack));
	static final NamedComponent FOOD           = named("minecraft:food"          , (gui, poseStack, partialTicks, screenWidth, screenHeight) -> gui.renderFood(poseStack));
	static final NamedComponent AIR            = named("minecraft:air"           , (gui, poseStack, partialTicks, screenWidth, screenHeight) -> gui.renderAir(poseStack));
	static final NamedComponent VEHICLE_HEALTH = named("minecraft:vehicle_health", (gui, poseStack, partialTicks, screenWidth, screenHeight) -> gui.renderVehicleHealth(poseStack));
	static final NamedComponent ITEM_NAME      = named("minecraft:item_name"     , (gui, poseStack, partialTicks, screenWidth, screenHeight) -> gui.renderSelectedItemName(poseStack));

	static void register(ComponentRegistry registry) {
		registry.registerTop(HOTBAR);
		registry.registerTop(CROSSHAIR);
		registry.registerTop(BOSS_HEALTH);
		registry.registerTop(ARMOR);
		registry.registerTop(HEALTH);
		registry.registerTop(FOOD);
		registry.registerTop(AIR);
		registry.registerTop(VEHICLE_HEALTH);
		registry.registerTop(ITEM_NAME);
	}
}
