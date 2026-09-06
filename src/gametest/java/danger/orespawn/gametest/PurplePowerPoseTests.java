package danger.orespawn.gametest;

import danger.orespawn.ModEntities;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.PurplePower;
import danger.orespawn.entity.client.FaceOrder;
import danger.orespawn.entity.client.GeoReplacementDescriptor;
import danger.orespawn.entity.client.ModelPurplePower;
import danger.orespawn.entity.client.PurplePowerGeoReplacement;
import danger.orespawn.entity.pose.PurplePowerPose;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestGenerator;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestFunction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.block.Rotation;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.loading.json.raw.Model;
import software.bernie.geckolib.loading.json.typeadapter.KeyFramesAdapter;
import software.bernie.geckolib.loading.object.BakedModelFactory;
import software.bernie.geckolib.loading.object.GeometryTree;

/**
 * ENT-S-146 (the classic PurplePower model transcribed from 1.7.10, 2026-09-06): what the dedicated
 * game-test server can pin of the fix. The model, the rolls, the accumulating rotations and the
 * render state are client-side and proven by the Slice 4 parity harness (the server strips the
 * client model classes, the 4b finding); the server pins the seams the model reads:
 * <ul>
 * <li>row 01 - {@link PurplePowerPose#getLevelRandom()} on a live {@link PurplePower} IS the level's
 *     random ({@code Level.getRandom()}, orig ModelPurplePower.java:57 {@code p.worldObj.rand}) and
 *     is NOT the entity's own ({@code Entity.getRandom()}, orig PurplePower.java:155 {@code this.rand}),
 *     the source the model would have got had the interface reused the inherited name;</li>
 * <li>row 02 - {@link PurplePowerPose#roll} (orig :57 / :66 / :75 {@code nextFloat() * 360.0f}), the one
 *     source both renderers read: three rolls from a seeded source equal three
 *     {@code nextFloat() * 360} of an equally seeded source, lie in [0, 360), and leave the source in
 *     step (the next int of both sources agrees) - exactly one float per roll;</li>
 * <li>row 03 - {@link FaceOrder#apply}, the production within-cube face-order pass a translucent rig needs,
 *     on a synthetic one-cube rig baked through GeckoLib's own loader and factory: the bake comes out
 *     in GeckoLib's WEST, EAST, NORTH, SOUTH, UP, DOWN quad order; applying the classic order of a
 *     mirrored ModelPart cube (UP, DOWN, EAST, NORTH, WEST, SOUTH in GeckoLib's labels) permutes the
 *     quads into it, idempotently; the seam policy answers an absent key with ABSENT (silent unless
 *     required, then logged once per resource), and each wrong shape (a bone the rig lacks, a missing
 *     cube-bearing bone, a wrong cube count, a list that is not a permutation) with
 *     WRONG_KEY_FALLBACK and the quads left exactly as found;</li>
 * <li>row 04 - the GeckoLib descriptor's render-state hooks on a live orb answer the classic model's constants
 *     ({@code renderColor} = {@code ModelPurplePower.COLOR} = {@code FastColor.ARGB32.colorFromFloat(0.55, 0.75,
 *     0.75, 0.75)}, the bytes (191, 191, 191, 140); {@code fullBright}; {@code cubeFaceOrderRequired}; the classic
 *     renderer's light level equals the seam's {@code FULL_BRIGHT_LEVEL}); {@code renderType} is the harness's
 *     (a client type) and is not touched here.</li>
 * </ul>
 * A {@link GameTestGenerator} over four synchronous rows in the batch {@code purplePowerPose}
 * (TEST-003: the spawned mob is discarded in a finally; row 03's synthetic resources carry a per-run
 * stamp because {@code FaceOrder}'s once-per-resource log map is JVM-wide). Frozen mobs spawn with
 * their feet on the floor (rel y 0, harness note F0.7).
 */
@GameTestHolder(OreSpawnMod.MOD_ID)
@PrefixGameTestTemplate(false)
public class PurplePowerPoseTests {
    private static final String BATCH = "purplePowerPose";
    private static final String FINDING = "ENT-S-146";
    /** Generated TestFunctions bypass the holder's template prefixing, so the template is named in full. */
    private static final String EMPTY_LARGE = OreSpawnMod.MOD_ID + ":empty_large";
    private static final int TIMEOUT_TICKS = 100;
    private static final BlockPos ORB_POS = new BlockPos(20, 0, 20);
    /** GeckoLib 4.8.4 BakedModelFactory.buildQuads: the order a fresh bake's quads come in (offsets 20-130). */
    private static final List<Direction> GECKOLIB_ORDER = List.of(
            Direction.WEST, Direction.EAST, Direction.NORTH, Direction.SOUTH, Direction.UP, Direction.DOWN);
    /** The classic ModelPart.Cube order (DOWN, UP, WEST, NORTH, EAST, SOUTH) of a MIRRORED cube in GeckoLib's labels. */
    private static final List<Direction> CLASSIC_MIRRORED_ORDER = List.of(
            Direction.UP, Direction.DOWN, Direction.EAST, Direction.NORTH, Direction.WEST, Direction.SOUTH);

    private record Row(int index, String name, Consumer<GameTestHelper> body) {
        String testName() {
            return String.format("purplepowerposetests.ent_s_146_%02d_%s", this.index, this.name);
        }
    }

    private static List<Row> rows() {
        return List.of(
                new Row(1, "pose_random_is_the_level_random", PurplePowerPoseTests::poseRandomIsTheLevelRandom),
                new Row(2, "roll_is_one_float_times_360", PurplePowerPoseTests::rollIsOneFloatTimes360),
                new Row(3, "face_order_permutes_a_bake_into_the_classic_order", PurplePowerPoseTests::faceOrderPermutesABake),
                new Row(4, "descriptor_hooks_are_the_classic_constants", PurplePowerPoseTests::descriptorHooksAreTheClassicConstants));
    }

    @GameTestGenerator
    public Collection<TestFunction> purplePowerPoseRows() {
        List<TestFunction> functions = new ArrayList<>();
        for (Row row : rows()) {
            functions.add(new TestFunction(BATCH, row.testName(), EMPTY_LARGE, Rotation.NONE, TIMEOUT_TICKS, 0L, true,
                    row.body()));
        }
        return functions;
    }

    // ------------------------------------------------------------------
    // Row 1: the pose interface hands the model the LEVEL's random, not the entity's
    // ------------------------------------------------------------------

    private static void poseRandomIsTheLevelRandom(GameTestHelper helper) {
        PurplePower orb = null;
        try {
            orb = (PurplePower) spawnFrozen(helper, ModEntities.PURPLE_POWER.get(), ORB_POS);
            PurplePowerPose pose = orb;
            RandomSource levelRandom = helper.getLevel().getRandom();
            helper.assertTrue(pose.getLevelRandom() == levelRandom, FINDING
                    + ": PurplePowerPose.getLevelRandom() must be the level's random source (orig ModelPurplePower.java:57 "
                    + "p.worldObj.rand) -- expected the ServerLevel's RandomSource instance, actual " + pose.getLevelRandom());
            helper.assertTrue(pose.getLevelRandom() != orb.getRandom(), FINDING
                    + ": PurplePowerPose.getLevelRandom() must not be the entity's own random (orig PurplePower.java:155 this.rand "
                    + "drives the flight targets) -- the two sources are the same instance");
            helper.assertTrue(orb.getRandom() != levelRandom, FINDING
                    + ": precondition -- Entity.getRandom() is the entity's own source, distinct from the level's");
            helper.succeed();
        } finally {
            discardQuietly(orb);
        }
    }

    // ------------------------------------------------------------------
    // Row 2: one roll = one nextFloat() * 360, on the shared static
    // ------------------------------------------------------------------

    private static void rollIsOneFloatTimes360(GameTestHelper helper) {
        for (long seed : new long[] {1L, 12345L, 2026L, 777L}) {
            RandomSource rolled = RandomSource.create(seed);
            RandomSource reference = RandomSource.create(seed);
            for (int frame = 0; frame < 3; frame++) {
                float roll = PurplePowerPose.roll(rolled);
                float expected = reference.nextFloat() * 360.0F;
                helper.assertTrue(roll == expected, FINDING + ": roll " + frame + " of seed " + seed
                        + " must be nextFloat() * 360.0f (orig ModelPurplePower.java:57) -- expected " + expected + ", actual " + roll);
                helper.assertTrue(roll >= 0.0F && roll < 360.0F, FINDING + ": roll " + frame + " of seed " + seed
                        + " must lie in [0, 360) -- actual " + roll);
            }
            int next = rolled.nextInt(1 << 20);
            int expectedNext = reference.nextInt(1 << 20);
            helper.assertTrue(next == expectedNext, FINDING + ": three rolls of seed " + seed
                    + " must consume exactly three floats (the sources fall out of step otherwise) -- expected the next int "
                    + expectedNext + ", actual " + next);
        }
        helper.succeed();
    }

    // ------------------------------------------------------------------
    // Row 3: FaceOrder.apply on a GeckoLib bake, and the seam's fallback policy
    // ------------------------------------------------------------------

    private static void faceOrderPermutesABake(GameTestHelper helper) {
        String run = Long.toHexString(System.nanoTime());
        ResourceLocation geo = ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID,
                "gametest/ent_s_146_" + run + ".geo.json");
        BakedGeoModel baked = bake();
        Map<String, List<List<Direction>>> before = FaceOrder.faceOrders(baked);
        helper.assertTrue(before.equals(Map.of("spoke", List.of(GECKOLIB_ORDER))), FINDING
                + ": a fresh GeckoLib bake must carry its quads in buildQuads order -- expected "
                + Map.of("spoke", List.of(GECKOLIB_ORDER)) + ", actual " + before);

        // Absent: silent unless required; required logs once per resource and leaves the quads.
        FaceOrder.Decision absent = FaceOrder.applyOrFallback(baked, Map.of(), geo, false);
        helper.assertTrue(absent.outcome() == FaceOrder.Outcome.ABSENT && !absent.logged(), FINDING
                + ": an absent key on an opaque rig must be ABSENT and silent -- actual " + absent);
        FaceOrder.Decision required = FaceOrder.applyOrFallback(baked, Map.of(), geo, true);
        helper.assertTrue(required.outcome() == FaceOrder.Outcome.ABSENT && required.logged(), FINDING
                + ": an absent key on a translucent rig must be ABSENT and logged once -- actual " + required);
        FaceOrder.Decision requiredAgain = FaceOrder.applyOrFallback(baked, Map.of(), geo, true);
        helper.assertTrue(requiredAgain.outcome() == FaceOrder.Outcome.ABSENT && !requiredAgain.logged(), FINDING
                + ": the same absent key on the same resource must not log again -- actual " + requiredAgain);
        helper.assertTrue(FaceOrder.faceOrders(baked).equals(before), FINDING + ": the absent-key fallback must leave the quads as found");

        // Right: the classic mirrored-cube order, applied and idempotent.
        Map<String, List<List<Direction>>> classic = Map.of("spoke", List.of(CLASSIC_MIRRORED_ORDER));
        FaceOrder.Decision applied = FaceOrder.applyOrFallback(baked, classic, geo, true);
        helper.assertTrue(applied.outcome() == FaceOrder.Outcome.APPLIED && !applied.logged(), FINDING
                + ": a right key must be APPLIED and silent -- actual " + applied);
        helper.assertTrue(FaceOrder.faceOrders(baked).equals(classic), FINDING
                + ": after FaceOrder.apply the quads must come in the classic order -- expected " + classic + ", actual "
                + FaceOrder.faceOrders(baked));
        FaceOrder.apply(baked, classic);
        helper.assertTrue(FaceOrder.faceOrders(baked).equals(classic), FINDING + ": applying the order twice must change nothing");

        // Wrong shapes: never a throw through the seam, the quads untouched, logged once per resource.
        List<Map<String, List<List<Direction>>>> wrong = List.of(
                Map.of("nosuchbone", List.of(CLASSIC_MIRRORED_ORDER)),
                Map.of("spoke", List.of(CLASSIC_MIRRORED_ORDER), "nosuchbone", List.of(CLASSIC_MIRRORED_ORDER)),
                Map.of("spoke", List.of(CLASSIC_MIRRORED_ORDER, CLASSIC_MIRRORED_ORDER)),
                Map.of("spoke", List.of(List.of(Direction.UP, Direction.DOWN, Direction.EAST, Direction.NORTH, Direction.WEST))),
                Map.of("spoke", List.of(List.of(Direction.UP, Direction.UP, Direction.EAST, Direction.NORTH, Direction.WEST, Direction.SOUTH))));
        for (int index = 0; index < wrong.size(); index++) {
            ResourceLocation resource = ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID,
                    "gametest/ent_s_146_" + run + "_wrong" + index + ".geo.json");
            FaceOrder.Decision first = FaceOrder.applyOrFallback(baked, wrong.get(index), resource, true);
            helper.assertTrue(first.outcome() == FaceOrder.Outcome.WRONG_KEY_FALLBACK && first.logged(), FINDING
                    + ": wrong shape " + index + " must be WRONG_KEY_FALLBACK and logged -- actual " + first);
            FaceOrder.Decision second = FaceOrder.applyOrFallback(baked, wrong.get(index), resource, true);
            helper.assertTrue(second.outcome() == FaceOrder.Outcome.WRONG_KEY_FALLBACK && !second.logged(), FINDING
                    + ": wrong shape " + index + " repeated on the same resource must not log again -- actual " + second);
            helper.assertTrue(FaceOrder.faceOrders(baked).equals(classic), FINDING
                    + ": wrong shape " + index + " must leave the quads exactly as found -- actual " + FaceOrder.faceOrders(baked));
            boolean threw = false;
            try {
                FaceOrder.apply(baked, wrong.get(index));
            } catch (IllegalStateException expected) {
                threw = true;
            }
            helper.assertTrue(threw, FINDING + ": the strict FaceOrder.apply must throw on wrong shape " + index
                    + " (the harness takes no fallback)");
        }
        helper.succeed();
    }

    // ------------------------------------------------------------------
    // Row 4: the descriptor's render-state hooks answer the classic constants (what the server can evaluate)
    // ------------------------------------------------------------------

    /**
     * ENT-S-146 (refuter B, D3): the GeckoLib descriptor's {@code renderColor} / {@code fullBright} /
     * {@code cubeFaceOrderRequired} on a live PurplePower, against the classic model's constants.
     * {@code renderType(entity)} is deliberately NOT called: it returns the classic model's
     * {@code Function<ResourceLocation, RenderType>} - a client type held by a client-only class
     * ({@code ModelPurplePower.RENDER_TYPE}; the dedicated server strips the client model classes, the 4b
     * finding) - and is proven by the parity harness instead (the render-state sidecars: the same function
     * object on both sides). {@code ModelPurplePower.COLOR} and {@code LIGHT_LEVEL} are compile-time
     * constants and reach this class inlined, so the model class is never loaded here.
     */
    private static void descriptorHooksAreTheClassicConstants(GameTestHelper helper) {
        PurplePower orb = null;
        try {
            orb = (PurplePower) spawnFrozen(helper, ModEntities.PURPLE_POWER.get(), ORB_POS);
            GeoReplacementDescriptor<PurplePower> descriptor = new PurplePowerGeoReplacement().descriptor();
            int expectedColor = FastColor.ARGB32.colorFromFloat(0.55F, 0.75F, 0.75F, 0.75F);
            helper.assertTrue(ModelPurplePower.COLOR == expectedColor, FINDING
                    + ": ModelPurplePower.COLOR (0x8CBFBFBF) must be FastColor.ARGB32.colorFromFloat(0.55, 0.75, 0.75, 0.75), orig "
                    + "ModelPurplePower.java:55 glColor4f packed -- expected " + Integer.toHexString(expectedColor)
                    + ", actual " + Integer.toHexString(ModelPurplePower.COLOR));
            helper.assertTrue(FastColor.ARGB32.alpha(expectedColor) == 140 && FastColor.ARGB32.red(expectedColor) == 191
                    && FastColor.ARGB32.green(expectedColor) == 191 && FastColor.ARGB32.blue(expectedColor) == 191, FINDING
                    + ": the packed colour must carry the bytes (191, 191, 191, 140) -- actual "
                    + Integer.toHexString(expectedColor));
            helper.assertTrue(descriptor.renderColor(orb, 0.0F) == ModelPurplePower.COLOR, FINDING
                    + ": the descriptor's renderColor must be the classic model's COLOR -- actual "
                    + Integer.toHexString(descriptor.renderColor(orb, 0.0F)));
            helper.assertTrue(descriptor.fullBright(orb), FINDING
                    + ": the descriptor's fullBright must be true (orig :56 lightmap 240 / 240)");
            helper.assertTrue(ModelPurplePower.LIGHT_LEVEL == GeoReplacementDescriptor.FULL_BRIGHT_LEVEL
                    && GeoReplacementDescriptor.FULL_BRIGHT_LEVEL == 15, FINDING
                    + ": the classic renderer's light level and the seam's FULL_BRIGHT_LEVEL must both be 15 -- actual "
                    + ModelPurplePower.LIGHT_LEVEL + " / " + GeoReplacementDescriptor.FULL_BRIGHT_LEVEL);
            helper.assertTrue(descriptor.cubeFaceOrderRequired(), FINDING
                    + ": a translucent rig's descriptor must require the within-cube face-order key");
            helper.succeed();
        } finally {
            discardQuietly(orb);
        }
    }

    /** A one-bone, one-cube rig through GeckoLib's own loader and factory (the DrawOrderFallbackTests idiom). */
    private static BakedGeoModel bake() {
        String json = "{\"format_version\":\"1.12.0\",\"minecraft:geometry\":[{\"description\":{"
                + "\"identifier\":\"geometry.orespawn.gametest.ent_s_146\",\"texture_width\":64,\"texture_height\":32},"
                + "\"bones\":[{\"name\":\"spoke\",\"pivot\":[0,24,0],\"cubes\":[{\"origin\":[-2,23.5,-0.5],\"size\":[4,1,1],"
                + "\"uv\":{\"west\":{\"uv\":[1,13],\"uv_size\":[-1,1]},\"east\":{\"uv\":[6,13],\"uv_size\":[-1,1]},"
                + "\"north\":{\"uv\":[5,13],\"uv_size\":[-4,1]},\"south\":{\"uv\":[10,13],\"uv_size\":[-4,1]},"
                + "\"up\":{\"uv\":[5,12],\"uv_size\":[-4,1]},\"down\":{\"uv\":[9,13],\"uv_size\":[-4,-1]}}}]}]}]}";
        Model model = KeyFramesAdapter.GEO_GSON.fromJson(json, Model.class);
        return BakedModelFactory.DEFAULT_FACTORY.constructGeoModel(GeometryTree.fromModel(model));
    }

    /** A frozen mob with its feet ON the floor (rel y 0, F0.7): no AI, no gravity, persistent, on the ground. */
    private static Mob spawnFrozen(GameTestHelper helper, EntityType<? extends Mob> type, BlockPos pos) {
        Mob mob = helper.spawn(type, pos);
        mob.setNoAi(true);
        mob.setPersistenceRequired();
        mob.setNoGravity(true);
        mob.setOnGround(true);
        return mob;
    }

    private static void discardQuietly(Entity entity) {
        if (entity != null && !entity.isRemoved()) {
            entity.discard();
        }
    }
}
