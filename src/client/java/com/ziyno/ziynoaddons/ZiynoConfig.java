package com.ziyno.ziynoaddons;

import net.minecraft.client.Minecraft;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public final class ZiynoConfig {
	private static final String LEVER_HITBOX = "leverHitbox";
	private static final String NO_HAND_SWAY = "noHandSway";
	private static final String LB_TIMER = "lbTimer";
	private static final String LB_TIMER_TICKS = "lbTimerTicks";
	private static final String TERMINAL_HIGHLIGHT = "terminalHighlight";
	private static final int DEFAULT_LB_TIMER_TICKS = 133;

	private static boolean loaded;
	private static boolean leverHitbox = true;
	private static boolean noHandSway = true;
	private static boolean lbTimer = true;
	private static int lbTimerTicks = DEFAULT_LB_TIMER_TICKS;
	private static boolean terminalHighlight = true;

	private ZiynoConfig() {
	}

	public static synchronized void load() {
		if (loaded) return;
		loaded = true;

		Path file = configPath();
		if (!Files.exists(file)) {
			save();
			return;
		}

		Properties properties = new Properties();
		try (InputStream input = Files.newInputStream(file)) {
			properties.load(input);
			leverHitbox = readBoolean(properties, LEVER_HITBOX, true);
			noHandSway = readBoolean(properties, NO_HAND_SWAY, true);
			lbTimer = readBoolean(properties, LB_TIMER, true);
			lbTimerTicks = readInt(properties, LB_TIMER_TICKS,
					lbTimer ? DEFAULT_LB_TIMER_TICKS : 0);
			lbTimer = lbTimerTicks > 0;
			terminalHighlight = readBoolean(properties, TERMINAL_HIGHLIGHT, true);
		} catch (IOException exception) {
			ZiynoAddons.LOGGER.error("Failed to load Ziyno Addons config", exception);
		}
	}

	public static synchronized boolean leverHitboxEnabled() {
		load();
		return leverHitbox;
	}

	public static synchronized boolean noHandSwayEnabled() {
		load();
		return noHandSway;
	}

	public static synchronized boolean lbTimerEnabled() {
		load();
		return lbTimer;
	}

	public static synchronized int lbTimerTicks() {
		load();
		return lbTimerTicks;
	}

	public static synchronized boolean terminalHighlightEnabled() {
		load();
		return terminalHighlight;
	}

	public static synchronized void setLeverHitbox(boolean enabled) {
		load();
		leverHitbox = enabled;
		save();
	}

	public static synchronized void setNoHandSway(boolean enabled) {
		load();
		noHandSway = enabled;
		save();
	}

	public static synchronized void setLbTimer(boolean enabled) {
		load();
		lbTimer = enabled;
		lbTimerTicks = enabled ? DEFAULT_LB_TIMER_TICKS : 0;
		save();
	}

	public static synchronized void setLbTimerTicks(int ticks) {
		load();
		lbTimerTicks = Math.max(0, ticks);
		lbTimer = lbTimerTicks > 0;
		save();
	}

	public static synchronized void setTerminalHighlight(boolean enabled) {
		load();
		terminalHighlight = enabled;
		save();
	}

	private static void save() {
		Path file = configPath();
		Properties properties = new Properties();
		properties.setProperty(LEVER_HITBOX, Boolean.toString(leverHitbox));
		properties.setProperty(NO_HAND_SWAY, Boolean.toString(noHandSway));
		properties.setProperty(LB_TIMER, Boolean.toString(lbTimer));
		properties.setProperty(LB_TIMER_TICKS, Integer.toString(lbTimerTicks));
		properties.setProperty(TERMINAL_HIGHLIGHT, Boolean.toString(terminalHighlight));

		try {
			Files.createDirectories(file.getParent());
			try (OutputStream output = Files.newOutputStream(file)) {
				properties.store(output, "Ziyno Addons settings");
			}
		} catch (IOException exception) {
			ZiynoAddons.LOGGER.error("Failed to save Ziyno Addons config", exception);
		}
	}

	private static boolean readBoolean(Properties properties, String key, boolean fallback) {
		String value = properties.getProperty(key);
		return value == null ? fallback : Boolean.parseBoolean(value);
	}

	private static int readInt(Properties properties, String key, int fallback) {
		String value = properties.getProperty(key);
		if (value == null) return fallback;
		try {
			return Math.max(0, Integer.parseInt(value));
		} catch (NumberFormatException ignored) {
			return fallback;
		}
	}

	private static Path configPath() {
		return Minecraft.getInstance().gameDirectory.toPath()
				.resolve("config")
				.resolve("ziyno-addons.properties");
	}
}
