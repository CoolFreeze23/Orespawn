package danger.orespawn.entity;

import danger.orespawn.MobStats;
import danger.orespawn.ModItems;
import danger.orespawn.OreSpawnConfig;
import danger.orespawn.entity.ai.GenericTargetSorter;
import danger.orespawn.entity.ai.MoveIndoorsGoal;

import java.util.List;

import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MoveThroughVillageGoal;
import net.minecraft.world.entity.ai.goal.OpenDoorGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class BandP extends Monster {
    private static final EntityDataAccessor<Integer> DATA_WHAT =
            SynchedEntityData.defineId(BandP.class, EntityDataSerializers.INT);

    private final GenericTargetSorter targetSorter;
    private static final float MOVE_SPEED = 0.32f;
    /** orig BandP.java:46 — MymainInventory is 100 slots. */
    private static final int STASH_SIZE = 100;
    private int whatset = 0;
    private int whatami = 0;
    private int gotStuff = 0;
    private final NonNullList<ItemStack> stash = NonNullList.withSize(STASH_SIZE, ItemStack.EMPTY);

    public BandP(EntityType<? extends BandP> type, Level level) {
        super(type, level);
        // orig BandP.java:53 — experienceValue = 1000.
        this.xpReward = 1000;
        // TF-035: orig BandP.java:42,55 — targets sort with GenericTargetSorter
        // (creeper-halved / big-silhouette-first), not plain distance.
        this.targetSorter = new GenericTargetSorter(this);
        // Needed for the OpenDoorGoal (orig BandP.java:60) to path through doors.
        if (this.getNavigation() instanceof GroundPathNavigation groundNav) {
            groundNav.setCanOpenDoors(true);
        }
    }

    @Override
    protected void registerGoals() {
        // orig BandP.java:56 — EntityAIMoveThroughVillage(0.5, false) at
        // priority 0. Vanilla MoveThroughVillageGoal is the same goal line;
        // 1.14+ villages are POI clusters (beds/workstations) rather than
        // door lists, so it engages only where village POI exists nearby —
        // the honest modern mapping (documented per the MoveIndoorsGoal
        // pattern). Door tolerance mirrors the OpenDoor goal below.
        this.goalSelector.addGoal(0, new MoveThroughVillageGoal(this, 0.5, false, 4, () -> true));
        // orig :57 — MyEntityAIWanderALot(16, 0.5).
        this.goalSelector.addGoal(1, new WaterAvoidingRandomStrollGoal(this, 0.5));
        this.goalSelector.addGoal(2, new LookAtPlayerGoal(this, Player.class, 10.0f)); // orig :58
        this.goalSelector.addGoal(3, new RandomLookAroundGoal(this)); // orig :59
        this.goalSelector.addGoal(4, new OpenDoorGoal(this, true)); // orig :60
        // orig :61 — EntityAIMoveIndoors; 1.21.1 roofed-shelter behavioral
        // match, see MoveIndoorsGoal Javadoc.
        this.goalSelector.addGoal(5, new MoveIndoorsGoal(this));
    }

    public static AttributeSupplier.Builder createAttributes() {
        // orig OreSpawnMain.java:6480 — BandP 100 HP / 1 ATK / 18 armor
        // (orig BandP.java:68,94,98 consumes BandP_stats directly).
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, MobStats.BANDP.maxHealth())
                .add(Attributes.MOVEMENT_SPEED, MOVE_SPEED)
                .add(Attributes.ATTACK_DAMAGE, MobStats.BANDP.attackDamage())
                .add(Attributes.ARMOR, MobStats.BANDP.armor())
                .add(Attributes.FOLLOW_RANGE, 20.0);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_WHAT, 0);
    }

    public int getWhat() {
        return this.entityData.get(DATA_WHAT);
    }

    public void setWhat(int value) {
        this.entityData.set(DATA_WHAT, value);
    }

    @Override
    public boolean removeWhenFarAway(double dist) {
        if (this.isPersistenceRequired()) return false;
        return this.gotStuff == 0;
    }

    @Override
    public void tick() {
        this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(MOVE_SPEED);
        super.tick();
        if (!this.level().isClientSide && this.whatset == 0) {
            this.whatset = 1;
            this.whatami = this.getRandom().nextInt(2);
            this.setWhat(this.whatami);
        }
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.VILLAGER_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.VILLAGER_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.VILLAGER_DEATH;
    }

    @Override
    protected float getSoundVolume() {
        return 1.5f;
    }

    @Override
    protected void customServerAiStep() {
        if (this.isRemoved()) return;
        super.customServerAiStep();

        if (this.getRandom().nextInt(12) == 1) {
            LivingEntity target = findSomethingToAttack();
            if (target != null) {
                this.lookAt(target, 10.0f, 10.0f);
                if (this.distanceToSqr(target) < 9.0) {
                    if (this.doHurtTarget(target) && target instanceof Player player) {
                        tryStealFromPlayer(player);
                    }
                } else {
                    this.getNavigation().moveTo(target, 1.25);
                }
            }
        }
    }

    /**
     * Direct port of the legacy 1.7.10 {@code BandP#func_70619_bc} armor-steal
     * branch (orig BandP.java:185-218): every successful melee hit steals one
     * item — armor first (helmet→boots), else the last non-empty inventory
     * slot — into the 100-slot {@link #stash}. Stolen stacks drop on death.
     * Each steal increments {@link #gotStuff}, which (per the legacy
     * {@code func_70692_ba} logic) blocks despawning while loot is held.
     */
    private void tryStealFromPlayer(Player player) {
        if (player.getAbilities().instabuild) return;

        int freeSlot = -1;
        for (int i = 0; i < this.stash.size(); i++) {
            if (this.stash.get(i).isEmpty()) {
                freeSlot = i;
                break;
            }
        }
        if (freeSlot < 0) return;

        Inventory inv = player.getInventory();
        // Try armor first (matches legacy preference order).
        for (EquipmentSlot slot : new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST,
                EquipmentSlot.LEGS, EquipmentSlot.FEET}) {
            ItemStack worn = player.getItemBySlot(slot);
            if (!worn.isEmpty()) {
                this.stash.set(freeSlot, worn.copy());
                player.setItemSlot(slot, ItemStack.EMPTY);
                this.gotStuff++;
                return;
            }
        }
        // Fall through to a random non-empty inventory slot.
        for (int i = inv.items.size() - 1; i >= 0; i--) {
            ItemStack candidate = inv.items.get(i);
            if (!candidate.isEmpty()) {
                this.stash.set(freeSlot, candidate.copy());
                inv.items.set(i, ItemStack.EMPTY);
                this.gotStuff++;
                return;
            }
        }
    }

    // EXCEPTION to the loot-table architecture (see phase_b_reports/B1_drops.md):
    // the stolen-item stash is runtime state and the bear/panda variant gate
    // reads synched entity data — neither is expressible in a loot JSON.
    // The unconditional emerald drop (orig BandP.java:148-151) lives in
    // loot_table/entities/band_p.json.
    @Override
    protected void dropCustomDeathLoot(net.minecraft.server.level.ServerLevel level,
                                       DamageSource source, boolean recentlyHit) {
        super.dropCustomDeathLoot(level, source, recentlyHit);
        // orig BandP.java:152-158 — bear variant (getWhat()==0) drops 2-4
        // paired uranium + titanium nuggets.
        if (this.getWhat() == 0) {
            int pairs = 2 + this.getRandom().nextInt(3);
            for (int i = 0; i < pairs; i++) {
                this.spawnAtLocation(ModItems.URANIUM_NUGGET.get());
                this.spawnAtLocation(ModItems.TITANIUM_NUGGET.get());
            }
        }
        // orig BandP.java:159-164 — drops everything it has stolen.
        for (ItemStack stolen : this.stash) {
            if (stolen.isEmpty()) continue;
            ItemEntity drop = new ItemEntity(level,
                    this.getX(), this.getY() + 0.5, this.getZ(), stolen);
            drop.setDefaultPickUpDelay();
            level.addFreshEntity(drop);
        }
        this.stash.clear();
    }

    private LivingEntity findSomethingToAttack() {
        // orig BandP.java:253-255 — PlayNicely disables aggression entirely.
        if (OreSpawnConfig.PLAY_NICELY.get()) return null;
        List<LivingEntity> list = this.level().getEntitiesOfClass(LivingEntity.class,
                this.getBoundingBox().inflate(20.0, 6.0, 20.0));
        list.sort(this.targetSorter);
        for (LivingEntity candidate : list) {
            if (isSuitableTarget(candidate)) return candidate;
        }
        return null;
    }

    /**
     * orig BandP.java:226-250 — line of sight required; fair game are
     * non-creative players, villagers, and the Girlfriend/Boyfriend
     * companions; everything else is ignored.
     */
    private boolean isSuitableTarget(LivingEntity target) {
        if (target == null || target == this || !target.isAlive()) return false;
        if (!this.getSensing().hasLineOfSight(target)) return false; // orig :236-238
        if (target instanceof Player player) return !player.getAbilities().instabuild;
        if (target instanceof Villager) return true; // orig :243-245
        if (target instanceof Girlfriend) return true; // orig :246-248
        return target instanceof Boyfriend; // orig :249
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("GotStuff", this.gotStuff);
        ListTag stashTag = new ListTag();
        for (ItemStack stack : this.stash) {
            if (stack.isEmpty()) continue;
            stashTag.add(stack.save(this.registryAccess()));
        }
        tag.put("Stash", stashTag);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.gotStuff = tag.getInt("GotStuff");
        this.stash.clear();
        if (tag.contains("Stash", Tag.TAG_LIST)) {
            ListTag list = tag.getList("Stash", Tag.TAG_COMPOUND);
            for (int i = 0; i < list.size() && i < this.stash.size(); i++) {
                ItemStack.parse(this.registryAccess(), list.getCompound(i))
                        .ifPresent(s -> this.stash.set(findFirstEmptyStashSlot(), s));
            }
        }
    }

    private int findFirstEmptyStashSlot() {
        for (int i = 0; i < this.stash.size(); i++) {
            if (this.stash.get(i).isEmpty()) return i;
        }
        return this.stash.size() - 1;
    }

    /** orig BandP.java:278-309 — "Criminal" spawner bypass; daytime; y>=100 (the y>=50 check is shadowed); no other BandP within 32/12/32; requires a villager within 36/12/36. */
    @Override
    public boolean checkSpawnRules(net.minecraft.world.level.LevelAccessor level,
                                   net.minecraft.world.entity.MobSpawnType spawnType) {
        if (OriginalSpawnGates.nearOwnSpawner(this, level)) return true;
        if (!OriginalSpawnGates.isDaytime(level)) return false;
        if (this.getY() < 50.0) return false;
        if (this.getY() < 100.0) return false;
        if (OriginalSpawnGates.anyOtherNearby(this, level, BandP.class, 32.0, 12.0, 32.0)) return false;
        return !level.getEntitiesOfClass(net.minecraft.world.entity.npc.Villager.class,
                this.getBoundingBox().inflate(36.0, 12.0, 36.0)).isEmpty();
    }
}
