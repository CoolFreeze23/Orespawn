package danger.orespawn.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.function.Predicate;

/**
 * Shared building blocks for the original 1.7.10 {@code func_70601_bi}
 * (getCanSpawnHere) gates, ported for Phase D1 (ENT-SYS-002 / ENT-SYS2-004).
 *
 * <p>Each original override is a combination of the same few checks, always in
 * entity-specific order with entity-specific bounds (the originals are
 * copy-paste variants; the asymmetric loop bounds like {@code for (k=-3;k<3)}
 * — i.e. -3..+2 — are preserved exactly by passing inclusive bounds here).
 * Entities compose these in their {@code checkSpawnRules} overrides, citing
 * their original line ranges; the full extracted corpus is in
 * {@code phase_d_reports/D1_original_spawn_rules.md}.</p>
 *
 * <p>Server side only — natural-spawn rule checks never run on the client.</p>
 */
public final class OriginalSpawnGates {

    private OriginalSpawnGates() {
    }

    /**
     * The original "spawnered" bypass: scan a box of block offsets around the
     * mob's feet for a vanilla mob spawner configured to spawn this entity
     * type; when found the natural-spawn gates are skipped entirely (the
     * originals {@code return true} immediately).
     *
     * <p>The 1.7.10 version compared the spawner's entity name string (e.g.
     * {@code "Alien"}, {@code "Rubber Ducky"}); the port compares the spawner's
     * configured entity id against {@code type} — the same discrimination, in
     * registry-id form.</p>
     *
     * <p>Typical original bounds (Alien.java:402-414 and ~30 clones):
     * x/z -3..+2, y 0..+4. Variants: x/z -2..+1 (Bee, CaveFisher, Rat),
     * x/z -2..+2 with y +1..+3 (Brutalfly, Mantis, Rotator).</p>
     *
     * @param minXZ inclusive minimum x/z offset (e.g. -3)
     * @param maxXZ inclusive maximum x/z offset (e.g. 2, matching the original exclusive {@code < 3} loop bound)
     * @param minY  inclusive minimum y offset
     * @param maxY  inclusive maximum y offset
     */
    public static boolean nearOwnSpawner(Mob mob, LevelAccessor level,
                                         int minXZ, int maxXZ, int minY, int maxY) {
        EntityType<?> wantedType = mob.getType();
        BlockPos feet = mob.blockPosition();
        for (int dz = minXZ; dz <= maxXZ; dz++) {
            for (int dx = minXZ; dx <= maxXZ; dx++) {
                for (int dy = minY; dy <= maxY; dy++) {
                    BlockPos pos = feet.offset(dx, dy, dz);
                    if (!level.getBlockState(pos).is(Blocks.SPAWNER)) continue;
                    if (spawnerSpawns(level, pos, wantedType)) return true;
                }
            }
        }
        return false;
    }

    /** {@link #nearOwnSpawner} with the most common original bounds: x/z -3..+2, y 0..+4. */
    public static boolean nearOwnSpawner(Mob mob, LevelAccessor level) {
        return nearOwnSpawner(mob, level, -3, 2, 0, 4);
    }

    /**
     * Single-position variant of {@link #nearOwnSpawner}: true when the block at
     * {@code pos} is a mob spawner configured to spawn this entity type. Used by
     * the gates whose original loop interleaves the spawner match with another
     * per-block test (e.g. Emperor Scorpion's combined spawner-or-clear-air scan,
     * EmperorScorpion.java:530-546).
     */
    public static boolean isOwnSpawner(Mob mob, LevelAccessor level, BlockPos pos) {
        return level.getBlockState(pos).is(Blocks.SPAWNER)
                && spawnerSpawns(level, pos, mob.getType());
    }

    private static boolean spawnerSpawns(LevelAccessor level, BlockPos pos, EntityType<?> wantedType) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (!(blockEntity instanceof SpawnerBlockEntity spawner)) return false;
        // BaseSpawner's next-spawn entity id is not exposed; read it from the
        // saved SpawnData ("SpawnData".entity.id). Cheap enough for spawn checks.
        CompoundTag tag = spawner.saveWithoutMetadata(level.registryAccess());
        CompoundTag entityTag = tag.getCompound("SpawnData").getCompound("entity");
        String spawnId = entityTag.getString("id");
        return EntityType.getKey(wantedType).toString().equals(spawnId);
    }

    /**
     * The original clear-air box check: every block in the offset box around
     * the mob's feet must match {@code allowed} (usually just air) or the
     * spawn is rejected. Bounds are inclusive, matching the cited loop ranges.
     */
    public static boolean boxMatches(Mob mob, LevelAccessor level,
                                     int minX, int maxX, int minY, int maxY, int minZ, int maxZ,
                                     Predicate<BlockState> allowed) {
        BlockPos feet = mob.blockPosition();
        for (int dz = minZ; dz <= maxZ; dz++) {
            for (int dx = minX; dx <= maxX; dx++) {
                for (int dy = minY; dy <= maxY; dy++) {
                    if (!allowed.test(level.getBlockState(feet.offset(dx, dy, dz)))) return false;
                }
            }
        }
        return true;
    }

    /** {@link #boxMatches} requiring plain air, the dominant original variant. */
    public static boolean airBox(Mob mob, LevelAccessor level,
                                 int minX, int maxX, int minY, int maxY, int minZ, int maxZ) {
        return boxMatches(mob, level, minX, maxX, minY, maxY, minZ, maxZ, BlockState::isAir);
    }

    /**
     * The original buddy-count check ({@code findBuddies()} /
     * {@code findNearestEntityWithinAABB}): number of same-class entities,
     * including this one once it counts itself, inside the bounding box
     * inflated by the given amounts. Originals compare the result against an
     * entity-specific cap.
     */
    public static int countBuddies(Mob mob, LevelAccessor level, Class<? extends Mob> buddyClass,
                                   double inflateX, double inflateY, double inflateZ) {
        return level.getEntitiesOfClass(buddyClass,
                mob.getBoundingBox().inflate(inflateX, inflateY, inflateZ)).size();
    }

    /** True when any OTHER entity of the class is inside the inflated box (the nearest-entity originals). */
    public static boolean anyOtherNearby(Mob mob, LevelAccessor level, Class<? extends Mob> buddyClass,
                                         double inflateX, double inflateY, double inflateZ) {
        return !level.getEntitiesOfClass(buddyClass,
                mob.getBoundingBox().inflate(inflateX, inflateY, inflateZ),
                other -> other != mob).isEmpty();
    }

    /**
     * Original {@code world.isDaytime()} ({@code func_72935_r}) — sky-darken
     * based, server side. Falls back to the wall-clock day window when the
     * accessor is not a server level (never the case for natural spawns).
     */
    public static boolean isDaytime(LevelAccessor level) {
        if (level instanceof ServerLevelAccessor server) {
            return server.getLevel().isDay();
        }
        return level.dayTime() % 24000L < 13000L;
    }

    /**
     * Original {@code isValidLightLevel} ({@code func_70814_o}) — the standard
     * monster darkness gate. Mapped to the vanilla 1.21.1 equivalent.
     */
    public static boolean isDarkEnough(Mob mob, LevelAccessor level) {
        if (level instanceof ServerLevelAccessor server) {
            return Monster.isDarkEnoughToSpawn(server, mob.blockPosition(), mob.getRandom());
        }
        return false;
    }

    /**
     * The five biomes where Ghost/GhostSkelly spawn year-round (orig
     * OreSpawnMain.java:4783-4788/4790-4795, ungated by the Oct 31 check);
     * everywhere else in halloween_ghosts.json requires Halloween.
     */
    public static boolean inYearRoundGhostBiome(Mob mob, LevelAccessor level) {
        var biome = level.getBiome(mob.blockPosition());
        return biome.is(net.minecraft.world.level.biome.Biomes.SNOWY_TAIGA)
                || biome.is(net.minecraft.world.level.biome.Biomes.TAIGA)
                || biome.is(net.minecraft.world.level.biome.Biomes.FROZEN_RIVER)
                || biome.is(net.minecraft.world.level.biome.Biomes.JUNGLE)
                || biome.is(net.minecraft.world.level.biome.Biomes.DARK_FOREST);
    }
}
