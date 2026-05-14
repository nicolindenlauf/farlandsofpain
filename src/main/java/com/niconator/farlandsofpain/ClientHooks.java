package com.niconator.farlandsofpain;

import net.neoforged.fml.loading.FMLEnvironment;

public final class ClientHooks {
    private ClientHooks() {
    }

    public static void togglePainOverlay() {
        if (!FMLEnvironment.dist.isClient()) {
            return;
        }

        try {
            Class.forName("com.niconator.farlandsofpain.client.ClientPainOverlay")
                    .getMethod("toggleOverlay")
                    .invoke(null);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Failed to toggle Farlands of Pain client overlay", e);
        }
    }
}
