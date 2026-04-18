package danger.orespawn.entity;

import danger.orespawn.ModEntities;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.ai.BugMeleeAttackGoal;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class EntityCaterKiller extends Monster {
    private static final EntityDataAccessor<Integer> DATA_ATTACKING =
            SynchedEntityData.defineId(EntityCaterKiller.class, EntityDataSerializers.INT);

    /**
     * Despawn timer — matches the 1.7.10 behavior where a Cater Killer that
     * sits below max health for 2400 ticks (2 minutes) despawns itself. This
     * prevents world-save bloat from half-damaged cater killers that escaped
     * combat.
     */
    private int damagedDespawnTicker = 0;

    /**
     * Cobweb-trail throttle. The 1.7.10 Cater Killer spat cobwebs into the
     * tile underneath whichever player it was actively chasing, slowing the
     * pursued player and forcing them into ranged combat. We rate-limit
     * those setBlock calls so a long chase doesn't hammer chunk updates.
     */
    private int cobwebCooldown = 0;
    private static final int COBWEB_INTERVAL_TICKS = 40;

    /**
     * Tree-eating regen throttle. The reference mob would munch leaves /
     * logs adjacent to itself for steady out-of-combat healing. The check
     * runs at most once per second to keep it cheap; the actual heal value
     * (5 HP per leaf, 10 HP per log) matches the 1.7.10 numbers.
     */
    private int treeEatCooldown = 0;
    private static final int TREE_EAT_INTERVAL_TICKS = 20;

    public EntityCaterKiller(EntityType<? extends EntityCaterKiller> type, Level level) {
        super(type, level);
        this.xpReward = 200;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new BugMeleeAttackGoal(
                this, this::setAttacking, BugMeleeAttackGoal.Params.caterKiller()));
        this.goalSelector.addGoal(2, new MyEntityAIWanderALot(this, 16, 1.0));
        this.goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 8.0f));
        this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 350.0)
                .add(Attributes.MOVEMENT_SPEED, 0.35)
                .add(Attributes.ATTACK_DAMAGE, 20.0)
                .add(Attributes.FOLLOW_RANGE, 40.0);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_ATTACKING, 0);
    }

    public int getAttacking() {
        return this.entityData.get(DATA_ATTACKING);
    }

    public void setAttacking(int value) {
        this.entityData.set(DATA_ATTACKING, value);
    }

    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        if (this.random.nextInt(3) == 0) {
            return SoundEvent.createVariableRangeEvent(
                    ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "caterkiller_living"));
        }
        return null;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvent.createVariableRangeEvent(
                ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "caterkiller_hit"));
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvent.createVariableRangeEvent(
                ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "caterkiller_death"));
    }

    @Override
    protected float getSoundVolume() {
        return 1.5f;
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        if (super.doHurtTarget(target)) {
            if (target instanceof LivingEntity) {
                double knockbackStrength = 1.2;
                double verticalKnockback = 0.1;
                float angle = (float) Math.atan2(target.getZ() - this.getZ(), target.getX() - this.getX());
                if (target.isRemoved() || target instanceof Player) verticalKnockback *= 2.0;
                target.push(Math.cos(angle) * knockbackStrength, verticalKnockback, Math.sin(angle) * knockbackStrength);
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        Entity attacker = source.getEntity();
        boolean ret = super.hurt(source, amount);
        if (attacker instanceof Mob mob) {
            this.setTarget(mob);
        }
        return ret;
    }

    @Override
    protected void customServerAiStep() {
        if (this.isRemoved()) return;
        super.customServerAiStep();

        if (this.getHealth() + 1.0f < this.getMaxHealth()) {
            ++this.damagedDespawnTicker;
            if (this.damagedDespawnTicker > 2400) {
                this.discard();
                return;
            }
        } else {
            this.damagedDespawnTicker = 0;
        }

        if (this.random.nextInt(150) == 1 && this.getHealth() < this.getMaxHealth()) {
            this.heal(2.0f);
        }

        if (this.cobwebCooldown > 0) --this.cobwebCooldown;
        if (this.treeEatCooldown > 0) --this.treeEatCooldown;

        LivingEntity target = this.getTarget();
        if (target instanceof Player chased && this.cobwebCooldown == 0
                && this.distanceToSqr(chased) < 144.0
                && this.level().getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING)) {
            tryDropCobwebUnder(chased);
            this.cobwebCooldown = COBWEB_INTERVAL_TICKS;
        }

        if (target == null && this.treeEatCooldown == 0
                && this.getHealth() < this.getMaxHealth()
                && this.level().getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING)) {
            tryEatNearbyTreeBlock();
            this.treeEatCooldown = TREE_EAT_INTERVAL_TICKS;
        }
    }

    /**
     * Spit a cobweb into the floor tile directly under the chased player.
     * Only fires on solid-floor tiles with an air block immediately above
     * (so we don't overwrite the player's standing tile or melt structural
     * blocks). The cobweb naturally despawns to nothing on player break.
     */
    private void tryDropCobwebUnder(Player chased) {
        if (!(this.level() instanceof ServerLevel server)) return;
        BlockPos under = chased.blockPosition();
        BlockState atFeet = server.getBlockState(under);
        if (!atFeet.isAir() && atFeet.getBlock() != Blocks.COBWEB) return;
        BlockPos floor = under.below();
        BlockState floorState = server.getBlockState(floor);
        if (floorState.isAir() || !floorState.getFluidState().isEmpty()) return;
        server.setBlock(under, Blocks.COBWEB.defaultBlockState(), 3);
    }

    /**
     * Health-regen by chewing through leaves and logs in a small radius
     * around the body. Picks one block at random per check so the eat
     * effect feels organic rather than zipper-stripping a forest in a
     * single tick. Uses the standard log/leaf tags so modded trees with
     * the right tag entries (most do) also feed the cater killer.
     */
    private void tryEatNearbyTreeBlock() {
        if (!(this.level() instanceof ServerLevel server)) return;
        int bx = (int) Math.floor(this.getX());
        int by = (int) Math.floor(this.getY());
        int bz = (int) Math.floor(this.getZ());
        for (int attempts = 0; attempts < 6; attempts++) {
            int dx = this.random.nextInt(5) - 2;
            int dy = this.random.nextInt(4) - 1;
            int dz = this.random.nextInt(5) - 2;
            BlockPos pos = new BlockPos(bx + dx, by + dy, bz + dz);
            BlockState state = server.getBlockState(pos);
            if (state.isAir()) continue;
            float healAmount;
            if (state.is(BlockTags.LEAVES)) {
                healAmount = 5.0f;
            } else if (state.is(BlockTags.LOGS)) {
                healAmount = 10.0f;
            } else {
                continue;
            }
            server.destroyBlock(pos, false, this);
            this.heal(healAmount);
            return;
        }
    }

    /**
     * 1.7.10 Cater Killer death sequence: rather than vanishing, the
     * caterpillar pupates into a Brutalfly at the same location and seeds
     * a small swarm of standard butterflies (3-5) as the chrysalis bursts.
     * The Brutalfly inherits the original rotation so the visual handoff
     * reads as a single creature undergoing metamorphosis. Death loot
     * still drops via {@link #dropCustomDeathLoot} so the player gets
     * their bones / leather / name tag despite the entity transformation.
     */
    @Override
    public void die(DamageSource cause) {
        if (!this.level().isClientSide && this.level() instanceof ServerLevel serverLevel) {
            EntityBrutalfly brutalfly = ModEntities.ENTITY_BRUTALFLY.get().create(serverLevel);
            if (brutalfly != null) {
                brutalfly.moveTo(this.getX(), this.getY() + 0.5, this.getZ(),
                        this.getYRot(), 0.0f);
                serverLevel.addFreshEntity(brutalfly);
            }
            int butterflies = 3 + this.random.nextInt(3);
            for (int i = 0; i < butterflies; i++) {
                EntityButterfly bf = ModEntities.ENTITY_BUTTERFLY.get().create(serverLevel);
                if (bf == null) continue;
                double ox = this.getX() + (this.random.nextDouble() - 0.5) * 1.5;
                double oz = this.getZ() + (this.random.nextDouble() - 0.5) * 1.5;
                bf.moveTo(ox, this.getY() + 0.5, oz,
                        this.random.nextFloat() * 360.0f, 0.0f);
                serverLevel.addFreshEntity(bf);
            }
        }
        super.die(cause);
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean recentlyHit) {
        super.dropCustomDeathLoot(level, source, recentlyHit);
        this.spawnAtLocation(Items.NAME_TAG);
        for (int i = 0; i < 10; i++) {
            this.spawnAtLocation(Items.LEATHER);
        }
        for (int i = 0; i < 6; i++) {
            this.spawnAtLocation(Items.BONE);
        }
    }
}
