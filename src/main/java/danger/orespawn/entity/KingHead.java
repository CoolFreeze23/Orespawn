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

public class KingHead extends Mob {

    public KingHead(EntityType<? extends KingHead> type, Level level) {
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
