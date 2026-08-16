package com.ziyno.ziynoaddons;

import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.gizmos.GizmoStyle;
import net.minecraft.gizmos.Gizmos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.stream.StreamSupport;

public final class TerminalHighlightFeature {
	private static final int SCAN_INTERVAL_TICKS = 5;
	private static final String INACTIVE_TERMINAL_NAME = "Inactive Terminal";
	private static final int IN_RANGE_FILL = 0x9926D138;
	private static final int OUT_OF_RANGE_FILL = 0x99E02924;

	private static boolean enabled;
	private static int ticksUntilScan;
	private static List<ArmorStand> nearbyTerminals = List.of();

	private TerminalHighlightFeature() {
	}

	public static void register() {
		enabled = ZiynoConfig.terminalHighlightEnabled();
		ClientTickEvents.END_CLIENT_TICK.register(TerminalHighlightFeature::onEndTick);
		LevelRenderEvents.BEFORE_GIZMOS.register(context -> {
			Minecraft minecraft = Minecraft.getInstance();
			if (!enabled || minecraft.level == null || minecraft.player == null
					|| nearbyTerminals.isEmpty()) return;

			LocalPlayer player = minecraft.player;
			double reach = player.getAttributeValue(Attributes.ENTITY_INTERACTION_RANGE);
			Vec3 eye = player.getEyePosition();

			for (ArmorStand terminal : nearbyTerminals) {
				if (terminal.isRemoved() || !isInactiveTerminal(terminal)) continue;
				AABB hitbox = terminal.getBoundingBox();
				boolean inRange = distanceSquaredToBox(eye, hitbox) <= reach * reach;
				Gizmos.cuboid(hitbox, GizmoStyle.fill(inRange ? IN_RANGE_FILL : OUT_OF_RANGE_FILL));
			}
		});

		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) ->
				dispatcher.register(ClientCommands.literal("terminalhighlight").executes(context -> {
					setEnabled(!enabled);
					return 1;
				}))
		);
	}

	private static void onEndTick(Minecraft minecraft) {
		if (!enabled || minecraft.level == null || minecraft.player == null) {
			nearbyTerminals = List.of();
			return;
		}

		if (ticksUntilScan-- <= 0) {
			ticksUntilScan = SCAN_INTERVAL_TICKS - 1;
			nearbyTerminals = StreamSupport
					.stream(minecraft.level.entitiesForRendering().spliterator(), false)
					.filter(ArmorStand.class::isInstance)
					.map(ArmorStand.class::cast)
					.filter(TerminalHighlightFeature::isInactiveTerminal)
					.toList();
		}
	}

	private static boolean isInactiveTerminal(ArmorStand armorStand) {
		Component name = armorStand.getCustomName();
		return name != null && INACTIVE_TERMINAL_NAME.equals(name.getString());
	}

	private static double distanceSquaredToBox(Vec3 point, AABB box) {
		double nearestX = Math.max(box.minX, Math.min(point.x, box.maxX));
		double nearestY = Math.max(box.minY, Math.min(point.y, box.maxY));
		double nearestZ = Math.max(box.minZ, Math.min(point.z, box.maxZ));
		double dx = point.x - nearestX;
		double dy = point.y - nearestY;
		double dz = point.z - nearestZ;
		return dx * dx + dy * dy + dz * dz;
	}

	private static void setEnabled(boolean value) {
		enabled = value;
		ZiynoConfig.setTerminalHighlight(value);
		ticksUntilScan = 0;
		if (!enabled) nearbyTerminals = List.of();

		Minecraft minecraft = Minecraft.getInstance();
		if (minecraft.player != null) {
			minecraft.player.sendSystemMessage(Component.literal(
					"Terminal highlighting: " + (enabled ? "ON" : "OFF")
			));
		}
	}
}
