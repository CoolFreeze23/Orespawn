# 2.0 FINAL Sitting — closing order, 2026-08-13

At `d63b079` (S6a interact → S6b LEG FIX → S7a seat → S7b camera →
S7c dims 3.25×2.25). No code changes mid-session. Marks:
**PASS / FAIL / TUNE / RATIFIED**. Green sitting → next session is
THE RELEASE. Ants: summon with `{AntRobotOwned:1}`.

## A. THE NEW WORK (S7a/b/c)

| # | Check | Mark | Notes |
|---|---|---|---|
| 1 | Mount the spider — the box is now the FULL 3.25×2.25 body (easy click, no ground-box hunt). Seat: ON TOP of the body (3 back, 2.6 up), not inside it. | | |
| 2 | Camera: glide-back on mount, body in lower third, clear view walking, no jerk from float/tilt, F5 clean, dismount = instant vanilla. Eyes-on: framing vs the taller body; collision arm in a tight cave/under trees (instant shorten, glide back, never clips). | | |
| 3 | Seat flick-sweep: hard 180° flicks — right or whiplash? (Fallback: seat-yaw smoothing, recorded.) | | |
| 4 | Mount THROUGH A LEG: right-click a leg part → mounts (F-1). | | |

## B. RATIFICATIONS (verbatim capture)

| # | Check | Mark | Notes |
|---|---|---|---|
| 5 | SPRINT: sustained full-sprint forced-lift feel — RATIFIED as-built, or invoke the velocity-compensated fallback (FIX_LOG S6b). | | |
| 6 | c5 TUNE: ant ×0.495 feel — step speed, trigger radii. | | |

## C. CARRIED ITEMS

| # | Check | Mark | Notes |
|---|---|---|---|
| 7 | Leg melee + arrows (both robots) → body-rate damage + HUD bar; King's bar on parts; ant hover dangle + low-hover sag. | | |

## D. CLASSIC PASS

| # | Check | Mark | Notes |
|---|---|---|---|
| 8 | `spiderMovement = "CLASSIC"`, fresh-summon both: legs pass-through, spider unsteerable, the RESTORED classic seat (3 back / 2.6 up — different from every prior build, and correct), camera inactive, 1.0 feel. OBS-1: rider equally buried in the classic ant (expected: yes). | | |

## Findings log

(FAILs triaged here; no mid-session fixes.)

## Close-out

(On "done": verdicts, remaining fix/tuning dockets, and the RELEASE
READINESS ASSESSMENT — everything since beta.3: BUG-035 Queen
freeze, BUG-036 creeper stowaway, the spider overhaul S1–S5b, the
sitting fixes S6a/S6b/S7a/S7b/S7c, seat + dims parity restorations;
proposed version; the changelog headline story.)

---

Prior sessions (compressed): legs PASS in-game; F-1 fixed S6a
`f1d6cfa`; F-2 fixed S6b `b27c44e`; seat restored S7a `21ac038`;
camera S7b `0ec1d00`; dims S7c `d63b079` (parallel session). All
triple-gated, suite 191/191+ both modes at each step.
