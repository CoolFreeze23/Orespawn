package de.dertoaster.multihitboxlib.util;

// Portions derived from MoreHitboxes by DarkPred (https://github.com/DarkPred/MoreHitboxes, commit 88899b3), MIT License — see LICENSE-MoreHitboxes.txt

/**
 * BUG-044 (ruled 2026-09-04): the once-per-game-tick gate of the client bone collection, keyed on a
 * PER-ENTITY render-tick stamp -- the last {@code Entity.tickCount} a collecting render pass ran for
 * that entity, {@link #UNSTAMPED} until the first one ({@code IMHLibFieldAccessor
 * _mhlibAccess_getRenderTickStamp}). Design after MoreHitboxes' {@code GeckoLibMobMixin}
 * ({@code renderTick < tickCount} to collect, {@code renderTick = tickCount} afterwards).
 *
 * <p>Why {@code stamp < tickCount} and not {@code stamp == tickCount}: the stamp records the last
 * tick that WAS collected, and the render loop is not guaranteed to observe every tick value. A
 * client frame during which the entity ticked twice (a hitch) takes {@code tickCount} from N to
 * N + 2, while a stamp that is advanced on equality sits at N + 1 -- equality never holds again
 * and, since {@code tickCount} only grows, the gate is wedged for the rest of the session (the
 * former per-renderer {@code currentTick} of the collector layer). {@code <} collects whenever the
 * entity has ticked at least once since the last collecting pass (a skipped tick is caught up on
 * the next frame), never twice within one tick (after a pass the stamp equals the tick), and needs
 * no special case for the initial {@link #UNSTAMPED} value. Keeping the stamp on the entity rather
 * than on the render layer gives two entities drawn by the same renderer independent gates instead
 * of one that follows whichever entity last matched and starves the other.</p>
 *
 * <p>Pure and common-side (no client imports): pinned by the server-side gametests
 * ({@code RenderTickGateTests}, batch {@code renderTickGate}) and driven through the real collector
 * layer by the headless {@code QueenPartPlacementProbe}.</p>
 */
public final class RenderTickGate {

	/** The stamp of an entity no collecting pass has run for yet; below every possible {@code tickCount}. */
	public static final int UNSTAMPED = -1;

	private RenderTickGate() {
	}

	/**
	 * @param stamp     the entity's render-tick stamp: the last collected tick, or {@link #UNSTAMPED}
	 * @param tickCount the entity's current {@code tickCount}
	 * @return whether the render pass about to run collects bone information for the entity
	 */
	public static boolean shouldCollect(int stamp, int tickCount) {
		return stamp < tickCount;
	}

	/**
	 * The stamp to store after a collecting pass: the tick it collected, so that further frames of
	 * the same tick are skipped and the next tick (or any later one) collects again.
	 */
	public static int advance(int tickCount) {
		return tickCount;
	}
}
