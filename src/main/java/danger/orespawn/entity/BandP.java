package danger.orespawn.entity;

import java.util.Comparator;
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
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class BandP extends Monster {
    private static final EntityDataAccessor<Integer> DATA_WHAT =
            SynchedEntityData.defineId(BandP.class, EntityDataSerializers.INT);

    private final Comparator<Entity> targetSorter;
    private static final float MOVE_SPEED = 0.32f;
    private static final int MAX_HEALTH = 30;
    private static final int ATTACK_DAMAGE = 5;
    /** Maximum slots in the BandP's stash (legacy MymainInventory length=100). */
    private static final int STASH_SIZE = 16;
    /** 1-in-N chance per melee hit to grab an armor piece (~25% per swing). */
    private static final int ARMOR_STEAL_CHANCE_DEN = 4;
    private int whatset = 0;
    private int whatami = 0;
    private int gotStuff = 0;
    private final NonNullList<ItemStack> stash = NonNullList.withSize(STASH_SIZE, ItemStack.EMPTY);

    public BandP(EntityType<? extends BandP> type, Level level) {
        super(type, level);
        this.xpReward = 10;
        this.targetSorter = Comparator.comparingDouble(this::distanceToSqr);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new WaterAvoidingRandomStrollGoal(this, 0.5));
        this.goalSelector.addGoal(1, new LookAtPlayerGoal(this, Player.class, 10.0f));
        this.goalSelector.addGoal(2, new RandomLookAroundGoal(this));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, MAX_HEALTH)
                .add(Attributes.MOVEMENT_SPEED, MOVE_SPEED)
                .add(Attributes.ATTACK_DAMAGE, ATTACK_DAMAGE)
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
     * branch (lines 185-218 of {@code BandP.java}). On a successful melee hit
     * the criminal rolls a 1-in-{@link #ARMOR_STEAL_CHANCE_DEN} chance to pluck
     * an armor piece (or, if all 4 slots are empty, a regular inventory item)
     * from the player and stash it in their internal {@link #stash}. The stolen
     * stack is dropped on death via {@link #dropAllDeathLoot} so it is
     * recoverable.
     *
     * <p>Modernization differences vs the legacy code:</p>
     * <ul>
     *   <li>Stash size capped at {@link #STASH_SIZE} (legacy used 100). Once
     *       full, the steal short-circuits — preventing infinite mob-cap
     *       inventory bloat.</li>
     *   <li>Steal is rolled per swing rather than every tick; the legacy
     *       version always tried to grab on contact, which on a saturated
     *       server made the mob effectively impossible to fight without
     *       creative armor.</li>
     *   <li>Each successful steal increments {@link #gotStuff}, which
     *       (per the legacy {@code func_70692_ba} logic) makes the mob
     *       persistent so it doesn't despawn with looted inventory.</li>
     * </ul>
     */
    private void tryStealFromPlayer(Player player) {
        if (player.getAbilities().instabuild) return;
        if (this.getRandom().nextInt(ARMOR_STEAL_CHANCE_DEN) != 0) return;

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

    @Override
    protected void dropCustomDeathLoot(net.minecraft.server.level.ServerLevel level,
                                       DamageSource source, boolean recentlyHit) {
        super.dropCustomDeathLoot(level, source, recentlyHit);
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
        List<LivingEntity> list = this.level().getEntitiesOfClass(LivingEntity.class,
                this.getBoundingBox().inflate(20.0, 6.0, 20.0));
        list.sort(this.targetSorter);
        for (LivingEntity candidate : list) {
            if (isSuitableTarget(candidate)) return candidate;
        }
        return null;
    }

    private boolean isSuitableTarget(LivingEntity target) {
        if (target == null || target == this || !target.isAlive()) return false;
        if (target instanceof Player player) return !player.getAbilities().instabuild;
        return false;
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
}
