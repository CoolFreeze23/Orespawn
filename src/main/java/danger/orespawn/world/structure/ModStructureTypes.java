package danger.orespawn.world.structure;

import danger.orespawn.OreSpawnMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * DeferredRegisters for OreSpawn's custom {@link StructureType} +
 * {@link StructurePieceType}. Both go in here because they're tightly
 * coupled — the structure resolves its piece type during {@code
 * findGenerationPoint}, and the piece's load-from-NBT constructor
 * needs to look up the same {@link StructurePieceType} entry.
 *
 * <p>Registered on the mod event bus from {@link OreSpawnMod}. Order is
 * not significant — both registries fire in the same lifecycle phase.</p>
 */
public class ModStructureTypes {

    public static final DeferredRegister<StructureType<?>> STRUCTURE_TYPES =
            DeferredRegister.create(Registries.STRUCTURE_TYPE, OreSpawnMod.MOD_ID);

    public static final DeferredRegister<StructurePieceType> STRUCTURE_PIECES =
            DeferredRegister.create(Registries.STRUCTURE_PIECE, OreSpawnMod.MOD_ID);

    /**
     * Generic "delegate to a configured feature" structure. All 13 OreSpawn
     * hand-built dungeons share this single structure type and only differ
     * in the {@code feature} field of their {@code worldgen/structure/*.json}
     * entries.
     */
    public static final DeferredHolder<StructureType<?>, StructureType<FeatureStructure>> FEATURE =
            STRUCTURE_TYPES.register("feature", () -> () -> FeatureStructure.CODEC);

    /**
     * Companion piece type for {@link FeatureStructure}. The
     * {@link StructurePieceType} contract is the two-argument
     * {@code load(StructurePieceSerializationContext, CompoundTag)} form;
     * we point it directly at the matching {@link FeatureStructurePiece}
     * constructor so chunk save/reload round-trips through NBT cleanly.
     */
    public static final DeferredHolder<StructurePieceType, StructurePieceType> FEATURE_PIECE =
            STRUCTURE_PIECES.register("feature_piece",
                    () -> (StructurePieceType) FeatureStructurePiece::new);

    /**
     * Phase 13C-fix2 — Dedicated {@link StructureType} for the Royal Trees
     * (Tree of Goodness + Queen Tree). Replaces the previous
     * {@code orespawn:feature} wiring for these specifically because the
     * generic feature-wrapper couldn't span chunks: the
     * {@code WorldGenRegion} 24-block write cap clipped the 60-tall, ±32-wide
     * trees at chunk borders. {@link RoyalTreeStructure} +
     * {@link RoyalTreePiece} use the canonical Mansion / Stronghold pattern
     * (massive blueprint bounding box + per-chunk write gating in
     * {@code postProcess}) to stitch the tree together across chunks.
     */
    public static final DeferredHolder<StructureType<?>, StructureType<RoyalTreeStructure>> ROYAL_TREE =
            STRUCTURE_TYPES.register("royal_tree", () -> () -> RoyalTreeStructure.CODEC);

    /**
     * Companion piece type for {@link RoyalTreeStructure}. Same direct-
     * constructor-reference pattern as {@link #FEATURE_PIECE}, since
     * {@link RoyalTreePiece} owns the {@code (StructurePieceSerializationContext,
     * CompoundTag)} load constructor and the chunk save/load round-trip
     * goes through that path.
     */
    public static final DeferredHolder<StructurePieceType, StructurePieceType> ROYAL_TREE_PIECE =
            STRUCTURE_PIECES.register("royal_tree_piece",
                    () -> (StructurePieceType) RoyalTreePiece::new);

    public static void register(IEventBus eventBus) {
        STRUCTURE_TYPES.register(eventBus);
        STRUCTURE_PIECES.register(eventBus);
    }
}
