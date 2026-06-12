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
 * Ruby ore placement — faithful port of the 1.7.10 lava-seek loop.
 *
 * <p>Original (overworld pass, orig OreSpawnWorld.java:879-892, identical loop in
 * {@code generateRuby}, orig OreSpawnWorld.java:330-347): per chunk, run
 * {@code rate + nextInt(spread)} attempts. Each attempt picks
 * {@code x = 3 + chunkX + nextInt(10)}, {@code y = nextInt(128)} (rejected unless
 * 0 &le; y &le; 50, Ruby_stats orig OreSpawnMain.java:1573 = rate 10, Y 0-50),
 * {@code z = 3 + chunkZ + nextInt(10)}, then walks DOWN from y while y &gt; 5
 * looking for a lava block (still or flowing) directly above a stone block; the
 * stone is replaced with a single ruby ore block. No vein — ruby only ever
 * generates as single blocks hugging lava floors.</p>
 *
 * <p>The attempt count and Y-window are config fields so the overworld pass
 * (rate 10 + nextInt(5), orig OreSpawnWorld.java:880) and the Mining-dimension
 * pass (rate 10 + nextInt(7), orig OreSpawnWorld.java:334) can share this
 * feature. The Mining 3x multiplier and its LessOre gating live in the placed
 * feature's {@code orespawn:less_ore_count} modifier.</p>
 */
public class RubyLavaSeekFeature extends Feature<RubyLavaSeekFeature.Config> {

    /** {@code attempts_base + nextInt(attempts_spread)} attempts; Y accepted in [0, max_y]. */
    public record Config(int attemptsBase, int attemptsSpread, int maxY) implements FeatureConfiguration {
        public static final Codec<Config> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.intRange(0, 256).fieldOf("attempts_base").forGetter(Config::attemptsBase),
                Codec.intRange(1, 256).fieldOf("attempts_spread").forGetter(Config::attemptsSpread),
                Codec.intRange(0, 127).optionalFieldOf("max_y", 50).forGetter(Config::maxY)
        ).apply(instance, Config::new));
    }

    public RubyLavaSeekFeature(Codec<Config> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<Config> context) {
        WorldGenLevel level = context.level();
        RandomSource random = context.random();
        Config config = context.config();
        // Placed with no in_square/height modifiers, so origin is the chunk corner.
        int chunkX = context.origin().getX();
        int chunkZ = context.origin().getZ();
        BlockState rubyOre = ModBlocks.ORE_RUBY.get().defaultBlockState();

        boolean placedAny = false;
        // orig OreSpawnWorld.java:880 — patchy = rate + nextInt(spread)
        int attempts = config.attemptsBase() + random.nextInt(config.attemptsSpread());
        for (int i = 0; i < attempts; i++) {
            // orig OreSpawnWorld.java:882-884 — x/z confined to 3..12 within the chunk
            int x = 3 + chunkX + random.nextInt(10);
            int y = random.nextInt(128);
            int z = 3 + chunkZ + random.nextInt(10);
            // orig OreSpawnWorld.java:885 — Y-window rejection (Ruby_stats 0..50)
            if (y > config.maxY()) continue;
            // orig OreSpawnWorld.java:886-891 — descend looking for lava-over-stone
            for (int m = y; m > 5; m--) {
                BlockState at = level.getBlockState(new BlockPos(x, m, z));
                if (!at.is(Blocks.LAVA)) continue;
                BlockPos belowPos = new BlockPos(x, m - 1, z);
                if (!level.getBlockState(belowPos).is(Blocks.STONE)) continue;
                level.setBlock(belowPos, rubyOre, 2);
                placedAny = true;
                break;
            }
        }
        return placedAny;
    }
}
