package danger.orespawn.gametest;

import danger.orespawn.ModEntities;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.EntityVortex;
import java.lang.reflect.Field;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.PressurePlateBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/**
 * ENT-S-089: the eight Vortex divergences restored to orig Vortex.java. Pins
 * the dims (item 1), the empty {@code doPush} (2), the persistence gate on
 * the daytime discard (4), the pressure-plate override (5) and the fixed
 * voice pitch (6). Items 3 (eye-line probe on wander candidates), 7 (particle
 * tangent, client-only) and 8 (retarget threshold and write-before-validate)
 * are code-reviewed against the cited lines; the wander loop is RNG-driven
 * and has no deterministic server-side observable to pin.
 */
@GameTestHolder(OreSpawnMod.MOD_ID)
@PrefixGameTestTemplate(false)
public class VortexParityTests {

    @GameTest(template = "empty")
    public static void v089a_vortex_dims_pin(GameTestHelper helper) {
        EntityVortex vortex = helper.spawnWithNoFreeWill(ModEntities.ENTITY_VORTEX.get(), new BlockPos(3, 2, 3));
        vortex.setNoAi(true);
        vortex.setPersistenceRequired();
        helper.assertTrue(vortex.getBbWidth() == 2.0f && vortex.getBbHeight() == 4.0f,
                "vortex dims drifted from orig Vortex.java:50 setSize(2.0f, 4.0f) (ENT-S-089 item 1): "
                        + vortex.getBbWidth() + "x" + vortex.getBbHeight());
        helper.succeed();
    }

    /**
     * Two overlapping Vortexes: with orig's empty collideWithEntity neither shoves
     * the other, so both stay put; vanilla doPush would separate them within a tick
     * (Entity.push needs a horizontal offset of at least 0.01, hence the 0.5 offset).
     */
    @GameTest(template = "empty")
    public static void v089b_vortex_never_pushes(GameTestHelper helper) {
        // spawnWithNoFreeWill only strips goals; the Vortex flies from customServerAiStep, so noAi is explicit.
        EntityVortex a = helper.spawnWithNoFreeWill(ModEntities.ENTITY_VORTEX.get(), new Vec3(3.0, 2.0, 3.0));
        EntityVortex b = helper.spawnWithNoFreeWill(ModEntities.ENTITY_VORTEX.get(), new Vec3(3.5, 2.0, 3.0));
        a.setNoAi(true);
        b.setNoAi(true);
        a.setPersistenceRequired();
        b.setPersistenceRequired();
        double ax = a.getX();
        double bx = b.getX();
        StringBuilder trace = new StringBuilder();
        int[] ticks = {0};
        helper.onEachTick(() -> {
            if (ticks[0]++ < 12) {
                trace.append(String.format("t%d a=(%.3f,%.3f,%.3f) da=%s hc=%b noAi=%b | b=(%.3f,%.3f,%.3f) db=%s; ",
                        ticks[0], a.getX(), a.getY(), a.getZ(), a.getDeltaMovement(), a.horizontalCollision, a.isNoAi(),
                        b.getX(), b.getY(), b.getZ(), b.getDeltaMovement()));
            }
        });
        helper.runAfterDelay(10, () -> {
            helper.assertTrue(Math.abs(a.getX() - ax) < 1.0e-6 && Math.abs(b.getX() - bx) < 1.0e-6
                            && Math.abs(a.getDeltaMovement().x) < 1.0e-9 && Math.abs(b.getDeltaMovement().x) < 1.0e-9,
                    "Vortexes must not push each other (orig Vortex.java:98-99, ENT-S-089 item 2): a moved "
                            + (a.getX() - ax) + ", b moved " + (b.getX() - bx) + " trace: " + trace);
            helper.succeed();
        });
    }

    /**
     * Daytime discard with the roll forced: the persistent Vortex survives (orig
     * Vortex.java:131-133 returns before the discard), the non-persistent control
     * is discarded on its first server tick, proving the forced roll reaches the
     * discard path. Runs alone because it sets the world's day time.
     */
    @GameTest(template = "empty", batch = "vortexDaytimeIsolation")
    public static void v089c_persistent_vortex_survives_daytime(GameTestHelper helper) {
        long priorDayTime = helper.getLevel().getDayTime();
        helper.getLevel().setDayTime(6000L);
        // GameTestHelper.spawn marks every mob persistence-required, so the control is added by hand.
        EntityVortex keeper = helper.spawnWithNoFreeWill(ModEntities.ENTITY_VORTEX.get(), new BlockPos(2, 2, 2));
        keeper.setNoAi(true);
        keeper.setPersistenceRequired();
        EntityVortex control = ModEntities.ENTITY_VORTEX.get().create(helper.getLevel());
        helper.assertTrue(control != null, "vortex type must construct");
        Vec3 controlPos = Vec3.atBottomCenterOf(helper.absolutePos(new BlockPos(6, 2, 6)));
        control.moveTo(controlPos.x, controlPos.y, controlPos.z, 0.0F, 0.0F);
        control.setNoAi(true);
        helper.assertTrue(helper.getLevel().addFreshEntity(control), "control vortex must be added to the level");
        helper.assertTrue(!control.isPersistenceRequired(), "control must not be persistence-required");
        forceDiscardRoll(keeper);
        forceDiscardRoll(control);
        helper.runAfterDelay(5, () -> {
            try {
                // Drive the roll explicitly as well: the level ticks the entities, but the assertion
                // must not depend on how many server ticks the harness delivered.
                for (int i = 0; i < 3 && !control.isRemoved(); i++) {
                    control.tick();
                }
                for (int i = 0; i < 3; i++) {
                    keeper.tick();
                }
                String diagnostics = "dayTime=" + helper.getLevel().getDayTime()
                        + " controlRandom=" + control.getRandom().getClass().getSimpleName()
                        + " controlTicks=" + control.tickCount + " controlPersistent=" + control.isPersistenceRequired()
                        + " keeperPersistent=" + keeper.isPersistenceRequired();
                helper.assertTrue(control.isRemoved(),
                        "control Vortex with the forced 1-in-500 roll must be discarded (discard path live); " + diagnostics);
                helper.assertTrue(!keeper.isRemoved() && keeper.isAlive(),
                        "persistence-required Vortex must survive the daytime discard (orig Vortex.java:131-133, ENT-S-089 item 4); " + diagnostics);
                helper.succeed();
            } finally {
                helper.getLevel().setDayTime(priorDayTime);
            }
        });
    }

    @GameTest(template = "empty")
    public static void v089d_vortex_ignores_pressure_plate(GameTestHelper helper) {
        BlockPos platePos = new BlockPos(2, 2, 2);
        helper.setBlock(platePos.below(), Blocks.STONE);
        helper.setBlock(platePos, Blocks.STONE_PRESSURE_PLATE);
        BlockPos controlPlate = new BlockPos(6, 2, 6);
        helper.setBlock(controlPlate.below(), Blocks.STONE);
        helper.setBlock(controlPlate, Blocks.STONE_PRESSURE_PLATE);
        // A noAi mob does not travel (no gravity), so both stand IN the plate block: feet at the plate's
        // y, inside its 4/16-high touch box, exactly where a landed entity would be.
        EntityVortex vortex = helper.spawnWithNoFreeWill(ModEntities.ENTITY_VORTEX.get(), platePos);
        vortex.setNoAi(true);
        vortex.setPersistenceRequired();
        Zombie zombie = helper.spawnWithNoFreeWill(EntityType.ZOMBIE, controlPlate);
        zombie.setNoAi(true);
        StringBuilder trace = new StringBuilder();
        int[] ticks = {0};
        helper.onEachTick(() -> {
            if (ticks[0]++ < 12) {
                trace.append(String.format("t%d zombie=(%.2f,%.3f,%.2f) onGround=%b plate=%s | vortex=(%.2f,%.3f,%.2f) plate=%s; ",
                        ticks[0], zombie.getX(), zombie.getY(), zombie.getZ(), zombie.onGround(),
                        helper.getBlockState(controlPlate), vortex.getX(), vortex.getY(), vortex.getZ(),
                        helper.getBlockState(platePos)));
            }
        });
        helper.runAfterDelay(10, () -> {
            // Also invoke the plate's own entityInside with each entity in its box, as the plate does
            // when an entity moves through it, so the assertion does not hinge on landing timing.
            press(helper, controlPlate, zombie);
            press(helper, platePos, vortex);
            helper.assertTrue(vortex.isIgnoringBlockTriggers(),
                    "Vortex must ignore block triggers (orig Vortex.java:221-223, ENT-S-089 item 5)");
            helper.assertTrue(!powered(helper, platePos),
                    "a Vortex standing on a stone pressure plate must not power it; trace: " + trace);
            helper.assertTrue(zombie.isAlive() && powered(helper, controlPlate),
                    "control: a zombie on the same plate powers it (plate mechanics live); trace: " + trace);
            helper.succeed();
        });
    }

    @GameTest(template = "empty")
    public static void v089e_vortex_voice_pitch_fixed(GameTestHelper helper) {
        EntityVortex vortex = helper.spawnWithNoFreeWill(ModEntities.ENTITY_VORTEX.get(), new BlockPos(3, 2, 3));
        vortex.setNoAi(true);
        vortex.setPersistenceRequired();
        for (int i = 0; i < 8; i++) {
            helper.assertTrue(vortex.getVoicePitch() == 1.0f,
                    "Vortex voice pitch must be exactly 1.0 (orig Vortex.java:78-80, ENT-S-089 item 6), got "
                            + vortex.getVoicePitch());
        }
        helper.succeed();
    }

    /** What the plate does when an entity is inside its box: recompute its signal from the entities present. */
    static void press(GameTestHelper helper, BlockPos plate, Entity entity) {
        BlockPos absolute = helper.absolutePos(plate);
        BlockState state = helper.getLevel().getBlockState(absolute);
        state.entityInside(helper.getLevel(), absolute, entity);
    }

    static boolean powered(GameTestHelper helper, BlockPos pos) {
        BlockState state = helper.getBlockState(pos);
        return state.hasProperty(PressurePlateBlock.POWERED) && state.getValue(PressurePlateBlock.POWERED);
    }

    /** Replaces the entity's random so every {@code nextInt(500)} answers 1: the daytime discard roll hits. */
    static void forceDiscardRoll(Entity entity) {
        RandomSource delegate = RandomSource.create(1234L);
        RandomSource forced = new ForcedRoll(delegate, 500, 1);
        try {
            Field field = Entity.class.getDeclaredField("random");
            field.setAccessible(true);
            field.set(entity, forced);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("cannot replace Entity.random", exception);
        }
    }

    /** A RandomSource that answers a fixed value for one bound and delegates everything else. */
    static final class ForcedRoll implements RandomSource {
        private final RandomSource delegate;
        private final int bound;
        private final int answer;

        ForcedRoll(RandomSource delegate, int bound, int answer) {
            this.delegate = delegate;
            this.bound = bound;
            this.answer = answer;
        }

        @Override
        public RandomSource fork() {
            return new ForcedRoll(this.delegate.fork(), this.bound, this.answer);
        }

        @Override
        public net.minecraft.world.level.levelgen.PositionalRandomFactory forkPositional() {
            return this.delegate.forkPositional();
        }

        @Override
        public void setSeed(long seed) {
            this.delegate.setSeed(seed);
        }

        @Override
        public int nextInt() {
            return this.delegate.nextInt();
        }

        @Override
        public int nextInt(int upper) {
            return upper == this.bound ? this.answer : this.delegate.nextInt(upper);
        }

        @Override
        public long nextLong() {
            return this.delegate.nextLong();
        }

        @Override
        public boolean nextBoolean() {
            return this.delegate.nextBoolean();
        }

        @Override
        public float nextFloat() {
            return this.delegate.nextFloat();
        }

        @Override
        public double nextDouble() {
            return this.delegate.nextDouble();
        }

        @Override
        public double nextGaussian() {
            return this.delegate.nextGaussian();
        }
    }
}
