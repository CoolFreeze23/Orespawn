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
  register under `[modern]` (MOD-031 `fireRespectsMobGriefing`, accepted default on, is the first).
- Targeting parity: the survey ledger `phase_g_reports/targeting_survey_2026-09-04.md` compares every
  hunter's target selection with 1.7.10; divergences are ruled on in BATCHES from the ledger's split,
  never one by one. Waves: 1 = T7 then T1; 2 = T3a, T2, T5, T6 (+ the T9 split); 3 = T8, T3b, T3c, T4,
  T10. One refuter on S/M batches, two on L; one changelog paragraph per wave. Targeting lanes never
  block the Phase G chain — the chain keeps gate priority; targeting batches gate when there is room.
- Refuters are counted by files touched: over twenty files gets two refuters regardless of the ledger's
  S/M/L label (owner, after wave 1). Flaky default-batch tests are never fixed by retries or widened
  waits: a harness slice finds root causes and isolates tests (mock-player placement, cross-test
  leakage, TEST-003 order sensitivity), findings presented before changes.
- Engine conventions (owner, after wave 2's first half): wherever 1.7.10 tested IMob the port tests `Mob` +
  `Enemy`, port-wide; engine-convention divergences (1.7.10's eye-level player posY, its selection-bounds
  line-of-sight ray) are adopted port-wide through one helper, never per site; an engine quirk without a
  player-visible signature is recorded as deliberately not reproduced, with rationale. Harness slice: only pure
  isolation fixes (finally restoration, batch separation, spacing) are applied without a further ruling.
- The night set (owner, 2026-09-04 night; addendum item 22): the doctrine — 1.7.10 behaviour unless a
  record says otherwise, OreSpawn's contribution exact and the engine's part the modern engine's, an
  engine-frame difference reproduced only with a player-visible signature (else a PN entry), old-engine
  accidents PN entries in both modes, gameplay improvements behind `[modern]` keys as MOD records. Order
  after the push: the MOD-033 extension, T5b (the re-assert rows, ENT-S-126, ENT-S-127, deepslate coal),
  ENT-S-120 slices (a) then (d) under the ruled scope (one `OrigPos` helper for class (b) and the §3.2
  spawn / effect sites; class (a) and the §3.1 accidents not reproduced, PN entries; the evening set's
  ENT-S-120 items superseded), then wave 3 (T8 with the Ender Knight's stare ray, T3b, T3c, T4, T10). The
  Phase G chain keeps gate priority throughout. Harness: F1 applied with one refuter, F5 open, the mock
  player's spawn shield cleared only in the row that pins the hit (F0.6). The ENT-S-120 premise was
  then verified against Mojang's 1.7.10 jar (2026-09-05): the server player's posY was the feet, so
  the ruled OrigPos sweep is held for the owner's amendment (`phase_g_reports/ents120_premise_2026-09-05.md`).
- Rulings of 2026-09-05 (addendum item 23): ENT-S-120 closed on the premise — no listed site diverges, `OrigPos`
  never written, the night set's ENT-S-120 items and the evening set's withdrawn by the closure, the census kept with
  the premise note at its head. Standing rule: an engine-behaviour claim about 1.7.10 becomes a finding only after a
  law-11 check against the jar; a "recalled" premise gets no ruling until verified. The 112 `.mirror()` calls in
  ButterflyModel and the seven item models join the mirror drop (same commit, one geometry-only refuter; the go is
  still the Section B EnderReaper A/B). ENT-S-140 is PN-022 (the engine's pose-sized hitbox, not reproduced). Wave 4
  (ENT-S-130 / 133 / 134 / 137 / 138 / 142 / 143 / 144 / 145 and ITEM-070's classic transcription under B2 — the
  vanilla ray in modern under `[modern] chainsawSweepVanillaSight`, default on) is one batch HELD behind the chain's
  no-look slices, which proceed now in this order, each its own gated slice: (a) the per-entity GeckoLib cache eviction,
  measured with the MHLib counters; (b) Slice 4c (PurplePower, Rotator, the render-instance expansion, the clone-aware
  geometry leg); (c) the G2 root-order contract (bone draw order = vanilla part order from the 4.8.4 bytecode, the
  z-fight exclusion retired, before/after per species before its gate; the proofs regenerate once more after the drop);
  (d) the spawn-100 benchmark harness (MHLib counters in the baseline; threshold proposed, not adopted); (e) the
  animation contract and the keyframe controller's return, designed and presented before wiring; (f) the package
  generator (SPEC, bone glossary, trigger inventory, TEXTURE_MAP, INVENTORY.csv) dry-run on the landed species, nothing
  under `artist_handoff/` committed until the drop lands. Wave 4 gates only when a slice is waiting on the owner; the
  mirror drop lands on the Section B go. Changelog: one paragraph per wave (wave 3's five folded). Harness: the sibling
  classes' floating frozen mobs stay (F0.7).
- Rulings of 2026-09-06 (addendum item 24): slice (c) approved — landed with the fifteen contested-fraction pins and
  the four PENDING_OWNER strings removed, the parity tools defaulting to no exclusion, a logged fallback when a rig's
  draw-order key is absent (loud when present and wrong; an asset-audit ERROR for the mod's own rigs). The animation
  contract's Q1–Q17 ruled (one `[modern] artistAnimations` master with a `classicAnimationSpecies` list; weight-blended
  layers; no death clip and the overlay kept for the pilot; four extras; no Tier-3 clips; the catmullrom repair in the
  replacement seam only; loops normalised to 1.0 s; the Queen (idle + one attack) and the Beaver the pilot pair; the
  keyframe leg, the Q9 repair and the Beaver clip regenerated as the first Tier-2 slice's precondition, presented before
  wiring). The benchmark thresholds R1–R7 adopted; the counters' test seam replaced by a system property on the gametest
  run. Locked bones: keying warned, renaming / re-parenting / deleting refused. ENT-S-146 fixed in classic inside 4c
  (two halves, the visual leg extended for translucency, presented before its gate); ENT-S-147 closed as PN-023; ENT-S-
  148 / 149 / 151 one XS batch; ENT-S-150 the survey lane first, then the value; OPT-030 with the MHLib harvest.
- Before any Tier-2 slice: the spawn-100 benchmark, classic versus candidate, with a proposed
  regression threshold; its baseline also measures MHLib's bone capture per frame and sync per tick
  and on the wire. Proof rule: geometry-only changes take one refuter; motion transcriptions,
  MHLib and renderer changes keep two. A Forge 1.7.10 Prism instance with the original jar is the
  visual ground truth.
- Rulings of 2026-09-12 (addendum item 25) — THE COST RULES, standing, winning over every earlier order of work
  (addendum item 26 and the 2026-09-06 order included): parity lanes frozen until the artist-tier rigs are cut over —
  new findings get a register line and stop, the audit waves run as records only, wave 6 does not run; order: item 15
  landed → the Tier-2 slices → the harvest remainder with Slice 5 → the Tier-1 slices (the first Tier-2 slice on the
  current harness behind the switch as 4b did; the drop regenerates its proofs with everyone else's; its report states
  what the drop would change beyond the proofs, with the cost); refuters: ONE for a conversion slice the harness proves,
  regardless of file count, with no before/after document (the gate literal and the proof rows are the evidence), TWO
  for MHLib / renderer / motion code, NONE for tooling and docs lanes (the tool's own tests and one dry run); a
  harness-semantics change still presents its before/after before its gate; records per landing = the FIX_LOG section
  and the register lines, a superseded dated report gets one banner line at its top naming the FIX_LOG section, the
  changelog once per push; a red gate on a row outside the slice = the smallest green change, one refuter, one FIX_LOG
  paragraph, a register line if a better fix exists; FIX_LOG's Phase A–F sections moved unchanged to `FIX_LOG_pre_G.md`
  (lanes read only the live file); decisions the recorded doctrine already answers are the agent's, tagged "decided under
  doctrine, reversible", one line each; every report opens with three counts and no percentages (rigs through the seam /
  the design's 106; artist-tier species with keyframe clips / 90; artist-tier species packaged / 90, from a regenerated
  scratch dry run). Item 15 LANDS NOW (the owner's look judges the keyframe Beaver with A–H): no event keyframes on
  loops (a README rule; code-fired events come from the trigger inventory); the artist gate opens on `idle` AND `walk`
  together, one without the other stays classic and the checker says so; the late-prime headless twin an open harness
  item (TEST-005) closed in the first Tier-2 slice; the three client-only keyframe rows accepted. Slice (c) / ENT-S-146
  deviations ratified: the ERROR-log-plus-fallback reading of "loud when present and wrong", the Queen outside the seam
  as an explicit set, `coplanar_depth_epsilon_blocks` 1e-5 the named tolerance of the translucent mode only (the
  unrounded projector at 1e-6 rides with the drop's proof regeneration). ENT-S-152 a disclosure (PN-025, no vanilla
  fork); ENT-S-153 reading (2) — NO_OVERLAY on the orb, both renderers (PN-026 now; the code deferred). Ruled, deferred
  (register lines, no code until the cut-over): ENT-S-157 with 145 ((a) + (b): bases √(0.1·A) = 0.1789 / 0.1924, the
  boost ADD_VALUE +0.1406 / +0.1271 to the 4.405 b/s cap; one batch, one refuter); ENT-S-155 (classic 0.8; modern 0.3
  under `[modern] spiderDriverModernSpeed`, default ON, one MOD record); ENT-S-158 (parity both modes through one shared
  travel shape, before/after in b/s per swimmer before its gate); ENT-S-159 (classic, with the next XS batch); OPT-032
  mitigation (c) with the harvest remainder (the real fix Slice 5's evaluator); OPT-031 with the drop; the OPT-013 look
  item is the look sheet's Section H. Order of work: push → the records with the FIX_LOG split → item 15 → Section H →
  the first Tier-2 slice; the drop on the Section B go, the package and the pilot after it; nothing else runs.
- Rulings of 2026-09-13 (addendum item 26): the push (origin/master c6ee196). The first Tier-2 slice's questions: the
  bare-name rule for a multi-group species without a gait group is (a) — the SPEC's `primary_group` (the first group
  when absent) carries the bare `walk`, the others stay `walk_<group>`; a label, not a semantic (the contract's fly →
  walk fallback plays a flyer's walk in flight); item 12's gate and the README rule stand; it lands with the next
  Tier-2 slice (the Dragonfly, Cockateil and Ruby Bird clips regenerate and their gates open; the seam delta takes a
  second refuter as renderer code). ENT-S-161 (a): the seam renderer overrides `renderCube` to hand the un-mangled
  transformed normal through, the harness's capturing renderer takes the same override, two refuters, a PN entry with
  the upstream report text as PN-024 did — it lands FIRST, under its own gate, and the Firefly, Cloud Shark and Gold
  Fish rejoin the next slice. ENT-S-160 (a) with (d), in the remainder slice after the weights slice ((c) is that
  slice's additive layer). Tooling, no refuter: the round-trip time tolerance widened to 5e-5 s (Blockbench's real
  precision read from its exporter when the `_preview` export is built); native-controller species exempt from the
  event-key rule, keyed on the SPEC's controller kind, the checker saying why. The weights slice (Q2 (a), Q13 (a)) is
  contract completion, not a parity lane: after the Tier-2 slices, two refuters, every transcription bit-exact to the
  classic hook (weights on artist clips only, or transcription groups declared always-on); fly / swim semantics settled
  there. THE ORDER, AMENDED (replaces cost rule 3's sequence): Tier-2 slices → the weights slice → the Tier-1 rigs
  through the seam with the hitbox profiles excluded (each boss's SPEC pre-declares its intended locked bones,
  provisional, from the G0 design or the Queen's profile as the template) → the full package; the harvest remainder,
  Slice 5 and the profiles are a phase after the package; the locked-bone policy stays WARN until then; the drop, the
  Queen's package and her pilot on the Section B go, independent of the slices. Order of work now: push → ENT-S-161 →
  the next Tier-2 slice (the three rejoined rigs plus the next species in the design's order, ten to fourteen in all,
  the naming rule with it, one refuter for the conversion, the counts open the report; the two tooling items in the
  same tooling commit). Nothing else runs.
- Rulings of 2026-09-13, second set (addendum item 27): the push (origin/master e7e916e). AMENDMENT 2 TO AMENDMENT 1
  (point 1 amended, the rest stands): a rig LANDS when it draws through the seam on the classic hook with the
  geometry, surface, visual and draw-order legs, the animation leg at 0 rad against the hook, and the reference leg
  where a 1.7.10 pair exists; an exact keyframe transcription ships only where the current form fits without a
  harness extension; every other artist-tier species runs on its hook until an artist delivers idle and walk (the
  self-gate), and NO harness or controller extension is built to transcribe it — the threshold, |cos| / Mth.sin,
  attacking-branch, head-look, position-write and health-frequency idioms are not extensions to schedule; the FK
  rigs land as real hierarchies, on the hook. A species without an exact transcription gets, in its package, the
  SPEC's plain-language transcription of its source formulas and a reference-only clip sampled from the hook at fixed
  inputs (limbSwingAmount 1, not attacking, looking ahead, full health; one natural period, else two seconds), named
  `<species>_reference.animation.json`, refused in the jar by the checker like `_preview` (tooling, no refuter: the
  sampler in the generator, applied to every packaged species). The threshold question: (c) — the Cricket, Baryonyx
  and Cryolophosaurus land on the hook. TEST-006 adopted (first-wins throughout the contest window; every existing
  proof identical; one refuter; the Cloud Shark rejoins the next slice). TEST-007: the converter or the audit refuses a
  cutout rig with a zero-thickness cube that omits the classic face order (tooling). The benchmark's smoke figures:
  timing-class, no action. The mirror drop lands now on its proofs (the reference leg and law 11), the 2026-09-05
  scope, one refuter, one gate; the owner's Section B item 1 look follows it, a failure reverting the drop by commit.
  The pilot pair's package (the Queen: idle and one attack; the Beaver) is generated into `artist_handoff/` after the
  drop, checker PASS, reported with its file list; species packages per slice; the full package when the last rig
  lands. COUNTS: the second count is "artist-tier species with an exact transcription shipped / 90", informational;
  the third, packaged, is the deliverable. Order of work: push → TEST-006, TEST-007 and the sampler → the mirror drop →
  the pilot pair's package → stop and report with the counts and the next slice's species list (the Cloud Shark plus
  the next ten to fourteen in the design's order, on the hook). Nothing else runs.

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
