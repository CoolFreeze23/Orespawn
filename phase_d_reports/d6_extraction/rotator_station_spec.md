# Rotator Station — extraction spec (D6)

**Original:** `orig:GenericDungeon.java:787-810` (`makeRotatorStation`)
**Status: ALREADY PORTED (worldgen path) — reconciliation spec.** The worldgen
trigger is live in `port:world/CrystalStructures.java:468-502`
(`tryPlaceRotatorStation`), audited number-by-number below (§9): **faithful, no
defects found**. The DungeonSpawnerBlock trigger (type 3) is **NOT wired** —
it currently falls to the generic-dungeon fallback
(`port:block/entity/RandomDungeonSpawnerBlockEntity.java:186`). Remaining work
is the DSB wiring only (§7).

**Mechanism decision:** NOT a `LegacyDungeonStructure`. This is a
decoration-phase direct builder with a dual trigger (OreSpawnWorld decoration
+ DSB), exactly the FairyTree/FairyCastleTree precedent in the pattern doc
§4 — one shared builder, two thin entry paths. The footprint is a 1×5×1
column, far below any threshold that would justify the structure pipeline;
the site scan reads world blocks, which is legal in the decoration regime
(pattern doc §4 "Read legality"). Consequences:

- **No DungeonType entry.** N/A.
- **No structure/structure_set/biome-tag JSONs.** N/A.
- **Salt 84355 (assigned to this task) is NOT consumed.** Return it to the
  free pool for the batch bookkeeping.

---

## 1. Entry points (every call site of `makeRotatorStation`)

Grep of the full original tree finds exactly two call sites (plus the
definition). Reachability confirmed — not dead code.

### 1a. Worldgen: `OreSpawnWorld.addRotatorStation` — orig OreSpawnWorld.java:1608-1626 (FULL)

Extracted completely:

- **Config gate:** `if (OreSpawnMain.RotatorEnable == 0) return false;`
  (orig :1609-1611). `RotatorEnable` defaults to 1
  (orig OreSpawnMain.java:6423, `config.get(mobs, "RotatorEnable", 1)`).
- **Odds gate:** `if (random.nextInt(150) != 0) return false;` → **1/150 per
  eligible chunk** (orig :1612-1614). Uses the decoration `Random` parameter.
- **Site scan:** 3 attempts (orig :1615). Each attempt: random column
  `posX = chunkX + nextInt(16)`, `posZ = chunkZ + nextInt(16)` (orig
  :1616-1617); descending Y probe `for (posY = 100; posY > 50; --posY)`
  (orig :1618) requiring **air at (posX, posY, posZ) AND CrystalGrass at
  (posX, posY-1, posZ)** (orig :1619) — i.e. it lands on the crystal-grass
  surface, Y window [51, 100] (posY > 50, so 51 is the last value tested).
- **On success:** `makeRotatorStation(world, posX, posY, posZ)`;
  `recently_placed = 50`; `return true` (orig :1620-1622).
- **FULL return contract:** `false` when config-disabled, when the 1/150
  roll fails, or when all 3 attempts find no site (orig :1610, :1613,
  :1625); `true` ONLY on actual placement (orig :1622). Unlike
  `addFairyTree` (WGEN-062), there is no pass-the-gate-return-true quirk —
  the honest contract matters because this method is FIRST in the
  short-circuit `||` chain below.

**Dispatch context** (orig OreSpawnWorld.java:187-196, dimension
`DimensionID5` = crystal dimension): runs only when `addFairyTree` returned
false (4/5 of chunks, per its quirk), after `addCrystalTermites`, and only
when `recently_placed == 0`; first in the chain
`addRotatorStation || addUrchinSpawner || addCrystalHauntedHouse ||
addRoundRotator` (orig :191), whose failure falls through to
`addCrystalBattleTower` (orig :192). Success arms the shared 50-chunk
cooldown. The port reproduces this entire dispatch, including the hoisted
per-generator `placementCooldown` (BUG-013), at
`port:world/CrystalStructures.java:97-138`.

- **Dimension:** crystal dimension (`DimensionID5`) only.
- **Biome checks:** none in the original (crystal dim is mono-biome); the
  CrystalGrass ground requirement is the effective biome filter.
- **LessLag:** not consulted anywhere in this path.

### 1b. Play time: `DungeonSpawnerBlock.func_149674_a` type 3 — orig DungeonSpawnerBlock.java:62-64

Read in full: single-builder index (`if (type == 3)` fires
`makeRotatorStation(world, clickedX, clickedY, clickedZ)` alone — NOT a
two-builder index). Context (orig :46-52): server side only, the spawner
block and the block above are set to air, then one `nextInt(50)` roll picks
the type. The builder is called at the block position with **no ground scan,
no config gate, no Y adjustment** — a floating or buried station is faithful
behavior.

---

## 2. Geometry — `makeRotatorStation` (orig GenericDungeon.java:787-810, read in full)

No loops; five fixed writes forming a 1-wide vertical column that FLOATS: with
worldgen's `cposy` = the air block directly above the grass, the column starts
4 blocks up, leaving a 4-air gap (cposy..cposy+3) beneath it.

| # | Position (rel. to cposx,cposy,cposz) | Block (orig) | Meta/flags | orig cite |
|---|---|---|---|---|
| 1 | (0, +4, 0) | `OreSpawnMain.CrystalStone` | 0, flag 2 | GD:790 |
| 2 | (0, +5, 0) | `Blocks.field_150474_ac` (mob spawner), mob "Rotator" via `func_98272_a` | 0, flag 2 | GD:791-795 |
| 3 | (0, +6, 0) | mob spawner, "Rotator" | 0, flag 2 | GD:796-800 |
| 4 | (0, +7, 0) | `OreSpawnMain.CrystalStone` | 0, flag 2 | GD:801 |
| 5 | (0, +8, 0) | `Blocks.field_150486_ae` (chest), then `func_72921_c(…, 2, 3)` → meta 2 = facing north | flag 2, then meta-set flag 3 | GD:802-803 |

Spawner/chest tile entities are fetched with the null-safe helpers
`getSpawnerTileEntity` (orig GD:86-95) / `getChestTileEntity` (orig
GD:75-84) — plain `func_147438_o` + instanceof, no other behavior.

**Footprint extents:** X 0..0, Y +4..+8, Z 0..0 (all inclusive). Max
worldgen altitude: cposy ≤ 100 → top block ≤ Y 108.

---

## 3. Loot — FIXED slot fill, NO weighted list

`makeRotatorStation` does **not** use any `WeightedRandomChestContent[]`
field and does not call `func_76293_a` — the task template's weighted-list
section is N/A (verified by reading GD:787-810 in full; the
`chestContents`/royal usage at GD:780-783 belongs to the preceding method).
The chest is filled slot-by-slot from `world.field_73012_v` (world.rand):

| Chest slot | Item (orig) | Count | Port mapping | orig cite |
|---|---|---|---|---|
| 1 | `OreSpawnMain.RotatorEgg` | 1 + nextInt(5) → 1-5 | `ModItems.ROTATOR_SPAWN_EGG` | GD:806 (item def OreSpawnMain.java:5581) |
| 2 | `OreSpawnMain.CrystalCoal` (block item) | 4 + nextInt(16) → 4-19 | `ModBlocks.CRYSTAL_COAL` | GD:807 (block def OreSpawnMain.java:1865) |
| 3 | `OreSpawnMain.CrystalCoal` | 4 + nextInt(16) → 4-19 | `ModBlocks.CRYSTAL_COAL` | GD:808 |

Slot 0 is left empty — faithful. Per the established Phase-C rule this
fixed fill stays **in code**, not JSON (already documented as the standing
approach for Rotator Station / Urchin Spawner / Haunted House at
`port:world/CrystalStructures.java:43-49`).

---

## 4. Spawner/mob mapping

| Spawner name (orig) | Original registration | Port |
|---|---|---|
| "Rotator" | `EntityRegistry.registerGlobalEntityID(Rotator.class, "Rotator", RotatorID)` orig OreSpawnMain.java:4173 (mod-entity :4177) | `ModEntities.ENTITY_ROTATOR` ("rotator", `port:ModEntities.java:254-255`) |

(The `RotatorEnable` checks in orig BiomeGenUtopianPlains.java:222/483 are
Utopia natural-spawn list entries for the mob, unrelated to this structure.)

---

## 5. Block palette

| Original | Port |
|---|---|
| `OreSpawnMain.CrystalStone` (OreSpawnMain.java:1864) | `ModBlocks.CRYSTAL_STONE` |
| `Blocks.field_150474_ac` (mob spawner) | `Blocks.SPAWNER` |
| `Blocks.field_150486_ae` (chest), meta 2 = facing north | `Blocks.CHEST.defaultBlockState()` — default FACING is north, meta reproduced exactly |

---

## 6. Suggested DungeonType entry / structure-set conversion

**N/A — decoration-phase direct builder** (pattern doc §4 regime table:
"an `OreSpawnWorld.add*` decoration call + DSB → ONE shared builder serving
both paths", FairyTree worked example). No DungeonType, no PlacementMode, no
JSON trio. Frequency is carried by the in-code 1/150 gate inside the ported
dispatch, which already reproduces the cooldown chain the C7 sqrt
equivalence only approximates — using a structure set here would be a
downgrade. **Salt 84355: unconsumed; return to the free pool.**
For the record, existing anchor logic = the original's own scan, ported
verbatim (air-above-CrystalGrass, Y descending 100→51), legal because
decoration-phase reads are permitted.

---

## 7. Remaining port work: DSB type 3 wiring (MISSING-IN-PORT)

`port:block/entity/RandomDungeonSpawnerBlockEntity.buildForType` has no
`case 3`; type 3 currently builds a generic dungeon via the interim fallback
(line 186). Fix, following the FairyTree DSB precedent
(`buildFairyTreeAt`, pattern doc §4):

1. In `CrystalStructures`, extract the five writes + chest fill from
   `tryPlaceRotatorStation` (port lines 482-496) into a private
   `buildRotatorStation(WorldGenLevel level, RandomSource random, int x,
   int y, int z)`; keep `tryPlaceRotatorStation` calling it after its scan.
2. Add public wrapper
   `buildRotatorStationAt(ServerLevel level, RandomSource random, BlockPos
   pos)` → `buildRotatorStation(level, random, pos.getX(), pos.getY(),
   pos.getZ())` — **no gate, no scan, no Y adjustment** (orig
   DungeonSpawnerBlock.java:63 validates nothing).
3. In `RandomDungeonSpawnerBlockEntity`: `private static final int
   TYPE_ROTATOR_STATION = 3; // orig DungeonSpawnerBlock.java:62-64` and
   `case TYPE_ROTATOR_STATION -> { CrystalStructures.buildRotatorStationAt(
   server, server.random, pos); yield true; }`.

`WorldGenLevel` is satisfied by `ServerLevel`, so one transcription serves
both regimes (same signature trick as the fairy trees).

---

## 8. World-block READS mid-build

- Site scan reads (air probe orig OSW:1619 air/grass) — decoration phase,
  neighbor chunks exist, reads legal (pattern doc §4). Not part of the
  build itself; the DSB path has no scan.
- `getSpawnerTileEntity`/`getChestTileEntity` after each write (GD:792,
  :797, :804) — pure self-reads of blocks just written, reproducible from
  the write set. No terrain-dependent mid-build reads. No deviation needed.

---

## 9. Reconciliation audit of the existing port (source vs `port:world/CrystalStructures.java:468-502`)

Number-by-number: 1/150 gate ✓(:470/orig 1612), 3 attempts ✓(:474/1615),
column pick `chunk + nextInt(16)` ✓(:475-476/1616-1617), Y scan 100→51
inclusive (`posY > 50`) ✓(:477/1618), air + CrystalGrass test ✓(:478-479/1619), stone +4
✓(:482/790), Rotator spawners +5/+6 ✓(:483-484/791-800), stone +7
✓(:485/801), chest +8 facing north ✓(:490-491/802-803), slot 1 egg
1+nextInt(5) ✓(:493/806), slots 2-3 coal 4+nextInt(16) ✓(:494-495/807-808),
`recently_placed=50` hoisted to caller ✓(:117-125/1621), full return
contract ✓(:470/:498/:501 vs 1610-1625). **No defects.**

Documented deltas (already-established, PARITY-note style):
- **Config gates dropped:** `RotatorEnable` (orig OSW:1609) not ported —
  port-wide decision, no `*Enable` flags anywhere in CrystalStructures;
  original default is enabled (OreSpawnMain.java:6423).
- **RNG stream merge:** original drew placement rolls from the decoration
  `Random` but chest counts from `world.field_73012_v` (GD:806-808); port
  uses the single decoration `RandomSource` for both. Distribution
  identical.
- **Grass test:** orig compares Block identity (OSW:1619); port compares
  the default BlockState (:479) — CrystalGrass has one state; equivalent.

## Surprises

- The station **floats**: writes start at +4 above the found air block, so
  worldgen leaves a 4-block air gap under the column (GD:790 vs OSW:1620).
  Faithful in the port; do not "fix" by grounding it.
- No weighted loot list at all despite living in GenericDungeon — fixed
  slot fill from world.rand (GD:806-808).
- Salt 84355 assigned by the batch is unused (no structure set for this
  mechanism).
- Worldgen path was already ported and clean; the ONLY gap is DSB type 3.
