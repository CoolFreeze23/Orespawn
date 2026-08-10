# Rainbow (cloud + rainbow arches) — Extraction Spec (1.7.10 → NeoForge 1.21.1)

Source of truth: `reference_1_7_10_source/sources/danger/orespawn/GenericDungeon.java` —
`makeRainbow` (GD:6260-6391, next method `makeEnormousCastleQ` at GD:6393), read IN FULL,
plus the instance color array `blkcolors` (GD:65) and the shared helpers
(`getSpawnerTileEntity` GD:86-95, `getChestTileEntity` GD:75-84). All coordinates are
relative to the build origin `(cposx, cposy, cposz)` = the three int args. Method-local
variables `width`/`depth`/`j` are reassigned per layer; every loop below is tabulated with
its own effective values.

Citation convention: `GD:NN` = GenericDungeon.java line NN, `OSW:NN` = OreSpawnWorld.java,
`OSM:NN` = OreSpawnMain.java, `DSB:NN` = DungeonSpawnerBlock.java, `RDS:NN` =
`src/main/java/danger/orespawn/block/entity/RandomDungeonSpawnerBlockEntity.java`,
`LDP:NN` = `src/main/java/danger/orespawn/world/structure/LegacyDungeonPiece.java`,
`LDS:NN` = `src/main/java/danger/orespawn/world/structure/LegacyDungeonStructure.java`.

Shared plumbing:
- Block writes via `OreSpawnMain.setBlockFast(world, x, y, z, block, meta, 2)` directly
  (GD:6276 etc.) — flag-2 chunk write, no neighbor updates → port
  `piece.place(x, y, z, state)` (structure_conversion_pattern.md §1 step 3 table).
- Spawners: `world.func_147465_d(..., field_150474_ac, 0, 2)` + `getSpawnerTileEntity` +
  `func_98272_a(name)` → `piece.placeSpawner(x, y, z, type)`.
- Chests: `world.func_147449_b(..., field_150486_ae)` (**default flag 3**, not flag 2) +
  `func_72921_c(x, y, z, 2, 3)` (**meta 2 = facing north**) + `getChestTileEntity` +
  `WeightedRandomChestContent.func_76293_a` → `piece.placeLootChest(...)` + loot JSON
  (flag delta is behaviorally a no-op — §12 S6).
- **Zero terrain reads anywhere in the method** — the only world reads are the 8
  tile-entity fetches after spawner/chest writes (self-reads, §10). Fully floating build.

---

## 1. Entry points — EVERY call site

Grep of the whole original tree for `makeRainbow`: exactly two call sites (OSW:2433 inside
`addD4Rainbow`, and DSB:192; GD:6260 is the definition). `addD4Rainbow` itself is called
from exactly one place: the Islands D4 dispatch at OSW:175 (`i == 18`).

| Caller | Signature used | Coords passed | Notes |
|---|---|---|---|
| `OreSpawnWorld.addD4Rainbow` (OSW:2433) | `OreSpawnMain.MyDungeon.makeRainbow(world, posX, 70 + world.field_73012_v.nextInt(20), posZ)` | X/Z jittered chunk pos, **Y = fixed sky band 70..89** — no scan of any kind | worldgen path, Islands (DimensionID4) only (§1.2) |
| `DungeonSpawnerBlock` type **46** (DSB:191-193) | `...makeRainbow(world, clickedX, clickedY, clickedZ)` | player-placed block pos, no offset | `type = world.field_73012_v.nextInt(50)` (DSB:52), fires 400 ticks after placement (DSB:39), self + block above deleted first (DSB:50-51). The `if (type == 46)` block read IN FULL: **single builder call, nothing else** (DSB:191-193) — not a two-builder index. |

### 1.1 `addD4Rainbow` — FULL method + return contract (OSW:2430-2436)

```java
// OSW:2430-2436
public boolean addD4Rainbow(World world, Random random, int chunkX, int chunkZ) {
    int posX = 4 + chunkX + random.nextInt(8);
    int posZ = 4 + chunkZ + random.nextInt(8);
    OreSpawnMain.MyDungeon.makeRainbow(world, posX, 70 + world.field_73012_v.nextInt(20), posZ);
    recently_placed = 50;
    return true;
}
```

**FULL return contract: unconditionally `true`, unconditionally builds, unconditionally
sets `recently_placed = 50` (OSW:2434 — the shared 50-chunk global cooldown declared at
OSW:30).** There is no LessLag gate (contrast `addD4GenericDungeon` OSW:2439-2441), no
biome check, no ground/space scan, and no failure path — every invocation places a
rainbow. The caller (OSW:175) discards the return value (bare statement), so the `true`
carries no chain coupling; the only cross-structure effect is the `recently_placed = 50`
write, which maps onto structure-set separation per the C7-approved approximation
(pattern §1 step 4). Anchoring quirk: X/Z draw from the chunk-provided `random`
(4 + chunk + nextInt(8) — the D4 corner-jitter shape), while Y draws from
`world.field_73012_v` (**world rand**) — same mixed-RNG quirk already documented on the
port's `skyBand150Origin` (LDS:219-235).

### 1.2 Worldgen dispatch chain (complete)

- `OreSpawnWorld.generate`, Islands branch: `world.field_73011_w.field_76574_g ==
  OreSpawnMain.DimensionID4` (OSW:132).
- Gate: `recently_placed == 0 && random.nextInt(100) == 0 &&
  this.D4BigSpaceCheck(world, chunkX*16, 7, chunkZ*16)` (OSW:134), then
  `i = random.nextInt(19)` (OSW:135); **`i == 18` → `addD4Rainbow` (OSW:174-176)** —
  the last slot of the D4 table (full i→structure table: enormous_castle_spec.md,
  reused per pattern §1 step 4).
- `D4BigSpaceCheck` (OSW:2655-2664): 65×55 probe at `posY + 4` = Y 11 — every block in
  `(x −25..+39, 11, z −25..+29)` must be air/log/apple-leaves/scary-leaves. Placement
  gate, not a mid-build read → absorbed into structure-set separation (pattern §1
  step 4, approved approximation).
- Effective odds: 1/100 × 1/19 = **1/1900 per Islands chunk**, no scan-failure factor
  (the add method cannot fail) — the standard D4 single-outcome arithmetic.

---

## 2. Geometry — per-loop tables (all ranges inclusive)

Write order matters: upper cloud slab → water "rain" → lower cloud shell → rainbow
arches (which OVERWRITE every water block — §12 S2) → spawners → chests. Loop bounds use
`i = -width; i < width` (X asymmetric: `−width..width−1`) and `k = -depth; k <= depth`
(Z symmetric).

### 2.1 Loop 1 — upper cloud slab, `j = +35` (GD:6270-6278)

`width = 12`, `depth = 1`, `blk_color = 0` (white): all `(−12..11, +35, −1..1)` =
**white stained clay** (24×1×3 solid slab).

### 2.2 Loop 2 — the "rain" columns (GD:6279-6283) — ALL LATER OVERWRITTEN

`k = 0`; `i` from `−width+1 = −11` step 3 while `< 12` → `i ∈ {−11, −8, −5, −2, 1, 4,
7, 10}` (8 columns):

| Cell (rel) | Block | Cite |
|---|---|---|
| `(i, +35, 0)` | water (source, `field_150355_j`) — replaces loop-1 slab cells | GD:6281 |
| `(i, +34, 0)` | flowing water (`field_150358_i`) — hangs under the slab | GD:6282 |

**Every one of these 16 water blocks is overwritten by the arch loop (§2.7) before the
method returns** — coverage proof in §12 S2. Flag-2 writes schedule no liquid updates,
so nothing flows in the interim; the finished structure contains ZERO water.

### 2.3 Loop 3 — lower cloud bottom ring, `j = +26` (GD:6284-6298)

`width = 13`, `depth = 2`: for `(−13..12, +26, −2..2)`, default air; perimeter
(`i == −13 || i == 12 || k == −2 || k == 2`) = **white stained clay**. 26×5 hollow ring —
interior `(−12..11, +26, −1..1)` is air (the cloud has **no floor** — §12 S3).

### 2.4 Loop 4 — lower cloud bulge ring, `j = +27` (GD:6299-6313)

`width = 14`, `depth = 3`: `(−14..13, +27, −3..3)`, perimeter (`i == −14 || i == 13 ||
k == −3 || k == 3`) = white stained clay, interior air. 28×7 ring — the widest layer.

### 2.5 Loop 5 — lower cloud top ring, `j = +28` (GD:6314-6328)

`width = 13`, `depth = 2`: identical shape to loop 3 at `j = +28` — 26×5 ring,
interior air.

### 2.6 Loop 6 — cloud roof slab, `j = +29` (GD:6329-6336)

`width = 12`, `depth = 1`: all `(−12..11, +29, −1..1)` = white stained clay (solid,
caps the loop-3..5 interior; the rainbow and chests stand on it). Net lower cloud: a
lens-shaped shell (rings 26/27/28 + roof 29), hollow and open underneath.

### 2.7 Loop 7 — the rainbow arches, base `j = 30` (GD:6337-6347)

For `m = 3..10` (inclusive; `m < 11`), `blk_color = blkcolors[m − 3]`
(`blkcolors = {14, 1, 4, 5, 3, 11, 10, 6}`, GD:65), all at `z = 0`, block = stained
clay of that color:

| Part | Cells (rel) | Cite |
|---|---|---|
| East leg | `(+m, +30..+30+m−1, 0)` | GD:6341 |
| West leg | `(−(m+1), +30..+30+m−1, 0)` | GD:6342 |
| Top bar | `(−(m+1)..+m, +30+m, 0)` | GD:6344-6346 |

Per-arch table (innermost → outermost):

| m | meta | Color (modern terracotta) | Legs at x | Legs y | Bar y | Bar x |
|---|---|---|---|---|---|---|
| 3 | 14 | red | +3 / −4 | 30..32 | 33 | −4..3 |
| 4 | 1 | orange | +4 / −5 | 30..33 | 34 | −5..4 |
| 5 | 4 | yellow | +5 / −6 | 30..34 | 35 | −6..5 |
| 6 | 5 | lime | +6 / −7 | 30..35 | 36 | −7..6 |
| 7 | 3 | light blue | +7 / −8 | 30..36 | 37 | −8..7 |
| 8 | 11 | blue | +8 / −9 | 30..37 | 38 | −9..8 |
| 9 | 10 | purple | +9 / −10 | 30..38 | 39 | −10..9 |
| 10 | 6 | pink | +10 / −11 | 30..39 | 40 | −11..10 |

The arches stand on the `j = 29` roof slab; the opening under the innermost (red) arch
is `x −3..2, y +30..+32, z 0`. Arches m ≥ 5 pass THROUGH the upper slab plane
(y +34/+35), overwriting its `z = 0` row across `x −11..10` — including all 16 water
blocks (§12 S2) — so the rainbow visibly threads the raining cloud (§12 S4).
`blkcolors` is used by NO other method (grep: GD:65 definition, GD:6339 sole use).

### 2.8 Spawners — 6 × "Cloud Shark" (GD:6348-6377)

Each: `func_147465_d(..., field_150474_ac, 0, 2)` + `func_98272_a("Cloud Shark")`. Two
3-high pillars flanking the arch opening, one block inside the red legs:

| # | Position (rel) | Mob | Cite |
|---|---|---|---|
| 1 | `(+2, +30, 0)` | `"Cloud Shark"` | GD:6348-6352 |
| 2 | `(−3, +30, 0)` | `"Cloud Shark"` | GD:6353-6357 |
| 3 | `(+2, +31, 0)` | `"Cloud Shark"` | GD:6358-6362 |
| 4 | `(−3, +31, 0)` | `"Cloud Shark"` | GD:6363-6367 |
| 5 | `(+2, +32, 0)` | `"Cloud Shark"` | GD:6368-6372 |
| 6 | `(−3, +32, 0)` | `"Cloud Shark"` | GD:6373-6377 |

All six cells are inside the arch opening (never touched by any earlier loop — they
embed in whatever air was there; no overwrite of structure blocks).

### 2.9 Chests (GD:6378-6390)

Two chest blocks at `(0, +30, 0)` and `(−1, +30, 0)` — adjacent along X on the roof
slab, centered under the arches. Each: `func_147449_b` (flag 3) + `func_72921_c(...,
2, 3)` (meta 2 = **facing north**) → 1.7.10 auto-unified double chest. **BOTH halves
are filled** (GD:6381-6384 and GD:6387-6390), each with
`WeightedRandomChestContent.func_76293_a(world.field_73012_v, RainbowContentsList,
chest, 10 + world.field_73012_v.nextInt(5))` → **10-14 pulls per half**.

Net shape: a hollow lens-shaped white cloud (y +26..+29) floating in the Islands sky,
an 8-color rainbow (y +30..+40, one block thick at z = 0) arching over six Cloud Shark
spawner blocks and a filled double chest, with a second flat white cloud slab at +35
that the rainbow pierces. No water survives (§12 S2).

---

## 3. Loot — FULL transcription

`RainbowContentsList` (GD:25) — constructor semantics `(item, meta=0, minStack,
maxStack, weight)`. **6 entries, total weight = 150** (6 × 25). Fill count:
`10 + world.field_73012_v.nextInt(5)` per chest half (GD:6383, 6389) →
`pools[0].rolls` uniform **min 10, max 14**; **both** double-chest halves get their own
10-14 pulls from the same list → one loot table JSON, bound to BOTH chest blocks.

| # | 1.7.10 item | Modern / port mapping (cite) | min | max | weight |
|---|---|---|---|---|---|
| 1 | `OreSpawnMain.MagicApple` | `orespawn:magic_apple` (ModItems.java:614; loot precedent basilisk_maze.json:242) | 1 | 1 | 25 |
| 2 | `OreSpawnMain.CloudSharkEgg` | `orespawn:cloud_shark_spawn_egg` (ModItems.java:914-915; spawn-egg-in-loot precedent beehive.json:128) | 4 | 10 | 25 |
| 3 | `Items.field_151103_aS` (bone) | `minecraft:bone` | 2 | 16 | 25 |
| 4 | `Items.field_151007_F` (string) | `minecraft:string` | 2 | 16 | 25 |
| 5 | `Items.field_151078_bh` (rotten flesh) | `minecraft:rotten_flesh` | 3 | 10 | 25 |
| 6 | `Items.field_151062_by` (bottle o' enchanting) | `minecraft:experience_bottle` | 4 | 10 | 25 |

→ `RES:loot_table/chests/rainbow.json`, rolls uniform 10-14, one entry per row,
`set_count` uniform per min/max above (omit for the 1/1 magic apple). Documented
approximation (pattern §1 step 5): original pulls landed in random slots with overwrite
collisions; a loot pool never collides.

---

## 4. Spawner / mob mapping table

| Spawner name | 1.7.10 class (registration) | Port EntityType (cite) |
|---|---|---|
| `"Cloud Shark"` (×6, GD:6351/6356/6361/6366/6371/6376) | `CloudShark` — `registerGlobalEntityID(CloudShark.class, "Cloud Shark", CloudSharkID)` OSM:4095, `registerModEntity` OSM:4099 | `ModEntities.CLOUD_SHARK` "cloud_shark" (ModEntities.java:60-63) — mapping already exercised by `CloudSharkDungeonGenerator.java:73-76` |

**No direct entity spawns** — spawner blocks only (no `spawnCreature`, no NBT, no yaw
handling anywhere in GD:6260-6391).

---

## 5. Block palette

| 1.7.10 field (meta) | Modern block | Used for | Cite |
|---|---|---|---|
| `Blocks.field_150406_ce` meta 0 | `minecraft:white_terracotta` | both cloud slabs + lens shell | GD:6276, 6291/6294, 6306/6309, 6321/6324, 6334 |
| `Blocks.field_150406_ce` meta 14 | `minecraft:red_terracotta` | arch m=3 | GD:65, 6339-6346 |
| `Blocks.field_150406_ce` meta 1 | `minecraft:orange_terracotta` | arch m=4 | ″ |
| `Blocks.field_150406_ce` meta 4 | `minecraft:yellow_terracotta` | arch m=5 | ″ |
| `Blocks.field_150406_ce` meta 5 | `minecraft:lime_terracotta` | arch m=6 | ″ |
| `Blocks.field_150406_ce` meta 3 | `minecraft:light_blue_terracotta` | arch m=7 | ″ |
| `Blocks.field_150406_ce` meta 11 | `minecraft:blue_terracotta` | arch m=8 | ″ |
| `Blocks.field_150406_ce` meta 10 | `minecraft:purple_terracotta` | arch m=9 | ″ |
| `Blocks.field_150406_ce` meta 6 | `minecraft:pink_terracotta` | arch m=10 | ″ |
| `Blocks.field_150350_a` | `minecraft:air` | lens-ring interiors | GD:6289, 6304, 6319 |
| `Blocks.field_150355_j` | (water — **dead writes**, all overwritten §12 S2) | rain columns | GD:6281 |
| `Blocks.field_150358_i` | (flowing water — dead writes) | rain columns | GD:6282 |
| `Blocks.field_150474_ac` | `minecraft:spawner` | 6 spawners | GD:6348-6373 |
| `Blocks.field_150486_ae` | `minecraft:chest[facing=north]` | double loot chest | GD:6379-6386 |

---

## 6. Footprint extents (relative to origin) + suggested DungeonType

| Axis | Min | Max | Extent | Determined by |
|---|---|---|---|---|
| X | `−14` | `+13` | **28** | bulge ring `width = 14` (GD:6299, 6302) |
| Y | `+26` | `+40` | **15** | bottom ring `j = 26` (GD:6286) / m=10 bar `y = 30+10` (GD:6345); **nothing is ever written below +26 or at/below the origin** |
| Z | `−3` | `+3` | **7** | bulge ring `depth = 3` (GD:6300, 6303) |

Suggested entry (6-int asymmetric ctor `(minXOff, maxXOff, downExtent, upExtent,
minZOff, maxZOff, mode)`, +1 margin on X/Z/up; `downExtent 0` already leaves 26 blocks
of slack below the lowest write):

```java
RAINBOW(-15, 14, 0, 41, -4, 4, PlacementMode.SKY_BAND_70),
```

## 7. Placement — **NEEDS_NEW_MODE** (`SKY_BAND_70`), original scan quoted

No existing mode fits. The anchor is the Cloud Shark no-scan sky-band shape but in a
DIFFERENT band: `SKY_BAND_150` hardcodes `y = 150 + nextInt(10)` (LDS:229-235), while
the rainbow uses **`y = 70 + nextInt(20)`**. The original "scan" in full:

```java
// OSW:2430-2436 — there is no scan; placement is unconditional
int posX = 4 + chunkX + random.nextInt(8);
int posZ = 4 + chunkZ + random.nextInt(8);
OreSpawnMain.MyDungeon.makeRainbow(world, posX, 70 + world.field_73012_v.nextInt(20), posZ);
recently_placed = 50;
return true;
```

Pattern §1 step 4 sanctions adding a mode for exactly this case ("if the original's
`add*` method does a ground scan the existing PlacementModes don't cover, add a mode").
Suggested: add **`SKY_BAND_70`** to `LegacyDungeonPiece.DungeonType.PlacementMode`
(after `SKY_BAND_150`, LDS case at :79) + a `skyBand70Origin` case in
`LegacyDungeonStructure.findGenerationPoint` — a line-for-line clone of
`skyBand150Origin` (LDS:229-235) with `int y = 70 + context.random().nextInt(20);`,
carrying over its Javadoc note that the original's mixed-RNG quirk (Y from world rand,
X/Z from the chunk random, §1.1) collapses into the single seeded structure-start
random. Unconditional success; frequency lives entirely in the structure set. (This is
a mode-parameter decision the pattern doc covers — no NEEDS_DESIGN_RULING condition
arises.)

JSON trio (copy the `cloud_shark_dungeon` trio — same dimension, same no-scan anchor
family):

- `RES:worldgen/structure/rainbow.json` — `"type": "orespawn:legacy_dungeon"`,
  `"dungeon_type": "RAINBOW"`, `"biomes": "#orespawn:has_structure/rainbow"`,
  `"step": "surface_structures"`, `"terrain_adaptation": "none"`,
  `"spawn_overrides": {}`.
- `RES:tags/worldgen/biome/has_structure/rainbow.json` — `["orespawn:island_biome"]`
  (matching cloud_shark_dungeon's tag file).
- `RES:worldgen/structure_set/rainbow.json` — §8.

## 8. Structure-set conversion

Effective odds: 1/100 (D4 gate, OSW:134) × 1/19 (`i == 18`, OSW:135/174) = **1/1900
per Islands chunk**, with no scan-failure attenuation (`addD4Rainbow` cannot fail).
C7 sqrt equivalence: spacing ≈ √1900 ≈ 43.6 → **spacing 44, separation 22** — the
standard Islands-D4 single-outcome pair (pattern §1 step 4). The `recently_placed`
cooldown and the `D4BigSpaceCheck` 65×55 air probe (OSW:2655-2664) map onto
structure-set separation (approved C7 approximation).

Salt: **84367** (assigned to this task; verified free — grep of
`RES:worldgen/structure_set/*.json` shows OreSpawn salts topping out at 84361
(cephadrome_altar), plus vanilla-style 10387399 (dim_villages); all currently unique).

`RES:worldgen/structure_set/rainbow.json`: random_spread, spacing 44, separation 22,
salt 84367.

## 9. DungeonSpawnerBlock outcome

- Original: `if (type == 46)` → `makeRainbow(world, clickedX, clickedY, clickedZ)` —
  one call, no offset, block read in full (DSB:191-193).
- Port: add `TYPE_RAINBOW = 46` (cite DSB:191-193) and
  `case TYPE_RAINBOW -> LegacyDungeonPiece.buildNow(server, pos, DungeonType.RAINBOW)`
  in `RandomDungeonSpawnerBlockEntity.buildForType` (wired types today:
  0/1/2/3/7/12/13/14/15/16/17/18/19/20/21/22/23/24/27/29/30/34/37/38/47, RDS:44-91;
  **type 46 currently falls through to the generic-dungeon default**, RDS:262).
- The DSB path bypasses the sky band entirely — a cloud-and-rainbow built 26-40 blocks
  ABOVE wherever the player placed the block (possibly intersecting terrain; nothing is
  cleared outside the written cells) is faithful behavior.

## 10. Mid-build world READS — classified

1. **Tile-entity fetches only**: 6 spawner fetches (GD:6349/6354/6359/6364/6369/6374)
   and 2 chest fetches (GD:6381, 6387) — SELF-reads of blocks written the line before;
   absorbed by `piece.placeSpawner` / `piece.placeLootChest`. No deviation decision
   needed.
2. **No other read exists**: zero `func_147439_a` / `func_147437_c` calls in
   GD:6260-6391. No foundation probe, no terrain conditioning — the generator is
   read-free (simpler than every prior D6b structure; nothing to model, nothing to
   flag).

## 11. RNG stream

The only draws in the builder are the **2 chest fill counts**
(`10 + world.field_73012_v.nextInt(5)`, GD:6383 and GD:6389), both of which move into
the loot JSON's `rolls 10-14` (pattern §1 step 3 rule 3 / step 5). The ported generator
therefore consumes **zero** random draws — clouds, arch colors (fixed `blkcolors`
array, GD:65), spawner positions, and chest positions are all constants; every
per-chunk replay pass is trivially identical. Dispatch-layer rolls (OSW:134 gate +
OSW:135 19-way pick + OSW:2431-2432 jitter on the chunk `random`; OSW:2433 Y band and
DSB:52 on `world.field_73012_v`) collapse into structure-set frequency / the new
placement mode / the DSB roll as usual.

## 12. Surprises / MISSING-IN-PORT

- **S1 (MISSING-IN-PORT)**: `makeRainbow` has no counterpart anywhere in
  `src/main/java/` (grep `Rainbow`: only the Rainbow Ant entity/block/egg family). No
  DungeonType, no generator, no JSON trio, no loot table; DSB type 46 falls through to
  the generic-dungeon default (RDS:262).
- **S2 — the rain never survives (dead writes)**: loop 2 places 8 water sources at
  `(i, +35, 0)` and 8 flowing-water blocks at `(i, +34, 0)` (GD:6279-6283), and the
  arch loop later overwrites **all 16**. Proof: at `y +35, z 0` the m=5 bar covers
  `x −6..5` and the m≥6 legs cover `x {6,−7,7,−8,8,−9,9,−10,10,−11}` — union
  `−11..10`, a superset of the water columns `{−11,−8,−5,−2,1,4,7,10}`; at `y +34` the
  m=4 bar (`−5..4`) plus the m≥5 legs (`{5,−6,…,10,−11}`) likewise cover `−11..10`.
  Flag-2 writes schedule no liquid updates, so nothing flows in between. The finished
  structure contains ZERO water. Port: replicate the write order (identical final
  state) or omit the water writes with a citation comment — final-state identical
  either way; do NOT "fix" the rain by moving the water elsewhere.
- **S3 — bottomless cloud**: the lower cloud's `j = +26` layer is a RING (interior
  air, GD:6289), so the lens is open underneath — players can fly up into the hollow
  (roofed at `j = +29`). Faithful; do not add a floor.
- **S4 — the rainbow pierces the upper cloud**: arches m ≥ 5 pass through the
  `y +34/+35` plane, so the upper slab's `z = 0` row ends as rainbow terracotta across
  `x −11..10` (only `x = −12` and `x = 11` stay white). Faithful — preserve write
  order (slab first, arches after).
- **S5 — color order**: `blkcolors = {14, 1, 4, 5, 3, 11, 10, 6}` → red innermost,
  pink outermost (a real rainbow has red OUTERMOST). Faithful; transcribe the array,
  do not "correct" it.
- **S6 — chest write flags + facing**: the chests are the method's only non-flag-2
  writes (`func_147449_b` = flag 3, GD:6379/6385) and get meta 2 = facing north via
  `func_72921_c(..., 2, 3)` (GD:6380/6386). Behaviorally the flag delta is a no-op
  (final state identical; nothing flammable/fluid adjacent), so flag-2 `piece`
  helpers match. Double-chest port shape per leaf_monster_dungeon_spec.md S7: both
  halves `facing=NORTH`, `(−1)` = `ChestType.LEFT`, `(0)` = `ChestType.RIGHT`
  (LEFT's partner sits clockwise of facing = east) — but unlike the Leaf Monster,
  **BOTH halves are loot-filled** (10-14 pulls each, GD:6383/6389): bind the loot
  table to both blocks. If `placeLootChest` still lacks a ChestType parameter (leaf
  monster S7), extend it the same WGEN-056 way.
- **S7 — unconditional placement**: `addD4Rainbow` has no scan, no biome check, no
  LessLag gate, and no failure path — always builds, always `recently_placed = 50`,
  always returns `true` (return value discarded by the sole caller, OSW:175). The
  whole contract collapses into structure-set frequency + the new placement mode;
  there is no WGEN-062-style quirk to carry.
- **S8 — mixed RNG**: X/Z from the chunk-provided `random` (OSW:2431-2432), Y from
  `world.field_73012_v` (OSW:2433), loot counts from `world.field_73012_v`
  (GD:6383/6389) — same shape as addD4CloudShark; collapses into the seeded
  structure-start random (documented on `skyBand150Origin`, LDS:219-228; repeat the
  note on the new `SKY_BAND_70`).
- **S9 — Y band differs from Cloud Shark's**: 70 + nextInt(20) → absolute build span
  Y 96..129 (origin 70..89 + writes +26..+40) vs the Cloud Shark's 150..159 band. This
  is why `SKY_BAND_150` cannot be reused as-is (§7) — losing the band would move the
  structure ~60 blocks (pattern §3 trap 7: Y windows are behavior).
- **S10 — nothing below the origin**: no write, read, or clear at or below `y +25`;
  the origin itself is 26 blocks under the lowest block. The suggested box keeps
  `downExtent 0` — do not inflate it downward (pattern §1 step 2: every covered chunk
  pays a replay).
