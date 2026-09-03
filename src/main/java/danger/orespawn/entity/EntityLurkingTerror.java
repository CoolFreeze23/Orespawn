package danger.orespawn.entity;

import danger.orespawn.MobStats;

import danger.orespawn.OreSpawnConfig;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.ai.GenericTargetSorter;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.util.Mth;
import danger.orespawn.entity.ai.TargetSelection;

public class EntityLurkingTerror extends Monster {
    // OPT-011: cached SoundEvents — identical createVariableRangeEvent ids,
    // allocated once per class instead of on every sound query.
    private static final SoundEvent SND_LURKINGHORROR_LIVING = SoundEvent.createVariableRangeEvent(
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "lurkinghorror_living"));
    private static final SoundEvent SND_LURKINGHORROR_HIT = SoundEvent.createVariableRangeEvent(
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "lurkinghorror_hit"));
    private static final SoundEvent SND_LURKINGHORROR_DEAD = SoundEvent.createVariableRangeEvent(
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "lurkinghorror_dead"));
    private static final EntityDataAccessor<Integer> DATA_ATTACKING =
            SynchedEntityData.defineId(EntityLurkingTerror.class, EntityDataSerializers.INT);

    private BlockPos currentFlightTarget = null;
    private int lastX = 0;
    private int lastY = 0;
    private int lastZ = 0;
    private int stuckCount = 0;

    /**
     * Per-entity render scratch (orig LurkingTerror.java:49 {@code renderdata = new RenderInfo()},
     * zeroed in entityInit orig LurkingTerror.java:71-81, accessor orig LurkingTerror.java:99-101).
     * Mutated client-side by {@code LurkingTerrorModel} for the leg-selector latch (ri1/rf1) and
     * the mouth latch (ri2/rf2) (orig ModelLurkingTerror.java:443-478); never datawatcher-synced.
     * The original's setRenderInfo (orig LurkingTerror.java:103-112) copied the instance onto itself
     * (ModelLurkingTerror.java:562 passed back the object from getRenderInfo), so it is omitted as in Kraken.
     * ENT-S-093.
     */
    private final danger.orespawn.entity.client.RenderInfo renderInfo =
            new danger.orespawn.entity.client.RenderInfo();

    public EntityLurkingTerror(EntityType<? extends EntityLurkingTerror> type, Level level) {
        super(type, level);
        this.xpReward = 20;
    }

    public static AttributeSupplier.Builder createAttributes() {
        // orig OreSpawnMain.java:6503 — LurkingTerror 30 HP / 6 ATK / 5 armor
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, MobStats.LURKING_TERROR.maxHealth())
                .add(Attributes.MOVEMENT_SPEED, 0.25)
                .add(Attributes.ATTACK_DAMAGE, MobStats.LURKING_TERROR.attackDamage())
                .add(Attributes.ARMOR, MobStats.LURKING_TERROR.armor());
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_ATTACKING, 0);
    }

    public int getAttacking() {
        return this.entityData.get(DATA_ATTACKING);
    }

    public void setAttacking(int value) {
        this.entityData.set(DATA_ATTACKING, value);
    }

    /** Mirrors orig LurkingTerror.java:99-101 {@code getRenderInfo()}. ENT-S-093. */
    public danger.orespawn.entity.client.RenderInfo getRenderInfo() {
        return this.renderInfo;
    }

    @Override
    protected float getSoundVolume() {
        return 0.55f;
    }

    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        return SND_LURKINGHORROR_LIVING;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SND_LURKINGHORROR_HIT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SND_LURKINGHORROR_DEAD;
    }

    @Override
    public void tick() {
        super.tick();
        Vec3 motion = this.getDeltaMovement();
        this.setDeltaMovement(motion.x, motion.y * 0.6, motion.z);
    }

    @Override
    protected void customServerAiStep() {
        if (this.isRemoved()) return;
        super.customServerAiStep();

        if (this.lastX == (int) this.getX() && this.lastY == (int) this.getY() && this.lastZ == (int) this.getZ()) {
            ++this.stuckCount;
        } else {
            this.stuckCount = 0;
            this.lastX = (int) this.getX();
            this.lastY = (int) this.getY();
            this.lastZ = (int) this.getZ();
        }

        if (this.currentFlightTarget == null) {
            this.currentFlightTarget = this.blockPosition();
        }

        double distSq = this.currentFlightTarget.distSqr(this.blockPosition());

        if (this.stuckCount > 30 || this.random.nextInt(120) == 0 || distSq < 4.0) {
            this.stuckCount = 0;
            int keepTrying = 50;
            while (keepTrying > 0) {
                int xdir = (this.random.nextInt(10) + 2) * (this.random.nextInt(2) == 0 ? -1 : 1);
                int zdir = (this.random.nextInt(10) + 2) * (this.random.nextInt(2) == 0 ? -1 : 1);

                BlockPos newTarget = new BlockPos(
                        (int) this.getX() + xdir,
                        (int) this.getY() + this.random.nextInt(5) - 2,
                        (int) this.getZ() + zdir);

                if (this.level().getBlockState(newTarget).isAir()) {
                    this.currentFlightTarget = newTarget;
                    break;
                }
                --keepTrying;
            }
        } else if (this.random.nextInt(9) == 0) {
            LivingEntity targetEntity = findSomethingToAttack();
            if (targetEntity != null) {
                this.setAttacking(1);
                this.currentFlightTarget = targetEntity.blockPosition().above(1);
                if (this.distanceToSqr(targetEntity) < 6.0) {
                    this.doHurtTarget(targetEntity);
                }
            } else {
                this.setAttacking(0);
            }
        }

        double dx = this.currentFlightTarget.getX() + 0.4 - this.getX();
        double dy = this.currentFlightTarget.getY() + 0.1 - this.getY();
        double dz = this.currentFlightTarget.getZ() + 0.4 - this.getZ();

        Vec3 motion = this.getDeltaMovement();
        double newMx = motion.x + (Math.signum(dx) * 0.4 - motion.x) * 0.3;
        double newMy = motion.y + (Math.signum(dy) * 0.7 - motion.y) * 0.2;
        double newMz = motion.z + (Math.signum(dz) * 0.4 - motion.z) * 0.3;
        this.setDeltaMovement(newMx, newMy, newMz);

        float targetYaw = (float) (Math.atan2(newMz, newMx) * 180.0 / Math.PI) - 90.0f;
        float yawDiff = Mth.wrapDegrees(targetYaw - this.getYRot());
        this.setYRot(this.getYRot() + yawDiff / 4.0f);
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
        Entity attacker = source.getEntity();
        if (attacker instanceof EntityLurkingTerror) return false;
        boolean ret = super.hurt(source, amount);
        if (attacker != null && this.currentFlightTarget != null) {
            this.currentFlightTarget = attacker.blockPosition();
        }
        return ret;
    }

    @Nullable
    private LivingEntity findSomethingToAttack() {
        if (OreSpawnConfig.PLAY_NICELY.get()) return null; // orig LurkingTerror.java:350-353
        List<LivingEntity> entities = this.level().getEntitiesOfClass(LivingEntity.class,
                this.getBoundingBox().inflate(12.0, 8.0, 12.0));
        // TF-035: orig sorts candidates with GenericTargetSorter (LurkingTerror.java:48 field,
        // :58 ctor, :355 Collections.sort), not plain distance — creepers/large targets rank closer.
        // OPT-021: nearest-first pick without the full list sort; TargetSelection
        // preserves the removed sort's order and stable-tie semantics exactly.
        return TargetSelection.firstMatch(entities, new GenericTargetSorter(this), this::isSuitableTarget);
    }

    /**
     * orig LurkingTerror.java:271-348 — attacks EVERYTHING alive and visible except
     * its own kind, a fixed list of OreSpawn species (mostly the other custom-AI
     * flyers/plants it would dogfight forever), and creative players. The original
     * checks Triffid twice (:317 and :338, a copy-paste slip) — behavior-identical,
     * reproduced as one check.
     */
    private boolean isSuitableTarget(LivingEntity target) {
        if (target == null || target == this || !target.isAlive()) return false;
        if (!this.getSensing().hasLineOfSight(target)) return false; // orig :281
        if (target instanceof EntityLurkingTerror) return false;     // orig :284
        if (target instanceof RockBase) return false;                // orig :287
        if (target instanceof EnderReaper) return false;             // orig :290
        if (target instanceof EntityLeafMonster) return false;       // orig :293
        if (target instanceof EntityTerribleTerror) return false;    // orig :296
        if (target instanceof Mothra) return false;                  // orig :299
        if (target instanceof CloudShark) return false;              // orig :302
        if (target instanceof EntityRotator) return false;           // orig :305
        if (target instanceof EntityBee) return false;               // orig :308
        if (target instanceof EntityMantis) return false;            // orig :311
        if (target instanceof CreepingHorror) return false;          // orig :314
        if (target instanceof EntityTriffid) return false;           // orig :317 (+dupe :338)
        if (target instanceof PitchBlack) return false;              // orig :320
        if (target instanceof Dragon) return false;                  // orig :323
        if (target instanceof Island) return false;                  // orig :326
        if (target instanceof IslandToo) return false;               // orig :329
        if (target instanceof EntityButterfly) return false;         // orig :332
        if (target instanceof Firefly) return false;                 // orig :335
        if (target instanceof Player p) return !p.getAbilities().invulnerable; // orig :341-346
        return true;
    }

    /** orig LurkingTerror.java:237-269 — spawner bypass; darkness; DAYTIME required; 1-in-2 dice; extra 1-in-6 dice in Chaos; no other within 32/16/32; y>=10. */
    @Override
    public boolean checkSpawnRules(net.minecraft.world.level.LevelAccessor level,
                                   net.minecraft.world.entity.MobSpawnType spawnType) {
        if (OriginalSpawnGates.nearOwnSpawner(this, level, -2, 1, 0, 4)) return true;
        if (!OriginalSpawnGates.isDarkEnough(this, level)) return false;
        if (!OriginalSpawnGates.isDaytime(level)) return false;
        if (this.getRandom().nextInt(2) != 1) return false;
        if (danger.orespawn.ModDimensionKeys.isIn(level, danger.orespawn.ModDimensionKeys.CHAOS)
                && this.getRandom().nextInt(6) != 0) return false;
        if (OriginalSpawnGates.anyOtherNearby(this, level, EntityLurkingTerror.class, 32.0, 16.0, 32.0)) return false;
        return this.getY() >= 10.0;
    }
}
