package danger.orespawn.gametest;

import danger.orespawn.ModEntities;
import danger.orespawn.OreSpawnConfig;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.Girlfriend;
import danger.orespawn.entity.ai.DinosaurMeleeAttackGoal;
import danger.orespawn.entity.ai.PointysaurusStareGoal;
import danger.orespawn.util.SeasonalDates;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtTargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/**
 * Targeting ledger batch T9 (ENT-S-125): the port-only targeting additions, split by the owner's ruling
 * of 2026-09-04 ("documented reason → MOD record behind modern; undocumented → removed from classic")
 * into four {@code [modern]} records — MOD-032 {@code godzillaSparesBossPeers} (a filter, read live),
 * MOD-033 {@code petsDefendOwner}, MOD-034 {@code pointysaurusStareAggro} and MOD-035
 * {@code cryolophosaurusRevengeChase} (goal registrations, construction snapshots) — one removal from
 * both modes (the Mantis's two inert target goals) and one deliberate parity exception kept in both modes
 * (MOD-036, the Girlfriend's Valentine safety gates). MOD-033's extension of 2026-09-05 adds the four companions
 * outside the ledger (Hydrolisc, VelocityRaptor, Boyfriend, Girlfriend — the whole target selector described per
 * mode as {@code priority:Goal<targetType>}) and Leon's tame rule (a predicate pin per mode: the hunt goal's
 * {@code canUse()} under the IMobConventionTests forced-roll seam — a tamed Leon holding a target refuses the frozen
 * Zombie 8 blocks east in modern and takes her in classic, the slot emptied as the modern control).
 *
 * <p>Per record: one row with the master on and the key at its default (the modern feature present),
 * one with the key set false and one with the master {@code modern.enabled} set false (the 1.7.10
 * behaviour: the goal absent from the selector / the boss peer accepted). The construction-snapshot
 * records spawn the entity AFTER the flip (goals register in the Mob constructor, the BOSS-017 shape;
 * {@code helper.spawn} keeps the goals, {@code spawnWithNoFreeWill} would strip them); the Godzilla rows
 * use the IgnoreScreenParityTests row-11 rig ({@code empty_tall}, the private {@code isSuitableTarget}
 * by reflection, a frozen PitchBlack and a frozen TheKing, a pig control) and, being a live read, ask
 * the same Godzilla before and after the flip. The Girlfriend rows run under the {@code SeasonalDates}
 * Feb-14 clock seam (the DateGateTests idiom) and flip the difficulty inside the test (the
 * CephadromeGateTests idiom), asking the {@code ValentineTargetGoal<Player>}'s own
 * {@link TargetingConditions} the way the IgnoreScreenParityTests goal-predicate shape does.</p>
 *
 * <p>Synchronous; every global flag, the difficulty and the clock are restored in a {@code finally};
 * spawns discarded, mock players removed. Own batch {@code portOnlyTargeting} (TEST-003). Geometry:
 * {@code empty_large} (48x16x48) for everything but Godzilla (25 tall, {@code empty_tall} 48x34x48, the
 * King 22 wide at 18 blocks so the two boxes never overlap); the Leon rows use the IMobConventionTests spots (the
 * hunter at (20,1,24), the Zombie at (28,1,24), inside its 40-block follow range).</p>
 */
@GameTestHolder(OreSpawnMod.MOD_ID)
@PrefixGameTestTemplate(false)
public class PortOnlyTargetingTests {

    private static final String BATCH = "portOnlyTargeting";

    /** Godzilla (9.9 wide) on the empty_tall floor; box 11.05..20.95 on x. */
    private static final BlockPos GODZILLA_POS = new BlockPos(16, 1, 24);
    /** The candidate 18 blocks east: TheKing's 22-wide box spans 23.5..45.5, clear of Godzilla and inside the shell. */
    private static final BlockPos PEER_POS = new BlockPos(34, 1, 24);
    /** Every other mob under test, alone on the empty_large floor. */
    private static final BlockPos MOB_POS = new BlockPos(24, 1, 24);
    /** The Girlfriend on the empty_large floor and a player 8 blocks east (inside her 16-block Valentine radius). */
    private static final BlockPos GIRLFRIEND_POS = new BlockPos(20, 1, 24);
    private static final Vec3 PLAYER_POS = new Vec3(28.5, 1.0, 24.5);
    private static final LocalDate VALENTINES = LocalDate.of(2026, 2, 14);
    /** The Leon rows: the IMobConventionTests spots — the hunter on the empty_large floor, the Zombie 8 blocks east (inside its 40-block follow range). */
    private static final BlockPos LEON_POS = new BlockPos(20, 1, 24);
    private static final BlockPos LEON_PREY_POS = new BlockPos(28, 1, 24);
    /** The vanilla hunt's acquisition roll: {@code NearestAttackableTargetGoal} reduces its 10-tick interval to {@code nextInt(5) != 0 → skip} (IMobConventionTests.GOAL_ROLL_BOUND). */
    private static final int GOAL_ROLL_BOUND = 5;

    /** Every global flag this class flips, read once per test and restored in every {@code finally} (keys first, master last). */
    private record Flags(boolean master, boolean godzilla, boolean pets, boolean stare, boolean chase) {
        static Flags read() {
            return new Flags(OreSpawnConfig.MODERN_ENABLED.get(),
                    OreSpawnConfig.MODERN_GODZILLA_SPARES_BOSS_PEERS.get(),
                    OreSpawnConfig.MODERN_PETS_DEFEND_OWNER.get(),
                    OreSpawnConfig.MODERN_POINTYSAURUS_STARE_AGGRO.get(),
                    OreSpawnConfig.MODERN_CRYOLOPHOSAURUS_REVENGE_CHASE.get());
        }

        void restore() {
            OreSpawnConfig.MODERN_GODZILLA_SPARES_BOSS_PEERS.set(this.godzilla);
            OreSpawnConfig.MODERN_PETS_DEFEND_OWNER.set(this.pets);
            OreSpawnConfig.MODERN_POINTYSAURUS_STARE_AGGRO.set(this.stare);
            OreSpawnConfig.MODERN_CRYOLOPHOSAURUS_REVENGE_CHASE.set(this.chase);
            OreSpawnConfig.MODERN_ENABLED.set(this.master);
        }
    }

    /** The three states each record is pinned in. */
    private enum State {
        /** master on, the key at its code default (asserted true). */
        MODERN_ON,
        /** master on, the key set false. */
        KEY_OFF,
        /** master off, the key left at its default (the master override). */
        MASTER_OFF;

        boolean modern() {
            return this == MODERN_ON;
        }

        void apply(GameTestHelper helper, String keyName, ModConfigSpec.BooleanValue key) {
            helper.assertTrue(key.getDefault(), "precondition: [modern] " + keyName + " defaults to true in code"
                    + " (the MOD-029 / MOD-031 precedent; T9 ruling 2026-09-04)");
            switch (this) {
                case MODERN_ON -> {
                    OreSpawnConfig.MODERN_ENABLED.set(true);
                    key.set(key.getDefault());
                }
                case KEY_OFF -> {
                    OreSpawnConfig.MODERN_ENABLED.set(true);
                    key.set(false);
                }
                case MASTER_OFF -> {
                    OreSpawnConfig.MODERN_ENABLED.set(false);
                    key.set(key.getDefault());
                }
            }
        }

        String label(String keyName) {
            return switch (this) {
                case MODERN_ON -> "modern.enabled=true, " + keyName + "=true (its default)";
                case KEY_OFF -> "modern.enabled=true, " + keyName + "=false";
                case MASTER_OFF -> "modern.enabled=false, " + keyName + "=true (its default; the master override)";
            };
        }
    }

    // ------------------------------------------------------------------
    // MOD-032 — Godzilla spares its boss peers (a filter, read live)
    // ------------------------------------------------------------------

    /** Modern on: the filter refuses a PitchBlack and a TheKing (MyUtils.isBigBoss / isRoyalty) and takes the pig. */
    @GameTest(template = "empty_tall", batch = BATCH)
    public void mod032_godzilla_spares_boss_peers_modern_on(GameTestHelper helper) {
        godzillaRow(helper, State.MODERN_ON);
    }

    /** Key off: the same Godzilla refused the peers with the key on and takes them once it is off (orig Godzilla.java:448-471). */
    @GameTest(template = "empty_tall", batch = BATCH)
    public void mod032_godzilla_takes_boss_peers_key_off(GameTestHelper helper) {
        godzillaRow(helper, State.KEY_OFF);
    }

    /** Master off with the key still on: classic — the peers are prey. */
    @GameTest(template = "empty_tall", batch = BATCH)
    public void mod032_godzilla_takes_boss_peers_master_off(GameTestHelper helper) {
        godzillaRow(helper, State.MASTER_OFF);
    }

    private static void godzillaRow(GameTestHelper helper, State state) {
        final Flags prior = Flags.read();
        final String key = "godzillaSparesBossPeers";
        Mob godzilla = null;
        Mob peer = null;
        Mob control = null;
        try {
            // The feature first, on every row: the refusal is the precondition of the classic rows' flip.
            State.MODERN_ON.apply(helper, key, OreSpawnConfig.MODERN_GODZILLA_SPARES_BOSS_PEERS);
            helper.assertTrue(OreSpawnConfig.godzillaSparesBossPeers(), "precondition: the helper reads true with the"
                    + " master on and the key at its default (MOD-032)");
            godzilla = spawnFrozen(helper, ModEntities.GODZILLA.get(), GODZILLA_POS);
            peer = spawnFrozen(helper, ModEntities.PITCH_BLACK.get(), PEER_POS);
            assertSees(helper, godzilla, peer);
            helper.assertTrue(!isSuitableTarget(godzilla, peer), "Godzilla.isSuitableTarget with " + State.MODERN_ON.label(key)
                    + ": a PitchBlack (MyUtils.isBigBoss) must be refused — the MOD-032 peer rule (commit a87c0649)");
            peer.discard();
            peer = spawnFrozen(helper, ModEntities.THE_KING.get(), PEER_POS);
            assertSees(helper, godzilla, peer);
            helper.assertTrue(!isSuitableTarget(godzilla, peer), "Godzilla.isSuitableTarget with " + State.MODERN_ON.label(key)
                    + ": a TheKing (MyUtils.isRoyalty) must be refused — the MOD-032 peer rule");
            if (!state.modern()) {
                // The live read: the same Godzilla, the same King, only the config changed.
                state.apply(helper, key, OreSpawnConfig.MODERN_GODZILLA_SPARES_BOSS_PEERS);
                helper.assertTrue(!OreSpawnConfig.godzillaSparesBossPeers(), "precondition: the helper reads false with "
                        + state.label(key) + " (the master && key evaluation, MOD-032)");
                helper.assertTrue(isSuitableTarget(godzilla, peer), "Godzilla.isSuitableTarget with " + state.label(key)
                        + ": classic is orig Godzilla.java:448-471 — the eight refusals and nothing more, so a TheKing"
                        + " is prey as in 1.7.10 (MOD-032; read live, no respawn needed)");
                peer.discard();
                peer = spawnFrozen(helper, ModEntities.PITCH_BLACK.get(), PEER_POS);
                assertSees(helper, godzilla, peer);
                helper.assertTrue(isSuitableTarget(godzilla, peer), "Godzilla.isSuitableTarget with " + state.label(key)
                        + ": a PitchBlack is prey as in 1.7.10 (MOD-032)");
            }
            peer.discard();
            peer = null;
            control = spawnFrozen(helper, EntityType.PIG, PEER_POS);
            assertSees(helper, godzilla, control);
            helper.assertTrue(isSuitableTarget(godzilla, control), "control: Godzilla.isSuitableTarget with "
                    + state.label(key) + " must take a vanilla pig on the same spot, so the peers' answer came from the"
                    + " peer rule alone and not from geometry, sight or the eight-name chain (MOD-032)");
        } finally {
            discard(control, peer, godzilla);
            prior.restore();
        }
        helper.succeed();
    }

    // ------------------------------------------------------------------
    // MOD-033 — Companions defend their owner (construction snapshot, eight pets)
    // ------------------------------------------------------------------

    /**
     * One pet of the record: what 1.7.10 registered on its targetTasks (kept in both modes) versus what
     * Phase 4E added (modern only).
     */
    private record Pet(String name, Supplier<? extends EntityType<? extends Mob>> type, String orig,
                       boolean origHurtBy, boolean origHunt) {
    }

    private static final List<Pet> PETS = List.of(
            new Pet("EntityLeon", ModEntities.ENTITY_LEON, "orig Leon.java:92-95 (the IMob hunt when PlayNicely == 0, EntityAIHurtByTarget)", true, true),
            new Pet("EntityGammaMetroid", ModEntities.ENTITY_GAMMA_METROID, "orig GammaMetroid.java:67 (EntityAIHurtByTarget only)", true, false),
            new Pet("EntitySpyro", ModEntities.ENTITY_SPYRO, "orig Spyro.java:73-81 (no targetTasks)", false, false),
            new Pet("EntityStinky", ModEntities.ENTITY_STINKY, "orig Stinky.java:67-77 (no targetTasks)", false, false),
            new Pet("ThePrince", ModEntities.THE_PRINCE, "orig ThePrince.java:86-92 (no targetTasks)", false, false),
            new Pet("ThePrincess", ModEntities.THE_PRINCESS, "orig ThePrincess.java:86-92 (no targetTasks)", false, false),
            new Pet("ThePrinceTeen", ModEntities.THE_PRINCE_TEEN, "orig ThePrinceTeen.java:116-119 (the IMob hunt when PlayNicely == 0, EntityAIHurtByTarget)", true, true),
            new Pet("ThePrinceAdult", ModEntities.THE_PRINCE_ADULT, "orig ThePrinceAdult.java:112-115 (the IMob hunt when PlayNicely == 0, EntityAIHurtByTarget)", true, true));

    /** Modern on: every pet carries the owner pair, HurtByTargetGoal and a NearestAttackableTargetGoal. */
    @GameTest(template = "empty_large", batch = BATCH)
    public void mod033_pets_defend_owner_modern_on(GameTestHelper helper) {
        petsRow(helper, State.MODERN_ON);
    }

    /** Key off: no owner goal anywhere; only what 1.7.10 registered remains. */
    @GameTest(template = "empty_large", batch = BATCH)
    public void mod033_pets_defend_owner_key_off(GameTestHelper helper) {
        petsRow(helper, State.KEY_OFF);
    }

    /** Master off with the key still on: classic. */
    @GameTest(template = "empty_large", batch = BATCH)
    public void mod033_pets_defend_owner_master_off(GameTestHelper helper) {
        petsRow(helper, State.MASTER_OFF);
    }

    private static void petsRow(GameTestHelper helper, State state) {
        final Flags prior = Flags.read();
        final String key = "petsDefendOwner";
        try {
            state.apply(helper, key, OreSpawnConfig.MODERN_PETS_DEFEND_OWNER);
            helper.assertTrue(OreSpawnConfig.petsDefendOwner() == state.modern(), "precondition: the helper reads "
                    + state.modern() + " with " + state.label(key) + " (MOD-033)");
            for (Pet pet : PETS) {
                Mob mob = null;
                try {
                    mob = spawnWithGoals(helper, pet.type().get(), MOB_POS); // AFTER the flip: a construction snapshot
                    int ownerHurtBy = count(mob.targetSelector, OwnerHurtByTargetGoal.class);
                    int ownerHurt = count(mob.targetSelector, OwnerHurtTargetGoal.class);
                    int hurtBy = count(mob.targetSelector, HurtByTargetGoal.class);
                    int hunt = count(mob.targetSelector, NearestAttackableTargetGoal.class);
                    String where = pet.name() + " spawned with " + state.label(key);
                    if (state.modern()) {
                        helper.assertTrue(ownerHurtBy == 1 && ownerHurt == 1, where + ": the owner-defence pair"
                                + " (OwnerHurtByTargetGoal, OwnerHurtTargetGoal) must be on the target selector — got "
                                + ownerHurtBy + " / " + ownerHurt + " (MOD-033, commit 27b66a39)");
                        helper.assertTrue(hurtBy == 1, where + ": HurtByTargetGoal must be on the target selector — got "
                                + hurtBy + " (MOD-033)");
                        helper.assertTrue(hunt == 1, where + ": one NearestAttackableTargetGoal (the hunt) must be on the"
                                + " target selector — got " + hunt + " (MOD-033)");
                    } else {
                        helper.assertTrue(ownerHurtBy == 0 && ownerHurt == 0, where + ": classic registers no owner goal"
                                + " (" + pet.orig() + ") — got " + ownerHurtBy + " / " + ownerHurt + " (MOD-033)");
                        helper.assertTrue(hurtBy == (pet.origHurtBy() ? 1 : 0), where + ": HurtByTargetGoal "
                                + (pet.origHurtBy() ? "stays (orig registered EntityAIHurtByTarget)" : "is absent (orig registered none)")
                                + " — got " + hurtBy + " (" + pet.orig() + ") (MOD-033)");
                        helper.assertTrue(hunt == (pet.origHunt() ? 1 : 0), where + ": the NearestAttackableTargetGoal "
                                + (pet.origHunt() ? "stays (orig registered the IMob hunt)" : "is absent (the tame hunt was Phase 4E's)")
                                + " — got " + hunt + " (" + pet.orig() + ") (MOD-033)");
                    }
                } finally {
                    if (mob != null) mob.discard();
                }
            }
        } finally {
            prior.restore();
        }
        helper.succeed();
    }

    // ------------------------------------------------------------------
    // MOD-033 — the four companions outside the ledger (construction snapshot; the extension of 2026-09-05)
    // ------------------------------------------------------------------

    /**
     * One companion of the extension: its whole target selector, described as {@code priority:Goal<targetType>}
     * (the anonymous IMob hunts report their vanilla superclass) and sorted, in each mode — the classic selector is
     * exactly the port's counterparts of what 1.7.10 registered on its targetTasks, at their port priorities; the
     * modern selector is that plus the port-only owner goals (Hydrolisc / VelocityRaptor: Phase 4E, commit 27b66a39;
     * Boyfriend / Girlfriend: commit 2b0c2cd, no stated intent), gated on the 2026-09-04 ruling.
     */
    private record Companion(String name, Supplier<? extends EntityType<? extends Mob>> type, String orig,
                             List<String> classic, List<String> modernOnly) {
        List<String> expected(boolean modern) {
            List<String> all = new ArrayList<>(this.classic);
            if (modern) all.addAll(this.modernOnly);
            all.sort(null);
            return all;
        }
    }

    private static final List<Companion> COMPANIONS = List.of(
            new Companion("EntityHydrolisc", ModEntities.ENTITY_HYDROLISC, "orig Hydrolisc.java:51-60 (tasks only, no targetTasks)",
                    List.of(),
                    List.of("1:OwnerHurtByTargetGoal", "2:OwnerHurtTargetGoal", "3:HurtByTargetGoal")),
            new Companion("VelocityRaptor", ModEntities.VELOCITY_RAPTOR, "orig VelocityRaptor.java:53-62 (tasks only, no targetTasks)",
                    List.of(),
                    List.of("1:OwnerHurtByTargetGoal", "2:OwnerHurtTargetGoal", "3:HurtByTargetGoal")),
            new Companion("Boyfriend", ModEntities.BOYFRIEND, "orig Boyfriend.java:138-147 (the Creeper and IMob hunts when PlayNicely == 0"
                    + " — MyEntityAINearestAttackableTargetGoal since ENT-S-135 — and the two Jealousy tasks; no owner task, no EntityAIHurtByTarget)",
                    List.of("2:MyEntityAINearestAttackableTargetGoal<Creeper>", "3:MyEntityAINearestAttackableTargetGoal<Mob>",
                            "4:JealousyTargetGoal<Boyfriend>", "5:JealousyTargetGoal<Boyfriend>"),
                    List.of("1:OwnerHurtByTargetGoal", "2:OwnerHurtTargetGoal")),
            new Companion("Girlfriend", ModEntities.GIRLFRIEND, "orig Girlfriend.java:161-174 (the two MyValentineTarget tasks, the Creeper"
                    + " and IMob hunts when PlayNicely == 0 — MyEntityAINearestAttackableTargetGoal since ENT-S-135 — and the two Jealousy"
                    + " tasks; no owner task, no EntityAIHurtByTarget)",
                    List.of("1:ValentineTargetGoal<Player>", "2:ValentineTargetGoal<Boyfriend>", "2:MyEntityAINearestAttackableTargetGoal<Creeper>",
                            "5:MyEntityAINearestAttackableTargetGoal<Mob>", "4:JealousyTargetGoal<Girlfriend>", "5:JealousyTargetGoal<Girlfriend>"),
                    List.of("3:OwnerHurtByTargetGoal", "4:OwnerHurtTargetGoal")));

    /** Modern on: each companion's selector is its 1.7.10 goals plus the owner pair (plus HurtByTargetGoal on the Hydrolisc and the Raptor). */
    @GameTest(template = "empty_large", batch = BATCH)
    public void mod033_companions_defend_owner_modern_on(GameTestHelper helper) {
        companionsRow(helper, State.MODERN_ON);
    }

    /** Key off: each companion's selector is exactly its 1.7.10 goals — the Hydrolisc's and the Raptor's empty. */
    @GameTest(template = "empty_large", batch = BATCH)
    public void mod033_companions_defend_owner_key_off(GameTestHelper helper) {
        companionsRow(helper, State.KEY_OFF);
    }

    /** Master off with the key still on: classic. */
    @GameTest(template = "empty_large", batch = BATCH)
    public void mod033_companions_defend_owner_master_off(GameTestHelper helper) {
        companionsRow(helper, State.MASTER_OFF);
    }

    private static void companionsRow(GameTestHelper helper, State state) {
        final Flags prior = Flags.read();
        final String key = "petsDefendOwner";
        try {
            state.apply(helper, key, OreSpawnConfig.MODERN_PETS_DEFEND_OWNER);
            helper.assertTrue(OreSpawnConfig.petsDefendOwner() == state.modern(), "precondition: the helper reads "
                    + state.modern() + " with " + state.label(key) + " (MOD-033)");
            for (Companion companion : COMPANIONS) {
                Mob mob = null;
                try {
                    mob = spawnWithGoals(helper, companion.type().get(), MOB_POS); // AFTER the flip: a construction snapshot
                    List<String> actual = describeTargetSelector(mob);
                    List<String> expected = companion.expected(state.modern());
                    helper.assertTrue(actual.equals(expected), companion.name() + " spawned with " + state.label(key)
                            + ": the target selector must be exactly " + expected + " — "
                            + (state.modern() ? "the 1.7.10 goals plus the port-only owner goals (MOD-033; Phase 4E 27b66a39 or 2b0c2cd)"
                                    : "the 1.7.10 goals alone, " + companion.orig() + " (MOD-033)")
                            + "; got " + actual);
                } finally {
                    if (mob != null) mob.discard();
                }
            }
        } finally {
            prior.restore();
        }
        helper.succeed();
    }

    // ------------------------------------------------------------------
    // MOD-033 — Leon's tame rule on its hunt (construction snapshot; a predicate pin per mode, the extension of 2026-09-05)
    // ------------------------------------------------------------------

    /** Modern on: a tamed Leon holding a target refuses the Zombie 8 blocks off; with the slot emptied it takes her (the control). */
    @GameTest(template = "empty_large", batch = BATCH)
    public void mod033_leon_tame_hunt_rule_modern_on(GameTestHelper helper) {
        leonTameRuleRow(helper, State.MODERN_ON);
    }

    /** Key off: the same tamed Leon holding a target takes the Zombie — orig Leon.java:93's bare IMob test. */
    @GameTest(template = "empty_large", batch = BATCH)
    public void mod033_leon_tame_hunt_rule_key_off(GameTestHelper helper) {
        leonTameRuleRow(helper, State.KEY_OFF);
    }

    /** Master off with the key still on: classic. */
    @GameTest(template = "empty_large", batch = BATCH)
    public void mod033_leon_tame_hunt_rule_master_off(GameTestHelper helper) {
        leonTameRuleRow(helper, State.MASTER_OFF);
    }

    private static void leonTameRuleRow(GameTestHelper helper, State state) {
        helper.assertTrue(helper.getLevel().getDifficulty() != Difficulty.PEACEFUL,
                "precondition: the game-test level runs at NORMAL, not Peaceful — the vanilla conditions refuse everything on"
                        + " Peaceful (MOD-033 test setup)");
        final Flags prior = Flags.read();
        final boolean priorPlayNicely = OreSpawnConfig.PLAY_NICELY.get();
        final String key = "petsDefendOwner";
        Mob leon = null;
        Mob prey = null;
        try {
            OreSpawnConfig.PLAY_NICELY.set(false); // the hunt's ENT-S-115 live canUse gate, out of the way
            helper.assertTrue(!OreSpawnConfig.PLAY_NICELY.get(), "precondition: PLAY_NICELY.set(false) must read back false (MOD-033 test setup)");
            state.apply(helper, key, OreSpawnConfig.MODERN_PETS_DEFEND_OWNER);
            helper.assertTrue(OreSpawnConfig.petsDefendOwner() == state.modern(), "precondition: the helper reads "
                    + state.modern() + " with " + state.label(key) + " (MOD-033)");
            leon = spawnWithGoals(helper, ModEntities.ENTITY_LEON.get(), LEON_POS); // AFTER the flip: a construction snapshot
            replaceRandom(leon, rolls(GOAL_ROLL_BOUND, 0)); // the goal's 1-in-5 acquisition roll pinned to fire
            NearestAttackableTargetGoal<?> hunt = huntGoal(helper, leon, "EntityLeon", state.label(key));
            prey = spawnPrey(helper, EntityType.ZOMBIE, LEON_PREY_POS);
            helper.assertTrue(prey instanceof Enemy, "precondition: a vanilla Zombie is an Enemy — an EntityMob, IMob in 1.7.10 (MOD-033 test setup)");
            assertSees(helper, leon, prey);
            TamableAnimal tamed = (TamableAnimal) leon;
            tamed.setTame(true, false);
            leon.setTarget(prey);
            helper.assertTrue(tamed.isTame() && leon.getTarget() == prey,
                    "precondition: the Leon reads tamed and holds a target (MOD-033 test setup)");
            String where = "EntityLeon's hunt (the NearestAttackableTargetGoal<Mob> @4) spawned with " + state.label(key);
            boolean can = hunt.canUse();
            Object pick = readField(hunt, NearestAttackableTargetGoal.class, "target");
            if (state.modern()) {
                helper.assertTrue(!can, where + ": tamed and holding a target it must refuse the Zombie 8 blocks off — the"
                        + " tame rule (!isTame() || getTarget() == null) is MOD-033's modern branch; canUse=" + can
                        + ", pick " + describe(pick) + " (MOD-033)");
                leon.setTarget(null);
                can = hunt.canUse();
                pick = readField(hunt, NearestAttackableTargetGoal.class, "target");
                helper.assertTrue(can && pick == prey, "control: " + where + " with the slot emptied must take the same"
                        + " Zombie — so the refusal came from the tame rule and not from geometry, sight or the Enemy"
                        + " selector; canUse=" + can + ", pick " + describe(pick) + " (MOD-033)");
            } else {
                helper.assertTrue(can && pick == prey, where + ": tamed and holding a target it must still take the Zombie"
                        + " 8 blocks off — classic is orig Leon.java:93's bare IMob test (an EntityLiving.class list through"
                        + " IMob.mobSelector, no tame term), the ENT-S-124 form; canUse=" + can + ", pick " + describe(pick)
                        + " (MOD-033)");
            }
        } finally {
            discard(prey, leon);
            OreSpawnConfig.PLAY_NICELY.set(priorPlayNicely);
            prior.restore();
        }
        helper.succeed();
    }

    // ------------------------------------------------------------------
    // MOD-034 — Pointysaurus stare aggression (construction snapshot)
    // ------------------------------------------------------------------

    @GameTest(template = "empty_large", batch = BATCH)
    public void mod034_pointysaurus_stare_goal_modern_on(GameTestHelper helper) {
        pointysaurusRow(helper, State.MODERN_ON);
    }

    @GameTest(template = "empty_large", batch = BATCH)
    public void mod034_pointysaurus_stare_goal_key_off(GameTestHelper helper) {
        pointysaurusRow(helper, State.KEY_OFF);
    }

    @GameTest(template = "empty_large", batch = BATCH)
    public void mod034_pointysaurus_stare_goal_master_off(GameTestHelper helper) {
        pointysaurusRow(helper, State.MASTER_OFF);
    }

    private static void pointysaurusRow(GameTestHelper helper, State state) {
        final Flags prior = Flags.read();
        final String key = "pointysaurusStareAggro";
        Mob mob = null;
        try {
            state.apply(helper, key, OreSpawnConfig.MODERN_POINTYSAURUS_STARE_AGGRO);
            helper.assertTrue(OreSpawnConfig.pointysaurusStareAggro() == state.modern(), "precondition: the helper reads "
                    + state.modern() + " with " + state.label(key) + " (MOD-034)");
            mob = spawnWithGoals(helper, ModEntities.POINTYSAURUS.get(), MOB_POS);
            int stare = count(mob.targetSelector, PointysaurusStareGoal.class);
            String where = "Pointysaurus spawned with " + state.label(key);
            helper.assertTrue(stare == (state.modern() ? 1 : 0), where + ": the PointysaurusStareGoal must be "
                    + (state.modern() ? "on the target selector (the MOD-034 eye-contact rule, commit 21b8d0e8)"
                            : "absent (orig Pointysaurus.java:50-55 has no such task)") + " — got " + stare + " (MOD-034)");
            helper.assertTrue(count(mob.targetSelector, HurtByTargetGoal.class) == 1
                            && count(mob.targetSelector, NearestAttackableTargetGoal.class) == 1,
                    where + ": HurtByTargetGoal and the players-only NearestAttackableTargetGoal stay in both modes"
                            + " (orig :55 revenge, the :183/:253 proximity scan) (MOD-034)");
        } finally {
            discard(mob);
            prior.restore();
        }
        helper.succeed();
    }

    // ------------------------------------------------------------------
    // MOD-035 — Cryolophosaurus revenge chase (construction snapshot)
    // ------------------------------------------------------------------

    @GameTest(template = "empty_large", batch = BATCH)
    public void mod035_cryolophosaurus_revenge_chase_modern_on(GameTestHelper helper) {
        cryolophosaurusRow(helper, State.MODERN_ON);
    }

    @GameTest(template = "empty_large", batch = BATCH)
    public void mod035_cryolophosaurus_revenge_chase_key_off(GameTestHelper helper) {
        cryolophosaurusRow(helper, State.KEY_OFF);
    }

    @GameTest(template = "empty_large", batch = BATCH)
    public void mod035_cryolophosaurus_revenge_chase_master_off(GameTestHelper helper) {
        cryolophosaurusRow(helper, State.MASTER_OFF);
    }

    private static void cryolophosaurusRow(GameTestHelper helper, State state) {
        final Flags prior = Flags.read();
        final String key = "cryolophosaurusRevengeChase";
        Mob mob = null;
        try {
            state.apply(helper, key, OreSpawnConfig.MODERN_CRYOLOPHOSAURUS_REVENGE_CHASE);
            helper.assertTrue(OreSpawnConfig.cryolophosaurusRevengeChase() == state.modern(), "precondition: the helper"
                    + " reads " + state.modern() + " with " + state.label(key) + " (MOD-035)");
            mob = spawnWithGoals(helper, ModEntities.CRYOLOPHOSAURUS.get(), MOB_POS);
            int chase = count(mob.goalSelector, DinosaurMeleeAttackGoal.class);
            String where = "Cryolophosaurus spawned with " + state.label(key);
            helper.assertTrue(chase == (state.modern() ? 1 : 0), where + ": the DinosaurMeleeAttackGoal must be "
                    + (state.modern() ? "on the goal selector (the MOD-035 revenge chase, commit f5cb0ba5)"
                            : "absent (orig Cryolophosaurus.java:51-57 has no attack task)") + " — got " + chase + " (MOD-035)");
            helper.assertTrue(count(mob.targetSelector, HurtByTargetGoal.class) == 1, where + ": HurtByTargetGoal stays in"
                    + " both modes (orig :57 EntityAIHurtByTarget — the revenge target is stored either way) (MOD-035)");
        } finally {
            discard(mob);
            prior.restore();
        }
        helper.succeed();
    }

    // ------------------------------------------------------------------
    // ENT-S-125 B2 — the Mantis's inert target goals are gone from both modes
    // ------------------------------------------------------------------

    /** orig Mantis.java registers no tasks and no targetTasks; the port's two inert goals were removed unconditionally. */
    @GameTest(template = "empty_large", batch = BATCH)
    public void s125_mantis_registers_no_goals_in_either_mode(GameTestHelper helper) {
        final Flags prior = Flags.read();
        Mob modern = null;
        Mob classic = null;
        try {
            OreSpawnConfig.MODERN_ENABLED.set(true);
            modern = spawnWithGoals(helper, ModEntities.ENTITY_MANTIS.get(), MOB_POS);
            assertNoGoals(helper, modern, "modern.enabled=true");
            modern.discard();
            modern = null;
            OreSpawnConfig.MODERN_ENABLED.set(false);
            classic = spawnWithGoals(helper, ModEntities.ENTITY_MANTIS.get(), MOB_POS);
            assertNoGoals(helper, classic, "modern.enabled=false");
        } finally {
            discard(classic, modern);
            prior.restore();
        }
        helper.succeed();
    }

    private static void assertNoGoals(GameTestHelper helper, Mob mantis, String mode) {
        int targets = mantis.targetSelector.getAvailableGoals().size();
        int goals = mantis.goalSelector.getAvailableGoals().size();
        helper.assertTrue(targets == 0 && goals == 0, "EntityMantis spawned with " + mode + ": both selectors must be"
                + " empty as orig Mantis.java's (no tasks, no targetTasks) — the port's HurtByTargetGoal and"
                + " NearestAttackableTargetGoal<Player> were inert (nothing read the slot, no HUD exists) and are removed"
                + " from both modes; got " + targets + " target goal(s), " + goals + " goal(s) (ENT-S-125, T9 B2)");
    }

    // ------------------------------------------------------------------
    // MOD-036 — the Girlfriend's Valentine safety gates hold in BOTH modes (no key)
    // ------------------------------------------------------------------

    /** Classic (master off): a creative player and a Peaceful player are still refused on Feb 14 — the parity exception. */
    @GameTest(template = "empty_large", batch = BATCH)
    public void mod036_girlfriend_valentine_gates_hold_in_classic(GameTestHelper helper) {
        girlfriendRow(helper, false);
    }

    /** Modern (master on): the same gates. */
    @GameTest(template = "empty_large", batch = BATCH)
    public void mod036_girlfriend_valentine_gates_hold_in_modern(GameTestHelper helper) {
        girlfriendRow(helper, true);
    }

    private static void girlfriendRow(GameTestHelper helper, boolean master) {
        final Flags prior = Flags.read();
        MinecraftServer server = helper.getLevel().getServer();
        Difficulty before = helper.getLevel().getDifficulty();
        helper.assertTrue(before != Difficulty.PEACEFUL,
                "precondition: the game-test level runs at NORMAL, not Peaceful (MOD-036 test setup)");
        String mode = "modern.enabled=" + master;
        Mob girlfriend = null;
        ServerPlayer creative = null;
        ServerPlayer survival = null;
        SeasonalDates.setClockForTesting(() -> VALENTINES);
        try {
            helper.assertTrue(SeasonalDates.isValentines(), "precondition: the Feb-14 clock seam is in place (MOD-036 test setup)");
            OreSpawnConfig.MODERN_ENABLED.set(master);
            girlfriend = spawnWithGoals(helper, ModEntities.GIRLFRIEND.get(), GIRLFRIEND_POS);
            helper.assertTrue(girlfriend instanceof Girlfriend gf && gf.isValentineAngry(),
                    "precondition: a Girlfriend spawned on Feb 14 is valentine-angry (orig Girlfriend.java:569-574) (MOD-036 test setup)");
            NearestAttackableTargetGoal<?> goal = valentinePlayerGoal(helper, girlfriend, mode);
            TargetingConditions conditions = targetConditionsOf(goal);
            creative = playerAt(helper, GameType.CREATIVE, helper.absoluteVec(PLAYER_POS));
            helper.assertTrue(creative.getAbilities().invulnerable, "precondition: a creative player is invulnerable (MOD-036 test setup)");
            assertSees(helper, girlfriend, creative);
            helper.assertTrue(!conditions.test(girlfriend, creative), "ValentineTargetGoal<Player> with " + mode
                    + ": a creative player must be refused on Feb 14 (Player.canBeSeenAsEnemy — kept in both modes on the"
                    + " owner's safety ruling; 1.7.10's MyEntityAITarget.java:96-98 hunted creative players) (MOD-036)");
            removePlayer(helper, creative);
            creative = null;
            survival = playerAt(helper, GameType.SURVIVAL, helper.absoluteVec(PLAYER_POS));
            assertSees(helper, girlfriend, survival);
            helper.assertTrue(conditions.test(girlfriend, survival), "control: ValentineTargetGoal<Player> with " + mode
                    + " must take a survival player 8 blocks off on " + before + " (in range, in sight, attackable), so the"
                    + " creative and Peaceful answers come from the gates alone (MOD-036)");
            server.setDifficulty(Difficulty.PEACEFUL, true);
            helper.assertTrue(helper.getLevel().getDifficulty() == Difficulty.PEACEFUL,
                    "precondition: MinecraftServer.setDifficulty(PEACEFUL, true) must show through level.getDifficulty() (MOD-036 test setup)");
            helper.assertTrue(!conditions.test(girlfriend, survival), "ValentineTargetGoal<Player> with " + mode
                    + ": the same survival player must be refused on PEACEFUL (LivingEntity.canAttack — kept in both modes"
                    + " on the owner's safety ruling; 1.7.10 hunted players in Peaceful on Feb 14) (MOD-036)");
        } finally {
            server.setDifficulty(before, true);
            removePlayer(helper, survival);
            removePlayer(helper, creative);
            discard(girlfriend);
            prior.restore();
            SeasonalDates.resetClock();
        }
        helper.succeed();
    }

    /** The one NearestAttackableTargetGoal on her target selector that scans Player.class: the ValentineTargetGoal<Player> @1. */
    private static NearestAttackableTargetGoal<?> valentinePlayerGoal(GameTestHelper helper, Mob girlfriend, String mode) {
        NearestAttackableTargetGoal<?> found = null;
        int matches = 0;
        for (WrappedGoal wrapped : girlfriend.targetSelector.getAvailableGoals()) {
            if (wrapped.getGoal() instanceof NearestAttackableTargetGoal<?> goal
                    && readField(goal, NearestAttackableTargetGoal.class, "targetType") == Player.class) {
                found = goal;
                matches++;
            }
        }
        helper.assertTrue(matches == 1 && found != null && found.getClass().getSimpleName().equals("ValentineTargetGoal"),
                "precondition: exactly one Player-scanning NearestAttackableTargetGoal — the ValentineTargetGoal<Player>"
                        + " (orig Girlfriend.java:161) — must be on her target selector with " + mode + "; found " + matches
                        + (found == null ? "" : " (" + found.getClass().getSimpleName() + ")") + " (MOD-036 test setup)");
        return found;
    }

    // ------------------------------------------------------------------
    // Helpers (the IgnoreScreenParityTests / CephadromeGateTests idioms)
    // ------------------------------------------------------------------

    /** Frozen: goals stripped, noAi, persistence set — for the live-read rows, where the selector is not under test. */
    private static <E extends Mob> E spawnFrozen(GameTestHelper helper, EntityType<E> type, BlockPos pos) {
        E mob = helper.spawnWithNoFreeWill(type, pos);
        mob.setNoAi(true);
        mob.setPersistenceRequired();
        return mob;
    }

    /** With its registered goals (the selectors are the site under test) but no AI, so nothing runs. */
    private static <E extends Mob> E spawnWithGoals(GameTestHelper helper, EntityType<E> type, BlockPos pos) {
        E mob = helper.spawn(type, pos);
        mob.setNoAi(true);
        mob.setPersistenceRequired();
        return mob;
    }

    private static int count(GoalSelector selector, Class<?> goalClass) {
        int n = 0;
        for (WrappedGoal wrapped : selector.getAvailableGoals()) {
            if (goalClass.isInstance(wrapped.getGoal())) n++;
        }
        return n;
    }

    private static void discard(Entity... entities) {
        for (Entity entity : entities) {
            if (entity != null) entity.discard();
        }
    }

    private static void assertSees(GameTestHelper helper, Mob hunter, LivingEntity target) {
        helper.assertTrue(hunter.hasLineOfSight(target), "precondition: " + hunter.getClass().getSimpleName()
                + " (eye " + String.format("%.2f", hunter.getEyeHeight()) + " above its feet) must see the "
                + target.getClass().getSimpleName() + " inside the barrier shell (T9 test geometry)");
    }

    /**
     * A mock ServerPlayer in the level with an explicit game mode (the game-test server defaults to
     * CREATIVE). Health is raised so nothing incidental can kill it. Deprecated mock-player factory
     * tolerated the way CephadromeGateTests and CreativeMappingParityTests do.
     */
    @SuppressWarnings({"removal", "deprecation"})
    private static ServerPlayer playerAt(GameTestHelper helper, GameType mode, Vec3 absolutePos) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        player.setGameMode(mode);
        player.getAttribute(Attributes.MAX_HEALTH).setBaseValue(1000.0);
        player.setHealth(1000.0f);
        player.teleportTo(helper.getLevel(), absolutePos.x, absolutePos.y, absolutePos.z, 0.0f, 0.0f);
        return player;
    }

    private static void removePlayer(GameTestHelper helper, ServerPlayer player) {
        if (player != null) {
            helper.getLevel().getServer().getPlayerList().remove(player);
        }
    }

    /** Godzilla's private {@code isSuitableTarget(LivingEntity)} — the port's one-arg shape of orig Godzilla.java:436. */
    private static boolean isSuitableTarget(Mob hunter, LivingEntity candidate) {
        String name = hunter.getClass().getSimpleName();
        try {
            Method method = hunter.getClass().getDeclaredMethod("isSuitableTarget", LivingEntity.class);
            method.setAccessible(true);
            return (Boolean) method.invoke(hunter, candidate);
        } catch (InvocationTargetException exception) {
            throw new IllegalStateException(name + ".isSuitableTarget threw", exception.getCause());
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("cannot invoke " + name + ".isSuitableTarget", exception);
        }
    }

    /** The goal's {@code protected TargetingConditions targetConditions}, which carries the combat gates and the predicate. */
    private static TargetingConditions targetConditionsOf(NearestAttackableTargetGoal<?> goal) {
        return (TargetingConditions) readField(goal, NearestAttackableTargetGoal.class, "targetConditions");
    }

    private static Object readField(Object owner, Class<?> declaring, String name) {
        try {
            Field field = declaring.getDeclaredField(name);
            field.setAccessible(true);
            return field.get(owner);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("cannot read " + declaring.getSimpleName() + "." + name, exception);
        }
    }

    // ------------------------------------------------------------------
    // Helpers for the MOD-033 extension rows (the IMobConventionTests idioms)
    // ------------------------------------------------------------------

    /**
     * Every goal on a target selector as {@code priority:Goal} — {@code priority:Goal<targetType>} for a
     * NearestAttackableTargetGoal, an anonymous subclass reporting its nearest named superclass — sorted, so the
     * whole selector is compared at once (positions and arguments, the S4 transcription rule).
     */
    private static List<String> describeTargetSelector(Mob mob) {
        List<String> out = new ArrayList<>();
        for (WrappedGoal wrapped : mob.targetSelector.getAvailableGoals()) {
            Goal goal = wrapped.getGoal();
            Class<?> type = goal.getClass();
            while (type.isAnonymousClass()) type = type.getSuperclass();
            String entry = wrapped.getPriority() + ":" + type.getSimpleName();
            if (goal instanceof NearestAttackableTargetGoal<?>) {
                entry += "<" + ((Class<?>) readField(goal, NearestAttackableTargetGoal.class, "targetType")).getSimpleName() + ">";
            }
            out.add(entry);
        }
        out.sort(null);
        return out;
    }

    /** The one {@code NearestAttackableTargetGoal} typed {@code Mob} on a hunter's target selector — the ENT-S-124 IMob hunt. */
    private static NearestAttackableTargetGoal<?> huntGoal(GameTestHelper helper, Mob hunter, String name, String mode) {
        NearestAttackableTargetGoal<?> found = null;
        int matches = 0;
        for (WrappedGoal wrapped : hunter.targetSelector.getAvailableGoals()) {
            if (wrapped.getGoal() instanceof NearestAttackableTargetGoal<?> goal
                    && readField(goal, NearestAttackableTargetGoal.class, "targetType") == Mob.class) {
                found = goal;
                matches++;
            }
        }
        helper.assertTrue(matches == 1 && found != null, "precondition: exactly one NearestAttackableTargetGoal<Mob> — the IMob"
                + " hunt of orig Leon.java:93 (ENT-S-124), registered in both modes — must be on " + name + "'s target selector"
                + " with " + mode + "; found " + matches + " (MOD-033 test setup)");
        return found;
    }

    /** Frozen prey with 1000 HP, so nothing incidental kills it (the IMobConventionTests idiom). */
    private static Mob spawnPrey(GameTestHelper helper, EntityType<? extends Mob> type, BlockPos pos) {
        Mob prey = spawnFrozen(helper, type, pos);
        prey.getAttribute(Attributes.MAX_HEALTH).setBaseValue(1000.0);
        prey.setHealth(1000.0f);
        return prey;
    }

    /** A seeded random with each (bound, answer) pair pinned — the VortexParityTests.ForcedRoll seam, chained as IMobConventionTests.rolls. */
    private static RandomSource rolls(int... boundAnswerPairs) {
        RandomSource source = RandomSource.create(1234L);
        for (int i = 0; i < boundAnswerPairs.length; i += 2) {
            source = new VortexParityTests.ForcedRoll(source, boundAnswerPairs[i], boundAnswerPairs[i + 1]);
        }
        return source;
    }

    /** Same seam as VortexParityTests.forceDiscardRoll: swap {@code Entity.random} for a forced source. */
    private static void replaceRandom(Entity entity, RandomSource forced) {
        try {
            Field field = Entity.class.getDeclaredField("random");
            field.setAccessible(true);
            field.set(entity, forced);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("cannot replace Entity.random", exception);
        }
    }

    private static String describe(Object pick) {
        return pick instanceof Entity entity ? entity.getClass().getSimpleName() + "#" + entity.getId() : String.valueOf(pick);
    }
}
