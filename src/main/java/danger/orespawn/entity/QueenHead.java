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
 * <b>Legacy 1.7.10 sidecar entity.</b> See {@link KingHead} for the full
 * explanation of this pattern and why it's superseded in 1.21.1 by
 * {@link OreSpawnPartEntity}.
 *
 * <p>In the 1.7.10 original ({@code reference_1_7_10_source/sources/danger/orespawn/QueenHead.java}),
 * Queen's head was a standalone {@code EntityLiving} that teleported to
 * {@code (parent.x − 30·sin(yaw), parent.y + 12, parent.z + 30·cos(yaw))}
 * every tick and forwarded damage via AABB search. The modern port expresses
 * Queen's three heads as named {@link OreSpawnPartEntity} children of
 * {@link TheQueen} ({@code headL} / {@code headC} / {@code headR}) — see
 * {@link TheQueen#getParts()}.</p>
 *
 * <p>Retained for NBT backward compatibility and for
 * {@link TheQueen#customServerAiStep()}'s flight-pattern hook only.</p>
 *
 * @deprecated superseded by {@link OreSpawnPartEntity} on {@link TheQueen};
 *             kept for save compatibility only.
 */
@Deprecated
public class QueenHead extends Mob {

    public QueenHead(EntityType<? extends QueenHead> type, Level level) {
        super(type, level);
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
        if (source.getMsgId().equals("inWall")) return false;
        Entity attacker = source.getEntity();
        if (attacker instanceof TheQueen || attacker instanceof QueenHead) return false;
        Entity direct = source.getDirectEntity();
        if (direct instanceof TheQueen || direct instanceof QueenHead) return false;

        AABB searchBox = this.getBoundingBox().inflate(48.0, 32.0, 48.0);
        List<TheQueen> queens = this.level().getEntitiesOfClass(TheQueen.class, searchBox);
        if (!queens.isEmpty()) {
            return queens.get(0).hurt(source, amount);
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
            List<TheQueen> queens = this.level().getEntitiesOfClass(TheQueen.class, searchBox);
            if (!queens.isEmpty()) {
                TheQueen queen = queens.get(0);
                this.setPos(
                        queen.getX() - 30.0 * Math.sin(Math.toRadians(queen.yBodyRot)),
                        queen.getY() + 12.0,
                        queen.getZ() + 30.0 * Math.cos(Math.toRadians(queen.yBodyRot))
                );
                this.setYRot(queen.getYRot());
                this.yBodyRot = queen.yBodyRot;
                this.setHealth(queen.getHealth());
            } else {
                this.discard();
            }
        }
        super.tick();
    }
}
