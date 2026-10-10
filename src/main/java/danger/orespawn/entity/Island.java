package danger.orespawn.entity;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import danger.orespawn.ModEntities;
import danger.orespawn.OreSpawnConfig;

/**
 * The Island (orig Island.java, the one in 25 of the Island block's islands; WGEN-119): a round flying island of end
 * stone flecked with diamond ore, mycelium on top with brown and red mushrooms, now and then a lava block, that drifts
 * on its heading a block at a time, taking its blocks with it: each step it clears its rings, moves, and builds them
 * again, blowing up (power 5) where it meets stone. A Triffid comes aboard now and then. It never despawns.
 */
public class Island extends Animal {
    /** orig Island.java:138: the rings' step round, a sixtieth of a turn. */
    private static final double DELTA_DIR = 0.10471975333333333;
    /** orig Island.java:139: the rings' step out, the float 0.35f widened. */
    private static final double DELTA_MAG = 0.35f;
    private float dir = 0.0f;
    private float speed = 0.1f;
    private int radius = 5;
    private int depth = 3;
    private int timer = 73;
    private int justSpawned = 1;
    private int ticker = 0;
    private int once = 1;
    private double myX, myY, myZ;
    private int dirchange;

    public Island(EntityType<? extends Island> type, Level level) {
        super(type, level);
        this.ticker = level.random.nextInt(50);
        this.dirchange = this.random.nextInt(2500);
    }

    @Override
    protected void registerGoals() {
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 50.0)
                .add(Attributes.MOVEMENT_SPEED, 0.0);
    }

    /** orig Island.java:111-113: {@code canDespawn} false. */
    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
    }

    /** orig Island.java:48-94. */
    @Override
    public void tick() {
        super.tick();
        this.setDeltaMovement(Vec3.ZERO);

        if (this.level().isClientSide) return;

        if (this.once != 0) {
            this.myX = this.getX();
            this.myY = this.getY();
            this.myZ = this.getZ();
            this.once = 0;
        }

        if (this.justSpawned != 0) {
            int speedFactor = OreSpawnConfig.ISLAND_SPEED_FACTOR.get();
            this.dir = this.random.nextFloat() * (float) Math.PI;
            if (this.random.nextInt(2) == 1) this.dir *= -1.0f;
            if (this.random.nextInt(40) != 1) {
                this.radius = 3 + this.random.nextInt(4);
                this.depth = 2 + this.random.nextInt(3);
                this.speed = this.random.nextFloat() / 50.0f * (float) speedFactor;
            } else {
                this.radius = 6 + this.random.nextInt(5);
                this.depth = 3 + this.random.nextInt(4);
                this.speed = this.random.nextFloat() / 200.0f * (float) speedFactor;
            }
            this.createIsland();
            if (this.isRemoved()) return;
            this.ticker = this.random.nextInt(50);
            this.dirchange = this.random.nextInt(10000);
        }

        ++this.ticker;
        if (this.ticker >= this.timer) {
            this.updateIsland();
            if (this.isRemoved()) return;
            this.ticker = 0;
        }

        --this.dirchange;
        if (this.dirchange <= 0) {
            this.dirchange = this.random.nextInt(5000);
            this.dir = this.random.nextFloat() * (float) Math.PI;
            if (this.random.nextInt(2) == 1) this.dir *= -1.0f;
        }

        this.justSpawned = 0;
    }

    @Override
    public void aiStep() {
        if (this.level().isClientSide) super.aiStep();
    }

    @Override
    protected void customServerAiStep() {
    }

    @Override
    public boolean causeFallDamage(float fallDistance, float multiplier, DamageSource source) {
        return false;
    }

    private Block at(int x, int y, int z) {
        return this.level().getBlockState(new BlockPos(x, y, z)).getBlock();
    }

    /** orig world.setBlock: flags 3. */
    private void set(int x, int y, int z, Block block) {
        this.level().setBlock(new BlockPos(x, y, z), block.defaultBlockState(), 3);
    }

    /** orig FastSetBlock: OreSpawnMain.setBlockFast with flags 3, the block set without a light update. */
    private void fastSet(int x, int y, int z, Block block) {
        this.level().setBlock(new BlockPos(x, y, z), block.defaultBlockState(), 3);
    }

    /** The top ring's cell over air: mycelium, one in 5,000 lava, one in 20 a mushroom over it (orig :157-171). */
    private void topCell(int ix, int y, int iz) {
        if (this.random.nextInt(5000) == 1) {
            this.set(ix, y, iz, Blocks.LAVA);
            return;
        }
        this.fastSet(ix, y, iz, Blocks.MYCELIUM);
        if (this.random.nextInt(20) != 1 || at(ix, y + 1, iz) != Blocks.AIR) return;
        if (this.random.nextInt(2) == 1) {
            this.set(ix, y + 1, iz, Blocks.BROWN_MUSHROOM);
        } else {
            this.set(ix, y + 1, iz, Blocks.RED_MUSHROOM);
        }
    }

    /** orig Island.java:137-192. */
    private void createIsland() {
        int ixlast;
        int izlast;
        int xoff = 0;
        int zoff = 0;
        int y = (int) this.getY();
        for (int i = 0; i < this.depth; ++i) {
            izlast = 0;
            ixlast = 0;
            for (double curdir = -3.1415926; curdir < 3.1415926; curdir += DELTA_DIR) {
                double tradius = this.radius;
                tradius /= (double) (i + 1);
                for (double h = 0.75; h < tradius; h += DELTA_MAG) {
                    int ix = (int) (this.getX() + Math.cos(curdir + (double) this.dir) * h);
                    int iz = (int) (this.getZ() + Math.sin(curdir + (double) this.dir) * h);
                    if (ix == ixlast && iz == izlast) continue;
                    ixlast = ix;
                    izlast = iz;
                    if (i == 0) {
                        Block bid = at(ix, y - i + 1, iz);
                        if (bid == Blocks.AIR) {
                            this.topCell(ix, y - i + 1, iz);
                            continue;
                        }
                        if (bid != Blocks.BEDROCK) continue;
                        this.discard();
                        return;
                    }
                    if (this.random.nextInt(10) == 1) {
                        this.fastSet(ix, y - i + 1, iz, Blocks.DIAMOND_ORE);
                        continue;
                    }
                    this.fastSet(ix, y - i + 1, iz, Blocks.END_STONE);
                }
            }
        }
        if (this.getX() < 0.0) xoff = -1;
        if (this.getZ() < 0.0) zoff = -1;
        this.set((int) this.getX() + xoff, y, (int) this.getZ() + zoff, Blocks.AIR);
        this.fastSet((int) this.getX() + xoff, y, (int) this.getZ() + zoff, Blocks.AIR);
    }

    /** orig Island.java:194-323: the drift, the island's blocks moved with it. */
    private void updateIsland() {
        int ixlast;
        int izlast;
        int xoff = 0;
        int zoff = 0;
        this.myX += (double) this.speed * Math.cos(this.dir);
        this.myZ += (double) this.speed * Math.sin(this.dir);
        int mx = (int) this.myX;
        int mz = (int) this.myZ;
        int px = (int) this.getX();
        int pz = (int) this.getZ();
        int y = (int) this.getY();
        if (mx != px || mz != pz) {
            Block bid;
            int ix;
            int iz;
            double h;
            double tradius;
            double curdir;
            int i;
            // clear the island where it is, a little wider than it was built
            for (i = 0; i < this.depth; ++i) {
                izlast = 0;
                ixlast = 0;
                for (curdir = -3.3; curdir < 3.3; curdir += DELTA_DIR / 2.0) {
                    tradius = this.radius;
                    tradius /= (double) (i + 1);
                    for (h = 0.75; h < tradius; h += DELTA_MAG) {
                    }
                    if ((h -= DELTA_MAG) < 0.75) h = 0.75;
                    while (h < tradius + DELTA_MAG) {
                        ix = (int) (this.getX() + Math.cos(curdir + (double) this.dir) * h);
                        iz = (int) (this.getZ() + Math.sin(curdir + (double) this.dir) * h);
                        if (ix != ixlast || iz != izlast) {
                            ixlast = ix;
                            izlast = iz;
                            if (i == 0 && ((bid = at(ix, y + 1 + 1, iz)) == Blocks.RED_MUSHROOM
                                    || bid == Blocks.BROWN_MUSHROOM)) {
                                this.fastSet(ix, y + 1 + 1, iz, Blocks.AIR);
                            }
                            this.fastSet(ix, y - i + 1, iz, Blocks.AIR);
                        }
                        h += DELTA_MAG / 2.0;
                    }
                }
            }
            if (this.getX() < 0.0) xoff = -1;
            if (this.getZ() < 0.0) zoff = -1;
            this.set((int) this.getX() + xoff, y, (int) this.getZ() + zoff, Blocks.END_STONE);
            // move: the whole block the drift reached, centred
            double nx = (int) this.myX;
            nx = this.myX < 0.0 ? nx - 0.5 : nx + 0.5;
            double nz = (int) this.myZ;
            nz = this.myZ < 0.0 ? nz - 0.5 : nz + 0.5;
            this.setPos(nx, this.getY(), nz);
            // build it again there
            for (i = 0; i < this.depth; ++i) {
                izlast = 0;
                ixlast = 0;
                for (curdir = -3.1415926; curdir < 3.1415926; curdir += DELTA_DIR) {
                    tradius = this.radius;
                    tradius /= (double) (i + 1);
                    for (h = 0.75; h < tradius; h += DELTA_MAG) {
                    }
                    if ((h -= DELTA_MAG * 3.0) < 0.75) h = 0.75;
                    while (h < tradius) {
                        ix = (int) (this.getX() + Math.cos(curdir + (double) this.dir) * h);
                        iz = (int) (this.getZ() + Math.sin(curdir + (double) this.dir) * h);
                        if (ix != ixlast || iz != izlast) {
                            ixlast = ix;
                            izlast = iz;
                            if (i == 0) {
                                bid = at(ix, y - i + 1, iz);
                                if (bid == Blocks.AIR) {
                                    this.topCell(ix, y - i + 1, iz);
                                } else if (bid == Blocks.BEDROCK) {
                                    this.discard();
                                    return;
                                }
                            } else {
                                bid = at(ix, y - i + 1, iz);
                                if (bid == Blocks.STONE) {
                                    // orig :294-297, createExplosion(this, ..., 5.0f, true): blocks broken whatever the
                                    // mobGriefing rule (TNT's interaction), the original passing true, not the rule
                                    this.level().explode(this, (double) ix, this.getY() - (double) i + 1.0, (double) iz,
                                            5.0f, Level.ExplosionInteraction.TNT);
                                } else if (this.random.nextInt(10) == 1) {
                                    this.fastSet(ix, y - i + 1, iz, Blocks.DIAMOND_ORE);
                                } else {
                                    this.fastSet(ix, y - i + 1, iz, Blocks.END_STONE);
                                }
                            }
                        }
                        h += DELTA_MAG;
                    }
                }
            }
            xoff = 0;
            if (this.getX() < 0.0) xoff = -1;
            zoff = 0;
            if (this.getZ() < 0.0) zoff = -1;
            this.set((int) this.getX() + xoff, y, (int) this.getZ() + zoff, Blocks.AIR);
            this.fastSet((int) this.getX() + xoff, y, (int) this.getZ() + zoff, Blocks.AIR);
        }
        // orig :320-322: now and then a Triffid aboard, when none is within 10 across and 5 up or down
        if (this.random.nextInt(2 + 2000 / this.timer) == 1 && this.level().getEntitiesOfClass(EntityTriffid.class,
                new AABB(this.getX() - 10.0, this.getY() - 5.0, this.getZ() - 10.0, this.getX() + 10.0,
                        this.getY() + 5.0, this.getZ() + 10.0)).isEmpty()
                && this.level() instanceof ServerLevel server) {
            EntityTriffid triffid = ModEntities.ENTITY_TRIFFID.get().create(server);
            if (triffid != null) {
                triffid.moveTo(this.getX(), this.getY() + 2.01, this.getZ(), server.random.nextFloat() * 360.0f, 0.0f);
                server.addFreshEntity(triffid);
                triffid.playAmbientSound();
            }
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("JustSpawned", this.justSpawned);
        tag.putInt("Idepth", this.depth);
        tag.putInt("Iradius", this.radius);
        tag.putFloat("Ispeed", this.speed);
        tag.putFloat("Idir", this.dir);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.justSpawned = tag.getInt("JustSpawned");
        this.depth = tag.getInt("Idepth");
        this.radius = tag.getInt("Iradius");
        this.speed = tag.getFloat("Ispeed");
        this.dir = tag.getFloat("Idir");
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return false;
    }

    @Nullable
    @Override
    public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob otherParent) {
        return null;
    }

    @Nullable
    @Override
    protected SoundEvent getAmbientSound() { return null; }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) { return null; }

    @Override
    protected SoundEvent getDeathSound() { return null; }
}
