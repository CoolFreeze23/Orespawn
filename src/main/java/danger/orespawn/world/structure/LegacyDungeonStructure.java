package danger.orespawn.world.structure;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;

import java.util.Optional;

/**
 * Audit Part 2 &mdash; dedicated {@link Structure} for the four legacy
 * Tech &amp; Danger dungeons (Shadow, Greenhouse, Robot Lab, White House)
 * that the user audit flagged as procedural hallucinations.
 *
 * <p>The previous {@link FeatureStructure} wrapper around their respective
 * {@code *Feature} classes ran the entire generator inside a single
 * {@code WorldGenLevel} write window (~24-block radius), which forced
 * every prior implementation to either (a) shrink the legacy footprint
 * to fit (procedural hallucination) or (b) silently get sheared at chunk
 * borders. The Phase 13C-fix Royal Trees solved the same problem with a
 * dedicated {@link RoyalTreePiece} that uses the canonical Mansion
 * multi-pass {@code chunkBox.isInside} stitching pattern; this class is
 * the same fix applied to the four big dungeons via a single shared
 * {@link LegacyDungeonPiece} dispatched on a {@link
 * LegacyDungeonPiece.DungeonType} enum.</p>
 *
 * <p>JSON usage: {@code "type": "orespawn:legacy_dungeon",
 * "dungeon_type": "SHADOW" | "GREENHOUSE" | "ROBOT_LAB" | "WHITE_HOUSE"}.</p>
 */
public class LegacyDungeonStructure extends Structure {

    public static final MapCodec<LegacyDungeonStructure> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
            settingsCodec(inst),
            Codec.STRING.fieldOf("dungeon_type").forGetter(s -> s.dungeonType.name())
    ).apply(inst, (settings, name) -> new LegacyDungeonStructure(settings, LegacyDungeonPiece.DungeonType.valueOf(name))));

    private final LegacyDungeonPiece.DungeonType dungeonType;

    public LegacyDungeonStructure(StructureSettings settings, LegacyDungeonPiece.DungeonType dungeonType) {
        super(settings);
        this.dungeonType = dungeonType;
    }

    @Override
    public Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
        ChunkPos chunk = context.chunkPos();
        int x = chunk.getMinBlockX() + 8;
        int z = chunk.getMinBlockZ() + 8;
        int y = context.chunkGenerator().getBaseHeight(
                x, z,
                Heightmap.Types.WORLD_SURFACE_WG,
                context.heightAccessor(),
                context.randomState());

        // Y-bound rejection — refuse to spawn near world floor/ceiling so the
        // structure has room to build up (Greenhouse +12, RobotLab +40,
        // WhiteHouse +25, ShadowDungeon -10/+10).
        if (y <= context.heightAccessor().getMinBuildHeight() + 12) return Optional.empty();
        if (y + dungeonType.upExtent + 4 >= context.heightAccessor().getMaxBuildHeight()) return Optional.empty();

        BlockPos origin = new BlockPos(x, y, z);
        return Optional.of(new GenerationStub(origin, builder ->
                builder.addPiece(new LegacyDungeonPiece(origin, dungeonType))));
    }

    @Override
    public StructureType<?> type() {
        return ModStructureTypes.LEGACY_DUNGEON.get();
    }
}
