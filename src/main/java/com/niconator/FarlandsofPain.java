package com.niconator;

import com.niconator.utils.Utils;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.server.level.ServerPlayer;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(FarlandsofPain.MODID)
public class FarlandsofPain {
    public static final String MODID = "farlandsofpain";
    public static final Logger LOGGER = LogUtils.getLogger();

    public FarlandsofPain(IEventBus modEventBus, ModContainer modContainer) {
        NeoForge.EVENT_BUS.register(this);
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    // public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
    //     dispatcher.register(Commands.literal("debugcommand")
    //         .requires(source -> source.hasPermission(2))
    //         .executes(context -> {
    //             debugcommand(context);
    //             return Command.SINGLE_SUCCESS;
    //         }));
    // }
    //
    // @SubscribeEvent
    // public void onRegisterCommands(RegisterCommandsEvent event) {
    //     FarlandsofPain.register(event.getDispatcher());
    // }

    // Apply the scaling to mob stats
    @SubscribeEvent
    public void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (event.getEntity() instanceof LivingEntity) {
            LivingEntity entity = (LivingEntity) event.getEntity();

            if (entity instanceof Player && Config.debug) {
                LOGGER.debug(MODID + ": Skipping player entity");
                return;
            }

            // Apply the scaling to health
            double healthMultiplier = Utils.calculateScalingMultimplier(entity, Config.healthFormula);
            if (Config.debug) {
                LOGGER.debug(MODID + ": Health multiplier: " + healthMultiplier + " evaluated with formula: " + Config.healthFormula);
            }

            AttributeInstance healthAttribute = entity.getAttribute(Attributes.MAX_HEALTH);
            if (healthAttribute != null) {
                healthAttribute.setBaseValue(healthAttribute.getBaseValue() * healthMultiplier);
                entity.setHealth((float) (healthAttribute.getBaseValue()));
            }

            // Apply the scaling to speed
            double speedMultiplier = Utils.calculateScalingMultimplier(entity, Config.speedFormula);
            if (Config.debug) {
                LOGGER.debug(MODID + ": Speed multiplier: " + speedMultiplier + " evaluated with formula: " + Config.speedFormula);
            }
    
            AttributeInstance speedAttribute = entity.getAttribute(Attributes.MOVEMENT_SPEED);
            if (speedAttribute != null) {
                speedAttribute.setBaseValue(speedAttribute.getBaseValue() * speedMultiplier);
            }

            // Apply the scaling to knockback resistance
            double knockbackMultiplier = Utils.calculateScalingMultimplier(entity, Config.knockbackFormula);
            if (Config.debug) {
                LOGGER.debug(MODID + ": Knockback multiplier: " + knockbackMultiplier + " evaluated with formula: " + Config.knockbackFormula);
            }
    
            AttributeInstance knockbackAttribute = entity.getAttribute(Attributes.KNOCKBACK_RESISTANCE);
            if (knockbackAttribute != null) {
                knockbackAttribute.setBaseValue(knockbackAttribute.getBaseValue() * knockbackMultiplier);
            }
        }
    }

    // Apply the scaling to damage received
    @SubscribeEvent
    public void onEntityHurt(LivingDamageEvent.Pre event) {
        if (!(event.getSource().getEntity() instanceof ServerPlayer) && event.getEntity() != null) {
            float damageMultiplier = (float) Utils.calculateScalingMultimplier((LivingEntity) event.getEntity(), Config.damageFormula);
            event.setNewDamage(event.getNewDamage() * damageMultiplier);
            if (Config.debug) {
                LOGGER.debug(MODID + ": Damage multiplier: " + damageMultiplier + " evaluated with formula: " + Config.damageFormula);
            }
        }
    }

    // Apply the scaling to loot
    @SubscribeEvent
    public void onLivingDeath(LivingDeathEvent event) {
        LivingEntity entity = event.getEntity();
        int lootMultiplier = (int) Utils.calculateScalingMultimplier((LivingEntity) event.getEntity(), Config.lootFormula);
        if (Config.debug) {
            LOGGER.debug(MODID + ": Loot multiplier: " + lootMultiplier + " evaluated with formula: " + Config.lootFormula);
        }

        if (!entity.level().isClientSide) { // Ensure it's server-side
            ServerLevel serverWorld = (ServerLevel) entity.level();
            ResourceKey<LootTable> lootTableId = entity.getType().getDefaultLootTable();
            LootTable lootTable = serverWorld.getServer().reloadableRegistries().getLootTable(lootTableId);

            if (lootTable == LootTable.EMPTY) {
                return;
            }

            // Build loot context
            LootParams lootParams = new LootParams.Builder(serverWorld)
                    .withParameter(LootContextParams.THIS_ENTITY, entity)
                    .withParameter(LootContextParams.ORIGIN, entity.position())
                    .withParameter(LootContextParams.DAMAGE_SOURCE, event.getSource())
                    .create(LootContextParamSets.ENTITY);

            // Drop loot with extra rolls
            for (int i = 0; i < lootMultiplier; i++) {
                lootTable.getRandomItems(lootParams, entity::spawnAtLocation);
            }
        }
    }
}

// To Do:
// - Danger-O-Meter
