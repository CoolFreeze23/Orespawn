package danger.orespawn.world;

import danger.orespawn.OreSpawnMod;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.SectionPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.loot.LootTable;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * BUG-021 (GitHub #6): the blocks a decoration-time builder writes into a chunk it may not write to. A 1.21 chunk's
 * decoration may write only into itself and the eight chunks around it (WorldGenRegion's write radius), and drops
 * everything farther, so the Crystal dimension's Fairy Castle Trees, whose platforms reach 25 to 42 blocks from their
 * trunk, were sheared at a chunk edge. 1.7.10 wrote the same blocks straight into the neighbouring chunks during
 * population. Here the write is kept for its chunk instead, and lands there the first time it can: at the start of that
 * chunk's own decoration ({@link #applyPending}, before its own features, as the original's block was there before
 * that chunk populated), or, for a chunk that has finished generating, on the server thread once it is loaded
 * ({@link #tick}). Kept per dimension in the save, so a restart loses none.
 */
@EventBusSubscriber(modid = OreSpawnMod.MOD_ID)
public final class DeferredWrites {
    private static final String DATA_NAME = OreSpawnMod.MOD_ID + "_deferred_worldgen_writes";
    /** The most chunks one server tick applies, so a backlog never stalls a tick. */
    private static final int CHUNKS_PER_TICK = 16;
    /** The chunks around its own that a FEATURES-step region may write (ChunkPyramid: blockStateWriteRadius(1)). */
    private static final int FEATURES_WRITE_RADIUS = 1;
    private static final Map<ResourceKey<Level>, Data> DATA = new ConcurrentHashMap<>();

    private DeferredWrites() {
    }

    /** One kept write: a block, laid only on air when {@code onlyIfAir}; a chest with its loot table; a spawner with its mob. */
    public record Write(BlockPos pos, BlockState state, boolean onlyIfAir, ResourceKey<LootTable> loot, EntityType<?> spawner) {
    }

    /** A block written now when the chunk may take it, else kept for its chunk. */
    public static void setBlock(WorldGenLevel level, BlockPos pos, BlockState state, boolean onlyIfAir) {
        write(level, new Write(pos.immutable(), state, onlyIfAir, null, null));
    }

    /** A chest with a loot table, written now or kept. */
    public static void chest(WorldGenLevel level, BlockPos pos, ResourceKey<LootTable> loot) {
        write(level, new Write(pos.immutable(), Blocks.CHEST.defaultBlockState(), false, loot, null));
    }

    /** A spawner of a mob, written now or kept. */
    public static void spawner(WorldGenLevel level, BlockPos pos, EntityType<?> mob) {
        write(level, new Write(pos.immutable(), Blocks.SPAWNER.defaultBlockState(), false, null, mob));
    }

    private static void write(WorldGenLevel level, Write w) {
        if (w.pos().getY() < level.getMinBuildHeight() || w.pos().getY() >= level.getMaxBuildHeight()) return;
        if (canWriteNow(level, w.pos())) {
            apply(level, w);
            return;
        }
        data(level.getLevel()).add(new ChunkPos(w.pos()), w);
    }

    /**
     * A region's own write test, made without asking {@link WorldGenRegion#ensureCanWrite} about a far chunk: that
     * logs an error for every far position ("Detected setBlock in a far chunk"), and a far write here is expected.
     */
    private static boolean canWriteNow(WorldGenLevel level, BlockPos pos) {
        if (level instanceof WorldGenRegion region) {
            ChunkPos center = region.getCenter();
            if (Math.abs(center.x - SectionPos.blockToSectionCoord(pos.getX())) > FEATURES_WRITE_RADIUS
                    || Math.abs(center.z - SectionPos.blockToSectionCoord(pos.getZ())) > FEATURES_WRITE_RADIUS) {
                return false;
            }
        }
        return level.ensureCanWrite(pos);
    }

    /** The writes kept for {@code chunk}, laid now: the chunk is being decorated, so it may take them. */
    public static void applyPending(WorldGenLevel level, ChunkPos chunk) {
        List<Write> writes = data(level.getLevel()).take(chunk);
        for (Write w : writes) apply(level, w);
    }

    /** Server thread, every tick of a level: the kept writes of chunks that have finished generating and are loaded. */
    public static void tick(ServerLevel level) {
        Data data = DATA.get(level.dimension());
        if (data == null || data.isEmpty()) return;
        int done = 0;
        for (long key : data.chunks()) {
            if (done >= CHUNKS_PER_TICK) break;
            ChunkPos chunk = new ChunkPos(key);
            if (level.getChunkSource().getChunkNow(chunk.x, chunk.z) == null) continue;
            for (Write w : data.take(chunk)) apply(level, w);
            done++;
        }
    }

    /** How many writes are kept for a level (the tests read it). */
    public static int pending(ServerLevel level) {
        return data(level).size();
    }

    /** Keeps a write for its chunk, as a builder outside the write radius does (the tests use it directly). */
    public static void keep(ServerLevel level, Write w) {
        data(level).add(new ChunkPos(w.pos()), w);
    }

    private static void apply(WorldGenLevel level, Write w) {
        if (w.onlyIfAir() && !level.getBlockState(w.pos()).isAir()) return;
        level.setBlock(w.pos(), w.state(), 2);
        if (w.loot() != null && level.getBlockEntity(w.pos()) instanceof RandomizableContainerBlockEntity chest) {
            chest.setLootTable(w.loot());
        }
        if (w.spawner() != null && level.getBlockEntity(w.pos()) instanceof SpawnerBlockEntity spawner) {
            spawner.getSpawner().setEntityId(w.spawner(), null, level.getRandom(), w.pos());
        }
    }

    private static Data data(ServerLevel level) {
        return DATA.computeIfAbsent(level.dimension(), key -> {
            synchronized (DeferredWrites.class) {
                return level.getDataStorage().computeIfAbsent(Data.factory(), DATA_NAME);
            }
        });
    }

    /** Forgets the cached data of a level that unloads (the next use reads it from the save again). */
    public static void unload(ServerLevel level) {
        DATA.remove(level.dimension());
    }

    /**
     * Reads a level's kept writes on the server thread as it loads, before any of its chunks generate, so the
     * decoration threads only ever find the cached data and never touch the level's data storage themselves.
     */
    @SubscribeEvent
    public static void onLevelLoad(LevelEvent.Load event) {
        if (event.getLevel() instanceof ServerLevel level) data(level);
    }

    @SubscribeEvent
    public static void onLevelTick(LevelTickEvent.Post event) {
        if (event.getLevel() instanceof ServerLevel level) tick(level);
    }

    @SubscribeEvent
    public static void onLevelUnload(LevelEvent.Unload event) {
        if (event.getLevel() instanceof ServerLevel level) unload(level);
    }

    /** The kept writes of one dimension, by chunk; every method synchronised (decoration runs on many threads). */
    public static final class Data extends SavedData {
        private final Map<Long, List<Write>> byChunk = new HashMap<>();

        public synchronized void add(ChunkPos chunk, Write w) {
            byChunk.computeIfAbsent(chunk.toLong(), k -> new ArrayList<>()).add(w);
            setDirty();
        }

        public synchronized List<Write> take(ChunkPos chunk) {
            List<Write> out = byChunk.remove(chunk.toLong());
            if (out == null) return List.of();
            setDirty();
            return out;
        }

        synchronized boolean isEmpty() {
            return byChunk.isEmpty();
        }

        synchronized long[] chunks() {
            return byChunk.keySet().stream().mapToLong(Long::longValue).toArray();
        }

        public synchronized int size() {
            return byChunk.values().stream().mapToInt(List::size).sum();
        }

        @Override
        public synchronized CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
            ListTag list = new ListTag();
            for (List<Write> writes : byChunk.values()) {
                for (Write w : writes) {
                    CompoundTag t = new CompoundTag();
                    t.putLong("pos", w.pos().asLong());
                    t.put("state", NbtUtils.writeBlockState(w.state()));
                    if (w.onlyIfAir()) t.putBoolean("air", true);
                    if (w.loot() != null) t.putString("loot", w.loot().location().toString());
                    if (w.spawner() != null) t.putString("mob", BuiltInRegistries.ENTITY_TYPE.getKey(w.spawner()).toString());
                    list.add(t);
                }
            }
            tag.put("writes", list);
            return tag;
        }

        public static Data load(CompoundTag tag, HolderLookup.Provider registries) {
            Data data = new Data();
            ListTag list = tag.getList("writes", Tag.TAG_COMPOUND);
            for (int i = 0; i < list.size(); i++) {
                CompoundTag t = list.getCompound(i);
                BlockPos pos = BlockPos.of(t.getLong("pos"));
                BlockState state = NbtUtils.readBlockState(registries.lookupOrThrow(Registries.BLOCK), t.getCompound("state"));
                ResourceKey<LootTable> loot = t.contains("loot")
                        ? ResourceKey.create(Registries.LOOT_TABLE, ResourceLocation.parse(t.getString("loot"))) : null;
                EntityType<?> mob = t.contains("mob")
                        ? BuiltInRegistries.ENTITY_TYPE.getOptional(ResourceLocation.parse(t.getString("mob"))).orElse(null) : null;
                data.byChunk.computeIfAbsent(new ChunkPos(pos).toLong(), k -> new ArrayList<>())
                        .add(new Write(pos, state, t.getBoolean("air"), loot, mob));
            }
            return data;
        }

        public static SavedData.Factory<Data> factory() {
            return new SavedData.Factory<>(Data::new, Data::load, null);
        }
    }
}
