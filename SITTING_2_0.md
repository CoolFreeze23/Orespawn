# 2.0 Overhaul Verification Sitting — 2026-08-12

## CROSS-CHECKS (session 2 — these finish F-1's mechanism determination)

| # | Check | Result |
|---|---|---|
| X1 | **ANT MOUNT**: `/summon orespawn:ant_robot ~ ~ ~5 {AntRobotOwned:1}` — the NBT flag is REQUIRED: a plain-summoned ant is unowned and faithfully refuses all interaction (`mobInteract` opens with `if (owned == 0) return PASS`, orig :929-935 parity; a bare-summon "nothing happens" is CORRECT and tells us nothing). Then empty-hand right-click — aim dead-center-LOW at the body mass; if legs keep eating the click, try from above. If mounted: Left Alt to hover, confirm the ride works. Record MOUNTS / DOES NOT MOUNT. | **MOUNTS** (owned summon; ride + movement work) |
| X2 | **SPIDER BODY MELEE**: `/summon orespawn:spider_robot ~ ~ ~5`, sword in hand, aim at the small body CORE (low and center at the entity anchor — the big visual shell was never the hitbox, even in classic), hit it. Watch health bar / hurt flash. Record DAMAGE LANDS / NO DAMAGE. **AIM SHARPENED post-X1: the spider's real box is at GROUND level between the legs (the visual body towers 4-6 blocks up on modern legs — the box never moved; classic was identical). Melee + retry the mount at ground-center.** | **DAMAGE LANDS — and a fresh spider MOUNTS when clicked at ground-center** |

**Session-2 observations (X1 ride):**
- **OBS-1 (verify in phase d):** rider sits INSIDE the ant's visual
  shell (F5 needed). Seat math is the faithful orig port (1.25
  behind center, 0.55 − the TF-029 player 0.5 offset ≈ 0.05 above
  the anchor); ridden sag is clamped ±0.15 and cannot be the cause.
  If the CLASSIC ant buries the rider identically → 1.0 parity,
  reclassify as a MODERNIZATION_NOTES candidate, not a FAIL.
- **OBS-2 (calibrate):** leg part boxes read "a little misaligned"
  from the visual legs. Q3 ruling was ONE box per leg: the 0.4 cube
  sits on the lower-segment chord midpoint — most of the leg has no
  box by design, and the axis-aligned cube protrudes slightly off
  steep segments. "Slightly off the segment" = design envelope
  (OBS); "nowhere near the leg" = defect (client part feed). Owner
  to calibrate which.

**Interpretation key (the result self-documents):**
- **X1 mounts + X2 lands** → ids are fine; interaction-swallowing on
  parts is the story, but PARTIAL — the spider's S5a mount wiring
  needs its own look on top of F-1.
- **X1 does NOT mount + X2 lands** → total interaction swallowing by
  the shared S4 part layer; F-1 covers everything.
- **X2 does NOT land on the body core** → the S4-red corrupted-id
  mode is live on the client; escalate F-1's triage to include the
  id capture/restore path before any fix ships.


Overhaul closed at `67acbf9`, suite 183/183 both modes. This sitting
validates ONLY what the suite cannot: the closing report's in-game
judgment list. Marks: **PASS** / **FAIL** (logged + triaged, no
mid-session fixes) / **TUNE** (expected for the ant's ×0.495 starting
tune — described precisely as tuning input, not defects).

Setup: fresh world, creative → survival as needed. Config is MODERN by
default (fresh `runs/client/config/orespawn-common.toml`). Useful:
`/gamemode creative`, `/time set day`, a flat area, a staircase, and
open ground ≥ 40 blocks for the spider.

---

## a. S4 client half (spider legs + King bar)

Setup: `/summon orespawn:spider_robot ~ ~ ~5`

| # | Check | Mark | Notes |
|---|---|---|---|
| a1 | Aim crosshair at a lower-leg shin → HUD bar reads "Giant Robot Spider" + health (not blank) | | |
| a2 | Melee a PLANTED shin → damage lands at body rate (health drops same as a body hit) | | |
| a3 | Arrow into a leg from range → connects on the leg (lead a mid-swing leg at any latency — hitting the body instead pays the same; that skew is Option A, not a fail) | | |
| a4 | `/summon orespawn:the_king` → crosshair bar now works on his giant parts (formerly blank) | | |

## b. Ride feel (spider — the three flagged items + jitter)

Setup: mount the spider (empty hand, right-click).

| # | Check | Mark | Notes |
|---|---|---|---|
| b1 | Sustained full sprint (hold W, ~10s straight line): legs keep up at ~2× the tuning displacement — constant re-stepping expected, but no leg left stretched behind / rubber-banding | | |
| b2 | Hard 180° look-flick at speed and at rest: body swivels instantly, legs walk around DELIBERATELY over ~1–3s (dead-band chase) — no skitter, no all-legs dance | | |
| b3 | Stairs while steered: 1.0 step height (vanilla rider buff, kept intentionally) — does stair climbing feel right from the saddle? | | |
| b4 | Stationary look-jitter: glance around ±small angles while stopped → legs move NOTHING (dead-band). Any dancing = FAIL | | |
| b5 | Seat: body stays rigid under you (±0.15 clamp) — no sag bounce, no seat detach, incl. mounting a spider mid-climb on terrain | | |

## c. Ant (walk, hitboxes, hover-dangle, ×0.495 tune)

Setup: `/summon orespawn:ant_robot ~ ~ ~5`

| # | Check | Mark | Notes |
|---|---|---|---|
| c1 | Six-leg walk + 1–2 block climb: feet plant on terrain, no slide, no stuck legs | | |
| c2 | Leg hitboxes: aim at an ant leg → HUD bar; melee a leg → body-rate damage | | |
| c3 | Mount + hover flight: airborne legs fold into the DANGLE under the body, re-plant on touchdown (designed look — judge the aesthetics) | | |
| c4 | Hovering LOW over planted feet: body sags toward the feet (up to ~0.5) — intended emergent look; judge it | | |
| c5 | TUNE — the ×0.495 starting tune: step speed / trigger radius at ant scale. Twitchy? Sluggish? Steps too long/short? Describe precisely (e.g. "steps too slow", "waits too long before stepping") | | |

## d. Classic pin (one config line back to 1.0)

Setup: quit world → edit `runs/client/config/orespawn-common.toml` →
`spiderMovement = "CLASSIC"` → reopen world → FRESH-summon both robots
(the mode is a construction snapshot; pre-existing moderns stay modern).

| # | Check | Mark | Notes |
|---|---|---|---|
| d1 | Classic spider: attacks pass THROUGH legs, body-only hitbox, classic client-side leg animation | | |
| d2 | Classic spider saddle: faithfully unsteerable (the 1.0 gap) | | |
| d3 | Classic ant: pass-through legs, classic gait, hover-ride identical to 1.0 feel | | |
| d4 | Overall: both robots read as bit-identical 1.0 | | |

---

## Findings log

**F-0 (question, resolved — NOT a finding): unmounted spider stands
still.** FAITHFUL. The original 1.7.10 SpiderRobot registers exactly
two AI tasks — `EntityAIWatchClosest` + `EntityAILookIdle`
(reference source :61-62) — no wander, no movement AI of any kind.
Classic 1.0 baseline: the spider stands where it spawned, watching
you, until ridden. Modern preserves it. Mark the observation PASS.

**F-1 — VERDICT SETTLED (X1+X2 complete): reclassified from FAIL.**
The mount wiring is CORRECT and classic-faithful: melee lands on the
body core and a fresh spider mounts when clicked at ground-center —
the real 2.0×1.5 box sits at the entity anchor exactly where classic
1.0's did, and clicking it works. The initial "cannot mount" was
aim-at-the-visual-shell (the box was never the shell, in 1.0
either). The client id path is EXONERATED (body damage lands — the
S4-red corrupted-id branch is dead). ONE real modern regression
remains, severity MEDIUM (polish): **MHLibPartEntity swallows
interactions** — a click ray that grazes any leg part dies as PASS
instead of forwarding, so modern spiders have FEWER working click
angles than classic (classic rays passed through legless air to the
box; modern rays get eaten by leg boxes). Fix (post-sitting):
vendored part→parent interact forwarding + player-path gametest.
Note the choice this embeds: forwarding makes legs CLICKABLE mount
surfaces — strictly better UX than classic ever had (classic-exact
click geometry is unreachable; non-forwarding is strictly worse) —
flagged for the owner's sign-off as a deliberate better-than-classic
delta. Docs follow-up: KNOWN_ISSUES should tell players the mount
spot is at ground level under the body, as in 1.0.
Original triage (superseded, kept for the record):
- PROVEN DEFECT regardless of the final mechanism: `MHLibPartEntity`
  has NO `interact` override — a right-click landing on ANY leg part
  reaches the server (vanilla routes interact packets to part
  entities via getEntityOrPart) and dies as `InteractionResult.PASS`.
  Parts route DAMAGE to the parent (S4) but nothing ever routed
  INTERACTIONS. A body wrapped in interactive-dead surfaces.
- The BODY path is intact on paper: parent pickability survives
  (mhLibIsPickable = vanilla && main-hitbox canReceiveDamage = TRUE
  on both profiles), `mobInteract` exists with the classic mount
  branch (SpiderRobot.java:393-396, needs: no passenger, within 4
  blocks of center, not sneaking, non-ingot hand).
- Discriminating cross-checks (owner, in progress): [1] melee the
  BODY core (the small 2.0×1.5 box at the entity anchor, not the big
  visual shell) — damage registering there = ids fine, interaction
  swallowing is the mechanism; no damage = client id/pick corruption
  (the S4 red's predicted mode). [2] ant mounts? Ant and spider share
  the whole part layer; ant-mounts-but-spider-doesn't points at S5a
  spider ride wiring instead. [3] Try clicking the exact body core
  vs. the visual shell — the classic hitbox was always the small
  core; the huge visual model was never clickable in 1.0 either.
- Proposed fix (post-sitting): vendored MHLibPartEntity.interact /
  interactAt forwarding to the parent's interact chain — makes legs
  honest interactive surfaces (matches the S4 honest-surfaces
  philosophy), fixes part-click mounting for both robots. Plus a
  player-path gametest driving interact through a part.

**F-2 (FAIL, HIGH, visual): ant legs cross the midline /
hyper-straighten / non-insect stepping.** Triage complete on the
table/conversion side:
- Tables CLEAN: AntRigProfile verified digit-for-digit against
  classic initLegData (by review AND by the in-suite live-classic-ant
  snapshot test); mirroring/signs correct (mirrored −X/+X pairs);
  the 2.5 sweep-floor amendment IS in the shipped constant (the
  1.375 misread never shipped — review caught it pre-commit).
- Conversion CLEAN and the harness DID run the ant rig:
  s5b_ant_render_parity_harness runs all 6 legs × 6 yaws × 7 targets
  against AntRigProfile.RIG with an INDEPENDENT 49px transcription —
  and ModelAntRobot.poseLeg was re-diffed against that transcription
  line-by-line during this triage: hip placement, chain advance,
  and all signs identical. Angles are provably right for any given
  foot target.
- Therefore the fault is foot-target STATE, and the leading theory
  fits all three symptoms at once: **yaw dynamics outrun the ant's
  rest-heading chase.** The ant's rest bearings chase body yaw at
  2.8°/tick behind an 8.6° dead-band, and its feet re-plant at 0.55
  blocks/tick — while the ant's body yaw moves FAST (vanilla body-
  rotation drag from the look goals, the hover-ride yaw chase, and
  classic's aggressive lookAt in combat). During any quick 90-180°
  body turn, planted feet sit CONTRALATERAL (crossing!) stretched to
  the 99.5% reach clamp (hyper-straight!) for the ~30-60 ticks the
  chase needs (catch-up stepping = non-insect motion). The classic
  ant never showed this because its solver works in ANGLE space with
  a velocity controller that snaps joints at up to ~32°/tick — it
  re-aims legs almost instantly on turns; our positional gait at ant
  scale is ~10× slower in angle terms. The spider masks the same
  mechanism with its 17-block scale and 3.4°/tick chase.
- Fix direction (pending the reference research): per-rig faster
  yaw chase for the ant, and/or a large-yaw-delta fast-path
  (re-plant promptly instead of the slow chase when |Δyaw| is big) —
  to be grounded in how the reference handles turning; research in
  flight.

**Reference research: COMPLETE** →
`phase_s_reports/reference_code_addendum.md` (file/function cites +
four mapped proposals). Verdict: F-2's mechanisms are structural in
his code and our S1 read missed them — (1) his rest frame has ZERO
yaw lag (our S5a dead-band chase is the inversion that creates
contralateral plants; his anti-jitter is a rotation-widened trigger
radius + body-turn rate limiting instead), (2) he REJECTS
uncomfortable targets and invalidates stretched plants via a comfort
capsule around REST — no reach clamp exists anywhere (our 98-99.5%
hip-ray clamp is the hyper-straighten mechanism), (3) airborne feet
advect with body velocity + yaw rate so swings track a turning body.
Proposals P1 (zero-lag rest + rotation-widened trigger), P2 (comfort
invalidation), P3 (swing advection + yaw-rate lead), P4 (stationary
trigger shrink, ant tune). P1+P2 are one coherent slice and
supersede part of the owner-accepted S5a anti-dance design — needs
an explicit design ruling. Note: the ant's near-straight REST legs
are law-bound classic geometry (98.3% extension — classic ant stood
that way too); P2 caps modern straightness AT that classic baseline
rather than beyond it.

(TUNE notes aggregate into the tuning docket at close-out.)

## Close-out

(Filled on "done": pass/fail/tune counts, proposed fixes by severity,
tuning docket if the ant notes warrant a follow-up commit.)
