# Reference Technique Notes — TheCymaera Procedural Animation Video Series

**Source:** Creator's own 3-video explanation of the reference project
(TheCymaera/minecraft-spider). Distilled from subtitle transcripts,
2026-08. Video 3's transcript is auto-generated (ASR) and garbled in
places; garbled terms are reconstructed and marked [ASR].

**How to use this doc:** These are the creator's own descriptions of the
mechanisms the S1 design extracted from source. Where a note CONFIRMS an
S1/S2/S3a decision, it is marked ✅ (increases confidence, no action).
Where it ADDS technique not yet in our design, it is marked ➕ (candidate
for current/future slices — reconcile against the design doc). Where it
is NOT APPLICABLE to our architecture (plugin display-entity rendering
vs our model renderer), it is marked ✖ with the reason. Nothing here
overrides the design doc or the recorded laws (classic probe geometry,
etc.) without an explicit design amendment.

---

## Video 1 — Procedural Walking

### FABRIK core (creator's plain-language definition)
A chain = root position + rigid segments, each with a length and a
tip-position. To solve toward a target:
1. Move the last segment's tip to the target.
2. Recursively move each parent segment to the base of its child
   (subtract parent from child → normalize → multiply by segment
   length → that's the segment's new base = next segment's target).
3. The chain is now detached from its root — run the same procedure
   back toward the root.
4. Repeat forward/backward passes until within an acceptable margin
   of error. He caps iterations (his: 10) in case no solution exists.

✅ Confirms S2's FABRIK implementation shape. Note his cap is 10; S1
recorded 20 from source; our S2 raised the budget to 300 after the
convergence-cliff blocker (mid-range still exits ≤5). Our number is
empirically ours — no action, but the discrepancy is why "reference
constants are starting points only" is written into S1 §1.

### Knee constraint = pre-straighten, not joint limits
Joints natively have full 360° freedom. For legs he does NOT implement
joint constraints — he **straightens the chain toward the target before
running FABRIK**, so the algorithm only bends the pre-posed chain and
the knee always folds the same way.

✅ Confirms S2's straighten-bias seed. This is the entire knee system —
there is no hidden constraint solver to port.

### Step rules (the walk gait, incrementally derived)
1. **Trigger:** when a foot gets too far from its rest position, step
   it back to rest. ✅ (S2 trigger capsule)
2. **Inhibitor:** adjacent legs must be grounded before a leg may
   step. ✅ (S2 inhibitors)
3. **Standstill tightening:** while standing still, DECREASE the
   trigger distance so the spider settles out of awkward poses
   instead of holding them. ✅ (S2's speed-lerped radius 0.25→0.8 —
   low speed = small radius = same effect)
4. **Swing interpolation:** legs phase through each other if rotation
   changes too fast — interpolate gradually across frames. ✅ (S2
   rate-based swing lerp)
5. **Comfort throttle:** if legs get too far from rest, stop/slow the
   BODY — the body waits for the legs, not just legs chasing the
   body. ✅ (S1 comfort feedback: uncomfortable leg throttles
   acceleration and freezes yaw)

### Terrain (video 1 level)
- Scan a column of blocks downward to find ground height → that
  becomes the leg's new rest position. ✅ (S3a, expanded to classic
  probe window by law)
- **Body follows legs:** body position = average of all leg
  positions + a small offset. ➕ *Partially in design* — S3b's
  preferred-height uses average grounded-leg TARGET height +
  bodyHeight (S1 §1); the video states the simpler primitive it grew
  from. No action; S3b's version is the evolved form.
- **Gravity v1:** accelerate body downward; apply counter-force only
  if at least one pair of OPPOSITE legs is grounded. ✖ superseded —
  see Video 2's gravity v2, which is what S1 §1 recorded ("capped
  normal-force scaled by grounded fraction" is our reading of the
  final source; see reconciliation note below).

### Discontiguous terrain (the pillar problem)
Single-column scanning fails over gaps. Fix: scan the 8 NEIGHBORING
columns too, pick the candidate closest to the rest position. New
problem: legs prefer a half-block sideways move over a full-block step
UP. Fix: **raise the comparison point half a block above rest when the
path ahead is obstructed** — that bias makes climbing win.

✅ Confirms S3a's 3×3 grid + height-biased scoring exactly, including
the 0.5 figure and the obstruction condition.

### Rendering workaround (his) — the 180° flip problem
Display entities have pitch+yaw but no tilt; leg joints crossing the
y-axis snapped 180° to re-orient. His fix: custom transformation matrix
with an arbitrary up-vector (cross product of y-axis and the
end-effector direction).

✖ Not applicable as a mechanism — we render through the existing model
angle fields, not display entities. BUT the underlying hazard is real
for us in different clothes: angle-representation discontinuities near
vertical. Our S2 render-parity harness's 384-case sweep is the guard;
if a future report shows a leg snap near straight-down poses, this is
the class of bug to suspect (angle convention crossing a pole), not the
solver.

---

## Video 2 — Running & Galloping

### Body-plan generalization
Quadruped / hexapod / octopod all run the SAME ruleset — only the
functions grouping adjacent and diagonal leg pairs were generalized.

✅ Confirms S1's claim that the ant's 6-leg rig reuses the spider
scheduler via the even/odd/row indexing (S5 will exercise this).

### Walk refinements (post-video-1)
- **Lookahead:** legs step AHEAD of the rest position (in the
  direction of travel). ✅ (S2 velocity-projected lookahead)
- **Diagonal-pair cooldown:** adjustable cooldown spacing the
  movement of diagonal pairs. ✅ (S2 cooldown pair)

### Gallop (deferred by Q2 ruling — recorded for the follow-up)
- Legs group into HORIZONTAL pairs; each pair has a **dominant leg**.
  When the dominant leg exits the trigger threshold, BOTH legs of the
  pair swing together.
- Cooldown between VERTICAL pairs prevents leap-frogging.
- Cooldown between HORIZONTAL pairs customizes the gait feel.
- Selection is manual (a flag), not speed-automatic. ✅ matches S1 §1
  and the Q2 walk-only ruling; this section is the follow-up's spec.

### Gravity v2 — center of mass + support polygon ➕ IMPORTANT
Video 1's "opposite pair grounded" rule is too simplistic for gallop
(and generally). His replacement, explicitly designed to avoid a full
rigid-body sim:
1. Pick a **center of mass** — doesn't need to be accurate, just
   believable.
2. Draw a **polygon around the grounded feet** (the support polygon).
3. COM inside the polygon → apply full counter-gravity force.
4. COM outside → apply the force **at an angle, originating from the
   closest point on the polygon** — the body tips/topples toward its
   unsupported side.
5. Point-in-polygon test: ray-cast from the point in any direction;
   odd intersection count = inside.
6. Stated emergent benefit: the spider **falls off ledges
   convincingly** (tips over the edge instead of hovering until the
   last foot leaves).

➕ RECONCILIATION REQUIRED WITH S3b (in flight): S1 §1 recorded the
body as "capped normal-force lift toward preferred height, scaled by
the fraction of grounded legs." That is a scalar simplification — it
sags uniformly but never TIPS: a spider with all four right feet
grounded and all left feet stranded gets 50% lift straight up, whereas
the support-polygon version correctly rolls it leftward off a ledge.
The polygon version is also cheap (one ray-cast test + closest-point on
a small 2D polygon). Options for the design amendment:
  (a) Ship S3b with the S1 scalar rule (matches the recorded design;
      pitch/roll from corner-leg geometry supplies SOME tilt
      visually), evaluate polygon gravity as a follow-up alongside
      gallop (they were co-designed in the reference).
  (b) Adopt COM + support polygon in S3b now — closer to the
      reference's final behavior, better ledge falls, moderately more
      implementation + test surface (COM definition per rig, polygon
      from grounded feet, angled-force application).
This is an owner/design decision; do not silently pick.

### IK recap
Restates the FABRIK normalize-multiply mechanics in detail (identical
to Video 1's, with explicit vector math). ✅ no new information.

### Cloaking / camouflage
Block-texture camouflage, color-matching tables, glazed-terracotta
glitch effect. ✖ Not applicable — cosmetic display-entity feature;
our spiders have real textured models. (If a "stealth robot" ever
becomes a 2.0 wishlist item, this is the reference, but it is out of
scope for the overhaul.)

---

## Video 3 — Robots (ASR transcript; reconstructed where garbled)

### Torso variants, brightness tricks, cloak color system
Display-entity body construction from vanilla blocks, brightness
0–15 as a shading tool, an assets-crawler generating block→average-
color tables for the cloak. ✖ All display-entity/resource-pack-
avoidance concerns — not applicable to our modeled entities.

### Per-segment initial rotation ➕ (small, relevant)
His legs previously bent uniformly because the chain was
**reinitialized (re-straightened) before every FABRIK application** —
✅ that confirms our per-solve straighten-bias is the reference's
actual final structure, not a one-time seed.
New in v3: one segment needed to bend DIFFERENTLY from the rest, so he
added an **initial rotation parameter per segment** applied at the
pre-straighten stage, giving control over how the chain resolves. He
calls it "still a shortcut with many limitations" vs a proper
constraint system.
➕ For us: our rigs already carry a per-segment rest pose
(+45/0/−45). If S2's single scalar pitch bias ever proves insufficient
for a rig (the ant in S5, or organic rigs later), the upgrade path is
exactly this: promote the bias to a per-segment vector applied at the
straighten stage. Note it; don't build it until a rig demands it.

### 3-DOF body orientation [ASR: "quorans"=quaternions, "UL
### ankles"=Euler angles]
Rewrote the body math on **quaternions** (easier than Euler for
composed 3-axis rotation). Target orientation:
- **Pitch** from the vector between FRONT and BACK legs.
- **Roll** from the vector between LEFT and RIGHT pairs.
✅ Confirms S3b's pitch/roll-from-corner-leg-geometry exactly.
- **~30° leeway** on top of the terrain-derived orientation so the
  body can aim at the player. ➕ Worth one line in S3b: the derived
  pitch/roll is a TARGET the body eases toward (through the 0.3
  low-pass), and combat/look poses may add a bounded offset on top —
  design the tilt pipeline so a later aim-offset composes cleanly
  rather than fighting the terrain term.

### Angular velocity + accidental spring ➕
Added angular velocity so projectile hits rotate the body; the
implementation accidentally produced a **damped-spring wobble** on
rotation, which he kept as a feature (tunable stiffness/damping).
➕ For us: S3b's 0.3 low-pass is a first-order smoother — it eases but
never overshoots. A spring-damper on the tilt would add the organic
overshoot-and-settle his robots have. Candidate as an S3b tuning
option or follow-up polish; also the natural home for hit-reaction
tilt (a damage impulse into the spring) if the Queen-pass contact
work ever extends to the spiders. Not required for S3b's exit
criteria.

### Server-side keyboard input (1.21.1)
He uses the new server input packets to drive plugin "riding." ✖ Not
applicable — we are a mod with a real `tickRidden` path (B3), which
is strictly better positioned.

---

## Summary table

| Mechanism | Status vs our project |
|---|---|
| FABRIK forward/backward + iteration cap | ✅ S2 (our cap empirically retuned) |
| Pre-straighten as the whole knee system | ✅ S2, re-confirmed per-solve by V3 |
| Trigger distance / standstill tightening | ✅ S2 speed-lerped radius |
| Adjacent-grounded inhibitors + pair cooldowns | ✅ S2 |
| Swing interpolation | ✅ S2 |
| Comfort throttle (body waits for legs) | ✅ S1/S2 |
| Velocity lookahead stepping | ✅ S2 |
| 3×3 neighbor scan + half-block climb bias | ✅ S3a (grid, bias=0.5, obstruction-gated) |
| Body = avg legs + offset → preferred height | ✅ S3b (evolved form) |
| Pitch from front/back, roll from left/right legs | ✅ S3b plan |
| Quaternion body orientation | ✅ S3b (JOML quaternions native to our stack) |
| **COM + support-polygon gravity (tips off ledges)** | ➕ **decision needed** — S3b amendment option vs S1's grounded-fraction scalar |
| ~30° aim leeway composed over terrain tilt | ➕ S3b pipeline note (compose-friendly design) |
| Spring-damper tilt (overshoot wobble, hit reaction) | ➕ optional polish / follow-up |
| Per-segment initial rotation at straighten stage | ➕ upgrade path if a rig needs it (S5+) |
| Gallop (dominant-leg horizontal pairs + cooldowns) | recorded for the Q2-deferred follow-up |
| Display-entity rendering, matrix up-vectors | ✖ (hazard class noted for angle poles) |
| Cloaking/camouflage, brightness shading, torso kits | ✖ cosmetic plugin features |
| Server-input riding | ✖ we have tickRidden |

**Net new decisions for the owner:** exactly one blocking (support-
polygon gravity vs scalar lift, §Video-2), two non-blocking notes to
fold into S3b's design amendment (aim-offset composability, spring
option), one future-slice note (per-segment rotation), and the gallop
spec banked for later.
