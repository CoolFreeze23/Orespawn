package danger.orespawn.entity;

import java.util.List;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

/**
 * <b>Legacy 1.7.10 sidecar entity.</b>
 *
 * <p>In 1.7.10 OreSpawn ({@code reference_1_7_10_source/sources/danger/orespawn/GodzillaHead.java}),
 * the "Mobzilla" boss (class name {@link Godzilla}) spawned a separate
 * {@code GodzillaHead} entity that teleported to {@code (parent.x − 17·sin(yaw),
 * parent.y + 16, parent.z + 17·cos(yaw))} every tick and served as a proxy
 * hitbox for players attacking the boss's head. Damage received was
 * re-routed to the parent via an AABB search for the nearest {@link Godzilla}.</p>
 *
 * <p><b>Retired in the 1.21.1 port (BOSS-047, hit boxes follow the rigs).</b> {@link Godzilla} no longer spawns one:
 * Godzilla's head is a bone-synced MultiHitboxLib part of Godzilla's own profile ({@code
 * data/orespawn/multihitboxlib/hitbox_profiles/godzilla.json}), on the drawn head, in place of this box at a fixed
 * gaze offset. The type stays registered so an old save's head still decodes; one that loads discards itself on its
 * first server tick ({@link #tick()}). A bypassing hit ({@code /kill}, ENT-S-172) kills it and reaches the body.</p>
 *
 * @deprecated retired by BOSS-047; kept for save compatibility only.
 */
@Deprecated
public class GodzillaHead extends Mob {

    public GodzillaHead(EntityType<? extends GodzillaHead> type, Level level) {
        super(type, level);
        this.noPhysics = true;
        this.xpReward = 0;
    }

    @Override
    protected void registerGoals() {}

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 4000.0)
                .add(Attributes.MOVEMENT_SPEED, 1.33)
                .add(Attributes.ATTACK_DAMAGE, 0.0);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        // ENT-S-172: damage that bypasses invulnerability - /kill, the void - kills the head itself AND reaches the
        // body in full, never capped or gated (a head that died alone would be revived by its own health mirror a
        // tick later, tick()): vanilla's own gates yield to this tag (Entity.isInvulnerableTo's Invulnerable flag, the
        // totem's checkTotemDeathProtection, WitherBoss.hurt's spawn-armour gate; the Ender Dragon answers /kill in
        // its own kill() override). 1.7.10's /kill could not name a mob, and its void source ran through the body's
        // timers, so this clause is the port's, not a transcription.
        if (source.is(net.minecraft.tags.DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            boolean died = super.hurt(source, amount);
            List<Godzilla> bodies = this.level().getEntitiesOfClass(Godzilla.class, this.getBoundingBox().inflate(32.0, 32.0, 32.0));
            if (!bodies.isEmpty()) bodies.get(0).hurt(source, amount);
            return died;
        }
        if (source.getMsgId().equals("inWall")) return false;
        Entity attacker = source.getEntity();
        if (attacker instanceof Godzilla || attacker instanceof GodzillaHead) return false;
        Entity direct = source.getDirectEntity();
        if (direct instanceof Godzilla || direct instanceof GodzillaHead) return false;

        AABB searchBox = this.getBoundingBox().inflate(32.0, 32.0, 32.0);
        List<Godzilla> godzillas = this.level().getEntitiesOfClass(Godzilla.class, searchBox);
        if (!godzillas.isEmpty()) {
            return godzillas.get(0).hurt(source, amount);
        }
        return false;
    }

    @Override
    public boolean causeFallDamage(float dist, float mult, DamageSource source) {
        return false;
    }

    @Override
    public boolean isPickable() {
        return true;
    }

    @Override
    public boolean isPushable() {
        return true;
    }

    @Override
    public void tick() {
        if (this.isRemoved()) return;
        // BOSS-047: the sidecar box is retired - the Godzilla's head is bone-synced MHLib parts of the
        // body's own profile, on the drawn head. The type stays registered for old saves; one that loads
        // discards itself here.
        if (!this.level().isClientSide()) {
            this.discard();
            return;
        }
        this.noPhysics = true;
        this.clearFire();
        if (!this.level().isClientSide()) {
            AABB searchBox = this.getBoundingBox().inflate(32.0, 32.0, 32.0);
            List<Godzilla> godzillas = this.level().getEntitiesOfClass(Godzilla.class, searchBox);
            if (!godzillas.isEmpty()) {
                Godzilla godzilla = godzillas.get(0);
                // BOSS-014: orig GodzillaHead.java:147-149 tracks yHeadRot.
                this.setPos(
                        godzilla.getX() - 17.0 * Math.sin(Math.toRadians(godzilla.getYHeadRot())),
                        godzilla.getY() + 16.0,
                        godzilla.getZ() + 17.0 * Math.cos(Math.toRadians(godzilla.getYHeadRot()))
                );
                this.setYRot(godzilla.getYRot());
                this.yBodyRot = godzilla.yBodyRot;
                this.setHealth(godzilla.getHealth());
            } else {
                this.discard();
            }
        }
        super.tick();
    }

    /**
     * orig GodzillaHead.java:47-49 ({@code func_70692_ba}, canDespawn): {@code return false;} — never despawns, where the 1.21 Mob default (and EntityLiving's) would. ENT-S-171.
     */
    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
    }
}
