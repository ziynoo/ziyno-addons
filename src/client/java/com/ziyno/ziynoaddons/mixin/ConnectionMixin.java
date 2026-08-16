package com.ziyno.ziynoaddons.mixin;

import com.ziyno.ziynoaddons.ZiynoAddons;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ClientboundPingPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Connection.class)
public abstract class ConnectionMixin {
	@Inject(
			method = "channelRead0(Lio/netty/channel/ChannelHandlerContext;Lnet/minecraft/network/protocol/Packet;)V",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/network/Connection;genericsFtw(Lnet/minecraft/network/protocol/Packet;Lnet/minecraft/network/PacketListener;)V"
			)
	)
	private void ziyno$onPacket(ChannelHandlerContext context, Packet<?> packet, CallbackInfo ci) {
		if (packet instanceof ClientboundPingPacket pingPacket && pingPacket.getId() != 0) {
			ZiynoAddons.onOdinStyleServerTick();
		}
	}
}
