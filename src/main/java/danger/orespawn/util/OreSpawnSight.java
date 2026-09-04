package danger.orespawn.util;

import danger.orespawn.OreSpawnMod;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * ENT-S-121 — the port-wide line-of-sight convention (owner's ruling 2026-09-04: "adopt the 1.7.10 convention
 * port-wide, not per site").
 *
 * <p>1.7.10's {@code EntityLivingBase.canEntityBeSeen} was {@code worldObj.rayTraceBlocks(eyes, targetEyes) == null}:
 * the two-argument overload, which forwards {@code (stopOnLiquid = false, ignoreBlockWithoutBoundingBox = false,
 * returnLastUncollidableBlock = false)} — liquids never stop the ray and every collidable block is tested on its
 * selection bounds. In vanilla 1.21.1 terms that is {@link ClipContext.Block#OUTLINE} with
 * {@link ClipContext.Fluid#NONE} (the mapping ENT-S-089 recorded for the Vortex's feet helper and ENT-S-118 for four
 * more), where vanilla's {@code LivingEntity.hasLineOfSight} clips {@code COLLIDER}: a target behind short grass, a
 * flower, a torch or any other collision-less block is seen by vanilla's ray and hidden by 1.7.10's.</p>
 *
 * <p>{@code danger.orespawn.mixin.LivingEntitySightMixin} routes every {@code hasLineOfSight} whose receiver is an
 * OreSpawn entity ({@link #isOreSpawn}: the registry namespace, no per-class marker) to {@link #canSee}, so every
 * {@code getSensing().hasLineOfSight} / {@code hasLineOfSight} site of the port's hunters whose receiver is an
 * OreSpawn entity, and every vanilla goal on an OreSpawn mob, reads the 1.7.10 ray; the one player-receiver site
 * (EnderReaper.java:82, orig :92 {@code player.canEntityBeSeen(this)}) calls {@link #canSee} directly; vanilla mobs
 * keep vanilla's (a vanilla receiver's ray toward an OreSpawn mob included; 1.7.10 skipped fire where OUTLINE hits
 * its thin slabs — a residual of the mapping), and {@code Sensing}'s per-tick cache (ENT-S-122's
 * memo plan sits above it) is untouched. The five feet-level helpers that clip a block ray from {@code getY() + 0.75}
 * (ThePrinceAdult / ThePrinceTeen {@code canSeeSpot}, Kraken / EntityBrutalfly / Cockateil {@code canSeeTarget}) carry
 * the same mapping inline.</p>
 */
public final class OreSpawnSight {

    /** Vanilla's range bound, kept: 1.7.10's only bound was a 200-step cap that answered SEEN, vanilla's 128 blocks
     *  answer unseen, and no hunter of the port scans that far (ENT-S-121 refuter A). */
    public static final double MAX_RANGE = 128.0;

    private OreSpawnSight() {
    }

    /** True for an entity registered under the {@code orespawn} namespace — the convention's receiver gate. */
    @SuppressWarnings("deprecation") // the intrusive holder is bound at registration; the same read GameTestHelper.spawn makes
    public static boolean isOreSpawn(Entity entity) {
        return OreSpawnMod.MOD_ID.equals(entity.getType().builtInRegistryHolder().key().location().getNamespace());
    }

    /**
     * 1.7.10's {@code canEntityBeSeen}: eye to eye, every block on its selection bounds, liquids ignored; vanilla's
     * same-level and 128-block bounds kept (NeoForm LivingEntity.java:3033-3043 with {@code OUTLINE} for {@code COLLIDER}).
     */
    public static boolean canSee(LivingEntity hunter, Entity target) {
        if (target.level() != hunter.level()) {
            return false;
        }
        Vec3 eye = new Vec3(hunter.getX(), hunter.getEyeY(), hunter.getZ());
        Vec3 targetEye = new Vec3(target.getX(), target.getEyeY(), target.getZ());
        if (targetEye.distanceTo(eye) > MAX_RANGE) {
            return false;
        }
        return hunter.level().clip(new ClipContext(eye, targetEye, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, hunter))
                .getType() == HitResult.Type.MISS;
    }
}
