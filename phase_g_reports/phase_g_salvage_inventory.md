# Phase G salvage inventory (2026-09-02)

Status: **triage record, owner decisions pending.** Nothing in this document is
landed on `master` except where a commit hash on `master` is named.

## 1. What happened

On 2026-08-31 the owner ran the Phase G brief (`PHASE_G_PROMPT.md`) through an
agent orchestrator (Claude Fable director + Codex workers, separate git
worktrees under `~/.ao/data/worktrees/orespawn/`). The owner's instructions to
the controller were "okay continue" (which the controller turned into
"adopt all ten G0 recommendations as written") and, later, "just finish the
entire thing, don't stop until everything is done and it's built" (which became
the G2–G5 authorization). Work stopped at ~01:55 JST 2026-09-01 with every
worker idle or exited.

Landed on `master` on 2026-09-02 (see FIX_LOG "PHASE G LANDING"):
`aa5b863` G0 design + inventory, `fcf0f48` G1 converter + parity harness,
`d87f81b` landing fix (G1 benchmark provenance regenerated; the committed
`fcf0f48` failed its own build gate on a fresh checkout).

Everything else was left uncommitted in eight worktrees. On 2026-09-02 each
was **snapshot-committed verbatim onto its own `ao/*` branch** so it is
recoverable and diffable. The snapshots are NOT gated, NOT reviewed, and NOT
for merge; `master` does not contain them.

| Worktree | Branch | Snapshot | Size | Verdict |
|---|---|---|---|---|
| orespawn-4 | `ao/phase-g0-clean` | `bc3a931` | 51 files, +76,735 | ideas only — see §3 (G2) |
| orespawn-5 | `ao/phase-g4-audit` | `e66b145` | 20 files, +7,097 | rewrite small — §3 (G4 audit) |
| orespawn-6 | `ao/phase-g-q6-live` | `7dc7a95` | 19 files, +9,572 | hold — §3 (Q6) |
| orespawn-7 | `ao/phase-g3-runtime` | `0d238ba` | 31 files, +13,807 | re-land core — §3 (G3) |
| orespawn-8 | `ao/phase-g4-server-pose` | `98d6df4` | 32 files, +6,106 | keep for Tier 1 — §3 (pose) |
| orespawn-9 | `ao/phase-g-case-fix` | `2636d20` | 11 files, +7,195 | **finding is real; fix differently** — §2 |
| orespawn-10 | `ao/phase-g1-evidence-fix` | `d7acf3f` | 13 files, +8,180 | discard (superseded by `d87f81b`) |
| phase-g-integration | `ao/phase-g-integration` | `394a7b6` | 2 files | discard (superseded by `d87f81b`) |

Quarantine is reversible: `git -C <worktree> reset --soft HEAD~1` restores the
dirty state; `git worktree remove` + `git branch -D` deletes a lane once its
salvage value is exhausted. Neither has been done.

## 2. The one live bug the run uncovered — texture filename case

**Finding (verified independently on 2026-09-02):** git's index tracks **147**
entity textures under uppercase names (`Kyuubi.png`, `GammaMetroid.png`,
`Fireflytexture.png`, `AttackSquid.png`, `Bird1.png`, …). This Windows
checkout has all 428 files lowercase on disk, and `core.ignorecase=true` hides
the difference from `git status`. Every Java reference is lowercase (a
`ResourceLocation` cannot be anything else), and the published beta jars
contain 428 lowercase entries — **only because they were built from this
particular working tree.** A fresh clone on Linux, macOS, CI, or even a new
Windows checkout writes the index names to disk, Gradle copies them into the
jar, the jar filesystem is case-sensitive, and those mobs render with missing
textures. Of the 147, **57 are referenced by a literal lowercase Java path**
(e.g. `Kyuubi.png` ← `"textures/entity/kyuubi.png"`); more are reached through
dynamically built names (`bird1..6`, the skin series). No Java literal contains
an uppercase path (none could — `ResourceLocation` rejects it), and
`provenance_byte_identical_assets.txt` already records every port path in
lowercase, so the intended names are unambiguous.

The orchestrator found this (worker orespawn-9: "68 case misses") but, bound by
a ruling it over-read ("keep all 428 shipped names" was about dedupe, not git
paths), built a build-time alias generator (67 lowercase aliases into a second
resource root, `processResources` exclusions, jar/jarJar entry rules, a case
contract v2, a mutation-tested audit). That machinery is unnecessary.

**Recommended fix (Slice 1, small, gated):** rename the 147 index entries to
lowercase (`git mv -f Upper.png upper.png`; content untouched, disk unchanged,
jar output byte-identical to what ships today; the provenance file needs no
change), and add one `asset_audit.py` check: every texture path literal in Java must resolve to
a `git ls-files` entry exactly (index names, not disk names) — that is the
mechanical guard. Orespawn-9's `Boyfriend`/`Girlfriend` skin-index clamping
(`normalizeSkinIndex`) is a separate, reasonable hardening; its
`ResourceSafetyTests` gametest is worth keeping in a simplified form.

## 3. Per-lane triage

### G2 foundation — orespawn-4 (`bc3a931`)
Contains: 16 Tier-3 `.geo.json` (coin, elevator, the three head sidecars,
island/island_too, purple_power, robot_1..5, rock_base, rotator, vortex), 5
static `.animation.json`, a 428-row `TEXTURE_MAP.csv`, a runtime
renderer-order contract JSON, `src/g2tool` (3 Java probes), 16 `g2_*.py`
tools (9,020 lines), and checkpoint/succession/blocker-register documents.
The orchestrator itself killed this lane's first worker and marked the
worktree "salvage"; its blocker register still has B09 (Blockbench
round-trip), B11 (case aliases) and B12 (G3 binding) open.
Verdict: **ideas only.** The rigs are unwired assets and would be dead weight
in the jar until a runtime consumes them (§ G3). The converter that produced
them is already on `master` (G1), so they can be regenerated in seconds when
needed. Salvage the `TEXTURE_MAP.csv` schema and the vanilla-vs-GeckoLib face
order finding (`down/up/west/north/east/south` vs
`west/east/north/south/up/down`) — the latter is a real converter requirement.

### G3 replacement runtime — orespawn-7 (`0d238ba`)
Contains: a `GeoReplacedEntity`-based seam (6 client classes, ~500 LOC:
descriptor, replacement animatable, generic model, shared renderer, Beaver
descriptor), a dev-only selector (`-Dorespawn.dev.phaseG.beaverRenderer=
classic|candidate`, production-forced classic), a 69-line MHLib change that
makes the bone-collector layer a no-op for profiles with `sync-with-model=false`
or no synced bones, `PhaseLockedKeyframeController`, and G3 proof documents
("hash surface v6", "selector-governance ACK v2").
The architectural idea is the best one in the run: `GeoReplacedEntity` lets a
species get a GeckoLib renderer **without editing its entity class**, keyed
by `DataTickets.ENTITY`, so 100+ entity classes stay untouched. Reviewed
against pinned 4.8.4 bytecode (`javap` citations are in the snapshot).
Verdict: **re-land the core** (seam + selector + MHLib no-op) as one ordinary
gated slice after a fresh review; drop the hash-surface/ACK documents. Its own
FIX_LOG draft records a "HARD RED": the seam does not yet consume G2's face-order
contract, so Beaver opacity alone does not prove cross-rig draw order — keep
that as the slice's open item.

### Server-authoritative pose foundation — orespawn-8 (`98d6df4`)
Contains: `entity/hitbox/pose/` (`ServerPoseInputs`, `BakedHitboxRig`,
`HitboxPoseBinding`, `MHLibPoseDriver`; 2,234 LOC, explicitly unhooked), an
independent Python FK oracle (1,546 lines; 8,376 dense rows at max error 0), a
provenance schema, and a self-disclosed provenance correction (an early fixture
had fabricated placeholder hashes; removed before review).
This is the Q3 ruling made concrete: new Tier-1 damage boxes are positioned by
a server-side evaluation of the rig, not by client bone packets.
Verdict: **keep, re-land when the first Tier-1 profile (MOD-025 King/Godzilla)
starts.** Review the 1,009-line `BakedHitboxRig` for size; the Python oracle can
stay under `tools/` as a test.

### GeckoLib asset-audit hardening — orespawn-5 (`e66b145`)
Contains: +4,434 lines in `tools/asset_audit.py` (Java type-identity proving,
masked-source parsing, dynamic-texture domain contracts), fixtures and two test
files, and a 3-line `ElevatorRenderer` `Mth.clamp` tidy.
The brief asked for ~5 checks: GeoModel resource triple resolves, geo/animation
filenames lowercase, profile `synched-bones` exist in the geo, every
`triggerableAnim` clip exists and declares `loop:false`. Those are ~100 lines.
Verdict: **rewrite small; do not merge the snapshot.** Add the §2 index-name
check in the same slice.

### Live Q6 benchmark infrastructure — orespawn-6 (`7dc7a95`)
Contains: a runbook, an init-script-attached `src/g1live` instrumentation set
(mixins counting MHLib packets/wire bytes, renderer selector witness), a
campaign/protocol schema, and tests. Never executed; explicitly
"infrastructure only".
Verdict: **hold** until a production-cutover candidate exists (after G3 core
lands and the animation-tolerance ruling is made). Keep the runbook's scene
definitions; the witness/attestation layers can be cut to a fraction.

### G1 evidence-policy migration — orespawn-10 (`d7acf3f`)
A 1,708-line policy module, 84 pinned tests, write-ahead-journal proof
publication, Gradle init-script pinning — all to survive a CRLF hash mismatch.
Verdict: **discard.** `d87f81b` fixed the same false red by regenerating the
smoke report with the gate's own writer. Nothing else in it is needed.

### Integration lane — phase-g-integration (`394a7b6`)
A FIX_LOG mojibake pass and a `.gitattributes` rule for FIX_LOG.
Verdict: **discard.** Superseded by the UTF-8 repair in `d87f81b`.

## 4. Proposed re-landing order (each a small, ordinary gated commit)

1. **Texture index-name normalization + audit guard** (§2) — **LANDED
   2026-09-02** (FIX_LOG BUG-038: 155 index-only renames, audit check 7). Fixes a live
   fresh-clone bug; no visual change on this machine; unblocks everything
   else because every later slice references texture paths.
2. **G3 core seam** (orespawn-7 core, ~330 LOC incl. MHLib no-op), dev-only
   selector, Beaver as the proof species — **LANDED 2026-09-02** (FIX_LOG
   "PHASE G SLICE 2"). Beaver uses the G1-approved code-driven pose, so the
   tolerance ruling (§5) was not needed for this slice; it still gates any
   artist-editable clip. Try it: `-Dorespawn.dev.beaverRenderer=candidate`.
3. **Small GeckoLib asset-audit checks** (rewrite of orespawn-5's intent).
4. **Tier-3 rigs**, regenerated from the landed converter and wired through
   the G3 seam — the first real in-game GeckoLib species besides the Queen.
5. **Server-pose foundation + first MOD-025 profile** (King), atomic
   manual-part replacement per the Q4 ruling.
6. **Live Q6** before the first production cutover of a Tier-1 boss.

## 5. Decisions the owner still has to make

- **Animation tolerance.** Baked keyframes cannot match `Mth.cos`'s lookup-table
  cosine within the 2e-6 rad threshold the run inherited (Catmull-Rom at 593
  probes: gait 7.2e-5, teeth 5.9e-5, tail 1.3e-5). Either accept a
  visually-invisible tolerance (~1e-3 rad) so Tier-2 clips become real
  artist-editable keyframes, or keep Tier-2 motion code-driven and shrink the
  Fiverr scope to new clips only. This decision gates slices 2 and 4.
- **Whether the ten G0 rulings stand as adopted by "okay continue".** The
  consequential ones: outright renderer replacement (no classic-visual config
  toggle); server-fed Tier-1 hitboxes; King/Godzilla head sidecars retained;
  all 428 shipped texture names retained (compatible with §2 — the jar already
  ships lowercase).
- **Worktree cleanup.** The eight `ao/*` lanes can be deleted once the slices
  above have mined them; until then they cost nothing.

*Prepared 2026-09-02 from a read of every worktree, the orchestrator's session
database, and the worker/director transcripts.*
