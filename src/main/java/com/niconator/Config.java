package com.niconator;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

@EventBusSubscriber(modid = FarlandsofPain.MODID, bus = EventBusSubscriber.Bus.MOD)
public class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    // General
    public static final ModConfigSpec.ConfigValue<String> DIMENSION_OFFSETS_JSON = BUILDER
            .comment("JSON string for defining dimension y-offsets.\nFor reference: overworld neutral height is 63, clouds are 191, overworld bedrock is -64, nether upper bedrock is 127, nether lower bedrock is 0, end neutral height is 63, aether neutral height is 63.\nThe default is: {\"minecraft:overworld\": -63, \"minecraft:the_nether\": -255, \"minecraft:the_end\": -512, \"aether:the_aether\": 128}")
            .define("general.dimension_offsets_json", "{\"minecraft:overworld\": -63, \"minecraft:the_nether\": -255, \"minecraft:the_end\": -512, \"aether:the_aether\": 128}");

    public static final ModConfigSpec.ConfigValue<Boolean> ZERO_ZERO_ORIGIN = BUILDER
            .comment("Use 0 0 as origin, instead of the spawn position. The default is: false")
            .define("general.zero_zero_origin", false);

    // Scaling formulas
    public static final ModConfigSpec.ConfigValue<String> HEALTH_FORMULA = BUILDER
            .comment("The scaling formulas for mob stats.\nAvailable variables: use x_coord, y_coord, z_coord, dist_from_spawn, nearest_player_level, entity_health and current_day variables.\nAvailable operators: +, -, *, /, ^, %.\nAvailable functions: min, max, clamp, round,  abs, acos, asin, atan, cbrt, ceil, cos, cosh, exp, floor, log, log10, log2, sin, sinh, sqrt, tan, tanh, signum.\n")
            .comment("The scaling-multiplier formula for health. The default is: max((dist_from_spawn / 750) + (abs(y_coord) / 100) + (nearest_player_level / 50) - 0.75, 0.75)")
            .define("formulas.healthFormula", "max((dist_from_spawn / 750) + (abs(y_coord) / 100) + (nearest_player_level / 50) - 0.75, 0.75)");

    public static final ModConfigSpec.ConfigValue<String> DAMAGE_FORMULA = BUILDER
            .comment("The scaling-multiplier formula for damage received. The default is: max((dist_from_spawn / 750) + (abs(y_coord) / 100) + (nearest_player_level / 50) - 0.75, 0.75)")
            .define("formulas.damageFormula", "max((dist_from_spawn / 750) + (abs(y_coord) / 100) + (nearest_player_level / 50) - 0.75, 0.75)");

    public static final ModConfigSpec.ConfigValue<String> LOOT_FORMULA = BUILDER
            .comment("The scaling-multiplier formula for loot. The default is: clamp((dist_from_spawn / 750) + (abs(y_coord) / 100) + (nearest_player_level / 50) - 1.0, 1.0, 10.0)")
            .define("formulas.lootFormula", "clamp((dist_from_spawn / 750) + (abs(y_coord) / 100) + (nearest_player_level / 50) - 1.0, 1.0, 10.0)");

    public static final ModConfigSpec.ConfigValue<String> SPEED_FORMULA = BUILDER
            .comment("The scaling-multiplier formula for speed. The default is: clamp((dist_from_spawn / 1500) + (abs(y_coord) / 100) + (nearest_player_level / 50) - 1.0, 1.0, 2.0)")
            .define("formulas.speedFormula", "clamp((dist_from_spawn / 1500) + (abs(y_coord) / 100) + (nearest_player_level / 50) - 1.0, 1.0, 2.0)");

    public static final ModConfigSpec.ConfigValue<String> KNOCKBACK_FORMULA = BUILDER
            .comment("The scaling-multiplier formula for knockback resistance. The default is: clamp((dist_from_spawn / 1500) + (abs(y_coord) / 100) + (nearest_player_level / 50) - 0.9, 0.9, 2.0)")
            .define("formulas.knockbackFormula", "clamp((dist_from_spawn / 1500) + (abs(y_coord) / 100) + (nearest_player_level / 50) - 0.9, 0.9, 2.0)");

    // Debug
    public static final ModConfigSpec.ConfigValue<Boolean> DEBUG = BUILDER
            .comment("Enable debug logging. The default is: false")
            .define("debug.debug", false);

    static final ModConfigSpec SPEC = BUILDER.build();

    // Cached values
    public static Map<String, Integer> dimensionOffsets = new HashMap<>();
    public static Boolean zeroZeroOrigin;
    public static String healthFormula;
    public static String damageFormula;
    public static String lootFormula;
    public static String speedFormula;
    public static String knockbackFormula;
    public static Boolean debug;

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
        knockbackFormula = KNOCKBACK_FORMULA.get();
        zeroZeroOrigin = ZERO_ZERO_ORIGIN.get();
        healthFormula = HEALTH_FORMULA.get();
        damageFormula = DAMAGE_FORMULA.get();
        lootFormula = LOOT_FORMULA.get();
        speedFormula = SPEED_FORMULA.get();
        debug = DEBUG.get();

        // Parse JSON string for dimension offsets
        Gson gson = new Gson();
        Type type = new TypeToken<Map<String, Integer>>() {}.getType();
        dimensionOffsets = gson.fromJson(DIMENSION_OFFSETS_JSON.get(), type);
    }
}
