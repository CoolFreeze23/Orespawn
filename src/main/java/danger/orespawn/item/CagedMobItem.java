package danger.orespawn.item;

import danger.orespawn.ModDataComponents;
import danger.orespawn.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

import java.util.List;

public class CagedMobItem extends Item {
    public CagedMobItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        if (level.isClientSide) return InteractionResult.SUCCESS;

        ItemStack stack = context.getItemInHand();
        ResourceLocation entityId = stack.get(ModDataComponents.CAGED_ENTITY.get());
        if (entityId == null) return InteractionResult.PASS;

        BlockPos pos = context.getClickedPos();
        Direction face = context.getClickedFace();
        BlockPos spawnPos = pos.relative(face);

        EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.get(entityId);
        if (type == null || type == EntityType.PIG && !entityId.equals(BuiltInRegistries.ENTITY_TYPE.getKey(EntityType.PIG))) {
            return InteractionResult.FAIL;
        }

        if (level instanceof ServerLevel serverLevel) {
            var entity = type.create(serverLevel, null, spawnPos, MobSpawnType.MOB_SUMMONED, true, false);
            if (entity != null) {
                if (stack.has(net.minecraft.core.component.DataComponents.CUSTOM_NAME)) {
                    entity.setCustomName(stack.getHoverName());
                }
                serverLevel.addFreshEntity(entity);
            }
        }

        if (context.getPlayer() != null && !context.getPlayer().getAbilities().instabuild) {
            stack.shrink(1);
            context.getPlayer().addItem(new ItemStack(ModItems.CAGE_EMPTY.get()));
        }

        return InteractionResult.CONSUME;
    }

    @Override
    public Component getName(ItemStack stack) {
        ResourceLocation entityId = stack.get(ModDataComponents.CAGED_ENTITY.get());
        if (entityId != null) {
            EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.get(entityId);
            if (type != null) {
                return Component.translatable("item.orespawn.caged_mob",
                        Component.translatable(type.getDescriptionId()));
            }
        }
        return super.getName(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag flag) {
        ResourceLocation entityId = stack.get(ModDataComponents.CAGED_ENTITY.get());
        if (entityId != null) {
            tooltipComponents.add(Component.literal(entityId.toString()).withStyle(net.minecraft.ChatFormatting.GRAY));
        }
    }

    public static ItemStack createForEntity(ResourceLocation entityId) {
        ItemStack stack = new ItemStack(ModItems.CAGED_MOB.get());
        stack.set(ModDataComponents.CAGED_ENTITY.get(), entityId);
        return stack;
    }
}
