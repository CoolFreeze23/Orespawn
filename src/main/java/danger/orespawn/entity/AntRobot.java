package danger.orespawn.entity;

import java.util.Comparator;
import java.util.List;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.resources.ResourceLocation;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.client.RenderSpiderRobotInfo;

public class AntRobot extends Mob {
    private static final EntityDataAccessor<Integer> DATA_ATTACKING =
            SynchedEntityData.defineId(AntRobot.class, EntityDataSerializers.INT);

    private static final double CHASE_SPEED = 0.2;
    private static final double KNOCKBACK_HORIZONTAL = 0.7;
    private static final double KNOCKBACK_VERTICAL = 0.1;
    private static final double PLAYER_OR_REMOVED_VERTICAL_MULTIPLIER = 2.0;
    private static final float STOMP_DAMAGE = 3.5f;
    private static final double STOMP_KNOCKBACK = 0.6;
    private static final float PARTICLE_OFFSET_BLOCKS = 4.0f;

    private static final int LEG_COUNT = 6;
    private static final double[] LEG_Y_ANGLES = {
        Math.toRadians(60), Math.toRadians(120), Math.toRadians(180),
        Math.toRadians(240), Math.toRadians(300), Math.toRadians(360)
    };
    private static final float[] LEG_OFFSETS = {1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f};
    private static final float[] LEG_Y_OFFS = {0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f};
    private final RenderSpiderRobotInfo renderInfo = new RenderSpiderRobotInfo(LEG_COUNT);

    private final Comparator<Entity> targetSorter;
    private final float moveSpeed = 0.3f;
    private int playing = 0;
    private int rideTicker = 0;
    private int owned = 0;

    public AntRobot(EntityType<? extends AntRobot> type, Level level) {
        super(type, level);
        this.xpReward = 150;
        this.targetSorter = Comparator.comparingDouble(this::distanceToSqr);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new LookAtPlayerGoal(this, Player.class, 12.0f));
        this.goalSelector.addGoal(2, new RandomLookAroundGoal(this));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 350.0)
                .add(Attributes.MOVEMENT_SPEED, 0.3)
                .add(Attributes.ATTACK_DAMAGE, 35.0)
                .add(Attributes.ARMOR, 6.0);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_ATTACKING, 0);
    }

    public int getAttacking() { return this.entityData.get(DATA_ATTACKING); }
    public void setAttacking(int value) { this.entityData.set(DATA_ATTACKING, value); }

    public void setOwned() { this.owned = 1; }
    public int getOwned() { return this.owned; }

    @Override
    protected void customServerAiStep() {
        if (this.isRemoved()) return;
        if (this.getFirstPassenger() != null) return;
        super.customServerAiStep();

        if (this.owned == 0) {
            if (this.getRandom().nextInt(20) == 0) {
                feetFindSomethingToHit();
            }
            LivingEntity currentTarget = this.getTarget();
            if (this.getRandom().nextInt(150) == 0) this.setTarget(null);
            if (currentTarget != null && !currentTarget.isAlive()) {
                this.setTarget(null);
                currentTarget = null;
            }
            if (currentTarget == null) currentTarget = findSomethingToAttack();
            if (currentTarget != null) {
                this.lookAt(currentTarget, 10.0f, 10.0f);
                if (this.distanceToSqr(currentTarget) > 16.0) {
                    double deltaZ = currentTarget.getZ() - this.getZ();
                    double deltaX = currentTarget.getX() - this.getX();
                    double yawToTarget = Math.atan2(deltaZ, deltaX);
                    this.setDeltaMovement(
                            CHASE_SPEED * Math.cos(yawToTarget),
                            this.getDeltaMovement().y,
                            CHASE_SPEED * Math.sin(yawToTarget));
                }
                double meleeRange = (6.0f + currentTarget.getBbWidth() / 2.0f);
                if (this.distanceToSqr(currentTarget) < meleeRange * meleeRange) {
                    this.setAttacking(1);
                    this.doHurtTarget(currentTarget);
                } else {
                    this.setAttacking(0);
                }
            } else {
                this.setAttacking(0);
            }
        }
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (source.getMsgId().equals("inWall") || source.getMsgId().equals("cactus")
                || source.getMsgId().equals("inFire") || source.getMsgId().equals("onFire")
                || source.getMsgId().equals("magic") || source.getMsgId().equals("starve")) {
            return false;
        }
        Entity attacker = source.getEntity();
        if (attacker instanceof LivingEntity livingAttacker) {
            this.setTarget(livingAttacker);
            this.lookAt(attacker, 20.0f, 20.0f);
        }
        return super.hurt(source, amount);
    }

    @Override
    public boolean causeFallDamage(float dist, float mult, DamageSource source) {
        return false;
    }

    @Override
    public void tick() {
        super.tick();
        this.clearFire();

        if (!this.level().isClientSide() && this.getFirstPassenger() != null && this.getRandom().nextInt(9) == 0) {
            LivingEntity riderTarget = findSomethingToAttack();
            if (riderTarget != null) {
                double meleeRange = (6.0f + riderTarget.getBbWidth() / 2.0f);
                if (this.distanceToSqr(riderTarget) < meleeRange * meleeRange) {
                    this.setAttacking(1);
                    this.doHurtTarget(riderTarget);
                }
            } else {
                this.setAttacking(0);
            }
        }

        float particleOffsetX = (float) (PARTICLE_OFFSET_BLOCKS * Math.cos(Math.toRadians(this.getYRot() - 80.0f)));
        float particleOffsetZ = (float) (PARTICLE_OFFSET_BLOCKS * Math.sin(Math.toRadians(this.getYRot() - 80.0f)));
        if (this.level().isClientSide()) {
            if (this.getRandom().nextInt(18) == 0) {
                this.level().addParticle(ParticleTypes.FLAME,
                        getX() + particleOffsetX, getY() + 0.5, getZ() + particleOffsetZ, 0, 0, 0);
            }
            if (this.getRandom().nextInt(7) == 0) {
                this.level().addParticle(ParticleTypes.SMOKE,
                        getX() + particleOffsetX, getY() + 0.5, getZ() + particleOffsetZ, 0, 0, 0);
            }
        }

        if (this.playing > 0) --this.playing;
        if (this.getFirstPassenger() != null && this.playing == 0 && this.getRandom().nextInt(80) == 1) {
            this.playSound(SoundEvent.createVariableRangeEvent(
                    ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "robotspider")), 0.35f, 1.0f);
            this.playing = 125;
        }
        this.rideTicker += this.getRandom().nextInt(3);
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        if (target instanceof LivingEntity livingTarget) {
            float yawToTarget = (float) Math.atan2(livingTarget.getZ() - this.getZ(), livingTarget.getX() - this.getX());
            boolean hurtApplied = livingTarget.hurt(this.damageSources().mobAttack(this), 35.0f);
            double verticalKnockback = KNOCKBACK_VERTICAL;
            if (livingTarget.isRemoved() || livingTarget instanceof Player) {
                verticalKnockback *= PLAYER_OR_REMOVED_VERTICAL_MULTIPLIER;
            }
            if (hurtApplied) {
                livingTarget.push(
                        Math.cos(yawToTarget) * KNOCKBACK_HORIZONTAL,
                        verticalKnockback,
                        Math.sin(yawToTarget) * KNOCKBACK_HORIZONTAL);
            }
            return hurtApplied;
        }
        return false;
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);
        if (this.owned == 0) return InteractionResult.PASS;
        if (held.is(Items.IRON_INGOT) && this.distanceToSqr(player) < 25.0) {
            if (!this.level().isClientSide()) {
                float heal = this.getMaxHealth() - this.getHealth();
                if (heal > 100.0f) heal = 100.0f;
                if (heal > 0) this.heal(heal);
            }
            if (!player.getAbilities().instabuild) held.shrink(1);
            return InteractionResult.sidedSuccess(this.level().isClientSide());
        }
        if (this.getFirstPassenger() != null && this.getFirstPassenger() instanceof Player
                && this.getFirstPassenger() != player) {
            return InteractionResult.PASS;
        }
        if (!this.level().isClientSide() && this.getFirstPassenger() == null && this.distanceToSqr(player) < 16.0) {
            player.startRiding(this);
            this.playSound(SoundEvent.createVariableRangeEvent(
                    ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "robotspidermount")), 0.45f, 1.0f);
        }
        return InteractionResult.sidedSuccess(this.level().isClientSide());
    }

    @Override
    public boolean shouldRiderSit() {
        return true;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("AntRobotOwned", this.owned);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.owned = tag.getInt("AntRobotOwned");
    }

    private void feetFindSomethingToHit() {
        AABB searchBox = this.getBoundingBox().inflate(10.0, 8.0, 10.0);
        List<LivingEntity> entities = this.level().getEntitiesOfClass(LivingEntity.class, searchBox);
        for (LivingEntity stompTarget : entities) {
            if (feetIsSuitableTarget(stompTarget)) {
                float yawToTarget = (float) Math.atan2(stompTarget.getZ() - this.getZ(), stompTarget.getX() - this.getX());
                boolean hurtApplied = stompTarget.hurt(this.damageSources().mobAttack(this), STOMP_DAMAGE);
                if (hurtApplied) {
                    stompTarget.push(
                            Math.cos(yawToTarget) * STOMP_KNOCKBACK,
                            0.1,
                            Math.sin(yawToTarget) * STOMP_KNOCKBACK);
                }
            }
        }
    }

    private boolean feetIsSuitableTarget(LivingEntity target) {
        if (target == null || target == this || !target.isAlive()) return false;
        if (target instanceof AntRobot) return false;
        if (target == this.getFirstPassenger()) return false;
        double dist = this.distanceTo(target);
        if (dist > 9.0f || dist < 6.0f) return false;
        if (target instanceof Player player && player.getAbilities().instabuild) return false;
        return true;
    }

    private LivingEntity findSomethingToAttack() {
        AABB searchBox = this.getBoundingBox().inflate(12.0, 12.0, 12.0);
        List<LivingEntity> entities = this.level().getEntitiesOfClass(LivingEntity.class, searchBox);
        for (LivingEntity candidate : entities) {
            if (isSuitableTarget(candidate)) return candidate;
        }
        return null;
    }

    private boolean isSuitableTarget(LivingEntity target) {
        if (target == null || target == this || !target.isAlive()) return false;
        if (target instanceof AntRobot) return false;
        if (target == this.getFirstPassenger()) return false;
        if (target instanceof Player player && player.getAbilities().instabuild) return false;
        return true;
    }

    public RenderSpiderRobotInfo getRenderSpiderRobotInfo() {
        renderInfo.gpcounter = this.tickCount;
        float walkPhase = this.tickCount * 0.15f;
        for (int i = 0; i < LEG_COUNT; i++) {
            renderInfo.ymid[i] = LEG_Y_ANGLES[i];
            renderInfo.ydisplayangle[i] = (float) LEG_Y_ANGLES[i];
            renderInfo.legoff[i] = LEG_OFFSETS[i];
            renderInfo.yoff[i] = LEG_Y_OFFS[i];
            float phase = walkPhase + (float)(i * Math.PI / 3.0);
            float swing = (float) Math.sin(phase) * 0.3f;
            renderInfo.p1xangle[i] = -0.4 + swing;
            renderInfo.p2xangle[i] = 0.6 + swing * 0.5;
            renderInfo.p3xangle[i] = -0.2 - swing * 0.3;
            renderInfo.uddisplayangle[i] = 0.0f;
        }
        return renderInfo;
    }
}
