# Slice (f) dry run — the artist package generator over the landed species (2026-09-06, after the refuter)

Command: `python tools/artist_package.py package --out <scratch>\pkg\artist_handoff_dryrun` (tool 0.2.0; exit 0; 149 files,
10 MB; the generated `dryrun_summary.md/.json` and `warnings.txt` sit in that directory beside README_FIRST.md, INVENTORY.csv,
TEXTURE_MAP.csv and `entities/<registry>/`; stdout/stderr captured as `dryrun_stdout.txt` / `dryrun_stderr.txt`). Nothing was
written under the repository's `artist_handoff/`.

| entity | tier (as the artist reads it) | files | bones | cubes | clips shipped | clips in SPEC | goals | flags | attacking | strike/launch sites | textures | locked | unlabelled | round-trip | effort | warnings |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| robot_1 | Tier 3 | 8 | 27 | 27 | 0 | 0 | 5 | 1 | STATE | 0 | 1 | 0 | 20 | EQUAL, order kept | 0 h | 20 |
| robot_2 | Tier 3 | 8 | 15 | 15 | 0 | 0 | 5 | 1 | STATE | 1 | 1 | 0 | 4 | EQUAL, order kept | 0 h | 4 |
| robot_3 | Tier 3 | 8 | 19 | 19 | 0 | 0 | 5 | 1 | MIXED | 1 | 1 | 0 | 0 | EQUAL, order kept | 0 h | 1 |
| robot_4 | Tier 3 | 8 | 56 | 56 | 0 | 0 | 5 | 2 | EVENT | 3 | 1 | 0 | 0 | EQUAL, order kept | 0 h | 0 |
| robot_5 | Tier 3 | 8 | 11 | 11 | 0 | 0 | 5 | 1 | MIXED | 1 | 1 | 0 | 0 | EQUAL, order kept | 0 h | 1 |
| the_queen | Tier 1 (boss; the design's 'done' row) | 9 | 110 | 130 | 8 | 8 | 5 | 6 | STATE | 6 | 2 | 27 (26 keyed by the shipped clips; warn) | 0 | EQUAL, order kept | 33.5 h | 1 |
| rotator | Tier 3 | 8 | 27 | 24 | 0 | 0 | 0 | 0 | NONE | 1 | 1 | 0 | 0 | EQUAL, order kept | 0 h | 0 |
| vortex | Tier 3 | 8 | 1 | 1 | 0 | 0 | 0 | 0 | NONE | 1 | 1 | 0 | 0 | EQUAL, order kept | 0 h | 0 |
| beaver | Tier 2 | 8 | 9 | 9 | 0 | 11 | 8 | 0 | NONE | 0 | 1 | 0 | 0 | EQUAL, order kept | 12.8 h | 0 |
| coin | Tier 3 | 8 | 1 | 1 | 0 | 0 | 1 | 0 | NONE | 0 | 1 | 0 | 0 | EQUAL, order kept | 0 h | 0 |
| island | Tier 3 | 8 | 3 | 3 | 0 | 0 | 0 | 0 | NONE | 0 | 1 | 0 | 0 | EQUAL, order kept | 0 h | 0 |
| island_too | Tier 3 | 8 | 3 | 3 | 0 | 0 | 0 | 0 | NONE | 0 | 1 | 0 | 0 | EQUAL, order kept | 0 h | 0 |
| elevator | Tier 3 | 17 | 5 | 5 | 0 | 0 | 0 | 5 | NONE | 0 | 10 | 0 | 5 | EQUAL, order kept | 0 h | 5 |
| purple_power | Tier 3 | 12 | 36 | 18 | 0 | 0 | 0 | 1 | NONE | 1 | 5 | 0 | 0 | EQUAL, order kept | 0 h | 0 |
| rock_base | Tier 3 | 17 | 22 | 22 | 0 | 0 | 0 | 1 | NONE | 0 | 10 | 0 | 0 | EQUAL, order kept | 0 h | 0 |

Files per entity: `<shipped name>.geo.json` (a copy), `<shipped name>.animation.json` (a copy), `<registry>.bbmodel`,
`SPEC.md`, `spec.manifest.json`, `roundtrip.report.json`, `reference/SLOTS.md`, `textures/<canonical>.png` (one per
canonical image). "clips in SPEC" = the contract clips the SPEC's table lists for the species (Tier 3: none, per P2 /
open question 7; the Queen: her eight native clips = her ACCEPTED set). "strike/launch sites" for the Queen: `doHurtTarget`
:816, the area helper `doAreaDamage` at :1157 and :1186 (its body hurts each victim twice, :1429-1430), three launches.
"locked" = a profile's synched-bones plus ancestors; `warn` = the one PROVISIONAL policy (open question 16): keying a locked
bone is a WARN today and becomes a REJECT when the server-side evaluator lands (the Queen's shipped clips key 26 of 27 —
every locked bone but `Body1`). "effort" = the generator's estimate (bosses 8 h + 0.15 h/bone + 1.5 h per clip to author or
improve; Tier 2 4 h + 0.2 h/bone + 1 h/clip; a `calm_idle` that `idle` covers is not counted — the Beaver's 13.8 h of the first
run is 12.8 h now).

INVENTORY.csv: 145 rows (both registration forms; the first run's 128 missed the 17 `Builder.<X>of(` registrations) —
15 landed candidates, 104 classic only, 26 excluded. Excluded notes in the vocabulary that is true per row: 15 projectiles
(`extends ThrowableProjectile` / `LargeFireball` / `AbstractArrow` / the `LaserBall` family), 1 fish hook, 1 item entity, the
six cows + spider_driver as "Tier 0: a vanilla CowModel / SpiderModel drawn by <Renderer> (the design's 'Vanilla-model reuse'
table)", ant_robot / spider_robot as "Tier 0: solver-fed / native rig". The head sidecars (godzilla_head / king_head /
queen_head) are classic only / Tier 3 per the design's rows 56 / 62 / 69 with the note "a head sidecar — its renderer refuses
to draw (shouldRender false); an invisible gaze/damage hitbox, so it renders nothing and takes no artist work". New columns:
`artist_tier` (the Queen: "Tier 1 (boss; the design's 'done' row)"), `effort_source`.

TEXTURE_MAP: 428 shipped, 338 unique payloads, 86 duplicate groups, 90 redundant names, 262 referenced by Java, 42 strays;
law series girlfriend 0..40 / boyfriend 0..27 / swimshorts 0..17 / elevator 1..10 all contiguous. Stray classes: 28 armor
sheets (`<material>_1/_2.png`, byte-identical to `textures/models/armor/<material>_layer_N.png`), 5 GUI/atlas files
(items.png, textures.png, logo.png, spinners.png, girlfriendgui.png), 8 item-renderer weapon textures (referenced only by
`OreSpawnItemRenderer`: attitudeadjuster, battleaxe, bertha, chainsaw, queenbattleaxe, royal, slice, squidzooka — these are the
262 − 254 = 8 references the G0 design's model/renderer count left out), 1 dormant twin (`hammytexture.png`, byte-identical to
`textures/item/hammytexture.png`, referenced by nothing). Entity textures whose bytes match an item texture (bandp_pirate,
godzillahead, kinghead) are NOT strays: an entity renderer draws them. RockBase: 10 per-type textures (`RockBaseRenderer.textureFor`
:56-69 has ten constants; types 1 / 2 / 7 share `rocktexture.png`).

## `check` on the dry run's own folders and on mutated returns (scratch `pkg\refuter_mutations\`)

| folder | verdict | exit | the deciding line(s) |
|---|---|---|---|
| the_queen as shipped | PASS | 0 | 8 × `WARN clip '<name>' keys 26 locked bone(s)`; `WARN locked bones: 26 of 27 keyed across 8 clip(s) [lock_mode warn] — <policy>`; `OK checked 8 clip(s), 47559 keyframe value(s), 2 texture(s)` |
| the_queen as shipped, `--lock-mode reject` | FAIL | 1 | `REJECT locked bones: 26 of 27 keyed ...` (the REJECT policy fails the shipped file) |
| coin as shipped | PASS | 0 | `OK locked bones: none on this rig`; `OK checked 0 clip(s) ... 1 texture(s)` |
| beaver as shipped (the empty animation) | FAIL | 1 | `REJECT required clip 'idle' is missing`; `REJECT required clip 'walk' is missing` |
| beaver, a minimal return (idle + walk on rff / head / body) | PASS | 0 | nine `WARN optional clip ... not delivered`; `OK checked 2 clip(s), 24 keyframe value(s)` |
| beaver, a renamed bone (`tale`) | FAIL | 1 | `REJECT clip 'idle' keys unknown bone 'tale' (renamed?)` |
| beaver, `idle` loop false | FAIL | 1 | `REJECT clip 'idle' loop is false, the SPEC says true` |
| beaver, a Molang string value | FAIL | 1 | `REJECT clip 'idle' rff.rotation @ 0.5: value 'math.sin(...)' is a Molang expression / text (rule 7: numbers only)` |
| beaver, a key at 1.5 s in a 1.0 s clip | FAIL | 1 | `REJECT clip 'walk' has a key at 1.5 s beyond its animation_length 1 s` |
| beaver, a returned geo with a changed cube | FAIL | 1 | `REJECT beaver.geo.json: bone body cube[0] size: [8.0, 8.0, 10.0] -> [10.0, 8.0, 10.0]` |
| beaver, the file named `beaver_v2.animation.json` | FAIL | 1 | `REJECT beaver_v2.animation.json: wrong file name — the sheet names it beaver.animation.json` |
| beaver, plus a `beaver_preview.animation.json` | PASS | 0 | `WARN beaver_preview.animation.json: a _preview file is a Blockbench-only aid and is not delivered — PROVISIONAL (open question 15)` |
| a folder that does not exist | FAIL | 1 | `REJECT folder ... does not exist: nothing was returned` |

Every returned geo also draws `WARN <geo>: a geo was returned; the shipped rig is used regardless (do not re-export the geo)`.

## Every warning the generator raised (32)

- BONE_UNLABELLED (29): robot_1 Shape1, Shape10, Shape11, Shape12, Shape13, Shape14, Shape15, Shape15a, Shape16, Shape17,
  Shape18, Shape2, Shape2a, Shape3, Shape4, Shape5, Shape6, Shape7, Shape8, Shape9; robot_2 Shape3, Shape6, Shape7, Shape8;
  elevator shape1, shape2, shape3, shape4, shape5 — the unnamed classic parts; the glossary shows a generated placeholder
  with the cube's size and position; the owner names them in the seed.
- ATTACKING_UNCLASSIFIED (2): robot_3 "MIXED: a 10-tick pulse per in-range think tick: the flag is raised at line(s) [155]
  every 35 ticks (reloadTicker reset) while a target is in range — before the line-of-sight gate, so it pulses whether or not
  a shot fires — and cleared when reloadTicker < 25 at line(s) [144] AND cleared on target loss at line(s) [162]"; robot_5 the
  same shape with a 5-tick pulse every 20 ticks (:143 / :131 / :152); the SPEC lists every site with its enclosing guard and
  the outer chain for the owner's reading.
- LOCKED_BONES_KEYED (1): the_queen "the shipped clips key 26 of the 27 SPEC-locked bones (LHead, LHead12, LHead4, Lwing1,
  NeckL1, NeckL13...): keying a locked bone is a WARN today and becomes a REJECT when the server-side hitbox evaluator lands
  (contract §8.1 / P7 say REJECT; the pilot boss's shipped clips key 26 of her 27 locked bones, so REJECT would fail her own
  baseline) — PROVISIONAL, open question 16".
- No SEED_MISSING, no TEXTURE_UNMAPPED, no GOAL_UNCLASSIFIED, no GOAL_UNPARSED, no ATTACKING_GUARD_UNPARSED, no
  WISHLIST_UNACCEPTED, no ROUNDTRIP_DIFF, no GEO_AMBIGUOUS in the final run.
