package dev.cheos.libhud;

import org.apache.commons.lang3.tuple.Pair;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import dev.cheos.libhud.api.Component;
import dev.cheos.libhud.api.event.*;
import dev.cheos.libhud.api.event.Event.EventPhase;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GameRenderer;
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
import net.minecraft.world.scores.*;

public class LibhudGui extends Gui {
	public int leftOffset = 39, rightOffset = 39;
	
	public LibhudGui(Minecraft minecraft, ItemRenderer itemRenderer) {
		super(minecraft, itemRenderer);
	}
	
	@Override
	public void render(PoseStack poseStack, float partialTicks) {
		Window window = minecraft.getWindow();
		screenWidth = window.getGuiScaledWidth();
		screenHeight = window.getGuiScaledHeight();
		leftOffset = rightOffset = 39;
		Font font = getFont();
		RenderSystem.enableBlend();
		
		if (EventBus.LIBHUD_BUS.post(new RenderEvent(poseStack, partialTicks, window, EventPhase.PRE))) return;
		
		if (Minecraft.useFancyGraphics()) {
			renderVignette(poseStack, minecraft.getCameraEntity());
		} else {
			RenderSystem.enableDepthTest();
			RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
			RenderSystem.defaultBlendFunc();
		}
		
		float delta = this.minecraft.getDeltaFrameTime();
		this.scopeScale = Mth.lerp(0.5F * delta, this.scopeScale, 1.125F);
		if (this.minecraft.options.getCameraType().isFirstPerson()) {
			if (this.minecraft.player.isScoping()) {
				renderSpyglassOverlay(poseStack, scopeScale);
			} else {
				this.scopeScale = 0.5F;
				ItemStack itemStack = this.minecraft.player.getInventory().getArmor(3);
				if (itemStack.is(Blocks.CARVED_PUMPKIN.asItem()))
					renderTextureOverlay(poseStack, PUMPKIN_BLUR_LOCATION, 1.0F);
			}
		}
		
		if (this.minecraft.player.getTicksFrozen() > 0)
			this.renderTextureOverlay(poseStack, POWDER_SNOW_OUTLINE_LOCATION, this.minecraft.player.getPercentFrozen());
		
		float portalTime = Mth.lerp(partialTicks, this.minecraft.player.oPortalTime, this.minecraft.player.portalTime);
		if (portalTime > 0.0F && !this.minecraft.player.hasEffect(MobEffects.CONFUSION))
			this.renderPortalOverlay(poseStack, portalTime);
		
		if (this.minecraft.gameMode.getPlayerMode() == GameType.SPECTATOR)
			this.spectatorGui.renderHotbar(poseStack);
		
		if (!this.minecraft.options.hideGui)
			for (Pair<ResourceLocation, Component> component : ComponentRegistry.INSTANCE.getComponents()) {
				if (EventBus.LIBHUD_BUS.post(new RenderComponentEvent(poseStack, partialTicks, window, component.getLeft(), component.getRight(), EventPhase.PRE))) continue;
				component.getRight().render(this, poseStack, partialTicks, this.screenWidth, this.screenHeight);
				EventBus.LIBHUD_BUS.post(new RenderComponentEvent(poseStack, partialTicks, window, component.getLeft(), component.getRight(), EventPhase.POST));
			}
		
		if (this.minecraft.player.getSleepTimer() > 0) {
			this.minecraft.getProfiler().push("sleep");
			RenderSystem.disableDepthTest();
			float sleepTimePercent = this.minecraft.player.getSleepTimer();
			float sleepTime = sleepTimePercent / 100.0F;
			if (sleepTime > 1.0F)
				sleepTime = 1.0F - (sleepTimePercent - 100.0F) / 10.0F;
			
			int overlayColor = (int) (220.0F * sleepTime) << 24 | 1052704;
			fill(poseStack, 0, 0, this.screenWidth, this.screenHeight, overlayColor);
			RenderSystem.enableDepthTest();
			RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
			this.minecraft.getProfiler().pop();
		}
		
		if (this.minecraft.isDemo())
			this.renderDemoOverlay(poseStack);
		
		this.renderEffects(poseStack);
		if (this.minecraft.options.renderDebug)
			this.debugScreen.render(poseStack);
		
		if (!this.minecraft.options.hideGui) {
			if (this.overlayMessageString != null && this.overlayMessageTime > 0) {
				this.minecraft.getProfiler().push("overlayMessage");
				float offsetTime = this.overlayMessageTime - partialTicks;
				int clampedTime = (int) (offsetTime * 255.0F / 20.0F);
				if (clampedTime > 255)
					clampedTime = 255;
				
				if (clampedTime > 8) {
					poseStack.pushPose();
					poseStack.translate(this.screenWidth / 2, this.screenHeight - 68, 0.0F);
					RenderSystem.enableBlend();
					RenderSystem.defaultBlendFunc();
					int color = 16777215;
					if (this.animateOverlayMessageColor)
						color = Mth.hsvToRgb(offsetTime / 50.0F, 0.7F, 0.6F) & 16777215;
					
					int alpha = clampedTime << 24 & -16777216;
					int width = font.width(this.overlayMessageString);
					this.drawBackdrop(poseStack, font, -4, width, 16777215 | alpha);
					font.drawShadow(poseStack, this.overlayMessageString, -width / 2, -4.0F, color | alpha);
					RenderSystem.disableBlend();
					poseStack.popPose();
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
					poseStack.pushPose();
					poseStack.translate(this.screenWidth / 2, this.screenHeight / 2, 0.0F);
					RenderSystem.enableBlend();
					RenderSystem.defaultBlendFunc();
					poseStack.pushPose();
					poseStack.scale(4.0F, 4.0F, 4.0F);
					int alpha = clampedTime << 24 & -16777216;
					int titleWidth = font.width(this.title);
					this.drawBackdrop(poseStack, font, -10, titleWidth, 16777215 | alpha);
					font.drawShadow(poseStack, this.title, -titleWidth / 2, -10.0F, 16777215 | alpha);
					poseStack.popPose();
					if (this.subtitle != null) {
						poseStack.pushPose();
						poseStack.scale(2.0F, 2.0F, 2.0F);
						int subtitleWidth = font.width(this.subtitle);
						this.drawBackdrop(poseStack, font, 5, subtitleWidth, 16777215 | alpha);
						font.drawShadow(poseStack, this.subtitle, -subtitleWidth / 2, 5.0F, 16777215 | alpha);
						poseStack.popPose();
					}
					RenderSystem.disableBlend();
					poseStack.popPose();
				}
				this.minecraft.getProfiler().pop();
			}
			
			this.subtitleOverlay.render(poseStack);
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
				this.displayScoreboardSidebar(poseStack, objective2);
			
			RenderSystem.enableBlend();
			RenderSystem.defaultBlendFunc();
			int scaledX = Mth.floor(this.minecraft.mouseHandler.xpos() * window.getGuiScaledWidth() / window.getScreenWidth());
			int scaledY = Mth.floor(this.minecraft.mouseHandler.ypos() * window.getGuiScaledHeight() / window.getScreenHeight());
			this.minecraft.getProfiler().push("chat");
			this.chat.render(poseStack, this.tickCount, scaledX, scaledY);
			this.minecraft.getProfiler().pop();
			objective2 = scoreboard.getDisplayObjective(0);
			if (!this.minecraft.options.keyPlayerList.isDown() || this.minecraft.isLocalServer() && this.minecraft.player.connection.getListedOnlinePlayers().size() <= 1 && objective2 == null)
				this.tabList.setVisible(false);
			else {
				this.tabList.setVisible(true);
				this.tabList.render(poseStack, this.screenWidth, scoreboard, objective2);
			}
			
			this.renderSavingIndicator(poseStack);
		}
		
		EventBus.LIBHUD_BUS.post(new RenderEvent(poseStack, partialTicks, window, EventPhase.POST));
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
	}
	
	public void setup() {
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderTexture(0, GUI_ICONS_LOCATION);
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
	}
	
	@Override
	protected void renderPlayerHealth(PoseStack poseStack) { }
	
	protected void renderArmor(PoseStack poseStack) {
		Player player = getCameraPlayer();
		if (player == null) return;
		if (!this.minecraft.gameMode.canHurtPlayer()) return;
		
		this.minecraft.getProfiler().push("armor");
		setup();
		int baseY = this.screenHeight - this.leftOffset;
		int baseX = this.screenWidth / 2 - 91;
		int armor = player.getArmorValue();
		
		for (int i = 0; i < 10; i++)
			if (armor > 0) {
				if (i * 2 + 1 < armor)
					blit(poseStack, baseX, baseY, 34, 9, 9, 9);
				if (i * 2 + 1 == armor)
					blit(poseStack, baseX, baseY, 25, 9, 9, 9);
				if (i * 2 + 1 > armor)
					blit(poseStack, baseX, baseY, 16, 9, 9, 9);
				baseX += 8;
			}
		
		this.leftOffset += 10;
		this.minecraft.getProfiler().pop();
	}
	
	protected void renderFood(PoseStack poseStack) {
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
			
			blit(poseStack, x, y, 16 + bgOffset * 9, 27, 9, 9);
			if (i * 2 + 1 < food)
				blit(poseStack, x, y, fgOffset + 52, 27, 9, 9);
			if (i * 2 + 1 == food)
				blit(poseStack, x, y, fgOffset + 61, 27, 9, 9);
		}
		
		this.rightOffset += 10;
		this.minecraft.getProfiler().pop();
	}
	
	protected void renderAir(PoseStack poseStack) {
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
					blit(poseStack, baseX - i * 8, baseY, 16, 18, 9, 9);
				else blit(poseStack, baseX - i * 8, baseY, 25, 18, 9, 9);
			this.rightOffset += 10;
		}
		this.minecraft.getProfiler().pop();
	}
	
	protected void renderHearts(PoseStack poseStack) {
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
		
		renderHearts(poseStack, player, baseX, baseY, rowHeight, yOffset, maxHealth, health, displayHealth, absorb, blink);
		
		this.leftOffset += rowCount * rowHeight;
		this.minecraft.getProfiler().pop();
	}
	
	@Override
	protected void renderVehicleHealth(PoseStack poseStack) {
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
				blit(poseStack, x, y, 52, 9, 9, 9);
				if (j * 2 + 1 + i < health)
					blit(poseStack, x, y, 88, 9, 9, 9);
				if (j * 2 + 1 + i == health)
					blit(poseStack, x, y, 97, 9, 9, 9);
			}
			
			y -= 10;
			this.rightOffset += 10;
		}
		this.minecraft.getProfiler().pop();
	}
	
	protected void renderJumpMeter(PoseStack poseStack) {
		setup();
		RenderSystem.disableBlend();
		PlayerRideableJumping playerRideableJumping = this.minecraft.player.jumpableVehicle();
		if (playerRideableJumping == null) return;
		super.renderJumpMeter(playerRideableJumping, poseStack, this.screenWidth / 2 - 91);
	}
	
	protected void renderExperienceBar(PoseStack poseStack) {
		setup();
		RenderSystem.disableBlend();
		PlayerRideableJumping playerRideableJumping = this.minecraft.player.jumpableVehicle();
		if (playerRideableJumping != null) return;
		if (!this.minecraft.gameMode.hasExperience()) return;
		super.renderExperienceBar(poseStack, this.screenWidth / 2 - 91);
	}
	
	protected void renderBossHealth(PoseStack poseStack) {
		this.minecraft.getProfiler().push("bossHealth");
		setup();
		this.bossOverlay.render(poseStack);
		this.minecraft.getProfiler().pop();
	}
	
	@Override
	public void renderSelectedItemName(PoseStack poseStack) {
		setup();
		RenderSystem.disableBlend();
		if (this.minecraft.options.advancedItemTooltips && this.minecraft.gameMode.getPlayerMode() != GameType.SPECTATOR)
			super.renderSelectedItemName(poseStack);
		else if (this.minecraft.player.isSpectator())
			this.spectatorGui.renderTooltip(poseStack);
	}
	
	@Override
	protected void renderCrosshair(PoseStack poseStack) {
		setup();
		super.renderCrosshair(poseStack);
	}
	
	@Override
	protected void renderHotbar(float partialTicks, PoseStack poseStack) {
//		setup(); // not needed here
		if (this.minecraft.gameMode.getPlayerMode() != GameType.SPECTATOR)
			super.renderHotbar(partialTicks, poseStack);
	}
}
