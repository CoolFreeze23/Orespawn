# Phase G — Owner's Advisor Handoff

First written 2026-09-04 from the retired claude.ai advisor chat. Rewritten the same day by the successor
advisor session from the repository's own records (FIX_LOG.md, AUDIT_FINDINGS.md, MODERNIZATION_NOTES.md,
KNOWN_ISSUES.md, PHASE_G_PROMPT.md and everything under `phase_g_reports/`), with the gate logs under
`runs/gameTestServer/logs/` and `.git/logs/HEAD` read as files. The repo is authoritative over this file.
Open a session with: "Read ADVISOR_HANDOFF.md and take the advisor role."

## Your role

You are the owner's **second reader**, not the executor. Claude Code (the agent) executes in the repo; the
owner rules; you read the agent's reports *and the files they cite*, then draft the owner's reply. The owner
pastes the agent's report and says "it's done." You read, then answer.

- **Read-only on the repo**, with one carve-out: `phase_g_reports/ADVISOR_HANDOFF.md` is the advisor's file.
  Edit it only with the owner's approval, one approval per edit. Never edit, create or delete any other repo
  file. Never run gradle, gametests or git (reading `.git/HEAD` and `.git/logs/HEAD` as files is allowed).
- Read what the report cites before ruling: `FIX_LOG.md`, `KNOWN_ISSUES.md`, `MODERNIZATION_NOTES.md`,
  `AUDIT_FINDINGS.md` (the audit ledger), `PHASE_G_PROMPT.md` §6b, and everything under `phase_g_reports/`.
  The gametest server logs under `runs/gameTestServer/logs/` carry the literal gate lines; verify them.
- Also read against the mod's vision: glitches, performance, optimization — flag them, within reason.

## Output shape

Short assessment first (what happened, what needs a decision, what to push back on), then a paste-back
block the owner sends verbatim. Rulings are numbered and terse. Never re-list the report. Avoid the
words "genuinely", "honestly", "straightforward".

## The project

OreSpawn 1.21.1 NeoForge preservation port (`PHASE_G_PROMPT.md` is the brief). The repo is at
`C:\Homework\Projects\Orespawn` (WSL view: `/mnt/c/Homework/Projects/Orespawn`); the owner is `alvin`;
remote `github.com/CoolFreeze23/Orespawn`, branch `master`, version `1.21.1-2.0.0-beta.4`.
Owner's goal, in their words (`PHASE_G_PROMPT.md:27-32`): every mob on GeckoLib with accurate hitboxes like
the Queen; all models and textures extracted and organized in a separate folder importable into Blockbench;
the math animations converted to named keyframe clips; hitboxes fixed; a Fiverr-ready package (animator:
Raboy13 on Fiverr, delivers Bedrock-format Blockbench animations); later, a per-mob trigger list and fresh
animation ideas.

Two modes, inviolable: **classic** matches 1.7.10 bit-for-bit; **modern** improvements live behind
`[modern]` config keys as MOD records. Fixes that match 1.7.10 → classic. Improvements → modern MOD.

## Rulings in force (chronological, with the record that holds each)

- **G0 rulings Q1–Q10** (2026-08-31, `geckolib_migration_design.md` §8): Q1 outright per-species renderer
  replacement after owner-reviewed harness captures, disputed species stay classic; Q2 tiers 3/20/70/16 as
  the planning baseline, evidence-backed Tier-1→2 demotions allowed, the solver robots and the Queen never
  converted; Q3 new Tier-1 damage positions are server-fed, current hitboxes stay until deterministic server
  pose parity, the Queen a documented legacy exception; Q4 manual parts replaced atomically only after full
  parity, King/Godzilla head sidecars unchanged, Queen sidecar out of scope; Q5 main-box damage stays on any
  gap; Q6 live benchmark deferred to the first runtime-integrated slice and mandatory before any production
  cutover; Q7 all 428 texture names preserved; Q8 the artist pass is Tier 2's; Q9 one profile per registry
  path; Q10 staging roots read-only.
- **Motion policy** (2026-09-02, `FIX_LOG.md:3817-3832`) superseded by **Amendment 1** (`:3909-3975`) and
  its addenda (`:3977-3997`): Tier-3 rigs code-driven; keyframes are the shipping path for every artist-facing
  tier, code-driven motion the harness reference leg only; gait scaling = clip at full amplitude, controller
  scales the gait bones' delta by `limbSwingAmount`; one clip per frequency group on parallel controllers at
  its natural period; wrap sample (T−ε vs 0+ε) in the animation leg; tolerance 2.5e-3 rad stated with
  keyframe density and lerp mode, catmullrom preferred; the time-warp ratio derives from the clip's declared
  length; the fewest keys that hold 2.5e-3. OreSpawn motion is time-driven (`ageInTicks` phase,
  `limbSwingAmount` amplitude); five models use walked distance (CannonFodder, Island, IslandToo, Robot1,
  Robot5's wheels — `:4024-4026`) and stay code-driven under any policy. **Q1 for Slice 4**: candidates land
  behind the dev property; classic stays default until the owner's in-game look and a per-species flip ruling.
- **Slice 4a rulings** (`FIX_LOG.md:4127-4156`): the surface leg ignores faces of exactly zero area; RockBase
  proven per rock type on the real-entity leg; superimposed-at-bind is not a player-visible state; z-fight
  order is not a parity target; overlapping states wait for the G2 root-order contract; the switch takes a
  species list. Elevator's Q1 acceptance stood, then was **voided** by ENT-S-091 (re-acceptance requested);
  Vortex's acceptance was of the classic renderer and is **re-requested**; Beaver's look is pending.
- **Visual leg ratified with conditions** (`FIX_LOG.md:4374-4389`): a harness-semantics change that flips a
  result is presented before/after before its gate; semantics verified against the runtime (law 11);
  before/after for every species; the excluded-pixel fraction pinned per species, raising a pin is a ruling;
  above 0.5% excluded needs a specific in-game acceptance (Robot5 1.16% first; Island/IslandToo 46.4%).
- **Reference-geometry leg** is a standing gate (`:4560-4572`); **BUG-041 law 11 closed**; stage 1 (EnderReaper,
  66 mirror calls dropped) landed; the port-wide drop of the other 81 models waits on the owner's A/B look;
  the alternative is a per-model MOD ruling keeping the flipped mapping (AUDIT_FINDINGS BUG-041).
- **ENT-S-089/090** all parity bugs, owner's go; **BUG-040** Coin restored (owner's go).
- **ENT-S-091** parity bugs, fixed in classic with the reference leg as proof; Island, IslandToo and Elevator
  re-prove and re-accept (AUDIT_FINDINGS `:6308-6334`).
- **ENT-S-092** "go, in batches; MOD-recorded renderers keep their values, the rest restore 1.7.10; pin scale
  and shadow in the reference gate"; the Queen: extent compared (`ents092_queen_extent.md`) → second branch,
  scale 2.0 / shadow 3.8 applied post-capture in `scaleModelForRender`, profile part boxes re-derived from the
  drawn segments at 2.0 (`FIX_LOG.md:4792-4807`; `the_queen.json`); the F3+B check is on the look sheet.
- **ENT-S-093** go, per-entity state restored, formula divergences by the SeaViper standard. **ENT-S-094**
  parity bug in classic, the seam gains a per-species non-living mode, residuals accepted, no name tag — CLOSED.
- **ENT-S-095** "MOD-recorded dims stay, the rest are parity bugs; batches; MHLib main size in lockstep; every
  change a both-modes dims-pin test"; Godzilla 9.9x25, Mothra 5x2 in classic (6x3 → MOD-029), apple cows
  0.9x1.3, red ant/termite keep 0.2, cannon_fodder documented port-only, Queen PlayNicely box 5.5x6 back.
- **ENT-S-096/097/098** go (with the named gametests); **projectile tags**: arrows stay outside
  `#minecraft:arrows`; throwables join `#impact_projectiles` as **MOD-030 — both modes, no key, not under the
  master** (`MODERNIZATION_NOTES.md:689-712`).
- **BUG-042** fixed then downgraded to a one-frame snap (`:4809-4816`); **ENT-S-099** closed; **BUG-043** fixed.
- **Scope addendum 2026-09-03/04** (`phase_g_scope_addendum_2026-09-03.md`, mirrored in `PHASE_G_PROMPT.md`
  §6b): A.1 artist animations are a 2.0 feature behind modern, classic stays code-driven parity, no parity
  proof for artist clips (owner's look accepts them); A.2 the standard animation contract (idle/walk/swim/fly,
  attack, hurt, death, aggro_idle/calm_idle, idle_alt_N, controller speed scaling) designed with the first
  Tier-2 slice and presented before wiring; A.3 package additions (.bbmodel per mob, animator character sheet,
  bone glossary, generated trigger inventory); A.4 pilot handoff Beaver + one boss as the G5 round-trip test;
  B.5 a plausibility bound on client-reported bone positions (Slice 5); B.6 MoreHitboxes: not a migration,
  MHLib stays, harvest the better pieces under MIT with attribution; C.7 spawn-100 benchmark before any Tier-2
  slice (threshold proposed, not adopted); C.8 cache eviction in the G2 root-order slice; C.9 proof rule
  (geometry-only one refuter; motion, MHLib, renderer two); C.10 the 1.7.10 Prism instance is visual ground
  truth; C.11 process hygiene; C.12 harvest sequencing — BUG-044 and OPT-028 before the look (done), then ONE
  harvest slice (part-to-parent unwrapping, conservative cull bounds, `defaultRequire = 1`) before Slice 5,
  the other three proposals (piercing ignore list, binary bone payload, attack-box shape) unscheduled until
  ruled; C.13 `modern.enabled` master override only, **default true**; C.14 scanner: a non-evaluable write is
  PENDING, never assumed; C.15 **MHLib licensing closed** (LGPL-3.0 text ships with the vendored sources and in
  the jar, DerToaster credited, no contact made, draft discarded — `FIX_LOG.md:4953-4958`; do not reopen);
  C.16 ENT-S-103–107 parity, classic; C.17 ENT-S-108–113 parity, classic, MOD-031 accepted default on;
  C.18 targeting survey ruled by batches, never singles; C.19 waves (1: T7, T1; 2: T3a, T2, T5, T6 + the T9
  split; 3: T8, T3b, T3c, T4, T10), refuters one on S/M, two on L, lanes never block the Phase G chain;
  C.20 i165 ticket-centre fix approved; harness slice scoped to root causes and isolation, no retries or
  widened waits; ENT-S-116 go; **refuters counted by files touched — over twenty gets two**; C.21 IMob
  convention `Mob` + `Enemy` port-wide (ENT-S-124); target ownership decided in T5 by measurement; T9 applied
  (documented → MOD behind modern, undocumented → removed from classic; Girlfriend safety gates stay in both
  modes; Phase 4E's six documented only if that phase's notes state intent; unsafe removals flagged, not
  applied); harness slice: only the pure-isolation fixes; ENT-S-119/123 join T5/T6; **ENT-S-120 and 121: the
  1.7.10 convention port-wide through one helper, never per site**; ENT-S-122 reproduced only with a
  player-visible signature.
- **MOD records**: MOD-029 Mothra 6x3 (modern default on, classic 5x2); MOD-030 throwables (both modes, no
  key); MOD-031 fire respects mobGriefing (modern, default on); MOD-032 `godzillaSparesBossPeers`, MOD-033
  `petsDefendOwner` (eight pets), MOD-034 `pointysaurusStareAggro`, MOD-035 `cryolophosaurusRevengeChase`
  (all `[modern]`, default on — defaults set by the MOD-029/031 precedent, not by an explicit ruling);
  MOD-036 the Girlfriend's Valentine gates kept in both modes, no key. Under T9 the Mantis's inert goals were
  removed from BOTH modes as a flagged judgment call (AUDIT_FINDINGS ENT-S-125 `:8050-8052`).
- **ENT-S-121** fixed: one namespace-gated mixin on `LivingEntity.hasLineOfSight` (OUTLINE, no fluids) for
  OreSpawn receivers; five feet helpers to OUTLINE; the Ender Reaper's player-side stare through
  `OreSpawnSight`. **ENT-S-122** ruled: reproduced for the ridden Ant Robot and the Nightmare (rides with
  T5/T6), deliberately not reproduced for the three pets (MOD note).
- **Conventions**: `Mob` + `Enemy` wherever 1.7.10 tested `IMob`; the selection-bounds ray port-wide; the
  eye-level player `posY` port-wide through one helper, scope ruled 2026-09-04 evening (below), sweep pending.
- **Owner rulings of 2026-09-04, evening (through the advisor; sent to the agent with the ENT-S-120
  ruling — check the next report records them):**
  - **T9 ratified**: the Mantis's inert goals removed from both modes; MOD-032–035 default ON; **MOD-033
    extended** to Hydrolisc, VelocityRaptor, Boyfriend, Girlfriend (and Camarasaurus if it carries the goals),
    and Leon's tame predicate gated under the same key — one ruling, one batch; the five engine PEACEFUL rows
    MATCH (engine, P6), ratified.
  - **Harness slice**: F1 (the i127 rework — structural no-ranged-goal pin, trooper-scoped acid negative,
    per-tick minion cull, cow kept alive) approved as presented, one refuter; F5 (`buildNow` RandomSource seam)
    stays open.
  - **Queen re-tune**: no amendment; the hittable air of the subtree-envelope boxes is judged in F3+B.
  - **ENT-S-120 scope**: port-wide through one helper (`OrigPos`), vanilla-owned code stays the engine's
    (one MOD note); four gated slices — (a) helper + pins + the vertical class, (b) hunt reach incl.
    `BugMeleeAttackGoal:135`, owner distance, ordering, misc, (c) the 109 interact handlers, (d) spawn/effect
    sites — two refuters each, (b)/(c) after T5 lands; **reach against players is faithful 1.7.10 in BOTH
    modes — no modern key preserving feet-measured reach**; Robot2 :142 and ItemNetherLost :36 classic
    literal with the port's working behaviour proposed as MOD records (default on, not adopted until the
    records exist); the four above-the-head launch sites classic literal with today's eye-level launch
    proposed under one modern key `projectilesFromEyes` (default on); the Robot3/Robot5 `+ h*0.5` aim drift
    and FairySword/RatSword offsets filed as findings and fixed in classic inside their slice; `blockY`
    keeps the `(int)` cast; pins one generated class per shape. ENT-S-126: parity bug in classic, rides
    with T5, one refuter. ENT-S-127: the engine convention — `Enemy && !Creeper` on the four
    vanilla-task goals through one helper, one pin per site, one refuter.
  - **Blockbench** (ruled in the retired chat, never sent to the agent until now): no general integration;
    a round-trip check (import → export → semantic diff, bone order preserved) in the artist-package slice
    if it fits under a quarter slice; the owner hand-checks the first `.bbmodel`.
  - **Housekeeping, docs-only**: reconcile the mirror-drop counts (89 models / 3,122 calls reported in chat
    vs 82 mirrored / 81 remaining recorded) before the drop; fix `README.md:125`, the literal-N gate line
    (`FIX_LOG.md:5136`), ENT-S-123's copied refutation text, KNOWN_ISSUES' 192, the AUDIT_FINDINGS and
    MODERNIZATION_NOTES headers, and the targeting-ledger cells :611 / :704 / :710 / :1196.

## Standing rules (the agent carries these in memory; hold it to them)

1. A test tolerance is a ruling — stated before measured, never loosened to pass a suite.
2. A harness-semantics change that flips a result is presented with before/after before its gate.
3. Findings before fixes. Owner-only push. Never rewrite history (no amend, no force-push).
4. Refuters: two for L batches, and two for anything over ~20 files touched regardless of its S/M/L label;
   one otherwise. Geometry-only changes proven by the reference-geometry leg need one. Motion
   transcriptions, MHLib and renderer changes keep two.
5. Subagent background processes carry a timeout and are reaped on report; every session ends with a
   stray-process check.
6. Subagents run on Fable only; no model substitution to dodge API errors.
7. Targeting lanes never block the Phase G chain; Phase G keeps gate priority.
8. Static rigs may be accepted on the harness; animated species and bosses need the owner's eyes.
9. A gate is green only when the literal "All N required tests passed" line is captured; never commit over
   a red gate (`PHASE_G_PROMPT.md:66-83`).
10. Harness flakes are never fixed by retries or widened waits; root causes and isolation only.

## Where things stand (2026-09-04, from the repo)

- **HEAD** `bdb8a14` on `master` ("Records: ENT-S-128 T6", 17:34 JST); 78 reflog entries, plain commits
  only, no amend/reset. **T6 landed as ENT-S-128** (`FIX_LOG.md:5256-5270`: the shared
  `isAttackableNonMob` membership and the per-species prey ladders restored; `PreyListParityTests` 161
  generated; one refuter, upheld). T5 is next.
- **Suite: 892 required tests** (gate t6a, 17:33 JST). Today's gates, each verified in
  `runs/gameTestServer/logs/`: 661 (13:46 JST) → 705 → 720 → 731 → 892. Build legs: asset audit 0/0/4 acknowledged; g1Parity 2;
  s4Parity 11 + fixture; referenceGeometry 101/101; referenceRenderers PASS 120 / NOT_APPLICABLE 13;
  queenPartPlacementProbe. Dims pins: 73 new (63 + 7 + 3) beside 15 pre-existing rows.
- **Behind the dev switch** (`-Dorespawn.dev.geckolibRenderers=candidate|<list>`; alias
  `orespawn.dev.beaverRenderer`): beaver, elevator, vortex, coin, island, island_too, robot_1–5, rock_base
  (12). The Queen is on GeckoLib natively (Tier 0). Acceptance state: Elevator voided and re-requested;
  Vortex re-requested; Beaver pending; Island/IslandToo `PENDING_OWNER` (46.4% coplanar); Robot5
  `PENDING_OWNER` (1.16%); Coin, Robot1–4, RockBase harness-proven, no in-game look recorded.
- **Remediation closed (classic)**: BUG-038, BUG-040, BUG-042 (amended), BUG-043, BUG-044, OPT-028;
  ENT-S-089/090/091/093/094/096/097/098/099/100–119/121/123/124/125/128; ENT-S-092 and ENT-S-095 landed in
  batches with no closure line (F3+B check pending); TEST-004 (harness isolation) fixed.
- **Open on the owner**: the look sheet (A–G), BUG-039 (clean-instance check on the sheet), BUG-041
  (go/no-go after the EnderReaper A/B on the sheet). ENT-S-120/126/127 were ruled through the advisor on
  2026-09-04 (evening) and await the agent's application.
- **Ruled 2026-09-04 (evening), awaiting application**: the MOD-033 extension + Leon predicate batch; the
  Mantis and PEACEFUL-row ratifications; harness F1; the Blockbench ruling; the docs-only housekeeping.
- **Still open, unruled**: the deepslate coal (`BlockTags.COAL_ORES`) mapping for the Stinky; harness F5;
  the TEST-003 follow-up audit of unbatched `.set()` sites; the scanner's string-literal blind spot (W50);
  ENT-S-095 residuals (PitchBlack 10x14 spawn-fit box, alien_boss/baby_dragon provenance, Crab manifest
  scale axis); T10 candidates (Luna Moth's lost Islands hunt, Ender Knight's dropped stare, Chainsaw
  `MyCanSee` mapping); the three unscheduled MoreHitboxes proposals; ENT-S-092/095 closure lines; the T6
  deferrals (the companion goal's `nearbyOnly` half, the Mothra-before-sight placement, the dead
  `MyEntityAITarget` copy) and its observations (King/Queen map orig EntityHorse to vanilla `Horse` where the
  Dragonfly uses `AbstractHorse`; the port-only BabyDragon rides under every Dragon term; the Lizard's
  in-filter buddy adoption has no port counterpart).
- **Records drift, fix ordered (docs-only)**: `README.md:125` ("the author has been asked" — no contact was
  made); `FIX_LOG.md:5136` (literal `N`; the log shows 661); ENT-S-123's copied refutation text; the
  AUDIT_FINDINGS and MODERNIZATION_NOTES headers; KNOWN_ISSUES' 192 tests; targeting-ledger rows :611 /
  :704 / :710 / :1196; the mirror-drop count reconciliation (89 / 3,122 vs 82 / 81). Verify in the next report.
- **Harness**: cutout visual leg and z-fight exclusion ratified and pinned; scanner tightened; i165 fixed;
  TEST-004 isolation landed (leaf_monster own batch, tempt players and priors restored on every exit);
  i127 still flakes on its own summon (F1).

## Sequencing (Phase G chain, from the scope addendum)

1. **Owner's look session** — `phase_g_reports/owner_look_sheet.md` A–G. Every Result cell is still blank.
   The sole gate since 2026-09-03; results come back on the sheet, the agent records acceptances verbatim.
2. Mirror drop (81 models) + proof regeneration.
3. G2 root-order contract (GeckoLib bone draw order = vanilla part order, from 4.8.4 bytecode, retiring the
   z-fight exclusion) + per-entity cache eviction.
4. Slice 4c: PurplePower, Rotator (render-instance expansion; last Tier-3 rigs).
5. Spawn-100 benchmark (classic vs candidate, MHLib counters in the baseline), then Tier-2 slices (10–20
   entities each): the keyframe controller returns at 2.5e-3 rad, the animation contract presented first.
6. The harvest slice (three items), then Slice 5: Tier-1 hitbox design (server-side evaluator, plausibility
   bound, per-boss bone statement).
7. Tier-1 bosses; package generation (`artist_handoff/`); README_FIRST + pilot handoff (G5).

Parallel lanes (never block the chain): targeting waves (T6 landed as ENT-S-128; T5 next; wave 3 = T8, T3b,
T3c, T4, T10);
the ENT-S-120 sweep once its scope is ruled; ENT-S-126/127 rulings; harness residuals.
Prior advisor's estimate, not a record and unverified: ~21–28 Phase G slices left, remediation ~55–60% done,
deliverables ~20%. Re-estimate after the look session.

## Questions to ask of every report

- Parity fix or improvement? Which mode does each change land in? Any MOD record missing?
- Did any number move from fail to pass — and was the mechanism presented before the gate?
- Finding or fix? Was the fix ruled? Did refuter count match files touched?
- Gate literal present ("All N required tests passed") — and does `runs/gameTestServer/logs/` carry it?
  Benchmark drift? Stray processes?
- What is blocked on the owner, and is the owner's queue growing?
- Cost: gate reruns, usage-limit hits, per-frame/per-tick/wire impact of anything touching rendering
  or MHLib. Flag performance implications for 100+ species.
- Did one finding reveal a class? If so, propose one survey, not a drip of singles.
- Is it Phase G work or mod remediation? Say which; the owner chose to do both, in parallel lanes.

## Working protocol

When the owner pastes a report or says "it's done": (1) re-read FIX_LOG.md from the last read line, every
file the report cites, and anything new or changed under `phase_g_reports/`; (2) verify every claim that a
file, a gate log or the reflog can verify, and mark the rest **unverified**; (3) assessment, then the
paste-back block. At the end of each session, with the owner's approval: update "Where things stand" and add
a three-line entry to the session log below.

## Blind spots (what the connected folder cannot show)

The agent's memory file and session transcripts (WSL home, not in the repo); anything the agent staged into
the Prism instances (`C:\Users\alvin\AppData\Roaming\PrismLauncher\instances` is not connected and a grant
request was refused); the owner's in-game results; gradle build output beyond what FIX_LOG quotes (only the
gametest server logs are on disk); usage-limit hits and stray-process checks (the agent's word); the
uncommitted working tree beyond file names and mtimes.

## Session log

- 2026-09-04 (advisor session 1): role taken; handoff created in the repo and rewritten from the records;
  transfer audit, blind-ruling re-check, ENT-S-120 draft ruling and look-session readiness delivered.
- 2026-09-04 (session 1, evening): owner ruled on T9 ratifications + the MOD-033 extension, harness F1,
  the Queen re-tune, ENT-S-120 (+ both-modes reach), Blockbench, and the docs housekeeping; paste-back
  handed over; Prism instances connected for the look-session precondition check.
