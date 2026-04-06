package danger.orespawn.entity;

import java.util.Comparator;
import java.util.List;
import danger.orespawn.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Skeleton;
import net.minecraft.world.entity.monster.Spider;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class Godzilla extends Monster {
    private static final EntityDataAccessor<Integer> DATA_ATTACKING =
            SynchedEntityData.defineId(Godzilla.class, EntityDataSerializers.INT);

    private final Comparator<Entity> targetSorter;
    private final float moveSpeed = 0.75f;
    private int hurtTimer = 0;
    private int jumped = 0;
    private int jumpTimer = 0;
    private int ticker = 0;
    private int streamCount = 8;
    private int largeUnknownDetected = 0;

    public Godzilla(EntityType<? extends Godzilla> type, Level level) {
        super(type, level);
        this.xpReward = 10000;
        this.targetSorter = Comparator.comparingDouble(this::distanceToSqr);
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
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 6000.0)
                .add(Attributes.MOVEMENT_SPEED, 0.75)
                .add(Attributes.ATTACK_DAMAGE, 150.0)
                .add(Attributes.FOLLOW_RANGE, 64.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0)
                .add(Attributes.STEP_HEIGHT, 12.0);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_ATTACKING, 0);
    }

    @Override
    public boolean removeWhenFarAway(double dist) {
        return false;
    }

    @Override
    public void tick() {
        this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(this.moveSpeed);
        super.tick();
        if (this.onGround()) {
            this.getNavigation().stop();
        }
    }

    public int mygetMaxHealth() {
        return 6000;
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
        if (this.getRandom().nextInt(5) == 0) {
            return SoundEvents.ENDER_DRAGON_GROWL;
        }
        return null;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.ENDER_DRAGON_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.ENDER_DRAGON_DEATH;
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

    public final int getAttacking() {
        return this.entityData.get(DATA_ATTACKING);
    }

    public final void setAttacking(int value) {
        this.entityData.set(DATA_ATTACKING, value);
    }

    // ---- Enchantment helper ----

    private void enchantItem(ItemStack stack, ResourceKey<Enchantment> key, int enchLevel) {
        this.level().registryAccess()
                .lookup(Registries.ENCHANTMENT)
                .flatMap(reg -> reg.get(key))
                .ifPresent(holder -> stack.enchant(holder, enchLevel));
    }

    // ---- Item drop helpers ----

    private ItemStack dropItemRand(ItemStack stack) {
        double ox = this.getX() + this.getRandom().nextInt(10) - this.getRandom().nextInt(10);
        double oy = this.getY() + 4.0 + this.getRandom().nextInt(10);
        double oz = this.getZ() + this.getRandom().nextInt(10) - this.getRandom().nextInt(10);
        ItemEntity itemEntity = new ItemEntity(this.level(), ox, oy, oz, stack);
        this.level().addFreshEntity(itemEntity);
        return stack;
    }

    private ItemStack dropItemRandAt(ItemStack stack, double dx, double dz) {
        double ox = dx + this.getRandom().nextInt(10) - this.getRandom().nextInt(10);
        double oy = this.getY() + 4.0 + this.getRandom().nextInt(6);
        double oz = dz + this.getRandom().nextInt(10) - this.getRandom().nextInt(10);
        ItemEntity itemEntity = new ItemEntity(this.level(), ox, oy, oz, stack);
        this.level().addFreshEntity(itemEntity);
        return stack;
    }

    // ---- Jump mechanics ----

    @Override
    protected void jumpFromGround() {
        while (this.getYRot() < 0.0f) this.setYRot(this.getYRot() + 360.0f);
        while (this.yHeadRot < 0.0f) this.yHeadRot += 360.0f;
        while (this.getYRot() > 360.0f) this.setYRot(this.getYRot() - 360.0f);
        while (this.yHeadRot > 360.0f) this.yHeadRot -= 360.0f;

        Vec3 dm = this.getDeltaMovement();
        float f = 0.2f + Math.abs(this.getRandom().nextFloat() * 0.45f);
        double mx = dm.x + f * Math.cos(Math.toRadians(this.yHeadRot + 90.0f));
        double mz = dm.z + f * Math.sin(Math.toRadians(this.yHeadRot + 90.0f));
        this.setDeltaMovement(mx, dm.y + 0.45, mz);
        this.getNavigation().stop();
    }

    private void jumpAtEntity(LivingEntity e) {
        Vec3 dm = this.getDeltaMovement();
        double d1 = e.getX() - this.getX();
        double d2 = e.getZ() - this.getZ();
        float angle = (float) Math.atan2(d2, d1);
        double dist = Math.sqrt(d1 * d1 + d2 * d2);
        this.setDeltaMovement(
                dm.x + dist * 0.05 * Math.cos(angle),
                dm.y + 1.25,
                dm.z + dist * 0.05 * Math.sin(angle)
        );
        this.getNavigation().stop();
    }

    // ---- Distance helpers ----

    private double getHorizontalDistanceSqToEntity(Entity e) {
        double dx = e.getX() - this.getX();
        double dz = e.getZ() - this.getZ();
        return dx * dx + dz * dz;
    }

    private double myGetDistanceSqToEntity(Entity e) {
        double d0 = this.getX() - e.getX();
        double d1 = e.getY() - this.getY();
        double d2 = this.getZ() - e.getZ();
        if (d1 > 0.0 && d1 < 20.0) d1 = 0.0;
        if (d1 > 20.0) d1 -= 10.0;
        return d0 * d0 + d1 * d1 + d2 * d2;
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
        // TODO: Also exclude OreSpawn custom blocks (amethyst block, ruby block, uranium block, titanium block, crystal stone, crystal grass)
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
            // TODO: skip GodzillaHead, Ghost, GhostSkelly

            target.hurt(this.damageSources().mobAttack(this), (float) damage / 2.0f);
            target.hurt(this.damageSources().genericKill(), (float) damage / 2.0f);
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

    private void fireCannon(LivingEntity e) {
        // TODO: Implement BetterFireball projectile for fire cannon attack.
        // Original fires a stream of fireballs (1 aimed + 5 spread) from mouth position
        // at yoff=19, xzoff=22, offset from head rotation angle.
        // Each volley decrements streamCount; resets to 8 every 100 ticks.
        if (this.streamCount > 0) {
            this.streamCount--;
        }
    }

    // ---- Combat: lightning attack ----

    private void doLightningAttack(LivingEntity e) {
        if (e == null) return;
        e.hurt(this.damageSources().mobAttack(this), 100.0f);
        e.igniteForSeconds(5);

        if (this.level() instanceof ServerLevel serverLevel) {
            boolean griefing = serverLevel.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING);
            serverLevel.explode(this, e.getX(), e.getY(), e.getZ(), 3.0f,
                    griefing ? Level.ExplosionInteraction.MOB : Level.ExplosionInteraction.NONE);

            LightningBolt bolt1 = new LightningBolt(EntityType.LIGHTNING_BOLT, serverLevel);
            bolt1.moveTo(e.getX(), e.getY() + 1.0, e.getZ());
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
        // TODO: if (target instanceof GodzillaHead) return false;
        if (target instanceof Creeper) return false;
        if (target instanceof Zombie) return false;
        if (target instanceof Spider) return false;
        if (target instanceof Skeleton) return false;
        // TODO: if (target instanceof Ghost) return false;
        // TODO: if (target instanceof GhostSkelly) return false;
        if (target instanceof Player player) {
            if (player.getAbilities().instabuild) return false;
        }
        return true;
    }

    private LivingEntity findSomethingToAttack() {
        AABB searchBox = this.getBoundingBox().inflate(64.0, 40.0, 64.0);
        List<LivingEntity> entities = this.level().getEntitiesOfClass(LivingEntity.class, searchBox);
        entities.sort(this.targetSorter);
        LivingEntity ret = null;
        boolean villagerFound = false;
        for (LivingEntity entity : entities) {
            // TODO: track GodzillaHead presence for head_found flag
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
    protected void customServerAiStep() {
        if (this.isRemoved()) return;
        if (this.level().isClientSide) return;
        super.customServerAiStep();

        ++this.ticker;
        if (this.ticker > 30000) this.ticker = 0;
        if (this.ticker % 100 == 0) this.streamCount = 8;
        if (this.hurtTimer > 0) --this.hurtTimer;
        if (this.jumpTimer > 0) --this.jumpTimer;

        if (this.getRandom().nextInt(200) == 0) {
            this.setTarget(null);
        }

        // --- Jump landing damage ---
        Vec3 dm = this.getDeltaMovement();
        if (dm.y < -0.95) this.jumped = 1;
        if (dm.y < -1.5) this.jumped = 2;
        if (this.jumped != 0 && dm.y > -0.1) {
            double df = this.jumped == 2 ? 1.5 : 1.0;
            this.doJumpDamage(this.getX(), this.getY(), this.getZ(), 10.0, 150.0 * df, 0);
            this.doJumpDamage(this.getX(), this.getY(), this.getZ(), 15.0, 75.0 * df, 0);
            this.doJumpDamage(this.getX(), this.getY(), this.getZ(), 25.0, 37.5 * df, 0);
            this.jumped = 0;
        }

        // --- Block crushing around self ---
        int xzrange = 12;
        if (this.getAttacking() != 0) xzrange = 16;
        int k = -3 + this.ticker % 30;
        crushBlocks(this.getX(), this.getZ(), xzrange, k);

        // --- Block crushing in front (head direction) ---
        double frontX = this.getX() + 16.0 * Math.sin(Math.toRadians(this.yHeadRot));
        double frontZ = this.getZ() - 16.0 * Math.cos(Math.toRadians(this.yHeadRot));
        int k2 = -3 + this.ticker % 12;
        crushBlocks(frontX, frontZ, xzrange, k2);

        if (k2 == 0) {
            this.doJumpDamage(frontX, this.getY(), frontZ, 15.0, 75.0, 1);
        }

        // --- Target acquisition and combat ---
        if (this.getRandom().nextInt(Math.max(1, 5 - this.largeUnknownDetected)) == 1) {
            LivingEntity e = this.getTarget();
            if (e != null) {
                if (!e.isAlive()) {
                    this.setTarget(null);
                    e = null;
                } else if (e instanceof Godzilla) {
                    // TODO: also check GodzillaHead
                    this.setTarget(null);
                    e = null;
                }
            }
            if (e == null) {
                e = this.findSomethingToAttack();
                // TODO: spawn GodzillaHead companion if not already present
            }

            if (e != null) {
                this.lookAt(e, 10.0f, 10.0f);

                if (this.getRandom().nextInt(65) == 1 && this.myGetDistanceSqToEntity(e) > 300.0) {
                    this.doLightningAttack(e);

                } else if (this.getRandom().nextInt(Math.max(1, 20 - this.largeUnknownDetected * 5)) == 1
                        && this.jumpTimer == 0) {
                    this.jumpAtEntity(e);
                    this.jumpTimer = 30;

                } else if (this.myGetDistanceSqToEntity(e)
                        < (double) (300.0f + e.getBbWidth() / 2.0f * (e.getBbWidth() / 2.0f))) {
                    this.setAttacking(1);
                    this.getNavigation().moveTo(e, 1.0);
                    if (this.getRandom().nextInt(Math.max(1, 4 - this.largeUnknownDetected)) == 0
                            || this.getRandom().nextInt(Math.max(1, 3 - this.largeUnknownDetected)) == 1) {
                        this.doHurtTarget(e);
                    }

                } else {
                    this.getNavigation().moveTo(e, 1.0);
                    if (this.getHorizontalDistanceSqToEntity(e) > 625.0) {
                        if (this.streamCount > 0) {
                            this.setAttacking(1);
                            double rr = Math.atan2(e.getZ() - this.getZ(), e.getX() - this.getX());
                            double rhdir = Math.toRadians((this.yHeadRot + 90.0f) % 360.0f);
                            double rdd = Math.abs(rr - rhdir) % (Math.PI * 2.0);
                            if (rdd > Math.PI) rdd -= Math.PI * 2.0;
                            if (Math.abs(rdd) < 0.5) {
                                this.fireCannon(e);
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
            float s = living.getBbHeight() * living.getBbWidth();
            if (s > 30.0f && !(target instanceof Godzilla)) {
                // TODO: also skip GodzillaHead, PitchBlack, Kraken, check isRoyalty
                living.setHealth(living.getHealth() / 2.0f);
                living.hurt(this.damageSources().mobAttack(this), 150.0f * 10.0f);
                this.largeUnknownDetected = 1;
            }
        }

        if (target instanceof net.minecraft.world.entity.boss.enderdragon.EnderDragon dragon) {
            // TODO: implement part-specific EnderDragon damage
            dragon.hurt(this.damageSources().mobAttack(this), 75.0f);
        }

        if (super.doHurtTarget(target)) {
            if (target instanceof LivingEntity) {
                double ks = 3.2;
                double inair = 0.3;
                float angle = (float) Math.atan2(target.getZ() - this.getZ(), target.getX() - this.getX());
                if (target.isRemoved() || target instanceof Player) {
                    inair *= 2.0;
                }
                target.push(Math.cos(angle) * ks, inair, Math.sin(angle) * ks);
            }
            return true;
        }
        return false;
    }

    // ---- Incoming damage ----

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (this.hurtTimer > 0) return false;

        float dm = Math.min(amount, 750.0f);
        Entity attacker = source.getEntity();
        if (attacker instanceof LivingEntity living) {
            float s = living.getBbHeight() * living.getBbWidth();
            if (s > 30.0f && !(living instanceof Godzilla)) {
                // TODO: also skip GodzillaHead, PitchBlack, Kraken, check isRoyalty
                dm /= 10.0f;
                this.hurtTimer = 50;
                this.largeUnknownDetected = 1;
            }
        }

        if (source.getMsgId().equals("cactus")) return false;

        boolean ret = super.hurt(source, dm);
        this.hurtTimer = 20;

        attacker = source.getEntity();
        if (attacker instanceof LivingEntity living && !(attacker instanceof Godzilla)) {
            // TODO: also skip GodzillaHead
            this.setTarget(living);
            this.getNavigation().moveTo(living, 1.2);
        }
        return ret;
    }

    // ---- Death loot ----

    @Override
    protected void dropCustomDeathLoot(DamageSource source, int looting, boolean recentlyHit) {
        super.dropCustomDeathLoot(source, looting, recentlyHit);

        dropItemRand(new ItemStack(Items.NETHER_STAR, 1));

        int scaleCount = 50 + this.getRandom().nextInt(30);
        for (int i = 0; i < scaleCount; i++) {
            dropItemRand(new ItemStack(ModItems.GODZILLA_SCALE.get(), 1));
        }

        int emeraldCount = 100 + this.getRandom().nextInt(160);
        for (int i = 0; i < emeraldCount; i++) {
            dropItemRand(new ItemStack(Items.EMERALD, 1));
        }

        int xpBottleCount = 50 + this.getRandom().nextInt(60);
        for (int i = 0; i < xpBottleCount; i++) {
            dropItemRand(new ItemStack(Items.EXPERIENCE_BOTTLE, 1));
        }

        int bonusCount = 25 + this.getRandom().nextInt(15);
        for (int i = 0; i < bonusCount; i++) {
            dropLootByRoll(this.getRandom().nextInt(80));
        }
    }

    @SuppressWarnings("java:S1479")
    private void dropLootByRoll(int roll) {
        ItemStack is;
        switch (roll) {
            case 0 -> dropItemRand(new ItemStack(ModItems.ULTIMATE_SWORD.get()));
            case 1 -> dropItemRand(new ItemStack(Items.DIAMOND));
            case 2 -> dropItemRand(new ItemStack(Items.DIAMOND_BLOCK));
            case 3 -> { is = dropItemRand(new ItemStack(Items.IRON_SWORD)); enchantSword(is); }
            case 4 -> { is = dropItemRand(new ItemStack(Items.IRON_SHOVEL)); enchantTool(is); }
            case 5 -> { is = dropItemRand(new ItemStack(Items.IRON_PICKAXE)); enchantToolSilkTouch(is); }
            case 6 -> { is = dropItemRand(new ItemStack(Items.IRON_AXE)); enchantTool(is); }
            case 7 -> { is = dropItemRand(new ItemStack(Items.IRON_HOE)); enchantTool(is); }
            case 8 -> { is = dropItemRand(new ItemStack(Items.IRON_HELMET)); enchantHelmet(is); }
            case 9 -> { is = dropItemRand(new ItemStack(Items.IRON_CHESTPLATE)); enchantArmor(is); }
            case 10 -> { is = dropItemRand(new ItemStack(Items.IRON_LEGGINGS)); enchantArmor(is); }
            case 11 -> { is = dropItemRand(new ItemStack(Items.IRON_BOOTS)); enchantBoots(is); }
            case 12 -> dropItemRand(new ItemStack(ModItems.ULTIMATE_BOW.get()));
            case 13 -> dropItemRand(new ItemStack(ModItems.ULTIMATE_AXE.get()));
            case 14 -> dropItemRand(new ItemStack(Items.GOLD_INGOT));
            case 15 -> dropItemRand(new ItemStack(ModItems.ULTIMATE_PICKAXE.get()));
            case 16 -> { is = dropItemRand(new ItemStack(Items.GOLDEN_SWORD)); enchantSword(is); }
            case 17 -> { is = dropItemRand(new ItemStack(Items.GOLDEN_SHOVEL)); enchantTool(is); }
            case 18 -> { is = dropItemRand(new ItemStack(Items.GOLDEN_PICKAXE)); enchantToolSilkTouch(is); }
            case 19 -> { is = dropItemRand(new ItemStack(Items.GOLDEN_AXE)); enchantTool(is); }
            case 20 -> { is = dropItemRand(new ItemStack(Items.GOLDEN_HOE)); enchantTool(is); }
            case 21 -> { is = dropItemRand(new ItemStack(Items.GOLDEN_HELMET)); enchantHelmet(is); }
            case 22 -> { is = dropItemRand(new ItemStack(Items.GOLDEN_CHESTPLATE)); enchantArmor(is); }
            case 23 -> { is = dropItemRand(new ItemStack(Items.GOLDEN_LEGGINGS)); enchantArmor(is); }
            case 24 -> { is = dropItemRand(new ItemStack(Items.GOLDEN_BOOTS)); enchantBoots(is); }
            case 25 -> dropItemRand(new ItemStack(ModItems.ULTIMATE_SHOVEL.get()));
            case 26 -> dropItemRand(new ItemStack(Items.IRON_BLOCK));
            case 27 -> dropItemRand(new ItemStack(Items.ENDER_PEARL));
            case 28 -> dropItemRand(new ItemStack(Items.IRON_INGOT));
            case 29 -> dropItemRand(new ItemStack(Items.NAME_TAG));
            case 30 -> { is = dropItemRand(new ItemStack(Items.DIAMOND_SWORD)); enchantSword(is); }
            case 31 -> { is = dropItemRand(new ItemStack(Items.DIAMOND_SHOVEL)); enchantTool(is); }
            case 32 -> { is = dropItemRand(new ItemStack(Items.DIAMOND_PICKAXE)); enchantToolSilkTouch(is); }
            case 33 -> { is = dropItemRand(new ItemStack(Items.DIAMOND_AXE)); enchantTool(is); }
            case 34 -> { is = dropItemRand(new ItemStack(Items.DIAMOND_HOE)); enchantTool(is); }
            case 35 -> { is = dropItemRand(new ItemStack(Items.DIAMOND_HELMET)); enchantHelmet(is); }
            case 36 -> { is = dropItemRand(new ItemStack(Items.DIAMOND_CHESTPLATE)); enchantArmor(is); }
            case 37 -> { is = dropItemRand(new ItemStack(Items.DIAMOND_LEGGINGS)); enchantArmor(is); }
            case 38 -> { is = dropItemRand(new ItemStack(Items.DIAMOND_BOOTS)); enchantBoots(is); }
            case 39 -> dropItemRand(new ItemStack(Items.GOLDEN_APPLE));
            case 40 -> dropItemRand(new ItemStack(Items.GOLD_BLOCK));
            case 41 -> dropItemRand(new ItemStack(Items.ENCHANTED_GOLDEN_APPLE));
            case 42 -> { is = dropItemRand(new ItemStack(ModItems.EXPERIENCE_SWORD.get())); enchantSword(is); }
            // TODO: case 43 -> Experience Helmet + enchantHelmet (register ExperienceHelmet in ModItems)
            // TODO: case 44 -> Experience Chestplate + enchantArmor (register ExperienceBody in ModItems)
            // TODO: case 45 -> Experience Leggings + enchantArmor (register ExperienceLegs in ModItems)
            // TODO: case 46 -> Experience Boots + enchantBoots (register ExperienceBoots in ModItems)
            case 47 -> { is = dropItemRand(new ItemStack(ModItems.AMETHYST_SWORD.get())); enchantSword(is); }
            case 48 -> { is = dropItemRand(new ItemStack(ModItems.AMETHYST_SHOVEL.get())); enchantTool(is); }
            case 49 -> { is = dropItemRand(new ItemStack(ModItems.AMETHYST_PICKAXE.get())); enchantToolSilkTouch(is); }
            case 50 -> { is = dropItemRand(new ItemStack(ModItems.AMETHYST_AXE.get())); enchantTool(is); }
            case 51 -> { is = dropItemRand(new ItemStack(ModItems.AMETHYST_HOE.get())); enchantTool(is); }
            case 52 -> dropItemRand(new ItemStack(ModItems.BLOCK_AMETHYST_ITEM.get()));
            // TODO: case 53 -> Amethyst Helmet + enchantHelmet (register AmethystHelmet in ModItems)
            // TODO: case 54 -> Amethyst Chestplate + enchantArmor (register AmethystBody in ModItems)
            // TODO: case 55 -> Amethyst Leggings + enchantArmor (register AmethystLegs in ModItems)
            // TODO: case 56 -> Amethyst Boots + enchantBoots (register AmethystBoots in ModItems)
            // TODO: case 57 -> Ruby Helmet + enchantHelmet (register RubyHelmet in ModItems)
            // TODO: case 58 -> Ruby Chestplate + enchantArmor (register RubyBody in ModItems)
            // TODO: case 59 -> Ruby Leggings + enchantArmor (register RubyLegs in ModItems)
            // TODO: case 60 -> Ruby Boots + enchantBoots (register RubyBoots in ModItems)
            case 61 -> { is = dropItemRand(new ItemStack(ModItems.RUBY_SWORD.get())); enchantSword(is); }
            case 62 -> { is = dropItemRand(new ItemStack(ModItems.RUBY_SHOVEL.get())); enchantTool(is); }
            case 63 -> { is = dropItemRand(new ItemStack(ModItems.RUBY_PICKAXE.get())); enchantToolSilkTouch(is); }
            case 64 -> { is = dropItemRand(new ItemStack(ModItems.RUBY_AXE.get())); enchantTool(is); }
            case 65 -> { is = dropItemRand(new ItemStack(ModItems.RUBY_HOE.get())); enchantTool(is); }
            case 66 -> dropItemRand(new ItemStack(ModItems.BLOCK_RUBY_ITEM.get()));
            // TODO: case 67 -> Ultimate Helmet + enchantHelmet (register UltimateHelmet in ModItems)
            // TODO: case 68 -> Ultimate Chestplate + enchantArmor (register UltimateBody in ModItems)
            // TODO: case 69 -> Ultimate Leggings + enchantArmor (register UltimateLegs in ModItems)
            // TODO: case 70 -> Ultimate Boots + enchantBoots (register UltimateBoots in ModItems)
            case 71 -> { is = dropItemRand(new ItemStack(ModItems.ULTIMATE_SHOVEL.get())); enchantTool(is); }
            case 73 -> { is = dropItemRand(new ItemStack(ModItems.ULTIMATE_PICKAXE.get())); enchantToolSilkTouch(is); }
            case 74 -> { is = dropItemRand(new ItemStack(ModItems.ULTIMATE_AXE.get())); enchantTool(is); }
            case 75 -> { is = dropItemRand(new ItemStack(ModItems.ULTIMATE_HOE.get())); enchantTool(is); }
            default -> dropItemRand(new ItemStack(Items.EMERALD));
        }
    }

    // ---- Loot enchantment patterns ----

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

    private void enchantToolSilkTouch(ItemStack is) {
        if (this.getRandom().nextInt(2) == 1) enchantItem(is, Enchantments.UNBREAKING, 2 + this.getRandom().nextInt(4));
        if (this.getRandom().nextInt(6) == 1) enchantItem(is, Enchantments.EFFICIENCY, 1 + this.getRandom().nextInt(5));
        if (this.getRandom().nextInt(6) == 1) enchantItem(is, Enchantments.SILK_TOUCH, 1);
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
}
