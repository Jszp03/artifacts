package artifacts.registry;

import artifacts.Artifacts;
import artifacts.platform.PlatformServices;
import dev.architectury.registry.registries.DeferredRegister;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;

import java.util.ArrayList;
import java.util.List;

public class ModAttributes {

    public static final DeferredRegister<Attribute> ATTRIBUTES = DeferredRegister.create(Artifacts.MOD_ID, Registries.ATTRIBUTE);

    public static final List<Holder<Attribute>> PLAYER_ATTRIBUTES = new ArrayList<>();
    public static final List<Holder<Attribute>> GENERIC_ATTRIBUTES = new ArrayList<>();

    public static final Holder<Attribute> ENTITY_EXPERIENCE = addPlayerAttribute("entity_experience", 1, 0, 64);
    public static final Holder<Attribute> VILLAGER_REPUTATION = addPlayerAttribute("villager_reputation", 0, 0, 1024);

    public static final Holder<Attribute> ATTACK_BURNING_DURATION = addGenericAttribute("attack_burning_duration", 0, 0, 60);
    public static final Holder<Attribute> DRINKING_SPEED = addGenericAttribute("drinking_speed", 1, 1, Double.MAX_VALUE);
    public static final Holder<Attribute> EATING_SPEED = addGenericAttribute("eating_speed", 1, 1, Double.MAX_VALUE);
    public static final Holder<Attribute> FLATULENCE = addGenericAttribute("flatulence", 0, 0, 1);
    public static final Holder<Attribute> INVINCIBILITY_TICKS = addGenericAttribute("invincibility_ticks", 0, 0, 20 * 60);
    public static final Holder<Attribute> MOUNT_SPEED = addGenericAttribute("mount_speed", 1, 1, 1024);
    public static final Holder<Attribute> MOVEMENT_SPEED_ON_SNOW = addGenericAttribute("movement_speed_on_snow", 1, 0, 1024);
    public static final Holder<Attribute> SLIP_RESISTANCE = addGenericAttribute("slip_resistance", 0, 0, 1);
    public static final Holder<Attribute> SPRINTING_SPEED = addGenericAttribute("sprinting_speed", 1, 1, 1024);
    public static final Holder<Attribute> SPRINTING_STEP_HEIGHT = addGenericAttribute("sprinting_step_height", 0, 0, 10);
    public static final Holder<Attribute> SWIM_SPEED = PlatformServices.platformHelper.getSwimSpeedAttribute();

    public static Holder<Attribute> addPlayerAttribute(String name, double d, double min, double max) {
        String id = "player." + name;
        Holder<Attribute> attribute = register(id, d, min, max);
        PLAYER_ATTRIBUTES.add(attribute);
        return attribute;
    }

    public static Holder<Attribute> addGenericAttribute(String name, double d, double min, double max) {
        String id = "generic." + name;
        Holder<Attribute> attribute = register(id, d, min, max);
        GENERIC_ATTRIBUTES.add(attribute);
        return attribute;
    }

    public static Holder<Attribute> register(String name, double d, double min, double max) {
        return PlatformServices.platformHelper.registerAttribute(name, new RangedAttribute(name, d, min, max).setSyncable(true));
    }
}
