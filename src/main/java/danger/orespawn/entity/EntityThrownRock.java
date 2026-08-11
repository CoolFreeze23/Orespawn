package danger.orespawn.entity;

import danger.orespawn.ModItems;
import danger.orespawn.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.sounds.SoundSource;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import danger.orespawn.ModEntities;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ItemSupplier;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class EntityThrownRock extends ThrowableProjectile implements ItemSupplier {
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

    // orig MyDispenserBehaviorRock.java:72-75 — dispensers spawn rocks at a bare position,
    // then call setRockType per the dispensed item
    public EntityThrownRock(Level level, double x, double y, double z) {
        super(ModEntities.ENTITY_THROWN_ROCK.get(), level);
        this.setPos(x, y, z);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DATA_ROCK_TYPE, 0);
    }

    public int getRockType() { return this.entityData.get(DATA_ROCK_TYPE); }

    /**
     * ENTITY_NOOP_RENDERER/thrown_rock — orig RenderThrownRock billboarded
     * textures/items/rock*.png keyed by getRockType() (RenderThrownRock.java:18-29,
     * 63-102; type 1 rocksmall ... type 12 rockcrystaltnt, default rocksmall).
     * The port items reuse those exact textures, so returning the matching rock
     * item through vanilla ThrownItemRenderer reproduces the original render.
     * Reads the synched rock type so the client picks the right sprite.
     */
    @Override
    public ItemStack getItem() {
        return new ItemStack(switch (this.getRockType()) {
            case 2 -> ModItems.ROCK.get();
            case 3 -> ModItems.ROCK_RED.get();
            case 4 -> ModItems.ROCK_GREEN.get();
            case 5 -> ModItems.ROCK_BLUE.get();
            case 6 -> ModItems.ROCK_PURPLE.get();
            case 7 -> ModItems.ROCK_SPIKEY.get();
            case 8 -> ModItems.ROCK_TNT.get();
            case 9 -> ModItems.ROCK_CRYSTAL_RED.get();
            case 10 -> ModItems.ROCK_CRYSTAL_GREEN.get();
            case 11 -> ModItems.ROCK_CRYSTAL_BLUE.get();
            case 12 -> ModItems.ROCK_CRYSTAL_TNT.get();
            default -> ModItems.ROCK_SMALL.get(); // type 1 and orig fallback (texture1)
        });
    }
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
            case 1 -> { damage = 2.0f; knockback = 0.1; }     // orig EntityThrownRock.java:79-80
            case 2, 3, 4 -> damage = 5.0f;                    // orig :89,99,110
            case 5 -> { damage = 10.0f; knockback = 0.1; }    // orig :123-124 — 10 dmg, ks 0.1
            case 6 -> damage = 20.0f;                         // orig :136
            case 7 -> damage = 40.0f;                         // orig :149-151 — ks 0.2 (only t8 gets 0.5)
            case 8 -> { damage = 40.0f; knockback = 0.5; verticalKnock = 0.055; } // orig :159-161
            case 9, 10, 11 -> damage = 150.0f;                // orig :170,184,200
            case 12 -> damage = 250.0f;                       // orig :216
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

        if (this.rockType == 3) target.igniteForSeconds(IGNITE_DURATION_SECONDS); // orig :107 — 20t fire
        if (this.rockType == 9) target.igniteForSeconds(50);                      // orig :178 — setFire(50) takes seconds in 1.7.10
        if (target instanceof LivingEntity living) {
            if (this.rockType == 4) {
                // orig :119 — Poison 100t
                living.addEffect(new MobEffectInstance(MobEffects.POISON, EFFECT_DURATION_SHORT, EFFECT_AMPLIFIER));
            }
            if (this.rockType == 5) {
                // orig :132 — Slowness 100t
                living.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, EFFECT_DURATION_SHORT, EFFECT_AMPLIFIER));
            }
            if (this.rockType == 6 || this.rockType == 9 || this.rockType == 10
                    || this.rockType == 11 || this.rockType == 12) {
                // orig :145,:180,:196,:212,:225 — Weakness (field_76437_t) 100t, NOT Wither
                living.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, EFFECT_DURATION_SHORT, EFFECT_AMPLIFIER));
            }
            if (this.rockType == 10) {
                // orig :193 — Poison 200t
                living.addEffect(new MobEffectInstance(MobEffects.POISON, EFFECT_DURATION_LONG, EFFECT_AMPLIFIER));
            }
            if (this.rockType == 11) {
                // orig :209 — Slowness 200t
                living.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, EFFECT_DURATION_LONG, EFFECT_AMPLIFIER));
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
        if (this.level().isClientSide) {
            this.discard();
            return;
        }
        // orig EntityThrownRock.java:229-285 — the block-impact branch (no entity hit,
        // rock_type != 0): break glass/glass panes in a 3x3x3 cube around the impact
        // (playing "orespawn:glassdead" once) and return the type-specific rock item.
        // Entity hits drop nothing (the orig only drops in the else-branch).
        if (!(result instanceof EntityHitResult) && this.rockType != 0) {
            BlockPos impact = this.blockPosition();
            boolean played = false;
            for (int i = -1; i <= 1; ++i) {
                for (int j = -1; j <= 1; ++j) {
                    for (int k = -1; k <= 1; ++k) {
                        BlockPos pos = impact.offset(i, j, k);
                        BlockState state = this.level().getBlockState(pos);
                        // orig :238 — plain glass and glass panes only (no break gate in the orig)
                        if (!state.is(Blocks.GLASS) && !state.is(Blocks.GLASS_PANE)) continue;
                        this.level().setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
                        if (!played) {
                            this.level().playSound(null, impact, ModSounds.GLASSDEAD.get(),
                                    SoundSource.NEUTRAL, 1.0f, 1.0f);
                            played = true;
                        }
                    }
                }
            }
            Item recovered = switch (this.rockType) {  // orig :249-284 — typed rock recovery
                case 1 -> ModItems.ROCK_SMALL.get();
                case 2 -> ModItems.ROCK.get();
                case 3 -> ModItems.ROCK_RED.get();
                case 4 -> ModItems.ROCK_GREEN.get();
                case 5 -> ModItems.ROCK_BLUE.get();
                case 6 -> ModItems.ROCK_PURPLE.get();
                case 7 -> ModItems.ROCK_SPIKEY.get();
                case 8 -> ModItems.ROCK_TNT.get();
                case 9 -> ModItems.ROCK_CRYSTAL_RED.get();
                case 10 -> ModItems.ROCK_CRYSTAL_GREEN.get();
                case 11 -> ModItems.ROCK_CRYSTAL_BLUE.get();
                case 12 -> ModItems.ROCK_CRYSTAL_TNT.get();
                default -> null;
            };
            if (recovered != null) {
                Block.popResource(this.level(), impact, new ItemStack(recovered));
            }
        }
        this.discard();
    }

    @Override
    public void tick() {
        // orig EntityThrownRock.java:291-293 — the water-skip probe samples the block at
        // the PRE-move position of this tick (captured before super), and uses plain (int)
        // casts, not floor: negative coordinates truncate toward zero. Orig bug kept.
        int preTickX = (int) this.getX();
        int preTickY = (int) this.getY();
        int preTickZ = (int) this.getZ();
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

        // orig :307-312 — stone-skipping: a rock falling gently (-0.55 < motionY < -0.15)
        // with enough horizontal speed (motionX^2 + motionZ^2 > 0.5, summed as float) that
        // finds water at the probe position reflects its vertical motion, keeping 3/4 of
        // the velocity on every axis — so a flat throw skips repeatedly until it slows
        // below the gate and finally sinks. Runs on both sides, as the orig did.
        BlockState skipBlock = this.level().getBlockState(new BlockPos(preTickX, preTickY, preTickZ));
        Vec3 velocity = this.getDeltaMovement();
        // orig :308 compares against field_150355_j (STILL water, id 9) only — flowing
        // water (field_150358_i, id 8) never skipped. 1.21 merged both into
        // minecraft:water, where LEVEL=0 (a source) is exactly the old still block, so
        // gate on Blocks.WATER + isSource().
        if (skipBlock.is(Blocks.WATER) && skipBlock.getFluidState().isSource()
                && velocity.y < (double) -0.15f && velocity.y > (double) -0.55f
                && (float) (velocity.x * velocity.x + velocity.z * velocity.z) > 0.5f) {
            // orig :309-311 — motionY = -(motionY*3/4); motionX,Z *= 3/4
            this.setDeltaMovement(velocity.x * 3.0 / 4.0, -(velocity.y * 3.0 / 4.0), velocity.z * 3.0 / 4.0);
        }
    }
}
