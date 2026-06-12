package danger.orespawn.world;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import danger.orespawn.OreSpawnConfig;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.placement.PlacementContext;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;

/**
 * Vein-count placement reproducing the exact 1.7.10 OreSpawn ore loop
 * arithmetic (WGEN-001/WGEN-011). The original pattern, shared by
 * {@code OreSpawnWorld.generateOres} (orig OreSpawnWorld.java:805-877) and
 * {@code ChunkOreGenerator.generateOresInChunk}
 * (orig ChunkOreGenerator.java:471-544), is:
 *
 * <pre>
 *   patchy = rate + nextInt(extraDice);          // attempt count
 *   if (LessOre != 0) patchy /= divisor;         // truncating division
 *   for (i &lt; patchy) {
 *       y = nextInt(128);
 *       if (y &gt; maxdepth || y &lt; mindepth) continue;  // attempt wasted
 *       placeVein(x, y, z);
 *   }
 * </pre>
 *
 * <p>A vanilla {@code count} + {@code height_range} pair cannot express this:
 * it would place EVERY attempt inside the Y window instead of discarding
 * out-of-window attempts. This modifier rolls the attempt count, the LessOre
 * truncation, and the per-attempt {@code nextInt(128)} Y rejection exactly as
 * written, emitting accepted positions at their rolled Y (XZ spread is left
 * to a following {@code in_square} modifier).</p>
 *
 * <p>{@code passes} models the Mining dimension calling the whole generator
 * up to three times: {@code generateOresInChunk} runs once, then twice more
 * only when {@code LessOre == 0} (orig ChunkProviderOreSpawn2.java:191-195).
 * So Mining features use {@code passes: 3, less_ore_passes: 1}, while the
 * LessOre==0-only boost passes use {@code less_ore_passes: 0}.</p>
 *
 * <p>Thread safety: stateless apart from immutable codec fields; the config
 * read is a NeoForge config getter (safe from worker threads).</p>
 */
public class OreSpawnVeinPlacement extends PlacementModifier {

    /** Original Y dice — every loop rolls {@code nextInt(128)} (orig OreSpawnWorld.java:812 etc). */
    private static final int Y_DICE = 128;

    public static final MapCodec<OreSpawnVeinPlacement> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.intRange(0, 256).fieldOf("count").forGetter(p -> p.count),
            Codec.intRange(0, 256).optionalFieldOf("extra_dice", 0).forGetter(p -> p.extraDice),
            Codec.intRange(0, 256).fieldOf("min_y").forGetter(p -> p.minY),
            Codec.intRange(0, 256).fieldOf("max_y").forGetter(p -> p.maxY),
            Codec.intRange(1, 8).optionalFieldOf("passes", 1).forGetter(p -> p.passes),
            Codec.intRange(0, 8).optionalFieldOf("less_ore_passes").forGetter(p -> p.lessOrePasses),
            Codec.intRange(1, 16).optionalFieldOf("less_ore_divisor", 1).forGetter(p -> p.lessOreDivisor)
    ).apply(instance, OreSpawnVeinPlacement::new));

    private final int count;
    private final int extraDice;
    private final int minY;
    private final int maxY;
    private final int passes;
    private final Optional<Integer> lessOrePasses;
    private final int lessOreDivisor;

    public OreSpawnVeinPlacement(int count, int extraDice, int minY, int maxY,
                                 int passes, Optional<Integer> lessOrePasses, int lessOreDivisor) {
        this.count = count;
        this.extraDice = extraDice;
        this.minY = minY;
        this.maxY = maxY;
        this.passes = passes;
        this.lessOrePasses = lessOrePasses;
        this.lessOreDivisor = lessOreDivisor;
    }

    @Override
    public Stream<BlockPos> getPositions(PlacementContext context, RandomSource random, BlockPos pos) {
        boolean lessOre = OreSpawnConfig.LESS_ORE.get();
        int effectivePasses = lessOre ? lessOrePasses.orElse(this.passes) : this.passes;

        Stream.Builder<BlockPos> out = Stream.builder();
        for (int pass = 0; pass < effectivePasses; pass++) {
            // orig: patchy = rate + nextInt(dice), then LessOre truncating division
            int attempts = this.count + (this.extraDice > 0 ? random.nextInt(this.extraDice) : 0);
            if (lessOre) {
                attempts /= this.lessOreDivisor;
            }
            for (int i = 0; i < attempts; i++) {
                // orig: randPosY = nextInt(128); reject outside [mindepth, maxdepth]
                int y = random.nextInt(Y_DICE);
                if (y < this.minY || y > this.maxY) {
                    continue;
                }
                out.add(pos.atY(y));
            }
        }
        return out.build();
    }

    @Override
    public PlacementModifierType<?> type() {
        return ModWorldGen.VEIN_COUNT.get();
    }
}
