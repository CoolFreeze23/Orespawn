package danger.orespawn.entity;

import danger.orespawn.MobStats;

import java.util.Comparator;
import java.util.List;
import danger.orespawn.ModEntities;
import danger.orespawn.ModBlocks;
import danger.orespawn.MobzillaSpawnTracker;
import danger.orespawn.OreSpawnConfig;
import danger.orespawn.util.MyUtils;
import danger.orespawn.OreSpawnMod;
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Skeleton;
import net.minecraft.world.entity.monster.Spider;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.BossEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.neoforged.neoforge.entity.PartEntity;

public class Godzilla extends Monster implements OreSpawnPartEntity.MultipartBoss {
    private static final int CONFIGURED_MAX_HEALTH = 6000;
    private static final double MELEE_PUSH_HORIZONTAL = 3.2;
    private static final double MELEE_PUSH_VERTICAL = 0.3;
    private static final float INCOMING_DAMAGE_CAP = 750.0f;
    private static final int LARGE_ENTITY_AREA_THRESHOLD = 30;

    /** BOSS-017: empty part array served while PlayNicely-shrunk. */
    private static final PartEntity<?>[] NO_PARTS = new PartEntity<?>[0];
    /** BOSS-017: constructor-time PlayNicely snapshot (orig Godzilla.java:71-75). */
    private boolean playNicelyShrunk = false;
    /**
     * BOSS-017: orig Godzilla.java:101/:271 — DataWatcher slot 21 mirrors
     * the live PlayNicely flag every tick so the client renderer can apply
     * the /4 visual shrink dynamically (orig RenderGodzilla.java:39-45).
     */
    private static final EntityDataAccessor<Integer> DATA_PLAY_NICELY =
            SynchedEntityData.defineId(Godzilla.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_ATTACKING =
            SynchedEntityData.defineId(Godzilla.class, EntityDataSerializers.INT);

    private final ServerBossEvent bossEvent = new ServerBossEvent(
            Component.literal("Mobzilla"), BossEvent.BossBarColor.PURPLE, BossEvent.BossBarOverlay.PROGRESS);

    private final Comparator<Entity> targetSorter;
    private final float moveSpeed = 0.75f;
    private int hurtTimer = 0;
    private int jumped = 0;
    private int jumpTimer = 0;
    private int ticker = 0;
    private int streamCount = 8;
    private int largeUnknownDetected = 0;
    private int headFound = 0;

    private final OreSpawnPartEntity<Godzilla> bodyLower;
    private final OreSpawnPartEntity<Godzilla> bodyUpper;
    private final OreSpawnPartEntity<Godzilla> headPart;
    private final OreSpawnPartEntity<Godzilla> tail;
    private final PartEntity<?>[] allParts;

    public Godzilla(EntityType<? extends Godzilla> type, Level level) {
        super(type, level);
        this.xpReward = 10000;
        // TF-035: orig Godzilla.java:57/:85 — GenericTargetSorter.
        this.targetSorter = new danger.orespawn.entity.ai.GenericTargetSorter(this);
        // BOSS-017: orig Godzilla.java:71-75 — constructor-time PlayNicely
        // snapshot: 2.475x6.25 instead of 9.9x25.
        this.playNicelyShrunk = danger.orespawn.OreSpawnConfig.PLAY_NICELY.get();
        this.refreshDimensions();

        this.bodyLower = new OreSpawnPartEntity<>(this, "bodyLow",  8.0f, 8.0f);
        this.bodyUpper = new OreSpawnPartEntity<>(this, "bodyUp",   6.0f, 6.0f);
        this.headPart  = new OreSpawnPartEntity<>(this, "head",     5.0f, 5.0f);
        this.tail      = new OreSpawnPartEntity<>(this, "tail",     4.0f, 4.0f);
        this.allParts = new PartEntity<?>[]{ bodyLower, bodyUpper, headPart, tail };
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(2, new WaterAvoidingRandomStrollGoal(this, 1.0));
        this.goalSelector.addGoal(3, new LookAtPlayerGoal(this, LivingEntity.class, 50.0f));
        this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
    }

    public static AttributeSupplier.Builder createAttributes() {
        // orig OreSpawnMain.java:6514 â€” "Mobzilla" 4000 HP / 175 ATK / 21 armor;
        // speed 0.75 matches orig Godzilla.java:107. Orig armor boosts to 25 while
        // a "large unknown" is detected (orig Godzilla.java:145-150) â€” base is 21.
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, MobStats.GODZILLA.maxHealth())
                .add(Attributes.MOVEMENT_SPEED, 0.75)
                .add(Attributes.ATTACK_DAMAGE, MobStats.GODZILLA.attackDamage())
                .add(Attributes.ARMOR, MobStats.GODZILLA.armor())
                .add(Attributes.FOLLOW_RANGE, 64.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0)
                .add(Attributes.STEP_HEIGHT, 12.0);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_PLAY_NICELY, 0);
        builder.define(DATA_ATTACKING, 0);
    }

    @Override
    public boolean removeWhenFarAway(double dist) {
        // BOSS-017: orig Godzilla.java:134-138 func_70692_ba — a "nice"
        // Godzilla is allowed to despawn; a hostile one never does.
        return danger.orespawn.OreSpawnConfig.PLAY_NICELY.get();
    }

    /**
     * 1.7.10 fidelity: ambient Mobzilla spawning required clear sky, night,
     * y >= 50, a roughly 16Ã—10Ã—16 air pocket, a 1/40 dice roll, and the
     * global {@code OreSpawnMain.godzilla_has_spawned} flag being unset.
     * Player-summoned Mobzillas (via the 9 Ancient Dried Egg Parts) bypass
     * this entire chain because the spawn item calls {@code spawn()} with
     * {@link MobSpawnType#SPAWN_EGG}, not {@link MobSpawnType#NATURAL}.
     */
    @Override
    public boolean checkSpawnRules(LevelAccessor level, MobSpawnType spawnType) {
        if (spawnType != MobSpawnType.NATURAL && spawnType != MobSpawnType.CHUNK_GENERATION
                && spawnType != MobSpawnType.SPAWNER) {
            return super.checkSpawnRules(level, spawnType);
        }
        if (this.getY() < 50.0) return false;
        if (!level.canSeeSky(this.blockPosition())) return false;

        ServerLevel serverLevel = (level instanceof ServerLevel sl) ? sl : null;
        if (serverLevel != null && !serverLevel.dimensionType().hasFixedTime() && serverLevel.isDay()) {
            return false;
        }
        if (this.getRandom().nextInt(40) != 1) return false;

        BlockPos minPos = BlockPos.containing(this.getX() - 8.0, this.getY(), this.getZ() - 8.0);
        BlockPos maxPos = BlockPos.containing(this.getX() + 8.0, this.getY() + 10.0, this.getZ() + 8.0);
        for (BlockPos pos : BlockPos.betweenClosed(minPos, maxPos)) {
            if (!level.getBlockState(pos).isAir()) return false;
        }

        AABB siblingBox = this.getBoundingBox().inflate(64.0, 16.0, 64.0);
        if (!level.getEntitiesOfClass(Godzilla.class, siblingBox, g -> g != this).isEmpty()) {
            return false;
        }

        if (OreSpawnConfig.MOBZILLA_SINGLE_SPAWN.get() && serverLevel != null) {
            if (MobzillaSpawnTracker.get(serverLevel).hasSpawned()) return false;
        }
        return true;
    }

    @Override
    public boolean isMultipartEntity() {
        return true;
    }

    @Override
    public PartEntity<?>[] getParts() {
        // BOSS-017: a PlayNicely-shrunk Godzilla is the orig's single
        // 2.475x6.25 box — no part surfaces, parent pickable instead.
        if (this.playNicelyShrunk) {
            return NO_PARTS;
        }
        return this.allParts;
    }

    @Override
    public void setId(int id) {
        super.setId(id);
        for (int i = 0; i < allParts.length; i++) {
            allParts[i].setId(id + i + 1);
        }
    }

    @Override
    public boolean isPickable() {
        // BOSS-017: shrunk (nice) Godzilla is directly hittable like the
        // orig's single small box.
        return this.playNicelyShrunk;
    }

    /**
     * BOSS-017: orig Godzilla.java:71-75 — 9.9x25 normally (port registers
     * 10x25, documented at ModEntities), 2.475x6.25 when PlayNicely was set
     * at construction time.
     */
    @Override
    public net.minecraft.world.entity.EntityDimensions getDefaultDimensions(net.minecraft.world.entity.Pose pose) {
        return this.playNicelyShrunk
                ? net.minecraft.world.entity.EntityDimensions.fixed(2.475f, 6.25f)
                : net.minecraft.world.entity.EntityDimensions.fixed(10.0f, 25.0f);
    }

    @Override
    public boolean hurtFromPart(OreSpawnPartEntity<?> part, DamageSource source, float amount) {
        String partName = part.getPartName();
        float multiplied = switch (partName) {
            case "head" -> amount;
            case "bodyLow", "bodyUp" -> amount * 0.5f;
            default -> amount * 0.25f + 1.0f;
        };
        return this.hurt(source, multiplied);
    }

    private void positionPart(OreSpawnPartEntity<Godzilla> part, double offsetX, double offsetY, double offsetZ) {
        float yawRad = this.yBodyRot * Mth.DEG_TO_RAD;
        double sin = Mth.sin(yawRad);
        double cos = Mth.cos(yawRad);
        double rx = offsetX * cos - offsetZ * sin;
        double rz = offsetX * sin + offsetZ * cos;
        part.setPos(this.getX() + rx, this.getY() + offsetY, this.getZ() + rz);
    }

    @Override
    public void tick() {
        Vec3[] oldPos = new Vec3[allParts.length];
        for (int i = 0; i < allParts.length; i++) {
            oldPos[i] = new Vec3(allParts[i].getX(), allParts[i].getY(), allParts[i].getZ());
        }

        this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(this.moveSpeed);
        super.tick();
        positionPart(bodyLower,  0.0,  2.0,   0.0);
        positionPart(bodyUpper,  0.0, 12.0,   0.0);
        positionPart(headPart,   0.0, 20.0,  -6.0);
        positionPart(tail,       0.0,  4.0,  10.0);

        for (int i = 0; i < allParts.length; i++) {
            allParts[i].xo = oldPos[i].x;
            allParts[i].yo = oldPos[i].y;
            allParts[i].zo = oldPos[i].z;
            allParts[i].xOld = oldPos[i].x;
            allParts[i].yOld = oldPos[i].y;
            allParts[i].zOld = oldPos[i].z;
        }

        if (this.onGround()) {
            this.getNavigation().stop();
        }
    }

    public int mygetMaxHealth() {
        return CONFIGURED_MAX_HEALTH;
    }

    @Override
    public boolean causeFallDamage(float fallDistance, float multiplier, DamageSource source) {
        return false;
    }

    @Override
    public void checkFallDamage(double y, boolean onGround, BlockState state, BlockPos pos) {
    }

    @Override
    public void thunderHit(ServerLevel level, LightningBolt bolt) {
    }

    @Override
    protected SoundEvent getAmbientSound() {
        // orig Godzilla.java:176-181 — "orespawn:godzilla_living" 1 time in 5.
        if (this.getRandom().nextInt(5) == 0) {
            return SoundEvent.createVariableRangeEvent(
                    ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "godzilla_living"));
        }
        return null;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        // orig Godzilla.java:183-185 — "orespawn:alo_hurt".
        return SoundEvent.createVariableRangeEvent(
                ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "alo_hurt"));
    }

    @Override
    protected SoundEvent getDeathSound() {
        // orig Godzilla.java:187-189 — "orespawn:godzilla_death".
        return SoundEvent.createVariableRangeEvent(
                ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "godzilla_death"));
    }

    @Override
    protected float getSoundVolume() {
        return 1.65f;
    }

    @Override
    public float getVoicePitch() {
        return 1.1f;
    }

    // ---- Synched data accessors ----

    /** BOSS-017: client-side accessor for the /4 render shrink (orig watcher 21). */
    public final int getPlayNicely() {
        return this.entityData.get(DATA_PLAY_NICELY);
    }

    public final int getAttacking() {
        return this.entityData.get(DATA_ATTACKING);
    }

    public final void setAttacking(int value) {
        this.entityData.set(DATA_ATTACKING, value);
    }

    // ---- Jump mechanics ----

    @Override
    public void jumpFromGround() {
        while (this.getYRot() < 0.0f) this.setYRot(this.getYRot() + 360.0f);
        while (this.yHeadRot < 0.0f) this.yHeadRot += 360.0f;
        while (this.getYRot() > 360.0f) this.setYRot(this.getYRot() - 360.0f);
        while (this.yHeadRot > 360.0f) this.yHeadRot -= 360.0f;

        Vec3 motion = this.getDeltaMovement();
        float forwardBoost = 0.2f + Math.abs(this.getRandom().nextFloat() * 0.45f);
        double mx = motion.x + forwardBoost * Math.cos(Math.toRadians(this.yHeadRot + 90.0f));
        double mz = motion.z + forwardBoost * Math.sin(Math.toRadians(this.yHeadRot + 90.0f));
        this.setDeltaMovement(mx, motion.y + 0.45, mz);
        this.getNavigation().stop();
    }

    private void jumpAtEntity(LivingEntity targetEntity) {
        Vec3 motion = this.getDeltaMovement();
        double deltaX = targetEntity.getX() - this.getX();
        double deltaZ = targetEntity.getZ() - this.getZ();
        float angle = (float) Math.atan2(deltaZ, deltaX);
        double horizontalDist = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);
        this.setDeltaMovement(
                motion.x + horizontalDist * 0.05 * Math.cos(angle),
                motion.y + 1.25,
                motion.z + horizontalDist * 0.05 * Math.sin(angle)
        );
        this.getNavigation().stop();
    }

    // ---- Distance helpers ----

    private double getHorizontalDistanceSqToEntity(Entity entity) {
        double dx = entity.getX() - this.getX();
        double dz = entity.getZ() - this.getZ();
        return dx * dx + dz * dz;
    }

    private double myGetDistanceSqToEntity(Entity entity) {
        double deltaX = this.getX() - entity.getX();
        double deltaY = entity.getY() - this.getY();
        double deltaZ = this.getZ() - entity.getZ();
        if (deltaY > 0.0 && deltaY < 20.0) deltaY = 0.0;
        if (deltaY > 20.0) deltaY -= 10.0;
        return deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ;
    }

    // ---- Block crushing ----

    private boolean isCrushable(Block block) {
        if (block == null) return false;
        if (!this.level().getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING)) return false;
        if (block == Blocks.GRASS_BLOCK) return false;
        if (block == Blocks.DIRT) return false;
        if (block == Blocks.STONE) return false;
        if (block == Blocks.MYCELIUM) return false;
        if (block == Blocks.LAVA) return false;
        if (block == Blocks.WATER) return false;
        if (block == Blocks.BEDROCK) return false;
        if (block == Blocks.OBSIDIAN) return false;
        if (block == Blocks.SAND) return false;
        if (block == Blocks.GRAVEL) return false;
        if (block == Blocks.IRON_BLOCK) return false;
        if (block == Blocks.DIAMOND_BLOCK) return false;
        if (block == Blocks.EMERALD_BLOCK) return false;
        if (block == Blocks.GOLD_BLOCK) return false;
        if (block == Blocks.ENDER_CHEST) return false;
        if (block == Blocks.COMMAND_BLOCK) return false;
        if (block == ModBlocks.BLOCK_AMETHYST.get()) return false;
        if (block == ModBlocks.BLOCK_RUBY.get()) return false;
        if (block == ModBlocks.BLOCK_URANIUM.get()) return false;
        if (block == ModBlocks.BLOCK_TITANIUM.get()) return false;
        if (block == ModBlocks.CRYSTAL_STONE.get()) return false;
        if (block == ModBlocks.CRYSTAL_GRASS.get()) return false;
        return true;
    }

    private void crushBlocks(double cx, double cz, int xzrange, int k) {
        if (!this.level().getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING)) return;
        for (int i = -xzrange; i <= xzrange; i++) {
            for (int j = -xzrange; j <= xzrange; j++) {
                BlockPos pos = BlockPos.containing(cx + i, this.getY() + k, cz + j);
                Block block = this.level().getBlockState(pos).getBlock();
                if (isCrushable(block)) {
                    this.level().setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
                } else if (block == Blocks.GRASS_BLOCK || block == Blocks.MYCELIUM) {
                    this.level().setBlock(pos, Blocks.DIRT.defaultBlockState(), 3);
                }
            }
        }
    }

    // ---- Combat: area jump damage ----

    private void doJumpDamage(double x, double y, double z, double dist, double damage, int knock) {
        AABB bb = new AABB(x - dist, y - 10.0, z - dist, x + dist, y + 10.0, z + dist);
        List<LivingEntity> entities = this.level().getEntitiesOfClass(LivingEntity.class, bb);
        for (LivingEntity target : entities) {
            if (target == null || target == this || !target.isAlive()) continue;
            if (target instanceof Godzilla) continue;
            if (target instanceof GodzillaHead || target instanceof Ghost || target instanceof GhostSkelly) continue;

            // Original (orig Godzilla.java:509-512) deals half as unattributed explosion
            // damage and half as fall damage. The previous port used genericKill (the
            // /kill source), which bypassed Creative/Spectator invulnerability (BUG-006).
            target.hurt(this.damageSources().explosion(null, null), (float) damage / 2.0f);
            target.hurt(this.damageSources().fall(), (float) damage / 2.0f);
            this.level().playSound(null, target.getX(), target.getY(), target.getZ(),
                    SoundEvents.GENERIC_EXPLODE.value(), SoundSource.HOSTILE,
                    0.85f, 1.0f + (this.getRandom().nextFloat() - this.getRandom().nextFloat()) * 0.5f);

            if (knock != 0) {
                float angle = (float) Math.atan2(target.getZ() - this.getZ(), target.getX() - this.getX());
                target.push(Math.cos(angle) * 3.5, 0.75, Math.sin(angle) * 3.5);
            }
        }
    }

    // ---- Combat: fire cannon ----

    private void fireCannon(LivingEntity targetEntity) {
        double yoff = 19.0;
        double xzoff = 22.0;
        double cx = this.getX() - xzoff * Math.sin(Math.toRadians(this.yHeadRot));
        double cz = this.getZ() + xzoff * Math.cos(Math.toRadians(this.yHeadRot));

        if (this.streamCount > 0) {
            this.level().playSound(null, cx, this.getY() + yoff, cz,
                    SoundEvents.TNT_PRIMED, SoundSource.HOSTILE,
                    1.0f, 1.0f / (this.getRandom().nextFloat() * 0.4f + 0.8f));

            BetterFireball bf = new BetterFireball(this.level(), this,
                    new Vec3(targetEntity.getX() - cx,
                             targetEntity.getY() + targetEntity.getBbHeight() / 2.0f - (this.getY() + yoff),
                             targetEntity.getZ() - cz));
            bf.moveTo(cx, this.getY() + yoff, cz, this.getYRot(), 0.0f);
            bf.setPos(cx, this.getY() + yoff, cz);
            bf.setReallyBig();
            this.level().addFreshEntity(bf);

            for (int i = 0; i < 5; i++) {
                float r1 = 5.0f * (this.getRandom().nextFloat() - this.getRandom().nextFloat());
                float r2 = 3.0f * (this.getRandom().nextFloat() - this.getRandom().nextFloat());
                float r3 = 5.0f * (this.getRandom().nextFloat() - this.getRandom().nextFloat());
                bf = new BetterFireball(this.level(), this,
                        new Vec3(targetEntity.getX() - cx + r1,
                                 targetEntity.getY() + targetEntity.getBbHeight() / 2.0f - (this.getY() + yoff) + r2,
                                 targetEntity.getZ() - cz + r3));
                bf.moveTo(cx, this.getY() + yoff, cz, this.getYRot(), 0.0f);
                bf.setPos(cx, this.getY() + yoff, cz);
                bf.setBig();
                if (this.getRandom().nextInt(2) == 1) bf.setSmall();
                this.level().addFreshEntity(bf);
            }

            this.level().playSound(null, cx, this.getY() + yoff, cz,
                    SoundEvents.ARROW_SHOOT, SoundSource.HOSTILE,
                    1.0f, 1.0f / (this.getRandom().nextFloat() * 0.4f + 0.8f));
            this.streamCount--;
        }
    }

    // ---- Combat: lightning attack ----

    private void doLightningAttack(LivingEntity targetEntity) {
        if (targetEntity == null) return;
        targetEntity.hurt(this.damageSources().mobAttack(this), 100.0f);
        targetEntity.igniteForSeconds(5);

        if (this.level() instanceof ServerLevel serverLevel) {
            boolean griefing = serverLevel.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING);
            serverLevel.explode(this, targetEntity.getX(), targetEntity.getY(), targetEntity.getZ(), 3.0f,
                    griefing ? Level.ExplosionInteraction.MOB : Level.ExplosionInteraction.NONE);

            LightningBolt bolt1 = new LightningBolt(EntityType.LIGHTNING_BOLT, serverLevel);
            bolt1.moveTo(targetEntity.getX(), targetEntity.getY() + 1.0, targetEntity.getZ());
            serverLevel.addFreshEntity(bolt1);

            LightningBolt bolt2 = new LightningBolt(EntityType.LIGHTNING_BOLT, serverLevel);
            bolt2.moveTo(this.getX(), this.getY() + 15.0, this.getZ());
            serverLevel.addFreshEntity(bolt2);
        }
    }

    // ---- Target selection ----

    private boolean isSuitableTarget(LivingEntity target) {
        if (target == null || target == this || !target.isAlive()) return false;
        if (!this.getSensing().hasLineOfSight(target)) return false;
        if (target instanceof Godzilla) return false;
        if (target instanceof GodzillaHead) return false;
        if (target instanceof Creeper) return false;
        if (target instanceof Zombie) return false;
        if (target instanceof Spider) return false;
        if (target instanceof Skeleton) return false;
        if (target instanceof Ghost) return false;
        if (target instanceof GhostSkelly) return false;
        // Don't pick fights with peers â€” Mothra, the royal couple, and other
        // OreSpawn bosses are tracked separately so Mobzilla doesn't grief
        // boss arenas and so co-existing bosses don't cancel each other out.
        if (target instanceof Mothra) return false;
        if (MyUtils.isBigBoss(target) || MyUtils.isRoyalty(target)) return false;
        if (target instanceof Player player) {
            if (player.getAbilities().instabuild) return false;
        }
        return true;
    }

    private LivingEntity findSomethingToAttack() {
        // BOSS-017: orig Godzilla.java:524-527 — PlayNicely pacifies AND
        // fakes headFound=1, also suppressing the GodzillaHead sidecar.
        if (danger.orespawn.OreSpawnConfig.PLAY_NICELY.get()) {
            this.headFound = 1;
            return null;
        }
        AABB searchBox = this.getBoundingBox().inflate(64.0, 40.0, 64.0);
        List<LivingEntity> entities = this.level().getEntitiesOfClass(LivingEntity.class, searchBox);
        entities.sort(this.targetSorter);
        LivingEntity ret = null;
        boolean villagerFound = false;
        this.headFound = 0;
        for (LivingEntity entity : entities) {
            if (entity instanceof GodzillaHead) { this.headFound = 1; }
            if (!villagerFound && entity instanceof Villager && entity.isAlive()
                    && this.getSensing().hasLineOfSight(entity)) {
                ret = entity;
                villagerFound = true;
            }
            if (ret == null && !villagerFound && isSuitableTarget(entity)) {
                ret = entity;
            }
        }
        return ret;
    }

    // ---- Main AI loop ----

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
        if (this.level().isClientSide) return;
        super.customServerAiStep();

        // 1.7.10 fidelity: continuously assert the per-world "Mobzilla has
        // spawned" flag while alive so chunk-load races can't sneak a second
        // ambient spawn in. The config gate lets servers opt into the legacy
        // multi-spawn behavior if they want.
        if (OreSpawnConfig.MOBZILLA_SINGLE_SPAWN.get() && this.level() instanceof ServerLevel sl) {
            MobzillaSpawnTracker.get(sl).markSpawned();
        }

        ++this.ticker;
        if (this.ticker > 30000) this.ticker = 0;
        if (this.ticker % 100 == 0) this.streamCount = 8;
        if (this.hurtTimer > 0) --this.hurtTimer;
        if (this.jumpTimer > 0) --this.jumpTimer;

        if (this.getRandom().nextInt(200) == 0) {
            this.setTarget(null);
        }

        // BOSS-017: orig Godzilla.java:290 gates jump detection (and thus the
        // landing damage), and :313/:334/:352 gate both crush loops and the
        // front jump-damage pulse, all behind PlayNicely == 0. Dynamic read
        // like every other consumer.
        boolean playNicely = danger.orespawn.OreSpawnConfig.PLAY_NICELY.get();
        // orig :271 — per-tick client sync of the flag (watcher slot 21).
        this.entityData.set(DATA_PLAY_NICELY, playNicely ? 1 : 0);

        // --- Jump landing damage ---
        Vec3 motion = this.getDeltaMovement();
        if (!playNicely) {
        if (motion.y < -0.95) this.jumped = 1;
        if (motion.y < -1.5) this.jumped = 2;
        if (this.jumped != 0 && motion.y > -0.1) {
            double landingDamageMultiplier = this.jumped == 2 ? 1.5 : 1.0;
            this.doJumpDamage(this.getX(), this.getY(), this.getZ(), 10.0, 150.0 * landingDamageMultiplier, 0);
            this.doJumpDamage(this.getX(), this.getY(), this.getZ(), 15.0, 75.0 * landingDamageMultiplier, 0);
            this.doJumpDamage(this.getX(), this.getY(), this.getZ(), 25.0, 37.5 * landingDamageMultiplier, 0);
            this.jumped = 0;
        }

        // --- Block crushing around self ---
        // 1.7.10 invoked crushBlocks() every tick over a (2*xzrange+1)Â² area
        // (â‰ˆ625 blocks at xzrange=12) twice per tick. On a modern 20-TPS
        // server with chunk-section mailbox dispatch that pegs the main
        // thread; we throttle to every 4th tick (5 Hz) and reduce the
        // stomping silhouette while idle.
        int xzrange = (this.getAttacking() != 0) ? 14 : 10;
        if (this.ticker % 4 == 0) {
            int k = -3 + (this.ticker / 4) % 30;
            crushBlocks(this.getX(), this.getZ(), xzrange, k);
        }

        // --- Block crushing in front (head direction) ---
        double frontX = this.getX() + 16.0 * Math.sin(Math.toRadians(this.yHeadRot));
        double frontZ = this.getZ() - 16.0 * Math.cos(Math.toRadians(this.yHeadRot));
        int k2 = -3 + (this.ticker / 4) % 12;
        if (this.ticker % 4 == 0) {
            crushBlocks(frontX, frontZ, xzrange, k2);
            if (k2 == 0) {
                this.doJumpDamage(frontX, this.getY(), frontZ, 15.0, 75.0, 1);
            }
        }
        } // end !playNicely (BOSS-017 crush/jump gate)

        // --- Target acquisition and combat ---
        if (this.getRandom().nextInt(Math.max(1, 5 - this.largeUnknownDetected)) == 1) {
            // BOSS-017: orig Godzilla.java:356-359 — PlayNicely nulls the
            // current target every combat pass.
            if (playNicely) {
                this.setTarget(null);
            }
            LivingEntity currentTarget = this.getTarget();
            if (currentTarget != null) {
                if (!currentTarget.isAlive()) {
                    this.setTarget(null);
                    currentTarget = null;
                } else if (currentTarget instanceof Godzilla || currentTarget instanceof GodzillaHead) {
                    this.setTarget(null);
                    currentTarget = null;
                }
            }
            if (currentTarget == null) {
                currentTarget = this.findSomethingToAttack();
                if (this.headFound == 0) {
                    GodzillaHead head = ModEntities.GODZILLA_HEAD.get().create(this.level());
                    if (head != null) {
                        head.moveTo(this.getX(), this.getY() + 20, this.getZ(), 0.0F, 0.0F);
                        this.level().addFreshEntity(head);
                        this.headFound = 1;
                    }
                }
            }

            if (currentTarget != null) {
                this.lookAt(currentTarget, 10.0f, 10.0f);

                if (this.getRandom().nextInt(65) == 1 && this.myGetDistanceSqToEntity(currentTarget) > 300.0) {
                    this.doLightningAttack(currentTarget);

                } else if (this.getRandom().nextInt(Math.max(1, 20 - this.largeUnknownDetected * 5)) == 1
                        && this.jumpTimer == 0) {
                    this.jumpAtEntity(currentTarget);
                    this.jumpTimer = 30;

                } else if (this.myGetDistanceSqToEntity(currentTarget)
                        < (double) (300.0f + currentTarget.getBbWidth() / 2.0f * (currentTarget.getBbWidth() / 2.0f))) {
                    this.setAttacking(1);
                    this.getNavigation().moveTo(currentTarget, 1.0);
                    if (this.getRandom().nextInt(Math.max(1, 4 - this.largeUnknownDetected)) == 0
                            || this.getRandom().nextInt(Math.max(1, 3 - this.largeUnknownDetected)) == 1) {
                        this.doHurtTarget(currentTarget);
                    }

                } else {
                    this.getNavigation().moveTo(currentTarget, 1.0);
                    if (this.getHorizontalDistanceSqToEntity(currentTarget) > 625.0) {
                        if (this.streamCount > 0) {
                            this.setAttacking(1);
                            double angleToTarget = Math.atan2(currentTarget.getZ() - this.getZ(), currentTarget.getX() - this.getX());
                            double headYawRad = Math.toRadians((this.yHeadRot + 90.0f) % 360.0f);
                            double angleDiff = Math.abs(angleToTarget - headYawRad) % (Math.PI * 2.0);
                            if (angleDiff > Math.PI) angleDiff -= Math.PI * 2.0;
                            if (Math.abs(angleDiff) < 0.5) {
                                this.fireCannon(currentTarget);
                            }
                        } else {
                            this.setAttacking(0);
                        }
                    } else {
                        this.setAttacking(0);
                    }
                }
            } else {
                this.setAttacking(0);
                this.streamCount = 8;
            }
        }

        if (this.getRandom().nextInt(35) == 1 && this.getHealth() < this.mygetMaxHealth()) {
            this.heal(5.0f);
        }
    }

    // ---- Melee attack ----

    @Override
    public boolean doHurtTarget(Entity target) {
        if (target instanceof LivingEntity living) {
            float footprintArea = living.getBbHeight() * living.getBbWidth();
            if (footprintArea > LARGE_ENTITY_AREA_THRESHOLD && !(target instanceof Godzilla)
                    && !MyUtils.isBigBoss(target) && !MyUtils.isRoyalty(target)) {
                living.setHealth(living.getHealth() / 2.0f);
                living.hurt(this.damageSources().mobAttack(this), 150.0f * 10.0f);
                this.largeUnknownDetected = 1;
            }
        }

        if (target instanceof net.minecraft.world.entity.boss.enderdragon.EnderDragon dragon) {
            dragon.hurt(this.damageSources().mobAttack(this), 75.0f);
        }

        if (super.doHurtTarget(target)) {
            if (target instanceof LivingEntity) {
                double knockbackHorizontal = MELEE_PUSH_HORIZONTAL;
                double knockbackVertical = MELEE_PUSH_VERTICAL;
                float pushAngle = (float) Math.atan2(target.getZ() - this.getZ(), target.getX() - this.getX());
                if (target.isRemoved() || target instanceof Player) {
                    knockbackVertical *= 2.0;
                }
                target.push(Math.cos(pushAngle) * knockbackHorizontal, knockbackVertical, Math.sin(pushAngle) * knockbackHorizontal);
            }
            return true;
        }
        return false;
    }

    // ---- Incoming damage ----

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (this.hurtTimer > 0) return false;

        float cappedDamage = Math.min(amount, INCOMING_DAMAGE_CAP);
        Entity attacker = source.getEntity();
        if (attacker instanceof LivingEntity living) {
            float footprintArea = living.getBbHeight() * living.getBbWidth();
            if (footprintArea > LARGE_ENTITY_AREA_THRESHOLD && !(living instanceof Godzilla)
                    && !MyUtils.isBigBoss(attacker) && !MyUtils.isRoyalty(attacker)) {
                cappedDamage /= 10.0f;
                this.hurtTimer = 50;
                this.largeUnknownDetected = 1;
            }
        }

        if (source.getMsgId().equals("cactus")) return false;

        boolean ret = super.hurt(source, cappedDamage);
        this.hurtTimer = 20;

        attacker = source.getEntity();
        if (attacker instanceof LivingEntity living && !(attacker instanceof Godzilla)
                && !(attacker instanceof GodzillaHead)) {
            this.setTarget(living);
            this.getNavigation().moveTo(living, 1.2);
        }
        return ret;
    }

    // Death drops are fully data-driven via loot_table/entities/godzilla.json
    // (orig Godzilla.java:820-1775: painting, 50-79 godzilla scale, 100-259
    // raw beef, 50-109 bone, 25-39 rolls of the d80 enchanted-gear table).
}
