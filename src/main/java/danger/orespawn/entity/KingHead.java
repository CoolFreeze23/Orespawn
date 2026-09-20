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
 * <p>In 1.7.10 OreSpawn ({@code reference_1_7_10_source/sources/danger/orespawn/KingHead.java}),
 * The King's "head" was a separate {@code EntityLiving} spawned at
 * {@code parent.y + 20} and teleported itself to
 * {@code (parent.x − 30·sin(yaw), parent.y + 12, parent.z + 30·cos(yaw))}
 * every tick. Damage was forwarded via an AABB search for the nearest
 * {@code TheKing}.</p>
 *
 * <p><b>Retired in the 1.21.1 port (BOSS-047, hit boxes follow the rigs).</b> {@link TheKing} no longer spawns one: the
 * King's heads are bone-synced MultiHitboxLib parts of the King's own profile ({@code
 * data/orespawn/multihitboxlib/hitbox_profiles/the_king.json}), on the drawn heads, in place of this box at a fixed
 * gaze offset. The type stays registered so an old save's head still decodes; one that loads discards itself on its
 * first server tick ({@link #tick()}). A bypassing hit ({@code /kill}, ENT-S-172) kills it and reaches the body.</p>
 *
 * @deprecated retired by BOSS-047; kept for save compatibility only.
 */
@Deprecated
public class KingHead extends Mob {

    public KingHead(EntityType<? extends KingHead> type, Level level) {
        super(type, level);
        // Sidecar flies freely — no gravity, no block collisions.
        this.noPhysics = true;
        this.xpReward = 0;
    }

    @Override
    protected void registerGoals() {}

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 6000.0)
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
            List<TheKing> bodies = this.level().getEntitiesOfClass(TheKing.class, this.getBoundingBox().inflate(48.0, 32.0, 48.0));
            if (!bodies.isEmpty()) bodies.get(0).hurt(source, amount);
            return died;
        }
        if (source.getMsgId().equals("inWall")) return false;
        Entity attacker = source.getEntity();
        if (attacker instanceof TheKing || attacker instanceof KingHead) return false;
        Entity direct = source.getDirectEntity();
        if (direct instanceof TheKing || direct instanceof KingHead) return false;

        AABB searchBox = this.getBoundingBox().inflate(48.0, 32.0, 48.0);
        List<TheKing> kings = this.level().getEntitiesOfClass(TheKing.class, searchBox);
        if (!kings.isEmpty()) {
            return kings.get(0).hurt(source, amount);
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
        // BOSS-047: the sidecar box is retired - the King's heads are bone-synced MHLib parts of the
        // body's own profile, on the drawn heads. The type stays registered for old saves; one that loads
        // discards itself here.
        if (!this.level().isClientSide()) {
            this.discard();
            return;
        }
        this.noPhysics = true;
        this.clearFire();
        if (!this.level().isClientSide()) {
            AABB searchBox = this.getBoundingBox().inflate(32.0, 32.0, 32.0);
            List<TheKing> kings = this.level().getEntitiesOfClass(TheKing.class, searchBox);
            if (!kings.isEmpty()) {
                TheKing king = kings.get(0);
                // BOSS-003: orig KingHead.java:147-149 tracks field_70759_as
                // (yHeadRot), not the body rotation — the head box leads the
                // gaze, not the torso.
                this.setPos(
                        king.getX() - 30.0 * Math.sin(Math.toRadians(king.getYHeadRot())),
                        king.getY() + 12.0,
                        king.getZ() + 30.0 * Math.cos(Math.toRadians(king.getYHeadRot()))
                );
                this.setYRot(king.getYRot());
                this.yBodyRot = king.yBodyRot;
                this.setHealth(king.getHealth());
            } else {
                this.discard();
            }
        }
        super.tick();
    }

    /**
     * orig KingHead.java:47-49 ({@code func_70692_ba}, canDespawn): {@code return false;} — never despawns, where the 1.21 Mob default (and EntityLiving's) would. ENT-S-171.
     */
    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
    }
}
