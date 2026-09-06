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

## RULINGS 2026-09-04 (third batch) — ENT-S-108..113 fixed, MOD-031 accepted (default on), targeting survey ledger

ENT-S-108 fixed: nine hunters (Cave Fisher, Dungeon Beast, Emperor Scorpion, Hercules Beetle, Nastysaurus, Spit Bug,
T. Rex, Trooper Bug, Crystal Urchin) carry the 1.7.10 scan again — a private `findSomethingToAttack()` (PlayNicely
gate, `getEntitiesOfClass(LivingEntity.class, box)` with the orig box, `TargetSelection.firstMatch` over a
`GenericTargetSorter` = the orig sort plus first-accepted loop) over a private `isSuitableTarget` transcribing the
orig chain in orig order (the ENT-S-106 screen at its orig position, line of sight, the species chain, creative =
instabuild), called from `customServerAiStep` on the orig random gate; the vanilla Player-only goals (the Cave
Fisher's Player + Animal pair) and the Urchin's `getNearestPlayer(16)` are removed, since no vanilla goal reproduces
the orig sorter, box or chain. The scan's own pick is re-derived every cadence tick (cleared when the scan is
empty, so prey leaving the box or sight stops the chase as in orig) and a target set by another path (hurt, revenge
goal, forget roll) is left alone, tracked by a `scanPick` field cleared in a `setTarget` override.
`TargetScanParityTests` (own batch, 45 generated: living prey selected with no vanilla goal left, ignore-list species
refused, creative refused and the same player in survival prey, the box pinned on +x/+y/+z from both sides, one
species of the hunter's own chain refused, each with a pig control); the nine ENT-S-106 ignore-screen rows for these
hunters now probe the restored filters directly (38 rows, batch unchanged). Refuted twice (orig fidelity of the nine
transcriptions; tests and compile), upheld; the fidelity refuter's slot hardening applied. Two pre-existing
divergences stated and passed to the targeting survey ledger: Nastysaurus/TRex keep a revenge target the orig
blanked while out of sight or under PlayNicely, and CaveFisher/DungeonBeast/Urchin chase an attacker the orig never
read; residuals likewise (TRex's 1-in-200 revenge drop, the Urchin's inner swing die, forget rolls evaluated per
tick, armor stands passing the chains).
ENT-S-109 fixed: `Abilities.instabuild` for orig `capabilities.isCreativeMode` at the ten sites in nine classes
(Cryolophosaurus; Brutalfly's strafe and filter; GammaMetroid; Kyuubi; LeafMonster; LurkingTerror; Rat;
TerribleTerror; Triffid), nothing else in those methods; `CreativeMappingParityTests` gains a generator producing
30 pins (per filter site: creative rejected, invulnerable-survival still prey, survival control; the Brutalfly
strafe driven once under a forced random and read back through its flight target). No `invulnerable`-idiom
creative check remains anywhere in the port. Refuted once, upheld.
ENT-S-110 fixed: `EntityLeon.isSuitableTarget` carries orig's PlayNicely gate (:391) at the orig position and the
untamed tail grants only orig `MyUtils.isAttackableNonMob` targets (:422-427), the membership reproduced inline in
EntityLeon because the port's `MyUtils.isAttackableNonMob` is a different set (ledger item); `LeonTargetingTests`
(own batch, six tests: villager accepted, pig rejected, tamed rejects, PlayNicely refuses a Zombie, creative and
survival still hold). Consequence: `IgnoreScreenParityTests` row 15's pig control became a Zombie. Refuted once,
upheld.
ENT-S-111 fixed: the IrukandjiArrow hit block is gated on `Mob` (orig :181 EntityLiving), which wraps the arrow
count, the Punch push and the ding as orig did; one two-lane pin in `projectileTypeParity` (cow pushed 0.4 + 1.2
with the 0.1 lift, survival mock player at the vanilla 0.4 alone, `ultimateSwordPvp` raised for the window).
Refuted once, upheld; the refuter's faithful widening applied after refutation.
ENT-S-112 fixed: the eight orig ally exclusions restored in `PitchBlack.isSuitableTarget` in orig order at the orig
position; `PitchBlackAllyTests` (own batch, a generator producing one test per excluded species: species refused,
pig and Zombie controls accepted). Ledger item: the port filter still lacks orig :501's line-of-sight step.
Refuted once, upheld.
ENT-S-113 fixed: `Cephadrome.isSuitableTarget` opens with orig :516-518's PEACEFUL guard and its player branch is
orig :557-570 line for line, `shouldattack` spent on the one answer it grants (orig :567), so an unfed shark stalks
a refused rider for a single scan as in 1.7.10; `CephadromeGateTests` (own batch, six tests: PEACEFUL rejects the
player and the Zombie NORMAL takes, the reset pinned, untouched by a Zombie, behind bad mood and under the creative
rejection). Ledger item: orig :488's PEACEFUL gate on the hunt roll itself. Refuted once, upheld.
MOD-031 implemented (owner: "accepted as a modern option, default on; classic stays 1.7.10"): `[modern]
fireRespectsMobGriefing` (`MODERN_FIRE_RESPECTS_MOB_GRIEFING`, default true — the proposal's false overridden by the
ruling), read only through `OreSpawnConfig.fireRespectsMobGriefing()` = master && key; two gated sites in
`BetterFireball`, read at impact: `onHitBlock` places the face fire only if `EventHooks.canEntityGrief(level,
getOwner())` (for every owner — the gate vanilla `LargeFireball.onHit` applies; `SmallFireball.onHitBlock` gates
Mob owners only), and `onHit` passes that same answer as the explosion's fire flag, null source and MOB
interaction unchanged; master or key off = the ENT-S-104 classic calls exactly (`canEntityGrief` never invoked).
Pin: `FireballModernFireTests` (own batch `fireballModernFire`, one test running three sequential flights: option on
+ rule off → no face fire, fire flag false, KEEP; option on + rule on → the classic result; master off + rule off →
classic fire). Harness consequence: the classic rule-off pin `s104_big_shot_with_mob_griefing_off_...` forces the key
off around its window and restores it with the rule. MOD-031's notes entry is ACCEPTED; KNOWN_ISSUES and README
carry the sentence. Refuted once, upheld.
HARNESS (TEST-003 / TF-023 follow-ups, found by this batch's gate): two default-batch tests went red once the
new batches ran ahead of the default batch. (1) `bug003_rat_ai_ticks_and_despawns`: unowned rats are
MONSTER-category and `Mob.checkDespawn` discards them on any tick in which the nearest player is beyond 128
blocks; mock players from concurrent bucket-mates log in at the world origin, millions of blocks away, so the
outcome depended on which tests shared the bucket (the TEST-003 order sensitivity, now explained). Fix: a
creative keeper player inside the pen for the AI window (rats ignore creative players, orig Rat.java:227),
removed before the despawn half. (2) `spider_hangout_village_i164` (and the identical `red_ant_hangout_village_i165`):
the far build waited a fixed 5 ticks after the FORCED region ticket for the queued chunk promotion to drain;
under the fuller suite it did not, the persistent Robot Spider was spawned into a section not yet tracked,
and the pad query read 0 (diagnostic run: no player, no spider anywhere, `isPositionEntityTicking` false).
Fix: a `startSequence().thenWaitUntil(isPositionEntityTicking(spawnCell))` before the build, timeout 400.
Both tests stay in the default batch (isolating them for the diagnostic run flipped a third order-sensitive
test, `i127_tower_centre_rooms_spawn_jumpy_bug`, exactly as TEST-003 predicts). Diagnostics stay in the
failure messages (removal reason, health, difficulty, player count, nearest-player distance, PlayNicely).

Refuters this batch, all on the session model: lane K two on ENT-S-108 (orig fidelity of the nine scan transcriptions; tests and compile; upheld, the slot hardening and the Leon-row Zombie control applied after), lane L one each on ENT-S-109 and ENT-S-113 (upheld), lane M one each on ENT-S-110, ENT-S-111 and ENT-S-112 (upheld; the Irukandji gate widened to orig's whole block after refutation, as its refuter proposed), lane N one on MOD-031 (upheld; the gate wording corrected), and one spot-check on the targeting survey ledger (one status corrected: EnderReaper's release rule is a divergence; counts propagated).

GATE (rulings5): 2026-09-04 07:54-07:56, the fourth run of this batch (the first three were red on two default-batch tests, bug003_rat_ai_ticks_and_despawns and spider_hangout_village_i164, diagnosed and fixed in the harness, see HARNESS above): g1BenchmarkVerify green (no drift); build green (asset audit 0 errors / 0 advisories / 4 acknowledged; G1 PARITY PASS 2 + 11 models, checked-in proofs verified; referenceGeometry; referenceRenderers PASS 120 / NOT_APPLICABLE 13; queenPartPlacementProbe); runGameTestServer: All 517 required tests passed (420 + targetScanParity 45 + creativeMappingParity 30 generated + cephadromeGates 6 + leonTargeting 6 + projectileTypeParity 1 + pitchBlackAllies 8 + fireballModernFire 1) in 1m23s; logs rulings5c.build.log / rulings5c.suite.log.

## TARGETING WAVE 1 (2026-09-04) — ledger batches fixed in classic

T7 → ENT-S-114 (PEACEFUL gates, S, one refuter): eleven port sites transcribed at the orig positions with
the orig polarity and term order — AntRobot's unridden block and its ridden stomp and hunt (orig :105, :617,
:620), the Cephadrome's hunt roll (:488, a stored revenge target spared on Peaceful), the Dragonfly's hunt
roll and filter (:142, :198; the held-prey bite carries the branch's gate until T5 makes the pick
transient), GammaMetroid's caller and filter (:241, :254), PurplePower's call site, its every-AI-tick
Peaceful discard and its filter (:173, :180-182, :236). `PeacefulGateParityTests` (own batch, 12 generated:
each site driven once on NORMAL and required to act, then on PEACEFUL flipped inside the test and required
to stand down, the difficulty restored in a finally). The Girlfriend's Valentine gate (port-only) goes with
the T9 split. Refuted once, upheld.

T1 → ENT-S-115 (PlayNicely gates, M, one refuter): 41 port sites in 38 files transcribed at the orig positions,
the flag read live as `OreSpawnConfig.PLAY_NICELY.get()` (the ENT-S-110 / BOSS-017 idiom) — 29 head-of-scan
gates (the Irukandji's, Skate's and Sea Monster's inline picks gated as a whole, as orig's scan method held the
stored-target read behind the gate; the Hammerhead's inline scan likewise), the Hammerhead / Nastysaurus / TRex
revenge blankings on the pass's copy of a foreign occupant with the stored slot kept (a guard on the ENT-S-108
ownership re-read so a blanked pass claims nothing; the scan's own pick runs on to the gated scan and is cleared as
at HEAD; the Hammerhead's port-only fallback read of the slot gated with the pass), the five construction-time registrations (Leon, the
Princes, Boyfriend, Girlfriend) and the five goal-only hunters (CaterKiller, EnderKnight, EnderReaper, SeaViper,
Pointysaurus) as a live `canUse` predicate on the always-registered vanilla goal, and Godzilla's pass-local nulled
where BOSS-017 had cleared the stored target. Nine ENT-S-108 scans were already gated at HEAD and are pinned, not
edited; Pointysaurus :186-188 (no pass-local in the port; the retaliation pass is the shared melee goal) is carried
to T5 with the single-slot residual (a stored revenge target is still fought under the flag by the Nastysaurus and
TRex melee goal and the SeaViper's bite goal). `PlayNicelyGateParityTests` (own batch, 56
generated: flag off → the site acts, flag on → nothing of it, the flag restored in a finally; the Pointysaurus row
drives a plain `ServerPlayer`, the framework's mock answering `isCreative()` true in every mode — the first gate
run was red on that control, harness only). Refuted once — two blocking defects fixed as proposed (B1 the
Nastysaurus / TRex blanking made a scan-owned pick stick under the flag; B2 the Hammerhead's fallback re-admitted the
blanked grudge), three pins added, eight gaps recorded, ENT-S-116 filed from the sweep. Gate: red once on the
Pointysaurus control (harness), red once on the TEST-003 pair i127 / i165 with the same code green on the rerun
(timing; 2 red runs in 33 on record), green with the fixes.

T7: one refuter, upheld (two non-blocking test gaps recorded). T1: one refuter, refuted — B1 (Nastysaurus / TRex: the unconditional blanking of the pass copy made a scan-owned pick stick under the flag; now only a foreign occupant is blanked) and B2 (Hammerhead: the port-only fallback read of the slot re-admitted the blanked grudge; now gated with the pass) fixed as proposed with three added pins; eight non-blocking gaps recorded, ENT-S-116 filed from the sweep.

GATE (wave1): gate wave1f green: build legs (asset audit 0 errors, G1 PARITY 2 + 11 models, referenceGeometry, referenceRenderers, queenPartPlacementProbe) and runGameTestServer 'All 585 required tests passed' (517 + 12 PeacefulGateParityTests + 56 PlayNicelyGateParityTests). Earlier runs: wave1 red on the Pointysaurus control (the framework mock's isCreative() override, harness fixed); wave1b, wave1d, wave1e red only on default-batch TEST-003 tests (i127 acid, i165 chunk wait, i050 vortex drag) with every wave pin green — the i165 flake is the FORCED ticket centred at pad +8 while the checked cell is +10 (a chunk boundary between them in both failing layouts); wave1c green on the pre-refuter code.

## ENT-S-116 (2026-09-04) — PlayNicely griefing gates, fixed in classic

ENT-S-116 (2026-09-04, owner "ENT-S-116: go, one refuter") — the two 1.7.10 PlayNicely griefing gates outside
target selection restored. `EntityStinky.customServerAiStep` (:284-287): the flying Stinky's idle block-eat (the port's `eatFlowers`;
orig eats coal ore — ENT-S-119) now runs only while `!OreSpawnConfig.PLAY_NICELY.get()`, the 1-in-50 roll spent
first (orig Stinky.java:583).
`EntityGammaMetroid.customServerAiStep` (:130-136): the Gamma Metroid's stone-eat gains the same term between the
1-in-20 / 1-in-100 roll pair and the sitting test (orig GammaMetroid.java:435). Classic transcription, polarity
`PlayNicely == 0` ↔ `!PLAY_NICELY.get()`, nothing else changed; both files already imported OreSpawnConfig. New
`PlayNicelyGriefingGateTests` (own batch `playNicelyGriefingGates`): `s116_stinky_583_coal_ore_eat (renamed with ENT-S-119)` and
`s116_gammametroid_435_stone_eat` — the block eaten and the +1 heal with the flag off, neither with it on, the
flag (and the Metroid test's `mobGriefing`) restored in a finally. javac rc 0; the gradle gate is the
orchestrator's.

## TARGETING WAVE 2 (2026-09-04) — ledger batches fixed in classic

T3a → ENT-S-117 (the whole proactive hunt missing, L, two refuters): the four hunts transcribed from orig —
the Attack Squid's 1-in-10 pass acting on a transient pick (the Alien shape) over the 10x4x10 box with the orig
whitelist ladder (non-creative players, Girlfriend, Boyfriend, Zombie, Villager, Spider, CaveSpider, Lizard; Ghost /
GhostSkelly refused), the 1-in-5 buddy adoption and follow, the `wasshot` rule (a Kraken-launched squid takes every
living entity), the PlayNicely and creative gates and the orig sorter; the Water Dragon's `!PEACEFUL && nextInt(5)==1`
pass over the 14x4x14 box with the own-kind exclusion, the tamed → monsters-only rule, the baby-never-hunts rule, the
port's shared `isAttackableNonMob` helper (its membership T6's), the PlayNicely gate and the ENT-S-108 `scanPick`
hand-off into the single slot; the Dragon's continuous IMob channel as a `NearestAttackableTargetGoal<Monster>` at
target priority 1 with the ENT-S-115 live `canUse` and `HurtByTargetGoal` at 2 as orig ordered them; the Islands
vampire butterfly's hunt (`butterfly_type == 1`, the Islands dimension, the 1-in-10, !PEACEFUL, the 8x5x8 box and the
1.0 bite) as `ButterflyIslandsHuntGoal`, the else-branch of the flight retarget. Disclosed: the Water Dragon's
`setTarget` clears the scan's mark on a change of occupant only, with a hurt-by-marked-pick hand-off, because the
vanilla goal's same-entity re-assert would otherwise make a fresh pick sticky (the ENT-S-108 nine carry that
exposure — observation for T5); Monster stands in for IMob (Slime, MagmaCube, Ghast, EnderDragon uncovered — named
for the owner); the vanilla goal's poll cadence and sphere-in-box geometry versus 1.7.10's `EntityAITasks`; the
Islands positive path has no gametest pin (the GameTestServer has no datapack dimensions), the code paths pinned by
direct scan / filter / bite calls, an overworld negative and a roll-order count. `ProactiveHuntParityTests` (own
batch, 50 generated). Refuted twice: A upheld with records corrections (orig Mothra implements IMob; GodzillaHead a port-only grant;
the act cadence and the inherited canUse gate disclosed; the eye-level convention filed as ENT-S-120); B found one
blocking defect, fixed — the Water Dragon's mark cleared on a hit the hurt timer swallowed, making a player pick
sticky; the clear now requires the store, with a swallowed-hit pin (51 generated) — and "Kraken-launched" corrected
to the Squid Zooka in both trees.

T2 → ENT-S-118 (line-of-sight and feet-ray steps, M, one refuter): 19 port sites in 18 files, each at the orig
position in its filter chain — thirteen filters and the Irukandji / Skate inline picks gain the port's `canSee` idiom
`if (!this.getSensing().hasLineOfSight(target)) return false;` (AntRobot's stomp and hunt filters, Fairy, GiantRobot,
Lizard, PitchBlack, PurplePower, the five robots, SpiderDriver; orig `getEntitySenses().canSee(e)`), and Spyro,
Stinky, ThePrince and ThePrincess regain the second, feet-level block ray of orig's `canSeeTarget` (from 0.75 above
their own feet to the candidate's position, `level().clip` with `Block.OUTLINE` / `Fluid.NONE`, the mapping ENT-S-089
recorded for the Vortex's copy: liquids never stop the ray, every collidable block is tested on its selection
bounds), and'ed onto the scan predicate in orig's short-circuit order. Nothing else in those methods: the Ant Robot's
dircheck branch and the robots' shot cones stay T8's, the Lizard's AttackSquid grant and buddy side effect T6's /
T10's, the Purple Power's tamed-pet / royalty steps and Mothra-as-prey T6's. Disclosed: the eye-to-eye idiom clips
COLLIDER where 1.7.10's `canEntityBeSeen` used the selection-bounds ray — a port-wide approximation, not this batch's
— and five earlier COLLIDER ports of the feet helper outside these files (ThePrinceAdult, ThePrinceTeen, Kraken,
Brutalfly, Cockateil). `SightStepParityTests` (own batch, 23 generated: a stone wall midway breaks the eye line and
the filter refuses, razed and it accepts; a parapet the eyes clear but the feet ray crosses, `findSomethingToAttack`
null with it and the prey without; a short-grass parapet pinning the selection-bounds mapping). Refuted once, upheld (no code defect; two record corrections, three
observations filed as ENT-S-121..123).

T3a: two refuters (files touched under twenty, label L) — A (transcription fidelity) upheld with one records correction (orig Mothra implements IMob, so the Dragon's Monster channel no longer holds her) and five notes (GodzillaHead a port-only grant, the Luna Moth observation's home, the Water Dragon's act cadence, the butterfly goal's inherited canUse gate, the eye-level posY convention filed as ENT-S-120); B (slot semantics, goal mappings, tests) found one blocking defect, fixed (the Water Dragon's ownership mark cleared on a hit the hurt timer swallowed, making a player pick sticky; the clear now requires the store, a swallowed-hit pin added) and seven gaps recorded. T2: one refuter, upheld — no code defect, two record corrections, three observations filed (ENT-S-121..123).

GATE (wave2): gate wave2b green: build legs (asset audit 0 errors, G1 PARITY 2 + 11 models, referenceGeometry, referenceRenderers, queenPartPlacementProbe) and runGameTestServer 'All 661 required tests passed' (the 13:46 log; 585 + 2 PlayNicelyGriefingGateTests + 51 ProactiveHuntParityTests + 23 SightStepParityTests = 661). Earlier run wave2a: red only on the ENT-S-116 Stinky pin's precondition (the framework's relativePos mirrors both axes under Rotation.NONE; the test now derives the relative position from absolutePos(ZERO)); the i165 harness fix rode along green.

## CONVENTIONS AND HARNESS (2026-09-04) — the IMob convention, the Stinky's idle routine and flight ray, the default-batch isolation fixes

TEST-004 (2026-09-04, owner: "apply the ranked fixes that are pure isolation — flag restoration in finally, batch
separation, spacing. No widened waits, no retries. Before/after per fix in the record") — the harness slice's
isolation fixes, gametest sources only: `leaf_monster_prey_allowlist_play_nicely` moved to its own batch
(`playNicelyWindow`, the boss017 precedent) and its `PLAY_NICELY` prior restored on every exit path (it held the global
flag true for a 150-tick window in `defaultBatch:0` with no restore on failure — the hypothesised cause of i050's zero
pull); the tempt players of `boyfriend_tempt_panic_door` and `leon_ducky_tame_untame_tempt` removed from the player list
exactly once on every exit (a timeout left a creative mock in the list for the rest of the run); `big_bertha`'s
`BIG_BERTHA_PVP` prior restored on every exit and `checkVeinRatio`'s `LESS_ORE` restored in a finally (the caller
already had one; the helper was the unsafe unit). The exit hook is a `GameTestListener` on the test's own
`GameTestInfo`, which the framework fires on succeed, fail, throw and timeout (1.21.1 has no `addCleanup`). i050's
messages now print the flag and the game time (diagnostic only; the flag is not pinned inside i050). Order-model
consequence recorded: removing a default-batch method shifts the later tests by one — `chipmunk_apple_tame_dead_bush_release`
crosses 50→49 and `i138_inca_pyramid_content` 100→99 — and the new batch iterates before the default batch, so the grid
cells after the old slot are unchanged. F1 (the i127 assertion rework) and F5 (a `buildNow` RandomSource seam, main
code) are not pure isolation and stay presented. Refuted once, upheld: the exit hook fires exactly once on every terminal path (GameTestInfo.tick notifies its
listeners in the finishing tick; `testInfo` is public final), no wait, retry or assertion changed, the order model
re-run identically; seven non-blocking notes, three applied (the hook registered right after the mock player
joins, the hand-off flag set before the removal, a javadoc sentence on listener order), the rest recorded: a
throwing restore would abort the ticker loop (kept loud on purpose), the tempt player stays a CREATIVE list
member for its own tempt phase by design, F5 stays open.

IMob convention → ENT-S-124 (IMob mapped to Monster at six sites, S, ruled 2026-09-04): 1.7.10's `IMob` was an
interface (every EntityMob plus Slime / MagmaCube / Ghast / EnderDragon and orig's Mothra) and the six target tasks
that filtered on `IMob.mobSelector` over `EntityLiving.class` lists — the Dragon's channel (a) (ENT-S-117), Leon, the
two Princes, the Boyfriend and the Girlfriend — had been ported as `NearestAttackableTargetGoal<Monster>`, dropping
every hostile that is no Monster subclass; each is now `NearestAttackableTargetGoal<>(this, Mob.class, <interval as
before>, <mustSee as before>, <mustReach as before>, e -> e instanceof Enemy [&& the site's own predicate])`, the
ENT-S-115 live `canUse` gates and the Dragon's `getFollowDistance() → 16` untouched (the four 3-arg sites spelled out
as the constructor's own `10, mustSee, false`). The remaining orig IMob tests are identities (the Boyfriend's /
Girlfriend's Creeper-class tasks, ENT-A-054; `MyEntityAIAvoidEntity` on EntityMob lists) and stand. Disclosed: the Ghast
is refused by vanilla `Mob.canAttackType` (not special-cased); orig's Mothra is still no prey of the six (the port
Mothra has no `Enemy`, the ENT-S-117 disclosure stands); the Boyfriend / Girlfriend helper residuals and the T3c box /
cadence widening stay with the ledger. `IMobConventionTests` (own batch, 25 generated); the five PlayNicelyGate rows
and the five ProactiveHunt Dragon rows read `Mob.class`, the Dragon shape row asserts the Enemy selector. javac green;
the gametest run is the gate's. Not yet refuted. Refuted once, upheld: all six hunks the ruled form with HEAD's interval / mustSee / mustReach, the sweep exhaustive
(18 IMob tokens in orig: 8 imports, Mothra's identity, 3 identity uses, the 6 sites), the tests discriminating;
three records amendments applied (the Creeper clause of 1.7.10's `canAttackClass`, filed as ENT-S-127; the
1.21.1 Enemy hierarchy wording; the Boyfriend / Girlfriend untamed-or-sitting gate of orig
MyEntityAINearestAttackableTarget.java:44-52 absent on the port goal — a T3c residual, noted on the ledger row)
and four nits recorded.

ENT-S-119 / ENT-S-123 (2026-09-04, the T5/T6 wave; owner: "ENT-S-119 and 123 join the T5/T6 wave"): the flying Stinky's
1.7.10 idle tick restored in `EntityStinky.java` — the coal-ore hunt of orig Stinky.java:435-496 / :584-604 transcribed as
`scanIt` / `isCoalOre` / `eatCoalOre` (six-face shells 1, 2, 3, 4, 6, 8 around `((int) x, (int) y + 1, (int) z)`, the
nearest by squared distance from that origin, navigated to at 1.25, eaten under distSq 12 with the heal of 1.0 and
`PLAYER_BURP` at 0.5 / `nextFloat * 0.2 + 1.5`), replacing the port's `eatFlowers`; the ENT-S-116 gate line moved inside the
not-sitting block after `doMovement()` (orig :511 → :582-583); `doMovement` now runs for every activity — the 1-in-300
retarget roll guarded by activity 2 (orig :552), the 1-in-7 idle attack pass (orig :568-581) reached in activity 1, the
activity-1 return placed after it (orig :582-607); the fields `closest`, `tx`, `ty`, `tz` (orig :54-57). And the
flight-target ray of orig Stinky.java:640 / Spyro.java:647 — `&& canSeeTarget(newTarget.getX(), getY(), getZ())` on the
`isAir()` acceptance in `EntityStinky.doMovement` and `EntitySpyro.doMovement`. Pinned by the new `StinkyIdleParityTests`
(own batch `stinkyIdleParity`, 19 generated rows: the shell faces and the shell sequence, nearest-wins, the eat radius,
the heal, the burp through the PlayLevelSoundEvent seam, a flower ignored, nothing while sitting, the idle pass firing /
under PEACEFUL / missed with the bound order, the flight ray behind a one-block wall for both flyers) and by
`PlayNicelyGriefingGateTests`' Stinky row re-pointed at a coal ore (`s116_stinky_583_coal_ore_eat`). javac rc 0. Refuted once, upheld: `scanIt` a probe-for-probe transcription, the shell sequence 1, 2, 3, 4, 6, 8 traced, the
eat block, the gate's position and term order, the idle pass and the roll ledger exact in every state, the two flight
rays exact; one pin added from its test gap (the eat radius measured from the scan origin: coal at origin + (3, 1, 1),
distSq 11 from the origin and 14 from the Stinky's block — 19 pins); noted for the owner: `isCoalOre` accepts
`COAL_ORE` only under the port's one-block mapping, so a deep-cave Stinky never finds deepslate coal where 1.7.10's
single ore was everywhere (a `BlockTags.COAL_ORES` ruling); the flight-target rows 17/18 discriminate through the
port's write-after-test order, filed as ENT-S-126 with the boxed-in retarget cost; helper cites corrected.

REFUTERS: TEST-004: one refuter, upheld (the exit hook fires once on every terminal path; three ordering notes applied). ENT-S-124: one refuter, upheld (three records amendments; the Creeper clause of 1.7.10's canAttackClass filed as ENT-S-127). ENT-S-119 / 123: one refuter, upheld (one pin added for the eat radius base; deepslate coal noted for a mapping ruling; the flight-target retarget order filed as ENT-S-126).

GATE: gate phase1a green: build legs (asset audit 0 errors, G1 PARITY 2 + 11 models, referenceGeometry, referenceRenderers, queenPartPlacementProbe) and runGameTestServer 'All 705 required tests passed' (661 + 25 IMobConventionTests + 19 StinkyIdleParityTests; leaf_monster now in its own batch).

## TARGETING T9 (2026-09-04) — port-only targeting additions: MOD-032..036 and the classic removals

T9 → ENT-S-125 (port-only targeting additions, M, one refuter): sixteen rows split under the owner's ruling into four `[modern]` records — default on, the MOD-029 / MOD-031 precedent, each read through a `master && key` helper listed in the master's comment and javadoc — MOD-032 `godzillaSparesBossPeers` (Godzilla's Mothra / `isBigBoss` / `isRoyalty` refusals, read live at the filter; classic is the orig eight names, so the Nightmare, the Kraken and the royals are prey as in 1.7.10 — Mothra never was, the ignore screen refuses EntityButterfly first), MOD-033 `petsDefendOwner` (one record over the Phase 4E six plus the Prince Teen / Adult: the owner pair — and where the port added them the HurtBy and the tame monster hunt — registered only in modern, read once per `registerGoals`, a construction snapshot; live on Leon / Teen / Adult, registered-but-unconsumed on the Metroid, Spyro, Stinky, the Prince and the Princess; classic is orig's target tasks only), MOD-034 `pointysaurusStareAggro` and MOD-035 `cryolophosaurusRevengeChase` (the stare goal and the revenge-chase melee goal registered only in modern; orig registered neither) — one removal from both modes (the Mantis's two inert target goals: nothing read the slot, the HUD their comment named does not exist), five ledger re-ratings (the DungeonBeast / EmperorScorpion / EnderKnight / EnderReaper / HerculesBeetle "PEACEFUL gate" rows are the engine's `canAttack`, MATCH (engine, P6) like every other Monster row; no code, never a `canAttack` override) and one deliberate parity exception kept in both modes with no key (MOD-036: the Girlfriend's Valentine gates refuse Peaceful and creative players; 1.7.10 hunted them). Disclosed: Phase 4E's only documentation is its commit message (flagged for the owner); the Mantis removal is a judgment call on documented-but-false; Leon's tame predicate stays ungated until the ENT-S-124 refutation closes. `PortOnlyTargetingTests` (own batch, 15). javac rc 0; the gradle gate is the orchestrator's. Refuted once: the code and the 15 pins upheld (every classic branch verified exact against orig's task tables
and Godzilla's orig filter; every row fails with its line reverted); two non-code defects fixed — the pin count
(16 → 15) and the provenance note: the phase-1 records commit swept the MOD-033 gates of Leon, both Princes,
Spyro and Stinky into f2f47ae / fbc66be before `OreSpawnConfig.petsDefendOwner()` existed in HEAD, so f2f47ae,
fbc66be and 0b00b56 do not compile on their own until this batch's commit (no amend; the T9 commit follows
directly); eight gaps recorded: two for the owner (MOD-033's only documentation is commit 27b66a39's message;
the Mantis removed on "documented but false", the split's B), the same owner goals ungated on four species
outside the ledger (Hydrolisc and VelocityRaptor inert, Boyfriend and Girlfriend live — a MOD-033 residual
presented for a ruling), the ledger's §3 counts stale by five, the ENT-S-124 ledger lines riding in the same
docs commit, two cite / wording nits and the Godzilla :594 mojibake.

REFUTERS: T9: one refuter (17 files touched, label M) — code and pins upheld; the pin count and the non-compiling-range provenance note fixed in the records; MOD-033's residual on four species outside the ledger presented for a ruling.

GATE: gate t9a green: build legs (asset audit 0 errors, G1 PARITY 2 + 11 models, referenceGeometry, referenceRenderers, queenPartPlacementProbe) and runGameTestServer 'All 720 required tests passed' (705 + 15 PortOnlyTargetingTests). The MOD-033 gates of Leon, both Princes, Spyro and Stinky had been swept into the phase-1 commits f2f47ae / fbc66be by mistake before their gate; this run is their gate.

## ENT-S-121 (2026-09-04) — the 1.7.10 line-of-sight convention adopted port-wide

ENT-S-121 (2026-09-04, owner: "ENT-S-120 and 121: adopt the 1.7.10 convention port-wide, not per site"): the port's hunters
read 1.7.10's line-of-sight ray. 1.7.10's `canEntityBeSeen` was `rayTraceBlocks(eyes, eyes)` — every collidable block on its
selection bounds, liquids never stopping the ray (`ClipContext.Block.OUTLINE` / `Fluid.NONE`) — where vanilla's
`LivingEntity.hasLineOfSight` clips collision shapes, so a target behind short grass, a flower or a torch was seen by the
port and hidden in 1.7.10. One convention, one injection: new `LivingEntitySightMixin` (registered in the existing
`orespawn.mixins.json`, common list) cancels `hasLineOfSight` at HEAD for receivers registered under `orespawn` with
`OreSpawnSight.canSee` — vanilla's method with OUTLINE for COLLIDER, the same-level check, the eye-to-eye points and the
128-block cap kept. All 73 `getSensing().hasLineOfSight` / `hasLineOfSight` sites of the hunters and the vanilla goals on
OreSpawn mobs reach it through the same virtual call; no call site, `Sensing`'s per-tick cache (ENT-S-122) or vanilla mob
changes. The five feet-helper ports that clipped COLLIDER against the ENT-S-089 mapping (ThePrinceAdult / ThePrinceTeen
`canSeeSpot`, Kraken / EntityBrutalfly / Cockateil `canSeeTarget`) now clip OUTLINE (all five already `Fluid.NONE`). New
`LineOfSightConventionTests` (own batch `lineOfSightConvention`, 11 rows): the Fairy hidden from a Zombie behind short grass,
a torch and a poppy (each asserted collision-less, selection-bounded and on the eye line) and seen with them razed and
through water; a vanilla Zombie's ray through the same grass unchanged; `Fairy.findSomethingToAttack` refusing the Zombie
behind the grass through `Sensing`; the `Sensing` memo standing until `tick()`; the five feet helpers refused by a
short-grass parapet by reflection. javac rc 0; the gradle gate is the orchestrator's. Refuted twice: A (the mixin's descriptor, wiring, gate and the OUTLINE / Fluid.NONE mapping against the 1.7.10
bytecode) and B (the eleven rows, the five helpers, the interactions with goals, caches and the landed pins) both
upheld the work and both found the same single defect, fixed — the Ender Reaper's stare asks the PLAYER's line of
sight (orig EnderReaper.java:92 `player.canEntityBeSeen(this)`), a receiver the namespace gate cannot reach, now
`OreSpawnSight.canSee(player, this)` — plus records notes applied: 1.7.10 skipped fire where OUTLINE hits its
one-sixteenth slabs and tested a moving piston's block where OUTLINE is empty (residuals of the mapping, recorded,
not fixed); the fractional shape deltas of torches, grass, flowers, lily pads and end portals are the engine's
shapes; 1.7.10's only range bound was a 200-step cap that answered seen where vanilla's kept 128-block cap
answers unseen; a vanilla receiver's ray toward an OreSpawn mob stays vanilla's by the gate (1.7.10's was the
selection ray too — consistent with "OreSpawn's mobs read the convention"); the reverse-direction fence band
and the target-in-a-selection-box blindness disclosed above.

REFUTERS: ENT-S-121: two refuters (a mixin into vanilla) — both upheld the mixin, the five helpers and the eleven rows; both found the Ender Reaper's player-receiver site, fixed by hand; A's mapping residuals (fire, moving piston, the 200-step cap wording) recorded.

GATE: gate c121a green: build legs (asset audit 0 errors, G1 PARITY 2 + 11 models, referenceGeometry, referenceRenderers, queenPartPlacementProbe) and runGameTestServer 'All 731 required tests passed' (720 + 11 LineOfSightConventionTests = 731).

## TARGETING WAVE 2, T6 (2026-09-04) — exclusion and prey lists, the shared isAttackableNonMob membership

T6 → ENT-S-128 (exclusion and prey lists, M, one refuter): the shared `MyUtils.isAttackableNonMob` rewritten to orig's
thirteen terms in orig order (Monster for EntityMob; the port-only GodzillaHead and EnderDragon grants removed; Crab,
Mantis, Molenoid, TheKing, TheQueen and WaterDragon inherit it at their orig positions), the Dragonfly's whitelist with
the horse-friendly option read live, the Lizard's AttackSquid grant, the Purple Power's tamed-pet and royalty steps, the
Rat's five refusals, the Terrible Terror's nineteen spared kinds, the Triffid's seven spared kinds with orig's Monster
fallthrough, and the Boyfriend / Girlfriend goal predicate (tamed target, PigZombie, Enderman, Mothra, Creeper, Ghast in
orig order, composed with the ENT-S-124 form; the Valentine rule stays MOD-036's); the three ENT-S-108 residual lists
were present at HEAD and are pinned. `PreyListParityTests` (own batch, 161 generated). Refuted once, upheld; three records corrections (the Mothra-before-sight deferral, the revert claim narrowed, the kind
counts) and four observations for the ledger.

REFUTERS: T6: one refuter — upheld; three records corrections applied, four ledger observations.

GATE: gate t6a green: build legs (asset audit 0 errors, G1 PARITY 2 + 11 models, referenceGeometry, referenceRenderers, queenPartPlacementProbe) and runGameTestServer 'All 892 required tests passed' (731 + the PreyListParityTests rows).

## TARGETING WAVE 2, T5 (2026-09-04) — target set / release rules, the ownership convention, the ENT-S-122 sight memos

T5 → ENT-S-129 (target set and release rules, L, two refuters): one ownership convention for every hunter — the
change-only mark with the hurt hand-off, chosen on the six measured cases (a scan's pick leaving the box, a revenge
target dying in the cleanup re-assert window, a same-entity re-assert, a swallowed hit, PlayNicely flipping on with
the pick stored, a foreign revenge set) where the every-set clear turned a fresh pick sticky — applied to the ten
mark carriers, the mark retired where orig read no stored target (CaveFisher, DungeonBeast, Urchin) and added where
orig stored (Irukandji, Skate, SeaMonster); the forgets moved into the passes on orig's field at orig's cadence
(the revenge field for CreepingHorror, CannonFodder, Kyuubi, LeafMonster, Rat; Crab's missing 1-in-100; Emperor's
`== 0`; GiantRobot's and AntRobot's clear before the read), a per-species `RevengeGoal` whose `release()` ends the
task on a nulled target as 1.7.10's did, six inert revenge tasks unregistered, the Ender pair's daylight roll and
legacy hold, Robot2's port-only same-kind alert removed, the Boyfriend / Girlfriend hold distance, the Dragonfly's
one bite per pass with nothing retained, Mob-only revenge stores for AntRobot and Crab, and the per-preset
PlayNicely stand-down in the melee goal (the ENT-S-115 residual). ENT-S-122 reproduced: the sight memos of the
ridden Ant Robot and the active Nightmare, the latter with orig's :259-280 activity-0 branch. `TargetReleaseParityTests`
(own batch, 53 generated). Refuted twice: A (the ownership evidence, the release rules, the RevengeGoal shape) upheld with ten notes, five applied —
the CaterKiller and Hammerhead registration cites; the displaced revenge target rolled, dead-checked and re-taken when
visible while the scan's pick occupies the slot (Nastysaurus, TRex — orig rolled rt every pass); CaterKiller's 1-in-200
guarded to the revenge goal's own target; Robot2's forget backed by a RevengeGoal release — and five recorded (the same
re-assert defect on Robot3 / Robot4 / Robot5 / Leon / the Water Dragon's forget, ledger MATCH rows to re-rate; the
latest-attacker facet; the Ender hold's creative screen; the companions' 20 as the absent Creeper task; cite nits); B
(the memos, the Nightmare branch, the tests, the interactions) found the production code sound and three test
defects, fixed — the frozen Nightmare set on the ground before its path, the (b)/(c) window row pinned to the
running-only tick parity with the revenge goal asserted running, the Knight's daylight dice pinned before noon — plus
the tier-1 Nightmare so its scan stays inside the cell and the probe-leg wording.
Gate: the first run was red on seven of the 53 rows with every other test green — six were the rows' own harness
assumptions (a frozen robot's head never turns so its bite bearing needed facing; the framework mock player's 60-tick
spawn shield swallowed the pinned bites and the Irukandji's counter-damage; a hurt-timer overlap on the Robot2 row; the
15-block hold rows measured from the mob's centre where the box inflation admits centres up to 15 + both half-widths,
the Zombie moved to 15.8 with the geometry pinned from live half-widths) and one a transcription miss the row caught:
`Chipmunk.customServerAiStep` still cleared the attack target on its own 1-in-200 where orig Chipmunk.java:104-106
cleared the revenge memory (`setRevengeTarget(null)`), fixed to `setLastHurtByMob(null)` — the EntityCannonFodder shape;
green on the rerun.

REFUTERS: T5: two refuters (over twenty files) — A upheld (ten notes, five applied); B found three test defects, fixed, and the production code sound.

GATE: gate t5b green: build legs (asset audit 0 errors, G1 PARITY 2 + 11 models, referenceGeometry, referenceRenderers, queenPartPlacementProbe) and runGameTestServer 'All 945 required tests passed' (892 + 53 TargetReleaseParityTests); the first run was red on seven rows (six harness assumptions in the rows, one Chipmunk transcription miss the row caught), green on the rerun.

## BUG-041 COUNTS RECONCILED (2026-09-05, docs-only; the owner's housekeeping item 26)

Every number in play was right for its date and population; the one to state before the drop lands is the
manifest's. HEAD 2e21008, `tools/reference_model_proofs.json` unchanged since a3a4b62: 101 models, every one with a
`reference_source`; 89 carry a pin, every pin is MIRROR only, and the pins sum to 3,122 — equal, model for model, to
the live `.mirror()` call sites in those 89 sources (all the no-arg form; no `.mirror(false)` anywhere). Twelve are
exact: cliffracer, coin, enderreaper, ghost, island, islandtoo, kyuubi, mosquito, seaviper, skate, stinkbug, tshirt.
THE PORT-WIDE DROP TODAY IS 89 MODELS / 3,122 CALLS, NOT 81. 81 was the 5354420 count after stage 1 (77 mirror-only
plus CaterKiller, Elevator, SeaViper and StinkBug; 3,101 calls); it became 89 / 3,122 when slice A rebuilt the
manifest to 101 at 6b10b14 (ten newcomers pinned — ant 20, cricket 11, gammametroid 21, leafmonster 5, rat 12,
rotator 3, rubberducky 8, terribleterror 21, vortex 1, wormsmall 3 = 105 calls) and the ENT-S-091 regenerations
retired StinkBug (50, 6b10b14) and SeaViper (34, a3a4b62): 81 − 2 + 10 = 89; 3,101 − 50 − 34 + 105 = 3,122. The
survey's 82 / 78 are its own 87-pair population (boxes 3,167 / 3,047); 87 was the manifest's size at 5354420 and 84
its pins then (87 − 3 exact). Dropped so far: 150 calls in three models (EnderReaper 66 at 5354420, stage 1;
StinkBug 50; SeaViper 34). Reference side: 3,542 `field_78809_i = true` stores in 108 of 109 reference models, none
before its addBox — 3,124 in the 89 pinned models' references (ModelGazelle 34 vs pin 33, ModelRobot4 56 vs pin 55:
one part each the port never mirrored, nothing to drop), the rest in the exact models, ModelButterfly, the item
models and ModelTheQueen.

OUTSIDE THE MANIFEST WITH THE SAME DEFECT — the owner's scope call: `ButterflyModel` (10 calls; skipped at 6b10b14
as a shared parameterised model serving four renderers; its original carries 10 inert stores) and the seven item
models under `client/model` (BattleAxe 15, Bertha 12, Chainsaw 7, Hammy 33, QueenBattleAxe 9, Slice 14,
SquidZooka 12 = 102; never surveyed — `reference_survey_manifest.py:12` scans entity/client only; their originals
carry 103 inert stores, Chainsaw's eighth being the `tooth` part the port lacks, a geometry note). Port-wide
including both: 97 models / 3,234 calls. Not BUG-041: Girlfriend / Boyfriend (2 + 2 HumanoidModel arm / leg
mirrors; 1.7.10 drew both with vanilla `ModelBiped`, ClientProxyOreSpawn.java:386-387) and the three head rigs
(1 each, port-only; 1.7.10 registered `RenderXHead(null, 0, 0)`).

Two record lines were wrong or forward-looking when written and stand corrected here, not rewritten: the
2026-09-02 gate entry's "every port entity model that pairs with a 1.7.10 model (87)" — the survey generator's 87
pairs; 14 more paired models joined at 6b10b14 and ButterflyModel pairs by name but is deliberately outside — and
the AUDIT BUG-041 entry's A/B and Resolution lines (66 calls "carried", "declared for all 78"), updated in place as
the register's status. Measured by a read-only lane (`count_mirror.py`, `ref_mirror.py`, `pair_census.py` over the
manifest at 5354420 / 6b10b14 / 4efdfd7 / a3a4b62 and HEAD); the 3,542 / 0-live reference stores cross-checked by
plain grep.

## ENT-S-120 PREMISE VERIFIED (2026-09-05) — the server-side player posY was the feet; the sweep is held

Before the ruled `OrigPos` sweep started, the census's convention ("a 1.7.10 player's posY was its eye level,
yOffset 1.62") was checked against Mojang's 1.7.10 client jar (sha1 e80d9b3b…, the BUG-041 copy) under law 11:
`EntityPlayer.<init>` stores 1.62 into Entity's yOffset, but `EntityPlayerMP.<init>` — the server player — stores
0 into yOffset and stepHeight, and `Entity.setPosition` places the bounding box at posY − yOffset. On the server a
player's posY was its feet, the same frame as the port's getY(); every census site runs server-side, so the sweep
as ruled (night set items 15–22) would have moved the port 1.62 blocks away from 1.7.10. Report:
`phase_g_reports/ents120_premise_2026-09-05.md`; the register entry carries the evidence bullet. Nothing was coded
on the old reading (no helper, no site, no PN entry); the ENT-S-120 lane is held for the owner's amendment. What
remains of the finding is client-side code with a player operand, if any — a separate small census.

Closed (owner, 2026-09-05): under law 11 the server-side player's posY was the feet, so no listed site diverges — the
night set's items 15–22 and the evening set's 5–10 are withdrawn by the closure (addendum item 22's supersession, the
closure line; item 23 (2)); OrigPos is never written; the census stays in the record with the premise note at its head;
PN-020 and PN-021 stand; no client-side census now.

## ITEM-070 (2026-09-05) — REPORT: the Chainsaw sweep's sight ray (no code change)

ITEM-070 → REPORT: the ENT-S-121 refuter's note filed. Chainsaw.java:137 gates the LEFT-CLICK SWEEP (orig UltimateSword.java:163-174 → :195 → :198-247; the felling box :351-371 tests no sight in either tree) with vanilla `LivingEntity.hasLineOfSight` — an exact eye-to-eye COLLIDER clip, fluids ignored, the Player receiver untouched by the ENT-S-121 mixin's namespace gate — where 1.7.10 walked ten samples from the player's feet + 1.4 (the server's `EntityPlayerMP.posY`; the ENT-S-120 census's client reading puts it at 3.02) to the target's mid-body and passed only `Blocks.air`. Compared on thirteen felling-site cases: the same on a clear line, leaves, logs, glass, fences and a target on open ground; different — the port sweeps what 1.7.10 spared — for a mob standing in grass / flowers / crops / a cobweb / snow / water (the tenth sample reads the target's own cell), a wall torch or 2-block plant between (any target), low mobs behind ground plants / one-layer snow / carpet / a bottom slab, and the player standing in tall ferns or swimming (1.7.10's sweep dead, the port's live); different the other way for trunk and canopy corner grazes and the 1.0-1.5 fence band (1.7.10 more permissive), and quadrant-dependent through the `(int)` column shift (BUG-027's ruled-faithful quirk). No MOD record (MOD-016 is the felling box; ITEM-037 closed the sweep without its ray; targeting_survey :1107's MATCH and the census's :402 AC read the geometry only): a classic parity bug on a signature weapon's combat reach, present in both modes. Two shapes for the owner: A — transcribe the walk as a helper (float steps, `(int)` casts per BUG-027 / MOD-024, `isAir()`, origin feet + 1.4 or eye + 1.4 per the owner's posY reading) with an eight-row pin batch, ≈45 lines, no engine hook; B — keep the vanilla ray under a MOD record, unconditional (B1, a record and a KNOWN_ISSUES line) or as a `[modern]` key with A behind classic (B2). Awaiting the ruling; nothing applied.

## MOD-033 EXTENSION (2026-09-05) — the owner / tame goals gated on four more companions and Leon's tame rule

MOD-033 extension (2026-09-05; the owner's ruling of 2026-09-04 on the T9 residual, ENT-S-124's refutation closed): the same `petsDefendOwner` key now reaches the four companions outside the targeting ledger and Leon's tame rule. EntityHydrolisc (:82-86) and VelocityRaptor (:77-81): all three Phase 4E target goals (OwnerHurtBy @1, OwnerHurt @2, HurtBy @3; commit 27b66a39) under `if (OreSpawnConfig.petsDefendOwner())`, the helper read once in `registerGoals` (a construction snapshot, the Spyro shape) — orig Hydrolisc.java:51-60 / VelocityRaptor.java:53-62 registered tasks only, no targetTasks, so classic registers nothing; inert either way (only the 1-in-200 `setTarget(null)`, :114 / :140, touches the slot). Boyfriend (:154-157) and Girlfriend (:218-221): the owner pair (@1/@2, @3/@4) under the same gate (the pair came with commit 2b0c2cd, 2026-04-06, no stated intent; gated on the ruling) — orig Boyfriend.java:138-147 / Girlfriend.java:161-174 register no owner task and no EntityAIHurtByTarget; the IMob hunt, the two Jealousy goals and the Girlfriend's two Valentine goals stay in both modes at their port priorities; live: the held-weapon melee (Boyfriend.java:337 / Girlfriend.java:376), the RangedAttackGoal @4 and Boyfriend's `getAmbientSound` (:709, a reader in both trees, orig :776-779) read the slot and Girlfriend :611 writes it, so a classic tamed Boyfriend / Girlfriend no longer defends its owner — 1.7.10's behaviour. EntityLeon (:165-182): the helper read ONCE into `final boolean petsDefendOwner`, the owner pair under it and the hunt's selector built from the same snapshot as a `Predicate<LivingEntity>` local — modern `e -> e instanceof Enemy && (!this.isTame() || this.getTarget() == null)`, classic `e -> e instanceof Enemy` (orig Leon.java:93: an EntityLiving.class list through IMob.mobSelector, no further selector, no tame term; the ENT-S-124 form) — never read live. Camarasaurus verified: the port registers no target goals (:66-81, tasks only, as orig Camarasaurus.java:53-62), nothing to gate. OreSpawnConfig: the key's comment and the helper's javadoc name the four species and the tame rule. Pins: PortOnlyTargetingTests `mod033_companions_defend_owner_modern_on / _key_off / _master_off` (the four spawned with their goals AFTER each flip, the whole target selector described as `priority:Goal<targetType>` and compared sorted — classic exactly the 1.7.10 set, the Hydrolisc's and the Raptor's empty; modern that plus the port-only owner goals) and `mod033_leon_tame_hunt_rule_modern_on / _key_off / _master_off` (a Leon spawned after the flip at the IMobConventionTests spots under the ForcedRoll seam, PlayNicely off, tamed and holding a frozen 1000-HP Zombie 8 blocks east as its target: the hunt's `canUse()` refuses her in modern — and takes her once the slot is emptied, the control — and takes her in classic, the pick read back) — six rows, the batch 15 → 21; IMobConventionTests' Leon tame-rule row asserts the modern default as a precondition and cites MOD-033. Records: MOD-033 (live / inert, classic, the tame-rule and extension bullets, the switch count 8 → 12 sites, the pins, status; the heading now names the Boyfriend / Girlfriend pair's own provenance), KNOWN_ISSUES's `petsDefendOwner` sentence (:210-213); README's line names no species and needed no change. javac rc 0 over the eight compilation units (six main, two gametest); the gradle gate is the orchestrator's. Not touched, for the owner: Leon's port priorities put HurtBy @3 ahead of the hunt @4 where orig Leon.java:92-95 had the hunt @1 ahead of HurtBy @2, and the Girlfriend's hunt sits @5 where orig :167 had it @3 — pre-existing positions outside this ruling, carried as they were — filed as ENT-S-130 (the refuter: both change classic behaviour under the priority pre-emption rule, the ENT-S-117 row-7 precedent).

GATE (m33f1): gate m33f1 green (first and only run, 00:38-00:40): build legs (asset audit 0 errors / 0 advisories / 4 acknowledged, G1 PARITY 2 + 11 models with the checked-in proofs verified, drift check exit 0) and runGameTestServer 'All 951 required tests passed' (945 + the 6 new PortOnlyTargetingTests rows; F1 adds none)

Refuted once (10 files touched, label M): the gating code and the six pins upheld — every classic set verified exact against orig's targetTasks tables, compile rc 0, flags and spawns restored on every path, no vacuous pass; three record defects fixed before the gate: the Boyfriend / Girlfriend pair's provenance (commit 2b0c2cd, 2026-04-06, not Phase 4E's 27b66a39 — the comments, the pin's message and the MOD-033 heading corrected), the MODERNIZATION_NOTES preamble hunk attributed (the orchestrator's housekeeping item 27, committed with these records, not the lane's), and a placeholder pin name (`s124_17_leon_slime_tame_rule`); the lane's draft cites corrected in this section (OreSpawnConfig javadoc :618-630; ENT-S-124 at AUDIT :8067 / :8090; the slot readers); the IMobConventionTests precondition touch kept (the row's mode dependence named instead of silent); the two priority questions answered and filed as ENT-S-130.

## HARNESS F1 (2026-09-05) — i127 reworked test-only, as ruled

F1 (i127) — `i127_tower_centre_rooms_spawn_jumpy_bug` (StructureTestsA.java:1100-1213; the behaviour half :1138-1212), applied 2026-09-05 under the 2026-09-04 ruling. The 200-tick negative read "no orespawn:acid anywhere in the bounds" while the trooper's own 1-in-5 × 1-in-30 summon (EntityTrooperBug.java:237-249, orig TrooperBug.java:441-443) was dropping a live Spit Bug beside it; the minion's SpitBugAcidAttackGoal (EntitySpitBug.java:86) fires Acid the negative could not tell from the trooper's — the mechanism proven from source, the attribution of the three logged runs (3/37 ≈ 8%, three grid cells, no layout dependence; harness slice 2026-09-04 §2) inferred. Test-only rework, nothing loosened: (1) a structural pin at t = 0 — the trooper is no RangedAttackMob and its goal table holds no SpitBugAcidAttackGoal / RangedAttackGoal / RangedBowAttackGoal / RangedCrossbowAttackGoal (EntityTrooperBug.java:82-97); (2) the negative scoped to the trooper — each tick every Acid in bounds must satisfy `getOwner() != bug` (an Acid the minion fires carries the minion as owner, Acid.java:17-18 → LaserBall.java:61-62 → ThrowableProjectile.setOwner; after the cull the lookup answers null, which passes), the failure message carrying the tick, every acid's owner, the Spit Bug count and the cow's health; (3) every EntitySpitBug in bounds discarded each tick, on the tick it appears (MinecraftServer.tickChildren runs ServerLevel.tick before GameTestTicker.tick, and EntityTickList never ticks an entity added during the iteration; the earliest minion Acid is three ticks out) — the count logged, not asserted, since P(no summon in 200 ticks) ≈ 26%; (4) the cow holds max health 1000 (the EntityLogicTestsA :118-121 idiom; ≤ 21 full hits in the window) so the target never drops and the window's cadence stays the one the test intends. Same 200 ticks, same timeoutTicks 400, same batch, positives byte-identical. Before: ≈ 8% intrinsic flake on the minion's acid. After: fails only when the trooper itself owns an Acid. javac rc 0.

GATE (m33f1): gate m33f1 green (first and only run, 00:38-00:40): build legs (asset audit 0 errors / 0 advisories / 4 acknowledged, G1 PARITY 2 + 11 models with the checked-in proofs verified, drift check exit 0) and runGameTestServer 'All 951 required tests passed' (945 + the 6 new PortOnlyTargetingTests rows; F1 adds none)

Refuted once (one file), upheld: every assertion, the window, the timeout, the batch and the entity population verified (the owner-scope chain through ThrowableProjectile.setOwner and EntityLookup.remove; the tick-order claim from MinecraftServer.tickChildren offsets 150 / 309 and EntityTickList's ensureActiveIsNotIterated; no same-tick fire path); two comment-only defects fixed (stale jump-seam cites :147-162 / :54-55; "one Acid per tick" → per 1-in-5 cadence tick, BugMeleeAttackGoal.java:159); the lane's seven deviations all kept; the stale jump cites in the untouched positives' messages (:1147 / :1152 / :1155) left as they are under "positives untouched" — the owner's call.

## TARGETING WAVE 2, T5b (2026-09-05) — the re-assert rows, the flyers' write-before-test, the vanilla-task Creeper convention, deepslate coal

T5b → ENT-S-131 (the ENT-S-129 refuter A's re-rated MATCH rows, owner's item 8), ENT-S-126 (item 9), ENT-S-127 (item 10) and
PN-021 (item 14), one lane, one refuter.

ENT-S-131 — five forgets that nulled the attack target where 1.7.10's `EntityAIHurtByTarget` ended (`EntityAITarget
.continueExecuting` on null) and that vanilla's `TargetGoal.canContinueToUse` re-asserted from the goal's own memory a tick
later: the T5 shape — a per-species private `RevengeGoal extends HurtByTargetGoal` with `release()` kept in a field, the forget
calling `setTarget(null)` + `revengeGoal.release()` on the orig dice at the orig position, nothing else moved —
  - Robot3: orig Robot3.java:242-244 (1-in-50 `== 1` inside the `reload_ticker == 0` pass; task :58) → Robot3.java:148, the goal
    :91-111 (was a plain `HurtByTargetGoal` :89 with the forget at :127);
  - Robot4: orig Robot4.java:282-284 (inside `reload_ticker == 0 && nextInt(8) == 1`; task :61) → Robot4.java:196, the goal
    :91-111 (was :89 / :175);
  - Robot5: orig Robot5.java:214-216 (inside `reload_ticker == 0`; task :56) → Robot5.java:135, the goal :78-98 (was :76 / :114);
  - Leon: orig Leon.java:340-342 (1-in-200 `== 1` every AI tick; task :95 — the :93 hunt task ended on the same null and the
    port's hunt goal holds no memory, so it still does) → EntityLeon.java:568-571, the goal :170-171 / :194-212 (was :170 / :545;
    the MOD-033 registerGoals, its key-gated predicate and the priorities untouched — ENT-S-130 stands);
  - Water Dragon: orig WaterDragon.java:594-596 (1-in-200 `== 0` every AI tick ahead of the :597 hunt pass, target or none;
    task :76) → WaterDragon.java:367-370, the goal :141-161 (was the melee goal's forget: `Presets.waterDragon` 200 rolled in
    BugMeleeAttackGoal.tick only while engaged → 0, ai/DinosaurMeleeAttackGoal.java:43).
  Disclosed, not applied: orig Robot2..5 roll the 1-in-50 clear BEFORE the pass reads the target (Robot2.java:281-284,
  Robot3.java:242-245, Robot4.java:282-285, Robot5.java:214-217); the port — T5's Robot2 included — reads first, so a forgotten
  target is engaged once more in the forgetting pass when the scan would not re-pick it (the GiantRobot :243 / AntRobot :109
  shape); the ledger's robot rows rate the pass MATCH — presented for wave 3.
ENT-S-126 — orig Stinky.java:638 / Spyro.java:645 wrote the candidate to the flight target BEFORE the air-and-ray test →
EntityStinky.doMovement :483 / EntitySpyro.doMovement :450, the test (:484-488 / :451-455) now only setting `found`; orig
:548-551 / :562-565's `do_new = true` on the null-init → EntityStinky :414-419 / EntitySpyro :366-371 (unreachable in both trees).
A boxed-in flyer steers at the last refused candidate and casts no ray until the :552 / :572 retarget clock, where HEAD re-ran
fifty rays every tick. Disclosed, not applied: orig rolled zdir before xdir (both ports xdir first), and EntityStinky's pick
lacks orig Stinky.java:617-627's owner branches (`nextInt(4) + 6` / `nextInt(8)`) that EntitySpyro carries.
ENT-S-127 — new util/OrigTargets.java, `vanillaTaskPrey(LivingEntity)` = `Enemy && !Creeper` (javadoc: orig
`EntityLiving.canAttackClass`, `cls != EntityCreeper.class && cls != EntityGhast.class`, asked by `EntityAITarget
.isSuitableTarget` ahead of every selector; the Ghast half vanilla's `Mob.canAttackType`) at the four vanilla-task goals:
Dragon.java:158, ThePrinceAdult.java:159, ThePrinceTeen.java:170 (orig Dragon.java:116, ThePrinceAdult.java:113,
ThePrinceTeen.java:117), EntityLeon.java:182-184 composed with the MOD-033 tame rule (orig Leon.java:93); the `Enemy` imports
dropped where unread. Custom scans and the Boyfriend / Girlfriend goals (orig MyEntityAITarget.java:111 granted the Creeper)
untouched.
PN-021 — EntityStinky.isCoalOre :404-412 `is(Blocks.COAL_ORE)` → `is(BlockTags.COAL_ORES)` (orig Stinky.java:443-487, the one
coal ore); both modes; PARITY_NOTES PN-021.
Pins, +12 (951 → 963): `TargetReleaseParityTests` 53 → 58 — `s131_01_robot3_242_forget_in_pass_final`,
`s131_02_robot4_282_forget_in_pass_final`, `s131_03_robot5_214_forget_in_pass_final`, `s131_04_leon_340_forget_final`,
`s131_05_waterdragon_594_forget_in_step_not_goal_tick` (the T5 rows' shape: the revenge goal on a stored attacker, the forget
pinned quiet as the control and then to fire, the slot empty and `canContinueToUse` false — the next tick's cleanup does not
refill it; the Water Dragon's melee goal ticked with the old 200 keeping the slot); `IMobConventionTests` 25 → 29 —
`s127_01_dragon_creeper`, `s127_02_leon_creeper`, `s127_03_theprinceadult_creeper`, `s127_04_theprinceteen_creeper` (a Creeper
refused beside each site's Zombie control); `StinkyIdleParityTests` 19 → 22 — rows 17 / 18 re-based on the ray-refusal count
(fifty y rolls with the wall, the flight target on the refused candidate, one roll razed), `s126_20_stinky_638_boxed_in_last_
candidate_no_repick`, `s126_21_spyro_645_boxed_in_last_candidate_no_repick` (fifty refusals → the last candidate; the clock
quiet → zero tries; the clock fired → fifty again), `pn021_22_stinky_443_deepslate_coal_ore_eat` (the row-1 probe with a deepslate
coal ore, eaten). Every row synchronous; the wall razed, flags restored and spawns discarded in a finally.
javac rc 0 (`--release 21 -proc:none -implicit:none`, the g1 runtime classpath + build/classes) over the 15 compilation units:
OrigTargets, Robot3, Robot4, Robot5, EntityLeon, WaterDragon, DinosaurMeleeAttackGoal, EntityStinky, EntitySpyro, Dragon,
ThePrinceAdult, ThePrinceTeen, TargetReleaseParityTests, StinkyIdleParityTests, IMobConventionTests. The gametest run is the
gate's (expected `All 963 required tests passed`).

GATE (t5b_t8): gate t5b_t8 green (first and only run, 02:01-02:03): build legs (asset audit 0 errors / 0 advisories / 4 acknowledged, G1 PARITY 2 + 11 models with the checked-in proofs verified, drift check exit 0) and runGameTestServer 'All 1023 required tests passed' (951 + T5b's 12 + T8's 60)

Refuted once (15 files touched, label M): upheld — no production or test defect; the forgets' dice, polarity and positions verified against orig at all five sites, release() ends the goal as orig's task ended, the flyers' write-before-test order and the unchanged roll sequence verified, vanillaTaskPrey equivalent to orig's exact-class test in scope (no Creeper subclass exists), the six coal shells through the one callee; nine record corrections applied to the drafts (the Stinky's null-init branch IS reached on the first server tick in both trees, redundant with the 2.1 test; cites :472-473 / :622-630 / :629-637 / :617-627 / :485-488 / :452-455; the ruling item numbers; the changelog's 'tamed'; the section heading; a class javadoc that contradicted its row) and the ENT-S-127 evidence cites refreshed; two observations filed — ENT-S-133 (the four robots' clear-before-read) and ENT-S-134 (the Stinky's owner flight branches) — plus a MOD-033 disclosure (Leon's forget and the modern owner goals) and a hygiene note (ProactiveHuntParityTests :884 / :890 / :914 consume an unpinned nextInt(200) ahead of the Water Dragon's pass; harmless, optionally pinned).

## TARGETING WAVE 3, T8 (2026-09-05) — creative gates: the ENT-S-107 mapping at the vanilla-goal sites, the Brutalfly / Mothra creative fall-through, the Ender pair's same-tick creative drop and shadowing, the Ender Knight's pumpkin-stare gate

- **ENT-S-132 — FIXED (2026-09-05, wave 3).** Targeting ledger batch T8 (`phase_g_reports/targeting_survey_2026-09-04.md` §T8: 17 rows, 16 blocks; row 17, MyValentineTarget, RECORDED under MOD-036, skipped), plus the 2026-09-04 ruling's item 11 (the Ender Knight's dropped stare ray, orig EnderKnight.java:92, fixed with T8). Seven port files, every site at the orig position with the orig polarity and the ENT-S-107 mapping (orig `capabilities.isCreativeMode` / the legacy loop's `isCreative` = `Abilities.instabuild`, never vanilla's `invulnerable` / `canBeSeenAsEnemy`); T3b / T3c / T4 keep their boxes, cadences and sorters; the Ender pair's revenge pick (`HurtByTargetGoal.canUse` → vanilla `canAttack`, `invulnerable`) overridden to the same mapping on the refuter's finding (D1), four pins:
  - EntityCaterKiller.java:105-125 (orig CaterKiller.java:546-549), SeaViper.java:101-121 (orig SeaViper.java:517-520), Pointysaurus.java:93-114 (orig Pointysaurus.java:242-245; the ENT-S-106 ignore screen composed ahead, the registration reduced to the 3-arg form so the predicate lives once): the vanilla `NearestAttackableTargetGoal<Player>` keeps its class, registration values and box; its `TargetingConditions` are rebuilt in an instance initializer of the ENT-S-115 anonymous subclass — `forNonCombat().range(getFollowDistance()).selector(e -> !(e instanceof Player p && p.getAbilities().instabuild))` — the same range, invisibility, alive / non-spectator and sight screens as `forCombat`, minus its three terms with no orig line here (`canAttack`: the Peaceful player refusal, inert on a Monster the engine despawns, and `canBeSeenAsEnemy`'s `invulnerable`; `canAttackType`; `isAlliedTo`).
  - EntityBrutalfly.java:207-218 (orig Brutalfly.java:216-226): the strafe in orig's nested shape — creative? → `target = null` (:224-226) so the 1-in-3 mob hunt (:228) runs; sight (:218) inside the non-creative arm, so an unseen survival nearest still shadows the hunt.
  - Mothra.java:393-401 (orig Mothra.java:225-236): the same nesting, with the :227 sight step (`getSensing().hasLineOfSight`, the ENT-S-121 ray) restored ahead of the mark — the T2 cross-reference closed here.
  - EnderKnight.java:69-102 / EnderReaper.java:66-99 (orig td.bq :155-182 with :65-67): the player goal's conditions rebuilt non-combat with the stare test as the selector and NO creative term (orig's pick had none); the same-tick drop after the pick in `canUse` (`if (this.target instanceof Player p && p.getAbilities().instabuild) { this.target = null; return false; }`) — a creative starer nearer than a survival one is picked and nulled, nothing hunted that pass, the survival starer shadowed (the Kraken KT-A pattern); `holdsLegacyTarget` (EnderKnight.java:115-119, EnderReaper.java:112-116) reads alive, not spectator (kept from HEAD; no 1.7.10 state), not `instabuild` — for HEAD's `canAttack`. The Reaper's registration goes 6-arg → 3-arg with the selector in the initializer (identical 10 / true / false).
  - EnderKnight.java:121-140 (orig EnderKnight.java:83-93): `shouldAttackPlayer(Player)` restored as the Reaper's twin — carved pumpkin (:84-87), the mid-height cone (:88-91), the :92 player-side ray through `OreSpawnSight.canSee(player, this)` (ENT-S-121 refuter B's routing) — wired as the goal's selector.
  - EnderKnight.java:58-62 / EnderReaper.java:55-59 (orig `EntityMob.attackEntityFrom` with td.bq :155-182; the T8 refuter's D1): the revenge goal's `canAttack(LivingEntity, TargetingConditions)` overridden — `t != null && t.isAlive() && !t.isSpectator() && !(t instanceof Player p && p.getAbilities().instabuild)` — where vanilla's `HurtByTargetGoal.canUse` reached `TargetGoal.canAttack` with the private `HURT_BY_TARGETING` (`forCombat`: `Player.canBeSeenAsEnemy` = `!abilities.invulnerable`): an invulnerable survival attacker is stored and hunted as in 1.7.10, a creative one refused at the pick; the `canContinueToUse` hold untouched.
  - CaveFisher.java:191, DungeonBeast.java:183, EntityEmperorScorpion.java:345, EntityHerculesBeetle.java:267, Nastysaurus.java:262, EntitySpitBug.java:301, TRex.java:245, EntityTrooperBug.java:335: present at HEAD (ENT-S-108 — `instabuild` at the orig position; the survey's port cites name the goals ENT-S-108 removed) — no edit, pinned.
- **Pins:** new `CreativeGateParityTests` (own batch `creativeGateParity`, TEST-003; `@GameTestGenerator`, 60 rows `creativegateparitytests.s132_NN_<site>_<case>`): the ENT-S-107 triple per site (creative refused / invulnerable-survival TAKEN — the discriminating row vanilla's `forCombat` fails / survival taken) — 24 filter rows by reflection, 9 goal rows through `canUse` with the pick read back, 6 Ender stare-goal rows (the creative row shows the pick's conditions admit the starer and the goal's target reads null: picked and nulled the same tick); 4 shadowing rows (a creative starer 8 blocks off shadows a survival starer 12 blocks off; alone, the survival starer is taken); 4 hold rows (creative dropped, invulnerable survival kept, through both target goals' `canContinueToUse`); 4 Knight stare rows (pumpkin, look-away, a stare through a stone wall with the cone asserted satisfied and `OreSpawnSight.canSee(player, knight)` false — razed, taken; a clear stare taken; each refusal by `shouldAttackPlayer` and the goal alike, with a within-row control); the Brutalfly (2) and Mothra (3) AI steps driven once under a scripted per-call random (a creative nearest falls through to the zombie's mark with every roll drawn; a seen survival nearest is strafed with the mob-hunt gate never drawn; Mothra's unseen survival nearest leaves the parked target and the gate undrawn); 4 revenge-pick rows (D1: a creative attacker refused with the slot empty, an invulnerable-survival attacker taken and stored by `start()` — the row vanilla's `canAttack` fails — the attacker primed through `setLastHurtByMob` with the hunter's `tickCount` raised past the goal's initial timestamp, TargetReleaseParityTests' idiom). `PlayNicelyGateParityTests` site 11 (the Knight's GoalProbe) now stares at the mid-height like the Reaper's. Synchronous, flags asserted not flipped, walls / players / spawns restored in a finally, survival players plain ServerPlayers (the mock's `isCreative()` is hardcoded true), no spawn shield cleared (no hit pinned). javac rc 0; the gametest run is the gate's.
- **Disclosed:** the three rebuilt goals' vanilla HOLD (`TargetGoal.canContinueToUse` → `canAttack`) still reads `invulnerable` — an invulnerable survival player is picked by orig's rule and released the next tick by vanilla's (T3b / T3c's custom scan or a T5 hold rule closes it); the alliance term `forNonCombat` drops had no orig counterpart; the selector runs ahead of the conditions' sight step where orig tested creative after sight (outcome-equivalent); `holdsLegacyTarget` keeps HEAD's spectator refusal; orig's td.bq read the game type — the ENT-S-107 mapping applied as ruled; the Ender pair's nearest-then-filter scan set stays T3b's (a creative non-starer does not shadow in the port).

GATE (t5b_t8): gate t5b_t8 green (first and only run, 02:01-02:03): build legs (asset audit 0 errors / 0 advisories / 4 acknowledged, G1 PARITY 2 + 11 models with the checked-in proofs verified, drift check exit 0) and runGameTestServer 'All 1023 required tests passed' (951 + T5b's 12 + T8's 60)

Refuted once (9 files touched, label M): upheld on the code as cited — all 16 rows transcribe orig line by line (the selectors, the Brutalfly / Mothra nesting and draw order, the Ender pair's same-tick drop and hold, the Knight's shouldAttackPlayer byte-equivalent to the Reaper's), the eight ENT-S-108 sites verified instabuild at orig's position with no vanilla goal left; one adjacent code gap fixed as the refuter proposed (D1: the Ender pair's revenge PICK read vanilla canAttack, i.e. invulnerable; canAttack(LivingEntity, TargetingConditions) overridden on both anonymous revenge goals to the ENT-S-107 mapping, four pins — the orchestrator's diff review and this gate its proof); three record corrections applied (the changelog's overclaims — the CaterKiller / SeaViper / Pointysaurus pick is released by the untouched vanilla hold next pass, a T3b / T5 matter; the shadowing needs a staring nearer player, the nearest-of-any-mode shadowing is T3b's; the hold's team term disclosed beside the alliance term; row 7's suffix); (a)–(f) answered: dropping forCombat's alliance term is a transcription (no orig team test), the spectator refusal kept as the port's convention, instabuild is the game type in every vanilla path, the PlayNicelyGateParityTests site-11 change keeps its pin, no T5b row goes red.

## TARGETING WAVE 3, T3b (2026-09-05) — scan sets: the living boxes back on the four players-only hunters, five spheres back to boxes, the Dragonfly's hunt outside its retarget, the Ender pair's nearest-then-filter pick every pass, the companions' box-only every-pass task with its Creeper hunt and nearbyOnly, the Ant Robot's distmul

- **ENT-S-135 — FIXED (2026-09-05, wave 3).** Targeting ledger batch T3b (`phase_g_reports/targeting_survey_2026-09-04.md` §T3b: 22 rows, 16 blocks). Fifteen port files and one new class, every site at the orig position with the orig polarity, box, cadence and term order; T5 / T6 / T7 / T8 rows untouched; T3c's four files untouched. The two refuters' code findings (the companion class's pre-reach grants and the Valentine Player task's sight, the Cater Killer's pass-local `e`, the Dragonfly's near-retarget 2.1, the `!mustSee` line) are applied and pinned (rows 41-43, s129_13 extended); their record corrections folded in. Two rows were present at HEAD (the Nightmare's heal branch, ENT-S-129; the grounded Stinky's pass, ENT-S-119) — verified, no edit, pinned.
  - EntityCaterKiller.java:126-134 (the vanilla `NearestAttackableTargetGoal<Player>` removed — orig CaterKiller.java:68 registers no search task), :327-343 (orig :462-500 — the 1-in-4 pass: T5's stored read and 1-in-200, then orig :471-473's `if (e == null)` scan on the pass-local `e` — nulled by the dead drop alone (:330, orig :466) and not re-read after the 1-in-200 (:336), so the clearing pass runs no scan and a cleared attacker is re-picked only on the next pass (T3b refuter B, D1) — the pick to the slot under the ENT-S-129 mark, re-derived every pass, cleared when empty), :389-393 (orig :559-574 — PlayNicely gate, the 20/8/20 `LivingEntity` box, `TargetSelection.firstMatch` over the `GenericTargetSorter` of orig :43 / :62 / :564), :404-410 (orig :533-557 — null / self / dead, `myCanSee`, the player branch `instabuild` — ENT-S-132's term at its orig position :548 — CaterKiller after the player branch, Monster, `MyUtils.isAttackableNonMob`), :425-468 (orig :626-676 `MyCanSee` — the ten-sample walk from 2.5 blocks ahead along the yaw at y + 3 to the target's mid-height, the per-axis normalisation, `(int)` casts as BUG-027 ruled faithful, passed through air / COBWEB / SHORT_GRASS + FERN / the four 1.7.10 `leaves` variants); the mark :101, the hurt hand-off :208-209, the `setTarget` override :225-229. The hold: the scan's pick re-derived every pass (orig's transient), a stored attacker (the `hurt` Mob store :96-98, the `RevengeGoal` :68 with T5's 1-in-200) read first and kept; `Params.caterKiller` consumes the slot. The vanilla hold ENT-S-129 / ENT-S-132 disclosed is gone with the goal.
  - SeaViper.java:125-134 (the goal removed — orig SeaViper.java:66 registers the revenge task alone), :298-318 (orig :482-495 — the 1-in-5 `== 1` pass in the Sea Monster's ENT-S-115 / ENT-S-129 slot shape: the stored read inside the gated method, a dead one cleared, the scan's pick re-derived under the mark), :336-340 (orig :530-551 — the 18/4/18 box, the sorter of :42 / :59 / :535), :350-356 (orig :504-528 — sight, the player branch :519, SeaViper, Monster, `isAttackableNonMob`); the mark :92, the hurt hand-off :243-244, `setTarget` :261-265. The hold: as the Cater Killer's; `Presets.seaViper` (with its stand-down) consumes the slot; the plain `HurtByTargetGoal` stays.
  - Hammerhead.java:146 (orig :207-209 — the pass's `if (e == null) e = findSomethingToAttack()` replacing `getNearestPlayer(this, 18.0)`), :175-179 (orig :251-268 — the 18/9/18 box, the sorter :57 of orig :38 / :48 / :256), :189-195 (orig :225-249 — sight, Hammerhead, the player branch, Monster, `isAttackableNonMob`); the pick transient per pass as orig and as HEAD; the ENT-S-115 fallback read and rt logic untouched.
  - SeaMonster.java:227 (orig :527-532 — the scan inside the ENT-S-129 mark's block), :265-269 (orig :513-533 — the 16/4/16 box, the sorter :72 of orig :39 / :55 / :518), :279-285 (orig :487-511 — sight :497, the player branch, SeaMonster, Monster, `isAttackableNonMob`).
  - Irukandji.java:187, :218-222, :230-234 (orig :294-295, :304-309; :270-288 — the 6/4/6 box, the sorter :61 of orig :47 / :295, sight then the player branch) and Skate.java:174, :205-209, :217-221 (orig :286-287, :296-301; :262-280 — the 10/4/10 box, the sorter :55 of orig :33 / :48 / :287): the spheres of `getNearestPlayer(this, 6.0 / 10.0)` gone; the mark and the stored read untouched.
  - EntityBrutalfly.java:206 / :348-360 and Mothra.java:416 / :293-305 (orig Brutalfly.java:215 / Mothra.java:224 — `findNearestEntityWithinAABB(EntityPlayer.class, box 30/20/30 / 25/20/25, this)`): `findNearestPlayerInStrafeBox()` = `getEntitiesOfClass(Player.class, bbox.inflate(…))`, the nearest on `<=` (the Kraken's ENT-S-105 idiom); the creative test the caller's (ENT-S-132's nesting untouched); Mothra's root box MOD-029's in modern mode as for stage 2.
  - AmbientFlightGoal.java:114-116, :142 (the tick's else branch → `onRetargetSkipped()`, a no-op default), :73 (`Params.dragonfly`'s near-retarget 2.1 — orig Dragonfly.java:124's `< 2.1f` on the integer cell distSq, which `tick` :108 compares: cells 0, 1, 2 retarget, 3 hunts; HEAD's 4.5 retargeted at 3 and 4 too — refuters A-D4 / B-Q4; the Params javadoc :48-52 corrected, the other presets untouched) and DragonflyHuntGoal.java:85-95 (orig Dragonfly.java:142-149 — the 1-in-12 hunt on every tick the :124 retarget did NOT fire: the roll, the difficulty, `findPrey`, the ENT-S-129 slot hand-off, the flight target onto the prey; the `pickRetarget` override removed — the wander is super's); the one-bite `tick` kept.
  - EnderKnight.java:85, :87-88, :92-101 and EnderReaper.java:81, :83-84, :88-97 (orig :61-81 with td.bq): the player goal's registration in the 6-arg form with interval 0 (every goal pass — the engine's every-other-tick harness for orig's every tick), the conditions without a range term, and `findTarget` overridden — `getNearestPlayer(x, y, z, getFollowDistance(), NO_SPECTATORS)`: the ONE nearest player of any mode within 64 / 81 of the position, a plain sphere, strict `<`, then :67's stare conditions on that player alone (nearest-then-filter: a nearer non-starer shadows a farther starer); T8's selector, same-tick creative drop and hold untouched.
  - new entity/ai/MyEntityAINearestAttackableTargetGoal.java (orig MyEntityAINearestAttackableTarget.java: interval 0 :60; the conditions `forCombat().selector(sel)` without vanilla's range sphere :64-67 — vanilla's own `!mustSee → ignoreLineOfSight` kept (:65; inert, every registration passes true — refuter B-D2) and no line of sight for a `Player.class` task (:66 — orig MyEntityAITarget.java:96 answers a player ahead of the sight step :108; refuter A-D2); `getFollowDistance()` = the per-task `targetDistance` :70-72; `canAttack` overridden :88-96 — after the conditions, orig's grants AHEAD of the nearbyOnly reach block (a Player :96, Mothra :105, a Creeper :111, a Ghast :114), else vanilla's reach cache (refuter A-D1: vanilla reach-tested every candidate and refused a fence-ringed creeper orig :111 took); `findTarget` :106-112 — the `expand(d, 4, d)` box over the target class, plain-distance order, the first `canAttack` accepts: the selector, `forCombat`'s screens, the line of sight, the grants, and with `nearbyOnly` vanilla's reach cache — `TargetGoal.canReach`'s 1.5-block end-node test as orig MyEntityAITarget.java:131-144, behind `reducedTickDelay(10 + nextInt(5))`: 5-7 goal passes, orig's 10 + nextInt(5) ticks for one candidate, halved in candidate evaluations); Boyfriend.java:164-187 and Girlfriend.java:229-253 (the Creeper task restored at orig's priority 2 — `Creeper.class`, 20, sight, nearbyOnly, the ENT-S-124 / ENT-S-128 predicate, the ENT-S-115 live gate — and the IMob task on 15 at 3 / 5, the Girlfriend's priority ENT-S-130's); Girlfriend.java:304-323 (`ValentineTargetGoal` on the new class: the 16/4/16 box for both Valentine tasks, MOD-036's selector and `forCombat` kept; the Player task takes a player it cannot see or reach as orig :96 did — HEAD's goal applied the sight, the draft the reach — the Boyfriend task keeps both). The order is plain distance — orig's creeper halving is T4's.
  - AntRobot.java:734-742 (orig :1011-1026 — `findSomethingToAttack(float distmul)`, `inflate(12 * distmul, 12, 12 * distmul)`), :251 (2.0f, orig :117) and :315 (1.0f, orig :622); the dircheck argument stays the T2 / T8 cross-reference.
  - PitchBlack.java:415-441 (ENT-S-129's orig :259-280 branch) and EntityStinky.java:436-459 (ENT-S-119's pass ahead of the activity-1 return): present at HEAD, verified, pinned.
- **Sorters (T4's):** `GenericTargetSorter` at the six sites orig sorted with it (the Cater Killer, Hammerhead, Irukandji, Sea Monster, Sea Viper, Skate) — the ledger's T4 §(ii) rows for the first five close with these loops; `<=` on the Brutalfly strafe (T4 §(iii)); nearest-then-filter on the Ender pair (T4 §(iv)); the companions' creeper halving (T4 §(v)) left to T4.
- **Pins:** new `ScanSetParityTests` (own batch `scanSetParity`, TEST-003; `@GameTestGenerator`, 43 rows `scansetparitytests.s135_NN_<species>_<site>` — 40 of the lane, 3 of its refuters: a fence-ringed Creeper granted ahead of the reach test by the Boyfriend's Creeper and IMob tasks with the goal's own `canReach` false as the precondition (41), the valentine-angry Girlfriend's Player task taking a survival player behind the SightStepParityTests wall under the SeasonalDates Feb-14 seam and her Boyfriend task refusing a Boyfriend behind it, taking him razed (42), the Dragonfly's flight target at cell distSq 2 retargeting and at 3 hunting — the bite lands (43)): the discriminating geometry per row — a survival player (a plain ServerPlayer) or a pig / Zombie at the box corner beyond the sphere (taken with the box, refused by the sphere) and straight above inside the sphere past the box's +y (refused, then taken just inside the top); the four living scans' Zombie with no goal left, own kind refused, the pig / villager / Zombie ladder, the sight step; the Cater Killer's walk against the eye ray both ways; the 1-in-4 / 1-in-5 / Dragonfly / Ender / companion cadences through the ForcedRoll seam; the Ant Robot's distmul; the grounded Stinky and its sitting gate; the Ender pair's shadowing non-starer and plain sphere (FOLLOW_RANGE lowered, a sneaking starer); the companions' corner Zombie, Creeper task at 2, every-pass, fence-ringed nearbyOnly; the Valentine player box. Frozen companions on the ground with FOLLOW_RANGE 40 for the path search; every flip restored in a finally; no hit pinned on a mock player. Re-based, none loosened: `PlayNicelyGateParityTests` sites 2 / 4 / 40 and its GoalProbe, `CreativeGateParityTests` rows 25-33 (the Cater Killer's and Sea Viper's triples through their filters), `TargetReleaseParityTests` s129_41 (the flight target parked, the retarget pinned quiet), `helperHoldDistance` and s129_13 extended (a Zombie attacker held by the RevengeGoal: the 1-in-200 clearing pass leaves the slot and the mark empty — no scan on the pass-local `e` — and the next pass re-picks it under the mark), `PeacefulGateParityTests` site 5 (`onRetargetSkipped`), `IMobConventionTests` / `PreyListParityTests` (the companion on the ground), `PortOnlyTargetingTests`' MOD-033 selector lists; its MOD-036 rows hold unchanged (they ask the Player task's conditions about creative and Peaceful, never the line of sight). javac rc 0 (`--release 21 -proc:none -implicit:none`, the g1 runtime classpath + build/classes) over the 24 compilation units; the gametest run is the gate's (expected `All 1066 required tests passed`: 1023 + T3b's 43).
- **Disclosed:** the Ender pair's every-other-tick goal pass for orig's every tick — this batch's own harness residual, a T10 row candidate (exact parity needs the pick in `customServerAiStep`); spectators skipped by the Ender search and the hunters' 2-arg box scans (NO_SPECTATORS) and refused by the conditions' `canBeSeenByAnyone` on the companion class's 3-arg scan (the port's convention); the Cater Killer's walk passes the four 1.7.10 `leaves` only (a literal transcription — `leaves2` and the modern leaf types occlude); the companions' path search bounded by FOLLOW_RANGE 16 in both trees for the candidates the reach test governs (no creeper, ghast, Mothra or Valentine player — orig's grants precede it) and the Boyfriend Valentine task's `mustReach`, inert at HEAD, now live; the companion hunts' tame / sitting gates of orig :44-52 are the companion block's residual, filed as ENT-S-137. `Params.dragonfly`'s 4.5 for orig's 2.1, disclosed by the draft as unchanged, is corrected above.

GATE (t3b): gate t3b_t3c red on three of ScanSetParityTests' 43 rows with every other test green (1081): s135_02 / s135_03 — a pre-existing transcription miss the rows caught: EntityBrutalfly.java:210 / :225 / :305 floored the strafe marks (blockPosition().above(n)) where orig Brutalfly.java:219 / :232 / :277 cast (int) — fixed in code (the twin Mothra sites already cast), two floor-derived sibling rows re-derived with the cast (CreativeGateParityTests :679-680, CreativeMappingParityTests :321), none loosened; s135_07 — the row's own positive-origin stone placement for orig's float walk (the fifth sample sits one cell nearer the origin on negative x / z and one more from float quantisation at |x| ≈ 10^7), rebuilt as a float-for-float replay of orig :627-671 on the entities' actual positions, the code untouched; the other 40 rows checked under the same lens (none origin-dependent); four same-class sites presented, not applied — ENT-S-138. Re-gate t3b_t3c2 green (04:31-04:32): build legs (asset audit 0 errors / 0 advisories / 4 acknowledged, G1 PARITY 2 + 11 models with the checked-in proofs verified, drift check exit 0) and runGameTestServer 'All 1084 required tests passed' (1023 + T3b's 43 + T3c's 18)

Refuted twice (24 files touched, label L): A (transcription) — every ladder, box, cadence and sorter and the myCanSee walk upheld (the four-block leaves set is 1.7.10's exact test; #minecraft:leaves would be a MOD-class widening); four code defects fixed as proposed — the companion goal's grants (Player / Mothra / Creeper / Ghast) ahead of the reach test as orig MyEntityAITarget.java:96 / :105 / :111 / :114, the Valentine Player task without sight or reach (orig :96 returns first), the CaterKiller forget pass reading the pass-local (orig :471), the Dragonfly near-retarget 4.5 → 2.1 (orig Dragonfly.java:124, Dragonfly-only) — three pins added (43); the CRLF working copies of four files recorded, not stripped (index LF, commits normalise). B (gates, holds, pins, re-bases) — the holds as T5 set them, the boxes / cadences / state gates at orig's positions, every re-based sibling row still pinning its 1.7.10 fact with nothing loosened; the inert !mustSee → ignoreLineOfSight restored; eight T4 tie-break rows closed by the loops noted in the ledger (two pinned here, six 'pin pending (T4)'), the Mothra stage-1 tie-break corrected to <= in both trees; the Ender pair's pick on the every-other-tick goal pass recorded as this batch's harness residual (a T10 row candidate); the companions' untamed / sitting hunt gates filed as ENT-S-137; twelve record corrections applied (cites :38 / :32 / :114-116, the 15-block sphere, the grant exception in the changelog, the ledger :1194 misreading for the owner).

## TARGETING WAVE 3, T3c (2026-09-05) — scan-set widening on the port's vanilla goals: orig's follow-range box, range and hold restored through each goal's own getFollowDistance at the Leon and both Princes (their targetChance-0 cadence with it), orig's 12x5x12 box and 1-in-6 cadence restored on the Pointysaurus's goal; the FOLLOW_RANGE attributes untouched

- **ENT-S-136 — FIXED (2026-09-05, wave 3; the Pointysaurus's cadence 1-in-6 applied in the Q2 follow-up the same day).** Targeting ledger batch T3c (`phase_g_reports/targeting_survey_2026-09-04.md` §T3c: 4 rows, 4 hunters). 1.7.10's `EntityAINearestAttackableTarget` scanned `boundingBox.expand(d, 4, d)` with d = the follow-range attribute (`EntityAITarget.getTargetDistance`, EntityLiving's base 16 — Leon.java:112-118, ThePrinceAdult.java:132-138, ThePrinceTeen.java:136-142 set none), rolled nothing (targetChance 0) and released beyond the same d; the Pointysaurus's own scan took `expand(12, 5, 12)` (Pointysaurus.java:253), sorted by GenericTargetSorter (:254), the first suitable (:258-262), acted on in the same pass (:201-213) and never stored. The port's four vanilla goals read FOLLOW_RANGE 40 / 24 / 64 / 32 for box, sphere and hold — an attribute that also sizes the navigator's path search at every site — so each goal instance now overrides `getFollowDistance()` (the Dragon's ENT-S-117 idiom) and the attributes stay:
  - EntityLeon.java:185-203 (orig Leon.java:92-93): `getFollowDistance() → 16.0` (:199-202) on the target-priority-4 `NearestAttackableTargetGoal<Mob>` — box `inflate(16, 4, 16)`, sphere 16, hold 16; `randomInterval` 10 → 0 (:192; orig's targetChance 0 — the ENT-S-117 mapping, Dragon.java:158; the ENT-S-124 clause at :191 says so); FOLLOW_RANGE 40 (:235) kept (the combat step's `getNavigation().moveTo` :476, FollowOwnerGoal, the stroll). The MOD-033 key gate and tame rule, the ENT-S-124 / ENT-S-127 selector and the ENT-S-115 gate untouched.
  - Pointysaurus.java:101-152 (orig Pointysaurus.java:253-262, :183, :201-213): the target-priority-3 `NearestAttackableTargetGoal<Player>` (:123-152) scans orig's box itself — `findTarget()` overridden (:139-146): `getEntitiesOfClass(Player.class, getBoundingBox().inflate(12, 5, 12))`, `TargetSelection.firstMatch` under the port's `GenericTargetSorter` (held in the goal, :124-125, as orig's field :39 / :49), the first candidate `canAttack(candidate, targetConditions)` admits; the T8 initializer rebuilt without `.range(...)` (:127-131 — the ENT-S-106 screen and the `instabuild` selector exactly as T8 wrote them); `getFollowDistance() → 12.0` (:148-151) kept for the hold alone (orig held nothing — the T5 row's hold rule); FOLLOW_RANGE 24 (:227) kept (the melee chase, MyEntityAIWanderALot); MOD-034's stare goal untouched. The T8 comment's "class, box and cadence are T3c's" clause brought to the state (:105-108); the ENT-S-136 comment (:109-122) no longer calls the box "the PN-020 family" — orig acted on the box's pick in the same pass (turned to, walked at), player-visible, which PN-020's ring (dropped before any attack step) does not cover. Interval 10 → 6 (:123 — the 6-arg constructor `new NearestAttackableTargetGoal<>(this, Player.class, 6, true, false, null)`, the initializer and overrides unchanged; `reducedTickDelay(6)` = 3 on the every-other-tick pass = orig :183's `nextInt(6) == 0` per tick, exact; the cadence clauses at :106-107 / :119-122 brought to the state — the Q2 follow-up, the same day).
  - ThePrinceAdult.java:159-177 (orig ThePrinceAdult.java:112-114): `getFollowDistance() → 16.0` (:173-176); `randomInterval` 10 → 0 (:166; the ENT-S-124 clauses at :157 / :165); FOLLOW_RANGE 64 (:187) kept (`getNavigation().moveTo` :316).
  - ThePrinceTeen.java:170-188 (orig ThePrinceTeen.java:116-118): `getFollowDistance() → 16.0` (:184-187); `randomInterval` 10 → 0 (:177; the clauses at :168 / :176); FOLLOW_RANGE 32 (:198) kept (`getNavigation().moveTo` :317).
- **Pins:** new `VanillaGoalRangeParityTests` (own batch `vanillaGoalRangeParity`, TEST-003; `@GameTestGenerator`, 18 rows `vanillagoalrangeparitytests.s136_NN_<species>_<row>`: three per site in orig file order (01-12), `cadence_no_roll` at each IMob site (13-15), `box_corner_taken` / `vertical_band_refused` at the Pointysaurus (16-17), `cadence_1_in_6` there (18)): `edge_inside` — a Zombie (the IMob goals) or a plain survival ServerPlayer (the Pointysaurus; PlayNicelyGateParityTests' `survivalServerPlayerAt`) 0.05 inside orig's edge along +x on the same floor, taken through `canUse()` with the pick read back (its box asserted to meet the scan box); `edge_outside` — at d + the hunter's half-width + the candidate's half-width + 0.05 along +x (18.10 / 13.80 / 19.475 / 17.975; orig's `selectEntitiesWithinAABB` was a box intersection, so d + 0.05 alone still met the box): outside orig's box and vanilla's sphere alike — the candidate's box asserted NOT to meet the goal's search area (`getTargetSearchArea(d)` by reflection at the Mob goals, `getBoundingBox().inflate(12, 5, 12)` at the Pointysaurus) and asserted inside the attribute's range, so HEAD's goal took it: refused, the discriminating row (d + 0.05 itself is PN-020's ring, unpinned); `follow_range_kept` — the attribute 40 / 24 / 64 / 32 unchanged (asserted in every row) with the goal's own `getFollowDistance()`, its conditions' `range` snapshot (16 at the Mob goals; none at the Pointysaurus — the field at its -1 default) and (Mob goals) `getTargetSearchArea(d)` = the hunter's box inflated (16, 4, 16) at orig's values; `cadence_no_roll` — `randomInterval == 0` by reflection and the inside Zombie taken with NO roll forced, the hunter's `Entity.random` a seeded draw counter that records no `nextInt(5)`; `box_corner_taken` — the survival player at (11.9, 0, 11.9), inside the box, 16.83 off, beyond any 12 sphere: taken; `vertical_band_refused` — the player 7.95 straight up (the hunter's 2.9 + 5 + 0.05: feet-anchored boxes, its feet clear the box's top), 7.95 off, inside any 12 sphere: refused; `cadence_1_in_6` — `randomInterval == 3` by reflection (interval 6's reducedTickDelay) and the inside player driven twice, the bound-3 roll forced to 1 (refused, nothing picked) then to 0 (taken). The Pointysaurus goal's roll forced (bound 3, with the 3-arg constructor's bound 5 chained — `rolls(GOAL_ROLL_BOUND, 0, 3, 0)`, the same chain at PlayNicelyGateParityTests:652 and CreativeGateParityTests:302), hunters with goals and no AI, candidates frozen (`setOnGround`), sight and the feet-to-feet distance asserted; PlayNicely set false and restored in a finally; players removed, spawns discarded there. javac rc 0 (the five files, and the Q2 follow-up's over Pointysaurus.java and the three probe classes; reflection names verified against neoforge-21.1.223.jar); the gametest run is the gate's.
- **Disclosed:** (i) cadence — the three IMob goals now pass interval 0 (orig: no roll on `EntityAITasks`' every-third-tick pass — the ledger's Leon "every tick" is the same task, corrected to "every 3rd tick"); vanilla's GoalSelector runs target goals on `Mob.serverAiStep`'s every-other-tick pass where 1.7.10's EntityAITasks ran every third — ENT-S-117's own residual, not this batch's; the Pointysaurus's `nextInt(6)` every tick (:183) against its goal's interval 10 — restored in the Q2 follow-up: interval 6 (`reducedTickDelay(6)` = 3 = 1-in-6 per tick, exact), the bound-5 forcings that serve its roll chained with bound 3 (PlayNicelyGateParityTests:652 — HEAD :619, CreativeGateParityTests:302 — HEAD :296, VanillaGoalRangeParityTests:263); (ii) the Pointysaurus's vanilla hold releases beyond 12 (was 24) or after 60 unseen ticks where orig stored no scan pick — the T5 row's hold rule; (iii) ENT-S-130 (priorities) stays a REPORT.

GATE (t3c): gate t3b_t3c red on three of ScanSetParityTests' 43 rows with every other test green (1081): s135_02 / s135_03 — a pre-existing transcription miss the rows caught: EntityBrutalfly.java:210 / :225 / :305 floored the strafe marks (blockPosition().above(n)) where orig Brutalfly.java:219 / :232 / :277 cast (int) — fixed in code (the twin Mothra sites already cast), two floor-derived sibling rows re-derived with the cast (CreativeGateParityTests :679-680, CreativeMappingParityTests :321), none loosened; s135_07 — the row's own positive-origin stone placement for orig's float walk (the fifth sample sits one cell nearer the origin on negative x / z and one more from float quantisation at |x| ≈ 10^7), rebuilt as a float-for-float replay of orig :627-671 on the entities' actual positions, the code untouched; the other 40 rows checked under the same lens (none origin-dependent); four same-class sites presented, not applied — ENT-S-138. Re-gate t3b_t3c2 green (04:31-04:32): build legs (asset audit 0 errors / 0 advisories / 4 acknowledged, G1 PARITY 2 + 11 models with the checked-in proofs verified, drift check exit 0) and runGameTestServer 'All 1084 required tests passed' (1023 + T3b's 43 + T3c's 18)

Refuted twice (5 files touched, label S): the first pass upheld the code (one getFollowDistance() override per goal, the attributes untouched) and found the rows' outside-edge premise wrong (orig's box took a candidate up to d plus both half-widths — PN-020's ring), the cadence halves unapplied (interval 10 → 0 at the three IMob goals per ENT-S-117's mapping of targetChance 0; the Pointysaurus's 1-in-6 → interval 6) and the Pointysaurus's box scan player-visible (orig acted in the same pass with no hold — not PN-020's case); all applied: the edge rows beyond both box and sphere, three cadence pins, the Pointysaurus goal's conditions without a range term and findTarget scanning orig's 12x5x12 box under orig's GenericTargetSorter (the hold's reach 12), the interval-6 registration with the three sibling forcings chained (bound 5 and bound 3) and an 18th pin; the second pass upheld the fix (the sorter, the dropped visibility scaling a transcription, the geometry re-derived on feet-anchored boxes) with two test-string cites and three record wordings corrected.

## TARGETING WAVE 3, T4 (2026-09-05) — tie-breaks and sorters: GenericTargetSorter back on the seventeen TF-035 remainders (through TargetSelection.firstMatch, the stable order), the companions' task on the port of its own creeper-halving sorter with the Valentine tasks plain, the fourteen rebuilt sorter sites and the Brutalfly / Mothra strafe ties pinned

- **ENT-S-139 — FIXED (2026-09-05, wave 3).** Targeting ledger batch T4 (`phase_g_reports/targeting_survey_2026-09-04.md` §T4: 35 rows, 35 blocks; plus the Mothra stage-1 tie cell T3b corrected to MATCH "pin pending (T4)"). 1.7.10 sorted every custom scan with `GenericTargetSorter` (orig GenericTargetSorter.java:18-35 — the first operand's terms :20-26: distance² from the hunter, halved for an EntityCreeper :21-23, divided by a `height * width` silhouette over 1 :24-26; the second's :27-33; the three-way :34, ties 0 — `Collections.sort` stable) and took the first suitable; the port's seventeen TF-035 remainders sorted by `Comparator.comparingDouble(this::distanceToSqr)`; the companions' own task (orig MyEntityAINearestAttackableTarget.java:38 / :57 → MyEntityAINearestAttackableTargetSorter.java:21-31: creeper halved, NO silhouette term) sorted plain in the port's `MyEntityAINearestAttackableTargetGoal`. Sort order only at every site — no scan box, class, cadence, gate or ownership mark moved (T3b / T5 / T6 / T7 / T8's rows stand).
  - The seventeen swaps, `new GenericTargetSorter(this)` through the existing `TargetSelection.firstMatch` (order-preserving — its index tiebreak is the stable sort's tie order, OPT-021's contract; the ENT-S-108 shape), the orig cite on each line, the `GenericTargetSorter` import added where the simple name is used and the now-unused `java.util.Comparator` import dropped at the inline sites: Cephadrome.java:120 (the field, used :433 — orig :61 / :84 / :580); Cryolophosaurus.java:146 (orig :58 / :218; the "nearest first" javadoc :138 corrected); entity/ai/DragonflyHuntGoal.java:103 `new GenericTargetSorter(this.mob)` (orig Dragonfly.java:45 / :236; the class javadoc :22); Fairy.java:92 (used :160 — orig :63 / :243); Frog.java:263, fully qualified as the file's own style (orig :55 / :312); EntityGammaMetroid.java:223 (orig :59 / :298); EntityKyuubi.java:137 (orig :56 / :209); EntityLeon.java:800, the custom scan (orig :63 / :97 / :435; the vanilla goal's plain pick is orig's own vanilla sorter — MATCH); Lizard.java:74 (used :156 — orig :45 / :62 / :340); PurplePower.java:45 (used :203 — orig :35 / :44 / :272); EntityRat.java:232 (orig :46 / :63 / :256); Robot1.java:63 (used :149 — orig :33 / :44 / :209); SpiderDriver.java:54 (the one field serving both sorts, :156 the mount pick and :175 the combat scan — orig :33 / :108 / :164); EntityStinky.java:572 (orig :48 / :76 / :692); EntityTerribleTerror.java:197 (orig :47 / :56 / :300); ThePrinceTeen.java:144 (used :917 — orig :79 / :121 / :544; the javadoc :910); EntityTriffid.java:248 (orig :42 / :54 / :326).
  - Present at HEAD, verified, no edit, pinned: the `GenericTargetSorter` field through `findSomethingToAttack` at ENT-S-135's five loops (EntityCaterKiller.java:88 / :393, Hammerhead.java:57 / :179, Irukandji.java:61 / :222, SeaMonster.java:72 / :269, SeaViper.java:79 / :340) and ENT-S-108's nine (CaveFisher.java:68 / :172, DungeonBeast.java:59 / :159, EntityEmperorScorpion.java:68 / :323, EntityHerculesBeetle.java:53 / :251, Nastysaurus.java:62 / :244, EntitySpitBug.java:62 / :278, TRex.java:43 / :227, EntityTrooperBug.java:64 / :312, Urchin.java:63 / :213); the `<=` replace of the strafe finders (EntityBrutalfly.java:348-360, Mothra.java:293-305 — ENT-S-135); the Ender pair's nearest-then-filter as ENT-S-135 left it (s135_11 / s135_14, no new pin).
  - The companions: new `entity/ai/MyEntityAINearestAttackableTargetSorter` (the port of orig's class — distance² halved for a Creeper, no silhouette term, `Double.compare` as the port's GenericTargetSorter), held per task by `MyEntityAINearestAttackableTargetGoal` (:53 the field, :65 the construction — orig :23 / :38) and read by `findTarget` (:126) through a new `protected targetOrder()` (:110-112); `Girlfriend.ValentineTargetGoal.targetOrder()` (Girlfriend.java:325-334, `import java.util.Comparator` :57) answers plain distance — orig MyValentineTarget.java:41 / :61's `MyValentineTargetSorter` (:20-24) — unobservable on its Player / Boyfriend scans, transcribed; the class javadoc :43-46.
- **Pins:** new `TargetSorterParityTests` (own batch `targetSorterParity`, TEST-003; `@GameTestGenerator`, 89 rows `targetsorterparitytests.s139_NN_<species>_<row>` in the ledger's T4 order): per site up to three geometries through the site's own scan by reflection (the private scan; the Dragonfly goal's `findPrey` off `EntityDragonfly.huntGoal`; the companions' IMob goal's `canUse()` with its pick read back), the hunter frozen at rel (20,1,24), the nearer candidate along −x and the farther along +x on the floor, sight and the feet-to-feet distances asserted — `creeper_outranks_nearer` (20: a pig where admitted, else a Zombie, 6 off against a creeper 7 off — the creeper the pick, plain distance would take the nearer), `big_silhouette_outranks_nearer` (30: a Silverfish 6 against a Ravager 9, a Chicken against an Iron Golem at the monster-refusing sites, the Lizard's Chicken 4 against a Spider 4.3, the Frog's Cricket 3 against a Mothra 7, the Dragonfly's Mosquito 4 against a Horse 5.5 — the big one the pick), `no_silhouette_term_nearer_small_wins` (2, the companions: the Silverfish over the Ravager, GenericTargetSorter's contrary ranking asserted), `control_nearer_unweighted_wins` (32: two of one kind, the nearer), the Irukandji's two controls (2: two standing survival players 5.0 / 5.1 and 4 / 5.5 — the nearer under both orders; 1.7.10's player silhouette was pose-independent, 1.08 in every pose but sleeping, so the draft's crouching-against-standing pin asserted a port-only outcome as 1.7.10's — ENT-S-140, REPORT), the Spider Driver's mount pick control (1), the Brutalfly / Mothra ties (2: two survival players at distance² 25 either side, the LAST in the box list's order the pick — a strict `<` keeps the first). Every weighted row recomputes orig's weights from the live entities and asserts the geometry discriminates before the pick, and the 20 creeper / 30 big-silhouette rows assert by reflection that the site's own filter admits the nearer candidate (`isSuitableTarget` / the Frog's `isInsectTarget` / the Dragonfly goal's `isPrey` beside the asserted sight / the companions' `canAttack(nearer, targetConditions)` — the ScanSetParityTests.filter idiom), so a ladder that refuses it fails the row on its precondition instead of passing without discriminating (T4 refuter B); PlayNicely false and restored in a finally, players removed and spawns discarded there; the difficulty and `dragonflyHorseFriendly` asserted, never flipped; companions tamed, on the ground, FOLLOW_RANGE 40 (the ScanSetParityTests idiom); no cadence roll drawn (direct scan calls; the goal's interval 0) — two outcome-neutral draws on driven paths: Lizard.java:148's `nextInt(100)` clearing an already-null lastHurtByMob (rows 21 / 22) and vanilla TargetGoal.canReach's `nextInt(5)` reach-cache duration on the companions' non-granted candidates (rows 82-87); no hit pinned.
- **Disclosed:** (i) the Frog's only over-1 insect is Mothra (an EntityButterfly in both trees) and the Dragonfly's is the horse — the rows use them; (ii) the Spider Driver's mount pick is unobservable (uniform robots) — a control; (iii) the Valentine subclass's plain sorter is unobservable — unpinned; (iv) `GenericTargetSorter`'s TF-035 javadoc remainder refreshed — "every custom scan of the targeting ledger now carries it (the TF-035 remainders closed by ENT-S-139)" — a comment only, no code; (v) `Double.compare` against orig's three-way differs on NaN / −0.0 alone (the comparator's terms verified under TF-035, FIX_LOG.md:1901-1912, and the survey's V2 :47, re-verified by the T4 refuters; ENT-S-108 verified the firstMatch shape); (vi) the ENT-S-135 records' "pin pending (T4)" cells close here; (vii) the port's player silhouette is pose-sized where 1.7.10's was 0.6 × 1.8 in every pose but sleeping — a crouching, swimming or gliding player ranks behind where 1.7.10 ranked it at every sorter site: ENT-S-140 (REPORT, the T4 refuter A), for the owner's ruling.
- javac rc 0 (20 edited sources — GenericTargetSorter's javadoc among them — and the two new classes; no gradle in this lane; the fix lane's re-run after the refutation rc 0, the mixin annotation-processor note and the pre-existing Frog.java deprecation note its only diagnostics); the gametest run is the gate's (expected `All 1173 required tests passed`: 1084 + 89).

GATE (t4): (the orchestrator's).

GATE (t4): gate t4 green (first and only run, 05:59-06:02): build legs (asset audit 0 errors / 0 advisories / 4 acknowledged, G1 PARITY 2 + 11 models with the checked-in proofs verified, drift check exit 0) and runGameTestServer 'All 1173 required tests passed' (1084 + T4's 89)

Refuted twice (21 files touched, label M): A (sorter semantics) — GenericTargetSorter's comparator verified term for term against orig :18-35 (the squared distance from the owner's feet in both trees under ENT-S-120's verified premise, the instanceof-creeper halving before the silhouette division, height × width > 1 dividing the candidate's own distance, Double.compare for the three-way, ties 0; firstMatch = the stable sort plus the first accepted), the new companion sorter line for line against orig MyEntityAINearestAttackableTargetSorter.java:21-31, the Valentine plain order = orig MyValentineTargetSorter.java:20-24, all 17 swaps at orig's sort points, the 14 present-at-HEAD sites (Urchin included — the ledger's 35th row) and the ENT-S-135 ties; one substantive defect — the Irukandji crouch row pinned a port-only outcome (1.7.10's player silhouette was 0.6 × 1.8 in every pose but sleeping; the port weighs the modern pose box) — the row reduced to a two-standing-players control and the divergence filed as ENT-S-140 (a ruling before the verified sorter moves; the sleeping case noted); two cite corrections (orig :18-35 with the three-way at :34; the comparator's terms verified under TF-035 and the survey's V2, not ENT-S-108). B (scope, pins, records) — every hunk the comparator construction or a comment, the companion goal's T3b shape intact, all 89 rows discriminating with the plain comparator reverted (the companions' two no-silhouette rows separate their sorter from GenericTargetSorter only — said so), 1084 + 89 = 1173; one hardening applied — every weighted row asserts by reflection that the site's filter admits the nearer candidate (the ScanSetParityTests :573 precedent), so a later ladder change fails the row on its precondition; the two outcome-neutral draws on driven paths named (Lizard :148's nextInt(100); TargetGoal.canReach's nextInt(5)); the distance² wording, the controls list and the abbreviated ledger tails corrected; GenericTargetSorter's stale TF-035 javadoc replaced (comment only).

## TARGETING WAVE 3, T10 (2026-09-05) — anything else: the Ender pair's stare-driven teleports, stare sound and scream with the pick every tick, the Hammerhead's and Irukandji's second attack die, the Lizard's in-filter buddy adoption, the Luna Moth's inherited Islands hunt

- **ENT-S-141 — FIXED (2026-09-05, wave 3).** Targeting ledger batch T10 (`phase_g_reports/targeting_survey_2026-09-04.md` §T10: 5 rows, 5 hunters; plus the ENT-S-117 refuters' Luna Moth observation and ENT-S-135's disclosed cadence residual (i)). 1.7.10's Ender Knight and Reaper ran the stare-driven teleports at the head of `onLivingUpdate` off the target the legacy loop had left (EnderKnight.java / EnderReaper.java:124-138: a staring player target inside distSq 16 → `teleportRandomly`, the far counter 0; any other target beyond distSq 256 for 30 ticks → `teleportToEntity` :149-157, the counter 0 on a landing; no target → screaming off, the counter 0), and their pick (:61-81) played "mob.endermen.stare" at the player once at the pick (:68-73 — the six-tick cadence recurs only while the pick is re-asked on target-less ticks, a starer picked and dropped each tick — a creative starer under td.bq's same-tick drop — since orig's findPlayerToAttack ran only while entityToAttack was null) and set the scream on (:74) or off (:78); the Hammerhead bit on `nextInt(3) == 1 || nextInt(4) == 1` (Hammerhead.java:213) and the Irukandji on `nextInt(4) == 0 || nextInt(5) == 1` (Irukandji.java:258); the Lizard's filter adopted a Lizard candidate as its buddy on a 1-in-10 while no follow ran (Lizard.java:328-330); the Luna Moth hunted as a type-1 butterfly through `super.updateAITasks()` (EntityLunaMoth.java:122). The port had none of it: the pair's aiStep carried the daylight and fire teleports alone with no `teleportToEntity`, the scream set by `hurt` only, the pick asked on the engine's every-other-tick goal pass; the first die alone at both bite sites; no adoption; the moth's own `AmbientFlightGoal` with no hunt. Nine port files, every site at the orig position with the orig polarity, dice order and short-circuit; T8's / T3b's goals, passes and ladders untouched but for the lines below. The §T10 IrukandjiArrow observation (lane M O5; count :88-90, ding :100-103): closed by ENT-S-111 (2026-09-04) — port IrukandjiArrow.java:89-106 gates count, push and ding on `hit instanceof Mob` — no T10 action.
  - The Ender pair (EnderKnight.java / EnderReaper.java): the fields `teleportDelay` / `stareTimer` (:43-46 / :42-45, orig :31-32, never saved as orig); the pick's side effects inside the goal's `findTarget` (:107-125 / :104-122 — the stare sound at the player's spot through `Level.playSound(null, …, ENDERMAN_STARE, HOSTILE, 1, 1)` while the timer reads 0, the timer counted and reset past 5, the scream on for a starer, the timer and the scream off for a nearest non-starer, nothing without a player); the teleport block at the head of `aiStep` ahead of `super.aiStep()` (:262-284 / :263-285 — orig's `onLivingUpdate` ran ahead of super's legacy loop, the same read of the previous tick's target): the staring branch with `teleportRandomly` and the counter 0, the far branch's `distanceToSqr > 256 && teleportDelay++ >= 30 && teleportToEntity(target)` with the same short-circuit, the no-target reset; `teleportToEntity(Entity)` (:302-312 / :303-313 — orig :149-157 line for line, the y term's sign order as written with `posY` read as `getY()`, the landing through `randomTeleport` as `teleportRandomly`); the pick every tick — a `customServerAiStep` override (:157-163 / :154-160) runs `targetSelector.tick()` on the tick `Mob.serverAiStep` only ticks the running goals (the engine's own `(tickCount + id) % 2 != 0 && tickCount > 1`), after the engine's pass slot and the navigation, so the pick, the creative drop and the hold are asked once every server tick as orig's loop asked them (ENT-S-135 (i) closed; the goals untouched). Orig :120-123 (`isJumping = false`, `faceEntity`) are the legacy loop's steering, not this row.
  - Hammerhead.java:154 `nextInt(3) == 1 || nextInt(4) == 1` (orig :213); Irukandji.java:196 `nextInt(4) == 0 || nextInt(5) == 1` (orig :258) — the second die drawn only when the first misses, orig's answers kept.
  - Lizard.java:142-144 — `instanceof Lizard && this.random.nextInt(10) == 1 && this.followTime <= 0 → this.buddy = target` ahead of the ladder's final false, orig's term order (the roll for Lizard candidates alone, ahead of the guard), the entity's random for orig's world random (the file's ENT-S-093 convention); the port's buddy consumers (:229-231, :236-238) already read it.
  - The Luna Moth — entity/ai/ButterflyIslandsHuntGoal.java:57-61 a protected `(EntityButterfly, Params)` constructor (the public one delegates, :48-50); entity/ai/LunaMothFlightGoal.java:27 `extends ButterflyIslandsHuntGoal` over `Params.lunaMoth()` (:34-36) with its torch-seeking `pickRetarget` kept — the moth's flight goal is the butterfly hunt goal (the T3a shape: the hunt in the retarget's else branch, the 8/5/8 scan, the bite inside distSq 6) as orig :122's `super.updateAITasks()` ran it on the moth; EntityLunaMoth.java:46-56 and EntityButterfly.java:72-73 the comments. No MOD record covers the moth (MODERNIZATION_NOTES carries none), so no split.
- **Pins:** new `MiscTargetingParityTests` (own batch `miscTargetingParity`, TEST-003; `@GameTestGenerator`, 21 rows `misctargetingparitytests.s141_NN_<row>`): tick-driven, the Ender mob LIVE at rel (20,1,24) with its movement speed zeroed and `Entity.random` pinned (`TeleportRolls`: the daylight dice quiet, the teleport offsets fixed) — a survival starer 3 blocks east picked and held, the mob 4 east / 4 south of its spawn after 20 ticks with the counter 0 and the scream on (01 / 06); a frozen Zombie 17 blocks east primed as the attacker held by the revenge goal and the mob at the transcribed landing 15.7 blocks along the line after 45 ticks with the counter 0 (02 / 07); synchronous — the pick's scream, stare sound (a `PlayLevelSoundEvent` ear at the player's spot, the StinkyIdleParityTests seam: heard on the first and the seventh `canUse`, the timer 1..5 then reset — the seven direct calls are the re-asked case, the :68-73 cadence as it recurs while the pick is re-asked) and the look-away reset (03 / 08); the counter's arithmetic through a direct `aiStep()` with the random re-pinned ahead of each teleport step (the toward jitter 0.5 → 0, then the random teleport's 0.5625 → +4 / +4) — no target clears, a non-staring target 8 off holds at 7, the same target 17 off counts 7 → 10, a counter written 30 lands the mob at the transcribed toward-spot and resets, a staring target 3 off lands it 4 east / 4 south (04 / 09); `customServerAiStep` on the engine's pass parity picks nothing, on the running-only parity starts the goal on the starer (05 / 10); the dice through `ScriptedRolls` driving one `customServerAiStep` on the frozen shark / jelly with a survival player inside the bite reach (its spawn shield cleared, the row's signal) — the second die alone bites with both drawn, the first alone bites with the second's entry the script's exact remainder (`[4->1]` / `[5->1]`), both missing bites not (11-13 / 14-16); the Lizard's private filter with a frozen Lizard 5 east — adopted on 1 (the field read back), not on 0 with a Zombie leaving the script untouched, not with the follow time above 0 though the roll is drawn (17-19); the Luna Moth's goal selector carrying exactly one `ButterflyIslandsHuntGoal` in slot 8 — its own `LunaMothFlightGoal` — and no plain flight goal (20), its scan taking a survival player 5 east as a butterfly's does and refusing him in creative (21). Survival players plain ServerPlayers; the spawn shield cleared in the six dice rows alone; frozen mobs on the ground; PlayNicely and the difficulty asserted, never flipped; spawns discarded, players removed and the ear closed in a finally (the tick-driven rows' in their delayed step).
- **Disclosed:** (i) the stare sound's audience and cadence are orig's `playSoundAtEntity` (a broadcast near the starer, once at the pick; the :68-73 six-tick cadence recurs only while the pick is re-asked on target-less ticks — a starer picked and dropped each tick, a creative starer under td.bq's same-tick drop — since orig's findPlayerToAttack ran only while entityToAttack was null), not vanilla 1.21.1's client-local, 400-tick-throttled `playStareSound`; (ii) `teleportToEntity`'s y term keeps orig :150's `− posY + eyeHeight` sign order with `posY` read as `getY()` (the :89 convention) — the test's `towardLanding` recomputes the same expression; (iii) the counter and the timer are unsaved, as orig; (iv) the Knight's wet teleport (orig :116 `isWet || isBurning`; the port's Knight `isOnFire()` alone at EnderKnight.java:257-260, the Reaper keeps both) — outside the ledger, filed as ENT-S-142 (REPORT); (v) the Luna Moth's own loop — `Params.lunaMoth()` on the butterfly's numbers (orig 10 / 0.68 / 0.75) and the torch scan inside the retarget under `!canSeeSky` for orig's night-time else-branch 1-in-10 — flight rows outside the ledger, filed as ENT-S-143 (REPORT); the hunt runs on the moth's single flight target under the moth goal's retarget roll where orig ran it on the butterfly loop's private target under that loop's own roll (the pre-existing single-target extraction); (vi) the off-tick pass also asks the revenge goal's `canUse` and both holds every tick, as orig's loop did, and ticks no goal with a `tick()` body twice; (vii) records notes for the orchestrator: ENT-D-020's "all ported (port EnderReaper.java:96-113)" is stale — the current block `:263-285`, `teleportToEntity` `:303-313`, the pick side effects `:104-122` under ENT-S-141; ENT-S-135's disclosure (i) closes with "ENT-S-141: customServerAiStep :157-163 / :154-160; pinned s141_05 / s141_10"; (viii) the Ender pair's silent teleports (orig teleportTo :203-204's "mob.endermen.portal" pair at the origin and the landing; the port's randomTeleport mapping particles-only) and their missing +6.2 attacking speed boost (orig :29-30, :100-107) — outside the ledger, filed by the T10 refuter as ENT-S-144 / ENT-S-145 (REPORT).
- javac rc 0 (the nine sources and the test class; no gradle in this lane; the two `getLightLevelDependentMagicValue` deprecation notes are the pre-existing ENT-S-129 daylight lines); the gametest run is the gate's (expected `All 1194 required tests passed`: 1173 + T10's 21).

GATE (t10): (the orchestrator's).

GATE (t10): gate t10 red on the six Ender teleport rows of MiscTargetingParityTests' 21 with every other test green (1188): the rows' own floor assumption — every mob spawned at rel y 1, a block above the template's stone, so LivingEntity.randomTeleport's walk down to the first motion-blocking block (identical to orig teleportTo :169-186 — the port's mapping is faithful) landed every teleport one block low, and the two live rows also fell under gravity before the counter fired (the toward vector turned by 0.143 on x); fixed in the rows — the mobs and players on the floor, the live mobs without gravity, the landings asserted exact on all three axes (the 0.01 y slack removed), the code untouched; the other 15 rows moved with the shared constants, none affected. Re-gate t10b green (08:07-08:09): build legs (asset audit 0 errors / 0 advisories / 4 acknowledged, G1 PARITY 2 + 11 models with the checked-in proofs verified, drift check exit 0) and runGameTestServer 'All 1194 required tests passed' (1173 + T10's 21)

Refuted once (10 files touched, label S): the nine transcriptions upheld — the Ender pair's teleport block at orig's position (branches, polarity, the 16 / 256 / 30 thresholds, the resets), teleportToEntity term for term with orig's draw order, the pick's stare sound as orig's server broadcast (not vanilla's client-local throttle — a transcription), the every-tick target pass through customServerAiStep ticking nothing twice (the running-only pass identical to Mob.serverAiStep's), the two-dice bites at orig's position with the short-circuit, the Lizard buddy adoption in orig's term order, the Luna Moth's inherited hunt over its own flight; the neighbouring-cell exposure of the 64 / 81 search judged safe by construction (the own player nearest, the revenge goal holding TARGET first); one gate-failing test defect fixed as proposed (the counter rows' toward step re-pinned with the jitter draw it consumes — 0.5, not the random teleport's 0.5625); six record corrections applied (the stare cadence recurs only while the pick is re-asked; the Lizard consumer cites; the §T10 IrukandjiArrow observation closed by ENT-S-111; the Luna Moth's one-in-four vampire type; the hit-to-pick latency wording; ENT-D-020's and ENT-S-135 (i)'s notes); two findings filed from the refuter's observations — ENT-S-144 (the silent teleports: orig played mob.endermen.portal at both ends, the port's randomTeleport mapping plays nothing) and ENT-S-145 (the pair's +6.2 attacking speed modifier dropped).

## RULINGS 2026-09-05 RECORDED (docs-only) — ENT-S-120 closed on the premise; the law-11 rule for engine claims; the mirror drop's scope; ENT-S-140 → PN-022; wave 4 held; ITEM-070 B2; the chain's no-look sequencing; the changelog fold; the floating-mob convention

The owner's rulings of 2026-09-05 (through the advisor), recorded in `phase_g_reports/phase_g_scope_addendum_2026-09-03.md`
item 23 (and the closure line under item 22's supersession); executed here, docs-only:

- PUSH: the 17 commits after 2e21008 pushed first — origin/master 814d151 confirmed before anything else landed.
- ENT-S-120 CLOSED on the premise (`phase_g_reports/ents120_premise_2026-09-05.md`): no listed site diverges; the night
  set's 15–22 and the evening set's 5–10 withdrawn by the closure; OrigPos never written; the census kept with the premise
  note at its head; PN-020 / PN-021 stand. The T3a refuter's observation and ITEM-070's ‡ rows re-read under the server
  frame — a note under each register entry. STANDING RULE: an engine-behaviour claim about 1.7.10 becomes a finding only
  after a law-11 check against the jar; a "recalled" premise is presented as unverified and receives no ruling until verified.
- MIRROR DROP, SCOPE: ButterflyModel (10) and the seven client/model item models (102) join the drop — pinned by the
  reference leg where a 1.7.10 pair exists, before/after captures in the proof set where none does; one refuter,
  geometry-only; the same commit. The go is still the Section B EnderReaper A/B.
- ENT-S-140 → PN-022 (the engine's pose-sized player hitbox; OreSpawn's sorter formula exact): deliberately not reproduced,
  no code, no refuters.
- WAVE 4, one batch, HELD: ENT-S-130 / 133 / 134 / 137 / 138 / 142 / 143 / 144 / 145 and ITEM-070's classic transcription —
  parity, classic; generated pins; refuters by files touched; one changelog paragraph. ITEM-070 ruled B2: the 1.7.10
  air-walk in classic (shape A, the item's own code, the in-grass / in-water quirk included), the vanilla ray in modern
  under `[modern] chainsawSweepVanillaSight` (default ON, one MOD record when it lands). The ledger's Chainsaw filter-order
  cell (:1107) re-rated DIVERGES with the ruling.
- SEQUENCING while the look session is pending — the chain's no-look work proceeds now, each its own gated slice, in
  this order: (a) per-entity GeckoLib cache eviction, measured with the MHLib counters; (b) Slice 4c (PurplePower, Rotator,
  the render-instance expansion, the clone-aware geometry leg); (c) the G2 root-order contract (bone draw order = vanilla
  part order from the 4.8.4 bytecode, the z-fight exclusion retired, before/after per species before its gate; the proofs
  regenerate once more after the drop); (d) the spawn-100 benchmark harness (MHLib counters in the baseline; threshold
  proposed, not adopted); (e) the animation contract and the keyframe controller's return, designed and presented before
  wiring; (f) the package generator (SPEC, bone glossary, generated trigger inventory, TEXTURE_MAP, INVENTORY.csv) built and
  dry-run on the landed species, nothing under artist_handoff/ committed until the drop lands. Wave 4 gates only when a
  slice is waiting on the owner; the mirror drop lands on the Section B go. Addendum C.8 (eviction inside the G2 slice) is
  superseded by (a).
- CHANGELOG NOTE: one paragraph per wave — wave 3's five paragraphs (creative players, scan sets, T3c, T4, T10) folded into
  one, nothing dropped from them.
- HARNESS: the sibling test classes' frozen mobs at rel y 1 (a block above the floor) stay; the convention recorded as
  F0.7 in `phase_g_reports/harness_slice_2026-09-04.md`; no cross-class change while the rows pass.

Tests unchanged (1194); no code.

## FINDING FILED 2026-09-05 (docs-only) — ENT-S-146, the classic PurplePower model is a re-authoring

Found while scoping Slice 4c (the chain's (b) slice): the port's `ModelPurplePower` spins its spokes at fixed rates,
opaque and world-lit, where 1.7.10 (orig ModelPurplePower.java:44-84) drew the orb translucent (0.55 alpha, 0.75
colour), fullbright, with each spoke-fan thrown into a fresh random orientation every frame under accumulating GL
rotations. No MOD record; ENT-S-093 had sampled it unfiled. REPORT for the owner's split ruling (model transcription
under the SeaViper standard; render state as a renderer change); the 4c candidate depends on it — recommended: the fix
first, inside 4c. Register entry ENT-S-146. No code.

## PHASE G SLICE (a) — GeckoLib per-entity cache eviction, measured with the MHLib counters (2026-09-05)

WHAT: OPT-029. The twelve replaced species renderers (Beaver, Coin, Elevator, Island, IslandToo, Robot1-5,
RockBase, Vortex, behind the Phase G dev switch) each hold one GeckoLib `SingletonAnimatableInstanceCache`, and
GeckoLib never removes an entry from it: every entity id ever drawn kept an `AnimatableManager` (nine bone
snapshots for the Beaver, the controllers map, the extra-data map, three timing fields) until the JVM exited —
the Slice 2 open item (FIX_LOG.md:3761-3764), ruled its own gated slice ahead of the G2 root-order contract
(owner, 2026-09-05; scope addendum item 23 (8)(a), superseding C.8). Now a manager is dropped when its entity
leaves the client level and every manager is dropped when the client level is unloaded, and the MHLib counters
dump shows both. ~340 LOC across three new main classes, three edits, one new test class. No draw-path change,
no mixin change, `TheQueen` untouched (an Entity animatable on an `InstancedAnimatableInstanceCache`: one manager
per entity instance, collected with the entity).

WHY IT IS SAFE: an `AnimatableManager` is state GeckoLib rebuilds on demand. `getManagerForId(long)` is
`if (!managers.containsKey(id)) managers.put(id, new AnimatableManager(animatable)); return managers.get(id);`
(4.8.4, offsets 0-48), so the next draw of an evicted id creates a fresh manager and its first tick re-snapshots
the bones — what an entity that has never been seen pays. An entity leaving and re-entering render distance leaves
and re-joins the client level (a new client `Entity` under the same network id), so eviction costs one manager
construction and one first-tick snapshot per re-entry, nothing else. Pinned by row 01 (the evicted id re-created as
a new instance on the next draw).

HOW:
- `entity/client/OreSpawnAnimatableInstanceCache` (new, 68 lines) extends `SingletonAnimatableInstanceCache` —
  the class GeckoLib already picks for a replacement (a `GeoReplacedEntity` is a `SingletonGeoAnimatable`, whose
  `animatableCacheOverride()` returns `new SingletonAnimatableInstanceCache(this)`, 0-8, handed back by
  `GeckoLibUtil.createInstanceCache` at 1-12; its Entity / BlockEntity branch, 15-35, is never reached — refuter A,
  D3) — with `evict(long)` = `managers.remove(id) != null`
  (:53), `evictAll()` = count then clear (:58), `size()` (:65). `managers` is GeckoLib's own
  `protected final Long2ObjectMap<AnimatableManager<?>>` (a `Long2ObjectOpenHashMap`, constructor 6-13).
- `entity/client/OreSpawnGeoReplacement`: `animCache` is now `new OreSpawnAnimatableInstanceCache(this)` (the
  `GeckoLibUtil` import dropped), exposed through `animatableCache()`; the constructor registers NOTHING.
  `entity/client/OreSpawnGeoReplacedEntityRenderer`'s constructor registers the cache —
  `GeoReplacementCaches.register(this.descriptor.entityType(), replacement.animatableCache())` — at renderer
  construction on the client main thread of a bootstrapped game. R0: the lane's first shape registered from the
  replacement's constructor; the orchestrator's direct run of the headless s4 geo probe on the compiled classes died
  with `Not bootstrapped (… minecraft:game_event)` from `ModEntities.<clinit>` (the descriptor's entity-type supplier,
  evaluated by `entityType()`; the 4b record's reason for the lambda suppliers) — which would have failed `s4Parity`
  inside the gate's build. Moved; the probe re-run passes (exit 0).
- `entity/client/GeoReplacementCaches` (new, 145 lines): the registry, `EntityType` → cache, in a
  `ConcurrentHashMap`; `register` replaces and returns the previous cache (renderers are rebuilt on every resource
  reload — `EntityRenderDispatcher.onResourceManagerReload` runs every provider again — and the old renderer, its
  singleton and its cache drop together), `unregister`, `cacheFor`, `evict(Entity)` (the cache for `getType()`,
  `evict(getId())`, the evictions counter incremented under `ENABLED` when true), `evictAll()` (the sum, added to
  the counter under `ENABLED`), `managersHeld()` (the gauge). Loads on the game-test server: nothing client-side is
  referenced. `ensureCountersRegistered()` is an empty method whose call forces class initialisation.
- `client/GeoReplacementCacheEvictor` (new, 77 lines): `@EventBusSubscriber(modid = OreSpawnMod.MOD_ID, value =
  Dist.CLIENT)` on the game bus — the `KeybindHandler` idiom, static `@SubscribeEvent` methods, no `bus =`
  (loader 4.0.42 deprecates `bus()` for removal; javac notes it on the existing mod-bus subscribers); nothing is
  registered on a dedicated server. `onEntityLeaveLevel(EntityLeaveLevelEvent)` → `evictIfClient(event.getLevel(),
  event.getEntity())`; `onLevelUnload(LevelEvent.Unload)` → `if (event.getLevel() instanceof Level l &&
  l.isClientSide()) GeoReplacementCaches.evictAll()`; `onLoggingIn(ClientPlayerNetworkEvent.LoggingIn)` →
  `evictAll()` (refuter A, D1 — the level drop without an unload: `Minecraft.clearClientLevel(Screen)`, reached
  from `ClientPacketListener.handleConfigurationStart` at 55 on a server-driven transfer / reconfigure, nulls the
  level at 67 and calls `updateLevelInEngines(null)` at 72 with no bus post, and the next `setLevel` finds
  `level == null` and posts nothing; `ClientHooks.firePlayerLogin` (0-13) posts `LoggingIn` from `handleLogin` at
  316, after `setLevel` at 181, so the dropped level's managers go before the new level's first draw; a no-op
  after an ordinary login). The static core `evictIfClient(Level, Entity)`
  returns false without touching anything unless `level.isClientSide()`; its static initialiser calls
  `GeoReplacementCaches.ensureCountersRegistered()` so the two counter names print from mod construction on.
- `de/dertoaster/multihitboxlib/util/MHLibCounters`: `counter(String)` (:75, the `Counter` constructor stays
  private, registers into `ALL` after the nine built-ins), `gauge(String, LongSupplier)` (:83) and the `Gauge`
  class (:151-167), `gauges()` (:93), `sumAndResetAll()` appends every gauge after the counters in registration
  order without resetting it (:98-107); `ALL` and the new `GAUGES` are `CopyOnWriteArrayList`s (registration can
  run on a mod-loading worker thread, the dump on the client main thread); class javadoc extended (:26-38);
  `formatDump` unchanged; the nine names and their order unchanged (javap of the compiled class: the `<clinit>`
  string order is `client.frames … client.apply_information`).

BYTECODE (NeoForge 21.1.223, javap over the pinned jar; a correction to the slice brief): the client removal hook is
`ClientLevel$EntityCallbacks.onTrackingEnd(Entity)` — `Entity.unRide` (1), `players.remove` (12),
`Entity.onRemovedFromLevel()` (19; the method itself is `isAddedToLevel = false; return`, 0-5), `getstatic
NeoForge.EVENT_BUS` (22), `new EntityLeaveLevelEvent` (25), `<init>(Entity, Level)` (34), `IEventBus.post` (37),
the part-entity cleanup (43-97). `onDestroyed(Entity)` is `0: return` — not the hook. The level hook: `Minecraft.
setLevel(ClientLevel, ReceivingLevelScreen$Reason)` posts `new LevelEvent$Unload(this.level)` when a level is
replaced (7-26, `ifnull 27`; `updateLevelInEngines` at 57) and `Minecraft.disconnect(Screen, boolean)` posts it when
the level is dropped (130-149, `ifnull 204`; `updateLevelInEngines(null)` at 211); `Minecraft.clearClientLevel(Screen)`
(from `handleConfigurationStart` 55) drops a level with NO post — covered by the `LoggingIn` hook (above). Other
`getManagerForId` callers in the jar (refuter A, D7): `GeoReplacedEntity` / `SingletonGeoAnimatable`'s `getAnimData` /
`setAnimData` / `triggerAnim` / `stopTriggeredAnim`, `StatelessAnimatable.handleClientAnimationPlay/Stop`,
`SingletonAnimTriggerPacket`'s receive lambda (the client work queue) and `GeoItemRenderer.actuallyRender` — none is
used by an OreSpawn replacement (no `registerSyncedAnimatable` / `triggerAnim` / `setAnimData` on one; `TheQueen.java:343`
is on itself). Thread: every touch of a cache map
is on the client main thread — the draw (`GeoModel.handleAnimations`, `getAnimatableInstanceCache` at 6,
`getManagerForId` at 12, inside the render pass `Minecraft.runTick(boolean)` drives through `GameRenderer.render`
at 422, on the thread that also runs `tick()` at 171 and `runAllTasks()` at 120), the removal
(`ClientPacketListener.handleRemoveEntities`: `PacketUtils.ensureRunningOnSameThread` at 6, then
`ClientLevel.removeEntity` → `Entity.setRemoved` at 20 → `TransientEntitySectionManager$Callback.onRemove` → the
callbacks' `onTrackingEnd`) and the unload (`Minecraft`'s own methods). An integrated server posts the same events
for its server entities and levels on the SERVER thread with the same ids; the `isClientSide()` guard is what keeps
that thread out of the render thread's map (pinned by row 05). The key is the entity id on both sides:
`GeoReplacedEntityRenderer.getInstanceId` = `currentEntity.getId()` (0-8); `EntityLeaveLevelEvent.getEntity().getId()`.

BEFORE / AFTER: before — one `AnimatableManager` per entity id ever drawn by a replaced renderer, never removed,
for the session (`managers_held`, had it existed, only ever rose). After — managers == replaced-renderer entities
currently in the client level that have been drawn: evicted on `EntityLeaveLevelEvent` (client level only) and all
at once on the client `LevelEvent.Unload` (disconnect, dimension change, respawn into a new level) and on
`ClientPlayerNetworkEvent.LoggingIn` (the transfer / reconfigure path that drops a level without an unload).

HOW THE OWNER READS IT: run the client with `-Dmhlib.counters=true` (and the Phase G dev switch selecting a
replaced species, e.g. `-Dorespawn.dev.beaverRenderer=candidate`). Every 100 client ticks the INFO line
`MHLib counters (per 100 ticks): client_tick=N client.frames=… client.apply_information=… orespawn.geo.evictions=E
orespawn.geo.managers_held=H` prints. With 20 Beavers in view: H=20 (each once drawn; a mob that dies, despawns or leaves the server's tracking
range shows E counting it and H falling — the eviction rides the server's `ClientboundRemoveEntitiesPacket`
(`ChunkMap$TrackedEntity.updatePlayer` → `removePairing`); a client chunk drop by itself removes no entity
(`ClientLevel.unload(LevelChunk)` only stops ticking) — refuter B); disconnect: H=0 on the title screen;
before this slice H would have stayed at the running total of every Beaver ever seen. E is per interval and resets;
H is the live value and never resets.

PINS: new `GeoCacheEvictionTests` (own batch `geoCacheEviction`, TEST-003; `@GameTestGenerator`, five synchronous
rows `geocacheevictiontests.opt029_NN_<row>`, `orespawn:empty_large` named in full, 100 ticks; every registration
restored and every spawn discarded in a finally; frozen mobs on the floor at rel y 0 per F0.7): 01 the cache on a
real `BeaverGeoReplacement` (the same instance for a repeated id; 100 ids → 100; `evict(37)` true → 99, again false;
the evicted id re-created as a new instance on the next draw → 100, evicted → 99; `evictAll()` 99 → 0; then 0);
02 the registry with twenty frozen Beavers "drawn" through `getManagerForId(getId())` (held base + 20 and the value
`sumAndResetAll()` prints for `orespawn.geo.managers_held` equal to it; the dump order — the nine MHLib names first
in declaration order, `orespawn.geo.evictions` after them, the gauge after every counter; `evict(beaver)` true for
each → base, the gauge following; once more → false); 03 a pig without a cache (`evict` false, nothing thrown, held
unchanged); 04 Beaver + Coin replacements, five managers each (`evictAll()` = base + 10, held 0, both empty); 05 the
guard (`evictIfClient(serverLevel, beaver)` false, nothing touched; then `GeoReplacementCaches.evict(beaver)` true).
Rows 01/02/04/05 construct real replacements (`new BeaverGeoReplacement()` / `new CoinGeoReplacement()`) and
register each one's cache the way the renderer constructor does (no renderer can be built on the game-test server) —
the row asserts the registry's entry afterwards and restores the previous entry in its finally. Assertion messages carry `OPT-029`, the expected and the actual values.

DISCLOSED: (i) `orespawn.geo.evictions` counts the unload path too (`evictAll()` adds its count), so E is every
manager dropped in the interval, not only per-entity leaves; (ii) `evictIfClient` also answers false for a null level
or entity (defensive, not pinned); (iii) the game-test server has no `ClientLevel`, so the two listeners are pinned
by their bytecode cites and the static core, not by firing them; (iv) the rows construct the real replacements on
the game-test server — settled by the gate's run (1199 passed), not by inspection: the `$1` descriptors do invoke
`PoseStack.scale` in their `applyScale` bodies (never executed there) and the classes carry `BeaverRenderer` /
`CoinRenderer` class constants from the inlined `SHADOW` (never resolved) — refuter A, D4; (v) the production
registration in `OreSpawnGeoReplacedEntityRenderer`'s constructor is unpinned in the suite (a client class; refuter A,
D2) — the owner's look proves it (`managers_held` rises as replaced mobs are drawn); (vi) `orespawn.geo.evictions`'
increments are unpinnable in the suite (`ENABLED` false there; refuter A, D6); (vii) javac rc 0 for the whole main
tree and the gametest tree into a scratch directory (the gametest compile with the fresh main classes FIRST on the
classpath — the runtime list carries the stale `build/classes/java/main` ahead of them); no gradle in this lane.

GATE: (ga2, 2026-09-06 02:28-02:31, after R0 and both refuters' items; a provisional run on the R0 tree, ga, 02:01-02:04, was also green at 1199): drift check clean (g1BenchmarkVerify); `build` exit 0 — asset audit `RESULT: 0 error(s), 0 advisory(ies), 4 acknowledged -> exit 0`, referenceGeometry `G1 PARITY PASS: 2 models; checked-in proof verified`, s4Parity `G1 PARITY PASS: 11 models; checked-in proof verified` (the headless probe constructs the shipped replacements again, registry-free); `runGameTestServer` exit 0 — literal `All 1199 required tests passed` (1194 + the five GeoCacheEvictionTests rows; the real replacement classes loaded and ran on the dedicated server).

Refuted twice (MHLib and renderer plumbing touched; 7 files): A — seven non-blocking items, all applied or disclosed: D1 the `Minecraft.clearClientLevel` level drop posts no `LevelEvent.Unload` (from `handleConfigurationStart` 55) → a third hook, `ClientPlayerNetworkEvent.LoggingIn` → `evictAll()`; D2 the production registration is unpinned after R0 (a client class), disclosed — the owner's counters line proves it; D3 the cache choice comes from `SingletonGeoAnimatable.animatableCacheOverride()`, not the instanceof branch — javadocs and records corrected; D4 the `$1` descriptors do invoke `PoseStack.scale` (never executed on the server) — records corrected; D5 stale line numbers regenerated; D6 `orespawn.geo.evictions` unpinnable in the suite (`ENABLED` false there), disclosed; D7 the `getManagerForId` caller list completed (none used by a replacement). The leak, the fix, the hooks, the thread argument, replace-on-reload, the counters and the pins upheld; the dedicated-server class load SAFE. B — four non-blocking items: B1 = D1; B2 twelve replaced species (not fourteen) and the chunk-drop reading (the eviction rides the server's remove packet; a client chunk drop removes no entity) — corrected; B3 an entity drawn outside a level posts no leave event (a GUI preview; pre-existing GeckoLib property), disclosed; B4 the rows' dist safety rests on javac folding `SHADOW` — noted in the test. Every client removal path, same-dimension respawn, dimension change and disconnect traced to the hooks; a fresh manager shown observationally identical to a never-evicted one; per-class SAFE verdict, then settled by the run.

R0 (orchestrator, before the refuters): registering the cache from the replacement constructor evaluated the descriptor's entity-type supplier, and the headless s4 geo probe run directly on the compiled classes died with `Not bootstrapped (… minecraft:game_event)` from `ModEntities.<clinit>` — it would have failed `s4Parity` inside the gate's build; moved to `OreSpawnGeoReplacedEntityRenderer`'s constructor (a bootstrapped client); the probe re-run passes with all twelve captures byte-identical to the last gradle run's.

## PHASE G SLICE 4c — PurplePower and Rotator through the seam: render-instance expansion, clone-aware geometry leg (2026-09-06)

SCOPE. Rotator and PurplePower land as GeckoLib candidates behind
`-Dorespawn.dev.geckolibRenderers` (species ids rotator, purple_power), the
chain's (b) slice (owner ruling 2026-09-05, scope addendum item 23 (8)(b)).
Both were deferred at 4a because their classic models draw each part N times
per frame under a per-draw pose-stack transform (Rotator: 3 blades x 8 = 24
draws, PurplePower: 3 spokes x 6 = 18) and the landed converter emitted one
bone per part. The classic renderers stay the default; nothing player-visible
changes. The suite count is unchanged (1199): no gametest leg (the 4b finding
stands - the dedicated gametest server strips client model classes); the proof
is the headless s4 harness. PurplePower is built against the classic model AS
IT STANDS: ENT-S-146 (REPORT, 2026-09-05) records that model as a re-authoring
of the 1.7.10 one; the harness law is candidate == classic, so the candidate
reproduces the deterministic three-rate spin, opaque and world-lit, and is
re-based when the fix lands (see "ENT-S-146" below).

RENDER-INSTANCE EXPANSION (converter + manifest). A manifest entry may declare
`render_instances: {<part>: {count, axis, step_scope, step_radians |
step_degrees, step_arithmetic, note}}`. The converter then emits, instead of the
part's single bone, one bone per classic DRAW:
- `step_scope: part` (Rotator, `RotatorModel.renderFan`: `blade.zRot =
  bladeAngle; bladeAngle += FAN_STEP` eight times inside ONE
  `mulPose(axis, rf1)`): a fan group bone `<part>__fan` (pivot at the model
  origin, the pose-stack frame the classic rotates in; bind identity - the hook
  spins it) over clones `<part>__i0..7` (the part's cubes and pivot; the step
  as the clone's bind rotation on the loop's axis, replacing the part's channel
  exactly as the loop's assignment does).
- `step_scope: stack` (PurplePower, `renderToBuffer` :59-78: `mulPose(Z,
  i * 1.0471976F)` on the stack OUTSIDE the spoke's own animated rotation): one
  parent per draw `<part>__fan<k>` (pivot at the origin, bind Z rotation the
  step) over the clone `<part>__i<k>` (the part's pivot, no bind rotation) that
  the hook animates with the classic `setupAnim` channels.
- The step is the classic model's own literal in its own arithmetic:
  `step_radians` 0.7853982 accumulated in float32 (`float32_accumulate`, the
  Rotator's `bladeAngle += FAN_STEP`), 1.0471976 multiplied in float32
  (`float32_multiply`, PurplePower's `i * 1.0471976F`); the JSON carries the
  exact float the classic draws with (design said `step_degrees`; kept as an
  accepted spelling, `step_radians` is what the two models declare).
- `<model>.conversion.json` records the mapping (`render_instances.bones`:
  bone -> role, source part, draw index, group bone, static rotation, the
  loop-assigned channel; `.parts`: count, axis, scope, step, per-draw angles).
  The bone-name drift check expects the expanded names (compiled parts minus
  the expanded ones, plus the groups and clones); `mirrored_cube_count` now
  counts emitted cubes (same number for every landed model). Rotator: 27
  bones / 24 cubes; PurplePower: 36 / 18. Expanded parts must be top-level
  and childless (asserted); nothing changes for a model without the key.

CLONE-AWARE GEOMETRY LEG (probe + parity tool). `root.visit` sees three parts;
the classic model draws 24 / 18 cubes. For a model with `render_instances` the
probe captures `renderToBuffer` itself: an instrumented `PoseStack` (a public,
non-final class; `pushPose` overridden) numbers every push, and a capturing
`VertexConsumer` opens a new group whenever a vertex arrives under a new push -
one group per `ModelPart.render` call (1.21.1 bytecode: render = pushPose,
translateAndRotate, compile, children, popPose). Each group is attributed to
its part by its UV multiset (pose-invariant; identical UV sets on two parts
would be refused, none here), the draw ordinal `k` of that part within the
sample is the group's key `<part>__i<k>` (cube index as before), and the counts
are asserted against the declaration (an undeclared part drawn twice, or a
declared one drawn any other number of times, fails). Every draw also records
the pose stack it ran under: `instance_pose` (the matrix at the part's own
push - the model's per-draw transform, i.e. the fan spin / the stack step) and
`draw_pose` (the matrix its cubes compiled with). The geo side already captures
per clone bone; for an expanded rig it also records every bone's cumulative
transform conjugated into classic space (`bone_poses_classic` = stack * M^-1,
M the probe's translate(0,1.5,0)*scale(1,-1,1) normalization). `cube_map` /
`assert_same_cube_set` / `geometry_parity` / `surface_mapping_parity` pair by
the same keys, unchanged in semantics. `transforms` stays per PART. The
animation leg for an expanded model: (1) the non-expanded bones as before; (2)
each clone's channels against its source part's `transforms` with the
loop-assigned channel replaced by the clone's static step (scope part) or
exactly (scope stack), positions in the parent's frame; (3) a static group
against its bind step; (4) COMPOSITION, measured on both sides: for every draw,
`instance_pose == bone_pose(group of clone k)` and `draw_pose == bone_pose(clone
k) * T(part pivot/16)` (GeckoLib corners are absolute, ModelPart corners local
to the pivot), linear entries at `animation_epsilon_radians` (2e-6; sines and
cosines of the channel angles, |d cos| <= |d theta|), translations at
`position_epsilon_model_units` (1e-4). A hook-animated group (the Rotator's
fans) has no part channel and is proven by (4) alone - no formula is restated
in the tool. Hidden-bone sets compare by source part. `subject_after` gains
`rf1` only when the state declares one. Every new field is gated on
`render_instances`: the landed dumps, generated files, geo-render dumps,
report entries and PNGs are byte-identical (12 entries, 73 text files, 171
PNGs, verified against `build/s4` and the checked-in proof, which agree).

THE TWO CANDIDATES.
- Rotator (`entity_state`): new `entity/pose/RotatorPose` (`getRenderInfo`);
  `EntityRotator implements RotatorPose`; `RotatorModel.setupAnim` delegates to
  `poseFrom(RotatorPose, six floats)` (the former body: capture the per-entity
  `RenderInfo`); the draw loop is untouched. The advance (`rf1 += 2`, wrap
  past 359) moved verbatim into `RotatorModel.advanceFanSpin(RenderInfo)`,
  called by the classic `renderToBuffer` where it always was and by the hook;
  `RotatorModel.fanSpinRadians` is `Axis.rotationDegrees`' own conversion
  (1.21.1 bytecode `ldc 0.017453292f; fmul`) so both sides feed the same float.
  `RotatorGeoReplacement`: lambda entity-type supplier, `EntityRotator.class`,
  `RotatorRenderer.SHADOW`, no controllers; the hook reads
  `inputs.subject(RotatorPose.class).getRenderInfo()`, spins `shape1__fan` /
  `shape2__fan` / `shape3__fan` about X / Y / Z through `rotateX/Y/Z` (the
  basis helpers conjugate: internal = (-x, y, -z)), then advances. Once per
  rendered frame: GeckoLib 4.8.4 `GeoReplacedEntityRenderer.actuallyRender`
  builds the AnimationState and calls `GeoModel.handleAnimations` (offsets
  579-718) only when `isReRender` is false (`iload 7; ifne 721`), and
  `handleAnimations` ends in `setCustomAnimations` (287-292, unconditional);
  `GeoRenderer.reRender` passes `iconst_1` (offsets 12, 36) and `render` calls
  `defaultRender` once. Disclosed edge: GeckoLib runs the hook for an
  invisible entity too (only the draw is skipped, offset 748-773), vanilla's
  LivingEntityRenderer skips `renderToBuffer` and so the classic advance; the
  angle is unobservable accumulated state - recorded, not reproduced.
  Manifest: rf1 presets 0, 2, 90, 180.5, 358, 359 x ages 0 / 10 (the pose
  ignores age); `subject_after.rf1` pins the advance and the wrap (358 -> 360
  > 359 -> 0; 359 -> 0) on both sides; visuals bind, rf1 2, 90 (the
  gyroscope), 180.5, 358.
- PurplePower (`code_driven`): `PurplePowerGeoReplacement` (lambda supplier,
  `PurplePower.class`, `PurplePowerRenderer.SHADOW`), texture by
  `getPurpleType()` through the descriptor's `texture(E)` hook and the new
  `PurplePowerRenderer.textureFor(int)` (the one per-type table, the classic
  `getTextureLocation` now reads it - behaviour unchanged); the hook writes the
  classic `setupAnim` angles verbatim (7.3 / 5.1 / 3.7 degrees per tick) onto
  the six clones of each spoke. Manifest period 3600 ticks (the exact common
  period: t*7.3, t*5.1, t*3.7 are all multiples of 360 iff 3600 | t, since 73,
  51, 37 are pairwise coprime); fractions sample ages 0, 4.5, 18, 45, 108, 900,
  1800 (all three rings at 180 degrees) and 3600 (the loop closes).
- `PhaseGDevRenderers.purplePowerRenderer()` / `rotatorRenderer()`;
  `OreSpawnClient` :87 / :165 switched to them. Both constructors stay
  registry-free (OPT-029 R0): the probe instantiates them in an un-bootstrapped
  JVM. The generated geo / animation JSON is shipped byte-identical to the
  converter's output as `geo/entity/{rotator,purplepower}.geo.json` and
  `animations/entity/{rotator,purplepower}.animation.json` (empty clips).

HARNESS CHANGES (tools/, src/g1tool; G1AnimationRuntime, S4CandidateRuntime
and build.gradle untouched): as described above - `G1ModelProbe`
(`renderInstanceContext`, `RenderInstanceContext.capture`,
`InstrumentedPoseStack`, `DrawCapturingVertexConsumer`, `bone_poses_classic`,
optional `source_part` / `draw_index` on cube groups), `ProbeSubject`
(`RotatorPose`, `rf1`), `layer_definition_to_geo.py`
(`expand_render_instances`, `render_instance_angles`, `float32`),
`g1_render_parity.py` (`candidate_bone_names`, expansion-aware
`animation_parity`, `render_instance_pose_parity`, one README line for
expanded models). No threshold, epsilon or pin of a landed model changed; the
harness law holds: byte-identical results for every landed entry.

EVIDENCE (scratch run of the full s4 pipeline; the orchestrator regenerates
phase_g_reports/s4_proof):
  model_elevator   static        geometry 0 blocks; surface 720 vertex-samples, 0 zero-area ignored; animation 0 rad; visual changed 0, MAE 0, contested 0
  model_vortex     static        geometry 0 blocks; surface 48 vertex-samples, 24 zero-area ignored; animation 0 rad; visual changed 0.000137, MAE 0.00423, contested 0
  model_coin       code_driven   geometry 0 blocks; surface 168 vertex-samples, 0 zero-area ignored; animation 0 rad; pos max 0 units; hidden checks 6; visual changed 0, MAE 0, contested 0
  model_island     code_driven   geometry 2.03e-07 blocks; surface 576 vertex-samples, 0 zero-area ignored; animation 0 rad; pos max 0 units; hidden checks 7; visual changed 0, MAE 0, contested 0.464
  model_islandtoo  code_driven   geometry 2.03e-07 blocks; surface 576 vertex-samples, 0 zero-area ignored; animation 0 rad; pos max 0 units; hidden checks 7; visual changed 0, MAE 0, contested 0.464
  model_robot1     code_driven   geometry 3e-07 blocks; surface 7128 vertex-samples, 0 zero-area ignored; animation 0 rad; pos max 0 units; hidden checks 10; visual changed 0, MAE 0, contested 0.000549
  model_robot5     code_driven   geometry 2e-07 blocks; surface 1320 vertex-samples, 0 zero-area ignored; animation 0 rad; pos max 0 units; hidden checks 4; visual changed 0, MAE 0, contested 0.0116
  model_robot2     entity_state  geometry 1e-06 blocks; surface 7560 vertex-samples, 0 zero-area ignored; animation 0 rad; pos max 0 units; states 5; hidden checks 20; visual changed 0, MAE 0, contested 0
  model_robot3     entity_state  geometry 1e-06 blocks; surface 5928 vertex-samples, 0 zero-area ignored; animation 0 rad; pos max 0 units; states 3; hidden checks 12; visual changed 0, MAE 0, contested 0.0025
  model_robot4     entity_state  geometry 3.7e-07 blocks; surface 17472 vertex-samples, 0 zero-area ignored; animation 0 rad; pos max 0 units; states 2; hidden checks 12; visual changed 0, MAE 0, contested 0.000412
  model_rockbase   entity_state  geometry 2e-07 blocks; surface 2064 vertex-samples, 0 zero-area ignored; animation 0 rad; pos max 0 units; states 14; hidden checks 14; visual changed 0, MAE 0, contested 0
  model_rotator    entity_state  geometry 1.57e-07 blocks; surface 7488 vertex-samples, 0 zero-area ignored; animation 4.1e-08 rad; pos max 0 units; states 6; hidden checks 12; visual changed 0, MAE 0, contested 0; composition 312 draws over 13 captures: instance pose linear 0 / translation 0 units, draw pose linear 0 / translation 2.4e-08 units
  model_purplepower code_driven  geometry 1.74e-07 blocks; surface 3888 vertex-samples, 0 zero-area ignored; animation 4.1e-08 rad; pos max 0 units; hidden checks 8; visual changed 0, MAE 0, contested 0.106; composition 162 draws over 9 captures: instance pose linear 0 / translation 0 units, draw pose linear 1.5e-07 / translation 0 units
  fixture_runtime_basis_yz fixture       geometry 2.77e-07 blocks; surface 336 posed vertex-samples exact; animation 0 rad; pos max 0 units over 42 channels
  (the 12 landed entries are byte-identical to the checked-in proof: every
  compiled dump, generated file, geo-render dump, reference leg, report entry
  and PNG; 73 -> 85 text files, 171 -> 201 PNGs are the two new models)

CONTESTED FRACTIONS (ruling 2, pinned per species): Rotator 0 on all five
captures (the blades of a fan overlap with coplanar faces, but rotator.png is
the same colour on the coplanar-overlapping ±z faces of adjacent blades — shape2's east end cap is (255,0,0) against
(251,0,0) elsewhere but never coplanar-overlaps another blade (refuter A's per-face scan) — so the contested rule, which
flags same-depth fragments of a DIFFERENT colour, finds nothing);
PurplePower 0.1058502197265625 at bind (age 0: all three rings lie in the same
one-pixel slab through the origin) and 0.0306 at t_quarter, 0 at the three
small ages - pinned at the maximum; above 0.5%, so PurplePower joins Island,
IslandToo and Robot5 in needing the owner's in-game acceptance (PENDING_OWNER).

ENT-S-146. Still REPORT when this landed, so the candidate proves the classic
as it stands. Re-basing after the fix touches: the classic model (the seeded
per-frame rolls through a `PurplePowerPose.getRandom()`, the 60-degree step as
the spoke's own zRot), the manifest entry (`entity_state`, seeds; `step_scope`
becomes `part` with the same expansion), the hook (three group spins from the
rolls, the accumulating X / Y reproduced), and the render state (translucent /
fullbright is a renderer change with the visual leg's cutout-only rasteriser
presented before its gate). The expansion and the composition leg are the same
either way.

OWNER LOOK: `-Dorespawn.dev.geckolibRenderers=rotator,purple_power` (or
`candidate`). Property values to check in-game: the Rotator's three rings turn
about X, Y, Z at 2 degrees per frame and the ball stays centred; PurplePower's
three spoke rings turn at their three rates; the purple type textures (0, 1, 2,
3, 10) follow `getPurpleType()`. Not pushed.

GATE: see this section's closing GATE line (4c, 2026-09-06 03:40-03:44, 1199 required tests passed) — this placeholder was the lane draft's copy before the refuter notes, filled 2026-09-06.

REFUTER A NOTES (2026-09-06, all non-blocking, applied or disclosed): (A1) the 4.1e-8 rad animation-leg maxima on both
models are a serialisation artefact — Python's exact binary32 value of float32 π in `conversion.json`
(3.1415927410125732) against Java's `Float.toString` shortest repr in the geo dump (3.1415927) — not the float32-accumulated
step against the JSON round trip (every emitted angle round-trips to the identical float32); the clone / static-group
comparisons therefore carry a noise floor of up to half a float32 ulp of the angle (≤ 2.4e-7 rad below 2π), inside 2e-6.
(A2) PurplePower's manifest entry gained `in_game_acceptance: PENDING_OWNER …` in Island's shape. (A3) latent, no model
affected: a HIDDEN expanded part would trip the composition leg (`sorted(seen) != clone_bones`) while the animation leg
collapses hidden clone names lossily — neither Rotator nor PurplePower hides anything; recorded as a limitation. (A4) the
animation leg's clone / static-group check compares the manifest's step (through the converter and the GeckoLib round
trip) against itself — it proves the round trip; the classic-vs-candidate content for the step lives in the geometry,
surface and composition legs (a wrong step or axis fails there); the float32 emulation's fidelity (≤ 2.98e-7 rad at k = 7)
sits below every epsilon and is verified by computation, not by the harness. (A5) the composition leg's docstring bound
corrected (entry error → angle error only up to √3; effective angular tolerance ≤ ~3.5e-6 rad; no threshold changed).
(A6) above. (A7) the converter's expanded-name drift check is at `tools/layer_definition_to_geo.py` :391-399.
(A8) 3600 ticks is the exact common period in integer arithmetic (3600 × 7.3 / 5.1 / 3.7 = 73 / 51 / 37 × 360); in the
float32 pipeline the loop closes ~1.2e-5 rad short of 73·2π at t_end (nothing asserts closure; both sides compute the same
float); t_half / t_end map the 6-fold rings onto themselves, so the informative samples are the small ages and t_quarter.
(A9) pre-existing, outside this slice: `minimum_observed_foreground_fraction` in `g1_render_parity.py` is always 1.0 — the
`min_foreground = min(...)` update at :1400 is dead code after the raise at :1396-1399; the threshold check itself works.

REFUTER B NOTES (2026-09-06, no blocking defect): (B1 = A2) the manifest's PENDING_OWNER field added. (B2, FILED as
ENT-S-147, REPORT) the candidate's fan advance rides GeckoLib's per-frame animation dedup (`handleAnimations` returns at 170
before `setCustomAnimations` when `tickCount + partialTick == lastUpdateTime` for the same instance): with one Rotator in
view and the game paused the candidate's gyroscope freezes behind the pause menu where the classic and 1.7.10 spin 2° per
rendered frame (two or more in view keep spinning — instance-count-dependent); duplicate partial ticks above ~1000 FPS and a
same-frame shadow pass are skipped the same way; the lane's invisible-entity edge (the candidate advances where vanilla's
`LivingEntityRenderer.render` skips the draw and so the classic advance) joins the same record. Fix shape in the entry
(a per-render-pass descriptor hook for the advance, the pose kept pure, the pose re-applied when the pass is deduped);
not changed here — renderer plumbing, two refuters, the owner's ruling; the candidate is behind the dev switch and its
javadoc cites the entry. (B3) `tools/reference_renderer_pins.json` named no candidate for the two species — now
`RotatorGeoReplacement.java` / `PurplePowerGeoReplacement.java`, so the reference renderer gate pins their shadow / scale
like every landed candidate. (B4) the ENT-S-146 re-basing note completed (ProbeSubject's interface, the face-order item
under translucency, the reference leg's re-measurement, the pins entry). (B5 = A6.) (B6) the period reasoning: 3600 holds
because 73 and 37 are coprime with 3600 (gcd(51, 3600) = 3: the 5.1 rate alone closes at 1200) — the manifest's
`period_note` corrected. (B7) one record for both edges: ENT-S-147. (B8) two line references corrected in records.md.
Upheld: the classic Rotator unchanged statement by statement (`Axis.rotationDegrees` = `fmul 0.017453292f` then
`rotation(F)` — `fanSpinRadians` feeds the identical float; orig :76's `(double) rf1 > 359.0` identical); the hook's
composition and basis (proven numerically: instance-pose 0 / 0, draw-pose 0 / 2.4e-8 over 312 draws); MHLib's collector
layer triggers no second `handleAnimations`; the PurplePower hook's angles / axes / rotation order (vanilla
`rotationZYX` = GeckoLib Z·Y·X; conjugation through S = diag(1, −1, 1) gives the helpers' (−x, y, −z)); texture by type
(the five textures, orig RenderPurplePower.java:46-62); the wiring (species ids, lambda suppliers, registry-free
constructors, the renderer-constructor cache registration, living render mode, shadows 0.1 / 0.825 against the pins);
the shipped resources byte-identical to the converter output with the float32 clone angles; the pins the maxima.

GATE: (4c, 2026-09-06 03:40-03:44, after both refuters' items): proofs regenerated by hand under the proof rule — s4 `G1 PARITY PASS: 13 models; checked-in proof updated` (model_rotator and model_purplepower added; every landed entry byte-identical, verified by the lane and refuter A), g1 `G1 PARITY PASS: 2 models; checked-in proof verified` (no drift), the benchmark proof rewritten for the g1tool class-directory pin (`G1 BENCHMARK EVIDENCE VERIFIED: SMOKE_ONLY / COMPONENT_PROXY_ONLY / PENDING_LIVE_PRECUTOVER; checked-in proof updated`); then the gate: drift check clean; `build` exit 0 — asset audit `RESULT: 0 error(s), 0 advisory(ies), 4 acknowledged -> exit 0` (the four new resources staged), referenceRenderers `PASS 120, PENDING 0, MOD 0, NOT_APPLICABLE 13, DIVERGES 0` with both candidates named, referenceGeometry `G1 PARITY PASS: 2 models`, s4Parity `G1 PARITY PASS: 13 models; checked-in proof verified`; `runGameTestServer` exit 0 — literal `All 1199 required tests passed` (no new gametests: the harness is the proof).

Refuted twice (harness + renderer, 18 files): A (the harness) — no blocking defect; nine non-blocking items applied or disclosed (the 4.1e-8 rad maxima a Python-double-vs-Java-float serialisation artefact; the PENDING_OWNER manifest field; hidden expanded parts unsupported by the composition leg, latent; the clone / static-group step check self-referential, the other legs carry the content; the composition docstring bound corrected; the rotator.png faces; a line reference; the period wording; a pre-existing dead line in the visual leg's foreground minimum); byte identity of all 12 landed entries re-run and upheld. B (the candidates and the classic) — no blocking defect; eight items: the PENDING_OWNER field; the candidate's fan advance rides GeckoLib's per-frame animation dedup (the gyroscope freezes behind the pause screen with one Rotator in view; duplicate partial ticks and shadow passes skipped) with the lane's invisible-entity edge — FILED as ENT-S-147 (REPORT, a fix shape, the owner's ruling); the renderer pins now name both candidates; the ENT-S-146 re-basing note completed; the faces; the period reasoning; two line references. Upheld: the classic Rotator unchanged statement by statement, the hook's composition and basis, the PurplePower hook's angles / axes / rotation order, texture by type, the wiring, the shipped resources byte-identical to the converter output.

## PHASE G — G2 ROOT-ORDER CONTRACT (2026-09-06) — bone draw order = vanilla part order; the z-fight exclusion retired (presented)

SCOPE. Owner ruling 2026-09-05, scope addendum item 23 (8)(c): its own gated slice,
the before/after per species presented BEFORE its gate. GeckoLib's bone draw order
is made equal to the classic renderer's part draw order for every landed candidate
(Beaver, Elevator, Vortex, Coin, Island, IslandToo, Robot1-5, RockBase, Rotator,
PurplePower), proven from the 4.8.4 and 21.1.223 bytecode and by the harness, so
the visual leg's z-fight exclusion (ruling 2, 2026-09-02) can retire. Nothing
player-visible changes for the default (classic) renderers; the candidates stay
behind `-Dorespawn.dev.geckolibRenderers`. No threshold, epsilon or pin changed;
the retirement is PRESENTED here and the pins are removed by the orchestrator on the
owner's word. MHLib untouched; the renderer path (the shared replacement model) is
touched, so two refuters apply.

THE TWO ORDERS (javap re-run this slice; offsets quoted):
- NeoForge 21.1.223 `PartDefinition.<init>`: `Maps.newHashMap` (offsets 5-8) holds
  `children`; `PartDefinition.bake(int,int)`: `children.entrySet().stream()` (1-9)
  collected by `Collectors.toMap(.., Object2ObjectArrayMap::new)` (36-44) into
  `ModelPart.children`. `ModelPart.render(PoseStack,VertexConsumer,int,int,int)`:
  `visible` test (1-4), `pushPose` (32), `translateAndRotate` (37), `skipDraw`
  gating `compile` (41-58), `children.values().iterator()` (62-70) each through
  `render` (108), `popPose` (115); `compile` draws the cubes in list order (4-45). So
  a tree drawn through `root.render` draws in the HashMap order of the child NAMES -
  but EVERY landed classic model overrides `renderToBuffer` and draws its parts by
  explicit `part.render(...)` calls (ModelElevator:82-86, VortexModel:37,
  ModelCoin:54, ModelIsland:76-78, ModelIslandToo:46-48, ModelRobot1:231-257,
  ModelRobot2:192-206, ModelRobot3:203-221, ModelRobot4:536-591, ModelRobot5:123-133,
  ModelRockBase:250-271, RotatorModel:98-102 through renderFan:124-131,
  ModelPurplePower:60-77, ModelBeaver:106-114; both harness fixtures draw
  `parent.render`), so on the classic side the order a player sees is the code's.
- GeckoLib 4.8.4 `GeometryTree.fromModel`: top-level bones in an
  `Object2ObjectOpenHashMap` (0-4), each bone's children in another
  (`BoneStructure.<init>(Bone)` 2-9); `BakedModelFactory$Builtin.constructGeoModel`:
  `new ObjectArrayList` (0-4), `topLevelBones().values()` iterated (9-17) and added
  (55), the list handed to the `BakedGeoModel` record (64-73; stored by `putfield` at
  6); `constructBone`: `BoneStructure.children().values()` (175-183) added to
  `GeoBone.getChildBones()` (214-226), the `ObjectArrayList` of `GeoBone.<init>`
  (5-12). `GeoRenderer.actuallyRender` iterates `topLevelBones()` (29-33,
  `renderRecursively` at 83); `renderRecursively`: `renderCubesOfBone` (36), then
  `renderChildBones` (92); `renderChildBones`: `getChildBones()` (8-12) each through
  `renderRecursively` (62). So GeckoLib's order is a pre-order traversal in
  fastutil's open-hash order of the bone names; JSON order never survives the load.
  Twelve of the fourteen rigs had a GeckoLib order different from the classic one
  (Vortex and Coin are single-bone rigs).
- Both sides traverse in pre-order (a parent's cubes, then each child's subtree),
  so equal sibling orders are equal draw orders: the contract is a sort of the
  factory's lists. Nothing copies them defensively: `BakedGeoModel.topLevelBones()`
  returns its field (0-4, a record accessor), `GeoBone.getChildBones()` returns its
  field (0-4); fastutil 8.5.12 `ObjectArrayList.sort(Comparator)` sorts in place.

THE CAPTURE (probe, compiled side). `G1ModelProbe.DrawOrderObserver` (:901-1076)
records, for every full capture, the ACTUAL classic draw order: every
`ModelPart.render` call is one push of the Slice 4c instrumented pose stack, and
the draws are attributed to parts by ELIMINATION - one `renderToBuffer` per
cube-bearing part with every other part's public `ModelPart.skipDraw` set (1.21.1:
`public boolean skipDraw`; it skips `compile` at 41-58 but keeps the push, so the
serials are identical run to run), so no UV or geometry uniqueness is assumed and
superimposed parts (Robot5's wheels at bind, RockBase's 22 at bind) are attributed
exactly. The observation runs on a SECOND bake of the same LayerDefinition, posed
identically per sample (a fresh `ProbeSubject` for entity states: seeded rolls
evolve identically), so the extra calls never touch the captured model - the
Rotator advances `rf1` inside `renderToBuffer` and `subject_after` pins that
advance. A part drawn n > 1 times is named `<part>__i<k>` by draw ordinal, the 4c
names, and cross-checked against the UV attribution (`draws`). Written per sample
as `draw_order` plus a top-level `draw_order_source`; every compiled dump is
byte-identical apart from those two fields (17 entries). The design's case (i) -
keeping the baked `Object2ObjectArrayMap` order for `root.visit`-drawn models - is
not a separate path: the same observation reports the map order whenever a model
draws through `root.render`, and none of the fourteen does (deviation, records.md).
The reference manifest's 101 dumps gain the fields too; the referenceGeometry gate
verified its 101 checked-in reports unchanged (proof dir untouched).

THE CONTRACT (converter + geo). `tools/layer_definition_to_geo.py
derive_bone_draw_order` (:343-458) merges EVERY full capture's sequence into one
total order over the cube-bearing geo bones (a DAG; a cycle would be a FINDING - a
state-dependent order no static bone order expresses; RockBase's 15 captures, 7
distinct subsequences of its 22-part order, merge cleanly), lifts it onto the geo
bone tree as a pre-order listing with siblings ordered by their subtree's first
classic draw (a subtree interleaved with another's, or a bone drawn after a
descendant, would be a FINDING; pairs never drawn together in any capture fall to
the converter's emission order - the tie-break; no landed model has an unobserved
unit), and writes it as `orespawn:bone_draw_order` INSIDE the geo's `description`
(:525-534), also into `<model>.conversion.json` as `bone_draw_order` +
`draw_order_evidence`. For the 4c expansions the clones come in ordinal order under
their groups where the part fell: Rotator `shape1__fan, shape1__i0..7, shape2__fan,
...` (scope part: the clones must be contiguous, and are); PurplePower
`Shape1__fan0, Shape1__i0, Shape1__fan1, Shape1__i1, ...` (scope stack: one group
per draw at that draw's position). Why inside the geo and not a
`geo/entity/<species>.draworder.json` sidecar: `GeckoLibCache.lambda$loadResources$6`
lists every `.json` under `geo` (offsets 0-9) and `lambda$loadModels$5` bakes each
as a model (94-110, `GeometryTree.fromModel` indexing `minecraftGeometry()[0]`), so
a sidecar there would fail the resource reload; whereas `MinecraftGeometry
.lambda$deserializer$0` reads only `bones` / `cape` / `description` and
`ModelProperties.lambda$deserializer$0` its seventeen named keys, so the key is
ignored by the loader. The rig and its order are one resource, the descriptor's
`modelResource` names it, and the asset audit's shipped-vs-proof byte check covers
it with no new rule. The elevator's geo is byte-identical between the g1 and s4
converter runs. Every generated animation / animation-contract file is
byte-identical to the checked-in proof (17 + 17); every geo differs by the one key,
every conversion.json by `bone_draw_order`, `draw_order_evidence` and the two
provenance hashes.

THE PRODUCTION REORDER. `DrawOrder` (entity/client, new, :1-166): `read(JsonObject)`
(the key, validated: strings, unique, non-empty), `load(ResourceManager,
ResourceLocation)`, `apply(BakedGeoModel, List)` - sorts `topLevelBones()` and every
`getChildBones()` in place by rank, every rig bone must be in the order and every
name in the order must be a rig bone (a drifted rig fails loudly), idempotent
(sorting a sorted list) - and `traversal(BakedGeoModel)`.
`OreSpawnGeoReplacementModel.getBakedModel` (:36-61) overrides GeckoLib's: `super`
fetches `GeckoLibCache.getBakedModels().get(location)` (0-9), throws when absent
(13-42), re-registers the processor's bones on a new instance (43-61);
`GeoRenderer.defaultRender` calls it on every draw (46-49). The override applies
the order when the returned instance is not the one it last ordered, then remembers
it - once per bake, before the first draw; `GeckoLibCache.reload` bakes everything
again into fresh maps (0-16; `lambda$loadModels$5` constructs each `BakedGeoModel`
anew) and `lambda$reload$0` swaps `MODELS` (4-5), so after a resource reload the
identity changes and the reorder runs again; a second model instance over the same
cached bake re-sorts a sorted list. The order is read from the geo resource through
`Minecraft.getInstance().getResourceManager()` - the manager GeckoLib loaded from.
The probe's every fresh bake goes through the SAME static (`S4CandidateRuntime
.freshBaked` :53-62, `G1AnimationRuntime.Evaluator.freshBaked` :55-59, and the
benchmark's evaluator :735-738), never a re-implementation; the geo dumps record
`bone_draw_order` (read) and `baked_bone_order` (the traversal after apply) and
the parity tool asserts them equal. The queen (`QueenModel`, its own GeoModel) is
outside the seam and carries no key. Headless smoke (scratch, a `ResourceManager`
stub over the SHIPPED assets dir): all 14 species load through `DrawOrder.load`,
`apply` is idempotent, 12 hash orders differed; an order missing a rig bone, an
order naming a bone the rig lacks, a geo without the key (the queen) and a missing
resource all fail loudly. The `getBakedModel` override itself needs
`Minecraft.getInstance()` and is proven by reading (refuter focus).

THE HARNESS. `tools/g1_render_parity.py draw_order_parity` (:309-368), a fourth
mechanical leg: the geo's key == the converter's `bone_draw_order` == the probe's
`bone_draw_order` == the fresh bake's `baked_bone_order` (a permutation of the
exact bone names), and per full capture the classic `draw_order` == the GeckoLib
`draw_order` (the bones `GeoRenderer` emitted cubes for, :1497-1540 in the probe).
`--no-contested-exclusion` (:1731-1734): `visual_parity` passes no exclusion mask to
`pixel_diff` (:1413-1414), reports `contested_exclusion_applied` /
`contested_excluded` and keeps the contested fraction as a diagnostic; a pin, if the
manifest still carries one, is still checked (:1462-1476) and is otherwise optional.
Nothing else in the visual leg changed: every per-capture row is identical to the
checked-in proof's (minus the new field) in both variants.

EVIDENCE (scratch runs, both manifests, both variants; all exit 0):
  s4 (13 models + fixture): `G1 DRAW ORDER PASS` for all 14 entries (142 captures,
  2,307 draws); `G1 PARITY PASS: 13 models` with the exclusion on AND with
  `--no-contested-exclusion` - every species `max changed 0, max MAE 0` except
  Vortex's accepted 0.000137 / 0.0042 boundary residual (contested 0, both runs).
  g1 (2 models + fixture): `G1 DRAW ORDER PASS` for all 3 (28 captures, 221 draws);
  `G1 PARITY PASS: 2 models`, both variants, changed 0 / MAE 0.
  Contested fractions unchanged by the reorder (the mask is order-independent):
  every pin met exactly in every run.

BEFORE / AFTER, every species (the landed proofs' captures = GeckoLib's own order,
versus the bake sorted into the classic order; "excl" is the ruling-2 exclusion;
all numbers through the tool's own render_capture / pixel_diff):
  proof model              pin(excluded)  BEFORE excl-on chg/MAE  BEFORE excl-OFF chg/MAE   AFTER excl-on chg/MAE  AFTER excl-OFF chg/MAE  worst-after-off
  s4    model_elevator     0.000000       0.000000  0.0000        0.000000  0.0000          0.000000  0.0000       0.000000  0.0000      -
  s4    model_vortex       0.000000       0.000137  0.0042        0.000137  0.0042          0.000137  0.0042       0.000137  0.0042      bind.front
  s4    model_coin         0.000000       0.000000  0.0000        0.000000  0.0000          0.000000  0.0000       0.000000  0.0000      -
  s4    model_island       0.464233       0.000000  0.0000        0.225342 37.7072          0.000000  0.0000       0.000000  0.0000      -
  s4    model_islandtoo    0.464233       0.000000  0.0000        0.225342 15.6237          0.000000  0.0000       0.000000  0.0000      -
  s4    model_robot1       0.000549       0.000000  0.0000        0.000549  0.0822          0.000000  0.0000       0.000000  0.0000      -
  s4    model_robot5       0.011612       0.000000  0.0000        0.005615  0.5896          0.000000  0.0000       0.000000  0.0000      -
  s4    model_robot2       0.000000       0.000000  0.0000        0.000000  0.0000          0.000000  0.0000       0.000000  0.0000      -
  s4    model_robot3       0.002502       0.000000  0.0000        0.000000  0.0000          0.000000  0.0000       0.000000  0.0000      -
  s4    model_robot4       0.000412       0.000000  0.0000        0.000000  0.0000          0.000000  0.0000       0.000000  0.0000      -
  s4    model_rockbase     0.000000       0.000000  0.0000        0.000000  0.0000          0.000000  0.0000       0.000000  0.0000      -
  s4    model_rotator      0.000000       0.000000  0.0000        0.000000  0.0000          0.000000  0.0000       0.000000  0.0000      -
  s4    model_purplepower  0.105850       0.000000  0.0000        0.052643  3.1211          0.000000  0.0000       0.000000  0.0000      -
  g1    model_elevator     0.000000       0.000000  0.0000        0.000000  0.0000          0.000000  0.0000       0.000000  0.0000      -
  g1    model_beaver       0.000015       0.000000  0.0000        0.000015  0.0034          0.000000  0.0000       0.000000  0.0000      -
The BEFORE excl-OFF column is what the exclusion had been hiding (Island 22.5% of
the image at MAE 37.7); AFTER excl-OFF is 0 / 0 for every species: every previously
contested pixel resolves identically on both sides. No species is left over - no
FINDING. For a player: nothing changes unless a rig self-overlaps; Island /
IslandToo, PurplePower, Robot5, Robot3, Robot1, Robot4 and Beaver now resolve their
coplanar faces exactly as the classic renderer does.

BYTE IDENTITY (scratch vs the checked-in proofs, both variants): the geometry,
surface-mapping, animation, reference-animation, reference-geometry and fixture
legs are identical for all 17 entries; every vanilla PNG identical (67 + 10); the
geo PNGs of the contested captures differ (9 s4 + 1 g1: the candidate now shows the
classic winner), the rest identical; diff PNGs identical with the exclusion on
(the blue paint), 14 + 1 differ with it off (no paint). The probe's javac recipe
reproduces every `source_class_sha256` pin (17 of 17). Not changed: the
rasteriser's tie rule (a fragment within 1e-6 wins only when nearer by more than
1e-9, i.e. first-wins at exact ties, where GL's LEQUAL is last-wins) - parity is
unaffected because both sides share the rule and now the order; recorded as an
observation, not this slice's change.

PINS PROPOSED FOR REMOVAL (the owner's word; the orchestrator lands them): every
`max_contested_fraction_pin` in tools/s4_model_proofs.json (13) and
tools/g1_model_proofs.json (2), and the four `in_game_acceptance: PENDING_OWNER`
entries (island, islandtoo, robot5, purplepower) whose condition - an excluded
fraction above 0.5% - is vacuous once nothing is excluded. The landing needs
`--no-contested-exclusion` on both parity commands in build.gradle (g1Parity
:220-232 (commandLine :225-231), s4Parity :288-302 (commandLine :294-301)) or the tool's default flipped, then the proofs
regenerated (s4 13 models, g1 2 models, `--write-proof`), `g1Benchmark` re-run and
its proof re-pinned (source provenance now drifts on G1PerformanceBenchmark.java
and G1AnimationRuntime.java, the model-input hashes and the g1tool class-dir pin,
as after 4c). Until the proofs regenerate the asset audit reports
`GECKO_GEO_PROOF_DRIFT` for the 14 shipped rigs (15 findings: elevator against both
proofs) and nothing else (0 advisories, 4 acknowledged).

SHIPPED: the 14 converter outputs copied over `geo/entity/{beaver, elevator, vortex,
coin, island, islandtoo, robot1-5, rockbase, rotator, purplepower}.geo.json`
(byte-identical to the scratch proof's generated files; the previous shipped files
were byte-identical to the previous generated ones, checked before the copy).

GATE: NOT RUN — presented (2026-09-06) for the owner's look before its gate, as ruled; the code is parked on branch `g2-root-order` (c5e9af1), refuted twice (no blocking defect; the pre-order assertion and the evidence guard applied after the refutation and re-verified: javac clean, the s4 geo probe through the new `DrawOrder.apply` byte-identical for all 14 rigs, `G1 PARITY PASS: 13 models` against the slice's scratch proof). It gates and lands on the owner's word with the pin removals; the owner-facing table: `phase_g_reports/g2_root_order_before_after_2026-09-06.md`. Wave 4 proceeds meanwhile (the sequencing ruling: a batch gates while a chain slice waits on the owner).

REFUTER A NOTES (2026-09-06, the production side and the loader; no blocking defect): (A1) the production seam has no
executed evidence in the harness (the probe poses through `setActiveModel`; the smoke drives `DrawOrder` over a stub) —
closed by the refuter's bytecode trace (the 1-arg `getBakedModel` the override declares is what `GeoRenderer.defaultRender`
32-49 calls; the sorted instance feeds every consumer; the sort runs on the render thread inside the first call of a bake)
and by the owner's in-game pass before any candidate becomes the default. (A2, OPEN for the owner) a rig WITHOUT the key
crashes the client on its first draw (a Blockbench re-export in a resource pack drops the key); recommended: loud when the
key is present and wrong, a logged fallback to GeckoLib's order when it is absent — not changed. (A3, applied) `apply`
now asserts the order is a pre-order of the rig (the sorted traversal equals the shipped list). (A4, latent) MHLib's
`GlibModelEnforcementManager` puts server-synced bakes into the cache outside `reload`; inert (every profile declares no
models). (A5, theoretical) a frame between the two apply-phase tasks with a bone-set-changing pack. (A6, applied) three
line references; the executed traversal on this path is `GeoReplacedEntityRenderer.renderRecursively` (an override) with
the interface `renderChildBones` — the conclusion unchanged; the BEFORE dumps' provenance (`build/{s4,g1}`, predating G2,
reproducing the checked-in report's numbers exactly).

REFUTER B NOTES (2026-09-06, the harness side; no blocking defect): (B1, recorded) the converter's DAG merge tie-breaks
by emission order and tests tree-expressibility afterwards, so a tree-expressible pair of captures can be reported as a
spurious FINDING for a NESTED rig (siblings X, Y with Z under Y; captures [X, Z] and [Y]; the valid pre-order [X, Y, Z]
exists) — unreachable for the 14 flat landed rigs, loud not silent, a limitation for future nested rigs. (B2, applied) the
draw-order leg passed vacuously with zero full captures or with units no capture drew (the shipped order then rests on the
emission tie-break) — it now fails `DRAW ORDER UNEVIDENCED`; every one of the 17 entries has `unobserved_units = []` and
full pair coverage (robot4 1540/1540, rockbase 231/231), so no result changes. (B3, recorded beside observation 7) within a
CUBE the face emission order differs on every model — classic down/up/west/north/east/south, GeckoLib
west/east/north/south/down/up — harmless for non-flat cubes (only a zero-thickness box emits two coplanar faces of one
cube; Vortex, single-part, contested 0); a flat box in a multi-part rig would expose the first-wins rasteriser to a
false alarm or to blindness against the game's LEQUAL last-wins — the face-order open item of
`OreSpawnGeoReplacedEntityRenderer`'s javadoc, still open. (B4, applied) the gradle line references; robot4's draws
:536-591; the geo-render dumps also differ by `geometry_sha256` (the geo changed) and their `cubes` groups are NOT
reordered (only `render_vertices`); `drawOrderJson()` :1504-1506; the Island / IslandToo pin 0.464233398438 is a 12-digit
rounding of the measured 0.4642333984375 (pre-existing; "met", not equal). (B5, for the landing) the manifests'
`model_robot5.visual_note` / `model_robot2.visual_note` cite ruling 2 for the bind-sample exclusion — still meaningful
(bind is not a visual sample) — to re-read when the four `in_game_acceptance` strings go. Upheld by the refuter's own runs:
`skipDraw` elimination sound (ModelPart.render: `visible` 1-4, push 32, `skipDraw` 41 → `compile` 58, children 62-70,
child render 108, pop 115; all 14 classic models draw only through `part.render`, all flat); the derivation's synthetic
tests (conflicts, cycles, child-before-parent, interleaving all FINDINGs; clones and superset captures merge cleanly); the
leg observes GeckoLib's real default traversal on the fresh bake after `DrawOrder.apply` and CATCHES a wrong shipped
order (beaver's top level reversed in a scratch copy: the four-way equality passed, the per-capture leg failed
`DRAW ORDER MISMATCH`); the switch changes only `contested_exclusion_applied`, `contested_excluded` and `z_fight_policy`;
the BEFORE numbers re-measured independently (Island 0.225342 / 37.7072, Robot5 0.005615 / 0.5896, PurplePower
0.052643 / 3.1211, Beaver 0.000015 / 0.0034; AFTER 0 / 0 everywhere); byte identity: 221 identical / 39 differing files
against the checked-in s4 proof, each difference exactly the expected field or the 9 contested geo PNGs.

## TARGETING WAVE 4 (2026-09-06) — the companions' goal order and state gates, the robots' forget-before-read, the Stinky's owner flight boxes, five flight-cell casts, the Knight's wet teleport, the Luna Moth's own loop, the Ender pair's portal sound (the attacking speed boost transcribed and HELD), the Chainsaw sweep's 1.7.10 walk under the modern key

- **ENT-S-130 / 133 / 134 / 137 / 138 / 142 / 143 / 144 and ITEM-070 (B2) — FIXED (2026-09-06, wave 4); ENT-S-145 — HELD (2026-09-06, wave 4).** Owner ruling 2026-09-05 (scope addendum item 23 (6)-(7)): one batch, parity, classic; generated pins; refuters by files touched; one changelog paragraph. Twenty port files (nineteen edited, one rewritten), two new generated test classes, six test classes re-based. Every site at the orig position with the orig polarity, term order and roll bounds; the `(int)` casts kept (BUG-027 / MOD-024); the modern key of ITEM-070 the wave's one `[modern]` addition; the ENT-S-145 hunks withdrawn before the gate (below).
  - **ENT-S-130 (the companions' target-goal order):** EntityLeon.java:176 the revenge goal @4, :197 the IMob hunt @3 (orig Leon.java:93's @1 over :95's @2 — a monster in sight displaces the revenge target; both engines let a strictly lower-numbered target goal pre-empt a running higher one), the modern-only owner pair keeping @1 / @2; Girlfriend.java:264 the IMob hunt @3 (orig :167), ahead of Jealousy @4 / @5 — the modern-only owner pair moved @3 / @4 → @1 / @2 (Girlfriend.java:229-230), the Leon precedent applied to keep MOD-033's promise: at @3 the pair tied with the hunt, `WrappedGoal.canBeReplacedBy` needs a strictly lower priority, so a running hunt was never pre-empted by OwnerHurtByTargetGoal and a hunting Girlfriend ignored her owner being hit for the hunt's duration (the wave-4 refuter A); at @1 / @2 it is strictly ahead of both hunts and the Jealousy tasks and ties with the two Valentine tasks (orig :161-162 — run only valentine-angry; registered first, they start first on a same-pass tie); the owner may still move the pair. `PortOnlyTargetingTests` COMPANIONS' Girlfriend classic list `3:MyEntityAINearestAttackableTargetGoal<Mob>`, the modern-only list `1:OwnerHurtByTargetGoal` / `2:OwnerHurtTargetGoal`; the `IMobConventionTests` port descriptions (Leon @3, Girlfriend @3).
  - **ENT-S-133 (the robots' forget):** Robot2.java:214-215, Robot3.java:147-148, Robot4.java:195-196, Robot5.java:134-135 — the 1-in-50 (with the ENT-S-131 `revengeGoal.release()`) rolled BEFORE `target = getTarget()` (orig :281-284 / :242-245 / :282-285 / :214-217): a forgotten attacker is not engaged in the forgetting pass — the read answers null, the scan refuses a Monster attacker, nothing looks, paths, poses or fires.
  - **ENT-S-134 (the Stinky's flight box):** EntityStinky.java:472-486 — orig Stinky.java:617-631's branches: `hasOwner && ownerFlying == 0` → `nextInt(4) + 6`, `hasOwner` → `nextInt(8)`, else `nextInt(5) + 6`; the target at the owner's truncated cell as before. The dice order (xdir ahead of zdir; orig rolled zdir first — every branch draws the same bound twice) upheld as ruled: noted, not changed, as the entry's resolution.
  - **ENT-S-137 (the companions' untamed / sitting gates):** Boyfriend.java:175 / :188 `if (!isTame()) return false;` (orig MyEntityAINearestAttackableTarget.java:44-46) and Girlfriend.java:249-250 / :267-268 `if (!isTame()) return false; if (isOrderedToSit()) return false;` (orig :44-49 and :50-52's `isSitting` — the Jealousy goals' `isOrderedToSit` idiom; `isInSittingPose()` would be the mechanism-faithful read, coincident on the server today) at the head of both hunts' `canUse`, ahead of the PlayNicely read (orig ran them ahead of the chance roll :53 and the scan :56); the Valentine tasks untouched (MyValentineTarget.java:47-59 carries none).
  - **ENT-S-138 (five flight-cell casts):** the four the entry names — EntityBrutalfly.java:145 / :148 (orig :172 / :174), Mothra.java:394 / :397 (orig :182 / :184) — `new BlockPos((int) x, (int) y, (int) z)` for the hunter's own cell and the near-retarget distSq read; entity/ai/AmbientFlightGoal.java:109 / :112 through the new `AmbientFlightGoal.castCell(Mob)` (:180-182; orig Dragonfly.java:122 / :124 — the base cast reaches the Dragonfly, the Firefly (orig Firefly.java:125 / :127), the Mosquito (orig EntityMosquito.java:94 / :96) and the VampireButterfly (the port's own class for orig EntityButterfly's vampire type, registered on the base goal)); entity/ai/DragonflyHuntGoal.java:91 `new BlockPos((int) x, (int) (y + 1.0), (int) z)` (orig :145-146) — and a fifth site found by the lane and closed in the same batch, presented for ratification: entity/ai/ButterflyIslandsHuntGoal.java:67 / :69, a SEPARATE site — its `tick` never reaches the base's :109 / :112; it closes orig EntityButterfly.java:152 / :154 and, through LunaMothFlightGoal (a subclass), EntityLunaMoth.java:124 / :126.
  - **ENT-S-142 (the Knight's wet teleport):** EnderKnight.java:259 `if (this.isInWaterRainOrBubble() || this.isOnFire())` — the Reaper's :116-119 line (EnderReaper.java:258).
  - **ENT-S-143 (the Luna Moth's own loop):** entity/ai/AmbientFlightGoal.java:69-75 `Params.lunaMoth()` = (10, 6, 2, 0.5, 0.68, 0.1, 0.75f, 1.0f, 100, 4.0, 25) (orig EntityLunaMoth.java:118, :126, :129, :149-151, :154); entity/ai/LunaMothFlightGoal.java rewritten (167 lines) — `pickRetarget` (the torch scan inside the retarget under `!canSeeSky`) gone, `onRetargetSkipped` (:72-89) the retarget's else branch: `!level().isDay() && nextInt(10) == 0` (orig :133 — `!isDaytime()` through the port's `isDay()` mapping), `closest` / `tx` / `ty` / `tz` reset (:134-137), the shells `for (i = 2; i < 15 && !scanIt(x, y, z, i, i, i); ++i) { if (i < 6) continue; ++i; }` on the truncated cell (:138-141), the target above the nearest torch (:142-144); `scanIt` (:97-155) orig :54-115 face for face — the ±x faces over y ± dy / z ± dz, the ±y faces, the ±z faces, the + face before the − face, a strictly nearer torch taking `closest` (HEAD's `findClosestTorch` read the ±x faces alone — a torch straight ahead on z was never found; an extension of the entry's "numbers and torch gate", flagged); `isTorch` (:161-166) TORCH + WALL_TORCH + `ModBlocks.EXTREME_TORCH` (orig :63 `Blocks.torch || OreSpawnMain.ExtremeTorch`, the AlienTorchSeekGoal mapping); entity/ai/ButterflyIslandsHuntGoal.java:74-92 — the retarget's else branch now `{ the Islands hunt if its roll and gates pass; onRetargetSkipped(); }` (orig EntityLunaMoth.java:122 ran `super.updateAITasks()` — the butterfly loop with its hunt — ahead of the moth's own loop; the hook is the base's no-op for the butterfly and Mothra). The single flight target and the one retarget roll for orig's two loops (the butterfly's private target under its own roll, the moth's under its own) stay the pre-existing ENT-S-141 extraction, disclosed under both entries.
  - **ENT-S-144 (the Ender pair's portal sound):** EnderKnight.java:308-318 / EnderReaper.java:307-317 `enderTeleportTo(x, y, z)` — the origin read ahead of the move (orig :162-164), `randomTeleport(x, y, z, true)` (orig's landing search :165-191 and the particle trail), false on a refusal (orig :188-191, nothing played), else `level().playSound(null, d3, d4, d5, SoundEvents.ENDERMAN_TELEPORT, SoundSource.HOSTILE, 1.0f, 1.0f)` (orig :203 `playSoundEffect` at the origin) and `playSound(SoundEvents.ENDERMAN_TELEPORT, 1.0f, 1.0f)` (orig :204 at the entity, now at the landing); `teleportRandomly` (:294 / :293) and `teleportToEntity` (:336 / :335) land through it, so every call site — the daylight escape, the wet / burning escape, the staring branch, the far branch, the hurt loop — plays the pair. Named apart from the engine's `Entity.teleportTo(double, double, double)` (void), which the entry's name would have clashed with. The mapping "mob.endermen.portal" → `minecraft:entity.enderman.teleport` (the event vanilla's `EnderMan.teleport` plays at both ends) is the status line's.
  - **ENT-S-145 (the attacking speed boost) — HELD:** transcribed and withdrawn before the gate. The static `AttributeModifier` (`orespawn:attacking_speed_boost`, `(double) 6.2f`, ADD_VALUE — orig :29-30's UUID modifier, operation 0, `setSaved(false)`), the `lastEntityToAttack` field and the :100-107 swap at the head of `aiStep` (between the drown line and the portal particles) were transcribed exactly in both files; the wave-4 refuter B showed the number does not carry across the movers: 1.7.10's legacy mover saturated it — `EntityCreature.updateEntityActionState` wrote the attribute into `moveForward`, and `moveFlying` normalised the input to length ≤ 1 and scaled it by a non-AI-enabled mob's fixed 0.1f `getAIMoveSpeed` — to a ≈3.1× sprint (≈1.4 → ≈4.4 blocks/s), while 1.21.1's mover (`MoveControl.tick` → `Mob.setSpeed(attribute)`, `getFrictionInfluencedSpeed`, `getInputVector` scaling by the unclamped speed) would accelerate the same 6.2 at ≈6.52 blocks/tick toward ≈14 blocks/tick. The register's resolution reserved the value choice; the owner rules between a literal 6.2 (≈14 blocks/tick under the modern mover), the value that reproduces the saturated ≈3.1× (ADD_VALUE ≈ 0.67 on the 0.32 base, or MULTIPLY_BASE ≈ 2.1), vanilla's EnderMan re-tune 0.15, and the pre-existing question of every legacy-AI species' base speed under the modern mover — HEAD's un-boosted Knight already moves at ≈4.4 blocks/s, 1.7.10's boosted pace (ENT-S-150, filed). The hunks are out of the tree (EnderKnight.java / EnderReaper.java carry ENT-S-142 / 144 alone — the imports, the statics and the aiStep block removed), the s145 pins withdrawn (28 rows), the MiscTargetingParityTests `spawnLive` re-base reverted (the file at HEAD).
  - **ITEM-070 B2 (the Chainsaw sweep's sight) — MOD-037:** item/Chainsaw.java:167-207 `static boolean myCanSee(Player player, LivingEntity e)` — orig UltimateSword.java:198-246 in floats: `nblks = 10` (:199), the start at the player's x / z and `getY() + 1.4f` (:200-204 — the SERVER player's posY, the feet: the ENT-S-120 premise check), the tenth-part steps to `e.getY() + e.getBbHeight() / 2` (:205-207), the three normalisation blocks with `nblks = (int) (nblks * |c|)` and the ±1 clamps (:208-240), the loop pre-incrementing and reading `getBlockState(new BlockPos((int) startx, (int) starty, (int) startz))` (:241-242 — the `(int)` casts, BUG-027 kept: at x or z &lt; 0 the column toward the origin; at y &lt; 0 — the modern world reaches −64 — the cell above the true cell on a fractional negative y, and void air (`isAir()` true) below the world's bottom as 1.7.10's `getBlock` answered air outside 0..255, documented at the method), `state.isAir()` alone passing (:243 — `== Blocks.air` by identity; cave and void air fold in, the TheQueen shape), false otherwise (:244), true past the last sample (:246); the argument order the port's `(player, e)` (orig `(e, player)`). item/Chainsaw.java:140-144 `isSuitableTarget`: `OreSpawnConfig.chainsawSweepVanillaSight() ? player.hasLineOfSight(target) : myCanSee(player, target)` — read live per swing. OreSpawnConfig.java:298-309 the field javadoc and `MODERN_CHAINSAW_SWEEP_VANILLA_SIGHT`, :590-604 the `[modern]` define `chainsawSweepVanillaSight` (default true, the MOD-029 / MOD-031 shape) with its key comment, :685-695 the helper `chainsawSweepVanillaSight()` = `MODERN_ENABLED && key`; the master's comment (:520) and javadoc (:189-190, :205) list the key and the helper with the nine other keys / helpers.
- **Pins (generated, own batches — TEST-003):** new `TargetingWave4ParityTests` (`targetingWave4Parity`, 28 rows `targetingwave4paritytests.s<finding>_NN_<species>_<what>`): s130_01 / s130_02 the pick order through a direct `targetSelector.tick()` on a frozen companion with both goals eligible — Leon: a Pig attacker (its hunt refuses a Pig) held by the revenge goal is displaced by a Zombie appearing 8 east (the hunt @3 pre-empts the running revenge @4) and loses the same pass on a fresh Leon; the Girlfriend (tamed, an owner on the player list): the untamed rival 4 east held by Jealousy @4 (its 1-in-3 acquisition roll pinned) is displaced by a Zombie and loses the same pass; s133_01-04 each robot with a Zombie attacker primed by name and held by its revenge goal, 8 east in full sight and inside its engage range (yaw −90 puts the +x facing on it, Robot2 out of melee reach so the control swings and grieves nothing): the forgetting pass under the fire roll (Robot2's 1-in-6 and Robot4's 1-in-8 gates pinned, the Pounder's 1-in-450 tantrum pinned quiet) clears the slot and engages nothing — the yaw stands, no path, the attack pose 0, no LaserBall, the memory final; the control under the quiet roll engages it (pathed / posed / fired — the `looked` signal is inert, the yaw −90 already facing the Zombie on +x, so the rows discriminate through the path, the pose and the laser); s134_01-03 the private `doMovement` in activity 2 under pinned dice (300 → 0, 7 → 0, 4 → 2, 8 → 3, 5 → 4, 2 → 1, 6 → 2) writing the branch's box off the owner's (a survival ServerPlayer, `ownerFlying` written) or the Stinky's truncated cell — (8, 0, 8), (3, 1, 3), (10, 0, 10) — HEAD's ownerless roll pinned to 10 so it is told apart; s137_01-05 the gates on both hunts' `canUse` with a Zombie 8 east and a Creeper 8 east, 2 south in sight (untamed refuses, tamed takes — the IMob hunt's pick the Creeper, the sorter's first: orig MyEntityAINearestAttackableTargetSorter.java:23-25 halves a creeper's distance², 68 / 2 = 34 over the Zombie's 64, and orig MyEntityAITarget.java:111 grants a Creeper ahead of the reach block (ENT-S-139, s139_82) — the gate lifted is the `canUse` flip on the same geometry; a sitting tamed Girlfriend refuses, standing takes; a sitting tamed Boyfriend hunts; the untamed Girlfriend's Valentine Player task picks under the `SeasonalDates` Feb-14 seam while her IMob hunt refuses); s138_01-05 at a fractional NEGATIVE y — rel 1.5 over the floor at rel 0 (the harness grid sits below y 0; the cast cell one above the floor's), the rows on the INTEGER x / z lattice (rel 20.0 / 24.0, the Dragonfly row's prey at rel 22.0 / 24.0 on the floor — distSq 4.25, inside the bite reach): an exact integer casts and floors alike on any origin, so the cast and the floor agree on x / z (asserted) and y alone discriminates — at the block-centre .5 a negative origin on x or z (the harness's x / z are random per run within ±14999992) shifted the cast cell there too and the rows' floor-cell preconditions read other values (the wave-4 refuter A); the expected numbers are unchanged by the move (the distSq pairs 8 / 9, 2 / 3, 3 / 4 and their reverses assumed the x / z agreement the lattice now guarantees): a parked flight target at distSq 8 / 9 (the Brutalfly's and Mothra's &lt; 9), 2 / 3 (the Dragonfly's &lt; 2.1), 3 / 4 (the butterfly's &lt; 4) from the cast cell and the reverse from the floor cell decides the retarget — the Brutalfly and Mothra through `customServerAiStep` with the strafe, hunt and retarget rolls pinned and the loop pinned to 8 east / 8 south; the Dragonfly through `goal.tick()` with the 1-in-12 pinned to fire (no bite on a retarget, the bite on the else branch — s135_43 re-derived at the fractional y); the butterfly's own copy (ButterflyIslandsHuntGoal.java:67 / :69 — the fifth site's pin) with the wander pinned to its own cast cell; s138_04 (the Dragonfly's prey at the fractional y read back at `(int) (posY + 1.0)`) kept as landed, its x / z assertions deriving from the cast on both sides; s142_01 / 02 a frozen Knight / Reaper with its feet on the floor and a water source in its feet cell (`updateInWaterStateAndDoFluidPushing` by reflection), `aiStep()` once under the pinned random (the daylight dice quiet; 4 east, 4 south, level) — moved there with the scream off; dry, the tick moves it nowhere; s143_01 the preset read off the goal, s143_02 night under the open sky on a non-retarget tick (100 → 1, the 1-in-10 → 0) with a TORCH on a stone 5 east: the flight target the cell above it (HEAD sought inside the retarget under cover alone), s143_03 by day: not, s143_04 the torch 5 SOUTH found on the +z face (HEAD's ±x scan never read it), s143_05 a retarget tick (100 → 0, the wander pinned to the cast cell) wanders, never the torch — the day time set and restored (`setDayTime` + `updateSkyBrightness`) in the finally; s144_01 / 02 a `PlayLevelSoundEvent` ear at the origin and at the landing: ENDERMAN_TELEPORT once at each on a landed `teleportRandomly` (the pinned 4 east / 4 south), nothing on a refused one (the y roll 0: a spot 32 below the floor, the landing search starting below the world's bottom, the origin restored). New `ChainsawSweepSightTests` (`chainsawSweepSight`, 9 rows `chainsawsweepsighttests.i070_NN_<row>_<what>`): a survival ServerPlayer on the floor, the frozen 1000-HP target 5 blocks south on the same column, the occluder placed on a cell a float-for-float replay of orig :198-246 (`sweepWalkCells`) reads at the layout's actual origin (the ScanSetParityTests row 7 idiom — the float grid beyond |2^23| and the cast's shift on a negative axis move the samples, so the cells are derived, never assumed), the sweep driven through the private `findSomethingToHit` on the registered Chainsaw, its damage the signal, the key flipped off for the walk half and restored: i070_01 (9b) SHORT_GRASS on the tenth sample (the target's own cell at a small positive origin), 02 (4a) a COBWEB on the fifth (the head cell between), 03 (4c) SHORT_GRASS on the seventh with a Pig (the ground cell before the pig's), 04 (8c) a bottom STONE_SLAB there, 05 (6) WATER there with a Cow, 06 (10) a COBWEB on the first sampled cell — with the target 5 blocks along z the first sample lands at z + 0.5, the head-height cell just ahead of the player, never the player's own cell (the row `i070_06_10_cobweb_on_first_sampled_cell_sweep_dead`; the table's row-10 picture, the player standing in the cobweb, is this same first-sample read for a target nearer than five blocks) — each: the walk refuses and the sweep spares under the key off, the ray admits and the sweep lands under the key on; 07 (12) the player three blocks up and an OAK_LOG at the corner cell (20, 2, 27) the segment enters between two samples and the eye line crosses: swept by the walk, refused by the ray; 08 (13) a STONE on the replayed sixth cell stops the walk, a STONE on the sample point's true cell (`BlockPos.containing`) stops it only where the cast reads that cell and is skipped where the cast reads the neighbour toward the origin, the ray refusing it in either frame — the frame reported in the message; 09 the master off with the key on forcing the walk (row 9b's grass: not swept), the master back on the ray (swept). The rows place their occluders on replayed sample cells: the table's pictures are the positive-origin readings, the gate's negative y shifts the cells one up through the `(int)` truncation, and the facts pinned are the walk's own — a non-air block on a sampled cell stops the walk, the vanilla ray passes collision-less and fluid blocks (the wave-4 refuter B). Re-based (harness semantics, the pinned values unchanged): ScanSetParityTests `spawnCompanion` tames (s135_33-38, s135_41; the Valentine rows unaffected) and s135_43's cells derived from the cast cell; TargetReleaseParityTests s129_39 / s129_40 tame the companion, s129_41 parks the flight target off the cast cell; PlayNicelyGateParityTests' GoalProbe tames the Boyfriend / Girlfriend; PreyListParityTests' GoalDriver tames a TamableAnimal hunter; IMobConventionTests' run tames the two companions (its Leon rows untouched — the tame-rule row tames the Leon itself) and describes Leon @3 / the Girlfriend @3; PortOnlyTargetingTests' Girlfriend classic selector `3:…<Mob>` and modern-only pair `1:OwnerHurtByTargetGoal` / `2:OwnerHurtTargetGoal`. TargetSorterParityTests' `spawnCompanion` already tamed; MiscTargetingParityTests untouched (the ENT-S-145 re-base reverted with the hold).
- **The modern key:** `[modern] chainsawSweepVanillaSight` (default true; MOD-037) — the only mode split of the wave; every other item lands in both modes (parity, classic — no MOD record covers them).
- **Disclosed:** (i) ENT-S-130 — the Girlfriend's modern owner pair moved @3 / @4 → @1 / @2 (the Leon's and the Boyfriend's slots; the Leon precedent applied to keep MOD-033's promise after the refutation — a tie at 3 with the hunt had left a running hunt un-pre-empted by OwnerHurtByTargetGoal); it ties at @1 / @2 with the Valentine tasks; the owner may still move it. (ii) ENT-S-134 — the xdir-before-zdir dice order upheld as ruled. (iii) ENT-S-138 — a fifth site beyond the entry's four, found by the lane and closed in the same batch (ButterflyIslandsHuntGoal.java:67 / :69 — a separate site closing the butterfly's and the moth's orig lines), presented for ratification. (iv) ENT-S-143 — the six-face `scan_it` transcribed beyond the entry's letter; the one flight target / one retarget roll for orig's two loops kept; the hunt ahead of the torch scan in the shared else branch; `isDay()` for `isDaytime()`. (v) ENT-S-144 — the method name `enderTeleportTo`; the origin as read ahead of the move (orig d3 / d4 / d5), not vanilla's `xo / yo / zo`; HOSTILE = the mobs' own sound source. (vi) ITEM-070 — the `(player, e)` argument order; rows 12 / 13 pin the table's answers (the walk TRUE / the ray FALSE for 12; the frame-dependent shift for 13), not the brief's "each FALSE with the walk" shorthand; the occluders sit on replayed sample cells (the table's pictures the positive-origin readings), the assertions holding at any origin through the replay. (vii) ENT-S-137 — `isOrderedToSit()` for orig's `isSitting()` (the Jealousy idiom; `isInSittingPose()` the mechanism-faithful read, coincident today). (viii) Findings filed from the wave's reading and the refutation, not fixed: ENT-S-148 (the Dragonfly's `hurt` flight-target site `attacker.blockPosition()` for orig :182's `(int)` casts — EntityDragonfly.java:94; the ENT-S-138 class, unlisted), ENT-S-149 (the Dragonfly's vertical steering blend — `Params.dragonfly()` at AmbientFlightGoal.java:76-77 blends 0.3 on every axis, the y blend at :128, where orig Dragonfly.java:156 blended y by `(double) 0.2f` — :155 / :157 x / z by `(double) 0.3f`), ENT-S-150 (the legacy-AI movement mapping — the wave-4 refuter B on ENT-S-145), ENT-S-151 (the Stinky's / Spyro's `closerToCenterThan(position(), 2.1)` for orig's integer-lattice `getDistanceSquared((int) posX, (int) posY, (int) posZ) < 2.1f` — the wave-4 refuter A); the flyer presets' `double` literals for orig's `(double) 0.Nf` blends and steers (sub-ulp scale) an observation, as is Mothra's inherited second flight loop (the records).
- javac rc 0 on the main sources (the 795-file argfile; the sixteen pre-existing removal notes, Chainsaw.java:65's `onEntitySwing` among them) and rc 0 on the gametest sources (the 77-file argfile; the pre-existing ProactiveHuntParityTests unchecked note) — both re-run by the fix lane on fresh class directories after the refutation edits; no gradle in this lane; the gametest run is the gate's (expected `All 1231 required tests passed`: 1194 + 28 + 9, if the count stood at 1194 after T10).

GATE: (w4c, 2026-09-06 07:13-07:17, after both refuters' items, the fix lane and the ENT-S-145 hold): drift check clean; `build` exit 0 — asset audit `RESULT: 0 error(s), 0 advisory(ies), 4 acknowledged -> exit 0`, referenceGeometry `G1 PARITY PASS: 2 models; checked-in proof verified`, s4Parity `G1 PARITY PASS: 13 models; checked-in proof verified`; `runGameTestServer` exit 0 — literal `All 1236 required tests passed` (1199 + the 28 TargetingWave4ParityTests rows + the 9 ChainsawSweepSightTests rows). Two red runs before it, each diagnosed, never retried unchanged: w4 (07:06) — the asset audit's `GECKO_GEO_PROOF_DRIFT` ×15 on the shipped rigs: not code — the slice (c) branch checkout had rewritten the shipped geo files with CRLF under `core.autocrlf` while the proof copies (attributed `eol=lf`) kept the scripts' LF bytes, and the audit compares bytes; fixed by restoring the rigs byte-for-byte from their proof copies and pinning `*.geo.json` / `*.animation.json` under the assets to `eol=lf` in `.gitattributes` (the rule the proof trees already carry); w4b (07:09) — one red row, `s138_04_dragonfly_145_prey_cell_cast_posy_plus_1`: the row's own geometry — its prey sat at a half-block x against the new origin-safety precondition (the cast and the floor must agree on x / z on any grid origin); the prey moved to integer x / z (22.0, 1.5, 24.0), y still fractional, the pinned values unchanged.

Refuted twice (24 files + 2 new test classes): A (the entities and the sibling re-bases) — two blocking test defects, fixed: the tamed-control rows s137_01 / s137_03 expected the Zombie where the companions' sorter picks the Creeper (68 / 2 = 34 against 64; ENT-S-139's own s139_82) — the rows now assert the sorter's answer; the cast rows s138_01 / 03 / 05 assumed the cast and the floor agree on x / z, false on a negative grid origin (random per run) — the fractional position is now integer on x / z so y alone discriminates; plus: the fifth ENT-S-138 site (ButterflyIslandsHuntGoal's own copy of the base lines) is a separate site, kept and re-attributed (the base cast reaches the Dragonfly, Firefly, Mosquito and VampireButterfly; the copy the Butterfly and the Luna Moth) — presented for ratification; the Girlfriend's modern owner pair tied at 3 with her hunt (a running hunt is never pre-empted by an equal priority — MOD-033's promise lost) → the pair at 1 / 2 as the Leon's landing, the owner may still move it; `isOrderedToSit` for orig `isSitting` coincident today (a note); the Stinky's dice order upheld as ruled; the s133 yaw signal inert (the rows discriminate through path / pose / laser); a Stinky / Spyro retarget measure filed (ENT-S-151); the Dragonfly `hurt` finding's ledger row (:322) re-rated. Upheld: ENT-S-133 / 130 / 137 / 134 / 138 / 142 token-faithful, nothing else moved, the re-bases minimal.

B (the moth, the Ender pair, the Chainsaw) — one BLOCKING finding, HELD out of the batch: ENT-S-145's +6.2 is transcribed exactly but 1.7.10's legacy mover clamped the input and moved at a fixed 0.1f AI speed, so the boost saturated to a ≈3.1× sprint (≈1.4 → ≈4.4 blocks/s), while the modern mover scales speed by the unclamped attribute (≈6.52 blocks/tick) — and HEAD's un-boosted Knight already moves at 1.7.10's boosted pace; the entry's resolution had reserved the value; the code, its two rows and the MiscTargetingParityTests re-base it forced are withdrawn, ENT-S-150 filed with the analysis and the options for the owner's ruling. Non-blocking: the Chainsaw rows' descriptions (row 10's cobweb lands on the first sampled cell, the one ahead of the player; the gate's negative y shifts every cell — the rows pin the walk's own facts on replayed sample cells, the table's pictures are the positive-origin readings) corrected; cites (the Dragonfly y-blend orig :155-157, `scanIt` :97-155, `enderTeleportTo`) corrected; observations recorded (the presets' double literals for orig's float casts; the origin sound is not gated by `isSilent`; `isDay()`'s fixed-time clause; two files' LF endings). Upheld: ENT-S-143 line by line against orig EntityLunaMoth.java:54-145 (the hunt-then-scan order, the six-face scan, the torch set), ENT-S-144 against orig :159-206 with the modern sound event, ITEM-070's `myCanSee` token by token against orig UltimateSword.java:198-247 with the key read live and the master forcing the walk, rows 12 / 13 pinning the table's answers.

CORRECTED 2026-09-06 (the slice (d) gate at a positive origin, fix lane 2, F1): the pins line's and (vi)'s claim that
the ChainsawSweepSightTests rows hold at any origin was FALSE — the half-block PLAYER / TARGET positions let the origin's
sign (and beyond 2^23, a float's ulp of 1, its parity) decide which column the walk read; at the earlier negative origins
the walk ran one column beside the eye line, so every "the ray admits" assertion was vacuous. The class now uses the integer
x / z lattice and is hand-replayed on both signs; the sample indices moved (4a 4 → 5, 4c / 8c / 6 6 → 7, 13 5 → 6); the
walk itself (`Chainsaw.myCanSee`) is unchanged. Details under "PHASE G SLICE (d)" (fix lane 2).

## PHASE G SLICE (e) — the animation contract and the keyframe controller's return: DESIGNED AND PRESENTED, nothing wired (2026-09-06)

The owner's sequencing ruling (addendum item 23 (8)(e)): designed and presented before anything is wired. A read-only
design lane produced `phase_g_reports/animation_contract/` — `contract_design.md`, `controller_design.md`,
`open_questions.md` (17 decisions, each with a recommendation and costs), `demo_results.json` and `records.md`; nothing
under `src/` changed. Presented for the owner's rulings; no gate applies to a design.

THE CONTRACT (summary): a clip inventory over parallel controller LAYERS, one clip per frequency group (`walk` for the gait
group, `walk_<group>` for the others — the Beaver's classic transcription is the salvaged trio renamed `walk` / `walk_teeth` /
`walk_tail`); the locomotion clips (`idle` / `walk` / `swim` / `fly`, `aggro_idle` / `calm_idle`, optional `idle_alt_N`)
selected by WEIGHTS from client-visible state — `limbSwingAmount` (the seam's own input), `isInWater()`, a species-declared
flyer flag with `!onGround()`, the species' synched `DATA_ATTACKING` int (51 classes carry it; vanilla's `isAggressive()`
is set by none) — smoothed over 5 ticks, so no thresholds and no cuts; the gait group's weight is `limbSwingAmount` itself
(the ruled scaling). `hurt` / `death` are client-observed edges of `hurtTime` / `deathTime` (the fields vanilla's overlay
and flip read; no packets, no entity flags); `attack` gets three transports chosen per species by the generated trigger
inventory (an EVENT-flag edge, a generic `LivingDamageEvent.Post` → `triggerAnim` packet for melee, named launch sites for
ranged); `idle_alt` rolls once per idle loop boundary (p = 0.15) keyed on the clip clock's cycle index so the ENT-S-147
dedup cannot double-roll. The two motion sources coexist per species through the per-entity manager: classic registers no
controllers and the hook poses; artist registers the layers and the hook returns early — the gate is
`manager.getAnimationControllers().isEmpty()`, decided once per manager; a proposed `[modern] artistAnimations` master
key (default ON, species self-gated by clip presence) plus a `classicAnimationSpecies` exclusion list in the MOD-029 /
MOD-031 shape. SPEC gains a tempo table, glossary hooks (`gait_bone`, `locked`), a clip table, the trigger-inventory link and
the density statement; Tier-1 bones in a profile's `synched-bones` are SPEC-locked. Artists may edit shape, keys, lerp
mode, channels and `animation_length`; not bone names, tempo, clip names / loop types, or locked bones.

THE CONTROLLER (summary): the salvaged `PhaseLockedKeyframeController` (commit 0d238ba) compiles unchanged against today's
tree (one delta in its probe, `animationAgeTicks(state)`) and returns with three changes — the time-warp reads the clip's
DECLARED `animation_length` (the ADDENDA ruling; the same shape at 0.5 / 1 / 2 / 3.3 s poses identically, 0.0 rad), an
additive layer mode for blending, and instrumentation. Delta scaling is from the BIND pose (GeckoLib writes
`value + initialSnapshot`; the G1 leg defines proportionality from bind; scaling from a first key would shift a cosine's
mean). GeckoLib composes nothing (two controllers on one bone: the last registered wins — measured 1.412 rad off), so
blending is the additive layer (composes to 5.2e-8 rad); native transitions blend IN only, and blend OUT to bind through
the bone-reset lerp — with the additive locomotion layers registered after the triggered controller that reads as a blend
back to the live pose. The wrap sample straddles the LUT index at every seam and holds. The code-driven hook runs AFTER the
controllers (`AnimationProcessor.tickAnimation` 268-284 then 287-292; demonstrated), so one source per species. OPT-029
needs nothing. Two FINDINGS: (1) GeckoLib 4.8.4's `catmullrom` stores `P0 = frames[i−1].endValue()`, which IS the
segment's own start value, so every segment has a kinked tangent — its error decays like linear's with a worse constant; a
~60-line OreSpawn-side repair of the easing arguments at clip load (through the same evaluator) is proposed (open question
9); (2) the salvaged Beaver clip's authored sign is inverted against the landed basis (2.827 rad error, 25 % sign
agreement; flipped → 1.344e-3 rad) — disposition only (open question 17).

DENSITY (the headless demo over the Beaver rig, `demo_results.json`: 1,057,088 gait / 264,272 teeth / 264,272 tail
comparisons at amplitudes 0 / 0.25 / 0.5 / 1, plus the wrap seams; the fewest keys per bone holding 2.5e-3 rad):
  linear                      gait 54 (2.483e-3; 53 fails at 2.575e-3)  teeth 41 (2.414e-3)  tail 19 (2.350e-3)
  catmullrom as GeckoLib evaluates it   gait not reached by 97 keys (6.85e-3)  teeth not reached (3.81e-3)  tail 31 (2.451e-3)
  catmullrom, spline args repaired at load   gait 15 (2.163e-3; 14 fails)  teeth 13 (1.892e-3)  tail 8 (2.295e-3)
  the salvaged 73 linear keys                gait 1.344e-3  teeth 7.47e-4  tail 1.49e-4
Wrap: |v(T−ε) − v(0+ε)| ≤ 1.6e-5 rad (linear) / ≤ 1e-7 (repaired); seam errors ≤ 4e-5; length independence 0.0 rad; the
arithmetic model of GeckoLib's spline rule reproduces the measured errors to four digits.

OPEN QUESTIONS (owner): 1 the config key shape · 2 the transition policy · 3 the death clip vs the vanilla death flip · 4
the hurt clip vs the red overlay · 5 idle_alt cadence · 6 the extras cap · 7 Tier-3 clips · 8 the ENT-S-147 per-render hook
shape · 9 the catmullrom spline-argument repair · 10 the density statement's wording (54 / 41 / 19 linear vs 15 / 13 / 8
repaired) · 11 the attack transport · 12 the aggro flag for species without `DATA_ATTACKING` · 13 weights vs discrete states
· 14 the authoring-length convention · 15 the Blockbench preview at tempo · 16 the pilot boss (the Queen recommended) · 17
the sign-inverted salvaged clip's disposition.

GATE: none — a design, presented; nothing in `src/` changed (the lane's stray-process check clean).

## PHASE G SLICE (d) — the spawn-100 benchmark harness: live scenes A–F, MHLib counters in the baseline, the headless collector companion, a threshold proposed (2026-09-06)

WHAT: the G1 smoke harness cannot see MHLib (its "candidate" is a bare GeoRenderer outside the mod loader:
"MHLib parts: 0" in every scene), and the protocol lists frame time, MSPT, MHLib packets and the synced part
count as live-only metrics. This slice builds the harness that measures them with MHLib's own cost in the
baseline (morehitboxes_evaluation.md §5 "Item 13 baseline fold"; owner 2026-09-05, scope addendum item 23
(8)(d); addendum C.7): eleven more MHLib counters at §5's sites; a dev command that spawns §5's six isolation
scenes and samples a run on both halves into paired classic/candidate reports; the headless companion that
times the real collector over the baked Queen rig and the MHLib-free Beaver rig; a threshold PROPOSED for the
owner, adopted nowhere. The OWNER runs the live scenes (a client is outside this lane's reach); what runs
headlessly ran here. ~3,400 lines: sixteen new main/gametest files, thirteen edits. No gate threshold changed.

COUNTERS (A). `de.dertoaster.multihitboxlib.util.MHLibCounters` (rewritten, 329 lines): four CLIENT names as
static fields after the nine (client.collector_ns, client.collector_alloc_bytes, net.c2s_bone_packets,
net.c2s_bone_bytes — indices 9–12 of the client dump; `orespawn.geo.evictions` still follows the nine and the
gauge still follows every counter, so GeoCacheEvictionTests.assertDumpOrder holds as read: nine at the head,
evictions >= 9, gauge >= all()); a SERVER list through `serverCounter(String)` (net.s2c_update_packets,
net.s2c_update_bytes, net.set_master_packets, server.align_sub_parts_parts, server.align_synched_parts,
server.part_setpos, server.placement_ns) with `sumAndResetServer` / `formatServerDump` ("MHLib counters
(server, per 100 ticks): server_tick=N ..."), dumped and reset by a new `MHLibMod.onServerTick`
(ServerTickEvent.Post, registered only under -Dmhlib.counters=true, MHLibMod.java:38-43, :87-103) — one list
per side because an integrated server shares the JVM with its client; a `DumpListener` both dump handlers
publish to (MHLibClient.java:62-66, MHLibMod.java:96-103) so the harness sums a run's intervals instead of
resetting under the dump; the test seam `enabledForTests` (package-private field :95; `serverEnabled()` :191 =
ENABLED || seam; `enableForTests` :201 / `enabledForTests()` :206, public because the game tests live in
another package). Sites, every increment guarded (client: MHLibCounters.ENABLED; server: serverEnabled()):
- client.collector_ns / collector_alloc_bytes: `MHLibCollectorProbe` (new, util, 102 lines) begin/end around
  MHLib's Pre → Post hooks, both paths, keyed on the entity
  (GeckolibEntityRenderEventHandler.java:43-46 Pre, :33-36 Post; replaced :56-59 / :76-79): the span is
  GeckoLib's render of the multipart entity WITH the collector's per-bone work inside it — §5's "per-frame
  capture cost"; the headless companion isolates the collector. One slot, no nesting (a foreign Post accounts
  nothing). Bytes from com.sun.management.ThreadMXBean.getCurrentThreadAllocatedBytes on the render thread.
- net.c2s_bone_packets / c2s_bone_bytes: CPacketBoneInformation.send (:57-65) with `encodedLength()` (:67-79):
  STREAM_CODEC.encode into a scratch FriendlyByteBuf, readableBytes — the payload, no packet header, no
  compression; encoded once more only under the property.
- net.s2c_update_packets / s2c_update_bytes: MixinServerEntity both send sites (:111-114 the linger resend,
  :119-122 the fresh compile) through `mhlib$countUpdateBroadcast` (:126-138): one per broadcast call whatever
  the tracker count (the protocol's scenes have one tracker); bytes = SPacketUpdateMultipart.encodedLength
  (RegistryAccess) (new, :69-83: write() into a RegistryFriendlyByteBuf, readableBytes).
- net.set_master_packets: IMultipartEntity.setMasterUUID before the SPacketSetMaster broadcast (:115-118).
- server.align_sub_parts_parts: the alignSubParts loop after the part's setPos (IMultipartEntity.java:318-321);
  server.align_synched_parts: the alignSynchedSubParts loop after applyInformation (:388-391); both also check
  !entity.level().isClientSide() inside the guard.
- server.part_setpos: MHLibPartEntity.setPos (:257-266), server side only (level non-null and not client).
- server.placement_ns: mhlibAiStep's whole server path (IMultipartEntity.java:411-414 start, :450-452 add; the
  client path returns before the add) and ModernSpiderGait.feedParts wrapped around a renamed body
  (ModernSpiderGait.java:1381-1412: feedParts :1381-1393 times, feedPartsBody :1395-1412 does the work).

DERIVED EXPECTATION (corrected by the slice (d) gate, 2026-09-06 — FIX LANE 2 (F3 / F4) below) vs §5's derived
table: server.part_setpos counts 20 per Queen tick and 24 per modern spider tick in the STEADY STATE, not 10 / 16 —
`MHLibPartEntity.tick` → `updateLastPos` (:250-255) calls setPos once per part per tick (a full setPos: a fresh
AABB and two getDimensions, a real cost) at the TICK tail, after the alignment's call at the aiStep tail
(`MixinLivingEntity.java:151-174`; and the gait feed's for the spider) — and ONE MORE per part on a part's FIRST
tick: 30 / 32 on the spawn tick (the lerp snap to the zero interp target, MHLibPartEntity.tick :109 — OPT-030; the
gate measured it, the lane's reading had missed it). §5 counted the position writes only. Pinned by BenchHarnessTests
rows 11–12: the mhlibAiStep-only figures (10 / 8), the spawn tick (30 / 32, the parts' transient), the second tick
(20 / 24). The remaining derived values hold: Queen 0 / 10 synched; spider 8 / 0.

THE HARNESS (B). Common (`danger.orespawn.bench`, no client import — the game tests reference only this half):
BenchScene (the six scenes: species, default 100, pitch, base distance, behind-player for B, what each fixes;
A/B at a 12-block pitch from 40 blocks out — refuter B), BenchState (idle = no AI, the protocol's fixed state;
wander = AI on, looks only), BenchSceneSpawner (LAYOUT, refuter B: a WEDGE — rows along the look axis from the
base distance at the pitch, each row's columns at multiples of the pitch within d·tan(35°) of the axis (half
the default 70° FOV), every slot under 180 blocks (the default simulation distance is 12 chunks = 192, the
entity-ticking range; the Queen's tracking range 256), rows in order and each row centre-out, so the first
slot stands on the axis at the base distance; `capacity(scene)` = the slots the wedge holds (A/B 132, C/D 551,
E/F 4,947), `layout` throws past it and an `IllegalStateException` guard asserts every slot < 180;
`maxDistanceBlocks`; `countTicking` (`ServerLevel.isPositionEntityTicking`); MC yaw → forward (-sin, cos),
right (-cos, -sin); spawn on the heightmap top, facing the origin, persistent, tagged `orespawn_bench`, no-AI
when idle — a no-AI mob keeps its placed position: `travel` is called but its body sits behind
`isControlledByLocalInstance()` = `Mob.isEffectiveAi()`, false for no-AI, so no gravity; discardTagged;
partCount), BenchStats (median, nearest-rank percentile, 1 % low = 1000 / p99), BenchSession (the scene and the
run: MSPT per tick from MinecraftServer.getTickTimesNanos()[getTickCount() % 100] at ServerTickEvent.Post — in
the 21.1.223 bytecode tickServer increments tickCount at offsets 6-11, fires Pre at 16, tickChildren at 28,
writes tickTimesNanos[tickCount % 100] at 153-204, fires Post at 246; the server counters summed from the dumps
inside the run plus the end partial minus the start partial; players online, dimension, part count; the
farthest slot distance from the layout and, at the END of the run, the spawned mobs in entity-ticking chunks),
BenchClientBridge (the volatile controller slot the client installs; every call a no-op without one),
BenchClientSnapshot (a record), BenchServerResult (a record; + countTicking, maxDistanceBlocks), BenchGit
(repository root by walking up from user.dir to `.git`; HEAD through symbolic, worktree, commondir and packed
refs; `workingTree` by index stat — refuter B — mtime seconds and size of every regular-file entry of a v2/v3
index against the file, what git status does before hashing, never running git; "unknown" / "unknown — head
only (reason)" on any failure), BenchReport (JSON schema 1 + Markdown; variant = the dev switch's state for the
species when it is one of the fourteen landed ones, classic otherwise; a `coverage` object — spawned, ticking,
in_client_level, max_distance_blocks, warning, note — and per-entity figures that divide the server counters by
`ticking` and the client counters by `in_client_level`; `working_tree`; every undefined metric written as JSON
null — never a bare NaN — with `serializeNulls` so the keys stay; pairing with the newest report of the other
label in the live dir: frame median regression %, p95 delta ms, 1 % low delta, allocation ratio, MSPT p95
delta, the per-entity collector_ns / c2s_bone_bytes deltas, S2C bytes delta, both working trees — information
only, a null input makes that delta null), BenchCommand (`/orespawn bench scene <A-F> [count] [idle|wander] |
start <seconds> | stop | report [label] | status`, `bench` requires op level 2; a count past the wedge's
capacity is refused with the numbers; the report reply warns when ticking < spawned and names a DIRTY working
tree; `register(CommandDispatcher)` for the tests), BenchHarness (init from the OreSpawnMod constructor,
OreSpawnMod.java:87-89: only under -Dorespawn.dev.bench=true adds the RegisterCommandsEvent and
ServerTickEvent.Post listeners). Client (`danger.orespawn.client.bench.BenchClientSampler`, 328 lines, installed
from OreSpawnClient.ClientEvents.clientSetup under the same property, OreSpawnClient.java:35-43): the frame
timer on RenderFrameEvent.Pre — Pre to the next Pre; in Minecraft.runTick(boolean) ClientHooks.fireRenderFramePre
is at offset 398, GameRenderer.render at 422, fireRenderFramePost at 438, and the blit, the swap and the next
iteration's ticks lie between one Pre and the next, so the interval is the frame the player sees; render-thread
CPU (ThreadMXBean.getCurrentThreadCpuTime) and process CPU (OperatingSystemMXBean.getProcessCpuLoad per client
tick); render-thread and JVM-wide allocation (getCurrentThreadAllocatedBytes / JDK 21
getTotalThreadAllocatedBytes); GC count and time deltas; the client counters summed like the server's; the
controls the client can report (launched version, resolution and gui scale, render/simulation distance,
graphics mode, vsync, framerate limit, FOV, entity distance scaling, entity shadows, particles, clouds, AO,
biome blend, FPS at start, GPU vendor/renderer/GL, CPU) with the owner's fields left blank in the report; the
dev switch per landed species; the level renderer's entity statistics and the benchmark-tagged entities in the
client level at the end. The server thread's requests (refuter B) are one latest-wins AtomicInteger slot (a
stop, or the seconds of a start) taken with getAndSet on the render thread's next frame: the run in flight ends
first (stopped early), then a start begins the new run — `scene` (abort → stop) then `start` in one frame
starts the new run, `start` then `stop` leaves nothing running. The sample buffer is appended and read under one
lock.

Reports: phase_g_reports/benchmark/live/<scene>_<classic|candidate>_<yyyyMMddTHHmmssZ>.json + .md (the live
dir is created on first write; nothing was written there in this lane).

THE HEADLESS COMPANION (C). QueenPartPlacementProbe `--bench <runs> <the_queen.geo.json> <the_queen.json>
<beaver.geo.json> <outDir>` (QueenPartPlacementProbe.java:144-152 dispatch, :692-1060 the section): the real
vendored layer over the baked rigs; the HEAD/TAIL hooks reach it through the REAL `MixinGeoRenderer.
_mhlib_callLayers` (`BenchGeoRenderer extends HeadlessGeoRenderer implements MixinGeoRenderer` :1040 — the mixin
interface's default method run as written over a layer list the rig fills: getRenderLayers(), the iterator,
the instanceof, the Consumer; the injector methods are never called); an adaptive warm-up (at least 20 runs,
then until three consecutive runs stay within 10 % of the median of the three before them, at most 60; every
warm-up run's ns reported), then `runs` measured runs of 1,000 walks; the median run, the median of the last
five and the per-bone figures; nanoTime + MHLibCollectorProbe.currentThreadAllocatedBytes (the live counter's
source). Rigs: `queen_collect_yawpi` (THE BASELINE ROW: the layer's private `bodyYawRotationTerm` set by
reflection to bodyYawRotationTerm(180°) = −π — a Queen facing the camera as the scenes spawn them — so
getRotationVector's foldBodyYaw builds its matrices as in-game, seven 3×3 double[][] per synched bone; per
bone HEAD hook, getBoneWorldPosition, calcScales, calcRotations, the synched bones' getScaleVector +
getRotationVector, TAIL hook; setScales/setRotations; onPostRender), `queen_collect_yaw0` (the same walk at
yaw 0: the fold's early-out, no matrices — the first lane's baseline, kept), `beaver_tax_inactive` (a FLOOR of
the hook tax: isBoneCollectionActive() is a constant false — the real chain, renderer instanceof
GeoReplacedEntityRenderer / getCurrentEntity() / shouldCollectModelBones(entity), needs a client renderer and
a live entity; the hooks return after the counter guard and that call), `beaver_tax_active` (the default
layer: the GeoEntity-path upper bound). NOT measured, both needing a live entity: tryAddBoneInformation and
the trust-client apply per synched bone (getPartByName + MHLibPartEntity.applyInformation, common logic
:132-137; the_queen.json :76-77 is sync-with-model: true, trust-client: true) — the companion measures the
collector's walk and fold only (the JSON's `not_measured`, the walk descriptions, the proposal). Children
walked by index so the walk allocates nothing of its own, and every per-bone result is stored into a static
sink (`BENCH_ESCAPE`) so it ESCAPES as in-game: without that, after a 33-run warm-up, C2's escape analysis
eliminated the yaw-0 walk's 110 world-position Vec3s and 20 vector Vec3s (16,904 → 11,704 B/walk in one
invocation — the probe's artefact, not the collector's cost). `working_tree` in the JSON beside git_head. The
existing modes and walkLayer are untouched; the normal mode still reports OVERALL: PASS.

HEADLESS NUMBERS (HEAD a03c0c5, JDK 21.0.7 Microsoft, this laptop). Three invocations of `--bench 10` after the
fixes (out_fix1 / out_fix2 / out_fix3; median of ten runs, then the median of the last five; bytes identical
in all 30 runs of each rig):
queen_collect_yawpi 110 bones / 10 synched: 11,003.3 / 11,800.5 / 12,002.1 ns per walk (last five 11,027.5 /
11,702.7 / 12,076.4); 32,864.0 B per walk (298.8 B per bone; 100.0 / 107.3 / 109.1 ns per bone); the fold at −π
costs 15,960 B per walk over the yaw-0 walk = 1,596 B per synched bone (by reading the seven 3×3 double[][]
are 1,064 B and the shipped Vec3 40 — the remaining ≈ 490 B per synched bone are not accounted for by reading;
the measurement stands).
queen_collect_yaw0: 6,563.4 / 6,725.9 / 6,829.2 ns (last five 5,992.6 / 6,795.2 / 7,375.1); 16,904.0 B (153.7 B
per bone) — the same 16,904 as the first lane (9,181.3 / 7,403.3 ns) and refuter B (7,542.4 / 6,434.8 / 7,116.0).
beaver_tax_inactive (FLOOR) 9 bones: 403.2 / 322.4 / 309.1 ns (last five 358.2 / 313.1 / 318.5), 0.0 B — a flat
steady state (the warm-up lists show the fall from ≈ 3,000-4,000 ns in the first run to the plateau within
8-20 runs; refuter B's 395 / 608 / 275 with the old three-run warm-up were mid-fall).
beaver_tax_active: 485.2 / 393.3 / 389.8 ns (last five 377.7 / 384.9 / 404.8), 1,120.0 B (124.4 B per bone).
THE NS BAND: eight yaw-0 invocations on record span 6,435-9,181 ns (max/min ≈ 1.43) — ≈ ±40 %, the JIT; the
yaw-π variant's three span 11,003-12,002 (max/min 1.09) but are too few to narrow it. The bytes are the signal:
bit-stable within and across invocations once the probe's results escape as in-game (the proposal, §1 / §3).
Earlier invocations of this lane's own code, superseded: the first three (08:00:29: 20,456 / 320 / 1,440 B —
the walk's for-each iterator per bone, the probe's own allocation, discarded with the walk-by-index fix;
08:01:48 and 08:01:49: 9,181.3 and 7,403.3 ns, 16,904 B — the first lane's reported pair); then in the fix
lane, before the escape sink, yaw-π 9,915 / 12,882 / 9,489 / 9,111 ns at 30,208 B and yaw-0 7,231 / 6,975 /
4,058 (11,704 B — the escape-analysis artefact) / 6,074 ns.

THRESHOLD (D): proposed, not adopted — bench\threshold_proposal.md (§5's shape: no rise in collector_ns or
c2s_bone_bytes per Queen — R1 read primarily on the companion's bytes per walk, the ns informational within a
≈ ±40 % band; scene E's allocation rate at or below classic's; the protocol's 10 % mixed-scene median
regression, 2.0 ms p95, 50 ms server p95, no sustained packet growth; today's yaw-π headless row as the first
baseline with the yaw-0 row kept; the fields the live runs fill; seven open points for the ruling). Nothing is
wired into any gate.

PINS (E): BenchHarnessTests (new, 989 lines, own batch `benchHarness`, 18 rows, `benchharnesstests.
sliced_NN_<row>`): 01 the wedge (every scene at its default count: each slot ahead — behind for B — at least the
base distance out, inside |x| ≤ d·tan 35°, under 180 blocks, its nearest neighbour EXACTLY one pitch away (no
duplicate, no gap — the real neighbour check, refuter B), the capacity ≥ the count, maxDistanceBlocks = an
independent farthest; the Queen: pitch 12 from 40, ten rows 40..148 with seven in the tenth, the farthest
hypot(136, 84) = 159.8, the fill order 0, ±12, ±24 in the first row, capacity 132 and 133 refused, B mirrors A
behind, yaw 90 → −x inside the wedge, facing yaws, token parsing); 02 scene A one Queen at the base distance on
the axis, no-AI, persistent, tagged, on the heightmap top with air at the feet and ground below, facing the
origin, ten parts, in an entity-ticking chunk (countTicking = 1); 03 scene B behind; 04/05 scenes C/D two robots,
modern ↔ eight parts, neighbours one pitch apart (along the axis: the near row is one slot wide); 06 scenes E/F
Beavers and Frogs, no parts, the Beaver landed and the Frog not, same size class; 07 wander keeps AI, the sweep
discards the tagged and only them; 08 the report JSON shape (every top-level key incl. working_tree and
coverage; counters.server_per_second and server_totals keyed by the seven names in order; per second = total /
wall seconds; per entity = per second / TICKING (80 of 100 spawned), null when nothing ticks; the coverage
warning present for 80 < 100 and null for 100; S2C packets sum update and set-master; C2S null without a
client; the on-disk text carries no NaN / Infinity token and re-parses with a non-lenient JsonReader — which is
pinned to reject a bare NaN; client.available=false with a reason; controls incl. the owner's blank fields;
fourteen dev-switch lines; the threshold marked PROPOSED; the markdown head; with a client: the client section,
the client counters keyed by the thirteen client names, the client per-entity figures / in_client_level (90),
the coverage line in the markdown); 09 pairing (10 % regression candidate over classic whichever is newer, p95
delta, allocation ratio, both heads, no counterpart → null, a null input → a null delta), counterpart labels,
base name, write to a temp dir (two files), newest, the paired md; 10 command gating (the live dispatcher has no
`orespawn` literal and the harness is not installed without the property — or has it with; a fresh dispatcher
gets orespawn/bench/{scene id count state, start seconds, stop, report label, status}; op 0 and 1 refused, 2
passes); 11 the Queen under the seam (mhlibAiStep: 0 / 10 / 10 / placement_ns > 0 / no broadcast; the SPAWN tick: 10
synched, 30 setPos and the parts at the world origin (OPT-030); the second tick: 20 and the parts back on the Queen; the
seam off counts nothing when it was off before); 12 the modern spider under the seam (8 / 0 / 8; the spawn tick: 8, 32,
the legs re-fed in-tick, placement_ns > 0; the second tick: 8, 24); 13 both packets' encodedLength against a manual encode (and the
S2C floor of ten fixed records); 14 the collector probe (accounts on the open key, nothing on a foreign or
replaced key, bytes ≥ a 128 KiB array); 15 the dump order (nine, then the four, evictions after them, the gauge
after every counter, no server name on the client side, the server dump exactly the seven, both INFO line
heads, the listener publish/remove); 16 the statistics (nearest-rank: one 50 ms frame in a hundred is p100, the 1 % low
100 FPS; two in two hundred: p99 10 ms, p99.5 50 ms, max 50 ms); 17 asynchronous: S2C broadcasts counted with bytes ≥
packets × the non-dirty payload length, the election broadcast counted, the level ticked the Queen — the seam
is turned on inside the first delayed step after every synchronous row restored it; that step's try/finally
restores the PRIOR value only if the step fails, the second step's finally restores the prior value; 18 git HEAD
through a loose ref and through packed-refs on a synthetic .git, the working tree: no index → head only, one
entry matching by mtime seconds and size → clean, a changed size → DIRTY naming the file, a deleted file →
DIRTY with 1 missing, index version 4 → head only, garbage → head only, no repository → unknown / head only,
and the live repository reads as one of the three shapes.

GATE: see this section's closing GATE line (bench2, 2026-09-06 10:01-10:03, 1254 required tests passed) — this placeholder was the lane draft's second copy, filled 2026-09-06.

REFUTER A NOTES (2026-09-06, the MHLib instrumentation; no blocking defect): (A1) the S2C cadence expectation for the
protocol's no-AI scenes is ≈6.7 per Queen per second, not 20 × trackers (`ServerEntity.sendChanges` 201-232: every
`updateInterval` ticks — the Queen's builder default 3 — or on impulse or dirty data; a standing Queen has no impulse) —
the runbook and the §5 table's line corrected here; the report measures actuals. (A2) under the property the byte
accounting's second encode runs on the sampled threads (C2S on the client tick inside the measured frame, S2C on the
server thread inside MSPT; the real encode is netty's) — disclosed in the report note and the runbook; identical under
both labels, outside collector_ns / placement_ns; bytes exact (the codec's own encode into a counting buffer, no header,
no compression; the second encode only under the property). (A3) the 20 / 24 setPos figures are derived by reading (the
rows pin them; unexecuted in the lane — the gate runs them). [The gate ran them 2026-09-06: 30 / 32 on the spawn tick, 20 / 24
from the second tick — FIX LANE 2 (F3 / F4) below.] (A4) the S2C count is per broadcast call for every
TrackedEntity whether tracked or not — an upper bound; the snapshot's `benchEntitiesInClientLevel` is the honest count;
the 10×10 Queen grid's far corners (≈260-269 blocks) exceed the 256-block tracking cap at render distance 16 — the
runbook says so (render distance 17+). [Superseded by refuter B's B1: the wedge keeps every mob under 180 blocks.]
(A5) `collector_ns` spans MHLib's Pre listener to its Post listener — after `preRender`, before `popPose` /
`renderFinal` / `doPostRenderCleanup` in 4.8.4's `defaultRender` (129 / 235 / 260 / 266) — the counter's javadoc
corrected. (A6) "1 % low" is 1000 / p99 (the p99-percentile FPS), not CapFrameX's average of the slowest 1 % —
labelled. (A7) line references corrected. (A8) `MHLibCollectorProbe`'s slot is three unsynchronised statics touched
from the render thread in production and from the game-test server thread in row 14 — no race exists.
Upheld: every §5 site once with the right thing counted (the Pre / Post pair once per `render`, `reRender` inside the
span, a cancelled Pre orphaned and accounted nothing; the C2S sender unique; both S2C sites; set_master elections only;
the align loops per part per tick; `part_setpos` server-only; `placement_ns` behaviour-preserving); the client guards
static-final, the server sites' `serverEnabled()` a plain static read (≈31 per Queen per tick, microseconds at 100 —
the seam acceptable as presented; the alternative is `systemProperty 'mhlib.counters', 'true'` on the gameTestServer run,
the owner's call); the server dump under the property on every dist, the two reset sets disjoint, the samplers reading
`sum()` without resetting, the lock order acyclic; the dump-order pins hold (the four names at 9-12, evictions 13, the
gauge 14); the frame timer Pre→Pre = the whole frame incl. tick, swap and the cap (vsync off / unlimited mandated); MSPT
from this tick's slot at Post.

REFUTER B NOTES (2026-09-06, the harness and the companion; one blocking item, all eleven applied by the fix lane):
(B1, blocking) the 10×10 Queen grid at a 24-block pitch (rows 30..246, far corners 260-269 blocks) lay past the
256-block tracking cap and the 192-block simulation distance, and the per-entity figures divided by count_spawned →
every scene is a WEDGE (rows from the base distance at the pitch, columns within d·tan 35° of the look axis, every slot
under 180 blocks, rows in order, each row centre-out); the Queen's pitch dropped to 12 (noPhysics; overlapping draws are
full draws — the javadoc says so); the robots keep 6 and E/F 2; B is the same fill behind the camera; the spawner asserts
every slot < 180 and reports max_distance_blocks; BenchServerResult gained countTicking (ServerLevel.
isPositionEntityTicking at the END of the run — not at report time, when `stop` may already have despawned the scene)
and maxDistanceBlocks; the report's `coverage` object (spawned, ticking, in_client_level, max_distance_blocks, warning,
note) and per-entity divisors (server / ticking, client / in_client_level); the runbook's controls gain the simulation
distance ≥ 12 beside the render distance ≥ 12 and the coverage object is explained; rows 1-3 re-derived (row 1's vacuous
`|Δx| − 24 < EPS` replaced by the nearest-neighbour-exactly-one-pitch check over every scene — B6). (B2) the companion's
Queen walk ran with the fold's yawTerm == 0 early-out → the layer's private bodyYawRotationTerm is set (reflection) to
bodyYawRotationTerm(180°) = −π before the walks (`queen_collect_yawpi`, the baseline row), the yaw-0 walk kept as
`queen_collect_yaw0`; the trust-client apply per synched bone and tryAddBoneInformation need a live entity and are NOT
measured — stated in the JSON (`not_measured`), the proposal and here: the companion measures the collector's walk and
fold only. (B3) `beaver_tax_inactive` was measured mid-JIT (837 → 113 ns across the measured runs) → an adaptive warm-up
(≥ 20 runs, then a plateau: three consecutive runs within 10 % of the median of the previous three, ≤ 60; every warm-up
run's value reported) and the median of the last five; the HEAD/TAIL hooks now run through the REAL
MixinGeoRenderer._mhlib_callLayers loop (BenchGeoRenderer implements the mixin interface headlessly); the
isBoneCollectionActive() chain cannot run headlessly (GeoReplacedEntityRenderer + entity) so the rig is labelled a
FLOOR of the hook tax in the JSON, the proposal and the records. (B4) the ns band stated as ≈ ±40 % (max/min ≈ 1.43 over
eight yaw-0 invocations), the bytes the primary signal of R1 and the ns informational, in the proposal's wording, the
records' deviation 12 and here. (B5) a NaN metric wrote a bare `NaN` — Gson's tree writer emits it whatever the builder
says (verified against gson 2.10.1) → NaN / infinity mapped to JSON null where the report is built, `serializeNulls` so
the keys stay, the option dropped, the pairing reader tolerant; row 8 re-parses the on-disk text with a non-lenient
JsonReader (pinned to reject `NaN`) and asserts no NaN / Infinity token. (B7) the javadoc's "a no-AI mob never travels
and therefore never falls" reworded from the bytecode: `travel` IS called every tick, but its body is gated by
isControlledByLocalInstance() = Mob.isEffectiveAi() (false for no-AI), so no gravity is ever applied — the heightmap-top
spawn is what puts the mob on the ground (the brief's "no-AI mobs still fall" is the one fact the bytecode contradicts;
presented in the records, deviation 17). (B8) the sampler processed a pending start before a pending stop → one
latest-wins AtomicInteger request slot: the run in flight ends first, then a start begins the new run (stop-then-start
starts it, start-then-stop leaves nothing running). (B9) row 17's first delayed step gained a try/finally that restores
the seam's PRIOR value when the step fails, and every finally restores the prior value, not `false`. (B10) `working_tree`
beside git_head: the index stat reading (mtime seconds and size; 10,651 files in ≈ 660 ms here; exactly the 13 modified
files), "unknown — head only (…)" when the index cannot be read; the runbook says to commit before a run; row 18 pins it
on a synthetic .git. (B11) the records' anchors fixed by hand (the handler's imports :8-9; MHLibMod :7, :16, :22;
ModernSpiderGait :1381-1412), this fixlog's `encodedLength()` :67-79 and ModernSpiderGait :1381-1412, the three headless
invocations noted above (the 08:00:29 run with 20,456 / 320 / 1,440 B — the for-each iterator — discarded), the
runbook's "render distance 16 covers it" sentence replaced by the wedge. Also found while applying B2/B3: after a long
warm-up C2's escape analysis eliminated the probe's per-bone Vec3s (16,904 → 11,704 B in one invocation) because the
probe dropped them where the game keeps them → every per-bone result now escapes through a static sink; the proposal
carries the caveat (a future JIT can only make the bytes DROP, which "must not rise" tolerates).

FIX LANE 2 (2026-09-06, after the slice (d) gate at the POSITIVE origin (12505408, −60, 3896280) — five red rows; each
diagnosed, none retried or widened; javac main + gametest rc 0 into fresh scratch class dirs; the suite NOT run here, the
orchestrator gates):

(F1) chainsawsweepsighttests.i070_04_8c_pig_behind_bottom_slab_not_swept — the ROW's geometry (a wave-4 row, not slice (d)'s;
Chainsaw.myCanSee untouched, it is orig :198-247 as ruled). Cause: PLAYER_POS / TARGET_POS were half-blocks (x 20.5, z 24.5 /
29.5). Beyond 2^23 a float's ulp is 1: the start x 12505428.5 rounds half-to-even to 12505428.0, dx = 0.05 never moves it (each
float sum re-rounds to the same integer), every sample casts to column 20 — the eye line's own column — and the seventh
replayed cell, rel (20, 2, 28) (y 2, not the table's ground cell 1: the gate's y −60 grid makes every sample y a negative
fraction and orig :242's (int) cast reads the cell ABOVE the true one), took the bottom slab; the eye-to-eye line (2.62 above
the feet down to the Pig's 1.765 — EntityType.PIG has no eyeHeight call, javap over the 21.1.223 jar: sized(0.9f, 0.9f) then
passengerAttachments(0.86875f), so EntityDimensions.scalable's h × 0.85) enters that cell's [2, 2.5] collision by 0.0215 at
z 28 → hasLineOfSight false → the row's "the ray admits" failed. At the earlier NEGATIVE origins the SAME half-block x read
column 21 (|x| < 2^23: the cast's step toward the origin on −N.5; beyond 2^23 the rounding's parity — the scratch replay
shows (−12505409, −3896281) → column 21 and (−12505408, −3896280) → column 20, which would have failed exactly as this run
did), one block beside the eye line, so every "the ray admits" pin was vacuous, 9b's tenth sample read rel z 30 (the cell
PAST the Zombie) and 12's "no sample in the corner cell" was true only because the whole walk lay in another column. The
wave-4 records' claim that the pins "hold at any origin: the cells are derived, never assumed" (FIX_LOG.md:6221 (vi) "the
assertions holding at any origin through the replay"; MODERNIZATION_NOTES.md:1048; the class's sweepWalkCells javadoc) is
FALSIFIED by this run: the replay makes the WALK's pins follow the walk anywhere, but the RAY's answer depends on where the
occluder physically sits against the eye line, which the half-block layout let the origin's sign (and, beyond 2^23, its
parity) decide.
Fix (ChainsawSweepSightTests.java only): the layout on the INTEGER x / z lattice (the ENT-S-138 rows' fix) — PLAYER_POS (20,
1, 24), TARGET_POS (20, 1, 29), ELEVATED_PLAYER_POS (20, 4, 24) (:102-105): an exact integer casts and floors alike on either
sign, dx is exactly 0 → column 20 at any |x| < 2^24; along z the samples sit at z + 0.5·i — the EVEN samples (i = 2, 4, 6, 8,
10) on whole z's, the same cell on both signs, the ODD ones on half-blocks, where the cast reads the cell toward the origin at
a negative z (|z| < 2^23 keeps them exact). Every row re-derived in the y −60 frame at a positive AND a negative origin — the
scratch replay `bench\replay\WalkReplay.java` → `replay.out` (the walk float for float incl. the (int) casts; the eye line
analytic with the javap eye heights; five origins: ±(12505408, 3896280), (−12505409, −3896281), ±100):
  - 9b (Zombie, index 9 = the tenth sample, z + 5.0): (20, 2, 29) on both signs — the Zombie's chest cell (the cast's cell
    above the mid-body point's rel y 1); grass: the walk stops, the ray is clear (no collision). Index unchanged; the message
    re-derived (it had called the cell the feet cell).
  - 4a (Zombie, index 4 → 5, z + 3.0): (20, 3, 27) both signs — cobweb: walk stops, ray clear.
  - 4c (Pig, index 6 → 7, z + 4.0): (20, 2, 28) both signs — grass: walk stops, ray clear.
  - 8c (Pig, index 6 → 7): (20, 2, 28) both signs — bottom slab [2, 2.5]: the eye line is 1.936 at z 28 and 1.765 at 29, under
    the cell entirely (margin −0.064) → ray clear; walk stops. (Index 6 would be (20, 2, 27) at a positive z — entered by
    0.107 — and (20, 2, 28) at a negative one: the sign would decide the answer.) The table's ground-cell picture (20, 1, 28)
    also clears, by 0.265, but the walk never reads that cell in this frame — the message says both.
  - 6 (Cow, index 6 → 7): (20, 2, 28) both signs — water: walk stops, the ray's Fluid.NONE clip clear.
  - 10 (Zombie, index 0, z + 0.5): (20, 3, 24) at a positive z (the player's own column, above the head cell), (20, 3, 25) at a
    negative one — a cobweb above the eye line (max y 2.62 < 3) either way: the walk dies on its first read, the ray is clear.
    The cell differs by sign, the fact does not; the message names both cells.
  - 12 (the elevated Pig, CORNER_LOG (20, 2, 27) kept): the replayed cells are rel y 6,5,5,4,4,4,3,3,2,2 at z 24|25, 25, 25|26,
    26, 26|27, 27, 27|28, 28, 28|29, 29 (positive|negative) — (20, 2, 27) is sampled on neither sign; the eye line (5.62 → 1.765)
    crosses it for z in [27.4, 28) (margin +0.464) → ray blocked, walk passes. Unchanged; the javadoc re-derived, and it says a
    POSITIVE y would land the seventh sample in the corner cell (the row's first precondition would then fail, loudly).
  - 13 (Zombie, index 5 → 6, z + 3.5): the replayed cell (20, 3, 27) at a positive z / (20, 3, 28) at a negative one; the point's
    true cell (20, 2, 27) on both; `coincide` is false in every frame the gate produces (the y shift alone at a positive z; y
    and z at a negative one). The row now pins the per-axis shifts (:322-327: x 0 on either sign; y +1 iff the point's y is a
    negative fraction; z +1 iff a negative half-block) and reports them; a stone on the true cell blocks the ray (margin
    +0.308) and is skipped by the walk on both signs, as before. The old frame message attributed every "differ" to the
    negative quadrant — wrong at this gate, where the shift was y's.
  - a float-envelope precondition in every row (assertFloatEnvelope :466-469: |x| < 2^24, |z| < 2^23), so an origin outside
    the derivation's lattice fails loudly instead of pinning an underived fact; the ray assertion names the replayed cell (:220).
  The class javadoc (:41-85) states the lattice, the y −60 frame's cells per species, which rows depend on the eye line, and
  the history; the sweepWalkCells javadoc (:410-419) replaces "the pins hold at any origin" with what actually holds.
  RECORDS (the orchestrator's; no record file edited here): FIX_LOG.md:6221 (vi) "the assertions holding at any origin through
  the replay" and MODERNIZATION_NOTES.md:1048 "The pins hold at any origin: the cells are derived, never assumed" → "the walk's
  pins follow the replay at any origin; the ray's pins (8c, 12, 13) hold on both x / z signs through the integer lattice and
  the even-sample cells, inside |x| < 2^24, |z| < 2^23 — the wave-4 claim was falsified at the slice (d) gate's positive
  origin (row 8c: a slab on the eye line at head height) and re-derived 2026-09-06"; MODERNIZATION_NOTES.md:1036-1046's cells
  (the sixth sample for 4a, the eighth for 4c / 8c / 6, the seventh for 13) as above.

(F2) benchharnesstests.sliced_05_scene_d_ant_robots — the ROW's expectation: sceneRobots asserted 8 parts for every modern
robot; ant_robot.json lists SIX legs (leg0..leg5 — "0.4-cubes on the ant's 49px segments", the file's own comment),
spider_robot.json eight. Fix: sceneRobots takes the species' leg count (8 / 6, :291-306) and pins BOTH the live profile's
partConfigs().size() (through IMultipartEntity.getHitboxProfile — the robots' ICustomHitboxProfileSupplier, empty for a
classic robot) and the parts array against it (:313-321). Row 4 (SpiderRobot) checked the same way: its 8 matches its JSON.

(F3 / F4) sliced_11 / sliced_12 — the ROWS' derivation (no MHLib change; the counter counts every server-side setPos, as its
comment says). The rows hand-tick a FRESHLY SPAWNED entity once: that is each part's FIRST tick, and MHLibPartEntity.tick
(:95-117) carries a client-lerp state machine — `newPosRotationIncrements` (:36) is an int the server never seeds
(setPositionAndRotationDirect :85 is called from readData :241, the client packet path, and from IMultipartEntity :728, the
trust-client apply — both client-side), so on the first tick it is 0 and the `== 0` branch (:109) calls
setPos(interpTargetX/Y/Z) = setPos(0, 0, 0), then −1 for good (:114-116). The TRUE per-tick derivation, server side:
  Queen — aiStep TAIL (MixinLivingEntity :151-160) → mhlibAiStep → alignSynchedSubParts → applyInformation → setPos: 10; tick
  TAIL (:163-174) → tickParts → part.tick → updateLastPos → setPos: 10; the first tick only → the snap: 10 → 30 on the spawn
  tick (measured 30; align_synched_parts 10; placement_ns 39,600), 20 from the second tick on.
  Spider — alignSubParts → setPos: 8; updateLastPos: 8; the snap: 8; then SpiderRobot.tick after super.tick() (:356) →
  ModernSpiderGait.serverTick → feedParts → positionLegPart → setPos: 8 → 32 on the spawn tick (measured 32;
  align_sub_parts_parts 8; placement_ns 54,000), 24 after.
The counting window is exactly the hand tick: the row body runs synchronously inside one game-test tick, so the level's own
tick of the entity lies outside both sumAndResetServer calls — no double-counted tick, no window wider than one tick. Fix:
rows 11 / 12 pin the spawn tick (30 / 32, :641-643 / :691-693), the transient (the Queen's parts at the world origin after the
spawn tick, :644-646; the spider's legs re-fed in the same tick — no window — :694-696), then a second tick at the steady 20 /
24 (:647-653 / :697-702). The steady-state figures the runbook quotes (400 / 480 per second) stand; a scene's first tick adds
10 (Queen) / 8 (robot) per spawned entity once. The snap is a real MHLib defect — a one-tick transient (the Queen's parts sit at
(0, 0, 0) from the spawn tick's tail to the next tick's alignment; the spider's are re-fed in-tick), not a per-tick cost —
drafted as `audit_OPT-030.txt` in the OPT shape; NOT fixed here.

(F5) sliced_16_order_statistics — the ROW's convention. BenchStats.percentile is nearest-rank, sorted[ceil(p·N) − 1]: p99 of
100 samples is the 99th smallest (10 ms), and the row's own 1..100 pin (percentile(·, 0.99) == 99) already said so; the 1 % low
line assumed the 100th (the "exclusive" sorted[floor(p·N)] reading) and expected 1000 / 50 = 20 FPS where the code, the
javadoc and the neighbouring pin give 100. Decision: KEEP nearest-rank (the standard; refuter A's A6 already labels 1 % low =
1000 / p99); the row (:836-851) now pins one 50 ms frame in a hundred as p100, not p99 (1 % low 100 FPS; max 50), and with 200
samples and two 50 ms frames p99 = the 198th smallest = 10 ms (100 FPS), p99.5 = the 199th = 50 ms (20 FPS), max 50 ms;
BenchStats' class javadoc (:5-16) and percentile's (:32-35) state the convention, the exclusive alternative and that it is not
used; the double product p·N lands on the exact integer at every rank the rows pin (99 of 100; 198 / 199 / 200 of 200).

Processes: javac ×3 (the replay, main, gametest) and java ×1 (the replay), each under `timeout`, all exited; the tasklist check
before the report found none of this lane's (the Gradle daemon's java.exe predates it).

GATE: (bench2, 2026-09-06 10:01-10:03, after both refuters, two fix lanes and the five-row fix): the benchmark proof rewritten by hand under the proof rule for the g1tool class-directory pin the probe's --bench mode moved (`G1 BENCHMARK EVIDENCE VERIFIED: SMOKE_ONLY / COMPONENT_PROXY_ONLY / PENDING_LIVE_PRECUTOVER; checked-in proof updated`); drift check clean; `build` exit 0 — asset audit `RESULT: 0 error(s), 0 advisory(ies), 4 acknowledged -> exit 0`, referenceGeometry `G1 PARITY PASS: 2 models; checked-in proof verified`, s4Parity `G1 PARITY PASS: 13 models; checked-in proof verified`; `runGameTestServer` exit 0 — literal `All 1254 required tests passed` (1236 + the 18 BenchHarnessTests rows). One red run before it (bench, 09:24, at the POSITIVE origin (12505408, −60, 3896280) — the first positive-x/z origin on record): five rows, each diagnosed by the fix lane, none retried — four in the new class (the AntRobot's six legs against an assumed eight; the Queen's and spider's parts writing one more position per part on their FIRST tick, the lerp snap to the zero interp target — OPT-030 filed; the p99 convention of a 100-sample set) and ONE landed wave-4 row, `i070_04_8c_pig_behind_bottom_slab_not_swept`: the ChainsawSweepSightTests half-block layout let the origin's sign, and beyond 2^23 (a float's ulp of 1) its parity, decide the walk's column — every "the ray admits" assertion had been vacuous at the earlier negative origins; the class now lies on the integer x / z lattice, re-derived and hand-replayed on both signs (the wave-4 records corrected in place, dated).

Refuted twice (MHLib and the client sampler touched; 13 files + 15 new): A (the instrumentation) — no blocking defect; eight items applied or disclosed: the S2C cadence expectation (≈6.7 / Queen / s for no-AI Queens, the builder's updateInterval 3), the second-encode caveat (under the property the byte accounting re-encodes on the sampled threads; identical under both labels, outside collector_ns / placement_ns), derived-not-measured wording, the broadcast count an upper bound with the snapshot's in-level count the honest figure, the collector span's exact bounds in 4.8.4's defaultRender (after preRender, before popPose / renderFinal), the 1 % low label (1000 / p99), line references, the probe slot's statics (no race); upheld: every §5 site once with the right thing counted, the client guards static-final and the server sites' plain read (≈31 per Queen per tick), the dump under the property on every dist with disjoint reset sets, the dump-order pins (the four names at 9-12, evictions 13, the gauge 14), the frame timer Pre→Pre and the MSPT slot. B (the harness) — one BLOCKING: the Queen grid (24-block pitch, rows to 246, columns ±108) reached 269 blocks — past the Queen's 256-block tracking cap and the default 12-chunk simulation distance — while the report normalised by count_spawned (≈96 tracked, ≈76 ticking); fixed by a wedge fill inside 180 blocks and the 70° FOV (Queen pitch 12 — no physics; 100 Queens within 160 blocks) with a coverage object and per-entity figures over the ticking / in-level counts; non-blocking: the headless walk's yaw-0 early-out (a yaw-π variant added — 32,864 B per walk, the baseline), the inactive tax's warm-up (a plateau detector; labelled a floor), the ns band (±40 %; bytes the primary signal), NaN in the JSON (null), a vacuous pitch assertion, a wrong no-AI rationale, a same-frame stop-then-start, the seam's restore, a working-tree reading beside git_head, records nits.

Fix lane 2 (the five red rows, each a harness assumption, no code fix): F1 the Chainsaw class's integer lattice (above); F2 the AntRobot's six legs pinned against the live profile; F3 / F4 the parts' first-tick snap (30 / 32 on the spawn tick, 20 / 24 after — OPT-030 filed, a real MHLib transient not fixed here); F5 nearest-rank p99 stated and pinned. Presented for the owner: the seam versus a mhlib.counters system property on the gameTestServer run; the brief's 'no-AI mobs fall' was wrong — travel's body sits behind isEffectiveAi, so they keep their placed position; scene B is not a culling control for the Queen (noCulling, shouldRenderAtSqrDistance → true) — it measures off-screen Queens drawn anyway; the ticking count is sampled at the end of the run; git_head cannot see a dirty tree beyond the index stat. The threshold is PROPOSED, not adopted: `phase_g_reports/benchmark/threshold_proposal_2026-09-06.md`; the owner's runbook `phase_g_reports/benchmark/owner_runbook.md`.

## PHASE G SLICE (f) — the artist package generator, dry-run on the landed species (2026-09-06; refuted once, fixed)

Owner 2026-09-05, scope addendum item 23 (8)(f): the package generator — SPEC, bone glossary, generated trigger inventory,
TEXTURE_MAP, INVENTORY.csv — built and dry-run on the landed species; nothing under `artist_handoff/` is committed until the
mirror drop lands (the drop changes the geometry of 97 models; a package generated before it would be regenerated). This slice
delivers the TOOL and its dry-run report; the package itself is generated later, on the drop. Repository writes: three new
things under `tools/` — `tools/artist_package.py` (3,335 lines, standard library only, tool version 0.2.0),
`tools/test_artist_package.py` (1,218 lines, a `unittest` runner, 24 pins), `tools/artist_specs/<registry>.json` (15 seeds).
No gradle, no gametest, no record-file edits, nothing under `artist_handoff/` (the tool refuses that path).

THE TOOL. One script, eight subcommands: `inventory` (INVENTORY.csv, one row per `ModEntities` registration: 145 rows —
both registration forms, `Builder.of(` and `Builder.<X>of(` — registry name, class, category, dims, model, tier AND the
artist-facing tier label, status landed candidate / classic only / excluded with a note in the vocabulary that is true for the
row (projectile / fish hook / item entity / vanilla-model reuse / head sidecar that renders nothing), files, bones, cubes,
clips, textures, the reference pin's scale and shadow, harness proof, hitbox profile, locked-bone count, artist scope, the
effort estimate and its source, client files); `texture-map` (TEXTURE_MAP.csv, every shipped textures/entity png: content
hash, canonical id, canonical name, duplicate-name twins, twins in other texture directories, canvas from the PNG IHDR parsed
by the tool, variant series with the contiguity law, stray class, Java consumers by a static scan of every file under
src/main/java, entity folders, 1.7.10 provenance, fan-out targets); `spec ENTITY` (SPEC.md + `spec.manifest.json`, the
machine-readable contract the checker reads — now carrying every bone's cube signatures, the lock policy, the keyed-locked
bones per shipped clip and the accepted clip set); `readme` (README_FIRST.md, the G5 contract, quoting `check`'s rule table);
`bbmodel ENTITY` (a Blockbench "Bedrock Entity" project from the geo + animation JSON, textures embedded); `roundtrip ENTITY`
(the .bbmodel back to geo + animation JSON under Blockbench's bedrock export rules, semantically diffed against the shipped
files — bone order as a list, cubes, easing and every other keyframe key); `package --out DIR` (the whole tree for the landed
species plus `dryrun_summary.{md,json}` and `warnings.txt`); `check FOLDER [--manifest FILE] [--lock-mode manifest|warn|reject]`
(validates a returned artist folder against the manifest: PASS / WARN / REJECT per the rule table README_FIRST prints, always
ending in a locked-bone summary line and a "checked N clip(s) ..." line; exit 1 on any REJECT). `main()` reconfigures
stdout/stderr to UTF-8 so the findings (§, —) survive a redirect on Windows.

ONE LOCKED-BONE POLICY (PROVISIONAL, open question 16). Contract §8.1 / P7 say the validator REJECTS a key on a SPEC-locked
bone; the pilot boss's shipped clips key 26 of her 27 locked bones (every one but `Body1`), so REJECT would fail her own
baseline. Until the owner rules, ONE sentence (`LOCK_POLICY`, artist_package.py :59-62) is printed identically in README_FIRST
rule 6, SPEC §3's glossary header, SPEC §5's footer, SPEC §7 and `check`'s summary line: keying a locked bone is a WARN today
and becomes a REJECT when the server-side hitbox evaluator lands. `lock_mode` in every manifest is `warn` (`LOCK_MODE_DEFAULT`);
the manifest key, or `check --lock-mode reject`, is the one switch. The Queen's §5 verdict rows say, per clip, "keys 26 locked
bone(s) ...: a WARN from `check` today; a REJECT once the evaluator lands — then this clip must be re-authored on unlocked bones,
or the lock lifted per bone by ruling"; her §7 states the 26-of-27 count clip by clip and that REJECT would fail the shipped file.

GENERATED vs AUTHORED. Generated, from the sources named in each SPEC: the size section (`ModEntities` dims, the reference
pin's scale/shadow); the bone glossary in the geo's own order (locked legacy name, readable label, the classic part path from
the Slice 4c `render_instances` sidecar for the clone rigs, parent, pivot, frequency group, `gait_bone`, `locked` with the
reason, cube count); the tempo table (natural period 2π/ω per group in ticks and seconds — and for a rectified shape (|cos|)
the VISIBLE period, half of it: Robot4's shield_arm 60 ticks natural / 30 ticks visible — the Blockbench preview rate at a 1 s
authoring length; the Beaver's gait 1.698 ticks / 11.78 clip-ticks per game tick, teeth 2.327, tail 12.566, matching
`demo_results.json` D); the clip table (contract clips per tier with the JSON `loop` value, layer, weight or trigger, the
bones — every bone of the rig for the base loops, the gait group marked as the speed-scaled one, other groups' bones pointed
at their own `<state>_<group>` layer, locked bones listed — length rule, code-triggered flag; the Queen's eight native clips
with the controller and the real trigger read from `registerControllers` and the call sites, nothing hard-coded); the trigger
inventory (every `addGoal` of `registerGoals` classified through a goal dictionary, with its `[modern: key]` guard where a
config gate encloses it and `UNPARSED` — never skipped — for a local-variable goal or a computed priority; every
`EntityDataAccessor`; every `setAttacking(N)` site with its enclosing method, the ACTUAL enclosing block's guard found by brace
depth over comment- and string-blanked source (a brace-less one-liner `if` included; an `else` written as the negated `if`
chain; an unreadable header reported as `(unparsed: ...)`), the outer guard chain, and the comment; melee `doHurtTarget` calls
AND the mod's area-damage helper `doAreaDamage` with how many times its body hurts each victim; projectile launches
(`LaserBall`, `BetterFireball`, `ThunderBolt`); native `triggerAnim` sites with a variable key resolved to the literals it is
assigned; the overrides; locomotion facts; and the "what fires each contract clip" table — for a species with SHIPPED native
clips a NATIVE table: its own clips with their triggers, the contract's generic names "not used by this species (native clip
set)"); the locked bones (a profile's `synched-bones` and every ancestor — the Queen: 27, the ten parts plus the neck and tail
chains and `root`); the textures (canonical + aliases + canvas); the reference slots; the effort estimate (a clip the seed
marks `leave`, or a `calm_idle` that `idle` covers, is not counted). Authored (the seeds, every one marked "draft for the
owner's edit"): the character-sheet paragraph, the size notes, the label dictionary (the Queen's 110 bones named by chain;
Robot4's 56; the named parts of the others), the frequency groups with the formula in plain English and the math with
file:line, the behaviour bullets, the improve/leave verdicts with the contract mapping, the extras (the Beaver's `chop`), the
wishlists (only clips the manifest accepts — the generator marks and warns on any other name) and, for the Queen, a `future`
list (§5.2: needs a code change and a ruling first).

DRY RUN (`package --out <scratch>\pkg\artist_handoff_dryrun`, exit 0, 149 files, 10 MB; stdout/stderr captured beside it):
all fifteen landed species — the fourteen replaced (beaver, coin, elevator, island, island_too, purple_power, robot_1..5,
rock_base, rotator, vortex) and the Queen. Per entity: 8 files (17 for the ten-texture Elevator and RockBase, 12 for
PurplePower's five, 9 for the Queen); bones 1..110; the Beaver's SPEC lists 11 clips (idle / idle_teeth / idle_tail / walk /
walk_teeth / walk_tail / swim / calm_idle / hurt / death / the `chop` extra), the Queen's 8 native, the Tier-3 thirteen none;
goals 0..8; the attacking flag classified STATE (Robot1, Robot2, the Queen), EVENT (Robot4 — with the caveat that `hurt()` :284
also raises it), MIXED (Robot3, Robot5: a 10- / 5-tick pulse per in-range think tick every 35 / 20 ticks, raised before the
line-of-sight gate, AND cleared on target loss), none (the rest); strike/launch sites 0..6 (the Queen: `doHurtTarget` :816,
`doAreaDamage` :1157 and :1186 whose body hurts each victim twice at :1429-1430, three launches); textures mapped 1 / 2 / 5 / 10;
round-trip EQUAL with bone order kept on every rig; effort the Queen 33.5 h (8 + 0.15 × 110 + 1.5 × 6), the Beaver 12.8 h
(4 + 0.2 × 9 + 1 × 7; `calm_idle` no longer counted), the Tier-3 thirteen 0 h. TEXTURE_MAP: 428 shipped, 338 unique payloads,
86 duplicate groups, 90 redundant names (the G0 Appendix A numbers exactly), 262 names referenced by Java (the design's 254
counted model/renderer references only; the extra 8 are `OreSpawnItemRenderer`'s held-weapon textures), 42 strays = 28
armor-sheet `_1/_2` twins of `textures/models/armor/*_layer_N.png` + 5 GUI/atlas files + 8 item-renderer weapon textures + 1
dormant twin (`hammytexture.png` = `textures/item/hammytexture.png`, referenced by nothing), the four law series contiguous.
WARNINGS (32): 29 bones without a label — exactly the unnamed classic `ShapeN` plates (Robot1 ×20, Robot2 ×4, Elevator ×5;
the placeholder carries the cube's size and position for the owner to name); 2 MIXED attacking verdicts (Robot3 :144/:155/:162,
Robot5 :131/:143/:152) for the owner to read; 1 LOCKED_BONES_KEYED — the Queen's shipped native clips key 26 of her 27 locked
bones (the policy sentence follows). No GOAL_UNPARSED, no ATTACKING_GUARD_UNPARSED, no WISHLIST_UNACCEPTED on the fifteen.

`check` ON THE DRY RUN AND ON MUTATED FOLDERS (scratch `pkg\refuter_mutations\`, eleven folders copied from the package with
the manifest, plus a path that does not exist): the Queen as shipped PASSes (exit 0) with eight per-clip WARN lines and the
summary "locked bones: 26 of 27 keyed across 8 clip(s) [lock_mode warn] — <policy>"; the same folder under `--lock-mode reject`
FAILs (exit 1: "REJECT locked bones: 26 of 27 keyed ..."); coin PASSes; the Beaver as shipped FAILs (required `idle` / `walk`
missing — the intended verdict for an un-animated return); a minimal Beaver return (idle + walk) PASSes; a renamed bone
(`tale`), a wrong loop, a Molang string, a key past `animation_length`, a changed cube size in a returned geo, a wrongly named
animation file, and a missing folder each FAIL with exit 1 and the REJECT line naming the rule; a `_preview` file WARNs
(PROVISIONAL, open question 15) and PASSes.

THE .bbmodel AND THE ROUND-TRIP (item 22 (11); built — it fit well under a quarter of the slice). The writer follows the
Blockbench 4.x bedrock codec as this lane knows it: pivot [x,y,z] → group origin [-x,y,z]; rotations negate X and Y, keep Z;
cube origin+size → from [-(x+sx),y,z] / to [-x,y+sy,z+sz]; per-face UV {uv,uv_size} → [u,v,u+w,v+h] (a negative uv_size
survives as a flipped face); box UV keeps uv_offset and mirror; a bone-level `mirror` is written back only for an all-box
project (Blockbench's `Project.box_uv` rule), otherwise it moves to the cubes with the same effect; animation values verbatim
in the Bedrock convention; loop true / hold_on_last_frame / absent ↔ "loop" / "hold" / "once"; deterministic uuid5 ids;
textures embedded as base64. The importer emulates the export (DFS over the outliner, so bone order = outliner order) and
the diff compares bone list AND order, parents, pivots, rotations, cubes (origin, size, inflate, pivot, rotation, effective
mirror, per-face or box UV), the description, clip names/order, loop, length, keys (time, values, lerp mode, pre/post) at
1e-6, AND the easing / easingArgs / any other keyframe key (a dropped easing or unknown key is a difference and is listed under
dropped_keys). Unknown description keys — the parked `orespawn:bone_draw_order` of branch g2-root-order — are PRESERVED by
this tool's writer/importer through `unhandled_root_fields` and reported as "reattached"; the converter's private cube key
`modelpart_mirror` has no Blockbench field and is reported as "dropped". A REAL Blockbench geo re-export drops both — which
is why README_FIRST forbids returning the geo: the artist returns the animation file. All fifteen shipped rigs are
DFS-consistent, so their order survives (a non-DFS rig is pinned to be reported as order CHANGED). STATED PLAINLY: the
round-trip is this tool's writer against this tool's importer — one memory of the Blockbench codec on both sides — so EQUAL
proves the two halves agree with each other; the owner's hand-check of one real Blockbench export (the first .bbmodel, then
the Queen's) is the real test (recorded in records.md).

PROVISIONAL FIELDS (tied to slice (e)'s open questions; the SPEC marks each): the loop authoring-length convention and the
`_preview` export (14, 15 — `check` warns on a delivered `_preview`), the death clip vs the vanilla flip (3), the hurt clip vs
the red overlay (4), the attack transport per species (11 — with the area-helper caveat: a `LivingDamageEvent.Post` per victim
would fire `attack` per victim per roll, twice each on the Queen), aggro_idle for species without a synched flag (12),
idle_alt cadence (5), the extras cap (6), Tier-3 extras (7), the density statement's wording (9, 10), the pilot boss and the
locked-bone policy (16) — the Queen's SPEC states her contract mapping as provisional and the lock as warn-mode.

REFUTER AND FIX LANE (one refuter, 2026-09-06; every item applied unless marked presented):
D1 applied — the one policy sentence in README rule 6 / SPEC §3, §5, §7 / `check`'s summary line; `lock_mode` warn for every
   species; the Queen's §5 rows and §7 say what keying 26 locked bones means under each policy (artist_package.py :59-62,
   :1767-1773, :1905-2230, :3054-3244).
D2 applied — `check` REJECTs a missing / empty folder, a missing / wrongly named / second animation file, a Molang string, a
   non-numeric or non-finite value, |rotation| > 3600° or |position| > 1024, a key past `animation_length`, a `timeline` key,
   a returned geo whose bones / parents / pivots / rotations / cubes / UVs / canvas differ (the round-trip's cube signature and
   `signature_differences` reused, the manifest carries the signatures), a wrong texture or canvas; WARNs where README says
   warn; the rule table (`CHECK_REJECTS` / `CHECK_WARNS`, :67-86) is printed by README_FIRST and SPEC §11 and pinned case by
   case (test_artist_package.py :995-1185).
D3 applied — `ENTITY_RE` matches both forms (:225-229): 145 rows; the note vocabulary per row (`excluded_note`, :538-550).
D4 applied — the design's "Vanilla-model reuse" table is read (`parse_vanilla_reuse_table`, :274-296): the six cows and
   spider_driver are Tier 0 with a true note; the head sidecars are classic only / Tier 3 and the note says they render nothing
   (`renders_nothing`, :528-535: the renderer's `shouldRender` returns false).
D5 applied — the NATIVE branch of the drive table (`contract_drives`, :1525-1560) and `native_clip_triggers` (:1476-1508):
   the Queen's eight clips with their real triggers (the Movement predicate lines 1538 / 1533 / 1536; the four strikes picked
   at :1177 under `if (this.pendingMeleeTicks == 0) within if (distanceToSqr < 900.0) within if (currentTarget != null)`;
   `death` from `die()` :1488), the generic names "not used by this species (native clip set)", `allow_idle_alt` false; the
   seed's wishlist rewritten to the accepted set, the three former wishes moved to `future` (§5.2), a `WISHLIST_UNACCEPTED`
   guard for any seed.
D6 applied — guards by brace depth (:916-1109): the Queen's :1131 reads `if (currentTarget != null)`; Robot2's :245 reads
   `NOT(if (rdd < 1.25))` (and :264 `NOT(if (this.justForFun > 0))`) as "other clears" the owner reads; Robot3 / Robot5's
   verdict wording is per in-range think tick (every 35 / 20 ticks, before the line-of-sight gate), not per shot
   (`ticker_facts`, :1260-1277); Robot4's transport-2 row carries the `hurt()` :284 caveat.
D7 applied — `doAreaDamage` in the melee scan (`AREA_HELPERS`, :1322-1367): the Queen's :1157 and :1186 (the brief said :1180;
   the call is at :1186), the helper's two `hurt` calls at :1429-1430, and the transport-1 caveat (`melee_transport_note`).
D8 applied — `parse_goals` (:1148-1214) lists a config-gated goal with `[modern: key]` (Girlfriend :228-231 would read
   `[modern: petsDefendOwner]`; she is not landed, so the fixture pins it) and reports a local-variable / computed-priority
   goal as UNPARSED with a `GOAL_UNPARSED` warning.
D9 applied — the base loops list every bone of the rig with the gait group marked (:1810-1832): the Beaver's idle / walk /
   swim rows name lff, lrf, rff, rrf (speed-scaled), body, head, nose (free), tail / teeth (their own layers).
D10 applied — `frequency_groups` states the visible half period of a rectified shape (:1735-1764).
D11 applied — easing and unknown keyframe keys compared (:2695-2705); the writer-and-importer statement above and in the
   report's note.
D12 applied — `_preview` is a WARN marked PROVISIONAL (open question 15).
D13 applied — the records (this entry, records.md): strays 28 + 5 + 8 + 1; 262 − 254 = 8 `OreSpawnItemRenderer` references;
   RockBase 10 textures; INVENTORY's `effort_hours` filled (+ `effort_source`); `calm_idle` not counted.
D14 applied — `Species.tier_label` (:512-522): "Tier 1 (boss; the design's 'done' row)" in SPEC §10, README's table, INVENTORY's
   `artist_tier`, the dry-run summary; the `idle_to_attack` hard-code removed.
D15 applied — 24 pins (14 → 24): D2's rejections (incl. the missing folder and `lock_mode: warn` / `reject`), D3's generic form,
   D4's table and the head sidecar, D8's guarded and unparsed goals, an EVENT and a MIXED verdict, the brace-less and one-liner
   guards, a non-DFS shipped geo, easing survival and a dropped unknown key, the item-renderer / dormant-twin strays, a
   canonical-name conflict (referenced beats registry-aligned), the native `clip_rows` branch and drive table, the area helper.
Presented (not done exactly as written): the refuter's own mutated folders were not available to this lane — equivalent
   mutations were rebuilt under scratch (the verdicts above); `case X:` labels without braces are not parsed (none in the
   fifteen; they would read `(unparsed: ...)`); the "stands in for the contract's X" phrase in the Queen's drive table comes
   from the seed's authored `contract` field, PROVISIONAL like the rest of her mapping; `ice_ball` / `acid` / `dead_irukandji`
   extend the mod's `LaserBall`, so the projectile vocabulary was widened to `*Ball`.

PINS: `tools/test_artist_package.py` — 24 tests over a synthetic repository (eight registrations in both forms; a nested rig
with per-face UV incl. a flipped face, a box-UV mirrored cube with its own pivot/rotation and an unknown description key;
three clips in the three keyframe shapes plus an easing key; a native GeoEntity boss with two controllers, a variable-key
trigger, a `die()` trigger and an area-damage helper; an EVENT pulser whose `hurt()` raises the flag; a MIXED ticker species;
a config-gated and an unparsable goal; hitbox profiles; seeds with a rectified group and an unaccepted wish; twin and series
textures; an armor-sheet stray, an item-renderer texture, a dormant item twin, a referenced-vs-registry-aligned twin pair).
`python tools/test_artist_package.py` → Ran 24 tests, OK.

GATE: (pkg, 2026-09-06, after the refuter and the fix lane): the tool is python under tools/ (no shipped class, asset or config changes) — the standing gate run as the proof of the tree: drift check clean; `build` exit 0 — asset audit `RESULT: 0 error(s), 0 advisory(ies), 4 acknowledged -> exit 0`, referenceGeometry `G1 PARITY PASS: 2 models; checked-in proof verified`, s4Parity `G1 PARITY PASS: 13 models; checked-in proof verified`; `runGameTestServer` exit 0 — literal `All 1254 required tests passed` (unchanged: no gametest in this slice); the tool's own pins `python tools/test_artist_package.py` — 24 tests OK; the dry run regenerated into the scratch directory (149 files; nothing under artist_handoff/ lands before the mirror drop); the summary in `phase_g_reports/artist_package_dryrun_2026-09-06.md`.

Refuted once (tooling, no shipped code): two blocking consistency items, thirteen non-blocking — all applied by the fix lane: ONE locked-bone policy in README_FIRST, the SPEC and `check` (keying a locked bone is a WARN today and becomes a REJECT when the server-side evaluator lands — PROVISIONAL, open question 16; the shipped Queen clips key 26 of her 27 locked bones, `root` among them against the seed's own label); `check` now enforces what the README promises (a missing or empty folder, a wrongly named or second animation file, a Molang-string value, a key past the clip length, non-finite or absurd values, changed cubes / UVs / sizes on a returned geo → REJECT; `_preview` a PROVISIONAL WARN, open question 15); the 17 `Builder.<X>of` registrations restored (145 inventory rows), the vanilla-reuse Tier-0 table read and the head sidecars labelled classic-only Tier 3; the Queen's drive table native (her clips' real triggers, `death` code-triggered from `die()`, the wishlist limited to accepted clips); the attacking verdicts' guards by brace depth (Robot4's `hurt()` caveat; Robot3 / Robot5's per-think-tick pulse; the Queen's guard shown correctly); the area-damage helper as a strike site with the per-victim caveat; guarded goals listed with their modern key, unparseable registrations reported; the Beaver's idle / walk bone lists the whole rig with the gait group marked scaled; the rectified period beside the natural one; the round-trip diff covering easing and unknown keyframe keys (the round-trip is writer and importer from one memory — the owner's hand-check of the first .bbmodel is the real test); the tier vocabulary (the Queen: Tier 1, boss; the design's 'done' row); the records' counts (strays 28 + 5 + 8 + 1; the 8 item-renderer references; RockBase 10 textures); 24 pins (was 14). Upheld: the texture map's dedupe (428 / 338 / 86 / 90, the four series contiguous, the armor strays), the locked-bone list (27 on the Queen), the round-trip EQUAL with bone order kept on every shipped rig, the tempo table's periods, the trigger counts, the README against G5's contract.

## RULINGS 2026-09-06 RECORDED (docs-only) — push; slice (c) approved with the pin removals and the missing-key policy; the (e) contract and controller rulings (Q1–Q17); the (d) thresholds adopted; the counters' system property; the (f) locked-bone policy; ENT-S-146 into 4c; ENT-S-147 closed (PN-023); the XS batch; the ENT-S-150 survey first; OPT-030 with the harvest; two ratifications; the order of work

Recorded in `phase_g_reports/phase_g_scope_addendum_2026-09-03.md` item 24; the register carries a dated line under each
entry named (ENT-S-130, 138, 145, 146, 147, 148, 149, 150, 151, OPT-030); PARITY_NOTES PN-023 closes ENT-S-147.
Executed here, docs-only: the push (origin/master 284972c before anything else landed); the (d) threshold proposal
`phase_g_reports/benchmark/threshold_proposal_2026-09-06.md` marked ADOPTED (R1–R7 as proposed; bytes lead R1, ns
informational within the ±40 % band; the mixed scene to be added before the first Tier-2 cutover; the live scenes the
owner's, off the critical path until a classic / candidate pair exists). Work in the ruled order: (c) gated and landed from
`g2-root-order` with the pin removals, the tools' default flipped to no exclusion, the missing-key fallback (logged when
absent, loud when present and wrong) and the asset-audit rule for the mod's own rigs → ENT-S-146 in 4c → the (e) records
and item 15 (the keyframe leg, the Q9 repair in the seam, the Beaver clip regenerated — presented before wiring) → the XS
batch (ENT-S-148 / 149 / 151) → the ENT-S-150 survey lane; item 17 (the counters' seam replaced by a system property on
the gametest run) and item 18 (the package tool's locked-bone policy) as small slices beside them; the mirror drop on
the owner's Section B go; the package and the pilot pair after it.

## PHASE G SLICE (c) LANDED — the G2 root-order contract: the pins removed, the exclusion off by default, the missing-key fallback, the audit rule (2026-09-06)

RULING. Owner 2026-09-06 (addendum item 24 (2)-(4)), approved from the presented
table (`phase_g_reports/g2_root_order_before_after_2026-09-06.md`): slice (c) gated
and landed from `g2-root-order` (0503bd0 on master) with the pin removals, the parity
tool's default flipped to no exclusion, the missing-key fallback (logged when absent,
loud when present and wrong) and an asset-audit ERROR for a shipped rig without the
key; the tie rule a fidelity note; the production seam's executed evidence the owner's
Section E look. This section records what the LANDING changed on top of the presented
branch; the presented slice's own section (G2 ROOT-ORDER CONTRACT, above) stands as
written. MHLib untouched; the renderer path (the shared replacement model) is touched,
so two refuters apply.

WHAT THE LANDING CHANGED VS THE PRESENTED BRANCH.
1. The pins. `tools/s4_model_proofs.json`: every `max_contested_fraction_pin` (13)
   and every `in_game_acceptance: PENDING_OWNER` string (island, islandtoo, robot5,
   purplepower) removed; `tools/g1_model_proofs.json`: the two pins removed. Fifteen
   pins, four strings, the fixtures untouched, nothing else removed. The notes that
   still hold keep their citation (refuter B, B2 - the landing's first rewording had
   dropped it): robot5's and rockbase's `visual_note` say bind is not a visual sample
   "(ruling 2, 2026-09-02: superimposed-at-bind is not a player-visible state; its
   z-fight clause retired by the G2 contract, 2026-09-06)"; robot2's `visual_note` is
   HEAD's text verbatim (it never cited ruling 2 or a pin); rotator's and purplepower's
   `render_instances_note` ended "z-fight contests are expected and pinned" and now end
   "...expected; the contested fraction is reported as a diagnostic, every pixel
   compared (G2 root-order contract, 2026-09-06)" - a manifest must not describe a pin
   that no longer exists (the notes beyond the task's robot2 / robot5 list are a
   deviation, presented in records.md). Against HEAD the ONLY differences are the
   removed fields and four note values (a byte-identical JSON round trip, checked).
2. The tool's default. `tools/g1_render_parity.py`: `visual_parity(...,
   exclude_contested=False)` - every pixel compared, the contested fraction MEASURED
   and reported (`max_contested_fraction`, per-capture `contested_fraction`,
   `contested_excluded`) as a diagnostic - a nonzero fraction with 0 changed pixels is
   the ordinary case now; `--contested-exclusion` restores the ruling-2 exclusion for
   a diagnostic run (replacing `--no-contested-exclusion`, which is gone, not aliased)
   and is refused together with `--write-proof` (the proof is written under the
   gate's policy only); the pin check and `IN_GAME_ACCEPTANCE_CONTESTED_FRACTION` are
   gone with the pins, the report's `contested_fraction_pin` and
   `requires_in_game_acceptance` fields with them; a manifest that still carries a
   retired field (`RETIRED_MANIFEST_FIELDS`) fails the leg, so a pin cannot linger
   silently; `z_fight_policy` names which policy ran; the README's intro gains the
   visual leg's new policy and the draw-order gate, and the per-model contested line
   reads "compared, none excluded (G2 root-order contract): ... a diagnostic". The
   pre-existing dead `min_foreground` line (refuter A's A9 kept it where it was) went
   with the pin block it lived in; `minimum_observed_foreground_fraction` was 1.0 in
   every report before and is 1.0 after (an observation, not a change). `build.gradle`
   untouched: g1Parity / s4Parity call the tool with no switch, i.e. the new default.
3. The missing-key policy. `DrawOrder.read` answers an ABSENT key (no `description`,
   or no such member) with an EMPTY order and throws for a key that is PRESENT and
   malformed (an explicit null, a non-array, a non-string or repeated entry, an empty
   array), its message the exact reason and naming no converter any more (refuter A,
   A4: on the client path the fix is the pack author's); `DrawOrder.load` lets that
   exception through unwrapped and wraps only a resource it cannot read. The seam's
   policy is `DrawOrder.applyOrFallback(model, resources, geo)`, the call
   `OreSpawnGeoReplacementModel.getBakedModel` now makes (once per bake, the identity
   test unchanged), returning a production `Decision(outcome, logged)` with `Outcome`
   APPLIED / ABSENT_FALLBACK / WRONG_KEY_FALLBACK (an overload over an already-read
   order is the same policy; the smoke and the row use both). An absent key leaves
   GeckoLib's own bone order in place and logs WARN once per resource. A key that is
   PRESENT AND WRONG - the read failures above, a name the rig lacks, a rig bone the
   key lacks, not a pre-order - or a resource that cannot be read does NOT crash the
   client either, the owner's "never" read as absolute (refuter A, A1; deviation 1 in
   records.md, the owner's call): it logs ERROR once per resource, naming the geo, the
   exact reason (`read`'s or `apply`'s own message) and the pack author's fix ("carry
   the orespawn:bone_draw_order array over from the shipped rig's description"), and
   takes the same fallback with the bake untouched; a right key is applied. Both lines
   open "Phase G draw order:" like the mod's other client logs. Once per resource is a
   static map geo -> the LAST reason: the same reason again is silent, a new reason
   (absent -> wrong, wrong -> another wrong, wrong -> absent) logs again, an applied
   bake clears the entry so a rig fixed and broken again is reported again; the map
   lives for the JVM on purpose (a design choice, refuter A, A3: it survives resource
   reloads, which re-bake every rig; process state, never game state). `apply`
   validates everything BEFORE it touches the bake (refuter A, A8: it used to sort in
   place and then throw on a name the rig lacks or a wrong pre-order, leaving a
   half-sorted bake - the refuter's probe showed [base, body, head, tail] left as
   [body, head, tail, base]): the set both ways on the tree as it stands, the pre-order
   against a sorted COPY, then the in-place sort; it still refuses an empty order. The
   logger is `LoggerFactory.getLogger("orespawn")` by literal (refuter A, A2: the
   lane's `OreSpawnMod.MOD_ID` left `#278 = Class // danger/orespawn/OreSpawnMod` in
   DrawOrder.class; the fix's class file has no `OreSpawnMod` entry at all, javap
   -v checked) - the same slf4j logger as `OreSpawnMod.LOGGER`, by name. The harness
   keeps requiring the key and never calls `applyOrFallback`: `S4CandidateRuntime
   .freshBaked` and `G1AnimationRuntime.evaluator` (the probe's geo dumps and the
   benchmark both bake through it) throw "harness failure: the generated geo ships
   without orespawn:bone_draw_order; the harness proves shipped rigs and takes no
   fallback", and `read` / `apply` / `load` keep throwing, so a proof can never come
   from a fallback bake.
4. The audit rule. `tools/asset_audit.py` check 8: ERROR `GECKO_GEO_DRAW_ORDER_MISSING`
   for any rig the replacement seam draws - the model resource of every
   `new GeoReplacementDescriptor<>(...)` (`seam_rigs`, a regex over the constructor's
   argument list; a construction without exactly one literal `geo/` resource is a
   STATIC-ANALYSIS LIMIT line, never silent) - whose `minecraft:geometry[0].description`
   lacks the key OR carries a wrong one (refuter B, B6: the content is checked, not
   presence only - a non-empty array of unique strings whose set equals the geo's
   bone-name set, else the same category with the reason: "is present but empty",
   "is present but not an array: ...", "holds non-string entries", "repeats ...", "does
   not name exactly the rig's bones (rig bones it lacks: ...; names the rig lacks:
   ...)"; the pre-order property is left to the client's `apply` and the harness's
   draw-order leg). And the directory is reconciled (refuter B, B3): every
   `geo/entity/*.geo.json` is a seam rig or a member of `OUTSIDE_SEAM = {"the_queen"}`
   with its dated reason (`QueenModel`, her own GeoModel, reads her rig; no descriptor;
   the contract derives the key from a vanilla part order she lacks; presented as a
   deviation), anything else - and a stale `OUTSIDE_SEAM` entry - is ERROR
   `GECKO_GEO_SEAM_UNRECONCILED`; a seam rig named by a descriptor but absent from the
   directory was already `TEXTURE_REF_MISSING`. Both categories are never
   acknowledgeable: they sit in `NEVER_ACKNOWLEDGED`, the ACKNOWLEDGED filter ignores
   them, and an ACKNOWLEDGED entry naming one is FATAL (exit 2). The summary line
   carries the reconciliation: "RESULT: ... ; draw order: 15 shipped geo: 14 seam + 1
   outside-seam". A key present and wrong is also caught for every proven rig by the
   existing byte check (`GECKO_GEO_PROOF_DRIFT`).
5. A pin. NEW `DrawOrderFallbackTests` (own batch `drawOrderFallback`, TEST-003: its one
   global touch is the synthetic resources it adds to the once-per-resource log map,
   so their names carry a per-run stamp), ONE row `g2_001_missing_key_falls_back_to
   _geckolib_order` (kept as one row, not a generator: the suite's count stays 1254 ->
   1255 for the gate), through the seam's own entry point over an in-memory
   `ResourceManager` (common classes; javap: no `net/minecraft/client` reference in
   the test class) serving a synthetic nested rig (body{head, tail}, base) baked through
   GeckoLib's own loader and factory (`KeyFramesAdapter.GEO_GSON`, `GeometryTree
   .fromModel`, `BakedModelFactory.DEFAULT_FACTORY` - none `@OnlyIn(CLIENT)`,
   javap-checked). Pinned through the outcome (refuter A, A5 / A6): absent ->
   ABSENT_FALLBACK logged on the first call, NOT on the second (same resource), logged
   again for a second resource, the factory's traversal left exactly as found (asserted
   a pre-order of the rig, whatever the hash order is); a correct key -> APPLIED, the
   contracted traversal, idempotent, silent; each of eleven wrong shapes (an explicit
   null, a string, an object, an empty array, a non-string entry, a JSON null entry, a
   repeat, an unknown name, a missing rig bone, a child ahead of its parent, an
   interleaved subtree) -> WRONG_KEY_FALLBACK, never a throw, the traversal IDENTICAL to
   the pre-call traversal (the bake staged into [base, body, tail, head], which the old
   partial sort would have changed for three of them), logged once per resource; the
   last-reason rule on one resource (absent -> wrong twice, the same wrong silent,
   another wrong again, wrong -> absent again, fixed then broken again); an unreadable
   resource -> WRONG_KEY_FALLBACK; and the strict `read` / `apply` still throw on the
   same inputs, the refused `apply` touching nothing. Compiles against the scratch main
   classes; not executed here (no gradle) - the same calls ran headlessly in the smoke
   below.

THE TIE RULE (fidelity note, ruling (4)). The harness rasteriser resolves an EXACT
depth tie first-wins (`render_capture`: a later fragment within 1e-6 replaces the
front only when nearer by more than 1e-9) where the game's LEQUAL depth test is
last-wins (a later coplanar fragment overwrites). Parity is unaffected: both captures
share the rule and, under the contract, the order, so the leg compares like with like;
what the diff PNGs cannot show is which of two coplanar faces the GAME shows - the
before/after table (`phase_g_reports/g2_root_order_before_after_2026-09-06.md`) says
it (last-wins, the classic order) and the owner's Section E look is that evidence.
Recorded in `render_capture`'s docstring; not changed.

THE SECTION E LOOK. The production seam (`OreSpawnGeoReplacementModel.getBakedModel`,
client-only through `Minecraft.getInstance()`) is proven by reading (refuter A's
bytecode trace: `GeoRenderer.defaultRender` 32-49 calls the 1-arg override before any
draw; the sorted instance feeds every consumer) and executed only by the owner's
in-game Section E look with the dev switch - recorded here as the seam's executed
evidence; the statics it calls are executed headlessly (the smoke) and on the game-test
server (the row).

EVIDENCE (scratch runs, both pipelines, the new default; all with `timeout`; exit codes):
  javac main (798 sources, `--release 21 -g`, scratch class dirs first): 0 (16
  pre-existing warnings); javac g1tool (9): 0; javac gametest (67, incl. the new
  class): 0. `G1ModelProbe vanilla` s4 / g1: 0 / 0 (14 / 3 dumps);
  `layer_definition_to_geo.py` s4 / g1: 0 / 0 (14 + 3 CONVERT GREEN);
  `reference_geometry_leg.py` s4: 0; `G1ModelProbe geo` s4 / g1: 0 / 0;
  `g1_render_parity.py --write-proof` (scratch proof dirs, NO switch) s4 / g1: 0 / 0 -
  `G1 PARITY PASS: 13 models` / `2 models`; `--contested-exclusion --validate-only`
  s4: 0 (`STAGING PASS`, Island's line "excluded: --contested-exclusion diagnostic
  run"); `--contested-exclusion --write-proof`: 2 (refused, no proof dir made); the
  retired-field guard: fires on a spec carrying a pin; `G1PerformanceBenchmark ... smoke`
  -> scratch report: 0 (4 scenes measured; source pins that will re-pin:
  G1PerformanceBenchmark.java, G1AnimationRuntime.java, g1_model_proofs.json); the
  fallback smoke (scratch javac + java over the SHIPPED assets, re-run on the fixed
  seam): 0 - `SMOKE OK: 14 seam rigs APPLIED from the shipped assets, the Queen
  ABSENT_FALLBACK (one WARN), 11 present-and-wrong shapes + 1 missing resource
  WRONG_KEY_FALLBACK (one ERROR each, the bake untouched), the strict statics throw`;
  14 ERROR lines and 2 WARN lines under `[orespawn/]` in all (eleven shapes, the
  missing resource, two for the churn resource; the Queen and the churn's absent step).
  Per species (changed / MAE / contested, the last a diagnostic):
    s4  elevator 0 / 0 / 0; vortex 0.000137 / 0.0042 / 0 (the accepted 4a-2 boundary
    residual, bind.front); coin 0 / 0 / 0; island 0 / 0 / 0.464233; islandtoo 0 / 0 /
    0.464233; robot1 0 / 0 / 0.000549; robot5 0 / 0 / 0.011612; robot2 0 / 0 / 0;
    robot3 0 / 0 / 0.002502; robot4 0 / 0 / 0.000412; rockbase 0 / 0 / 0; rotator
    0 / 0 / 0; purplepower 0 / 0 / 0.105850; fixture_runtime_basis_yz no visual leg
    (draw order 7 captures / 14 draws).
    g1  elevator 0 / 0 / 0; beaver 0 / 0 / 0.000015; fixture no visual leg (1 / 2).
  Draw-order leg: PASS for all 17 entries (s4 142 captures / 2,307 draws; g1 28 / 221).
  Asset audit (read-only, the tree as it stands, the fixed rule): `RESULT: 15
  error(s), 0 advisory(ies), 4 acknowledged; draw order: 15 shipped geo: 14 seam + 1
  outside-seam -> exit 1` - the 15 are the pre-existing GECKO_GEO_PROOF_DRIFT (the 14
  shipped rigs against the not-yet-regenerated proofs, elevator against both), 0
  GECKO_GEO_DRAW_ORDER_MISSING, 0 GECKO_GEO_SEAM_UNRECONCILED, the STATIC-ANALYSIS
  LIMITS identical before and after the rule (the 5 pre-existing), `seam_rigs`
  enumerating exactly the 14 seam rigs. On a scratch COPY of the tree with the
  scratch-regenerated proofs substituted (what the orchestrator's --write-proof
  produces): `RESULT: 0 error(s), 1 advisory(ies), 4 acknowledged; draw order: 15
  shipped geo: 14 seam + 1 outside-seam -> exit 0` (the advisory is
  INDEX_CASE_UNCHECKED: the copy is outside git). On that copy with the key REMOVED
  from robot5 (shipped copy AND its proof copy, so the byte check stays quiet):
  `RESULT: 1 error(s), ...` - `GECKO_GEO_DRAW_ORDER_MISSING: robot5.geo ... is absent`;
  with the key EMPTIED (`[]`) instead: `RESULT: 1 error(s), ...` -
  `GECKO_GEO_DRAW_ORDER_MISSING: robot5.geo ... is present but empty`; with the Queen
  removed from `OUTSIDE_SEAM`: `RESULT: 1 error(s), ...; draw order: 15 shipped geo: 14
  seam + 0 outside-seam` - `GECKO_GEO_SEAM_UNRECONCILED: the_queen.geo`. The shipped
  files and the checked-in proof dirs were never written.

BYTE IDENTITY (scratch proofs). Against the presented lane's exclusion-OFF scratch runs
(the G2 code with `--no-contested-exclusion`): s4 258 of 260 files identical, g1 42 of
44 - only `evidence/report.json` (the two removed pin fields, `z_fight_policy`'s
wording) and `evidence/README.md` differ; every PNG identical, so the default flip
touched nothing else. Against the CHECKED-IN proofs (which predate G2): identical for
every animation.json, animation-contract.json, the reference-geometry json, every
vanilla PNG and every non-contested geo / diff PNG (s4 207 of 260, g1 34 of 44);
differing exactly the key-bearing files and the evidence texts. The orchestrator's
regeneration WILL change: s4_proof - `generated/*.geo.json` (14, the key) and
`generated/*.conversion.json` (14), `evidence/report.json`, `evidence/README.md`, 14
diff PNGs (island bind / t0, islandtoo bind / t0, purplepower bind / t_quarter, robot1
a0_5_t0 / a0_5_t_half / a0_5_t_three_quarter, robot3 s_attacking_ri0_a0_5_t_0_230000 /
s_idle_ri1_a0_5_t_0_100000, robot4 s_idle_a0_t0, robot5 a0_05000000074505806_t0 /
a1_t0 - no blue paint) and 9 geo PNGs (island bind, islandtoo bind, purplepower bind /
t_quarter, robot1 a0_5_t0 / a0_5_t_half / a0_5_t_three_quarter, robot5
a0_05000000074505806_t0 / a1_t0 - the classic winner shows); g1_proof -
`generated/*.geo.json` (3), `generated/*.conversion.json` (3), `evidence/report.json`,
`evidence/README.md`, beaver `a1_t0.diff.png` and `a1_t0.geo.png`; the benchmark
re-pin - `g1_proof/benchmark/report.json` (its source pins, model inputs, class-dir
pin) and whatever of README.md / protocol.json the gate rewrites. Nothing else.

UNTOUCHED, ON PURPOSE: `build.gradle`; the shipped rigs; the checked-in proof dirs; the
converter; the probe; MHLib; the Queen; `tools/artist_package.py`,
`tools/test_artist_package.py`, `tools/artist_specs/` (another lane's).

REFUTER A NOTES (2026-09-06, the Java seam; applied): (A1) "loud when present and
wrong" was a throw under `EntityRenderDispatcher.render`, a client crash on the mob's
first frame - the same crash the ruling forbids for an absent key; read as an ERROR log
once per resource plus the fallback, the crash report one line away, presented to the
owner as deviation 1. (A2) `LoggerFactory.getLogger(OreSpawnMod.MOD_ID)` left `#278 =
Class // danger/orespawn/OreSpawnMod` in DrawOrder.class despite the constant folding;
the literal `"orespawn"` with a comment, javap -v: no `OreSpawnMod` entry at all.
(A3) the once-per-resource set's lifetime was undocumented; now a map geo -> the last
reason, JVM lifetime stated in the javadoc and the records as a design choice. (A4)
`read`'s messages told a pack author to run `tools/layer_definition_to_geo.py`; the
client path names the pack author's fix instead, the harness keeps its own wording.
(A5) the row pinned a boolean; it pins the production `Decision` (outcome + logged),
absent logged once then silent, a second resource logged again. (A6) the wrong shapes
were pinned as throws; each of eleven is pinned as WRONG_KEY_FALLBACK with the traversal
identical to the pre-call one, logged once per resource. (A7) the model's field comment
said "the bake `DrawOrder#apply` last ran on"; it says the bake the seam last decided on
(applied or fell back). (A8) `apply` sorted in place before it validated (a name the rig
lacks, a wrong pre-order: half-sorted bake left behind, shown by the refuter's probe);
it validates the set both ways and the pre-order against a sorted copy first, sorts
only on success, pinned by the unchanged-traversal assertions. (A9) the row's loading path is
`@OnlyIn`-clean (javap: no `net/minecraft/client` reference in the test class; none of the
thirty GeckoLib loader classes it reaches - `BakedModelFactory`, `GeometryTree`,
`KeyFramesAdapter`, `GeoBone` and the rest - carries `@OnlyIn`; `RenderUtil`, reached from
`Builtin.constructGeoModel`, has client references in other methods but no annotation and
no static initialiser), and it is the FIRST GeckoLib bake on the game-test server - no
earlier row bakes a rig - so the gate run is the first evidence that the bake loads on the
dedicated server (noted as low risk; the gate below decides). (A10) the "rig repeats bone"
branch (now in `check`) is unreachable from a GeckoLib bake - `GeometryTree.fromModel`
builds its maps with `put`, so a duplicated name collapses silently - and stays as
belt-and-braces, harmless.

REFUTER B NOTES (2026-09-06, tools / proofs / records; applied or the orchestrator's):
(B1) records, orchestrator. (B2) the reworded bind notes had lost the ruling-2 citation;
robot5's and rockbase's `visual_note` cite it with the retired clause, robot2's is
HEAD's text verbatim (it never cited ruling 2 or a pin), rotator's and purplepower's
stay, the JSON round trip byte-identical and the diff against HEAD only the removed
fields and four note values. (B3) the audit's coverage was the seam's by construction,
so a rig outside the seam passed silently; every `geo/entity/*.geo.json` is now
reconciled - a seam rig or a dated `OUTSIDE_SEAM` member (the Queen) - else
`GECKO_GEO_SEAM_UNRECONCILED`, never acknowledgeable, the count on the summary line.
(B4) records, orchestrator. (B5) records, orchestrator. (B6) the rule checked presence
only, so an empty or malformed key in a shipped rig would have reached the client as a
log; the content is validated (non-empty, unique strings, the set equal to the geo's
bones) under the same category with the reason. (B7) records, orchestrator. (B8)
records, orchestrator. (B9) "the look sheet" was used for the before/after table; the
records name `phase_g_reports/g2_root_order_before_after_2026-09-06.md` wherever that
was meant (the look sheet is `owner_look_sheet.md`). (B10) records, orchestrator. (B11)
records, orchestrator.

GATE: (g2land then g2land2, 2026-09-06 13:41-15:05, after both refuters and the fix lane): the proofs regenerated by hand under the proof rule with the exclusion off - s4 "G1 PARITY PASS: 13 models; checked-in proof updated"; g1 verify drifted (the two pins removed from the manifest) then "G1 PARITY PASS: 2 models; checked-in proof updated"; the benchmark "G1 BENCHMARK EVIDENCE VERIFIED: SMOKE_ONLY / COMPONENT_PROXY_ONLY / PENDING_LIVE_PRECUTOVER; checked-in proof updated" (re-pinned: G1AnimationRuntime.java, G1PerformanceBenchmark.java and g1_model_proofs.json - the first two had gone stale at the presented commit, as its section disclosed). Gate g2land (13:42-13:45): drift verified, build successful, suite RED - "9 required tests failed", all nine ChainsawSweepSightTests rows on their own origin precondition (this run's grid origin z -14,593,049, |z| >= 2^23; nothing of this landing implicated - the rows are fixed in their own section below, CHAINSAW SWEEP ROWS AT A FIXED SITE). Gate g2land2 (15:02-15:05) on the same tree: drift verified ("checked-in proof verified"), build successful, "All 1255 required tests passed" (1254 + DrawOrderFallbackTests' one row; the batch drawOrderFallback:0 ran - the first GeckoLib bake on the dedicated game-test server loaded).

REFUTERS (2026-09-06): A (the Java seam and the harness runtimes) and B (the tools, the manifests, the audit rule, the proof byte identity, the records) - no blocker; their items applied by the fix lane and the orchestrator (REFUTER A NOTES / REFUTER B NOTES above); the reading of "loud" and the Queen's place outside the seam are presented to the owner in this section's deviations.

## PHASE G SLICE (f), ITEM 18 — the locked-bone policy as ruled (2026-09-06)

Owner 2026-09-06, scope addendum item 24 (18): keying a locked bone is allowed and WARNED (the SPEC states the consequence:
the hitbox part follows the bone); renaming, re-parenting or deleting one is REFUSED; the README, the Queen's SPEC and the
checker say the same thing; PROVISIONAL comes off; the reject mode stays available for the day the server-side evaluator
lands. Three files touched: `tools/artist_package.py` (0.2.0 → 0.2.1; 3,335 → 3,371 lines), `tools/test_artist_package.py`
(24 → 25 pins; 1,218 → 1,305 lines), `tools/artist_specs/the_queen.json` (one label). No gradle, no state-changing git, no
record-file edits, nothing under `artist_handoff/` (the dry run regenerated under scratch only). The other open questions of
the presented contract (the Queen's pilot-boss / contract mapping — 16 —, the death flip — 3 —, `_preview` — 15 —, the
authoring length — 14 —, the attacking-flag heuristic) stay PROVISIONAL where they appear; only the lock policy is ruled.

ONE WORDING. `LOCK_POLICY` (artist_package.py :63-64) is the ruled sentence — "Keying a locked bone is allowed; the checker
warns, and the hitbox part follows the bone in-game (the consequence, so keep such keys deliberate). Renaming, re-parenting or
deleting a locked bone is refused." — printed verbatim in README_FIRST rule 6 and its WARN table, SPEC §3's glossary header,
§5's per-clip notes (`lock_note`, :1772-1777) and native footer (:2064), §7 (:2153-2164), §11 item 2 (:2200), the generator's
LOCKED_BONES_KEYED warning (:1929-1932), the manifest's `lock_policy_text` (:2218) and `check`'s summary line (:3262-3272).
"PROVISIONAL" / "open question 16" are gone from the policy everywhere it appears: the constant and its comment (:55-66),
README rule 6 (:2813), the SPEC's §3 / §5 / §7 / §11, the manifest's lock fields, `check`'s output and the `--lock-mode` help
(:3301-3303). The contract-mapping marker on a native clip row (still PROVISIONAL, open question 16, as the brief leaves it) is
now written out — "PROVISIONAL (its contract mapping: open question 16)" — and placed BEFORE the lock note (:1804-1808), so
the trailing " PROVISIONAL" the §5 table used to append can no longer read as the lock policy's. The reject mode is kept and
said once per document (`LOCK_REJECT_MODE`, :65-66 — README rule 6, SPEC §7): "A reject mode for keys on locked bones stays
available for the day the server-side hitbox evaluator lands (`check --lock-mode reject`, or `lock_mode: reject` in the
manifest); it is not today's policy." `LOCK_MODE_DEFAULT` stays `warn`. README rule 6 no longer opens "Never key a bone the
sheet marks `locked`" — it opens with what a locked bone is and then the sentence; rule 1 (never rename, delete or re-parent
ANY bone) is unchanged.

THE MANIFEST. `lock_policy` is the token "warn-keyed, refuse-structural" (`LOCK_POLICY_ID`, :62); `lock_policy_text` carries
the sentence (:2218); `lock_mode` stays `warn`. `check` prints the token beside the sentence in its summary line and WARNS
(:3082-3084) when a manifest's `lock_policy` is anything else — a package generated before the ruling: "lock_policy is not
`warn-keyed, refuse-structural` — the package predates the 2026-09-06 ruling; regenerate it (the checker applies the ruled
policy regardless)" — so an old manifest never re-prints the PROVISIONAL sentence.

THE CHECKER. The geo comparison (:3098-3135) names a locked bone whose name, parent or presence changed: a locked bone
missing from the returned rig → REJECT "locked bone X renamed or deleted (missing from the returned rig; the new name(s) [...]
are not this rig's) — it carries or parents a hitbox part; renaming, re-parenting or deleting a locked bone is refused"; a
locked bone under another parent → REJECT "locked bone X re-parented (old -> new) — it carries or parents a hitbox part; ...
refused"; a free bone re-parented stays the plain rig-rule REJECT ("bone X re-parented (old -> new)"). Verified as the brief
asked: the per-bone comparison (parents, pivots, bind rotations, cubes / UVs — the last since the refuter) used to run only
when the returned bone list matched the manifest's exactly; it now runs for every bone present in both rigs whatever the
set/order verdict, so a re-parent beside a rename is still named. Keys on locked bones stay the per-clip WARN with the count
line (:3229-3233), now ending "(they carry or parent a hitbox part; the part follows the bone in-game — allowed, keep it
deliberate)"; under `reject` mode the same line is a REJECT ending "REJECTED under lock_mode reject". The summary line:
"locked bones: K of N keyed across C clip(s)[; S renamed, re-parented or deleted (names) — REJECTED] [lock_mode M;
warn-keyed, refuse-structural] — <the sentence>" — REJECT when anything structural was refused, WARN when keyed, OK otherwise;
in reject mode the sentence is prefixed "keys on locked bones are REJECTED in this run (the reject mode, kept for the day the
server-side hitbox evaluator lands); the ruled policy: ...". `CHECK_REJECTS` gains "a `locked` bone renamed, re-parented or
deleted in a returned `.geo.json` (the finding names the bone)" (:81 — README's REJECT table and SPEC §11 item 4 quote it);
`CHECK_WARNS[0]` names the clip and bones (:85); SPEC §11 item 1 adds "(a `locked` bone renamed, re-parented or deleted is
refused by name)" (:2197-2198).

THE QUEEN. Her seed's `root` label said "carries every hitbox part — never key it"; it now states the consequence ("every
hitbox part hangs from it, so a key here moves them all with it — allowed and warned like any locked bone, but leave it to the
yaw"). Her regenerated SPEC: the §3 header, the eight §5 rows ("<seed note> — PROVISIONAL (its contract mapping: open
question 16) — keys 26 locked bone(s) (LHead, LHead12, LHead4, Lwing1, +22 more): <the sentence>"), the §5 footer ("Locked
bones: <the sentence>"), §7 ("The synced part bones and every ancestor are SPEC-locked (contract §8.1). <the sentence> <the
reject-mode sentence>" and "The shipped clips already key 26 of these 27 bones — `idle` keys 26; ... `death` keys 26 — allowed
and warned as above: the hitbox parts follow those bones in-game, which is how the shipped boss already animates; every §5
verdict that invites an edit to one of these clips repeats the consequence") and §11 carry the one wording; the pilot-boss /
contract-mapping marks (open question 16 in `artist_scope`, §5's footer, §6's drive table, §5.2's third item), the death flip
(3) and the attacking-flag heuristic stay as they were.

DRY RUN regenerated in place (`package --out <scratch>\pkg\artist_handoff_dryrun`, exit 0, stdout / stderr beside it as
`dryrun_stdout_lock.txt` / `dryrun_stderr_lock.txt`): fifteen species, every round-trip EQUAL with order kept, the Queen's row
"27 (26 keyed by the shipped clips; warn)", the one LOCKED_BONES_KEYED warning now "the shipped clips key 26 of the 27
SPEC-locked bones (LHead, LHead12, LHead4, Lwing1, NeckL1, NeckL13...): allowed and warned — <the sentence>"; no PROVISIONAL
text attached to a lock statement in README_FIRST.md, the Queen's SPEC.md, spec.manifest.json or warnings.txt (grepped: the
remaining PROVISIONAL lines are the generic header sentence, the contract-mapping marks, the death flip, `_preview`, the
authoring length and the attacking-flag heuristic).

CHECK OUTPUTS (`<scratch>\pkg\lock_mutations\*` built by `make_lock_mutations.py` from the regenerated package; transcripts
under `<scratch>\pkg\lock_checks\*.txt`):
- the_queen_as_shipped (manifest + geo + animation + textures as the package copies them) → PASS, exit 0:
  `WARN   the_queen.geo.json: a geo was returned; the shipped rig is used regardless (do not re-export the geo)`
  8 × `WARN   clip '<idle|idle_to_attack|attack|bite|tail_whip_right|tail_whip_left|roar|death>' keys 26 locked bone(s): root,
  leftLeg, Lwing1, NeckL1, NeckL2, NeckL3, +20 more (they carry or parent a hitbox part; the part follows the bone in-game —
  allowed, keep it deliberate)`
  `WARN   locked bones: 26 of 27 keyed across 8 clip(s) [lock_mode warn; warn-keyed, refuse-structural] — Keying a locked bone
  is allowed; the checker warns, and the hitbox part follows the bone in-game (the consequence, so keep such keys deliberate).
  Renaming, re-parenting or deleting a locked bone is refused.`
  `OK     checked 8 clip(s), 47559 keyframe value(s), 2 texture(s) against spec.manifest.json (the_queen)` / `PASS`.
  No PROVISIONAL text. (The dry run's own `entities/the_queen` folder: the same plus `OK the_queen.bbmodel: parses`, PASS.)
- the_queen_locked_renamed (`LHead`, a synced head part, renamed `LeftHead`; its four children re-pointed, as a Blockbench
  rename does) → FAIL, exit 1:
  `REJECT the_queen.geo.json: bone set/order changed (missing ['LHead'], added ['LeftHead'], order changed)`
  `REJECT the_queen.geo.json: locked bone LHead renamed or deleted (missing from the returned rig; the new name(s) ['LeftHead']
  are not this rig's) — it carries or parents a hitbox part; renaming, re-parenting or deleting a locked bone is refused`
  4 × `REJECT the_queen.geo.json: bone <LHead1|LHead2|LHead3|LJaw1> re-parented (LHead -> LeftHead)`
  the geo WARN and the 8 keyed WARNs as above
  `REJECT locked bones: 26 of 27 keyed across 8 clip(s); 1 renamed, re-parented or deleted (LHead renamed or deleted) — REJECTED
  [lock_mode warn; warn-keyed, refuse-structural] — <the sentence>` / `OK checked 8 clip(s), 47559 ...` / `FAIL`.
- the_queen_locked_reparented (`Tail4`, a synced tail part, moved from `Tail3` to `root`; names and order intact) → FAIL, exit 1:
  `REJECT the_queen.geo.json: locked bone Tail4 re-parented (Tail3 -> root) — it carries or parents a hitbox part; renaming,
  re-parenting or deleting a locked bone is refused`; the geo WARN and the keyed WARNs;
  `REJECT locked bones: 26 of 27 keyed across 8 clip(s); 1 renamed, re-parented or deleted (Tail4 re-parented) — REJECTED
  [lock_mode warn; warn-keyed, refuse-structural] — <the sentence>` / `FAIL`.
- the_queen_locked_deleted (`Body1`, a synced part and a leaf, removed) → FAIL, exit 1:
  `REJECT the_queen.geo.json: bone set/order changed (missing ['Body1'], added [], order changed)`
  `REJECT the_queen.geo.json: locked bone Body1 renamed or deleted (missing from the returned rig) — it carries or parents a
  hitbox part; renaming, re-parenting or deleting a locked bone is refused`; the geo WARN and the keyed WARNs;
  `REJECT locked bones: 26 of 27 keyed across 8 clip(s); 1 renamed, re-parented or deleted (Body1 renamed or deleted) —
  REJECTED [lock_mode warn; warn-keyed, refuse-structural] — <the sentence>` / `FAIL`.
- the_queen_as_shipped with `--lock-mode reject` (the mode kept for the evaluator) → FAIL, exit 1:
  8 × `REJECT clip '<name>' keys 26 locked bone(s): root, leftLeg, Lwing1, NeckL1, NeckL2, NeckL3, +20 more (they carry or
  parent a hitbox part; REJECTED under lock_mode reject)`
  `REJECT locked bones: 26 of 27 keyed across 8 clip(s) [lock_mode reject; warn-keyed, refuse-structural] — keys on locked
  bones are REJECTED in this run (the reject mode, kept for the day the server-side hitbox evaluator lands); the ruled policy:
  <the sentence>` / `FAIL`.
- the pre-ruling refuter copy (`refuter_mutations\the_queen_as_shipped`, its manifest from 0.2.0) → PASS, exit 0, with the new
  first line `WARN   spec.manifest.json: lock_policy is not `warn-keyed, refuse-structural` — the package predates the
  2026-09-06 ruling; regenerate it (the checker applies the ruled policy regardless)` and the ruled sentence in the summary —
  the old PROVISIONAL sentence is never re-printed.

PINS: `python tools/test_artist_package.py` → Ran 25 tests, OK (24 → 25). Adjusted: `test_spec_and_manifest` (:788-795 the
sentence in §3 / §7 / §11 and `LOCK_REJECT_MODE` in §7, no PROVISIONAL / open question 16 in `LOCK_POLICY` or §7; :809-811
`lock_policy` == "warn-keyed, refuse-structural", `lock_policy_text` == the sentence), `test_native_clip_rows_branch_and_
wishlist_guard` (:859-866 the per-clip note is "keys 1 locked bone(s) (head): <the sentence>", the mapping marker precedes it,
the note no longer ends in "PROVISIONAL"; :870-871 §7's "allowed and warned as above"), `test_package_and_check_rejections`
(:1021-1024 README rule 6 carries both sentences and no "Never key a bone", the structural rule in the REJECT table; :1034 the
clean summary; :1061-1091 keyed → WARN with the consequence and nothing PROVISIONAL in the findings, `--lock-mode reject` and
`lock_mode: reject` → REJECT with the reject-mode summary, a stale 0.2.0-style manifest → the WARN and the ruled sentence),
`test_check_on_a_native_species` (:1259-1260 the summary format, nothing PROVISIONAL; :1269-1272 the reject-mode text). New:
`test_check_refuses_a_locked_bone_renamed_reparented_or_deleted` (:1195-1249) — renamed (`hand` → `paw`: the bone and the new
name named, the summary counts it), re-parented (`hand`: `arm` → `root`), deleted, a re-parent beside a rename (`arm` → `tail`
with `hand` → `paw`: both named — the per-bone comparison runs whatever the set verdict), a free bone re-parented (`tail`: the
rig-rule REJECT, not called locked, the locked summary OK), and a keyed locked bone beside a structural refusal (the WARN
stands beside the REJECT: the key is allowed, the structure is not).

Presented (not done exactly as written): the tool version was bumped 0.2.0 → 0.2.1 so a package generated under the ruled
policy is distinguishable from the 0.2.0 dry run (the `generated_by` field, the README / SPEC headers, the summary title) —
not asked by the brief; the manifest gained `lock_policy_text` beside the token so the sentence travels with the package;
`check` gained the stale-manifest WARN; a locked bone renamed and one deleted are one finding ("renamed or deleted") because a
returned geo cannot tell them apart (the new names, when any, are listed in the line); the records that describe the
pre-ruling policy — `phase_g_reports/artist_package_dryrun_2026-09-06.md` :60-61 / :87-89 (the old sentence, 0.2.0), the scratch
drafts `fixlog.txt` ("ONE LOCKED-BONE POLICY (PROVISIONAL, open question 16)") and `records.md` (deviation 4, open question 3)
— were NOT edited (no record-file edits); this entry supersedes them.

REFUTER NOTES (2026-09-06, one refuter, no blocker; applied): (D1) the structural test had no unlocked-bone case, so the
`name in locked` guard of the missing-bone loop could be mutated to `if True:` unseen — `test_check_refuses_a_locked_bone_
renamed_reparented_or_deleted` (:1203-1298) gained an unlocked bone deleted and one renamed (`tail` gone / `tail` → `tale`:
the set line, the bone named under the rig rule, no "locked bone" finding, the summary `OK locked bones: 0 of 3 keyed across
0 clip(s) [...]`, `passed` false); the mutant applied to a SCRATCH copy of the tool (`fix18\mutant_both|label|count\`: both
halves of the guard, and each half alone) fails that test — verified, the repo file untouched by the mutation. (D2)
`DEFAULT_LABELS["root"]` (:1611) said "never keyed by an artist clip" — reworded to the consequence: "a key here moves every
part with it: allowed and warned like any locked bone, leave it to the yaw"; the grep for "never key" over the tool, the tests
and `tools/artist_specs/*.json` finds nothing else (the test's `assertNotIn("Never key a bone", readme)` is a negative pin and
stays; the Queen's seed label already carried the ruled wording and is unchanged); pins: no DEFAULT_LABELS value and no
generated README says it (:768-769, :1029). (D3) records, orchestrator. (D4 / D7 / D8) a bone missing from the returned rig is
matched by its body — pivot, bind rotation, cube signatures (`_bone_fingerprint_matches`, :3067-3080) — against the bones the
shipped rig has not (the added ones, and every copy of a duplicated name); one match each way identifies the rename: `locked
bone X renamed to Y (Y is not this rig's name) — it carries or parents a hitbox part; renaming, re-parenting or deleting a
locked bone is refused`; no match: `locked bone X deleted (missing from the returned rig; no other bone carries its pivot and
cubes) — ... refused`; several: the old hedge `renamed or deleted (...; the bones [...] all carry its pivot and cubes)`
(:3167-3191); an unlocked missing bone gets the same line without "locked" and ending "— every bone name is fixed (README
rule 1)" — the children whose parent field followed an identified rename are folded INTO the rename line ("its N child
bone(s) follow it (a, b, ...) — one rename, reported once", :3172-3177) and get no re-parent line of their own (the root
rename: 27 REJECT lines and "9 renamed, re-parented or deleted" → one line and "1 (root renamed to Root)"; the lane's `LHead`
rename: the four child lines gone); a genuine re-parent — any other new parent, the top level included — stays the REJECT it
was (:3199-3204), and the summary counts stay per locked bone. (D5) duplicate names in a returned geo → `REJECT <geo>:
duplicate bone name X (N times) — a name listed twice is refused by name; neither copy is compared` (:3163-3164; the set line
adds `duplicated [...]`), and the per-bone comparison skips the name (`by` excludes it, :3133), so a bone never reads as
parented to itself; a locked bone renamed onto an EXISTING name is identified among the duplicated copies by its body and
reported `locked bone X renamed to Y (a name the rig already has: the returned rig has N bones named Y) — ... refused`
(:3173-3174; `a_rename_collides_locked`: "Tail4 renamed to Tail3 (...has 2 bones named Tail3; its 1 child bone(s) follow it
(Tail5)...)" replaces "Tail3 re-parented (Tail2 -> Tail3)"). (D6) the order verdict compares the ORDER of the names both rigs
share (first occurrences), not the multiset (:3157-3160): a pure swap prints `order changed`, a rename / deletion / addition
keeping the order prints `order kept`; no pin had fixed the old word (only "bone set/order changed" was pinned), the new pins
say both (:1223, :1330-1336). (D9) a manifest WITHOUT `lock_policy` gets the stale-manifest WARN too, "absent" in place of the
value — `lock_policy is absent, not `warn-keyed, refuse-structural` — the package predates the 2026-09-06 ruling; ...`
(:3110-3113; pin :1362-1370); a present-but-different value keeps the existing text — the value itself is still never echoed
(a 0.2.0 manifest's is the PROVISIONAL sentence). (D10) a `lock_mode` that is neither `warn` nor `reject`, from the manifest
or the API override → `REJECT lock_mode 'X' is not warn or reject (<source>; `warn` is the ruled policy, `reject` the mode
kept for the day the server-side hitbox evaluator lands — regenerate the package or pass --lock-mode warn|reject)` and FAIL
(:3105-3109); the CLI's `--lock-mode` already had `choices=("manifest", "warn", "reject")` (`manifest` = the default = read
the manifest; kept) — a pin asserts it refuses `strict` (:1372-1386). (D11) SPEC §11 item 4 strips a trailing period from each
rule entry before the "; " join (:2205), so `LOCK_POLICY`'s period no longer prints ".;" — the regenerated README and every
SPEC: 0 hits; pins :803-804 and :1030-1031. (D12) `absent` for a missing pivot / bind rotation in a finding (`_shown`,
:3062-3064; :3205-3207 — `bind rotation changed absent -> [5.0, 0, 0]`); a key on a bone a returned geo ADDED reads `keys added
bone 'X' (an artist-added bone; not in the shipped rig)`, on a bone a returned geo RENAMED `keys renamed bone 'Y' (the shipped
rig's 'X', renamed in the returned geo — the rename is refused; key 'X')`, and `(renamed?)` only when no geo explains it
(:3283-3290; pins :1344-1360). (D13) records, orchestrator. Found and fixed while applying: the first cut of
`follows_rename` read a re-parent to the TOP LEVEL (no parent) as "following" a rename that never happened — the refuter's
`c2_reparent_to_toplevel` PASSed for one run; fixed (`old in renames and renames[old] == new parent`, :3147-3150) and pinned
(a locked bone whose parent field is removed → `re-parented (arm -> None)`, :1233-1239). Pins 25 → 26 (new: `test_check_names_
duplicates_renames_order_and_manifest_modes`, :1300-1386), all OK; `py_compile` clean; every folder re-checked from the final
tool (`fix18\checks\`, the table in `records_lock.md` §6; no transcript contains "PROVISIONAL", "never key" or ".;"); the
dry run regenerated under scratch from the final tool (exit 0; the repo's `artist_handoff/` still absent). The tool version
stays 0.2.1 (not asked).

GATE: (g2land2, 2026-09-06 15:02-15:05, shared with the slice (c) landing as (d) and (f) shared bench2 - the tool is python under tools/, no shipped class, asset or config change; the standing gate run as the proof of the tree): drift verified, build successful, "All 1255 required tests passed"; the tool's own suite `python tools/test_artist_package.py` -> "Ran 26 tests ... OK", and the refuter's mutation folders re-checked by the orchestrator after the fix lane (the root rename reported once with its 26 followers, the duplicate refused by name, the Queen as shipped PASS with the ruled summary).

## CHAINSAW SWEEP ROWS AT A FIXED SITE (2026-09-06) — the nine ITEM-070 rows ran at the random grid origin and refused ~44 % of origins (|z| ≥ 2^23) by their own precondition; they now build their geometry at a fixed absolute site inside the walk's float envelope, built and torn down within the row

DIAGNOSIS. The g2land gate (`g2land.suite.log`, batch `chainsawSweepSight:0`, 13:43:31) failed all nine
`ChainsawSweepSightTests` rows — `i070_01_9b_tall_mob_standing_in_short_grass_not_swept` through
`i070_09_master_off_forces_walk_with_key_on` — on their own precondition: "the layout's origin is inside the walk's
float envelope (|x| < 2^24, |z| < 2^23) the rows' cells were derived for (ITEM-070 test geometry); player at
(1.2136938E7, -59.0, -1.4593025E7)" (rows 01-08 at x 12136918 … 12137289, z −14593049; row 09 at z −14592995).
The harness places its structure grid at a random corner, x and z uniform in ±14,999,992 (NF GameTestServer
`startTests`, bytecode: `ldc -14999992 / 14999992`, corner y −59 → structure-block layer −60); this run's z
−14,593,049 has |z| ≥ 2^23 = 8,388,608, so the precondition — added at the bench2 gate when the rows moved onto the
integer lattice ("float ulp 1 beyond 2^23 → integer lattice, all rows hand-replayed both signs") to fail loudly
where the 1.7.10-faithful walk's float sampling (`Chainsaw.myCanSee`, orig UltimateSword.java:198-247, float for
float) pins every half-block sample by ties-to-even (at this run's origin the walk did not advance at all: all ten samples read rel z 25) — rejects it, and rejects (15M − 2^23)/15M ≈ 44 % of
all origins. The code under test is not at fault and is unchanged; the rows' assumption (the origin is in the
envelope) was.

FIX (src/gametest/java/danger/orespawn/gametest/ChainsawSweepSightTests.java only; javac rc 0). Every row builds
its geometry at a FIXED ABSOLUTE SITE inside the envelope; the structure at the random origin is only the row's
runner (`helper.succeed()` / `assertTrue` through it; nothing of the geometry reads `helper.absolutePos`):
- Sites: rows 01-07 and 09 at x = 100,000 + k·96 (k = 0..6, 7), z = 100,000 (the positive quadrant); row 08, the
  cast-shift row, at x = z = −100,000 (the negative quadrant its derivation names: the (int) cast reads toward the
  origin on the half-block z). One site per row, six chunks apart, because a batch's rows tick concurrently and
  identical FORCED tickets collapse into one in the DistanceManager (`Ticket.equals` on type/level/key/forceTicks). The rel
  offsets put the whole layout in one chunk on either sign (x +4, z +8 … +13). The envelope precondition stays as
  a guard on the site constants (`assertFloatEnvelope(helper, siteX, siteZ)`: the layout's farthest |x| < 2^24,
  |z| < 2^23).
- Floor: rel y 0 = `level.getHeight(MOTION_BLOCKING_NO_LEAVES, …)` at the player's column — the first air block
  over the flat game-test world's grass (GameTestServer: WorldPresets.FLAT, WorldOptions(0L, false, false);
  FlatLevelGeneratorSettings.getDefault: bedrock, dirt×2, grass → surface −61, first air −60), i.e. the very y of
  the harness grid's structure-block layer (F0.7: `empty_large` is all air over the framework's stone, so the rows'
  rel y 1 is a block above the floor there and here alike). Every absolute y at a site is therefore the same
  float it was in the structure frame; pinned: the floor level under both columns, the whole layout below y 0.
- Per row, on the server thread: (1) `Site.open` — envelope guard; a FORCED region ticket per site chunk
  (`addRegionTicket(FORCED, chunk, 2, chunk)`: level 33 − 2 = 31, entity-ticking at that chunk, NF
  DistanceManager.addRegionTicket / ChunkLevel); a synchronous `ServerLevel.getChunk` of the span and the 5×5 ring
  around each chunk (what NF ChunkMap.prepareEntityTickingChunk needs at FULL, its range-2 future), so the
  entity-ticking promotion waits on nothing but the main thread's own queue; the floor derived; the ground
  checked untouched (the flat world's GRASS_BLOCK under both columns — a harness structure's cleared box would
  show STONE there, NF StructureUtils.clearBlock — and every cell of the layout box rel (18,0,22)-(22,8,31) air);
  the box outside this row's structure (`helper.getBounds()`). (2) The runner schedules one poll per tick (from the
  synchronous body — the framework iterates its tick-time map live) up to 60 ticks: when every site chunk is
  `ServerLevel.isPositionEntityTicking` (the entity manager's TICKING visibility and the ticket range — the
  harness's own start condition, F0.4), the row's body runs WITHIN THAT TICK as before: it records each overwritten
  cell's prior (`Site.setBlock`, flags 3 as `GameTestHelper.setBlock`), spawns the frozen 1000-HP prey at the
  site with `spawnWithNoFreeWill`'s own sequence (create, persistence, moveTo, addFreshEntity, removeFreeWill,
  then noAi) and pins that `getEntitiesOfClass` — the sweep's own query — returns it, places the survival player,
  replays the walk, runs the sweep and the assertions exactly as before. (3) A finally (each row's, and the
  poll's finally (`runWhenReady`) as the safety net; idempotent) removes the player, discards the spawns, restores every overwritten cell
  to its prior, releases the tickets; then the row asserts nothing survives (every restored cell holds its prior,
  no LivingEntity in the layout box, player and tickets gone) and succeeds. Row ids, batch, template, holder and
  prefix annotations, assertion messages (item id, expected/actual) and the integer-lattice cells are unchanged.
- Why the row spans ticks (the design's synchronous (1)-(4) deviates here): a chunk loaded within a tick keeps its
  entity sections HIDDEN until the queued full-status promotion (`ChunkHolder.scheduleFullChunkPromotion` →
  `thenRunAsync` on the chunk source's main-thread executor, which `getChunk`'s `managedBlock` exits before
  running) pumps between ticks — TF-023 (FIX_LOG, StructureTestsA:481-506, the Basilisk maze): a same-tick spawn
  plus `getEntitiesOfClass` misses mobs that ARE in section storage, and the sweep's target lookup is exactly that
  query. Hence the FORCED region ticket (released in the finally) and the wait for F0.4's condition.

PER-ROW TABLE (site x/z; y −60 derived; cells rel to the site, unchanged from the integer-lattice derivation; the
replay at the site reproduces them by construction — every site coordinate is an integer below 2^23, so each
half-block sample is exact in float, and the y frame is the structure's own −60):

| row | site (x, z) | geometry / cells | expected | tears down |
|---|---|---|---|---|
| 01 i070_01_9b | (100000, 100000) | Zombie (20,1,29), player (20,1,24); short grass on sample 10 → rel (20,2,29) | walk refuses / not swept; ray admits / swept | grass cell → air; Zombie discarded; player removed; ticket released |
| 02 i070_02_4a | (100096, 100000) | Zombie; cobweb on sample 6 → rel (20,3,27) | same pattern | cobweb → air; Zombie; player; ticket |
| 03 i070_03_4c | (100192, 100000) | Pig; short grass on sample 8 → rel (20,2,28) | same pattern | grass → air; Pig; player; ticket |
| 04 i070_04_8c | (100288, 100000) | Pig; bottom stone slab on sample 8 → rel (20,2,28) (eye line 1.94 and lower there, under the slab's cell) | same pattern | slab → air; Pig; player; ticket |
| 05 i070_05_6 | (100384, 100000) | Cow; water on sample 8 → rel (20,2,28) | same pattern (the ray's Fluid.NONE clip ignores water) | water → air; Cow; player; ticket |
| 06 i070_06_10 | (100480, 100000) | Zombie; cobweb on sample 1 → rel (20,3,24) (z + 0.5 cast to the player's own column at a positive z) | walk dies on its first read / not swept; ray admits / swept | cobweb → air; Zombie; player; ticket |
| 07 i070_07_12 | (100576, 100000) | Pig; player elevated (20,4,24); oak log at rel (20,2,27), none of the ten samples | walk sees / swept (key off); ray refuses / spared (key on) | log → air; Pig; player; ticket |
| 08 i070_08_13 | (−100000, −100000) | Zombie; sample 7's point rel (20, 2.7025, 27.5) → cast cell rel (20,3,28), true cell rel (20,2,27); shift (0,1,1) pinned (negative y and negative half-block z) | stone on the cast cell: walk stops / not swept; stone on the true cell: walk skips it / swept, ray refuses / not swept under the key | both stone cells → air; Zombie; player; ticket |
| 09 i070_09_master | (100672, 100000) | Zombie; short grass on sample 10 → rel (20,2,29) (row 9b's geometry) | master off + key on → walk / not swept; master on → ray / swept | grass → air; Zombie; player; ticket; master and key restored |

Absolute examples: row 01 player (100020, −59, 100024), Zombie (100020, −59, 100029), grass (100020, −58, 100029);
row 08 player (−99980, −59, −99976), Zombie (−99980, −59, −99971), seventh sample z −99972.5 → (int) −99972 (rel 28),
y −57.8975 → (int) −57 (rel 3); the point's own cell (−99980, −58, −99973) = rel (20, 2, 27).

Additions beyond the design, all preconditions or teardown checks (no expected outcome changed): the floor level
under both columns; the layout below y 0; untouched ground (grass under both columns, the box air); outside this
row's structure; the spawned prey queryable; row 08's "sits in the negative quadrant"; row 09's "ten samples"
(it indexes sample 10); "nothing of the site survives" after the teardown; the site wait's own failure message
("reached entity-ticking within 60 ticks … F0.4; the queued promotion of TF-023").

HARNESS NOTE for the F0 list (`phase_g_reports/harness_slice_2026-09-04.md` §0; MODERNIZATION_NOTES.md carries
no F0.x entry — the F0.6 / F0.7 form is the report's):
- **F0.8 Off-structure sites (2026-09-06).** A row whose expectations depend on absolute float precision runs its
  geometry at a fixed absolute site inside the envelope, force-loads the site's chunks, and tears everything down
  in its own finally; the structure at the random origin is only the row's runner. The harness grid's corner is
  uniform in ±14,999,992 (GameTestServer `startTests`), so any row that reads its own absolute position into a
  float walk is outside |z| < 2^23 in ~44 % of runs. The site pattern (`ChainsawSweepSightTests.Site`): a FORCED
  region ticket per site chunk at distance 2 (level 31), a synchronous `getChunk` of the span and its 5×5 ring,
  then a per-tick wait for `isPositionEntityTicking` on every site chunk (the F0.4 condition — a same-tick spawn
  after a synchronous load is invisible to `getEntitiesOfClass`, TF-023) before the single-tick body; one site per
  row of a batch, six chunks apart (identical FORCED tickets collapse in the DistanceManager; the sites' chunk sets are disjoint — a pitch of six chunks against a two-chunk ring, never five or under — which is what keeps a neighbour's teardown from pulling a chunk under a running row); rel y 0 from the
  heightmap (the flat world's first air, −60, the structure-block layer's own y); the ground checked untouched
  (the flat world's grass under the layout — a cleared structure box shows stone); every overwritten cell restored
  to its recorded prior, every spawn discarded, the player removed, the tickets released, and the row asserts so
  before it succeeds.

REFUTER NOTES (2026-09-06, one refuter, no blocker; the claim confirmed by a standalone float replay of the walk at
the slice-(d) origin, a negative in-envelope origin, the red-gate origin and the four site classes, and by javap of 27
NeoForge classes; applied): (D1) the records draft's line ranges were stale for fifteen members - corrected to the
saved file (records, orchestrator). (D2) the Javadoc and the drafts said "the runner's finally" calls close() again -
`run` has no finally; it is the poll's (`Site.runWhenReady`) - reworded (:460, the drafts). (D3) the post-teardown
check's `player == null && !ticketed` terms are fields close() resets itself, proving only that close() ran - left as
they are (the living-entity scan and the cell identity are the check; the player list carries no public per-row
query worth the coupling). (D4) the mechanism sentence said the walk "rounds the half-block samples by the origin's
parity"; at the red-gate origin it did not advance at all (ties-to-even pinned all ten samples to rel z 25) -
sharpened. (D5) the Javadoc's grid span said ~4,300 blocks; at ~1,200 tests the grid is ~8,100 deep in z - corrected
(the per-site collision bound stays below 1e-8). (D6) `ticketed = true` was set after the ticket loop; set before it,
so an exception mid-loop cannot leak a ticket past close() (:514). (D7) the protection against a neighbour's teardown
is the DISJOINT chunk sets (a six-chunk pitch against a two-chunk ring), not only ticket identity - said in F0.8, with
"never five or under". (D8) NeoForge's `Ticket.equals` also compares `forceTicks` - cited. Confirmed as claimed:
the relative geometry and the y frame identical to HEAD at every site (cell for cell, both signs); row 08's (0,1,1)
shift at (-100000,-100000); the level-31 FORCED ticket = ENTITY_TICKING; the synchronous getChunk; the poll's
condition is the query's own visibility gate (TF-023); the prey spawned only after it; the polls scheduled up front
because GameTestInfo.tickInternal iterates its map live; the 100-tick budget starts after the harness's own chunk
gate so the 60 polls always fit; the nine rows' chunk sets pairwise disjoint; the teardown complete and idempotent,
the player removed as HEAD did; the body never runs on a closed site; no helper geometry call remains; the envelope
guard is the site's first statement; the flat seed-0 world is deterministic and pristine at the sites; javac rc 0.

GATE: (g2land2, 2026-09-06 15:02-15:05, after the refuter's items): drift verified, build successful, "All 1255 required tests passed" - the batch chainsawSweepSight:0 (9 tests) ran at the fixed sites and passed at a grid origin the old rows would have accepted or refused alike; the red g2land run (13:42-13:45, all nine rows on the origin precondition) is the diagnosis above.

REFUTER (2026-09-06, one refuter, harness rows only): no blocker; the geometry at the sites equal to HEAD's cell for cell by a standalone float replay, the ticket/poll design sound on the bytecode, the teardown complete; its items applied (REFUTER NOTES above).

## ENT-S-146 — the classic PurplePower model transcribed from 1.7.10 (2026-09-06, inside Slice 4c): the per-frame rolls of the level RNG, the accumulating rotations bug-for-bug, the translucent fullbright render state on both renderers, the visual leg's translucent mode, the within-cube face order

RULING. Owner 2026-09-06, item 20: a parity bug, both halves fixed in classic first, inside
Slice 4c — the per-frame rolls from the level RNG through a `PurplePowerPose` seeded in the
entity_state harness kind, the accumulating rotations bug-for-bug and disclosed, the
translucent fullbright render state through `entityTranslucent` on BOTH renderers; the visual
leg extended for translucency as a harness-semantics change (before/after presented before
its gate); the PurplePower candidate re-proven against the fixed classic; two refuters. The
port's opaque steady spin (7.3 / 5.1 / 3.7 degrees per tick) had no key and no MOD record and
is gone. Nothing outside PurplePower changes for a player; the candidate stays behind
`-Dorespawn.dev.geckolibRenderers=purple_power`.

THE MODEL (`entity/client/ModelPurplePower.java`, rewritten; orig ModelPurplePower.java:44-86,
statement for statement, line-cited in the source). `setupAnim` writes no angle (orig :50) and
delegates to `poseFrom(PurplePowerPose, six floats)`, which captures the LEVEL's random — orig
:57 `p.worldObj.rand` — through the new `entity/pose/PurplePowerPose.getLevelRandom()`
(`PurplePower implements` it as `level().getRandom()`; the ENT-S-093 convention would have
allowed the entity's, the ORIGINAL used the world's, the world's is transcribed). The name is
`getLevelRandom`, not the brief's `getRandom`: `Entity.getRandom()` is public in 1.21.1 and
returns the entity's OWN source (orig PurplePower.java:155-163 `this.rand`, the flight targets),
so a `getRandom()` on the interface would have been satisfied by the inherited method and handed
the model the wrong source without a compile error. `renderToBuffer` (:154-174): push (:51);
`rf1 = roll(random)` (:57, `PurplePowerPose.roll` = `nextFloat() * 360.0f`, the ONE static both
renderers read); `mulPose(Axis.XP.rotationDegrees(rf1))` (:58); `renderFan(innerSpoke)` = six
draws with `spoke.zRot = newangle; newangle += 1.0471976f` accumulated in float32 (:59-63 — the
step ON the part); `mulPose(XP, rf1)` AGAIN (:64 — not negated: the X rotation doubles and
carries into the next fans); a fresh roll, `mulPose(YP)`, the middle fan, `mulPose(YP)` again
(:66-73); a fresh roll, `mulPose(ZP)`, the outer fan, `mulPose(ZP)` again (:75-82); pop (:85).
Net per frame, as the register said: Shape1 under X(r1), Shape2 under X(r1)·X(r1)·Y(r2), Shape3
under X(r1)·X(r1)·Y(r2)·Y(r2)·Z(r3); nothing persists past the pop. `rollRadians` is
`Axis.rotationDegrees`' own conversion (`ldc 0.017453292f; fmul`) so the hook feeds the identical
float. A null random (never in the game: `LivingEntityRenderer.render` calls `setupAnim` at 510
before `renderToBuffer` at 621; only the harness's static / reference-geometry captures) rolls 0.
The three boxes and texOffs (:24-41) are unchanged; the standing reference leg re-measured
`reference_purplepower` PASS with the pinned MIRROR 3 (101 checked-in reports verified, no drift).

THE RENDER STATE. orig :52 `GL_NORMALIZE`: no counterpart (unit normals). :53-54 blend: the
model's render type — `EntityModel(Function)` with `RenderType::entityTranslucent` (the
`SlimeModel` idiom); `LivingEntityRenderer.getRenderType` (21.1.223 bytecode: 7-16 the
invisible-to-viewer `itemEntityTranslucentCull`, 17-30 the visible body → `model.renderType
(texture)`, 31-44 the outline) returns it to `render`, which draws through
`bufferSource.getBuffer(renderType)` (579). `RenderType.lambda$static$7` builds
`entityTranslucent` with `TRANSLUCENT_TRANSPARENCY` (22-25), `NO_CULL` (28-31), `LIGHTMAP`,
`OVERLAY` and the builder defaults `LEQUAL_DEPTH_TEST` / `COLOR_DEPTH_WRITE`
(`CompositeStateBuilder.<init>` 26-29 / 75-78; `COLOR_DEPTH_WRITE = new WriteMaskStateShard
(true, true)`, `RenderStateShard.<clinit>` 1179-1188; `LEQUAL_DEPTH_TEST = ("<=", 515)`,
1147-1160); `RenderStateShard.lambda$static$10` (bootstrap #10) = `enableBlend;
blendFuncSeparate(SRC_ALPHA, ONE_MINUS_SRC_ALPHA, ONE, ONE_MINUS_SRC_ALPHA)`. :55 colour:
`ModelPurplePower.COLOR` = `0x8CBFBFBF`, what `FastColor.ARGB32.colorFromFloat(0.55f, 0.75f, 0.75f,
0.75f)` returns (`as8BitChannel` is `Mth.floor(f * 255)`; a compile-time constant, pinned equal by the
game-test row and checked in the probe JVM), so the vertex carries (191, 191, 191, 140); every part is
drawn with it (`ModelPart.Cube.compile` hands its colour argument to every `addVertex`, 176; the
shader multiplies the texel by it and the blend uses its alpha), ABSOLUTELY as `glColor4f` was:
the colour the renderer passes (white, or the 0.15-alpha `654311423` for a mob invisible to the
viewer, `render` 610-621) is disregarded exactly as 1.7.10's `RendererLivingEntity` 0.15 was
overridden by the model's :55. :56 lightmap 240 / 240: the fullbright texel — the light is the
RENDERER's in 1.21.1 (`EntityRenderer.getPackedLightCoords` 0-24 = `LightTexture.pack
(getBlockLightLevel, getSkyLightLevel)`; `pack` = `block << 4 | sky << 20`), so
`PurplePowerRenderer.getBlockLightLevel` / `getSkyLightLevel` answer `ModelPurplePower
.LIGHT_LEVEL` = 15 (the `MagmaCubeRenderer` idiom, bytecode 0-2): `pack(15, 15)` = 15728880 =
`LightTexture.FULL_BRIGHT`, whose texel is (240, 240) — orig's exact coordinates. :83-84 colour
reset / blend off: a per-call argument and a render-type-scoped state, nothing to reset. Shadow /
scale untouched (`referenceRenderers`: PASS 120, DIVERGES 0, the PurplePower entry pinned as before).

THE CANDIDATE (`PurplePowerGeoReplacement.java`, rewritten; `entity_state`). Three new hooks on
`GeoReplacementDescriptor` — `renderType(E)` (the render-type FUNCTION, a `Function<ResourceLocation,
RenderType>`, the shape the classic model holds it in (`Model.renderType`); null = GeckoLib's own),
`renderColor(E, partialTick)` (`WHITE` = -1 = GeckoLib's own, `Color.<clinit>` `iconst_m1`),
`fullBright(E)` — plus `cubeFaceOrderRequired()`; `OreSpawnGeoReplacedEntityRenderer` applies them
at GeckoLib's own decision points: `getRenderType` (4.8.4 `GeoReplacedEntityRenderer.getRenderType`:
an invisible entity the viewer sees → `itemEntityTranslucentCull` 24-46; a visible one → `GeoRenderer
.getRenderType` 52-61 → `GeoModel.getRenderType` = `entityCutoutNoCull`; invisible glowing → outline
62-89 — the override fills exactly the visible-body branch, the one the classic model's function
fills, and leaves the others to GeckoLib), `getRenderColor` (`GeoRenderer.defaultRender` takes
`getRenderColor(...).argbInt()` once, 4-18, and passes the int to `preRender`, `actuallyRender`
(176; the replaced renderer's override hands it to `GeoRenderer.actuallyRender` at 773 →
`renderRecursively` → `renderCubesOfBone` → `renderCube` → `createVerticesOfQuad`, whose
`addVertex` takes it at 81)), and the two light-level getters (the dispatcher's
`getPackedLightCoords` → `GeoReplacedEntityRenderer.render` → `defaultRender` at 18), which answer
`GeoReplacementDescriptor.FULL_BRIGHT_LEVEL` = 15 for a `fullBright` species — the seam's own constant;
the shared base names no species model (refuter A, D4). PurplePower's descriptor returns the classic
model's own constants (`RENDER_TYPE` itself — the SAME function object the classic renderer applies,
so the harness proves the two by identity — `COLOR`, true),
so the two renderers cannot drift apart. The hook rolls the same `PurplePowerPose.roll` three times
in X, Y, Z order from `inputs.subject(PurplePowerPose.class).getLevelRandom()` and turns nine GROUP
bones: `Shape1__fan` X(r1); `Shape2__carry_x1`, `Shape2__carry_x2` (X(r1) each: orig :58, :64) over
`Shape2__fan` Y(r2); `Shape3__carry_x1/x2`, `Shape3__carry_y1/y2` (orig :58, :64, :67, :73) over
`Shape3__fan` Z(r3) — the clones (the 60-degree step, `step_scope: part`) never touched. Nested,
because a pose stack rotated about X then Y is parent X over child Y, and one GeckoLib bone rotates
Z, then Y, then X (`RenderUtil.rotateMatrixAroundBone`: `mulPose(ZP)` 10-22, `mulPose(YP)` 35-47,
`mulPose(XP)` 60-72) and cannot express X-then-Y; and ONE GROUP PER `glRotatef` rather than one per
doubled angle because a single X(2 r1) rounds differently from X(r1)·X(r1): the doubled form left
Shape2's normals 4.4e-7 and Shape3's 1.5e-6 off the classic's, over the surface leg's 1e-6 (measured;
the tolerance stands, the rig became more literal; with the literal chains the normals are exact).
The converter gained `group_chain` (outermost first, the last the fan; default `["<part>__fan"]`,
recorded in the mapping only when declared — the Rotator's `conversion.json` is byte-identical).

THE FACE ORDER (new `entity/client/FaceOrder.java`; the G2 open item settled for a translucent
rig). NeoForge 21.1.223 `ModelPart.Cube.<init>` fills its polygons DOWN, UP, WEST, NORTH, EAST,
SOUTH (offsets 365-785: DOWN 365, UP 436, WEST 507, NORTH 578, EAST 649, SOUTH 720, each slot ending
in its `Polygon.<init>` and `aastore`; `Polygon.<init>` negates a mirrored cube's X normals) and
`compile` emits them so; GeckoLib 4.8.4 `BakedModelFactory.buildQuads` builds WEST, EAST, NORTH,
SOUTH, UP, DOWN (20-130, one `buildQuad` per direction) and `renderCube` emits the array. The order
the candidate is matched to is the 1.21.1 CLASSIC'S, which is not 1.7.10's: the original `ModelBox`
(the 1.7.10 client jar, class `bis`, located by its ten-argument constructor shape, VERIFIED by
javap) stored its six quads at 365 / 439 / 500 / 561 / 628 / 695 as the x2, x1, y1, y2, z1, z2 faces
(+X, -X, -Y, +Y, -Z, +Z; a mirrored box swaps x1 / x2 first at 136-153, as `Cube.<init>` does at
121-135, and reverses each quad at 774-806) and drew them in that order (`render` 0-28). Under
blending that decides, at a spoke's tips, which of its own end / side faces show through the others
- the port follows vanilla's own `ModelPart.Cube` and cannot follow 1.7.10 here; the PN draft
`pp146\pn_cube_order.md` presents it (refuter A, D2). Invisible for a cutout rig
(the depth test decides; only a zero-thickness box ties), decisive under blending with the depth
written: a back face emitted before its front face shows through the front face's alpha, one
emitted after it is depth-rejected — the candidate would have blended a different orb. The contract
travels like the draw order: the converter writes, for a rig whose manifest declares
`cube_face_order: "classic"`, `orespawn:cube_face_order` in the geo description (bone → one array
per cube of GeckoLib direction names; derived from the bytecode rule per cube's mirror flag since the
captures are posed, self-checked against the three cubes unrotated at bind whose captured normals
must be the rule's in order, and proven by the parity tool's new per-face check: the per-quad
normal sequences of both renderers equal on every capture, 972 faces over 9 captures);
`FaceOrder.read` / `apply` (strict: every check before the in-place permutation of each
`GeoCube.quads()` array) / `applyOrFallback` (`OreSpawnGeoReplacementModel.getBakedModel`, beside
the draw order, once per bake: a present-and-wrong key ERROR-logged once per resource and the bake
left as found; an absent key silent unless the descriptor requires it — a translucent rig —
then WARN once; the pack author's fix named). The harness applies a present key through the strict
static (`S4CandidateRuntime.freshBaked`, `G1AnimationRuntime.evaluator`) and records `cube_face_order`
/ `baked_cube_face_order` in the geo dump. Scoped to rigs that declare it: no cutout rig's geo, proof
or capture changes (every one byte-identical); the asset audit's new `GECKO_GEO_FACE_ORDER_INVALID`
(never acknowledgeable) fails a seam rig whose key is present and malformed. Whether the contract
should extend to every rig is the owner's call — the Vortex's flat cubes would be the first to move.

THE HARNESS MODE (`tools/g1_render_parity.py`; the harness-semantics change, presented in
`before_after.md` BEFORE the gate). A per-model `visual_mode` in the manifest: `render_type`
(`entity_cutout_no_cull`, the verbatim `render_capture` path every landed model runs, or
`entity_translucent`), `vertex_color` (the RGBA bytes), `light` (documentary: no lightmap is
applied on either side). `render_capture_blended` emulates the GPU for one model's quads in emission
order: the texture-alpha discard (< 0.1, the same line in both entity shaders, before `color *=
vertexColor`), the texel times the vertex colour, LEQUAL with the depth WRITTEN, SRC_ALPHA /
ONE_MINUS_SRC_ALPHA over the background, 8-bit quantisation after every fragment. "Same plane" is
held to a window that is its OWN named tolerance — the manifest's `thresholds
.coplanar_depth_epsilon_blocks`, 1e-5 blocks, an owner ruling presented with its number in
`before_after.md` (the sweep 0 / 1e-7 / 1e-6 / 3e-6 / 1e-5 / 3e-5 verbatim, the two residual pixels,
the options), no default in the tool (refuter B, D2). What the data shows: on the classic side the
six spokes' coplanar depths agree to <= 1e-9 at most contested pixels (genuine ties); the cross-side
same-fragment noise is ~1e-7 genuine (vertex deltas <= 1.8e-7) plus up to ~1e-6 from the harness's
own `Camera.project` rounding every vertex to 6 decimals — a pre-existing quirk of BOTH modes — so a
window below that flips passes between the two sides (94 / 158 / 35 pixels on three captures at
1e-6; 0 / 2 / 0 / 0 at 1e-5, the two an edge-on face's depth gradient, present at every window and
flagged by the contested diagnostic); the test is against the LAST written depth (max walk-back
9.9e-6 measured, chained passes 0); faces 1/16 block apart are decided exactly as the GPU decides.
The blend-mode contested diagnostic counts any other-quad fragment passing within that window,
whatever its texel, never cleared. Not emulated on either side, either mode: lightmap, directional
shading, fog, the hurt overlay, the top-left fill rule. The report carries `visual_mode` (the mode,
the vertex colour, the emulated states, the coplanar epsilon), a blend-mode `z_fight_policy` and the
observed `render_state` only for a model that declares a mode; the README gains "Render mode" /
"Render state observed" lines for it. THE MODE IS OBSERVED, NOT ASSERTED (refuter B, D3): the probe
records beside every dump, both modes, `<id>.render-state.json` — the render type as the FUNCTION
object each side holds (`RenderType` itself cannot be initialised in the probe JVM: `RenderType
.<clinit>` reaches `Items.<clinit>` through `ItemRenderer.<clinit>`, "Not bootstrapped", measured
2026-09-06, OPT-029 R0), named by the one `RenderType` factory its owner class's constant pool
references (`ModelPurplePower` → `entityTranslucent` → `entity_translucent`; the `EntityModel`
default → `entityCutoutNoCull`; GeckoLib's `GeoModel.getRenderType` → `entityCutoutNoCull`), the
colour and the packed light every captured vertex carried after each side was handed what its
renderer would hand it (the classic renderer's light-level overrides evaluated registry-free through
a null-filled `EntityRendererProvider.Context` when the manifest names `classic_renderer`; the
descriptor's `renderColor` / `fullBright` on the candidate) — and the parity tool requires both
sides equal to each other and to the visual mode for EVERY model (`G1 RENDER STATE PASS` × 13 + 2:
PurplePower `entity_translucent` / (191, 191, 191, 140) / 15728880 over 3888 + 3888 vertices, the same
function object on both sides; every cutout rig `entity_cutout_no_cull` / white / the probe's light,
a loud failure otherwise). The existing dumps are byte-identical (the compiled dump's sha256 is the
conversion's provenance): every cutout model's compiled dump, conversion and report entry unchanged. Manifest: `entity_state`, four seeds (1, 12345, 2026,
777) × ages 0 / 10 (the pose ignores age), `render_instances` `step_scope: part` /
`float32_accumulate` with the chains, `cube_face_order: "classic"`, `visual_mode` (191, 191, 191, 140 /
full_bright), the four seeded `t0` / `t_half` captures as the visual samples (bind — the unrolled flat
rings — is not a rendered frame). `subject_after` unchanged; the rolls are proven through the pose
(the negative run — the hook consuming the Y roll before the X roll — fails the geometry leg at
Shape1__i0 with a 0.032-block corner delta).

EVIDENCE (scratch `pp146\`, every process under `timeout`; exit codes). javac main 800 sources rc 0
(16 pre-existing warnings), g1tool 10 rc 0 (`RenderStateProbe.java` new), gametest 68 rc 0 (2
pre-existing warnings), `--release 21 -g`, scratch class dirs first (the fix lane re-ran everything
below from `pp146\fix\`; the implementation lane's numbers held). `G1ModelProbe vanilla` s4 / g1 / reference: 0 / 0 / 0 (14 / 3 / 101
dumps); `layer_definition_to_geo.py` s4 / g1: 0 / 0 (14 + 3 CONVERT GREEN); `reference_geometry_leg.py`
s4 (coin): 0; the standing reference leg against `phase_g_reports/reference_proof` (verify mode): 0,
`REFERENCE GEOMETRY PROOF: 101 checked-in reports verified`, `reference_purplepower` PASS pinned
MIRROR 3; `G1ModelProbe geo` s4 / g1: 0 / 0; `g1_render_parity.py --write-proof` into scratch proof
dirs s4 / g1: 0 / 0 — `G1 PARITY PASS: 13 models` / `2 models`. PurplePower: geometry 1.87e-7 blocks
(162 cube-samples), surface 3888 vertex-samples UV 0 / normal 0, animation 4.1e-8 rad, positions 0,
hidden checks 8, states 4, draw order 9 captures / 162 draws, face order 972 faces, composition 162
draws: instance pose 0 / 0, draw pose 0 / 0; visual changed 3.05e-5 (two pixels of `s_seed_12345_t0`,
an edge-on face's unstable depth interpolation: 1.6e-5 blocks of depth from 2e-7 of position; the
Vortex's accepted residual is the same class), MAE 4.6e-4, contested 0.127-0.161 (a diagnostic).
Byte identity (`compare_proofs.py`): s4 240 files identical — all 186 PNGs of the twelve cutout
models and the fixture, every other model's generated files and report entry; changed only
`evidence/report.json`, `evidence/README.md`, `generated/model_purplepower.{geo,conversion,
animation-contract}.json`; 15 PurplePower PNGs removed, 12 added; top level only
`visual_artifact_count` 201 → 198. g1 44 of 44 identical. `referenceRenderers`: PASS 120, PENDING 0,
MOD 0, NOT_APPLICABLE 13, DIVERGES 0. Asset audit, read-only on the tree as it stands: `RESULT: 1
error(s), 0 advisory(ies), 4 acknowledged` — the one is `GECKO_GEO_PROOF_DRIFT` on purplepower (the
shipped rig against the not-yet-regenerated proof; clears on regeneration); on a scratch copy with the
scratch proof substituted: `RESULT: 0 error(s), 1 advisory(ies), 4 acknowledged; draw order: 15 shipped
geo: 14 seam + 1 outside-seam -> exit 0` (the advisory is INDEX_CASE_UNCHECKED, the copy is outside
git); with a duplicated direction planted in the shipped key (and its proof copy): `RESULT: 1
error(s)` — `GECKO_GEO_FACE_ORDER_INVALID: purplepower.geo ... Shape1__i0 cube 0 is not a permutation`.
A headless `FaceOrder` smoke on a GeckoLib bake (the game-test row replicated, un-bootstrapped JVM):
fresh order [west, east, north, south, up, down], the classic mirrored order applied and idempotent,
absent silent / required WARN once, five wrong shapes WRONG_KEY_FALLBACK with the quads untouched and
ERROR once each, the strict `apply` throws — `SMOKE OK` (re-run against the fix's classes: the same).
Shipped `purplepower.geo.json` regenerated (LF, 27 bones, both keys, byte-identical to the scratch
proof copy and to the fix lane's regeneration); `purplepower.animation.json` byte-identical to before.
The refuters' items, evidenced (fix lane): `g1_render_parity.py --validate-only` s4 / g1: 0 / 0 —
`G1 PARITY STAGING PASS: 13 models; no proof written` / `2 models`, a `G1 RENDER STATE PASS` line per
model; a scratch `--write-proof` s4 / g1: 0 / 0 (`G1 PARITY PASS: 13 models; checked-in proof updated`
into the SCRATCH proof dir), byte identity against the checked-in proofs unchanged from the lane's
(s4 240 identical incl. all 186 cutout PNGs, 5 changed / 12 added / 15 removed; g1 44 / 44) and against
the lane's own run s4 254 / 257 (every PNG, the four PurplePower captures included) / g1 44 / 44. The
asset audit on the tree: rc 1, `GECKO_GEO_PROOF_DRIFT` only; on a scratch copy with the regenerated
proof: rc 0 for the shipped rig, rc 1 with the reason named for each planted key — two arrays for the
one-cube `Shape1__i0` ("lists 2 cube(s) for Shape1__i0 but the rig bone has 1"), `Shape1__i0` removed
("lacks the cube-bearing rig bone(s) Shape1__i0"), the cube-less `Shape1__fan` named ("names
Shape1__fan, which is not a cube-bearing bone of the rig"), the key removed entirely ("is absent, but
the descriptor requires it (cubeFaceOrderRequired: a translucent rig)"), the duplicated direction
("Shape1__i0 cube 0 is not a permutation"). Negatives: the geo probe over a generated dir whose
PurplePower geo lacks the key: rc 1, "harness failure: the generated geo ships without
orespawn:cube_face_order, which the shipped descriptor requires (cubeFaceOrderRequired: a translucent
rig); the harness proves shipped rigs and takes no fallback" (the bind bake, the G1 evaluator path);
the parity tool over a staged copy with the key stripped from geo / dump / conversion: rc 1, `FACE
ORDER REQUIRED model_purplepower`; a manifest without `coplanar_depth_epsilon_blocks`: rc 1, "carry no
coplanar_depth_epsilon_blocks ... has no default". `FastColor.ARGB32.colorFromFloat(0.55, 0.75, 0.75,
0.75)` == `ModelPurplePower.COLOR` == 0x8cbfbfbf == -1933590593, bytes 140 / 191 / 191 / 191 (the probe
JVM). The gametest run is the gate's (expected `All 1259 required tests passed`: 1255 + the four rows).

PINS. New `PurplePowerPoseTests` (own batch `purplePowerPose`, TEST-003; a `@GameTestGenerator` over
four synchronous rows `purplepowerposetests.ent_s_146_NN_<row>`, `orespawn:empty_large` named in
full, 100 ticks; the spawned orb discarded in a finally; row 03's synthetic resources carry a per-run
stamp): 01 `pose_random_is_the_level_random` — a frozen PurplePower's `getLevelRandom()` IS the
ServerLevel's `RandomSource` instance and is NOT `Entity.getRandom()` (the two are distinct); 02
`roll_is_one_float_times_360` — for seeds 1, 12345, 2026, 777 three rolls equal three `nextFloat() *
360.0f` of an equally seeded source, lie in [0, 360), and both sources' next int agrees (exactly one
float per roll); 03 `face_order_permutes_a_bake_into_the_classic_order` — the smoke's assertions on the
dedicated server through the production statics; 04 `descriptor_hooks_are_the_classic_constants`
(refuter B, D3) — on a live orb the descriptor's `renderColor` == `ModelPurplePower.COLOR`
(0x8CBFBFBF, a compile-time constant now, pinned == `FastColor.ARGB32.colorFromFloat(0.55, 0.75, 0.75,
0.75)` with the bytes (191, 191, 191, 140)), `fullBright` true, `cubeFaceOrderRequired` true,
`ModelPurplePower.LIGHT_LEVEL` == `GeoReplacementDescriptor.FULL_BRIGHT_LEVEL` == 15; `renderType` is
NOT called there (a client `Function<ResourceLocation, RenderType>` held by the client-only model
class — the 4b finding; the harness proves it: the same function object on both sides). Client-side
and the harness's: the rolls' effect (the geometry / composition legs), the render state (the visual
mode; the observed sidecars), the face order (the per-face check). Compile proof: javac of the whole
gametest tree rc 0; the class references no `net/minecraft/client` type (javap; the
`ModelPurplePower` Class constant javac records beside the inlined `COLOR` / `LIGHT_LEVEL` is
referenced by no instruction and is never resolved — `renderColor` on the descriptor is an `ldc`,
`fullBright` an `iconst_1`); `FaceOrder.class` and `PurplePowerPose.class` reference no client
or mod class (javap, the DrawOrder discipline).

FILES THAT CHANGE ON THE ORCHESTRATOR'S REGENERATION. `phase_g_reports/s4_proof`:
`generated/model_purplepower.geo.json`, `.conversion.json`, `.animation-contract.json`,
`evidence/report.json`, `evidence/README.md`, `evidence/visual/model_purplepower/*` (the 15 old PNGs go,
12 new: `s_seed_1_t0`, `s_seed_12345_t0`, `s_seed_2026_t_half`, `s_seed_777_t0` × vanilla / geo / diff) —
nothing else in s4_proof; `phase_g_reports/g1_proof`: nothing under `generated/` or `evidence/`; the
benchmark proof (`g1_proof/benchmark/report.json` and whatever the gate rewrites) re-pins —
`G1AnimationRuntime.java` is one of its source pins and the g1tool class directory changed
(`G1ModelProbe`, `ProbeSubject`, `S4CandidateRuntime`); `phase_g_reports/reference_proof`: nothing
(verified). Shipped: `src/main/resources/assets/orespawn/geo/entity/purplepower.geo.json` (already
regenerated, in the working tree).

DISCLOSED. (1) `getLevelRandom` for the brief's `getRandom` (above). (2) The invisible-to-viewer case:
vanilla draws such a mob through `itemEntityTranslucentCull` at 0.15 alpha; the model overrides the
colour to 0.55 as 1.7.10's model overrode `RendererLivingEntity`'s 0.15 — bug-for-bug, an edge no
player meets without an invisibility potion on the orb. (3) The carries: one group per `glRotatef`
after the doubled form measured 1.5e-6 (above); the rig has 27 bones, 18 cubes. (4) The blended
rasteriser's depth-tie window is its OWN named tolerance, `coplanar_depth_epsilon_blocks` = 1e-5 (an
owner ruling, presented with its number and the sweep in `before_after.md`; the option of 1e-6 with
the projector's rounding removed is presented, not taken — a harness-semantics change for every
species); the two-pixel residual is an edge-on face's depth gradient. (5) The face-order contract's scope (above).
(6) ENT-S-147's per-frame dedup applies to the rolls as it applied to the Rotator's advance: behind the
pause screen with one orb in view the candidate holds its last frame where the classic rolls again.
(7) Not emulated on either side: lightmap, directional shading, fog, the overlay, the fill rule.
(8) `GeoReplacementDescriptor.class` names `RenderType` only in the `Signature` attribute of
`renderType(E)` (the erased descriptor is `(Entity)Function`, javap; never resolved); the descriptor
loads on the game-test server (`GeoCacheEvictionTests`) and row 04 invokes its `renderColor` /
`fullBright` there — the gate run settles it, as it did for the `$1` descriptors. (9) The within-cube
face order both renderers emit is the 1.21.1 classic's, not 1.7.10's (`ModelBox` drew +X, -X, -Y, +Y,
-Z, +Z; VERIFIED against the 1.7.10 jar, `bis.<init>` 365-772, `render` 0-28; the PN draft
`pn_cube_order.md`): the port cannot follow it with vanilla's own `ModelPart.Cube`. (10) The hurt /
death flash: 1.7.10 drew a SECOND untextured `mainModel.render` under `GL_EQUAL` with
`glColor4f(brightness, 0, 0, 0.4)` set BEFORE the model's own `glColor4f(0.75, 0.75, 0.75, 0.55)` overrode
it (VERIFIED, `RendererLivingEntity.doRender` = `boh.a(sv, DDDFF)` 870-946), so the orb never flashed
red but re-blended its front layer flat grey; 1.21.1 tints the one draw ~30% red through the overlay
on BOTH renderers (`getOverlayCoords` 430-444, `OverlayTexture.v` = 3, the shader's `mix`); the PN
draft `pn_hurt_flash.md` presents three readings; the record lists the overlay as "not emulated".
(11) `ModelPurplePower.COLOR` is the literal `0x8CBFBFBF` now (was the `colorFromFloat` call), so the
descriptor and the game-test row read it inlined without loading the client model class; pinned equal
to `colorFromFloat` by row 04 and checked in the probe JVM. (12) The probe writes a new per-model
sidecar beside every dump, both modes (`<id>.render-state.json`); no existing dump field changed.
(13) The manifest gains `classic_renderer` (PurplePower only) for the light observation, and the named
`coplanar_depth_epsilon_blocks` threshold. (14) The seam parses the geo resource once per bake and
hands the document to both keys (`DrawOrder.geoJson`, the `JsonObject` overloads, `unreadable`;
refuter A, D5); both `load`s kept for their callers. (15) The descriptor's render-type hook returns
the function, not the `RenderType` (`renderType(E)`), the only form the un-bootstrapped harness can
observe; the shipped renderer applies it to the texture on the same condition as before.

REFUTERS' FOCUS. (a) The RNG consumption order on both sides — three `PurplePowerPose.roll` per frame,
X, Y, Z, one `nextFloat` each (`ModelPurplePower.renderToBuffer` :157-169 vs
`PurplePowerGeoReplacement.applyCustomAnimations` :124-135); the negative run's failure mode. (b) The
composition: the classic's `X(r1)·X(r1)·Y(r2)·Y(r2)·Z(r3)` pose stack against the chains' cumulative
transforms (the composition leg 0 / 0; the basis conjugation of `rotateX/Y/Z`). (c) The render-type /
light / colour path on both renderers against the cited bytecode; the descriptor hooks' default
branches (GeckoLib's invisible / outline branches untouched). (d) The rasteriser's blend / depth
emulation against the RenderType states (the plane tolerance, the write, the quantisation, the
discard); the mode illustration numbers. (e) The cutout captures' byte identity (`compare_proofs.py`)
and that no report field was added for an undeclared model. (f) The regenerated rig's draw-order key (a
permutation of the 27 bones in pre-order) and face-order key (18 bones × one cube × six names); the
converter's mirror rule against `ModelPart.Cube.<init>`; the self-check's three cubes. (g) The OPT-029
R0 constraint: both constructors registry-free (the probe instantiated the replacement in the
un-bootstrapped JVM: rc 0); the `RenderType::entityTranslucent` method reference does not initialise
`RenderType`. (h) The seam's fallback policy for the face order (`applyOrFallback`: absent / required /
wrong, once per resource, the bake untouched) and the audit rule's content check.

REFUTER A NOTES (2026-09-06, applied by the fix lane). D1 (MUST-FIX, harness strictness): found —
`S4CandidateRuntime.freshBaked` and the `G1AnimationRuntime` evaluator applied a present key and took
an absent one silently even for a rig whose descriptor requires it; done — the replacement is
instantiated first and `descriptor().cubeFaceOrderRequired()` threads into `freshBaked` (a throw
"harness failure ... which the descriptor of <class> requires ... takes no fallback", beside the
draw-order one), the evaluator gains the four-argument form the probe uses for every bind bake
(`S4CandidateRuntime.cubeFaceOrderRequired(candidateClass)`), the two-argument form is documented
as the benchmark's opaque-only path (no translucent rig reaches it), and the parity tool FAILS
(`FACE ORDER REQUIRED`) rather than skips when a model declares a blending mode and the geo carries
no key — all three negatives run (rc 1 each, above). D2 (disclosure): the premise VERIFIED against
the 1.7.10 jar (`bis` located by shape; offsets quoted in `FaceOrder`, `ModelPurplePower`, the
converter's evidence string and `pn_cube_order.md`); the status line and this log say the cube order
is the 1.21.1 classic's. D3 (disclosure): the premise VERIFIED against the 1.7.10 jar (`boh.doRender`,
offsets quoted; `pn_hurt_flash.md`); the record keeps the overlay as "not emulated". D4: found — the
shared base read `ModelPurplePower.LIGHT_LEVEL`; done — `GeoReplacementDescriptor.FULL_BRIGHT_LEVEL`
(15) on the descriptor, the base reads it, row 04 pins it equal to the classic renderer's constant.
D5: found — two parses of the geo resource per bake; done — one parse (`DrawOrder.geoJson`) handed to
both keys through new `JsonObject` overloads, the unreadable case answered by both seams' logged
fallback (`unreadable`), both `load`s kept. D6 / D7 / D8: sentences in the records (the CRLF facts
corrected; `roll(null) → 0` as a harness-only path a future caller could silently take; the
`group_chain` check shape-only).

REFUTER B NOTES (2026-09-06, applied by the fix lane). D1 (audit): found — the audit checked a
present key against the bone NAME set only (a cube-less bone named, a cube-bearing bone missing, a
wrong cube count passed); done — `_geo_bones` returns `{name: cube_count}`, `_face_order_problem`
mirrors `FaceOrder.read` + `apply` exactly (the key's bones == the rig's cube-bearing bones, one array
per cube, six-name permutations) and, for a descriptor whose source overrides
`cubeFaceOrderRequired()` to true (detected in the java file `seam_rigs` already attributes the rig
to; a file with more than one descriptor is SKIPPED), an absent key is the same ERROR — the three
planted shapes, the key removed entirely and the lane's negative each rc 1 with the reason named,
the shipped rig rc 0 (above); D9 — `/tools/asset_audit.py text eol=lf` in `.gitattributes`. D2
(the window, presented not tuned): (a) done — `thresholds.coplanar_depth_epsilon_blocks` (1e-5) read
by `render_capture_blended`'s caller, reported as before, a blended model without it a loud failure
(no default; negative run rc 1); (b) done — the causal paragraph rewritten to the data here, in
`before_after.md` (the sweep table verbatim, the residual, the two options, the unrounded-projector
row), `status_line.md` and `records.md`. D3 (the mode observed): done — the render-state sidecars on
both sides, the classic renderer's light evaluated registry-free, the descriptor hooks' results as
the renderer applies them, the colour / light observed at every vertex, the render type by the
function object's owner (RenderType cannot be initialised there: measured); the parity tool
requires both sides equal and equal to the mode for every model; the dumps byte-identical (a sidecar
is the new file, not a new field; the report field only for a declared mode); the gametest row 04
pins `renderColor` / `fullBright` / `cubeFaceOrderRequired` on the server and does not touch
`renderType`. D4 / D5 / D6: the reversed vertex cycle (a `VISUAL_MODES` comment + a records
sentence), the last-written-depth test (walk-back 9.9e-6, chained 0) and the channel tolerance under
a 0.55 layer (~1.8×, pre-existing, not changed) — sentences in `before_after.md` and the records.
D7: the evidence string's offsets → DOWN 365, UP 436, WEST 507, NORTH 578, EAST 649, SOUTH 720-785
(`FaceOrder`, the converter's rule and comment, this log; verified in `NF_ModelPart_Cube.txt`). D8:
the records' parity line is the `--validate-only` `G1 PARITY STAGING PASS: 13 models; no proof
written` (the scratch `--write-proof` line kept beside it, named as scratch).

GATE: (pp146, 2026-09-06 17:51-17:56, after both refuters and the fix lane; the before/after presented in phase_g_reports/ent_s_146_before_after_2026-09-06.md BEFORE this gate, as ruled): the proofs regenerated by hand under the proof rule - s4 "G1 PARITY PASS: 13 models; checked-in proof updated" (PurplePower re-proven seeded and translucent under the entity_translucent mode; the twelve cutout models, the fixture and every reference-leg file byte-identical), g1 "G1 PARITY PASS: 2 models; checked-in proof verified" (no drift), the benchmark "G1 BENCHMARK EVIDENCE VERIFIED ...; checked-in proof updated" (re-pinned: G1AnimationRuntime.java and the g1tool class directory); the regenerated purplepower.geo.json staged with the proofs. Gate: drift verified ("checked-in proof verified"), build successful, "All 1259 required tests passed" (1255 + PurplePowerPoseTests' four rows; the batch purplePowerPose:0 ran; the chainsawSweepSight:0 batch passed at a second random origin).

REFUTERS (2026-09-06): A (the Java: transcription, RNG, render state, FaceOrder, OPT-029 R0, the rows) and B (the harness: the blend emulation vs the RenderType states, the depth-tie window, the face-order derivation, byte identity, the records) - no blocker; their items applied by the fix lane (REFUTER A NOTES / REFUTER B NOTES above): the harness strict on a required face-order key, the audit rule matching FaceOrder.apply, the render state OBSERVED on both sides, the depth-tie window a named tolerance presented with the sweep table, two verified 1.7.10 divergences filed as ENT-S-152 / ENT-S-153.

## PHASE G — THE KEYFRAME LEG'S RETURN (2026-09-06): the controller, the Q9 repair in the seam, the Beaver clip regenerated, the density as an output — PRESENTED, the shipped Beaver clip unchanged

RULING. Owner 2026-09-06, scope addendum item 24 (5)-(15): the controller's return as the first Tier-2 slice's
precondition — the keyframe animation leg back in the harness with the wrap sample and the density as an OUTPUT
(Amendment 1 points 3-5, ADDENDA (1)-(2)); Q9 (a) scoped (the catmullrom spline arguments repaired at load in the
replacement seam only, the Queen's native model on stock semantics until her own ruling, a deliberate divergence
recorded with the javap cite and reported upstream); Q10's wording; Q14 (a) every loop at 1.0 s; Q17 (a) the Beaver
clip regenerated under the converter's rule; Q1 (a) the `[modern] artistAnimations` master, default ON, species
self-gated by clip presence, `classicAnimationSpecies` the exclusion list; Q2 (a) / Q13 (a) the additive layer.
PRESENTED before wiring: the classic code-driven path stays the shipped path for the Beaver; the keyframe path is the
harness reference leg's candidate.

WHAT LANDED (code; nothing player-visible):
- `entity/client/animation/PhaseLockedKeyframeController` — the salvaged controller (0d238ba) returned with the three
  changes the design named: the time-warp from the clip's DECLARED `animation_length` (`adjustTick` maps the classic
  LUT index onto `Animation.length()` read at prime; ratio = declared / natural period), an additive layer mode (the
  scaled delta added to what earlier layers wrote THIS frame — `bone.hasRotationChanged()`, set by `tickAnimation`'s
  drain at 407 and cleared at 1250 — never to last frame's value; the (e) demo's scratch variant read the bone
  unconditionally and would have accumulated on a persistent manager), and instrumentation (`declaredClipTicks`,
  `lastClipTick`, `lastCosineIndex`, `clipTicksPerSourceTick`, the public `classicCosineIndex` chain the harness's
  wrap sampler uses). `KeyframeLayer` (a frequency group as data: group, clip, omega, bones, gaitScaled; builds the
  controller on any animatable and state readers — the shipped entity readers in-game, explicit inputs in the
  harness). `SplineRepair` (the Q9 repair: P0 = the key before the segment's start, P3 = the key after its end,
  periodic across a loop's seam, clamped on play-once; only the easing arguments change; idempotent; the anchor and
  linear frames the loaded objects).
- `OreSpawnGeoReplacement`: `keyframeLayers()` (empty for every species; the Beaver declares three),
  `registerControllers` → `registerKeyframeLayers(registrar, loadedClips())` — self-gated by clip presence FIRST
  (`idle` or `walk` in the loaded file, else the classic source; a layer whose own clip is missing is skipped, its
  bones holding bind), the config SECOND (`OreSpawnConfig.artistAnimations(type)`), decided once as GeckoLib builds
  the per-entity manager (the construction snapshot; OPT-029 evicts it on leave). `OreSpawnGeoReplacementModel`:
  `getAnimation` serves every clip from a repaired copy of the loaded file, once per loaded `BakedAnimations` by
  identity (a resource reload repairs the new bake), a missing file left to GeckoLib's own resolution; and
  `setCustomAnimations` gates the classic hook on `manager.getAnimationControllers().isEmpty()` (one motion source per
  species; the hook runs after the controllers, `handleAnimations` 268-284 then 287-292, so a hook that posed would
  overwrite the layers). `BeaverGeoReplacement`: the three layers (`walk` 3.7 rad/tick on rff/lrf/lff/rrf scaled;
  `walk_teeth` 2.7; `walk_tail` 0.5), the classic hook unchanged and still the shipped path; its descriptor's
  entity-type supplier made a lazy lambda (the bound `ModEntities.BEAVER::get` initialised `ModEntities` eagerly and
  tripped `Bootstrap.checkBootstrapCalled` the moment the headless leg instantiated the class — the S4 species'
  convention, OPT-029 R0).
- `OreSpawnConfig`: MOD-038 — `[modern] artistAnimations` (default true) and `classicAnimationSpecies` (list, empty;
  registry names, the bare name accepted for the mod's own species), read through `artistAnimations(EntityType)` =
  master && key && !listed; the master's comment and javadoc list them with the ten others.
- The harness: `G1ModelProbe` — a `gait_scaled` model may declare `keyframe_reference_leg` (candidate_class,
  clip_path, clip_manifest, spline_repair, wrap_epsilon_lut_indices, density_search); the schedule then gains the
  wrap pairs on BOTH sides (T−ε vs 0+ε at every seam of every frequency group the float phase straddles, one pair
  per amplitude; `KeyframeLeg.wrapRequests`); the geo side samples the shipped layers of the shipped replacement
  (`S4CandidateRuntime.instantiate` → `keyframeLayers()` → `KeyframeLayer.controller` on explicit inputs) over the
  clip through GeckoLib's own loader (`FileLoader.loadAnimationsFile` 51-69 mirrored) and processor on a persistent
  bake and manager, repaired by the production `SplineRepair` when declared, and writes per sample
  `keyframe_rotations` (classic terms) plus the leg's block: the clip's density / lerp mode / argument state as
  BAKED, the density SEARCH (the generator's rule regenerated in memory for N = 3.. under the evaluator in force, the
  fewest holding the tolerance, confirmed on the full dense schedule at every amplitude with one fewer failing), the
  file pinned to the generator's rule (max key delta 0.0), the wrap sample, the time-warp facts, the reversed
  schedule (0.0), the density statement in Q10's wording. `tools/g1_render_parity.py` — `keyframe_reference_leg_parity`:
  every layer bone of every non-bind sample against the compiled `setupAnim` at the manifest's NAMED tolerance
  (`thresholds.keyframe_reference_leg_epsilon_radians`, no default), every other bone at bind, the wrap pairs'
  continuity per group, the probe's density / confirmation / dense / wrap / order blocks required to hold, the
  statement rebuilt and compared, the report and README lines. A model without the block is untouched: the shipped
  manifests (g1: 2 models; s4: 13 + fixture) through the modified harness VERIFY the checked-in proofs byte for byte
  (`G1 PARITY PASS … checked-in proof verified`, both trees; the only `diff -rq` residue against today's gate outputs
  is the pre-existing per-JVM lambda address in the `.render-state.json` sidecars and a hash-ordered object in
  PurplePower's dump, shown to vary between two runs of the gate's own classes — `before_after.md` §1).
- The generator: `tools/keyframe_clip.py` over `tools/keyframe_clips/beaver.json` (the g3 salvage brought forward
  as a manifest-driven tool: one loop per frequency group from the g1 manifest's classic channels, 1.0 s, full
  amplitude, the converter's sign rule — authored X = +classic degrees; the salvaged generator's sign was inverted
  against the landed basis, Q17 (a); the density recorded as the harness's output). Its output is a SCRATCH clip
  for the harness run and a gametest resource (`src/gametest/resources/orespawn_gametest/keyframes/`, LF rule added);
  the shipped `beaver.animation.json` is unchanged (`"animations": {}`).
- Pins: `KeyframeLegTests` (own batch `keyframeLeg`, TEST-003; six rows): the shipped empty clip registers no layer
  (both paths); the reference clip registers the three, and each config gate registers none; on the server, through
  the production code on both sides, the layers pose within 2.5e-3 rad of the classic hook over a late-start schedule
  and the model's gate stands the hook down; the Q9 defect against the pinned jar and the repair's arithmetic
  (periodic / clamped / idempotent / the evaluator over the repaired arguments equals the textbook spline);
  additive layers compose without accumulating over six frames of one manager (non-additive: last wins); the
  declared-length time-warp, the late prime, the LUT chain's straddle and saturation.

THE DENSITY (an output; the scratch run on the regenerated clip, `before_after.md` §2): "2.5e-3 rad; Beaver reference
leg 15 / 13 / 8 catmullrom keys per bone with spline arguments repaired at load; wrap sample T−ε vs 0+ε included".
Sample grid against the compiled `ModelBeaver.setupAnim` (17,568 layer-bone samples incl. 264 wrap pairs): gait
2.162e-3, teeth 1.892e-3, tail 2.295e-3; the dense confirmation (1,057,088 / 264,272 / 264,272 comparisons at four
amplitudes) 2.163e-3 / 1.892e-3 / 2.295e-3, one fewer 2.711e-3 / 2.544e-3 / 3.984e-3 (fails); wraps 24 / 17 / 4
seams straddled (LUT 65531 → 4), continuity ≤ 3e-8; reversed schedule 0.0. With the repair OFF (the library's kinked
spline, the same clip): the leg FAILS — 4.850e-2 / 3.087e-2 / 1.467e-2; gait and teeth not within 2.5e-3 by 97 keys
(6.85e-3 / 3.81e-3 at 97), the tail at 31 — the (e) demonstration's numbers reproduced through the shipped classes.
The divergence recorded (PN-024 draft) and the upstream report drafted.

DEVIATIONS PRESENTED: (1) the additive layer's base reads the bone only when `hasRotationChanged()` (the design's
scratch variant read it unconditionally; on a persistent manager that accumulates across frames); (2) the Beaver
descriptor's lazy supplier (above); (3) the generator is manifest-driven (`tools/keyframe_clip.py` +
`tools/keyframe_clips/<species>.json`) rather than a Beaver-only script; (4) the leg's density search runs in the
probe against the manifest's classic channels on the (e) schedule (late start 137.371, 40.5 ticks, 4097 / 65537
intervals), the dumped grid (5 × 4 + 595 × 4 + wraps) being what the parity tool compares with the compiled model;
(5) the shipped g1 manifest is NOT edited — the leg is declared on a scratch copy for the proof run, so the
harness's shipped outputs stay byte-identical until the owner's look moves the clip and the leg into the tree;
(6) the gametest reference clip is a resource of the gametest source set, never the jar.

NOT DONE (by design): the shipped `beaver.animation.json` unchanged; the shipped manifests unchanged; no
`_preview` export (slice (f)); no locked-bone drop in `getAnimation` (the Queen pilot's); no trigger transports,
weights or idle_alt (later slices); nothing wired in-game — the switch that would enable the Beaver's keyframe path
is the shipped clip file gaining the three contract clips with `[modern] artistAnimations` at its default; it is not
flipped.

REFUTER A NOTES (fix lane, 2026-09-06): (A1) D1 MUST-FIX - the thirteen empty `registerControllers` overrides (and the
harness fixture's, `G1RuntimeBasisFixtureReplacement` :31-33) deleted with their imports, the base's method made `final`
(`OreSpawnGeoReplacement` :126-137: `registerKeyframeLayers(registrar, loadedClips())` the single self-gating path), javac
main / g1tool rc 0 proving no override remains, and `KeyframeLegTests.kf_007` (:507-541) pinning the presented state on the
server: the fourteen replacements instantiated registry-free, every `keyframeLayers()` empty but the Beaver's three, the base
method final and un-overridden, the production path registering nothing. (A2) D2 - event keyframes on a phase-locked loop
fire ONCE per manager lifetime (`executedKeyFrames` cleared only by `resetEventKeyFrames`, behind the `adjustedTick >=
length` gate that `adjustTick`'s `index * L / 65536 < L` never reaches): the controller unchanged, one sentence in its class
javadoc (:61-67), an OPEN contract item in `records.md`. (A3) D3 - `KeyframeLayer` javadoc :14-19: only the LATER layer's
additive flag decides, and the additive base is read only for the layer's amplitude-scaled bones, so a gait clip keying a
non-gait bone writes it non-additively. (A4) D4 - the Elevator (:28) and Vortex (:14) suppliers are lazy lambdas; javac rc 0;
the s4 probe rc 0 twice. (A5) D5 - PN-024, the upstream report and the before/after state the parametrisation wherever
"plays as Blockbench previews it" appears: C1 in each segment's NORMALISED time (`EasingType.apply`: `currentTick /
transitionLength`), C1 in tick time only under uniform keys (the generator's rule), not under non-uniform keys (the
refuter's hand-check: keys at 0 / 0.2 / 0.5 / 1.0, slopes 0.1418 vs 0.0945 rad/tick at key 1). (A6) D6 - the stale-bake
retention is bounded (one `repairedSource` / `repaired` pair, replaced not accumulated; the model instance replaced on
F3+T): the `getAnimation` javadoc (:129-130) and `records.md`. (A7) D7 - `records.md` section 2.7's "moves to reflection"
plan sentence corrected to the fact: `kf_003` has no fallback in code, a `RuntimeDistCleaner` refusal fails the row loudly
(the right outcome), and the refuter predicts it loads (same-name assignability checks in `GeoModel.getRenderType` /
`handleAnimations`; `DataTickets.<clinit>` ldcs only `OnlyIn`-free classes). (A8) D8 - the `getAnimation` javadoc
(:125-128): a present file that lacks the clip answers `null` without the fallback-resource walk, GeckoLib's own order
today (no `getAnimationResourceFallbacks` override anywhere), so a future fallback declaration must make the override walk
it; `kf_008` pins the `null`.

REFUTER B NOTES (fix lane, 2026-09-06): (B1) D1 MUST-FIX - `mod_record.md` (MOD-038) rewritten into MOD-037's exact form
(Origin / Ruling as its own bullet / Classic / Switch / Effect when effective / Not covered / Pin / Harness consequence /
Status). (B2) D2 - `KeyframeLegTests.kf_008` (:543-600) seeds `GeckoLibCache.ANIMATIONS` (a private static non-final `Map`;
the jar carries no `module-info`, an automatic module, every package open) by reflection with a bake of the reference clip
under the Beaver's animation resource, constructs the shipped model registry-free and pins a repaired copy never the loaded
object, the same instance twice (identity), a fresh repaired copy once the map's value is a new `BakedAnimations` and once
the map itself is swapped, `null` for a clip the present file lacks, the map restored in a finally; the read-verified chain
(`AnimationProcessor.buildAnimationQueue` -> `model.getAnimation`; `AnimationController.setAnimation` via `lastModel`) is
recorded beside it in case the gate refuses the access. (B3) D3 - `tools/g1_render_parity.py` :885-897: the density table
must run consecutively from `min_keys`, every row below a group's fewest must exceed the tolerance and the fewest row must
hold it (N = 3 beats N = 4..7 on every group); validate-only PASS unchanged, a mutated dump (row 5's gait error 1e-3; the
"3" row deleted) rejected. (B4) D4 - the two authoring implementations (Python `round(v, 10)` half-even vs Java
`Math.round(v * 1e10) / 1e10` half-up) are pinned only at the file's density; a divergence at another N is bounded by
1.7e-12 rad, acceptable - said in `records.md` section 5.4. (B5) D5 - `records.md` section 5.3's wrap split corrected to the
dumps' 36 + 26 + 4 seams (144 + 104 + 16 pairs over four amplitudes = 264); the last seam of each group coincides with the
loop period and is excluded by `wrapRequests`' `cycle * period + epsilon <= loopPeriod` bound, not by the straddle test.
(B6) D6 - PN-024's header reads "PROPOSED with the landing (Q9 (a) ruled the divergence, 2026-09-06; the PN record lands on
the owner's acceptance)"; the upstream report's "all loaders are affected" softened to what the NeoForge 4.8.4 jar shows
(no sources jar beside it in the Gradle cache; nothing downloaded). (B7) D7 - both latent proof-drift sources fixed: the
sidecars' `function_class` drops the hidden-class address (`RenderStateProbe.stableClassName` :198-208), and the dump's
`cube_face_order` / `baked_cube_face_order` are emitted in bone-name order (`G1ModelProbe.faceOrderJson` :1403-1412; the
source was `FaceOrder.read`'s `Map.copyOf`, whose iteration order the JDK salts per JVM); neither file is in the proof map -
verify mode PASS on both trees after the change, each chain run twice - and two consecutive runs of every chain now `diff
-rq` clean over the whole tree (0 lines: scratch g1, shipped g1, shipped s4), where the gate's own classes could not; the
KNOWN_ISSUES note is unnecessary. (B8) D8 - the code already does the parenthetical (a layer registers only when ITS clip
is present; the species flips only when at least one declared layer's clip is present): an `idle`-only delivery registers
nothing and the classic hook poses, which `kf_001` already pins, so the flip premise is falsified and the gate unchanged;
the real gap (a delivered `idle` passes `check` but nothing plays it; `idle` + `walk_teeth` flips the species with the gait
and tail at bind per section 2.4) is an OPEN contract item for the weights slice. (B9) D9 - `.gitattributes` :25-27 gains
`/tools/keyframe_clip.py` and `/tools/keyframe_clips/*.json` as LF. (B10) D10 - the token is no longer recomputed: the probe
writes each wrap sample's group and token beside its id (`KeyframeLeg.WRAP_FIELD` :86, `Prepared.wrapProvenance` :574-591 -
the same `frequencyToken` over the same channel float that built the id; `G1ModelProbe` :1356-1359) and the tool matches on
that record (:840-853, :861-879; `keyframe_frequency_token` deleted); validate-only PASS unchanged (the
`keyframe_reference_leg` block JSON-equal to the lane's dump; 528 wrap samples carry the record), a copy without the record
or with a wrong token rejected.

THE RED kf17 GATE AND THE AUDIT'S COMMENT HANDLING (orchestrator, 2026-09-06 19:49-19:53). The first gate of this
slice (with item 17) re-pinned the benchmark proof and then failed in the build at `assetAudit`: `GECKO_GEO_SEAM_UNRECONCILED`
for `elevator.geo` and `vortex.geo` - "drawn by no GeoReplacementDescriptor" - and "15 shipped geo: 12 seam + 1
outside-seam". Diagnosis: `tools/asset_audit.py`'s `seam_rigs` reads each descriptor statement up to its first `;`, and the
fix lane's lazy-supplier lines (refuter A, D4) carry a trailing comment "(OPT-029 R0; item 15 refuter A D4)" whose `;`
comes before the geo literal - the two rigs were SKIPPED and the reconciliation flagged them (slice (c)'s refuter B had
noted the scans strip no comments). Not a rig or seam defect: a tool fragility. Fix (the orchestrator, tools only):
`_strip_java_comments` (a string-aware scanner blanking `//` and `/* */` outside string and char literals, newlines
kept) applied in `seam_rigs`, `_descriptor_requires_face_order` and the per-rig descriptor count; self-tested; the
audit on the tree: `RESULT: 0 error(s), 0 advisory(ies), 4 acknowledged; draw order: 15 shipped geo: 14 seam + 1
outside-seam`. Left as it was: `_entity_dims`' `;` cut over `ModEntities.java` (a comment with a `;` inside a
registration statement would shorten it; none exists). A first attempt at the patch through a shell heredoc halved the
escapes and left a syntax error (the kf17b attempt failed the build in one second); repaired by building the literals
from character codes - the standing lesson: write Python with the editor, not a heredoc, when a backslash matters.

THE RED kf17b GATE (orchestrator, 2026-09-06 19:55). With the audit green the suite died at start-up:
`ResourceLocationException: Non [a-z0-9/._-] character in path of location: orespawn:orespawn:empty_large` from
`GameTestInfo.prepareTestStructure`. Diagnosis: `KeyframeLegTests` row 03 used the generator-style template constant
`EMPTY_LARGE` ("orespawn:empty_large") in a `@GameTest(template = ...)` annotation; `@PrefixGameTestTemplate(false)`
stops the framework prefixing the CLASS name onto the template, but an annotated template is still namespaced by the
mod id (the sibling rows' "empty" resolves to `orespawn:empty`), so the constant doubled the namespace - only a
manually built `TestFunction` (the generator classes) takes the fully qualified string. Fix: the row's template is
"empty_large" (one string; the constant stays unused for the generator idiom). A harness convention worth recording
beside F0.x: annotated rows name templates bare, generated rows name them qualified.

THE RED kf17c GATE (orchestrator, 2026-09-06 19:56-20:01). The suite ran to the end: "3 required tests failed" -
`kf_003_layers_pose_as_the_classic_hook_on_the_server`, `kf_005_additive_layer_composes_without_accumulating`,
`kf_006_declared_length_time_warp_and_late_prime` - each "Attempted to load class
net/minecraft/client/multiplayer/ClientLevel for invalid dist DEDICATED_SERVER" (RuntimeDistCleaner). Diagnosis: the
three are the rows that drive GeckoLib's `AnimationProcessor.tickAnimation` on the dedicated server; its first act is
`GeoModel.applyMolangQueries`, whose implementation reaches `MolangQueries` (javap: 58 `net/minecraft/client` references),
which the server's dist cleaner refuses at class load; the headless probe JVM has no dist cleaner and tolerates it, which
is why refuter A's prediction ("it loads") held for the classes it swept but not for this one, reached only at
execution. The rows that passed (01 registration, 02 the repair arithmetic, 04, 07 the presented state, 08 the seam's
getAnimation) never tick. Fix (test double only): the rows' `ServingModel` overrides `applyMolangQueries` as a no-op -
the reference clips carry constants only (the package tool rejects Molang, rule 7), so the queries feed nothing; the
evaluator itself (the shipped controller, `AnimationProcessor`, `EasingType`) is untouched; the classic model in those
rows is driven through the hook, not `tickAnimation`. Recorded beside F0.x as a harness convention: a server-side row
that ticks a GeckoLib model must skip the Molang query setup.

THE RED kf17d GATE AND THE RESOLUTION (orchestrator, 2026-09-06 20:05-20:35). With the Molang query setup skipped the
same three rows failed the same way, so the trigger was traced empirically: the harness's probe (the same
`AnimationProcessor.tickAnimation` path in a plain JVM, where `ClientLevel` loads freely) run under
`-Xlog:class+load` loads `MolangQueries` immediately after `AnimationProcessor$QueuedAnimation`, `BoneAnimationQueue`,
`AnimationPointQueue` and an `AnimationController` lambda - i.e. inside `AnimationController.process` - and
`MolangQueries`'s static initialiser then pulls `Player`, `Variable` and `ClientLevel`. The bytecode names the site:
`AnimationController.processCurrentAnimation` calls `MathParser.setVariable(String, ...)` at offset 148,
unconditionally for every running animation, and `MathParser` registers through `MolangQueries` (`registerVariable`,
`getVariableFor`, `isExistingVariable`). So GeckoLib 4.8.4's controller processing is CLIENT-ONLY by construction: no
dedicated-server game test can drive `AnimationController.process`, whatever the model double skips. Resolution: the
three rows that tick the controller (`kf_003_layers_pose_as_the_classic_hook_on_the_server`,
`kf_005_additive_layer_composes_without_accumulating`, `kf_006_declared_length_time_warp_and_late_prime`) are no longer
gate rows - their `@GameTest` annotations are replaced by a CLIENT-ONLY note stating the mechanism, the bodies kept
reviewable and runnable under the client run; the class javadoc marks them. Their facts live in the harness leg
(`KeyframeLeg` drives the identical `tickAnimation` path over the shipped controller in the probe JVM: the pose within
2.5e-3 rad of the classic over the full schedule on one persistent manager - so no accumulation across frames - and the
measured time-warps 11.777 / 8.594 / 1.592), which is the presented proof; the late-prime case of kf_006 is the one
fact with no headless twin today - filed below as an open harness item. Not a vacuous pass: a row that cannot run on
the dedicated server is not counted, rather than counted green. The gate's required-test total becomes 1259 + 5 = 1264
(kf_001, 002, 004, 007, 008). The `ServingModel.applyMolangQueries` no-op from the kf17c fix stays (it is correct and
harmless; the rows it served are the client-only ones).

GATE: (kf17 -> kf17e, 2026-09-06 19:49-20:24, with item 17 in the same tree, after both refuters and the fix lane): the benchmark proof re-pinned by hand under the proof rule (G1AnimationRuntime.java, G1ModelProbe.java and the g1tool class directory drifted; the s4 / g1 parity proofs verified unchanged by the lane, both refuters and the fix lane in verify mode); then four red runs, each diagnosed above and none a defect of the presented code: kf17 red in the build at assetAudit (the audit's first-';' cut inside a trailing comment hid two rigs' geo literals - the audit strips comments now), kf17b red at suite start-up (an annotated row named its template with the namespace, which the framework prefixes again), kf17c and kf17d red on the same three rows at class load (GeckoLib's AnimationController.processCurrentAnimation initialises MolangQueries, which needs ClientLevel - controller processing is client-only by construction; the three rows are client-only checks now, their facts the harness leg's). Gate kf17e (20:18-20:24): drift verified ("checked-in proof verified"), build successful, "All 1264 required tests passed" (1259 + KeyframeLegTests' five gate rows kf_001 / 002 / 004 / 007 / 008; the batch keyframeLeg:0 ran, the benchHarness:0 (17) and benchHarnessAsync:0 (1) batches of item 17 ran, the suite log carries "MHLib counters enabled (-Dmhlib.counters=true)" and the per-100-tick server dumps).

REFUTERS (2026-09-06): A (the seam and the controller) and B (the harness, the generator, the records) - no blocker; their items applied by the fix lane (REFUTER A NOTES / REFUTER B NOTES above): the thirteen empty registerControllers overrides gone and the base method final, the production repair site pinned (kf_008), the density search's low end asserted, the two dump non-determinisms removed, the depth of every record corrected; PN-024 (the spline repair) and MOD-038 (the master key) filed; two OPEN contract items and one OPEN harness item in the records.

## PHASE G SLICE (d), ITEM 17 — the counters' test seam removed; the gametest run carries -Dmhlib.counters=true (2026-09-06)

Ruling (owner, 2026-09-06, item 17): "the counters: a system property on the gametest run, no test seam in production code."

What slice (d) had: MHLibCounters carried a package-private field `enabledForTests`, `enableForTests(boolean)`, `enabledForTests()` and `serverEnabled()` (= `ENABLED || enabledForTests`); the seven server-side counter sites read `serverEnabled()` so that BenchHarnessTests rows 11, 12 and 17 could flip the seam on a game-test server that ran without the property.

What changed:
- MHLibCounters.java: the field and the three methods are gone (old :89-96 and :191-213). `serverEnabled()` is REMOVED rather than kept as an alias of the constant: the server sites read the static-final `ENABLED` directly, the same form as the client sites (`if (MHLibCounters.ENABLED) ...`), so there is one guard in the class and nothing left to re-seam. The class javadoc's server paragraph (:64-69) states the single guard and the cost argument again (the JIT folds the static-final on both sides; slice (d)'s extra static boolean read per server site is gone). `ENABLED` (:78) still reads `Boolean.getBoolean(PROPERTY)`, `PROPERTY = "mhlib.counters"` (:77).
- The seven server sites: IMultipartEntity.java :116 (net.set_master_packets), :319 (server.align_sub_parts_parts), :389 (server.align_synched_parts), :413 (server.placement_ns in mhlibAiStep); MHLibPartEntity.java :261 (server.part_setpos); MixinServerEntity.java :135 (net.s2c_update_packets / net.s2c_update_bytes; its javadoc :129-131 no longer cites the seam); ModernSpiderGait.java :1384 (server.placement_ns in feedParts). Each `MHLibCounters.serverEnabled()` became `MHLibCounters.ENABLED`; nothing else on those lines moved, and what each site counts is unchanged.
- build.gradle :107-111: the gameTestServer run -- and only that run -- gains `systemProperty 'mhlib.counters', 'true'` next to its existing neoforge.enabledGameTestNamespaces property (the property name is the one MHLibCounters.ENABLED reads, :77-78). The configureEach / client / server / data entries are untouched: the counters stay off in every other run.
- BenchHarnessTests.java: rows 11, 12 and 17 rely on the property. A shared precondition `assertCountersLive` (:623-632) asserts `MHLibCounters.ENABLED` with a message naming `-Dmhlib.counters=true` and build.gradle's runs.gameTestServer; every try/finally that flipped and restored the seam is gone; row 11's "seam off counts nothing" block (old :654-663) went with the seam (a JVM whose constant is true cannot pin the off state; the fold is a javap matter, recorded in the slice records). Row 17 (asynchronous, a 39-tick wait) now sums the server dumps it sees through a MHLibCounters.DumpListener and adds the partial it reads itself (:874-918), because with the property live MHLibMod.onServerTick zeroes the server list every 100 server ticks -- a dump inside the wait would otherwise vanish from the read. Rows 11 / 12 keep their exact expectations (Queen: 0 / 10 / 10 from mhlibAiStep alone, 30 on the spawn tick, 10 / 20 on the second, OPT-030; spider: 8 / 0 / 8, 32, 8 / 24): each reset-act-read is one synchronous stretch of one tick, in-row, and nothing else ticks a multipart entity inside it. The class javadoc (:76, :85-97) and the section comment (:621) describe the property; the two row tags keep their slice (d) names (the gate's test IDs; a note at :147-148).

Side effects, stated: on the game-test server the counters are live for the whole suite -- one INFO line "MHLib counters (server, per 100 ticks): ..." every 100 server ticks in the suite log, and orespawn.geo.evictions now counts during GeoCacheEvictionTests (no row asserts its value; the two sumAndResetAll() reads there take the gauge and the key order only). Rows 11 / 12 / 17 fail at the precondition on a run without the property (the client run's /test), naming it. The counter names and the dump order are unchanged (GeoCacheEvictionTests.assertDumpOrder and BenchHarnessTests row 15 hold as written).

Verification (this lane, no gradle): javac of every src/main/java source (800 files, --release 21 -g, into a fresh scratch dir) rc 0; javac of every src/gametest/java source (68 files) rc 0; grep of src/ for serverEnabled / enableForTests / enabledForTests: nothing; javap: MHLibCounters.ENABLED is `public static final boolean` (ACC_PUBLIC, ACC_STATIC, ACC_FINAL, no ConstantValue attribute -- the fold is the JIT's, exactly as at the client sites), the four server-site classes carry only `getstatic MHLibCounters.ENABLED` and no serverEnabled reference, and no seam string survives in either compiled tree.

REFUTER A NOTES (2026-09-06, the production code and the run config; no blocker, no must-fix): (A1) the client
run's `/test` is the only other invoker of the three counter rows and fails them at `assertCountersLive` naming the
property - acceptable, the gate is `gameTestServer`; caveat recorded: were the property ever added to the `client`
run, `MHLibClient.onClientTick` would reset the CLIENT list on the render thread while rows 14 / 15 read it on the
integrated-server thread (a race the dedicated server cannot have). (A2) the one production path newly live on the
gate: `SPacketUpdateMultipart.write` runs for every multipart broadcast of every species through
`MixinServerEntity.mhlib$countUpdateBroadcast` -> `encodedLength` (a scratch buffer, released in a finally; the
holders pre-built; `MHLibPartEntity.defineSynchedData` empty) - safe by reading, the gate run the proof. (A3) the
counter methods carry no guard of their own; every site branches on `getstatic MHLibCounters.ENABLED` around the
call (IMultipartEntity x5 incl. the client site, MHLibPartEntity x2, MixinServerEntity x1, ModernSpiderGait x1,
MHLibMod's listener registration) - with the property absent the cost is one folded static-final read. (A4) no
licence / author / header line in the five MHLib files (each opens at `package`); `MHLibCounters`'s class javadoc
keeps one sentence of history about the removed seam - kept. (A5) the two test IDs `..._under_the_seam` stay (a
rename is a required-test-list change - the owner's); the Java method names dropped the suffix (refuter B, D2).
(A6) the property's arrival in the JVM is proven only by the gate (the same `systemProperty` mechanism as the
sibling namespace property); expected in the suite log: `MHLib counters enabled (-Dmhlib.counters=true) ...` at
construction, then the per-100-tick server dumps. (A7) `.gitattributes` and the `animation/` files modified in
the tree during the review are item 15's lane's, not this item's. (A8) row 17's listener is removed on both coded
exits; only a harness-level failure between its steps could leave it, harmlessly.

REFUTER B NOTES (2026-09-06, the rows; one must-fix, applied by the orchestrator): (B1) MUST-FIX - the asynchronous
row 17's 39-tick window was protected from rows 11 / 12 / 15's in-row `sumAndResetServer()` only by an ASSUMED
start order: a row's body starts 20 ticks after its OWN structure's chunks are entity-ticking
(`GameTestInfo.tick` 27-58 -> `isPositionEntityTicking`; `ensureStructureIsPlaced` 9-33, `bipush 20`;
`startExecution` 1-19), in the async chunk pipeline's order - nothing pins it (slice (d) had the same exposure; the
lane's javadoc had stated it as a guarantee). Applied: row 17 runs in its own batch `benchHarnessAsync`
(`BATCH_ASYNC`, the generator picks it for `row.asynchronous()`); a batch runs only after the previous batch has
completed (`GameTestRunner$1.testCompleted` -> `MultipleTestTracker.isDone` -> `afterBatchFunction` ->
`runBatch(i+1)`), so no synchronous row's reset can land in its window; the test IDs are unchanged (they come from
`Row.tag`); the class javadoc reworded. (B2) the stale method identifiers `serverCountersQueenUnderTheSeam` /
`serverCountersModernSpiderUnderTheSeam` renamed (the tags stay); every prose mention of the seam already read
"removed". (B3) the client run's `/test` fails the three rows at their precondition - by design (A1). (B4) row
17's four `>=` sums are global: a multipart entity surviving from an earlier batch could only ADD - never fail the
row; cosmetic. Confirmed as claimed: rows 11 / 12's exact reads are safe by construction whatever the batch
membership or start order (the act is a direct `mhlibAiStep()` / `tick()` call; rows tick one after another -
`GameTestTicker.tick` 7-17, `GameTestInfo.tickInternal` 12-24 then 36-113; every level entity tick, tracker
broadcast and the 100-tick dump (`ServerTickEvent.Post` after `tickChildren`) run outside every body); row 17's
dump-summing loses and double-counts nothing (`LongAdder.sumThenReset` per counter, read then published on the
server thread; the listener added before the spawn and the reset, removed on both exits); the other rows'
expectations assume no zero baseline across ticks; no gametest captures logs; the row count is unchanged (18 rows,
420 `@GameTest` methods - the removed "seam off counts nothing" block was inside row 11); javac rc 0, no client
refs in the test class.

GATE: (kf17e, 2026-09-06 20:18-20:24, shared with item 15's landing; after both refuters and the orchestrator's fixes): drift verified, build successful, "All 1264 required tests passed" - the batches benchHarness:0 (17 tests) and benchHarnessAsync:0 (1 test, the asynchronous row in its own batch) ran with the counters live: the suite log opens with "MHLib counters enabled (-Dmhlib.counters=true): dumping the server counters every 100 server ticks" and carries the per-100-tick server dumps; the four earlier red runs of the shared gate (kf17 to kf17d) were item 15's and the audit's, recorded in item 15's section.

REFUTERS (2026-09-06): A (the production code and the run config) and B (the rows) - no blocker; B's one must-fix (the asynchronous row's unprotected window) applied by the orchestrator: its own batch, a framework guarantee in place of an assumed start order; the rest recorded (REFUTER A NOTES / REFUTER B NOTES above).

## ENT-S-150 SURVEY (2026-09-06) — the legacy-AI speed class under the modern mover, read-only; four findings filed, the ENT-S-145 / 150 record corrected

RULING. Owner 2026-09-06, item 23 (ii): the Ender pair's +6.2 attacking boost (ENT-S-150) is one case of a class - survey the class first, then the value with ENT-S-145's hunks re-applied. A read-only lane wrote `phase_g_reports/legacy_ai_speed_survey_2026-09-06.md` (357 lines): both engines' movement laws law-11 checked against the jars with javap offsets (Appendix A), a 115-row per-species table (section 2), the mapping options (section 3), four register drafts (Appendix B). What it found: the 1.7.10 legacy loop is linear and capped (v = 4.317 * A b/s, the cap 4.405 at A >= 1.0204: `td.bq` 552-566 writes the attribute into moveForward, `sv.bl` 12-14 returns the fixed 0.1f, `sa.a(FFF)` 25-39 clamps the input at length 1); the 1.7.10 task AI and the 1.21.1 mover are the SAME quadratic law (v = 43.17 * s^2 below s = 1.02: `tv.c` 156-169 / `sw.i(F)`; `MoveControl.tick` 334-349 -> `Mob.setSpeed` (setZza at 7), `getFrictionInfluencedSpeed` 7-20, `getInputVector` 17-31, `aiStep` 607-626), so every task-AI species carried across 1:1 (75 rows, ~85 classes, attributes and goal modifiers matching number for number except SpiderDriver, ThePrinceTeen and the port-only BabyDragon); the class that changed law is the two Ender mobs (EnderKnight, EnderReaper) - the rest of the legacy class is motionless, parts or ridden-only. Top ratios (port / 1.7.10): SpiderDriver 0.141x (the 1.21.1 spider base 0.3 for 1.7.10's legacy-era 0.8), EnderReaper idle 3.70x, EnderKnight idle 3.20x, every default-travel swimmer 0.50x in water (the engine's water factor), EnderReaper chase 1.34x, EnderKnight chase 1.004x (the record's claim that HEAD's un-boosted Knight already runs at 1.7.10's boosted pace confirmed: 4.421 vs 4.405 b/s), ThePrinceTeen 1.196x (the constructor's 0.35f over the registered 0.32 - the BOSS-026 fix undone by OPT-009's constructor assert). No MOD record re-tunes any speed. The record's arithmetic corrected (section 1.6): the modern mover scales by the unclamped attribute only above s = 1.0204 - below it the input is 0.98 * s and the pace is quadratic - so the register's 'MULTIPLY_BASE ~2.1 / ADD_VALUE ~0.67 on 0.32' came from a linear reading and would give ~42 b/s (9.6x the 1.7.10 sprint); the boost to the saturated 4.405 b/s is MULTIPLY_BASE +0.786 (Knight) / +0.661 (Reaper) or ADD_VALUE +0.1406 / +0.1271; on today's bases there is nothing to boost for the Knight (0.998). Filed: ENT-S-155 (SpiderDriver on the 1.21.1 spider base), ENT-S-156 (ThePrinceTeen's effective 0.35), ENT-S-157 (the Ender pair's idle walk 3.2x / 3.7x - the class's two members and the (a)+(b) mapping), ENT-S-158 (every default-travel swimmer at half pace in water); the ENT-S-150 and ENT-S-145 entries carry the survey's status and the correction. Presented, nothing decided: the options (a) base re-tune A' = sqrt(0.1 * A_1.7.10) -> Knight 0.1789 / Reaper 0.1924, (b) the modifier at the saturated value, (c) a PN per class, are the owner's (item 23 (i) follows the ruling). No code changed; nothing gated.

GATE: none - a read-only survey (docs-only: the report, four register entries, two status corrections); the next gate is the XS batch

REFUTER: none - a survey report whose engine claims are law-11 checked with javap offsets in Appendix A; the owner rules on it (item 23 (i) follows).
