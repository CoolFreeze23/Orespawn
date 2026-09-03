package danger.orespawn.gametest;

import danger.orespawn.ModEntities;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.TheQueen;
import de.dertoaster.multihitboxlib.api.IMHLibFieldAccessor;
import de.dertoaster.multihitboxlib.util.RenderTickGate;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/**
 * BUG-044 (ruled 2026-09-04, "per-entity stamp with gametests for the hitch and
 * two-Queen cases"): the once-per-game-tick gate of MHLib's client bone
 * collection. The vendored collector kept ONE {@code currentTick} per render
 * layer (one layer per renderer instance), collected while
 * {@code currentTick == entity.tickCount || currentTick < 0} and advanced it to
 * {@code tickCount + 1} only on equality. A client frame during which the entity
 * ticked twice left the stamp one behind for good (equality never held again and
 * {@code tickCount} only grows), and two Queens drawn by the same renderer shared
 * the stamp, so it followed whichever entity last matched and the other never
 * collected. The fix (design after MoreHitboxes' {@code GeckoLibMobMixin}, MIT):
 * the stamp is a field on the ENTITY
 * ({@code IMHLibFieldAccessor#_mhlibAccess_getRenderTickStamp}, -1 until the
 * first collecting pass) and the rule is {@link RenderTickGate#shouldCollect}
 * = {@code stamp < tickCount}, advanced to the collected tick.
 *
 * <p>What a server-side gametest can see: the pure gate ({@link RenderTickGate},
 * no client imports) and the per-entity storage on real {@link TheQueen}
 * instances (the LivingEntity mixin applies on the server too). The render layer
 * itself cannot run here (it needs a client); it is driven headlessly by the
 * {@code src/g1tool} {@code QueenPartPlacementProbe}'s render-tick-gate checks,
 * which fail the {@code check} task.</p>
 *
 * <p>Batch (TEST-003): every test is synchronous within one tick (spawn, assert,
 * discard in a {@code finally}) and the class declares its own batch,
 * {@code renderTickGate}. Template {@code empty_large} is 48x16x48; two frozen
 * Queens at POS_A and POS_B is the QueenPlayNicelyDimsTests precedent.</p>
 */
@GameTestHolder(OreSpawnMod.MOD_ID)
@PrefixGameTestTemplate(false)
public class RenderTickGateTests {

    private static final BlockPos POS_A = new BlockPos(24, 8, 24);
    private static final BlockPos POS_B = new BlockPos(12, 8, 12);

    private static TheQueen spawnFrozen(GameTestHelper helper, BlockPos pos) {
        TheQueen queen = helper.spawnWithNoFreeWill(ModEntities.THE_QUEEN.get(), pos);
        queen.setNoAi(true);
        queen.setPersistenceRequired();
        return queen;
    }

    private static void discard(TheQueen... queens) {
        for (TheQueen queen : queens) {
            if (queen != null) queen.discard();
        }
    }

    /** The MHLib field accessor the LivingEntity mixin adds to every Queen. */
    private static IMHLibFieldAccessor<?> stamps(GameTestHelper helper, TheQueen queen, String which) {
        Object asObject = queen;
        helper.assertTrue(asObject instanceof IMHLibFieldAccessor<?>,
                which + " is not an IMHLibFieldAccessor (MHLib LivingEntity mixin missing) (BUG-044)");
        return (IMHLibFieldAccessor<?>) asObject;
    }

    /**
     * One simulated render pass of the gate for an entity, exactly what the collector's
     * onPreRender(Entity) / onPostRender(Entity) do with the stamp: returns whether the pass
     * collected and advances the entity's stamp to its tick when it did.
     */
    private static boolean simulatePass(IMHLibFieldAccessor<?> access, int tickCount) {
        boolean collecting = RenderTickGate.shouldCollect(access._mhlibAccess_getRenderTickStamp(), tickCount);
        if (collecting) {
            access._mhlibAccess_setRenderTickStamp(RenderTickGate.advance(tickCount));
        }
        return collecting;
    }

    /**
     * (a) The hitch. The stamp sits at 10 and the next rendered frame sees tickCount
     * 12 (the entity ticked twice between frames): the pass must collect and advance
     * the stamp to 12; a second frame in the same tick must not collect; tick 13
     * collects again; the very first pass (stamp -1) collects. The old rule
     * ({@code == tickCount}, advance to {@code tickCount + 1}) fails the hitch: it
     * would sit at 11 against 12 and every later tick, forever.
     */
    @GameTest(template = "empty_large", batch = "renderTickGate")
    public void bug044_hitch_stamp_behind_by_two_still_collects(GameTestHelper helper) {
        helper.assertTrue(RenderTickGate.shouldCollect(RenderTickGate.UNSTAMPED, 0),
                "first pass: an unstamped entity (-1) must collect at tickCount 0 (BUG-044)");
        helper.assertTrue(RenderTickGate.shouldCollect(10, 12),
                "hitch: stamp 10 with tickCount 12 (two ticks in one frame) must collect (BUG-044)");
        int stamp = RenderTickGate.advance(12);
        helper.assertTrue(stamp == 12, "advance(12) must stamp the collected tick, got " + stamp + " (BUG-044)");
        helper.assertTrue(!RenderTickGate.shouldCollect(stamp, 12),
                "a second frame in the same tick (stamp 12, tickCount 12) must not collect (BUG-044)");
        helper.assertTrue(RenderTickGate.shouldCollect(stamp, 13),
                "the next tick (stamp 12, tickCount 13) must collect again (BUG-044)");
        // The equality rule under the same hitch: advanced to 11, tickCount jumps to 12, wedged; '<' recovers.
        int equalityStamp = 10 + 1;
        helper.assertTrue(equalityStamp != 12 && RenderTickGate.shouldCollect(equalityStamp, 12),
                "the old equality gate would sit at 11 against tickCount 12 forever; '<' must recover (BUG-044)");
        helper.succeed();
    }

    /**
     * (b) Two Queens, independent fields: both start at -1 and writing one leaves the
     * other untouched, in both directions.
     */
    @GameTest(template = "empty_large", batch = "renderTickGate")
    public void bug044_two_queens_have_independent_stamps(GameTestHelper helper) {
        TheQueen a = null;
        TheQueen b = null;
        try {
            a = spawnFrozen(helper, POS_A);
            b = spawnFrozen(helper, POS_B);
            IMHLibFieldAccessor<?> sa = stamps(helper, a, "Queen A");
            IMHLibFieldAccessor<?> sb = stamps(helper, b, "Queen B");
            helper.assertTrue(sa._mhlibAccess_getRenderTickStamp() == RenderTickGate.UNSTAMPED
                            && sb._mhlibAccess_getRenderTickStamp() == RenderTickGate.UNSTAMPED,
                    "fresh Queens must start unstamped (-1), got A=" + sa._mhlibAccess_getRenderTickStamp()
                            + " B=" + sb._mhlibAccess_getRenderTickStamp() + " (BUG-044)");
            sa._mhlibAccess_setRenderTickStamp(7);
            helper.assertTrue(sa._mhlibAccess_getRenderTickStamp() == 7
                            && sb._mhlibAccess_getRenderTickStamp() == RenderTickGate.UNSTAMPED,
                    "stamping Queen A (7) must leave Queen B unstamped (-1), got A="
                            + sa._mhlibAccess_getRenderTickStamp() + " B=" + sb._mhlibAccess_getRenderTickStamp() + " (BUG-044)");
            sb._mhlibAccess_setRenderTickStamp(9);
            helper.assertTrue(sa._mhlibAccess_getRenderTickStamp() == 7 && sb._mhlibAccess_getRenderTickStamp() == 9,
                    "stamping Queen B (9) must leave Queen A at 7, got A="
                            + sa._mhlibAccess_getRenderTickStamp() + " B=" + sb._mhlibAccess_getRenderTickStamp() + " (BUG-044)");
        } finally {
            discard(a, b);
        }
        helper.succeed();
    }

    /**
     * (b') Two Queens, the gate decides per entity when their tick counts differ: A
     * at tick 10 and B at tick 20 each collect on their own first pass and stamp
     * their own tick; the same frame again collects for neither; A ticking to 11
     * while B stays at 20 collects for A only. The shared per-renderer stamp could
     * not do this: after A's pass (11) B's tick 20 never matched, and after B's
     * (21) A's never did.
     */
    @GameTest(template = "empty_large", batch = "renderTickGate")
    public void bug044_two_queens_gate_decides_per_entity(GameTestHelper helper) {
        TheQueen a = null;
        TheQueen b = null;
        try {
            a = spawnFrozen(helper, POS_A);
            b = spawnFrozen(helper, POS_B);
            IMHLibFieldAccessor<?> sa = stamps(helper, a, "Queen A");
            IMHLibFieldAccessor<?> sb = stamps(helper, b, "Queen B");
            a.tickCount = 10;
            b.tickCount = 20;
            helper.assertTrue(simulatePass(sa, a.tickCount) && simulatePass(sb, b.tickCount),
                    "first frame: both Queens must collect (A tick 10, B tick 20) (BUG-044)");
            helper.assertTrue(sa._mhlibAccess_getRenderTickStamp() == 10 && sb._mhlibAccess_getRenderTickStamp() == 20,
                    "each Queen must carry its own tick as the stamp, got A=" + sa._mhlibAccess_getRenderTickStamp()
                            + " B=" + sb._mhlibAccess_getRenderTickStamp() + " (BUG-044)");
            helper.assertTrue(!simulatePass(sa, a.tickCount) && !simulatePass(sb, b.tickCount),
                    "a second frame in the same ticks must collect for neither Queen (BUG-044)");
            a.tickCount = 11;
            helper.assertTrue(simulatePass(sa, a.tickCount) && !simulatePass(sb, b.tickCount),
                    "A ticked to 11, B still at 20: A must collect and B must not (BUG-044)");
            helper.assertTrue(sa._mhlibAccess_getRenderTickStamp() == 11 && sb._mhlibAccess_getRenderTickStamp() == 20,
                    "after A's pass: A=11 and B untouched at 20, got A=" + sa._mhlibAccess_getRenderTickStamp()
                            + " B=" + sb._mhlibAccess_getRenderTickStamp() + " (BUG-044)");
        } finally {
            discard(a, b);
        }
        helper.succeed();
    }
}
