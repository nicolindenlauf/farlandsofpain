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
            .comment("Formula for health scaling (use x_coord, y_coord, z_coord, dist_from_spawn, nearest_player_level, entity_health an current_day variables)")
            .define("health.healthFormula", "(dist_from_spawn / 1000) + 1.0");

    public static final ModConfigSpec.ConfigValue<Double> MIN_HEALTH = BUILDER
            .comment("Minimum health multiplier")
            .define("health.minHealth", 1.0);

    public static final ModConfigSpec.ConfigValue<Double> MAX_HEALTH = BUILDER
            .comment("Maximum health multiplier")
            .define("health.maxHealth", 1.0);

    public static final ModConfigSpec.ConfigValue<String> DAMAGE_FORMULA = BUILDER
            .comment("Formula for damage scaling (use x_coord, y_coord, z_coord, dist_from_spawn, nearest_player_level, entity_health an current_day variables)")
            .define("damage.damageFormula", "(dist_from_spawn / 500) + 1.0");

    public static final ModConfigSpec.ConfigValue<Double> MIN_DAMAGE = BUILDER
            .comment("Minimum damage multiplier")
            .define("damage.minDamage", 1.0);

    public static final ModConfigSpec.ConfigValue<Double> MAX_DAMAGE = BUILDER
            .comment("Maximum damage multiplier")
            .define("damage.maxDamage", 1.0);

    public static final ModConfigSpec.ConfigValue<String> LOOT_FORMULA = BUILDER
            .comment("Formula for loot scaling (use x_coord, y_coord, z_coord, dist_from_spawn, nearest_player_level, entity_health an current_day variables)")
            .define("loot.lootFormula", "(dist_from_spawn / 500) + 1.0");

    public static final ModConfigSpec.ConfigValue<Double> MIN_LOOT = BUILDER
            .comment("Minimum loot multiplier")
            .define("loot.minLoot", 1.0);

    public static final ModConfigSpec.ConfigValue<Double> MAX_LOOT = BUILDER
            .comment("Maximum loot multiplier")
            .define("loot.maxLoot", 1.0);

    public static final ModConfigSpec.ConfigValue<String> SPEED_FORMULA = BUILDER
            .comment("Formula for speed scaling (use x_coord, y_coord, z_coord, dist_from_spawn, nearest_player_level, entity_health an current_day variables)")
            .define("speed.speedFormula", "(dist_from_spawn / 500) + 1.0");

    public static final ModConfigSpec.ConfigValue<Double> MIN_SPEED = BUILDER
            .comment("Minimum speed multiplier")
            .define("speed.minSpeed", 1.0);

    public static final ModConfigSpec.ConfigValue<Double> MAX_SPEED = BUILDER
            .comment("Maximum speed multiplier")
            .define("speed.maxSpeed", 1.0);

    public static final ModConfigSpec.ConfigValue<String> KNOCKBACK_FORMULA = BUILDER
            .comment("Formula for knockback scaling (use x_coord, y_coord, z_coord, dist_from_spawn, nearest_player_level, entity_health an current_day variables)")
            .define("knockback.knockbackFormula", "(dist_from_spawn / 500) + 1.0");

    public static final ModConfigSpec.ConfigValue<Double> MIN_KNOCKBACK = BUILDER
            .comment("Minimum knockback multiplier")
            .define("knockback.minKnockback", 1.0);

    public static final ModConfigSpec.ConfigValue<Double> MAX_KNOCKBACK = BUILDER
            .comment("Maximum knockback multiplier")
            .define("knockback.maxKnockback", 1.0);

    static final ModConfigSpec SPEC = BUILDER.build();

    public static String healthFormula;
    public static double minHealth;
    public static double maxHealth;
    public static String damageFormula;
    public static double minDamage;
    public static double maxDamage;
    public static String lootFormula;
    public static double minLoot;
    public static double maxLoot;
    public static String speedFormula;
    public static double minSpeed;
    public static double maxSpeed;
    public static String knockbackFormula;
    public static double minKnockback;
    public static double maxKnockback;

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event)
    {
        healthFormula = HEALTH_FORMULA.get();
        minHealth = MIN_HEALTH.get();
        maxHealth = MAX_HEALTH.get();
        damageFormula = DAMAGE_FORMULA.get();
        minDamage = MIN_DAMAGE.get();
        maxDamage = MAX_DAMAGE.get();
        lootFormula = LOOT_FORMULA.get();
        minLoot = MIN_LOOT.get();
        maxLoot = MAX_LOOT.get();
        speedFormula = SPEED_FORMULA.get();
        minSpeed = MIN_SPEED.get();
        maxSpeed = MAX_SPEED.get();
        knockbackFormula = KNOCKBACK_FORMULA.get();
        minKnockback = MIN_KNOCKBACK.get();
        maxKnockback = MAX_KNOCKBACK.get();
    }
}
