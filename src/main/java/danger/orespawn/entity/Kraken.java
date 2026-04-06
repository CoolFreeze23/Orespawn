package danger.orespawn.entity;

import java.util.Comparator;
import java.util.List;
import danger.orespawn.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.animal.Squid;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class Kraken extends Monster {
    private static final EntityDataAccessor<Integer> DATA_ATTACKING =
            SynchedEntityData.defineId(Kraken.class, EntityDataSerializers.INT);

    private final Comparator<Entity> targetSorter;
    private BlockPos currentFlightTarget = null;
    private LivingEntity caught = null;
    private int newtarget = 0;
    private int release = 0;
    private int weatherSet = 10;
    private int longEnough = 3600;
    private int callReinforcements = 0;
    private boolean hitByPlayer = false;
    private int straightDown = 1;
    private int hurtTimer = 0;

    public Kraken(EntityType<? extends Kraken> type, Level level) {
        super(type, level);
        this.xpReward = 500;
        this.targetSorter = Comparator.comparingDouble(this::distanceToSqr);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 3000.0)
                .add(Attributes.MOVEMENT_SPEED, 0.5)
                .add(Attributes.ATTACK_DAMAGE, 80.0)
                .add(Attributes.FOLLOW_RANGE, 64.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0)
                .add(Attributes.ARMOR, 8.0);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_ATTACKING, 0);
    }

    @Override
    public boolean canBreatheUnderwater() {
        return true;
    }

    @Override
    public boolean fireImmune() {
        return true;
    }

    @Override
    public boolean removeWhenFarAway(double dist) {
        if (this.isPersistenceRequired()) return false;
        if (this.longEnough <= 0) return true;
        if (this.getY() > 150.0 && this.getHealth() < this.mygetMaxHealth() / 2.0f) return true;
        if (this.getY() > 180.0 && this.longEnough <= 0) {
            this.discard();
            return true;
        }
        return false;
    }

    public int mygetMaxHealth() {
        return 3000;
    }

    public int getKrakenHealth() {
        return (int) this.getHealth();
    }

    @Override
    public void tick() {
        super.tick();
        if (this.isRemoved()) return;

        if (this.currentFlightTarget == null) {
            this.currentFlightTarget = new BlockPos(
                    (int) this.getX(), (int) (this.getY() - 10.0), (int) this.getZ());
        } else {
            Vec3 dm = this.getDeltaMovement();
            double dampedY = this.getY() < this.currentFlightTarget.getY()
                    ? dm.y * 0.72 : dm.y * 0.5;
            this.setDeltaMovement(dm.x, dampedY, dm.z);
        }

        if (this.weatherSet > 0) {
            --this.weatherSet;
            if (this.weatherSet == 0 && this.level() instanceof ServerLevel serverLevel) {
                serverLevel.setWeatherParameters(0, 6000, true, true);
                this.weatherSet = 100;
            }
        }
    }

    @Override
    public void aiStep() {
        super.aiStep();
    }

    @Override
    protected void customServerAiStep() {
        if (this.isRemoved()) return;
        super.customServerAiStep();

        if (this.hurtTimer > 0) --this.hurtTimer;
        if (this.longEnough > 0) --this.longEnough;

        if (this.getRandom().nextInt(400) == 1 && this.level() instanceof ServerLevel serverLevel) {
            LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(serverLevel);
            if (bolt != null) {
                bolt.moveTo(this.getX(), this.getY() - 16.0, this.getZ());
                serverLevel.addFreshEntity(bolt);
            }
        }

        if (this.currentFlightTarget == null) {
            this.currentFlightTarget = new BlockPos(
                    (int) this.getX(), (int) this.getY(), (int) this.getZ());
        }

        double dxFlight = this.currentFlightTarget.getX() - this.getX();
        double dyFlight = this.currentFlightTarget.getY() - this.getY();
        double dzFlight = this.currentFlightTarget.getZ() - this.getZ();
        double distSqToTarget = dxFlight * dxFlight + dyFlight * dyFlight + dzFlight * dzFlight;

        if (this.newtarget != 0 || this.getRandom().nextInt(250) == 1 || distSqToTarget < 9.1) {
            pickNewFlightTarget();
        } else if (this.caught == null && this.getRandom().nextInt(8) == 1) {
            searchForPrey();
        }

        handleCaughtEntity();
        applyFlightMovement();
        applyObstructionAvoidance();

        if (this.getY() > 256.0 && !this.isPersistenceRequired()) {
            this.discard();
        }
    }

    private void pickNewFlightTarget() {
        this.newtarget = 0;
        int groundDist;
        for (groundDist = 0; groundDist < 31; ++groundDist) {
            BlockPos checkPos = new BlockPos(
                    (int) this.getX(), (int) this.getY() - groundDist, (int) this.getZ());
            if (!this.level().getBlockState(checkPos).isAir()) {
                this.straightDown = 0;
                break;
            }
        }
        groundDist = 20 - groundDist;

        int keepTrying = 50;
        boolean foundAir = false;
        int targetX = (int) this.getX();
        int targetY = (int) this.getY() + groundDist;
        int targetZ = (int) this.getZ();

        while (!foundAir && keepTrying > 0) {
            int xdir = this.getRandom().nextInt(6) + 12;
            int zdir = this.getRandom().nextInt(6) + 12;
            if (this.getRandom().nextInt(2) == 0) zdir = -zdir;
            if (this.getRandom().nextInt(2) == 0) xdir = -xdir;
            if (this.straightDown != 0) {
                xdir = 0;
                zdir = 0;
            }
            targetX = (int) this.getX() + xdir;
            targetY = (int) this.getY() + groundDist + this.getRandom().nextInt(9) - 6;
            targetZ = (int) this.getZ() + zdir;
            BlockPos candidatePos = new BlockPos(targetX, targetY, targetZ);
            if (this.level().getBlockState(candidatePos).isAir()
                    && this.canSeeTarget(targetX, targetY, targetZ)) {
                foundAir = true;
            }
            --keepTrying;
        }
        this.currentFlightTarget = new BlockPos(targetX, targetY, targetZ);

        if (this.longEnough <= 0
                || (this.getY() < 200.0 && this.getHealth() < this.mygetMaxHealth() / 4.0f)) {
            this.currentFlightTarget = new BlockPos(
                    this.currentFlightTarget.getX(),
                    this.currentFlightTarget.getY() + 30,
                    this.currentFlightTarget.getZ());

            if (this.hitByPlayer && this.callReinforcements == 0
                    && this.getHealth() < this.mygetMaxHealth() / 8.0f
                    && this.getY() > 130.0
                    && this.level() instanceof ServerLevel serverLevel) {
                this.callReinforcements = 1;
                for (int i = 0; i < 10; i++) {
                    Entity newEntity = this.getType().create(serverLevel);
                    if (newEntity != null) {
                        double sx = this.getX() + this.getRandom().nextInt(10)
                                - this.getRandom().nextInt(10);
                        double sz = this.getZ() + this.getRandom().nextInt(10)
                                - this.getRandom().nextInt(10);
                        newEntity.moveTo(sx, 170.0, sz,
                                this.getRandom().nextFloat() * 360.0f, 0.0f);
                        serverLevel.addFreshEntity(newEntity);
                    }
                }
            }
        }
    }

    private void searchForPrey() {
        Player playerTarget = findNearestValidPlayer();
        if (playerTarget != null && this.getSensing().hasLineOfSight(playerTarget)) {
            this.currentFlightTarget = new BlockPos(
                    (int) playerTarget.getX(),
                    (int) playerTarget.getY() + 15,
                    (int) playerTarget.getZ());
            this.attackWithSomething(playerTarget);
        }
        if (playerTarget == null && this.getRandom().nextInt(2) == 0) {
            LivingEntity entityTarget = findSomethingToAttack();
            if (entityTarget != null) {
                this.currentFlightTarget = new BlockPos(
                        (int) entityTarget.getX(),
                        (int) entityTarget.getY() + 15,
                        (int) entityTarget.getZ());
                this.attackWithSomething(entityTarget);
            }
        }
    }

    private void handleCaughtEntity() {
        if (this.caught == null) return;

        if (!this.caught.isRemoved() && this.caught.isAlive()) {
            this.currentFlightTarget = new BlockPos(
                    (int) this.getX(), 200, (int) this.getZ());
            if (this.getY() > 190.0) {
                this.release = 1;
            }
            Vec3 myMotion = this.getDeltaMovement();
            this.caught.setDeltaMovement(myMotion);
            if (this.getY() - this.caught.getY() > 16.0) {
                this.caught.setDeltaMovement(
                        this.caught.getDeltaMovement().add(0, 0.25, 0));
            }
            this.caught.setPos(this.getX(), this.getY() - 15.0, this.getZ());
            this.caught.setYRot(this.getYRot());

            if (this.getRandom().nextInt(50) == 1) {
                this.doHurtTarget(this.caught);
            }
            if (this.release != 0 || this.getRandom().nextInt(250) == 1) {
                releaseCaughtEntity();
            }
        } else {
            releaseCaughtEntity();
        }
    }

    private void releaseCaughtEntity() {
        this.caught = null;
        this.newtarget = 1;
        this.release = 0;
        this.setAttacking(0);
    }

    private void applyFlightMovement() {
        double var1 = this.currentFlightTarget.getX() + 0.3 - this.getX();
        double var3 = this.currentFlightTarget.getY() + 0.1 - this.getY();
        double var5 = this.currentFlightTarget.getZ() + 0.3 - this.getZ();
        Vec3 motion = this.getDeltaMovement();
        double mx = motion.x + (Math.signum(var1) * 0.45 - motion.x) * 0.15;
        double my = motion.y + (Math.signum(var3) * 0.70999 - motion.y) * 0.202;
        double mz = motion.z + (Math.signum(var5) * 0.45 - motion.z) * 0.15;
        this.setDeltaMovement(mx, my, mz);

        float targetYaw = (float) (Math.atan2(mz, mx) * 180.0 / Math.PI) - 90.0f;
        float yawDiff = Mth.wrapDegrees(targetYaw - this.getYRot());
        this.zza = 0.4f;
        if (Math.abs(mx) + Math.abs(mz) < 0.15) {
            yawDiff = 0.0f;
        }
        this.setYRot(this.getYRot() + yawDiff / 5.0f);
    }

    private void applyObstructionAvoidance() {
        double obstructionFactor = 0.0;
        for (int k = -20; k < 18; k += 2) {
            for (int i = 1; i < 10; i += 2) {
                double dx = (double) i * Math.cos(Math.toRadians(this.getYRot() + 90.0f));
                double dz = (double) i * Math.sin(Math.toRadians(this.getYRot() + 90.0f));
                BlockPos checkPos = new BlockPos(
                        (int) (this.getX() + dx),
                        (int) this.getY() + k,
                        (int) (this.getZ() + dz));
                if (!this.level().getBlockState(checkPos).isAir()) {
                    obstructionFactor += 0.1;
                }
            }
        }
        if (obstructionFactor > 0) {
            this.setDeltaMovement(
                    this.getDeltaMovement().add(0, obstructionFactor * 0.08, 0));
            this.setPos(this.getX(),
                    this.getY() + obstructionFactor * 0.08, this.getZ());
        }
    }

    private Player findNearestValidPlayer() {
        AABB searchBox = this.getBoundingBox().inflate(25.0, 40.0, 25.0);
        List<Player> players = this.level().getEntitiesOfClass(Player.class, searchBox);
        Player nearest = null;
        double minDist = Double.MAX_VALUE;
        for (Player p : players) {
            if (p.getAbilities().instabuild) continue;
            double d = this.distanceToSqr(p);
            if (d < minDist) {
                minDist = d;
                nearest = p;
            }
        }
        return nearest;
    }

    private void attackWithSomething(LivingEntity target) {
        if (this.caught != null) return;
        double dx = this.getX() - target.getX();
        double dz = this.getZ() - target.getZ();
        double dy = this.getY() - target.getY() - 15.0;
        double dist = dx * dx + dz * dz + dy * dy;
        if (dist < 30.0) {
            this.caught = target;
            this.release = 0;
            this.setAttacking(1);
        }
    }

    public boolean canSeeTarget(double pX, double pY, double pZ) {
        Vec3 from = new Vec3(this.getX(), this.getY() + 0.75, this.getZ());
        Vec3 to = new Vec3(pX, pY, pZ);
        HitResult result = this.level().clip(new ClipContext(
                from, to, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
        return result.getType() == HitResult.Type.MISS;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        Entity e = source.getEntity();
        if (this.currentFlightTarget != null && e instanceof Player
                && this.getHealth() > this.mygetMaxHealth() / 4.0f) {
            this.hitByPlayer = true;
            this.currentFlightTarget = new BlockPos(
                    (int) e.getX(), (int) e.getY() + 15, (int) e.getZ());
        }
        if (this.hurtTimer > 0) return false;
        this.hurtTimer = 30;
        boolean ret = super.hurt(source, amount);
        if (this.getRandom().nextInt(2) == 1) {
            this.release = 1;
        }
        return ret;
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

    @Override
    protected SoundEvent getAmbientSound() {
        if (this.getRandom().nextInt(5) == 0) {
            return SoundEvents.ELDER_GUARDIAN_AMBIENT;
        }
        return null;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return null;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.ELDER_GUARDIAN_DEATH;
    }

    @Override
    protected float getSoundVolume() {
        return 2.0f;
    }

    @Override
    public float getVoicePitch() {
        return 1.0f;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("LongEnough", this.longEnough);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.longEnough = tag.getInt("LongEnough");
    }

    private void enchantItem(ItemStack stack, ResourceKey<Enchantment> key, int enchLevel) {
        this.level().registryAccess()
                .lookup(Registries.ENCHANTMENT)
                .flatMap(reg -> reg.get(key))
                .ifPresent(holder -> stack.enchant(holder, enchLevel));
    }

    private ItemStack dropItemRand(ItemStack stack) {
        double ox = this.getX() + this.getRandom().nextInt(8) - this.getRandom().nextInt(8);
        double oy = this.getY() + 1.0;
        double oz = this.getZ() + this.getRandom().nextInt(8) - this.getRandom().nextInt(8);
        ItemEntity itemEntity = new ItemEntity(this.level(), ox, oy, oz, stack);
        this.level().addFreshEntity(itemEntity);
        return stack;
    }

    private void enchantSword(ItemStack is) {
        if (this.getRandom().nextInt(6) == 1) enchantItem(is, Enchantments.SHARPNESS, 1 + this.getRandom().nextInt(5));
        if (this.getRandom().nextInt(6) == 1) enchantItem(is, Enchantments.SMITE, 1 + this.getRandom().nextInt(5));
        if (this.getRandom().nextInt(6) == 1) enchantItem(is, Enchantments.BANE_OF_ARTHROPODS, 1 + this.getRandom().nextInt(5));
        if (this.getRandom().nextInt(6) == 1) enchantItem(is, Enchantments.KNOCKBACK, 1 + this.getRandom().nextInt(5));
        if (this.getRandom().nextInt(2) == 1) enchantItem(is, Enchantments.UNBREAKING, 2 + this.getRandom().nextInt(4));
        if (this.getRandom().nextInt(6) == 1) enchantItem(is, Enchantments.FIRE_ASPECT, 1 + this.getRandom().nextInt(5));
        if (this.getRandom().nextInt(6) == 1) enchantItem(is, Enchantments.SHARPNESS, 1 + this.getRandom().nextInt(5));
    }

    private void enchantTool(ItemStack is) {
        if (this.getRandom().nextInt(2) == 1) enchantItem(is, Enchantments.UNBREAKING, 2 + this.getRandom().nextInt(4));
        if (this.getRandom().nextInt(6) == 1) enchantItem(is, Enchantments.EFFICIENCY, 1 + this.getRandom().nextInt(5));
    }

    private void enchantToolSilk(ItemStack is) {
        enchantTool(is);
        if (this.getRandom().nextInt(6) == 1) enchantItem(is, Enchantments.SILK_TOUCH, 1 + this.getRandom().nextInt(5));
    }

    private void enchantHelmet(ItemStack is) {
        if (this.getRandom().nextInt(6) == 1) enchantItem(is, Enchantments.PROTECTION, 1 + this.getRandom().nextInt(5));
        if (this.getRandom().nextInt(6) == 1) enchantItem(is, Enchantments.PROJECTILE_PROTECTION, 1 + this.getRandom().nextInt(5));
        if (this.getRandom().nextInt(6) == 1) enchantItem(is, Enchantments.FIRE_PROTECTION, 1 + this.getRandom().nextInt(5));
        if (this.getRandom().nextInt(6) == 1) enchantItem(is, Enchantments.BLAST_PROTECTION, 1 + this.getRandom().nextInt(5));
        if (this.getRandom().nextInt(2) == 1) enchantItem(is, Enchantments.UNBREAKING, 2 + this.getRandom().nextInt(4));
        if (this.getRandom().nextInt(6) == 1) enchantItem(is, Enchantments.RESPIRATION, 1 + this.getRandom().nextInt(2));
        if (this.getRandom().nextInt(6) == 1) enchantItem(is, Enchantments.AQUA_AFFINITY, 1 + this.getRandom().nextInt(5));
    }

    private void enchantArmor(ItemStack is) {
        if (this.getRandom().nextInt(6) == 1) enchantItem(is, Enchantments.PROTECTION, 1 + this.getRandom().nextInt(5));
        if (this.getRandom().nextInt(6) == 1) enchantItem(is, Enchantments.PROJECTILE_PROTECTION, 1 + this.getRandom().nextInt(5));
        if (this.getRandom().nextInt(6) == 1) enchantItem(is, Enchantments.FIRE_PROTECTION, 1 + this.getRandom().nextInt(5));
        if (this.getRandom().nextInt(6) == 1) enchantItem(is, Enchantments.BLAST_PROTECTION, 1 + this.getRandom().nextInt(5));
        if (this.getRandom().nextInt(2) == 1) enchantItem(is, Enchantments.UNBREAKING, 2 + this.getRandom().nextInt(4));
    }

    private void enchantBoots(ItemStack is) {
        if (this.getRandom().nextInt(6) == 1) enchantItem(is, Enchantments.FEATHER_FALLING, 5 + this.getRandom().nextInt(5));
        if (this.getRandom().nextInt(2) == 1) enchantItem(is, Enchantments.UNBREAKING, 2 + this.getRandom().nextInt(4));
    }

    @Override
    protected void dropCustomDeathLoot(DamageSource source, int looting, boolean recentlyHit) {
        super.dropCustomDeathLoot(source, looting, recentlyHit);

        dropItemRand(new ItemStack(ModItems.KRAKEN_TOOTH.get(), 1));
        dropItemRand(new ItemStack(Items.GOLDEN_APPLE, 1));

        int fishCount = 120 + this.getRandom().nextInt(160);
        for (int i = 0; i < fishCount; i++) {
            dropItemRand(new ItemStack(Items.COOKED_COD, 1));
        }

        int bonusCount = 5 + this.getRandom().nextInt(10);
        for (int i = 0; i < bonusCount; i++) {
            int roll = this.getRandom().nextInt(53);
            ItemStack is;
            switch (roll) {
                case 0 -> dropItemRand(new ItemStack(ModItems.ULTIMATE_SWORD.get()));
                case 1 -> dropItemRand(new ItemStack(Items.DIAMOND));
                case 2 -> dropItemRand(new ItemStack(Items.EMERALD_BLOCK));
                case 3 -> enchantSword(dropItemRand(new ItemStack(Items.DIAMOND_SWORD)));
                case 4 -> enchantTool(dropItemRand(new ItemStack(Items.DIAMOND_SHOVEL)));
                case 5 -> enchantToolSilk(dropItemRand(new ItemStack(Items.DIAMOND_PICKAXE)));
                case 6 -> enchantTool(dropItemRand(new ItemStack(Items.DIAMOND_AXE)));
                case 7 -> enchantTool(dropItemRand(new ItemStack(Items.DIAMOND_HOE)));
                case 8 -> enchantHelmet(dropItemRand(new ItemStack(Items.DIAMOND_HELMET)));
                case 9 -> enchantArmor(dropItemRand(new ItemStack(Items.DIAMOND_CHESTPLATE)));
                case 10 -> enchantArmor(dropItemRand(new ItemStack(Items.DIAMOND_LEGGINGS)));
                case 11 -> enchantBoots(dropItemRand(new ItemStack(Items.DIAMOND_BOOTS)));
                case 12 -> dropItemRand(new ItemStack(ModItems.ULTIMATE_BOW.get()));
                case 13 -> dropItemRand(new ItemStack(ModItems.ULTIMATE_AXE.get()));
                case 14 -> dropItemRand(new ItemStack(Items.GOLD_INGOT));
                case 15 -> dropItemRand(new ItemStack(ModItems.ULTIMATE_PICKAXE.get()));
                case 16 -> enchantSword(dropItemRand(new ItemStack(Items.IRON_SWORD)));
                case 17 -> enchantTool(dropItemRand(new ItemStack(Items.IRON_SHOVEL)));
                case 18 -> enchantToolSilk(dropItemRand(new ItemStack(Items.IRON_PICKAXE)));
                case 19 -> enchantTool(dropItemRand(new ItemStack(Items.IRON_AXE)));
                case 20 -> enchantTool(dropItemRand(new ItemStack(Items.IRON_HOE)));
                case 21 -> enchantHelmet(dropItemRand(new ItemStack(Items.IRON_HELMET)));
                case 22 -> enchantArmor(dropItemRand(new ItemStack(Items.IRON_CHESTPLATE)));
                case 23 -> enchantArmor(dropItemRand(new ItemStack(Items.IRON_LEGGINGS)));
                case 24 -> enchantBoots(dropItemRand(new ItemStack(Items.IRON_BOOTS)));
                case 25 -> dropItemRand(new ItemStack(ModItems.ULTIMATE_SHOVEL.get()));
                case 26 -> dropItemRand(new ItemStack(Items.GOLD_BLOCK));
                case 27 -> dropItemRand(new ItemStack(Items.NAME_TAG));
                case 28 -> dropItemRand(new ItemStack(Items.IRON_INGOT));
                case 29 -> dropItemRand(new ItemStack(Items.SADDLE));
                case 30 -> enchantSword(dropItemRand(new ItemStack(Items.GOLDEN_SWORD)));
                case 31 -> enchantTool(dropItemRand(new ItemStack(Items.GOLDEN_SHOVEL)));
                case 32 -> enchantToolSilk(dropItemRand(new ItemStack(Items.GOLDEN_PICKAXE)));
                case 33 -> enchantTool(dropItemRand(new ItemStack(Items.GOLDEN_AXE)));
                case 34 -> enchantTool(dropItemRand(new ItemStack(Items.GOLDEN_HOE)));
                case 35 -> enchantHelmet(dropItemRand(new ItemStack(Items.CHAINMAIL_HELMET)));
                case 36 -> enchantArmor(dropItemRand(new ItemStack(Items.CHAINMAIL_CHESTPLATE)));
                case 37 -> enchantArmor(dropItemRand(new ItemStack(Items.CHAINMAIL_LEGGINGS)));
                case 38 -> enchantBoots(dropItemRand(new ItemStack(Items.CHAINMAIL_BOOTS)));
                case 39 -> dropItemRand(new ItemStack(Items.GOLDEN_APPLE));
                case 40 -> dropItemRand(new ItemStack(Items.DIAMOND_BLOCK));
                case 41 -> dropItemRand(new ItemStack(Items.ENCHANTED_GOLDEN_APPLE));
                case 42 -> enchantSword(dropItemRand(new ItemStack(ModItems.EXPERIENCE_SWORD.get())));
                // TODO: Cases 43-46 drop Experience armor; using diamond as placeholder
                case 43 -> enchantHelmet(dropItemRand(new ItemStack(Items.DIAMOND_HELMET)));
                case 44 -> enchantArmor(dropItemRand(new ItemStack(Items.DIAMOND_CHESTPLATE)));
                case 45 -> enchantArmor(dropItemRand(new ItemStack(Items.DIAMOND_LEGGINGS)));
                case 46 -> enchantBoots(dropItemRand(new ItemStack(Items.DIAMOND_BOOTS)));
                case 47 -> enchantSword(dropItemRand(new ItemStack(ModItems.AMETHYST_SWORD.get())));
                case 48 -> enchantTool(dropItemRand(new ItemStack(ModItems.AMETHYST_SHOVEL.get())));
                case 49 -> enchantToolSilk(dropItemRand(new ItemStack(ModItems.AMETHYST_PICKAXE.get())));
                case 50 -> enchantTool(dropItemRand(new ItemStack(ModItems.AMETHYST_AXE.get())));
                case 51 -> enchantTool(dropItemRand(new ItemStack(ModItems.AMETHYST_HOE.get())));
                case 52 -> dropItemRand(new ItemStack(ModItems.BLOCK_AMETHYST_ITEM.get()));
                default -> dropItemRand(new ItemStack(Items.DIAMOND));
            }
        }
    }

    private boolean isSuitableTarget(LivingEntity target) {
        if (target == null || target == this || !target.isAlive()) return false;
        if (!this.getSensing().hasLineOfSight(target)) return false;
        if (target instanceof Kraken) return false;
        if (target instanceof Squid) return false;
        if (target instanceof Player player) {
            return !player.getAbilities().instabuild && !player.getAbilities().invulnerable;
        }
        if (!target.onGround() && !target.isInWater()) return false;
        return true;
    }

    private LivingEntity findSomethingToAttack() {
        AABB searchBox = this.getBoundingBox().inflate(20.0, 40.0, 20.0);
        List<LivingEntity> entities = this.level().getEntitiesOfClass(
                LivingEntity.class, searchBox);
        entities.sort(this.targetSorter);
        for (LivingEntity entity : entities) {
            if (isSuitableTarget(entity)) return entity;
        }
        return null;
    }

    public final int getAttacking() {
        return this.entityData.get(DATA_ATTACKING);
    }

    public final void setAttacking(int value) {
        this.entityData.set(DATA_ATTACKING, value);
    }
}
