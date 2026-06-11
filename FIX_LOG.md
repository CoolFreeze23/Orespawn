# FIX_LOG — OreSpawn Port Parity Implementation

Work record for `IMPLEMENTATION_PLAN.md`. One entry per closed finding ID.
Statuses: **FIXED** / **VERIFIED-CORRECT** (audit wrong, proof cited) / **DEFERRED** (owner-approved only).

Original source: `reference_1_7_10_source/sources/danger/orespawn/` (referred to as `orig:`).
Port source: `src/main/java/danger/orespawn/` (referred to as `port:`).

---

## Pending manual tests

Fixes that can only be truly confirmed in-game. Append here in every phase; burn the
list down at the end before release.

- [ ] **BUG-003** — Place a Rat mob spawner (or `/summon orespawn:rat`), let it tick:
      server must not crash, rat must despawn normally when far away.
- [ ] **BUG-004** — Tame a Prince, push it to its growth thresholds (or downgrade
      Teen/Adult with gold ingot), then log the owner out with the chunk loaded:
      transformation must complete with ownership intact, no NPE.
- [ ] **BUG-005** — Let TheQueen reduce a Survival player to 0 HP via melee: normal
      death screen/drops/respawn. Also confirm a low-HP mob victim still vanishes
      without drops (original quirk preserved).
- [ ] **BUG-006** — Stand next to Godzilla's jump landing in Creative and Spectator:
      no damage taken; Survival players still take the shockwave.
- [ ] **B2 attribute caps** — `/summon orespawn:the_king` then `/data get entity` its
      Health: must read 7000 (not 1024). Same spot-check for TheQueen (6000) and
      Godzilla (4000).
- [ ] **B2 stats** — spot-check 3-4 reconciled mobs in-game (`/attribute ... minecraft:generic.armor base get`)
      against the table in `phase_b_reports/B2_mobstats.md`.
- [ ] **B1 drops** — kill (Survival) one of each: Kraken (ink sacs 120-279 + d53 gear,
      NO cooked cod), Godzilla (painting/beef/bone, NO emeralds), TheQueen (56× scale/
      beef/bone/flesh + Princess spawns), TheKing (royal set + 300 random registry items
      + Prince spawns), Mothra (20 moths burst), Dragon (beef 1-6, no bones). Confirm
      nothing drops twice.
- [ ] **B3 riding** — mount and fly/ride each: Dragon (no rubber-banding — BUG-020),
      Leon, Leonopteryx, Cephadrome (fed first), Ostrich (FAST jump on UP key),
      ThePrinceTeen + Adult (tamed; strafe keys fire the canon trio). Verify a second
      player observing sees smooth movement.
- [ ] **B3 SpiderDriver** — armor 8 mounted / 20 on foot; attacks (with poison) while
      mounted on the SpiderRobot.
- [ ] **B4 animations** — stand still near Bee/Mothra/Urchin/Kyuubi etc.: idle
      animations must keep moving (no frozen wings); Mothra renders at 10× scale and
      flaps slowly (0.2 frequency).
- [ ] **ENT-A-002/012/017/023/040/060/072/078/091/106** — eyeball hitboxes (F3+B) of Alien/AntRobot/AttackSquid/BandP/Bee/Brutalfly/CaterKiller/CaveFisher/CloudShark/CreepingHorror against the sizes in `phase_c_reports/C1_entities_A_C.md`; CaterKiller must halve with `playNicely=true`.
- [ ] **ENT-A-031/060** — drop Basilisk and Brutalfly into lava: no fire damage.
- [ ] **ENT-A-100/101** — spawn many Crabs naturally: sizes vary (¼/½/full, occasional giants); spawner Crabs all small (0.35).
- [ ] **ENT-A-102/103** — Crab walks toward water, takes dry-out damage away from it, plays scorpion sounds on melee and a splash when healing in water.
- [ ] **ENT-A-004** — Alien melee applies Poison (40t on Easy, 30t otherwise), not Hunger.
- [ ] **ENT-A-013/014** — AntRobot melee noticeably throttled; while ridden it occasionally stomps nearby mobs for ~3.0.
- [ ] **ENT-A-025** — BandP steals an item on every successful hit (armor first).
- [ ] **ENT-A-036** — Basilisk uses custom living/hurt/death sounds (no Ravager).
- [ ] **ENT-A-045-051** — Big Bertha/Royal/Hammy: swing projectiles one-shot in range (496/746/82), respect `bigBerthaPvp` for players/tamed; Girlfriend/Boyfriend never hit.
- [ ] **ENT-A-054** — Boyfriend follows cooked beef, panics when hit, opens doors.
- [ ] **ENT-A-074/075** — damaged CaterKiller transforms after ~2 min into Brutalfly + 10 Butterflies (explosion sound); eats nearby leaves/logs (heal 2.0, occasional burp).
- [ ] **ENT-A-080** — CaveFisher hunts nearby passive animals, not just players.
- [ ] **ENT-A-082** — Cephadrome attacks Mothra/untamed Leon/GammaMetroid/WaterDragon; Kraken takes 1.5× hits.
- [ ] **ENT-A-087** — Chipmunk tames with apple (50%), releases with dead bush.
- [ ] **ENT-A-095/097** — Cockateil spawns with random bird type; only type 5 birds can drop rubies (player kill, 1-in-3).
- [ ] **ENT-A-110** — CreepingHorror only spawns naturally in darkness at night below y=15 (or in Chaos).
- [ ] **ENT-A-112** — Cryolophosaurus proactively chases nearby prey, not only retaliating.
- [ ] **C1 loot** — kill (Survival): Alien (spider eyes/flint/map/clock/compass), AntRobot (redstone jackpot), Beaver (0–2 porkchop), tamed Camarasaurus (2–6 poppies; untamed none), CaveFisher/CliffRacer/CloudShark/Cryolophosaurus/CreepingHorror (gamble drops), Coin (10-slot jackpot).
- [ ] **C1 spawns** — verify Alien/Alosaurus/Camarasaurus/Baryonyx spawn in the Mining dim (and Alosaurus/Baryonyx in Utopia), no longer in the End/overworld; Boyfriend beach hotspots; Bee/CaterKiller/Basilisk/Brutalfly/BandP in their per-biome lists.
- [ ] **ENT-D-002/006** — Dragon tames/heals with raw beef (1-in-5), ignores bones; diamond on a tamed dragon spawns a tamed Spyro and removes the adult.
- [ ] **ENT-D-012** — ridden Hoverboard hums `orespawn:hover` (randomized 1–6 variants), not the beacon tone.
- [ ] **ENT-D-014** — Emperor Scorpion in combat occasionally spawns a baby scorpion midway to its target (no cap — can flood).
- [ ] **ENT-D-022** — Cage: players bounce back an empty cage; Creeper always cages; Ghast/Enderman escape ~20%, Kraken ~95%; Bat gives 2 caged bats, Cockateil 4, AttackSquid 6; villager-hit consumes the cage and returns it; Iron Golem cageable; tamed Girlfriend/Boyfriend eat the cage with no drop.
- [ ] **ENT-D-025/026/027** — thrown rocks: t5 deals 10; t6/9–12 apply Weakness (not Wither); t9 ignites ~50s; block impacts shatter glass in 3×3×3 with the glassdead sound and return the same rock type; entity hits return nothing.
- [ ] **ENT-D-037** — tamed Gazelle drops 2–6 poppies; untamed drops 0–2 raw beef.
- [ ] **C2 loot** — kill (Survival): DungeonBeast (25% each crystal pink ingot/crystal apple/oak log/nothing ×0–2), Fairy (crystal torch), Firefly (extreme torch), GammaMetroid (5–14 gold nuggets + 6–15 iron), Ghost/GhostSkelly (nothing), GiantRobot (~60–116 laser balls + 10–19 kit/component rolls incl. detector rails).
- [ ] **C2 spawns** — Dragon/GoldFish/EnderReaper on the Island biome; DungeonBeast/Flounder/Irukandji/Frog in Crystal; EnderKnight/EnderReaper/Hammerhead/GammaMetroid/DungeonBeast in Chaos; GammaMetroid swarms in Mining; EnderKnight/EnderReaper dark-forest hotspots (w20/w38) and NONE in the End; Fairy only in dark forests overworld; Girlfriend beach hotspot (8–15 groups); Hydrolisc swamp/jungle; Frog river/swamp; ghosts in snowy taiga/taiga/frozen river/jungle/dark forest; no more ocean spawns for Flounder/GoldFish/Irukandji/Hammerhead/Hydrolisc.
- [ ] **ENT-K-005** — Kraken plays its custom living growl (1-in-5 ambient) and alo_death on death, not Elder Guardian sounds.
- [ ] **ENT-K-013** — LeafMonster attacks Ants/Butterflies/LunaMoths and players only (ignores other small mobs); never hunts with `playNicely=true`.
- [ ] **ENT-K-019/083** — Leon and RubberDucky untame with a dead bush (Leon glass no longer works); RubberDucky tames/tempts with raw cod, not wheat.
- [ ] **ENT-K-023** — Lizard out of water periodically pathfinds to the nearest water (never to lava/fire).
- [ ] **ENT-K-033** — MantisClaw hits drain 1 HP silently (no extra hurt flash) and heal the wielder 1.
- [ ] **ENT-K-045** — Ostrich takes no cactus damage but normal damage otherwise.
- [ ] **ENT-K-046** — tamed Ostrich drops 2–6 poppies; untamed drops 0–2 feathers.
- [ ] **ENT-K-050** — Peacock breeds with Crystal Apple only.
- [ ] **ENT-K-051** — Nightmares spawn mostly tiny (t=0.5); big ones rare (t=4 ≈ 1.5%); hitbox/model grow together up to 10×14 blocks; `nightmareSize=5` forces max size.
- [ ] **ENT-K-056** — PurplePower type 2 poisons, type 3 weakens (2.5 s each).
- [ ] **ENT-K-058** — wild rats attack players/pets even with default configs; a rat with an owner never attacks its owner (and respects ratPlayerFriendly/ratPetFriendly).
- [ ] **C3 loot** — kill (Survival): LeafMonster (log OR leaves OR rotten flesh), LurkingTerror (beef/flint/feather), Rat (rotten flesh), Robot2 (2–9 iron blocks + 5–10 iron ingots + redstone parts), Robot3/5 (20–40 laser balls + redstone parts), Robot4 (20–56 laser balls + RayGun + painting + redstone parts), Rotator (one of crystal pink ingot/tigers eye ingot/crystal coal/iron).
- [ ] **C3 spawns** — Kraken/Leon/Leonopteryx/RubyBird never spawn naturally (spawner-block/dungeon only); Kyuubi Nether weight doubled; Lizard only river/swamp/ocean; rat swarms in dark forests (10–20 packs) and taigas, not everywhere.

---

## Phase A — Critical & High bugs (closed 2026-06-11)

Build verification: `./gradlew build` → BUILD SUCCESSFUL (NeoForge 21.1.223, Java 21).

### BUG-001 — FIXED (CRITICAL)
- **Files:** `src/main/java/de/dertoaster/multihitboxlib/EntityEventHandler.java`
- **Change:** Removed the explicit `bus = EventBusSubscriber.Bus.MOD` from `@EventBusSubscriber`. `EntityEvent.Size` / `PlayerEvent.StartTracking` / `StopTracking` are GAME-bus events; registration now uses NeoForge 21.1's per-listener bus auto-detection (the `bus` attribute is deprecated-for-removal on 21.1.223).
- **Repro note:** Before — mod construction threw `IllegalArgumentException` registering game-bus event listeners on the mod bus (launch crash). After — listeners are routed to `NeoForge.EVENT_BUS`; cannot recur because no bus is forced.

### BUG-002 — FIXED (CRITICAL)
- **Files:** `src/main/java/de/dertoaster/multihitboxlib/GameEventHandler.java`
- **Change:** Same as BUG-001 for `PlayerEvent.PlayerLoggedInEvent` (GAME bus). Asset-synch enforcement now actually fires on login.

### BUG-001-family (found during fix, not in audit) — FIXED
- **Files:** `src/main/java/de/dertoaster/multihitboxlib/ModEventHandler.java`, `api/event/server/AssetEnforcementManagerRegistrationEvent.java`, `api/event/server/SynchAssetFinderRegistrationEvent.java`, `init/MHLibNetwork.java`
- **Change:** MHLib's two custom registration events implemented `IModBusEvent` but are posted on `NeoForge.EVENT_BUS` (`AssetEnforcement.java:46,67`) — the game bus rejects mod-bus event types at post time, so `AssetEnforcement.init()` would throw during common setup, and `ModEventHandler`'s MOD-bus listeners could never receive them. Removed the `IModBusEvent` marker from both events and let `ModEventHandler`/`MHLibNetwork` use bus auto-detection.
- **Verification:** `AssetEnforcement.initializeManagers/initializeAssetFinders` post on the game bus; listeners now resolve to the same bus; compile clean.

### BUG-003 — FIXED (CRITICAL)
- **Files:** `src/main/java/danger/orespawn/entity/EntityRat.java`
- **Change:** Replaced the `String myOwner` field with `@Nullable UUID ownerUuid`. NBT now writes `putUUID("MyOwner")` only when an owner exists; reads accept the UUID form, parse the legacy string form defensively (empty/`"null"`/malformed → no owner), and `customServerAiStep` no longer calls `UUID.fromString` at all.
- **Repro note:** Before — a rat from a Crystal-dungeon spawner deserialized with no `MyOwner` tag, `getString` returned `""`, and `UUID.fromString("")` crashed the server on its first AI tick. After — the parse site is gone; legacy strings are handled in try/catch on load only.
- **Parity:** Owner-following/teleport thresholds (64.0 / 256.0 distSqr) untouched.

### BUG-004 — FIXED (CRITICAL)
- **Files:** `src/main/java/danger/orespawn/entity/ThePrince.java` (transformToTeen), `ThePrinceTeen.java` (transformToAdult + gold-ingot downgrade), `ThePrinceAdult.java` (gold-ingot downgrade) — all 4 audited sites
- **Change:** `getPlayerByUUID` result is null-checked; when the owner is offline the new life-stage entity gets `setOwnerUUID(this.getOwnerUUID()); setTame(true, true)` instead of `tame(null)`.
- **Repro note:** Before — `TamableAnimal.tame(null)` NPE'd the server the moment a chunk-loaded prince hit its growth thresholds with its owner logged out. After — ownership transfers by UUID without resolving the player.

### BUG-005 — FIXED (CRITICAL)
- **Files:** `src/main/java/danger/orespawn/entity/TheQueen.java` (both discard sites: `doHurtTarget` and `customServerAiStep`, now `finishTrackedVictim()`)
- **Change:** When the health-tracked victim reaches 0 HP: players receive a lethal `hurt(mobAttack(this), Float.MAX_VALUE)` (full death pipeline, kill attributed to the Queen); non-player mobs keep the original `discard()` quirk.
- **Original:** `orig TheQueen.java:260-261, 340-341` (`func_70106_y()` on any victim). Player-deletion side replicated a 1.7.10 defect — deviation recorded in PARITY_NOTES.md, modernization entry MOD-001.
- **Repro note:** Before — a player "killed" via the tracked-HP path was removed without death screen/drops/respawn (ghost connection). After — players die normally; mobs behave exactly as the original.

### BUG-006 — FIXED (CRITICAL)
- **Files:** `src/main/java/danger/orespawn/entity/Godzilla.java` (`doJumpDamage`)
- **Change:** `mobAttack + genericKill` halves replaced with `explosion(null, null)` + `fall()` halves.
- **Verification vs original:** `orig Godzilla.java:509-512` — half damage via `DamageSource.func_94539_a(null)` (unattributed explosion), half via `DamageSource.field_76379_h` (fall). The port now matches source-for-source; `genericKill` (the `/kill` source) bypassed Creative/Spectator invulnerability, which the original never did.

### BUG-007 — FIXED (CRITICAL)
- **Files:** `src/main/java/danger/orespawn/entity/SpiderRobot.java`
- **Change:** Deleted the empty `addAdditionalSaveData`/`readAdditionalSaveData` overrides (no extra fields to persist → inherited behavior is correct); removed the now-unused `CompoundTag` import.
- **Repro note:** Before — Health/effects/PersistenceRequired/equipment never persisted; half-killed robots reloaded at full HP and name-tagged ones could despawn. After — vanilla `LivingEntity` persistence applies.

### BUG-008 — VERIFIED-CORRECT (HIGH)
- **Proof the audit was wrong:** `port EntityWormLarge.java:199-208` already persists `wormsSpawned` (`tag.putInt("wormsSpawned", ...)` in `addAdditionalSaveData`, `tag.getInt` in `readAdditionalSaveData`). The 40-worm brood spawn at `:133-147` is gated on `wormsSpawned == 0`, which survives reload. No change made.

### BUG-009 — FIXED (HIGH)
- **Files:** `src/main/java/danger/orespawn/ModSpawnControl.java`
- **Change:** `NATURAL_SPAWNS` wrapped in `Collections.synchronizedSet(...)`.
- **Repro note:** Before — `FinalizeSpawnEvent` mutated the WeakHashMap-backed set from chunk-gen worker threads while `EntityJoinLevelEvent` mutated it on the server thread; a concurrent rehash can corrupt the map (infinite `getEntry` loop) or throw CME. After — all access serialized; the two handlers only do single `add`/`remove` calls, so no compound-operation races remain.

### BUG-010 — FIXED (HIGH)
- **Files:** `src/main/java/danger/orespawn/entity/ThePrince.java`, `ThePrincess.java`
- **Change:** (1) Restored the original's activity cycling (`orig ThePrince.java:529-539`, `orig ThePrincess.java:629-639`): while not sitting, activity 0 → 1, and a 1/100-per-tick roll re-picks flying (1/20) vs landed (19/20) — this is the original's only path back from activity 2, and it was missing. (2) Disabled the activity-2 → `noPhysics` mapping until the original's flight movement (`do_movement`) is ported (Phase D), since noPhysics without flight control sank hurt princes through terrain into the void.
- **Repro note:** Before — one hit set activity 2 permanently (persisted via `SpyroActivity`), entity fell through the world. After — activity recovers within ~5s on average and noPhysics is never enabled. Temporary deviation logged in PARITY_NOTES.md pending flight restoration.

### BUG-011 — FIXED (HIGH)
- **Files:** `src/main/java/danger/orespawn/entity/Kraken.java` (`handleCaughtEntity`)
- **Change:** Caught `ServerPlayer`s are now moved with `connection.teleport(...)` (kraken yaw kept for parity with the original's forced rotation) and `hurtMarked = true` so the forced motion syncs; non-player victims keep raw `setPos`/`setYRot` as before.
- **Repro note:** Before — per-tick server-side `setPos` on a client-authoritative player caused rubber-banding and "moved wrongly" kicks. After — position updates go through the movement-check-exempt teleport path.

### BUG-012 — VERIFIED-CORRECT (HIGH)
- **Proof the audit's premise is original behavior:** `orig TheKing.java:824-826` — `if (e instanceof EntityMob && s < 3.0f) { e.setDead(); return false; }`. The port (`port TheKing.java:951-953`, `Monster` + bbWidth×bbHeight < 3.0 + `discard()`) matches it exactly; players can never be `Monster`, so no player is at risk. Faithful port retained per ground rule 2; logged as MOD-002 (ORIGINAL-BUG) for the modernization pass.

### BUG-013 — FIXED (HIGH)
- **Files:** `src/main/java/danger/orespawn/world/OreSpawnChunkGenerator.java`, `world/CrystalStructures.java`, `world/package-info.java`
- **Change:** Both 50-chunk anti-clustering cooldowns converted from `static` to per-generator-instance `AtomicInteger` fields (`dungeonPlacementCooldown`, `crystalStructureCooldown`). `CrystalStructures.generate` now receives the cooldown as a parameter, and the six `set(50)` calls were hoisted from the structure helpers (each had exactly one success path) to the call sites — semantics within a single dimension unchanged.
- **Repro note:** Before — one static counter shared across every dimension instance meant a Mining-dim dungeon suppressed Utopia/Crystal placements. After — each dimension's generator cools down independently; atomics keep it safe across parallel worldgen threads. Residual ordering nondeterminism inherent to any chunk-order cooldown is unchanged (matches the original's design); full determinism would alter placement behavior and is deferred to the OPT review (Phase F).

---

## Phase B — Carried forward (owners for every PARTIAL)

All 21 Phase B PARTIALs have a designated closing phase; none is unowned.

| ID | Remainder | Closes in |
|---|---|---|
| ENT-A-002 (Alien) | hitbox 1.1×3.25 | Phase C — entities |
| ENT-A-012 (AntRobot) | hitbox 2.75×1.25 | Phase C — entities |
| ENT-A-017 (AttackSquid) | dimensions 1.0×1.25 | Phase C — entities |
| ENT-A-023 (BandP) | size 0.75×1.75 + worn-gear armor clamp 8–23 | Phase C — entities |
| ENT-A-031 (Basilisk) | fire immunity | Phase C — entities |
| ENT-A-040 (Bee) | size 1.5×2.5 | Phase C — entities |
| ENT-A-060 (Brutalfly) | size 5.0×2.0 + fire immunity | Phase C — entities |
| ENT-A-072 (CaterKiller) | size 2.9×4.6 | Phase C — entities |
| ENT-A-078 (CaveFisher) | size 1.35×0.75 | Phase C — entities |
| ENT-A-091 (CloudShark) | size 1.0×0.75 | Phase C — entities |
| ENT-A-100 (Crab) | scale-driven size 3.75×3.5×scale | Phase C — entities |
| ENT-A-103 (Crab) | attack/splash sounds | Phase C — entities |
| ENT-A-106 (CreepingHorror) | size 0.75×0.5 | Phase C — entities |
| ENT-K-051 (PitchBlack) | continuous scale model (vs discrete tiers) | Phase C — entities (renderer follow-through) |
| ENT-K-068 (Robot4) | difficulty-scaled melee 15/20/25 | Phase C — entities |
| ENT-S-006 (SeaMonster) | water speed-boost dead code (0.55 in water) | Phase C — entities |
| ENT-S-061 (Urchin) | fire immunity | Phase C — entities |
| ENT-S-068 (Vortex) | fire immunity | Phase C — entities |
| BOSS-006 (TheQueen) | +2/+3/+5 phase armor scaling | Phase C — bosses |
| BOSS-026 (ThePrinceTeen) | size 3.25×4.25 | Phase C — bosses |
| ANIM-012 (rider controls) | Elevator riding (entity port) | Phase D — missing features (rider elevator) |

---

## Phase B — Systemic issues (closed 2026-06-11)

Build verification: `./gradlew build` → BUILD SUCCESSFUL (all four streams merged).
Detailed per-value / per-entity citation tables live in `phase_b_reports/` —
each is the authoritative record for its stream; this log summarizes and indexes them.

### B1 — Double-drop architectural consolidation — FIXED (ENT-SYS-001, ENT-SYS2-001 + 36 per-entity drop findings)
- **Report:** `phase_b_reports/B1_drops.md` (per-entity original lists, citations, divergences removed) plus the Leon/Cephadrome rows in `phase_b_reports/B3_riders.md` §Task 7.
- **Architecture (uniform):** the loot-table JSON is the single source of truth for item death-drops; every duplicating `dropCustomDeathLoot` override was deleted and each JSON rewritten to the exact original 1.7.10 drop list (40 entities + Kraken/Godzilla/TheQueen/TheKing; Dragon's leftover override removed in integration). Non-item death behavior (Mothra's 20-moth burst, Brutalfly's/CaterKiller's butterfly bursts, Queen→Princess and King→Prince spawns) moved to `die()` with citations.
- **Exceptions (justified individually in the report):** TheKing keeps the code path for its 150+150 random item/block-registry draws (inexpressible in JSON; duplicated JSON pools emptied instead); BandP keeps code for its dynamic stolen-item stash and its variant-gated nugget drop (loot conditions cannot read either); enchanted gear everywhere uses one `minecraft:enchant_randomly` per item in place of the original's 0–7 independent per-enchantment dice (uniform documented approximation — PARITY_NOTES PN-005).
- **Notable parity recoveries:** Kraken 120–279 ink sacs (port had cooked cod!), Godzilla beef/bone/painting (port had emeralds/xp bottles/nether star), Queen's 56× pools, Hammerhead's unique reward items, full d53/d80 boss gear tables.
- **Audit corrections:** ENT-K-043 VERIFIED-CORRECT; ENT-K-055's claimed original list was wrong (real orig: 10 leather + 6 beef + 6 rotten flesh + 6 string — port follows the verified source).
- **MISSING-ITEM:** `MyHammy` (orig Hammerhead 1-in-3 drop) is not registered in the port; omitted, no substitute invented — backlog for Phase D.

### B2 — MobStats reconciliation — FIXED (ENT-SYS2-002 + 65 per-entity stat findings)
- **Report:** `phase_b_reports/B2_mobstats.md` — one row per stat value (HP/ATK/ARMOR/SPEED/XP), each with its own orig citation; no batch assertions.
- **Change:** `MobStats.java` rewritten as the live single source of truth: 59 constants copied exactly from `orig OreSpawnMain.java:6466-6525`, each Javadoc-cited; every table entity's `createAttributes()` now reads from it (ARMOR added wherever the original had defense). Entity-side original overrides honored and documented (Leon hardcodes 250/55/16; Crab reads PitchBlack health — original bug preserved; King/Queen/Godzilla armor boosts kept on corrected bases). Hardcoded melee floats (SpiderRobot 50f, AntRobot 35f, Crab) replaced with the ATTACK_DAMAGE attribute. xpReward reconciled where the original sets experienceValue. Original config clamping (orig `:6066-6096`) deferred to the config findings (Phase E).
- **Critical discovery fixed (not in audit):** vanilla 1.21.1 clamps MAX_HEALTH to 1024 / ATTACK_DAMAGE to 2048, so every big boss silently ran at 1024 HP. Fixed via AT `public-f ... RangedAttribute maxValue` + cap raise to 100000 in the mod constructor (`OreSpawnMod.java`).
- **Audit corrections:** ENT-D-024 (RedAnt) and ENT-D-060 (Hydrolisc HP/speed) VERIFIED-CORRECT with orig citations; Hydrolisc's missing ARMOR 10 (which the audit missed) added.

### B3 — Rider flight + mounted attacks — FIXED (ENT-K-017, ENT-K-044, BOSS-027, BOSS-033, BUG-020, ENT-S-017/018/019)
- **Report:** `phase_b_reports/B3_riders.md` (per-mount original physics constants, all cited).
- **Change:** new shared `entity/ai/RiderFlightController.java` — a line-by-line port of the original's hand-rolled ridden physics (hover, terrain-follow, yaw lag, fly-up/FAST-jump, smoothed throttle, friction), parameterized per mount with citation-carrying `Config` records. Mounts now use the vanilla-horse client-predicted pattern (`getControllingPassenger` + `tickRidden` + `travel` guard): Dragon (refactored off server-side movement — closes BUG-020), Leon + Leonopteryx, Cephadrome, Ostrich (runner: FAST jump `+1.0 + v*6.0`, 20-tick latch), ThePrinceTeen/Adult (saddle-free mounting + strafe-key canon trio per the originals). SpiderDriver: 8-mounted/20-on-foot armor via stable AttributeModifier, and the mounted branch now steers the robot and actually attacks (melee + Poison 60t, 1-in-2, 16t cooldown).
- **Invention removed:** Dragon's G-key big-fireball volley had no original counterpart — riderSpecial is now a no-op; Dragon's real ridden projectiles are strafe-key driven, ported per `orig Dragon.java:1060-1161`.
- **Noted, not done here:** Elevator exists in the port but keeps the generic ±0.15 Δy fallback (proper port = Phase D backlog).

### B4 — Animation frequency mistranslation — FIXED (systemic `wingspeed → limbSwingAmount` finding, 08_animations)
- **Report:** `phase_b_reports/B4_animations.md` (per-file expression counts + wingspeed sources).
- **Change:** 342 trig-frequency expressions across 41 model files restored from `limbSwingAmount` (runtime movement → frozen idle animations) to the original constructor-constant `wingspeed`, every coefficient preserved and every constant cited from `orig ClientProxyOreSpawn.java`; follow-up pass corrected 12 more models that had the right structure but a wrong 1.0 constant (Basilisk 0.3, Cassowary 0.55, Princes 0.65, …). `ButterflyModel` takes wingspeed per entity (Butterfly 1.0 / LunaMoth 0.75 / Mothra 0.2). Urchin's double-applied amplitude fixed to orig (frequency×wingspeed, amplitude×f1). Mothra render scale 5.0 → 10.0 and LunaMoth scale 1.5 restored (orig proxy:407,411).

---

## Phase C — Category fixes (2026-06-11)

### Phase C slice 1 — entities A–C
- **Report:** `phase_c_reports/C1_entities_A_C.md` (per-finding orig citation / old / new value tables).
- **Scope & outcome:** 61 ENT-A findings (48 open DIVERGENT + 13 carried-forward PARTIAL remainders): **46 FIXED, 3 VERIFIED-CORRECT, 12 PARTIAL** (remainders all named with owners — mostly ENT-SYS-002 spawn-rule gates and unported entities/items for Phase D). Hitboxes and fire immunity restored in `ModEntities` for 12 entities (incl. Crab's scale-driven dims and CaterKiller's PlayNicely halving); 11 loot JSONs rewritten to the original drop lists; Bertha/BerthaHit damage (496/746/82), per-type ranges (81/101/64) and the `bigBerthaPvp` gates restored incl. the original operator-precedence quirk; behavior parity restored for Alien (Poison), AntRobot (melee throttle + ridden stomp), BandP (steal-every-hit, 100-slot stash), Basilisk (bite effects + custom sounds), Boyfriend (cooked-beef tempt, panic, door-opening), Camarasaurus (diet + tamed poppies), CaterKiller (metamorphosis + tree-eat heal, inventions removed), CaveFisher (passive-mob predation), Cephadrome (target list + EnderDragon/Kraken handling), Chipmunk (apple/dead-bush), Cockateil (bird-type randomization), Crab (scale dice + water ecology + sounds), CreepingHorror (spawn rules), Cryolophosaurus (proactive hunting), CrystalCow (RedCow lineage), LaserBall (immunity list). Spawn biome modifiers rebuilt against the original registrations for Alien/Alosaurus/BandP/Baryonyx/Basilisk/Bee/Boyfriend/Brutalfly/Camarasaurus/CaterKiller, incl. a new Mining-dimension spawn list. Audit errors corrected with proof: ENT-A-004/005 (Poison + real drop items), 006/011 (Mining-dim spawn lists, no overworld addSpawn), 023 (no armor clamp), 026 (emeralds), 035/064 (real biome lists), 042 (Poison), 045 (tier values already correct), 100 (live crab width 2.5).
- **Finding IDs:** 001, 002, 004, 005, 006, 011, 012, 013, 014, 015, 017, 023, 025, 026, 027, 029, 031, 033, 035, 036, 037, 040, 042, 044, 045, 047, 048, 049, 051, 054, 057, 060, 064, 068, 070, 071, 072, 074, 075, 077, 078, 080, 081, 082, 087, 090, 091, 093, 095, 097, 098, 100, 101, 102, 103, 106, 109, 110, 112, 113, 114 (all ENT-A).
- **Build:** `.\gradlew.bat build` → BUILD SUCCESSFUL.

### Phase C slice 2 — entities D–I
- **Report:** `phase_c_reports/C2_entities_D_I.md` (per-finding orig citation / old / new value tables).
- **Scope & outcome:** 34 open DIVERGENT ENT-D findings: **18 FIXED, 1 VERIFIED-CORRECT, 15 PARTIAL** (remainders → ENT-SYS-002 spawn-rule/date gates, Phase D). Dragon re-tamed/healed with raw beef and the diamond→Spyro rebirth restored (audit's "Magic Apple" was wrong); EntityCage's ~100-species whitelist rebuilt with per-species escape dice, multi-count drops (Bat/Silverfish/Dragonfly ×2, Cockateil ×4, AttackSquid ×6) and the unlisted-mob/tamed-GF-BF/player branches; EmperorScorpion's invented summon timer replaced by the orig 1-in-80 midpoint dice; ThrownRock t5 damage, Weakness-not-Wither effects, 50s ignite, 12-type rock recovery and 3×3×3 glass-breaking (new combined `glassdead` + `hover` sound events, the latter fixing the Hoverboard's beacon hum); Hammerhead boss bar removed. Drops rewritten to the originals for DungeonBeast (crystal ingot/apple/oak-log/nothing), Fairy (crystal torch), Firefly (extreme torch), GammaMetroid (gold nuggets), Gazelle (tamed-poppies/untamed-beef via new `OreSpawnTamed` NBT flag), Ghost/GhostSkelly (nothing), GiantRobot (60–116 laser balls + 10-item kit pool incl. detector rail — audit's "piston-head" was wrong). Spawns rebuilt against the original registrations for Dragon/DungeonBeast/EasterBunny/EnderKnight/EnderReaper/Fairy/Flounder/Frog/GammaMetroid/Ghost/GhostSkelly/Girlfriend/GoldFish/Hammerhead/Hydrolisc/Irukandji — invented End/Nether/ocean/cave habitats removed, Utopia-sub-biome (Island/Crystal/Chaos) and Mining-dim lists populated, Girlfriend's 12-entry per-biome map (beach w30 8–15 hotspot) restored. Audit errors corrected with proof: ENT-D-006 (diamond, not apple), 014 (no population condition), 027 (orig has no mobGriefing gate), 036 (Mining dim, not Crystal/Nether), 037 (poppies only when tamed), 045 (detector rail), 057/063 (Chaos/Crystal sub-biome lists), 039/041 (the w15-3–6 22-biome block is Halloween-only).
- **Finding IDs:** ENT-D-002, 003, 004, 006, 008, 009, 011, 012, 014, 018, 021, 022, 025, 026, 027, 029, 030, 031, 032, 033, 035, 036, 037, 038, 039, 040, 041, 045, 050, 053, 055, 057, 061, 063.
- **Build:** `.\gradlew.bat build` → BUILD SUCCESSFUL.

### Phase C slice 3 — entities K–R
- **Report:** `phase_c_reports/C3_entities_K_R.md` (per-finding orig citation / old / new value tables).
- **Scope & outcome:** 26 open DIVERGENT ENT-K findings + 2 carried-forward PARTIAL remainders (051, 068): **25 FIXED, 2 VERIFIED-CORRECT, 1 PARTIAL** (Rat spawn gates → ENT-SYS-002, Phase D). Kraken's custom kraken_living/alo_death sounds wired and its invented natural ocean spawn removed (orig is spawner/summon-only); Kyuubi Nether weight 5→10; LeafMonster prey restored to the Ant/Butterfly/LunaMoth/player allow-list (PlayNicely-gated) and its drops to the orig log/leaves/rotten-flesh one-of; Leon untames with dead bush (audit's "carrot tame" was wrong — beef was already correct) and all three invented Leon/Leonopteryx natural spawns removed (dungeon spawners are the orig path); Lizard seeks WATER (not fire — and not lava either, audit half-wrong) and spawns river/swamp/ocean w5/w4/w2 2-4; LurkingTerror drops beef/flint/feather; MantisClaw's lifesteal restored to the orig silent heal(-1)/heal(+1) drain (no potion effects, no invuln frames); Ostrich verified cactus-only-immune (audit misread the inversion) and its tamed-poppy/untamed-feather drop split restored; Peacock breeds with Crystal Apple (not wheat); PitchBlack's scale model restored to the orig five discrete t ∈ {0.5,1,2,3,4} with the cascading 1/4-1/8-1/32-1/64 dice, 2.5t×3.5t hitbox and NightmareSize forcing (audit's "continuous scale" was wrong, and the flight formula 0.5+t/10 was already the orig's); PurplePower type-2/type-3 effects corrected to Poison/Weakness; Rat configs rescoped to OWNED rats only (orig defaults are true — audit's flip-to-false fix would have diverged), drop corrected to rotten flesh, and swarm spawns rebuilt (dark_forest w35 10-20 + taiga w25 2-8); Robot2-5 loot rebuilt from the originals (iron blocks/ingots, 20-56 laser balls each, the shared d15 redstone-component table, Robot4's RayGun + painting); Robot4's "15/20/25 difficulty melee" proven dead code (never called + internally bugged) — melee is the attribute 12; Rotator drops the crystal-ingot one-of pool; RubberDucky tames with raw fish and untames with dead bush; RubyBird's natural crystal-plains spawn removed (ruby dungeons are ported) and its bespoke day-only sound wired.
- **Finding IDs:** ENT-K-004, 005, 009, 013, 014, 019, 021, 023, 024, 027, 033, 045, 046, 050, 051, 056, 058, 059, 060, 064, 066, 068, 071, 073, 081, 083, 088, 089.
- **Build:** `.\gradlew.bat build` → BUILD SUCCESSFUL.
