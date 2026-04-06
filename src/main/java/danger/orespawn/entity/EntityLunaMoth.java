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
    public int moth_type;
    private BlockPos currentFlightTarget = null;
    private int closest = 99999;
    private int tx = 0, ty = 0, tz = 0;

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
        Vec3 mot = this.getDeltaMovement();
        this.setDeltaMovement(mot.x, mot.y * 0.6, mot.z);
    }

    private boolean scanForTorches(int x, int y, int z, int dx, int dy, int dz) {
        int found = 0;
        for (int i = -dy; i <= dy; i++) {
            for (int j = -dz; j <= dz; j++) {
                BlockState state = this.level().getBlockState(new BlockPos(x + dx, y + i, z + j));
                if (state.is(Blocks.TORCH) || state.is(Blocks.WALL_TORCH)) {
                    int d = dx * dx + j * j + i * i;
                    if (d < this.closest) {
                        this.closest = d; this.tx = x + dx; this.ty = y + i; this.tz = z + j; found++;
                    }
                }
                state = this.level().getBlockState(new BlockPos(x - dx, y + i, z + j));
                if (state.is(Blocks.TORCH) || state.is(Blocks.WALL_TORCH)) {
                    int d = dx * dx + j * j + i * i;
                    if (d < this.closest) {
                        this.closest = d; this.tx = x - dx; this.ty = y + i; this.tz = z + j; found++;
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
        if (this.random.nextInt(100) == 0 || distSq < 4.0) {
            for (int tries = 25; tries > 0; tries--) {
                BlockPos newTarget = new BlockPos(
                        (int) this.getX() + this.random.nextInt(10) - this.random.nextInt(10),
                        (int) this.getY() + this.random.nextInt(6) - 2,
                        (int) this.getZ() + this.random.nextInt(10) - this.random.nextInt(10));
                if (this.level().getBlockState(newTarget).isAir()) {
                    this.currentFlightTarget = newTarget;
                    break;
                }
            }
        } else if (!this.level().canSeeSky(this.blockPosition()) && this.random.nextInt(10) == 0) {
            this.closest = 99999;
            this.tx = 0; this.ty = 0; this.tz = 0;
            for (int i = 2; i < 15; i++) {
                if (this.scanForTorches((int) this.getX(), (int) this.getY(), (int) this.getZ(), i, i, i)) break;
                if (i >= 6) i++;
            }
            if (this.closest < 99999) {
                this.currentFlightTarget = new BlockPos(this.tx, this.ty + 1, this.tz);
            }
        }

        double dx = this.currentFlightTarget.getX() + 0.5 - this.getX();
        double dy = this.currentFlightTarget.getY() + 0.1 - this.getY();
        double dz = this.currentFlightTarget.getZ() + 0.5 - this.getZ();

        Vec3 mot = this.getDeltaMovement();
        double mx = mot.x + (Math.signum(dx) * 0.5 - mot.x) * 0.1;
        double my = mot.y + (Math.signum(dy) * 0.68 - mot.y) * 0.1;
        double mz = mot.z + (Math.signum(dz) * 0.5 - mot.z) * 0.1;
        this.setDeltaMovement(mx, my, mz);

        float targetYaw = (float) (Math.atan2(mz, mx) * 180.0 / Math.PI) - 90.0f;
        float yawDiff = Mth.wrapDegrees(targetYaw - this.getYRot());
        this.zza = 0.75f;
        this.setYRot(this.getYRot() + yawDiff);
    }

    @Override
    public boolean checkSpawnRules(LevelAccessor level, MobSpawnType spawnType) {
        BlockState state = level.getBlockState(this.blockPosition());
        if (!state.isAir()) return false;
        if (this.level().canSeeSky(this.blockPosition())) return false;
        return this.getY() >= 50.0;
    }
}
