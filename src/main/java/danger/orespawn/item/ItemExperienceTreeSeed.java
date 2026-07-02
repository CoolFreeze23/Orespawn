package danger.orespawn.item;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import danger.orespawn.ModBlocks;

/**
 * orig ItemExperienceTreeSeed.java:25-42 — right-clicking grass/dirt/farmland
 * places an Experience Plant one block above (unconditionally, block flag 2),
 * spawns 10 client-side happy-villager particles, and consumes the seed
 * unless the player is in creative.
 */
public class ItemExperienceTreeSeed extends Item {
    public ItemExperienceTreeSeed(Item.Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();

        if (!level.isClientSide) {
            // orig :27-30 — only grass, dirt, farmland; a miss consumes nothing
            BlockState state = level.getBlockState(pos);
            if (!state.is(Blocks.GRASS_BLOCK) && !state.is(Blocks.DIRT) && !state.is(Blocks.FARMLAND)) {
                return InteractionResult.FAIL;
            }
            // orig :31 — placed at y+1 with flag 2, no emptiness check
            level.setBlock(pos.above(), ModBlocks.EXPERIENCE_PLANT.get().defaultBlockState(), 2);
        } else {
            // orig :33-35 — 10 happy-villager particles, client side
            for (int i = 0; i < 10; ++i) {
                level.addParticle(ParticleTypes.HAPPY_VILLAGER,
                        pos.getX() + level.random.nextFloat(),
                        pos.getY() + 1.0 + level.random.nextFloat(),
                        pos.getZ() + level.random.nextFloat(),
                        0.0, 0.0, 0.0);
            }
        }

        // orig :37-39 — creative players keep the seed
        Player player = context.getPlayer();
        if (player == null || !player.getAbilities().instabuild) {
            context.getItemInHand().shrink(1);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
