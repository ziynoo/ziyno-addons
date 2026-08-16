package com.ziyno.ziynoaddons.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.ItemTags;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MultiPlayerGameMode.class)
public abstract class SwordBlockBreakMixin {
	@Inject(method = "continueDestroyBlock", at = @At("HEAD"), cancellable = true)
	private void ziyno$stopBreakingWhileSwordBlocking(
			BlockPos pos,
			Direction direction,
			CallbackInfoReturnable<Boolean> cir
	) {
		Minecraft client = Minecraft.getInstance();
		if (client.player != null
				&& client.player.getMainHandItem().is(ItemTags.SWORDS)
				&& client.options.keyUse.isDown()) {
			cir.setReturnValue(false);
		}
	}
}
