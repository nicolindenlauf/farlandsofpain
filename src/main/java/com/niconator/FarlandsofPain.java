package com.niconator;

import org.slf4j.Logger;

import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;

import com.mojang.logging.LogUtils;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.minecraft.world.entity.player.Player;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.monster.Monster;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.minecraft.world.entity.ai.attributes.Attributes;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(FarlandsofPain.MODID)
public class FarlandsofPain {
    // Define mod id in a common place for everything to reference
    public static final String MODID = "farlandsofpain";
    // Directly reference a slf4j logger
    private static final Logger LOGGER = LogUtils.getLogger();

    // The constructor for the mod class is the first code that is run when your mod
    // is loaded.
    // FML will recognize some parameter types like IEventBus or ModContainer and
    // pass them in automatically.
    public FarlandsofPain(IEventBus modEventBus, ModContainer modContainer) {
        // Register ourselves for server and other game events we are interested in.
        // Note that this is necessary if and only if we want *this* class
        // (FarlandsofPain) to respond directly to events.
        // Do not add this line if there are no @SubscribeEvent-annotated functions in
        // this class, like onServerStarting() below.
        NeoForge.EVENT_BUS.register(this);

        // Register our mod's ModConfigSpec so that FML can create and load the config
        // file for us
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    @SubscribeEvent
    public void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (event.getEntity() instanceof Monster) {
            LivingEntity entity = (LivingEntity) event.getEntity();
            // Example of modifying health and damage based on the spawn location
            double x = entity.getX(); // Get the X coordinate of spawn
            double y = entity.getY(); // Get the Y coordinate of spawn (height)
            double z = entity.getZ(); // Get the Z coordinate of spawn

            Vec3 vector = new Vec3(x, 0, z);
            double dist = vector.length();

            Player nearestPlayer = entity.level().getNearestPlayer(entity.getX(), entity.getY(), entity.getZ(), 100.0, true);
            double nearestPlayerlevel = 0.0;
            if (nearestPlayer != null) {
                nearestPlayerlevel = nearestPlayer.experienceLevel;
            }

            double healthMultiplier = 1.0; // Default value
            try {
                Expression expression = new ExpressionBuilder(Config.healthFormula)
                        .variables("x", "y", "z", "dist", "nearestPlayerlevel", "entityhealth", "current_day") // Define allowed variables
                        .build()
                        .setVariable("x", x)
                        .setVariable("y", y)
                        .setVariable("z", z)
                        .setVariable("dist", dist)
                        .setVariable("nearestPlayerlevel", nearestPlayerlevel)
                        .setVariable("entityhealth", entity.getHealth())
                        .setVariable("current_day", entity.level().getGameTime()/24000L);

                healthMultiplier = expression.evaluate(); // Evaluate the formula
            } catch (Exception e) {
                LOGGER.warn(MODID + ": Error evaluating formula: " + Config.healthFormula + ": " + e.getMessage());
            }
            LOGGER.info(MODID + ": Health formula: " + Config.healthFormula);
            LOGGER.info(MODID + ": evaluated Health multiplier: " + healthMultiplier);
            // Apply the scaling to health
            AttributeInstance healthAttribute = entity.getAttribute(Attributes.MAX_HEALTH);
            if (healthAttribute != null) {
                healthAttribute.setBaseValue(healthAttribute.getBaseValue() * healthMultiplier);
                entity.setHealth((float) (healthAttribute.getBaseValue() * healthMultiplier));
            }
            
            // Apply the scaling to damage (melee attack damage)
            AttributeInstance attackDamageAttribute = entity.getAttribute(Attributes.ATTACK_DAMAGE);
            if (attackDamageAttribute != null) {
                attackDamageAttribute.setBaseValue(attackDamageAttribute.getBaseValue() * healthMultiplier);
            }
            LOGGER.info(MODID + ": final entityhealth: " + entity.getHealth());
        }
    }
}


// To Do:
// - Modify the following:
//   - Health (done)
//   - Attack damage and Ranged attack damage
//   - Loot rolls
//   - Speed
//   - Knockback resistance
// - Limit to hostile mobs
