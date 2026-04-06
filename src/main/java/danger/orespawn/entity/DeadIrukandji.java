package danger.orespawn.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;

public class DeadIrukandji extends LaserBall {
    public DeadIrukandji(EntityType<? extends ThrowableProjectile> type, Level level) {
        super(type, level);
        this.setIrukandji();
    }

    public DeadIrukandji(Level level, LivingEntity shooter) {
        super(level, shooter);
        this.setIrukandji();
    }
}