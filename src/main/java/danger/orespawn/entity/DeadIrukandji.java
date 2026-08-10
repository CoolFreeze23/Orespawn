package danger.orespawn.entity;

import danger.orespawn.ModEntities;
import danger.orespawn.ModItems;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class DeadIrukandji extends LaserBall {
    public DeadIrukandji(EntityType<? extends ThrowableProjectile> type, Level level) {
        super(type, level);
        this.setIrukandji();
    }

    public DeadIrukandji(Level level, LivingEntity shooter) {
        super(ModEntities.DEAD_IRUKANDJI.get(), shooter, level);
        this.setIrukandji();
    }

    // orig MyDispenserBehaviorDeadIrukandji.java:29-31 — dispensers spawn at a bare position
    public DeadIrukandji(Level level, double x, double y, double z) {
        super(ModEntities.DEAD_IRUKANDJI.get(), level, x, y, z);
        this.setIrukandji();
    }

    /**
     * ENTITY_NOOP_RENDERER/dead_irukandji — orig RenderItemUrchin drew spinner
     * tile 86 (orig DeadIrukandji.java:12 my_index=86), the dead_irukandji item
     * sprite (textures/items/deadirukandji.png); feeds vanilla ThrownItemRenderer.
     */
    @Override
    public ItemStack getItem() {
        return new ItemStack(ModItems.DEAD_IRUKANDJI.get());
    }
}