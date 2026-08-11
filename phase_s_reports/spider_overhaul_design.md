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

  *S3 as-built amendments:* the scan window is classic's own probe
  column (11 up / 14 down, findNewFooting:717) — shorter windows are a
  parity break, not a tuning freedom; reach CONTRACTION (0.7/0.45/0.25
  of rest reach, floored 3.5) precedes stranding, mirroring classic's
  16→3.5 sweep. **Body dynamics are a VISUAL layer** (the reference's
  physics body adapted to a vanilla Mob): deterministic shared state
  (lift PD spring with gravity and support-capped leg force; pitch/roll
  low-passed from planted corner-group centroids) drives a conjugated
  render transform `T = translate(lift) ∘ Ry(a)Rx(pitch)Rz(roll)Ry(−a)`
  (a = −yawRad) applied MODERN-ONLY in the renderer — CONJUGATED ABOUT
  the vanilla +1.501 model pivot (rotating about the bare anchor slid
  planted feet by (R−I)·c, the S3b review BLOCKER) — while the client
  solve targets inverse-transformed feet (with the production reach
  clamp) so planted feet stay motionless in world space under tilt.
  Proven in the tick domain by the hardened harness: math-pair identity,
  a JOML replay of the renderer's exact op sequence against the double
  math (the transcription check the first harness lacked), and the full
  clamp-mirrored compensation round trip with every grid cell accounted
  for. The renderer consumes RAW tick dynamics values (no partial-tick
  lerp — lerping against tick-solved angles sawtoothed feet up to ~1.7
  blocks) with per-tick rate limits keeping body stepping sub-visual;
  the residual render-domain caveats (vanilla body-yaw interpolation)
  are the classic-shared quirk family. Entity physics, hitboxes, gait
  triggers and scans all keep using the real body position — classic
  renders byte-identically. Sag emerges from the support-scaled force
  cap exactly as the reference intends; sag is attenuated while ridden
  until S5 reconciles the seat. **S4 note (review):** the server and
  client integrate independent dynamics copies (self-healing, gap
  halves ~every 2 ticks); server-fed parts must either tolerate
  ~latency+1 ticks of body-dynamics skew or the keyframe payload gains
  the four dynamics scalars. *Aim-offset composability (reference video
  notes, V3):* the tilt pipeline does NOT preclude the reference's
  ~30° aim leeway — pitch/roll are single scalars consumed symmetrically
  by the renderer and the foot compensation from one source, so a later
  bounded combat/look offset simply adds into the final values before
  both consumers pick them up; no S3b change needed. The COM/support-
  polygon gravity from the same notes is deliberately NOT retrofitted
  (design ruling: scalar lift is committed and stability-proven) — banked
  as MOD-026 with the gallop it was co-designed with.
- **S4 — multi-part hitboxes.** Profiles, the server-side feed,
  damage routing, HUD unwrap, hitbox tests 5–8.
- **S5 — ant variant + ride integration.** Ant rig parameters (6-leg,
  49 px, hover interplay), SpiderRobot modern-only B3 `tickRidden`
  (Q1: YES), full suite sweep, KNOWN_ISSUES/CHANGELOG/config docs
  (changelog frames classic as one-config-line parity preservation).

  *S4 as-designed (research complete 2026-08-11; implementation next
  session — recorded here so nothing is re-derived):*
  - **Gate mechanism:** SpiderRobot implements MHLib's
    `ICustomHitboxProfileSupplier` (its first implementor; the API
    explicitly anticipates dynamic suppliers and never caches them).
    Return `Optional.empty()` = zero parts (classic); return `null` =
    fall through to the OPT-001-cached datapack lookup (modern).
    **Ctor timing:** `mhlibOnConstructor` runs at LivingEntity ctor
    TAIL — before SpiderRobot's snapshot field assigns — so the
    supplier decides via: server = `movementModeDecided ? modernGait !=
    null : config == MODERN` (a ctor-end flag; ctor-time config IS the
    snapshot-to-be); client = the synced DATA_MODERN_GAIT flag, with a
    LAZY part build (`mhlibOnConstructor()` re-invoked once the flag
    arrives — partArray null-check guards double-build). Client parts
    are needed for crosshair/reach picking and are positioned locally
    from the replayed solve (no new packets).
  - **Feed:** serverTick (and clientTick for the local parts) computes
    per-leg world joints via the S3b-proven pipeline — inverse-
    transform the foot (reach-clamped), planar solve, forward-transform
    the joints — and `MHLibPartEntity.setPos` centers each part on the
    lower-segment (knee2→foot) midpoint. MHLib's own `alignSubParts`
    static alignment runs earlier in the same tick (aiStep TAIL, inside
    super.tick()) and is harmlessly overwritten by the feed — verified
    ordering, no MHLib alignment change needed.
  - **Vendored MHLib changes (as-built, all Queen-neutral —
    fix-review-verified per consumer):** (1) `isPickable :=
    (collidable || canReceiveDamage) && enabled` — pickability decoupled
    from hard collision (her parts are collidable:true, truth table
    unchanged); (2) the env-damage routing rule (drop source-less
    damage via parts iff the MAIN hitbox can receive damage — her main
    cannot, so she keeps full routing; explosions are safe on both
    profiles: even unowned TNT carries itself as direct source);
    (3) `syncWithModel()` gates on updateSynching's server AND client
    branches plus both tracking hooks (election/keepalive churn dead
    for boneless profiles); (4) `MHLibClientPartRegistration` +
    `AccessorClientLevel` (field javap-verified) for post-add client
    part registration. The per-tick `MixinServerEntity` part stream is
    DOCUMENTED as the pre-first-keyframe / non-mirrored-client fallback
    (the client mirror overwrites it; gating it is an S5 change-only
    candidate if profiling warrants — recorded, not ruled).
  - **Profile** (`data/orespawn/multihitboxlib/hitbox_profiles/
    spider_robot.json`, Queen format): sync-with-model false,
    trust-client false, synched-bones [], main-hitbox {collidable
    false, canReceiveDamage TRUE (body stays pickable + damageable —
    D3 law), size [2.0, 1.5] EXACTLY (classic dims — MHLib hooks
    EntityEvent.Size from the profile, so the size field must equal
    classic or dims tests break)}; parts leg0..leg7: collidable false,
    can-receive-damage true, damage-modifier 1.0,
    max-deviation-from-server 0, box multihitboxlib:aabb 0.6×0.6.
  - **Skew tolerance (design ruling, Option A — restated honestly after
    the S4 review proved the original ceiling wrong):** the keyframe
    payload does NOT grow, and server truth does not bend to client
    rendering (grace-clamp REJECTED). The contract: PLANTED legs — parts
    match the visual leg to ~1e-3 (dynamics skew self-heals, 2-tick
    half-life). SWINGING legs — parts follow the SERVER-TRUE swing
    trajectory; the client's rendered leg lags that trajectory by
    network latency for the swing's duration (~2.3 blocks per latency
    tick, bounded by swing amplitude). Bounded-impact rationale: legs
    route ×1.0, so a whiffed swing-leg shot costs the player nothing
    versus aiming at the body, and vanilla mobs carry the identical
    latency skew on their whole body. The invariant tests assert this
    restated contract (planted exact; swinging vs the server
    trajectory). Pre-approved fallback unchanged: four scalars on the
    existing 40-tick keyframe, never a per-tick channel.
  - **Ant deferral:** ant_robot.json ships in S5 WITH the ant's
    supplier + gait — a profile without a feed would give every ant
    (classic included, absent a supplier) static misplaced boxes,
    violating zero-parts-in-classic.
  - **HUD unwrap** (GirlfriendOverlay): unwrap `PartEntity.getParent()`
    BEFORE the LivingEntity checks — fixes spider legs AND the King's
    pre-existing blank bar; reviewers must sweep every part-bearing
    entity (King, Queen, Godzilla, sidecars).
  - **Tests 5–8:** (5) part centers track the test-side FK recompute of
    the lower-segment midpoints while walking (ε honest about the
    float domain; independence law — the test FK is the anchor, with
    bodyTransform's JOML replay as its validation); (6) equal damage
    through a leg part vs the body on twin spiders = equal health loss
    (×1.0); (7) classic zero parts + pickable parent + typed-count
    parity, modern 8 parts + parent STILL pickable; (8) NBT round-trip
    spider re-settles all parts within reach (gait state is transient
    by design, MOD-022 family — "identical positions" is settle-
    equivalence, not bit equality).
  - **BUG-036 found during this research** (fixed ahead of S4): MHLib's
    upstream demo profile `data/minecraft/.../creeper.json` shipped in
    the jar — every vanilla creeper had multipart hitboxes and an
    unpickable main box in public betas. Deleted; VanillaParityTests
    pins the no-vanilla-parts contract.

  *S5 as-designed (research complete 2026-08-11; implementation next
  session — zero re-derivation):*
  - **Ant ground truth (read from source this session):** per-leg
    tables (AntRobot.initLegData:610-620): legoff {0.75,0.75,1.0,1.0,
    1.15,1.15}, ymid {0.0, π, −0.7853982, 3.9269907, 0.7853982,
    2.3561945}, yrange ±0.2617994 (same magnitude as spider),
    pairedwith {1,0,3,2,5,4}, yoff all −0.75. Segments 49px =
    3.0625 blocks (AntRobot:744-746), MAX_REACH 9.1875. Classic probe
    windows: distance 144/22 px (9/1.375 blocks; AntRobot:732), yaw
    trigger ×8/6 (vs spider's 8/7), swing-bias factor 0.8 (:858, vs
    spider 0.875). REST reaches (probe opening values, :839,862-874;
    NOTE the leg 0/1 override runs AFTER the ≥4 branch): legs 0/1 →
    6.0, legs 2/3 → 9.0, legs 4/5 → 4.0. ModelAntRobot hip placement
    and chain advance (:242-245) are STRUCTURALLY IDENTICAL to the
    spider model (−cos(ymid)·legoff·16 / sin·legoff·16 / yoff·−16;
    −sin(xRot)·49) — the S2 conversion mapping (α_w = yawRad − α_m,
    yd = α_w − yawRad + π/2, ud=a2/p2=0 split) carries over UNCHANGED;
    the render-parity harness generalizes by rig and must run the ant
    grid too.
  - **Rig abstraction:** introduce `LegRig` (instance record/class:
    legCount, segmentLength, maxReach, hipRadial/neutralYaw/
    hipVertical/swingRange/pairedWith/restReach arrays, legBearing/
    hipX/hipZ/hipY/restFootX/restFootZ using the classic formulas) —
    SpiderRigProfile becomes the spider LegRig instance; AntRigProfile
    the ant's. ModernSpiderGait: SpiderRobot→Mob params + a LegRig
    ctor field; arrays sized rig.legCount(); solveLegAngles gains a
    rig param (harness signatures update); scan-window law
    (SCAN_UP/DOWN 11/14) is CLASSIC PROBE GEOMETRY for the spider —
    the ANT's classic probe scans a different column (verify
    AntRobot.findNewFooting's yScan loop and mirror ITS numbers in the
    ant rig — the law says per-rig probe geometry, not shared numbers).
    Gait tuning constants that scale with rig size (TRIGGER radii,
    STEP_SPEED, LIFT_HEIGHT, DANGLE_DROP, VERTICAL_RETRIGGER, tilt
    spans auto-derived already): move into LegRig with spider values as
    S2-S3 shipped and ant values scaled ~×(3.0625/6.1875) as the
    starting tune.
  - **Shared entity surface:** `IModernLeggedRobot` (getModernGait,
    isModernMovement, getRenderSpiderRobotInfo — both robots already
    use RenderSpiderRobotInfo) — payload handlers accept either robot
    type via the interface; keyframe decoder validates leg count in
    {6,8} and the HANDLER validates against the entity's rig exactly.
    Registrar version bumps (wire semantics change) per the standing
    rule. AntRobot: supplier + ctor-tail single-read + snapshot +
    onSyncedDataUpdated build (same S4 pattern, DATA accessor on
    AntRobot), classic updateLegs callsite branch, AntRobotRenderer
    modern-only tilt branch (same pivot conjugation), profile
    ant_robot.json: main EXACTLY [2.75, 1.25] (i083 pins these dims —
    the Size-hook trap), 6 legs leg0..leg5, same routing/lava rule.
    HOVER-RIDE INTERPLAY: tickRidden hover physics UNTOUCHED — a
    hovering body strands legs (scan window misses ground) and the
    stranded-dangle IS the designed look; the ant's dynamics sag floor
    while ridden applies as on the spider.
  - **SpiderRobot ridden path (Q1, modern-only):**
    getControllingPassenger returns the first PLAYER passenger IFF
    modernGait != null (classic: null — faithful no-steer; the
    SpiderDriver is never controlling in either mode and its
    velocity-set shoving coexists untouched). tickRidden ground-walker
    per the B3 pattern (yaw from rider, WASD travel at the 0.35
    attribute speed, jump ignored, step-height as-is); the gait needs
    no steering-specific changes (rest bearings rotate with yaw; the
    speed-lerped trigger radius already handles 0.35 = FULL_SPEED) —
    reviewers attack exactly that claim plus mount/dismount mid-swing
    and driver coexistence. RIDER SEAT RESOLUTION (S3b handoff): while
    ridden clamp body-dynamics lift AND sag to ±0.15 (near-rigid body
    so the real-pos-rendered rider stays coherent); test pins it.
  - **Server-side ride tests** (player-path law honestly applied:
    player-driven vehicle travel is client-controlled and untestable in
    gametests — assert the wiring: controlling-passenger truth table
    both modes, dismount mid-swing state consistency, driver-shove
    coexistence, ridden lift clamp; the FEEL is the owner's in-game
    session, recorded as exit evidence).
  - **Docs:** CHANGELOG 2.0 section framing classic as one-config-line
    parity preservation (ratified default-modern override), config
    comment final pass, KNOWN_ISSUES 2.0 notes (swing-leg latency skew
    tolerance in player terms; ant hover-dangle look).
  - **Parked ruling to present in the S5 boundary report:** the
    MixinServerEntity pre-first-keyframe part-stream gate (delta
    paragraph; owner ratifies or declines there — slice does not hold
    on it).

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
