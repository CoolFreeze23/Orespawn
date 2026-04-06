package danger.orespawn.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class EntityLunaMoth extends EntityButterfly {
    private static final int NO_CLOSEST_MATCH = 99999;
    private static final double NEAR_TARGET_DIST_SQ = 4.0;
    private static final int RANDOM_WANDER_CHANCE = 100;
    private static final int WANDER_ATTEMPTS = 25;
    private static final int WANDER_XY_RANGE = 10;
    private static final int WANDER_Y_RANGE = 6;
    private static final int WANDER_Y_BIAS = 2;
    private static final int TORCH_SCAN_RARE_CHANCE = 10;
    private static final int TORCH_SCAN_MIN_RADIUS = 2;
    private static final int TORCH_SCAN_MAX_RADIUS = 15;
    private static final int TORCH_SCAN_SKIP_AFTER = 6;
    private static final double VERTICAL_DRAG = 0.6;
    private static final double STEER_TO_TARGET_XY = 0.5;
    private static final double STEER_TO_TARGET_Y = 0.68;
    private static final double STEER_BLEND = 0.1;
    private static final float FORWARD_SPEED = 0.75f;
    private static final double BLOCK_CENTER_OFFSET = 0.5;
    private static final double FLIGHT_Y_OFFSET = 0.1;
    private static final int MIN_SPAWN_Y = 50;

    public int moth_type;
    private BlockPos currentFlightTarget = null;
    private int closestTorchDistSq = NO_CLOSEST_MATCH;
    private int targetX = 0;
    private int targetY = 0;
    private int targetZ = 0;

    public EntityLunaMoth(EntityType<? extends EntityLunaMoth> type, Level level) {
        super(type, level);
        this.moth_type = this.random.nextInt(4);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 2.0)
                .add(Attributes.MOVEMENT_SPEED, 0.1)
                .add(Attributes.ATTACK_DAMAGE, 0.0);
    }

    @Override
    public void tick() {
        super.tick();
        Vec3 motion = this.getDeltaMovement();
        this.setDeltaMovement(motion.x, motion.y * VERTICAL_DRAG, motion.z);
    }

    private boolean scanForTorches(int x, int y, int z, int dx, int dy, int dz) {
        int found = 0;
        for (int i = -dy; i <= dy; i++) {
            for (int j = -dz; j <= dz; j++) {
                BlockState state = this.level().getBlockState(new BlockPos(x + dx, y + i, z + j));
                if (state.is(Blocks.TORCH) || state.is(Blocks.WALL_TORCH)) {
                    int distSqToTorch = dx * dx + j * j + i * i;
                    if (distSqToTorch < this.closestTorchDistSq) {
                        this.closestTorchDistSq = distSqToTorch;
                        this.targetX = x + dx;
                        this.targetY = y + i;
                        this.targetZ = z + j;
                        found++;
                    }
                }
                state = this.level().getBlockState(new BlockPos(x - dx, y + i, z + j));
                if (state.is(Blocks.TORCH) || state.is(Blocks.WALL_TORCH)) {
                    int distSqToTorch = dx * dx + j * j + i * i;
                    if (distSqToTorch < this.closestTorchDistSq) {
                        this.closestTorchDistSq = distSqToTorch;
                        this.targetX = x - dx;
                        this.targetY = y + i;
                        this.targetZ = z + j;
                        found++;
                    }
                }
            }
        }
        return found != 0;
    }

    @Override
    protected void customServerAiStep() {
        if (this.isRemoved()) return;

        if (this.currentFlightTarget == null) {
            this.currentFlightTarget = this.blockPosition();
        }

        double distSq = this.currentFlightTarget.distSqr(this.blockPosition());
        if (this.random.nextInt(RANDOM_WANDER_CHANCE) == 0 || distSq < NEAR_TARGET_DIST_SQ) {
            for (int tries = WANDER_ATTEMPTS; tries > 0; tries--) {
                BlockPos newTarget = new BlockPos(
                        (int) this.getX() + this.random.nextInt(WANDER_XY_RANGE) - this.random.nextInt(WANDER_XY_RANGE),
                        (int) this.getY() + this.random.nextInt(WANDER_Y_RANGE) - WANDER_Y_BIAS,
                        (int) this.getZ() + this.random.nextInt(WANDER_XY_RANGE) - this.random.nextInt(WANDER_XY_RANGE));
                if (this.level().getBlockState(newTarget).isAir()) {
                    this.currentFlightTarget = newTarget;
                    break;
                }
            }
        } else if (!this.level().canSeeSky(this.blockPosition()) && this.random.nextInt(TORCH_SCAN_RARE_CHANCE) == 0) {
            this.closestTorchDistSq = NO_CLOSEST_MATCH;
            this.targetX = 0;
            this.targetY = 0;
            this.targetZ = 0;
            for (int i = TORCH_SCAN_MIN_RADIUS; i < TORCH_SCAN_MAX_RADIUS; i++) {
                if (this.scanForTorches((int) this.getX(), (int) this.getY(), (int) this.getZ(), i, i, i)) break;
                if (i >= TORCH_SCAN_SKIP_AFTER) i++;
            }
            if (this.closestTorchDistSq < NO_CLOSEST_MATCH) {
                this.currentFlightTarget = new BlockPos(this.targetX, this.targetY + 1, this.targetZ);
            }
        }

        double dx = this.currentFlightTarget.getX() + BLOCK_CENTER_OFFSET - this.getX();
        double dy = this.currentFlightTarget.getY() + FLIGHT_Y_OFFSET - this.getY();
        double dz = this.currentFlightTarget.getZ() + BLOCK_CENTER_OFFSET - this.getZ();

        Vec3 motion = this.getDeltaMovement();
        double mx = motion.x + (Math.signum(dx) * STEER_TO_TARGET_XY - motion.x) * STEER_BLEND;
        double my = motion.y + (Math.signum(dy) * STEER_TO_TARGET_Y - motion.y) * STEER_BLEND;
        double mz = motion.z + (Math.signum(dz) * STEER_TO_TARGET_XY - motion.z) * STEER_BLEND;
        this.setDeltaMovement(mx, my, mz);

        float targetYaw = (float) (Math.atan2(mz, mx) * 180.0 / Math.PI) - 90.0f;
        float yawDiff = Mth.wrapDegrees(targetYaw - this.getYRot());
        this.zza = FORWARD_SPEED;
        this.setYRot(this.getYRot() + yawDiff);
    }

    @Override
    public boolean checkSpawnRules(LevelAccessor level, MobSpawnType spawnType) {
        BlockState state = level.getBlockState(this.blockPosition());
        if (!state.isAir()) return false;
        if (this.level().canSeeSky(this.blockPosition())) return false;
        return this.getY() >= MIN_SPAWN_Y;
    }
}
