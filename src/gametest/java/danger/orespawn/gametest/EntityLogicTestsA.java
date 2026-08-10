package danger.orespawn.gametest;

import danger.orespawn.ModDataComponents;
import danger.orespawn.ModEntities;
import danger.orespawn.ModItems;
import danger.orespawn.OreSpawnConfig;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.Alien;
import danger.orespawn.entity.AntRobot;
import danger.orespawn.entity.BandP;
import danger.orespawn.entity.BerthaHit;
import danger.orespawn.entity.Boyfriend;
import danger.orespawn.entity.CaveFisher;
import danger.orespawn.entity.Cephadrome;
import danger.orespawn.entity.Chipmunk;
import danger.orespawn.entity.Cryolophosaurus;
import danger.orespawn.entity.Dragon;
import danger.orespawn.entity.EntityCage;
import danger.orespawn.entity.EntityEmperorScorpion;
import danger.orespawn.entity.EntityLeafMonster;
import danger.orespawn.entity.EntityLeon;
import danger.orespawn.entity.EntityRat;
import danger.orespawn.entity.EntityRubberDucky;
import danger.orespawn.entity.EntityScorpion;
import danger.orespawn.entity.EntitySpyro;
import danger.orespawn.entity.EntityStinkBug;
import danger.orespawn.entity.Girlfriend;
import danger.orespawn.entity.Kraken;
import danger.orespawn.entity.Lizard;
import danger.orespawn.entity.Peacock;
import danger.orespawn.entity.PurplePower;
import danger.orespawn.entity.SeaViper;
import danger.orespawn.entity.UltimateArrow;
import danger.orespawn.entity.ai.SeaViperBiteGoal;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Difficulty;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/**
 * Entity-logic batch A (testing_session batch: category entity_logic, first 21
 * items by id order, i021..i046). K base 400 — reserved 400-499, UNUSED: every
 * test in this batch fits inside the 48x16x48 {@code empty_large} template, so
 * no far-offset out-of-template builds were needed.
 *
 * <p>Conventions used throughout:
 * <ul>
 *   <li>All staging happens at the CENTER of empty_large (rel ~24,*,24) so the
 *       largest mod AI scan boxes (16 blocks) cannot reach a neighboring test's
 *       template on the shared test grid.</li>
 *   <li>"Pinned" entities are set no-gravity with zeroed motion and re-moved to
 *       a fixed spot every tick, so range-gated attack branches see constant
 *       distances and nothing falls through the (floorless) template.</li>
 *   <li>Statistical asserts sample the real random rolls; every band is chosen
 *       so the false-failure probability at the documented rate is &lt; 1e-9
 *       (Hoeffding/Chernoff math is shown at each assert).</li>
 *   <li>Difficulty flips (EASY windows) happen atomically inside a single
 *       scheduled runnable: game-test runnables never interleave with entity
 *       ticking, so the flip can never leak into another test's ticks.</li>
 * </ul>
 */
@GameTestHolder(OreSpawnMod.MOD_ID)
@PrefixGameTestTemplate(false)
public class EntityLogicTestsA {

    // ==================== shared helpers ====================

    private static void setMaxHealth(LivingEntity e, double hp) {
        Objects.requireNonNull(e.getAttribute(Attributes.MAX_HEALTH)).setBaseValue(hp);
        e.setHealth((float) hp);
    }

    private static void zeroArmor(LivingEntity e) {
        Objects.requireNonNull(e.getAttribute(Attributes.ARMOR)).setBaseValue(0.0);
    }

    /** Freeze an entity at an absolute position (no gravity, no residual motion). */
    private static void pin(Entity e, Vec3 abs) {
        e.setNoGravity(true);
        e.setDeltaMovement(Vec3.ZERO);
        e.moveTo(abs.x, abs.y, abs.z, e.getYRot(), e.getXRot());
    }

    /** A survival-mode mock player positioned at an absolute location (NOT added to the level). */
    private static Player mockPlayerAt(GameTestHelper h, Vec3 abs) {
        Player p = h.makeMockPlayer(GameType.SURVIVAL);
        p.moveTo(abs.x, abs.y, abs.z, 0.0f, 0.0f);
        return p;
    }

    /** A survival-mode mock player ADDED to the level (so mod AI entity scans can find it). */
    private static Player addedMockPlayerAt(GameTestHelper h, Vec3 abs) {
        Player p = mockPlayerAt(h, abs);
        p.setNoGravity(true);
        h.assertTrue(h.getLevel().addFreshEntity(p), "mock player must be addable to the level");
        return p;
    }

    /** Stone pad at relative y=1 (and y=0 when {@code doubleLayer}) covering [x1..x2]x[z1..z2]. */
    private static void pad(GameTestHelper h, int x1, int z1, int x2, int z2, boolean doubleLayer) {
        for (int x = x1; x <= x2; x++) {
            for (int z = z1; z <= z2; z++) {
                h.setBlock(new BlockPos(x, 1, z), Blocks.STONE);
                if (doubleLayer) h.setBlock(new BlockPos(x, 0, z), Blocks.STONE);
            }
        }
    }

    /** 2-high stone wall ring on the perimeter of [x1..x2]x[z1..z2] at y=2..3. */
    private static void ring(GameTestHelper h, int x1, int z1, int x2, int z2) {
        for (int x = x1; x <= x2; x++) {
            for (int z = z1; z <= z2; z++) {
                if (x == x1 || x == x2 || z == z1 || z == z2) {
                    h.setBlock(new BlockPos(x, 2, z), Blocks.STONE);
                    h.setBlock(new BlockPos(x, 3, z), Blocks.STONE);
                }
            }
        }
    }

    private static int enchLevel(GameTestHelper h, ItemStack stack,
                                 net.minecraft.resources.ResourceKey<net.minecraft.world.item.enchantment.Enchantment> key) {
        ItemEnchantments ench = stack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
        return ench.getLevel(h.getLevel().registryAccess()
                .registryOrThrow(Registries.ENCHANTMENT).getHolderOrThrow(key));
    }

    // =====================================================================
    // i021-item-043-ent-s-057 — Ultimate Bow
    // =====================================================================

    /**
     * Checklist i021-item-043-ent-s-057 (ITEM-043 + ENT-S-057).
     * Documented values: phase_c_reports/C6_items_blocks.md:71 — fixed self-enchant
     * Power 5 / Flame 3 / Punch 2 / Infinity 1, instant release at velocity 3.0,
     * flat 1-in-4 crit (orig UltimateBow.java:29-34,46-64); and
     * phase_c_reports/C4_entities_S_Z.md:29 — arrow damage = ceil(velocity x
     * ultimateBowDamage), config default 10 clamp 2-20 (orig UltimateArrow.java:157,279,
     * orig OreSpawnMain.java:1519-1530). Port: item/UltimateBow.java:26-59,
     * entity/UltimateArrow.java:29-41, OreSpawnConfig.java:294.
     */
    @GameTest(template = "empty_large", timeoutTicks = 300)
    public void ultimate_bow_instant_power5_crit_damage(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        Vec3 shooterPos = helper.absoluteVec(new Vec3(24.0, 6.0, 20.0));
        Player shooter = mockPlayerAt(helper, shooterPos);
        // Face +Z, 15 degrees downward so the point-blank shot dips into the cow's body.
        shooter.moveTo(shooterPos.x, shooterPos.y, shooterPos.z, 0.0f, 15.0f);
        AABB arrowScan = new AABB(BlockPos.containing(shooterPos)).inflate(8.0);

        // (a) Self-enchants exactly Power 5 (not 10), Flame 3, Punch 2, Infinity 1
        //     — orig UltimateBow.java:30-33, port item/UltimateBow.java:26-33.
        ItemStack enchStack = new ItemStack(ModItems.ULTIMATE_BOW.get());
        enchStack.getItem().inventoryTick(enchStack, level, shooter, 0, true);
        helper.assertValueEqual(enchLevel(helper, enchStack, Enchantments.POWER), 5, "Ultimate Bow baked Power level");
        helper.assertValueEqual(enchLevel(helper, enchStack, Enchantments.FLAME), 3, "Ultimate Bow baked Flame level");
        helper.assertValueEqual(enchLevel(helper, enchStack, Enchantments.PUNCH), 2, "Ultimate Bow baked Punch level");
        helper.assertValueEqual(enchLevel(helper, enchStack, Enchantments.INFINITY), 1, "Ultimate Bow baked Infinity level");

        // (b) Instant fire: releaseUsing immediately after "drawing" launches at
        //     the fixed 3.0 velocity — no charge scaling (orig UltimateBow.java:46-48).
        //     Unenchanted stack so later damage math has no vanilla Power bonus.
        ItemStack bow = new ItemStack(ModItems.ULTIMATE_BOW.get());
        bow.getItem().releaseUsing(bow, level, shooter, 71999); // released 1 tick after use started
        List<UltimateArrow> arrows = level.getEntitiesOfClass(UltimateArrow.class, arrowScan);
        helper.assertValueEqual(arrows.size(), 1, "one arrow per instant release");
        double speed = arrows.get(0).getDeltaMovement().length();
        helper.assertTrue(speed > 2.80 && speed < 3.20,
                "arrow launch speed ~3.0 regardless of draw time, got " + speed);
        arrows.get(0).discard();

        // (c) Crit rate ~25% — mechanism nextInt(4)==1 (orig UltimateBow.java:49-51).
        //     n=800 samples, X~Bin(800, 0.25), mean 200. Band [100,320]:
        //     Hoeffding P(|X-200| >= 100) <= 2*exp(-2*800*(0.125)^2) = 2*exp(-25) ~ 2.8e-11 < 1e-9.
        int crits = 0;
        ItemStack critBow = new ItemStack(ModItems.ULTIMATE_BOW.get());
        for (int i = 0; i < 800; i++) {
            critBow.getItem().releaseUsing(critBow, level, shooter, 71999);
            List<UltimateArrow> shot = level.getEntitiesOfClass(UltimateArrow.class, arrowScan);
            helper.assertValueEqual(shot.size(), 1, "one arrow per release (crit sampling)");
            if (shot.get(0).isCritArrow()) crits++;
            shot.get(0).discard();
        }
        helper.assertTrue(crits >= 100 && crits <= 320,
                "crit count " + crits + "/800 outside [100,320] for the documented 1-in-4 rate");

        // (d) Hit damage = ceil(speed x ultimateBowDamage); halving the config halves it.
        //     Config is read at arrow construction (UltimateArrow.java:29-41), so the
        //     set/restore brackets ONLY the releaseUsing call — no cross-test leak.
        Vec3 cowPos = helper.absoluteVec(new Vec3(24.0, 6.0, 23.0));
        final Cow cow1 = helper.spawnWithNoFreeWill(EntityType.COW, new BlockPos(24, 6, 23));
        setMaxHealth(cow1, 10000.0);
        pin(cow1, cowPos);
        ItemStack shotBow1 = new ItemStack(ModItems.ULTIMATE_BOW.get());
        shotBow1.getItem().releaseUsing(shotBow1, level, shooter, 71999);
        List<UltimateArrow> flight1 = level.getEntitiesOfClass(UltimateArrow.class, arrowScan);
        helper.assertValueEqual(flight1.size(), 1, "damage-phase arrow spawned");
        flight1.get(0).setCritArrow(false); // crits add a random bonus — force the deterministic path

        final boolean[] done = {false};
        helper.runAfterDelay(4, () -> {
            // speed 3.0 (+- tiny shootFromRotation jitter) x config 10 => ceil in [29,31]
            float delta1 = 10000.0f - cow1.getHealth();
            helper.assertTrue(delta1 >= 29.0f && delta1 <= 31.0f,
                    "full-speed hit ~ceil(3 x 10) = 30, got " + delta1);
            cow1.discard();

            // halve the config: 5 => ceil(~3 x 5) in [14,16] ~ half of 30
            final Cow cow2 = helper.spawnWithNoFreeWill(EntityType.COW, new BlockPos(24, 6, 23));
            setMaxHealth(cow2, 10000.0);
            pin(cow2, cowPos);
            int oldCfg = OreSpawnConfig.ULTIMATE_BOW_DAMAGE.get();
            ItemStack shotBow2 = new ItemStack(ModItems.ULTIMATE_BOW.get());
            try {
                OreSpawnConfig.ULTIMATE_BOW_DAMAGE.set(5);
                shotBow2.getItem().releaseUsing(shotBow2, level, shooter, 71999);
            } finally {
                OreSpawnConfig.ULTIMATE_BOW_DAMAGE.set(oldCfg); // arrow already captured 5 in its ctor
            }
            List<UltimateArrow> flight2 = level.getEntitiesOfClass(UltimateArrow.class, arrowScan);
            helper.assertValueEqual(flight2.size(), 1, "halved-config arrow spawned");
            flight2.get(0).setCritArrow(false);
            helper.runAfterDelay(4, () -> {
                float delta2 = 10000.0f - cow2.getHealth();
                helper.assertTrue(delta2 >= 14.0f && delta2 <= 16.0f,
                        "halved config => ~ceil(3 x 5) = 15 damage (half of 30), got " + delta2);
                cow2.discard();
                done[0] = true;
            });
        });
        helper.succeedWhen(() -> helper.assertTrue(done[0], "damage phases finished"));
    }

    // =====================================================================
    // i022-ent-a-045-051-bertha — Big Bertha class shockwave
    // =====================================================================

    /**
     * Checklist i022-ent-a-045-051-bertha (ENT-A-045 + ENT-A-051).
     * Documented values: phase_c_reports/C1_entities_A_C.md:76,108 — Bertha 496 dmg,
     * Royal 746, Hammy/"Attitude" 82 (orig OreSpawnMain.java get_weaponstats, orig
     * BerthaHit.java:76-105); C1_entities_A_C.md:80 — Girlfriend/Boyfriend spared
     * UNCONDITIONALLY (orig BerthaHit.java:68-75 precedence quirk), players/tamed
     * pets spared only while bigBerthaPvp is false. Port: entity/BerthaHit.java:46-86,
     * item/Bertha.java:60-94, OreSpawnConfig.java:274 (default false).
     */
    @GameTest(template = "empty_large", timeoutTicks = 200)
    public void big_bertha_shockwave_damage_and_pvp(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        // Owner never needs to be in the level: BerthaHit keeps the direct owner ref.
        Vec3 ownerPos = helper.absoluteVec(new Vec3(24.0, 6.0, 27.0));
        final Player owner = mockPlayerAt(helper, ownerPos);
        final Vec3 victimPos = helper.absoluteVec(new Vec3(24.0, 6.0, 24.0)); // 3 blocks from owner (< range gates 81/64)

        // Swing spawns the shockwave projectile (orig Bertha.java:78-98; port item/Bertha.java:60-81).
        ItemStack bertha = new ItemStack(ModItems.BIG_BERTHA.get());
        bertha.getItem().onEntitySwing(bertha, owner);
        List<BerthaHit> swung = level.getEntitiesOfClass(BerthaHit.class, new AABB(BlockPos.containing(ownerPos)).inflate(10.0));
        helper.assertValueEqual(swung.size(), 1, "swing must spawn one BerthaHit shockwave");
        helper.assertValueEqual(bertha.getDamageValue(), 1, "1 durability per swing (orig Bertha.java:78)");
        swung.get(0).discard();

        final boolean[] done = {false};
        final boolean pvpOld = OreSpawnConfig.BIG_BERTHA_PVP.get();

        // -- per-item one-shot damage values (hitType 0/2/3 = Bertha/Royal/Hammy) --
        final int[][] typeAndDamage = { {0, 496}, {2, 746}, {3, 82} };
        final Cow[] dmgVictim = new Cow[1];
        for (int i = 0; i < typeAndDamage.length; i++) {
            final int idx = i;
            helper.runAfterDelay(2 + i * 6L, () -> {
                dmgVictim[0] = helper.spawnWithNoFreeWill(EntityType.COW, new BlockPos(24, 6, 24));
                setMaxHealth(dmgVictim[0], 2000.0);
                zeroArmor(dmgVictim[0]);
                pin(dmgVictim[0], victimPos);
                launchBerthaHit(helper, owner, typeAndDamage[idx][0], dmgVictim[0]);
            });
            helper.runAfterDelay(5 + i * 6L, () -> {
                float delta = 2000.0f - dmgVictim[0].getHealth();
                // hitType 3's own 2.1-power explosion lands inside the 496>82>expl
                // invulnerability window, so the delta stays exactly the doc value.
                helper.assertValueEqual(delta, (float) typeAndDamage[idx][1],
                        "BerthaHit type " + typeAndDamage[idx][0] + " damage (orig BerthaHit.java:76-105)");
                if (typeAndDamage[idx][0] == 0) {
                    helper.assertTrue(dmgVictim[0].getRemainingFireTicks() > 0,
                            "type-0 hit ignites 10 s (orig BerthaHit.java:82)");
                }
                dmgVictim[0].discard();
            });
        }

        // -- Girlfriend/Boyfriend NEVER hit, even with bigBerthaPvp=true --
        final LivingEntity[] royal = new LivingEntity[1];
        helper.runAfterDelay(22, () -> {
            OreSpawnConfig.BIG_BERTHA_PVP.set(true);
            Girlfriend gf = helper.spawn(ModEntities.GIRLFRIEND.get(), new BlockPos(24, 6, 24));
            gf.setNoGravity(true);
            royal[0] = gf;
            launchBerthaHit(helper, owner, 0, gf);
        });
        helper.runAfterDelay(25, () -> {
            helper.assertValueEqual(royal[0].getHealth(), royal[0].getMaxHealth(),
                    "Girlfriend unconditionally spared (orig BerthaHit.java:68-75 precedence quirk)");
            royal[0].discard();
            Boyfriend bf = helper.spawn(ModEntities.BOYFRIEND.get(), new BlockPos(24, 6, 24));
            bf.setNoGravity(true);
            royal[0] = bf;
            launchBerthaHit(helper, owner, 0, bf);
        });
        helper.runAfterDelay(28, () -> {
            helper.assertValueEqual(royal[0].getHealth(), royal[0].getMaxHealth(),
                    "Boyfriend unconditionally spared (orig BerthaHit.java:68-75)");
            royal[0].discard();
        });

        // -- players respect the config: spared at pvp=false, 496 at pvp=true --
        final Player[] victimPlayer = new Player[1];
        helper.runAfterDelay(30, () -> {
            OreSpawnConfig.BIG_BERTHA_PVP.set(false);
            Player b = addedMockPlayerAt(helper, victimPos);
            setMaxHealth(b, 600.0);
            victimPlayer[0] = b;
            launchBerthaHit(helper, owner, 0, b);
        });
        helper.runAfterDelay(33, () -> {
            helper.assertValueEqual(victimPlayer[0].getHealth(), 600.0f,
                    "player spared while bigBerthaPvp=false (orig BerthaHit.java:70-72)");
            OreSpawnConfig.BIG_BERTHA_PVP.set(true);
            launchBerthaHit(helper, owner, 0, victimPlayer[0]);
        });
        helper.runAfterDelay(36, () -> {
            helper.assertValueEqual(victimPlayer[0].getHealth(), 600.0f - 496.0f,
                    "player takes the full 496 while bigBerthaPvp=true");
            victimPlayer[0].clearFire();
            victimPlayer[0].discard();
        });

        // -- tamed pets respect the config too --
        final Wolf[] pet = new Wolf[1];
        helper.runAfterDelay(38, () -> {
            OreSpawnConfig.BIG_BERTHA_PVP.set(false);
            Wolf w = helper.spawnWithNoFreeWill(EntityType.WOLF, new BlockPos(24, 6, 24));
            w.setTame(true, false);
            w.setOwnerUUID(UUID.randomUUID());
            setMaxHealth(w, 600.0);
            pin(w, victimPos);
            pet[0] = w;
            launchBerthaHit(helper, owner, 0, w);
        });
        helper.runAfterDelay(41, () -> {
            helper.assertValueEqual(pet[0].getHealth(), 600.0f,
                    "tamed pet spared while bigBerthaPvp=false (orig BerthaHit.java:70-72)");
            OreSpawnConfig.BIG_BERTHA_PVP.set(true);
            launchBerthaHit(helper, owner, 0, pet[0]);
        });
        helper.runAfterDelay(44, () -> {
            helper.assertValueEqual(pet[0].getHealth(), 600.0f - 496.0f,
                    "tamed pet takes the full 496 while bigBerthaPvp=true");
            pet[0].clearFire();
            pet[0].discard();
            OreSpawnConfig.BIG_BERTHA_PVP.set(pvpOld); // restore default (false)
            done[0] = true;
        });

        helper.succeedWhen(() -> helper.assertTrue(done[0], "all BerthaHit phases finished"));
    }

    /** Spawns a BerthaHit 1.2 blocks west of the victim flying east — hits on its first tick. */
    private static void launchBerthaHit(GameTestHelper helper, Player owner, int type, LivingEntity victim) {
        Vec3 c = victim.getBoundingBox().getCenter();
        double back = victim.getBbWidth() / 2.0 + 1.2;
        BerthaHit hit = new BerthaHit(helper.getLevel(), owner);
        hit.setHitType(type);
        hit.setPos(c.x - back, c.y, c.z);
        hit.setDeltaMovement(back + 1.0, 0.0, 0.0);
        helper.getLevel().addFreshEntity(hit);
    }

    // =====================================================================
    // i023-ent-k-033 — Mantis Claw silent drain
    // =====================================================================

    /**
     * Checklist i023-ent-k-033 (ENT-K-033).
     * Documented values: phase_c_reports/C3_entities_K_R.md:19 — silent -1 HP drain
     * with NO damage event (orig MantisClaw.java:36-37 target.heal(-1)) plus
     * attacker.heal(+1) and 1 durability; max damage 1000 (orig MantisClaw.java:25).
     * Port: item/MantisClaw.java:19-36. The "no second hurt flash" is asserted as
     * hurtTime staying 0: the flash is driven by the hurt event that sets hurtTime.
     */
    @GameTest(template = "empty_large", timeoutTicks = 100)
    public void mantis_claw_silent_drain(GameTestHelper helper) {
        Player attacker = mockPlayerAt(helper, helper.absoluteVec(new Vec3(24.0, 6.0, 22.0)));
        attacker.setHealth(10.0f);
        Cow victim = helper.spawnWithNoFreeWill(EntityType.COW, new BlockPos(24, 6, 24));
        victim.setNoGravity(true);
        float startHp = victim.getHealth();

        ItemStack claw = new ItemStack(ModItems.MANTIS_CLAW.get());
        helper.assertValueEqual(claw.getMaxDamage(), 1000,
                "MantisClaw durability override (orig MantisClaw.java:25)");

        claw.getItem().hurtEnemy(claw, victim, attacker);
        helper.assertValueEqual(victim.getHealth(), startHp - 1.0f,
                "first hit silently drains exactly 1 HP (orig MantisClaw.java:36)");
        helper.assertValueEqual(victim.hurtTime, 0,
                "drain fires no hurt event — no second hurt flash (ENT-K-033)");
        helper.assertValueEqual(attacker.getHealth(), 11.0f,
                "attacker healed +1 per hit (orig MantisClaw.java:37)");
        helper.assertValueEqual(claw.getDamageValue(), 1, "1 durability per hit");

        // Second hit in the same tick still applies — setHealth bypasses invuln frames,
        // which is exactly what distinguishes the drain from a hurt() call.
        claw.getItem().hurtEnemy(claw, victim, attacker);
        helper.assertValueEqual(victim.getHealth(), startHp - 2.0f,
                "second same-tick hit drains again (no invulnerability window involved)");
        helper.assertValueEqual(attacker.getHealth(), 12.0f, "attacker healed again");
        helper.assertValueEqual(claw.getDamageValue(), 2, "durability again");

        victim.discard();
        helper.succeed();
    }

    // =====================================================================
    // i025-ent-a-004 — Alien poison durations
    // =====================================================================

    /**
     * Checklist i025-ent-a-004 (ENT-A-004).
     * Documented values: phase_c_reports/C1_entities_A_C.md:51 — POISON (not Hunger),
     * 40 ticks on Easy / 30 ticks otherwise, applied on a 1-in-5 roll
     * (orig Alien.java:200-210; the Normal/Hard branches are unreachable quirks).
     * Port: entity/Alien.java:135-159. GameTestServer runs at Difficulty.NORMAL
     * (decompiled GameTestServer.java:85); the EASY window below is atomic inside
     * one runnable so it cannot leak into any other test's entity ticks.
     */
    @GameTest(template = "empty_large", timeoutTicks = 100)
    public void alien_poison_easy_normal(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        MinecraftServer server = level.getServer();
        helper.assertValueEqual(level.getDifficulty(), Difficulty.NORMAL,
                "baseline gametest server difficulty (GameTestServer.java:85)");

        Alien alien = helper.spawn(ModEntities.ALIEN.get(), new BlockPos(24, 6, 24));
        alien.setNoGravity(true);
        Cow cow = helper.spawnWithNoFreeWill(EntityType.COW, new BlockPos(24, 6, 26));
        cow.setNoGravity(true);
        setMaxHealth(cow, 100000.0);

        // NORMAL: n=800 direct doHurtTarget calls. Poison fires on nextInt(5)==1
        // (p=0.2, mean 160). Band [60,260]: Hoeffding P(|X-160| >= 100) <=
        // 2*exp(-2*800*(0.125)^2) = 2*exp(-25) ~ 2.8e-11 < 1e-9.
        int poisoned = 0;
        for (int i = 0; i < 800; i++) {
            cow.removeAllEffects();
            cow.invulnerableTime = 0;
            cow.setHealth(cow.getMaxHealth());
            alien.doHurtTarget(cow);
            helper.assertFalse(cow.hasEffect(MobEffects.HUNGER),
                    "Alien must never apply Hunger (audit-corrected, C1:51)");
            MobEffectInstance poison = cow.getEffect(MobEffects.POISON);
            if (poison != null) {
                poisoned++;
                helper.assertValueEqual(poison.getDuration(), 30,
                        "poison 30 ticks (1.5 s) at non-Easy difficulty (orig Alien.java:200-210)");
                helper.assertValueEqual(poison.getAmplifier(), 0, "poison amplifier 0");
            }
        }
        helper.assertTrue(poisoned >= 60 && poisoned <= 260,
                "poison applications " + poisoned + "/800 outside [60,260] for the documented 1-in-5 roll");

        // EASY window — atomic set/assert/restore inside this same runnable.
        try {
            server.setDifficulty(Difficulty.EASY, true);
            boolean sawEasyPoison = false;
            // P(no poison in 200 rolls) = 0.8^200 ~ 4e-20 — far below the 1e-9 budget.
            for (int i = 0; i < 200 && !sawEasyPoison; i++) {
                cow.removeAllEffects();
                cow.invulnerableTime = 0;
                cow.setHealth(cow.getMaxHealth());
                alien.doHurtTarget(cow);
                MobEffectInstance poison = cow.getEffect(MobEffects.POISON);
                if (poison != null) {
                    sawEasyPoison = true;
                    helper.assertValueEqual(poison.getDuration(), 40,
                            "poison 40 ticks (2 s) on Easy (orig Alien.java:200-210)");
                }
                helper.assertFalse(cow.hasEffect(MobEffects.HUNGER), "never Hunger on Easy either");
            }
            helper.assertTrue(sawEasyPoison, "poison must appear within 200 Easy-mode hits");
        } finally {
            server.setDifficulty(Difficulty.NORMAL, true);
        }

        cow.discard();
        alien.discard();
        helper.succeed();
    }

    // =====================================================================
    // i026-ent-a-013-014 — AntRobot melee cadence + ridden stomp
    // =====================================================================

    /**
     * Checklist i026-ent-a-013-014, cadence half (ENT-A-013).
     * Documented value: phase_c_reports/C1_entities_A_C.md:52 — unridden melee is
     * attempted only on a 1-in-15 roll per tick (orig AntRobot.java:130-145), not
     * every tick. Port: entity/AntRobot.java:122-132. With the victim's
     * invulnerability cleared every tick, every attempt lands, so hits/tick
     * estimates the gate directly (an ungated port would land ~1 hit per tick,
     * far above the accepted band).
     */
    @GameTest(template = "empty_large", timeoutTicks = 2100)
    public void antrobot_melee_cadence(GameTestHelper helper) {
        final Vec3 robotPos = helper.absoluteVec(new Vec3(24.0, 6.0, 24.0));
        final Vec3 cowPos = helper.absoluteVec(new Vec3(24.0, 6.0, 26.0)); // dist 2 < melee range ~6.45
        final AntRobot robot = helper.spawn(ModEntities.ANT_ROBOT.get(), new BlockPos(24, 6, 24));
        final Cow cow = helper.spawnWithNoFreeWill(EntityType.COW, new BlockPos(24, 6, 26));
        setMaxHealth(cow, 10000.0);
        zeroArmor(cow);
        pin(robot, robotPos);
        pin(cow, cowPos);

        final int WINDOW = 1800;
        final int[] ticks = {0};
        final int[] hits = {0};
        final boolean[] done = {false};
        helper.onEachTick(() -> {
            if (done[0]) return;
            float now = cow.getHealth();
            if (now < 10000.0f) {
                // ENT-A-014 companion fact: melee damage is the raw attack attribute
                // (30, MobStats.ANT_ROBOT; orig OreSpawnMain.java:6475).
                helper.assertValueEqual(10000.0f - now, 30.0f,
                        "AntRobot melee = ATTACK_DAMAGE 30 (entity/AntRobot.java:209-211)");
                hits[0]++;
            }
            cow.setHealth(10000.0f);
            cow.invulnerableTime = 0;
            pin(cow, cowPos);
            pin(robot, robotPos);
            ticks[0]++;
            if (ticks[0] >= WINDOW) {
                // X~Bin(1800, 1/15), mean 120. Band [40,280]:
                //  lower tail Chernoff exp(-(80)^2/(2*120)) = exp(-26.7) ~ 2.5e-12;
                //  upper tail exp(-(160)^2/(3*120)) = exp(-71) — both << 1e-9.
                // An every-tick melee (the audited pre-fix behavior) would land ~1800 hits.
                helper.assertTrue(hits[0] >= 40 && hits[0] <= 280,
                        "melee hits " + hits[0] + "/1800 ticks outside [40,280] for the 1-in-15 cadence gate");
                cow.discard();
                robot.discard();
                done[0] = true;
            }
        });
        helper.succeedWhen(() -> helper.assertTrue(done[0], "cadence window finished"));
    }

    /**
     * Checklist i026-ent-a-013-014, stomp half (ENT-A-014).
     * Documented values: phase_c_reports/C1_entities_A_C.md:53 — while ridden, a
     * 1-in-50 per-tick stomp hits the 6-9 block ring for ATTACK_DAMAGE/10 = 3.0
     * (orig AntRobot.java:617-619,1000). Port: entity/AntRobot.java:164-167,269-296.
     */
    @GameTest(template = "empty_large", timeoutTicks = 1900)
    public void antrobot_ridden_stomp(GameTestHelper helper) {
        final Vec3 robotPos = helper.absoluteVec(new Vec3(24.0, 6.0, 24.0));
        final Vec3 cowPos = helper.absoluteVec(new Vec3(24.0, 6.0, 31.5)); // ring distance 7.5 (in 6..9)
        final AntRobot robot = helper.spawn(ModEntities.ANT_ROBOT.get(), new BlockPos(24, 6, 24));
        pin(robot, robotPos);
        // Rider: a mock player held only as a passenger (never added to the level —
        // the ridden branches only check getFirstPassenger() != null).
        Player rider = mockPlayerAt(helper, robotPos);
        helper.assertTrue(rider.startRiding(robot, true), "rider must mount the AntRobot");
        helper.assertTrue(robot.getFirstPassenger() == rider, "rider registered as first passenger");

        final Cow cow = helper.spawnWithNoFreeWill(EntityType.COW, new BlockPos(24, 6, 31));
        setMaxHealth(cow, 10000.0);
        zeroArmor(cow);
        pin(cow, cowPos);

        final boolean[] done = {false};
        helper.onEachTick(() -> {
            if (done[0]) return;
            float now = cow.getHealth();
            if (now < 10000.0f) {
                // The 6-9 ring victim is outside melee range (6+0.45), so any damage
                // must be the stomp: exactly ATTACK_DAMAGE/10 = 3.0.
                helper.assertValueEqual(10000.0f - now, 3.0f,
                        "ridden stomp = ATTACK_DAMAGE/10 = 3.0 (orig AntRobot.java:1000)");
                cow.discard();
                robot.discard();
                done[0] = true;
                return;
            }
            cow.invulnerableTime = 0;
            pin(cow, cowPos);
            pin(robot, robotPos);
        });
        // 1-in-50 per tick: P(no stomp in 1800 ticks) = (49/50)^1800 = e^-36 ~ 2e-16.
        helper.succeedWhen(() -> helper.assertTrue(done[0], "a 3.0-damage stomp must occur while ridden"));
    }

    // =====================================================================
    // i027-ent-a-025 — BandP steals on every hit, armor first
    // =====================================================================

    /**
     * Checklist i027-ent-a-025 (ENT-A-025).
     * Documented values: phase_c_reports/C1_entities_A_C.md:54 — steal on EVERY
     * successful hit, armor first (helmet-&gt;boots), 100-slot stash
     * (orig BandP.java:46,185-218). Port: entity/BandP.java:125-186.
     * Hits are observed as health drops (BandP ATK 1, MobStats.BANDP,
     * orig OreSpawnMain.java:6480); the victim's food level is held below the
     * regen threshold so natural regen can never mask a sub-1.0 hit.
     */
    @GameTest(template = "empty_large", timeoutTicks = 900)
    public void bandp_steals_every_hit_armor_first(GameTestHelper helper) {
        final Vec3 bandpPos = helper.absoluteVec(new Vec3(24.0, 6.0, 24.0));
        final Vec3 playerPos = helper.absoluteVec(new Vec3(24.0, 6.0, 25.5)); // distSq < 9 melee gate
        final BandP bandp = helper.spawn(ModEntities.BAND_P.get(), new BlockPos(24, 6, 24));
        pin(bandp, bandpPos);
        final Player victim = addedMockPlayerAt(helper, playerPos);
        victim.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.IRON_HELMET));
        victim.setItemSlot(EquipmentSlot.CHEST, new ItemStack(Items.IRON_CHESTPLATE));
        victim.setItemSlot(EquipmentSlot.LEGS, new ItemStack(Items.IRON_LEGGINGS));
        victim.setItemSlot(EquipmentSlot.FEET, new ItemStack(Items.IRON_BOOTS));
        victim.getInventory().items.set(0, new ItemStack(Items.BREAD));
        victim.getInventory().items.set(1, new ItemStack(Items.BREAD));
        victim.getInventory().items.set(2, new ItemStack(Items.BREAD));

        final EquipmentSlot[] armorOrder = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};
        final List<EquipmentSlot> emptiedOrder = new ArrayList<>();
        final int[] hitEvents = {0};
        final boolean[] done = {false};

        helper.onEachTick(() -> {
            if (done[0]) return;
            // hit detection (before topping health back up)
            if (victim.getHealth() < 20.0f) hitEvents[0]++;
            victim.setHealth(20.0f);
            victim.getFoodData().setFoodLevel(10); // < 18 => no natural regen masking a 0.4-HP hit
            pin(victim, playerPos);
            pin(bandp, bandpPos);

            // steal accounting
            int armorMissing = 0;
            for (EquipmentSlot slot : armorOrder) {
                boolean empty = victim.getItemBySlot(slot).isEmpty();
                if (empty) {
                    armorMissing++;
                    if (!emptiedOrder.contains(slot)) emptiedOrder.add(slot);
                }
            }
            int breadMissing = 0;
            for (int i = 0; i < 3; i++) {
                if (victim.getInventory().items.get(i).isEmpty()) breadMissing++;
            }
            int stolen = armorMissing + breadMissing;
            // armor-first invariant (orig BandP.java:185-218 steal preference order)
            if (breadMissing > 0) {
                helper.assertValueEqual(armorMissing, 4,
                        "inventory items must only be stolen after ALL armor is gone (armor first)");
            }
            helper.assertValueEqual(stolen, hitEvents[0],
                    "exactly one item stolen per successful hit (orig BandP.java:185-218)");
            if (stolen >= 6) {
                // 4 armor + 2 bread observed; stop before the last bread so post-
                // exhaustion hits can never break the 1:1 hit/steal accounting.
                helper.assertValueEqual(emptiedOrder,
                        List.of(EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET),
                        "armor stolen in helmet->chest->legs->boots order");
                bandp.discard();
                victim.discard();
                done[0] = true;
            }
        });
        helper.succeedWhen(() -> helper.assertTrue(done[0], "six steal-per-hit events observed"));
    }

    // =====================================================================
    // i028-ent-a-054 — Boyfriend tempt / panic / doors
    // =====================================================================

    /**
     * Checklist i028-ent-a-054 (ENT-A-054).
     * Documented values: phase_c_reports/C1_entities_A_C.md:58 — TemptGoal(cooked
     * beef)@2, PanicGoal(1.5)@6, OpenDoorGoal@10 with door navigation
     * (orig Boyfriend.java:127-148). Port: entity/Boyfriend.java:78-99.
     * Tempt needs a real ServerPlayer (vanilla TemptGoal scans level.players(),
     * which only ever contains ServerPlayers — ServerLevel.java:1701).
     * The panic assert is behavior-level: four independent hurt trials must each
     * accumulate &gt;=3 blocks of TOTAL movement within 25 ticks of the hit
     * (cumulative, not net, displacement — PanicGoal may chain several short
     * random runs). The only other movement source
     * (WaterAvoidingRandomStrollGoal@8, 1/120-per-tick trigger) passes all four
     * windows with probability ~(0.19)^4 ~ 0.1%, while a working PanicGoal
     * (speed 1.5, ~0.4 blocks/tick while fleeing) passes essentially always on
     * the open pad.
     */
    @SuppressWarnings({"removal", "deprecation"})
    @GameTest(template = "empty_large", timeoutTicks = 1600)
    public void boyfriend_tempt_panic_door(GameTestHelper helper) {
        // Arena: walled pad so pathing is real but contained.
        pad(helper, 6, 6, 42, 42, false);
        ring(helper, 6, 6, 42, 42);
        // Door cell: 3x3 interior, walls y2..y3, single wooden door in the south wall.
        for (int x = 30; x <= 34; x++) {
            for (int z = 20; z <= 24; z++) {
                if (x == 30 || x == 34 || z == 20 || z == 24) {
                    helper.setBlock(new BlockPos(x, 2, z), Blocks.STONE);
                    helper.setBlock(new BlockPos(x, 3, z), Blocks.STONE);
                }
            }
        }
        BlockState doorLower = Blocks.OAK_DOOR.defaultBlockState();
        final BlockPos doorRel = new BlockPos(32, 2, 24);
        helper.setBlock(doorRel, doorLower);
        helper.setBlock(doorRel.above(), doorLower.setValue(DoorBlock.HALF, DoubleBlockHalf.UPPER));

        final Boyfriend bf = helper.spawn(ModEntities.BOYFRIEND.get(), new BlockPos(16, 2, 16));
        final ServerPlayer sp = helper.makeMockServerPlayerInLevel();
        final Vec3 spPos = helper.absoluteVec(new Vec3(23.0, 2.0, 16.0)); // 7 blocks from the boyfriend, tempt range 10
        sp.teleportTo(helper.getLevel(), spPos.x, spPos.y, spPos.z, 0.0f, 0.0f);
        sp.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.COOKED_BEEF));

        final Cow panicAttacker = helper.spawnWithNoFreeWill(EntityType.COW, new BlockPos(8, 2, 8));
        final int[] state = {0};        // 0 tempt, 1 panic, 2 door, 3 done
        final int[] dwell = {0};
        final int[] trial = {0};
        final int[] trialTick = {0};
        final double[] trialMoved = {0.0};
        final Vec3[] lastPos = new Vec3[1];
        final boolean[] removedSp = {false};

        helper.onEachTick(() -> {
            switch (state[0]) {
                case 0 -> { // tempt: the boyfriend must approach and linger near the beef holder
                    if (bf.distanceTo(sp) < 4.0) dwell[0]++;
                    if (dwell[0] >= 60) {
                        helper.getLevel().getServer().getPlayerList().remove(sp);
                        removedSp[0] = true;
                        state[0] = 1;
                        trialTick[0] = -40; // settle time before the first panic trial
                    }
                }
                case 1 -> { // panic: 4 hurt trials, each must accumulate >=3 blocks of movement in 25 ticks
                    trialTick[0]++;
                    if (trialTick[0] == 0) {
                        trialMoved[0] = 0.0;
                        lastPos[0] = bf.position();
                        bf.hurt(bf.damageSources().mobAttack(panicAttacker), 1.0f);
                    } else if (trialTick[0] > 0) {
                        trialMoved[0] += bf.position().distanceTo(lastPos[0]);
                        lastPos[0] = bf.position();
                        if (trialMoved[0] >= 3.0) {
                            trial[0]++;
                            trialTick[0] = -95; // wait out the 100-tick revenge timer between trials
                            if (trial[0] >= 4) {
                                state[0] = 2;
                                Vec3 cell = helper.absoluteVec(new Vec3(32.0, 2.0, 22.0));
                                bf.moveTo(cell.x, cell.y, cell.z, 0.0f, 0.0f);
                                bf.getNavigation().stop();
                            }
                        } else if (trialTick[0] > 25) {
                            helper.fail("Boyfriend did not panic-run >=3 cumulative blocks within 25 ticks of "
                                    + "being hurt (trial " + (trial[0] + 1) + " of 4; PanicGoal(1.5)@6, orig Boyfriend.java:131)");
                        }
                    }
                }
                case 2 -> { // door: path out of the closed cell; OpenDoorGoal must open the door
                    if (helper.getTick() % 20 == 0) {
                        Vec3 outside = helper.absoluteVec(new Vec3(32.0, 2.0, 29.0));
                        bf.getNavigation().moveTo(outside.x, outside.y, outside.z, 1.0);
                    }
                    BlockState door = helper.getBlockState(doorRel);
                    if (door.getBlock() instanceof DoorBlock && door.getValue(DoorBlock.OPEN)) {
                        bf.discard();
                        panicAttacker.discard();
                        state[0] = 3;
                    }
                }
                default -> { }
            }
        });
        helper.succeedWhen(() -> {
            helper.assertTrue(state[0] == 3, "tempt->panic->door phases must all complete (state=" + state[0] + ")");
            if (!removedSp[0]) helper.getLevel().getServer().getPlayerList().remove(sp);
        });
    }

    // =====================================================================
    // i030-ent-a-080 — CaveFisher hunts passive animals
    // =====================================================================

    /**
     * Checklist i030-ent-a-080 (ENT-A-080).
     * Documented value: phase_c_reports/C1_entities_A_C.md:62 — the CaveFisher
     * preys on passive mobs, not only players: NearestAttackableTargetGoal&lt;Animal&gt;
     * at priority 3 (orig CaveFisher.java:193-228). Port: entity/CaveFisher.java:62-66.
     * The fisher is never provoked; any cow damage is proactive predation.
     */
    @GameTest(template = "empty_large", timeoutTicks = 800)
    public void cavefisher_hunts_passives(GameTestHelper helper) {
        pad(helper, 16, 16, 32, 32, false);
        final CaveFisher fisher = helper.spawn(ModEntities.CAVE_FISHER.get(), new BlockPos(24, 2, 24));
        final Cow cow1 = helper.spawnWithNoFreeWill(EntityType.COW, new BlockPos(22, 2, 24));
        final Cow cow2 = helper.spawnWithNoFreeWill(EntityType.COW, new BlockPos(26, 2, 24));
        setMaxHealth(cow1, 1000.0);
        setMaxHealth(cow2, 1000.0);
        final boolean[] done = {false};
        helper.onEachTick(() -> {
            if (done[0]) return;
            if (cow1.getHealth() < 1000.0f || cow2.getHealth() < 1000.0f) {
                fisher.discard();
                cow1.discard();
                cow2.discard();
                done[0] = true;
            }
        });
        helper.succeedWhen(() -> helper.assertTrue(done[0],
                "unprovoked CaveFisher must attack a passive animal (ENT-A-080)"));
    }

    // =====================================================================
    // i031-ent-a-082 — Cephadrome target list + Kraken x1.5
    // =====================================================================

    /**
     * Checklist i031-ent-a-082 (ENT-A-082).
     * Documented values: phase_c_reports/C1_entities_A_C.md:63 — targets Mothra and
     * untamed Leon/GammaMetroid/WaterDragon (orig Cephadrome.java:404-432,515-573);
     * Kraken takes x1.5 melee damage (orig Cephadrome.java:421-424). Port:
     * entity/Cephadrome.java:98 (MELEE_DAMAGE 70), :309 (Kraken x1.5), :375-396
     * (isSuitableTarget). Positive phases are damage-observed (the scan runs on a
     * 1-in-7 per-tick roll and never calls setTarget); the tamed-Leon phase is a
     * deterministic negative — a tamed Leon is filtered before any dice roll.
     */
    @GameTest(template = "empty_large", timeoutTicks = 2300)
    public void cephadrome_targets_and_kraken_bonus(GameTestHelper helper) {
        final Vec3 cephPos = helper.absoluteVec(new Vec3(24.0, 8.0, 24.0));
        final Vec3 victimPos = helper.absoluteVec(new Vec3(24.0, 8.0, 27.0)); // dist 3 < melee range 6+w/2

        // Phase table: 4 positives (damage expected) then tamed-Leon negative.
        final List<java.util.function.Supplier<Mob>> spawners = List.of(
                () -> helper.spawnWithNoFreeWill(ModEntities.MOTHRA.get(), new BlockPos(24, 8, 27)),
                () -> helper.spawnWithNoFreeWill(ModEntities.ENTITY_LEON.get(), new BlockPos(24, 8, 27)),
                () -> helper.spawnWithNoFreeWill(ModEntities.ENTITY_GAMMA_METROID.get(), new BlockPos(24, 8, 27)),
                () -> helper.spawnWithNoFreeWill(ModEntities.WATER_DRAGON.get(), new BlockPos(24, 8, 27)));
        final String[] names = {"Mothra", "untamed Leon", "untamed GammaMetroid", "untamed WaterDragon"};

        final int[] phase = {0};
        final int[] phaseTick = {0};
        final Cephadrome[] ceph = {null};
        final Mob[] victim = {null};
        final boolean[] done = {false};

        helper.onEachTick(() -> {
            if (done[0]) return;
            if (victim[0] == null) {
                ceph[0] = helper.spawn(ModEntities.CEPHADROME.get(), new BlockPos(24, 8, 24));
                if (phase[0] < 4) {
                    victim[0] = spawners.get(phase[0]).get();
                } else {
                    EntityLeon leon = helper.spawnWithNoFreeWill(ModEntities.ENTITY_LEON.get(), new BlockPos(24, 8, 27));
                    leon.setTame(true, false);
                    leon.setOwnerUUID(UUID.randomUUID());
                    victim[0] = leon;
                }
                setMaxHealth(victim[0], 100000.0);
                phaseTick[0] = 0;
            }
            pin(ceph[0], cephPos);
            pin(victim[0], victimPos);
            phaseTick[0]++;

            if (phase[0] < 4) {
                if (victim[0].getHealth() < 100000.0f) {
                    ceph[0].discard();
                    victim[0].discard();
                    victim[0] = null;
                    phase[0]++;
                } else if (phaseTick[0] > 400) {
                    // scan fires 1-in-7/tick; P(no roll in 400 ticks) = (6/7)^400 ~ 1e-27
                    helper.fail("Cephadrome never attacked " + names[phase[0]] + " within 400 ticks (ENT-A-082)");
                }
            } else if (phase[0] == 4) {
                helper.assertValueEqual(victim[0].getHealth(), 100000.0f,
                        "tamed Leon must never be attacked (entity/Cephadrome.java:383)");
                if (phaseTick[0] > 200) {
                    // Kraken x1.5: direct doHurtTarget with armor zeroed for exact numbers.
                    Kraken kraken = helper.spawn(ModEntities.KRAKEN.get(), new BlockPos(24, 8, 30));
                    kraken.setNoGravity(true);
                    zeroArmor(kraken);
                    setMaxHealth(kraken, 5000.0);
                    ceph[0].doHurtTarget(kraken);
                    helper.assertValueEqual(kraken.getHealth(), 5000.0f - 105.0f,
                            "Kraken takes MELEE_DAMAGE 70 x 1.5 = 105 (orig Cephadrome.java:421-424)");
                    Cow control = helper.spawnWithNoFreeWill(EntityType.COW, new BlockPos(24, 8, 21));
                    control.setNoGravity(true);
                    zeroArmor(control);
                    setMaxHealth(control, 500.0);
                    ceph[0].doHurtTarget(control);
                    helper.assertValueEqual(control.getHealth(), 500.0f - 70.0f,
                            "non-Kraken control takes the flat 70 (entity/Cephadrome.java:98,309)");
                    kraken.discard();
                    control.discard();
                    ceph[0].discard();
                    victim[0].discard();
                    done[0] = true;
                }
            }
        });
        helper.succeedWhen(() -> helper.assertTrue(done[0], "all Cephadrome phases finished"));
    }

    // =====================================================================
    // i032-ent-a-087 — Chipmunk apple taming / dead-bush release
    // =====================================================================

    /**
     * Checklist i032-ent-a-087 (ENT-A-087).
     * Documented values: phase_c_reports/C1_entities_A_C.md:64 — tame item is an
     * APPLE at 1-in-2 (orig Chipmunk.java:141), untame item is a DEAD BUSH
     * (orig Chipmunk.java:172). Port: entity/Chipmunk.java:111-160.
     */
    @GameTest(template = "empty_large", timeoutTicks = 100)
    public void chipmunk_apple_tame_dead_bush_release(GameTestHelper helper) {
        Chipmunk chipmunk = helper.spawnWithNoFreeWill(ModEntities.CHIPMUNK.get(), new BlockPos(24, 6, 24));
        chipmunk.setNoGravity(true);
        Player player = mockPlayerAt(helper, helper.absoluteVec(new Vec3(24.0, 6.0, 25.5)));

        // n=150 tame attempts at the documented p=1/2 (nextInt(2)==0), mean 75.
        // Band [30,120]: Hoeffding P(|X-75| >= 45) <= 2*exp(-2*150*0.09) = 2*exp(-27) ~ 3.7e-12.
        int successes = 0;
        int releases = 0;
        for (int i = 0; i < 150; i++) {
            helper.assertFalse(chipmunk.isTame(), "attempt starts untamed");
            player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.APPLE));
            chipmunk.mobInteract(player, InteractionHand.MAIN_HAND);
            helper.assertValueEqual(player.getItemInHand(InteractionHand.MAIN_HAND).getCount(), 0,
                    "apple consumed on every attempt, success or not (orig Chipmunk.java:141)");
            if (chipmunk.isTame()) {
                successes++;
                helper.assertValueEqual(chipmunk.getOwnerUUID(), player.getUUID(), "tame sets the feeding player as owner");
                // dead bush releases it (orig Chipmunk.java:172)
                player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.DEAD_BUSH));
                chipmunk.mobInteract(player, InteractionHand.MAIN_HAND);
                helper.assertValueEqual(player.getItemInHand(InteractionHand.MAIN_HAND).getCount(), 0, "dead bush consumed");
                helper.assertFalse(chipmunk.isTame(), "dead bush releases the chipmunk");
                helper.assertTrue(chipmunk.getOwnerUUID() == null, "owner cleared on release");
                releases++;
            }
        }
        helper.assertTrue(successes >= 30 && successes <= 120,
                "tame successes " + successes + "/150 outside [30,120] for the documented 1-in-2 rate");
        helper.assertValueEqual(releases, successes, "every tame was reversed by a dead bush");
        chipmunk.discard();
        helper.succeed();
    }

    // =====================================================================
    // i034-ent-a-112 — Cryolophosaurus proactive hunting
    // =====================================================================

    /**
     * Checklist i034-ent-a-112 (ENT-A-112).
     * Documented value: phase_c_reports/C1_entities_A_C.md:69 — 1-in-5 proactive
     * hunt over a 9x2x9 box with an exclusion list (orig Cryolophosaurus.java:141-211);
     * the audited pre-fix port was retaliation-only. Port:
     * entity/Cryolophosaurus.java:74-122. The prey is never provoked, so any pig
     * damage proves proactive aggression. Timeout is padded because another test
     * in this suite briefly sets playNicely=true (which pauses this scan too).
     */
    @GameTest(template = "empty_large", timeoutTicks = 1000)
    public void cryolophosaurus_proactive_hunt(GameTestHelper helper) {
        final Vec3 dinoPos = helper.absoluteVec(new Vec3(24.0, 6.0, 24.0));
        final Vec3 pigPos = helper.absoluteVec(new Vec3(24.0, 6.0, 25.5)); // distSq < 5 bite gate
        final Cryolophosaurus dino = helper.spawn(ModEntities.CRYOLOPHOSAURUS.get(), new BlockPos(24, 6, 24));
        final Pig pig = helper.spawnWithNoFreeWill(EntityType.PIG, new BlockPos(24, 6, 25));
        setMaxHealth(pig, 1000.0);
        final boolean[] done = {false};
        helper.onEachTick(() -> {
            if (done[0]) return;
            pin(dino, dinoPos);
            pin(pig, pigPos);
            if (pig.getHealth() < 1000.0f) {
                dino.discard();
                pig.discard();
                done[0] = true;
            }
        });
        helper.succeedWhen(() -> helper.assertTrue(done[0],
                "Cryolophosaurus must proactively bite adjacent prey (ENT-A-112)"));
    }

    // =====================================================================
    // i035-ent-d-002-006 — Dragon taming / heal / bone / diamond->Spyro
    // =====================================================================

    /**
     * Checklist i035-ent-d-002-006 (ENT-D-002 + ENT-D-006).
     * Documented values: phase_c_reports/C2_entities_D_I.md:15 — raw BEEF tames at
     * 1-in-5 and heals to full (orig Dragon.java:1212-1219,1245-1252); C2:16 — a
     * DIAMOND on a tamed dragon spawns a tamed Spyro and discards the adult
     * (orig Dragon.java:1351-1369). Bones are not an interaction item at all.
     * Port: entity/Dragon.java:906-1074, mygetMaxHealth()=200 (Dragon.java:146).
     * Everything runs in a single tick, so the (flying) dragon never moves.
     */
    @GameTest(template = "empty_large", timeoutTicks = 100)
    public void dragon_beef_tame_heal_bone_diamond(GameTestHelper helper) {
        Dragon dragon = helper.spawn(ModEntities.DRAGON.get(), new BlockPos(24, 8, 24));
        dragon.setNoGravity(true);
        Player player = mockPlayerAt(helper, dragon.position().add(2.0, 0.0, 0.0)); // distSq < 25 gates

        // (a) statistical taming: n=600 beef attempts at p=1/5 (nextInt(5)==1), mean 120.
        //     Band [40,220]: Chernoff lower tail exp(-(80)^2/(2*120)) = exp(-26.7) ~ 2.5e-12,
        //     upper tail exp(-(100)^2/(3*120)) = exp(-27.8) ~ 8e-13 — both << 1e-9.
        int successes = 0;
        for (int i = 0; i < 600; i++) {
            if (dragon.isTame()) { // reset with TNT (release item, entity/Dragon.java:961-971)
                player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.TNT));
                dragon.mobInteract(player, InteractionHand.MAIN_HAND);
                helper.assertFalse(dragon.isTame(), "TNT releases the dragon between attempts");
            }
            dragon.setHealth(150.0f); // so a tame success visibly heals to full
            player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.BEEF));
            dragon.mobInteract(player, InteractionHand.MAIN_HAND);
            helper.assertValueEqual(player.getItemInHand(InteractionHand.MAIN_HAND).getCount(), 0,
                    "beef consumed on every attempt (orig Dragon.java:1212-1219)");
            if (dragon.isTame()) {
                successes++;
                helper.assertValueEqual(dragon.getOwnerUUID(), player.getUUID(), "tame success sets owner");
                helper.assertValueEqual(dragon.getHealth(), 200.0f,
                        "tame success heals to mygetMaxHealth()=200 (entity/Dragon.java:146,918)");
            }
        }
        helper.assertTrue(successes >= 40 && successes <= 220,
                "tame successes " + successes + "/600 outside [40,220] for the documented 1-in-5 rate");

        // (b) bones are ignored on an untamed dragon: no consume, no tame, no action.
        if (dragon.isTame()) {
            player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.TNT));
            dragon.mobInteract(player, InteractionHand.MAIN_HAND);
        }
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.BONE));
        InteractionResult boneResult = dragon.mobInteract(player, InteractionHand.MAIN_HAND);
        helper.assertValueEqual(player.getItemInHand(InteractionHand.MAIN_HAND).getCount(), 1,
                "bone is not consumed (audit-corrected ENT-D-002: beef, not bone)");
        helper.assertFalse(dragon.isTame(), "bone does not tame");
        helper.assertFalse(boneResult.consumesAction(), "bone interaction falls through (PASS)");

        // (c) beef heals a damaged TAMED dragon to full (orig Dragon.java:1245-1252).
        dragon.setTame(true, false);
        dragon.setOwnerUUID(player.getUUID());
        dragon.setHealth(100.0f);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.BEEF));
        dragon.mobInteract(player, InteractionHand.MAIN_HAND);
        helper.assertValueEqual(dragon.getHealth(), 200.0f, "beef heals a tamed dragon to full");
        helper.assertValueEqual(player.getItemInHand(InteractionHand.MAIN_HAND).getCount(), 0, "healing beef consumed");

        // (d) diamond on the TAMED adult -> tamed Spyro replaces it (orig Dragon.java:1351-1369).
        Vec3 dragonPos = dragon.position();
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.DIAMOND));
        dragon.mobInteract(player, InteractionHand.MAIN_HAND);
        helper.assertTrue(dragon.isRemoved(), "the adult dragon is discarded");
        helper.assertValueEqual(player.getItemInHand(InteractionHand.MAIN_HAND).getCount(), 0, "diamond consumed");
        List<EntitySpyro> spyros = helper.getLevel().getEntitiesOfClass(EntitySpyro.class,
                new AABB(BlockPos.containing(dragonPos)).inflate(3.0));
        helper.assertValueEqual(spyros.size(), 1, "exactly one Spyro spawned at the adult's position");
        helper.assertTrue(spyros.get(0).isTame(), "the Spyro is tamed (adult was tame)");
        helper.assertValueEqual(spyros.get(0).getOwnerUUID(), player.getUUID(), "the Spyro keeps the owner");
        spyros.get(0).discard();
        helper.succeed();
    }

    // =====================================================================
    // i036-ent-d-014 — Emperor Scorpion baby flood
    // =====================================================================

    /**
     * Checklist i036-ent-d-014 (ENT-D-014).
     * Documented values: phase_c_reports/C2_entities_D_I.md:18 — inside a
     * nextInt(4)==0 AI gate, a nextInt(20)==1 roll while a combat target exists
     * spawns ONE Scorpion at the self/target midpoint, uncapped and cooldown-free
     * (orig EmperorScorpion.java:408,437-438). Port:
     * entity/EntityEmperorScorpion.java:158-186. Counting &gt;=2 distinct babies
     * proves both the spawn and the absence of a cap; babies are discarded on
     * sight so the arena can never flood for real.
     */
    @GameTest(template = "empty_large", timeoutTicks = 2300)
    public void emperor_scorpion_baby_flood(GameTestHelper helper) {
        final Vec3 empPos = helper.absoluteVec(new Vec3(24.0, 8.0, 21.0));
        final Vec3 cowPos = helper.absoluteVec(new Vec3(24.0, 8.0, 27.0));
        final EntityEmperorScorpion emperor = helper.spawn(ModEntities.ENTITY_EMPEROR_SCORPION.get(), new BlockPos(24, 8, 21));
        final Cow cow = helper.spawnWithNoFreeWill(EntityType.COW, new BlockPos(24, 8, 27));
        setMaxHealth(cow, 100000.0);
        final Set<UUID> seen = new HashSet<>();
        final boolean[] done = {false};

        helper.onEachTick(() -> {
            if (done[0]) return;
            pin(emperor, empPos);
            pin(cow, cowPos);
            cow.removeAllEffects();
            cow.setHealth(cow.getMaxHealth());
            emperor.setTarget(cow); // hold the combat-target gate open
            for (EntityScorpion baby : helper.getLevel().getEntitiesOfClass(
                    EntityScorpion.class, helper.getBounds().inflate(16.0))) {
                seen.add(baby.getUUID());
                baby.discard();
            }
            // Per-tick spawn p = (1/4)(1/20) = 1/80; over the ~2200 usable ticks the
            // expected count is ~27, and P(fewer than 2) = e^-27 * (1+27) ~ 5e-11 < 1e-9.
            if (seen.size() >= 2) {
                emperor.discard();
                cow.discard();
                done[0] = true;
            }
        });
        helper.succeedWhen(() -> helper.assertTrue(done[0],
                ">=2 distinct baby scorpions must spawn mid-combat (ENT-D-014, uncapped)"));
    }

    // =====================================================================
    // i037-ent-d-022 — Entity cage capture matrix
    // =====================================================================

    private record CageOutcome(int emptyCages, int cagedItems, List<ResourceLocation> cagedIds) {}

    /**
     * Fires one cage at the victim by single-stepping the projectile's own tick()
     * (the projectile is never added to the level, so the server cannot double-tick
     * it; onHitEntity runs the identical code path). Returns the classified drops.
     */
    private CageOutcome throwCage(GameTestHelper helper, LivingEntity victim) {
        ServerLevel level = helper.getLevel();
        Vec3 c = victim.getBoundingBox().getCenter();
        double back = victim.getBbWidth() / 2.0 + 1.5;
        EntityCage cage = new EntityCage(ModEntities.ENTITY_CAGE.get(), level);
        cage.setPos(c.x - back, c.y, c.z);
        cage.setDeltaMovement(back + 1.0, 0.0, 0.0);
        for (int i = 0; i < 4 && !cage.isRemoved(); i++) {
            cage.tick();
        }
        helper.assertTrue(cage.isRemoved(), "cage projectile must resolve (hit) within 4 ticks");
        int empty = 0;
        int caged = 0;
        List<ResourceLocation> ids = new ArrayList<>();
        for (ItemEntity drop : level.getEntitiesOfClass(ItemEntity.class,
                new AABB(BlockPos.containing(c)).inflate(8.0))) {
            ItemStack s = drop.getItem();
            if (s.is(ModItems.CAGE_EMPTY.get())) {
                empty += s.getCount();
            } else if (s.is(ModItems.CAGED_MOB.get())) {
                caged += s.getCount();
                ids.add(s.get(ModDataComponents.CAGED_ENTITY.get()));
            }
            drop.discard();
        }
        return new CageOutcome(empty, caged, ids);
    }

    /** Loops fresh victims of the given type until one capture happens; asserts drop count/id. */
    private <T extends Mob> void assertMultiCapture(GameTestHelper helper, EntityType<T> type,
                                                    BlockPos rel, int expectedCount, String label) {
        ResourceLocation expectedId = net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.getKey(type);
        // Capture chance per throw is 0.8 (only the 20% base roll can miss for
        // these species): P(no capture in 100 throws) = 0.2^100 ~ 1.3e-70.
        for (int i = 0; i < 100; i++) {
            T victim = helper.spawn(type, rel);
            victim.setNoGravity(true);
            CageOutcome o = throwCage(helper, victim);
            helper.assertValueEqual(o.emptyCages(), 0,
                    label + " must never return an empty cage (no species escape dice)");
            if (o.cagedItems() > 0) {
                helper.assertTrue(victim.isRemoved(), label + " is consumed on capture");
                helper.assertValueEqual(o.cagedItems(), expectedCount,
                        label + " capture drops x" + expectedCount + " caged items (ENT-D-022 multi-count)");
                for (ResourceLocation id : o.cagedIds()) {
                    helper.assertValueEqual(id, expectedId, label + " caged item carries the right entity id");
                }
                return;
            }
            victim.discard(); // base-fail trial: mob survives, cage lost
        }
        helper.fail(label + ": no capture within 100 throws (p=0.8 each — impossible unless broken)");
    }

    /**
     * Checklist i037-ent-d-022 (ENT-D-022).
     * Documented values: phase_c_reports/C2_entities_D_I.md:19 — 20% base failure on
     * ANY entity hit (cage lost, nothing drops, orig EntityCage.java:160,935-937);
     * players always escape with an empty cage back (:172-178); tamed
     * Girlfriend/Boyfriend eat the cage with no drop (:292-299); per-species escape
     * dice incl. Ghast/Enderman 20% and Kraken 95%; multi-drops Bat x2 (:197),
     * Cockateil x4 (:505), AttackSquid x6 (:509); Creeper/Villager/IronGolem plain
     * captures. Port: entity/EntityCage.java:98-276.
     */
    @GameTest(template = "empty_large", timeoutTicks = 400)
    public void entity_cage_capture_matrix(GameTestHelper helper) {
        // (1) Players: never caged; empty cage returns on the 80% non-base-fail hits.
        //     P(no empty-cage return in 40 throws) = 0.2^40 ~ 1e-28.
        Player playerVictim = addedMockPlayerAt(helper, helper.absoluteVec(new Vec3(10.0, 8.0, 10.0)));
        boolean sawEmptyReturn = false;
        for (int i = 0; i < 40; i++) {
            CageOutcome o = throwCage(helper, playerVictim);
            helper.assertValueEqual(o.cagedItems(), 0, "a player can never be caged (orig EntityCage.java:172-178)");
            if (o.emptyCages() > 0) sawEmptyReturn = true;
        }
        helper.assertTrue(sawEmptyReturn, "player hits must bounce the cage back empty");
        helper.assertFalse(playerVictim.isRemoved(), "player unharmed by cage hits");
        playerVictim.discard();

        // (2) Creeper: no escape dice — outcomes are only {capture, base-fail}, never empty-cage.
        assertMultiCapture(helper, EntityType.CREEPER, new BlockPos(14, 8, 10), 1, "Creeper");
        // (6) Villager and Iron Golem: plain 1x captures.
        assertMultiCapture(helper, EntityType.VILLAGER, new BlockPos(18, 8, 10), 1, "Villager");
        assertMultiCapture(helper, EntityType.IRON_GOLEM, new BlockPos(22, 8, 10), 1, "Iron Golem");
        // (5) Multi-count species: Bat x2, Cockateil x4, AttackSquid x6.
        assertMultiCapture(helper, EntityType.BAT, new BlockPos(26, 8, 10), 2, "Bat");
        assertMultiCapture(helper, ModEntities.COCKATEIL.get(), new BlockPos(30, 8, 10), 4, "Cockateil");
        assertMultiCapture(helper, ModEntities.ATTACK_SQUID.get(), new BlockPos(34, 8, 10), 6, "AttackSquid");

        // (3) Ghast + Enderman: 20% escape dice — both captures AND empty-cage escapes
        //     must appear. Per throw: capture 0.64, escape-return 0.16, base-fail 0.2.
        //     P(either outcome absent in 400 throws) <= 0.36^400 + 0.84^400 ~ 4e-31 + 3e-31.
        for (int species = 0; species < 2; species++) {
            String name = species == 0 ? "Ghast" : "Enderman";
            int captures = 0;
            int escapes = 0;
            for (int i = 0; i < 400 && (captures == 0 || escapes == 0); i++) {
                Mob victim = species == 0
                        ? helper.spawn(EntityType.GHAST, new BlockPos(24, 9, 24))
                        : helper.spawn(EntityType.ENDERMAN, new BlockPos(24, 9, 24));
                victim.setNoGravity(true);
                CageOutcome o = throwCage(helper, victim);
                if (o.cagedItems() > 0) {
                    captures++;
                    helper.assertTrue(victim.isRemoved(), "captured mob consumed");
                } else {
                    if (o.emptyCages() > 0) escapes++;
                    helper.assertFalse(victim.isRemoved(), "escaped/base-fail mob survives");
                    victim.discard();
                }
            }
            helper.assertTrue(captures >= 1, name + " must be capturable (80% of non-escape hits)");
            helper.assertTrue(escapes >= 1, name + " must sometimes escape with an empty cage (20% dice)");
        }

        // (4) Kraken: 95% escape dice. n=600 throws; capture p = 0.8*0.05 = 0.04 (mean 24),
        //     escape-return p = 0.8*0.95 = 0.76 (mean 456).
        //     P(0 captures) = 0.96^600 ~ 2.3e-11; P(captures > 75): Chernoff
        //     exp(-(51)^2/(3*24)) ~ 2e-16; P(escapes < 300): exp(-(156)^2/(2*456)) ~ 2.6e-12.
        int kCaptures = 0;
        int kEscapes = 0;
        Kraken kraken = helper.spawn(ModEntities.KRAKEN.get(), new BlockPos(24, 9, 30));
        kraken.setNoGravity(true);
        for (int i = 0; i < 600; i++) {
            if (kraken.isRemoved()) {
                kraken = helper.spawn(ModEntities.KRAKEN.get(), new BlockPos(24, 9, 30));
                kraken.setNoGravity(true);
            }
            CageOutcome o = throwCage(helper, kraken);
            if (o.cagedItems() > 0) kCaptures++;
            else if (o.emptyCages() > 0) kEscapes++;
        }
        kraken.discard();
        helper.assertTrue(kCaptures >= 1, "Kraken capturable at the rare 5% (0 in 600 is ~2e-11)");
        helper.assertTrue(kCaptures <= 75, "Kraken captures " + kCaptures + "/600 — escape dice missing?");
        helper.assertTrue(kEscapes >= 300, "Kraken escapes " + kEscapes + "/600 — expected ~456 at the 95% dice");

        // (7) Tamed Girlfriend/Boyfriend: cage eaten, no drop, mob survives —
        //     deterministic across ALL random branches (base-fail also drops nothing).
        Girlfriend gf = helper.spawn(ModEntities.GIRLFRIEND.get(), new BlockPos(24, 8, 36));
        gf.setNoGravity(true);
        gf.setTame(true, false);
        gf.setOwnerUUID(UUID.randomUUID());
        Boyfriend bf = helper.spawn(ModEntities.BOYFRIEND.get(), new BlockPos(28, 8, 36));
        bf.setNoGravity(true);
        bf.setTame(true, false);
        bf.setOwnerUUID(UUID.randomUUID());
        for (int i = 0; i < 5; i++) {
            CageOutcome og = throwCage(helper, gf);
            helper.assertValueEqual(og.emptyCages() + og.cagedItems(), 0,
                    "tamed Girlfriend eats the cage — nothing ever drops (orig EntityCage.java:292-299)");
            helper.assertFalse(gf.isRemoved(), "tamed Girlfriend survives");
            CageOutcome ob = throwCage(helper, bf);
            helper.assertValueEqual(ob.emptyCages() + ob.cagedItems(), 0,
                    "tamed Boyfriend eats the cage — nothing ever drops (orig EntityCage.java:292-299)");
            helper.assertFalse(bf.isRemoved(), "tamed Boyfriend survives");
        }
        gf.discard();
        bf.discard();
        helper.succeed();
    }

    // =====================================================================
    // i039-ent-k-013 — LeafMonster prey allow-list + playNicely
    // =====================================================================

    /**
     * Checklist i039-ent-k-013 (ENT-K-013).
     * Documented values: phase_c_reports/C3_entities_K_R.md:16 — explicit prey
     * allow-list Ant/Butterfly/LunaMoth/non-creative players
     * (orig LeafMonster.java:178-207) and the playNicely gate (:210-212).
     * Port: entity/EntityLeafMonster.java:134-156. Cow and pig sit inside the scan
     * box the whole time and must never be touched (deterministic negative); each
     * allow-listed prey in turn must be bitten.
     */
    @GameTest(template = "empty_large", timeoutTicks = 1900)
    public void leaf_monster_prey_allowlist_play_nicely(GameTestHelper helper) {
        final Vec3 monsterPos = helper.absoluteVec(new Vec3(24.0, 8.0, 24.0));
        final EntityLeafMonster monster = helper.spawn(ModEntities.ENTITY_LEAF_MONSTER.get(), new BlockPos(24, 8, 24));

        final Mob ant = helper.spawnWithNoFreeWill(ModEntities.ENTITY_ANT.get(), new BlockPos(24, 8, 25));
        final Mob butterfly = helper.spawnWithNoFreeWill(ModEntities.ENTITY_BUTTERFLY.get(), new BlockPos(26, 8, 24));
        final Mob luna = helper.spawnWithNoFreeWill(ModEntities.ENTITY_LUNA_MOTH.get(), new BlockPos(22, 8, 24));
        final Cow cow = helper.spawnWithNoFreeWill(EntityType.COW, new BlockPos(24, 8, 22));
        final Pig pig = helper.spawnWithNoFreeWill(EntityType.PIG, new BlockPos(25, 8, 22));
        final Mob[] preyList = {ant, butterfly, luna};
        final String[] preyNames = {"Ant", "Butterfly", "LunaMoth"};
        for (Mob prey : preyList) setMaxHealth(prey, 1000.0);
        setMaxHealth(cow, 1000.0);
        setMaxHealth(pig, 1000.0);

        final boolean playNicelyOld = OreSpawnConfig.PLAY_NICELY.get();
        final int[] phase = {0};      // 0..2 = prey phases, 3 = playNicely, 4 = done
        final int[] phaseTick = {0};
        helper.onEachTick(() -> {
            if (phase[0] >= 4) return;
            pin(monster, monsterPos);
            // Non-prey must NEVER be touched — the allow-list filters them before any dice.
            helper.assertValueEqual(cow.getHealth(), 1000.0f, "cow is not LeafMonster prey (ENT-K-013)");
            helper.assertValueEqual(pig.getHealth(), 1000.0f, "pig is not LeafMonster prey (ENT-K-013)");
            pin(cow, monsterPos.add(0.0, 0.0, -2.0));
            pin(pig, monsterPos.add(1.0, 0.0, -2.0));
            phaseTick[0]++;

            if (phase[0] < 3) {
                // current prey pinned in bite range (distSq<5); the others parked at 3.5
                for (int i = phase[0]; i < 3; i++) {
                    Mob p = preyList[i];
                    if (p.isRemoved()) continue;
                    Vec3 spot = (i == phase[0])
                            ? monsterPos.add(0.0, 0.0, 1.5)
                            : monsterPos.add(3.5, 0.0, i - 1.0);
                    pin(p, spot);
                }
                Mob current = preyList[phase[0]];
                if (current.getHealth() < 1000.0f) {
                    current.discard();
                    phase[0]++;
                    phaseTick[0] = 0;
                    if (phase[0] == 3) OreSpawnConfig.PLAY_NICELY.set(true);
                } else if (phaseTick[0] > 500) {
                    // per-tick bite p ~ (1/4)*(1/8 or 1/10 combined ~0.2125) ~ 5.3%;
                    // P(no bite in 500 ticks) = (0.947)^500 ~ 1.5e-12.
                    helper.fail("LeafMonster never bit the allow-listed " + preyNames[phase[0]]
                            + " within 500 ticks (ENT-K-013)");
                }
            } else {
                // playNicely=true: findSomethingToAttack short-circuits to null —
                // deterministic peace (orig LeafMonster.java:210-212).
                if (phaseTick[0] == 1) {
                    Mob peace = helper.spawnWithNoFreeWill(ModEntities.ENTITY_ANT.get(), new BlockPos(24, 8, 25));
                    setMaxHealth(peace, 1000.0);
                    preyList[0] = peace;
                }
                Mob peace = preyList[0];
                pin(peace, monsterPos.add(0.0, 0.0, 1.5));
                helper.assertValueEqual(peace.getHealth(), 1000.0f,
                        "LeafMonster never hunts while playNicely=true (orig LeafMonster.java:210-212)");
                if (phaseTick[0] >= 150) {
                    OreSpawnConfig.PLAY_NICELY.set(playNicelyOld);
                    peace.discard();
                    monster.discard();
                    cow.discard();
                    pig.discard();
                    phase[0] = 4;
                }
            }
        });
        helper.succeedWhen(() -> helper.assertTrue(phase[0] >= 4, "all LeafMonster phases finished"));
    }

    // =====================================================================
    // i040-ent-k-019-083 — Leon / RubberDucky tame, untame, tempt
    // =====================================================================

    /**
     * Checklist i040-ent-k-019-083 (ENT-K-019 + ENT-K-083).
     * Documented values: phase_c_reports/C3_entities_K_R.md:17 — Leon's untame item
     * is a DEAD BUSH, not glass (orig Leon.java:1035; port EntityLeon.java:734);
     * C3:27 — RubberDucky tames with raw COD at 1-in-2 and tempts with cod
     * (orig RubberDucky.java:242), dead bush untames (:273-287); wheat is only the
     * vanilla breeding food and never tames. Port: entity/EntityRubberDucky.java:72,175-216,345.
     */
    @SuppressWarnings({"removal", "deprecation"})
    @GameTest(template = "empty_large", timeoutTicks = 900)
    public void leon_ducky_tame_untame_tempt(GameTestHelper helper) {
        // ---- Leon: glass does NOT untame; dead bush does (all in one tick) ----
        EntityLeon leon = helper.spawnWithNoFreeWill(ModEntities.ENTITY_LEON.get(), new BlockPos(12, 6, 12));
        leon.setNoGravity(true);
        Player owner = mockPlayerAt(helper, leon.position().add(2.0, 0.0, 0.0)); // distSq < 49 gates
        leon.setTame(true, false);
        leon.setOwnerUUID(owner.getUUID());

        owner.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.GLASS));
        leon.mobInteract(owner, InteractionHand.MAIN_HAND);
        helper.assertTrue(leon.isTame(), "glass does NOT untame Leon (audit-corrected ENT-K-019)");
        helper.assertValueEqual(leon.getOwnerUUID(), owner.getUUID(), "owner intact after glass");
        helper.assertValueEqual(owner.getItemInHand(InteractionHand.MAIN_HAND).getCount(), 1,
                "glass not consumed (it only toggles sit, EntityLeon.java:770-778)");

        owner.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.DEAD_BUSH));
        leon.mobInteract(owner, InteractionHand.MAIN_HAND);
        helper.assertFalse(leon.isTame(), "dead bush untames Leon (orig Leon.java:1035)");
        helper.assertTrue(leon.getOwnerUUID() == null, "owner cleared");
        helper.assertValueEqual(owner.getItemInHand(InteractionHand.MAIN_HAND).getCount(), 0, "dead bush consumed");
        leon.discard();

        // ---- RubberDucky: cod tames at 1-in-2; dead bush untames; wheat never tames ----
        EntityRubberDucky ducky = helper.spawnWithNoFreeWill(ModEntities.ENTITY_RUBBER_DUCKY.get(), new BlockPos(12, 6, 16));
        ducky.setNoGravity(true);
        Player feeder = mockPlayerAt(helper, ducky.position().add(1.5, 0.0, 0.0));
        // n=150 attempts at p=1/2 (nextInt(2)==0), mean 75. Band [30,120]:
        // Hoeffding 2*exp(-2*150*0.09) = 2*exp(-27) ~ 3.7e-12.
        int successes = 0;
        for (int i = 0; i < 150; i++) {
            feeder.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.COD));
            ducky.mobInteract(feeder, InteractionHand.MAIN_HAND);
            helper.assertValueEqual(feeder.getItemInHand(InteractionHand.MAIN_HAND).getCount(), 0,
                    "cod consumed on every attempt (orig RubberDucky.java:242-272)");
            if (ducky.isTame()) {
                successes++;
                feeder.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.DEAD_BUSH));
                ducky.mobInteract(feeder, InteractionHand.MAIN_HAND);
                helper.assertFalse(ducky.isTame(), "dead bush untames the ducky (orig RubberDucky.java:273-287)");
            }
        }
        helper.assertTrue(successes >= 30 && successes <= 120,
                "cod tame successes " + successes + "/150 outside [30,120] for the documented 1-in-2 rate");
        ducky.discard();

        EntityRubberDucky wheatDucky = helper.spawnWithNoFreeWill(ModEntities.ENTITY_RUBBER_DUCKY.get(), new BlockPos(12, 6, 16));
        wheatDucky.setNoGravity(true);
        feeder.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.WHEAT));
        wheatDucky.mobInteract(feeder, InteractionHand.MAIN_HAND);
        helper.assertFalse(wheatDucky.isTame(),
                "wheat never tames — it is only the vanilla breeding food (EntityRubberDucky.java:345)");
        wheatDucky.discard();

        // ---- cod TEMPT: needs a real ServerPlayer (TemptGoal scans level.players()) ----
        pad(helper, 18, 18, 34, 34, false);
        ring(helper, 18, 18, 34, 34);
        final EntityRubberDucky temptDucky = helper.spawn(ModEntities.ENTITY_RUBBER_DUCKY.get(), new BlockPos(21, 2, 26));
        final ServerPlayer sp = helper.makeMockServerPlayerInLevel();
        Vec3 spPos = helper.absoluteVec(new Vec3(28.0, 2.0, 26.0)); // 7 blocks away, tempt range 10
        sp.teleportTo(helper.getLevel(), spPos.x, spPos.y, spPos.z, 0.0f, 0.0f);
        sp.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.COD));

        final int[] dwell = {0};
        final boolean[] done = {false};
        helper.onEachTick(() -> {
            if (done[0]) return;
            if (temptDucky.distanceTo(sp) < 4.0) dwell[0]++;
            if (dwell[0] >= 60) {
                helper.getLevel().getServer().getPlayerList().remove(sp);
                temptDucky.discard();
                done[0] = true;
            }
        });
        helper.succeedWhen(() -> {
            helper.assertTrue(done[0], "ducky must approach and linger near the cod holder (TemptGoal, orig RubberDucky.java:242)");
            if (!done[0]) helper.getLevel().getServer().getPlayerList().remove(sp);
        });
    }

    // =====================================================================
    // i041-ent-k-023 — Lizard seeks water, never lava
    // =====================================================================

    /**
     * Checklist i041-ent-k-023 (ENT-K-023).
     * Documented values: phase_c_reports/C3_entities_K_R.md:18 — scan_it matches
     * WATER blocks only, never lava/fire (orig Lizard.java:184-227, audit-corrected:
     * the pre-fix port sought lava), scan cadence 1-in-100/tick when dry, nav speed
     * 1.33. Port: entity/Lizard.java:149-203. Symmetric water and lava pools flank
     * the lizard; the mod's scan can only ever navigate it toward the water side,
     * and any lava contact or ignition fails the test instantly (this is exactly
     * the regression the audit described).
     */
    @GameTest(template = "empty_large", timeoutTicks = 2400)
    public void lizard_seeks_water_never_lava(GameTestHelper helper) {
        pad(helper, 14, 14, 34, 34, true); // double layer: pools are recessed into y1 over stone y0
        ring(helper, 14, 14, 34, 34);
        // water pool 2x2 east of center, flush with the floor
        for (int x = 30; x <= 31; x++) for (int z = 24; z <= 25; z++) helper.setBlock(new BlockPos(x, 1, z), Blocks.WATER);
        // lava pool 2x2 west of center, symmetric
        for (int x = 17; x <= 18; x++) for (int z = 24; z <= 25; z++) helper.setBlock(new BlockPos(x, 1, z), Blocks.LAVA);
        final Vec3 waterCenter = helper.absoluteVec(new Vec3(31.0, 1.5, 25.0));

        final Lizard lizard = helper.spawn(ModEntities.LIZARD.get(), new BlockPos(24, 2, 24));
        final int[] waterNavTicks = {0};
        final boolean[] done = {false};
        helper.onEachTick(() -> {
            if (done[0]) return;
            helper.assertFalse(lizard.isInLava(), "lizard must NEVER walk into lava (audit-corrected ENT-K-023)");
            helper.assertFalse(lizard.getRemainingFireTicks() > 0, "lizard must never catch fire");
            BlockPos navTarget = lizard.getNavigation().getTargetPos();
            if (navTarget != null) {
                double dx = navTarget.getX() + 0.5 - waterCenter.x;
                double dz = navTarget.getZ() + 0.5 - waterCenter.z;
                if (dx * dx + dz * dz < 2.5 * 2.5) waterNavTicks[0]++;
            }
            // Success: it physically reached water, or its navigation demonstrably
            // steered at the water pool for >=12 ticks (scan fires 1-in-100/tick).
            if (lizard.isInWater() || waterNavTicks[0] >= 12) {
                lizard.discard();
                done[0] = true;
            }
        });
        helper.succeedWhen(() -> helper.assertTrue(done[0],
                "lizard must path to the WATER pool (orig Lizard.java:184-227)"));
    }

    // =====================================================================
    // i042-ent-k-050-ent-s-031 — crystal-apple-only breeding
    // =====================================================================

    /**
     * Checklist i042-ent-k-050-ent-s-031 (ENT-K-050 + ENT-S-031).
     * Documented values: phase_c_reports/C3_entities_K_R.md:22 — Peacock breeding
     * item is MyCrystalApple only (orig Peacock.java:259-261; port Peacock.java:172-173);
     * phase_c_reports/C4_entities_S_Z.md:26 — StinkBug likewise crystal apple ONLY
     * (orig StinkBug.java:173-175; port EntityStinkBug.java:111-114). Regular
     * apples are not food: no love, no consumption.
     */
    @GameTest(template = "empty_large", timeoutTicks = 2000)
    public void crystal_apple_breeding_peacock_stinkbug(GameTestHelper helper) {
        // two 7x7 walled pens so the pairs stay within BreedGoal's 8-block partner range
        pad(helper, 10, 21, 16, 27, false);
        ring(helper, 10, 21, 16, 27);
        pad(helper, 32, 21, 38, 27, false);
        ring(helper, 32, 21, 38, 27);

        Peacock peacockA = helper.spawn(ModEntities.PEACOCK.get(), new BlockPos(12, 2, 24));
        Peacock peacockB = helper.spawn(ModEntities.PEACOCK.get(), new BlockPos(14, 2, 24));
        EntityStinkBug bugA = helper.spawn(ModEntities.ENTITY_STINK_BUG.get(), new BlockPos(34, 2, 24));
        EntityStinkBug bugB = helper.spawn(ModEntities.ENTITY_STINK_BUG.get(), new BlockPos(36, 2, 24));
        List<net.minecraft.world.entity.animal.Animal> pairs = List.of(peacockA, peacockB, bugA, bugB);

        Player feeder = mockPlayerAt(helper, helper.absoluteVec(new Vec3(24.0, 2.0, 24.0)));
        // negative: a REGULAR apple does nothing (isFood=false -> no love, not consumed)
        for (net.minecraft.world.entity.animal.Animal a : pairs) {
            feeder.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.APPLE));
            a.mobInteract(feeder, InteractionHand.MAIN_HAND);
            helper.assertFalse(a.isInLove(), "regular apple must not start love mode (ENT-K-050/ENT-S-031)");
            helper.assertValueEqual(feeder.getItemInHand(InteractionHand.MAIN_HAND).getCount(), 1,
                    "regular apple not consumed");
        }
        // positive: crystal apple -> love mode for all four
        for (net.minecraft.world.entity.animal.Animal a : pairs) {
            feeder.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ModItems.CRYSTAL_APPLE.get()));
            a.mobInteract(feeder, InteractionHand.MAIN_HAND);
            helper.assertTrue(a.isInLove(), "crystal apple starts love mode");
            helper.assertValueEqual(feeder.getItemInHand(InteractionHand.MAIN_HAND).getCount(), 0,
                    "crystal apple consumed");
        }

        final boolean[] done = {false};
        helper.onEachTick(() -> {
            if (done[0]) return;
            boolean babyPeacock = helper.getLevel().getEntitiesOfClass(Peacock.class, helper.getBounds())
                    .stream().anyMatch(Peacock::isBaby);
            boolean babyBug = helper.getLevel().getEntitiesOfClass(EntityStinkBug.class, helper.getBounds())
                    .stream().anyMatch(EntityStinkBug::isBaby);
            if (babyPeacock && babyBug) {
                helper.getLevel().getEntitiesOfClass(Peacock.class, helper.getBounds()).forEach(Entity::discard);
                helper.getLevel().getEntitiesOfClass(EntityStinkBug.class, helper.getBounds()).forEach(Entity::discard);
                done[0] = true;
            }
        });
        helper.succeedWhen(() -> helper.assertTrue(done[0],
                "both crystal-apple-fed pairs must produce babies"));
    }

    // =====================================================================
    // i044-ent-k-056 — PurplePower orb effects
    // =====================================================================

    /**
     * Checklist i044-ent-k-056 (ENT-K-056).
     * Documented values: phase_c_reports/C3_entities_K_R.md:24 — type 2 applies
     * POISON 50 ticks, type 3 applies WEAKNESS 50 ticks (audit had them swapped;
     * orig PurplePower.java:301-306). Port: entity/PurplePower.java:166-169.
     * The non-royal branch also does setHealth(h*15/16) then hurt(5): with a
     * 1000-HP zero-armor victim that is exactly 937.5 - 5 = 932.5.
     */
    @GameTest(template = "empty_large", timeoutTicks = 100)
    public void purple_power_orb_effects(GameTestHelper helper) {
        PurplePower orb2 = helper.spawn(ModEntities.PURPLE_POWER.get(), new BlockPos(24, 6, 24));
        orb2.setNoGravity(true);
        orb2.setPurpleType(2);
        Cow victim2 = helper.spawnWithNoFreeWill(EntityType.COW, new BlockPos(24, 6, 26));
        victim2.setNoGravity(true);
        zeroArmor(victim2);
        setMaxHealth(victim2, 1000.0);
        orb2.doHurtTarget(victim2);
        MobEffectInstance poison = victim2.getEffect(MobEffects.POISON);
        helper.assertTrue(poison != null, "type-2 orb applies Poison (orig PurplePower.java:302)");
        helper.assertValueEqual(poison.getDuration(), 50, "Poison lasts 50 ticks (2.5 s)");
        helper.assertValueEqual(poison.getAmplifier(), 0, "Poison amplifier 0");
        helper.assertFalse(victim2.hasEffect(MobEffects.WEAKNESS), "type 2 never applies Weakness (audit-corrected)");
        helper.assertValueEqual(victim2.getHealth(), 932.5f,
                "non-royal hit: setHealth(15/16) then hurt(5) => 932.5 (entity/PurplePower.java:160-165)");

        PurplePower orb3 = helper.spawn(ModEntities.PURPLE_POWER.get(), new BlockPos(24, 6, 22));
        orb3.setNoGravity(true);
        orb3.setPurpleType(3);
        Cow victim3 = helper.spawnWithNoFreeWill(EntityType.COW, new BlockPos(24, 6, 20));
        victim3.setNoGravity(true);
        zeroArmor(victim3);
        setMaxHealth(victim3, 1000.0);
        orb3.doHurtTarget(victim3);
        MobEffectInstance weakness = victim3.getEffect(MobEffects.WEAKNESS);
        helper.assertTrue(weakness != null, "type-3 orb applies Weakness (orig PurplePower.java:305)");
        helper.assertValueEqual(weakness.getDuration(), 50, "Weakness lasts 50 ticks (2.5 s)");
        helper.assertFalse(victim3.hasEffect(MobEffects.POISON), "type 3 never applies Poison");

        orb2.discard();
        orb3.discard();
        victim2.discard();
        victim3.discard();
        helper.succeed();
    }

    // =====================================================================
    // i045-ent-k-058 — Rat wild aggression / owner immunity
    // =====================================================================

    /**
     * Checklist i045-ent-k-058 (ENT-K-058).
     * Documented values: phase_c_reports/C3_entities_K_R.md:25 — RatPlayerFriendly/
     * RatPetFriendly (both default true) only gate rats WITH an owner; wild rats
     * attack players and pets regardless, and an owned rat never attacks its owner
     * (orig Rat.java:185-249, esp. :230-233). Port: entity/EntityRat.java:183-202,
     * OreSpawnConfig.java:266-267 (defaults stay true — no config change needed).
     */
    @GameTest(template = "empty_large", timeoutTicks = 2000)
    public void rat_wild_aggression_owner_immunity(GameTestHelper helper) {
        final Vec3 ratPos = helper.absoluteVec(new Vec3(24.0, 6.0, 24.0));
        final Vec3 nearPos = helper.absoluteVec(new Vec3(24.0, 6.0, 25.5));  // distSq < 4 bite gate
        final Vec3 farPos = helper.absoluteVec(new Vec3(24.0, 6.0, 40.0));   // outside the 9-block scan
        helper.assertTrue(OreSpawnConfig.RAT_PLAYER_FRIENDLY.get() && OreSpawnConfig.RAT_PET_FRIENDLY.get(),
                "documented defaults: both rat configs true (orig OreSpawnMain.java:1472-1473)");

        final Player player = addedMockPlayerAt(helper, nearPos);
        final EntityRat[] rat = {helper.spawn(ModEntities.ENTITY_RAT.get(), new BlockPos(24, 6, 24))};
        final Wolf[] pet = {null};
        final int[] phase = {0};     // 0 wild-vs-player, 1 wild-vs-pet, 2 owned-vs-owner, 3 done
        final int[] phaseTick = {0};

        helper.onEachTick(() -> {
            if (phase[0] >= 3) return;
            pin(rat[0], ratPos);
            phaseTick[0]++;
            switch (phase[0]) {
                case 0 -> { // wild rat attacks the player despite ratPlayerFriendly=true
                    pin(player, nearPos);
                    player.getFoodData().setFoodLevel(10);
                    if (player.getHealth() < 20.0f) {
                        player.setHealth(20.0f);
                        rat[0].discard();
                        rat[0] = helper.spawn(ModEntities.ENTITY_RAT.get(), new BlockPos(24, 6, 24));
                        pet[0] = helper.spawnWithNoFreeWill(EntityType.WOLF, new BlockPos(24, 6, 25));
                        pet[0].setTame(true, false);
                        pet[0].setOwnerUUID(UUID.randomUUID());
                        setMaxHealth(pet[0], 1000.0);
                        phase[0] = 1;
                        phaseTick[0] = 0;
                    } else if (phaseTick[0] > 700) {
                        // bite p/tick ~ (1/5)*0.25 = 5%; P(none in 700) = 0.95^700 ~ 2.6e-16
                        helper.fail("wild rat never attacked the player (ENT-K-058, orig Rat.java:185-249)");
                    }
                }
                case 1 -> { // wild rat attacks a tamed pet despite ratPetFriendly=true
                    pin(player, farPos); // keep the player outside the 9-block scan
                    pin(pet[0], nearPos);
                    if (pet[0].getHealth() < 1000.0f) {
                        pet[0].discard();
                        rat[0].discard();
                        rat[0] = helper.spawn(ModEntities.ENTITY_RAT.get(), new BlockPos(24, 6, 24));
                        rat[0].setOwner(player);
                        phase[0] = 2;
                        phaseTick[0] = 0;
                    } else if (phaseTick[0] > 700) {
                        helper.fail("wild rat never attacked the tamed pet (ENT-K-058)");
                    }
                }
                case 2 -> { // owned rat NEVER attacks its owner — deterministic filter (orig Rat.java:230-233)
                    pin(player, nearPos);
                    player.getFoodData().setFoodLevel(10);
                    helper.assertValueEqual(player.getHealth(), 20.0f,
                            "a rat with an owner must never attack that owner");
                    if (phaseTick[0] >= 400) {
                        rat[0].discard();
                        player.discard();
                        phase[0] = 3;
                    }
                }
                default -> { }
            }
        });
        helper.succeedWhen(() -> helper.assertTrue(phase[0] >= 3, "all rat phases finished"));
    }

    // =====================================================================
    // i046-ent-s-010 — SeaViper poison duration
    // =====================================================================

    /** Public-bridge subclass: lets the test fire the goal's protected poison hook directly. */
    private static final class BiteBridge extends SeaViperBiteGoal {
        BiteBridge(Mob viper) {
            super(viper, v -> { });
        }
        void fire(LivingEntity target) {
            this.onSuccessfulAttack(target);
        }
    }

    /**
     * Checklist i046-ent-s-010 (ENT-S-010).
     * Documented values: phase_c_reports/C4_entities_S_Z.md:24 — POISON (never
     * Hunger, audit-corrected) for 6 s (120 t) normally and 8 s (160 t) on EASY,
     * applied on a 1-in-2 roll after a successful bite (orig SeaViper.java:337,347-356;
     * the Normal/Hard branches are an unreachable decompile quirk). Port:
     * entity/ai/SeaViperBiteGoal.java:22-31. The EASY window is atomic inside this
     * single tick-0 body, so it cannot leak into other tests' entity ticks.
     */
    @GameTest(template = "empty_large", timeoutTicks = 100)
    public void sea_viper_poison_duration(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        MinecraftServer server = level.getServer();
        helper.assertValueEqual(level.getDifficulty(), Difficulty.NORMAL,
                "baseline gametest server difficulty (GameTestServer.java:85)");

        SeaViper viper = helper.spawn(ModEntities.SEA_VIPER.get(), new BlockPos(24, 6, 24));
        viper.setNoGravity(true);
        Cow victim = helper.spawnWithNoFreeWill(EntityType.COW, new BlockPos(24, 6, 26));
        victim.setNoGravity(true);
        setMaxHealth(victim, 1000.0);
        BiteBridge bite = new BiteBridge(viper);

        // NORMAL: n=150 bites at the documented p=1/2 (nextInt(2)==1), mean 75.
        // Band [30,120]: Hoeffding 2*exp(-2*150*0.09) = 2*exp(-27) ~ 3.7e-12 < 1e-9.
        int poisoned = 0;
        for (int i = 0; i < 150; i++) {
            victim.removeAllEffects();
            bite.fire(victim);
            helper.assertFalse(victim.hasEffect(MobEffects.HUNGER),
                    "Sea Viper must never apply Hunger (audit-corrected ENT-S-010)");
            MobEffectInstance poison = victim.getEffect(MobEffects.POISON);
            if (poison != null) {
                poisoned++;
                helper.assertValueEqual(poison.getDuration(), 120,
                        "poison 6 s (120 t) at non-Easy difficulty (orig SeaViper.java:347-356)");
            }
        }
        helper.assertTrue(poisoned >= 30 && poisoned <= 120,
                "poison applications " + poisoned + "/150 outside [30,120] for the documented 1-in-2 roll");

        // EASY window — atomic set/assert/restore.
        try {
            server.setDifficulty(Difficulty.EASY, true);
            boolean sawEasy = false;
            // P(no poison in 100 rolls) = 0.5^100 ~ 7.9e-31.
            for (int i = 0; i < 100 && !sawEasy; i++) {
                victim.removeAllEffects();
                bite.fire(victim);
                MobEffectInstance poison = victim.getEffect(MobEffects.POISON);
                if (poison != null) {
                    sawEasy = true;
                    helper.assertValueEqual(poison.getDuration(), 160,
                            "poison 8 s (160 t) on Easy (orig SeaViper.java:347-356)");
                }
                helper.assertFalse(victim.hasEffect(MobEffects.HUNGER), "never Hunger on Easy either");
            }
            helper.assertTrue(sawEasy, "poison must appear within 100 Easy-mode bites");
        } finally {
            server.setDifficulty(Difficulty.NORMAL, true);
        }

        viper.discard();
        victim.discard();
        helper.succeed();
    }
}
