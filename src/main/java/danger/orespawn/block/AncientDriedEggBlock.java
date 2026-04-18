package danger.orespawn.block;

import danger.orespawn.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import java.util.List;
import java.util.function.Supplier;

/**
 * Phase 10 — Ancient Dried Egg fossil block.
 *
 * In 1.7.10 the canonical "ancient dried egg" lore-block was a per-mob
 * {@link OreGenericEgg} subclass — every prehistoric mob had its own ore that
 * dropped XP and a fixed spawn egg. This modernized version collapses that
 * pattern into a single rehydration-style fossil block:
 *
 *   - Right-click with a Water Bucket → consume the bucket (replace with empty
 *     bucket), play a wet "splash" sound, set the block to AIR, and yield ONE
 *     random prehistoric spawn egg from the canonical 1.12.2 dino pool.
 *   - Mining the block normally drops itself (so players can collect & relocate
 *     them; Silk Touch is implicit because we don't override the loot table to
 *     shatter).
 *
 * The water-bucket consumption uses {@link ItemInteractionResult#sidedSuccess}
 * so the server is the source of truth (no client-side dupe is possible because
 * we only mutate the held stack on the server branch). The block transitions
 * to AIR atomically in the same server tick the spawn egg is dropped.
 */
public class AncientDriedEggBlock extends Block {

    /**
     * Canonical 1.12.2 dino-fossil spawn-egg pool. Lazy suppliers because
     * {@link ModItems} static initialiser runs after our block registry.
     */
    private static final List<Supplier<SpawnEggItem>> DINO_POOL = List.of(
            () -> ModItems.ALOSAURUS_SPAWN_EGG.get(),
            () -> ModItems.CRYOLOPHOSAURUS_SPAWN_EGG.get(),
            () -> ModItems.NASTYSAURUS_SPAWN_EGG.get(),
            () -> ModItems.POINTYSAURUS_SPAWN_EGG.get(),
            () -> ModItems.TREX_SPAWN_EGG.get(),
            () -> ModItems.VELOCITY_RAPTOR_SPAWN_EGG.get(),
            () -> ModItems.BABY_DRAGON_SPAWN_EGG.get()
    );

    public AncientDriedEggBlock(BlockBehaviour.Properties properties) {
        super(properties.sound(SoundType.GRAVEL));
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level,
                                              BlockPos pos, Player player, InteractionHand hand,
                                              BlockHitResult hit) {
        if (!stack.is(Items.WATER_BUCKET)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        if (!level.isClientSide()) {
            // Server is authoritative for both the bucket swap and the spawn-egg
            // drop. Doing both in the same tick prevents desync / dupes.
            int idx = level.getRandom().nextInt(DINO_POOL.size());
            SpawnEggItem egg = DINO_POOL.get(idx).get();
            ItemStack eggStack = new ItemStack(egg, 1);

            level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);

            if (!player.getAbilities().instabuild) {
                ItemStack emptyBucket = new ItemStack(Items.BUCKET);
                stack.shrink(1);
                if (stack.isEmpty()) {
                    player.setItemInHand(hand, emptyBucket);
                } else if (!player.getInventory().add(emptyBucket)) {
                    player.drop(emptyBucket, false);
                }
            }

            if (!player.getInventory().add(eggStack)) {
                player.drop(eggStack, false);
            }

            level.playSound(null, pos, net.minecraft.sounds.SoundEvents.BUCKET_EMPTY,
                    net.minecraft.sounds.SoundSource.BLOCKS, 1.0f, 1.0f);
            level.playSound(null, pos, net.minecraft.sounds.SoundEvents.TURTLE_EGG_HATCH,
                    net.minecraft.sounds.SoundSource.BLOCKS, 0.8f, 1.0f);
        }
        return ItemInteractionResult.sidedSuccess(level.isClientSide());
    }
}
