package danger.orespawn.item;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.server.level.ServerLevel;
import danger.orespawn.ModBlocks;

public class ItemExperienceTreeSeed extends Item {
    public ItemExperienceTreeSeed(Item.Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        if (level.isClientSide) return InteractionResult.SUCCESS;

        Player player = context.getPlayer();
        if (player == null) return InteractionResult.PASS;

        BlockPos pos = context.getClickedPos();
        BlockState state = level.getBlockState(pos);
        boolean validBlock = state.is(Blocks.GRASS_BLOCK) || state.is(Blocks.DIRT) || state.is(Blocks.FARMLAND);
        if (!validBlock) return InteractionResult.FAIL;

        BlockPos above = pos.above();
        if (!level.isEmptyBlock(above)) return InteractionResult.FAIL;
        level.setBlock(above, ModBlocks.EXPERIENCE_PLANT.get().defaultBlockState(), 3);

        if (level instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER,
                    above.getX() + 0.5, above.getY() + 0.5, above.getZ() + 0.5, 10, 0.5, 0.5, 0.5, 0.1);
        }

        level.playSound(null, player.blockPosition(), SoundEvents.GRASS_PLACE, SoundSource.BLOCKS, 1.0F, 1.0F);
        context.getItemInHand().shrink(1);
        return InteractionResult.SUCCESS;
    }
}
