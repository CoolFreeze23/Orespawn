package danger.orespawn.item;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

public class ItemPizza extends Item {
    private final Block blockToPlace;

    public ItemPizza(Item.Properties properties, Block blockToPlace) {
        super(properties);
        this.blockToPlace = blockToPlace;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        if (level.isClientSide) return InteractionResult.SUCCESS;

        Player player = context.getPlayer();
        if (player == null) return InteractionResult.PASS;

        BlockPos pos = context.getClickedPos().relative(context.getClickedFace());
        if (level.getBlockState(pos).isAir()) {
            level.setBlock(pos, blockToPlace.defaultBlockState(), 3);
            context.getItemInHand().shrink(1);
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.FAIL;
    }
}
