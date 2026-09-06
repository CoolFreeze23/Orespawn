# Records — Phase G slice (e) design lane (2026-09-06)

Scratch root: `C:\Users\alvin\AppData\Local\Temp\claude\C--Homework-Sessions-Japan\d446b9a1-8c94-40b6-bc87-53918041ff5e\scratchpad\anim\`.
Nothing under `C:\Homework\Projects\Orespawn` was written; no gradle; git read-only (`git show`, `git show --stat`,
`git show --name-only`, `git status --porcelain`); every process under `timeout`; no filesystem / Gradle-cache /
Prism crawling (only the paths given in the brief).

## 1. Files read (repository, read-only)

- `FIX_LOG.md` :3900-4080 (Amendment 1, ADDENDA, Slice 4a head), :5566-5640 (ENT-S-146, OPT-029 / slice (a)),
  :5880-5960 (Slice 4c tail, ENT-S-147 filing, G2 root-order head); grep hits for ENT-S-146/147, OPT-029, MOD-029/031/037.
- `phase_g_reports/phase_g_scope_addendum_2026-09-03.md` (all 244 lines).
- `phase_g_reports/phase_g_salvage_inventory.md` :85-125, :210-235.
- `phase_g_reports/geckolib_migration_design.md` :205-235, :310-325, :505-560.
- `phase_g_reports/ADVISOR_HANDOFF.md` :85-110; `AUDIT_FINDINGS.md` :9749-9800 (ENT-S-147 and neighbours);
  `MODERNIZATION_NOTES.md` :1-30 and the MOD-033..037 headings.
- `src/main/java/danger/orespawn/entity/client/OreSpawnGeoReplacement.java`, `OreSpawnGeoReplacementModel.java`
  (HEAD via `git show HEAD:`), `OreSpawnGeoReplacedEntityRenderer.java`, `PoseInputs.java`, `GeoReplacementDescriptor.java`,
  `BeaverGeoReplacement.java`, `RenderInfo.java`; `ModelBeaver.java` :12-13, :37, :90-101;
  `entity/pose/OstrichPose.java`, `CaveFisherPose.java`; `entity/Beaver.java` :40-110; `entity/Alien.java` grep
  (DATA_ATTACKING :48, :116, :128-138, :230-239); attacking-flag survey over `entity/*.java` (52 files with
  `getAttacking()`; 51 `EntityDataAccessor<Integer> DATA_ATTACKING`, one `ATTACKING_TICKS`, one byte `ATTACKING`;
  2 `swing(InteractionHand` callers; 0 `setAggressive(` callers).
- `src/main/java/danger/orespawn/OreSpawnConfig.java` :170-260, :500-530, :605-640, :680-723.
- `src/g1tool/java/danger/orespawn/g1/G1AnimationRuntime.java` (all), `S4CandidateRuntime.java` :1-90, the g1tool
  directory listing; `tools/g1_render_parity.py` :700-860 and the kind/tolerance grep; `tools/g1_model_proofs.json`
  :40-80 (the Beaver entry); `tools/layer_definition_to_geo.py` :440-500 and the sign grep; `tools/` listing.
- `src/main/resources/assets/orespawn/animations/entity/` listing and `beaver.animation.json` (empty `animations`);
  `src/main/resources/assets/orespawn/geo/entity/beaver.geo.json` (bone table via a Python read);
  `build/resources/main/assets/orespawn/geo/entity/beaver.geo.json` (the demo's rig input).
- MHLib: `src/main/java/de/dertoaster/multihitboxlib/api/IMultipartEntity.java` grep (`syncToModel`, `synchedBones`,
  `trustClient`), `client/IBoneInformationCollectorLayerCommonLogic.java` :26-31;
  `src/main/resources/data/orespawn/multihitboxlib/hitbox_profiles/{ant_robot,spider_robot,the_queen}.json` (the three
  fields).
- `build/g1/runtime-classpath.txt` (newline-separated; joined with `;` and backslashes → slashes for the javac argfile);
  `build/classes/java/{main,g1tool}` presence of the named classes.

## 2. The salvaged commit `0d238ba` (31 files, `git show --name-only --format=`)

```
.gitattributes
phase_g_reports/g3_proof/FINAL_CANDIDATE_PATH_CLASSES.md
phase_g_reports/g3_proof/FIX_LOG_DRAFT.md
phase_g_reports/g3_proof/G3_BUILD_REGION_CONTRACT.md
phase_g_reports/g3_proof/PENDING_BUILD_GRADLE.patch
phase_g_reports/g3_proof/PENDING_G1_G3_BUILD_REGION_REGISTRATION.patch
phase_g_reports/g3_proof/PINNED_BYTECODE_CITATIONS.md
phase_g_reports/g3_proof/reference/beaver.generated-baseline.animation.json
src/main/java/danger/orespawn/OreSpawnClient.java
src/main/java/danger/orespawn/client/DeveloperRendererSelection.java
src/main/java/danger/orespawn/client/PhaseGDeveloperRenderers.java
src/main/java/danger/orespawn/entity/animation/PhaseLockedKeyframeController.java
src/main/java/danger/orespawn/entity/client/BeaverGeoReplacedRenderer.java
src/main/java/danger/orespawn/entity/client/BeaverGeoReplacement.java
src/main/java/danger/orespawn/entity/client/GeoReplacementDescriptor.java
src/main/java/danger/orespawn/entity/client/OreSpawnGeoReplacedEntityRenderer.java
src/main/java/danger/orespawn/entity/client/OreSpawnGeoReplacement.java
src/main/java/danger/orespawn/entity/client/OreSpawnGeoReplacementModel.java
src/main/java/de/dertoaster/multihitboxlib/api/IMHLibExtendedRenderLayer.java
src/main/java/de/dertoaster/multihitboxlib/client/IBoneInformationCollectorLayerCommonLogic.java
src/main/java/de/dertoaster/multihitboxlib/client/geckolib/renderlayer/GeckolibBoneInformationCollectorLayer.java
src/main/resources/assets/orespawn/animations/entity/beaver.animation.json
src/main/resources/assets/orespawn/geo/entity/beaver.geo.json
tools/g3_beaver_animation.py
tools/g3_fixtures/controller_contract.animation.json
tools/g3_java/danger/orespawn/client/PhaseGDeveloperRenderersContractProbe.java
tools/g3_java/danger/orespawn/entity/client/G3CandidateChainContractProbe.java
tools/g3_java/danger/orespawn/entity/client/G3ProductionFuseContractProbe.java
tools/g3_java/danger/orespawn/g3/G3ControllerContractProbe.java
tools/g3_java/danger/orespawn/g3/MHLibBoneCollectionContractProbe.java
tools/g3_runtime_contract_test.py
```

Read from it (`git show 0d238ba:<path>`, copies under `headless_demo/salvaged/`): `PhaseLockedKeyframeController.java`
(272 lines), `G3ControllerContractProbe.java` (1,717), `BeaverGeoReplacement.java` (75), `controller_contract.animation.json`
(33), `g3_beaver_animation.py` (105), `PINNED_BYTECODE_CITATIONS.md` (156), `FIX_LOG_DRAFT.md` (367, skimmed by grep),
`beaver.animation.json` (3,551, saved, not read).

## 3. Bytecode read (javap, pinned jars; dumps under `headless_demo/javap/`, 31 files)

GeckoLib 4.8.4: `AnimationController` (process, processCurrentAnimation, adjustTick, getAnimationPointAtTick,
getCurrentKeyFrameLocation, tryTriggerAnimation, triggerableAnim, handleAnimationState, setAnimation, stop,
forceAnimationReset, transitionLength, hasAnimationFinished, stopTriggeredAnimation, createInitialQueues,
saveSnapshotsForAnimation, the 4-arg constructor), `AnimationProcessor` (tickAnimation, updateBoneSnapshots,
resetBoneTransformationMarkers, isSuspectedCompletedRotation, preAnimationSetup), `GeoModel` (handleAnimations,
applyMolangQueries), `AnimatableManager` (+ `$ControllerRegistrar`), `EasingType` (+ `$CatmullRomEasing`: getPointOnSpline,
apply; the registry names in `<clinit>`; lerpWithOverride; fromJson/fromString), `BakedAnimationsAdapter` (bakeAnimation,
buildKeyframeStack, addSplineArgs, addBedrockKeyframes, calculateAnimationLength, readTimestamp; the `lerp_mode` /
`animation_length` / `loop` string constants), `KeyFramesAdapter`, `Animation`, `Animation$LoopType`, `RawAnimation`
(+ `$Stage`), `AnimationState`, `BoneSnapshot`, `Keyframe`, `KeyframeStack`, `BoneAnimation`, `AnimationPoint`,
`AnimationPointQueue`, `BoneAnimationQueue` (addNextRotation), `GeoBone` (signatures), `PlayState`,
`AnimationController$State`, `MathParser`, `MolangQueries` (static init and which methods touch `DataTickets` /
`Minecraft`), `GeoReplacedEntity` (triggerAnim), `GeoReplacedEntityRenderer` (constructor, getInstanceId), `RenderUtil`
(getReplacedAnimatable), `GeoAnimatable`, `BakedAnimations`.
NeoForge 21.1.223: `LivingEntity`, `Mob`, `Entity` (signatures: hurtTime, deathTime, swinging/swingTime/attackAnim,
walkAnimation, isInWater/isUnderWater/onGround, isAggressive / MOB_FLAG_AGGRESSIVE, getRandom), `FlyingAnimal`,
`WalkAnimationState`.

## 4. Demo commands and exit codes

Toolchain: `C:\Users\alvin\AppData\Roaming\PrismLauncher\java\java-runtime-delta\bin\{javac,java,javap}.exe`
(OpenJDK 21.0.7). Classpath argfile `headless_demo/cp.args` = `headless_demo/classes; build/classes/java/main;`
+ `build/g1/runtime-classpath.txt` joined with `;` (backslashes → slashes). All commands ran from
`headless_demo/` under `timeout`.

| # | Command | Exit |
|---|---|---|
| 1 | `git show --stat 0d238ba`; `git show --name-only --format= 0d238ba`; `git show 0d238ba:<8 paths>` (read-only) | 0 |
| 2 | `javap -p -c -constants -classpath <geckolib jar> <23 classes>` → `javap/*.javap.txt` | 0 each |
| 3 | `javap -p -classpath <neoforge jar> LivingEntity / Mob / Entity / FlyingAnimal+WalkAnimationState` | 0 |
| 4 | `javap -p -c` of `MathParser`, `MolangQueries`, `GeoReplacedEntity`, `GeoReplacedEntityRenderer`; `javap -p` of `RenderUtil`, `ControllerRegistrar`, `GeoAnimatable`, `BakedAnimations`, `AnimationPointQueue` | 0 |
| 5 | `bash compile_salvaged_check.sh` (first attempt: classpath not parsed — MSYS/`printf` mangling; second: newline-separated file joined wrongly) | 1, 1 (tooling; `javac.log` shows "package software.bernie.geckolib … does not exist") |
| 6 | `javac -proc:none -nowarn -d salvaged_compile_check/classes @cp.args <verbatim salvaged controller + probe>` | 1 — exactly 1 error: `G3ControllerContractProbe.java:1555: cannot find symbol animationAgeTicks(state)`; the controller compiled (`PhaseLockedKeyframeController.class` + `$StateFloatFunction.class` emitted) |
| 7 | `javac -proc:none -Xlint:none -d classes @cp.args src/.../PhaseLockedKeyframeController.java src/.../AnimDemo.java` (three iterations: initial, +repair mode / null handling, +key-count labels) | 0, 0, 0 |
| 8 | `java @cp.args danger.orespawn.anim.demo.AnimDemo <beaver.geo.json> smoke_results.json 64 256` (smoke) | 1 — NPE at the density lookup when a group never reached tolerance (the finding itself: catmullrom as-is); fixed by #7's second iteration |
| 9 | `java @cp.args … AnimDemo <geo> smoke_results.json 128 1024` (smoke, 3 s) | 0 |
| 10 | `java @cp.args … AnimDemo <geo> demo_results.json 4096 65536` (full; run 1, kept as `demo_results.run1.json`) | 0 |
| 11 | `java @cp.args … AnimDemo <geo> demo_results.json 4096 65536` (full; run 2 after the label fix; 21 s) | 0 — `demo_results.json` sha256 `e6ffd27d28a7f816ba1626501babf85feb87d3008f5dafd6b87329ec50de663f`; run 1 and run 2 confirmations identical |

Demo schedule (run 2): search 4,097 uniform ages + quadrature/seam probes at amplitude 1 per key count (3..97,
early stop at 73 once every group reached tolerance); confirmation and section C on 65,537 uniform ages + probes ×
amplitudes {0, .25, .5, 1} → 1,057,088 gait comparisons and 264,272 each for teeth and tail per run; wrap sample
with ε = 4 × period / 65536 (24 / 17 / 4 seams); D on 2,049 ages per declared length; G on 65 ages × 5 amplitudes ×
4 harness variants. The JVM was not bootstrapped: `AnimationProcessor.tickAnimation` was driven directly (the Molang
actor update and `handleAnimations`'s `Minecraft.getInstance()` are not on that path; `MolangQueries`' static
initialiser only registers lambdas).

## 5. Deviations from the brief

- The demonstration drives the salvaged controller through `AnimationProcessor.tickAnimation` on the shipped rig
  (the full GeckoLib write path) rather than through a scratch adaptation of `G3ControllerContractProbe` or
  `G1AnimationRuntime` as such; the probe's `PhaseHarness` pattern (direct `process` + queue read) was the model,
  and `G1AnimationRuntime`'s Beaver transcription is the classic reference used verbatim. The production
  `BeaverGeoReplacement.applyCustomAnimations` (the `AnimationState` form) needs `DataTickets`, hence a bootstrapped
  game, so the harness-accepted transcription stands in for it, exactly as the landed G1 proof does.
- The brief's list said "catmullrom before more keys": the demonstration adds a third mode ("catmullrom with spline
  arguments repaired at load") because the bytecode shows GeckoLib's own catmullrom cannot hold the tolerance for the
  larger amplitudes; it is presented as a proposal, not applied.
- A LAYERED (additive) variant was added to the scratch controller to measure the transition/blend proposal; it is
  off by default and part of no ruling.
- `headless_demo/logs/{debug,latest}.log` were created by the log4j configuration on the runtime classpath when the
  JVM ran from `headless_demo/`; scratch only.
- `git status --porcelain` (read-only) showed modified and untracked files in the working tree
  (`phase_g_reports/g1_proof/benchmark/*`, MHLib sources, `src/main/java/danger/orespawn/bench/`, …): the other lane's
  gate work, observed only; none of it is this lane's.

## 6. Stray-process check (last action before the report, 2026-09-06 09:56:38)

```
tasklist | grep -i "java\|javap\|python"
java.exe 94624 901,988 K
```

Only the pre-existing `java.exe` PID 94624 (present before this lane started — a Gradle daemon / the other lane's gate);
no javap, python or demo JVM remains. Every process this lane launched ran under `timeout` and exited.
