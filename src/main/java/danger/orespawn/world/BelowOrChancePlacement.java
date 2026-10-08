package danger.orespawn.world;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.placement.PlacementContext;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;

/**
 * {@code orespawn:below_or_chance}: keeps a position under {@code y}, and one in {@code chance} of those at or over it —
 * the original's gate on a lava lake in the Village and Mining (ChunkProviderOreSpawn2.java:317,
 * ChunkProviderOreSpawn3.java:302: {@code if (y < 63 || rand.nextInt(10) == 0)}), taken on the height drawn, before the
 * lake sinks to the ground.
 */
public class BelowOrChancePlacement extends PlacementModifier {
    public static final MapCodec<BelowOrChancePlacement> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.INT.fieldOf("y").forGetter(p -> p.y),
            Codec.intRange(1, 1_000_000).fieldOf("chance").forGetter(p -> p.chance)
    ).apply(instance, BelowOrChancePlacement::new));

    private final int y;
    private final int chance;

    public BelowOrChancePlacement(int y, int chance) {
        this.y = y;
        this.chance = chance;
    }

    @Override
    public Stream<BlockPos> getPositions(PlacementContext context, RandomSource random, BlockPos pos) {
        return pos.getY() < this.y || random.nextInt(this.chance) == 0 ? Stream.of(pos) : Stream.empty();
    }

    @Override
    public PlacementModifierType<?> type() {
        return ModWorldGen.BELOW_OR_CHANCE.get();
    }
}
