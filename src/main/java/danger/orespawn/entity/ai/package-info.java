/**
 * OreSpawn boss-AI goal classes for NeoForge 1.21.1.
 *
 * <h2>Porting context</h2>
 *
 * <p>The original 1.7.10 OreSpawn bosses ran all their AI inline inside
 * {@code Entity.func_70030_z_()} (the legacy per-tick hook). Flight
 * pathing, target acquisition, attack selection, and phase-transition
 * dialogue were all interleaved in one ~600-line method per entity
 * ({@code TheKing.java:515-1005}, {@code TheQueen.java:443-970} in
 * {@code reference_1_7_10_source/sources/danger/orespawn/}). This pattern
 * was forced by MC 1.7's lack of a split
 * {@code goalSelector}/{@code targetSelector}, which didn't arrive until
 * MC 1.8.</p>
 *
 * <p>The 1.12.2 ConquerantFix fork strips the bosses entirely, but
 * demonstrates the modern replacement pattern via its
 * {@code MyEntityAI*} hierarchy ({@code reference_1_12_2_source/sources/danger/orespawn/util/ai/})
 * — every concern (wandering, target acquisition, attack-on-collide,
 * panic) is its own deobfuscated {@code EntityAIBase} subclass.</p>
 *
 * <p>NeoForge 1.21.1 uses the same dual-selector system. This package
 * contains:
 * <ul>
 *   <li><b>KingEndGameGoal</b>: handles the "Prepare to die!" dialogue
 *       cutscene and the transition into the enraged phase. Takes
 *       priority 0 and owns {@code MOVE|LOOK|JUMP} so nothing else can
 *       run during the cutscene.</li>
 *   <li><b>KingFlightGoal</b>: picks flight targets, runs the smooth
 *       motion lerp toward the current target, and updates yaw. Owns
 *       {@code MOVE}.</li>
 *   <li><b>KingAttackGoal</b>: target acquisition (throttled to every
 *       10 ticks to avoid main-thread AABB scans), melee + jump damage,
 *       and the three ranged attack streams (fireball / thunder / ice).
 *       Owns {@code LOOK}.</li>
 *   <li><b>QueenFlightGoal</b>: like KingFlightGoal but with Queen's
 *       extra "follow The King when happy" rule.</li>
 *   <li><b>QueenAttackGoal</b>: like KingAttackGoal with Queen's
 *       fireball + lightning streams (no ice stream).</li>
 *   <li><b>QueenMoodGoal</b>: when {@code attackLevel > 1000}, either
 *       spawn a PurplePower barrage (mood=mad) or terraform the
 *       terrain and spawn butterflies (mood=happy).</li>
 * </ul>
 *
 * <h2>Threading</h2>
 *
 * <p>All goals run on the server tick thread via {@code GoalSelector.tick()}.
 * None of the methods here should be called from a world-gen thread or
 * an async task. Entity-list queries ({@code getEntitiesOfClass}) are
 * bounded to every 10 ticks inside the attack goals to keep the
 * per-boss cost sub-millisecond on populated servers.</p>
 *
 * <h2>Entity state sharing</h2>
 *
 * <p>Goals do not own any boss state — they read and write fields on the
 * parent entity via public getter/setter pairs (see
 * {@code TheKing#getFlightTarget()} / {@code setFlightTarget(...)} and
 * friends). The entity stays the single source of truth for NBT
 * persistence and boss-bar updates; goals are stateless behavioural
 * strategies that compose on top.</p>
 */
package danger.orespawn.entity.ai;
