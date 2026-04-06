package danger.orespawn.item;

import danger.orespawn.ModEntities;
import danger.orespawn.entity.EntityCage;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import java.util.Map;
import java.util.function.Supplier;

public class CritterCage extends Item {
    private final int cageId;

    public CritterCage(Item.Properties properties, int cageId) {
        super(properties);
        this.cageId = cageId;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (cageId == 0) {
            if (!level.isClientSide) {
                EntityCage projectile = new EntityCage(level, player, 0);
                projectile.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 1.5F, 1.0F);
                level.addFreshEntity(projectile);
                level.playSound(null, player.blockPosition(), SoundEvents.SNOWBALL_THROW, SoundSource.PLAYERS, 0.5F, 0.4F);
            }
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
        }
        return InteractionResultHolder.pass(stack);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (cageId == 0) return InteractionResult.PASS;

        Level level = context.getLevel();
        if (level.isClientSide) return InteractionResult.SUCCESS;

        Player player = context.getPlayer();
        if (player == null) return InteractionResult.PASS;

        BlockPos pos = context.getClickedPos().above();
        EntityType<?> type = getCageEntityType();
        if (type != null) {
            Entity entity = type.create(level);
            if (entity != null) {
                entity.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, 0.0F, 0.0F);
                level.addFreshEntity(entity);
            }
        }
        level.playSound(null, player.blockPosition(), SoundEvents.CHICKEN_EGG, SoundSource.PLAYERS, 1.0F, 1.0F);
        context.getItemInHand().shrink(1);
        return InteractionResult.SUCCESS;
    }

    public int getCageId() {
        return cageId;
    }

    private EntityType<?> getCageEntityType() {
        return switch (cageId) {
            case 2 -> ModEntities.ENTITY_RAT.get();
            case 4 -> ModEntities.FAIRY.get();
            case 6 -> ModEntities.GOLD_FISH.get();
            case 8 -> ModEntities.CHIPMUNK.get();
            case 10 -> ModEntities.LIZARD.get();
            default -> null;
        };
    }
}
