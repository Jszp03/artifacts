package artifacts.mixin.attribute.invincibilityticks;

import artifacts.registry.ModAttributes;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {

    @Inject(method = "hurt", at = @At(value = "FIELD", target = "Lnet/minecraft/world/entity/LivingEntity;invulnerableTime:I", opcode = Opcodes.PUTFIELD, shift = At.Shift.AFTER))
    private void hurt(DamageSource damageSource, float f, CallbackInfoReturnable<Boolean> cir) {
        artifacts$applyBonusTicks();
    }

    @Inject(method = "handleDamageEvent", at = @At(value = "FIELD", target = "Lnet/minecraft/world/entity/LivingEntity;invulnerableTime:I", opcode = Opcodes.PUTFIELD, shift = At.Shift.AFTER))
    private void handleDamageEvent(DamageSource damageSource, CallbackInfo ci) {
        artifacts$applyBonusTicks();
    }

    @SuppressWarnings("ConstantConditions")
    @Unique
    private void artifacts$applyBonusTicks() {
        LivingEntity entity = (LivingEntity) (Object) this;
        int bonusTicks = (int) entity.getAttributeValue(ModAttributes.INVINCIBILITY_TICKS);
        entity.invulnerableTime += bonusTicks;
    }
}
