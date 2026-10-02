package danger.orespawn.data;

import java.util.Map;
import java.util.function.BooleanSupplier;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import danger.orespawn.OreSpawnConfig;
import net.neoforged.neoforge.common.conditions.ICondition;

/**
 * MOD-040 — a load condition on a [modern] feature: {@code {"type": "orespawn:modern", "key": "horseArmour"}} in a
 * recipe's or a loot modifier's {@code neoforge:conditions} loads it only while that feature is on (modern.enabled and
 * its own key, through the feature's {@code OreSpawnConfig} evaluation). Read as the data packs load, so a change
 * follows on /reload or a restart. An unknown key is a decoding error, not a silent false.
 */
public record ModernCondition(String key) implements ICondition {

    /** The [modern] features a data file may be conditioned on, by key. */
    private static final Map<String, BooleanSupplier> FEATURES = Map.of(
            "horseArmour", OreSpawnConfig::horseArmour);

    public static final MapCodec<ModernCondition> CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
            Codec.STRING.validate(key -> FEATURES.containsKey(key)
                            ? DataResult.success(key)
                            : DataResult.error(() -> "unknown [modern] feature \"" + key + "\"; known: " + FEATURES.keySet()))
                    .fieldOf("key").forGetter(ModernCondition::key)
    ).apply(builder, ModernCondition::new));

    @Override
    public boolean test(IContext context) {
        return FEATURES.get(this.key).getAsBoolean();
    }

    @Override
    public MapCodec<? extends ICondition> codec() {
        return CODEC;
    }

    @Override
    public String toString() {
        return "modern(\"" + this.key + "\")";
    }
}
