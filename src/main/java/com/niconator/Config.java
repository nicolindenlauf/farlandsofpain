package com.niconator;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

@EventBusSubscriber(modid = FarlandsofPain.MODID, bus = EventBusSubscriber.Bus.MOD)
public class Config
{
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder().comment("The scaling formulas for mob stats.\nAvailable variables: use x_coord, y_coord, z_coord, dist_from_spawn, nearest_player_level, entity_health and current_day variables.\nAvailable operators: +, -, *, /, ^, %.\nAvailable functions: min, max, clamp, round,  abs, acos, asin, atan, cbrt, ceil, cos, cosh, exp, floor, log, log10, log2, sin, sinh, sqrt, tan, tanh, signum.");

    public static final ModConfigSpec.ConfigValue<String> HEALTH_FORMULA = BUILDER
            .comment("The scaling formula for health. The default is: max((dist_from_spawn / 750) + (abs(y_coord - 70) / 100) + (nearest_player_level / 50) - 0.75, 0.75)")
            .define("health.healthFormula", "max((dist_from_spawn / 750) + (abs(y_coord - 70) / 100) + (nearest_player_level / 50) - 0.75, 0.75)"); 

    public static final ModConfigSpec.ConfigValue<String> DAMAGE_FORMULA = BUILDER
            .comment("The scaling formula for damage received. The default is: max((dist_from_spawn / 750) + (abs(y_coord - 70) / 100) + (nearest_player_level / 50) - 0.75, 0.75)")
            .define("damage.damageFormula", "max((dist_from_spawn / 750) + (abs(y_coord - 70) / 100) + (nearest_player_level / 50) - 0.75, 0.75)");

    public static final ModConfigSpec.ConfigValue<String> LOOT_FORMULA = BUILDER
            .comment("The scaling formula for loot. The default is: clamp((dist_from_spawn / 750) + (abs(y_coord - 70) / 100) + (nearest_player_level / 50) - 1.0, 1.0, 10.0)")
            .define("loot.lootFormula", "clamp((dist_from_spawn / 750) + (abs(y_coord - 70) / 100) + (nearest_player_level / 50) - 1.0, 1.0, 10.0)");

    public static final ModConfigSpec.ConfigValue<String> SPEED_FORMULA = BUILDER
            .comment("The scaling formula for speed. The default is: clamp((dist_from_spawn / 1500) + (abs(y_coord - 70) / 100) + (nearest_player_level / 50) - 1.0, 1.0, 2.0)")
            .define("speed.speedFormula", "clamp((dist_from_spawn / 1500) + (abs(y_coord - 70) / 100) + (nearest_player_level / 50) - 1.0, 1.0, 2.0)");

    public static final ModConfigSpec.ConfigValue<String> KNOCKBACK_FORMULA = BUILDER
            .comment("The scaling formula for knockback resistance. The default is: clamp((dist_from_spawn / 1500) + (abs(y_coord - 70) / 100) + (nearest_player_level / 50) - 0.9, 0.9, 2.0)")
            .define("knockback.knockbackFormula", "clamp((dist_from_spawn / 1500) + (abs(y_coord - 70) / 100) + (nearest_player_level / 50) - 0.9, 0.9, 2.0)");

    static final ModConfigSpec SPEC = BUILDER.build();

    public static String healthFormula;
    public static String damageFormula;
    public static String lootFormula;
    public static String speedFormula;
    public static String knockbackFormula;

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event)
    {
        healthFormula = HEALTH_FORMULA.get();
        damageFormula = DAMAGE_FORMULA.get();
        lootFormula = LOOT_FORMULA.get();
        speedFormula = SPEED_FORMULA.get();
        knockbackFormula = KNOCKBACK_FORMULA.get();
    }
}
