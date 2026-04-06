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
        Entity e = source.getEntity();
        if (e instanceof Godzilla || e instanceof GodzillaHead) return false;
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
