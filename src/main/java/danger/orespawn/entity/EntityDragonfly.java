package danger.orespawn.entity;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.ai.DragonflyHuntGoal;

/**
 * Dragonfly — a small airborne predator that chases and bites small mobs.
 *
 * <p>Flight + hunt logic lives in {@link DragonflyHuntGoal}. The legacy
 * 1.7.10 inline {@code customServerAiStep} has been removed; this class
 * now only handles vertical drag, hurt-retargeting (nudging the flight
 * goal toward the attacker), day-time spawn windowing, sounds, and
 * breeding (disabled).
 */
public class EntityDragonfly extends Animal {

    /**
     * Reference to the hunt goal so {@link #hurt} can retarget the flight
     * toward the attacker — matches 1.7.10's swap of {@code currentFlightTarget}
     * to {@code attacker.blockPosition()} in {@code hurt()}.
     */
    @Nullable
    private DragonflyHuntGoal huntGoal;

    public EntityDragonfly(EntityType<? extends EntityDragonfly> type, Level level) {
        super(type, level);
        this.xpReward = 5;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 10.0)
                .add(Attributes.MOVEMENT_SPEED, 0.33)
                .add(Attributes.ATTACK_DAMAGE, 2.0);
    }

    @Override
    protected void registerGoals() {
        this.huntGoal = new DragonflyHuntGoal(this);
        this.goalSelector.addGoal(3, this.huntGoal);
    }

    @Override
    public boolean isPushable() {
        return true;
    }

    @Override
    public void tick() {
        super.tick();
        Vec3 motion = this.getDeltaMovement();
        this.setDeltaMovement(motion.x, motion.y * 0.6, motion.z);
    }

    @Override
    public boolean causeFallDamage(float dist, float mult, DamageSource source) {
        return false;
    }

    @Override
    protected void checkFallDamage(double y, boolean onGround, BlockState state, BlockPos pos) {
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        boolean ret = super.hurt(source, amount);
        Entity attacker = source.getEntity();
        if (attacker != null && this.huntGoal != null) {
            this.huntGoal.setFlightTarget(attacker.blockPosition());
        }
        return ret;
    }

    @Override
    public boolean removeWhenFarAway(double dist) {
        return !this.isPersistenceRequired();
    }

    @Override
    public boolean checkSpawnRules(net.minecraft.world.level.LevelAccessor level,
                                    net.minecraft.world.entity.MobSpawnType spawnType) {
        return this.getY() >= 50.0 && level.dayTime() % 24000L < 13000L;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvent.createVariableRangeEvent(
                ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "dragonfly_living"));
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvent.createVariableRangeEvent(
                ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "dragonfly_hurt"));
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvent.createVariableRangeEvent(
                ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "dragonfly_death"));
    }

    @Override
    protected float getSoundVolume() {
        return 0.25f;
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return false;
    }

    @Nullable
    @Override
    public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob otherParent) {
        return null;
    }
}
