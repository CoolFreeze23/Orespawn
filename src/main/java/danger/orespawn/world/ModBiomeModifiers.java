package danger.orespawn.world;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import danger.orespawn.OreSpawnMod;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.world.BiomeModifier;
import net.neoforged.neoforge.common.world.ModifiableBiomeInfo;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

/**
 * The mod's own biome modifier serializers.
 *
 * <p>{@link AddSpawnsInCategory} (JSON type {@code orespawn:add_spawns_in_category}) adds spawn entries to a NAMED spawn
 * list. NeoForge's {@code neoforge:add_spawns} files every entry under the entity type's own {@code MobCategory}, which
 * cannot express a 1.7.10 registration that put one species into a list of another type: the Frog's river and swamp
 * entries were {@code EnumCreatureType.waterCreature} (orig OreSpawnMain.java:4963, :4966) and its river, jungle and
 * swamp entries {@code ambient} (:4964, :4965, :4967) while the Frog itself counts as a creature (ENT-S-170). The list
 * an entry sits in decides the pass that spawns it, that pass's cap and — through {@code FrogSpawnPlacement} — whether
 * it is placed in water or on the ground.
 */
public final class ModBiomeModifiers {
    public static final DeferredRegister<MapCodec<? extends BiomeModifier>> SERIALIZERS =
            DeferredRegister.create(NeoForgeRegistries.Keys.BIOME_MODIFIER_SERIALIZERS, OreSpawnMod.MOD_ID);

    public static final DeferredHolder<MapCodec<? extends BiomeModifier>, MapCodec<AddSpawnsInCategory>> ADD_SPAWNS_IN_CATEGORY =
            SERIALIZERS.register("add_spawns_in_category", () -> RecordCodecBuilder.mapCodec(inst -> inst.group(
                    Biome.LIST_CODEC.fieldOf("biomes").forGetter(AddSpawnsInCategory::biomes),
                    MobCategory.CODEC.fieldOf("category").forGetter(AddSpawnsInCategory::category),
                    MobSpawnSettings.SpawnerData.CODEC.listOf().fieldOf("spawners").forGetter(AddSpawnsInCategory::spawners)
            ).apply(inst, AddSpawnsInCategory::new)));

    private ModBiomeModifiers() {
    }

    public static void register(IEventBus eventBus) {
        SERIALIZERS.register(eventBus);
    }

    /** Adds {@code spawners} to the {@code category} list of every biome in {@code biomes}, whatever the entities' own categories. */
    public record AddSpawnsInCategory(HolderSet<Biome> biomes, MobCategory category,
                                      List<MobSpawnSettings.SpawnerData> spawners) implements BiomeModifier {
        @Override
        public void modify(Holder<Biome> biome, Phase phase, ModifiableBiomeInfo.BiomeInfo.Builder builder) {
            if (phase == Phase.ADD && biomes.contains(biome)) {
                for (MobSpawnSettings.SpawnerData spawner : spawners) {
                    builder.getMobSpawnSettings().addSpawn(category, spawner);
                }
            }
        }

        @Override
        public MapCodec<? extends BiomeModifier> codec() {
            return ADD_SPAWNS_IN_CATEGORY.get();
        }
    }
}
