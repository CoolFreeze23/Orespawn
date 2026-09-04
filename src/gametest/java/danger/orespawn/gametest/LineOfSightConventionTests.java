package danger.orespawn.gametest;

import danger.orespawn.ModEntities;
import danger.orespawn.OreSpawnConfig;
import danger.orespawn.OreSpawnMod;
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
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/**
 * ENT-S-121 (owner's ruling 2026-09-04: "adopt the 1.7.10 convention port-wide, not per site"). 1.7.10's
 * {@code EntityLivingBase.canEntityBeSeen} was {@code rayTraceBlocks(eyes, targetEyes) == null} — the two-argument
 * overload, {@code (stopOnLiquid = false, ignoreBlockWithoutBoundingBox = false, returnLastUncollidableBlock = false)}:
 * every collidable block tested on its selection bounds, liquids never stopping the ray — vanilla 1.21.1's
 * {@code ClipContext.Block.OUTLINE} with {@code Fluid.NONE}, where vanilla's {@code LivingEntity.hasLineOfSight} clips
 * {@code COLLIDER}. The port's convention is one HEAD injection ({@code LivingEntitySightMixin} →
 * {@code OreSpawnSight.canSee}) on {@code hasLineOfSight}, gated on the receiver's registry namespace, so every
 * {@code getSensing().hasLineOfSight} / {@code hasLineOfSight} site of the port's hunters and every vanilla goal on an
 * OreSpawn mob reads the 1.7.10 ray, vanilla mobs keep vanilla's, and {@code Sensing}'s per-tick cache is untouched.
 * Separately, the five earlier ports of the feet-level helper (ThePrinceAdult / ThePrinceTeen {@code canSeeSpot},
 * Kraken / EntityBrutalfly / Cockateil {@code canSeeTarget}) now clip OUTLINE like the Vortex's (ENT-S-089) and the
 * four of ENT-S-118.
 *
 * <p>Geometry (the SightStepParityTests floor): the hunter frozen on the floor of the 48x16x48 empty_large at rel
 * (20, 1, ·), the prey 8 blocks east at (28, 1, ·). Eye-line rows: a dirt block at (24, 1, 24) carrying the occluder
 * at (24, 2, 24) — short grass (XYZ-offset), a torch, a poppy (XZ-offset) — collision-less and selection-bounded
 * (asserted). Offset plants shift their selection box by up to 0.25 in x / z and 0.2 down, seeded by the block's
 * absolute column, so hunter and prey are spawned on the z of the occluder's actual box; their eye line (a Fairy's
 * 1.68 to a Zombie's 2.74, or a Zombie's 2.74 to a Pig's 1.765 — 2.13..2.33 across the occluder column) crosses the
 * box for every offset and never touches the dirt, asserted directly before every verdict: the OUTLINE clip stops on
 * the occluder, the COLLIDER clip misses. Feet rows: the ENT-S-118 mapping row — a short-grass parapet on the floor
 * at (27, 1, 24), the helper's ray from (20.5, 1.75, 24.5) to (28.5, 1.0, 24.5) crossing it at y 1.14..1.05.</p>
 *
 * <p>Synchronous; frozen (noAi) mobs never tick {@code Sensing}, so the cache is cleared by hand where a cached site
 * is driven. {@code PLAY_NICELY} is set false for the scan row and restored in a finally (the scan answers null under
 * it, ENT-S-115); spawns discarded and occluders razed in the finally. Every row fails with the convention reverted
 * (the mixin removed, or a feet helper back on COLLIDER); the vanilla control row carries an OreSpawn half so it does
 * too, and the through-water leg rides the short-grass row for the same reason (it pins {@code Fluid.NONE}, the leg
 * ENT-S-118 left unpinned).</p>
 */
@GameTestHolder(OreSpawnMod.MOD_ID)
@PrefixGameTestTemplate(false)
public class LineOfSightConventionTests {

    private static final String BATCH = "lineOfSightConvention";
    private static final String TEST_PREFIX = "lineofsightconventiontests.";
    /** Generated TestFunctions bypass the holder's template prefixing, so the template is named in full (SightStepParityTests). */
    private static final String EMPTY_LARGE = OreSpawnMod.MOD_ID + ":empty_large";
    private static final int TIMEOUT_TICKS = 100;
    private static final String FINDING = "ENT-S-121";

    /** The hunter's floor block; the eye-line rows replace its z by the occluder's box centre. */
    private static final BlockPos HUNTER_POS = new BlockPos(20, 1, 24);
    /** 8 blocks east: inside the Fairy's 8-block scan box (Fairy.FLIGHT_SEARCH_RANGE). */
    private static final BlockPos PREY_POS = new BlockPos(28, 1, 24);
    /** The dirt carrying the eye-line occluder, and the occluder on it, midway between hunter and prey. */
    private static final BlockPos SUPPORT_POS = new BlockPos(24, 1, 24);
    private static final BlockPos OCCLUDER_POS = new BlockPos(24, 2, 24);
    /** The feet-ray parapet: the floor-row block in front of the ray's end (SightStepParityTests.PARAPET_POS). */
    private static final BlockPos PARAPET_POS = new BlockPos(27, 1, 24);
    /** The feet ray's end: a target's own position on the floor 8 blocks east (SightStepParityTests: (28.5, 1.0, 24.5)). */
    private static final Vec3 FEET_END = new Vec3(28.5, 1.0, 24.5);
    /** The feet helpers' eye height above the feet (orig {@code posY + 0.75}). */
    private static final double FEET_EYE = 0.75;
    /** Prey health, high enough that nothing incidental kills it. */
    private static final float PREY_HEALTH = 1000.0f;

    // ------------------------------------------------------------------
    // The row table
    // ------------------------------------------------------------------

    private interface Body {
        void run(GameTestHelper helper);
    }

    private record Row(int index, String tag, Body body) {
        String testName() {
            return TEST_PREFIX + String.format("s121_%02d_%s", this.index, this.tag);
        }
    }

    private static List<Row> rows() {
        List<Row> rows = new ArrayList<>();
        // (1) the eye-to-eye convention on an OreSpawn hunter: hidden behind three collision-less blocks, seen with them
        //     razed, seen through water
        rows.add(new Row(1, "fairy_eye_line_short_grass", helper -> eyeLineRow(helper, Blocks.SHORT_GRASS, "short grass", true)));
        rows.add(new Row(2, "fairy_eye_line_torch", helper -> eyeLineRow(helper, Blocks.TORCH, "torch", false)));
        rows.add(new Row(3, "fairy_eye_line_poppy", helper -> eyeLineRow(helper, Blocks.POPPY, "poppy", false)));
        // (2) the scope: a vanilla Zombie keeps vanilla's COLLIDER ray through the same grass
        rows.add(new Row(4, "vanilla_zombie_keeps_collider_ray", LineOfSightConventionTests::vanillaControlRow));
        // (3) a restored ENT-S-118 site reads the convention through Sensing
        rows.add(new Row(5, "fairy_scan_refuses_behind_short_grass", LineOfSightConventionTests::fairyScanRow));
        // (4) Sensing's per-tick memo is untouched
        rows.add(new Row(6, "sensing_cache_clears_per_tick", LineOfSightConventionTests::sensingCacheRow));
        // (5) the five feet-helper ports, COLLIDER -> OUTLINE
        rows.add(new Row(7, "theprinceadult_canseespot_short_grass",
                helper -> feetHelperRow(helper, ModEntities.THE_PRINCE_ADULT, "canSeeSpot", "ThePrinceAdult.java:545-547")));
        rows.add(new Row(8, "theprinceteen_canseespot_short_grass",
                helper -> feetHelperRow(helper, ModEntities.THE_PRINCE_TEEN, "canSeeSpot", "ThePrinceTeen.java:565-567")));
        rows.add(new Row(9, "kraken_canseetarget_short_grass",
                helper -> feetHelperRow(helper, ModEntities.KRAKEN, "canSeeTarget", "Kraken.java (canSeeTarget)")));
        rows.add(new Row(10, "brutalfly_canseetarget_short_grass",
                helper -> feetHelperRow(helper, ModEntities.ENTITY_BRUTALFLY, "canSeeTarget", "Brutalfly.java:147-149")));
        rows.add(new Row(11, "cockateil_canseetarget_short_grass",
                helper -> feetHelperRow(helper, ModEntities.COCKATEIL, "canSeeTarget", "Cockateil.java:166-168")));
        return rows;
    }

    /** One test per row: 11 TestFunctions in the {@code lineOfSightConvention} batch. */
    @GameTestGenerator
    public Collection<TestFunction> lineOfSightConventionRows() {
        List<TestFunction> functions = new ArrayList<>();
        for (Row row : rows()) {
            functions.add(new TestFunction(BATCH, row.testName(), EMPTY_LARGE, Rotation.NONE, TIMEOUT_TICKS, 0L, true, helper -> {
                row.body().run(helper);
                helper.succeed();
            }));
        }
        return functions;
    }

    // ------------------------------------------------------------------
    // (1) The eye-to-eye convention: hidden behind a selection box, seen with it gone, seen through water
    // ------------------------------------------------------------------

    private static void eyeLineRow(GameTestHelper helper, Block occluder, String what, boolean waterLeg) {
        Arena arena = new Arena(helper);
        try {
            EyeLine line = new EyeLine(helper, arena, occluder, what);
            line.spawn(ModEntities.FAIRY.get(), EntityType.ZOMBIE);
            line.assertOnSelectionBoxOnly();
            helper.assertTrue(!line.hunter.hasLineOfSight(line.prey), "Fairy.hasLineOfSight(Zombie) behind the " + what
                    + ": 1.7.10's canEntityBeSeen (rayTraceBlocks(eyes, eyes) -> func_147447_a(…, false, false, false)) tested every"
                    + " collidable block on its selection bounds, so the " + what + " hid the Zombie; the port's hunter must not see"
                    + " through it either — only vanilla's COLLIDER ray does (" + FINDING + ")");
            line.raze();
            helper.assertTrue(line.hunter.hasLineOfSight(line.prey), "control: with the " + what + " razed the Fairy must see the"
                    + " Zombie — the selection box, not the rest of the line, is what hid it (" + FINDING + ")");
            if (waterLeg) {
                line.flood();
                BlockHitResult fluid = clipEyes(helper, line.hunter, line.prey, ClipContext.Block.OUTLINE, ClipContext.Fluid.SOURCE_ONLY);
                helper.assertTrue(fluid.getType() == HitResult.Type.BLOCK && fluid.getBlockPos().equals(helper.absolutePos(OCCLUDER_POS)),
                        "precondition: a fluid-picking ray along the eye line must stop on the water source at " + OCCLUDER_POS
                                + " — the water is on the line — saw " + describeHit(helper, fluid) + " (" + FINDING + " test geometry)");
                helper.assertTrue(line.hunter.hasLineOfSight(line.prey), "Fairy.hasLineOfSight(Zombie) through a water source:"
                        + " 1.7.10's stopOnLiquid = false never stopped the ray on liquid — Fluid.NONE, the leg ENT-S-118 left unpinned"
                        + " (" + FINDING + ")");
            }
        } finally {
            arena.cleanUp();
        }
    }

    // ------------------------------------------------------------------
    // (2) The scope: vanilla receivers keep vanilla's ray
    // ------------------------------------------------------------------

    private static void vanillaControlRow(GameTestHelper helper) {
        Arena arena = new Arena(helper);
        try {
            EyeLine line = new EyeLine(helper, arena, Blocks.SHORT_GRASS, "short grass");
            line.spawn(ModEntities.FAIRY.get(), EntityType.ZOMBIE);
            line.assertOnSelectionBoxOnly();
            helper.assertTrue(!line.hunter.hasLineOfSight(line.prey), "the OreSpawn half: Fairy.hasLineOfSight(Zombie) behind the short"
                    + " grass must be false — the convention (" + FINDING + ")");
            line.discardPair();
            line.spawn(EntityType.ZOMBIE, EntityType.PIG);
            line.assertOnSelectionBoxOnly();
            helper.assertTrue(line.hunter.hasLineOfSight(line.prey), "the vanilla half: a Zombie's hasLineOfSight(Pig) through the same"
                    + " short grass — its eye line stopped by the selection box, as just asserted — must stay true: the convention is"
                    + " gated on the receiver's registry namespace (orespawn), so vanilla mobs keep vanilla's COLLIDER ray (" + FINDING + ")");
        } finally {
            arena.cleanUp();
        }
    }

    // ------------------------------------------------------------------
    // (3) A restored ENT-S-118 site: the Fairy's scan reads the convention through Sensing
    // ------------------------------------------------------------------

    private static void fairyScanRow(GameTestHelper helper) {
        helper.assertTrue(helper.getLevel().getDifficulty() != Difficulty.PEACEFUL,
                "precondition: the game-test level runs at NORMAL, not Peaceful — Fairy.isSuitableTarget refuses everything on"
                        + " Peaceful (" + FINDING + " test setup)");
        final boolean prior = OreSpawnConfig.PLAY_NICELY.get();
        Arena arena = new Arena(helper);
        try {
            OreSpawnConfig.PLAY_NICELY.set(false);
            helper.assertTrue(!OreSpawnConfig.PLAY_NICELY.get(),
                    "precondition: PLAY_NICELY.set(false) must read back false — findSomethingToAttack answers null under it"
                            + " (ENT-S-115), which is not the step under test (" + FINDING + " test setup)");
            EyeLine line = new EyeLine(helper, arena, Blocks.SHORT_GRASS, "short grass");
            line.spawn(ModEntities.FAIRY.get(), EntityType.ZOMBIE);
            line.assertOnSelectionBoxOnly();
            line.hunter.getSensing().tick();
            Object found = invoke(line.hunter, "findSomethingToAttack");
            helper.assertTrue(found == null, "Fairy.findSomethingToAttack behind the short grass: orig Fairy.java:232-234's canSee"
                    + " — the port's getSensing().hasLineOfSight (ENT-S-118) — reaches the convention through Sensing, so the scan"
                    + " must refuse the Zombie the selection-bounds ray cannot reach — saw " + describe((Entity) found) + " (" + FINDING + ")");
            line.raze();
            line.hunter.getSensing().tick();
            found = invoke(line.hunter, "findSomethingToAttack");
            helper.assertTrue(found == line.prey, "control: with the grass razed the scan must return the Zombie — the ray, not the"
                    + " rest of the filter, refused it — saw " + describe((Entity) found) + " (" + FINDING + ")");
        } finally {
            OreSpawnConfig.PLAY_NICELY.set(prior);
            arena.cleanUp();
        }
    }

    // ------------------------------------------------------------------
    // (4) Sensing's per-tick memo is untouched by the convention
    // ------------------------------------------------------------------

    private static void sensingCacheRow(GameTestHelper helper) {
        Arena arena = new Arena(helper);
        try {
            EyeLine line = new EyeLine(helper, arena, Blocks.SHORT_GRASS, "short grass");
            line.spawn(ModEntities.FAIRY.get(), EntityType.ZOMBIE);
            line.assertOnSelectionBoxOnly();
            Mob fairy = line.hunter;
            Mob zombie = line.prey;
            fairy.getSensing().tick();
            helper.assertTrue(!fairy.getSensing().hasLineOfSight(zombie), "Sensing.hasLineOfSight behind the short grass must answer"
                    + " the convention's false — Sensing asks the mob's hasLineOfSight, where the injection sits (" + FINDING + ")");
            line.raze();
            helper.assertTrue(!fairy.getSensing().hasLineOfSight(zombie), "the per-tick memo: with the grass razed but Sensing not"
                    + " ticked, the cached unseen verdict must stand — the convention sits under Sensing, not in it; its cache lifetime"
                    + " is ENT-S-122's, untouched (" + FINDING + ")");
            fairy.getSensing().tick();
            helper.assertTrue(fairy.getSensing().hasLineOfSight(zombie), "after Sensing.tick() the razed grass must show: seen"
                    + " (" + FINDING + ")");
            line.raise();
            helper.assertTrue(fairy.getSensing().hasLineOfSight(zombie), "the memo the other way: the grass back but Sensing not"
                    + " ticked — the cached seen verdict stands (" + FINDING + ")");
            fairy.getSensing().tick();
            helper.assertTrue(!fairy.getSensing().hasLineOfSight(zombie), "after Sensing.tick() the raised grass must show: unseen"
                    + " (" + FINDING + ")");
        } finally {
            arena.cleanUp();
        }
    }

    // ------------------------------------------------------------------
    // (5) The five feet-helper ports: the ENT-S-118 mapping row, the helper driven by reflection
    // ------------------------------------------------------------------

    private static void feetHelperRow(GameTestHelper helper, Supplier<? extends EntityType<? extends Mob>> type, String method, String orig) {
        Arena arena = new Arena(helper);
        try {
            Mob hunter = arena.frozen(type.get(), Vec3.atBottomCenterOf(HUNTER_POS));
            double floorY = helper.absolutePos(HUNTER_POS).getY();
            helper.assertTrue(hunter.getY() == floorY, "precondition: the " + name(hunter) + " stands exactly on rel y 1.0 (abs " + floorY
                    + "), so the helper's ray starts at rel y 1.75 and crosses the parapet column x 27..28 at rel y 1.14..1.05 — saw "
                    + hunter.getY() + " (" + FINDING + " test geometry)");
            arena.place(PARAPET_POS, Blocks.SHORT_GRASS);
            BlockState state = helper.getBlockState(PARAPET_POS);
            BlockPos abs = helper.absolutePos(PARAPET_POS);
            helper.assertTrue(state.getCollisionShape(helper.getLevel(), abs).isEmpty() && !state.getShape(helper.getLevel(), abs).isEmpty(),
                    "precondition: the parapet (" + state + ") has no collision shape and a non-empty selection shape, so it tells a"
                            + " selection-bounds ray from a collider ray (" + FINDING + " test setup)");
            Vec3 from = new Vec3(hunter.getX(), hunter.getY() + FEET_EYE, hunter.getZ());
            Vec3 to = helper.absoluteVec(FEET_END);
            BlockHitResult outline = helper.getLevel().clip(new ClipContext(from, to, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, hunter));
            helper.assertTrue(outline.getType() == HitResult.Type.BLOCK && outline.getBlockPos().equals(abs),
                    "precondition: the selection-bounds ray from (20.5, 1.75, 24.5) to (28.5, 1.0, 24.5) must stop on the parapet at "
                            + PARAPET_POS + " — saw " + describeHit(helper, outline) + " (" + FINDING + " test geometry)");
            BlockHitResult collider = helper.getLevel().clip(new ClipContext(from, to, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, hunter));
            helper.assertTrue(collider.getType() == HitResult.Type.MISS, "precondition: the collider ray along the same line must miss"
                    + " — the grass has no collision box and the ray ends on the floor's top face — saw " + describeHit(helper, collider)
                    + " (" + FINDING + " test geometry)");
            String where = name(hunter) + "." + method;
            helper.assertTrue(!invokeFeetHelper(hunter, method, to), where + "(the floor 8 blocks east) with the short-grass parapet: orig "
                    + orig + " — rayTraceBlocks(start, end, false), every collidable block on its selection bounds, no liquid stop —"
                    + " stopped on the grass; the port helper must clip OUTLINE (the ENT-S-089 mapping), not COLLIDER (" + FINDING + ")");
            arena.raze(PARAPET_POS);
            helper.assertTrue(invokeFeetHelper(hunter, method, to), "control: with the parapet razed " + where + " must answer true — the"
                    + " grass, not the rest of the ray, refused it (" + FINDING + ")");
        } finally {
            arena.cleanUp();
        }
    }

    // ------------------------------------------------------------------
    // Fixtures
    // ------------------------------------------------------------------

    /** One row's spawns and blocks: discarded, and razed in reverse order of placing, in the finally. */
    private static final class Arena {
        private final GameTestHelper helper;
        private final List<Entity> spawned = new ArrayList<>();
        private final List<BlockPos> placed = new ArrayList<>();

        Arena(GameTestHelper helper) {
            this.helper = helper;
        }

        void place(BlockPos pos, Block block) {
            this.helper.setBlock(pos, block);
            if (!this.placed.contains(pos)) {
                this.placed.add(pos);
            }
        }

        void raze(BlockPos pos) {
            this.helper.setBlock(pos, Blocks.AIR);
        }

        /** Frozen on the floor: goals stripped, noAi, persistence set (the SightStepParityTests idiom). */
        Mob frozen(EntityType<? extends Mob> type, Vec3 rel) {
            Mob mob = this.helper.spawnWithNoFreeWill(type, rel);
            mob.setNoAi(true);
            mob.setPersistenceRequired();
            this.spawned.add(mob);
            return mob;
        }

        /** Frozen prey with 1000 HP, so nothing incidental kills it. */
        Mob prey(EntityType<? extends Mob> type, Vec3 rel) {
            Mob prey = frozen(type, rel);
            prey.getAttribute(Attributes.MAX_HEALTH).setBaseValue(PREY_HEALTH);
            prey.setHealth(PREY_HEALTH);
            return prey;
        }

        void discard(Entity entity) {
            if (entity != null && !entity.isRemoved()) {
                entity.discard();
            }
        }

        void cleanUp() {
            for (Entity entity : this.spawned) {
                discard(entity);
            }
            for (int i = this.placed.size() - 1; i >= 0; i--) {
                raze(this.placed.get(i));
            }
        }
    }

    /**
     * The eye-line arena: the occluder on its dirt support, then hunter and prey spawned 8 blocks apart on the floor, on
     * the z of the occluder's actual selection box (offset plants shift it by up to 0.25; the offset is seeded by the
     * block's absolute column, so the same block re-raised at the same column takes the same box).
     */
    private static final class EyeLine {
        private final GameTestHelper helper;
        private final Arena arena;
        private final Block occluder;
        private final String what;
        private final double z;
        Mob hunter;
        Mob prey;

        EyeLine(GameTestHelper helper, Arena arena, Block occluder, String what) {
            this.helper = helper;
            this.arena = arena;
            this.occluder = occluder;
            this.what = what;
            arena.place(SUPPORT_POS, Blocks.DIRT);
            arena.place(OCCLUDER_POS, occluder);
            BlockState state = helper.getBlockState(OCCLUDER_POS);
            BlockPos abs = helper.absolutePos(OCCLUDER_POS);
            helper.assertTrue(state.is(occluder), "precondition: the " + what + " stands at " + OCCLUDER_POS + " on its dirt — saw "
                    + state + " (" + FINDING + " test setup)");
            helper.assertTrue(state.getCollisionShape(helper.getLevel(), abs).isEmpty() && !state.getShape(helper.getLevel(), abs).isEmpty(),
                    "precondition: the " + what + " (" + state + ") has no collision shape and a non-empty selection shape, so it tells"
                            + " a selection-bounds ray from a collider ray (" + FINDING + " test setup)");
            AABB box = state.getShape(helper.getLevel(), abs).bounds();
            this.z = OCCLUDER_POS.getZ() + (box.minZ + box.maxZ) / 2.0;
        }

        void spawn(EntityType<? extends Mob> hunterType, EntityType<? extends Mob> preyType) {
            this.hunter = this.arena.frozen(hunterType, new Vec3(HUNTER_POS.getX() + 0.5, HUNTER_POS.getY(), this.z));
            this.prey = this.arena.prey(preyType, new Vec3(PREY_POS.getX() + 0.5, PREY_POS.getY(), this.z));
        }

        void discardPair() {
            this.arena.discard(this.hunter);
            this.arena.discard(this.prey);
        }

        /** The geometry behind every verdict: the eye line stops on the occluder's selection box and on nothing's collision box. */
        void assertOnSelectionBoxOnly() {
            BlockPos abs = this.helper.absolutePos(OCCLUDER_POS);
            BlockHitResult outline = clipEyes(this.helper, this.hunter, this.prey, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE);
            this.helper.assertTrue(outline.getType() == HitResult.Type.BLOCK && outline.getBlockPos().equals(abs),
                    "precondition: the selection-bounds ray from the " + name(this.hunter) + "'s eye (" + eye(this.hunter) + ") to the "
                            + name(this.prey) + "'s (" + eye(this.prey) + ") must stop on the " + this.what + " at " + OCCLUDER_POS + " — saw "
                            + describeHit(this.helper, outline) + " (" + FINDING + " test geometry)");
            BlockHitResult collider = clipEyes(this.helper, this.hunter, this.prey, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE);
            this.helper.assertTrue(collider.getType() == HitResult.Type.MISS, "precondition: the collider ray along the same eye line"
                    + " must miss — the " + this.what + " has no collision box and the dirt under it sits below the line — saw "
                    + describeHit(this.helper, collider) + " (" + FINDING + " test geometry)");
        }

        void raze() {
            this.arena.raze(OCCLUDER_POS);
        }

        void raise() {
            this.arena.place(OCCLUDER_POS, this.occluder);
        }

        void flood() {
            this.arena.place(OCCLUDER_POS, Blocks.WATER);
        }
    }

    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------

    private static BlockHitResult clipEyes(GameTestHelper helper, LivingEntity hunter, Entity target, ClipContext.Block block,
                                           ClipContext.Fluid fluid) {
        Vec3 eye = new Vec3(hunter.getX(), hunter.getEyeY(), hunter.getZ());
        Vec3 targetEye = new Vec3(target.getX(), target.getEyeY(), target.getZ());
        return helper.getLevel().clip(new ClipContext(eye, targetEye, block, fluid, hunter));
    }

    private static String describeHit(GameTestHelper helper, BlockHitResult hit) {
        return hit.getType() == HitResult.Type.MISS ? "MISS"
                : "BLOCK at rel " + hit.getBlockPos().subtract(helper.absolutePos(BlockPos.ZERO)) + " (" + helper.getLevel().getBlockState(hit.getBlockPos()) + ")";
    }

    private static String name(Entity entity) {
        return entity.getClass().getSimpleName();
    }

    private static String eye(LivingEntity entity) {
        return String.format("%.2f above its feet", entity.getEyeHeight());
    }

    private static String describe(Entity entity) {
        return entity == null ? "null" : name(entity) + "#" + entity.getId();
    }

    /** A private no-arg method of the hunter's own class, by reflection (the SightStepParityTests idiom). */
    private static Object invoke(Object target, String methodName) {
        String where = target.getClass().getSimpleName() + "." + methodName;
        try {
            Method method = target.getClass().getDeclaredMethod(methodName);
            method.setAccessible(true);
            return method.invoke(target);
        } catch (InvocationTargetException exception) {
            throw new IllegalStateException(where + " threw", exception.getCause());
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("cannot invoke " + where, exception);
        }
    }

    /** The feet helper — {@code canSeeSpot(double, double, double)} or {@code canSeeTarget(double, double, double)} — on absolute coordinates. */
    private static boolean invokeFeetHelper(Mob hunter, String methodName, Vec3 to) {
        String where = hunter.getClass().getSimpleName() + "." + methodName;
        try {
            Method method = hunter.getClass().getDeclaredMethod(methodName, double.class, double.class, double.class);
            method.setAccessible(true);
            return (Boolean) method.invoke(hunter, to.x, to.y, to.z);
        } catch (InvocationTargetException exception) {
            throw new IllegalStateException(where + " threw", exception.getCause());
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("cannot invoke " + where, exception);
        }
    }
}
