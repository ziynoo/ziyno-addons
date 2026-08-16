package com.ziyno.ziynoaddons.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.ziyno.ziynoaddons.ZiynoConfig;
import net.minecraft.client.renderer.ItemInHandRenderer;
import org.joml.Quaternionfc;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ItemInHandRenderer.class)
public abstract class NoHandSwayMixin {
	private static final String HAND_RENDER_METHOD =
			"renderHandsWithItems(FLcom/mojang/blaze3d/vertex/PoseStack;"
					+ "Lnet/minecraft/client/renderer/SubmitNodeCollector;"
					+ "Lnet/minecraft/client/player/LocalPlayer;I)V";
	private static final String ROTATE_POSE_TARGET =
			"Lcom/mojang/blaze3d/vertex/PoseStack;mulPose(Lorg/joml/Quaternionfc;)V";

	@Redirect(
			method = HAND_RENDER_METHOD,
			at = @At(value = "INVOKE", target = ROTATE_POSE_TARGET, ordinal = 0)
	)
	private void ziyno$removePitchHandSway(PoseStack poseStack, Quaternionfc rotation) {
		if (!ZiynoConfig.noHandSwayEnabled()) poseStack.mulPose(rotation);
	}

	@Redirect(
			method = HAND_RENDER_METHOD,
			at = @At(value = "INVOKE", target = ROTATE_POSE_TARGET, ordinal = 1)
	)
	private void ziyno$removeYawHandSway(PoseStack poseStack, Quaternionfc rotation) {
		if (!ZiynoConfig.noHandSwayEnabled()) poseStack.mulPose(rotation);
	}
}
