package artifacts.mixin.item.pocketpiston.client;

import artifacts.extensions.pocketpiston.LivingEntityExtensions;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin implements LivingEntityExtensions {

    @Unique
    private static final int RETRACTION_DURATION = 2;

    @Unique
    private static final int RETRACTION_DELAY = 4;

    @Shadow
    public int swingTime;

    @Unique
    private float artifacts$pocketPistonLength;

    @Unique
    private int artifacts$pocketPistonTimeRemaining;

    @Inject(method = "tick", at = @At("HEAD"))
    private void updatePocketPistonLength(CallbackInfo ci) {
        float d = (artifacts$pocketPistonTimeRemaining < RETRACTION_DURATION ? -1F : 1F) / RETRACTION_DURATION;
        artifacts$pocketPistonLength = Math.max(0, Math.min(1, artifacts$pocketPistonLength + d));

        if (swingTime != 0) {
            artifacts$pocketPistonTimeRemaining = RETRACTION_DELAY + RETRACTION_DURATION;
        }

        if (artifacts$pocketPistonTimeRemaining > 0) {
            artifacts$pocketPistonTimeRemaining -= 1;
        }
    }

    @Unique
    @Override
    public float artifacts$getPocketPistonLength() {
        Minecraft minecraft = Minecraft.getInstance();
        float partialTicks = minecraft.getTimer().getGameTimeDeltaPartialTick(true);
        float d = (artifacts$pocketPistonTimeRemaining + partialTicks < RETRACTION_DURATION ? -1F : 1F) / RETRACTION_DURATION;
        return Math.max(0, Math.min(1, artifacts$pocketPistonLength + d * partialTicks));
    }
}
