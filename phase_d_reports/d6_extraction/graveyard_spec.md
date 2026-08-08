# Ender Reaper Graveyard — Extraction Spec (1.7.10 → NeoForge 1.21.1)

Source of truth: `reference_1_7_10_source/sources/danger/orespawn/GenericDungeon.java` —
`makeEnderReaperGraveyard` (GD:2490-2563) **plus its helper `makeAGrave` (GD:2565-2576)**,
both read in full. All coordinates are relative to the build origin `(cposx, cposy, cposz)`
= the three int args. Method-local constants: `width = 11` (GD:2494), `length = 13`
(GD:2495).

Citation convention: `GD:NN` = GenericDungeon.java line NN, `OSW:NN` = OreSpawnWorld.java,
`OSM:NN` = OreSpawnMain.java, `DSB:NN` = DungeonSpawnerBlock.java, `RDS:NN` =
`src/main/java/danger/orespawn/block/entity/RandomDungeonSpawnerBlockEntity.java`,
`LDP:NN` = `src/main/java/danger/orespawn/world/structure/LegacyDungeonPiece.java`,
`LDS:NN` = `src/main/java/danger/orespawn/world/structure/LegacyDungeonStructure.java`.

Shared plumbing:
- `FastSetBlock(world, x, y, z, block)` (GD:187-189) → flag-2 chunk write, no neighbor
  updates → port `piece.place(x, y, z, state)` (structure_conversion_pattern.md §1 step 3
  table).
- Spawners placed with `world.func_147465_d(x, y, z, Blocks.field_150474_ac, 0, 2)` then
  `getSpawnerTileEntity` (GD:86-95) + `func_98272_a(name)` → port
  `piece.placeSpawner(x, y, z, type)`. Chests via `func_147465_d(..., field_150486_ae, 0, 2)`
  then `getChestTileEntity` (GD:75-84) + `WeightedRandomChestContent.func_76293_a` → port
  `piece.placeLootChest(x, y, z, lootKey)`.
- **This builder DOES read world blocks mid-build** — one conditional foundation loop
  (GD:2500). Fully classified in §10; the port has an exact sanctioned precedent (royal
  altar dirt skirt, LDP:1740-1747). No NEEDS_DESIGN_RULING condition arises.

---

## 1. Entry points — EVERY call site

Grep of the whole original tree for `makeEnderReaperGraveyard`: exactly two call sites
(OSW:1536, DSB:108). `makeAGrave` is called ONLY from inside `makeEnderReaperGraveyard`
(GD:2555-2562) — it is a private-in-practice helper with no other caller anywhere
(grep: GD:2555-2562 + its definition GD:2565 only; the old pre-audit copy at
`src/danger/orespawn/GenericDungeon.java` mirrors the same and is not shipped code).

| Caller | Signature used | Coords passed | Notes |
|---|---|---|---|
| `OreSpawnWorld.addEndReapers` (OSW:1536) | `OreSpawnMain.MyDungeon.makeEnderReaperGraveyard(world, posX, posY, posZ)` | scan hit, **no Y offset** — posY is the AIR block directly above end stone | worldgen path, **The End only** (§1.2) |
| `DungeonSpawnerBlock` type **18** (DSB:107-109) | `...makeEnderReaperGraveyard(world, clickedX, clickedY, clickedZ)` | player-placed block pos, no offset | `type = world.rand.nextInt(50)` (DSB:52), fires 400 ticks after placement (DSB:39), self + block above deleted first (DSB:50-51). The `if (type == 18)` block read IN FULL: **single builder call, nothing else** (DSB:107-109) — not a two-builder index. |

### 1.1 `addEndReapers` — FULL method + return contract (OSW:1527-1540)

```java
// OSW:1527-1540
public void addEndReapers(World world, Random random, int chunkX, int chunkZ) {
    if (random.nextInt(25) != 0) {
        return;
    }
    for (int i = 0; i < 3; ++i) {
        int posX = chunkX + random.nextInt(16);
        int posZ = chunkZ + random.nextInt(16);
        for (int posY = 90; posY > 10; --posY) {
            if (!world.func_147437_c(posX, posY, posZ) || world.func_147439_a(posX, posY - 1, posZ) != Blocks.field_150377_bs || !this.quickSpaceCheck(world, posX, posY, posZ)) continue;
            OreSpawnMain.MyDungeon.makeEnderReaperGraveyard(world, posX, posY, posZ);
            return;
        }
    }
}
```

1. Gate: `random.nextInt(25) != 0 → return` (OSW:1528) — 1/25, chunk-provided `random`.
2. Up to 3 attempts (OSW:1531): `posX/posZ = chunk + random.nextInt(16)` (OSW:1532-1533).
3. Column scan `posY = 90` down to `11` inclusive (`posY > 10`, OSW:1534): require
   `func_147437_c` (air at pos) AND `func_147439_a(posX, posY-1, posZ) ==
   Blocks.field_150377_bs` (**end stone directly below**) AND
   `quickSpaceCheck(world, posX, posY, posZ)` (OSW:1535).
4. Hit → `makeEnderReaperGraveyard(world, posX, posY, posZ)` and `return` (OSW:1536-1537).

**FULL return contract: the method is `void`.** On gate failure or all-attempts-miss it
returns having done nothing. On success it builds and returns — **`recently_placed` is
NOT set** (contrast the overworld adds, e.g. addMonsterIsland OSW:1411): End structures
bypass the global 50-chunk cooldown entirely, exactly like `addHospital` (hospital spec
S6). There is no boolean/early-true coupling to port (no WGEN-062-style quirk).

`quickSpaceCheck` (OSW:2625-2633): 12×12 clearance probe — every block at
`(posX-2..posX+9, posY+4, posZ-2..posZ+9)` must be air, else reject. Placement scan, not
a mid-build read → maps into the PlacementMode (§7).

### 1.2 Worldgen dispatch chain (complete)

- `OreSpawnWorld.generate` switch on `dimensionId`, `case 1:` →
  `generateEnd(world, random, chunkX*16, chunkZ*16)` (OSW:219-222) — **End-exclusive** in
  worldgen; the only other path is DSB type 18.
- `generateEnd` (OSW:226-241): always `addEndAnts` first (**empty method**, OSW:1509-1510),
  then `i = world.field_73012_v.nextInt(4)` (OSW:228 — **world rand**, not the passed
  `random`): `i==0` addEndKnights, **`i==1` → addEndReapers (OSW:232-234)**, `i==2`
  addHospital, `i==3` addEnderCastle.
- No config gate on this path (no `DisableOverworldDungeons` — that gate is
  overworld-only, OSW:284) and no biome check (the whole End dimension qualifies).
- Effective odds: 1/4 × 1/25 = **1/100 per End chunk** before scan success.

---

## 2. Geometry — per-loop tables (all ranges inclusive)

`width = 11` → i spans `0..10` (perimeter i ∈ {0, 10}); `length = 13` → k spans `0..12`
(perimeter k ∈ {0, 12}). Write order: foundation → floor → fence cage → 4 spawners →
8 graves (graves overwrite floor cells — §2.5).

### 2.1 Loop A — conditional foundation skirt, 4 deep (GD:2497-2504)

For `j = 1..4`, `i = 0..10`, `k = 0..12`: **read** `world.func_147439_a(cposx+i, cposy−j,
cposz+k)`; if it is NOT air, skip; else `FastSetBlock(..., Blocks.field_150377_bs)`
(end stone).

| Cell (rel) | Condition | Block | Cite |
|---|---|---|---|
| `(0..10, −1..−4, 0..12)` | only where the pre-build world has air | end stone | GD:2497-2503 |

Fills the gap between the raised floor pad and uneven End terrain, at most 4 blocks down;
existing terrain is never replaced. Read classification in §10 (pre-build terrain,
read-at-write-cell — royal-altar-skirt precedent).

### 2.2 Loop B — floor pad (GD:2505-2511)

`j = 0` (GD:2505), `blk = Blocks.field_150377_bs` (GD:2506): all `(0..10, 0, 0..12)` =
end stone (GD:2507-2511). Unconditional — 11×13 solid pad at origin level.

### 2.3 Loop C — iron-bars fence cage, open top (GD:2512-2522)

For `j = 1..4`, `i = 0..10`, `k = 0..12`: `blk = air` (GD:2515); if
`i == 0 || k == 0 || i == width−1 || k == length−1` → `blk = Blocks.field_150411_aY`
(iron bars, GD:2516-2518); write `blk` (GD:2519).

| Where | Cell (rel) | Block | Cite |
|---|---|---|---|
| perimeter `i∈{0,10} or k∈{0,12}` | `(i, 1..4, k)` | iron bars | GD:2516-2518 |
| interior `(1..9, 1..4, 1..11)` | | air | GD:2515 |

**No roof** — nothing is ever written at `j ≥ 5`. The graveyard is a 4-high open-top
iron-bar pen (S3).

### 2.4 Spawners — 4 × "Ender Reaper" at the inner corners (GD:2523-2554)

Each: `func_147465_d(..., field_150474_ac, 0, 2)` then `func_98272_a("Ender Reaper")`.
All at `j = 1`, one block inside each fence corner (`width−2 = 9`, `length−2 = 11`):

| # | Position (rel) | Mob name string | Cite |
|---|---|---|---|
| 1 | `(1, 1, 1)` | `"Ender Reaper"` | GD:2523-2530 |
| 2 | `(9, 1, 11)` | `"Ender Reaper"` | GD:2531-2538 |
| 3 | `(1, 1, 11)` | `"Ender Reaper"` | GD:2539-2546 |
| 4 | `(9, 1, 1)` | `"Ender Reaper"` | GD:2547-2554 |

All four cells were loop-C interior air — no overwrite of fence bars.

### 2.5 The 8 graves — `makeAGrave` helper, FULL extraction (GD:2555-2562 → GD:2565-2576)

```java
// GD:2565-2576
public void makeAGrave(World world, int cposx, int cposy, int cposz, int xoff, int zoff) {
    TileEntityChest chest = null;
    WeightedRandomChestContent[] chestContents = null;
    chestContents = this.GraveContentsList;
    this.FastSetBlock(world, cposx + xoff, cposy + 1, cposz + zoff - 1, Blocks.field_150343_Z);
    this.FastSetBlock(world, cposx + xoff, cposy, cposz + zoff + 1, Blocks.field_150343_Z);
    world.func_147465_d(cposx + xoff, cposy, cposz + zoff, (Block)Blocks.field_150486_ae, 0, 2);
    chest = this.getChestTileEntity(world, cposx + xoff, cposy, cposz + zoff);
    if (chest != null) {
        WeightedRandomChestContent.func_76293_a((Random)world.field_73012_v, (WeightedRandomChestContent[])chestContents, (IInventory)chest, (int)(3 + world.field_73012_v.nextInt(3)));
    }
}
```

Per grave at `(xoff, zoff)`:

| Part | Cell (rel) | Block | Overwrites | Cite |
|---|---|---|---|---|
| Headstone | `(xoff, +1, zoff−1)` | obsidian | loop-C interior air | GD:2569 |
| Foot marker | `(xoff, 0, zoff+1)` | obsidian | **loop-B floor end stone** | GD:2570 |
| Chest | `(xoff, 0, zoff)` | chest | **loop-B floor end stone** | GD:2571 |
| Fill | 3 + `world.rand.nextInt(3)` = **3-5 pulls** of `GraveContentsList` | | | GD:2574 |

The headstone stands ONE block up (`+1`) on the −z side; the foot marker and chest are
sunk flush INTO the floor at `y+0` (S4). Every grave faces the same way (headstone on the
−z side). The 8 call sites (GD:2555-2562), in order:

| # | `(xoff, zoff)` | Headstone | Chest | Foot |
|---|---|---|---|---|
| 1 | `(1, 6)` | `(1, +1, 5)` | `(1, 0, 6)` | `(1, 0, 7)` |
| 2 | `(3, 4)` | `(3, +1, 3)` | `(3, 0, 4)` | `(3, 0, 5)` |
| 3 | `(5, 4)` | `(5, +1, 3)` | `(5, 0, 4)` | `(5, 0, 5)` |
| 4 | `(7, 4)` | `(7, +1, 3)` | `(7, 0, 4)` | `(7, 0, 5)` |
| 5 | `(3, 8)` | `(3, +1, 7)` | `(3, 0, 8)` | `(3, 0, 9)` |
| 6 | `(5, 8)` | `(5, +1, 7)` | `(5, 0, 8)` | `(5, 0, 9)` |
| 7 | `(7, 8)` | `(7, +1, 7)` | `(7, 0, 8)` | `(7, 0, 9)` |
| 8 | `(9, 6)` | `(9, +1, 5)` | `(9, 0, 6)` | `(9, 0, 7)` |

Layout: two rows of three (z = 4 and z = 8, x ∈ {3, 5, 7}) plus two singles at the
mid-length edges `(1, 6)` and `(9, 6)`. No grave cell collides with any other grave or
with a spawner (spawners sit at k ∈ {1, 11}; grave writes span k 3..9). All grave cells
are strictly interior (x 1..9, z 3..9).

Net shape: an 11×13 end-stone pad raised one block above the End surface on a
conditional 4-deep end-stone skirt, fenced by a 4-high roofless iron-bar wall, with four
Ender Reaper spawners in the corners and 8 obsidian-marked graves, each hiding a loot
chest sunk flush in the floor.

---

## 3. Loot — FULL transcription

`GraveContentsList` (GD:43) — constructor semantics `(item, meta=0, minStack, maxStack,
weight)`. **4 entries, total weight = 140** (4 × 35). Fill count per chest:
`3 + world.rand.nextInt(3)` (GD:2574) → `pools[0].rolls` uniform **min 3, max 5**.
Eight chests share the ONE list → **one** loot table JSON, referenced 8 times.

| # | 1.7.10 item | Modern / port mapping | min | max | weight |
|---|---|---|---|---|---|
| 1 | `Items.field_151061_bv` | `minecraft:ender_eye` | 6 | 16 | 35 |
| 2 | `Item.func_150898_a(Blocks.field_150328_O)` (red flower, meta 0) | `minecraft:poppy` | 6 | 16 | 35 |
| 3 | `Item.func_150898_a(Blocks.field_150327_N)` (yellow flower) | `minecraft:dandelion` | 6 | 16 | 35 |
| 4 | `Items.field_151079_bi` | `minecraft:ender_pearl` | 6 | 16 | 35 |

→ `RES:loot_table/chests/ender_reaper_grave.json`, rolls uniform 3-5, one entry per row,
`set_count` uniform 6-16 on each. All four items are vanilla — no port-item lookups
needed. Documented approximation (pattern §1 step 5): original pulls landed in random
chest slots with overwrite collisions; a loot pool never collides.

---

## 4. Spawner / mob mapping table

| Spawner name | 1.7.10 class (registration) | Port EntityType (cite) |
|---|---|---|
| `"Ender Reaper"` (×4, GD:2529/2537/2545/2553) | `EnderReaper` — `registerGlobalEntityID(EnderReaper.class, "Ender Reaper", EnderReaperID)` OSM:4133, `registerModEntity` OSM:4137 | `ModEntities.ENDER_REAPER` "ender_reaper" (ModEntities.java:89-91) |

No direct entity spawns — spawner blocks only. (Same mob string the Hospital's dome
spawners use — mapping already exercised by hospital_monster_island_spec.md §D.)

---

## 5. Block palette

| 1.7.10 field | Modern block | Used for | Cite |
|---|---|---|---|
| `Blocks.field_150377_bs` | `minecraft:end_stone` | foundation skirt + floor pad | GD:2501, 2506-2509 |
| `Blocks.field_150411_aY` | `minecraft:iron_bars` | 4-high perimeter fence | GD:2517 |
| `Blocks.field_150350_a` | `minecraft:air` | cage interior | GD:2515 |
| `Blocks.field_150343_Z` | `minecraft:obsidian` | grave headstones + foot markers | GD:2569-2570 |
| `Blocks.field_150474_ac` | `minecraft:spawner` | 4 spawners | GD:2526/2534/2542/2550 |
| `Blocks.field_150486_ae` | `minecraft:chest` | 8 grave chests | GD:2571 |

All vanilla; flag-2 writes throughout (iron bars keep their unconnected shape until a
neighbor update, same as 1.7.10 — `piece.place` preserves this).

---

## 6. Footprint extents + suggested DungeonType

Relative to `(cposx, cposy, cposz)`:

| Axis | Min | Max | Extent | Determined by |
|---|---|---|---|---|
| X | `0` | `+10` | **11** | `width = 11` loops (GD:2494, 2498) |
| Y | `−4` | `+4` | **9** | foundation `j = 1..4` below (GD:2497) / fence top `j = 4` (GD:2512); headstones only reach `+1` |
| Z | `0` | `+12` | **13** | `length = 13` loops (GD:2495, 2499) |

Suggested entry (asymmetric 6-int ctor `(xMin, xMax, down, up, zMin, zMax, mode)`, +1
margin):

```java
ENDER_REAPER_GRAVEYARD(-1, 11, 5, 5, -1, 13, PlacementMode.END_SURFACE),
```

## 7. Placement — `END_SURFACE`, exact fit, no new mode

`addEndReapers`'s scan (§1.1) is **line-for-line identical** to `addHospital`'s
(OSW:1527-1540 vs OSW:1542-1555): 1/25 gate, 3 attempts of `chunk + nextInt(16)` jitter,
Y 90→11 downward scan for air directly on end stone, `quickSpaceCheck` 12×12 air probe at
+4, no Y offset, no `recently_placed`. The port's `endSurfaceOrigin`
(LDS:152-185) was written for exactly this scan pair (its Javadoc names
addEnderCastle/addHospital, LDS:153-167; the graveyard is the third user — worth adding
to that Javadoc when wiring). `quickSpaceCheck` maps to the existing
`footprintClearAbove` approximation (LDS:181), documented there.

JSON pair (copy the `hospital` trio — same dimension, same anchor; hospital has no
separate biome tag file, the tag is inline):

- `RES:worldgen/structure/ender_reaper_graveyard.json` — `"type":
  "orespawn:legacy_dungeon"`, `"dungeon_type": "ENDER_REAPER_GRAVEYARD"`,
  `"biomes": "#minecraft:is_end"`, `"step": "surface_structures"`,
  `"terrain_adaptation": "none"`, `"spawn_overrides": {}`.
- No `has_structure` tag file needed (vanilla tag inline, matching `hospital.json`).

## 8. Structure-set conversion

Effective odds: 1/4 (generateEnd picker, OSW:228) × 1/25 (gate, OSW:1528) = **1/100 per
End chunk** before scan success — identical arithmetic to the Hospital
(hospital spec §A8), which shipped as spacing 10 / separation 5
(`RES:worldgen/structure_set/hospital.json`).

C7 sqrt equivalence: spacing ≈ √100 = 10 → **spacing 10, separation 5**.
Salt: **84357** (assigned to this task; verified free — grep of
`RES:worldgen/structure_set/*.json` shows in-use salts topping out at 84354, plus the
known 84312 collision and one vanilla-style 10387399).

`RES:worldgen/structure_set/ender_reaper_graveyard.json`: random_spread, spacing 10,
separation 5, salt 84357.

## 9. DungeonSpawnerBlock outcome

- Original: `if (type == 18)` → `makeEnderReaperGraveyard(world, clickedX, clickedY,
  clickedZ)` — one call, no offset, block read in full (DSB:107-109).
- Port: add `TYPE_ENDER_REAPER_GRAVEYARD = 18` (cite DSB:107-109) and
  `case TYPE_ENDER_REAPER_GRAVEYARD -> LegacyDungeonPiece.buildNow(server, pos,
  DungeonType.ENDER_REAPER_GRAVEYARD)` in `RandomDungeonSpawnerBlockEntity.buildForType`
  (wired types today: 0/1/2/3/7/12/14/17/19/21/22/23/24/27/29/30/37/38/47, RDS:44-79 +
  RDS:133-218; **type 18 currently falls through to the generic-dungeon default**,
  RDS:219).
- The DSB path bypasses the End scan entirely — a graveyard built in the overworld at
  the clicked position is faithful behavior. The foundation loop still runs there (live
  `ServerLevel`, reads legal, `buildNow`'s unclipped window) and fills up to 4 blocks of
  air below with end stone wherever the player's terrain has gaps.

## 10. Mid-build world READS — classified

1. **Foundation loop (GD:2500) — PRE-BUILD TERRAIN read, read-at-write-cell.** The very
   first loop reads `func_147439_a` at each cell `(i, −1..−4, k)` and writes end stone
   into that SAME cell only if it read air. Each cell is read exactly once, BEFORE any
   write ever touches it (loop A is first, and nothing else writes below `j = 0`), so no
   read ever observes the structure's own output — this is purely a terrain probe, NOT a
   self-read, and an in-memory model (BasiliskMaze-style) is impossible by construction.
   **Port mechanism — the sanctioned royal-altar-skirt precedent (LDP:1740-1747)**: gate
   with `inChunk(x, y, z)` FIRST, then `pLevel.getBlockState(...)`, then `place(...)`.
   Chunk-stitching safe because the read cell == the write cell: reads outside the
   current chunk never happen (inChunk short-circuits), and the write such a read would
   have gated is skipped by the same test, so every pass stays consistent regardless of
   neighbor-chunk state. Condition is `isAir()` ONLY (the original tests `!= air` alone
   — do not copy the royal skirt's grass/water extras).
2. **Tile-entity fetches** (GD:2527/2535/2543/2551 spawners, GD:2572 chest via
   `func_147438_o`) — SELF-reads of blocks written the line before; absorbed by
   `piece.placeSpawner` / `piece.placeLootChest`. No deviation decision needed.
3. No other `func_147439_a`/`func_147437_c` call exists in GD:2490-2576.

## 11. RNG stream

The only draws in the builder + helper are the **8 chest fill counts**
(`3 + world.field_73012_v.nextInt(3)`, GD:2574, once per `makeAGrave` call), all of which
move into the loot JSON's `rolls 3-5` (pattern §1 step 3 rule 3 / step 5). The ported
generator therefore consumes **zero** random draws — geometry, spawner positions, mob
name, and grave layout are all constants; the foundation conditionals branch on terrain,
not RNG, and are pass-safe per §10. The dispatch-layer rolls (OSW:228 4-way picker on
`world.rand`, OSW:1528 gate on the chunk `random`, DSB:52 on `world.rand`) live outside
the builder and collapse into structure-set frequency / the DSB roll as usual.

## 12. Surprises / MISSING-IN-PORT

- **S1 (MISSING-IN-PORT)**: `makeEnderReaperGraveyard`/`makeAGrave` exist nowhere in
  `src/main/java/` (grep `EnderReaperGraveyard|GRAVEYARD`: zero matches). No DungeonType,
  no generator, no JSON pair, no loot table; DSB type 18 falls through to the
  generic-dungeon default (RDS:219).
- **S2**: The foundation skirt is CONDITIONAL on pre-build terrain air (GD:2500) — the
  one mid-build world read. Port it via the royal-altar dirt-skirt read-at-write-cell
  pattern (§10), not an in-memory model and not an unconditional fill (an unconditional
  fill would replace up to 4 layers of existing End terrain the original preserves).
- **S3**: **No roof.** The iron-bar cage is 4 high and open-topped (loop C stops at
  `j = 4`, nothing at `j ≥ 5`) — Ender Reapers spawned inside are only fenced, not
  enclosed. Faithful; do not add a lid.
- **S4**: Grave anatomy is asymmetric: headstone obsidian at `y+1` on the −z side, foot
  obsidian and the chest SUNK FLUSH into the floor at `y+0` (GD:2569-2571), both
  replacing loop-B end stone. The chest lid sits level with the floor under open air —
  openable. Preserve the write order (floor first, graves last).
- **S5**: `addEndReapers` does NOT set `recently_placed` (OSW:1536-1537) — End
  structures bypass the global cooldown, same as addHospital/addEnderCastle (hospital
  spec S6). Its `void` return contract carries no coupling.
- **S6**: The origin Y is the air block ABOVE end stone with no offset (OSW:1535-1536),
  so the floor pad sits one block above the terrain surface; the skirt (§2.1) closes the
  resulting gap on uneven ground. Same raised-platform quirk as Spit Bug Lair S6.
- **S7**: All 8 graves share one loot list (`GraveContentsList`, GD:43) of exactly four
  vanilla items — eye of ender, poppy, dandelion, ender pearl (flowers for the graves,
  ender drops for the theme). Do not invent per-grave variation.
- **S8**: Mixed RNG sources in the dispatch layers (same shape as hospital spec S9):
  `generateEnd`'s 4-way picker uses `world.rand` (OSW:228) while the `addEndReapers`
  gate uses the chunk-provided `random` (OSW:1528); the builder's loot counts use
  `world.rand` (GD:2574). All collapse into structure-set frequency / loot rolls.
- **S9**: `addEndAnts` (OSW:1509-1510), called unconditionally before the End picker, is
  an empty method — no behavior to port from it.
