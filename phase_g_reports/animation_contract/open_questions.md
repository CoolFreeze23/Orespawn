# Open questions for the owner — slice (e), the animation contract and the controller's return

Each with a recommendation and the cost of each option. Numbers are from
`headless_demo/demo_results.json`. Nothing is wired until ruled.

## 1. Config key shape

- (a) **One master key `[modern] artistAnimations` (default ON) + a string-list exclusion
  `classicAnimationSpecies`**, species self-gated by clip presence (a species without an `idle`/`walk`
  stays classic) — *recommended*. Cost: two keys, one MOD record, one helper; per-species flips are list
  edits; a flip reaches an entity when its manager is rebuilt (render-distance re-entry, F3+T, re-login).
- (b) One boolean key per species. Cost: ~70 keys for Tier 2 alone; the master comment's key list becomes
  unreadable; same snapshot semantics.
- (c) Live per-frame read with dynamic controller add/remove on a flip. Cost: a per-frame config read per
  entity, controller lifecycle code that GeckoLib does not have (`AnimatableManager.addController` exists,
  but the hook gate and the controllers must switch atomically), and a new class of bugs; no user-visible
  gain beyond an instant flip.

**Ruled (owner, 2026-09-06, addendum item 24 (5)-(14)):** (a) — one master key `[modern] artistAnimations` (default ON) plus the string-list exclusion `classicAnimationSpecies`; a species is self-gated by clip presence (an empty clip file stays classic).

## 2. Transition policy

- (a) **Weight-blended locomotion layers (additive, 5-tick ramps) + GeckoLib's native blend-in for
  triggered clips + bone-reset blend-out (3 ticks) with the locomotion re-added; triggered clips REPLACE
  the gait on the bones they animate** — *recommended*. Cost: the additive variant of the controller
  (measured composition error 5.2e-8 rad), a weight mask; the artist authors attack clips as full poses.
- (b) Same, but triggered clips ADD to the gait. Cost: none in code; artists must author attack clips as
  deltas from idle, which Blockbench does not preview naturally.
- (c) Hard cuts everywhere (GeckoLib's native behaviour on a phase-locked controller). Cost: visible pops
  at every state change; simplest code.

**Ruled (owner, 2026-09-06, addendum item 24 (5)-(14)):** (a) — weight-blended locomotion layers, GeckoLib's native blend-in for triggered clips, the bone-reset blend-out with the locomotion re-added; triggered clips REPLACE the gait on the bones they animate (ruled with 13).

## 3. `death` clip vs the vanilla death flip

- (a) **Keep the flip; no `death` clip in the pilot** — *recommended for the pilot* (it is what the owner's
  look accepts today; zero renderer change).
- (b) A `death` clip replaces the flip: a third `applyRotations` mode ("living, no death flip") for
  species whose JSON has `death`. Cost: ~15 renderer lines, two refuters, the artist animates the fall.
- (c) Both: the clip plays under the flip. Cost: none; looks odd (a falling body doing a death pose).

**Ruled (owner, 2026-09-06, addendum item 24 (5)-(14)):** (a) for the pilot — the vanilla flip kept, no `death` clip; the later rule is (b) when a species' JSON ships a `death` clip (the Queen's precedent decides its shape).

## 4. `hurt` clip vs the red overlay

- (a) **Keep the overlay always** — *recommended* (vanilla's cue; every mod expects it; zero change).
- (b) Suppress the overlay while `hurt` plays (`getPackedOverlay` override, one line + a manager flag).
- (c) Per species in SPEC. Cost: one more SPEC field; the validator must check it.

**Ruled (owner, 2026-09-06, addendum item 24 (5)-(14)):** (a) — the red overlay kept always.

## 5. `idle_alt` cadence

- (a) **One roll per idle loop boundary, p = 0.15, uniform choice, only while idle weight > 0.9 and no
  triggered clip plays; keyed on the clip-clock cycle index** — *recommended*. Cost: a float and an int
  per manager.
- (b) A timer (every 5-10 s). Cost: a per-manager timer that must be clip-clock based to survive the dedup.
- (c) None in the pilot. Cost: nothing; the feature waits.

**Ruled (owner, 2026-09-06, addendum item 24 (5)-(14)):** (a) — one roll per idle loop boundary, p = 0.15, uniform, only while the idle weight > 0.9 and no triggered clip plays; keyed on the clip-clock cycle index.

## 6. How many extras a SPEC may add

- (a) **Cap of four per species without a ruling; more by ruling** — *recommended*. Cost: none.
- (b) Unlimited, each entered in the trigger inventory. Cost: uncontrolled growth of per-species code
  (each extra with a code trigger is an entity edit).
- (c) None until the pilot lands. Cost: the boss pilot likely needs at least one.

**Ruled (owner, 2026-09-06, addendum item 24 (5)-(14)):** (a) — a cap of four extras per species without a ruling; more by ruling.

## 7. Tier-3 species and artist clips

- (a) **No artist clips (Amendment 1 point 1 stands); their JSON stays empty** — *recommended*.
- (b) Allow extras only (e.g. a hover bob on the Elevator). Cost: a code-driven species would need the
  dual-source gate too; conflicts with "Tier-3 rigs code-driven".

**Ruled (owner, 2026-09-06, addendum item 24 (5)-(14)):** (a) — no artist clips for Tier-3 species; their JSON stays empty.

## 8. ENT-S-147 shape for per-render side effects

- (a) **The register's `onRenderPass(entity, partialTick)` descriptor hook, called from `preRender`
  when `!isReRender`, with the pose kept pure and re-applied when the pass was deduped** — *recommended*
  (it is the only shape that reproduces the classic cadence). Cost: renderer plumbing, two refuters,
  a before/after on the pause screen. The keyframe controllers need nothing from it.
- (b) Leave the quirk (the Rotator freezes behind the pause menu). Cost: a recorded divergence.

**Ruled (owner, 2026-09-06, addendum item 24 (5)-(14)):** (b) — the quirk is recorded, not reproduced: PN-023, ENT-S-147 closed; no renderer plumbing is spent on what sits behind the pause menu.

## 9. GeckoLib's `catmullrom` spline arguments: repair at load?

- (a) **Repair the arguments in `OreSpawnGeoReplacementModel.getAnimation` (P0 = the key before the
  segment start, periodic for loops) and report it upstream** — *recommended*. Effect: the reference
  leg needs 15 / 13 / 8 keys per bone instead of 54 / 41 / 19; artists' catmullrom clips play as
  Blockbench previews them. Cost: ~60 lines, a per-`Animation` cache, two refuters (it changes what every
  clip's `catmullrom` means in-game — a deliberate divergence from the library, to be recorded); the
  owner's look on the Beaver decides whether it is visible.
- (b) Ship linear at 54 / 41 / 19. Cost: 3.6-4.5× the keys; artists editing the reference clips see
  dense key rows; stock GeckoLib semantics kept.
- (c) Wait for an upstream fix. Cost: the pinned 4.8.4 does not change; nothing lands.

**Ruled (owner, 2026-09-06, addendum item 24 (5)-(14)):** (a) — the repair at load in `OreSpawnGeoReplacementModel.getAnimation` ONLY (the Queen's native model on stock semantics until her own ruling), a deliberate divergence from the library recorded with the javap cite and reported upstream, two refuters; the owner's look on the Beaver decides whether it is visible.

## 10. The density statement in the ruling record

- (a) **"2.5e-3 rad; Beaver reference leg: 54 / 41 / 19 linear keys per bone (gait / teeth / tail), wrap
  sample T−ε vs 0+ε included"** if question 9 is (b), or **"15 / 13 / 8 catmullrom keys per bone with
  spline arguments repaired at load"** if (a) — *one of the two, per the ruling on 9*. Either is an
  output of the harness and is re-derived on every re-transcription.

**Ruled (owner, 2026-09-06, addendum item 24 (5)-(14)):** the density statement reads "2.5e-3 rad; Beaver reference leg 15 / 13 / 8 catmullrom keys per bone with spline arguments repaired at load; wrap sample T−ε vs 0+ε included" — an output of the harness, re-derived on every re-transcription.

## 11. The `attack` trigger transport

- (a) **Per species by the trigger inventory: the client-observed EVENT-flag edge where the species'
  `DATA_ATTACKING` pulses at the strike; else the server `LivingDamageEvent.Post` → `triggerAnim` packet
  for melee; named launch-site calls for ranged** — *recommended*. Cost: one event subscriber, the
  packet per strike, one line per ranged launch site.
- (b) Server packets only. Cost: a packet per strike for every species; melee clips play at impact, not
  at the swing.
- (c) Client-observed only. Cost: species without a client-visible strike get no `attack`.

**Ruled (owner, 2026-09-06, addendum item 24 (5)-(14)):** (a) — per species by the trigger inventory: the client-observed event-flag edge where `DATA_ATTACKING` pulses at the strike, else the server `LivingDamageEvent.Post` → `triggerAnim` packet for melee, named launch-site calls for ranged.

## 12. `aggro_idle` for species without a synched attacking flag

- (a) **`calm_idle` only until the species' SPEC adds a synched byte mirroring `getTarget() != null`
  (classic-side, parity-neutral, one byte per change)** — *recommended*. Cost: an entity edit per
  species when its clips arrive.
- (b) Add the byte to every hunter now via a shared helper. Cost: ~50 entity edits in one slice, refuters
  by files touched (two).
- (c) Never: `aggro_idle` only for the 51 species with `DATA_ATTACKING`. Cost: an uneven contract.

**Ruled (owner, 2026-09-06, addendum item 24 (5)-(14)):** (a) — `calm_idle` only until a species' SPEC adds the synched byte mirroring `getTarget() != null`.

## 13. Locomotion selection: weights or discrete states

- (a) **Weights (`w_move = limbSwingAmount`, booleans ramped over 5 ticks)** — *recommended*; no
  thresholds to tune, no flicker. Cost: the additive controller (question 2).
- (b) Discrete states with hysteresis (enter at 0.05, leave at 0.02) and cuts or GeckoLib transitions.
  Cost: thresholds are rulings; visible pops or a relative-clock restart at each change.

**Ruled (owner, 2026-09-06, addendum item 24 (5)-(14)):** (a) — weights (`w_move = limbSwingAmount`, booleans ramped over 5 ticks); ruled with 2.

## 14. Authoring-length convention

- (a) **Normalise every loop to 1.0 s** — *recommended* (uniform; the SPEC states the tempo; the
  demonstration shows the length is free). Cost: every preview is off-tempo (gait 11.78×).
- (b) Author at the natural period where ≥ 0.25 s (tail 0.628 s), else 1.0 s. Cost: two conventions in
  one file.

**Ruled (owner, 2026-09-06, addendum item 24 (5)-(14)):** (a) — every loop normalised to 1.0 s; the SPEC states the tempo.

## 15. Blockbench preview at tempo

- (a) **The package tool exports a `<name>_preview.animation.json` stretched to the natural periods,
  for Blockbench only, never shipped** — *recommended*. Cost: a generator option; a validator rule that
  rejects `_preview` in the jar.
- (b) Accept the caveat with the SPEC sentence. Cost: none.

**Ruled (owner, 2026-09-06, addendum item 24 (5)-(14)):** (a) — the package tool exports a `<name>_preview.animation.json` stretched to the natural periods, Blockbench-only, never shipped; the validator rejects `_preview` in the jar.

## 16. The pilot boss (addendum A.4)

- (a) **The Queen** — the live bone-synced Tier-1 case (`sync-with-model: true`), so the SPEC-locked
  rule is exercised on the pilot. Cost: the largest rig; the lock list is long.
- (b) A robot (`sync-with-model: false`, solver-fed) — exercises nothing MHLib-specific.
- (c) Godzilla / the King — Tier-1 with MOD-025 pending; the hitbox design is not there yet.

**Ruled (owner, 2026-09-06, addendum item 24 (5)-(14)):** (a) — the Queen is the pilot boss (`idle` plus one `attack` in the pilot); the Beaver is the other pilot.

## 17. The salvaged Beaver clip's sign (a finding, ruling only on disposition)

The salvaged `tools/g3_beaver_animation.py` output is inverted against the landed basis (2.827 rad
error; `controller_design.md` §12). Options: (a) **regenerate under the converter's rule (X = +classic°,
Y/Z = −classic°) when the harness's keyframe leg lands; the salvaged proof documents stay in the
snapshot as-is with this note** — *recommended*; (b) re-land the salvaged JSON with a sign flip now
(it is an asset nothing consumes: dead weight until the leg lands).

**Ruled (owner, 2026-09-06, addendum item 24 (5)-(14)):** (a) — regenerate the Beaver clip under the converter's rule when the harness's keyframe leg lands (item 15); the salvaged proof documents stay in the snapshot as they are, with this note.
