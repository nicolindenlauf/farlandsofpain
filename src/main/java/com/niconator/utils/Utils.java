package com.niconator.utils;

import com.niconator.FarlandsofPain;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
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
        String dimensionName = entity.level().dimension().location().toString();
        FarlandsofPain.LOGGER.debug(FarlandsofPain.MODID + ": Dimension name: " + dimensionName);

        double x_coord = entity.getX();
        double y_coord = entity.getY();
        if (dimensionName.equals("minecraft:overworld")) {
        } else if (dimensionName.equals("minecraft:the_nether")) {
            y_coord = y_coord - 64 - 128; // overworld ends at -64, nether starts at 128
        } else if (dimensionName.equals("minecraft:the_end")) {
            y_coord = y_coord - 64 - 128 - 128 - 70; // overworld ends at -64, nether starts at 128, nether is 128 blocks deep, 70 is neutral height
        } else if (dimensionName.equals("aether:the_aether")) {
            y_coord = y_coord + 200 + 70; // clouds are 200 blocks high, 70 is neutral height
        }
        double z_coord = entity.getZ();

        Vec3 vector = new Vec3(x_coord, 0, z_coord);
        double dist = vector.length();

        Player nearestPlayer = entity.level().getNearestPlayer(entity.getX(), entity.getY(), entity.getZ(), 100.0, true);
        double nearestPlayerlevel = 0.0;
        if (nearestPlayer != null) {
            nearestPlayerlevel = nearestPlayer.experienceLevel;
        }

        // Evaluate the formula
        double multiplier = 1.0; // Fallback value
        try {
            Expression expression = new ExpressionBuilder(formula)
                    .functions(lower, higher, clamp, round)
                    .variables("x_coord", "y_coord", "z_coord", "dist_from_spawn", "nearest_player_level", "entity_health", "current_day") // Define allowed variables
                    .build()
                    .setVariable("x_coord", x_coord)
                    .setVariable("y_coord", y_coord)
                    .setVariable("z_coord", z_coord)
                    .setVariable("dist_from_spawn", dist)
                    .setVariable("nearest_player_level", nearestPlayerlevel)
                    .setVariable("entity_health", entity.getHealth())
                    .setVariable("current_day", entity.level().getGameTime()/24000L);

            multiplier = expression.evaluate();
        } catch (Exception e) {
            FarlandsofPain.LOGGER.warn(FarlandsofPain.MODID + ": Error evaluating formula: " + formula + ": " + e.getMessage());
        }

        return multiplier;
    }
}
