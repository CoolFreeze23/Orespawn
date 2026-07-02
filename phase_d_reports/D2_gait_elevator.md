# Phase D — slice D2: robot gait solvers, GiantRobot walk state, Elevator rider

Findings: ANIM-006 (close), ANIM-012 (close), ANIM-014 (close, VERIFIED-CORRECT),
ENT-D-066 (new, close). Progress notes on ENT-A-016 / ENT-S-021 (leg-anim clauses
closed; owners unchanged). PN-002 checked: it is ThePrince/ThePrincess flight —
no intersection with the Elevator; it stays with D3.

## D2a — SpiderRobot / AntRobot leg-gait solver (ANIM-006 remainder)

The originals compute the walk client-side per entity: each foot is planted at a
world position; when a leg's reach/swing/elevation leaves its window (or its
step scheduler fires) the foot is relocated to fresh ground and three joint
angles converge at speed-scaled rates. State lives in `RenderSpiderRobotInfo`
(shared holder class for both robots); the models consume it per frame.

| Port site | Original | Content |
|---|---|---|
| `entity/client/RenderSpiderRobotInfo.java` | orig RenderSpiderRobotInfo.java:6-40 | full field set (y/ud angle groups, p1-p3 x-angles, foot world positions, hip real positions, footup/uppoint/footingticker/pairedwith, gpcounter); parameterized by leg count (8 spider / 6 ant) |
| `SpiderRobot.initLegData` | orig SpiderRobot.java:111-198 | 8 legs, mirrored pairs (0-1, 2-3, 4-5, 6-7); legoff 1.25/2.0/1.75/3.4 per pair; ymid −0.32/3.4615927, −1.0/4.1415925, 0.62831855/2.5132742, 1.05/2.0915928; yrange ±0.2617994; yoff −0.3/−0.1 |
| `SpiderRobot.getNewVelocity` | orig SpiderRobot.java:200-238 | velocity controller, scale = speed×8 clamped [1, 4] |
| `SpiderRobot.updateLegs` | orig SpiderRobot.java:240-379 | relocation windows dd>294 / dd<32 / yaw>range×8/7 / \|ud\|>1.25 / ticker==0; 3×99px segments; grass trample on foot-land while ridden + mobGriefing, no dice (SHORT_GRASS→air, GRASS_BLOCK→dirt below — client-side like the original) |
| `SpiderRobot.findNewFooting` | orig SpiderRobot.java:381-486 | reach sweep 16 (rear 10) → 3.5, scan 11 up/14 down, ±1 spread then unbiased ±3, post-scan 294 reach reject; lift bumps +1/+1.5/+1.5 at 3/48/100px |
| `AntRobot.initLegData` | orig AntRobot.java:156-229 | 6 legs, pairs 0-1 / 2-3 / 4-5, legoff 0.75/1.0/1.15, yrange ±0.2617994, yoff −0.75 |
| `AntRobot.getNewVelocity` | orig AntRobot.java:231-269 | scale = speed×18 clamped [2, 8] |
| `AntRobot.updateLegs` | orig AntRobot.java:271-404 | windows dd>144 / dd<22; 3×49px segments; **no** foot-land side effects |
| `AntRobot.findNewFooting` | orig AntRobot.java:406-510 | reach 9 (side-rear 4, front/back-center 6), scan 8 up/9 down, in-loop 144 reach reject; lift bumps +0.3/+0.6/+0.6 at 3/24/50px |
| solver step call | orig SpiderRobot.java:704 / AntRobot.java:740 | once per client tick from `tick()` |
| init call | orig SpiderRobot.java:508-511 / AntRobot.java:532-535 | `entityInit` primes leg data at construction; `didonce` re-init latch inside `updateLegs` preserved as `legDataInitialized` |

Both models (`ModelSpiderRobot`/`ModelAntRobot`, fixed per-leg render loops from
C8/ANIM-006) already consumed the holder fields; no model changes were needed.
The canned sine-wave generators in both entities' `getRenderSpiderRobotInfo()`
were deleted — it is now a plain accessor (orig SpiderRobot.java:515-517 /
AntRobot.java:540-542).

Preserved original quirks: the hand-typed `pi = 3.1415926545` in both
`findNewFooting`s; int-truncated block coordinates; the spider's client-side
world mutation for grass trampling (documented in the method Javadoc).

## D2b — ANIM-014 (GiantRobot walk state) — VERIFIED-CORRECT

Proof: every `RenderGiantRobotInfo` field the model uses is written at orig
ModelGiantRobot.java:162-167 and read back at :170-224 in the same render call —
per-frame scratch, not cross-frame state. The only other write anywhere is
`renderdata.gpcounter = 2000000` (orig GiantRobot.java:80) with no reader. The
port's `ModelGiantRobot.setupAnim` (ANIM-005, C8) computes the identical
formulas per frame — constants 0.19634954084936207 and 0.6283185400806344 match
orig :164-167 digit-for-digit. Recreating the holder would change nothing
visible; the walk state already matches the original.

## D2c — Elevator rider (ANIM-012 remainder)

Full port of orig Elevator.java into `entity/Elevator.java` using the B3
client-predicted riding architecture (the original ran everything server-side
in a full `onUpdate` replacement with boat-lerp + input packets; the mapping
and its deltas are documented in the class Javadoc).

| Port site | Original | Content |
|---|---|---|
| `tickRidden` | orig Elevator.java:364-503 | hover probe 1.25 (lift 0.06 + pos 0.1 / sink 0.01); obstruction wedge depth 3+v×8, +0.05/block, lift ×0.11; yaw lag \|1.85−v\| clamp 0.01..0.9; pitch 10×v; exploding −0.05/t speed bleed; heading-sign reverse test (hand-typed pi); throttle +0.025 (+0.15 boosted) / −0.02 cap 0.35; fly-up key raises cap 0.85→1.85; move + friction 0.98/0.94/0.98 |
| `travel` (riderless) | orig Elevator.java:368-379, 485-488 | hover probe 0.75, zero horizontal motion; remote clients position-lerp (replaces orig boat-lerp :342-363) |
| `serverRiddenTick` | orig Elevator.java:304-315, 368-376, 490-498, 504-510 | exploding state machine (1-in-20000 at v>0.65, 45t); grass trample (1-in-200, mobGriefing); crash at speed>0.75 into a wall → 6+d10 sticks + 2 diamonds, no board item; entity push excluding rider/Girlfriend/Boyfriend |
| `clientEffectsTick` | orig Elevator.java:260-296, 316-326 | 1+v×10 smoke/redstone puffs behind+ahead; splash column over water; exploding show (poof/explosion/smoke/large-smoke ×15 at int-truncated offsets + 1-in-10 explode pops) |
| `hurt` | orig Elevator.java:165-192 | passenger blocks non-player damage; inWall immune; damage ×10 accum, >40 (or creative hit) destroys — **item drop restored** (orig :184-186, was missing) |
| `mobInteract` | orig Elevator.java:542-565 | Ultimate Sword within 4 blocks cycles color 1..10; occupied-board guard; mount |
| `setRiderVerticalInput` | orig :441-443 via `RiderInputPayload` | `RideableFlyer` implemented — the fly-up key now reaches the Elevator (was the generic ±0.15 Δy fallback); fly-down ignored (original had only the single UP/FAST key) |
| save/load | orig Elevator.java:523-536 | color only, clamp 1..10 |
| attributes | orig Elevator.java:109-115 | 60 HP / speed 1.33 / attack 0 (already correct) |
| sizing/tracking | orig Elevator.java:58, OreSpawnMain.java:3883 | 1.25×1.0 (was 1.0×1.0); range 128 / update 1 / velocity updates |
| `ElevatorRenderer` | orig RenderElevator.java:23, 31-38, 52-55 | shadow 0.25; boat hit-wobble sin(t)·t·damage/10·forwardDirection; **texture = elevator{color}.png 1..10** (was hardcoded elevator.png; the 10 textures were already in assets) |
| `ItemElevator` | orig ItemElevator.java:21, 25-36 | stack 1; spawn +1.2 above the clicked block at random yaw; creative keeps the item; invented anvil-place sound removed |
| hover hum | orig Elevator.java:297-303 | `ModSounds.HOVER` 1-in-80 / 0.45 / 55t cooldown (ENT-D-012 sound, moved with the entity) |

Documented mapping deltas (class Javadoc): crash detection is server-side
against a wall probe + last-two-ticks position-delta speed (the mover collision
flag only exists on the integrating side), so destruction can lag the impact by
one tick; the riderless client hover nudge (orig :328-336) is superseded by
vanilla position lerp.

## D2d — ENT-D-066 (new finding): duplicate hoverboard removed (owner decision 2026-07-02)

The port shipped two hoverboards; the original had one (see the finding entry
for citations). Removed: `HoverboardEntity`, `HoverboardItem`, `HoverboardModel`,
`HoverboardRenderer`, the `hoverboard` entity/item registrations, attributes
row, renderer/layer registrations, creative-tab row, `models/item/hoverboard.json`,
2 lang keys. Display names corrected: `item.orespawn.elevator` and
`entity.orespawn.elevator` → "Hoverboard" (orig OreSpawnMain.java:5174 /
3880-3881). Not archived to MODERNIZATION_NOTES per owner decision (duplicated
an original feature; nothing to reintroduce). World-compat: placed
`orespawn:hoverboard` entities/items vanish from existing port worlds.

## Ledger

391 terminal (371 FIXED + 20 VERIFIED-CORRECT + 0 DEFERRED) / 211 open,
total 602 (601 audit IDs + ENT-D-066). `tools/ledger_reconcile.py` green
(TOTAL_EXPECTED bumped to 602).

## Manual tests (appended to FIX_LOG pending list)

- SpiderRobot/AntRobot walk with planted feet that step ahead of the body
  (no synchronized sine paddling); feet stay put while the body glides; legs
  relocate when overstretched; ridden spider occasionally tramples grass.
- Hoverboard (`/give orespawn:elevator` — displays "Hoverboard"): W/S throttle,
  Left Alt = FAST boost; climbs terrain; pitch grows with speed; slamming a
  wall above ~0.75 speed shatters it into sticks + 2 diamonds; random
  malfunction (explosion noises/particles, speed bleed) at high speed; Ultimate
  Sword click cycles 10 skins; destruction by mob punches blocked while ridden;
  the hover hum plays only while ridden.
- Creative tab: exactly one Hoverboard entry; `orespawn:hoverboard` no longer
  resolves (removal is intentional).
