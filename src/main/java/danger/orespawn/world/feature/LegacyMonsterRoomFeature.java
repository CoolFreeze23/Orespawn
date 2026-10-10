package danger.orespawn.world.feature;

import com.mojang.serialization.Codec;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.MonsterRoomFeature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

/**
 * Mining's and the Village's monster rooms (WGEN-117): vanilla's room, its rule and its build, each try on a random of
 * its own seeded by one draw. The room draws its walls, its chests and its spawner only where its wall openings let it
 * build, and those openings depend on what was decorated before; on the placement's random that moved every later try of
 * the chunk. Now a room built or not leaves the tries after it where they were.
 */
public class LegacyMonsterRoomFeature extends MonsterRoomFeature {

    public LegacyMonsterRoomFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        RandomSource own = RandomSource.create(context.random().nextLong());
        return super.place(new FeaturePlaceContext<>(context.topFeature(), context.level(), context.chunkGenerator(), own,
                context.origin(), context.config()));
    }
}
