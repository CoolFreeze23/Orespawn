# Targeting survey — every hunter's target selection, 1.7.10 against the port (2026-09-04)

## 0. Ruling, method, legend

**Owner's ruling (verbatim):** "Targeting survey, read-only lane: compare every hunter's target selection — scan set, filters, gates (PlayNicely, creative, PEACEFUL, allies, ignore screen), tie-breaks — 1.7.10 against the port, one ledger under phase_g_reports. Present the split; I'll rule on batches, not singles, from here."

Recorded as standing item 18 of `phase_g_reports/phase_g_scope_addendum_2026-09-03.md` ("From here the owner rules on batches, not singles") and in `PHASE_G_PROMPT.md:591-593`.

**Method.**

- Four read-only lanes split the species by orig file: S1 Alien–CreepingHorror (15 files), S2 Cryolophosaurus–Kyuubi (20), S3 LeafMonster–Rotator (19) plus the three 1.7.10 AI helper classes (`MyEntityAINearestAttackableTarget`, `MyEntityAITarget`, `MyValentineTarget`), S4 RubberDucky–WaterDragon (25). Lane parts: `<scratchpad>/survey/part_S1.md` … `part_S4.md`; this ledger merges them.
- Port side: the COMMITTED tree at **`6af0a1c`** (`git show HEAD:src/main/java/danger/orespawn/...`), never the working tree, which at survey time carried the ENT-S-108..113 lanes' edits. Port cites are `<Class>.java:N` under `src/main/java/danger/orespawn/entity/` unless a path is given (`util/MyUtils.java`, `entity/ai/...`, `block/RepellentBlock.java`).
- Orig side: `reference_1_7_10_source/sources/danger/orespawn/<File>.java` (CFR decompile, SRG names; `orig <File>.java:N`).
- Vanilla 1.21.1 goal semantics: from the NeoForm decompile. S1, S2 and S4 cite the decompile jar in the gradle cache (`~/.gradle/caches/neoformruntime/intermediate_results/decompile_d62f7f84…_output.jar`, "vanilla `<Class>.java:N`"); S3 cites the in-repo `build/neoform/neoFormJoined1.21.1-20240808.144430/steps/transformSource/transformed/net/minecraft/` tree ("(v) `<Class>.java:N`"). The two numberings differ for the same methods (e.g. `LivingEntity.canAttack` :864-866 versus :899-909); both are kept in §1 where a point is cited from both. The Kraken/Brutalfly `<=` tie-break was verified from 1.7.10 bytecode under ENT-S-105; the other 1.7.10 vanilla AI classes (`EntityAITarget`, `EntityAIHurtByTarget`, `EntityAINearestAttackableTarget`, `EntityMob`, `EntityCreature`) are not in the reference tree and are described from MCP source and the `mc1710/*.javap.txt` dumps in the scratchpad, without SRG-mapped line cites (S1 and S3 say so explicitly).
- Per species a fixed 10-row schema — aspect | 1.7.10 (file:line) | port HEAD (file:line) | status | note — over: scan set · filter order · PlayNicely gate · creative gate · PEACEFUL gate · allies / species exclusions · ignore screen · tie-break / selection rule · target set / release · other. The three non-hunters (CreeperRepellent, KrakenRepellent, StinkBug) carry one N/A row each.
- Rows are reproduced verbatim from the parts and are **not re-judged** here; the four preambles are merged once in §1; the fix lanes' observations (`laneJ_records.md` scope notes, `laneL_records.md` "Observations", `laneM_records.md` "Observations filed on the way") are folded in §3.6 and, where a part had missed one, into the batch tables as clearly labelled observation-sourced entries.

**Status legend.**

| status | meaning |
|---|---|
| MATCH | the same selection behaviour in both trees. Qualified forms — "MATCH (engine, P6)" (S1: an EntityMob/Monster despawned by the engine on Peaceful), "MATCH (effective)" (S3), "MATCH (box)", "MATCH (structure)" — are MATCH |
| DIVERGES | different behaviour with no record covering it — a parity bug by the standing rule (below) |
| RECORDED (record) | the divergence is deliberate and carries a MOD / OPT / BOSS record (§3.4) |
| FIXED (ENT-S-xxx) | restored in HEAD `6af0a1c` by a finding fixed this session (ENT-S-100 / 101 / 105 / 106 / 107) |
| FIX IN FLIGHT (ENT-S-xxx) | covered by ENT-S-108..113 — edited in the working tree at survey time, not in HEAD (§4.12) |
| PORT-ONLY | a port addition with no orig counterpart and no record (§4 batch T9: candidates for a MOD entry or removal) |
| N/A | the aspect does not exist in either tree (or the block is not a hunter) |

**Standing rule.** Any divergence from 1.7.10 that no MOD record covers is a parity bug. It is the rule every ENT-S-108..113 record invokes ("a parity bug by the standing rule" — `AUDIT_FINDINGS.md:7019, :7032, :7041, :7049, :7057, :7065`) and the ENT-S-089 precedent ("all eight parity bugs, no MOD record" — `FIX_LOG.md:4534`). Every DIVERGES row below is therefore a parity bug until the owner rules otherwise on its batch; RECORDED rows are not; PORT-ONLY rows need a ruling either way (record or remove).

**In flight the same day.** ENT-S-108 (living-entity scans for eight hunters and the CaveFisher in part), ENT-S-109 (ten `invulnerable` → `instabuild` sites), ENT-S-110 (Leon: PlayNicely gate and the attackable-non-mob tail), ENT-S-111 (IrukandjiArrow push gate — a projectile, no ledger row), ENT-S-112 (PitchBlack ally exclusions) and ENT-S-113 (Cephadrome PEACEFUL guard and `shouldattack` reset) were ruled "all parity, fixed in classic; generated tests per site where the pattern allows; two refuters on 108, one on each of the rest" (addendum item 17) and were being fixed and gated while this survey ran (lanes J, L, M; the ENT-S-108 lane's `TargetScanParityTests` is in the working tree). Their rows say **FIX IN FLIGHT** (37 rows, listed in §4.12) so that a re-read after the gate can flip them to FIXED; nothing in those rows is counted as a divergence. Fixes already in HEAD (ENT-S-100 / 101 / 105 / 106 / 107) say FIXED (38 rows, §4.11).

**Conventions of this ledger.**

- §2 lists the 82 blocks in orig-file alphabetical order, case-insensitive (`CreeperRepellent` before `CreepingHorror`; `EntityButterfly` / `EntityCannonFodder` under E; `TRex` after `TheQueen`), the three AI helper blocks after the species. Each block heading carries its lane (`— lane S1`) because the parts' shared-fact labels are per-lane (see the label map at the end of §1: S1's "P3" is the revenge goal, S4's "P3" is PEACEFUL).
- Nine rows carry a compound status (§3.5 — e.g. Cephadrome PEACEFUL "FIX IN FLIGHT (ENT-S-113) / DIVERGES", Leon scan set "custom MATCH; vanilla goal DIVERGES"). They are counted ONCE, under DIVERGES, in the §3 tables, and batched by their DIVERGES half; the other half is named in the row and in §3.5.
- The helper block `MyEntityAINearestAttackableTarget` has an allies row marked "DIVERGES (folded into filter order)" that its part did not count as a separate aspect; §3 counts it as a DIVERGES row (221 rows, with the refuter correction below) while the parts' own aspect count is 220 (219 + that correction). Both numbers are given wherever it matters.
- Formatting fixes applied to the parts' text: two bare `||` inside code spans (LeafMonster "other", TheQueen "PlayNicely gate") escaped as `\|\|` so the rows render as table rows. Nothing else in a row was changed, except one status: the spot-check refuter corrected the EnderReaper "target set / release" row from MATCH to DIVERGES (2026-09-04; the row carries the evidence, and every count in §3–§4 includes it).
- No ENT-S numbers are assigned anywhere in this ledger (§4.13).

## 1. Vanilla goal semantics (the four lane preambles, merged once)

Each point names the lane(s) that stated it. Line cites keep each lane's numbering (S1/S2/S4 = decompile jar; S3 = in-repo transformed tree, marked "(v)").

- **V1 — 1.7.10 custom scan shape** (S1 P1; S2; S3; S4 P5). `findSomethingToAttack()` = PlayNicely gate (`OreSpawnMain.PlayNicely != 0 → null`) → `worldObj.getEntitiesWithinAABB(EntityLivingBase.class, boundingBox.expand(dx,dy,dz))` (`func_72872_a` / `func_72314_b`) → `Collections.sort(list, GenericTargetSorter)` → the first entry passing `isSuitableTarget`. The cadence rolls live in `func_70619_bc` (`updateAITasks`), which runs because the hunters return true from `func_70650_aV` (`isAIEnabled`) — S1 lists the site in each of its 14 hunters (Alien :124 … CreepingHorror :80); EnderKnight and EnderReaper have no override and run the legacy loop of V10 instead. AntRobot and SpiderRobot's frontal scan skip the sort (first suitable in raw `getEntitiesWithinAABB` order); the stomp scans hit every suitable entity.
- **V2 — `GenericTargetSorter` and the TF-035 remainder** (S1 P1/P2; S2; S3; S4 P5). Orig `GenericTargetSorter.java:18-35` (S2 cites :18-34): distSq to the hunter, halved for `EntityCreeper`, divided by `height*width` when that product exceeds 1; strict compare, so the stable sort keeps encounter order on ties. Port `entity/ai/GenericTargetSorter.java:32-47` (S4 cites :31-47) reproduces the weighting through `Double.compare`. TF-035 (`FIX_LOG.md:1901-1912`; riders :2046-2049, :2070) swapped the bosses and many hunters to it, "~45 non-boss call sites remain" (`FIX_LOG.md:2046-2049`); where a port hunter still ranks by `Comparator.comparingDouble(this::distanceToSqr)` the row says "plain nearest / plain distance" and DIVERGES (tie-break) — the "TF-035 systemic remainder" (batch T4). The orig helper goals used their own sorters: `MyEntityAINearestAttackableTargetSorter.java:21-31` (creeper halving, no silhouette term), `MyValentineTargetSorter.java:20-24` (plain distSq).
- **V3 — port custom scan shape** (S1 P2; S2; S3; S4 P5). `OreSpawnConfig.PLAY_NICELY.get()` gate → `level().getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(dx,dy,dz))` → `TargetSelection.firstMatch(list, GenericTargetSorter, isSuitableTarget)`. `entity/ai/TargetSelection.firstMatch` (S1: :13-28, :65-84, :102-106; S2: :60-84, :102-106; S3: :47-84; S4: :65-84) is documented and implemented as order-equivalent to stable-sort-then-first-match (index tiebreak = stable-sort tie order, identical predicate call sequence; OPT-021, `AUDIT_FINDINGS.md:5668`, RECORDED neutral). The encounter order itself (1.7.10 chunk entity lists versus 1.21.1 entity sections) is engine-defined and only matters on exact weight ties — not marked per row. The RNG stream (orig `worldObj.rand` / `this.rand` versus port `getRandom()` / `this.random`) has the same bounds and odds, stream only — the ENT-S-093 convention, RECORDED as Kraken KT-C under ENT-S-100 — noted in scan-set rows, never counted.
- **V4 — the shared helpers** (S2; S3; S4 P9; lane J table; lane M O1). `MyUtils.isIgnoreable`: orig `MyUtils.java:117-152` (RockBase, EntityAnt, EntityButterfly, EntityMosquito, Dragonfly, Firefly, Cricket, Cockateil, Termite, Ghost, GhostSkelly, Elevator) = port `util/MyUtils.java:39-52`, same order — MATCH since ENT-S-101 (membership) and ENT-S-106 (the 38 orig call sites: 11 present, 27 restored — 17 private-filter sites, 9 goal predicates, 1 inline in the Urchin). `MyUtils.isAttackableNonMob`: orig `MyUtils.java:77-115` = EntityMob, Mothra, Leon, Dragon, Spyro, `isRoyalty`, GammaMetroid, Cephadrome, WaterDragon, Girlfriend, Boyfriend, EntityVillager, Stinky; port `util/MyUtils.java:54-63` = EnderDragon, Kraken, Godzilla, GodzillaHead, Basilisk, Cephadrome, TheKing, TheQueen — a different list, only Cephadrome common, **no AUDIT / FIX / MOD record** (grep: usage only). It is read at HEAD by Crab :366, EntityMantis :279, EntityMolenoid :289, TheKing :1187, TheQueen :1386 (lane M O1); orig also called it from CaterKiller :556, Hammerhead :248, SeaMonster :510, SeaViper :527, WaterDragon :679 and Leon :422-426 (ENT-S-110 reproduces the orig membership inline in EntityLeon rather than call the port helper). Where a hunter falls through to it after its own Monster / royalty / EnderDragon steps, the effective prey lost in the port is Mothra, Leon, Dragon, Spyro, GammaMetroid, WaterDragon, Girlfriend, Boyfriend, Villager, Stinky; the port's additions (Kraken, Godzilla, GodzillaHead, Basilisk) are Monsters and already prey via the Monster step (S4 P9). `MyUtils.isRoyalty` matches (orig :46-75 = port :9-19: ThePrince, ThePrinceTeen, ThePrinceAdult, ThePrincess, TheKing, KingHead, TheQueen, QueenHead, PurplePower). `MyUtils.isBigBoss` (port util :75-80: Godzilla, GodzillaHead, PitchBlack, Kraken) has no orig counterpart; Godzilla :598 and TheKing :981-990 read it.
- **V5 — the 1.7.10 revenge goal `EntityAIHurtByTarget(this, false)`** (S1 P3; S4 P2; S3). Fires once per new revenge timestamp when `getAITarget()` — the revenge target `EntityLivingBase.attackEntityFrom` stores for any `EntityLivingBase` attacker — passes `EntityAITarget.isSuitableTarget`: alive, `canAttackClass`, no sight check (`shouldCheckSight = false`); `startExecuting` → `setAttackTarget(getAITarget())`; `continueExecuting` reads `getAttackTarget()` only — alive, within `followRange` (16 unless the species sets it; no S4 species does, grep `field_111265_b` empty), `capabilities.disableDamage` players dropped; `resetTask` → `setAttackTarget(null)`; the `false` = no call-for-help. **Lane disagreement, both kept:** S4 P2 reads `shouldExecute` as `isSuitableTarget(target, true)` (includeInvincibles), so a creative attacker is admitted for one pass and dropped by `continueExecuting`; S1 P3 reads the check as refusing `disableDamage` players outright. Neither lane verified 1.7.10 vanilla from bytecode; both trees record the attacker; the one-pass difference is not counted as a divergence by either part. Where neither tree consumes the stored target (Basilisk, CreepingHorror, LeafMonster, Molenoid, Peacock, Rat, Robot1, SpiderDriver, Triffid, Kyuubi, GammaMetroid, …) the goal is inert in both.
- **V6 — the port revenge goal `HurtByTargetGoal(this)`** (S1 P3; S2; S3 VAN-HBT; S4 P2). Vanilla `HurtByTargetGoal.java:33-52, 60-71` (S3: (v) :19, :37-55, :64-74); `TargetGoal.java:37-71, 84-88` (S3: (v) :41-74, :88-91). Fires once per `lastHurtByMobTimestamp` when `lastHurtByMob` (set by `LivingEntity.hurt` for any LivingEntity attacker, vanilla `LivingEntity.java:1139-1145`) passes `TargetingConditions.forCombat().ignoreLineOfSight().ignoreInvisibilityTesting()` — alive and not spectator (`canBeSeenByAnyone`, `LivingEntity.java:876-878`), `canAttack` (`LivingEntity.java:864-866`: a player in PEACEFUL → false, else `canBeSeenAsEnemy` = not invulnerable :872-874; `Player.canBeSeenAsEnemy` adds `!abilities.invulnerable`, `Player.java:903-905`), `canAttackType` (≠ ghast, `Mob.java:244-246`), `isAlliedTo`; the universal-anger rule for players; `toIgnoreDamage` classes skipped. `start` → `setTarget(lastHurtByMob)`, `targetMob` kept, unseen memory 300 ticks; `canContinueToUse` drops on `!canAttack`, beyond FOLLOW_RANGE, or unseen > 300 ticks, and otherwise **re-asserts** `setTarget(targetMob)` whenever the mob's target is null (`TargetGoal.java:39-42, 66`) — 1.7.10's `continueExecuting` had no such re-assert, so a port hunter that nulls its own target gets its last attacker back while that attacker stays valid; `stop` → `setTarget(null)`. `setAlertOthers` ((v) :57-61, :76-115; Robot2 only in this ledger) retargets same-class mobs within FOLLOW_RANGE × 10 that have no target. Net: same role in both trees; the port adds the 300-tick unseen release, the PEACEFUL-player refusal and the re-assert.
- **V7 — vanilla `NearestAttackableTargetGoal<T>(mob, T.class, [randomInterval,] mustSee[, mustReach][, predicate])`** (S1 P4; S2; S3 VAN-NAT; S4 P1). Vanilla `NearestAttackableTargetGoal.java:34-73` (S3: (v) :44-82). Evaluated on the every-other-tick goal pass (`Mob.java:736-750`) with `nextInt(reducedTickDelay(10) = 5) == 0` (`Goal.java:50-52`) ≈ one acquisition attempt per 10 ticks, and only while no target is held. `Player.class` → `getNearestPlayer(conditions, mob, x, eyeY, z)` over ALL players of the level (`EntityGetter.java:125-127, 148-160`; S3: (v) :131-132, :154-171) — no box; nearest by distSq to the mob's eye with strict `<` (first wins ties); any other class → `getEntitiesOfClass(T, bbox.inflate(FOLLOW_RANGE, 4, FOLLOW_RANGE))` (:54-56, :58-69) then the same nearest pick. `start` → `setTarget` (:76-79); release by `TargetGoal.canContinueToUse` — null, `!canAttack`, same team, beyond FOLLOW_RANGE, and with `mustSee` more than `reducedTickDelay(60)` = 30 evaluated ticks (60 game ticks) unseen; `stop` → `setTarget(null)`; `mustReach` path cache (v) :96-119. FOLLOW_RANGE defaults to 16 (`Mob.createMobAttributes`) where the species sets none (Boyfriend, Girlfriend).
- **V8 — `TargetingConditions.forCombat().range(FOLLOW_RANGE).selector(pred)`** (S1 P4; S2; S3 VAN-TC; S4 P1). Vanilla `TargetingConditions.java:60-93` (S3: (v) :60-92), in order: self → `canBeSeenByAnyone` (alive, not spectator; `LivingEntity.java:876-878` / (v) :911-913) → the selector → `attacker.canAttack(target)` (`LivingEntity.java:864-866` / (v) :899-909: a Player is refused in PEACEFUL, else `target.canBeSeenAsEnemy()`; for players `!abilities.invulnerable`, `Player.java:903-905` / (v) :966-968 — CREATIVE sets both `instabuild` and `invulnerable` ((v) `GameType.java:57-61`), so a survival player made invulnerable by other means is also refused) → `canAttackType` (no Ghast, `Mob.java:244-246` / (v) :253-255) → `isAlliedTo` (team; for a `TamableAnimal` the owner, (v) `TamableAnimal.java:218-224`; `TamableAnimal.canAttack` also refuses the owner, (v) :190-192) → distance ≤ max(range × visibilityPercent, 2) — a sphere; sneaking ×0.8, invisibility ×0.7×armor (`LivingEntity.java:834-847`) → `getSensing().hasLineOfSight` when `checkLineOfSight` / `mustSee`.
- **V9 — plain nearest-player scans** (S2; S4 P5; S1 Brutalfly row; ENT-S-105). Port `Level.getNearestPlayer(Entity, double)` (`EntityGetter.java:78-104` / :96-104; S3: (v) :84-99, :102-110): all players of the level with `NO_SPECTATORS`, sphere distSq < d², strict `<` — creative INCLUDED, plain distance from the entity position, no box. Its 1.7.10 counterparts: `World.findNearestEntityWithinAABB` (`func_72857_a`; Brutalfly :215, Mothra :224, Kraken :963) — box-bounded, any mode, replaces on `<=` so the LAST equidistant player wins (bytecode-verified under ENT-S-105, fixed for the Kraken only); `World.getClosestPlayerToEntity` (`ahb.a(sa,D)` → `a(DDDD)`, `mc1710/ahb.javap.txt:8128-8190`; EnderKnight / EnderReaper :65) — every player, no creative or alive check, strict `<` (first wins ties).
- **V10 — the 1.7.10 legacy (non-AI) loop** (S2). `EntityCreature.updateEntityActionState` (`td.bq`, `mc1710/td.javap.txt`): target null → `findPlayerToAttack` (`bR`, :62-73 of the dump) → path; else alive → attack when seen, dead → null (:107-152); then, the same tick, `entityToAttack instanceof EntityPlayerMP && theItemInWorldManager.isCreative()` → null (:155-182) — so a creative player is picked and dropped the same tick, shadowing a farther survival player (the Kraken KT-A pattern). `EntityMob.attackEntityFrom` (`yg.a(ro,F)`, `mc1710/yg.javap.txt:81-116`) sets `entityToAttack` to any attacker that is not self / rider / mount. Applies to EnderKnight and EnderReaper (no `isAIEnabled` override, no task lists); SpiderDriver's legacy `attackEntity` / `findPlayerToAttack` never run (`isAIEnabled` true, S4).
- **V11 — `BugMeleeAttackGoal` / `DinosaurMeleeAttackGoal`** (S2; rows in S1, S3, S4). Port `entity/ai/BugMeleeAttackGoal.java` acquires nothing — it acts on `mob.getTarget()` (:83-96, `canUse` on `getTarget()` alive); `forgetTargetRoll` N > 0 → `setTarget(null)` on `nextInt(N) == 0` EVERY tick the goal runs, ahead of the cadence gate (:123-133). The `Params` / `DinosaurMeleeAttackGoal.Presets` search dims (e.g. CaterKiller 20/8, Nastysaurus 32/8) are recorded but never read by the goal (:116-157).
- **V12 — creative mapping** (S1 P5; S4 P6; lane J; lane L). Orig `capabilities.isCreativeMode` (`field_71075_bZ.field_75098_d`) ↔ port `Abilities.instabuild` — the ENT-S-107 ruling: 1.21.1 `GameType.updatePlayerAbilities` sets `instabuild` for CREATIVE only, `invulnerable` for CREATIVE and SPECTATOR, and either flag can be toggled by hand. Port sites still testing `invulnerable` are the ENT-S-109 class (ten listed sites in flight); the vanilla goals of V6–V8 test `invulnerable` (+ spectator) inherently — the unlisted remainder is batch T8.
- **V13 — PEACEFUL** (S1 P6; S3; S4 P3). For `EntityMob` → `Monster` hunters neither tree gates target selection on difficulty; the engine removes the mob (orig `EntityMob.onUpdate` → `setDead`; port `Mob.checkDespawn`, `Mob.java:703-705` / (v) :745-748, with `Monster.shouldDespawnInPeaceful` :55-57) — rows say "MATCH (engine, P6)" (S1) or "N/A" / "N/A (P3)" (S2–S4). Hunters on other bases carry their own gates in both trees and are compared: EntityLiving → Mob (AntRobot, SpiderRobot, PurplePower), EntityCreature → PathfinderMob (Cephadrome), EntityTameable → TamableAnimal (Dragon, GammaMetroid, Leon, RubberDucky, Spyro, Stinky, ThePrince family, WaterDragon), EntityAnimal → Animal (Dragonfly, Fairy, Frog, Lizard, StinkBug, Island / IslandToo), EntityAmbientCreature → AmbientCreature (EntityButterfly, Mothra). The vanilla goals additionally refuse PLAYER targets in PEACEFUL (`canAttack`) — inert for Monsters (rows say PORT-ONLY, inert).
- **V14 — line of sight** (S1 P7; S4 P4). Orig `getEntitySenses().canSee` (`func_70635_at().func_75522_a`, `EntitySenses`: per-tick cached eye-to-eye block ray, liquids ignored) ↔ port `getSensing().hasLineOfSight` (per-tick cached `LivingEntity.hasLineOfSight`, `LivingEntity.java:2888-2898`: eye-to-eye COLLIDER clip, 128-block cap) — treated as MATCH. Separate orig tests are compared per block: the `canSeeTarget(x,y,z)` rays from eye y+0.75 to the target's FEET (Spyro :436-438, Stinky :317-319, ThePrince :416-418, ThePrincess :404-406), the 10-step `MyCanSee` block-marches (CaterKiller :626-676, Molenoid :344-394, TheKing :965, UltimateSword :198-247), and the facing cones (AntRobot dircheck 0.75 rad, Robot3-5 0.5 rad, SpiderRobot bearing 0.75).
- **V15 — records that make a row RECORDED or MATCH** (S4 P8; S2; S3; S1). RECORDED: MOD-001 (TheQueen deletes the health-tracked victim), MOD-002 (TheKing deletes small Monster attackers; the Queen carries the same rule), MOD-022 (transient combat state — revenge / `rt` — not persisted), MOD-029 (Mothra enlarged root hitbox as the modern-mode default — widens both sweeps), OPT-004 (EntityVortex: one 5-tick target cache), OPT-016 / OPT-026 (King/Queen sort-free scan and containment head-check, order preserved), OPT-021 (sort-then-first-match → `TargetSelection.firstMatch`, neutral), BOSS-017 (PlayNicely consumed by King / Queen / Princess), ENT-S-100 KT-C (rand stream). MATCH because a record restored the orig rule: ENT-K-026 (LurkingTerror list), ENT-K-030 (Mantis chain), ENT-K-058 (Rat friendliness scope), ENT-K-079 (Rotator chain), ENT-K-082 (RubberDucky prey + buddy), ENT-S-002 (Scorpion brain), ENT-S-021 (SpiderRobot filters), ENT-S-074 (WaterDragon — ranged branch ONLY), ENT-S-089 / TF-035 (Vortex), BOSS-024 (ThePrince prey), ITEM-037 (Chainsaw sweep). `MODERNIZATION_NOTES` MOD-003 / 010 / 021 / 024 touch nothing in these filters (MOD-010 is the removed Boyfriend bro-mode gate; MOD-021 the optional wiki mobs — the port's `VampireButterfly` is one of them; MOD-022 / 024 persistence and opt-in notes).
- **V16 — in flight this batch, and in HEAD** (S4 P7; S1; S2; addendum item 17). In flight: ENT-S-108 living scans (CaveFisher — Player and Animal goals, DungeonBeast, EmperorScorpion, HerculesBeetle, Nastysaurus, SpitBug, TRex, TrooperBug, Urchin; Pointysaurus excluded — players-only in orig :246), ENT-S-109 creative → instabuild (Cryolophosaurus :120, EntityBrutalfly :205 strafe and :358 filter, EntityGammaMetroid :216, EntityKyuubi :145, EntityLeafMonster :162, EntityLurkingTerror :248, EntityRat :199, EntityTerribleTerror :163, EntityTriffid :253), ENT-S-110 (EntityLeon filter :391 gate + :422-427 tail), ENT-S-112 (PitchBlack eight exclusions), ENT-S-113 (Cephadrome :516 guard + :567 reset). In HEAD: ENT-S-100 (Kraken KT-A/B1/B2/D/E), ENT-S-101 (ignore-list membership), ENT-S-105 (Kraken `<=`), ENT-S-106 (ignore screen at the 38 sites), ENT-S-107 (EntityLeon :753 and Cephadrome :403 `instabuild`).

**Label map** (the parts' shared-fact labels used inside the rows → the V-points above):

| lane | label | meaning | here |
|---|---|---|---|
| S1 | P1 / P2 | 1.7.10 scan shape + sorter / port scan shape + firstMatch + rand stream | V1, V2 / V3 |
| S1 | P3 | revenge goal, both trees (incl. the port re-assert) | V5, V6 |
| S1 | P4 | vanilla `NearestAttackableTargetGoal` + `TargetingConditions` | V7, V8 |
| S1 | P5 / P6 / P7 | creative mapping / PEACEFUL for EntityMob-Monster / line of sight | V12 / V13 / V14 |
| S2 | "shared refs" | ignore list, sorter, TargetSelection, `isAttackableNonMob`, vanilla goals, legacy loop, BugMeleeAttackGoal, rand stream | V2–V4, V6–V11 |
| S3 | VAN-NAT / VAN-TC / VAN-HBT | vanilla nearest-attackable goal / targeting conditions / hurt-by goal ((v) cites) | V7 / V8 / V6 |
| S4 | P1 / P2 / P3 / P4 | vanilla nearest-attackable goal / revenge goal / PEACEFUL / sight | V7–V8 / V5–V6 / V13 / V14 |
| S4 | P5 / P6 / P7 / P8 / P9 | tie-break / creative mapping / in flight / records / `isAttackableNonMob` | V2–V3, V9 / V12 / V16 / V15 / V4 |

## 2. Ledger — every species block, verbatim from the lane parts

Orig-file alphabetical order (case-insensitive); the three AI helper blocks last. Each heading carries the lane whose labels (P-points, VAN-*) its rows use — resolve them with the label map at the end of §1. Rows are the parts' text; the only edits are the two escaped `||` and the one refuter status correction (EnderReaper, target set / release) noted in §0.

### Alien — orig `Alien.java`, port `Alien.java` — lane S1
| aspect | 1.7.10 (file:line) | port HEAD (file:line) | status | note |
|---|---|---|---|---|
| scan set | `EntityLivingBase`, box 12x4x12 from the hunter's bounding box (Alien.java:370); in `func_70619_bc` on `worldObj.rand.nextInt(8)==0` (:314) | `LivingEntity`, `getBoundingBox().inflate(12,4,12)` (Alien.java:256-257); in `customServerAiStep` on `getRandom().nextInt(8)==0` (:225) | MATCH | P1/P2 |
| filter order | `isSuitableTarget` :349-364: null (:350) → self (:353) → !alive (:356) → Player → `!isCreativeMode` (:359-362) → every non-player false (:363); no sight, no ignore | :263-267: null/self/!alive (:264) → Player → `!instabuild` (:265) → false (:266) | MATCH | players-only hunter in both trees |
| PlayNicely gate | `findSomethingToAttack` :367-369 returns null ahead of the scan, even with a live stored target | :251, same position | MATCH | |
| creative gate | `field_75098_d` :361 | `instabuild` :265 | MATCH | P5 |
| PEACEFUL gate | none (EntityMob, :40) | none (Monster, :38) | MATCH (engine, P6) | |
| allies / species exclusions | none — every non-player is refused (:363) | none (:266) | MATCH | the revenge goal (P3) may still hold a mob attacker in both trees |
| ignore screen | none (not among the 38 orig callers) | none | N/A | |
| tie-break / selection rule | `Collections.sort(GenericTargetSorter)` (:41,:59,:371) then first suitable (:380-385) | `TargetSelection.firstMatch(list, targetSorter=GenericTargetSorter, …)` (:51,:78,:260) | MATCH | P2 |
| target set / release | sticky: `getAttackTarget()` alive → returned ahead of the loop (:375-378), else `setAttackTarget(null)` (:379); the scan pick is returned, never stored (:384); stored sources: `attackEntityFrom` for `EntityLiving` attackers (+ navigate 1.2, forced true return; :234-239) and EntityAIHurtByTarget(false) (:66) | sticky :252-254; pick not stored (:260); `hurt` stores `Mob` attackers (+ navigate 1.2, forced true; :212-216); HurtByTargetGoal (:100) | MATCH | P3 re-assert applies equally — both trees consume the stored target |
| other | none | none | MATCH | the torch-seek PlayNicely gate (orig :328 / port AlienTorchSeekGoal :96) is not target selection |

### Alosaurus — orig `Alosaurus.java`, port `Alosaurus.java` — lane S1
| aspect | 1.7.10 (file:line) | port HEAD (file:line) | status | note |
|---|---|---|---|---|
| scan set | `EntityLivingBase` box 12x5x12 (Alosaurus.java:218); `nextInt(5)==0` (:164) | `LivingEntity` inflate(12,5,12) (Alosaurus.java:198-199); `nextInt(5)==0` (:170) | MATCH | |
| filter order | :182-212: null → self → !alive → `isIgnoreable` (:192) → Alosaurus (:195) → Cryolophosaurus (:198) → VelocityRaptor (:201) → sight (:204) → Player `!isCreativeMode` (:207-210) → true (:211) | :182-193, same order (:183 → :184 → :185 → :186 → :187 → :188 → :189-190 → :192) | MATCH | |
| PlayNicely gate | :215-217 | :197 | MATCH | |
| creative gate | :209 | :190 instabuild | MATCH | P5 |
| PEACEFUL gate | none | none | MATCH (engine, P6) | |
| allies / species exclusions | Alosaurus, Cryolophosaurus, VelocityRaptor (+ the ignore list) | same (:185-187) | MATCH | |
| ignore screen | :192, after !alive, before the species checks | :184, same position | FIXED (ENT-S-101) | the site was always present; the list membership is what ENT-S-101 restored |
| tie-break / selection rule | GenericTargetSorter (:39,:48,:219) + first suitable | firstMatch with GenericTargetSorter (:48,:62,:203) | MATCH | |
| target set / release | pick is transient: used for look/reach/swing/navigate on the scan tick (:165-175), never stored; `getAttackTarget()` never read; EntityAIHurtByTarget(false) (:54) stores a revenge target nothing consumes; no attackEntityFrom override | pick stored via `setTarget(prey)` and cleared on an empty scan (:171-177); consumed by `DinosaurMeleeAttackGoal` (:78-79; BugMeleeAttackGoal.java:83-96 `canUse` on `getTarget()` alive, `forgetTargetRoll` 0 at DinosaurMeleeAttackGoal.java:33), which chases/bites between scans; HurtByTargetGoal (:89) feeds the same consumer and its P3 re-assert restores the attacker after the scan's `setTarget(null)`, so an attacker the scan excludes (Cryolophosaurus, VelocityRaptor, an ignoreable) is chased until FOLLOW_RANGE 32 (:100) or 300 unseen ticks | FIXED (ENT-S-129, wave 2)` — the inert revenge task unregistered; the pass already refreshed the slot — was DIVERGES | release rule; the ENT-A-009 resolution (AUDIT_FINDINGS.md:171) calls the setTarget hand-off "the accepted mapping", but no MOD record exists |
| other | none | none | MATCH | |

### AntRobot — orig `AntRobot.java`, port `AntRobot.java` — lane S1
| aspect | 1.7.10 (file:line) | port HEAD (file:line) | status | note |
|---|---|---|---|---|
| scan set | two unsorted scans: stomp `feetFindSomethingToHit` box 10x8x10 (AntRobot.java:943), every suitable entity hit (:947-952), rolled `nextInt(20)==0` unridden (:106-108) and `nextInt(50)==0` ridden (:617-619); hunt `findSomethingToAttack(distmul,dircheck)` box (12·distmul)x12x(12·distmul) (:1015) — unridden distmul 2.0 → 24x12x24 (:117,:133), ridden distmul 1.0 → 12x12x12 (:622) | stomp inflate(10,8,10) (AntRobot.java:659-660), all hit (:661-673), rolls :226-227 / :290-291; hunt always inflate(12,12,12) (:689) | DIVERGES | scan-set narrowing: the unridden hunt box 24x12x24 → 12x12x12; stomp and ridden hunt match |
| filter order | hunt `isSuitableTarget` :1028-1071: null → self → !alive → AntRobot (:1038) → the rider (:1041) → `isIgnoreable` (:1044) → sight (:1047) → [dircheck=true only: distSq < 36 → true, else heading offset > 0.75 rad → false (:1050-1065)] → Player `!isCreativeMode` (:1066-1069) → true; stomp :955-992: … → rider (:968) → ignore (:971) → sight (:974) → 6 ≤ dist ≤ 9 (:977-986) → creative (:987-990) → true | hunt :697-704: null/self/!alive → AntRobot → first passenger → ignore (:701) → creative (:702) → true — no sight, no dircheck; stomp :677-686: … → ignore (:681) → distance (:682-683) → creative (:684) — no sight | FIXED (ENT-S-118, wave 2) — the sight in both filters (:974 stomp, :1047 hunt); the dircheck heading branch (:1050-1065) stays with T8 — was DIVERGES | filter order: sight dropped from both filters; the dircheck branch (orig call sites :133 and :622) dropped |
| PlayNicely gate | :940-942 (stomp) and :1012-1014 (hunt) return before the scan | none in either scan (:658-695) | FIXED (ENT-S-115, wave 1) — was DIVERGES | missing gate, two sites |
| creative gate | :989 / :1068 | :684 / :702 instabuild | MATCH | P5 |
| PEACEFUL gate | `owned == 0 && difficulty != PEACEFUL` gates the whole unridden block (:105); ridden stomp and ridden melee each require `!= PEACEFUL` (:617, :620); EntityLiving base — no engine despawn | `owned == 0` only (:225); ridden rolls ungated (:290, :293); Mob base — no engine despawn (grep: no PEACEFUL / getDifficulty in the file) | FIXED (ENT-S-114, wave 1) — was DIVERGES | missing gate, three sites |
| allies / species exclusions | AntRobot (:965, :1038), the rider (:968, :1041) | AntRobot (:679, :699), first passenger (:680, :700) | MATCH | |
| ignore screen | :971 / :1044, after the rider check, before sight | :681 / :701, after the passenger check | FIXED (ENT-S-106) | |
| tie-break / selection rule | no sort: first suitable in `getEntitiesWithinAABB` order (:1019-1024); stomp hits every suitable entity | first in `getEntitiesOfClass` order (:691-693); stomp hits all | MATCH | GenericTargetSorter constructed but unused in both (orig :43,:54; port :84,:106) |
| target set / release | unridden (:109-118): `nextInt(150)==0` → `setAttackTarget(null)`; dead → null; if null → transient pick with dircheck false, not stored; melee on `nextInt(15)==0` re-reads `getAttackTarget()` and, if null, re-picks with dircheck TRUE (:130-134); `attackEntityFrom` stores `EntityLiving` attackers (+ look 20/20; :579-583); no AI target tasks (:55-56) | :229-235 same 1-in-150 / dead / transient pick; melee on 1-in-15 reuses that pick, no dircheck re-pick (:249-253); `hurt` stores any `LivingEntity` attacker, players included (:271-274); no targetSelector | FIXED (ENT-S-129, wave 2)` — the Mob-only store; the 1-in-150 clear ahead of the read; the melee re-pick structural until T8's dircheck (recorded) — was DIVERGES | release rule: player attackers become the stored target; the dircheck re-pick at the melee site is gone |
| other | `owned != 0` disables the unridden hunt (:105); ridden scans need a rider (:617-620) | same (:225; :290-293) | MATCH | |

### AttackSquid — orig `AttackSquid.java`, port `AttackSquid.java` — lane S1
| aspect | 1.7.10 (file:line) | port HEAD (file:line) | status | note |
|---|---|---|---|---|
| scan set | `EntityLivingBase` box 10x4x10 (AttackSquid.java:608); `nextInt(10)==1` (:502) | NO proactive scan — `customServerAiStep` (AttackSquid.java:169-185) only reads `getTarget()`; no `getEntitiesOfClass`, no `isSuitableTarget`, no PLAY_NICELY anywhere in the 296-line file | FIXED (ENT-S-117, wave 2) — `findSomethingToAttack` :227-236, box 10/4/10, the 1-in-10 pass :193 — was DIVERGES | scan-set: the whole hunt is missing; ENT-A-017..022 (AUDIT_FINDINGS.md:234-281) cover HP, swing odds, watercanon, drops, spawn and hurt exclusions — none records the scan |
| filter order | `isSuitableTarget` :551-602: null → self → !alive → sight (:561) → Player `!isCreativeMode` (:564-567) → Girlfriend true (:568) → Boyfriend true (:571) → EntityZombie true (:574) → EntityVillager true (:577) → EntitySpider true (:580) → EntityCaveSpider true (:583) → Ghost false (:586) → GhostSkelly false (:589) → Lizard true (:592) → AttackSquid: `nextInt(5)==1` adopts it as `buddy`, false (:595-600) → anything else only when `wasshot != 0` (:601) | none (P3 revenge only) | FIXED (ENT-S-117, wave 2) — `isSuitableTarget` :248-266, the orig ladder in orig order — was DIVERGES | filter order |
| PlayNicely gate | :605-607 | none | FIXED (ENT-S-117, wave 2) — :229, read live — was DIVERGES | missing gate |
| creative gate | :566 | none (only the revenge goal's `invulnerable`, P3) | FIXED (ENT-S-117, wave 2) — :250 `instabuild` — was DIVERGES | missing gate |
| PEACEFUL gate | none | none | MATCH (engine, P6) | |
| allies / species exclusions | Ghost, GhostSkelly, AttackSquid (buddy adoption) refused; the whitelist above; other species only when `wasshot` | none | FIXED (ENT-S-117, wave 2) — Ghost / GhostSkelly / AttackSquid (buddy) refused :257-264 — was DIVERGES | exclusion list |
| ignore screen | none | none | N/A | |
| tie-break / selection rule | GenericTargetSorter (:54,:70,:609) + first suitable | none | FIXED (ENT-S-117, wave 2) — GenericTargetSorter :65 via `firstMatch` :235 — was DIVERGES | tie-break |
| target set / release | sticky: `getAttackTarget()` alive → returned (:613-616), else `setAttackTarget(null)` (:617); pick transient (:622); `attackEntityFrom` stores `EntityLiving` attackers except AttackSquid / WaterDragon (+ navigate 1.2; :379-389); EntityAIHurtByTarget(false) (:75); with no target and `buddy != null` → follow the buddy at 1.0 (:515-517) | `getTarget()` alive else `setAttacking(0)` (:170-184); `hurt` stores `Mob` attackers (+ navigate 1.2; :123-126); HurtByTargetGoal (:67); no buddy | FIXED (ENT-S-117, wave 2) — sticky read :232-233, dead-drop :234, pick transient (:194), buddy-follow :207-209 — was DIVERGES | release rule: no re-acquisition once the revenge target is lost; buddy-follow absent |
| other | `wasshot != 0` (a Squid Zooka-launched squid — the survey's "Kraken-launched" corrected 2026-09-04: only ItemSquidZooka sets `wasshot` in either tree) makes every other living entity suitable (:601) | none | FIXED (ENT-S-117, wave 2) — `wasshot != 0` :265 — was DIVERGES | other — an orig-only rule dropped with the scan |

### BandP — orig `BandP.java`, port `BandP.java` — lane S1
| aspect | 1.7.10 (file:line) | port HEAD (file:line) | status | note |
|---|---|---|---|---|
| scan set | `EntityLivingBase` box 20x6x20 (BandP.java:256); `nextInt(12)==1` (:181) | `LivingEntity` inflate(20,6,20) (BandP.java:246-247); `nextInt(12)==1` (:155) | MATCH | |
| filter order | :226-250: null → self → !alive → sight (:236) → Player `!isCreativeMode` (:239-242) → EntityVillager (:243) → Girlfriend (:246) → Boyfriend (:249) → false | :258-265, same order (:259 → :260 → :261 → :262 → :263 → :264) | MATCH | |
| PlayNicely gate | :253-255 | :245 | MATCH | |
| creative gate | :241 | :261 instabuild | MATCH | P5 |
| PEACEFUL gate | none | none | MATCH (engine, P6) | |
| allies / species exclusions | whitelist players / Villager / Girlfriend / Boyfriend; no exclusions | same | MATCH | |
| ignore screen | none | none | N/A | |
| tie-break / selection rule | GenericTargetSorter (:42,:55,:257) + first suitable | firstMatch with GenericTargetSorter (:45,:63,:250) | MATCH | |
| target set / release | transient pick per 1-in-12 tick (:181-184; navigate 1.25 when out of reach :220-222); nothing stored; no targetTasks (:56-61); no attackEntityFrom override | transient (:156-166); no targetSelector (registerGoals :71-87); no hurt override | MATCH | |
| other | none | none | MATCH | the steal on hit (orig :185-218 / port :178-…) is post-selection |

### Basilisk — orig `Basilisk.java`, port `Basilisk.java` — lane S1
| aspect | 1.7.10 (file:line) | port HEAD (file:line) | status | note |
|---|---|---|---|---|
| scan set | `EntityLivingBase` box 24x7x24 (Basilisk.java:419); `nextInt(5)==0` (:360) | `LivingEntity` inflate(24,7,24) (Basilisk.java:242-243); `nextInt(5)==0` (:209) | MATCH | |
| filter order | :384-413: null → self → !alive → `isIgnoreable` (:394) → sight (:397) → Basilisk (:400) → LeafMonster (:403) → Player creative false (:406-411) → true | :255-263, same order (:256 → :257 → :258 → :259 → :260 → :261 → :262) | MATCH | |
| PlayNicely gate | :416-418 | :241 | MATCH | |
| creative gate | :408 | :261 instabuild | MATCH | P5 |
| PEACEFUL gate | none | none | MATCH (engine, P6) | |
| allies / species exclusions | Basilisk, LeafMonster (+ the ignore list) | Basilisk, EntityLeafMonster | MATCH | |
| ignore screen | :394, before sight | :257, before sight | FIXED (ENT-S-101) | the site was always present; membership restored |
| tie-break / selection rule | GenericTargetSorter (:43,:53,:420) + first suitable | firstMatch with GenericTargetSorter (:55,:65,:247) | MATCH | |
| target set / release | transient re-pick per scan tick (:361), never stored, `getAttackTarget()` never read; EntityAIHurtByTarget(false) (:59) inert; `attackEntityFrom` stores nothing (:344-350) | transient (:210); HurtByTargetGoal (:89) inert — no consumer (comment :86-88); `hurt` stores nothing (:188-194) | MATCH | |
| other | none | none | MATCH | the Slowness on the scanned target (orig :372-374 / port :227) is post-selection |

### Bee — orig `Bee.java`, port `EntityBee.java` — lane S1
| aspect | 1.7.10 (file:line) | port HEAD (file:line) | status | note |
|---|---|---|---|---|
| scan set | `EntityLivingBase` box 10x6x10 (Bee.java:329); `this.rand.nextInt(15)==0` in the else-branch of the flight-retarget test (:179,:198) | `LivingEntity` inflate(10,6,10) (EntityBee.java:225-226); `random.nextInt(15)==0` in the same else-branch (:147,:167) | MATCH | |
| filter order | :296-323: null → self → !alive → sight (:306) → target in water false (:309) → Player `!isCreativeMode` (:312-315) → EntityVillager (:316) → Girlfriend (:319) → Boyfriend (:322) → false | :239-248, same order (:240 → :241 → :242 → :244 → :245 → :246 → :247) | MATCH | |
| PlayNicely gate | :326-328 | :224 | MATCH | |
| creative gate | :314 | :244 instabuild | MATCH | P5 |
| PEACEFUL gate | none | none | MATCH (engine, P6) | |
| allies / species exclusions | whitelist only, no exclusions | same | MATCH | |
| ignore screen | none | none | N/A | |
| tie-break / selection rule | GenericTargetSorter (:37,:50,:330) + first suitable | firstMatch with `new GenericTargetSorter(this)` (:231) | MATCH | |
| target set / release | retaliation field `rt` set by `attackEntityFrom` for any `EntityLivingBase` attacker while a flight target exists (:246-248); read before the scan on hunt ticks (:200-206), dropped only when `isDead` (:201-203); scan pick transient; no AI tasks | `retaliationTarget` set for `LivingEntity` attackers (:211-213); read first (:168-170), dropped when `isRemoved()` (:169); transient pick; no targetSelector | MATCH | |
| other | none | none | MATCH | |

### Brutalfly — orig `Brutalfly.java`, port `EntityBrutalfly.java` — lane S1
| aspect | 1.7.10 (file:line) | port HEAD (file:line) | status | note |
|---|---|---|---|---|
| scan set | (a) player strafe on `nextInt(6)==0` (Brutalfly.java:213): `World.findNearestEntityWithinAABB(EntityPlayer.class, box 30x20x30, this)` (:215) — the nearest player of ANY mode inside the box; (b) mob hunt when (a) yielded null and `nextInt(3)==0` (:228): `EntityLivingBase` box 25x20x25 (:447) | (a) `level().getNearestPlayer(this, 30.0)` (EntityBrutalfly.java:204) — all level players with `NO_SPECTATORS`, sphere distSq < 900 (vanilla EntityGetter.java:78-104); (b) inflate(25,20,25) (:334-335), same odds (:214) | DIVERGES | scan-set: strafe geometry box 30x20x30 → sphere r 30 (box corners reach ~42 blocks, the sphere adds ±30 vertical); spectators skipped (no 1.7.10 analogue) |
| filter order | (a) :216-226: nearest → `!isCreativeMode` (:217) → sight (:218) → strafe/shoot; a creative nearest is NULLED (:224-226) so (b) can run; (b) :408-441: null → self → !alive → Brutalfly (:418) → Mothra (:421) → Vortex (:424) → `isIgnoreable` (:427) → sight (:430) → EntityMob true (:433) → Player `!isCreativeMode` (:436-439) → false (:440) | (a) :205: nearest → `!invulnerable` → sight; an invulnerable nearest is NOT nulled, so `target == null` (:214) fails and (b) never runs; (b) :344-360, same chain (Monster for EntityMob :357; `!invulnerable` :358) | DIVERGES | filter order: a creative/invulnerable nearest player shadows the mob hunt in the port; 1.7.10 fell through to it |
| PlayNicely gate | (b) only, :444-446; (a) ungated | (b) only, :333; (a) ungated | MATCH | |
| creative gate | :217 (a) and :438 (b) `isCreativeMode` | :205 and :358 `invulnerable` | FIX IN FLIGHT (ENT-S-109) | both sites are on ENT-S-109's list |
| PEACEFUL gate | none | none | MATCH (engine, P6) | |
| allies / species exclusions | Brutalfly, Mothra, Vortex (+ the ignore list) | EntityBrutalfly, Mothra, EntityVortex (:346-350) | MATCH | |
| ignore screen | :427, after Vortex, before sight | :355, same position | FIXED (ENT-S-101) | the site was always present; membership restored |
| tie-break / selection rule | (a) `<=` — the LAST equidistant player wins (World.func_72857_a; bytecode verified under ENT-S-105); (b) GenericTargetSorter (:50,:60,:448) + first suitable | (a) strict `<` — the FIRST wins (vanilla EntityGetter.java:85); (b) firstMatch with GenericTargetSorter (:341) | DIVERGES | tie-break on the strafe pick: the ENT-S-105 class, fixed for the Kraken only |
| target set / release | no stored target: both picks are per-tick transients; `currentFlightTarget` is steered to the pick (:219, :232); `attackEntityFrom` re-aims the flight target at any attacker (:276-278), no setAttackTarget; no AI tasks | same transients (:206, :217); `hurt` re-aims (:296-298); no targetSelector | MATCH | |
| other | players are only ever strafed (no melee path); mobs meleed inside distSq 25, else shot (:233-239) | same (:218-224) | MATCH | |

### CaterKiller — orig `CaterKiller.java`, port `EntityCaterKiller.java` — lane S1
| aspect | 1.7.10 (file:line) | port HEAD (file:line) | status | note |
|---|---|---|---|---|
| scan set | `EntityLivingBase` box 20x8x20 (CaterKiller.java:563); `nextInt(4)==0` (:462), and only when no live stored target (:463-473) | vanilla `NearestAttackableTargetGoal<>(this, Player.class, true)` (EntityCaterKiller.java:100): all level players within FOLLOW_RANGE 40 (:110) × visibility, no box, ≈1 attempt per 10 ticks, only while no target is held (P4); plus HurtByTargetGoal (:99); `Params.caterKiller()` records 20/8 search dims (BugMeleeAttackGoal.java:63) the goal never reads (:116-157) | DIVERGES | scan-set narrowing: players only — EntityMob and `isAttackableNonMob` prey (:553-556) are never hunted; box 20x8x20 → sphere r 40; cadence 1-in-4 → ≈1-in-10; not on ENT-S-108's list |
| filter order | :533-557: null → self → !alive → `MyCanSee` (:543; a custom 10-step block ray from 2.5 blocks ahead of the body at y+3 to the target's mid-height, air/web/tall-grass/leaves transparent, :626-676) → Player `!isCreativeMode` (:546-549) → CaterKiller false (:550) → EntityMob true (:553) → `isAttackableNonMob` (:556) | the `TargetingConditions` chain (P4): self → alive/spectator → `canAttack` (PEACEFUL-player, invulnerable) → `canAttackType` → `isAlliedTo` → range × visibility → vanilla eye-to-eye `hasLineOfSight`; no species branch | DIVERGES | filter order: the custom LoS replaced by the vanilla eye ray; the non-player branches absent |
| PlayNicely gate | :560-562 gates the hunt | no gate on either target goal (PLAY_NICELY only at :86 size and :258 tree-eat) | FIXED (ENT-S-115, wave 1) — was DIVERGES | missing gate |
| creative gate | :548 `isCreativeMode` | vanilla `Abilities.invulnerable` + spectator via `Player.canBeSeenAsEnemy` (P5) | DIVERGES | the ENT-S-109 mapping class, reached through the vanilla goal; not on ENT-S-109's list |
| PEACEFUL gate | none | none (vanilla `canAttack` additionally refuses players in PEACEFUL — moot) | MATCH (engine, P6) | |
| allies / species exclusions | CaterKiller refused (:550), after the player branch | no species branch (players-only goal) | DIVERGES | exclusion list — absent with the non-player branch |
| ignore screen | none | none | N/A | |
| tie-break / selection rule | GenericTargetSorter (:43,:62,:564) + first suitable | nearest player by plain distSq to the eye, strict `<` (vanilla EntityGetter.java:148-160) | DIVERGES | tie-break |
| target set / release | stored target read first (:463); dead → null (:464-467); `nextInt(200)==0 → setAttackTarget(null)` inside the 1-in-4 block (:468-470, ≈1-in-800 per tick); if null → transient pick (:471-473, not stored); `attackEntityFrom` stores `EntityLiving` attackers (:96-98); EntityAIHurtByTarget(false) (:68) | goals store via `setTarget` and release per TargetGoal (FOLLOW_RANGE 40, 60-tick unseen memory, `canAttack`); `BugMeleeAttackGoal` consumes it (`forgetTargetRoll` 200 → `setTarget(null)` on `nextInt(200)==0` EVERY tick the goal runs, BugMeleeAttackGoal.java:123-129); `hurt` stores `Mob` attackers (:170-172); HurtByTargetGoal (:99) with the P3 re-assert | FIXED (ENT-S-129, wave 2) — the 1-in-200 inside the 1-in-4 pass, final; the vanilla player goal's hold stays with T3b — was DIVERGES | release rule: sticky vanilla target (range / unseen) vs transient re-pick; forget cadence ≈1-in-800 → 1-in-200 |
| other | `foundmob` (:475,:499) suppresses the tree-walk while hunting (:517) | `target == null` gate (:261) | MATCH | not selection |

### CaveFisher — orig `CaveFisher.java`, port `CaveFisher.java` — lane S1
| aspect | 1.7.10 (file:line) | port HEAD (file:line) | status | note |
|---|---|---|---|---|
| scan set | `EntityLivingBase` box 10x3x10 (CaveFisher.java:234); `nextInt(8)==0` (:168) | two vanilla goals (CaveFisher.java:81-82, :86-87): `Player.class` — all level players within FOLLOW_RANGE 16 (:97) × visibility, no box; `Animal.class` — `getEntitiesOfClass(Animal, bbox.inflate(16,4,16))`; each ≈1 attempt per 10 ticks, only while no target is held (P4) | FIX IN FLIGHT (ENT-S-108) | the class narrowing is ENT-S-108's subject; residual to watch after it: box 10x3x10 vs 16x4x16 / sphere 16, cadence 1-in-8 vs ≈1-in-10 |
| filter order | :193-228: null → self → !alive → `isIgnoreable` (:203) → sight (:206) → CaveFisher (:209) → EnderReaper (:212) → EnderKnight (:215) → EntityMob false (:218) → Player creative false (:221-226) → true | `TargetingConditions` with selector `!isIgnoreable` (:82, :87) ahead of sight (P4); no species branch (moot under the Player / Animal classes) | FIX IN FLIGHT (ENT-S-108) | its fix shape names "the orig exclusion chain" |
| PlayNicely gate | :231-233 | none (no PLAY_NICELY in the file) | FIXED (ENT-S-108; pinned by ENT-S-115, wave 1) — the gate landed with the restored scan (CaveFisher.java:196) — was DIVERGES | missing gate; outside ENT-S-108's text |
| creative gate | :223 `isCreativeMode` | vanilla `invulnerable` + spectator (P5) | DIVERGES | the ENT-S-109 mapping class via the vanilla goal; not on its list |
| PEACEFUL gate | none | none | MATCH (engine, P6) | |
| allies / species exclusions | CaveFisher, EnderReaper, EnderKnight, every EntityMob | none (moot for the Player / Animal goals) | FIX IN FLIGHT (ENT-S-108) | |
| ignore screen | :203, after !alive, before sight | goal predicates :82, :87 — inside `TargetingConditions`, ahead of the sight check | FIXED (ENT-S-106) | |
| tie-break / selection rule | GenericTargetSorter (:38,:49,:235) + first suitable | nearest by plain distSq to the eye, strict `<` (vanilla EntityGetter.java:148-160) | DIVERGES | tie-break |
| target set / release | transient pick per 1-in-8 tick (:169-181), nothing stored, `getAttackTarget()` never read; EntityAIHurtByTarget(false) (:55) inert; `attackEntityFrom` stores nothing (:185-191) | goals store via `setTarget`, released by TargetGoal (FOLLOW_RANGE 16, 60-tick unseen memory, `canAttack`); `BugMeleeAttackGoal` consumes it (`forgetTargetRoll` 0, BugMeleeAttackGoal.java:64); HurtByTargetGoal (:78) is now consumed too, with the P3 re-assert | FIXED (ENT-S-129, wave 2)` — the scan every pass, the revenge task unregistered, the mark retired — was DIVERGES | release rule: a sticky vanilla target and a live revenge chase where 1.7.10 re-picked every scan and never retaliated |
| other | none | none | MATCH | |

### Cephadrome — orig `Cephadrome.java`, port `Cephadrome.java` — lane S1
| aspect | 1.7.10 (file:line) | port HEAD (file:line) | status | note |
|---|---|---|---|---|
| scan set | `EntityLivingBase` box 16x20x16 (Cephadrome.java:579); `nextInt(7)==1 && difficulty != PEACEFUL` (:488), only when no live stored target (:489-496) | `LivingEntity` inflate(16,20,16) (Cephadrome.java:410-411); `nextInt(7)==1` (:360), only when no live target (:361-368) | MATCH | box and cadence; the PEACEFUL half of :488 is in the PEACEFUL row |
| filter order | :515-573: PEACEFUL false (:516) → null → self → !alive → sight (:528) → Cephadrome false (:531) → EntityMob true (:534) → Mothra true (:537) → Leon untamed (:540-543) → GammaMetroid untamed (:544-547) → WaterDragon untamed (:548-551) → EntityDragon true (:552) → Player: creative false (:557) / `hit_by_player` true (:560) / `badmood` true (:563) / `shouldattack > 0` → reset to 0 and true (:566-569) / false (:570) → false (:572) | :390-407: null/self/!alive (:391) → sight (:392) → Cephadrome (:393) → Monster (:394) → Mothra (:397) → EntityLeon / EntityGammaMetroid / WaterDragon `!isTame()` (:398-400) → EnderDragon (:401) → Player: instabuild false (:403) / `hitByPlayer or badmood or shouldattack > 0` without the reset (:404) → false (:406) | FIX IN FLIGHT (ENT-S-113) | the PEACEFUL head and the `shouldattack` reset are ENT-S-113's two lines; the rest matches |
| PlayNicely gate | :576-578 gates the scan | `findSomethingToAttack` :409-415 has no gate (PLAY_NICELY only feeds `wasfed`, :175-177) | FIXED (ENT-S-115, wave 1) — was DIVERGES | missing gate |
| creative gate | :557 | :403 instabuild | FIXED (ENT-S-107) | |
| PEACEFUL gate | :488 (scan cadence) and :516 (filter head); EntityCreature — no engine despawn | neither (no PEACEFUL / getDifficulty in the file); PathfinderMob — no engine despawn | FIXED (ENT-S-114, wave 1) — was FIX IN FLIGHT (ENT-S-113) / DIVERGES | ENT-S-113 restores :516 only; the :488 cadence guard is outside its text (counted once as DIVERGES) |
| allies / species exclusions | Cephadrome refused; whitelist EntityMob, Mothra, untamed Leon / GammaMetroid / WaterDragon, EntityDragon, gated players | same (:393-404) | MATCH | |
| ignore screen | none | none | N/A | |
| tie-break / selection rule | GenericTargetSorter (:61,:84,:580) + first suitable | `Comparator.comparingDouble(this::distanceToSqr)` (:118) — plain distance, no creeper / silhouette weighting | DIVERGES | tie-break / sorter: the TF-035 swap never reached this file (ENT-A-082's Phase C fix predates it) |
| target set / release | stored target read (:489), dead → null (:490-493), pick transient (:495, not stored); `attackEntityFrom` stores any `EntityLivingBase` attacker + navigate 1.2 unless `hurt_timer > 0` or cactus (:434-448); `hit_by_player` when a player hits it below 90% (:449-451); EntityAIHurtByTarget(false) (:82) | same read / dead / transient (:361-368); `hurt` stores any LivingEntity + navigate (:333-342), `hitByPlayer` (:343-345); HurtByTargetGoal (:132) | MATCH | P3 re-assert applies equally |
| other | `badmood` from the spawner-bypass spawn (:607), `shouldattack` from a refused mount (:896-901), both cleared by feeding (:882-883); PlayNicely==0 re-feeds every tick (:661-663) | :528-531, :448-452, :431-432, :175-177 | MATCH | the ridden branch (orig :498-501: no navigation and bite reach 10 while ridden; port :370-374 always navigates 1.7 with reach 6+w/2) changes the bite reach, not the pick — noted, not counted |

### CloudShark — orig `CloudShark.java`, port `CloudShark.java` — lane S1
| aspect | 1.7.10 (file:line) | port HEAD (file:line) | status | note |
|---|---|---|---|---|
| scan set | `EntityLivingBase` box 12x10x12 (CloudShark.java:249); `this.rand.nextInt(9)==2` (:153) | `LivingEntity` inflate(12,10,12) (CloudShark.java:168-169); `random.nextInt(9)==2` (:135) | MATCH | |
| filter order | :202-243: null → self → !alive → sight (:212) → RockBase false (:215) → EntityAnt false (:218) → EntityButterfly true (:221) → Cockateil true (:224) → EntityMosquito true (:227) → Firefly true (:230) → Player `!isCreativeMode` true (:233-238) → GoldFish true (:239) → CliffRacer (:242) | :184-198, same order | MATCH | a creative player falls through to the CliffRacer test in both |
| PlayNicely gate | :246-248 | :167 | MATCH | |
| creative gate | :235 | :195 instabuild | MATCH | P5 |
| PEACEFUL gate | none | none | MATCH (engine, P6) | |
| allies / species exclusions | RockBase, EntityAnt refused; the whitelist above | same | MATCH | |
| ignore screen | none (RockBase / EntityAnt named directly) | none | N/A | |
| tie-break / selection rule | GenericTargetSorter (:37,:45,:250) + first suitable | firstMatch with GenericTargetSorter (:44,:173) | MATCH | |
| target set / release | transient pick per 1-in-9 tick (:155-161), nothing stored; `attackEntityFrom` re-aims the flight target at any attacker (:191-194); no AI tasks | same (:136-142; :84-87); no targetSelector | MATCH | |
| other | none | none | MATCH | |

### Crab — orig `Crab.java`, port `Crab.java` — lane S1
| aspect | 1.7.10 (file:line) | port HEAD (file:line) | status | note |
|---|---|---|---|---|
| scan set | `EntityLivingBase` box 16x6x16 (Crab.java:424); `nextInt(5)==1` (:340), only when no live stored target | `LivingEntity` inflate(16,6,16) (Crab.java:340-341); `nextInt(5)==1` (:268) | MATCH | |
| filter order | :379-418: null → self → !alive → sight (:389) → Player `!isCreativeMode` (:392-395) → Crab false (:396) → EntityMob true (:399) → Lizard (:402) → RubberDucky (:405) → EntityVillager (:408) → Girlfriend (:411) → Boyfriend (:414) → `isAttackableNonMob` (:417) | :355-367, same order | MATCH | |
| PlayNicely gate | :421-423 | :339 | MATCH | |
| creative gate | :394 | :358 instabuild | MATCH | P5 |
| PEACEFUL gate | none | none | MATCH (engine, P6) | |
| allies / species exclusions | Crab refused; whitelist + `isAttackableNonMob` | same | FIXED (ENT-S-128, wave 2) — the helper fix covers the :366 call site; pinned through the Crab (lane M O1 closed) — was MATCH |  |
| ignore screen | none | none | N/A | |
| tie-break / selection rule | GenericTargetSorter (:43,:58,:425) + first suitable | firstMatch with GenericTargetSorter (:57,:79,:344) | MATCH | |
| target set / release | `nextInt(100)==1 → setAttackTarget(null)` (:342-344); dead → null (:345-348); if null → transient pick (:349-351; the finder also returns a live stored target first, :429-433); `attackEntityFrom` stores `EntityLiving` attackers (+ navigate 1.2; a Crab attacker returns false; :232-238); EntityAIHurtByTarget(false) (:64) | dead → null (:270-273); transient pick (:274); NO 1-in-100 forget; `hurt` stores any `LivingEntity` attacker, players included (+ navigate 1.2; :235-237), bypassing the revenge goal's invulnerable / creative screen; HurtByTargetGoal (:91) | FIXED (ENT-S-129, wave 2)` — the 1-in-100 ahead of the read; the Mob-only store outside the timer — was DIVERGES | release rule: the 1-in-100 forget is missing; player attackers are stored directly |
| other | reach scaled by `getCrabScale()` (:354) | :280 | MATCH | not selection |

### CreeperRepellent — orig `CreeperRepellent.java`, port `block/RepellentBlock.java` — lane S1
| aspect | 1.7.10 (file:line) | port HEAD (file:line) | status | note |
|---|---|---|---|---|
| (all) | not a hunter: `CreeperRepellent extends BlockTorch` (CreeperRepellent.java:22-23); its 10-tick `updateTick` scan over living entities (:83-145) pushes EntityCreeper (:94-109), EntityAnt (:110-125) and PurplePower (:126-145; a type-10 PurplePower aborts the loop, :128-130) away from the block — a repel, not a target pick | `block/RepellentBlock.java:99-130` (Creeper :130, EntityAnt :123, PurplePower with the type-10 abort noted :128-129) | N/A | no targeting aspects in either tree; the repel set itself was not audited by this lane |

### CreepingHorror — orig `CreepingHorror.java`, port `CreepingHorror.java` — lane S1
| aspect | 1.7.10 (file:line) | port HEAD (file:line) | status | note |
|---|---|---|---|---|
| scan set | `EntityLivingBase` box 16x4x16 (CreepingHorror.java:206); `nextInt(5)==1` (:139) | `LivingEntity` inflate(16,4,16) (CreepingHorror.java:169-170); `nextInt(5)==1` (:153) | MATCH | |
| filter order | :147-200: null → self → !alive → sight (:157) → CreepingHorror (:160) → RockBase (:163) → EnderReaper (:166) → LeafMonster (:169) → Dragon (:172) → TerribleTerror (:175) → LurkingTerror (:178) → PitchBlack (:181) → Firefly (:184) → Island (:187) → IslandToo (:190) → Player creative false (:193-198) → true | :184-200, same order | MATCH | |
| PlayNicely gate | :203-205 | :168 | MATCH | |
| creative gate | :195 | :198 instabuild | MATCH | P5 |
| PEACEFUL gate | none | none | MATCH (engine, P6) | |
| allies / species exclusions | the ten names above | same (:187-197) | MATCH | |
| ignore screen | none (RockBase and Firefly named directly) | none | N/A | |
| tie-break / selection rule | GenericTargetSorter (:42,:58,:207) + first suitable | firstMatch with GenericTargetSorter (:50,:62,:173) | MATCH | |
| target set / release | transient pick per 1-in-5 tick (:139-143), the stored target never read; `nextInt(200)==1 → func_70604_c(null)` = setRevengeTarget(null) clears the revenge MEMORY (:136-138); EntityAIHurtByTarget(false) (:57) inert (EntityAIPanic :52; no attack task); no attackEntityFrom override | transient (:154); `nextInt(200)==1 → setTarget(null)` clears the attack TARGET instead (:150-152) — the port's own convention for this orig line is `setLastHurtByMob(null)` (Cryolophosaurus :89-91, Boyfriend :286, ThePrince :239-241, Girlfriend :324, Peacock :135, Lizard :143-145, EasterBunny :107, ThePrincess :227); HurtByTargetGoal (:82) inert (no consumer); no hurt override | FIXED (ENT-S-129, wave 2)` — `setLastHurtByMob(null) — was DIVERGES | release rule (wrong field); inert in both trees today — nothing consumes the stored target |
| other | none | none | MATCH | |

### Cryolophosaurus — orig `Cryolophosaurus.java`, port `Cryolophosaurus.java` — lane S2
| aspect | 1.7.10 (file:line) | port HEAD (file:line) | status | note |
|---|---|---|---|---|
| scan set | `EntityLivingBase`, own bb `expand(9,2,9)` (:217), `func_70619_bc` (:141) on world-rand `nextInt(5)==1` (:150) | `LivingEntity`, bb `inflate(9,2,9)` (:128-129), `customServerAiStep` (:86) on entity-rand `nextInt(5)==1` (:96) | MATCH | rand stream only (KT-C convention) |
| filter order | :158-211 null → self → dead → sight (:168) → Alosaurus, TRex, Cryolophosaurus, Ghost, GhostSkelly, CaveFisher, GammaMetroid, EntityButterfly, Firefly, EntityMosquito, RockBase (:171-203) → player creative (:204-208) | :109-122 null/self/dead (:110) → sight (:111) → the same eleven in the same order (:112-118) → player creative (:120) | MATCH | — |
| PlayNicely gate | :214 `PlayNicely != 0 → null` in `findSomethingToAttack` | :127 `OreSpawnConfig.PLAY_NICELY.get()` | MATCH | — |
| creative gate | :206 `isCreativeMode` | :120 `invulnerable` | FIX IN FLIGHT (ENT-S-109) | — |
| PEACEFUL gate | none (:158-229; EntityMob despawns in peaceful) | none (:109-133; Monster) | MATCH | — |
| allies / species exclusions | Alosaurus, TRex, Cryolophosaurus, Ghost, GhostSkelly, CaveFisher, GammaMetroid, EntityButterfly, Firefly, EntityMosquito, RockBase (:171-203) | same eleven (:112-118) | MATCH | — |
| ignore screen | none (:158-211; not among the 38 ENT-S-106 callers) | none (:109-122) | MATCH | — |
| tie-break / selection rule | `Collections.sort(TargetSorter)` (:58, :218) then first suitable (:222-226) | `TargetSelection.firstMatch(…, comparingDouble(distanceToSqr), …)` (:132) — plain nearest | DIVERGES | tie-break: creeper/silhouette weights lost (TF-035 remainder) |
| target set / release | scan pick never stored (:150-155, used directly); revenge target cleared on world-rand 1-in-200 (:147-149 `func_70604_c(null)`); attack target only from `EntityAIHurtByTarget` (:57), dropped by the vanilla target-AI rules (dead / follow range / 60 t unseen) | scan pick not stored (:97-104); `setLastHurtByMob(null)` 1-in-200 (:90-91); `HurtByTargetGoal` (:62) held per `TargetGoal` (dead / FOLLOW_RANGE 16 :77 / 30 evaluated ticks unseen); PLUS `DinosaurMeleeAttackGoal` preset `forgetTargetRoll 200` → `setTarget(null)` on 1-in-200 every tick (`BugMeleeAttackGoal` :123-128, `DinosaurMeleeAttackGoal.Presets.cryolophosaurus` :37) | RECORDED (MOD-035) / MATCH in classic` — no melee goal in classic; the modern goal's forget is the record's tuning — was DIVERGES | release rule: a second, port-only 1-in-200 drop of the attack target |
| other | no attack goal — the HurtBy target is never chased (goals :51-57) | `DinosaurMeleeAttackGoal` @2 (:57-58) chases/bites the HurtBy target with the :150-152 dice (`BugMeleeAttackGoal` :131-156) | RECORDED (MOD-035, ENT-S-125) — the melee chase goal registers only while `modern.enabled && cryolophosaurusRevengeChase`; classic = orig :51-57, no chase — was PORT-ONLY | the accepted Dinosaur mapping (AUDIT_FINDINGS.md:171, Alosaurus record); no Cryolophosaurus record names it; affects the retaliation chase, not who is chosen |

### Dragon — orig `Dragon.java`, port `Dragon.java` — lane S2
| aspect | 1.7.10 (file:line) | port HEAD (file:line) | status | note |
|---|---|---|---|---|
| scan set | (a) vanilla `EntityAINearestAttackableTarget(this, EntityLiving.class, 0, true, false, IMob.mobSelector)` @target-1, registered only when `PlayNicely == 0` (:115-117): every tick, follow-range box, sight, IMob only; (b) `findSomethingToAttack`: `EntityLivingBase` bb `expand(20,20,20)` (:580) from three world-rand callers — ground `func_70619_bc` 1-in-10 when !sitting, activity 0, no rider, !PEACEFUL (:430); ridden `fly_with_rider` 1-in-7 (`freq` :488, :498) !PEACEFUL; flight 1-in-9 when !toofar, unstick 0, flyaway 0, !PEACEFUL (:734) | (a) absent — `targetSelector` holds `HurtByTargetGoal` only (:145); (b) `LivingEntity` bb `inflate(20,20,20)` (:935-936) from `customServerAiStep` 1-in-10 (:369-372), `handleRiderCombat` 1-in-7 (:547, :559), flight 1-in-9 (:646-649), all PEACEFUL-gated, entity-rand | FIXED (ENT-S-117, wave 2) — channel (a) as the live-gated `NearestAttackableTargetGoal<Monster>` at target priority 1 (:148-166), follow range 16; the (b) custom scan unchanged (MATCH); the vanilla four (Slime / MagmaCube / Ghast / EnderDragon) rejoin channel (a) under the IMob convention, Mob + Enemy (ENT-S-124); OreSpawn's Mothra (orig Mothra.java:52 implements IMob; the port Mothra no Enemy) stays disclosed — was DIVERGES | scan-set narrowing: the continuous vanilla IMob channel (a) is missing; (b) matches |
| filter order | :527-574 PEACEFUL → null → self → dead → sight (:540) → LurkingTerror, EnderReaper, TerribleTerror, LeafMonster, CreepingHorror, Triffid (:543-560) → EntityMob true (:561) → Mothra true (:564) → Kraken true (:567) → player false (:570) → false | :917-932 null/self/dead (:918) → sight (:919) → PEACEFUL (:920) → Dragon false (:922) → `MyUtils.isAlly` (:923; util :65-72) → Monster (:925) → Mothra (:926) → Kraken (:927) → Player false (:929) → false | MATCH | PEACEFUL moved after sight (side-effect-free); the extra Dragon/Spyro names are inert (neither was EntityMob-accepted in orig) |
| PlayNicely gate | :577 in `findSomethingToAttack`; :115 also gates the vanilla goal's registration | none in `findSomethingToAttack` (:934-940) | FIXED (ENT-S-115, wave 1) — the scan gate; the :115 registration half rides with T3a's IMob channel — was DIVERGES; the :115 registration half FIXED (ENT-S-117, wave 2) as the goal's live `canUse` (:158) | missing gate |
| creative gate | none — players are never prey (:570) | none (:929) | N/A | — |
| PEACEFUL gate | :528 filter + :430 / :498 / :734 callers | :920 filter + :370 / :546 / :647 callers | MATCH | — |
| allies / species exclusions | LurkingTerror, EnderReaper, TerribleTerror, LeafMonster, CreepingHorror, Triffid (:543-560) | Dragon (:922) + `isAlly`: LurkingTerror, EnderReaper, TerribleTerror, LeafMonster, CreepingHorror, Triffid, Spyro (util :65-72) | MATCH | extra names inert |
| ignore screen | none (not an ENT-S-106 caller) | none | MATCH | — |
| tie-break / selection rule | sorter (:120, :581), first suitable (:585-590) | `GenericTargetSorter` (:132) via `firstMatch` (:939) | MATCH | TF-035 rider applied |
| target set / release | ridden branch: stored target cleared 1-in-250 (:499-501), dead → cleared (:502-505), else scan, pick not stored (:507); hurt: Dragon/Spyro attackers ignored (:407-412), tamed + player attacker → no retarget (:416-418), any other living attacker → `setAttackTarget` + navigate (:419-421); `EntityAIHurtByTarget` @2 (:118); channel (a) sets the attack target every tick it finds an IMob | `handleRiderCombat` 1-in-250 clear (:549-551), dead clear (:554-557), scan (:559); hurt :302-316 same rules (`setTarget` + `setLastHurtByMob` + `moveTo`); `HurtByTargetGoal` (:145) | FIXED (ENT-S-117, wave 2) — channel (a) stores its IMob on start; `HurtByTargetGoal` at priority 2 (:167, orig :118) — was DIVERGES | release/set rule: only channel (a)'s stored-target feed is missing; the rest matches |
| other | tamed dragon under 25 % health flees the scanned prey (:737-742); ground scan only at activity 0 without a rider (:430) | :651-658 flee; :369 gates | MATCH | — |

### Dragonfly — orig `Dragonfly.java`, port `EntityDragonfly.java` (hunt in `entity/ai/DragonflyHuntGoal.java`) — lane S2
| aspect | 1.7.10 (file:line) | port HEAD (file:line) | status | note |
|---|---|---|---|---|
| scan set | `EntityLivingBase` bb `expand(10,6,10)` (:235), in `func_70619_bc`'s else-branch: entity-rand 1-in-12 on every tick where the 1-in-300 / near flight retarget did NOT fire, and !PEACEFUL (:124, :142) | `LivingEntity` bb `inflate(10,6,10)` (DragonflyHuntGoal :70-71) from `pickRetarget` (:58-67), which runs only when the flight retarget fires (`AmbientFlightGoal` :109-113: 1-in-300 or distSq < 4.5, `Params.dragonfly` :73) and then a 1-in-12 roll (:59) | DIVERGES | cadence inverted: the hunt sits inside the retarget instead of outside it (its own javadoc: "roughly every 3600 ticks" :27-28 vs ≈ every 12 in orig) |
| filter order | :197-229 PEACEFUL → null → self → dead → sight (:210) → EntityAnt, EntityButterfly, Cockateil, EntityMosquito, Firefly true (:213-227) → EntityHorse && `DragonflyHorseFriendly == 0` (:228) → false | :77-83 self → dead → !EntityDragonfly → !Player → !(horseFriendly && AbstractHorse) → `bbWidth <= 0.6` → sight | FIXED (ENT-S-128, wave 2) — the whitelist (DragonflyHuntGoal.isPrey :100-118) for the width rule; the sight step ahead of it again as orig :210 precedes :213 — was DIVERGES | whitelist replaced by a width rule; sight last |
| PlayNicely gate | :232 | none (DragonflyHuntGoal :69-84; `AmbientFlightGoal.canUse` :88-90 gates passenger/water only) | FIXED (ENT-S-115, wave 1) — was DIVERGES | — |
| creative gate | N/A (players never in the whitelist :213-228) | N/A (players excluded :79) | N/A | — |
| PEACEFUL gate | :142 caller + :198 filter | none | FIXED (ENT-S-114, wave 1) — was DIVERGES | an Animal does not despawn in peaceful — the port hunts there |
| allies / species exclusions | prey whitelist: EntityAnt, EntityButterfly, Cockateil, EntityMosquito, Firefly, EntityHorse (config) | prey = any living thing ≤ 0.6 wide except dragonflies and players; horses (1.4 wide) never prey even with the toggle off (:80-82) | FIXED (ENT-S-128, wave 2) — EntityAnt, EntityButterfly, Cockateil, EntityMosquito, Firefly, AbstractHorse unless `dragonflyHorseFriendly` — was DIVERGES | extra prey: chickens, bats, rabbits, cats, silverfish, endermites, baby animals, Cricket/Chipmunk-sized OreSpawn mobs; missing prey: horses |
| ignore screen | none | none | MATCH | — |
| tie-break / selection rule | sorter (:45, :236), first suitable | plain nearest via `firstMatch` (:75-76) | DIVERGES | — |
| target set / release | never stored; flight target := prey (:146); one bite per pass when distSq < 6 (:147-148) | `setTarget(prey)` (:62); bites every tick while distSq < 6 (:50-54); nothing clears the target (no owning goal; `hurt` only moves the flight target :90-96) | FIXED (ENT-S-129, wave 2)` — one bite per pass, nothing retained (the slot a pass-tick hand-off) — was DIVERGES | release rule: the prey is stored and never dropped |
| other | hurt → flight target := attacker (:178-184) | hurt → `setFlightTarget(attacker)` (:90-96) | MATCH | — |

### DungeonBeast — orig `DungeonBeast.java`, port `DungeonBeast.java` — lane S2
| aspect | 1.7.10 (file:line) | port HEAD (file:line) | status | note |
|---|---|---|---|---|
| scan set | `EntityLivingBase` bb `expand(16,3,16)` (:253) in `func_70619_bc` on world-rand 1-in-8 (:172) | vanilla `NearestAttackableTargetGoal<>(this, Player.class, true, pred)` (:69-70): `Player.class` only, every player within FOLLOW_RANGE 24 (:79) × visibility from the eye, ≈ 1-in-10 cadence (shared refs) | FIX IN FLIGHT (ENT-S-108) | class narrowing filed; box 16×3×16 → radius-24 sphere and the cadence also differ |
| filter order | :200-247 null → self → dead → `isIgnoreable` (:210) → sight (:213) → Rat, DungeonBeast, Rotator, Peacock, Irukandji, Skate, Whale, Flounder (:216-239) → player creative (:240-244) | vanilla chain: self → spectator/dead → `!isIgnoreable` (:70) → `canAttack` (PEACEFUL-for-players, `!invulnerable`) → not Ghast → not allied → range → sight last (`TargetingConditions.test`) | FIXED (ENT-S-108; pinned by ENT-S-128, wave 2) — chain at DungeonBeast.java:202-209 — was DIVERGES | exclusions absent (moot while players-only; ENT-S-108's fix shape carries "the orig exclusion chain"); sight moved last (side-effect-free) |
| PlayNicely gate | :250 | none (:59-71) | FIXED (ENT-S-108; pinned by ENT-S-115, wave 1) — gate at DungeonBeast.java:183 — was DIVERGES | — |
| creative gate | :242 `isCreativeMode` | vanilla `abilities.invulnerable` (`Player.canBeSeenAsEnemy` :903-905 via `TargetingConditions` :73) | DIVERGES | the ENT-S-109 class at a vanilla-goal site; not on that record's list |
| PEACEFUL gate | none (EntityMob) | vanilla: a Player is refused in PEACEFUL (`LivingEntity.canAttack` :864-866) | MATCH (engine, P6) | inert — Monster despawns in peaceful (re-rated 2026-09-04, T9: was PORT-ONLY — no port-written code, the engine's own `canAttack` refusal, rated MATCH (engine, P6) on every other Monster row per V13; nothing to delete, never add a `canAttack` override) |
| allies / species exclusions | Rat, DungeonBeast, Rotator, Peacock, Irukandji, Skate, Whale, Flounder (:216-239) | none (:69-70) | FIX IN FLIGHT (ENT-S-108) | — |
| ignore screen | :210, ahead of sight | :70 as the goal predicate, ahead of the vanilla sight check | FIXED (ENT-S-106) | — |
| tie-break / selection rule | sorter (:53, :254), first suitable (:258-263) | nearest player from the eye, strict `<`, first in `players()` order on ties (`EntityGetter` :148-165) | DIVERGES | — |
| target set / release | scan pick never stored (:173-185, re-picked every 1-in-8 pass; `setAttacking(0)` when none :184); `EntityAIHurtByTarget` (:59) target inert (no attack goal) | the goal stores the pick (`setTarget` :77) and holds it while alive / within 24 / seen within 30 evaluated ticks (`TargetGoal` :38-71), `stop()` → null; `BugMeleeAttackGoal` (:61-62) acts on it, `forgetTargetRoll 0` (`Params.dungeonBeast` :70) | FIXED (ENT-S-129, wave 2)` — as CaveFisher — was DIVERGES | release rule: stored-and-held vs re-scanned each pass |
| other | `EntityAIHurtByTarget(this,false)` (:59) | `HurtByTargetGoal(this)` (:66), no alert | MATCH | — |

### EmperorScorpion — orig `EmperorScorpion.java`, port `EntityEmperorScorpion.java` — lane S2
| aspect | 1.7.10 (file:line) | port HEAD (file:line) | status | note |
|---|---|---|---|---|
| scan set | `EntityLivingBase` bb `expand(24,6,24)` (:507) in `func_70619_bc` on world-rand 1-in-4 (:408), only when no live stored target (:417-419) | vanilla `NearestAttackableTargetGoal<>(this, Player.class, true, pred)` (:80-81): players within FOLLOW_RANGE 40 (:91), ≈ 1-in-10 | FIX IN FLIGHT (ENT-S-108) | box 24×6×24 → radius 40 |
| filter order | :460-501 null → self → dead → sight (:470) → `isIgnoreable` (:473) → EntityEnderman, EnderKnight, EnderReaper, EntityCreeper, Scorpion, EmperorScorpion (:476-493) → player creative (:494-498) | vanilla chain with `!isIgnoreable` predicate (:81) → creative/PEACEFUL → range → sight | FIXED (ENT-S-108; pinned by ENT-S-128, wave 2) — EntityEmperorScorpion.java:298-303 — was DIVERGES | exclusions absent; ignore-before-sight vs orig sight-before-ignore (side-effect-free, noted in ENT-S-106) |
| PlayNicely gate | :504 | none (:65-82, :171-192) | FIXED (ENT-S-108; pinned by ENT-S-115, wave 1) — gate at EntityEmperorScorpion.java:279 — was DIVERGES | — |
| creative gate | :496 | vanilla `invulnerable` | DIVERGES | ENT-S-109 class, unlisted site |
| PEACEFUL gate | none | vanilla player refusal | MATCH (engine, P6) | inert (re-rated 2026-09-04, T9: was PORT-ONLY — no port-written code, the engine's own `canAttack` refusal, rated MATCH (engine, P6) on every other Monster row per V13; nothing to delete, never add a `canAttack` override) |
| allies / species exclusions | EntityEnderman, EnderKnight, EnderReaper, EntityCreeper, Scorpion, EmperorScorpion (:476-493) | none | FIX IN FLIGHT (ENT-S-108) | — |
| ignore screen | :473, after sight | :81 predicate | FIXED (ENT-S-106) | — |
| tie-break / selection rule | sorter (:64, :508), first suitable (:512-517) | nearest player `<` | DIVERGES | — |
| target set / release | stored target (from `EntityAIHurtByTarget` :71 or hurt-by-`EntityLiving` :389-393) cleared when dead (:410-413) or on world-rand 1-in-100 inside the 1-in-4 pass (:414-416 ≈ 1-in-400 per tick); scan pick never stored (:418) | goal-stored (:80); hurt-by-`Mob` → `setTarget` + `moveTo` (:163-166); `EmperorScorpionPoisonGoal` (BugMelee, `Params.emperorScorpion` forget 100 :61) drops it on 1-in-100 EVERY tick (:123-128); `TargetGoal` release rules | FIXED (ENT-S-129, wave 2)` — the 1-in-100 (`== 0`) inside the 1-in-4 pass on the stored target, final; the convention — was DIVERGES | forget cadence ≈ 4× orig; the scan pick is held by the goal |
| other | 1-in-20 Scorpion spawned toward the target (:437-439) | :186-191 on `getTarget()` | MATCH | — |

### EnderKnight — orig `EnderKnight.java`, port `EnderKnight.java` — lane S2
| aspect | 1.7.10 (file:line) | port HEAD (file:line) | status | note |
|---|---|---|---|---|
| scan set | legacy non-AI EntityMob (no `isAIEnabled` override, no task lists :35-39): `func_70782_k` (:61-81) runs every tick the legacy loop holds no target (`td.bq`, shared refs): nearest player of ANY mode within 64 by `World.getClosestPlayerToEntity` (:65; `ahb.a(sa,D)` → `a(DDDD)`: no creative or alive check, strict `<`) | vanilla `NearestAttackableTargetGoal<>(this, Player.class, true)` (:47): every player within FOLLOW_RANGE 64 (:57), ≈ 1-in-10 cadence, mob sight | DIVERGES | scan-set: one nearest-of-any-mode candidate per tick vs nearest eligible player ≈ every 10 ticks |
| filter order | PlayNicely (:62) → nearest player (:65) → `shouldAttackPlayer` (:83-93): no pumpkin helmet (:84-87), player's look vector within `1 − 0.025/d` of the knight's mid-height (:88-91), player `canEntityBeSeen(knight)` (:92) → screaming on + return; else stareTimer reset, screaming off (:77-78) | vanilla chain only: self → spectator/dead → (no selector) → `canAttack` (PEACEFUL / creative) → range 64 → mob sight | DIVERGES | the enderman stare gate and the pumpkin exclusion are absent — any visible player is hunted |
| PlayNicely gate | :62 | none (:40-48) | FIXED (ENT-S-115, wave 1) — was DIVERGES | — |
| creative gate | none in the pick; the legacy loop drops a creative `EntityPlayerMP` target the same tick (`td.bq` :155-182), so a creative starer is picked (screaming toggles :74) then dropped, shadowing a farther survival player | creative skipped inside the scan (`invulnerable`, shared refs) | DIVERGES | shadowing lost (the Kraken KT-A pattern) and `invulnerable` for `isCreative` |
| PEACEFUL gate | none | vanilla player refusal | MATCH (engine, P6) | inert (re-rated 2026-09-04, T9: was PORT-ONLY — no port-written code, the engine's own `canAttack` refusal, rated MATCH (engine, P6) on every other Monster row per V13; nothing to delete, never add a `canAttack` override) |
| allies / species exclusions | none | none | MATCH | — |
| ignore screen | none | none | MATCH | — |
| tie-break / selection rule | the single nearest player (strict `<`, first wins ties), then tested — a nearer non-staring player blocks a farther starer | nearest ELIGIBLE player (`<`) | DIVERGES | nearest-then-filter vs filter-then-nearest |
| target set / release | `entityToAttack` set by the pick and by `EntityMob.attackEntityFrom` for any non-rider attacker (`yg.a(ro,F)`, shared refs); cleared when dead (`td.bq`), when creative (`td.bq`), and by the daylight roll (:111-115 `field_70789_a = null`) | goal-stored; `HurtByTargetGoal` (:46); `TargetGoal` release (dead / > 64 / unseen 30 evaluated ticks); no daylight target drop (`aiStep` :75-96 has none) | FIXED (ENT-S-129, wave 2)` — the daylight roll; both goals on the legacy hold — was DIVERGES | daylight release missing; unseen-ticks release port-only |
| other | stare-driven teleports: a staring target within distSq < 16 → `teleportRandomly`, a target > 256 away for 30 ticks → `teleportToEntity` (:124-138); screaming set by the pick (:74, :78) | none (:75-96); screaming only from `hurt` (:108) | DIVERGES | chase behaviour, not selection; listed for the split |

### EnderReaper — orig `EnderReaper.java`, port `EnderReaper.java` — lane S2
| aspect | 1.7.10 (file:line) | port HEAD (file:line) | status | note |
|---|---|---|---|---|
| scan set | legacy non-AI as EnderKnight: `func_70782_k` (:61-81) every target-less tick, nearest player of any mode within 81 (:65) | vanilla `NearestAttackableTargetGoal<>(this, Player.class, 10, true, false, pred)` (:50-51): players within FOLLOW_RANGE 81 (:82), ≈ 1-in-10 cadence | DIVERGES | geometry matches (81); cadence and candidate set differ |
| filter order | PlayNicely (:62) → nearest (:65) → `shouldAttackPlayer` (:83-93: pumpkin, look vector, player-side `canEntityBeSeen`) | vanilla chain with predicate `shouldAttackPlayer` (:60-73: carved pumpkin :61-63, look vector :65-71, `player.hasLineOfSight(this)` :72) → `canAttack` (PEACEFUL / creative) → range 81 → mob-side sight (`mustSee`) | MATCH | stare/pumpkin chain present; the added mob-side ray is the same eye-to-eye trace |
| PlayNicely gate | :62 | none (:41-52) | FIXED (ENT-S-115, wave 1) — was DIVERGES | — |
| creative gate | none in the pick; legacy loop drops a creative target the same tick (`td.bq` :155-182) — a creative starer shadows a farther survival starer | creative skipped inside the scan (`invulnerable`) | DIVERGES | shadowing lost; `invulnerable` for `isCreative` |
| PEACEFUL gate | none | vanilla player refusal | MATCH (engine, P6) | inert (re-rated 2026-09-04, T9: was PORT-ONLY — no port-written code, the engine's own `canAttack` refusal, rated MATCH (engine, P6) on every other Monster row per V13; nothing to delete, never add a `canAttack` override) |
| allies / species exclusions | none | none | MATCH | — |
| ignore screen | none | none | MATCH | — |
| tie-break / selection rule | the single nearest player, then the stare test | nearest STARING eligible player (`<`) | DIVERGES | nearest-then-filter vs filter-then-nearest |
| target set / release | `entityToAttack` from the pick / `EntityMob.attackEntityFrom`; cleared when dead, creative, or by the daylight roll (:111-115) | goal-stored; `HurtByTargetGoal` (:47); daylight roll → `setTarget(null)` (:121-130); `TargetGoal` release (dead / > 81 / unseen 30 evaluated ticks) | FIXED (ENT-S-129, wave 2)` — both goals on the legacy hold (no range, no unseen release) — was DIVERGES | unseen-ticks release is port-only but of the same class as orig's sight-gated attack — **status corrected MATCH → DIVERGES by the spot-check refuter (2026-09-04):** orig's legacy loop holds `entityToAttack` until dead / creative / daylight (:111-115); HEAD's goal + `TargetGoal` drops it beyond 81 blocks and after 30 evaluated unseen ticks, and `HurtByTargetGoal` (:47) after 300 — the EnderKnight row counts the identical release as DIVERGES; batched in T5 |
| other | stare-driven teleports (:124-138); screaming set by the pick (:74, :78) | absent (:100-139); screaming only from `hurt` (:151) | DIVERGES | ENT-D-020's "all ported" claim does not hold at HEAD for :126-131; chase, not selection |

### EntityButterfly — orig `EntityButterfly.java`, port `EntityButterfly.java` — lane S2
| aspect | 1.7.10 (file:line) | port HEAD (file:line) | status | note |
|---|---|---|---|---|
| scan set | `EntityLivingBase` bb `expand(8,5,8)` (:218) in `func_70619_bc`'s else-branch: entity-rand 1-in-10 && dimension == `DimensionID4` ("Dimension-Islands", `WorldProviderOreSpawn4.java:22-23`) && `butterfly_type == 1` (the `vbutterfly1.png` skin :45) && !PEACEFUL (:161) | none — `AmbientCreature` with `AmbientFlightGoal` only (:40, :65-66); no targeting code in the class | FIXED (ENT-S-117, wave 2) — `ButterflyIslandsHuntGoal` (entity/ai, new) registered at EntityButterfly :73; hunt branch :62-73, scan :97-103, filter :111-117, bite EntityButterfly :159-164 — was DIVERGES | scan set removed: the Islands-dimension vampire-type hunt is gone. The port's `VampireButterfly` (MOD-021 optional wiki mob; its javadoc :36-41 records "zero references … in reference_1_7_10_source") is a separate `Monster` with a vanilla player goal (:80-81), not this branch |
| filter order | :194-215 PEACEFUL → null → self → dead → sight (:207) → player: `!creative` (:210-213) → EntityHorse (:214) | N/A — no hunt | N/A | see scan set |
| PlayNicely gate | none (:217-230) | N/A | N/A | orig had no gate here |
| creative gate | :212 | N/A | N/A | see scan set |
| PEACEFUL gate | :161 caller + :195 filter + :187 in `attackEntityAsMob` | N/A | N/A | see scan set |
| allies / species exclusions | prey = non-creative players and horses only | N/A | N/A | see scan set |
| ignore screen | none | N/A | N/A | — |
| tie-break / selection rule | sorter (:56, :219), first suitable | N/A | N/A | see scan set |
| target set / release | never stored; flight target := prey (:165), bite when distSq < 6 on a 1-in-2 roll (:166-167, :183-191) | N/A | N/A | see scan set |
| other | — | — | N/A | one divergence counted (scan set) |

### EntityCannonFodder — orig `EntityCannonFodder.java`, port `EntityCannonFodder.java` — lane S2
| aspect | 1.7.10 (file:line) | port HEAD (file:line) | status | note |
|---|---|---|---|---|
| scan set | `EntityLivingBase` bb `expand(10,4,10)` (:316) in `func_70619_bc` when `is_activated == 2` (:349), !PEACEFUL and world-rand 1-in-`pfreq` (5; VelocityRaptor 4 :363-366) (:367) | `LivingEntity` `inflate(10,4,10)` (:254-255) in `customServerAiStep` when activated == 2 (:273), !PEACEFUL and entity-rand 1-in-5 (:284) | MATCH | the VelocityRaptor 1-in-4 row belongs to that class (port comment :275-277); rand stream only |
| filter order | :272-313 PEACEFUL → null → self → dead → sight (:288) → sitting: > 144 from the patrol point → false (:291) → EntityMob true (:294) → other fodder with a different non-zero hat true (:297-301) → player: `!creative`, not `name_one`/`name_two` (:302-311) → false | :220-251 PEACEFUL → null/self/dead → sight (:226) → sit leash (:228-234) → Monster (:235) → rival hat (:236-239) → player: `!instabuild`, not the trusted UUIDs (:240-249) → false | MATCH | — |
| PlayNicely gate | none (:315-328) | none (:253-259) | MATCH | — |
| creative gate | :304 | :241 `instabuild` | MATCH | — |
| PEACEFUL gate | :276 filter + :367 caller | :221 + :284 | MATCH | — |
| allies / species exclusions | same-hat / hatless fodder spared (:297-301); the two owners (:307-310) | :236-247 | MATCH | — |
| ignore screen | none | none | MATCH | — |
| tie-break / selection rule | `LocalTargetSorter` (:42, :317), first suitable | `GenericTargetSorter` (:51) via `firstMatch` (:258) | MATCH | — |
| target set / release | scan pick never stored (:368-373); revenge target cleared on world-rand 1-in-200 (:346-348 `func_70604_c(null)`) | pick not stored (:285-291); `setTarget(null)` 1-in-200 (:272) | FIXED (ENT-S-129, wave 2)` — `setLastHurtByMob(null) — was DIVERGES | release rule: clears the attack target where orig cleared the revenge (last-hurt-by) target |
| other | — | — | MATCH | — |

### Fairy — orig `Fairy.java`, port `Fairy.java` — lane S2
| aspect | 1.7.10 (file:line) | port HEAD (file:line) | status | note |
|---|---|---|---|---|
| scan set | `EntityLivingBase` bb `expand(8,8,8)` (:242) in `func_70619_bc`'s else-if: world-rand 1-in-12 && !PEACEFUL when the 1-in-200 / near flight retarget did not fire (:270, :288) | `LivingEntity` `inflate(8,8,8)` (:152-153, `FLIGHT_SEARCH_RANGE` :68) in `customServerAiStep`'s else-if: 1-in-12 && !PEACEFUL (:169, :182) | MATCH | — |
| filter order | :219-236 PEACEFUL → null → self → dead → sight (:232) → EntityMob (:235) | :145-149 PEACEFUL → null/self/dead → Monster | FIXED (ENT-S-118, wave 2) — was DIVERGES | the sight step is missing |
| PlayNicely gate | :239 | none (:151-157) | FIXED (ENT-S-115, wave 1) — was DIVERGES | — |
| creative gate | N/A (:235 EntityMob only) | N/A (:148) | N/A | — |
| PEACEFUL gate | :220 filter + :288 caller | :146 + :182 | MATCH | — |
| allies / species exclusions | prey = EntityMob only (:235) | Monster only (:148) | MATCH | — |
| ignore screen | none | none | MATCH | — |
| tie-break / selection rule | sorter (:63, :243) | plain nearest (:90, :156) | DIVERGES | — |
| target set / release | never stored; flight target := prey (:292); bite when distSq < 6 (:293-294) | :185-189 same (`MELEE_RANGE_SQR` 6 :69) | MATCH | — |
| other | owner-follow branch sits after the hunt roll (:297-304) | :191-200 | MATCH | — |

### Frog — orig `Frog.java`, port `Frog.java` — lane S2
| aspect | 1.7.10 (file:line) | port HEAD (file:line) | status | note |
|---|---|---|---|---|
| scan set | `EntityLivingBase` bb `expand(8,3,8)` (:311) in `func_70619_bc`: entity-rand 1-in-12 && !PEACEFUL (:261) | `inflate(8,3,8)` (:257-258) in `customServerAiStep`: 1-in-12 && !PEACEFUL (:243-244) | MATCH | — |
| filter order | :273-305 PEACEFUL → null → self → dead → sight (:286) → EntityAnt, EntityButterfly, Cricket, EntityMosquito, Firefly, WormSmall true (:289-304) | :265-275 self → dead → sight → EntityAnt, EntityButterfly, EntityCricket, EntityMosquito, Firefly, EntityWormSmall | MATCH | in-filter PEACEFUL absent but the caller gate (:244) covers it; null step absent (the list never holds null) |
| PlayNicely gate | :308 | none (:256-263) | FIXED (ENT-S-115, wave 1) — was DIVERGES | — |
| creative gate | N/A | N/A | N/A | — |
| PEACEFUL gate | :261 + :274 | :244 | MATCH | — |
| allies / species exclusions | prey whitelist of six (:289-304) | the same six (:269-274) | MATCH | — |
| ignore screen | none | none | MATCH | — |
| tie-break / selection rule | sorter (:55, :312) | plain nearest (:262) | DIVERGES | — |
| target set / release | never stored; navigate + bite when distSq < 6 (:265-268) | :247-250 | MATCH | — |
| other | — | — | MATCH | — |

### GammaMetroid — orig `GammaMetroid.java`, port `EntityGammaMetroid.java` — lane S2
| aspect | 1.7.10 (file:line) | port HEAD (file:line) | status | note |
|---|---|---|---|---|
| scan set | `EntityLivingBase` bb `expand(10,3,10)` (:297) in `func_70619_bc`: !PEACEFUL && world-rand 1-in-5 (:241); child → null (:294) | `inflate(10,3,10)` (:201-202) in `customServerAiStep`: entity-rand 1-in-5 (:112); baby / tame → null (:199-200) | MATCH | the missing caller-side PEACEFUL gate is counted in the PEACEFUL row |
| filter order | :253-288 PEACEFUL → null → self → dead → `isIgnoreable` (:266) → sight (:269) → GammaMetroid (:272) → EntityMob (:275) → tamed self → false (:278) → player creative (:281-285) | :208-219 null/self/dead → `isIgnoreable` (:210) → sight (:211) → EntityGammaMetroid (:212) → Monster (:213) → tame (:214) → player creative (:215-217) | MATCH | minus the PEACEFUL step (PEACEFUL row) |
| PlayNicely gate | :291 | none (:198-206) | FIXED (ENT-S-115, wave 1) — was DIVERGES | — |
| creative gate | :283 | :216 `invulnerable` | FIX IN FLIGHT (ENT-S-109) | — |
| PEACEFUL gate | :241 caller + :254 filter | none | FIXED (ENT-S-114, wave 1) — was DIVERGES | a TamableAnimal persists in peaceful — an untamed metroid hunts there |
| allies / species exclusions | own kind (:272), EntityMob (:275) | own kind (:212), Monster (:213) | MATCH | — |
| ignore screen | :266, ahead of sight | :210 | FIXED (ENT-S-106) | — |
| tie-break / selection rule | sorter (:59, :298) | plain nearest (:205) | DIVERGES | — |
| target set / release | never stored (:241-250); `EntityAIHurtByTarget` (:67) target inert (no attack goal) | pick not stored (:113-123); `HurtByTargetGoal` (:84) plus port-only `OwnerHurtByTargetGoal`, `OwnerHurtTargetGoal` (:82-83) and `NearestAttackableTargetGoal<Monster>` when tame (:85) set `target`; no goal consumes it | RECORDED (MOD-033, ENT-S-125) — the owner pair and the tame hunt register only in modern (registered-but-unconsumed); classic = HurtBy only (orig :67) — was PORT-ONLY | a tamed metroid in orig never targets anything (:278) |
| other | — | — | MATCH | — |

### GiantRobot — orig `GiantRobot.java`, port `GiantRobot.java` — lane S2
| aspect | 1.7.10 (file:line) | port HEAD (file:line) | status | note |
|---|---|---|---|---|
| scan set | `EntityLivingBase` bb `expand(16,12,16)` (:346) in `func_70619_bc` on world-rand 1-in-5 (:241), only when no live stored target (:250-252) | `inflate(16,12,16)` (:224-225) in `customServerAiStep` on entity-rand 1-in-5 (:135), only when no stored target (:142) | MATCH | — |
| filter order | :314-340 null → self → dead → `isIgnoreable` (:324) → sight (:327) → EntityMob (:330) → player creative (:333-337) | :232-238 null/self/dead → `isIgnoreable` (:234) → Monster (:235) → player `instabuild` (:236) | FIXED (ENT-S-118, wave 2) — was DIVERGES | the sight step is missing |
| PlayNicely gate | :343 | none (:223-230) | FIXED (ENT-S-115, wave 1) — was DIVERGES | — |
| creative gate | :335 | :236 `instabuild` | MATCH | — |
| PEACEFUL gate | none | none | MATCH | — |
| allies / species exclusions | EntityMob (:330) | Monster (:235) | MATCH | — |
| ignore screen | :324 | :234 | FIXED (ENT-S-106) | — |
| tie-break / selection rule | sorter (:51, :347) | `GenericTargetSorter` (:63) via `firstMatch` (:229) | MATCH | TF-035 rider (ENT-D-043) |
| target set / release | stored target cleared 1-in-100 (:243-245) BEFORE it is read (:246); dead → cleared (:246-249); scan pick never stored (:251); hurt-by-`EntityLiving` → `setAttackTarget` (:307-310); `EntityAIHurtByTarget` (:58) | the target is read (:136) BEFORE the 1-in-100 clear (:137), so the cleared target is still engaged this pass; dead → cleared (:138-141); pick not stored (:142); hurt-by-`Mob` → `setTarget` (:217-218); `HurtByTargetGoal` (:85) | FIXED (ENT-S-129, wave 2)` — the clear ahead of the read, final — was DIVERGES | release rule: one-pass ordering of the 1-in-100 drop |
| other | — | — | MATCH | — |

### Godzilla — orig `Godzilla.java`, port `Godzilla.java` — lane S2
| aspect | 1.7.10 (file:line) | port HEAD (file:line) | status | note |
|---|---|---|---|---|
| scan set | `EntityLivingBase` bb `expand(64,40,64)` (:534) in `func_70619_bc` on world-rand `nextInt(5 − large_unknown_detected) == 1` (:355), only when the stored target is null or dropped (:369-370) | `inflate(64,40,64)` (:612-613) on entity-rand `nextInt(max(1, 5 − largeUnknownDetected)) == 1` (:729), only when no stored target (:745-746) | MATCH | — |
| filter order | villager pass first — `isVillagerTarget` (:481-495: null → self → dead → sight → EntityVillager); a sighted villager anywhere in sorter order replaces an earlier pick (:547-550); otherwise `isSuitableTarget` (:432-479): null → self → dead → `isIgnoreable` (:442) → sight (:445) → Godzilla, GodzillaHead, EntityCreeper, EntityZombie, EntitySpider, EntitySkeleton, Ghost, GhostSkelly (:448-471) → player creative (:472-476) | villager pass (:620-624: Villager && alive && sight) with the same precedence (:618-628); `isSuitableTarget` (:582-603): null/self/dead → `isIgnoreable` (:584) → sight (:585) → the same eight (:586-593) → Mothra (:597) → `isBigBoss` / `isRoyalty` (:598) → player `instabuild` (:599-601) | MATCH | order matches; the extra names are counted in the allies row |
| PlayNicely gate | :524-527 scan gate (also fakes `head_found = 1`); :357-359 nulls the LOCAL `e` each pass — the stored target is untouched | :608-611 same scan gate; :732-734 `setTarget(null)` each pass — clears the STORED target | FIXED (ENT-S-115, wave 1) — the pass's local is nulled, the stored target kept; BOSS-017's stored clear is gone — was DIVERGES | BOSS-017 mapped the local null to a stored clear; visible only with PlayNicely on (the HurtBy target is dropped instead of merely skipped) |
| creative gate | :474 | :600 `instabuild` | MATCH | — |
| PEACEFUL gate | none | none | MATCH | — |
| allies / species exclusions | Godzilla, GodzillaHead, EntityCreeper, EntityZombie, EntitySpider, EntitySkeleton, Ghost, GhostSkelly (:448-471) | the same eight (:586-593) + Mothra (:597) + `isBigBoss` (Godzilla, GodzillaHead, PitchBlack, Kraken; util :75-79) + `isRoyalty` (TheKing, TheQueen, KingHead, QueenHead, ThePrince, ThePrinceAdult, ThePrincess, ThePrinceTeen, PurplePower; util :9-19) (:598) | RECORDED (MOD-032, ENT-S-125) — the boss-peer refusals apply only while `modern.enabled && godzillaSparesBossPeers` (read live); classic = the orig eight refusals; the Mothra line is redundant in both modes (the ignore screen refuses EntityButterfly first) — was PORT-ONLY | unrecorded ("Don't pick fights with peers" :594-596; no AUDIT / FIX / MOD entry) — Mothra, PitchBlack, the Kraken and the royals were prey in 1.7.10 |
| ignore screen | :442, ahead of sight | :584 | FIXED (ENT-S-106) | — |
| tie-break / selection rule | sorter (:85, :538); first villager in sorter order beats everything, else first suitable (:541-553) | `entities.sort(targetSorter)` (:614, `GenericTargetSorter` :123), same loop (:618-628) | MATCH | TF-035 rider (FIX_LOG.md:2046) |
| target set / release | stored target (`EntityAIHurtByTarget` :84; hurt :784-793: any living attacker except Godzilla / GodzillaHead → `setAttackTarget` + navigate) dropped when dead or own kind (:361-367); scan pick never stored (:370) | :737-743 same drops; hurt :858-868 same; pick not stored (:746) | MATCH | — |
| other | the scan's `head_found` side effect (:540-546) spawns MobzillaHead (:371-372) | :617-619, :747-752 | MATCH | — |

### Hammerhead — orig `Hammerhead.java`, port `Hammerhead.java` — lane S2
| aspect | 1.7.10 (file:line) | port HEAD (file:line) | status | note |
|---|---|---|---|---|
| scan set | `EntityLivingBase` bb `expand(18,9,18)` (:255) in `func_70619_bc` on world-rand 1-in-3 (:191), only when the revenge target `rt` is unusable (:193-208) | `level().getNearestPlayer(this, 18.0)` (:129): players only, sphere 18 from the shark's position, spectators excluded (`EntityGetter` :96-104); only when neither `revengeTarget` nor `getTarget()` is set (:118-133); entity-rand 1-in-3 (:117) | DIVERGES | scan-set narrowing (living box → player sphere) — the ENT-S-108 class, but Hammerhead is not on that record |
| filter order | :225-249 null → self → dead → sight (:235) → Hammerhead false (:238) → player: `!creative` (:241-244) → EntityMob true (:245) → `MyUtils.isAttackableNonMob` (:248; orig list, shared refs) | none — `!instabuild` only (:130) | DIVERGES | sight, own-kind and the mob / non-mob prey rules absent |
| PlayNicely gate | :194-196 (nulls `rt` for the pass) and :252 (scan) | none (:112-150) | FIXED (ENT-S-115, wave 1) — both sites (:194 rt blank, :252 scan); the port-only getTarget() fallback gated with the pass (refuter B2) — was DIVERGES | — |
| creative gate | :243 | :130 `instabuild` | MATCH | — |
| PEACEFUL gate | none | none | MATCH | — |
| allies / species exclusions | prey = EntityMob + the orig `isAttackableNonMob` list + non-creative players; Hammerhead spared (:238) | players only | DIVERGES | the port's shared `isAttackableNonMob` (util :54-63) is a different list (EnderDragon, Kraken, Godzilla, GodzillaHead, Basilisk, Cephadrome, TheKing, TheQueen) — unused here today but what a restore would inherit (ENT-S-110's Leon fix shape names the same helper) |
| ignore screen | none | none | MATCH | — |
| tie-break / selection rule | sorter (:48, :256) | nearest player, strict `<` (`EntityGetter` :78-93) | DIVERGES | — |
| target set / release | `rt` := any living attacker (:178-181); dropped when dead or on world-rand 1-in-250 (:198-201) and skipped when out of sight this pass (:202-204); scan pick never stored (:207-218) | `revengeTarget` := living attacker (:105-108); dropped when dead / 1-in-250 (:120-123); no out-of-sight skip; falls back to `getTarget()` from `HurtByTargetGoal` (:59) (:125-127); scan pick not stored (:128-133) | FIXED (ENT-S-129, wave 2)` — the out-of-sight skip; the inert revenge task unregistered (the port-only fallback kept for the ENT-S-115 pin, empty in play) — was DIVERGES | release rule: the out-of-sight skip is missing |
| other | attack dice 1-in-3 OR 1-in-4 (:213) | 1-in-3 only (:140) | DIVERGES | attack cadence, not selection; listed for the split |

### HerculesBeetle — orig `HerculesBeetle.java`, port `EntityHerculesBeetle.java` — lane S2
| aspect | 1.7.10 (file:line) | port HEAD (file:line) | status | note |
|---|---|---|---|---|
| scan set | `EntityLivingBase` bb `expand(16,6,16)` (:420) in `func_70619_bc` on world-rand 1-in-4 (:350), only when no live stored target (:351-358) | vanilla `NearestAttackableTargetGoal<>(this, Player.class, true, pred)` (:60-61): players within FOLLOW_RANGE 24 (:71), ≈ 1-in-10 | FIX IN FLIGHT (ENT-S-108) | box 16×6×16 → radius 24 |
| filter order | :385-414 null → self → dead → `isIgnoreable` (:395) → sight (:398) → EntityCreeper (:401) → HerculesBeetle (:404) → player creative (:407-411) | vanilla chain with `!isIgnoreable` predicate (:61) → creative / PEACEFUL → range → sight | FIXED (ENT-S-108; pinned by ENT-S-128, wave 2) — EntityHerculesBeetle.java:251-252 — was DIVERGES | exclusions absent |
| PlayNicely gate | :417 | none (:50-62, :150-157) | FIXED (ENT-S-108; pinned by ENT-S-115, wave 1) — gate at EntityHerculesBeetle.java:234 — was DIVERGES | — |
| creative gate | :409 | vanilla `invulnerable` | DIVERGES | ENT-S-109 class, unlisted site |
| PEACEFUL gate | none | vanilla player refusal | MATCH (engine, P6) | inert (re-rated 2026-09-04, T9: was PORT-ONLY — no port-written code, the engine's own `canAttack` refusal, rated MATCH (engine, P6) on every other Monster row per V13; nothing to delete, never add a `canAttack` override) |
| allies / species exclusions | EntityCreeper, HerculesBeetle (:401-406) | none | FIX IN FLIGHT (ENT-S-108) | — |
| ignore screen | :395, ahead of sight | :61 predicate | FIXED (ENT-S-106) | — |
| tie-break / selection rule | sorter (:51, :421) | nearest player `<` | DIVERGES | — |
| target set / release | stored target (`EntityAIHurtByTarget` :57; hurt-by-`EntityLiving` → `setAttackTarget` + navigate :331-335) cleared when dead (:352-355); scan pick never stored (:357) | goal-stored (:60); hurt-by-`Mob` → `setTarget` + `moveTo` (:141-145); `BugMeleeAttackGoal` `forgetTargetRoll 0` (`Params.herculesBeetle` :62); `TargetGoal` release | FIXED (ENT-S-129, wave 2)` — the convention (the pick transient through the re-assert window) — was DIVERGES | the scan pick is held by the goal |
| other | — | — | MATCH | — |

### Irukandji — orig `Irukandji.java`, port `Irukandji.java` — lane S2
| aspect | 1.7.10 (file:line) | port HEAD (file:line) | status | note |
|---|---|---|---|---|
| scan set | `EntityLivingBase` bb `expand(6,4,6)` (:294) in `func_70619_bc` on world-rand 1-in-8 (:253); a live stored target pre-empts the scan (:299-302) | `getNearestPlayer(this, 6.0)` (:136): players only, sphere 6, spectators excluded; only when `getTarget()` is null (:134-135); entity-rand 1-in-8 (:133) | DIVERGES | orig's filter accepted only players (:283-287), so the class is equal; the geometry differs (box ±6 / ±4 → sphere 6) |
| filter order | :270-288 null → self → dead → sight (:280) → player: `!creative` (:283-286) → false | `!instabuild` (:137) | FIXED (ENT-S-118, wave 2) — the sight term in the inline pick, ahead of !instabuild; the sphere-vs-box geometry stays the scan-set row — was DIVERGES | the sight step is missing |
| PlayNicely gate | :291 | none (:105-155) | FIXED (ENT-S-115, wave 1) — the inline pick gated as a whole — was DIVERGES | — |
| creative gate | :285 | :137 `instabuild` | MATCH | — |
| PEACEFUL gate | none | none | MATCH | — |
| allies / species exclusions | players only (:283-287) | players only | MATCH | — |
| ignore screen | none | none | MATCH | — |
| tie-break / selection rule | sorter (:47, :295) | nearest `<` | DIVERGES | for standing players the sorter reduces to nearest (uniform 1.08 silhouette); sneaking players (0.9, undivided) rank differently |
| target set / release | stored target (`EntityAIHurtByTarget` :52; hurt-by-`EntityLiving` except Irukandji :146-151) preferred while alive (:299-302), cleared when dead (:303); scan pick never stored (:304-309) | stored target preferred (:134); the scan pick IS stored via `setTarget` (:139); a dead stored target is never cleared here (:142 → :151-152 `setAttacking(0)`), and no goal owns a scan-set target, so re-scanning stops until the jelly is next hurt (:98-99 / `HurtByTargetGoal` :62) | FIXED (ENT-S-129, wave 2)` — the pick under the mark, re-derived; a dead stored target cleared — was DIVERGES | release rule: a dead scan target sticks |
| other | attack dice 1-in-4 OR 1-in-5 (:258) | 1-in-4 only (:145) | DIVERGES | attack cadence, not selection; listed for the split |

### Kraken — orig `Kraken.java`, port `Kraken.java` — lane S2
| aspect | 1.7.10 (file:line) | port HEAD (file:line) | status | note |
|---|---|---|---|---|
| scan set | in `updateAITasks` when `caught == null && world-rand 1-in-8 && PlayNicely == 0` (:961): (1) nearest `EntityPlayer` of any mode in bb `expand(25,40,25)` via `World.func_72857_a` (:963); (2) if that ended null, 1-in-2 (:974) → `findSomethingToAttack`: `EntityLivingBase` bb `expand(20,40,20)` (:1134) | same gate (:273-274, entity-rand) → `searchForPrey` (:371-396): (1) `findNearestPlayer` `inflate(25,40,25)` (:526-539); (2) 1-in-2 (:386) → `inflate(20,40,20)` (:695-697) | FIXED (ENT-S-100) | KT-A; rand stream RECORDED (KT-C) |
| filter order | (1) creative → null (:965, :970-972) → sight (:966) → grab (:967-968); (2) :1060-1128 null → self → dead → `isIgnoreable` (:1070) → sight (:1073) → player: `!creative`, `!isFlying` (:1076-1082) → onGround or inWater (:1083) → EntitySquid, AttackSquid, Kraken, Spyro, ridden Dragon / Cephadrome / Leon / PrinceTeen / PrinceAdult, Chicken, Chipmunk, StinkBug, Mothra (:1086-1127) | (1) :373-384; (2) :663-685 in the same order | FIXED (ENT-S-100) | KT-B1 / KT-B2 |
| PlayNicely gate | :961 (branch), :1131 (scan) | :274, :692 | MATCH | — |
| creative gate | :965, :1078 `isCreativeMode`; :1081 `isFlying` | :374, :668 `instabuild`; :669 `flying` | FIXED (ENT-S-100) | KT-A / KT-B2 |
| PEACEFUL gate | none | none | MATCH | — |
| allies / species exclusions | the fourteen names of :1086-1127 incl. the five ridden mounts | :672-684 | FIXED (ENT-S-100) | — |
| ignore screen | :1070, ahead of sight | :665 | FIXED (ENT-S-100) | — |
| tie-break / selection rule | (1) `func_72857_a` replaces on `<=` — the LAST equidistant player wins; (2) sorter (:81, :1135), first suitable (:1139-1144) | (1) `<=` (:533); (2) `GenericTargetSorter` (:91) via `firstMatch` (:700) | FIXED (ENT-S-105) | — |
| target set / release | no attack target; `caught` held while `!isDead` (:983-984), released above y 190 (:986-988) and by the 1-in-50 / 1-in-250 rolls; hurt: player attacker with health > max/4 → `hit_by_player` + flight target (:1154-1157), then 1-in-2 `release` (:1163-1165) | `handleCaughtEntity` holds while `!isRemoved()` (KT-D, ENT-S-100); hurt :565-570, :582-584 | FIXED (ENT-S-100) | KT-D faithful; KT-E max-health base |
| other | the grab force-moves the victim each tick (:989-998) | same (MOD-004 proposal unimplemented) | MATCH | MOD-004 is a proposal only |

### KrakenRepellent — orig `KrakenRepellent.java`, port `block/RepellentBlock.java` — lane S2
| aspect | 1.7.10 (file:line) | port HEAD (file:line) | status | note |
|---|---|---|---|---|
| all | N/A — a `BlockTorch` subclass (:21-22), not a hunter: on its 10-tick block tick (:58-60, :62-67) it lists `EntityLivingBase` in a 40×50×40 box (x ± 20, y −10..+40, z ± 20 :81-82) and pushes Krakens (:93-108) and EntityAnts (:109-123) away by 0.4 × (20 − distance) | `RepellentBlock` / `WallRepellentBlock` — no target selection to compare | N/A | no hunter targeting in this species; out of this lane's scope |

### Kyuubi — orig `Kyuubi.java`, port `EntityKyuubi.java` — lane S2
| aspect | 1.7.10 (file:line) | port HEAD (file:line) | status | note |
|---|---|---|---|---|
| scan set | `EntityLivingBase` bb `expand(12,4,12)` (:208) in `func_70619_bc` on world-rand 1-in-10 (:161) | `inflate(12,4,12)` (:132-133) in `customServerAiStep` on entity-rand 1-in-10 (:110) | MATCH | — |
| filter order | :173-202 null → self → dead → `isIgnoreable` (:183) → sight (:186) → EntityMob (:189) → EntityPigZombie (:192) → player creative (:195-199) | :139-148 null/self/dead → `isIgnoreable` (:141) → sight (:142) → Monster (:143) → player creative (:144-146) | MATCH | the PigZombie step is absent but ZombifiedPiglin is a Monster |
| PlayNicely gate | :205 | none (:131-137) | FIXED (ENT-S-115, wave 1) — was DIVERGES | — |
| creative gate | :197 | :145 `invulnerable` | FIX IN FLIGHT (ENT-S-109) | — |
| PEACEFUL gate | none | none | MATCH | — |
| allies / species exclusions | EntityMob, EntityPigZombie (:189-194) | Monster (:143) | MATCH | — |
| ignore screen | :183, ahead of sight | :141 | FIXED (ENT-S-106) | — |
| tie-break / selection rule | sorter (:56, :209) | plain nearest (:136) | DIVERGES | — |
| target set / release | scan pick never stored (:161-170); revenge target cleared on world-rand 1-in-200 (:157-159 `func_70604_c(null)`); `EntityAIHurtByTarget` (:55) target inert (no attack goal) | pick not stored (:111-126); `setTarget(null)` 1-in-200 (:106-108); `HurtByTargetGoal` (:61) | FIXED (ENT-S-129, wave 2)` — `setLastHurtByMob(null)` ahead of super — was DIVERGES | release rule: clears the attack target where orig cleared the revenge target |
| other | — | — | MATCH | — |

### LeafMonster — orig `LeafMonster.java`, port `EntityLeafMonster.java` — lane S3
| aspect | 1.7.10 (file:line) | port HEAD (file:line) | status | note |
|---|---|---|---|---|
| scan set | `getEntitiesWithinAABB(EntityLivingBase, bbox.expand(4,6,4))` LeafMonster.java:213 in `findSomethingToAttack`; called from `updateAITasks` :163 on `nextInt(4)==1` | `getEntitiesOfClass(LivingEntity, bbox.inflate(4,6,4))` EntityLeafMonster.java:145-146; `customServerAiStep` :126 `nextInt(4)==1` | MATCH | same box, cadence, class |
| filter order | :178-207 null, self, dead, sight (:188), EntityAnt→true (:191), EntityButterfly→true (:194), EntityLunaMoth→true (:197), Player non-creative→true (:200-205), else false | :155-165 null/self/dead (:156), sight (:157), Ant (:158), Butterfly (:159), LunaMoth (:160), Player (:161-163), false | MATCH | prey-list style, same order (LunaMoth extends Butterfly in both, redundant in both) |
| PlayNicely gate | :210-212 in `findSomethingToAttack` (`PlayNicely != 0` → null) | :144 `PLAY_NICELY.get()` → null | MATCH | |
| creative gate | :202 `field_75098_d` (isCreativeMode) | :162 `abilities.invulnerable` | FIX IN FLIGHT (ENT-S-109) | listed site LeafMonster.java:202 ↔ :162 |
| PEACEFUL gate | none (EntityMob despawns) | none (Monster despawns) | N/A | |
| allies / species exclusions | none (prey grants only: Ant, Butterfly, LunaMoth, players) | same | MATCH | |
| ignore screen | none (LeafMonster is not an `isIgnoreable` caller) | none | N/A | |
| tie-break / selection rule | :214 `Collections.sort(GenericTargetSorter)` (:36 field, :48 ctor) then first suitable | :151 `TargetSelection.firstMatch(entities, new GenericTargetSorter(this), isSuitableTarget)` | MATCH | |
| target set / release | nothing stored; prey used the same tick (:164-174, `setAttacking` 1/0); :160-162 1-in-100 `func_70604_c(null)` = **setRevengeTarget(null)** (clears the revenge memory that feeds `EntityAIHurtByTarget` :47); the goal's attackTarget is consumed by no goal (no EntityAIAttackOnCollide) | nothing stored; :122-124 1-in-100 **`setTarget(null)`** (clears the attack target set by `HurtByTargetGoal` :54, VAN-HBT) | FIXED (ENT-S-129, wave 2)` — `setLastHurtByMob(null) — was DIVERGES | release rule: orig forgets *who hurt it* (the ongoing revenge chase persists until `EntityAITarget.continueExecuting` fails), port drops *the current target* (and VAN-HBT will not re-fire on the old timestamp); inert in both trees — no goal consumes the attack target (no melee goal either side) |
| other | melee within distSq<5 on `nextInt(8)==0 \|\| nextInt(10)==1` (:169-171) | :132-134 same | MATCH | grid-snap when not attacking (:96-121 / :89-104) is movement, not targeting |

### Leon — orig `Leon.java`, port `EntityLeon.java` — lane S3
| aspect | 1.7.10 (file:line) | port HEAD (file:line) | status | note |
|---|---|---|---|---|
| scan set | custom: `EntityLivingBase, bbox.expand(20,20,20)` Leon.java:434; callers `fly_with_rider` :364 (1-in-7 :357, ridden, attackTarget first), `fly_without_rider` :600 (1-in-8 :599, `!toofar && unstick==0 && flyaway==0`), `always_do` :914 (1-in-10, activity==0, not sitting, no rider). Vanilla goal :92-93 `EntityAINearestAttackableTarget(this, EntityLiving.class, chance 0, sight, nearbyOnly=false, IMob.mobSelector)` registered only when `PlayNicely == 0` at construction: box follow-range(16 default; :112-118 sets no follow range) x4 x16, every tick, IMob EntityLivings only | custom: `LivingEntity, inflate(20,20,20)` EntityLeon.java:730-731; callers `flyWithRider` :497 (1-in-7 :489), `flyWithoutRider` :631 (1-in-8 :628-630), `alwaysDo` :527 (1-in-10 :523-526). Vanilla goal :158 `NearestAttackableTargetGoal<>(this, Monster.class, 10, true, false, e -> !isTame() \|\| getTarget()==null)`: VAN-NAT box `inflate(40, 4, 40)` (FOLLOW_RANGE 40 :170), 1-in-10 (reduced to 5 on the 2-tick cadence), always registered | custom MATCH; vanilla goal DIVERGES | goal path: box 16→40 (scan-set widening), chance 0 (every tick) → 1-in-10, class IMob→Monster, PORT-ONLY predicate `!isTame() \|\| getTarget()==null` |
| filter order | :387-428 PEACEFUL (:388), PlayNicely (:391), null (:394), self (:397), dead (:400), ignore (:403), sight (:406), Leon→false (:409), EntityMob→true (:412), Player: creative→false else `return !tamed` (:415-421), untamed && `isAttackableNonMob`→true (:422-426), false (:427) | :745-758 PEACEFUL (:746), null/self/dead (:747), ignore (:748), sight (:749), EntityLeon→false (:750), Monster→true (:751), Player: instabuild→false else `return !isTame()` (:752-755), `if (!isTame()) return true` (:756), false | FIX IN FLIGHT (ENT-S-110) | PlayNicely step (:391) absent; untamed rule "any living" instead of the `isAttackableNonMob` list; once restored, note the port list itself differs (header) |
| PlayNicely gate | :391-393 (filter) and :431-433 (scan) → nothing; goal registration gated at construction :92 | none in the custom path (:728-735, :745-758); none on the vanilla goal (:158) | FIXED (ENT-S-110 filter / ENT-S-115 goal, wave 1) — the goal-registration half as a live canUse; orig's scan-head gate :431-433 has no port line, the ENT-S-110 filter gate (:765) the scan calls is outcome-equivalent — was FIX IN FLIGHT (ENT-S-110); goal-registration gate DIVERGES | ENT-S-110 names the :391 gate; the :92 construction-time gate on the IMob goal is a separate missing gate |
| creative gate | :417 `field_75098_d` (isCreativeMode) | :753 `abilities.instabuild` | FIXED (ENT-S-107) | vanilla goal: orig `EntityAITarget` used `capabilities.disableDamage`, port VAN-TC `invulnerable` — moot on both (players are never `EntityLiving`/`Monster` candidates) |
| PEACEFUL gate | :388 (filter), :357 (ridden), :599 (unridden), :914 (always_do) | :746, :488, :629, :525 | MATCH | |
| allies / species exclusions | self (:409); grants: EntityMob (:412), untamed: `MyUtils.isAttackableNonMob` list (Mothra, Dragon, Spyro, royalty, GammaMetroid, Cephadrome, WaterDragon, Girlfriend, Boyfriend, Villager, Stinky) | self (:750); grants: Monster (:751), untamed: any LivingEntity (:756) | FIX IN FLIGHT (ENT-S-110) | see header for the port `isAttackableNonMob` membership drift |
| ignore screen | :403 after dead, before sight | :748 same position | FIXED (ENT-S-106) | |
| tie-break / selection rule | custom :435 `GenericTargetSorter` (:63 field, :97 ctor) then first suitable; vanilla goal: nearest by plain distanceSq (1.7.10 `EntityAINearestAttackableTarget.Sorter`) | custom :734 `Comparator.comparingDouble(this::distanceToSqr)` (plain distance); vanilla goal: VAN-NAT nearest, strict `<` | custom DIVERGES; goal MATCH | tie-break: creeper / silhouette weighting dropped in the custom scan (TF-035 migration not done for Leon) |
| target set / release | custom scan stores nothing; `fly_with_rider` reads `getAttackTarget()` first (:358), drops it when dead (:359-362); `updateAITasks` :340-342 1-in-200 `setAttackTarget(null)`; `attackEntityFrom` :318-331: Leon attacker → no damage/no target, `tamed && player` → damage taken but no target (:324-326), else `setAttackTarget(attacker)` + `setLastAttacker` + navigate 1.2 (:327-329); `EntityAIHurtByTarget(this,false)` :95 | `flyWithRider` :491-495 same; `customServerAiStep` :515-517 1-in-200 `setTarget(null)`; `hurt` :402-412: Leon attacker → false (:403), `isTame() && player` → **false before super.hurt** (:404), else `setTarget` + moveTo 1.2 (:409-412); `HurtByTargetGoal` :157 (VAN-HBT, 300-tick unseen memory, no sight); PORT-ONLY `OwnerHurtByTargetGoal` :155 and `OwnerHurtTargetGoal` :156 (orig registers no owner goals) | RECORDED (MOD-033, ENT-S-125) — the owner pair registers only in modern; classic = orig :92-95; the scan-set row's PORT-ONLY predicate rides with the same key — was PORT-ONLY (owner goals) | out of targeting scope but adjacent: orig :321 applies the damage to a tamed Leon hit by a player and only skips the retaliation; port :404 skips the damage too |
| other | tamed && health < 25% → flee target instead of attack (:602-607); sitting (:351) / rider (:914) gates; ridden bite reach 9+w/2 (:368), unridden 7+w/2 (:614) | :633-641 flee; :487 / :524 gates; :501-502, :648-649 reach | MATCH | goal predicate `getTarget()==null` (port-only) prevents the vanilla goal from replacing an existing target while tame |

### Lizard — orig `Lizard.java`, port `Lizard.java` — lane S3
| aspect | 1.7.10 (file:line) | port HEAD (file:line) | status | note |
|---|---|---|---|---|
| scan set | `EntityLivingBase, bbox.expand(12,4,12)` Lizard.java:339; `updateAITasks` :270 gated `difficulty != PEACEFUL && nextInt(10)==1` | `LivingEntity, inflate(12,4,12)` Lizard.java:147-148; `customServerAiStep` :211 same gate | MATCH | |
| filter order | :300-332 PEACEFUL (:301), null (:304), self (:307), dead (:310), sight (:313), AttackSquid→true (:316), EntitySpider→true (:319), EntityCaveSpider→true (:322), EntityChicken→true (:325), `Lizard && nextInt(10)==1 && follow_time<=0` → `buddy = it` side effect (:328-330), false (:331) | :132-139 PEACEFUL (:133), null/self/dead (:134), Spider (:135), CaveSpider (:136), Chicken (:137), false | FIXED (ENT-S-118, wave 2) — sight (:313); the AttackSquid grant (:316) stays T6, the buddy side effect (:328-330) T10 — was DIVERGES | sight step (:313) missing; AttackSquid grant (:316) missing; buddy side effect (:328-330) missing |
| PlayNicely gate | :336-338 → null | none (:141-152) | FIXED (ENT-S-115, wave 1) — was DIVERGES | missing gate |
| creative gate | none (players never granted) | none | N/A | |
| PEACEFUL gate | :301 (filter) + :270 (tick) | :133 + :211 | MATCH | |
| allies / species exclusions | prey grants only: AttackSquid, Spider, CaveSpider, Chicken | Spider, CaveSpider, Chicken | FIXED (ENT-S-128, wave 2) — was DIVERGES | AttackSquid (port class exists) dropped from the prey list |
| ignore screen | none (not a caller) | none | N/A | |
| tie-break / selection rule | :340 `GenericTargetSorter` (:45 field, :62 ctor) then first suitable | :72 `Comparator.comparingDouble(this::distanceToSqr)`, :151 `firstMatch` | DIVERGES | tie-break: plain distance (TF-035 migration not done) |
| target set / release | revenge-first: :344-346 1-in-100 `setAttackTarget(null)`; :347-349 alive `getAttackTarget()` returned unfiltered; else :350 clear then scan. attackTarget set by `attackEntityFrom` :112-114 (any living attacker, non-cactus) and `EntityAIHurtByTarget` :70 | :142-145 same shape on `getLastHurtByMob` (1-in-100 `setLastHurtByMob(null)`, alive → return, else clear); `hurt` :108 `setLastHurtByMob(attacker)`; `HurtByTargetGoal` :84 sets `Mob.target`, which the port scan never reads | MATCH | field mapping attackTarget → lastHurtByMob, same 1-in-100 clear, same unfiltered revenge-first |
| other | prey: distSq<12 → attacking + `nextInt(4)==0 \|\| nextInt(5)==1` melee, else navigate 1.2 (:274-281); Lizard-buddy adoption inside the filter (:328-330) | :215-222 same; no buddy adoption | DIVERGES | the 1-in-10 "adopt a nearby Lizard as buddy" side effect of the orig filter is gone |

### LurkingTerror — orig `LurkingTerror.java`, port `EntityLurkingTerror.java` — lane S3
| aspect | 1.7.10 (file:line) | port HEAD (file:line) | status | note |
|---|---|---|---|---|
| scan set | `EntityLivingBase, bbox.expand(12,8,12)` LurkingTerror.java:354; `updateAITasks` :189-191 else-branch `nextInt(9)==0` (skipped on waypoint re-pick :171 `nextInt(120)==0 \|\| distSq<2.1`) | `LivingEntity, inflate(12,8,12)` EntityLurkingTerror.java:211-212; `customServerAiStep` :160 `nextInt(9)==0` (re-pick :142 `stuckCount>30 \|\| nextInt(120)==0 \|\| distSq<4.0`) | MATCH | box/class/cadence match; port-only stuck counter (:127-134) and 2.1→4.0 arrival threshold only delay the hunt branch by a tick, they do not change who is chosen |
| filter order | :271-348 null, self, dead, sight (:281), LurkingTerror (:284), RockBase (:287), EnderReaper (:290), LeafMonster (:293), TerribleTerror (:296), Mothra (:299), CloudShark (:302), Rotator (:305), Bee (:308), Mantis (:311), CreepingHorror (:314), Triffid (:317), PitchBlack (:320), Dragon (:323), Island (:326), IslandToo (:329), EntityButterfly (:332), Firefly (:335), Triffid again (:338), Player creative→false (:341-346), true | :227-250 identical order (Triffid once :241) | MATCH | ENT-K-026 restored the list |
| PlayNicely gate | :351-353 | :210 | MATCH | |
| creative gate | :343 `field_75098_d` | :248 `abilities.invulnerable` | FIX IN FLIGHT (ENT-S-109) | listed site LurkingTerror.java:343 ↔ :248 |
| PEACEFUL gate | none (EntityMob) | none (Monster) | N/A | |
| allies / species exclusions | self + RockBase, EnderReaper, LeafMonster, TerribleTerror, Mothra, CloudShark, Rotator, Bee, Mantis, CreepingHorror, Triffid, PitchBlack, Dragon, Island, IslandToo, EntityButterfly, Firefly | same 18 | MATCH | |
| ignore screen | none (not a caller; RockBase/Butterfly/Firefly are listed explicitly instead) | none | N/A | |
| tie-break / selection rule | :355 `GenericTargetSorter` (:48 field, :58 ctor) | :217 `GenericTargetSorter` | MATCH | |
| target set / release | nothing stored; prey → flight target (:194) and bite within distSq<6 (:195-197); `attackEntityFrom` :228-235 retargets the flight waypoint to any attacker; no targetTasks (:51-59) | :161-167 same; `hurt` :198-206 same, plus port-only `attacker instanceof EntityLurkingTerror → false` (:200, damage-side); no target goals | MATCH | |
| other | attacking flag gates despawn (:84-89) | not targeting | N/A | |

### Mantis — orig `Mantis.java`, port `EntityMantis.java` — lane S3
| aspect | 1.7.10 (file:line) | port HEAD (file:line) | status | note |
|---|---|---|---|---|
| scan set | `EntityLivingBase, bbox.expand(16,8,16)` Mantis.java:398; `updateAITasks` :205 else-branch `nextInt(8)==0` (re-pick :186 `stuck>50 \|\| nextInt(300)==0 \|\| distSq<2.1`), retaliation target tried first (:207-213) | `LivingEntity, inflate(16,8,16)` EntityMantis.java:235-236; :170 `nextInt(8)==0` (re-pick :150, threshold 4.0), retaliation first (:171-173) | MATCH | PORT-ONLY target goals :67-68 (`HurtByTargetGoal`, `NearestAttackableTargetGoal<Player>` VAN-NAT, no ignore predicate) — see release row |
| filter order | :311-392 null, self, dead, sight (:321), inWater→false (:324), Player→`!creative` (:327-330), Mantis (:331), Irukandji (:334), Skate (:337), Flounder (:340), Whale (:343), EntitySquid (:346), WaterDragon (:349), AttackSquid (:352), TerribleTerror (:355), LurkingTerror (:358), CloudShark (:361), Rotator (:364), Bee (:367), Mothra (:370), EntityMob→true (:373), EntityButterfly→true (:376), Cockateil→true (:379), Fairy→true (:382), unreachable player branch (:385-390), `isAttackableNonMob` (:391) | :255-280 identical order; Monster :275; `MyUtils.isAttackableNonMob` :279 | MATCH | ENT-K-030 restored the chain |
| PlayNicely gate | :395-397 | :234 | MATCH | |
| creative gate | :329 `field_75098_d` | :260 `instabuild` | MATCH | |
| PEACEFUL gate | none (EntityMob) | none (Monster); the port-only Player goal also refuses players in PEACEFUL (VAN-TC) | N/A | |
| allies / species exclusions | exclusions as above; final grant = orig `isAttackableNonMob` list (header) | same exclusions; final grant = port `isAttackableNonMob` list (header) | FIXED (ENT-S-128, wave 2) — the helper's membership (util/MyUtils.java:75-89); the :276 call site unchanged — was DIVERGES | grant list: port adds EnderDragon, Kraken, Godzilla, GodzillaHead, Basilisk; drops Leon, Dragon, Spyro, ThePrince/Teen/Adult/Princess, KingHead, QueenHead, PurplePower, GammaMetroid, Girlfriend, Boyfriend, Villager, Stinky (Mothra/WaterDragon are excluded earlier in both) |
| ignore screen | none (not a caller) | none | N/A | |
| tie-break / selection rule | :399 `GenericTargetSorter` (:49 field, :62 ctor) | :241 `GenericTargetSorter` | MATCH | |
| target set / release | `rt` = last living attacker (:253-260, with flight retarget), used first each hunt tick, dropped only when dead (:208-210); never cleared otherwise; no targetTasks | `retaliationTarget` :216-225 / :171-172 same. PORT-ONLY: `HurtByTargetGoal` :67 and `NearestAttackableTargetGoal<Player>` :68 set `Mob.target`, which nothing in EntityMantis consumes (no melee goal; the custom loop uses `retaliationTarget` / `findSomethingToAttack`) | FIXED (ENT-S-125) — both inert target goals removed from both modes; `registerGoals` empty as orig (a judgment call: the documented effect never existed) — was PORT-ONLY | inert additions (comment :60-66 says they feed a pathfinding HUD) |
| other | bite reach 5+w/2 (:217) | :179 | MATCH | |

### Molenoid — orig `Molenoid.java`, port `EntityMolenoid.java` — lane S3
| aspect | 1.7.10 (file:line) | port HEAD (file:line) | status | note |
|---|---|---|---|---|
| scan set | `EntityLivingBase, bbox.expand(12,6,12)` Molenoid.java:281; `updateAITasks` :175 `nextInt(4)==0` | `LivingEntity, inflate(12,6,12)` EntityMolenoid.java:295-296; :141 `nextInt(4)==0` | MATCH | |
| filter order | :251-275 null, self, dead, `MyCanSee` (:261), Player→`!creative` (:264-267), Molenoid→false (:268), EntityMob→true (:271), `isAttackableNonMob` (:274) | :280-290 null/self/dead, `myCanSee` (:282), Player (:284-286), EntityMolenoid (:287), Monster (:288), `isAttackableNonMob` (:289) | MATCH | sight is the custom block-march (:344-394 / :315-358), not vanilla LoS, in both |
| PlayNicely gate | :278-280 (scan); block lob :183, spoil :214, dig :239 | :294; :152, :182, :254 | MATCH | |
| creative gate | :266 `field_75098_d` | :285 `instabuild` | MATCH | |
| PEACEFUL gate | none (EntityMob) | none (Monster) | N/A | |
| allies / species exclusions | self; grants EntityMob + orig `isAttackableNonMob` list | self; grants Monster + port `isAttackableNonMob` list | FIXED (ENT-S-128, wave 2) — as Mantis (:289); Mothra and WaterDragon granted again through the helper — was DIVERGES | same grant-list drift as Mantis (here Mothra and WaterDragon are affected too: orig grants both, port grants neither) |
| ignore screen | none (not a caller) | none | N/A | |
| tie-break / selection rule | :282 `GenericTargetSorter` (:38 field, :47 ctor) | :301 `GenericTargetSorter` | MATCH | |
| target set / release | nothing stored (method-scoped `e`, :170); `EntityAIHurtByTarget` :53 sets attackTarget that no goal consumes | nothing stored (:137); `HurtByTargetGoal` :74 likewise unconsumed | MATCH | inert in both |
| other | reach 6+w/2 (:179), bite inside distSq<16 else block lob (:181-183) | :146-156 | MATCH | |

### Mothra — orig `Mothra.java`, port `Mothra.java` — lane S3
| aspect | 1.7.10 (file:line) | port HEAD (file:line) | status | note |
|---|---|---|---|---|
| scan set | stage 1 (players): `findNearestEntityWithinAABB(EntityPlayer, bbox.expand(25,20,25), this)` Mothra.java:224, gated :222 `nextInt(10)==0 && !PEACEFUL && MothraPeaceful==0`; stage 2 (fallback, only when stage 1 yielded null, :237 `nextInt(3)==0`): `EntityLivingBase, bbox.expand(15,20,15)` :489 | stage 1: `level.getNearestPlayer(this, 25.0)` Mothra.java:392 = nearest non-spectator player within a **sphere r=25 from the entity position** ((v) `EntityGetter.java:102-110,84-99`), gate :390-391 same; stage 2: `LivingEntity, inflate(15,20,15)` :286-287, :397 `nextInt(3)==0`; MOD-029 modern-mode 6x3 root box widens both sweeps (:166-174) | DIVERGES (stage 1); stage 2 RECORDED (MOD-029) / MATCH classic | scan-set shape: box 25/20/25 around the bbox → sphere 25 (taller, narrower on diagonals); stage 2 identical in classic mode |
| filter order | stage 1: :225-236 nearest player → creative? then `target=null` (falls through to stage 2) : sight (:227) → chase/shoot; stage 2 `isSuitableTarget` :424-483 PEACEFUL (:425), null, self, dead, ignore (:437), sight (:440), Mothra (:443), Brutalfly (:446), Vortex (:449), VelocityRaptor (:452), Cryolophosaurus (:455), TerribleTerror (:458), LurkingTerror (:461), CloudShark (:464), Rotator (:467), Bee (:470), Mantis (:473), Player creative→false (:476-481), true | stage 1 :393-396: `target != null && !instabuild` → chase/shoot, **no sight check**, and a creative nearest player leaves `target` non-null so :397 never falls through to stage 2; stage 2 :263-281 identical order | DIVERGES (stage 1); stage 2 MATCH | filter order: stage-1 sight step (:227) missing; creative fall-through to the mob hunt (:233-235) missing |
| PlayNicely gate | :486-488 (stage 2 only) | :285 (stage 2 only) | MATCH | |
| creative gate | :226 (stage 1) and :478 (stage 2) `field_75098_d` | :393 and :279 `instabuild` | MATCH | |
| PEACEFUL gate | :222 (stage gate), :385 (`attackWithSomething`), :425 (filter); MothraPeaceful :222, :382 | :390, :306, :264; MOTHRA_PEACEFUL :391, :305 | MATCH | |
| allies / species exclusions | Mothra, Brutalfly, Vortex, VelocityRaptor, Cryolophosaurus, TerribleTerror, LurkingTerror, CloudShark, Rotator, Bee, Mantis | same 11 | MATCH | |
| ignore screen | :437 after dead, before sight | :266 same | MATCH | pre-existing (one of the 11 callers ENT-S-106 found present) |
| tie-break / selection rule | stage 1: nearest player by distanceSq (vanilla `findNearestEntityWithinAABB`, strict `<`); stage 2: :490 `GenericTargetSorter` (:60 field, :70 ctor) | stage 1: strict `<` over the level player list ((v) `EntityGetter.java:88-95`); stage 2: :85 `GenericTargetSorter`, :290 `firstMatch` | MATCH | |
| target set / release | nothing stored (flight waypoint only, :228/:241); `attackEntityFrom` :274-285 Mothra attacker immune, waypoint → attacker+2; no targetTasks | :243-251 same; `EntityButterfly.registerGoals` (:65) registers no target goals | MATCH | |
| other | shoot dice 1-in-3 (1-in-2 on HARD) :165,:178-180 | :367 | MATCH | |

### Nastysaurus — orig `Nastysaurus.java`, port `Nastysaurus.java` — lane S3
| aspect | 1.7.10 (file:line) | port HEAD (file:line) | status | note |
|---|---|---|---|---|
| scan set | `EntityLivingBase, bbox.expand(32,8,32)` Nastysaurus.java:282; `updateAITasks` :212 `nextInt(5)==0`, after the retaliation target (:214-226) | `NearestAttackableTargetGoal<>(this, Player.class, true, !isIgnoreable)` Nastysaurus.java:72-73: VAN-NAT `Player.class` → all players in the level within FOLLOW_RANGE 40 (:84) × visibility, no box, 1-in-10; consumed by `DinosaurMeleeAttackGoal` :64-65 (preset `DinosaurMeleeAttackGoal.java:35`, `BugMeleeAttackGoal.java:82-96` reads `mob.getTarget()`; the preset's 32/8 "search" fields are never used) | FIX IN FLIGHT (ENT-S-108) | players-only narrowing; box 32x8x32 → sphere 40 |
| filter order | :246-276 null, self, dead, ignore (:256), Nastysaurus (:259), Cryolophosaurus (:262), VelocityRaptor (:265), sight (:268), Player→`!creative` (:271-274), true | VAN-TC: self, spectator/alive, `!isIgnoreable` (:73), `canAttack` (PEACEFUL-player rule, `!invulnerable`), `canAttackType`, allied, range, sight | FIX IN FLIGHT (ENT-S-108) | species steps moot while players-only; ignore sits ahead of sight in both |
| PlayNicely gate | :215-217 (retaliation target dropped) and :279-281 (scan) | none (:72-73, `BugMeleeAttackGoal` has no gate) | FIXED (ENT-S-115 :215 / ENT-S-108 :279, wave 1) — the pass-local blanking of a foreign occupant (the scan's own pick is cleared as at HEAD, refuter B1); the slot's goal-side consumption stays with T5 — was DIVERGES | missing gate |
| creative gate | :273 `field_75098_d` | VAN-TC `Player.canBeSeenAsEnemy` → `abilities.invulnerable` ((v) `Player.java:966-968`) | DIVERGES | same class as ENT-S-109 (invulnerable vs instabuild) but through the vanilla predicate, not a listed site |
| PEACEFUL gate | none (EntityMob) | none (Monster); VAN-TC additionally refuses players in PEACEFUL | N/A | |
| allies / species exclusions | Nastysaurus, Cryolophosaurus, VelocityRaptor | none | FIX IN FLIGHT (ENT-S-108) | moot while players-only |
| ignore screen | :256 before species chain and sight | goal predicate :73 (inside VAN-TC, ahead of sight) | FIXED (ENT-S-106) | |
| tie-break / selection rule | :283 `GenericTargetSorter` (:41 field, :52 ctor) then first suitable | nearest player by plain distanceSq from eye position, strict `<` ((v) `EntityGetter.java:160-167`) | DIVERGES | tie-break: no creeper/silhouette weighting (matters once the scan is widened) |
| target set / release | `rt` = last living attacker (:195-205); each 1-in-5 tick: dropped when dead or 1-in-250 (:219-222), skipped (kept) when out of sight (:223-225) then rescan; no attackTarget use (`EntityAIHurtByTarget` :58 inert) | `Mob.target` via goals; `BugMeleeAttackGoal.tick` :123-129 1-in-250 `setTarget(null)` per tick; hunt target released by `TargetGoal.canContinueToUse` (beyond 40, unseen > 60 ticks); revenge via `HurtByTargetGoal` :69 (VAN-HBT: no sight, 300-tick unseen memory) | FIXED (ENT-S-129, wave 2)` — the 1-in-250 inside the pass on rt, final; rt kept through sight loss (the slot residual disclosed); no range / unseen release; the PlayNicely residual closed by the melee goal's stand-down — was DIVERGES | release rule: orig keeps `rt` through sight loss and re-evaluates every hunt tick with no memory window; port drops the hunt target after 60 unseen ticks and the revenge target after 300 |
| other | reach 4.5+w/2 (:232), dice `nextInt(4)==0 \|\| nextInt(5)==1` (:234), navigate 1.25 (:238) | preset 4.5 / 4,5 / 1.25 (`DinosaurMeleeAttackGoal.java:35`), cadence gate `BugMeleeAttackGoal.java:133` | MATCH | |

### Peacock — orig `Peacock.java`, port `Peacock.java` — lane S3
| aspect | 1.7.10 (file:line) | port HEAD (file:line) | status | note |
|---|---|---|---|---|
| scan set | `EntityLivingBase, bbox.expand(10,2,10)` Peacock.java:225; `updateAITasks` :190 `nextInt(10)==1` after the PEACEFUL return (:187) | `getEntitiesOfClass(EntityTermite, inflate(10,2,10), alive && sight)` Peacock.java:177-179; `customServerAiStep` :141 `nextInt(10)==1` after :138 | MATCH | class narrowed to EntityTermite; equivalent because the orig filter accepts only Termite (:218) |
| filter order | :202-219 PEACEFUL (:203), null, self, dead, sight (:215), `instanceof Termite` (:218) | predicate :179 alive && sight; PEACEFUL by the caller :138 | MATCH | |
| PlayNicely gate | :222-224 (scan); vanilla goal registered only when `PlayNicely == 0` (:64) | :171-173 | MATCH | |
| creative gate | none (Termites only) | none | N/A | |
| PEACEFUL gate | :187 (tick) + :203 (filter) | :138 | MATCH | |
| allies / species exclusions | prey = Termite only | same | MATCH | |
| ignore screen | none (not a caller) | none | N/A | |
| tie-break / selection rule | :226 `GenericTargetSorter` (:45 field, :55 ctor) then first suitable | :182 `TargetSelection.first(termites, GenericTargetSorter)` | MATCH | |
| target set / release | nothing stored; :183-185 1-in-200 `setRevengeTarget(null)`; vanilla `EntityAINearestAttackableTarget(this, Termite.class, chance 6, sight)` :65 sets an attackTarget no Peacock goal consumes (no attack goal) | :134-136 1-in-200 `setLastHurtByMob(null)` (correct field); no target goals (:57-65) | RECORDED (ENT-S-129)` — inert in both, no code — was DIVERGES | port drops the orig Termite target goal (inert in orig — nothing read the attackTarget) |
| other | melee 6.0 within distSq<4 else navigate 1.2 (:191-195) | :144-149 | MATCH | |

### PitchBlack — orig `PitchBlack.java`, port `PitchBlack.java` — lane S3
| aspect | 1.7.10 (file:line) | port HEAD (file:line) | status | note |
|---|---|---|---|---|
| scan set | `EntityLivingBase, bbox.expand(16+6t, 10+4t, 16+6t)` (t = scale 0.5..4) PitchBlack.java:544-546; callers: `onUpdate` :281-288 (activity==0, 1-in-10 → activity 1), `onUpdate` :259-275 (server, 1-in-250 heal branch: 1-in-5 & solid ground within 10 → scan, null → activity 0), `updateAITasks` :361-363 (activity==1, else-branch 1-in-8; re-pick :343 `nextInt(150)==0 \ | \ | FIXED (ENT-S-129, wave 2)` — the orig :259-280 branch (heal 1 + scale, the null-scan deactivation, the spontaneous activation) — was distSq<2.1`) | same formula PitchBlack.java:509-513 (t from tier: 0.5,1,2,3,4); callers `customServerAiStep` :421-431 (activity==0, 1-in-10 → activity 1 + nav stop), :448-449 (1-in-8 else-branch; re-pick :437-438) | DIVERGES | scan set: the :259-275 heal-branch call site (the only path that RESETS activity to 0 when nothing is found) is absent from the port (tick :392-403 has no heal branch) |
| filter order | :485-538 null, self, `instanceof EntityLivingBase` (:492, always true), dead, ignore (:498), sight (:501), PitchBlack (:504), EnderReaper (:507), LeafMonster (:510), TerribleTerror (:513), LurkingTerror (:516), CreepingHorror (:519), Island (:522), IslandToo (:525), Triffid (:528), Player creative→false (:531-536), true | :520-526 null/self/dead, ignore (:522), PitchBlack (:523), Player instabuild→false (:524), true | FIXED (ENT-S-118, wave 2) — between the ignore screen and the self-kind refusal, ahead of the ENT-S-112 allies (lane M O4 closed) — was DIVERGES | sight step (:501) missing (not covered by ENT-S-112, which names the allies) |
| PlayNicely gate | :541-543 → null | none (:509-518) | FIXED (ENT-S-115, wave 1) — was DIVERGES | missing gate |
| creative gate | :533 `field_75098_d` | :524 `instabuild` | MATCH | |
| PEACEFUL gate | none (EntityMob) | none (Monster) | N/A | |
| allies / species exclusions | self + EnderReaper, LeafMonster, TerribleTerror, LurkingTerror, CreepingHorror, Island, IslandToo, Triffid | self only | FIX IN FLIGHT (ENT-S-112) | |
| ignore screen | :498 after dead, before sight | :522 | FIXED (ENT-S-106) | |
| tie-break / selection rule | :547 `GenericTargetSorter` (:54 field, :67 ctor) | :156 `GenericTargetSorter`, :517 `firstMatch` | MATCH | |
| target set / release | nothing stored; `attackEntityFrom` :413-427 20-tick damage gate, waypoint → attacker+2, activity 1, path cleared; no targetTasks (:68-72) | `hurt` :491-502 same; no target goals (:160-165) | MATCH | |
| other | giant-target reach floor 100 for EntityDragon/Godzilla/GodzillaHead (:369-377); reach 5+w/2+t (:365-367) | :451-466 | MATCH | |

### Pointysaurus — orig `Pointysaurus.java`, port `Pointysaurus.java` — lane S3
| aspect | 1.7.10 (file:line) | port HEAD (file:line) | status | note |
|---|---|---|---|---|
| scan set | `EntityLivingBase, bbox.expand(12,5,12)` Pointysaurus.java:253; `updateAITasks` :183 `nextInt(6)==0`, retaliation target first (:185-197) | `NearestAttackableTargetGoal<>(this, Player.class, true, !isIgnoreable)` Pointysaurus.java:72-73: all players in the level within FOLLOW_RANGE 24 (:84) × visibility, 1-in-10; PORT-ONLY `PointysaurusStareGoal` :65 (`ai/PointysaurusStareGoal.java:32,37-39,63`: nearest player within 32 looking at the mob, creative/spectator excluded :48, sight :58, locks the target after 5 ticks :90-95); consumed by `DinosaurMeleeAttackGoal` :54-55 | RECORDED (MOD-034, ENT-S-125) — the stare goal registers only in modern (`pointysaurusStareAggro`); classic = orig :50-55; the box → sphere widening of the player goal stays T3c — was DIVERGES + PORT-ONLY | players-only is orig behaviour (:242-246; ENT-S-108 excludes Pointysaurus) but the box 12x5x12 became a sphere of 24 (scan-set widening); the stare goal is a port invention with no MOD record (AUDIT_FINDINGS.md:7031 mentions it only for its creative token) |
| filter order | :217-247 null, self, dead, ignore (:227), Pointysaurus (:230), EntityMob (:233), VelocityRaptor (:236), sight (:239), Player→`!creative` (:242-245), false (:246) | VAN-TC with `!isIgnoreable` (:73) ahead of sight; species steps absent (moot: players only in both) | MATCH (effective) | ignore before sight in both |
| PlayNicely gate | :186-188 (retaliation dropped) and :250-252 (scan) | none | FIXED (ENT-S-115, wave 1) — scan half (:250, both proactive goals); :186-188 DEFERRED to T5 (no pass-local in the port; the retaliation pass is the shared melee goal) — was DIVERGES | missing gate |
| creative gate | :244 `field_75098_d` | VAN-TC `invulnerable` for the NearestAttackable goal; stare goal `isCreative()` (= instabuild) :48 | DIVERGES (goal path) | |
| PEACEFUL gate | none (EntityMob) | none (Monster); VAN-TC refuses players in PEACEFUL | N/A | |
| allies / species exclusions | self, EntityMob, VelocityRaptor (moot: only players pass) | none | MATCH (effective) | |
| ignore screen | :227 before species chain and sight | goal predicate :73 | FIXED (ENT-S-106) | |
| tie-break / selection rule | :254 `GenericTargetSorter` (:39 field, :49 ctor) — for players the silhouette term is uniform, so effectively nearest | nearest player by plain distanceSq from eye position, strict `<` | MATCH (effective) | |
| target set / release | `rt` = last living attacker (:166-176), used first each 1-in-6 tick, dropped when dead / 1-in-250 / not visible this tick (:189-197) | `Mob.target` via goals; preset forget 1-in-250 (`DinosaurMeleeAttackGoal.java:36`, `BugMeleeAttackGoal.java:123-129`); hunt target released beyond 24 / 60 unseen ticks (VAN-NAT); revenge via `HurtByTargetGoal` :59 (no sight, 300-tick memory); stare target held while eye contact lasts (`PointysaurusStareGoal.java:78-83`) | FIXED (ENT-S-129, wave 2) — the 1-in-250 inside the 1-in-6 pass on rt, final, and the :186-188 PlayNicely blanking (the ENT-S-115 deferral); the sight skip stays with T3c's vanilla scan (recorded) — was DIVERGES | release rule (as Nastysaurus) |
| other | reach 4+w/2 (:203), dice `nextInt(5)==0 \ | \ | RECORDED (MOD-034, ENT-S-125) — as the scan-set row — was nextInt(6)==1` (:205), navigate 1.25 (:209) | preset 4.0 / 5,6 / 1.25 (`DinosaurMeleeAttackGoal.java:36`); PORT-ONLY eye-contact aggression :60-65 | PORT-ONLY |  |

### PurplePower — orig `PurplePower.java`, port `PurplePower.java` — lane S3
| aspect | 1.7.10 (file:line) | port HEAD (file:line) | status | note |
|---|---|---|---|---|
| scan set | `EntityLivingBase, bbox.expand(32,24,32)` PurplePower.java:271; `updateAITasks` :173 else-branch `nextInt(7)==2 && difficulty != PEACEFUL` (re-pick :155) | `LivingEntity, inflate(32,24,32)` PurplePower.java:187-188; :121 `nextInt(7)==2` (no PEACEFUL term; re-pick :112) | MATCH (box) | PEACEFUL term of the call site counted in the PEACEFUL row |
| filter order | :234-265 PEACEFUL (:236), null, self, dead, ignore (:248), sight (:251), Player: creative→false, `return type<=0 \ | \ | FIXED (ENT-S-118, wave 2) — sight (:251); PEACEFUL (:236) FIXED (ENT-S-114, wave 1); the tamed-pet / royalty steps (:261-264) stay T6 — was type==10` (:254-260), `type not 0/10 && tamed EntityTameable → false` (:261-263), `return !isRoyalty` (:264) | :194-203 null/self/dead, ignore (:196), Player instabuild→false (:197), Player → `type<=0 \ | \ | type==10` (:199-201), true | DIVERGES | filter order: PEACEFUL (:236), sight (:251), tamed-pet exclusion (:261-263) and royalty exclusion (:264) all missing |
| PlayNicely gate | :268-270 → null | none (:186-192) | FIXED (ENT-S-115, wave 1) — was DIVERGES | missing gate |
| creative gate | :256 `field_75098_d` | :197 `instabuild` | MATCH | |
| PEACEFUL gate | :173 (call site), :180-182 (`setDead` in PEACEFUL), :236 (filter) | none; PurplePower extends `Mob` (:29), not `Monster`, so no peaceful despawn either | FIXED (ENT-S-114, wave 1) — was DIVERGES | missing gate: the port hunts in PEACEFUL (its `setHealth(h/4-1)` at :151 bypasses difficulty scaling) |
| allies / species exclusions | tamed pets for types other than 0/10 (:261-263); royalty via `MyUtils.isRoyalty` (:264): ThePrince, ThePrinceTeen, ThePrinceAdult, ThePrincess, TheKing, KingHead, TheQueen, QueenHead, PurplePower | none (port `MyUtils.isRoyalty` exists at `util/MyUtils.java:9-19` but is not called) | FIXED (ENT-S-128, wave 2) — was DIVERGES | exclusion list missing |
| ignore screen | :248 after dead, before sight | :196 | FIXED (ENT-S-106) | |
| tie-break / selection rule | :272 `GenericTargetSorter` (:35 field, :44 ctor) | :41 `Comparator.comparingDouble(this::distanceToSqr)`, :191 `firstMatch` | DIVERGES | tie-break: plain distance (TF-035 migration not done) |
| target set / release | nothing stored; `attackEntityFrom` :209-224 arrow-immune, damage capped 10, waypoint → attacker mid-height; no AI tasks (EntityLiving) | `hurt` :170-179 same; `registerGoals` empty (:45) | MATCH | |
| other | reach 4+w/2 then `setDead` after the hit (:175-178) | :125-129 | MATCH | |

### Rat — orig `Rat.java`, port `EntityRat.java` — lane S3
| aspect | 1.7.10 (file:line) | port HEAD (file:line) | status | note |
|---|---|---|---|---|
| scan set | `EntityLivingBase, bbox.expand(9,2,9)` Rat.java:255; `updateAITasks` :159 `nextInt(5)==1` | `LivingEntity, inflate(9,2,9)` EntityRat.java:213-214; :157 `nextInt(5)==1` | MATCH | |
| filter order | :185-249 null, self, dead, ignore (:195), sight (:198), Irukandji (:201), Skate (:204), Whale (:207), Flounder (:210), Rat (:213), Ghost (:216), GhostSkelly (:219), DungeonBeast (:222), Player: creative→false; owned rat: owner→false, RatPlayerFriendly→false (:225-238); owned rat vs EntityTameable: RatPetFriendly && tamed→false, same owner→false (:239-247), true | :192-210 null/self/dead, ignore (:194), sight (:195), EntityRat (:196), Player: invulnerable→false, owned: owner / RAT_PLAYER_FRIENDLY (:198-204), owned vs TamableAnimal: RAT_PET_FRIENDLY && tame / same owner (:205-208), true | FIXED (ENT-S-128, wave 2) — :204-210 (Ghost / GhostSkelly: the ignore screen's, both trees) — was DIVERGES | filter order: Irukandji, Skate, Whale, Flounder, DungeonBeast steps missing (Ghost/GhostSkelly are covered by the ignore screen in both trees); creative token in flight |
| PlayNicely gate | :252-254 → null | none (:212-218) | FIXED (ENT-S-115, wave 1) — was DIVERGES | missing gate |
| creative gate | :227 `field_75098_d` | :199 `invulnerable` | FIX IN FLIGHT (ENT-S-109) | listed site Rat.java:227 ↔ :199 |
| PEACEFUL gate | none (EntityMob) | none (Monster) | N/A | |
| allies / species exclusions | Irukandji, Skate, Whale, Flounder, Rat, Ghost, GhostSkelly, DungeonBeast; owner / same-owner pets when owned | Rat; owner / same-owner pets when owned | FIXED (ENT-S-128, wave 2) — was DIVERGES | missing Irukandji, Skate, Whale, Flounder, DungeonBeast |
| ignore screen | :195 after dead, before sight | :194 | FIXED (ENT-S-106) | |
| tie-break / selection rule | :256 `GenericTargetSorter` (:46 field, :63 ctor) | :217 `Comparator.comparingDouble(this::distanceToSqr)` | DIVERGES | tie-break: plain distance (TF-035 migration not done) |
| target set / release | nothing stored; :156-158 1-in-200 `setRevengeTarget(null)`; `EntityAIHurtByTarget` :62 sets an attackTarget nothing consumes | nothing stored; :153-155 1-in-200 `setTarget(null)`; `HurtByTargetGoal` :74 unconsumed | FIXED (ENT-S-129, wave 2)` — `setLastHurtByMob(null) — was DIVERGES | release rule as LeafMonster (revenge-memory clear became attack-target clear; inert in both) |
| other | RatPlayerFriendly / RatPetFriendly defaults 1/1 (OreSpawnMain.java:1472-1473), applied only to owned rats | `ratPlayerFriendly` / `ratPetFriendly` default true (OreSpawnConfig.java:341-342), owned rats only (ENT-K-058 ruling) | MATCH | bite within distSq<4 on `nextInt(8)==0 \|\| nextInt(7)==1` (:164 / :162-163) MATCH |

### Robot1 — orig `Robot1.java`, port `Robot1.java` — lane S3
| aspect | 1.7.10 (file:line) | port HEAD (file:line) | status | note |
|---|---|---|---|---|
| scan set | `EntityLivingBase, bbox.expand(8,3,8)` Robot1.java:208; `onLivingUpdate` :116 `nextInt(8)==0` (both sides; explosion server-only :117) | `LivingEntity, inflate(8,3,8)` Robot1.java:142-143; `aiStep` :103 `nextInt(8)==0` (both sides; explosion server-only :108) | MATCH | |
| filter order | :176-202 null, self, dead, ignore (:186), sight (:189), EntityMob→false (:192), Player creative→false (:195-200), true | :149-155 null/self/dead, ignore (:151), Monster→false (:152), Player instabuild→false (:153), true | FIXED (ENT-S-118, wave 2) — was DIVERGES | sight step (:189) missing |
| PlayNicely gate | :205-207 → null | none (:141-147) | FIXED (ENT-S-115, wave 1) — was DIVERGES | missing gate |
| creative gate | :197 `field_75098_d` | :153 `instabuild` | MATCH | |
| PEACEFUL gate | none (EntityMob) | none (Monster) | N/A | |
| allies / species exclusions | EntityMob | Monster | MATCH | |
| ignore screen | :186 after dead, before sight | :151 | FIXED (ENT-S-106) | |
| tie-break / selection rule | :209 `GenericTargetSorter` (:33 field, :44 ctor) | :61 `Comparator.comparingDouble(this::distanceToSqr)`, :146 `firstMatch` | DIVERGES | tie-break: plain distance (TF-035 migration not done) |
| target set / release | nothing stored; `EntityAIHurtByTarget` :51 (unconsumed) | nothing stored; `HurtByTargetGoal` :70 (unconsumed) | MATCH | |
| other | self-detonation within distSq<5 on 1-in-18 (:117-120) | :108-113 | MATCH | port-only spin animation (:124-127) is cosmetic |

### Robot2 — orig `Robot2.java`, port `Robot2.java` — lane S3
| aspect | 1.7.10 (file:line) | port HEAD (file:line) | status | note |
|---|---|---|---|---|
| scan set | `EntityLivingBase, bbox.expand(14,3,14)` Robot2.java:385; `updateAITasks` :279 `nextInt(6)==1 && PlayNicely==0`, attackTarget first (:281-289) | `LivingEntity, inflate(14,3,14)` Robot2.java:262-263; :191 `nextInt(6)==1 && !PLAY_NICELY`, target first (:192-195) | MATCH | |
| filter order | :353-379 null, self, dead, ignore (:363), sight (:366), EntityMob→false (:369), Player creative→false (:372-377), true | :270-276 null/self/dead, ignore (:272), Monster (:273), Player instabuild (:274), true | FIXED (ENT-S-118, wave 2) — was DIVERGES | sight step (:366) missing |
| PlayNicely gate | :279 (think tick), :320 (tantrum), :382-384 (scan) | :191, :233, :261 | MATCH | |
| creative gate | :374 `field_75098_d` | :274 `instabuild` | MATCH | |
| PEACEFUL gate | none (EntityMob) | none (Monster) | N/A | |
| allies / species exclusions | EntityMob | Monster | MATCH | |
| ignore screen | :363 after dead, before sight | :272 | FIXED (ENT-S-106) | |
| tie-break / selection rule | :386 `GenericTargetSorter` (:38 field, :50 ctor) | :89 `GenericTargetSorter`, :267 `firstMatch` | MATCH | |
| target set / release | attackTarget: 1-in-50 clear (:281-283), dead → clear (:284-287); `attackEntityFrom` :338-351 sets attackTarget for `EntityLiving` attackers (not players) + `setLastAttacker` + navigate 1.2; `EntityAIHurtByTarget(this, false)` :57 — no call for help, own kind not ignored | :193-194 same clears; `hurt` :248-257 `Mob` attackers → `setTarget` + moveTo 1.2; `HurtByTargetGoal(this, Robot2.class).setAlertOthers()` :98 — VAN-HBT ignores damage from other Robot2s and alerts every Robot2 within FOLLOW_RANGE x 10 that has no target | FIXED (ENT-S-129, wave 2)` — no alert, no same-kind exemption — was DIVERGES | release/target-set rule: port-only same-kind alert and same-kind damage exemption |
| other | body-facing cone 1.25 rad (:292-301), reach 5+w/2 (:302), dice 5/6 (:304) | :199-212 | MATCH | |

### Robot3 — orig `Robot3.java`, port `Robot3.java` — lane S3
| aspect | 1.7.10 (file:line) | port HEAD (file:line) | status | note |
|---|---|---|---|---|
| scan set | `EntityLivingBase, bbox.expand(16,3,16)` Robot3.java:325; `updateAITasks` :240 when `reload_ticker==0` (every 35 ticks, :252), attackTarget first (:242-251) | `LivingEntity, inflate(16,3,16)` Robot3.java:173-174; :124-129 same | MATCH | |
| filter order | :293-319 null, self, dead, ignore (:303), sight (:306), EntityMob→false (:309), Player creative→false (:312-317), true | :180-186 null/self/dead, ignore (:182), Monster (:183), Player instabuild (:184), true | FIXED (ENT-S-118, wave 2) — the selection sight; the shot's sight gate vs orig's 0.5-rad cone stays T8 — was DIVERGES | sight step (:306) missing from selection (the port gates the SHOT on sight :135 instead; orig gates the shot on a 0.5-rad head-facing cone :255-263) |
| PlayNicely gate | :322-324 → null | none (:172-178) | FIXED (ENT-S-115, wave 1) — was DIVERGES | missing gate |
| creative gate | :314 `field_75098_d` | :184 `instabuild` | MATCH | |
| PEACEFUL gate | none (EntityMob) | none (Monster) | N/A | |
| allies / species exclusions | EntityMob | Monster | MATCH | |
| ignore screen | :303 after dead, before sight | :182 | FIXED (ENT-S-106) | |
| tie-break / selection rule | :326 `GenericTargetSorter` (:39 field, :51 ctor) | :79 `GenericTargetSorter`, :177 `firstMatch` | MATCH | |
| target set / release | attackTarget 1-in-50 clear (:242-244), dead → clear (:245-248); `attackEntityFrom` :285-291 sets no target; `EntityAIHurtByTarget` :58 (revenge target consumed at :245; 1.7.10 goal: no sight, `disableDamage` players excluded) | :126-127; `HurtByTargetGoal` :88 (VAN-HBT: no sight, `invulnerable` players excluded), consumed at :125 | MATCH | |
| other | fires when distSq<256 && cone<0.5 (:255-276), navigate 0.5 | fires when distSq<256 && line of sight (:132-137), navigate 0.5 | N/A | firing gate, not selection (noted for the owner) |

### Robot4 — orig `Robot4.java`, port `Robot4.java` — lane S3
| aspect | 1.7.10 (file:line) | port HEAD (file:line) | status | note |
|---|---|---|---|---|
| scan set | `EntityLivingBase, bbox.expand(16,4,16)` Robot4.java:389; `updateAITasks` :280 `reload_ticker==0 && nextInt(8)==1`, attackTarget first (:282-291) | `LivingEntity, inflate(16,4,16)` Robot4.java:277-278; :173-177 same | MATCH | |
| filter order | :357-383 null, self, dead, ignore (:367), sight (:370), EntityMob→false (:373), Player creative→false (:376-381), true | :285-291 null/self/dead, ignore (:287), Monster (:288), Player instabuild (:289), true | FIXED (ENT-S-118, wave 2) — was DIVERGES | sight step (:370) missing |
| PlayNicely gate | :386-388 → null | :276 | MATCH | |
| creative gate | :378 `field_75098_d` | :289 `instabuild` | MATCH | |
| PEACEFUL gate | none (EntityMob) | none (Monster) | N/A | |
| allies / species exclusions | EntityMob | Monster | MATCH | |
| ignore screen | :367 after dead, before sight | :287 | FIXED (ENT-S-106) | |
| tie-break / selection rule | :390 `GenericTargetSorter` (:41 field, :54 ctor) | :80 `GenericTargetSorter`, :282 `firstMatch` | MATCH | |
| target set / release | attackTarget 1-in-50 clear (:282-284), dead → clear (:285-288); `attackEntityFrom` :336-355 shield / 65-tick gate then attackTarget for `EntityLiving` attackers + navigate 1.2 (:346-351); `EntityAIHurtByTarget` :61 | :175-176; `hurt` :259-272 `Mob` attackers (:266-270); `HurtByTargetGoal` :89 | MATCH | |
| other | melee 3+w/2 else head-facing cone 0.5 (:295-326) | :181-200 | MATCH | |

### Robot5 — orig `Robot5.java`, port `Robot5.java` — lane S3
| aspect | 1.7.10 (file:line) | port HEAD (file:line) | status | note |
|---|---|---|---|---|
| scan set | `EntityLivingBase, bbox.expand(30,6,30)` Robot5.java:299; `updateAITasks` :212 when `reload_ticker==0` (every 20 ticks, :224), attackTarget first (:214-223) | `LivingEntity, inflate(30,6,30)` Robot5.java:162-163; :111-116 same | MATCH | |
| filter order | :267-293 null, self, dead, ignore (:277), sight (:280), EntityMob→false (:283), Player creative→false (:286-291), true | :169-175 null/self/dead, ignore (:171), Monster (:172), Player instabuild (:173), true | FIXED (ENT-S-118, wave 2) — the selection sight; the shot's sight gate vs orig's 0.5-rad cone stays T8 — was DIVERGES | sight step (:280) missing from selection (port gates the shot on sight :125; orig on a 0.5-rad cone :228-235) |
| PlayNicely gate | :296-298 → null | none (:161-167) | FIXED (ENT-S-115, wave 1) — was DIVERGES | missing gate |
| creative gate | :288 `field_75098_d` | :173 `instabuild` | MATCH | |
| PEACEFUL gate | none (EntityMob) | none (Monster) | N/A | |
| allies / species exclusions | EntityMob | Monster | MATCH | |
| ignore screen | :277 after dead, before sight | :171 | FIXED (ENT-S-106) | |
| tie-break / selection rule | :300 `GenericTargetSorter` (:39 field, :50 ctor) | :66 `GenericTargetSorter`, :166 `firstMatch` | MATCH | |
| target set / release | attackTarget 1-in-50 clear (:214-216), dead → clear (:217-220); `attackEntityFrom` :259-265 sets no target; `EntityAIHurtByTarget` :56 (consumed at :217) | :113-114; `HurtByTargetGoal` :75 (consumed at :112) | MATCH | |
| other | fires within distSq<900 && cone<0.5, navigates when >36 (:227-251) | :120-128 | N/A | firing gate, not selection |

### Rotator — orig `Rotator.java`, port `EntityRotator.java` — lane S3
| aspect | 1.7.10 (file:line) | port HEAD (file:line) | status | note |
|---|---|---|---|---|
| scan set | `EntityLivingBase, bbox.expand(12,10,12)` Rotator.java:371; callers `onUpdate` :154 every tick both sides (`busy_fighting` flag + client particle), `updateAITasks` :209 else-branch `nextInt(9)==2` (re-pick :191 `nextInt(300)==0 \|\| distSq<2.1`) | `LivingEntity, inflate(12,10,12)` EntityRotator.java:257-258; `tick` :104 every tick both sides; `customServerAiStep` :190 `nextInt(9)==2` (re-pick :172 `closerToCenterThan(pos, 2.1)`, i.e. distance not distanceSq) | MATCH | box/class/cadence match; arrival threshold 1.45→2.1 blocks only delays the hunt branch |
| filter order | :294-365 null, self, dead, ignore (:304), sight (:307), Player creative→false (:310-315), Termite (:316), Vortex (:319), Rotator (:322), DungeonBeast (:325), Peacock (:328), CrystalCow (:331), Irukandji (:334), Skate (:337), Whale (:340), Flounder (:343), Urchin (:346), TerribleTerror (:349), LurkingTerror (:352), CloudShark (:355), Mothra (:358), Bee (:361), `!Mantis` (:364) | :228-250 identical order | MATCH | ENT-K-079 restored the chain |
| PlayNicely gate | :368-370 | :256 | MATCH | |
| creative gate | :312 `field_75098_d` | :232 `instabuild` | MATCH | |
| PEACEFUL gate | none (EntityMob) | none (Monster) | N/A | |
| allies / species exclusions | self + Termite, Vortex, DungeonBeast, Peacock, CrystalCow, Irukandji, Skate, Whale, Flounder, Urchin, TerribleTerror, LurkingTerror, CloudShark, Mothra, Bee, Mantis | same 16 | MATCH | |
| ignore screen | :304 after dead, before sight | :230 | MATCH | pre-existing (one of the 11) |
| tie-break / selection rule | :372 `GenericTargetSorter` (:49 field, :60 ctor) | :261 `GenericTargetSorter`, `firstMatch` | MATCH | |
| target set / release | nothing stored; `attackEntityFrom` :242-253 arrow-immune, waypoint → attacker; no targetTasks | `hurt` :151-161 same (AbstractArrow); `registerGoals` empty (:69-70) | MATCH | |
| other | hit within distSq<9 (:212); `busy_fighting` drives the despawn exemption (:101-109) | :198, :82-87 | MATCH | |

### RubberDucky — orig `RubberDucky.java`, port `EntityRubberDucky.java` — lane S4
| aspect | 1.7.10 (file:line) | port HEAD (file:line) | status | note |
|---|---|---|---|---|
| scan set | `EntityLivingBase.class`, bb.expand(8,4,8) (:460), from `updateAITasks` :362 behind `difficulty != PEACEFUL && nextInt(5)==1` (:392); `findSomethingToAttack` :456-478 | `LivingEntity.class`, bb.inflate(8,4,8) (:363-364), from `customServerAiStep` :234 behind `!PEACEFUL && nextInt(5)==1` (:251); `findSomethingToAttack` :356-375 | MATCH | restored under ENT-K-082 |
| filter order | :424-454: PEACEFUL(:425) → null(:428) → self(:431) → dead(:434) → sight(:437) → AttackSquid true(:440) → EntitySquid true(:443) → RubberDucky ∧ 1-in-10 → buddy, no return(:446-448) → killCount≥5 ∧ player → !creative(:449-452) → false(:453) | :341-354: PEACEFUL(:342) → null/self/dead(:343) → sight(:344) → AttackSquid(:345) → Squid(:346) → buddy adopt(:347-349) → killCount≥5 ∧ player → !instabuild(:350-352) → false(:353) | MATCH | same steps, same order, same side effect |
| PlayNicely gate | :457-459 → null | :362 `PLAY_NICELY` → null | MATCH | |
| creative gate | `isCreativeMode` :451 | `instabuild` :351 | MATCH | P6 |
| PEACEFUL gate | cadence :392 + filter :425 | cadence :251 + filter :342 | MATCH | tameable, no P3 despawn |
| allies / species exclusions | own kind never prey (adopted as buddy :446-448); no other list | same :347-349 | MATCH | |
| ignore screen | none in orig | none | N/A | not an `isIgnoreable` caller |
| tie-break / selection rule | GenericTargetSorter (:49, :69, sort :461) then first suitable | GenericTargetSorter via `TargetSelection.firstMatch` (:373-374) | MATCH | P5 |
| target set / release | keeps `getAttackTarget()` while alive (:465-468), else `setAttackTarget(null)` + buddy=null (:469-470); the scan result is used directly, never stored; attack target only set by `EntityAIHurtByTarget` (:78) | keeps `getTarget()` while alive (:365-366), else `setTarget(null)` + buddy=null (:367-368); `HurtByTargetGoal` (:91) | MATCH | P2 |
| other | players unlock at killCount ≥ 5 (:449; count bumped by player kills :142-145); tamed ducks still hunt (no tame gate); `attackEntityFrom` sets no target (:136-169) | :350, :155-159; `hurt` :150-186 sets no target | MATCH | |

### Scorpion — orig `Scorpion.java`, port `EntityScorpion.java` — lane S4
| aspect | 1.7.10 (file:line) | port HEAD (file:line) | status | note |
|---|---|---|---|---|
| scan set | `EntityLivingBase.class`, bb.expand(8,3,8) (:259), `updateAITasks` :170 behind `nextInt(6)==0` (:175); `findSomethingToAttack` :255-271 | `LivingEntity.class`, bb.inflate(8,3,8) (:177-178), `customServerAiStep` :133 behind `nextInt(6)==0` (:142); :175-185 | MATCH | ENT-S-002 restored the scan |
| filter order | :203-253: null/self/dead(:204-212) → ignore(:213) → sight(:216) → Ghost false(:219) → GhostSkelly false(:222) → VelocityRaptor true(:225) → EntitySpider true(:228) → EntityCaveSpider true(:231) → Scorpion false(:234) → EmperorScorpion false(:237) → Creeper true(:240) → EntityMob false(:243) → player creative false(:246-251) → true(:252) | :195-211 same order; CaveSpider folded into `Spider` (:204, subclass in both trees) | MATCH | |
| PlayNicely gate | :256-258 → null | :176 | MATCH | |
| creative gate | `isCreativeMode` :248 | `instabuild` :209 | MATCH | P6 |
| PEACEFUL gate | none (EntityMob :42-43) | none (`Monster` :35) | N/A | P3 |
| allies / species exclusions | Ghost, GhostSkelly, Scorpion, EmperorScorpion, every other EntityMob (:219-245) | Ghost, GhostSkelly, EntityScorpion, EntityEmperorScorpion, Monster (:199-208) | MATCH | |
| ignore screen | :213 after dead, before sight | :197 same position | MATCH | shared list FIXED (ENT-S-101) |
| tie-break / selection rule | GenericTargetSorter (:44, :55, sort :260) | GenericTargetSorter via firstMatch (:184) | MATCH | P5 |
| target set / release | no persistent target: each 1-in-6 pass rescans and uses the result directly; an empty scan means no pursuit (`setAttacking(0)`, :189-191); attack target only via `EntityAIHurtByTarget` (:62) | scan hit → `setTarget(prey)` (:144); an empty scan does NOT clear it; `BugMeleeAttackGoal` (Params.scorpion forgetTargetRoll 0, ai/BugMeleeAttackGoal.java:60) pursues `getTarget()` until it is dead (canContinueToUse :90-96); `HurtByTargetGoal` (:79) | FIXED (ENT-S-129, wave 2)` — the slot refreshed every pass; the revenge task unregistered — was DIVERGES | release rule: a target that leaves the 8/3/8 box or line of sight is chased until dead in the port; orig dropped it on the next failed scan |
| other | `attackEntityFrom` :195-201 sets no target | `hurt` :168-172 sets no target | MATCH | |

### SeaMonster — orig `SeaMonster.java`, port `SeaMonster.java` — lane S4
| aspect | 1.7.10 (file:line) | port HEAD (file:line) | status | note |
|---|---|---|---|---|
| scan set | `EntityLivingBase.class`, bb.expand(16,4,16) (:517), `updateAITasks` :431 behind `nextInt(5)==1` (:465); :513-534 | no box scan: `level().getNearestPlayer(this, 16.0)` (:174) only when `getTarget()==null` (:172-173), `customServerAiStep` :144 behind `nextInt(5)==1` (:171); sphere r16 over all players, spectators excluded (P5) | DIVERGES | scan-set narrowing: players only, sphere vs 16x4x16 box; not in ENT-S-108's list |
| filter order | :487-511: null/self/dead → sight(:497) → player → !creative(:500-503) → SeaMonster false(:504) → EntityMob true(:507) → `isAttackableNonMob`(:510) | :175 `nearest != null && !instabuild` only | DIVERGES | missing gate: sight (:497); EntityMob / attackable-non-mob prey absent (P9) |
| PlayNicely gate | :514-516 → null | none in SeaMonster.java | FIXED (ENT-S-115, wave 1) — the inline pick gated as a whole — was DIVERGES | missing gate |
| creative gate | `isCreativeMode` :502 | `instabuild` :175 | MATCH | P6 |
| PEACEFUL gate | none (EntityMob :37-38) | none (`Monster` :35) | N/A | P3 |
| allies / species exclusions | SeaMonster (:504); hurt: a SeaMonster attacker never retargets (:358-359) | scan never yields a SeaMonster (players only); hurt: SeaMonster attacker → false (:136) | MATCH | hurt path only |
| ignore screen | none in orig | none | N/A | |
| tie-break / selection rule | GenericTargetSorter (:39, :55, sort :518) | vanilla nearest player, `<` (P5) | DIVERGES | tie-break |
| target set / release | `getAttackTarget()` kept while alive (:522-525) else nulled (:526); the scan result itself is never stored; persistent targets come only from hurt (:361) / `EntityAIHurtByTarget` (:62) | nearest player stored with `setTarget` (:177) and kept while alive (:180); `HurtByTargetGoal` (:75) | FIXED (ENT-S-129, wave 2)` — as Irukandji — was DIVERGES | release rule: port persists the found player until death, orig re-validated every pass |
| other | hurt retaliation: `EntityLiving` attacker → `setAttackTarget` + `setTarget` + navigate 1.2 (:357-364) | `Mob` attacker → `setTarget` + navigate 1.2 (:135-139) | MATCH | players excluded in both |

### SeaViper — orig `SeaViper.java`, port `SeaViper.java` — lane S4
| aspect | 1.7.10 (file:line) | port HEAD (file:line) | status | note |
|---|---|---|---|---|
| scan set | `EntityLivingBase.class`, bb.expand(18,4,18) (:534), `updateAITasks` :448 behind `nextInt(5)==1` (:482); :530-551 | `NearestAttackableTargetGoal<>(this, Player.class, true)` (:96): all players in the level within FOLLOW_RANGE 32 (:106) sphere, 1-in-5 per alternate-tick pass (P1); `HurtByTargetGoal` (:95); comment :84-85 says it "replace[s] the legacy 18×4×18 scan" | DIVERGES | scan-set narrowing: players only; 32-sphere vs 18x4x18 box; cadence; not in ENT-S-108's list |
| filter order | :504-528: null/self/dead → sight(:514) → player → !creative(:517-520) → SeaViper false(:521) → EntityMob true(:524) → `isAttackableNonMob`(:527) | vanilla `TargetingConditions.forCombat` chain (P1): self → spectator/dead → canAttack → allied → range → sight | DIVERGES | filter order: vanilla chain replaces the orig ladder; EntityMob / attackable-non-mob prey absent (P9) |
| PlayNicely gate | :531-533 → null | none | FIXED (ENT-S-115, wave 1) — as the player goal's live canUse; the bite goal's slot consumption stays with T5 — was DIVERGES | missing gate |
| creative gate | `isCreativeMode` :519 | vanilla `canBeSeenAsEnemy` → `abilities.invulnerable` (P1) | DIVERGES | missing gate (creative mapping); not in ENT-S-109's list |
| PEACEFUL gate | none (EntityMob :40-41) | none (`Monster` :43); vanilla rejects players in PEACEFUL | N/A | P3 |
| allies / species exclusions | SeaViper (:521); hurt: SeaViper attacker → false (:375-377) | goal is Player.class; hurt: SeaViper attacker → false (:196) | MATCH | hurt path only |
| ignore screen | none in orig | none | N/A | |
| tie-break / selection rule | GenericTargetSorter (:42, :59, sort :535) | vanilla nearest, `<` | DIVERGES | tie-break |
| target set / release | `getAttackTarget()` kept while alive (:539-543); scan result not stored; hurt (:378-380) / `EntityAIHurtByTarget` (:66) set it | goal-set target; released per P1 (dead / creative / PEACEFUL-player / >32 / unseen 60t); `SeaViperBiteGoal` (ai/SeaViperBiteGoal.java:16-31, Presets.seaViper forgetTargetRoll 0, ai/DinosaurMeleeAttackGoal.java:44) pursues until dead | FIXED (ENT-S-129, wave 2) — the PlayNicely stand-down; the vanilla player goal's hold stays with T3b — was DIVERGES | release rule |
| other | hurt retaliation :374-381 (`EntityLiving`) | :202-205 (`Mob`) | MATCH | |

### Skate — orig `Skate.java`, port `Skate.java` — lane S4
| aspect | 1.7.10 (file:line) | port HEAD (file:line) | status | note |
|---|---|---|---|---|
| scan set | `EntityLivingBase.class`, bb.expand(10,4,10) (:286), `updateAITasks` :214 behind `nextInt(8)==1` (:245); :282-303 | `getNearestPlayer(this, 10.0)` (:124) when `getTarget()==null` (:122-123), `customServerAiStep` :96 behind `nextInt(8)==1` (:121) | DIVERGES | scan-set geometry: sphere r10 (all players) vs 10x4x10 box; orig's filter also admits only players (:275-279), so the class narrowing is not observable |
| filter order | :262-280: null/self/dead → sight(:272) → player → !creative(:275-278) → false(:279) | :125 `!instabuild` only | FIXED (ENT-S-118, wave 2) — as Irukandji — was DIVERGES | missing gate: sight (:272) |
| PlayNicely gate | :283-285 → null | none | FIXED (ENT-S-115, wave 1) — the inline pick gated as a whole — was DIVERGES | missing gate |
| creative gate | `isCreativeMode` :277 | `instabuild` :125 | MATCH | P6 |
| PEACEFUL gate | none (EntityMob :31-32) | none (`Monster` :33) | N/A | P3 |
| allies / species exclusions | none (players only); hurt: Skate attacker → false (:135-137) | none; hurt: Skate attacker → false (:87) | MATCH | |
| ignore screen | none in orig | none | N/A | |
| tie-break / selection rule | GenericTargetSorter (:33, :48, sort :287) — every player has the same silhouette weight, so it reduces to nearest player, first-encountered on ties | vanilla nearest player, `<` (first-encountered on ties) | MATCH | same choice; different mechanism |
| target set / release | `getAttackTarget()` kept while alive (:291-295); scan result not stored; hurt (:142-144) / `EntityAIHurtByTarget` (:53) | nearest player stored via `setTarget` (:127), kept while alive (:130); `HurtByTargetGoal` (:57) | FIXED (ENT-S-129, wave 2)` — as Irukandji — was DIVERGES | release rule |
| other | hurt retaliation :138-145 (`EntityLiving`) | :88-91 (`Mob`) | MATCH | |

### SpiderDriver — orig `SpiderDriver.java`, port `SpiderDriver.java` — lane S4
| aspect | 1.7.10 (file:line) | port HEAD (file:line) | status | note |
|---|---|---|---|---|
| scan set | two scans from `updateAITasks` :60-84: (a) mount: `SpiderRobot.class`, bb.expand(25,15,25) (:107) behind `!PEACEFUL && nextInt(5)==0 && ridingEntity==null` (:66), first rider-less robot (:115); (b) mounted combat: `EntityLivingBase.class`, bb.expand(35,15,35) (:163) behind `!PEACEFUL && nextInt(4)==0 && ridingEntity!=null` (:74) | (a) `SpiderRobot.class`, inflate(25,15,25) (:149-150) behind :102, `!robot.isVehicle()` (:153); (b) `LivingEntity.class`, inflate(35,15,35) (:167-168) behind :121 | MATCH | boxes, classes, gates and cadence match |
| filter order | :121-157: PEACEFUL(:122) → null/self/dead(:125-133) → ignore(:134) → SpiderRobot(:137) → SpiderDriver(:140) → EntitySpider(:143) → EntityCaveSpider(:146) → sight(:149) → player → !creative(:152-155) → `!(distSq < 36)`(:156) | :156-164: PEACEFUL(:157) → null/self/dead(:158) → ignore(:159) → SpiderRobot/SpiderDriver(:160) → Spider/CaveSpider(:161) → player → !instabuild(:162) → `!(distSq < 36)`(:163) | FIXED (ENT-S-118, wave 2) — was DIVERGES | missing gate: the sight check (:149-151) is absent |
| PlayNicely gate | :104-106 (mount scan) and :160-162 (combat scan) → null | none in either scan (:148-154, :166-171) | FIXED (ENT-S-115, wave 1) — was DIVERGES | missing gate (both scans) |
| creative gate | `isCreativeMode` :154 | `instabuild` :162 | MATCH | P6 |
| PEACEFUL gate | cadence :66/:74 + filter :122 | cadence :102/:121 + filter :157 | MATCH | |
| allies / species exclusions | SpiderRobot, SpiderDriver, EntitySpider, EntityCaveSpider (:137-148) | same four (:160-161) | MATCH | |
| ignore screen | :134 after dead, before the spider exclusions | :159 same position | FIXED (ENT-S-106) | |
| tie-break / selection rule | GenericTargetSorter (:33, sorts :108 and :164) | plain `Comparator.comparingDouble(this::distanceToSqr)` (:52, used :153/:170) | DIVERGES | tie-break: TF-035 remainder, not swapped in the ENT-S-017/018/019 batch |
| target set / release | no stored target: each pass rescans (:74-83); `EntityAIHurtByTarget` (:41) sets an attack target nothing consumes (no attack task; the legacy `attackEntity` :86-94 and `findPlayerToAttack` :55-58 never run because `isAIEnabled` is true :51-53 — 1.7.10 runs `updateAITasks`, not `updateEntityActionState`) | no stored target (:121-145); `HurtByTargetGoal` (:79) with no consumer; `registerGoals` :74-80 drops vanilla `Spider`'s player-target goals | MATCH | effective behaviour equal; orig legacy path is dead code |
| other | robot steering toward far prey (:76-82) | :127-135 | MATCH | not a selection rule |

### SpiderRobot — orig `SpiderRobot.java`, port `SpiderRobot.java` — lane S4
| aspect | 1.7.10 (file:line) | port HEAD (file:line) | status | note |
|---|---|---|---|---|
| scan set | from `onUpdate` :587, ridden only: (a) frontal `EntityLivingBase.class`, bb.expand(20,12,20) (:975), first suitable in raw list order, behind `!PEACEFUL && !remote && rider!=null && nextInt(15)==0` (:593); (b) stomp `EntityLivingBase.class`, bb.expand(20,8,20) (:900), EVERY suitable hit, behind `... nextInt(40)==0` (:590) | `tick()` :282: (a) inflate(20,12,20) (:570), raw order (:572-574), gate :295-296; (b) inflate(20,8,20) (:619), all hit (:621-623), gate :289-290 | MATCH | ENT-S-021 |
| filter order | frontal :988-1038: null/self/dead → SpiderRobot(:998) → EntitySpider(:1001) → SpiderDriver(:1004) → EntityCaveSpider(:1007) → == rider(:1010) → ignore(:1013) → sight(:1016) → bearing(:1019-1026) → distSq<36 true(:1027-1029) → bearing>0.75 false(:1030-1032) → player → !creative(:1033-1036) → true(:1037). Stomp :912-952: null/self/dead → same four kinds → == rider(:934) → dist>18 false(:941) → dist<12 false(:944) → player → !creative(:947-950) → true | frontal :586-609 same order; stomp :632-647 same order | MATCH | point-blank creative bypass quirk kept both sides (:1027 / :605) |
| PlayNicely gate | :972-974 (frontal), :897-899 (stomp) | :569, :618 | MATCH | |
| creative gate | `isCreativeMode` :1035, :949 | `instabuild` :607, :645 | MATCH | P6 |
| PEACEFUL gate | cadence gates :590, :593 (class is `EntityLiving` :41-42, no P3 despawn) | :289, :295 (`Mob` :50) | MATCH | |
| allies / species exclusions | SpiderRobot, EntitySpider, SpiderDriver, EntityCaveSpider, the rider | same (:588-592, :634-638) | MATCH | |
| ignore screen | frontal :1013 after the kind/rider exclusions, before sight; none in the stomp | :593 same; none in stomp (:632-647) | MATCH | shared list FIXED (ENT-S-101) |
| tie-break / selection rule | frontal: NO sort — first suitable in `getEntitiesWithinAABB` order (:975-985) although a sorter exists (:50, :60); stomp: all | frontal unsorted (:571-574, comment :565-568); stomp all | MATCH | quirk kept |
| target set / release | nothing stored; hit immediately (:599 / :908); no target tasks (`field_70715_bh` never used) | nothing stored (:302, :622); `registerGoals` :233-236 has no target goal | MATCH | |
| other | attacks only while ridden (:590, :593) | :290, :296; AI suspended while ridden (:262, citing orig :94-102) | MATCH | |

### SpitBug — orig `SpitBug.java`, port `EntitySpitBug.java` — lane S4
| aspect | 1.7.10 (file:line) | port HEAD (file:line) | status | note |
|---|---|---|---|---|
| scan set | `EntityLivingBase.class`, bb.expand(12,7,12) (:374), `updateAITasks` :258 behind `nextInt(5)==0` (:267), current target kept if alive (:268-272) else scan (:273-275); :370-386 | `NearestAttackableTargetGoal<>(this, Player.class, true, !isIgnoreable)` (:71-72), FOLLOW_RANGE 32 (:82), P1 cadence; `HurtByTargetGoal` (:68) | FIX IN FLIGHT (ENT-S-108) | players-only; the 32-sphere/cadence difference rides with the same rebuild |
| filter order | :324-368: null/self/dead → ignore(:334) → sight(:337) → EnderReaper(:340) → EnderKnight(:343) → Enderman(:346) → Hydrolisc(:349) → Creeper(:352) → SpitBug(:355) → TrooperBug(:358) → player creative false(:361-366) → true(:367) | predicate `!isIgnoreable` then the vanilla chain (P1) | FIX IN FLIGHT (ENT-S-108) | fix shape restores the orig exclusion chain |
| PlayNicely gate | :371-373 → null | none | FIXED (ENT-S-108; pinned by ENT-S-115, wave 1) — gate at EntitySpitBug.java:261 — was DIVERGES | missing gate; not named in ENT-S-108 |
| creative gate | `isCreativeMode` :363 | vanilla `invulnerable` (P1) | DIVERGES | missing gate (creative mapping); not in ENT-S-109's list |
| PEACEFUL gate | none (EntityMob :45-46) | none (`Monster` :31) | N/A | P3 |
| allies / species exclusions | EnderReaper, EnderKnight, Enderman, Hydrolisc, Creeper, SpitBug, TrooperBug (:340-360) | none (moot for Player.class) | FIX IN FLIGHT (ENT-S-108) | |
| ignore screen | :334 after dead, before sight | goal predicate, ahead of the LoS test (:71-72) | FIXED (ENT-S-106) | cannot bite until ENT-S-108 |
| tie-break / selection rule | GenericTargetSorter (:47, :61, sort :375) | vanilla nearest, `<` | DIVERGES | tie-break |
| target set / release | `getAttackTarget()` kept while alive (:268-272); scan result not stored; hurt (:247-253) sets it | goal-set target released per P1; `SpitBugAcidAttackGoal` (ai/SpitBugAcidAttackGoal.java:33-94, Params.spitBug forgetTargetRoll 0 :71) pursues until dead | FIXED (ENT-S-129, wave 2)` — the convention — was DIVERGES | release rule |
| other | hurt retaliation :247-253 (`EntityLiving`, after hurt-timer/cactus/fall filter) | :168-172 (`Mob`) | MATCH | |

### Spyro — orig `Spyro.java`, port `EntitySpyro.java` — lane S4
| aspect | 1.7.10 (file:line) | port HEAD (file:line) | status | note |
|---|---|---|---|---|
| scan set | `EntityLivingBase.class`, bb.expand(12,6,12) (:701), from `do_movement` (called every server tick from `onUpdate` :473) behind `nextInt(6)==1 && !PEACEFUL` (:588); `do_movement` exits when sitting (:566) or `activity == 1` (:569-571) → hunts only while flying; ctor :73-81 registers NO target tasks | `LivingEntity.class`, inflate(12,6,12) (:484), from `doMovement` (called from `tick()` :199) behind `nextInt(6)==1 && !PEACEFUL` (:380); exits when sitting (:362) or `activity == 1` (:363) | MATCH | the port's added goals are in the release row |
| filter order | :672-695: PEACEFUL(:673) → null/self/dead → sight(:685) → Spyro false(:688) → Mothra true(:691) → `instanceof EntityMob`(:694); plus a second test in the scan loop: `canSeeTarget(x,y,z)` eye(y+0.75)→feet block ray (:709, :436-438) | :475-481: PEACEFUL → null/self/dead → sight → EntitySpyro false → `instanceof Monster`; no second ray (:483-490) | FIXED (ENT-S-118, wave 2) — the feet ray (:709, :436-438); Mothra-as-prey (:691) stays T6 — was DIVERGES | prey set: Mothra (a Butterfly, not a Monster in the port) no longer prey; the feet-ray gate is missing |
| PlayNicely gate | :698-700 → null | none in EntitySpyro.java | FIXED (ENT-S-115, wave 1) — was DIVERGES | missing gate (BOSS-017 covered bosses + Princess only) |
| creative gate | none (no player prey) | none | N/A | |
| PEACEFUL gate | :588 + :673 | :380 + :476 | MATCH | |
| allies / species exclusions | Spyro (:688) | EntitySpyro (:479) | MATCH | |
| ignore screen | none | none | N/A | |
| tie-break / selection rule | GenericTargetSorter (:55, :82, sort :702) | GenericTargetSorter field (:78) via firstMatch (:489) | MATCH | P5 |
| target set / release | nothing stored (scan result used directly); `setRevengeTarget(null)` 1-in-200 (:488-490) | `setTarget(null)` 1-in-200 (:333-335) maps the revenge reset to the attack target; `OwnerHurtByTargetGoal`, `OwnerHurtTargetGoal`, `HurtByTargetGoal`, `NearestAttackableTargetGoal<>(Monster.class, 10, true, false, e -> isTame())` (:101-104) set `getTarget()`, which `doMovement` never reads | RECORDED (MOD-033, ENT-S-125) — the four goals register only in modern (registered-but-unconsumed); classic = no target goals (orig :73-81) — was PORT-ONLY | four target goals with no orig counterpart; the custom path ignores them |
| other | tamed ∧ health < 25% → flee from the found target (:591-595); owner rules do not filter prey | :383-389 | MATCH | |

### StinkBug — orig `StinkBug.java`, port `EntityStinkBug.java` — lane S4
| aspect | 1.7.10 (file:line) | port HEAD (file:line) | status | note |
|---|---|---|---|---|
| no targeting | `EntityAnimal` (:37-38); ctor :47-54 registers goal tasks only, no scan, no `isSuitableTarget`; `setRevengeTarget(null)` 1-in-200 (:78-80); the death gas (:95-103) hits every `EntityLivingBase` in x±8 / y−5..+10 / z±8 with no selection | `Animal` (:35); `registerGoals` :47-55 no target goal; `setTarget(null)` 1-in-200 (:107-109) maps the revenge reset; death gas :92-99 same box, no selection | N/A | no target selection in either tree |

### Stinky — orig `Stinky.java`, port `EntityStinky.java` — lane S4
| aspect | 1.7.10 (file:line) | port HEAD (file:line) | status | note |
|---|---|---|---|---|
| scan set | `EntityLivingBase.class`, bb.expand(12,6,12) (:691), from `do_movement` (:534, called from `updateAITasks` whenever not sitting :511) behind `nextInt(7)==1 && !PEACEFUL` (:568) — the combat roll runs BEFORE the `activity == 1` return (:582-607), so a grounded Stinky hunts too; ctor :67-77 registers NO target tasks | `LivingEntity.class`, inflate(12,6,12) (:450), from `doMovement` (:280) behind `nextInt(7)==1 && !PEACEFUL` (:329) — but `doMovement` returns first when `activity != 2` (:312), so the port hunts only while flying | DIVERGES | scan-set narrowing (state gate): grounded Stinky never scans in the port |
| filter order | :665-685: PEACEFUL(:666) → null/self/dead → sight(:678) → Mothra true(:681) → `instanceof EntityMob`(:684); plus `canSeeTarget` feet ray in the loop (:699, :317-319) | :442-447: PEACEFUL → null/self/dead → sight → `instanceof Monster`; no feet ray (:449-455) | FIXED (ENT-S-118, wave 2) — the feet ray (:699, :317-319); Mothra (:681) stays T6 — was DIVERGES | prey set: Mothra dropped; feet-ray gate missing |
| PlayNicely gate | :688-690 → null | none | FIXED (ENT-S-115, wave 1) — was DIVERGES | missing gate |
| creative gate | none | none | N/A | |
| PEACEFUL gate | :568 + :666 | :329 + :443 | MATCH | |
| allies / species exclusions | none (no self-kind check; a Stinky is not EntityMob) | none | MATCH | |
| ignore screen | none | none | N/A | |
| tie-break / selection rule | GenericTargetSorter (:48, :76, sort :692) | plain `comparingDouble(distanceToSqr)` (:454) | DIVERGES | tie-break: TF-035 remainder |
| target set / release | nothing stored; `setRevengeTarget(null)` 1-in-200 (:502-504) | `setTarget(null)` 1-in-200 (:249-251); `OwnerHurtByTargetGoal`/`OwnerHurtTargetGoal`/`HurtByTargetGoal`/`NearestAttackableTargetGoal<>(Monster.class, 10, true, false, e -> isTame())` (:102-105) unread by `doMovement` | RECORDED (MOD-033, ENT-S-125) — as Spyro (orig :67-77) — was PORT-ONLY | as Spyro |
| other | tamed ∧ health < 25% → flee (:569-572) | :332-337 | MATCH | |

### TerribleTerror — orig `TerribleTerror.java`, port `EntityTerribleTerror.java` — lane S4
| aspect | 1.7.10 (file:line) | port HEAD (file:line) | status | note |
|---|---|---|---|---|
| scan set | `EntityLivingBase.class`, bb.expand(12,8,12) (:299), `updateAITasks` :119: the hunt is the `else if nextInt(9)==0` branch (:148-157) of the flight-retarget test (:130); :295-311; ctor :49-57 registers NO target tasks | `LivingEntity.class`, inflate(12,8,12) (:169), `customServerAiStep` :100: `else if nextInt(9)==0` (:126-134) of the same test (:108); `registerGoals` :47-48 empty | MATCH | |
| filter order | :216-293: null/self/dead → sight(:226) → RockBase(:229) → TerribleTerror(:232) → EnderReaper(:235) → Mothra(:238) → LurkingTerror(:241) → CloudShark(:244) → Rotator(:247) → Bee(:250) → Mantis(:253) → LeafMonster(:256) → CreepingHorror(:259) → Triffid(:262) → PitchBlack(:265) → Dragon(:268) → Island(:271) → IslandToo(:274) → EntityButterfly(:277) → Firefly(:280) → Triffid again(:283) → player creative false(:286-291) → true(:292) | :150-166: null/self/dead → sight → EntityTerribleTerror → RockBase → EnderReaper → CloudShark → EntityRotator → PitchBlack → CreepingHorror → Island → IslandToo → player → !invulnerable → true | MATCH | order of the retained side-effect-free steps is immaterial; the missing entries are counted in the allies row |
| PlayNicely gate | :296-298 → null | none (:168-174) | FIXED (ENT-S-115, wave 1) — was DIVERGES | missing gate |
| creative gate | `isCreativeMode` :288 | `invulnerable` :163 | FIX IN FLIGHT (ENT-S-109) | |
| PEACEFUL gate | none (EntityMob :44-45) | none (`Monster` :29) | N/A | P3 |
| allies / species exclusions | 19 spared kinds (:229-285) | 10 kinds (:153-161): Mothra, LurkingTerror, Bee, Mantis, LeafMonster, Triffid, Dragon, EntityButterfly, Firefly are MISSING | FIXED (ENT-S-128, wave 2) — was DIVERGES | exclusion list (9 species hunted that 1.7.10 spared); no record (ENT-S-112 is PitchBlack's own list) |
| ignore screen | none (RockBase/Butterfly/Firefly are named explicitly instead) | none | N/A | |
| tie-break / selection rule | GenericTargetSorter (:47, :56, sort :300) | plain `comparingDouble(distanceToSqr)` (:173) | DIVERGES | tie-break: TF-035 remainder |
| target set / release | nothing stored; flight target set to the prey (:152); hurt sets the flight target to the attacker (:184-191) | nothing stored; :129; hurt :90-97 | MATCH | |
| other | none | none | N/A | |

### TheKing — orig `TheKing.java`, port `TheKing.java` — lane S4
| aspect | 1.7.10 (file:line) | port HEAD (file:line) | status | note |
|---|---|---|---|---|
| scan set | `EntityLivingBase.class`, bb.expand(80,64,80) (:1004), preceded when `isEnd==2` by an `EntityPlayer.class` pass over the same box (:989-1003); from `updateAITasks` :340 in the `else if nextInt(attrand)==0` branch (:525), attrand 5 (:343) → 3 (:455, :486); :984-1021 | inflate(80,64,80) (:1199), `isEnd==2` player pass (:1201-1210), living pass (:1212-1229); `aiStepPrimary` :735 (via `KingPrimaryGoal.tick` ai/KingPrimaryGoal.java:106-108) branch :771, attackChance :738-750 | MATCH | |
| filter order | :925-982: null/self/dead → KingHead → head_found=1, false(:935-938) → royalty(:939) → >144 from home false(:942-946) → ignore(:947) → [isEnd==2: player → !creative(:951-954), Girlfriend true(:955), Boyfriend true(:958), Villager true(:961)] → `MyCanSee`(:965) → player → !creative(:968-971) → Horse(:972) → EntityMob(:975) → EntityDragon(:978) → `isAttackableNonMob`(:981) | :1159-1188 identical order (`MyCanSee` :1179) | MATCH | fallthrough membership in the allies row |
| PlayNicely gate | :985-988 (head_found=1, null) and the revenge null :527-529 | :1195-1198 and :779-781 | MATCH | BOSS-017 |
| creative gate | `isCreativeMode` :953, :970 | `instabuild` :1173, :1182 | MATCH | P6 |
| PEACEFUL gate | none (EntityMob :61-62) | none (`Monster` :103) | N/A | P3 |
| allies / species exclusions | KingHead, royalty (MyUtils.java:46-75), the shared ignore list; prey fallthrough `isAttackableNonMob` (MyUtils.java:77-115) | KingHead, royalty (util/MyUtils.java:9-19), ignore list; fallthrough util/MyUtils.java:54-63 | FIXED (ENT-S-128, wave 2) — the helper (:1187); correction: a Mothra is refused at the King's ignore screen (orig :947 ↔ port :1169, an EntityButterfly in both trees), not lost through the helper — was DIVERGES | exclusion list / prey set via P9: Mothra, Leon, Dragon, Spyro, GammaMetroid, WaterDragon, Girlfriend, Boyfriend, Villager, Stinky are no longer prey outside the isEnd==2 branch; unrecorded |
| ignore screen | :947 after the home-distance test, before the isEnd branch and `MyCanSee` | :1169 same position | MATCH | shared list FIXED (ENT-S-101) |
| tie-break / selection rule | GenericTargetSorter (:64, :95, sorts :991, :1005); the loop keeps probing after the first pick only to set head_found (:1014-1018) | GenericTargetSorter (:160) via firstMatch (:1208, :1229); head_found via a containment pass (:1217-1223) | RECORDED (OPT-016 / OPT-026) | order-preserving, ruled neutral |
| target set / release | `rt` set in `attackEntityFrom` for any living non-royalty attacker (:835-842); preferred over the scan (:526) unless PlayNicely (:527-529) or the attacker is a King/KingHead (:530-533); dropped when dead, on 1-in-250, or > 128 from home in guard mode (:538-541), or not `MyCanSee` (:542-544); scan result not stored; `EntityAIHurtByTarget` (:99) unread by combat | `revengeTarget` set in `hurt` (:1003-1009); :776-799 identical chain; `HurtByTargetGoal` (:219) unread | MATCH | MOD-022 covers non-persistence |
| other | big non-boss attackers (silhouette > 30, not royalty/Godzilla/GodzillaHead/PitchBlack/Kraken) take damage/10 and flag `large_unknown` (:816-823); small EntityMob attackers (< 3) are deleted and ignored (:824-827); home leash 144 (:942) / guard 128 (:538) | :981-990 uses `MyUtils.isBigBoss` (util/MyUtils.java:75-80: Godzilla, GodzillaHead, PitchBlack, Kraken); small Monster attackers discarded (:991-994) | RECORDED (MOD-002) | the deletion is the recorded original bug; the rest matches |

### ThePrince — orig `ThePrince.java`, port `ThePrince.java` — lane S4
| aspect | 1.7.10 (file:line) | port HEAD (file:line) | status | note |
|---|---|---|---|---|
| scan set | `EntityLivingBase.class`, bb.expand(12,6,12) (:768), from `do_movement` (:585, called from `updateAITasks` when not sitting :529-551) behind `nextInt(7)==1 && !PEACEFUL` (:619); the combat roll precedes the `activity == 1` return (:670-672), so grounded princes hunt; ctor :86-92 registers NO target tasks | `LivingEntity.class`, inflate(12,6,12) (:580), from `doMovement` (:284) behind `nextInt(7)==1 && !PEACEFUL` (:351); the `activity == 1` return follows the combat block (:402-404) | MATCH | |
| filter order | :727-762: PEACEFUL(:728) → null/self/dead → sight(:740) → royalty(:743) → EntityMob true(:746) → Mothra(:749) → EntityButterfly(:752) → Cockateil(:755) → Dragonfly(:758) → EntityMosquito(:761); plus the `canSeeTarget` feet ray in the loop (:776, :416-418) | :566-576 same ladder; no feet ray (:578-585) | FIXED (ENT-S-118, wave 2) — the feet ray (:776, :416-418) on the BOSS-024 ladder — was DIVERGES | missing gate: the eye(y+0.75)→feet block ray (:776) is absent; ladder restored under BOSS-024 |
| PlayNicely gate | :765-767 → null | :579 | MATCH | BOSS-017 / BOSS-024 |
| creative gate | none (no player prey) | none | N/A | |
| PEACEFUL gate | :619 + :728 | :351 + :567 | MATCH | |
| allies / species exclusions | royalty (:743) | `MyUtils.isRoyalty` (:570) | MATCH | |
| ignore screen | none | none | N/A | |
| tie-break / selection rule | GenericTargetSorter (:61, :93, sort :769) | GenericTargetSorter (:102) via firstMatch (:584) | MATCH | P5 |
| target set / release | nothing stored; `setRevengeTarget(null)` 1-in-200 (:513-515) | `setLastHurtByMob(null)` 1-in-200 (:240-242); `OwnerHurtByTargetGoal`/`OwnerHurtTargetGoal`/`HurtByTargetGoal`/`NearestAttackableTargetGoal<>(Monster.class, 10, true, false, e -> isTame())` (:114-117) set `getTarget()`, unread by `doMovement` | RECORDED (MOD-033, ENT-S-125) — as Spyro (orig :86-92) — was PORT-ONLY | four target goals with no orig counterpart |
| other | untamed: auto-tames to the nearest player within 10 (:522-528); tamed ∧ health < 25% → flee (:622-626) | :252-259; :354-363 | MATCH | |

### ThePrinceAdult — orig `ThePrinceAdult.java`, port `ThePrinceAdult.java` — lane S4
| aspect | 1.7.10 (file:line) | port HEAD (file:line) | status | note |
|---|---|---|---|---|
| scan set | custom: `EntityLivingBase.class`, bb.expand(32,20,32) (:523), reached from (a) ground spotting `!sitting ∧ activity==0 ∧ no rider ∧ !PEACEFUL ∧ nextInt(10)==1` (:392-399), (b) `fly_with_rider` `nextInt(5)==1 ∧ !PEACEFUL` (:454), (c) `do_movement` `!toofar ∧ flyaway==0 ∧ !PEACEFUL ∧ nextInt(6)==1` (:708) after `getAttackTarget()` (:709-716). Vanilla task: `EntityAINearestAttackableTarget(EntityLiving.class, 0, sight, false, IMob selector)` registered only when PlayNicely==0 (:112-114): bb.expand(followRange 16, 4, 16), IMob only, every 3rd tick; `EntityAIHurtByTarget(false)` (:115) | custom: inflate(32,20,32) (:856); (a) :580-589, (b) :466-485, (c) :705-714. Vanilla: `NearestAttackableTargetGoal<>(this, Monster.class, true)` (:145), unconditional, FOLLOW_RANGE 64 (:155) → bb.inflate(64,4,64), P1 cadence; `HurtByTargetGoal` (:144); plus `OwnerHurtByTargetGoal`/`OwnerHurtTargetGoal` (:142-143) with no orig counterpart | DIVERGES | scan-set: the vanilla goal's box is 64x4x64 vs 16x4x16 and its release range 64 vs 16 (P1); custom scans match; the owner goals are PORT-ONLY |
| filter order | custom :476-517: PEACEFUL(:477) → null/self/dead → sight(:489) → royalty(:492) → EntityMob true(:495) → Mothra(:498) → Kraken(:501) → Leon !tamed(:504-507) → WaterDragon !tamed(:508-511) → GammaMetroid !tamed(:512-515) → false(:516). Vanilla task: IMob selector + `isSuitableTarget(…, false)` (alive, same-owner/owner excluded, sight) | custom :838-851 identical; vanilla: Monster.class + P1 chain (`isAlliedTo` excludes the owner) | MATCH | |
| PlayNicely gate | custom :520-522 → null; vanilla task not registered under PlayNicely (:112) | custom :855; vanilla goal :145 registered regardless | FIXED (ENT-S-115, wave 1) — was DIVERGES | missing gate (goal half) |
| creative gate | none (no player prey; a tamed adult never retaliates against players :378-380) | none; :280-282 | N/A | |
| PEACEFUL gate | :392, :454, :708 + :477 | :581, :468, :705 + :839 | MATCH | |
| allies / species exclusions | royalty; tamed Leon / WaterDragon / GammaMetroid spared (:504-515) | :842-849 | MATCH | |
| ignore screen | none | none | N/A | |
| tie-break / selection rule | GenericTargetSorter (:78, :117, sort :524); vanilla task plain-distance sorter | GenericTargetSorter (:131) via firstMatch (:860); vanilla nearest `<` | MATCH | P5 |
| target set / release | attack target from hurt (:381-383, any living attacker; not players when tamed :378-380) or the tasks; `do_movement` prefers `getAttackTarget()`, nulls it when dead (:709-713); `always_do` nulls it 1-in-250 (:420-422); scan results not stored | `hurt` :283 `setTarget`; :707-711; `alwaysDo` 1-in-250 `setTarget(null)` (:616-618); goal release per P1 (see scan row for the range) | MATCH | |
| other | ridden hunting (:443-467); tamed low-health flee (:718-723); flyaway break-off (:732) | :466-485; :716-725; :737 | MATCH | |

### ThePrincess — orig `ThePrincess.java`, port `ThePrincess.java` — lane S4
| aspect | 1.7.10 (file:line) | port HEAD (file:line) | status | note |
|---|---|---|---|---|
| scan set | `EntityLivingBase.class`, bb.expand(12,6,12) (:849), from `do_movement` (:651) behind `nextInt(7)==1 && !PEACEFUL` (:706); the `activity == 1` return follows the combat block (:757-759); ctor :86-92 registers NO target tasks | inflate(12,6,12) (:595), `doMovement` behind :459; the `activity == 1` return follows (:510-512) | MATCH | |
| filter order | :814-843: PEACEFUL(:815) → null/self/dead → sight(:827) → royalty(:830) → EntityMob true(:833) → Mothra(:836) → Dragonfly(:839) → EntityMosquito(:842); plus the `canSeeTarget` feet ray (:857, :404-406) | :579-590 same ladder; no feet ray (:593-600) | FIXED (ENT-S-118, wave 2) — was DIVERGES | missing gate: feet ray |
| PlayNicely gate | :846-848 → null | :594 | MATCH | BOSS-017 |
| creative gate | none | none | N/A | |
| PEACEFUL gate | :706 + :815 | :459 + :580 | MATCH | |
| allies / species exclusions | royalty (:830) | :583 | MATCH | |
| ignore screen | none | none | N/A | |
| tie-break / selection rule | GenericTargetSorter (:59, :93, sort :850) | GenericTargetSorter (:100) via firstMatch (:599) | MATCH | P5 |
| target set / release | nothing stored; `setRevengeTarget(null)` 1-in-200 (:513) | `setLastHurtByMob(null)` 1-in-200 (:226-228); `OwnerHurtByTargetGoal`/`OwnerHurtTargetGoal`/`HurtByTargetGoal`/`NearestAttackableTargetGoal<>(Monster.class, 10, true, false, e -> isTame())` (:112-115) unread by `doMovement` | RECORDED (MOD-033, ENT-S-125) — as Spyro (orig :86-92) — was PORT-ONLY | as ThePrince |
| other | untamed auto-tame within 10 (:525); tamed low-health flee (:709-713) | :244-250; :462-471 | MATCH | |

### ThePrinceTeen — orig `ThePrinceTeen.java`, port `ThePrinceTeen.java` — lane S4
| aspect | 1.7.10 (file:line) | port HEAD (file:line) | status | note |
|---|---|---|---|---|
| scan set | custom: `EntityLivingBase.class`, bb.expand(25,20,25) (:543), from (a) ground spotting (:398-405), (b) `fly_with_rider` `nextInt(5)==1` (:474), (c) `do_movement` `nextInt(7)==1` (:728) after `getAttackTarget()` (:729-736). Vanilla: `EntityAINearestAttackableTarget(EntityLiving.class, 0, sight, false, IMob)` only when PlayNicely==0 (:116-118), box 16x4x16, every 3rd tick; `EntityAIHurtByTarget(false)` (:119) | custom inflate(25,20,25) (:880); (a) :585-594, (b) :468-482, (c) :710-719. Vanilla `NearestAttackableTargetGoal<>(this, Monster.class, true)` (:156) unconditional, FOLLOW_RANGE 32 (:166) → 32x4x32; `HurtByTargetGoal` (:155); owner goals (:153-154) PORT-ONLY | DIVERGES | scan-set: goal box/release 32 vs 16 |
| filter order | :496-537 same ladder as the Adult (royalty, EntityMob, Mothra, Kraken, untamed Leon/WaterDragon/GammaMetroid) | :862-875 identical | MATCH | |
| PlayNicely gate | :540-542; task gated at construction (:116) | :879; goal :156 ungated | FIXED (ENT-S-115, wave 1) — was DIVERGES | missing gate (goal half) |
| creative gate | none | none | N/A | |
| PEACEFUL gate | :398, :474, :728 + :497 | :586, :469, :710 + :863 | MATCH | |
| allies / species exclusions | as Adult (:512-535) | :866-873 | MATCH | |
| ignore screen | none | none | N/A | |
| tie-break / selection rule | GenericTargetSorter (:79, :121, sort :544) | plain `comparingDouble(distanceToSqr)` (:142, used :884) | DIVERGES | tie-break: TF-035 remainder |
| target set / release | hurt :387-391 (not players when tamed :384-386); `getAttackTarget()` first (:729-733); `always_do` 1-in-250 null (:440-442) | :284; :712-716; :622-624 | MATCH | |
| other | ridden hunting (:463-487); low-health flee (:738-743) | :467-482; :721-730 | MATCH | |

### TheQueen — orig `TheQueen.java`, port `TheQueen.java` — lane S4
| aspect | 1.7.10 (file:line) | port HEAD (file:line) | status | note |
|---|---|---|---|---|
| scan set | `EntityLivingBase.class`, bb.expand(80,60,80) (:937), from `updateAITasks` :302 in the `else if nextInt(attrand)==0` branch (:529), attrand 5 (:309) → 3 when `player_hit_count < 10 ∧ health < max/2` (:472-474); :932-954 | inflate(80,60,80) (:1397), `aiStepPrimary` :1038 (via `QueenPrimaryGoal.tick`, ai/QueenPrimaryGoal.java:95-98, every tick :90-93) branch :1078, attackChance :1046-1048; :1389-1417 | MATCH | |
| filter order | :888-930: null/self/dead → QueenHead → head_found=1, false(:898-901) → royalty(:902) → >144 from home false(:905-909) → ignore(:910) → sight (`EntitySenses`, :913) → player → !creative(:916-919) → Horse(:920) → EntityMob(:923) → EntityDragon(:926) → `isAttackableNonMob`(:929) | :1367-1387 identical (sight :1378) | MATCH | fallthrough membership in the allies row |
| PlayNicely gate | `PlayNicely != 0 \|\| isHappy()` at :933-936 (head_found=1, null) and :532-534 (revenge null); `isHappy()` = mood 0 (:206-208) | :1393-1396 and :1083-1085 | MATCH | BOSS-017 |
| creative gate | `isCreativeMode` :918 | `instabuild` :1381 | MATCH | P6 |
| PEACEFUL gate | none (EntityMob :53-54) | none (`Monster` :129) | N/A | P3 |
| allies / species exclusions | QueenHead, royalty, ignore list; prey fallthrough `isAttackableNonMob` (MyUtils.java:77-115) | :1369-1370, :1377; fallthrough util/MyUtils.java:54-63 | FIXED (ENT-S-128, wave 2) — as the King (:1386; the ignore screen orig :910 ↔ port :1377) — was DIVERGES | exclusion list / prey set via P9 (same ten species as the King); unrecorded |
| ignore screen | :910 after the home-distance test, before sight | :1377 same | MATCH | shared list FIXED (ENT-S-101) |
| tie-break / selection rule | GenericTargetSorter (:56, :88, sort :938) | GenericTargetSorter (:234) via firstMatch (:1416); head_found via containment (:1402-1408) | RECORDED (OPT-016) | |
| target set / release | `rt` set in `attackEntityFrom` for any living non-royalty attacker (:801-808); preferred (:531) unless PlayNicely/happy (:532-534) or Queen/QueenHead (:535-538); dropped when dead, 1-in-450, > 128 from home in guard mode (:543-546), or not `MyCanSee` (:547-549); scan result not stored; `EntityAIHurtByTarget` (:92) unread | `revengeTarget` in `hurt` (:700-705); :1079-1102 identical; `HurtByTargetGoal` (:273) unread | MATCH | MOD-022 |
| other | mood: any real hit sets mood 1 (:775), full health 1-in-500 → 0 (:445-447), `always_mad` forces 1 (:448-450); PurplePower attackers ignored (:786-788); small EntityMob attackers deleted (:790-793); health-tracked victim `ev` removed at 0 HP (:243-267) | :657, :826-836; :671; :673-676; `finishTrackedVictim` :780-794 | RECORDED (MOD-001 / MOD-002) | mood rules match; deletions are recorded originals |

### TRex — orig `TRex.java`, port `TRex.java` — lane S4
| aspect | 1.7.10 (file:line) | port HEAD (file:line) | status | note |
|---|---|---|---|---|
| scan set | `EntityLivingBase.class`, bb.expand(20,6,20) (:254), `updateAITasks` :177 behind `nextInt(5)==1` (:182); the revenge field `rt` is preferred (:184) and the scan runs only when it is null (:197-199); :250-266 | `NearestAttackableTargetGoal<>(this, Player.class, true, !isIgnoreable)` (:58-59), FOLLOW_RANGE 40 (:70), P1 cadence; `HurtByTargetGoal` (:55) | FIX IN FLIGHT (ENT-S-108) | players-only; 40-sphere vs 20x6x20 rides with the rebuild |
| filter order | :216-248: null/self/dead → ignore(:226) → sight(:229) → TRex(:232) → Cryolophosaurus(:235) → VelocityRaptor(:238) → player creative false(:241-246) → true(:247) | predicate + vanilla chain (P1) | FIX IN FLIGHT (ENT-S-108) | |
| PlayNicely gate | :185-187 nulls `rt`; :251-253 nulls the scan | none | FIXED (ENT-S-115 :185 / ENT-S-108 :251, wave 1) — as Nastysaurus — was DIVERGES | missing gate (both sites) |
| creative gate | `isCreativeMode` :243 | vanilla `invulnerable` (P1/P2) | DIVERGES | missing gate (creative mapping); not in ENT-S-109's list |
| PEACEFUL gate | none (EntityMob :38-39) | none (`Monster` :28) | N/A | P3 |
| allies / species exclusions | TRex, Cryolophosaurus, VelocityRaptor (:232-240) | none (moot for Player.class) | FIX IN FLIGHT (ENT-S-108) | |
| ignore screen | :226 after dead, before sight | goal predicate ahead of LoS (:58-59) | FIXED (ENT-S-106) | |
| tie-break / selection rule | GenericTargetSorter (:40, :50, sort :255) | vanilla nearest, `<` | DIVERGES | tie-break |
| target set / release | `rt` = last living attacker of any kind incl. players (:169-172, set in `attackEntityFrom` after the cactus filter); released when dead, on a 1-in-200 roll, when out of sight, or under PlayNicely (:189-195); scan result never stored; `EntityAIHurtByTarget` (:56) sets an attack target the combat code does not read | revenge via `HurtByTargetGoal` (:55, P2: creative attackers rejected up front); goal targets released per P1; `DinosaurMeleeAttackGoal` (Presets.trex forgetTargetRoll 0, ai/DinosaurMeleeAttackGoal.java:34) pursues until dead — no 1-in-200 forgiveness, no LoS re-check | FIXED (ENT-S-129, wave 2)` — as Nastysaurus (1-in-200) — was DIVERGES | release rule |
| other | `attackEntityFrom` :165-175 records `rt` | `hurt` :144-150 records nothing (vanilla `lastHurtByMob` does) | MATCH | via P2 |

### Triffid — orig `Triffid.java`, port `EntityTriffid.java` — lane S4
| aspect | 1.7.10 (file:line) | port HEAD (file:line) | status | note |
|---|---|---|---|---|
| scan set | `EntityLivingBase.class`, bb.expand(10,8,10) (:325), from (a) `onUpdate` every tick while `hurt_timer <= 0`, facing only (:119-124), (b) `updateAITasks` behind `nextInt(10)==1 ∧ hurt_timer <= 0` (:255), attack; :321-337 | (a) `tick()` server side (:136-144), (b) `customServerAiStep` :216; inflate(10,8,10) (:242); :240-246 | MATCH | |
| filter order | :275-319: null/self/dead → ignore(:285) → sight(:288) → Creeper false(:291) → EnderReaper(:294) → Triffid(:297) → TerribleTerror(:300) → LurkingTerror(:303) → PitchBlack(:306) → Dragon(:309) → player creative false(:312-317) → true(:318) | :248-255: null/self/dead → ignore(:250) → EntityTriffid(:251) → sight(:252) → player → !invulnerable(:253) → `!(target instanceof Monster)`(:254) | MATCH | the Triffid/sight swap is between side-effect-free steps; the prey-set change is in the allies row |
| PlayNicely gate | :322-324 → null | none (:240-246) | FIXED (ENT-S-115, wave 1) — was DIVERGES | missing gate |
| creative gate | `isCreativeMode` :314 | `invulnerable` :253 | FIX IN FLIGHT (ENT-S-109) | |
| PEACEFUL gate | none (EntityMob :40-41) | none (`Monster` :31) | N/A | P3 |
| allies / species exclusions | Creeper, EnderReaper, Triffid, TerribleTerror, LurkingTerror, PitchBlack, Dragon (:291-311); every other EntityMob (zombies, skeletons, OreSpawn Monsters) is prey | EntityTriffid + a blanket `!Monster` (:254): all Monsters spared, the Dragon (a tameable, not a Monster) no longer spared | FIXED (ENT-S-128, wave 2) — the named seven :268-274, orig's Monster fallthrough `return true` :276 — was DIVERGES | exclusion list: blanket Monster exclusion vs the named seven; Dragon hunted |
| ignore screen | :285 after dead, before sight | :250 after dead, before sight | FIXED (ENT-S-106) | |
| tie-break / selection rule | GenericTargetSorter (:42, :54, sort :326) | plain `comparingDouble(distanceToSqr)` (:245) | DIVERGES | tie-break: TF-035 remainder |
| target set / release | nothing stored; `EntityAIHurtByTarget` (:59) unread | nothing stored; `HurtByTargetGoal` (:59) unread | MATCH | |
| other | hunting suspended while `hurt_timer > 0` (:255, :119) | :216, :136 | MATCH | |

### TrooperBug — orig `TrooperBug.java`, port `EntityTrooperBug.java` — lane S4
| aspect | 1.7.10 (file:line) | port HEAD (file:line) | status | note |
|---|---|---|---|---|
| scan set | `EntityLivingBase.class`, bb.expand(12,7,12) (:514), `updateAITasks` :404 behind `nextInt(5)==0` (:413), current target kept if alive (:414-418) else scan (:419-421); :510-526 | `NearestAttackableTargetGoal<>(this, Player.class, true, !isIgnoreable)` (:73-74), FOLLOW_RANGE 32 (:84), P1; `HurtByTargetGoal` (:70) | FIX IN FLIGHT (ENT-S-108) | |
| filter order | :464-508: null/self/dead → ignore(:474) → sight(:477) → Hydrolisc(:480) → EnderReaper(:483) → EnderKnight(:486) → Enderman(:489) → Creeper(:492) → TrooperBug(:495) → SpitBug(:498) → player creative false(:501-506) → true(:507) | predicate + vanilla chain (P1) | FIX IN FLIGHT (ENT-S-108) | |
| PlayNicely gate | :511-513 → null | none | FIXED (ENT-S-108; pinned by ENT-S-115, wave 1) — gate at EntityTrooperBug.java:295 — was DIVERGES | missing gate |
| creative gate | `isCreativeMode` :503 | vanilla `invulnerable` (P1) | DIVERGES | missing gate (creative mapping); not in ENT-S-109's list |
| PEACEFUL gate | none (EntityMob :48-49) | none (`Monster` :32) | N/A | P3 |
| allies / species exclusions | Hydrolisc, EnderReaper, EnderKnight, Enderman, Creeper, TrooperBug, SpitBug (:480-500) | none (moot for Player.class) | FIX IN FLIGHT (ENT-S-108) | |
| ignore screen | :474 after dead, before sight | goal predicate ahead of LoS (:73-74) | FIXED (ENT-S-106) | |
| tie-break / selection rule | GenericTargetSorter (:50, :63, sort :515) | vanilla nearest, `<` | DIVERGES | tie-break |
| target set / release | `getAttackTarget()` kept while alive (:414-418); scan result not stored; hurt (:393-399) sets it | goal release per P1; `TrooperBugLeapAttackGoal` (ai/TrooperBugLeapAttackGoal.java:19-43, Params.trooperBug forgetTargetRoll 0, ai/BugMeleeAttackGoal.java:72) pursues until dead | FIXED (ENT-S-129, wave 2)` — the convention — was DIVERGES | release rule |
| other | hurt retaliation :393-399 (`EntityLiving`); the SpitBug summon reads the current target (:441) | :182-186 (`Mob`); :203-204 `getTarget()` | MATCH | |

### UltimateSword — orig `UltimateSword.java`, port `item/UltimateSword.java` + `item/Chainsaw.java` — lane S4
| aspect | 1.7.10 (file:line) | port HEAD (file:line) | status | note |
|---|---|---|---|---|
| scan set | Chainsaw only (:148-151 `this == MyChainsaw`): `findSomethingToHit` (:163-174) — `EntityLivingBase.class` in the player's bb.expand(5,5,5) (:164), every suitable target hit for `chainsaw_stats.damage` (:172), on left-click; the Ultimate Sword / Battle Axe / Queen Battle Axe have no sweep | Chainsaw.java `onLeftClickEntity` :110-116 → `findSomethingToHit` :119-126: `LivingEntity.class`, inflate(5,5,5) (:120-121), all suitable hit (:124); UltimateSword.java has no sweep (:87-97) | MATCH | ITEM-037 |
| filter order | :176-196: null(:177) → == player(:180) → dead(:183) → [pvp off: Player/Girlfriend/Boyfriend false(:188-190), tamed EntityTameable false(:191-193)] → `MyCanSee(e, player)` (:195; :198-247: 10-step block ray from (x, y+1.4, z) to the target's centre) | Chainsaw.java :134-138: == player / dead → `super.onLeftClickEntity` guard (UltimateSword.java:88-95, same four exclusions under `ULTIMATE_SWORD_PVP`) → `player.hasLineOfSight(target)` (eye-to-eye) | MATCH | the ray geometry differs (y+1.4→centre vs eye→eye) but inside a 5-block box it answers the same except at block edges; the port comment :131-132 records the mapping |
| PlayNicely gate | none | none | N/A | |
| creative gate | none | none | N/A | |
| PEACEFUL gate | none | none | N/A | |
| allies / species exclusions | pvp off: Player, Girlfriend, Boyfriend, tamed EntityTameable (:188-193; also the direct-hit guard :138-147) | Player, Girlfriend, Boyfriend, tamed TamableAnimal (UltimateSword.java:88-95) | MATCH | |
| ignore screen | none | none | N/A | |
| tie-break / selection rule | none — every suitable target is hit, no sort | none (:122-125) | MATCH | |
| target set / release | nothing stored | nothing stored | N/A | |
| other | the direct-hit guard `onLeftClickEntity` returns true (cancel) for the protected kinds when pvp is off (:138-147) | UltimateSword.java:87-97 | MATCH | |

### Urchin — orig `Urchin.java`, port `Urchin.java` — lane S4
| aspect | 1.7.10 (file:line) | port HEAD (file:line) | status | note |
|---|---|---|---|---|
| scan set | `EntityLivingBase.class`, bb.expand(16,3,16) (:276), `updateAITasks` :190 behind `nextInt(8)==0` (:195); :272-288 | `getNearestPlayer(this, 16.0)` (:150) when `getTarget()==null` (:148-149), `customServerAiStep` :143 behind `nextInt(8)==0` (:147) | FIX IN FLIGHT (ENT-S-108) | listed in ENT-S-108 |
| filter order | :220-270: null/self/dead → ignore(:230) → sight(:233) → Vortex(:236) → Rotator(:239) → Peacock(:242) → CrystalCow(:245) → Irukandji(:248) → Skate(:251) → Whale(:254) → Flounder(:257) → Urchin(:260) → player creative false(:263-268) → true(:269) | :154 `!isIgnoreable && !instabuild`; no sight | FIX IN FLIGHT (ENT-S-108) | the sight gate (:233) is part of the chain the rebuild restores |
| PlayNicely gate | :273-275 → null | none | FIXED (ENT-S-108; pinned by ENT-S-115, wave 1) — gate at Urchin.java:236 — was DIVERGES | missing gate |
| creative gate | `isCreativeMode` :265 | `instabuild` :154 | MATCH | P6 |
| PEACEFUL gate | none (EntityMob :41-42) | none (`Monster` :33) | N/A | P3 |
| allies / species exclusions | Vortex, Rotator, Peacock, CrystalCow, Irukandji, Skate, Whale, Flounder, Urchin (:236-262) | none (moot for players) | FIX IN FLIGHT (ENT-S-108) | |
| ignore screen | :230 after dead, before sight | :154 inline, ahead of the creative check | FIXED (ENT-S-106) | unreachable until ENT-S-108 |
| tie-break / selection rule | GenericTargetSorter (:43, :55, sort :277) | vanilla nearest player, `<` | DIVERGES | tie-break |
| target set / release | nothing stored (scan each pass; `EntityAIHurtByTarget` :61 unread by combat) | nearest player stored via `setTarget` (:156), kept while alive (:159); `HurtByTargetGoal` (:66) | FIXED (ENT-S-129, wave 2)` — as CaveFisher — was DIVERGES | release rule |
| other | none | none | N/A | |

### Vortex — orig `Vortex.java`, port `EntityVortex.java` — lane S4
| aspect | 1.7.10 (file:line) | port HEAD (file:line) | status | note |
|---|---|---|---|---|
| scan set | `EntityLivingBase.class`, bb.expand(16,10,16) (:345), every tick from `onUpdate` on both sides (:114) and again from `updateAITasks` (:184); :341-357; ctor :48-55 registers NO target tasks | same box (:330-331) through `currentPullTarget()` (:312-322): one scan per 5 ticks shared by `tick()` (:123) and `customServerAiStep` (:226); no target goal | RECORDED (OPT-004) | ruled: ≤5-tick acquisition latency accepted; dead targets dropped immediately (:313-316) |
| filter order | :286-339: null/self/dead → ignore(:296) → sight(:299) → player creative false(:302-307) → Vortex(:308) → Rotator(:311) → Mothra(:314) → Brutalfly(:317) → Peacock(:320) → CrystalCow(:323) → Irukandji(:326) → Skate(:329) → Whale(:332) → Flounder(:335) → Urchin(:338) | :343-357 same order | MATCH | ENT-S-089 / TF-035 |
| PlayNicely gate | :342-344 → null | :329 | MATCH | |
| creative gate | `isCreativeMode` :304 | `instabuild` :347 | MATCH | P6 |
| PEACEFUL gate | none (EntityMob :40-41) | none (`Monster` :28) | N/A | P3 |
| allies / species exclusions | Vortex, Rotator, Mothra, Brutalfly, Peacock, CrystalCow, Irukandji, Skate, Whale, Flounder, Urchin (:308-338) | :348-353 same eleven | MATCH | |
| ignore screen | :296 after dead, before sight | :345 same | MATCH | shared list FIXED (ENT-S-101) |
| tie-break / selection rule | GenericTargetSorter (:43, :54, sort :346) | GenericTargetSorter via firstMatch (:334) | MATCH | P5 |
| target set / release | nothing stored, rescanned every tick | cached 5 ticks, invalidated on death/removal (:313-316) | RECORDED (OPT-004) | |
| other | hurt sets the flight target to the attacker and winds the pull for 20 ticks (:225-234) | :284-292 | MATCH | |

### WaterDragon — orig `WaterDragon.java`, port `WaterDragon.java` — lane S4
| aspect | 1.7.10 (file:line) | port HEAD (file:line) | status | note |
|---|---|---|---|---|
| scan set | `EntityLivingBase.class`, bb.expand(14,4,14) (:689), `updateAITasks` behind `!PEACEFUL && nextInt(5)==1` (:597); skipped under PlayNicely (:683-685) and for babies (:686-688); current target kept if alive (:694-697) else nulled (:698); :682-706 | NO proactive scan: `registerGoals` :99-114 holds `WaterCanonAttackGoal` (consumes `getTarget()`), goal targets come only from `HurtByTargetGoal` (:113) and `hurt` (:266-269); no `findSomethingToAttack` in the file | FIXED (ENT-S-117, wave 2) — `findSomethingToAttack` :388-398, box 14/4/14, the pass :338-340 → `selectTarget` :360-372 → the slot — was DIVERGES | scan-set: the whole proactive hunt is absent; unrecorded (ENT-S-074 restored only the ranged branch) |
| filter order | :650-680: PEACEFUL(:651) → null/self/dead → sight(:663) → WaterDragon false(:666) → EntityMob true(:669) → tamed → false(:672-674) → player → !creative(:675-678) → `isAttackableNonMob`(:679) | none (P2 conditions on the revenge goal only) | FIXED (ENT-S-117, wave 2) — `isSuitableTarget` :412-421 — was DIVERGES | dependent on the scan row |
| PlayNicely gate | :683-685 → null | none | FIXED (ENT-S-117, wave 2) — :390 — was DIVERGES | missing gate (dependent) |
| creative gate | `isCreativeMode` :677 | none beyond the revenge goal's `invulnerable` (P2) | FIXED (ENT-S-117, wave 2) — :419 `instabuild` — was DIVERGES | missing gate (dependent) |
| PEACEFUL gate | :597 + :651 | none of its own; `HurtByTargetGoal` rejects players in PEACEFUL, mobs still targeted; `WaterCanonAttackGoal` (:392-467) has no PEACEFUL test | FIXED (ENT-S-117, wave 2) — :338 caller (difficulty before the roll) + :413 filter — was DIVERGES | missing gate (partial) |
| allies / species exclusions | WaterDragon (:666); a tamed dragon hunts only EntityMob (:672-674); prey fallthrough `isAttackableNonMob` (P9); hurt ignores WaterDragon/AttackSquid/WaterBall attackers (:470-478, :484-489) | hurt ignores WaterDragon/AttackSquid/WaterBall (:256-260); no filter otherwise | FIXED (ENT-S-117, wave 2) — WaterDragon :416, tamed → Monster only :417-418, `isAttackableNonMob` :420 = the port helper (membership: T6; EnderDragon and GodzillaHead are port-only grants there) — was DIVERGES | exclusion list / tamed rule (dependent) |
| ignore screen | none | none | N/A | |
| tie-break / selection rule | GenericTargetSorter (:50, :67, sort :690) | none (no scan) | FIXED (ENT-S-117, wave 2) — GenericTargetSorter :86 via `firstMatch` :397 — was DIVERGES | tie-break (dependent) |
| target set / release | attack target from hurt (:490-492, `EntityLiving` attacker, not AttackSquid/WaterDragon); `setAttackTarget(null)` 1-in-200 every AI tick (:594-596); kept while alive (:694-698) | `hurt` :266-269 (`Mob`); the 1-in-200 release is `Presets.waterDragon` forgetTargetRoll 200 (ai/DinosaurMeleeAttackGoal.java:43, rolled in ai/BugMeleeAttackGoal.java:123-128 while engaged); `HurtByTargetGoal` release per P1 (FOLLOW_RANGE 32, :123) | MATCH — note (ENT-S-117, wave 2): the hunt's own pick lives in the slot under `scanPick` (:98), re-derived per pass; the hurt / revenge target sticky as orig (:395) | the roll only matters while a target exists in both trees |
| other | baby never hunts (:686-688); tamed dragon restricted to EntityMob prey (:672-674) | neither rule exists (no scan) | FIXED (ENT-S-117, wave 2) — baby :391, tamed :418 — was DIVERGES | dependent on the scan row |

### MyEntityAINearestAttackableTarget — orig `MyEntityAINearestAttackableTarget.java`, port replaced by vanilla `NearestAttackableTargetGoal<>(…, Monster.class, true)` (Boyfriend.java:144, Girlfriend.java:208); its subclass `MyEntityAIJealousy` → `ai/JealousyTargetGoal.java` — lane S3
Users in orig: Boyfriend.java:138 (EntityCreeper.class, 20.0f, chance 0, sight, nearbyOnly, `IMob.mobSelector`), :141 (EntityLiving.class, 15.0f, chance 0, sight, nearbyOnly, `IMob.mobSelector`); Girlfriend.java:164, :167 (same pair); all four registered only when `PlayNicely == 0` at construction; MyEntityAIJealousy.java:13-14 extends it (Boyfriend.java:144,147 / Girlfriend.java:170,173). Species-level rows belong to the Boyfriend/Girlfriend lane; this block covers the goal itself.
| aspect | 1.7.10 (file:line) | port HEAD (file:line) | status | note |
|---|---|---|---|---|
| scan set | `selectEntitiesWithinAABB(targetClass, bbox.expand(targetDistance, 4.0, targetDistance), selector)` MyEntityAINearestAttackableTarget.java:56 — `targetDistance` is the per-goal ctor value (:36), not follow range; gates :44-52 (tameable must be tamed; Girlfriend not sitting) and :53 `chance > 0 && nextInt(100) > chance` (percent, 0 = every tick) | VAN-NAT `Monster.class`: box `inflate(FOLLOW_RANGE, 4, FOLLOW_RANGE)` (Boyfriend/Girlfriend set no FOLLOW_RANGE → `Mob.createMobAttributes` default 16), 1-in-10 roll; the dedicated 20-block Creeper goal has no counterpart; JealousyTargetGoal keeps the per-goal distance via `getFollowDistance()` override (`ai/JealousyTargetGoal.java:34-37`) and adds the tame / sitting / owner gates dynamically (:39-45) | DIVERGES | scan set: per-goal 20 (creepers) / 15 (IMob) → 16 for both; every tick → 1-in-10; Creeper priority goal dropped |
| filter order | sorted list, first passing `isSuitableTarget(e,false)` = MyEntityAITarget.java:78-129: null/self/dead; tamed owner: tamed target → false, owner → false (:88-95); **Player → `valentines_day != 0`** (:96-98); EntityPigZombie → false (:99); EntityEnderman → false (:102); **Mothra → true before sight** (:105); sight (:108); EntityCreeper → true (:111); EntityGhast → true (:114); nearbyOnly path check (:117-127); true | VAN-TC (self, spectator/alive, `canAttack`: PEACEFUL-player rule + `!invulnerable`, `canAttackType` no Ghast, allied/owner via `TamableAnimal.canAttack` + `isAlliedTo`, range × visibility, sight) with no selector on the Monster goal; JealousyTargetGoal selector = rival not tamed (`ai/JealousyTargetGoal.java:28-29`) | FIXED (ENT-S-128, wave 2) — PigZombie / Enderman refused, Mothra and Creeper taken in sight (Boyfriend :153-192, Girlfriend :218-261); the Valentine player rule is MOD-036's; DEFERRED with the construction: the Ghast (engine, ENT-S-127), nearbyOnly, and orig :105's Mothra grant ahead of the sight step — was DIVERGES | filter order: PigZombie/Enderman exclusions gone (both are Monsters → now prey); Mothra grant gone (port Mothra is an EntityButterfly, not a Monster); Ghast grant reversed (`canAttackType`); nearbyOnly reachability dropped (`mustReach=false`); the valentine player rule moved to `ValentineTargetGoal` (below) |
| PlayNicely gate | construction-time `PlayNicely == 0` on every registration (Boyfriend.java:137-147, Girlfriend.java:163-173) | none on the Monster goal (Boyfriend.java:144, Girlfriend.java:208); dynamic `PLAY_NICELY.get()` inside JealousyTargetGoal only (`ai/JealousyTargetGoal.java:41`) | FIXED (ENT-S-115, wave 1) — the Monster goal's live canUse; the Creeper tasks have no port goal (scan-set row) — was DIVERGES | missing gate on the Monster goal |
| creative gate | none in the class (creative players would be prey on Valentine's day; players otherwise refused by :96-98) | VAN-TC `invulnerable` — moot for `Monster.class` | N/A | |
| PEACEFUL gate | none (IMob prey despawn) | VAN-TC refuses players only; Monsters despawn | N/A | |
| allies / species exclusions | tamed target, owner (:88-95); PigZombie, Enderman (:99-104) | owner (`TamableAnimal.canAttack`), owner's team (`isAlliedTo`) | FIXED (ENT-S-128, wave 2) — the tamed-target rule transcribed (inert behind the Enemy pre-filter, as behind IMob); the owner via TamableAnimal.canAttack / isAlliedTo — was DIVERGES (folded into filter order) |  |
| ignore screen | none | none | N/A | |
| tie-break / selection rule | `MyEntityAINearestAttackableTargetSorter.java:21-31`: distanceSq halved for creepers (no silhouette term), stable sort → first-encountered wins ties | VAN-NAT nearest by plain distanceSq from eye position, strict `<` | DIVERGES | tie-break: creeper halving dropped |
| target set / release | :68-71 `setAttackTarget` on start; MyEntityAITarget.java:43-66 continue: null → false; dead → clear + false; beyond `targetDistance`² → false; tamed owner vs tamed target → false; sight timeout 60; :74-76 reset → null | VAN-NAT start → `setTarget`; `TargetGoal.canContinueToUse`: null, `!canAttack`, team, beyond FOLLOW_RANGE 16, 60 unseen ticks; stop → null | FIXED (ENT-S-129, wave 2)` — `getFollowDistance() → 15` (closes the T3c box row for these goals) — was DIVERGES | release rule: hold distance 20 / 15 (per goal) → 16; otherwise the same 60-tick sight memory |
| other | Girlfriend sitting gate :50-52; tameable-must-be-tamed :44-49 | JealousyTargetGoal :42-43 (tame, not sitting, has owner) | MATCH | |

### MyEntityAITarget — orig `MyEntityAITarget.java`, port `entity/MyEntityAITarget.java` (present at HEAD but with no subclass or user — the live replacement is vanilla `TargetGoal` + `TargetingConditions`) — lane S3
Abstract base of the two goals above; it scans nothing itself.
| aspect | 1.7.10 (file:line) | port HEAD (file:line) | status | note |
|---|---|---|---|---|
| scan set | none (abstract; subclasses scan) | none; port class unused (grep of HEAD finds only the comment at Girlfriend.java:230) | N/A | |
| filter order | `isSuitableTarget` :78-129 as listed in the block above (Player → `valentines_day != 0` :96-98; PigZombie :99; Enderman :102; Mothra→true :105; sight :108; Creeper→true :111; Ghast→true :114; nearbyOnly :117-127) | dead-code copy `MyEntityAITarget.java:79-106`: Player → **always false** (:87), ZombifiedPiglin (:88), Creeper→true (:89), Ghast→true (:90), sight (:92), nearbyOnly (:96-104) — Enderman exclusion and Mothra grant absent; effective replacement = VAN-TC (no species rules at all) | FIXED (ENT-S-128, wave 2) — for the effective replacement (the goals' predicates); the dead copy `entity/MyEntityAITarget.java` stays with housekeeping — was DIVERGES (effective); PORT-ONLY (dead copy) | the port copy would drift (valentine rule, Enderman, Mothra) if ever wired; nothing wires it |
| PlayNicely gate | none (registration-time in the users) | none | N/A | |
| creative gate | none | none in the copy; VAN-TC `invulnerable` in the replacement | N/A | |
| PEACEFUL gate | none | none | N/A | |
| allies / species exclusions | tamed target / owner when the task owner is tamed (:88-95) | copy :82-85 same; replacement: `TamableAnimal.canAttack` + `isAlliedTo` | MATCH | |
| ignore screen | none | none | N/A | |
| tie-break / selection rule | none (subclass sorters) | none | N/A | |
| target set / release | `continueExecuting` :43-66: null; dead → clear; beyond `targetDistance`²; tamed-vs-tamed; 60-tick sight memory; `resetTask` :74-76 → null | copy :43-65, :74-77 identical; replacement `TargetGoal.canContinueToUse` (v) `TargetGoal.java:41-74` — same shape with FOLLOW_RANGE instead of the per-goal distance and `!canAttack` instead of the tamed-vs-tamed rule | MATCH | |
| other | reachability cache 10+rand(5) ticks, end-node within 1.5 blocks (:131-144) | copy :108-117; replacement `TargetGoal.canReach` (v) :125-140 same numbers | MATCH | |

### MyValentineTarget — orig `MyValentineTarget.java`, port `Girlfriend.ValentineTargetGoal` (Girlfriend.java:226-248, registered :204-205) — lane S3
Users in orig: Girlfriend.java:161 (EntityPlayer.class, 16.0f, chance 0, sight, nearbyOnly) and :162 (Boyfriend.class, same); not PlayNicely-gated.
| aspect | 1.7.10 (file:line) | port HEAD (file:line) | status | note |
|---|---|---|---|---|
| scan set | `selectEntitiesWithinAABB(targetClass, bbox.expand(16, 4.0, 16))` MyValentineTarget.java:60 (`targetDistance` 16, :39); gates :48 `valentines_day != 0`, :51-56 Girlfriend `feelingBetter != 0` → false, :57 percent chance (0 = every tick) | Player goal: VAN-NAT `Player.class` → all players in the level within 16 (`getFollowDistance()` :240-242) × visibility, no box; Boyfriend goal: box `inflate(16, 4, 16)`; `canUse` :244-247 `isValentineAngry()` (Feb 14 && feelingBetter==0, :128-130) && super; randomInterval 0 (:238) → no roll | DIVERGES (Player goal) / MATCH (Boyfriend goal) | scan set: player box 16x4x16 → sphere 16 (×0.8 for a sneaking player) |
| filter order | sorted, first passing MyEntityAITarget.java:78-129: null/self/dead; tamed owner: tamed target / owner → false (:88-95); Player → true on the day (:96-98); (PigZombie/Enderman/Mothra/Creeper/Ghast steps moot for Player/Boyfriend classes); sight (:108); nearbyOnly path check (:117-127) | VAN-TC with selector `candidate != owner && !(tame && candidate is a tamed pet)` (:236-241): self, spectator/alive, selector, `canAttack` (PEACEFUL players refused; `!invulnerable`), allied, range, sight; `mustReach=true` (:238) | MATCH (structure) | see creative / PEACEFUL rows for the two added refusals |
| PlayNicely gate | none | none | MATCH | |
| creative gate | none — a creative player is valid prey on Valentine's day | VAN-TC refuses `abilities.invulnerable` players (creative and spectator) | DIVERGES | port-only gate |
| PEACEFUL gate | none | VAN-TC `canAttack` refuses players in PEACEFUL ((v) `LivingEntity.java:899-901`) | RECORDED (MOD-036, ENT-S-125) — a deliberate parity exception kept in BOTH modes on the owner's safety ruling; pinned — was DIVERGES | port-only gate |
| allies / species exclusions | owner and tamed targets when tamed (:88-95) | selector :239-241 (owner; tamed pets when tame) + `TamableAnimal.canAttack` | MATCH | |
| ignore screen | none | none | N/A | |
| tie-break / selection rule | `MyValentineTargetSorter.java:20-24` plain distanceSq, stable sort | VAN-NAT plain distanceSq from eye position, strict `<` | MATCH | |
| target set / release | :72-75 `setAttackTarget`; MyEntityAITarget.java:43-66 continue (16-block hold, 60-tick sight memory); reset → null | VAN-NAT start; `TargetGoal.canContinueToUse` with `getFollowDistance()` 16 (:240-242), 60 unseen ticks; stop → null | MATCH | |
| other | priority 1 (players) / 2 (Boyfriends) (Girlfriend.java:161-162) | priority 1 / 2 (Girlfriend.java:204-205) | MATCH | |

## 3. Split summary

### 3.1 Every block — status counts per row (10-row schema; the three non-hunters have one N/A row)

| # | block (orig file) | lane | rows | MATCH | DIVERGES | RECORDED | FIXED | FIX IN FLIGHT | PORT-ONLY | N/A | DIVERGES types (the part's own summary) |
|---|---|---|---|---|---|---|---|---|---|---|---|
| 1 | Alien | S1 | 10 | 9 | 0 | 0 | 0 | 0 | 0 | 1 | — |
| 2 | Alosaurus | S1 | 10 | 8 | 1 | 0 | 1 | 0 | 0 | 0 | release rule |
| 3 | AntRobot | S1 | 10 | 4 | 5 | 0 | 1 | 0 | 0 | 0 | scan-set narrowing; filter order; missing gate ×2 (PlayNicely, PEACEFUL); release rule |
| 4 | AttackSquid | S1 | 10 | 1 | 8 | 0 | 0 | 0 | 0 | 1 | scan-set (whole scan missing); filter order; missing gate ×2 (PlayNicely, creative); exclusion list; tie-break; release rule; other (orig-only `wasshot` rule) |
| 5 | BandP | S1 | 10 | 9 | 0 | 0 | 0 | 0 | 0 | 1 | — |
| 6 | Basilisk | S1 | 10 | 9 | 0 | 0 | 1 | 0 | 0 | 0 | — |
| 7 | Bee | S1 | 10 | 9 | 0 | 0 | 0 | 0 | 0 | 1 | — |
| 8 | Brutalfly | S1 | 10 | 5 | 3 | 0 | 1 | 1 | 0 | 0 | scan-set (strafe geometry); filter order (creative shadowing of the mob hunt); tie-break (`<=` last vs `<` first) — the creative gate itself is FIX IN FLIGHT (ENT-S-109) |
| 9 | CaterKiller | S1 | 10 | 2 | 7 | 0 | 0 | 0 | 0 | 1 | scan-set narrowing; filter order; missing gate ×2 (PlayNicely, creative); exclusion list; tie-break; release rule |
| 10 | CaveFisher | S1 | 10 | 2 | 4 | 0 | 1 | 3 | 0 | 0 | missing gate ×2 (PlayNicely, creative); tie-break; release rule — scan set / filter / allies FIX IN FLIGHT (ENT-S-108), ignore FIXED (ENT-S-106) |
| 11 | Cephadrome | S1 | 10 | 4 | 3 | 0 | 1 | 1 | 0 | 1 | missing gate ×2 (PlayNicely; the PEACEFUL cadence guard :488); tie-break / sorter — filter FIX IN FLIGHT (ENT-S-113), creative FIXED (ENT-S-107) |
| 12 | CloudShark | S1 | 10 | 9 | 0 | 0 | 0 | 0 | 0 | 1 | — |
| 13 | Crab | S1 | 10 | 8 | 1 | 0 | 0 | 0 | 0 | 1 | release rule |
| 14 | CreeperRepellent | S1 | 1 | 0 | 0 | 0 | 0 | 0 | 0 | 1 | block, not a hunter |
| 15 | CreepingHorror | S1 | 10 | 8 | 1 | 0 | 0 | 0 | 0 | 1 | release rule |
| 16 | Cryolophosaurus | S2 | 10 | 6 | 2 | 0 | 0 | 1 | 1 | 0 | tie-break; release rule (port-only 1-in-200 drop). +1 PORT-ONLY (melee goal). creative FIX IN FLIGHT (ENT-S-109) |
| 17 | Dragon | S2 | 10 | 6 | 3 | 0 | 0 | 0 | 0 | 1 | scan-set narrowing (vanilla IMob channel missing); missing gate (PlayNicely); release rule (that channel's stored target) |
| 18 | Dragonfly | S2 | 10 | 2 | 7 | 0 | 0 | 0 | 0 | 1 | scan-set cadence; filter order; missing gate (PlayNicely); missing gate (PEACEFUL); exclusion list (width rule, horses lost); tie-break; release rule (prey stored, never dropped) |
| 19 | DungeonBeast | S2 | 10 | 1 | 5 | 0 | 1 | 2 | 1 | 0 | filter order; missing gate (PlayNicely); creative (`invulnerable` at the vanilla goal); tie-break; release rule (goal-held). +1 PORT-ONLY (vanilla PEACEFUL refusal, inert). scan set + allies FIX IN FLIGHT (ENT-S-108); ignore FIXED (ENT-S-106) |
| 20 | EmperorScorpion | S2 | 10 | 1 | 5 | 0 | 1 | 2 | 1 | 0 | filter order; missing gate (PlayNicely); creative; tie-break; release rule (forget cadence ×4, goal-held). +1 PORT-ONLY. scan set + allies FIX IN FLIGHT (ENT-S-108) |
| 21 | EnderKnight | S2 | 10 | 2 | 7 | 0 | 0 | 0 | 1 | 0 | scan-set (cadence / candidate set); filter order (stare + pumpkin gate absent); missing gate (PlayNicely); creative (shadowing lost, `invulnerable`); tie-break (nearest-then-test vs test-then-nearest); release rule (daylight drop missing); other (stare teleports). +1 PORT-ONLY |
| 22 | EnderReaper | S2 | 10 | 3 | 6 | 0 | 0 | 0 | 1 | 0 | scan-set cadence; missing gate (PlayNicely); creative (shadowing); tie-break; other (stare teleports); release rule (refuter correction — the range / unseen-ticks release is port-only). +1 PORT-ONLY |
| 23 | EntityButterfly | S2 | 10 | 0 | 1 | 0 | 0 | 0 | 0 | 9 | scan-set removed (Islands vampire-type hunt); remaining rows N/A |
| 24 | EntityCannonFodder | S2 | 10 | 9 | 1 | 0 | 0 | 0 | 0 | 0 | release rule (attack target cleared instead of revenge target) |
| 25 | Fairy | S2 | 10 | 6 | 3 | 0 | 0 | 0 | 0 | 1 | filter order (sight missing); missing gate (PlayNicely); tie-break |
| 26 | Frog | S2 | 10 | 7 | 2 | 0 | 0 | 0 | 0 | 1 | missing gate (PlayNicely); tie-break |
| 27 | GammaMetroid | S2 | 10 | 4 | 3 | 0 | 1 | 1 | 1 | 0 | missing gate (PlayNicely); missing gate (PEACEFUL — bites, TamableAnimal); tie-break. +1 PORT-ONLY (owner / tame target goals). creative FIX IN FLIGHT (ENT-S-109); ignore FIXED |
| 28 | GiantRobot | S2 | 10 | 6 | 3 | 0 | 1 | 0 | 0 | 0 | filter order (sight missing); missing gate (PlayNicely); release rule (1-in-100 ordering) |
| 29 | Godzilla | S2 | 10 | 7 | 1 | 0 | 1 | 0 | 1 | 0 | gate semantics (PlayNicely clears the stored target). +1 PORT-ONLY (peer exclusions: Mothra, bosses, royals — unrecorded). ignore FIXED |
| 30 | Hammerhead | S2 | 10 | 3 | 7 | 0 | 0 | 0 | 0 | 0 | scan-set narrowing (players-only sphere; not on ENT-S-108); filter order; missing gate (PlayNicely); exclusion list (shared `isAttackableNonMob` also differs); tie-break; release rule (out-of-sight skip); other (attack dice) |
| 31 | HerculesBeetle | S2 | 10 | 1 | 5 | 0 | 1 | 2 | 1 | 0 | filter order; missing gate (PlayNicely); creative; tie-break; release rule (goal-held). +1 PORT-ONLY. scan set + allies FIX IN FLIGHT (ENT-S-108) |
| 32 | Irukandji | S2 | 10 | 4 | 6 | 0 | 0 | 0 | 0 | 0 | scan-set geometry; filter order (sight missing); missing gate (PlayNicely); tie-break; release rule (dead scan target sticks); other (attack dice) |
| 33 | Kraken | S2 | 10 | 3 | 0 | 0 | 7 | 0 | 0 | 0 | all rows FIXED (ENT-S-100 / 105) or MATCH; KT-C RECORDED |
| 34 | KrakenRepellent | S2 | 1 | 0 | 0 | 0 | 0 | 0 | 0 | 1 | N/A — block, not a hunter |
| 35 | Kyuubi | S2 | 10 | 5 | 3 | 0 | 1 | 1 | 0 | 0 | missing gate (PlayNicely); tie-break; release rule (attack vs revenge target). creative FIX IN FLIGHT (ENT-S-109); ignore FIXED |
| 36 | LeafMonster | S3 | 10 | 6 | 1 | 0 | 0 | 1 | 0 | 2 | release rule (revenge-memory clear → attack-target clear; inert). Non-DIVERGES flags: creative FIX IN FLIGHT (ENT-S-109) |
| 37 | Leon | S3 | 10 | 2 | 3 | 0 | 2 | 2 | 1 | 0 | scan-set widening (vanilla goal 16→40, every tick→1-in-10, IMob→Monster); missing gate (PlayNicely on the goal registration :92); tie-break (plain distance). Non-DIVERGES flags: filter / untamed rule / PlayNicely FIX IN FLIGHT (ENT-S-110); creative FIXED (ENT-S-107); ignore FIXED (ENT-S-106); PORT-ONLY owner-hurt goals + goal predicate |
| 38 | Lizard | S3 | 10 | 3 | 5 | 0 | 0 | 0 | 0 | 2 | filter order (sight step missing); missing gate (PlayNicely); exclusion/prey list (AttackSquid dropped); tie-break; other (buddy-adoption side effect dropped) |
| 39 | LurkingTerror | S3 | 10 | 6 | 0 | 0 | 0 | 1 | 0 | 3 | —. Non-DIVERGES flags: creative FIX IN FLIGHT (ENT-S-109) |
| 40 | Mantis | S3 | 10 | 6 | 1 | 0 | 0 | 0 | 1 | 2 | exclusion/grant list (shared `isAttackableNonMob` membership). Non-DIVERGES flags: PORT-ONLY inert target goals |
| 41 | Molenoid | S3 | 10 | 7 | 1 | 0 | 0 | 0 | 0 | 2 | exclusion/grant list (shared `isAttackableNonMob` membership) |
| 42 | Mothra | S3 | 10 | 8 | 2 | 0 | 0 | 0 | 0 | 0 | scan-set shape (player box → sphere); filter order (player-stage sight missing, creative fall-through missing). Non-DIVERGES flags: stage-2 sweep RECORDED (MOD-029) in modern mode |
| 43 | Nastysaurus | S3 | 10 | 1 | 4 | 0 | 1 | 3 | 0 | 1 | missing gate (PlayNicely); missing gate (creative via `invulnerable`); tie-break (plain nearest); release rule (60/300-tick memories vs per-tick `rt`). Non-DIVERGES flags: scan / filter / allies FIX IN FLIGHT (ENT-S-108); ignore FIXED (ENT-S-106) |
| 44 | Peacock | S3 | 10 | 7 | 1 | 0 | 0 | 0 | 0 | 2 | release rule (orig inert Termite target goal dropped) |
| 45 | PitchBlack | S3 | 10 | 4 | 3 | 0 | 1 | 1 | 0 | 1 | scan-set (heal-branch call site that resets activity dropped); filter order (sight step missing); missing gate (PlayNicely). Non-DIVERGES flags: allies FIX IN FLIGHT (ENT-S-112); ignore FIXED (ENT-S-106) |
| 46 | Pointysaurus | S3 | 10 | 3 | 4 | 0 | 1 | 0 | 1 | 1 | scan-set widening (12x5x12 → sphere 24); missing gate (PlayNicely); missing gate (creative via `invulnerable` on the goal path); release rule. Non-DIVERGES flags: PORT-ONLY stare goal (no MOD record); ignore FIXED (ENT-S-106) |
| 47 | PurplePower | S3 | 10 | 4 | 5 | 0 | 1 | 0 | 0 | 0 | filter order (sight, tamed-pet, royalty steps missing); missing gate (PlayNicely); missing gate (PEACEFUL, incl. self-discard); exclusion list (royalty / tamed pets); tie-break. Non-DIVERGES flags: ignore FIXED (ENT-S-106) |
| 48 | Rat | S3 | 10 | 2 | 5 | 0 | 1 | 1 | 0 | 1 | filter order (5 species steps missing); missing gate (PlayNicely); exclusion list (Irukandji, Skate, Whale, Flounder, DungeonBeast); tie-break; release rule (revenge → target clear; inert). Non-DIVERGES flags: creative FIX IN FLIGHT (ENT-S-109); ignore FIXED (ENT-S-106) |
| 49 | Robot1 | S3 | 10 | 5 | 3 | 0 | 1 | 0 | 0 | 1 | filter order (sight); missing gate (PlayNicely); tie-break. Non-DIVERGES flags: ignore FIXED (ENT-S-106) |
| 50 | Robot2 | S3 | 10 | 6 | 2 | 0 | 1 | 0 | 0 | 1 | filter order (sight); release/target-set rule (port-only same-kind alert + same-kind damage exemption). Non-DIVERGES flags: ignore FIXED (ENT-S-106) |
| 51 | Robot3 | S3 | 10 | 5 | 2 | 0 | 1 | 0 | 0 | 2 | filter order (sight); missing gate (PlayNicely). Non-DIVERGES flags: ignore FIXED (ENT-S-106) |
| 52 | Robot4 | S3 | 10 | 7 | 1 | 0 | 1 | 0 | 0 | 1 | filter order (sight). Non-DIVERGES flags: ignore FIXED (ENT-S-106) |
| 53 | Robot5 | S3 | 10 | 5 | 2 | 0 | 1 | 0 | 0 | 2 | filter order (sight); missing gate (PlayNicely). Non-DIVERGES flags: ignore FIXED (ENT-S-106) |
| 54 | Rotator | S3 | 10 | 9 | 0 | 0 | 0 | 0 | 0 | 1 | — |
| 55 | RubberDucky | S4 | 10 | 9 | 0 | 0 | 0 | 0 | 0 | 1 | — |
| 56 | Scorpion | S4 | 10 | 8 | 1 | 0 | 0 | 0 | 0 | 1 | release rule |
| 57 | SeaMonster | S4 | 10 | 3 | 5 | 0 | 0 | 0 | 0 | 2 | scan-set narrowing; missing gate (sight); missing gate (PlayNicely); tie-break; release rule |
| 58 | SeaViper | S4 | 10 | 2 | 6 | 0 | 0 | 0 | 0 | 2 | scan-set narrowing; filter order; missing gate (PlayNicely); missing gate (creative mapping); tie-break; release rule |
| 59 | Skate | S4 | 10 | 4 | 4 | 0 | 0 | 0 | 0 | 2 | scan-set (geometry); missing gate (sight); missing gate (PlayNicely); release rule |
| 60 | SpiderDriver | S4 | 10 | 6 | 3 | 0 | 1 | 0 | 0 | 0 | missing gate (sight); missing gate (PlayNicely, both scans); tie-break |
| 61 | SpiderRobot | S4 | 10 | 10 | 0 | 0 | 0 | 0 | 0 | 0 | — |
| 62 | SpitBug | S4 | 10 | 1 | 4 | 0 | 1 | 3 | 0 | 1 | missing gate (PlayNicely); missing gate (creative mapping); tie-break; release rule |
| 63 | Spyro | S4 | 10 | 5 | 2 | 0 | 0 | 0 | 1 | 2 | exclusion list / prey set (Mothra + feet ray); missing gate (PlayNicely) |
| 64 | StinkBug | S4 | 1 | 0 | 0 | 0 | 0 | 0 | 0 | 1 | no targeting |
| 65 | Stinky | S4 | 10 | 3 | 4 | 0 | 0 | 0 | 1 | 2 | scan-set (state gate: flying only); exclusion list / prey set (Mothra + feet ray); missing gate (PlayNicely); tie-break |
| 66 | TerribleTerror | S4 | 10 | 3 | 3 | 0 | 0 | 1 | 0 | 3 | exclusion list (9 species); missing gate (PlayNicely); tie-break |
| 67 | TheKing | S4 | 10 | 6 | 1 | 2 | 0 | 0 | 0 | 1 | exclusion list / prey set (shared `isAttackableNonMob`, P9) |
| 68 | ThePrince | S4 | 10 | 6 | 1 | 0 | 0 | 0 | 1 | 2 | missing gate (feet ray) |
| 69 | ThePrinceAdult | S4 | 10 | 6 | 2 | 0 | 0 | 0 | 0 | 2 | scan-set (vanilla goal 64 vs 16); missing gate (PlayNicely, goal half) |
| 70 | ThePrincess | S4 | 10 | 6 | 1 | 0 | 0 | 0 | 1 | 2 | missing gate (feet ray) |
| 71 | ThePrinceTeen | S4 | 10 | 5 | 3 | 0 | 0 | 0 | 0 | 2 | scan-set (vanilla goal 32 vs 16); missing gate (PlayNicely, goal half); tie-break |
| 72 | TheQueen | S4 | 10 | 6 | 1 | 2 | 0 | 0 | 0 | 1 | exclusion list / prey set (P9) |
| 73 | TRex | S4 | 10 | 1 | 4 | 0 | 1 | 3 | 0 | 1 | missing gate (PlayNicely); missing gate (creative mapping); tie-break; release rule |
| 74 | Triffid | S4 | 10 | 4 | 3 | 0 | 1 | 1 | 0 | 1 | exclusion list (blanket Monster; Dragon hunted); missing gate (PlayNicely); tie-break |
| 75 | TrooperBug | S4 | 10 | 1 | 4 | 0 | 1 | 3 | 0 | 1 | missing gate (PlayNicely); missing gate (creative mapping); tie-break; release rule |
| 76 | UltimateSword | S4 | 10 | 5 | 0 | 0 | 0 | 0 | 0 | 5 | — |
| 77 | Urchin | S4 | 10 | 1 | 3 | 0 | 1 | 3 | 0 | 2 | missing gate (PlayNicely); tie-break; release rule |
| 78 | Vortex | S4 | 10 | 7 | 0 | 2 | 0 | 0 | 0 | 1 | — |
| 79 | WaterDragon | S4 | 10 | 1 | 8 | 0 | 0 | 0 | 0 | 1 | scan-set (proactive hunt absent) + 7 dependent rows: filter order, missing gate (PlayNicely), missing gate (creative), missing gate (PEACEFUL, partial), exclusion list / tamed rule, tie-break, other (baby + tamed rules) |
| 80 | MyEntityAINearestAttackableTarget | S3 | 10 | 1 | 6 | 0 | 0 | 0 | 0 | 3 | scan-set (per-goal 20/15 → 16, every tick → 1-in-10, Creeper goal dropped); filter order (PigZombie/Enderman/Mothra/Ghast/nearbyOnly rules); missing gate (PlayNicely on the Monster goal); tie-break (creeper halving dropped); release rule (hold distance) |
| 81 | MyEntityAITarget | S3 | 10 | 3 | 1 | 0 | 0 | 0 | 0 | 6 | filter order (effective replacement VAN-TC carries none of the species rules). Non-DIVERGES flags: PORT-ONLY dead-code copy |
| 82 | MyValentineTarget | S3 | 10 | 6 | 3 | 0 | 0 | 0 | 0 | 1 | scan-set shape (player box → sphere 16); missing gate reversed (port-only creative refusal); missing gate reversed (port-only PEACEFUL refusal) |
| | **total** | | **793** | **378** | **221** | **6** | **38** | **37** | **15** | **98** | |

### 3.2 Totals

- **Blocks:** 82 = 79 species blocks (76 hunters on the 10-row schema + 3 non-hunters: CreeperRepellent, KrakenRepellent, StinkBug) + 3 AI helper blocks (10 rows each). **Rows:** 793.
- **By status:** MATCH 378 · DIVERGES 221 rows (220 aspects: the parts' own 219 plus the refuter's EnderReaper release correction — the helper's folded allies row is the one extra) · RECORDED 6 · FIXED 38 · FIX IN FLIGHT 37 · PORT-ONLY 15 · N/A 98. Compound rows (§3.5) are counted once, under DIVERGES.
- **By lane:** S1 (15 blocks, 141 rows): MATCH 87 · DIVERGES 33 · FIXED 6 · FIX IN FLIGHT 5 · N/A 10. S2 (20 blocks, 191 rows): MATCH 76 · DIVERGES 70 · FIXED 14 · FIX IN FLIGHT 9 · PORT-ONLY 8 · N/A 14. S3 (22 blocks, 220 rows): MATCH 106 · DIVERGES 55 · FIXED 12 · FIX IN FLIGHT 9 · PORT-ONLY 3 · N/A 35. S4 (25 blocks, 241 rows): MATCH 109 · DIVERGES 63 · RECORDED 6 · FIXED 6 · FIX IN FLIGHT 14 · PORT-ONLY 4 · N/A 39.
- **DIVERGES by the parts' own type taxonomy** (they differ — S4 counts a missing sight step as "missing gate", S3 as "filter order"; §4 is the reconciled grouping): S1 33 = scan-set 4, missing gate 10, filter order 4, exclusion list 2, tie-break 5, release rule 7, other 1. S2 70 (the part's 69 + the refuter's EnderReaper release row) = missing/changed gate 22 (PlayNicely 14 + Godzilla semantics 1, PEACEFUL 2, creative 5), scan-set 7, filter order 9, exclusion list 2, tie-break 13, release rule 13, other 4. S3 54 = scan-set 6, missing/added gate 16, filter order 12, exclusion/prey list 5, tie-break 7, release/target-set rule 7, other 1. S4 63 = scan-set 7, missing gate 27 (PlayNicely 15, sight/feet-ray 5, creative mapping 5, PEACEFUL 1, WaterDragon tamed/baby rules 1), filter order 2, exclusion list / prey set 7, tie-break 12, release rule 8.
- **By proposed batch (§4):** T1 44 · T2 18 · T3a 19 · T3b 22 · T3c 4 · T4 35 · T5 33 · T6 18 · T7 6 · T8 17 · T9 15 · T10 5 = **236** = 221 DIVERGES rows + 15 PORT-ONLY rows, each listed exactly once. Two observation-sourced entries (Crab in T6, IrukandjiArrow in T10) sit outside that count.
- **Hunters with no DIVERGES row:** 12 of 76 (§3.3). **Hunters with exactly one:** 16 (Alosaurus, Crab, CreepingHorror, EntityButterfly, EntityCannonFodder, Godzilla, LeafMonster, Mantis, Molenoid, Peacock, Robot4, Scorpion, TheKing, TheQueen, ThePrince, ThePrincess — of which Godzilla, Mantis, ThePrince and ThePrincess also carry a PORT-ONLY row).

### 3.3 Fully matching hunters (no DIVERGES row, no PORT-ONLY row)

| hunter | lane | rows that are not plain MATCH |
|---|---|---|
| Alien | S1 | ignore screen N/A |
| BandP | S1 | ignore screen N/A |
| Basilisk | S1 | ignore screen FIXED (ENT-S-101) |
| Bee | S1 | ignore screen N/A |
| CloudShark | S1 | ignore screen N/A |
| Kraken | S2 | six rows FIXED (ENT-S-100), tie-break FIXED (ENT-S-105); KT-C rand stream RECORDED |
| LurkingTerror | S3 | creative gate FIX IN FLIGHT (ENT-S-109); PEACEFUL / ignore / other N/A |
| Rotator | S3 | PEACEFUL N/A |
| RubberDucky | S4 | ignore screen N/A (restored under ENT-K-082) |
| SpiderRobot | S4 | none — the only block with ten MATCH rows (ENT-S-021) |
| UltimateSword (Chainsaw sweep) | S4 | five N/A rows (an item, ITEM-037) |
| Vortex | S4 | scan set and release RECORDED (OPT-004) |

Non-hunters, N/A throughout: CreeperRepellent (S1, `block/RepellentBlock`), KrakenRepellent (S2, same block), StinkBug (S4, no target selection in either tree).

### 3.4 RECORDED items — what is already intentional

Rows whose status is RECORDED (6):

| block | aspect | record | what it covers |
|---|---|---|---|
| TheKing | tie-break / selection rule | OPT-016 / OPT-026 | sort-free `firstMatch` and the containment `head_found` pass — order-preserving, ruled neutral |
| TheKing | other | MOD-002 | small Monster attackers deleted and ignored (`:991-994`) — the recorded original bug; `isBigBoss` damage/10 + `large_unknown` match |
| TheQueen | tie-break / selection rule | OPT-016 | as the King |
| TheQueen | other | MOD-001 / MOD-002 | the health-tracked victim removed at 0 HP (`finishTrackedVictim` :780-794); small Monster attackers deleted |
| Vortex | scan set | OPT-004 | one 16x10x16 living scan per 5 ticks shared by `tick()` and `customServerAiStep` (≤ 5-tick acquisition latency accepted) |
| Vortex | target set / release | OPT-004 | the 5-tick cache, invalidated on death / removal |

Records that make a MATCH or compound row intentional without being its status: Kraken KT-C (ENT-S-100 — orig `world.rand` versus port entity-rand on every roll site, the ENT-S-093 convention; noted in every scan-set row, never counted); MOD-029 (Mothra's modern-mode 6x3 root box widens both sweeps — stage 2 of Mothra's scan-set row, "RECORDED (MOD-029) / MATCH classic"); MOD-022 (TheKing / TheQueen `rt` not persisted — their release rows MATCH); BOSS-017 (PlayNicely consumed by King / Queen / Princess — their PlayNicely rows MATCH; the Godzilla mapping it produced is DIVERGES, T1); BOSS-024 (ThePrince prey ladder restored); OPT-021 (every `TargetSelection.firstMatch` site, neutral).

Named by the parts as "accepted" or "mentioned" but carrying **no MOD record** — so they stay DIVERGES / PORT-ONLY and are batched: the Alosaurus `setTarget` hand-off (ENT-A-009's resolution, `AUDIT_FINDINGS.md:171`, calls it "the accepted mapping"; T5), the Cryolophosaurus `DinosaurMeleeAttackGoal` (same record; T9), the Godzilla PlayNicely stored-clear (BOSS-017's mapping; T1), the Godzilla peer exclusions (a code comment only, `Godzilla.java:594-596`; T9), the Pointysaurus stare goal (`AUDIT_FINDINGS.md:7031` mentions it only for its creative token; T9), the ENT-A-017..022 AttackSquid records (cover HP / swing / watercanon / drops / spawn / hurt, not the scan; T3a), ENT-S-074 (restored the WaterDragon's ranged branch only; T3a), ENT-D-020 (claims the EnderReaper stare teleports "all ported" — not at HEAD; T10).

### 3.5 Compound rows (counted once, under DIVERGES; batched by the DIVERGES half)

| block | aspect | status as written | DIVERGES half → batch | other half |
|---|---|---|---|---|
| Cephadrome | PEACEFUL gate | FIX IN FLIGHT (ENT-S-113) / DIVERGES | the `:488` hunt-roll guard → T7 | the `:516` filter head is ENT-S-113 |
| Leon | scan set | custom MATCH; vanilla goal DIVERGES | the vanilla goal's box / cadence / class → T3c | the custom 20x20x20 scan matches |
| Leon | PlayNicely gate | FIX IN FLIGHT (ENT-S-110); goal-registration gate DIVERGES | the `:92-94` construction-time gate on the IMob goal → T1 | the `:391` filter gate is ENT-S-110 |
| Leon | tie-break / selection rule | custom DIVERGES; goal MATCH | the custom scan's plain-distance comparator → T4 | the vanilla goal's nearest pick matches orig's plain sorter |
| Mothra | scan set | DIVERGES (stage 1); stage 2 RECORDED (MOD-029) / MATCH classic | the stage-1 player box → sphere → T3b | stage 2 matches in classic; modern widening recorded |
| Mothra | filter order | DIVERGES (stage 1); stage 2 MATCH | the stage-1 sight step and creative fall-through → T8 (sight cross-referenced from T2) | stage 2's ladder matches |
| Pointysaurus | scan set | DIVERGES + PORT-ONLY | the box → sphere-24 widening → T3c | the stare goal → T9 |
| MyEntityAITarget | filter order | DIVERGES (effective); PORT-ONLY (dead copy) | the effective replacement (VAN-TC, no species rules) → T6 | the unused port copy `entity/MyEntityAITarget.java` → T9 |
| MyValentineTarget | scan set | DIVERGES (Player goal) / MATCH (Boyfriend goal) | the player box → sphere → T3b | the Boyfriend goal's 16x4x16 box matches |

And the one folded row: MyEntityAINearestAttackableTarget "allies / species exclusions" — "DIVERGES (folded into filter order)": counted as a row here, not as a separate aspect by its part; listed under the helper's filter-order entry in T6.

### 3.6 Observations folded in from the fix lanes (where each landed)

| # | source | observation | in the ledger |
|---|---|---|---|
| 1 | lane L (ENT-S-109 reading) | EntityGammaMetroid lacks orig GammaMetroid.java:254's PEACEFUL guard in `isSuitableTarget` and orig :241's `difficulty != PEACEFUL` gate on the 1-in-5 hunt roll (port :213 / :112) — the ENT-S-113 shape | captured — GammaMetroid PEACEFUL row DIVERGES (S2); T7 |
| 2 | lane L | The Brutalfly strafe keeps a creative `target` non-null (orig :224-226 nulls it so the 1-in-3 mob hunt at :228 can run) and scans a 30-block sphere (`getNearestPlayer(this, 30.0)`, port :204) where orig :215 used a 30 x 20 x 30 box | captured — Brutalfly filter-order row (S1) → T8; scan-set row → T3b |
| 3 | lane L | Cephadrome: orig :488 gates the whole 1-in-7 hunt roll on `!= PEACEFUL`, so on Peaceful a revenge target is not attacked either; the port's `customServerAiStep` (:360 at HEAD, :361 after the lane's import) lacks it — ENT-S-113 restores the filter guard only | captured — Cephadrome PEACEFUL row, the DIVERGES half (S1); T7 |
| 4 | lane M O3 | `EntityLeon.registerGoals` (:158) adds its `NearestAttackableTargetGoal<Monster>` unconditionally; orig Leon.java:92-94 registered the target task only `if (OreSpawnMain.PlayNicely == 0)`; the ENT-S-110 gate covers the private filter only | captured — Leon PlayNicely row, the DIVERGES half (S3); T1 |
| 5 | lane M O4 | Port `PitchBlack.isSuitableTarget` has no line-of-sight step; orig :501-503 refused what `getEntitySenses().canSee` could not see, between the ignore screen and the self-kind check | captured — PitchBlack filter-order row DIVERGES (S3); T2 |
| 6 | lane M O5 | `IrukandjiArrow.onHitEntity` still increments a player's arrow count (:88-90) and can send the ARROW_HIT_PLAYER ding (:100-103) where orig :181's `EntityLiving` gate covered the push, the count (:185) and the ding (:190-192, dead code in 1.7.10); ENT-S-111 gated the push only | **not in any part** (a projectile, outside the 10-row schema) — added as an observation-sourced entry in T10 |
| 7 | lane M O1 (also lane J; S2 / S3 / S4 headers) | Port `MyUtils.isAttackableNonMob` (util :54-63) is not orig MyUtils.java:77-115; read by Crab :366, EntityMantis :279, EntityMolenoid :289, TheKing :1187, TheQueen :1386; no MOD record | captured for Mantis, Molenoid, TheKing, TheQueen (allies rows DIVERGES) and as the shared-helper point V4; **the S1 Crab allies row reads MATCH** ("whitelist + `isAttackableNonMob` — same"), judged on the call site, not the helper's membership — added as an observation-sourced entry in T6, the row left as written |
| 8 | lane L | EntityKyuubi: orig :192 excludes EntityPigZombie explicitly; the port relies on `instanceof Monster` (ZombifiedPiglin is a Monster in 1.21.1) — behaviour-identical | captured — Kyuubi filter-order row MATCH (S2); no action |
| 9 | lane J (ENT-S-106 scope note) | Eight goal-shaped hunters and the Urchin scan players only, so the restored ignore screen "cannot bite until those scans are widened"; the CaveFisher's Animal goal is the one goal site where it bites today | captured — the ENT-S-108 FIX IN FLIGHT rows; the not-on-108 siblings (CaterKiller, Hammerhead, SeaMonster, SeaViper) in T3b |
| 10 | lane J (ENT-S-107 scope note) | The `getAbilities().invulnerable` idiom at ten further sites | captured — ENT-S-109 FIX IN FLIGHT rows; the vanilla-goal remainder in T8 |
| 11 | lane M O2 | `IgnoreScreenParityTests` row 15 (`s106_15_leon_403`) uses the pig control that orig :427 rejects for an untamed Leon — it goes red with ENT-S-110 until its control is a Zombie | not a targeting row; noted in §4.12 for the ENT-S-110 gate |

## 4. Proposed batches for ruling

**How the batches were cut.** Every DIVERGES row (221) and every PORT-ONLY row (15) appears in exactly one batch below, grouped by divergence type as the ruling proposed, with three adjustments the data forced: (1) **T3 is split** into T3a "the whole proactive hunt is missing" (AttackSquid, WaterDragon, EntityButterfly, the Dragon's vanilla IMob channel — their dependent filter / gate / sorter / release rows ride with the root, because the port has no scan or filter to patch), T3b "narrowing, geometry, cadence and state gates" and T3c "widening on the port's vanilla goals"; (2) the four players-only hunters **not** on ENT-S-108 (CaterKiller, Hammerhead, SeaMonster, SeaViper) keep their filter-order and allies rows inside T3b, since only a rebuilt scan can carry them; (3) the three S2 filter-order rows on ENT-S-108 hunters (DungeonBeast, EmperorScorpion, HerculesBeetle — "exclusions absent, moot while players-only") sit at the end of T6 as residuals expected to close with ENT-S-108. Rows whose fix would collapse into another batch's rebuild carry a "rides with" note; they are still counted where they stand so the owner sees the full split.

**Test approach (all batches).** Per the ENT-S-108..113 ruling: generated tests per site where the pattern allows, each batch in a gametest batch of its own (TEST-003 — nothing on the default batch), synchronous and reflection-driven where the parts' precedents are (`IgnoreScreenParityTests` generator over sites, `CreativeMappingParityTests` triple, `LeonTargetingTests` flag-off / flag-on with the flag restored in a finally, `CephadromeGateTests` difficulty flip, `PitchBlackAllyTests` species refused + pig and Zombie controls, `KrakenTargetingParityTests` forced rolls through `findSomethingToAttack`), tick-driven (`KrakenHoldReleaseTests` shape) only where a release rule needs ticks. Proof rule for targeting logic (transcription): **one refuter per finding; two where a lane restores a scan loop** (the ENT-S-108 precedent, addendum items 9 and 17).

**Effort scale.** S = one-line sites with a generator; M = a method rewritten or ten-plus one-line sites; L = a scan loop or goal rebuilt from orig.

### T1 — Missing PlayNicely gates (44 rows, 44 blocks: 43 hunters + the Boyfriend/Girlfriend goal)

| species | aspect | orig cite | port cite | one-line description |
|---|---|---|---|---|
| AntRobot | PlayNicely gate | AntRobot.java:940-942 (stomp), :1012-1014 (hunt) — return before the scan | AntRobot.java:658-695 — neither scan gated | missing gate, two sites |
| CaterKiller | PlayNicely gate | CaterKiller.java:560-562 | EntityCaterKiller.java:99-100 (target goals; PLAY_NICELY only at :86 size and :258 tree-eat) | missing gate on the goal path |
| CaveFisher | PlayNicely gate | CaveFisher.java:231-233 | CaveFisher.java:81-87 (goals; no PLAY_NICELY in the file) | missing gate; outside ENT-S-108's text |
| Cephadrome | PlayNicely gate | Cephadrome.java:576-578 | Cephadrome.java:409-415 (`findSomethingToAttack`; PLAY_NICELY only feeds `wasfed`, :175-177) | missing gate |
| Dragon | PlayNicely gate | Dragon.java:577 (scan; :115 also gates the goal registration — T3a) | Dragon.java:934-940 | missing gate on `findSomethingToAttack` |
| Dragonfly | PlayNicely gate | Dragonfly.java:232 | entity/ai/DragonflyHuntGoal.java:69-84 (`AmbientFlightGoal.canUse` :88-90 gates passenger/water only) | missing gate |
| DungeonBeast | PlayNicely gate | DungeonBeast.java:250 | DungeonBeast.java:59-71 (goals) | missing gate |
| EmperorScorpion | PlayNicely gate | EmperorScorpion.java:504 | EntityEmperorScorpion.java:65-82, :171-192 | missing gate |
| EnderKnight | PlayNicely gate | EnderKnight.java:62 | EnderKnight.java:40-48 (goals) | missing gate |
| EnderReaper | PlayNicely gate | EnderReaper.java:62 | EnderReaper.java:41-52 (goals) | missing gate |
| Fairy | PlayNicely gate | Fairy.java:239 | Fairy.java:151-157 | missing gate |
| Frog | PlayNicely gate | Frog.java:308 | Frog.java:256-263 | missing gate |
| GammaMetroid | PlayNicely gate | GammaMetroid.java:291 | EntityGammaMetroid.java:198-206 | missing gate |
| GiantRobot | PlayNicely gate | GiantRobot.java:343 | GiantRobot.java:223-230 | missing gate |
| Godzilla | PlayNicely gate (semantics) | Godzilla.java:357-359 nulls the LOCAL `e` each pass (the :524-527 scan gate matches port :608-611) | Godzilla.java:732-734 `setTarget(null)` each pass — clears the STORED target | BOSS-017 mapped the local null to a stored clear; visible only with PlayNicely on (the HurtBy target is dropped instead of merely skipped) |
| Hammerhead | PlayNicely gate | Hammerhead.java:194-196 (nulls `rt` for the pass), :252 (scan) | Hammerhead.java:112-150 — none | missing gate, two sites |
| HerculesBeetle | PlayNicely gate | HerculesBeetle.java:417 | EntityHerculesBeetle.java:50-62, :150-157 | missing gate |
| Irukandji | PlayNicely gate | Irukandji.java:291 | Irukandji.java:105-155 — none | missing gate |
| Kyuubi | PlayNicely gate | Kyuubi.java:205 | EntityKyuubi.java:131-137 | missing gate |
| Leon | PlayNicely gate (goal registration) | Leon.java:92-94 — target task registered only when `PlayNicely == 0` | EntityLeon.java:158 — `NearestAttackableTargetGoal<Monster>` always registered | the :391 filter gate is ENT-S-110 (in flight); the construction-time gate on the IMob goal is a separate missing gate (lane M O3) |
| Lizard | PlayNicely gate | Lizard.java:336-338 | Lizard.java:141-152 | missing gate |
| Nastysaurus | PlayNicely gate | Nastysaurus.java:215-217 (retaliation target dropped), :279-281 (scan) | Nastysaurus.java:72-73 (goal; `BugMeleeAttackGoal` has no gate) | missing gate, two sites |
| PitchBlack | PlayNicely gate | PitchBlack.java:541-543 | PitchBlack.java:509-518 | missing gate |
| Pointysaurus | PlayNicely gate | Pointysaurus.java:186-188 (retaliation dropped), :250-252 (scan) | Pointysaurus.java:65-73 (stare goal + player goal; none) | missing gate, two sites |
| PurplePower | PlayNicely gate | PurplePower.java:268-270 | PurplePower.java:186-192 | missing gate |
| Rat | PlayNicely gate | Rat.java:252-254 | EntityRat.java:212-218 | missing gate |
| Robot1 | PlayNicely gate | Robot1.java:205-207 | Robot1.java:141-147 | missing gate |
| Robot3 | PlayNicely gate | Robot3.java:322-324 | Robot3.java:172-178 | missing gate |
| Robot5 | PlayNicely gate | Robot5.java:296-298 | Robot5.java:161-167 | missing gate |
| SeaMonster | PlayNicely gate | SeaMonster.java:514-516 | SeaMonster.java:172-177 — none in the file | missing gate |
| SeaViper | PlayNicely gate | SeaViper.java:531-533 | SeaViper.java:95-96 (goals; none) | missing gate |
| Skate | PlayNicely gate | Skate.java:283-285 | Skate.java:122-127 — none | missing gate |
| SpiderDriver | PlayNicely gate | SpiderDriver.java:104-106 (mount scan), :160-162 (combat scan) | SpiderDriver.java:148-154, :166-171 — neither | missing gate (both scans) |
| SpitBug | PlayNicely gate | SpitBug.java:371-373 | EntitySpitBug.java:71-72 (goal; none) | missing gate; not named in ENT-S-108 |
| Spyro | PlayNicely gate | Spyro.java:698-700 | EntitySpyro.java:483-490 — none in the file | missing gate (BOSS-017 covered bosses + Princess only) |
| Stinky | PlayNicely gate | Stinky.java:688-690 | EntityStinky.java:449-455 — none | missing gate |
| TerribleTerror | PlayNicely gate | TerribleTerror.java:296-298 | EntityTerribleTerror.java:168-174 | missing gate |
| ThePrinceAdult | PlayNicely gate (goal half) | ThePrinceAdult.java:112-114 — task registered only when `PlayNicely == 0` (the custom :520-522 gate matches port :855) | ThePrinceAdult.java:145 — goal registered regardless | missing gate (goal half) |
| ThePrinceTeen | PlayNicely gate (goal half) | ThePrinceTeen.java:116-118 (custom :540-542 matches port :879) | ThePrinceTeen.java:156 — goal ungated | missing gate (goal half) |
| TRex | PlayNicely gate | TRex.java:185-187 (nulls `rt`), :251-253 (scan) | TRex.java:55-59 (goals; none) | missing gate (both sites) |
| Triffid | PlayNicely gate | Triffid.java:322-324 | EntityTriffid.java:240-246 | missing gate |
| TrooperBug | PlayNicely gate | TrooperBug.java:511-513 | EntityTrooperBug.java:70-74 (goals; none) | missing gate |
| Urchin | PlayNicely gate | Urchin.java:273-275 | Urchin.java:148-156 — none | missing gate |
| MyEntityAINearestAttackableTarget (Boyfriend / Girlfriend) | PlayNicely gate | Boyfriend.java:137-147, Girlfriend.java:163-173 — registration-time `PlayNicely == 0` on every goal | Boyfriend.java:144, Girlfriend.java:208 — the Monster goal ungated; `PLAY_NICELY.get()` is read by `JealousyTargetGoal` :41 and by the melee loops (Boyfriend.java:282, Girlfriend.java:320, which null the loop's local victim), but the Monster goal still acquires and Boyfriend's `RangedAttackGoal` :131 consumes `getTarget()` ungated | missing gate on the Monster goal |

- **Species:** 43 hunters + 1 helper (44 rows; 52 sites — AntRobot, Hammerhead, Nastysaurus, Pointysaurus, SpiderDriver, TRex have two each, the helper four).
- **Shapes:** 29 one-line `findSomethingToAttack` / `getNearestPlayer` gates at the orig position; 15 goal-side gates where orig gated at construction (`if (PlayNicely == 0)` registration) — the port convention reads the flag live (the TheKing BOSS-017 idiom, as ENT-S-110 did), so these become a `canUse` predicate or a goal-selector guard; 4 revenge-null sites (orig nulls `rt` under the flag: Hammerhead, Nastysaurus, Pointysaurus, TRex); Godzilla's mapping is the one semantic case (skip the local pick, keep the stored target).
- **What a player would notice:** with Play Nicely on, 1.7.10's hunters stop hunting on their own (they still retaliate); in the port 43 of them keep hunting — a "play nicely" server still sees Cephadromes, the five robots, sea monsters, the Leonopteryx's and the Princes' monster goals, the Boyfriend / Girlfriend and every player-hunting bug picking fights — and Godzilla additionally drops a target it was already chasing on every pass.
- **Test:** one `@GameTestGenerator` over the 52 sites (the `LeonTargetingTests` s110 shape: flag off → the control prey accepted, flag on → refused, flag restored in a finally; goal sites through the goal's `canUse` / `TargetingConditions` as `IgnoreScreenParityTests` does; the four `rt` sites with a written revenge target read back null under the flag; Godzilla with a stored target kept under the flag), own batch.
- **Effort:** M (many one-line sites; the goal-side gates need the live-read predicate). **Refuters:** 1.

### T2 — Missing / dropped line-of-sight and feet-ray steps (18 rows, 18 hunters)

| species | aspect | orig cite | port cite | one-line description |
|---|---|---|---|---|
| AntRobot | filter order | AntRobot.java:1047 (hunt sight), :974 (stomp sight); dircheck :1050-1065 (call sites :133, :622) | AntRobot.java:697-704 (hunt), :677-686 (stomp) — no sight, no dircheck | sight dropped from both filters; the dircheck heading branch (see T8 note) dropped |
| Fairy | filter order | Fairy.java:232 | Fairy.java:145-149 | the sight step is missing |
| GiantRobot | filter order | GiantRobot.java:327 | GiantRobot.java:232-238 | the sight step is missing |
| Irukandji | filter order | Irukandji.java:280 | Irukandji.java:137 (`!instabuild` only) | the sight step is missing |
| Lizard | filter order | Lizard.java:313 (sight); :316 AttackSquid; :328-330 buddy | Lizard.java:132-139 | sight step missing; the AttackSquid grant (T6) and the buddy side effect (T10) are the same row's other losses |
| PitchBlack | filter order | PitchBlack.java:501-503 | PitchBlack.java:520-526 (:527 after ENT-S-112) | sight step missing (not covered by ENT-S-112, which names the allies; lane M O4) |
| PurplePower | filter order | PurplePower.java:251 (sight); :236 PEACEFUL, :261-264 tamed-pet / royalty | PurplePower.java:194-203 | sight missing (the PEACEFUL step is T7, the tamed-pet / royalty steps T6) |
| Robot1 | filter order | Robot1.java:189 | Robot1.java:149-155 | sight step missing |
| Robot2 | filter order | Robot2.java:366 | Robot2.java:270-276 | sight step missing |
| Robot3 | filter order | Robot3.java:306 | Robot3.java:180-186 (the SHOT is gated on sight :135 instead; orig gates the shot on a 0.5-rad head-facing cone :255-263) | sight step missing from selection |
| Robot4 | filter order | Robot4.java:370 | Robot4.java:285-291 | sight step missing |
| Robot5 | filter order | Robot5.java:280 | Robot5.java:169-175 (shot gated on sight :125; orig cone :228-235) | sight step missing from selection |
| Skate | filter order | Skate.java:272 | Skate.java:125 | missing gate: sight |
| SpiderDriver | filter order | SpiderDriver.java:149-151 | SpiderDriver.java:156-164 | the sight check is absent |
| Spyro | filter order | Spyro.java:709, :436-438 (`canSeeTarget` eye y+0.75 → feet ray in the scan loop); :691 Mothra | EntitySpyro.java:483-490 — no second ray | the feet-ray gate is missing; Mothra (a Butterfly, not a Monster, in the port) no longer prey — see T6 note |
| Stinky | filter order | Stinky.java:699, :317-319; :681 Mothra | EntityStinky.java:449-455 | feet-ray gate missing; Mothra dropped |
| ThePrince | filter order | ThePrince.java:776, :416-418 | ThePrince.java:578-585 | the eye(y+0.75)→feet block ray is absent; the ladder itself was restored under BOSS-024 |
| ThePrincess | filter order | ThePrincess.java:857, :404-406 | ThePrincess.java:593-600 | missing gate: feet ray |

- Cross-references, not counted here: Mothra's stage-1 sight step (its row is in T8), CaterKiller's custom `MyCanSee` (T3b), Hammerhead / SeaMonster sight (T3b), AttackSquid / WaterDragon sight (T3a).
- **Species:** 18 (14 eye-to-eye `canSee` steps, 4 feet rays).
- **What a player would notice:** hunters lock on through walls — the five robots choose what they cannot see (their shots are gated on sight, their choice is not), the Fairy, Giant Robot, Lizard, Purple Power, Nightmare, Skate, Irukandji and Spider Driver pick a hidden target over a visible one, the Ant Robot stomps and hunts blind; Spyro, Stinky, the Prince and the Princess bite what a feet-level ray refused (a target on a ledge above or below).
- **Test:** a generator per site: the species or mock player 8 blocks ahead behind a one-block wall — refused; wall removed — accepted (the `PitchBlackAllyTests` geometry with a wall row); the feet-ray sites need a target whose eyes are visible and feet occluded (a slab under the target) — refused in orig, accepted at HEAD.
- **Effort:** M. **Refuters:** 1.

### T3a — The whole proactive hunt is missing (19 rows, 4 hunters; dependent rows ride with the root)

| species | aspect | orig cite | port cite | one-line description |
|---|---|---|---|---|
| AttackSquid | scan set | AttackSquid.java:608 (box 10x4x10), :502 (`nextInt(10)==1`) | AttackSquid.java:169-185 — reads `getTarget()` only; no `getEntitiesOfClass`, no `isSuitableTarget`, no PLAY_NICELY in the 296-line file | the whole hunt is missing; ENT-A-017..022 record HP / swing / watercanon / drops / spawn / hurt, none the scan |
| AttackSquid | filter order | AttackSquid.java:551-602 | — | the whitelist ladder (non-creative players, Girlfriend, Boyfriend, Zombie, Villager, Spider, CaveSpider, Lizard; Ghost / GhostSkelly refused; AttackSquid → `buddy` on 1-in-5) absent |
| AttackSquid | PlayNicely gate | AttackSquid.java:605-607 | — | dependent |
| AttackSquid | creative gate | AttackSquid.java:566 | — (only the revenge goal's `invulnerable`) | dependent |
| AttackSquid | allies / species exclusions | AttackSquid.java:586-600 | — | Ghost, GhostSkelly, AttackSquid (buddy adoption) refused; dependent |
| AttackSquid | tie-break / selection rule | AttackSquid.java:54, :70, :609 | — | dependent |
| AttackSquid | target set / release | AttackSquid.java:613-617, :622; buddy-follow :515-517 | AttackSquid.java:170-184 | no re-acquisition once the revenge target is lost; buddy-follow absent |
| AttackSquid | other | AttackSquid.java:601 — `wasshot != 0` (a Squid Zooka-launched squid — the survey's "Kraken-launched" corrected 2026-09-04: only ItemSquidZooka sets `wasshot` in either tree) makes every living entity suitable | — | an orig-only rule dropped with the scan |
| Dragon | scan set | Dragon.java:115-117 — vanilla `EntityAINearestAttackableTarget(this, EntityLiving.class, 0, true, false, IMob.mobSelector)` registered when `PlayNicely == 0` (every tick, follow-range box, sight, IMob only) | Dragon.java:145 — `targetSelector` holds `HurtByTargetGoal` only | the continuous vanilla IMob channel (a) is missing; the custom 20x20x20 scan (b) matches |
| Dragon | target set / release | Dragon.java:115-118 — channel (a) sets the attack target every tick it finds an IMob | Dragon.java:145 | only channel (a)'s stored-target feed is missing; the rest matches |
| EntityButterfly | scan set | EntityButterfly.java:218 (box 8x5x8), :161 (Islands dimension `DimensionID4`, `butterfly_type == 1`, entity-rand 1-in-10, !PEACEFUL); filter :194-215 | EntityButterfly.java:40, :65-66 — `AmbientCreature` with `AmbientFlightGoal` only | the Islands-dimension vampire-type hunt is gone; the port's `VampireButterfly` (MOD-021 optional wiki mob) is a separate Monster with a vanilla player goal, not this branch |
| WaterDragon | scan set | WaterDragon.java:689 (box 14x4x14), :597 (`!PEACEFUL && nextInt(5)==1`), :682-706 | WaterDragon.java:99-114 — `WaterCanonAttackGoal` consumes `getTarget()`; targets only from `HurtByTargetGoal` :113 and `hurt` :266-269 | the whole proactive hunt is absent; unrecorded (ENT-S-074 restored only the ranged branch) |
| WaterDragon | filter order | WaterDragon.java:650-680 | — | dependent on the scan row |
| WaterDragon | PlayNicely gate | WaterDragon.java:683-685 | — | dependent |
| WaterDragon | creative gate | WaterDragon.java:677 | — | dependent |
| WaterDragon | PEACEFUL gate | WaterDragon.java:597 + :651 | `HurtByTargetGoal` rejects players in PEACEFUL only; `WaterCanonAttackGoal` :392-467 has no PEACEFUL test | missing gate (partial) |
| WaterDragon | allies / species exclusions | WaterDragon.java:666 (WaterDragon), :672-674 (tamed → EntityMob only), :679 (`isAttackableNonMob`, orig list) | hurt-side exclusions only (:256-260) | exclusion list / tamed rule (dependent; a restore inherits the port helper — T6) |
| WaterDragon | tie-break / selection rule | WaterDragon.java:50, :67, :690 | — | dependent |
| WaterDragon | other | WaterDragon.java:686-688 (baby never hunts), :672-674 (tamed restricted to EntityMob) | — | dependent |

- **Species:** 4 (AttackSquid 8 rows, WaterDragon 8, Dragon 2, EntityButterfly 1).
- **What a player would notice:** the Attack Squid and the Water Dragon never hunt on their own — they only retaliate, so a swimmer is safe until they hit one; a Kraken-launched squid does not turn on everything as in 1.7.10; the Islands-dimension vampire butterfly never bites players or horses; a Dragon does not go after nearby monsters continuously, only when its 1-in-10 / 1-in-7 / 1-in-9 custom rolls hit.
- **Test:** the `TargetScanParityTests` shape (the ENT-S-108 lane's class): a prey species inside the orig box acquired under forced rolls, one outside the box refused; a generator row per whitelist / exclusion member; the Dragon's IMob channel through its restored goal's `TargetingConditions`; own batch.
- **Effort:** L (four scan loops transcribed from orig; AttackSquid's buddy and `wasshot` rules, WaterDragon's tamed and baby rules). **Refuters:** 2 (scan loops restored).

### T3b — Scan-set narrowing, geometry, cadence and state gates (22 rows, 16 blocks)

| species | aspect | orig cite | port cite | one-line description |
|---|---|---|---|---|
| AntRobot | scan set | AntRobot.java:1015 — hunt box (12·distmul)x12x(12·distmul); unridden distmul 2.0 → 24x12x24 (:117, :133) | AntRobot.java:689 — always inflate(12,12,12) | the unridden hunt box 24x12x24 → 12x12x12; stomp and ridden hunt match |
| Brutalfly | scan set | Brutalfly.java:215 — `findNearestEntityWithinAABB(EntityPlayer.class, box 30x20x30)` | EntityBrutalfly.java:204 — `getNearestPlayer(this, 30.0)`, sphere | strafe geometry box → sphere r 30 (box corners reach ~42 blocks, the sphere adds ±30 vertical); spectators skipped (no 1.7.10 analogue) |
| CaterKiller | scan set | CaterKiller.java:563 (box 20x8x20), :462 (`nextInt(4)==0`) | EntityCaterKiller.java:100 — `NearestAttackableTargetGoal<Player>`, FOLLOW_RANGE 40 (:110) | players only — EntityMob and `isAttackableNonMob` prey (:553-556) never hunted; box → sphere r 40; cadence 1-in-4 → ≈1-in-10; not on ENT-S-108's list |
| CaterKiller | filter order | CaterKiller.java:533-557 (`MyCanSee` :543, :626-676: a 10-step block ray from 2.5 blocks ahead at y+3 to the target's mid-height) | the `TargetingConditions` chain | the custom LoS replaced by the vanilla eye ray; the non-player branches absent (rides with the rebuild) |
| CaterKiller | allies / species exclusions | CaterKiller.java:550 — CaterKiller refused after the player branch | — | absent with the non-player branch (rides with the rebuild) |
| Dragonfly | scan set | Dragonfly.java:235 (box 10x6x10), :124 / :142 — 1-in-12 on every tick where the 1-in-300 / near flight retarget did NOT fire | entity/ai/DragonflyHuntGoal.java:58-67, :70-71 — `pickRetarget` runs only when the flight retarget fires (`AmbientFlightGoal` :109-113) and then rolls 1-in-12 | cadence inverted: ≈ every 12 ticks → "roughly every 3600 ticks" (the goal's own javadoc :27-28) |
| EnderKnight | scan set | EnderKnight.java:61-81 — `func_70782_k` every target-less tick; nearest player of ANY mode within 64 | EnderKnight.java:47 — `NearestAttackableTargetGoal<Player>`, FOLLOW_RANGE 64 (:57) | one nearest-of-any-mode candidate per tick versus the nearest eligible player ≈ every 10 ticks |
| EnderReaper | scan set | EnderReaper.java:61-81 — within 81 | EnderReaper.java:50-51 — FOLLOW_RANGE 81 (:82) | geometry matches (81); cadence and candidate set differ |
| Hammerhead | scan set | Hammerhead.java:255 (box 18x9x18), :191 (1-in-3) | Hammerhead.java:129 — `getNearestPlayer(this, 18.0)` | living box → player sphere: the ENT-S-108 class, but Hammerhead is not on that record |
| Hammerhead | filter order | Hammerhead.java:225-249 | Hammerhead.java:130 — `!instabuild` only | sight, own-kind and the mob / non-mob prey rules absent (rides with the rebuild) |
| Hammerhead | allies / species exclusions | Hammerhead.java:238 (Hammerhead spared), :245 (EntityMob), :248 (`isAttackableNonMob`, orig list) | — (players only) | prey = EntityMob + orig `isAttackableNonMob` + non-creative players; a restore inherits the port helper (T6) |
| Irukandji | scan set | Irukandji.java:294 — box 6x4x6 | Irukandji.java:136 — `getNearestPlayer(this, 6.0)` | class equal (orig's filter accepted only players); the geometry differs (box ±6 / ±4 → sphere 6) |
| Mothra | scan set (stage 1) | Mothra.java:224 — `findNearestEntityWithinAABB(EntityPlayer.class, box 25x20x25)` | Mothra.java:392 — `getNearestPlayer(this, 25.0)` | stage-1 box → sphere 25 (taller, narrower on diagonals); stage 2 MATCH in classic, RECORDED (MOD-029) in modern |
| PitchBlack | scan set | PitchBlack.java:259-275 — the server 1-in-250 heal branch: 1-in-5 & solid ground within 10 → scan, null → activity 0 | PitchBlack.java:392-403 — the tick has no heal branch (:421-431 and :448-449 match orig :281-288 / :361-363) | the only call site that RESETS activity to 0 when nothing is found is absent |
| SeaMonster | scan set | SeaMonster.java:517 (box 16x4x16), :465 (1-in-5) | SeaMonster.java:172-174 — `getNearestPlayer(this, 16.0)` when `getTarget()==null` | players only, sphere versus box; not in ENT-S-108's list |
| SeaMonster | filter order | SeaMonster.java:487-511 | SeaMonster.java:175 — `!instabuild` only | sight (:497) missing; EntityMob / attackable-non-mob prey absent (rides with the rebuild) |
| SeaViper | scan set | SeaViper.java:534 (box 18x4x18), :482 (1-in-5) | SeaViper.java:96 — `NearestAttackableTargetGoal<Player>`, FOLLOW_RANGE 32 (:106); its comment :84-85 says it "replace[s] the legacy 18×4×18 scan" | players only; 32-sphere versus box; cadence; not in ENT-S-108's list |
| SeaViper | filter order | SeaViper.java:504-528 | the vanilla `TargetingConditions.forCombat` chain | the vanilla chain replaces the orig ladder; EntityMob / attackable-non-mob prey absent (rides with the rebuild) |
| Skate | scan set | Skate.java:286 — box 10x4x10 | Skate.java:124 — `getNearestPlayer(this, 10.0)` | sphere r 10 versus the box; the class narrowing is not observable (players only in orig too) |
| Stinky | scan set | Stinky.java:691, :568 — the combat roll runs BEFORE the `activity == 1` return (:582-607) | EntityStinky.java:450, :329 — `doMovement` returns first when `activity != 2` (:312) | state gate: a grounded Stinky never scans in the port |
| MyEntityAINearestAttackableTarget (Boyfriend / Girlfriend) | scan set | MyEntityAINearestAttackableTarget.java:56, :36, :53 — per-goal distance 20 (EntityCreeper goal) / 15 (IMob goal), chance 0 = every tick | Boyfriend.java:144, Girlfriend.java:208 — `NearestAttackableTargetGoal<Monster>`, FOLLOW_RANGE default 16, 1-in-10; `entity/ai/JealousyTargetGoal.java:34-45` keeps its own distance | per-goal 20 / 15 → 16 for both; every tick → 1-in-10; the Creeper priority goal dropped |
| MyValentineTarget (Girlfriend) | scan set (Player goal) | MyValentineTarget.java:60 — box 16x4x16 | Girlfriend.java:226-248 `ValentineTargetGoal` — the Player goal over all players within `getFollowDistance()` 16 (:240-242) | player box → sphere 16 (×0.8 for a sneaking player); the Boyfriend goal's box matches |

- **Shapes:** (i) players-only scans NOT on ENT-S-108, with their dependent filter / allies rows — CaterKiller, Hammerhead, SeaMonster, SeaViper (10 rows); (ii) box → sphere geometry — Brutalfly strafe, Irukandji, Skate, Mothra stage 1, the Girlfriend's Valentine player goal (5); (iii) cadence / candidate-set changes — Dragonfly, EnderKnight, EnderReaper, the Boyfriend / Girlfriend goal (4); (iv) a box size, a call site and a state gate — AntRobot, PitchBlack, Stinky (3).
- **Watch after ENT-S-108 lands** (its rows are FIX IN FLIGHT, not counted here): the parts flag geometry / cadence residuals on the ENT-S-108 hunters — CaveFisher 10x3x10 versus 16x4x16 / sphere 16 and 1-in-8 versus ≈1-in-10; DungeonBeast 16x3x16 → sphere 24; EmperorScorpion 24x6x24 → 40; HerculesBeetle 16x6x16 → 24; Nastysaurus 32x8x32 → 40; SpitBug 12x7x12 → 32; TRex 20x6x20 → 40; TrooperBug 12x7x12 → 32; Urchin 16x3x16 → sphere 16 — which survive if the fix widens the goals' class rather than restoring the orig box scans.
- **What a player would notice:** the Cater Killer, Hammerhead, Sea Monster and Sea Viper ignore the villagers, animals and monsters 1.7.10 sent them after; the Brutalfly, Mothra, Irukandji and Skate reach a player 30 / 25 / 6 / 10 blocks straight up or down that a box would not (and miss one at a box corner); the Dragonfly almost never hunts (every ~3600 ticks instead of ~12); the Ender Knight and Reaper acquire on a 10-tick cadence instead of every tick; a Stinky standing on the ground never hunts; the Nightmare never resets to idle after an empty heal-branch scan; the unridden Ant Robot's hunt box is a quarter of the orig area.
- **Test:** `TargetScanParityTests` shape for the four rebuilds (prey in the orig box acquired; a member of each exclusion refused; the mob / non-mob prey granted); geometry tests with a mock player at a box corner (accepted in orig) and straight above at the sphere edge (refused in orig); cadence tests under forced rolls; a grounded Stinky with prey in the box (acquired in orig); own batch.
- **Effort:** L. **Refuters:** 2 for the rows that restore a scan loop (CaterKiller, Hammerhead, SeaMonster, SeaViper, the Boyfriend / Girlfriend goal), 1 for the geometry / cadence / state-gate rows.

### T3c — Scan-set widening on the port's vanilla goals (4 rows, 4 hunters)

| species | aspect | orig cite | port cite | one-line description |
|---|---|---|---|---|
| Leon | scan set (vanilla goal) | Leon.java:92-93 — `EntityAINearestAttackableTarget(this, EntityLiving.class, 0, sight, false, IMob.mobSelector)`: box 16x4x16 (no follow range set, :112-118), every tick, IMob EntityLivings | EntityLeon.java:158 — `NearestAttackableTargetGoal<>(this, Monster.class, 10, true, false, e -> !isTame() \|\| getTarget()==null)`: box 40x4x40 (FOLLOW_RANGE 40, :170), 1-in-10 | box 16 → 40; every tick → 1-in-10; IMob → Monster; the predicate is port-only (T9) |
| Pointysaurus | scan set | Pointysaurus.java:253 (box 12x5x12), :183 (1-in-6) | Pointysaurus.java:72-73 — `NearestAttackableTargetGoal<Player>`, FOLLOW_RANGE 24 (:84); plus `PointysaurusStareGoal` :65 | players-only is orig behaviour (:242-246; ENT-S-108 excludes Pointysaurus) but the box became a sphere of 24 — widening; the stare goal is a port invention (T9) |
| ThePrinceAdult | scan set (vanilla goal) | ThePrinceAdult.java:112-114 — `EntityAINearestAttackableTarget(EntityLiving.class, 0, sight, false, IMob)`: box 16x4x16, every 3rd tick | ThePrinceAdult.java:145 — `NearestAttackableTargetGoal<>(this, Monster.class, true)`, FOLLOW_RANGE 64 (:155) → 64x4x64 | goal box 64x4x64 versus 16x4x16 and release range 64 versus 16; the custom scans match; the owner goals (:142-143) are PORT-ONLY (T9) |
| ThePrinceTeen | scan set (vanilla goal) | ThePrinceTeen.java:116-118 — same, box 16x4x16 | ThePrinceTeen.java:156 — FOLLOW_RANGE 32 (:166) → 32x4x32 | goal box / release 32 versus 16; owner goals (:153-154) PORT-ONLY (T9) |

- **What a player would notice:** a wild Leonopteryx and the Princes go after monsters 40 / 64 / 32 blocks away instead of 16 and keep chasing that far (orig held to 16); the Pointysaurus sees a player 24 blocks away where 1.7.10 needed 12x5x12.
- **Test:** a goal-range test per site (the goal's `TargetingConditions` accepts at 15, refuses at 20 — read through the goal on the hunter's target selector, the `IgnoreScreenParityTests` TARGET_GOAL_PREDICATE shape); own batch.
- **Effort:** S (a FOLLOW_RANGE / per-goal range per site; the goals need `getFollowDistance` overrides rather than a smaller attribute where the attribute also drives pathing). **Refuters:** 1.

### T4 — Tie-breaks and sorters (35 rows, 35 blocks)

| species | aspect | orig cite | port cite | one-line description |
|---|---|---|---|---|
| Cephadrome | tie-break / selection rule | Cephadrome.java:61, :84, :580 — GenericTargetSorter | Cephadrome.java:118 — `Comparator.comparingDouble(this::distanceToSqr)` | plain distance, no creeper / silhouette weighting: the TF-035 swap never reached this file (ENT-A-082's Phase C fix predates it) |
| Cryolophosaurus | tie-break / selection rule | Cryolophosaurus.java:58, :218 | Cryolophosaurus.java:132 | creeper / silhouette weights lost (TF-035 remainder) |
| Dragonfly | tie-break / selection rule | Dragonfly.java:45, :236 | entity/ai/DragonflyHuntGoal.java:75-76 | plain nearest |
| Fairy | tie-break / selection rule | Fairy.java:63, :243 | Fairy.java:90, :156 | plain nearest |
| Frog | tie-break / selection rule | Frog.java:55, :312 | Frog.java:262 | plain nearest |
| GammaMetroid | tie-break / selection rule | GammaMetroid.java:59, :298 | EntityGammaMetroid.java:205 | plain nearest |
| Kyuubi | tie-break / selection rule | Kyuubi.java:56, :209 | EntityKyuubi.java:136 | plain nearest |
| Leon | tie-break / selection rule (custom scan) | Leon.java:63, :97, :435 | EntityLeon.java:734 | creeper / silhouette weighting dropped in the custom scan (the vanilla goal's nearest pick matches orig's plain goal sorter) |
| Lizard | tie-break / selection rule | Lizard.java:45, :62, :340 | Lizard.java:72, :151 | plain distance (TF-035 migration not done) |
| PurplePower | tie-break / selection rule | PurplePower.java:35, :44, :272 | PurplePower.java:41, :191 | plain distance |
| Rat | tie-break / selection rule | Rat.java:46, :63, :256 | EntityRat.java:217 | plain distance |
| Robot1 | tie-break / selection rule | Robot1.java:33, :44, :209 | Robot1.java:61, :146 | plain distance |
| SpiderDriver | tie-break / selection rule | SpiderDriver.java:33; sorts :108, :164 | SpiderDriver.java:52 (used :153 / :170) | TF-035 remainder, not swapped in the ENT-S-017/018/019 batch |
| Stinky | tie-break / selection rule | Stinky.java:48, :76, :692 | EntityStinky.java:454 | TF-035 remainder |
| TerribleTerror | tie-break / selection rule | TerribleTerror.java:47, :56, :300 | EntityTerribleTerror.java:173 | TF-035 remainder |
| ThePrinceTeen | tie-break / selection rule | ThePrinceTeen.java:79, :121, :544 | ThePrinceTeen.java:142 (used :884) | TF-035 remainder |
| Triffid | tie-break / selection rule | Triffid.java:42, :54, :326 | EntityTriffid.java:245 | TF-035 remainder |
| CaterKiller | tie-break / selection rule | CaterKiller.java:43, :62, :564 | vanilla `getNearestPlayer` — plain distSq to the eye, strict `<` (EntityGetter.java:148-160) | nearest player; rides with T3b |
| CaveFisher | tie-break / selection rule | CaveFisher.java:38, :49, :235 | the two vanilla goals (EntityGetter.java:148-160) | nearest by plain distSq; rides with ENT-S-108 if its rebuild sorts |
| DungeonBeast | tie-break / selection rule | DungeonBeast.java:53, :254 | vanilla goal (EntityGetter.java:148-165) | nearest player from the eye, first in `players()` order on ties; rides with ENT-S-108 |
| EmperorScorpion | tie-break / selection rule | EmperorScorpion.java:64, :508 | vanilla goal | nearest player `<`; rides with ENT-S-108 |
| Hammerhead | tie-break / selection rule | Hammerhead.java:48, :256 | Hammerhead.java:129 (`getNearestPlayer`, EntityGetter.java:78-93) | nearest player `<`; rides with T3b |
| HerculesBeetle | tie-break / selection rule | HerculesBeetle.java:51, :421 | vanilla goal | nearest player `<`; rides with ENT-S-108 |
| Irukandji | tie-break / selection rule | Irukandji.java:47, :295 | Irukandji.java:136 (`getNearestPlayer`) | nearest `<`; for standing players the sorter reduces to nearest (uniform 1.08 silhouette), sneaking players (0.9, undivided) rank differently; rides with T3b |
| Nastysaurus | tie-break / selection rule | Nastysaurus.java:41, :52, :283 | vanilla goal ((v) EntityGetter.java:160-167) | no creeper / silhouette weighting — matters once the scan is widened; rides with ENT-S-108 |
| SeaMonster | tie-break / selection rule | SeaMonster.java:39, :55, :518 | SeaMonster.java:174 (`getNearestPlayer`) | nearest player `<`; rides with T3b |
| SeaViper | tie-break / selection rule | SeaViper.java:42, :59, :535 | SeaViper.java:96 (vanilla goal) | vanilla nearest `<`; rides with T3b |
| SpitBug | tie-break / selection rule | SpitBug.java:47, :61, :375 | EntitySpitBug.java:71-72 (vanilla goal) | vanilla nearest; rides with ENT-S-108 |
| TRex | tie-break / selection rule | TRex.java:40, :50, :255 | TRex.java:58-59 (vanilla goal) | vanilla nearest; rides with ENT-S-108 |
| TrooperBug | tie-break / selection rule | TrooperBug.java:50, :63, :515 | EntityTrooperBug.java:73-74 (vanilla goal) | vanilla nearest; rides with ENT-S-108 |
| Urchin | tie-break / selection rule | Urchin.java:43, :55, :277 | Urchin.java:150 (`getNearestPlayer`) | nearest player `<`; rides with ENT-S-108 |
| Brutalfly | tie-break / selection rule (strafe) | Brutalfly.java:215 — `World.func_72857_a` replaces on `<=`: the LAST equidistant player wins (bytecode verified under ENT-S-105) | EntityBrutalfly.java:204 — `getNearestPlayer`, strict `<`: the FIRST wins (EntityGetter.java:85) | the ENT-S-105 class, fixed for the Kraken only |
| EnderKnight | tie-break / selection rule | EnderKnight.java:65 — the single nearest player (`<`), THEN `shouldAttackPlayer` :83-93 | EnderKnight.java:47 — the nearest ELIGIBLE player | nearest-then-filter versus filter-then-nearest: a nearer non-staring player blocks a farther starer in orig |
| EnderReaper | tie-break / selection rule | EnderReaper.java:65, :83-93 | EnderReaper.java:50-51, :60-73 — the nearest STARING eligible player | nearest-then-filter versus filter-then-nearest |
| MyEntityAINearestAttackableTarget (Boyfriend / Girlfriend) | tie-break / selection rule | MyEntityAINearestAttackableTargetSorter.java:21-31 — distSq halved for creepers (no silhouette term), stable sort | VAN-NAT nearest by plain distSq from the eye, strict `<` | the creeper halving dropped |

- **Shapes:** (i) the TF-035 plain-distance remainder on custom scans — 17 rows, each a one-line comparator swap to `GenericTargetSorter` through `firstMatch` (Cephadrome … Triffid); (ii) vanilla-goal / `getNearestPlayer` plain-nearest sites — 14 rows that collapse into T3b (CaterKiller, Hammerhead, Irukandji, SeaMonster, SeaViper) or into ENT-S-108's rebuild (CaveFisher, DungeonBeast, EmperorScorpion, HerculesBeetle, Nastysaurus, SpitBug, TRex, TrooperBug, Urchin) only if those rebuilds sort with `GenericTargetSorter`; (iii) `<=` last-wins versus `<` first-wins on the Brutalfly strafe (1); (iv) nearest-then-filter versus filter-then-nearest on the Ender Knight / Reaper (2); (v) the helper goal's creeper halving (1).
- **What a player would notice:** with several candidates in reach, 1.7.10 preferred creepers (half distance) and small silhouettes over big ones; the port picks the plain nearest at these sites, so a big nearby mob outranks a small one that orig weighted closer, and a creeper no longer draws the hunter first; the Brutalfly strafes the first of two equidistant players instead of the last; the Ender Knight / Reaper hunt the nearest player who stares instead of testing only the nearest player (a friend standing closer no longer shields the starer).
- **Test:** two candidates at weights the sorters separate — a creeper at distSq 2d beside a pig at d (orig picks the creeper), a 2x2 silhouette at d beside a 1x1 at 1.5d — through `findSomethingToAttack` by reflection (the `KrakenTargetingParityTests` shape); two equidistant mock players for the Brutalfly (`ENT-S-105` pin shape); a nearer non-staring player plus a farther starer for the Knight / Reaper; own batch.
- **Effort:** M (17 one-line swaps, 4 special cases; the 14 goal sites ride elsewhere). **Refuters:** 1.

### T5 — Target set / release rules (33 rows, 33 blocks)

| species | aspect | orig cite | port cite | one-line description |
|---|---|---|---|---|
| Alosaurus | target set / release | Alosaurus.java:165-175 — the pick is transient (look / reach / swing / navigate on the scan tick), never stored; :54 revenge target nothing consumes | Alosaurus.java:171-177 — `setTarget(prey)`, cleared on an empty scan; `DinosaurMeleeAttackGoal` :78-79 chases it; `HurtByTargetGoal` :89 re-asserts; FOLLOW_RANGE 32 (:100) | an attacker the scan excludes (Cryolophosaurus, VelocityRaptor, an ignoreable) is chased until FOLLOW_RANGE 32 or 300 unseen ticks; ENT-A-009 calls the hand-off "the accepted mapping" but no MOD record exists |
| AntRobot | target set / release | AntRobot.java:109-118; :130-134 (melee re-pick with dircheck TRUE); :579-583 (`EntityLiving` attackers only) | AntRobot.java:229-235; :249-253 (no dircheck re-pick); :271-274 (`hurt` stores any LivingEntity, players included) | player attackers become the stored target; the dircheck re-pick at the melee site is gone |
| CaterKiller | target set / release | CaterKiller.java:463-473 — stored target read first; `nextInt(200)==0` inside the 1-in-4 block (≈ 1-in-800 per tick); transient pick | goals store via `setTarget`, TargetGoal release (FOLLOW_RANGE 40, 60-tick unseen); entity/ai/BugMeleeAttackGoal.java:123-129 forget 200 EVERY tick | sticky vanilla target versus transient re-pick; forget cadence ≈ 1-in-800 → 1-in-200 |
| CaveFisher | target set / release | CaveFisher.java:169-181 — transient pick per 1-in-8 tick; :55 revenge inert; :185-191 stores nothing | goals store via `setTarget`; BugMeleeAttackGoal forget 0 (:64); `HurtByTargetGoal` :78 now consumed, with the re-assert | a sticky vanilla target and a live revenge chase where 1.7.10 re-picked every scan and never retaliated |
| Crab | target set / release | Crab.java:342-344 — `nextInt(100)==1 → setAttackTarget(null)`; :232-238 `EntityLiving` attackers only | Crab.java:270-274 — no 1-in-100 forget; :235-237 `hurt` stores any LivingEntity | the 1-in-100 forget is missing; player attackers are stored directly, bypassing the revenge goal's screen |
| CreepingHorror | target set / release | CreepingHorror.java:136-138 — `nextInt(200)==1 → func_70604_c(null)` = `setRevengeTarget(null)` | CreepingHorror.java:150-152 — `setTarget(null)` | wrong field (the port's own convention for this orig line is `setLastHurtByMob(null)`); inert in both trees today |
| Cryolophosaurus | target set / release | Cryolophosaurus.java:147-149 — revenge cleared 1-in-200; no attack goal | Cryolophosaurus.java:90-91 `setLastHurtByMob(null)` PLUS `DinosaurMeleeAttackGoal.Presets.cryolophosaurus` :37 forget 200 (BugMeleeAttackGoal :123-128) | a second, port-only 1-in-200 drop of the attack target |
| Dragonfly | target set / release | Dragonfly.java:146-148 — never stored; flight target := prey; one bite per pass | entity/ai/DragonflyHuntGoal.java:62 `setTarget(prey)`, :50-54 bites every tick; nothing clears it (`hurt` only moves the flight target :90-96) | the prey is stored and never dropped |
| DungeonBeast | target set / release | DungeonBeast.java:173-185 — re-picked every 1-in-8 pass; `setAttacking(0)` when none (:184) | DungeonBeast.java:77 (goal stores); TargetGoal hold (alive / within 24 / seen within 30 evaluated ticks); BugMeleeAttackGoal :61-62 forget 0 | stored-and-held versus re-scanned each pass |
| EmperorScorpion | target set / release | EmperorScorpion.java:410-416 — dead → clear; 1-in-100 inside the 1-in-4 pass (≈ 1-in-400 per tick); :418 pick never stored | EntityEmperorScorpion.java:80 (goal-stored), :163-166; `EmperorScorpionPoisonGoal` forget 100 every tick (BugMeleeAttackGoal :123-128) | forget cadence ≈ 4× orig; the scan pick is held by the goal |
| EnderKnight | target set / release | EnderKnight.java:111-115 — the daylight roll nulls the target (`field_70789_a = null`) | EnderKnight.java:75-96 — `aiStep` has no daylight drop; TargetGoal unseen release instead | daylight release missing; unseen-ticks release port-only (the Reaper's daylight drop :121-130 matches) |
| EnderReaper | target set / release | EnderReaper.java:111-115 — the legacy loop holds `entityToAttack` until dead / creative / the daylight roll (`td.bq`, V10) | EnderReaper.java:50-51 (goal) + `TargetGoal` — dropped beyond FOLLOW_RANGE 81 (:82) and after 30 evaluated unseen ticks; `HurtByTargetGoal` :47 after 300; the daylight roll → `setTarget(null)` :121-130 matches | the range / unseen-ticks releases are port-only — status corrected by the spot-check refuter; the EnderKnight row counts the identical release |
| EntityCannonFodder | target set / release | EntityCannonFodder.java:346-348 — `func_70604_c(null)` 1-in-200 | EntityCannonFodder.java:272 — `setTarget(null)` 1-in-200 | clears the attack target where orig cleared the revenge (last-hurt-by) target |
| GiantRobot | target set / release | GiantRobot.java:243-249 — the 1-in-100 clear (:243-245) BEFORE the read (:246) | GiantRobot.java:136-141 — the read (:136) BEFORE the clear (:137) | one-pass ordering: the cleared target is still engaged this pass |
| Hammerhead | target set / release | Hammerhead.java:198-204 — `rt` dropped dead / 1-in-250, SKIPPED when out of sight this pass | Hammerhead.java:120-127 — no out-of-sight skip; falls back to `getTarget()` from `HurtByTargetGoal` :59 | the out-of-sight skip is missing |
| HerculesBeetle | target set / release | HerculesBeetle.java:352-357 — dead → cleared; pick never stored | EntityHerculesBeetle.java:60 (goal-stored), :141-145; BugMeleeAttackGoal forget 0 (:62) | the scan pick is held by the goal |
| Irukandji | target set / release | Irukandji.java:299-309 — stored target preferred while alive, cleared when dead (:303); pick never stored | Irukandji.java:134-152 — the pick IS stored (`setTarget` :139); a dead stored target is never cleared (:142 → :151-152 `setAttacking(0)`) | a dead scan target sticks — re-scanning stops until the jelly is next hurt |
| Kyuubi | target set / release | Kyuubi.java:157-159 — `func_70604_c(null)` 1-in-200 | EntityKyuubi.java:106-108 — `setTarget(null)` | clears the attack target where orig cleared the revenge target |
| LeafMonster | target set / release | LeafMonster.java:160-162 — `setRevengeTarget(null)` 1-in-100 | EntityLeafMonster.java:122-124 — `setTarget(null)` | orig forgets who hurt it, the port drops the current target; inert in both (no consumer) |
| Nastysaurus | target set / release | Nastysaurus.java:219-225 — `rt` dropped dead / 1-in-250, KEPT through sight loss, re-evaluated every hunt tick | BugMeleeAttackGoal.java:123-129 (1-in-250 per tick); TargetGoal (beyond 40 / unseen 60); `HurtByTargetGoal` :69 (300) | the port drops the hunt target after 60 unseen ticks and the revenge target after 300; orig had no memory window PlayNicely residual carried from ENT-S-115 (wave 1): under the flag the goal keeps consuming a stored revenge target every tick, which orig's pass-local blanking stood down — the transcription target is the melee goal's tick (per-preset stand-down), ruled here. |
| Peacock | target set / release | Peacock.java:65 — vanilla `EntityAINearestAttackableTarget(Termite.class, chance 6, sight)`, an attackTarget no goal consumed | Peacock.java:57-65 — no target goals | the port drops the orig Termite target goal (inert in orig — nothing read the attackTarget) |
| Pointysaurus | target set / release | Pointysaurus.java:189-197 — `rt` dropped dead / 1-in-250 / not visible this tick | entity/ai/DinosaurMeleeAttackGoal.java:36 (forget 1-in-250 per tick); TargetGoal (beyond 24 / unseen 60); `HurtByTargetGoal` :59 (300); `PointysaurusStareGoal` :78-83 | release rule (as Nastysaurus) PlayNicely residual carried from ENT-S-115 (wave 1): under the flag the goal keeps consuming a stored revenge target every tick, which orig's pass-local blanking stood down — the transcription target is the melee goal's tick (per-preset stand-down), ruled here. |
| Rat | target set / release | Rat.java:156-158 — `setRevengeTarget(null)` 1-in-200 | EntityRat.java:153-155 — `setTarget(null)` | as LeafMonster; inert in both |
| Robot2 | target set / release | Robot2.java:57 — `EntityAIHurtByTarget(this, false)`: no call for help, own kind not ignored; :338-351 | Robot2.java:98 — `HurtByTargetGoal(this, Robot2.class).setAlertOthers()` | port-only same-kind alert (every Robot2 within FOLLOW_RANGE × 10 with no target) and same-kind damage exemption |
| Scorpion | target set / release | Scorpion.java:189-191 — each 1-in-6 pass rescans; an empty scan → `setAttacking(0)` | EntityScorpion.java:144 — `setTarget(prey)`, an empty scan does not clear it; BugMeleeAttackGoal forget 0 (:60), `canContinueToUse` :90-96 | a target that leaves the 8/3/8 box or line of sight is chased until dead |
| SeaMonster | target set / release | SeaMonster.java:522-526 — `getAttackTarget()` kept while alive, else nulled; the scan result never stored | SeaMonster.java:177-180 — the nearest player stored with `setTarget`, kept while alive | the port persists the found player until death; orig re-validated every pass |
| SeaViper | target set / release | SeaViper.java:539-543 | SeaViper.java:96 (goal) + entity/ai/SeaViperBiteGoal.java:16-31 (forget 0, DinosaurMeleeAttackGoal.java:44) | goal-set target pursued until dead PlayNicely residual carried from ENT-S-115 (wave 1): under the flag the goal keeps consuming a stored revenge target every tick, which orig's pass-local blanking stood down — the transcription target is the melee goal's tick (per-preset stand-down), ruled here. |
| Skate | target set / release | Skate.java:291-295 | Skate.java:127-130 — `setTarget`, kept while alive | release rule |
| SpitBug | target set / release | SpitBug.java:268-272 | entity/ai/SpitBugAcidAttackGoal.java:33-94 (forget 0, :71) | pursued until dead |
| TRex | target set / release | TRex.java:189-195 — `rt` released dead / 1-in-200 / out of sight / PlayNicely | entity/ai/DinosaurMeleeAttackGoal.java:34 (Presets.trex forget 0); `HurtByTargetGoal` :55 | no 1-in-200 forgiveness, no LoS re-check |
| TrooperBug | target set / release | TrooperBug.java:414-418 | entity/ai/TrooperBugLeapAttackGoal.java:19-43 (forget 0, BugMeleeAttackGoal :72) | pursued until dead |
| Urchin | target set / release | Urchin.java:272-288 — scan each pass; nothing stored | Urchin.java:156-159 — `setTarget`, kept while alive | release rule |
| MyEntityAINearestAttackableTarget (Boyfriend / Girlfriend) | target set / release | MyEntityAITarget.java:43-66 — hold beyond `targetDistance`² (20 / 15 per goal), 60-tick sight memory | `TargetGoal.canContinueToUse` with FOLLOW_RANGE 16 | hold distance 20 / 15 → 16; otherwise the same 60-tick sight memory |

- **Shapes:** (a) a goal-held target versus per-pass re-validation — 18 rows (Alosaurus, CaterKiller, CaveFisher, DungeonBeast, EmperorScorpion, HerculesBeetle, Nastysaurus, Pointysaurus, Scorpion, SeaMonster, SeaViper, Skate, SpitBug, TRex, TrooperBug, Urchin, Irukandji's dead target, Dragonfly's never-dropped prey); (b) forget rolls — Crab (missing), GiantRobot (ordering), Cryolophosaurus (port-only second roll), with EmperorScorpion's ×4 and CaterKiller's ×4 cadence inside (a); (c) a revenge-memory clear mapped onto the attack target — CreepingHorror, EntityCannonFodder, Kyuubi, LeafMonster, Rat (inert today: no goal consumes the target); (d) attacker storage — AntRobot and Crab store player attackers (orig `EntityLiving` only), Robot2's alert; (e) EnderKnight's daylight drop, EnderReaper's port-only range / unseen release (refuter correction), Hammerhead's out-of-sight skip, Peacock's dropped goal, the helper's hold distance.
- **Dependency:** the (a) rows on ENT-S-108 hunters and the T3b rebuilds close by themselves only if those rebuilds restore the orig per-pass scan (nothing stored); a class-widened goal keeps the sticky target and these rows stay open — worth ruling together with ENT-S-108's shape.
- **What a player would notice:** once a port hunter picks something it chases it to the death — through walls, out of its box, past the 1-in-N forgiveness 1.7.10 rolled — so a Scorpion, Sea Monster, Sea Viper, Skate, Spit Bug, T-Rex, Trooper Bug, Urchin, Cater Killer, Cave Fisher, Dungeon Beast, Hercules Beetle, Nastysaurus or Pointysaurus that saw a player once keeps coming; the Emperor Scorpion forgets four times faster; an Alosaurus chases an attacker it would never have picked; the Dragonfly never lets go; the Irukandji freezes on a dead target; the Crab never forgets; the Giant Robot keeps a target it just forgot for one more pass; the Cryolophosaurus drops its attacker twice as often; Robot2s call each other for help and no longer hurt each other; the Ender Knight keeps hunting at daybreak; the Ender Reaper lets a target go once it is 81 blocks off or unseen for 60 ticks (1.7.10 held it); the Hammerhead chases through lost sight; the Ant Robot and Crab store player attackers where 1.7.10 stored mobs only; five hunters forget the wrong thing (inert today).
- **Test:** tick-driven tests in a batch of their own (the `KrakenHoldReleaseTests` shape): the target moved out of the box or behind a wall → dropped on the next pass; forced rolls for the forget cadences (the `ForcedRoll` seam); `getLastHurtByMob()` versus `getTarget()` read-back for the (c) rows; a written attacker player for AntRobot / Crab.
- **Effort:** L. **Refuters:** 1 (2 wherever the fix re-shapes a goal into a per-pass scan loop).

### T6 — Exclusion / prey lists, including the shared `MyUtils.isAttackableNonMob` membership (18 rows, 15 blocks; + 1 observation)

| species | aspect | orig cite | port cite | one-line description |
|---|---|---|---|---|
| Mantis | allies / species exclusions | Mantis.java:391 — `isAttackableNonMob` (orig MyUtils.java:77-115) | EntityMantis.java:279 — port util/MyUtils.java:54-63 | grant list: the port adds EnderDragon, Kraken, Godzilla, GodzillaHead, Basilisk; drops Leon, Dragon, Spyro, the Prince / Teen / Adult / Princess, KingHead, QueenHead, PurplePower, GammaMetroid, Girlfriend, Boyfriend, Villager, Stinky (Mothra / WaterDragon excluded earlier in both) |
| Molenoid | allies / species exclusions | Molenoid.java:274 | EntityMolenoid.java:289 | the same drift; here Mothra and WaterDragon are affected too (orig grants both, the port grants neither) |
| TheKing | allies / species exclusions | TheKing.java:981 — fallthrough `isAttackableNonMob` | TheKing.java:1187 | Mothra, Leon, Dragon, Spyro, GammaMetroid, WaterDragon, Girlfriend, Boyfriend, Villager, Stinky no longer prey outside the isEnd==2 branch; unrecorded |
| TheQueen | allies / species exclusions | TheQueen.java:929 | TheQueen.java:1386 | the same ten species as the King; unrecorded |
| Crab (observation, lane M O1) | allies / species exclusions | Crab.java:417 — `isAttackableNonMob` | Crab.java:366 | the S1 row reads MATCH on the call site ("whitelist + `isAttackableNonMob` — same"); the helper's membership drift applies here too — left as written, listed so the helper fix covers all five HEAD callers |
| Dragonfly | filter order | Dragonfly.java:213-228 — whitelist EntityAnt, EntityButterfly, Cockateil, EntityMosquito, Firefly; EntityHorse when `DragonflyHorseFriendly == 0` | entity/ai/DragonflyHuntGoal.java:77-83 — `bbWidth <= 0.6`, not a Dragonfly, not a Player, the horse toggle | the whitelist replaced by a width rule; sight last |
| Dragonfly | allies / species exclusions | the same | DragonflyHuntGoal.java:80-82 | extra prey: chickens, bats, rabbits, cats, silverfish, endermites, baby animals, Cricket / Chipmunk-sized OreSpawn mobs; missing prey: horses (1.4 wide, never prey even with the toggle off) |
| Lizard | allies / species exclusions | Lizard.java:316 — AttackSquid → true | Lizard.java:135-137 — Spider, CaveSpider, Chicken only | AttackSquid (the port class exists) dropped from the prey list |
| PurplePower | allies / species exclusions | PurplePower.java:261-263 (tamed pets spared for types other than 0 / 10), :264 (`return !isRoyalty`) | PurplePower.java:199-201 — neither (port `MyUtils.isRoyalty` util :9-19 exists, not called) | exclusion list missing: royalty and tamed pets are prey |
| Rat | filter order | Rat.java:201-222 — Irukandji, Skate, Whale, Flounder, DungeonBeast (Rat, Ghost, GhostSkelly too) | EntityRat.java:196 — Rat only (Ghost / GhostSkelly covered by the ignore screen in both) | five species steps missing |
| Rat | allies / species exclusions | the same | the same | missing Irukandji, Skate, Whale, Flounder, DungeonBeast |
| TerribleTerror | allies / species exclusions | TerribleTerror.java:229-285 — 19 spared kinds | EntityTerribleTerror.java:153-161 — 10 kinds | Mothra, LurkingTerror, Bee, Mantis, LeafMonster, Triffid, Dragon, EntityButterfly, Firefly MISSING — nine species hunted that 1.7.10 spared; no record (ENT-S-112 is PitchBlack's own list) |
| Triffid | allies / species exclusions | Triffid.java:291-311 — Creeper, EnderReaper, Triffid, TerribleTerror, LurkingTerror, PitchBlack, Dragon spared; every other EntityMob prey | EntityTriffid.java:254 — `!(target instanceof Monster)` blanket | blanket Monster exclusion versus the named seven; the Dragon (a tameable, not a Monster) hunted |
| MyEntityAINearestAttackableTarget (Boyfriend / Girlfriend) | filter order | MyEntityAITarget.java:96-127 — Player only on Valentine's (:96-98), EntityPigZombie false (:99), EntityEnderman false (:102), Mothra true before sight (:105), EntityCreeper true (:111), EntityGhast true (:114), nearbyOnly path check (:117-127) | Boyfriend.java:144 / Girlfriend.java:208 — VAN-TC with no selector on the Monster goal; entity/ai/JealousyTargetGoal.java:28-29 selector = rival not tamed | PigZombie / Enderman now prey; the Mothra grant gone (an EntityButterfly in the port); the Ghast grant reversed (`canAttackType`); nearbyOnly reachability dropped (`mustReach=false`); the valentine player rule moved to `ValentineTargetGoal` |
| MyEntityAINearestAttackableTarget | allies / species exclusions (folded) | MyEntityAITarget.java:88-95 (tamed target / owner), :99-104 (PigZombie, Enderman) | owner via `TamableAnimal.canAttack`, owner's team via `isAlliedTo` | folded into the filter-order row by its part; not a separate aspect |
| MyEntityAITarget | filter order (effective) | MyEntityAITarget.java:78-129 | entity/MyEntityAITarget.java:79-106 — an unused copy (Player always false :87, ZombifiedPiglin :88, no Enderman / Mothra rules); the live replacement is VAN-TC | the effective replacement carries none of the species rules; the dead copy would drift if ever wired (T9) |
| DungeonBeast | filter order (ENT-S-108 residual) | DungeonBeast.java:216-239 — Rat, DungeonBeast, Rotator, Peacock, Irukandji, Skate, Whale, Flounder | DungeonBeast.java:70 — predicate `!isIgnoreable` only | exclusions absent (moot while players-only); its allies row is FIX IN FLIGHT — expected to close with ENT-S-108, listed so nothing is lost |
| EmperorScorpion | filter order (ENT-S-108 residual) | EmperorScorpion.java:476-493 — EntityEnderman, EnderKnight, EnderReaper, EntityCreeper, Scorpion, EmperorScorpion | EntityEmperorScorpion.java:81 | exclusions absent; ignore-before-sight versus orig sight-before-ignore (side-effect-free) |
| HerculesBeetle | filter order (ENT-S-108 residual) | HerculesBeetle.java:401-406 — EntityCreeper, HerculesBeetle | EntityHerculesBeetle.java:61 | exclusions absent |

- Cross-references, not counted here: Spyro / Stinky lose Mothra as prey because the port Mothra is an `EntityButterfly`, not a Monster (their rows are in T2); Hammerhead's, SeaMonster's, SeaViper's, WaterDragon's and CaterKiller's `isAttackableNonMob` fallthroughs return with their scans (T3a / T3b) and inherit whatever this batch rules for the helper; ENT-S-110 reproduced the orig membership inline in EntityLeon — a helper fix makes that inline copy redundant.
- **Shapes:** (i) the shared helper — one membership fix at `util/MyUtils.java:54-63` covers Crab, Mantis, Molenoid, TheKing, TheQueen (and every future restore); (ii) per-species lists — Dragonfly (2 rows), Lizard, PurplePower, Rat (2), TerribleTerror, Triffid; (iii) the Boyfriend / Girlfriend helper rules (3 rows); (iv) the three ENT-S-108 residuals.
- **What a player would notice:** the Mantis, Molenoid, King and Queen (and the Crab) no longer attack Leons, Dragons, Spyros, Gamma Metroids, Water Dragons, Girlfriends / Boyfriends, villagers, Stinkies or the royals — but do attack the Kraken, Godzilla and the Basilisk; the Dragonfly eats chickens, rabbits, cats and baby animals but never horses; the Lizard ignores Attack Squids; the Purple Power attacks the royals and tamed pets; the Rat attacks Irukandjis, Skates, Whales, Flounders and Dungeon Beasts; the Terrible Terror hunts nine of its Danger-Dimension kin; the Triffid spares every monster but hunts the Dragon; the Boyfriend / Girlfriend attack zombie pigmen and endermen, ignore Ghasts and Mothra, and chase unreachable targets.
- **Test:** the `PitchBlackAllyTests` shape — a generator row per (hunter, species): the species refused or granted as orig says, a pig and a Zombie as controls; the helper gets one generator over its 13 orig members through each HEAD caller (the ENT-S-110 `LeonTargetingTests` villager / pig pair as the template); own batch.
- **Effort:** M. **Refuters:** 1.

### T7 — PEACEFUL gates: filter guards and hunt-roll gates (6 rows, 6 blocks)

| species | aspect | orig cite | port cite | one-line description |
|---|---|---|---|---|
| AntRobot | PEACEFUL gate | AntRobot.java:105 — `owned == 0 && difficulty != PEACEFUL` gates the whole unridden block; :617, :620 — the ridden stomp and melee each require `!= PEACEFUL` | AntRobot.java:225 — `owned == 0` only; :290, :293 — ridden rolls ungated; no PEACEFUL / `getDifficulty` in the file | missing gate, three sites (an EntityLiving / Mob — no engine despawn) |
| Cephadrome | PEACEFUL gate (hunt roll) | Cephadrome.java:488 — `nextInt(7)==1 && difficulty != PEACEFUL` | Cephadrome.java:360 (`customServerAiStep`; :361 after lane L's import) — no gate | ENT-S-113 restores the :516 filter head only; the :488 cadence guard also spares a revenge target on Peaceful (lane L) |
| Dragonfly | PEACEFUL gate | Dragonfly.java:142 (caller) + :198 (filter) | none in `DragonflyHuntGoal` / `AmbientFlightGoal` | an Animal does not despawn in Peaceful — the port hunts there |
| GammaMetroid | PEACEFUL gate | GammaMetroid.java:241 (caller) + :254 (filter) | EntityGammaMetroid.java:112, :208-219 — neither | a TamableAnimal persists in Peaceful — an untamed metroid hunts there (lane L) |
| PurplePower | PEACEFUL gate | PurplePower.java:173 (call site), :180-182 (`setDead` in PEACEFUL), :236 (filter) | PurplePower.java:121 — no term; the class extends `Mob` (:29), so no Peaceful despawn either | the port hunts in Peaceful (its `setHealth(h/4-1)` at :151 bypasses difficulty scaling) |
| MyValentineTarget (Girlfriend) | PEACEFUL gate | none (MyValentineTarget.java:48-57) | Girlfriend `ValentineTargetGoal` through VAN-TC `canAttack` ((v) LivingEntity.java:899-901) | a port-only gate: players refused in Peaceful on Valentine's day |

- **What a player would notice:** on Peaceful the Ant Robot, the Dragonfly, an untamed Gamma Metroid and the Purple Power still hunt (none of them despawns); the Cephadrome still attacks whoever hurt it; the Girlfriend refuses the Valentine's-day players 1.7.10 let her take.
- **Test:** the `CephadromeGateTests` shape (difficulty flipped with `MinecraftServer.setDifficulty(PEACEFUL, true)` inside the test, asserted through `level.getDifficulty()`, restored in a finally; NORMAL asserted as the precondition); the hunt-roll sites driven once under forced rolls with a written revenge target; own batch.
- **Effort:** S. **Refuters:** 1.

### T8 — Creative-mapping remainders and other gates (17 rows, 16 blocks)

| species | aspect | orig cite | port cite | one-line description |
|---|---|---|---|---|
| CaterKiller | creative gate | CaterKiller.java:548 — `isCreativeMode` | EntityCaterKiller.java:100 — vanilla `Player.canBeSeenAsEnemy` → `invulnerable` (+ spectator) | the ENT-S-109 class through the vanilla goal; not on its list (rides with T3b) |
| CaveFisher | creative gate | CaveFisher.java:223 | CaveFisher.java:81-82, :86-87 (goals) | the ENT-S-109 class via the vanilla goal; not on its list (rides with ENT-S-108) |
| DungeonBeast | creative gate | DungeonBeast.java:242 | DungeonBeast.java:69-70 (goal; `Player.canBeSeenAsEnemy` Player.java:903-905 via TargetingConditions :73) | the ENT-S-109 class at a vanilla-goal site (rides with ENT-S-108) |
| EmperorScorpion | creative gate | EmperorScorpion.java:496 | EntityEmperorScorpion.java:80-81 | ENT-S-109 class, unlisted site (rides with ENT-S-108) |
| HerculesBeetle | creative gate | HerculesBeetle.java:409 | EntityHerculesBeetle.java:60-61 | ENT-S-109 class, unlisted site (rides with ENT-S-108) |
| Nastysaurus | creative gate | Nastysaurus.java:273 | Nastysaurus.java:72-73 — VAN-TC `Player.canBeSeenAsEnemy` ((v) Player.java:966-968) | same class, through the vanilla predicate (rides with ENT-S-108) |
| Pointysaurus | creative gate (goal path) | Pointysaurus.java:244 | Pointysaurus.java:72-73 — VAN-TC `invulnerable`; entity/ai/PointysaurusStareGoal.java:48 uses `isCreative()` | the goal path reads `invulnerable` (rides with T3c) |
| SeaViper | creative gate | SeaViper.java:519 | SeaViper.java:96 (goal) | not in ENT-S-109's list (rides with T3b) |
| SpitBug | creative gate | SpitBug.java:363 | EntitySpitBug.java:71-72 | not in ENT-S-109's list (rides with ENT-S-108) |
| TRex | creative gate | TRex.java:243 | TRex.java:58-59 | not in ENT-S-109's list (rides with ENT-S-108) |
| TrooperBug | creative gate | TrooperBug.java:503 | EntityTrooperBug.java:73-74 | not in ENT-S-109's list (rides with ENT-S-108) |
| Brutalfly | filter order (strafe) | Brutalfly.java:216-226 — a creative nearest is NULLED (:224-226) so the 1-in-3 mob hunt (:228) can run; sight :218 | EntityBrutalfly.java:205, :214 — an invulnerable / creative nearest is not nulled, `target == null` fails, the mob hunt never runs | a creative nearest player shadows the mob hunt in the port; 1.7.10 fell through to it (lane L) |
| Mothra | filter order (stage 1) | Mothra.java:225-236 — creative → `target = null`, falls through to stage 2; sight :227 | Mothra.java:393-397 — no sight check; a creative nearest leaves `target` non-null, so :397 never falls through | the stage-1 sight step (T2 shape) and the creative fall-through to the mob hunt are both missing |
| EnderKnight | creative gate | none in the pick; the legacy loop drops a creative `EntityPlayerMP` target the same tick (`td.bq` :155-182) | EnderKnight.java:47 — creative skipped inside the scan via `invulnerable` | shadowing lost (the Kraken KT-A pattern) and `invulnerable` for `isCreative` |
| EnderReaper | creative gate | the same (`td.bq` :155-182) | EnderReaper.java:50-51 | shadowing lost; `invulnerable` for `isCreative` |
| EnderKnight | filter order | EnderKnight.java:83-93 — `shouldAttackPlayer`: no pumpkin helmet (:84-87), the player's look vector within `1 − 0.025/d` of the knight's mid-height (:88-91), player-side `canEntityBeSeen` (:92); screaming set by the pick (:74, :78) | EnderKnight.java:47 — the vanilla chain only, no selector | the enderman stare gate and the pumpkin exclusion are absent — any visible player is hunted (the Reaper kept them, EnderReaper.java:60-73) |
| MyValentineTarget (Girlfriend) | creative gate | none — a creative player is valid prey on Valentine's day (MyEntityAITarget.java:96-98) | Girlfriend `ValentineTargetGoal` — VAN-TC refuses `abilities.invulnerable` players (creative and spectator) | a port-only gate |

- Cross-reference, not counted here: the AntRobot's dircheck heading test (orig :1050-1065: inside distSq 36 always, else the heading offset > 0.75 rad refuses; call sites :133 and :622) rides in its T2 row.
- **Shapes:** (i) eleven vanilla-goal sites that read `invulnerable` because the goal does — they close with the T3b / T3c / ENT-S-108 rebuilds if those restore a custom filter, otherwise each needs an `instabuild` selector; (ii) four creative-nearest shadowing / fall-through sites (Brutalfly, Mothra, EnderKnight, EnderReaper — the KT-A pattern); (iii) the Ender Knight's stare / pumpkin gate; (iv) the Girlfriend's port-only creative refusal.
- **What a player would notice:** at eleven goal-shaped hunters a survival player made invulnerable by a command is safe and a creative player with invulnerability toggled off is hunted (1.7.10 asked "creative?"); the Brutalfly and Mothra stop hunting mobs while a creative player stands nearby; the Ender Knight / Reaper no longer let a creative starer shadow a survival one; the Ender Knight attacks any visible player without the stare / pumpkin gate; the Girlfriend refuses creative players on Valentine's day.
- **Test:** the `CreativeMappingParityTests` triple per site (creative rejected / invulnerable-survival prey / survival prey) through the goal's `TargetingConditions`; the strafe / stage-1 sites driven once as the ENT-S-109 Brutalfly strafe test is (forced rolls, the parked flight target), with a creative player nearer than a survival one for the shadowing cases; the Knight's stare gate with a pumpkin-helmeted mock player; own batch.
- **Effort:** M. **Refuters:** 1.

### T9 — Port-only additions without a MOD record: candidates for MOD entries or removal (15 rows, 15 blocks)

| species | aspect | orig cite | port cite | one-line description |
|---|---|---|---|---|
| Cryolophosaurus | other | Cryolophosaurus.java:51-57 — no attack goal; the HurtBy target is never chased | Cryolophosaurus.java:57-58 — `DinosaurMeleeAttackGoal` @2 chases / bites the HurtBy target with the :150-152 dice | the accepted Dinosaur mapping (AUDIT_FINDINGS.md:171, the Alosaurus record); no Cryolophosaurus record names it; affects the retaliation chase, not who is chosen |
| DungeonBeast | PEACEFUL gate | none (EntityMob) | vanilla `LivingEntity.canAttack` :864-866 refuses players in PEACEFUL | inert — a Monster despawns in Peaceful. **Re-rated 2026-09-04, T9: MATCH (engine, P6)** — no port-written code (the engine's `canAttack`, V13); nothing to delete, never add a `canAttack` override |
| EmperorScorpion | PEACEFUL gate | none | vanilla player refusal | inert. **Re-rated 2026-09-04, T9: MATCH (engine, P6)** — no port-written code (the engine's `canAttack`, V13); nothing to delete, never add a `canAttack` override |
| EnderKnight | PEACEFUL gate | none | vanilla player refusal | inert. **Re-rated 2026-09-04, T9: MATCH (engine, P6)** — no port-written code (the engine's `canAttack`, V13); nothing to delete, never add a `canAttack` override |
| EnderReaper | PEACEFUL gate | none | vanilla player refusal | inert. **Re-rated 2026-09-04, T9: MATCH (engine, P6)** — no port-written code (the engine's `canAttack`, V13); nothing to delete, never add a `canAttack` override |
| HerculesBeetle | PEACEFUL gate | none | vanilla player refusal | inert. **Re-rated 2026-09-04, T9: MATCH (engine, P6)** — no port-written code (the engine's `canAttack`, V13); nothing to delete, never add a `canAttack` override |
| GammaMetroid | target set / release | GammaMetroid.java:241-250 — never stored; :67 revenge target inert; a tamed metroid never targets anything (:278) | EntityGammaMetroid.java:82-85 — `OwnerHurtByTargetGoal`, `OwnerHurtTargetGoal`, `HurtByTargetGoal`, `NearestAttackableTargetGoal<Monster>` when tame set `target`; no goal consumes it | port-only owner / tame target goals |
| Godzilla | allies / species exclusions | Godzilla.java:448-471 — the eight names | Godzilla.java:586-598 — the eight + Mothra (:597) + `isBigBoss` / `isRoyalty` (:598) | unrecorded ("Don't pick fights with peers", :594-596; no AUDIT / FIX / MOD entry) — Mothra, PitchBlack, the Kraken and the royals were prey in 1.7.10 |
| Leon | target set / release | Leon.java:95 — `EntityAIHurtByTarget` only; no owner goals | EntityLeon.java:155-156 — `OwnerHurtByTargetGoal`, `OwnerHurtTargetGoal`; :158 goal predicate `!isTame() \|\| getTarget()==null` | port-only owner goals and goal predicate; adjacent (out of scope): port `hurt` :404 skips the damage a tamed Leon took from a player where orig :321 applied it and only skipped the retaliation |
| Mantis | target set / release | Mantis.java — no targetTasks | EntityMantis.java:67-68 — `HurtByTargetGoal` and `NearestAttackableTargetGoal<Player>` set `Mob.target`, which nothing consumes (comment :60-66: they feed a pathfinding HUD) | inert additions |
| Pointysaurus | other | Pointysaurus.java — no stare rule | Pointysaurus.java:60-65 + entity/ai/PointysaurusStareGoal.java:32-95 — the nearest player within 32 looking at the mob, creative / spectator excluded (:48), sight (:58), locks after 5 ticks (:90-95) | eye-contact aggression — a port invention with no MOD record (AUDIT_FINDINGS.md:7031 mentions it only for its creative token) |
| Spyro | target set / release | Spyro.java:73-81 — no target tasks | EntitySpyro.java:101-104 — `OwnerHurtByTargetGoal`, `OwnerHurtTargetGoal`, `HurtByTargetGoal`, `NearestAttackableTargetGoal<>(Monster.class, 10, true, false, e -> isTame())`, unread by `doMovement` | four target goals with no orig counterpart; the custom path ignores them |
| Stinky | target set / release | Stinky.java:67-77 | EntityStinky.java:102-105 | as Spyro |
| ThePrince | target set / release | ThePrince.java:86-92 | ThePrince.java:114-117 | four target goals with no orig counterpart |
| ThePrincess | target set / release | ThePrincess.java:86-92 | ThePrincess.java:112-115 | as ThePrince |

- Noted from other rows, not counted here: ThePrinceAdult / ThePrinceTeen `OwnerHurtByTargetGoal` / `OwnerHurtTargetGoal` (:142-143 / :153-154; their scan-set rows are in T3c); the unused `entity/MyEntityAITarget.java` copy (its filter row is in T6); Robot2's `setAlertOthers` (T5); the Mothra modern-mode sweep is RECORDED (MOD-029) and needs nothing.
- **Shapes:** (i) owner / tame target goals on tameables — GammaMetroid, Leon, Spyro, Stinky, ThePrince, ThePrincess (+ Adult / Teen): today inert for the custom-scan fliers (their `doMovement` never reads `getTarget()`) but live where a melee goal consumes the target; (ii) a prey rule — Godzilla's peer exclusions; (iii) a behaviour — the Pointysaurus stare goal; (iv) a chase — the Cryolophosaurus melee goal; (v) inert vanilla PEACEFUL player refusals (five) and the Mantis's inert goals.
- **What a player would notice (if kept):** tamed Leons, Metroids, Spyros, Stinkies and the Prince / Princess defend and avenge their owner; Godzilla will not fight Mothra, the Nightmare, the Kraken or the royals; the Pointysaurus locks on to anyone who stares at it; the Cryolophosaurus chases whoever hurt it. The five PEACEFUL refusals and the Mantis goals change nothing a player sees.
- **Ruling needed per item:** a MOD record (keep as a modern option, classic off, or keep unconditionally) or removal. **Test:** none for a record; a presence assertion on the goal selector for a removal (the `IgnoreScreenParityTests` TARGET_GOAL_PREDICATE shape enumerates the selector).
- **Effort:** S (records) / M (removals). **Refuters:** 1 for a removal, none for a record.

### T10 — Anything else (5 rows, 5 hunters; + 1 observation)

| species | aspect | orig cite | port cite | one-line description |
|---|---|---|---|---|
| EnderKnight | other | EnderKnight.java:124-138 — stare-driven teleports: a staring target within distSq < 16 → `teleportRandomly`, a target > 256 away for 30 ticks → `teleportToEntity`; screaming set by the pick (:74, :78) | EnderKnight.java:75-96 — none; screaming only from `hurt` (:108) | chase behaviour, not selection; listed for the split |
| EnderReaper | other | EnderReaper.java:124-138 | EnderReaper.java:100-139 — absent; screaming only from `hurt` (:151) | ENT-D-020's "all ported" claim does not hold at HEAD for :126-131 |
| Hammerhead | other | Hammerhead.java:213 — attack dice `1-in-3 OR 1-in-4` | Hammerhead.java:140 — 1-in-3 only | attack cadence, not selection |
| Irukandji | other | Irukandji.java:258 — `1-in-4 OR 1-in-5` | Irukandji.java:145 — 1-in-4 only | attack cadence, not selection |
| Lizard | other | Lizard.java:328-330 — `Lizard && nextInt(10)==1 && follow_time<=0` → `buddy = it` inside the filter | Lizard.java:132-139 — no adoption | the 1-in-10 "adopt a nearby Lizard as buddy" side effect is gone |
| IrukandjiArrow (observation, lane M O5) | projectile hit | IrukandjiArrow.java:181 — the `instanceof EntityLiving` gate covered the push, the arrow-count increment (:185) and the ding (:190-192, dead code in 1.7.10) | IrukandjiArrow.java:88-90 (count), :100-103 (ding) — ENT-S-111 gated the push only (:92 in the working tree) | the port still bumps a player's arrow count and can send ARROW_HIT_PLAYER — not target selection; listed so it is not lost |

- **What a player would notice:** the Ender Knight and Reaper never teleport toward or away from a starer; the Hammerhead and Irukandji bite less often (one die instead of two); Lizards no longer pair up; an Irukandji arrow still counts as a player hit and dings.
- **Test:** per item, own batch (tick-driven for the teleports; forced rolls for the dice; a filter call with a Lizard candidate reading the `buddy` field back; the ENT-S-111 two-lane test extended with the arrow count and the ding).
- **Effort:** S. **Refuters:** 1.

- **Observation (2026-09-04, ENT-S-117 refuters A and B):** the Luna Moth lost orig's inherited Islands vampire hunt —
  orig EntityLunaMoth.java:117-122 calls `super.updateAITasks()` and hunted as a type-1 moth; the port's
  `EntityLunaMoth.registerGoals` (:49-50, `LunaMothFlightGoal`) drops the inherited goal. No species block exists for
  the Luna Moth; carried here as a T10 row candidate (a T3a addendum if the owner prefers).

### 4.11 Already fixed this session — not double-counted anywhere above (38 FIXED rows)

| finding | rows in this ledger |
|---|---|
| ENT-S-100 — Kraken targeting (KT-A player search, KT-B1 exclusion chain, KT-B2 `flying`, KT-D hold to removal, KT-E health base; KT-C recorded) | Kraken: scan set, filter order, creative gate, allies, ignore screen, target set / release (6) |
| ENT-S-101 — shared `isIgnoreable` membership | Alosaurus, Basilisk, Brutalfly ignore-screen rows (3); the S2 / S4 "MATCH — shared list FIXED (ENT-S-101)" ignore rows (Scorpion, SpiderRobot, TheKing, TheQueen, Vortex, Mothra, Rotator) inherit it |
| ENT-S-105 — Kraken `<=` last-equidistant-player tie-break | Kraken tie-break (1) — the same class remains open for the Brutalfly strafe (T4) |
| ENT-S-106 — the ignore screen at the 38 orig call sites | ignore-screen rows of AntRobot, CaveFisher, DungeonBeast, EmperorScorpion, GammaMetroid, GiantRobot, Godzilla, HerculesBeetle, Kyuubi, Leon, Nastysaurus, PitchBlack, Pointysaurus, PurplePower, Rat, Robot1, Robot2, Robot3, Robot4, Robot5, SpiderDriver, SpitBug, TRex, Triffid, TrooperBug, Urchin (26); the nine goal-predicate and the Urchin inline sites "cannot bite until the scans are widened" (ENT-S-108 / T3b) |
| ENT-S-107 — `instabuild` for orig `isCreativeMode` in EntityLeon and Cephadrome | Leon creative gate, Cephadrome creative gate (2) |

(ENT-S-102 / 103 / 104 are projectile findings with no targeting row.)

### 4.12 In flight — FIX IN FLIGHT rows (37), to be flipped to FIXED after the same-day gate

| finding | rows | residuals the parts flag outside the finding's text (already batched above) |
|---|---|---|
| ENT-S-108 — living-entity scans | CaveFisher scan set / filter order / allies; DungeonBeast scan set / allies; EmperorScorpion scan set / allies; HerculesBeetle scan set / allies; Nastysaurus scan set / filter order / allies; SpitBug scan set / filter order / allies; TRex scan set / filter order / allies; TrooperBug scan set / filter order / allies; Urchin scan set / filter order / allies (24) | their PlayNicely gates (T1), creative mapping through the vanilla goals (T8), tie-breaks (T4), release rules (T5), the S2 filter-order rows (T6 residuals), and the box → FOLLOW_RANGE-sphere / cadence residuals (T3b watch list) — all conditional on the fix shape ("widen each goal … or restore the orig box scan") |
| ENT-S-109 — ten `invulnerable` → `instabuild` sites | creative-gate rows of Brutalfly, Cryolophosaurus, GammaMetroid, Kyuubi, LeafMonster, LurkingTerror, Rat, TerribleTerror, Triffid (9; Brutalfly's row covers both of its sites) | lane L's observations 1–3 (§3.6) — batched in T7 / T8 / T3b |
| ENT-S-110 — untamed EntityLeon | Leon filter order, allies (2); the PlayNicely row's FIX IN FLIGHT half | the goal-registration PlayNicely gate (T1, lane M O3); the shared helper (T6, lane M O1) — ENT-S-110 inlined the orig membership; `IgnoreScreenParityTests` row 15's pig control goes red with this fix until it is a Zombie (lane M O2) |
| ENT-S-112 — PitchBlack ally exclusions | PitchBlack allies (1) | the missing sight step (T2, lane M O4) |
| ENT-S-113 — Cephadrome PEACEFUL guard + `shouldattack` reset | Cephadrome filter order (1); the PEACEFUL row's FIX IN FLIGHT half | the :488 hunt-roll guard (T7, lane L) |

ENT-S-111 (IrukandjiArrow push) has no ledger row; its residual (count + ding) is the T10 observation.

### 4.13 Numbers

No ENT-S numbers are assigned here. The owner rules on the batches T1–T10 (T3 in three parts); numbers follow the rulings, one record per batch or per batch member as the owner decides, with the refuter counts above.


## Wave log

Row statuses above are updated per wave; the §3 summary tables keep the ledger's
initial (2026-09-04, pre-wave) counts as the baseline for the batch rulings.

- Wave 1, batch T7 → ENT-S-114: 5 row(s) marked FIXED; skipped: MyValentineTarget (Girlfriend). The Cephadrome row's DIVERGES half (:488) closed by ENT-S-114; its FIX IN FLIGHT half is ENT-S-113's :516 head, landed the same day. MyValentineTarget's port-only gate goes with the T9 split.

- Wave 1, batch T1 → ENT-S-115: 44 row(s) marked FIXED. Pointysaurus :186-188 (the rt-blanking half) deferred to T5: no pass-local in the port, the retaliation pass is the shared melee goal. Seven rows were already gated at HEAD by ENT-S-108 and are pinned, not re-edited. Refuted once, two blocking defects fixed (Nastysaurus / TRex scan-pick blanking, Hammerhead fallback read).

- Wave 2, batch T3a → ENT-S-117: 19 row(s) marked FIXED. All 19 rows fixed. Observations logged: O1 the ENT-S-108 hunters' scanPick re-assert exposure (T5); O2 the Luna Moth lost orig's inherited Islands hunt (orig EntityLunaMoth.java:122 vs port LunaMothFlightGoal, registerGoals :49-50) — no ledger block exists, carried as a T10 observation; Monster-for-IMob leaves Slime / MagmaCube / Ghast / EnderDragon and orig's Mothra (implements IMob) out of the Dragon's channel (a), disclosed for the owner.

- Wave 2, batch T2 → ENT-S-118: 18 row(s) marked FIXED. All 18 rows fixed (19 sites); the split rows' other losses stay with T6 / T8 / T10. Observations filed from the refuter: ENT-S-121 (ray mode port-wide, five COLLIDER feet-helper ports), ENT-S-122 (frozen sight-cache states in 1.7.10), ENT-S-123 (the dropped flight-target canSeeTarget caller in Spyro / Stinky).

- Ruling 2026-09-04, IMob convention → ENT-S-124: the IMob → Monster clause of the T3c Leon row, the Princes' and the Boyfriend / Girlfriend block's class, and the vanilla four of the ENT-S-117 Dragon disclosure closed (Mob + Enemy at all six sites); Mothra stays disclosed; boxes / cadence / the Creeper goal stay in T3c.
- Observations (2026-09-04, ENT-S-124 refuter): (1) 1.7.10's vanilla target tasks refused Creepers as well as Ghasts (`EntityLiving.canAttackClass`), so the Dragon / Leon / Prince vanilla-goal rows carry a Creeper clause the port's goals do not — filed as ENT-S-127 for a port-wide ruling; (2) orig MyEntityAINearestAttackableTarget.java:44-52 refused whenever the Boyfriend / Girlfriend was untamed (and the Girlfriend sat); the port goal carries only the PlayNicely gate — a T3c residual on the helper block's row.
- Ruling 2026-09-04, T9 → ENT-S-125: MOD-032..035 recorded behind [modern] keys (Godzilla's boss-peer refusals, the Phase 4E companions' owner-defense goals, the Pointysaurus stare, the Cryolophosaurus chase), the Mantis's inert goals removed from both modes, MOD-036 the Girlfriend's Valentine safety gates kept in both modes as a deliberate parity exception; the five engine PEACEFUL rows re-rated MATCH (engine, P6). §3's PORT-ONLY counts (15; S2 8) are stale by five (10; S2 3) and by the rows recorded here; the §3 tables keep the pre-wave baseline by the ledger's own rule. Observation (T9 refuter): the same owner goals stay ungated on Hydrolisc / VelocityRaptor (inert) and Boyfriend / Girlfriend (live, Boyfriend.java:293 / Girlfriend.java:332) — no ledger block; a MOD-033 residual presented for a ruling.
- Observations (2026-09-04, ENT-S-121 refuter): (1) the Ender Knight's port (EnderKnight.java:51-57) dropped orig EnderKnight.java:92's stare ray entirely (no shouldAttackPlayer; the Reaper kept its stare, now routed through OreSpawnSight) — its scan-set / filter rows should carry it; (2) Chainsaw.java:137 maps orig UltimateSword.java:198-247's air-only MyCanSee voxel walk to player.hasLineOfSight — a pre-existing mapping no MOD record covers (MOD-016 is felling only); not a targeting row.


- Wave 2, batch T6 → ENT-S-128: 19 row(s) marked FIXED. 18 rows: 14 fixed by ENT-S-128, one split (the companion goal's nearbyOnly half stays with the ENT-S-124 construction), three present at HEAD by ENT-S-108 and pinned; the Crab observation closed. Corrections: the King / Queen rows listed Mothra among the lost prey, but she is refused at their ignore screen in both trees; the WaterDragon allies note's helper membership is now orig's. Deferred: the companion goal's Ghast grant (engine, ENT-S-127), its nearbyOnly half and the Mothra-before-sight placement (orig MyEntityAITarget.java:105 before :108; the ENT-S-124 construction's mustSee keeps the goal's line of sight behind the predicate); the dead entity/MyEntityAITarget.java copy (housekeeping). Observations: TheKing.java:1184 / TheQueen.java:1383 map orig EntityHorse to vanilla Horse (the Dragonfly uses AbstractHorse); the port-only BabyDragon rides under every Dragon term; orig Lizard.java:328-330's in-filter buddy adoption has no port counterpart.

- Wave 2, batch T5 → ENT-S-129: 33 row(s) marked FIXED. 33 rows: 30 fixed by ENT-S-129, Cryolophosaurus recorded (MOD-035, MATCH in classic), Peacock recorded (inert in both), Pointysaurus split (forget / PlayNicely / dead-drop fixed; the sight skip recorded — its scan is the T3c vanilla goal). One ownership convention for every mark carrier: the change-only mark with the hurt hand-off (the Water Dragon's ENT-S-117 form), chosen because the every-set clear turned a fresh pick sticky in the cleanup re-assert window, the same-entity re-assert and the PlayNicely flip (cases b, c, e); the mark retired on CaveFisher / DungeonBeast / Urchin and added on Irukandji / Skate / SeaMonster. ENT-S-122 reproduced for the ridden Ant Robot and the active Nightmare (with orig PitchBlack.java:259-280's activity-0 branch — the PitchBlack scan-set row closes with it). Six inert revenge tasks unregistered; the per-preset PlayNicely stand-down in the melee goal closes the ENT-S-115 residual for Nastysaurus, TRex, Pointysaurus and SeaViper.
- Observations (2026-09-04, ENT-S-129 refuter A): (1) the vanilla TargetGoal re-assert undoes a pass-level setTarget(null) on rows the ledger rates MATCH — Robot3.java:127, Robot4.java:175, Robot5.java:114 (1-in-50), EntityLeon.java:515 (1-in-200) and the Water Dragon's Presets.waterDragon forget 200 (orig WaterDragon.java:594-596 ended the task) — the same defect ENT-S-129 fixed on seven species; those rows should be re-rated DIVERGES and fixed with the RevengeGoal.release() shape in a follow-up; (2) orig replaced rt with every later living attacker (Nastysaurus :199-202, TRex :169-172, Pointysaurus :168-176) where the port's running revenge goal is never asked canUse again, so a second attacker is ignored until rt is released (pre-existing, vanilla's shape); (3) the Ender pair's legacy hold screens a creative player through vanilla's abilities.invulnerable, orig through isCreative — discriminating only for a hand-toggled flag; (4) the companions' "20 / 15 per goal" hold: the 20 belonged to the absent Creeper task (ENT-S-115), the 15 to the IMob goal now overridden.
