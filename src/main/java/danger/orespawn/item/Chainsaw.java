package danger.orespawn.item;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import danger.orespawn.ModBlocks;
import danger.orespawn.ModSounds;
import danger.orespawn.OreSpawnConfig;
import danger.orespawn.WeaponStats;

import java.util.List;

/**
 * Chainsaw, ported from the 1.7.10 UltimateSword class's chainsaw-only paths
 * (orig OreSpawnMain.java:1650 — {@code new UltimateSword(..., toolCHAINSAW)}).
 *
 * <p>Signature mechanics (orig UltimateSword.java):</p>
 * <ul>
 *   <li>:45-46 — no baked enchants;</li>
 *   <li>:62-68 — saw sound on swing, with a 50-tick re-trigger timer;</li>
 *   <li>:91-110 — flame/smoke/spark particles beside the player while the
 *   swing timer runs (client side);</li>
 *   <li>:148-151,163-174 — left-click swings damage every visible living
 *   entity within 5 blocks for the chainsaw damage stat (56);</li>
 *   <li>:351-371 — breaking a block crushes wood/leaf-type blocks in an
 *   11x16x11 box (x/z -5..5, y -5..10), dropping each as an item at a random
 *   offset; if the broken block was leaf-like, only leaf-like blocks crush;</li>
 *   <li>:383-394 — mining speed = chainsaw efficiency stat (75) against
 *   wood/plant/crushable blocks, else 2.0.</li>
 * </ul>
 */
public class Chainsaw extends UltimateSword {

    // orig OreSpawnMain.java:1516 — get_weaponstats("Chainsaw", 3, 1500, 10, 56, 75);
    // the config layer for these stats is Phase E scope.
    private static final float AOE_DAMAGE = WeaponStats.CHAINSAW.damage();
    private static final float SAW_EFFICIENCY = WeaponStats.CHAINSAW.efficiency();
    // orig UltimateSword.java:65 — swingtimer = 50
    private static final int SWING_TIMER_TICKS = 50;

    private int swingTimer = 0;
    // orig UltimateSword.java:385 — set from the last block the saw was held
    // against, selects the leaf-only crush mode (same single-instance state
    // as the original item object)
    private boolean leaf = false;

    public Chainsaw(Tier tier, Item.Properties properties) {
        super(tier, Variant.NONE, properties);
    }

    /** orig UltimateSword.java:62-68 — short saw sound, throttled to 50 ticks. */
    @Override
    public boolean onEntitySwing(ItemStack stack, LivingEntity entity) {
        if (entity != null && swingTimer == 0) {
            entity.level().playSound(null, entity.getX(), entity.getY(), entity.getZ(),
                    ModSounds.CHAINSAWSHORT.get(), SoundSource.PLAYERS,
                    1.0f, entity.level().random.nextFloat() * 0.2f + 0.9f);
            swingTimer = SWING_TIMER_TICKS;
        }
        return false;
    }

    /** orig UltimateSword.java:91-110 — timer countdown + running-saw particles. */
    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        if (swingTimer > 0) {
            --swingTimer;
        }
        if (level.isClientSide && swingTimer > 0 && entity != null) {
            float dx = (float) Math.cos(Math.toRadians(entity.getYRot() + 90.0f + 45.0f));
            float dz = (float) Math.sin(Math.toRadians(entity.getYRot() + 90.0f + 45.0f));
            if (level.random.nextInt(8) == 0) {
                level.addParticle(ParticleTypes.FLAME, entity.getX() + dx, entity.getY(), entity.getZ() + dz,
                        (level.random.nextFloat() - level.random.nextFloat()) / 20.0f,
                        level.random.nextFloat() / 10.0f,
                        (level.random.nextFloat() - level.random.nextFloat()) / 20.0f);
            }
            if (level.random.nextInt(2) == 0) {
                level.addParticle(ParticleTypes.SMOKE, entity.getX() + dx, entity.getY(), entity.getZ() + dz,
                        (level.random.nextFloat() - level.random.nextFloat()) / 20.0f,
                        level.random.nextFloat() / 10.0f,
                        (level.random.nextFloat() - level.random.nextFloat()) / 20.0f);
            }
            if (level.random.nextInt(10) == 0) {
                level.addParticle(ParticleTypes.FIREWORK, entity.getX() + dx, entity.getY(), entity.getZ() + dz,
                        (level.random.nextFloat() - level.random.nextFloat()) / 20.0f,
                        level.random.nextFloat() / 5.0f,
                        (level.random.nextFloat() - level.random.nextFloat()) / 20.0f);
            }
        }
    }

    /**
     * orig UltimateSword.java:148-151 — left-clicking (with or without a
     * direct target) revs the AoE: every suitable living entity within 5
     * blocks and in line of sight takes the chainsaw damage stat.
     */
    @Override
    public boolean onLeftClickEntity(ItemStack stack, Player player, Entity entity) {
        boolean cancelled = super.onLeftClickEntity(stack, player, entity);
        if (player != null) {
            findSomethingToHit(player);
        }
        return cancelled;
    }

    /** orig UltimateSword.java:163-174 — 5-block AoE sweep. */
    private void findSomethingToHit(Player player) {
        List<LivingEntity> targets = player.level().getEntitiesOfClass(LivingEntity.class,
                player.getBoundingBox().inflate(5.0, 5.0, 5.0));
        for (LivingEntity target : targets) {
            if (!isSuitableTarget(target, player)) continue;
            target.hurt(player.damageSources().playerAttack(player), AOE_DAMAGE);
        }
    }

    /**
     * orig UltimateSword.java:176-196 — skips self, dead entities and (with
     * pvp off, via the shared onLeftClickEntity guard) players/companions/
     * tamed pets; then the sight test (:195): in classic 1.7.10's own
     * {@link #myCanSee} walk (:198-247); in modern, under {@code [modern]
     * chainsawSweepVanillaSight} (MOD-037, default on, read live per swing),
     * vanilla's collision ray {@code Player.hasLineOfSight} (eye to eye, the
     * COLLIDER clip — fluids and collision-less blocks never stop it) — the
     * mapping the port had carried unrecorded (ITEM-070, ruled B2 2026-09-05).
     */
    private boolean isSuitableTarget(LivingEntity target, Player player) {
        if (target == player || !target.isAlive()) return false;
        if (super.onLeftClickEntity(ItemStack.EMPTY, player, target)) return false;
        if (OreSpawnConfig.chainsawSweepVanillaSight()) return player.hasLineOfSight(target); // MOD-037 — modern: the vanilla ray
        return myCanSee(player, target);                                                     // orig :195 — MyCanSee(e, player), the 1.7.10 walk (classic; ITEM-070)
    }

    /**
     * orig UltimateSword.java:198-247 {@code MyCanSee(e, player)} — the chainsaw's own sight test, a hand-rolled block march
     * in floats (the Cater Killer's / King's / Queen's / Molenoid's class of walk): from the SERVER player's position at
     * {@code posY + 1.4f} (:200-203 — {@code EntityPlayerMP.posY} is the feet, the ENT-S-120 premise check of 2026-09-05:
     * 1.4 above the feet, 0.22 below the eyes) toward the target's MID-BODY, {@code posY + height / 2} (:205-207), in ten
     * steps of a tenth of the offset each (:199, :205-207); should any axis step exceed one block, the other two are
     * divided by it, the sample count scaled by it with an {@code (int)} count (:208-240 — cumulative over x, y, z, each
     * component then clamped to ±1; inside the 5-block sweep box no axis reaches 1 for a target narrower than about 9.4
     * blocks, so the walk is exactly ten samples with the tenth ON the mid-body point); each sample pre-incremented and read
     * with {@code (int)} casts (:242 — truncation toward zero, BUG-027 VERIFIED-CORRECT faithful / MOD-024's floor a modern
     * opt-in: at x &lt; 0 or z &lt; 0 the column one block toward the origin is read; at y &lt; 0 — the modern world reaches
     * −64, 1.7.10's floor was 0 — the cell above the true cell on a fractional negative y, the same rule; below the world's
     * minimum build height {@code getBlockState} answers void air, as 1.7.10's {@code getBlock} answered air outside 0..255
     * (recalled, ITEM-070's caveat iii) — both {@code isAir()}), and passed only through AIR (:243-244 — {@code bid ==
     * Blocks.air} by identity; {@code state.isAir()} here, the TheQueen shape: cave air and void air fold in, no 1.7.10
     * counterpart): every other block — collision-less plants, crops, saplings, torches, snow layers, carpets, cobwebs, fire,
     * signs, water, lava — stops it, and a block the segment enters BETWEEN two samples (a trunk or canopy corner) is never
     * examined. The player standing in a 2-block plant, a cobweb or water reads its own head cell at the first sample and
     * sweeps nothing (ITEM-070's row 10, kept as ruled). The argument order is the port's ({@code (player, e)}); orig's was
     * {@code (e, player)}. ITEM-070 (B2, 2026-09-05): the classic sight; modern keeps the vanilla ray under MOD-037.
     */
    static boolean myCanSee(Player player, LivingEntity e) {
        int nblks = 10;                                                      // orig :199
        double cx = player.getX();                                           // orig :200
        double cz = player.getZ();                                           // orig :201
        float startx = (float) cx;                                           // orig :202
        float starty = (float) (player.getY() + (double) 1.4f);              // orig :203 — the server player's posY (the feet) + 1.4f
        float startz = (float) cz;                                           // orig :204
        float dx = (float) ((e.getX() - (double) startx) / 10.0);            // orig :205
        float dy = (float) ((e.getY() + (double) (e.getBbHeight() / 2.0f) - (double) starty) / 10.0); // orig :206 — the target's mid-body
        float dz = (float) ((e.getZ() - (double) startz) / 10.0);            // orig :207
        if ((double) Math.abs(dx) > 1.0) {                                   // orig :208-218
            dy /= Math.abs(dx);
            dz /= Math.abs(dx);
            nblks = (int) ((float) nblks * Math.abs(dx));
            if (dx > 1.0f) dx = 1.0f;
            if (dx < -1.0f) dx = -1.0f;
        }
        if ((double) Math.abs(dy) > 1.0) {                                   // orig :219-229
            dx /= Math.abs(dy);
            dz /= Math.abs(dy);
            nblks = (int) ((float) nblks * Math.abs(dy));
            if (dy > 1.0f) dy = 1.0f;
            if (dy < -1.0f) dy = -1.0f;
        }
        if ((double) Math.abs(dz) > 1.0) {                                   // orig :230-240
            dy /= Math.abs(dz);
            dx /= Math.abs(dz);
            nblks = (int) ((float) nblks * Math.abs(dz));
            if (dz > 1.0f) dz = 1.0f;
            if (dz < -1.0f) dz = -1.0f;
        }
        for (int i = 0; i < nblks; ++i) {                                    // orig :241-245
            startx += dx;
            starty += dy;
            startz += dz;
            BlockState state = player.level().getBlockState(new BlockPos((int) startx, (int) starty, (int) startz)); // orig :242 — pre-increment, then the (int) casts (BUG-027)
            if (state.isAir()) continue;                                     // orig :243 — Blocks.air alone passes
            return false;                                                    // orig :244
        }
        return true;                                                         // orig :246
    }

    /** orig UltimateSword.java:351-371 — crush wood/leaves in an 11x16x11 box. */
    @Override
    public boolean mineBlock(ItemStack stack, Level level, BlockState state, BlockPos pos, LivingEntity miner) {
        if (!level.isClientSide) {
            boolean leafOnly = this.leaf;
            for (int i = -5; i <= 5; ++i) {
                for (int j = -5; j <= 10; ++j) {
                    for (int k = -5; k <= 5; ++k) {
                        BlockPos crushPos = pos.offset(i, j, k);
                        BlockState crush = level.getBlockState(crushPos);
                        if (leafOnly ? !isLeaves(crush) : !canCrush(crush)) continue;
                        dropItemRand(level, crush, crushPos);
                        level.setBlock(crushPos, Blocks.AIR.defaultBlockState(), 3);
                    }
                }
            }
        }
        return super.mineBlock(stack, level, state, pos, miner);
    }

    /** orig UltimateSword.java:373-381 — item pops at a randomly offset spot. */
    private void dropItemRand(Level level, BlockState state, BlockPos pos) {
        ItemStack drop = new ItemStack(state.getBlock());
        if (drop.isEmpty()) return;
        ItemEntity item = new ItemEntity(level,
                pos.getX() + level.random.nextInt(5) - level.random.nextInt(5),
                pos.getY() + 1.0 + level.random.nextInt(5),
                pos.getZ() + level.random.nextInt(5) - level.random.nextInt(5),
                drop);
        level.addFreshEntity(item);
    }

    /**
     * orig UltimateSword.java:253-312 — chainsaw crush set: web, logs,
     * leaves, planks, saplings, tall grass, cactus plus the mod's logs and
     * leaves (vanilla families mapped to their block tags).
     */
    private boolean canCrush(BlockState state) {
        return state.is(Blocks.COBWEB)
                || state.is(BlockTags.LOGS)
                || state.is(BlockTags.LEAVES)
                || state.is(BlockTags.PLANKS)
                || state.is(BlockTags.SAPLINGS)
                || state.is(Blocks.SHORT_GRASS)
                || state.is(Blocks.CACTUS)
                || state.is(ModBlocks.CRYSTAL_PLANKS.get())
                || state.is(ModBlocks.APPLE_LEAVES.get())
                || state.is(ModBlocks.SKY_TREE_LOG.get())
                || state.is(ModBlocks.DUPLICATOR_LOG.get())
                || state.is(ModBlocks.EXPERIENCE_LEAVES.get())
                || state.is(ModBlocks.SCARY_LEAVES.get())
                || state.is(ModBlocks.CHERRY_LEAVES.get())
                || state.is(ModBlocks.PEACH_LEAVES.get())
                || state.is(ModBlocks.CRYSTAL_LEAVES.get())
                || state.is(ModBlocks.CRYSTAL_LEAVES_2.get())
                || state.is(ModBlocks.CRYSTAL_LEAVES_3.get())
                || state.is(ModBlocks.CRYSTAL_TREE_LOG.get());
    }

    /** orig UltimateSword.java:314-349 — leaf-like set for the leaf-only mode. */
    private boolean isLeaves(BlockState state) {
        return state.is(Blocks.COBWEB)
                || state.is(BlockTags.LEAVES)
                || state.is(BlockTags.SAPLINGS)
                || state.is(Blocks.SHORT_GRASS)
                || state.is(ModBlocks.APPLE_LEAVES.get())
                || state.is(ModBlocks.EXPERIENCE_LEAVES.get())
                || state.is(ModBlocks.SCARY_LEAVES.get())
                || state.is(ModBlocks.CHERRY_LEAVES.get())
                || state.is(ModBlocks.PEACH_LEAVES.get())
                || state.is(ModBlocks.CRYSTAL_LEAVES.get())
                || state.is(ModBlocks.CRYSTAL_LEAVES_2.get())
                || state.is(ModBlocks.CRYSTAL_LEAVES_3.get());
    }

    /** orig UltimateSword.java:383-394 — saw efficiency vs wood/plant blocks. */
    @Override
    public float getDestroySpeed(ItemStack stack, BlockState state) {
        this.leaf = isLeaves(state);
        if (state.is(BlockTags.LOGS) || state.is(BlockTags.PLANKS) || state.is(BlockTags.LEAVES)
                || state.is(BlockTags.SAPLINGS) || canCrush(state)) {
            return SAW_EFFICIENCY;
        }
        return 2.0f;
    }
}
