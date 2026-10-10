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
import net.minecraft.world.phys.Vec3;
import danger.orespawn.ModBlocks;
import danger.orespawn.OreSpawnConfig;

/**
 * The Island Too (orig IslandToo.java, 24 in 25 of the Island block's islands; WGEN-119): a stepped slab, widest at its
 * grass top, of stone with one ore of eight through it (the island's {@code blocktype}) and now and then a treasure
 * block; on top now and then water, pink and blue flowers and a small sky tree. It drifts one way of four a block at a
 * time, clearing the face it leaves and building the face it reaches, blowing up (power 5) where it meets end stone (an
 * Island's). It never despawns.
 */
public class IslandToo extends Animal {
    private int dir = 0;
    private float speed = 0.1f;
    private int width = 5;
    private int depth = 3;
    private int length = 10;
    private int timer = 42;
    private int justSpawned = 1;
    private int ticker = 0;
    private int once = 1;
    private double myX, myY, myZ;
    private int dirchange = 0;
    private int blocktype = 0;

    public IslandToo(EntityType<? extends IslandToo> type, Level level) {
        super(type, level);
        this.ticker = level.random.nextInt(50);
        this.dirchange = this.random.nextInt(5000);
    }

    @Override
    protected void registerGoals() {
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 50.0)
                .add(Attributes.MOVEMENT_SPEED, 0.0);
    }

    /** orig IslandToo.java:104-106: {@code canDespawn} false. */
    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
    }

    /** orig IslandToo.java:41-87. */
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
            int sizeFactor = OreSpawnConfig.ISLAND_SIZE_FACTOR.get();
            int speedFactor = OreSpawnConfig.ISLAND_SPEED_FACTOR.get();
            this.dir = this.random.nextInt(4);
            if (this.random.nextInt(40) != 1) {
                this.length = this.width = 1 + this.random.nextInt(5 * sizeFactor);
                this.depth = 1 + this.random.nextInt(4);
                this.speed = this.random.nextFloat() / 40.0f * (float) speedFactor;
                if (this.length * this.width * this.depth <= 64) this.speed *= 2.0f;
                if (this.length * this.width * this.depth <= 32) this.speed *= 2.0f;
            } else {
                this.length = this.width = 5 + this.random.nextInt(8 * sizeFactor);
                this.depth = 3 + this.random.nextInt(6);
                this.speed = this.random.nextFloat() / 150.0f * (float) speedFactor;
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
            this.dir = this.random.nextInt(4);
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

    /** orig IslandToo.java:134-158: struck, the island first sits on its block's centre; the hurt answers false. */
    @Override
    public boolean hurt(DamageSource source, float amount) {
        int ix = (int) this.getX();
        int iz = (int) this.getZ();
        double x = ix < 0 ? ix - 0.5 : ix + 0.5;
        double z = iz < 0 ? iz - 0.5 : iz + 0.5;
        this.setPos(x, this.getY(), z);
        super.hurt(source, amount);
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

    /**
     * The top's cell over air: one in 5,000 water; else grass, and over it one in 30 a pink or a blue flower or, failing
     * that, one in 100 a small sky tree (orig :180-198, :403-421). Returns false where the cell was bedrock: the island
     * is gone.
     */
    private boolean topCell(int ix, int y, int iz) {
        Block bid = at(ix, y, iz);
        if (bid == Blocks.AIR) {
            if (this.random.nextInt(5000) == 1) {
                this.set(ix, y, iz, Blocks.WATER);
                return true;
            }
            this.fastSet(ix, y, iz, Blocks.GRASS_BLOCK);
            if (this.random.nextInt(30) == 1) {
                if (at(ix, y + 1, iz) != Blocks.AIR) return true;
                if (this.random.nextInt(2) == 1) {
                    this.set(ix, y + 1, iz, ModBlocks.FLOWER_PINK.get());
                } else {
                    this.set(ix, y + 1, iz, ModBlocks.FLOWER_BLUE.get());
                }
                return true;
            }
            if (this.random.nextInt(100) != 1 || at(ix, y + 1, iz) != Blocks.AIR) return true;
            smallTree(ix, y + 1, iz);
            return true;
        }
        if (bid != Blocks.BEDROCK) return true;
        this.discard();
        return false;
    }

    /** orig IslandToo.java:160-208. */
    private void createIsland() {
        int xoff = 0;
        int zoff = 0;
        if (this.getX() < 0.0) xoff = 1;
        if (this.getZ() < 0.0) zoff = 1;
        int y = (int) this.getY();
        for (int k = 0; k <= this.depth; ++k) {
            int il = this.length / (this.depth - k + 1);
            if (il < 1) il = 1;
            for (int i = -il; i <= il; ++i) {
                for (int j = -il; j <= il; ++j) {
                    int ix = (int) this.getX() + j - xoff;
                    int iz = (int) this.getZ() + i - zoff;
                    if (k == this.depth) {
                        if (!this.topCell(ix, y + k, iz)) return;
                        continue;
                    }
                    this.mySetBlock(ix, y + k, iz);
                }
            }
        }
        this.set((int) this.getX() - xoff, y, (int) this.getZ() - zoff, Blocks.AIR);
    }

    /** orig IslandToo.java:210-266: stone, the island's ore through it, now and then a treasure block. */
    private void mySetBlock(int ix, int iy, int iz) {
        Block bid = Blocks.STONE;
        if (this.blocktype == 0) this.blocktype = 1 + this.random.nextInt(8);
        if (this.blocktype == 1 && this.random.nextInt(5) == 1) bid = Blocks.COAL_ORE;
        if (this.blocktype == 2 && this.random.nextInt(10) == 1) bid = Blocks.IRON_ORE;
        if (this.blocktype == 3 && this.random.nextInt(20) == 1) bid = Blocks.EMERALD_ORE;
        if (this.blocktype == 4 && this.random.nextInt(30) == 1) bid = ModBlocks.ORE_TITANIUM.get();
        if (this.blocktype == 5 && this.random.nextInt(30) == 1) bid = ModBlocks.ORE_URANIUM.get();
        if (this.blocktype == 6 && this.random.nextInt(30) == 1) bid = ModBlocks.ORE_RUBY.get();
        if (this.blocktype == 7 && this.random.nextInt(30) == 1) bid = ModBlocks.ORE_AMETHYST.get();
        if (this.blocktype == 8 && this.random.nextInt(20) == 1) bid = Blocks.GOLD_ORE;
        if (bid == Blocks.STONE) {
            if (this.random.nextInt(3000) == 1) bid = ModBlocks.BLOCK_ENDER_PEARL.get();
            if (this.random.nextInt(3000) == 2) bid = ModBlocks.BLOCK_EYE_OF_ENDER.get();
            if (this.random.nextInt(3000) == 3) bid = ModBlocks.BLOCK_AMETHYST.get();
            if (this.random.nextInt(3000) == 4) bid = ModBlocks.BLOCK_RUBY.get();
            if (this.random.nextInt(3000) == 5) bid = ModBlocks.BLOCK_URANIUM.get();
            if (this.random.nextInt(3000) == 6) bid = ModBlocks.BLOCK_TITANIUM.get();
            if (this.random.nextInt(3000) == 7) bid = Blocks.GOLD_BLOCK;
            if (this.random.nextInt(3000) == 8) bid = Blocks.DIAMOND_BLOCK;
        }
        this.fastSet(ix, iy, iz, bid);
    }

    private static boolean isFlower(Block b) {
        return b == ModBlocks.FLOWER_PINK.get() || b == ModBlocks.FLOWER_BLUE.get() || b == ModBlocks.FLOWER_BLACK.get()
                || b == ModBlocks.FLOWER_SCARY.get();
    }

    /** orig IslandToo.java:268-438: the drift, the trailing face cleared and the leading face built. */
    private void updateIsland() {
        int xoff = 0;
        int zoff = 0;
        if (this.dir == 0) {
            this.myZ -= (double) this.speed;
        } else if (this.dir == 1) {
            this.myZ += (double) this.speed;
        } else if (this.dir == 2) {
            this.myX += (double) this.speed;
        } else {
            this.myX -= (double) this.speed;
        }
        int ke;
        int ks;
        int je;
        int js;
        int mx = (int) this.myX;
        int mz = (int) this.myZ;
        int px = (int) this.getX();
        int pz = (int) this.getZ();
        int y = (int) this.getY();
        if (mx == px && mz == pz) return;
        Block bid;
        int ix;
        int iz;
        int il;
        // the face it leaves
        if (this.dir == 0) {
            js = 1; je = 1; ks = -1; ke = 1;
        } else if (this.dir == 1) {
            js = -1; je = -1; ks = -1; ke = 1;
        } else if (this.dir == 2) {
            js = -1; je = 1; ks = -1; ke = -1;
        } else {
            js = -1; je = 1; ks = 1; ke = 1;
        }
        if (this.getX() < 0.0) xoff = 1;
        if (this.getZ() < 0.0) zoff = 1;
        for (int i = 0; i <= this.depth; ++i) {
            il = this.length / (this.depth - i + 1);
            if (il < 1) il = 1;
            for (int j = js * il; j <= je * il; ++j) {
                for (int k = ks * il; k <= ke * il; ++k) {
                    ix = (int) this.getX() + k - xoff;
                    iz = (int) this.getZ() + j - zoff;
                    if (i == this.depth) {
                        bid = at(ix, y + i + 1, iz);
                        if (isFlower(bid)) this.fastSet(ix, y + i + 1, iz, Blocks.AIR);
                        if (bid == Blocks.WATER) this.set(ix, y + i, iz, Blocks.AIR);
                        if (bid == ModBlocks.SKY_TREE_LOG.get()) {
                            this.set(ix, y + i + 1, iz, Blocks.AIR);
                            bid = at(ix, y + i + 2, iz);
                            if (bid == ModBlocks.SKY_TREE_LOG.get()) {
                                this.set(ix, y + i + 2, iz, Blocks.AIR);
                                bid = at(ix, y + i + 3, iz);
                                if (bid == ModBlocks.SKY_TREE_LOG.get()) this.set(ix, y + i + 3, iz, Blocks.AIR);
                            }
                        }
                        if (at(ix, y + i, iz) == Blocks.WATER) this.set(ix, y + i, iz, Blocks.AIR);
                    }
                    this.fastSet(ix, y + i, iz, Blocks.AIR);
                }
            }
        }
        this.mySetBlock((int) this.getX() - xoff, y, (int) this.getZ() - zoff);
        // move: the whole block the drift reached, centred
        double nx = mx;
        nx = this.myX < 0.0 ? nx - 0.5 : nx + 0.5;
        double nz = mz;
        nz = this.myZ < 0.0 ? nz - 0.5 : nz + 0.5;
        this.setPos(nx, this.getY(), nz);
        // the face it reaches
        if (this.dir == 0) {
            js = -1; je = -1; ks = -1; ke = 1;
        } else if (this.dir == 1) {
            js = 1; je = 1; ks = -1; ke = 1;
        } else if (this.dir == 2) {
            js = -1; je = 1; ks = 1; ke = 1;
        } else {
            js = -1; je = 1; ks = -1; ke = -1;
        }
        zoff = 0;
        xoff = 0;
        if (this.getX() < 0.0) xoff = 1;
        if (this.getZ() < 0.0) zoff = 1;
        this.set((int) this.getX() - xoff, y, (int) this.getZ() - zoff, Blocks.AIR);
        for (int i = 0; i <= this.depth; ++i) {
            il = this.length / (this.depth - i + 1);
            if (il < 1) il = 1;
            for (int j = js * il; j <= je * il; ++j) {
                for (int k = ks * il; k <= ke * il; ++k) {
                    ix = (int) this.getX() + k - xoff;
                    iz = (int) this.getZ() + j - zoff;
                    if (i == this.depth) {
                        if (!this.topCell(ix, y + i, iz)) return;
                        continue;
                    }
                    bid = at(ix, y + i, iz);
                    if (bid == Blocks.END_STONE) {
                        // orig :427-430, createExplosion(this, ..., 5.0f, true): blocks broken whatever the
                        // mobGriefing rule (TNT's interaction), the original passing true, not the rule
                        this.level().explode(this, (double) ix, this.getY() + (double) i, (double) iz, 5.0f,
                                Level.ExplosionInteraction.TNT);
                        continue;
                    }
                    this.mySetBlock(ix, y + i, iz);
                }
            }
        }
        this.set((int) this.getX() - xoff, y, (int) this.getZ() - zoff, Blocks.AIR);
    }

    /**
     * orig Trees.SmallTree (Trees.java:335-382), as the Island Too's top grows it: on grass, dirt or farmland under it, a
     * sky tree log one to three high with apple leaves over it and round its top; elsewhere nothing (the original's
     * other branches all return), set with flags 2.
     */
    private void smallTree(int x, int y, int z) {
        Block below = at(x, y - 1, z);
        if (below != Blocks.GRASS_BLOCK && below != Blocks.DIRT && below != Blocks.FARMLAND) {
            Block b2 = at(x, y - 2, z);
            if (b2 != Blocks.GRASS_BLOCK && b2 != Blocks.DIRT && b2 != Blocks.FARMLAND) {
                Block b3 = at(x, y - 3, z);
                if (b3 != Blocks.GRASS_BLOCK && b3 != Blocks.DIRT && b3 != Blocks.FARMLAND) {
                    this.level().setBlock(new BlockPos(x, y, z), Blocks.AIR.defaultBlockState(), 2);
                }
            }
            return;
        }
        int realy = y - 1;
        Block log = ModBlocks.SKY_TREE_LOG.get();
        Block leaves = ModBlocks.APPLE_LEAVES.get();
        if (at(x, realy + 1, z) == Blocks.AIR) treeSet(x, realy + 1, z, log);
        if (this.random.nextInt(2) == 1) {
            if (at(x, realy + 2, z) == Blocks.AIR) treeSet(x, realy + 2, z, log);
            if (this.random.nextInt(2) == 1) {
                if (at(x, realy + 3, z) == Blocks.AIR) treeSet(x, realy + 3, z, log);
            } else {
                --realy;
            }
        } else {
            realy -= 2;
        }
        if (at(x, realy + 4, z) == Blocks.AIR) treeSet(x, realy + 4, z, leaves);
        for (int i = -1; i <= 1; ++i) {
            for (int j = -1; j <= 1; ++j) {
                if (at(x + i, realy + 3, z + j) != Blocks.AIR) continue;
                treeSet(x + i, realy + 3, z + j, leaves);
            }
        }
    }

    private void treeSet(int x, int y, int z, Block block) {
        this.level().setBlock(new BlockPos(x, y, z), block.defaultBlockState(), 2);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("JustSpawned", this.justSpawned);
        tag.putInt("Iwidth", this.width);
        tag.putInt("Idepth", this.depth);
        tag.putInt("Ilength", this.length);
        tag.putFloat("Ispeed", this.speed);
        tag.putInt("Idir", this.dir);
        tag.putInt("Iblocktype", this.blocktype);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.justSpawned = tag.getInt("JustSpawned");
        this.width = tag.getInt("Iwidth");
        this.depth = tag.getInt("Idepth");
        this.length = tag.getInt("Ilength");
        this.speed = tag.getFloat("Ispeed");
        this.dir = tag.getInt("Idir");
        this.blocktype = tag.getInt("Iblocktype");
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
