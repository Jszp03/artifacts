package artifacts.neoforge.data;

import artifacts.Artifacts;
import artifacts.loot.ArtifactRarityAdjustedChance;
import artifacts.loot.ConfigValueChance;
import artifacts.neoforge.loot.RollLootTableModifier;
import artifacts.registry.ModItems;
import artifacts.registry.ModLootTables;
import com.google.common.collect.ImmutableList;
import com.google.gson.*;
import com.mojang.serialization.JsonOps;
import net.minecraft.advancements.critereon.EntityFlagsPredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.functions.SmeltItemFunction;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemEntityPropertyCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemKilledByPlayerCondition;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.common.loot.LootTableIdCondition;

import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class LootModifiers implements DataProvider {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    protected final List<Builder> lootBuilders = new ArrayList<>();
    private final PackOutput packOutput;
    private final Map<String, JsonElement> toSerialize = new HashMap<>();

    public LootModifiers(PackOutput packOutput) {
        this.packOutput = packOutput;
    }

    private void addLoot() {
        for (ResourceKey<LootTable> lootTable : List.of(EntityType.COW.getDefaultLootTable(), EntityType.MOOSHROOM.getDefaultLootTable())) {
            lootBuilders.add(
                    new Builder(lootTable)
                            .lootPoolCondition(ConfigValueChance.everlastingBeefChance())
                            .lootModifierCondition(LootTableIdCondition.builder(lootTable.location()))
                            .parameterSet(LootContextParamSets.ENTITY)
                            .lootPoolCondition(LootItemKilledByPlayerCondition.killedByPlayer())
                            .everlastingBeef()
            );
        }

        for (ResourceKey<LootTable> lootTable : Arrays.asList(
                BuiltInLootTables.VILLAGE_DESERT_HOUSE,
                BuiltInLootTables.VILLAGE_PLAINS_HOUSE,
                BuiltInLootTables.VILLAGE_SAVANNA_HOUSE
        )) {
            builder(lootTable, 0.05F)
                    .item(ModItems.VILLAGER_HAT.value());
        }
        for (ResourceKey<LootTable> lootTable : Arrays.asList(
                BuiltInLootTables.VILLAGE_SNOWY_HOUSE,
                BuiltInLootTables.VILLAGE_TAIGA_HOUSE
        )) {
            builder(lootTable, 0.08F)
                    .item(ModItems.VILLAGER_HAT.value())
                    .item(ModItems.SNOWSHOES.value());
        }

        builder(BuiltInLootTables.SPAWN_BONUS_CHEST, 1)
                .item(ModItems.WHOOPEE_CUSHION.value());
        builder(BuiltInLootTables.VILLAGE_ARMORER, 0.1F)
                .item(ModItems.STEADFAST_SPIKES.value())
                .item(ModItems.SUPERSTITIOUS_HAT.value())
                .item(ModItems.RUNNING_SHOES.value())
                .item(ModItems.VAMPIRIC_GLOVE.value());
        builder(BuiltInLootTables.VILLAGE_BUTCHER, 0.05F)
                .item(ModItems.EVERLASTING_BEEF.value());
        builder(BuiltInLootTables.VILLAGE_TANNERY, 0.2F)
                .item(ModItems.UMBRELLA.value(), 3)
                .item(ModItems.WHOOPEE_CUSHION.value(), 2)
                .item(ModItems.KITTY_SLIPPERS.value())
                .item(ModItems.BUNNY_HOPPERS.value())
                .item(ModItems.SCARF_OF_INVISIBILITY.value())
                .item(ModItems.ANGLERS_HAT.value());
        builder(BuiltInLootTables.VILLAGE_TEMPLE, 0.2F)
                .item(ModItems.CROSS_NECKLACE.value())
                .item(ModItems.ANTIDOTE_VESSEL.value())
                .item(ModItems.CHARM_OF_SINKING.value())
                .item(ModItems.WARP_DRIVE.value());
        builder(BuiltInLootTables.VILLAGE_TOOLSMITH, 0.15F)
                .item(ModItems.DIGGING_CLAWS.value())
                .item(ModItems.POCKET_PISTON.value());
        builder(BuiltInLootTables.VILLAGE_WEAPONSMITH, 0.1F)
                .item(ModItems.FERAL_CLAWS.value(), 2)
                .item(ModItems.CHARM_OF_SHRINKING.value());
        builder(BuiltInLootTables.ABANDONED_MINESHAFT, 0.3F)
                .item(ModItems.ONION_RING.value(), 2)
                .item(ModItems.NIGHT_VISION_GOGGLES.value())
                .item(ModItems.PANIC_NECKLACE.value())
                .item(ModItems.OBSIDIAN_SKULL.value())
                .item(ModItems.SUPERSTITIOUS_HAT.value())
                .item(ModItems.DIGGING_CLAWS.value())
                .item(ModItems.CLOUD_IN_A_BOTTLE.value())
                .item(ModItems.VAMPIRIC_GLOVE.value())
                .item(ModItems.AQUA_DASHERS.value())
                .item(ModItems.PICKAXE_HEATER.value())
                .drinkingHat(1);
        builder(BuiltInLootTables.BASTION_HOGLIN_STABLE, 0.25F)
                .artifact(5)
                .item(ModItems.BUNNY_HOPPERS.value(), 3)
                .item(ModItems.FLAME_PENDANT.value(), 3)
                .item(ModItems.COWBOY_HAT.value(), 3)
                .item(ModItems.PICKAXE_HEATER.value(), 3)
                .item(ModItems.WITHERED_BRACELET.value(), 3)
                .item(ModItems.EVERLASTING_BEEF.value());
        builder(BuiltInLootTables.BASTION_TREASURE, 0.65F)
                .artifact(6)
                .item(ModItems.GOLDEN_HOOK.value(), 3)
                .item(ModItems.CROSS_NECKLACE.value(), 3)
                .item(ModItems.FIRE_GAUNTLET.value(), 2)
                .item(ModItems.STEADFAST_SPIKES.value())
                .item(ModItems.PANIC_NECKLACE.value())
                .item(ModItems.CRYSTAL_HEART.value())
                .item(ModItems.ANTIDOTE_VESSEL.value());
        builder(BuiltInLootTables.BURIED_TREASURE, 0.25F)
                .item(ModItems.SNORKEL.value(), 5)
                .item(ModItems.FLIPPERS.value(), 5)
                .item(ModItems.UMBRELLA.value(), 5)
                .item(ModItems.GOLDEN_HOOK.value(), 5)
                .item(ModItems.FERAL_CLAWS.value(), 3)
                .item(ModItems.DIGGING_CLAWS.value(), 3)
                .item(ModItems.KITTY_SLIPPERS.value(), 3)
                .item(ModItems.BUNNY_HOPPERS.value(), 3)
                .item(ModItems.LUCKY_SCARF.value(), 3)
                .item(ModItems.AQUA_DASHERS.value(), 3)
                .item(ModItems.ANGLERS_HAT.value(), 3)
                .drinkingHat(3);
        builder(BuiltInLootTables.DESERT_PYRAMID, 0.2F)
                .item(ModItems.FLAME_PENDANT.value(), 2)
                .item(ModItems.THORN_PENDANT.value(), 2)
                .item(ModItems.WHOOPEE_CUSHION.value(), 2)
                .item(ModItems.CHARM_OF_SINKING.value(), 2)
                .item(ModItems.SHOCK_PENDANT.value())
                .item(ModItems.UMBRELLA.value())
                .item(ModItems.SCARF_OF_INVISIBILITY.value())
                .item(ModItems.UNIVERSAL_ATTRACTOR.value())
                .item(ModItems.VAMPIRIC_GLOVE.value())
                .item(ModItems.ONION_RING.value());
        builder(BuiltInLootTables.END_CITY_TREASURE, 0.3F)
                .item(ModItems.CHORUS_TOTEM.value(), 6)
                .item(ModItems.HELIUM_FLAMINGO.value(), 4)
                .item(ModItems.WARP_DRIVE.value(), 2)
                .item(ModItems.CRYSTAL_HEART.value())
                .item(ModItems.CLOUD_IN_A_BOTTLE.value());
        builder(BuiltInLootTables.JUNGLE_TEMPLE, 0.35F)
                .item(ModItems.KITTY_SLIPPERS.value(), 2)
                .item(ModItems.ROOTED_BOOTS.value(), 2)
                .item(ModItems.BUNNY_HOPPERS.value())
                .item(ModItems.ANGLERS_HAT.value());
        builder(BuiltInLootTables.NETHER_BRIDGE, 0.15F)
                .item(ModItems.CROSS_NECKLACE.value())
                .item(ModItems.NIGHT_VISION_GOGGLES.value())
                .item(ModItems.POCKET_PISTON.value())
                .item(ModItems.RUNNING_SHOES.value())
                .item(ModItems.COWBOY_HAT.value())
                .item(ModItems.CHARM_OF_SHRINKING.value())
                .item(ModItems.STRIDER_SHOES.value())
                .item(ModItems.WITHERED_BRACELET.value())
                .drinkingHat(1);
        builder(BuiltInLootTables.PILLAGER_OUTPOST, 0.25F)
                .item(ModItems.PANIC_NECKLACE.value())
                .item(ModItems.POCKET_PISTON.value())
                .item(ModItems.STEADFAST_SPIKES.value())
                .item(ModItems.POWER_GLOVE.value())
                .item(ModItems.CROSS_NECKLACE.value())
                .item(ModItems.SCARF_OF_INVISIBILITY.value())
                .item(ModItems.CRYSTAL_HEART.value())
                .item(ModItems.CLOUD_IN_A_BOTTLE.value())
                .item(ModItems.SUPERSTITIOUS_HAT.value())
                .item(ModItems.ROOTED_BOOTS.value());
        builder(BuiltInLootTables.RUINED_PORTAL, 0.15F)
                .item(ModItems.NIGHT_VISION_GOGGLES.value())
                .item(ModItems.THORN_PENDANT.value())
                .item(ModItems.FIRE_GAUNTLET.value())
                .item(ModItems.POWER_GLOVE.value())
                .item(ModItems.UNIVERSAL_ATTRACTOR.value())
                .item(ModItems.OBSIDIAN_SKULL.value())
                .item(ModItems.LUCKY_SCARF.value())
                .item(ModItems.COWBOY_HAT.value())
                .item(ModItems.CHARM_OF_SHRINKING.value())
                .item(ModItems.STRIDER_SHOES.value());
        builder(BuiltInLootTables.SHIPWRECK_TREASURE, 0.15F)
                .item(ModItems.GOLDEN_HOOK.value(), 3)
                .item(ModItems.SNORKEL.value())
                .item(ModItems.FLIPPERS.value())
                .item(ModItems.SCARF_OF_INVISIBILITY.value())
                .item(ModItems.STEADFAST_SPIKES.value())
                .item(ModItems.UNIVERSAL_ATTRACTOR.value())
                .item(ModItems.FERAL_CLAWS.value())
                .item(ModItems.NIGHT_VISION_GOGGLES.value())
                .item(ModItems.OBSIDIAN_SKULL.value())
                .item(ModItems.RUNNING_SHOES.value())
                .item(ModItems.CHARM_OF_SINKING.value());
        builder(BuiltInLootTables.SIMPLE_DUNGEON, 0.15F)
                .item(ModItems.CHARM_OF_SHRINKING.value())
                .item(ModItems.WARP_DRIVE.value())
                .item(ModItems.WITHERED_BRACELET.value())
                .item(ModItems.STRIDER_SHOES.value());
        builder(BuiltInLootTables.STRONGHOLD_CORRIDOR, 0.3F)
                .item(ModItems.POWER_GLOVE.value())
                .item(ModItems.ANTIDOTE_VESSEL.value())
                .item(ModItems.SUPERSTITIOUS_HAT.value())
                .item(ModItems.LUCKY_SCARF.value())
                .item(ModItems.AQUA_DASHERS.value())
                .item(ModItems.HELIUM_FLAMINGO.value())
                .item(ModItems.ROOTED_BOOTS.value())
                .item(ModItems.PICKAXE_HEATER.value());
        builder(BuiltInLootTables.UNDERWATER_RUIN_BIG, 0.45F)
                .item(ModItems.SNORKEL.value(), 3)
                .item(ModItems.FLIPPERS.value(), 3)
                .item(ModItems.SUPERSTITIOUS_HAT.value())
                .item(ModItems.LUCKY_SCARF.value())
                .item(ModItems.FIRE_GAUNTLET.value())
                .item(ModItems.CROSS_NECKLACE.value())
                .item(ModItems.POWER_GLOVE.value())
                .item(ModItems.CLOUD_IN_A_BOTTLE.value())
                .item(ModItems.ANGLERS_HAT.value());
        builder(BuiltInLootTables.WOODLAND_MANSION, 0.4F)
                .item(ModItems.CHORUS_TOTEM.value(), 2)
                .artifact(1);
        builder(BuiltInLootTables.IGLOO_CHEST, 0.3F)
                .item(ModItems.SNOWSHOES.value(), 2)
                .item(ModItems.VILLAGER_HAT.value())
                .item(ModItems.LUCKY_SCARF.value());
        builder(BuiltInLootTables.ANCIENT_CITY_ICE_BOX, 0.2F)
                .item(ModItems.SNOWSHOES.value());
        builder(BuiltInLootTables.ANCIENT_CITY, 0.15F)
                .item(ModItems.ROOTED_BOOTS.value())
                .item(ModItems.PICKAXE_HEATER.value())
                .item(ModItems.ONION_RING.value())
                .item(ModItems.AQUA_DASHERS.value())
                .item(ModItems.CHARM_OF_SINKING.value())
                .item(ModItems.SHOCK_PENDANT.value())
                .item(ModItems.HELIUM_FLAMINGO.value())
                .item(ModItems.WARP_DRIVE.value());

        archaeologyBuilder(BuiltInLootTables.DESERT_PYRAMID_ARCHAEOLOGY)
                .item(ModItems.COWBOY_HAT.value())
                .item(ModItems.OBSIDIAN_SKULL.value())
                .item(ModItems.SNORKEL.value())
                .item(ModItems.POWER_GLOVE.value())
                .item(ModItems.KITTY_SLIPPERS.value())
                .item(ModItems.NIGHT_VISION_GOGGLES.value())
                .item(ModItems.SHOCK_PENDANT.value());
        archaeologyBuilder(BuiltInLootTables.DESERT_WELL_ARCHAEOLOGY)
                .item(ModItems.CHARM_OF_SINKING.value())
                .item(ModItems.UNIVERSAL_ATTRACTOR.value())
                .item(ModItems.SUPERSTITIOUS_HAT.value())
                .item(ModItems.UMBRELLA.value())
                .item(ModItems.WARP_DRIVE.value())
                .drinkingHat(1);
        archaeologyBuilder(BuiltInLootTables.OCEAN_RUIN_COLD_ARCHAEOLOGY)
                .item(ModItems.LUCKY_SCARF.value())
                .item(ModItems.FIRE_GAUNTLET.value())
                .item(ModItems.ANGLERS_HAT.value())
                .item(ModItems.DIGGING_CLAWS.value())
                .item(ModItems.ANTIDOTE_VESSEL.value())
                .item(ModItems.WITHERED_BRACELET.value());
        archaeologyBuilder(BuiltInLootTables.OCEAN_RUIN_WARM_ARCHAEOLOGY)
                .item(ModItems.AQUA_DASHERS.value())
                .item(ModItems.ONION_RING.value())
                .item(ModItems.RUNNING_SHOES.value())
                .item(ModItems.BUNNY_HOPPERS.value())
                .item(ModItems.VAMPIRIC_GLOVE.value())
                .item(ModItems.STRIDER_SHOES.value());
        archaeologyBuilder(BuiltInLootTables.TRAIL_RUINS_ARCHAEOLOGY_RARE)
                .item(ModItems.ROOTED_BOOTS.value())
                .item(ModItems.PICKAXE_HEATER.value())
                .item(ModItems.AQUA_DASHERS.value())
                .item(ModItems.SNOWSHOES.value())
                .item(ModItems.STEADFAST_SPIKES.value())
                .item(ModItems.VILLAGER_HAT.value())
                .item(ModItems.CLOUD_IN_A_BOTTLE.value())
                .item(ModItems.FERAL_CLAWS.value())
                .item(ModItems.POCKET_PISTON.value())
                .item(ModItems.WHOOPEE_CUSHION.value())
                .item(ModItems.FLAME_PENDANT.value())
                .item(ModItems.THORN_PENDANT.value())
                .item(ModItems.CHARM_OF_SHRINKING.value());
    }

    protected Builder builder(ResourceKey<LootTable> lootTable, float baseChance) {
        if (!ModLootTables.INJECTED_LOOT_TABLES.contains(lootTable)) {
            throw new IllegalArgumentException("Missing injected loot table: %s".formatted(lootTable));
        }
        Builder builder = new Builder(lootTable);
        builder.lootPoolCondition(ArtifactRarityAdjustedChance.adjustedChance(baseChance));
        builder.lootModifierCondition(LootTableIdCondition.builder(lootTable.location()));
        lootBuilders.add(builder);
        return builder;
    }

    protected Builder archaeologyBuilder(ResourceKey<LootTable> lootTable) {
        if (!ModLootTables.ARCHAEOLOGY_LOOT_TABLES.contains(lootTable)) {
            throw new IllegalArgumentException("Missing archaeology loot table: %s".formatted(lootTable));
        }
        Builder builder = new Builder(lootTable).replace();
        builder.lootModifierCondition(LootTableIdCondition.builder(lootTable.location()));
        builder.lootModifierCondition(ConfigValueChance.archaeologyChance());
        lootBuilders.add(builder);
        return builder;
    }

    protected void start() {
        addLoot();

        for (Builder lootBuilder : lootBuilders) {
            add("inject/" + lootBuilder.getName(), lootBuilder.build());
        }
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cache) {
        start();

        Path neoForgePath = this.packOutput.getOutputFolder(PackOutput.Target.DATA_PACK).resolve("neoforge").resolve("loot_modifiers").resolve("global_loot_modifiers.json");
        Path modifierFolderPath = this.packOutput.getOutputFolder(PackOutput.Target.DATA_PACK).resolve(Artifacts.MOD_ID).resolve("loot_modifiers");
        List<ResourceLocation> entries = new ArrayList<>();

        ImmutableList.Builder<CompletableFuture<?>> futuresBuilder = new ImmutableList.Builder<>();

        toSerialize.forEach((name, json) -> {
            entries.add(Artifacts.id(name));
            Path modifierPath = modifierFolderPath.resolve(name + ".json");
            futuresBuilder.add(DataProvider.saveStable(cache, json, modifierPath));
        });

        JsonObject forgeJson = new JsonObject();
        forgeJson.addProperty("replace", false);
        forgeJson.add("entries", GSON.toJsonTree(entries.stream().map(ResourceLocation::toString).collect(Collectors.toList())));

        JsonArray conditions = new JsonArray();
        JsonObject condition = new JsonObject();
        JsonObject modsLoaded = new JsonObject();
        conditions.add(condition);
        condition.addProperty("condition", "fabric:not");
        condition.add("value", modsLoaded);
        modsLoaded.addProperty("condition", "fabric:all_mods_loaded");
        modsLoaded.add("values", new JsonArray());
        forgeJson.add("fabric:load_conditions", conditions);

        futuresBuilder.add(DataProvider.saveStable(cache, forgeJson, neoForgePath));

        return CompletableFuture.allOf(futuresBuilder.build().toArray(CompletableFuture[]::new));
    }

    public <T extends IGlobalLootModifier> void add(String modifier, T instance) {
        JsonElement json = IGlobalLootModifier.DIRECT_CODEC.encodeStart(JsonOps.INSTANCE, instance).getOrThrow();
        this.toSerialize.put(modifier, json);
    }

    @Override
    public String getName() {
        return "Global Loot Modifiers : " + Artifacts.MOD_ID;
    }

    @SuppressWarnings({"UnusedReturnValue", "SameParameterValue"})
    protected static class Builder {

        private final ResourceKey<LootTable> lootTable;
        private final LootPool.Builder lootPool = LootPool.lootPool();
        private final List<LootItemCondition> conditions;
        private boolean replace = false;

        private LootContextParamSet paramSet = LootContextParamSets.CHEST;

        private Builder(ResourceKey<LootTable> lootTable) {
            this.lootTable = lootTable;
            this.conditions = new ArrayList<>();
        }

        private RollLootTableModifier build() {
            return new RollLootTableModifier(conditions.toArray(new LootItemCondition[]{}), Artifacts.key(Registries.LOOT_TABLE, "inject/" + lootTable.location().getPath()), replace);
        }

        protected LootTable.Builder createLootTable() {
            return new LootTable.Builder().withPool(lootPool);
        }

        public LootContextParamSet getParameterSet() {
            return paramSet;
        }

        protected String getName() {
            return lootTable.location().getPath();
        }

        private Builder parameterSet(LootContextParamSet paramSet) {
            this.paramSet = paramSet;
            return this;
        }

        public Builder replace() {
            this.replace = true;
            return this;
        }

        private Builder lootPoolCondition(LootItemCondition.Builder condition) {
            lootPool.when(condition);
            return this;
        }

        private Builder lootModifierCondition(LootItemCondition.Builder condition) {
            conditions.add(condition.build());
            return this;
        }

        private Builder item(Item item, int weight) {
            lootPool.add(LootTables.item(item, weight));
            return this;
        }

        private Builder item(Item item) {
            return item(item, 1);
        }

        private Builder artifact(int weight) {
            lootPool.add(LootTables.artifact(weight));
            return this;
        }

        private Builder drinkingHat(int weight) {
            lootPool.add(LootTables.drinkingHat(weight));
            return this;
        }

        private Builder everlastingBeef() {
            lootPool.add(LootTables.item(ModItems.EVERLASTING_BEEF.value(), 1)
                    .apply(
                            SmeltItemFunction.smelted().when(
                                    LootItemEntityPropertyCondition.hasProperties(
                                            LootContext.EntityTarget.THIS,
                                            EntityPredicate.Builder.entity().flags(
                                                    EntityFlagsPredicate.Builder.flags()
                                                            .setOnFire(true)
                                            )
                                    )
                            )
                    )
            );
            return this;
        }
    }
}
