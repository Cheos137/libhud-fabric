package dev.cheos.libhud;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import dev.cheos.libhud.api.Component;
import dev.cheos.libhud.api.event.Event.EventPhase;
import dev.cheos.libhud.api.event.EventBus;
import dev.cheos.libhud.api.event.RenderComponentEvent;
import dev.cheos.libhud.api.event.RenderEvent;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PlayerRideableJumping;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;
import org.apache.commons.lang3.tuple.Pair;

public class LibhudGui extends Gui {
	public int leftOffset = 39, rightOffset = 39;
	
	public LibhudGui(Minecraft minecraft, ItemRenderer itemRenderer) {
		super(minecraft, itemRenderer);
	}
	
	@Override
	public void render(GuiGraphics graphics, float partialTicks) {
		Window window = this.minecraft.getWindow();
		this.screenWidth = graphics.guiWidth();
		this.screenHeight = graphics.guiHeight();
		this.leftOffset = this.rightOffset = 39;
		Font font = this.getFont();
		RenderSystem.enableBlend();
		
		if (EventBus.LIBHUD_BUS.post(new RenderEvent(graphics, partialTicks, window, EventPhase.PRE))) return;
		
		if (Minecraft.useFancyGraphics()) {
			this.renderVignette(graphics, this.minecraft.getCameraEntity());
		} else
			// otherwise set by #renderVignette
			RenderSystem.enableDepthTest();
		
		float delta = this.minecraft.getDeltaFrameTime();
		this.scopeScale = Mth.lerp(0.5F * delta, this.scopeScale, 1.125F);
		if (this.minecraft.options.getCameraType().isFirstPerson()) {
			if (this.minecraft.player.isScoping()) {
				renderSpyglassOverlay(graphics, this.scopeScale);
			} else {
				this.scopeScale = 0.5F;
				ItemStack itemStack = this.minecraft.player.getInventory().getArmor(3);
				if (itemStack.is(Blocks.CARVED_PUMPKIN.asItem()))
					renderTextureOverlay(graphics, PUMPKIN_BLUR_LOCATION, 1.0F);
			}
		}
		
		if (this.minecraft.player.getTicksFrozen() > 0)
			this.renderTextureOverlay(graphics, POWDER_SNOW_OUTLINE_LOCATION, this.minecraft.player.getPercentFrozen());
		
		float portalTime = Mth.lerp(partialTicks, this.minecraft.player.oSpinningEffectIntensity, this.minecraft.player.spinningEffectIntensity);
		if (portalTime > 0.0F && !this.minecraft.player.hasEffect(MobEffects.CONFUSION))
			this.renderPortalOverlay(graphics, portalTime);
		
		if (this.minecraft.gameMode.getPlayerMode() == GameType.SPECTATOR)
			this.spectatorGui.renderHotbar(graphics);
		
		if (!this.minecraft.options.hideGui)
			for (Pair<ResourceLocation, Component> component : ComponentRegistry.INSTANCE.getComponents()) {
				if (EventBus.LIBHUD_BUS.post(new RenderComponentEvent(graphics, partialTicks, window, component.getLeft(), component.getRight(), EventPhase.PRE))) continue;
				component.getRight().render(this, graphics, partialTicks, this.screenWidth, this.screenHeight);
				EventBus.LIBHUD_BUS.post(new RenderComponentEvent(graphics, partialTicks, window, component.getLeft(), component.getRight(), EventPhase.POST));
			}
		
		if (this.minecraft.player.getSleepTimer() > 0) {
			this.minecraft.getProfiler().push("sleep");
			RenderSystem.disableDepthTest();
			float sleepTimePercent = this.minecraft.player.getSleepTimer();
			float sleepTime = sleepTimePercent / 100.0F;
			if (sleepTime > 1.0F)
				sleepTime = 1.0F - (sleepTimePercent - 100.0F) / 10.0F;
			
			int overlayColor = (int) (220.0F * sleepTime) << 24 | 0x101020;
			graphics.fill(0, 0, this.screenWidth, this.screenHeight, overlayColor);
			this.minecraft.getProfiler().pop();
		}
		
		if (this.minecraft.isDemo())
			this.renderDemoOverlay(graphics);
		
		this.renderEffects(graphics);
		if (this.minecraft.options.renderDebug)
			this.debugScreen.render(graphics);
		
		if (!this.minecraft.options.hideGui) {
			if (this.overlayMessageString != null && this.overlayMessageTime > 0) {
				this.minecraft.getProfiler().push("overlayMessage");
				float offsetTime = this.overlayMessageTime - partialTicks;
				int clampedTime = (int) (offsetTime * 255.0F / 20.0F);
				if (clampedTime > 255)
					clampedTime = 255;
				
				if (clampedTime > 8) {
					graphics.pose().pushPose();
					graphics.pose().translate(this.screenWidth / 2, this.screenHeight - 68, 0.0F);
					int color = 0xffffff;
					if (this.animateOverlayMessageColor)
						color = Mth.hsvToRgb(offsetTime / 50.0F, 0.7F, 0.6F) & 0xffffff;
					
					int alpha = clampedTime << 24 & 0xff000000;
					int width = font.width(this.overlayMessageString);
					this.drawBackdrop(graphics, font, -4, width, 0xffffff | alpha);
					graphics.drawString(font, this.overlayMessageString, -width / 2, -4, color | alpha);
					graphics.pose().popPose();
				}
				
				this.minecraft.getProfiler().pop();
			}
			
			if (this.title != null && this.titleTime > 0) {
				this.minecraft.getProfiler().push("titleAndSubtitle");
				float offsetTime = this.titleTime - partialTicks;
				int clampedTime = 255;
				if (this.titleTime > this.titleFadeOutTime + this.titleStayTime)
					clampedTime = (int) ((this.titleFadeInTime + this.titleStayTime + this.titleFadeOutTime - offsetTime) * 255.0F / this.titleFadeInTime);
				if (this.titleTime <= this.titleFadeOutTime)
					clampedTime = (int) (offsetTime * 255.0F / this.titleFadeOutTime);
				clampedTime = Mth.clamp(clampedTime, 0, 255);
				
				if (clampedTime > 8) {
					graphics.pose().pushPose();
					graphics.pose().translate(this.screenWidth / 2, this.screenHeight / 2, 0.0F);
					RenderSystem.enableBlend();
					graphics.pose().pushPose();
					graphics.pose().scale(4.0F, 4.0F, 4.0F);
					int alpha = clampedTime << 24 & 0xff000000;
					int titleWidth = font.width(this.title);
					this.drawBackdrop(graphics, font, -10, titleWidth, 0xffffff | alpha);
					graphics.drawString(font, this.title, -titleWidth / 2, -10, 0xffffff | alpha);
					graphics.pose().popPose();
					if (this.subtitle != null) {
						graphics.pose().pushPose();
						graphics.pose().scale(2.0F, 2.0F, 2.0F);
						int subtitleWidth = font.width(this.subtitle);
						this.drawBackdrop(graphics, font, 5, subtitleWidth, 0xffffff | alpha);
						graphics.drawString(font, this.subtitle, -subtitleWidth / 2, 5, 0xffffff | alpha);
						graphics.pose().popPose();
					}
					RenderSystem.disableBlend();
					graphics.pose().popPose();
				}
				this.minecraft.getProfiler().pop();
			}
			
			this.subtitleOverlay.render(graphics);
			Scoreboard scoreboard = this.minecraft.level.getScoreboard();
			Objective objective = null;
			PlayerTeam playerTeam = scoreboard.getPlayersTeam(this.minecraft.player.getScoreboardName());
			if (playerTeam != null) {
				int id = playerTeam.getColor().getId();
				if (id >= 0)
					objective = scoreboard.getDisplayObjective(3 + id);
			}
			
			Objective objective2 = objective != null ? objective : scoreboard.getDisplayObjective(1);
			if (objective2 != null)
				this.displayScoreboardSidebar(graphics, objective2);
			
			RenderSystem.enableBlend();
			int scaledX = Mth.floor(this.minecraft.mouseHandler.xpos() * window.getGuiScaledWidth() / window.getScreenWidth());
			int scaledY = Mth.floor(this.minecraft.mouseHandler.ypos() * window.getGuiScaledHeight() / window.getScreenHeight());
			this.minecraft.getProfiler().push("chat");
			this.chat.render(graphics, this.tickCount, scaledX, scaledY);
			this.minecraft.getProfiler().pop();
			objective2 = scoreboard.getDisplayObjective(0);
			if (!this.minecraft.options.keyPlayerList.isDown() || this.minecraft.isLocalServer() && this.minecraft.player.connection.getListedOnlinePlayers().size() <= 1 && objective2 == null)
				this.tabList.setVisible(false);
			else {
				this.tabList.setVisible(true);
				this.tabList.render(graphics, this.screenWidth, scoreboard, objective2);
			}
			
			this.renderSavingIndicator(graphics);
		}
		
		EventBus.LIBHUD_BUS.post(new RenderEvent(graphics, partialTicks, window, EventPhase.POST));
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
	}
	
	public void setup() {
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
	}
	
	@Override
	protected void renderPlayerHealth(GuiGraphics graphics) { } // replaced by components
	
	// VANILLA COMPONENTS START // replacements for #renderPlayerHealth
	
	protected void renderArmor(GuiGraphics graphics) {
		Player player = getCameraPlayer();
		if (player == null) return;
		if (!this.minecraft.gameMode.canHurtPlayer()) return;
		
		this.minecraft.getProfiler().push("armor");
		setup();
		int baseY = this.screenHeight - this.leftOffset;
		int baseX = this.screenWidth / 2 - 91;
		int armor = player.getArmorValue();

		if (armor > 0) {
			for (int i = 0; i < 10; i++) {
				if (i * 2 + 1 < armor)
					graphics.blit(GUI_ICONS_LOCATION, baseX, baseY, 34, 9, 9, 9);
				if (i * 2 + 1 == armor)
					graphics.blit(GUI_ICONS_LOCATION, baseX, baseY, 25, 9, 9, 9);
				if (i * 2 + 1 > armor)
					graphics.blit(GUI_ICONS_LOCATION, baseX, baseY, 16, 9, 9, 9);
				baseX += 8;
			}
			this.leftOffset += 10;
		}
		this.minecraft.getProfiler().pop();
	}
	
	protected void renderFood(GuiGraphics graphics) {
		Player player = getCameraPlayer();
		if (player == null) return;
		if (!this.minecraft.gameMode.canHurtPlayer()) return;
		if (getVehicleMaxHearts(getPlayerVehicleWithHealth()) != 0) return;
		
		this.minecraft.getProfiler().push("food");
		setup();
		int baseX = this.screenWidth / 2 + 91 - 9;
		int baseY = this.screenHeight - this.rightOffset;
		int food = player.getFoodData().getFoodLevel();
		
		for (int i = 0; i < 10; i++) {
			int x = baseX - i * 8;
			int y = baseY;
			
			if (player.getFoodData().getSaturationLevel() <= 0.0F && this.tickCount % (food * 3 + 1) == 0)
				y = this.screenHeight - this.rightOffset + this.random.nextInt(3) - 1;
			
			int fgOffset = 0;
			int bgOffset = 0;
			if (player.hasEffect(MobEffects.HUNGER)) {
				fgOffset = 36;
				bgOffset = 13;
			}
			
			graphics.blit(GUI_ICONS_LOCATION, x, y, 16 + bgOffset * 9, 27, 9, 9);
			if (i * 2 + 1 < food)
				graphics.blit(GUI_ICONS_LOCATION, x, y, fgOffset + 52, 27, 9, 9);
			if (i * 2 + 1 == food)
				graphics.blit(GUI_ICONS_LOCATION, x, y, fgOffset + 61, 27, 9, 9);
		}
		
		this.rightOffset += 10;
		this.minecraft.getProfiler().pop();
	}
	
	protected void renderAir(GuiGraphics graphics) {
		Player player = getCameraPlayer();
		if (player == null) return;
		if (!this.minecraft.gameMode.canHurtPlayer()) return;
		
		this.minecraft.getProfiler().push("air");
		setup();
		int baseY = this.screenHeight - this.rightOffset;
		int baseX = this.screenWidth / 2 + 91 - 9;
		int maxAir = player.getMaxAirSupply();
		int air = Math.min(player.getAirSupply(), maxAir);
		
		if (player.isEyeInFluid(FluidTags.WATER) || air < maxAir) {
			int bubbles = Mth.ceil((air - 2) * 10.0 / maxAir);
			int poppedBubbles = Mth.ceil(air * 10.0 / maxAir) - bubbles;
			
			for (int i = 0; i < bubbles + poppedBubbles; ++i)
				if (i < bubbles)
					graphics.blit(GUI_ICONS_LOCATION, baseX - i * 8, baseY, 16, 18, 9, 9);
				else graphics.blit(GUI_ICONS_LOCATION, baseX - i * 8, baseY, 25, 18, 9, 9);
			this.rightOffset += 10;
		}
		this.minecraft.getProfiler().pop();
	}
	
	protected void renderHearts(GuiGraphics graphics) {
		Player player = this.getCameraPlayer();
		if (player == null) return;
		if (!this.minecraft.gameMode.canHurtPlayer()) return;
		
		this.minecraft.getProfiler().push("health");
		setup();
		int health = Mth.ceil(player.getHealth());
		boolean blink = this.healthBlinkTime > this.tickCount && (this.healthBlinkTime - this.tickCount) / 3L % 2L == 1L;
		long millis = Util.getMillis();
		
		if (health < this.lastHealth && player.invulnerableTime > 0) {
			this.lastHealthTime = millis;
			this.healthBlinkTime = this.tickCount + 20;
		} else if (health > this.lastHealth && player.invulnerableTime > 0) {
			this.lastHealthTime = millis;
			this.healthBlinkTime = this.tickCount + 10;
		}

		if (millis - this.lastHealthTime > 1000L) {
			this.displayHealth = health;
			this.lastHealthTime = millis;
		}

		this.lastHealth = health;
		int displayHealth = this.displayHealth;
		this.random.setSeed(this.tickCount * 312871);
		
		int baseX = this.screenWidth / 2 - 91;
		int baseY = this.screenHeight - this.leftOffset;
		float maxHealth = Math.max((float)player.getAttributeValue(Attributes.MAX_HEALTH), Math.max(displayHealth, health));
		int absorb = Mth.ceil(player.getAbsorptionAmount());
		int rowCount = Mth.ceil((maxHealth + absorb) / 2.0F / 10.0F);
		int rowHeight = Math.max(10 - (rowCount - 2), 3);
		int yOffset = -1;
		
		if (player.hasEffect(MobEffects.REGENERATION))
			yOffset = this.tickCount % Mth.ceil(maxHealth + 5.0F);
		
		renderHearts(graphics, player, baseX, baseY, rowHeight, yOffset, maxHealth, health, displayHealth, absorb, blink);
		
		this.leftOffset += (rowCount - 1) * rowHeight + 10;
		this.minecraft.getProfiler().pop();
	}
	
	// edited to reset texture location / render state + fix profiler with component structure
	@Override
	protected void renderVehicleHealth(GuiGraphics graphics) {
		setup();
		LivingEntity vehicle = this.getPlayerVehicleWithHealth();
		if (vehicle == null) return;
		int maxHealth = this.getVehicleMaxHearts(vehicle);
		if (maxHealth == 0) return;
		
		this.minecraft.getProfiler().push("mountHealth");
		int health = (int) Math.ceil(vehicle.getHealth());
		int baseY = this.screenHeight - this.rightOffset;
		int baseX = this.screenWidth / 2 + 91;
		int y = baseY;
		
		for (int i = 0; maxHealth > 0; i += 20) {
			int maxRowHealth = Math.min(maxHealth, 10);
			maxHealth -= maxRowHealth;
			
			for (int j = 0; j < maxRowHealth; j++) {
				int x = baseX - j * 8 - 9;
				graphics.blit(GUI_ICONS_LOCATION, x, y, 52, 9, 9, 9);
				if (j * 2 + 1 + i < health)
					graphics.blit(GUI_ICONS_LOCATION, x, y, 88, 9, 9, 9);
				if (j * 2 + 1 + i == health)
					graphics.blit(GUI_ICONS_LOCATION, x, y, 97, 9, 9, 9);
			}
			
			y -= 10;
			this.rightOffset += 10;
		}
		this.minecraft.getProfiler().pop();
	}
	
	protected void renderJumpMeter(GuiGraphics graphics) {
		setup();
		RenderSystem.disableBlend();
		PlayerRideableJumping playerRideableJumping = this.minecraft.player.jumpableVehicle();
		if (playerRideableJumping == null) return;
		super.renderJumpMeter(playerRideableJumping, graphics, this.screenWidth / 2 - 91);
	}
	
	protected void renderExperienceBar(GuiGraphics graphics) {
		setup();
		RenderSystem.disableBlend();
		PlayerRideableJumping playerRideableJumping = this.minecraft.player.jumpableVehicle();
		if (playerRideableJumping != null) return;
		if (!this.minecraft.gameMode.hasExperience()) return;
		super.renderExperienceBar(graphics, this.screenWidth / 2 - 91);
	}
	
	protected void renderBossHealth(GuiGraphics graphics) {
		this.minecraft.getProfiler().push("bossHealth");
		setup();
		this.bossOverlay.render(graphics);
		this.minecraft.getProfiler().pop();
	}
	
	@Override
	public void renderSelectedItemName(GuiGraphics graphics) {
		setup();
		RenderSystem.disableBlend();
		if (this.minecraft.options.advancedItemTooltips && this.minecraft.gameMode.getPlayerMode() != GameType.SPECTATOR)
			super.renderSelectedItemName(graphics);
		else if (this.minecraft.player.isSpectator())
			this.spectatorGui.renderTooltip(graphics);
	}
	
	// VANILLA COMPONENTS END //
	
	@Override
	protected void renderCrosshair(GuiGraphics graphics) {
		setup();
		super.renderCrosshair(graphics);
	}
	
	@Override
	protected void renderHotbar(float partialTicks, GuiGraphics graphics) {
//		setup(); // not needed here
		if (this.minecraft.gameMode.getPlayerMode() != GameType.SPECTATOR)
			super.renderHotbar(partialTicks, graphics);
	}
	
	// publicise getters
	@Override public Player getCameraPlayer() { return super.getCameraPlayer(); }
	@Override public LivingEntity getPlayerVehicleWithHealth() { return super.getPlayerVehicleWithHealth(); }
	@Override public int getVehicleMaxHearts(LivingEntity livingEntity) { return super.getVehicleMaxHearts(livingEntity); }
}
