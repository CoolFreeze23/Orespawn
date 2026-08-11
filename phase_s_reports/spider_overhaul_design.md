# Procedural Spider Overhaul — S1 Design Document

**Project:** 2.0 flagship (MOD-014), first post-parity feature.
**Scope:** SpiderRobot + AntRobot procedural IK leg animation + per-limb
multi-part hitboxes, config-gated (`spiderMovement = classic | modern`,
default **modern** per owner directive — note: MOD-014 suggested default
classic; the owner's project brief overrides, recorded here).
**Inviolable:** `classic` is the untouched D2 parity path, bit-identical;
the 1.0 suite stays green in classic throughout.
**Status:** APPROVED 2026-08-11 as written, including the default-modern
override of MOD-014 (S5's changelog must frame classic as one-config-line
parity preservation). Rulings recorded in §7. S2 in progress.

---

## 1. Reference technique (TheCymaera/minecraft-spider) — extracted

Paper/Kotlin plugin, package `com.heledron.spideranimation`. Technique
only; our implementation is original Java on our architecture.

- **IK:** FABRIK (forward-and-backward reaching), 20 iterations, 0.01
  convergence tolerance, positions-first with rotations derived after.
  The knee problem is solved *before* the solve: the chain is
  pre-straightened along the hip→target direction with a fixed −80°
  pitch bias, so FABRIK only bends the pre-posed chain and the knee
  always folds the same way. No hard extension clamp — overstretch is
  the gait layer's problem.
- **Gait:** distance-triggered, never timer-initiated. Legs indexed
  even/odd = left/right, row = leg/2, two diagonal groups. WALK: a leg
  steps when its foot exits a speed-scaled **trigger capsule** around
  its rest axis (radius lerps 0.25→0.8 with speed/maxSpeed), gated by
  inhibitor cooldowns (no adjacent leg mid-swing; cross-pair and
  same-pair cooldowns). GALLOP: row-paired legs swing near-together,
  rows phase-offset; falls back to WALK when not walking; selection is a
  manual flag, not speed-automatic. Comfort feedback: an uncomfortable
  leg (outside the fixed 1.2-radius comfort capsule) throttles
  acceleration and freezes yaw.
- **Foot placement:** target = rest position + velocity-projected
  lookahead (0.6 × trigger radius), rotated by yaw rate for
  turn-in-place. Ground via a downward scan through the lookahead XZ
  (+1.6·bodyHeight above to −2.5·bodyHeight below), with a 3×3
  neighbor-column candidate grid scored by distance to a preferred point
  that is raised 0.5 when the block ahead is impassable (the ledge/wall
  climb assist). No valid ground → the leg goes **stranded** (dangles at
  lookahead, claims no contact, may re-step unconditionally). Swing is
  rate-based lerp with a parabolic `4t(1−t)` lift (peak 0.35), mid-step
  retargeting allowed, snap-down on obstacle contact.
- **Body:** a physics body, not kinematically pinned — gravity −0.08,
  drag, and a capped normal-force lift toward preferred height
  (average grounded-leg target height + bodyHeight, raycast-clamped),
  scaled by the fraction of grounded legs, so a spider with stranded
  legs sags naturally. Pitch/roll derived purely from corner-leg
  geometry, low-pass filtered (0.3). Stairs "just work" emergently.
- **Reference constants:** recorded in the S1 research report (kept in
  the workflow record); we will derive our own tuned per-rig values and
  cite the reference figures as starting points only.
- **LICENSE:** no LICENSE file, no SPDX. README grants: commercial and
  non-commercial use of plugin and source permitted; attribution
  appreciated, not required; "do not resell without making substantial
  changes." Custom-permissive. Technique extraction + original
  re-implementation is unambiguously permitted; we copy no code
  regardless (architecture doesn't transplant: display-entity plugin vs
  modded entity renderer).

## 2. Our architecture today (D2 + B3 + E4 facts the design builds on)

- `RenderSpiderRobotInfo` is a plain data bag owned by the ENTITY,
  **written client-side only** (`updateLegs` in the client tick), read
  by the models via volatile display fields. The models consume
  **angles** (`ydisplayangle`, `uddisplayangle`, `p1x/p2x/p3xangle`) and
  pose ONE shared leg-part set N times (8 spider / 6 ant). Segments:
  spider 3×99 px, ant 3×49 px; rest pose +45/0/−45; per-leg constants
  (hip offsets, neutral yaw `ymid`, swing range `yrange`, pairing) are
  switch tables in the entities.
- The D2 scheduler: alternating pairs, combined ticker age > 50 steps
  the longer-planted foot; relocation also triggers on distance/yaw/
  elevation windows. All server-invisible. The SpiderRobot's grass
  trample fires client-side on foot-land while ridden (faithful to the
  original's client-side quirk).
- **SpiderRobot has NO ridden movement path** — no `tickRidden`, no
  `getControllingPassenger`. A mounted player cannot steer; only a
  mounted SpiderDriver moves it (by directly setting its velocity). The
  B3 wave never reached it. AntRobot HAS the full B3 `tickRidden` (E4:
  hover, obstruction climb, yaw lag, double integration).
- Hitboxes: single AABBs — spider 2.0×1.5, ant 2.75×1.25, MISC, no
  natural spawns (eggs/kits/structures/EasterBunny only).
- Three multi-part patterns exist: King/Godzilla **manual
  OreSpawnPartEntity** (ctor-built parts, hand positioned per tick,
  `hurtFromPart` multipliers, parent unpickable full-size); Queen
  **MHLib bone-sync** (profile JSON parts, GeckoLib client bone
  collection → packets → `alignSynchedSubParts`; hardened by
  OPT-001/002/003 but still client-trusting); the faithful **sidecars**
  (independent gaze-tracking entities — wrong tool here).
- MHLib core is renderer-agnostic: parts are built from
  `hitbox_profiles/*.json` in the LivingEntity ctor mixin, and
  `MHLibPartEntity.setPos` / `setPositionAndRotationDirect` are public.
  `hurt` routes to `IMultipartEntity.hurt(part, ...)`. The client-trust
  problem is specifically the GeckoLib bone-collection path, which we
  will NOT use.
- Crosshair HUD (C8): resolves `mc.crosshairPickEntity` then a 16-block
  ray, requires `LivingEntity`, then `instanceof` chains. **Aiming at
  any PartEntity blanks it today** (the King's bar already disappears
  at full size). No PartEntity unwrap exists.
- Config: no enum precedent; `ModConfigSpec.Builder#defineEnum` in the
  `tweaks` push is the clean first use (TOML serializes as a string —
  the requested `classic|modern` shape; live-settable in tests).

## 3. Design decisions

### D1 — Server-authoritative solve, deterministic client replay

The modern solver runs in the **server tick**. Rationale: (a) per-limb
hitboxes are server entities — feeding them requires server-side leg
positions, and the Queen's client-sourced path is the architecture
MOD-014 explicitly flags as weaker (OPT-003's hardening documented the
cost of trusting client bones); (b) GameTests run server-side — the
gait invariants in §6 are only testable against a server solver; (c)
the D2 path stays client-only, so classic mode is structurally
untouched.

Sync design — **step events, not per-tick positions**: the swing
animation is rate-based and deterministic given (leg, from, to,
startTick), so the server broadcasts compact step events plus low-rate
body-pose corrections; the client mirrors the interpolation locally and
renders through the same angle fields the models already consume.
Planted feet are world-anchored constants between events — zero
per-tick traffic for a standing spider (the OPT-002/003 change-only
philosophy applied from day one). A periodic keyframe (every ~2s or on
tracking-start) snaps drift.

### D2 — FABRIK with straighten-bias, solved in world space, emitted as
### model angles

Three fixed-length segments per leg (99/49 px = 6.1875/3.0625 blocks at
1/16 scale) solved by FABRIK (20 iter / 0.01 tol budget, cheap for a
3-joint chain) with the reference's pre-straighten knee seed (pitch bias
per rig, tuned in S2). The solver's world-space joint positions are then
converted to the EXISTING model angle fields (`ydisplayangle`,
`uddisplayangle`, `p1x/p2x/p3xangle`) — **the D2 models render both
modes unchanged**. No new renderers, no new textures: the asset audit is
structurally unaffected. The conversion (world joints → the models'
hip-relative yaw/elevation/segment-pitch conventions) is the highest-
precision-risk step and gets a dedicated render-parity harness in S2
(pose a known leg both ways, assert angle deltas < ε).

### D3 — Hitboxes: MHLib profile parts with a new server-side solver feed

Per MOD-025's "extend, don't invent a fourth": we use **MHLib parts
from a `hitbox_profiles/spider_robot.json` / `ant_robot.json` profile**,
but positioned by a new, third alignment feed — the server-side IK
solver calls `MHLibPartEntity.setPos` directly each tick (parts follow
the *lower leg segment* midpoint). The GeckoLib collection layer, bone
packets, and `alignSynchedSubParts` are not involved — this feed is
server-exact by construction and needs none of the OPT-003 machinery.
Rejected alternatives: manual King-style parts (re-implements what the
profile JSON + part machinery already provide: damage modifiers,
part-id blocks, save behavior); sidecars (independent surfaces, not
limb-tracking).

- **Granularity:** one box per leg (lower segment, ~0.6³ blocks) + the
  existing parent AABB. 8+1 surfaces (spider), 6+1 (ant). Two-box legs
  rejected for S4: 17 parts per spider is packet/entity bloat with no
  gameplay payoff; revisit only if leg-hit feel demands it.
- **Damage:** leg parts route ×1.0 to the parent (profile modifier) —
  no new weak points; total damage-in behavior identical to 1.0. The
  parent body REMAINS pickable (unlike the King): the body is the
  primary target; legs are additional honest surfaces. Projectiles and
  melee both resolve through the vanilla part hit-detection that
  `isMultipartEntity` enables.
- **Classic mode: zero parts.** The profile lookup is gated at
  construction (BOSS-017 `playNicelyShrunk` snapshot pattern): classic
  spiders never construct parts, so every 1.0 entity-count and AABB
  assumption holds bit-identically. (Typed `getEntities` queries never
  see PartEntities anyway — verified against the structure tests — but
  zero-parts-in-classic makes it unconditional.)
- **Crosshair HUD:** add a `PartEntity → getParent()` unwrap at the top
  of the overlay's resolution chain. This fixes the spider legs AND the
  pre-existing King blank-bar gap in one place. (Logged as the design's
  one deliberate touch outside the spider files.)

### D4 — Config gate

`OreSpawnConfig.SpiderMovement { CLASSIC, MODERN }` via `defineEnum`
(`spiderMovement`, default `MODERN`), `tweaks` section, the mod's first
enum config. Snapshot **at entity construction** (parts + solver choice
must agree for the entity's lifetime); a config flip affects newly
constructed/loaded entities, documented in the config comment. Classic
snapshot ⇒ D2 `updateLegs` client path exactly as shipped, no parts, no
server solver, no sync — the modern code is unreachable dead weight for
that entity.

*S2 amendment (independent review):* the config is COMMON (per-side
files, never synced), so the SERVER's construction snapshot is published
to clients on a synched entity flag and the client's own config is never
consulted — otherwise mismatched client/server files would leave client
legs frozen at full stretch in multiplayer.

### D5 — Gait scope for S2 (walk only)

WALK gait only in the first shipping slice: distance-triggered trigger
capsule (radius speed-lerped), adjacent-leg inhibitors, cooldown pair
(tuned per rig: 8-leg diagonal groups; 6-leg ant uses the same
even/odd/row indexing which degenerates correctly to a tripod-like
pattern). GALLOP deferred — see open question Q2.

## 4. Blast radius

| Surface | Impact | Handling |
|---|---|---|
| D2 solver / models | none in classic; modern writes the same display fields | render-parity harness (S2) |
| SpiderRobot riding | **no ridden path exists today** (pre-existing gap) | Q1 for owner — proposal: B3 tickRidden in modern only (S5) |
| AntRobot B3 hover ride | legs may not reach ground while hovering | stranded-leg dangle is the designed emergent answer; ride feel unchanged (hover physics untouched) |
| SpiderDriver steering | sets robot velocity directly | works identically under both solvers (solver consumes velocity, doesn't produce it) |
| Grass trample | client-side in classic (faithful); foot-land is server-side in modern | modern tramples server-side (same trigger, gamerule-gated); classic untouched |
| Spawn/despawn/kits/Wrench | keyed on the entity, not legs | untouched; kit round-trip test stays green both modes |
| Asset audit | no new renderers/textures | unaffected by design |
| Crosshair HUD | PartEntity unwrap (also fixes the King) | S4 |

**Suite pins (classic-mode) — tests that must stay green unchanged and
get an explicit classic pin where they exercise movement-adjacent
behavior:** `b2` stats table; `b3` driver armor + mounted bite;
`antrobot_melee_cadence`; `antrobot_ridden_stomp` (center-distance ring
— mode-agnostic, pinned classic anyway for determinism);
`i083_hitbox_size_table` (TYPE dims unchanged in both modes); DSB 48/49
counts; both hangout structure tests (exactly-1 typed counts — safe, see
D3); loot/recipe/config-gate tests (mode-agnostic). Pin mechanism: the
isolated-batch config idiom from BOSS-017 — pinned tests set
`SPIDER_MOVEMENT = CLASSIC` in an isolated batch and restore.

**New tests (modern mode, isolated batch):**
- *Gait invariants:* (1) no planted-foot slide — a planted foot's world
  position moves < ε between steps while the body walks a straight
  line; (2) all feet grounded within tolerance on flat ground within N
  ticks of spawn; (3) step cadence bounds — walking at max speed, each
  leg steps within [min,max] tick windows and adjacent legs never swing
  simultaneously; (4) stranded recovery — a spider walked off a cliff
  edge re-plants all feet within N ticks of landing.
- *Hitbox invariants:* (5) each part's center tracks its solver leg
  segment within ε for M ticks of walking; (6) damage to a leg part
  arrives at the parent at ×1.0; (7) classic-mode spider registers zero
  parts and matches 1.0 entity counts; (8) part positions are identical
  before/after a save-load cycle settles.

## 5. Slice plan

- **S2 — solver core + flat-ground walk + config gate.** FABRIK,
  angle conversion + render-parity harness, walk gait on flat ground,
  step-event sync, `spiderMovement` enum, classic pins, gait invariant
  tests 1–3. SpiderRobot only. *Exit: modern spider walks flat ground
  with no foot slide; classic bit-identical; suite green both modes.*
- **S3 — terrain adaptation + body dynamics.** Ground-scan grid with
  height bias, stranded legs, body height float + pitch/roll from leg
  geometry, stairs/slopes, cliff-recovery test 4, server-side trample.
- **S4 — multi-part hitboxes.** Profiles, the server-side feed,
  damage routing, HUD unwrap, hitbox tests 5–8.
- **S5 — ant variant + ride integration.** Ant rig parameters (6-leg,
  49 px, hover interplay), SpiderRobot modern-only B3 `tickRidden`
  (Q1: YES), full suite sweep, KNOWN_ISSUES/CHANGELOG/config docs
  (changelog frames classic as one-config-line parity preservation).

Each slice: full gate (build + asset audit + suite, exit-code-guarded),
FIX_LOG entries under "## 2.0 — Spider Overhaul", commit per slice.

## 6. Risk register

| Risk | Sev | Mitigation |
|---|---|---|
| Angle-conversion mismatch (world joints → model conventions) makes modern legs render wrong | HIGH | S2 render-parity harness before any gait work; the D2 angle semantics are documented in RenderSpiderRobotInfo |
| Sync drift between server feet and client replay | MED | deterministic rate-based swing + periodic keyframe snap; invariant test 5 measures it |
| Server solver cost (N spiders × 8 legs × FABRIK) | LOW | 3-joint chains converge in ≤ a few iterations; budget per-tick vector math ≈ trivial vs the OPT-004/006 scans we just removed; profile in S2 |
| Classic-mode regression via shared code touch | HIGH | classic path files untouched except the construction-time branch; suite pins; bit-identity review per slice |
| MHLib profile gating misfires (parts in classic) | MED | construction snapshot + invariant test 7 |
| Ant hover + gait interplay looks broken | MED | stranded-dangle design; S5 dedicated ride QA |
| Suite interference from config flips | LOW | isolated-batch idiom (established) |

## 7. Design rulings (2026-08-11) — design APPROVED as written

Approved including the default-modern override of MOD-014; S5's
changelog must frame classic as one-config-line parity preservation.

- **Q1 — SpiderRobot riding: YES.** Modern mode adds a B3-pattern
  `tickRidden` ground-walker path in S5. Classic keeps the faithful
  no-steer behavior exactly as shipped (the pre-existing 1.0 gap).
- **Q2 — Gait scope: walk-only** for 2.0. Gallop is a possible
  follow-up once walk feel is proven.
- **Q3 — Part granularity: one box per leg + pickable body.**
- **Q4 — Organic rigs (CaveFisher, EmperorScorpion): deferred to a
  later project.**

S2 execution order per the approval directive: the render-parity
harness comes FIRST — no gait work until the world-joint → model-angle
conversion is proven within ε. If the conversion fights, stop at the
harness and show the owner the mismatch rather than tuning blind.

---

*S1 research inputs: reference-technique report (TheCymaera/
minecraft-spider, read 2026-08-11, license recorded §1) and the D2/B3/
suite architecture survey — both preserved in the session workflow
records; key facts restated above with file:line citations available in
the survey.*
