# Cloud Shark Dungeon — Extraction Spec (1.7.10 → NeoForge 1.21.1)

Source of truth: `reference_1_7_10_source/sources/danger/orespawn/GenericDungeon.java`,
method `makeCloudSharkDungeon` (GD:2059-2091), loot field GD:47, plus
`OreSpawnWorld.addD4CloudShark` (OSW:2423-2428).
All coordinates below are relative to the **build origin** `(X0, Y0, Z0)` = the three int args of
`makeCloudSharkDungeon(World, int cposx, int cposy, int cposz)` (GD:2059).

Citation convention: `GD:NN` = GenericDungeon.java line NN, `OSW:NN` = OreSpawnWorld.java,
`OSM:NN` = OreSpawnMain.java, `DSB:NN` = DungeonSpawnerBlock.java. Port citations name the port file + line.

The method calls **no helpers** beyond the standard `FastSetBlock` (GD:187-189, flags=2),
`getSpawnerTileEntity` / `getChestTileEntity` (GD:75-95), and the shared
`WeightedRandomChestContent.func_76293_a` chest fill. It was read in full (GD:2059-2091; next method
`makeLeafMonsterDungeon` starts GD:2093). **No client-side guard** (`world.field_72995_K`) exists in
this method — unlike makeKyuubiDungeon GD:1106 — a harmless omission since both call paths are
server-side.

---

## 1. Entry points

| Caller | Signature used | Coords passed | Notes |
|---|---|---|---|
| `OreSpawnWorld` D4 decoration, **outside the 19-way dispatch** (OSW:179-181) | `addD4CloudShark(world, random, chunkX*16, chunkZ*16)` when `(i = random.nextInt(300)) == 0` | see §9 — sky anchor Y 150-159 | fires **every** D4 chunk at 1/300; NOT gated by `recently_placed`, no `D4BigSpaceCheck`, no LessLag, no biome check |
| `DungeonSpawnerBlock` **type 14** (DSB:95-97) | `OreSpawnMain.MyDungeon.makeCloudSharkDungeon(world, clickedX, clickedY, clickedZ)` | player-placed block pos, unmodified | `type = world.rand.nextInt(50)` (DSB:52); single builder in the `type == 14` block (read in full DSB:95-97) |

### `addD4CloudShark(world, random, chunkX, chunkZ)` (OSW:2423-2428) — FULL return contract

```java
public boolean addD4CloudShark(World world, Random random, int chunkX, int chunkZ) {
    int posX = 4 + chunkX + random.nextInt(8);          // OSW:2424
    int posZ = 4 + chunkZ + random.nextInt(8);          // OSW:2425
    OreSpawnMain.MyDungeon.makeCloudSharkDungeon(world, posX,
            150 + world.field_73012_v.nextInt(10), posZ); // OSW:2426
    return true;                                         // OSW:2427
}
```

- **Always returns `true`; the return value is discarded** at the call site (OSW:179-181 — the call
  is a bare statement). Nothing downstream branches on it — no suppression side effects to preserve
  (contrast addFairyTree WGEN-062).
- **Does NOT set `recently_placed`** (contrast `addD4Rainbow` OSW:2434) and is **not gated on it** —
  Cloud Shark dungeons are fully decoupled from the D4 structure cooldown economy. They can generate
  in the same chunk as a 19-way structure, and in consecutive chunks.
- **No terrain interaction whatsoever**: no ground scan, no biome check, no clearance probe. The
  build floats at Y 150-159 over whatever is below.
- RNG-source quirk: X/Z jitter draws from the passed decoration `random`, but **Y draws from
  `world.field_73012_v` (world.rand)** — two different streams in the original. In the port both
  collapse into the structure-placement RandomSource (documented mapping delta, no distribution
  change).

---

## 2. Geometry — the entire structure (7 block writes, zero loops, zero RNG in geometry)

A 7-block floating "cloud": a 2-block vertical glowstone core, 4 spawners in a cardinal plus around
the core top, a chest capping it. **No air clearing, no floor, no shell.**

| # | What | Where (relative to X0,Y0,Z0) | Block / write | Cite |
|---|---|---|---|---|
| 1 | Core top | `(X0, Y0, Z0)` | glowstone, `FastSetBlock` (flag 2) | GD:2064 |
| 2 | Core bottom | `(X0, Y0-1, Z0)` | glowstone, `FastSetBlock` (flag 2) | GD:2065 |
| 3 | Spawner east | `(X0+1, Y0, Z0)` | `mob_spawner`, `func_147465_d(..., 0, 2)`, mob `"Cloud Shark"` via `func_98272_a` | GD:2066-2070 |
| 4 | Spawner west | `(X0-1, Y0, Z0)` | same | GD:2071-2075 |
| 5 | Spawner south | `(X0, Y0, Z0+1)` | same | GD:2076-2080 |
| 6 | Spawner north | `(X0, Y0, Z0-1)` | same | GD:2081-2085 |
| 7 | Loot chest | `(X0, Y0+1, Z0)` — directly atop core; **meta 0, no facing set** (no `func_72921_c` call, unlike the Kyuubi chests) | chest, filled from `CloudSharkContentsList` with `4 + world.field_73012_v.nextInt(5)` = **4-8 weighted stacks** | GD:2086-2090 |

Every position is deterministic given the origin. The only RNG draw inside the method is the chest
fill count (GD:2089), which moves into the loot-table JSON — the port generator is **draw-free**.

---

## 3. Chest loot — FULL transcription

`CloudSharkContentsList` (GD:47). Constructor semantics:
`WeightedRandomChestContent(item, meta, minStack, maxStack, weight)`. Fill = 4-8 pulls (GD:2089),
each into a random slot (collisions overwrite — standard documented approximation; the loot pool
never collides, slightly higher average yield).

Total weight = **140** (5 × 25 + 15). 6 entries:

| # | 1.7.10 item (meta) | Modern / port mapping (cite) | min | max | weight |
|---|---|---|---|---|---|
| 1 | `Items.field_151115_aP` (0) | `minecraft:cod` (1.7.10 raw fish, damage 0 = cod) | 6 | 16 | 25 |
| 2 | `Items.field_151103_aS` (0) | `minecraft:bone` | 6 | 16 | 25 |
| 3 | `Items.field_151007_F` (0) | `minecraft:string` | 6 | 16 | 25 |
| 4 | `Items.field_151121_aF` (0) | `minecraft:paper` | 6 | 16 | 25 |
| 5 | `OreSpawnMain.MyExperienceTreeSeed` (decl OSM:1257, init OSM:1949 `"experiencetree_seed"`) | `ModItems.EXPERIENCE_TREE_SEED` "experience_tree_seed" (ModItems.java:610-611) | 1 | 2 | 15 |
| 6 | `Items.field_151078_bh` (0) | `minecraft:rotten_flesh` | 6 | 16 | 25 |

Suggested file: `RES:loot_table/chests/cloud_shark_dungeon.json`, `pools[0].rolls` uniform
`min 4, max 8`.

---

## 4. Block palette — modern mapping

| 1.7.10 field | Modern block | Used for | Cites |
|---|---|---|---|
| `Blocks.field_150426_aN` | `minecraft:glowstone` | 2-block core | GD:2064-2065 |
| `Blocks.field_150474_ac` | `minecraft:spawner` | 4 Cloud Shark spawners | GD:2066, 2071, 2076, 2081 |
| `Blocks.field_150486_ae` | `minecraft:chest` | 1 loot chest (default facing) | GD:2086 |

No air writes, no OreSpawn custom blocks.

## 5. Spawner / mob mapping

| Spawner name | 1.7.10 class (registration) | Count / placement | Port EntityType |
|---|---|---|---|
| `"Cloud Shark"` | `danger.orespawn.CloudShark` — `EntityRegistry.registerGlobalEntityID(CloudShark.class, "Cloud Shark", CloudSharkID)` (OSM:4095, mod-entity OSM:4099) | 4 spawners, cardinal plus at `(X0±1, Y0, Z0)` / `(X0, Y0, Z0±1)` (GD:2066-2085) | `ModEntities.CLOUD_SHARK` → `EntityType<CloudShark>` (ModEntities.java:60-63) |

No direct entity spawns. Port note: `CLOUD_SHARK` registers `SpawnPlacementTypes.NO_RESTRICTIONS`
(ModEntityAttributes.java:284), so the sky-level spawners function without ground beneath.

---

## 6. Total footprint (relative to origin) — fully deterministic

| Axis | Min | Max | Extent | Determined by |
|---|---|---|---|---|
| X | `X0 - 1` | `X0 + 1` | **3 blocks** | west/east spawners (GD:2071, 2066) |
| Y | `Y0 - 1` | `Y0 + 1` | **3 blocks** | core bottom (GD:2065) / chest (GD:2086) |
| Z | `Z0 - 1` | `Z0 + 1` | **3 blocks** | north/south spawners (GD:2081, 2076) |

Suggested `DungeonType` entry (asymmetric constructor, +1 margin each side):
`CLOUD_SHARK_DUNGEON(-2, 2, 2, 2, -2, 2, PlacementMode.SKY_BAND_150)`
(constructor LegacyDungeonPiece.java:224-233; placement mode is NEW — see §7).

Pattern-doc discrepancy noted: structure_conversion_pattern.md §0 claims none of the remaining D6
structures is a "≤12-block same-chunk box" — this one is exactly that (3×3×3, X/Z jitter 4..11 keeps
`X0±1` inside 3..12, never crossing a chunk border). The chunk-generator hook is nonetheless
**prohibited for new structures** (§0 table: no `/locate`, no persistence), so the
`LegacyDungeonStructure` pipeline still applies; the tiny box just makes the per-chunk replay trivial
(single intersecting chunk).

---

## 7. Placement — structure set + anchoring

- **Dimension:** OSW:132 `DimensionID4` (decl OSM:378, `BaseDimensionID + 3` OSM:1598) = the Islands
  dimension → port `orespawn:islands`, biome tag value `orespawn:island_biome` (same as
  `RES:tags/worldgen/biome/has_structure/inca_pyramid.json`).
- **Odds:** `(i = random.nextInt(300)) == 0` (OSW:179) → exactly **1/300 per chunk**, unconditioned
  (no `recently_placed` gate, no space check). C7 sqrt equivalence: √300 ≈ 17.3 →
  **spacing 17 / separation 8** (nearest-integer rounding matches the 665→26 and 1900→44
  precedents; separation = ⌊spacing/2⌋).
- **Salt: 84351** (batch 84350-84355 assigned to this task; grep of
  `RES:worldgen/structure_set/*.json` 2026-08-08 confirms 84346-84349 and 84350+ free — highest in
  use is 84345 monster_island, plus the 84330-84344 D5/D6 block).

### Anchoring — NEEDS_NEW_MODE

None of the existing modes fits (LegacyDungeonPiece.java:164-207): `SURFACE_CENTER`,
`LOWEST_SURFACE_36`, `ISLANDS_GRASS`, `END_SURFACE`, `OCEAN_SURFACE` all anchor on a terrain/water
surface. The original has **no scan at all** — quoted in full in §1: X/Z = `4 + chunk + nextInt(8)`,
Y = `150 + nextInt(10)` (OSW:2424-2426), unconditional success, floating ~143 blocks above the
Islands Y7 grass plane.

Add **`SKY_BAND_150`** to `PlacementMode` + a case in `LegacyDungeonStructure.findGenerationPoint`:
`x = chunkMinX + 4 + nextInt(8)`, `z = chunkMinZ + 4 + nextInt(8)`, `y = 150 + nextInt(10)`, always
present (never returns empty — frequency control lives entirely in the structure set). No
`getBaseHeight` call, no accept window, no LessLag skip. This is a mechanism already sanctioned by
pattern doc §1 Step 4 ("add a mode"), so the spec is NOT flagged NEEDS_DESIGN_RULING.

### JSON trio (copy inca_pyramid's and rename)

- `RES:worldgen/structure/cloud_shark_dungeon.json` — `"type": "orespawn:legacy_dungeon"`,
  `"dungeon_type": "CLOUD_SHARK_DUNGEON"`, `"biomes": "#orespawn:has_structure/cloud_shark_dungeon"`,
  `"step": "surface_structures"`, `"spawn_overrides": {}`, `"terrain_adaptation": "none"`.
- `RES:worldgen/structure_set/cloud_shark_dungeon.json` — random_spread, spacing 17, separation 8,
  **salt 84351**.
- `RES:tags/worldgen/biome/has_structure/cloud_shark_dungeon.json` — `["orespawn:island_biome"]`.

### DungeonSpawnerBlock path

Original: `type == 14` → `makeCloudSharkDungeon(world, clickedX, clickedY, clickedZ)` (DSB:95-97),
single builder, no offset, no validation — at a ground-level spawner block the "sky" structure builds
embedded at click height (faithful; the 2-block air clear above the DSB pos,
RandomDungeonSpawnerBlockEntity.java:98-99, already frees the chest/core cells). Port currently has
**no case 14** — it falls into the generic-dungeon fallback
(RandomDungeonSpawnerBlockEntity.java:186). Add `TYPE_CLOUD_SHARK_DUNGEON = 14` →
`case TYPE_CLOUD_SHARK_DUNGEON -> LegacyDungeonPiece.buildNow(server, pos, DungeonType.CLOUD_SHARK_DUNGEON)`
(pattern of the KYUUBI case, RandomDungeonSpawnerBlockEntity.java:155-159).

---

## 8. World-block READS during build

**None in the geometry.** Zero `world.func_147439_a` (getBlock) calls in GD:2059-2091. The only
reads are `getSpawnerTileEntity` / `getChestTileEntity` (GD:2067, 2072, 2077, 2082, 2087) on blocks
the method **just placed itself** — self-reads fully reproducible from the write set, subsumed by
`piece.placeSpawner` / `piece.placeLootChest`. No in-memory model needed; no deviation decision
needed. RNG contract is trivially satisfied (generator is draw-free once loot rolls move to JSON).

---

## 9. Surprises / MISSING-IN-PORT

1. **MISSING-IN-PORT — the entire structure.** No `CLOUD_SHARK_DUNGEON` DungeonType exists
   (LegacyDungeonPiece.java DungeonType list ends at MONSTER_ISLAND :161); DSB type 14 falls to the
   generic-dungeon fallback. Needs: `CloudSharkDungeonGenerator` (tiny — 7 place calls), DungeonType
   entry (§6), new `SKY_BAND_150` placement mode (§7), JSON trio + loot table, DSB case 14.
2. **The 1/300 roll sits OUTSIDE the 19-way dispatch and outside all gating** (OSW:179-181): no
   `recently_placed` check, no `recently_placed = 50` set, no `D4BigSpaceCheck`, no LessLag. It is
   the only D4 big-structure roll with this property — do not "harmonize" it into the cooldown
   economy; the structure-set conversion (spacing 17/8) is the whole frequency story.
3. **Mixed RNG sources in the original anchor** (OSW:2424-2426): X/Z from the decoration `random`,
   Y from `world.field_73012_v`. Port uses one placement RandomSource for all three — PARITY-note in
   the slice report, no distribution change.
4. **`i` is overwritten by the roll** (`(i = random.nextInt(300))`, OSW:179) reusing the 19-way
   dispatch variable — cosmetic decompiler artifact, no behavioral coupling (the dispatch has already
   completed by then).
5. **No air clearing and no facing meta on the chest** (GD:2086, meta 0) — at sky altitude both are
   moot; via DSB in solid terrain the structure generates buried except the 2 pre-cleared cells.
   Faithful — do not add clearing.
6. **Floating glowstone/spawners survive** only because of flag-2 writes (no neighbor updates) —
   port must write through `piece.place` (UPDATE_CLIENTS) per the standard helper contract.
7. **Spawners at Y150-159 need no ground**: port CloudShark registers `NO_RESTRICTIONS` spawn
   placement (ModEntityAttributes.java:284), so spawner activation in mid-air works as in 1.7.10.
8. **Pattern-doc claim corrected** (§6): this IS a ≤12-block same-chunk box, contradicting §0's
   "none of the remaining 22 are" — pipeline choice unaffected (chunk hook prohibited for new
   structures).
