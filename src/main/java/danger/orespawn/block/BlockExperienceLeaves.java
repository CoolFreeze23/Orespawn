package danger.orespawn.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrownExperienceBottle;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class BlockExperienceLeaves extends LeavesBlock {
    private static final long TICKS_PER_DAY = 24000L;
    private static final long XP_DROP_WINDOW_START = 14000L;
    private static final long XP_DROP_WINDOW_END = 22000L;
    private static final int XP_BOTTLE_DROP_ROLL_BOUND = 65;
    private static final int XP_BOTTLE_DROP_SUCCESS_INDEX = 1;
    private static final int THROWN_BOTTLE_ROLL_BOUND = 75;
    private static final int THROWN_BOTTLE_SUCCESS_INDEX = 1;
    private static final double THROWN_BOTTLE_HORIZONTAL_SPREAD = 2.0;
    private static final double THROWN_BOTTLE_VERTICAL_IMPULSE = -0.1;

    private static final long PARTICLE_WINDOW_START = 13000L;
    private static final long PARTICLE_WINDOW_END = 23000L;
    private static final long DAWN_BLEND_END = 14000L;
    private static final long DUSK_BLEND_START = 22000L;
    private static final int TWILIGHT_RATE_DIVISOR = 2;
    private static final int ABOVE_PARTICLE_BASE_INTERVAL = 200;
    private static final int BELOW_PARTICLE_BASE_INTERVAL = 40;
    private static final int ABOVE_PARTICLE_BURST_COUNT = 10;
    private static final int BELOW_PARTICLE_BURST_COUNT = 4;
    private static final double ABOVE_PARTICLE_Y = 1.25;
    private static final double BELOW_PARTICLE_Y = -1.25;

    /**
     * Player-harvest XP rewards. 1.7.10 only emitted XP via random ticks
     * (see {@link #randomTick}) so a chopped-down experience tree still
     * felt unrewarding without a "leaves drop XP" payoff. We mirror the
     * iconic XP-bottle drop logic on harvest as well: a small chance per
     * leaf-block destroyed to drop a Bottle o' Enchanting plus a few
     * direct XP orbs. Silk-touch and shears suppress the bonus (their
     * loot-table entries already return the leaf block as an item — and
     * matching vanilla, harvesting cleanly should not also reward XP).
     */
    private static final int HARVEST_BOTTLE_DROP_CHANCE = 5;
    private static final int HARVEST_XP_ORB_CHANCE = 3;
    private static final int HARVEST_XP_ORB_MIN = 1;
    private static final int HARVEST_XP_ORB_MAX = 3;

    public BlockExperienceLeaves(BlockBehaviour.Properties properties) {
        super(properties);
    }

    /**
     * 1.7.10 fidelity: experience-tree leaves emit XP randomly during
     * their own random tick. Since 1.21.1 vanilla loot tables can't
     * conditionally drop XP based on tool / silk-touch / fortune in a
     * clean way, we hook {@link Block#playerDestroy} which fires AFTER
     * loot resolution. That lets us preserve silk-touch/shears as a
     * "clean harvest" (the loot table returns the leaf block; we skip
     * the XP) and only reward bare-handed / hoe / axe harvests with a
     * Bottle o' Enchanting + a small direct XP burst — matching the
     * spirit of "the experience tree gives experience".
     */
    @Override
    public void playerDestroy(Level level, Player player, BlockPos pos, BlockState state,
                              BlockEntity blockEntity, ItemStack tool) {
        super.playerDestroy(level, player, pos, state, blockEntity, tool);
        if (level.isClientSide() || !(level instanceof ServerLevel sl)) return;
        if (player.isCreative()) return;

        Holder<Enchantment> silk = sl.registryAccess()
                .lookupOrThrow(Registries.ENCHANTMENT)
                .getOrThrow(Enchantments.SILK_TOUCH);
        if (EnchantmentHelper.getItemEnchantmentLevel(silk, tool) > 0) return;
        if (tool.is(Items.SHEARS)) return;

        RandomSource random = sl.random;
        if (random.nextInt(HARVEST_BOTTLE_DROP_CHANCE) == 0) {
            Block.popResource(sl, pos, new ItemStack(Items.EXPERIENCE_BOTTLE));
        }
        if (random.nextInt(HARVEST_XP_ORB_CHANCE) == 0) {
            int xp = HARVEST_XP_ORB_MIN + random.nextInt(HARVEST_XP_ORB_MAX - HARVEST_XP_ORB_MIN + 1);
            popExperience(sl, pos, xp);
        }
    }

    @Override
    public boolean isRandomlyTicking(BlockState state) {
        return true;
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        super.randomTick(state, level, pos, random);
        if (level.isClientSide()) return;

        long timeOfDayTicks = level.getDayTime() % TICKS_PER_DAY;
        if (timeOfDayTicks < XP_DROP_WINDOW_START || timeOfDayTicks > XP_DROP_WINDOW_END) return;

        BlockPos above = pos.above();
        BlockPos below = pos.below();

        if (random.nextInt(XP_BOTTLE_DROP_ROLL_BOUND) == XP_BOTTLE_DROP_SUCCESS_INDEX && level.getBlockState(above).is(Blocks.AIR)) {
            Block.popResource(level, above, new ItemStack(Items.EXPERIENCE_BOTTLE));
        }

        if (random.nextInt(THROWN_BOTTLE_ROLL_BOUND) == THROWN_BOTTLE_SUCCESS_INDEX && level.getBlockState(below).is(Blocks.AIR)) {
            ThrownExperienceBottle bottle = new ThrownExperienceBottle(level, pos.getX(), pos.getY() - 1, pos.getZ());
            bottle.setDeltaMovement(
                    (random.nextFloat() - random.nextFloat()) / THROWN_BOTTLE_HORIZONTAL_SPREAD,
                    THROWN_BOTTLE_VERTICAL_IMPULSE,
                    (random.nextFloat() - random.nextFloat()) / THROWN_BOTTLE_HORIZONTAL_SPREAD
            );
            level.addFreshEntity(bottle);
        }
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        super.animateTick(state, level, pos, random);

        long timeOfDayTicks = level.getDayTime() % TICKS_PER_DAY;
        if (timeOfDayTicks < PARTICLE_WINDOW_START || timeOfDayTicks > PARTICLE_WINDOW_END) return;

        int twilightRollBonus = 0;
        if (timeOfDayTicks < DAWN_BLEND_END) {
            twilightRollBonus = (int) ((DAWN_BLEND_END - timeOfDayTicks) / TWILIGHT_RATE_DIVISOR);
        }
        if (timeOfDayTicks > DUSK_BLEND_START) {
            twilightRollBonus = (int) ((timeOfDayTicks - DUSK_BLEND_START) / TWILIGHT_RATE_DIVISOR);
        }

        BlockPos above = pos.above();
        BlockPos below = pos.below();

        if (random.nextInt(ABOVE_PARTICLE_BASE_INTERVAL + twilightRollBonus) == 1 && level.getBlockState(above).is(Blocks.AIR)) {
            for (int p = 0; p < ABOVE_PARTICLE_BURST_COUNT; p++) {
                level.addParticle(ParticleTypes.FIREWORK,
                        pos.getX() + 0.5, pos.getY() + ABOVE_PARTICLE_Y, pos.getZ() + 0.5,
                        random.nextGaussian(), Math.abs(random.nextGaussian()), random.nextGaussian());
            }
        }

        if (random.nextInt(BELOW_PARTICLE_BASE_INTERVAL + twilightRollBonus) == 1 && level.getBlockState(below).is(Blocks.AIR)) {
            for (int p = 0; p < BELOW_PARTICLE_BURST_COUNT; p++) {
                level.addParticle(ParticleTypes.FIREWORK,
                        pos.getX() + 0.5, pos.getY() + BELOW_PARTICLE_Y, pos.getZ() + 0.5,
                        random.nextFloat() - random.nextFloat(),
                        -Math.abs(random.nextFloat()),
                        random.nextFloat() - random.nextFloat());
            }
        }
    }
}
