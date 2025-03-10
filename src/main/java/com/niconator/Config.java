package com.niconator;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

@EventBusSubscriber(modid = FarlandsofPain.MODID, bus = EventBusSubscriber.Bus.MOD)
public class Config
{
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.ConfigValue<String> HEALTH_FORMULA = BUILDER
            .comment("Formula for health scaling (use x, y, z, dist, nearestPlayerlevel, entityhealth an current_day variables)")
            .define("modifiers.healthFormula", "(dist / 1000) + 1.0");

    static final ModConfigSpec SPEC = BUILDER.build();

    public static String healthFormula;

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event)
    {
        healthFormula = HEALTH_FORMULA.get(); // Load the health formula from config
    }
}
