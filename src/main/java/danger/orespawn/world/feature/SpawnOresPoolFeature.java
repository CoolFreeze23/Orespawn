package danger.orespawn.world.feature;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import danger.orespawn.ModBlocks;
import danger.orespawn.OreSpawnConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.TagMatchTest;

import java.util.List;
import java.util.function.Supplier;

/**
 * The original SpawnOres pool worldgen, restored line-by-line (WGEN-005).
 * Ports {@code OreSpawnWorld.generateOres}'s SpawnOres block
 * (orig OreSpawnWorld.java:355-803, overworld) and
 * {@code ChunkOreGenerator.generateOresInChunk}'s identical pass
 * (orig ChunkOreGenerator.java:21-469, OreSpawn dimensions), replacing the
 * interim Phase C7 redesign (2 boss-block features + ancient dried eggs,
 * PN-010 — now retired).
 *
 * <p>Per chunk (one {@code place} call = one original generator pass):</p>
 * <pre>
 *   patchy = 28 + nextInt(count_dice)          // rate 28: orig OreSpawnMain.java:1579
 *                                              // dice 20 overworld (OSW:356) / 30 dims (COG:22)
 *   if (nextInt(20) == 0) patchy += 30         // OSW:357-359 / COG:23-25
 *   if (LessOre) patchy /= 3                   // OSW:360-362 / COG:26-28
 *   repeat patchy times:
 *     x = 3 + nextInt(10); y = nextInt(128); z = 3 + nextInt(10)   // OSW:366-368 / COG:32-34
 *     if (y < 50 || y > 128) skip              // discard, not re-roll — OSW:369 / COG:35
 *     7-in-104 → rare pool nextInt(7)          // OSW:370-402 / COG:36-68
 *     else     → common pool nextInt(98)       // OSW:406-801 / COG:72-467
 *     vein of clump size 4                     // OSW:403,802 / COG:69,468
 * </pre>
 *
 * <p>The Mining dimension runs THREE passes when LessOre is off and one
 * (divided) pass with it on (orig ChunkProviderOreSpawn2.java:191-195) —
 * expressed as {@code passes}/{@code less_ore_passes} in the config.</p>
 *
 * <p>Mapping deltas (documented in phase_d_reports/D5_structures_spawnores.md):
 * the original replaced bare stone only; veins here use vanilla ore placement
 * against {@code #minecraft:stone_ore_replaceables}, the same treatment every
 * other restored ore received in Phase C7 (WGEN-001) — granite/diorite/
 * andesite patches did not exist in a 1.7.10 world. Ordering relative to the
 * other ores is by generation step rather than the original's
 * SpawnOres-first call order; both replace stone, so overlap resolution is
 * not player-distinguishable. Config-file overrides of rate/clump/depth are
 * hardcoded at the original defaults per the ITEM-065 DEFERRED ruling
 * (PN-013).</p>
 */
public class SpawnOresPoolFeature extends Feature<SpawnOresPoolFeature.Configuration> {

    /** orig OreSpawnMain.java:1579 — get_orestats(config, "SpawnOres", 28, 4, 50, 128). */
    private static final int RATE = 28;
    private static final int CLUMP_SIZE = 4;
    private static final int MIN_DEPTH = 50;
    private static final int MAX_DEPTH = 128;

    public record Configuration(int countDice, int passes, int lessOrePasses) implements FeatureConfiguration {
        public static final Codec<Configuration> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.INT.fieldOf("count_dice").forGetter(Configuration::countDice),
                Codec.INT.optionalFieldOf("passes", 1).forGetter(Configuration::passes),
                Codec.INT.optionalFieldOf("less_ore_passes", 1).forGetter(Configuration::lessOrePasses)
        ).apply(instance, Configuration::new));
    }

    /**
     * The 98-entry common pool in the exact {@code nextInt(98)} switch order
     * of orig OreSpawnWorld.java:406-801 (verified identical to
     * ChunkOreGenerator.java:72-467). Full row-by-row derivation:
     * phase_d_reports/d5_extraction/spawn_ores_spec.md §2.
     */
    private static final List<Supplier<Block>> COMMON_POOL = List.of(
            ModBlocks.SPIDER_SPAWN_BLOCK,            // c0 — orig Spider
            ModBlocks.BAT_SPAWN_BLOCK,               // c1 — orig Bat
            ModBlocks.COW_SPAWN_BLOCK,               // c2 — orig Cow
            ModBlocks.PIG_SPAWN_BLOCK,               // c3 — orig Pig
            ModBlocks.SQUID_SPAWN_BLOCK,             // c4 — orig Squid
            ModBlocks.CHICKEN_SPAWN_BLOCK,           // c5 — orig Chicken
            ModBlocks.CREEPER_SPAWN_BLOCK,           // c6 — orig Creeper
            ModBlocks.SKELETON_SPAWN_BLOCK,          // c7 — orig Skeleton
            ModBlocks.ZOMBIE_SPAWN_BLOCK,            // c8 — orig Zombie
            ModBlocks.SLIME_SPAWN_BLOCK,             // c9 — orig Slime
            ModBlocks.GHAST_SPAWN_BLOCK,             // c10 — orig Ghast
            ModBlocks.ZOMBIFIED_PIGLIN_SPAWN_BLOCK,  // c11 — orig ZombiePigman
            ModBlocks.ENDERMAN_SPAWN_BLOCK,          // c12 — orig Enderman
            ModBlocks.CAVE_SPIDER_SPAWN_BLOCK,       // c13 — orig CaveSpider
            ModBlocks.SILVERFISH_SPAWN_BLOCK,        // c14 — orig Silverfish
            ModBlocks.MAGMA_CUBE_SPAWN_BLOCK,        // c15 — orig MagmaCube
            ModBlocks.WITCH_SPAWN_BLOCK,             // c16 — orig Witch
            ModBlocks.SHEEP_SPAWN_BLOCK,             // c17 — orig Sheep
            ModBlocks.WOLF_SPAWN_BLOCK,              // c18 — orig Wolf
            ModBlocks.MOOSHROOM_SPAWN_BLOCK,         // c19 — orig Mooshroom
            ModBlocks.OCELOT_SPAWN_BLOCK,            // c20 — orig Ocelot
            ModBlocks.BLAZE_SPAWN_BLOCK,             // c21 — orig Blaze
            ModBlocks.WITHER_SKELETON_SPAWN_BLOCK,   // c22 — orig WitherSkeleton
            ModBlocks.ENDER_DRAGON_SPAWN_BLOCK,      // c23 — orig EnderDragon
            ModBlocks.SNOW_GOLEM_SPAWN_BLOCK,        // c24 — orig SnowGolem
            ModBlocks.IRON_GOLEM_SPAWN_BLOCK,        // c25 — orig IronGolem
            ModBlocks.WITHER_SPAWN_BLOCK,            // c26 — orig WitherBoss
            ModBlocks.GIRLFRIEND_SPAWN_BLOCK,        // c27 — orig Girlfriend
            ModBlocks.RED_COW_SPAWN_BLOCK,           // c28 — orig RedCow
            ModBlocks.GOLD_COW_SPAWN_BLOCK,          // c29 — orig GoldCow
            ModBlocks.ENCHANTED_APPLE_COW_SPAWN_BLOCK, // c30 — orig EnchantedCow
            ModBlocks.MOTHRA_SPAWN_BLOCK,            // c31 — orig MOTHRA
            ModBlocks.ALOSAURUS_SPAWN_BLOCK,         // c32 — orig Alo
            ModBlocks.CRYOLOPHOSAURUS_SPAWN_BLOCK,   // c33 — orig Cryo
            ModBlocks.CAMARASAURUS_SPAWN_BLOCK,      // c34 — orig Cama
            ModBlocks.VELOCITY_RAPTOR_SPAWN_BLOCK,   // c35 — orig Velo
            ModBlocks.HYDROLISC_SPAWN_BLOCK,         // c36 — orig Hydro
            ModBlocks.BASILISK_SPAWN_BLOCK,          // c37 — orig Basil
            ModBlocks.DRAGONFLY_SPAWN_BLOCK,         // c38 — orig Dragonfly
            ModBlocks.EMPEROR_SCORPION_SPAWN_BLOCK,  // c39 — orig EmperorScorpion
            ModBlocks.SCORPION_SPAWN_BLOCK,          // c40 — orig Scorpion
            ModBlocks.CAVE_FISHER_SPAWN_BLOCK,       // c41 — orig CaveFisher
            ModBlocks.SPYRO_SPAWN_BLOCK,             // c42 — orig Spyro
            ModBlocks.BARYONYX_SPAWN_BLOCK,          // c43 — orig Baryonyx
            ModBlocks.GAMMA_METROID_SPAWN_BLOCK,     // c44 — orig GammaMetroid
            ModBlocks.COCKATEIL_SPAWN_BLOCK,         // c45 — orig Cockateil
            ModBlocks.KYUUBI_SPAWN_BLOCK,            // c46 — orig Kyuubi
            ModBlocks.ALIEN_SPAWN_BLOCK,             // c47 — orig Alien
            ModBlocks.ATTACK_SQUID_SPAWN_BLOCK,      // c48 — orig AttackSquid
            ModBlocks.WATER_DRAGON_SPAWN_BLOCK,      // c49 — orig WaterDragon
            ModBlocks.KRAKEN_SPAWN_BLOCK,            // c50 — orig Kraken
            ModBlocks.LIZARD_SPAWN_BLOCK,            // c51 — orig Lizard
            ModBlocks.CEPHADROME_SPAWN_BLOCK,        // c52 — orig Cephadrome
            ModBlocks.DRAGON_SPAWN_BLOCK,            // c53 — orig Dragon
            ModBlocks.BEE_SPAWN_BLOCK,               // c54 — orig Bee
            ModBlocks.HORSE_SPAWN_BLOCK,             // c55 — orig Horse
            ModBlocks.TROOPER_BUG_SPAWN_BLOCK,       // c56 — orig TrooperBug
            ModBlocks.SPIT_BUG_SPAWN_BLOCK,          // c57 — orig SpitBug
            ModBlocks.STINK_BUG_SPAWN_BLOCK,         // c58 — orig StinkBug
            ModBlocks.OSTRICH_SPAWN_BLOCK,           // c59 — orig Ostrich
            ModBlocks.GAZELLE_SPAWN_BLOCK,           // c60 — orig Gazelle
            ModBlocks.CHIPMUNK_SPAWN_BLOCK,          // c61 — orig Chipmunk
            ModBlocks.CREEPING_HORROR_SPAWN_BLOCK,   // c62 — orig CreepingHorror
            ModBlocks.TERRIBLE_TERROR_SPAWN_BLOCK,   // c63 — orig TerribleTerror
            ModBlocks.CLIFF_RACER_SPAWN_BLOCK,       // c64 — orig CliffRacer
            ModBlocks.TRIFFID_SPAWN_BLOCK,           // c65 — orig Triffid
            ModBlocks.PITCH_BLACK_SPAWN_BLOCK,       // c66 — orig PitchBlack
            ModBlocks.LURKING_TERROR_SPAWN_BLOCK,    // c67 — orig LurkingTerror
            ModBlocks.GODZILLA_PART_SPAWN_BLOCK,     // c68 — orig GodzillaPart
            ModBlocks.GODZILLA_SPAWN_BLOCK,          // c69 — orig Godzilla (full egg IS pool-placed)
            ModBlocks.WORM_SMALL_SPAWN_BLOCK,        // c70 — orig SmallWorm
            ModBlocks.WORM_MEDIUM_SPAWN_BLOCK,       // c71 — orig MediumWorm
            ModBlocks.WORM_LARGE_SPAWN_BLOCK,        // c72 — orig LargeWorm
            ModBlocks.CASSOWARY_SPAWN_BLOCK,         // c73 — orig Cassowary
            ModBlocks.CLOUD_SHARK_SPAWN_BLOCK,       // c74 — orig CloudShark
            ModBlocks.GOLD_FISH_SPAWN_BLOCK,         // c75 — orig GoldFish
            ModBlocks.LEAF_MONSTER_SPAWN_BLOCK,      // c76 — orig LeafMonster
            ModBlocks.TSHIRT_SPAWN_BLOCK,            // c77 — orig Tshirt
            ModBlocks.ENDER_KNIGHT_SPAWN_BLOCK,      // c78 — orig EnderKnight
            ModBlocks.ENDER_REAPER_SPAWN_BLOCK,      // c79 — orig EnderReaper
            ModBlocks.BEAVER_SPAWN_BLOCK,            // c80 — orig Beaver
            ModBlocks.TREX_SPAWN_BLOCK,              // c81 — orig TRex
            ModBlocks.HERCULES_BEETLE_SPAWN_BLOCK,   // c82 — orig Hercules
            ModBlocks.MANTIS_SPAWN_BLOCK,            // c83 — orig Mantis
            ModBlocks.STINKY_SPAWN_BLOCK,            // c84 — orig Stinky
            ModBlocks.BOYFRIEND_SPAWN_BLOCK,         // c85 — orig Boyfriend
            ModBlocks.THE_KING_PART_SPAWN_BLOCK,     // c86 — orig TheKingPart (full King egg is craft-only)
            ModBlocks.EASTER_BUNNY_SPAWN_BLOCK,      // c87 — orig EasterBunny
            ModBlocks.CATER_KILLER_SPAWN_BLOCK,      // c88 — orig CaterKiller
            ModBlocks.MOLENOID_SPAWN_BLOCK,          // c89 — orig Molenoid
            ModBlocks.SEA_MONSTER_SPAWN_BLOCK,       // c90 — orig SeaMonster
            ModBlocks.SEA_VIPER_SPAWN_BLOCK,         // c91 — orig SeaViper
            ModBlocks.LEON_SPAWN_BLOCK,              // c92 — orig Leon
            ModBlocks.HAMMERHEAD_SPAWN_BLOCK,        // c93 — orig Hammerhead
            ModBlocks.RUBBER_DUCKY_SPAWN_BLOCK,      // c94 — orig RubberDucky
            ModBlocks.VILLAGER_SPAWN_BLOCK,          // c95 — orig Villager
            ModBlocks.BAND_P_SPAWN_BLOCK,            // c96 — orig Criminal (= BandP, WGEN-017)
            ModBlocks.THE_QUEEN_PART_SPAWN_BLOCK     // c97 — orig TheQueenPart (full Queen egg is craft-only)
    );

    /**
     * The 7-entry rare pool ({@code nextInt(104) < 7} then {@code nextInt(7)}),
     * orig OreSpawnWorld.java:371-402 == ChunkOreGenerator.java:37-68.
     */
    private static final List<Supplier<Block>> RARE_POOL = List.of(
            ModBlocks.BRUTALFLY_SPAWN_BLOCK,         // r0 — orig Brutalfly
            ModBlocks.NASTYSAURUS_SPAWN_BLOCK,       // r1 — orig Nastysaurus
            ModBlocks.POINTYSAURUS_SPAWN_BLOCK,      // r2 — orig Pointysaurus
            ModBlocks.CRICKET_SPAWN_BLOCK,           // r3 — orig Cricket
            ModBlocks.FROG_SPAWN_BLOCK,              // r4 — orig Frog
            ModBlocks.SPIDER_DRIVER_SPAWN_BLOCK,     // r5 — orig SpiderDriver
            ModBlocks.CRAB_SPAWN_BLOCK               // r6 — orig Crab
    );

    public SpawnOresPoolFeature(Codec<Configuration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<Configuration> context) {
        WorldGenLevel level = context.level();
        RandomSource random = context.random();
        BlockPos chunkOrigin = context.origin();
        Configuration config = context.config();

        // LESS_ORE is a BooleanValue ("lessOre", default false) — the original's
        // `LessOre != 0` int check maps to the flag directly.
        boolean lessOre = OreSpawnConfig.LESS_ORE.get();
        int passes = lessOre ? config.lessOrePasses() : config.passes();
        RuleTest stoneTarget = new TagMatchTest(BlockTags.STONE_ORE_REPLACEABLES);
        boolean placedAnything = false;

        for (int pass = 0; pass < passes; pass++) {
            // orig OSW:356 / COG:22 — rate + nextInt(dice)
            int veinCount = RATE + random.nextInt(config.countDice());
            // orig OSW:357-359 / COG:23-25 — 1-in-20 bonus burst
            if (random.nextInt(20) == 0) {
                veinCount += 30;
            }
            // orig OSW:360-362 / COG:26-28 — LessOre integer division
            if (lessOre) {
                veinCount /= 3;
            }
            for (int vein = 0; vein < veinCount; vein++) {
                int x = 3 + random.nextInt(10);           // orig OSW:366 / COG:32
                int y = random.nextInt(128);              // orig OSW:367 / COG:33
                int z = 3 + random.nextInt(10);           // orig OSW:368 / COG:34
                // orig OSW:369 / COG:35 — out-of-band veins are DISCARDED, not
                // re-rolled (~39% of rolls; the effective count is patchy·78/128).
                if (y > MAX_DEPTH || y < MIN_DEPTH) {
                    continue;
                }
                // orig OSW:370 / COG:36 — 7-in-104 rare-vs-common tier roll.
                Block block = random.nextInt(104) < 7
                        ? RARE_POOL.get(random.nextInt(RARE_POOL.size())).get()
                        : COMMON_POOL.get(random.nextInt(COMMON_POOL.size())).get();
                OreConfiguration veinConfig = new OreConfiguration(
                        List.of(OreConfiguration.target(stoneTarget, block.defaultBlockState())),
                        CLUMP_SIZE);
                placedAnything |= Feature.ORE.place(veinConfig, level, context.chunkGenerator(), random,
                        new BlockPos(chunkOrigin.getX() + x, y, chunkOrigin.getZ() + z));
            }
        }
        return placedAnything;
    }
}
