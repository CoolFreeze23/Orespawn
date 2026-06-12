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
 * The isMobzillaScale flag controls whether contact applies Strength —
 * orig BlockRuby.java:37-47 gives the Mobzilla Scale Block variant
 * {@code Potion.damageBoost} (Strength) 200t on touch/walk (ITEM-008).
 */
public class BlockRuby extends Block {
    // orig BlockRuby.java:39,45 — PotionEffect(damageBoost, 200, 0)
    private static final int STRENGTH_DURATION_TICKS = 200;
    private static final int STRENGTH_AMPLIFIER = 0;

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
            living.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, STRENGTH_DURATION_TICKS, STRENGTH_AMPLIFIER));
        }
        super.stepOn(level, pos, state, entity);
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (isMobzillaScale && entity instanceof LivingEntity living) {
            living.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, STRENGTH_DURATION_TICKS, STRENGTH_AMPLIFIER));
        }
        super.entityInside(state, level, pos, entity);
    }
}
