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
 * <p><b>Obsoleted in the 1.21.1 port.</b> {@link Godzilla} now carries a proper
 * {@link net.neoforged.neoforge.entity.PartEntity} array (see
 * {@link OreSpawnPartEntity}), so hit detection is handled by the engine,
 * rendering interpolates correctly, and damage forwarding is O(1) instead
 * of an AABB query per tick.</p>
 *
 * <p>Retained solely for:
 * <ol>
 *   <li><b>NBT backward compatibility</b> — old saves that still contain an
 *       {@code "orespawn:godzilla_head"} entity must still decode without
 *       error.</li>
 *   <li><b>Flight-pattern hook parity</b> — AI goals spawned from
 *       {@link Godzilla} may still target this head for legacy scripted
 *       dialogue/pattern hooks. Once the PartEntity framework is proven,
 *       the spawn call and this class can be removed together.</li>
 * </ol>
 *
 * <p>Do not add new functionality here — put it on {@link Godzilla} and its
 * {@link OreSpawnPartEntity} children instead.</p>
 *
 * @deprecated Use {@link OreSpawnPartEntity} on {@link Godzilla} instead.
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
        this.noPhysics = true;
        this.clearFire();
        if (!this.level().isClientSide()) {
            AABB searchBox = this.getBoundingBox().inflate(32.0, 32.0, 32.0);
            List<Godzilla> godzillas = this.level().getEntitiesOfClass(Godzilla.class, searchBox);
            if (!godzillas.isEmpty()) {
                Godzilla godzilla = godzillas.get(0);
                this.setPos(
                        godzilla.getX() - 17.0 * Math.sin(Math.toRadians(godzilla.yBodyRot)),
                        godzilla.getY() + 16.0,
                        godzilla.getZ() + 17.0 * Math.cos(Math.toRadians(godzilla.yBodyRot))
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
}
