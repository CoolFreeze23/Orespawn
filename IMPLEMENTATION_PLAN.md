# OreSpawn Port — Implementation Plan

> **Ground rules:** Read this entire file before writing any code.
> If FIX_LOG.md exists, read it too and resume from where it leaves off
> instead of starting at Phase A. Re-read the Ground rules and
> Verification loop sections whenever resuming a session.

This plan governs all fixes for the OreSpawn port audit. The audit
produced AUDIT_INVENTORY.md, AUDIT_FINDINGS.md (601 entries with stable
IDs), and audit_sections/01-10. The original source is at
`reference_1_7_10_source/sources/danger/orespawn/` (decompiled 1.7.10
sources; see `reference_1_7_10_source/INDEX.md` for the CFR name-mapping
cheatsheet). The port targets NeoForge 1.21.1 (Java 21, official Mojang
mappings).

Your goal: bring the port to 100% verified parity with the original mod,
with clean, fully deobfuscated, professionally documented,
1.21.1-idiomatic code. Every finding ID must end in a terminal state:
**FIXED**, **VERIFIED-CORRECT** (audit was wrong, with proof), or
**DEFERRED** (with my explicit approval only).

## Ground rules

1. AUDIT_FINDINGS.md is the work queue. Work finding-by-finding. Never
   close an ID without citing the original file:line you matched and
   the port file:line you changed.
2. Parity is defined by the ORIGINAL's behavior, not by what seems
   reasonable. If the original has a weird value (10000 HP, a 0.001
   drop chance, an AI goal that looks buggy), port it faithfully. You
   do not get to silently "improve" gameplay. HOWEVER — every time you
   encounter something in the original that is bugged, janky,
   exploitable, or clashes with modern vanilla design, log it in
   MODERNIZATION_NOTES.md (see below) while still porting it
   faithfully. Faithful port now, curated improvement list for later.
3. Never resolve a finding by removing the feature. MISSING means
   implement it, not delete the references to it.
4. No obfuscated remnants anywhere: no func_/field_ names, no SRG
   names, no p_123456_ parameter names, no single-letter variables
   outside trivial loop indices. Every identifier must be a meaningful
   English name. If the original uses func_70015_d, you write
   setRemainingFireTicks-equivalent logic with the modern mapped API
   and name local variables by what they mean (fireDurationSeconds,
   not i).
5. All code must be idiomatic NeoForge 1.21.1:
   - DeferredRegister/DeferredHolder for all registrations
   - Correct event bus: mod bus for lifecycle/registration events,
     game bus (NeoForge.EVENT_BUS) for gameplay events — this is
     finding #1's crash, get it right everywhere
   - Modern AttributeSupplier builders for entity attributes
   - EntityDataAccessor/SynchedEntityData for client-synced state
   - Codecs + DataComponents where 1.21.1 replaced NBT item tags
   - Datagen or JSON for recipes/loot tables/tags, not code-side hacks
   - No deprecated API calls; if a 1.7.10 mechanic has no direct
     modern equivalent, implement the closest behavioral match and
     document the mapping decision in a comment

## Documentation standard (apply to every file you touch)

- Class-level Javadoc: what this class is, which original class it
  ports (file path in original repo), and a summary of any intentional
  behavioral mapping decisions
- Javadoc on every public/protected method: what it does, parameters,
  return, side (client/server/both) when it matters
- Inline comments explain WHY, not what. Good: `// Original spawns the
  shockwave on a 47-tick cycle (Mobzilla.java:212); kept exact to
  preserve dodge timing`. Bad: `// increment counter`
- Magic numbers from the original become named constants with a
  comment citing the original source location and value
- Animation code: comment each state, its trigger condition, and its
  length in ticks, matching the original model/animation definitions
- Comments must be accurate to the code as written. Never leave a
  comment describing behavior the code doesn't have.

## MODERNIZATION_NOTES.md (maintain throughout all phases)

This is the roadmap for the post-parity 2.0 pass. For everything you
notice while porting, log an entry with:

- ID (MOD-001, MOD-002, ...) and category:
  - **ORIGINAL-BUG** — broken in the original, e.g. the OreGenericEgg
    dupe, double-drop pattern, Creative-mode kills
  - **BALANCE** — values wildly out of line with modern vanilla, e.g.
    armor durability, 10x stat outliers
  - **VANILLA-INTEGRATION** — could hook into modern systems: loot
    table datapacks, biome tags, structure jigsaws, advancements,
    enchantment compat, brewing, villager trades, /summon-friendly
    NBT, mob category caps
  - **UX** — missing tooltips, no JEI-friendly recipes, confusing
    dimension access
  - **TECH-DEBT** — patterns kept for parity that should be redesigned
- Original behavior (file:line) and exactly what's wrong or dated
- A concrete proposal for the modern version, including which vanilla
  1.21.1 system it should integrate with
- Impact estimate: gameplay-visible or invisible, and rough effort
- Cross-reference the finding ID(s) it relates to, if any

Do NOT implement any of these. This file is planning output only.
By the end it should read as a complete, prioritized design doc for
a "OreSpawn Modernized" follow-up milestone.

## Order of work

**Phase A** — the 7 CRITICAL bugs, then 6 HIGH bugs. Each must compile
and include a short repro note (what crashed before, why it can't now).

**Phase B** — systemic issues that touch many findings at once: the
double-drop pattern (~25 entities + 3 bosses), the stats drift / dead
MobStats reconciliation (port the REAL original values, cite each),
rider flight + ranged attacks on mounts, the 39-model animation
mistranslation.

**Phase C** — remaining DIVERGENT findings, category by category
(entities → bosses → items/blocks → worldgen → animations/GUI),
fixing every value mismatch number-by-number.

**Phase D** — MISSING features: the ~25 structures, Village/Islands
dimension access, dungeon pool restoration, Prince/Princess ranged
attacks, dispenser behavior, etc. These are full implementations
verified line-by-line against the original.

**Phase E** — PARTIAL completions, then the 21 UNVERIFIED items: gather
the evidence the audit said was needed (e.g., diff the full recipe
corpus, not a spot check) and either fix or mark VERIFIED-CORRECT.

**Phase F** — optimizations: apply the 20 behavior-neutral OPT-* items.
The 4 behavior-affecting and 3 mixed ones: present each as a proposal
with the exact behavior delta and WAIT for my approval. Do not apply.

**Phase G** — Release (only after Phases A–F are complete, all 601
findings terminal, and I have explicitly approved the release):

1. Final verification: `./gradlew build` clean; run the data
   generation task if used; confirm the built jar's mods.toml
   metadata (mod id, version, display name, NeoForge version range
   `[21.1,)`, Minecraft 1.21.1) is correct
2. Set the release version following semver (suggest one based on
   the scope, confirm with me before tagging)
3. Write CHANGELOG.md for this release, organized as:
   - Highlights (5-10 bullets a player actually cares about)
   - Crash fixes (the criticals, plain-English symptoms: "Fixed
     server crash when a Rat spawned from a mob spawner")
   - Parity fixes by category (entities, bosses, items, worldgen,
     animations) — summarize patterns, don't dump all 601 IDs;
     link to FIX_LOG.md for the full record
   - Known issues / intentionally preserved original quirks (pull
     from PARITY_NOTES.md)
   - A short "What's next" teaser from MODERNIZATION_NOTES.md

   Write it for players first, modders second. No internal finding
   IDs in the player-facing sections.
4. Commit everything with a clean history (logical commits per
   phase/system, not one giant blob), tag the version, push to
   GitHub
5. Create the GitHub release: tag, title, the changelog as the
   release body, attach the built jar from build/libs (the main
   jar, not -sources or -slim unless I say otherwise)
6. Update the repo homepage: rewrite README.md to reflect the
   ported state — what the mod is, supported version (NeoForge
   1.21.1), install instructions, feature overview, credit and
   link to the original OreSpawn repo and authors, license status,
   link to the changelog, and a roadmap section summarizing the
   modernization plan. Update the GitHub repo description and
   topics to match if you have access.
7. Show me the changelog, README, and release draft BEFORE
   publishing. Publish only after my explicit go-ahead.

## Verification loop (every batch, no exceptions)

1. Code compiles: `./gradlew build` must pass before moving on
2. For each finding closed: re-diff the relevant values against the
   original and state them ("original Mobzilla.java:88 health=4000.0F,
   port MobzillaEntity.java:61 MAX_HEALTH=4000.0 ✓")
3. Update AUDIT_FINDINGS.md statuses in place and append to
   FIX_LOG.md: finding ID, files changed, summary, verification
   evidence
4. Flag anything you could not verify instead of guessing. Writing
   plausible code and marking it FIXED without checking the original
   is the single worst failure mode here.

## Checkpoints

Stop and report after each phase with: findings closed (by ID),
findings remaining, build status, anything DEFERRED or needing my
decision. Do not proceed past a phase with a broken build or
unexplained open findings from that phase.

## Done means

- Every one of the 601 findings in a terminal state
- `./gradlew build` clean
- Zero obfuscated identifiers (grep for `func_`, `field_`, `p_[0-9]`
  must return nothing)
- PARITY_NOTES.md lists every intentional deviation (target: near
  zero) and every original-bug-replicated note
- FIX_LOG.md is a complete, ID-referenced change record
- MODERNIZATION_NOTES.md complete and prioritized
- CHANGELOG.md, README.md updated; release tagged, published, and
  jar attached (after my approval)
