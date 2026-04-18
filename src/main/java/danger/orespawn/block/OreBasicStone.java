package danger.orespawn.block;

import danger.orespawn.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.TransparentBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Stone blocks that spawn creatures when broken (e.g. rat stone, fairy
 * stone, ant troll stone). Extends {@link TransparentBlock} so light
 * passes through and adjacent faces between identical blocks are culled
 * (no seam artifacts).
 *
 * <p>Trap variants (RED_ANT_TROLL / TERMITE_TROLL) port the legacy
 * {@code OreBasicStone.func_149664_b} (1.7.10 lines 47-58). The legacy
 * hook was {@code onBlockDestroyedByPlayer} — i.e. it fired ONLY when a
 * player broke the block, not when an explosion, piston, or hostile mob
 * (Mobzilla, Ghast fireball, etc.) destroyed it. The 1.21.1 equivalent
 * is {@link #playerDestroy}, which gives us the same player-only guard
 * for free and skips the trap when the block is broken by world events.
 * This is the explicit stability guard against the infinite-spawn /
 * crash-on-explosion case the user flagged.</p>
 *
 * <p>Modern Silk Touch bypass: a player holding any tool with
 * {@link Enchantments#SILK_TOUCH} skips the trap entirely and gets the
 * block dropped as an item (matches the modern player expectation set
 * by vanilla silverfish blocks).</p>
 */
public class OreBasicStone extends TransparentBlock {

    public enum StoneType { NORMAL, RAT, FAIRY, RED_ANT_TROLL, TERMITE_TROLL }

    private static final int RAT_SPAWN_MAX_EXTRA = 10;
    private static final int FAIRY_SPAWN_MAX_EXTRA = 6;
    private static final int ANT_TROLL_SPAWN_BASE = 3;
    private static final int ANT_TROLL_SPAWN_MAX_EXTRA = 3;

    private final StoneType stoneType;

    public OreBasicStone(BlockBehaviour.Properties properties, StoneType stoneType) {
        super(properties);
        this.stoneType = stoneType;
    }

    public OreBasicStone(BlockBehaviour.Properties properties) {
        this(properties, StoneType.NORMAL);
    }

    @Override
    protected boolean skipRendering(BlockState state, BlockState adjacentState, Direction direction) {
        return adjacentState.is(this) || super.skipRendering(state, adjacentState, direction);
    }

    /**
     * Legacy {@code onBlockDestroyedByPlayer} parity for the rat / fairy
     * variants. Trap variants intentionally use {@link #playerDestroy}
     * instead so explosions and mob breakers cannot trigger the swarm.
     */
    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock()) && level instanceof ServerLevel serverLevel && !serverLevel.isClientSide()) {
            if (stoneType == StoneType.RAT || stoneType == StoneType.FAIRY) {
                spawnOnBreak(serverLevel, pos);
            }
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    /**
     * Player-only break hook — modern equivalent of the legacy
     * {@code func_149664_b}. Used for the troll variants so explosions /
     * pistons / Mobzilla cannot snowball into infinite swarm spawns.
     * Silk Touch bypasses the trap entirely.
     */
    @Override
    public void playerDestroy(Level level, Player player, BlockPos pos, BlockState state,
                               BlockEntity blockEntity, ItemStack tool) {
        super.playerDestroy(level, player, pos, state, blockEntity, tool);
        if (level.isClientSide() || !(level instanceof ServerLevel serverLevel)) return;
        if (stoneType != StoneType.RED_ANT_TROLL && stoneType != StoneType.TERMITE_TROLL) return;
        // Silk Touch bypass: matches vanilla silverfish-block convention.
        if (hasSilkTouch(serverLevel, tool)) return;
        spawnOnBreak(serverLevel, pos);
    }

    private static boolean hasSilkTouch(ServerLevel level, ItemStack tool) {
        if (tool == null || tool.isEmpty()) return false;
        Holder<Enchantment> silk = level.registryAccess()
                .lookupOrThrow(Registries.ENCHANTMENT)
                .getOrThrow(Enchantments.SILK_TOUCH);
        return EnchantmentHelper.getItemEnchantmentLevel(silk, tool) > 0;
    }

    private void spawnOnBreak(ServerLevel level, BlockPos pos) {
        int spawnCount;
        switch (stoneType) {
            case RAT:
                spawnCount = 1 + level.random.nextInt(RAT_SPAWN_MAX_EXTRA);
                for (int i = 0; i < spawnCount; i++) {
                    spawnCreature(level, ModEntities.ENTITY_RAT.get(), pos);
                }
                break;
            case FAIRY:
                spawnCount = 1 + level.random.nextInt(FAIRY_SPAWN_MAX_EXTRA);
                for (int i = 0; i < spawnCount; i++) {
                    spawnCreature(level, ModEntities.FAIRY.get(), pos);
                }
                break;
            case RED_ANT_TROLL:
                spawnCount = ANT_TROLL_SPAWN_BASE + level.random.nextInt(ANT_TROLL_SPAWN_MAX_EXTRA);
                for (int i = 0; i < spawnCount; i++) {
                    spawnCreature(level, ModEntities.ENTITY_RED_ANT.get(), pos);
                }
                break;
            case TERMITE_TROLL:
                spawnCount = ANT_TROLL_SPAWN_BASE + level.random.nextInt(ANT_TROLL_SPAWN_MAX_EXTRA);
                for (int i = 0; i < spawnCount; i++) {
                    spawnCreature(level, ModEntities.ENTITY_TERMITE.get(), pos);
                }
                break;
            default:
                break;
        }
    }

    private void spawnCreature(ServerLevel level, EntityType<? extends Mob> type, BlockPos pos) {
        Mob entity = type.create(level);
        if (entity != null) {
            double x = pos.getX() + 0.5 + (level.random.nextFloat() - level.random.nextFloat()) * 0.2;
            double z = pos.getZ() + 0.5 + (level.random.nextFloat() - level.random.nextFloat()) * 0.2;
            entity.moveTo(x, pos.getY() + 0.01, z, level.random.nextFloat() * 360.0f, 0.0f);
            entity.finalizeSpawn(level, level.getCurrentDifficultyAt(pos), MobSpawnType.EVENT, null);
            level.addFreshEntity(entity);
        }
    }
}
