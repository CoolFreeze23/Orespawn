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

    /**
     * orig ItemRock.java:75-128 — right-clicking a block places a pet Rock
     * critter of this item's type one block above, with a random yaw.
     */
    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        if (level.isClientSide) return InteractionResult.SUCCESS;

        BlockPos pos = context.getClickedPos();
        RockBase rock = ModEntities.ROCK_BASE.get().create(level);
        if (rock != null) {
            // orig ItemRock.java:130-148 — +0.5 centering, y + 1.01, random yaw
            rock.moveTo(pos.getX() + 0.5, pos.getY() + 1.01, pos.getZ() + 0.5,
                    level.random.nextFloat() * 360.0F, 0.0F);
            rock.placeRock(this.rockType); // orig ItemRock.java:86-121
            level.addFreshEntity(rock);
        }
        // orig ItemRock.java:124-126 — consume one unless creative
        Player player = context.getPlayer();
        if (player == null || !player.getAbilities().instabuild) {
            context.getItemInHand().shrink(1);
        }
        return InteractionResult.SUCCESS;
    }

    public int getRockType() {
        return rockType;
    }
}
