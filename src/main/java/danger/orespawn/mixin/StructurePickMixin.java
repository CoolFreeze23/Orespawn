package danger.orespawn.mixin;

import danger.orespawn.world.structure.StructurePicks;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * WGEN-080 — the pick of an OreSpawn one-pick structure set is final. A HEAD injection on
 * {@code ChunkGenerator.tryGenerateStructure} (NeoForm ChunkGenerator.java:540-564), which
 * {@code createStructures} calls for a set's first pick and, when that pick finds no site, for each of the set's
 * other structures in turn: for the sets in {@link StructurePicks#ONE_PICK_SETS} every structure but the first pick
 * answers {@code false} without being asked, so a spot whose pick found no site stays empty, as the original's
 * builder left its chunk. Every other set, OreSpawn's and every mod's, goes through untouched. Registered in
 * {@code orespawn.mixins.json} (common side).
 */
@Mixin(ChunkGenerator.class)
public abstract class StructurePickMixin {

    @Inject(method = "tryGenerateStructure", at = @At("HEAD"), cancellable = true)
    private void orespawn$firstPickOnly(StructureSet.StructureSelectionEntry entry, StructureManager structureManager,
                                        RegistryAccess registryAccess, RandomState randomState,
                                        StructureTemplateManager structureTemplateManager, long seed, ChunkAccess chunk,
                                        ChunkPos chunkPos, SectionPos sectionPos, CallbackInfoReturnable<Boolean> cir) {
        if (StructurePicks.passedOver(registryAccess, entry, seed, chunkPos)) {
            cir.setReturnValue(false);
        }
    }
}
