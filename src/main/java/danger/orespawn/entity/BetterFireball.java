package danger.orespawn.entity;

import danger.orespawn.ModEntities;
import danger.orespawn.OreSpawnConfig;
import danger.orespawn.util.MyUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.projectile.LargeFireball;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileDeflection;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.EventHooks;

public class BetterFireball extends LargeFireball {
    private static final int MAX_LIFETIME_TICKS = 600;
    private static final float LARGE_MOB_BB_AREA_THRESHOLD = 30.0f;
    private static final float DAMAGE_SMALL = 5.0f;
    private static final float DAMAGE_LARGE = 10.0f;
    private static final int FIRE_SECONDS_ON_HIT = 5;
    private static final double SMOKE_OFFSET_Y = 0.5;
    /**
     * orig BetterFireball.java:84 setSmall() {@code func_70105_a(0.3125f, 0.3125f)}
     * — the vanilla small-fireball box (EntityType.SMALL_FIREBALL .sized(0.3125F, 0.3125F)).
     */
    private static final float SMALL_SIZE = 0.3125f;

    private int ticksAlive = 0;
    private int explosionPower = 1;
    private boolean small = false;
    /** orig BetterFireball.java:70-72,152,214 — shooter-set flag sparing players/Dragons. */
    private boolean notme = false;

    public BetterFireball(EntityType<? extends LargeFireball> type, Level level) {
        super(type, level);
    }

    /**
     * orig BetterFireball.java:54-68 — the shooter constructor (every shooter builds
     * shots as {@code new BetterFireball(level, this, accel)}). ENT-S-098, owner
     * ruling 2026-09-03: it used to chain to {@code LargeFireball(level, shooter,
     * movement, 1)}, which is {@code super(EntityType.FIREBALL, ...)} (1.21.1
     * LargeFireball.java:23-26), so a shot was typed {@code minecraft:fireball}: the
     * better_fireball registration (its .sized, noSummon, the renderer binding)
     * governed only EntityType#create instances, clients rebuilt shots as plain
     * LargeFireballs, and NBT saved them under the vanilla id. LargeFireball has no
     * type-taking shooter constructor, so this goes through the (EntityType, Level)
     * chain with the mod's own type and replays, in the same order, exactly what the
     * vanilla chain assigned (AbstractHurtingProjectile.java:37-50 -> Fireball.java:27-29
     * -> LargeFireball.java:23-26): moveTo the shooter's feet at the fresh 0/0
     * rotation, reapplyPosition, the normalised-times-{@code accelerationPower}
     * (0.1) delta movement with hasImpulse (vanilla's private
     * assignDirectionalMovement), setOwner, then setRot from the shooter. Vanilla's
     * private explosionPower keeps its field initialiser 1, the literal the old
     * chain passed, so the LargeFireball-side explosion is unchanged; the port's
     * own power/damage/small flags are untouched. The 1.7.10 kinematics (:58-67:
     * shooter position + rotation, motion zeroed, accel = dir/|dir| * 0.1) are the
     * same values.
     */
    public BetterFireball(Level level, LivingEntity shooter, Vec3 movement) {
        this(ModEntities.BETTER_FIREBALL.get(), level);
        this.moveTo(shooter.getX(), shooter.getY(), shooter.getZ(), this.getYRot(), this.getXRot());
        this.reapplyPosition();
        this.setDeltaMovement(movement.normalize().scale(this.accelerationPower));
        this.hasImpulse = true;
        this.setOwner(shooter);
        this.setRot(shooter.getYRot(), shooter.getXRot());
    }

    public void setNotMe() { this.notme = true; }
    public void setBig() { this.explosionPower = 2; }
    public void setReallyBig() { this.explosionPower = 4; }
    /**
     * orig BetterFireball.java:82-85 — flags the 5-damage / no-explosion variant
     * AND shrinks the box from the constructors' 1.0x1.0 (:48/:57) to
     * 0.3125x0.3125 (:84). The port's setSmall only flipped the flag; owner
     * ruling 2026-09-03 (ENT-S-095) restores the shrink the modern way: the
     * projectile chain (LargeFireball/Fireball/AbstractHurtingProjectile/
     * Projectile) has no dims hook of its own and {@code getDefaultDimensions}
     * is a LivingEntity method, so the non-living override point is
     * {@code Entity#getDimensions(Pose)} (the vanilla AreaEffectCloud pattern),
     * keyed on the flag and applied through {@code refreshDimensions()}, which
     * rebuilds the live AABB via reapplyPosition() whether or not the entity
     * is in a level yet (shooters call setSmall before addFreshEntity). As in
     * 1.7.10 the flag is neither synced nor saved: the client keeps the
     * type's 1x1 box exactly as its World-ctor copy did (orig :46-49).
     */
    public void setSmall() {
        this.small = true;
        this.refreshDimensions();
    }

    /** orig BetterFireball.java:84 — 0.3125x0.3125 once setSmall() ran, else the registered 1.0x1.0 (orig :48/:57). */
    @Override
    public EntityDimensions getDimensions(Pose pose) {
        return this.small ? EntityDimensions.scalable(SMALL_SIZE, SMALL_SIZE) : super.getDimensions(pose);
    }

    @Override
    public void tick() {
        super.tick();
        this.ticksAlive++;
        if (this.ticksAlive >= MAX_LIFETIME_TICKS) {
            this.discard();
            return;
        }
        if (this.level().isClientSide) {
            this.level().addParticle(ParticleTypes.SMOKE, this.getX(), this.getY() + SMOKE_OFFSET_Y, this.getZ(), 0, 0, 0);
        }
    }

    /**
     * orig BetterFireball.java:136-155 (tick sweep skips) and :208-217 (impact
     * returns) — the fireball passes through other BetterFireballs, Mothra,
     * GodzillaHeads and Royalty, and, when the shooter set notme, through
     * players and Dragons. The original aborted the whole collision scan when
     * such an entity appeared anywhere in the sweep list; the port maps this
     * to per-entity pass-through, the closest vanilla-hit-detection analogue.
     */
    @Override
    protected boolean canHitEntity(Entity target) {
        if (target instanceof BetterFireball || target instanceof Mothra
                || target instanceof GodzillaHead || MyUtils.isRoyalty(target)) {
            return false;
        }
        if (this.notme && (target instanceof Player || target instanceof Dragon)) {
            return false;
        }
        return super.canHitEntity(target);
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        if (this.level().isClientSide) return;
        Entity target = result.getEntity();
        if (target == this.getOwner()) return;

        if (target instanceof LivingEntity living) {
            float boundingBoxArea = living.getBbHeight() * living.getBbWidth();
            // orig BetterFireball.java:221-223 — big mobs lose half their HP,
            // except Royalty/Godzilla/GodzillaHead/PitchBlack/Kraken
            if (boundingBoxArea > LARGE_MOB_BB_AREA_THRESHOLD
                    && !MyUtils.isRoyalty(living)
                    && !(living instanceof Godzilla)
                    && !(living instanceof GodzillaHead)
                    && !(living instanceof PitchBlack)
                    && !(living instanceof Kraken)) {
                living.setHealth(living.getHealth() / 2.0f);
            }
        }

        float damage = this.small ? DAMAGE_SMALL : DAMAGE_LARGE;
        target.hurt(this.damageSources().fireball(this, this.getOwner()), damage);
        target.igniteForSeconds(FIRE_SECONDS_ON_HIT);
    }

    /**
     * orig BetterFireball.java:232-264, the block half of func_70227_a (ENT-S-104, owner
     * ruling 2026-09-04: a parity bug, fixed in classic; MOD-031, owner ruling 2026-09-04
     * "accepted as a modern option, default on; classic stays 1.7.10", gates the fire on
     * mobGriefing while effective, below). A block hit switches on the hit side
     * ({@code field_72310_e} 0..5 = down, up, north, south, west, east, :236-260) to the
     * neighbour on that side and, if that cell is air ({@code func_147437_c} = isAirBlock,
     * :261), sets {@code Blocks.fire} there ({@code func_147449_b} = setBlock with flags 3,
     * :262) — before the explosion (:265-267) and the setDead (:268), and for small shots
     * too (the small gate covers only the explosion). Port: the hit face's relative
     * position, {@code Level.isEmptyBlock} (= isAir) and {@code setBlockAndUpdate}
     * (flags 3), with the fire state from {@code BaseFireBlock.getState} as 1.21.1's own
     * fireballs use it (soul fire over soul soil / soul sand, plain fire elsewhere; 1.7.10
     * had the one fire block). In classic, orig's conditions are kept where vanilla's
     * differ: no mobGriefing / EntityMobGriefingEvent gate (1.21.1
     * {@code SmallFireball.onHitBlock} has one for Mob owners) and no
     * {@code BaseFireBlock.canBePlacedAt} survival check (orig placed fire on any air cell;
     * fire that cannot survive there removes itself on its own tick in both versions).
     * MOD-031 (owner ruling 2026-09-04: "accepted as a modern option, default on; classic
     * stays 1.7.10"): while {@code OreSpawnConfig.fireRespectsMobGriefing()} is effective
     * (master AND key, read here at impact, never snapshotted) the fire is placed only if
     * {@code EventHooks.canEntityGrief(level, getOwner())} -- vanilla
     * the gate vanilla's fireballs use ({@code LargeFireball.onHit} for every owner;
     * {@code SmallFireball.onHitBlock} only for Mob owners): the mobGriefing gamerule, through
     * EntityMobGriefingEvent for a non-null owner; the survival check stays out in both
     * modes. Master or key off: exactly the classic placement (fire always, no event
     * posted). super first: {@code Projectile.onHitBlock} is the
     * block-side {@code onProjectileHit} callback (target blocks, bells, decorated pots),
     * vanilla plumbing 1.7.10 had no equivalent of, which the ENT-S-102 replay in
     * {@link #onHit} already ran before the fix.
     */
    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);
        if (this.level().isClientSide) return;
        // MOD-031 (owner ruling 2026-09-04: "accepted as a modern option, default on; classic
        // stays 1.7.10"): the effective option, read at impact, gates orig :261-263's fire on
        // canEntityGrief(level, owner) for every owner, as vanilla LargeFireball.onHit does
        // (SmallFireball.onHitBlock gates Mob owners only); classic keeps it
        // unconditional and posts no EntityMobGriefingEvent.
        if (OreSpawnConfig.fireRespectsMobGriefing() && !EventHooks.canEntityGrief(this.level(), this.getOwner())) {
            return;
        }
        BlockPos firePos = result.getBlockPos().relative(result.getDirection());
        if (this.level().isEmptyBlock(firePos)) {
            this.level().setBlockAndUpdate(firePos, BaseFireBlock.getState(this.level(), firePos));
        }
    }

    /**
     * orig BetterFireball.java:205-270 (func_70227_a): one impact, one explosion, and
     * only when not small (:265-267 {@code if (!this.small) explode(power)}, then
     * setDead :268). ENT-S-102, owner ruling 2026-09-04 ("fix with a test"): this
     * used to chain to {@code super.onHit} = 1.21.1 {@code LargeFireball.onHit},
     * which explodes at vanilla's private explosionPower (1, sourced by the fireball
     * itself) and discards BEFORE the port's own blast ran, so a big shot exploded
     * twice per impact (1, then 1 / 2 / 4) and a small shot, which 1.7.10 never
     * exploded, still got the vanilla power-1 blast. LargeFireball.onHit is only
     * {@code Projectile.onHit} plus that blast and the discard, so the blast is
     * skipped by replaying Projectile.onHit's dispatch here instead of chaining to
     * it: an entity hit deflects a redirectable projectile it struck, runs
     * {@link #onHitEntity} and posts PROJECTILE_LAND at the hit location; a block
     * hit runs {@link #onHitBlock} (which places orig :232-264's fire, ENT-S-104) and
     * posts PROJECTILE_LAND at the block. Then, server-side, the port's own explosion
     * when not small and the discard; the null source is orig :266
     * ({@code func_72885_a(null, x, y, z, power, true, mobGriefing)}), whose two flags
     * were fire = true always and block destruction = mobGriefing (1.7.10
     * {@code Explosion.isFlaming} / {@code isSmoking}). ENT-S-104 (owner ruling
     * 2026-09-04, classic): the port used to feed mobGriefing into 1.21.1's single
     * {@code fire} slot, so an OreSpawn fireball placed no explosion fire while the rule
     * was off; it now passes fire = true and lets {@code ExplosionInteraction.MOB}
     * resolve the destruction flag, which for a null source IS the mobGriefing gamerule:
     * 1.21.1 {@code Level.explode} (the 13-argument form, bytecode) maps MOB to
     * {@code EventHooks.canEntityGrief(level, source) ? getDestroyType(RULE_MOB_EXPLOSION_DROP_DECAY)
     * : BlockInteraction.KEEP}, and NeoForge 21.1.223's {@code canEntityGrief(level, null)}
     * returns {@code level.getGameRules().getBoolean(RULE_MOBGRIEFING)} without posting an
     * EntityMobGriefingEvent, so, as before, the null-source path reads the gamerule
     * directly and the vanilla blast's event post for the owner is skipped with the
     * blast. MOD-031 (owner ruling 2026-09-04: "accepted as a modern option, default on;
     * classic stays 1.7.10"): while {@code OreSpawnConfig.fireRespectsMobGriefing()} is
     * effective (master AND key, read here at impact, never snapshotted) the fire flag is
     * {@code EventHooks.canEntityGrief(level, getOwner())} instead of true -- vanilla
     * {@code LargeFireball.onHit}'s flag (the gamerule, through EntityMobGriefingEvent for
     * a non-null owner); the null source and the MOB interaction are unchanged, so block
     * destruction stays the gamerule's in both modes. Master or key off: fire = true and no
     * event posted, exactly the classic call. Vanilla's private explosionPower still loads
     * from the shared
     * {@code ExplosionPower} key ({@code LargeFireball.readAdditionalSaveData}; orig
     * :277-281 wrote the same key), but nothing reads it any more: the only blast is
     * this one, at the port's {@code explosionPower}.
     */
    @Override
    protected void onHit(HitResult result) {
        HitResult.Type type = result.getType();
        if (type == HitResult.Type.ENTITY) {
            EntityHitResult entityHit = (EntityHitResult) result;
            Entity target = entityHit.getEntity();
            if (target.getType().is(EntityTypeTags.REDIRECTABLE_PROJECTILE) && target instanceof Projectile projectile) {
                projectile.deflect(ProjectileDeflection.AIM_DEFLECT, this.getOwner(), this.getOwner(), true);
            }
            this.onHitEntity(entityHit);
            this.level().gameEvent(GameEvent.PROJECTILE_LAND, result.getLocation(), GameEvent.Context.of(this, null));
        } else if (type == HitResult.Type.BLOCK) {
            BlockHitResult blockHit = (BlockHitResult) result;
            this.onHitBlock(blockHit);
            BlockPos pos = blockHit.getBlockPos();
            this.level().gameEvent(GameEvent.PROJECTILE_LAND, pos, GameEvent.Context.of(this, this.level().getBlockState(pos)));
        }
        if (!this.level().isClientSide) {
            if (!this.small) {
                // orig :266 — fire = true always; block destruction = mobGriefing, which MOB
                // resolves through the gamerule for this null source (ENT-S-104). MOD-031
                // (owner ruling 2026-09-04: "accepted as a modern option, default on; classic
                // stays 1.7.10"): while the option is effective the fire flag is vanilla
                // LargeFireball.onHit's canEntityGrief(level, owner), read here at impact;
                // classic keeps true.
                boolean fire = !OreSpawnConfig.fireRespectsMobGriefing()
                        || EventHooks.canEntityGrief(this.level(), this.getOwner());
                this.level().explode(null, this.getX(), this.getY(), this.getZ(),
                        (float) this.explosionPower, fire, Level.ExplosionInteraction.MOB);
            }
            this.discard();
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("ExplosionPower", this.explosionPower);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("ExplosionPower")) {
            this.explosionPower = tag.getInt("ExplosionPower");
        }
    }
}
