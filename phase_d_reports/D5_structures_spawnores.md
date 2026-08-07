# Phase D — Slice D5: representative structures + SpawnOres pool (2026-08-08)

Scope per the approved slice plan: port 2-3 representative structures
(BasiliskMaze, the Nightmare structure, one Islands "D4" castle), write
`structure_conversion_pattern.md` thorough enough that the remaining ~22 D6
structures are mechanical application, and restore the ~105-type SpawnOres
block pool (WGEN-005) unblocking ITEM-062's 116 water-bucket egg recipes.

Paths: `orig:` = `reference_1_7_10_source/sources/danger/orespawn/`,
`port:` = `src/main/java/danger/orespawn/`, `RES:` = `src/main/resources/data/orespawn/`.
Extraction specs (all independently verified against the originals):
`d5_extraction/basilisk_maze_spec.md`, `d5_extraction/nightmare_spec.md`,
`d5_extraction/enormous_castle_spec.md`, `d5_extraction/spawn_ores_spec.md`.

Findings closed: **WGEN-037 FIXED · WGEN-038 VERIFIED-CORRECT · WGEN-005 FIXED ·
ITEM-062 FIXED · WGEN-051..057 FIXED (new) · ITEM-066 FIXED (new)**;
WGEN-042 and ITEM-020 advanced to PARTIAL with named remainders (D6).
Ledger: 613 IDs (605 + 8 new), 454 terminal / 159 open, `tools/ledger_reconcile.py` green.

---

## 1. Infrastructure extensions (port:world/structure/)

| Change | Where | Why |
|---|---|---|
| Asymmetric piece bounding boxes | `LegacyDungeonPiece.DungeonType` 6-arg ctor (minX/maxX/down/up/minZ/maxZ) | BasiliskMaze reaches X −8..+64 from its origin; a symmetric box wastes chunk replays or (as the towers proved, WGEN-055) silently clips |
| `PlacementMode` per type | `DungeonType.PlacementMode` + `LegacyDungeonStructure.findGenerationPoint` dispatch | original `add*` methods anchor differently: chunk-centre heightmap (legacy types), lowest-of-36-columns (maze, OSW:2573-2597), Islands grass + jitter + LessLag (rookery/towers, OSW:2203-2274) |
| `buildNow(ServerLevel, BlockPos, DungeonType)` | `LegacyDungeonPiece` | the Dungeon Spawner Block path (orig DSB:52-202) builds live at the block pos with the level RNG; the piece's whole box becomes the write window so nothing chunk-clips |
| `placeLootChest` (+ facing variants of both chest helpers) | `LegacyDungeonPiece` | data-driven chest lists (C7-approved treatment) + the originals' chest facing metadata (WGEN-056) |
| `spawnPersistent` | `LegacyDungeonPiece` | orig `spawnCreature` + `func_110163_bv()` pattern (BasiliskMaze.java:243-252,398-410); yaw drawn by the caller to keep the RNG stream identical across chunk passes |
| Per-structure generator classes | `BasiliskMazeGenerator`, `NightmareRookeryGenerator` | keeps the 2400-line piece from growing ~500 lines per D6 structure; dispatch stays in `postProcess` |

The cross-chunk RNG stitching contract (draw unconditionally, gate only
writes, never read world state — model it in memory instead) is codified in
`structure_conversion_pattern.md` §1 step 3.

## 2. BasiliskMaze — WGEN-037 FIXED

Line-by-line port of orig `BasiliskMaze.java` (460 lines) as structure
`orespawn:basilisk_maze` (`BasiliskMazeGenerator`, dungeon_type BASILISK_MAZE).

| Element | Original | Port | Cite |
|---|---|---|---|
| Depth roll | `20 + nextInt(10)` | same, first piece-RNG draw | BM:31 |
| Cavity clear | 60×30, maze half 5 high / castle half 7; antechamber strip 5×6×30 | `clearArea` | BM:254-276 |
| Maze | randomized Prim over 10×10 grid, 3-block cells, border flags 16/32/64/128, `rnd(n)` ∈ [1,n], x-major outlist, frontier promote order T/R/B/L, `findInNbr` checks the rolled direction BEFORE cycling | `computeMazeWalls` + helpers, `nextInt(n)+1` on the piece RNG | BM:39-110,147-241 |
| Wall raster | `drawSide` inclusive endpoints, ≥30 clamp, 3-high obsidian | bitmap + `placeMazeWalls` | BM:112-145 |
| Entrances | west scan ascending probes local x=1, east scan DESCENDING probes x=28; 3-high carve | `openMaze` probing the in-memory bitmap (the original read back its own writes — impossible across chunk passes; the bitmap holds exactly that state) | BM:278-297 |
| Castle floor + traps | 60×30 obsidian; 80 lava traps (+1..+28 both axes) maze half; 20 RTP traps castle half | same, `BLOCK_TELEPORT` | BM:303-313 |
| Ceilings/walls/shells | bedrock ceilings +4/+6; triple east/north/south shells; divider lip +5 | same | BM:314-347 |
| Antechamber | sandstone floor x−4..−1, obsidian ceiling +5, iron-ore walls, 3 sandstone pillars, 3 Extreme Torches | same | BM:348-384 |
| Redstone torches | (+30, +4, z+2/15/27) | same | BM:385-387 |
| Chests | `2 + nextInt(3)` chests at (+58, +1, z+2+2k), torch at +4 above each; fill 5-10 weighted stacks | `placeLootChest` → `chests/basilisk_maze` (31 entries, total weight 495, rolls uniform 5-10) | BM:388-396 + BM:28 |
| Basilisks | 3 at (+45/46/47, +1.01, +15), random yaw, `func_110163_bv` — NO spawner blocks | `spawnPersistent(BASILISK, …)` | BM:397-410 |
| Entrance pyramid + shaft | 9 sandstone rings 20×20→4×4; 4×4 bedrock shaft, 2×2 air core, spiral obsidian step cycling (1,1)→(2,1)→(2,2)→(1,2), descends to −depth+1 punching the antechamber ceiling | `makeEntrance` | BM:413-458 |
| Worldgen anchor | 6×6 column sample at offsets {0,3,6,9,12,15}, topmost solid in Y 128..31, strictly-lowest wins, require >40, origin −2 | `LOWEST_SURFACE_36` mode (getBaseHeight−1 = predicted topmost solid) | OSW:2573-2597 |
| Frequency | Mining rotation: `recently_placed==0 && nextInt(95)==1` then `nextInt(7)`, i==0 → 1/665 | mining_biome set spacing 26 / separation 13 / salt 84330 (WGEN-039 equivalence, C7-approved) | OSW:79-101 |
| DSB trigger | type 23 at the block pos (no scan/offset) | outcome 23 → `buildNow(BASILISK_MAZE)` | DSB:122-124 |

Loot-table notes: CagedGirlfriend (orig OSM:5432, no dedicated port item) is
`orespawn:caged_mob` + `set_components {orespawn:caged_entity: orespawn:girlfriend}`
— flagged MISSING-IN-PORT (item identity) in the spec, not blocking. The
original's random-slot fill could overwrite earlier stacks; the loot-pool form
cannot — same documented approximation as every chest conversion since C6.

## 3. Nightmare structures — WGEN-038 VERIFIED-CORRECT, Rookery ported (WGEN-042 partial)

**WGEN-038 (NightmareDungeon):** the audit asked for a port; exhaustive search
proves the class is never instantiated in 1.7.10 (single occurrence in the
whole tree = its own declaration; no RTP pathway builds dungeons —
`nightmare_spec.md` §1). Porting it as generating content would invent
behavior → VERIFIED-CORRECT (audit premise wrong), spec retained for the
record (sealed 25×25×12 RTP-floored box, 50/50 EmperorScorpion/Nightmare
spawner, 31-entry gear list).

**Nightmare Rookery** (the structure that actually generated), orig
`GenericDungeon.makeNightmareRookery` GD:5242-5312 → `orespawn:nightmare_rookery`:

| Element | Original | Port | Cite |
|---|---|---|---|
| Two ridge passes | 26 columns each, i −5..20; Z drift `+= nextInt(3)−1` NOT reset between passes | `buildSpikeRidge` called twice, drift threaded through | GD:5249,5253,5283 |
| Pillars | `h = 1 + nextInt(20)` stone columns; 4 independent `nextInt(j+5)==1` side bulges per block in +X/−X/+Z/−Z order | same, draws unconditional (stitching contract) | GD:5254-5268 |
| Caps | at first j≥18: spawner "Nightmare" (= PitchBlack, OSM:4023) at j+2 sitting on chest at j+1, pillar truncates | `placeSpawner(PITCH_BLACK)` + `placeLootChest`, `break` | GD:5269-5279 |
| Loot | `NightmareRookeryContentsList` 10 entries, total weight 270, fill 4-8 | `chests/nightmare_rookery`, rolls uniform 4-8 | GD:29,5278 |
| Anchor | LessLag 50% skip; corner+nextInt(8); grass scan Y20→5, anchored AT the grass block | `ISLANDS_GRASS` mode (plane grass = getBaseHeight−1; scan window kept as accept bounds 5..20) | OSW:2253-2274 |
| Frequency | Islands D4 roll 1/100 × i==14 of nextInt(19) → 1/1900 | island_biome set 44/22, salt 84331 | OSW:132-178 |
| DSB trigger | type 38 | outcome 38 → `buildNow` | DSB:167-169 |

The original's `D4BigSpaceCheck`/shared `recently_placed` map onto
structure-set spacing per the C7 precedent (see
`tryPlaceIslandsGenericDungeon`'s documented treatment).

## 4. Challenge Towers (EnormousCastle/Q) — reconciliation, WGEN-051..056 + ITEM-066 FIXED

The towers were already identified as the `makeEnormousCastle`/`Q` port
(WGEN-043, C7). D5 diffed the entire implementation against the originals
(GD:191-786 King / GD:6393-6987 Queen, spec §3-§8) and reconciled:

| ID | Defect | Fix |
|---|---|---|
| WGEN-051 | invented `level = 6` lock ("QA Fix") | original roll restored: `1 + nextInt(6)`, `≤3 → +3` two-thirds of the time (GD:202-205); archived MOD-012 |
| WGEN-052 | invented scaffolding columns (2 sites) | removed; the originals place no climbable blocks anywhere (verified GD:191-786/6393-6987); archived MOD-012 |
| WGEN-053 | invented loot palettes | `chests/challenge_tower_level1..5` transcribe level1-5ContentsList (GD:57-61) entry-for-entry — totals 165/235/235/255/1285 asserted by `tools/d5_gen_tower_loot.py`; level-5's 83-egg jackpot complete (CriminalEgg → band_p_spawn_egg per WGEN-017) |
| WGEN-054 | "Jumpy Bug" → SpitBug | → `ENTITY_TROOPER_BUG` ("Jumpy Bug" is TrooperBug's EntityList name, OSM:3943) at all 4 ladder sites |
| WGEN-055 | placement: heightmap-centre anchor, 44/22 sets (vs C7's approved 36/18 = 1/1267 text), ±40 box clipping the worm ring (x,z −28..+55, GD:362-374) | ISLANDS_GRASS anchor (grass + nextInt(8) + LessLag, OSW:2203-2228); sets → 36/18; box → asymmetric −39..+57 / −30..+57 / −4..+85 |
| WGEN-056 | chest facing lost | facing-aware placement; meta 5/4/3/2 (GD:744/754/765/776) → EAST/WEST/SOUTH/NORTH toward room centre |
| ITEM-066 | inert `prince_egg`/`princess_egg` trophy items in the prize chests AND the Queen/Teen/Adult drops | all five consumer sites → functional `the_prince_spawn_egg`/`the_princess_spawn_egg` (orig ThePrinceEgg/ThePrincessEgg are ItemSpawnEggs, OSM:5616/5630; drops at TheQueen.java:192, ThePrinceTeen.java:318, ThePrinceAdult.java:314); trophy items/models/lang removed |

Also verified faithful and left unchanged: all §3/§4 geometry (28×28 base,
iron-bar cage, skirt+fence, 16 corner + 3 scorpion spawners, six floors
26/26/24/24/22/22 × 10/10/9/9/8/16 with the j-accumulates-outside-guards
quirk, stair/hole chaining, platform/arm/stair to x−37, ~74-spawner worm
ring), the decor rooms (mob/reward matrices, RTP blocks on the prize floor,
alternating hole corners), and the reward-6 fixed chest slots. King/Queen
palette differences (stone/obsidian, gold/ruby accents, quartz/amethyst
spire, Terrible/Lurking Terror, both mob ladders) match spec §7's 10-row
diff. reward-6 remains reachable only on the bottom floor of a level-6 tower.
The Queen's decors 2-6 calling the King's `fill_chests` (GD:6742 etc.) is
reproduced by sharing one table set per reward tier.

DSB outcomes: 2 (King, DSB:59-61) and 47 (Queen, DSB:194-196) wired.
(Note: DSB type 12 is `makePlayPool`, not the castle — an early D5 excerpt
misread this; the spec settles it.)

## 5. SpawnOres pool — WGEN-005 FIXED (PN-010 closed)

106 new `OreGenericEgg` blocks (119-row master table, spec §2; 13 already
existed) + `SpawnOresPoolFeature` reproducing the original roll structure —
see the feature's Javadoc for the line-cited pseudocode (rate 28, dice 20
overworld / 30 dims, 1/20 +30 burst, LessOre ÷3, Y 50..127 discard filter,
7-in-104 rare tier, exact `nextInt(98)`/`nextInt(7)` switch orders verified
identical across OSW:371-801 and COG:37-467). Placement: biome modifiers
`add_spawn_ores` (overworld), `add_spawn_ores_dims` (Utopia/Village/Chaos —
the CP1/CP3/CP6 call sites; CP4 Islands and CP5 Crystal correctly get none),
`add_spawn_ores_mining` (passes 3 / less-ore 1, CP2:191-195). The Crystal
dimension's separate 11-type pool was already at parity (WGEN-023).

Retired interim artifacts (all were PN-010): dragon/kraken configured+placed
features (6 placed incl. never-wired `_dim`/`_mining` orphans),
`add_boss_spawn_blocks`, and the invented `ancient_dried_egg`
block/worldgen/assets (archived MOD-013). The kraken/dragon BLOCKS remain —
they are pool members c50/c53.

Assets: every block reuses the original texture already in-repo (125 ore*.png,
one case-fold `oremothra`), cube_all model, self-drop + survives_explosion
loot, and the ORIGINAL "Ancient Dried <Mob> Spawn Egg" display names parsed
straight from OSM:2665-3021 — including renaming the 11 crystal egg-ores and
kraken/dragon whose C7 names deviated. Generator: `tools/d5_gen_spawn_ores.py`
(fails loudly on any unresolved mapping).

## 6. Egg recipes — ITEM-062 FIXED

116 shapeless `water_bucket + spawn block → 1 egg` JSONs + 3 nine-part
combines (Mobzilla/King/Queen full blocks, OSM:2886/2892/2898), generated
from the same verified table. The bucket returns via the modern crafting
remainder = the 1.7.10 container-item behavior. Mapping decisions:
vanilla-mob outputs use modern vanilla eggs — including
ender_dragon/iron_golem/snow_golem/wither (all exist since 1.20.5; verified
against the 1.21.1 client jar after the spec initially claimed otherwise);
`CriminalEgg` → `band_p_spawn_egg` (WGEN-017: Criminal = BandP);
`EnchantedCowEgg` → `enchanted_apple_cow_spawn_egg` (the port's consolidation
target for the original EnchantedCow); `RedCowEgg`/`GoldCowEgg`/`SpyroEgg`/
`LeonEgg` → the faithful-lineage eggs (red_cow/gold_cow/spyro/leon). The
WitherBossEgg meta-64 quirk (OSM:2745) is meaningless post-flattening and
dropped. The original's asymmetry is preserved: Mobzilla part AND full blocks
worldgen (c68/c69); King/Queen full blocks are craft-only (parts c86/c97);
CrystalCow's block generates nowhere (no pool slot) yet keeps its recipe.

## 7. Mapping deltas + notes recorded

- **PN-015** — worldgen structure randomness is seed-stable (deterministic
  piece RNG; required by chunk-replay stitching; DSB path keeps live RNG).
- **PN-016** — SpawnOres veins target `#minecraft:stone_ore_replaceables` at
  the `underground_ores` step (orig: bare stone, SpawnOres-first ordering).
- **MOD-012** — tower QoL pack (guaranteed prize + climb aids) archived.
- **MOD-013** — ancient-dried-egg rehydration archived.
- C7 bookkeeping corrected in passing: the WGEN-043 resolution's "existing
  36/18 spacing" statement now matches the shipped files (they were 44/22);
  `ModBlocks`' "Mobzilla part is craft-only" comment inverted the original
  asymmetry and was rewritten.

## 8. Observations for later phases (not acted on)

- **Phase-14 invented entities:** `APPLE_COW`, `GOLDEN_APPLE_COW`,
  `VAMPIRE_BUTTERFLY` are wiki-canon additions with no 1.7.10 source (their
  own Javadoc says so), and the original `EnchantedCow` was renamed/merged
  into `ENCHANTED_APPLE_COW`; `BABY_DRAGON` coexists with the faithful
  `SPYRO`. C7 already stripped their invented natural spawns; the entity
  registrations themselves are Phase E audit material (invented-content
  ruling may apply).
- The `AncientDriedEggBlock` removal also removed the only in-code consumer
  of the 7-vs-9 DINO_POOL doc mismatch the spec flagged (§10.12) — moot.

## 9. Verification

- **Independent re-derivation pass:** four independent verifiers re-derived
  every number in the D5 code from the ORIGINALS (not the specs): maze
  (all 460 orig lines: Prim semantics, drawSide clamp, castle rows, entrance
  math, box extents, 31-entry loot, RNG contract), rookery (full method +
  placement + 10-entry loot + box + set odds), towers (level roll, hole-corner
  matrix, both mob cascades, reward flow, chest slots/facings, all five loot
  lists entry-for-entry, extents, sets, DSB, trophy-egg sweep), spawn ores
  (all 98+7 pool slots re-derived case-by-case from both original switches,
  roll structure, dim mapping, 12-recipe sample, retirement sweep, lang
  sample). Findings and dispositions:
  - **CRITICAL (fixed):** `SpawnOresPoolFeature` read `LESS_ORE` as an int —
    it is a BooleanValue (`lessOre`, default false); would not compile.
  - **MAJOR (fixed):** `data/minecraft/tags/block/mineable/shovel.json` still
    listed the removed `ancient_dried_egg` (a stale entry breaks the whole
    vanilla tag at datapack load). Cleaned; the sweep also surfaced the
    invented Extractor recipe `extracting_trex_dna.json`
    (ancient_dried_egg → trex_tooth) — deleted with the ADE retirement
    (no original counterpart; the original has no ancient-dried-egg item).
  - **MINOR (documented):** the LOWEST_SURFACE_36 mapping cannot reproduce
    one original subtlety — where modern terrain exceeds Y128 (impossible in
    the 128-tall 1.7.10 world), the original's block scan could still find a
    cave floor/overhang inside the 31..128 window, while the noise-surface
    probe disqualifies the column. Comment corrected in
    `LegacyDungeonStructure`; inherent to structure-start-time placement.
  - Everything else survived unchanged (rookery and towers: zero findings).
- `tools/ledger_reconcile.py`: 613 IDs, 454 terminal / 159 open, green.
- Loot totals asserted in-generator (165/235/235/255/1285; 495; 270).
- Full `./gradlew build`: recorded in FIX_LOG at commit (gate).
- In-game checks appended to TESTING_CHECKLIST.md §D5.
