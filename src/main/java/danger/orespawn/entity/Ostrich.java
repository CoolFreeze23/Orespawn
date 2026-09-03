package danger.orespawn.entity;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.FollowOwnerGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import danger.orespawn.ModEntities;
import danger.orespawn.OreSpawnMod;

public class Ostrich extends TamableAnimal
        implements danger.orespawn.network.RiderInputPayload.RideableFlyer,
        danger.orespawn.entity.pose.OstrichPose {
    // OPT-011: cached SoundEvents — identical createVariableRangeEvent ids,
    // allocated once per class instead of on every sound query.
    private static final SoundEvent SND_CRYO_HURT = SoundEvent.createVariableRangeEvent(
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "cryo_hurt"));
    private static final SoundEvent SND_CRYO_DEATH = SoundEvent.createVariableRangeEvent(
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "cryo_death"));

    /**
     * Ridden-run tuning, number-for-number from orig Ostrich.java:401-535
     * (ridden branch of onLivingUpdate). The Ostrich is a runner, not a flier:
     * no hover, but an upward-scanning terrain probe 1 + v*10 @ 0.075/block
     * applied 1:1 to motionY/posY (orig :417-429), rise cap 4.0 (orig
     * :430-432), yaw lag 1.85 above 0.01 (orig :448-459), and the "FAST" jump
     * — fly-up key triggers a single +1.0 + v*6.0 hop with a 20-tick latch
     * that only counts down after release (orig :470-478). Throttle 0.045
     * ramped to max 0.75 (orig :368/:491-498), reverse 0.25 @ -0.03 (orig
     * :500-501), then gravity 0.25 and friction 0.95/0.85/0.95 (orig :532-535).
     */
    private static final danger.orespawn.entity.ai.RiderFlightController.Config RIDER_RUN_CONFIG =
            new danger.orespawn.entity.ai.RiderFlightController.Config(
                    false, 0.0, 0.0, 0.0, 0.0,
                    1, 10.0, 0.075, 1.0, true,
                    4.0,
                    1.85, 0.01, false,
                    true, 1.0, 6.0,
                    0.045, 0.75, -0.03, 0.25, true,
                    0.25, 0.95, 0.85);

    private final danger.orespawn.entity.ai.RiderFlightController riderRun =
            new danger.orespawn.entity.ai.RiderFlightController(RIDER_RUN_CONFIG);
    /** Held state of the rider's jump key (client-set for prediction, server-set via payload). */
    private boolean riderFlyUp = false;

    /**
     * ENT-S-093: per-entity render scratch, as the original had it (orig
     * Ostrich.java:45 {@code renderdata = new RenderInfo()}, re-created in the
     * ctor orig :67; entityInit orig :89-103 zeroes all eight fields, which a fresh
     * {@code new RenderInfo()} per construction already is). Mutated client-side by
     * {@code OstrichModel}: {@code rf1} is the ridden head-yaw smoothing latch
     * (orig ModelOstrich.java:338-345), {@code ri1} the wing-flap 1-in-3 selector
     * (orig ModelOstrich.java:370-375). Never datawatcher-synced. No setRenderInfo:
     * orig Ostrich.java:109-118 is only ever handed its own instance
     * (orig ModelOstrich.java:334/383), a self-copy, omitted as in Kraken.
     */
    private final danger.orespawn.entity.client.RenderInfo renderInfo =
            new danger.orespawn.entity.client.RenderInfo();

    /** Mirrors orig Ostrich.java:105-107 {@code getRenderInfo()}. */
    @Override
    public danger.orespawn.entity.client.RenderInfo getRenderInfo() {
        return this.renderInfo;
    }

    // ENT-S-093 OstrichPose accessors: Entity's xOld/zOld/yRotO are public fields,
    // so the pose interface needs them as methods (orig ModelOstrich.java:299,:336).
    @Override
    public double xOld() { return this.xOld; }

    @Override
    public double zOld() { return this.zOld; }

    @Override
    public float yRotO() { return this.yRotO; }

    /**
     * ENT-S-093 interim bridge: orig EntityCannonFodder.java:36,228-230
     * {@code get_is_activated()} via orig Ostrich.java:42-43 (extends
     * EntityCannonFodder). Constant 0 (the original's default) until Ostrich is
     * re-parented to the port EntityCannonFodder, whose :70 accessor then satisfies
     * OstrichPose by inheritance. Nothing in the port can activate an Ostrich
     * today: the clone at EntityCannonFodder.java:166-171 skips setStuff for
     * non-fodder newborns. Consumed by the hat gate (orig ModelOstrich.java:420-425)
     * and the sitting predicate (orig ModelOstrich.java:349).
     */
    @Override
    public int getIsActivated() { return 0; }

    public Ostrich(EntityType<? extends Ostrich> type, Level level) {
        super(type, level);
        this.xpReward = 10;
        this.setOrderedToSit(false);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new BreedGoal(this, 1.0));
        this.goalSelector.addGoal(2, new FollowOwnerGoal(this, 2.0, 10.0f, 2.0f));
        this.goalSelector.addGoal(3, new AvoidEntityGoal<>(this, Monster.class, 8.0f, 1.0, 1.9));
        this.goalSelector.addGoal(4, new TemptGoal(this, 1.2, Ingredient.of(Items.WHEAT), false));
        this.goalSelector.addGoal(5, new PanicGoal(this, 1.5));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 6.0f));
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Mob.class, 5.0f));
        this.goalSelector.addGoal(8, new WaterAvoidingRandomStrollGoal(this, 1.0));
        this.goalSelector.addGoal(9, new RandomLookAroundGoal(this));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 25.0)
                .add(Attributes.MOVEMENT_SPEED, 0.38)
                .add(Attributes.ATTACK_DAMAGE, 6.0);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        // orig Ostrich.java:133-138 — cactus damage skipped; everything else applied
        // via super, but the method always reports false to the attacker (orig quirk).
        if (!source.getMsgId().equals("cactus")) {
            super.hurt(source, amount);
        }
        return false;
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (stack.is(Items.WHEAT) && this.distanceToSqr(player) < 16.0) {
            if (!this.isTame()) {
                if (!this.level().isClientSide) {
                    if (this.random.nextInt(2) == 0) {
                        this.setTame(true, true);
                        this.setOwnerUUID(player.getUUID());
                        this.heal(this.getMaxHealth() - this.getHealth());
                    }
                }
            } else if (this.isOwnedBy(player)) {
                this.heal(this.getMaxHealth() - this.getHealth());
            }
            if (!player.getAbilities().instabuild) stack.shrink(1);
            return InteractionResult.SUCCESS;
        }

        if (stack.isEmpty() && this.distanceToSqr(player) < 16.0) {
            if (!this.level().isClientSide) {
                player.startRiding(this);
                this.setOrderedToSit(false);
            }
            return InteractionResult.SUCCESS;
        }

        return super.mobInteract(player, hand);
    }

    @Override
    protected void customServerAiStep() {
        if (this.isRemoved()) return;
        if (this.random.nextInt(200) == 1) this.setTarget(null);
        if (this.random.nextInt(250) == 0) this.heal(1.0f);
        if (this.getFirstPassenger() != null) {
            // orig Ostrich.java:536-538 — drop a rider that has been removed.
            if (this.getFirstPassenger().isRemoved()) {
                this.ejectPassengers();
            }
            return;
        }
        super.customServerAiStep();
    }

    // ==================== Riding (ENT-K-044) ====================

    /**
     * Any mounted player steers — the original had no tame gate on riding
     * (orig Ostrich.java — bare-hand mount within 16 sq blocks, wild or tame).
     */
    @Nullable
    @Override
    public net.minecraft.world.entity.LivingEntity getControllingPassenger() {
        if (!this.getPassengers().isEmpty() && this.getPassengers().get(0) instanceof Player player) {
            return player;
        }
        return super.getControllingPassenger();
    }

    /**
     * Seats the rider 0.15 blocks behind center, 1.4 up (orig
     * Ostrich.java:542-547 — forward offset -0.15f, mounted y-offset 1.4
     * at :321-323).
     */
    @Override
    protected void positionRider(net.minecraft.world.entity.Entity passenger,
            net.minecraft.world.entity.Entity.MoveFunction callback) {
        if (!this.hasPassenger(passenger)) return;
        double rx = this.getX() + 0.15 * Math.sin(Math.toRadians(this.getYRot()));
        double ry = this.getY() + 1.4;
        double rz = this.getZ() - 0.15 * Math.cos(Math.toRadians(this.getYRot()));
        callback.accept(passenger, rx, ry, rz);
    }

    /**
     * Client-predicted ridden running (ENT-K-044): the riding client runs the
     * original sprint/jump physics (orig Ostrich.java:401-535, constants in
     * {@link #RIDER_RUN_CONFIG}) and syncs position like a vanilla horse.
     */
    @Override
    protected void tickRidden(Player rider, net.minecraft.world.phys.Vec3 travelVector) {
        super.tickRidden(rider, travelVector);
        if (this.isControlledByLocalInstance()) {
            this.riderRun.tick(this, rider, this.riderFlyUp, false);
        }
    }

    /**
     * Skips vanilla travel while player-ridden: {@link #tickRidden} already
     * applied the full move via {@code RiderFlightController}, so running
     * vanilla travel too would integrate the motion twice.
     */
    @Override
    public void travel(net.minecraft.world.phys.Vec3 travelVector) {
        if (this.isControlledByLocalInstance() && this.getControllingPassenger() instanceof Player) {
            return;
        }
        super.travel(travelVector);
    }

    /**
     * {@inheritDoc}
     *
     * <p>For the Ostrich the fly-up key is the FAST running jump (orig
     * Ostrich.java:470-478); the descend key is ignored — a runner has no
     * downward control.</p>
     */
    @Override
    public void setRiderVerticalInput(boolean up, boolean down) {
        this.riderFlyUp = up;
    }

    @Nullable
    @Override
    protected SoundEvent getAmbientSound() { return null; }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SND_CRYO_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SND_CRYO_DEATH;
    }

    @Override
    protected float getSoundVolume() { return 0.4f; }
    @Override
    public boolean isFood(ItemStack stack) { return stack.is(Items.WHEAT); }

    @Override
    public boolean causeFallDamage(float fallDistance, float multiplier, DamageSource source) { return false; }
    @Override
    protected void checkFallDamage(double y, boolean onGround, BlockState state, BlockPos pos) { }

    @Override
    public boolean removeWhenFarAway(double dist) {
        if (this.isBaby()) return false;
        if (this.getFirstPassenger() != null) return false;
        if (this.isPersistenceRequired()) return false;
        return !this.isTame();
    }

    /** orig Ostrich.java:325-338 — y>=50; daytime; 1-in-4 dice; no other Ostrich within 16/6/16. */
    @Override
    public boolean checkSpawnRules(net.minecraft.world.level.LevelAccessor level,
                                   net.minecraft.world.entity.MobSpawnType spawnType) {
        if (this.getY() < 50.0) return false;
        if (!OriginalSpawnGates.isDaytime(level)) return false;
        if (this.random.nextInt(4) != 1) return false;
        return !OriginalSpawnGates.anyOtherNearby(this, level, Ostrich.class, 16.0, 6.0, 16.0);
    }

    @Override
    public void addAdditionalSaveData(net.minecraft.nbt.CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        // Exposes the tame flag to the loot-table NBT predicate so the tamed-only
        // poppy drop (orig Ostrich.java:283-294) stays data-driven in ostrich.json —
        // same convention as Gazelle/Camarasaurus.
        tag.putBoolean("OreSpawnTamed", this.isTame());
    }

    @Nullable
    @Override
    public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob otherParent) {
        return new Ostrich(ModEntities.OSTRICH.get(), this.level());
    }
}
