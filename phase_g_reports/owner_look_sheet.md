# Owner look sheet — one in-game session (2026-09-03)

Seven items wait on the owner's eyes. Ordered so JVM arguments change once and the config flips once:
five launches, grouped by instance and launch setting. Every expectation cites where it comes from.

## Preconditions (verified from the repo and the Prism folders)

- **Instances** (`C:\Users\alvin\AppData\Roaming\PrismLauncher\instances\`, `instgroups.json` has no groups):
  - **Dev instance = `CrazyCraft 5.0`** (NeoForge 21.1.248, 201 mods, GeckoLib 4.9.2). `minecraft/mods/orespawn-1.21.1-2.0.0-beta.4.jar`
    (2026-09-02 18:08) is the dev build the owner last ran; `orespawn-1.21.1-2.0.0-beta.4.RELEASE-BACKUP.jar` (2026-08-21) sits
    in the instance root (the "release jar in the instance root", FIX_LOG.md:4578). `instance.cfg` carries
    `OverrideJavaArgs=true`, `JvmArgs="-Dorespawn.dev.beaverRenderer=candidate"`.
  - **Clean instance = `Distant Horizons & Iris Shaders`** (29 mods, none of the pose mods; AUDIT_FINDINGS.md:5942 names it
    "the clean test bed"). Its `minecraft/mods/` still holds `orespawn-1.21.1-1.0.0-beta.3.jar` (2026-08-11) — NOT the current build.
    No custom JVM args (`OverrideJavaArgs=false`).
  - **No 1.7.10 instance exists** (seven instances: five NeoForge 1.21.1, one Forge 1.20.1, one bare 1.21.1). AUDIT_FINDINGS.md:6133
    records the same ("no 1.7.10 jar exists under Prism"). Fallback for every "previous build" comparison: the beta.3 / release-backup jars.
- **DONE 2026-09-03 (orchestrator): the current build is staged.** `CrazyCraft 5.0\minecraft\mods\orespawn-1.21.1-2.0.0-beta.4.jar`
  is now the jar from commit f3d5f51's gated tree (sha1 9cfc05bdecad8328fd3650871f8e44e194a48333; the stale 2026-09-02 jar was moved to the instance root as
  `orespawn-1.21.1-2.0.0-beta.4.stale-20260902.jar`). For Section B the same jar sits at
  `Distant Horizons & Iris Shaders\orespawn-1.21.1-2.0.0-beta.4.for-section-B.jar` (instance root, NOT in mods): after Section A,
  move it into `minecraft\mods\` and take `orespawn-1.21.1-1.0.0-beta.3.jar` out. The paragraph below is the pre-staging state, kept for the record.
- **(historical) The deployed dev jar predated the EnderReaper drop.** Its `ModelEnderReaper.class` still compiles 66 `CubeListBuilder.mirror`
  calls (javap over the jar's class; the class is byte-for-byte the 2026-08-11 12:01 one that the release-backup and beta.3 jars carry,
  12779 B), while the build tree's class (`build/classes/.../ModelEnderReaper.class`, 2026-09-02 23:17) has 0. **Deploy the
  post-Queen-restore build to BOTH instances before the session** (one `orespawn-*.jar` per `mods/` folder; remove the old one).
  `gradlew deployToPrism` cannot do it: `gradle.properties:34` points at an instance named `ORESPAWN TEST`, which does not exist — copy by hand.
- **Dev switch grammar** (`src/main/java/danger/orespawn/client/DevRendererSwitch.java:16-40`): property `orespawn.dev.geckolibRenderers`;
  value `candidate` = EVERY GeckoLib candidate; or a comma list of species ids. `orespawn.dev.beaverRenderer` is an alias with the same
  grammar — so the owner's current `-Dorespawn.dev.beaverRenderer=candidate` turns on all twelve candidates (beaver, elevator, vortex, coin,
  island, island_too, robot_1..robot_5, rock_base — `PhaseGDevRenderers.java`, wired in `OreSpawnClient.java:61-65,93,100,104,108-109,161,166`).
  Sections C and D need the argument REMOVED (Prism: Edit instance → Settings → Java → "Java arguments"); Section E sets the species list.
  Classic renderers are the default with no property; a selected candidate logs a `Phase G dev switch:` warning at startup.
- **PlayNicely** is `playNicely = false` under `[tweaks]` in `<instance>\minecraft\config\orespawn-common.toml` (CrazyCraft copy: `[tweaks]`
  line 109, key line 134; `OreSpawnConfig.java:273,294`, registered as a COMMON config at `OreSpawnMod.java:86`). Edit with the game closed;
  summon fresh mobs after the flip (The Queen snapshots it in her constructor, `TheQueen.java:210-213`, and re-syncs the render flag every
  10 ticks, `:774-776`).
- **World:** a creative superflat with the TESTING_CHECKLIST.md "Session setup" commands (`/gamemode creative`, `/time set day`,
  `/gamerule doDaylightCycle false`, `/weather clear`). Bosses need a big open area: The Queen is ~18 blocks tall with a ~50-block wingspan
  at the restored scale (`phase_g_reports/ents092_queen_extent.md`). `/kill @e[type=!minecraft:player]` between rows.
- **Screenshots:** F2 in Section A at fixed camera positions; the same camera in B, C and E is the side-by-side (a single instance cannot
  run two renderers or two jars at once).

## Launch order

| # | Instance | Jar in `mods/` | JVM argument | `playNicely` | Covers |
|---|---|---|---|---|---|
| A | Distant Horizons & Iris Shaders | as found: `orespawn-1.21.1-1.0.0-beta.3.jar` | none | false | baselines: EnderReaper (item 1), old sizes for the recheck rows (item 2) |
| B | Distant Horizons & Iris Shaders | current build (beta.3 removed) | none | false | BUG-039 repro (item 7), EnderReaper new build (item 1) |
| C | CrazyCraft 5.0 | current build | none (REMOVE `-Dorespawn.dev.beaverRenderer=candidate`) | false | recheck rows (2), Queen normal F3+B (3), Elevator classic post-094 (6), Robot5 and Beaver classic references (4, 5) |
| D | CrazyCraft 5.0 | current build | none | **true** | Queen PlayNicely F3+B (3); optional nice-mode rows |
| E | CrazyCraft 5.0 | current build | `-Dorespawn.dev.geckolibRenderers=beaver,elevator,robot_5` | false | Beaver candidate (5), Elevator candidate (6), Robot5 candidate wheels (4) |

The EnderReaper A/B runs in the clean instance because its two launches are needed anyway (no extra jar juggling); FIX_LOG.md:4578's
designated baseline (the RELEASE-BACKUP jar in the CrazyCraft root) carries the identical `ModelEnderReaper.class` and is the
equivalent route at the cost of one more launch.

---

## Section A — clean instance, jar as found (beta.3), no JVM argument: baselines

| Item | Instance | JVM argument | How to spawn or reach it | Compare against | Pass looks like | Result |
|---|---|---|---|---|---|---|
| 1 (baseline) EnderReaper, mirrored build | Distant Horizons & Iris Shaders | none | `/summon orespawn:ender_reaper ~ ~ ~6`; F2 front-on and from the scythe side (scythe1-3 hang off the pivot at x = -17, the reaper's right; `ModelEnderReaper.java:393-403`). Note the head face (UV 58,145: black face with two magenta eye texels), the magenta blade quads (scythe2 UV 58,118; scythe3 UV 61,133) and the rib "Shape" boxes' glyph-like texels. | — (this IS the baseline; the beta.3 jar's `ModelEnderReaper.class` is byte-identical to the release-backup's) | Two screenshots saved at known camera spots. | |
| 2 (baseline) old port sizes | Distant Horizons & Iris Shaders | none | Same spots, one screenshot each with F3+B on: `/summon orespawn:brutalfly ~ ~5 ~`, `orespawn:irukandji`, `orespawn:fairy`, `orespawn:robot_3`, `orespawn:the_king ~ ~ ~40`, `orespawn:kraken` (over deep water). | — | Screenshots saved (the "old port scale" column of `ents092_recheck_list.md`). | |

Quit, replace `mods/orespawn-1.21.1-1.0.0-beta.3.jar` with the current build.

## Section B — clean instance, current build, no JVM argument

| Item | Instance | JVM argument | How to spawn or reach it | Compare against | Pass looks like | Result |
|---|---|---|---|---|---|---|
| 7 BUG-039 hoverboard rider posture | Distant Horizons & Iris Shaders | none | `/give @s orespawn:elevator`, right-click a block (board spawns 1.2 above it at a random yaw, `ItemElevator.java:24-45`) — or `/summon orespawn:elevator ~2 ~1 ~`. Right-click the board with an EMPTY hand to mount (`Elevator.java:580-599`; an Ultimate Sword within 4 blocks recolours it instead). F5 to third person; look at the rider's legs; turn the camera while the board lags behind. | The CrazyCraft observation (seated rider, FIX_LOG.md:4206-4214). Discriminator (AUDIT_FINDINGS.md:5943-5944): seated = legs bent forward, torso clamped to the board's facing; standing = legs straight onto the deck, torso turns freely while the board lags. Pose mods present ONLY in CrazyCraft (verified by listing its mods): NotEnoughAnimations 1.9.3, Player Animator 2.0.4, Serious Player Animations 1.2.0, Better Combat 2.4.0, SittingPlus 1.0.1. | Rider STANDS on the deck → close BUG-039 as external with a compatibility note naming those mods; rider SITS here too → reopen as a port bug (`Elevator.shouldRiderSit()` returns false, `Elevator.java:155-158`). | |
| 1 (A/B) EnderReaper, un-mirrored build (BUG-041 stage 1) | Distant Horizons & Iris Shaders | none | `/summon orespawn:ender_reaper ~ ~ ~6`; the two Section-A camera spots; F2. | The Section-A screenshots. Expectation (AUDIT_FINDINGS.md:6120-6147, :6148-6154; FIX_LOG.md:4574-4584, :4586-4596, law 11 from Mojang's 1.7.10 jar): the geometry is identical; on every face the asymmetric texels run the OTHER way horizontally (blade, head face, rib glyphs). Also expected, not a mirror effect: a smaller shadow disc, 0.5 → 0.2 (ENT-S-092 batch 2, `EnderReaperRenderer.java:24-27`, `renderer_findings.md:75`). Nothing else moves, resizes or disappears. | Same silhouette, pose and scale as the baseline with the texel orientation flipped on the asymmetric faces and no seam/hole/missing part — and the owner's "go": that word lands the port-wide drop on the other 81 models (AUDIT_FINDINGS.md:6155-6160). FAIL = any moved/missing geometry, or the owner rules the port keeps the flipped mapping (recorded per model as a MOD ruling). | |

Quit. Deploy the same jar into `CrazyCraft 5.0\minecraft\mods\` (remove the 2026-09-02 jar), clear the instance's Java arguments, keep `playNicely = false`.

## Section C — dev instance, current build, NO JVM argument, playNicely = false

Recheck rows first (item 2), then the Queen (3), the board (6), then the classic references for Robot5 (4) and Beaver (5).

| Item | Instance | JVM argument | How to spawn or reach it | Compare against | Pass looks like | Result |
|---|---|---|---|---|---|---|
| 2 Brutalfly (x9) | CrazyCraft 5.0 | none | `/summon orespawn:brutalfly ~ ~5 ~`, F3+B | Section-A shot; `ents092_recheck_list.md` row 1: scale 1 → 9, shadow 1.5 → 6.75 (batch 1a) | Nine times the baseline size with the shadow scaled to match; box wraps the body. | |
| 2 Irukandji (x0.25) | CrazyCraft 5.0 | none | `/summon orespawn:irukandji`, F3+B | Section-A shot; recheck row: 1 → 0.25, shadow 0.2 → 0.025; hitbox small again (ENT-S-095 batch 1, `ents092_changelog_note.md`) | A quarter of the baseline size, tiny box and shadow. | |
| 2 Kraken (x0.333) | CrazyCraft 5.0 | none | `/summon orespawn:kraken` over deep water, F3+B | Section-A shot; recheck row: 3 → 1, shadow 3 → 1 (batch 1b, FIX_LOG.md:4728-4740); box 4x15 | One third of the baseline size, plain 4x15 box around it. | |
| 2 SeaMonster (x0.333) | CrazyCraft 5.0 | none | `/summon orespawn:sea_monster` over deep water, F3+B | recheck row: 3 → 1, shadow 1.5 → 1 (batch 1b); box 5x5 → 1.25x2.5 (ENT-S-095 batch 1) | One third the previous size with the smaller box. | |
| 2 Fairy (x0.35) | CrazyCraft 5.0 | none | `/summon orespawn:fairy`, F3+B | Section-A shot; recheck row: 1 → 0.35, shadow 0.15 → 0.035 | About a third of the baseline size. | |
| 2 TheKing (x2.1) | CrazyCraft 5.0 | none | `/summon orespawn:the_king ~ ~ ~40`, F3+B | Section-A shot; recheck row: 1 → 2.1, shadow 5 → 3.99 (batch 1b: parts positioned by code offsets, AUDIT_FINDINGS.md:6308-6312) | Roughly twice the baseline size; part boxes still on the body, 22x24 envelope. | |
| 2 Robot3 (x0.5) | CrazyCraft 5.0 | none | `/summon orespawn:robot_3`, F3+B | Section-A shot; recheck row: 1 → 0.5, shadow 2 → 0.5 | Half the baseline size. | |
| 2 Cricket / Hydrolisc / Godzilla / Dragonfly / EmperorScorpion / ThePrinceTeen | CrazyCraft 5.0 | none | `/summon orespawn:cricket`, `orespawn:hydrolisc`, `orespawn:godzilla ~ ~ ~40`, `orespawn:dragonfly`, `orespawn:emperor_scorpion`, `orespawn:the_prince_teen`; F3+B | recheck rows: 1 → 0.5; 1 → 0.65; 3 → 2 (batch 1b); 1 → 1.5; 1 → 1.5; 0.85 → 1.25 | Each mob's size changed by its ratio column, model not lost inside nor bursting out of its box beyond the 1.7.10 look. | |
| 3 The Queen, normal scale (ENT-S-092 batch 1b, in flight: 2.0 with MHLib parts re-tuned) | CrazyCraft 5.0 | none | `/summon orespawn:the_queen ~ ~ ~40` in the open; F3+B; walk around her; hit a head once (melee or arrow). | `ents092_queen_held.md` ("Verification" paragraph) and `ents092_queen_extent.md`: drawn at 2.0 she is ~18 blocks tall at the root, ~50-block wingspan, shadow 3.8. The ten MHLib part boxes (`the_queen.json` synched-bones: Body1, LHead, LHead4, LHead12, Lwing1, Tail1, Tail4, Tail7, leftLeg, rightLeg) are client-fed from the bones, so only F3+B can verify them; failure mode of the wrong (pre-capture) order = boxes hanging in empty air 14-18 blocks inside the body while the visible heads are unhittable. Known, not a fail: the [3,8] leg boxes extend upward from the hip into the body; the QueenHead sidecar sits y+20 just above the drawn head; the 22x24 parent box is unchanged. | Each of the ten boxes sits on its drawn part — three head boxes on the three heads, Tail1/4/7 along the tail with Tail7 at the tip, Lwing1 at the left wing root, Body1 on the body, leg boxes at the hips — and a head hit registers. | |
| 6 Elevator, classic renderer, post-slice-B + ENT-S-094 re-acceptance | CrazyCraft 5.0 | none | Board as in item 7. (a) RIDE: mount, W/S throttle, steer by looking, Left Alt = fly-up/FAST (`KeybindHandler.java:36-40`); turn hard both ways. (b) NAME: `/summon orespawn:elevator ~2 ~1 ~ {CustomName:'"Board"',CustomNameVisible:1b}` and look at it. (c) HIT: dismount, `/gamemode survival`, punch it once or twice, then `/gamemode creative` (a creative punch destroys the board by design: `Elevator.java:125-146`, orig :184-186; 5 survival punches also destroy it). (d) optional death-branch probe, since in play the board is discarded rather than dying: `/data merge entity @e[type=orespawn:elevator,limit=1,sort=nearest] {Health:0f}` and watch it for the second before it vanishes. | The Q1 acceptance the owner gave and voided (FIX_LOG.md:4136-4137; AUDIT_FINDINGS.md:6170 "Elevator's Q1 acceptance void"); slice B re-expression (AUDIT_FINDINGS.md:6183-6195: pivots 0, lift cancelled in `ElevatorRenderer.scale()`, drawn exactly as before); ENT-S-094 contract (AUDIT_FINDINGS.md:6224-6256, FIX_LOG.md:4672-4690): entity yaw lerped as the 1.7.10 RenderManager passed it, boat-style hit wobble kept, NO hurt red tint, NO name tag, no death Z-flip. Accepted residuals, not fails: leash line on both paths, invisibility render type, engine shadow multipliers (owner acceptance AUDIT_FINDINGS.md:6254-6257). | Board sits on the ground exactly as the accepted look; deck pivots with the steering with no extra swing or snap; the named board shows no floating name; a survival punch wobbles it with no red flash; (d) it vanishes without tipping onto its side. | |
| 4 Robot5, classic renderer (reference for E) | CrazyCraft 5.0 | none | `/summon orespawn:robot_5 ~5 ~ ~5`; F2 from the s4 camera (yaw 34, pitch -28, `tools/s4_model_proofs.json` model_robot5) and side-on; let it roll (wheels turn only while moving: `ModelRobot5.java:103-113`). | — (reference screenshot for Section E) | Screenshots saved; note how much the flat wheel faces flicker. | |
| 5 Beaver, classic renderer, adult + baby, idle + walk (reference for E) | CrazyCraft 5.0 | none | `/summon orespawn:beaver ~3 ~ ~3` and `/summon orespawn:beaver ~3 ~ ~3 {Age:-24000}` (Beaver extends Animal, `Beaver.java:40`; breeding alternative: `/give @s orespawn:crystal_apple 8`, feed two adults — `Beaver.java:265-266`, ENT-A-039). WALK: step within 8 blocks — it flees (`AvoidEntityGoal` Player 8.0, `Beaver.java:71`; PanicGoal after a hit). IDLE: back off past 8 blocks and watch (it wanders rarely). F2 each state at a fixed spot. | — (reference; scale 0.75 adult / 0.375 baby, shadow 0.1125: `BeaverRenderer.java` SCALE/SHADOW, ENT-S-092 batch 1a, `ents092_changelog_note.md`) | Four screenshots saved (adult idle/walk, baby idle/walk). | |

Quit. Set `playNicely = true` in `CrazyCraft 5.0\minecraft\config\orespawn-common.toml` (`[tweaks]`, line 134). JVM arguments stay empty.

## Section D — dev instance, current build, NO JVM argument, playNicely = true

| Item | Instance | JVM argument | How to spawn or reach it | Compare against | Pass looks like | Result |
|---|---|---|---|---|---|---|
| 3 The Queen, PlayNicely scale | CrazyCraft 5.0 | none | `/summon orespawn:the_queen ~ ~ ~40` (fresh spawn after the flip); F3+B; hit a head. | `ents092_queen_held.md`: 1.7.10 nice scale = 2.0 / 4 = 0.5 (`RenderTheQueen.java:40-46`), so she draws at half the Section-C size (~9 blocks tall, ~25-block wingspan) and the ten part positions pull in with the bones; part SIZES do not change (profile values). Today's build (pre-restore) drew her at 0.25 with full-size part positions — that is the failure look. | The same ten boxes sit on the half-size drawn heads, tail, wing root, body and hips (nothing left out in the air at the full-size positions); a head hit registers. | |
| optional, same flip: King / Godzilla / Kraken nice modes (batch 1b, ENT-S-096) | CrazyCraft 5.0 | none | `/summon orespawn:the_king ~ ~ ~40`, `orespawn:godzilla ~ ~ ~40`, `orespawn:kraken` (deep water); F3+B | FIX_LOG.md:4728-4752, AUDIT_FINDINGS.md:6312-6320: King and Godzilla draw at scale/4 (2.1/4, 2/4); Kraken draws at a third with a 1.33x5 box | Each draws at the fraction stated, box as stated. | |

Quit. Set `playNicely = false` again. Set the instance's Java arguments to exactly:
`-Dorespawn.dev.geckolibRenderers=beaver,elevator,robot_5`
(replace the old `-Dorespawn.dev.beaverRenderer=candidate`; that token would switch on all twelve candidates).

## Section E — dev instance, current build, `-Dorespawn.dev.geckolibRenderers=beaver,elevator,robot_5`, playNicely = false

Startup log must show three `Phase G dev switch:` warnings (beaver, elevator, robot_5). None of the three candidates has a PlayNicely branch.
Ignore Hats Renewed hats on the candidate beaver — a known third-party check against the concrete GeckoLib class (FIX_LOG.md:3781-3800).

| Item | Instance | JVM argument | How to spawn or reach it | Compare against | Pass looks like | Result |
|---|---|---|---|---|---|---|
| 5 Beaver GeckoLib candidate — adult walk | CrazyCraft 5.0 | `-Dorespawn.dev.geckolibRenderers=beaver,elevator,robot_5` | `/summon orespawn:beaver ~3 ~ ~3`; approach within 8 blocks so it flees; same camera as Section C. | Section-C adult-walk shot. Owner's pending look: FIX_LOG.md:3984-3985 ("in-game look on Beaver"), :4136-4137 ("Beaver look still pending"). Contract: the classic `ModelBeaver.setupAnim` maths evaluated on the geo bones (FIX_LOG.md:3730-3736; g1 proof `phase_g_reports/g1_proof`), scale 0.75 and shadow matched to `BeaverRenderer` (`BeaverGeoReplacement.java:36-42`, batch 1a). Disclosed residual: GeckoLib lifts the model +0.01 blocks (FIX_LOG.md:3764-3766). | Same size, stride tempo and amplitude, texture and shadow as classic; no popping or missing part. | |
| 5 Beaver candidate — adult idle | CrazyCraft 5.0 | same | Back off past 8 blocks; same camera. | Section-C adult-idle shot | Indistinguishable from classic at rest (tail/breathing motion the same). | |
| 5 Beaver candidate — baby walk | CrazyCraft 5.0 | same | `/summon orespawn:beaver ~3 ~ ~3 {Age:-24000}`; approach so it flees. | Section-C baby-walk shot; baby scale 0.75 / 2 = 0.375 (`BeaverRenderer.scale`, `BeaverGeoReplacement.applyScale`) | Half-size beaver walking like the adult, shadow shrunk with it. | |
| 5 Beaver candidate — baby idle | CrazyCraft 5.0 | same | Back off; same camera. | Section-C baby-idle shot | Indistinguishable from the classic baby at rest. | |
| 6 Elevator GeckoLib candidate (species `elevator`) | CrazyCraft 5.0 | same | Repeat item 6 (a)-(c) on the candidate board. | The Section-C classic board. Contract: `ElevatorGeoReplacement.java:43-59` (hit wobble via `applyRotations`, then the 1.5 + 0.01 lift cancel) and `:77 nonLivingRender()` — entity-yaw draw, NO_OVERLAY, no name tag (AUDIT_FINDINGS.md:6244-6250). Re-acceptance requested by slice B (FIX_LOG.md:4648-4650). | Candidate board sits, steers, wobbles and hides its name exactly like the classic board, with no red flash. | |
| 4 Robot5 GeckoLib candidate — the wheels (s4 1.16% excluded pixels) | CrazyCraft 5.0 | same | `/summon orespawn:robot_5 ~5 ~ ~5`; same two cameras as Section C; let it roll. WHERE TO LOOK: each wheel is two identical 2x8x8 boxes at the same pivot, the second rotated 45° about X (`ModelRobot5.java:43-61`, `:111,:113`), so their flat inner and outer faces are coplanar and the overlap zone z-fights between the two textures (UV 0,23 grey-rimmed square vs UV 0,43) in BOTH renderers; the s4 diff capture paints exactly those rims blue (`phase_g_reports/s4_proof/evidence/visual/model_robot5/a1_t0.diff.png`). | The Section-C classic screenshots. Rule: `tools/s4_model_proofs.json` model_robot5 `max_contested_fraction_pin` 0.0116 (761/65536 pixels), `in_game_acceptance: "PENDING_OWNER: excluded fraction above 0.5% (ruling 2026-09-02)"`; FIX_LOG.md:4322-4324 (all 368 changed pixels were coplanar contests), :4388-4389 (rule 5: above 0.5% needs a specific in-game acceptance; Robot5 is the first), :4437-4443. Z-fight ORDER is not a parity target (ruling 2, FIX_LOG.md:4129-4133). | The candidate's wheels are the same eight-point wheels at the same size and place with the same rim texels; any flicker is confined to the flat wheel faces and is no worse than classic's — that sentence, quoted, becomes the manifest's `in_game_acceptance`. | |

---

## Facts not in the repo, and the fallback used

- No 1.7.10 instance or jar under Prism (AUDIT_FINDINGS.md:6133 and the instance listing) → the EnderReaper baseline is the previous
  build (beta.3 in the clean instance, or the RELEASE-BACKUP jar in the CrazyCraft root; identical `ModelEnderReaper.class`).
- The "1.7.10 look" of the un-mirrored faces can only be judged from the owner's memory of 1.7.10; the mapping itself is law-11-proven
  (FIX_LOG.md:4586-4596), so the row asks for geometry-unchanged + orientation-flipped, and the owner's go/no-go.
- `deployToPrism` targets a non-existent instance (`gradle.properties:34`) → hand-copy the jar.
- The Queen row's expected look (2.0 / 0.5 with parts on the drawn body) describes the in-flight change as specified in
  `ents092_queen_held.md`; if the landed edit differs, the row's numbers follow the landed commit.
- The death-branch probe (item 6 (d)) is the only way to reach the living death sequence on the board (`hurt()` discards it in play);
  it is optional and marked as such.

## How to record

Fill the **Result** column (`PASS` / `FAIL` / notes — same marking as TESTING_CHECKLIST.md:8-9) and hand the sheet back. Each Result
is copied **verbatim** into FIX_LOG.md as an `OWNER (verbatim):` entry and into the resolution line of its finding in AUDIT_FINDINGS.md:
BUG-041 (go = the port-wide drop lands; no-go = per-model MOD ruling), ENT-S-092 recheck rows and The Queen batch 1b, ENT-S-091/094 Elevator
re-acceptance, the Beaver Q1 acceptance, Robot5's `in_game_acceptance` in `tools/s4_model_proofs.json`, and BUG-039 (closed external with
the compatibility note, or reopened). Nothing is paraphrased; a FAIL row is triaged after the session, not fixed mid-session.

---

## Section F — 1.7.10 ground-truth instance: `OreSpawn 1.7.10 Reference` (new, 2026-09-03)

Built from Prism's own meta, nothing launched yet: Minecraft 1.7.10 + Forge 10.13.4.1614 + LWJGL 2.9.4-nightly, with
`orespawn-1.7.10-20.3.jar` (sha1 d43dbe9a…76d7; entry-for-entry identical to the repo's reference jar, which only lacks eight
directory entries) in `minecraft\mods\`. No Java 8 exists on this PC, so the instance is pre-set to Prism's managed
`java\jre-legacy` and Prism downloads it on first launch (auto-download and auto-switch are on globally).

| Step | What happens | Pass looks like | Result |
|---|---|---|---|
| Open Prism; the instance appears ungrouped (restart Prism if it does not) | directory watcher | listed with the default icon | |
| Edit → Version: Minecraft 1.7.10, Forge 10.13.4.1614, LWJGL 2 2.9.4; Mods: orespawn-1.7.10-20.3.jar enabled. Do NOT click Change Version / Update | meta fetch | three components, one mod | |
| Launch (your account or Play Offline) | Prism downloads jre-legacy (Java 8u51, ~65 MB), the 1.7.10 client (sha1 e80d9b3b…bbc6), libraries, Forge universal + scala/akka, the asset index and ~112 MB of assets: ~200-250 MB, a few minutes, no prompts | console shows `Compatible Java found at: …/jre-legacy/bin/javaw.exe` | |
| Main menu | FML loading bar, then `Forge 10.13.4.1614 / 4 mods loaded, 4 mods active` | OreSpawn 1.7.10.20.3 listed | |
| If it stops with "not compatible with Java version 21/17" | auto-switch did not run | Instance Settings → Java → Download Java → Mojang jre-legacy (Java 8), relaunch; never tick "skip compatibility checks" | |
| If it stops with "javaw.exe couldn't be found" | the managed download was skipped (offline, or global auto-download off) | re-enable the global Java toggles or use the instance's Download Java, relaunch | |

Use it as the "compare against" for every row above that asks for the 1.7.10 look: same spawn commands
(`/summon` in 1.7.10 is `/summon orespawn.<Name>` … check the F3 entity name; OreSpawn 1.7.10 also ships spawn eggs
in its creative tab), same camera. There is no 1.7.10 PlayNicely config toggle in-game: it is `PlayNicely` in
`minecraft\config\OreSpawn.cfg`, written on first run.


## Section G — dev instance, current build, `-Dmhlib.counters=true`: OPT-028 counter proof and the BUG-044 look (new, 2026-09-03)

Staging: after the BUG-044/OPT-028 gate the dev instance carries two jars — the post-fix build as the active
`orespawn-1.21.1-2.0.0-beta.4.jar` and the pre-fix build renamed `orespawn-1.21.1-2.0.0-beta.4.pre-044.jar.disabled`
in the same mods folder (rename the extensions to swap; never both enabled). Add the JVM argument
`-Dmhlib.counters=true` under Instance Settings → Java → JVM arguments. With it on, the client log prints one line
every 100 client ticks: `MHLib counters (per 100 ticks): ...` with `client.recursive_start`, `client.recursive_end`,
`client.bones_visited`, `client.collecting_passes`, `client.world_pos_reads`, `client.folds`, `client.frames` and the
rest. The proof number is `recursive_start ÷ frames` over a window with exactly one Queen in view and no other
multipart mob (robot spider or ant) or GeckoLib mob on screen: `client.frames` counts every multipart
entity's render pass and the bone counters count every GeckoLib entity's bones.

| Step | Expect | Result |
|---|---|---|
| Post-fix jar: spawn one Queen, stand still with her in view for two log windows | `recursive_start ÷ frames` ≈ 110 (her 110 bones once each), `recursive_end` equal | |
| Pre-fix jar (`.pre-044`), same spot, same window | ≈ 220 (every bone pushed twice through the bridge hook) | |
| Post-fix jar: `collecting_passes` per window versus client ticks in the window | ≈ 1 per tick with one Queen (the once-per-tick gate); ≈ 2 per tick with two Queens | |
| Two-Queen case, post-fix: two Queens in view, F3+B, 5 s | both Queens' part boxes follow their animation every tick | |
| Two-Queen case, pre-fix jar | one Queen's part boxes stick at rest offsets (BUG-044 starvation) | |
| Hitch case, post-fix: one Queen in view, stall the client for more than two ticks (F3+T resource reload, or alt-tab to a heavy window for a second), come back | her part boxes keep following | |
| Hitch case, pre-fix jar | after the stall the part boxes stay at rest offsets for the rest of the session (BUG-044 wedge); server-side reach against her parts fails where the animation has moved them | |
