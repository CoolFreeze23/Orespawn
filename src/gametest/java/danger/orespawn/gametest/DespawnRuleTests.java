package danger.orespawn.gametest;

import danger.orespawn.ModEntities;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.EntityLurkingTerror;
import java.util.List;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.TamableAnimal;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/**
 * ENT-S-171: the despawn rule ({@code removeWhenFarAway}, orig {@code canDespawn}/{@code func_70692_ba}) transcribed
 * per class. 1.21 {@code Mob.checkDespawn} keeps the persistent and asks {@code removeWhenFarAway} for the rest at the
 * same 128 / 32-block distances 1.7.10 used, so the per-class answer is the whole of the original's rule. Pins the
 * animals the original despawned (the port's Animal default never did), the mobs and monsters it never despawned
 * (the port's Mob default did), the child, tamed, ridden and attacking clauses, and the clock-bound rules.
 *
 * <p>Every entity here is built with {@code EntityType.create}, never {@code GameTestHelper.spawn}: the helper marks
 * each mob it spawns persistence-required, which would answer the first clause of every rule for the wrong reason.
 */
@GameTestHolder(OreSpawnMod.MOD_ID)
@PrefixGameTestTemplate(false)
public class DespawnRuleTests {

    private static final double FAR = 1.0E8;

    /** Orig: {@code return !isNoDespawnRequired()} (plus the child and tamed clauses, which a fresh adult does not meet). */
    @GameTest(template = "empty")
    public static void s171a_animals_the_original_despawned_despawn(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        List<EntityType<? extends Mob>> types = List.of(
                ModEntities.FROG.get(), ModEntities.ENTITY_CRICKET.get(), ModEntities.ENTITY_TSHIRT.get(),
                ModEntities.BARYONYX.get(), ModEntities.CASSOWARY.get(), ModEntities.FLOUNDER.get(),
                ModEntities.ENTITY_STINK_BUG.get(), ModEntities.WHALE.get(),
                ModEntities.CAMARASAURUS.get(), ModEntities.CHIPMUNK.get(), ModEntities.WATER_DRAGON.get(),
                ModEntities.THE_PRINCE_ADULT.get(), ModEntities.THE_PRINCE_TEEN.get());
        for (EntityType<? extends Mob> type : types) {
            Mob mob = type.create(level);
            helper.assertTrue(mob != null, type.toShortString() + " could not be created");
            helper.assertTrue(!mob.isPersistenceRequired(), type.toShortString() + " came out persistent (the pin would be vacuous)");
            helper.assertTrue(mob.removeWhenFarAway(FAR),
                    type.toShortString() + ": a wild, grown, non-persistent one refuses to despawn (ENT-S-171)");
        }
        helper.succeed();
    }

    /** Orig: {@code return false} on mobs and monsters whose 1.21 default (Mob, Monster) would despawn them. */
    @GameTest(template = "empty")
    public static void s171b_mobs_the_original_kept_stay(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        List<EntityType<? extends Mob>> types = List.of(
                ModEntities.ENTITY_WORM_LARGE.get(), ModEntities.ENTITY_WORM_MEDIUM.get(),
                ModEntities.ENTITY_WORM_SMALL.get(), ModEntities.ANT_ROBOT.get(), ModEntities.ELEVATOR.get(),
                ModEntities.SPIDER_ROBOT.get(), ModEntities.PURPLE_POWER.get(), ModEntities.ROCK_BASE.get(),
                ModEntities.GODZILLA_HEAD.get(), ModEntities.KING_HEAD.get(), ModEntities.QUEEN_HEAD.get());
        for (EntityType<? extends Mob> type : types) {
            Mob mob = type.create(level);
            helper.assertTrue(mob != null, type.toShortString() + " could not be created");
            helper.assertTrue(!mob.removeWhenFarAway(FAR),
                    type.toShortString() + ": the original never despawns it, the port would (ENT-S-171)");
        }
        helper.succeed();
    }

    /** The tamed stay (Water Dragon); a ridden Prince stays, an unridden wild one goes; a Lurking Terror stays while attacking. */
    @GameTest(template = "empty")
    public static void s171c_the_tamed_the_ridden_and_the_attacking_stay(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();

        TamableAnimal tamed = ModEntities.WATER_DRAGON.get().create(level);
        tamed.setTame(true, false);
        helper.assertTrue(!tamed.removeWhenFarAway(FAR), "a tamed Water Dragon despawns (orig WaterDragon.java:179-188)");

        Mob prince = ModEntities.THE_PRINCE_ADULT.get().create(level);
        helper.assertTrue(!prince.isPersistenceRequired() && prince.removeWhenFarAway(FAR),
                "control: a wild, unridden, non-persistent Prince adult should despawn (orig ThePrinceAdult.java:1303-1311)");
        Mob rider = ModEntities.FROG.get().create(level);
        helper.assertTrue(rider.startRiding(prince, true) && prince.isVehicle(), "the test rider could not mount the Prince");
        helper.assertTrue(!prince.removeWhenFarAway(FAR), "a ridden Prince despawns (orig ThePrinceAdult.java:1303-1311)");

        EntityLurkingTerror terror = ModEntities.ENTITY_LURKING_TERROR.get().create(level);
        terror.setAttacking(1);
        helper.assertTrue(!terror.removeWhenFarAway(FAR), "an attacking Lurking Terror despawns (orig LurkingTerror.java:84-89)");
        terror.setAttacking(0);
        helper.assertTrue(terror.removeWhenFarAway(FAR), "an idle Lurking Terror stays");
        helper.succeed();
    }

    /**
     * The clock rules against the level's own {@code isDay}, the call the overrides make: the Gold Fish despawns only by
     * night (orig GoldFish.java:39-44); the Pitch Black (orig PitchBlack.java:208-213), the Creeping Horror (orig
     * CreepingHorror.java:230-235) and the Firefly (orig Firefly.java:186-191) only by day.
     */
    @GameTest(template = "empty")
    public static void s171d_the_clock_bound_rules_follow_the_clock(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        boolean day = level.isDay();
        Mob fish = ModEntities.GOLD_FISH.get().create(level);
        helper.assertTrue(fish.removeWhenFarAway(FAR) == !day, "the Gold Fish's answer does not follow !isDay (" + day + ")");
        for (EntityType<? extends Mob> type : List.<EntityType<? extends Mob>>of(
                ModEntities.PITCH_BLACK.get(), ModEntities.CREEPING_HORROR.get(), ModEntities.FIREFLY.get())) {
            Mob mob = type.create(level);
            helper.assertTrue(mob.removeWhenFarAway(FAR) == day,
                    type.toShortString() + ": the answer does not follow isDay (" + day + ")");
        }
        helper.succeed();
    }

    /**
     * The child clause, orig {@code if (isChild()) { setPersistenceRequired(); return false; }}: a child that meets the
     * check is kept AND made persistent for life. Pins the five that gained the clause, the two whose clause had been
     * dropped (Easter Bunny, Peacock), the four whose clause had lost the persistence line (Lizard, Ostrich, Rubber
     * Ducky, Velocity Raptor) and the three tameables that carry it before their tamed clause.
     */
    @GameTest(template = "empty")
    public static void s171e_a_child_is_kept_and_made_persistent(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        List<EntityType<? extends Mob>> types = List.of(
                ModEntities.BARYONYX.get(), ModEntities.CASSOWARY.get(), ModEntities.FLOUNDER.get(),
                ModEntities.ENTITY_STINK_BUG.get(), ModEntities.WHALE.get(),
                ModEntities.EASTER_BUNNY.get(), ModEntities.PEACOCK.get(),
                ModEntities.LIZARD.get(), ModEntities.OSTRICH.get(), ModEntities.ENTITY_RUBBER_DUCKY.get(),
                ModEntities.VELOCITY_RAPTOR.get(),
                ModEntities.CAMARASAURUS.get(), ModEntities.CHIPMUNK.get(), ModEntities.WATER_DRAGON.get());
        for (EntityType<? extends Mob> type : types) {
            Mob child = type.create(level);
            child.setBaby(true);
            helper.assertTrue(child.isBaby(), type.toShortString() + " did not become a child");
            helper.assertTrue(!child.isPersistenceRequired(), type.toShortString() + " child came out persistent before the check");
            helper.assertTrue(!child.removeWhenFarAway(FAR), type.toShortString() + ": a child despawns (ENT-S-171)");
            helper.assertTrue(child.isPersistenceRequired(),
                    type.toShortString() + ": the child was not made persistent by the check (orig setPersistenceRequired)");
        }
        helper.succeed();
    }
}
