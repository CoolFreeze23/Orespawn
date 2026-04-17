package danger.orespawn.item;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 1.21.1 port of 1.7.10 ZooCage — instantly builds a glass-walled enclosure with
 * a quartz floor &amp; ceiling so the player has somewhere to release their captured
 * mobs from {@link CagedMobItem}. Sized variants (2/4/6/8/10) match the original
 * mod's five item registrations.
 *
 * <p>1.7.10 used {@code field_150371_ca} (quartz_block) for floor/ceiling and
 * {@code field_150359_w} (glass) for walls. The geometry is {@code (cage_size/2 + 1)}
 * half-extent on each horizontal axis with the same height — kept verbatim.</p>
 *
 * <p>{@code Block.UPDATE_ALL & ~UPDATE_NEIGHBORS} ({@link Block#UPDATE_CLIENTS})
 * is used for the bulk write to skip neighbor cascades on a 21×21×11 cube.</p>
 */
public class ZooCageItem extends Item {
    private static final int BULK_FLAG = Block.UPDATE_CLIENTS;

    private final int cageSize;

    public ZooCageItem(Properties properties, int cageSize) {
        super(properties.stacksTo(16));
        this.cageSize = cageSize;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        if (player == null) return InteractionResult.PASS;
        if (level.isClientSide) return InteractionResult.SUCCESS;

        int half = cageSize / 2 + 1;
        BlockPos origin = context.getClickedPos().above();

        BlockState floorCeiling = Blocks.QUARTZ_BLOCK.defaultBlockState();
        BlockState walls = Blocks.GLASS.defaultBlockState();
        BlockState air = Blocks.AIR.defaultBlockState();

        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int i = -half; i <= half; i++) {
            for (int j = -half; j <= half; j++) {
                for (int k = 0; k <= half + 1; k++) {
                    cursor.set(origin.getX() + i, origin.getY() + k, origin.getZ() + j);
                    BlockState set;
                    if (k == 0 || k == half + 1) {
                        set = floorCeiling;
                    } else if (i == half || i == -half || j == half || j == -half) {
                        set = walls;
                    } else {
                        set = air;
                    }
                    level.setBlock(cursor, set, BULK_FLAG);
                }
            }
        }

        level.playSound(null, player.blockPosition(), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 1.0F, 1.5F);

        if (!player.getAbilities().instabuild) {
            ItemStack inHand = context.getItemInHand();
            inHand.shrink(1);
        }
        return InteractionResult.CONSUME;
    }
}
