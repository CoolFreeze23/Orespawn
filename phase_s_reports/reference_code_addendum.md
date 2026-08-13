# Reference-code addendum — TheCymaera/minecraft-spider, source read

**Date:** 2026-08-12, during the owner's 2.0 verification sitting.
**Trigger:** sitting findings F-2 (ant legs cross the midline,
hyper-straighten, non-insect stepping) and the owner's directive to
read the reference CODE, not the video notes. Shallow clone read by
three parallel reviewers; all cites are `file :: function` in his
repo (Kotlin). License stance unchanged: technique and understanding
extracted; our implementation stays original — no code translation.

**Headline:** our S1 read treated midline-avoidance and bend-keeping
as EMERGENT properties of a drift-triggered gait. In his code they
are ENFORCED by four structural mechanisms we never extracted — and
two of our own S5 design decisions (the dead-banded rest-heading
chase; the hip-ray reach clamp) are near-exact inversions of his
approach. Every one of our ant symptoms maps cleanly.

---

## 1. Why his legs never cross the midline

- **Zero-lag rest frame.** `Leg.updateMemo` (Leg.kt:79-106):
  rest = authored body-local point rotated by the body's CURRENT yaw
  (yaw-only quaternion, `Gait.scanPivotMode` default `PivotMode.YAxis`),
  recomputed every tick. NO dead-band, NO rate limit on the rest
  frame. All turn smoothing lives in the BODY's angular velocity
  (`Behaviour.rotateTowards`, Behaviour.kt:71-105: desired yaw →
  angular velocity, accel-limited, and SCALED BY THE FRACTION OF
  LEGS CURRENTLY GROUNDED — the body physically cannot spin fast
  while legs are mid-step).
- **Candidates cannot be contralateral.** `Leg.locateGroundTarget`
  (Leg.kt:257-319) generates candidates ONLY near rest+lookahead
  (one down-ray + 8 neighboring block corners, ≈1.2 blocks). There is
  no "reject cross-body" test because the generator cannot emit one.
- **Comfort capsule invalidates stale plants.** A vertical capsule
  around the REST point (radius `comfortZoneRadius` 1.2 ≈ 0.5-0.9× the
  rest lateral offset): a candidate outside it is null
  (Leg.kt:314-316); a PLANTED foot that drifts outside it is
  converted to an airborne "stranded" target (Leg.kt:147-149), and a
  non-grounded target **bypasses all gait gating**
  (`if (!leg.target.isGrounded) return true`, GaitType.kt:22,58) —
  the leg lifts IMMEDIATELY, neighbors notwithstanding.
- **Jitter guard = trigger radius, not frame lag.** His trigger
  radius lerps 0.25 (still) → 0.8 (moving) and is FORCED to the full
  moving value while the body is yaw-rotating (`SpiderBody.lerpedGait`,
  SpiderBody.kt:66-73) — rotation widens the trigger band instead of
  lagging the rest frame.

**Maps to F-2 crossing:** our rest bearings chase yaw at 2.8°/t
behind an 8.6° dead-band (the S5a anti-dance design). During any fast
turn our planted feet sit contralateral in the NEW body frame with
nothing invalidating them — the mechanism his design makes
impossible. Our S5a anti-dance solved look-jitter but by lagging the
frame; his solves it by widening the trigger during rotation and
rate-limiting the body's own turn.

## 2. Why his legs never hyper-straighten

- **No reach clamp exists anywhere.** Not on the hip ray, not in the
  chain: `KinematicChain.fabrik` (KinematicChain.kt:15-24, 20 iters,
  tol 0.01) has no max-length constraint. Over-reach is handled by
  REJECTION (comfort capsule above) plus:
- **Comfort feedback into locomotion.** `Behaviour.walkAt`
  (Behaviour.kt:111-114): if ANY planted leg's foot is outside its
  comfort capsule, commanded body velocity is multiplied by
  `uncomfortableSpeedMultiplier` — **0.0 for walk** (full stop; 0.6
  gallop) — and the yaw target freezes (rotateTowards:85). The body
  waits for its legs.
- **Rest ≈ 52-54% of total leg length** in every body plan (hexapod:
  presets.kt:36-43 + Gait bodyHeight 1.1) — legs always keep visible
  bend at rest. Plus a pre-FABRIK knee-up seed
  (`chain.straightenDirection` pitched −80°, Leg.kt:114-124,
  Gait.kt:118-119) so half-extended legs pose with an arc, not a rod.

**Maps to F-2 hyper-straightening:** our 98-99.5% hip-ray render
clamp is exactly the mechanism his design avoids — a stretched foot
stays planted as a straight rod instead of being invalidated. Caveat
for us: the ANT'S REST GEOMETRY IS LAW-BOUND at 98.3% extension
(classic probe opening 9.0 on a 9.19 chain — classic ant legs were
near-straight at rest too). His 53% invariant is unreachable for us
without breaking classic stance parity; what IS adoptable is refusing
to stretch BEYOND rest (comfort invalidation), which caps modern
straightness at classic's own baseline.

## 3. Hexapod body plan + gait grouping

- Plans are authored as Cartesian body-local (hip, rest) POINTS, one
  vector per pair with x negated for the mirror (`BodyPlan.addLegPair`,
  presets.kt:16-19); ordering front-to-back, even=left/odd=right —
  `LegLookUp` index math depends on it.
- Hexapod rests: lateral |x| near-constant per row (1.0/1.3/1.2 —
  mids MOST lateral, since they are geometrically closest to the
  midline), fore-aft spread (+1.1/−0.3/−2.0), rear legs longest.
  Ours (classic-bound): bearings ±45°/±90°/±135°, lateral clearance
  7.07/6.75/3.64 — rear feet tucked toward the midline, the least
  crossing margin exactly where his plan gives the most. Not
  actionable (classic tables are law) but explains why OUR rear legs
  cross first.
- **Grouping: emergent alternating tripod** ({0,3,4}/{1,2,5}) from
  ADJACENT-inhibition: a leg may lift only while its same-side
  neighbors AND its contralateral same-row partner are planted
  (WalkGaitType.canMoveLeg, GaitType.kt:17-41), 1-tick stagger within
  a tripod. Our inhibitor (pair partner + same-side ±2) is nearly the
  same rule — our pair partner IS the contralateral same-row leg —
  with the known index-adjacency caveat on the ant's mid-first
  ordering (mid↔rear uninhibited; recorded S5b).
- **Step targets lead the turn**, not just the velocity:
  `lookAheadPosition` (Leg.kt:247-255) = rest + 0.6·triggerRadius
  along velocity, then ROTATED about the body by the per-tick yaw
  rate. And airborne feet ADVECT with the body every tick
  (`applyBodyMotion`, Leg.kt:159-162/189-192: add body velocity,
  rotate by yaw rate about the center) — swings track a turning body
  instead of flying to a stale world point. We have the velocity
  lead; we lack both rotation terms.

---

## Mapped fix proposals (owner-gated; supersedes parts of the S5a
anti-dance design, which was owner-accepted — flagged accordingly)

**P1 — zero-lag rest frame + rotation-widened trigger (replaces the
dead-band CHASE; keeps its purpose).** Rest targets become pure
functions of current yaw every tick. Anti-dance moves to where his
lives: widen the trigger radius by a term ∝ |per-tick yaw delta|
(rotation forces the "moving" radius), so look-jitter still moves
nothing, while genuine turns trigger prompt steps toward YAW-FRESH
rests — stale contralateral plants become geometrically impossible to
sustain. Ride feel unchanged (body yaw stays 1:1 with the rider; we
cannot rate-limit body yaw like he does without breaking the B3
idiom, so the trigger-widening carries the whole jitter load).
Touches: ModernSpiderGait serverTick (delete restYaw follower state),
LegRig (drop restYawRate; keep/repurpose a jitter-widening constant).
Both rigs; spider re-gates every S2-S5 invariant.

**P2 — comfort invalidation (replaces stretching; the crossing +
straightening backstop).** Per-leg comfort radius measured from the
REST point (rig data: ≈ 0.4-0.5× rest radius — ant ~2.5-3, spider
~4-5 blocks, always < the rest lateral offset so a comfortable foot
can never cross the midline). A planted foot outside it strands
IMMEDIATELY (bypassing inhibitors, exactly like our existing stranded
path) and re-plants toward the current rest+lookahead. The hip-ray
render clamp stays only as the final IK guard, no longer the steady
state. Modern straightness gets capped at classic's own rest
baseline.

**P3 — swing advection + yaw-rate lead (turn-quality).** In-flight
feet inherit body velocity and rotate about the body by the per-tick
yaw delta (both sides — deterministic replay preserved by applying
the same advection in clientTick from synced state); step targets
gain the yaw-rate rotation term next to the existing velocity lead.

**P4 (optional, ant-only tune):** trigger radius floor scaled down
when stationary (his 0.25 vs moving 0.8) so idle ants re-center feet
tightly — folds into the c5 tuning docket.

Sequencing note: P1+P2 are one coherent slice (the mechanisms
interlock; P1 without P2 loses the jitter dead-band with only the
widened trigger as guard, P2 without P1 strands legs against a
lagging frame). P3 is separable polish. All three re-run the full
reviewer + triple-gate machinery; the S5a exit-evidence items (b1/b2
feel) get re-judged after P1 lands since the mechanism changes.
