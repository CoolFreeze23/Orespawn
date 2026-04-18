package danger.orespawn.world.structure;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;

/**
 * Single-piece companion to {@link FeatureStructure}. Holds the
 * {@link ConfiguredFeature} ID this piece should delegate to (stored as
 * a {@link ResourceLocation} so it survives the chunk save / reload
 * round-trip), and replays the feature's {@code place} call once during
 * {@link #postProcess}.
 *
 * <p>The bounding box of the piece is intentionally a single 1×1×1 cell
 * at the feature's origin: each wrapped feature owns its own bound-checks
 * and footprint, so the structure pipeline only needs to mark "there is a
 * structure starting here." Vanilla's chunk-overlap gating still calls
 * {@code postProcess} exactly once per generated chunk that intersects
 * this box, which means the feature runs exactly once per spawn — there
 * is no risk of duplicate placement from large overlaps.</p>
 */
public class FeatureStructurePiece extends StructurePiece {

    private final ResourceLocation featureId;

    public FeatureStructurePiece(BlockPos origin, Holder<ConfiguredFeature<?, ?>> feature) {
        super(ModStructureTypes.FEATURE_PIECE.get(), 0, new BoundingBox(origin));
        this.featureId = feature.unwrapKey()
                .map(ResourceKey::location)
                .orElseThrow(() -> new IllegalStateException(
                        "FeatureStructurePiece requires a registry-backed ConfiguredFeature; got an inline holder."));
    }

    public FeatureStructurePiece(StructurePieceSerializationContext ctx, CompoundTag tag) {
        super(ModStructureTypes.FEATURE_PIECE.get(), tag);
        this.featureId = ResourceLocation.parse(tag.getString("feature"));
    }

    @Override
    protected void addAdditionalSaveData(StructurePieceSerializationContext ctx, CompoundTag tag) {
        tag.putString("feature", featureId.toString());
    }

    @Override
    public void postProcess(WorldGenLevel level,
                            StructureManager structureManager,
                            ChunkGenerator chunkGenerator,
                            RandomSource random,
                            BoundingBox boundingBox,
                            ChunkPos chunkPos,
                            BlockPos pivot) {
        // The wrapped feature does its own heightmap resolution + footprint
        // bound-checks, so we hand it the piece's logical origin and let it
        // re-anchor. This keeps the per-feature stability guards
        // (Y-bound check, surface-stability check, no-liquid check) authoritative.
        BlockPos origin = new BlockPos(this.boundingBox.minX(), this.boundingBox.minY(), this.boundingBox.minZ());

        Registry<ConfiguredFeature<?, ?>> registry =
                level.registryAccess().registryOrThrow(Registries.CONFIGURED_FEATURE);
        ConfiguredFeature<?, ?> configured = registry.get(featureId);
        if (configured == null) {
            throw new IllegalStateException(
                    "FeatureStructurePiece could not resolve configured feature " + featureId);
        }
        configured.place(level, chunkGenerator, random, origin);
    }
}
