package artifacts.fabric.mixin.ability.smeltores;

import artifacts.event.ArtifactEvents;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.mutable.MutableInt;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LootTable.class)
public class LootTableMixin {

    @ModifyReturnValue(method = "getRandomItems(Lnet/minecraft/world/level/storage/loot/LootContext;)Lit/unimi/dsi/fastutil/objects/ObjectArrayList;", at = @At("RETURN"))
    public ObjectArrayList<ItemStack> modifyBlockDrops(ObjectArrayList<ItemStack> original, LootContext context) {
        if (context.hasParam(LootContextParams.ORIGIN)) {
            Vec3 origin = context.getParam(LootContextParams.ORIGIN);
            MutableInt experience = new MutableInt(0);
            ObjectArrayList<ItemStack> result = new ObjectArrayList<>(original.size());
            for (ItemStack stack : original) {
                result.add(ArtifactEvents.applySmeltOresAbility(
                        stack,
                        context.getParamOrNull(LootContextParams.THIS_ENTITY),
                        context.getParamOrNull(LootContextParams.BLOCK_STATE),
                        experience::add
                ));
            }
            ExperienceOrb.award(context.getLevel(), origin, experience.intValue());
            return result;
        }
        return original;
    }
}
