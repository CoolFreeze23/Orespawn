# Stinky House — Extraction Spec (1.7.10 → NeoForge 1.21.1)

Source of truth: `reference_1_7_10_source/sources/danger/orespawn/GenericDungeon.java` —
`makeStinkyHouse` (GD:5314-5381; next method `makeRubberDuckyPond` at GD:5383, the White
House family begins at `makeWhiteHouse` GD:5423). Method read IN FULL, including the
shared helpers it uses (`FastSetBlock` GD:187-189, `getSpawnerTileEntity` GD:86-95,
`getChestTileEntity` GD:75-84). All coordinates are relative to the build origin
`(cposx, cposy, cposz)` = the three int args. Method-local constants: `height = 2`
(GD:5322), `width = 9` (GD:5323), `length = 12` (GD:5324), `yardwidth = 16` (GD:5325),
`yardlength = 24` (GD:5326).

Citation convention: `GD:NN` = GenericDungeon.java line NN, `OSW:NN` = OreSpawnWorld.java,
`OSM:NN` = OreSpawnMain.java, `DSB:NN` = DungeonSpawnerBlock.java, `RDS:NN` =
`src/main/java/danger/orespawn/block/entity/RandomDungeonSpawnerBlockEntity.java`,
`LDP:NN` = `src/main/java/danger/orespawn/world/structure/LegacyDungeonPiece.java`,
`LDS:NN` = `src/main/java/danger/orespawn/world/structure/LegacyDungeonStructure.java`.

Shared plumbing:
- `FastSetBlock(world, x, y, z, block)` (GD:187-189) → flag-2 chunk write, no neighbor
  updates → port `piece.place(x, y, z, state)` (structure_conversion_pattern.md §1 step 3
  table).
- Spawners: `world.func_147465_d(..., field_150474_ac, 0, 2)` +
  `getSpawnerTileEntity` + `func_98272_a(name)` → `piece.placeSpawner(x, y, z, type)`.
- Chest: `func_147465_d(..., field_150486_ae, 0, 2)` + `getChestTileEntity` +
  `WeightedRandomChestContent.func_76293_a` → `piece.placeLootChest(...)` + loot JSON.
- **Zero world reads in the builder** — no `func_147439_a`/`func_147437_c` anywhere in
  GD:5314-5381; the only reads are the two spawner TE fetches and one chest TE fetch
  (self-reads, absorbed by the helpers). §10.
- The builder DOES draw in-generator RNG (yard fence decay, house decay) — all draws are
  pure functions of loop indices + earlier draws, never of world state, so the stitching
  contract is satisfied with the draws left in the generator (§11).

---

## 1. Entry points — EVERY call site

Grep of the whole original tree for `makeStinkyHouse`: exactly two call sites
(OSW:2292 and DSB:171; GD:5314 is the definition; the old pre-audit copy at
`src/danger/orespawn/GenericDungeon.java:5331` mirrors the same and is not
shipped code).

| Caller | Signature used | Coords passed | Notes |
|---|---|---|---|
| `OreSpawnWorld.addD4StinkyHouse` (OSW:2292) | `OreSpawnMain.MyDungeon.makeStinkyHouse(world, posX, posY, posZ)` | scan hit, **no offset — posY is the GRASS BLOCK itself**, not the air above (§1.1) | worldgen path, Islands (Dimension4) only (§1.2) |
| `DungeonSpawnerBlock` type **39** (DSB:170-172) | `...makeStinkyHouse(world, clickedX, clickedY, clickedZ)` | player-placed block pos, no offset | roll `type = world.field_73012_v.nextInt(50)` (DSB:52), fires 400 ticks after placement (DSB:39), self + block above deleted first (DSB:50-51). The `if (type == 39)` block read IN FULL: **single builder call, nothing else** (DSB:170-172) — not a two-builder index. |

### 1.1 `addD4StinkyHouse` — FULL method + return contract (OSW:2276-2297)

```java
// OSW:2276-2297
public boolean addD4StinkyHouse(World world, Random random, int chunkX, int chunkZ) {
    if (OreSpawnMain.LessLag != 0 && random.nextInt(2) != 0) {
        return false;
    }
    int posX = chunkX + random.nextInt(8);
    int posZ = chunkZ + random.nextInt(8);
    for (int posY = 20; posY > 4; --posY) {
        Block bid = world.func_147439_a(posX, posY, posZ);
        if (bid != Blocks.field_150349_c) continue;
        for (int x = -8; x < 20; ++x) {
            for (int z = -8; z < 20; ++z) {
                bid = world.func_147439_a(posX + x, posY + 18, posZ + z);
                if (bid == Blocks.field_150350_a) continue;
                return false;
            }
        }
        OreSpawnMain.MyDungeon.makeStinkyHouse(world, posX, posY, posZ);
        recently_placed = 50;
        return true;
    }
    return false;
}
```

1. LessLag gate: `LessLag != 0 && random.nextInt(2) != 0 → return false` (OSW:2277-2279)
   — 50% skip only when the config is on, chunk-provided `random`.
2. ONE attempt (no retry loop): `posX/posZ = chunk + random.nextInt(8)` (OSW:2280-2281).
3. Column scan `posY = 20` down to `5` inclusive (`posY > 4`, OSW:2282): require the
   block AT `(posX, posY, posZ)` to be **grass block** (`field_150349_c`, OSW:2283-2284)
   — the anchor is the grass block itself, NOT the air above it.
4. On the first grass hit: 28×28 single-layer air probe at `posY + 18`, `x, z ∈ −8..19`
   (OSW:2285-2291); **any non-air → `return false` immediately** — the whole attempt
   aborts, the Y scan does not resume.
5. All clear → `makeStinkyHouse(world, posX, posY, posZ)`, `recently_placed = 50`,
   `return true` (OSW:2292-2294).
6. No grass in the window → `return false` (OSW:2296).

**FULL return contract: `true` ONLY on an actual placement** (which also sets the
50-chunk global cooldown, OSW:2293). LessLag skip, scan miss, and probe failure all
return `false`. No WGEN-062-style early-true quirk. Line-for-line the same shape as
`addD4NightmareRookery` (OSW:2253-2274, the `ISLANDS_GRASS` reference) except the probe
volume: rookery's own probe differs, but both probes collapse into structure-set
separation per the C7 approximation (LDS Javadoc, LDS:293-304).

### 1.2 Worldgen dispatch chain (complete)

- `OreSpawnWorld.generate`: `world.field_73011_w.field_76574_g == OreSpawnMain.DimensionID4`
  (OSW:132) — **Islands dimension only** in worldgen; the only other path is DSB type 39.
- Big-structure roll (OSW:134): `recently_placed == 0 && random.nextInt(100) == 0 &&
  this.D4BigSpaceCheck(world, chunkX*16, 7, chunkZ*16)`, then `i = random.nextInt(19)`
  (OSW:135); **`i == 15` → `addD4StinkyHouse` (OSW:165-167)** — single-outcome index
  (full i→structure table: enormous_castle_spec.md, reused for all Islands D6 work).
- Effective odds: 1/100 × 1/19 = **1/1900 per Islands chunk** before the LessLag gate
  and scan success.
- `D4BigSpaceCheck` (dispatch level) + the in-add 28×28 probe + `recently_placed` all
  map onto structure-set spacing/separation (pattern §1 step 4, C7-approved).

---

## 2. Geometry — per-loop tables (all ranges inclusive)

Write order: yard fence/bushes first (loop A), then the house shell (loop B, overwriting
any loop-A debris inside its footprint), then 2 spawners, then 1 chest. **Everything is
written at `cposy+1` and above — nothing is ever written at the origin level or below**
(no foundation, no floor; the terrain grass at `cposy` is the house floor).

### 2.1 Loop A — decayed yard fence + dead bushes (GD:5327-5342)

`i = 0..24` (outer, X), `k = 0..16` (inner, Z), all writes at `(cposx + i − 5, cposy + 1,
cposz + k − 4)` — i.e. rel X `−5..+19`, rel Z `−4..+12`, one layer at `y+1`. Per cell,
in source order:

1. `bid = air` (GD:5329).
2. Perimeter (`i == 0 || i == 24 || k == 0 || k == 16`) → `bid = fence`
   (`field_150422_aJ`, oak fence, GD:5330-5332).
3. `bid == fence && world.field_73012_v.nextInt(3) == 1` → `bid = air` (GD:5333-5335) —
   **1/3 of fence posts knocked out** (draw only happens on perimeter cells).
4. `bid == air && world.field_73012_v.nextInt(10) == 1` → `bid = dead bush`
   (`field_150330_I`, GD:5336-5338) — draw happens for every interior cell AND every
   knocked-out perimeter cell (a bush can stand in a fence gap).
5. `bid == air → continue` — **air is never written** (GD:5339); terrain/vegetation in
   the yard is left untouched.

| Where | Cell (rel) | Block | Cite |
|---|---|---|---|
| perimeter ring, ~2/3 of cells | `(−5..+19, +1, −4..+12)` edge | oak fence | GD:5331 |
| perimeter gaps, 1/10 of them | same ring | dead bush | GD:5337 |
| interior, 1/10 of cells | `(−4..+18, +1, −3..+11)` | dead bush | GD:5337 |
| everything else | — | **no write** (`continue`) | GD:5339 |

Yard is asymmetric around the house (house X 0..12 sits 5 in from the −X fence, 7 from
+X; house Z 0..9 sits 4 from the −Z fence, 3 from +Z).

### 2.2 Loop B — the house shell, 13×10×3 (GD:5343-5365)

`i = 0..12` (outer, X), `k = 0..9` (middle, Z), `j = 0..2` (inner, Y), write at
`(cposx + i, cposy + j + 1, cposz + k)` — rel Y `+1..+3`. Per cell, in source order
(later rules overwrite earlier ones):

| # | Rule (source order) | Result | Cite |
|---|---|---|---|
| 1 | default | air | GD:5346 |
| 2 | perimeter `i∈{0,12} or k∈{0,9}` | oak planks (`field_150344_f`) | GD:5347-5349 |
| 3 | `bid==planks && j==1 && (i==1 \|\| i==11 \|\| k==1 \|\| k==8)` | glass pane (`field_150410_aZ`) — 8 windows at wall height 1, one adjacent to each corner: `(0,1,1) (0,1,8) (12,1,1) (12,1,8) (1,1,0) (11,1,0) (1,1,9) (11,1,9)` | GD:5350-5352 |
| 4 | `j == 2` | oak planks — full 13×10 flat roof slab, overriding walls/interior | GD:5353-5355 |
| 5 | `world.field_73012_v.nextInt(10) == 1` | air — **UNCONDITIONAL draw for every one of the 390 cells**; ~10% of walls, windows, roof (and interior air, no-op) knocked out — the house is generated pre-ruined | GD:5356-5358 |
| 6 | `(j==0 \|\| j==1) && i==0 && (k==4 \|\| k==5)` (De-Morganed from GD:5359) | air — 2-wide 2-tall doorway in the `i = 0` (−X) wall, forced open AFTER the decay roll | GD:5359-5361 |
| 7 | write `bid` **unconditionally** (air included) | clears all terrain inside the 13×10×3 volume | GD:5362 |

Net: 13(X)×10(Z) plank box, walls 2 high (`j = 0..1`) with a flat plank roof at `j = 2`,
8 corner-adjacent glass-pane windows, a 2×2 doorway centered on the −X wall
(`k = 4..5`), all riddled with random 1/10 decay holes; interior `(1..11, +1..+2, 1..8)`
is air; the floor is the untouched terrain grass at `cposy`.

### 2.3 Spawners — 2, at floor level inside the house (GD:5366-5375)

Each: `func_147465_d(..., field_150474_ac, 0, 2)` then `func_98272_a(name)`. Both at
`y+1` (= house `j = 0` layer), interior cells written air by loop B:

| # | Position (rel) | Mob name string | Cite |
|---|---|---|---|
| 1 | `(2, +1, 2)` | `"Stink Bug"` | GD:5366-5370 |
| 2 | `(length−2, +1, width−2)` = `(10, +1, 7)` | `"Stinky"` | GD:5371-5375 |

### 2.4 Chest — 1, house center (GD:5376-5380)

`(length/2, +1, width/2)` = `(6, +1, 4)`: chest via `func_147465_d(...,
field_150486_ae, 0, 2)`, filled with `8 + world.field_73012_v.nextInt(5)` = **8-12
pulls** of `StinkyHouseContentsList` (GD:5379).

Net shape: a ruined 13×10 oak-plank shack with ~10% of its fabric randomly missing,
sitting directly on the Islands grass inside a gap-toothed oak-fence yard (25×17)
scattered with dead bushes — two spawners (Stink Bug + Stinky) and one loot chest on
the dirt floor.

---

## 3. Loot — FULL transcription

`StinkyHouseContentsList` (GD:28) — constructor semantics `(item, meta=0, minStack,
maxStack, weight)`. **7 entries, total weight = 215** (3×35 + 3×25 + 35). Fill count:
`8 + nextInt(5)` (GD:5379) → `pools[0].rolls` uniform **min 8, max 12**. One chest.

| # | 1.7.10 item | Modern / port mapping (cite) | min | max | weight |
|---|---|---|---|---|---|
| 1 | `OreSpawnMain.MyDeadStinkBug` | `ModItems.DEAD_STINK_BUG` "dead_stink_bug" (ModItems.java:269) | 4 | 10 | 35 |
| 2 | `OreSpawnMain.StinkyEgg` | `ModItems.STINKY_SPAWN_EGG` "stinky_spawn_egg" (ModItems.java:1088-1089; orig-name cross-proof EasterBunny.java:200) | 4 | 10 | 35 |
| 3 | `OreSpawnMain.StinkBugEgg` | `ModItems.STINK_BUG_SPAWN_EGG` "stink_bug_spawn_egg" (ModItems.java:1058-1059; cross-proof EasterBunny.java:149) | 4 | 10 | 35 |
| 4 | `Items.field_151103_aS` | `minecraft:bone` | 6 | 16 | 25 |
| 5 | `Items.field_151044_h` | `minecraft:coal` | 6 | 16 | 25 |
| 6 | `Items.field_151007_F` | `minecraft:string` | 6 | 16 | 25 |
| 7 | `Items.field_151078_bh` | `minecraft:rotten_flesh` | 3 | 10 | 35 |

→ `RES:loot_table/chests/stinky_house.json`, rolls uniform 8-12, one entry per row,
`set_count` uniform per row. Documented approximation (pattern §1 step 5): original
pulls landed in random chest slots with overwrite collisions; a loot pool never
collides.

---

## 4. Spawner / mob mapping table

| Spawner name | 1.7.10 registration | Port EntityType (cite) |
|---|---|---|
| `"Stink Bug"` (GD:5369) | `StinkBug.class` — `registerGlobalEntityID(..., "Stink Bug", StinkBugID)` OSM:3959, `registerModEntity` OSM:3963 | `ModEntities.ENTITY_STINK_BUG` "stink_bug" (ModEntities.java:396-397) |
| `"Stinky"` (GD:5374) | `Stinky.class` — `registerGlobalEntityID(..., "Stinky", StinkyID)` OSM:4269, `registerModEntity` OSM:4273 | `ModEntities.ENTITY_STINKY` "stinky" (ModEntities.java:484-485) |

**No direct entity spawns** — spawner blocks only (no yaw/NBT/persistence handling to
extract; the DamselInDistress/GirlfriendIsland-style concerns do not arise here).
Spawn-gate compatibility notes: the port `EntityStinkBug.checkSpawnRules` requires
`y >= 50` but has the near-own-spawner bypass (EntityStinkBug.java:117-123, orig
StinkBug.java:136-151) — the house spawner at Islands grass level (Y 6-21) works via
that bypass; `EntityStinky.checkSpawnRules` is daytime + ≤2 buddies within 20/10/20
(EntityStinky.java:505-511) with no spawner bypass — faithful to orig Stinky.java:286-291.

---

## 5. Block palette

| 1.7.10 field | Modern block | Used for | Cite |
|---|---|---|---|
| `Blocks.field_150350_a` | `minecraft:air` | decay holes, doorway, interior clearing (house loop only — yard never writes air) | GD:5329, 5346, 5357, 5360 |
| `Blocks.field_150422_aJ` | `minecraft:oak_fence` | yard perimeter | GD:5331 |
| `Blocks.field_150330_I` | `minecraft:dead_bush` | yard scatter (1.21 `#dead_bush_may_place_on` includes `#dirt` ⊇ grass block — survives; flag-2 write skips the check anyway and dead bush has no random-tick decay) | GD:5337 |
| `Blocks.field_150344_f` (meta 0) | `minecraft:oak_planks` | walls + roof | GD:5348, 5354 |
| `Blocks.field_150410_aZ` | `minecraft:glass_pane` | 8 windows | GD:5351 |
| `Blocks.field_150474_ac` | `minecraft:spawner` | 2 spawners | GD:5366, 5371 |
| `Blocks.field_150486_ae` | `minecraft:chest` | 1 loot chest | GD:5376 |
| (placement scan only) `Blocks.field_150349_c` | grass block | anchor test | OSW:2284 |

Fence and glass-pane connection states: place `defaultBlockState()` (unconnected) via
flag-2 `piece.place`, matching every existing generator (IncaPyramidGenerator.java:331,
MiniDungeonGenerator.java:92, LDP:1328) — the established treatment of the
1.7.10-render-time vs modern-blockstate connection difference.

---

## 6. Footprint extents + suggested DungeonType

Relative to `(cposx, cposy, cposz)`:

| Axis | Min | Max | Extent | Determined by |
|---|---|---|---|---|
| X | `−5` | `+19` | **25** | yard `i − 5`, `i = 0..24` (GD:5327, 5340) |
| Y | `+1` | `+3` | **3** | all writes at `cposy + 1` (GD:5340) / house `j + 1`, `j ≤ 2` (GD:5345, 5362) — **nothing at or below origin level** |
| Z | `−4` | `+12` | **17** | yard `k − 4`, `k = 0..16` (GD:5328, 5340) |

Suggested entry (asymmetric 6-int ctor `(xMin, xMax, down, up, zMin, zMax, mode)`, +1
margin; `down = 1` follows the no-dig convention of MINI_DUNGEON/CEPHADROME_ALTAR,
LDP:192/194):

```java
STINKY_HOUSE(-6, 20, 1, 4, -5, 13, PlacementMode.ISLANDS_GRASS),
```

## 7. Placement — **existing mode fits: `ISLANDS_GRASS`** (no new mode needed)

`addD4StinkyHouse`'s anchoring (§1.1) is line-for-line identical to
`addD4NightmareRookery`'s (OSW:2253-2274), which `ISLANDS_GRASS` ports
(`islandsGrassOrigin`, LDS:293-318): LessLag 50% skip (LDS:306-308 ← OSW:2277-2279),
`chunk + nextInt(8)` jitter (LDS:310-311 ← OSW:2280-2281), anchor AT the grass block
found by the Y 20→5 scan via `getBaseHeight − 1` with the hard `5..20` window
(LDS:312-316 ← OSW:2282-2284). The in-add 28×28 air probe at +18 (OSW:2285-2291) and
the dispatch-level `D4BigSpaceCheck` (OSW:134) collapse into structure-set separation
per the mode's Javadoc (LDS:300-303, C7 treatment). Worth appending to that Javadoc
when wiring: `addD4StinkyHouse` (OSW:2276-2297) is another exact user.

JSON trio (copy the `cephadrome_altar` trio — same dimension, same anchor — and
rename):

- `RES:worldgen/structure/stinky_house.json` — `"type": "orespawn:legacy_dungeon"`,
  `"dungeon_type": "STINKY_HOUSE"`, `"biomes": "#orespawn:has_structure/stinky_house"`,
  `"step": "surface_structures"`, `"terrain_adaptation": "none"`,
  `"spawn_overrides": {}`.
- `RES:worldgen/structure_set/stinky_house.json` — §8.
- `RES:tags/worldgen/biome/has_structure/stinky_house.json` — `["orespawn:island_biome"]`
  (matching `has_structure/cephadrome_altar.json`).

## 8. Structure-set conversion

Effective odds: 1/100 (big-structure gate, OSW:134) × 1/19 (`i == 15`, OSW:135/165) =
**1/1900 per Islands chunk** — the standard single-outcome Islands D4 arithmetic
(pattern §1 step 4: → spacing 44/22; precedents `nightmare_rookery.json` and
`cephadrome_altar.json`, both 44/22).

C7 sqrt equivalence: spacing ≈ √1900 ≈ 43.6 → **spacing 44, separation 22**.
Salt: **84365** (assigned to this task; verified free — grep of
`RES:worldgen/structure_set/*.json` shows OreSpawn salts topping out at 84361
(cephadrome_altar), plus the vanilla-style 10387399 on dim_villages).

`RES:worldgen/structure_set/stinky_house.json`: random_spread, spacing 44,
separation 22, salt 84365.

## 9. DungeonSpawnerBlock outcome

- Original: `if (type == 39)` → `makeStinkyHouse(world, clickedX, clickedY, clickedZ)`
  — one call, no offset, block read in full (DSB:170-172).
- Port: add `TYPE_STINKY_HOUSE = 39` (cite DSB:170-172) and
  `case TYPE_STINKY_HOUSE -> LegacyDungeonPiece.buildNow(server, pos,
  DungeonType.STINKY_HOUSE)` in `RandomDungeonSpawnerBlockEntity.buildForType`
  (wired types today: 0/1/2/3/7/12/13/14/15/16/17/18/19/20/21/22/23/24/27/29/30/34/
  37/38/47, RDS:44-91 + RDS:145-256; **type 39 currently falls through to the
  generic-dungeon default**).
- The DSB path bypasses the grass scan and dimension check entirely — a stinky house
  built at the clicked position in any dimension, floating or embedded (no foundation,
  no terrain clearing outside the house volume), is faithful behavior. `buildNow`
  keeps live RNG, so the decay pattern differs per placement as in 1.7.10.

## 10. Mid-build world READS — classified

1. **None.** The builder contains no `func_147439_a`/`func_147437_c` call
   (GD:5314-5381 read in full) — no foundation probe, no terrain conditionals. The
   yard loop's `continue`-on-air (GD:5339) is a write-skip decided purely by RNG/loop
   indices, not a world read.
2. **Tile-entity fetches** (GD:5367, 5372 spawners; GD:5377 chest) — self-reads of
   blocks written the line before; absorbed by `piece.placeSpawner` /
   `piece.placeLootChest`. No deviation decision needed.
3. The placement-scan reads (OSW:2283-2291) live outside the builder and map into
   `ISLANDS_GRASS` + set separation (§7).

No sanctioned-helper (`terrainStateIfInChunk`) usage needed; no NEEDS_DESIGN_RULING
condition arises.

## 11. RNG stream

All builder draws use `world.field_73012_v` (world rand). In the port:

- **Stays in the generator** (deterministic piece RandomSource, drawn identically in
  every per-chunk replay pass): the yard rolls — one `nextInt(3)` per perimeter cell
  (80 cells: 25×17 − 23×15, GD:5333) plus one `nextInt(10)` per air-at-that-point cell
  (345 interior cells + each knocked-out perimeter cell, GD:5336) — and the house decay rolls — one
  `nextInt(10)` per cell, **unconditional**, 13×10×3 = 390 draws (GD:5356). Every
  conditional draw's condition depends only on loop indices and earlier draws, never
  on world state or `inChunk`, so all passes consume the identical stream (pattern §1
  step 3 rule 1 satisfied without restructuring). Gate only the writes (the helpers do
  this internally); preserve the yard loop's air-skip as a write-skip, not a draw-skip.
- **Moves to JSON**: the chest fill count `8 + nextInt(5)` (GD:5379) → loot `rolls
  8-12`, plus all in-fill draws inside `func_76293_a`.
- PARITY note (slice report): original decay/fence-gap patterns came from shared
  live `world.rand`; the port's per-piece seed makes each house's ruin pattern
  seed-stable. The DSB `buildNow` path keeps live-RNG behavior. Dispatch-layer rolls
  (OSW:134-135 gate+picker, OSW:2277 LessLag, OSW:2280-2281 jitter, DSB:52) collapse
  into structure-set frequency / `islandsGrassOrigin` / the DSB roll as usual.

## 12. Surprises / MISSING-IN-PORT

- **S1 (MISSING-IN-PORT)**: `makeStinkyHouse` has no counterpart anywhere in
  `src/main/java/` (grep `Stink`: only the two entities, eggs, spawn blocks, items,
  renderers — no structure/DungeonType/DSB case) and no `stinky_house` JSON exists.
  Worldgen `i == 15` and DSB type 39 both fall through today.
- **S2 — pre-ruined by design**: the 1/10 decay roll (GD:5356) runs over EVERY house
  cell after walls/windows/roof are decided — holes in the roof, missing wall planks,
  shattered windows are generated content. Do not "repair", and keep the roll's
  position in the rule order (decay before the doorway force-clear, so the door is
  always fully open).
- **S3 — the yard never writes air** (GD:5339 `continue`): fence gaps expose whatever
  terrain/vegetation was already there, and yard cells the original leaves alone must
  not be cleared by the port. Contrast the house loop, which writes air
  unconditionally (GD:5362) and does clear terrain inside its 13×10×3 volume.
- **S4 — anchor is the grass block itself** (OSW:2283-2284, no +1): all writes start
  at `cposy + 1`, so the house sits directly ON the surface with the grass as its
  floor — different from the air-anchored overworld adds (leaf monster S11). Nothing
  is written at `y ≤ 0`: no foundation, no floor, `down = 1` in the ctor.
- **S5 — one attempt, aborting probe**: unlike the End/overworld adds (3-4 jitter
  attempts), `addD4StinkyHouse` draws ONE position, and a failed 28×28 air probe
  `return false`s out of the whole method rather than continuing the Y scan
  (OSW:2285-2291) — exactly the shape `islandsGrassOrigin` already models; nothing
  extra to port.
- **S6 — dead bushes can replace fence posts**: a knocked-out perimeter cell re-rolls
  the 1/10 bush chance (GD:5333-5338), so bushes appear IN the fence line as well as
  scattered across the yard. Keep the two rolls' order and conditions exactly.
- **S7 — spawn-gate interplay** (§4): Stink Bug's port `y >= 50` rule is bypassed near
  its own spawner (EntityStinkBug.java:121) — required for this structure to function
  at Islands grass level; Stinky's daytime/buddy-cap gate (EntityStinky.java:505-511)
  throttles the second spawner naturally. No change needed — noted so nobody "fixes"
  the low-altitude Stink Bug spawner as a bug.
- **S8 — window pattern is corner-adjacent, not centered**: the pane condition
  (GD:5350-5352) puts the 8 windows one cell in from each corner at wall height 1;
  the long-wall midsections have no windows. Faithful — do not recenter.
- **S9 — mixed RNG sources in the dispatch layers**: the big-structure gate + 19-way
  picker use the chunk-provided `random` (OSW:134-135), as do the LessLag gate and
  jitter (OSW:2277-2281); the builder's decay/fence/loot draws use `world.rand`
  (GD:5333/5336/5356/5379); DSB:52 uses `world.rand`. All collapse per §11.
- **S10 — fence/pane connection states**: flag-2 writes leave modern `oak_fence` /
  `glass_pane` in their unconnected default state until a neighbor update; 1.7.10
  computed connections at render time. Established treatment: place
  `defaultBlockState()` like every existing generator (§5) — document, don't deviate.
