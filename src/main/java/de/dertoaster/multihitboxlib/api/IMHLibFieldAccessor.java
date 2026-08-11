package de.dertoaster.multihitboxlib.api;

import de.dertoaster.multihitboxlib.entity.MHLibPartEntity;
import de.dertoaster.multihitboxlib.entity.hitbox.HitboxProfile;
import de.dertoaster.multihitboxlib.network.client.CPacketBoneInformation;
import de.dertoaster.multihitboxlib.util.BoneInformation;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.entity.PartEntity;
import org.apache.commons.lang3.NotImplementedException;

import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.UUID;

// DO NOT IMPLEMENT THIS INTERFACE!!
public interface IMHLibFieldAccessor<T extends LivingEntity> {

    public default PartEntity<?>[] _mhlibAccess_getPartArray() {
        throw new NotImplementedException();
    }

    public default void _mhlibAccess_setPartArray(final PartEntity<?>[] value) {
        throw new NotImplementedException();
    }

    public default Queue<UUID> _mhlibAccess_getTrackerQueue() {
        throw new NotImplementedException();
    }

    public default int _mhlibAccess_getTicksSinceLastSynch() {
        throw new NotImplementedException();
    }

    public default void _mhlibAccess_setTicksSinceLastSynch(int value) {
        throw new NotImplementedException();
    }

    public default Map<String, MHLibPartEntity<T>> _mhlibAccess_getPartMap() {
        throw new NotImplementedException();
    }

    public default void _mhlibAccess_setPartMap(Map<String, MHLibPartEntity<T>> value) {
        throw new NotImplementedException();
    }

    public default Map<String, BoneInformation> _mhlibAccess_getSynchMap() {
        throw new NotImplementedException();
    }

    public default UUID _mhlibAccess_getMasterUUID() {
        throw new NotImplementedException();
    }

    public default void _mhlibAccess_setMasterUUID(UUID value) {
        throw new NotImplementedException();
    }

    public default Optional<CPacketBoneInformation.Builder> _mlibAccess_getBoneInfoBuilder() {
        throw new NotImplementedException();
    }

    public default void _mlibAccess_setBoneInfoBuilder(Optional<CPacketBoneInformation.Builder> value) {
        throw new NotImplementedException();
    }

    // ──────────────────────────────────────────────────────────────────
    // OPT-001: per-entity hitbox-profile cache backing fields (see
    // IMultipartEntity.getHitboxProfile for the cache logic and
    // MHLibDatapackLoaders.invalidateProfileCache for the invalidation
    // story: the stored generation is compared against the global
    // profile-cache generation, which is bumped on every datapack
    // reload / server stop, forcing a re-resolve).
    // ──────────────────────────────────────────────────────────────────

    /** Cached profile, or {@code null} if never resolved for this entity. */
    public default Optional<HitboxProfile> _mhlibAccess_getCachedHitboxProfile() {
        throw new NotImplementedException();
    }

    /** Generation stamp captured when the cached profile was resolved. */
    public default int _mhlibAccess_getCachedHitboxProfileGeneration() {
        throw new NotImplementedException();
    }

    public default void _mhlibAccess_setCachedHitboxProfile(Optional<HitboxProfile> profile, int generation) {
        throw new NotImplementedException();
    }

    // ──────────────────────────────────────────────────────────────────
    // OPT-003 (ruled 2026-08-11): client-side change-only bone-streaming
    // state backing fields (see IMultipartEntity.updateSynching for the
    // throttle and its invalidation story). The last-sent map is the
    // boneInformation of the last CPacketBoneInformation actually sent —
    // null means "nothing sent yet" or "cache invalidated by a mastership
    // change" and always forces the next built payload out. The tick
    // counter is clamped at BONE_INFORMATION_KEEPALIVE_TICKS and forces a
    // keepalive re-send when it reaches that interval.
    // ──────────────────────────────────────────────────────────────────

    /** Bone map of the last {@code CPacketBoneInformation} actually sent, or {@code null}. */
    public default Map<String, BoneInformation> _mhlibAccess_getLastSentBoneInformation() {
        throw new NotImplementedException();
    }

    public default void _mhlibAccess_setLastSentBoneInformation(Map<String, BoneInformation> value) {
        throw new NotImplementedException();
    }

    public default int _mhlibAccess_getTicksSinceLastBoneInfoSend() {
        throw new NotImplementedException();
    }

    public default void _mhlibAccess_setTicksSinceLastBoneInfoSend(int value) {
        throw new NotImplementedException();
    }

}
