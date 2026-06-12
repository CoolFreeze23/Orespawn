package danger.orespawn.world;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import danger.orespawn.OreSpawnConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;
import net.minecraft.world.level.levelgen.placement.RepeatingPlacement;

/**
 * Count placement that honours the 1.7.10 {@code LessOre} config switch
 * (ITEM-064). The original divided per-chunk vein counts when
 * {@code OreSpawnMain.LessOre != 0}: by 3 for uranium/titanium/amethyst/salt
 * (orig OreSpawnWorld.java:807-848) and by 2 for the red-ant/termite troll
 * blocks (orig OreSpawnWorld.java:857-870). The divisor is data-driven so each
 * placed feature JSON declares the original's value.
 *
 * <p>Used as a drop-in replacement for {@code minecraft:count} in
 * {@code data/orespawn/worldgen/placed_feature/*.json}; with {@code lessOre}
 * false (the original default, orig OreSpawnMain.java:1470) it behaves exactly
 * like vanilla count placement.</p>
 */
public class LessOreCountPlacement extends RepeatingPlacement {

    public static final MapCodec<LessOreCountPlacement> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            IntProvider.NON_NEGATIVE_CODEC.fieldOf("count").forGetter(p -> p.count),
            Codec.intRange(1, Integer.MAX_VALUE).optionalFieldOf("less_ore_divisor", 3).forGetter(p -> p.lessOreDivisor)
    ).apply(instance, LessOreCountPlacement::new));

    private final IntProvider count;
    private final int lessOreDivisor;

    public LessOreCountPlacement(IntProvider count, int lessOreDivisor) {
        this.count = count;
        this.lessOreDivisor = lessOreDivisor;
    }

    @Override
    protected int count(RandomSource random, BlockPos pos) {
        int n = this.count.sample(random);
        return OreSpawnConfig.LESS_ORE.get() ? n / this.lessOreDivisor : n;
    }

    @Override
    public PlacementModifierType<?> type() {
        return ModWorldGen.LESS_ORE_COUNT.get();
    }
}
