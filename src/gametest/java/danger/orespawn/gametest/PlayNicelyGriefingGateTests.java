package danger.orespawn.gametest;

import danger.orespawn.ModEntities;
import danger.orespawn.OreSpawnConfig;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.EntityGammaMetroid;
import danger.orespawn.entity.EntityStinky;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/**
 * ENT-S-116: the two 1.7.10 PlayNicely griefing gates outside target selection, restored at the orig positions
 * and read live as {@code OreSpawnConfig.PLAY_NICELY} (orig read the static {@code OreSpawnMain.PlayNicely == 0};
 * the port's convention since ENT-S-110 / BOSS-017 / ENT-S-115):
 * <ul>
 *   <li>orig Stinky.java:583 — {@code if (nextInt(50) == 0 && PlayNicely == 0)} heads the flying Stinky's
 *       idle block-eat inside {@code activity == 1} (:582) — the gate only: the port's {@code eatFlowers} eats
 *       flowers where orig's {@code scan_it} hunted coal ore (a pre-existing divergence, ENT-S-119), so this
 *       pin follows the port's routine and fails loudly when that routine changes; port
 *       {@code EntityStinky.customServerAiStep}, the roll
 *       spent ahead of the flag as in orig;</li>
 *   <li>orig GammaMetroid.java:435 — {@code (nextInt(20) == 0 && health < max || nextInt(100) == 0) &&
 *       PlayNicely == 0 && !isSitting()} heads the Gamma Metroid's stone-eat; port
 *       {@code EntityGammaMetroid.customServerAiStep}, the flag between the roll pair and the sitting test as in
 *       orig (the port rolls the entity random where orig rolled the world's — the ENT-S-093 convention, same
 *       bounds).</li>
 * </ul>
 *
 * <p>Two plain tests, the PlayNicelyGateParityTests shape: {@code PLAY_NICELY} set false, the port method that
 * carries the site ({@code customServerAiStep}, protected, invoked by reflection) driven once under pinned rolls
 * and required to show the griefing effect itself — the control: the block one step off is gone (air) and the
 * hunter, spawned {@link #HEALTH_DEFICIT} below max, is healed by exactly 1 — the block put back and the health
 * reset, the flag set true, the same hunter driven once more under the same pins and required to show nothing of
 * it (the block still standing, the health unmoved), the flag restored in a finally on every path. Synchronous —
 * nothing ticks between the flip and the restore; the flag is global, so the batch is this class alone
 * (TEST-003). With either port line reverted the flag-on drive eats the block and heals, and the test fails.</p>
 *
 * <p>What one call does, read from the port: {@code EntityStinky.eatFlowers} scans east of the Stinky
 * ({@code x + i}, i = 1..8, a slab of {@code y + 1 ± j}, {@code z ± j}, j = min(i, 2)) for a dandelion or poppy,
 * points the navigation at the first hit and, when that hit is within distSq 12 of the Stinky's block, sets it to
 * air and heals 1.0 — so a dandelion at {@code (x + 1, y, z)}, distSq 1, is eaten in the very call.
 * {@code EntityGammaMetroid.scanForStone} scans the shells i = 1..5 around {@code ((int) x, (int) y + 1, (int) z)}
 * for the nearest stone, points the navigation at it and, when it is within distSq 12, removes it under
 * {@code mobGriefing}, heals 1.0 and burps — so a stone one east of that origin, distSq 1, is eaten in the very
 * call. Rolls pinned through the VortexParityTests.ForcedRoll seam: the Stinky's 1-in-200 target drop and both
 * 1-in-100 rolls (the self-heal, the activity flip) quiet, the 1-in-50 flower roll firing; the Metroid's 1-in-5
 * prey pass quiet (no scan, no walk), the 1-in-20 hurt roll firing with the health below max (the 1-in-100 pinned
 * quiet, never reached — the pair short-circuits). Geometry as PlayNicelyGateParityTests: the hunter frozen at
 * rel (20,1,24) of the 48x16x48 empty_large; {@code mobGriefing} is set on for the Metroid's test and restored
 * with the flag.</p>
 */
@GameTestHolder(OreSpawnMod.MOD_ID)
@PrefixGameTestTemplate(false)
public class PlayNicelyGriefingGateTests {

    /**
     * The structure-relative position of an absolute block. Not {@code GameTestHelper.relativePos}: the framework's
     * method rotates by the test rotation turned a further 180 degrees, so under {@code Rotation.NONE} it mirrors
     * both horizontal axes about the structure block (vanilla 1.21.1 GameTestHelper.java:873-878) and reports
     * rel (-21, 1, -24) for the block one east of rel (20, 1, 24). {@code absolutePos(ZERO)} is the structure block.
     */
    private static BlockPos rel(GameTestHelper helper, BlockPos absolute) {
        return absolute.subtract(helper.absolutePos(BlockPos.ZERO));
    }

    private static final String BATCH = "playNicelyGriefingGates";
    private static final String FINDING = "ENT-S-116";
    /** The hunter's spot, rel to the structure block (PlayNicelyGateParityTests.HUNTER_POS). */
    private static final BlockPos HUNTER_POS = new BlockPos(20, 1, 24);
    /** How far below max the hunter starts, so the site's heal(1.0f) reads as +1 and the Metroid's hurt roll applies. */
    private static final float HEALTH_DEFICIT = 10.0f;
    /** What one call heals (orig Stinky.java:601 / GammaMetroid.java:455 heal(1.0f); the port's eatFlowers / scanForStone). */
    private static final float HEAL_AMOUNT = 1.0f;
    private static final float HEALTH_EPSILON = 1.0e-3f;

    /** orig Stinky.java:583 (port EntityStinky.customServerAiStep): the flower-eat runs with the flag off, not with it on. */
    @GameTest(template = "empty_large", batch = BATCH)
    public static void s116_stinky_583_flower_eat(GameTestHelper helper) {
        run(helper, new StinkyFlowerProbe());
    }

    /** orig GammaMetroid.java:435 (port EntityGammaMetroid.customServerAiStep): the stone-eat runs with the flag off, not with it on. */
    @GameTest(template = "empty_large", batch = BATCH)
    public static void s116_gammametroid_435_stone_eat(GameTestHelper helper) {
        run(helper, new MetroidStoneProbe());
    }

    // ------------------------------------------------------------------
    // Runner: control with the flag down, the same site silent with it up, the flag restored
    // ------------------------------------------------------------------

    private static void run(GameTestHelper helper, GriefProbe probe) {
        final boolean prior = OreSpawnConfig.PLAY_NICELY.get();
        try {
            OreSpawnConfig.PLAY_NICELY.set(false);
            helper.assertTrue(!OreSpawnConfig.PLAY_NICELY.get(),
                    "precondition: PLAY_NICELY.set(false) must read back false (" + FINDING + ")");
            probe.setUp(helper);
            probe.drive();
            probe.observe(helper);
            helper.assertTrue(probe.showed(), "control: with playNicely off " + probe.where() + " must show "
                    + probe.effect() + " in the one call — saw " + probe.trace + " (" + FINDING + ")");
            probe.reset(helper);
            OreSpawnConfig.PLAY_NICELY.set(true);
            helper.assertTrue(OreSpawnConfig.PLAY_NICELY.get(),
                    "precondition: PLAY_NICELY.set(true) must read back true (" + FINDING + ")");
            probe.drive();
            probe.observe(helper);
            helper.assertTrue(probe.untouched(), probe.where() + " with playNicely on: orig " + probe.orig()
                    + " gates the eat out while PlayNicely != 0, read live, so the same hunter under the same pinned rolls that showed "
                    + probe.effect() + " with the flag off must show nothing of it — the block still standing, the health unmoved"
                    + " — saw " + probe.trace + " (" + FINDING + ")");
        } finally {
            OreSpawnConfig.PLAY_NICELY.set(prior);
            probe.cleanUp(helper);
        }
        helper.succeed();
    }

    // ------------------------------------------------------------------
    // The griefing shape: one call eats the block and heals with the flag down, touches nothing with it up
    // ------------------------------------------------------------------

    /**
     * One griefing site: the hunter frozen at {@link #HUNTER_POS} with its health {@link #HEALTH_DEFICIT} below max
     * and its rolls pinned, the eaten block placed where the site's first ring finds it within distSq 12, so one
     * call of the port method eats it and heals. {@link #observe} reads the block and the health after a drive,
     * {@link #reset} puts both back for the second drive, {@link #cleanUp} runs in the finally, tolerant of a
     * set-up that never finished.
     */
    private abstract static class GriefProbe {
        Mob hunter;
        /** The eaten block, absolute. */
        BlockPos block;
        float healthBefore;
        float healthAfter;
        boolean standing;
        boolean gone;
        String trace = "(not driven)";

        abstract String where();

        abstract String orig();

        abstract String effect();

        abstract Block eaten();

        abstract void setUp(GameTestHelper helper);

        /** What else the trace carries after a drive. */
        String more() {
            return "";
        }

        /** The port method that carries the site — the protected customServerAiStep — once. */
        void drive() {
            invoke(this.hunter, this.hunter.getClass(), "customServerAiStep");
        }

        void observe(GameTestHelper helper) {
            BlockState state = helper.getLevel().getBlockState(this.block);
            this.standing = state.is(eaten());
            this.gone = state.isAir();
            this.healthAfter = this.hunter.getHealth();
            this.trace = name(eaten()) + " at rel " + rel(helper, this.block).toShortString() + " "
                    + (this.standing ? "standing" : this.gone ? "gone (air)" : "replaced by " + state)
                    + ", health " + this.healthBefore + " -> " + this.healthAfter + more();
        }

        /** The control's effect: the block eaten to air and the hunter healed by exactly {@link #HEAL_AMOUNT}. */
        boolean showed() {
            return this.gone && Math.abs(this.healthAfter - (this.healthBefore + HEAL_AMOUNT)) < HEALTH_EPSILON;
        }

        /** Nothing of it: the block still standing and the health where it was. */
        boolean untouched() {
            return this.standing && Math.abs(this.healthAfter - this.healthBefore) < HEALTH_EPSILON;
        }

        void startBelowMax(GameTestHelper helper) {
            float max = this.hunter.getMaxHealth();
            this.healthBefore = max - HEALTH_DEFICIT;
            this.hunter.setHealth(this.healthBefore);
            helper.assertTrue(this.hunter.getHealth() == this.healthBefore && this.healthBefore < max,
                    "precondition: the " + this.hunter.getClass().getSimpleName() + " starts " + HEALTH_DEFICIT + " below its max "
                            + max + ", so the site's heal(1.0f) reads as +1 — got " + this.hunter.getHealth() + " (" + FINDING + ")");
        }

        void place(GameTestHelper helper) {
            helper.getLevel().setBlock(this.block, eaten().defaultBlockState(), 3);
            helper.assertTrue(helper.getLevel().getBlockState(this.block).is(eaten()), "precondition: the " + name(eaten())
                    + " must stand at rel " + rel(helper, this.block).toShortString() + " before the drive (" + FINDING + ")");
        }

        void reset(GameTestHelper helper) {
            place(helper);
            this.hunter.setHealth(this.healthBefore);
            helper.assertTrue(this.hunter.getHealth() == this.healthBefore, "reset: the health must be back at "
                    + this.healthBefore + " before the flag-on drive — got " + this.hunter.getHealth() + " (" + FINDING + ")");
        }

        void cleanUp(GameTestHelper helper) {
            discardQuietly(this.hunter);
        }
    }

    /**
     * orig Stinky.java:583 (port EntityStinky.customServerAiStep, the flower-eat): activity 1 — the idle state orig
     * :582 runs the eat inside — set through the setter (field and synched value), the 1-in-200 target drop and both
     * 1-in-100 rolls (the self-heal, the activity flip) pinned quiet, the 1-in-50 flower roll pinned to fire; a
     * dandelion one block east of the Stinky's block at its feet level — the first ring's {@code (x + 1, y + 1 - 1, z)}
     * probe, distSq 1 of the Stinky's block — gone and the Stinky healed by 1 with the flag off, standing and the
     * health unmoved with it on.
     */
    private static final class StinkyFlowerProbe extends GriefProbe {
        private EntityStinky stinky;

        @Override
        String where() {
            return "EntityStinky.customServerAiStep (the flower-eat)";
        }

        @Override
        String orig() {
            return "Stinky.java:583";
        }

        @Override
        String effect() {
            return "the dandelion one block east eaten (air) and the Stinky healed by " + HEAL_AMOUNT;
        }

        @Override
        Block eaten() {
            return Blocks.DANDELION;
        }

        @Override
        String more() {
            return ", activity " + this.stinky.activity;
        }

        @Override
        void setUp(GameTestHelper helper) {
            this.stinky = spawnFrozen(helper, ModEntities.ENTITY_STINKY.get(), HUNTER_POS);
            this.hunter = this.stinky;
            this.stinky.setActivity(1);
            helper.assertTrue(this.stinky.activity == 1 && this.stinky.getActivity() == 1,
                    "precondition: activity 1 — orig Stinky.java:582 runs the flower-eat inside activity == 1 only (" + FINDING + ")");
            helper.assertTrue(!this.stinky.isOrderedToSit() && !this.stinky.isTame(),
                    "precondition: a fresh Stinky is untamed and not sitting, so the step reaches the site with activity 1 kept"
                            + " — no owner-flying or far-owner flip to 2 (" + FINDING + ")");
            startBelowMax(helper);
            replaceRandom(this.stinky, rolls(200, 0, 100, 0, 50, 0));
            this.block = BlockPos.containing(this.stinky.getX() + 1, this.stinky.getY(), this.stinky.getZ());
            helper.assertTrue(this.block.equals(this.stinky.blockPosition().east())
                            && rel(helper, this.block).equals(HUNTER_POS.east()),
                    "precondition: the dandelion's spot is one block east of the Stinky's block at its feet level, rel "
                            + HUNTER_POS.east().toShortString() + " — the port's first-ring probe (x + 1, y + 1 - 1, z) — got rel "
                            + rel(helper, this.block).toShortString() + " (" + FINDING + ")");
            helper.assertTrue(this.stinky.blockPosition().distSqr(this.block) < 12.0,
                    "precondition: the dandelion is inside the eat radius, distSq < 12 of the Stinky's block (orig Stinky.java:599)"
                            + " — distSq " + this.stinky.blockPosition().distSqr(this.block) + " (" + FINDING + ")");
            place(helper);
        }
    }

    /**
     * orig GammaMetroid.java:435 (port EntityGammaMetroid.customServerAiStep, the stone-eat): the 1-in-5 prey pass
     * pinned quiet (no scan, no walk), the 1-in-20 hurt roll pinned to fire with the health below max (the 1-in-100
     * pinned quiet and never reached — the pair short-circuits), the Metroid a fresh spawn: not sitting (the gate's
     * own term), untamed, grown; {@code mobGriefing} set on for the test (restored in the finally) so the removal is
     * live; a stone one block east of the port's scan origin {@code ((int) x, (int) y + 1, (int) z)} — the first
     * ring's +x face, distSq 1; rel (21,2,24) where the structure sits at positive coordinates — gone and the Metroid
     * healed by 1 with the flag off, standing and the health unmoved with it on.
     */
    private static final class MetroidStoneProbe extends GriefProbe {
        private EntityGammaMetroid metroid;
        private GameRules.BooleanValue mobGriefing;
        private boolean priorMobGriefing;
        private boolean mobGriefingTouched;

        @Override
        String where() {
            return "EntityGammaMetroid.customServerAiStep (the stone-eat)";
        }

        @Override
        String orig() {
            return "GammaMetroid.java:435";
        }

        @Override
        String effect() {
            return "the stone one block east of the scan origin eaten (air) and the Metroid healed by " + HEAL_AMOUNT;
        }

        @Override
        Block eaten() {
            return Blocks.STONE;
        }

        @Override
        String more() {
            return ", closestStoneDistSq " + readField(this.metroid, EntityGammaMetroid.class, "closestStoneDistSq");
        }

        @Override
        void setUp(GameTestHelper helper) {
            ServerLevel level = helper.getLevel();
            this.mobGriefing = level.getGameRules().getRule(GameRules.RULE_MOBGRIEFING);
            this.priorMobGriefing = this.mobGriefing.get();
            this.mobGriefingTouched = true;
            this.mobGriefing.set(true, level.getServer());
            helper.assertTrue(level.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING),
                    "precondition: mobGriefing on, so the stone-eat's removal (orig GammaMetroid.java:452, port scanForStone) is live (" + FINDING + ")");
            this.metroid = spawnFrozen(helper, ModEntities.ENTITY_GAMMA_METROID.get(), HUNTER_POS);
            this.hunter = this.metroid;
            helper.assertTrue(!this.metroid.isOrderedToSit() && !this.metroid.isTame() && !this.metroid.isBaby(),
                    "precondition: a fresh Metroid is not sitting (the gate's own term, orig GammaMetroid.java:435 !isSitting()),"
                            + " untamed and grown (" + FINDING + ")");
            startBelowMax(helper);
            replaceRandom(this.metroid, rolls(5, 1, 20, 0, 100, 1));
            BlockPos origin = new BlockPos((int) this.metroid.getX(), (int) this.metroid.getY() + 1, (int) this.metroid.getZ());
            this.block = origin.east();
            helper.assertTrue(helper.getBounds().contains(Vec3.atCenterOf(this.block)),
                    "precondition: the stone's spot, one block east of the port's scan origin " + origin.toShortString() + " (rel "
                            + rel(helper, origin).toShortString() + "), lies inside the structure — the first ring's +x face,"
                            + " distSq 1, inside the eat radius distSq < 12 (orig GammaMetroid.java:451) (" + FINDING + ")");
            place(helper);
        }

        @Override
        void cleanUp(GameTestHelper helper) {
            super.cleanUp(helper);
            if (this.mobGriefingTouched) {
                this.mobGriefing.set(this.priorMobGriefing, helper.getLevel().getServer());
            }
        }
    }

    // ------------------------------------------------------------------
    // Helpers (the PlayNicelyGateParityTests idiom)
    // ------------------------------------------------------------------

    /** Frozen at its spot: goals stripped, noAi, persistence set (the PlayNicelyGateParityTests idiom). */
    private static <E extends Mob> E spawnFrozen(GameTestHelper helper, EntityType<E> type, BlockPos pos) {
        E mob = helper.spawnWithNoFreeWill(type, pos);
        mob.setNoAi(true);
        mob.setPersistenceRequired();
        return mob;
    }

    private static void discardQuietly(Entity entity) {
        if (entity != null && !entity.isRemoved()) {
            entity.discard();
        }
    }

    /** A seeded random with each (bound, answer) pair pinned — the VortexParityTests.ForcedRoll seam, chained as PlayNicelyGateParityTests.rolls. */
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

    private static Object invoke(Object target, Class<?> declaring, String name) {
        String where = declaring.getSimpleName() + "." + name;
        try {
            Method method = declaring.getDeclaredMethod(name);
            method.setAccessible(true);
            return method.invoke(target);
        } catch (InvocationTargetException exception) {
            throw new IllegalStateException(where + " threw", exception.getCause());
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("cannot invoke " + where, exception);
        }
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

    private static String name(Block block) {
        return BuiltInRegistries.BLOCK.getKey(block).toString();
    }
}
