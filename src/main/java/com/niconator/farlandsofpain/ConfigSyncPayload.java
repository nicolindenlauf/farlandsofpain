package com.niconator.farlandsofpain;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record ConfigSyncPayload(
        String dimensionOffsetsJson,
        boolean zeroZeroOrigin,
        String healthFormula,
        String damageFormula,
        String lootFormula,
        String xpFormula,
        String speedFormula,
        String knockbackFormula,
        double maxHealthMultiplier,
        double maxDamageMultiplier,
        double maxSpeedMultiplier,
        double maxKnockbackMultiplier,
        double maxXpMultiplier,
        int maxExtraLootRolls
) implements CustomPacketPayload {
    public static final Type<ConfigSyncPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(FarlandsofPain.MODID, "config_sync"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ConfigSyncPayload> STREAM_CODEC = StreamCodec.ofMember(ConfigSyncPayload::write, ConfigSyncPayload::read);

    public static ConfigSyncPayload current() {
        return new ConfigSyncPayload(
                Config.dimensionOffsetsJson,
                Config.zeroZeroOrigin,
                Config.healthFormula,
                Config.damageFormula,
                Config.lootFormula,
                Config.xpFormula,
                Config.speedFormula,
                Config.knockbackFormula,
                Config.maxHealthMultiplier,
                Config.maxDamageMultiplier,
                Config.maxSpeedMultiplier,
                Config.maxKnockbackMultiplier,
                Config.maxXpMultiplier,
                Config.maxExtraLootRolls
        );
    }

    @Override
    public Type<ConfigSyncPayload> type() {
        return TYPE;
    }

    private void write(RegistryFriendlyByteBuf buffer) {
        buffer.writeUtf(dimensionOffsetsJson);
        buffer.writeBoolean(zeroZeroOrigin);
        buffer.writeUtf(healthFormula);
        buffer.writeUtf(damageFormula);
        buffer.writeUtf(lootFormula);
        buffer.writeUtf(xpFormula);
        buffer.writeUtf(speedFormula);
        buffer.writeUtf(knockbackFormula);
        buffer.writeDouble(maxHealthMultiplier);
        buffer.writeDouble(maxDamageMultiplier);
        buffer.writeDouble(maxSpeedMultiplier);
        buffer.writeDouble(maxKnockbackMultiplier);
        buffer.writeDouble(maxXpMultiplier);
        buffer.writeVarInt(maxExtraLootRolls);
    }

    private static ConfigSyncPayload read(RegistryFriendlyByteBuf buffer) {
        return new ConfigSyncPayload(
                buffer.readUtf(),
                buffer.readBoolean(),
                buffer.readUtf(),
                buffer.readUtf(),
                buffer.readUtf(),
                buffer.readUtf(),
                buffer.readUtf(),
                buffer.readUtf(),
                buffer.readDouble(),
                buffer.readDouble(),
                buffer.readDouble(),
                buffer.readDouble(),
                buffer.readDouble(),
                buffer.readVarInt()
        );
    }
}
