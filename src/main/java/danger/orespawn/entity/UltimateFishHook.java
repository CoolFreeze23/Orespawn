package danger.orespawn.entity;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import danger.orespawn.ModItems;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;

/**
 * Bobber for the Ultimate Fishing Rod. Port of orig UltimateFishHook.java.
 *
 * The original copied vanilla 1.7.10 EntityFishHook wholesale and changed four
 * things: fire immunity (orig :76-77), lava counted as water for buoyancy and
 * bite timing (orig :271-276), custom weighted catch pools including OreSpawn
 * and lava fish (orig :41-45, :422-449), and 1-6 bonus XP per catch delivered
 * at the player (orig :411). The port keeps vanilla FishingHook mechanics
 * (bite state machine via the access-transformed catchingFish/nibble/
 * currentState) and layers those four deltas on top rather than re-copying the
 * base class.
 */
public class UltimateFishHook extends FishingHook {

    /**
     * One weighted pool entry, equivalent to 1.7.10 WeightedRandomFishable:
     * optional random damage up to {@code damagePercent} of max durability and
     * an optional random level-30 enchant (func_150709_a / func_150707_a).
     */
    private record PoolEntry(Supplier<ItemStack> stack, int weight, float damagePercent, boolean enchant) {
        PoolEntry(Supplier<ItemStack> stack, int weight) {
            this(stack, weight, 0.0f, false);
        }
    }

    // orig UltimateFishHook.java:41 — junk pool (identical to vanilla 1.7.10)
    private static final List<PoolEntry> JUNK = List.of(
            new PoolEntry(() -> new ItemStack(Items.LEATHER_BOOTS), 10, 0.9f, false),
            new PoolEntry(() -> new ItemStack(Items.LEATHER), 10),
            new PoolEntry(() -> new ItemStack(Items.BONE), 10),
            // 1.7.10 potion meta 0 = water bottle
            new PoolEntry(() -> PotionContents.createItemStack(Items.POTION, Potions.WATER), 10),
            new PoolEntry(() -> new ItemStack(Items.STRING), 5),
            new PoolEntry(() -> new ItemStack(Items.FISHING_ROD), 2, 0.9f, false),
            new PoolEntry(() -> new ItemStack(Items.BOWL), 10),
            new PoolEntry(() -> new ItemStack(Items.STICK), 5),
            // 1.7.10 dye meta 0 x10 = 10 ink sacs
            new PoolEntry(() -> new ItemStack(Items.INK_SAC, 10), 1),
            new PoolEntry(() -> new ItemStack(Items.TRIPWIRE_HOOK), 10),
            new PoolEntry(() -> new ItemStack(Items.ROTTEN_FLESH), 10));

    // orig UltimateFishHook.java:42 — treasure pool (identical to vanilla 1.7.10)
    private static final List<PoolEntry> TREASURE = List.of(
            new PoolEntry(() -> new ItemStack(Items.LILY_PAD), 1),
            new PoolEntry(() -> new ItemStack(Items.NAME_TAG), 1),
            new PoolEntry(() -> new ItemStack(Items.SADDLE), 1),
            new PoolEntry(() -> new ItemStack(Items.BOW), 1, 0.25f, true),
            new PoolEntry(() -> new ItemStack(Items.FISHING_ROD), 1, 0.25f, true),
            new PoolEntry(() -> new ItemStack(Items.BOOK), 1, 0.0f, true));

    // orig UltimateFishHook.java:43 — vanilla fish pool (cod/salmon/clownfish/pufferfish)
    private static final List<PoolEntry> VANILLA_FISH = List.of(
            new PoolEntry(() -> new ItemStack(Items.COD), 60),
            new PoolEntry(() -> new ItemStack(Items.SALMON), 25),
            new PoolEntry(() -> new ItemStack(Items.TROPICAL_FISH), 2),
            new PoolEntry(() -> new ItemStack(Items.PUFFERFISH), 13));

    // orig UltimateFishHook.java:44 — lava fish pool
    private static final List<PoolEntry> LAVA_FISH = List.of(
            new PoolEntry(() -> new ItemStack(ModItems.SUNSPOT_URCHIN.get()), 25),
            new PoolEntry(() -> new ItemStack(ModItems.LAVA_EEL.get()), 10),
            new PoolEntry(() -> new ItemStack(ModItems.SUN_FISH.get()), 15),
            new PoolEntry(() -> new ItemStack(ModItems.SPARK_FISH.get()), 10),
            new PoolEntry(() -> new ItemStack(ModItems.FIRE_FISH.get()), 15));

    // orig UltimateFishHook.java:45 — OreSpawn water fish pool
    private static final List<PoolEntry> ORESPAWN_FISH = List.of(
            new PoolEntry(() -> new ItemStack(ModItems.BLUE_FISH.get()), 25),
            new PoolEntry(() -> new ItemStack(ModItems.PINK_FISH.get()), 10),
            new PoolEntry(() -> new ItemStack(ModItems.ROCK_FISH.get()), 15),
            new PoolEntry(() -> new ItemStack(ModItems.WOOD_FISH.get()), 10),
            new PoolEntry(() -> new ItemStack(ModItems.GREY_FISH.get()), 15));

    public UltimateFishHook(EntityType<? extends UltimateFishHook> type, Level level) {
        super(type, level);
    }

    // NEW FINDING (logged as ITEM-066): the port previously added +3 luck /
    // +2 lure speed here with no original counterpart (orig :91-110 passes no
    // bonuses; catch quality came solely from the custom pools). Removed.
    public UltimateFishHook(Player player, Level level, int luck, int lureSpeed) {
        super(player, level, luck, lureSpeed);
    }

    /**
     * orig UltimateFishHook.java:175 — the original discarded the hook unless
     * the player still held the Ultimate Fishing Rod. Vanilla's check looks
     * for Items.FISHING_ROD specifically, which would discard this hook on
     * the first tick; overriding via the access transformer redirects the
     * check to the Ultimate rod (either hand, matching vanilla 1.21.1 hand
     * semantics; 1.7.10 had no offhand).
     */
    @Override
    public boolean shouldStopFishing(Player player) {
        boolean held = player.getMainHandItem().is(ModItems.ULTIMATE_FISHING_ROD.get())
                || player.getOffhandItem().is(ModItems.ULTIMATE_FISHING_ROD.get());
        if (!player.isRemoved() && player.isAlive() && held && !(this.distanceToSqr(player) > 1024.0)) {
            return false;
        }
        this.discard();
        return true;
    }

    /**
     * orig UltimateFishHook.java:271-276 — the copied 1.7.10 tick counted lava
     * material into the in-liquid fraction d10 exactly like water (5 bounding
     * box slices, orig :265-276), and applied a SINGLE vertical term
     * {@code motionY += 0.04*(2*d10-1)} with extra 0.9/0.8 damping while
     * d10 > 0 and no separate gravity in liquid (orig :347-355), so the
     * bobber settled half-submerged AT the lava surface just like a vanilla
     * bobber on water.
     *
     * TF-028 (i085, ENT-S-059): the previous port ran super.tick() and then
     * ADDED a lava surface correction on top. Vanilla 1.21.1
     * FishingHook.tick() (decompiled neoForm patch output,
     * net/minecraft/world/entity/projectile/FishingHook.java:139-238) is
     * water-blind: with lava the fluid, f stays 0.0 (:156-161), so its
     * BOBBING branch (:192-221) anchors the hook to the BOTTOM of the block
     * (blockpos.getY()+0.0) and its non-water gravity (:224-226) adds
     * -0.03/tick, then it moves and scales by 0.92 (:228-236). Three
     * competing vertical forces made the bobber churn ~0.6 blocks under the
     * surface instead of floating. The lava pass must be EXCLUSIVE, not
     * additive:
     *
     * (1) pre-pass: while BOBBING in lava (hookedIn is always null in
     *     BOBBING), flip currentState to HOOKED_IN_ENTITY before super.tick().
     *     With hookedIn null that vanilla branch is a pure no-op return
     *     (FishingHook.java:179-190), bypassing the f=0 bottom-anchor pull
     *     (:192-199), the -0.03 gravity (:224-226), the move (:228) and the
     *     0.92 scale (:234-235) while still running Projectile base ticking,
     *     the shouldStopFishing discard and the onGround life counter
     *     (:141-154).
     * (2) post-pass: restore BOBBING and run one faithful copy of the vanilla
     *     BOBBING body (:192-236) with f = the LAVA fluid height, and no
     *     gravity term — matching the original, which applied none while in
     *     liquid (orig :347-352).
     */
    @Override
    public void tick() {
        // TF-028 step (1): make vanilla's water-blind physics a no-op while
        // bobbing in lava. getHookedIn() is null whenever BOBBING (vanilla
        // only hooks entities from FLYING, FishingHook.java:164-177), checked
        // anyway so a desynced client can never orphan a real hooked entity.
        boolean lavaBobbing = this.currentState == FishingHook.FishHookState.BOBBING
                && this.getHookedIn() == null
                && this.level().getFluidState(this.blockPosition()).is(FluidTags.LAVA);
        if (lavaBobbing) {
            this.currentState = FishingHook.FishHookState.HOOKED_IN_ENTITY;
        }
        super.tick();
        if (lavaBobbing) {
            this.currentState = FishingHook.FishHookState.BOBBING;
        }
        if (this.isRemoved()) {
            return;
        }
        // The no-op pass cannot move the hook, so pos/fluid are still valid
        // for step (2); recomputed because the FLYING path below does move.
        BlockPos pos = this.blockPosition();
        FluidState fluid = this.level().getFluidState(pos);
        if (!fluid.is(FluidTags.LAVA)) {
            return;
        }
        float height = fluid.getHeight(this.level(), pos);
        boolean inLava = height > 0.0f;
        if (this.currentState == FishingHook.FishHookState.FLYING) {
            if (inLava && this.getHookedIn() == null) {
                // vanilla FishingHook.tick() water entry (:171-175): damp
                // motion, start bobbing
                this.setDeltaMovement(this.getDeltaMovement().multiply(0.3, 0.2, 0.3));
                this.currentState = FishingHook.FishHookState.BOBBING;
            }
        } else if (lavaBobbing) {
            // TF-028 step (2): faithful copy of the vanilla BOBBING body
            // (FishingHook.java:192-221) with f = lava height. This is the
            // ONLY vertical force this tick, so the bobber holds the lava
            // surface exactly as the original's single buoyancy term did
            // (orig :347-355, equilibrium half-submerged at d10 = 0.5).
            Vec3 vec3 = this.getDeltaMovement();
            double d0 = this.getY() + vec3.y - (double) pos.getY() - (double) height;
            if (Math.abs(d0) < 0.01) {
                d0 += Math.signum(d0) * 0.1;
            }
            this.setDeltaMovement(vec3.x * 0.9, vec3.y - d0 * (double) this.random.nextFloat() * 0.2, vec3.z * 0.9);
            // openWater/outOfWaterTime bookkeeping (:200-207, :218-220) is
            // skipped: both fields are private with no AT, and openWater only
            // feeds vanilla's loot-table treasure gate, which retrieve()
            // replaces wholesale with the original's pools.
            if (inLava && !this.level().isClientSide) {
                // Biting dunk (vanilla :208-213). Vanilla gates it on the
                // private synced `biting` flag and draws from the private
                // syncronizedRandom (neither is access-transformed); on the
                // server biting == (nibble > 0) exactly (catchingFish sets
                // DATA_BITING with nibble, :294-299/:347-348), so gate on the
                // AT'd nibble instead. Server-only matches the ORIGINAL,
                // whose whole dunk block ran under !isRemote (orig :277,
                // :343-345); the client still shows the initial -0.4 dip via
                // DATA_BITING -> onSyncedDataUpdated (:118-123), which the
                // no-op pass does not suppress.
                if (this.nibble > 0) {
                    this.setDeltaMovement(this.getDeltaMovement()
                            .add(0.0, -0.1 * (double) this.random.nextFloat() * (double) this.random.nextFloat(), 0.0));
                }
                // drives timeUntilLured/timeUntilHooked/nibble exactly as in
                // water (vanilla :215-217)
                this.catchingFish(pos);
            }
            // NO gravity here: vanilla's -0.03 (:224-226) only applies out of
            // water, and the original applied none while in liquid (orig
            // :277/:347-352).
            this.move(MoverType.SELF, this.getDeltaMovement());
            this.updateRotation();
            this.setDeltaMovement(this.getDeltaMovement().scale(0.92));
            this.reapplyPosition();
        }
    }

    /**
     * Port of orig UltimateFishHook.java:384-420 (func_146034_e). Replaces the
     * vanilla loot-table catch with the original's custom pools, flings the
     * catch to the player as a fire-resistant item (orig :409 set
     * fireResistance=3000; the port uses the fire-immune item entity), and
     * spawns 1-6 XP at the player (orig :411).
     */
    @Override
    public int retrieve(ItemStack rod) {
        Player player = this.getPlayerOwner();
        if (this.level().isClientSide || player == null || this.shouldStopFishing(player)) {
            return 0;
        }
        int i = 0;
        if (this.getHookedIn() != null) {
            // orig :389-398 — reel the hooked entity toward the player; rod
            // damage 3 (the original did not use vanilla 1.21's 3/5 split)
            this.pullEntity(this.getHookedIn());
            CriteriaTriggers.FISHING_ROD_HOOKED.trigger((ServerPlayer) player, rod, this, Collections.emptyList());
            this.level().broadcastEntityEvent(this, (byte) 31);
            i = 3;
        } else if (this.nibble > 0) {
            ItemStack catchStack = this.getCatch(player, rod);
            CriteriaTriggers.FISHING_ROD_HOOKED.trigger((ServerPlayer) player, rod, this, List.of(catchStack));
            // orig :400-410 — spawn at hook +1.25y and fling toward the player
            ItemEntity itementity = new EntityLavaLovingItem(this.level(), this.getX(), this.getY() + 1.25, this.getZ(), catchStack);
            double d0 = player.getX() - this.getX();
            double d1 = player.getY() - this.getY();
            double d2 = player.getZ() - this.getZ();
            double d3 = Math.sqrt(d0 * d0 + d1 * d1 + d2 * d2);
            itementity.setDeltaMovement(d0 * 0.1, d1 * 0.1 + Math.sqrt(d3) * 0.08, d2 * 0.1);
            this.level().addFreshEntity(itementity);
            // orig :411 — 1-6 bonus XP dropped at the player
            player.level().addFreshEntity(new ExperienceOrb(player.level(),
                    player.getX(), player.getY() + 0.5, player.getZ() + 0.5, this.random.nextInt(6) + 1));
            i = 1;
        }
        if (this.onGround()) {
            i = 2; // orig :414-416
        }
        this.discard();
        return i;
    }

    /**
     * Port of orig UltimateFishHook.java:422-449 (func_146033_f).
     * Luck of the Sea / Lure on the rod shift the junk and treasure odds
     * (orig :424-429); in lava the catch always comes from the lava pool
     * (orig :430-434); otherwise junk/treasure/fish, with fish split 50/50
     * between the vanilla and OreSpawn pools (orig :443-448). The original's
     * junk/treasure stat awards (orig :436, :440) have no 1.21.1 equivalent —
     * those stats were removed from vanilla — so only FISH_CAUGHT is kept.
     */
    private ItemStack getCatch(Player player, ItemStack rod) {
        float f = this.level().random.nextFloat();
        int i = getEnchantLevel(Enchantments.LUCK_OF_THE_SEA, rod);
        int j = getEnchantLevel(Enchantments.LURE, rod);
        float f1 = Mth.clamp(0.1f - (float) i * 0.025f - (float) j * 0.01f, 0.0f, 1.0f);
        float f2 = Mth.clamp(0.05f + (float) i * 0.01f - (float) j * 0.01f, 0.0f, 1.0f);
        // orig :430-434 — lava check (isInLava or lava block at hook position)
        if (this.isInLava() || this.level().getBlockState(this.blockPosition()).is(Blocks.LAVA)) {
            player.awardStat(Stats.FISH_CAUGHT, 1);
            return this.finishCatch(pick(LAVA_FISH));
        }
        if (f < f1) {
            return this.finishCatch(pick(JUNK));
        }
        f -= f1;
        if (f < f2) {
            return this.finishCatch(pick(TREASURE));
        }
        player.awardStat(Stats.FISH_CAUGHT, 1);
        if (this.level().random.nextFloat() < 0.5f) {
            return this.finishCatch(pick(VANILLA_FISH));
        }
        return this.finishCatch(pick(ORESPAWN_FISH));
    }

    private int getEnchantLevel(ResourceKey<Enchantment> key, ItemStack rod) {
        Holder<Enchantment> holder = this.level().registryAccess()
                .lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(key);
        return EnchantmentHelper.getItemEnchantmentLevel(holder, rod);
    }

    /** WeightedRandom.getRandomItem over a pool (orig pool selection, :433 etc.). */
    private PoolEntry pick(List<PoolEntry> pool) {
        int total = 0;
        for (PoolEntry e : pool) {
            total += e.weight();
        }
        int r = this.random.nextInt(total);
        for (PoolEntry e : pool) {
            r -= e.weight();
            if (r < 0) {
                return e;
            }
        }
        return pool.get(pool.size() - 1);
    }

    /**
     * 1.7.10 WeightedRandomFishable.func_150708_a: random damage biased toward
     * the cap (damagePercent of max durability, min 1) and, for enchantable
     * entries, a random level-30 enchant.
     */
    private ItemStack finishCatch(PoolEntry entry) {
        ItemStack stack = entry.stack().get();
        if (entry.damagePercent() > 0.0f) {
            int cap = (int) (entry.damagePercent() * (float) stack.getMaxDamage());
            int dmg = stack.getMaxDamage() - this.random.nextInt(this.random.nextInt(cap) + 1);
            if (dmg > cap) {
                dmg = cap;
            }
            if (dmg < 1) {
                dmg = 1;
            }
            stack.setDamageValue(dmg);
        }
        if (entry.enchant()) {
            stack = EnchantmentHelper.enchantItem(this.random, stack, 30,
                    this.level().registryAccess(), Optional.empty());
        }
        return stack;
    }
}
