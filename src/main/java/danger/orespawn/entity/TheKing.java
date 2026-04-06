package danger.orespawn.entity;

import java.util.Comparator;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import danger.orespawn.ModItems;

public class TheKing extends Monster {
    private static final EntityDataAccessor<Integer> DATA_ATTACKING =
            SynchedEntityData.defineId(TheKing.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_PLAY_NICELY =
            SynchedEntityData.defineId(TheKing.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_IS_END =
            SynchedEntityData.defineId(TheKing.class, EntityDataSerializers.INT);

    private static final int MAX_HEALTH_VALUE = 6000;
    private static final double ATTACK_DAMAGE_VALUE = 250.0;
    private static final int DEFENSE_VALUE = 12;
    private static final double MOVE_SPEED_VALUE = 0.62;

    private BlockPos currentFlightTarget = null;
    private final Comparator<Entity> targetSorter;
    private LivingEntity rt = null;
    private double attdam = ATTACK_DAMAGE_VALUE;
    private int hurt_timer = 0;
    private int homex = 0;
    private int homez = 0;
    private int stream_count = 0;
    private int stream_count_l = 0;
    private int stream_count_i = 0;
    private int ticker = 0;
    private int player_hit_count = 0;
    private int backoff_timer = 0;
    private int guard_mode = 0;
    private volatile int head_found = 0;
    private int wing_sound = 0;
    private int large_unknown_detected = 0;
    private int isEnd = 0;
    private int endCounter = 0;

    public TheKing(EntityType<? extends TheKing> type, Level level) {
        super(type, level);
        // Entity dimensions (22x24) should be set in EntityType.Builder.sized(22.0f, 24.0f)
        this.xpReward = 25000;
        this.noCulling = true;
        this.noPhysics = true;
        this.setNoGravity(true);
        this.targetSorter = Comparator.comparingDouble(this::distanceToSqr);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, MAX_HEALTH_VALUE)
                .add(Attributes.MOVEMENT_SPEED, MOVE_SPEED_VALUE)
                .add(Attributes.ATTACK_DAMAGE, ATTACK_DAMAGE_VALUE)
                .add(Attributes.FOLLOW_RANGE, 128.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0)
                .add(Attributes.ARMOR, DEFENSE_VALUE);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_ATTACKING, 0);
        builder.define(DATA_PLAY_NICELY, 0);
        builder.define(DATA_IS_END, 0);
    }

    public int mygetMaxHealth() {
        return MAX_HEALTH_VALUE;
    }

    public final int getAttacking() {
        return this.entityData.get(DATA_ATTACKING);
    }

    public final void setAttacking(int value) {
        this.entityData.set(DATA_ATTACKING, value);
    }

    public int getPlayNicely() {
        return this.entityData.get(DATA_PLAY_NICELY);
    }

    public int getIsEnd() {
        return this.entityData.get(DATA_IS_END);
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distSq) {
        return true;
    }

    @Override
    protected boolean shouldDespawnInPeaceful() {
        return false;
    }

    @Override
    public boolean removeWhenFarAway(double dist) {
        return false;
    }

    @Override
    protected float getSoundVolume() {
        return 1.35f;
    }

    @Override
    public float getVoicePitch() {
        return 1.0f;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        // TODO: Replace with custom SoundEvent "orespawn:king_living"
        return SoundEvents.ENDER_DRAGON_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        // TODO: Replace with custom SoundEvent "orespawn:king_hit"
        return SoundEvents.ENDER_DRAGON_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        // TODO: Replace with custom SoundEvent "orespawn:trex_death"
        return SoundEvents.ENDER_DRAGON_DEATH;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    protected void doPush(Entity entity) {
    }

    @Override
    public boolean canBreatheUnderwater() {
        return true;
    }

    @Override
    public boolean causeFallDamage(float fallDistance, float multiplier, DamageSource source) {
        return false;
    }

    @Override
    protected void checkFallDamage(double y, boolean onGround, BlockState state, BlockPos pos) {
    }

    @Override
    public void thunderHit(ServerLevel level, LightningBolt bolt) {
    }

    // ---- Tick / AI ----

    @Override
    public void tick() {
        super.tick();
        this.wing_sound++;
        if (this.wing_sound > 30) {
            if (!this.level().isClientSide) {
                // TODO: Replace with custom SoundEvent "orespawn:MothraWings"
                this.playSound(SoundEvents.ENDER_DRAGON_FLAP, 1.75f, 0.75f);
            }
            this.wing_sound = 0;
        }

        this.noPhysics = true;
        Vec3 motion = this.getDeltaMovement();
        this.setDeltaMovement(motion.x, motion.y * 0.6, motion.z);

        if (this.player_hit_count < 10 && this.getHealth() < (float)(this.mygetMaxHealth() * 2 / 3)) {
            this.attdam = ATTACK_DAMAGE_VALUE * 2;
        }
        if (this.player_hit_count < 10 && this.getHealth() < (float)(this.mygetMaxHealth() / 2)) {
            this.attdam = ATTACK_DAMAGE_VALUE * 4;
        }
        if (this.player_hit_count < 10 && this.getHealth() < (float)(this.mygetMaxHealth() / 4)) {
            this.attdam = ATTACK_DAMAGE_VALUE * 8;
        }
        if (this.player_hit_count < 10 && this.getHealth() < (float)(this.mygetMaxHealth() / 8)) {
            this.attdam = ATTACK_DAMAGE_VALUE * 16;
        }

        if (this.level().isClientSide) {
            this.isEnd = this.entityData.get(DATA_IS_END);
            if (this.isEnd != 0 && this.getRandom().nextInt(3) == 1) {
                float f = 7.0f;
                Vec3 mot = this.getDeltaMovement();
                for (int i = 0; i < 10; i++) {
                    this.level().addParticle(ParticleTypes.FIREWORK,
                            this.getX() - (double) f * Math.sin(Math.toRadians(this.getYRot())),
                            this.getY() + 14.0,
                            this.getZ() + (double) f * Math.cos(Math.toRadians(this.getYRot())),
                            (this.getRandom().nextGaussian() - this.getRandom().nextGaussian()) / 4.0 + mot.x * 6.0,
                            (this.getRandom().nextGaussian() - this.getRandom().nextGaussian()) / 4.0,
                            (this.getRandom().nextGaussian() - this.getRandom().nextGaussian()) / 4.0 + mot.z * 6.0);
                }
            }
        }
    }

    @Override
    protected void customServerAiStep() {
        int xdir;
        int zdir;
        int attrand = 5;
        int which;
        LivingEntity e;
        LivingEntity f;

        if (this.isRemoved()) return;
        super.customServerAiStep();

        this.entityData.set(DATA_IS_END, this.isEnd);
        this.entityData.set(DATA_PLAY_NICELY, 0);

        // ---- End-game phase 1: dialogue ----
        if (this.isEnd == 1) {
            this.endCounter++;
            this.noPhysics = true;
            this.setDeltaMovement(Vec3.ZERO);
            this.hurt_timer = 10;
            if (this.isRemoved()) return;

            Player p = this.findNearestPlayer();
            if (p != null) {
                this.lookAt(p, 10.0f, 10.0f);
                p.setDeltaMovement(Vec3.ZERO);
                double dd0 = this.getX() - p.getX();
                double dd1 = this.getZ() - p.getZ();
                float f2 = (float) (Math.atan2(dd1, dd0) * 180.0 / Math.PI) - 90.0f;
                p.setYRot(f2);
                p.setHealth(1.0f);
            }

            if (this.endCounter == 10) {
                this.msgToPlayers("The King: Enough of this charade. I am done. You have shown me what I wanted to know.");
                return;
            }
            if (this.endCounter == 80) {
                this.msgToPlayers("The King: That's right my little pet. It has all been a game. You never killed me. You can't.");
                return;
            }
            if (this.endCounter == 160) {
                this.msgToPlayers("The King: I am the one. The only. The many. I exist within both space and time. Everywhere and always.");
                return;
            }
            if (this.endCounter == 240) {
                this.msgToPlayers("The King: I used you to learn your ways, and I have reached my conclusion on your species.");
                return;
            }
            if (this.endCounter == 300) {
                this.msgToPlayers("The King: You have 10 seconds to run...");
                return;
            }
            if (this.endCounter == 320) { this.msgToPlayers("9."); return; }
            if (this.endCounter == 340) { this.msgToPlayers("8."); return; }
            if (this.endCounter == 360) { this.msgToPlayers("7."); return; }
            if (this.endCounter == 380) { this.msgToPlayers("6."); return; }
            if (this.endCounter == 400) { this.msgToPlayers("5."); return; }
            if (this.endCounter == 420) { this.msgToPlayers("4."); return; }
            if (this.endCounter == 440) { this.msgToPlayers("3."); return; }
            if (this.endCounter == 460) { this.msgToPlayers("2."); return; }
            if (this.endCounter == 480) { this.msgToPlayers("1."); return; }
            if (this.endCounter == 500) {
                this.msgToPlayers("The King: Prepare to die!");
                this.isEnd = 2;
                return;
            }
            return;
        }

        // ---- End-game phase 2: enraged ----
        if (this.isEnd == 2) {
            this.hurt_timer = 10;
            this.player_hit_count = 0;
            this.stream_count = 10;
            this.stream_count_l = 10;
            this.stream_count_i = 10;
            attrand = 3;
            this.guard_mode = 0;
            this.large_unknown_detected = 1;
            if (this.backoff_timer > 0) this.backoff_timer--;
        }

        if (this.hurt_timer > 0) this.hurt_timer--;

        if ((this.homex == 0 && this.homez == 0) || this.guard_mode == 0) {
            this.homex = (int) this.getX();
            this.homez = (int) this.getZ();
        }

        this.ticker++;
        if (this.ticker > 30000) this.ticker = 0;
        if (this.ticker % 80 == 0) this.stream_count = 10;
        if (this.ticker % 90 == 0) this.stream_count_l = 5;
        if (this.ticker % 70 == 0) this.stream_count_i = 8;
        if (this.backoff_timer > 0) this.backoff_timer--;

        if (this.player_hit_count < 10 && this.getHealth() < (float) (this.mygetMaxHealth() / 2)) {
            attrand = 3;
        }

        this.noPhysics = true;

        if (this.currentFlightTarget == null) {
            this.currentFlightTarget = BlockPos.containing(this.getX(), this.getY(), this.getZ());
        }

        if (this.tooFarFromHome() || this.getRandom().nextInt(200) == 0 || this.flightTargetDistSqr() < 9.1) {
            zdir = this.getRandom().nextInt(120);
            xdir = this.getRandom().nextInt(120);
            if (this.getRandom().nextInt(2) == 0) zdir = -zdir;
            if (this.getRandom().nextInt(2) == 0) xdir = -xdir;

            int altAdj = computeAltitudeAdjustment(this.homex, this.homez);
            int targetY = Math.min((int) (this.getY() + altAdj), 230);
            this.currentFlightTarget = new BlockPos(this.homex + xdir, targetY, this.homez + zdir);

        } else if (this.getRandom().nextInt(attrand) == 0) {
            // ---- Target acquisition ----
            e = this.rt;

            if (e instanceof TheKing) {
                // TODO: also check KingHead
                this.rt = null;
                e = null;
            }
            if (e != null) {
                float d1 = (float) (e.getX() - this.homex);
                float d2 = (float) (e.getZ() - this.homez);
                float dist = (float) Math.sqrt(d1 * d1 + d2 * d2);
                if (!e.isAlive() || this.getRandom().nextInt(250) == 1
                        || (dist > 128.0f && this.guard_mode == 1)) {
                    e = null;
                    this.rt = null;
                }
                if (e != null && !this.MyCanSee(e)) {
                    e = null;
                }
            }

            f = this.findSomethingToAttack();

            if (this.head_found == 0) {
                // TODO: Spawn KingHead entity at this.getX(), this.getY() + 20, this.getZ()
            }

            if (e == null) e = f;

            if (e != null) {
                this.setAttacking(1);

                if (this.backoff_timer == 0) {
                    int targetDist = Math.min((int) (e.getY() + e.getBbHeight() / 2.0f + 1.0), 230);
                    this.currentFlightTarget = new BlockPos((int) e.getX(), targetDist, (int) e.getZ());
                    if (this.getRandom().nextInt(70) == 1) {
                        this.backoff_timer = 80 + this.getRandom().nextInt(80);
                    }
                } else if (this.flightTargetDistSqr() < 9.1) {
                    zdir = this.getRandom().nextInt(20) + 30;
                    xdir = this.getRandom().nextInt(20) + 30;
                    if (this.getRandom().nextInt(2) == 0) zdir = -zdir;
                    if (this.getRandom().nextInt(2) == 0) xdir = -xdir;

                    int altAdj = computeAltitudeAdjustment((int) e.getX(), (int) e.getZ());
                    int targetY = Math.min((int) (this.getY() + altAdj), 230);
                    this.currentFlightTarget = new BlockPos((int) e.getX() + xdir, targetY, (int) e.getZ() + zdir);
                }

                // Melee range area + direct attack
                if (this.distanceToSqr(e) < 900.0) {
                    if (this.getRandom().nextInt(2) == 1) {
                        this.doJumpDamage(this.getX(), this.getY(), this.getZ(),
                                15.0, ATTACK_DAMAGE_VALUE / 4, 0);
                    }
                    this.doHurtTarget(e);
                }

                // Forward area damage
                double dx = this.getX() + 20.0 * Math.sin(Math.toRadians(this.yHeadRot));
                double dz = this.getZ() - 20.0 * Math.cos(Math.toRadians(this.yHeadRot));
                if (this.getRandom().nextInt(3) == 1) {
                    this.doJumpDamage(dx, this.getY() + 10.0, dz,
                            15.0, ATTACK_DAMAGE_VALUE / 2, 1);
                }

                // Ranged attacks when target is far
                if (this.getHorizontalDistanceSqToEntity(e) > 900.0) {
                    which = this.getRandom().nextInt(3);
                    if (which == 0 && this.stream_count > 0) {
                        this.setAttacking(1);
                        if (isAimedAt(e)) this.firecanon(e);
                    } else if (which == 1 && this.stream_count_l > 0) {
                        this.setAttacking(1);
                        if (isAimedAt(e)) this.firecanonl(e);
                    } else if (this.stream_count_i > 0) {
                        this.setAttacking(1);
                        if (isAimedAt(e)) this.firecanoni(e);
                    }
                }
            } else {
                this.setAttacking(0);
                this.stream_count = 10;
                this.stream_count_l = 5;
                this.stream_count_i = 8;
            }
        }

        // Purple power in enraged mode
        if (this.getAttacking() != 0 && this.isEnd == 2) {
            /* TODO: Spawn PurplePower entity
            double xzoff = 10.0;
            double yoff = 14.0;
            PurplePower pwr = new PurplePower(this.level(), ...);
            pwr.moveTo(
                this.getX() - xzoff * Math.sin(Math.toRadians(this.getYRot())),
                this.getY() + yoff,
                this.getZ() + xzoff * Math.cos(Math.toRadians(this.getYRot())),
                0, 0);
            Vec3 mot = this.getDeltaMovement();
            pwr.setDeltaMovement(mot.x * 3.0, 0, mot.z * 3.0);
            pwr.setPurpleType(10);
            this.level().addFreshEntity(pwr);
            */
        }

        // ---- Flight movement ----
        double goalX = this.currentFlightTarget.getX() + 0.5 - this.getX();
        double goalY = this.currentFlightTarget.getY() + 0.1 - this.getY();
        double goalZ = this.currentFlightTarget.getZ() + 0.5 - this.getZ();

        Vec3 mot = this.getDeltaMovement();
        double newMx = mot.x + (Math.signum(goalX) * 0.7 - mot.x) * 0.35;
        double newMy = mot.y + (Math.signum(goalY) * 0.69999 - mot.y) * 0.3;
        double newMz = mot.z + (Math.signum(goalZ) * 0.7 - mot.z) * 0.35;
        this.setDeltaMovement(newMx, newMy, newMz);

        float targetYaw = (float) (Math.atan2(newMz, newMx) * 180.0 / Math.PI) - 90.0f;
        float yawDelta = Mth.wrapDegrees(targetYaw - this.getYRot());
        this.zza = 1.0f;
        this.setYRot(this.getYRot() + yawDelta / 8.0f);

        // ---- Passive healing ----
        if (this.getRandom().nextInt(30) == 1 && this.getHealth() < this.mygetMaxHealth()) {
            this.heal(5.0f);
            if (this.large_unknown_detected != 0) {
                this.heal(200.0f);
            }
        }
        if (this.player_hit_count < 10 && this.getHealth() < 2000.0f) {
            this.heal(2000.0f - this.getHealth());
        }
    }

    // ---- Combat ----

    @Override
    public boolean doHurtTarget(Entity target) {
        if (target == null) return false;

        if (target instanceof LivingEntity living) {
            float s = living.getBbHeight() * living.getBbWidth();
            if (s > 30.0f
                    // TODO: && !MyUtils.isRoyalty(living)
                    // TODO: Exclude Godzilla, GodzillaHead, PitchBlack, Kraken
                    && !(living instanceof EnderDragon)) {
                living.setHealth(living.getHealth() / 2.0f);
                living.hurt(this.damageSources().mobAttack(this), (float) this.attdam * 10.0f);
                this.large_unknown_detected = 1;
            }
        }

        if (target instanceof EnderDragon dragon) {
            DamageSource genDmg = this.damageSources().generic();
            if (this.getRandom().nextInt(6) == 1) {
                dragon.head.hurt(genDmg, (float) this.attdam);
            } else {
                dragon.body.hurt(genDmg, (float) this.attdam);
            }
        }

        boolean hit = target.hurt(this.damageSources().mobAttack(this), (float) this.attdam);
        if (hit) {
            double ks = 3.3;
            double inair = 0.25;
            float angle = (float) Math.atan2(target.getZ() - this.getZ(), target.getX() - this.getX());
            inair += this.getRandom().nextFloat() * 0.25;
            if (target.isRemoved() || target instanceof Player) {
                inair *= 1.5;
            }
            target.push(Math.cos(angle) * ks, inair, Math.sin(angle) * ks);
        }
        return hit;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        boolean ret = false;
        if (this.hurt_timer > 0) return false;

        float dm = Math.min(amount, 750.0f);

        if (source.getMsgId().equals("inWall")) return false;

        Entity attacker = source.getEntity();
        if (attacker instanceof LivingEntity living) {
            float s = living.getBbHeight() * living.getBbWidth();
            if (s > 30.0f
                    // TODO: && !MyUtils.isRoyalty(living)
                    // TODO: Exclude Godzilla, GodzillaHead, PitchBlack, Kraken
                    ) {
                dm /= 10.0f;
                this.hurt_timer = 50;
                this.large_unknown_detected = 1;
            }
            if (living instanceof Monster && s < 3.0f) {
                living.discard();
                return false;
            }
        }

        if (!source.getMsgId().equals("cactus")) {
            this.hurt_timer = 20;
            ret = super.hurt(source, dm);
            if (attacker instanceof Player) {
                this.player_hit_count++;
            }
            if (attacker instanceof LivingEntity living && this.currentFlightTarget != null
                    // TODO: && !MyUtils.isRoyalty(attacker)
                    ) {
                this.rt = living;
                int targetY = Math.min((int) attacker.getY(), 230);
                this.currentFlightTarget = new BlockPos((int) attacker.getX(), targetY, (int) attacker.getZ());
            }
        }
        return ret;
    }

    @Override
    public int getArmorValue() {
        if (this.large_unknown_detected != 0) return 25;
        if (this.player_hit_count < 10 && this.getHealth() < (float) (this.mygetMaxHealth() * 2 / 3))
            return DEFENSE_VALUE + 1;
        if (this.player_hit_count < 10 && this.getHealth() < (float) (this.mygetMaxHealth() / 2))
            return DEFENSE_VALUE + 2;
        if (this.player_hit_count < 10 && this.getHealth() < (float) (this.mygetMaxHealth() / 4))
            return DEFENSE_VALUE + 3;
        return DEFENSE_VALUE;
    }

    // ---- Projectile attacks ----

    private void firecanon(LivingEntity e) {
        double yoff = 14.0;
        double xzoff = 32.0;
        double cx = this.getX() - xzoff * Math.sin(Math.toRadians(this.getYRot()));
        double cz = this.getZ() + xzoff * Math.cos(Math.toRadians(this.getYRot()));

        if (this.stream_count > 0) {
            this.playSound(SoundEvents.TNT_PRIMED, 1.0f,
                    1.0f / (this.getRandom().nextFloat() * 0.4f + 0.8f));

            /* TODO: Spawn BetterFireball projectiles
            BetterFireball bf = new BetterFireball(this.level(), this,
                    e.getX() - cx,
                    e.getY() + e.getBbHeight() / 2.0f - (this.getY() + yoff),
                    e.getZ() - cz);
            bf.moveTo(cx, this.getY() + yoff, cz, this.getYRot(), 0.0f);
            bf.setPos(cx, this.getY() + yoff, cz);
            bf.setReallyBig();
            this.level().addFreshEntity(bf);

            for (int i = 0; i < 6; i++) {
                float r1 = 5.0f * (this.getRandom().nextFloat() - this.getRandom().nextFloat());
                float r2 = 3.0f * (this.getRandom().nextFloat() - this.getRandom().nextFloat());
                float r3 = 5.0f * (this.getRandom().nextFloat() - this.getRandom().nextFloat());
                bf = new BetterFireball(this.level(), this,
                        e.getX() - cx + r1,
                        e.getY() + e.getBbHeight() / 2.0f - (this.getY() + yoff) + r2,
                        e.getZ() - cz + r3);
                bf.moveTo(cx, this.getY() + yoff, cz, this.getYRot(), 0.0f);
                bf.setPos(cx, this.getY() + yoff, cz);
                bf.setBig();
                if (this.getRandom().nextInt(2) == 1) bf.setSmall();
                this.level().addFreshEntity(bf);
            }
            */

            this.level().playSound(null, cx, this.getY() + yoff, cz,
                    SoundEvents.ARROW_SHOOT, SoundSource.HOSTILE,
                    1.0f, 1.0f / (this.getRandom().nextFloat() * 0.4f + 0.8f));
            this.stream_count--;
        }
    }

    private void firecanonl(LivingEntity e) {
        double yoff = 14.0;
        double xzoff = 32.0;
        double cx = this.getX() - xzoff * Math.sin(Math.toRadians(this.getYRot()));
        double cz = this.getZ() + xzoff * Math.cos(Math.toRadians(this.getYRot()));

        if (this.stream_count_l > 0) {
            this.level().playSound(null, cx, this.getY() + yoff, cz,
                    SoundEvents.ARROW_SHOOT, SoundSource.HOSTILE,
                    1.0f, 1.0f / (this.getRandom().nextFloat() * 0.4f + 0.8f));

            /* TODO: Spawn ThunderBolt projectiles
            for (int i = 0; i < 3; i++) {
                ThunderBolt lb = new ThunderBolt(this.level(), cx, this.getY() + yoff, cz);
                lb.moveTo(cx, this.getY() + yoff, cz, 0.0f, 0.0f);
                double dx = e.getX() - lb.getX();
                double dy = e.getY() + 0.25 - lb.getY();
                double dz = e.getZ() - lb.getZ();
                float horizDist = Mth.sqrt((float)(dx * dx + dz * dz)) * 0.2f;
                lb.shoot(dx, dy + horizDist, dz, 1.4f, 4.0f);
                Vec3 lbMot = lb.getDeltaMovement();
                lb.setDeltaMovement(lbMot.x * 3.0, lbMot.y * 3.0, lbMot.z * 3.0);
                this.level().addFreshEntity(lb);
            }
            */

            this.stream_count_l--;
        }
    }

    private void firecanoni(LivingEntity e) {
        double yoff = 14.0;
        double xzoff = 32.0;
        double cx = this.getX() - xzoff * Math.sin(Math.toRadians(this.getYRot()));
        double cz = this.getZ() + xzoff * Math.cos(Math.toRadians(this.getYRot()));

        if (this.stream_count_i > 0) {
            this.level().playSound(null, cx, this.getY() + yoff, cz,
                    SoundEvents.ARROW_SHOOT, SoundSource.HOSTILE,
                    1.0f, 1.0f / (this.getRandom().nextFloat() * 0.4f + 0.8f));

            /* TODO: Spawn IceBall projectiles
            for (int i = 0; i < 5; i++) {
                IceBall lb = new IceBall(this.level(), cx, this.getY() + yoff, cz);
                lb.setIceMaker(1);
                lb.moveTo(cx, this.getY() + yoff, cz, 0.0f, 0.0f);
                double dx = e.getX() - lb.getX();
                double dy = e.getY() + 0.25 - lb.getY();
                double dz = e.getZ() - lb.getZ();
                float horizDist = Mth.sqrt((float)(dx * dx + dz * dz)) * 0.2f;
                lb.shoot(dx, dy + horizDist, dz, 1.4f, 4.0f);
                Vec3 lbMot = lb.getDeltaMovement();
                lb.setDeltaMovement(lbMot.x * 3.0, lbMot.y * 3.0, lbMot.z * 3.0);
                this.level().addFreshEntity(lb);
            }
            */

            this.stream_count_i--;
        }
    }

    private LivingEntity doJumpDamage(double x, double y, double z, double dist, double damage, int knock) {
        AABB bb = new AABB(x - dist, y - 10.0, z - dist, x + dist, y + 10.0, z + dist);
        List<LivingEntity> entities = this.level().getEntitiesOfClass(LivingEntity.class, bb);
        entities.sort(this.targetSorter);

        for (LivingEntity target : entities) {
            if (target == this || !target.isAlive()) continue;
            // TODO: if (MyUtils.isRoyalty(target)) continue;
            // TODO: if (target instanceof Ghost || target instanceof GhostSkelly) continue;

            target.hurt(this.damageSources().magic(), (float) damage / 2.0f);
            target.hurt(this.damageSources().generic(), (float) damage / 2.0f);

            target.playSound(SoundEvents.GENERIC_EXPLODE.value(), 0.65f,
                    1.0f + (this.getRandom().nextFloat() - this.getRandom().nextFloat()) * 0.5f);

            if (knock != 0) {
                double ks = 2.75;
                double inair = 0.65;
                float angle = (float) Math.atan2(target.getZ() - this.getZ(), target.getX() - this.getX());
                target.push(Math.cos(angle) * ks, inair, Math.sin(angle) * ks);
            }
        }
        return null;
    }

    // ---- Targeting ----

    private boolean isSuitableTarget(LivingEntity target) {
        if (target == null || target == this || !target.isAlive()) return false;

        // TODO: if (target instanceof KingHead) { head_found = 1; return false; }
        // TODO: if (MyUtils.isRoyalty(target)) return false;

        float d1 = (float) (target.getX() - this.homex);
        float d2 = (float) (target.getZ() - this.homez);
        if (Math.sqrt(d1 * d1 + d2 * d2) > 144.0f) return false;

        // TODO: if (MyUtils.isIgnoreable(target)) return false;

        if (this.isEnd == 2) {
            if (target instanceof Player player) {
                return !player.getAbilities().instabuild;
            }
            // TODO: if (target instanceof Girlfriend || target instanceof Boyfriend) return true;
            if (target instanceof Villager) return true;
        }

        if (!this.MyCanSee(target)) return false;

        if (target instanceof Player player) {
            return !player.getAbilities().instabuild;
        }
        if (target instanceof Horse) return true;
        if (target instanceof Monster) return true;
        if (target instanceof EnderDragon) return true;
        // TODO: return MyUtils.isAttackableNonMob(target);
        return false;
    }

    private LivingEntity findSomethingToAttack() {
        AABB searchBox = this.getBoundingBox().inflate(80.0, 64.0, 80.0);

        if (this.isEnd == 2) {
            List<Player> players = this.level().getEntitiesOfClass(Player.class, searchBox);
            players.sort(this.targetSorter);
            this.head_found = 1;
            for (Player player : players) {
                if (this.isSuitableTarget(player)) return player;
            }
        }

        List<LivingEntity> entities = this.level().getEntitiesOfClass(LivingEntity.class, searchBox);
        entities.sort(this.targetSorter);
        LivingEntity ret = null;
        this.head_found = 0;
        for (LivingEntity entity : entities) {
            if (ret == null && this.isSuitableTarget(entity)) {
                ret = entity;
            }
            if (ret != null && this.head_found != 0) break;
        }
        return ret;
    }

    // ---- Helpers ----

    private boolean isAimedAt(LivingEntity e) {
        double rr = Math.atan2(e.getZ() - this.getZ(), e.getX() - this.getX());
        double rhdir = Math.toRadians((this.yHeadRot + 90.0f) % 360.0f);
        double rdd = Math.abs(rr - rhdir) % (Math.PI * 2.0);
        if (rdd > Math.PI) rdd -= Math.PI * 2.0;
        return Math.abs(rdd) < 0.5;
    }

    public boolean MyCanSee(LivingEntity e) {
        double xzoff = 22.0;
        int nblks = 20;
        double cx = this.getX() - xzoff * Math.sin(Math.toRadians(this.getYRot()));
        double cz = this.getZ() + xzoff * Math.cos(Math.toRadians(this.getYRot()));
        float startx = (float) cx;
        float starty = (float) (this.getY() + this.getBbHeight() * 7.0f / 8.0f);
        float startz = (float) cz;
        float dx = (float) ((e.getX() - startx) / 20.0);
        float dy = (float) ((e.getY() + e.getBbHeight() / 2.0f - starty) / 20.0);
        float dz = (float) ((e.getZ() - startz) / 20.0);

        if (Math.abs(dx) > 1.0f) {
            dy /= Math.abs(dx);
            dz /= Math.abs(dx);
            nblks = (int) (nblks * Math.abs(dx));
            dx = Mth.clamp(dx, -1.0f, 1.0f);
        }
        if (Math.abs(dy) > 1.0f) {
            dx /= Math.abs(dy);
            dz /= Math.abs(dy);
            nblks = (int) (nblks * Math.abs(dy));
            dy = Mth.clamp(dy, -1.0f, 1.0f);
        }
        if (Math.abs(dz) > 1.0f) {
            dy /= Math.abs(dz);
            dx /= Math.abs(dz);
            nblks = (int) (nblks * Math.abs(dz));
            dz = Mth.clamp(dz, -1.0f, 1.0f);
        }

        for (int i = 0; i < nblks; i++) {
            startx += dx;
            starty += dy;
            startz += dz;
            BlockState state = this.level().getBlockState(BlockPos.containing(startx, starty, startz));
            if (state.isAir() || !state.getFluidState().isEmpty()) continue;
            return false;
        }
        return true;
    }

    private boolean tooFarFromHome() {
        float d1 = (float) (this.getX() - this.homex);
        float d2 = (float) (this.getZ() - this.homez);
        return Math.sqrt(d1 * d1 + d2 * d2) > 120.0f;
    }

    private double flightTargetDistSqr() {
        if (currentFlightTarget == null) return Double.MAX_VALUE;
        double dx = currentFlightTarget.getX() - this.getX();
        double dy = currentFlightTarget.getY() - this.getY();
        double dz = currentFlightTarget.getZ() - this.getZ();
        return dx * dx + dy * dy + dz * dz;
    }

    private double getHorizontalDistanceSqToEntity(Entity e) {
        double d1 = e.getZ() - this.getZ();
        double d2 = e.getX() - this.getX();
        return d1 * d1 + d2 * d2;
    }

    private int computeAltitudeAdjustment(int centerX, int centerZ) {
        int dist = 0;
        for (int i = -5; i <= 5; i += 5) {
            scan:
            for (int j = -5; j <= 5; j += 5) {
                BlockPos checkPos = new BlockPos(centerX + j, (int) this.getY(), centerZ + i);
                BlockState state = this.level().getBlockState(checkPos);
                if (!state.isAir()) {
                    for (int k = 1; k < 20; k++) {
                        state = this.level().getBlockState(checkPos.above(k));
                        dist++;
                        if (state.isAir()) continue scan;
                    }
                } else {
                    for (int k = 1; k < 20; k++) {
                        state = this.level().getBlockState(checkPos.below(k));
                        dist--;
                        if (!state.isAir()) continue scan;
                    }
                }
            }
        }
        return dist / 9 + 2;
    }

    private void msgToPlayers(String s) {
        List<Player> players = this.level().getEntitiesOfClass(Player.class,
                this.getBoundingBox().inflate(80.0, 64.0, 80.0));
        for (Player p : players) {
            p.sendSystemMessage(Component.literal(s));
        }
    }

    private Player findNearestPlayer() {
        List<Player> players = this.level().getEntitiesOfClass(Player.class,
                this.getBoundingBox().inflate(80.0, 64.0, 80.0));
        players.sort(this.targetSorter);
        return players.isEmpty() ? null : players.get(0);
    }

    public void setGuardMode(int i) {
        this.guard_mode = i;
    }

    public void setFree() {
        this.isEnd = 1;
    }

    // ---- Drops ----

    private void dropItemRand(ItemStack stack) {
        double ox = this.getX() + this.getRandom().nextInt(20) - this.getRandom().nextInt(20);
        double oy = this.getY() + 12.0;
        double oz = this.getZ() + this.getRandom().nextInt(20) - this.getRandom().nextInt(20);
        ItemEntity itemEntity = new ItemEntity(this.level(), ox, oy, oz, stack);
        this.level().addFreshEntity(itemEntity);
    }

    @Override
    protected void dropCustomDeathLoot(DamageSource source, int looting, boolean recentlyHit) {
        super.dropCustomDeathLoot(source, looting, recentlyHit);

        // TODO: Spawn "The Prince" entity at this.getX(), this.getY() + 10, this.getZ()

        dropItemRand(new ItemStack(ModItems.ROYAL_GUARDIAN_CHESTPLATE.get(), 1));
        dropItemRand(new ItemStack(ModItems.ROYAL_GUARDIAN_HELMET.get(), 1));
        dropItemRand(new ItemStack(ModItems.ROYAL_GUARDIAN_LEGGINGS.get(), 1));
        dropItemRand(new ItemStack(ModItems.ROYAL_GUARDIAN_BOOTS.get(), 1));
        dropItemRand(new ItemStack(ModItems.ROYAL_GUARDIAN_SWORD.get(), 1));

        int icount = BuiltInRegistries.ITEM.size();
        int j = 0;
        while (j < 150) {
            Item item = BuiltInRegistries.ITEM.byId(this.getRandom().nextInt(icount));
            if (item == null || item == Items.AIR) continue;
            j++;
            dropItemRand(new ItemStack(item, 1));
        }

        int bcount = BuiltInRegistries.BLOCK.size();
        j = 0;
        while (j < 150) {
            Block block = BuiltInRegistries.BLOCK.byId(this.getRandom().nextInt(bcount));
            if (block == null || block == Blocks.AIR) continue;
            Item blockItem = block.asItem();
            if (blockItem == Items.AIR) continue;
            j++;
            dropItemRand(new ItemStack(blockItem, 1));
        }
    }

    // ---- NBT ----

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("KingHomeX", this.homex);
        tag.putInt("KingHomeZ", this.homez);
        tag.putInt("GuardMode", this.guard_mode);
        tag.putInt("PlayerHits", this.player_hit_count);
        tag.putInt("IsEnd", this.isEnd);
        tag.putInt("EndCounter", this.endCounter);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.homex = tag.getInt("KingHomeX");
        this.homez = tag.getInt("KingHomeZ");
        this.guard_mode = tag.getInt("GuardMode");
        this.player_hit_count = tag.getInt("PlayerHits");
        this.isEnd = tag.getInt("IsEnd");
        this.endCounter = tag.getInt("EndCounter");
    }
}
