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
    rewritten — the way Amendment 1 superseded the motion-policy ruling; one ruling stands, this one. **Closure (owner, 2026-09-05):** under law 11 the 1.7.10 server-side player's posY was the feet
    (`EntityPlayerMP.<init>` zeroes yOffset — `phase_g_reports/ents120_premise_2026-09-05.md`), so no listed site
    diverges; the night set's 15–22 and the evening set's 5–10 are withdrawn by this closure; `OrigPos` is never
    written; the census stays in the record with the premise note at its head; PN-020 and PN-021 stand. (10) **Order of
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

23. **Rulings of 2026-09-05 (owner, through the advisor):** (1) **Push:** the 17 commits after 2e21008 pushed
    (origin/master 814d151) before anything else landed. (2) **ENT-S-120 closed on the premise** — the closure line under
    item 22's supersession; `OrigPos` is never written; the census stays in the record with the premise note at its head;
    PN-020 and PN-021 stand (neither rests on the premise); no client-side census now — a client-only OreSpawn site that
    reads a player's posY is filed if it turns up in renderer or package work. (3) **Standing rule — law 11 for engine
    claims:** an engine-behaviour claim about 1.7.10 becomes a finding only after a law-11 check against the jar; a census
    or refuter observation whose premise is marked "recalled" is presented as unverified and receives no ruling until it
    is verified. The T3a refuter's ENT-S-120 observation and ITEM-070's ‡ rows are re-read under the server frame, a note
    each (AUDIT ENT-S-120 / ITEM-070). (4) **Mirror drop, scope:** the 112 calls in `ButterflyModel` (10) and the seven
    `client/model` item models (102) join the drop — pinned by the reference leg where a 1.7.10 pair exists, before/after
    captures in the proof set where none does; one refuter, geometry-only; the same commit. The go is still the Section B
    EnderReaper A/B. (5) **ENT-S-140:** the pose-sized player hitbox is the modern engine's and OreSpawn's sorter formula
    is exact — PN-022, deliberately not reproduced, no code, no refuters. (6) **Wave 4, one batch, HELD behind (8):**
    ENT-S-130, 133, 134, 137, 138, 142, 143, 144, 145 and ITEM-070's classic transcription — parity, classic; generated
    pins; refuters by files touched; one changelog paragraph. (7) **ITEM-070: B2** — the 1.7.10 air-walk transcribed in
    classic (shape A, the item's own code, its in-grass and in-water quirk included); the vanilla ray kept in modern under
    `[modern] chainsawSweepVanillaSight`, default ON, one MOD record; lands with wave 4. (8) **Sequencing while the look
    session is pending:** the chain's work that needs no look proceeds now, in this order, each its own gated slice:
    (a) per-entity GeckoLib cache eviction, measured with the MHLib counters; (b) Slice 4c — PurplePower and Rotator, the
    render-instance expansion and the clone-aware geometry leg; (c) the G2 root-order contract — bone draw order =
    vanilla part order from the 4.8.4 bytecode, the z-fight exclusion retired, before/after per species presented before
    its gate; the proofs regenerate once more after the drop, an accepted cost; (d) the spawn-100 benchmark harness,
    MHLib counters in the baseline, threshold proposed, not adopted; (e) the animation contract and the keyframe
    controller's return, designed and presented before anything is wired; (f) the package generator — SPEC, bone
    glossary, generated trigger inventory, TEXTURE_MAP, INVENTORY.csv — built and dry-run on the landed species; nothing
    under `artist_handoff/` is committed until the drop lands. Wave 4 gates only when a slice above is waiting on the
    owner. The mirror drop lands on the owner's Section B go. Item 8 above (the eviction inside the G2 root-order slice)
    is superseded by (a): the eviction is its own slice, ahead of the root-order contract. (9) **Changelog note:** one
    paragraph per wave — wave 3's five folded into one. (10) **Harness:** the sibling test classes' floating frozen mobs
    stay; the convention is recorded in the harness notes (F0.7); no cross-class change while the rows pass.

24. **Rulings of 2026-09-06 (owner, through the advisor):** (1) **Push:** the 15 commits after 814d151 pushed
    (origin/master 284972c) before anything else landed. (2) **Slice (c), the G2 root-order contract — approved from the
    table:** gated and landed from `g2-root-order` with the pin removals (the fifteen `max_contested_fraction_pin` entries
    and the four `PENDING_OWNER` strings go; the parity tools' default flips to no exclusion; both proofs regenerate; the
    benchmark re-pins). (3) **Missing-key policy:** a logged fallback to GeckoLib's own order when the key is absent (a
    resource pack must never crash the client), loud when present and wrong; an asset-audit ERROR for any shipped
    orespawn rig without the key. (4) **The tie rule:** a fidelity note in the record; the look sheet says last-wins;
    the production seam's executed evidence is the Section E look. (5)–(14) **Slice (e), the contract and the
    controller's return:** Q1 (a) one `[modern] artistAnimations` master, default ON, `classicAnimationSpecies` the
    exclusion list, species self-gated by clip presence; Q2 (a) weight-blended locomotion layers, GeckoLib's blend-in for
    triggered clips, the bone-reset blend-out, triggered clips replacing the gait on the bones they animate; Q13 (a)
    weights; Q3 (a) for the pilot — the vanilla flip stays, no death clip; the rule for later: a species whose JSON ships a
    death clip gets (b), designed when the first such clip arrives, the Queen's own death clip the precedent; Q4 (a) the
    overlay stays; Q5 (a); Q6 (a) four extras per species without a ruling; Q7 (a) Tier 3 gets no artist clips; Q8 (b)
    the pause-screen freeze a recorded divergence, PN entry, ENT-S-147 closes on it — no renderer plumbing for what sits
    behind the pause menu, the hook stays the shape if a per-render effect ever has a live signature; Q9 (a) scoped: the
    catmullrom spline arguments repaired at load in the replacement seam (`OreSpawnGeoReplacementModel.getAnimation`)
    only, the Queen's native model on stock semantics until her own ruling, recorded as a deliberate divergence from the
    library with the javap cite, reported upstream, two refuters, the Beaver look decides visibility; Q10 follows —
    "2.5e-3 rad; Beaver reference leg 15 / 13 / 8 catmullrom keys per bone with spline arguments repaired at load; wrap
    sample T−ε vs 0+ε included"; Q11 (a) per species by the trigger inventory; Q12 (a) calm_idle until the SPEC adds
    the synched byte; Q14 (a) every loop normalised to 1.0 s; Q15 (a) the `_preview` export, Blockbench-only, rejected
    in the jar; Q16 (a) the Queen the pilot boss, scoped to a subset of her clips (idle and one attack), the Beaver the
    other pilot; Q17 (a) the Beaver clip regenerated under the converter's rule when the keyframe leg lands, the salvaged
    documents kept with the note. (15) **Next on the chain:** the keyframe leg (the wrap sample, density as an output),
    the Q9 repair, and the Beaver clip regenerated — the controller's return as the first Tier-2 slice's precondition,
    presented before wiring. (16) **Slice (d):** R1–R7 ADOPTED as proposed — bytes lead R1, ns informational within the
    ±40 % band; the mixed scene added before the first Tier-2 cutover; the live scenes the owner's, not on the critical
    path until a classic / candidate pair exists. (17) **The counters:** a system property on the gametest run, no test
    seam in production code. (18) **Slice (f), locked bones:** keying a locked bone is allowed and WARNED (the SPEC
    states the consequence: the hitbox part follows the bone); renaming, re-parenting or deleting one is REFUSED; the
    README, the Queen's SPEC and the checker say the same thing; PROVISIONAL comes off; the reject mode stays available
    for the day the server-side evaluator lands. (19) Nothing under `artist_handoff/` is committed before the mirror
    drop; after it, the package for every landed species, the pilot pair first. (20) **ENT-S-146:** a parity bug; both
    halves fixed in classic first, inside 4c — the per-frame rolls from the level RNG through a `PurplePowerPose` seeded
    in the entity_state kind, the accumulating rotations bug-for-bug and disclosed, the translucent fullbright render
    state through `entityTranslucent`; the visual leg extended for translucency as a harness-semantics change,
    before/after presented before its gate; the PurplePower candidate re-proven against the fixed classic; two
    refuters; the port's opaque steady spin undocumented — no key. (21) **ENT-S-147:** closed under Q8, a PN entry.
    (22) **ENT-S-148, 149, 151:** parity, classic, one XS batch, one refuter. (23) **ENT-S-150:** (ii) first — the
    read-only survey of every legacy-AI species' speed under the modern mover as its own lane, the boost one case of a
    class; (i) after it — the value that reproduces 1.7.10's saturated ≈3.1× sprint (MULTIPLY_BASE ≈ 2.1 or the survey's
    mapping), the withdrawn ENT-S-145 hunks re-applied with the number and its pins. (24) **OPT-030** rides with the
    MHLib harvest slice, two refuters; not before. (25) The Girlfriend's owner pair at priorities 1 and 2 on the Leon
    precedent: ratified; the fifth ENT-S-138 site closed in the batch: ratified. (26) **Order of work:** push → (c)
    gated and landed → ENT-S-146 in 4c → the (e) records and item 15 → the XS batch (22) → the ENT-S-150 survey lane;
    the mirror drop on the owner's Section B go, the package and the pilot after it; wave 5 (24 and whatever the survey
    files) gates only when a slice above is waiting on the owner.
25. **Rulings of 2026-09-12 (owner, through the advisor):** (1) **Push:** master pushed now, the 21 commits after
    origin/master 284972c (origin/master 182a76d). **Cost rules (standing; they win over every earlier order of work,
    addendum item 26 and the 2026-09-06 order included):** (2) parity lanes are frozen until the artist-tier rigs are cut
    over; items 18–23 of this set are rulings, entered in the register as "ruled, deferred", not coded; new findings get a
    register line and stop; the audit waves run as records only; wave 6 does not run. (3) **Order:** item 15 landed, then
    the Tier-2 slices, then the harvest remainder with Slice 5, then the Tier-1 slices. The first Tier-2 slice starts as
    soon as item 15 is landed, on the current harness, behind the switch as 4b did; the drop, when it lands, regenerates its
    proofs with everyone else's. That slice's report states anything the drop would change in what the slice produced
    beyond the proofs (geos, clip signs), with the cost. (4) **Conversion slices the harness proves:** one refuter
    regardless of file count, no before/after document (the gate literal and the proof rows are the evidence), records as
    the FIX_LOG section and register lines. Two refuters stay for MHLib, renderer and motion code; a harness-semantics
    change still presents its before/after before its gate. (5) **Tooling and docs lanes** (python under tools/, records
    scripts, checker policy, report edits): no refuter; the tool's own tests and one dry run are the check. (6) **Records
    per landing:** the FIX_LOG section and the register lines. A dated report a landing supersedes gets one banner line at
    its top naming the FIX_LOG section (the form already on the G2 before/after), never passage-by-passage notes. The
    changelog is written once per push. (7) **A red gate on a row outside the slice:** the smallest change that makes it
    green, one refuter, one paragraph in FIX_LOG, a register line if a better fix exists for later. (8) **FIX_LOG:** the
    Phase A–F sections (FIX_LOG:128–3452) move unchanged to `FIX_LOG_pre_G.md` with a one-line pointer where they stood;
    the preamble and "Pending manual tests" stay; one docs-only commit; from then on lanes read only the live file.
    (9) **Decisions the recorded doctrine already answers** (PN-012/016, Amendment 1, the addendum, the (e) rulings) are
    the agent's: made, tagged "decided under doctrine, reversible", reported one line each; ask only where the doctrine is
    silent. (10) **Every report opens with three counts and no percentages:** rigs landed through the seam over the
    design's conversion count; artist-tier species with keyframe clips / 90; artist-tier species packaged / 90. The package
    dry run (scratch, not `artist_handoff/`) is regenerated at the end of every slice for the third count. **Item 15 —
    lands now** (this reverses "wires after the Beaver look"; the owner's look judges the keyframe Beaver in the same
    sitting as A–H): (11) **event keyframes** are not part of the contract on looping clips (they fire once per manager
    under the phase lock); the README states "no event keyframes on loops — code-fired events come from the trigger
    inventory"; revisited only if an artist needs one. (12) **The idle-only delivery gap:** the artist gate opens on
    `idle` AND `walk` delivered together; one without the other stays classic and the checker says so; a README rule.
    (13) **The late-prime case without a headless twin:** an open harness item, closed by the headless twin in the first
    Tier-2 slice, not before. (14) **The three keyframe rows client-only:** accepted and recorded; the headless leg is the
    proof. **Slice (c), ENT-S-146 — the presented deviations (docs only):** (15) "loud when present and wrong" as an ERROR
    log once per resource plus the fallback: ratified — a resource pack never crashes the client; the asset-audit ERROR is
    what guards the shipped rigs. The Queen declared outside the seam as an explicit set: ratified. (16) **The translucent
    mode's depth-tie window:** 1e-5 stands as the named tolerance `coplanar_depth_epsilon_blocks` for the translucent mode
    only (PurplePower is its one user). The unrounded projector with 1e-6 rides with the drop's proof regeneration, which
    redoes every proof anyway — before/after per species presented then; the window retires if the numbers allow. Not a
    separate slice. (17) **ENT-S-152:** a disclosure — the port follows 1.21.1's within-cube face order and the two
    renderers match each other by the face-order contract; a PN entry, no vanilla fork. (18) **ENT-S-153:** reading (2) —
    NO_OVERLAY on the orb on both renderers, the ENT-S-094 shape, one line each side. No red flash 1.7.10 never showed;
    the grey pass is out of proportion. PN entry now; the code change ruled, deferred with the parity lanes. **The speed
    class and MHLib — ruled, deferred (register lines now; no code until the cut-over):** (19) **ENT-S-157 with ENT-S-145**
    (this is item 23 (i)'s value): the Ender pair in classic gets (a) + (b) — bases re-tuned to A' = √(0.1·A): Knight
    0.1789, Reaper 0.1924, so the idle walk is 1.7.10's 1.38 / 1.60 b/s; the attacking boost as a per-species ADD_VALUE
    (+0.1406 / +0.1271, orig's operation 0) reaching the 4.405 b/s cap while a target is held; the withdrawn ENT-S-145
    hunks re-applied with those numbers and the 28 rows re-based. One batch, one refuter, when the freeze lifts.
    (20) **ENT-S-155:** classic faithful — the SpiderDriver on 1.7.10's 0.8, absurd as it is; modern keeps the 1.21.1
    spider base under `[modern] spiderDriverModernSpeed`, default ON, one MOD record. (21) **ENT-S-158:** parity, both
    modes, one port-wide mapping — the 1.7.10 water pace restored for every OreSpawn swimmer on the default travel through
    one shape (a shared travel override or helper), never per species. The shape and a before/after table in blocks per
    second per swimmer are presented before its gate; refuters by files touched. (22) **ENT-S-159:** parity, classic, XS,
    with the next XS batch. (23) **OPT-032:** mitigation (c) — the master client is exempt from culling, so the stream
    never stops (the master's own render cost, the pre-harvest state for one client); pin: the master's `shouldRender`
    true with her outside its frustum while a non-master's is false. (a) and (b) are not adopted: they change the
    election protocol OPT-003 designed. Coded with the harvest remainder, not before; the real fix is Slice 5's
    server-side evaluator, recorded. (24) **OPT-031:** with the drop's proof regeneration. (25) **The OPT-013 look item**
    goes on the look sheet now as Section H: one Queen, walk beyond ~80 blocks, turn away for ten seconds, turn back — her
    parts must not be resting; two minutes. **Records:** (26) the wave-5 commit-message correction in FIX_LOG stands; no
    amend, as ever. (27) **Order of work:** push → the cost rules, the deferred rulings and the two PN entries recorded
    with the FIX_LOG split (docs-only) → item 15 landed (one gate) → Section H → the first Tier-2 slice (the species list
    the agent's; the headless twin in it; one refuter; the counts open the report). The drop lands on the owner's
    Section B go; the package and the pilot follow it. Nothing else runs.
26. **Rulings of 2026-09-13 (owner, through the advisor):** (1) **Push:** master pushed now, the seven commits after
    origin/master 182a76d (origin/master c6ee196). **The first Tier-2 slice's questions:** (2) the bare-name rule for a
    multi-group species without a gait group: (a) — the SPEC names the species' primary locomotion group explicitly
    (`primary_group`; the first group when absent) and that group's clip carries the bare `walk`; the other groups
    stay `walk_<group>`. The contract's fallback (fly → walk with the state's weight) is what plays a flyer's walk in
    flight, so the bare name is a label, not a semantic; item 12's gate and the README rule stand. Lands with the next
    Tier-2 slice: the Dragonfly, Cockateil and Ruby Bird clip files regenerate under it and their gates open; the seam
    delta takes a second refuter as renderer code. (3) **ENT-S-161:** (a) — the seam renderer overrides `renderCube`
    to hand the un-mangled transformed normal through; the harness's `CapturingGeoRenderer` takes the same override;
    two refuters; a PN entry recording the deliberate divergence from the library with the upstream report text, as
    PN-024 did. It lands first in the next session under its own gate; the Firefly, Cloud Shark and Gold Fish then
    rejoin the next slice. (4) **ENT-S-160:** (a) with (d) as recommended, executed in the remainder slice after the
    weights slice, since (c) is that slice's additive layer. Nothing now. (5) **The round-trip's six time-only diffs:**
    widen the round-trip time tolerance to 5e-5 s (tooling, no refuter). Blockbench's real timecode precision is read
    from its exporter source when the `_preview` export (Q15) is built, and the emulation follows it then. (6) **The
    event-key rule:** native-controller species are exempt (the Queen), keyed on the SPEC's controller kind; the checker
    says why. Tooling. (7) **The weights slice** (Q2 (a), Q13 (a), already ruled) is contract completion, not a parity
    lane. It lands after the Tier-2 slices, two refuters, under one constraint: every transcription stays bit-exact to
    the classic hook (the reference leg is its proof) — weights apply to artist clips only, or transcription groups are
    declared always-on. The flyers' and swimmers' fly / swim semantics are settled in that slice, not before. (8) The
    mirror drop's effect on the six rigs (three geo regenerations, no clip change): noted. (9) The decided-under-doctrine
    items stand. **The order, amended (replaces cost rule 3's sequence):** (10) Tier-2 slices → the weights slice → the
    Tier-1 rigs through the seam in the same form with the hitbox profiles excluded (each boss's SPEC pre-declares its
    intended locked bones, marked provisional, from the G0 design where it has them and the Queen's profile as the
    template where it does not) → the full package. The harvest remainder, Slice 5 and the profiles are a phase after
    the package; the locked-bone policy stays WARN until then. The drop, the Queen's package and her pilot go on the
    owner's Section B go, whenever that comes, independent of the slices. **Order of work now:** (11) push →
    ENT-S-161 (3) → the next Tier-2 slice: the three rejoined rigs plus the next species in the design's order, ten to
    fourteen in all, the naming rule (2) with it, one refuter for the conversion, the counts open the report; items 5
    and 6 ride in the same tooling commit. Nothing else runs.

## D. Rulings executed the same day (for cross-reference)

- ENT-S-098 fixed (shot BetterFireballs carry the mod's own type; save/load round-trip pinned) and its
  projectile-wide sweep filed under the same finding.
- MOD-029 accepted as the modern-mode default (Mothra 6x3 behind the modern config; classic 5x2).
- The renderer pin scanner's reassigned-local blind spot tightened; any pin that changed was presented
  before its gate.
- Kraken targeting divergences filed as a finding with the parity split.
- BUG-043 checked against upstream MultiHitboxLib; the version gap recorded.
