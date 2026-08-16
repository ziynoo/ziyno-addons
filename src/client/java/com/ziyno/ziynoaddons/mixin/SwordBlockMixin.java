package com.ziyno.ziynoaddons.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemInHandRenderer.class)
public abstract class SwordBlockMixin {
	private float ziyno$swingProgress;

	@Inject(method = "renderArmWithItem", at = @At("HEAD"))
	private void ziyno$captureSwingProgress(
			AbstractClientPlayer player,
			float partialTick,
			float pitch,
			InteractionHand hand,
			float swingProgress,
			ItemStack item,
			float equipProgress,
			PoseStack poseStack,
			SubmitNodeCollector submitNodeCollector,
			int light,
			CallbackInfo ci
	) {
		ziyno$swingProgress = swingProgress;
	}

	@Inject(method = "applyItemArmTransform", at = @At("TAIL"))
	private void ziyno$applySwordBlock(
			PoseStack poseStack,
			HumanoidArm arm,
			float equipProgress,
			CallbackInfo ci
	) {
		Minecraft client = Minecraft.getInstance();
		if (client.player == null || arm != HumanoidArm.RIGHT) return;
		if (!client.player.getMainHandItem().is(ItemTags.SWORDS) || !client.options.keyUse.isDown()) return;
		if (ziyno$swingProgress > 0.0f && ziyno$swingProgress < 0.9f) return;

		poseStack.translate(-0.045f, -0.035f, 0.0f);
		poseStack.mulPose(Axis.YP.rotationDegrees(120.0f));
		poseStack.mulPose(Axis.XP.rotationDegrees(-76.0f));
		poseStack.mulPose(Axis.ZP.rotationDegrees(-45.0f));
		poseStack.scale(1.23f, 1.23f, 1.23f);
	}
}
