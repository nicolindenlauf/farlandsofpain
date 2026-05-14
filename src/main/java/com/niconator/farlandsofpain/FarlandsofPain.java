package com.niconator.farlandsofpain;

import com.mojang.brigadier.Command;
import com.mojang.logging.LogUtils;
import com.niconator.farlandsofpain.utils.Utils;
import net.minecraft.commands.Commands;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingExperienceDropEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import org.slf4j.Logger;

@Mod(FarlandsofPain.MODID)
public class FarlandsofPain {
    public static final String MODID = "farlandsofpain";
    public static final Logger LOGGER = LogUtils.getLogger();
    public static final ResourceLocation HEALTH_MODIFIER_ID = ResourceLocation.fromNamespaceAndPath(MODID, "health_multiplier");
    public static final ResourceLocation SPEED_MODIFIER_ID = ResourceLocation.fromNamespaceAndPath(MODID, "speed_multiplier");
    public static final ResourceLocation KNOCKBACK_MODIFIER_ID = ResourceLocation.fromNamespaceAndPath(MODID, "knockback_resistance_multiplier");

    public FarlandsofPain(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(Config::onConfigLoad);
        modEventBus.addListener(Config::onConfigReload);
        modEventBus.addListener(NetworkHandler::register);
        if (FMLEnvironment.dist.isClient()) {
            initializeClient(modEventBus);
        }
        NeoForge.EVENT_BUS.register(this);
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private static void initializeClient(IEventBus modEventBus) {
        try {
            Class.forName("com.niconator.farlandsofpain.client.ClientPainOverlay")
                    .getMethod("init", IEventBus.class)
                    .invoke(null, modEventBus);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Failed to initialize Farlands of Pain client overlay", e);
        }
    }


    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("pain-o-meter")
                .requires(source -> source.getPlayer() != null)
                .executes(context -> {
                    NetworkHandler.toggleOverlay(context.getSource().getPlayerOrException());
                    return Command.SINGLE_SUCCESS;
                }));
    }

    @SubscribeEvent
    public void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            NetworkHandler.syncToPlayer(player);
        }
    }

    @SubscribeEvent
    public void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (!(event.getEntity() instanceof LivingEntity entity)) {
            debug(event.getEntity().getName() + " joined, but is not a LivingEntity, therefore can't have its stats modified");
            return;
        }

        if (isPlayer(entity)) {
            debug(entity.getName() + " joined. Players don't get their stats modified.");
            return;
        }

        applyAttributeMultiplier(entity, Attributes.MAX_HEALTH, HEALTH_MODIFIER_ID, Config.healthFormula,
                AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL, "health", Config.maxHealthMultiplier);
        entity.setHealth(entity.getMaxHealth());

        applyAttributeMultiplier(entity, Attributes.MOVEMENT_SPEED, SPEED_MODIFIER_ID, Config.speedFormula,
                AttributeModifier.Operation.ADD_MULTIPLIED_BASE, "speed", Config.maxSpeedMultiplier);

        applyAttributeMultiplier(entity, Attributes.KNOCKBACK_RESISTANCE, KNOCKBACK_MODIFIER_ID, Config.knockbackFormula,
                AttributeModifier.Operation.ADD_MULTIPLIED_BASE, "knockback resistance", Config.maxKnockbackMultiplier);
    }

    @SubscribeEvent
    public void onEntityHurt(LivingDamageEvent.Pre event) {
        LivingEntity entity = event.getEntity();
        Entity sourceEntity = event.getSource().getEntity();
        if (!(sourceEntity instanceof LivingEntity source)) {
            debug(entity.getName() + " took damage from a non-living or missing source, therefore damage won't be modified");
            return;
        }

        if (isPlayer(source)) {
            debug(source.getName() + " dealt damage to " + entity.getName() + ". Players don't get their damage modified.");
            return;
        }

        double damageMultiplier = Utils.calculateScalingMultiplier(entity, Config.damageFormula, 0.0, Config.maxDamageMultiplier);
        event.setNewDamage(event.getNewDamage() * (float) damageMultiplier);
        debug(entity.getName() + " had its received damage multiplied by " + damageMultiplier + ", evaluated with formula: " + Config.damageFormula);
    }

    @SubscribeEvent
    public void onLivingExperienceDrop(LivingExperienceDropEvent event) {
        LivingEntity entity = event.getEntity();
        if (isPlayer(entity)) {
            return;
        }

        int originalExperience = event.getDroppedExperience();
        if (originalExperience <= 0) {
            return;
        }

        double xpMultiplier = Utils.calculateScalingMultiplier(entity, Config.xpFormula, 0.0, Config.maxXpMultiplier);
        int scaledExperience = Utils.scaleExperience(originalExperience, xpMultiplier);
        event.setDroppedExperience(scaledExperience);
        debug(entity.getName() + " had its dropped XP changed from " + originalExperience + " to " + scaledExperience
                + " using multiplier " + xpMultiplier + ", evaluated with formula: " + Config.xpFormula);
    }

    @SubscribeEvent
    public void onLivingDeath(LivingDeathEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity.level().isClientSide()) {
            return;
        }

        int extraLootRolls = Math.min(Math.max((int) Utils.calculateScalingMultiplier(entity, Config.lootFormula) - 1, 0), Config.maxExtraLootRolls);
        if (extraLootRolls == 0) {
            return;
        }

        ServerLevel serverLevel = (ServerLevel) entity.level();
        ResourceKey<LootTable> lootTableId = entity.getType().getDefaultLootTable();
        LootTable lootTable = serverLevel.getServer().reloadableRegistries().getLootTable(lootTableId);
        if (lootTable == LootTable.EMPTY) {
            debug(entity.getName() + " has no default loot table, therefore can't have its loot modified");
            return;
        }

        LootParams lootParams = new LootParams.Builder(serverLevel)
                .withParameter(LootContextParams.THIS_ENTITY, entity)
                .withParameter(LootContextParams.ORIGIN, entity.position())
                .withParameter(LootContextParams.DAMAGE_SOURCE, event.getSource())
                .create(LootContextParamSets.ENTITY);

        for (int i = 0; i < extraLootRolls; i++) {
            lootTable.getRandomItems(lootParams, entity::spawnAtLocation);
        }
        debug(entity.getName() + " dropped " + extraLootRolls + " extra loot rolls, evaluated with formula: " + Config.lootFormula);
    }

    private static void applyAttributeMultiplier(
            LivingEntity entity,
            net.minecraft.core.Holder<net.minecraft.world.entity.ai.attributes.Attribute> attribute,
            ResourceLocation modifierId,
            String formula,
            AttributeModifier.Operation operation,
            String label,
            double maxMultiplier
    ) {
        AttributeInstance attributeInstance = entity.getAttribute(attribute);
        if (attributeInstance == null) {
            debug(entity.getName() + " has no " + label + " attribute, therefore it can't be modified");
            return;
        }

        if (attributeInstance.hasModifier(modifierId)) {
            debug(entity.getName() + " already has had its " + label + " modified");
            return;
        }

        double multiplier = Utils.calculateScalingMultiplier(entity, formula, 0.0, maxMultiplier);
        attributeInstance.addPermanentModifier(new AttributeModifier(modifierId, multiplier - 1, operation));
        debug(entity.getName() + " had its " + label + " multiplied by " + multiplier + ", evaluated with formula: " + formula);
    }

    private static boolean isPlayer(LivingEntity entity) {
        return entity instanceof Player || entity instanceof ServerPlayer;
    }

    private static void debug(String message) {
        if (Config.debug) {
            LOGGER.debug(MODID + ": " + message);
        }
    }
}
