package com.ziyno.ziynoaddons;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ShapeRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.LeverBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommands.literal;

public final class LeverHitboxFeature {
	private static final int SCAN_RADIUS = 24;
	private static final int SCAN_INTERVAL_TICKS = 10;
	private static final int WHITE = 0xFFFFFFFF;
	private static final float LINE_WIDTH = 2.5f;

	private static boolean enabled;
	private static int ticksUntilScan;
	private static BlockPos lastScanCenter;
	private static List<BlockPos> nearbyLevers = List.of();

	private LeverHitboxFeature() {
	}

	public static void register() {
		enabled = ZiynoConfig.leverHitboxEnabled();
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			if (!enabled || client.level == null || client.player == null) {
				nearbyLevers = List.of();
				lastScanCenter = null;
				return;
			}

			BlockPos center = client.player.blockPosition();
			boolean movedFarEnough = lastScanCenter == null
					|| center.distManhattan(lastScanCenter) > 2;
			if (ticksUntilScan-- <= 0 || movedFarEnough) {
				ticksUntilScan = SCAN_INTERVAL_TICKS - 1;
				lastScanCenter = center.immutable();
				scanNearbyLevers(client, center);
			}
		});

		LevelRenderEvents.AFTER_SOLID_FEATURES.register(context -> {
			Minecraft client = Minecraft.getInstance();
			if (!enabled || client.level == null || nearbyLevers.isEmpty()) return;

			Vec3 camera = context.levelState().cameraRenderState.pos;
			if (camera == null) return;

			VertexConsumer lines = context.bufferSource().getBuffer(RenderTypes.lines());
			for (BlockPos pos : nearbyLevers) {
				BlockState state = client.level.getBlockState(pos);
				if (!(state.getBlock() instanceof LeverBlock)) continue;

				ShapeRenderer.renderShape(
						context.poseStack(),
						lines,
						state.getShape(client.level, pos),
						pos.getX() - camera.x,
						pos.getY() - camera.y,
						pos.getZ() - camera.z,
						WHITE,
						LINE_WIDTH
				);
			}
		});

		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) ->
				dispatcher.register(literal("leverhitbox")
						.then(literal("toggle").executes(context -> {
							setEnabled(!enabled);
							return 1;
						})))
		);
	}

	private static void scanNearbyLevers(Minecraft client, BlockPos center) {
		List<BlockPos> found = new ArrayList<>();
		BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();

		for (int x = -SCAN_RADIUS; x <= SCAN_RADIUS; x++) {
			for (int y = -SCAN_RADIUS; y <= SCAN_RADIUS; y++) {
				for (int z = -SCAN_RADIUS; z <= SCAN_RADIUS; z++) {
					cursor.set(center.getX() + x, center.getY() + y, center.getZ() + z);
					if (client.level.getBlockState(cursor).getBlock() instanceof LeverBlock) {
						found.add(cursor.immutable());
					}
				}
			}
		}

		nearbyLevers = List.copyOf(found);
	}

	private static void setEnabled(boolean value) {
		enabled = value;
		ZiynoConfig.setLeverHitbox(value);
		ticksUntilScan = 0;
		lastScanCenter = null;
		if (!enabled) nearbyLevers = List.of();
		showStatus();
	}

	private static void showStatus() {
		Minecraft client = Minecraft.getInstance();
		if (client.player != null) {
			client.player.sendSystemMessage(Component.literal(
					"Lever hitboxes: " + (enabled ? "ON" : "OFF")
			));
		}
	}
}
