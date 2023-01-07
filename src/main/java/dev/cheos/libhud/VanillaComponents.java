package dev.cheos.libhud;

import static dev.cheos.libhud.api.Component.*;

import dev.cheos.libhud.api.Component.NamedComponent;
import net.fabricmc.loader.api.FabricLoader;

public class VanillaComponents {
	public static final NamedComponent HOTBAR         = named("minecraft:hotbar"        , (gui, poseStack, partialTicks, screenWidth, screenHeight) -> gui.renderHotbar(partialTicks, poseStack));
	public static final NamedComponent CROSSHAIR      = named("minecraft:crosshair"     , (gui, poseStack, partialTicks, screenWidth, screenHeight) -> gui.renderCrosshair(poseStack));
	public static final NamedComponent EXPERIENCE_BAR = named("minecraft:experience_bar", (gui, poseStack, partialTicks, screenWidth, screenHeight) -> gui.renderExperienceBar(poseStack));
	public static final NamedComponent JUMP_METER     = named("minecraft:jump_meter"    , (gui, poseStack, partialTicks, screenWidth, screenHeight) -> gui.renderJumpMeter(poseStack));
	public static final NamedComponent BOSS_HEALTH    = named("minecraft:boss_health"   , (gui, poseStack, partialTicks, screenWidth, screenHeight) -> gui.renderBossHealth(poseStack));
	public static final NamedComponent ARMOR          = named("minecraft:armor"         , (gui, poseStack, partialTicks, screenWidth, screenHeight) -> gui.renderArmor(poseStack));
	public static final NamedComponent HEALTH         = named("minecraft:health"        , (gui, poseStack, partialTicks, screenWidth, screenHeight) -> gui.renderHearts(poseStack));
	public static final NamedComponent FOOD           = named("minecraft:food"          , (gui, poseStack, partialTicks, screenWidth, screenHeight) -> gui.renderFood(poseStack));
	public static final NamedComponent AIR            = named("minecraft:air"           , (gui, poseStack, partialTicks, screenWidth, screenHeight) -> gui.renderAir(poseStack));
	public static final NamedComponent VEHICLE_HEALTH = named("minecraft:vehicle_health", (gui, poseStack, partialTicks, screenWidth, screenHeight) -> gui.renderVehicleHealth(poseStack));
	public static final NamedComponent ITEM_NAME      = named("minecraft:item_name"     , (gui, poseStack, partialTicks, screenWidth, screenHeight) -> gui.renderSelectedItemName(poseStack));
	
	static void register(ComponentRegistry registry) {
		registry.registerTop(HOTBAR);
		registry.registerTop(CROSSHAIR);
		registry.registerTop(EXPERIENCE_BAR);
		registry.registerTop(JUMP_METER);
		registry.registerTop(BOSS_HEALTH);
		registry.registerTop(HEALTH);
		registry.registerTop(ARMOR);
		registry.registerTop(FOOD);
		registry.registerTop(VEHICLE_HEALTH);
		registry.registerTop(AIR);
		if (isLoaded("fabric") || isLoaded("fabric-api") || isLoaded("fabric-rendering-v1"))
			registry.registerTop(named("fabric:api_callback", new FabricApiSupport())); // adding the fabric api callback here fixes most of the ordering issues encountered by mods listening to the callback
		registry.registerTop(ITEM_NAME);
	}
	
	private static boolean isLoaded(String mod) {
		return FabricLoader.getInstance().isModLoaded(mod);
	}
}
