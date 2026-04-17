package danger.orespawn;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

/**
 * Per-world persistent flag mirroring 1.7.10 OreSpawn's
 * {@code OreSpawnMain.godzilla_has_spawned} static int.
 *
 * <p>In 1.7.10, that flag was a global JVM-wide static that prevented more than
 * one Mobzilla from naturally spawning per server session. We promote it to a
 * proper {@link SavedData} so it survives world restarts and is scoped to the
 * Overworld save folder, matching the original "one Mobzilla per world" intent
 * without leaking between save files.</p>
 *
 * <p>Players can still summon additional Mobzillas via the 9 Ancient Dried
 * Mobzilla Egg Parts (the spawn item bypasses {@link #hasSpawned}), so this
 * gate only affects {@link Godzilla#checkSpawnRules ambient spawn attempts}.</p>
 */
public class MobzillaSpawnTracker extends SavedData {
    private static final String DATA_NAME = OreSpawnMod.MOD_ID + "_mobzilla_spawn_tracker";
    private static final String KEY_HAS_SPAWNED = "HasSpawned";

    private boolean hasSpawned = false;

    public boolean hasSpawned() {
        return this.hasSpawned;
    }

    public void markSpawned() {
        if (!this.hasSpawned) {
            this.hasSpawned = true;
            this.setDirty();
        }
    }

    public void reset() {
        if (this.hasSpawned) {
            this.hasSpawned = false;
            this.setDirty();
        }
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        tag.putBoolean(KEY_HAS_SPAWNED, this.hasSpawned);
        return tag;
    }

    public static MobzillaSpawnTracker load(CompoundTag tag, HolderLookup.Provider registries) {
        MobzillaSpawnTracker tracker = new MobzillaSpawnTracker();
        tracker.hasSpawned = tag.getBoolean(KEY_HAS_SPAWNED);
        return tracker;
    }

    public static SavedData.Factory<MobzillaSpawnTracker> factory() {
        return new SavedData.Factory<>(MobzillaSpawnTracker::new, MobzillaSpawnTracker::load, null);
    }

    /**
     * Resolves (or creates) the tracker for this world. Always queries the
     * Overworld so summons or spawns from any dimension share the same flag —
     * this matches the 1.7.10 behavior where the flag was JVM-global.
     */
    public static MobzillaSpawnTracker get(ServerLevel level) {
        ServerLevel overworld = level.getServer().overworld();
        return overworld.getDataStorage().computeIfAbsent(factory(), DATA_NAME);
    }
}
