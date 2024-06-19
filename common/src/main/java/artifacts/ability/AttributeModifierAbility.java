package artifacts.ability;

import artifacts.config.value.Value;
import artifacts.config.value.ValueTypes;
import artifacts.registry.ModAbilities;
import artifacts.registry.ModAttributes;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public record AttributeModifierAbility(Holder<Attribute> attribute, Value<Double> amount, AttributeModifier.Operation operation, ResourceLocation id, boolean ignoreCooldown) implements ArtifactAbility {

    private static final Set<Holder<Attribute>> POSITIVE_ATTRIBUTES_WITH_TOOLTIP;
    private static final Set<Holder<Attribute>> NEGATIVE_ATTRIBUTES_WITH_TOOLTIP = Set.of(
            Attributes.SCALE,
            Attributes.FALL_DAMAGE_MULTIPLIER
    );

    static {
        POSITIVE_ATTRIBUTES_WITH_TOOLTIP = new HashSet<>();
        POSITIVE_ATTRIBUTES_WITH_TOOLTIP.addAll(ModAttributes.PLAYER_ATTRIBUTES);
        POSITIVE_ATTRIBUTES_WITH_TOOLTIP.addAll(ModAttributes.GENERIC_ATTRIBUTES);
        POSITIVE_ATTRIBUTES_WITH_TOOLTIP.add(ModAttributes.SWIM_SPEED);
        POSITIVE_ATTRIBUTES_WITH_TOOLTIP.addAll(List.of(
                Attributes.ATTACK_DAMAGE,
                Attributes.ATTACK_KNOCKBACK,
                Attributes.ATTACK_SPEED,
                Attributes.BLOCK_BREAK_SPEED,
                Attributes.JUMP_STRENGTH,
                Attributes.KNOCKBACK_RESISTANCE,
                Attributes.MAX_HEALTH,
                Attributes.SAFE_FALL_DISTANCE
        ));
        POSITIVE_ATTRIBUTES_WITH_TOOLTIP.remove(ModAttributes.MAX_ATTACK_DAMAGE_ABSORBED);
    }

    public static final MapCodec<AttributeModifierAbility> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            BuiltInRegistries.ATTRIBUTE.holderByNameCodec().fieldOf("attribute").forGetter(AttributeModifierAbility::attribute),
            ValueTypes.ATTRIBUTE_MODIFIER_AMOUNT.codec().fieldOf("amount").forGetter(AttributeModifierAbility::amount),
            AttributeModifier.Operation.CODEC.fieldOf("operation").forGetter(AttributeModifierAbility::operation),
            ResourceLocation.CODEC.fieldOf("id").forGetter(AttributeModifierAbility::id),
            Codec.BOOL.optionalFieldOf("ignore_cooldown", true).forGetter(AttributeModifierAbility::ignoreCooldown)
    ).apply(instance, AttributeModifierAbility::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, AttributeModifierAbility> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.holderRegistry(Registries.ATTRIBUTE),
            AttributeModifierAbility::attribute,
            ValueTypes.ATTRIBUTE_MODIFIER_AMOUNT.streamCodec(),
            AttributeModifierAbility::amount,
            AttributeModifier.Operation.STREAM_CODEC,
            AttributeModifierAbility::operation,
            ResourceLocation.STREAM_CODEC,
            AttributeModifierAbility::id,
            ByteBufCodecs.BOOL,
            AttributeModifierAbility::ignoreCooldown,
            AttributeModifierAbility::new
    );

    public AttributeModifier createModifier() {
        return new AttributeModifier(id(), amount().get(), operation());
    }

    private void onAttributeUpdated(LivingEntity entity) {
        if (attribute() == Attributes.MAX_HEALTH && entity.getHealth() > entity.getMaxHealth()) {
            entity.setHealth(entity.getMaxHealth());
        }
    }

    @Override
    public Type<?> getType() {
        return ModAbilities.ATTRIBUTE_MODIFIER.value();
    }

    @Override
    public boolean isNonCosmetic() {
        return !Mth.equal(amount().get(), 0);
    }

    @Override
    public void onUnequip(LivingEntity entity, boolean wasActive) {
        AttributeInstance attributeInstance = entity.getAttribute(attribute());
        if (attributeInstance != null && wasActive) {
            attributeInstance.removeModifier(id());
            onAttributeUpdated(entity);
        }
    }

    @Override
    public void wornTick(LivingEntity entity, boolean isOnCooldown, boolean isActive) {
        AttributeInstance attributeInstance = entity.getAttribute(attribute());
        if (attributeInstance == null) {
            return;
        }
        AttributeModifier existingModifier = attributeInstance.getModifier(id());
        if (!ignoreCooldown() && isOnCooldown) {
            if (isActive) {
                onUnequip(entity, true);
            }
        } else {
            if (existingModifier == null || !Mth.equal(amount().get(), existingModifier.amount())) {
                attributeInstance.removeModifier(id());
                attributeInstance.addPermanentModifier(createModifier());
                onAttributeUpdated(entity);
            }
        }
    }

    @Override
    public void addAbilityTooltip(List<MutableComponent> tooltip) {
        String attributeName = attribute().unwrapKey().orElseThrow().location().getPath();
        if (attributeName.equals("swim_speed")) { // neoforge swim speed
            attributeName = "generic.swim_speed";
        }

        if (amount().get() > 0) {
            for (Holder<Attribute> attribute : POSITIVE_ATTRIBUTES_WITH_TOOLTIP) {
                if (attribute.isBound() && attribute.value() == attribute().value()) {
                    tooltip.add(tooltipLine(attributeName));
                }
            }
        } else {
            for (Holder<Attribute> attribute : NEGATIVE_ATTRIBUTES_WITH_TOOLTIP) {
                if (attribute.isBound() && attribute.value() == attribute().value()) {
                    tooltip.add(tooltipLine(attributeName));
                }
            }
        }
    }
}
