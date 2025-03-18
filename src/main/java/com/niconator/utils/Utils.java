package com.niconator.utils;

import com.niconator.FarlandsofPain;
import com.niconator.Config;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
import net.objecthunter.exp4j.function.Function;

public class Utils {
    
    public static double calculateScalingMultimplier(LivingEntity entity, String formula) {

        // Define custom functions
        // Min
        Function lower = new Function("min", 2) {

            @Override
            public double apply(double... args) {
                return Math.min(args[0], args[1]);
            }
        };
    
        // Max
        Function higher = new Function("max", 2) {
    
            @Override
            public double apply(double... args) {
                return Math.max(args[0], args[1]);
            }
        };
    
        // Clamp
        Function clamp = new Function("clamp", 3) {
    
            @Override
            public double apply(double... args) {
                return Math.min(Math.max(args[0], args[1]), args[2]);
            }
        };
    
        // Round
        Function round = new Function("round", 1) {
    
            @Override
            public double apply(double... args) {
                return Math.round(args[0]);
            }
        };

        // Get variables
        int[] evaluatedCoords = getEvaluatedCoords(entity);
        if (Config.debug) {
            FarlandsofPain.LOGGER.debug(FarlandsofPain.MODID + ": Evaluated coords: " + evaluatedCoords[0] + ", " + evaluatedCoords[1] + ", " + evaluatedCoords[2]);
        }

        double dist = Math.sqrt((evaluatedCoords[0] * evaluatedCoords[0]) + (evaluatedCoords[2] * evaluatedCoords[2]));

        Player nearestPlayer = entity.level().getNearestPlayer(entity.getX(), entity.getY(), entity.getZ(), 256.0, true);
        double nearestPlayerLevel = 0.0;
        if (nearestPlayer != null) {
            nearestPlayerLevel = nearestPlayer.experienceLevel;
        }

        // Evaluate the formula
        double multiplier;
        try {
            Expression expression = new ExpressionBuilder(formula)
                    .functions(lower, higher, clamp, round)
                    .variables("x_coord", "y_coord", "z_coord", "dist_from_spawn", "nearest_player_level", "entity_health", "current_day") // Define allowed variables
                    .build()
                    .setVariable("x_coord", evaluatedCoords[0])
                    .setVariable("y_coord", evaluatedCoords[1])
                    .setVariable("z_coord", evaluatedCoords[2])
                    .setVariable("dist_from_spawn", dist)
                    .setVariable("nearest_player_level", nearestPlayerLevel)
                    .setVariable("entity_health", entity.getHealth())
                    .setVariable("current_day", entity.level().getGameTime()/24000L);

            multiplier = expression.evaluate();
        } catch (Exception e) {
            FarlandsofPain.LOGGER.warn(FarlandsofPain.MODID + ": Error evaluating formula: " + formula + ": " + e.getMessage());
            multiplier = 1.0;
        }

        return multiplier;
    }

    private static int[] getEvaluatedCoords(LivingEntity entity) {
        // Get dimension name
        String dimensionName = entity.level().dimension().location().toString();
        
        // Get dimension offset
        int dimensionOffset;
        try {
            dimensionOffset = Config.dimensionOffsets.get(dimensionName);
        } catch (Exception e) {
            dimensionOffset = 0;
            FarlandsofPain.LOGGER.warn(FarlandsofPain.MODID + ": no config for dimension-offset: " + dimensionName + ". defaulting to 0.");
        }
        
        // Evaluate coordinates
        double x_coord;
        double y_coord;
        double z_coord;
        if (Config.zeroZeroOrigin) {
            x_coord = entity.getX();
            y_coord = entity.getY() + dimensionOffset;
            z_coord = entity.getZ();
        } else {
            x_coord = entity.getX() - entity.level().getSharedSpawnPos().getX();
            y_coord = entity.getY() + dimensionOffset;
            z_coord = entity.getZ() - entity.level().getSharedSpawnPos().getZ();
        }

        return new int[] {(int) x_coord, (int) y_coord, (int) z_coord};
    }
}
