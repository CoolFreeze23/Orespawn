package danger.orespawn.entity;

import danger.orespawn.ModEntities;
import danger.orespawn.ModItems;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class Acid extends LaserBall {
    public Acid(EntityType<? extends ThrowableProjectile> type, Level level) {
        super(type, level);
        this.setAcid();
    }

    public Acid(Level level, LivingEntity shooter) {
        super(ModEntities.ACID.get(), shooter, level);
        this.setAcid();
    }

    // orig MyDispenserBehaviorAcid.java:29-31 — dispensers spawn acid at a bare position
    public Acid(Level level, double x, double y, double z) {
        super(ModEntities.ACID.get(), level, x, y, z);
        this.setAcid();
    }

    /**
     * ENTITY_NOOP_RENDERER/acid — orig RenderItemUrchin drew spinner tile 85
     * (orig Acid.java:12 my_index=85), pixel-identical to the acid item sprite
     * (textures/items/acid.png); feeds vanilla ThrownItemRenderer.
     */
    @Override
    public ItemStack getItem() {
        return new ItemStack(ModItems.ACID.get());
    }
}