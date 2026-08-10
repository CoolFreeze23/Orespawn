# Phase D Rollup — Structures (final, 2026-08-10)

Phase D is COMPLETE. This rollup records the terminal state of every
D-owned finding, the open items handed to Phase E, and the full
pending-manual-tests list. Slices: D1-D4 (audits + castles/towers), D5
(BasiliskMaze, Nightmare Rookery, EnormousCastle, pattern doc, WGEN-005),
D6a (six strong-model structures + trees), D6b batches 1-4 (22 mechanical
structures + 2 sweep-surfaced ports + the full 50-outcome Dungeon Spawner
Block table) + this close-out.

## 1. What Phase D shipped (structures)

All 1.7.10 OreSpawn structures now generate (or live-build via the
Dungeon Spawner Block), each ported line-by-line from
`reference_1_7_10_source` with a verified extraction spec in
`phase_d_reports/d6_extraction/` (D6-era) or an audit spec (D1-D5 era):

- **Mining:** BasiliskMaze, KyuubiDungeon, EnderKnightDungeon (mining
  variant, LOWEST_GRASS_36).
- **Islands (D4 roll):** EnormousCastle King+Queen, Nightmare Rookery,
  Robot Lab, Greenhouse, White House, CloudSharkDungeon, MiniDungeon,
  CephadromeAltar, StinkyHouse, Pumpkin, Rainbow, RubyDungeon,
  GenericDungeon, unstable anthills.
- **Overworld:** PlayPool, WaterDragonLair, GoldFishBowl,
  GirlfriendIsland, MonsterIsland, FrogPond, HauntedHouse,
  LeafMonsterDungeon, SpitBugLair, BouncyCastle, RubberDuckyPond
  (all behind the restored DisableOverworldDungeons gate).
- **Village dim:** DamselInDistress, SpiderHangout (SpiderDriverEnable
  gate), RedAntHangout.
- **End:** EnderCastle, EnderDragonHospital, EnderReaperGraveyard,
  EnderKnightDungeon (end variant).
- **Crystal:** RotatorStation, RoundRotator, CrystalHauntedHouse,
  CrystalBattleTower, UrchinSpawner gate, fairy/royal trees.
- **Swamp:** SpitBugLair; **Desert:** BouncyCastle; **snow-border:**
  Igloo (builder + DSB only — see §3).
- **Dungeon Spawner Block:** all 50 original `nextInt(50)` outcomes live
  (ITEM-020), with the original clickedY+1 offsets (43/44/45) and
  recentring-cancel offsets so DSB builds land exactly at the clicked
  position (30/31/36/41/42).

Infrastructure: `LegacyDungeonStructure`/`LegacyDungeonPiece` with 13
placement modes, per-JSON `placement_mode` override (dual-dimension
structures), deterministic per-piece RNG stitching, `buildNow` live-build,
`terrainStateIfInChunk` sanctioned probe, facing-aware loot chests, loot
convention one-table-per-(list, fill formula), C7 sqrt spacing
equivalence, salts 84301-84374 + 10387399 all unique.

## 2. D-owned findings — terminal states (close-out)

Closed this close-out (with the D6b batch-4/close-out commits):

| Finding | Terminal state |
|---|---|
| WGEN-042 (~25+ structures absent) | FIXED — every structure ported; Igloo placement carved out to its own finding (§3) |
| ITEM-020 / WGEN-036 (DSB pool 50→2) | FIXED — all 50 outcomes wired, 400t fuse faithful |
| WGEN-014 (Mining dungeons) | FIXED — maze D5, Kyuubi D6a, EnderKnight b4, BeeHive restored (C) |
| WGEN-018 (Village structures) | FIXED — Damsel b3, Spider/RedAnt hangouts b4 |
| WGEN-021 (Islands D4 structures) | FIXED — all D4 builders ported b1-b3 + D5; unstable anthills wired to island_biome |
| WGEN-033 (End structures) | FIXED — Hospital + EnderCastle D6a |
| ITEM-064 (LESS_ORE wiring) | FIXED — overworld half in C, Mining half via WGEN-011 (C) |

Closed earlier in Phase D (already terminal in the ledger): WGEN-005,
WGEN-037, WGEN-038, WGEN-040, WGEN-044, WGEN-045, WGEN-051..062,
ITEM-062, ITEM-065 (DEFERRED, user-approved), ITEM-066, PN-014, PN-018,
BOSS-045, BOSS-046, plus the D6b batch findings WGEN-063+ (see §4).

## 3. Open items handed to Phase E

| Item | Why open | Phase E owner note |
|---|---|---|
| Igloo worldgen placement (new finding, was inside WGEN-042) | The original generates igloos on snow-biome borders with a frequency scheme that has no clean biome-tag mapping (igloo_spec.md §7.3, NEEDS_DESIGN_RULING). Builder + DSB type 20 shipped. | Decide border-biome mapping + frequency; must honor the DisableOverworldDungeons gate |
| ENT-A-054 (Boyfriend AI remainder) | Ranged attack (ENT-A-055), Jealousy goals, MoveIndoors — entity work, not structure work | Phase E entity slice |
| ENT-A-083 (Cephadrome flight) | Rider-controlled flight system — entity work | Phase E entity slice (signature feature) |
| WGEN-003 (Block-of-Ruby veins) | Ore feature never added | Feature + add_ores wiring |
| WGEN-004 (vanilla-ore boost) | Six boost features never added | Feature + add_ores wiring |
| WGEN-007 (wild crop patches) | Strawberry/corn/tomato patches never added | Random-patch features |
| ITEM-023 (ZooCage block form) | Item-based flow accepted in C; placed-cage block undecided | Design decision + block if wanted |
| AntRobot spawn-path persistence (red_ant_hangout_spec.md S2) | Kit/egg-spawned Robot Ants lack removeWhenFarAway/persistence overrides the original had; the structure's robot is spawnPersistent-covered | Entity persistence pass |
| UTF-8 BOMs in 70 datapack JSONs | Pre-existing; Gson-tolerant, breaks strict tooling | Running as a separate background task |

## 4. D6b batch-4 + close-out findings (all new this slice)

See AUDIT_FINDINGS.md WGEN-063+ / ITEM-067+ entries (added by
tools/d6b_ledger_patch.py): greenhouse plant-table drift (sugar cane/rice
restored), DisableOverworldDungeons dead config (gate restored), royal
altar box clipping, ALIEN_WTF box clipping, greenhouse double-door entry,
white-house door + button, CrystalBattleTowerFeature deletion (invented +
dead), DSB Robot Lab offset, feature-core chest facings, bee/mantis egg
loot restoration, CrystalMazeFeature audit verdict. Box widenings reseed
affected piece RNG for existing seeds — documented delta (pre-release).

## 5. Pending manual tests (complete list)

Everything under TESTING_CHECKLIST.md sections:
- **§Audit Parts 2-4** (Shadow, Greenhouse, Robot Lab, White House,
  AlienWTF, LeonNest, royal altars, challenge towers) — including the
  close-out door/plant/box regressions listed at the end of §D6b.
- **§D5** (BasiliskMaze /locate + interior, Nightmare Rookery,
  EnormousCastle King/Queen, SpawnOres pool, 116 egg recipes).
- **§D6a** (EnderCastle both dimensions, IncaPyramid, KyuubiDungeon,
  Hospital+MonsterIsland, Robot Lab annexes, fairy/royal trees).
- **§D6b batches 1-4** (the 24 structures + DSB full-sweep lines + the
  three regression lines added at close-out).
- **DSB spot-checks:** place Random Dungeon Spawner blocks until each new
  outcome class is seen at least once (types 4/5/6/8/25/31/33/36/41/42/45
  especially — adapters + offset corrections); verify royal altar and
  robot lab DSB builds are centered on the clicked position; verify
  types 43/44/45 build one block up.
- **Config gates:** disableOverworldDungeons=true suppresses all 11
  overworld dungeon types (fresh chunks); spiderDriverEnable=false
  suppresses Spider Hangout worldgen but NOT its DSB outcome; lessLag
  halves Islands D4 structure rates; urchinEnable=false stops new Crystal
  urchin spawners.

## 6. Ledger

After the close-out patch: see `tools/ledger_reconcile.py` output in the
close-out commit message. Phase D owns zero open findings; every former
D-owned open item is either terminal or re-owned to Phase E per §3.
