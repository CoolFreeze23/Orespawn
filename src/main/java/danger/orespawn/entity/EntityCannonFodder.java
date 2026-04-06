package danger.orespawn.entity;

import java.util.Comparator;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
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
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

public class EntityCannonFodder extends TamableAnimal {
    private static final EntityDataAccessor<Integer> IS_ACTIVATED =
            SynchedEntityData.defineId(EntityCannonFodder.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> HAT_COLOR =
            SynchedEntityData.defineId(EntityCannonFodder.class, EntityDataSerializers.INT);

    String nameOne = null;
    String nameTwo = null;
    private int px = 0, py = 0, pz = 0;
    private final Comparator<Entity> localTargetSorter;

    public EntityCannonFodder(EntityType<? extends EntityCannonFodder> type, Level level) {
        super(type, level);
        this.localTargetSorter = Comparator.comparingDouble(this::distanceToSqr);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(IS_ACTIVATED, 0);
        builder.define(HAT_COLOR, 0);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0)
                .add(Attributes.MOVEMENT_SPEED, 0.25)
                .add(Attributes.ATTACK_DAMAGE, 4.0);
    }

    public int getHatColor() { return this.entityData.get(HAT_COLOR); }
    public void setHatColor(int c) { this.entityData.set(HAT_COLOR, c); }
    public int getIsActivated() { return this.entityData.get(IS_ACTIVATED); }
    public void setIsActivated(int a) { this.entityData.set(IS_ACTIVATED, a); }

    public void setStuff(int hc, int ia, String s1, String s2) {
        this.setHatColor(hc);
        this.setIsActivated(ia);
        this.nameOne = s1;
        this.nameTwo = s2;
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (stack.is(Items.GOLDEN_APPLE) && this.distanceToSqr(player) < 16.0) {
            this.setHatColor(1);
            if (this.nameOne == null) this.nameOne = player.getStringUUID();
            if (this.getIsActivated() == 0) this.setIsActivated(1);
            this.setTame(true, true);
            this.setOwnerUUID(player.getUUID());
            this.heal(this.getMaxHealth() - this.getHealth());
            if (!player.getAbilities().instabuild) stack.shrink(1);
            return InteractionResult.SUCCESS;
        }

        if (stack.is(Items.ENCHANTED_GOLDEN_APPLE) && this.distanceToSqr(player) < 16.0) {
            this.setHatColor(3);
            if (this.nameOne == null) this.nameOne = player.getStringUUID();
            if (this.getIsActivated() == 0) this.setIsActivated(1);
            this.setTame(true, true);
            this.setOwnerUUID(player.getUUID());
            this.heal(this.getMaxHealth() - this.getHealth());
            if (!player.getAbilities().instabuild) stack.shrink(1);
            return InteractionResult.SUCCESS;
        }

        if (this.getIsActivated() == 2 && this.distanceToSqr(player) < 16.0 && stack.isEmpty()) {
            if (this.isOrderedToSit()) {
                this.setOrderedToSit(false);
            } else {
                this.setOrderedToSit(true);
                this.px = (int) this.getX();
                this.py = (int) this.getY();
                this.pz = (int) this.getZ();
            }
            return InteractionResult.SUCCESS;
        }

        return super.mobInteract(player, hand);
    }

    private boolean isSuitableTarget(LivingEntity target) {
        if (this.level().getDifficulty() == Difficulty.PEACEFUL) return false;
        if (target == null || target == this || !target.isAlive()) return false;

        if (this.isOrderedToSit()) {
            double dx2 = this.px - target.getX();
            double dy2 = this.py - target.getY();
            double dz2 = this.pz - target.getZ();
            if (dx2 * dx2 + dy2 * dy2 + dz2 * dz2 > 144.0) return false;
        }
        if (target instanceof Monster) return true;
        if (target instanceof EntityCannonFodder cf) {
            int c = cf.getHatColor();
            return c != 0 && c != this.getHatColor();
        }
        if (target instanceof Player p) {
            if (p.getAbilities().instabuild) return false;
            if (this.nameOne != null && this.nameOne.equals(p.getStringUUID())) return false;
            if (this.nameTwo != null && this.nameTwo.equals(p.getStringUUID())) return false;
            return true;
        }
        return false;
    }

    private LivingEntity findSomethingToAttack() {
        List<LivingEntity> entities = this.level().getEntitiesOfClass(LivingEntity.class,
                this.getBoundingBox().inflate(10.0, 4.0, 10.0));
        entities.sort(this.localTargetSorter);
        for (LivingEntity e : entities) {
            if (this.isSuitableTarget(e)) return e;
        }
        return null;
    }

    @Override
    protected void customServerAiStep() {
        if (this.isRemoved()) return;
        super.customServerAiStep();
        if (this.random.nextInt(200) == 1) this.setTarget(null);
        if (this.getIsActivated() != 2) return;

        if (this.level().getDifficulty() != Difficulty.PEACEFUL && this.random.nextInt(5) == 1) {
            LivingEntity e = this.findSomethingToAttack();
            if (e != null) {
                this.getNavigation().moveTo(e, 1.25);
                if (this.distanceToSqr(e) < 9.0 && (this.random.nextInt(8) == 0 || this.random.nextInt(7) == 1)) {
                    e.hurt(this.damageSources().mobAttack(this), 4.0f);
                }
            } else if (this.isOrderedToSit()) {
                this.getNavigation().moveTo(this.px, this.py, this.pz, 0.65);
            }
        }
        if (this.random.nextInt(250) == 1) this.heal(1.0f);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putString("NameOne", this.nameOne != null ? this.nameOne : "");
        tag.putString("NameTwo", this.nameTwo != null ? this.nameTwo : "");
        tag.putInt("IsActivated", this.getIsActivated());
        tag.putInt("HatColor", this.getHatColor());
        tag.putInt("PatrolX", this.px);
        tag.putInt("PatrolY", this.py);
        tag.putInt("PatrolZ", this.pz);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.nameOne = tag.getString("NameOne");
        if (this.nameOne.isEmpty()) this.nameOne = null;
        this.nameTwo = tag.getString("NameTwo");
        if (this.nameTwo.isEmpty()) this.nameTwo = null;
        this.setIsActivated(tag.getInt("IsActivated"));
        this.setHatColor(tag.getInt("HatColor"));
        this.px = tag.getInt("PatrolX");
        this.py = tag.getInt("PatrolY");
        this.pz = tag.getInt("PatrolZ");
        if (this.nameOne != null) {
            this.setTame(true, false);
        }
    }

    @Override
    public boolean isFood(ItemStack stack) { return false; }

    @Nullable
    @Override
    public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob otherParent) { return null; }
}
