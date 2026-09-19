package danger.orespawn.gametest;

import danger.orespawn.ModEntities;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.FrogSpawnPlacement;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/**
 * ENT-S-170: the Frog's spawn placement follows the list the biome carries it in (orig
 * SpawnerAnimals.canCreatureTypeSpawnAtLocation): two-deep water where it is a water creature, the ground rule where
 * an ambient or creature list carries it. Pins the registration, the shipped biome lists and the per-list rule.
 */
@GameTestHolder(OreSpawnMod.MOD_ID)
@PrefixGameTestTemplate(false)
public class FrogSpawnPlacementTests {

    @GameTest(template = "empty")
    public static void s170a_frog_placement_type_is_the_list_aware_one(GameTestHelper helper) {
        helper.assertTrue(SpawnPlacements.getPlacementType(ModEntities.FROG.get()) == FrogSpawnPlacement.INSTANCE,
                "the Frog's spawn placement type is not FrogSpawnPlacement (ENT-S-170)");
        helper.succeed();
    }

    /**
     * The lists as the port ships them: Utopia, the Village and the Crystal plains carry the Frog as a water creature
     * and nowhere else (orig BiomeGenUtopianPlains.java:135; the Crystal w1 3-5); the river and the swamp carry it in
     * both their water and their ambient lists, the jungle in its ambient list only (orig OreSpawnMain.java:4963-4967,
     * the companion modifiers through orespawn:add_spawns_in_category).
     */
    @GameTest(template = "empty")
    public static void s170b_biome_lists_carry_the_frog_as_the_original_did(GameTestHelper helper) {
        Registry<Biome> biomes = helper.getLevel().registryAccess().registryOrThrow(Registries.BIOME);
        EntityType<?> frog = ModEntities.FROG.get();
        for (String id : new String[] {"orespawn:utopia_plains", "orespawn:village_biome", "orespawn:crystal_plains"}) {
            Biome biome = biomes.get(ResourceLocation.parse(id));
            helper.assertTrue(biome != null, id + " is not registered");
            MobSpawnSettings settings = biome.getMobSettings();
            helper.assertTrue(FrogSpawnPlacement.listedAsWater(settings, frog),
                    id + " should carry the Frog in its water list");
            helper.assertTrue(!FrogSpawnPlacement.listedOnGround(settings, frog),
                    id + " should carry the Frog in no ground list");
        }
        for (String id : new String[] {"river", "swamp"}) {
            Biome biome = biomes.get(ResourceLocation.withDefaultNamespace(id));
            helper.assertTrue(biome != null, "minecraft:" + id + " is not registered");
            helper.assertTrue(FrogSpawnPlacement.listedAsWater(biome.getMobSettings(), frog),
                    "minecraft:" + id + " should carry the Frog in its water list (orig OreSpawnMain.java:4963 / :4966)");
            helper.assertTrue(FrogSpawnPlacement.listedOnGround(biome.getMobSettings(), frog),
                    "minecraft:" + id + " should carry the Frog in its ambient list (orig :4964 / :4967)");
        }
        Biome jungle = biomes.get(ResourceLocation.withDefaultNamespace("jungle"));
        helper.assertTrue(jungle != null, "minecraft:jungle is not registered");
        helper.assertTrue(FrogSpawnPlacement.listedOnGround(jungle.getMobSettings(), frog)
                        && !FrogSpawnPlacement.listedAsWater(jungle.getMobSettings(), frog),
                "minecraft:jungle should carry the Frog in its ambient list only (orig :4965)");
        helper.succeed();
    }

    /**
     * The per-list rule: a water-only listing refuses dry ground and one-deep water and accepts two-deep water; a
     * ground listing the reverse; a listing in both takes either; a biome that lists the Frog nowhere keeps the
     * ground rule.
     */
    @GameTest(template = "empty")
    public static void s170c_water_listed_frog_needs_two_deep_water(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        EntityType<?> frog = ModEntities.FROG.get();
        helper.setBlock(new BlockPos(2, 1, 2), Blocks.STONE);
        BlockPos ground = helper.absolutePos(new BlockPos(2, 2, 2));
        helper.setBlock(new BlockPos(5, 0, 5), Blocks.STONE);
        helper.setBlock(new BlockPos(5, 1, 5), Blocks.WATER);
        helper.setBlock(new BlockPos(5, 2, 5), Blocks.WATER);
        BlockPos water = helper.absolutePos(new BlockPos(5, 2, 5));
        helper.setBlock(new BlockPos(7, 1, 7), Blocks.STONE);
        helper.setBlock(new BlockPos(7, 2, 7), Blocks.WATER);
        BlockPos shallow = helper.absolutePos(new BlockPos(7, 2, 7));

        helper.assertTrue(!FrogSpawnPlacement.isSpawnPositionOk(level, ground, frog, true, false),
                "a water-listed Frog took dry ground");
        helper.assertTrue(!FrogSpawnPlacement.isSpawnPositionOk(level, shallow, frog, true, false),
                "a water-listed Frog took one-deep water (orig: liquid at the cell AND below it)");
        helper.assertTrue(FrogSpawnPlacement.isSpawnPositionOk(level, water, frog, true, false),
                "a water-listed Frog refused two-deep water");
        helper.assertTrue(FrogSpawnPlacement.isSpawnPositionOk(level, ground, frog, false, true),
                "a ground-listed Frog refused dry ground");
        helper.assertTrue(!FrogSpawnPlacement.isSpawnPositionOk(level, water, frog, false, true),
                "a ground-listed Frog took water");
        helper.assertTrue(FrogSpawnPlacement.isSpawnPositionOk(level, ground, frog, true, true)
                        && FrogSpawnPlacement.isSpawnPositionOk(level, water, frog, true, true),
                "a Frog listed in both refused one of its two placements");
        helper.assertTrue(FrogSpawnPlacement.isSpawnPositionOk(level, ground, frog, false, false),
                "an unlisted Frog lost the ground rule");
        helper.succeed();
    }
}
