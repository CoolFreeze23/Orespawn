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

## Phase D — slice D6b batches 1-4: mechanical structures (2026-08-08/10, batch 4 COMPLETE — close-out pending)

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