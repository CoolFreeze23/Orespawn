package danger.orespawn.entity;

import danger.orespawn.MobStats;

import java.util.Comparator;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.Mth;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.block.Blocks;
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
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.BossEvent;
import net.minecraft.network.chat.Component;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.client.RenderSpiderRobotInfo;

public class SpiderRobot extends Mob {
    private static final EntityDataAccessor<Integer> DATA_ATTACKING =
            SynchedEntityData.defineId(SpiderRobot.class, EntityDataSerializers.INT);

    private static final int LEG_COUNT = 8;
    private final RenderSpiderRobotInfo renderInfo = new RenderSpiderRobotInfo(LEG_COUNT);
    /** Lazy one-shot leg-data (re)initialization flag (orig {@code didonce}, SpiderRobot.java:53). */
    private boolean legDataInitialized = false;

    private final ServerBossEvent bossEvent = new ServerBossEvent(
            Component.literal("Spider Robot"), BossEvent.BossBarColor.WHITE, BossEvent.BossBarOverlay.PROGRESS);

    private final Comparator<Entity> targetSorter;
    private final float moveSpeed = 0.35f;
    private int soundCooldown = 0;

    public SpiderRobot(EntityType<? extends SpiderRobot> type, Level level) {
        super(type, level);
        // orig SpiderRobot.java:64 — XP = SpiderRobot_stats.health / 2 = 1500/2.
        this.xpReward = 750;
        this.targetSorter = Comparator.comparingDouble(this::distanceToSqr);
        // orig SpiderRobot.java:508-511 — entityInit primes the leg data once at construction.
        initLegData();
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new LookAtPlayerGoal(this, Player.class, 12.0f));
        this.goalSelector.addGoal(1, new RandomLookAroundGoal(this));
    }

    public static AttributeSupplier.Builder createAttributes() {
        // orig OreSpawnMain.java:6474 — SpiderRobot 1500 HP / 100 ATK / 16 armor;
        // speed 0.35 matches orig SpiderRobot.java:51.
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, MobStats.SPIDER_ROBOT.maxHealth())
                .add(Attributes.MOVEMENT_SPEED, 0.35)
                .add(Attributes.ATTACK_DAMAGE, MobStats.SPIDER_ROBOT.attackDamage())
                .add(Attributes.ARMOR, MobStats.SPIDER_ROBOT.armor());
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_ATTACKING, 0);
    }

    public int getAttacking() { return this.entityData.get(DATA_ATTACKING); }
    public void setAttacking(int value) { this.entityData.set(DATA_ATTACKING, value); }

    @Override
    public void startSeenByPlayer(ServerPlayer player) {
        super.startSeenByPlayer(player);
        this.bossEvent.addPlayer(player);
    }

    @Override
    public void stopSeenByPlayer(ServerPlayer player) {
        super.stopSeenByPlayer(player);
        this.bossEvent.removePlayer(player);
    }

    @Override
    protected void customServerAiStep() {
        this.bossEvent.setProgress(this.getHealth() / this.getMaxHealth());
        if (this.isRemoved()) return;
        if (this.getFirstPassenger() != null) return;
        super.customServerAiStep();
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (source.getMsgId().equals("inWall") || source.getMsgId().equals("cactus")
                || source.getMsgId().equals("inFire") || source.getMsgId().equals("onFire")
                || source.getMsgId().equals("magic") || source.getMsgId().equals("starve")) {
            return false;
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
        if (!this.level().isClientSide() && this.getFirstPassenger() != null && this.getRandom().nextInt(15) == 0) {
            LivingEntity target = findSomethingToAttack();
            if (target != null) {
                double meleeRange = (12.0f + target.getBbWidth() / 2.0f);
                if (this.distanceToSqr(target) < meleeRange * meleeRange) {
                    this.setAttacking(1);
                    this.doHurtTarget(target);
                }
            } else {
                this.setAttacking(0);
            }
        }
        float exhaustOffset = 8.0f;
        float exhaustX = (float) (exhaustOffset * Math.cos(Math.toRadians(this.getYRot() - 90.0f)));
        float exhaustZ = (float) (exhaustOffset * Math.sin(Math.toRadians(this.getYRot() - 90.0f)));
        if (this.level().isClientSide()) {
            if (this.getRandom().nextInt(8) == 0) {
                this.level().addParticle(ParticleTypes.FLAME, getX() + exhaustX, getY() + 2.0, getZ() + exhaustZ, 0, 0, 0);
            }
            if (this.getRandom().nextInt(2) == 0) {
                this.level().addParticle(ParticleTypes.SMOKE, getX() + exhaustX, getY() + 2.0, getZ() + exhaustZ, 0, 0, 0);
            }
            // orig SpiderRobot.java:704 — the leg solver steps once per client tick.
            updateLegs();
        }
        if (this.soundCooldown > 0) --this.soundCooldown;
        if (this.getFirstPassenger() != null && this.soundCooldown == 0 && this.getRandom().nextInt(80) == 1) {
            this.playSound(SoundEvent.createVariableRangeEvent(
                    ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "robotspider")), 0.45f, 1.0f);
            this.soundCooldown = 125;
        }
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        if (target instanceof LivingEntity livingTarget) {
            double knockbackStrength = 1.2;
            double upwardKnockback = 0.15;
            float angleToTarget = (float) Math.atan2(livingTarget.getZ() - this.getZ(), livingTarget.getX() - this.getX());
            // Attribute is authoritative (orig melee used the attack attribute).
            boolean ret = livingTarget.hurt(this.damageSources().mobAttack(this),
                    (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE));
            if (livingTarget.isRemoved() || livingTarget instanceof Player) upwardKnockback *= 2.0;
            if (ret) livingTarget.push(Math.cos(angleToTarget) * knockbackStrength, upwardKnockback, Math.sin(angleToTarget) * knockbackStrength);
            return ret;
        }
        return false;
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);
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
                    ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "robotspidermount")), 0.65f, 1.0f);
        }
        return InteractionResult.sidedSuccess(this.level().isClientSide());
    }

    @Override
    public boolean shouldRiderSit() {
        return false;
    }

    // The previous empty overrides dropped the super call, so Health, effects,
    // PersistenceRequired and equipment were never saved — robots reloaded at full HP
    // and name-tagged ones could despawn (BUG-007). The overrides are removed entirely:
    // SpiderRobot has no extra fields to persist, so the inherited behavior is correct.

    private LivingEntity findSomethingToAttack() {
        AABB searchBox = this.getBoundingBox().inflate(20.0, 12.0, 20.0);
        List<LivingEntity> entities = this.level().getEntitiesOfClass(LivingEntity.class, searchBox);
        for (LivingEntity e : entities) {
            if (isSuitableTarget(e)) return e;
        }
        return null;
    }

    private boolean isSuitableTarget(LivingEntity target) {
        if (target == null || target == this || !target.isAlive()) return false;
        if (target instanceof SpiderRobot) return false;
        if (target == this.getFirstPassenger()) return false;
        if (target instanceof Player p && p.getAbilities().instabuild) return false;
        return true;
    }

    /** Live gait-solver state consumed by the model (orig SpiderRobot.java:515-517). */
    public RenderSpiderRobotInfo getRenderSpiderRobotInfo() {
        return renderInfo;
    }

    // ==================== Procedural leg-gait solver ====================
    // Line-by-line port of the original's client-side inverse-kinematics walk:
    // each foot stays planted in the world while the body moves; when a leg's
    // reach, swing angle or elevation leaves its comfort window (or its step
    // scheduler fires), the foot is relocated to fresh ground ahead and the
    // three joint angles converge on the new pose at speed-scaled rates.

    /**
     * Per-leg constants and initial state (orig SpiderRobot.java:111-198).
     * Legs form mirrored pairs (0-1 front, 2-3 mid-front, 4-5 mid-rear,
     * 6-7 rear) with alternating swing-range signs so paired feet step out
     * of phase.
     */
    private void initLegData() {
        for (int leg = 0; leg < LEG_COUNT; ++leg) {
            renderInfo.ycurrentangle[leg] = 0.0f;
            renderInfo.ywantedangle[leg] = 0.0f;
            renderInfo.ydisplayangle[leg] = 0.0f;
            renderInfo.yvelocity[leg] = 0.0f;
            renderInfo.ymid[leg] = 0.0f;
            renderInfo.yoff[leg] = 0.0f;
            renderInfo.yrange[leg] = 0.0f;
            renderInfo.udcurrentangle[leg] = 0.0f;
            renderInfo.udwantedangle[leg] = 0.0f;
            renderInfo.uddisplayangle[leg] = 0.0f;
            renderInfo.udvelocity[leg] = 0.0f;
            // Rest pose: upper segment 45° up, middle level, lower 45° down (orig :127-129).
            renderInfo.p1xangle[leg] = 0.7853981633974483;
            renderInfo.p2xangle[leg] = 0.0;
            renderInfo.p3xangle[leg] = -0.7853981633974483;
            renderInfo.pxvelocity[leg] = 0.0f;
            renderInfo.foot_xpos[leg] = (float) this.getX();
            renderInfo.foot_ypos[leg] = (float) this.getY();
            renderInfo.foot_zpos[leg] = (float) this.getZ();
            renderInfo.realposx[leg] = 0.0f;
            renderInfo.realposy[leg] = 0.0f;
            renderInfo.realposz[leg] = 0.0f;
            renderInfo.legoff[leg] = 0.0f;
            renderInfo.footup[leg] = 1;
            renderInfo.uppoint[leg] = 0.0f;
            renderInfo.footingticker[leg] = 0;
            renderInfo.gpcounter = 0;
            switch (leg) {
                // orig :142-148
                case 0 -> { renderInfo.legoff[leg] = 1.25f; renderInfo.ymid[leg] = -0.32f;      renderInfo.yrange[leg] = 0.2617994f;  renderInfo.pairedwith[leg] = 1; renderInfo.yoff[leg] = -0.3f; }
                // orig :149-155
                case 1 -> { renderInfo.legoff[leg] = 1.25f; renderInfo.ymid[leg] = 3.4615927f;  renderInfo.yrange[leg] = -0.2617994f; renderInfo.pairedwith[leg] = 0; renderInfo.yoff[leg] = -0.3f; }
                // orig :156-162
                case 2 -> { renderInfo.legoff[leg] = 2.0f;  renderInfo.ymid[leg] = -1.0f;       renderInfo.yrange[leg] = 0.2617994f;  renderInfo.pairedwith[leg] = 3; renderInfo.yoff[leg] = -0.1f; }
                // orig :163-169
                case 3 -> { renderInfo.legoff[leg] = 2.0f;  renderInfo.ymid[leg] = 4.1415925f;  renderInfo.yrange[leg] = -0.2617994f; renderInfo.pairedwith[leg] = 2; renderInfo.yoff[leg] = -0.1f; }
                // orig :170-176
                case 4 -> { renderInfo.legoff[leg] = 1.75f; renderInfo.ymid[leg] = 0.62831855f; renderInfo.yrange[leg] = 0.2617994f;  renderInfo.pairedwith[leg] = 5; renderInfo.yoff[leg] = -0.3f; }
                // orig :177-183
                case 5 -> { renderInfo.legoff[leg] = 1.75f; renderInfo.ymid[leg] = 2.5132742f;  renderInfo.yrange[leg] = -0.2617994f; renderInfo.pairedwith[leg] = 4; renderInfo.yoff[leg] = -0.3f; }
                // orig :184-190
                case 6 -> { renderInfo.legoff[leg] = 3.4f;  renderInfo.ymid[leg] = 1.05f;       renderInfo.yrange[leg] = 0.2617994f;  renderInfo.pairedwith[leg] = 7; renderInfo.yoff[leg] = -0.1f; }
                // orig :191-196
                case 7 -> { renderInfo.legoff[leg] = 3.4f;  renderInfo.ymid[leg] = 2.0915928f;  renderInfo.yrange[leg] = -0.2617994f; renderInfo.pairedwith[leg] = 6; renderInfo.yoff[leg] = -0.1f; }
                default -> { }
            }
        }
    }

    /**
     * Speed-scaled angular-velocity controller (orig SpiderRobot.java:200-238).
     * Body speed is scaled ×8 and clamped to [1,4]; the joint accelerates by
     * 0.25°·scale per tick toward the target, snaps to fixed 0.5°/1°·scale
     * rates inside the 2°/4°·scale approach windows, caps at 4°·scale, and
     * stops dead inside the 0.5°·scale deadband. Mirrored for negative error.
     *
     * @param bodySpeed       blocks moved last tick (drives how fast joints may move)
     * @param angleError      remaining angle to the target, radians (sign = direction)
     * @param currentVelocity the joint's angular velocity from last tick
     * @return the new angular velocity, radians/tick
     */
    private float getNewVelocity(float bodySpeed, float angleError, float currentVelocity) {
        float scale = bodySpeed * 8.0f;
        if (scale < 1.0f) {
            scale = 1.0f;
        }
        if (scale > 4.0f) {
            scale = 4.0f;
        }
        if (angleError > 0.0f) {
            if ((double) angleError < Math.PI / 360 * (double) scale) {
                currentVelocity = 0.0f;
            } else {
                currentVelocity = (float) ((double) currentVelocity + 0.004363323129985824 * (double) scale);
                if ((double) angleError < 0.06981317007977318 * (double) scale) {
                    currentVelocity = (float) (Math.PI / 180 * (double) scale);
                }
                if ((double) angleError < Math.PI / 90 * (double) scale) {
                    currentVelocity = (float) (Math.PI / 360 * (double) scale);
                }
                if ((double) currentVelocity > 0.06981317007977318 * (double) scale) {
                    currentVelocity = (float) (0.06981317007977318 * (double) scale);
                }
            }
        } else if ((double) angleError > -Math.PI / 360 * (double) scale) {
            currentVelocity = 0.0f;
        } else {
            currentVelocity = (float) ((double) currentVelocity - 0.004363323129985824 * (double) scale);
            if ((double) angleError > -0.06981317007977318 * (double) scale) {
                currentVelocity = -((float) (Math.PI / 180 * (double) scale));
            }
            if ((double) angleError > -Math.PI / 90 * (double) scale) {
                currentVelocity = -((float) (Math.PI / 360 * (double) scale));
            }
            if ((double) currentVelocity < -0.06981317007977318 * (double) scale) {
                currentVelocity = -((float) (0.06981317007977318 * (double) scale));
            }
        }
        return currentVelocity;
    }

    /**
     * One solver step (orig SpiderRobot.java:240-379). Client-only, exactly
     * like the original's {@code isRemote} early-out: the gait is a visual
     * that reads the entity's already-synced position each frame.
     *
     * <p>Per leg: advance the step scheduler, recompute the hip socket's
     * world position, relocate the foot if out of range/angle/elevation
     * (294 &gt; reach×16 &gt; 32, yaw beyond range×8/7, |elevation| &gt; 1.25,
     * or scheduler fired), then converge segment fold, elevation and yaw on
     * the planted foot with {@link #getNewVelocity}. When all three axes
     * settle the foot is "down" and — a ridden robot with mobGriefing on —
     * tramples tall grass to air and grass blocks to dirt at the foot
     * (orig :370-377; client-local in the original too, since this code
     * never ran server-side).</p>
     */
    public void updateLegs() {
        if (!this.level().isClientSide()) {
            return;
        }
        // orig :244-247 — yaw normalized into [0,360) before the trig below.
        float normalizedYaw = this.getYRot() % 360.0f;
        while (normalizedYaw < 0.0f) {
            normalizedYaw += 360.0f;
        }
        this.setYRot(normalizedYaw);
        ++renderInfo.gpcounter;
        if (!this.legDataInitialized) {
            this.legDataInitialized = true;
            this.initLegData();
        }
        float deltaX = (float) (this.xo - this.getX());
        float deltaY = (float) (this.yo - this.getY());
        float deltaZ = (float) (this.zo - this.getZ());
        float bodySpeed = (float) Math.sqrt(deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ);
        for (int leg = 0; leg < LEG_COUNT; ++leg) {
            int settledAxes = 0;
            renderInfo.footingticker[leg] = renderInfo.footingticker[leg] + 1;
            // Hip socket world position (orig :263-265).
            renderInfo.realposx[leg] = (float) (this.getX() - (double) renderInfo.legoff[leg]
                    * Math.sin(Math.toRadians(Mth.wrapDegrees((double) (this.getYRot() + 90.0f))) + (double) renderInfo.ymid[leg]));
            renderInfo.realposz[leg] = (float) (this.getZ() + (double) renderInfo.legoff[leg]
                    * Math.cos(Math.toRadians(Mth.wrapDegrees((double) (this.getYRot() + 90.0f))) + (double) renderInfo.ymid[leg]));
            renderInfo.realposy[leg] = (float) this.getY() + renderInfo.yoff[leg];
            // Alternating-pair step scheduler (orig :266-269): whichever paired
            // foot has been planted longer steps once their combined age passes 50.
            int pairTickerSum = renderInfo.footingticker[leg] + renderInfo.footingticker[renderInfo.pairedwith[leg]];
            if (pairTickerSum > 50 && renderInfo.footingticker[leg] > renderInfo.footingticker[renderInfo.pairedwith[leg]]) {
                renderInfo.footingticker[leg] = 0;
            }
            deltaX = renderInfo.realposx[leg] - renderInfo.foot_xpos[leg];
            deltaY = renderInfo.realposy[leg] - renderInfo.foot_ypos[leg];
            deltaZ = renderInfo.realposz[leg] - renderInfo.foot_zpos[leg];
            float footDistance = (float) Math.sqrt(deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ);
            footDistance *= 16.0f;
            // Yaw deviation from the leg's neutral direction (orig :275-282).
            float yawDeviation = (float) (Math.abs((double) renderInfo.ycurrentangle[leg]
                    - (Math.toRadians(Mth.wrapDegrees((double) this.getYRot())) + (double) renderInfo.ymid[leg])) % (Math.PI * 2));
            if ((double) yawDeviation > Math.PI) {
                yawDeviation = (float) ((double) yawDeviation - Math.PI * 2);
            }
            if ((double) yawDeviation < -Math.PI) {
                yawDeviation = (float) ((double) yawDeviation + Math.PI * 2);
            }
            yawDeviation = Math.abs(yawDeviation);
            // Foot relocation triggers (orig :283-290).
            if (footDistance > 294.0f || footDistance < 32.0f
                    || yawDeviation > Math.abs(renderInfo.yrange[leg]) * 8.0f / 7.0f
                    || (double) Math.abs(renderInfo.udcurrentangle[leg]) > 1.25
                    || renderInfo.footingticker[leg] == 0) {
                this.findNewFooting(leg);
                deltaX = renderInfo.realposx[leg] - renderInfo.foot_xpos[leg];
                deltaY = renderInfo.realposy[leg] - renderInfo.foot_ypos[leg];
                deltaZ = renderInfo.realposz[leg] - renderInfo.foot_zpos[leg];
                footDistance = (float) Math.sqrt(deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ);
                footDistance *= 16.0f;
            }
            // Segment fold: leg reach (3 × 99px segments projected onto the
            // fold angle) converges on the foot distance (orig :291-303).
            float segment1Reach = (float) (99.0 * Math.cos(renderInfo.p2xangle[leg] - renderInfo.p1xangle[leg]));
            float segment2Reach = 99.0f;
            float segment3Reach = (float) (99.0 * Math.cos(renderInfo.p2xangle[leg] - renderInfo.p3xangle[leg]));
            float totalReach = segment1Reach + segment2Reach + segment3Reach;
            float reachError = totalReach - footDistance;
            renderInfo.pxvelocity[leg] = this.getNewVelocity(bodySpeed, (float) ((double) reachError * Math.PI / 360.0), renderInfo.pxvelocity[leg]);
            if (renderInfo.pxvelocity[leg] == 0.0f || Math.abs(reachError) < 8.0f) {
                ++settledAxes;
            }
            renderInfo.p1xangle[leg] = renderInfo.p1xangle[leg] + (double) renderInfo.pxvelocity[leg];
            renderInfo.p2xangle[leg] = 0.0;
            renderInfo.p3xangle[leg] = -renderInfo.p1xangle[leg];
            // Elevation: tilt toward the planted foot, or toward the mid-step
            // lift point while the foot travels (orig :304-334).
            float elevationTarget = renderInfo.uppoint[leg] != 0.0f
                    ? (float) Math.atan2(footDistance, (double) (renderInfo.realposy[leg] - renderInfo.uppoint[leg]) * 16.0)
                    : (float) Math.atan2(footDistance, (double) (renderInfo.realposy[leg] - renderInfo.foot_ypos[leg]) * 16.0);
            renderInfo.udwantedangle[leg] = (float) ((double) elevationTarget - 1.5707963267948966);
            while ((double) renderInfo.udwantedangle[leg] > Math.PI) {
                renderInfo.udwantedangle[leg] = (float) ((double) renderInfo.udwantedangle[leg] - Math.PI * 2);
            }
            while ((double) renderInfo.udwantedangle[leg] < -Math.PI) {
                renderInfo.udwantedangle[leg] = (float) ((double) renderInfo.udwantedangle[leg] + Math.PI * 2);
            }
            double targetHeading = renderInfo.udwantedangle[leg];
            double currentHeading = renderInfo.udcurrentangle[leg];
            double headingError = (targetHeading - currentHeading) % (Math.PI * 2);
            while (headingError > Math.PI) {
                headingError -= Math.PI * 2;
            }
            while (headingError < -Math.PI) {
                headingError += Math.PI * 2;
            }
            renderInfo.udvelocity[leg] = this.getNewVelocity(bodySpeed * 2.0f, (float) headingError, renderInfo.udvelocity[leg]);
            if (renderInfo.udvelocity[leg] == 0.0f || Math.abs(headingError) < Math.PI / 90) {
                renderInfo.uppoint[leg] = 0.0f;
                ++settledAxes;
            }
            currentHeading += (double) renderInfo.udvelocity[leg];
            while (currentHeading > Math.PI) {
                currentHeading -= Math.PI * 2;
            }
            while (currentHeading < -Math.PI) {
                currentHeading += Math.PI * 2;
            }
            renderInfo.uddisplayangle[leg] = renderInfo.udcurrentangle[leg] = (float) currentHeading;
            // Yaw: swing toward the planted foot's world bearing (orig :335-369).
            deltaZ = renderInfo.realposz[leg] - renderInfo.foot_zpos[leg];
            deltaX = renderInfo.realposx[leg] - renderInfo.foot_xpos[leg];
            renderInfo.ywantedangle[leg] = (float) Math.atan2(deltaZ, deltaX);
            targetHeading = renderInfo.ywantedangle[leg];
            currentHeading = renderInfo.ycurrentangle[leg];
            headingError = (targetHeading - currentHeading) % (Math.PI * 2);
            if (headingError > Math.PI) {
                headingError -= Math.PI * 2;
            }
            if (headingError < -Math.PI) {
                headingError += Math.PI * 2;
            }
            renderInfo.yvelocity[leg] = this.getNewVelocity(bodySpeed, (float) headingError, renderInfo.yvelocity[leg]);
            if (renderInfo.yvelocity[leg] == 0.0f || Math.abs(headingError) < Math.PI / 90) {
                ++settledAxes;
            }
            renderInfo.ycurrentangle[leg] = renderInfo.ycurrentangle[leg] + renderInfo.yvelocity[leg];
            while ((double) renderInfo.ycurrentangle[leg] > Math.PI) {
                renderInfo.ycurrentangle[leg] = (float) ((double) renderInfo.ycurrentangle[leg] - Math.PI * 2);
            }
            while ((double) renderInfo.ycurrentangle[leg] < -Math.PI) {
                renderInfo.ycurrentangle[leg] = (float) ((double) renderInfo.ycurrentangle[leg] + Math.PI * 2);
            }
            float displayYaw = (float) ((double) renderInfo.ycurrentangle[leg]
                    - Math.toRadians(Mth.wrapDegrees((double) this.getYRot())) - 1.5707963267948966);
            while ((double) displayYaw > Math.PI) {
                displayYaw = (float) ((double) displayYaw - Math.PI * 2);
            }
            while ((double) displayYaw < -Math.PI) {
                displayYaw = (float) ((double) displayYaw + Math.PI * 2);
            }
            renderInfo.ydisplayangle[leg] = displayYaw;
            if (settledAxes != 3) continue;
            // All three axes settled → foot lands (orig :370-377). The grass
            // trample below ran in this client-only path in 1.7.10 as well,
            // so the change is client-local there and here alike.
            renderInfo.footup[leg] = 0;
            // orig :372 int-truncates the foot coordinates (differs from flooring at negatives).
            BlockPos footPos = new BlockPos((int) renderInfo.foot_xpos[leg], (int) renderInfo.foot_ypos[leg], (int) renderInfo.foot_zpos[leg]);
            boolean griefing = this.level().getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING);
            if (this.level().getBlockState(footPos).is(Blocks.SHORT_GRASS) && this.getFirstPassenger() != null && griefing) {
                this.level().setBlock(footPos, Blocks.AIR.defaultBlockState(), 3);
            }
            BlockPos belowFoot = footPos.below();
            if (this.level().getBlockState(belowFoot).is(Blocks.GRASS_BLOCK) && this.getFirstPassenger() != null && griefing) {
                this.level().setBlock(belowFoot, Blocks.DIRT.defaultBlockState(), 3);
            }
        }
    }

    /**
     * Plants the foot of leg {@code leg} on fresh ground (orig
     * SpiderRobot.java:381-486). Aims a probe ahead of the hip along the
     * leg's neutral direction, biased by the swing range (flipped when the
     * body moves against its facing, zeroed while turning); sweeps the reach
     * from 16 (10 for rear legs) down to 3.5 blocks, scanning an 11-up/14-down
     * column with a ±1 spread at each stop for solid ground; if nothing is
     * found the sweep repeats un-biased with a ±3 spread; the final fallback
     * hangs the foot half-reach ahead at hip height minus one. A relocated
     * airborne foot gets a mid-step lift point above the travel midpoint that
     * grows with step length (+1 above 3px, +1.5 above 48px, +1.5 more
     * above 100px).
     */
    private void findNewFooting(int leg) {
        float reach = 16.0f;
        boolean found = false;
        double headingRad = Math.toRadians((this.getYRot() + 90.0f) % 360.0f);
        // orig :391 — the original's slightly-off hand-typed PI, kept for exactness.
        double pi = 3.1415926545;
        renderInfo.footingticker[leg] = 0;
        float deltaX = (float) (this.getX() - this.xo);
        float deltaZ = (float) (this.getZ() - this.zo);
        double travelHeading = Math.atan2(deltaZ, deltaX);
        double travelSpeed = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);
        double headingDiff = Math.abs(travelHeading - headingRad) % (pi * 2.0);
        if (headingDiff > pi) {
            headingDiff -= pi * 2.0;
        }
        headingDiff = Math.abs(headingDiff);
        if (Math.abs(travelSpeed) < 0.01) {
            headingDiff = 0.0;
        }
        float swingBias = renderInfo.yrange[leg];
        swingBias *= 0.875f;
        if (Math.abs((this.yRotO - this.getYRot()) % 360.0f) > 0.75f) {
            swingBias = 0.0f;
        }
        if (leg >= 4) {
            reach = 10.0f;
        }
        // Moving backward relative to facing: step behind instead (orig :413-419).
        if (headingDiff > 1.5) {
            swingBias = -swingBias;
            reach = 10.0f;
            if (leg >= 4) {
                reach = 16.0f;
            }
        }
        float fallbackX = (float) ((double) renderInfo.realposx[leg] - (double) (reach / 2.0f)
                * Math.sin(Math.toRadians(Mth.wrapDegrees((double) (this.getYRot() + 90.0f))) + (double) renderInfo.ymid[leg]));
        float fallbackZ = (float) ((double) renderInfo.realposz[leg] + (double) (reach / 2.0f)
                * Math.cos(Math.toRadians(Mth.wrapDegrees((double) (this.getYRot() + 90.0f))) + (double) renderInfo.ymid[leg]));
        float fallbackY = renderInfo.realposy[leg] - 1.0f;
        float footX = fallbackX;
        float footY = fallbackY;
        float footZ = fallbackZ;
        float initialReach = reach;
        int spread = 1;
        while (!found && reach > 3.5f) {
            footX = (float) ((double) renderInfo.realposx[leg] - (double) reach
                    * Math.sin(Math.toRadians(Mth.wrapDegrees((double) (this.getYRot() + 90.0f))) + (double) renderInfo.ymid[leg] - (double) swingBias));
            footZ = (float) ((double) renderInfo.realposz[leg] + (double) reach
                    * Math.cos(Math.toRadians(Mth.wrapDegrees((double) (this.getYRot() + 90.0f))) + (double) renderInfo.ymid[leg] - (double) swingBias));
            footY = renderInfo.realposy[leg];
            for (int yScan = 11; !found && yScan > -14; --yScan) {
                for (int xSpread = -spread; !found && xSpread <= spread; ++xSpread) {
                    for (int zSpread = -spread; !found && zSpread <= spread; ++zSpread) {
                        BlockPos probe = BlockPos.containing((int) footX + xSpread, (int) footY + yScan, (int) footZ + zSpread);
                        // orig :432-433 — non-air with a solid material.
                        if (this.level().getBlockState(probe).isAir() || !this.level().getBlockState(probe).isSolid()) continue;
                        footY += (float) (yScan + 1);
                        footX += (float) xSpread;
                        footZ += (float) zSpread;
                        found = true;
                        break;
                    }
                }
            }
            if (found) {
                deltaX = renderInfo.realposx[leg] - footX;
                float deltaY = renderInfo.realposy[leg] - footY;
                deltaZ = renderInfo.realposz[leg] - footZ;
                float footingDistance = (float) Math.sqrt(deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ);
                if ((footingDistance *= 16.0f) > 294.0f) {
                    found = false;
                }
            }
            // Second pass: bias removed, spread widened to ±3 (orig :451-455).
            if (!((reach -= 1.0f) < 3.5f) || swingBias == 0.0f) continue;
            swingBias = 0.0f;
            spread = 3;
            reach = initialReach;
        }
        if (!found) {
            footX = fallbackX;
            footY = fallbackY;
            footZ = fallbackZ;
        }
        float previousFootX = renderInfo.foot_xpos[leg];
        float previousFootY = renderInfo.foot_ypos[leg];
        float previousFootZ = renderInfo.foot_zpos[leg];
        renderInfo.foot_xpos[leg] = footX;
        renderInfo.foot_ypos[leg] = footY;
        renderInfo.foot_zpos[leg] = footZ;
        // Mid-step lift point for a foot that was down (orig :467-485).
        if (renderInfo.footup[leg] == 0) {
            renderInfo.footup[leg] = 1;
            deltaX = previousFootX - footX;
            float deltaY = previousFootY - footY;
            deltaZ = previousFootZ - footZ;
            float stepLength = (float) Math.sqrt(deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ);
            stepLength *= 16.0f;
            float liftPoint = (previousFootY + footY) / 2.0f;
            if (stepLength > 3.0f) {
                liftPoint += 1.0f;
            }
            if (stepLength > 48.0f) {
                liftPoint += 1.5f;
            }
            if (stepLength > 100.0f) {
                liftPoint += 1.5f;
            }
            renderInfo.uppoint[leg] = liftPoint;
        }
    }
}
