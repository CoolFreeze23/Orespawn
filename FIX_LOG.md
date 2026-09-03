# FIX_LOG — OreSpawn Port Parity Implementation

Work record for `IMPLEMENTATION_PLAN.md`. One entry per closed finding ID.
Statuses: **FIXED** / **VERIFIED-CORRECT** (audit wrong, proof cited) / **DEFERRED** (owner-approved only).

Original source: `reference_1_7_10_source/sources/danger/orespawn/` (referred to as `orig:`).
Port source: `src/main/java/danger/orespawn/` (referred to as `port:`).

---

## Pending manual tests

Fixes that can only be truly confirmed in-game. Append here in every phase; burn the
list down at the end before release.

- **BUG-003** — Place a Rat mob spawner (or `/summon orespawn:rat`), let it tick:
server must not crash, rat must despawn normally when far away.
- **BUG-004** — Tame a Prince, push it to its growth thresholds (or downgrade
the Adult/Teen with a diamond — regressions restored in D3),
then log the owner out with the chunk loaded:
transformation must complete with ownership intact, no NPE.
- **BUG-005** — Let TheQueen reduce a Survival player to 0 HP via melee: normal
death screen/drops/respawn. Also confirm a low-HP mob victim still vanishes
without drops (original quirk preserved).
- **BUG-006** — Stand next to Godzilla's jump landing in Creative and Spectator:
no damage taken; Survival players still take the shockwave.
- **B2 attribute caps** — `/summon orespawn:the_king` then `/data get entity` its
Health: must read 7000 (not 1024). Same spot-check for TheQueen (6000) and
Godzilla (4000).
- **B2 stats** — spot-check 3-4 reconciled mobs in-game (`/attribute ... minecraft:generic.armor base get`)
against the table in `phase_b_reports/B2_mobstats.md`.
- **B1 drops** — kill (Survival) one of each: Kraken (ink sacs 120-279 + d53 gear,
NO cooked cod), Godzilla (painting/beef/bone, NO emeralds), TheQueen (56× scale/
beef/bone/flesh + Princess spawns), TheKing (royal set + 300 random registry items
+ Prince spawns), Mothra (20 moths burst), Dragon (beef 1-6, no bones). Confirm
nothing drops twice.
- **B3 riding** — mount and fly/ride each: Dragon (no rubber-banding — BUG-020),
Leon, Leonopteryx, Cephadrome (fed first), Ostrich (FAST jump on UP key),
ThePrinceTeen + Adult (tamed; strafe keys fire the canon trio). Verify a second
player observing sees smooth movement.
- **B3 SpiderDriver** — armor 8 mounted / 20 on foot; attacks (with poison) while
mounted on the SpiderRobot.
- **B4 animations** — stand still near Bee/Mothra/Urchin/Kyuubi etc.: idle
animations must keep moving (no frozen wings); Mothra renders at 10× scale and
flaps slowly (0.2 frequency).
- **ENT-A-002/012/017/023/040/060/072/078/091/106** — eyeball hitboxes (F3+B) of Alien/AntRobot/AttackSquid/BandP/Bee/Brutalfly/CaterKiller/CaveFisher/CloudShark/CreepingHorror against the sizes in `phase_c_reports/C1_entities_A_C.md`; CaterKiller must halve with `playNicely=true`.
- **ENT-A-031/060** — drop Basilisk and Brutalfly into lava: no fire damage.
- **ENT-A-100/101** — spawn many Crabs naturally: sizes vary (¼/½/full, occasional giants); spawner Crabs all small (0.35).
- **ENT-A-102/103** — Crab walks toward water, takes dry-out damage away from it, plays scorpion sounds on melee and a splash when healing in water.
- **ENT-A-004** — Alien melee applies Poison (40t on Easy, 30t otherwise), not Hunger.
- **ENT-A-013/014** — AntRobot melee noticeably throttled; while ridden it occasionally stomps nearby mobs for ~3.0.
- **ENT-A-025** — BandP steals an item on every successful hit (armor first).
- **ENT-A-036** — Basilisk uses custom living/hurt/death sounds (no Ravager).
- **ENT-A-045-051** — Big Bertha/Royal/Hammy: swing projectiles one-shot in range (496/746/82), respect `bigBerthaPvp` for players/tamed; Girlfriend/Boyfriend never hit.
- **ENT-A-054** — Boyfriend follows cooked beef, panics when hit, opens doors.
- **ENT-A-074/075** — damaged CaterKiller transforms after ~2 min into Brutalfly + 10 Butterflies (explosion sound); eats nearby leaves/logs (heal 2.0, occasional burp).
- **ENT-A-080** — CaveFisher hunts nearby passive animals, not just players.
- **ENT-A-082** — Cephadrome attacks Mothra/untamed Leon/GammaMetroid/WaterDragon; Kraken takes 1.5× hits.
- **ENT-A-087** — Chipmunk tames with apple (50%), releases with dead bush.
- **ENT-A-095/097** — Cockateil spawns with random bird type; only type 5 birds can drop rubies (player kill, 1-in-3).
- **ENT-A-110** — CreepingHorror only spawns naturally in darkness at night below y=15 (or in Chaos).
- **ENT-A-112** — Cryolophosaurus proactively chases nearby prey, not only retaliating.
- **C1 loot** — kill (Survival): Alien (spider eyes/flint/map/clock/compass), AntRobot (redstone jackpot), Beaver (0–2 porkchop), tamed Camarasaurus (2–6 poppies; untamed none), CaveFisher/CliffRacer/CloudShark/Cryolophosaurus/CreepingHorror (gamble drops), Coin (10-slot jackpot).
- **C1 spawns** — verify Alien/Alosaurus/Camarasaurus/Baryonyx spawn in the Mining dim (and Alosaurus/Baryonyx in Utopia), no longer in the End/overworld; Boyfriend beach hotspots; Bee/CaterKiller/Basilisk/Brutalfly/BandP in their per-biome lists.
- **ENT-D-002/006** — Dragon tames/heals with raw beef (1-in-5), ignores bones; diamond on a tamed dragon spawns a tamed Spyro and removes the adult.
- **ENT-D-012** — ridden Hoverboard hums `orespawn:hover` (randomized 1–6 variants), not the beacon tone.
- **ENT-D-014** — Emperor Scorpion in combat occasionally spawns a baby scorpion midway to its target (no cap — can flood).
- **ENT-D-022** — Cage: players bounce back an empty cage; Creeper always cages; Ghast/Enderman escape ~20%, Kraken ~95%; Bat gives 2 caged bats, Cockateil 4, AttackSquid 6; villager-hit consumes the cage and returns it; Iron Golem cageable; tamed Girlfriend/Boyfriend eat the cage with no drop.
- **ENT-D-025/026/027** — thrown rocks: t5 deals 10; t6/9–12 apply Weakness (not Wither); t9 ignites ~50s; block impacts shatter glass in 3×3×3 with the glassdead sound and return the same rock type; entity hits return nothing.
- **ENT-D-037** — tamed Gazelle drops 2–6 poppies; untamed drops 0–2 raw beef.
- **C2 loot** — kill (Survival): DungeonBeast (25% each crystal pink ingot/crystal apple/oak log/nothing ×0–2), Fairy (crystal torch), Firefly (extreme torch), GammaMetroid (5–14 gold nuggets + 6–15 iron), Ghost/GhostSkelly (nothing), GiantRobot (~60–116 laser balls + 10–19 kit/component rolls incl. detector rails).
- **C2 spawns** — Dragon/GoldFish/EnderReaper on the Island biome; DungeonBeast/Flounder/Irukandji/Frog in Crystal; EnderKnight/EnderReaper/Hammerhead/GammaMetroid/DungeonBeast in Chaos; GammaMetroid swarms in Mining; EnderKnight/EnderReaper dark-forest hotspots (w20/w38) and NONE in the End; Fairy only in dark forests overworld; Girlfriend beach hotspot (8–15 groups); Hydrolisc swamp/jungle; Frog river/swamp; ghosts in snowy taiga/taiga/frozen river/jungle/dark forest; no more ocean spawns for Flounder/GoldFish/Irukandji/Hammerhead/Hydrolisc.
- **ENT-K-005** — Kraken plays its custom living growl (1-in-5 ambient) and alo_death on death, not Elder Guardian sounds.
- **ENT-K-013** — LeafMonster attacks Ants/Butterflies/LunaMoths and players only (ignores other small mobs); never hunts with `playNicely=true`.
- **ENT-K-019/083** — Leon and RubberDucky untame with a dead bush (Leon glass no longer works); RubberDucky tames/tempts with raw cod, not wheat.
- **ENT-K-023** — Lizard out of water periodically pathfinds to the nearest water (never to lava/fire).
- **ENT-K-033** — MantisClaw hits drain 1 HP silently (no extra hurt flash) and heal the wielder 1.
- **ENT-K-045** — Ostrich takes no cactus damage but normal damage otherwise.
- **ENT-K-046** — tamed Ostrich drops 2–6 poppies; untamed drops 0–2 feathers.
- **ENT-K-050** — Peacock breeds with Crystal Apple only.
- **ENT-K-051** — Nightmares spawn mostly tiny (t=0.5); big ones rare (t=4 ≈ 1.5%); hitbox/model grow together up to 10×14 blocks; `nightmareSize=5` forces max size.
- **ENT-K-056** — PurplePower type 2 poisons, type 3 weakens (2.5 s each).
- **ENT-K-058** — wild rats attack players/pets even with default configs; a rat with an owner never attacks its owner (and respects ratPlayerFriendly/ratPetFriendly).
- **C3 loot** — kill (Survival): LeafMonster (log OR leaves OR rotten flesh), LurkingTerror (beef/flint/feather), Rat (rotten flesh), Robot2 (2–9 iron blocks + 5–10 iron ingots + redstone parts), Robot3/5 (20–40 laser balls + redstone parts), Robot4 (20–56 laser balls + RayGun + painting + redstone parts), Rotator (one of crystal pink ingot/tigers eye ingot/crystal coal/iron).
- **C3 spawns** — Kraken/Leon/Leonopteryx/RubyBird never spawn naturally (spawner-block/dungeon only); Kyuubi Nether weight doubled; Lizard only river/swamp/ocean; rat swarms in dark forests (10–20 packs) and taigas, not everywhere.
- **ENT-S-006** — SeaMonster visibly faster in water than on land (0.55 vs 0.25).
- **ENT-S-010** — SeaViper bites apply Poison ~6 s (8 s on Easy), never Hunger.
- **ENT-S-030/031** — killing a StinkBug nauseates (not starves) everything nearby incl. well above it; it breeds with Crystal Apple only, not apples.
- **ENT-S-033** — tamed Stinky occasionally burps coal out the front and farts a skin-matched item (e.g. blaze powder for skin 0) out the back, with sounds.
- **ENT-S-045** — TRex roars/hurts/dies with the orespawn trex/alo sounds, no Ravager audio.
- **ENT-S-057** — Ultimate Bow full-draw hit ≈ ceil(3×ultimateBowDamage); halving the config halves it; bow self-enchants Power 5 (not 10).
- **ENT-S-061/068** — drop an Urchin and a Vortex into lava: no fire damage.
- **ENT-S-065** — right-clicking a tamed Velocity Raptor with an empty hand no longer mounts it (sit toggle on shift still works).
- **ENT-S-069** — Vortex melee never launches the victim skyward; the drag pull still works.
- **C4 loot** — kill (Survival): Scorpion (~10% each gold/uranium/titanium nugget, often nothing), Skate (string), SpiderRobot (14–27 redstone-component drops), SpitBug (1–3 amethyst gems), tamed Spyro (1–4 beef; untamed nothing), TerribleTerror (one of flesh/emerald/feather), tamed VelocityRaptor (2–6 poppies; untamed nothing), Vortex (eye + painting + 5–11 mixed ingots/nuggets/sticks), WormSmall (nothing).
- **C4 spawns** — TRex only in Chaos/Mining dims (not overworld); TerribleTerror in Island/Chaos (not overworld); Urchin/Skate/Vortex in Crystal (+Chaos), no longer oceans/Nether; Spyro/VelocityRaptor only in Mining; Stinky in Nether + badlands + Island, not forests; SeaMonster ocean w4 + swamps; WormSmall never naturally.
- **BOSS-002/007** — F3+B on TheKing and TheQueen: 22-wide × 24-tall envelope; parts still take/route damage (King parent unhittable, Queen parts glued to bones); check the rendered models don't look lost inside the box.
- **BOSS-005/012** — place a King/Queen spawner: ~5 s fuse then both the spawner and the block above turn to air, boss appears 8 blocks up with its living sound and leashes near the spawn point; with `theKingEnable=false`/`theQueenEnable=false` the block still fizzles to air but spawns nothing.
- **BOSS-006** — Queen below 2/3 HP with <10 player hits: armor reads 23 (`/attribute` won't show it — overridden getter — so verify via reduced damage taken).
- **BOSS-010** — first hit on a dormant (blue) Queen deals normal damage while the 3 s wake-up animation plays; she can keep fighting/being hurt during it.
- **BOSS-016** — Godzilla growls godzilla_living (sporadic), hurts with alo_hurt, dies with godzilla_death — no Ender Dragon audio.
- **BOSS-018/020** — feeding a tamed Prince cooked beef heals 80 (not 20); DIAMOND (after diamond block) transforms baby→teen; gold ingot and cake do nothing on the baby.
- **BOSS-024** — baby Prince hunts Butterflies/Cockateils/Dragonflies/Mosquitoes/Mothra, not just monsters.
- **BOSS-025/035/042** — kill (Survival): ThePrince (1–4 beef), ThePrincess (1–4 beef), ThePrinceAdult (1 Prince Egg) — no diamonds/gold.
- **BOSS-026/031** — F3+B: PrinceTeen 3.25×4.25, PrinceAdult 6.25×10.25; eyeball model scale vs box, rider seats still correct.
- **BOSS-029** — gold ingot on a tamed PrinceTeen does nothing (no baby regression).
- **BOSS-032** — tamed idle riderless Adult with `fullPowerKingEnable=true` transforms after the grow counter (diamond block fast-path) into a King that goes through the isEnd "free" sequence ("Prepare to die!"); with the config false it never transforms; King no longer deals doubled damage from that config.
- **BOSS-036** — PrinceAdult: king_living only while aggro and riderless, king_hit on hurt, trex_death on death.
- **ITEM-001/005** — break overworld ruby/amethyst ore: never explodes; break a red-ant/termite troll block without Silk Touch: 15–20 mobs erupt (Silk Touch no longer bypasses).
- **ITEM-003/021** — uranium/titanium ore drops XP only below y=40; breaking an ender-pearl/eye-of-ender egg block pops 5–9 XP half the time and never duplicates itself.
- **ITEM-011/012** — left-click a Pizza block to eat a slice; left-click Duct Tape to repair held gear (both previously right-click only).
- **ITEM-013/014** — step on an RTP block: random teleport fires; walk onto mole dirt: feet sink slightly (lowered collision box).
- **ITEM-016** — Crystal Furnace: items cook in 7.5 s (150t); crystal coal burns 20000t (~133 smelts), crystal logs 800t, crystal planks 400t.
- **ITEM-019** — Kraken/Creeper repellent pushes targets away continuously (every ~0.5 s) within radius 20, not once a minute.
- **ITEM-027** — place a Duplicator Log on dirt/grass with blocks nearby: tree grows block-by-block, then copies ~20 nearby blocks into the 5×5 area.
- **ITEM-037** — Chainsaw: left-click swings deal 56 AoE damage in r=5 with the saw sound; breaking a log crushes the 11×16×11 wood/leaf volume.
- **ITEM-040** — Experience Sword in hotbar slowly repairs worn OreSpawn armor while draining the sword (armor-XP tick).
- **ITEM-043** — Ultimate Bow fires instantly at full speed (no charge-up) and crits ~25% of shots.
- **ITEM-047/048/049** — Instant Garden (18×15 plot, 8 crops + reeds/melons + 3 water channels), Instant Shelter (7×7 furnished, 14-item chest), StepUp/Down/Across (8-way from look yaw, extreme torches every 8, stops at obstructions, explosion fx, kept in creative).
- **ITEM-050/051/052** — ZooKeeper makes a mob persistent (1 use, breaks); Sifter on water/sand/gravel/dirt/grass rolls the original tables (mod fish from water!); Wrench refuses healthy unowned AntRobots, kits re-spawn robots with carried-over health/name.
- **ITEM-053** — DeadIrukandji is throwable; WaterBall hits drop a pickup ~10% of the time; SunspotUrchin lights blocks on fire; LaserBall/IceBall have no cooldown.
- **ITEM-058** — Peacock boots glide works with `royalGlideEnable=false`; Royal/Queen boots glide only with it true.
- **ITEM-059** — uranium/titanium ore smelts into a NUGGET (9 nuggets → ingot).
- **ITEM-064** — with `lessOre=true`, new chunks carry ~1/3 the uranium/titanium/amethyst/salt veins and ~1/2 the troll blocks.

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

## Carried forward — medium/low bugs (assigned 2026-06-13)

**Phase E owns BUG-014..BUG-031 (17 findings; BUG-020 was closed in Phase B3).**
The plan's Phase A covered only the 7 CRITICAL + 6 HIGH bugs; the 9 MEDIUM + 9 LOW
entries had no named owner until this assignment.

## Phase B — Carried forward (owners for every PARTIAL)

All 21 Phase B PARTIALs have a designated closing phase; none is unowned.


| ID                         | Remainder                                   | Closes in                                    |
| -------------------------- | ------------------------------------------- | -------------------------------------------- |
| ENT-A-002 (Alien)          | hitbox 1.1×3.25                             | Phase C — entities                           |
| ENT-A-012 (AntRobot)       | hitbox 2.75×1.25                            | Phase C — entities                           |
| ENT-A-017 (AttackSquid)    | dimensions 1.0×1.25                         | Phase C — entities                           |
| ENT-A-023 (BandP)          | size 0.75×1.75 + worn-gear armor clamp 8–23 | Phase C — entities                           |
| ENT-A-031 (Basilisk)       | fire immunity                               | Phase C — entities                           |
| ENT-A-040 (Bee)            | size 1.5×2.5                                | Phase C — entities                           |
| ENT-A-060 (Brutalfly)      | size 5.0×2.0 + fire immunity                | Phase C — entities                           |
| ENT-A-072 (CaterKiller)    | size 2.9×4.6                                | Phase C — entities                           |
| ENT-A-078 (CaveFisher)     | size 1.35×0.75                              | Phase C — entities                           |
| ENT-A-091 (CloudShark)     | size 1.0×0.75                               | Phase C — entities                           |
| ENT-A-100 (Crab)           | scale-driven size 3.75×3.5×scale            | Phase C — entities                           |
| ENT-A-103 (Crab)           | attack/splash sounds                        | Phase C — entities                           |
| ENT-A-106 (CreepingHorror) | size 0.75×0.5                               | Phase C — entities                           |
| ENT-K-051 (PitchBlack)     | continuous scale model (vs discrete tiers)  | Phase C — entities (renderer follow-through) |
| ENT-K-068 (Robot4)         | difficulty-scaled melee 15/20/25            | Phase C — entities                           |
| ENT-S-006 (SeaMonster)     | water speed-boost dead code (0.55 in water) | Phase C — entities                           |
| ENT-S-061 (Urchin)         | fire immunity                               | Phase C — entities                           |
| ENT-S-068 (Vortex)         | fire immunity                               | Phase C — entities                           |
| BOSS-006 (TheQueen)        | +2/+3/+5 phase armor scaling                | Phase C — bosses                             |
| BOSS-026 (ThePrinceTeen)   | size 3.25×4.25                              | Phase C — bosses                             |
| ANIM-012 (rider controls)  | Elevator riding (entity port)               | Phase D — missing features (rider elevator)  |


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

### Phase C slice 4 — entities S–Z

- **Report:** `phase_c_reports/C4_entities_S_Z.md` (per-finding orig citation / old / new value tables).
- **Scope & outcome:** 27 open DIVERGENT ENT-S findings + ENT-SYS-003 + 3 carried-forward PARTIAL remainders (006, 061, 068): **22 FIXED, 9 PARTIAL** (all nine are spawn findings whose weights/biomes JSON half is done; the `func_70601_bi` gate remainders → ENT-SYS-002, Phase D). SeaMonster's water speed-boost wired into MOVEMENT_SPEED (0.55 in water / 0.25, per-tick like the orig) and its ocean w4 + swamp w2 spawns restored; SeaViper bite restored to Poison with the orig 6s/8s-easy duration quirk (audit's flat 8s was wrong); StinkBug death gas corrected to Nausea (not Poison — audit wrong) in the exact −5..+10 vertical box, and its food to Crystal Apple only (the raw-fish isWheat is dead code); Stinky's item economy rebuilt — front burp now drops COAL with the burp sound, the 19-skin rear table restored item-for-item (blaze powder…peach seed) with the `orespawn:fart` sound (new aggregate `fart` event = random fart1-9, matching the 1.7.10 sounds.json); TRex wired to trex_living/alo_hurt/trex_death; UltimateArrow damage = ceil(velocity × ultimateBowDamage config) per the orig, with the bow's self-enchant restored to the orig fixed Power 5; VelocityRaptor's invented riding removed entirely (orig is a plain tameable) and its drop corrected to TAMED-only poppies 2-6 via the OreSpawnTamed flag; Spyro's drop likewise corrected to TAMED-only beef 1-4 (audit's "apple" was wrong); Vortex's invented skywardLaunch attack (and its false "signature 1.7.10 attack" comment) removed, its drops rebuilt (vortex eye + painting + the d10 stick/ingot/nugget/irukandji/crystal-coal pool — audit's "bone" was wrong), and `.fireImmune()` added to Vortex and Urchin; Skate drop corrected to STRING (audit's "raw fish" was wrong); Scorpion/SpitBug/SpiderRobot/TerribleTerror/WormSmall loot rebuilt from the originals (10% nugget one-of; 1-3 amethyst gems; 14-27 rolls of the shared d15 redstone table; flesh/emerald/feather one-of; nothing). Spawn domains rebuilt against the orig registrations: TRex/TerribleTerror/Urchin/Vortex/Skate/Spyro/VelocityRaptor/WormSmall pulled from invented overworld/ocean/Nether lists and placed into their real Island/Crystal/Chaos/Mining dimension lists; Stinky's Nether w2 + mesa-group ambient spawns restored. ENT-SYS-003 closed: the CaterKiller/Cryolophosaurus false-parity comments were corrected with the C1 behavior fixes, and the last one found (Vortex) is removed here. Audit errors corrected with proof: ENT-S-010 (duration), 014 (string), 026 (amethyst gems), 028 (tamed beef), 030 (nausea), 031 (crystal apple only), 044/062/071 (real dim lists — the audit misattributed `BiomeGenUtopianPlains` sections by one), 015/029/067 (single-dim spawn sources), 066 (tamed-gated 2-6), 070 (painting + d10 table, no bone).
- **Finding IDs:** ENT-S-004, 006, 008, 010, 014, 015, 023, 026, 028, 029, 030, 031, 033, 035, 039, 040, 044, 045, 057, 061, 062, 065, 066, 067, 068, 069, 070, 071, 079, 080; ENT-SYS-003.
- **Build:** `.\gradlew.bat build` → BUILD SUCCESSFUL.

### Phase C slice 5 — bosses

- **Report:** `phase_c_reports/C5_bosses.md` (per-finding orig citation / old / new value tables).
- **Scope & outcome:** 16 open DIVERGENT BOSS findings + 2 carried-forward PARTIAL remainders (006 Queen phase armor, 026 PrinceTeen size): **18 FIXED, 0 VERIFIED-CORRECT, 0 PARTIAL**. King and Queen restored to the original 22×24 envelope (EntityType + the Queen's MHLib main-hitbox, which was the live 16×12 box; part/bone damage routing untouched) — this also re-aligns the King's `MyCanSee` height·7/8 sight origin to the orig y+21; the King/Queen spawner blocks rebuilt to the original contract (100-tick fuse on placement, spawner + block above → air, spawn at y+8 with living sound, `setGuardMode(1)` home leash, new `theKingEnable`/`theQueenEnable` configs default-true per orig OreSpawnMain.java:6434-6435 — a disabled spawner still consumes itself); Queen's +2/+3/+5 phase armor override ported verbatim incl. the orig's unreachable +3/+5 branches (effective bonus always +2 — documented quirk), and her invented dormant-phase invulnerability removed (the blue→red Geckolib wake-up is now purely cosmetic; first hit damages normally per 1.7.10); Godzilla switched off Ender Dragon audio onto orespawn godzilla_living (1-in-5)/alo_hurt/godzilla_death; ThePrince feeding heals nutrition×10 (not flat 20), grows with DIAMOND (not gold ingot, invented cake shortcut removed) and hunts the original prey list (Mothra/Butterfly/Cockateil/Dragonfly/Mosquito restored); PrinceTeen resized 3.25×4.25 and its invented gold-ingot teen→baby regression deleted; PrinceAdult resized 6.25×10.25, its King transform re-gated on the full orig condition (idle + riderless + !Peaceful + tamed + `fullPowerKingEnable`) with `king.setFree()` restored (isEnd end-game sequence) — the port's invented "King deals ×2 damage" repurposing of that config was removed — and its sounds restored to king_living (aggro+riderless only)/king_hit/trex_death; Prince/Princess loot → 1–4 beef, PrinceAdult loot → Prince Egg ×1. PlayNicely shrink/targeting remains with BOSS-017 (open PARTIAL).
- **Finding IDs:** BOSS-002, 005, 006, 007, 010, 012, 016, 018, 020, 024, 025, 026, 029, 031, 032, 035, 036, 042.
- **Build:** `.\gradlew.bat build` → BUILD SUCCESSFUL.

### Phase C slice 6 — items & blocks

- **Report:** `phase_c_reports/C6_items_blocks.md` (per-finding orig citation / old / new value tables); recipe diff table in `phase_c_reports/C6_recipe_diff.md`.
- **Scope & outcome:** 58 open ITEM- findings (33 DIVERGENT + 19 PARTIAL + 6 UNVERIFIED): **53 FIXED, 5 PARTIAL** (020 dungeon structure pool → WGEN-042; 023 placed-cage block form; 053 Shoes/GameController throwables; 062 absent recipe families; 064 Mining-dim density → WGEN-011 — all Phase D), 7 MISSING skipped per ground rules. Block stats restored number-by-number (ore hardness/resistance incl. titanium 15.0/5.0, gem-block light 6/3/7/6, crystal-ore ctor parameter shift unwound, Lavafoam 5.0/5.0 friction 1.1); behavior parity for OreBasicStone (15–20 mobs, no Silk Touch escape), uranium/titanium y<40 XP, egg blocks (5–9 XP — the audit's "5–11" and the port's item-dupe both wrong), Pizza/DuctTape left-click, RTP stepOn, MoleDirt sunken box, CrystalFurnace (150t cook + 20000/800/400 crystal fuels), ExtremeTorch Cephadrome offsets, repellent 10t pulse, corn height cap 4–7, AppleLeaves Islands-only night transform, DuplicatorTree incremental build + 5×5 copy; 19 crop loot JSONs rebuilt. Weapons: UltimateSword/RoyalGuardian/Battle-axes/Nightmare/Poison/Experience/BigHammer/MantisClaw enchants+durabilities corrected (Bertha-class 9000 family incl. Slice rebuilt as the orig plain clone), Chainsaw's real identity restored (no enchants; 56-damage r=5 AoE + 11×16×11 tree crush + saw sound), Bertha's invented reach/kill-counter removed. UltimateBow back to instant-fire velocity 3.0 / baked Power 5 / 1-in-4 crit; SkateBow Infinity bypass + pull cap; UltimateFishingRod Unbreaking 2 (invented Luck/Lure removed). Gadgets rebuilt to the orig structures: InstantGarden 18×15, InstantShelter 7×7 (exact chest list), StepUp/Down/Across 8-way + extreme torches, ZooKeeper persistence (dur 1), Sifter's five weighted tables, Wrench/robot-kit health carry-through. All 14 armor sets' durability multipliers + enchantabilities fixed (they were swapped/scaled), four sets' baked enchants corrected, peacock glide un-gated from royalGlideEnable. Recipes: 381 orig registrations script-diffed — 59 JSONs rewritten to the originals, 16 invented recipes removed (ray gun craft, royal gear set, robot kits, pizza/island/lavafoam blocks…), uranium/titanium smelting → nuggets XP 0.3, salt/popcorn back to smelting; kyanite system documented as a port addition (→ WGEN-024). lessOre wired via the new `orespawn:less_ore_count` placement modifier (ores ÷3, troll blocks ÷2). Projectile family verified number-by-number; invented LaserBall/IceBall cooldowns and the invented Coin item removed; DeadIrukandji throw, WaterBall 1-in-10 drop, urchin fire restored. Audit errors corrected with proof: ITEM-007 (uranium/titanium blocks are 5.0/5.0 light 3/7, not 4.0/4.0 light 6), ITEM-021 (orig XP 5..9, not 5–11), ITEM-061 note (orig :3084 crafts a wooden door, not a piston).
- **Finding IDs:** ITEM-001..021, 023..028, 030..056, 058, 059, 062, 064.
- **Build:** `.\gradlew.bat build` → BUILD SUCCESSFUL.

### Phase C slice 7 — worldgen

- **Report:** `phase_c_reports/C7_worldgen.md` (per-finding tables incl. rate-equivalence math, audit-error proofs, Phase D owners).
- **Scope & outcome:** 39 open WGEN- findings (14 DIVERGENT + 17 PARTIAL + 8 UNVERIFIED): **29 FIXED, 5 VERIFIED-CORRECT** (009, 041, 046, 047, 050), **5 PARTIAL** (005 SpawnOres pool, 014/018/033 missing structures, 036 spawner-outcome table — remainders all Phase D structure/spawn-block scope), 0 still-UNVERIFIED; 11 MISSING skipped per ground rules. Ore veins rebuilt on a new `orespawn:vein_count` placement modifier (`OreSpawnVeinPlacement`) that reproduces the original `rate + nextInt(dice)` attempts, the y=nextInt(128) reject-outside-window rule, the LessOre ÷3 (÷2 trolls) truncation, and the Mining-dim ×3 passes — uranium/titanium/amethyst/salt/troll values restored number-by-number; ruby reverted to the orig lava-seek single-block placement (`RubyLavaSeekFeature`) in overworld/Utopia-only dungless dims, plus Mining triple pass and the previously-absent Nether lavafoam/ruby and Mining lapis boost. Islands and Chaos got real noise settings (flat bedrock/dirt/grass plane; nether-shaped 128-high stone) replacing floating_islands/overworld noise; Mining/Village gained vanilla mineshafts/strongholds, water/lava lakes (1/4, 1/8) and 8×monster rooms. Anthills (redfreq per dim), veggie patches, and Islands unstable anthills are now world-placed. Five biome spawn rosters rebuilt from `BiomeGenUtopianPlains`/`ChunkProviderOreSpawn2` (incl. the inherited vanilla + Utopia layers in Village, the full ~55-entry Chaos list, Mining's dino/alien roster); inventions (rat/worms/beaver/vampire-butterfly…) and weight-doubling `dim_*_locals` duplicates removed. Structures: shadow/WTF/leonopteryx/beehive sets re-spaced to 26/13 (1/665 Mining rotation odds), beehive re-homed to Mining, greenhouse/robot-lab/white-house + challenge towers re-homed to Islands (towers ARE original — `makeEnormousCastle(Q)`), royal altars 45/22 (1/2000), redundant crystal maze/tower datapack duplicates deleted (code path is authoritative), generic-dungeon spawner pool restored to the exact `nextInt(12)` ladder and generic/ruby chest loot transcribed to loot tables (5+d7 / 4+d7 rolls); ruby dungeon back to Utopia lava-adjacent placement. 11 crystal egg-ore blocks (`OreGenericEgg`, XP-on-break — audit's "break-to-spawn" claim disproven) registered with original assets and wired into the crystal sphere generator. Termite Crystal-travel empty-inventory/armor gate and ant pet co-teleport (48×24×48 box) ported. WGEN-024 decided: pink-tourmaline vein removed, kyanite vein kept as the documented crafting-chain exception (PN-009); PN-010 (SpawnOres reduction), PN-011 (Utopia portal block) added. Audit errors corrected with proof: WGEN-017 (Jeffery=giant_robot, Criminal=band_p), WGEN-023 (eggs drop XP, never spawned mobs), WGEN-031 (Chaos visuals are vanilla), WGEN-035 (ruby dungeon is Utopia-only), WGEN-039 (port was 32/16, not 26/13), WGEN-043 (towers exist in orig, Islands placement), WGEN-046 (ScragglyTreeWithBranches is dead code; SmallTree is IslandToo-only).
- **Finding IDs:** WGEN-001, 002, 005, 006, 008..012, 014, 016..020, 022..036, 039..041, 043, 046..050.
- **Build:** `.\gradlew.bat build` → BUILD SUCCESSFUL.
- **Pending manual tests (in-game only):**
  - Vein rates: create a new world, `/execute in orespawn:mining run tp 0 100 0`, mine a few chunks at Y10-20 and compare uranium/titanium density vs overworld (expect ≈3× from the triple pass); check salt only above Y50, amethyst only below Y25.
  - Ruby lava-seek: overworld cave lakes below Y50 — ruby ore should appear in stone directly UNDER lava, never as free-floating veins; Nether should have lavafoam veins and occasional nether-ruby.
  - Islands terrain: `/execute in orespawn:islands run tp 0 80 0` — flat grass plane at Y7, no vanilla carvers/lakes; unstable anthills near Y7 grass.
  - Chaos terrain: 128-high stone world with grass band ~Y60-65, vanilla sky; verify chaos roster spawns (bees/cassowaries/dino mix) and no dungeons/towers.
  - Structures: `/locate structure orespawn:beehive` (Mining only), `orespawn:greenhouse` / `robot_lab` / `white_house` / `challenge_tower_king` (Islands only), `orespawn:royal_altars` (Utopia, ~45-chunk grid).
  - Dungeons: in Utopia dig at lava pools below Y50 for the ruby-brick dungeon (chest should contain ruby kit/ThunderStaff pool); generic cobble dungeons in Utopia/Mining/Village/Islands — break the spawner block and confirm the 12-mob ladder mobs appear after ~20 s (400t).
  - Mining lakes/rooms: caves should contain vanilla-style water/lava lakes and monster rooms; `/locate structure minecraft:mineshaft` and `stronghold` must resolve in Mining and Village.
  - Termite gate: right-click a termite with any inventory item or armor equipped → "Empty your inventory!" / "Take off your armor!"; empty → Crystal teleport. Ant teleport with a sitting vs following tamed wolf nearby — only the following one co-teleports.
  - Crystal egg ores: in the Crystal dimension sphere shells, break oreurchin/orerat/etc. blocks — expect the block item + occasional 5-9 XP, NO mob spawn.

### Phase C slice 8 — animations & GUI

- **Report:** `phase_c_reports/C8_animations_gui.md` (per-finding orig citation / old / new value tables, audit-error proofs, Phase D owners).
- **Scope & outcome:** 15 open ANIM- findings (6 DIVERGENT + 9 PARTIAL): **11 FIXED, 3 VERIFIED-CORRECT** (015 duplicate of ITEM-016; 019/020 stale — PurplePower repel and all six dimension teleports already exist), **1 PARTIAL** (006 — all legs now render; canned-sine gait vs the orig RenderSpiderRobotInfo leg solver → Phase D entity-AI owner), 2 MISSING skipped per ground rules (014, 016). The original's generic `RenderInfo` POJO recreated as a per-entity client scratch (`entity/client/RenderInfo.java`) and attached to Kraken/Rotator/Robot2/Robot3 — the audit's "server-synced ri1" claim disproven (orig never datawatcher-synced it; the real divergence was per-entity vs model-singleton state). Rotator restored to the 24-blade tri-axis gyroscope (each blade 8× at 45° steps, fans spun on X/Y/Z by per-entity rf1 +2°/frame wrap 359°); GiantRobot got its full walk cycle back (movescale clamp, hip sway/bob, two-phase thigh/shin) with two-pass shared-part leg/arm rendering and the getAttacking()-gated punch windmill — obviating ANIM-014's holder class (orig recomputes every pose per frame); SpiderRobot now poses-and-renders each of its 8 legs inside the loop (same bug found+fixed in ModelAntRobot, 6 legs); Robot2/3/4 arm animations re-gated on getAttacking() (Robot2 random per-arm windmill via ri1 re-roll at sine zero crossings; Robot3 latch-at-cosine-crossing; Robot4 shield pump |cos|·45°+0.75 and cannon aim 0.85 rad with the pivot-following cannon assembly, shin rest offsets corrected); Rat's attack-vs-idle TAIL thrash restored (audit said "head bob" — orig ModelRat.java:116-120 only animates the tail) with leg phase signs fixed and the invented head yaw removed. Fly-up keybind default reverted to Left Alt (orig LWJGL 56); fly-down/special documented as port-only. The HUD rewritten to the original universal pointed-at-mob health bar: crosshair pick entity + 16-block ray-trace fallback, ~45-type eligibility chain with ownership/activity gates and special cases (phasing Worm, BandP Banker/Politician, >0.75-scale Crab), textured 182×5 girlfriendgui.png bar (texture copied from orig assets) at y=25/15 with the 0xFF3434 name 10px above, GUI_OVERLAY_ENABLE gate kept. ExperienceCatcher restored to the original single-orb mechanic (1×2×1 click column, value ≥3, 80% roll → Bottle o' Enchanting + string + stick, catcher consumed unless creative; miss drops the catcher). Per-mob spawn flags completed: 56 config entries added (orig OreSpawnMain.java:6364-6465; boyfriendEnable default false, orig key spellings nightmareEnable/criminalEnable kept) and ~65 ModSpawnControl map entries wired incl. bosses, water mobs, Robots 1-5, ambients and all cow variants under cowEnable; KrakenRevengeHandler now respects KRAKEN_ENABLE. Crystal Furnace verification side-find fixed: fuel container item left behind when the stack empties (lava bucket → empty bucket, orig TileEntityCrystalFurnace.java:165-170).
- **Finding IDs:** ANIM-002, 003, 005, 006, 007, 008, 009, 010, 011, 013, 015, 017, 018, 019, 020.
- **Build:** `.\gradlew.bat build` → BUILD SUCCESSFUL.
- **Pending manual tests (in-game, nearly all visual):**
  - Rotator spins as a 24-blade gyroscope ball (three 8-blade fans turning on different axes), not three flat blades.
  - Summon two Krakens side by side — their mouth-twitch cycles should differ (per-entity state), and twitch more aggressively while attacking.
  - GiantRobot ("Jeffery") walks with BOTH legs and arms visible, hips bobbing; aggro it and confirm the windmill punch + shoulder twist.
  - SpiderRobot renders all 8 legs; AntRobot renders all 6.
  - Robot2: arms rest at the sides when idle, windmill (randomly right/left/both) only in combat. Robot3: arms swing only in combat. Robot4: shield arm pumps and cannon raises to fire only in combat, cannon barrel follows the arm.
  - Rat: tail sways gently when calm, thrashes fast and wide when attacking; head no longer turns.
  - Controls: with default binds, Left Alt (not Space) makes a ridden Dragon/Cephadrome/Ostrich fly up / sprint.
  - HUD: crosshair on TheKing (or Mobzilla, a robot, a big crab…) shows the textured health bar above the hotbar with the red name label; named pets show their custom name; an owned Girlfriend shows the bar but someone else's does not; bar shifts up 10px while swimming eye-deep or wearing armor; `guiOverlayEnable=false` hides it.
  - ExperienceCatcher: click the ground under a dropped XP orb (worth ≥3) — expect a Bottle o' Enchanting + string + stick about 4 times in 5, and the catcher (dropped back at your feet) on a miss.
  - Spawn flags: set e.g. `krakenEnable=false`, `godzillaEnable=false`, `cowEnable=false` in `orespawn-common.toml` — no natural spawns of those mobs (and no AttackSquid revenge Krakens); spawn eggs/summons still work.
  - Crystal Furnace: burn a lava bucket — the empty bucket must remain in the fuel slot.

### Phase C checkpoint — corrected ledger (2026-06-13)

The end-of-C checkpoint reported 394 terminal findings; that figure was wrong on two
counts and is corrected here for the record (script: `tools/ledger_reconcile.py`):

- The per-phase closure totals (A 13 + B 117 + C 284 = 414) count closure EVENTS, not
unique IDs: 20 of Phase B's 21 carried-forward PARTIALs were re-counted when Phase C
closed their remainders (13 in C1, 2 in C3, 3 in C4, 2 in C5; ANIM-012's Elevator
remainder went to Phase D, not C). 414 − 20 = 394 unique IDs *touched*.
- Of those 394, 49 carry `Resolution: PARTIAL`, which is NOT a terminal state.

**True ledger after Phase C: 345 terminal (326 FIXED + 19 VERIFIED-CORRECT +
0 DEFERRED) / 256 open** = 49 resolved-PARTIAL (all Phase D-owned) + 61 MISSING
(Phase D) + 95 untouched PARTIAL (Phase E) + 7 untouched UNVERIFIED (Phase E) +
17 medium/low BUGs (Phase E, see carried-forward section above) + 27 OPT (Phase F).
345 + 256 = 601 ✓.

---

## Phase D preliminaries — PN-009 closure (2026-06-13)

### WGEN-024 / PN-009 — kyanite branch removal (owner decision: Option A)

- **Decision:** faithful replication. The Phase-10 invented kyanite/pink-tourmaline
branch is removed from the parity build; its complete design (blocks, items, tier
stats, armor values, recipes, worldgen) is archived in MODERNIZATION_NOTES MOD-009
as a deliberate 2.0 content candidate, including the world-compat impact (branch
items vanish from existing port worlds on load).
- **Files deleted (35):** `ModBlocks` ORE_KYANITE/ORE_PINK_TOURMALINE, `ModItems`
KYANITE/PINK_TOURMALINE gems + 2 BlockItems + 5 kyanite tools + 4 kyanite armor,
`ModToolTiers.KYANITE`, `ModArmorMaterials.KYANITE`, 13 `ModCreativeTabs` rows;
data: 13 recipe JSONs, 2 block loot tables, `add_crystal_dim_ores` biome modifier,
`ore_kyanite` configured+placed features, 2 tag entries (mineable/pickaxe,
needs_iron_tool); assets: 2 blockstates, 2 block models, 13 item models, 12 lang
entries.
- **Display names restored:** `crystal_stone` family renamed to the original 1.7.10
strings — block "Kyanite" (orig `OreSpawnMain.java:3029`), tools "Kyanite
Sword/Pickaxe/Shovel/Hoe/Axe" (orig `:3239-3243`); they were shipping as "Crystal
Stone ...".
- **Verification:** `git grep kyanite|tourmaline -- src/` → only two explanatory
comments remain; the original chain (crystal_stone + crystal_sticks → 5 tools,
`:3244-3252`; ×8 → Crystal Furnace, `:3082`; tier 3/800/+6/ench 45, `:1507` =
`ModToolTiers.CRYSTAL_STONE` 800/6.0/5.0/45) is untouched. Build: see commit.

---

## Phase D — slice D1: spawn architecture + dimension access (2026-06-13)

### Scope and method

The original 1.7.10 sources contain **103** per-entity `func_70601_bi` overrides
(extracted corpus: `phase_d_reports/D1_original_spawn_rules.md`, generated by
`tools/extract_spawn_rules.py`). D1 ports every one of them as a `checkSpawnRules`
override, each citing its original file:line range in Javadoc. Coverage is verified
mechanically: `tools/d1_gate_diff.py` reports **0 originals without a port gate**.

### Shared infrastructure (new)

- `ModDimensionKeys` — the six dimension `ResourceKey`s mapped from the original
numeric IDs (OreSpawnMain.java:1595-1600): Utopia/Mining/Village/Islands/Crystal/
Chaos, plus the `isIn` helper used by every dimension-gated rule.
- `entity/OriginalSpawnGates` — the five primitives every original gate combines:
spawner-proximity bypass (id-matched, the 1.7.10 name-string check in registry-id
form; single-pos variant `isOwnSpawner` for interleaved scans), clear-air/`boxMatches`
with the originals' inclusive bounds, `countBuddies`/`anyOtherNearby`,
`isDaytime` (func_72935_r), `isDarkEnough` (func_70814_o).

### Gate batches

- **Batch 1** (`tools/insert_spawn_gates.py`, 29 entities A-C + robots + misc) and
manual ports with side effects: AttackSquid, CaveFisher, Crab (spawner forces 0.35
scale), Rotator (`wasSpawnered`), Rat (Crystal air-pocket), Cephadrome (`badmood`),
WormLarge (`wormsSpawned`), PitchBlack (scale clamp + Chaos crowd check).
- **Batch 2** (`tools/insert_spawn_gates_d1b.py`, 31 entities): Boyfriend/Girlfriend
(spawner-else-super), Dragon, DungeonBeast (Crystal 25≤y≤28 + air-ring),
EasterBunny, EmperorScorpion (interleaved spawner-or-air scan), EnderKnight,
EnderReaper, GammaMetroid, HerculesBeetle, Kyuubi, Leon, Pointysaurus, PurplePower,
RockBase, RubyBird, SpitBug, Spyro, Stinky, TRex, TerribleTerror, TheKing,
ThePrince(+Adult/Teen `return false`), ThePrincess, TheQueen, Triffid, TrooperBug,
WormMedium, WormSmall; manual: **Vortex** (`wasSpawnered` + `busyFighting` — gate,
far-away-despawn exemption orig :64-72, daytime-discard guard orig :134-143).
- **Batch 3** (`tools/fix_preexisting_gates.py`, 18 rebuilt): pre-existing gates
audited against the corpus (`phase_d_reports/D1_preexisting_gate_audit.md`); the
divergent ones rebuilt — Butterfly (spawner forces type 1, Islands), Dragonfly,
LunaMoth (night+Islands), StinkBug (spawner), Firefly (night+buddies+Islands),
Flounder (day), Frog (Crystal dice), Ghost/GhostSkelly (spawner; the canSeeSky
invention replaced with the original night check), Hammerhead (full chain),
Irukandji (day), Ostrich (day), SeaMonster (night+darkness), SeaViper (day),
Skate (day), VelocityRaptor (true day check, not canSeeSky), WaterDragon
(spawner+day), Whale (day); manual: **Urchin** (`wasSpawnered` + despawn guards
orig :87-107). Kept as-is (verified faithful): Beaver, Chipmunk, CloudShark,
CreepingHorror, Ant, Cricket, Mosquito, Termite (≡ inherited Ant rule), Fairy,
Gazelle, GoldFish, Lizard, CliffRacer, SpiderDriver, and the documented
config-gated adaptations Godzilla + Mothra.

### Categories / placements / spawn entries (earlier this slice, same commit series)

- MobCategory fixes: Coin + Cephadrome → AMBIENT, AttackSquid + RubberDucky →
WATER_CREATURE, WormLarge → CREATURE; placements registered accordingly.
- New BM JSONs: AttackSquid (river/swamp/ocean), RubberDucky (river/deep ocean),
Cephadrome (snowy), Coin (overworld), WormLarge (plains/savanna/plateau).

### Findings closed (45)

- ENT-SYS-002, ENT-SYS2-003, ENT-SYS2-004 — the three systemic spawn findings.
- WGEN-013 — verified already satisfied by the C7 roster rebuild (mining ambients).
- 31 Phase-C PARTIALs whose remainder was the gate: ENT-A-006/011/027/029/035/044/
064/071/077, ENT-D-004/009/018/021/030/032/033/036/050/053/057/063, ENT-K-060,
ENT-S-008/015/029/035/040/044/062/067/071.
- 6 category/spawn-entry findings: ENT-A-021/085/099, ENT-K-085, ENT-S-087, ENT-D-046.
- Still PARTIAL (seasonal registration remainder only; gates done): ENT-D-011
(Easter), ENT-D-039/041 (Halloween) — owned by the seasonal-gates slice.

### WGEN-015 (PN-012) — approved 2026-07-02, committed

Village-dimension villages: `worldgen/structure/dim_village.json` +
`structure_set/dim_villages.json`, spacing 9 / separation 7 per
MapGenMoreVillages.java:11-12. Owner approved Option A: the original delegated
village *style* to vanilla (`MapGenMoreVillages` only overrode spacing/separation),
so the port uses modern 1.21.1 jigsaw plains villages; density/placement —
OreSpawn's actual contribution — are exact. See PN-012 (APPROVED). No
1.7.10-style template work planned (style was never OreSpawn content).

### Verification

- `tools/d1_gate_diff.py`: 103 original rules, 0 missing in port.
- `gradlew compileJava`: green after each batch.
- Manual test notes: spawner-placed mobs (e.g. via `/setblock` spawner with mob id)
must spawn regardless of light/altitude gates; natural spawns of Alosaurus/TRex
etc. only at night above y50 with clear air; Vortex/Urchin/Rotator from spawners
must not despawn when the player walks away.

---

## Phase D — slice D2: robot gait solvers, GiantRobot walk state, Elevator rider (2026-07-02)

- **Report:** `phase_d_reports/D2_gait_elevator.md` (full citation tables).
- **ANIM-006 — FIXED (remainder closed).** The canned-sine gait in SpiderRobot/AntRobot
  replaced with line-by-line ports of the original client-side leg solver:
  `initLegData`/`getNewVelocity`/`updateLegs`/`findNewFooting` (orig SpiderRobot.java:111-486,
  orig AntRobot.java:156-510 — same algorithm, different constants: 8 vs 6 legs,
  99px vs 49px segments, ×8[1,4] vs ×18[2,8] velocity scales, 294/32 vs 144/22
  relocation windows, spider-only ridden grass trample). `RenderSpiderRobotInfo`
  expanded to the original's full field set (orig RenderSpiderRobotInfo.java:6-40),
  leg-count-parameterized; both models already consumed it (C8 per-leg render loops).
  Original quirks preserved: hand-typed `pi = 3.1415926545`, int-truncated block
  coordinates, client-side trample world-mutation.
- **ANIM-014 — VERIFIED-CORRECT.** `RenderGiantRobotInfo` is per-frame scratch: written
  orig ModelGiantRobot.java:162-167, read :170-224 in the same render call; the sole
  other write (`gpcounter = 2000000`, orig GiantRobot.java:80) has no reader. The C8
  walk cycle (ANIM-005) already computes the identical per-frame formulas
  (0.19634954084936207 / 0.6283185400806344 digit-for-digit), so the walk state
  matches with no holder class. Owner accepted the proof 2026-07-02.
- **ANIM-012 — FIXED (7 of 7 mounts).** The Elevator remainder closed: full port of
  orig Elevator.java:232-515 as client-predicted `tickRidden` (B3 architecture) +
  server-side world effects (`serverRiddenTick`) + client particle effects. Hover
  1.25/0.75, obstruction climb ×0.11, yaw lag |1.85−v|, exploding malfunction
  (1-in-20000 @ v>0.65, 45t, −0.05/t speed bleed), crash → 6+d10 sticks + 2 diamonds,
  throttle 0.025/0.15-boost/−0.02, fly-up "FAST" cap 0.85→1.85 via `RideableFlyer`
  (orig :441-443; fly-down deliberately ignored — orig had one key). Also restored:
  missing item drop on destruction (orig :184-186), color-cycle renderer textures
  elevator1-10.png (orig :45-54/73-107) + boat hit-wobble (orig RenderElevator.java:31-38),
  1.25×1.0 hitbox (orig :58), tracking 128/1/velocity (orig OreSpawnMain.java:3883),
  item spawn placement +1.2/random-yaw/stack-1 (orig ItemElevator.java:21-36, invented
  anvil sound removed), `ModSounds.HOVER` hum. Mapping deltas (crash detection
  server-side, one-tick lag possible; riderless client nudge → vanilla lerp) are in
  the class Javadoc. PN-002 checked: ThePrince/ThePrincess flight — no Elevator
  intersection; stays with D3.
- **ENT-D-066 — new finding, FIXED (owner decision 2026-07-02).** The port shipped a
  second, invented hoverboard ("Phase 10" `HoverboardEntity` + `hoverboard` item)
  alongside the faithful `elevator` port; the original had exactly one
  (orig OreSpawnMain.java:1904/5174/3879-3883). Duplicate removed entirely (4 classes,
  2 registrations, attributes/renderer/layer/creative-tab rows, item model, 2 lang
  keys); not archived to MODERNIZATION_NOTES (duplicated an original feature — owner
  ruled nothing worth reintroducing). `elevator` item/entity displays corrected
  "Elevator" → "Hoverboard". **World-compat:** placed `orespawn:hoverboard`
  entities/items vanish from existing port worlds (unknown id).
- **Progress notes (owners unchanged, Phase E):** ENT-A-016 (AntRobot) and ENT-S-021
  (SpiderRobot) leg-animation clauses closed by ANIM-006's solver port; their
  ride-physics / flame-attack remainders stay open. ENT-A-016's stale
  "compare HoverboardEntity" fix hint retargeted to `entity/Elevator.java`.
- **Ledger:** 391 terminal (371 FIXED + 20 VERIFIED-CORRECT) / 211 open, total 602
  (601 audit IDs + ENT-D-066; `tools/ledger_reconcile.py` TOTAL_EXPECTED bumped, green).
- **Build:** `.\gradlew.bat compileJava` → green.
- **Pending manual tests (in-game):**
  - SpiderRobot/AntRobot: feet plant in the world and step ahead as the body moves
    (no synchronized sine paddling); legs relocate when overstretched; a ridden
    SpiderRobot occasionally flattens tall grass / turns grass blocks to dirt.
  - Hoverboard (`orespawn:elevator`, displays "Hoverboard"): W/S throttle with
    Left Alt FAST boost; terrain climb; pitch grows with speed; wall slam above
    ~0.75 speed shatters into sticks + 2 diamonds; rare high-speed malfunction
    (explosions/smoke, speed bleed, 45t); Ultimate Sword click cycles 10 skins;
    mob punches can't destroy it while ridden; hover hum only while ridden.
  - Creative tab: exactly one Hoverboard entry; `/summon orespawn:hoverboard` fails
    (intentional removal).

---

## Phase D — close-out (2026-08-10): PHASE D COMPLETE

- **Close-out fixes (all verified against originals):** WGEN-064
  DisableOverworldDungeons gate restored in findGenerationPoint over the 11
  wired overworld types (orig OSW:284; DSB path stays ungated); WGEN-067
  greenhouse double-door entry rebuilt (two doors at width/2 & width/2−1,
  lintels, meta-4 buttons — the D6a robot-lab door trace; the port had a
  single door at the wrong x); WGEN-068 white-house door upper half
  restored + button re-hung NORTH; ITEM-068 bee/mantis/small-beehive chest
  facings restored (inward E/W/S/N ring per orig metas); ITEM-069
  bee/mantis egg loot restored (BEE_SPAWN_EGG 2-8 w15, MANTIS_SPAWN_EGG
  2-4 w20 — the invented golden-carrot/spider-eye stand-ins removed).
- **CrystalMazeFeature audit (sweep F5):** NOT deleted — it maps to real
  original code (CrystalMaze.java, per-chunk at Y=25); the live faithful
  path is the chunk-generator port (WGEN-027), leaving the Feature a
  datapack-orphaned divergent duplicate → WGEN-070, Phase E retirement
  candidate.
- **Ledger close-out (tools/d6b_ledger_patch.py):** WGEN-042 and ITEM-020
  closed FIXED; WGEN-014/018/021/033/036 + ITEM-064 closed (their
  remainders were exactly the D5-D6b ports); new findings WGEN-063..071 +
  ITEM-067..069 recorded (10 FIXED, WGEN-070/071 open → Phase E);
  ENT-A-054, ENT-A-083, WGEN-003/004/007, ITEM-023 re-owned to Phase E.
  Ledger 618 → 630 IDs, 479 terminal / 151 open, **Phase D owns zero open
  findings.** Full rollup: `phase_d_reports/phase_d_rollup.md`.

---

## Phase D — slice D6b batches 1-4: mechanical structures (2026-08-08/10, batch 4 COMPLETE — close-out above)

- **Pipeline:** every structure ran spec-extract → independent spec-verify →
  implement → per-structure code-verify (specs in `phase_d_reports/d6_extraction/`).
- **Batch 1 (committed 1cb4cf0):** PlayPool (DSB 12, ocean-air anchor, set 46/23),
  CloudSharkDungeon (DSB 14, Islands sky band Y150-159, 17/8), GoldFishBowl
  (DSB 17, ocean, 46/23), SpitBugLair (DSB 19, swamp, 14/7), UrchinSpawner
  reconciliation (missing UrchinEnable gate restored in CrystalStructures),
  RotatorStation live path (DSB 3). New placement modes OCEAN_SURFACE_AIR /
  SKY_BAND_150 / SWAMP_GRASS_SURFACE. Verify pass: 4x zero issues + 1 real
  catch — the WGEN-062 restore was arming the cooldown on the suppress path
  (original arms it only on build, OSW:1992); fixed via tri-state result.
- **Batch 2 (this commit):** Igloo (builder + DSB 20 ONLY — worldgen placement
  deliberately unwired, NEEDS_DESIGN_RULING: the snow-biome-border frequency/
  biome decision, igloo_spec.md §7.3), EnderReaperGraveyard (End, DSB 18,
  10/5), WaterDragonLair (ocean, DSB 13, 46/23), LeafMonsterDungeon (plains,
  DSB 15, 17/8), MiniDungeon (Islands, DSB 16, 44/22), CephadromeAltar
  (Islands, DSB 34, 44/22). Salts 84350-84361 all unique.
- **Batch-2 verify pass: 0 critical / 0 major / 4 minor, all resolved:**
  (1) Igloo apex-"skylight" is quadrant-dependent (float32 four-quadrant
  simulation; code was bit-faithful, doc + spec S3 corrected); (2) Graveyard's
  interim reflection access to the piece level replaced with the new sanctioned
  `terrainStateIfInChunk` read-at-write-cell helper; (3) LeafMonster's
  foundation-root probe (air/tallgrass, orig GD:2113-2114) restored via the
  same helper, replacing an interim treat-as-air fill; (4) MiniDungeon
  fetch-count comment corrected (23 fetches / 12 sites).
- **Infrastructure:** `LegacyDungeonPiece.terrainStateIfInChunk` — the one
  sanctioned pre-build terrain read (read cell == write cell only), now used
  by the royal-altar-style skirts; documented against the pattern doc's
  no-reads contract.
- **Batch 3 (2026-08-10, this commit):** BouncyCastle (desert, DSB 26, set
  15/7, salt 84362, new mode SAND_SURFACE_MINUS1 — anchor is the sand block,
  OSW:1292), DamselInDistress (Village dim, DSB 28, 16/8, 84363, new mode
  VILLAGE_GRASS_SURFACE — grass-block anchor + quickSpaceCheck via
  footprintClearAbove; direct Girlfriend spawn GD:3727-3732 via spawnEntity,
  persistence inherent to the entity class), GirlfriendIsland (ocean, DSB 35,
  42/21, 84364 — MonsterIsland's geometry twin, fixed spawner set, no 50/50
  roll), StinkyHouse (Islands i==15, DSB 39, 44/22, 84365), Pumpkin (Islands
  i==17, DSB 44 with the original's clickedY+1, 44/22, 84366, new mode
  ISLANDS_GRASS_AIR, no loot — chest-free builder), Rainbow (Islands i==18,
  DSB 46, 44/22, 84367, new mode SKY_BAND_70 = Y 70+nextInt(20) sky band;
  double chest, both halves loot-bound 10-14).
- **Batch-3 ruling (pattern doc §1 step 5 amended):** a 1.7.10 chest list
  consumed at more than one fill count ships ONE loot table PER (list, fill
  formula) pair — generalizes the D6a Kyuubi four-fill precedent; first
  cross-structure use: `chests/damsel_in_distress.json` (rolls 10-14) vs
  `chests/girlfriend_island.json` (rolls 4-8), entries identical, twins
  cross-named via `_shared_list` keys.
- **Pattern-doc trap corrected (batch 3):** step 6's "some indices fire TWO
  builders (e.g. type 24)" example was itself the truncated-read trap — a
  full-dispatch recount (DSB:53-202) shows all 50 blocks are single-call;
  types 43/44/45 are the only clickedY+1 outliers.
- **Batch-3 verify pass: 0 critical / 6 minor** (7 verifiers: 6 per-structure
  + 1 cross-cutting): damsel iron-bar comment mis-justified the repo-wide
  unconnected-panes approximation (comment fixed; approximation stands),
  rainbow loop-7 comment credited m>=5 arches for the four +34 rain-cell
  overwrites that the m=4 bar performs (fixed), pumpkin wiring comments cited
  OSW:2426 for the :2416 makePumpkin call (fixed in both shared files),
  damsel spec S10 lacked the superseding strikethrough §3 carries (fixed),
  70 pre-existing datapack JSONs carry a UTF-8 BOM (Gson-tolerated, tooling
  hazard — logged as a standalone cleanup task, not batch-3 scope), and the
  postProcess dispatch is a switch STATEMENT so exhaustiveness is not
  compiler-enforced (informational; 33/33 cases verified present, no default
  arm to mask omissions).
- **DSB outcome table now covers 31 of 50** (0-3, 7, 12-24, 26-30, 34, 35,
  37-39, 44, 46, 47). NOTE: the batch-2 entry above previously claimed
  "27 of 50" — that was a miscount; the enumerated types summed to 25
  (verified by constant/case recount, 25 + batch-3's 6 = 31).
- **Batch 4 (2026-08-10, this commit) — structures:** SpiderHangout (Village
  dim, DSB 48, set 19/9, salt 84368; SpiderDriverEnable worldgen gate honored
  in findGenerationPoint per orig OSW:1323-1325, LESS_LAG precedent; silent
  persistent Robot Spider spawn), RedAntHangout (Village dim, DSB 49, 16/8,
  84369; unowned persistent Robot Red Ant), FrogPond (plains, DSB 43 with
  clickedY+1, 46/23, 84370; SAND_SURFACE_MINUS1 reused as the biome-agnostic
  −1-anchor mode), RubberDuckyPond (plains, DSB 40, 17/8, 84371; only the +1
  chest loot-bound — faithful oddity), **plus two structures the DSB sweep
  surfaced as unported and absent from the original 22-list**: HauntedHouse
  (overworld, GD:891-1010, DSB 5 + worldgen ahh-chain OSW:979-997, 17/8,
  84372; 14-slot 50%-per-slot loot JSON) and EnderKnightDungeon
  (dual-dimension End+Mining like EnderCastle, GD:1794-1932, DSB 11, sets
  10/5/84373 + 26/13/84374, new mode LOWEST_GRASS_36 = the 6×6 lowest-surface
  scan with grass-accept and NO −2 sink, per-JSON placement_mode override).
- **Batch 4 — DSB outcome sweep (ITEM-020 complete):** the remaining 13
  non-generator outcomes wired per `dsb_sweep_spec.md`: 9/10/32 direct
  buildNow; 31/42 (+25,0,+25), 36 (+11,0,+7), 41 (+12,0,+9) buildNow with
  offsets canceling the ported generators' internal recentring; 25/45(+1)/33
  via three new CrystalStructures adapters (type 33 redirected to the
  faithful CS builder); 4/6/8 via new public `buildAt` cores extracted from
  Beehive/SmallBeehive/MantisNest features (worldgen paths byte-identical).
  **All 50 DSB outcomes now live.**
- **Batch 4 — parity fixes landed with the sweep:** F3 (royal altar box
  4/56 → 10/59: the down-4 box clipped the v=1..9 dirt skirt and the top 2
  air-clear rows in BOTH paths), F7 (DSB Robot Lab case now pre-offsets
  +5,0,+25 to cancel the port's recentring), F8 (ALIEN_WTF box widened to
  (-20,20,25,6,-22,20): the south Part room's far wall at z=origin−21 was
  ALWAYS dropped on the DSB path, ~1/16 worldgen). Box changes reseed the
  affected pieces' RNG for existing seeds — documented delta. F4:
  CrystalBattleTowerFeature DELETED (dead registration, zero datapack refs,
  invented loot — no-procedural-fabrication rule; superseded by the
  faithful CrystalStructures builder). F9: stale TYPE_ROTATOR_STATION cite
  fixed (DSB:62-64).
- **Batch-4 verify pass: 1 CRITICAL / 10 minor (8 verifiers).** CRITICAL:
  `pickGreenhousePlant` had silently drifted from GD:5067-5125 — case 7
  placed PUMPKIN where the original places reeds/sugar cane (GD:5090-5092)
  and t==19 fell to air though MyRicePlant → ModBlocks.RICE_PLANT exists
  (GD:5123-5125); both fixed (affects worldgen GREENHOUSE + DSB 36). Minor
  fixes: silent spawnPersistent variant (the hangouts' originals are bare
  spawns with no living-sound — the shared helper's ambient one-shot now
  suppressible), SAND_SURFACE_MINUS1 Javadocs note the frog-pond reuse,
  4 cite corrections (EKD OSW span, lowestGrassOrigin :2096, Igloo/
  WaterDragonLair stale CrystalStructures refs), RDS buildForType Javadoc
  modernized. **Logged for close-out (pre-existing, discovered by the
  sweep):** DISABLE_OVERWORLD_DUNGEONS config defined but never read (orig
  OSW:284 gates the whole overworld rotation); the three feature cores drop
  original chest facings (metas 2-5 → all north); bee/mantis feature loot
  substitutes GOLDEN_CARROT/SPIDER_EYE for the original egg items on a
  false "no equivalent" premise. Plus pre-existing F1 (greenhouse doors),
  F2 (white-house door top half), F5 (CrystalMazeFeature dead registration)
  from the sweep's full reads.
- **Build:** full `./gradlew build` SUCCESSFUL all four batches.

---

## Phase D — slice D6a: strong-model structures (2026-08-08)

- **Specs:** `phase_d_reports/d6_extraction/` — six extraction/audit specs, each
  independently verified against the originals before implementation.
- **WGEN-042 — PARTIAL (advanced):** EnderCastle (GD:3207-3623; End
  END_SURFACE placement + Islands i==7 via the new per-JSON placement_mode
  override; DSB 27; 8-entry loot 270), IncaPyramid (GD:3735-4042; write-set
  model for ramp self-reads, PN-018 deviation; DSB 29; 14-entry loot 480),
  KyuubiDungeon (GD:1095-1361; Mining set 26/13; DSB 7; five loot tables,
  totals 110/130, four blaze fill formulas kept distinct), EnderDragonHospital
  (GD:2815-2991; 4 End Crystals via the new spawnEntity helper — NO dragon
  exists in the original; End-only 10/5; DSB 24), MonsterIsland (GD:5170-5240;
  overworld OCEAN_SURFACE, minecraft:ocean only, 42/21; DSB 37), FairyTree/
  FairyCastleTree live path (DSB 0/1 + LessLag shrinks restored). Salts
  84340-84345. Remaining ~16 mechanical structures -> D6b.
- **Robot Lab reconciliation — WGEN-058..061 (new) FIXED:** invented chest
  palette (dropper/dispenser/clock/comparator) -> faithful 23-entry
  `chests/robot_lab.json` (total 755); Robo-Pounder/Robo-Warrior bindings
  unswapped; hangar-before-pillars order restored (the port erased both rear
  sniper spawners every generation); railway/assembly/altar/door hardware
  restored (powered rails, powered crusher lever, quartz stairs/white carpet,
  piston facing, NORTH-facing door pair). Anchor switched to the faithful
  ISLANDS_GRASS; the /locate recentring retained per the audit's own
  recommendation.
- **WGEN-044 — FIXED (audit corrected):** BlockDuplicatorLog was already
  faithful; the missing half was the worldgen seed log (VeggiePatchFeature
  what==5, orig OSW:1915-1916) — restored.
- **WGEN-045 — FIXED:** BlockExperiencePlant's placeholder grower replaced
  with the faithful Trees.ExperienceTree port (live-tick reads legal, kept).
- **WGEN-062 (new) — FIXED:** fairy-tree dispatch scan-exhaustion return
  restored to the original TRUE (suppresses the chunk's follow-ups).
- **Infrastructure:** END_SURFACE + OCEAN_SURFACE placement modes, per-JSON
  placement_mode override, generic spawnEntity helper; the pattern doc gained
  the tree-generator addendum and the "port the FULL return contract" trap.
- **Verification:** four-verifier independent pass over all new code:
  0 critical / 0 major / 4 minor, all resolved pre-commit (ocean anchor
  off-by-one at Y40, robot door 180-degree mirror, Inca Javadoc overclaim,
  WGEN-062). EnderCastle/Kyuubi verifiers: zero findings. Verifiers also
  corrected two D5-assessment miscounts: EnderCastle has THREE loot chests
  (not 4), and DSB types 24/37 are single-call (the "type-24 pair" premise
  was a truncated-read artifact).
- **Notes:** PN-017 (End placement), PN-018 (Inca ramps), PN-019 (ocean
  biome). No new MOD entries (removed inventions were duplicative).
- **Ledger:** 618 IDs (613 + WGEN-058..062), 461 terminal / 157 open,
  reconcile green. **Build:** full `./gradlew build` — see commit.
- **Pending manual tests:** TESTING_CHECKLIST.md section D6a.

---

## Phase D — slice D5: representative structures + SpawnOres pool (2026-08-08)

- **Report:** `phase_d_reports/D5_structures_spawnores.md` (full citation tables);
  extraction specs in `phase_d_reports/d5_extraction/` (basilisk_maze / nightmare /
  enormous_castle / spawn_ores — each independently verified against the originals).
- **Pattern doc:** `phase_d_reports/structure_conversion_pattern.md` — the D6 playbook
  (mechanism decision, LegacyDungeonStructure recipe, RNG stitching contract, loot
  conversion rules, placement-odds table, DSB wiring, verification checklist).

### Structures

- **WGEN-037 — FIXED.** BasiliskMaze ported line-by-line (orig BasiliskMaze.java:30-458)
  as `LegacyDungeonStructure`/`BasiliskMazeGenerator`: randomized-Prim 10x10 maze
  (in-memory wall bitmap replaces the original's read-after-write entrance probes —
  required by chunk stitching), 80 lava + 20 RTP floor traps, iron-ore antechamber,
  pyramid + spiral parkour shaft, 2-4 chests (31-entry list -> `chests/basilisk_maze`,
  weight 495, rolls 5-10; CagedGirlfriend via caged_mob + caged_entity component),
  3 persistent Basilisks (spawnPersistent, no spawner blocks). Mining set 26/13
  (1/665 rotation odds), LOWEST_SURFACE_36 anchor (OSW:2573-2597), DSB outcome 23.
- **WGEN-038 — VERIFIED-CORRECT.** `NightmareDungeon` is dead code in 1.7.10 (never
  instantiated; exhaustive proof in nightmare_spec.md §1) — porting it would invent
  behavior. The live Nightmare structure is the Rookery (below).
- **WGEN-042 — PARTIAL (advanced).** NightmareRookery ported
  (orig GenericDungeon.java:5242-5312): two 26-column drunkard ridges (Z drift carries
  across passes), 4 side-bulge rolls per pillar block, PitchBlack spawner + chest caps
  on h>=19 pillars, 10-entry loot (weight 270, rolls 4-8), ISLANDS_GRASS anchor with
  LessLag gate (OSW:2253-2274), island set 44/22 (1/1900), DSB outcome 38.
  Remaining ~20 structures -> D6.
- **Challenge Towers reconciled (WGEN-051..056 + ITEM-066, all new, all FIXED):**
  the existing makeEnormousCastle/Q port diffed end-to-end against GD:191-786/6393-6987.
  Removed inventions: level-6 lock (roll restored, GD:202-205) and scaffolding columns
  (archived MOD-012). Restored: faithful level1-5 chest lists as
  `chests/challenge_tower_level1..5` (totals 165/235/235/255/1285 assert-verified),
  "Jumpy Bug" = TrooperBug spawners, chest facings (meta 5/4/3/2 -> room centre),
  faithful placement (grass anchor + jitter + LessLag; sets corrected 44/22 -> the
  C7-approved 36/18; bounding box extended so the level-6 buried worm ring
  (x,z -28..+55, GD:362-374) is no longer chunk-clipped), functional
  the_prince/the_princess spawn eggs in the prize chests AND the Queen/Teen/Adult
  drops (invented trophy items removed). DSB outcomes 2 + 47.
- **WGEN-057 — FIXED (found in passing).** mantis_nest and royal_trees structure sets
  shared salt 84312; royal_trees re-salted 84332.
- **Infrastructure:** asymmetric piece bounding boxes, per-type PlacementMode
  (SURFACE_CENTER / LOWEST_SURFACE_36 / ISLANDS_GRASS), `buildNow` live-build entry
  (level RNG, unclipped window), facing-aware + loot-table chest placement,
  `spawnPersistent`. Per-structure generator classes keep LegacyDungeonPiece flat.

### SpawnOres pool + egg recipes

- **WGEN-005 — FIXED.** 106 new OreGenericEgg blocks (119-row verified master table);
  `SpawnOresPoolFeature` reproduces the original roll exactly (28+nextInt(20/30),
  1/20 +30, LessOre÷3, Y 50..127 discard, 7-in-104 rare tier, exact switch orders)
  for overworld + Utopia/Village/Chaos + Mining x3 (CP2:191-195). Original
  "Ancient Dried <Mob> Spawn Egg" names restored everywhere (incl. the 13
  previously-deviating existing blocks). Interim PN-010 artifacts retired:
  dragon/kraken features (incl. never-wired _dim/_mining orphans),
  add_boss_spawn_blocks, and the invented ancient_dried_egg block/worldgen
  (archived MOD-013). Generators: `tools/d5_gen_spawn_ores.py`.
- **ITEM-062 — FIXED (closes the last remainder).** All 116 water-bucket
  spawn-block->egg recipes + 3 nine-part combines (OSM:2665-3021). Vanilla eggs for
  vanilla mobs (ender_dragon/iron_golem/snow_golem/wither all exist since 1.20.5 —
  verified in the 1.21.1 client jar); CriminalEgg -> band_p_spawn_egg (WGEN-017);
  EnchantedCowEgg -> enchanted_apple_cow_spawn_egg (consolidation target).
- **ITEM-020 — PARTIAL (advanced).** DSB outcome table now covers 2/21/22/23/38/47.

### Notes & ledger

- **PARITY_NOTES:** PN-010 closed; PN-015 (seed-stable structure RNG) and PN-016
  (SpawnOres replaceables-tag/step mapping) added.
- **MODERNIZATION_NOTES:** MOD-012 (tower QoL pack), MOD-013 (rehydration block).
- **Observation for Phase E (not acted on):** Phase-14 invented entities
  (APPLE_COW/GOLDEN_APPLE_COW/VAMPIRE_BUTTERFLY; EnchantedCow renamed into
  ENCHANTED_APPLE_COW; BABY_DRAGON alongside SPYRO) — invented-content ruling
  candidates.
- **Ledger:** 613 IDs (605 + WGEN-051..057 + ITEM-066), 454 terminal
  (431 FIXED + 22 VERIFIED-CORRECT + 1 DEFERRED) / 159 open;
  `tools/ledger_reconcile.py` green (TOTAL_EXPECTED 613).
- **Verification pass:** 4 independent verifiers re-derived every D5 number
  from the originals (details in the report §9). Caught + fixed pre-commit: a
  LESS_ORE Boolean-vs-int compile error in SpawnOresPoolFeature; the stale
  `ancient_dried_egg` entry in `data/minecraft/tags/block/mineable/shovel.json`
  (would have broken the vanilla tag at load); the invented
  `extracting_trex_dna.json` Extractor recipe (ADE-coupled, no original
  counterpart — deleted). One documented delta: LOWEST_SURFACE_36 can't probe
  under overhangs where modern terrain exceeds Y128 (impossible in 1.7.10's
  128-tall world). Rookery and tower verifiers: zero findings.
- **Build:** full `./gradlew build` — see commit.
- **Pending manual tests (in-game):** appended to TESTING_CHECKLIST.md §D5:
  - `/locate structure orespawn:basilisk_maze` (Mining): pyramid marker, parkour
    shaft, maze solvable W->E, 3 aggressive persistent Basilisks, east-wall chests
    roll the 31-entry table, lava/RTP floor traps live.
  - `/locate structure orespawn:nightmare_rookery` (Islands): jagged spire cluster,
    ~10% of spires capped chest-under-spawner, Nightmare spawners live.
  - Challenge Towers (Islands): height varies by the restored roll (short towers
    common, full 6-floor towers ~28%); no scaffolding; chests face the room centre;
    level-5 room loot includes the 83-egg jackpot; prize floor (level-6 towers only)
    gives ROYAL gear + a FUNCTIONAL Prince/Princess spawn egg; buried worm-spawner
    ring extends well past the west stair without chunk seams.
  - Random Dungeon Spawner block: outcomes 2/23/38/47 build King tower / maze /
    rookery / Queen tower at the block.
  - SpawnOres: new chunks Y50+ carry frequent varied spawn-ore veins in overworld,
    Utopia, Village, Chaos, Mining x3 (none in Islands/Crystal-beyond-its-own-pool);
    breaking drops the block + 50% 5-9 XP; every block + water bucket crafts its
    mob's egg (bucket returned); 9 Mobzilla/King/Queen parts combine to full blocks.
  - LessOre=1: spawn-ore veins ~1/3; LessLag=1: rookery/towers ~half frequency.

---

## Phase D — slice D4: items/blocks/small-entity batch (2026-07-02)

- **Report:** `phase_d_reports/D4_items_small_entities.md` (full citation tables).
- **Scope:** the approved D4 pool (ITEM-022/029/057/060/061/063/065, ANIM-016,
  ENT-A-052/088, ENT-D-010/052, ENT-K-007/011/047/048/076/080/084,
  ENT-S-025/034/036/047/059/078/085) plus the reconcile pool's D4-assigned
  PARTIAL remainders (ENT-A-001/098, ITEM-053/062, ENT-D-011/039/041).
  ENT-D-044 was already closed in D3 and was not redone.

### Items & blocks

- **ITEM-063 — FIXED.** All 8 dispenser behaviors (orig OreSpawnMain.java:5755-5773
  + the MyDispenserBehavior* one-liners): IrukandjiArrow (pickup allowed), WaterBall,
  SunspotUrchin, Acid, IceBall, DeadIrukandji, LaserBall, and the shared rock behavior
  stamped onto all 12 rock items — `ModDispenserBehaviors`, velocity 1.1 /
  inaccuracy 6.0 / +0.1 vertical bias / aux 1002, per vanilla BehaviorProjectileDispense.
  Projectile `(Level, x, y, z)` constructors added where missing.
- **ITEM-029 — FIXED.** Special-food effects per orig ItemSunFish.java:29-48:
  Butter Candy Speed+Jump 2000t, Cooked Bacon Regen+Strength 2000t, Crystal Apple
  Regen+Strength 3000t, Heart Regen IV / Strength III / Fire Res III / Resistance II
  6000t + Speed/Jump 5000t. Heart display name corrected to the original "Love".
- **ITEM-057 — FIXED.** The armor-set XP effect was already live via the ITEM-040
  handler (C6); D4 closed the item half: `ItemExperienceTreeSeed` placement/consumption
  ported faithfully, and the port's invented leaf-harvest mechanic removed from
  `BlockExperienceLeaves`. The experience-tree worldgen body is WGEN-045 (D5).
- **ITEM-022 — VERIFIED-CORRECT.** RockBlock is dead code in 1.7.10 — the class is
  never instantiated or registered anywhere; no block form existed in-game.
- **ENT-K-076 — FIXED.** RockBase death drop (one rock item matching the mob's type)
  restored, and a port-wide 0-vs-1-based rock-type mismatch fixed: `ItemRock`
  registrations realigned to the original 1-12 scheme so `EntityThrownRock` damage and
  `RockBase` placement resolve the correct type.
- **ITEM-060/061 — FIXED.** `skate_bow.json`; `chest_from_crystal_planks.json`
  (orig :3083/:3209). Audit correction on ITEM-061: field_151135_aq at :3084-3085 is
  the 1.7.10 wooden door (2x3 plank shape) — the port's existing
  `oak_door_from_crystal_planks.json` was faithful all along; there is no piston recipe.
- **ITEM-062 — PARTIAL (narrowed).** Six diffed-absent recipes added (skate bow, chest,
  red bed, raw corn dog, bucket from pink-tourmaline ingots, cobweb from string).
  Remainder: the 116 water-bucket spawn-block→egg conversions
  (OreSpawnMain.java:2667+), blocked on WGEN-005's ~105-type SpawnOres pool → D5.
- **ITEM-065 — DEFERRED.** The orig config-file per-tier weapon/armor/ore overrides
  can't be replicated against NeoForge's frozen item registries without registry
  mutation; original default values stay hardcoded (verified in earlier slices).
  Documented as PN-013; config system archived as MOD-011 (2.0 candidate).

### Small entities

- **ENT-K-007 — FIXED.** Kyuubi `fireImmune()` (orig Kyuubi.java:47-48) — no more
  self-damage from its own fire.
- **ENT-S-025 / ENT-S-047 — FIXED.** SpitBug and Triffid cactus + fall immunity.
- **Drops — ENT-A-088 / ENT-D-052 / ENT-K-084 / ENT-S-034 / ENT-K-011 / ENT-A-098
  FIXED.** chipmunk.json (incl. tamed-only poppy per the established in-code
  convention), gold_fish.json, rubber_ducky.json, stinky.json rebuilt from the orig
  tables; Lavafoam Nether XP bonus 5+nextInt(5)+nextInt(5) via `getExpDrop`
  (orig Lavafoam.java:110-116); Coin jackpot's empty CoinEgg slot filled with the
  ported coin spawn egg (closes the C1 PARTIAL).
- **ENT-S-085 — FIXED.** WormLarge theft: 1-in-4 helmet-else-chestplate
  (orig WormLarge.java:210-230) + independent 1-in-4 held item (:231-238), stack
  zeroed and scattered; PlayNicely gate (:192-198); 8-block non-creative targets
  (:199-202); death drops (:352-377) and the "Large Worm" spawner bypass (:263-309).
- **ENT-S-078 — FIXED.** WormSmall surface-block check at every burrow step
  (orig WormSmall.java:107-110/124-127/139-142, tall grass counts as air :104-106);
  1-in-6 boots theft (:188-195); night-only spawn (:214-216).
- **ENT-K-047/048 — FIXED.** Peacock termite hunting (nearest visible Termite
  :202-237, flat 6.0 damage :166-169, 1-in-200 revenge clear :181-200) and egg laying
  (clear-air / first-half-of-day / 50≤y≤100 / ≤2 buddies gate :101-119 — restores the
  never-called `findBuddies()` — 1-3 eggs :171-179,197-199); Crystal Apple breeding.
- **ENT-D-010 — FIXED.** EasterBunny: carrot taming + mob-egg laying with the full
  115-entry mob→spawn-egg lookup (script-extracted from the orig table and mapped to
  the port's spawn-egg items); natural spawns Easter-gated (see ANIM-016).
- **ENT-S-059 — FIXED.** UltimateFishHook rebuilt on vanilla `FishingHook` with ATs
  widening `nibble`/`currentState`/`catchingFish`/`shouldStopFishing`
  (accesstransformer.cfg): orig weighted junk/treasure/vanilla-fish/OreSpawn-fish/
  lava-fish pools in `getCatch` with Luck-of-the-Sea/Lure scaling; lava fishing
  (buoyancy, bite cycle, lava particles); `fireImmune()` hook (orig :76-77) spawning
  `EntityLavaLovingItem` for lava catches; XP orb on retrieve; random durability
  damage + level-30 enchant on caught gear. Invented +3 luck / +2 lure-speed
  constructor bonuses removed; renderer switched to the vanilla `FishingHookRenderer`.
- **ENT-A-052 — FIXED.** BetterFireball `canHitEntity` pass-throughs (other
  BetterFireballs, Mothra, GodzillaHead, Royalty, and Player/Dragon when `notme` set)
  + HP-halving exemptions (Royalty, Godzilla, GodzillaHead, PitchBlack, Kraken).
- **ENT-A-001 — FIXED (closes the C1 PARTIAL).** TrooperBug/SpitBug acid immunity in
  `LaserBall.onHitEntity` (acid discards on impact).
- **ITEM-053 — FIXED (closes the C6 PARTIAL).** Shoes & GameController throwables:
  new `ItemShoes` drives all 5 items; full per-target damage table incl.
  Girlfriend/Boyfriend 1.0f and the Valentine's-Day 10.0f override; reddust +
  snowballpoof impact particles.
- **ENT-S-036 / ENT-K-080 — FIXED (verification only).** SunspotUrchin fire placement
  was restored in C6 (ITEM-053's projectile pass); Rotator `wasSpawnered` persistence
  was implemented in D1 (set in checkSpawnRules, NBT-persisted, consumed by the
  despawn exemption). Both ledger entries were simply never updated.

### Seasonal gates (ANIM-016; closes ENT-D-011/039/041)

- **ANIM-016 — FIXED.** New `util/SeasonalDates` evaluates isHalloween/isValentines/
  isEaster from `LocalDate` at check time — the orig froze the flags once at init via
  GregorianCalendar; live evaluation is the deliberate deviation logged as **PN-014**.
- **Halloween (ENT-D-039/041 closed):** the 22-biome Ghost/GhostSkelly w15 3-6 block
  added as `halloween_ghosts.json` (20 modern biomes after mapping), runtime-gated in
  `checkSpawnRules` with the 5 year-round biomes exempt
  (`OriginalSpawnGates.inYearRoundGhostBiome`).
- **Easter (ENT-D-011 closed):** EasterBunny natural spawns denied unless
  `isEaster()`.
- **Valentine's:** Girlfriend becomes the original giant angry variant — 2.5x8.0
  dimensions, 800 HP, `girlfriendv.png` texture (5x render scale), MyValentineTarget
  goal (players + Boyfriends while angry, owner/tamed-pet filtered), inWall damage
  immunity, `o_hurt` ambient; Rose Sword 1-in-4 cure (clears target, resizes, drops
  Love items), persisted as `feelingBetter` NBT and synced for the client renderer.

### Verification & ledger

- **Ledger:** 442 terminal (420 FIXED + 21 VERIFIED-CORRECT + 1 DEFERRED) / 163 open,
  total 605 (`tools/ledger_reconcile.py` green; patch script `tools/d4_ledger_patch.py`).
- **Build:** full `.\gradlew.bat build` → green (BUILD SUCCESSFUL, 28 tasks).
- **Pending manual tests (in-game):**
  - Dispensers fire all 8 projectile types; dispensed rocks keep their type and damage.
  - Butter Candy / Cooked Bacon / Crystal Apple / "Love" grant their potion effects.
  - RockBase mobs drop the matching rock on death; thrown rocks deal per-type damage.
  - New recipes craft: skate bow, chest / red bed from crystal planks, raw corn dog,
    bucket from pink-tourmaline ingots, cobweb from string.
  - Kyuubi stands in its own fire unharmed; SpitBug/Triffid ignore cactus and falls.
  - Chipmunk (poppy when tamed), GoldFish, RubberDucky, Stinky drops; Lavafoam gives
    bonus XP only in the Nether; Coin jackpot can yield the coin spawn egg.
  - WormLarge steals helmet/chestplate/held item and scatters them; WormSmall steals
    boots at night and pops when surfacing.
  - Peacock hunts termites, lays 1-3 eggs in the morning at y 50-100, breeds with
    Crystal Apple.
  - System date on Easter: EasterBunny spawns naturally, tames with a carrot, lays
    mob eggs from the 115-entry table.
  - Ultimate Fishing Rod: bobber floats and catches in lava (item survives), custom
    junk/treasure/fish pools, caught gear arrives damaged + level-30 enchanted,
    XP orb on catch.
  - System date Oct 31: Ghost/GhostSkelly spawn across the Halloween biome list
    (5 biomes keep them year-round). Feb 14: Girlfriend spawns giant and hostile;
    Rose Sword hits eventually cure her; thrown shoes deal 10 damage.

### D4 checkpoint rulings (owner, 2026-07-03)

- **ITEM-065 DEFERRED — approved.** The deferral recorded above now carries the
  owner's explicit approval (the plan's requirement for any DEFERRED terminal
  state). PN-013 and MOD-011 stand as written; PN-013 header updated to APPROVED.
- **PN-014 — approved as an explicit behavior deviation.** Live `LocalDate`
  seasonal gates (vs the original's frozen at-init GregorianCalendar) carry the
  owner's sign-off in their own right, not just the audit's recommendation;
  PN-014 header updated to APPROVED.

---

## Phase D — slice D3: ranged attacks + Prince-family flight (2026-06-13)

- **Report:** `phase_d_reports/D3_ranged_flight.md` (full citation tables).

### Small-entity batch

- **BUG-032 — new finding, FIXED.** 39 aggregate sound events the original's sounds.json
  defines (e.g. `mothrawings` → mothrawings1/2/3, `b_fight`, `b_taunt`, `o_hurt`,
  `robot_living`) were missing from the port's sounds.json while code referenced the
  aggregate names — every such sound silently played nothing. All 39 added.
- **ENT-S-058 — FIXED (audit corrected).** Orig UltimateArrow.java has no
  ignite/knockback/trail; the real behaviors ported: UltimateSwordPvp-gated
  heal-instead-of-damage (+1 HP, arrow-hit sound, discard) for players / Girlfriend /
  Boyfriend / tamed pets, and `canHitEntity` passthrough for Elevators and ridden
  Cephadrome/Dragon/AbstractHorse.
- **ENT-A-055 — FIXED.** Boyfriend weapon system: `RangedAttackMob` +
  `RangedAttackGoal(1.25, 20, 10.0f)`; UltimateArrow when holding the Ultimate Bow
  (2.0f, 1-in-4 crit) else Shoes id 6 (1.8f/4.0f); armed melee in `customServerAiStep`
  (25t cooldown, Big-Bertha 10-block reach, `b_fight`, `b_taunt` at 4-7 blocks,
  1-in-100 revenge forgiveness). Invented BOYFRIEND_BRO_MODE combat gate removed —
  orig `bro_mode` (OreSpawnMain.java:1481) is voice-only (ENT-A-058 scope); archived
  as MOD-010. The config key itself stays (it is original).
- **ENT-D-049 — FIXED.** Girlfriend: same system with `o_` sounds, Shoes id 2-5,
  1-in-200 target clear (orig Girlfriend.java).
- **ENT-A-019 + ENT-A-018 — FIXED.** AttackSquid `watercanon` (1-in-5 roll, InkSack
  1-in-3 else WaterBall, 1.4f/5.0f, muzzle offsets, orig yHeadRot/yRot aiming quirk
  preserved); melee restored to the original double roll (`nextInt(4)==0 ||
  nextInt(5)==1` = 40%).
- **ENT-A-062 — FIXED.** Brutalfly `attackWithSomething`: Easy SmallFireball / Normal
  50-50 / Hard BetterFireball, +1 HP self-heal per shot, shoot odds 1-in-3 (1-in-2
  Hard); invented melee-on-player replaced by the original ranged-only engagement.
- **ENT-D-044 — FIXED.** GiantRobot `fireLaserBall`: 0.5 rad aim gate (melee nested
  inside per orig :256-263), reload 10/25 keyed on distSq 100, `setSpecial()` far
  shots, original volumes/pitches.

### Prince family (BOSS-019/021/022/023 baby · BOSS-028 teen · BOSS-034 adult · BOSS-039/040/041 princess; PN-002 closed)

- **ThePrince (baby) — BOSS-019/021/022/023 FIXED.** Full `do_movement`
  (orig :585-725: activity cycling 1/100 with 1/20 fly, owner-flying 1.75×/3.5×
  speedups, flee-when-hurt retreat, signum steering 0.5/0.7 prods, yaw/3), canon trio
  `firecanon`/`firecanonl`/`firecanoni` (muzzle xz 3.0 / y 1.0, 0.5 rad head-bearing
  gate, 5-12 block band, DATA_FIRE + dry gate), ice/flint fire toggles with messages,
  okToGrow gate dropped from natural growth (kill>25 && fed>10 && day>10), noPhysics
  restored for activity 2 (BUG-010 interim disable lifted), 0.6 y-damping + water
  buoyancy, revenge forgiveness corrected to `setLastHurtByMob(null)`.
- **ThePrinceTeen — BOSS-028 FIXED.** `fly_without_rider` (orig :677-834): vertical
  damping ternary (unreachable 0.61 arm kept verbatim), 1-in-7 combat roll, 8-block
  bite + 5-19t fly-away, `shoot_somethingAt` volley <20 blocks, owner-anchored flight
  targets (5-18 / 0-5 flying / 16-25 wild) requiring line of sight, terrain-following
  lift (0.05/block × 0.05), signum steering + direct `move()`. `always_do`
  (orig :435-461): 2 HP regen 1/250, 1/250 target forgiveness, owner creative-flight
  follow, 1/50 settle roll (1/15 keeps flying). Ground spotting 1-in-10
  (orig :398-405). While flying, vanilla goals/physics are bypassed (orig :849-857 —
  `aiStep` override; travel skipped). `hurt()` rewritten per orig :343-393 (cactus/
  fire/lava/inWall immune, fireballs pop, teen/Spyro immune, sit-break + take-flight,
  hurt_timer 20, tame-vs-player no-retaliate). Wing sound every 20t; owner >20 blocks
  launches flight. Interactions per orig :1127-1273: diamond block now steal-tames,
  owner-only gate, beef full heal, food ×10, ice/flint toggles, **DIAMOND teen→baby
  regression restored** (`ThePrince.setOkToGrow()` added), sit toggle grounds
  (activity 0). Targeting per orig :496-555: 25/20/25 box, PlayNicely/Peaceful/royalty
  gates, prey = Monster/Mothra/Kraken/untamed Leon/WaterDragon/GammaMetroid.
- **BOSS-029 — RE-FIXED (audit corrected).** The Phase C note "orig has no shrink-back"
  was wrong: orig ThePrinceTeen.java:1230-1250 has a DIAMOND regression. The C fix
  correctly removed the invented gold-ingot item; D3 restores the faithful diamond one.
- **BOSS-045 — new finding, FIXED.** Teen's invented cake growth shortcut removed
  (duplicated the diamond block's function; orig has no cake branch).
- **ThePrinceAdult — BOSS-034 FIXED.** Same brain with adult numbers
  (orig :657-814/415-441/389-413): 1-in-6 combat roll, 10-block bite, volley <~24
  blocks (muzzle xz 6.0 / y 3.5), spreads 8-23 / 0-11 / 20-34, 5 HP regen, wing sound
  every 30t, owner >30 blocks launches flight, inWall hurt = no damage but take-flight.
  Interactions per orig :1109-1249 (all <36 distSq): owner-only gate, beef full heal,
  food ×10, ice/flint toggles, **DIAMOND adult→teen regression restored**, sit toggle.
- **BOSS-046 — new finding, FIXED.** Adult's invented cake shortcut + gold-ingot
  regression removed (both duplicated original diamond-item features).
- **ThePrincess — BOSS-039/040/041 FIXED.** `do_movement` identical to the baby's;
  canon trio at baby scale; noPhysics for activity 2 + 0.6 damping + buoyancy; food
  heal nutrition ×10 (invented fedCount++ dropped); ice/flint toggles with
  Princess-specific messages; diamond block steal-tames per orig. Melee fixed to the
  original 9.0 via `doHurtTarget` (+kill counting) — clarifies BOSS-038: the 10.0 the
  audit "verified" is the attribute (orig :102), but orig melee used
  `getAttackStrength()`=9. **Power system ported** (orig :518-628): attack_level
  +1/tick (+4 in combat, 0 while extinguished), DATA_POWER synced every 10 steps,
  client firework-spark aura >400, discharge >500 → 3 PurplePower orbs (type 1-3,
  3× her motion) in combat, else the terraforming bloom (5 column probes under
  mobGriefing: flowers incl. the 6 OreSpawn kinds on grass, dirt→grass, stone→dirt
  cover, sand→cactus/dirt, lava→water, plus 2 Butterfly/Cockateil hatches —
  orig "Bird" = Cockateil, OreSpawnMain.java:3831).
- **PN-002 — CLOSED.** All four royals fly with the original noPhysics mapping; the
  BUG-010 interim disable is fully lifted (MOD-003 remains the 2.0 candidate).
- **Mapping deltas (non-player-visible):** 1.7.10's raw-block flower/terraform writes →
  `setBlockAndUpdate`; still/flowing lava (two 1.7.10 blocks) → the single modern lava
  block, both becoming water; teen/adult flight bypass implemented as an `aiStep`
  override (clients keep vanilla lerp, matching the original's hand-rolled client lerp).
- **Ledger:** 410 terminal (390 FIXED + 20 VERIFIED-CORRECT) / 195 open, total 605
  (602 + BOSS-045 + BOSS-046 + BUG-032; `tools/ledger_reconcile.py` green).
- **Build:** `.\gradlew.bat compileJava` → green; full `.\gradlew.bat build` at commit.
- **Pending manual tests (in-game):**
  - Boyfriend/Girlfriend: hand them an Ultimate Bow (arrows fly, heal allies when PvP
    off) vs. no bow (shoes fly); melee sounds b_fight/o_fight; taunts at 4-7 blocks.
  - AttackSquid: ink/water projectiles beyond 3 blocks; melee inside.
  - Brutalfly: fireball type follows difficulty; heals itself while strafing players.
  - GiantRobot: laser volleys only once its head faces you; slower, special lasers
    from >10 blocks.
  - Prince family: babies/princess take off (1-in-2000 per tick at idle) and land;
    hurt pets at <25% HP flee airborne; canon trio fires only in the 5-12 band while
    lit; ice block/flint toggle the fire with chat messages; teen/adult fly to a
    distant owner, bite-and-break-off in combat, wing flaps audible; diamond
    regressions teen→baby and adult→teen work; princess blooms terrain at peace and
    vents PurplePower in combat (sparkle aura when charged).

## TESTING_FINDINGS (GameTest suite, 2026-08-10)

Triaged run: **145 GameTests, 48 failed**. Triage split: **24 port-defect findings**
below (TF-001..TF-024, spanning 25 test methods; 4 flagged expected_red),
**19 test-infrastructure fixes** applied to TEST CODE ONLY (src/gametest — no
src/main change), and **6 harness-limit reclassifications** returning checklist
items to MANUAL_ONLY (listed after the findings). Sixteen HIGH findings
(TF-002..TF-016, TF-018) share one systemic root cause: the two global loot
modifiers (add_ruby_to_dungeon / add_amethyst_to_dungeon) carry only a
random_chance condition and NO loot-table scoping, so ruby/amethyst are injected
into EVERY loot roll game-wide — the original scoped these gems to three vanilla
ChestGenHooks pools only (OreSpawnMain.java:5391-5403). Canonical mechanism
writeups: TF-009 (entity/block side) and TF-010 (structure-chest side + missing
companion injections). All findings were logged **status OPEN**; the
user-approved fix batch (2026-08-10, commit follows this edit) resolves them
as follows — **TF-001..TF-022 and TF-024 are now FIXED/CLOSED; TF-023 and
TF-025 remain OPEN**:

- **Bundle A (TF-002..TF-016, TF-018 + the GLM halves of i014_egg/i145/i146):**
  gem GLMs scoped per-table with exact per-table chances derived from the
  1.7.10 weighted pools (add_ruby/amethyst_to_{simple_dungeon 0.1875,
  jungle_temple 0.1667, desert_pyramid 0.1154} — split from a shared mean at
  batch-verify); codec now decodes item/min_count/max_count/chance; single
  chance application; thunder_staff/ant_robot_kit/spider_robot_kit injections
  added (0.125/0.1667/0.1154). CTOR-ORDER CORRECTION: the TF-009/TF-018 fix
  texts below prescribe "1-3/1-2 counts" — that misread 1.7.10
  WeightedRandomChestContent(stack, min, max, WEIGHT): (stack,1,1,3) is
  min=max=1 at WEIGHT 3, so the shipped min_count=max_count=1 JSONs are the
  faithful form; do NOT "restore" 1-3/1-2 stack counts.
- **Bundle B (TF-017):** all 14 tables re-gated with the modern silk schema
  (verified byte-identical in structure to vanilla 1.21.1); ore_ruby/
  ore_amethyst gems set_count 1-2.
- **Bundle C (TF-022):** OreRuby (shared by both gem ores) moved to getExpDrop,
  5+nextInt(5)+nextInt(5) every break, no Y gate.
- **Bundle D (TF-019/TF-020):** dead-bush release + ICE extinguish restored
  per orig Dragon.java:1261-1290; TNT falls to the generic sit toggle.
- **Bundle E, greenhouse half (TF-021):** LegacyDungeonPiece piece writes now
  carry UPDATE_KNOWN_SHAPE (FLAG_PIECE_WRITE = 2|16) — live-path parity with
  the original's update-free setBlockFast; frog-pond half deferred to TF-025.
- **TF-001 (+ TEST-005 closed):** OwnerFollowAnyNavGoal (vanilla FollowOwnerGoal
  minus the 1.21 navigation-type ctor check) on WaterDragon with the original's
  parameters (orig WaterDragon.java:71 — MyEntityAIFollowOwner(2.0, 10, 2),
  which had no navigation restriction).
- **TF-024 CLOSED (docs amended per user ruling):** the instant diamond-block
  transform is faithful (orig ThePrince.java:195-206 + :556-568); the bug004
  test now asserts the faithful flow and items.json i002 is corrected.
- **TF-023 CLOSED (harness, port exonerated — residual triage 2026-08-10):**
  the robot always spawned; freshly force-loaded chunks keep their entity
  sections HIDDEN until the main thread pumps the FullChunkStatus promotion
  (ChunkHolder.scheduleFullChunkPromotion → entityManager.updateChunkStatus),
  so the same-tick AABB query missed it — the chunk-border correlation was
  the section boundary, not the spawn. Test now pins a FORCED ticket +
  pre-loads and delays the body. The identical mechanism explained
  i141's "0 End Crystals" and item001_005's far-region query misses.
- **Residual-red triage (post-batch run, 29 → 10 → all accounted):** the 7
  non-expected reds were ALL test infrastructure — i115/i131 still drove the
  removed spawnAfterBreak site (re-pointed to getExpDrop); item001_005
  relocated in-template (HIDDEN-section query race); i122/i126 counted
  STACKS where the docs count PICKS (vanilla createStackSplitter splits
  over-stack picks — caged_mob 2-4 → singles, zoo_keeper 10-16 → singles —
  exactly as 1.7.10's generateStacks did in-chest; switched to
  getRandomItemsRaw); dsb_igloo counted container slots vs pool successes
  (shuffleAndSplitItems scatters); red_ant per TF-023 above.
- **Residual-audit completion (Bundle C extension, applied):** the
  spawnAfterBreak XP audit closed the class — the original had exactly 8
  XP-popping block classes; OreCrystal (crystal_coal) and OreCrystalCrystal
  (crystal_crystal/tigers_eye_ore) were the last two still on the dead path
  (orig OreCrystal.java:71-77 / OreCrystalCrystal.java:66-72 — 5..18 XP
  below y40). Migrated to getExpDrop with the Y gate, mirroring OreUranium —
  the same user-approved ITEM-003/TF-022 mechanism, applied as Bundle C's
  completion (no test currently covers these three ores' XP; flagged for a
  follow-up assert).
- **TF-026 FIXED (2026-08-11, live-session blocker — ticking-entity crash):**
  WaterDragon's melee preset shipped innerAttackRoll = 0, so the goal's
  unconditional `nextInt(innerAttackRoll)` threw "Bound must be positive" on
  the first in-reach attack, killing the server — reachable only since
  TF-001 made the entity spawnable (crash-2026-08-11_05.46.43-server.txt).
  Orig WaterDragon.java:597/603: cadence nextInt(5)==1, then
  nextInt(4)==0 || nextInt(5)==1 → correct Params (…, 5, 4, 5, 200, …).
  Fixed the preset (DinosaurMeleeAttackGoal.Presets.waterDragon) and added
  a construction-time positive-rolls guard to BugMeleeAttackGoal.Params
  (forgetTargetRoll excluded — 0 = "never forget", gated at :118). Suite
  green 143/143 post-fix.
- **TF-025 CLOSED (2026-08-11, user-ratified docs amendment — "the
  fluid-mechanics analysis is decisive"):** spec S9 amended to the observed
  stable end-state, i166's cascade asserts re-pointed (one-block lip flow at
  +2, cascade dead at +3, no rim water), tf025_diag deleted. Original entry
  (root-cause isolation) follows: the
  diagnostic dump (t=0..250) shows a faithful build settling STABLE: all
  cross/riser/sheet blocks placed exactly per GD:6018-6039, flow advancing
  one block to (+2,+1) then dying — water above water becomes falling flow,
  which never spreads horizontally, in modern AND 1.7.10 fluid logic. The
  spec-S9 "spills past the rim" expectation was an extraction-time
  inference (nothing in GD:6018-6039 generates a spill; it is emergent
  fluid behavior). PROPOSAL: amend frog_pond spec S9 + re-point i166's
  cascade assert to the observed stable end-state (sources + the one-block
  lip flow, air at the rim) — pending the user's in-game memory of the
  1.7.10 pond as the final arbiter; then delete tf025_diag.

- **TF-001 (HIGH)** `cephadrome_targets_and_kraken_bonus` — WaterDragon is
  unspawnable: its ctor throws IllegalArgumentException "Unsupported mob type for
  FollowOwnerGoal" (vanilla 1.21 FollowOwnerGoal rejects water-bound navigation);
  this is the already-logged TEST-005 defect, hit here in phase 3 because
  WaterDragon is a documented Cephadrome target. Orig:
  reference_1_7_10_source/sources/danger/orespawn/Cephadrome.java:404-432,515-573;
  phase_c_reports/C1_entities_A_C.md:63. Port:
  src/main/java/danger/orespawn/entity/WaterDragon.java:84 (ctor throw);
  TESTING_CHECKLIST.md:386-394 (TEST-005, OPEN). Fix: per TEST-005 — replace
  FollowOwnerGoal with a navigation-agnostic follow goal (copy without the ctor
  navigation check, or an amphibious variant). **EXPECTED_RED** (stays red until
  TEST-005 is fixed; TEST-005 cross-link comments added). Status: OPEN.
- **TF-002 (HIGH)** `i033_cockateil_variants_and_type5_ruby` — defect half (infra
  empty-stack half already fixed in test code): the unscoped add_ruby GLM injects
  ruby into cockateil rolls regardless of BirdType, killed_by_player, or the 1/3
  chance, so the hard zero-probability negatives (type-5 without killed_by_player
  x60, type-2 x60, single-pick ruby AND feather) fail with ~99% probability per
  run. Orig: Cockateil.java:242-248 (ruby only when BirdType==5 && killedByPlayer
  && nextInt(3)==1); OreSpawnMain.java:5391-5403. Port:
  src/main/resources/data/orespawn/loot_modifiers/add_ruby_to_dungeon.json:1-12
  (unscoped). Fix: TF-009 GLM scoping/codec fix. Status: OPEN.
- **TF-003 (HIGH)** `i038_gazelle_ostrich_tamed_vs_untamed_kills` — untamed
  gazelle kill dropped {ruby=1, beef=1}: beef 1 is in the documented 0-2 core, the
  ruby is GLM-injected into the death roll (foreign-items assert; the
  OreSpawnTamed-branched JSON was asserted clean in the same test). Orig:
  Gazelle.java:337-352; OreSpawnMain.java:5391-5403. Port:
  add_ruby_to_dungeon.json:1-12 (unscoped). Fix: TF-009. Status: OPEN.
- **TF-004 (HIGH)** `i055_c1_drop_tables` — purest proof of the unscoped GLM:
  UNTAMED camarasaurus rolled drops from a table whose only pool is gated on
  OreSpawnTamed:1b, via a direct LootTable.getRandomItems call with no world
  involvement (contamination impossible; GLM hook verified at patched
  LootTable.java:136 CommonHooks.modifyLoot). In-game: every drop-nothing kill has
  ~13.4% chance of dropping a gem. Orig: Camarasaurus.java:303-312;
  OreSpawnMain.java:5391-5403. Port: add_ruby_to_dungeon.json +
  add_amethyst_to_dungeon.json (unscoped). Fix: TF-009. Status: OPEN.
- **TF-005 (HIGH)** `i058_c4_drop_tables` — worm_small rolled items despite the
  documented EMPTY table (pools.size()==0 asserted in the same test, pure
  getRandomItems roll); items are GLM-injected into the empty result list. Orig:
  WormSmall.java:230-232 (drops nothing); OreSpawnMain.java:5391-5403. Port: both
  GLM JSONs (unscoped). Fix: TF-009. Status: OPEN.
- **TF-006 (HIGH)** `i059_d4_pet_drop_tables_and_stinky_kills` — untamed Stinky
  kill dropped {ruby=1}; documented untamed Stinky drops NOTHING (full drop
  override in the original); the single tamed-only pool was asserted clean
  immediately before, so the ruby is GLM-injected. Orig: Stinky.java:257-266;
  OreSpawnMain.java:5391-5403. Port: add_ruby_to_dungeon.json:1-12 (unscoped).
  Fix: TF-009. Status: OPEN.
- **TF-007 (HIGH)** `i060_prince_family_drops` — the_prince kill dropped
  {amethyst_gem=1, beef=2}: beef 2 is in the documented 1-4 band, the amethyst is
  GLM-injected (documented: beef only, no extras). Orig: ThePrince.java:354-361
  (nextInt(4)+1 beef, nothing else); OreSpawnMain.java:5391-5403. Port:
  add_amethyst_to_dungeon.json:1-12 (unscoped). Fix: TF-009. Status: OPEN.
- **TF-008 (HIGH)** `i105_boss_drops_kraken_godzilla_queen_mothra_dragon` —
  Mothra kill produced EXACTLY the documented set (painting 1, gold_nugget 53,
  moth_scale 25, blaze_rod 3, nether_star 1) PLUS a GLM-injected ruby=1, breaking
  the exact-count assert (boss drop code itself confirmed correct); also poisons
  the exact-count Queen/King asserts on unlucky rolls. Orig: Mothra.java:341-363;
  OreSpawnMain.java:5391-5403. Port: add_ruby_to_dungeon.json:1-12 (unscoped).
  Fix: TF-009. Status: OPEN.
- **TF-009 (HIGH)** `i115_crystal_egg_ore_breaks` — SHARED ROOT CAUSE (systemic,
  entity/block side): the port registers both GLMs globally with only a
  random_chance condition, so EVERY loot roll — entity kills, block breaks,
  chests, even registered-but-empty tables — gets a bonus orespawn:ruby (eff.
  0.3x0.25=7.5%) or orespawn:amethyst_gem (0.25x0.25=6.25%) per roll; the
  original added the gems ONLY to dungeon/pyramid CHESTS via ChestGenHooks. Here:
  ore_urchin break dropped a stray amethyst_gem at the break position (not
  cross-cell contamination — same strays appear in pure getRandomItems rolls,
  TF-004/TF-005). Secondary bug: AddItemsLootModifier.CODEC hardcodes
  count=1/chance=0.25 via MapCodec.unit, ignoring the JSONs' declared count/chance
  (ruby JSON says 0.3), and doApply re-rolls chance on top of the conditions
  (double roll). Orig: OreSpawnMain.java:5391-5403 (ChestGenHooks
  dungeonChest/pyramidJungleChest/pyramidDesertyChest only;
  WeightedRandomChestContent ruby/amethyst 1-3 dungeon+jungle, 1-2 desert). Port:
  src/main/resources/data/orespawn/loot_modifiers/add_ruby_to_dungeon.json:1-12
  and add_amethyst_to_dungeon.json:1-12 (no neoforge:loot_table_id condition);
  src/main/java/danger/orespawn/loot/AddItemsLootModifier.java:18-26 (unit codec
  ignores JSON count/chance), :40-44 (doApply second roll);
  data/neoforge/loot_modifiers/global_loot_modifiers.json. Fix: scope both
  modifiers with a neoforge:loot_table_id (or any-of) condition to
  minecraft:chests/simple_dungeon, minecraft:chests/jungle_temple,
  minecraft:chests/desert_pyramid — or drop the GLMs for per-table datapack
  injections; fix the CODEC to actually read count/chance
  (Codec.INT.fieldOf("count"), Codec.FLOAT.fieldOf("chance")); remove the double
  roll (keep the coded chance OR the random_chance condition, not both); restore
  the original chest counts (1-3 dungeon/jungle, 1-2 desert). Status: OPEN.
- **TF-010 (HIGH)** `i122_basilisk_maze_content_and_sink` — structure-chest side
  of the TF-009 root cause: the basilisk_maze chest table rolled amethyst_gem,
  absent from the documented 31-entry list (BM:28) and from the port's own
  chests/basilisk_maze.json; the original's injection never touched OreSpawn's
  own structure chests. Same codec bugs as TF-009, plus the port DROPS the
  original's thunder staff / ant robot kit / spider robot kit injections. Orig:
  OreSpawnMain.java:5391-5402 (dungeonChest ruby w3/amethyst w3/thunder staff w2;
  pyramidJungleChest +AntRobotKit w3; pyramidDesertyChest +SpiderRobotKit w2 —
  vanilla pools ONLY); GenericDungeon.java:280-296 (BasiliskContentsList fills
  OreSpawn chests directly, no hook). Port: both GLM JSONs (no table
  restriction); AddItemsLootModifier.java:22-23, :40-45;
  data/neoforge/loot_modifiers/global_loot_modifiers.json. Fix: TF-009 scoping +
  codec repair + single-apply chance, PLUS add the missing
  thunder_staff/ant_robot_kit/spider_robot_kit modifiers, tuning rates to the
  original's weighted-pool semantics. Status: OPEN.
- **TF-011 (HIGH)** `i126_challenge_tower_level6_prizes` — unscoped GLM injected
  ruby into a chests/challenge_tower_level1 roll (emerald-kit tier, GD:57 —
  contains no ruby; ruby belongs to the level4 table only); all tower-content
  asserts before loot sampling passed. Orig: OreSpawnMain.java:5391-5402;
  GenericDungeon.java:57. Port: add_ruby_to_dungeon.json (unscoped);
  AddItemsLootModifier.java:40-45. Fix: TF-009/TF-010. Status: OPEN.
- **TF-012 (HIGH)** `i131_spawn_ore_breaks` — silk-touch playerDestroy of
  spider_spawn_block dropped {amethyst_gem=1, spider_spawn_block=1}: the silk
  break routes through Block.playerDestroy -> dropResources -> getDrops -> GLM,
  so the stray is injected into the block's own loot roll (documented: exactly
  1x itself, silk-independent; single-self-entry JSON assert passed immediately
  before). Orig: OreSpawnMain.java:5391-5403 (gems are chest-only). Port:
  add_amethyst_to_dungeon.json:1-12 (unscoped); AddItemsLootModifier.java:40-44.
  Fix: TF-009. Status: OPEN.
- **TF-013 (HIGH)** `i138_inca_pyramid_content` — unscoped GLM injected ruby into
  a chests/inca_pyramid roll (documented 480-weight 14-entry IncaContentsList,
  GD:38, and the port's inca_pyramid.json contain no ruby); all
  geometry/grave/spawner asserts passed. Orig: OreSpawnMain.java:5391-5402;
  GenericDungeon.java:38. Port: add_ruby_to_dungeon.json (unscoped);
  AddItemsLootModifier.java:40-45. Fix: TF-009/TF-010. Status: OPEN.
- **TF-014 (HIGH)** `i139_kyuubi_dungeon_content` — unscoped GLM injected
  amethyst_gem into a chests/kyuubi_dungeon roll (documented 110-weight
  KyuubiContentsList, GD:53, and kyuubi_dungeon.json contain no amethyst); all
  hut/shaft/altar/ziggurat asserts passed. Orig: OreSpawnMain.java:5391-5402;
  GenericDungeon.java:53. Port: add_amethyst_to_dungeon.json (unscoped);
  AddItemsLootModifier.java:40-45. Fix: TF-009/TF-010. Status: OPEN.
- **TF-015 (HIGH)** `i140_robot_lab_content_and_redstone` — unscoped GLM injected
  amethyst_gem into a chests/robot_lab roll (documented 755-weight
  RobotContentsList and robot_lab.json contain no amethyst); all
  spawner/railway/assembly/door asserts passed (crusher-piston delayed assert
  never ran — test aborted at loot stats). Orig: OreSpawnMain.java:5391-5402;
  orig RobotContentsList (fills GD:4344/4349). Port: add_amethyst_to_dungeon.json
  (unscoped); AddItemsLootModifier.java:40-45. Fix: TF-009/TF-010. Status: OPEN.
- **TF-016 (HIGH)** `i141_hospital_content` — unscoped GLM injected amethyst_gem
  into a chests/hospital roll (documented 210-weight 6-entry
  HospitalContentsList, GD:44, and hospital.json contain no amethyst); all
  cage/spawner/crystal-cap asserts passed (delayed 4-crystal/no-dragon assert
  never ran — test aborted at loot stats). Orig: OreSpawnMain.java:5391-5402;
  GenericDungeon.java:44. Port: add_amethyst_to_dungeon.json (unscoped);
  AddItemsLootModifier.java:40-45. Fix: TF-009/TF-010. Status: OPEN.
- **TF-017 (HIGH)** `item001_005_gem_ores_troll_blocks` — USER-OBSERVED companion
  defect (not asserted by the current test, which fails earlier on TF-022):
  ore_ruby/ore_amethyst ALWAYS drop the raw ore block and never the gems. Their
  loot tables gate the ore-block branch on a match_tool silk-touch predicate in
  the pre-1.20.5 schema ("enchantments" directly under "predicate"); the 1.21.1
  ItemPredicate codec only knows items/count/components/predicates (verified in
  transformed ItemPredicate.java:20-31) and silently ignores the unknown key, so
  the predicate parses EMPTY and matches ANY tool — the gem branch is dead code.
  Explains "dropping raw ore + not smelting" exactly (no ore->gem furnace recipe
  exists; gems unobtainable from mining). Gem entry also lacks the documented
  count (orig 1 + nextInt(2) = 1-2; port drops exactly 1). SYSTEMIC: the same
  dead schema appears in 14 tables (blocks/ore_ruby, ore_amethyst, ore_salt,
  ore_titanium, ore_uranium, tigers_eye_ore, apple_leaves, cherry_leaves,
  crystal_leaves, crystal_leaves_2, crystal_leaves_3, experience_leaves,
  peach_leaves, scary_leaves) — every one always takes its "silk" branch. Orig:
  OreRuby.java:32-42 (func_149650_a returns MyRuby, func_149679_a returns
  1 + nextInt(2)). Port: data/orespawn/loot_table/blocks/ore_ruby.json and
  ore_amethyst.json (dead match_tool schema, no set_count on the gem entry) +
  the 12 further tables listed above. Fix: rewrite the silk gate in all 14
  tables to the 1.20.5+ schema ("predicate": {"predicates":
  {"minecraft:enchantments": [{"enchantments": "minecraft:silk_touch", "levels":
  {"min": 1}}]}}); add set_count uniform 1-2 to the ruby/amethyst gem entries;
  after the fix, extend item001_005 (or a sibling) to assert drop identity
  (non-silk break: 1-2 gems, zero ore blocks). **EXPECTED_RED**. Status: OPEN.
- **TF-018 (HIGH)** `rainbow_islands_sky_i162` (same defect also reddens
  `rubber_ducky_pond_plains_i168`, `haunted_house_overworld_i169`,
  `ender_knight_dungeon_i170`) — all four observed "1 orespawn:ruby" inside chest
  tables whose documented 1.7.10 lists (RainbowContentsList GD:25, duck-pond list
  GD:27, KnightContentsList GD:50, haunted-house kit GD:950-993) contain no ruby;
  the table JSONs are clean, the item comes from the unscoped GLM (~7.5%/~6.25%
  per roll game-wide). Same codec bug (MapCodec.unit(1)/unit(0.25f) drop the
  JSON's count/chance; declared 0.3 silently runs as 0.25). Orig:
  OreSpawnMain.java:5391-5402; GenericDungeon.java:25/:27/:50/:950-993. Port:
  add_ruby_to_dungeon.json + add_amethyst_to_dungeon.json (random_chance only —
  no neoforge:loot_table_id); AddItemsLootModifier.java:18-26, :40-45. Fix:
  TF-009 scoping to the modern equivalents of the three vanilla categories with
  original per-category items and 1-3/1-2 counts + codec repair; all four tests
  then go green on their loot loops without test changes. Status: OPEN.
- **TF-019 (MED)** `dragon_beef_tame_heal_bone_diamond` — wrong release item: the
  original untames a tamed dragon with a DEAD BUSH and has NO TNT interaction
  (TNT falls into the generic any-item sit-toggle), but the port untames on
  Items.TNT and lets a dead bush fall through to the sit toggle; undocumented
  deviation (no FIX_LOG/phase_c_reports/C2 record). The test now asserts the
  documented dead-bush release as a final phase so phases a-d stay verified.
  Orig: Dragon.java:1261-1275 (deadbush releases; no TNT branch in func_70085_c).
  Port: src/main/java/danger/orespawn/entity/Dragon.java:961-971 (stack.is(
  Items.TNT) release branch). Fix: change the release branch to
  stack.is(Blocks.DEAD_BUSH.asItem()) and delete the TNT branch (TNT then falls
  to the generic sit-toggle at Dragon.java:1064-1071, matching the original).
  **EXPECTED_RED**. Status: OPEN.
- **TF-020 (MED)** `dragon_beef_tame_heal_bone_diamond` (adjacent, spotted in
  passing; not covered by any current test assert — reported only) — second
  interaction-item deviation in the same mobInteract rewrite: the original
  extinguishes dragon fireballs with an ICE block (ownership-gated), the port
  uses SOUL_SAND; undocumented substitution. Orig: Dragon.java:1276-1290 (ICE +
  func_152114_e -> setDragonFire(0), "Dragon fireballs extinguished."). Port:
  Dragon.java:974-984 (stack.is(Blocks.SOUL_SAND.asItem())). Fix: change the
  extinguish branch to stack.is(Blocks.ICE.asItem()). Status: OPEN.
- **TF-021 (MED)** `greenhouse_plants_regression_i172` — greenhouse plots
  self-erase their mushrooms on the live/DSB buildNow path: empty plots 657/4813
  = 13.65% vs the documented 1-in-20 (5%); the excess matches the two mushroom
  slots (2/20 = 10%) dying in ~86% of placements. In 1.21.1 mushrooms cannot
  survive on farmland (canSurvive needs a solid-render face below; farmland is
  15/16 tall) and BushBlock.updateShape returns AIR when any later adjacent
  write lands — which the generator's own subsequent writes provide; flag-2
  setBlock on a live ServerLevel still runs neighbour SHAPE updates, whereas the
  1.7.10 setBlockFast flag 2 fired none. Worldgen path (ChunkAccess writes)
  unaffected — live-path parity gap. Orig: GenericDungeon.java:5075-5080 (t==2/3
  brown/red mushroom on farmland plots), :5068 (index 8 sole empty roll),
  :187-189 (FastSetBlock, no neighbour updates). Port:
  src/main/java/danger/orespawn/world/structure/LegacyDungeonPiece.java:964-987
  (pickGreenhousePlant cases 2/3), :58/:581-585 (place -> setBlock flag 2). Fix:
  write with flags 2 | 16 (Block.UPDATE_KNOWN_SHAPE) in LegacyDungeonPiece.place
  and the other setBlock writers so piece writes stop shape-updating previously
  placed fragile plants; audit lily pads/wart/cocoa for the same benefit. Test
  then goes green unchanged. Status: OPEN.
- **TF-022 (MED)** `item001_005_gem_ores_troll_blocks` — break XP for
  ore_ruby/ore_amethyst never pops (got 0, documented 5..13 every break): the
  shared OreRuby class still rolls its 5+nextInt(5)+nextInt(5) XP inside
  spawnAfterBreak gated on dropExperience, but NeoForge 1.21.1 calls
  spawnAfterBreak with dropExperience=false on EVERY break path and sources
  break XP exclusively from IBlockExtension.getExpDrop (verified:
  CommonHooks.java:538-551 in neoforge-21.1.223; BlockDropsEvent.java:57
  computes xp from state.getExpDrop even with a null breaker). Exact ITEM-003
  wiring bug already fixed for OreUranium/OreTitanium/Lavafoam on 2026-08-10 —
  OreRuby was missed. Orig: OreRuby.java:26-30 (5 + nextInt(5) + nextInt(5)
  popped on every non-silk break; identical OreAmethyst). Port:
  src/main/java/danger/orespawn/block/OreRuby.java:30-37 (spawnAfterBreak
  override, dead code under NeoForge). Fix: move the roll to a getExpDrop
  override (no Y gate), mirroring OreUranium.java:67-76; delete the
  spawnAfterBreak XP (silk-no-XP comes free via
  EnchantmentHelper.processBlockExperience). Test needs no changes. Status: OPEN.
- **TF-023 (MED)** `red_ant_hangout_village_i165` — buildNow(RED_ANT_HANGOUT)
  produces no Robot Red Ant: the same-tick typed entity query returned 0 while
  every block assert (36-nest/220-gravel census, stone base, forced-air volume)
  passed. The spawn call is present and unconditional (bytecode-verified:
  generate ends with piece.spawnPersistent(ANT_ROBOT, cx+8, cy+1, cz+8)), the
  position is inside the piece box, ModSpawnControl cannot cancel it, and the
  structurally identical Spider Hangout robot spawned in the same run — failure
  is specific to the AntRobot add/query path; mechanism not yet isolated. Repro
  lead: this run's ant spawn X fell exactly on a chunk border (block 0 of its
  chunk); the spider's was mid-chunk. Orig: GenericDungeon.java:7064-7068
  (unconditional createEntityByName "Robot Red Ant" + spawnEntityInWorld). Port:
  src/main/java/danger/orespawn/world/structure/RedAntHangoutGenerator.java:159-160
  + LegacyDungeonPiece.java:646-655 (spawnPersistent: inChunk gate, type.create
  null-return path, addFreshEntityWithPassengers). Fix: reproduce on a live
  server with instrumentation in spawnPersistent (log the inChunk gate,
  EntityType.create null, addFreshEntity return incl. canceled
  EntityJoinLevelEvent, same-tick queryability; test chunk-border vs mid-chunk
  spawn X); fix whichever link drops the AntRobot — the test is faithful and
  stays as-is. Status: OPEN.
- **TF-024 (LOW)** `bug004_documented_diamond_block_tame_keeps_baby` — the
  documented BUG-004 flow (diamond-block tame leaves a baby prince that waits
  for a separate diamond to grow) is violated: the prince transforms instantly,
  as the user observed live — the tame sets kill/fed/day counters to 1000, which
  trips the counter-driven growth check on the next AI tick. The 1.7.10 original
  behaves IDENTICALLY, so this is a docs-vs-source conflict the red test
  deliberately pins; port parity itself holds. Orig: ThePrince.java:195-206
  (counters 1000 on tame), :556-568 (growth on counters alone). Port:
  src/main/java/danger/orespawn/entity/ThePrince.java:583-595, :286-291. Fix:
  maintainer decision — amend the docs (testing_session/items.json i002 + the
  FIX_LOG BUG-004 "grow baby->teen" wording) to match the source's instant
  transform, or set the tame counters to 0 for the two-step flow (deliberate
  divergence from the original). **EXPECTED_RED**. Status: OPEN.

- **TF-025 (MED)** `frog_pond_plains_i166` — the frog pond's cascade never
  spills past the 7×7 rim on the live buildNow path: the documented behavior
  (spec S9, generated in BOTH versions) has the +1 riser/flow-cross water
  running outward over the sheet and off the rim; in the post-triage run no
  water reached (+4,+1,0) after 300 ticks on a flat stand-in plane. Suspects:
  the batch-4 flowing→source flattening (frog_pond spec S4, PlayPool S3
  precedent) interacting with source-block stability on the flat sheet, or
  fluid scheduled-tick behavior under piece.place flag-2 writes on the live
  path. The test is faithful to the documented spill; mechanism not yet
  isolated (same treatment as TF-023). Orig: GenericDungeon.java:6030-6034
  (riser + flowing cross, meta-0 flowing water). Port:
  src/main/java/danger/orespawn/world/structure/FrogPondGenerator.java (S4
  flattening) + LegacyDungeonPiece.place flag-2. Fix: reproduce live with
  instrumentation; if the flattening is the cause, place the cross as
  Blocks.WATER flowing states (level>0) or schedule fluid ticks explicitly;
  align with whatever TF-021's flag decision lands on. Status: OPEN.

**Post-triage re-run (143 tests): 29 red — fully accounted for**: the TF
findings' tests (the unscoped-GLM umbrella flickers probabilistically, so
single-roll tests like i105 float in and out; sampling-loop tests stay red),
the 4 expected-reds, TF-025 above, and two same-run infra slips fixed in test
code after the run (b3 spider-driver read raced the driver's faithful
auto-mount, port SpiderDriver.java:106; dsb_igloo's registry-absence filter
tripped on VANILLA minecraft:igloo — namespace-scoped now).

- **TF-030 OPEN (parity review — Leonopteryx/Leon entity duplication):** 1.7.10
  has ONE entity (class Leon, registered "Leonopteryx"); the port registers TWO
  (orespawn:leon with the bespoke LeonModel + 256x256 leon.png, and
  orespawn:leonopteryx on the generic ButterflyModel — whose renderer scrambled
  the copied texture until the 2026-08-11 asset wave re-pointed it at the Leon
  layer as a minimal visual fix). Open question for parity review: which id do
  spawns/structures/eggs reference (LEONOPTERYX_NEST, ride tests, spawn eggs),
  and should the duplicate be consolidated into one entity with an id alias for
  existing worlds. Proposed fix: audit every reference, keep orespawn:leon as
  the canonical entity, alias/remove the twin. Renderer-level symptom fixed;
  entity-level consolidation deferred to review. Consolidation scope grew at the
  2026-08-11 cleanup wave: (a) the port's LeonRenderer omits the original's
  1.75x render scale + 1.75 shadow (orig ClientProxyOreSpawn.java:500); (b)
  LeonModel draws BOTH the standing and f-prefixed flying part sets every
  frame (98 parts), z-fighting whichever set is un-animated; (c) the interim
  Leonopteryx static-pose model gets no wing/leg animation until consolidated.

- **TF-031 FIXED (2026-08-11, user ruling on MOD-020):** Extractor block removed
  as an orphaned port invention. Grep evidence: the full 1.7.10 reference dump
  (reference_1_7_10_source, sources + assets) contains zero Extractor/extractor
  hits — no class, block, art, or DNA/fossil-extraction mechanic of any kind —
  and the block's output chain was already gone (kyanite / pink tourmaline
  extracting recipes deleted per PN-009/MOD-009; the last remaining
  extracting_trex_dna recipe deleted with the ADE retirement in D5). Design
  archived under MOD-009's "Extractor (DNA extraction bench)" sub-bullet for a
  possible 2.0 revival. Files removed:
  src/main/java/danger/orespawn/block/Extractor.java,
  src/main/java/danger/orespawn/block/entity/ExtractorBlockEntity.java,
  src/main/java/danger/orespawn/recipe/ExtractingRecipe.java,
  src/main/java/danger/orespawn/recipe/ModRecipes.java (the
  orespawn:extracting type/serializer served only this block; the now-empty
  danger/orespawn/recipe package was deleted with it),
  src/main/resources/assets/orespawn/blockstates/extractor.json,
  src/main/resources/assets/orespawn/models/block/extractor.json,
  src/main/resources/assets/orespawn/models/item/extractor.json,
  src/main/resources/data/orespawn/recipe/extractor.json,
  src/main/resources/data/orespawn/loot_table/blocks/extractor.json.
  Deregistered/edited in place: ModBlocks.EXTRACTOR, ModItems.EXTRACTOR_ITEM,
  ModBlockEntities.EXTRACTOR_BE (+ import), ModCreativeTabs tab entry,
  OreSpawnMod's ModRecipes.register(modEventBus) call, the
  block.orespawn.extractor lang key (en_us.json), and the orespawn:extractor
  entry in data/minecraft/tags/block/mineable/pickaxe.json. No textures existed
  to delete (the 2026-08-11 asset wave had aliased the model to vanilla
  iron_block instead of inventing extractor_* art). Verified: compileJava clean,
  python tools/asset_audit.py = 0 errors (report JSON refreshed), zero
  extractor/Extracting references remain under src/. Status: FIXED.

- **Session observation (Phase E note):** EntityCage's DATA_CAGE_INDEX synched
  value is never written server-side (client-only write in tick), so clients
  always read 160; harmless today (only empty cages are thrown) but a latent
  sync bug if filled-cage rendering ever lands.
- **Repellent wall placement gap (logged with the torch-shape fix):** orig
  Kraken/Creeper repellents extend BlockTorch and wall-mount; the port is
  floor-only (torch visual + shape landed 2026-08-11; wall variant needs a
  WallTorchBlock-style twin — small follow-up, Phase E or beta feedback).

### Harness-limit reclassifications (returned to MANUAL_ONLY)

The following checklist items were reclassified MANUAL_ONLY in
testing_session/classification.json (rationale prefix "harness limit:"). Root
wall for four of them: GameTestServer.create builds its WorldDimensions from the
FLAT preset against an EMPTY datapack LevelStem registry (decompiled 1.21.1
GameTestServer.java:97-103), so only the three vanilla dimensions ever exist —
the run log prepares only minecraft:overworld.

- **i070-d2-hoverboard-crash** — wall-crash branch unreachable headlessly: ridden
  movement integrates on the CONTROLLING CLIENT (tickRidden gated by
  isControlledByLocalInstance(), per ANIM-012); every server-side travel() path
  zeroes horizontal motion, a ServerPlayer is never a local instance, and
  external movers land between entity ticks — no server path can produce the
  required >0.75 in-tick horizontal delta. Test method removed from
  EntityLogicTestsB.java with a stub comment. Manual: ride into a wall above
  0.75 b/t in a real client — 6-15 sticks + exactly 2 diamonds, rider ejected,
  no Hoverboard item.
- **i114-c7-termite-gate-ant-chain-wgen-049** — every hop assertion needs a live
  destination ServerLevel and server.getLevel(UTOPIA/MINING/ISLANDS/CRYSTAL)
  returns null on the GameTestServer (confirmed by the run-log failure and the
  three-dimension shutdown save). Test method removed from EntityLogicTestsB.java
  with a stub comment; the termite refusal messages were the only
  destination-free sub-checks and go manual with the rest.
- **i158-damselindistress-village-dim** — Village LEVEL-existence half only:
  getServer().getLevel(orespawn:village) is always null in the harness even
  though data/orespawn/dimension/village.json loads on a real server
  (runclient). Only that assert was removed; structure-content,
  orespawn:village_biome binding, and VILLAGE_GRASS_SURFACE placement-mode
  asserts remain automated and passing.
- **i162-rainbow-islands-sky** — SKY_BAND_70 anchor sub-check only: the probe
  needs the orespawn:islands ServerLevel and its real generator
  (findGenerationPoint against contextFor(islands)). Removed with a
  HARNESS_LIMIT stub; build/spawner/chest/loot assertions remain automated
  (loot currently red from TF-018 only).
- **i164-spiderhangout-village-dim** — SpiderDriverEnable worldgen-gate sub-check
  only: the positive control needs a Village grass surface inside the Y 41..100
  scan window; the flat overworld surface at Y -60 fails the scan, so a gate-off
  empty result would be unattributable to the config gate. Removed with a
  HARNESS_LIMIT stub; pad/spawner/robot/silent-spawn assertions remain automated
  and all passed — test expected green.
- **i170-enderknightdungeon-end-mining** — LOWEST_GRASS_36 Mining-anchor
  sub-check only: the no-sink probe needs the orespawn:mining
  ServerLevel/generator (getBaseHeight recomputation), and the flat surface
  (Y -60) is outside the 31..128 scan window, so no stand-in can form. Removed
  with a HARNESS_LIMIT stub; octagon/shelf-statistics/spawner/loot assertions
  remain automated (currently red only from TF-018).

### Infra fixes: test code only

The 19 infrastructure fixes recorded in this triage (empty-stack stripping for
death-path parity, float32 replica sanity split, in-level SURVIVAL ServerPlayers
for ownership-gated interactions, biome-fill gamerule raise, creative-tab
CATEGORY filter, DSB corner/quadrant assert corrections, cage onHitEntity
bridge, cascade containment wall, terracotta soil bed, vein-smear floor
re-derivation, dimension-stub removals above) were applied to TEST CODE ONLY
(src/gametest and test helpers). No src/main change has been made in this
session — every TF fix above awaits user approval. Status of all 24 findings:
OPEN.

### Manual-session fail batch (2026-08-11) — TF-027..TF-029 + behavior triage

Verdicts for the 10 behavior notes from the 2026-08-10/11 manual sitting
(testing_session fail batch): 3 PORT_BUG findings below, 7 FAITHFUL closures
summarized after them. All three fixes land in the same batch.

- **TF-027 FIXED (2026-08-11, manual-session fail batch — duct tape inert
  twin, i003/ITEM-011/012):** two distinct failures compounded. (1) REAL PORT
  BUG: the port registered duct tape twice — the functional BlockItem
  `orespawn:duct_tape` (ModItems.java:49, reachable only via the creative tab,
  ModCreativeTabs.java:80) and a completely inert plain item
  `orespawn:duct_tape_item` (ModItems.java:650 `registerSimpleItem` — no
  block-placing or repair behavior; tab entry ModCreativeTabs.java:510, lang
  key en_us.json:290). The crafting recipe
  (data/orespawn/recipe/duct_tape_item.json:15-17) AND the checklist give-line
  (TESTING_CHECKLIST.md:50,509) both yielded the inert one, so in survival the
  repair mechanic was unreachable — the item placed nothing, clicks did
  nothing, exactly what the user saw; both ids display "Duct Tape"
  (en_us.json:25,290), indistinguishable in-game. The gametest
  (MiscTests.java:140-151) setBlocks the tape directly and never exercised
  the item path, so it could not catch this. (2) WRONG EXPECTATION: "LEFT-click
  with duct tape repairs held gear" was never the 1.7.10 flow — the original
  is cake-style: orig OreSpawnMain.java:1619-1620 (single ItemDuctTape wired
  to MyDuctTapeBlock, max stack 1), :3331 (slime+string recipe);
  ItemDuctTape.java:26-66 (onItemUse PLACES the block — the item itself never
  repairs); BlockDuctTape.java:87-117 (right- AND left-click on the PLACED
  block with the damaged item as the unstacked MAIN-hand stack repair
  maxDamage/6 min 1 per click, 6 slices then the block vanishes; no offhand
  in 1.7.10 at all). The port's block half was already faithful
  (port BlockDuctTape.java:50-91, gametest-verified). Fix: recipe result
  re-pointed to `orespawn:duct_tape`; the inert DUCT_TAPE_ITEM registration,
  its tab entry and lang key removed; the block item registered
  `.stacksTo(1)` per orig func_77625_d(1); TESTING_CHECKLIST.md:50/509
  reworded to the give-`duct_tape` place-then-click flow; gametest extension
  (place via the BlockItem's useOn) flagged optional hardening.
- **TF-028 FIXED (2026-08-11, manual-session fail batch — lava bobber
  physics, i085/ENT-S-059):** the port's UltimateFishHook.tick() ran vanilla
  `super.tick()` FIRST (port UltimateFishHook.java:145-177) — and vanilla
  1.21.1 FishingHook.tick is water-blind in lava (NeoForge 21.1.223 decompiled
  FishingHook.java:158-241: lava is not FluidTags.WATER so f stays 0.0, its
  BOBBING branch pulls the hook toward the BOTTOM of the current block AND
  applies −0.03/tick gravity, then moves and scales by 0.92) — and only
  afterwards ADDED a second BOBBING correction toward the lava surface
  (:164-175): three competing vertical forces per tick where the original had
  ONE. Orig UltimateFishHook.java:265-276 counts BOTH water and lava material
  into the in-liquid fraction d10, and :347-355 applies a single buoyancy term
  `motionY += 0.04*(2*d10-1)` with 0.8/0.9 damping and NO gravity while in
  liquid — the bobber floats half-submerged AT the lava surface like a vanilla
  bobber on water. Port symptom (numeric simulation of the exact combined
  update, scratchpad bobber_sim.py): equilibrium ~0.6 blocks UNDER the 8/9
  lava surface in a 1-deep pool with erratic 0.0-0.6 excursions, near-floor
  hang in a 3-deep pool — the user's "bobber starts in lava then starts
  floating out of it". The bite state machine itself was correctly driven
  (catchingFish at :174) and the FLYING→BOBBING lava entry (:158-163) fine.
  Fix: the lava pass made exclusive, not additive — while in lava and
  BOBBING, currentState is set to HOOKED_IN_ENTITY around super.tick()
  (hookedIn null makes that vanilla branch a pure no-op return,
  FishingHook.java:182-193, skipping its f=0 correction, gravity, move and
  0.92 scale while keeping Projectile base ticking/shouldStopFishing), then
  BOBBING is restored and one faithful copy of the vanilla BOBBING body runs
  with `f = fluid.getHeight(...)`: the d0 surface term, |d0|<0.01 kick,
  0.9/0.9 horizontal damping, biting dunk, catchingFish, then
  move(SELF)/updateRotation/scale(0.92)/reapplyPosition — and NO gravity in
  lava, per orig :277/:347-352. No new ATs needed (nibble/currentState/
  catchingFish already access-transformed per D4 §12).
- **TF-029 FIXED (2026-08-11, manual-session fail batch — hoverboard seat,
  i069/D2/ANIM-012):** two compounding porting errors put the board at the
  rider's waist ("when riding, hoverboard isnt on feet. its in the middle of
  the player"). (a) Model not re-anchored: the original rendered through a
  boat-style Render with NO −1.5 living-model offset
  (orig RenderElevator.java:27-45 — translate to entity pos, scale(−1,−1,1)),
  so the deck slab (orig ModelElevator.java:46-51, boxes at model y 0..1,
  rotation point 0) drew its top face at the entity's posY; the port copied
  the box geometry verbatim (port ModelElevator.java:30-53) but renders it
  through MobRenderer/EntityModel (ElevatorRenderer.java:21,37), whose
  convention anchors the model root 1.501 blocks ABOVE the entity origin —
  deck rendered at boardY+1.44..1.50. (b) Rider offset taken literally: port
  Elevator.positionRider put the passenger's feet at getY()+0.5
  (port Elevator.java:180-185), misreading orig :161-163 (getMountedYOffset
  0.5) + :519 — the 1.7.10 net math is +0.5 + player.getYOffset() (1.12)
  − setPosition's yOffset (1.62) = feet at boardY exactly (standing,
  shouldRiderSit=false per orig :121-123), while non-players
  (getYOffset()=0) genuinely rode 0.5 up. Hitbox registration itself was
  faithful (ModEntities.java:583-586, 1.25×1.0 per orig :58). Fix: all five
  model shapes baked with PartPose.offset(0, 24, 0) — 24 px = 1.5 blocks
  down, deck top back at ~boardY, hit-wobble pivot unchanged (it rotates at
  the entity origin like orig RenderElevator:30-38); positionRider →
  players +0.0, non-player passengers keep +0.5; the misreading Javadoc
  corrected.

**Manual-session behavior triage (2026-08-11):** the other 7 verdicts of the
10-item fail batch came back FAITHFUL — the observed behavior is the 1.7.10
original reproduced 1:1, closed in TESTING_CHECKLIST with MOD entries filed
for the four where the user voiced a preference: **ITEM-013/014** mole dirt
(0.125 sink + 0.3× drag are the exact orig MoleDirtBlock.java:33-43 values —
CONFIRMED-INTENDED, no MOD entry); **ITEM-001/005** gem-ore smelting (no
ruby/amethyst furnace recipe ever existed, orig OreSpawnMain.java:3092-3117 —
smelting sub-check dropped); **ITEM-027** duplicator pacing
(one-write-per-random-tick, ~12.5 min mean to full tree; observed ~2 MC days
was sleep-skip + out-of-range copy source → **MOD-015** growth-steps config);
**ITEM-037** chainsaw felling (blind 11×16×11 box, orig
UltimateSword.java:351-371 → **MOD-016** attached-only BFS config, plus its
provenance note on the LOGS/LEAVES tag mapping); **ITEM-047** instant-garden
Y (feet-anchored, clicked Y ignored, orig InstantGarden.java:41-50 →
**MOD-017** click-anchored-Y config); **ENT-A-074/075** CaterKiller transform
(tree-free 2400-tick metamorphosis, orig CaterKiller.java:438-448, verified
into decompiled NeoForge Mob.serverAiStep — checklist retest protocol
amended: real sword through 19 armor, stay in 32 blocks, don't die);
**i019/ENT-D-025..027** rock place-vs-throw split (in-reach block click
places a pet Rock with no clearance check by design, orig
ItemRock.java:75-128 → **MOD-018** always-throw config; the projectile's
in-flight invisibility is the separate i018 renderer item). **MOD-019**
(experience-gear self-repair / built-in mending, default-off candidate) was
additionally filed for the i009 user request — the original's
ExperienceSword.java:55-103 is an XP trickle only and never repaired
anything.
---

## Phase E — E0: Phase 14 disposition + Cephadrome source-wins + cow spawn parity (2026-08-11)

**Ruling applied:** wiki-documented mobs absent from the 1.7.10 source
(VampireButterfly, AppleCow, GoldenAppleCow) are now optional content behind
`phase14ContentEnable` (default false) — natural spawns gated in
ModSpawnControl (cows also keep the orig CowEnable gate,
OreSpawnMain.java:4609), spawn eggs hidden from creative tabs, no recipes
exist. EnchantedAppleCow is exempt: it is the original EnchantedCow
(orig OreSpawnMain.java:3599, display name "Enchanted Golden Apple Cow"
:2765) consolidated under the display-name-derived id. Full record in
MODERNIZATION_NOTES MOD-021; KNOWN_ISSUES updated.

- **TF-032 FIXED (parity — Cephadrome feed gate, source wins over wiki):**
  orig Cephadrome.java:878 accepts RAW beef (field_151082_bd) / chicken
  (field_151076_bf) / porkchop (field_151147_al) within 5 blocks as a
  heal-to-full + `wasfed=1` + `shouldattack=0` + heart-burst
  (playTameEffect(true) :884, body :858-870) trigger, one item consumed
  outside creative; empty hand mounts only when fed, consuming the flag
  (:893-904). There is NO tame state in the source. Removed the Phase-14
  grafts from port entity/Cephadrome.java: `DATA_TAMED` accessor +
  define + save/read ("CephaTamed"), porkchop-only `TAME_FOOD` +
  tame branch in mobInteract, priority-1 TemptGoal (orig has no
  EntityAITempt), and the `isTamed()` player-aggro immunity in
  isSuitableTarget (orig gate is hitByPlayer/badmood/shouldattack only,
  :537-554). Restored the three-raw-meats branch verbatim with a new
  `spawnFeedHearts()` porting the original particle body. Old saves'
  `CephaTamed` flag is dropped silently. Wiki variant archived in MOD-021.
- **TF-033 FIXED (parity — cow overworld spawns vs orig
  OreSpawnMain.java:4609-4624):** the pre-audit lump
  `add_overworld_creatures.json` gave red_cow an invented all-overworld
  w6 1-2 entry, gave enchanted_apple_cow an invented all-overworld w1 1-1
  entry, and omitted GoldCow's overworld spawns entirely. Replaced with
  per-biome modifiers matching the original addSpawn table: red_cow
  plains+forest w8 4-8, old_growth_pine_taiga+taiga w5 2-5, savanna w8 1-3,
  savanna_plateau w2 1-3 (:4610-4615); gold_cow plains+forest w5 2-6,
  old_growth_pine_taiga+taiga w5 2-5 (:4616-4619); enchanted_apple_cow
  forest+plains w3 2-4, old_growth_pine_taiga w5 2-5, mushroom_fields
  w15 3-6 (:4620-4623). Biome mapping: field_76772_c=plains,
  field_76767_f=forest, field_150578_U=old_growth_pine_taiga,
  field_76768_g=taiga, field_150588_X=savanna, field_150587_Y=
  savanna_plateau, field_76789_p=mushroom_fields. New files
  creature_{red_cow,gold_cow,enchanted_apple_cow}__*.json (9); lump rows
  removed. apple_cow/golden_apple_cow lump rows kept as the wiki spawn
  profile behind the TF-032 ruling's runtime gate.
- **TF-034 OPEN (verification gap — add_overworld_creatures.json
  residuals):** the same pre-audit lump file is the sole spawn source for
  beaver, cassowary, chipmunk, cockateil, gazelle, ostrich, peacock, ant,
  red_ant, and cliff_racer, with flat all-overworld weights that predate
  the audit and have no per-entity original-registration verification
  (orig Beaver, for contrast, is per-biome: river w10 2-4, forest w3 2-4,
  birch w2 2-4, tall-birch w2 2-5, mega-taiga w5 2-5, taiga w5 2-5,
  OreSpawnMain.java:4601-4608). Owned by the E4 PARTIAL batches — verify
  each entity's original addSpawn rows when its category batch runs
  (beaver/cassowary/chipmunk/cockateil = ENT-A, gazelle/ostrich/peacock =
  ENT-D/K per category split, ants = WGEN-049 adjunct, cliff_racer =
  TEST-002 overlap).

## Phase E — E2: the 7 UNVERIFIED, evidence gathered (2026-08-11)

All seven reached terminal states; two required code fixes, one removed an
invented spawn row, four closed VERIFIED-CORRECT with the missing evidence.

- **ENT-D-064 FIXED** — orig IrukandjiArrow deals a FLAT 100 (orig :157),
  not velocity-scaled: `func_70239_b` is an empty override (:269-270) and
  `func_70242_d` returns 100 (:272-273); crit adds nextInt(52) (:172-173).
  Port entity/IrukandjiArrow.java rewritten: custom onHitEntity with the
  orig flow — ultimateSwordPvp==off no-sells players/Girlfriend/Boyfriend/
  tamed pets (:158-170), flat 100 + crit, burning-arrow 5s ignite (:176-177),
  arrow-count increment + Punch knockback (0.6/level along flight, :187-188)
  + arrow-hit-player ding (:190-192), deflect at -0.1 with yaw flip on
  no-sell (:195-199). REMOVED the port's invented velocity-scaled 6.0 base
  AND its three invented on-hit effects (Poison III/Weakness II/Slowness II
  10s) — the orig arrow applies no potion effects (zero Potion refs).
- **ENT-K-032 FIXED (tier) / damage VERIFIED** — orig MantisClaw is
  ItemSword(toolEMERALD) (OreSpawnMain.java:1661; emerald_stats :1512 =
  1300 uses / dmg 6 / ench 75): 1.7.10 attack = 4+6 = 10. The audit's
  "dmg 10" cited MantisClaw.java:23's `weaponDamage` — a private field
  nothing reads (dead). Port already carried EMERALD attack attributes
  (6.0 tier + accepted +3 base per ENT-A-045) and the 1000-durability
  override; fixed the ctor's wrong ModToolTiers.AMETHYST →
  ModToolTiers.EMERALD (enchantability 75 + emerald repair parity).
- **ENT-K-041 VERIFIED-CORRECT** — zero rider refs in orig Mothra.java
  (riddenByEntity/field_70153_n/func_70085_c: 0 hits). Not-a-feature.
- **ENT-S-016 VERIFIED-CORRECT** — the audit read the DEAD `Slice.java`
  class (never instantiated); shipped Slice is `new Bertha(...)`
  (OreSpawnMain.java:1646) — the port's ITEM-032 Bertha-clone is faithful.
  Byproduct: 1.7.10 enchant field map proven (j..o = ids 16-21:
  field_77338_j=Sharpness, _77339_k=Smite, _77336_l=Bane, _77337_m=
  Knockback, _77334_n=FireAspect, _77335_o=Looting; _77347_r=Unbreaking),
  anchored by the ITEM-031-verified Bertha bake.
- **ENT-S-037 VERIFIED-CORRECT** — no Termite addSpawn in orig (both
  `Termite.class` refs are registrations); block-driven spawning matches:
  port CrystalAntBlock.java:64 + OreBasicStone.java:115 troll eruption +
  add_anthills/add_troll_blocks worldgen.
- **ENT-S-049 FIXED** — no Triffid spawn registration exists anywhere in
  the orig; the port's add_overworld_monsters.json w4 1-2 row was invented
  — removed (egg/spawn-block/cage pathways remain, matching orig).
- **ENT-S-050 VERIFIED-CORRECT** — orig lockout timer extracted: 300
  (Triffid.java:224/:229, re-armed on BLOCKED hits too :223-224), open
  rolls nextInt(80)==2 → nextInt(8)==1 (:248-252). Port EntityTriffid
  matches all four values/behaviors (:35/:151-155/:164/:201-202).

## Phase E — E1: the 17 untriaged BUGs, independently-verified triage (2026-08-11)

Method: 11 read-only review passes (grouped by entity/file) produced verdicts
with orig+port citations; every FAITHFUL claim then went to an independent
reviewer who tried to refute it by re-opening the cited files. All 13 FAITHFUL
verdicts survived. Outcome: **13 VERIFIED-CORRECT, 3 FIXED, 1 PARTIAL
(DEFERRED-pending)** — the audit's proposed "fixes" for the 13 would each
have broken parity (adding NBT persistence the original never had, clamping
authentic over-max enchants, smoothing a pull that was always erratic,
flooring a LoS ray the original truncates).

- **VERIFIED-CORRECT (13):** BUG-014/016/017 (King/Godzilla/Queen transient
  state — orig persists only its six/zero/five keys; King's attackDamage
  recomputes per tick from persisted PlayerHits), BUG-019 (Vortex pull —
  1.7.10 addVelocity never set velocityChanged; yank-on-damage-tick IS the
  original feel, port reproduces the same markHurt channel), BUG-022 (scan
  cadence is the orig's own; caching → OPT-004), BUG-023/024/026/030
  (Mothra/GiantRobot/Kraken-flags/WormMedium — no orig NBT overrides),
  BUG-027 (Queen LoS (int) truncation is the orig's, floor would diverge),
  BUG-029 (stale — container-return already implemented, cites orig
  :165-170), BUG-031 (client heal unguarded in orig onUpdate too), BUG-015
  (the ~300 random-registry King drops are authentic, orig :200-226).
  Consolidated MOD entries: MOD-022 (unpersisted transient state),
  MOD-023 (King loot cap opt-in), MOD-024 (modern-idiom opt-ins).
- **BUG-018 FIXED (Kraken weather, the MIXED case):** loop/override/
  non-persistence faithful and kept; two real divergences corrected in
  port Kraken.java tick — duration 6000→300 (orig func_76080_g/76090_f(300))
  and no-upgrade-of-existing-rain (flags forced only when !isRaining, else
  thundering preserved), mirroring orig :171-185 branch-for-branch.
- **BUG-025 FIXED (superseded):** helper deleted in B1; the real content —
  orig rolls FORTUNE I-V (not Silk Touch, which appears nowhere in
  Kraken.java) and over-max levels are authentic — recorded as the MOD-007
  addendum with an explicit no-clamp instruction.
- **BUG-028 FIXED (RTP burst):** Level.addParticle is an empty no-op on the
  server (decompiled 1.21.1 Level.java:465-466) — burst never rendered.
  Now per-player ServerLevel.sendParticles (in 1.7.10 only the teleported
  player's own client drew it, orig RTPBlock.java:51-56); mapping corrected
  smoke=SMOKE, explode=POOF, reddust=red DUST.
- **BUG-021 PARTIAL (DEFERRED-pending sign-off):** mechanism corrected —
  WorldGenRegion drops writes beyond its 1-chunk radius; only
  FairyCastleTree (reach 25-42 blocks) systematically shears (FairyTree ≤2
  marginal; everything else fits). Observability shipped now: swallow-catch
  logs, safeSetBlock warns via ensureCanWrite, Javadoc corrected. Remaining:
  FairyCastleTree → LegacyDungeonStructure conversion (royal-altar
  precedent) + re-derivation of the D5 dispatch coupling (fairy success
  suppresses termites/big structures, 50-chunk cooldown, OSW:188-196/1992)
  — strong-model work, proposed DEFERRED at the phase boundary.
- **TF-035 FIXED (new, from the BUG-022 investigation):** two unledgered
  Vortex divergences — (a) target sorting used plain distance where the
  orig uses GenericTargetSorter (creeper distance halved, large mobs
  prioritized by silhouette area, orig GenericTargetSorter.java:19-27);
  (b) isSuitableTarget lacked the PlayNicely gate (orig Vortex.java:341-344)
  and the ignore-list (MyUtils.isIgnoreable + Vortex/Rotator/Mothra/
  Brutalfly/Peacock/CrystalCow/Irukandji/Skate/Whale/Flounder/Urchin,
  :290-339), and checked invulnerable where the orig checks CREATIVE.
  Ported entity/ai/GenericTargetSorter (shared class restored) and fixed
  EntityVortex as the reference site. SYSTEMIC remainder: ~51 other entity
  files sort targets by plain distance — each E4 category batch swaps its
  entities' comparator during their line-by-line verification.

## Phase E — E3: MISSING implementations + carried remainders (2026-08-11)

- **ENT-A-083 FIXED (residuals)** — the flying-mount system itself shipped in
  B3 and the tame-flag half died with TF-032; fixed the two true residuals:
  hitbox 1.5x1.5 → 2.5x2.25 (orig Cephadrome.java:73) and the ridden
  22-tick MothraWings wing beat at 0.5 volume (orig :652-659), server-side
  in tick(), distinct from the faithful 1-in-6 unridden ambient (orig
  :184-189).
- **WGEN-003 + WGEN-004 FIXED** — 7 boost features (block_ruby 1/2/Y0-15;
  diamond 4/6/Y0-30; diamond_block 2/4/Y0-20; emerald 4/6/Y0-40;
  emerald_block 2/4/Y0-20; gold 4/8/Y0-40; gold_block 2/4/Y0-25 — orig
  OreSpawnMain.java:1573-1585) as minecraft:ore configured features (stone +
  deepslate targets, deepslate ore variants where vanilla has them) placed
  via orespawn:vein_count with less_ore_passes 0 — the entire boost block
  sits inside the orig's LessOre==0 gate, so LessOre worlds get none, exactly
  as 1.7.10. Wired into add_ores.json (overworld) AND, closing a gap the
  implementation surfaced, into mining_biome.json via 7 *_mining placed
  features at passes 3 / less_ore_passes 0 (the orig Mining provider calls
  the same boost block up to 3x; the mining biome previously had NO boosts
  and zero emerald of any kind).
- **WGEN-007 FIXED** — WildCropsFeature ports addStrawberries /
  addCorn / addTomatoes line-for-line (gates 1/20, 1/35, 1/70; LessLag
  attempt scaling 6/5/3; Y100→41 grass-below scan; the 9-air-above column
  veto; the exact multi-block stalk grammars — corn_0/1/3 and tomato_0/1/2/3
  per the BlockCorn.java:21-23 authoritative mapping). Overworld modifiers:
  strawberries → forest/windswept_forest/birch_forest/old_growth_birch_forest
  (established C1 ForestHills mapping), corn+tomatoes → plains. Dimension
  wiring per the orig gates: all three in Utopia, corn+tomatoes in Village
  (orig :966 excludes strawberries from DimensionID3).
- **WGEN-070 FIXED** — dead divergent CrystalMazeFeature retired (class
  deleted + tombstone comment); world/CrystalMaze via OreSpawnChunkGenerator
  remains the single faithful mechanism (WGEN-027).
- **ENT-A-054 FIXED (final remainders)** — JealousyTargetGoal ported
  (tamed/non-sitting/owner-holding companion hunts UNTAMED same-kind rivals,
  never tamed ones, orig MyEntityAIJealousy.java:31-48; per-goal ranges 6/3
  chance 5/15 per orig Boyfriend.java:146-147; PlayNicely gated dynamically)
  + MoveIndoorsGoal @11 (documented 1.21.1 behavioral match: roofed-shelter
  seeking at night/rain — vanilla removed the 1.7.10 door/village framework
  in 1.14; mapping decision in the Javadoc). Girlfriend's mirror gaps (her
  jealousy pair, Panic@6, OpenDoor@10, MoveIndoors@11, orig
  Girlfriend.java:155-175) are NOTED FOR THE E4 ENT-D BATCH — her goal-list
  completion belongs to her own open partial.
- **ITEM-023 FIXED** — block-form question terminally closed: orig ZooCage
  is an ITEM building a quartz/glass enclosure at the player (ZooCage.java,
  77 lines, full read); NO cage block exists in 1.7.10, so the item-based
  flow is the faithful shape. Real divergence corrected: port cage_size args
  were 2/4/6/8/10; orig passes 3/5/9/13/17 (OreSpawnMain.java:1931-1935),
  so zoo_cage_6/8/10 built undersized — widths now 5/7/11/15/19 (half =
  size/2+1, orig ZooCage.java:31).
- **WGEN-071 FIXED (the §7.3 igloo ruling)** — placement wired as a DOUBLE
  mechanical gate reproducing the original's border-artifact rarity with no
  invented frequency: biome tag = minecraft:snowy_plains ONLY (exact-name
  "Ice Plains" mapping, Spikes excluded) AND the piece verifies a true
  air-over-snow_BLOCK column at generation time (orig OSW:1270-1272),
  relocating among the 4 jitter attempts and no-op'ing silently when none
  passes — snow-block surfaces inside snowy_plains occur essentially only
  at ice_spikes borders, so the orig's ~zero-in-plain-biome frequency
  emerges mechanically. New SNOW_SURFACE_MINUS2 mode (spec §7.2: 4
  attempts, 41≤Y≤100, anchor firstFree−2); structure_set 15/7/84356
  (§8 C7 numbers); IGLOO joins OVERWORLD_DUNGEON_TYPES (WGEN-064 gate;
  DSB type-20 buildNow stays ungated per §9). Documented deltas: relocation
  box −22..+23, snow-layer admissible in the air cell only (freeze_top_layer
  cross-pass stability), fixed-Y retry checks, piece-RNG retry draws.
  §1.3 vanilla-terrain caveat retained in the Javadoc.
- **README reconciliation (user directive, 2026-08-11)** — coverage-table
  parentheticals purged of removed content (kyanite ×2, Extractor; Phase 14
  rows now state the MOD-021 gate and the TF-032 feed-to-ride truth);
  registry table recounted from asset_audit parses + datapack file counts
  (145 entities / 560 items / 214 blocks / 2 menus / 328 recipes / 122
  entity loot tables / 154 modifiers / 40 configured features / 47
  structures / 6 dimensions; orespawn:extracting row deleted); v1.1 banner
  now records the audit superseding the wiki milestone; roadmap bullets for
  Extractor / kyanite / pink tourmaline rewritten as REMOVED with MOD-020 /
  MOD-009 pointers; VampireButterfly + AppleCow bullets re-marked OPTIONAL
  (MOD-021), Cephadrome tame bullet REMOVED (TF-032), Duplicator bullet
  corrected to the ITEM-027 faithful mechanic, "Pink Tourmaline" arsenal /
  power-curve mentions renamed to the shipped Pink Crystal line.
- **E6 sweep inputs noted during the E3 gate (both PRE-EXISTING — identical
  counts in every earlier green run):** (1) "Unprimed heightmap:
  OCEAN_FLOOR_WG" ×3 per suite run (vanilla logs then self-primes;
  predates the igloo mode); (2) "Failed to apply component patch
  {orespawn:caged_entity=>orespawn:girlfriend} ... stack size N > max 1"
  ~120-150 per run — something builds caged-girlfriend stacks above the
  max-1 stack limit (EntityCage/caged-item interplay; candidate for the
  E4 ENT-D cage batch or E6). Neither fails a test.

## Phase E — BUG-021 ruling applied (2026-08-11)

**DEFERRED approved by owner — designated FIRST POST-BETA PATCH ITEM; does
not block the beta.** Routing: (1) AUDIT_FINDINGS BUG-021 resolution updated
to DEFERRED with the full scope; (2) player-language entry added to
KNOWN_ISSUES.md open items (what shears, ~1-in-25 Crystal-chunk castle-tree
frequency, the log signature to look for, and the patch commitment);
(3) the deferred work is a DEDICATED STRONG-MODEL SESSION: FairyCastleTree →
LegacyDungeonStructure conversion (royal-altar precedent) with
findGenerationPoint reproducing addFairyTree (OSW:1968-1986, 1/5 castle
roll) plus re-derivation of the D5 dispatch coupling (fairy success
suppresses termites/big structures + 50-chunk cooldown, OSW:188-196/1992).
Observability shipped in E1 stays as the beta-period tripwire. Ledger after
this ruling: **511 terminal / 119 open** (92 entity PARTIALs + 27 OPT).

## Phase E — E4 BOSS batch: all 8 PARTIALs terminal (2026-08-11)

**7 FIXED, 1 VERIFIED-CORRECT (stale).** Defects found vs faithful split:
6 findings surfaced real divergences (BOSS-003/008/014 degraded sidecars,
BOSS-009 missing Bird variant + invented offsets, BOSS-017 flag never
consumed, BOSS-037 NBT key rename); BOSS-043 was already implemented
(BOSS-005/012 work); BOSS-044 was a design decision, now documented.

- **BOSS-003/008/014** — the head sidecars are FAITHFUL ORIGINALS
  (invisible 19.9x10 / 9.9x10 boxes teleporting 30/17 blocks along the
  GAZE at y+12/+16, orig KingHead/QueenHead/GodzillaHead.java:33,147-149;
  orig renderers were empty stubs). Restored registration sizes
  (3x3/2x2/3x3 -> orig) and the yBodyRot->yHeadRot basis; stale
  "deprecated/future removal" rationale in ModEntities rewritten.
- **BOSS-009** — Queen happy discharge restored to orig :424-430: air-gated
  attempts at ±14/y+0..19 offsets, 50/50 Butterfly vs Cockateil (orig
  registers Cockateil under the name "Bird", OreSpawnMain.java:3831).
- **BOSS-017** — PlayNicely now consumed everywhere the orig consumed it:
  King/Queen/Godzilla targeting + revenge gates (Queen regains the
  PlayNicely half of her `|| isHappy` gates), Godzilla crush/jump gates +
  despawn-when-nice + the missing DATA_PLAY_NICELY sync, constructor-time
  size snapshots (5.5x6 / 5.5x6 / 2.475x6.25 — shrunk King/Godzilla serve
  no parts and are directly pickable, the orig single-box shape), and /4
  render scale on all three renderers (GeckoLib preRender for the Queen).
  ThePrincess leg was stale (gate existed, port :584). New suite test
  ConfigGateTests#boss017_play_nicely_gates.
- **BOSS-037** — "ThePrinceAdultGrow" legacy NBT fallback (orig :1318).
- **BOSS-043** — VERIFIED-CORRECT stale (config + spawner blocks + spawn
  gates + fizzle tests all shipped earlier).
- **BOSS-044** — manual parts FINAL for King/Godzilla, MHLib Queen-only,
  sidecars kept as faithful originals; MHLib-everywhere archived as MOD-025.
- **TF-035 riders** — all six boss classes swapped to GenericTargetSorter
  (orig fields/ctors cited per class: TheKing :64/:95, TheQueen :56/:88,
  Godzilla :57/:85, ThePrince :61/:93, ThePrincess :59/:93, ThePrinceAdult
  :78/:117). ~45 non-boss call sites remain for the ENT-* batches.

## Phase E — E4 ENT-A batch: all 34 PARTIALs terminal (2026-08-11)

**30 FIXED, 4 VERIFIED-CORRECT (ENT-A-007/030/039/116, all reviewer-upheld).**
The ten entity-cluster line-by-line verifications completed with
per-value orig citations (full per-finding record in the ledger). Highlights:
Alien village/torch AI faithfully rebuilt (flagless one-shot torch seek,
interior-blind cube-shell scan bug kept); AntRobot rider hover physics
ported number-for-number (orig :659-877) as client-predicted tickRidden;
Boyfriend regained armor floor/fire immunity/0.5x1.6 size, tamed poppy
drop, ambient voice lines, wet-skin + untame + voice toggle + health
report + FrogPrince (Frog.java kiss now sets prince status); Camarasaurus
shrank to the orig 0.5x1.2 and lost invented rideability/targets;
CloudShark regained its prey ecosystem and orig persistence; Crab/
CreepingHorror target lists and LOS restored; CrystalCow invented
never-despawn flag removed. TF-034 riders: beaver (5 per-biome files),
chipmunk (9 ambient files + MobCategory.AMBIENT re-category per orig
EnumCreatureType.ambient), cockateil (14 per-biome files incl. the w35
jungle hotspot) replace their flat lump rows; cassowary flagged
report-only (no open finding — its lump row remains, noted for E8).
TF-035 riders: sorters swapped in Alien, Alosaurus, AntRobot,
EntityCannonFodder (Chipmunk inherits). Flag from the Alien sweep for a
future pass: port Alien.hurt() invents a 5-tick retaliation cooldown the
orig lacks (orig :225-241) — logged as an E8-report observation, not
fixed (outside the finding's scope).

## Phase E — E4 ENT-D batch: all 8 PARTIALs terminal (2026-08-11)

**7 FIXED, 1 VERIFIED-CORRECT (ENT-D-020, reviewer-upheld).** Per-finding
citations in the ledger. Highlights: Dragon regained the orespawn
MothraWings flap (port had ender-dragon audio) + sorter; ENT-D-020's
"provocation explosions" premise REFUTED (zero explosion calls in the
288-line orig — screaming+teleport only; the sweep flagged wet-teleport /
daylight-gate / pumpkin-stare gaps as E8-report observations); CannonFodder
invented apple hats removed and the orig carrot/potato/quinoa hat chain +
slot-one-steal bug + corncob cloning table restored; ThrownRock water-skip
ported with the (int)-truncation probe bug kept; GiantRobot regained
WanderALot(14) + MoveThroughVillage and lost the invented stroll;
Girlfriend valentine-mode remainder completed + base size 0.5x1.6
(ModEntities) + her full goal-list gap set closed (jealousy pair, Panic,
OpenDoor+door-nav, MoveIndoors, dance — ENT-D-048). ENT-D-065 closed via
the E2 evidence (orig arrow has NO debuffs; the port's were the invention,
already removed). E6 input: the caged-girlfriend component-patch errors
remain queued for the E6 root-cause as directed.

## Phase E — E4 ENT-K batch: all 21 in-batch PARTIALs terminal (2026-08-11)

**18 FIXED, 3 VERIFIED-CORRECT (K-002/070/077, reviewer-upheld).**
(K-018/K-022 reserved for the E5 Leon/Leonopteryx consolidation.)
Highlights: K-002's "4x shorter invuln" premise refuted — the orig's
ctor-set field_70174_ab=120 is the vestigial fireResistance field vanilla
never reads (INDEX.md cheatsheet row wrong; correction queued E8), the
real gate is hurt_timer=30 matched by the port; LaserBall regained the
irukandji miss-drop, full impact-effects block (incl. the missing
'-nextFloat()' smoke-Z bug), corrected explosion flags, and the
reddust-args-as-velocity trail bug; LeafMonster + Peacock + Molenoid +
LurkingTerror spawn lumps replaced with per-biome files (orig addSpawn
row cites) + checkSpawnRules gates; Mantis butterfly prey; Molenoid dig
direction un-inverted + MoleDirt placement; Mothra difficulty-scaled
BetterFireball; PitchBlack ender-dragon part-wise explosion damage (head
1-in-8) + drop extras; Robot fleet: griefing moved to Robot4 with ranged
LaserBall + live shielding, shot sounds on 3/5; RockBase Crystal-dim
type lottery + Y>=50 rule; Rotator 16-species exclusion list restored;
RubberDucky squid prey + buddy-follow. One integration fix by the
orchestrator: EnderDragon.body is private in 1.21.1 — PitchBlack's
body strike now uses getSubEntities()[2] (vanilla part order).

## Phase E — E4 ENT-S batch: all 19 PARTIALs terminal (2026-08-11)

**17 FIXED, 2 VERIFIED-CORRECT (S-012/056, reviewer-upheld).** Highlights:
Scorpion acquisition brain restored (1-in-6 rescan, 8/3/8 sorted scan,
full prey/exclusion ladder incl. the prey-on-everything fallthrough) +
attack sound at the TARGET + cactus immunity + seven per-row spawn files
(the dropped w28 dark_forest hotspot restored; invented windswept_savanna
coverage removed); projectile trio: Shoes special-target cases verified/
fixed, ThunderBolt royalty exemption, WaterBall exemptions + ~10% pickup;
SpiderRobot frontal flame + faithful stomp, invented boss bar REMOVED;
Spyro Dragon-evolution/untame/rename + extinguisher corrected;
VelocityRaptor untame/rename; StinkBug per-biome spawns; TrooperBug
SpitBug minion summon + cactus/fall immunity; Tshirt night/no-buddy
gates; UltimateFishHook wait timers + reel-pull (TF-028 float preserved);
WaterDragon ranged WaterBall + fireball volleys integrated with the
TF-001 nav-agnostic goals; Worm boot/leggings theft on both sizes.
Orchestrator integration note: one leftover-JVM file lock on the
NeoForge jar required a daemon restart mid-gate (not a code issue).

## Phase E — E6 (part): log-noise investigations closed (2026-08-11)

- **Caged-girlfriend component-patch errors (~130/suite-run) ROOT-CAUSED
  and FIXED** — chests/basilisk_maze.json rolled `orespawn:caged_mob`
  (maxStack 1, component-holding) with set_count uniform 2-4 + the
  girlfriend component; component validation rejects count>1 patched
  stacks, logging the error and stripping contents. The orig legitimately
  stacked: BasiliskMaze.java:28 (CagedGirlfriend min2 max4 w15) with
  CritterCage.java:31 stack-16. Fix preserves the orig semantics with
  valid stacks: the row now references child table
  basilisk_maze_caged_girlfriend.json (weight 15 → rolls uniform 2-4 of
  single-count caged girlfriends). Same expected count, separate slots.
- **"Unprimed heightmap: OCEAN_FLOOR_WG" ×3/run INVESTIGATED-BENIGN** —
  the three LegacyDungeonStructure.findGenerationPoint dry-column checks
  (:287/:337/:370) query OCEAN_FLOOR_WG during placement scans; on a
  proto-chunk that has not primed that map, vanilla ChunkAccess logs the
  error and then computes the heightmap on demand — identical to vanilla
  structures hitting unprimed maps. Self-healing, no behavioral effect,
  count stable across every green run since E0. No code change.

## Phase E — E5: TF-030 Leon/Leonopteryx consolidation + ENT-K-018/022 (2026-08-11)

- **TF-030 FIXED (consolidation per design ruling: dedup + id alias + 1.75x
  scale + z-fighting parts + static pose retired).** 1.7.10 has ONE class,
  Leon, registered as "Leonopteryx" (orig OreSpawnMain.java:4377
  registerGlobalEntityID / :4381 registerModEntity); the port's twin classes
  are now one. CANONICAL id: `orespawn:leonopteryx` (snake_case of the orig
  registration name). SAVE-COMPAT: `orespawn:leon` stays registered as a
  second EntityType built from the same EntityLeon class — saved entities,
  caged mobs (EntityCage stores type ids; CaptureSpec keys on
  `instanceof EntityLeon`, so both ids capture at orig :865-874 odds), both
  spawn eggs, both loot tables (identical orig drop lists;
  entities/leon.json + entities/leonopteryx.json), chaos_biome's
  `orespawn:leon` spawner entry, and the LEONOPTERYX_NEST dungeon spawner all
  keep resolving. `entity/Leonopteryx.java` DELETED — divergence reconciled
  against orig Leon.java (the only source of truth): its invented
  ServerBossEvent boss bar, MEAT-tag taming, FLIGHT_SPEED 0.6/0.4 movement,
  hurt-window 10-with-no-15-tick-gate, and 4.0x2.0 hitbox all dropped in
  favor of EntityLeon's faithful port; both registrations now use the orig
  hitbox 3.5x8.25 (orig Leon.java:80 setSize(3.5f, 8.25f)); rider seat
  height re-pinned to the orig mounted offset 3.75 (orig Leon.java:238-240
  func_70042_X) since the old `getBbHeight()*0.85` stand-in only worked with
  the retired 4.5 height. clientTrackingRange 16 kept for both ids
  (deliberate modernization; orig tracking 64 blocks, OSM:4381). Renderer:
  `LeonopteryxRenderer` (interim static-pose band-aid) DELETED; both ids
  render via LeonRenderer, which now restores the orig 1.75x scale
  (orig RenderLeon.java:39-41 `GL11.glScalef((float)this.scale, ...)` with
  scale=1.75f and shadow 1.0f*1.75f from ClientProxyOreSpawn.java:500 —
  `new RenderLeon(new ModelLeon(0.22f), 1.0f, 1.75f)`) and carries the
  twin's no-frustum-cull override (mesh far exceeds the hitbox). Model:
  LeonModel drew ALL 98 parts every frame; orig ModelLeon.java renders
  exactly one set per state (`if (e.getActivity() == 0)` standing set
  :803-851, else f-prefixed flying set :1054-1102) — setupAnim now toggles
  ModelPart.visible per activity, restoring the gate and killing the
  z-fighting; full wing/leg animation now serves both ids (static pose
  retired). Misc consolidation follow-through: ModEntityAttributes both ids
  -> EntityLeon.createAttributes; ModSpawnControl gates the leonopteryx id
  under the same leonEnable flag (orig has one "Leonopteryx" entry,
  OSM:6523); GirlfriendOverlay label branch retargeted `instanceof
  EntityLeon` showing custom-name-or-"Leonopteryx" (orig
  GirlfriendOverlayGui.java:390-398); lang gains
  entity.orespawn.leonopteryx + item.orespawn.leonopteryx_spawn_egg and
  entity.orespawn.leon now reads "Leonopteryx" (orig OSM:4378-4379 both
  localizations are "Leonopteryx"); stale ButterflyModel-sharing comment in
  VampireButterflyRenderer corrected. Suite: no test asserted the
  duplication — CoreStatTests LEONOPTERYX 250/55/16, DsbOutcomeTests case-32
  nest spawner, EntityLogicTestsA leon tests all hold unchanged. NOT
  runtime-verified (gradle gated centrally): compile, render scale/pose in
  client, and ride-seat height at the new hitbox.
- **ENT-K-018 FIXED** — EntityLeon.doHurtTarget now ports orig
  Leon.java:275-301 (func_70652_k) faithfully: Ender Dragon branch hits a
  dragon PART with an attacker-less explosion-typed source
  (func_94539_a(null)+func_94540_d -> damageSources().explosion(null,null)),
  1-in-6 head part (field_70986_h) else body part (field_70987_i), 55.0, no
  knockback (orig :279-288; only `head` is public in 1.21.1, the body part
  is resolved by name from getSubEntities()); 4x damage vs Kraken
  (orig :290-292, iskraken=4.0f); knockback 1.25 with 0.15->0.3 in-air
  doubling unchanged (orig :294-298). Hurt window: the audit's "set hurt
  window 10" misread orig :322 (hurt_timer=15 — already ported as the
  hurtTimer full-block gate); orig :83's maxHurtResistantTime=10 is NOT
  ported — LivingEntity.invulnerableDuration is final in 1.21.1 and the
  value is unobservable behind the 15-tick gate (vanilla's partial window
  is at most half of 10 or of the default 20 = 10 ticks < 15), so behavior
  is identical; documented in the EntityLeon ctor.
- **ENT-K-022 FIXED (by consolidation)** — the leonopteryx id now runs
  EntityLeon, which already carries the full orig sound set: leon_living
  ambient gated activity==1 && no rider (orig Leon.java:208-216), leon_hit
  hurt (:218-220), leon_death death (:222-224), volume 1.75 (:226-228),
  pitch 0.85 (:230-232), plus the 20-tick mothrawings flap loop at 0.5f
  (:508-516) — all via the createVariableRangeEvent idiom; sounds.json
  already defines leon_living/leon_hit(1-3)/leon_death/mothrawings events.
  No separate Leonopteryx sound code needed; the sound-less twin is gone.

## Phase E — E6 (close) + E7: TEST-002 registrations + repellent wall-mounting (2026-08-11)

- **TEST-002 RESOLVED** — the six entities (spit_bug, gamma_metroid,
  island_too, cliff_racer, red_ant, the_princess) + two identical-rule
  siblings (ant, the_prince) registered in
  ModEntityAttributes.registerSpawnPlacements: ON_GROUND +
  MOTION_BLOCKING_NO_LEAVES with the faithful predicate per entity
  (Monster::checkMonsterSpawnRules for SpitBug — sole Monster subclass;
  Animal::checkAnimalSpawnRules for IslandToo — no orig override, so the
  1.7.10 EntityAnimal default applies; Mob::checkMobSpawnRules for the
  rest — their orig func_70601_bi overrides bypassed the animal
  grass/light default). Per-entity gates stay in the existing cited
  checkSpawnRules overrides. EVIDENCE CORRECTION: the actual startup
  ServerLifecycleHooks ERROR (runs/client/logs/latest.log:622) lists 38
  unregistered-but-spawning entities, not six — the remaining ~28
  orespawn ids (ant-family done; girlfriend/boyfriend/dragon/godzilla/
  robots/etc.) plus two iceandfire externals are a QUEUED E8 follow-up
  sweep (log-only today: they spawn with NO_RESTRICTIONS semantics, as
  before). Also flagged for E8: gamma_metroid MobCategory mismatch
  (registered CREATURE, MONSTER-listed in mining/chaos biome JSONs).
- **E7 repellent wall-mounting RESOLVED** — 1.7.10 repellents extend
  BlockTorch (KrakenRepellent.java:21-22, CreeperRepellent.java:22-23;
  wall-meta particle branches :35-51 prove live wall placement).
  Port mirrors vanilla torch/wall-torch: WallRepellentBlock extends
  RepellentBlock (ITEM-019 repel behavior untouched), FACING +
  wall-torch shapes + sturdy-face survival + pop-off; both items now
  StandingAndWallBlockItem; wall blockstates/models on the existing
  textures; wall loot drops the standing item; lang keys added. Floor
  ids/behavior unchanged (saves safe). asset audit 0 errors @ 216 blocks.

## Phase E — E6 CORRECTION + gate-discipline note (2026-08-11)

**Gate breach acknowledged:** commit c4a7390 was created while the suite
was RED (i122 basilisk-maze stack-count 11 > 10) — the orchestrator's
shell chain sequenced the commit after a grep that succeeded on the
failure line, and the commit message falsely claims 150/150. This commit
supersedes it with the correct fix and a green gate.

**The red exposed a better root-cause fix:** the child-table split
changed slot economy (2-4 separate stacks per selection) and broke the
i122 bound — but the ORIGINAL CritterCage stacks to 16
(CritterCage.java:31), so the truly faithful fix is CagedMobItem
stacksTo(1) → stacksTo(16): the basilisk-maze row returns to the orig
single-stack count 2-4 form (BasiliskMaze.java:28), the component errors
vanish (count ≤ max), i122's slot bound holds, and 1.21's
identical-components-only merging keeps different captured mobs from
stacking. The max-1 was a port invention. Child table deleted; the
prior E6 entry's split description is superseded by this one.

## Phase E — pre-F promotions

- **TF-036 FIXED (parity — Alien invented retaliation cooldown):** the
  port's Alien.hurt() gated damage behind a 5-tick hurtTimer and returned
  false while it ran; orig Alien.java:225-241 has no working gate —
  hurt_timer is declared 0 (:43) and decremented (:311-313) but never set
  above zero, so super.hurt lands on every hit, and every hurt with a mob
  attacker (:234 instanceof EntityLiving — excludes players) targets it,
  navigates to it at 1.2, and forces a true return (:235-238). Port now
  mirrors that flow exactly (cactus immunity :228-230 kept); the invented
  hurtTimer field and its decrement are deleted. (entity/Alien.java)
- **TF-037 FIXED (parity — EnderReaper missing Enderman-family
  behaviors):** three orig behaviors ported: (1) wet-teleport — orig
  EnderReaper.java:116-119 teleports when wet OR burning; the port only
  checked isOnFire, now isInWaterRainOrBubble() || isOnFire(); (2)
  daylight gate — orig :111-115: server-side, daytime, brightness > 0.5,
  sky visible, dice rand*30 < (f-0.4)*2 → target null, screaming off,
  teleportRandomly (mapped via getLightLevelDependentMagicValue +
  canSeeSky, the vanilla EnderMan idiom); (3) pumpkin-stare
  shouldAttackPlayer — orig :83-93: pumpkin helmet hides the player
  (:84-87; 1.7.10 wearable pumpkin → modern carved pumpkin), else attack
  only when the look vector aligns (d1 > 1.0 - 0.025/d0, :88-91) and the
  player can see the reaper (:92); wired as the predicate on the Player
  NearestAttackableTargetGoal, matching the orig func_70782_k gate (:67).
  (entity/EnderReaper.java)
- **TF-038 FIXED (parity — CannonFodder LOS gate + conditional armor):**
  isSuitableTarget now requires getSensing().hasLineOfSight(target)
  before the sit-anchor/faction checks (orig EntityCannonFodder.java:
  288-290 func_70635_at().func_75522_a); getArmorValue() override added
  returning 3 only while is_activated == 2, else 0 (orig :330-335
  func_70658_aO) — previously armor was 0 in all states.
  (entity/EntityCannonFodder.java)
- **TF-039 FIXED (reference docs — INDEX.md field_70174_ab row):** the
  CFR cheatsheet mapped field_70174_ab to invulnerableTime/hurtTime
  ("iframes"); it is actually Entity.fireResistance, a vestigial 1.7.10
  constant (1) that vanilla never reads — orig writes such as
  GammaMetroid.java:58 (= 1000) were no-ops and must not be ported as
  invulnerability. Row corrected with a do-not-port note; verification
  credit: ENT-K-002. (reference_1_7_10_source/INDEX.md)

## Pre-F batch: 38-entity placement sweep complete + gamma category + observation promotions (2026-08-11)

- **Spawn-placement sweep DONE** — all remaining orespawn ids from the
  startup ServerLifecycleHooks error now carry faithful placement layers
  (32 registrations spliced this batch; TEST-002's eight landed earlier).
  Per-entity orig func_70601_bi evidence recorded by the sweep passes;
  predicate choice follows FUNCTION over convention (documented at the
  splice): daytime-required Monster subclasses (BandP/Bee/Crab/...) take
  Mob::checkMobSpawnRules — the Monster darkness predicate would
  dead-gate them; super-calling overrides (Boyfriend/Girlfriend/...)
  keep the 1.7.10 animal default via Animal::checkAnimalSpawnRules;
  Kyuubi's unconditional-true orig gate takes Mob (strict parity over
  convention — the sweep's own nuance, adopted); flyers/water use
  NO_RESTRICTIONS / IN_WATER precedents; leon + leonopteryx registered
  identically per TF-030. The four iceandfire:* ids in the same error are
  CONFIRMED external (zero references in our tree; IceAndFireCE jar in
  the dev-client mods folder) — another mod's leakage, ignored per ruling.
- **gamma_metroid category ALIGNED** — orig registers its spawns as
  monster-list entries (ChunkProviderOreSpawn2 + BiomeGenUtopianPlains
  monster list); registration moved CREATURE → MONSTER to match, biome
  JSONs already agreed. Knock-on (despawn semantics for the tameable in
  MONSTER category) noted in the sweep record.
- **TF-036..TF-039 promoted and CLOSED** — Alien's invented 5-tick
  retaliation cooldown removed (orig :225-241); EnderReaper wet-teleport
  + daylight gate + pumpkin-stare ported (orig :83-93/:111-119);
  CannonFodder LOS gate (orig :288-290) + activated armor-3 (orig
  :330-335); INDEX.md field_70174_ab row corrected to fireResistance
  (ENT-K-002 verification credited). No orphan observations remain.

## Phase F — behavior-neutral OPT items applied (2026-08-11)

**22 of 27 OPT findings terminal** (batch 1 MHLib 4, batch 2 18 incl. the
OPT-020 no-op): 15 FIXED under the hard bit-identity contract, 3 STALE
(005 overlay rewrite, 014 orig-never-early-exits + E4 faithful rebuild,
027 double-scan already gone), 1 precondition-unmet (013 noCulling stays
— no cull box provably covers the part envelopes), OPT-020 no-op.
Notables: OPT-009 30 ctor-once speed asserts + 4 genuinely-dynamic kept
per-tick through cached AttributeInstance; OPT-011 200+ sound getters
hoisted to statics across ~92 classes; OPT-016/021/026 single-pass min
with sort-stability tie preservation via the new TargetSelection helper;
OPT-002 change-only multipart sends with interpolation-draining linger.
REMAINING FIVE await the owner's one-pass ruling: OPT-003/004/006/022
(behavior-affecting) + OPT-007 (mixed; its neutral half withheld to
present the item whole). Suite delta this phase: zero (the neutrality
contract held).

## Phase F CLOSED — the five owner-ruled OPT items applied (2026-08-11)

OPT-003 (with keepalive @8t under the 10t master timeout + a required
soundness addition the audit missed: server-side sync-map retention,
wholesale-replaced per packet, cleared on master reset — without it,
client-side skipping would have flapped synced parts to fallback
offsets), OPT-004 (5-tick shared Vortex target cache, immediate
dead-target invalidation), OPT-006 (5-tick Kraken probe, interval-scaled
impulse, byte-identical probe coordinates), OPT-022 (20-tick gate;
onCraftedBy migration REJECTED per ruling, rejection recorded in-code),
OPT-007 (neutral half only; Large's duplicate scan pair shares one
tick-stamped result, Medium's TargetingConditions hoisted; Medium/Small
scan-MERGING found non-neutral post-TF-035 — documented, not applied;
throttle declined per ruling). LEDGER: 630/630 terminal (628 resolved +
2 owner-approved DEFERRED). Phase F complete.

# 2.0 — OreSpawn Modernized

## S1: Procedural Spider Overhaul — research + design (2026-08-11)

Design-only slice; no code. Deliverable:
`phase_s_reports/spider_overhaul_design.md`, awaiting owner approval.

**Research.** Two tracks: (1) TheCymaera/minecraft-spider technique
study — FABRIK with pre-straighten knee bias, distance-triggered gait
capsules with inhibitor cooldowns, velocity-projected foot lookahead
over a 3x3 biased ground scan, stranded-leg dangle, physics body with
grounded-fraction lift cap (stairs emergent). LICENSE RECORDED: no
LICENSE file/SPDX; README grants commercial+non-commercial use of
plugin and source, attribution optional, "do not resell without
substantial changes" — custom-permissive; we write original Java
regardless, technique only. (2) D2/B3/suite architecture survey —
RenderSpiderRobotInfo is a client-written angle bag the models consume
via forward kinematics (so a new solver can write the same fields and
render unchanged); SpiderRobot has NO ridden movement path (pre-1.0
gap, mounted players cannot steer); 16 robot-touching suite tests
inventoried with their single-AABB assumptions; no EnumValue precedent
in OreSpawnConfig; crosshair HUD blanks on any PartEntity (King
already affected). Direct MHLib check: MHLibPartEntity.setPos /
setPositionAndRotationDirect are public and hurt() routes via
IMultipartEntity.hurt — a server-side solver can feed profile parts
directly, bypassing the client-bone trust path (the "third feed"
MOD-014 predicted).

**Decisions (D1-D5, justified in the design doc).** Server-
authoritative solve with deterministic client replay of compact step
events; FABRIK emitting the existing model angle fields (renderers,
models, assets untouched); MHLib profile parts fed server-side per
MOD-025 extend-don't-invent, 1 box/leg routing x1.0, body stays
pickable, zero parts in classic; defineEnum spiderMovement
(CLASSIC|MODERN) default MODERN per owner directive (overrides
MOD-014's suggested classic default), construct-time snapshot per the
BOSS-017 pattern; walk-only gait for S2. Slices S2-S5, risk register,
and owner questions Q1-Q4 (SpiderRobot ridden path, gallop scope, part
granularity, organic-rig extension) in the doc.

## S1 APPROVED — rulings recorded, S2 begins (2026-08-11)

Design approved as written, including default-MODERN (the owner's
override of MOD-014's suggested classic default; S5's changelog must
frame classic as one-config-line parity preservation). Rulings:
Q1 YES — modern-only B3 tickRidden for SpiderRobot lands in S5,
classic keeps the faithful no-steer gap; Q2 walk-only; Q3 one box per
leg + pickable body; Q4 organic rigs deferred to a later project.
S2 order per the approval directive: render-parity harness FIRST (no
gait work until the world-joint -> model-angle conversion is proven
within epsilon; if it fights, stop and show the mismatch), then
flat-ground walk gait (trigger capsule + inhibitors), step-event sync
with periodic keyframe, spiderMovement enum with construction-time
snapshot, classic pins via the isolated-batch idiom, gait invariant
tests 1-3. SpiderRobot only. Exit: modern walks flat ground with no
foot slide, classic bit-identical, suite green in both modes.

## BETA.3 HOTFIX — three field-confirmed bugs (2026-08-11)

First real-world field reports, all from the owner's DH/Iris instance on
day one of public beta.2 (log citations in AUDIT_FINDINGS):

- BUG-032 FIXED — databuddy was implementation-only; the published jar
  crashed every install that lacked it. Now jarJar'd (META-INF/jarjar,
  [6.0.0.0,6.1.0)). Packaging bugs are invisible to the suite by
  construction; the release checklist gains a clean-instance launch step.
- BUG-033 FIXED (CRITICAL) — LegacyDungeonPiece + RoyalTreePiece kept
  per-pass scratch state in plain instance fields; concurrent postProcess
  passes for different chunks of the SAME piece (c2me workers and DH
  distant-gen threads, both in the field logs) raced. Pass A's
  finally-null NPE'd pass B mid-place → dead chunk → wedged chunk system →
  the reported "blocks stop breaking" freeze; two live passes could also
  swap chunk-clip boxes (silent wrong-chunk writes). Both classes now hold
  the pass state in a ThreadLocal PassCtx; helper signatures unchanged.
  Sweep found no further offenders on place/postProcess paths.
- BUG-034 FIXED — dungeonBeast() shipped innerAttackRoll=0 (the TF-026
  guard slip class); the DungeonBeast was unspawnable with NaturalSpawner
  log spam. Orig DungeonBeast.java:172/:177: cadence 8, outer 7, inner 8
  (the ==1 inner quirk is already modeled in the goal). Fixed to 8.
- NEW TEST — EntityConstructionTests constructs all registered orespawn
  entity types once per suite run (suite 150 → 151): the gate BUG-034
  lacked.

INCIDENT DISCLOSURE (process, per the c4a7390 standard): while this hotfix
was in progress, a side S2 research effort overstepped
its brief and wrote an unreviewed S2 gait implementation into the working
tree (edits to OreSpawnConfig/SpiderRobot/ModNetwork + 5 new files).
Nothing reached a commit. The spillover was moved out to a local
scratchpad (s2_quarantine/, including a diff patch of the shared-file
edits), the tree was restored from HEAD, and the reviewed-by-me S2 solver
files were parked alongside so this hotfix gates exactly the tree it
commits. S2 resumes per the approved harness-first order; the quarantined
code may inform but will not be adopted wholesale.

## S2 COMPLETE — FABRIK core, walk gait, sync, config gate (2026-08-11)

Rebuilt cleanly on top of the beta.3 hotfix (ee8041c) after that
session's set-aside handoff: nothing was copied from the parked
spillover — the slice was re-applied from the working session's own
context with the independent-review fixes folded in, then re-gated from
scratch. Landed: PlanarFabrik (planar 3-segment FABRIK, straighten-bias
knee seed), SpiderRigProfile (documented hand-mirror of the classic
initLegData tables + probe geometry; mirror-drift gametest guards it),
ModernSpiderGait (server-authoritative walk gait per S1 D1/D5 —
distance-triggered speed-widened capsule, pair/neighbor inhibitors,
land cooldowns, velocity-projected lookahead with one fixed-point
refinement; world-joint -> model-angle conversion per D2 writing the
classic RenderSpiderRobotInfo fields; client replay of step events),
SpiderStepPayload + SpiderGaitKeyframePayload (+ decoder validation) +
ModNetwork registrations + start-tracking keyframe, spiderMovement enum
(default MODERN per design ruling) with SERVER construction snapshot
published on a synched entity flag, and SpiderGaitTests (render-parity
harness of 384 cases, FABRIK property/convergence pins, walk invariants
1-3, construction-snapshot + mirror guard). Classic path untouched
except the ctor snapshot and the one branch around updateLegs(); the
D2 solver, models, renderers and assets are byte-identical.

**Harness-first order honored:** the conversion was proven before gait
trust — derivation validated three ways: algebraically (it reproduces
classic's own ydisplay formula), by independent reviewer re-derivation
from the vanilla JOML render chain, and numerically (FK-of-angles onto
rig hip / FABRIK joints / target within 0.011 blocks across yaws, legs,
and target classes including the near-extension band).

**Independent review (3 reviewers on the pre-rebuild snapshot), all
findings resolved or dispositioned:**
- BLOCKER: FABRIK 20-iteration budget failed the near-extension band
  (measured 21-275 iterations needed at 18.0-18.56 blocks; up to
  0.145-block foot error the old harness never sampled). FIXED:
  MAX_ITERATIONS 300 (mid-range still exits in <=5 via tolerance);
  harness + property tests now cover 17.0/18.2/18.5/18.55 with a
  direct residual assertion.
- BLOCKER (multiplayer): spiderMovement is COMMON config — per-side
  files, never synced — and both sides snapshotted their OWN copy;
  mismatched files left client legs frozen at full stretch. FIXED:
  only the server reads the config; the snapshot rides a synched
  entity flag (DATA_MODERN_GAIT) and the client materializes its
  replay controller from that flag alone.
- MAJOR: vertical-retrigger livelock — a rest column scanning onto a
  ledge/wall >2 blocks off body level re-stepped (and broadcast)
  forever. FIXED: vertical-only retriggers now require the candidate
  footing to differ by >=0.5 blocks.
- MAJOR: a player starting to track a not-yet-ticked spider received
  an all-zero keyframe and trusted it up to 40 ticks. FIXED:
  buildKeyframe self-initializes the rest pose first.
- MAJOR (test): the walk test drove via setPos, which the server
  nulls into xo before each entity tick — the gait ran at observed
  speed 0 and the radius lerp/lookahead had zero coverage. FIXED:
  the walk drives through entity physics (delta movement re-pinned
  per tick) with a travel-distance assertion; the radius lerp is
  additionally pinned as pure math.
- MINOR fixes: keyframes phase-shifted by entity id (no synchronized
  bursts); keyframe decoder validates the wire leg count before
  allocating; barrier-shell interaction documented in the walk test;
  harness angles round-tripped through float to match the render
  precision domain; degenerate-fallback epsilon/axis aligned between
  solver and harness; lookahead duration refined against the
  displaced target.
- Documented as FAITHFUL, not fixed (both shared with classic, both
  cancel from everything the solver computes): the vanilla +1.501
  vertical render translate, and posing against entity yaw while the
  renderer uses interpolated body yaw. Changing either would break
  classic visual parity; revisit only as a deliberate S3+ decision.
- Accepted-risk notes: client/server clock skew shows as a clamped
  late swing start (keyframe-corrected); entity-id reuse could
  misroute one step for <=40 ticks (needs same-tick recycling);
  mid-swing keyframes snap to the swing target by design.
- Reviewer verdicts also REFUTED (no change needed): bitmask sign
  extension, division-by-zero, packet-order races,
  PacketDistributor signatures, test flakiness (both gait tests
  traced deterministic against framework tick order and margins).

**Classic pins disposition:** no per-test pins added in S2 — the
review's parity reviewer verified the modern server tick mutates no
world/body state (packets only), so every existing test is provably
mode-agnostic; instead the FULL suite runs under both defaults
(sweep, below). Per-test pins land with S4/S5 where genuine server
behavior deltas (parts, tickRidden) first appear.

GATE: build+assetAudit exit 0 (0 err/0 adv/3 ack); suite exit 0 —
155/155 under spiderMovement=MODERN and 155/155 under CLASSIC (151
baseline incl. the hotfix's EntityConstructionTests + 4 new). S2 exit
criteria met: modern walks flat ground with no planted-foot slide,
classic bit-identical, suite green in both modes.

## BUG-035 — Queen mid-air animation freeze (2026-08-11)

Owner-reported field bug, root-caused to a code/data contradiction:
Actions-controller one-off triggers (thenPlay = defer-to-json) against
json clips declared loop:true / hold_on_last_frame — clips that can never
finish never clear their trigger, so the controller never returns to its
STOP predicate and the last attack owns all 58 bones forever. Fixed
data-only: bite/tail_whip_left/tail_whip_right/roar -> loop:false; death
keeps hold_on_last_frame (corpse pose relies on it); idle/attack keep
looping. Verification chain recorded in AUDIT_FINDINGS (GeckoLib
AnimationController source + pinned 4.8.4 bytecode: thenPlay ->
LoopType.DEFAULT; hasAnimationFinished requires STOPPED; LOOP/HOLD never
reach it). Owner-directed pattern scan: no siblings — TheQueen is the
codebase's only GeckoLib animatable. Client-visual behavior: not suite-
assertable; owner verifying in-game (first melee cycle -> blend back to
stance through a combat lull -> full fight to death -> corpse pose).

Queen-pass inputs (three-reader sweep; NOT fixed per design ruling — held
for the upcoming Queen brief): (1) IS_AWAKE/TRANSITION_TICKS is a
write-once latch — post-wake there is no calm state, diverging from the
orig's dynamic getAttacking() keying (calm wings 0.35/0.15 vs aggro
0.85/0.26 — orig never stops animating); (2) plausible-unverified
Movement dead-latch: a one-frame client isDeadOrDying() true during
server-side heal top-ups could STOP Movement permanently (same-
RawAnimation no-op claim needs GL source verification — the same reviewer
also mis-claimed thenPlay=PLAY_ONCE, refuted by bytecode); (3) stalled-
WAKING statue if customServerAiStep stops being reached with
TRANSITION_TICKS>0 (hurt() re-arm requires ticks==0 — unrecoverable edge);
(4) WAKE_UP_DURATION_TICKS 60 < idle_to_attack 71.7t (recorded benign,
audit_sections/08:18); (5) QueenPrimaryGoal lacks requiresUpdateEveryTick
-> flight impulse + attack triggers at ~half rate vs siblings that
override it (orig ran every AI tick); (6) hurt() arms the wake-up before
every damage filter (healed explosions and discarded attackers wake her);
(7) doc/code mismatch at TheQueen.java:125-127 ("hits 1" vs flip at 0).

## S3a COMPLETE — terrain adaptation: scan grid, stranded legs, trample (2026-08-11)

First half of S3 per the escape hatch (body dynamics + pitch/roll +
renderer tilt are S3b, next session). Landed in ModernSpiderGait + the
two gait payloads + three new gametests (suite 155 -> 158):
- 3x3 biased footing scan: nine columns, classic's own probe window
  (11 up / 14 down, SpiderRobot.findNewFooting:717), every walkable
  surface per column (multi-surface walk retires S2's wall-column
  pathology), collision-shape surface heights (slab treads carry feet
  at +0.5; fence-post shapes >1.0 are not footing), scored against a
  preferred point raised 1.5 when the body's path is blocked at chest
  height (ledge/wall climb assist).
- Classic-style reach CONTRACTION before stranding (0.7/0.45/0.25 of
  rest reach, floored 3.5 — the analogue of classic's 16->3.5 sweep).
- Stranded legs: dangle semi-folded below the hip, follow the body,
  claim no contact, re-step unconditionally; strand transitions ride
  the step payload (strand flag), keyframes carry a stranded mask.
- Vertical-retrigger x climb-assist reconciliation (owner-flagged):
  vertical-only re-steps require >=0.5 improvement of the |footY-bodyY|
  mismatch; blocked attempts arm a 10-tick rescan cooldown.
- Server-side trample in modern mode at classic's cadence — EVERY tick
  a ridden leg is settled, mobGriefing-gated, classic's exact block
  logic incl. the (int)-truncation quirk (classic's client-side site
  untouched; on a dedicated server classic tramples nothing, faithfully).
- Tests: s3_cliff_recovery (invariant 4: fall, strand census, bounded
  re-plant, no-slide throughout), s3_slab_stairs_climb (emergent stairs;
  climb >=4 blocks, footing + tight cadence bounds), s3_modern_trample_
  server_side (ridden walk over a grass field -> dirt on the server).

**Independent review (3 reviewers, owner-mandated), all dispositioned:**
- BLOCKER: no reach contraction — narrow bridges/ridges stranded every
  leg whose fixed-distance rest column was off-terrain while classic
  grips near the hip. FIXED (contraction sweep above).
- MAJOR: est2 refinement overwrote a valid est1 candidate with null
  (spurious strand + payload pair at cliff lips). FIXED: refined scan
  is fallback-only.
- MAJOR: reach checked from the CURRENT hip against a target projected
  up to v*est ahead (over-strict by up to ~3.6 blocks; front legs have
  2.19 blocks of headroom). FIXED: hip projected by the same est.
- MAJOR: payload registrar still "1.0" across wire-format changes — an
  S2-era jar would pass negotiation and desync mid-session. FIXED:
  bumped to "1.1" + STANDING RULE comment: bump on every format change
  (S3b/S4/S5).
- MAJOR (test): stairs one-shot grounded assert 30 ticks post-drive had
  ~3-5 ticks of worst-case margin. FIXED: 60-tick rest window; cadence
  bounds tightened to falsifiable values (cliff <=12, stairs <=15 — the
  old <=30 could not catch a 7-tick-period livelock).
- MINORs FIXED: slab feet floated +0.5 (collision-shape tops now);
  gate-blocked vertical retriggers rescanned ~230 blocks/leg/tick
  forever (cooldown); scan window off-by-one vs its names; un-strand
  swing-start pop (client departs from its own rendered dangle, target
  stays server-authoritative); trample cadence matched to classic's
  every-settled-tick (was touchdown-only); mock rider + setup-assert
  entity leaks in tests; stale absolute/relative comments and floating
  test terrain (cliff platform now solid from the rel-0 surface).
- Verified-safe (no change): strand payload rate is bounded (>=5-tick
  period per leg); trample truncation quirk cannot red the test at
  negative-coordinate plots (integral foot Y; XZ shift absorbed by the
  field-wide scan); mock rider provably persists (tickPassenger Player
  branch); early-payload race self-heals via keyframes.
- Regression caught by the re-gate (cliff red): the first S3a scan
  window (4 up / 8 down) was far shorter than classic's probe and,
  once the test platform stopped floating, stranded a rear leg whose
  only footing was the cliff-wall top. Fixed by adopting classic's
  11/14 window — shorter windows are NOT a tuning freedom, they are a
  parity break with the probe geometry.

**Process notes:** (1) BUG-035 (Queen animation freeze, from a parallel
session) was found riding uncommitted in the tree by the review's
slice-contamination check — committed separately as ad129e2 so this
slice's diff equals its claim. (2) A wedged gametest-server JVM held
the world's session.lock; the next runGameTestServer FAILED TO START
yet gradle exited 0 — a false green caught only by reading the pass
line. GATE RULE HARDENED: a gate is green only when the literal
"All N required tests passed" line is captured; exit codes alone are
insufficient. (3) Stranded-leg rescans run every tick by design
(fast cliff recovery); perpetual stranding (bridge over a deep void)
pays a contraction sweep per tick — S3b tuning candidate if profiling
warrants.

GATE: build+assetAudit exit 0 (0 err/0 adv/3 ack); suite 158/158 under
MODERN and 158/158 under CLASSIC, pass lines verified. Classic path
untouched this slice (payloads/gait/tests only). S3b remains: body
height float, pitch/roll from corner legs, renderer tilt with foot
compensation, plus its own reviewer pass and both-mode gates.

## S3b COMPLETE — body dynamics: height float, pitch/roll, renderer tilt (2026-08-11)

S3 is now fully landed (S3a terrain + this). Harness-FIRST honored per
the directive: the tilt-compensation harness existed and passed before
the dynamics were trusted — and the reviewer pass then proved the first
harness had a structural blind spot (below). Landed:
- Height float: PD spring toward the planted-feet average (stiffness
  0.15 / damping 0.5, spectral radius sqrt(0.5)/tick — reviewer-derived,
  no limit cycles possible: both cap-saturated regimes are constant-
  force), gravity -0.08 always acting, leg force up-only capped by
  0.32 x grounded fraction — sag emerges when support collapses; exact
  zero-lift equilibrium on flat ground. Rate-limited (0.15/tick).
- Pitch/roll: low-passed (0.3) toward planted corner-group centroid
  tilt, spans DERIVED from the rig's real rest stance (review: magic 14
  vs real 19.55/21.14 over-read slopes 1.4-1.5x), clamp 0.35 rad, rate
  limit 0.02 rad/tick; swinging legs contribute their swing DESTINATION
  so centroids stay continuous (review: dropping a far swing leg hopped
  the roll target ~5 deg every stride); empty groups decay toward level
  (review: falling spiders kept stale tilt frozen); sag attenuated to
  -0.15 while ridden (review: passengers render from real entity state
  and would hover over a sagged body — S5 reconciles the seat).
- Renderer (modern-only branch, classic render byte-identical):
  T = translate(lift) conjugated about the VANILLA +1.501 MODEL PIVOT
  with Ry(a)Rx(pitch)Rz(roll)Ry(-a), a = -yawRad, raw tick values.
- Client solve compensates with the exact inverse + production's reach
  clamp pulled per-leg along the hip ray (review: near-max grips under
  high tilt pushed compensated targets past leg reach -> unpredictable
  foot creep; now the shortfall is the same graceful straight-stretch
  family as untilted overreach).

**Independent review (3 reviewers), all dispositioned:**
- BLOCKER (drift): the tilt rotated about the ENTITY ANCHOR while
  vanilla draws the model +1.501 above it — planted feet slid by
  (R-I)*(0,1.501,0), up to 0.52 blocks at the clamp, at exactly the
  held-tilt poses ramps produce. FIXED: pivot conjugation in the
  renderer (the existing math pair then becomes exactly true; feet land
  on their classic-quirk anchors under any tilt).
- MAJOR (drift): per-frame lerp of dynamics values against tick-solved
  compensation angles = up to ~1.7-block sawtooth foot slide on far
  legs. FIXED: renderer consumes raw tick values (compensation cancels
  exactly every frame) + per-tick rate limits keep body stepping
  sub-visual.
- MAJOR (harness): the first harness closed its loop through
  production's own bodyTransform — structurally blind to transcription
  and render-chain divergence (the BLOCKER lived exactly there), and
  its "skip" path was dead code (parity reviewer: deterministic 3456/0).
  FIXED: JOML replay of the renderer's exact op sequence asserted
  against the double math; a true-stance-radius (16-block) target class
  added; production's reach clamp mirrored; every cell accounted for
  (exact + clamped == total, exact >= 70%); design-doc claim reworded
  to what is actually proven.
- MAJOR (dynamics): reach overflow of compensated targets — FIXED via
  the per-leg clamp above + harness coverage.
- MINORs FIXED: rig-derived spans; centroid continuity; rider sag
  floor; group-empty decay + javadoc; left/right handedness labels
  corrected to world axes in both files (odd legs = +X at yaw 0 =
  body-LEFT; the S2 'right' label was inverted).
- Verified clean (no change): PoseStack transcription order/handedness/
  conjugation sign (term-for-term vs JOML, both yaws hand-checked);
  discrete PD stability (roots 0.675 +/- 0.211i, 2.65% overshoot, cap
  cannot inject energy; the 'lift dips every swing' attack REFUTED —
  holding needs only f >= 0.25); the NO-FEEDBACK property (body state
  has zero server-side readers; scans/triggers/physics never see it —
  every pre-S3b test provably unaffected); test determinism incl. the
  teleport-sag trajectory, barrier-ceiling non-interference, and the
  free-will yaw concern (SpiderRobot has no movement goals, look goals
  are head-only).
- Accepted, documented: entity-yaw vs interpolated-body-yaw remains the
  classic-shared quirk (commutator term ~0.1-0.2 blocks in plausible
  turns); 1-tick client dynamics lag self-heals (~2-tick half-life);
  S4 design note added — server-fed parts must tolerate ~latency+1
  ticks of dynamics skew or the keyframe grows the four scalars.

Tests: s3b_tilt_compensation_harness (identity + JOML transcription
replay + 4608-cell clamp-mirrored round trip), s3b_body_settles_flat_
and_sags (settle without oscillation, teleport-sag, re-settle),
s3b_ramp_pitch_sign (nose-up on the ramp, near-zero roll, no-slide
throughout). Suite 158 -> 161.

GATE: build+assetAudit exit 0 (0 err/0 adv/3 ack); suite 161/161 under
MODERN and 161/161 under CLASSIC, pass lines verified. S3 exit criteria
met: the modern spider's body rides its legs — sagging, tilting,
settling — with planted feet motionless in world space (tick-domain
proven; render-chain transcription-checked); classic bit-identical;
suite green both modes. Next: S4 (multi-part hitboxes) per the slice
plan, on the owner's go.

## PROJECT LAW ADDITION + reference-video ingestion (2026-08-11, post-S3b)

**LAW (owner-ratified from the S3b review): a verification harness must
not close its loop through the code it verifies.** A harness that
asserts production math against production's own transform proves only
self-consistency — the S3b pivot BLOCKER lived exactly in that blind
spot. Every future harness must anchor at least one leg of its loop in
an independent formulation (a literal transcription replay, an
independently-derived reference, or ground-truth data). Stands
alongside: the literal-pass-line gate rule, classic probe geometry,
and the payload-version bump rule.

Ingested phase_s_reports/reference_video_notes.md — the reference
creator's own 3-video explanation, cross-marked against our design.
Confirmations throughout (FABRIK shape, pre-straighten as the whole
knee system re-confirmed per-solve, trigger/inhibitor/lookahead rules,
3x3 grid with the exact 0.5 climb bias, pitch/roll from corner legs).
Design rulings on its net-new items: COM + support-polygon gravity NOT
retrofitted over S3b's committed scalar lift — banked as MOD-026 with
the gallop spec, spring-damper tilt and per-segment rotation; the
aim-offset composability note folded into the S3 design amendment
(verified: the tilt pipeline composes cleanly, no code change).

## BUG-036 + S4 research complete — implementation next session (2026-08-11)

S4 (multi-part hitboxes) research finished and recorded as the design
doc's "S4 as-designed" block: the ICustomHitboxProfileSupplier gate
(with the ctor-timing flag and lazy client part build), the
alignSubParts same-tick overwrite ordering, the one Queen-neutral
vendored change (updateSynching gated on syncWithModel — kills a
~10-tick SPacketSetMaster churn for boneless profiles), the exact
profile spec (main size MUST equal classic dims — MHLib hooks
EntityEvent.Size), the skew ruling (parts tolerate ~latency+1 ticks;
keyframes stay foot-state-only; the four scalars are the pre-approved
fallback on the existing 40-tick keyframe), the ant-profile deferral
to S5, the HUD unwrap plan, and tests 5-8 shapes. ESCAPE HATCH
invoked for the implementation: research + BUG-036 fit this session
honestly; the build-out, reviewer pass and both-mode gates do not.

BUG-036 (found by that research, FIXED here): MHLib's upstream demo
creeper profile shipped in the jar — vanilla creepers had multipart
hitboxes and an unpickable main box in public beta.2/3. Data file
deleted; VanillaParityTests pins the no-vanilla-parts contract
(suite 161 -> 162). Full record in AUDIT_FINDINGS BUG-036.

## S4 RED — three-reviewer review REFUTED the slice; parked uncommitted (2026-08-11)

The S4 implementation (profile, supplier gate, part feed, updateSynching
server gate, HUD unwrap, tests 5-8) is complete in the working tree and
suite-green 168/168 BOTH modes — and the review proved that green is
worthless here: no gametest exercises a client, a raycast, a projectile
or a swinging leg, and all four independent BLOCKER/MAJOR reachability
defects live exactly there. RED-GATE LAW: nothing commits. Tree parked
uncommitted under the one-writer rule; this entry is the complete fix
docket so the next session implements with zero re-derivation.

**BLOCKERS (all confirmed with decompiled-source traces):**
1. PARTS UNHITTABLE: profile collidable:false makes
   MHLibPartEntity.isPickable() false (:345-347 — collidable && enabled;
   there IS no separate pickable flag) -> excluded from melee crosshair
   pick (GameRenderer.pick), ALL projectiles (canBeHitByProjectile =
   alive && pickable) and the overlay ray fallback. can-receive-damage
   is unreachable; test 6 masked it by calling part.hurt() directly.
   FIX: vendored — isPickable() := (collidable || canReceiveDamage) &&
   enabled (Queen-neutral: her parts are collidable:true), do NOT flip
   collidable (hard collision classic never had); add a real pick-path
   test (canBeHitByProjectile + a GameRenderer-style predicate).
2. LAZY CLIENT BUILD CLOBBERS THE NETWORK ID: mhlibOnConstructor's tail
   setId(ENTITY_COUNTER...) runs AFTER the client applied the server id;
   the follow-up setId(getId()) re-propagates the WRONG id -> every
   client attack/interact packet misaddresses (spider unattackable/
   unrideable; multiplayer id collisions can misdirect onto other
   entities; EntityLookup removal leaks). AND lazily built parts are
   never registered in ClientLevel.partEntities (add-time-only,
   onTrackingStart) so client picking stays blind regardless.
   FIX: build from an onSyncedDataUpdated(DATA_MODERN_GAIT) override
   with id capture/restore (final int syncedId = getId(); build;
   setId(syncedId) — cascade then matches the server's base+1..+8), plus
   a small vendored ClientLevel.partEntities re-registration helper
   (accessor mixin); keep classic zero-part (eager-disabled-parts
   alternative violates D3). Consider the parity reviewer's hardening:
   guard mhlibOnConstructor's re-id with isClientSide inside MHLib.
3. SWING-PHASE SKEW IS THE FLOOR NOT THE CEILING — OWNER RULING NEEDED:
   client swing replay runs one full latency behind the server clock for
   the WHOLE 4-12-tick swing -> server-fed parts lead rendered swinging
   legs by ~2.3 blocks/latency-tick (4-11 blocks realistic), ~7x the
   accepted dynamics-skew ceiling; planted legs meet tolerance (~1e-3).
   Options: (a) RESTATE the tolerance honestly (planted exact; swinging
   = server-true trajectory, client view lags by latency; gameplay
   impact bounded — legs route x1.0 so a missed swing-leg costs nothing
   vs aiming at the body); (b) grace-clamp server swing progress by a
   latency budget (parts lag truth instead of leading the view).
   Recommendation: (a); await ruling.
4. NEW LAVA CHANNEL (MAJOR, damage reviewer): part baseTick fires
   lavaHurt -> routes to parent; "lava" is not in SpiderRobot.hurt's
   msgId filter -> stranded/dangling shin boxes over lava damage a
   spider classic never damaged. FIX (Queen-safe rule): in the part-hurt
   router, drop source-less environmental damage ONLY when the profile's
   MAIN hitbox canReceiveDamage (spider: env acts on the body, parts are
   directed-attack surfaces); when main cannot receive damage (Queen),
   parts stay the only channel — unchanged.

**MAJORS/MINORS to fix in the same pass:** mirror the syncWithModel gate
into updateSynching's CLIENT branch (empty 8-tick keepalive packets per
tracked spider) AND the two tracking-hook elections (EntityEventHandler
start/stop — the gametest's "never elects" wording currently overstates:
it seeds the queue directly and no tracking events fire); rewrite test
7's typed-query assertion to getEntitiesOfClass (typed EntityType
queries DO return parts as their parent's type once fed — the current
assert is green only because unticked parts idle at world origin; audit
i164's same-family query); test 8: tag.remove("UUID") before load()
(current sequence leaks a stale byUuid entry for the rest of the run);
ctor-tear hardening: make the supplier's ctor-tail config read the ONE
authoritative read (store the decision; ctor body consumes it — kills
the worldgen-thread two-read tear that could build parts on a
CLASSIC-snapshot spider); positionLegPart degenerate-bearing fallback
must mirror solveLegAngles' (legBearing+PI/2 at dh<=1e-6, drop the 1e-9
world-+X fallback) incl. the test mirror; document the explosion
closest-surface note (9-box blast profile is a strictly larger honest
surface — carve out of the profile's "identical damage-in" comment);
ruling wanted: MixinServerEntity's per-tick 8-part S2C stream is
overwritten by the client mirror every tick — document as pre-keyframe
fallback or gate for boneless+no-deviation profiles (change-only law).

**Verified clean by the same review (keep, do not re-litigate):**
routing math exact (x1.0, armor/filters identical part-vs-body,
invulnerability window nets ALL multi-surface scenarios to max-not-sum:
same-tick multi-leg, pierce, sweep — parts aren't LivingEntities — and
explosions); dims bit-identical classic (scalable(2.0,1.5) == .sized);
classic bit-identity end-to-end incl. the Size hook and client ctor
flag read; the damage-funnel window is UNOBSERVABLE (sequential entity
ticks); PART_HALF_HEIGHT anchoring and bearing math correct; the
election test IS deterministic; the four-boss sweep cannot interfere;
no MHLib layer caches a stale empty for the lazy path.

## S4 GREEN — docket implemented, fix-review triple-PASS, committed (2026-08-11)

**PROJECT LAW (owner-ratified from the S4 red): a test must exercise the
path the player uses, not the API beneath it.** The arrow-through-a-leg
flight test, the lava scenario outcome test and the pick-gate asserts
are that law's enforcement. **Skew ruling: OPTION A** — tolerance
restated honestly (planted legs exact ~1e-3; swinging legs are
SERVER-TRUE and the client view lags by latency; x1.0 routing bounds
the impact; grace-clamp REJECTED: server truth does not bend to client
rendering). Design doc updated; the tracking test asserts the server
trajectory.

Every S4 RED docket finding implemented and then re-verified by a
second three-reviewer pass (verdicts: PASS / GREEN / PASS — no new
defects, nothing masked): isPickable := (collidable||canReceiveDamage)
&& enabled (Queen truth table unchanged; King/Godzilla manual parts
use their own isPickable, unaffected); the client part build moved to
onSyncedDataUpdated with id capture/restore (cascade == server's
base+1..+8, bytecode-verified fire order: SetEntityData bundles after
AddEntity) + MHLibClientPartRegistration/AccessorClientLevel
(field javap-verified); env-damage routing rule (drop source-less via
parts iff MAIN canReceiveDamage — Queen keeps full routing; explosions
provably safe: even unowned TNT is its own direct source); all three
election/keepalive gates (updateSynching server+client, both tracking
hooks); ctor-tear single-read; degenerate-bearing mirrors (production
AND test); sourced-damage twins; class-based counts + untyped
type-census pin (typed queries DO see parts — EnderDragon parity —
and the heap-pollution hazard bit our own first pin as predicted);
part-id cascade pin; UUID-strip; i164/i165 query conversions; honest
election wording; stale-javadoc sweep.

**New mechanism finding (recorded):** MHLib's alignSubParts stomp
re-stacks parts at the BODY before tickParts, so parts fluid-sample at
the body position every tick — the lava channel was UNREACHABLE in
production (the red-docket finding was right about routing, wrong
about reachability); the routing rule stays as the defensive second
wall, with both arms directly pinned. The MixinServerEntity per-tick
part stream is DOCUMENTED as the pre-first-keyframe/non-mirrored-
client fallback (client mirror overwrites it; gating = S5 change-only
candidate, recorded not ruled).

**Open item (by design, not omission):** the client half (id restore,
pick registration, HUD unwrap on legs) is untestable in server-only
gametests — bytecode-traced by two independent reviewers, and queued
for the owner's in-game verification session per the recorded exit
evidence (KNOWN territory: first client session validates it).

GATE: build+assetAudit exit 0 (0 err/0 adv/3 ack); suite 170/170 under
MODERN and 170/170 under CLASSIC, pass lines verified (162 baseline +
8: tests 5-8, election neutrality, part-parent sweep, arrow player-
path, lava outcome). S4 exit criteria met; S5 (ant rig + ride
integration + suite sweep) remains.

## S5 research complete — as-designed block recorded (2026-08-11)

Closing-slice research done and recorded in the design doc's "S5
as-designed" block: the ant's exact per-leg tables/probe
windows/reaches read from source (incl. the leg-0/1 reach override
ordering quirk and the 0.8 swing-bias factor), the model-formula
identity that carries the S2 conversion mapping over unchanged, the
LegRig abstraction plan (per-rig classic probe geometry per the
scan-window law), the IModernLeggedRobot payload generalization with a
registrar bump, the ant S4-pattern part integration (main EXACTLY
2.75x1.25), the Q1 tickRidden design with the SpiderDriver-never-
controlling rule, and the S3b rider-seat resolution (ridden dynamics
clamped to +/-0.15). ESCAPE HATCH: implementation, reviewers and gates
go to a fresh session with nothing to re-derive; the parked
MixinServerEntity stream ruling is presented in the boundary report.

## S5a — ridden path + ratified stream gate + seat resolution (2026-08-11)

HONEST SCOPE SPLIT (escape hatch invoked): the directed S5 slice is
larger than one session closes truthfully. S5a (this entry) delivers
the spider-scoped closing work — the Q1 ridden path, the owner-ratified
MixinServerEntity part-stream gate, the S3b seat resolution, and the
player-facing docs. S5b (one further session) carries the LegRig
refactor, the full ant integration (tables already recorded in the
design doc's "S5 as-designed" block), and the overhaul's closing
report. The overhaul ledger stays OPEN until S5b.

Implemented:
- RATIFIED stream gate: IMultipartEntity.mhlibShouldStreamParts()
  (absent profile / sync-to-model / any deviation>0 -> stream; else
  gate) + MixinServerEntity guard. The review found the ruling's
  pairing-time-seed premise FALSE as first written (addPairing only
  nulls the cache; an unconditional early return sent nothing, ever) —
  fixed by gating only on a non-null cache, which makes the ratified
  seed REAL: exactly one full compile+broadcast per gained tracker,
  then silence. Gated profiles assert local client mirroring (the
  modern spider's gait replay); Queen streams byte-identically.
- Q1 ride: getControllingPassenger (modern owns the decision — player
  controls, non-player NEVER; classic = pure super, bit-identity
  CONFIRMED by decompiled-dispatch trace incl. the jockey branch),
  B3 tickRidden (setRot rider yaw, pitch 0), getRiddenInput (full
  forward, half strafe, quarter reverse, no jump), getRiddenSpeed
  (MOVEMENT_SPEED 0.35). First suite run RED: vanilla's jockey branch
  handed control to the SpiderDriver via super — modern arm now decides
  outright. updateControlFlags override added (modern+ridden -> LOOK
  off): the jockey branch's REAL vanilla consumer is goal suppression,
  not travel interception (reviewer trace) — this preserves pre-S5
  driver-ridden head stillness AND stops server look goals fighting a
  steering rider; classic keeps both vanilla behaviors untouched.
- Seat resolution (S3b handoff): ridden body dynamics clamped to
  +/-0.15 lift AND sag; bounds CONVERGE at LIFT_RATE_LIMIT (review: a
  hard flip snapped the body up to 0.85 blocks at mount).
- Rest-heading follower (review MAJOR): rider-yaw 1:1 coupling meant a
  ~6.6-degree look-flick displaced 17-block rest targets past the 2.0
  stationary trigger — perpetual leg dance + trample grinding. Rest
  targets now follow a dead-banded (8.6 deg), rate-limited (3.4 deg/t,
  worst-case rest displacement 1.02 b/t < STEP_SPEED 1.1) heading;
  dangle/strand stay on TRUE yaw (keyframes sync only the stranded
  flag — verify pass caught the server/client dangle mismatch).
- Registrar 1.2: conservative compat fence (comment honest: traffic
  pattern changed on MHLib's channel, no wire format changed).
- Docs: CHANGELOG 2.0 section (classic = one-config-line parity
  preservation), KNOWN_ISSUES 2.0 notes (leg surfaces, swing-latency
  in player terms, lava rule, steered step-height 1.0 + air-accel
  vanilla buffs kept deliberately, ant deferral), config comment.
- Tests +5 (=175): ride control truth table (+ driver shove), ridden
  seat clamp (discriminating +Z shelf, both scan windows fully on it
  after the verify pass caught an axis-swapped first cut + dismount
  control phase proving the terrain lifts), mount/dismount mid-swing
  (windowed census), yaw-jitter-no-dance (zero triggers in-band, 90
  deg turn re-plants), stream-gate predicate both arms. All three
  SpiderDriver test riders setNoAi (their combat drive lives in
  customServerAiStep — removeFreeWill does NOT strip it); S4 trample
  rider swapped player->driver (a controlling mock player freezes
  server-side vehicle travel — the exact Q1 dispatch working).

Three-reviewer pass (ride / classic-parity / gate+tests): no blockers;
3 MAJOR + minors, all fixed above. Verify pass on the fixes (3
verifiers): fixes confirmed, 1 test-geometry fragility + 3 doc
residuals, all fixed; one verifier's "deterministic fail" claim on the
seat test was empirically refuted by green runs but its geometric
substance (axis swap) was real and drove the robust shelf.

GATE: suite 175/175 MODERN and 175/175 CLASSIC (pass lines captured
literally), build+assetAudit 0 err / 0 adv / 3 ack. Nothing pushes;
the owner's in-game sitting decides the release. S5b remains.

## S5b COMPLETE — LegRig refactor + ant rig: THE OVERHAUL IS DONE (2026-08-11)

The closing slice. Two phases, each gated:

**Phase 1 — LegRig refactor, neutrality PROVEN.** LegRig (per-rig
tables + classic probe geometry + rig-scaled tuning + ctor-derived
tilt spans + pitchGroup + reachMargin + tramples flag);
SpiderRigProfile -> static facade over its LegRig (numbers verbatim);
ModernSpiderGait rig-parameterized (Mob params, arrays sized
legCount, solveLegAngles takes the rig); IModernLeggedRobot; payload
handlers generalized (keyframe decoder envelope {6,8}; handlers
validate the target entity's rig EXACTLY); registrar 1.3 (real
format-envelope change: leg count 6 now legal on the wire);
GaitSyncEvents generalized. Neutrality was gated as its own
checkpoint: the FULL 175-test suite green under BOTH modes on the
refactored spider before any ant code landed — every S2-S5a invariant
re-run against the refactor, plus a line-by-line reviewer diff proof
(every moved constant verbatim; span derivation bit-identical
per-term division; sagFloorEff init timing; trample condition order).

**Phase 2 — the ant.** AntRigProfile from the recorded tables (all
verified digit-for-digit vs initLegData by review AND by a live
classic ant in-suite); scan window 8/8 (the exact foot-space reading
of the classic yScan loop — adjudicated more faithful than the
spider's historical 11/14); rest reaches 6/9/4 with the leg-0/1
override-order quirk mirrored; tuning x0.495 starting tune; lift/sag
+/-0.5; NO trample (classic ant feet have no block side effects — a
rig flag, caught at design-derivation time); reach margin 0.995 (the
law-bound 9.0 opening + 0.75 hip drop = 98.3% of max reach; the
spider's 0.98 would have rejected the ant's own classic rest stance).
AntRobot integrated on the S4 pattern verbatim (supplier shadowing,
ctor-tail single read, DATA_MODERN_GAIT, onSyncedDataUpdated
id-restore + pick registration, cascade +1..+6); hover-ride physics,
positionRider seat and getControllingPassenger UNTOUCHED — a hovering
body strands legs and the dangle is the designed look.
ant_robot.json: main EXACTLY [2.75, 1.25] (i083 Size-hook pin holds
in-suite both modes), 6x 0.4-cube legs, spider damage conventions.
AntRobotRenderer: the S3b pivot-conjugated tilt transcription.

**Tests +8 (=183):** ant render-parity harness (rig-parameterized,
INDEPENDENT 49px transcription after review caught a self-referential
segment length), construction snapshot vs a live classic ant, mode
gate + id cascade + dims pin, flat walk, hover all-legs-stranded
degenerate + re-plant (needed the new 34-high empty_tall template —
the 16-high cages' invisible BARRIER ceiling sat inside the ant's
scan window and the gait CORRECTLY planted on it; census-proven, and
no strand band exists in a 16-high cage), part damage twins + env
rule (third-ant fix for the vanilla hurt-cooldown), stream-gate
predicate, keyframe round-trip at 6 + negative path (legs=7 dies at
decode). Rig span pins added BOTH rigs (independent table recompute).

**Three-reviewer pass:** neutrality PROVEN (no finding); one MAJOR
(both remaining reviewers converged): the ant contraction floor
mirrored the WRONG classic constant — 1.375 (22 px) is classic's
relocation-trigger window, the probe sweep's floor is 2.5 — fixed to
2.5 per the spider's sweep-floor convention, design doc amended (the
S5 research note itself carried the misattribution). Minors fixed:
harness self-reference, "diagonal pairs" doc error (the classic
transcription comment was wrong against its own table — corrected in
both places), out-of-cap harness target relabeled in-cap, orphaned
javadoc removed, test ants hardened (owned=1 — customServerAiStep
combat survives removeFreeWill). Recorded as accepted tuning: the
+/-2 index-neighbor inhibitor permits ant mid+rear same-side co-swing
(review-verified deadlock-free, pairs never co-swing, >=3 legs always
planted, classic's own scheduler was looser).

GATE: suite 183/183 MODERN and 183/183 CLASSIC (pass lines captured
literally), build+assetAudit 0 err / 0 adv / 3 ack.

**THE 2.0 SPIDER OVERHAUL LEDGER — CLOSED:**
- S1 design + approval ................ aa73329
- S2 gait core (FABRIK, sync, gate) ... ef5f14a  (+ BUG-035 ad129e2)
- S3a terrain ......................... a8fe2f2
- S3b body dynamics ................... beafdd4  (+ laws/research 5ecfe1a)
- S4 multi-part hitboxes .............. 9fc2389  (red-gate cycle honored)
- S5 research ......................... f783d78
- S5a ride + stream gate + seat ....... 893147f
- S5b LegRig + ant (this commit) ...... closes the ledger
Laws in force throughout: pass-line gates, harness independence,
player-path testing, one-writer, per-rig classic probe geometry,
wire-change registrar bumps, three-reviewer review per slice,
red-gate, escape hatch. Nothing pushed — the owner's in-game sitting
decides the release.

## S6a — sitting F-1: part interact forwarding + OBS logging (2026-08-13)

The 2.0 verification sitting (SITTING_2_0.md) settled F-1: spider
mount wiring was CORRECT and classic-faithful all along (the box is
the ground-level core, as in 1.0; ids exonerated by body melee) —
the one real regression was that MHLibPartEntity swallowed
interactions (no interact override; part clicks died as PASS, giving
modern robots FEWER working click angles than classic). Vendored
fix, owner-ratified as a deliberate better-than-classic delta: parts
forward interact() to the parent's full vanilla chain — legs are now
clickable mount surfaces. Neutrality: Queen has no mobInteract (part
clicks route to the same vanilla default as her body); King/Godzilla
parts are OreSpawnPartEntity, untouched. No wire change (no
registrar bump).

Also per sitting rulings: OBS-1 (rider inside ant shell) verified
1.0-parity BY CONSTRUCTION (positionRider not mode-gated; dynamics
never move the rider) — kept, MOD-027 records the seat-raise
candidate. OBS-2 (one-box leg alignment) logged as the accepted Q3
design cost — MOD-028 records the per-segment upgrade; new suite pin
locks live part boxes to the solver's chord anchor within 1e-6 so
drift can never go silent. KNOWN_ISSUES: mount-spot guidance (ground
core, as in 1.0), legs-clickable delta, ant ownership gate
({AntRobotOwned:1} for summons).

Tests +3 (=186): s6_mount_through_leg_part (player-path: interact
driven through a real leg part → mounted; classic body-click parity
control alongside), s6_queen_part_interact_neutrality (part result
== body result, never mountable), s6_part_anchor_chord_pin.

GATE: suite 186/186 MODERN and 186/186 CLASSIC (pass lines
captured), build+assetAudit 0 err / 0 adv / 3 ack. Nothing pushes.

## S6b — THE LEG FIX: reference mechanisms replace ours (2026-08-13)

Owner-approved P1+P2+P3 from the reference-code addendum; the cloned
reference source was re-read per mechanism before implementing
(code-as-authority rule), and each mechanism was reviewer-verified
side-by-side at the boundaries.

**P1 — zero-lag rest frame + rotation-latched trigger** (reference
Leg.updateMemo + lerpedGait/isRotatingYaw): the S5a dead-band chase
is DELETED — it lagged the rest frame by design, which is exactly
what parked planted feet contralateral through every fast turn (the
sitting's crossing mechanism). Rests are now pure functions of
current yaw; the anti-dance duty moved to a rotation-forced trigger
radius (>0.5 deg/tick arms a 10-tick latch forcing the moving
radius). The look-jitter zero-motion pin stays green — and is now
DISCRIMINATING (spider pin widened to +/-10 deg, the latch-only
band; new ant +/-6 deg pin where the latch is load-bearing).

**P2 — comfort invalidation + candidate validity** (reference
comfort capsule + canMoveLeg's unconditional first line): a planted
foot outside the valid plant region — comfort disc around the
CURRENT rest (spider 6.0, ant 3.0, both under the rig's min lateral
rest offset so a valid foot can NEVER cross the midline) OR the
classic contraction corridor (1.5 half-width along the hip->rest
ray, floored at the classic sweep floor — our deliberate divergence
from the reference, which has no corridor: the classic bridge-grip
law) — lifts IMMEDIATELY, bypassing the pair/neighbor inhibitors
(forcedLift observability; S2 inv3 amended: co-swing legal iff a
lift was forced). Candidates are filtered by the SAME predicate, so
the generator is structurally unable to emit a contralateral plant.
Implementation reds that taught real lessons: comfort-ONLY
invalidation oscillated against legal corridor edge-grips
(plant->invalidate loop) — unified to one predicate both places; and
plants must KEEP satisfying the 3D reach guard their candidates
passed (reviewer MAJOR: the ant front pair could SETTLE ~0.9 past the
render cap — a standing clamp slide, not sub-visual) — the plant
validity now includes the reach cap, and the ant walk test regained
its independent literal 3D bound.

**P3 — swing advection + clamped yaw lead** (reference
applyBodyMotion + lookAheadPosition): in-flight swing origins
inherit body translation + yaw rotation per tick (both sides); the
drift-scan target leads by one tick of yaw rate CLAMPED to ~10 deg
(the raw delta over-rotated on snap turns). Plus the review MAJOR:
**mid-swing target revalidation** — in-flight targets are re-judged
against the current frame every tick and re-targeted (fresh step
payload) or stranded, the reference's locateGroundTarget-per-tick +
softResetStep behavior; without it, landings went stale above ~4.5
deg/tick of sustained rotation and churned. Pinned by a 6 deg/t
churn phase asserting real plant streaks. Client cold-start yaw
seeding fixed (payload-initialized gaits rotated swing origins up to
180 deg on their first tick).

**Recorded for owner sign-off (boundary report): sprint forced
lifts.** At high straight-line speed the comfort margin is smaller
than the inhibition windows, so inhibited steps convert to forced
lifts — the reference behaves identically EXCEPT its body brakes
(uncomfortableSpeedMultiplier 0.0) prevent sustained max-speed
discomfort; our vanilla-driven bodies cannot brake, so we churn
forced lifts at sprint instead of slowing. Ratify or direct the
velocity-compensated-anchor alternative. Also recorded: the latch
threshold (0.5 deg/t vs the reference's ~0.06 desired-omega — an
external-yaw adaptation; slow-turn band accepted as tuning), no
vertical bound in the comfort disc (the 3D reach cap + vertical
retrigger bound that axis), no airborne step gate (reference has
one; our stranded/dangle covers true airborne), and the pre-existing
S3-era in-place re-step cycling at long-term corridor grips (out of
scope, logged).

Tests: net +4 (=190): spider spin/churn/flick side+comfort
invariants every tick, ant spin/flick, edge-platform no-crossing +
resolution, ant look-jitter; jitter pin widened; inv3 amended; ant
walk bound restored; independent envelope bounds added alongside the
predicate oracles (review: self-referential-oracle guard).

GATE: suite 190/190 MODERN and 190/190 CLASSIC (pass lines
captured), build+assetAudit 0 err / 0 adv / 3 ack. Nothing pushes.

## S7a — the LOST SEAT restored + modern composition (2026-08-13)

Sitting-3 FAIL-3 triage found a parity bug older than the overhaul:
the spider's ORIGINAL seat system (orig SpiderRobot.java:523-536,656
— rider 3.0 behind ±0.05 bob, 2.625 up ±0.02 bob, SpiderDriver flat
2.0, players −0.5 per TF-029, rideTicker clock) was NEVER PORTED; the
port fell back to the vanilla anchor-level seat. Restored for BOTH
modes (classic = term-for-term the original, reviewer-verified).
MODERN composes the seat point through the S3b body transform — the
same frame the leg parts ride — so the rider rides the visual body;
honest bounds: ridden clamp covers lift only, pitch/roll keep
MAX_TILT (seat may swing ~1.3 on steep terrain), and under tilt the
rider slips vs the rendered shell by ~1.501·sin(tilt) (accepted
constant-quirk family). Ant: modern-only +0.9 raise + composition
(MOD-027 shipped); classic ant seat untouched. Test s7_seat_geometry
(=191): classic exact pins (driver 2.0/3.0, player 2.125 TF-029 arm),
modern inverse-transform pins over DISCRIMINATING tilt terrain
(reviewer: flat ground degenerated the assert to identity). Reviewer
verdict: approve, no functional blocker; new sitting item recorded
(180° flick × the 3-block seat arm — eyes-on; seat-yaw smoothing is
the fallback). Out-of-scope catch escalated separately: ModEntities
spider .sized(2.0,1.5) vs orig setSize(3.25,2.25) — possible
pre-existing dims parity deviation, needs its own investigation
(touches the S4 profile main-size law if real).

GATE: 191/191 MODERN + 191/191 CLASSIC (pass lines captured), build
0/0/3. Camera (FAIL-4) design presented; implementation awaits the
owner's paragraph approval. Nothing pushes.

## S7b — the riding camera (2026-08-13)

Sitting FAIL-4, owner-approved design, SSR technique (MIT, studied
not copied): WrapOperation on Camera.setup's zoom-arm move (ordinal
0; WrapOperation so SSR's own redirect of the same instruction
chains instead of crashing — reviewer MEDIUM), arm-only time-based
smoothing (~0.25s constant; pivot rides the S7a seat RAW so the two
systems cannot stack — the owner-flagged interaction, refuted with
mechanism by the reviewer), 8-corner collision snap-in/glide-out
(recovery time-based per reviewer), spider 10 / ant 6 + up 2 +
shoulder 1.5, mountCamera config (default on), byte-identical
vanilla fall-through (verified against bytecode). Stale-state
1s-gap reset. Client-visual only; suite 191/191 both modes
(camera untestable server-side — reviewer-verified vs the mapped
jar; feel is the owner's next sitting).

## S7c — spider dims parity restored: ENT-S-088 (2026-08-13)

The S7a out-of-scope catch, investigated and owner-ruled: "original
wins, per law." ORIG SpiderRobot.java:58 sets 3.25x2.25 in the ctor
— the ONLY size call in the class, no ridden/child restate, and
1.12.2 has no SpiderRobot at all — while the port registered an
uncited 2.0x1.5. NOT a ruled deviation: the ENT-S-020 stats row
never examined size, so the spider slipped through the exact
audit crack the Phase C sweep caught on Alien/Cephadrome/PrinceTeen;
the S4 profile then codified 2.0x1.5 as "classic dims" unverified.

Restored 3.25x2.25 in LOCKSTEP per the Size-hook law: ModEntities
.sized(3.25, 2.25) with the orig citation + profile
spider_robot.json main [3.25, 2.25], law comment rewritten to state
the GENERAL rule (profile main size = classic EntityType dims,
EXACTLY — a mismatch silently forks modern dims from classic via
MHLib's EntityEvent.Size hook). Guard gap the drift exploited now
closed: s4_part_counts_and_classic_zero pins 3.25x2.25 in BOTH
modes (the ant's s5b/i083 mirror; the spider had NO dims pin — no
gametest anywhere asserted its box, i083's table is A-entities
only). Net test count unchanged (=191, amended not added).

Derived ranges self-correct (live getBbWidth reads): SpiderDriver
mount-seek (4+w/2)^2 5.0->5.625 and drive range 11+w/2
12.0->12.625 — both now the numbers the original computed from its
3.25 width; default eye height 0.85h 1.275->1.9125 = the 1.7.10
default. Seat unaffected (S7a constants are absolute orig values —
designed for the 2.25-tall box, so the restored dims make the seat
MORE coherent, not less). KNOWN_ISSUES mount-spot paragraph
rewritten; its "in 1.0 too / never was the hitbox" claim corrected
honestly (true of port 1.0's small box, false of 1.7.10).

ANT CROSS-CHECK (owner-ordered, same audit crack): AntRobot is
CLEAN — orig AntRobot.java:52 setSize(2.75, 1.25) == port
registration (cited) == ant_robot.json profile == i083 + s5b pins.
Both robots' dims are now positively parity-cited, not assumed
(recorded in ENT-S-088's note).

CAMERA RE-VERIFY (S7b landed before this slice; owner sequencing):
analytically dims-independent — the pivot rides the S7a seat's
absolute constants, the arm constants (10/6, up 2, shoulder 1.5)
are absolute, and collision clips BLOCKS only (ClipContext VISUAL;
the vehicle entity never occludes its own camera), so the bigger
box cannot move the camera in code. The real interplay is
INDIRECT: 3.25x2.25 changes which tight spaces the spider can
enter at all. Two items added to the camera reviewer list and
recorded as sitting eyes-on (SITTING_2_0.md): framing correctness
with the taller/wider body; the collision arm against the bigger
suffocation profile in tight spaces.

GATE: suite 191/191 MODERN and 191/191 CLASSIC (pass lines
captured; the flagged seat/ride tests green in both modes — the
S7a seat constants are absolute, so the dims change never touches
them), build+assetAudit 0 err / 0 adv / 3 ack. Nothing pushes;
eyes-on items go to the owner's sitting.

## BUG-037 — invented wild royal spawns deleted + TEST-003 partial (2026-08-11)

(Renumbered from BUG-036 at release prep: two parallel sessions both
assigned BUG-036 on 2026-08-11 — the creeper demo-profile fix keeps the
ID, this later royal-spawns entry becomes BUG-037. Commit 4ea395c's
message retains the old number; history is not rewritten.)

Field report (owner, CrazyCraft 5.0): Princess spawned beside the player
on a fresh world. Verified NOT original: the complete orig addSpawn
roster (55 classes) has no royalty; companion_royalty.json was pre-audit
Phase 4E content — the lone survivor of six invented companion-spawn
files (its five siblings were deleted during the audit) and covered by
no finding. Deleted per the faithful-first contract (option D over the
reporter's A-C: gating/narrowing would preserve invented content).
checkSpawnRules stay faithfully always-true (orig :369-371/:381-383);
chunk-gen pre-population of roster-backed CREATUREs (girlfriends at
world spawn!) is original behavior, untouched. Regression net in
SpawnGateTests with a positive control. TEST-003 opened: boss005/boss012
moved to per-test isolated batches (the proven default-batch detonators);
remaining unbatched config-flip sites listed for an design ruling.
Pre-audit-provenance sweep flagged ~48 modifier files whose last content
touch predates Phase C — most were verified-without-modification by
C/D/E phases, but the royalty case proves unvalidated survivors exist:
a modifier-provenance audit (each file's spawner list diffed against the
orig addSpawn table + dimension rosters) is proposed as a follow-up.

## BUG-035 follow-up — Queen sweep items dispositioned (2026-08-20)

Owner directed the held Queen-pass inputs (BUG-035 entry above) be
finished. Verified first: the BUG-035 loop:false fix is in-tree and in
the shipped beta jar; melee trigger keys match the registered
triggerableAnims; all 10 MHLib hitbox-profile bone refs and every
animation-keyed bone exist in the geo (110 bones). Dispositions:

- (2) Movement dead-latch — FIXED (defensive): predicate now calls
  forceAnimationReset() before returning STOP on isDeadOrDying(), so a
  one-frame client-side flicker can no longer latch the controller
  STOPPED against an unchanged RawAnimation instance. Real death path
  unaffected (entity removed shortly after). The underlying same-
  RawAnimation no-op claim remains unverified against GL bytecode; the
  guard is correct in either case (forceAnimationReset() confirmed
  present in pinned 4.8.4 via javap).
- (3) Stalled-WAKING statue — FIXED: the TRANSITION_TICKS countdown
  moved from customServerAiStep() to tick() (server-side guarded), so
  the wake-up can no longer stall unrecoverably if AI stops being
  reached while ticks>0.
- (4) WAKE_UP_DURATION_TICKS 60 vs 71.7t clip — FIXED: 72. Previously
  recorded benign, but it cut the final 12 ticks of the authored
  wake-up before the stance promotion; 72 lets the clip play out.
- (5) QueenPrimaryGoal cadence — FIXED: requiresUpdateEveryTick()
  override added (orig func_70030_z_ ran every AI tick; the modern
  goal system ticks running goals on alternate ticks without it).
  NOTE: KingPrimaryGoal has the same gap — NOT fixed here (out of the
  Queen brief's scope; the King was field-tested at current cadence).
  Recorded as a follow-up candidate.
- (6) hurt() wake-arm ordering — FIXED: the transition arm moved from
  the top of hurt() to after the damage filters (immediately before
  super.hurt), so healed explosions, discarded tiny-monster attackers,
  inWall, and invuln-window hits no longer wake her.
- (7) doc/code mismatch ("hits 1" vs flip at 0) — FIXED with (3): the
  relocated comment now says hits 0, matching the code.
- (1) post-wake calm-state divergence (write-once IS_AWAKE latch vs
  orig's dynamic getAttacking() wing keying) — STILL HELD: a return-to-
  calm would also revert the invented blue/red texture phase mid-fight;
  that is a presentation design decision, not a defect. Owner call.

Client-visual items ((2), (4)) are not suite-assertable; (3), (5), (6)
are server logic. Gate: full build green (see commit).

## WGEN Chaos round 2 — density inversion + BlendedNoise unit conversion (2026-08-21)

Owner field reports, two rounds: "giant flatworld of stone", then after
the first fix "still doesn't look right — more verticalness, hills and
mountains". Three source-verified faithfulness bugs, each alone enough
to flatten the dimension:

1. INVERTED THRESHOLD. orig ChunkProviderOreSpawn6.func_147419_a
   renders the density field as stone-by-default with AIR where
   d15 > 0 — the photographic negative of the Nether (whose math it
   copies verbatim). The port used the standard positive=solid
   convention. Fix: final_density wrapped in mul(-1).
2. CELL-vs-BLOCK SAMPLING. Legacy noise coords are per 4x8x4 CELL;
   modern BlendedNoise samples per BLOCK (compute(): blockX() *
   684.412 * xz_scale — verified in the decompiled 1.21.1 source).
   Correct conversion is xz_scale 0.25 / y_scale 0.375 — exactly what
   vanilla's own legacy-Nether port uses (nether/base_3d_noise.json).
   The port had 1.0/3.0: 4x too fine horizontally, 8x vertically.
3. /128 NORMALIZATION. BlendedNoise.compute() returns
   clampedLerp(min/512, max/512, sel) / 128.0 (same decompiled source,
   last line). The port's spline/blends are in legacy raw units, so the
   noise arrived ~128x too weak and the ±2 cosine band dictated the
   shape: deterministic flat plates at band heights. Fix: mul(128) on
   the noise term, keeping the whole router in legacy units.

Also this round: no sea (orig places no fluid; water branch at
replaceBiomeBlocks:161 is dead code; default_fluid air / sea_level 0),
grass+dirt default surface at ALL heights with the Y60-65 band the only
patchy zone (orig :137-160 — the first fix had this backwards), and the
teleporter landing hunt ported (orig OreSpawnTeleporter.justPutMe:88-129
random-walk; findSafeY -> findSafeSpot, void columns no longer
blind-drop at Y64).

Verification: tools/chaos_slice.py re-implements the legacy octave
stack and renders orig math vs shipped-bug vs fixed JSON side by side —
fixed is numerically identical to orig (max |A-C| = 0.0) and shows the
rolling grass highlands/mountains from the field report; the shipped
bug reproduces the reported flat plates. Gates: build+assetAudit exit 0,
gametest 192/192.

## PHASE G0 — GeckoLib inventory, tier proposal, and approval stop (2026-08-31)

WHAT: added the read-only mechanical inventory generator
`tools/phase_g_inventory.py` and its generated 109-row design report at
`phase_g_reports/geckolib_migration_design.md`. The report inventories
108 hand-coded models plus the completed Queen GeoModel (36,403 model
LOC), all 121 custom-model registry consumers, the seven vanilla-model
reuse consumers, registered dimensions, renderer scales, animation-risk
classes, audit references, and texture/provenance data. It proposes 3 / 20 /
70 / 16 custom rigs in Tiers 0 / 1 / 2 / 3, gives a server-authoritative
multipart design for every Tier-1 rig, records the visual-parity and
performance policies, and ends at ten numbered owner questions. No geo,
animation, profile, entity, renderer, or shipped asset was converted or
changed.

WHY: Phase G spans 100+ visual migrations and can silently change classic
presentation, damage surfaces, and frame/server cost. G0 makes the source
set and policy choices reviewable before any irreversible-looking batch
work. King/Godzilla/Mothra manual parts remain in the recommendation until
their replacements pass independently formulated parity; the faithful
King/Queen/Godzilla head sidecars remain unless the owner explicitly rules
otherwise. New Tier-1 damage positions are recommended server-fed rather
than extending the Queen's client-trusted exception.

EVIDENCE: the generator hard-fails unless it sees 109 models, 108
hand-coded models, 36,403 model LOC, 428 entity PNGs, 426 provenance rows,
and seven vanilla reuse renderers. Its scan found 338 unique PNG payloads,
86 duplicate groups / 90 redundant names, 254 renderer-referenced names,
and only `blue_queen.png` / `red_queen.png` without byte-identical 1.7.10
provenance. A document check proved 109 non-empty model rows, 20 matching
Tier-1 hitbox designs, ten owner questions, and byte equality of the copied
603-line `PHASE_G_PROMPT.md`; certutil reproduced SHA-256
706CB5D662B6E1FF98D952DA3DF3994050A2E9A0FB394F91E712B9792D15641D.
The read-only staging survey recorded tracked `extracted/` (1,370 files),
main-checkout-only `temp_queen/` (eight scratch files), and
`blockbench_exports/` (373 files) without importing or modifying them.

BASELINE NOTE: the first baseline build stopped making progress inside a
NeoGradle PIDBasedFileLock wait. It was interrupted after diagnosis,
`gradlew --stop` returned 0, and one non-concurrent retry completed with
`RESULT: 0 error(s), 0 advisory(ies), 3 acknowledged -> exit 0` and build
exit 0. No Gradle invocations overlapped.

GATE: build exit 0 with literal asset-audit line `RESULT: 0 error(s), 0
advisory(ies), 3 acknowledged -> exit 0`; runGameTestServer explicit exit
0 with literal suite line `All 192 required tests passed` (N verified as
192). G0 STOPS at the owner approval gate; no G1-G5 work follows this
entry.

## PHASE G1 — LayerDefinition converter + independent parity harness (2026-08-31)

WHAT: built a non-production `g1tool` source set which executes each compiled
`createBodyLayer()`, serializes the definition and baked `ModelPart` trees with
fixed LF output, converts those trees to Bedrock geo, bakes the result through
GeckoLib 4.8.4, and captures the real `ModelPart.Cube.compile` and
`GeoRenderer` vertex paths. Exact source bone names are mandatory. Explicit
per-face UV output preserves mirrored ModelPart semantics without GeckoLib's
incompatible native mirror. `ModelElevator` is the genuinely static visible
Tier-3 proof; `ModelBeaver` is the Tier-2 proof. A deterministic, non-production
fixture separately covers nested parents, non-mirrored UVs, nonzero bind
rotations, and uniform inflate. No production model, renderer, entity, resource
registration, or shipped asset was migrated; G2 did not begin.

ANIMATION SCOPE: Elevator stays at its static bind pose with no controller.
Beaver uses the owner-approved G1 legacy-parity exception: a fresh GeckoLib
`BakedGeoModel` invokes the exact legacy `Mth.cos` formulas through
`GeoModel.setCustomAnimations`. Compiled Java independently executes
`ModelBeaver.setupAnim`; Python only compares the two captured results. The
emitted `model_beaver.animation.json` is mechanically labeled
`REFERENCE_ONLY_NOT_RUNTIME_ACCEPTANCE`, is never loaded or used by acceptance,
and has a throwing `getAnimationResource` guard. Its 4,745 authored timestamps
per channel / 28,470 constant vectors pass generic schema checks only. This does
NOT claim baked-keyframe runtime acceptance or completion of PHASE_G_PROMPT's G3
math-to-artist-editable-keyframes work; that remains `OUTSTANDING_G3`.

PARITY EVIDENCE: immutable regeneration is green against
`phase_g_reports/g1_proof/evidence/report.json`. Elevator has 5 exact bones / 5
cubes, maximum geometry, UV, normal, animation, changed-pixel, and pixel-MAE
deltas all `0`. Beaver has 9 exact bones / 9 cubes: maximum corner delta
`2.00000000116773e-7` blocks, UV/normal delta `0`, actual-candidate rotation
delta `0` radians, changed-pixel fraction `1.52587890625e-5` (limit `0.001`), and
MAE `0.00335184733072917` (limit `0.25`). Its runtime-pose gate covers 2,401
samples total: 2,380 dense samples including endpoints across amplitudes
`0/.25/.5/1`, of which 2,372 are off-grid interiors. Zero probes coincide with
reference keys within `1e-9` tick; minimum separation is
`0.0007339999999942393` tick. Maximum dense runtime-pose error is `0`; maximum
candidate gait-amplitude proportionality error is `5.00000000291934e-8`
radians, and unscaled teeth/tail amplitude error is `0` (all below `2e-6`). The
fixture's max corner/normal deltas are `1.41509716908816e-7` /
`1.51327459479588e-7`, with UV delta `0`. All 18 generated text captures are LF.
The visual output directory is cleared first and contains exactly the 30 PNGs
referenced by the report; stale Coin and old Beaver captures are rejected.

PERFORMANCE EVIDENCE: the checked-in report is deliberately labeled
`SMOKE_ONLY / COMPONENT_PROXY_ONLY / PENDING_LIVE_PRECUTOVER`. It executes the
current compiled classic pose and current generated-geo/custom-hook candidate;
any `*.poses.json` makes the benchmark fail. Two measured runs per scene use
AB/BA alternation per timing batch. Seed, camera, and resolution are `N/A`.
The mixed-100 component proxy measured classic/candidate median
`0.08837500000000001 / 0.16547 ms` and p95
`0.108845 / 0.20591 ms`, producing exact WARNING numbers
`87.23620933521923%` median-ratio delta and `0.09706500000000001 ms` p95 delta.
These component-only warnings are not evaluated against or substituted for Q6.
The validator independently recomputes per-run/aggregate 1% lows, aggregate
median/p95/p99, scene regressions, the configured budget scene and both warning
fields, and cross-checks the provisional/live limits. Evidence is bound to base
revision `aa5b8637b2457a987445161b1c7f2f6ebe0b9d59`, exact benchmark/gate/build/
manifest source hashes, compiled dumps, generated geo/current candidate inputs,
and loaded GeckoLib/NeoForge/Minecraft artifacts and representative class hashes.
The real same-machine 60-second warmup + five 120-second runs for every live
scene—including actual off-screen/culling, Tier-1, GPU, server, allocation/GC,
MHLib packets and part-count metrics—remains a mandatory pre-cutover Q6 gate.

REVIEW: controller-side independent read-only re-reviews returned PASS for
parity, fixture coverage, stale-proof rejection, benchmark provenance, and the
recomputing validator. Both proof writers were followed by immutable no-write
verification (`g1Parity` + `g1BenchmarkVerify`, exit 0).

FINAL GATE: guarded `build` exit 0; `assetAudit` printed literal `RESULT: 0
error(s), 0 advisory(ies), 3 acknowledged -> exit 0`; immutable parity and
benchmark proof verification passed; `jarJar` completed; `runGameTestServer`
exit 0 with literal `All 192 required tests passed`. G1 closes here. Q6 remains
`PENDING_LIVE_PRECUTOVER`; no production cutover, push, or G2 work occurred.

## PHASE G LANDING — G0 + G1 fast-forwarded to master; G1 provenance defect fixed (2026-09-02)

WHAT: fast-forwarded `master` from `5733200` to `fcf0f48` — `aa5b863` (G0
inventory generator + design doc) and `fcf0f48` (G1 converter + parity
harness), both produced by the AO orchestrator run of 2026-08-31 on
`ao/phase-g-integration`. The landing gate was re-run on `master` itself.

DEFECT FOUND ON LANDING (disclosed per the c4a7390 standard): `fcf0f48` as
committed could not pass its own build gate on a fresh checkout. Its G1 entry
above records a green `build`, and the worktree log confirms the suite line
(`All 192 required tests passed`, orespawn-4 worktree, 2026-08-31 20:32 JST) —
but that gate ran against mixed-CRLF working bytes of `build.gradle` (SHA-256
`fcd276…`, 243 CR bytes) and the commit then normalized the file to LF (blob
`6f1276…`) through the new `.gitattributes` rule. `tools/g1_benchmark_gate.py:166`
hashes the LIVE `build.gradle` against the value recorded in the checked-in
report, and `check` (hence `build`) depends on `g1BenchmarkVerify`, so every
fresh checkout of `fcf0f48` is red. Reproduced on `master` before fixing:
`AssertionError: benchmark source provenance drift: build.gradle`, exit 1.
The orchestrator had hit the same false red (worker orespawn-10, an
uncommitted 1,708-line "evidence policy" migration in its worktree); that
approach is NOT adopted here.

FIX (minimal, tool-driven): regenerated the SMOKE_ONLY component benchmark on
`master` with `gradlew g1Benchmark` and rewrote the checked-in proof set with
the gate's own writer mode (`g1_benchmark_gate.py --write-proof`):
`g1_proof/benchmark/report.json` + `README.md` changed; `protocol.json` is
byte-identical. Nothing else under `g1_proof` changed, and
`g1_proof/evidence/report.json` carries no reference to the benchmark, so
`g1Parity` is untouched. The report's `build.gradle` hash is now the committed
blob (`6f1276…`) and its base revision is `fcf0f48`. The numbers quoted in the
G1 entry above (mixed-100 median-ratio delta 87.24 %, p95 delta 0.097065 ms)
are superseded by 107.29 % / 0.111945 ms — same-machine component-proxy timing
noise under the unchanged `SMOKE_ONLY / COMPONENT_PROXY_ONLY /
PENDING_LIVE_PRECUTOVER` labels; no Q6 claim is made or changed. The original
report remains in history at `fcf0f48`. `g1BenchmarkVerify` on `master`: exit 0,
literal `G1 BENCHMARK EVIDENCE VERIFIED … checked-in proof verified`.

NOT LANDED: everything else from the orchestrator run stays uncommitted in its
`~/.ao` worktrees — G2 Tier-3 rigs + texture map (orespawn-4), G3 Beaver
GeoReplaced candidate (orespawn-7), G4 audit extension (orespawn-5) and
server-pose foundation (orespawn-8), Q6 live runbook (orespawn-6), case-alias
micro-slice (orespawn-9), G1 evidence policy (orespawn-10). Salvage material
pending owner triage; see the Phase G progress review of 2026-09-02.

INCIDENTAL: the Chaos round-2 entry above (2026-08-21) had been written with
raw cp1252 bytes (0x97 em dash x7, 0xB1 plus-minus x1), making FIX_LOG.md
invalid UTF-8; an editor pass during this landing turned those 8 characters
into U+FFFD. Restored as the intended proper UTF-8 characters, verified
byte-for-byte against HEAD; FIX_LOG.md is now valid UTF-8 throughout.
Follow-up candidate: a UTF-8 validity check on tracked docs in
`tools/asset_audit.py`.

GATE (on master, HEAD fcf0f48 + this fix, sequential, no overlap): `build`
exit 0 — asset audit literal `RESULT: 0 error(s), 0 advisory(ies), 3
acknowledged -> exit 0`, `G1 PARITY PASS: 2 models; checked-in proof
verified`, `G1 BENCHMARK EVIDENCE VERIFIED … checked-in proof verified`,
jarJar built, `BUILD SUCCESSFUL in 47s`; `runGameTestServer` exit 0 —
literal `All 192 required tests passed` (N verified as 192; `192 GAME TESTS
COMPLETE`), `BUILD SUCCESSFUL in 1m 22s`. Not pushed; publish is the
owner's call.

## PHASE G SALVAGE — worktree quarantine + inventory + texture-case finding (2026-09-02)

WHAT: the eight AO worktrees left dirty by the 2026-08-31 orchestrator run were
snapshot-committed verbatim onto their own `ao/*` branches (bc3a931 G2
foundation, e66b145 G4 audit, 7dc7a95 Q6 live, 0d238ba G3 runtime, 98d6df4
server pose, 2636d20 case fix, d7acf3f G1 evidence policy, 394a7b6 integration)
- NOT gated, NOT reviewed, NOT merged; `master` contains none of it. Triage of
every lane, verdicts, and a six-slice re-landing order are recorded in
`phase_g_reports/phase_g_salvage_inventory.md` (this commit).

FINDING (live bug, not fixed here - presented for ruling): git's index tracks
147 entity textures under UPPERCASE names (`Kyuubi.png`, `GammaMetroid.png`,
`Fireflytexture.png`, ...) while this checkout has all 428 lowercase on disk;
`core.ignorecase=true` hides the difference. Every Java reference is lowercase
(57 of the 147 are hit by a literal path, more by dynamically built names) and
the shipped beta jars contain 428 lowercase entries ONLY because they were
built from this working tree. A fresh clone on any OS writes the index names,
Gradle copies them into the jar, and the case-sensitive jar filesystem then
fails those texture lookups in-game. Verified 2026-09-02: `git ls-files` vs
`ls`, jar listing (0 uppercase entries), Java literal scan (0 uppercase
literals), provenance file (0 uppercase port paths). Recommended fix is
Slice 1 of the inventory: 147 index-only `git mv` renames (disk and jar
byte-identical to today) plus an asset-audit check that texture literals
resolve against index names. The orchestrator's build-time alias generator
(worker orespawn-9) is not adopted.

INCIDENTAL: `phase_g_reports/` now also carries this inventory; the asset audit
does not read `phase_g_reports/`, so this commit is docs-only in effect.

GATE (on master, sequential): `build` exit 0 - asset audit literal `RESULT: 0
error(s), 0 advisory(ies), 3 acknowledged -> exit 0`, `G1 PARITY PASS: 2
models`, benchmark evidence verified; `runGameTestServer` exit 0 - literal
`All 192 required tests passed` (`192 GAME TESTS COMPLETE`). Not pushed.

## BUG-038 — resource files tracked under uppercase names; index-only rename + audit check 7 (2026-09-02)

WHAT: Slice 1 of the Phase G salvage plan. 155 `git mv` case-only renames
(147 `textures/entity`, 3 `textures/items`, 1 `textures/blocks`, 4 `sounds`)
so the git index matches the lowercase names that the disk, every Java
reference, `sounds.json`, the provenance file and the shipped jars already
use. Content untouched (staged blobs identical to HEAD), disk untouched, no
lowercase collisions, no uppercase directories outside META-INF. Added asset
audit check 7 (`check_index_case`): reads `git ls-files` and errors on any
tracked `assets/`/`data/` path that is not ResourceLocation-valid lowercase
(`RESOURCE_PATH_CASE`) and on any Java asset literal that exists on disk but
is not tracked under that exact name (`TEXTURE_REF_CASE`); if git is
unavailable it emits one ADVISORY rather than passing silently.

WHY: a fresh clone anywhere would have shipped the uppercase names and lost
those textures and sounds in-game; the release jars were correct only
because they were built from this particular checkout. Full analysis in
AUDIT_FINDINGS BUG-038. The orchestrator's build-time alias generator
(salvage lane orespawn-9) is not adopted - the index rename is the whole fix.

VERIFICATION: audit on the renamed tree prints the standing pass form (0 err /
0 adv / 3 ack). Mutation test: `git mv -f kyuubi.png Kyuubi.png` -> audit
reports `RESOURCE_PATH_CASE` and `TEXTURE_REF_CASE` errors, exit 1; restored
-> clean again (results recorded in the GATE line below).

GATE (on master, sequential): audit on the renamed tree `RESULT: 0 error(s), 0
advisory(ies), 3 acknowledged -> exit 0`; mutation `Kyuubi.png` -> `RESULT: 2
error(s) ... exit 1` (RESOURCE_PATH_CASE + TEXTURE_REF_CASE), restored ->
clean; `build` exit 0 (same audit line, `G1 PARITY PASS: 2 models`, benchmark
evidence verified); built jar: 428 entity textures, 0 uppercase entries under
assets/; `runGameTestServer` exit 0 - literal `All 192 required tests passed`
(`192 GAME TESTS COMPLETE`). Not pushed.

## PHASE G SLICE 2 — GeckoLib replacement seam + Beaver developer candidate (2026-09-02)

WHAT: re-landed the core of salvage lane orespawn-7 (G3) as ordinary code,
~330 LOC across eight classes: `GeoReplacementDescriptor` (registry identity +
resource triple + texture/scale hooks), `OreSpawnGeoReplacement` (one
`GeoReplacedEntity` singleton per registry entry; GeckoLib keys its state by
entity id, so the ENTITY CLASS IS NEVER TOUCHED), `OreSpawnGeoReplacementModel`
(the one shared GeoModel; `setCustomAnimations` delegates to the animatable),
`OreSpawnGeoReplacedEntityRenderer` (shared base), `BeaverGeoReplacement` +
`BeaverGeoReplacedRenderer`, `DevRendererSwitch` (pure policy) and
`PhaseGDevRenderers`. `OreSpawnClient` registers Beaver through the switch;
classic `BeaverRenderer` stays the default and its layer definition stays
registered. Assets: `geo/entity/beaver.geo.json` (the landed G1 converter's
output, byte-identical to `g1_proof/generated/model_beaver.geo.json`) and an
empty `animations/entity/beaver.animation.json`. Vendored MHLib: bone
collection is now a strict no-op for a replaced renderer whose entity has no
profile, or whose profile names no bones or neither syncs nor trusts the client
(the G3 hunks, predicate widened after review). Gametest `phaseg001` pins the
switch contract (suite 193).

HOW TO SEE IT: run the client with `-Dorespawn.dev.beaverRenderer=candidate`
(runClient JVM args, or a Prism instance's custom JVM arguments): Beaver draws
through GeckoLib on the converted rig and a warning line is logged. Without the
property nothing about the game changes.

DESIGN: Beaver's pose is `ModelBeaver.setupAnim` evaluated in
`GeoModel.setCustomAnimations` on the geo bones - the G1-approved code-driven
path, harness-proven within float rounding - NOT the G3 snapshot's 272-line
`PhaseLockedKeyframeController` with 72-segment cosine clips. That controller
subclasses `AnimationController` and writes its protected internals, and it
quietly adopted a 0.0021 rad keyframe tolerance the owner never ruled on. The
code path needs no tolerance, so the artist-editable-keyframe question stays
genuinely open (salvage inventory §5).

REVIEW: three read-only reviewers (API vs pinned 4.8.4 bytecode; parity vs
classic Beaver; regression/safety), all PASS, no blockers; every substantive
finding fixed before the gate: (1) `limbSwingAmount` is read from GeckoLib's
`AnimationState` - its replaced renderer computes it bit-exactly as vanilla,
including the seated-rider zeroing my re-derivation had dropped; (2) baby
shadow: `MobRenderer` scales by `getAgeScale()`, the base renderer now overrides
`getShadowRadius`; (3) the scale hook is guarded on `isReRender`; (4) GeckoLib
4.8.4 defect: `GeoReplacedEntityRenderer.postRender` AND `renderFinal` both call
`EntityRenderer.render`, so a replaced entity's name tag drew twice (once under
the model transform) and, with GeckoLib's own `renderLeash`, the leash up to
three times - the base renderer overrides `postRender` and `renderLeash` as
no-ops, leaving vanilla's single draw from `renderFinal`; (5) MHLib predicate
widened to `syncToModel || trustClient` so a datapack profile with sync=false /
trust=true keeps positioning parts client-side (no shipped profile is affected:
Queen true/true/10 bones, robots false/false/[]); (6) the rotation-basis comment
was replaced - one reviewer derived [-x,-y,+z] from the converter's pivot
mapping while the converter's own notes say [-x,+y,-z]; only X is proven by the
harness, and the comment now says exactly that.

OPEN ITEMS (recorded, not fixed): face order - vanilla emits
down/up/west/north/east/south, GeckoLib west/east/north/south/up/down;
irrelevant for opaque rigs, must be addressed before any translucent or
self-overlapping rig replaces its classic renderer. `SingletonAnimatableInstanceCache`
never evicts: every entity id ever drawn keeps an `AnimatableManager` (nine
bone snapshots for Beaver) for the session - a cost multiplier to weigh before
the pattern reaches 100+ species. GeckoLib translates the model +0.01 blocks
(sub-pixel vs classic). A replaced renderer WITH a bone-synced profile would hit
MHLib's `currentTick=-1` lifecycle gap (`onPostRenderReplacedEntity` never
advances it); Tier-1 profiles are server-fed by ruling Q3, so unaffected.
Spectator/invisible alpha tint (0x27) is not reproduced by GeckoLib's replaced
renderer.

NOT DONE: no in-game capture - client rendering is outside the suite's reach by
construction. The owner's review through the switch is the acceptance step
before Beaver's classic renderer is replaced (ruling Q1).

GATE (on master, sequential): compile of main + gametest clean; `build` exit 0 -
asset audit literal `RESULT: 0 error(s), 0 advisory(ies), 3 acknowledged -> exit
0`, `G1 PARITY PASS: 2 models`, benchmark evidence verified; built jar contains
`geo/entity/beaver.geo.json`, `animations/entity/beaver.animation.json` and the
new classes; `runGameTestServer` exit 0 - literal `All 193 required tests
passed` (`193 GAME TESTS COMPLETE`, +1 = phaseg001). Not pushed.

## PHASE G SLICE 2 — owner in-game review + third-party compat finding (2026-09-02)

REVIEW: the owner ran the Slice 2 build in the CrazyCraft 5.0 instance (201
mods, GeckoLib 4.9.2) with `-Dorespawn.dev.beaverRenderer=candidate`. Startup
clean (switch warning logged, no OreSpawn/GeckoLib/MHLib errors), Beaver
summoned and reviewed: "looks like it works". One issue reported: hats from
Hats Renewed 21.1.1 no longer sit on the beaver's head.

ROOT CAUSE (bytecode of `me.guivnf.mods.hats.client.compat.GeckoLibCompat`):
`isGeckoLibEntity` resolves the entity's renderer and tests it with
`Class.isInstance` against the CONCRETE class
`software.bernie.geckolib.renderer.GeoEntityRenderer`; its reflective head
lookup (`getGeoModel` -> `getBone(String)` over HEAD_NAMES = head/Head/skull/
Skull/neck/Neck/head_pivot/HEAD) is also bound to that class. The seam's
renderer extends `GeoReplacedEntityRenderer` - the sibling GeckoLib renderer
that is the whole point of Slice 2 (no entity-class edits) - so the check
fails and `MixinEntityRenderDispatcher.hats$fallbackRender` places the hat
from `getEyeHeight`/`getBbHeight`, wrong for a low forward-headed rig. The
converted geo DOES carry a bone named `head`; had the check tested the
`GeoRenderer` interface (implemented by both renderers, and the declaring
type of `getGeoModel`), the hat would have been positioned correctly. The
Queen (a `GeoEntityRenderer`) passes the class check but her rig has no bone
in HEAD_NAMES (`LHead`, `NeckL1`, ...), so hats already fell back on her.

DISPOSITION: not fixable in this repo without either giving up the
replaced-entity design or mixing into another mod's classes; reported
upstream to Hats Renewed with the one-line fix. Recorded as a CUTOVER INPUT
for ruling Q1: any third-party mod that hooks vanilla `EntityModel` parts or
checks `GeoEntityRenderer` by class loses a species the day it moves to this
seam. Inventory §5 updated.

GATE (docs-only, on master, sequential): `build` exit 0 - `RESULT: 0 error(s), 0
advisory(ies), 3 acknowledged -> exit 0`; `runGameTestServer` exit 0 - literal
`All 193 required tests passed`. Not pushed.

## PHASE G RULING — animation tolerance / motion policy (2026-09-02)

OWNER RULING (verbatim): "Tolerance ruling: Slice 4 defaults to code-driven
motion (the G1-proven path). Keyframes are opt-in per species for deliberate
re-animation, accepted visually under Q1 - no numeric tolerance applies there.
Drop the 0.0021 controller from the gate; keep it only as a scaffold generator
for artist hand-off if useful. Standing rule: a test tolerance is a ruling -
never loosen one to pass a suite without flagging it. Before Slice 4: (1) list
which Tier-3 species will ever need bone positions server-side, since those
are code-driven regardless; (2) for the record, was the keyframe controller
driven by limbSwing/limbSwingAmount or by animation time? (3) is
Molang-expression output viable for exact motion-in-data, and are limb-swing
queries available to replaced entities? Continue with Slice 3 meanwhile."
The offered alternative (keyframes at 2e-3 rad for every species) was NOT
selected. STANDING LAW ADDED: a test tolerance is an owner ruling; loosening
one to pass a suite is presented as a proposed ruling, never adopted silently.

ANSWERS (from code, 2026-09-02):
(1) None of the 16 Tier-3 species needs a server-side bone position. Coin,
Elevator, Island/IslandToo, PurplePower, Robot1-5, RockBase, Rotator, Vortex
have no part/bone references; the three head sidecars (KingHead, GodzillaHead,
QueenHead) only mention `OreSpawnPartEntity` in deprecation javadoc - their
boxes are placed by the boss, not by a rig. Tier-1 bosses are code-driven
regardless (server pose evaluator, ruling Q3).
(2) The salvaged `PhaseLockedKeyframeController` was driven by ANIMATION AGE
(`(float) tickCount + partialTick`, times a per-clip angular frequency) with
gait amplitude multiplied by `limbSwingAmount`, never by `limbSwing`. That is
faithful to OreSpawn's own models, which are NOT vanilla-style: 40 models phase
their trig on `ageInTicks` and 106 scale by `limbSwingAmount`; only FOUR use the
walked distance `limbSwing` (CannonFodderModel, ModelIsland, ModelIslandToo,
ModelRobot1). Foot slide is original 1.7.10 behavior, and an artist's
time-based Blockbench preview matches the in-game phase model for all but
those four, which stay code-driven under any policy.
(3) Molang: viable in principle but not turnkey. GeckoLib 4.8.4's `math.cos`
evaluates through vanilla `Mth.cos` (the same 65536-entry table the classic
models use), so a cosine expression could reproduce classic motion to float
rounding. But the query set has NO `limb_swing`/`limb_swing_amount`; the
nearest is `query.ground_speed`, computed from `getDeltaMovement` (velocity,
not `walkAnimation.speed`), so amplitude parity would need OreSpawn to register
its own actor variable via `MolangQueries.setActorVariable`, and whether the
actor exposes the underlying entity for REPLACED animatables is unverified.
Per-frame cost (expression-tree evaluation per keyframe per bone) is unmeasured.
Not pursued under the code-driven default; recorded as the route if
motion-as-data ever becomes a product goal.

EFFECT: `PhaseLockedKeyframeController` and its 0.0021 rad gate stay in the
salvage lane (orespawn-7 @ 0d238ba); Slice 4 wires Tier-3 rigs through the
Slice 2 seam with code-driven poses; salvage inventory §5 updated.

## PHASE G SLICE 3 — asset audit check 8: GeckoLib rigs, clips, triggers, MHLib profiles (2026-09-02)

WHAT: `tools/asset_audit.py` check 8 (`check_geckolib`, ~110 lines) - the
rewrite-small of salvage lane orespawn-5 (whose +4,434-line version is not
adopted). Rules, each an ERROR: `GECKO_GEO_INVALID` (geo JSON unparsable, no
`minecraft:geometry`, no bones, duplicate bone names); `GECKO_ANIM_INVALID`
(animation JSON unparsable / no `animations` object); `GECKO_CLIP_MISSING`
(a `RawAnimation` clip literal in Java defined in no animation file);
`GECKO_TRIGGER_NEVER_FINISHES` (a `triggerableAnim` chain ending in
`thenLoop`/`thenPlayAndHold`, or in `thenPlay` of a clip whose JSON loop mode
is not `false` - the BUG-035 class, made a build failure); `PROFILE_MAIN_SIZE_MISMATCH`
(profile `main-hitbox.size` vs the registration's `.sized(w, h)` - the
profile main-size law); `PROFILE_BONE_MISSING` (`synched-bones` / `parts[].name`
absent from `geo/entity/<name>.geo.json`); `PROFILE_SYNC_WITHOUT_GEO`;
`PROFILE_VANILLA_NAMESPACE` (any profile under `data/minecraft/` - BUG-036);
`PROFILE_INVALID`; plus ADVISORY `PROFILE_ENTITY_UNKNOWN`. The Queen's
`death` clip is trigger-fired and holds its last frame on purpose, so
`("GECKO_TRIGGER_NEVER_FINISHES", "death")` joins ACKNOWLEDGED with its
justification; the standing audit pass form is therefore now
`0 error(s), 0 advisory(ies), 4 acknowledged` (was 3).

VERIFICATION: clean tree -> `RESULT: 0 error(s), 0 advisory(ies), 4
acknowledged -> exit 0`. Mutation tests, each restored from git afterwards:
Queen `bite` loop -> true => GECKO_TRIGGER_NEVER_FINISHES(bite), exit 1;
the_queen.json `Body1` -> `Body9` => PROFILE_BONE_MISSING, exit 1;
spider_robot.json main size 3.25 -> 3.5 => PROFILE_MAIN_SIZE_MISMATCH
("[3.5, 2.25] != [3.25, 2.25]"), exit 1; `data/minecraft/.../creeper.json`
added => PROFILE_VANILLA_NAMESPACE, exit 1; TheQueen.java `thenPlay("bite")`
-> `"bitee"` => GECKO_CLIP_MISSING, exit 1. Final clean run identical to the
first.

LIMITS: clip lookup is mod-wide (a clip name is checked against every
animation file, since the Java class holding `triggerableAnim` is not the one
naming the animation file); the trigger rule is conservative - any definition
of that clip name with a non-finishing loop fires it. Fine while clip names
stay unique per mod; revisit if two species share a clip name with different
loop modes.

GATE (on master, sequential): `build` exit 0 - audit literal `RESULT: 0 error(s), 0
advisory(ies), 4 acknowledged -> exit 0` (check 8 active inside `build`), `G1
PARITY PASS: 2 models`, benchmark evidence verified; `runGameTestServer` exit 0 -
literal `All 193 required tests passed`. Not pushed.

## PHASE G RULING — AMENDMENT 1 (2026-09-02) — supersedes "PHASE G RULING — animation tolerance / motion policy"

CONTEXT: the superseded ruling was written before the owner had the brief,
on the vanilla `limbSwing` premise that this log's answer (2) refuted
(OreSpawn's models are time-driven: `ageInTicks` phase, `limbSwingAmount`
amplitude; only four use walked distance). Per the owner, the commits carrying
the superseded ruling are NOT rewritten; this amendment supersedes it. The
standing tolerance rule ("a test tolerance is a ruling - never loosen one to
pass a suite without flagging it") is unchanged.

OWNER RULING (verbatim points):
1. Slice 4 stands: Tier-3 rigs code-driven. The four limbSwing-distance
   models (CannonFodder, Island, IslandToo, Robot1) are code-driven under any
   policy.
2. For every artist-facing tier, keyframes are the shipping path, per
   deliverables 3 and 4. Code-driven stays as the harness reference leg only.
3. Gait scaling: the mechanism the salvaged controller already uses - clip
   authored at full amplitude, controller scales the gait bones' animated
   delta by limbSwingAmount. It comes back into the gate for Tier-1/2 slices.
4. Animation-leg tolerance: 2.5e-3 rad ratified, stated alongside keyframe
   density and lerp mode. Try catmullrom (verify against 4.8.4 bytecode)
   before adding keyframes.
5. Per-part frequencies: one clip per frequency group on parallel
   controllers, each at its natural period; no single-period clips over
   multi-frequency rigs. Add a wrap sample (T-eps vs 0+eps) to the animation
   leg.
6. Tier 1: Q3 fixes where part positions come from, not what the client
   renders. In the Tier-1 hitbox design, state per boss which bones carry
   parts and how artist re-animation of those bones is handled - SPEC-locked
   bones or a server-side evaluator.
7. The standing tolerance rule is unchanged.

VERIFIED FOR THE RECORD (2026-09-02):
- (4) `catmullrom`: pinned GeckoLib 4.8.4 declares `EasingType.CATMULLROM`
  and `EasingType.catmullRom(double)` (javap of the pinned jar), so
  Catmull-Rom keyframe interpolation is available; the JSON `lerp_mode`
  spelling is checked at first use.
- (5) The salvaged controller ALREADY does per-frequency clips: Beaver
  registered three `PhaseLockedKeyframeController`s - Gait (3.7 rad/tick over
  rff/lrf/lff/rrf), Teeth (2.7), Tail (0.5) - each with its own clip
  (`gait`/`teeth`/`tail`), i.e. one clip per frequency group on parallel
  controllers. Each clip is authored as a NORMALIZED 1.0 s period
  (`NORMALIZED_CLIP_TICKS = 20`; 73 keys per bone at 1/72 s) and the
  controller time-warps it to the natural period (2*pi/f: 1.70, 2.33 and 12.57
  ticks for Beaver). The literal natural periods are too short to hand-edit,
  so the normalized-length convention is kept: the artist edits the SHAPE,
  the controller supplies the TEMPO, and each SPEC states the tempo. A
  Blockbench preview therefore shows the shape at a 1 s period, not the
  in-game rate.
- (5) Wrap sample: the salvaged gate asserted `loop_boundary_modulo` (poses at
  20n-0.001 / 20n / 20n+0.001 equal their in-period counterparts). The LANDED
  G1 animation leg samples fractions 0/.25/.5/.75/1.0 plus dense off-grid
  probes but has no explicit T-eps vs 0+eps pair; that pair is a binding
  requirement of the keyframe animation leg when it returns (Tier-1/2 slices).

EFFECT ON THE PLAN: Slice 4 (Tier-3, code-driven through the Slice 2 seam)
proceeds unchanged. The G3 lane's `PhaseLockedKeyframeController` is
re-scheduled: it returns with the first Tier-2 slice, reviewed and re-gated
at 2.5e-3 rad with density + lerp mode stated, per-frequency controllers,
limbSwingAmount delta scaling, and the wrap sample. The Tier-1 hitbox design
(inventory slice 5) must state per boss which bones carry parts and whether
those bones are SPEC-locked or served by the server-side evaluator.

GATE (docs-only, on master, sequential): `build` exit 0 - `RESULT: 0 error(s), 0
advisory(ies), 4 acknowledged -> exit 0`; `runGameTestServer` exit 0 - literal
`All 193 required tests passed`. Not pushed (owner holds push until this
amendment is committed; it now is).

## PHASE G RULING — AMENDMENT 1, ADDENDA + Q1 for Slice 4 (2026-09-02)

OWNER (verbatim): "1. The time-warp in PhaseLockedKeyframeController derives
its ratio from the clip's declared length, not a hardcoded 1.0 s, so artist
length edits don't change in-game tempo. SPEC still states the tempo. 2. When
the controller returns, target the fewest catmullrom keys that hold 2.5e-3
rad; 73 per bone is a conversion artifact, not a spec. Q1: Slice 4 species
land behind the same dev property as Beaver. I'll do the in-game look on
Beaver plus a sample of the 16 and rule on flipping them to default
afterward."

RECORDED: (1) binding on the controller's return - `NORMALIZED_CLIP_TICKS`
becomes the loaded clip's `animation_length` (in ticks), so the time-warp
ratio is period / declared length and an artist stretching a clip changes
nothing in-game; the SPEC states the tempo. (2) keyframe density is an
output of the harness, not an input: convert with catmullrom and the fewest
keys that hold 2.5e-3 rad, recorded alongside the tolerance. (Q1) every
Slice 4 species registers its GeckoLib candidate behind the Beaver dev
switch; classic stays the default until the owner's in-game look and a
per-species flip ruling. Master pushed to origin at bc6c735 before Slice 4
began.

GATE (docs-only, on master, sequential): `build` exit 0 - `RESULT: 0 error(s), 0
advisory(ies), 4 acknowledged -> exit 0`; `runGameTestServer` exit 0 - literal
`All 193 required tests passed`. Not pushed (push resumes on the owner's word).

## PHASE G SLICE 4a — Tier-3 through the seam: Elevator lands; two harness findings; s4 pipeline (2026-09-02)

SCOPE DECISIONS (from the five-reader Tier-3 survey, recorded here rather than
re-derived later):
- The three head sidecars (king_head, queen_head, godzilla_head) are EXCLUDED
  from Phase G conversion: their renderers return `shouldRender() == false`
  with empty `render`, their models emit zero vertices, and that is faithful
  to the 1.7.10 empty `Render*` stubs (BOSS-003/008/014). A GeckoLib renderer
  for an entity that never renders is dead code. Tier-3 count is 13.
- PurplePower and Rotator draw each part 6x / 8x under per-draw pose-stack
  transforms (18 and 24 draws); the landed converter emits one bone per part,
  so they need a render-instance expansion (the G2 lane's helper-group idea)
  plus a clone-aware geometry leg. Deferred to Slice 4c.
- The eight animated flat rigs (Coin, Island, IslandToo, Robot1-5) need
  code-driven poses on Y and Z axes, Robot4 needs per-frame POSITION writes
  (12 cannon parts, hand-rolled FK), Robot2/3 latch on per-entity RenderInfo
  and the entity RNG, Robot4 writes a synced flag from the render thread
  (ENT-K-070, kept bug-for-bug). Slice 4b, with a suite-visible animation leg
  on real entities (gametest: classic setupAnim vs production
  applyCustomAnimations on the baked geo) because the headless probe passes a
  null entity and these models dereference it.
- Vanilla-style `limbSwing` (distance) drives five models, not four: Robot5's
  wheels (`limbSwing * 0.15 % 2pi`) were missed by the cos()-only grep that
  produced the earlier count. All five stay code-driven under Amendment 1.

HARNESS FINDINGS (harness-first law: stopped, not tuned; rulings proposed):
- VORTEX (zero-thickness 128x64x0 billboard): the surface-mapping leg fails at
  bind - "no GeoRenderer vertex matches position (-4.0, -2.625, 0.0) and normal
  (0.0, -1.0, 0.0)". Vanilla emits all six faces (24 vertices, six distinct
  normals); GeckoLib 4.8.4's baker emits 24 vertices with normals {+X x8, -Z x4,
  +Z x4, +Y x8} - it collapses the flat cube's degenerate faces. Those faces have
  zero area and draw nothing, so the mismatch is on invisible geometry; the
  visible +-Z quad matches. PROPOSED RULING: the surface-mapping leg ignores
  zero-area faces (a harness rule change - owner's call, per the standing rule).
  Vortex stays classic until ruled.
- ROCK_BASE (22 flat parts, 10 with bind rotations on X, Y and Z): the geometry
  and surface legs PASS for all 22 parts - this is the first mechanical proof of
  the converter's bind-rotation signs on Y and Z (max corner delta within
  1e-5 blocks, UV/normal delta 0). The visual leg fails at bind: changed
  fraction 0.0320 > 0.001. At bind every part is visible, so all twelve rock
  types are drawn superimposed and coincident faces z-fight; vanilla resolves
  them in field-declaration order, GeckoLib in its own root order (the G2
  finding: GeometryTree keeps roots in a Map). In-game only one type's 2-10
  parts are visible, but the crystal groups (3a-d, 4a-d) are rotated copies
  about one origin and overlap too. PROPOSED RULING: either (a) extend the
  probe to capture per-rock-type visibility so the visual leg compares what a
  player sees, or (b) treat root-order-sensitive rigs as needing the G2
  root/face-order contract before conversion. RockBase stays classic until
  ruled; its unproven geo is not shipped.

LANDED: Elevator (hoverboard) through the Slice 2 seam. `ElevatorGeoReplacement`
- static rig (the G1 Tier-3 proof, geo byte-identical to
`g1_proof/generated/model_elevator.geo.json`), paint-colour texture 1..10 (out
of range -> 1, exactly the classic mapping), and the boat-style hit wobble
from `ElevatorRenderer.setupRotations` ported through a new descriptor hook
`applyRotations`, applied after GeckoLib's own rotations (the 6-arg
`GeoReplacedEntityRenderer.applyRotations` that `actuallyRender` calls; the
5-arg form delegates to it). Registered behind the dev switch; classic
`ElevatorRenderer` and its layer definition remain.

DEV SWITCH (Q1): one property for every candidate,
`-Dorespawn.dev.geckolibRenderers=candidate`; the original
`-Dorespawn.dev.beaverRenderer=candidate` is honored as an alias, so the
owner's existing Prism JVM argument now enables Beaver AND Elevator. Gametest
`phaseg001` pins the two-property contract.

PIPELINE: build.gradle gains the `s4*` task block - the same probe, converter
and parity tools over `tools/s4_model_proofs.json` and
`phase_g_reports/s4_proof` (separate `build/s4`; `check` depends on
`s4Parity`). Because the G1 benchmark report pins build.gradle's hash, the
SMOKE_ONLY benchmark was regenerated with `gradlew g1Benchmark` and the gate's
`--write-proof` (labels unchanged, no Q6 claim; timing numbers moved within
noise). s4 proof: Elevator - surface 720 vertex-samples, UV 0, normal 0;
animation 0 rad; visual 0 changed pixels, MAE 0.

INCIDENTAL: `tools/g1_model_proofs.json` still names the Elevator texture as
`Elevator1.png`; since BUG-038 the tracked file is `elevator1.png`. It resolves
on this case-insensitive checkout only. Left untouched here because that
manifest's hash is pinned by the G1 benchmark proof; the s4 manifest uses the
lowercase name. Follow-up: fix and regenerate the G1 proof in one commit.

REVIEW (two read-only reviewers; parity PASS, regression FAIL(1) -> fixed):
- BLOCKER fixed: `.gitattributes` pinned LF only for the G1 proof tree, so
  a fresh checkout under `core.autocrlf=true` would have materialized the new
  `s4_proof` JSON/MD as CRLF and `s4Parity`'s byte-compare would go red - the
  landing-day false red all over again. `tools/s4_model_proofs.json` and
  `phase_g_reports/s4_proof/**` (json/md `eol=lf`, png `binary`) are now
  pinned; `git ls-files --eol` shows `i/lf w/lf attr/text eol=lf` for them.
- MINOR fixed: `getShadowRadius` now multiplies by `LivingEntity.getScale()`
  as well as the age scale, matching `MobRenderer` (a `generic.scale`
  modifier would otherwise leave the candidate's shadow unscaled).
- MINOR fixed: the switch warning names the property that actually selected
  the candidate (`DevRendererSwitch.candidateSource()`), so a launch config
  carrying only the old Beaver property is told the truth.
- NEW GUARD from a reviewer's gap: audit check 8 `GECKO_GEO_PROOF_DRIFT` -
  a shipped `geo/entity/<name>.geo.json` must be byte-identical to every
  `phase_g_reports/*_proof/generated/model_<name>.geo.json`; until now nothing
  tied the asset GeckoLib loads to the parity evidence. Mutation-tested (one
  appended byte -> ERROR, exit 1; restored -> clean).
- Verified from bytecode: `actuallyRender` invokes the 6-arg
  `applyRotations`, and the hook lands in the same frame as
  `ElevatorRenderer.setupRotations`'s trailing `mulPose` - after the entity
  scale, before the model basis change; `ageInTicks` and `partialTick` are the
  same quantities as vanilla's. `GeoRenderer.reRender` DOES call
  `actuallyRender(isReRender=true)`, so on a re-render pass GeckoLib re-applies
  its own yaw/death rotations and, with them, this hook - a library trait
  inherited, not introduced; no layer triggers re-render on these renderers.
- Noted, not changed: the s4 manifest re-proves Elevator (already in the G1
  proof) - kept as the s4 pipeline's live smoke and the home for RockBase /
  Vortex once ruled; the +0.009-block vertical offset of every GeckoLib
  candidate (GeckoLib's `translate(0, 0.01, 0)` vs vanilla's -1.501 datum)
  remains the recorded Slice 2 open item; proof PNG bytes depend on the
  installed Pillow version (same exposure as G1).

GATE (on master, sequential, final tree): compile clean; `build` exit 0 - audit
literal `RESULT: 0 error(s), 0 advisory(ies), 4 acknowledged -> exit 0` (check 8
incl. GECKO_GEO_PROOF_DRIFT), `s4Parity` `G1 PARITY PASS: 1 models; checked-in
proof verified`, `g1Parity` 2 models, regenerated benchmark evidence verified;
built jar carries `geo/entity/elevator.geo.json` and the Elevator candidate
classes; `runGameTestServer` exit 0 - literal `All 193 required tests passed`.
Not pushed.

## PHASE G RULINGS on the Slice 4a harness findings + Q1 acceptances (2026-09-02)

OWNER (verbatim): "1. Vortex: accepted. The surface leg ignores faces of
exactly zero area (not 'small') and reports the ignored count per rig in its
output. The visual leg remains the backstop and must pass for Vortex from
front and back. 2. RockBase: option (a). The visual leg compares per-state
visibility for all twelve rock types; superimposed-at-bind is not a
player-visible state and z-fight order is not a parity target. Move RockBase
to 4b with the real-entity leg. Any in-game state that draws overlapping parts
waits for the G2 root-order contract." Also: "Extend the switch so the
property also accepts a species list (e.g. `=beaver,elevator`) for bisecting
during in-game looks." "Q1 acceptances: Elevator and Vortex candidate
renderers accepted - both match classic in-game. Beaver look still pending
from me; 4b can continue behind the switch meanwhile." Two findings from the
in-game look, outside Phase G, report before fixing, separate commits: (3)
hoverboard rider sits, owner wants standing - check the 1.7.10 reference and
classify parity bug vs 2.0 improvement; (4) Vortex "flies around doing
nothing" - read-only behavior report, original vs port, faithful idle / unmet
trigger / port bug. Then: push, then 4b.

RECORDED: (1) harness rule change - `g1_render_parity.py` surface-mapping leg
skips faces whose area is exactly 0 and reports `ignored_zero_area_faces` per
rig; Vortex is proven with TWO visual cameras (front and back). (2) RockBase
leaves the Python bind-pose visual leg; its parity is per rock type via the
Slice 4b real-entity leg; states that draw overlapping parts are gated on the
G2 root/face-order contract. Switch: species list accepted.

CORRECTION to the Q1 record: Vortex had NO candidate renderer in the build the
owner ran (it was withdrawn from 4a when its harness leg went red), so the
"Vortex candidate accepted" look was of the classic renderer. Elevator's
acceptance stands. Vortex's acceptance is re-requested once its candidate
lands under ruling (1). Master pushed to origin before 4b.

## PHASE G SLICE 4a-2 — rulings applied: Vortex lands; species-list switch (2026-09-02)

HARNESS (ruling 1): `tools/g1_render_parity.py` surface-mapping leg now drops
quads whose area is EXACTLY 0.0 (sum of the two triangle cross-product norms,
compared to 0.0, never a tolerance) from both the vanilla and the GeckoLib
vertex lists before pairing, and reports `ignored_zero_area_faces` per rig in
the evidence report and the pass line. The visual leg accepts a `cameras`
list (name/yaw/pitch) and every camera must pass; captures are named
`<sample>.<camera>.*.png`. Vortex is proven with `front` (yaw 0) and `back`
(yaw 180).

VORTEX (proof, `phase_g_reports/s4_proof`): geometry 6 cube-samples, max
delta 0; surface 48 vertex-samples, 24 zero-area faces ignored, UV 0, normal
0; animation 0 rad; visual max changed 0.000137, max MAE 0.0042, both
cameras. Elevator unchanged (0/0/0). Shipped `geo/entity/vortex.geo.json` is
the proof copy (check 8 GECKO_GEO_PROOF_DRIFT pins it). `VortexGeoReplacement`
- static billboard, fixed texture, shadow 1.5 - registered behind the switch.
Owner's Vortex acceptance is RE-REQUESTED: the previous look was the classic
renderer (see the correction above).

SWITCH: property grammar is now `candidate` (all species) or a
comma-separated species list of registry names (`beaver,elevator`), trimmed
and case-insensitive; both `orespawn.dev.geckolibRenderers` and the alias
`orespawn.dev.beaverRenderer` accept it; the warn line names the property
that selected the species. `phaseg001` pins twelve cases.

ROCK_BASE (ruling 2): moved to Slice 4b; its parity is per rock type via the
real-entity leg; states that draw overlapping parts wait for the G2
root/face-order contract.

G1 PROOF: the parity tool's report gained `ignored_zero_area_faces` and a per-row
`camera` field, so the immutable G1 evidence no longer byte-matched its
regeneration (`g1Parity` red: `checked-in G1 proof drift: evidence/report.json`).
Regenerated with `g1_render_parity.py --write-proof`: only
`g1_proof/evidence/report.json` changed (16+/3-: the new fields at 0 / null and
the surface leg's evidence text); geometry, PNGs and README byte-identical;
verify mode green. The benchmark proof is unaffected (the parity tool is not in
its pinned set).

GATE (on master, sequential): compile clean; `build` exit 0 - audit `RESULT: 0
error(s), 0 advisory(ies), 4 acknowledged -> exit 0`, `s4Parity` 2 models
(Vortex: 24 zero-area faces ignored, front+back visual), `g1Parity` 2 models,
benchmark evidence verified; jar carries `geo/entity/vortex.geo.json` +
`VortexGeoReplacement`; `runGameTestServer` exit 0 - literal `All 193 required
tests passed`. Not pushed.

## OWNER FINDINGS 3 + 4 — reports, no fixes (2026-09-02)

(3) HOVERBOARD RIDER POSTURE -> AUDIT BUG-039. The 1.7.10 original STOOD
(hand-written `shouldRiderSit() { return false; }`, orig Elevator.java:121-123)
and the port already returns false (Elevator.java:154-158) with the standing
seat math (TF-029); the NeoForge hook is live. The seated rider the owner saw
is most plausibly one of the pack's player-animation mods (NotEnoughAnimations,
Player Animator, Serious Player Animations, SittingPlus) re-posing the rider.
Classification by the owner's rubric: parity-bug class, but the port code is
already faithful - no change until reproduced in the DH & Iris instance
(29 mods, none of those). No MOD entry proposed.

(4) VORTEX "FLIES AROUND DOING NOTHING" -> AUDIT ENT-S-089. Faithful idle +
unmet trigger: the target predicate rejects creative players and needs a
living, line-of-sight target inside 16/10/16 - identical to 1.7.10. Eight
real divergences were found underneath (hitbox 2x4 -> 1x1.5 lowering the LoS
eye; missing empty doPush so the vortex shoves victims away; wander picks
without the canSeeTarget probe; missing persistence gate on the dawn discard;
pressure plate; voice pitch; particle drift sign; wander threshold/quirk).
Proposed as one audit-fix slice in its own commit after the owner's word;
a survival-vs-creative target-acquisition gametest goes with it.

## PHASE G SLICE 4b — eight Tier-3 code-driven rigs behind the switch; production-hook harness; Coin deferred (2026-09-02)

SCOPE. Island, IslandToo, Robot1, Robot2, Robot3, Robot4, Robot5 and RockBase
land as GeckoLib candidates behind `-Dorespawn.dev.geckolibRenderers` (species
ids island, island_too, robot_1..robot_5, rock_base). Their poses are the
classic `setupAnim` formulas verbatim on the converted rigs (Amendment 1,
Tier 3: code-driven; no clip emitted or accepted). Coin is DEFERRED under
BUG-040 (its classic port model is not the original and renders nothing).
The suite count is unchanged (193): see "NO GAMETEST LEG" below.

PRODUCTION SEAM. `PoseInputs` (subject, ageInTicks, limbSwing,
limbSwingAmount, netHeadYaw, headPitch) is the hook's input; the shared model
adapts GeckoLib's state into it (`PoseInputs.fromState`: TICK = tickCount,
head angles un-negated - the replaced renderer negates both into
EntityModelData, bytecode-read). Reason: GeckoLib's `DataTickets` registers a
data component in its initialiser and cannot load in the un-bootstrapped
probe JVM, so a state-based hook could never be harness-driven. Base-class
helpers write bones in ModelPart vocabulary and hold the basis in one place:
`rotateX/Y/Z`, `moveTo`, `classicPosition`, `setVisible`, `pose()` (harness
entry). Landed species are untouched in behaviour: Beaver's private helper
was renamed `setInternalRotX` (the new protected `rotateX` collided; same
body). Descriptor entity-type suppliers are lambdas (a bound method reference
evaluated `ModEntities` eagerly and tripped the bootstrap check in the probe).
`RockBaseRenderer.textureFor(int)` is the one per-type texture table; the
candidate reads it through the descriptor's `texture(E)` hook.

POSE INTERFACES (the four classic models that read their entity). New
`danger.orespawn.entity.pose.{Robot2Pose, Robot3Pose, Robot4Pose,
RockBasePose}` declare exactly the accessors those `setupAnim` bodies call
(getRenderInfo/getAttacking/getRandom, getAttacking/setShielding,
getRockType); the entities implement them with their existing methods (one
`implements` clause each, no body changes). Each classic model's `setupAnim`
now delegates to `poseFrom(<interface>, six floats)` whose body is the
former `setupAnim` body character for character; the GeckoLib hooks read the
same interface through `PoseInputs.subject(Class)`. This is what lets the
harness pose BOTH sides from a declared state instead of a live entity.

NO GAMETEST LEG - DISCLOSURE. The plan (and the scoping report) said the
entity-bound species would get a suite-visible real-entity gametest. It
cannot exist: the dedicated gametest server's `RuntimeDistCleaner` refuses
every client class (`ModelPart`, `EntityModel`, the classic models
themselves), so a test class that references them stops the server before
any test runs ("Failed to start the minecraft server") - and Gradle still
exits 0, which is exactly why the standing gate demands the literal `All N
required tests passed`; the first 4b gate run tripped that check. The
gametest was removed; the entity-bound proof moved into the headless probe
via the pose interfaces above, which is stronger (deterministic, no live
entity, seeded RNG) and suite-independent. Scoping claim retracted.

BASIS (derived from GeckoLib 4.8.4 bytecode, then PROVEN by
`fixture_runtime_basis_yz`): the converter writes pivot (-x, 24-y, z) and
`BakedModelFactory` negates JSON pivot X and rotation X/Y, so the internal
pivot is (x, 24-y, z) - classic space reflected in Y. Conjugating through
that reflection: internal rotation = (-xRot, yRot, -zRot). `RenderUtil.
translateMatrixToBone` translates (-posX, posY, posZ)/16, so a classic pivot
move (dx, dy, dz) is posX=-dx, posY=-dy, posZ=dz, with a nested child's
ModelPart x/y/z local to the parent's pivot. The fixture writes all six
channels (X/Y/Z rotation, X/Y/Z position) on a rotated, inflated parent and
a nested child from all five inputs; its surface-mapping leg (posed geometry
through GeckoLib's real renderer) is exact and its animation leg is 0 rad /
0 units. Y rotation is also proven on Island's head, Z on Robot1's keys.

HARNESS UPGRADES (tools/, src/g1tool; G1AnimationRuntime and build.gradle
untouched; the benchmark proof was rewritten because it pins the g1tool
class directory hash - smoke run, unchanged semantics):
- kind `code_driven`: the probe instantiates the manifest's `candidate_class`
  (the SHIPPED replacement), binds the production
  `OreSpawnGeoReplacementModel`'s processor to a fresh bake and calls
  `pose(PoseInputs)`; manifest inputs `net_head_yaw`/`head_pitch` join
  `limb_swing`/amplitude samples; the geo probe emits `java_positions` and
  `hidden_bones`; the animation leg compares positions
  (`position_epsilon_model_units` 1e-4) and hidden-bone sets as well as
  rotations.
- kind `entity_state`: manifest `entity_states` (attacking, ri1, seed,
  rock_type) x the sample grid; each side gets a FRESH `ProbeSubject` built
  from the same state (RenderInfo preset, `RandomSource.create(seed)`), the
  compiled side is posed through `poseFrom`, the candidate through the hook;
  besides bones, `subject_after` (ri1 latch result, Robot4 shielding write)
  must match. Robot2: four ri1 presets at age 10 (latch closed), the idle
  crossing at 17.96 (sin(t*20 deg) 359.2->360.7: ri1 reset), and the
  attacking crossing with seed 12345 (identical re-roll). Robot3: presets,
  then the cosine crossing at 4.6 for both attack states. Robot4: idle vs
  attacking x amplitudes x ages 0/7.5/20, shielding asserted. RockBase: rock
  types 0..13 (types 0/13 draw nothing: proven by the hidden-bone check).
  Compiled-side cube capture filters hidden parts by path because
  `ModelPart.visit` ignores `visible` while `ModelPart.render` honours it.
- HARNESS FINDING, visual leg: the rasteriser alpha-BLENDED texels and wrote
  depth for fully transparent ones, so results depended on draw order (Island
  bind: 12.8% changed, every changed pixel at identical winning depth). Both
  renderers draw entity models with `RenderType.entityCutoutNoCull`
  (fragment shader discards alpha < 0.1, no depth write, no blending); the
  leg now does the same (`CUTOUT_ALPHA_THRESHOLD`). Island then matched
  exactly. Elevator/Vortex/Beaver had opaque UV windows, so G1/4a were not
  affected.
- RULING 2 APPLIED MECHANICALLY: pixels where two different quads reach the
  front within 1e-6 depth with different texels are draw-order z-fights in
  BOTH renderers (Robot5: all 368 changed pixels were coplanar contests,
  winning depths equal to 3e-16). They are excluded from the changed/MAE
  comparison, painted blue in the diff PNG, and reported as
  `contested_fraction` per capture and `max_contested_fraction` per model;
  no threshold changed. Bind captures of Robot2/3/4/5 and RockBase are not
  visual samples (parts sit superimposed until posed; superimposed-at-bind is
  not a player-visible state); the posed/state samples are. Overlapping
  states remain the G2 root-order contract's business; the contested
  fractions below measure what that contract owes.
- Report writer covers the new kinds; `assert_same_cube_set` guards both
  geometry legs.

EVIDENCE (phase_g_reports/s4_proof, written by the green run):
  model_elevator   static        geometry 0 blocks; surface 720 vertex-samples, 0 zero-area ignored; animation 0 rad; visual changed 0, MAE 0, contested 0
  model_vortex     static        geometry 0 blocks; surface 48 vertex-samples, 24 zero-area ignored; animation 0 rad; visual changed 0.000137, MAE 0.00423, contested 0
  model_island     code_driven   geometry 2.01e-07 blocks; surface 1008 vertex-samples, 0 zero-area ignored; animation 0 rad; pos max 0 units; hidden checks 6; visual changed 0, MAE 0, contested 0
  model_islandtoo  code_driven   geometry 2.01e-07 blocks; surface 1008 vertex-samples, 0 zero-area ignored; animation 0 rad; pos max 0 units; hidden checks 6; visual changed 0, MAE 0, contested 0
  model_robot1     code_driven   geometry 3e-07 blocks; surface 7128 vertex-samples, 0 zero-area ignored; animation 0 rad; pos max 0 units; hidden checks 10; visual changed 0, MAE 0, contested 0.000549
  model_robot5     code_driven   geometry 2e-07 blocks; surface 1320 vertex-samples, 0 zero-area ignored; animation 0 rad; pos max 0 units; hidden checks 4; visual changed 0, MAE 0, contested 0.0116
  model_robot2     entity_state  geometry 1e-06 blocks; surface 7560 vertex-samples, 0 zero-area ignored; animation 0 rad; pos max 0 units; states 5; hidden checks 20; visual changed 0, MAE 0, contested 0
  model_robot3     entity_state  geometry 1e-06 blocks; surface 5928 vertex-samples, 0 zero-area ignored; animation 0 rad; pos max 0 units; states 3; hidden checks 12; visual changed 0, MAE 0, contested 0.0025
  model_robot4     entity_state  geometry 3.7e-07 blocks; surface 17472 vertex-samples, 0 zero-area ignored; animation 0 rad; pos max 0 units; states 2; hidden checks 12; visual changed 0, MAE 0, contested 0.000412
  model_rockbase   entity_state  geometry 2e-07 blocks; surface 2064 vertex-samples, 0 zero-area ignored; animation 0 rad; pos max 0 units; states 14; hidden checks 14; visual changed 0, MAE 0, contested 0
  fixture_runtime_basis_yz fixture       geometry 2.77e-07 blocks; surface 336 posed vertex-samples exact; animation 0 rad; pos max 0 units over 42 channels

DEFERRED / FINDINGS: Coin -> AUDIT BUG-040 (report only, owner's call): the
port's `ModelCoin` is a 16x16x4 box at UV (0,0) on the 512x512 sheet whose
window is fully transparent, spinning `age*0.1`; the 1.7.10 original is a
256x256x1 mirrored quad at UV (0,0), pivot (0,-109,0), drawn at 0.125 scale
(`RenderCoin(new ModelCoin(0.22f), 0.75f, 0.125f)`), yaw
`cos(age*0.05*0.22)*PI`. The classic coin is invisible in the port; the
candidate (parity-proven on geometry, surface and animation legs) was pulled
rather than ship a faithful conversion of a wrong model.

G1 PROOF regenerated with `--write-proof` because the shared tool's visual
report gained fields: `evidence/report.json`, `evidence/README.md`, and ONE
PNG - `visual/model_beaver/a1_t0.diff.png`, where a single contested pixel
(1.5e-5 of the image) is now painted; vanilla/geo captures are byte-identical.
`benchmark/report.json` + `README.md` rewritten (g1tool directory hash pin).

OWNER LOOK: `-Dorespawn.dev.geckolibRenderers=island,island_too,robot_1,robot_2,robot_3,robot_4,robot_5,rock_base`
(or `candidate` for all landed species). Not pushed.

GATE (on master, sequential): compile clean (main, g1tool, gametest); `build`
exit 0 - audit `RESULT: 0 error(s), 0 advisory(ies), 4 acknowledged -> exit 0`,
`s4Parity` 10 models + basis fixture (checked-in s4 proof verified), `g1Parity`
2 models (checked-in proof verified), benchmark evidence verified; jar carries the
eight geo files, eight empty clips, eight replacement classes and PoseInputs, no
tooling classes; `runGameTestServer` exit 0 - literal `All 193 required tests
passed`. A first gate run of this slice had the (since removed) gametest class
stop the server with Gradle still exiting 0; the literal check caught it.

## PHASE G RULING — VISUAL LEG RATIFIED WITH CONDITIONS (2026-09-02)

The owner ratified the two Slice 4b harness-semantics changes (cutout
rasterisation; z-fight exclusion under ruling 2) and set conditions that are
now standing rules (recorded in the saved memory as well):

1. A harness-semantics change that flips a result is presented with
   before/after numbers and a justification BEFORE the gate that depends on it,
   exactly like a tolerance. (Slice 4b reported after its gate; that was the
   breach this rule closes.)
2. The semantics are verified against the actual runtime (law 11), not memory.
3. Before/after is reported for every species, not only the motivating one.
4. Each species' excluded-pixel fraction is pinned in its manifest so it
   cannot grow silently; raising a pin is an owner ruling.
5. Any species above 0.5% excluded needs a specific in-game acceptance from
   the owner. Robot5 (1.16%) is the first.

LAW 11 VERIFICATION of the cutout semantics (all from the pinned jars, not
memory):
- Vanilla path: `LivingEntityRenderer.getRenderType` (bytecode): translucent
  -> `RenderType.itemEntityTranslucentCull`; visible -> `EntityModel.renderType(texture)`;
  glowing -> outline. `Model.renderType` applies the function given to the
  constructor; `EntityModel()`'s no-arg constructor binds
  `RenderType::entityCutoutNoCull` (BootstrapMethods #65, REF_invokeStatic).
  Every classic OreSpawn model in the manifests uses that implicit
  constructor (no `super(RenderType...)` call), and none of the nine
  renderers overrides `getRenderType` (only Fairy/Ghost/GhostSkelly do).
- GeckoLib path: `GeoReplacedEntityRenderer.getRenderType` (bytecode):
  translucent -> `itemEntityTranslucentCull`; else `GeoRenderer.getRenderType`
  -> `GeoModel.getRenderType` -> `RenderType.entityCutoutNoCull`; glowing ->
  outline. Same branch structure; the visual leg's case is the visible,
  non-translucent, non-glowing one on both sides.
- `RenderType.ENTITY_CUTOUT_NO_CULL` (lambda$static$3, bytecode): shader
  `RENDERTYPE_ENTITY_CUTOUT_NO_CULL_SHADER`, `NO_TRANSPARENCY`, `NO_CULL`,
  `LIGHTMAP`, `OVERLAY`; builder defaults (`CompositeStateBuilder.<init>`):
  `LEQUAL_DEPTH_TEST`, `COLOR_DEPTH_WRITE`. So: no blending, depth written for
  every fragment the shader keeps, both faces drawn.
- Fragment shader `assets/minecraft/shaders/core/rendertype_entity_cutout_no_cull.fsh`
  (client-extra jar): `if (color.a < 0.1) discard;` before any colour math.
  The harness discards 8-bit alpha < 25.5 (= 0.1 * 255, exact, `<`), writes
  colour and depth for kept fragments, never blends. That is the render type.

BEFORE / AFTER, every species (the pre-4b rasteriser at 8f74d0e run on the
CURRENT captures and manifests, versus the ratified leg; "excluded" is the
z-fight fraction now pinned):
  proof model              BEFORE changed     MAE  pass   AFTER changed     MAE  excluded(pinned)  worst-before
  s4    model_elevator          0.000000  0.0000  True        0.000000  0.0000          0.000000  -
  s4    model_vortex            0.000137  0.0042  True        0.000137  0.0042          0.000000  bind.front
  s4    model_island            0.132339 17.1546 False        0.000000  0.0000          0.000000  a0_5_t_half
  s4    model_islandtoo         0.132339 10.7301 False        0.000000  0.0000          0.000000  a0_5_t_half
  s4    model_robot1            0.000549  0.0822  True        0.000000  0.0000          0.000549  a0_5_t0
  s4    model_robot5            0.005615  0.5896 False        0.000000  0.0000          0.011612  a0_05000000074505806_t0
  s4    model_robot2            0.000000  0.0000  True        0.000000  0.0000          0.000000  -
  s4    model_robot3            0.000000  0.0000  True        0.000000  0.0000          0.002502  -
  s4    model_robot4            0.000000  0.0000  True        0.000000  0.0000          0.000412  -
  s4    model_rockbase          0.038315  1.0096 False        0.000000  0.0000          0.000000  s_type_9_t0
  g1    model_elevator          0.000000  0.0000  True        0.000000  0.0000          0.000000  -
  g1    model_beaver            0.000015  0.0034  True        0.000000  0.0000          0.000015  a1_t0
Flipped by the change: model_island, model_islandtoo, model_rockbase and
model_robot5 (all red under the blending rasteriser, all exact now).
Unchanged verdicts with excluded pixels only: robot1, robot3, robot4, beaver.
Vortex's 0.000137 is its accepted boundary residual on both legs.

PINS: `max_contested_fraction_pin` per model in both manifests at the observed
values (fractions of 65536 pixels): elevator 0, vortex 0, island 0,
islandtoo 0, robot1 36/65536, robot5 761/65536, robot2 0, robot3 164/65536,
robot4 27/65536, rockbase 0, beaver 1/65536. The parity tool fails on growth
(`CONTESTED PIN EXCEEDED`), refuses a model without a pin, reports the pin
and `requires_in_game_acceptance` (> 0.5%), and prints both on its pass line.
Robot5 is marked `in_game_acceptance: PENDING_OWNER` in the manifest.

G1: the manifest gained pins (elevator 0, beaver 1/65536) and the report
gained two fields per model; the G1 proof and the benchmark proof (manifest
hash pin) were rewritten; captures are byte-identical.

GATE (on master, sequential): `build` exit 0 - audit 0/0/4, `s4Parity` 10 models
(pins printed on every visual pass line; Robot5 flagged IN-GAME ACCEPTANCE REQUIRED),
`g1Parity` 2 models, benchmark evidence verified (proof rewritten for the manifest
pin fields); `runGameTestServer` exit 0 - literal `All 193 required tests passed`.

## BUG-040 FIX — classic Coin restored to the 1.7.10 model; reference-geometry leg (2026-09-02)

WHAT CHANGED (own commit, owner's go): `ModelCoin` and `CoinRenderer` are the
1.7.10 originals (see AUDIT BUG-040 resolution for the exact geometry, the
mirror-ordering correction, the 0.125 scale and 0.09375 shadow, and the
cosine yaw). `CoinGeoReplacement` returns behind the dev switch (species id
`coin`) with the descriptor's `applyScale` carrying the 0.125.

THE INDEPENDENT LEG. `tools/reference_geometry_leg.py` parses a decompiled
1.7.10 `ModelBase` constructor (textureWidth/Height, `new ModelRenderer(this,
u, v)`, addBox with/without inflate, setRotationPoint and `+=` adjustments,
setTextureSize, mirror with its construction-time semantics, showModel,
rotateAngle assignments and the `setRotation` helper, addChild) into a
geometry list and compares it with the compiled port dump the probe already
writes (parts matched by exact box signature, then pivots, nesting and
initial rotations checked; per-part texture size checked against the sheet).
Anything outside that idiom is reported UNPARSEABLE rather than guessed. It
runs as `s4ReferenceGeometry` for every manifest model declaring
`reference_source`, and the parity tool refuses to pass such a model
without a PASS from it (`--reference-dir`); the leg's JSON is part of the
checked-in proof. Coin: PASS, 1 part. Parser coverage: all 109 reference
`Model*.java` files parse (the four holdouts needed `+=` rotation points and
a scalar read of a part field).

DISCLOSURE: `tools/reference_geometry_leg.py` entered the tree in the
previous commit (412e2d0, the pins commit) unwired and unexercised, because
`git add -A` swept it in; it is exercised by this commit's gate.

EVIDENCE: model_coin geometry 0 blocks;
surface 168 vertex-samples exact; animation
0 rad (7 samples across the
571.2-tick period); reference leg PASS; visual changed
0, MAE 0, excluded 0 (pin 0).
Benchmark proof rewritten (build.gradle hash pin; smoke numbers only).

GATE (on master, sequential): compile clean; `build` exit 0 - audit 0/0/4,
`s4ReferenceGeometry` PASS (model_coin), `s4Parity` 11 models + fixture (checked-in
proof verified, reference leg folded in), `g1Parity` 2 models, benchmark evidence
verified; `runGameTestServer` exit 0 - literal `All 193 required tests passed`.

## REFERENCE-GEOMETRY SURVEY — the owner's population question (2026-09-02)

"How many other models could have port geometry or motion diverging from
1.7.10, and is a reference-geometry leg feasible with the converter?"
Answered mechanically, not by extrapolation: the leg ran over all 87 port
models that pair with a 1.7.10 model (generated survey manifest; one probe
JVM per model; parser covers all 109 reference files). GEOMETRY: exact 2
(Coin, Kyuubi); mirror-flag divergence 82 (78 with nothing else) -> AUDIT
BUG-041, a port-wide texture-flip the owner must rule on; geometry-moving 7
(CaterKiller, Elevator, Island, IslandToo, SeaViper, Skate, StinkBug) ->
AUDIT ENT-S-091. MOTION: not mechanically comparable (1.7.10 classes cannot
run here); sampled reads only. FEASIBILITY: the leg is wired
(`s4ReferenceGeometry` + `--reference-dir`); declaring `reference_source`
pins any model. Report: `phase_g_reports/reference_geometry_survey.md`
(+ `.json`). Tool changes this commit: categorised comparison, construction-
time flag semantics, twin-aware matching, `--repository-root`, survey mode.

Housekeeping: `tools/__survey_manifest.json`, a temporary file the survey
wrote into tools/ while the Coin commit's `git add -A` ran, was committed
in e06414a by mistake and is removed here.

## ENT-S-089 SPLIT PRESENTED — eight Vortex divergences, all parity bugs (2026-09-02)

Per the owner's rule, each of the eight was classified (parity bug without a
record / intentional with a MOD record / intentional without one yet) and
adversarially refuted twice. All eight: PARITY BUG, NO RECORD; no refutation
survived on the label. MOD records are in MODERNIZATION_NOTES.md (MOD-001..
028) and none touches these sites; the port file annotates every deliberate
choice and annotates none of these. Full evidence and the proposed fix +
tests are in AUDIT ENT-S-089. New: ENT-S-090, the pressure-plate override
dropped across ~35 entities (systemic sweep proposed). No fix applied:
awaiting the owner's go.

GATE (on master, sequential): `build` exit 0 - audit 0/0/4, `s4ReferenceGeometry` PASS
(model_coin, categorised leg; s4 proof rewritten for the new fields), `s4Parity` 11
models, `g1Parity` 2 models, benchmark verified; `runGameTestServer` exit 0 - literal
`All 193 required tests passed`. Not pushed.

## ENT-S-089 FIX — Vortex restored to orig Vortex.java on all eight divergences (2026-09-02)

Owner's go on the presented split (all eight parity bugs, no MOD record).
Changes: ModEntities `.sized(2.0f, 4.0f)` (orig :50); EntityVortex: empty
`doPush` (orig :98-99), `isIgnoringBlockTriggers` (orig :221-223),
`getVoicePitch` 1.0 (orig :78-80), particle tangent `dir - PI/2` (orig
:122-124), `!isPersistenceRequired()` ahead of the daytime discard (orig
:131-133), wander loop restored (orig :165-182: 1-in-300 or `< 2.1`;
candidate written before validation; air AND eye-line `canSeeTarget`, orig
:146-148) with the port's invented stuck counter removed. Five gametests
pin what the server can observe (see AUDIT ENT-S-089). Suite grows by 5.

GATE (on master, sequential): compile clean; `build` exit 0 - audit 0/0/4, `s4Parity` 11
models, `g1Parity` 2 models, benchmark verified; `runGameTestServer` exit 0 - literal
`All 198 required tests passed` (193 + 5 Vortex pins). Not pushed.

## ENT-S-090 FIX — pressure-plate override restored on fifteen entities (2026-09-02)

Owner's go. The mechanical sweep (reference `func_145773_az` returning true
versus port `isIgnoringBlockTriggers`) found 15, not ~35: twenty of the
original overrides return false, which is the default. Fourteen entities
patched here (Vortex in ENT-S-089), each citing its reference line; one
parameterised gametest over the fifteen registry ids with a zombie control.
Suite grows by 1.

GATE (on master, sequential): compile clean; `build` exit 0 - audit 0/0/4, parity and
benchmark verified; `runGameTestServer` exit 0 - literal `All 199 required tests passed`. Not pushed.

## REFERENCE-GEOMETRY GATE + BUG-041 STAGE 1 (EnderReaper A/B) (2026-09-02)

STANDING GATE. `tools/reference_model_proofs.json` lists every port entity
model that pairs with a 1.7.10 model (87); `referenceDumpCompiledModels`
(probe, vanilla mode) and `referenceGeometry` (the leg, `--proof-dir
phase_g_reports/reference_proof`) run under `check` ahead of the suite.
Models still carrying ruled parity bugs PIN their exact divergence counts
(`pinned_divergences`, e.g. `MIRROR: 55`): the leg passes only when the
observed categories equal the pin, so a new divergence or a partial fix
without a pin update is red; a pin is cleared in the same commit as its
fix. 84 models are pinned (BUG-041 mirror, ENT-S-091 geometry); exact today:
kyuubi, coin, enderreaper. The leg's reports are checked in and drift-verified like
the Phase G proofs.

BUG-041 STAGE 1. EnderReaper's 66 `.mirror()` calls are dropped (ModelEnderReaper);
its pin is cleared and the leg reports an exact match against the parsed
1.7.10 source. This is the owner's A/B model: most asymmetric texture among
the mirror-only models (mean texel difference against its own horizontal
mirror 0.297). To compare: the release jar in the instance root renders the
old mirrored faces; this build renders them as 1.7.10 did. The port-wide
drop (81 more models) waits for the owner's word after the look.

Law 11 for the ordering claim is closed from Mojang's 1.7.10 client jar
(see AUDIT BUG-041). build.gradle changed (two tasks): benchmark proof
rewritten.

## BUG-041 LAW 11 CLOSED; ENT-S-093 SPLIT PRESENTED (2026-09-02)

The 1.7.10 ordering claim behind the mirror finding is now verified from
Mojang's official 1.7.10 client jar (version manifest -> SHA-1
e80d9b3bf5085002218d4be59e668bac718abbc6 over 5256245 bytes; no copy existed under Prism):
`ModelRenderer` copies the ModelBase texture size in its constructor,
`addBox` hands the box only the texture offsets, and the `ModelBox`
constructor reads the mirror flag and the texture size itself; a flag or
size set afterwards never reaches an existing box. The A/B model for the
owner is EnderReaper (most asymmetric texture, 0.297). ENT-S-093: all 14
shared-state ports are parity bugs (every original kept a per-entity
RenderInfo); eight carry further formula divergences, listed in the audit
entry. Presented; no fix applied yet.

## ENT-S-092 METHOD VERIFIED; PER-RENDERER FINDINGS (2026-09-02)

Owner's condition met before the count is trusted: 49 renderers verified
against every scale path by reading (two adversarial passes, 61/63
refutations failed; one label corrected, Mosquito is DIVERGES). World scale
diverges in 44; shadow in 48. The mechanical sweep's shadow
verdict held on every verified row, its scale heuristic did not (5 false
positives: baby branches, a shared renderer, a compensating hook), so the
scale column is a screen only. The sweep parser was fixed twice on the way
(generic constructors; balanced-parenthesis argument parsing for shared
renderers such as Mothra's). Findings: phase_g_reports/renderer_findings.md.

GATE (on master, sequential): `build` exit 0 - audit 0/0/4, `referenceGeometry` (87
checked-in reports verified; EnderReaper exact, 84 pinned, Coin and Kyuubi exact),
`s4Parity` 11 models, `g1Parity` 2 models, benchmark evidence verified (build.gradle
pin); `runGameTestServer` exit 0 - literal `All 199 required tests passed`. Not pushed.

## ENT-S-091 SLICE A — six rigs regenerated from the 1.7.10 source (2026-09-02)

Owner's ruling: parity bugs, fixed in classic with the reference leg as
proof. Island, IslandToo, Skate, Mosquito, Ghost: the port's hand-authored
rigs replaced by the originals' geometry (generated from the parsed
constructors) and the originals' animation (transcribed from their render
bodies); StinkBug: geometry regenerated (the port had dropped the trailing
`+= 6.0f` pivot adjustments), animation unchanged. Renderer scale/shadow from
the verified ENT-S-092 findings for these species. Reference pins cleared:
the standing gate now requires an exact match for all six (plus Coin,
Kyuubi, EnderReaper). Island/IslandToo GeckoLib candidates re-proven on the
new rigs (age-driven samples over the 125.66-tick slowest period); their
earlier acceptance is void per the owner; re-acceptance requested. Slice B
(Elevator, CaterKiller, Tshirt reads; SeaViper) follows.

GATE (on master, sequential): compile clean; `build` exit 0 - audit 0/0/4, `referenceGeometry`
101 checked-in reports verified (exact: coin, kyuubi, enderreaper, island, islandtoo, skate,
mosquito, ghost, stinkbug), `s4Parity` 11 models (Island/IslandToo re-proven; visual excluded
fraction pinned at the observed 0.4642 - three interpenetrating cubes - IN-GAME ACCEPTANCE
REQUIRED), `g1Parity`, benchmark verified; `runGameTestServer` exit 0 - literal `All 199 required tests passed`.
Not pushed.

## ENT-S-091 SLICE B — Elevator and CaterKiller equivalent re-expressions made leg-exact; Tshirt restored (2026-09-02)

Elevator's +24 px pivot bake cancelled the MobRenderer lift the original's
plain Render never applied: equivalent, now expressed the original's way
(pivots 0, lift cancelled in the renderer, GeckoLib candidate translated
1.5 + 0.01 down after applyRotations). CaterKiller's 49 extra parts are the
unrolled render loops; the leg now checks declared multiplicities per copy
(`unrolled_parts`, new UNROLL_ARITY category) and the motion read is pinned
in the manifest. Tshirt genuinely diverged and is restored (two quads,
declared 512x256 sheet, scale 0.33). Reference pins: elevator and tshirt
cleared (exact required); caterkiller mirror-only. Elevator's s4 proof
rewritten; re-acceptance requested with Island/IslandToo. SeaViper next.

GATE (on master, sequential): compile clean; `build` exit 0 - audit 0/0/4, `referenceGeometry`
101 verified (tshirt exact; elevator and caterkiller mirror-only pins), `s4Parity` 11 models
(Elevator re-proven), `g1Parity` 2 models (G1 proof regenerated for Elevator's geometry -
the classic model changed; evidence captures identical), benchmark verified (proof
rewritten for the generated-file pins); `runGameTestServer` exit 0 - literal `All 199 required tests passed`.
Not pushed.

## ENT-S-091 SLICE C — SeaViper regenerated and re-transcribed (2026-09-02)

Rig from the parsed 1.7.10 constructor (with the +32 z shifts the port had
dropped); animation transcribed from the original's render body; two
refuters, no defects; the offset-versus-rotation semantics proven from the
1.7.10 ModelRenderer bytecode (offsets translate unscaled before the scaled
rotation-point translate). The old port had written offsetZ into zRot. Pin
cleared; exact match required by the standing gate. ENT-S-091 is complete:
ten models, seven regenerated, two equivalent re-expressions made
leg-exact, one restored from a re-authoring.

GATE (ents091c): build green (asset audit 0/0/4 acknowledged; g1Parity 2, s4Parity 11; referenceGeometry 101/101 PASS, reference_seaviper 34 parts exact, no pins drifted; benchmark verified); runGameTestServer: All 199 required tests passed.

## ENT-S-094 FIX — Elevator drawn as the 1.7.10 plain Render on both paths (2026-09-03)

Classic ElevatorRenderer skips every living-only rotation (shake, sleep, death
flip, upside-down), draws at the entity yaw the 1.7.10 RenderManager passed,
renders with NO_OVERLAY (no hurt tint), shows no name tag. The leash line
cannot be dropped on the classic path (EntityRenderer.renderLeash is private,
reached only from render()) and is left on both paths symmetrically; owner
options: Elevator.canBeLeashed() false (a gameplay change: the 1.7.10 board WAS
leashable, it just drew no line), or copy render(). The
GeckoLib seam gains a per-species non-living mode (`nonLivingRender()` on the
descriptor, consulted in applyRotations / getPackedOverlay / shouldShowName);
Elevator's descriptor opts in, every other species is unchanged. Override
points proven from the 1.21.1 and GeckoLib 4.8.4 bytecode; two refuters
upheld. Residuals disclosed in AUDIT_FINDINGS (invisibility render type; shadow
keeps the engine's scale/age multipliers).
Follow-up (refuted once, upheld): both paths draw at Mth.lerp(partialTicks, yRotO, getYRot()), the exact 1.7.10 RenderManager formula (bytecode: prev + (cur - prev) * partial, no wrap; the port's LivingEntity.tick keeps yRot - yRotO within 180 so rotLerp could not differ), instead of the lerped body yaw.

## ENT-S-092 BATCH 1a — 1.7.10 renderer scale and shadow restored on 38 renderers; pin leg added (2026-09-03)

Truth table over all 133 registrations (1.7.10 RenderX constructor +
preRenderCallback versus every port scale path), refuted per chunk. 38 renderer
files restored in the CoinRenderer house style with the original's baby
branches transcribed; Robot3 and Beaver candidates matched; one refuter per
chunk, all upheld. `tools/reference_renderer_pins.py` pins every renderer's
scale and shadow in the reference gate (`referenceRenderers` under `check`);
the 40 shadow-only renderers and the five bosses stay PENDING in the manifest
until their batches land. Recheck list and changelog note under
phase_g_reports/. Hitbox dimension divergences found on the way: ENT-S-095.

## ENT-S-093 FIX — per-entity RenderInfo restored on 14 species; formula divergences transcribed (2026-09-03)

Every species keeps its selector/filter state on the entity again (Kraken
pattern); the formula divergences in CaveFisher, Cephadrome, Dragon,
DungeonBeast, Leon, Ostrich, ThePrinceTeen and the others listed in the split
are transcribed line for line from the original render bodies, each draft
upheld by two independent refuters before install (the SeaViper standard),
then each install refuted once. Pose interfaces for CaveFisher, Ostrich,
PitchBlack. Gametest RenderInfoParityTests (15 tests) pins the per-entity
holders server-side.

GATE (remediation-0903): build green (asset audit 0/0/4 acknowledged; g1Parity 2, s4Parity 11 with the Elevator conversion proofs rewritten; referenceGeometry 101/101; referenceRenderers PASS 75 / PENDING 45 / NOT_APPLICABLE 13 / DIVERGES 0; benchmark proof rewritten for build.gradle + Elevator inputs); runGameTestServer: All 214 required tests passed (RenderInfoParityTests in its own batch; with the 15 new tests in the default batch, bug003_rat_ai_ticks_and_despawns and dsb_item020_towers_maze_rookery failed on the reshuffled buckets and passed again unchanged once the batch was isolated: TEST-003).

## ENT-S-092 BATCH 2 — 1.7.10 shadow radius restored on the 40 shadow-only renderers (2026-09-03)

Shadow constants written as the original's `par2 * par3` products (or the plain
literal where the original passed one argument; RockBase 0), passed to super;
world scale untouched where it already matched (LunaMoth and Mothra now apply
their existing 1.5 / 10.0 through a SCALE constant, value-identical, so the pin
leg can see the constant). Robot1, Robot2, Robot4, Vortex and RockBase GeckoLib
descriptors matched. One refuter per chunk, all eight upheld. Pin manifest: 114
pins, 13 not applicable, five bosses pending (batch 1b, consequences presented).
Crab and PitchBlack: shadow pinned, scale axis DYNAMIC (entity getter on both
sides). Further hitbox-dimension notes folded into ENT-S-095.

GATE (ents092-batch2): build green; referenceRenderers PASS 115 / PENDING 5 at this batch (119 / 1 after batch 1b); runGameTestServer: All 214 required tests passed (gate b2, then the final gate of the day on the full tree).

## ENT-S-092 BATCH 1b — four bosses restored to the 1.7.10 render scale; The Queen held (2026-09-03)

Five boss reads, each refuted once, on how hit surfaces couple to the render
scale. Kraken (3 -> 1), SeaMonster (3 -> 1), TheKing (1 -> 2.1) and Godzilla
(3 -> 2) have no coupling (plain AABBs or code-offset part entities), so the
ruling applies directly: constants, shadows and the PlayNicely quarter-scale
branches restored in the house style, one refuter upheld; pin manifest 119
pins. TheQueen is PARTIAL: her MHLib parts follow the drawn bones when the
scale sits after GeckoLib's capture, so the 1.7.10 size relocates the hit
surfaces onto the drawn body; presented for the owner, PENDING in the
manifest. Kraken's missing PlayNicely mode filed as ENT-S-096.

GATE (ents092-batch1b): build green; referenceRenderers PASS 119 / PENDING 1 (TheQueen); runGameTestServer: All 214 required tests passed (gate b1b and the final gate after the pin-leg edits).

## ENT-S-096 FIX — Kraken PlayNicely mode restored (2026-09-03)

Hitbox 4x15 / 1.3333334x5 chosen at construction from the PlayNicely config,
never resized afterwards (the original's constructor-only setSize and the
King/Godzilla pattern); the render scale drops to a third while nice through
the synched datum the original kept in watcher 21. Four gametests in their own
batches pin both hitboxes, the no-resize snapshot and the datum. Refuted once.
The four behavioural PlayNicely gates (weather, lightning, prey search, target
search) are filed as ENT-S-097 for the owner's go. ENT-S-094's residuals were
accepted by the owner and the board shows no name tag by ruling; recorded.
The stray src/danger copy of the 1.7.10 sources was deleted in its own commit
after a reference check (only historical Phase D specs mention it).

GATE (ents096): build green (referenceRenderers PASS 119 / PENDING 1); runGameTestServer: All 218 required tests passed on the 096 tree (gate s096), then All 281 on the tree with ENT-S-095 batch 1 (gate s095b1, which carries the guarded datum test).

## ENT-S-095 BATCH 1 — 63 hitbox registrations restored to the 1.7.10 setSize (2026-09-03)

Population sweep over all 145 registrations (port .sized / getDefaultDimensions
versus every 1.7.10 func_70105_a call, PlayNicely and dynamic branches tracked):
67 divergent, none MOD-recorded. Batch 1 restores the 63 plain registrations to
the original literals with a citation per line (largest: Tshirt 4x4, Molenoid
3.9x2.6, EmperorScorpion 3.5x3, HerculesBeetle 3.25x2.75, Robot2 3x6.2, Mantis
2.5x3.25; SeaMonster shrinks 5x5 -> 1.25x2.5). Both-modes dims-pin gametests in
their own batch. Refuted once. Batches 2 (Godzilla, Mothra) and 3 (TheQueen,
MHLib lockstep) pending; the Queen's extent comparison is reported separately.

GATE (ents095-b1): build green (referenceRenderers PASS 119 / PENDING 1; g1Parity 2, s4Parity 11; referenceGeometry 101/101); runGameTestServer: All 281 required tests passed (218 prior + 63 HitboxDimsParityTests in batch hitboxDimsParity; gates s095b1 and s095b1b).

## ENT-S-095 BATCH 2 + RULINGS — Godzilla 9.9, Mothra 5x2, cows, fireball, transients (2026-09-03)

Godzilla's registration and classic branch return to 9.9x25 with the PlayNicely
quarter kept and both modes pinned. Mothra returns to 5x2 in classic mode; the
port comment that enlarged it is quoted in AUDIT_FINDINGS and its reason filed
as MOD-029 (config-gated modern option, proposal only). Red ant and termite
keep 0.2 with the EntityAgeable transient recorded; the apple cows follow the
cow line to 0.9x1.3; BetterFireball.setSmall shrinks the box again;
cannon_fodder is documented as port-only and pinned. Seven new gametests.

GATE (ents095-b2): build green (referenceRenderers PASS 120 / PENDING 0; queenPartPlacementProbe OVERALL PASS); runGameTestServer: All 295 required tests passed (gate day3c on the full tree: 281 prior + 7 batch-2 + 3 Queen + 4 Kraken-gate tests).

## ENT-S-097 FIX — Kraken's four PlayNicely behaviour gates restored (2026-09-03)

Weather summoning, the lightning roll, the prey search and the target search are
gated on the live PlayNicely config exactly where and how the original gated them
on its static, roll-before-flag order kept. Four gametests in four batches pin
each gate under both flag states with the weather snapshotted and restored.
Refuted once. Two pre-existing Kraken targeting divergences noted for the ledger.

GATE (ents097): build green; runGameTestServer: All 295 required tests passed (gate day3c; the four gate tests in batches krakenGateWeather/Lightning/Prey/Target, weather read through the ServerLevelData flags, prey test in empty_tall below the barrier ceiling).

## ENT-S-092 THE QUEEN + ENT-S-095 BATCH 3 + BUG-042 — render scale 2.0 restored, parts on the drawn body, PlayNicely box back (2026-09-03)

The extent comparison proved the port rig is the 1.7.10 model drawn at half
size, so the second branch of the ruling applied: SCALE 2.0 / SHADOW 3.8 applied
in GeckoLib's scaleModelForRender, after the entityRenderTranslations capture, so
the bone world matrices carry it; composite PlayNicely scale 0.5 = 1.7.10's
2.0 / 4. Profile part sizes, pivots and fallbacks derived per part from the drawn
segments at 2.0; main size 22x24 unchanged. Part placement verified headlessly
with GeckoLib's own matrix code (exact linearity, boxes contain their segments in
both modes); the same probe exposed BUG-042: bone matrix tracking had never been
enabled, so MHLib had been receiving the world origin for every synced bone; fixed
in the renderer. PlayNicely box 5.5x6 restored through the MHLib size callback
with a LOW-priority size listener; both-modes pins. BUG-035 scan: asset audit
check 8 green, animation json and controllers untouched. Refuted once.

GATE (queen): build green (referenceRenderers PASS 120 / PENDING 0; queenPartPlacementProbe: linearity PASS, placement PASS at body yaw 0/45/90/180 in both modes, collector lifecycle PASS; asset audit 0 errors; benchmark proof rewritten for build.gradle + the g1tool classpath); runGameTestServer: All 295 required tests passed (gate day3c on the full tree).

## AMENDMENT — BUG-042 downgraded (2026-09-03)

Law-11 re-read of GeckoLib 4.8.4: GeoBone's matrix getters arm tracking on their
first call (getWorldSpaceMatrix offsets 0-2), so the untracked-bone gap behind
BUG-042 lasted one render frame per bake, not the whole stream; the explicit
enablement in QueenRenderer.preRender stays as the fix for that first frame. The
record, not the commit, is amended. ENT-S-099 closes: the robots have no synced
bones and vanilla renderers.

## RULINGS 2026-09-03 (second batch) — ENT-S-098 fixed, MOD-029 accepted, scanner tightened, findings filed

ENT-S-098: a fired BetterFireball now carries `orespawn:better_fireball` through a
constructor that replays the vanilla kinematics; save/load round trip pinned; the
projectile sweep found one sibling (UltimateFishHook cast as a vanilla bobber,
filed) and a double explosion on impact (ENT-S-102, filed). MOD-029 accepted as
the modern-mode default: the config gains its first master switch, `[modern]
enabled` (default false) with `mothraWideRootHitbox` beneath it, Mothra snapshots
it at construction (6x3 modern, 5x2 classic), four tests in their own batch; the
existing per-feature 2.0 keys stay independent pending the owner's word. The
renderer pin scanner now tracks reassignments of float locals (last unconditional
write wins; an if-branch write keeps the pre-branch default; loops, switches and
lambdas unbind with a reason; `%=`, `++` and `--` forms and nested braceless ifs
unbind as forms the scan does not read): 73 lab cases under two refuter rounds, six
of which flipped from PASS to DIVERGES where the blind spot was (the unreadable-
write forms, the dangling else, a braceless while); no repo pin changed (PASS 120
before and after), so nothing needed presenting before the gate. Documented
limitation: a braceless `for` wrapping a braced if reads as a plain if-write because
the header's semicolons hide the loop from the look-back (the default-branch value is
still what the declaration holds). One interpretation (a non-evaluable write on a
non-default branch is treated like a ternary's taken branch) presented for ruling.
Kraken targeting filed as ENT-S-100 with its split (five parity bugs, one recorded
convention) and the shared ignore list as ENT-S-101.
BUG-043 upstream: the vendored copy carries upstream's final collector code and BUG-043's static-accumulator aliasing is present in that final state; the upstream repository DerToaster98/MultiHitBoxLib was deleted between 2026-05-10 and 2026-09-03, its last release was MC1.20.1-1.8.1 (2024-10-03), and we are 0 releases / 0 commits behind, i.e. effectively the maintainer, so the fix could not be taken from upstream. Licensing flag for the owner: upstream's LICENSE file is LGPL-3.0 while its gradle declares mod_license=All Rights Reserved and its README forbids jar-in-jar, forks and ports.
1.7.10 reference instance: Prism instance 'OreSpawn 1.7.10 Reference' created (Minecraft 1.7.10, Forge 10.13.4.1614, LWJGL 2.9.4-nightly-20150209; the Java 8 legacy runtime installs on first launch) with the owner-supplied orespawn-1.7.10-20.3.jar (sha1 d43dbe9a400dc8df06418da3e04d36422b2176d7) under minecraft/mods; nothing launched; owner look sheet section F carries the first-launch checklist.

GATE (rulings2): 2026-09-03 21:53-21:57: g1BenchmarkVerify green without regeneration (no provenance drift); build green (asset audit 0 errors / 0 advisories / 4 acknowledged; G1 PARITY PASS 2 + 11 models, checked-in proofs verified; referenceGeometry; referenceRenderers PASS 120 / NOT_APPLICABLE 13; queenPartPlacementProbe); runGameTestServer: All 303 required tests passed (295 + 4 ProjectileTypeParity + 4 MothraModernDims) in 2m45s; logs rulings2.build.log / rulings2.suite.log.

## RULINGS 2026-09-03 (second batch, addendum) — MoreHitboxes per-feature comparison against the vendored MHLib

MoreHitboxes per-feature comparison against the vendored MHLib (owner clarification of item 11: not a migration decision; MHLib stays; identify what MoreHitboxes does better and port those pieces into the vendored MHLib under MIT with attribution, the most performant design per feature; both libraries side by side only if the bytecode shows their mixin targets do not collide). Report at phase_g_reports/morehitboxes_evaluation.md; the first, keep-or-switch report is kept as morehitboxes_evaluation_v1_superseded.md because the new one cites it. Collision verdict: exactly one shared mixin target (GeoEntityRenderer.renderRecursively), both non-cancelling at distinct instructions, so Mixin would load both, but the ruling's bar is not met and side by side stays off the table. Harvest list, ranked: per-entity once-per-tick bone collection with MHLib-native trims (also fixes BUG-044), Player.attack part-to-parent unwrapping, conservative cull bounds (an OPT-013 re-ruling), piercing ignore-list correctness, a fixed-layout binary bone payload, descriptor-exact renderRecursively selectors (OPT-028), the attack-box data shape resolved server-side against MHLib's synced parts, defaultRequire 1 for the mixin config. Not worth porting, with reasons: MoreHitboxes' server-side placement, its trust model (none), its profile sync, the hit-result parent rewrite, its Level.getEntities mixins, anchors, fixPosOnRefresh. Item 13 fold specified: counters to add on the MHLib side (bones visited, recursive start/end, world-position reads, folds, bone infos built, collector nanoseconds and allocated bytes, C2S/S2C packets and bytes, server alignment and placement time) with six isolation scenes and a proposed, not adopted, regression threshold. Two findings filed from the comparison: BUG-044 (the per-renderer collection stamp wedges on a two-tick frame and starves all but one entity per renderer) and OPT-028 (bare-name selectors hook GeckoLib's bridge: double push/pop per bone for every GeckoLib entity). Attribution mechanics proposed (LICENSE-MoreHitboxes.txt in META-INF and beside the vendored sources, per-file headers on ported code, a third-party notices section; the MHLib licensing flag restated). Verification: refuted once on the session model after four launches died on server-side API errors (owner: rerun those, redo anything that ran on another model); the refuter's blocking defect (the orchestrator's interim 'no server-visible behaviour change' correction for harvest 1) and its minors are applied in the installed text. No code changed.

## RULINGS 2026-09-04 — BUG-044/OPT-028, ENT-S-100/101/102, projectile tags, modern master, scanner, MHLib licensing

BUG-044 fixed: the once-per-tick bone-collection gate is a per-entity render-tick stamp (stamp < tickCount, util/RenderTickGate, design after MoreHitboxes' GeckoLibMobMixin, MIT text shipped and attributed), decided once per render pass and keyed on the actual entity on both renderer paths; RenderTickGateTests (own batch) pin the hitch and two-Queen cases on spawned Queens, and the placement probe's new section 4b drives the real layer through both cases (exit 1 on regression). OPT-028 fixed: the three geckolib mixins name one method each by full descriptor (GeoEntityRenderer's typed Entity form; the erased GeoAnimatable form for the replaced renderer and the interface, which have no bridge), so the per-bone push/pop runs once; util/MHLibCounters (-Dmhlib.counters=true, one INFO line per 100 client ticks) is the proof instrument: recursive_start divided by frames reads 110 with one Queen in view (220 on the bare-name build); a throwaway bare-name build is staged beside the fixed jar as the literal before. Refuted twice (MHLib semantics against the 4.8.4 bytecode; tests, probe, compile and attribution), upheld; the accessor glue in onPreRender/onPostRender is covered by inspection only (coverage gap noted).
ENT-S-100 fixed (five items: the nearest player of any mode with the creative null at the call site; the orig-order isSuitableTarget chain with the shared ignore list and the ten exclusions, ridden mounts via isVehicle; flying, not invulnerable; hold until removal; MobStats max health) and ENT-S-101 fixed (isIgnoreable restored to the 1.7.10 twelve; LunaMoth stays ignoreable through Butterfly in both trees, so six species newly spared and three newly hunted); 48 pins in three own batches; refuted twice (fidelity against the 1.7.10 sources and the verified 1.7.10 jar's bytecode; test validity and compile), upheld. Filed on the way: ENT-S-105 (nearest-player tie-break), ENT-S-106 (the ignore screen missing from most hunters: orig 38 callers, port 11), ENT-S-107 (Leon and Cephadrome map creative to invulnerable).
ENT-S-102 fixed (one explosion per impact at the port's power, none when small; `BetterFireball.onHit`
replays `Projectile.onHit`'s dispatch and never reaches `LargeFireball.onHit`), two pins. Projectile
tags: the 1.7.10 bows applied Punch and Flame but never Power (orig UltimateBow.java:52-57,
SkateBow.java:53-58), so by the owner's condition the ultimate and irukandji arrows stay OUT of
`#minecraft:arrows` (pinned) and do not grant the shoot_arrow advancement; the throwable family
joined `#minecraft:impact_projectiles` as MOD-030 (pinned). Two findings filed on the way:
ENT-S-103 (UltimateArrow never receives Punch knockback) and ENT-S-104 (BetterFireball no longer
places fire beside a block hit).
Modern master (owner: "master override only"): `OreSpawnConfig` gains one effective-value helper per
modern feature (`spiderMovement()`, `mountCamera()`, `phase14ContentEnable()`, beside
`mothraWideRootHitbox()`), every read site routed, keys unchanged in name, section and default;
`ModernMasterOverrideTests` (own batch) pins the truth table and the routed robot construction;
the six modern-gait test classes now raise the master with the key. CONSEQUENCE ON A DEFAULT CONFIG
(master false): `tweaks.spiderMovement` (default MODERN) and `tweaks.mountCamera` (default true)
are effectively CLASSIC / off until `modern.enabled = true`; `phase14ContentEnable = true` also
needs the master. MOD-029 amended, MOD-021 annotated, KNOWN_ISSUES and the changelog note updated.
Scanner (owner: "a write inside a non-evaluable branch is not provable; report it as pending for
presentation, never assume a branch"): such writes now yield status PENDING with a `not provable`
detail (a default-path non-evaluable write stays DIVERGES); lab: W38 PASS → PENDING as expected, and
W50 PASS → PENDING because a string literal containing `; if (` had fooled the statement look-back
into a false PASS (string-literal lexing is a separate pre-existing blind spot, presented for its own
ruling); repo leg identical before and after (PASS 120 / PENDING 0 / NOT_APPLICABLE 13, no entry
changed), so nothing needed presenting before the gate. Record in
phase_g_reports/renderer_pin_scanner_lab_2026-09-03.md.
MHLib licensing (lane F): upstream MultiHitboxLib's LICENSE.md is the unmodified FSF GNU Lesser General
Public License, Version 3, 29 June 2007 (git blob 0a041280bd00a9d068f503b8ee7ce35214bd24a1, 165 lines),
byte-identical on master at the MC1.20.1-1.8.1 commit 166a4fd, on the final master commit 5480d37 and
on the 1.21-NeoForge head c555dc0 the vendored copy derives from (read through the Binaris00 fork
network; the SHAs match the currentOid values in Wayback captures of the live repo); GitHub's own
detection showed "LGPL-3.0 license" through the last capture on 2026-05-10 and the README blob returned
404 by 2026-06-30. Side by side: gradle.properties line 10 `mod_license=All Rights Reserved` on every
branch, expanded by build.gradle into mods.toml / neoforge.mods.toml line 3 `license="${mod_license}"`,
so every released jar declared All Rights Reserved; README "Terms of use": "The license needs to be met
(GNU license)." plus no jar-in-jar/shadowing, no forks or ports, no re-uploads, mandatory credits and
commercial use by e-mail permission; Modrinth project zxK3GsTY declared LGPL-3.0-or-later (captures
2023-12-01 to 2024-06-13) then AGPL-3.0-or-later linking the README (2025-05-23 to 2025-12-28) and is
now deleted; CurseForge project 899090 declared "Custom License" whose text opens "The GPL license is
to be applied here." (capture 2024-10-14) and is removed too. Standing rule: the LICENSE text (LGPL-3.0)
governs until the owner says otherwise. Contact: neither Modrinth nor CurseForge offers messaging and
the project is gone from both; the author's own designated channels are e-mail
(dertoaster@cq-repoured.net, the README's permission channel) and the MHLib Discord server; a draft
e-mail is staged in the owner's mailbox, unsent, awaiting the owner's word on the channel.
Refuters this batch, all on the session model: lane A two (upheld), lane B two (upheld), lane C one (upheld; minors applied: the explosion fire-flag mapping recorded under ENT-S-104, a shooter-alive assertion added), lane D one (upheld; the client-side config clause added), lane E one (upheld; two follow-up fixes applied and re-run: failures outrank PENDING, ternary arms on a branch are never assumed; repo leg unchanged).

GATE (rulings3): 2026-09-04 01:57-02:00: g1BenchmarkVerify drifted (main classes changed) and the benchmark was regenerated (proof updated: SMOKE_ONLY / COMPONENT_PROXY_ONLY / PENDING_LIVE_PRECUTOVER); build green (asset audit 0 errors / 0 advisories / 4 acknowledged; G1 PARITY PASS 2 + 11 models, checked-in proofs verified; referenceGeometry; referenceRenderers PASS 120 / NOT_APPLICABLE 13; queenPartPlacementProbe including the new section 4b); runGameTestServer: All 369 required tests passed (303 + renderTickGate 3 + ignoreListParity 26 + krakenTargetingParity 21 + krakenHoldRelease 1 + modernMasterOverride 11 + projectileTypeParity 4) in 1m23s; logs rulings3.build.log / rulings3.suite.log.

## RULINGS 2026-09-04 (second batch) — modern.enabled default true, ENT-S-103..107 fixed, MOD-031 proposed, MHLib licensing closed

Config default ruling (owner, 2026-09-04): `modern.enabled` defaults to true — "the master defers to
per-feature keys by default and only forces classic when set false." `OreSpawnConfig` `[modern] enabled` is
now `define("enabled", true)` (introduced 2026-09-03 with default false, flipped by this ruling); the spec
comment, javadoc and the three per-feature pointers carry the new semantics; no other key, name, section or
default changed and the four effective-value helpers are untouched. Pin:
`ModernMasterOverrideTests#master_defaults_true_and_default_config_reads_modern` (batch
`modernMasterOverride`): `getDefault()` is true, and with the master true and the keys at their spec
defaults `spiderMovement()` reads MODERN and `mountCamera()` reads true — the default experience is the
modern robots with the riding camera again. Docs rewritten (MODERNIZATION_NOTES MOD-029 and MOD-021,
KNOWN_ISSUES, README's "Modern robots" row, the changelog note): the master defaults to true and defers to
the keys; `modern.enabled = false` is the one-line switch to the exact 1.7.10 experience for every 2.0
feature at once; `spiderMovement = "CLASSIC"` still works per feature; `phase14ContentEnable = true` alone
is enough again unless the master was set false; the previous "consequence on a default config" text (the
batch entry above) is superseded. Harness consequence: `HitboxDimsParityTests#s095_mothra_dims_both_modes`
pins the classic 5x2 and now forces the master off around the pin (restored in finally), because on real
defaults a fresh Mothra is the modern 6x3 (MothraModernDimsTests pins that box); the untracked generated
gametest run config `runs/gameTestServer/config/orespawn-common.toml`, whose only non-default value was the
stale `[modern] enabled = false`, was deleted so NeoForge regenerates it on real defaults (backup in the
session scratchpad). Credits (LGPL ruling): `gradle.properties` `mod_credits`, the value behind
`credits="${mod_credits}"` in neoforge.mods.toml, now ends "Bundles MultiHitboxLib by DerToaster
(LGPL-3.0)."; README's Credits names DerToaster for MultiHitboxLib; the vendored `[[mods]]` entry is
untouched.

ENT-S-103 fixed: `UltimateArrow.doKnockback` reads the bow's Punch level off the weapon copy and pushes 0.6 ×
level along the flat flight line with the 0.1 lift (orig UltimateArrow.java:189-191, the IrukandjiArrow shape),
Mob-gated as orig :183; vanilla's Punch never reached the arrow because it is ruled outside #minecraft:arrows.
ENT-S-104 fixed: `BetterFireball.onHitBlock` places orig :232-264's fire on the air side of the hit face (air
check only, no mobGriefing gate, small shots included), and the impact explosion passes fire = true with MOB
resolving destruction through the gamerule — for the null source exactly `canEntityGrief(level, null)` =
RULE_MOBGRIEFING in NeoForge 21.1.223's bytecode — where the port had fed the gamerule into the fire slot. Four
pins in `projectileTypeParity`: the s103 two-lane velocity differential (0, +0.1, +1.2); s104 small-shot face
fire; big shot with the rule on (one fire-flagged explosion, DESTROY, the dirt hearth gone); big shot with the
rule off (one fire-flagged explosion, KEEP, wall, hearth and face fire intact; the flip waits out the
batch-mates' windows and is restored in a finally). MOD-031 filed: a config-gated "fire respects mobGriefing"
modern option, PROPOSED, not implemented. One refuter per finding, upheld.
ENT-S-105 fixed: `Kraken.findNearestPlayer` updates on `<=` per orig `World.func_72857_a` (1.7.10 `ahb`
bytecode `dcmpl; ifle`, update body on `d1 <= d0`): the last of two equidistant players wins; pinned with two
mirror-placed survival players whose scan order is read back with the scan's own call, plus an unequal-distance
control. Refuted once, upheld.
ENT-S-106 fixed: the shared ignore screen restored at the orig position in every hunter that had lost it — all
38 orig call sites mapped, 11 already present, 27 restored (17 in private filters, 9 as the predicate of the
vanilla NearestAttackableTargetGoal the port uses, 1 inline in the Urchin's players-only scan), none
unmappable; `IgnoreScreenParityTests` (own batch, a `@GameTestGenerator` producing 38 TestFunctions, one per
orig site) and a changelog entry. Scope note filed as ENT-S-108: eight goal-shaped hunters and the Urchin scan
players only in the port where 1.7.10 scanned living entities, so their restored screen cannot bite until
those scans are widened. Refuted twice (orig fidelity; tests and compile), upheld.
ENT-S-107 fixed: `EntityLeon` and `Cephadrome` test `instabuild` for orig's `isCreativeMode`, not
`invulnerable`; `CreativeMappingParityTests` (own batch, six tests: creative rejected, invulnerable-survival
still prey, survival control, per hunter). Refuted once, upheld. Siblings filed as ENT-S-109 (nine more hunters
with the `invulnerable` idiom).

MHLib licensing — item CLOSED (owner, 2026-09-04): "I'm not sending the e-mail; discard the draft. Keep the
LGPL text with the vendored sources and add DerToaster to the mod's credits." The draft e-mail staged in the
owner's mailbox was moved to the trash unsent; no contact was made on any channel. The LGPL-3.0 text ships
verbatim as `META-INF/LICENSE-MultiHitboxLib.txt` (inside the jar) and beside the vendored sources; DerToaster
is named in the mod's credits and README; the upstream field wordings stay recorded side by side (batch entry
above). Standing rule unchanged: the LICENSE text governs.

Refuters this batch, all on the session model: lane G one (upheld; a stale MOD-029 sentence fixed), lane H two, one per finding (upheld; the lane's note that Boyfriend/Girlfriend fire with Punch 0 was corrected: both trees seed Punch from the held bow), lane I one (upheld), lane J three, two on ENT-S-106 (orig fidelity; tests and compile) and one on ENT-S-107 (upheld; the Brutalfly/King/Queen rows disclosed as non-discriminating by construction).

GATE (rulings4): 2026-09-04 03:10-03:12: g1BenchmarkVerify green (no drift); build green (asset audit 0 errors / 0 advisories / 4 acknowledged; G1 PARITY PASS 2 + 11 models, checked-in proofs verified; referenceGeometry; referenceRenderers PASS 120 / NOT_APPLICABLE 13; queenPartPlacementProbe); runGameTestServer on the regenerated default run config (modern.enabled = true): All 420 required tests passed (369 + ignoreScreenParity 38 + creativeMappingParity 6 + krakenTargetingParity 2 + projectileTypeParity 4 + modernMasterOverride 1) in 1m22s; logs rulings4.build.log / rulings4.suite.log.
