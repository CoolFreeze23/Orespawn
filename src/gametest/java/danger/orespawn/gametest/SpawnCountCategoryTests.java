package danger.orespawn.gametest;

import danger.orespawn.ModEntities;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.world.ModBiomeModifiers;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ambient.AmbientCreature;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LocalMobCapCalculator;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/**
 * Which cap a natural spawn fills. A spawn pass draws from the list of its category and stops when that category's
 * count reaches its cap, but 1.21.1 counts a mob toward its type's {@link MobCategory} alone
 * ({@code NaturalSpawner.createState}, {@code SpawnState.afterSpawn}), where 1.7.10 counted it toward every creature
 * type whose class it extends ({@code World.countEntities(EnumCreatureType, true)}). A species the ambient pass draws
 * that does not count as ambient never brings that pass to its cap. The Ghost and the Ghost Skelly, ambient creatures
 * in 1.7.10 (EntityAmbientCreature) and listed in ambient lists only, count as ambient again.
 */
@GameTestHolder(OreSpawnMod.MOD_ID)
@PrefixGameTestTemplate(false)
public class SpawnCountCategoryTests {

    /** The Ghost and the Ghost Skelly fill the ambient cap and leave the monster cap alone. */
    @GameTest(template = "empty")
    public static void spawncount_a_ghosts_fill_the_ambient_cap(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        helper.assertTrue(ModEntities.GHOST.get().getCategory() == MobCategory.AMBIENT, "ghost is not AMBIENT");
        helper.assertTrue(ModEntities.GHOST_SKELLY.get().getCategory() == MobCategory.AMBIENT, "ghost_skelly is not AMBIENT");

        BlockPos at = helper.absolutePos(new BlockPos(1, 2, 1));
        List<Entity> mobs = new ArrayList<>();
        for (EntityType<?> type : List.of(ModEntities.GHOST.get(), ModEntities.GHOST_SKELLY.get(),
                ModEntities.GHOST.get())) {
            Entity mob = type.create(level);
            helper.assertTrue(mob != null, "could not create " + type.toShortString());
            mob.moveTo(at.getX() + 0.5, at.getY(), at.getZ() + 0.5, 0.0f, 0.0f);
            level.addFreshEntity(mob);
            mobs.add(mob);
        }
        // the spawner's own count, over these three alone: what the next tick's spawn passes would read
        NaturalSpawner.SpawnState state = NaturalSpawner.createState(1, mobs,
                (chunk, consumer) -> consumer.accept(level.getChunk(ChunkPos.getX(chunk), ChunkPos.getZ(chunk))),
                new LocalMobCapCalculator(level.getChunkSource().chunkMap));
        int ambient = state.getMobCategoryCounts().getInt(MobCategory.AMBIENT);
        int monster = state.getMobCategoryCounts().getInt(MobCategory.MONSTER);
        mobs.forEach(Entity::discard);
        helper.assertValueEqual(ambient, 3, "Ghosts counted toward the ambient cap");
        helper.assertValueEqual(monster, 0, "Ghosts counted toward the monster cap");
        helper.succeed();
    }

    /**
     * The rule over every list the mod ships: a species whose class is an ambient creature and that only ambient lists
     * carry, on any day the tables know (every {@code when} of original_spawns.json and extra_spawns.json) and in the
     * mod's own biomes, counts as ambient, so the pass that spawns it is the cap it fills.
     */
    @GameTest(template = "empty")
    public static void spawncount_b_ambient_only_species_count_as_ambient(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        Map<EntityType<?>, Set<MobCategory>> listed = new java.util.HashMap<>();
        for (String file : List.of("original_spawns.json", "extra_spawns.json")) {
            for (ModBiomeModifiers.SpawnTable.Entry entry : SpawnTableTests.table(helper, file).entries()) {
                for (MobSpawnSettings.SpawnerData data : entry.spawners()) {
                    listed.computeIfAbsent(data.type, t -> new TreeSet<>()).add(entry.category());
                }
            }
        }
        for (var biome : level.registryAccess().registryOrThrow(Registries.BIOME).entrySet()) {
            if (!biome.getKey().location().getNamespace().equals(OreSpawnMod.MOD_ID)) continue;
            Biome value = biome.getValue();
            for (MobCategory category : MobCategory.values()) {
                for (MobSpawnSettings.SpawnerData data : value.getMobSettings().getMobs(category).unwrap()) {
                    listed.computeIfAbsent(data.type, t -> new TreeSet<>()).add(category);
                }
            }
        }
        Map<String, String> bad = new TreeMap<>();
        Set<String> checked = new TreeSet<>();
        for (var e : listed.entrySet()) {
            EntityType<?> type = e.getKey();
            ResourceLocation id = BuiltInRegistries.ENTITY_TYPE.getKey(type);
            if (!id.getNamespace().equals(OreSpawnMod.MOD_ID) || !e.getValue().equals(Set.of(MobCategory.AMBIENT))) continue;
            Entity probe = type.create(level);
            boolean ambientClass = probe instanceof AmbientCreature;
            if (probe != null) probe.discard();
            if (!ambientClass) continue;
            checked.add(id.toString());
            if (type.getCategory() != MobCategory.AMBIENT) bad.put(id.toString(), type.getCategory().getName());
        }
        // the Butterfly, the Luna Moth, the Firefly, the Fairy, the Mosquito, the Ghost and the Ghost Skelly
        helper.assertTrue(checked.size() >= 7, "found too few ambient-only ambient creatures: " + checked);
        helper.assertTrue(bad.isEmpty(), "ambient-only ambient creatures that count toward another cap: " + bad);
        helper.succeed();
    }
}
