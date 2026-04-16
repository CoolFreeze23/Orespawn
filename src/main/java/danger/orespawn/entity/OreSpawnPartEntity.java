package danger.orespawn.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.entity.PartEntity;

/**
 * Generic hittable sub-part for OreSpawn boss entities. One instance
 * represents a single named region of a boss's body (head, wing, tail
 * segment, etc.) and forwards damage to the parent, tagged with the
 * part identity so the parent can apply per-region multipliers.
 *
 * <h2>Paradigm shift: 1.7.10 "sidecar entity" → 1.21.1 {@link PartEntity}</h2>
 *
 * <p>In 1.7.10 OreSpawn, The King's head was implemented as a standalone
 * {@code EntityLiving} subclass ({@code KingHead.java:19-161}) that was
 * spawned separately from the boss and teleported itself to
 * {@code (parent.x - 30·sin(parent.yHeadRot), parent.y + 12,
 * parent.z + 30·cos(parent.yHeadRot))} every tick. Damage was forwarded by
 * running an AABB search around the head, locating the nearest
 * {@code TheKing}, and invoking {@code attackEntityFrom} on it. The pattern
 * was duplicated verbatim for {@code QueenHead} and {@code GodzillaHead}.</p>
 *
 * <p>That approach had three serious problems:
 * <ol>
 *   <li><b>Desync</b>: the head and body were two independent entities with
 *       independent client-tracking ranges. A player on the edge of the
 *       body's tracking range could see the body and not the head, or vice
 *       versa.</li>
 *   <li><b>Ghost parts</b>: if the parent died on the same tick the head was
 *       ticking, the head's AABB search failed to find the parent, so the
 *       head called {@code setDead()} on itself — but the "head gone, body
 *       still alive" race was also possible.</li>
 *   <li><b>Only one region per boss</b>: you could never have more than a
 *       single hittable sub-entity because the tracking/damage logic was
 *       1-to-1. Queen's three heads, wings, and tail were all folded into
 *       the main body hitbox.</li>
 * </ol>
 *
 * <p>NeoForge 1.21.1 provides first-class multi-part support through
 * {@link PartEntity}: parts are owned by the parent (not tracked as
 * standalone entities), their IDs are reserved contiguously by the parent's
 * {@code setId} override, and the client receives part-hit packets
 * correlated by those reserved IDs. Vanilla {@code EnderDragon} is the
 * reference implementation. We follow the same pattern.</p>
 *
 * <h2>Lifecycle</h2>
 * <ul>
 *   <li>Construct parts inside the parent's constructor. Each call
 *       increments {@code Entity.ENTITY_COUNTER} once, giving the part a
 *       unique id that the parent's {@code setId} override then reassigns
 *       to a contiguous block.</li>
 *   <li>The parent's {@code getParts()} must return a stable
 *       {@link PartEntity}{@code []} (don't build it each call).</li>
 *   <li>Position parts in the parent's {@code tick()} method, after
 *       {@code super.tick()} has advanced the parent. Remember to copy the
 *       previous-tick pose into {@code xo/yo/zo/xOld/yOld/zOld} so the
 *       client can interpolate — otherwise parts teleport on screen.</li>
 * </ul>
 *
 * <h2>Side/threading</h2>
 * <p>Parts live on both client and server. Their {@link #hurt} is invoked on
 * the server when the client sends an attack packet targeting the part's
 * reserved id. Never call {@link #hurt} from a world-gen thread or an
 * asynchronous task — the parent's {@code hurt} path runs goal-selector
 * callbacks that assume the server tick thread.</p>
 *
 * @param <T> the parent boss entity type
 */
public class OreSpawnPartEntity<T extends Entity> extends PartEntity<T> {

    private final String name;
    private final EntityDimensions size;

    public OreSpawnPartEntity(T parent, String name, float width, float height) {
        super(parent);
        this.name = name;
        this.size = EntityDimensions.scalable(width, height);
        this.refreshDimensions();
        // Parts can drift 30+ blocks from the parent's render anchor
        // (e.g. The Queen's outstretched wing tips). Without noCulling,
        // frustum culling uses the *part's own* AABB which is tiny relative
        // to the parent, causing the part to pop in/out of view whenever it
        // swings outside the parent's cull volume.
        this.noCulling = true;
        // Parts never take fire damage — the parent handles environmental
        // damage once, centrally, to avoid multiplying a single lava dip
        // by the number of parts.
        this.setRemainingFireTicks(0);
    }

    public String getPartName() {
        return this.name;
    }

    @Override
    public EntityDimensions getDimensions(Pose pose) {
        return this.size;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        // Parts do not own any synced data — all state lives on the parent.
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        // Parts are recreated by the parent's constructor on world load, so
        // there's nothing to restore here. See shouldBeSaved() = false.
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        // Parts never save (shouldBeSaved() = false); this method is only
        // present to satisfy the Entity contract.
    }

    /**
     * Parts must be ray-traceable so players can click them to attack.
     * This controls {@code pick()} / crosshair targeting, not entity-entity
     * collision (see {@link #canBeCollidedWith}).
     */
    @Override
    public boolean isPickable() {
        // Only pickable while the parent is alive. Once the parent dies, we
        // want incoming attacks to fall through to the ground / other mobs
        // instead of silently hitting a zombie hitbox.
        return this.getParent() != null && this.getParent().isAlive();
    }

    /**
     * Parts never participate in entity-entity push collision. The parent
     * handles pushing itself (usually via {@code noPhysics = true}). If we
     * returned true, players brushing a dangling tail segment would get
     * shoved even though the parent isn't moving.
     */
    @Override
    public boolean canBeCollidedWith() {
        return false;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    /**
     * Forwards damage to the parent. If the parent implements
     * {@link MultipartBoss}, the part identity is threaded through so the
     * parent can scale the damage per region (e.g. head = full damage, wing
     * = 25%). Otherwise the damage is forwarded verbatim.
     *
     * <p>1.7.10 parallel: {@code KingHead.func_70097_a} did an AABB search
     * every hit to find the parent. We skip that because {@link #getParent}
     * is O(1).</p>
     */
    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (this.isInvulnerableTo(source)) {
            return false;
        }
        T parent = this.getParent();
        if (parent == null || !parent.isAlive()) {
            // Orphaned part (parent removed mid-tick). Swallow the hit so
            // projectiles don't loop back to the entity that fired them.
            return false;
        }
        if (parent instanceof MultipartBoss boss) {
            return boss.hurtFromPart(this, source, amount);
        }
        return parent.hurt(source, amount);
    }

    /**
     * Treat the part and the parent as "the same entity" for the purposes
     * of vanilla code that checks entity identity (e.g. preventing a boss
     * from targeting its own head). 1.7.10 handled this by spamming
     * {@code instanceof TheKing || instanceof KingHead} checks everywhere;
     * this override removes the need for those.
     */
    @Override
    public boolean is(Entity entity) {
        return this == entity || this.getParent() == entity;
    }

    @Override
    public boolean shouldBeSaved() {
        // Parts are rebuilt from the parent's constructor on every world
        // load, so we must NOT write them into the chunk NBT — otherwise
        // we'd accumulate orphaned PartEntity instances on every reload.
        return false;
    }

    @Override
    public ItemStack getPickResult() {
        return this.getParent().getPickResult();
    }

    /**
     * Interface for boss entities that want per-part damage routing.
     * Implementing this lets different body regions have custom damage
     * multipliers (e.g. head = full damage, legs = 50%, wings = 25%).
     *
     * <p>Implementations should call {@code this.hurt(source, scaledAmount)}
     * — not some special "real" hurt path — so that all the usual hurt-time
     * bookkeeping (revenge targeting, mood flags, boss-bar updates) runs
     * exactly once per hit, regardless of which part was struck.</p>
     */
    public interface MultipartBoss {
        boolean hurtFromPart(OreSpawnPartEntity<?> part, DamageSource source, float amount);
    }
}
