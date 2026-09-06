# The standard animation contract — specification for ruling

Phase G slice (e), design lane, 2026-09-06. Owner ruling 2026-09-05, scope addendum item 23 (8)(e);
addendum §A.2; FIX_LOG "PHASE G RULING — AMENDMENT 1" (:3908-3974) and its ADDENDA (:3976-3998).
**Presented for ruling before anything is wired. Nothing under `src/` changes with this document.**

Every "proposed" below is a proposal; every number quoted from the demonstration is in
`headless_demo/demo_results.json` (the exact commands in `records.md`). Rulings already binding are
marked **RULED** with their source; the rest is for the owner.

---

## 0. Standing premises (RULED)

| # | Premise | Source |
|---|---|---|
| P1 | Artist animations are a 2.0 feature behind the modern config; classic stays code-driven parity; one renderer carries both motion sources per species; no parity proof applies to artist clips (the owner's in-game look accepts them); the harness legs guard the classic source only. | addendum A.1 |
| P2 | Keyframes are the shipping path for every artist-facing tier (Tier 1 and 2); code-driven stays the harness reference leg. Tier 3 and the five `limbSwing`-distance models (CannonFodder, Island, IslandToo, Robot1, Robot5) are code-driven under any policy. | Amendment 1 points 1-2; Slice 4a scope note |
| P3 | Gait scaling: the clip is authored at full amplitude; the controller scales the gait bones' animated delta by `limbSwingAmount`. | Amendment 1 point 3 |
| P4 | Animation-leg tolerance 2.5e-3 rad, stated with keyframe density and lerp mode; density is an output — the fewest keys holding the tolerance; 73/bone was a conversion artefact. | Amendment 1 point 4; ADDENDA (2) |
| P5 | One clip per frequency group on parallel controllers, each at its natural period; never one single-period clip over a multi-frequency rig; the animation leg gains a wrap sample (T−ε vs 0+ε). | Amendment 1 point 5 |
| P6 | The time-warp ratio derives from the clip's DECLARED `animation_length`; an artist's length edit changes nothing in-game; the SPEC states the tempo. | ADDENDA (1) |
| P7 | Tier 1: part positions come from the server-side evaluator, not from what the client renders; per boss the SPEC states which bones carry parts and whether they are SPEC-locked or evaluator-served. | Amendment 1 point 6; Q3 |
| P8 | `modern.enabled` is a master override (default true) deferring to per-feature keys; new modern features register under `[modern]` in the MOD-029 / MOD-031 shape (a `master && key` helper, never a direct key read). | addendum C.13; `OreSpawnConfig.java:173-190, 511-526, 605-723` |

---

## 1. Vocabulary

- **Clip**: one named animation in the species' `<name>.animation.json` (Bedrock/GeckoLib format; `loop`,
  `animation_length`, `bones.<bone>.rotation|position|scale`, keys with `post` and `lerp_mode`).
- **Frequency group**: the set of bones that move at one natural period (Beaver: gait 3.7 rad/tick over
  `rff lrf lff rrf`; teeth 2.7 over `teeth`; tail 0.5 over `tail`). A locomotion loop is one clip PER GROUP
  (P5), so "the walk" of a species with three groups is three clips: `walk` (the gait group) plus the
  group clips the SPEC names (`walk_teeth`, `walk_tail`; §2.1).
- **Layer**: one controller instance in the species' per-entity `AnimatableManager`. Layers run in
  registration order (`AnimatableManager$ControllerRegistrar.build` → `Object2ObjectArrayMap`, insertion
  order; `AnimationProcessor.tickAnimation` iterates that map, 4.8.4 offsets 10-33).
- **Weight**: the per-frame scalar a layer multiplies its delta by (P3's mechanism generalised; §5).
- **Tempo**: the controller's clock, `ageInTicks × ω`, mapped onto the clip's declared length (P6). The
  artist never sets the tempo; the SPEC states it.
- **Delta**: what a layer adds to the bone: the clip's evaluated value minus the bone's bind rotation
  (GeckoLib writes `setRotX(value + initialSnapshot.rotX)`, `AnimationProcessor.tickAnimation` 323-341;
  the G1 leg defines gait proportionality as `bind + amplitude · (unit − bind)`,
  `tools/g1_render_parity.py:770-777`). Positions and scales are written absolutely (425-461); their
  deltas are the raw values.

---

## 2. The clip inventory

### 2.1 Locomotion loops (`loop: true`; one clip per frequency group)

| Clip | Role | Weight source (§3) | Groups |
|---|---|---|---|
| `idle` | standing, no target | `w_idle = (1 − w_move) · (1 − w_swim) · (1 − w_fly)` | every group the species idles with |
| `walk` | ground locomotion | `w_walk = w_move · (1 − w_swim) · (1 − w_fly)`; the gait group additionally × `limbSwingAmount` (P3) | gait group + SPEC-named group clips (`walk_<group>`) |
| `swim` | in water | `w_swim` | as SPEC |
| `fly` | airborne (flyer species only) | `w_fly` | as SPEC |
| `aggro_idle` / `calm_idle` | replace `idle` on the attacking state (§4.3) | `w_idle · w_aggro` / `w_idle · (1 − w_aggro)` | as `idle` |
| `idle_alt_N` (N ≥ 1, optional) | a random variation of `idle`, played once | chosen at an idle loop boundary (§4.4) | as `idle` |

Naming rule for group clips: the gait group's clip carries the bare name (`walk`); every other
frequency group of the same locomotion state is `<state>_<group>` where `<group>` is the SPEC's group
name (Beaver: `walk_teeth`, `walk_tail`, `idle_teeth`, `idle_tail`). A species with ONE frequency group
has only the bare names. The classic transcription of the Beaver (the harness reference leg) is exactly
the salvaged trio renamed: `gait` → `walk`, `teeth` → `walk_teeth`, `tail` → `walk_tail`; at
`limbSwingAmount = 0` the gait delta is 0, so today's classic "idle" IS `walk` at zero amplitude plus the
free-running teeth and tail — the artist's `idle` may differ (P1), but the reference leg's idle is that.

### 2.2 Triggered clips (`loop: false`; one controller, `triggerableAnim`)

| Clip | Trigger (§4) | Loop type | Blend in / out |
|---|---|---|---|
| `attack` | a strike (event) | `play_once` | in: GeckoLib transition (`transitionLength`); out: §5.3 |
| `hurt` | damage taken (event) | `play_once` | same |
| `death` | dying (event, terminal) | `hold_on_last_frame` | in: transition; never out |

Priority when several fire in one frame: `death` > `hurt` > `attack` (`AnimationController.tryTriggerAnimation`
replaces `triggeredAnimation` outright, 4.8.4 offsets 1-53; the last trigger wins, so the controller side
orders them).

### 2.3 Mob-specific extras

Named per SPEC (addendum A.2), in the SPEC's clip table with: name, loop type, trigger (a state flag or
an event from the trigger inventory), the bones it may touch, and its layer position (base or overlay).
Proposed cap: **four extras per species** without a ruling; more needs the owner (open question 6). No
extra may reuse a contract name.

### 2.4 Clip names are fixed; absence is allowed

A species' JSON may omit any clip. Fallbacks: `aggro_idle`/`calm_idle` → `idle`; `swim`/`fly` → `walk`
(then `idle`) with the state's weight; a missing `walk_<group>` → `idle_<group>` → nothing (the group's
bones hold bind); a missing triggered clip → no trigger registered (vanilla's own cue stays: the red
overlay, the death flip; §4). A species whose JSON has NO `idle` (nor `walk`) is not an artist species:
the classic source stays even under the modern config (§6.2). This is what lets the modern key default
to ON without flipping species that have no clips yet.

---

## 3. Locomotion state from the entity

All inputs are client-visible without a packet; they are read once per frame into a plain record
(`MotionInputs`, the `PoseInputs` shape: `subject`, `ageInTicks`, `limbSwingAmount`, `inWater`, `flying`,
`attacking`, `hurtTime`, `deathTime`, `swingTime`, `random`) by the replacement — so the classic leg's
harness can drive the same record headlessly if a species' classic source ever consumes it.

| Input | Source (client) | Notes |
|---|---|---|
| `limbSwingAmount` | `AnimationState.getLimbSwingAmount()` — the replaced renderer computes it as vanilla `LivingEntityRenderer.render` does (`walkAnimation.speed(partialTick)`, clamped to 1, 0 when dead or riding) | already the seam's input (`OreSpawnGeoReplacement.limbSwingAmount`) |
| `inWater` | `Entity.isInWater()` (the client evaluates fluid heights every tick); a SPEC may narrow to `isUnderWater()` | no flag needed |
| `flying` | species declared `locomotion: flyer` in SPEC AND `!entity.onGround()`; `FlyingAnimal.isFlying()` where a species implements it | OreSpawn's flyers steer by their own goals (`AmbientFlightGoal`, the moth/dragonfly/butterfly presets); `onGround` is synced to the client by movement, so no flag is needed. The trigger inventory (addendum A.3) lists the flyers mechanically (`FlyingMoveControl` / `noPhysics` / the flight goals). |
| `attacking` | the species' synched `DATA_ATTACKING` int where it exists (51 entity classes define `EntityDataAccessor<Integer> DATA_ATTACKING`; one `ATTACKING_TICKS`, one byte `ATTACKING`) via a pose-style accessor `int getAttacking()` (the `CaveFisherPose` shape) | vanilla `Mob.isAggressive()` (`MOB_FLAG_AGGRESSIVE`) is set by ZERO OreSpawn classes, so it is not a signal here; species without a flag: open question 12 |
| `hurtTime`, `deathTime` | `LivingEntity.hurtTime` (set to 10 by the client's damage event), `LivingEntity.deathTime` (incremented client-side while `isDeadOrDying`) | the fields vanilla's red overlay and death flip read; no flag needed |
| `swingTime` / `attackAnim` | `LivingEntity.swingTime` (from `ClientboundAnimatePacket`) | only 2 OreSpawn entity classes call `swing(InteractionHand)`; a weak signal, listed for completeness |
| `random` | `Entity.getRandom()` | the client entity's own `RandomSource`; not synced; visuals only |

### 3.1 Proposed thresholds and smoothing

- `w_move = limbSwingAmount` (continuous; no threshold needed for a weighted blend). If the owner rules
  for discrete selection instead (open question 13): moving = `limbSwingAmount ≥ 0.05` (release below
  `0.02`; hysteresis so a mob idling on a slope does not flicker). Vanilla's own `WalkAnimationState.isMoving()`
  is `speed > 0`, which flickers.
- Booleans (`inWater`, `flying`, `attacking`) are smoothed into weights with a ramp of **5 ticks (0.25 s)**
  per direction, per manager (a float per weight stored in the manager's extra data; advanced on the
  clip clock, so a deduped frame — ENT-S-147 — neither double-advances nor stalls it: the ramp is a
  function of `ageInTicks` since the flip).
- Priority among locomotion states: `fly` > `swim` > `walk`/`idle` (the weight products in §2.1).

---

## 4. Triggers

### 4.1 `hurt`

Client-observed: the rising edge of `hurtTime` (0 → 10). Stored per manager as the last-seen
`hurtTime`; an edge is `hurtTime > lastHurtTime`. No packet, no entity change; the same field vanilla
reads for the red overlay, so the clip and the overlay agree frame for frame. Interaction with the
overlay itself: open question 4.

### 4.2 `death`

Client-observed: `deathTime > 0` (first frame of dying). `hold_on_last_frame`. Vanilla/GeckoLib also
rotate the whole model 90° about Z over `deathTime` (`GeoReplacedEntityRenderer.applyRotations`
89-145; the `OreSpawnGeoReplacedEntityRenderer.applyRotations` javadoc cites the offsets). Whether a
`death` clip replaces that flip is open question 3: replacing it needs the renderer to skip the flip for
species that have a `death` clip (a third mode beside living / non-living in `applyRotations`, ~15 lines,
renderer change → two refuters); keeping it means the artist animates a body that is already falling
over.

### 4.3 `aggro_idle` / `calm_idle` (a state, not an event)

`w_aggro` = smoothed `attacking != 0` for species with the flag. The trigger inventory classifies each
species' flag as STATE (held while a target is engaged — the Alien sets 1 for the duration of its melee
goal, `Alien.java:230-239`) or EVENT (pulsed at a strike); only STATE flags drive `w_aggro`. Species
without a client-visible attacking state get `calm_idle` only until their SPEC adds a synched byte
mirroring `getTarget() != null` (a one-line classic-side entity edit; parity-neutral: it changes no
behaviour, only what the client can see; ~1 byte per change on the wire) — open question 12.

### 4.4 `attack` (an event)

Three transports, chosen per species by the trigger inventory:

1. **Server event, no entity edits**: a NeoForge `LivingDamageEvent.Post` subscriber: when the source
   entity is a replaced species → `GeoReplacedEntity.triggerAnim(attacker, "triggers", "attack")`
   (4.8.4: server branch → `GeckoLibNetworking.triggerEntityAnim` → `EntityAnimTriggerPacket` →
   `RenderUtil.getReplacedAnimatable(entityType)` → `manager.tryTriggerAnimation`; the salvage's
   `PINNED_BYTECODE_CITATIONS.md` §"Replaced trigger path" pins that order). Covers every melee species
   at the moment damage lands (after the wind-up, i.e. the clip plays at impact, not before it). Cost:
   one packet per strike per tracked client; the clip cannot anticipate the hit.
2. **Client-observed EVENT flag edge**: for species whose `DATA_ATTACKING` pulses at the strike, the
   rising edge (as `hurt`). Zero cost; fires when the server sets the flag, which for OreSpawn's goals
   is usually at the swing, before impact — the better-looking option where it exists.
3. **Named call sites** (SPEC extras): ranged species fire `attack` (or an extra such as `spit`) from
   the launch site — one `AnimationTriggers.fire(this, "spit")` call in the entity (a classic-side edit
   that only sends a packet; parity-neutral). The trigger inventory lists the launch sites per species.

Recommendation: 2 where the flag is an EVENT, else 1 for melee, 3 for ranged; the inventory is the
per-species decision record.

### 4.5 `idle_alt_N`

At each `idle` loop boundary (the clip clock's cycle index changes; the index is stored per manager
so a repeated frame — pause-screen dedup, shadow pass — cannot re-roll), while `w_idle > 0.9` and no
triggered clip is playing: roll `random.nextFloat() < p_alt` (SPEC, default **0.15**); on success pick
`N` uniformly among the species' `idle_alt_*` clips and play it once
(`RawAnimation.begin().thenPlay("idle_alt_2").thenLoop("idle")` — GeckoLib's stage queue handles the
return natively). Cadence is therefore one roll per idle period (Beaver at a 1 s idle: about one
variation every 6-7 s). Open question 5.

---

## 5. Controller-side speed scaling, layering and transitions

### 5.1 Speed scaling (P3)

The gait group's layer multiplies its delta by `limbSwingAmount` per frame; every other layer's
weight is a locomotion/aggro weight from §3. The SPEC declares `gait_bones` (the group scaled by speed;
the harness's `scaled_bones` for the classic transcription: Beaver `rff lrf lff rrf`). Non-gait bones
are never speed-scaled — the Beaver's teeth and tail chew and wag at rest, as in 1.7.10
(`ModelBeaver.setupAnim:97-101` has no `limbSwingAmount` factor). Demonstrated: the controller's gait
output at amplitudes 0 / 0.25 / 0.5 / 1 tracks the classic to 2.483e-3 rad at 54 linear keys per bone
(1,057,088 comparisons; `demo_results.json` B.linear confirmation) and the teeth / tail channels are
amplitude-independent (their error is identical across amplitudes).

### 5.2 Layering (proposed; the mechanism behind every blend)

GeckoLib composes nothing: each controller writes its bones absolutely, so two controllers on one bone
= the last registered wins (measured: 1.412 rad error against the sum of the layers,
`demo_results.json` G). The salvaged controller already evaluates its clip and collapses the result to a
constant point before GeckoLib writes it (`PhaseLockedKeyframeController.scale`); adding the bone's
accumulated delta (earlier layers' writes this frame — GeckoLib drains each controller's queues right
after its `process`, `tickAnimation` 87-410) to that constant makes the layer ADDITIVE. Measured:
walk × a + idle × (1 − a) composes to 5.2e-8 rad of the sum of the layers measured alone (G). Euler
angles add exactly for one axis (every classic OreSpawn locomotion channel is single-axis) and
approximately for small multi-axis overlays; the SPEC notes the approximation for a multi-axis extra.

### 5.3 Transitions

- **Between locomotion loops** (idle ↔ walk ↔ swim ↔ fly, calm ↔ aggro): weights, never cuts (§3.1 ramps;
  `w_move` is already continuous). The phase-locked clock never resets, so a blend between two loops of
  the same group is phase-coherent (both are driven by the same `ageInTicks × ω`).
- **Into a triggered clip**: GeckoLib's own TRANSITIONING blend from the bone's current snapshot to the
  clip's first key over `transitionLength` ticks (`AnimationController.process` 241-324,
  `BoneAnimationQueue.addNextRotation`); proposed default **3 ticks**, SPEC-overridable per clip.
- **Out of a triggered clip**: GeckoLib has no blend-out — when the clip stops, the bone-reset lerp
  returns each bone to BIND over `getBoneResetTime()` ticks (`tickAnimation` 590-904), not to the
  locomotion pose. With additive locomotion layers registered AFTER the triggered controller, the
  locomotion delta is re-added on top of the reset, so the visible result is a `boneResetTime` blend from
  the clip's last pose to the live locomotion pose. Proposed `boneResetTime` **3 ticks**. The
  interaction "attack adds to the gait" vs "attack replaces the gait on the bones it animates" is open
  question 2 (proposed default: replace — the locomotion layers zero their weight on the bones the
  playing triggered clip animates, ramped over the same 3 ticks).

---

## 6. Two motion sources per species (P1)

### 6.1 Mechanism

Same renderer, same model, same replacement; the per-entity manager decides:

| | classic (reference leg) | artist |
|---|---|---|
| `registerControllers` | registers nothing (as today) | registers the layers of §2 |
| `applyCustomAnimations` (runs AFTER the controllers: `GeoModel.handleAnimations` 268-284 then 287-292; demonstrated in `demo_results.json` E — the hook overwrites the controller's bone) | the classic pose (as today) | returns at once |
| gate | `manager.getAnimationControllers().isEmpty()` — the manager is one map lookup away from the hook (`animatable.getAnimatableInstanceCache().getManagerForId(instanceId)`, what `handleAnimations` itself does at 5-15) | |

The decision is taken once per manager (at `AnimatableManager` construction, which calls
`registerControllers`); it needs no new state and cannot disagree with itself. A config flip reaches
entities as their managers are rebuilt: on entering render distance (OPT-029 evicts on leave), on a
resource reload (F3+T rebuilds every renderer, replacement and cache), on re-login — the BOSS-017
"construction snapshot" shape, documented in the key's comment. The alternative (live per-frame read
plus dynamic controller add/remove) is open question 1.

### 6.2 Config key shape (proposed, MOD-029 / MOD-031 shape; open question 1)

```
[modern]
  # MOD-0NN: artist keyframe animations on the GeckoLib candidates. Only takes effect while modern.enabled
  # is true; classic mode always poses from the 1.7.10 formulas. A species without artist clips keeps the
  # classic pose whatever this says. Picked up by entities as they (re)enter render distance or on F3+T.
  artistAnimations = true
  # Species kept on the classic pose even in modern mode (registry names), the owner's per-species flip list.
  classicAnimationSpecies = []
```

`OreSpawnConfig.artistAnimations(EntityType<?>)` = `MODERN_ENABLED && MODERN_ARTIST_ANIMATIONS &&
!classicAnimationSpecies.contains(type)`; the master's comment and javadoc list the key as they list
the nine others (`OreSpawnConfig.java:511-526`, `:189-205`). One MOD record in MODERNIZATION_NOTES
(next number after MOD-037). Per-species boolean keys (one per species) are the alternative; with 70
Tier-2 rigs that is 70 keys — rejected in the recommendation.

### 6.3 Dev switch (unchanged)

`-Dorespawn.dev.geckolibRenderers=candidate` still selects the GeckoLib renderer at all (Q1, Slice 4a);
the artist key selects the motion source inside it. Classic renderer selected → no GeckoLib, no clips.

---

## 7. SPEC additions

Every `SPEC.md` (migration design §7) gains, generated by the package tool (slice (f)):

1. **Tempo table** — per frequency group: name, bones, `ω` (rad/tick), natural period in ticks and
   seconds, and the sentence the artist needs: "in-game, `walk` plays once every 1.698 ticks (0.085 s);
   your timeline's length is the SHAPE's timeline and does not change that" (P6). Beaver:
   gait 3.7 → 1.698 ticks; teeth 2.7 → 2.327 ticks; tail 0.5 → 12.566 ticks (`demo_results.json` D).
2. **Bone glossary hooks** — beside each locked legacy bone name: a readable label, its frequency group,
   `gait_bone: yes/no`, and for Tier 1 `locked: yes` with the reason (§8).
3. **Clip table** — the contract clips the species ships, each with loop type, layer (base/overlay),
   weight source or trigger, bones it may animate, transition length; the extras (§2.3).
4. **Trigger inventory link** — the mechanically generated table (addendum A.3): every state flag and AI
   goal of the species, classified STATE / EVENT, and which clip each drives (§4).
5. **Density statement** — for the classic transcription the harness verifies: "reference leg at
   2.5e-3 rad: gait 54 linear keys / bone, teeth 41, tail 19" (or the repaired-catmullrom figures if
   ruled; `controller_design.md` §7). This line is the harness's, not the artist's: artist clips carry no
   tolerance (P1).
6. **Preview note** — §9.

---

## 8. What an artist may and may not edit

| May | May not |
|---|---|
| The shape of any contract clip: keys, values, `lerp_mode` (linear / catmullrom / any GeckoLib easing), rotation / position / scale channels, any bone not locked | Bone names (verbatim legacy names; the glossary gives labels) |
| The clip's `animation_length` — it is the shape's own timeline (P6) | The tempo (the controller's; the SPEC states it) |
| Add `idle_alt_N` clips | Add or rename contract clips; add extras not in the SPEC |
| Edit an extra's shape | Change a clip's loop type (the controller's `RawAnimation` stage decides; the package validator pins the JSON `loop` per role because GeckoLib reads it, `BakedAnimationsAdapter.bakeAnimation` 28-33, and the salvaged prime refuses a mismatch) |
| Key any bone on a Tier-2 rig | Key a SPEC-locked bone on a Tier-1 boss (§8.1) — the validator rejects the clip, the controller excludes the bone |
| Use sound / particle keyframes (GeckoLib fires them through the controller's handlers; proposed: sounds allowed via a whitelist in SPEC, particles allowed) | Custom-instruction keyframes (no handler is registered; they would log a warning per frame, `processCurrentAnimation` 508-546) |

### 8.1 Tier-1 (MHLib) bones

The vendored MHLib profile (`data/orespawn/multihitboxlib/hitbox_profiles/<boss>.json`) declares
`sync-with-model`, `trust-client` and `synched-bones`; the Queen's is `true / true / [...]` (the live
bone-synced case), the robots' `false / false / []`. Under P7 a bone in `synched-bones` — and every
ancestor of it — is **SPEC-locked**: its part positions come from the server-side evaluator, so a client
clip on it would move the picture away from the hitbox. The controller registers no channel for a
locked bone; the validator rejects keys on it; the glossary marks it. When the server-side evaluator can
consume the artist clip itself (Slice 5's open question), the lock can lift per bone — that is a later
ruling, recorded in the SPEC as `locked: until evaluator`.

> **Superseded 2026-09-06 (owner item 18; FIX_LOG "PHASE G SLICE (f), ITEM 18").** The validator does NOT reject keys on a SPEC-locked bone: keying a locked bone is allowed and WARNED (the hitbox part follows the bone in-game, so the artist keeps such keys deliberate); renaming, re-parenting or deleting a locked bone is REFUSED. The §8 table's "the validator rejects the clip" and this section's "the validator rejects keys on it" read under that ruling; the controller's exclusion of locked bones and the `locked: until evaluator` lift are unchanged. A reject mode for keys stays available in the package tool (`check --lock-mode reject`) for the day the server-side evaluator lands.

---

## 9. The Blockbench preview caveat

Blockbench plays a clip at its declared length. In-game the controller plays it at the natural period
(P6). For the Beaver's gait that is 11.78 clip-ticks per game tick at a 1 s authoring length (D): a 1 s
preview shows the SHAPE at 1/11.78 of the in-game rate. The SPEC states the rate (§7.1). Two authoring
conventions are possible (open question 14): normalise every loop to 1.0 s (the salvaged convention;
uniform, but every preview is off-tempo), or author each loop at its natural period in seconds where
that is editable (tail 0.628 s previews at tempo; gait 0.085 s does not — 8.5 frames at 100 fps). The
package tool can also export a `_preview` copy stretched to the natural period for Blockbench only (open
question 15). Whatever the convention, an artist who re-times a clip changes nothing in-game (D: the same
shape at 0.5 s / 1 s / 2 s / 3.3 s poses identically to 0.0 rad).

---

## 10. Scope by tier

- Tier 2 (70 rigs, migration design §3): the full contract; the pilot is the Beaver (addendum A.4).
- Tier 1 (bosses): the full contract with §8.1 locks; the pilot's boss is open question 16.
- Tier 3 (13 rigs) and the five `limbSwing`-distance models: no artist clips (P2); their JSON stays
  `"animations": {}` as shipped today. Open question 7 records the option of allowing extras there.
