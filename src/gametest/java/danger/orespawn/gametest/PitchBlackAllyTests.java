package danger.orespawn.gametest;

import danger.orespawn.ModEntities;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.PitchBlack;
import danger.orespawn.util.MyUtils;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestGenerator;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestFunction;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.block.Rotation;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/**
 * ENT-S-112 (owner ruling 2026-09-04: "ENT-S-108 through 113: all parity, fix in classic"):
 * the Nightmare's ally exclusions. orig PitchBlack.java:485-538 {@code isSuitableTarget}
 * refuses, after the self-kind check (:504-506) and ahead of the creative-player check
 * (:531-536), eight species by name: EnderReaper (:507), LeafMonster (:510), TerribleTerror
 * (:513), LurkingTerror (:516), CreepingHorror (:519), Island (:522), IslandToo (:525) and
 * Triffid (:528) -- the Danger Dimension's own fauna, left alone by its apex predator. The
 * port's filter (self-kind, the ignore screen, creative players) named none of them.
 *
 * <p>One generated test per excluded species (the IgnoreScreenParityTests shape: a
 * {@link GameTestGenerator} over the eight-row table in {@link #allies()}, in orig order,
 * each row citing its orig lines), reached through the private
 * {@code isSuitableTarget(LivingEntity)} by reflection. The species stands frozen 8 blocks in
 * front of a frozen Nightmare with line of sight asserted, off the shared ignore list (so the
 * ENT-S-106 step is not what refuses it) and no Nightmare itself (nor the self-kind step); the
 * filter must refuse it, and on the same spot a vanilla pig (the Animal control, for the two
 * Islands) and a vanilla Zombie (the Monster control, for the six Monster allies) must both be
 * accepted, so geometry, sight, the ignore step and a species' base class are not what refused
 * it -- only its name.</p>
 *
 * <p>Synchronous, no global state touched. Own batch (TEST-003). Template empty_large
 * (48x16x48): the Nightmare at (20, 1, 24) keeps its registered 2x3 box until a size tier is
 * applied (finalizeSpawn or NBT, neither of which the spawn helper runs), so its eye sits at
 * rel 3.55, under the barrier ceiling at rel 17 -- as would the largest tier's 10x14 box (eye
 * rel 12.9); the prey at (28, 1, 24), the tallest of them the 2x4 Triffid. Spawns are
 * discarded in a finally.</p>
 */
@GameTestHolder(OreSpawnMod.MOD_ID)
@PrefixGameTestTemplate(false)
public class PitchBlackAllyTests {

    private static final String BATCH = "pitchBlackAllies";
    private static final String TEST_PREFIX = "pitchblackallytests.";
    /** Generated TestFunctions bypass the holder's template prefixing, so the template is named in full. */
    private static final String EMPTY_LARGE = OreSpawnMod.MOD_ID + ":empty_large";
    private static final int TIMEOUT_TICKS = 100;

    /** Hunter and prey 8 blocks apart on the floor, clear line of sight. */
    private static final BlockPos HUNTER_POS = new BlockPos(20, 1, 24);
    private static final BlockPos PREY_POS = new BlockPos(28, 1, 24);

    /** One excluded species: its orig PitchBlack.java lines, the port's type, and what it is. */
    private record Ally(int index, String tag, String orig,
                        Supplier<? extends EntityType<? extends Mob>> species, String speciesWhy) {
        String id() {
            return String.format("s112_%02d_%s", this.index, this.tag);
        }

        String testName() {
            return TEST_PREFIX + this.id();
        }
    }

    /** The eight exclusions, orig PitchBlack.java:507-530, in orig order. */
    private static List<Ally> allies() {
        List<Ally> allies = new ArrayList<>();
        allies.add(new Ally(1, "ender_reaper_507", "PitchBlack.java:507-509", ModEntities.ENDER_REAPER,
                "an Ender Reaper (port EnderReaper, a Monster)"));
        allies.add(new Ally(2, "leaf_monster_510", "PitchBlack.java:510-512", ModEntities.ENTITY_LEAF_MONSTER,
                "a Leaf Monster (port EntityLeafMonster, a Monster)"));
        allies.add(new Ally(3, "terrible_terror_513", "PitchBlack.java:513-515", ModEntities.ENTITY_TERRIBLE_TERROR,
                "a Terrible Terror (port EntityTerribleTerror, a Monster)"));
        allies.add(new Ally(4, "lurking_terror_516", "PitchBlack.java:516-518", ModEntities.ENTITY_LURKING_TERROR,
                "a Lurking Terror (port EntityLurkingTerror, a Monster)"));
        allies.add(new Ally(5, "creeping_horror_519", "PitchBlack.java:519-521", ModEntities.CREEPING_HORROR,
                "a Creeping Horror (port CreepingHorror, a Monster)"));
        allies.add(new Ally(6, "island_522", "PitchBlack.java:522-524", ModEntities.ISLAND,
                "an Island (port Island, an Animal)"));
        allies.add(new Ally(7, "island_too_525", "PitchBlack.java:525-527", ModEntities.ISLAND_TOO,
                "an IslandToo (port IslandToo, an Animal)"));
        allies.add(new Ally(8, "triffid_528", "PitchBlack.java:528-530", ModEntities.ENTITY_TRIFFID,
                "a Triffid (port EntityTriffid, a Monster)"));
        return allies;
    }

    /** One test per excluded species: eight TestFunctions in the {@code pitchBlackAllies} batch. */
    @GameTestGenerator
    public Collection<TestFunction> allyExclusions() {
        List<TestFunction> functions = new ArrayList<>();
        for (Ally ally : allies()) {
            functions.add(new TestFunction(BATCH, ally.testName(), EMPTY_LARGE, Rotation.NONE, TIMEOUT_TICKS, 0L, true,
                    helper -> run(helper, ally)));
        }
        return functions;
    }

    // ------------------------------------------------------------------
    // Runner
    // ------------------------------------------------------------------

    private static void run(GameTestHelper helper, Ally ally) {
        Mob hunter = null;
        Mob species = null;
        Mob control = null;
        try {
            hunter = spawnFrozen(helper, ModEntities.PITCH_BLACK.get(), HUNTER_POS);
            String where = "PitchBlack.isSuitableTarget (orig " + ally.orig() + ")";
            species = spawnFrozen(helper, ally.species().get(), PREY_POS);
            helper.assertTrue(!MyUtils.isIgnoreable(species), "precondition: " + ally.speciesWhy()
                    + " is not on the shared ignore list, so the orig PitchBlack.java:498-500 step (ENT-S-106) is not what"
                    + " refuses it (ENT-S-112 test setup)");
            helper.assertTrue(!(species instanceof PitchBlack), "precondition: " + ally.speciesWhy()
                    + " is no Nightmare, so the orig PitchBlack.java:504-506 self-kind step is not what refuses it"
                    + " (ENT-S-112 test setup)");
            assertSees(helper, hunter, species, ally.speciesWhy());
            boolean accepted = invokeFilter(hunter, species);
            helper.assertTrue(!accepted, where + ": " + ally.speciesWhy() + " is excluded by name at orig " + ally.orig()
                    + " and must be refused, but it was accepted (ENT-S-112)");
            species.discard();
            species = null;

            control = spawnFrozen(helper, EntityType.PIG, PREY_POS);
            assertSees(helper, hunter, control, "a vanilla pig");
            helper.assertTrue(invokeFilter(hunter, control), "control: " + where + " must accept a vanilla pig on the same"
                    + " spot (no exclusion names it; orig :537 answers true), so " + ally.speciesWhy() + " was refused by its"
                    + " name and not by geometry, sight or the rest of the chain (ENT-S-112)");
            control.discard();
            control = null;

            control = spawnFrozen(helper, EntityType.ZOMBIE, PREY_POS);
            helper.assertTrue(control instanceof Monster, "precondition: a Zombie is a Monster (ENT-S-112 test setup)");
            assertSees(helper, hunter, control, "a vanilla Zombie");
            helper.assertTrue(invokeFilter(hunter, control), "control: " + where + " must accept a vanilla Zombie on the same"
                    + " spot (a Monster no exclusion names; the Nightmare hunts monsters, orig :537), so a Monster base class"
                    + " is not what refused " + ally.speciesWhy() + " (ENT-S-112)");
        } finally {
            if (control != null) control.discard();
            if (species != null) species.discard();
            if (hunter != null) hunter.discard();
        }
        helper.succeed();
    }

    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------

    private static void assertSees(GameTestHelper helper, Mob hunter, LivingEntity target, String what) {
        helper.assertTrue(hunter.hasLineOfSight(target),
                "precondition: the Nightmare (eye " + String.format("%.2f", hunter.getEyeHeight()) + " above its feet on the"
                        + " floor) must see " + what + " on the prey spot inside the barrier shell (ENT-S-112 test geometry)");
    }

    /** Frozen: goals stripped, noAi, persistence set (the IgnoreListParityTests idiom). */
    private static Mob spawnFrozen(GameTestHelper helper, EntityType<? extends Mob> type, BlockPos pos) {
        Mob mob = helper.spawnWithNoFreeWill(type, pos);
        mob.setNoAi(true);
        mob.setPersistenceRequired();
        return mob;
    }

    /** The Nightmare's private {@code isSuitableTarget(LivingEntity)} -- the port's one-arg shape of the orig two-arg method. */
    private static boolean invokeFilter(Mob hunter, LivingEntity candidate) {
        String name = hunter.getClass().getSimpleName() + ".isSuitableTarget";
        try {
            Method method = hunter.getClass().getDeclaredMethod("isSuitableTarget", LivingEntity.class);
            method.setAccessible(true);
            return (Boolean) method.invoke(hunter, candidate);
        } catch (InvocationTargetException exception) {
            throw new IllegalStateException(name + " threw", exception.getCause());
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("cannot invoke " + name, exception);
        }
    }
}
