package com.ziyno.ziynoaddons.mixin;

import net.minecraft.client.Camera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(Camera.class)
public abstract class CameraMixin {
	@ModifyConstant(method = "tick", constant = @Constant(floatValue = 0.5f))
	private float ziyno$useFasterEyeHeightSmoothing(float vanillaSmoothing) {
		return 0.9f;
	}
}
