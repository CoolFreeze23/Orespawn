# Phase C slice 8 — Animations & GUI (ANIM- series)

Scope: every `ANIM-` finding with no prior resolution — 6 DIVERGENT
(002, 003, 005, 006, 011, 017), 9 PARTIAL (007, 008, 009, 010, 013, 015,
018, 019, 020), plus 2 MISSING findings skipped per ground rules
(ANIM-014, ANIM-016 — Phase D scope, no resolution line). ANIM-001
(wingspeed systemic) and ANIM-004 (Mothra scale) were closed in Phase B
slice B4 and were not reopened.

Outcome: **11 FIXED** (002, 003, 005, 007, 008, 009, 010, 011, 013, 017,
018), **3 VERIFIED-CORRECT** (015, 019, 020 — all stale audit claims, see
audit-errors section), **1 PARTIAL** (006 — leg rendering fixed; gait
solver remainder named below), **2 skipped MISSING** (014, 016).

Every original value below was re-verified in the 1.7.10 CFR source before
the fix. The 1.21.1 "render once via root" constraint was handled per the
slice brief: shared-part multi-pass rendering is reproduced by posing and
re-rendering the same `ModelPart`s inside `renderToBuffer` (Rotator fans,
GiantRobot limbs, SpiderRobot/AntRobot legs), with pose values computed in
`setupAnim` and carried in model fields — no per-frame heap allocations.

## A note on the original's `RenderInfo`

Several findings hinge on the original's generic `RenderInfo` POJO (orig
`RenderInfo.java:6-15` — four ints `ri1..ri4`, four floats `rf1..rf4`).
The audit described it as "server-synced"; it is not. Each entity
constructs one instance (e.g. orig `Kraken.java:58`
`renderdata = new RenderInfo()`), exposes it via `getRenderInfo()`, and
the *model* mutates it client-side during render (e.g. orig
`ModelKraken.java:1045-1057`, orig `ModelRotator.java:75-78`). It is a
per-entity client scratchpad whose entire purpose is that two entities of
the same type animate independently instead of sharing the model
singleton's fields. The port now has the same class
(`entity/client/RenderInfo.java`, new) attached to Kraken, Rotator,
Robot2 and Robot3.

## Fixed findings

### Model animations (ANIM-002, 003, 005, 006, 007, 008, 009, 010)

| ID | Original (citation → value) | Old port | New port | Notes |
|----|------------------------------|----------|----------|-------|
| ANIM-002 | Kraken mouth/fin twitch keyed on per-entity `ri1`/`ri2`, re-rolled at the cosine zero crossing — idle `nextInt(10)`/`nextInt(15)`, attacking `nextInt(4)`/`nextInt(3)`; twitch only when `ri1∈{1,3}` (orig `ModelKraken.java:1045-1058`; scratch at orig `Kraken.java:58,123-125`) | `ri1`/`ri2` were fields on the model singleton — every rendered Kraken shared one twitch state | `Kraken` carries a `RenderInfo`; `ModelKraken.setupAnim` reads/re-rolls `entity.getRenderInfo().ri1/ri2` with the original dice and gates | **Audit error** — "server-synced" claim wrong, see below. Visual logic itself was already ported; only the state's residence was wrong. |
| ANIM-003 | Each of 3 blade boxes rendered 8× in a 45° fan (step 0.7853982 rad, orig `ModelRotator.java:56`); the three fans spin about X, Y, Z respectively by accumulating per-entity `rf1 += 2°`/frame, wrap at 359° (orig `:44-80`, accumulate `:75-78`) | each shape rendered once, all three spun about Z only at 1×/1.5×/2× | `RotatorModel.renderToBuffer` renders each blade 8× at 45° steps inside an X/Y/Z-rotated pose (`Axis.XP/YP/ZP`), angle from `EntityRotator`'s `RenderInfo.rf1`, advanced +2° per rendered frame | The signature 24-blade gyroscope ball is back. |
| ANIM-005 | Walk cycle: `movescale = limbSwingAmount*0.65` clamp 1; hip sway `cos/sin(-t*0.25)*π*0.1`; 2-phase thigh/shin angles; hip bob ±4px at 2× frequency; arms = thigh swing idle, windmill punch `sin(t*0.5)*π/5 ±` offsets + shoulder twist when `getAttacking()!=0`; torso counter-twist /2; each shared leg/arm part drawn TWICE at mirrored hip/shoulder offsets (orig `ModelGiantRobot.java:150-279`, attack `:230-240`, wingspeed 0.25 from orig `ClientProxyOreSpawn.java:516`) | head look + tiny idle arm sway only; every shared part drawn once → second leg/arm invisible | full pose math in `setupAnim` (fields, no allocations); `renderToBuffer` replays the original order: hip → leg(+1) → leg(−1) → arm(+1) → arm(−1) → torso/head via `renderLeg`/`renderArm` helpers | Covers ANIM-014's substance: orig pose values are recomputed every frame from the anim args; `RenderGiantRobotInfo` carries no cross-frame state for this model, so no holder class is needed. |
| ANIM-006 | 8 legs posed AND rendered inside the loop (orig `ModelSpiderRobot.java:302-411`, renders `:392-410`); jaw snap `:412-427` | pose loop kept but rendered once after it → only leg i=7 visible | `renderToBuffer` loops i=0..7: `poseLeg(r, i)` then renders the shared leg parts; jaw logic moved to `setupAnim` | **PARTIAL** — same bug found and fixed in `ModelAntRobot` (6 legs, same pattern). Remainder: port gait data is a canned sine (`SpiderRobot.java:221-237`) vs the orig `RenderSpiderRobotInfo` leg solver → Phase D (entity-AI owner). |
| ANIM-007 | Legs swing on time at 0.3 (wingspeed 1.0, orig `ClientProxyOreSpawn.java:440`), amplitude × limbSwingAmount, frozen <0.1; at each `sin(toRadians(t*20))` zero crossing `ri1` re-rolled — 0 idle, 1..3 attacking; right arm windmills at `toRadians(t*20)` when `ri1∈{1,3}`, left when `ri1∈{2,3}`, else rest 0 (orig `ModelRobot2.java:133-170`) | legs on `limbSwing`; arms windmilled constantly | per-entity `RenderInfo.ri1` on `Robot2`; zero-crossing re-roll with `getAttacking()` gate; per-arm windmill selection; legs time-driven | |
| ANIM-008 | Legs at 0.55 (wingspeed 1.0, orig `ClientProxyOreSpawn.java:441`); at each cosine zero crossing `ri1 = getAttacking()!=0 ? 1 : 0`; arm swing `cos(t)*π*0.15` zeroed when `ri1==0`, upper arms −1 rad / forearms +1 rad offsets (orig `ModelRobot3.java:163-186`) | no `getAttacking()` use; arms always swung | per-entity `RenderInfo.ri1` on `Robot3`; latch-at-zero-crossing + idle zeroing; offsets kept | |
| ANIM-009 | Legs at 0.5 (wingspeed 1.0, orig `ClientProxyOreSpawn.java:442`), shins NO rest offset, calves +0.175, knee guards +0.63, thighs −0.175; shield arm pumps `|cos(toRadians(t%360)*6)|*0.7853982 + 0.75` ONLY while attacking, rest 0; `setShielding(1)` while angle > amp/3 (client-local write as in orig); cannon arm aims 0.85 rad only while attacking; cannon assembly follows the upper arm: pivot = shoulder + (cos,sin)(arm xRot)·14 (orig `ModelRobot4.java:421-500`) | right arm on a fixed always-on cycle; cannon frozen; shins carried a spurious +0.175 | `getAttacking()` gates both arms; shin offsets corrected; cannon group repositioned per-frame from the arm pivot | |
| ANIM-010 | Legs scurry at 1.7 (wingspeed 1.0, orig `ClientProxyOreSpawn.java:482`) with per-leg phase signs (+,−,−,+); TAIL thrashes `cos(t*1.5)*π*0.25` while `getAttacking()!=0`, gentle `cos(t*0.4)*π*0.05` idle; `tail1.yRot = a*0.5`, `tail2.yRot = a*1.25`, tail2 follows tail1's tip (cos/sin·9) (orig `ModelRat.java:111-120`) | walk + an invented head yaw; no attack branch | leg signs corrected; attack/idle tail branch restored; invented head yaw removed | **Audit error** — the dropped pose is the tail, not a "head bob"; orig `ModelRat.java:116-120` only touches `tail1`/`tail2`. |

### Input & HUD (ANIM-011, 013)

| ID | Original (citation → value) | Old port | New port | Notes |
|----|------------------------------|----------|----------|-------|
| ANIM-011 | One key "OreSpawn UP/FAST", LWJGL keycode 56 = **Left Alt** (orig `KeyHandler.java:15-18`) | fly_up default SPACE | fly_up default `GLFW_KEY_LEFT_ALT`; SPACE rejected (collides with vanilla mount-jump/dismount); fly_down (LCTRL) and special (G) documented in the class Javadoc as port-only additions with no original counterpart | |
| ANIM-013 | Universal pointed-at-mob health bar: vanilla pick entity, fallback 16-block entity ray trace requiring a living target (orig `GirlfriendOverlayGui.java:105-114`); ~45 eligible types (`:115-428`) with ownership gates (Girlfriend/Boyfriend/Princes/Princess `isOwnedBy`), activity gates (Princes teen/adult, Dragon, Cephadrome `getActivity()==0`), special cases (Worm only when not phasing `:250-256`, BandP label by `getWhat()` `:405-409`, Crab only >0.75 scale `:425-428`, custom-name-or-label); textured 182×5 bar from `girlfriendgui.png` (bg v=0, fill v=5, fill width = fraction·183, `:441-446`) centered at y=25 (y=15 eye-in-water or armored, `:435-439`), name 0xFF3434 10px above (`:440`); config gate `GuiOverlayEnable` (`:102`) | top-left list of owned Girlfriends within 16 blocks, flat-color bars, no crosshair targeting | `client/GirlfriendOverlay.java` rewritten: `Minecraft.crosshairPickEntity` + `ProjectileUtil.getEntityHitResult` 16-block fallback; full eligibility chain with all gates; `girlfriendgui.png` **copied from the original assets** into `assets/orespawn/textures/gui/` (256×256 verified); coordinates/sizes per the orig citations; `GUI_OVERLAY_ENABLE` gate kept | Orig also hid the bar while the Girlfriend rode the player's shoulders (no port shoulder-riding equivalent) — omitted, noted in the class Javadoc. The orig's unrelated side writes (`current_dimension`, `FastGraphicsLeaves`, orig `:100-101`) belong to other systems and were not replicated. |

### Items, blocks & systems (ANIM-017, 018)

| ID | Original (citation → value) | Old port | New port | Notes |
|----|------------------------------|----------|----------|-------|
| ANIM-017 | Click a block → scan a 1×2×1 column at the click point (orig `ExperienceCatcher.java:33`); first orb with value ≥3 passing the 80% roll (`nextInt(5)==1` is the miss, `:37`) is discarded → drop Bottle o' Enchanting + string + stick at click point (`:38-50`); catcher consumed unless creative (`:51-53`); on no catch, the catcher itself is dropped and one always removed (`:56-62`); swing on use (`:30`) | vacuumed ALL orbs in r=3 and paid out emeralds/gold/diamonds by XP total (invented) | original semantics restored verbatim in `item/ExperienceCatcher.java` | |
| ANIM-018 | ~100 `XxxEnable` flags gate `EntityRegistry.addSpawn` (orig `OreSpawnMain.java:6364-6465`); all default 1 except `BoyfriendEnable` default 0 (`:6430`); `CowEnable` gates RedCow/GoldCow/EnchantedCow natural spawns (`:4609-4624`); `WormEnable` gates WormLarge only (`:4630-4634`); `KrakenEnable` `:6426` | 42 types mapped in `ModSpawnControl`; `KRAKEN_ENABLE` absent from config entirely | **56 config flags added** (`OreSpawnConfig`, orig key spellings kept: `nightmareEnable` for PitchBlack `:6462`, `criminalEnable` for BandP `:6388`; `boyfriendEnable` default **false** per orig) and **~65 map entries wired** (`ModSpawnControl`): bosses (Godzilla/TheKing/TheQueen/Kraken), water mobs (SeaMonster/SeaViper/AttackSquid/Whale/Flounder/Skate/GoldFish/CloudShark/Irukandji), Robots 1-5, Rotator/Vortex/DungeonBeast, ambients (Butterfly/LunaMoth/Tshirt/Coin/Firefly/Fairy/Bee), all 6 cow variants under `cowEnable`, Girlfriend/Boyfriend, Lizard/RubberDucky/Beaver, CreepingHorror/TerribleTerror/CliffRacer/Triffid/LeafMonster/LurkingTerror/PitchBlack, EnderKnight/EnderReaper, EmperorScorpion/Scorpion/Mantis/Stinky/HerculesBeetle/Rat/TrooperBug/SpitBug/StinkBug/CaveFisher/Alien/WaterDragon/WormLarge/Crab/BandP, plus the pre-existing-but-unmapped `ROCK_ENABLE`→RockBase, `THE_KING/QUEEN_ENABLE`→TheKing/TheQueen. `KrakenRevengeHandler` now checks `KRAKEN_ENABLE` before the AttackSquid revenge spawn | Cancellation mechanism unchanged (FinalizeSpawnEvent + EntityJoinLevelEvent on NATURAL/CHUNK_GENERATION) — equivalent to the orig's addSpawn gating for natural spawns. |

## Verified-correct findings (stale audit claims)

| ID | Audit claim | Reality (proof) |
|----|-------------|-----------------|
| ANIM-015 | Crystal Furnace cooks in 100 ticks; crystal fuels inert | Duplicate of **ITEM-016**, fixed in Phase C slice 6: cook 150 ticks and the orig fuel table (lava/CrystalCoal 20000, CrystalTreeLog 800, CrystalPlanks 400, orig `TileEntityCrystalFurnace.java:174-179, 226-277`) already live in `CrystalFurnaceBlockEntity`. Slice 8 found and fixed one *unflagged* remainder while verifying: the orig leaves the fuel's container item behind when the stack empties (lava bucket → empty bucket, orig `:165-170`) — `getCraftingRemainingItem()` handling added. |
| ANIM-019 | Creeper-repellent predicate misses PurplePower | Stale: the ITEM-019 `RepellentBlock` rewrite already repels PurplePower in the CREEPER variant — including the orig quirk that a type-10 PurplePower aborts the whole scan (`return`, orig `CreeperRepellent.java:126-145`; port `block/RepellentBlock.java:109-114`). The `ModBlocks.java:131-136` lines the audit cites are now the crystal rat/fairy stones — the predicate design it describes no longer exists. |
| ANIM-020 | Only 1 of 5+ dimension destinations implemented | Stale + misread: orig `OreSpawnTeleporter` is only the *placement* helper; the travel triggers are the rideable insects — Ant→Utopia (orig `EntityAnt.java:95`), RedAnt→Mining (`EntityRedAnt.java:83`), RainbowAnt→Village (`EntityRainbowAnt.java:55`), UnstableAnt→Islands (`EntityUnstableAnt.java:55`), Termite→Crystal (`Termite.java:108`), Butterfly→Chaos (`EntityButterfly.java:276`). The port implements all six via `EntityAnt.mobInteract` + subclass `getTargetDimension()` overrides and `EntityButterfly`, with the safe-landing scan (`findSafeY`, mirroring orig `OreSpawnTeleporter.justPutMe`) and the tamed-pet 48×24×48 co-teleport (orig `:153-162`, ported under WGEN-049). All six dimension JSONs exist (`data/orespawn/dimension/`). |

## Audit errors found (with proof)

1. **ANIM-002** — claimed `RenderInfo.ri1` was "server-synced". Orig
   `RenderInfo.java:6-15` is a plain POJO; orig `Kraken.java:58` constructs
   it locally and no datawatcher ever touches it; orig
   `ModelKraken.java:1045-1057` mutates it from the render thread. The real
   divergence was per-entity vs model-singleton state, which is what was
   fixed. (Ground-rule 4 therefore does not require syncing here — the
   original itself was client-local.)
2. **ANIM-010** — described an "attack head bob". Orig
   `ModelRat.java:116-120` animates `tail1`/`tail2` exclusively; the head
   has no attack pose. Fixed as a tail thrash. The port's head yaw was
   itself an invention and was removed.
3. **ANIM-015** — stale; duplicate of ITEM-016 (already fixed, slice 6).
4. **ANIM-019** — stale; PurplePower repel already present from ITEM-019,
   including the type-10 scan-abort quirk.
5. **ANIM-020** — stale and misattributed; all six teleport destinations
   exist (see table above).
6. **ANIM-014 (skipped MISSING, note only)** — the Fix guidance ("recreate
   `RenderGiantRobotInfo`, feed it from GiantRobot tick") is unnecessary:
   orig `ModelGiantRobot.java:154-167` recomputes every pose value from the
   frame's animation arguments; the info object holds no cross-frame state.
   The ANIM-005 fix reproduces the geometry without it.

## PARTIAL / deferred (Phase D owners)

- **ANIM-006** — all 8 SpiderRobot legs (and all 6 AntRobot legs) now
  render; the simplified canned-sine gait in `SpiderRobot.java:221-237` vs
  the orig `RenderSpiderRobotInfo` leg solver remains → **Phase D,
  entity-AI owner**.
- **ANIM-012** (prior PARTIAL, untouched this slice) — Elevator still on
  the generic rider fallback → Phase D backlog, per B3.

## Skipped MISSING (Phase D, no resolution line)

- **ANIM-014** — GiantRobot `RenderGiantRobotInfo` holder. Substantively
  obsoleted by the ANIM-005 fix (see audit-errors note 6); left MISSING for
  the Phase D owner to confirm-and-close.
- **ANIM-016** — seasonal (Halloween/Valentine's/Easter) gates.

## Files changed

| File | Change |
|------|--------|
| `entity/client/RenderInfo.java` | **new** — per-entity render scratch (orig `RenderInfo.java:6-15`) |
| `entity/Kraken.java`, `entity/EntityRotator.java`, `entity/Robot2.java`, `entity/Robot3.java` | `RenderInfo` field + `getRenderInfo()` accessor |
| `entity/client/ModelKraken.java` | twitch state moved to per-entity `RenderInfo` (ANIM-002) |
| `entity/client/RotatorModel.java` | 24-blade tri-axis gyroscope (ANIM-003) |
| `entity/client/ModelGiantRobot.java` | walk cycle + attack windmill + two-pass limbs (ANIM-005) |
| `entity/client/ModelSpiderRobot.java`, `entity/client/ModelAntRobot.java` | pose-and-render per leg inside the loop (ANIM-006) |
| `entity/client/ModelRobot2.java`, `ModelRobot3.java`, `ModelRobot4.java` | attack-gated arm animations (ANIM-007/008/009) |
| `entity/client/RatModel.java` | attack/idle tail thrash, leg signs (ANIM-010) |
| `client/KeybindHandler.java` | fly_up default Left Alt (ANIM-011) |
| `client/GirlfriendOverlay.java` | crosshair-target HUD rewrite (ANIM-013) |
| `src/main/resources/assets/orespawn/textures/gui/girlfriendgui.png` | **new** — copied from original assets (ANIM-013) |
| `gui/CrystalFurnaceBlockEntity.java` | fuel container-item remainder (ANIM-015 verification side-find) |
| `item/ExperienceCatcher.java` | original catch semantics (ANIM-017) |
| `OreSpawnConfig.java` | 56 per-mob enable flags added (ANIM-018) |
| `ModSpawnControl.java` | ~65 map entries added (ANIM-018) |
| `KrakenRevengeHandler.java` | `KRAKEN_ENABLE` gate (ANIM-018) |

## Build status

`.\gradlew.bat build --console=plain` → **BUILD SUCCESSFUL** (warnings
only: pre-existing deprecation notes).
