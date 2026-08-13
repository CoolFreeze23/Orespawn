# 2.0 Verification Sitting — session 3 (final order), 2026-08-13

At `b27c44e` (S6a interact forwarding + S6b THE LEG FIX, 190/190 both
modes). No code changes mid-session; FAILs get triage only. Marks:
**PASS / FAIL / TUNE / RATIFIED**. A fully green sitting → the 2.0
release conversation is next.

Summon notes: ants need `{AntRobotOwned:1}` to obey you; the spider
mount spot is the body box — since S7c the full-size 3.25×2.25
original box (no more aiming for a small ground core) — or, new in
S6a, any LEG.

---

## PRIORITY — the leg fix's exit criteria (pass/fail on S6b)

| # | Check | Mark | Notes |
|---|---|---|---|
| 1 | **ANT WALK (the S6b verdict)**: `/summon orespawn:ant_robot ~ ~ ~5 {AntRobotOwned:1}` — watch it walk and turn. Legs stay on their OWN sides (no crossing), no hyper-straight stretching, stepping reads as insect movement — planting and lifting in a natural rhythm. | | |
| 2 | **SPIDER FLICKS**: mount a fresh spider — hard 180° look-flicks and fast sustained turns. Deliberate re-plant (a quick recovery hop on flicks is designed), no skitter, no crossing under rotation. Stationary look-jitter still moves nothing. | | |
| 3 | **SPRINT RATIFICATION**: sustained full-sprint, straight line. At sprint the legs lift aggressively (forced lifts — the reference brakes its body instead, ours can't). Reads fine → RATIFIED as-built; reads badly → the velocity-compensated fallback (FIX_LOG S6b) gets scheduled. Wording captured verbatim. | | |
| 4 | **MOUNT THROUGH A LEG**: empty-hand right-click directly on a leg part → mounts (F-1/S6a forwarding). | | |

## CARRIED FORWARD — the unfinished phases

| # | Check | Mark | Notes |
|---|---|---|---|
| 5 | Leg melee + arrow into a leg (spider AND ant) → body-rate damage, HUD bar on legs; `/summon orespawn:the_king` → bar on his parts (formerly blank). | | |
| 6 | Ant hover (mounted, Left Alt): dangle look in flight, re-plant on landing; body sag hovering low over planted feet. | | |
| 7 | **c5 TUNE**: with the gait fixed, judge the ant's ×0.495 tune — step speed, trigger radii. Twitchy/sluggish/steps too long or short — verbatim capture for the tuning docket. | | |
| 8 | **Phase d classic pin**: quit world → `runs/client/config/orespawn-common.toml` → `spiderMovement = "CLASSIC"` → reopen → FRESH-summon both. Legs pass-through, unsteerable spider saddle, 1.0 feel — plus OBS-1: is the rider equally buried in the CLASSIC ant (expected: yes, parity)? | | |

## Findings log

- Items 1-2: **PASS** (owner verdict: "the legs look good" — THE LEG
  FIX holds in-game).
- **FAIL-3 (seat math): FIXED in S7a.** Root cause bigger than
  diagnosed: the spider's ORIGINAL seat (3.0 back, 2.625 up, driver
  2.0, orig :523-536) was never ported — the vanilla anchor seat was
  a classic PARITY BUG. Restored both modes; modern composes through
  the S3b body transform; ant raised +0.9 modern-only (OBS-1/MOD-027
  shipped). Suite-pinned (s7_seat_geometry, discriminating tilt).
  NEW sitting item from the reviewer: 180° look-flicks now sweep the
  3-block seat arm ~6 blocks/tick — eyes-on the flick feel with the
  restored seat; seat-yaw smoothing is the fallback.
- **FAIL-4 (riding camera): FIXED in S7b** (owner-approved design;
  arm-only smoothing over the raw S7a seat pivot, 8-corner collision,
  reviewer-verified; feel is eyes-on at this sitting). S7c (spider dims
  restored to the orig 3.25×2.25, ENT-S-088) landed AFTER the camera's
  reviewer pass, so the camera reviewer list now carries two added
  items, re-verified analytically in S7c (pivot rides the absolute
  seat constants, arm constants absolute, collision clips BLOCKS
  only — the camera is dims-independent in code) and eyes-on here:
  (1) framing correctness with the taller/wider body; (2) the
  collision arm in tight spaces, where the bigger suffocation
  profile changes which spaces the spider can even enter.
- **S7c (spider dims)**: eyes-on the restored full-size body box —
  mount clicks anywhere in the 3.25×2.25 box, F3+B vs the visual
  body, and the SpiderDriver's slightly longer mount-seek/drive
  ranges (both derive from live width and now match the original).

## Close-out

(On "done": verdicts, fix docket if any, the c5 tuning docket, and
the release-readiness assessment.)

---

## Prior-session record (compressed)

Session 1-2 results: F-0 idle-stand = classic parity (PASS); F-1
spider mount = reclassified, wiring correct, part interaction
swallowing fixed in S6a `f1d6cfa` (legs now clickable — deliberate
delta, ratified); F-2 ant leg visuals = root-caused via the
reference-code addendum and fixed in S6b `b27c44e` (zero-lag rest
frame + comfort invalidation + swing advection/revalidation); X1 ant
mounts (owned), X2 body melee lands + ground-center mount works;
OBS-1 rider-in-ant-shell = 1.0 parity (MOD-027 candidate); OBS-2
one-box hitbox offset = Q3 design cost (MOD-028, chord pin added).
