# The keyframe controller's return — design for ruling

Phase G slice (e), design lane, 2026-09-06. Companion to `contract_design.md`. The salvaged
`PhaseLockedKeyframeController` (commit `0d238ba`, `src/main/java/danger/orespawn/entity/animation/`,
272 lines) reviewed against the rulings, with a headless demonstration on the shipped Beaver rig
(`headless_demo/`, numbers in `headless_demo/demo_results.json`). **Nothing is wired; nothing under
`src/` changes.** Bytecode offsets are GeckoLib 4.8.4 (`javap -p -c` of the pinned jar; dumps under
`headless_demo/javap/`).

---

## 1. What the salvaged controller is, in one paragraph

An `AnimationController` subclass whose `process` (a) computes the classic float phase
`(float) ageInTicks × ω × 1.0F` exactly as compiled `ModelBeaver.setupAnim` does, (b) primes itself
straight into `RUNNING` on its first call (GeckoLib's ordinary zero-length TRANSITIONING bootstrap would
show one frame at t = 0 for an entity first seen late), (c) hands GeckoLib that phase as the controller
clock, and overrides `adjustTick` to map it onto clip time by reproducing `Mth.cos`'s table-index chain
(`× 10430.378F + 16384F`, `f2i`, `& 65535`, then the cosine quarter-turn offset), so the clip is sampled
at exactly the phase the classic LUT would have used (including `f2i` saturation at absurd ages), and
(d) after GeckoLib has evaluated the clip's keyframes into per-bone queues, multiplies the named gait
bones' rotation points by `limbSwingAmount` and collapses each to a constant point, which GeckoLib then
writes to the bone. Three of them run in parallel on the Beaver (gait 3.7, teeth 2.7, tail 0.5 rad/tick),
one per frequency group. It compiles unchanged against today's tree (`records.md` §3).

## 2. The ADDENDA change: the time-warp from the declared length

Salvaged: `NORMALIZED_CLIP_TICKS = 20.0` hard-coded; `primeFirstFrame` refused any clip whose baked
length was not 20 ticks; `adjustTick` returned `index × 20 / 65536`.

Returned: `primeFirstFrame` reads `loadedAnimation.length()` (GeckoLib bakes `animation_length` seconds
× 20, `BakedAnimationsAdapter.bakeAnimation` 1-19; absent → the last key's time, 73-87) and stores it;
`adjustTick` returns `index × declaredLength / 65536`. The time-warp ratio is therefore
`declaredLength / naturalPeriod` clip-ticks per game tick, never 1.0 s. Demonstrated (D): the same
shape authored at 0.5 s / 1.0 s / 2.0 s / 3.3 s poses identically — max |Δ| = 0.0 rad over 2049 ages × 6
bones per length — while the ratio changes with the declared length (gait: 5.89 / 11.78 / 23.55 / 38.87
clip-ticks per game tick; the natural periods 1.698 / 2.327 / 12.566 ticks are the constants). The
scratch copy with this change is `headless_demo/src/danger/orespawn/anim/demo/PhaseLockedKeyframeController.java`;
the diff against the salvaged source is the three items in its class javadoc.

## 3. Parallel per-frequency-group controllers

Already the salvaged shape (Amendment 1 "VERIFIED FOR THE RECORD"). What the bytecode adds to the
design: controllers run in registration order (`AnimatableManager$ControllerRegistrar.build` →
`Object2ObjectArrayMap`, insertion order; `tickAnimation` 10-33), and each controller's queues are
drained and written to the bones immediately after ITS `process` (87-410), before the next controller
runs. So (1) a bone must belong to exactly one locomotion group — the SPEC's group map is a partition;
(2) two controllers on one bone = the last registered wins (measured 1.412 rad against the sum of two
layers, G); (3) the per-controller cost is `createInitialQueues` re-allocating a `BoneAnimationQueue`
(nine `LinkedList`s) for EVERY registered bone on EVERY process (1214-1242): 9 bones × 9 queues × 3
controllers = 243 list allocations per Beaver per frame before any keyframe work, plus the salvaged
collapse's 12 `AnimationPoint`s. That is a line item for the spawn-100 benchmark (slice (d)), not a
reason to change the design; a later optimisation could keep the queues across frames since the
controller clears them itself.

## 4. Delta scaling by `limbSwingAmount`: from the bind pose

The scaled quantity is the clip's evaluated value, which GeckoLib adds to the bone's initial snapshot
(`tickAnimation` 323-341: `setRotX(lerp(point) + initialSnapshot.rotX)`). So the clip value IS the
delta from the bind pose, and scaling it scales the delta from bind. The alternative — a delta from the
clip's first key — would be wrong for a cosine gait (its first key is the peak; scaling from it shifts
the mean by `(1 − a) × A`). G1 evidence: the harness's `gait_scaled` leg defines proportionality as
`bind + amplitude × (unit − bind)` per bone (`tools/g1_render_parity.py:770-777`), and the Beaver's
converted rig has no bind rotation on any of the six bones (`beaver.geo.json`: every bone `rotation`
absent), so bind = 0 and the two definitions coincide there; for a rig with bind rotations they differ,
and the bind-pose definition is the one both GeckoLib and the harness use. Demonstrated: the gait error
at amplitudes 0 / 0.25 / 0.5 / 1 is bounded by the amplitude-1 error (2.483e-3 rad, B.linear) and the
unscaled channels' error is amplitude-independent (identical worst cases at amplitude 0 and 1).

## 5. Transitions

**What GeckoLib 4.8.4 does natively.** On `setAnimation` (a new `RawAnimation`) the controller enters
TRANSITIONING (`setAnimation` 40-98); on the next process it polls the first stage, snapshots the bones
(`saveSnapshotsForAnimation`) and, for `transitionLength` ticks of ITS relative clock, feeds each
animated bone a point from the snapshot's rotation to the first keyframe's start value
(`process` 241-324, `BoneAnimationQueue.addNextRotation` 7-72), then goes RUNNING with `tickOffset`
reset (`adjustTick` 1-59). When a play-once clip finishes, `processCurrentAnimation` 71-100 stops the
controller; bones it no longer writes are lerped back to their INITIAL snapshot over
`getBoneResetTime()` ticks (`tickAnimation` 590-904, with the 2π-wrap special case). There is no blend
between two controllers and no blend-out to anything but bind.

**What the returned controller must do.** Its clock is absolute (the entity's age), so GeckoLib's
relative-clock transition is bypassed by the prime — deliberately: the salvaged rule "a controller
first seen late must not restart at phase zero" is Amendment 1's premise (ANIM-001). Blending is
therefore weight-based in the controller (`contract_design.md` §5): the additive layer variant of the
scratch copy (`additive = true`) adds its scaled delta to the bone's accumulated delta instead of
replacing it — measured composition error 5.2e-8 rad (G) — and the weight is the ramped locomotion
weight. Triggered clips stay on an ordinary `AnimationController` with `transitionLength` (blend in) and
`boneResetTime` (blend out to bind), registered FIRST so the additive locomotion layers re-add their
delta on top during the reset; the "attack replaces the gait on the bones it animates" policy is a
weight mask on the locomotion layers (open question 2).

## 6. The wrap sample

Per frequency group, at every natural-period seam inside the dense span, the controller is sampled at
`seam − ε`, `seam` and `seam + ε` with `ε = 4 × period / 65536` (four LUT indices), and the sample
counts only if the LUT index straddles the wrap (before > 60000, after < 5000; the salvaged probe's
rule). Results on the proposed clips (C): gait 24 seams, teeth 17, tail 4, indices 65531 → 4 at every
seam; |v(T−ε) − v(0+ε)| ≤ 1.61e-5 rad (linear at 54/41/19) and ≤ 1e-7 rad (repaired catmullrom at
15/13/8); error against the classic at the three points ≤ 3.99e-5 rad (linear) and ≤ 1e-7 (repaired).
Both hold 2.5e-3. The harness leg should adopt this exact sampler (with the index-straddle check, so a
seam that the float phase does not cross is not counted as a wrap sample).

## 7. The catmullrom density search — the finding of this slice

### 7.1 Algorithm

For each lerp mode: for N = 3, 4, … keys per bone (both endpoints included; uniform in time; the cosine
sampled exactly at the keys; full amplitude), bake the clip through GeckoLib's own loader
(`KeyFramesAdapter.GEO_GSON` → `BakedAnimationsAdapter`, the path a mod's `.animation.json` takes), run
the three controllers through `AnimationProcessor.tickAnimation` on a fresh bake of the shipped rig, and
take the max |candidate − classic| over the search schedule (4097 uniform ages over 40.5 ticks from
137.371, plus the five quadratures ± 1e-4 of every cycle of every frequency, amplitude 1). The fewest N
whose max ≤ 2.5e-3 rad is the group's density; it is then CONFIRMED on the full schedule (65537 uniform
ages plus quadratures; amplitudes 0 / 0.25 / 0.5 / 1; 1,057,088 comparisons for the gait group, 264,272
each for teeth and tail) together with N − 1, which must fail.

### 7.2 Results (`demo_results.json` B, C)

| Mode | gait (A = 1.414 rad) | teeth (0.785) | tail (0.157) |
|---|---|---|---|
| `linear` — fewest keys / bone | **54** (2.483e-3; 53 → 2.575e-3) | **41** (2.414e-3; 40 → 2.547e-3) | **19** (2.350e-3; 18 → 2.675e-3) |
| `catmullrom` as GeckoLib 4.8.4 evaluates it | not reached by 97 keys (6.85e-3 at 97; 73 → 9.14e-3) | not reached by 97 (3.81e-3) | **31** (2.451e-3; 30 → 2.533e-3) |
| `catmullrom` with the spline arguments repaired at load (§7.3) | **15** (2.163e-3; 14 → 2.711e-3) | **13** (1.892e-3; 12 → 2.544e-3) | **8** (2.295e-3; 7 → 3.984e-3) |
| the salvaged 73 linear keys, for reference | 1.344e-3 | 7.47e-4 | 1.49e-4 |

So: 73 was indeed an artefact — linear needs 54 / 41 / 19. GeckoLib's `catmullrom` as shipped is WORSE
than linear at every density for the two larger amplitudes (error falls like 1/N, not 1/N⁴); with the
arguments repaired it is 3.6-4.5× sparser than linear.

### 7.3 Why: the loader's spline-argument rule (bytecode)

`BakedAnimationsAdapter.addSplineArgs` (offsets 59-203) gives each CATMULLROM keyframe two easing
arguments: `args[0] = i == 0 ? frame.startValue() : frames.get(i − 1).endValue()` (119-146) and
`args[1] = i + 1 < size ? frames.get(i + 1).endValue() : frame.endValue()` (147-183).
`EasingType$CatmullRomEasing.apply` (69-113) then evaluates `getPointOnSpline(t, args[0], start, end,
args[1])` — the textbook Catmull-Rom with P0 = args[0], P1 = start, P2 = end, P3 = args[1]. But
`buildKeyframeStack` builds consecutive keyframes as `(length, start = previous value, end = this value)`
(99-117, 425-447), so `frames.get(i − 1).endValue()` IS `frames.get(i).startValue()`: **P0 == P1 on every
segment**, and on the last segment P3 == P2. The resulting curve starts each segment with half the chord
slope instead of the neighbour-derived tangent — a kink at every key — which is an O(h²) error, the same
order as linear, with a worse constant. An arithmetic model of exactly this rule reproduces the measured
GeckoLib numbers to four digits (F: 9 keys 0.10429 vs 0.10429; 13 keys 0.05557 vs 0.05556), and the model
of the textbook periodic rule reproduces the repaired measurement (both 15 keys for the gait).

### 7.4 The repair (proposed; presented, not done)

An OreSpawn-side load pass over the baked `Animation`: for every CATMULLROM keyframe rebuild the two
arguments as P0 = the key BEFORE the segment start (periodic for loop clips: the key before t = L, since
v(L) = v(0); clamped for play-once) and P3 = the key after the segment end (periodic / clamped). Only the
`easingArgs` change; values, lengths, easing types and GeckoLib's evaluator are untouched. It fits in
`OreSpawnGeoReplacementModel.getAnimation` (override → repair once per `Animation`, cache by identity;
the demonstration's `SplineRepair` is ~60 lines and constructs only public records: `Keyframe`,
`KeyframeStack`, `BoneAnimation`, `Animation`, `BakedAnimations`, `Constant`). Consequences to rule on:
it makes the in-game curve match Blockbench's own catmullrom (which uses true neighbours) BETTER than
stock GeckoLib does — an artist's preview and the game agree more, not less; it applies to artist clips
too (no tolerance applies to them, P1, but their look changes from stock GeckoLib's); and it is the
kind of behavioural divergence from a library that deserves an upstream report alongside. Open question 9.
Without it the density statement is the linear row; with it, the repaired row.

### 7.5 The density statement (P4)

"Animation-leg tolerance 2.5e-3 rad; Beaver classic transcription: `walk` 54 linear keys / bone,
`walk_teeth` 41, `walk_tail` 19; wrap sample included" — or, if the repair is ruled, "15 / 13 / 8
catmullrom keys / bone with spline arguments repaired at load". Either way the statement is per
frequency group (the error scales with amplitude, so the gait needs the most keys), an output of the
harness, and re-derived whenever a classic formula is re-transcribed.

## 8. The interaction with `setCustomAnimations`

`GeoModel.handleAnimations` runs `AnimationProcessor.tickAnimation` (all controllers, offsets 268-284)
and THEN `setCustomAnimations` (287-292) → `OreSpawnGeoReplacement.applyCustomAnimations`. Demonstrated
(E): a hook that writes `rff` after the controllers wins (controller −1.1165 rad, hook sentinel 0.1235,
bone reads 0.1235). Therefore a species runs ONE source: under the artist source the hook returns at
once; under classic no controllers are registered. The gate is the manager's controller map
(`contract_design.md` §6.1); no flag can drift from it. Also relevant: `tickAnimation`'s bone-reset loop
(590-904) only touches bones whose `hasRotationChanged()` is false — the hook's `markRotationAsChanged`
marks its bones, `resetBoneTransformationMarkers` (1250) runs at the END of `tickAnimation`, i.e. before
the hook, so the hook's marks survive into the next frame's reset check and its bones are never reset.
That is why the landed code-driven path coexists with an empty controller set today, and it stays true.

## 9. The ENT-S-147 trap

`handleAnimations` returns at 170 — before controllers and hook — when the same instance renders again
at the same `tickCount + partialTick` (pause screen with one entity in view, duplicate partials, a shadow
pass). The returned controller is immune by construction: its clock is the entity's age, so a skipped
frame loses nothing and a repeated frame recomputes the same pose. What must NOT live in `process` or
the state handler is anything that advances by call count: the `idle_alt` roll, the hurt / attack edge
detectors and the weight ramps are keyed on the clip clock (cycle index, `ageInTicks` since the flip)
and stored per manager, so a repeat is idempotent and a skip is caught up. Per-render side effects
that must fire per rendered frame (the Rotator's fan advance) keep the register's proposed
`onRenderPass` descriptor hook shape (ENT-S-147 fix shape) — outside this controller. Open question 8.

## 10. OPT-029

Controllers are constructed inside `AnimatableManager`'s constructor (`registerControllers`, 27-55) and
live in the per-entity manager, which `OreSpawnAnimatableInstanceCache.evict` drops when the entity
leaves the client level. Nothing to change: eviction costs one manager construction plus the
controllers' first prime on re-entry (an absolute clock, so no visible restart). The manager count gauge
already reports them.

## 11. MHLib and the Tier-1 bones

The Queen's profile is `sync-with-model: true, trust-client: true` with a `synched-bones` list; the
robots' are `false / false / []`. Under Amendment 1 point 6 and Q3, a synched bone's part position is the
server-side evaluator's, so a client clip on it would move the picture off the hitbox. The contract
(`contract_design.md` §8.1): synched bones and their ancestors are SPEC-locked — the controller
registers no channel for them (the clip's `BoneAnimation` for a locked bone is dropped at the same load
pass as the spline repair), the package validator rejects keys on them, the glossary marks them. The
lock lifts per bone only when the server-side evaluator consumes the artist clip (Slice 5). The MHLib
collector layer is a no-op for `sync-with-model = false` profiles and unaffected by controllers.

## 12. A finding on the salvaged Beaver clip: its sign is inverted against the landed basis

The salvaged generator (`tools/g3_beaver_animation.py`) authors `rff: −gait°` "because Bedrock X
authoring is sign-inverted when GeckoLib bakes the rotation" — true (constant X and Y rotation keys are
negated at load, `BakedAnimationsAdapter.buildKeyframeStack` 215-224 / 256-265, Z is not) — and its probe
compared the loaded value with the CLASSIC `xRot` sign. But the landed seam's basis, proven by the
Slice 4b fixture, is internal rotation = `(−xRot, yRot, −zRot)`, and the landed converter authors
keyframes as `X = +classic°, Y = −classic°, Z = −classic°` (`tools/layer_definition_to_geo.py`
`json_rotation_delta`, :457-463) precisely so the loader's negation lands on the internal basis. The
salvaged clip therefore poses the Beaver's legs and teeth in the opposite sense: measured 2.827 rad max
error (2 × amplitude) with 25% sign agreement (A.salvaged), versus 1.344e-3 rad and 100% with every
authored sign flipped (A.landed). The demonstration's generator uses the landed convention; the salvaged
generator must be flipped before any of its output is reused. The salvaged proof documents' "GREEN" was
against a reference in the wrong basis.

## 13. The salvaged probe against today's tree

`G3ControllerContractProbe.java` (1,717 lines) compiled verbatim against `build/classes/java/main` plus
the g1 runtime classpath fails on exactly ONE symbol: `animationAgeTicks(state)` (line 1555 — the
salvaged `OreSpawnGeoReplacement` had it; the landed base class has `ageInTicks(entity, state)`, the
same `(float) tickCount + partialTick` chain without the salvaged `DataTickets.TICK == tickCount`
fail-closed check). The controller itself compiles unchanged. The probe's other dependencies
(`GeoReplacementDescriptor`'s six-argument constructor, `ModelBeaver.createBodyLayer`, GeckoLib's
records) all resolve. When the probe is re-landed as the harness's keyframe leg it needs that one rename
plus the P6 length change and the three-mode density search of §7.

## 14. What wiring would touch (a list for the slice that wires it — not done here)

1. `entity/animation/PhaseLockedKeyframeController` (the scratch copy's three deltas; `additive` on).
2. `OreSpawnGeoReplacementModel.getAnimation` override: locked-bone drop, spline-argument repair (if
   ruled), per-`Animation` cache.
3. `OreSpawnGeoReplacement`: `registerControllers` builds the contract layers from a per-species
   `AnimationSpec` (groups, gait bones, clips present, extras) when `OreSpawnConfig.artistAnimations(type)`;
   `applyCustomAnimations` gate on the manager's controller map; a `motionInputs(entity, state)` reader.
4. `OreSpawnConfig`: `[modern] artistAnimations` + `classicAnimationSpecies`; helper; master comment /
   javadoc lists; one MOD record.
5. Triggers: the `LivingDamageEvent.Post` subscriber (server), the edge detectors (client), the
   `applyRotations` death-flip mode if ruled.
6. Harness: the keyframe leg (`kind: keyframe`) with density + lerp mode stated, the wrap sampler, the
   sign convention pinned; the Beaver's `g1_model_proofs.json` entry gains the classic transcription
   clips as the reference leg's keyframe artefact.
7. Gametests: the manager gate (classic vs artist per config), the trigger path, the dedup idempotence.
