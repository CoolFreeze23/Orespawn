package danger.orespawn.item;

import danger.orespawn.ModBlocks;
import danger.orespawn.OreSpawnConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.Tags;
import danger.orespawn.OreSpawnMod;

/**
 * 1.21.1 port of 1.7.10 ItemMinersDream — bores a 64-long, 5-wide, 5-tall
 * axis-aligned tunnel forward from the player. Faithful to {@code func_77648_a}:
 * clears junk (stone/dirt/sand/gravel/fluids/netherrack/end_stone/crystal_stone),
 * caps unstable ceilings with cobblestone (or crystal_stone in the Crystal dim),
 * fills floor holes when the ceiling above is fully open, and torches every
 * 5 blocks along the centerline on solid floor.
 *
 * <p><b>Modernization vs 1.7.10:</b>
 * <ul>
 *   <li>Junk detection is tag-driven ({@code minecraft:base_stone_overworld},
 *       {@code minecraft:dirt}, {@code minecraft:sand}, {@code c:gravels},
 *       {@code minecraft:base_stone_nether}, {@code c:end_stones}) instead of
 *       hardcoded block IDs.</li>
 *   <li>Ores are explicitly preserved via {@code Tags.Blocks.ORES} so vanilla
 *       AND modded ores survive — the original mod relied on the absence of
 *       ore IDs from its junk list, which silently broke for new ore mods.</li>
 *   <li>{@code level.setBlock(pos, state, 2)} (UPDATE_CLIENTS, no neighbor
 *       notification) is used for the bulk clear to avoid the cascading
 *       lighting / fluid / falling-block updates that nuke TPS at 1600 blocks
 *       per click. The 3-flag standard write is reserved for the torches.</li>
 * </ul></p>
 */
public class ItemMinersDream extends Item {
    private static final int TUNNEL_LENGTH = 64;
    private static final int TUNNEL_HALF_WIDTH = 2;
    private static final int TUNNEL_HEIGHT = 5;
    private static final int TORCH_INTERVAL = 5;

    /** Bulk clear flag: client sync only, no neighbor notify, no fluid/lighting cascade. */
    private static final int BULK_FLAG = Block.UPDATE_CLIENTS;

    private static final ResourceKey<Level> CRYSTAL_DIMENSION =
            ResourceKey.create(net.minecraft.core.registries.Registries.DIMENSION,
                    ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "crystal"));

    public ItemMinersDream(Item.Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        if (player == null) return InteractionResult.PASS;

        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        if (OreSpawnConfig.MINERS_DREAM_EXPENSIVE.get() && !player.getAbilities().instabuild) {
            int diamondSlot = -1;
            for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                if (player.getInventory().getItem(i).is(Items.DIAMOND)) { diamondSlot = i; break; }
            }
            if (diamondSlot == -1) return InteractionResult.PASS;
            player.getInventory().getItem(diamondSlot).shrink(1);
        }

        Direction facing = player.getDirection();
        int dx = facing.getStepX();
        int dz = facing.getStepZ();
        if (dx == 0 && dz == 0) return InteractionResult.FAIL;

        BlockPos origin = context.getClickedPos().relative(facing);
        boolean crystalDim = level.dimension().equals(CRYSTAL_DIMENSION);
        BlockState capState = (crystalDim ? ModBlocks.CRYSTAL_STONE.get() : Blocks.COBBLESTONE).defaultBlockState();
        BlockState air = Blocks.AIR.defaultBlockState();

        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();

        for (int forward = 0; forward < TUNNEL_LENGTH; forward++) {
            int baseX = origin.getX() + dx * forward;
            int baseZ = origin.getZ() + dz * forward;

            for (int h = 0; h < TUNNEL_HEIGHT; h++) {
                int solidAbove = 0;
                int y = origin.getY() + h;

                for (int w = -TUNNEL_HALF_WIDTH; w <= TUNNEL_HALF_WIDTH; w++) {
                    int x = baseX + (dz != 0 ? w : 0);
                    int z = baseZ + (dx != 0 ? w : 0);
                    cursor.set(x, y, z);

                    BlockState here = level.getBlockState(cursor);
                    if (isJunk(here)) {
                        level.setBlock(cursor, air, BULK_FLAG);
                    }

                    if (h == TUNNEL_HEIGHT - 1) {
                        cursor.set(x, y + 1, z);
                        BlockState above = level.getBlockState(cursor);
                        if (!above.isAir()) solidAbove++;
                        if (isUnstableCeiling(above)) {
                            level.setBlock(cursor, capState, BULK_FLAG);
                        }
                    }
                }

                if (h == TUNNEL_HEIGHT - 1 && solidAbove == 0) {
                    for (int w = -TUNNEL_HALF_WIDTH; w <= TUNNEL_HALF_WIDTH; w++) {
                        int x = baseX + (dz != 0 ? w : 0);
                        int z = baseZ + (dx != 0 ? w : 0);
                        cursor.set(x, y + 1, z);
                        if (level.getBlockState(cursor).isAir()) {
                            level.setBlock(cursor, air, BULK_FLAG);
                        }
                    }
                }
            }

            if (forward % TORCH_INTERVAL == 0) {
                cursor.set(baseX, origin.getY() - 1, baseZ);
                BlockState floor = level.getBlockState(cursor);
                BlockPos torchPos = cursor.above().immutable();
                if (level.isEmptyBlock(torchPos)) {
                    if (crystalDim && floor.is(ModBlocks.CRYSTAL_STONE.get())) {
                        level.setBlock(torchPos, ModBlocks.CRYSTAL_TORCH.get().defaultBlockState(), Block.UPDATE_ALL);
                    } else if (isTorchableFloor(floor)) {
                        level.setBlock(torchPos, ModBlocks.EXTREME_TORCH.get().defaultBlockState(), Block.UPDATE_ALL);
                    }
                }
            }
        }

        level.playSound(null, player.blockPosition(), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 1.0F, 1.5F);
        if (!player.getAbilities().instabuild) {
            context.getItemInHand().shrink(1);
        }
        return InteractionResult.CONSUME;
    }

    private static boolean isJunk(BlockState state) {
        if (state.isAir()) return false;
        if (state.is(Tags.Blocks.ORES)) return false;
        if (state.is(BlockTags.LOGS) || state.is(BlockTags.LEAVES)) return false;
        if (state.is(Tags.Blocks.CHESTS) || state.is(Tags.Blocks.STORAGE_BLOCKS)) return false;
        if (state.is(BlockTags.BEDS) || state.is(BlockTags.DOORS)) return false;
        if (state.is(Tags.Blocks.OBSIDIANS)) return false;
        if (state.is(Blocks.BEDROCK) || state.is(Blocks.BARRIER)) return false;
        if (state.is(ModBlocks.CRYSTAL_STONE.get())) return true;
        return state.is(BlockTags.BASE_STONE_OVERWORLD)
                || state.is(BlockTags.DIRT)
                || state.is(BlockTags.SAND)
                || state.is(Tags.Blocks.GRAVELS)
                || state.is(BlockTags.BASE_STONE_NETHER)
                || state.is(Tags.Blocks.END_STONES)
                || state.is(Blocks.NETHERRACK)
                || state.is(Blocks.WATER) || state.is(Blocks.LAVA);
    }

    private static boolean isUnstableCeiling(BlockState state) {
        if (state.isAir()) return false;
        return state.is(Tags.Blocks.GRAVELS)
                || state.is(BlockTags.SAND)
                || state.is(Blocks.WATER) || state.is(Blocks.LAVA);
    }

    private static boolean isTorchableFloor(BlockState state) {
        return state.is(BlockTags.BASE_STONE_OVERWORLD)
                || state.is(BlockTags.DIRT)
                || state.is(Tags.Blocks.GRAVELS)
                || state.is(BlockTags.BASE_STONE_NETHER)
                || state.is(Tags.Blocks.END_STONES)
                || state.is(Blocks.BEDROCK);
    }
}
