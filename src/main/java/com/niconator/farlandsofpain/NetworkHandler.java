package com.niconator.farlandsofpain;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

public final class NetworkHandler {
    private static final String NETWORK_VERSION = "1";

    private NetworkHandler() {
    }

    public static void register(RegisterPayloadHandlersEvent event) {
        event.registrar(NETWORK_VERSION)
                .playToClient(ConfigSyncPayload.TYPE, ConfigSyncPayload.STREAM_CODEC, (payload, context) -> Config.applySyncedValues(payload))
                .playToClient(ToggleOverlayPayload.TYPE, ToggleOverlayPayload.STREAM_CODEC, (payload, context) -> ClientHooks.togglePainOverlay());
    }

    public static void toggleOverlay(ServerPlayer player) {
        PacketDistributor.sendToPlayer(player, ToggleOverlayPayload.INSTANCE);
    }

    public static void syncToPlayer(ServerPlayer player) {
        PacketDistributor.sendToPlayer(player, ConfigSyncPayload.current());
    }

    public static void syncToAllPlayers() {
        var server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) {
            return;
        }

        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            syncToPlayer(player);
        }
    }
}
