# 08 — Animations, Events/Tick, GUI/HUD, Networking

Audit slice for the 1.7.10 → NeoForge 1.21.1 port. Paths: original = `reference_1_7_10_source/sources/danger/orespawn/` (flat, CFR), port = `src/main/java/danger/orespawn/`.

**Architectural note (corrects the audit brief):** the port does **NOT** use GeckoLib broadly. Exactly **one** entity uses GeckoLib: `TheQueen` (only `the_queen.animation.json` / `the_queen.geo.json` exist under `assets/orespawn/animations/entity/` and `geo/entity/`). All other ~150 entities use hand-translated vanilla `EntityModel` classes in `entity/client/` mirroring the original `Model*.java` procedural animation. All 107 original `Model*` classes have a port counterpart class (8 of them are item models, ported as `client/model/*ItemModel.java`); no model class is wholly missing. The original `RenderInfo`/`RenderSpiderRobotInfo`/`RenderGiantRobotInfo` classes are **not HUD overlays** — they are per-entity animation scratch-state holders (`RenderInfo.java:6-15`).

---

## Animations

### TheQueen (GeckoLib — the only one)

| Check | Result |
|---|---|
| Controllers | `TheQueen.java:1366-1386` — "Movement" (idle / idle_to_attack / attack stance machine) + "Actions" (triggerable one-offs, default `PlayState.STOP`). |
| Controller↔JSON name match | EXACT both directions. Controller refs: `idle`, `idle_to_attack`, `attack` (`TheQueen.java:148-153`), triggerables `bite`→`bite`, `tail_left`→`tail_whip_left`, `tail_right`→`tail_whip_right`, `roar`→`roar`, `death`→`death` (`TheQueen.java:1380-1385`). JSON clips: `idle`(3.58s loop), `idle_to_attack`(3.58s hold), `attack`(3.58s loop), `bite`(0.90s), `tail_whip_right`(1.75s), `tail_whip_left`(1.79s), `roar`(3.58s hold), `death`(3.58s hold). No dangling refs either direction. |
| Server-side setters | All present: `IS_AWAKE`/`TRANSITION_TICKS` synched data (`TheQueen.java:129-135, 273-274`), set in `hurt()` gate (`:538-541`) and tick countdown (`:676-683`); persisted to NBT (`:1318-1319, 1330-1335`). Melee triggers from server AI via `triggerQueenAction` (`:1032-1045`); death anim fired before `super.die()` (`:1338-1344`). |
| Timing coupling | Melee damage deferred to impact frame: bite 8t / tails 12t / roar 16t (`TheQueen.java:1036-1044, 690-699`) — all < clip lengths, OK. **Minor mismatch:** `WAKE_UP_DURATION_TICKS = 60` (`:135`) vs `idle_to_attack` length 3.583s = **71.7 ticks** — Movement controller is promoted to the `attack` loop ~12 ticks before the transition clip ends (masked by the 5-tick blend, `:1367`). |
| Latent risk | `bite`/`tail_whip_*`/`attack` are `loop: true` in JSON but fired with `thenPlay()`. GeckoLib 4 stage loop-type (PLAY_ONCE) takes precedence, so behavior should be correct, but the JSON loop flags are misleading — UNVERIFIED at runtime. |

**Status: PORTED** (verified end-to-end).

### Procedural models — spot checks (16 entities)

| Entity | Original driver(s) | Port | Status |
|---|---|---|---|
| **TheKing** | wing/jaw/tail driven by `getAttacking()` datawatcher + ageInTicks (`ModelTheQueen`-style; `ModelTheKing.java`) | `entity/client/ModelTheKing.java:938-1105` — faithful 1:1 (wing flap freq 0.75/0.35 by attack state, 3-head jaw chatter, tail wave). Driver synced server-side: `TheKing.java:102, 230, 243-244`, set in AI at `:780, 826-838` | **PORTED** |
| **Godzilla** | walk-cycle legs (`limbSwingAmount`-gated), arm/jaw rage on `getAttacking()` (`ModelGodzilla.java:633-663`) | `entity/client/ModelGodzilla.java:534-643` — faithful incl. claw curl; `Godzilla.java:68, 124, 290-295`, setters in attack AI `:665-692` | **PORTED** |
| **TRex** | jaw chomp when attacking, walk lean (`ModelTRex.java:216-227`) | `entity/client/ModelTRex.java:216, 231`; setter via `DinosaurMeleeAttackGoal` (`TRex.java:30, 48, 69, 165-170`) | **PORTED** |
| **Dragon** | wing flap / activity (sit-fly) / attack jaw, `getAttacking()`+`getActivity()` (`ModelDragon.java:421, 457, 492-510`) | `entity/client/ModelDragon.java:412-511` faithful; Dragon is also the only `RideableFlyer` (`Dragon.java:148, 344, 982`) | **PORTED** |
| **Worms (L/M/S)** | idle slither sine chain (`ModelWormLarge.java:184-274`); burrow is entity logic via `noPhysics` (`WormLarge.java:45, 139-155`) | `WormLargeModel.java:186-278` 1:1; burrow ported (`EntityWormLarge.java:35, 92-128`); Medium/Small same pattern | **PORTED** |
| **EntityAnt** | leg scuttle ×6 + jaw chew (`ModelAnt.java:162-171`) | `AntModel.java:165-178` 1:1 (incl. `*limbSwingAmount` amplitude) | **PORTED** |
| **Kraken** | 6 tentacles `dangle_tentacle` keyed on `getAttacking()`; fin twitch keyed on **server-synced** `RenderInfo.ri1` (`ModelKraken.java:1029-1058`; `Kraken.java:58, 105, 123-132`) | `ModelKraken.java:594-628` tentacles/fins ported, but `ri1` is now **client-local random** (`:128, 619-622`) instead of entity state — cosmetic-only divergence; `Kraken.java:317, 387, 627-631` sets attacking server-side | **DIVERGENT** (minor, visual) |
| **Rotator** | spin special: each of 3 blade shapes rendered **8×** in a fan, fans spun on X, Y and Z axes via accumulating `ri.rf1 += 2°` (`ModelRotator.java:44-80`) | `RotatorModel.java:33-45` renders each shape **once**, spins all three around **Z only** at 1×/1.5×/2× speed — the signature 24-blade gyroscope ball is gone | **DIVERGENT** (major visual) |
| **Mothra** (shares ModelButterfly) | wing flap `cos(f2 * 1.3 * wingspeed)`, wingspeed = ctor constant: Butterfly 1.0, LunaMoth 0.75, **Mothra 0.2**, render scale 10.0 (`ModelButterfly.java:96`; `ClientProxyOreSpawn.java:405-411`) | `ButterflyModel.java:96` uses `ageInTicks * 1.3f * limbSwingAmount` — wingspeed constant replaced by movement amount (flap freezes when hovering, freq jitters with speed; Mothra's slow-flap identity lost). `MothraRenderer.java:24` scales 5.0 not 10.0 | **DIVERGENT** |
| **EntityBee** | wing/antenna sines on `wingspeed=1.0` const + attack stinger state (`ModelBee.java:14, 40, 188`) | `BeeModel.java:186-214` — same `wingspeed→limbSwingAmount` substitution in all 8 frequency terms; `getAttacking()` branch kept | **DIVERGENT** |
| **GiantRobot** ("Jeffery") | full walk cycle (hip bob + 2-phase legs by re-rendering shared parts at both positions), punch-windmill arms when `getAttacking()!=0`, state in `RenderGiantRobotInfo` (`ModelGiantRobot.java:150-279`, attack at `:230-240`) | `entity/client/ModelGiantRobot.java:150-161` — **all of it dropped**; only head look + tiny idle arm sway. `renderToBuffer` (`:164-183`) draws each part once, so the second leg/arm of each pair (drawn by reposition-re-render in 1.7.10) never renders. No `RenderGiantRobotInfo` class exists in port | **DIVERGENT** (major) |
| **SpiderRobot** | 8 legs posed AND rendered inside the loop (`ModelSpiderRobot.java:302-411`, renders at `:392-410`); jaw snap on `getAttacking()` + `gpcounter` (`:412-427`) | `ModelSpiderRobot.java:259-352` keeps the 8-iteration pose loop **but never renders inside it**; `renderToBuffer` (`:372+`) draws once after, so **only leg i=7 is visible — 7 of 8 legs do not render**. Jaw snap ported (`:353-368`); gait data simplified to a canned sine in `SpiderRobot.java:221-237` | **DIVERGENT** (major, rendering bug) |
| **Robot2** | walk legs + attack-gated random arm poses via `getAttacking()`/`RenderInfo.ri1` (`ModelRobot2.java:133-153`) | `ModelRobot2.java:129-148` — walk ported; arms **windmill constantly at 20°/tick regardless of attack state**; attack/idle distinction dropped | **PARTIAL** |
| **Robot4** | walk + attack-gated shield/cannon arm anims (`ModelRobot4.java`, `getAttacking()` refs) | `ModelRobot4.java:417-459` — walk ported; right arm swings on a fixed always-on cycle, cannon arm frozen at constant angle; no `getAttacking()` use | **PARTIAL** |
| **EntityRat** | attack-vs-idle head bob (`ModelRat.java:116`) | `RatModel.java:60-67` — walk + head yaw only; attack pose dropped | **PARTIAL** |
| **Urchin** | spine wave `cos(f2 * k * wingspeed) * amp * f1` (`ModelUrchin.java:14, 34, 155-158`) | `ModelUrchin.java:137-161` — frequency `* limbSwingAmount` **and** amplitude `* limbSwingAmount` (double-applied) | **DIVERGENT** |

### Systemic finding — `wingspeed → limbSwingAmount` mistranslation

Original models multiply the trig **frequency** by `this.wingspeed`, a constructor **constant** (almost always 1.0; varies per reuse, e.g. Mothra 0.2). 39 port model files instead multiply frequency by `limbSwingAmount` (runtime movement amount, ~0 at idle). Consequences: idle/hover animations freeze solid (original mobs idled with motion), and frequency modulation causes phase-jitter during speed changes. Affected (grep `ageInTicks \* k \* limbSwingAmount`): `ModelWaterDragon(10)`, `ModelAttackSquid(20)`, `ModelUrchin(21)`, `KyuubiModel(31)`, `ModelDungeonBeast(17)`, `TrooperBugModel(14)`, `ModelPitchBlack(10)`, `BeeModel(8)`, `LeonModel(8)`, `StinkBugModel(8)`, `EmperorScorpionModel(7)`, `ModelSeaViper(7)`, `ModelSeaMonster(7)`, `ModelGoldFish(6)`, `SpitBugModel(6)`, plus 24 more files (1-5 hits each) incl. `ButterflyModel`, `MantisModel`, `ModelAlien`, `ModelHammerhead`, `ScorpionModel`, `VelocityRaptorModel`, `HydroliscModel`, `MolenoidModel`, `ModelCockateil`, `ModelCloudShark`, `ModelCaveFisher`, `OstrichModel`, `DragonflyModel`, `BrutalflyModel`. By contrast `ModelTheKing`/`ModelGodzilla`/`ModelDragon`/`ModelTRex` correctly use a constant (`WING_SPEED`/`ANIM_SPEED`). **DIVERGENT (systemic, ~39 files).**

Attack-driver coverage: of 46 original entity models reading `getAttacking()`, 40 port models retain it; dropped in `ModelRobot2/3/4`, `ModelGiantRobot`, `RatModel` (`ModelRobot3` shares Robot2's pattern, verified by absence in grep). No port animation state lacks a server-side setter among those checked — every `getAttacking()` is a `SynchedEntityData` accessor written in server AI. No geo-without-anim or anim-without-geo dangling assets (1 of each, both for the_queen, both referenced by `QueenModel.java:43-46`).

---

## Keybinds & rider control

| Mechanic | Original | Port | Status |
|---|---|---|---|
| Keybind registration | One key: "OreSpawn UP/FAST", LWJGL code 56 = **Left Alt** (`KeyHandler.java:15-18`); empty `onKeyInput` (`:21-23`) | Three keys: fly_up=**SPACE**, fly_down=LCTRL, special=G (`client/KeybindHandler.java:18-37`, registered `:54-62`) | **DIVERGENT** (different default key; 2 new keys are additions) |
| Client→server input send | `RiderControl.onTick` sends `RiderControlMessage` only on keystate **change** (`RiderControl.java:22-33`) | `KeybindHandler.onClientTick` sends `RiderInputPayload` **every tick while held**, only when mounted and no screen open (`KeybindHandler.java:39-52`) | **PORTED** (semantics equivalent; per-tick send is chattier) |
| Server handling | Sets **global static** `OreSpawnMain.flyup_keystate` (`RiderControlMessageHandler.java:25`) — shared by all players (original bug) | Per-player: resolves `player.getVehicle()`, dispatches to `RideableFlyer` interface or generic ±0.15 Δy fallback (`network/RiderInputPayload.java:31-51`) | **PORTED** (fixes the global-state bug) |
| Consumers of fly-up | 7 entities poll `flyup_keystate`: `Dragon`, `Leon`, `Cephadrome` (`Cephadrome.java:786-789`), `Ostrich` (jump/FAST, `Ostrich.java:470-474`), `Elevator`, `ThePrinceTeen`, `ThePrinceAdult` | Only `Dragon` implements `RideableFlyer` (`Dragon.java:148, 344, 982`; grep `RideableFlyer` = 2 files). Port `Cephadrome.java`, `Ostrich.java`, `Leonopteryx.java` have **no** `travel`/`tickRidden`/`getControllingPassenger` riding control at all | **PARTIAL** — rider flight/jump MISSING for Cephadrome, Ostrich, Leonopteryx(Leon), Elevator, ThePrinceTeen, ThePrinceAdult |

---

## HUD/GUI

| Mechanic | Original | Port | Status |
|---|---|---|---|
| `GirlfriendOverlayGui` | Despite the name, a **universal pointed-at-mob health bar**: renders name + textured health bar (`girlfriendgui.png`, 182×5, centered above hotbar) for the entity under the crosshair, covering ~45 entity types incl. Girlfriend/Boyfriend (ownership-gated), Princes, Dragon, bosses (King `:360-364`, Queen `:365-369`, Mobzilla `:335-339`), robots, big crabs (`GirlfriendOverlayGui.java:75-447`; pointed-entity lookup `:105-114`; bar draw `:432-446`; gated on `GuiOverlayEnable` `:102`) | `client/GirlfriendOverlay.java:27-62` — top-left list of **owned Girlfriends within 16 blocks only**; flat-color bars; no crosshair targeting, no Boyfriend, no bosses/mounts/robots. Config gate ported (`OreSpawnConfig.GUI_OVERLAY_ENABLE`, `:33`) | **PARTIAL** (≈1 of ~45 entity cases; core "boss health bar" UX missing) |
| `RenderSpiderRobotInfo` | Anim scratch data (`SpiderRobot` leg angles) | Ported as data class `entity/client/RenderSpiderRobotInfo.java:3-25`, filled by `SpiderRobot.java:221-237` | **PORTED** (but see SpiderRobot render bug above) |
| `RenderGiantRobotInfo` | GiantRobot walk-cycle scratch data (`ModelGiantRobot.java:154-167`) | No class in port; grep `RenderGiantRobotInfo` = 0 hits | **MISSING** (consumed by the dropped walk anim) |
| `OreSpawnGUIHandler` | IGuiHandler IDs 0=furnace, 1=workbench (`OreSpawnGUIHandler.java:18-44`) | `ModMenuTypes.java` + screen registration `OreSpawnClient.java:335-339` (`RegisterMenuScreensEvent`) | **PORTED** (modern equivalent) |
| Crystal Workbench GUI/container | 3×3 crafting, result at (124,35), grid at (30,17), drops grid on close, block+distance `stillValid` (`ContainerCrystalWorkbench.java:28-68`; `CrystalWorkbenchGUI.java`) | `gui/CrystalWorkbenchMenu.java:45-72` identical slot map; recipe re-eval `:79-104`; close-drop `:165-168`; `stillValid` vs `ModBlocks.CRYSTAL_WORKBENCH` `:107-109`; screen `CrystalWorkbenchScreen.java` | **PORTED** (verified) |
| Crystal Furnace | Cook = **150 ticks** (`TileEntityCrystalFurnace.java:174-179`), custom fuel table: lava/CrystalCoal **20000**, CrystalTreeLog 800, CrystalPlanks 400, etc. (`:226-277`) | `gui/CrystalFurnaceBlockEntity.java:34` cook = **100 ticks**; fuel via vanilla `fuel.getBurnTime(RecipeType.SMELTING)` (`:183`) — **no burn-time registration anywhere in the port** for Crystal Coal / Crystal Log / Crystal Planks (grep `getBurnTime|FurnaceFuel` = only this file), so the signature crystal fuels are inert | **PARTIAL/DIVERGENT** (speed 1.5× original spec; custom fuels missing) |

---

## Event & tick handlers

The original's entire runtime event surface is: `KeyHandler` + `RiderControl` (client tick) + `GirlfriendOverlayGui` (overlay) registered in `ClientProxyOreSpawn.java:385` / `CommonProxyOreSpawn.java:30`; there are **no** FML server/player tick handlers — "tick mechanics" live in entity `onUpdate`s and in **install-time date checks**.

| Mechanic | Original | Port | Status |
|---|---|---|---|
| Holiday gates | `GregorianCalendar` at init (`OreSpawnMain.java:4518-4521`): Oct 31 → Ghost/GhostSkelly biome spawns (`:4521-4566`); Feb 14 → `valentines_day=1` (`:4567-4569`) consumed by `MyValentineTarget` AI (Girlfriend targets players/Boyfriends to kiss — `Girlfriend.java:161-162`, `MyValentineTarget.java:47-50`); Apr 20 → `easter_day=1` gating EasterBunny spawns (`:4681`) | No date logic anywhere (grep `Calendar|Valentine|easter` = 0 in port). Ghost/GhostSkelly/EasterBunny have plain config-gated spawns (`ModSpawnControl.java:57-58, 89`) | **MISSING** (all three seasonal mechanics) |
| Kraken revenge spawn | `AttackSquid.func_70097_a` (`AttackSquid.java:392-397`): on player kill, outside Dim5, `nextInt(15)==1`, `KrakenEnable!=0`, `wasshot==0` → 1-3 Krakens at y=170 ±4 | `KrakenRevengeHandler.java:38-65` via `LivingDeathEvent`: same dims/odds (==0 vs ==1, same 1/15), same y=170/±4/1-3 count. Differences: `wasshot` gate dropped; gate is `ALL_MOBS_DISABLE` — no `KRAKEN_ENABLE` exists in port config (grep = 0) | **PORTED** (minor gate divergences) |
| Lava-proof drops | Fire-immune mobs spawned `EntityLavaLovingItem` drops inline per-entity (`EntityLavaLovingItem.java` original) | Centralized `ModLavaDropHandler.java:24-45` (`LivingDropsEvent`, namespace+`fireImmune()` filtered) swapping drops for `EntityLavaLovingItem` | **PORTED** (mechanism changed, behavior equivalent) |
| One-Mobzilla flag | JVM-global `OreSpawnMain.godzilla_has_spawned` (`OreSpawnMain.java:381, 6549`) | `MobzillaSpawnTracker.java:22-70` — `SavedData` on overworld, persisted; consumed by `Godzilla.checkSpawnRules` | **PORTED** (improved: persistent per-save) |
| Per-mob spawn disable | ~100 `XxxEnable` config flags gate `EntityRegistry.addSpawn` (grep `Enable = config.get` = 100, e.g. `OreSpawnMain.java:1519`) | `ModSpawnControl.java:53-101` maps **42** entity types; cancellation via `FinalizeSpawnEvent`+`EntityJoinLevelEvent` (`:109-135`). Bosses/water mobs (Kraken etc.) unmapped | **PARTIAL** (≈42/100 flags enforced) |
| Repellent blocks | `CreeperRepellent.findSomethingToRepell` repels Creeper + EntityAnt + **PurplePower** (`CreeperRepellent.java:94-126`); Kraken version repels Kraken + EntityAnt (`KrakenRepellent.java:93-109`) | `block/RepellentBlock.java:26-47` predicate blocks; `ModBlocks.java:131-136`: kraken = Kraken‖EntityAnt ✓, creeper = Creeper‖EntityAnt — **PurplePower omitted** | **PARTIAL** (creeper variant missing one target) |
| UltimateBow | Auto-enchant + no-arrow fire + 1/4 crit roll (`UltimateBow` orig; config `OreSpawnMain.java:1519, 1526-1530`) | `item/UltimateBow.java:24-58` — auto Power(config)/Flame/Punch/Infinity, spawns `UltimateArrow` without ammo, crit `pull>=1‖nextInt(4)==0` (`:51`) | **PORTED** |
| ExperienceCatcher | Catches **one** orb (value ≥3, 80% chance) → drops Bottle o' Enchanting + string + stick, item consumed (`ExperienceCatcher.java:29-62`) | `item/ExperienceCatcher.java:24-61` — vacuums **all** orbs in r=3 → pays out emeralds/gold/diamonds by XP total | **DIVERGENT** (entirely different conversion) |
| `GenericTargetSorter` | Distance comparator (`GenericTargetSorter.java:14-37`) | Inline `Comparator<Entity>` per entity (e.g. `Cephadrome.java:60`) + `EntityCannonFodder.java` named copy | **PORTED** |
| `MyUtils` | `isRoyalty/isAttackableNonMob/isIgnoreable` (`MyUtils.java:46-117`) | `util/MyUtils.java:9-55` — all three + `isAlly`/`isBigBoss` additions | **PORTED** |
| `OreSpawnTeleporter` | Custom placement for 5 dims (`OreSpawnTeleporter.java:22-96`) | Only `block/UtopiaPortalBlock.java:23-50` (entityInside → utopia/back). No teleporter/portal for mining/crystal/chaos/village/islands found in this slice | **PARTIAL** (1 of 5+ destinations; rest may belong to dimension slice but no code exists) |

---

## Networking

| Channel/message | Original | Port | Status |
|---|---|---|---|
| Rider control C2S | `RiderControlMessage` (1 int keystate), registered `CommonProxyOreSpawn.java:30`, handler writes global static (`RiderControlMessageHandler.java:21-27`) | `RiderInputPayload` (3 bools), registered `network/ModNetwork.java:12-19` (`playToServer`, versioned "1.0"), per-player vehicle dispatch (`RiderInputPayload.java:31-51`) | **PORTED** (richer payload; consumer coverage gap noted in rider section) |
| Other networking | none in 1.7.10 mod | Vendored MHLib registers 6 payloads (`de/dertoaster/multihitboxlib/init/MHLibNetwork.java:27-40`: bone info, multipart sync, functional anim progress, etc.) — new infrastructure for multipart bosses, no 1.7.10 counterpart | **N/A** (addition) |

GeckoLib's own `triggerAnim` network sync (used by TheQueen's Actions controller) is library-internal; no extra registration needed — verified the trigger names match registered triggerable anims exactly (see TheQueen table).

---

## Summary & counts

Rows audited: 38. **PORTED 16 · PARTIAL 10 · DIVERGENT 9 · MISSING 3 · UNVERIFIED 0** (plus 2 N/A additions). One systemic DIVERGENT finding spans ~39 model files.

Top findings (severity order):

1. **Systemic `wingspeed`→`limbSwingAmount` frequency mistranslation in ~39 port models** — idle/flying animations freeze and jitter (e.g. `ButterflyModel.java:96`, `BeeModel.java:188`, `ModelUrchin.java:138` vs originals' constant `wingspeed`).
2. **SpiderRobot renders 1 of 8 legs** — pose loop without per-iteration render (`ModelSpiderRobot.java:262-352` vs original `:392-410`).
3. **GiantRobot lost walk + attack animations and duplicate-part limbs** (`ModelGiantRobot.java:150-161` vs original `:150-279`); `RenderGiantRobotInfo` MISSING.
4. **Rider flight controls missing for 6 of 7 original mounts** — only Dragon implements `RideableFlyer` (`RiderInputPayload.java:37`); Cephadrome/Ostrich/Leonopteryx/Elevator/PrinceTeen/PrinceAdult unridable-in-air.
5. **Pointed-at-mob health-bar HUD reduced to a Girlfriend-only list** (`GirlfriendOverlay.java:39-43` vs `GirlfriendOverlayGui.java:105-428`, ~45 entity types).
6. **Crystal Furnace custom fuels dead** — no burn times for Crystal Coal (20000)/Log(800)/Planks(400) (`CrystalFurnaceBlockEntity.java:183` vs `TileEntityCrystalFurnace.java:267-275`); cook 100t vs 150t.
7. **All seasonal content missing** — Halloween ghost spawns, Valentine's Girlfriend AI, Easter Bunny gate (`OreSpawnMain.java:4518-4572` → nothing in port).
8. **Rotator's 24-blade tri-axis spin reduced to 3 flat Z-spinning blades** (`RotatorModel.java:33-38` vs `ModelRotator.java:44-80`).
9. **Mothra flaps at 6.5× intended speed and renders half size** — lost wingspeed 0.2 and scale 10→5 (`MothraRenderer.java:24`, `ButterflyModel.java:96` vs `ClientProxyOreSpawn.java:411`).
10. **Per-mob spawn-disable enforcement covers ~42 of ~100 original config flags** (`ModSpawnControl.java:53-101`); KrakenEnable absent entirely.
