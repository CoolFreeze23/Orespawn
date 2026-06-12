package danger.orespawn.world;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import danger.orespawn.OreSpawnConfig;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;
import net.minecraft.world.level.levelgen.placement.RepeatingPlacement;

/**
 * Count placement that honours the 1.7.10 {@code LessOre} config switch
 * (ITEM-064). The original divided per-chunk vein ATTEMPT counts when
 * {@code OreSpawnMain.LessOre != 0}: by 3 for uranium/titanium/amethyst/salt
 * (orig OreSpawnWorld.java:807-848) and by 2 for the red-ant/termite troll
 * blocks (orig OreSpawnWorld.java:857-870), with Java integer truncation.
 *
 * <p>Because the port folds the original's attempt-count + Y-window rejection
 * into a single "effective accepted veins" distribution (the {@code count}
 * IntProvider — see phase_c_reports/C7_worldgen.md for the math), dividing the
 * folded sample would NOT reproduce the original's
 * truncate-then-reject arithmetic. JSONs therefore supply an explicit
 * {@code less_ore_count} IntProvider holding the separately folded LessOre
 * distribution; when present it is sampled instead of dividing. The
 * {@code less_ore_divisor} fallback is kept for placed features whose attempt
 * count has no Y-rejection (where plain truncating division IS exact).</p>
 *
 * <p>With {@code lessOre} false (the original default,
 * orig OreSpawnMain.java:1470) this behaves exactly like vanilla count
 * placement.</p>
 */
public class LessOreCountPlacement extends RepeatingPlacement {

    public static final MapCodec<LessOreCountPlacement> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            IntProvider.NON_NEGATIVE_CODEC.fieldOf("count").forGetter(p -> p.count),
            Codec.intRange(1, Integer.MAX_VALUE).optionalFieldOf("less_ore_divisor", 3).forGetter(p -> p.lessOreDivisor),
            IntProvider.NON_NEGATIVE_CODEC.optionalFieldOf("less_ore_count").forGetter(p -> p.lessOreCount)
    ).apply(instance, LessOreCountPlacement::new));

    private final IntProvider count;
    private final int lessOreDivisor;
    private final Optional<IntProvider> lessOreCount;

    public LessOreCountPlacement(IntProvider count, int lessOreDivisor, Optional<IntProvider> lessOreCount) {
        this.count = count;
        this.lessOreDivisor = lessOreDivisor;
        this.lessOreCount = lessOreCount;
    }

    @Override
    protected int count(RandomSource random, BlockPos pos) {
        if (OreSpawnConfig.LESS_ORE.get()) {
            return lessOreCount.map(p -> p.sample(random))
                    .orElseGet(() -> this.count.sample(random) / this.lessOreDivisor);
        }
        return this.count.sample(random);
    }

    @Override
    public PlacementModifierType<?> type() {
        return ModWorldGen.LESS_ORE_COUNT.get();
    }
}
