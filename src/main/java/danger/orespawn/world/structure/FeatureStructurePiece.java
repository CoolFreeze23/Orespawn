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
 * <p>The bounding box of the piece is inflated around the feature's logical
 * origin by per-construction extents (defaults: ±16 horizontal, -16 below /
 * +80 above for the Royal Trees). This box acts as a "permit" for the
 * wrapped feature: any {@code level.setBlock} call inside the envelope is
 * accepted, while writes outside it are silently bulldozed by the chunk
 * generator on the next pass. A previous 1×1×1 box caused the Phase 13C
 * Royal Trees to be sheared at chunk borders — the inflated box fixes
 * that by giving the tree's full ±9-block horizontal footprint and
 * up-to-60-block vertical reach a contiguous write surface across multiple
 * chunks. Vanilla calls {@code postProcess} once per generated chunk that
 * intersects the inflated box (up to four times for a ±16 envelope), so
 * to keep the wrapped feature's RNG / surface checks deterministic we
 * gate placement to the single chunk that contains the feature's stored
 * {@link #featureOrigin}. Cross-chunk writes from that one invocation
 * still flow through the {@link WorldGenLevel}, which buffers them into
 * neighbouring chunks that the inflated box has already "permitted".</p>
 */
public class FeatureStructurePiece extends StructurePiece {

    private final ResourceLocation featureId;
    private final BlockPos featureOrigin;

    /**
     * Construct a piece whose bounding box is inflated around {@code origin} by
     * {@code horizontalExtent} blocks on the X/Z axes, by {@code downExtent}
     * blocks below {@code origin.y}, and by {@code upExtent} blocks above.
     * The inflated box acts as a "permit" for the wrapped feature: any
     * {@code level.setBlock} call inside this envelope is allowed, while
     * writes outside it are silently bulldozed by the chunk generator on the
     * very next pass — which is exactly what produced the sheer chunk-edge
     * cut-off observed on the Phase 13C Royal Trees with the previous 1×1×1
     * box.
     */
    public FeatureStructurePiece(BlockPos origin, Holder<ConfiguredFeature<?, ?>> feature,
                                 int horizontalExtent, int downExtent, int upExtent) {
        super(ModStructureTypes.FEATURE_PIECE.get(), 0,
                new BoundingBox(
                        origin.getX() - horizontalExtent,
                        origin.getY() - downExtent,
                        origin.getZ() - horizontalExtent,
                        origin.getX() + horizontalExtent,
                        origin.getY() + upExtent,
                        origin.getZ() + horizontalExtent));
        this.featureId = feature.unwrapKey()
                .map(ResourceKey::location)
                .orElseThrow(() -> new IllegalStateException(
                        "FeatureStructurePiece requires a registry-backed ConfiguredFeature; got an inline holder."));
        this.featureOrigin = origin.immutable();
    }

    public FeatureStructurePiece(StructurePieceSerializationContext ctx, CompoundTag tag) {
        super(ModStructureTypes.FEATURE_PIECE.get(), tag);
        this.featureId = ResourceLocation.parse(tag.getString("feature"));
        this.featureOrigin = new BlockPos(
                tag.getInt("origin_x"),
                tag.getInt("origin_y"),
                tag.getInt("origin_z"));
    }

    @Override
    protected void addAdditionalSaveData(StructurePieceSerializationContext ctx, CompoundTag tag) {
        tag.putString("feature", featureId.toString());
        tag.putInt("origin_x", featureOrigin.getX());
        tag.putInt("origin_y", featureOrigin.getY());
        tag.putInt("origin_z", featureOrigin.getZ());
    }

    @Override
    public void postProcess(WorldGenLevel level,
                            StructureManager structureManager,
                            ChunkGenerator chunkGenerator,
                            RandomSource random,
                            BoundingBox boundingBox,
                            ChunkPos chunkPos,
                            BlockPos pivot) {
        // Anchor-chunk gate: only place the feature during the postProcess
        // call for the chunk that owns the feature's origin. Other intersected
        // chunks (the ones the inflated bounding box "permits" cross-chunk
        // writes into) skip placement so we don't re-run the feature with
        // stale RNG and double-stamp the geometry.
        if (chunkPos.x != (featureOrigin.getX() >> 4) || chunkPos.z != (featureOrigin.getZ() >> 4)) {
            return;
        }
        // The wrapped feature does its own heightmap resolution + footprint
        // bound-checks, so we hand it the piece's stored logical origin
        // (NOT the inflated bounding box's min corner) and let it re-anchor.
        // This keeps the per-feature stability guards (Y-bound check,
        // surface-stability check, no-liquid check) authoritative.
        Registry<ConfiguredFeature<?, ?>> registry =
                level.registryAccess().registryOrThrow(Registries.CONFIGURED_FEATURE);
        ConfiguredFeature<?, ?> configured = registry.get(featureId);
        if (configured == null) {
            throw new IllegalStateException(
                    "FeatureStructurePiece could not resolve configured feature " + featureId);
        }
        configured.place(level, chunkGenerator, random, featureOrigin);
    }
}
