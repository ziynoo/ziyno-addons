package com.ziyno.ziynoaddons;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommands.literal;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommands.argument;

public final class ZiynoAddons implements ClientModInitializer {
	public static final String MOD_ID = "ziyno-addons";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	private static final String TRIGGER_MESSAGE_1 = "ENERGY HEED MY CALL!";
	private static final String TRIGGER_MESSAGE_2 = "THUNDER LET ME BE YOUR CATALYST!";
	private static final String TEST_TRIGGER_MESSAGE = "yasdadsadsoo";
	private static final String BLOOD_DOOR_MESSAGE = "The BLOOD DOOR has been opened!";
	private static final String WATCHER_DONE_MESSAGE =
			"[BOSS] The Watcher: You have proven yourself. You may pass.";

	private static final boolean DEBUG_FORCE_HUD = false;
	private static final boolean DEBUG_LOG_CHAT = false;
	private static final double LB_TIMER_BASE_SECONDS = 27.4;
	private static final int RED_THRESHOLD_TICKS = 40;
	private static final int RELEASE_NOW_DURATION_TICKS = 2;
	private static final float TEXT_SCALE = 1.7f;
	private static final int Y_OFFSET_ABOVE_CROSSHAIR = 30;

	private static volatile int remainingServerTicks;
	private static volatile int releaseNowTicks;
	private static final List<Double> campTimes = new ArrayList<>();
	private static boolean bloodCampRunning;
	private static int bloodCampTicks;

	@Override
	public void onInitializeClient() {
		LOGGER.info("Ziyno Addons client initialized for Minecraft 26.1.2");
		ZiynoConfig.load();
		registerCampCommands();
		registerFeatureCommands();
		PreventPlacingPlayerHeads.register();
		TerminalHighlightFeature.register();

		if (DEBUG_FORCE_HUD && ZiynoConfig.lbTimerEnabled()) {
			remainingServerTicks = ZiynoConfig.lbTimerTicks();
		}
		registerChatListener();
		registerHudTimer();
	}

	private static void registerChatListener() {
		ClientReceiveMessageEvents.GAME.register((message, overlay) -> checkChatMessage(message));
		ClientReceiveMessageEvents.CHAT.register(
				(message, signed, sender, params, timestamp) -> checkChatMessage(message)
		);
	}

	private static void checkChatMessage(Component message) {
		String raw = message.getString();
		checkBloodCampTimer(raw);
		if (!ZiynoConfig.lbTimerEnabled()) return;

		if (DEBUG_LOG_CHAT) LOGGER.info("CHAT SEEN: {}", raw);
		if (raw.contains(TRIGGER_MESSAGE_1)
				|| raw.contains(TRIGGER_MESSAGE_2)
				|| raw.contains(TEST_TRIGGER_MESSAGE)) {
			LOGGER.info("Timer started");
			remainingServerTicks = ZiynoConfig.lbTimerTicks();
			releaseNowTicks = 0;
		}
	}

	private static void checkBloodCampTimer(String raw) {
		if (raw.contains(BLOOD_DOOR_MESSAGE)) {
			bloodCampRunning = true;
			bloodCampTicks = 0;
			return;
		}

		if (raw.contains(WATCHER_DONE_MESSAGE) && bloodCampRunning) {
			bloodCampRunning = false;
			double seconds = Math.max(0, bloodCampTicks / 20.0 - 0.15);
			campTimes.add(seconds);
			LOGGER.info("Blood camp completed in {}s", String.format("%.2f", seconds));
		}
	}

	public static void onOdinStyleServerTick() {
		if (ZiynoConfig.lbTimerEnabled()) {
			if (remainingServerTicks > 0) {
				if (--remainingServerTicks == 0) releaseNowTicks = RELEASE_NOW_DURATION_TICKS;
			} else if (releaseNowTicks > 0) {
				releaseNowTicks--;
			}
		}

		if (bloodCampRunning) bloodCampTicks++;
	}

	private static void registerCampCommands() {
		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
			dispatcher.register(literal("camps").executes(context -> {
				showCampStats();
				return 1;
			}));
			dispatcher.register(literal("allcamps").executes(context -> {
				showAllCamps();
				return 1;
			}));
		});
	}

	private static void registerFeatureCommands() {
		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
			dispatcher.register(literal("nohandsway")
					.then(literal("toggle").executes(context -> {
						boolean enabled = !ZiynoConfig.noHandSwayEnabled();
						ZiynoConfig.setNoHandSway(enabled);
						message("No hand sway: " + (enabled ? "ON" : "OFF"));
						return 1;
					})));

			dispatcher.register(literal("lbtimer")
					.then(argument("seconds", DoubleArgumentType.doubleArg(0.0)).executes(context -> {
						double seconds = DoubleArgumentType.getDouble(context, "seconds");
						if (seconds == 0.0) {
							ZiynoConfig.setLbTimerTicks(0);
							remainingServerTicks = 0;
							releaseNowTicks = 0;
							message("LB timer: OFF");
							return 1;
						}

						if (seconds <= LB_TIMER_BASE_SECONDS) {
							message("LB timer time must be above 27.4, or 0 to disable.");
							return 0;
						}

						int ticks = Math.max(1,
								(int) Math.round((seconds - LB_TIMER_BASE_SECONDS) * 20.0));
						ZiynoConfig.setLbTimerTicks(ticks);
						remainingServerTicks = 0;
						releaseNowTicks = 0;
						message(String.format("LB timer set to %.2fs (%d ticks).", seconds, ticks));
						return 1;
					})));
		});
	}

	private static void showCampStats() {
		Minecraft client = Minecraft.getInstance();
		if (client.player == null) return;

		if (campTimes.isEmpty()) {
			message("§cNo camp times recorded this session.");
			return;
		}

		double average = campTimes.stream().mapToDouble(Double::doubleValue).average().orElse(0);
		message("§6Blood Camp Stats");
		message("§7Runs: §f" + campTimes.size());
		message("§7Average: §f" + String.format("%.1fs", average));
		message("§7Median:  §f" + String.format("%.1fs", getCampMedian()));
	}

	private static void showAllCamps() {
		if (Minecraft.getInstance().player == null) return;
		if (campTimes.isEmpty()) {
			message("§cNo camp times recorded this session.");
			return;
		}

		String list = campTimes.stream()
				.map(time -> String.format("%.1fs", time))
				.collect(Collectors.joining(", "));
		message("§6All Blood Camp Times");
		message("§7Runs (" + campTimes.size() + "): §f" + list);
	}

	private static void message(String text) {
		Minecraft client = Minecraft.getInstance();
		if (client.player != null) client.player.sendSystemMessage(Component.literal(text));
	}

	private static double getCampMedian() {
		List<Double> sorted = new ArrayList<>(campTimes);
		Collections.sort(sorted);
		int size = sorted.size();
		return (size & 1) == 1
				? sorted.get(size / 2)
				: (sorted.get(size / 2 - 1) + sorted.get(size / 2)) / 2.0;
	}

	private static void registerHudTimer() {
		HudElementRegistry.addLast(
				Identifier.fromNamespaceAndPath(MOD_ID, "countdown_timer"),
				(graphics, tickCounter) -> {
					if (!ZiynoConfig.lbTimerEnabled()
							|| (remainingServerTicks <= 0 && releaseNowTicks <= 0)) return;

					Minecraft client = Minecraft.getInstance();
					if (client.screen != null || client.options.hideGui) return;

					String text;
					int color;
					if (remainingServerTicks > 0) {
						text = String.format("%.2f", remainingServerTicks / 20.0);
						color = remainingServerTicks <= RED_THRESHOLD_TICKS ? 0xFFFF5555 : 0xFFFFFF55;
					} else {
						text = "RELEASE NOW";
						color = 0xFFFF55FF;
					}

					float scaledWidth = client.font.width(text) * TEXT_SCALE;
					float x = graphics.guiWidth() / 2f - scaledWidth / 2f;
					float y = graphics.guiHeight() / 2f - Y_OFFSET_ABOVE_CROSSHAIR;

					graphics.pose().pushMatrix();
					graphics.pose().translate(x, y);
					graphics.pose().scale(TEXT_SCALE, TEXT_SCALE);
					graphics.text(client.font, text, 0, 0, color);
					graphics.pose().popMatrix();
				}
		);
	}
}
