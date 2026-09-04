package danger.orespawn.entity;

import danger.orespawn.ModEntities;
import danger.orespawn.ModItems;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
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
import danger.orespawn.entity.ai.TargetSelection;

public class EntityCannonFodder extends TamableAnimal {
    private static final EntityDataAccessor<Integer> IS_ACTIVATED =
            SynchedEntityData.defineId(EntityCannonFodder.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> HAT_COLOR =
            SynchedEntityData.defineId(EntityCannonFodder.class, EntityDataSerializers.INT);

    String trustedPlayerUuidPrimary = null;
    String trustedPlayerUuidSecondary = null;
    private int patrolBlockX = 0;
    private int patrolBlockY = 0;
    private int patrolBlockZ = 0;
    private final Comparator<Entity> localTargetSorter;

    public EntityCannonFodder(EntityType<? extends EntityCannonFodder> type, Level level) {
        super(type, level);
        // orig EntityCannonFodder.java:42 — targets rank by GenericTargetSorter (creepers
        // and large silhouettes outrank nearer small targets), not by raw distance.
        this.localTargetSorter = new danger.orespawn.entity.ai.GenericTargetSorter(this);
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

    public void setStuff(int hatColor, int activated, String trustedUuidPrimary, String trustedUuidSecondary) {
        this.setHatColor(hatColor);
        this.setIsActivated(activated);
        this.trustedPlayerUuidPrimary = trustedUuidPrimary;
        this.trustedPlayerUuidSecondary = trustedUuidSecondary;
        this.setPersistenceRequired(); // orig EntityCannonFodder.java:221 func_110163_bv
    }

    // orig EntityCannonFodder.java:77-203 — the full fodder interaction chain, in orig
    // order: vanilla first, then repeat-interaction ownership promotion, then the three
    // hat foods (carrot=1, quinoa=2, potato=3), corncob cloning, and the sit-toggle.
    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        // orig :83-85 — vanilla EntityTameable interaction is consulted FIRST and wins.
        InteractionResult vanillaResult = super.mobInteract(player, hand);
        if (vanillaResult.consumesAction()) return vanillaResult;

        // orig :86-106 — interacting with an already-named, tamed fodder promotes it to
        // is_activated=2 (combat-ready) and rotates the two trusted-name slots. Orig bug
        // kept: while name_two is still empty, ANY player's click takes over slot one
        // (orig :100-105); only a fodder with both slots filled blocks strangers (orig :95).
        if (this.trustedPlayerUuidPrimary != null && this.isTame()) {
            if (this.trustedPlayerUuidPrimary.equals(player.getStringUUID())) {
                if (this.trustedPlayerUuidSecondary == null) {
                    // orig :88-93 — the owner clicking again fills both slots with
                    // themselves and arms the fodder.
                    this.trustedPlayerUuidSecondary = this.trustedPlayerUuidPrimary;
                    this.trustedPlayerUuidPrimary = player.getStringUUID();
                    this.setOwnerUUID(player.getUUID()); // orig :91 func_152115_b(name_one)
                    this.setIsActivated(2);
                }
            } else if (this.trustedPlayerUuidSecondary != null) {
                if (!this.trustedPlayerUuidSecondary.equals(player.getStringUUID())) {
                    // orig :95 — a stranger's interaction is eaten with no effect.
                    return InteractionResult.SUCCESS;
                }
                // orig :96-99 — the slot-two player takes over slot one (and ownership).
                this.trustedPlayerUuidSecondary = this.trustedPlayerUuidPrimary;
                this.trustedPlayerUuidPrimary = player.getStringUUID();
                this.setOwnerUUID(player.getUUID()); // orig :98
                this.setIsActivated(2);
            } else {
                // orig :100-105 — a second player fills slot one; the old owner shifts
                // to slot two.
                this.trustedPlayerUuidSecondary = this.trustedPlayerUuidPrimary;
                this.trustedPlayerUuidPrimary = player.getStringUUID();
                this.setOwnerUUID(player.getUUID()); // orig :103
                this.setIsActivated(2);
            }
        }

        // orig :107-125 — CARROT (Items.field_151172_bF) = team hat 1
        if (stack.is(Items.CARROT) && this.distanceToSqr(player) < 16.0) {
            return this.applyHatFood(player, stack, 1);
        }
        // orig :126-144 — POTATO (Items.field_151174_bG) = team hat 3
        if (stack.is(Items.POTATO) && this.distanceToSqr(player) < 16.0) {
            return this.applyHatFood(player, stack, 3);
        }
        // orig :145-163 — MyQuinoa = team hat 2
        if (stack.is(ModItems.QUINOA.get()) && this.distanceToSqr(player) < 16.0) {
            return this.applyHatFood(player, stack, 2);
        }

        // orig :164-189 — corncob cloning: a fully-activated (2) fodder fed MyCornCob
        // spawns a fresh member of the same species with hats/names/owner copied over.
        if (this.getIsActivated() == 2 && stack.is(ModItems.CORN_COB.get())
                && this.distanceToSqr(player) < 16.0) {
            // orig :166-175 — species table keyed by instanceof: default "Ostrich",
            // then Lizard / Chipmunk / "Velocity Raptor". The port's Lizard and
            // VelocityRaptor do not (yet) extend EntityCannonFodder, so exact-type
            // checks stand in for instanceof — equivalent, since no orig fodder
            // subclass has subclasses of its own.
            EntityType<?> species = ModEntities.OSTRICH.get(); // orig :166 default
            if (this.getType() == ModEntities.LIZARD.get()) species = ModEntities.LIZARD.get(); // orig :167-169
            if (this instanceof Chipmunk) species = ModEntities.CHIPMUNK.get(); // orig :170-172
            if (this.getType() == ModEntities.VELOCITY_RAPTOR.get()) species = ModEntities.VELOCITY_RAPTOR.get(); // orig :173-175
            if (!this.level().isClientSide) { // orig :176
                Entity newborn = species.create(this.level());
                if (newborn != null) {
                    // orig :176 + spawnCreature :205-214 — offset by world-random floats
                    // on x/z, +0.01 on y, random yaw, pitch 0.
                    newborn.moveTo(this.getX() + this.level().random.nextFloat(),
                            this.getY() + 0.01,
                            this.getZ() + this.level().random.nextFloat(),
                            this.level().random.nextFloat() * 360.0f, 0.0f);
                    this.level().addFreshEntity(newborn); // orig :210
                    if (newborn instanceof Mob newbornMob) newbornMob.playAmbientSound(); // orig :211 func_70642_aH
                    // orig :177-181 casts unconditionally to EntityCannonFodder; guarded
                    // here so a not-yet-re-parented Ostrich/Lizard/VelocityRaptor spawn
                    // still succeeds (the stuff-copy self-heals once they re-parent).
                    if (newborn instanceof EntityCannonFodder newbornFodder) {
                        newbornFodder.setOwnerUUID(this.getOwnerUUID()); // orig :178 func_152115_b(func_152113_b())
                        newbornFodder.setTame(true, true); // orig :179 func_70903_f(true)
                        newbornFodder.setStuff(this.getHatColor(), this.getIsActivated(),
                                this.trustedPlayerUuidPrimary, this.trustedPlayerUuidSecondary); // orig :180
                    }
                }
            }
            this.level().broadcastEntityEvent(this, (byte) 7); // orig :182 func_70908_e(true) hearts
            // orig :183 — "random.explode" played at the PLAYER, volume 0.75f, pitch 2.0f.
            this.level().playSound(player, player.blockPosition(), SoundEvents.GENERIC_EXPLODE.value(),
                    SoundSource.NEUTRAL, 0.75f, 2.0f);
            if (!player.getAbilities().instabuild) stack.shrink(1); // orig :184-188
            return InteractionResult.SUCCESS;
        }

        // orig :190-202 — fallthrough: a combat-ready fodder toggles sit/patrol on ANY
        // other interaction; the orig has no empty-hand requirement.
        if (this.getIsActivated() != 2 || !(this.distanceToSqr(player) < 16.0)) {
            return InteractionResult.PASS; // orig :190 returns false
        }
        if (this.isOrderedToSit()) {
            this.setOrderedToSit(false);
            this.level().broadcastEntityEvent(this, (byte) 7); // orig :193 — hearts on standing up
        } else {
            this.setOrderedToSit(true);
            this.level().broadcastEntityEvent(this, (byte) 6); // orig :197 func_70908_e(false) — smoke on sitting
            // orig :198-200 — the sit spot becomes the 12-block patrol anchor enforced
            // in isSuitableTarget.
            this.patrolBlockX = (int) this.getX();
            this.patrolBlockY = (int) this.getY();
            this.patrolBlockZ = (int) this.getZ();
        }
        return InteractionResult.SUCCESS; // orig :194,:202
    }

    // orig :107-125 / :126-144 / :145-163 — the three hat-food blocks are verbatim
    // copies of one another in the orig except for the hat id, so they share one body
    // here: set the team hat, claim slot one if empty, bump activation 0->1, tame to
    // the SLOT-ONE name (orig :116 func_152115_b(name_one) — NOT necessarily the
    // feeding player), hearts, full heal, persistence, consume one unless creative.
    private InteractionResult applyHatFood(Player player, ItemStack stack, int hatColor) {
        this.setHatColor(hatColor); // orig :108/:127/:146
        if (this.trustedPlayerUuidPrimary == null) this.trustedPlayerUuidPrimary = player.getStringUUID(); // orig :109-111
        if (this.getIsActivated() == 0) this.setIsActivated(1); // orig :112-114
        this.setTame(true, true); // orig :115 func_70903_f(true)
        this.setOwnerUUID(UUID.fromString(this.trustedPlayerUuidPrimary)); // orig :116 — owner stays the slot-one player
        this.level().broadcastEntityEvent(this, (byte) 7); // orig :117 func_70908_e(true) hearts
        this.heal(this.getMaxHealth() - this.getHealth()); // orig :118
        this.setPersistenceRequired(); // orig :119 func_110163_bv
        if (!player.getAbilities().instabuild) stack.shrink(1); // orig :120-124
        return InteractionResult.SUCCESS;
    }

    private boolean isSuitableTarget(LivingEntity target) {
        if (this.level().getDifficulty() == Difficulty.PEACEFUL) return false;
        if (target == null || target == this || !target.isAlive()) return false;
        // orig EntityCannonFodder.java:288-290 — line-of-sight gate: the fodder
        // only considers targets its senses can see (func_70635_at().func_75522_a),
        // checked before the sit-anchor and faction logic.
        if (!this.getSensing().hasLineOfSight(target)) return false;

        if (this.isOrderedToSit()) {
            double deltaXToSitAnchor = this.patrolBlockX - target.getX();
            double deltaYToSitAnchor = this.patrolBlockY - target.getY();
            double deltaZToSitAnchor = this.patrolBlockZ - target.getZ();
            if (deltaXToSitAnchor * deltaXToSitAnchor + deltaYToSitAnchor * deltaYToSitAnchor
                    + deltaZToSitAnchor * deltaZToSitAnchor > 144.0) return false;
        }
        if (target instanceof Monster) return true;
        if (target instanceof EntityCannonFodder otherCannon) {
            int otherHatColor = otherCannon.getHatColor();
            return otherHatColor != 0 && otherHatColor != this.getHatColor();
        }
        if (target instanceof Player p) {
            if (p.getAbilities().instabuild) return false;
            if (this.trustedPlayerUuidPrimary != null && this.trustedPlayerUuidPrimary.equals(p.getStringUUID())) {
                return false;
            }
            if (this.trustedPlayerUuidSecondary != null && this.trustedPlayerUuidSecondary.equals(p.getStringUUID())) {
                return false;
            }
            return true;
        }
        return false;
    }

    private LivingEntity findSomethingToAttack() {
        List<LivingEntity> entities = this.level().getEntitiesOfClass(LivingEntity.class,
                this.getBoundingBox().inflate(10.0, 4.0, 10.0));
        // OPT-021: nearest-first pick without the full list sort; TargetSelection
        // preserves the removed sort's order and stable-tie semantics exactly.
        return TargetSelection.firstMatch(entities, this.localTargetSorter, this::isSuitableTarget);
    }

    // orig EntityCannonFodder.java:330-335 (func_70658_aO / getTotalArmorValue) —
    // armor is 3 only while fully combat-activated (is_activated == 2), else 0.
    @Override
    public int getArmorValue() {
        return this.getIsActivated() == 2 ? 3 : 0;
    }

    @Override
    protected void customServerAiStep() {
        if (this.isRemoved()) return;
        super.customServerAiStep();
        if (this.random.nextInt(200) == 1) this.setLastHurtByMob(null); // orig EntityCannonFodder.java:346-348 — func_70604_c(null) = setRevengeTarget(null): the revenge MEMORY forgotten (the port's lastHurtByMob), not the attack target (ENT-S-129)
        if (this.getIsActivated() != 2) return;

        // orig EntityCannonFodder.java:352-358 — species table: base sfreq 7 / dm 4.0f,
        // but a Chipmunk swings on the 6-gate for 3.0f. (The Lizard and VelocityRaptor
        // rows of the orig table belong to those entities' own batches.)
        int swingFrequency = 7;
        float fodderDamage = 4.0f;
        if (this instanceof Chipmunk) {
            fodderDamage = 3.0f;
            swingFrequency = 6;
        }
        if (this.level().getDifficulty() != Difficulty.PEACEFUL && this.random.nextInt(5) == 1) {
            LivingEntity attackTarget = this.findSomethingToAttack();
            if (attackTarget != null) {
                this.getNavigation().moveTo(attackTarget, 1.25);
                if (this.distanceToSqr(attackTarget) < 9.0
                        && (this.random.nextInt(swingFrequency + 1) == 0 || this.random.nextInt(swingFrequency) == 1)) {
                    attackTarget.hurt(this.damageSources().mobAttack(this), fodderDamage);
                }
            } else if (this.isOrderedToSit()) {
                this.getNavigation().moveTo(this.patrolBlockX, this.patrolBlockY, this.patrolBlockZ, 0.65);
            }
        }
        if (this.random.nextInt(250) == 1) this.heal(1.0f);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putString("NameOne", this.trustedPlayerUuidPrimary != null ? this.trustedPlayerUuidPrimary : "");
        tag.putString("NameTwo", this.trustedPlayerUuidSecondary != null ? this.trustedPlayerUuidSecondary : "");
        tag.putInt("IsActivated", this.getIsActivated());
        tag.putInt("HatColor", this.getHatColor());
        tag.putInt("PatrolX", this.patrolBlockX);
        tag.putInt("PatrolY", this.patrolBlockY);
        tag.putInt("PatrolZ", this.patrolBlockZ);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.trustedPlayerUuidPrimary = tag.getString("NameOne");
        if (this.trustedPlayerUuidPrimary.isEmpty()) this.trustedPlayerUuidPrimary = null;
        this.trustedPlayerUuidSecondary = tag.getString("NameTwo");
        if (this.trustedPlayerUuidSecondary.isEmpty()) this.trustedPlayerUuidSecondary = null;
        this.setIsActivated(tag.getInt("IsActivated"));
        this.setHatColor(tag.getInt("HatColor"));
        this.patrolBlockX = tag.getInt("PatrolX");
        this.patrolBlockY = tag.getInt("PatrolY");
        this.patrolBlockZ = tag.getInt("PatrolZ");
        if (this.trustedPlayerUuidPrimary != null) {
            this.setTame(true, false);
        }
    }

    @Override
    public boolean isFood(ItemStack stack) { return false; }

    @Nullable
    @Override
    public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob otherParent) { return null; }
}
