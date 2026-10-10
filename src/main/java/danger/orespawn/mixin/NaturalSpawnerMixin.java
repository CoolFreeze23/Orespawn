package danger.orespawn.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import danger.orespawn.entity.ListedSpawnPlacement;
import danger.orespawn.world.GenerationRange;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Natural spawning draws each try's height from the bottom of the build range to the surface. In Utopia, the Village,
 * Crystal and Mining the build range reaches below the original's world, onto solid bedrock where nothing can spawn, so
 * in their new land the draw starts at the original's Y0 (as 1.7.10's did); land an earlier version generated keeps the
 * whole range ({@link GenerationRange#spawnBottom}).
 */
@Mixin(NaturalSpawner.class)
public abstract class NaturalSpawnerMixin {

    @WrapOperation(method = "getRandomPosWithin",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;getMinBuildHeight()I"))
    private static int orespawn$spawnBottom(Level level, Operation<Integer> original,
                                            @Local(argsOnly = true) LevelChunk chunk) {
        int bottom = original.call(level);
        return level instanceof ServerLevel server
                ? GenerationRange.spawnBottom(server.getChunkSource().getGenerator(), chunk, bottom) : bottom;
    }

    /**
     * WGEN-086: a species the port's lists carry ({@link ListedSpawnPlacement}) is placed by the list this try drew it
     * from, as 1.7.10's spawner placed an entry by its list's creature type (SpawnerAnimals.canCreatureTypeSpawnAtLocation):
     * a biome that lists it in a water list and a ground list (the Frog in the river and the swamp) no longer places a
     * water draw on the ground or a ground draw in the water.
     */
    @WrapOperation(method = "isValidSpawnPostitionForType",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/SpawnPlacements;isSpawnPositionOk(Lnet/minecraft/world/entity/EntityType;Lnet/minecraft/world/level/LevelReader;Lnet/minecraft/core/BlockPos;)Z"))
    private static boolean orespawn$placedByItsList(EntityType<?> type, LevelReader level, BlockPos pos,
                                                   Operation<Boolean> original,
                                                   @Local(argsOnly = true) MobCategory category) {
        if (SpawnPlacements.getPlacementType(type) == ListedSpawnPlacement.INSTANCE) {
            return ListedSpawnPlacement.isSpawnPositionOk(level, pos, type, category);
        }
        return original.call(type, level, pos);
    }
}
