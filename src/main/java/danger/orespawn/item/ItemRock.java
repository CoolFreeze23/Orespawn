package danger.orespawn.item;

import danger.orespawn.ModEntities;
import danger.orespawn.entity.EntityThrownRock;
import danger.orespawn.entity.RockBase;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

public class ItemRock extends Item {
    private final int rockType;

    public ItemRock(Item.Properties properties, int rockType) {
        super(properties);
        this.rockType = rockType;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide) {
            EntityThrownRock projectile = new EntityThrownRock(level, player, this.rockType);
            projectile.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 1.5F, 1.0F);
            level.addFreshEntity(projectile);
            level.playSound(null, player.blockPosition(), SoundEvents.SNOWBALL_THROW, SoundSource.PLAYERS, 0.5F, 0.4F);
        }
        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        if (level.isClientSide) return InteractionResult.SUCCESS;

        BlockPos pos = context.getClickedPos().above();
        RockBase rock = ModEntities.ROCK_BASE.get().create(level);
        if (rock != null) {
            rock.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, 0.0F, 0.0F);
            level.addFreshEntity(rock);
        }
        context.getItemInHand().shrink(1);
        return InteractionResult.SUCCESS;
    }

    public int getRockType() {
        return rockType;
    }
}
