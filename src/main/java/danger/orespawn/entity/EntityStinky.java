package danger.orespawn.entity;

import java.util.Comparator;
import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.Difficulty;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtTargetGoal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import danger.orespawn.ModItems;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.OreSpawnConfig;
import danger.orespawn.entity.ai.TargetSelection;

public class EntityStinky extends TamableAnimal {
    // OPT-011: cached SoundEvents — identical createVariableRangeEvent ids,
    // allocated once per class instead of on every sound query.
    private static final SoundEvent SND_FART = SoundEvent.createVariableRangeEvent(
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "fart"));
    private static final SoundEvent SND_DUCK_HURT = SoundEvent.createVariableRangeEvent(
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "duck_hurt"));
    private static final SoundEvent SND_CRYO_DEATH = SoundEvent.createVariableRangeEvent(
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "cryo_death"));
    private static final EntityDataAccessor<Integer> DATA_SPYRO_FIRE =
            SynchedEntityData.defineId(EntityStinky.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_ACTIVITY =
            SynchedEntityData.defineId(EntityStinky.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_SKIN =
            SynchedEntityData.defineId(EntityStinky.class, EntityDataSerializers.INT);

    private static final double OWNER_FAR_DIST_SQ = 256.0;
    private static final float LOW_HEALTH_FRACTION = 0.25f;
    private static final int SKIN_VARIANT_COUNT = 19;

    private BlockPos currentFlightTarget = null;
    public int activity = 1;
    private int ownerFlying = 0;
    private final float moveSpeed = 0.3f;
    private int skinColor = -1;
    // orig Stinky.java:54-57 — the idle block-eat's nearest coal ore: its squared distance from the scan origin (99999 = none) and its position (ENT-S-119)
    private int closest = 99999;
    private int tx = 0;
    private int ty = 0;
    private int tz = 0;

    public EntityStinky(EntityType<? extends EntityStinky> type, Level level) {
        super(type, level);
        // OPT-009: constant speed - assert the attribute base once here instead
        // of re-writing it every tick (same value the removed per-tick call set).
        this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(this.moveSpeed);
        this.xpReward = 35;
        this.noPhysics = false;
        this.setOrderedToSit(false);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(2, new AvoidEntityGoal<>(this, Monster.class, 8.0f, 0.3, 0.4));
        this.goalSelector.addGoal(3, new MyEntityAIFollowOwner(this, 1.15, 12.0f, 2.0f));
        this.goalSelector.addGoal(4, new TemptGoal(this, 1.25, Ingredient.of(Items.BEEF), false));
        this.goalSelector.addGoal(5, new PanicGoal(this, 1.5));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 6.0f));
        this.goalSelector.addGoal(7, new WaterAvoidingRandomStrollGoal(this, 0.75));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));

        // MOD-033 (T9 A2, petsDefendOwner): the four target goals are modern only, a construction snapshot
        // (the helper read once here; goals register in the Mob ctor, the BOSS-017 shape — a config change
        // applies to newly spawned Stinkies); orig Stinky.java:67-77 registered no target tasks at all.
        // Registered but unconsumed at HEAD: nothing here reads the target slot (doMovement bites its own findSomethingToAttack pick; the 1-in-200 setTarget(null) only clears it),
        // so these goals wait for a consumer.
        if (OreSpawnConfig.petsDefendOwner()) {
            this.targetSelector.addGoal(1, new OwnerHurtByTargetGoal(this));
            this.targetSelector.addGoal(2, new OwnerHurtTargetGoal(this));
            this.targetSelector.addGoal(3, new HurtByTargetGoal(this));
            this.targetSelector.addGoal(4, new NearestAttackableTargetGoal<>(this, Monster.class, 10, true, false, e -> this.isTame()));
        }
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 100.0)
                .add(Attributes.MOVEMENT_SPEED, 0.3)
                .add(Attributes.ATTACK_DAMAGE, 10.0);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_SPYRO_FIRE, 1);
        builder.define(DATA_ACTIVITY, 1);
        builder.define(DATA_SKIN, 0);
    }

    public int getActivity() {
        this.activity = this.entityData.get(DATA_ACTIVITY);
        return this.activity;
    }

    public void setActivity(int val) {
        this.activity = val;
        this.entityData.set(DATA_ACTIVITY, val);
    }

    public int getSpyroFire() {
        return this.entityData.get(DATA_SPYRO_FIRE);
    }

    public void setSpyroFire(int val) {
        this.entityData.set(DATA_SPYRO_FIRE, val);
    }

    public int getSkin() {
        this.skinColor = this.entityData.get(DATA_SKIN);
        return this.skinColor;
    }

    public void setSkin(int val) {
        this.skinColor = val;
        this.entityData.set(DATA_SKIN, val);
    }

    @Override
    public boolean removeWhenFarAway(double dist) {
        if (this.isPersistenceRequired()) return false;
        return !this.isTame();
    }

    @Override
    public void tick() {
        super.tick();

        if (this.isInWater()) {
            Vec3 motion = this.getDeltaMovement();
            this.setDeltaMovement(motion.x, motion.y + 0.07, motion.z);
        }

        // orig Stinky.java:335-338 — 1-in-1750: "random.burp" + COAL dropped in front.
        if (!this.level().isClientSide && this.random.nextInt(1750) == 1) {
            this.playSound(SoundEvents.PLAYER_BURP, 1.0f, 1.0f);
            dropItemFront(Items.COAL.getDefaultInstance());
        }

        // orig Stinky.java:339-398 — 1-in-2000: "orespawn:fart" (pitch 1.5) + skin-indexed rear drop.
        if (!this.level().isClientSide && this.random.nextInt(2000) == 2) {
            this.playSound(SND_FART, 1.0f, 1.5f);
            dropRandomItemRear();
        }

        if (this.currentFlightTarget == null) {
            this.currentFlightTarget = this.blockPosition();
        }
        if (this.skinColor < 0) {
            this.skinColor = this.random.nextInt(SKIN_VARIANT_COUNT);
            this.setSkin(this.skinColor);
        }

        if (this.activity == 2) {
            Vec3 motion = this.getDeltaMovement();
            this.setDeltaMovement(motion.x, motion.y * 0.6, motion.z);
        }

        if (!this.level().isClientSide && this.random.nextInt(2000) == 1) {
            this.setSkin(this.random.nextInt(SKIN_VARIANT_COUNT));
        }
    }

    @Override
    public boolean causeFallDamage(float fallDistance, float multiplier, DamageSource source) {
        return false;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (source.getMsgId().equals("cactus")) return false;
        boolean ret = super.hurt(source, amount);
        this.setOrderedToSit(false);
        this.setActivity(2);
        return ret;
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (stack.is(Items.BEEF) && player.distanceToSqr(this) < 16.0) {
            if (!this.isTame()) {
                if (!this.level().isClientSide) {
                    if (this.random.nextInt(2) == 1) {
                        this.tame(player);
                        this.level().broadcastEntityEvent(this, (byte) 7);
                        this.heal(100.0f - this.getHealth());
                    } else {
                        this.level().broadcastEntityEvent(this, (byte) 6);
                    }
                }
            } else if (this.isOwnedBy(player)) {
                this.heal(100.0f - this.getHealth());
            }
            if (!player.getAbilities().instabuild) stack.shrink(1);
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }

        if (this.isTame() && this.isOwnedBy(player) && player.distanceToSqr(this) < 16.0) {
            if (!this.isOrderedToSit()) {
                this.setOrderedToSit(true);
                this.setActivity(1);
            } else {
                this.setOrderedToSit(false);
            }
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }

        return super.mobInteract(player, hand);
    }

    @Override
    protected void customServerAiStep() {
        if (this.isRemoved()) return;

        if (this.random.nextInt(200) == 1) {
            this.setTarget(null);
        }

        if (this.activity != 2) {
            super.customServerAiStep();
        }

        if (this.random.nextInt(100) == 1 && this.getHealth() < 100.0f) {
            this.heal(1.0f);
        }

        if (!this.isOrderedToSit()) {
            if (this.activity == 0) setActivity(1);
            if (this.random.nextInt(100) == 1) {
                setActivity(this.random.nextInt(20) == 1 ? 2 : 1);
            }

            this.ownerFlying = 0;
            if (this.isTame() && this.getOwner() instanceof Player ownerPlayer) {
                if (ownerPlayer.getAbilities().flying) {
                    this.ownerFlying = 1;
                    setActivity(2);
                }
            }

            if (this.activity == 1 && this.isTame() && this.getOwner() != null &&
                    this.distanceToSqr(this.getOwner()) > OWNER_FAR_DIST_SQ) {
                setActivity(2);
            }

            doMovement();

            // orig Stinky.java:582-583 — do_movement's activity-1 branch, reached only through :511's !isSitting(): the coal-ore eat is off under PlayNicely; the roll is spent ahead of the flag, orig's term order (ENT-S-116, ENT-S-119)
            if (this.activity == 1 && this.random.nextInt(50) == 0 && !OreSpawnConfig.PLAY_NICELY.get()) {
                eatCoalOre();
            }
        }
    }

    /**
     * orig Stinky.java:584-604 — the idle block-eat behind the :583 gate: {@code closest} reset to 99999 and the pick
     * cleared (:584-587); the six-face shells i = 1, 2, 3, 4, 6, 8 around the truncated origin
     * {@code ((int) posX, (int) posY + 1, (int) posZ)} — the loop :588-596: {@code j = min(i, 2)},
     * {@code scan_it(x, y + 1, z, i, j, i)}, the first shell that finds anything breaks, and past i = 4 the trailing
     * {@code ++i} skips 5 and 7; the nearest coal ore navigated to at 1.25 (:598) and, when its squared distance from
     * that origin is under 12 (:599), set to air with flag 2 (:600), the Stinky healed 1.0 (:601) and "random.burp"
     * played at 0.5 / pitch {@code nextFloat * 0.2 + 1.5} (:602 — PLAYER_BURP, the EntityGammaMetroid.scanForStone
     * idiom; the port rolls the entity random, ENT-S-093). The {@code (int)} casts are orig's truncation. (ENT-S-119)
     */
    private void eatCoalOre() {
        this.closest = 99999;
        this.tz = 0;
        this.ty = 0;
        this.tx = 0;
        for (int i = 1; i < 9; ++i) {
            int j = i;
            if (j > 2) {
                j = 2;
            }
            if (scanIt((int) this.getX(), (int) this.getY() + 1, (int) this.getZ(), i, j, i)) break;
            if (i < 4) continue;
            ++i;
        }
        if (this.closest < 99999) {
            this.getNavigation().moveTo(this.tx, this.ty, this.tz, 1.25);
            if (this.closest < 12) {
                this.level().setBlock(new BlockPos(this.tx, this.ty, this.tz), Blocks.AIR.defaultBlockState(), 2);
                this.heal(1.0f);
                this.playSound(SoundEvents.PLAYER_BURP, 0.5f, this.random.nextFloat() * 0.2f + 1.5f);
            }
        }
    }

    /**
     * orig Stinky.java:435-496 ({@code scan_it}): the six faces of the (dx, dy, dz) shell around (x, y, z) — the ±x faces
     * over y ± dy, z ± dz (:441-458), the ±y faces over x ± dx, z ± dz (:459-476), the ±z faces over x ± dx, y ± dy
     * (:477-494), the + face probed before the − face at each step; a coal ore ({@code Blocks.field_150365_q}) nearer
     * than {@code closest} by squared distance from (x, y, z) takes {@code closest} and {@code tx/ty/tz}; true when any
     * probe took it (ENT-S-119).
     */
    private boolean scanIt(int x, int y, int z, int dx, int dy, int dz) {
        int d;
        int found = 0;
        for (int i = -dy; i <= dy; ++i) {
            for (int j = -dz; j <= dz; ++j) {
                if (isCoalOre(x + dx, y + i, z + j) && (d = dx * dx + j * j + i * i) < this.closest) {
                    this.closest = d;
                    this.tx = x + dx;
                    this.ty = y + i;
                    this.tz = z + j;
                    ++found;
                }
                if (isCoalOre(x - dx, y + i, z + j) && (d = dx * dx + j * j + i * i) < this.closest) {
                    this.closest = d;
                    this.tx = x - dx;
                    this.ty = y + i;
                    this.tz = z + j;
                    ++found;
                }
            }
        }
        for (int i = -dx; i <= dx; ++i) {
            for (int j = -dz; j <= dz; ++j) {
                if (isCoalOre(x + i, y + dy, z + j) && (d = dy * dy + j * j + i * i) < this.closest) {
                    this.closest = d;
                    this.tx = x + i;
                    this.ty = y + dy;
                    this.tz = z + j;
                    ++found;
                }
                if (isCoalOre(x + i, y - dy, z + j) && (d = dy * dy + j * j + i * i) < this.closest) {
                    this.closest = d;
                    this.tx = x + i;
                    this.ty = y - dy;
                    this.tz = z + j;
                    ++found;
                }
            }
        }
        for (int i = -dx; i <= dx; ++i) {
            for (int j = -dy; j <= dy; ++j) {
                if (isCoalOre(x + i, y + j, z + dz) && (d = dz * dz + j * j + i * i) < this.closest) {
                    this.closest = d;
                    this.tx = x + i;
                    this.ty = y + j;
                    this.tz = z + dz;
                    ++found;
                }
                if (isCoalOre(x + i, y + j, z - dz) && (d = dz * dz + j * j + i * i) < this.closest) {
                    this.closest = d;
                    this.tx = x + i;
                    this.ty = y + j;
                    this.tz = z - dz;
                    ++found;
                }
            }
        }
        return found != 0;
    }

    /**
     * orig Stinky.java:443-444 etc. — {@code getBlock(x, y, z) == Blocks.field_150365_q}: coal ore, the mapping BeehiveFeature.java:38-39
     * records (ENT-S-119), read through {@code #minecraft:coal_ores}: the modern engine split 1.7.10's one coal ore into
     * {@code coal_ore} and {@code deepslate_coal_ore} — the ore features place one or the other by the stone they replace
     * (vanilla's ore_coal and the port's ore_boost_*.json pairs alike) — so the tag is the one block of 1.7.10 (PN-021, both modes).
     */
    private boolean isCoalOre(int x, int y, int z) {
        return this.level().getBlockState(new BlockPos(x, y, z)).is(BlockTags.COAL_ORES);
    }

    private void doMovement() {
        boolean doNew = false;
        if (this.currentFlightTarget == null) { // orig Stinky.java:548-551 — a null flight target is initialised AND retargeted this tick (ENT-S-126)
            doNew = true;
            this.currentFlightTarget = this.blockPosition();
        }

        boolean hasOwner = false;
        double ox = 0, oy = 0, oz = 0;
        LivingEntity owner = null;

        // orig Stinky.java:552 — the 1-in-300 retarget roll is activity 2's alone; do_movement itself runs for every activity (ENT-S-119)
        if (this.activity == 2 && this.random.nextInt(300) == 0) doNew = true;

        if (this.isTame() && this.getOwner() != null) {
            owner = this.getOwner();
            hasOwner = true;
            ox = owner.getX(); oy = owner.getY(); oz = owner.getZ();
            if (this.distanceToSqr(owner) > 100.0) doNew = true;
            if (this.ownerFlying != 0 && this.distanceToSqr(owner) > 36.0) doNew = true;
        }

        // orig Stinky.java:568-581 — the 1-in-7 idle attack pass, every activity, ahead of the activity-1 branch: the roll, then the difficulty, then the scan (ENT-S-119)
        if (this.random.nextInt(7) == 1 && this.level().getDifficulty() != Difficulty.PEACEFUL) {
            LivingEntity target = findSomethingToAttack();
            if (target != null) {
                if (this.isTame() && this.getHealth() / 100.0f < LOW_HEALTH_FRACTION) {
                    setActivity(2);
                    doNew = false;
                    this.currentFlightTarget = BlockPos.containing(
                            this.getX() + (this.getX() - target.getX()),
                            this.getY() + 1, this.getZ() + (this.getZ() - target.getZ()));
                } else {
                    setActivity(2);
                    this.currentFlightTarget = BlockPos.containing(target.getX(), target.getY() + 1, target.getZ());
                    doNew = false;
                    float reach = 3.0f + target.getBbWidth() / 2.0f;
                    if (this.distanceToSqr(target) < (double)(reach * reach)) {
                        this.doHurtTarget(target);
                    }
                }
            }
        }

        // orig Stinky.java:582-607 — activity 1 stops here (its block-eat is customServerAiStep's call after this method); the flight below is activity 2's (ENT-S-119)
        if (this.activity != 2) return;

        if (this.currentFlightTarget.closerToCenterThan(this.position(), 2.1)) {
            doNew = true;
        }

        if (doNew) {
            int keepTrying = 50;
            boolean found = false;
            while (!found && keepTrying > 0) {
                int gox = (int)(hasOwner ? ox : this.getX());
                int goy = (int)(hasOwner ? oy : this.getY());
                int goz = (int)(hasOwner ? oz : this.getZ());
                int xdir = this.random.nextInt(5) + 6;
                int zdir = this.random.nextInt(5) + 6;
                if (this.random.nextInt(2) == 0) zdir = -zdir;
                if (this.random.nextInt(2) == 0) xdir = -xdir;

                BlockPos newTarget = BlockPos.containing(gox + xdir,
                        goy + this.random.nextInt(6 + this.ownerFlying * 2) - 2,
                        goz + zdir);
                // orig Stinky.java:638 — the candidate is written to the flight target BEFORE the air-and-ray test (:639-641 only
                // re-arm the loop): a refused candidate is the one steered toward once the tries run out, and a boxed-in flyer
                // pays the fifty rays once per retarget, not every tick (ENT-S-126)
                this.currentFlightTarget = newTarget;
                // orig Stinky.java:640 — an air candidate the feet ray (posY + 0.75 → the candidate's block corner) cannot reach is refused as stone (ENT-S-123)
                if (this.level().getBlockState(newTarget).isAir()
                        && canSeeTarget(newTarget.getX(), newTarget.getY(), newTarget.getZ())) {
                    found = true;
                }
                --keepTrying;
            }
        }

        double speedFactor = 1.0;
        double vx = this.currentFlightTarget.getX() + 0.5 - this.getX();
        double vy = this.currentFlightTarget.getY() + 0.1 - this.getY();
        double vz = this.currentFlightTarget.getZ() + 0.5 - this.getZ();
        if (this.ownerFlying != 0) {
            speedFactor = 1.75;
            if (owner != null && this.distanceToSqr(owner) > 49.0) speedFactor = 3.5;
        }

        Vec3 motion = this.getDeltaMovement();
        double mx = motion.x + (Math.signum(vx) * 0.5 - motion.x) * 0.15 * speedFactor;
        double my = motion.y + (Math.signum(vy) * 0.7 - motion.y) * 0.21 * speedFactor;
        double mz = motion.z + (Math.signum(vz) * 0.5 - motion.z) * 0.15 * speedFactor;
        this.setDeltaMovement(mx, my, mz);

        float yaw = (float)(Math.atan2(mz, mx) * 180.0 / Math.PI) - 90.0f;
        float yawDelta = Mth.wrapDegrees(yaw - this.getYRot());
        this.setYRot(this.getYRot() + yawDelta / 3.0f);
    }

    private void dropItemFront(ItemStack stack) {
        float dropOffset = 0.75f + Math.abs(this.random.nextFloat() * 0.75f);
        double dx = this.getX() - (double) dropOffset * Math.sin(Math.toRadians(this.yBodyRot));
        double dz = this.getZ() + (double) dropOffset * Math.cos(Math.toRadians(this.yBodyRot));
        ItemEntity item = new ItemEntity(this.level(), dx, this.getY() + 0.9, dz, stack);
        this.level().addFreshEntity(item);
    }

    private void dropItemRear(ItemStack stack) {
        float dropOffset = 0.55f + Math.abs(this.random.nextFloat() * 0.55f);
        double dx = this.getX() + (double) dropOffset * Math.sin(Math.toRadians(this.yBodyRot));
        double dz = this.getZ() - (double) dropOffset * Math.cos(Math.toRadians(this.yBodyRot));
        ItemEntity item = new ItemEntity(this.level(), dx, this.getY() + 0.25, dz, stack);
        this.level().addFreshEntity(item);
    }

    private void dropRandomItemRear() {
        // orig Stinky.java:341-397 — rear drop indexed by the 19 skin variants.
        ItemStack drop = switch (this.skinColor) {
            case 0 -> new ItemStack(Items.BLAZE_POWDER);
            case 1 -> new ItemStack(Items.ROTTEN_FLESH);
            case 2 -> new ItemStack(Items.MELON_SEEDS);
            case 3 -> new ItemStack(ModItems.URANIUM_NUGGET.get());
            case 4 -> new ItemStack(Items.WHEAT);
            case 5 -> new ItemStack(Items.SUGAR_CANE);
            case 6 -> new ItemStack(Items.TORCH);
            case 7 -> new ItemStack(Items.EMERALD);
            case 8 -> new ItemStack(Items.GOLD_INGOT);
            case 9 -> new ItemStack(Items.OAK_LEAVES);
            case 10 -> new ItemStack(ModItems.TITANIUM_NUGGET.get());
            case 11 -> new ItemStack(ModItems.APPLE_TREE_SEED.get());
            case 12 -> new ItemStack(Items.DIAMOND);
            case 13 -> new ItemStack(Items.SAND);
            case 14 -> new ItemStack(Items.COBBLESTONE);
            case 15 -> new ItemStack(Items.BONE);
            case 16 -> new ItemStack(Items.STRING);
            case 17 -> new ItemStack(ModItems.CHERRY_TREE_SEED.get());
            case 18 -> new ItemStack(ModItems.PEACH_TREE_SEED.get());
            default -> ItemStack.EMPTY;
        };
        if (!drop.isEmpty()) {
            dropItemRear(drop);
        }
    }

    private boolean isSuitableTarget(LivingEntity target) {
        if (this.level().getDifficulty() == Difficulty.PEACEFUL) return false;
        if (target == null || target == this || !target.isAlive()) return false;
        if (!this.getSensing().hasLineOfSight(target)) return false;
        return target instanceof Monster;
    }

    private LivingEntity findSomethingToAttack() {
        if (OreSpawnConfig.PLAY_NICELY.get()) return null; // orig Stinky.java:688-690 — PlayNicely != 0 returns null ahead of the scan (ENT-S-115)
        AABB searchBox = this.getBoundingBox().inflate(12.0, 6.0, 12.0);
        List<LivingEntity> targets = this.level().getEntitiesOfClass(LivingEntity.class, searchBox);
        // OPT-021: nearest-first pick without the full list sort; TargetSelection
        // preserves the removed sort's order and stable-tie semantics exactly.
        // orig Stinky.java:699 — the loop takes the first candidate that passes isSuitableTarget AND the feet ray canSeeTarget(posX, posY, posZ) (ENT-S-118)
        return TargetSelection.firstMatch(targets, Comparator.comparingDouble(this::distanceToSqr),
                candidate -> this.isSuitableTarget(candidate) && this.canSeeTarget(candidate.getX(), candidate.getY(), candidate.getZ()));
    }

    /**
     * orig Stinky.java:317-319 ({@code canSeeTarget}): {@code worldObj.rayTraceBlocks(Vec3(posX, posY + 0.75, posZ),
     * Vec3(pX, pY, pZ), false) == null} — a block-only ray from 0.75 above the feet to a point; the scan (:699) aims it
     * at the candidate's own position, its feet. 1.7.10's {@code rayTraceBlocks(start, end, stopOnLiquid = false)} is
     * {@code func_147447_a(start, end, false, false, false)}: liquids never stop the ray ({@code Fluid.NONE}); blocks
     * without a collision box are NOT skipped ({@code ignoreBlockWithoutBoundingBox = false}) — every block that passes
     * {@code canCollideCheck} is tested on its selection bounds ({@code Block.collisionRayTrace}), the {@code OUTLINE}
     * shape, the mapping ENT-S-089 recorded for the same helper on the Vortex; a null result is a MISS (ENT-S-118).
     */
    private boolean canSeeTarget(double x, double y, double z) {
        Vec3 from = new Vec3(this.getX(), this.getY() + 0.75, this.getZ());
        return this.level().clip(new net.minecraft.world.level.ClipContext(from, new Vec3(x, y, z),
                net.minecraft.world.level.ClipContext.Block.OUTLINE, net.minecraft.world.level.ClipContext.Fluid.NONE, this))
                .getType() == net.minecraft.world.phys.HitResult.Type.MISS;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("SpyroActivity", this.getActivity());
        tag.putInt("SpyroFire", this.getSpyroFire());
        tag.putInt("StinkySkin", this.getSkin());
        // Exposes the tame flag to the loot-table NBT predicate for the
        // tamed-only beef drop (orig Stinky.java:257-266) — same convention
        // as Gazelle/Camarasaurus.
        tag.putBoolean("OreSpawnTamed", this.isTame());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.activity = tag.getInt("SpyroActivity");
        this.setActivity(this.activity);
        this.setSpyroFire(tag.getInt("SpyroFire"));
        this.skinColor = tag.getInt("StinkySkin");
        this.setSkin(this.skinColor);
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return stack.is(Items.BEEF);
    }

    @Nullable
    @Override
    public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob otherParent) {
        return null;
    }

    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        return null;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SND_DUCK_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SND_CRYO_DEATH;
    }

    @Override
    protected float getSoundVolume() {
        return 0.6f;
    }

    /** orig Stinky.java:286-291 — daytime; at most 2 buddies within 20/10/20 (findBuddies, Stinky.java:705-708). */
    @Override
    public boolean checkSpawnRules(net.minecraft.world.level.LevelAccessor level,
                                   net.minecraft.world.entity.MobSpawnType spawnType) {
        if (!OriginalSpawnGates.isDaytime(level)) return false;
        return OriginalSpawnGates.countBuddies(this, level, EntityStinky.class, 20.0, 10.0, 20.0) <= 2;
    }
}
