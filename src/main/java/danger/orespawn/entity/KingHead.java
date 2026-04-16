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
 * <p><b>Obsoleted in the 1.21.1 port.</b> {@link TheKing} now carries a
 * proper {@link net.neoforged.neoforge.entity.PartEntity} array — see
 * {@link OreSpawnPartEntity} — so hit detection is handled by the engine,
 * client-side rendering interpolates correctly, and damage forwarding is
 * O(1) instead of an AABB query.</p>
 *
 * <p>This class is kept only for:
 * <ol>
 *   <li><b>NBT backward compatibility</b>: old saves with a {@code "orespawn:king_head"}
 *       entity in the world will still load without error.</li>
 *   <li><b>AI-hook parity</b>: {@link TheKing#customServerAiStep()} still
 *       spawns one, mirroring the 1.7.10 flight pattern where enemies
 *       targeting "the head" pull Queen's flight path upward. Once the
 *       PartEntity framework is proven in playtesting, the spawn call and
 *       this class can be removed together.</li>
 * </ol>
 *
 * <p>Do not add new functionality here — put it on {@link TheKing} and its
 * {@link OreSpawnPartEntity} children instead.</p>
 *
 * @deprecated superseded by {@link OreSpawnPartEntity} on {@link TheKing};
 *             kept for save compatibility only.
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
        this.noPhysics = true;
        this.clearFire();
        if (!this.level().isClientSide()) {
            AABB searchBox = this.getBoundingBox().inflate(32.0, 32.0, 32.0);
            List<TheKing> kings = this.level().getEntitiesOfClass(TheKing.class, searchBox);
            if (!kings.isEmpty()) {
                TheKing king = kings.get(0);
                this.setPos(
                        king.getX() - 30.0 * Math.sin(Math.toRadians(king.yBodyRot)),
                        king.getY() + 12.0,
                        king.getZ() + 30.0 * Math.cos(Math.toRadians(king.yBodyRot))
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
}
