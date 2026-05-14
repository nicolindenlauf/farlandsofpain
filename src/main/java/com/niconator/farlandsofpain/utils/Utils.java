package com.niconator.farlandsofpain.utils;

import com.niconator.farlandsofpain.Config;
import com.niconator.farlandsofpain.FarlandsofPain;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.objecthunter.exp4j.Expression;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class Utils {
    private static final double NEARBY_PLAYER_RADIUS = 256.0;
    private static final double NEARBY_MOB_RADIUS = 32.0;
    private static final Map<String, Expression> EXPRESSION_CACHE = new ConcurrentHashMap<>();

    private Utils() {
    }

    public static boolean isValidFormula(String formula) {
        return FormulaSupport.isValidFormula(formula);
    }

    public static void clearFormulaCache() {
        EXPRESSION_CACHE.clear();
    }

    public static double calculateScalingMultiplier(LivingEntity entity, String formula, double min, double max) {
        return FormulaSupport.clamp(calculateScalingMultiplier(entity, formula), min, max);
    }

    public static double calculateScalingMultiplier(LivingEntity entity, String formula) {
        int[] evaluatedCoords = getEvaluatedCoords(entity);
        if (Config.debug) {
            FarlandsofPain.LOGGER.debug(FarlandsofPain.MODID + ": Evaluated coords: " + evaluatedCoords[0] + ", " + evaluatedCoords[1] + ", " + evaluatedCoords[2]);
        }

        double horizontalDistanceFromSpawn = Math.sqrt((evaluatedCoords[0] * evaluatedCoords[0]) + (evaluatedCoords[2] * evaluatedCoords[2]));
        double distanceFromWorldOrigin = Math.sqrt((entity.getX() * entity.getX()) + (entity.getZ() * entity.getZ()));
        double verticalDepth = Math.max(-evaluatedCoords[1], 0.0);
        Player nearestPlayer = entity.level().getNearestPlayer(entity.getX(), entity.getY(), entity.getZ(), NEARBY_PLAYER_RADIUS, true);
        double nearestPlayerLevel = nearestPlayer == null ? 0.0 : nearestPlayer.experienceLevel;
        double nearestPlayerHealthPercent = nearestPlayer == null || nearestPlayer.getMaxHealth() <= 0.0F
                ? 0.0
                : nearestPlayer.getHealth() / nearestPlayer.getMaxHealth() * 100.0;
        int nearbyPlayerCount = countNearbyPlayers(entity);
        int nearbyMobCount = countNearbyMobs(entity);
        long currentDay = entity.level().getGameTime() / 24000L;
        long moonPhase = Math.floorMod(entity.level().getDayTime() / 24000L, 8L);
        double worldDifficulty = entity.level().getDifficulty().getId();
        double localDifficulty = entity.level().getCurrentDifficultyAt(entity.blockPosition()).getEffectiveDifficulty();

        try {
            Expression expression = EXPRESSION_CACHE.computeIfAbsent(formula, FormulaSupport::buildExpression);
            synchronized (expression) {
                return expression
                        .setVariable("x_coord", evaluatedCoords[0])
                        .setVariable("y_coord", evaluatedCoords[1])
                        .setVariable("z_coord", evaluatedCoords[2])
                        .setVariable("dist_from_spawn", horizontalDistanceFromSpawn)
                        .setVariable("horizontal_distance_from_spawn", horizontalDistanceFromSpawn)
                        .setVariable("distance_from_world_origin", distanceFromWorldOrigin)
                        .setVariable("vertical_depth", verticalDepth)
                        .setVariable("nearest_player_level", nearestPlayerLevel)
                        .setVariable("nearest_player_health_percent", nearestPlayerHealthPercent)
                        .setVariable("nearby_player_count", nearbyPlayerCount)
                        .setVariable("nearby_mob_count", nearbyMobCount)
                        .setVariable("entity_health", entity.getHealth())
                        .setVariable("entity_max_health", entity.getMaxHealth())
                        .setVariable("current_day", currentDay)
                        .setVariable("moon_phase", moonPhase)
                        .setVariable("world_difficulty", worldDifficulty)
                        .setVariable("local_difficulty", localDifficulty)
                        .evaluate();
            }
        } catch (Exception e) {
            FarlandsofPain.LOGGER.warn(FarlandsofPain.MODID + ": Error evaluating formula: " + formula + ": " + e.getMessage());
            return 1.0;
        }
    }

    public static double clamp(double value, double min, double max) {
        return FormulaSupport.clamp(value, min, max);
    }

    public static int scaleExperience(int originalExperience, double multiplier) {
        return FormulaSupport.scaleExperience(originalExperience, multiplier);
    }

    private static int countNearbyPlayers(LivingEntity entity) {
        double radiusSqr = NEARBY_PLAYER_RADIUS * NEARBY_PLAYER_RADIUS;
        int count = 0;
        for (Player player : entity.level().players()) {
            if (!player.isSpectator() && player.isAlive() && player.distanceToSqr(entity) <= radiusSqr) {
                count++;
            }
        }
        return count;
    }

    private static int countNearbyMobs(LivingEntity entity) {
        AABB area = entity.getBoundingBox().inflate(NEARBY_MOB_RADIUS);
        return entity.level().getEntitiesOfClass(LivingEntity.class, area, nearby -> isMobForFormula(entity, nearby)).size();
    }

    private static boolean isMobForFormula(LivingEntity source, LivingEntity nearby) {
        return nearby != source && !(nearby instanceof Player) && nearby.isAlive() && nearby.distanceToSqr(source) <= NEARBY_MOB_RADIUS * NEARBY_MOB_RADIUS;
    }

    private static int[] getEvaluatedCoords(LivingEntity entity) {
        String dimensionName = entity.level().dimension().location().toString();
        int dimensionOffset = Config.dimensionOffsets.getOrDefault(dimensionName, 0);
        if (!Config.dimensionOffsets.containsKey(dimensionName)) {
            FarlandsofPain.LOGGER.warn(FarlandsofPain.MODID + ": no config for dimension-offset: " + dimensionName + ". defaulting to 0.");
        }

        double xCoord;
        double yCoord = entity.getY() + dimensionOffset;
        double zCoord;
        if (Config.zeroZeroOrigin) {
            xCoord = entity.getX();
            zCoord = entity.getZ();
        } else {
            xCoord = entity.getX() - entity.level().getSharedSpawnPos().getX();
            zCoord = entity.getZ() - entity.level().getSharedSpawnPos().getZ();
        }

        return new int[] {(int) xCoord, (int) yCoord, (int) zCoord};
    }
}
