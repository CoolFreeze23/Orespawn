# Harness slice — i050 / i127 root causes and default-batch isolation (2026-09-04)

Presented for the owner's ruling under the harness-semantics rule ("Harness slice for i050 and i127, scoped to root
causes and test isolation — mock-player placement, cross-test leakage, TEST-003 order sensitivity. No retries or
widened waits as fixes. Present findings before changes."). **No harness change from this report is applied**; the
only harness change in the tree is the separately approved i165 ticket-centre fix. Read-only investigation by one
lane over the working tree at 7021b5a (+ the uncommitted i165 fix, + the untracked ENT-S-116 tests), the 37
`*.suite.log` runs on record, and the in-repo NeoForm decompile (cited "NF:").

## 0. Facts everything rests on (proven)

- **F0.1 Grid.** One continuous 8-per-row grid for the whole run, never cleared between batches
  (`new StructureGridSpawner(blockpos, 8, false)`, NF gametest/framework/GameTestServer.java:245); column pitch =
  x-size + 5, row pitch = row max z-size + 6 (StructureGridSpawner.java:51-58). Templates: `empty` 8³, `empty_large`
  48×16×48, `empty_tall` 48×34×48. Every row on record had a 48-deep template, so the row pitch is always 54 — every
  failing "failed at" z-offset is a multiple of 54. The origin is random per run (GameTestServer.java:242).
- **F0.2 Buckets.** `groupingBy(batchName)` into a HashMap (NF GameTestBatchFactory.java:18), partition 50 (:15, :26).
  Batches preceding `defaultBatch` in HashMap order total 218 tests in the 585-test layout, 215 in the 582 layout, 150
  in the 517 layout.
- **F0.3 Registration order.** NeoForge puts `clazz.getDeclaredMethods()` of every `@GameTestHolder` class into a
  `HashSet<Method>` (NF net/neoforged/neoforge/gametest/GameTestHooks.java:46, :78) and registers in HashSet iteration
  order (:58 → GameTestRegistry.java:23/:58). `Method.hashCode()` = `className.hashCode() ^ methodName.hashCode()`;
  1097 declared methods (337 lambdas) → table capacity 2048. A model of this order reproduces every failing position
  on record: i050 = default index 45 (wave1e x-offset 211 = 3·53 + 4·13, dz 32·54), i127 = index 78 (wave1e col 0 /
  dz 1998; wave1b dx 225 / dz 1944), i165 = index 86 (wave1b dx 145; wave1d col 0 / dz 2052), s115_30 = index 29 of
  its batch, diag1's i127 = index 78 with the two isolated batches ahead.
- **F0.4 Start timing.** A body runs 20 ticks after every chunk intersecting its structure is entity-ticking (NF
  GameTestInfo.java:36, :83-90, :104; ServerLevel.java:1710); a chunk reaches ENTITY_TICKING only after its neighbour
  ring is FULL (NF ChunkMap.java:386), so a structure bordered by wilderness starts later than an interior one. Tests
  tick in list order (GameTestTicker.java:39), after the level tick. The GameTestServer never sleeps
  (GameTestServer.java:213-214): a far build stalls one tick for seconds, then hundreds of ticks run per second — log
  wall-clock spacing is not tick spacing.
- **F0.5 Encasing.** Barrier walls one block outside each structure, roof at maxY + 1 (NF StructureUtils.java:131-148);
  the walls start at bounds.minY, one block above the structure-block layer, which `clearSpaceForStructure` leaves as
  air (:171-181). Nothing crosses between templates above minY; entities up to one block tall (Acid 0.25, items, orbs)
  could pass under the walls at the structure-block layer (not implicated here).

## 1. `i050_vortex_no_launch_drag_pull` — "was 0.0" (wave1e only)

**Proven:** the exact 0.0 means the push never ran: `golem.setDeltaMovement(0,0,0)`
(src/gametest/java/danger/orespawn/gametest/EntityLogicTestsB.java:374) immediately precedes `vortex.ai()` (:375),
and the pull branch (src/main/java/danger/orespawn/entity/EntityVortex.java:230-237) fires unconditionally once
`currentPullTarget()` (:312-323) returns a target within distSq 81 with `windedCooldownTicks == 0` (only `hurt()` on
the vortex sets it, :283-291; never hurt). So `findSomethingToAttack()` (:325-331) returned null. Its exits:
`PLAY_NICELY.get()` → null (:329); no LivingEntity in the vortex box; `isSuitableTarget` rejecting the golem
(:343-357). The last two are excluded:
- *Section-visibility race — refuted.* `golem.moveTo(gpos)` (:372) targets the exact spawn position; `onMove` does
  nothing unless the section key changes (NF PersistentEntitySectionManager.java:400-419); the golem's section was
  created TICKING because the body cannot run before all intersecting chunks are entity-ticking (GameTestInfo.java:104)
  and `addEntity` takes the chunk visibility (:78-95). The vortex box (16/10/16, dims 2×4, ModEntities.java:311-314)
  spans rel x 7.5..41.5 inside the 48-wide template, so no foreign entity can out-rank the golem in
  `GenericTargetSorter`.
- *isSuitableTarget — refuted.* IronGolem is not in `MyUtils.isIgnoreable` (util/MyUtils.java:39-52);
  `hasLineOfSight` (:346 → NF LivingEntity.java:3033) clips COLLIDER eye-to-eye over the `floor()` stone; not a player;
  not an excluded class.
- *Random rolls — refuted.* The retarget loop (:181-200) and the 1-in-8 melee (:239-242) do not gate the pull.
- *Vortex not in the level — irrelevant.* `ai()` needs only `level()`, `random`, position and bounding box (set by
  `moveTo`) and `Sensing`; the query is against the level where the golem is (detached by design,
  EntityLogicTestsB.java:100-101).

**Hypothesised (the only remaining exit):** `PLAY_NICELY` was TRUE at i050's body. The flip-site sweep finds exactly
one default-batch test holding `PLAY_NICELY = true` across ticks: `leaf_monster_prey_allowlist_play_nicely`
(src/gametest/java/danger/orespawn/gametest/EntityLogicTestsA.java:1442-1508) — `set(true)` at the third LeafMonster
bite (:1486), restored 150 ticks later inside the same `onEachTick` (:1505-1506), no finally. The other default-batch
writers (i053 :701/:783, i083 :1190-1202, i092 :1481/:1510) are synchronous set / restore-in-finally. Both tests are in
**defaultBatch:0** (leaf_monster index 2, i050 index 45).

**Log evidence (wave1e):** `defaultBatch:0` starts 09:44:31; the only progress line before the failure is 09:44:39
(45 of 50 started, 5 not); i050 is synchronous, so it was among the 5 not yet started, and it failed at 09:44:53,
before tick N+20's line (09:44:54). Why late: in the 585 layout i050 is global 263 = row 32 **col 7**, the row's last
cell, with wilderness on three sides → the chunk ring must generate first (F0.4). In the 582 layout i050 is row 32
col 4 (interior) → starts with leaf_monster → passes (wave1/b/c); interior in the 517 layouts too. Record: 1 failure
in 3 edge-layout runs, 0 in about 34 interior-layout runs. Rate check: LeafMonster bite p ≈ 0.053/tick
(entity/EntityLeafMonster.java:126-133); P(3 bites ≤ k ticks) ≈ 2% (k = 10) to 10% (k = 20).

**Proposed fix (rank 2; HIGH for the hazard class, MEDIUM-HIGH as the wave1e cause):** move
`leaf_monster_prey_allowlist_play_nicely` to its own batch (precedent ConfigGateTests.java:697 `boss017` /
`playNicelyIsolation`, whose javadoc :690-696 names "the Vortex drag-pull test" as the victim of exactly this
window), or rewrite its phase 3 as a synchronous set → driven `ai()` loop → restore-in-finally. It is also unrestored on
its phase-3 failure path (:1502-1504), which would pacify every PlayNicely-gated hunter for the rest of the run — and
since ENT-S-115 gated 38 hunters, every multi-tick hunt test in bucket 0 is exposed, not only i050. Add
`playNicely=` + `PLAY_NICELY.get()` and `gameTime=` to i050's messages (the bug003 idiom, CrashReproTests.java:125-130);
do not pin the flag false inside i050 (it would hide the leak from the others). Before: fails whenever its start is
delayed past leaf_monster's third bite (layout-dependent). After: no default-batch test can observe a foreign `true`.
**Diagnostic:** messages above plus gameTime logging at EntityLogicTestsA.java:1486/:1506 — a failure with
`playNicely=true` inside the window proves it; a throwaway build with `setupTicks = 40` on i050 predicts ≈35%
failures with `playNicely=true` in any layout, and none with leaf_monster isolated.

## 2. `i127_tower_centre_rooms_spawn_jumpy_bug` — "Did not expect acid to exist" (diag1, wave1b, wave1e)

**Proven from source:** the TrooperBug summons a live Spit Bug. `EntityTrooperBug.customServerAiStep`
(src/main/java/danger/orespawn/entity/EntityTrooperBug.java:220-246) runs every tick; on the 1-in-5 tick (:231) with a
live target, `nextInt(30) == 1` (:234) creates an `EntitySpitBug` (:235) at the bug↔target midpoint ± 4 (:237-241),
`addFreshEntity` (:242) — full AI, non-persistent. The test hands it a live target every tick
(src/gametest/java/danger/orespawn/gametest/StructureTestsA.java:1137-1140 `bug.setTarget(cow)`), so the summon rolls
at 1/150 per tick while the cow lives. The minion carries `SpitBugAcidAttackGoal` (entity/EntitySpitBug.java:86); its
scan (:260-299) accepts the cow, and the goal fires `new Acid(this.mob.level(), this.mob)`
(entity/ai/SpitBugAcidAttackGoal.java:72, `addFreshEntity` :85) on 1-in-5 cadence ticks once the cow is beyond reach
3.45 and within 20 blocks, after a 1-in-7 burst start, 8 rounds. Any live Acid inside the bounds fails
`assertEntityNotPresent` (NF GameTestHelper.java:585-590). The summon exists since f558369 (2026-08-11); the test
predates it (e5e8674, 2026-08-10). The bug's own goal table (:85-95) has no spit — the bug spawns the thing that
spits.

**Why intermittent:** the cow (10 HP) dies at the first landed swing (ATK 20) once the trooper is in reach, but the
test's own `bug.jumpFromGround()` (:1127) puts the bug about 8 blocks up, out of reach for about 30 ticks → the cow
lives about 45 ticks → P(summon) ≈ 26%; the minion then needs acquisition and a burst start with the cow beyond 3.45
before the swing lands → ≈3-8% per run. Record: 3/37 runs ≈ 8%, in three different grid cells → no layout
dependence.

**External sources excluded:** i065's acids (EntityLogicTestsB.java:1092-1100) drop inside i065's own template and
every acid discards on any hit or at 200 ticks; i148's lair (StructureTestsB.java:343-352) is in-template and its three
SpitBug spawners need a `players()` member within 16 blocks (NF BaseSpawner.java:48, :57-61) — they sit at the
template centre, ≥ 29 blocks from any neighbour's edge; DSB dungeons are far, without tickets; no test uses ItemAcid
or dispensers; the barrier boxes stop projectiles above minY. Order is only SECONDARY: the minion is non-persistent,
so `Mob.checkDespawn` (NF Mob.java:745-760, distance 128) discards it on its first tick if the nearest `players()`
member is more than 128 blocks away — leon_ducky's tempt player (index 87, 60-100 blocks from i127 in every layout on
record) keeps it alive, as does "no player at all". **Hypothesised:** that the logged acids were the minion's (the
message carries no owner).

**Proposed fix (rank 1, HIGH, no loosening):** (1) structural pin — `bug.goalSelector.getAvailableGoals()` contains no
`SpitBugAcidAttackGoal` / ranged goal (EntityTrooperBug.java:85-95); (2) scope the 200-tick negative to the trooper —
each tick, for every ACID in bounds assert `((Projectile) acid).getOwner() != bug`, and discard every
`ENTITY_SPIT_BUG` in bounds each tick (optionally counted as a positive pin of orig TrooperBug.java:441-443);
(3) keep the cow alive for the whole window (max health 1000, EntityLogicTestsA.java:111-114 idiom). Before: about
5-10% intrinsic flake, unaffected by isolation. After: deterministic; fails only if the trooper itself owns an acid.
**Diagnostic:** on failure report each ACID's owner, the SpitBug count in bounds, `cow.isAlive()` / health and the tick
(prediction: owner = EntitySpitBug, one SpitBug present, cow alive); a deterministic repro replaces the trooper's
`random` (KrakenHoldReleaseTests.java:161 idiom) returning 0 for bound 5 and 1 for bound 30 on the first AI tick, with
the cow at max health 1000 six blocks from the midpoint → the current assertion fails every run within about 40 ticks.

## 3. Isolation classes across the default batch (146 tests)

**3(a) Mock players.** Every `makeMockServerPlayerInLevel` logs in at the world spawn, CREATIVE by default (NF
GameTestServer.java:85; the mock's `isCreative()` returns true in every mode, GameTestHelper.java:302), and joins
`players()` (:308); all seven default-batch sites move the player in the same body, none is left at the spawn across a
tick:
- bug003 (bucket 2 / index 134): keeper CREATIVE at rel (27.5, 2, 21.5) (CrashReproTests.java:117-119), removed at tick
  220 first statement (:122); farPlayer 300 blocks east for one synchronous slice, finally (:133-143).
- boyfriend_tempt_panic_door (2/118): sp CREATIVE at rel (23, 2, 16) (EntityLogicTestsA.java:765-767); removed on dwell
  ≥ 60 (:784) or only on success (:829-832) → **stays for the rest of the run on failure or timeout**.
- chipmunk (1/50): survival, finally (:992-1024). dragon (0/47): survival, finally (:1100-1184). leon_ducky (1/87): owner
  survival, finally (:1544-1604); tempt sp CREATIVE at rel (28, 2, 26) (:1611-1614), removed on dwell ≥ 60 (:1622) or
  only on success (:1627-1630) → **stays on failure**.
- Plain `makeMockPlayer` Players added via `addFreshEntity` are not in `players()` but visible to entity scans:
  big_bertha victim (EntityLogicTestsA.java:135-139, discarded tick 36, not on failure), CoreStatTests b3 victim
  (:188-193), bug006 (synchronous).
- Visibility: targeting scans need line of sight and barriers block `clip` — none; spawner activation needs a player
  within 16 blocks across the 5-block gap — none (the in-template spawners sit ≥ 10 blocks from their edges and the
  players at 16-28); despawn: any `players()` member anywhere is "nearest" for `Mob.checkDespawn` of every
  non-persistent mob (`helper.spawn` sets persistence, GameTestHelper.java:127) — bug003's rats (fixed) and i127's
  minion (§2) are the exposed ones.

**3(b) Cross-test leakage.** Multi-tick global windows: leaf_monster PLAY_NICELY (§1); big_bertha toggles
BIG_BERTHA_PVP at ticks 22/30/33/38/41, restored only in the tick-44 callback (EntityLogicTestsA.java:339-406), not on
failure (no other reader today). Never restored: item064 `checkVeinRatio` leaves LESS_ORE = true
(ConfigGateTests.java:159-165). Synchronous set / restore-in-finally (safe): item058 (ConfigGateTests.java:99-133),
wgen042 (:381-421), wgen064 (:512-569), spider_driver (:595-606), c8_spawn_flags (SpawnGateTests.java:84-115),
c8_kraken_revenge + RULE_DOMOBLOOT (:156-190), i007 (StructureTestsA.java:354-418), i053 PLAY_NICELY + setDayTime
(EntityLogicTestsB.java:697-786), i083 (:1190-1204), i092 (:1481-1510), alien / sea_viper EASY→NORMAL
(EntityLogicTestsA.java:517-538, :1928-1947). Shared `level.random`: `LegacyDungeonPiece.buildNow` draws layouts from
the level RNG (world/structure/LegacyDungeonPiece.java:613-618) → layout-count assertions are order-sensitive; the
dsb_item020 detonation of 2026-09-03 (DsbOutcomeTests.java:598-605) fits (hypothesised, no failing log on record).
Static state: `MobzillaSpawnTracker` only in an own-batch class; `Entity.random` replacements are per instance.
Entities: `succeed()` discards within bounds.inflate(1) (NF GameTestInfo.java:235-240), `fail()` discards nothing
(:244), the grid is never reused → failed tests' mobs stay, walled; far FORCED tickets are removed in finally /
sequence paths; structure tickets are dropped at batch end (GameTestRunner.java:103).

**3(c) TEST-003 and bucket boundaries.** Documented order-sensitive tests: boss005/boss012 (flag window + 40-block
scan; isolated), bug003 (despawn distance; keeper), dsb_item020 (level RNG; unfixed), i164/i165 (FORCED ticket vs
checked cell + promotion wait; i165 fixed in the working tree, StructureTestsC.java p+10), i127 (intrinsic summon,
despawn-modulated), i050 (leaf_monster window). Correction to the audit's model: the in-batch order is the
HashSet<Method> order over ALL declared methods of the holder classes (F0.3) — adding an own-batch test does not
reshuffle the default batch; adding, removing or renaming a default-batch method (or its class) inserts at its hash
slot and shifts every later test by one (the tests at 49→50 and 99→100 cross a boundary); crossing 1536 declared
methods (capacity 4096; 1097 now) reshuffles everything. Separately, any change in an earlier batch's count moves
every default-batch GRID cell (582→585 moved i050 from col 4 to col 7) — the geometric channel behind §1. Boundaries
now: bucket 0 = 0..49 (`cryolophosaurus_proactive_hunt` … `i083_hitbox_size_table`); bucket 1 = 50..99
(`chipmunk_apple_tame_dead_bush_release` … `spider_hangout_village_i164`); bucket 2 = 100..145
(`i138_inca_pyramid_content` … `i142_monster_island_content`). Key members: bucket 0 leaf_monster 2, i065 27, i050 45,
dragon 47; bucket 1 chipmunk 50, dsb_item020 61, i127 78, i165 86, leon_ducky 87, i164 99; bucket 2 i148 108,
boyfriend 118, bug003 134.

## 4. Proposed fixes ranked by confidence (none applied)

1. **i127** — structural no-ranged-goal pin, owner-scoped acid negative, per-tick minion cull, cow kept alive — HIGH
   (the producer is in-box and reproducible on demand).
2. **leaf_monster → own batch** (or a synchronous phase 3); flag + gameTime in i050's messages — HIGH for the class,
   MEDIUM-HIGH as the wave1e cause pending the diagnostic.
3. **boyfriend / leon tempt players** removed on every path (or a players-only batch with bug003) — MEDIUM (leaks only
   on failure, then re-arms the despawn hazard).
4. **big_bertha PVP window / item064 LESS_ORE** restored in a finally or isolated — LOW impact, TEST-003 shape.
5. **`buildNow(level, origin, type, RandomSource)` seam** for the layout-count assertions — MEDIUM (a main-code
   overload; explains the dsb_item020 detonation).

Tests found unsafe beside others as written: leaf_monster_prey_allowlist_play_nicely, big_bertha_shockwave_damage_and_pvp,
boyfriend_tempt_panic_door, leon_ducky_tame_untame_tempt, bug003_rat_ai_ticks_and_despawns (as a player source),
i127_tower_centre_rooms_spawn_jumpy_bug (a despawn-sensitive minion until fixed), dsb_item020_towers_maze_rookery and
the other buildNow count asserts, item064_less_ore_vein_ratios.

Lane artefacts (data only) under the session scratchpad `harness_slice/`: `tests.json` (every test with its resolved
batch), `flip_sites.txt` (every config / difficulty / daytime / player site mapped to test and batch),
`classmethods.json`, `order_model.py` + `default_order.txt` (the registration-order model and the resulting
default-batch order with bucket membership).
