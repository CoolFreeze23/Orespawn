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
19. **Targeting ledger, ruled by wave (owner, 2026-09-04, fourth batch):** wave 1 now — T7 (PEACEFUL
    gates) then T1 (PlayNicely gates), "safety first"; wave 2 next — T3a, T2, T5, T6; wave 3 trails — T8,
    T3b, T3c, T4, T10. Generated pins wherever the pattern allows; one refuter on S/M batches, two on L; one
    changelog paragraph per wave. **T9:** port-only additions with a documented reason become MOD records
    behind the modern config; the rest are removed from classic; the split is presented with wave 2.
    **Targeting lanes never block the Phase G chain:** the look session, mirror drop, proof regeneration,
    the G2 root-order slice with cache eviction and 4c keep gate priority; targeting batches gate when there
    is room. Harness fixes of the same day acknowledged: the keeper-player and chunk-ticking waits are the
    right shape.
20. **After wave 1 (owner, 2026-09-04, fifth batch):** the i165 chunk-wait geometry fix (the FORCED ticket
    centred on the checked cell) approved as presented; **a harness slice for i050 and i127, scoped to root
    causes and test isolation** — mock-player placement, cross-test leakage, TEST-003 order sensitivity —
    **with no retries or widened waits as fixes, findings presented before changes**; ENT-S-116 (the two
    PlayNicely griefing gates) fixed in classic with one refuter; **refuter rule amended: counted by files
    touched — over twenty files gets two refuters regardless of the ledger's S/M/L label**; the wave-1 T1
    commit message (53 pins where the records say 56) stands, no amend; wave 2 starts with T3a and T2, the
    T9 split presented alongside (`phase_g_reports/targeting_t9_split_2026-09-04.md`).
21. **After wave 2's first half (owner, 2026-09-04, sixth batch):** (1) **IMob convention: `Mob` + `Enemy`,
    port-wide, wherever 1.7.10 tested IMob** — the Dragon's channel updated accordingly; (2) **target ownership:
    one convention for all hunters, chosen in T5 by which variant matches 1.7.10 in the measured cases**, the
    evidence presented with T5; (3) **T9 applied:** documented reason → MOD record behind modern, undocumented →
    removed from classic; the Girlfriend's safety gates stay in both modes as a recorded, deliberate parity
    exception; Phase 4E's six count as documented only if that phase's notes state intent; any removal that
    breaks a mob or makes it unsafe is flagged, not applied; (4) **harness slice: the ranked fixes that are pure
    isolation are applied** — flag restoration in a finally, batch separation, spacing — no widened waits, no
    retries, a before/after per fix in the record; (5) ENT-S-119 and ENT-S-123 join the T5/T6 wave; **ENT-S-120
    and ENT-S-121: the 1.7.10 convention adopted port-wide, not per site**; **ENT-S-122: reproduced if it has a
    player-visible signature, otherwise recorded as deliberately not reproduced with the rationale**; (6) next:
    T5 and T6.

22. **The night set (owner, 2026-09-04 night, through the advisor; the complete set — nothing earlier pending):**
    (1) **Doctrine, restated for the records:** the port is also a modernization to the 1.21.1 engine. The law is
    1.7.10 behaviour unless a record says otherwise: OreSpawn's own contribution stays exact and the engine's part
    is the modern engine's (PARITY_NOTES PN-012 villages, PN-016 ore tags). An engine-frame difference is
    reproduced only when it has a player-visible signature (the ENT-S-122 test); otherwise it is recorded as a PN
    entry, not coded. Old-engine accidents are PN entries in both modes, no key. Gameplay improvements live behind
    `[modern]` keys as MOD records. (2) **Push:** the 15 wave-2 commits after e35329a pushed first (origin/master
    2e21008); nothing else landed before the push confirmed. (3) **T9 / ENT-S-125 ratified:** the Mantis's inert
    goals removed from both modes; MOD-032..035 default ON; the five engine PEACEFUL rows MATCH (engine, P6); the
    commit-message reading of Phase 4E stands. (4) **MOD-033 extended, one batch:** the same owner / tame goals on
    Hydrolisc, VelocityRaptor, Boyfriend, Girlfriend — and Camarasaurus if it carries them — gated under
    `petsDefendOwner` (registered only in modern; classic = orig's target tasks), and Leon's tame predicate under the
    same key now that ENT-S-124's refutation has closed; pins per site where the pattern allows; refuters by files
    touched; MOD-033, KNOWN_ISSUES and the changelog note updated. (5) **Harness:** F1 approved as presented — the
    i127 rework (structural no-ranged-goal pin on the trooper, the acid negative scoped to the trooper's own acid,
    per-tick minion cull, the cow kept alive for the window), one refuter, before/after in the record; F5 (the
    `buildNow` RandomSource seam) stays open. (6) **The mock player's 60-tick spawn shield** is never cleared
    helper-wide: rows that pin a hit on a fresh mock player clear it in the row, as T5 did; the pattern recorded once
    in the harness notes (`phase_g_reports/harness_slice_2026-09-04.md` F0.6) and the observation under AUDIT
    TEST-003. (7) **ENT-S-092, the Queen:** no amendment to the re-tune; the hittable air is judged at the F3+B look.
    (8) **Wave-2 follow-ups:** the re-assert rows (Robot3 :127, Robot4 :175, Robot5 :114, EntityLeon :515, the Water
    Dragon's forget) re-rated DIVERGES and fixed with the `RevengeGoal.release()` shape as one follow-up batch, T5b,
    generated pins, refuters by files touched; ENT-S-126 a parity bug in classic — orig's write-before-test retarget
    order in Spyro and Stinky with the boxed-in pin — rides with T5b; ENT-S-127 reproduced — the refusal has a
    player-visible signature (a tamed Dragon, Leon or Prince attacking a creeper): `Enemy && !Creeper` on the four
    vanilla-task goals through one helper, one pin per site, rides with T5b; the Ender Knight's dropped stare ray
    (orig EnderKnight.java:92) a parity bug in classic, carried by its scan-set / filter rows, fixed with wave 3's
    T8; Chainsaw.java:137 (the air-only MyCanSee voxel walk mapped to the vanilla ray) filed as a finding with the
    two rays' answers compared on the felling cases, ruled when presented; the companion goals' engine ring (targets
    whose centres lie inside the box inflation but beyond the range test) has no player-visible signature — a PN
    entry (PN-020), deliberately not reproduced, no code; deepslate coal: the Stinky's `isCoalOre` accepts
    `BlockTags.COAL_ORES` in BOTH modes — the modern engine split one ore into two blocks, the mapping the ore
    generator uses — a PN entry of the PN-016 shape, pinned with a deepslate row beside the coal row (rides with T5b,
    the batch that holds the Stinky). (9) **ENT-S-120 — the scope ruling on the census** (the census's narrower
    recommendation superseded): ONE helper (`OrigPos`: y / blockY / distSq / dist / dy as proposed) applied wherever
    the old frame carries OreSpawn's intent toward another entity — class (b) entire (flight targets, owner hover,
    projectile aims, the up/down, ring, hold and band tests, the four feet-ray endpoints) and class (c) §3.2 (spawns
    and effects at another entity's posY); never per site. Not reproduced — one PN entry of the PN-012 shape citing
    the census's §0 table: class (a) entire; hunt reach and range, right-click interact ranges, owner distances,
    sorters and the misc rows keep the modern engine's frame (feet) in BOTH modes with OreSpawn's numbers exact; no
    modern key for reach in either direction. Old-engine accidents — PN entries in both modes, no keys: the class (c)
    §3.1 reverse list entire (the player's own posY as an origin): Bertha/Slice, ItemRayGun, ItemThunderStaff and
    ItemSquidZooka keep today's eye-level launch; Robot2 :142 and ItemNetherLost :36 keep the port's working
    behaviour; InstantShelter and InstantGarden keep the ground-level floor; vanilla-owned code (FollowOwnerGoal /
    OwnerFollowAnyNavGoal, the vanilla NearestAttackableTargetGoal picks, getNearestPlayer, the fishing hook,
    container reach) is the engine's — the same PN entry. Two gated slices: (a) helper + generated pins + class (b)
    with the ray endpoints; (d) the §3.2 spawn / effect sites, including the two EmperorScorpion / TrooperBug spawn
    lines the census could not locate; refuters by files touched; one changelog paragraph for the lane; no other
    lane edits the same hunter files while a slice is open. Effect, for the record: flyers hover and target one
    block higher over players; projectiles aim at the head (+0.25 / +0.55 / +0.75) or, at the h/2 sites, above it, as
    in 1.7.10; the Molenoid tunnels UP toward a level player; the Kraken holds a player 16.62 below itself; the
    follow band, the feet-ray endpoints and the effect centres move with the player's eyes; reach against a level
    player stays 0.2–0.5 blocks longer than 1.7.10's (the Irukandji 1.73 vs 0.61) and right-click reach stays 4
    rather than 3.66 — recorded, not coded. The Robot3 / Robot5 `+ h*0.5` aim drift and FairySword `+1.0` / RatSword
    are separate formula divergences — filed as findings, fixed in classic inside their slice, never silently;
    `blockY` keeps the `(int)` cast as orig, the negative-y edge documented, no floor; pins as proposed for the
    reproduced classes — one generated class per shape, a survival mock player as prey, a pig control, every flip
    restored in a finally. **Supersession (owner, the same night, follow-up):** these ENT-S-120 items (the night
    set's 15–22) supersede the evening set's items 5–10 (2026-09-04 evening, through the advisor, recorded in
    `phase_g_reports/ADVISOR_HANDOFF.md` under "Owner rulings of 2026-09-04, evening": the port-wide scope with four
    gated slices (a)–(d) at two refuters each, reach against players faithful to 1.7.10 in both modes, the effect
    statement, MOD records for Robot2 :142 and ItemNetherLost :36, and a `projectilesFromEyes` modern key for the
    four above-the-head launch sites). The evening ruling stays in the record as written and is superseded, not
    rewritten — the way Amendment 1 superseded the motion-policy ruling; one ruling stands, this one. (10) **Order of
    the remediation lane after the push:** the MOD-033 extension → T5b with the three follow-ups of (8) → ENT-S-120
    (a), then (d) → wave 3 (T8 with the Ender Knight, T3b, T3c, T4, T10); the Phase G chain keeps gate priority
    throughout. (11) **Blockbench** (ruled earlier, never sent): no general integration; a round-trip check (import →
    export → semantic diff, bone order preserved) goes in the artist-package slice if it fits under a quarter slice;
    the owner hand-checks the first `.bbmodel`. (12) **Records and housekeeping, docs-only, before the mirror drop:**
    the ENT-S-129 entry's scripting line removed; (6) and the ring entry recorded where they belong; the mirror-drop
    counts reconciled in FIX_LOG before the drop lands (89 models / 3,122 calls reported against 82 mirrored of 87,
    78 mirror-only, EnderReaper's 66 landed, 81 remaining recorded); README.md:125 (no contact was made; the LICENSE
    text governs), FIX_LOG's literal-N wave-2 gate line (661), ENT-S-123's copied refutation paragraph, KNOWN_ISSUES'
    "192-test" line, the AUDIT_FINDINGS "REPORT ONLY" and MODERNIZATION_NOTES "nothing here is implemented" headers
    and the targeting ledger rows :611 / :704 / :710 / :1196 fixed; `phase_g_reports/ADVISOR_HANDOFF.md` is the
    advisor's — tracked and committed unchanged whenever it changes, never edited; `Claude outputs/` at the root is
    the desktop app's mirror, excluded via `.git/info/exclude`, deleted by the owner.

## D. Rulings executed the same day (for cross-reference)

- ENT-S-098 fixed (shot BetterFireballs carry the mod's own type; save/load round-trip pinned) and its
  projectile-wide sweep filed under the same finding.
- MOD-029 accepted as the modern-mode default (Mothra 6x3 behind the modern config; classic 5x2).
- The renderer pin scanner's reassigned-local blind spot tightened; any pin that changed was presented
  before its gate.
- Kraken targeting divergences filed as a finding with the parity split.
- BUG-043 checked against upstream MultiHitboxLib; the version gap recorded.
