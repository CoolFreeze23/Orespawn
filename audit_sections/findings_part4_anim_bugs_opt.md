# Findings — Animations/Events, Bugs, Optimizations

Consolidated from `audit_sections/08_animations_events_gui.md` (ANIM), `audit_sections/09_bugs.md` (BUG), `audit_sections/10_optimizations.md` (OPT). Paths: original = `reference_1_7_10_source/sources/danger/orespawn/`, port = `src/main/java/danger/orespawn/` (MHLib under `src/main/java/de/dertoaster/multihitboxlib/`).

Entries: **78 total** — ANIM 20 (DIVERGENT 8 · PARTIAL 10 · MISSING 2) · BUG 31 (CRITICAL 7 · HIGH 6 · MEDIUM 9 · LOW 9) · OPT 27 (HIGH 8 · MEDIUM 11 · LOW 8).

---

## ANIM — Animations, Events, GUI/HUD divergences

### ANIM-001 — Systemic: `wingspeed` → `limbSwingAmount` frequency mistranslation (39 model files)
- **Status:** DIVERGENT
- **Original:** e.g. `ModelButterfly.java:96`, `ModelBee.java:14, 40, 188`, `ModelUrchin.java:14, 34, 155-158` — trig **frequency** multiplied by `this.wingspeed`, a constructor **constant** (usually 1.0; per-reuse, e.g. Mothra 0.2). Idle/hover animations run continuously.
- **Port:** 39 files in `entity/client/` multiply frequency by `limbSwingAmount` (runtime movement amount, ~0 at idle): animations freeze solid when idle/hovering and phase-jitter during speed changes. Verified by grep `ageInTicks \* k \* limbSwingAmount` (hits in parentheses): `KyuubiModel(31)`, `ModelUrchin(21)`, `ModelAttackSquid(20)`, `ModelDungeonBeast(17)`, `TrooperBugModel(14)`, `ModelWaterDragon(10)`, `ModelPitchBlack(10)`, `BeeModel(8)`, `StinkBugModel(8)`, `LeonModel(8)`, `EmperorScorpionModel(7)`, `ModelSeaViper(7)`, `ModelSeaMonster(7)`, `ModelGoldFish(6)`, `SpitBugModel(6)`, `ModelCockateil(5)`, `ScorpionModel(5)`, `ModelCaveFisher(4)`, `ModelBandP(4)`, `ModelHammerhead(4)`, `VelocityRaptorModel(4)`, `ModelCloudShark(4)`, `HydroliscModel(4)`, `MolenoidModel(4)`, `ModelEnderReaper(4)`, `MantisModel(4)`, `ModelEasterBunny(3)`, `LizardModel(3)`, `ModelEnderKnight(3)`, `ModelAlien(3)`, `HerculesBeetleModel(2)`, `OstrichModel(2)`, `TriffidModel(2)`, `LurkingTerrorModel(2)`, `DragonflyModel(2)`, `BrutalflyModel(1)`, `ModelPeacock(1)`, `ButterflyModel(1)`, `TshirtModel(1)`. Includes the table-row cases BeeModel (`BeeModel.java:186-214`, all 8 frequency terms), ModelUrchin (`ModelUrchin.java:137-161`, where `limbSwingAmount` is double-applied: frequency **and** amplitude — the original only scaled amplitude by `f1`), and ButterflyModel (`ButterflyModel.java:96`, shared by Butterfly/LunaMoth/Mothra). Counter-examples done right: `ModelTheKing`/`ModelGodzilla`/`ModelDragon`/`ModelTRex` use a constant `WING_SPEED`/`ANIM_SPEED`.
- **Fix:** In all 39 files, replace `limbSwingAmount` inside `Mth.cos/sin` frequency arguments with a per-model `static final float WING_SPEED` matching the original ctor constant (1.0 default; Mothra 0.2, LunaMoth 0.75); keep `limbSwingAmount` only where the original multiplied **amplitude** by `f1` (e.g. restore Urchin to frequency×const, amplitude×limbSwingAmount).

### ANIM-002 — Kraken: fin twitch driver demoted to client-local random
- **Status:** DIVERGENT
- **Original:** `ModelKraken.java:1029-1058`; `Kraken.java:58, 105, 123-132` — fin twitch keyed on **server-synced** `RenderInfo.ri1`.
- **Port:** `entity/client/ModelKraken.java:594-628` (`:128, 619-622`) — `ri1` is now client-local random; tentacle/fin anims otherwise ported, `getAttacking()` synced (`Kraken.java:317, 387, 627-631`). Cosmetic-only divergence.
- **Fix:** Either accept (visual-only), or add a synched int to `Kraken` mirroring original `ri1` and read it in the model.

### ANIM-003 — Rotator: 24-blade tri-axis gyroscope reduced to 3 flat Z-spinning blades
- **Status:** DIVERGENT
- **Original:** `ModelRotator.java:44-80` — each of 3 blade shapes rendered **8×** in a fan; fans spun on X, Y and Z axes via accumulating `ri.rf1 += 2°`.
- **Port:** `entity/client/RotatorModel.java:33-45` — each shape rendered once, all three spun around **Z only** at 1×/1.5×/2×; the signature 24-blade ball is gone.
- **Fix:** In `renderToBuffer`, render each blade part 8 times with 45° pose offsets, and assign the three fans X/Y/Z rotation axes with an accumulating angle field (2°/frame) as in the original.

### ANIM-004 — Mothra: renders at half size (flap-speed identity covered by ANIM-001)
- **Status:** DIVERGENT
- **Original:** `ClientProxyOreSpawn.java:405-411` — Mothra rendered at scale **10.0** with wingspeed 0.2 (slow majestic flap).
- **Port:** `entity/client/MothraRenderer.java:24` — scale **5.0**; `ButterflyModel.java:96` flaps at ~6.5× intended speed (see ANIM-001).
- **Fix:** Change `MothraRenderer` scale from 5.0 to 10.0; restore wingspeed 0.2 via the ANIM-001 constant fix.

### ANIM-005 — GiantRobot: walk cycle, attack windmill, and duplicate-part limbs all dropped
- **Status:** DIVERGENT
- **Original:** `ModelGiantRobot.java:150-279` (attack at `:230-240`) — full walk cycle (hip bob + 2-phase legs by re-rendering shared parts at both positions), punch-windmill arms when `getAttacking()!=0`, state in `RenderGiantRobotInfo`.
- **Port:** `entity/client/ModelGiantRobot.java:150-161` — only head look + tiny idle arm sway; `renderToBuffer` (`:164-183`) draws each part once, so the second leg/arm of each pair never renders.
- **Fix:** Re-render each shared limb part at both leg/arm positions inside `renderToBuffer` (or duplicate the parts), reinstate the walk-cycle pose math from the original, and gate windmill arms on the entity's `getAttacking()` accessor (see also ANIM-014 for the missing state holder).

### ANIM-006 — SpiderRobot: only 1 of 8 legs renders
- **Status:** DIVERGENT
- **Original:** `ModelSpiderRobot.java:302-411` — 8 legs posed **and rendered inside** the loop (renders at `:392-410`); jaw snap at `:412-427`.
- **Port:** `entity/client/ModelSpiderRobot.java:259-352` — keeps the 8-iteration pose loop but never renders inside it; `renderToBuffer` (`:372+`) draws once after the loop, so only leg i=7 is visible. Jaw snap ported (`:353-368`); gait simplified to a canned sine (`SpiderRobot.java:221-237`).
- **Fix:** Move the leg `render` call inside the 8-iteration pose loop (pose leg i, render leg i) as the original does, or maintain 8 distinct leg `ModelPart`s posed per index.

### ANIM-007 — Robot2: attack-gated arm poses replaced by constant windmill
- **Status:** PARTIAL
- **Original:** `ModelRobot2.java:133-153` — walk legs + attack-gated random arm poses via `getAttacking()`/`RenderInfo.ri1`.
- **Port:** `entity/client/ModelRobot2.java:129-148` — walk ported; arms windmill constantly at 20°/tick regardless of attack state.
- **Fix:** Gate the arm windmill behind the entity's `getAttacking()` synched accessor; pose arms at a resting angle when idle.

### ANIM-008 — Robot3: attack driver dropped (shares Robot2 pattern)
- **Status:** PARTIAL
- **Original:** `ModelRobot3.java` — `getAttacking()`-gated arm animation (same pattern as Robot2).
- **Port:** `entity/client/ModelRobot3.java` — no `getAttacking()` use (verified by absence in grep of attack-driver coverage, 08 §"Attack-driver coverage").
- **Fix:** Same as ANIM-007: read `getAttacking()` and gate the attack arm pose on it.

### ANIM-009 — Robot4: attack-gated shield/cannon arm anims dropped
- **Status:** PARTIAL
- **Original:** `ModelRobot4.java` — walk + attack-gated shield/cannon arm animations keyed on `getAttacking()`.
- **Port:** `entity/client/ModelRobot4.java:417-459` — walk ported; right arm swings on a fixed always-on cycle, cannon arm frozen at a constant angle; no `getAttacking()` use.
- **Fix:** Restore the `getAttacking()` branch: idle pose when 0, shield raise + cannon aim cycle when attacking.

### ANIM-010 — EntityRat: attack head pose dropped
- **Status:** PARTIAL
- **Original:** `ModelRat.java:116` — attack-vs-idle head bob.
- **Port:** `entity/client/RatModel.java:60-67` — walk + head yaw only.
- **Fix:** Read the rat's `getAttacking()` accessor in `setupAnim` and apply the original attack head-bob branch.

### ANIM-011 — Keybinds: fly-up default key changed (Left Alt → Space)
- **Status:** DIVERGENT
- **Original:** `KeyHandler.java:15-18` — one key "OreSpawn UP/FAST", LWJGL 56 = **Left Alt**.
- **Port:** `client/KeybindHandler.java:18-37, 54-62` — fly_up=**SPACE**, fly_down=LCTRL, special=G (two keys are new additions).
- **Fix:** Decide intentionally: either set fly_up default to `GLFW_KEY_LEFT_ALT` for parity, or document SPACE as a deliberate UX change (SPACE conflicts with vanilla mount-jump/dismount expectations).

### ANIM-012 — Rider flight/jump controls missing for 6 of 7 original mounts
- **Status:** PARTIAL
- **Original:** 7 entities poll `flyup_keystate`: `Dragon`, `Leon`, `Cephadrome` (`Cephadrome.java:786-789`), `Ostrich` (jump/FAST, `Ostrich.java:470-474`), `Elevator`, `ThePrinceTeen`, `ThePrinceAdult`.
- **Port:** Only `Dragon` implements `RideableFlyer` (`Dragon.java:148, 344, 982`; grep `RideableFlyer` = 2 files). Port `Cephadrome.java`, `Ostrich.java`, `Leonopteryx.java` have no `travel`/`tickRidden`/`getControllingPassenger` riding control at all; Elevator/ThePrinceTeen/ThePrinceAdult likewise unhandled (`network/RiderInputPayload.java:31-51` falls back to a generic ±0.15 Δy only for `RideableFlyer`).
- **Fix:** Implement `RideableFlyer` (plus `getControllingPassenger`/`tickRidden`) on Cephadrome, Ostrich (jump/FAST semantics), Leonopteryx, Elevator, ThePrinceTeen, ThePrinceAdult so `RiderInputPayload` dispatch reaches them.

### ANIM-013 — HUD: pointed-at-mob health bar reduced to owned-Girlfriend list
- **Status:** PARTIAL
- **Original:** `GirlfriendOverlayGui.java:75-447` — universal crosshair-target health bar (name + `girlfriendgui.png` 182×5 textured bar above hotbar) covering ~45 entity types incl. ownership-gated Girlfriend/Boyfriend, Princes, Dragon, bosses (King `:360-364`, Queen `:365-369`, Mobzilla `:335-339`), robots, big crabs; pointed-entity lookup `:105-114`; bar draw `:432-446`; config gate `:102`.
- **Port:** `client/GirlfriendOverlay.java:27-62` — top-left list of owned Girlfriends within 16 blocks only; flat-color bars; no crosshair targeting, no bosses/mounts/robots. Config gate ported (`:33`).
- **Fix:** Reimplement crosshair-entity resolution (pick entity via `Minecraft.crosshairPickEntity` or a ray trace), restore the textured 182×5 bar centered above the hotbar, and port the ~45-type eligibility list (ownership gates for Girlfriend/Boyfriend).

### ANIM-014 — GiantRobot: `RenderGiantRobotInfo` walk-cycle state holder absent
- **Status:** MISSING
- **Original:** `ModelGiantRobot.java:154-167` — per-entity walk-cycle scratch data in `RenderGiantRobotInfo`.
- **Port:** absent — grep `RenderGiantRobotInfo` = 0 hits (consumed by the walk anim dropped in ANIM-005).
- **Fix:** Recreate `RenderGiantRobotInfo` as a per-entity data class (mirror `entity/client/RenderSpiderRobotInfo.java:3-25`) and feed it from `GiantRobot` tick as part of the ANIM-005 walk-cycle restoration.

### ANIM-015 — Crystal Furnace: cook speed 1.5× and signature crystal fuels inert
- **Status:** PARTIAL
- **Original:** `TileEntityCrystalFurnace.java:174-179` — cook = **150 ticks**; custom fuel table (`:226-277`): lava/CrystalCoal **20000**, CrystalTreeLog 800, CrystalPlanks 400, etc.
- **Port:** `gui/CrystalFurnaceBlockEntity.java:34` — cook = **100 ticks**; fuel via vanilla `fuel.getBurnTime(RecipeType.SMELTING)` (`:183`); no burn-time registration anywhere for Crystal Coal/Log/Planks (grep `getBurnTime|FurnaceFuel` = only this file), so they don't burn.
- **Fix:** Register burn times for Crystal Coal (20000), Crystal Tree Log (800), Crystal Planks (400) via `IItemExtension#getBurnTime` overrides or a `FurnaceFuelBurnTimeEvent` handler; set cook time back to 150 (or document 100 as an intended buff).

### ANIM-016 — Seasonal content: Halloween/Valentine's/Easter gates all absent
- **Status:** MISSING
- **Original:** `OreSpawnMain.java:4518-4521` — `GregorianCalendar` at init: Oct 31 → Ghost/GhostSkelly biome spawns (`:4521-4566`); Feb 14 → `valentines_day=1` (`:4567-4569`) consumed by `MyValentineTarget` AI (`Girlfriend.java:161-162`, `MyValentineTarget.java:47-50`); Apr 20 → `easter_day=1` gating EasterBunny spawns (`:4681`).
- **Port:** absent — grep `Calendar|Valentine|easter` = 0; Ghost/GhostSkelly/EasterBunny have plain config-gated spawns (`ModSpawnControl.java:57-58, 89`).
- **Fix:** Add a date check (`LocalDate.now()`) evaluated at server start/spawn-check time; gate Ghost/GhostSkelly and EasterBunny spawn rules on it and port `MyValentineTarget` (Girlfriend kiss-target goal) activated on Feb 14.

### ANIM-017 — ExperienceCatcher: conversion mechanic entirely different
- **Status:** DIVERGENT
- **Original:** `ExperienceCatcher.java:29-62` — catches **one** orb (value ≥3, 80% chance) → drops Bottle o' Enchanting + string + stick; item consumed.
- **Port:** `item/ExperienceCatcher.java:24-61` — vacuums **all** orbs in r=3 → pays out emeralds/gold/diamonds by XP total.
- **Fix:** Either restore original semantics (single orb ≥3 value, 80% roll, Bottle o' Enchanting + string + stick, consume item) or sign off the redesign explicitly.

### ANIM-018 — Per-mob spawn-disable flags: ~42 of ~100 enforced
- **Status:** PARTIAL
- **Original:** ~100 `XxxEnable` config flags gate `EntityRegistry.addSpawn` (grep `Enable = config.get` = 100, e.g. `OreSpawnMain.java:1519`).
- **Port:** `ModSpawnControl.java:53-101` maps **42** entity types; cancellation via `FinalizeSpawnEvent`+`EntityJoinLevelEvent` (`:109-135`). Bosses/water mobs unmapped; `KRAKEN_ENABLE` does not exist in port config at all (grep = 0).
- **Fix:** Extend the `ModSpawnControl` map to cover all ~100 original flags (add the missing config entries, incl. `KRAKEN_ENABLE`, which `KrakenRevengeHandler` should also respect).

### ANIM-019 — Creeper Repellent: PurplePower target omitted
- **Status:** PARTIAL
- **Original:** `CreeperRepellent.java:94-126` — repels Creeper + EntityAnt + **PurplePower**; `KrakenRepellent.java:93-109` repels Kraken + EntityAnt.
- **Port:** `block/RepellentBlock.java:26-47` + `ModBlocks.java:131-136` — kraken predicate = Kraken‖EntityAnt ✓; creeper predicate = Creeper‖EntityAnt — PurplePower missing.
- **Fix:** Add `|| e instanceof PurplePower` to the creeper-repellent predicate in `ModBlocks.java:131-136`.

### ANIM-020 — Dimension teleporter: 1 of 5+ destinations implemented
- **Status:** PARTIAL
- **Original:** `OreSpawnTeleporter.java:22-96` — custom placement for 5 dims (mining/crystal/chaos/village/islands + utopia).
- **Port:** only `block/UtopiaPortalBlock.java:23-50` (entityInside → utopia/back); no teleporter/portal code for the other dimensions exists.
- **Fix:** Implement portal blocks + placement logic for the remaining dimensions (mirror `UtopiaPortalBlock`), coordinating with the dimension-slice audit on which dims actually exist in the port.

---

## BUG — Port-code bugs (from 09_bugs.md)

### BUG-001 — MHLib `EntityEventHandler` on MOD bus with GAME-bus events
- **Severity:** CRITICAL
- **Location:** `de/dertoaster/multihitboxlib/EntityEventHandler.java:15` (handlers at 19, 35, 44)
- **Scenario:** `@EventBusSubscriber(bus = MOD)` subscribing to `EntityEvent.Size`/`PlayerEvent.StartTracking`/`StopTracking` (GAME-bus) → `IllegalArgumentException` during mod construction → launch crash (or, if not classloaded, hitbox-resize and tracking hooks silently dead).
- **Fix:** Remove `bus = EventBusSubscriber.Bus.MOD` so the class registers on the GAME bus.

### BUG-002 — MHLib `GameEventHandler` on MOD bus with a GAME-bus event
- **Severity:** CRITICAL
- **Location:** `de/dertoaster/multihitboxlib/GameEventHandler.java:9` (handler at 13)
- **Scenario:** Same mismatch as BUG-001 for `PlayerEvent.PlayerLoggedInEvent` — startup crash, or asset-synch enforcement never fires on login.
- **Fix:** Remove `bus = EventBusSubscriber.Bus.MOD` from the annotation.

### BUG-003 — `EntityRat`: `UUID.fromString("")` ticking-entity crash for spawner/summoned rats
- **Severity:** CRITICAL
- **Location:** `danger/orespawn/entity/EntityRat.java:139` (root cause at 110–116)
- **Scenario:** NBT lacking `MyOwner` yields `""` from `getString`; `customServerAiStep` then runs `UUID.fromString("")` → `IllegalArgumentException` → server crash. Crystal-dimension dungeons place rat spawners, so this fires in normal play; such rats also never despawn.
- **Fix:** In `readAdditionalSaveData`, treat empty/`"null"` strings as no owner (`if (s.isEmpty()) myOwner = null;`) and wrap `UUID.fromString` in try/catch as `Fairy.java:191-195` does.

### BUG-004 — Prince growth chain calls `tame(null)` when owner offline — NPE crash
- **Severity:** CRITICAL
- **Location:** `danger/orespawn/entity/ThePrince.java:241`, `ThePrinceTeen.java:207, 246`, `ThePrinceAdult.java:249`
- **Scenario:** `getPlayerByUUID` returns null for an offline owner; `TamableAnimal.tame(null)` NPEs. `ThePrince.customServerAiStep:230` auto-transforms once counters pass thresholds, so a chunk-loaded prince crashes the server the moment its owner logs out.
- **Fix:** Null-check the resolved player; on null, fall back to `setOwnerUUID(this.getOwnerUUID()); setTame(true, true);` instead of `tame(player)` at all four sites.

### BUG-005 — `TheQueen.doHurtTarget` can `discard()` a ServerPlayer
- **Severity:** CRITICAL
- **Location:** `danger/orespawn/entity/TheQueen.java:486–502`
- **Scenario:** When the health-tracked victim hits 0 HP the Queen calls `discard()`; for a player this removes the entity without the death pipeline — no death screen/drops/respawn, ghost connection on the server.
- **Fix:** Restrict the discard path to non-player mobs; for players (and ideally all victims) apply lethal `hurt`/`die(damageSource)` instead.

### BUG-006 — `Godzilla.doJumpDamage` uses `genericKill` — kills Creative/Spectator players
- **Severity:** CRITICAL
- **Location:** `danger/orespawn/entity/Godzilla.java:422–441`
- **Scenario:** Landing shockwave uses `damageSources().genericKill()` (the `/kill` source, bypasses invulnerability) — creative/spectator players near the landing die outright, spectators even through walls.
- **Fix:** Use `damageSources().mobAttack(this)` so vanilla invulnerability rules apply.

### BUG-007 — `SpiderRobot` save/load no-ops without `super` — living-entity data lost
- **Severity:** CRITICAL
- **Location:** `danger/orespawn/entity/SpiderRobot.java:199–202`
- **Scenario:** Empty `addAdditionalSaveData`/`readAdditionalSaveData` overrides drop the `super` call: Health, effects, attribute modifiers, PersistenceRequired, equipment, leash never persist. Half-killed robots reload at full HP; name-tagged ones can despawn.
- **Fix:** Call `super.addAdditionalSaveData(tag)` / `super.readAdditionalSaveData(tag)` in both overrides.

### BUG-008 — `EntityWormLarge` respawns its 40-worm brood on every world reload
- **Severity:** HIGH
- **Location:** `danger/orespawn/entity/EntityWormLarge.java:30` (spawn loop at 133–145)
- **Scenario:** `wormsSpawned` not written to NBT; each chunk reload with the large worm alive spawns another 40 medium/small worms — a few relogs produce hundreds of entities (entity bomb, TPS loss).
- **Fix:** Persist `wormsSpawned` in `addAdditionalSaveData`/`readAdditionalSaveData`.

### BUG-009 — `ModSpawnControl.NATURAL_SPAWNS`: non-thread-safe set mutated from worker threads
- **Severity:** HIGH
- **Location:** `danger/orespawn/ModSpawnControl.java:42`
- **Scenario:** WeakHashMap-backed set mutated from `FinalizeSpawnEvent` on chunk-gen worker threads while the server thread touches it in `EntityJoinLevelEvent` — concurrent rehash can corrupt the map (infinite loop in `getEntry`) or throw CME.
- **Fix:** Wrap with `Collections.synchronizedSet(...)`, or key off entity UUIDs in a `ConcurrentHashMap`-backed set.

### BUG-010 — `ThePrince`/`ThePrincess`: permanent `noPhysics` after being hurt
- **Severity:** HIGH
- **Location:** `danger/orespawn/entity/ThePrince.java:144` (set at 179, 211), `ThePrincess.java:114` (set at 130, 153)
- **Scenario:** `tick()` maps activity 2 → `noPhysics`; `hurt()` sets activity 2 and nothing resets it except the owner sit-toggle. A wild/unattended prince that takes one hit sinks through terrain into the void; activity persists (`SpyroActivity`) so it survives relogs.
- **Fix:** Reset activity to 1 when the attack/target ends (mirror the `setAttacking(0)` path), or stop mapping activity 2 to `noPhysics`.

### BUG-011 — `Kraken` force-sets a caught player's position/motion every tick
- **Severity:** HIGH
- **Location:** `danger/orespawn/entity/Kraken.java:285–301`
- **Scenario:** Server-side `setPos`/`setDeltaMovement` on a client-authoritative player every tick → violent rubber-banding and "moved too quickly/wrongly" kicks on strict servers.
- **Fix:** For `ServerPlayer`, use `connection.teleport(...)` (or make the player a passenger of the tentacle part) instead of raw `setPos`.

### BUG-012 — `TheKing.hurt` silently deletes small attackers
- **Severity:** HIGH
- **Location:** `danger/orespawn/entity/TheKing.java:951–953`
- **Scenario:** Any non-player `Monster` under 3.0 bb that damages TheKing is `discard()`ed — no death event, drops, or `LivingDeathEvent`; players' tamed mobs and other mods' minions are wiped with no feedback.
- **Fix:** Replace `discard()` with lethal damage (`hurt(genericKill)` on non-players) or restrict the wipe to OreSpawn's own minion classes.

### BUG-013 — Worldgen cooldowns are static, cross-dimension, and cross-thread
- **Severity:** HIGH
- **Location:** `danger/orespawn/world/OreSpawnChunkGenerator.java:107`, `world/CrystalStructures.java:68`
- **Scenario:** Static `AtomicInteger recentlyPlaced` shared across all dimension instances and parallel worldgen threads: Mining-dim cooldowns suppress Utopia/Crystal dungeons; check-then-act race makes structure distribution non-deterministic per seed.
- **Fix:** Make `recentlyPlaced` an instance field (per-generator) or key the cooldown by dimension; derive randomness from chunk-seeded random for determinism.

### BUG-014 — `TheKing`: combat/AI state not persisted
- **Severity:** MEDIUM
- **Location:** `danger/orespawn/entity/TheKing.java` (fields `lightningStreamCount`, `iceStreamCount`, `ticker`, `backoffTimer`, `largeEntityDetected`, `attackDamage`, `revengeTarget`, `headEntityFound`)
- **Scenario:** None saved to NBT — relogging mid-fight resets attack streams, backoff, and the buffed `attackDamage`; the boss "forgets" the fight.
- **Fix:** Serialize `attackDamage`, stream counters, and `backoffTimer` in `addAdditionalSaveData`/`readAdditionalSaveData`.

### BUG-015 — `TheKing.dropCustomDeathLoot`: up to 300 random-registry item drops
- **Severity:** MEDIUM
- **Location:** `danger/orespawn/entity/TheKing.java:1321–1339`
- **Scenario:** Two `while (j < 150)` loops draw random IDs from the item/block registries (any mod's items, technical items included) — one kill dumps ~300 item entities: lag spike + exploit-grade loot.
- **Fix:** Replace registry sampling with a curated loot table (or a small whitelisted item pool) and cap total drops to a sane count (e.g. ≤32).

### BUG-016 — `Godzilla`: combat state not persisted
- **Severity:** MEDIUM
- **Location:** `danger/orespawn/entity/Godzilla.java` (fields `ticker`, `streamCount`, `largeUnknownDetected`, `jumped`, `jumpTimer`, `headFound`)
- **Scenario:** Fire-stream and jump state reset on relog; a mid-air "jumped" Godzilla reloads with `jumped=false` and never runs its landing-damage path, leaving stale state.
- **Fix:** Persist `jumped`/`jumpTimer`/`streamCount` in NBT.

### BUG-017 — `TheQueen.mood` not persisted — angry queen reloads happy
- **Severity:** MEDIUM
- **Location:** `danger/orespawn/entity/TheQueen.java:179` (mood logic ~707–720)
- **Scenario:** With `QUEEN_ALWAYS_MAD` off, relogging during a fight resets the Queen to placid — players can defuse aggression by relogging.
- **Fix:** Write/read `mood` in the existing NBT methods.

### BUG-018 — `Kraken` weather lock fights the vanilla weather cycle and isn't persisted
- **Severity:** MEDIUM
- **Location:** `danger/orespawn/entity/Kraken.java:57` (decrement), `:135` (`setWeatherParameters`)
- **Scenario:** Every `weatherSet` expiry re-forces a thunderstorm, overriding `/weather clear` and other mods; timer not saved so relog re-triggers immediately; multiple Krakens each re-arm independently.
- **Fix:** Set weather once per Kraken (persist a flag in NBT) and/or check `level.isThundering()` before forcing.

### BUG-019 — `EntityVortex`: server-side push/launch velocities on players not synced
- **Severity:** MEDIUM
- **Location:** `danger/orespawn/entity/EntityVortex.java:184–187` (pull), `:244–273` (`skywardLaunch`)
- **Scenario:** `push()`/`setDeltaMovement()` on a `ServerPlayer` without `hurtMarked = true` sends no motion packet — the signature tornado pull/launch works only erratically (piggy-backing knockback from coincident `doHurtTarget`).
- **Fix:** Set `victim.hurtMarked = true` for players after modifying `deltaMovement` in both code paths.

### BUG-020 — `Dragon` ridden flight moved server-side while the client owns vehicle movement
- **Severity:** MEDIUM
- **Location:** `danger/orespawn/entity/Dragon.java:343–349` (server `move(SELF)`), `:355–360` (`travel` early-return)
- **Scenario:** With a controlling player passenger, vanilla expects the riding client to move the vehicle; the port moves the dragon in server `aiStep` instead → server and client fight over position → jitter/rubber-banding while flying, worse with latency.
- **Fix:** Implement rider movement in `travel()`/`tickRidden` (client-predicted, like vanilla horses); keep server `move(SELF)` only for the riderless AI path.

### BUG-021 — Crystal structures truncated at chunk borders (silently)
- **Severity:** MEDIUM
- **Location:** `danger/orespawn/world/CrystalStructures.java` (large features, e.g. FairyCastleTree); swallow-catch at `OreSpawnChunkGenerator.java:264`
- **Scenario:** Feature-style generation writes outside the 3×3 writable region during `applyBiomeDecoration`; exceptions are caught and ignored, so big trees/castles generate with sheared-off edges depending on chunk order.
- **Fix:** Convert oversized pieces to Jigsaw/Structure pieces with proper bounding boxes, or clamp placement to the writable region.

### BUG-022 — `EntityVortex` scans all nearby LivingEntities every tick on both sides
- **Severity:** MEDIUM
- **Location:** `danger/orespawn/entity/EntityVortex.java:101` (`tick()`), `:176` (`customServerAiStep`)
- **Scenario:** 32×20×32 `getEntitiesOfClass` + per-candidate LoS raycast every tick on server *and* client (client only needs it for smoke particles) — several vortexes measurably hit frame and tick time.
- **Fix:** Cache the target for ~10 ticks; gate the client particle check behind a cheap distance test. (Perf side covered by OPT-004.)

### BUG-023 — `Mothra`: movement/heal state not persisted
- **Severity:** LOW
- **Location:** `danger/orespawn/entity/Mothra.java:41–45`
- **Scenario:** `lastX/Y/Z`, `stuckCount`, `healthTicker` reset on relog; stuck-detection and regen restart. Minor hiccup only.
- **Fix:** Persist `healthTicker` if regen cadence matters; otherwise accept as-is.

### BUG-024 — `GiantRobot.reloadTicker` not persisted
- **Severity:** LOW
- **Location:** `danger/orespawn/entity/GiantRobot.java:38`
- **Scenario:** Relog during the rocket reload window lets the robot fire immediately. Balance-only.
- **Fix:** Save the ticker in NBT.

### BUG-025 — `Kraken.enchantToolSilk` rolls Silk Touch I–V (illegal levels)
- **Severity:** LOW
- **Location:** `danger/orespawn/entity/Kraken.java:501`
- **Scenario:** Drops can carry Silk Touch above max level 1; anvils/grindstones and validation mods treat the stack as illegal.
- **Fix:** Clamp the rolled level to `enchantment.getMaxLevel()`.

### BUG-026 — `Kraken`: `hitByPlayer`/`callReinforcements` not persisted
- **Severity:** LOW
- **Location:** `danger/orespawn/entity/Kraken.java` (fields near top)
- **Scenario:** Relogging mid-fight re-arms the reinforcement wave — a second squad can spawn.
- **Fix:** Persist both flags in NBT.

### BUG-027 — `TheQueen.myCanSee` truncates negative coordinates toward zero
- **Severity:** LOW
- **Location:** `danger/orespawn/entity/TheQueen.java:1189–1230`
- **Scenario:** `(int)(startx + dx)` rounds toward zero, so in negative-coordinate quadrants the LoS ray samples the wrong block column — Queen occasionally sees through (or fails to see past) corners.
- **Fix:** Use `Mth.floor`/`BlockPos.containing` for the sample positions.

### BUG-028 — `RTPBlock` spawns particles via `Level.addParticle` on the server — never visible
- **Severity:** LOW
- **Location:** `danger/orespawn/block/RTPBlock.java:77–81`
- **Scenario:** `entityInside` returns early on the client then calls `level.addParticle` on the server `Level` (a no-op) — the teleport burst never shows (sound works).
- **Fix:** Use `((ServerLevel) level).sendParticles(...)`.

### BUG-029 — `CrystalFurnaceBlockEntity` consumes bucket fuels without returning the container
- **Severity:** LOW
- **Location:** `danger/orespawn/gui/CrystalFurnaceBlockEntity.java:182–189`
- **Scenario:** Fueling with a lava bucket destroys the bucket (`fuel.shrink(1)`), unlike the vanilla furnace which leaves an empty bucket.
- **Fix:** After shrinking, place `fuel.getCraftingRemainingItem()` into the fuel slot if it's empty.

### BUG-030 — `EntityWormMedium`: `upcount`/`downcount` not persisted
- **Severity:** LOW
- **Location:** `danger/orespawn/entity/EntityWormMedium.java:23–24`
- **Scenario:** Burrow/emerge cycle resets on relog; purely cosmetic.
- **Fix:** Persist both counters, or accept.

### BUG-031 — `EntityVortex.tick` heals client-side
- **Severity:** LOW
- **Location:** `danger/orespawn/entity/EntityVortex.java:117–119`
- **Scenario:** `heal(1.0f)` runs on both sides with independent RNG — client's local health copy briefly diverges (visual only; next health sync corrects).
- **Fix:** Gate the heal behind `!level().isClientSide`.

---

## OPT — Optimization proposals (from 10_optimizations.md)

### OPT-001 — MHLib `getHitboxProfile()` registry lookup per part per tick / per bone per frame
- **Impact:** HIGH
- **Location:** `de/dertoaster/multihitboxlib/api/IMultipartEntity.java:345-357` (impl), `init/MHLibDatapackLoaders.java:34-36`; hot call sites `IMultipartEntity.java:176, 210, 268, 370-391`, `client/IBoneInformationCollectorLayerCommonLogic.java:36-52`
- **Cost:** `BuiltInRegistries.ENTITY_TYPE.getKey()` + datapack-registry map lookup + `Optional` alloc × part count × tick rate (server) and × bone count × frame rate (client) for every multipart boss.
- **Proposal:** Cache `Optional<HitboxProfile>` in a field on the entity (via `IMHLibFieldAccessor`), populated in `mhlibOnConstructor`, invalidated on datapack reload.
- **Behavior:** neutral (identical results; only `/reload` invalidation must be wired)

### OPT-002 — MHLib sends a full multipart update packet every tick even when nothing moved
- **Impact:** HIGH
- **Location:** `de/dertoaster/multihitboxlib/mixin/entity/MixinServerEntity.java:24-33`; payload at `network/server/SPacketUpdateMultipart.java:29-31, 67-77`
- **Cost:** Per tick × per multipart entity × per tracking player: full pos/rot/size for all parts + `ArrayList` + one `PartDataHolder` record per part per tick.
- **Proposal:** Track last-sent part transforms; skip the send when no part moved beyond epsilon and no part data is dirty (or throttle unchanged syncs to every 10 ticks as keepalive).
- **Behavior:** neutral (positions identical for idle bosses; strict change-only send alters nothing visible)

### OPT-003 — MHLib master client streams `CPacketBoneInformation` continuously regardless of change
- **Impact:** HIGH
- **Location:** `de/dertoaster/multihitboxlib/api/IMultipartEntity.java:283-319` (`updateSynching`); per-frame collection `client/IBoneInformationCollectorLayerCommonLogic.java:34-61`; builder allocs `network/client/CPacketBoneInformation.java:73-165`
- **Cost:** Per-tick C2S packet per mastered multipart entity + per-frame `synchronized tryAddBoneInformation` (`mixin/entity/MixinLivingEntity.java:134`) + Optional/HashSet churn per bone.
- **Proposal:** Diff bone info against the last sent packet and only send on change, plus a low-rate keepalive so the 10-tick master-timeout (`updateSynching:288`) doesn't rotate masters.
- **Behavior:** affecting (server-side hitbox positions for static poses update less often — no visible difference; keepalive required to preserve master-election behavior)

### OPT-004 — EntityVortex: up to 3 ungated AABB scans per tick on both sides
- **Impact:** HIGH
- **Location:** `danger/orespawn/entity/EntityVortex.java:101` (`tick()`, client+server), `:176` (`customServerAiStep`), scan at `:277-281`
- **Cost:** 2–3 `getEntitiesOfClass(LivingEntity, inflate(16,10,16))` + full list sort per vortex per tick — worst ungated per-tick scan in the port.
- **Proposal:** Cache the found target in a field, rescan every 5 ticks, reuse the result between `tick()` (particles only need "has target") and `customServerAiStep()`.
- **Behavior:** affecting (pull/aggro and particle-onset latency goes from 0 to ≤5 ticks — flag for sign-off)

### OPT-005 — GirlfriendOverlay: entity scan + string concat every rendered frame
- **Impact:** HIGH
- **Location:** `danger/orespawn/client/GirlfriendOverlay.java:39-41` (per-frame AABB + `getEntitiesOfClass`), `:47`, `:59` (string concats)
- **Cost:** Per frame (60–240 Hz): AABB alloc, predicate entity query, list alloc, 2+ string allocs per girlfriend.
- **Proposal:** Move the scan to a `ClientTickEvent.Post` handler refreshing a cached list (with pre-formatted name/health strings) every 10 ticks; `render` only draws the cache.
- **Behavior:** neutral (HUD data at most 0.5 s stale — cosmetic latency only)

### OPT-006 — Kraken obstruction probe: 95 block reads every server tick, unconditionally
- **Impact:** HIGH
- **Location:** `danger/orespawn/entity/Kraken.java:188` (call), `:339-360` (`applyObstructionAvoidance`: 19×5 grid, `new BlockPos` per probe)
- **Cost:** ~95 `getBlockState` + ~95 `BlockPos` allocs per Kraken per tick; Krakens spawn in packs of 1–10 (`KrakenRevengeHandler`, reinforcements at `Kraken.java:247-258`).
- **Proposal:** Run the probe every 4–5 ticks with one `BlockPos.MutableBlockPos`, scaling the lift impulse by the interval to keep net buoyancy identical.
- **Behavior:** affecting (obstruction-response latency up to 5 ticks; impulse scaling keeps average motion equal — flag)

### OPT-007 — Worm chain: duplicate player/segment scans twice per tick with allocations
- **Impact:** HIGH
- **Location:** `danger/orespawn/entity/EntityWormLarge.java:104, 165`; `EntityWormSmall.java:87, 139`; worst `EntityWormMedium.java:90-97, 150-156` (`getNearestEntity` + fresh `TargetingConditions` + AABB, twice per tick)
- **Cost:** 2 player-list scans per worm per tick; Medium adds 2 entity scans + 2 `TargetingConditions` allocs per tick.
- **Proposal:** Hoist `TargetingConditions.forNonCombat()` to a `static final`; compute nearest-player/nearest-small-worm once per tick and share between `aiStep` and `customServerAiStep`.
- **Behavior:** neutral (hoist + same-tick sharing); any further throttling (every 2–4 ticks) is affecting (tracking latency) — flag separately

### OPT-008 — Crystal-dimension terrain rewrite scans the full world column with a BlockPos per block
- **Impact:** HIGH
- **Location:** `danger/orespawn/world/OreSpawnChunkGenerator.java:302-336` (`replaceTerrain`: ≈98k `new BlockPos` + `getBlockState` per chunk), `:345-377` (`fillShallowWater`: second column scan)
- **Cost:** ~100k+ short-lived `BlockPos` allocations and full-height state reads per chunk on worldgen worker threads.
- **Proposal:** Reuse one `BlockPos.MutableBlockPos` per column; start the downward scan at `chunk.getHeight(WORLD_SURFACE_WG, x, z)`; merge `fillShallowWater` into the same column walk.
- **Behavior:** neutral (identical block output; heightmap start is safe — everything above surface is air)

### OPT-009 — ~35 entity classes reset MOVEMENT_SPEED base value every tick
- **Impact:** MEDIUM
- **Location:** Representative: `Godzilla.java:222`, `ThePrinceAdult.java:121`, `Baryonyx.java:68`, `EntityKyuubi.java:64`, `Basilisk.java:92`, `Camarasaurus.java:85`, `Girlfriend.java:118`, `Alien.java:102`, `Dragon.java:284`, `Boyfriend.java:119`, `Nastysaurus.java:72`, `Cryolophosaurus.java:66`, `Pointysaurus.java:83`, `BandP.java:92`, `EntityRat.java:85`, `EntityRubberDucky.java:120`, `EntitySpyro.java:129`, `EasterBunny.java:65`, `EntityLeafMonster.java:72`, `EntityMolenoid.java:91`, `ThePrinceTeen.java:120`, `CreepingHorror.java:66`, `DungeonBeast.java:79`, `EntityLeon.java:298`, `Cephadrome.java:127`, `Crab.java:98-99`, `Peacock.java:66`, `Cassowary.java:57`, `Alosaurus.java:84`, `EntityStinky.java:145`, `TRex.java:79`, etc. (only `SeaViper.java:147-149`/`WaterDragon.java:149-151` are genuinely dynamic)
- **Cost:** Attribute-map lookup per entity per tick across ~140 entity types (no sync spam — `setBaseValue` early-exits on equal — but the lookup is pure waste for constants).
- **Proposal:** Delete the per-tick call for constant speeds and set the value in `createAttributes()`; for water/land mirrors (SeaViper, WaterDragon, Crab) cache the `AttributeInstance` and only call `setBaseValue` on medium change.
- **Behavior:** neutral

### OPT-010 — Godzilla allocates a Vec3 array + Vec3 per part every tick
- **Impact:** MEDIUM
- **Location:** `danger/orespawn/entity/Godzilla.java:216-220`
- **Cost:** `new Vec3[allParts.length]` + one `Vec3` per part per tick (server + client).
- **Proposal:** Replace with three reusable `double[]` fields (or store prev positions on the parts themselves).
- **Behavior:** neutral

### OPT-011 — ~37 sound getters allocate a new SoundEvent + ResourceLocation on every call
- **Impact:** MEDIUM
- **Location:** Representative: `GiantRobot.java:157-171`, `Robot1.java`–`Robot5.java` (3 each), `ThePrincess.java`, `ThePrinceTeen.java`, `PitchBlack.java` (3 each), `SpiderRobot.java:149-150, 187-188`, `Ostrich.java`, `VelocityRaptor.java`, `Fairy.java`, `Lizard.java`, `Ghost.java`, `GhostSkelly.java`
- **Cost:** `SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(...))` per ambient/hurt/death sound query (ambient polled periodically per mob); two allocs + string handling per call; bypasses the 100 registered `ModSounds` entries.
- **Proposal:** Replace with the corresponding `ModSounds.X.get()` holder (or a `static final SoundEvent` per class).
- **Behavior:** neutral (same sound id; registered events also serialize properly to clients)

### OPT-012 — Oversized-weapon culling mixin: 8 deferred-holder item checks per entity per frame
- **Impact:** MEDIUM
- **Location:** `danger/orespawn/mixin/EntityCullingMixin.java:16-33`
- **Cost:** Inside `getBoundingBoxForCulling` for every entity (vanilla included) every frame: `getMainHandItem` + up to 8 `ModItems.X.get()` + `is()` checks + `inflate(5.0)` AABB alloc on match.
- **Proposal:** Replace the 8 checks with a single item-tag test (`mainHand.is(OVERSIZED_WEAPONS_TAG)`) or a lazily-built `static Set<Item>`; `stack.isEmpty()` early-out already exists.
- **Behavior:** neutral

### OPT-013 — Twelve big-mob renderers disable frustum culling entirely
- **Impact:** MEDIUM
- **Location:** `entity/client/QueenRenderer.java:50-52`, plus `shouldRender` overrides in `TheKingRenderer`, `GodzillaRenderer`, `GodzillaHeadRenderer`, `KingHeadRenderer`, `QueenHeadRenderer`, `KrakenRenderer`, `MothraRenderer`, `DungeonBeastRenderer`, `SeaMonsterRenderer`, `PitchBlackRenderer`, `LeonopteryxRenderer`
- **Cost:** Full model render every frame whenever the entity is loaded, even fully off-screen — for the largest models in the mod.
- **Proposal:** Keep culling but size `Entity.getBoundingBoxForCulling()` (or `shouldRender` calling super with an inflated AABB, ~30 blocks) to the real part envelope instead of returning `true` unconditionally.
- **Behavior:** neutral (visually, if the inflated box covers the part envelope — flag for visual verification on wing/tail extremes)

### OPT-014 — Alien torch scan probes 4,913 blocks per scan (docs claim 256)
- **Impact:** MEDIUM
- **Location:** `danger/orespawn/entity/AlienTorchSeekGoal.java:134-153` (radius 8 → 17³ probes), throttle at `:70` (~every 30 ticks per alien)
- **Cost:** ~4,900 block reads per alien per ~30 ticks, multiplied by alien pack sizes.
- **Proposal:** Scan in expanding shells with early exit once a torch is found within the legacy ≈5-block break distance, or cap radius to the documented 256-candidate budget; keep the existing `MutableBlockPos`.
- **Behavior:** neutral for early-exit-on-nearest; affecting if radius is reduced (smaller seek range — flag)

### OPT-015 — Godzilla block crushing re-resolves deferred block holders per block
- **Impact:** MEDIUM
- **Location:** `danger/orespawn/entity/Godzilla.java:405-418` (`crushBlocks`), `:374-402` (`isCrushable`: ~20 identity compares incl. 6 `ModBlocks.X.get()` resolutions per block), called twice every 4th tick over a 29×29 slice (`:611-626`)
- **Cost:** ~1,700 block reads + up to ~34k comparisons per 4 ticks while loaded; `BlockPos.containing` alloc per block.
- **Proposal:** Build a lazily-initialized `static Set<Block>` of non-crushables (resolve `ModBlocks` holders once) and iterate with a `MutableBlockPos`.
- **Behavior:** neutral

### OPT-016 — King/Queen target scans sort the entire 80×64×80 entity list
- **Impact:** MEDIUM
- **Location:** `danger/orespawn/entity/TheKing.java:1149-1173` (sort at `:1162`), `TheQueen.java:1254-1262`; re-scan of the same box at `TheKing.java:1273-1285`
- **Cost:** Every ~3–5 ticks per boss: full `LivingEntity` query over a 160×128×160 region + O(n log n) sort, when only nearest-suitable + a "head exists" flag are needed.
- **Proposal:** Replace sort-then-scan with single-pass nearest-suitable selection (track min distance); reuse one scan result for `findSomethingToAttack`/`findNearestPlayer` within the same tick.
- **Behavior:** neutral (nearest-first selection preserved)

### OPT-017 — Ghost / GhostSkelly poll getNearestPlayer every idle tick
- **Impact:** MEDIUM
- **Location:** `danger/orespawn/entity/Ghost.java:96-100`, `GhostSkelly.java:87-91` (runs whenever `attackCooldown == 0`; the second gated call at `Ghost.java:113-114`/`GhostSkelly.java:104-105` is fine)
- **Cost:** Player-list distance scan per ghost per tick (cheap per call but ungated; contact range ~2 blocks).
- **Proposal:** Early-exit with a squared-distance check against the cached flight-target player, or gate the fallback poll to every 5 ticks.
- **Behavior:** neutral for the distance-early-exit variant; affecting if throttled (≤5 ticks contact-damage latency — flag)

### OPT-018 — MHLib runs a hitbox-profile registry lookup in every LivingEntity constructor
- **Impact:** MEDIUM
- **Location:** `de/dertoaster/multihitboxlib/mixin/entity/MixinLivingEntity.java:74-76` → `IMultipartEntity.mhlibOnConstructor` (`IMultipartEntity.java:469-505`, up to 4 `getHitboxProfile()` calls)
- **Cost:** Registry `getKey` + datapack lookup × 4 for every living entity constructed JVM-wide (vanilla mobs included) — significant during chunk load / spawn waves.
- **Proposal:** Memoize per `EntityType` in a static `Map<EntityType<?>, Optional<HitboxProfile>>` invalidated on datapack reload; bail out on cached empty.
- **Behavior:** neutral

### OPT-019 — MHLib part alignment allocates ~6 Vec3 per part per tick with linear `contains`
- **Impact:** MEDIUM
- **Location:** `de/dertoaster/multihitboxlib/api/IMultipartEntity.java:162-193` (`alignSubParts`: chained Vec3 allocs + `synchedBones().contains` linear scan per part), `:195-239` (`alignSynchedSubParts`: fallback `BoneInformation` alloc per synced bone per tick)
- **Cost:** Per part per tick for every multipart boss on the server.
- **Proposal:** Precompute a per-part `isSynched` boolean at construction; fold the rotation/scale/translate chain into inline double math; only build the fallback `BoneInformation` when the sync map lacks the bone.
- **Behavior:** neutral

### OPT-020 — Boss bars updated every tick
- **Impact:** LOW
- **Location:** `PitchBlack.java:344`, `Kraken.java:155`, `Mothra.java:237`, `Godzilla.java:570`, `TheQueen.java:642`, `SpiderRobot.java:99`, etc.
- **Cost:** Negligible — `ServerBossEvent.setProgress` only broadcasts on change.
- **Proposal:** No action needed; listed to close out the checklist item.
- **Behavior:** neutral (N/A — no change proposed)

### OPT-021 — Sort-then-first-match pattern in ~25 small mobs
- **Impact:** LOW
- **Location:** Representative: `Fairy.java:147`, `EntityMantis.java:232`, `BandP.java:218`, `EntityVortex.java:280`, `EntityLeon.java:557`, `Robot2`–`Robot5`, `GiantRobot.java:142`
- **Cost:** O(n log n) sort of a small list every scan where a single-pass min would do.
- **Proposal:** Shared single-pass nearest-suitable selection helper in a util class.
- **Behavior:** neutral

### OPT-022 — Armor auto-enchant check runs per armor piece per inventory tick
- **Impact:** LOW
- **Location:** `danger/orespawn/item/ItemOreSpawnArmor.java:130-150`
- **Cost:** One data-component presence check per OreSpawn armor stack per tick (cheap; enchant application itself is once).
- **Proposal:** Apply enchants in `onCraftedBy`/first pickup instead of polling `inventoryTick`, or gate the check to every 20 ticks.
- **Behavior:** affecting (`onCraftedBy` migration changes when loot/creative-given stacks get enchanted; the 20-tick gate delays first-tick enchanting by ≤1 s — flag whichever is chosen)

### OPT-023 — Godzilla re-resolves the Mobzilla SavedData every tick
- **Impact:** LOW
- **Location:** `danger/orespawn/entity/Godzilla.java:579-581` → `MobzillaSpawnTracker.get():67-70`
- **Cost:** `overworld().getDataStorage().computeIfAbsent` map lookup per tick (markSpawned is idempotent-guarded).
- **Proposal:** Cache a local `markedSpawned` boolean on the entity and skip after first success.
- **Behavior:** neutral

### OPT-024 — Dragon rider-mode pushes via a broad `getEntities` query per tick
- **Impact:** LOW
- **Location:** `danger/orespawn/entity/Dragon.java:472-478`
- **Cost:** Per tick while ridden: AABB alloc + all-entity query (mirrors vanilla `pushEntities`, acceptable).
- **Proposal:** Optional: pass a `pushable` predicate into the query to skip the post-filter.
- **Behavior:** neutral

### OPT-025 — Worldgen flora helpers allocate BlockPos pairs in descending column loops
- **Impact:** LOW
- **Location:** `danger/orespawn/world/OreSpawnChunkGenerator.java:613-620` (flowers), `:636-643` (rice), `:659-666` (quinoa), `:682-689` (termite mounds), `:203-211` (scraggly trees), `:559-587` (ore veins: `new BlockPos` + `BlockState.equals` per cell)
- **Cost:** Per chunk: a few thousand short-lived `BlockPos` allocations; `equals` instead of `==`/`is()` on interned states.
- **Proposal:** Reuse a `MutableBlockPos`; start scans at the heightmap; compare states with `.is(block)`.
- **Behavior:** neutral

### OPT-026 — TheKing line-of-sight: manual 20+-step block ray per candidate
- **Impact:** LOW
- **Location:** `danger/orespawn/entity/TheKing.java:1185-1225` (`MyCanSee`), called per candidate from `isSuitableTarget:1138`
- **Cost:** Up to ~20–60 `getBlockState` + `BlockPos.containing` allocs per candidate per scan (scan throttled ~1/3–5 ticks).
- **Proposal:** Evaluate `MyCanSee` only for the current best candidate (after the distance min-pass) and reuse a `MutableBlockPos` in the march.
- **Behavior:** neutral (if applied only to the selected candidate in the same order)

### OPT-027 — Spawn-cluster checks scan twice per spawn attempt
- **Impact:** LOW
- **Location:** Representative: `EntityAnt.java:208-214`, `EntityCricket.java:156-162`, `Chipmunk.java:211-219`, `EntityTermite.java:221-228`, `Frog.java:273-283` — each runs both a `size() <= N` check and a separate count over the same `inflate(20,10,20)` box
- **Cost:** Spawn-time only: duplicate entity query per `checkSpawnRules` call during spawn cycles.
- **Proposal:** Compute the count once and reuse for both checks.
- **Behavior:** neutral
