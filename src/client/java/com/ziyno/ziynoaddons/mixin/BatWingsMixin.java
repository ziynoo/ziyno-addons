package com.ziyno.ziynoaddons.mixin;

import net.minecraft.client.model.ambient.BatModel;
import net.minecraft.client.model.geom.ModelPart;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BatModel.class)
public abstract class BatWingsMixin {
	@Inject(method = "<init>", at = @At("TAIL"))
	private void ziyno$hideBatWings(ModelPart root, CallbackInfo ci) {
		ModelPart body = root.getChild("body");
		body.getChild("right_wing").visible = false;
		body.getChild("left_wing").visible = false;
	}
}
