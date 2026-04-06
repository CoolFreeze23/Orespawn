package danger.orespawn.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;

public class Acid extends LaserBall {
    public Acid(EntityType<? extends ThrowableProjectile> type, Level level) {
        super(type, level);
        this.setAcid();
    }

    public Acid(Level level, LivingEntity shooter) {
        super(level, shooter);
        this.setAcid();
    }
}