package danger.orespawn.entity;

import danger.orespawn.OreSpawnMod;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

/**
 * Base ant entity that provides dimension teleportation on right-click.
 * In the original 1.7.10 OreSpawn, clicking ants transported the player
 * to one of six custom dimensions. Subclasses override {@link #getTargetDimension()}
 * to select their target dimension; the base Ant sends to Utopia.
 *
 * Dimension mapping (matches original 1.7.10 DimensionIDs):
 *   Ant         -> Utopia   (DimensionID)
 *   RedAnt      -> Mining   (DimensionID2)
 *   RainbowAnt  -> Village  (DimensionID3)
 *   UnstableAnt -> Islands  (DimensionID4)
 *   Termite     -> Crystal  (DimensionID5)
 *   Butterfly   -> Chaos    (DimensionID6) — separate class, not an Ant subclass
 */
public class EntityAnt extends Animal {
    // All six OreSpawn dimension keys, shared with Butterfly for Chaos access
    public static final ResourceKey<Level> UTOPIA =
            ResourceKey.create(Registries.DIMENSION, ResourceLocation.fromNamespaceAndPath("orespawn", "utopia"));
    public static final ResourceKey<Level> MINING =
            ResourceKey.create(Registries.DIMENSION, ResourceLocation.fromNamespaceAndPath("orespawn", "mining"));
    public static final ResourceKey<Level> VILLAGE =
            ResourceKey.create(Registries.DIMENSION, ResourceLocation.fromNamespaceAndPath("orespawn", "village"));
    public static final ResourceKey<Level> ISLANDS =
            ResourceKey.create(Registries.DIMENSION, ResourceLocation.fromNamespaceAndPath("orespawn", "islands"));
    public static final ResourceKey<Level> CRYSTAL =
            ResourceKey.create(Registries.DIMENSION, ResourceLocation.fromNamespaceAndPath("orespawn", "crystal"));
    public static final ResourceKey<Level> CHAOS =
            ResourceKey.create(Registries.DIMENSION, ResourceLocation.fromNamespaceAndPath("orespawn", "chaos"));

    /** Ticks before the player can teleport again (4 seconds) */
    private static final int TELEPORT_COOLDOWN = 80;

    public double moveSpeed = 0.15;

    public EntityAnt(EntityType<? extends EntityAnt> type, Level level) {
        super(type, level);
        this.xpReward = 0;
    }

    /**
     * Which dimension this ant type teleports the player to.
     * Subclasses override to target different dimensions.
     */
    protected ResourceKey<Level> getTargetDimension() {
        return UTOPIA;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new PanicGoal(this, 1.4));
        this.goalSelector.addGoal(1, new MyEntityAIWanderALot(this, 9, 1.0));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 1.0)
                .add(Attributes.MOVEMENT_SPEED, 0.15)
                .add(Attributes.ATTACK_DAMAGE, 0.0);
    }

    /**
     * Right-clicking an ant with an empty hand teleports the player to this ant's
     * target dimension, or back to the Overworld if already there. Mirrors the
     * original 1.7.10 interact() -> transferPlayerToDimension() behavior.
     */
    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (this.level().isClientSide()) return InteractionResult.SUCCESS;
        if (!(player instanceof ServerPlayer serverPlayer)) return InteractionResult.PASS;
        if (!player.getItemInHand(hand).isEmpty()) return InteractionResult.PASS;
        if (player.isOnPortalCooldown()) return InteractionResult.PASS;

        ResourceKey<Level> target = getTargetDimension();
        // Toggle: if already in the target dimension, go home to Overworld
        ResourceKey<Level> destination = this.level().dimension().equals(target)
                ? Level.OVERWORLD
                : target;

        ServerLevel destLevel = serverPlayer.server.getLevel(destination);
        if (destLevel == null) return InteractionResult.FAIL;

        serverPlayer.setPortalCooldown(TELEPORT_COOLDOWN);

        double x = serverPlayer.getX();
        double z = serverPlayer.getZ();
        int safeY = findSafeY(destLevel, BlockPos.containing(x, 0, z));

        serverPlayer.teleportTo(destLevel, x, safeY, z,
                serverPlayer.getYRot(), serverPlayer.getXRot());

        serverPlayer.playSound(SoundEvents.ENDERMAN_TELEPORT, 1.0f, 1.0f);
        return InteractionResult.SUCCESS;
    }

    /**
     * Scans from Y=256 downward to find a safe landing spot (solid block with
     * 2 air blocks above). Falls back to Y=64 if nothing suitable is found.
     */
    public static int findSafeY(ServerLevel level, BlockPos column) {
        int minY = level.getMinBuildHeight();
        for (int y = Math.min(256, level.getMaxBuildHeight() - 1); y > minY; y--) {
            BlockPos feet = new BlockPos(column.getX(), y, column.getZ());
            BlockPos below = feet.below();
            BlockState ground = level.getBlockState(below);
            if (ground.isAir() || ground.is(Blocks.LAVA) || ground.liquid()) continue;
            BlockState atFeet = level.getBlockState(feet);
            BlockState atHead = level.getBlockState(feet.above());
            if (!atFeet.blocksMotion() && !atHead.blocksMotion()) {
                return y;
            }
        }
        return Math.max(minY + 1, 64);
    }

    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        return null;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return null;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return null;
    }

    @Override
    protected float getSoundVolume() {
        return 0.0f;
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return false;
    }

    @Override
    public boolean causeFallDamage(float dist, float mult, DamageSource source) {
        return false;
    }

    @Nullable
    @Override
    public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob otherParent) {
        return null;
    }

    @Override
    public boolean checkSpawnRules(LevelAccessor level, MobSpawnType spawnType) {
        if (this.getY() < 50.0) return false;
        return level.getEntitiesOfClass(EntityAnt.class,
                this.getBoundingBox().inflate(20.0, 10.0, 20.0)).size() <= 4;
    }

    protected int findBuddies() {
        return this.level().getEntitiesOfClass(EntityAnt.class,
                this.getBoundingBox().inflate(20.0, 10.0, 20.0)).size();
    }

    @Override
    protected void customServerAiStep() {
        if (this.random.nextInt(200) == 1) {
            this.setTarget(null);
        }
        super.customServerAiStep();
    }
}
