# Damsel In Distress — Extraction Spec (1.7.10 → NeoForge 1.21.1)

Source of truth: `reference_1_7_10_source/sources/danger/orespawn/GenericDungeon.java` —
`makeDamselInDistress` (GD:3625-3733, next method `makeIncaPyramid` at GD:3735). Method
read IN FULL, plus its plumbing helpers (`getSpawnerTileEntity` GD:86-95,
`getChestTileEntity` GD:75-84; writes go through `OreSpawnMain.setBlockFast(..., meta,
flags 2)` directly, GD:3664 etc.). All coordinates are relative to the build origin
`(cposx, cposy, cposz)` = the three int args. Method-local constants: `length = 4`
(GD:3636), `width = 4` (GD:3637), `height = 5` (GD:3638). Dead locals: `stuffdir`
(init 0 at GD:3631, set 2 at GD:3639 — never read) and the `meta = 0` reset at GD:3668
(meta stays 0 for every `setBlockFast` write in the method).

Citation convention: `GD:NN` = GenericDungeon.java, `OSW:NN` = OreSpawnWorld.java,
`OSM:NN` = OreSpawnMain.java, `DSB:NN` = DungeonSpawnerBlock.java, `GF:NN` = orig
`Girlfriend.java`, `LDP:NN` = `src/main/java/danger/orespawn/world/structure/
LegacyDungeonPiece.java`, `LDS:NN` = `.../LegacyDungeonStructure.java`, `RDS:NN` =
`src/main/java/danger/orespawn/block/entity/RandomDungeonSpawnerBlockEntity.java`.

Shared plumbing:
- All geometry writes are `setBlockFast(..., bid, meta 0, flags 2)` → port
  `piece.place(x, y, z, state)` (pattern §1 step 3 table).
- Spawners: `func_147465_d(..., field_150474_ac, 0, 2)` + `getSpawnerTileEntity` +
  `func_98272_a(name)` → `piece.placeSpawner(x, y, z, type)`.
- Chest: `func_147465_d(..., field_150486_ae, 2, 2)` (**meta 2 = facing north**) +
  `getChestTileEntity` + `func_76293_a` → `piece.placeLootChest(x, y, z,
  Direction.NORTH, lootKey)` (facing overload, LDP:474).
- **One DIRECT entity spawn** — a "Girlfriend" placed in the jail cell (GD:3727-3732),
  extracted exactly in §4.1.
- Client-side guard `if (world.field_72995_K) return;` (GD:3640-3642) — a no-op in the
  port (structure postProcess and `buildNow` are server-only); dropped, documented (§12
  S11).
- Mid-build world READS: **none** besides the tile-entity self-reads absorbed by the
  piece helpers (§10). No `func_147439_a`/`func_147437_c` anywhere in GD:3625-3733.

---

## 1. Entry points — EVERY call site

Grep of the whole original tree for `makeDamselInDistress`: exactly two call sites
(OSW:1311 and DSB:138; GD:3625 is the definition).

| Caller | Signature used | Coords passed | Notes |
|---|---|---|---|
| `OreSpawnWorld.addDamselInDistress` (OSW:1311) | `OreSpawnMain.MyDungeon.makeDamselInDistress(world, posX, posY - 1, posZ)` | scan hit **minus 1** — the origin is the GRASS BLOCK itself, not the air above (contrast Leaf Monster/Spit Bug) | worldgen path, **Village dimension (DimensionID3) only** (§1.2) |
| `DungeonSpawnerBlock` type **28** (DSB:137-139) | `...makeDamselInDistress(world, clickedX, clickedY, clickedZ)` | player-placed block pos, no offset | roll `type = world.rand.nextInt(50)` (DSB:52), fires 400 ticks after placement (DSB:39), self + block above deleted first (DSB:50-51). The `if (type == 28)` block read IN FULL: **single builder call, nothing else** (DSB:137-139) — not a two-builder index. |

### 1.1 `addDamselInDistress` — FULL method + return contract (OSW:1301-1317)

```java
// OSW:1301-1317
public boolean addDamselInDistress(World world, Random random, int chunkX, int chunkZ) {
    if (random.nextInt(250) != 0) {
        return false;
    }
    for (int i = 0; i < 4; ++i) {
        int posX = chunkX + random.nextInt(16);
        int posZ = chunkZ + random.nextInt(16);
        boolean which = false;
        for (int posY = 100; posY > 40; --posY) {
            if (world.func_147439_a(posX, posY, posZ) != Blocks.field_150350_a
                || world.func_147439_a(posX, posY - 1, posZ) != Blocks.field_150349_c
                || !this.quickSpaceCheck(world, posX, posY - 1, posZ)) continue;
            OreSpawnMain.MyDungeon.makeDamselInDistress(world, posX, posY - 1, posZ);
            recently_placed = 50;
            return true;
        }
    }
    return false;
}
```

1. Gate: `random.nextInt(250) != 0 → return false` (OSW:1302-1304) — 1/250, chunk
   `random`.
2. Up to 4 attempts (OSW:1305): `posX/posZ = chunk + random.nextInt(16)`
   (OSW:1306-1307). Dead local `which` (OSW:1308) — ignore.
3. Column scan `posY = 100` down to `41` inclusive (OSW:1309): require air at `posY`
   AND **grass block** (`field_150349_c`) at `posY − 1` AND
   `quickSpaceCheck(world, posX, posY - 1, posZ)` (OSW:1310).
4. Hit → `makeDamselInDistress(world, posX, posY - 1, posZ)` — **origin = the grass
   block** — then `recently_placed = 50`, `return true` (OSW:1311-1313).
5. All attempts miss → `return false` (OSW:1316).

**FULL return contract: `true` ONLY on an actual placement** (which also sets the
50-chunk global cooldown, OSW:1312). Gate fail and scan fail both return `false`. No
WGEN-062-style early-true quirk. NOTE: the caller ignores the return value (§1.2) — the
chaining in this dimension is done with `recently_placed` gates OUTSIDE the methods, so
the boolean carries no coupling beyond the cooldown it sets.

`quickSpaceCheck` (OSW:2625-2633): 12×12 clearance probe — every block at
`(posX-2..posX+9, argY+4, posZ-2..posZ+9)` must be air. Called here with `argY =
posY - 1`, so the probed plane is **origin + 4** (one below the roof course at +5),
offset toward +x/+z exactly like the graveyard's use. Placement scan, not a mid-build
read → maps into the PlacementMode (§7).

### 1.2 Worldgen dispatch chain (complete)

- `OreSpawnWorld.generate`, `world.field_73011_w.field_76574_g ==
  OreSpawnMain.DimensionID3` branch (OSW:114-131): mosquitos (config-gated),
  `addAnts ×4`, `addAppleTrees`, `addGenericDungeon` (unconditional), then
  `if (recently_placed == 0) addDamselInDistress` (**OSW:121-123**), then
  `if (recently_placed == 0) addSpiderHangout` (OSW:124-126), then
  `if (recently_placed == 0) addRedAntHangout` (OSW:127-129).
- **DimensionID3 = the Village dimension**: `BaseDimensionID + 2` (OSM:1597),
  provider `WorldProviderOreSpawn3` ("Dimension-VillageMania",
  WorldProviderOreSpawn3.java:24), single biome `BiomeGenUtopianPlains` instance named
  "Villages" with `BiomeVillageID` (WorldProviderOreSpawn3.java:21) → port biome
  `orespawn:village_biome` (precedent: `RES:worldgen/structure/dim_village.json` uses
  the inline `"biomes": "orespawn:village_biome"`).
- No `DisableOverworldDungeons` gate (that gate is overworld-only, OSW:284) and no
  per-attempt biome check (the whole dimension is one biome).
- Effective odds: **1/250 per Village-dimension chunk** before scan success (the
  `recently_placed == 0` pre-gate and generic-dungeon coupling map onto structure-set
  separation per the C7 approximation, pattern §1 step 4).

---

## 2. Geometry — per-loop tables (all ranges inclusive)

Axes: loop `i` = X offset, `j` = Z offset in loop A (but **Y offset** in loop E — watch
the swap), `k` = Y offset in loop A (but **Z offset** in loop E). Write order:
shell → roof course 1 → roof course 2 → front gable → jail bars → spawners → chest →
girlfriend. Later writes overwrite earlier ones (gable re-rolls two roof rows, §2.4).

Mossy rule (used by loops A-D): whenever the chosen block is cobblestone, draw
`world.field_73012_v.nextInt(8)`; on `== 1` substitute mossy cobblestone
(GD:3657-3659, 3673-3675, 3683-3685, 3693-3701). The draw is conditional ONLY on loop
indices (never on world state) — see §11.

### 2.1 Loop A — 9×9×5 shell with 3×3 doorway (GD:3643-3667)

`i = −4..+4`, `j = −4..+4`, `k = 0..+4`; per cell, in source order:
`bid = air` (GD:3646); `k == 0` → cobble floor (GD:3648-3650); `i == ±4` → cobble wall
(GD:3651-3653); `j == ±4` → cobble wall (GD:3654-3656); mossy roll if cobble
(GD:3657-3659); **door carve LAST** — `k ∈ {1,2,3} && i ∈ {−1,0,1} && j == −length`
→ `bid = air` (GD:3660-3663, the decompiled negation de-Morgans to exactly that);
write at `(cposx+i, cposy+k, cposz+j)` (GD:3664).

| Where | Cell (rel) | Block | Cite |
|---|---|---|---|
| floor | `(−4..4, 0, −4..4)` | cobble (1/8 mossy) | GD:3648-3650 |
| walls | `i = ±4` or `j = ±4`, `y 1..4` | cobble (1/8 mossy) | GD:3651-3656 |
| doorway | `(−1..1, 1..3, −4)` | air (carved AFTER the mossy roll — those 9 cells still consume draws, §11) | GD:3660-3663 |
| interior | `(−3..3, 1..4, −3..3)` | air | GD:3646 |

### 2.2 Loop B — roof course 1, `y = +5` (GD:3669-3678)

`i = −3..+3` (`-width+1..width-1`), `j = −4..+3` (`-length..length-1` — **asymmetric**,
stops one short on +z), `k = height = 5`: cobble, 1/8 mossy (GD:3672-3676). 7×8 slab.
Covers every interior column (interior z ≤ +3) → the room is sealed; the strip above
the back wall (`z = +4, y = 5`) is **never written** (§12 S6).

### 2.3 Loop C — roof course 2, `y = +6` (GD:3679-3688)

`i = −2..+2` (`-width+2..width-2`), `j = −4..+2` (`-length..length-2`), `k = 6`:
cobble, 1/8 mossy (GD:3682-3686). 5×7 slab, again biased toward −z.

### 2.4 Loop D — front gable at `z = −4`, `y = +5..+9` (GD:3689-3705)

`k` starts at `height = 5`, `j = −length = −4` fixed (GD:3689-3690). Outer
`m = 4..0` descending; inner `i = m..0` descending; per inner iteration TWO
draw+write pairs — `(cposx+i, cposy+k, cposz−4)` then `(cposx−i, cposy+k, cposz−4)`,
each cobble with its own 1/8 mossy roll (GD:3692-3702); `++k` after each `m`
(GD:3704). Expanded:

| m | y | x cells | Cite |
|---|---|---|---|
| 4 | +5 | −4..+4 (x=0 written twice, two draws) | GD:3691-3703 |
| 3 | +6 | −3..+3 (ditto) | " |
| 2 | +7 | −2..+2 (ditto) | " |
| 1 | +8 | −1..+1 (ditto) | " |
| 0 | +9 | 0 (written twice, two draws) | " |

A solid triangular gable on the front face. Its `y = +5` row overwrites loop B's
`z = −4` row (x −3..3) and its `y = +6` row overwrites loop C's `z = −4` row (x −2..2)
— same block family, fresh mossy rolls, later write wins (§12 S5).

### 2.5 Loop E — iron-bar jail wall at `z = +1` (GD:3706-3711)

**Index swap:** here `j` is the Y offset and `k` the Z offset. `i = −3..+3`
(`-width+1; i < width`), `j = 1..4` (`j < height`), `k = length − 3 = 1` (GD:3708);
write iron bars at `(cposx − i, cposy + j, cposz + 1)` (GD:3709 — the `−i` spans the
same −3..+3 set). 7×4 = 28 bars, no RNG. Spans the full interior width and height →
partitions the hut into a front room (`z −3..0`) and a **sealed back cell**
(`z +2..+3`) holding the chest and the Girlfriend — the "damsel in distress" locked
behind bars. All 28 cells were loop-A interior air; the bars' top row (y 4) meets the
roof at y 5.

### 2.6 Spawners — 2 × "Scorpion" in the front room (GD:3712-3721)

Each: `func_147465_d(..., field_150474_ac, 0, 2)` + `func_98272_a("Scorpion")`.

| # | Position (rel) | Expression | Mob | Cite |
|---|---|---|---|---|
| 1 | `(−3, +1, −3)` | `(cposx−width+1, cposy+1, cposz−length+1)` | `"Scorpion"` | GD:3712-3716 |
| 2 | `(+3, +1, −3)` | `(cposx+width−1, cposy+1, cposz−length+1)` | `"Scorpion"` | GD:3717-3721 |

Both front corners, on the floor, flanking the doorway. Loop-A interior air cells — no
overwrite of walls or bars.

### 2.7 Chest — in the jail cell (GD:3722-3726)

`world.func_147465_d(cposx + width − 1, cposy + 1, cposz + length − 1,
field_150486_ae, 2, 2)` → chest at `(+3, +1, +3)`, **meta 2 = facing north** (into the
cell), back-east corner behind the bars. Fill: `func_76293_a(world.field_73012_v,
DamselContentsList, chest, 10 + world.field_73012_v.nextInt(5))` → **10-14 pulls**
(GD:3725). Single chest, no double-chest pairing.

Net shape: a 9×9 cobble/mossy hut with a 3×3 front doorway, stepped cobble roof
(+5/+6) biased toward the front, a solid triangular front gable rising to +9, an
iron-bar wall splitting off a back jail cell containing a loot chest and a live
Girlfriend, and two Scorpion spawners guarding the front room. No foundation — nothing
is written below y 0.

---

## 3. Loot — FULL transcription

`DamselContentsList` (GD:39) — constructor semantics `(item, meta=0, minStack,
maxStack, weight)`. **9 entries, total weight = 315** (9 × 35). Fill count
`10 + nextInt(5)` (GD:3725) → `pools[0].rolls` uniform **min 10, max 14**.

| # | 1.7.10 item | Modern / port mapping (cite) | min | max | weight |
|---|---|---|---|---|---|
| 1 | `Items.field_151035_b` | `minecraft:iron_pickaxe` | 1 | 1 | 35 |
| 2 | `Items.field_151040_l` | `minecraft:iron_sword` | 1 | 1 | 35 |
| 3 | `Items.field_151157_am` | `minecraft:cooked_porkchop` | 3 | 10 | 35 |
| 4 | `Items.field_151082_bd` | `minecraft:beef` (raw) | 3 | 10 | 35 |
| 5 | `Items.field_151077_bg` | `minecraft:cooked_chicken` | 3 | 10 | 35 |
| 6 | `Items.field_151101_aQ` | `minecraft:cooked_cod` (1.7.10's single cooked-fish item; documented mapping) | 3 | 10 | 35 |
| 7 | `OreSpawnMain.MyBLT` ("blt_sandwich", OSM:1856/2333) | port `ModItems.BLT_SANDWICH` "blt_sandwich" (ModItems.java:546) | 4 | 10 | 35 |
| 8 | `OreSpawnMain.MySalad` ("salad", OSM:1855/2332) | port `ModItems.SALAD` "salad" (ModItems.java:544) | 4 | 10 | 35 |
| 9 | `OreSpawnMain.MyCornDog` ("corndog_cooked", OSM:1847/2310) | port `ModItems.CORN_DOG` "corn_dog" (ModItems.java:520) | 4 | 10 | 35 |

→ `RES:loot_table/chests/damsel_in_distress.json`, rolls uniform 10-14, one entry per
row, `set_count` per row (omit for the 1/1 tools). Documented approximation (pattern
§1 step 5): original pulls landed in random chest slots with overwrite collisions; a
loot pool never collides.

**Shared list**: `makeGirlfriendIsland` (GD:4962) uses the SAME `DamselContentsList`
(GD:4968) for TWO chests, each filled `4 + nextInt(5)` → rolls 4-8 per chest
(GD:5021, 5026). ~~When GirlfriendIsland is ported it should reference this same JSON
with its own `rolls` — do not fork the item list (§12 S10).~~ **SUPERSEDED by the
batch-3 ruling (girlfriend_island_spec.md §3, pattern §1 step 5 as amended): a table's
`rolls` is fixed per file, so GirlfriendIsland ships its OWN
`chests/girlfriend_island.json` (rolls 4-8) with entries identical to this one — one
table per (list, fill formula) pair, the Kyuubi precedent. Add a `_shared_list`
comment key here naming the twin.**

---

## 4. Mob / entity table

| Use | Name | 1.7.10 registration | Port EntityType (cite) |
|---|---|---|---|
| Spawner ×2 (GD:3715/3720) | `"Scorpion"` | `registerGlobalEntityID(Scorpion.class, "Scorpion", ScorpionID)` OSM:3791, `registerModEntity` OSM:3795 | `ModEntities.ENTITY_SCORPION` "scorpion" (ModEntities.java:258-260) |
| **DIRECT spawn ×1** (GD:3727-3732) | `"Girlfriend"` | `registerGlobalEntityID(Girlfriend.class, "Girlfriend", GirlfriendID)` OSM:3575, `registerModEntity` OSM:3579 | `ModEntities.GIRLFRIEND` "girlfriend" (ModEntities.java:445-447) |

### 4.1 The direct Girlfriend spawn — EXACT extraction (GD:3727-3732)

```java
// GD:3727-3732
Entity var8 = null;
var8 = EntityList.func_75620_a((String)"Girlfriend", (World)world);
if (var8 != null) {
    var8.func_70012_b((double)(cposx - width + 2), (double)(cposy + 1),
                      (double)(cposz + length - 1),
                      world.field_73012_v.nextFloat() * 360.0f, 0.0f);
    world.func_72838_d(var8);
}
```

- **Position**: `(cposx − 2, cposy + 1, cposz + 3)` — inside the jail cell, on the
  floor, two west of the origin column (five west of the chest at `+3, +1, +3`). The
  doubles are casts of INTS: the entity stands at
  the block CORNER, not the +0.5 center. Transcribe the corner coordinates exactly —
  do not "fix" to center (§12 S9).
- **Yaw**: `world.rand.nextFloat() * 360.0f`, pitch 0 (`func_70012_b` =
  setLocationAndAngles). One RNG draw.
- **NBT / init**: NONE — no `func_110161_a`/onSpawnWithEgg, no tags, no taming; a
  factory-fresh entity is placed via `func_72838_d` (spawnEntityInWorld — no spawn-rule
  checks run on this path).
- **Persistence**: the spawn call sets NO persistence flag (`func_110163_bv` is NOT
  called). The entity never despawns anyway because the CLASS overrides
  `func_70692_ba() → false` (canDespawn, GF:850-852; Girlfriend extends
  EntityTameable, GF:59). The port entity carries the same override —
  `removeWhenFarAway → false` (port entity/Girlfriend.java:213-215).
- **Port mapping**: `piece.spawnEntity(ModEntities.GIRLFRIEND.get(), ox − 2, oy + 1,
  oz + 3, yaw)` (LDP:518-527 — the gated, caller-draws-yaw direct-spawn helper; its
  Javadoc precedent is the Hospital's end crystals, same "random yaw, no persistence
  flag, class never despawns" shape). Draw
  `yaw = random.nextFloat() * 360.0f` UNCONDITIONALLY in the generator before the call
  (RNG contract). Do NOT use `spawnPersistent` — it would write a
  `PersistenceRequired` flag the original save never had; the class-level despawn
  override is what carries persistence, faithfully, in both versions.

---

## 5. Block palette

| 1.7.10 field | Modern block | Used for | Cite |
|---|---|---|---|
| `Blocks.field_150350_a` | `minecraft:air` | interior, doorway | GD:3629/3646/3662 |
| `Blocks.field_150347_e` | `minecraft:cobblestone` | floor, walls, roof, gable | GD:3649/3652/3655/3672/3682/3693/3698 |
| `Blocks.field_150341_Y` | `minecraft:mossy_cobblestone` | 1/8 substitution on every cobble | GD:3658/3674/3684/3695/3700 |
| `Blocks.field_150411_aY` | `minecraft:iron_bars` | jail wall | GD:3709 |
| `Blocks.field_150474_ac` | `minecraft:spawner` | 2 spawners | GD:3712/3717 |
| `Blocks.field_150486_ae` (meta 2) | `minecraft:chest[facing=north]` | 1 loot chest | GD:3722 |
| (placement scan only) `Blocks.field_150349_c` | grass block | air-above-grass anchor test | OSW:1310 |

All flag-2 writes (iron bars keep unconnected shape until a neighbor update, as in
1.7.10 — `piece.place` preserves this).

---

## 6. Footprint extents + suggested DungeonType

Relative to `(cposx, cposy, cposz)` (= the grass block on the worldgen path):

| Axis | Min | Max | Extent | Determined by |
|---|---|---|---|---|
| X | `−4` | `+4` | **9** | shell `i = −4..4` (GD:3643); gable m=4 row reaches ±4 (GD:3691) |
| Y | `0` | `+9` | **10** | floor `k = 0` (GD:3648) / gable apex `y = +9` (GD:3691-3704). **Nothing below y 0** — no foundation |
| Z | `−4` | `+4` | **9** | shell `j = −4..4` (GD:3644) |

Suggested entry (asymmetric 6-int ctor `(xMin, xMax, down, up, zMin, zMax, mode)`, +1
margin; down = 1 covers the origin course sitting in terrain):

```java
DAMSEL_IN_DISTRESS(-5, 5, 1, 10, -5, 5, PlacementMode.VILLAGE_GRASS_SURFACE),
```

## 7. Placement — **NEEDS_NEW_MODE** (`VILLAGE_GRASS_SURFACE` proposed)

No existing mode matches. The scan (quoted in full in §1.1, OSW:1305-1315) is the
Spit-Bug/Leaf-Monster shape (4 attempts of `chunk + nextInt(16)` jitter, Y 100→41,
air directly above grass) **with two differences**:

1. **Anchor offset −1**: the builder receives `posY − 1` — the GRASS BLOCK, not the
   air above it (OSW:1311). `SWAMP_GRASS_SURFACE` anchors at the air block
   (LDS:249-267); no grass-surface mode anchors at surface−0... i.e. the block itself.
   (The −1-style anchor exists only in `OCEAN_SURFACE`, which is water-specific.)
2. **Clearance probe**: `quickSpaceCheck(posX, posY − 1, posZ)` — 12×12 all-air plane
   at anchor+4, offset −2..+9 on both axes (OSW:2625-2633). `SWAMP_GRASS_SURFACE`
   has no clearance test.

Proposed new mode (pattern §1 step 4 sanctions adding one — this is a covered
decision, not NEEDS_DESIGN_RULING): `VILLAGE_GRASS_SURFACE` =
`swampGrassSurfaceOrigin` (LDS:249-267: window 41..100 via `getBaseHeight`, dry-column
grass approximation) **minus one on Y** (return `firstFree − 1`), plus the existing
`footprintClearAbove` approximation (LDS:275-292) for `quickSpaceCheck` — the same
pairing `endSurfaceOrigin` already uses for the graveyard/hospital's identical probe
(LDS:181). Add the enum constant + one case in `findGenerationPoint` (LDS:67-80).

JSON pair:

- `RES:worldgen/structure/damsel_in_distress.json` — `"type":
  "orespawn:legacy_dungeon"`, `"dungeon_type": "DAMSEL_IN_DISTRESS"`,
  `"biomes": "orespawn:village_biome"` (inline, `dim_village.json` precedent — the
  dimension is single-biome, §1.2), `"step": "surface_structures"`,
  `"terrain_adaptation": "none"`, `"spawn_overrides": {}`.
- No `has_structure` tag file needed (inline single biome).

## 8. Structure-set conversion

Effective odds: **1/250 per Village-dimension chunk** (OSW:1302) before scan success;
the `recently_placed == 0` pre-gate (OSW:121) and the 50-chunk cooldown it sets on
success (OSW:1312) map onto structure-set separation (C7 approximation, pattern §1
step 4).

C7 sqrt equivalence: spacing ≈ √250 ≈ 15.8 → **spacing 16, separation 8**.
Salt: **84363** (assigned to this task; verified free — grep of
`RES:worldgen/structure_set/*.json` shows OreSpawn salts 84301-84361 in use, highest
84361 cephadrome_altar; 84363 unused).

`RES:worldgen/structure_set/damsel_in_distress.json`: random_spread, spacing 16,
separation 8, salt 84363.

## 9. DungeonSpawnerBlock outcome

- Original: `if (type == 28)` → `makeDamselInDistress(world, clickedX, clickedY,
  clickedZ)` — one call, no offset, block read in full (DSB:137-139).
- Port: add `TYPE_DAMSEL_IN_DISTRESS = 28` (cite DSB:137-139) and
  `case TYPE_DAMSEL_IN_DISTRESS -> LegacyDungeonPiece.buildNow(server, pos,
  DungeonType.DAMSEL_IN_DISTRESS)` in `RandomDungeonSpawnerBlockEntity.buildForType`
  (wired today: 0/1/2/3/7/12/13/14/15/16/17/18/19/20/21/22/23/24/27/29/30/34/37/38/47,
  RDS:44-91 + RDS:145-261; **type 28 currently falls through to the generic-dungeon
  default**, RDS:262).
- The DSB path bypasses the grass scan and the −1 offset — the hut floor sits AT the
  clicked position rather than replacing a grass block, and the Girlfriend +
  Scorpion spawners fire wherever the player is (any dimension). Faithful behavior;
  `buildNow`'s live RNG covers the mossy rolls and yaw.

## 10. Mid-build world READS — classified

1. **Tile-entity fetches** (GD:3713/3718 spawners via `getSpawnerTileEntity`, GD:3723
   chest via `getChestTileEntity`) — SELF-reads of blocks written the line before;
   absorbed by `piece.placeSpawner` / `piece.placeLootChest`. No deviation decision
   needed.
2. `world.field_72995_K` (GD:3640) — a side check, not a block read; dropped (§12 S11).
3. **No terrain reads at all** — the builder writes unconditionally everywhere
   (contrast the graveyard/leaf-monster foundation probes). Nothing to model, no
   `terrainStateIfInChunk` use, no flags.

## 11. RNG stream

Draws inside the builder, in order:

1. **Loop A** (GD:3657): one `nextInt(8)` per cell whose `bid` is cobble at the roll
   point — i.e. every floor/wall cell INCLUDING the 9 doorway cells (the door carve
   runs AFTER the roll, GD:3660-3663, so those draws happen and are then discarded).
   The condition depends only on loop indices — deterministic, identical every pass.
2. **Loop B** (GD:3673): 7×8 = 56 draws. **Loop C** (GD:3683): 5×7 = 35 draws.
3. **Loop D** (GD:3694/3699): 2 draws per inner iteration = 2×(5+4+3+2+1) = 30 draws
   (the x=0 cell of each row is written twice with separate draws — keep both).
4. Chest fill count `10 + nextInt(5)` (GD:3725) → moves into loot-JSON `rolls 10-14`
   (pattern §1 step 3 rule 3).
5. Girlfriend yaw `nextFloat() * 360` (GD:3730) → drawn UNCONDITIONALLY by the ported
   generator, passed to `piece.spawnEntity` (§4.1).

Port the geometry draws 1-3 verbatim on the piece `RandomSource` in source order
(including the discarded doorway draws and the double x=0 gable draws) — every draw is
gated only on loop indices, so all passes consume the identical sequence; only the
writes are chunk-gated (inside `piece.place`). Loop E (bars) and the
spawner/chest/entity placements draw nothing except items 4-5 above. Original mixed
sources (`world.rand` for everything in the builder; the chunk `random` only in the
add-method gate/jitter) collapse into structure-set frequency as usual.

## 12. Surprises / MISSING-IN-PORT

- **S1 (MISSING-IN-PORT)**: `makeDamselInDistress` has no counterpart anywhere in
  `src/main/` (grep `Damsel|DAMSEL` over `java/` + `resources/`: zero matches). No
  DungeonType, no generator, no JSON pair, no loot table; DSB type 28 falls through to
  the generic-dungeon default (RDS:262).
- **S2**: The structure's point is the **jail cell**: the iron-bar wall (§2.5) fully
  partitions the hut; the chest AND the live Girlfriend are behind the bars, the two
  Scorpion spawners guard the front room. There is no opening into the cell — the
  player must break bars. Do not add a door.
- **S3**: Dead locals — `stuffdir` (init 0 GD:3631, set 2 GD:3639) and the `meta = 0` re-assignment
  (GD:3668) are never consumed; the `boolean which` in the add method (OSW:1308)
  likewise. Ignore all three.
- **S4**: Draw-then-carve order in loop A: the 9 doorway cells consume mossy rolls
  before being forced to air (GD:3657-3663). Positional-only condition → pass-safe,
  but transcribe the order so the draw count matches the original distribution.
- **S5**: The gable overwrites the front rows of both roof courses with fresh rolls
  (§2.4), and writes its center column twice per row. Faithful; replicate write order,
  do not dedupe.
- **S6**: Asymmetric roof bounds — course 1 `j ≤ length−1`, course 2 `i ≤ width−2 &&
  j ≤ length−2` (GD:3669-3670, 3679-3680): the roof is biased toward −z, and the strip
  directly above the back wall (`z = +4, y = 5`) plus the side-wall tops at y 5 are
  never written. The interior is still fully sealed (interior z ≤ +3 all covered).
  Faithful; do not square it up.
- **S7**: **Anchor is the grass block itself** — the builder is called with
  `posY − 1` (OSW:1311), so the floor REPLACES the grass and the hut sits flush with
  the terrain (contrast Leaf Monster/Spit Bug's one-block-raised anchor). This −1 plus
  the `quickSpaceCheck` clearance is why no existing PlacementMode fits → NEEDS_NEW_MODE
  (§7).
- **S8**: The chest is placed with **meta 2 = facing north** (GD:3722) — the method's
  only nonzero-meta write. Use the `placeLootChest` facing overload (LDP:474) with
  `Direction.NORTH`; single chest, no ChestType concern.
- **S9**: Girlfriend spawn quirks to preserve exactly (§4.1): integer corner
  coordinates (no +0.5), caller-drawn random yaw, pitch 0, no NBT, no finalize/spawn
  rules, **no persistence flag** — persistence comes from the class's
  canDespawn/removeWhenFarAway override in both versions. Use `piece.spawnEntity`,
  not `spawnPersistent`.
- **S10**: `DamselContentsList` is shared with `makeGirlfriendIsland` (GD:4968) —
  ~~point both structures at ONE `chests/damsel_in_distress.json`; only the `rolls`
  differ~~ **SUPERSEDED by the batch-3 ruling (see §3): rolls is fixed per table
  file, so each structure ships its own table — `chests/damsel_in_distress.json`
  rolls 10-14 vs `chests/girlfriend_island.json` rolls 4-8, entries identical
  (GD:3725 vs GD:5021/5026), `_shared_list` keys cross-naming the twins.**
- **S11**: The client-side guard (GD:3640-3642) has no port equivalent or need —
  postProcess and `buildNow` run server-side only. Documented drop.
- **S12**: All three port foods exist with matching names (blt_sandwich / salad /
  corn_dog, ModItems.java:520/544/546) — no MISSING-IN-PORT items in the loot list;
  the only naming delta is corndog_cooked → corn_dog.
