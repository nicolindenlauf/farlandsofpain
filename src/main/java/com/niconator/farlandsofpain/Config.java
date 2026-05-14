package com.niconator.farlandsofpain;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.niconator.farlandsofpain.utils.Utils;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public final class Config {
    private static final Gson GSON = new Gson();
    private static final Type DIMENSION_OFFSETS_TYPE = new TypeToken<Map<String, Integer>>() {}.getType();
    private static final String DEFAULT_DIMENSION_OFFSETS_JSON = "{\"minecraft:overworld\": -63, \"minecraft:the_nether\": -255, \"minecraft:the_end\": -512, \"aether:the_aether\": 128}";
    private static final String FORMULA_HELP = "Variables: x_coord, y_coord, z_coord, dist_from_spawn, horizontal_distance_from_spawn, distance_from_world_origin, vertical_depth, nearest_player_level, nearest_player_health_percent, nearby_player_count, nearby_mob_count, entity_health, entity_max_health, current_day, moon_phase, world_difficulty, local_difficulty. "
            + "Operators: +, -, *, /, ^, %. Functions: min, max, clamp, round, pow, lerp, smoothstep, logistic, select, gt, gte, lt, lte, eq, abs, acos, asin, atan, cbrt, ceil, cos, cosh, exp, floor, log, log10, log2, sin, sinh, sqrt, tan, tanh, signum.";
    private static final String DEFAULT_HEALTH_FORMULA = "max((dist_from_spawn / 750) + (abs(y_coord) / 100) + (nearest_player_level / 50) - 0.75, 0.75)";
    private static final String DEFAULT_LOOT_FORMULA = "max((dist_from_spawn / 750) + (abs(y_coord) / 100) + (nearest_player_level / 50) - 1.0, 1.0)";
    private static final String DEFAULT_SPEED_FORMULA = "max((dist_from_spawn / 1500) + (abs(y_coord) / 100) + (nearest_player_level / 50) - 1.0, 1.0)";
    private static final String DEFAULT_KNOCKBACK_FORMULA = "max((dist_from_spawn / 1500) + (abs(y_coord) / 100) + (nearest_player_level / 50) - 0.9, 0.9)";

    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.ConfigValue<String> DIMENSION_OFFSETS_JSON;
    public static final ModConfigSpec.BooleanValue ZERO_ZERO_ORIGIN;
    public static final ModConfigSpec.ConfigValue<String> HEALTH_FORMULA;
    public static final ModConfigSpec.ConfigValue<String> DAMAGE_FORMULA;
    public static final ModConfigSpec.ConfigValue<String> LOOT_FORMULA;
    public static final ModConfigSpec.ConfigValue<String> XP_FORMULA;
    public static final ModConfigSpec.ConfigValue<String> SPEED_FORMULA;
    public static final ModConfigSpec.ConfigValue<String> KNOCKBACK_FORMULA;
    public static final ModConfigSpec.DoubleValue MAX_HEALTH_MULTIPLIER;
    public static final ModConfigSpec.DoubleValue MAX_DAMAGE_MULTIPLIER;
    public static final ModConfigSpec.DoubleValue MAX_SPEED_MULTIPLIER;
    public static final ModConfigSpec.DoubleValue MAX_KNOCKBACK_MULTIPLIER;
    public static final ModConfigSpec.DoubleValue MAX_XP_MULTIPLIER;
    public static final ModConfigSpec.IntValue MAX_EXTRA_LOOT_ROLLS;
    public static final ModConfigSpec.IntValue OVERLAY_X;
    public static final ModConfigSpec.IntValue OVERLAY_Y;
    public static final ModConfigSpec.IntValue OVERLAY_UPDATE_INTERVAL_TICKS;
    public static final ModConfigSpec.BooleanValue DEBUG;
    public static final ModConfigSpec SPEC;

    public static String dimensionOffsetsJson = DEFAULT_DIMENSION_OFFSETS_JSON;
    public static Map<String, Integer> dimensionOffsets = defaultDimensionOffsets();
    public static boolean zeroZeroOrigin = false;
    public static String healthFormula = DEFAULT_HEALTH_FORMULA;
    public static String damageFormula = DEFAULT_HEALTH_FORMULA;
    public static String lootFormula = DEFAULT_LOOT_FORMULA;
    public static String xpFormula = DEFAULT_LOOT_FORMULA;
    public static String speedFormula = DEFAULT_SPEED_FORMULA;
    public static String knockbackFormula = DEFAULT_KNOCKBACK_FORMULA;
    public static double maxHealthMultiplier = 1024.0;
    public static double maxDamageMultiplier = 1024.0;
    public static double maxSpeedMultiplier = 16.0;
    public static double maxKnockbackMultiplier = 16.0;
    public static double maxXpMultiplier = 1024.0;
    public static int maxExtraLootRolls = 64;
    public static int overlayX = 8;
    public static int overlayY = 8;
    public static int overlayUpdateIntervalTicks = 10;
    public static boolean debug = false;

    static {
        BUILDER.comment("General settings for distance and dimension calculations.").push("general");
        DIMENSION_OFFSETS_JSON = BUILDER
                .comment(
                        "JSON object mapping dimension IDs to Y-coordinate offsets.",
                        "These offsets let formulas treat different dimensions as deeper or higher than the Overworld.",
                        "Configured shows this option as a text field, so keep the value valid JSON.",
                        "Default: " + DEFAULT_DIMENSION_OFFSETS_JSON
                )
                .define("dimensionOffsetsJson", DEFAULT_DIMENSION_OFFSETS_JSON, Config::isValidDimensionOffsetsJson);
        ZERO_ZERO_ORIGIN = BUILDER
                .comment(
                        "Use world coordinate 0,0 as the horizontal origin.",
                        "When disabled, distance formulas use the world spawn position instead."
                )
                .define("zeroZeroOrigin", false);
        BUILDER.pop();

        BUILDER.comment(
                "Formula settings used to calculate mob scaling multipliers.",
                "All formulas must evaluate to a finite number with the variables listed below.",
                FORMULA_HELP
        ).push("formulas");
        HEALTH_FORMULA = defineFormula("healthFormula", DEFAULT_HEALTH_FORMULA, "Multiplier for mob maximum health.");
        DAMAGE_FORMULA = defineFormula("damageFormula", DEFAULT_HEALTH_FORMULA, "Multiplier for damage dealt by scaled non-player mobs.");
        LOOT_FORMULA = defineFormula("lootFormula", DEFAULT_LOOT_FORMULA, "Loot multiplier. The mod converts this to extra loot rolls by subtracting 1 and rounding down.");
        XP_FORMULA = defineFormula("xpFormula", DEFAULT_LOOT_FORMULA, "Experience multiplier for XP dropped by scaled non-player mobs.");
        SPEED_FORMULA = defineFormula("speedFormula", DEFAULT_SPEED_FORMULA, "Multiplier for mob movement speed.");
        KNOCKBACK_FORMULA = defineFormula("knockbackFormula", DEFAULT_KNOCKBACK_FORMULA, "Multiplier for mob knockback resistance.");
        BUILDER.pop();

        BUILDER.comment(
                "Safety limits applied after formula evaluation.",
                "These limits protect worlds from accidental extreme values in formulas."
        ).push("limits");
        MAX_HEALTH_MULTIPLIER = BUILDER
                .comment("Maximum health multiplier after formula evaluation. The minimum is always 0.")
                .defineInRange("maxHealthMultiplier", 1024.0, 0.0, 1_000_000.0);
        MAX_DAMAGE_MULTIPLIER = BUILDER
                .comment("Maximum damage multiplier after formula evaluation. The minimum is always 0.")
                .defineInRange("maxDamageMultiplier", 1024.0, 0.0, 1_000_000.0);
        MAX_SPEED_MULTIPLIER = BUILDER
                .comment("Maximum movement-speed multiplier after formula evaluation. Keep this modest to avoid broken pathfinding.")
                .defineInRange("maxSpeedMultiplier", 16.0, 0.0, 1024.0);
        MAX_KNOCKBACK_MULTIPLIER = BUILDER
                .comment("Maximum knockback-resistance multiplier after formula evaluation.")
                .defineInRange("maxKnockbackMultiplier", 16.0, 0.0, 1024.0);
        MAX_XP_MULTIPLIER = BUILDER
                .comment("Maximum experience multiplier after formula evaluation. The minimum is always 0.")
                .defineInRange("maxXpMultiplier", 1024.0, 0.0, 1_000_000.0);
        MAX_EXTRA_LOOT_ROLLS = BUILDER
                .comment("Maximum extra loot rolls from one mob death.")
                .defineInRange("maxExtraLootRolls", 64, 0, 1024);
        BUILDER.pop();

        BUILDER.comment("Client-side Pain-O-Meter overlay settings.").push("client");
        OVERLAY_X = BUILDER
                .comment("Overlay X position in screen pixels, counted from the left edge.")
                .defineInRange("overlayX", 8, 0, 10_000);
        OVERLAY_Y = BUILDER
                .comment("Overlay Y position in screen pixels, counted from the top edge.")
                .defineInRange("overlayY", 8, 0, 10_000);
        OVERLAY_UPDATE_INTERVAL_TICKS = BUILDER
                .comment("How often the overlay recalculates formulas, in game ticks. 20 ticks is roughly one second.")
                .defineInRange("overlayUpdateIntervalTicks", 10, 1, 200);
        BUILDER.pop();

        BUILDER.comment("Diagnostics for troubleshooting formulas and entity scaling.").push("debug");
        DEBUG = BUILDER
                .comment("Enable detailed debug logging. Leave this disabled during normal gameplay.")
                .define("debugLogging", false);
        BUILDER.pop();

        SPEC = BUILDER.build();
    }

    private Config() {
    }

    static void onConfigLoad(final ModConfigEvent.Loading event) {
        bake(event.getConfig());
    }

    static void onConfigReload(final ModConfigEvent.Reloading event) {
        bake(event.getConfig());
        NetworkHandler.syncToAllPlayers();
    }

    public static void applySyncedValues(ConfigSyncPayload payload) {
        applyValues(
                payload.dimensionOffsetsJson(),
                payload.zeroZeroOrigin(),
                payload.healthFormula(),
                payload.damageFormula(),
                payload.lootFormula(),
                payload.xpFormula(),
                payload.speedFormula(),
                payload.knockbackFormula(),
                payload.maxHealthMultiplier(),
                payload.maxDamageMultiplier(),
                payload.maxSpeedMultiplier(),
                payload.maxKnockbackMultiplier(),
                payload.maxXpMultiplier(),
                payload.maxExtraLootRolls(),
                overlayX,
                overlayY,
                overlayUpdateIntervalTicks,
                debug
        );
    }

    private static ModConfigSpec.ConfigValue<String> defineFormula(String name, String defaultValue, String comment) {
        return BUILDER
                .comment(comment, "Invalid formulas are rejected when the config loads.", FORMULA_HELP)
                .define(name, defaultValue, value -> value instanceof String string && Utils.isValidFormula(string));
    }

    private static void bake(ModConfig config) {
        if (!FarlandsofPain.MODID.equals(config.getModId()) || config.getType() != ModConfig.Type.COMMON) {
            return;
        }

        applyValues(
                DIMENSION_OFFSETS_JSON.get(),
                ZERO_ZERO_ORIGIN.get(),
                HEALTH_FORMULA.get(),
                DAMAGE_FORMULA.get(),
                LOOT_FORMULA.get(),
                XP_FORMULA.get(),
                SPEED_FORMULA.get(),
                KNOCKBACK_FORMULA.get(),
                MAX_HEALTH_MULTIPLIER.get(),
                MAX_DAMAGE_MULTIPLIER.get(),
                MAX_SPEED_MULTIPLIER.get(),
                MAX_KNOCKBACK_MULTIPLIER.get(),
                MAX_XP_MULTIPLIER.get(),
                MAX_EXTRA_LOOT_ROLLS.get(),
                OVERLAY_X.get(),
                OVERLAY_Y.get(),
                OVERLAY_UPDATE_INTERVAL_TICKS.get(),
                DEBUG.get()
        );
    }

    private static void applyValues(
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
            int maxExtraLootRolls,
            int overlayX,
            int overlayY,
            int overlayUpdateIntervalTicks,
            boolean debug
    ) {
        Config.dimensionOffsetsJson = dimensionOffsetsJson;
        Config.dimensionOffsets = parseDimensionOffsets(dimensionOffsetsJson);
        Config.zeroZeroOrigin = zeroZeroOrigin;
        Config.healthFormula = healthFormula;
        Config.damageFormula = damageFormula;
        Config.lootFormula = lootFormula;
        Config.xpFormula = xpFormula;
        Config.speedFormula = speedFormula;
        Config.knockbackFormula = knockbackFormula;
        Config.maxHealthMultiplier = maxHealthMultiplier;
        Config.maxDamageMultiplier = maxDamageMultiplier;
        Config.maxSpeedMultiplier = maxSpeedMultiplier;
        Config.maxKnockbackMultiplier = maxKnockbackMultiplier;
        Config.maxXpMultiplier = maxXpMultiplier;
        Config.maxExtraLootRolls = maxExtraLootRolls;
        Config.overlayX = overlayX;
        Config.overlayY = overlayY;
        Config.overlayUpdateIntervalTicks = overlayUpdateIntervalTicks;
        Config.debug = debug;
        Utils.clearFormulaCache();
    }

    private static boolean isValidDimensionOffsetsJson(Object value) {
        if (!(value instanceof String json)) {
            return false;
        }

        try {
            return !parseDimensionOffsets(json).isEmpty();
        } catch (JsonSyntaxException | ClassCastException e) {
            return false;
        }
    }

    private static Map<String, Integer> parseDimensionOffsets(String json) {
        Map<String, Integer> parsed = GSON.fromJson(json, DIMENSION_OFFSETS_TYPE);
        if (parsed == null) {
            return defaultDimensionOffsets();
        }
        return new HashMap<>(parsed);
    }

    private static Map<String, Integer> defaultDimensionOffsets() {
        Map<String, Integer> defaults = new HashMap<>();
        defaults.put("minecraft:overworld", -63);
        defaults.put("minecraft:the_nether", -255);
        defaults.put("minecraft:the_end", -512);
        defaults.put("aether:the_aether", 128);
        return defaults;
    }
}
