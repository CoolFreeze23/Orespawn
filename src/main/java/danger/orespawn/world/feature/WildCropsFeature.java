package danger.orespawn.world.feature;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import danger.orespawn.ModBlocks;
import danger.orespawn.OreSpawnConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

/**
 * Wild crop patches (WGEN-007) — faithful port of the three wild-crop
 * generators called from {@code OreSpawnWorld.generateSurface}
 * (orig OreSpawnWorld.java:276-278):
 *
 * <ul>
 * <li><b>Strawberries</b> — {@code OreSpawnWorld.addStrawberries}
 * (orig OreSpawnWorld.java:961-978). 1-in-20 per-chunk gate
 * ({@code nextInt(20) != 0} skips, orig :962). Eligible in the Utopia
 * dimension or overworld biomes named Forest / ForestHills / Birch Forest /
 * Birch Forest Hills (orig :966). Then 5 attempts (orig :967): pick a random
 * column in the chunk, scan Y100 down through air to Y41 (orig :970); where
 * the block below is grass, place ONE strawberry plant and move to the next
 * attempt (orig :971-973).</li>
 *
 * <li><b>Corn</b> — {@code OreSpawnWorld.addCorn}
 * (orig OreSpawnWorld.java:1023-1068). 1-in-35 gate
 * ({@code nextInt(35) != 1} skips, orig :1026). Attempts {@code nc} = 6,
 * reduced to 5 when LessLag==1 and 3 when LessLag==2 (orig :1025,1029-1034).
 * Eligible in the Utopia or Village dimension or the overworld Plains biome
 * (orig :1036). Per attempt: the same Y100&rarr;Y41 grass-below air scan
 * (orig :1041-1043), plus ALL 9 blocks above the placement must be air or
 * the whole attempt is abandoned (orig :1044-1048). Stalk height
 * {@code 1 + nextInt(5)} (orig :1049): height 1 &rarr; lone CornPlant1 tip
 * (orig :1051); height 2 &rarr; CornPlant2 base + CornPlant1 above
 * (orig :1053-1056); height &ge; 3 &rarr; CornPlant2 base, CornPlant4 for
 * the middle (i = 1..height-1) and CornPlant1 at posY+height — height+1
 * blocks total (orig :1058-1062).</li>
 *
 * <li><b>Tomatoes</b> — {@code OreSpawnWorld.addTomatoes}
 * (orig OreSpawnWorld.java:1069-1110). 1-in-70 gate
 * ({@code nextInt(70) != 1} skips, orig :1071). Fixed 5 attempts
 * (orig :1076), same biome set and 9-air-above column check as corn
 * (orig :1075,1080-1087). Height {@code 1 + nextInt(3)} (orig :1088):
 * height 1 &rarr; TomatoPlant1 (orig :1090); height 2 &rarr; TomatoPlant2 +
 * TomatoPlant1 above (orig :1092-1095); height 3 &rarr; TomatoPlant3 base,
 * TomatoPlant4 middle (i = 1..height-1), TomatoPlant1 top
 * (orig :1097-1101).</li>
 * </ul>
 *
 * <p>Block mapping (BlockCorn.java:21-23 Javadoc; InstantGarden orig :111/:119
 * place MyTomatoPlant1/MyCornPlant1 where the port places TOMATO_0/CORN_0):
 * Plant1 &rarr; {@code *_0}, Plant2 &rarr; {@code *_1}, Plant3 &rarr;
 * {@code *_2}, Plant4 &rarr; {@code *_3}.</p>
 *
 * <p>The original's biome/dimension gate is expressed by which biome JSONs /
 * biome modifiers reference each placed feature: strawberries in the Utopia
 * biome and the forest,windswept_forest,birch_forest,old_growth_birch_forest
 * overworld modifier (repo mapping convention, phase_c_reports/
 * C1_entities_A_C.md:97-100: forestHills &rarr; windswept_forest,
 * birchForestHills &rarr; old_growth_birch_forest); corn and tomatoes in the
 * Utopia + Village biomes and the plains overworld modifier.</p>
 */
public class WildCropsFeature extends Feature<WildCropsFeature.Config> {

    /** Which of the three original generators this configured feature runs. */
    public enum Crop implements StringRepresentable {
        STRAWBERRY("strawberry"),
        CORN("corn"),
        TOMATO("tomato");

        public static final Codec<Crop> CODEC = StringRepresentable.fromEnum(Crop::values);

        private final String name;

        Crop(String name) {
            this.name = name;
        }

        @Override
        public String getSerializedName() {
            return name;
        }
    }

    public record Config(Crop crop) implements FeatureConfiguration {
        public static final Codec<Config> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Crop.CODEC.fieldOf("crop").forGetter(Config::crop)
        ).apply(instance, Config::new));
    }

    public WildCropsFeature(Codec<Config> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<Config> context) {
        WorldGenLevel level = context.level();
        RandomSource random = context.random();
        // Placed with no in_square/height modifiers, so origin is the chunk corner.
        int chunkX = context.origin().getX();
        int chunkZ = context.origin().getZ();

        return switch (context.config().crop()) {
            case STRAWBERRY -> placeStrawberries(level, random, chunkX, chunkZ);
            case CORN -> placeCorn(level, random, chunkX, chunkZ);
            case TOMATO -> placeTomatoes(level, random, chunkX, chunkZ);
        };
    }

    /** orig OreSpawnWorld.addStrawberries (OreSpawnWorld.java:961-978). */
    private boolean placeStrawberries(WorldGenLevel level, RandomSource random, int chunkX, int chunkZ) {
        // orig OreSpawnWorld.java:962 — 1-in-20 per-chunk gate
        if (random.nextInt(20) != 0) return false;

        boolean placedAny = false;
        // orig OreSpawnWorld.java:967 — 5 attempts
        for (int i = 0; i < 5; i++) {
            int x = chunkX + random.nextInt(16);
            int z = chunkZ + random.nextInt(16);
            // orig OreSpawnWorld.java:970 — scan Y100 down while air, stop at Y41
            for (int y = 100; y > 40 && level.getBlockState(new BlockPos(x, y, z)).isAir(); y--) {
                if (!level.getBlockState(new BlockPos(x, y - 1, z)).is(Blocks.GRASS_BLOCK)) continue;
                // orig OreSpawnWorld.java:972-973 — ONE strawberry plant, then next attempt
                level.setBlock(new BlockPos(x, y, z),
                        ModBlocks.STRAWBERRY_PLANT.get().defaultBlockState(), 2);
                placedAny = true;
                break;
            }
        }
        return placedAny;
    }

    /** orig OreSpawnWorld.addCorn (OreSpawnWorld.java:1023-1068). */
    private boolean placeCorn(WorldGenLevel level, RandomSource random, int chunkX, int chunkZ) {
        // orig OreSpawnWorld.java:1026 — 1-in-35 per-chunk gate (success index 1)
        if (random.nextInt(35) != 1) return false;

        // orig OreSpawnWorld.java:1025,1029-1034 — 6 attempts; 5 if LessLag==1, 3 if LessLag==2
        int nc = 6;
        int lessLag = OreSpawnConfig.LESS_LAG.get();
        if (lessLag == 1) nc = 5;
        if (lessLag == 2) nc = 3;

        boolean placedAny = false;
        attempts:
        // orig OreSpawnWorld.java:1037 — nc attempts
        for (int j = 0; j < nc; j++) {
            int x = chunkX + random.nextInt(16);
            int z = chunkZ + random.nextInt(16);
            // orig OreSpawnWorld.java:1041 — scan Y100 down while air, stop at Y41
            for (int y = 100; y > 40 && level.getBlockState(new BlockPos(x, y, z)).isAir(); y--) {
                if (!level.getBlockState(new BlockPos(x, y - 1, z)).is(Blocks.GRASS_BLOCK)) continue;
                // orig OreSpawnWorld.java:1044-1048 — the 9 blocks above must ALL
                // be air, else the whole attempt is abandoned (continue block0)
                if (!allAirAbove(level, x, y, z)) continue attempts;
                // orig OreSpawnWorld.java:1049-1050 — stalk height 1 + nextInt(5)
                int height = 1 + random.nextInt(5);
                if (height == 1) {
                    // orig OreSpawnWorld.java:1051 — lone Plant1 (growing tip)
                    level.setBlock(new BlockPos(x, y, z),
                            ModBlocks.CORN_0.get().defaultBlockState(), 2);
                } else if (height == 2) {
                    // orig OreSpawnWorld.java:1053-1056 — Plant2 base + Plant1 tip
                    level.setBlock(new BlockPos(x, y, z),
                            ModBlocks.CORN_1.get().defaultBlockState(), 2);
                    level.setBlock(new BlockPos(x, y + 1, z),
                            ModBlocks.CORN_0.get().defaultBlockState(), 2);
                } else {
                    // orig OreSpawnWorld.java:1058-1062 — Plant2 base, Plant4
                    // middle (i = 1..height-1), Plant1 at y+height (height+1 blocks)
                    level.setBlock(new BlockPos(x, y, z),
                            ModBlocks.CORN_1.get().defaultBlockState(), 2);
                    for (int i = 1; i < height; i++) {
                        level.setBlock(new BlockPos(x, y + i, z),
                                ModBlocks.CORN_3.get().defaultBlockState(), 2);
                    }
                    level.setBlock(new BlockPos(x, y + height, z),
                            ModBlocks.CORN_0.get().defaultBlockState(), 2);
                }
                placedAny = true;
                continue attempts;
            }
        }
        return placedAny;
    }

    /** orig OreSpawnWorld.addTomatoes (OreSpawnWorld.java:1069-1110). */
    private boolean placeTomatoes(WorldGenLevel level, RandomSource random, int chunkX, int chunkZ) {
        // orig OreSpawnWorld.java:1071 — 1-in-70 per-chunk gate (success index 1)
        if (random.nextInt(70) != 1) return false;

        boolean placedAny = false;
        attempts:
        // orig OreSpawnWorld.java:1076 — 5 attempts
        for (int j = 0; j < 5; j++) {
            int x = chunkX + random.nextInt(16);
            int z = chunkZ + random.nextInt(16);
            // orig OreSpawnWorld.java:1080 — scan Y100 down while air, stop at Y41
            for (int y = 100; y > 40 && level.getBlockState(new BlockPos(x, y, z)).isAir(); y--) {
                if (!level.getBlockState(new BlockPos(x, y - 1, z)).is(Blocks.GRASS_BLOCK)) continue;
                // orig OreSpawnWorld.java:1083-1087 — same 9-air-above column check
                if (!allAirAbove(level, x, y, z)) continue attempts;
                // orig OreSpawnWorld.java:1088-1089 — plant height 1 + nextInt(3)
                int height = 1 + random.nextInt(3);
                if (height == 1) {
                    // orig OreSpawnWorld.java:1090 — lone Plant1
                    level.setBlock(new BlockPos(x, y, z),
                            ModBlocks.TOMATO_0.get().defaultBlockState(), 2);
                } else if (height == 2) {
                    // orig OreSpawnWorld.java:1092-1095 — Plant2 base + Plant1 top
                    level.setBlock(new BlockPos(x, y, z),
                            ModBlocks.TOMATO_1.get().defaultBlockState(), 2);
                    level.setBlock(new BlockPos(x, y + 1, z),
                            ModBlocks.TOMATO_0.get().defaultBlockState(), 2);
                } else {
                    // orig OreSpawnWorld.java:1097-1101 — Plant3 base, Plant4
                    // middle (i = 1..height-1), Plant1 at y+height
                    level.setBlock(new BlockPos(x, y, z),
                            ModBlocks.TOMATO_2.get().defaultBlockState(), 2);
                    for (int i = 1; i < height; i++) {
                        level.setBlock(new BlockPos(x, y + i, z),
                                ModBlocks.TOMATO_3.get().defaultBlockState(), 2);
                    }
                    level.setBlock(new BlockPos(x, y + height, z),
                            ModBlocks.TOMATO_0.get().defaultBlockState(), 2);
                }
                placedAny = true;
                continue attempts;
            }
        }
        return placedAny;
    }

    /**
     * orig OreSpawnWorld.java:1044-1048 — {@code is_all_air} check: the 9 blocks
     * at y+1..y+9 must all be air. The original scans all 9 without early exit,
     * but no RNG is drawn, so short-circuiting is stream-identical.
     */
    private static boolean allAirAbove(WorldGenLevel level, int x, int y, int z) {
        for (int i = 1; i < 10; i++) {
            if (!level.getBlockState(new BlockPos(x, y + i, z)).isAir()) return false;
        }
        return true;
    }
}
