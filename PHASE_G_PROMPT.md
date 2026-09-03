# PHASE G — GeckoLib Model Migration, Animation Conversion & Artist Handoff Package

> **This document is a complete, self-contained project brief.** It was written to be
> pasted into a fresh Claude Code session with no prior context. Everything you need —
> project background, current state, standing laws, build environment, the proven
> template to replicate, known traps, and the phased plan — is in this file. Read all
> of it before touching anything.

---

## 1. What this project is

**Repository:** `C:\Homework\Projects\Orespawn` (git, branch `master`, remote
`https://github.com/CoolFreeze23/Orespawn` — public, owner CoolFreeze23).

This is a **preservation port of OreSpawn 1.7.10 to NeoForge 1.21.1**, currently at
`1.21.1-2.0.0-beta.4` (see `gradle.properties:25`). The 1.0 line was a
line-by-line-verified faithful port (630 audited findings, all citing original
file:line); the 2.0 line adds clearly-labeled modern improvements behind config
gates, with classic 1.7.10 behavior preserved bit-identically. A 192-test GameTest
suite guards it, run under BOTH `spiderMovement` config modes on every build. The
decompiled original source ships in-repo at `reference_1_7_10_source\` (read-only
reference — never edit it).

**The owner's directive for this phase (verbatim):**

> "i really want the models to be updated to geckolab ones so that we can have
> accurate hitboxes like the queen and also later maybe update the animations. i need
> all the models and textures extracted and completely organized. and the mathimatical
> aniatmions converted so they work with the models and imported and their hitboxes
> fixed. make sure when you organize them its really oragnized so i can go on fiverr
> and have someone do it for me and make the animations"

Decomposed, that is **four deliverables**:

1. **Model migration to GeckoLib** — convert the hand-coded Java entity models to
   Blockbench/GeckoLib `.geo.json` rigs, so entities can get bone-synced multi-part
   hitboxes "like the queen" and artist-editable animations.
2. **Complete, organized extraction of all models and textures** — a clean,
   navigable package covering every entity: the rig, its texture(s), its current
   animation behavior, all documented per-entity.
3. **Mathematical animations converted** — the original's procedural trig
   animations (pure `cos`/`sin` code, no keyframes) baked into keyframed
   `.animation.json` clips that play on the converted rigs, **imported and working
   in-game**, with hitboxes fixed (bone-synced where warranted).
4. **A Fiverr-ready artist handoff package** — organized well enough that a hired
   Blockbench animator can open it cold, understand exactly what each entity needs,
   and deliver animations that drop into the mod without engineering rework.

This is the project the S1 spider-overhaul design explicitly deferred: ruling **Q4 —
"Organic rigs (CaveFisher, EmperorScorpion): deferred to a later project"**
(`phase_s_reports\spider_overhaul_design.md:508-515`). That later project is now.

**Scale honesty, up front:** there are **108 hand-coded Java model classes**
(36,403 LOC total) and **428 entity texture files**. Converting everything at once is
not a single session's work. Phase G0 below therefore mandates a tiering design that
the owner approves before conversion begins. Do not skip it.

---

## 2. How the owner works — process laws (non-negotiable)

These laws were established over months of work on this repo. Violating them has
burned sessions before. Follow them exactly.

1. **The guarded gate.** Every commit must pass, in order:
   - `.\gradlew.bat build` — exit 0. This transitively runs `assetAudit`
     (`tools/asset_audit.py`, hard gate wired at `build.gradle:115-121`) and `jarJar`.
     Recorded pass form: "build+assetAudit exit 0 (0 err/0 adv/3 ack)".
   - `.\gradlew.bat runGameTestServer` — the 192-test suite, run under BOTH config
     modes where the change could touch them (the suite flips `spiderMovement`
     internally via isolated batches; full-suite sweeps are run under each default).
   - **THE GUARD:** a gate is green **only when the literal `"All N required tests
     passed"` line is captured from the output.** Exit codes alone are insufficient —
     a wedged JVM once held `session.lock`, the next run failed to start, and gradle
     still exited 0 (`FIX_LOG.md:2670-2675`). Capture the pass line, verify N, then
     check the exit code explicitly (`SUITE=$?` then test it). If a daemon wedges:
     `.\gradlew.bat --stop` and retry.
   - **NEVER commit over a red gate.** Not even when the failure is provably
     unrelated to your change (`FIX_LOG.md:2825` — "RED-GATE LAW: nothing commits").
     Diagnose, report, wait.
   - **Never run two gradle invocations concurrently.** They corrupt each other's
     caches and file locks.
2. **Approval gates.** Design documents are presented to the owner and work STOPS
   until the owner rules. The owner answers open questions with explicit rulings
   (see `phase_s_reports\spider_overhaul_design.md` §7 for the format). Findings are
   presented **before** fixes unless the owner has pre-ruled a fix in.
3. **Owner-only publish.** NEVER `git push`, tag, or create a GitHub release without
   the owner's explicit go for that specific action. Prepare and present everything;
   the owner says "go".
4. **Classic parity is inviolable.** Modern behavior ships behind config gates;
   classic mode must remain bit-identical to 1.7.10 and is regression-tested. For
   THIS phase note carefully: the parity law was written about game logic. A
   renderer/model swap is a **visual** change to shipped 1.0 entities — treat visual
   parity as a first-class requirement (see §7, the render-parity harness) and put
   the classic-vs-modern question for visuals to the owner in the G0 design.
5. **Harness-first law.** Any risky conversion (coordinates, angles, formats) gets a
   verification harness BEFORE the conversion work proceeds, and if the conversion
   fights you, **stop at the harness and show the owner the mismatch rather than
   tuning blind** (`spider_overhaul_design.md:517-520`). This law was honored in the
   spider overhaul (384-case render-parity harness, proven three independent ways)
   and it applies with full force to Java-model → geo.json conversion.
6. **Harness-independence law** (`FIX_LOG.md:2775-2783`): a verification harness must
   not close its loop through the code it verifies. At least one leg must anchor in
   an independent formulation (literal transcription, independently-derived
   reference, or ground-truth data).
7. **Honest disclosure standard.** If something goes wrong (an agent writes where it
   shouldn't, a mistake ships), disclose it unprompted in FIX_LOG, never rewrite
   history, and add a mechanical guard against recurrence.
8. **Escape hatch.** If context degrades mid-task: commit clean at a green gate or
   stop and report — never push through degraded context.
9. **One-writer rule.** Check `git status` before starting. If the tree carries
   another session's uncommitted work, do not stash/clobber it — report and
   coordinate through the owner.
10. **FIX_LOG discipline.** All work in this phase logs under FIX_LOG.md's 2.0
    section, in the established entry style (what/why/evidence/gate result).
11. **Review discipline.** Substantive slices get a multi-reviewer pass before the
    gate. When using orchestrated review agents, use **read-only agent types** for
    reviewers — a "read-only-instructed" general agent once wrote a full
    implementation into the tree mid-task (disclosed in FIX_LOG). Reviewer claims
    about library behavior must be verified against the **pinned jar's bytecode**
    (`javap`), not docs — a reviewer once confidently asserted GeckoLib behavior
    that the bytecode refuted.

---

## 3. Build environment (Windows 11, PowerShell + Git Bash available)

- **JAVA_HOME must point at PrismLauncher's bundled JDK 21** or gradle fails:
  - Git Bash: `export JAVA_HOME="/c/Users/alvin/AppData/Roaming/PrismLauncher/java/java-runtime-delta"`
  - PowerShell: `$env:JAVA_HOME = "C:\Users\alvin\AppData\Roaming\PrismLauncher\java\java-runtime-delta"`
- Gradle: NeoGradle userdev 7.1.21, NeoForge `21.1.223`, Minecraft `1.21.1`,
  parchment mappings 2024.11.17. First build after a clean is slow (decompile); be
  patient, don't kill it.
- **GeckoLib is pinned at `4.8.4`** (`gradle.properties:17`,
  `build.gradle:134`). The owner's play instance runs GeckoLib 4.9.2 — behavior
  differences between the two must be checked against bytecode when they matter.
- MultiHitboxLib (MHLib) is **vendored as source** at
  `src\main\java\de\dertoaster\multihitboxlib\` (98 files, its own `[[mods]]` block
  in `neoforge.mods.toml`, own mixin config, databuddy jarJar'd). Do not update or
  restructure it casually.
- `.\gradlew.bat deployToPrism` copies the built jar into the "ORESPAWN TEST" Prism
  instance for in-game checks (`gradle.properties:34`).
- GitHub CLI auth (never print or persist the token):
  `GH_TOKEN="$(printf 'protocol=https\nhost=github.com\n\n' | git credential fill | grep '^password=' | cut -d= -f2-)"`

---

## 4. Current state — exact numbers (surveyed 2026-08-31, HEAD `5733200`)

### 4a. Models and renderers (the conversion surface)

- All entity model/renderer code lives in ONE flat package:
  `src\main\java\danger\orespawn\entity\client\` (238 .java files).
- **109 model classes** (36,403 LOC): 106 extend `EntityModel<T>`, 2 extend
  `HumanoidModel<T>` (`ModelBoyfriend`, `ModelGirlfriend`), 1 extends GeckoLib's
  `GeoModel<T>` (`QueenModel`). Zero use `HierarchicalModel`. So **108 hand-coded
  models** + the Queen. 108 declare `createBodyLayer()` returning a
  `LayerDefinition` — this is the machine-readable cube data your converter will
  read.
- Largest models (LOC): ModelTheKing 1667, TriffidModel 1609, ModelThePrinceAdult
  1571, TrooperBugModel 1305, LeonModel 1149, ModelPitchBlack 1044, SpitBugModel
  937, ModelKraken 915, ModelThePrinceTeen 913, ModelGodzilla 816,
  EmperorScorpionModel 814.
- **127 renderer classes**, nearly all `MobRenderer`. Registration is centralized in
  `src\main\java\danger\orespawn\OreSpawnClient.java`: 145
  `registerEntityRenderer` calls (from :35) and 116 `registerLayerDefinition` calls
  (from :259). 7 renderers reuse vanilla models outright (6 cow variants use
  `CowModel`/`ModelLayers.COW`, SpiderDriver uses vanilla `SpiderModel`) — these
  likely need no conversion at all.
- **Procedural animation is pervasive:** 106 of 109 models override `setupAnim`; 97
  use `Mth.cos`/`Mth.sin`. Only 12 have static poses (ModelCoin, ModelElevator,
  the head sidecars, etc.). Style (e.g. `ModelBeaver.java:90-102`):
  ```java
  float newangle = Mth.cos(ageInTicks * 3.7F * ANIM_SPEED) * (float) Math.PI * 0.45F * limbSwingAmount;
  this.rff.xRot = newangle;  this.lrf.xRot = newangle;
  this.lff.xRot = -newangle; this.rrf.xRot = -newangle;
  newangle = Mth.cos(ageInTicks * 2.7F * ANIM_SPEED) * (float) Math.PI * 0.25F;
  this.teeth.xRot = newangle;
  ```
  Per-part frequencies desynchronize limbs; `wingspeed`-style constructor constants
  set per-species tempo (Butterfly 1.0, LunaMoth 0.75, Mothra 0.2); many models
  branch on `entity.getAttacking()` to switch amplitude/frequency; some (Chipmunk)
  gate on `limbSwingAmount > 0.1F`; head-yaw/pitch propagates via
  `Math.toRadians(netHeadYaw)` onto several parts. A few models do hand-rolled
  forward-kinematic chaining (EmperorScorpion's 5-segment legs, Alien's
  doLeftLeg/doRightLeg/doJaw/doTail).
- **Historical trap already fixed once — do not reintroduce it:** ANIM-001
  (`AUDIT_FINDINGS.md:5065`): an earlier pass put `limbSwingAmount` into the trig
  **frequency** instead of amplitude across 39 files, freezing animations at idle.
  The distinction between "time-driven" (`ageInTicks` in the phase) and
  "gait-scaled" (`limbSwingAmount` as an amplitude factor) is semantic and must
  survive conversion.

### 4b. The original 1.7.10 reference (ground truth for animations)

- `reference_1_7_10_source\sources\danger\orespawn\` — flat package, 586 files, CFR
  decompile with **obfuscated MCP names**: `setRotationAngles` = `func_78087_a`,
  `render` = `func_78088_a`, `rotateAngleX/Y/Z` = `field_78795_f/78796_g/78808_h`,
  `rotationPointX/Y/Z` = `field_78800_c/78797_d/78798_e`.
- **Critical:** in nearly every original model, `func_78087_a` is an empty stub —
  **the real animation math lives inside `func_78088_a` (render)**, which calls
  `func_78087_a` then does trig directly. 109 `Model*.java` files, 126
  `Render*.java` files.
- The port's `setupAnim` bodies are the already-verified translations of that math.
  **Use the port's Java as the primary conversion source** (it's deobfuscated and
  audited), and the reference for spot-verification only.

### 4c. Textures (the organization surface)

- Single flat directory: `src\main\resources\assets\orespawn\textures\entity\` —
  **428 PNGs, no subfolders.** 426 are byte-identical to the 1.7.10 originals
  (provenance mapped in `provenance_byte_identical_assets.txt`); the only new art is
  `blue_queen.png` / `red_queen.png` (2048², the Queen's dormant/aggro pair).
- **Three naming conventions coexist** (the biggest handoff hazard):
  legacy `<mob>texture.png` (~120 files), modern short `<mob>.png`, and numeric
  variant series (`girlfriend0..40`, `stinkytexture1..19`, `bird1..6`).
- **~90 files are redundant byte-identical duplicates** — 86 source images exist
  under 2–3 names (e.g. `beaver.png` + `beavertexture.png`; `hammytexture.png` =
  `godzillahead.png` = `kinghead.png`; all 19 `stinky<N>.png` = `stinkytexture<N>.png`).
  **Effective unique-image count ≈ 338.** An artist who repaints one name leaves its
  twins stale — the handoff package MUST carry a dedupe/rename map.
- Variant series that are runtime-driven and must stay contiguous:
  `girlfriend0..40` (selected by `GirlfriendRenderer.java:36-45` via
  `getTameSkin()`), `boyfriend0..27`, `bikini0..17`/`swimshorts0..17`.
- Strays in `textures/entity/` that are NOT mob skins: 14 pairs of `<gem>_1/2.png`
  armor layer sheets (duplicated properly under `textures\models\armor\`),
  `logo.png`, `items.png`, `girlfriendgui.png`, weapon sheets, etc. Organize around
  them; don't ship renames without updating code references.
- Dimension histogram: 202 files at 64×32, then 64×64/128×64/128×128 mid-tier;
  bosses at 1024²/2048². Non-power-of-two oddities exist (`triffid.png` 532×715 —
  flag for the artist).
- **The asset audit does NOT cover entity textures** (it gates item models,
  blockstates, renderer registration, GUI refs, sounds — `tools/asset_audit.py`).
  Entity-texture breakage is invisible to the gate. Phase G should extend it (see
  G4) so converted geo/animation/texture references become machine-checked.

### 4d. What already exists toward this goal

- `blockbench_exports\` at repo root: **109 per-entity staging folders**, each
  holding the port's Java model source + its texture(s) (e.g. `Alien\ModelAlien.java`
  + `alien.png`). **Only `TheQueen\` contains an actual conversion**
  (`the_queen.geo.json` + `the_queen.animation.json`). This is raw-material staging,
  not finished extraction — your organized package builds on and supersedes it.
- Python conversion helpers at repo root, from the Queen's own conversion:
  `clean_models_for_blockbench.py`, `build_queen_hierarchical.py`,
  `compile_queen_animation.py`, `compile_all_queen_anims.py`,
  `simplify_animation.py`, `verify_hierarchy.py`. Read them first — they are the
  proven toolchain seed. Also inventory the unscanned `extracted\` and `temp_queen\`
  root directories during G0.
- Backlog items this phase absorbs (read them):
  - **MOD-014** (`MODERNIZATION_NOTES.md:228-272`) — the spider overhaul design;
    its D3 ruling chose **server-authoritative solver-fed** MHLib parts over the
    Queen's client-trusted bone sync for the robots. Both feeds now exist in-repo.
  - **MOD-025** (`:518-528`) — bone-synced hitbox profiles for **TheKing and
    Godzilla** (currently manual `OreSpawnPartEntity` layouts + faithful head
    sidecars). This is the owner's "hitboxes fixed" ask, pre-scoped.
  - **MOD-028** (`:587-598`) — per-segment leg boxes upgrade path.
  - **MOD-003** (`:37-47`) — royal pets' flight wants animation-driven presentation.
- KNOWN_ISSUES items in this area: i043/i074 (animations/scales unverified vs
  1.7.10), i096/i098 (King/Queen models inside huge hitboxes), BUG-035 entries
  (Queen freeze — fixed, awaiting long-term confirmation).

---

## 5. THE TEMPLATE — The Queen's complete GeckoLib + MHLib pipeline

The Queen is the one fully-migrated entity. **Replicate this pattern exactly.**
Full detail below; primary sources: `TheQueen.java`, `QueenModel.java`,
`QueenRenderer.java`, `the_queen.geo.json`, `the_queen.animation.json`,
`data\orespawn\multihitboxlib\hitbox_profiles\the_queen.json`.

### 5a. Assets

- **Geo:** `assets\orespawn\geo\entity\the_queen.geo.json` — Bedrock format
  `1.12.0`, identifier `geometry.ModelTheQueen`, texture 2048×2048, **110 bones**
  (103 with cubes), exactly ONE parentless root bone (`root`), everything else
  parented into a single tree. Legacy 1.7.10 part names kept verbatim as bone names.
  **Filename must be all-lowercase** — 1.21.1 `ResourceLocation` rejects uppercase;
  the internal `geometry.*` identifier may keep its original casing
  (`QueenModel.java:15-23` documents this exact trap).
- **Animations:** `assets\orespawn\animations\entity\the_queen.animation.json` —
  Bedrock format `1.8.0`, 8 clips, each keyframing the same 58-bone set:
  | clip | loop | length (s) |
  |---|---|---|
  | idle | `true` | 3.5833 |
  | idle_to_attack | `"hold_on_last_frame"` | 3.5833 |
  | attack | `true` | 3.5833 |
  | bite | `false` | 0.8958 |
  | tail_whip_right | `false` | 1.75 |
  | tail_whip_left | `false` | 1.7917 |
  | roar | `false` | 3.5833 |
  | death | `"hold_on_last_frame"` | 3.5833 |
  Keyframe shape: `animations.<clip>.bones.<Bone>.position|rotation.<t>: {"vector":[x,y,z]}`.

### 5b. Client classes

- `QueenModel extends GeoModel<TheQueen>` — returns the three ResourceLocations;
  `getTextureResource` branches per-frame on synced state
  (`isAwake() ? red_queen : blue_queen`).
- `QueenRenderer extends GeoEntityRenderer<TheQueen>` — ctor
  `super(context, new QueenModel())`, `shadowRadius = 3.0f`;
  `shouldRender` overridden to `true` (frustum culling disabled because part
  hitboxes extend beyond the root AABB — needed for any bone-synced boss);
  `preRender` applies `poseStack.scale(0.25f,...)` when PlayNicely shrinks her
  (bone-synced parts follow scaled bones automatically). **Nothing MHLib-specific
  in the renderer** — MHLib's `MixinGeoEntityRenderer` auto-attaches
  `GeckolibBoneInformationCollectorLayer` to every `GeoEntityRenderer`.
- Registration: `OreSpawnClient.java:74`
  `event.registerEntityRenderer(ModEntities.THE_QUEEN.get(), QueenRenderer::new);`
  — GeckoLib entities need NO `registerLayerDefinition` (comment at :293).

### 5c. Entity wiring

`TheQueen extends Monster implements GeoEntity`:
- Cache: `private final AnimatableInstanceCache animCache = GeckoLibUtil.createInstanceCache(this);`
  returned from `getAnimatableInstanceCache()`.
- Two controllers (`registerControllers`, `TheQueen.java:1440-1469`):
  1. **"Movement"** (transition 5): a state machine on synced entity data —
     `isDeadOrDying()` → `forceAnimationReset()` + `PlayState.STOP`;
     `getTransitionTicks() > 0` → `idle_to_attack`; `isAwake()` → `attack`;
     else `idle`. (The `forceAnimationReset()` guard prevents a one-frame client
     death-flicker from latching the controller STOPPED — BUG-035 follow-up.)
  2. **"Actions"** (transition 5): default predicate `state -> PlayState.STOP`,
     plus five `triggerableAnim(key, RawAnimation.begin().thenPlay(clip))` entries.
     Server code fires them via `this.triggerAnim("Actions", key)`. Trigger keys
     may differ from clip names (`tail_left` → `tail_whip_left`).
- Melee handshake: AI picks bite/tail_left/tail_right/roar, triggers the anim,
  stores a pending target + delay, and applies damage later at the clip's impact
  frame — animation-synchronized combat.
- `die()` triggers "death" BEFORE `super.die(...)` (once dead, Movement STOPs and
  would suppress the trigger).
- Synced flags (`IS_AWAKE`, `TRANSITION_TICKS`) are saved/restored in
  `addAdditionalSaveData`/`readAdditionalSaveData`.

### 5d. Bone-synced hitbox profile (MHLib)

- File convention: `data\<ns>\multihitboxlib\hitbox_profiles\<entity registry path>.json`
  → `data\orespawn\multihitboxlib\hitbox_profiles\the_queen.json`.
- Structure: `sync-with-model: true`, `trust-client: true`, `synched-bones` = list
  of 10 geo bone names (must match geo EXACTLY), `main-hitbox` (collidable false,
  canReceiveDamage false, `size: [22, 24]`), and one `parts[]` entry per synced
  bone with `damage-modifier` and an `multihitboxlib:aabb` box (size/position/pivot).
  With `canReceiveDamage: false` on the main box, ALL damage routes through parts.
- **No per-entity MHLib code.** `MixinLivingEntity` makes every LivingEntity an
  `IMultipartEntity`; profile presence alone activates parts at construction.
- Client→server sync: the auto-attached render layer reads
  `GeoBone#getWorldPosition` per frame for profile-listed bones, ships
  `CPacketBoneInformation`, and `alignSynchedSubParts()` snaps parts next `aiStep`;
  with `trust-client: true` the client also snaps its own parts immediately.
- **LAWS that bind every new profile:**
  - **Profile main size = classic `EntityType` dims, EXACTLY** — a mismatch
    silently forks modern dims from classic via MHLib's `EntityEvent.Size` hook
    (`FIX_LOG.md:3299-3304`). Add a both-modes dims pin test per entity.
  - **Vanilla neutrality** — no profiles under `data/minecraft/...` may ship
    (BUG-036: a vendored demo `creeper.json` gave vanilla creepers multipart
    hitboxes in two shipped betas; `VanillaParityTests` now pins this).
  - **Server-authoritative preference** — the Queen's `trust-client: true` path
    trusts client-sourced bone positions (recorded as a caveat in MOD-014:252-263).
    The robots' profiles (`spider_robot.json`, `ant_robot.json`) are instead fed
    server-side by the gait solver calling `MHLibPartEntity.setPos` directly. For
    new GeckoLib bosses the Queen path is the template, but the design doc must
    state the trust model per entity and let the owner rule.

### 5e. GeckoLib behavioral facts (verified against pinned 4.8.4 bytecode — BUG-035)

These cost a real field bug to learn. Bake them into everything:

- `thenPlay(clip)` = `LoopType.DEFAULT` = **defer to the animation JSON's `loop`
  declaration.** It does NOT force play-once.
- A triggered animation overrides a `PlayState.STOP` predicate until
  `hasAnimationFinished()`, which requires controller state STOPPED. A clip with
  `loop: true` **never** finishes; `hold_on_last_frame` parks PAUSED and never
  finishes either. **Therefore: every one-off trigger clip must declare
  `loop: false` in the JSON**, or it freezes the controller forever (the Queen's
  mid-air freeze). `death` on hold_on_last_frame is the deliberate exception —
  corpse pose — and is fine because the entity despawns.
- Loop-mode review is MANDATORY for every animation JSON: cross-check every
  `triggerableAnim` clip against its JSON `loop` declaration. A mechanical scan
  for this exists conceptually in FIX_LOG (BUG-035 pattern scan) — automate it
  into the asset audit in G4.
- **Client-visual behavior is invisible to the GameTest suite by construction.**
  Animation defects escape the gate. This is why the render-parity harness (G1)
  and owner in-game verification matter.
- When docs and behavior disagree, disassemble the pinned jar (`javap -c` against
  gradle's cached `geckolib-neoforge-1.21.1-4.8.4.jar`) — that settled BUG-035's
  review dispute.

---

## 6. THE PLAN — Phase G, sliced

Work in order. Each slice ends at a full guarded gate + FIX_LOG entry + commit.
G0 ends at an APPROVAL STOP — no conversion work until the owner rules.

### G0 — Inventory, design doc, and tiering (STOP for approval)

Produce `phase_g_reports/geckolib_migration_design.md` covering:

1. **Complete per-entity inventory table** (machine-generated, all 108+ models):
   model class, LOC, entity type(s) served, EntityType dims, texture file(s) +
   dimensions + duplicate-name twins, `setupAnim` complexity class (static / simple
   cyclic / gait-scaled / state-branching / FK-chained), renderer scale overrides
   (Mothra 10.0, LunaMoth 1.5, EasterBunny/Peacock 0.5, valentine-Girlfriend 5.0,
   PlayNicely ÷4 on bosses), and any audit findings touching it.
2. **Tiering proposal** for owner ruling. A sane starting shape (propose your own
   with rationale):
   - **Tier 1 — full GeckoLib + bone-synced hitboxes:** the giant bosses where
     "accurate hitboxes" is the point (TheKing, Godzilla, Kraken, Mobzilla-class,
     Basilisk, the Prince line, WaterDragon/Dragon, Cephadrome, SeaMonster...).
     MOD-025's King/Godzilla profiles land here.
   - **Tier 2 — GeckoLib rig + keyframed animations, single hitbox:** ordinary
     mobs whose animations the artist will improve.
   - **Tier 3 — convert rig, bake animation, no artist pass:** trivial/static
     models (Coin, Elevator, RockBase, head sidecars...).
   - **Tier 0 — skip:** vanilla-model reuse renderers (6 cows, SpiderDriver);
     SpiderRobot/AntRobot (own solver-driven system — the 2.0 flagship; their legs
     are procedural IK and must NOT be keyframe-baked); TheQueen (done).
3. **The visual-parity question, put to the owner explicitly:** does the GeckoLib
   swap replace vanilla renderers outright (with harness-proven pixel parity), or
   ship config-gated (`modelStyle = GECKOLIB|CLASSIC`)? Note the cost of a config
   gate: double renderer registration paths and double maintenance. Recommend one.
4. **Performance note:** GeckoLib per-frame bone evaluation is heavier than baked
   vanilla `ModelPart` trees. 100+ migrated species can matter. Propose a
   measurement step (e.g. spawn-100 FPS comparison) as part of the harness.
5. **Animation conversion policy** (per complexity class):
   - *Simple cyclic* (`base ± cos(ageInTicks·f)·A`): bake to keyframes by sampling
     one full period at the clip's natural length; loop `true`. The math is exact —
     document each clip's source formula next to it.
   - *Gait-scaled* (`· limbSwingAmount`): CANNOT be baked into a fixed clip
     (amplitude varies per-tick with movement). Options: GeckoLib walk/idle clip
     pair with controller-side blend by speed; or molang `query.ground_speed`
     expressions; or keep the math in a custom controller writing bone rotations.
     Recommend per-tier (artists can't edit code-driven bones — Tier 2 should
     prefer clip-pair blending). Remember the ANIM-001 lesson: amplitude, not
     frequency.
   - *State-branching* (`getAttacking()` etc.): separate clips per state +
     controller state machine on synced data (the Queen's Movement controller is
     the pattern).
   - *FK-chained* (EmperorScorpion, Alien): the chains become real parent-child
     bone hierarchies in the geo (do in Blockbench terms what the code did in
     trig) — these are the hardest and the reason Q4 was deferred; schedule them
     late, after the toolchain is proven on simple models.
6. **Hitbox design per Tier-1 boss:** which bones sync, part sizes/damage
   modifiers, what happens to the manual `OreSpawnPartEntity` layouts and the
   faithful 1.7.10 head sidecars (19.9×10 / 9.9×10 gaze-tracking boxes — BOSS-003/
   008/014 are FAITHFUL and need an owner ruling before removal), trust model
   (client-trusted vs server-fed), and the main-size law compliance line per
   entity.
7. **Texture organization + dedupe plan** (see G2) and the **artist package spec**
   (see G5) as appendices for ruling.
8. **Open questions** section in the S1 style — numbered, each with a
   recommendation. STOP after presenting this doc.

### G1 — Conversion toolchain + render-parity harness (harness FIRST)

1. Build (or extend the Queen's Python scripts into) a **`LayerDefinition` →
   `.geo.json` converter**: parse each model class's `createBodyLayer()` (cubes,
   origins, sizes, UVs, dilations, pivots, initial rotations, mirror flags) into
   Bedrock geo. Java `PartDefinition` trees map 1:1 onto bone trees. Keep original
   part names as bone names verbatim (code and hitbox profiles will reference
   them). Prefer executing the actual Java (a small JVM tool or gametest-side
   dumper serializing baked `ModelPart` trees to JSON) over regex-parsing source —
   the compiled tree is ground truth and regex parsing of 36k LOC will lie to you.
2. Build the **render-parity harness** before converting anything for real:
   - *Geometry leg:* for each converted model, compare every cube's baked
     world-space corners (vanilla `ModelPart` tree at bind pose) against the geo
     JSON's computed corners — assert deltas < ε. This is the independent-
     formulation leg (vanilla bake vs your geo math).
   - *Visual leg:* screenshot A/B of vanilla renderer vs GeoRenderer at bind pose
     and at fixed animation phases (deterministic seed/time), pixel-diff under a
     threshold. The spider overhaul's harness laws apply: independent
     transcription, no self-referential loops.
   - *Animation leg (for G3):* sample the Java `setupAnim` output (bone rotations
     at t = 0, T/4, T/2, 3T/4) against the baked clip's evaluated keyframes.
3. Prove the toolchain end-to-end on ONE simple Tier-3 model and ONE simple
   Tier-2 model (suggest Beaver — clean 3-formula setupAnim), present the
   harness numbers, gate, commit. **If conversion fights you, stop and show the
   mismatch.**

### G2 — Batch extraction + the ORGANIZED package (the owner's headline ask)

Create `artist_handoff/` (repo root, committed) with this structure:

```
artist_handoff/
  README_FIRST.md                  <- the artist contract (see G5)
  INVENTORY.csv                    <- one row per entity: tier, files, status, sizes
  TEXTURE_MAP.csv                  <- every shipped png -> canonical image id,
                                      duplicate-name twins, canvas size, consumers
  entities/
    <entity_registry_name>/        <- e.g. the_king/  (registry names, lowercase)
      <name>.geo.json              <- converted rig (Blockbench-openable)
      <name>.animation.json        <- baked current animations (looping idle/walk...)
      textures/
        <canonical>.png            <- deduped canonical texture(s) + variants
      SPEC.md                      <- per-entity sheet: what this mob is, in-game
                                      size/scale notes, bone glossary (which bone is
                                      which body part), current animation behavior in
                                      plain English + the source formulas, which clips
                                      the artist should improve vs leave, loop-mode
                                      table, trigger keys the code fires, hitbox bones
                                      that MUST keep their names, reference screenshots
      reference/
        screenshot_*.png           <- in-game captures (dev client) for scale/look
```

Rules: registry-name folders (lowercase, matching `ModEntities`); canonical
textures deduped per TEXTURE_MAP (the shipped jar keeps its 428 names — do NOT
mass-rename shipped assets in this phase; the map is how artist edits fan back out
to every twin); variant series documented (girlfriend0..40 contiguous law);
armor-sheet strays excluded from entity folders. Batch-convert per tier order,
harness-verified per entity, in review-sized commits (10–20 entities per slice,
each gated).

### G3 — Animation conversion (math → keyframes → working in-game)

Per the G0 policy: bake clips, verify each with the harness's animation leg,
wire minimal controllers (idle/walk state machines on the Queen pattern), and
migrate Tier-1/2 entities' renderers to `GeoEntityRenderer` per the template in
§5. Every animation JSON passes the loop-mode review (§5e). Every migrated entity
gets in-game eyes at least once via `deployToPrism` before its slice ships;
KNOWN_ISSUES gains a "migrated, awaiting long-look" list like the beta.1 visual
recheck.

### G4 — Hitboxes (MOD-025 executed) + audit hardening

Author profiles for the Tier-1 bosses per the G0 designs; per-entity dims-pin
gametests (both modes); VanillaParityTests stays green; extend
`tools/asset_audit.py` with a GECKOLIB check: every `GeoModel` resource triple
resolves, every profile `synched-bones` name exists in its geo, every
`triggerableAnim` clip exists AND declares a finishing loop mode (false), geo
filenames lowercase. That turns the BUG-035 class of defect into a build failure
forever.

### G5 — The Fiverr handoff finalization

`README_FIRST.md` is the artist contract. It must state, in plain non-programmer
English:

- What the mod is, what they're being hired for (improve/replace the baked
  animations; optionally new ones per SPEC wishlists).
- **Toolchain:** Blockbench (free), open the `.geo.json` per entity, texture(s)
  sit alongside; work in a "Bedrock Entity" project; deliver `.animation.json`
  (format 1.8.0) per entity — and optionally the `.bbmodel` working file.
- **Hard rules:** never rename/delete/re-parent bones (code and hitboxes reference
  them by name — renames break the mod); never rename clips the SPEC marks as
  code-triggered; set the `loop` mode exactly as the SPEC's table says (loop
  `true` for idle/walk cycles, `false` for one-shot actions, `hold_on_last_frame`
  only where the SPEC says); keep texture canvas sizes; keep each clip's length
  near the SPEC's stated period unless the SPEC allows otherwise.
- What "done" looks like per entity and how files come back (folder-per-entity
  mirroring `entities/`).
- A priority order (Tier 1 bosses first) and a per-entity effort estimate so the
  owner can scope gigs.

Final slice: verify a round-trip — take one artist-style edited animation.json,
drop it in, gate, run in-game. Then hand the owner the package summary.

---

## 6b. Scope addendum (owner rulings, 2026-09-03)

Recorded in full in `phase_g_reports/phase_g_scope_addendum_2026-09-03.md`; executed at the slices
named there, sequencing unchanged (owner look session, mirror drop, proof regeneration, the G2
root-order slice with the GeckoLib per-entity cache eviction, then Slice 4c). The headline rulings:

- Artist animations are a 2.0 feature behind the modern config; classic stays code-driven parity.
  Same renderer, two motion sources per species. No parity proof applies to artist clips; the
  owner's in-game look accepts them.
- A standard animation contract (idle / walk / swim / fly by locomotion, attack, hurt, death,
  aggro_idle / calm_idle on the attacking state, optional random idle_alt_N, controller-side speed
  scaling; mob extras named per SPEC) is designed with the first Tier-2 slice and presented for
  ruling before it is wired.
- Handoff package additions: a .bbmodel per mob, an animator's character-sheet paragraph per mob, a
  bone glossary with readable labels beside the locked legacy names, a generated trigger inventory
  per mob from its AI goals and state flags. Pilot handoff (Beaver + one boss) as the G5 round-trip
  test as soon as the contract and package format exist.
- Slice 5 design questions, beside the server-side-evaluator question: a plausibility bound on
  client-reported bone positions for any trust-client path; the MoreHitboxes per-feature comparison
  (not a migration: MHLib stays; pieces MoreHitboxes does better are ported into the vendored MHLib
  under MIT with attribution, the most performant design per feature; both libraries side by side
  only if the bytecode shows their mixin targets do not collide).
- Hitbox-library harvest sequencing: BUG-044 (per-entity render-tick stamp) and OPT-028
  (descriptor-exact selectors, counter proof 220 → 110) land before the owner's look session; the
  remaining harvests (part-to-parent unwrapping, conservative cull bounds, defaultRequire = 1) form
  one harvest slice scheduled before Slice 5; the comparison's other proposals wait for a ruling.
- `modern.enabled` is a master override only, default true: it defers to the per-feature keys (names
  unchanged) and forces every modern feature to classic only when set false; new modern features
  register under `[modern]`.
- Before any Tier-2 slice: the spawn-100 benchmark, classic versus candidate, with a proposed
  regression threshold; its baseline also measures MHLib's bone capture per frame and sync per tick
  and on the wire. Proof rule: geometry-only changes take one refuter; motion transcriptions,
  MHLib and renderer changes keep two. A Forge 1.7.10 Prism instance with the original jar is the
  visual ground truth.

## 7. Standing traps checklist (things that have actually bitten this project)

- Uppercase in resource filenames → `ResourceLocation` crash. Lowercase all geo/
  animation filenames; internal `geometry.*` ids may keep case.
- `loop: true`/`hold_on_last_frame` on a triggered clip → permanent freeze (BUG-035).
- `limbSwingAmount` in frequency instead of amplitude → idle-frozen animations (ANIM-001).
- Profile `main-hitbox.size` ≠ EntityType dims → silent modern/classic dims fork.
- Any file under `data/minecraft/` in the jar → vanilla contamination (BUG-036).
- Gradle exit 0 without the literal pass line → false green. Capture the line.
- Two concurrent gradle runs → lock corruption. Never overlap.
- Config-flipping gametests outside isolated batches → cross-test flake
  (TEST-003 established batch isolation; follow `SpiderGaitTests` idiom).
- Frustum culling on bone-synced bosses → parts freeze offscreen
  (`shouldRender` → true, `noCulling`).
- GeckoLib docs vs pinned 4.8.4 vs owner's 4.9.2 — verify with bytecode when
  behavior is disputed.
- `reference_1_7_10_source\` is read-only. Never edit it.
- Review agents in orchestration: reviewers must be read-only agent types.
- Mass-renaming shipped textures without a code-reference sweep → invisible mobs
  the asset audit won't catch (it doesn't check entity textures).

---

## 8. Your first actions in this session

1. `git status` + `git log --oneline -5` — confirm a clean tree at/after
   `5733200` on `master`. If dirty with someone else's work: stop, report.
2. Set JAVA_HOME (§3). Run `.\gradlew.bat build` once to confirm a green baseline
   before touching anything (capture the asset-audit line).
3. Read, in order: this file; `phase_s_reports\spider_overhaul_design.md` (the
   design-doc format + harness laws); `MODERNIZATION_NOTES.md` MOD-014/025/028;
   the Queen pipeline files (§5); the Queen Python helpers;
   `FIX_LOG.md` 2.0 section tail (gate style + BUG-035/036 entries).
4. Begin G0. Generate the inventory mechanically (a script over
   `entity\client\*.java` + `ModEntities.java` + the texture dir + provenance
   file — commit the script under `tools/`).
5. Present the G0 design doc and STOP for the owner's rulings.

Log everything under FIX_LOG.md's 2.0 section as you go. The owner reads FIX_LOG.

*Prepared 2026-08-31 from a four-reader repo survey at HEAD `5733200`
(v2.0.0-beta.4). Numbers in §4 are exact as of that commit.*
