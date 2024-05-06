package artifacts.neoforge.event;

import artifacts.ability.*;
import artifacts.component.AbilityToggles;
import artifacts.item.UmbrellaItem;
import artifacts.platform.PlatformServices;
import artifacts.registry.ModAbilities;
import artifacts.registry.ModGameRules;
import artifacts.registry.ModTags;
import artifacts.util.AbilityHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.food.FoodProperties;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.*;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.UUID;

public class ArtifactEventsNeoForge {

    private static final AttributeModifier UMBRELLA_SLOW_FALLING = new AttributeModifier(
            UUID.fromString("a7a25453-2065-4a96-bc83-df600e13f390"),
            "artifacts:umbrella_slow_falling",
            -0.875,
            AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
    );

    public static void register() {
        NeoForge.EVENT_BUS.addListener(EventPriority.LOW, ArtifactEventsNeoForge::onLivingDamage);
        NeoForge.EVENT_BUS.addListener(EventPriority.HIGH, ArtifactEventsNeoForge::onLivingFall);
        NeoForge.EVENT_BUS.addListener(ArtifactEventsNeoForge::onLivingUpdate);
        NeoForge.EVENT_BUS.addListener(ArtifactEventsNeoForge::onDrinkingHatItemUse);
        NeoForge.EVENT_BUS.addListener(ArtifactEventsNeoForge::onGoldenHookExperienceDrop);
        NeoForge.EVENT_BUS.addListener(ArtifactEventsNeoForge::onKittySlippersChangeTarget);
        NeoForge.EVENT_BUS.addListener(EventPriority.LOW, ArtifactEventsNeoForge::onDiggingClawsBreakSpeed);
        NeoForge.EVENT_BUS.addListener(ArtifactEventsNeoForge::onDiggingClawsHarvestCheck);
        NeoForge.EVENT_BUS.addListener(ArtifactEventsNeoForge::onFoodEaten);
        NeoForge.EVENT_BUS.addListener(ArtifactEventsNeoForge::onPlayerTick);
    }

    private static void onPlayerTick(PlayerTickEvent.Post event) {
        AbilityToggles abilityToggles = PlatformServices.platformHelper.getAbilityToggles(event.getEntity());
        if (event.getEntity() instanceof ServerPlayer serverPlayer && abilityToggles != null) {
            abilityToggles.sendToClient(serverPlayer);
        }
    }

    private static void onLivingDamage(LivingDamageEvent event) {
        AbsorbDamageAbility.onLivingDamage(event.getEntity(), event.getSource(), event.getAmount());
        ApplyFireResistanceAfterFireDamageAbility.onLivingDamage(event.getEntity(), event.getSource(), event.getAmount());
    }

    private static void onLivingFall(LivingFallEvent event) {
        if (AbilityHelper.hasAbilityActive(ModAbilities.CANCEL_FALL_DAMAGE.get(), event.getEntity())) {
            event.setDamageMultiplier(0);
        }
        onCloudInABottleFall(event);
    }

    private static void onLivingUpdate(EntityTickEvent.Post event) {
        onKittySlippersLivingUpdate(event);
        onUmbrellaLivingUpdate(event);
    }

    private static void onCloudInABottleFall(LivingFallEvent event) {
        event.setDistance(DoubleJumpAbility.getReducedFallDistance(event.getEntity(), event.getDistance()));
    }

    private static void onDrinkingHatItemUse(LivingEntityUseItemEvent.Start event) {
        event.setDuration(ReduceEatingDurationAbility.getDrinkingHatUseDuration(event.getEntity(), event.getItem().getUseAnimation(), event.getDuration()));
    }

    private static void onGoldenHookExperienceDrop(LivingExperienceDropEvent event) {
        int originalXp = event.getOriginalExperience();
        int droppedXp = event.getDroppedExperience();
        int modifiedXp = droppedXp + ExperienceBonusAbility.getExperienceBonus(originalXp, event.getEntity(), event.getAttackingPlayer());
        event.setDroppedExperience(modifiedXp);
    }

    private static void onKittySlippersChangeTarget(LivingChangeTargetEvent event) {
        LivingEntity target = event.getNewTarget();
        if (AbilityHelper.hasAbilityActive(ModAbilities.SCARE_CREEPERS.get(), target)
                && event.getEntity() instanceof Mob creeper
                && creeper.getType().is(ModTags.CREEPERS)
        ) {
            event.setCanceled(true);
        }
    }

    private static void onKittySlippersLivingUpdate(EntityTickEvent event) {
        if (event.getEntity() instanceof LivingEntity entity) {
            if (AbilityHelper.hasAbilityActive(ModAbilities.SCARE_CREEPERS.get(), entity.getLastHurtByMob())
                    && event.getEntity().getType().is(ModTags.CREEPERS)
            ) {
                entity.setLastHurtByMob(null);
            }
        }
    }

    private static void onDiggingClawsBreakSpeed(PlayerEvent.BreakSpeed event) {
        float speedBonus = DigSpeedAbility.getSpeedBonus(event.getEntity(), event.getState());
        if (speedBonus > 0) {
            event.setNewSpeed(event.getNewSpeed() + speedBonus);
        }
    }

    private static void onDiggingClawsHarvestCheck(PlayerEvent.HarvestCheck event) {
        event.setCanHarvest(event.canHarvest() || UpgradeToolTierAbility.canHarvestWithTier(event.getEntity(), event.getTargetBlock()));
    }

    private static void onUmbrellaLivingUpdate(EntityTickEvent event) {
        if (event.getEntity() instanceof LivingEntity entity) {
            // TODO
            AttributeInstance gravity = entity.getAttribute(Attributes.GRAVITY);
            if (gravity != null) {
                boolean isInWater = entity.isInWater() && !AbilityHelper.hasAbilityActive(ModAbilities.SINKING.get(), entity);
                if (ModGameRules.UMBRELLA_IS_GLIDER.get()
                        && !entity.onGround() && !isInWater
                        && entity.getDeltaMovement().y < 0
                        && !entity.hasEffect(MobEffects.SLOW_FALLING)
                        && UmbrellaItem.isHoldingUmbrellaUpright(entity)
                ) {
                    if (!gravity.hasModifier(UMBRELLA_SLOW_FALLING)) {
                        gravity.addTransientModifier(UMBRELLA_SLOW_FALLING);
                    }
                    entity.fallDistance = 0;
                } else if (gravity.hasModifier(UMBRELLA_SLOW_FALLING)) {
                    gravity.removeModifier(UMBRELLA_SLOW_FALLING.id());
                }
            }
        }
    }

    private static void onFoodEaten(LivingEntityUseItemEvent.Finish event) {
        FoodProperties properties = event.getItem().getFoodProperties(event.getEntity());
        if (properties != null) {
            ApplyHasteAfterEatingAbility.applyHasteEffect(event.getEntity(), properties);
            GrowPlantsAfterEatingAbility.applyBoneMeal(event.getEntity(), properties);
        }
    }
}