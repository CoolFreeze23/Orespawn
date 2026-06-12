package danger.orespawn.item;

import danger.orespawn.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

/**
 * StepUp, ported from 1.7.10 StepUp.java:26-99. Builds an ascending cobble
 * stair (up to 33 steps) in the direction the player's head faces (8-way,
 * including diagonals), stopping at the first non-air block, with an
 * Extreme Torch every 8 steps. Explosion sound/particles on use.
 */
public class StepUp extends Item {
    // orig StepUp.java:30 — length 33
    static final int LENGTH = 33;

    public StepUp(Item.Properties properties) {
        super(properties);
    }

    /**
     * orig StepUp.java:34-78 — octant from head yaw: (yaw + 22.5) % 360 / 45.
     * Yaw 0 is south (+z); each octant steps 45° clockwise. Out-of-range
     * results (negative yaw) leave delta 0,0 and the item does nothing,
     * matching the original quirk.
     *
     * @return {dx, dz} step per stair, or null when no octant matched
     */
    static int[] headingDeltas(Player player) {
        float f = (player.yHeadRot + 22.5f) % 360.0f;
        return switch ((int) (f / 45.0f)) {
            case 0 -> new int[]{0, 1};
            case 1 -> new int[]{-1, 1};
            case 2 -> new int[]{-1, 0};
            case 3 -> new int[]{-1, -1};
            case 4 -> new int[]{0, -1};
            case 5 -> new int[]{1, -1};
            case 6 -> new int[]{1, 0};
            case 7 -> new int[]{1, 1};
            default -> null;
        };
    }

    /**
     * orig StepUp.java:82-88 — explosion sound plus smoke/explosion puffs.
     * StepUp spawns its particles one block higher ({@code yOffset} 1.0)
     * than StepDown/StepAccross (orig StepDown.java:84-88, yOffset 0).
     */
    static void playUseEffects(Level level, Player player, int x, int y, int z, float yOffset) {
        level.playSound(null, player.blockPosition(), SoundEvents.GENERIC_EXPLODE.value(),
                SoundSource.PLAYERS, 1.0f, 1.5f);
        if (level instanceof ServerLevel serverLevel) {
            for (int n = 0; n < 6; ++n) {
                double px = x + level.random.nextFloat() - level.random.nextFloat();
                double py = y + level.random.nextFloat() + yOffset;
                double pz = z + level.random.nextFloat() - level.random.nextFloat();
                serverLevel.sendParticles(ParticleTypes.LARGE_SMOKE, px, py, pz, 1, 0, 0, 0, 0);
                serverLevel.sendParticles(ParticleTypes.EXPLOSION, px, py, pz, 1, 0, 0, 0, 0);
            }
        }
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        if (player == null) return InteractionResult.PASS;

        // orig StepUp.java:31-33 — start one above the clicked block
        int x = context.getClickedPos().getX();
        int y = context.getClickedPos().getY() + 1;
        int z = context.getClickedPos().getZ();

        int[] delta = headingDeltas(player);
        if (delta == null) return InteractionResult.FAIL;

        if (level.isClientSide) return InteractionResult.SUCCESS;
        playUseEffects(level, player, x, y, z, 1.0f);

        // orig StepUp.java:91-95 — climb one block per step until obstructed;
        // Extreme Torch on steps 1, 9, 17, 25 ((k-1) % 8 == 0) when air above
        for (int k = 1; k < LENGTH; ++k) {
            BlockPos step = new BlockPos(x + k * delta[0], y + k - 1, z + k * delta[1]);
            if (!level.getBlockState(step).isAir()) break;
            level.setBlock(step, Blocks.COBBLESTONE.defaultBlockState(), 2);
            if ((k - 1) % 8 == 0 && level.getBlockState(step.above()).isAir()) {
                level.setBlock(step.above(), ModBlocks.EXTREME_TORCH.get().defaultBlockState(), 2);
            }
        }

        // orig StepUp.java:96-98 — consume one unless creative
        if (!player.getAbilities().instabuild) {
            context.getItemInHand().shrink(1);
        }
        return InteractionResult.CONSUME;
    }
}
