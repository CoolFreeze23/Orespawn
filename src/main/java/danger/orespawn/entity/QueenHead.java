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
