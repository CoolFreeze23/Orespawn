# Mini Dungeon — Extraction Spec (1.7.10 → NeoForge 1.21.1)

Source of truth: `reference_1_7_10_source/sources/danger/orespawn/GenericDungeon.java` —
`makeMiniDungeon` (GD:2229-2406, next method `makeGoldFishBowl` at GD:2408). All coordinates
are relative to the build origin `(cposx, cposy, cposz)` = the three int args.

Citation convention: `GD:NN` = GenericDungeon.java line NN, `OSW:NN` = OreSpawnWorld.java,
`OSM:NN` = OreSpawnMain.java, `DSB:NN` = DungeonSpawnerBlock.java. Port citations name
file + line.

Shared plumbing:
- `FastSetBlock(world, x, y, z, block)` (GD:187-189) → `OreSpawnMain.setBlockFast(..., meta 0,
  flag 2)` — flag-2 chunk write, no neighbor updates → port `piece.place(x, y, z, state)`
  (structure_conversion_pattern.md §1 step 3 table).
- Spawners placed with `world.func_147465_d(x, y, z, Blocks.field_150474_ac, 0, 2)` then
  `getSpawnerTileEntity` (GD:86-95, a `func_147438_o` getTileEntity fetch) +
  `func_98272_a(name)` → port `piece.placeSpawner(x, y, z, type)`.
- Chest via `func_147465_d(..., field_150486_ae, 0, 2)` + `getChestTileEntity` (GD:75-84)
  → port `piece.placeLootChest(x, y, z, lootKey)`.

## 1. Entry points — EVERY call site

Grep of the whole original tree for `makeMiniDungeon`: exactly two call sites (DSB:102 and
OSW:2400, the latter inside `addD4Mini`); `addD4Mini` itself has exactly one caller, the
Islands D4 dispatch (OSW:151).

| Caller | Signature used | Coords passed | Notes |
|---|---|---|---|
| `OreSpawnWorld.addD4Mini` (OSW:2400) | `OreSpawnMain.MyDungeon.makeMiniDungeon(world, posX, posY, posZ)` | scan hit, **no Y offset** — posY is the GRASS block itself, so the j=0 cobblestone floor replaces the grass | Islands dimension (D4) only, i==10 in the D4 roll (§1.2) |
| `DungeonSpawnerBlock` type **16** (DSB:101-103) | `...makeMiniDungeon(world, clickedX, clickedY, clickedZ)` | player-placed block pos, no offset | roll `world.rand.nextInt(50)` (DSB:52), fires 400 ticks after placement (DSB:39), self+above deleted first (DSB:50-51). The `if (type == 16)` block read IN FULL: **single builder call, nothing else** (DSB:101-103) — not a two-builder index. |

### 1.1 `addD4Mini` — FULL method + return contract (OSW:2391-2405)

```java
public boolean addD4Mini(World world, Random random, int chunkX, int chunkZ) {
    if (OreSpawnMain.LessLag != 0 && random.nextInt(2) != 0) {
        return false;
    }
    int posX = chunkX + random.nextInt(8);
    int posZ = chunkZ + random.nextInt(8);
    for (int posY = 20; posY > 4; --posY) {
        Block bid = world.func_147439_a(posX, posY, posZ);
        if (bid != Blocks.field_150349_c) continue;
        OreSpawnMain.MyDungeon.makeMiniDungeon(world, posX, posY, posZ);
        recently_placed = 50;
        return true;
    }
    return false;
}
```

1. LessLag gate: when the config is on, `random.nextInt(2) != 0 → return false` — 50% skip
   (the draw itself only happens when LessLag != 0, OSW:2392).
2. ONE attempt (no 4-try loop): `posX = chunkX + random.nextInt(8)`,
   `posZ = chunkZ + random.nextInt(8)` (OSW:2395-2396).
3. Column scan `posY = 20` down to `5` inclusive (`posY > 4`, OSW:2397): first block equal to
   `Blocks.field_150349_c` (grass block) wins (OSW:2398-2399).
4. Hit → `makeMiniDungeon(world, posX, posY, posZ)` — **anchor AT the grass block, no
   offset** — then `recently_placed = 50`, `return true` (OSW:2400-2402).
5. Scan miss → `return false` (OSW:2404).

**Return contract: `true` ONLY on an actual placement** (which sets the 50-chunk global
cooldown). No addFairyTree-style early-true quirk (WGEN-062): the LessLag skip and the scan
miss both return `false`. (The caller ignores the return value anyway — the D4 dispatch's
`if (i == 10)` arm is fire-and-forget, OSW:150-152.)

### 1.2 Worldgen dispatch chain (complete)

The Islands "D4" roll (OSW:132-178): gated by
`world.field_73011_w.field_76574_g == OreSpawnMain.DimensionID4` (OSW:132) AND
`recently_placed == 0 && random.nextInt(100) == 0 &&
this.D4BigSpaceCheck(world, chunkX*16, 7, chunkZ*16)` (OSW:134), then `i = random.nextInt(19)`
(OSW:135); **`i == 10` → `addD4Mini` (OSW:150-152)** — a single-outcome index, so effective
odds = 1/100 × 1/19 = **1/1900 per Islands chunk** before the LessLag gate and grass scan
(the full i→structure table is in `d5_extraction/enormous_castle_spec.md` §12.3 — row
`| 10 | addD4Mini | OSW:150-152 |` — reused per pattern §1 step 4).
The `D4BigSpaceCheck` air probe, the `recently_placed` 50-chunk cooldown, and the ≤50%
LessLag skip map onto structure-set separation / the port's `ISLANDS_GRASS` gate per the
C7-approved approximation.

## 2. Geometry — per-loop tables (all ranges inclusive)

Write order: cage box → j=7 grass ring → j=8 grass ring → staircase → j=9 spawner ring →
4 corner pillars + top spawners → 6 floor spawners → chest. Later writes overwrite earlier
ones only where noted (§2.9). Dead locals: none (the TE locals are plumbing).

### 2.1 Loop 1 — 10×10×7 iron-bar cage (GD:2238-2266)

`i = 0..9`, `k = 0..9`, `j = 0..6`; block chosen by LAST matching rule (plain sequential
assignments, GD:2241-2262):

| Rule order | Condition | Block |
|---|---|---|
| 1 (default) | — | air |
| 2 | perimeter: `i==0 || k==0 || i==9 || k==9` | iron bars |
| 3-6 | the four corners `(0,0),(9,9),(0,9),(9,0)` | cobblestone |
| 7 | `j == 0` (entire 10×10 floor) | cobblestone |
| 8 | `j == 6` AND perimeter | cobblestone |

Net: 10×10 cobblestone floor at `j=0`; iron-bar walls `j=1..5` on the perimeter with solid
cobblestone corner columns; a cobblestone rim ring at `j=6` (interior `j=1..6` carved to air,
8×8). Placement is `FastSetBlock` at `(cposx+i, cposy+j, cposz+k)` (GD:2263).

### 2.2 Loop 2 — grass ring at j=7 (GD:2267-2276)

`i = 1..8`, `k = 1..8`, `j = 7`: ring cells (`i==1 || i==8 || k==1 || k==8`) = grass block,
interior (`2..7` both axes) = air (GD:2270-2274).

### 2.3 Loop 3 — grass ring at j=8 (GD:2277-2286)

`i = 2..7`, `k = 2..7`, `j = 8`: ring cells (`i==2 || i==7 || k==2 || k==7`) = grass block,
interior (`3..6` both axes) = air (GD:2280-2284). Together with loop 2 this forms a stepped
grass "roof pyramid" inset 1 then 2 blocks from the cage rim.

### 2.4 Staircase west of the cage (GD:2287-2301)

Init `i = -6, j = 1, k = 3` (GD:2287-2289); 6 iterations `m = 0..5`, each ending `++i; ++j`
(GD:2299-2300) → steps at `(x, y) = (-6,1), (-5,2), (-4,3), (-3,4), (-2,5), (-1,6)`.
Per step (z spans `k..k+3` = `3..6`):

| Cell (rel) | Block | Cite |
|---|---|---|
| `(x, y, 3)`, `(x, y, 4)`, `(x, y, 5)`, `(x, y, 6)` | oak planks (meta 0) | GD:2291-2294 |
| `(x, y+1, 3)` and `(x, y+1, 6)` | oak fence (railings) | GD:2295-2296 |
| `(x, y+2, 3)` and `(x, y+2, 6)` | torch | GD:2297-2298 |

A 4-wide ascending stair from `(-6, +1)` up to `(-1, +6)`, level with the j=6 cage rim —
it walks over the wall onto the j=7 grass ring. Torches sit on fence posts via flag-2
writes (no neighbor updates); fences/torches top out at y `+7`/`+8` on the last step.

### 2.5 Loop 4 — Butterfly spawner ring at j=9 (GD:2302-2312)

`i = 3..6`, `k = 3..6`, `j = 9`; `if (i != 3 && i != 6 && k != 3 && k != 6) continue;`
(GD:2306) → the 12 ring cells of the 4×4 (interior 2×2 skipped) each get a mob spawner set
to `"Butterfly"` (GD:2307-2310). These float at j=9, one above the j=8 grass ring's plane,
over the open roof interior. (The local `blk = air` at GD:2305 is assigned but never
written — dead value.)

### 2.6 Corner pillars + top spawners (GD:2313-2352)

Four corner columns, each: cobblestone at `j = 7..10` (loop `j=7; j<11`, GD:2315-2317 etc.),
then — with `j` now 11 after the loop — a spawner at `(i, 11, k)`:

| Corner (i, k) | Pillar j=7..10 | Spawner at j=11 | Cite |
|---|---|---|---|
| (0, 0) | cobblestone ×4 | `"Terrible Terror"` | GD:2313-2322 |
| (9, 9) | cobblestone ×4 | `"Butterfly"` | GD:2323-2332 |
| (0, 9) | cobblestone ×4 | `"Terrible Terror"` | GD:2333-2342 |
| (9, 0) | cobblestone ×4 | `"Butterfly"` | GD:2343-2352 |

The pillars extend the loop-1 corner columns (j=0..6) up to j=10, spawner capping at j=11.

### 2.7 Interior floor spawners at j=1 (GD:2353-2400)

| # | Position (rel) | Mob name string | Cite |
|---|---|---|---|
| 1 | `(1, 1, 1)` | `"Terrible Terror"` | GD:2353-2360 |
| 2 | `(8, 1, 8)` | `"Terrible Terror"` | GD:2361-2368 |
| 3 | `(8, 1, 1)` | `"Butterfly"` | GD:2369-2376 |
| 4 | `(1, 1, 8)` | `"Butterfly"` | GD:2377-2384 |
| 5 | `(4, 1, 4)` | `"Lurking Terror"` | GD:2385-2392 |
| 6 | `(5, 1, 5)` | `"Lurking Terror"` | GD:2393-2400 |

All six overwrite loop-1 interior air cells.

### 2.8 Chest (GD:2401-2405)

One chest at `(3, +1, 3)` (`func_147465_d(..., field_150486_ae, 0, 2)`), filled with
`WeightedRandomChestContent.func_76293_a(world.field_73012_v, MiniContentsList, chest,
4 + world.field_73012_v.nextInt(5))` → **4-8 weighted pulls** into random slots (collisions
overwrite — documented approximation, pattern §1 step 5). List selected at GD:2237.

### 2.9 Overwrites (behavioral — preserve write order or place later items last)

- The 6 floor spawners and the chest overwrite loop-1 interior air at j=1 (§2.7, §2.8).
- The corner pillars (§2.6) write j=7..11 at the corners — cells no earlier loop touches
  (loops 2/3 span i,k 1..8 / 2..7 only).
- The j=9 Butterfly ring and everything above j=8 land in never-written air. No
  cross-loop block erasures exist in this builder (contrast Spit Bug Lair S3).

Net shape: a 10×10 iron-bar cage with cobblestone floor, corner columns and j=6 rim; a
2-step grass roof pyramid (rings at j=7 and j=8); 12 Butterfly spawners floating in a ring
at j=9; four cobblestone corner towers to j=10 capped by spawners at j=11; a 4-wide oak
staircase with fence railings and torches climbing the west side to the roof; 6 spawners
and 1 loot chest on the floor inside.

## 3. Loot — FULL transcription

`MiniContentsList` (GD:45) — constructor semantics `(item, meta=0, minStack, maxStack,
weight)`. 6 entries, **total weight = 190** (4×35 + 2×25). `pools[0].rolls`: uniform
**min 4, max 8** (from `4 + nextInt(5)`, GD:2404).

| # | 1.7.10 item | Modern / port mapping (cite) | min | max | weight |
|---|---|---|---|---|---|
| 1 | `Items.field_151153_ao` (golden apple, meta 0) | `minecraft:golden_apple` | 6 | 16 | 35 |
| 2 | `OreSpawnMain.MyCrystalApple` ("crystalapple", OSM:1900) | port `ModItems.CRYSTAL_APPLE` "crystal_apple" (ModItems.java:561) | 6 | 16 | 35 |
| 3 | `OreSpawnMain.MyBacon` ("cookedbacon", OSM:1850) | `ModItems.COOKED_BACON` "cooked_bacon" (ModItems.java:531) | 6 | 16 | 35 |
| 4 | `OreSpawnMain.MyFireFish` ("firefish", OSM:1709) | `ModItems.FIRE_FISH` "fire_fish" (ModItems.java:486) | 6 | 16 | 35 |
| 5 | `OreSpawnMain.InstantGarden` ("instantgarden", OSM:1937) | `ModItems.INSTANT_GARDEN` "instant_garden" (ModItems.java:648) | 2 | 4 | 25 |
| 6 | `OreSpawnMain.InstantShelter` ("instantshelter", OSM:1936) | `ModItems.INSTANT_SHELTER` "instant_shelter" (ModItems.java:646) | 2 | 4 | 25 |

→ `RES:loot_table/chests/mini_dungeon.json`, rolls uniform 4-8, one entry per row above.
All-food-and-utility list — no weapons/armor; do not pad it.

## 4. Spawner / mob mapping table

| Spawner name | Count (positions) | 1.7.10 registration | Port EntityType (cite) |
|---|---|---|---|
| `"Butterfly"` | **16** — 12 ring j=9 (§2.5) + 2 corner tops (9,9)/(9,0) (§2.6) + 2 floor (8,1)/(1,8) (§2.7) | `EntityButterfly` registered `"Butterfly"` (OSM:3607, 3611) | `ModEntities.ENTITY_BUTTERFLY` "butterfly" (ModEntities.java:516-518, MobCategory.AMBIENT) |
| `"Terrible Terror"` | **4** — 2 corner tops (0,0)/(0,9) + 2 floor (1,1)/(8,8) | `TerribleTerror` registered `"Terrible Terror"` (OSM:3999, 4003) | `ModEntities.ENTITY_TERRIBLE_TERROR` "terrible_terror" (ModEntities.java:266-268) |
| `"Lurking Terror"` | **2** — floor (4,4)/(5,5) | `LurkingTerror` registered `"Lurking Terror"` (OSM:4031, 4035) | `ModEntities.ENTITY_LURKING_TERROR` "lurking_terror" (ModEntities.java:238-240) |

22 spawners total. No direct entity spawns — spawner blocks only.

## 5. Block palette

| 1.7.10 field (meta) | Modern block | Used for | Cite |
|---|---|---|---|
| `Blocks.field_150347_e` | `minecraft:cobblestone` | floor, corners, j=6 rim, corner pillars | GD:2246-2261, 2316-2346 |
| `Blocks.field_150411_aY` | `minecraft:iron_bars` (default state — unconnected; precedent: HospitalGenerator.java:86, LegacyDungeonPiece.java:1105) | cage walls j=1..5 | GD:2243 |
| `Blocks.field_150349_c` | `minecraft:grass_block` | roof rings j=7/j=8 | GD:2272, 2282 |
| `Blocks.field_150344_f` meta 0 | `minecraft:oak_planks` | staircase treads | GD:2291-2294 |
| `Blocks.field_150422_aJ` | `minecraft:oak_fence` | staircase railings | GD:2295-2296 |
| `Blocks.field_150478_aa` | `minecraft:torch` | staircase lights (on fence posts, flag-2) | GD:2297-2298 |
| `Blocks.field_150350_a` | `minecraft:air` | cage/roof interiors | GD:2241, 2270, 2280 |
| `Blocks.field_150474_ac` | `minecraft:spawner` | 22 spawners | GD:2307 etc. |
| `Blocks.field_150486_ae` | `minecraft:chest` | 1 loot chest | GD:2401 |

## 6. Footprint extents + suggested DungeonType

Relative to `(cposx, cposy, cposz)`:

| Axis | Min | Max | Extent | Determined by |
|---|---|---|---|---|
| X | `-6` | `+9` | **16** | staircase start i=-6 (GD:2287) / cage i=9 (GD:2238) |
| Y | `0` | `+11` | **12** | floor j=0 (GD:2257) / corner-top spawners j=11 (GD:2318 etc.); stair torches top at +8 |
| Z | `0` | `+9` | **10** | cage k=0..9 (GD:2239); staircase z 3..6 is inside this |

Suggested entry (asymmetric 6-int ctor, +1 margin — the same box as HOSPITAL, which is the
same 10×10-cage-plus-west-ramp shape family, LegacyDungeonPiece.java:156):

```java
MINI_DUNGEON(-7, 10, 1, 12, -1, 10, PlacementMode.ISLANDS_GRASS),
```

**PlacementMode: `ISLANDS_GRASS` — exact fit, no new mode needed.** `addD4Mini`'s scan
(OSW:2391-2405, quoted in full §1.1) is line-for-line identical to
`addD4NightmareRookery`'s (OSW:2253-2261) — the scan `ISLANDS_GRASS` was written from —
minus the rookery's extra 30×9 air-clearance probe (OSW:2262-2268, which the mode does not
model anyway): LessLag 50% skip, `chunk + nextInt(8)` jitter, grass scan Y 20→5, anchor AT
the grass block. The port implementation (`islandsGrassOrigin`,
LegacyDungeonStructure.java:305-318) reproduces all of it, returning
`getBaseHeight − 1` and rejecting the chunk (null) when that Y falls outside the original
scan's 5..20 window (LegacyDungeonStructure.java:316 — a reject, not a clamp). Precedent users: ROBOT_LAB, KING/QUEEN_TOWER,
NIGHTMARE_ROOKERY, INCA_PYRAMID (LegacyDungeonPiece.java:89, 116-117, 132, 145).

## 7. Structure-set conversion

- Effective odds: Islands D4 roll `1/100 × 1/19` = **1/1900 per Islands chunk** (OSW:134-135,
  151) → C7 sqrt equivalence: spacing ≈ √1900 ≈ 43.6 → **spacing 44, separation 22** (the
  standard single-outcome-D4 pair, per pattern §1 step 4).
- **Salt 84360** (assigned to this task; grep of `RES:worldgen/structure_set/*.json` on
  extraction date shows the highest in the mod's 843xx dungeon-salt block = 84354
  [spit_bug_lair]; batch values 84350-84352 also taken; `dim_villages.json` sits outside
  the block at the vanilla-style 10387399. The historical mantis_nest/royal_trees 84312
  collision (pattern doc :138) is resolved — royal_trees now 84332. 84360 is free).
- `D4BigSpaceCheck` + `recently_placed` + LessLag couplings → structure-set separation /
  the ISLANDS_GRASS gate (C7 approximation; the mode already replays the LessLag 50% skip,
  LegacyDungeonStructure.java:306-308).

JSON trio (copy the `robot_lab`/`nightmare_rookery` trio and rename — same dimension +
anchor):

- `RES:worldgen/structure/mini_dungeon.json` — `"type": "orespawn:legacy_dungeon"`,
  `"dungeon_type": "MINI_DUNGEON"`, `"biomes": "#orespawn:has_structure/mini_dungeon"`,
  `"step": "surface_structures"`, `"spawn_overrides": {}` (terrain_adaptation: robot_lab
  uses `beard_thin`; the flat Islands plane makes it near-moot — follow the batch's
  prevailing choice).
- `RES:worldgen/structure_set/mini_dungeon.json` — random_spread, spacing 44,
  separation 22, salt 84360.
- `RES:tags/worldgen/biome/has_structure/mini_dungeon.json` — `["orespawn:island_biome"]`
  (matching robot_lab's tag file).

## 8. DungeonSpawnerBlock outcome

- Original: `if (type == 16)` → `makeMiniDungeon(world, clickedX, clickedY, clickedZ)` —
  one call, no offset, block read in full (DSB:101-103).
- Port: add to `src/main/java/danger/orespawn/block/entity/RandomDungeonSpawnerBlockEntity.java`
  constant `TYPE_MINI_DUNGEON = 16` (cite DSB:101-103) and case
  `TYPE_MINI_DUNGEON -> LegacyDungeonPiece.buildNow(server, pos, DungeonType.MINI_DUNGEON)`
  in `buildForType` (currently types 0/1/2/3/7/12/14/17/19/21/22/23/24/27/29/30/37/38/47 are
  wired, RandomDungeonSpawnerBlockEntity.java:44-79; **type 16 falls through to the
  generic-dungeon fallback** today, the `default` arm at :219).
- The DSB path bypasses the grass scan entirely — a cage floating or embedded in terrain at
  the clicked position is faithful behavior (standing FairyTree/Hospital ruling).

## 9. World-block READS mid-build

**Self-reads only.** The world reads inside GD:2229-2406 are the post-write tile-entity
fetches — 12 call sites: `getSpawnerTileEntity` at 11 sites (GD:2308 inside the j=9 ring
loop, which executes 12 times; GD:2319/2329/2339/2349 corners; GD:2357/2365/2373/2381/2389/
2397 floor) and `getChestTileEntity` ×1 at GD:2402 — 23 fetches at runtime (22 spawners +
1 chest), each at a position written the statement before — all absorbed by the port's `piece.placeSpawner` / `piece.placeLootChest` helpers.
There is no `func_147439_a` (getBlock) call anywhere in the method — no pre-build terrain
read, no in-memory model needed, no deviation decision.

## 10. RNG stream

Exactly **one draw** in the whole builder: the chest fill count `4 +
world.field_73012_v.nextInt(5)` (GD:2404), which moves into the loot JSON's `rolls 4-8`
(pattern §1 step 3 rule 3). The ported generator body is therefore RNG-free — geometry,
all 22 spawner positions/names, and the chest position are constants; every per-chunk
replay pass is trivially identical. (Dispatch-layer rolls — OSW:134/135 D4 roll and
OSW:2392/2395-2396 LessLag+jitter on the chunk `random`, DSB:52 on `world.rand` — live
outside the builder and map to structure-set placement / ISLANDS_GRASS / the DSB roll,
as usual.)

## 11. Surprises / MISSING-IN-PORT

- **S1 (MISSING-IN-PORT)**: `makeMiniDungeon` has no counterpart anywhere in
  `src/main/java/` (grep `makeMiniDungeon|MINI_DUNGEON|MiniDungeon`: zero matches). No
  DungeonType, no JSON trio, no loot table; worldgen i==10 and DSB type 16 both fall
  through today.
- **S2**: "Mini dungeon" is mini only by D4-castle standards — 16×12×10 footprint with
  **22 spawners** (16 Butterfly, 4 Terrible Terror, 2 Lurking Terror). The Butterfly is
  AMBIENT category in the port (ModEntities.java:517); 16 of the 22 spawners emit a
  harmless ambient mob. Do not "fix" the mix — it is the original's joke (butterflies
  everywhere, two Lurking Terrors hiding at the center floor next to the chest).
- **S3**: Floor anchor replaces terrain: the worldgen origin is the grass block itself
  (OSW:2398-2400, no +1/−1 offset), so the j=0 cobblestone floor overwrites the Islands
  grass surface and the cage interior sits at ground level. `ISLANDS_GRASS` anchors
  identically (grassY, LegacyDungeonStructure.java:312-317) — do not add an offset.
- **S4**: The staircase (§2.4) extends 6 blocks WEST of the cage (x −6..−1) with nothing
  below its treads — at worldgen on the flat Islands plane it floats one to six blocks
  above the grass. Faithful; do not add supports. Its top step (−1, +6) is level with the
  j=6 rim, letting players walk onto the j=7 grass ring.
- **S5**: Torches stand on fence posts (GD:2295-2298) via flag-2 writes — a placement
  modern survival rules reject; the port's `piece.place` UPDATE_CLIENTS write preserves
  them exactly like the original (pattern §1 step 3 table's floating-torch note). Iron
  bars go in as `defaultBlockState()` (unconnected) per the standing precedent
  (HospitalGenerator.java:86, EnderCastleGenerator.java:92) — visual-only delta until a
  neighbor update touches them.
- **S6**: The 12-spawner Butterfly ring at j=9 (§2.5) floats in the open roof bowl — the
  spawners rest on nothing (j=8's cells at i,k 3..6 interior are air/never-written).
  Faithful.
- **S7**: No cross-loop overwrite erasures (§2.9) — unlike Spit Bug Lair, later loops here
  only fill previously-written air or virgin cells, so write order matters only for the
  floor spawners/chest replacing interior air.
- **S8**: `addD4Mini` performs ONE jitter attempt (no 4-try loop, contrast the ocean/swamp
  scans) and scans Y 20→5 for grass — both already modeled by `ISLANDS_GRASS`
  (LegacyDungeonStructure.java:305-318). Its `return true` happens only on placement (§1.1)
  — no WGEN-062-style contract quirk, and the D4 dispatch discards the boolean anyway.
- **S9**: The loot list is entirely food/utility (golden apple, crystal apple, bacon,
  fire fish, instant garden/shelter — §3), total weight 190, rolls 4-8. Unusually for an
  OreSpawn dungeon there is no weapon/armor entry — do not invent any.
