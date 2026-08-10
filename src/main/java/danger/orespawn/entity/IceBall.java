package danger.orespawn.entity;

import danger.orespawn.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class IceBall extends LaserBall {
    private boolean createIce = false;

    public IceBall(EntityType<? extends ThrowableProjectile> type, Level level) {
        super(type, level);
        this.setIceBall();
    }

    public IceBall(Level level, LivingEntity shooter) {
        super(danger.orespawn.ModEntities.ICE_BALL.get(), shooter, level);
        this.setIceBall();
    }

    // orig MyDispenserBehaviorIceball.java:29-31 — dispensers spawn iceballs at a bare position
    public IceBall(Level level, double x, double y, double z) {
        super(danger.orespawn.ModEntities.ICE_BALL.get(), level, x, y, z);
        this.setIceBall();
    }

    public void enableIceCreation() {
        this.createIce = true;
    }

    /**
     * ENTITY_NOOP_RENDERER/ice_ball — orig RenderItemUrchin drew spinner tile 84
     * (orig IceBall.java:16 my_index=84), the ice_ball item sprite
     * (textures/items/iceball.png); feeds vanilla ThrownItemRenderer.
     */
    @Override
    public ItemStack getItem() {
        return new ItemStack(ModItems.ICE_BALL.get());
    }

    @Override
    protected void onHit(HitResult result) {
        if (!this.level().isClientSide && this.createIce) {
            Vec3 pos = result.getLocation();
            for (int i = 0; i < 5; i++) {
                int dx = this.random.nextInt(4) * (this.random.nextBoolean() ? 1 : -1);
                int dy = this.random.nextInt(4) * (this.random.nextBoolean() ? 1 : -1);
                int dz = this.random.nextInt(4) * (this.random.nextBoolean() ? 1 : -1);
                BlockPos target = BlockPos.containing(pos.x + dx, pos.y + dy, pos.z + dz);
                if (this.level().isEmptyBlock(target)) {
                    this.level().setBlockAndUpdate(target, Blocks.ICE.defaultBlockState());
                }
            }
        }
        super.onHit(result);
    }
}