package com.niconator.farlandsofpain.client;

import com.niconator.farlandsofpain.Config;
import com.niconator.farlandsofpain.FarlandsofPain;
import com.niconator.farlandsofpain.utils.Utils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;

import java.util.List;

public final class ClientPainOverlay {
    private static final ResourceLocation OVERLAY_ID = ResourceLocation.fromNamespaceAndPath(FarlandsofPain.MODID, "pain_o_meter_overlay");
    private static boolean enabled = false;
    private static long lastUpdateTick = Long.MIN_VALUE;
    private static List<Row> cachedRows = List.of();

    private ClientPainOverlay() {
    }

    public static void init(IEventBus modEventBus) {
        modEventBus.addListener(ClientPainOverlay::registerGuiLayers);
    }

    public static void registerGuiLayers(RegisterGuiLayersEvent event) {
        event.registerAboveAll(OVERLAY_ID, ClientPainOverlay::render);
    }

    public static void toggleOverlay() {
        enabled = !enabled;
        lastUpdateTick = Long.MIN_VALUE;
        cachedRows = List.of();
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player != null) {
            minecraft.player.displayClientMessage(
                    Component.translatable(enabled
                                    ? "command.farlandsofpain.pain_o_meter.enabled"
                                    : "command.farlandsofpain.pain_o_meter.disabled")
                            .withStyle(enabled ? ChatFormatting.GREEN : ChatFormatting.GRAY),
                    false
            );
        }
    }

    private static void render(GuiGraphics guiGraphics, net.minecraft.client.DeltaTracker deltaTracker) {
        Minecraft minecraft = Minecraft.getInstance();
        if (!enabled || minecraft.options.hideGui || minecraft.player == null) {
            return;
        }

        updateCacheIfNeeded(minecraft.player);

        int x = Config.overlayX;
        int y = Config.overlayY;
        int lineHeight = minecraft.font.lineHeight + 2;
        Component title = Component.literal("Pain-O-Meter");
        int labelWidth = minecraft.font.width(title);
        int valueWidth = 0;
        for (Row row : cachedRows) {
            labelWidth = Math.max(labelWidth, minecraft.font.width(row.label()));
            valueWidth = Math.max(valueWidth, minecraft.font.width(row.value()));
        }

        int gap = 14;
        int panelWidth = Math.max(labelWidth + gap + valueWidth + 8, minecraft.font.width(title) + 8);
        int panelHeight = ((cachedRows.size() + 1) * lineHeight) + 6;
        guiGraphics.fill(x - 4, y - 3, x + panelWidth, y + panelHeight, 0x88000000);
        guiGraphics.drawString(minecraft.font, title, x, y, 0xFFAA00, true);

        for (int i = 0; i < cachedRows.size(); i++) {
            Row row = cachedRows.get(i);
            int rowY = y + ((i + 1) * lineHeight);
            int valueX = x + labelWidth + gap + valueWidth - minecraft.font.width(row.value());
            guiGraphics.drawString(minecraft.font, row.label(), x, rowY, row.color(), true);
            guiGraphics.drawString(minecraft.font, row.value(), valueX, rowY, row.color(), true);
        }
    }

    private static void updateCacheIfNeeded(Player player) {
        long gameTime = player.level().getGameTime();
        if (!cachedRows.isEmpty() && gameTime - lastUpdateTick < Config.overlayUpdateIntervalTicks) {
            return;
        }

        lastUpdateTick = gameTime;
        cachedRows = List.of(
                new Row(Component.literal("Health"), Component.literal("x" + format(Utils.calculateScalingMultiplier(player, Config.healthFormula, 0.0, Config.maxHealthMultiplier))), 0xFF5555),
                new Row(Component.literal("Damage"), Component.literal("x" + format(Utils.calculateScalingMultiplier(player, Config.damageFormula, 0.0, Config.maxDamageMultiplier))), 0xAA0000),
                new Row(Component.literal("Speed"), Component.literal("x" + format(Utils.calculateScalingMultiplier(player, Config.speedFormula, 0.0, Config.maxSpeedMultiplier))), 0x55FF55),
                new Row(Component.literal("Knockback"), Component.literal("x" + format(Utils.calculateScalingMultiplier(player, Config.knockbackFormula, 0.0, Config.maxKnockbackMultiplier))), 0x55FFFF),
                new Row(Component.literal("Loot"), Component.literal("x" + Math.min((int) Utils.calculateScalingMultiplier(player, Config.lootFormula), Config.maxExtraLootRolls + 1)), 0xFFAA00),
                new Row(Component.literal("XP"), Component.literal("x" + format(Utils.calculateScalingMultiplier(player, Config.xpFormula, 0.0, Config.maxXpMultiplier))), 0xFFFF55)
        );
    }

    private static String format(double value) {
        return String.format("%.2f", value);
    }

    private record Row(Component label, Component value, int color) {
    }
}
