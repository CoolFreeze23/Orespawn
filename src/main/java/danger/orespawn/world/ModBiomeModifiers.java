package danger.orespawn.world;

import danger.orespawn.ModSpawnControl;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import danger.orespawn.OreSpawnConfig;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.util.SeasonalDates;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.world.BiomeModifier;
import net.neoforged.neoforge.common.world.ModifiableBiomeInfo;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

/**
 * The mod's own biome modifier serializers.
 *
 * <p>{@link AddSpawnsInCategory} (JSON type {@code orespawn:add_spawns_in_category}) adds spawn entries to a NAMED spawn
 * list. NeoForge's {@code neoforge:add_spawns} files every entry under the entity type's own {@code MobCategory}, which
 * cannot express a 1.7.10 registration that put one species into a list of another type: the Frog's river and swamp
 * entries were {@code EnumCreatureType.waterCreature} (orig OreSpawnMain.java:4963, :4966) and its river, jungle and
 * swamp entries {@code ambient} (:4964, :4965, :4967) while the Frog itself counts as a creature (ENT-S-170). The list
 * an entry sits in decides the pass that spawns it, that pass's cap and — through {@code ListedSpawnPlacement} — whether
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

    /**
     * JSON type {@code orespawn:chaos_red_colours}: the port's red Chaos (WGEN-106), its sky, fog, water, water fog, grass and
     * foliage colours set on {@code biomes} while {@code modern.chaosRed} is on; off, the biome keeps the original's.
     */
    public static final DeferredHolder<MapCodec<? extends BiomeModifier>, MapCodec<ChaosRedColours>> CHAOS_RED_COLOURS =
            SERIALIZERS.register("chaos_red_colours", () -> RecordCodecBuilder.mapCodec(inst -> inst.group(
                    Biome.LIST_CODEC.fieldOf("biomes").forGetter(ChaosRedColours::biomes),
                    com.mojang.serialization.Codec.INT.fieldOf("sky_color").forGetter(ChaosRedColours::sky),
                    com.mojang.serialization.Codec.INT.fieldOf("fog_color").forGetter(ChaosRedColours::fog),
                    com.mojang.serialization.Codec.INT.fieldOf("water_color").forGetter(ChaosRedColours::water),
                    com.mojang.serialization.Codec.INT.fieldOf("water_fog_color").forGetter(ChaosRedColours::waterFog),
                    com.mojang.serialization.Codec.INT.fieldOf("grass_color").forGetter(ChaosRedColours::grass),
                    com.mojang.serialization.Codec.INT.fieldOf("foliage_color").forGetter(ChaosRedColours::foliage)
            ).apply(inst, ChaosRedColours::new)));

    /**
     * JSON type {@code orespawn:spawn_table} (WGEN-086): spawn entries in named lists, many to a file, each with its
     * biomes, its list ({@code category}), its spawners and when it applies ({@code when}, {@code always} if left out).
     */
    public static final DeferredHolder<MapCodec<? extends BiomeModifier>, MapCodec<SpawnTable>> SPAWN_TABLE =
            SERIALIZERS.register("spawn_table", () -> RecordCodecBuilder.mapCodec(inst -> inst.group(
                    SpawnTable.Entry.CODEC.listOf().fieldOf("entries").forGetter(SpawnTable::entries)
            ).apply(inst, SpawnTable::new)));

    /** JSON type {@code orespawn:remove_disabled_spawns}: no fields, every biome. */
    public static final DeferredHolder<MapCodec<? extends BiomeModifier>, MapCodec<RemoveDisabledSpawns>> REMOVE_DISABLED_SPAWNS =
            SERIALIZERS.register("remove_disabled_spawns", () -> MapCodec.unit(RemoveDisabledSpawns.INSTANCE));

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

    /**
     * The overworld's and the Nether's spawn lists as the original's (original_spawns.json: its EntityRegistry.addSpawn
     * calls, OreSpawnMain.java:4500-5000, replayed as Forge 10.13.4.1614 ran them; and the dated blocks' additions to
     * Mining, whose biome was Extreme Hills), and the port's own overworld spawns behind {@code [modern] extraSpawns}
     * (extra_spawns.json). Every entry goes into its named list, whatever its entity's own category, as the original
     * listed it. The day's conditions are read once as the server starts ({@link #today}), as the original read its
     * calendar once at load (OreSpawnMain.java:4518-4571).
     */
    public record SpawnTable(List<Entry> entries) implements BiomeModifier {
        /** The conditions of the day a server started: the dates' and the {@code extraSpawns} option's. */
        public record Today(boolean halloween, boolean easter, boolean extraSpawns) {
        }

        private static MinecraftServer todayServer;
        private static Today today;

        /**
         * The conditions of the running server's start, read on the first call for it (the first biome it modifies), so
         * every list of one start takes the same day even across midnight.
         */
        public static synchronized Today today() {
            MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
            if (today == null || server != todayServer) {
                todayServer = server;
                today = new Today(SeasonalDates.isHalloween(), SeasonalDates.isEaster(), OreSpawnConfig.extraSpawns());
            }
            return today;
        }

        /** One entry: its biomes, its list, its spawners and when it applies. */
        public record Entry(HolderSet<Biome> biomes, MobCategory category, List<MobSpawnSettings.SpawnerData> spawners,
                            When when) {
            static final Codec<Entry> CODEC = RecordCodecBuilder.create(inst -> inst.group(
                    Biome.LIST_CODEC.fieldOf("biomes").forGetter(Entry::biomes),
                    MobCategory.CODEC.fieldOf("category").forGetter(Entry::category),
                    MobSpawnSettings.SpawnerData.CODEC.listOf().fieldOf("spawners").forGetter(Entry::spawners),
                    When.CODEC.optionalFieldOf("when", When.ALWAYS).forGetter(Entry::when)
            ).apply(inst, Entry::new));
        }

        /**
         * When an entry applies: {@code always}; on 31 October ({@code halloween}) or 20 April ({@code easter}), the
         * original's dated blocks, or on any other day ({@code ordinary}); or while {@code [modern] extraSpawns} is on
         * ({@code extra_spawns}).
         */
        public enum When implements StringRepresentable {
            ALWAYS("always"), ORDINARY("ordinary"), HALLOWEEN("halloween"), EASTER("easter"), EXTRA_SPAWNS("extra_spawns");

            static final Codec<When> CODEC = StringRepresentable.fromEnum(When::values);
            private final String name;

            When(String name) {
                this.name = name;
            }

            @Override
            public String getSerializedName() {
                return this.name;
            }

            /** Whether the entry applies on the given day. */
            public boolean holdsOn(Today day) {
                return switch (this) {
                    case ALWAYS -> true;
                    case ORDINARY -> !day.halloween() && !day.easter();
                    case HALLOWEEN -> day.halloween();
                    case EASTER -> day.easter();
                    case EXTRA_SPAWNS -> day.extraSpawns();
                };
            }

            /** Whether the entry applies now (the clock and the config read at this call). */
            public boolean holds() {
                return holdsOn(new Today(SeasonalDates.isHalloween(), SeasonalDates.isEaster(), OreSpawnConfig.extraSpawns()));
            }
        }

        @Override
        public void modify(Holder<Biome> biome, Phase phase, ModifiableBiomeInfo.BiomeInfo.Builder builder) {
            if (phase != Phase.ADD) return;
            Today day = today();
            for (Entry entry : this.entries) {
                if (!entry.biomes().contains(biome) || !entry.when().holdsOn(day)) continue;
                for (MobSpawnSettings.SpawnerData spawner : entry.spawners()) {
                    builder.getMobSpawnSettings().addSpawn(entry.category(), spawner);
                }
            }
        }

        @Override
        public MapCodec<? extends BiomeModifier> codec() {
            return SPAWN_TABLE.get();
        }
    }

    /** The port's red Chaos, while {@code modern.chaosRed} is on; read once, when the world starts. */
    public record ChaosRedColours(HolderSet<Biome> biomes, int sky, int fog, int water, int waterFog, int grass, int foliage)
            implements BiomeModifier {
        @Override
        public void modify(Holder<Biome> biome, Phase phase, ModifiableBiomeInfo.BiomeInfo.Builder builder) {
            if (phase != Phase.AFTER_EVERYTHING || !biomes.contains(biome) || !danger.orespawn.OreSpawnConfig.chaosRed()) return;
            builder.getSpecialEffects().skyColor(sky).fogColor(fog).waterColor(water).waterFogColor(waterFog)
                    .grassColorOverride(grass).foliageColorOverride(foliage);
        }

        @Override
        public MapCodec<? extends BiomeModifier> codec() {
            return CHAOS_RED_COLOURS.get();
        }
    }

    /**
     * Takes every disabled mob ({@link ModSpawnControl#naturalSpawnEnabled}) out of every spawn list of every biome,
     * after all the adds: the original never added a disabled mob to a list ({@code if (XEnable != 0) addSpawn(...)}),
     * so it neither spawned at chunk generation nor took a share of the list. The config is read once, at server start,
     * as the original read its own once at load.
     */
    public enum RemoveDisabledSpawns implements BiomeModifier {
        INSTANCE;

        @Override
        public void modify(Holder<Biome> biome, Phase phase, ModifiableBiomeInfo.BiomeInfo.Builder builder) {
            if (phase != Phase.REMOVE) return;
            for (MobCategory category : builder.getMobSpawnSettings().getSpawnerTypes()) {
                builder.getMobSpawnSettings().getSpawner(category)
                        .removeIf(spawner -> !ModSpawnControl.naturalSpawnEnabled(spawner.type));
            }
        }

        @Override
        public MapCodec<? extends BiomeModifier> codec() {
            return REMOVE_DISABLED_SPAWNS.get();
        }
    }
}
