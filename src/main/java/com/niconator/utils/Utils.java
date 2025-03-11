package com.niconator.utils;

import com.niconator.FarlandsofPain;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;

public class Utils {
    public static double calculateScalingMultimplier(LivingEntity entity, String formula, double min, double max) {
        double x_coord = entity.getX();
        double y_coord = entity.getY(); // height
        double z_coord = entity.getZ();

        Vec3 vector = new Vec3(x_coord, 0, z_coord);
        double dist = vector.length();

        Player nearestPlayer = entity.level().getNearestPlayer(entity.getX(), entity.getY(), entity.getZ(), 100.0, true);
        double nearestPlayerlevel = 0.0;
        if (nearestPlayer != null) {
            nearestPlayerlevel = nearestPlayer.experienceLevel;
        }

        double multiplier = 1.0; // Fallback value
        try {
            Expression expression = new ExpressionBuilder(formula)
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

        return Math.max(min, Math.min(max, multiplier));
    }
}
