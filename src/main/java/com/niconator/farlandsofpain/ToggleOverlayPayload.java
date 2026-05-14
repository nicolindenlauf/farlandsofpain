package com.niconator.farlandsofpain;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record ToggleOverlayPayload() implements CustomPacketPayload {
    public static final ToggleOverlayPayload INSTANCE = new ToggleOverlayPayload();
    public static final Type<ToggleOverlayPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(FarlandsofPain.MODID, "toggle_overlay"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ToggleOverlayPayload> STREAM_CODEC = StreamCodec.unit(INSTANCE);

    @Override
    public Type<ToggleOverlayPayload> type() {
        return TYPE;
    }
}
