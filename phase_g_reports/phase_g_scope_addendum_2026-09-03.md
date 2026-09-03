# Phase G scope addendum — owner rulings of 2026-09-03

Recorded verbatim in intent; executed at the slices named. Sequencing is unchanged: the owner's
in-game look session, then the port-wide mirror drop (BUG-041), proof regeneration, the G2
root-order slice, then Slice 4c. Where an item names a later slice, it is a standing instruction for
that slice, not a task for now.

## A. Motion policy: two motion sources per species

1. **Artist animations are a 2.0 feature behind the modern config.** Classic mode stays code-driven
   parity (the Amendment-1 motion policy: `setupAnim` math, or its exact keyframe transcription where
   the harness proves it). The same renderer carries both motion sources per species and the modern
   config selects the artist source. **No parity proof applies to artist clips; their acceptance is the
   owner's in-game look.** The harness legs (geometry, animation, visual) keep guarding the classic
   source only.
2. **Standard animation contract** — designed with the first Tier-2 slice and wired once in the shared
   controller: `idle` / `walk` / `swim` / `fly` selected by locomotion state; `attack`; `hurt`; `death`;
   `aggro_idle` / `calm_idle` selected on the attacking state; optional `idle_alt_N` clips chosen at
   random; controller-side speed scaling (the salvaged gait-scaling mechanism). Mob-specific extras are
   named per SPEC. **The contract is presented for ruling before anything is wired.**
3. **Handoff package additions** (G2 / G5): a `.bbmodel` per mob; a character-sheet paragraph per mob
   written for an animator (what the creature is, how it moves, what its attacks look like); a bone
   glossary with readable labels beside the locked legacy names; and a mechanically generated trigger
   inventory per mob derived from its AI goals and state flags (which states exist, what fires each
   clip).
4. **Pilot handoff** — Beaver plus one boss — as soon as the contract and the package format exist,
   serving as the G5 round-trip test before the rest of the package is built.

## B. Slice 5 design questions (added to the existing server-side-evaluator question)

5. **A plausibility bound on client-reported bone positions** for any trust-client path: the server
   must reject or clamp positions that cannot follow from the entity's pose, scale and animation
   envelope (the Queen's synced parts are the live case; BUG-042/043 showed how far the client path
   can drift unnoticed).
6. **MoreHitboxes (DarkPred, `morehitboxes`, MIT, 1.21.1-1.9.4-alpha, GeckoLib optional >= 4.5.1)
   compared against the vendored MultiHitboxLib, feature by feature.** Owner's clarification, same
   day: this is not a migration decision; MHLib stays. The goal is to identify what MoreHitboxes does
   better and port those pieces into the vendored MHLib under MoreHitboxes' MIT license with
   attribution, choosing the most performant design for each. Running both libraries is off the
   table unless the bytecode shows their mixin targets do not collide. The report is structured per
   feature — trust model, server-side part placement, attack boxes, culling bounds, projectile and
   melee hit detection, network sync — and states for each how MoreHitboxes does it, how MHLib does
   it, which is cheaper per frame, per tick and on the wire, and the cost to port. It lands under
   `phase_g_reports/morehitboxes_evaluation.md`. The harvest itself is a later slice, one piece at a
   time, each under the proof rule (MHLib changes keep two refuters) and re-accepted in-game where
   the Queen or the robots are touched.

## C. Standing rules and slice-bound tasks

7. **Spawn-100 benchmark before any Tier-2 slice:** classic versus candidate over the landed species,
   with a proposed regression threshold for the gate (a threshold is a ruling: proposed, not adopted).
   The baseline also measures MHLib's current cost on our side — client bone capture per frame (the
   collector layer's per-bone work), sync per tick and on the wire (packets, bytes), and server-side
   part placement per tick — so the MoreHitboxes comparison in item 6 carries real numbers rather
   than derived counts; the report names the counters to add and how to isolate them.
8. **GeckoLib per-entity cache eviction lands in the G2 root-order slice.**
9. **Proof rule:** geometry-only changes verified by the reference-geometry leg take ONE refuter;
   motion transcriptions, MHLib changes and renderer changes keep TWO.
10. **1.7.10 visual ground truth:** a Forge 1.7.10 instance in Prism with the original OreSpawn jar
    (`orespawn-1.7.10-20.3.jar`, supplied by the owner) is the reference for every in-game
    comparison; set up 2026-09-03 (see FIX_LOG for the instance record).
11. **Process hygiene:** background processes launched by subagents carry a timeout and are reaped
    when the agent reports; every session ends with a stray-process check before the report.
12. **Hitbox-library harvest sequencing (owner, later the same day):** BUG-044 (per-entity render-tick
    stamp, gametests for the hitch and two-Queen cases, two refuters) and OPT-028 (descriptor-exact
    `renderRecursively` selectors, the 220 → 110 `recursive_start` counter as proof) go before the
    owner's look session. The remaining harvests from the comparison — `Player.attack` part-to-parent
    unwrapping, conservative cull bounds, `defaultRequire = 1` — form ONE harvest slice scheduled before
    Slice 5. The comparison's other proposals (piercing ignore-list correctness, the fixed-layout binary
    bone payload, the attack-box data shape) stay unscheduled until ruled on.
13. **Config master (owner, same batch):** `modern.enabled` is a master override only — off forces every
    modern feature off, on defers to the existing per-feature keys, which keep their names and sections;
    new modern features register under `[modern]`. **Default true** (owner, 2026-09-04, second batch):
    by default the master defers to the keys and only forces classic when set false.
14. **Scanner (owner, same batch):** a write inside a non-evaluable branch is not provable; the renderer
    pin scanner reports it as PENDING for presentation and never assumes a branch.
15. **MHLib licensing (owner, same batch; closed 2026-09-04):** the exact LGPL version of upstream's
    LICENSE text (LGPL-3.0, no "or later") and the toml field's wording (All Rights Reserved) are recorded
    side by side; the LICENSE text governs and ships verbatim with the vendored sources and inside the
    jar; DerToaster is named in the mod's credits. Contact was not pursued: Modrinth and CurseForge offer
    no path (both project pages deleted), and the owner declined the e-mail route and discarded the draft.
16. **ENT-S-103 to ENT-S-107 (owner, 2026-09-04, second batch):** all parity bugs, fixed in classic; 106
    gets a parameterized test over all 38 original `isIgnoreable` callers and a changelog entry; 104
    restores the 1.7.10 fire behaviour and files a MOD proposal for a config-gated "fire respects
    mobGriefing" option; two refuters on 106, one on each of the rest.
17. **ENT-S-108 to ENT-S-113 (owner, 2026-09-04, third batch):** all parity, fixed in classic; generated
    tests per site where the pattern allows; two refuters on 108, one on each of the rest. **MOD-031**
    accepted as a modern option, default ON; classic stays 1.7.10.
18. **Targeting survey (owner, same batch):** a read-only lane compares every hunter's target selection —
    scan set, filters, gates (PlayNicely, creative, PEACEFUL, allies, ignore screen), tie-breaks — 1.7.10
    against the port in one ledger under `phase_g_reports`, presenting the split (parity bug / recorded /
    fixed). **From here the owner rules on batches, not singles:** divergences are grouped into proposed
    batches in the ledger and ruled on as batches.

## D. Rulings executed the same day (for cross-reference)

- ENT-S-098 fixed (shot BetterFireballs carry the mod's own type; save/load round-trip pinned) and its
  projectile-wide sweep filed under the same finding.
- MOD-029 accepted as the modern-mode default (Mothra 6x3 behind the modern config; classic 5x2).
- The renderer pin scanner's reassigned-local blind spot tightened; any pin that changed was presented
  before its gate.
- Kraken targeting divergences filed as a finding with the parity split.
- BUG-043 checked against upstream MultiHitboxLib; the version gap recorded.
