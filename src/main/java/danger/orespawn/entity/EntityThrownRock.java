package danger.orespawn.entity;

import danger.orespawn.ModItems;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import danger.orespawn.ModEntities;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

public class EntityThrownRock extends ThrowableProjectile {
    private static final EntityDataAccessor<Integer> DATA_ROCK_TYPE =
            SynchedEntityData.defineId(EntityThrownRock.class, EntityDataSerializers.INT);

    private static final int MAX_AGE_TICKS = 1000;
    private static final float ROTATION_STEP_DEGREES = 30.0f;
    private static final int EFFECT_DURATION_SHORT = 100;
    private static final int EFFECT_DURATION_LONG = 200;
    private static final int EFFECT_AMPLIFIER = 0;
    private static final int IGNITE_DURATION_SECONDS = 20;
    private static final float EXPLOSION_POWER_MEDIUM = 2.1f;
    private static final float EXPLOSION_POWER_LARGE = 5.1f;
    private static final double EXPLOSION_TARGET_OFFSET_Y = 0.25;

    private int rockType = 0;
    private int ticksAlive = 0;
    private float visualRotationDegrees = 0.0f;

    public EntityThrownRock(EntityType<? extends ThrowableProjectile> type, Level level) {
        super(type, level);
    }

    public EntityThrownRock(Level level, LivingEntity shooter, int rockType) {
        super(ModEntities.ENTITY_THROWN_ROCK.get(), shooter, level);
        this.rockType = rockType;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DATA_ROCK_TYPE, 0);
    }

    public int getRockType() { return this.entityData.get(DATA_ROCK_TYPE); }
    public void setRockType(int type) {
        if (this.level() == null || this.level().isClientSide) return;
        this.rockType = type;
        this.entityData.set(DATA_ROCK_TYPE, type);
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        if (this.level().isClientSide || this.isRemoved()) return;
        Entity target = result.getEntity();
        Entity owner = this.getOwner();
        if (owner == null || target == owner) return;

        float damage;
        double knockback = 0.2;
        double verticalKnock = 0.025;

        switch (this.rockType) {
            case 1 -> { damage = 2.0f; knockback = 0.1; }
            case 2, 3, 4, 5 -> damage = 5.0f;
            case 6 -> damage = 20.0f;
            case 7, 8 -> { damage = 40.0f; knockback = 0.5; verticalKnock = 0.055; }
            case 9, 10, 11 -> damage = 150.0f;
            case 12 -> damage = 250.0f;
            default -> damage = 2.0f;
        }

        // Owner may not be a Player (e.g. dispensers, command-spawned), so pick the right damage source
        if (owner instanceof Player player) {
            target.hurt(this.damageSources().playerAttack(player), damage);
        } else if (owner instanceof LivingEntity livingOwner) {
            target.hurt(this.damageSources().mobAttack(livingOwner), damage);
        } else {
            target.hurt(this.damageSources().thrown(this, owner), damage);
        }
        float angle = (float) Math.atan2(target.getZ() - owner.getZ(), target.getX() - owner.getX());
        if (target.isRemoved()) verticalKnock *= 2.0;
        target.push(Math.cos(angle) * knockback, verticalKnock, Math.sin(angle) * knockback);

        if (this.rockType == 3) target.igniteForSeconds(IGNITE_DURATION_SECONDS);
        if (target instanceof LivingEntity living) {
            if (this.rockType == 4) {
                living.addEffect(new MobEffectInstance(MobEffects.POISON, EFFECT_DURATION_SHORT, EFFECT_AMPLIFIER));
            }
            if (this.rockType == 5) {
                living.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, EFFECT_DURATION_SHORT, EFFECT_AMPLIFIER));
            }
            if (this.rockType == 6 || this.rockType == 9 || this.rockType == 11) {
                living.addEffect(new MobEffectInstance(MobEffects.WITHER, EFFECT_DURATION_SHORT, EFFECT_AMPLIFIER));
            }
            if (this.rockType == 10) {
                living.addEffect(new MobEffectInstance(MobEffects.POISON, EFFECT_DURATION_LONG, EFFECT_AMPLIFIER));
            }
            if (this.rockType == 12) {
                living.addEffect(new MobEffectInstance(MobEffects.WITHER, EFFECT_DURATION_SHORT, EFFECT_AMPLIFIER));
            }
        }

        if (this.rockType == 8 && !this.level().isClientSide) {
            boolean canGrief = this.level().getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING);
            this.level().explode(null, target.getX(), target.getY() + EXPLOSION_TARGET_OFFSET_Y, target.getZ(),
                    EXPLOSION_POWER_MEDIUM, canGrief, Level.ExplosionInteraction.MOB);
        }
        if (this.rockType == 12 && !this.level().isClientSide) {
            boolean canGrief = this.level().getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING);
            this.level().explode(null, target.getX(), target.getY() + EXPLOSION_TARGET_OFFSET_Y, target.getZ(),
                    EXPLOSION_POWER_LARGE, canGrief, Level.ExplosionInteraction.MOB);
        }
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (!this.level().isClientSide) {
            Block.popResource(this.level(), this.blockPosition(), new ItemStack(ModItems.ROCK.get()));
        }
        this.discard();
    }

    @Override
    public void tick() {
        super.tick();
        this.visualRotationDegrees += ROTATION_STEP_DEGREES;
        this.visualRotationDegrees %= 360.0f;
        this.setXRot(this.visualRotationDegrees);
        ++this.ticksAlive;
        if (this.ticksAlive > MAX_AGE_TICKS) this.discard();

        if (this.level().isClientSide) {
            this.rockType = this.getRockType();
        } else {
            this.setRockType(this.rockType);
        }
    }
}
