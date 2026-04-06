package danger.orespawn.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import danger.orespawn.entity.EntityAnt;

/**
 * Salt block that damages ant entities on contact.
 */
public class OreSalt extends Block {
    public OreSalt(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public void stepOn(Level level, BlockPos pos, BlockState state, Entity entity) {
        damageAnt(level, entity);
        super.stepOn(level, pos, state, entity);
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        damageAnt(level, entity);
        super.entityInside(state, level, pos, entity);
    }

    private void damageAnt(Level level, Entity entity) {
        if (entity instanceof EntityAnt) {
            entity.hurt(level.damageSources().generic(), 5.0f);
        }
    }
}
