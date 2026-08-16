package com.ziyno.ziynoaddons.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.tags.ItemTags;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerModel.class)
public abstract class SwordBlockArmMixin {
	@Inject(method = "setupAnim(Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;)V", at = @At("TAIL"))
	private void ziyno$applySwordBlockArm(AvatarRenderState state, CallbackInfo ci) {
		Minecraft client = Minecraft.getInstance();
		if (client.player == null || state.id != client.player.getId()) return;
		if (!client.player.getMainHandItem().is(ItemTags.SWORDS) || !client.options.keyUse.isDown()) return;

		float swing = client.player.getAttackAnim(0.0f);
		if (swing > 0.0f && swing < 0.9f) return;

		PlayerModel model = (PlayerModel) (Object) this;
		model.rightArm.xRot = (float) Math.toRadians(-45.0);
		model.rightArm.yRot = (float) Math.toRadians(-30.0);
		model.rightArm.zRot = 0.0f;
	}
}
