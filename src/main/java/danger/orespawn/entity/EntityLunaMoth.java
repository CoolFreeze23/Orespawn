package danger.orespawn.entity;

import danger.orespawn.entity.ai.LunaMothFlightGoal;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/**
 * Luna Moth — a night-flying variant of {@link EntityButterfly} that is
 * attracted to torches when sheltered from the sky.
 *
 * <p>Flight logic lives in {@link LunaMothFlightGoal}. This class keeps
 * only the non-AI pieces: the {@code moth_type} visual variant, the
 * vertical-drag override (so moths hover a bit heavier than butterflies),
 * and the 1.7.10 spawn-rule (must be in air, must be sheltered, must be
 * above Y=50).
 */
public class EntityLunaMoth extends EntityButterfly {
    private static final double VERTICAL_DRAG = 0.6;
    private static final int MIN_SPAWN_Y = 50;

    public int moth_type;

    public EntityLunaMoth(EntityType<? extends EntityLunaMoth> type, Level level) {
        super(type, level);
        this.moth_type = this.random.nextInt(4);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 2.0)
                .add(Attributes.MOVEMENT_SPEED, 0.1)
                .add(Attributes.ATTACK_DAMAGE, 0.0);
    }

    /**
     * Replaces the base butterfly flight goal with a torch-seeking moth
     * specialisation. Same priority slot so only one flight goal runs;
     * this is the NeoForge 1.21.1 analogue of 1.7.10's subclass-
     * overridden {@code customServerAiStep}.
     */
    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(8, new LunaMothFlightGoal(this));
    }

    @Override
    public void tick() {
        super.tick();
        Vec3 motion = this.getDeltaMovement();
        this.setDeltaMovement(motion.x, motion.y * VERTICAL_DRAG, motion.z);
    }

    @Override
    public boolean checkSpawnRules(LevelAccessor level, MobSpawnType spawnType) {
        BlockState state = level.getBlockState(this.blockPosition());
        if (!state.isAir()) return false;
        if (level.canSeeSky(this.blockPosition())) return false;
        return this.getY() >= MIN_SPAWN_Y;
    }
}
