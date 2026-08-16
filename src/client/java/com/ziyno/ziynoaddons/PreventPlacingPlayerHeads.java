package com.ziyno.ziynoaddons;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ItemLore;
import org.lwjgl.glfw.GLFW;

import java.util.HashSet;
import java.util.Set;

public final class PreventPlacingPlayerHeads {
	private static final Minecraft CLIENT = Minecraft.getInstance();
	private static final Set<String> ignoredSkyblockIds = new HashSet<>();
	private static KeyMapping blacklistKey;

	private PreventPlacingPlayerHeads() {
	}

	public static void register() {
		blacklistKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
				"key.ziynoaddons.pph_blacklist",
				GLFW.GLFW_KEY_UNKNOWN,
				KeyMapping.Category.MISC
		));

		UseBlockCallback.EVENT.register((player, world, hand, hitResult) ->
				shouldPrevent(player.getItemInHand(hand)) ? InteractionResult.SUCCESS : InteractionResult.PASS
		);

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			while (blacklistKey.consumeClick()) toggleHeldItemIgnored();
		});
	}

	private static boolean shouldPrevent(ItemStack stack) {
		if (stack == null || stack.isEmpty() || stack.getItem() != Items.PLAYER_HEAD) return false;

		String skyblockId = getSkyblockId(stack);
		if (skyblockId != null && ignoredSkyblockIds.contains(skyblockId)) return false;
		return hasRightClickLore(stack);
	}

	private static boolean hasRightClickLore(ItemStack stack) {
		ItemLore lore = stack.get(DataComponents.LORE);
		if (lore == null) return false;

		for (Component line : lore.lines()) {
			String text = line.getString();
			if (text.contains("RIGHT CLICK") || text.contains("Right-click")) return true;
		}
		return false;
	}

	private static String getSkyblockId(ItemStack stack) {
		CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
		if (customData == null) return null;

		CompoundTag extra = customData.copyTag().getCompound("ExtraAttributes").orElse(null);
		return extra == null ? null : extra.getString("id").orElse(null);
	}

	private static void toggleHeldItemIgnored() {
		if (CLIENT.player == null) return;

		ItemStack stack = CLIENT.player.getMainHandItem();
		if (stack.isEmpty()) return;

		String skyblockId = getSkyblockId(stack);
		if (skyblockId == null) {
			CLIENT.player.sendSystemMessage(Component.literal("§cThis item has no SkyBlock ID."));
			return;
		}

		if (ignoredSkyblockIds.remove(skyblockId)) {
			CLIENT.player.sendSystemMessage(Component.literal("§bPPH Ignore §cRemoved §b" + skyblockId));
		} else {
			ignoredSkyblockIds.add(skyblockId);
			CLIENT.player.sendSystemMessage(Component.literal("§bPPH Ignore §aAdded §b" + skyblockId));
		}
	}
}
