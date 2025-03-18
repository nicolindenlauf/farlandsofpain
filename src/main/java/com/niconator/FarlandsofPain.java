package com.niconator;

import com.niconator.utils.Utils;

import org.slf4j.Logger;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(FarlandsofPain.MODID)
public class FarlandsofPain {
    public static final String MODID = "farlandsofpain";
    public static final Logger LOGGER = LogUtils.getLogger();
    public static final ResourceLocation HEALTH_MODIFIER_ID = ResourceLocation.fromNamespaceAndPath(MODID, "health_multiplier");
    public static final ResourceLocation SPEED_MODIFIER_ID = ResourceLocation.fromNamespaceAndPath(MODID, "speed_multiplier");
    public static final ResourceLocation KNOCKBACK_MODIFIER_ID = ResourceLocation.fromNamespaceAndPath(MODID, "knockback_resistance_multiplier");

    public FarlandsofPain(IEventBus modEventBus, ModContainer modContainer) {
        NeoForge.EVENT_BUS.register(this);
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("pain-o-meter")
            .requires(source -> source.hasPermission(1))
            .executes(context -> {
                painOMeter(context);
                return Command.SINGLE_SUCCESS;
            }));
    }
    
    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        FarlandsofPain.register(event.getDispatcher());
    }

    // Apply the scaling to mob stats
    @SubscribeEvent
    public void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (event.getEntity() == null) {
            if (Config.debug) {
                LOGGER.debug(MODID + ": some entity joined, but is null, therefore can't have its stats modified");
            }
            return;
        }

        if (!(event.getEntity() instanceof LivingEntity)) {
            if (Config.debug) {
                LOGGER.debug(MODID + ": " + event.getEntity().getName() + " joined, but is not a LivingEntity, therefore can't have its stats modified");
            }
            return;
        }
        LivingEntity entity = (LivingEntity) event.getEntity();

        // Skip Player ServerPlayer
        if (entity instanceof ServerPlayer || entity instanceof Player) {
            if (Config.debug) {
                LOGGER.debug(MODID + ": " + event.getEntity().getName() + " joined. Players dont get their stats modified.");
            }
            return;
        }

        // Apply the scaling to health
        double healthMultiplier = Utils.calculateScalingMultimplier(entity, Config.healthFormula);
        AttributeInstance healthAttribute = entity.getAttribute(Attributes.MAX_HEALTH);
        if (healthAttribute != null) {
            try {
                healthAttribute.addPermanentModifier(new AttributeModifier(
                    HEALTH_MODIFIER_ID, // Unique ID for the modifier
                    healthMultiplier - 1, // Amount to add (e.g., 0.5 for 50% increase)
                    AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL // Multiply the base value
                ));
                entity.setHealth((float) (healthAttribute.getValue())); // Update current health
                if (Config.debug) {
                    LOGGER.debug(MODID + ": " + event.getEntity().getName() + " had its health multiplied by " + healthMultiplier + ", evaluated with formula: " + Config.healthFormula);
                }
            } catch (Exception e) {
                if (Config.debug) {
                    LOGGER.debug(MODID + ": " + event.getEntity().getName() + " already has had its health modified");
                }
            }
        } else {
            if (Config.debug) {
                LOGGER.debug(MODID + ": " + event.getEntity().getName() + " has no health attribute, therefore can't have its health modified");
            }
        }

        // Apply the scaling to speed
        double speedMultiplier = Utils.calculateScalingMultimplier(entity, Config.speedFormula);
        AttributeInstance speedAttribute = entity.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speedAttribute != null) {
            try {
                speedAttribute.addPermanentModifier(new AttributeModifier(
                    SPEED_MODIFIER_ID, // Unique ID for the modifier
                    speedMultiplier - 1, // Amount to add (e.g., 0.5 for 50% increase)
                    AttributeModifier.Operation.ADD_MULTIPLIED_BASE // Multiply the base value
                ));
                if (Config.debug) {
                    LOGGER.debug(MODID + ": " + event.getEntity().getName() + " had its speed multiplied by " + speedMultiplier + ", evaluated with formula: " + Config.speedFormula);
                }
            } catch (Exception e) {
                if (Config.debug) {
                    LOGGER.debug(MODID + ": " + event.getEntity().getName() + " already has had its speed modified");
                }
            }
        } else {
            if (Config.debug) {
                LOGGER.debug(MODID + ": " + event.getEntity().getName() + " has no speed attribute, therefore can't have its speed modified");
            }
        }

        // Apply the scaling to knockback resistance
        double knockbackMultiplier = Utils.calculateScalingMultimplier(entity, Config.knockbackFormula);
        AttributeInstance knockbackAttribute = entity.getAttribute(Attributes.KNOCKBACK_RESISTANCE);
        if (knockbackAttribute != null) {
            try {
                knockbackAttribute.addPermanentModifier(new AttributeModifier(
                    KNOCKBACK_MODIFIER_ID, // Unique ID for the modifier
                    knockbackMultiplier - 1, // Amount to add (e.g., 0.5 for 50% increase)
                    AttributeModifier.Operation.ADD_MULTIPLIED_BASE // Multiply the total value
                ));
                if (Config.debug) {
                    LOGGER.debug(MODID + ": " + event.getEntity().getName() + " had its knockback resistence multiplied by " + knockbackMultiplier + ", evaluated with formula: " + Config.knockbackFormula);
                }
            } catch (Exception e) {
                if (Config.debug) {
                    LOGGER.debug(MODID + ": " + event.getEntity().getName() + " already has had its knockback resistance modified");
                }
            }
        } else {
            if (Config.debug) {
                LOGGER.debug(MODID + ": " + event.getEntity().getName() + " has no knockback resistance attribute, therefore can't have its knockback resistance modified");
            }
        }
    }

    // Apply the scaling to damage received
    @SubscribeEvent
    public void onEntityHurt(LivingDamageEvent.Pre event) {
        if (event.getEntity() == null) {
            if (Config.debug) {
                LOGGER.debug(MODID + ": some entity took damage, but is null, therefore can't have its recieved damage modified");
            }
            return;
        }
        LivingEntity entity = event.getEntity();

        if (event.getSource().getEntity() == null) {
            if (Config.debug) {
                LOGGER.debug(MODID + ": some entity took damage, but the source is null, therefore can't have its damage modified");
            }
            return;
        }
        LivingEntity source = (LivingEntity) event.getSource().getEntity();
        
        // Skip damage from Player ServerPlayer
        if ((source instanceof ServerPlayer) || (source instanceof Player)) {
            if (Config.debug) {
                LOGGER.debug(MODID + ": " + source.getName() + " dealt damage to " + entity.getName() + ". Players dont get their damage modified.");
            }
            return;
        }

        double damageMultiplier = Utils.calculateScalingMultimplier(entity, Config.damageFormula);
        event.setNewDamage(event.getNewDamage() * (float) damageMultiplier);
        if (Config.debug) {
            LOGGER.debug(MODID + ": " + event.getEntity().getName() + " had its recieved damage multiplied by " + damageMultiplier + ", evaluated with formula: " + Config.damageFormula);
        }
    }

    // Apply the scaling to loot
    @SubscribeEvent
    public void onLivingDeath(LivingDeathEvent event) {
        if (event.getEntity() == null) {
            if (Config.debug) {
                LOGGER.debug(MODID + ": some entity died, but is null, therefore can't have its loot modified");
            }
            return;
        }
        LivingEntity entity = event.getEntity();
        
        if (entity.level().isClientSide()) {
            return; // Function can only be run on the server
        }

        int lootMultiplier = (int) Utils.calculateScalingMultimplier((LivingEntity) entity, Config.lootFormula);
        lootMultiplier = Math.max(lootMultiplier - 1, 0); // convert from Multiplier to Bonus rolls, ensure never below 0
        
        ServerLevel serverWorld = (ServerLevel) entity.level();
        ResourceKey<LootTable> lootTableId = entity.getType().getDefaultLootTable();
        LootTable lootTable = serverWorld.getServer().reloadableRegistries().getLootTable(lootTableId);
        
        if (lootTable == LootTable.EMPTY) {
            if (Config.debug) {
                LOGGER.debug(MODID + ": " + event.getEntity().getName() + " has no default loot table, therefore can't have its loot modified");
            }
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
        if (Config.debug) {
            LOGGER.debug(MODID + ": " + event.getEntity().getName() + " has dropped " + lootMultiplier + " extra loot drops, evaluated with formula: " + Config.lootFormula);
        }
    }
    
    public static void painOMeter(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        if (context.getSource().getEntity() == null) {
            if (Config.debug) {
                LOGGER.debug(MODID + ": someone used pain-o-meter, but is null, therefore can't get a Pain-O-Meter message");
            }
            return;
        }
        LivingEntity entity = (LivingEntity) context.getSource().getEntity();
        
        double healthMultiplier = Utils.calculateScalingMultimplier(entity, Config.healthFormula);
        double speedMultiplier = Utils.calculateScalingMultimplier(entity, Config.speedFormula);
        double knockbackMultiplier = Utils.calculateScalingMultimplier(entity, Config.knockbackFormula);
        double damageMultiplier = Utils.calculateScalingMultimplier(entity, Config.damageFormula);
        int lootMultiplier = (int) Utils.calculateScalingMultimplier(entity, Config.lootFormula);

        // Create a formatted message
        Component message = Component.literal("=== Pain-O-Meter ===\n")
                .append(Component.literal("Health Multiplier: ").withStyle(ChatFormatting.RED))
                .append(Component.literal(String.format("%.2f", healthMultiplier)).withStyle(ChatFormatting.WHITE))
                .append(Component.literal("\nSpeed Multiplier: ").withStyle(ChatFormatting.GREEN))
                .append(Component.literal(String.format("%.2f", speedMultiplier)).withStyle(ChatFormatting.WHITE))
                .append(Component.literal("\nKnockback Multiplier: ").withStyle(ChatFormatting.BLUE))
                .append(Component.literal(String.format("%.2f", knockbackMultiplier)).withStyle(ChatFormatting.WHITE))
                .append(Component.literal("\nDamage Multiplier: ").withStyle(ChatFormatting.DARK_RED))
                .append(Component.literal(String.format("%.2f", damageMultiplier)).withStyle(ChatFormatting.WHITE))
                .append(Component.literal("\nLoot Multiplier: ").withStyle(ChatFormatting.GOLD))
                .append(Component.literal(String.valueOf(lootMultiplier)).withStyle(ChatFormatting.WHITE));

        // Send the message to the player the second null check is only because my IDE is complaining
        if (entity == null) {
            if (Config.debug) {
                LOGGER.debug(MODID + ": someone used pain-o-meter, but is null, therefore can't get a Pain-O-Meter message");
            }
            return;
        }
        entity.sendSystemMessage(message);
    }
}
