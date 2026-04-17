package danger.orespawn.item;

import danger.orespawn.ModDataComponents;
import danger.orespawn.ModItems;
import danger.orespawn.entity.EntityCage;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * 1.21.1 modernization of the 1.7.10 CritterCage "empty" cage. Two interaction paths,
 * matching the original mod:
 *
 * <ol>
 *   <li><b>Right-click in air</b> ({@link #use}) — throws an {@link EntityCage}
 *       projectile, exactly like 1.7.10's {@code func_77659_a} branch when
 *       {@code cage_id == CageEmpty.cage_id}.</li>
 *   <li><b>Right-click on a mob</b> ({@link #interactLivingEntity}) — direct
 *       capture path. Serializes the mob's full live state (health, custom name,
 *       taming, age, equipment) into the resulting filled cage's
 *       {@link ModDataComponents#CAGED_ENTITY_DATA} component, deletes the mob,
 *       and replaces this stack with the filled cage. <b>This preserves state
 *       that 1.7.10 silently discarded</b> — the original mod always re-spawned
 *       a vanilla-default entity on release.</li>
 * </ol>
 *
 * <p>NBT lives on the {@link DataComponentType} stream, never on the
 * {@code ItemStack} directly — the modern paradigm replacement for 1.7.10's
 * {@code stack.stackTagCompound}.</p>
 */
public class EmptyCageItem extends Item {
    public EmptyCageItem(Properties properties) {
        super(properties.stacksTo(16));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        level.playSound(null, player.blockPosition(), SoundEvents.ARROW_SHOOT, SoundSource.PLAYERS, 0.5F,
                0.4F / (level.getRandom().nextFloat() * 0.4F + 0.8F));

        if (!level.isClientSide) {
            EntityCage projectile = new EntityCage(level, player, 160);
            projectile.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 1.5F, 1.0F);
            level.addFreshEntity(projectile);
        }

        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }
        player.getCooldowns().addCooldown(this, 5);
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity target, InteractionHand hand) {
        if (!(target instanceof Mob mob)) return InteractionResult.PASS;
        if (target instanceof Player) return InteractionResult.PASS;

        Level level = player.level();
        if (level.isClientSide) {
            return InteractionResult.sidedSuccess(true);
        }
        if (!(level instanceof ServerLevel serverLevel)) return InteractionResult.PASS;

        ResourceLocation entityId = BuiltInRegistries.ENTITY_TYPE.getKey(mob.getType());
        CompoundTag mobData = new CompoundTag();
        mob.saveWithoutId(mobData);

        ItemStack filled = CagedMobItem.createForEntity(entityId, mobData);
        if (mob.hasCustomName()) {
            filled.set(DataComponents.CUSTOM_NAME, mob.getCustomName());
        }

        for (int i = 0; i < 6; i++) {
            serverLevel.sendParticles(ParticleTypes.EXPLOSION,
                    target.getX(), target.getY() + 0.5, target.getZ(), 1, 0, 0, 0, 0);
            serverLevel.sendParticles(ParticleTypes.SMOKE,
                    target.getX(), target.getY() + 0.5, target.getZ(), 1, 0, 0, 0, 0);
        }
        level.playSound(null, target.blockPosition(), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 1.0F, 1.5F);

        mob.discard();

        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }
        if (!player.getInventory().add(filled)) {
            player.drop(filled, false);
        }
        return InteractionResult.CONSUME;
    }

    /** Helper for code paths (loot tables, EntityCage projectile fallback) that
     *  want to manufacture an empty cage stack. */
    public static ItemStack newStack(int count) {
        return new ItemStack(ModItems.CAGE_EMPTY.get(), count);
    }
}
