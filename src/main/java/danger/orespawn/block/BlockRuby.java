package danger.orespawn.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Used for gem/scale storage blocks (e.g. Mobzilla Scale Block).
 * The isMobzillaScale flag controls whether stepping on the block applies fire resistance.
 */
public class BlockRuby extends Block {
    private static final int FIRE_RESISTANCE_DURATION_TICKS = 200;
    private static final int FIRE_RESISTANCE_AMPLIFIER = 0;

    private final boolean isMobzillaScale;

    public BlockRuby(BlockBehaviour.Properties properties, boolean isMobzillaScale) {
        super(properties);
        this.isMobzillaScale = isMobzillaScale;
    }

    public BlockRuby(BlockBehaviour.Properties properties) {
        this(properties, false);
    }

    @Override
    public void stepOn(Level level, BlockPos pos, BlockState state, Entity entity) {
        if (isMobzillaScale && entity instanceof LivingEntity living) {
            living.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, FIRE_RESISTANCE_DURATION_TICKS, FIRE_RESISTANCE_AMPLIFIER));
        }
        super.stepOn(level, pos, state, entity);
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (isMobzillaScale && entity instanceof LivingEntity living) {
            living.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, FIRE_RESISTANCE_DURATION_TICKS, FIRE_RESISTANCE_AMPLIFIER));
        }
        super.entityInside(state, level, pos, entity);
    }
}
