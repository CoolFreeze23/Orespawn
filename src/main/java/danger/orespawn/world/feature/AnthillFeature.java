package danger.orespawn.world.feature;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import danger.orespawn.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

/**
 * Anthill surface blocks — faithful port of {@code OreSpawnWorld.addAnts}
 * (orig OreSpawnWorld.java:1472-1507).
 *
 * <p>Original: 1-in-30 chunk gate, then 4 attempts. Each attempt picks a random
 * column in the chunk and scans from Y100 down through air to Y41 for a grass
 * block. On grass: a 1-in-{@code redfreq} roll picks one of red / rainbow /
 * unstable / termite anthill blocks (nextInt(4)), otherwise the black ant
 * block; the grass block itself is replaced. Callers pass redfreq 4 (overworld
 * orig OreSpawnWorld.java:323, Village :118) or 2 (Mining :106-107 twice,
 * Chaos :206) — values below 2 are clamped to 2 (orig :1476-1478).</p>
 *
 * <p>The original's RedAntEnable/BlackAntEnable/... config switches default to
 * enabled; the config layer is Phase E scope, so all five block types are
 * always eligible here.</p>
 */
public class AnthillFeature extends Feature<AnthillFeature.Config> {

    /** 1-in-{@code redfreq} chance per placement that the anthill is a special (non-black) type. */
    public record Config(int redfreq) implements FeatureConfiguration {
        public static final Codec<Config> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.intRange(2, 64).fieldOf("redfreq").forGetter(Config::redfreq)
        ).apply(instance, Config::new));
    }

    public AnthillFeature(Codec<Config> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<Config> context) {
        WorldGenLevel level = context.level();
        RandomSource random = context.random();
        int chunkX = context.origin().getX();
        int chunkZ = context.origin().getZ();
        int redfreq = context.config().redfreq();

        // orig OreSpawnWorld.java:1479 — 1-in-30 per-chunk gate (LessLag=0 default)
        if (random.nextInt(30) != 0) return false;

        boolean placedAny = false;
        // orig OreSpawnWorld.java:1482 — 4 attempts per gated chunk
        for (int i = 0; i < 4; i++) {
            int x = chunkX + random.nextInt(16);
            int z = chunkZ + random.nextInt(16);
            // orig OreSpawnWorld.java:1485 — scan Y100 down while air, stop at Y41
            for (int y = 100; y > 40 && level.getBlockState(new BlockPos(x, y, z)).isAir(); y--) {
                BlockPos below = new BlockPos(x, y - 1, z);
                if (!level.getBlockState(below).is(Blocks.GRASS_BLOCK)) continue;
                BlockState anthill;
                if (random.nextInt(redfreq) == 0) {
                    // orig OreSpawnWorld.java:1488-1499 — special anthill picker
                    anthill = switch (random.nextInt(4)) {
                        case 0 -> ModBlocks.RED_ANT_BLOCK.get().defaultBlockState();
                        case 1 -> ModBlocks.RAINBOW_ANT_BLOCK.get().defaultBlockState();
                        case 2 -> ModBlocks.UNSTABLE_ANT_BLOCK.get().defaultBlockState();
                        default -> ModBlocks.TERMITE_BLOCK.get().defaultBlockState();
                    };
                } else {
                    // orig OreSpawnWorld.java:1503 — black ant block
                    anthill = ModBlocks.ANT_BLOCK.get().defaultBlockState();
                }
                level.setBlock(below, anthill, 2);
                placedAny = true;
                break;
            }
        }
        return placedAny;
    }
}
