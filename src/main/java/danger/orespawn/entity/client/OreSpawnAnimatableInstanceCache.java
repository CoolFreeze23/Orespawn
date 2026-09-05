package danger.orespawn.entity.client;

import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.animatable.instance.SingletonAnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;

/**
 * OPT-029 (Phase G slice (a), 2026-09-05): GeckoLib's
 * {@link SingletonAnimatableInstanceCache} with eviction.
 *
 * <p>This is the class GeckoLib 4.8.4 picks for a replaced animatable, made
 * explicit. A {@code GeoReplacedEntity} is a {@code SingletonGeoAnimatable}, whose
 * {@code animatableCacheOverride()} returns {@code new SingletonAnimatableInstanceCache(this)}
 * (javap over the 4.8.4 jar: offsets 0-8), and
 * {@code GeckoLibUtil.createInstanceCache(GeoAnimatable)} hands that override back
 * when it is non-null (offsets 1-12) before its Entity / BlockEntity test (15-35:
 * singleton when neither, instanced otherwise) -- which a replacement therefore
 * never reaches (refuter A, 2026-09-06). Either path gives the singleton cache.</p>
 *
 * <p>The singleton cache keeps one {@link AnimatableManager} per animatable
 * instance id in {@code protected final Long2ObjectMap<AnimatableManager<?>>
 * managers} (a {@code Long2ObjectOpenHashMap}, constructor offsets 6-13), and
 * {@code getManagerForId(long)} is {@code if (!managers.containsKey(id))
 * managers.put(id, new AnimatableManager(animatable)); return managers.get(id);}
 * (offsets 0-48). Nothing in GeckoLib ever removes an entry. For a replaced
 * entity the id is the entity id ({@code GeoReplacedEntityRenderer.getInstanceId}
 * returns {@code currentEntity.getId()}, offsets 0-8), so every entity ever drawn
 * by a replaced renderer kept a manager -- its {@code boneSnapshotCollection}
 * (one {@code BoneSnapshot} per animated bone), {@code animationControllers},
 * {@code extraData} and the timing fields -- for the whole client session
 * (FIX_LOG.md:3761-3764, the Slice 2 open item).</p>
 *
 * <p>Eviction is safe because a manager is state GeckoLib rebuilds on demand:
 * the next draw of that id ({@code GeoModel.handleAnimations}, offset 12) calls
 * {@code getManagerForId} again, finds no entry and creates a fresh manager whose
 * first tick re-snapshots the bones -- exactly what an entity that has never been
 * seen pays. An entity that leaves and re-enters render distance leaves and
 * re-joins the client level (a new {@code EntityLeaveLevelEvent} / join cycle,
 * with a new client {@code Entity} instance under the same network id), so
 * evicting on leave costs one manager construction and one first-tick snapshot
 * per re-entry and nothing else. The map is touched only from the client main
 * thread; the argument is written down on {@link GeoReplacementCaches}.</p>
 */
public final class OreSpawnAnimatableInstanceCache extends SingletonAnimatableInstanceCache {

    public OreSpawnAnimatableInstanceCache(GeoAnimatable animatable) {
        super(animatable);
    }

    /** Drops the manager held for {@code entityId}; {@code true} when one was held. */
    public boolean evict(long entityId) {
        return this.managers.remove(entityId) != null;
    }

    /** Drops every manager; returns how many were held. */
    public int evictAll() {
        int held = this.managers.size();
        this.managers.clear();
        return held;
    }

    /** Managers currently held: one per entity id drawn since that id was last evicted. */
    public int size() {
        return this.managers.size();
    }
}
