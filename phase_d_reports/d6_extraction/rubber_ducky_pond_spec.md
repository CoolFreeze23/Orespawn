# Rubber Ducky Pond — Extraction Spec (1.7.10 → NeoForge 1.21.1)

Source of truth: `reference_1_7_10_source/sources/danger/orespawn/GenericDungeon.java` —
`makeRubberDuckyPond` (GD:5383-5421; previous method `makeStinkyHouse` ends at GD:5381,
next method `makeWhiteHouse` at GD:5423). Method read IN FULL, including the shared
helpers it uses (`FastSetBlock` GD:187-189, `getSpawnerTileEntity` GD:86-95,
`getChestTileEntity` GD:75-84). All coordinates are relative to the build origin
`(cposx, cposy, cposz)` = the three int args. No method-local size constants — every
bound is a literal.

Citation convention: `GD:NN` = GenericDungeon.java line NN, `OSW:NN` = OreSpawnWorld.java,
`OSM:NN` = OreSpawnMain.java, `DSB:NN` = DungeonSpawnerBlock.java, `RDS:NN` =
`src/main/java/danger/orespawn/block/entity/RandomDungeonSpawnerBlockEntity.java`,
`LDP:NN` = `src/main/java/danger/orespawn/world/structure/LegacyDungeonPiece.java`,
`LDS:NN` = `src/main/java/danger/orespawn/world/structure/LegacyDungeonStructure.java`.

Shared plumbing / header traps:
- `FastSetBlock(world, x, y, z, block)` (GD:187-189) → flag-2 chunk write, no neighbor
  updates → port `piece.place(x, y, z, state)` (structure_conversion_pattern.md §1
  step 3 table). Used ONLY by the pond loop (GD:5415-5418); the tower writes use
  `func_147465_d` directly — flag 2 for spawners/chests/glass, **flag 3 for the four
  water blocks** (GD:5405, 5407-5408; §12 S5).
- Spawners: `func_147465_d(..., field_150474_ac, 0, 2)` + `getSpawnerTileEntity` +
  `func_98272_a(name)` → `piece.placeSpawner(x, y, z, type)`.
- Chests: TWO placed, ONE filled (GD:5396-5401) — the shipped double-chest treatment
  (PlayPoolGenerator / LeafMonsterDungeonGenerator.java:272-279) applies verbatim (§12 S3).
- **Zero world reads in the builder** — no `func_147439_a`/`func_147437_c` anywhere in
  GD:5383-5421; the only reads are the spawner/chest TE fetches (self-reads absorbed by
  the helpers). §10.
- **One RNG draw in the builder** — the chest fill count `8 + nextInt(5)` (GD:5400),
  which moves into the loot JSON's rolls. The ported generator consumes ZERO draws —
  trivially stitching-safe (§11).
- The old pre-audit copy at `src/danger/orespawn/GenericDungeon.java:5400` mirrors the
  same method and is not shipped code.

---

## 1. Entry points — EVERY call site

Grep of the whole original tree for `makeRubberDuckyPond`: exactly two call sites
(OSW:1229 and DSB:174; GD:5383 is the definition; the pre-audit `src/danger/` copies
at DungeonSpawnerBlock.java:184 / OreSpawnWorld.java:1247 mirror the same and are not
shipped code).

| Caller | Signature used | Coords passed | Notes |
|---|---|---|---|
| `OreSpawnWorld.addRubberDuckyPond` (OSW:1229) | `OreSpawnMain.MyDungeon.makeRubberDuckyPond(world, posX, posY, posZ)` | scan hit, **no Y offset — posY is the AIR block directly above grass** (§1.1) | worldgen path, vanilla overworld Plains only (§1.2) |
| `DungeonSpawnerBlock` type **40** (DSB:173-175) | `...makeRubberDuckyPond(world, clickedX, clickedY, clickedZ)` | player-placed block pos, no offset | roll `type = world.field_73012_v.nextInt(50)` (DSB:52), fires 400 ticks after placement (DSB:39), self + block above deleted first (DSB:50-51). The `if (type == 40)` block read IN FULL: **single builder call, nothing else** (DSB:173-175) — not a two-builder index; not one of the `clickedY + 1` outliers (those are 43/44/45, DSB:182-190). |

### 1.1 `addRubberDuckyPond` — FULL method + return contract (OSW:1217-1236)

```java
// OSW:1217-1236
public boolean addRubberDuckyPond(World world, Random random, int chunkX, int chunkZ) {
    if (random.nextInt(275) != 0) {
        return false;
    }
    BiomeGenBase b = world.func_72807_a(chunkX, chunkZ);
    if (b.field_76791_y.equals("Plains")) {
        for (int i = 0; i < 4; ++i) {
            int posX = chunkX + random.nextInt(16);
            int posZ = chunkZ + random.nextInt(16);
            boolean which = false;
            for (int posY = 100; posY > 40; --posY) {
                if (world.func_147439_a(posX, posY, posZ) != Blocks.field_150350_a
                    || world.func_147439_a(posX, posY - 1, posZ) != Blocks.field_150349_c) continue;
                OreSpawnMain.MyDungeon.makeRubberDuckyPond(world, posX, posY, posZ);
                recently_placed = 50;
                return true;
            }
        }
    }
    return false;
}
```

1. Gate: `random.nextInt(275) != 0 → return false` (OSW:1218-1220) — 1/275, drawn from
   the chunk-provided `random` BEFORE the biome check. **Same gate value as
   `addLeafMonster` (OSW:1197).**
2. Biome: `world.func_72807_a(chunkX, chunkZ)` (chunk-corner block coords) must have
   `biomeName` **exactly `"Plains"`** (OSW:1221-1222) — excludes Sunflower Plains and
   every other biome; else fall through to `return false`.
3. Up to 4 attempts (OSW:1223): `posX = chunkX + random.nextInt(16)`,
   `posZ = chunkZ + random.nextInt(16)` (OSW:1224-1225). (Dead local
   `boolean which = false`, OSW:1226 — ignore.)
4. Column scan `posY = 100` down to `41` inclusive (`posY > 40`, OSW:1227): require
   `block(posX, posY, posZ) == air` AND `block(posX, posY-1, posZ) ==
   Blocks.field_150349_c` (**grass block directly below**) (OSW:1228).
5. Hit → `makeRubberDuckyPond(world, posX, posY, posZ)`, `recently_placed = 50`,
   `return true` (OSW:1229-1231).
6. All attempts miss → `return false` (OSW:1235).

**FULL return contract: `true` ONLY on an actual placement** (which also sets the
50-chunk global cooldown). Gate fail, biome fail, and scan fail all return `false`.
No WGEN-062-style early-true quirk. This is the LAST link of the fall-through chain —
its return value is assigned to `ahh` and then discarded (nothing follows it,
OSW:319-321), so there is no downstream suppression to model. Line-for-line the same
shape as `addLeafMonster` (OSW:1196-1215) and `addSpitBug` (OSW:1238-1257), the
`SWAMP_GRASS_SURFACE` reference users.

### 1.2 Chain position (OSW:274-328)

The whole block is gated by `OreSpawnMain.DisableOverworldDungeons == 0 &&
world.field_73011_w.field_76574_g == 0 && recently_placed == 0` (OSW:284) —
vanilla-overworld exclusive, config-disableable. The independent 6-way pool roll
(OSW:285-303) precedes the fall-through chain; then `addANest` (1/230) →
`addHauntedHouse` (1/285) → `addLeafMonster` (1/275) → `addSpitBug` (1/190,
OSW:1239) → `addIgloo` (1/220, OSW:1260) → `addBouncyCastle` (1/230, OSW:1281) →
**`addRubberDuckyPond` (OSW:319-321)**, each link running only when every earlier
link returned false. Total suppression by the six earlier links is
≤ 1/230 + 1/285 + 1/275 + 1/190 + 1/220 + 1/230 ≈ 2.6% before their own biome/scan
filters — negligible; the `recently_placed` coupling maps onto structure-set
separation per the C7-approved approximation (pattern §1 step 4).

---

## 2. Geometry — rule by rule (all ranges inclusive)

Write order is source order and is behavior: **spawners → chests → glass → water →
pond** (the pond loop runs LAST and clears the two air layers underneath the floating
tower). Everything is written at `cposy + 0` and above — nothing at or below `cposy − 1`
(the terrain grass at `cposy − 1` is the pond's floor; §12 S6).

### 2.1 Spawner pair (GD:5390-5395)

`i = 0..1`: `func_147465_d(cposx + i, cposy + 6, cposz, field_150474_ac, 0, 2)`
(GD:5391), TE fetch (GD:5392, null-guard GD:5393), `func_98272_a("Rubber Ducky")`
(GD:5394).

| # | Position (rel) | Mob name string | Cite |
|---|---|---|---|
| 1 | `(0, +6, 0)` | `"Rubber Ducky"` | GD:5390-5394 |
| 2 | `(1, +6, 0)` | `"Rubber Ducky"` | GD:5390-5394 |

### 2.2 Chest pair — TWO placed, ONE filled (GD:5396-5401)

- Chest at `(0, +5, 0)` (GD:5396) — placed, **never fetched, never filled**.
- Chest at `(1, +5, 0)` (GD:5397) — TE fetched (GD:5398), filled with
  `8 + world.field_73012_v.nextInt(5)` = **8-12 pulls** of `RubberDuckyContentsList`
  (GD:5399-5401).

Both flag-2 writes. 1.7.10 auto-merged the adjacent pair into one double chest; port
treatment in §12 S3.

### 2.3 Glass pair (GD:5402-5403)

Glass **block** (`field_150359_w`, not panes) at `(0, +4, 0)` and `(1, +4, 0)`, flag 2.

### 2.4 Water sources — the method's only flag-3 writes (GD:5404-5408)

- `i = 0..1`: still water (`field_150355_j`, meta 0) at `(i, +3, 0)`, **flags 3**
  (GD:5404-5406).
- Flowing water (`field_150358_i`, meta 0) at `(−1, +3, 0)` and `(+2, +3, 0)`,
  **flags 3** (GD:5407-5408).

All four flatten to the modern `minecraft:water` source state (§5, §12 S4); flag-3
notified neighbors so the cascade started immediately (§12 S5). Net: a 4-wide water
line at `y+3` under the glass/chest/spawner totem, pouring down into the pond.

### 2.5 Pond loop (GD:5409-5420)

`i = 0..11` (outer, X), `k = 0..10` (inner, Z), all via `FastSetBlock` (flag 2), at
`(cposx + i − 5, ..., cposz + k − 5)` — rel X `−5..+6`, rel Z `−5..+5`. Per cell, in
source order:

| # | Rule | Cell(s) | Block | Cite |
|---|---|---|---|---|
| 1 | default | interior `(−4..+5, +0, −4..+4)` (10×9) | still water | GD:5411 |
| 2 | perimeter `i==0 \|\| k==0 \|\| i==11 \|\| k==10` | ring at rel X ±5/+6 edge, Z ±5 edge | sand | GD:5412-5414 |
| 3 | write at `cposy + 0` | whole 12×11 layer | (the above) | GD:5415 |
| 4 | `bid = air` then write at `+1` and `+2` | whole 12×11 footprint, 2 layers | air | GD:5416-5418 |

The `+1`/`+2` air layers clear terrain over the whole pond AND pass under the floating
tower (the tower's lowest course is `+3` — nothing of it is erased). The tower columns
`(0..1, 0)` are interior pond cells (`i = 5..6, k = 5`), so the anchor column itself
ends up water at rel 0.

Net shape: a 12×11 one-block-thick pond pad sitting ON the Plains grass — sand
perimeter ring, 10×9 still-water interior — with a floating two-column totem centered
over it at `x 0..1, z 0`: water sources at `+3` (widened to 4 by the two flowing-water
caps at `x −1/+2`), glass at `+4`, chests at `+5` (loot in the `+1` half), and two
"Rubber Ducky" spawners at `+6`. Water pours from under the glass 2 blocks down onto
the pond surface.

---

## 3. Loot — FULL transcription

`RubberDuckyContentsList` (GD:27) — constructor semantics `(item, meta=0, minStack,
maxStack, weight)`. **13 entries, every weight 35, total weight = 455.** Fill count:
`8 + nextInt(5)` (GD:5400) → `pools[0].rolls` uniform **min 8, max 12**. Grep of the
original tree: this list is consumed ONLY here (GD:5389/5400) — one (list, fill
formula) pair → ONE table (pattern §1 step 5 as amended).

| # | 1.7.10 item | Modern / port mapping (cite) | min | max | weight |
|---|---|---|---|---|---|
| 1 | `OreSpawnMain.MyDeadStinkBug` | `ModItems.DEAD_STINK_BUG` "dead_stink_bug" (ModItems.java:269; orig "deadstinkbug" OSM:1951) | 4 | 10 | 35 |
| 2 | `OreSpawnMain.MyFireFish` | `ModItems.FIRE_FISH` "fire_fish" (ModItems.java:486; orig "firefish" OSM:1709) | 4 | 10 | 35 |
| 3 | `OreSpawnMain.MySunFish` | `ModItems.SUN_FISH` "sun_fish" (ModItems.java:489; orig "sunfish" OSM:1710) | 4 | 10 | 35 |
| 4 | `OreSpawnMain.MySparkFish` | `ModItems.SPARK_FISH` "spark_fish" (ModItems.java:495; orig "sparkfish" OSM:1730) | 4 | 10 | 35 |
| 5 | `OreSpawnMain.MyGreenFish` | `ModItems.GREEN_FISH` "green_fish" (ModItems.java:498; orig "greenfish" OSM:1752) | 4 | 10 | 35 |
| 6 | `OreSpawnMain.MyBlueFish` | `ModItems.BLUE_FISH` "blue_fish" (ModItems.java:500; orig "bluefish" OSM:1753) | 4 | 10 | 35 |
| 7 | `OreSpawnMain.MyPinkFish` | `ModItems.PINK_FISH` "pink_fish" (ModItems.java:502; orig "pinkfish" OSM:1754) | 4 | 10 | 35 |
| 8 | `OreSpawnMain.MyRockFish` | `ModItems.ROCK_FISH` "rock_fish" (ModItems.java:504; orig "rockfish" OSM:1755) | 4 | 10 | 35 |
| 9 | `OreSpawnMain.MyWoodFish` | `ModItems.WOOD_FISH` "wood_fish" (ModItems.java:506; orig "woodfish" OSM:1756) | 4 | 10 | 35 |
| 10 | `OreSpawnMain.MyGreyFish` | `ModItems.GREY_FISH` "grey_fish" (ModItems.java:508; orig "greyfish" OSM:1757) | 4 | 10 | 35 |
| 11 | `OreSpawnMain.RubberDuckyEgg` | `ModItems.RUBBER_DUCKY_SPAWN_EGG` "rubber_ducky_spawn_egg" (ModItems.java:1084; orig "eggrubberducky" OSM:5622; cross-proof: port EasterBunny.java:213 maps orig RubberDuckyEgg → the same item) | 4 | 10 | 35 |
| 12 | `OreSpawnMain.MyPeacockFeather` | `ModItems.PEACOCK_FEATHER` "peacock_feather" (ModItems.java:255; orig "peacockfeather" OSM:1718) | 4 | 10 | 35 |
| 13 | `Items.field_151008_G` | `minecraft:feather` | 6 | 16 | 35 |

→ `RES:loot_table/chests/rubber_ducky_pond.json`, rolls uniform 8-12, one entry per
row, `set_count` uniform per row. Bound to the `(1, +5, 0)` chest only; the `(0, +5,
0)` chest stays empty. Documented approximation (pattern §1 step 5): original pulls
landed in random chest slots with overwrite collisions; a loot pool never collides.

---

## 4. Spawner / mob mapping table

| Spawner name | 1.7.10 registration | Port EntityType (cite) |
|---|---|---|
| `"Rubber Ducky"` (×2, GD:5394) | `RubberDucky.class` — `registerGlobalEntityID(..., "Rubber Ducky", RubberDuckyID)` OSM:4393, `registerModEntity` OSM:4397 | `ModEntities.ENTITY_RUBBER_DUCKY` "rubber_ducky" (ModEntities.java:476-478) |

**No direct entity spawns** — spawner blocks only (no yaw/NBT/persistence extraction
needed; `spawnPersistent` concerns do not arise here).

Spawn-gate compatibility: orig `RubberDucky.func_70601_bi` (RubberDucky.java:508-526,
read in full) scans X/Z offsets `−3..+2` and Y offsets `0..+4` around the would-spawn
position for a mob spawner whose entity name equals `"Rubber Ducky"` → immediate
`true`; otherwise requires `y >= 50` AND daytime. The port preserves this exactly:
`EntityRubberDucky.checkSpawnRules` (EntityRubberDucky.java:382-389) =
`OriginalSpawnGates.nearOwnSpawner` bypass, then `y >= 50`, then daytime. The Plains
anchor window (Y 41..100, §1.1 step 4) is partly below 50 — **the spawner bypass is
what makes the pond function at low altitudes**; noted so nobody "fixes" it (§12 S8). Ducks that
spawn beside the totem fall into the pond, as in 1.7.10.

---

## 5. Block palette

| 1.7.10 field (meta) | Modern block | Used for | Cite |
|---|---|---|---|
| `Blocks.field_150474_ac` | `minecraft:spawner` | 2 spawners | GD:5391 |
| `Blocks.field_150486_ae` | `minecraft:chest` (default state, two singles — §12 S3) | chest pair | GD:5396-5397 |
| `Blocks.field_150359_w` | `minecraft:glass` (full block, NOT panes) | 2 glass blocks | GD:5402-5403 |
| `Blocks.field_150355_j` meta 0 | `minecraft:water` (source) | totem water line + pond interior | GD:5405, 5411 |
| `Blocks.field_150358_i` meta 0 | `minecraft:water` (source) — the 1.13 flattening maps both water blocks at meta 0 to the source state (play_pool_spec.md §6 precedent, shipped in PlayPoolGenerator.java:92-100) | 2 flowing-water caps | GD:5407-5408 |
| `Blocks.field_150354_m` | `minecraft:sand` | pond perimeter ring | GD:5412-5414 |
| `Blocks.field_150350_a` | `minecraft:air` | +1/+2 clearing layers | GD:5416-5418 |
| (placement scan only) `Blocks.field_150349_c` | grass block | air-above-grass anchor test | OSW:1228 |

---

## 6. Footprint extents (relative to origin) + suggested DungeonType

| Axis | Min | Max | Extent | Determined by |
|---|---|---|---|---|
| X | `−5` | `+6` | **12** | pond `i − 5`, `i = 0..11` (GD:5409, 5415); totem reaches only `−1..+2` |
| Y | `+0` | `+6` | **7** | pond layer at `cposy` (GD:5415) / spawners at `+6` (GD:5391) — **nothing below origin level** |
| Z | `−5` | `+5` | **11** | pond `k − 5`, `k = 0..10` (GD:5410, 5415); totem all at `z = 0` |

Suggested entry (asymmetric 6-int ctor `(xMin, xMax, down, up, zMin, zMax, mode)`, +1
margin; `down = 1` follows the no-dig convention of MINI_DUNGEON/CEPHADROME_ALTAR/
STINKY_HOUSE, LDP:192/194/208):

```java
RUBBER_DUCKY_POND(-6, 7, 1, 7, -6, 6, PlacementMode.SWAMP_GRASS_SURFACE),
```

## 7. Placement — **existing mode fits: `SWAMP_GRASS_SURFACE`** (no new mode needed)

`addRubberDuckyPond`'s anchoring (§1.1, quoted in full there) is line-for-line
identical to `addSpitBug`'s (OSW:1238-1257) and `addLeafMonster`'s (OSW:1196-1215),
which `SWAMP_GRASS_SURFACE` already ports (`swampGrassSurfaceOrigin`, LDS:254-284):
up to 4 attempts of `chunk + nextInt(16)` jitter (LDS:268-270 ← OSW:1223-1225), Y
100→41 hard window (LDS:275 ← OSW:1227), air directly above a grass block approximated
as "dry column" (WORLD_SURFACE == OCEAN_FLOOR, LDS:276-280 — documented mapping delta
on the mode), anchor at the AIR block with no offset (LDS:281 ← OSW:1229). The mode is
biome-agnostic — the Plains part lives entirely in this structure's JSON, exactly as
the shipped Leaf Monster precedent (LDP:187-190). The leaf monster spec already
anticipated this reuse ("later Igloo/BouncyCastle/RubberDuckyPond candidates share the
same scan shape", leaf_monster_dungeon_spec.md §7); worth appending to the mode's
Javadoc when wiring: `addRubberDuckyPond` (OSW:1217-1236) is another exact user.

JSON trio (copy the `leaf_monster_dungeon` structure/structure_set/tag JSONs and
rename — same anchor mode, same biome treatment; CORRECTED by the verification pass:
leaf_monster_dungeon uses a biome TAG, not an inline biome — its structure JSON reads
`"biomes": "#orespawn:has_structure/leaf_monster_dungeon"` and the tag file contains
exactly `["minecraft:plains"]`, matching LDP:187-190's own "biome via tag" note and
the pattern §1 step 4 template; the inline single-biome shape ALSO ships —
monster_island `"minecraft:ocean"`, bouncy_castle `"minecraft:desert"` — and is
functionally identical, but this spec follows its declared twin):

- `RES:worldgen/structure/rubber_ducky_pond.json` —
  `"type": "orespawn:legacy_dungeon"`, `"dungeon_type": "RUBBER_DUCKY_POND"`,
  `"biomes": "#orespawn:has_structure/rubber_ducky_pond"`,
  `"step": "surface_structures"`, `"terrain_adaptation": "none"`,
  `"spawn_overrides": {}`.
- `RES:worldgen/structure_set/rubber_ducky_pond.json` — §8.
- `RES:tags/worldgen/biome/has_structure/rubber_ducky_pond.json` — values
  `["minecraft:plains"]` ONLY (the original's exact-name `"Plains"` check, OSW:1222,
  excludes Sunflower Plains and meadows; leaf_monster_dungeon tag precedent).

## 8. Structure-set conversion

Effective odds ≈ **1/275 per vanilla-overworld chunk** (OSW:1218) before the Plains
corner-biome filter and scan (biome selectivity is carried by the biome field; the
≈2.7% suppression from the six earlier chain links and the
`recently_placed`/pool-roll couplings are absorbed per the C7 approximation, pattern
§1 step 4).

C7 sqrt equivalence: spacing ≈ √275 ≈ 16.6 → **spacing 17, separation 8** — identical
arithmetic to leaf_monster_dungeon (same 1/275 gate; its set is 17/8, salt 84359).
Distinct salts keep the two Plains grids independent, mirroring the original's
independent per-chunk gates.

Salt: **84371** (assigned to this task; verified free — grep of
`RES:worldgen/structure_set/*.json` shows OreSpawn salts in use 84301-84367
(highest: rainbow 84367) plus the vanilla-style 10387399 on dim_villages; sibling
in-flight D6 specs claim 84368 spider_hangout, 84369 red_ant_hangout, 84370
frog_pond, 84372 haunted_house, 84373/84374 ender_knight — 84371 collides with
nothing).

`RES:worldgen/structure_set/rubber_ducky_pond.json`: random_spread, spacing 17,
separation 8, salt 84371.

## 9. DungeonSpawnerBlock outcome

- Original: `if (type == 40)` → `makeRubberDuckyPond(world, clickedX, clickedY,
  clickedZ)` — one call, no offset, block read in full (DSB:173-175).
- Port: add `TYPE_RUBBER_DUCKY_POND = 40` (cite DSB:173-175) and
  `case TYPE_RUBBER_DUCKY_POND -> LegacyDungeonPiece.buildNow(server, pos,
  DungeonType.RUBBER_DUCKY_POND)` in `RandomDungeonSpawnerBlockEntity.buildForType`
  (wired types today: 0/1/2/3/7/12-24/26-30/34/35/37/38/39/44/46/47 — constants
  RDS:44-103, dispatch RDS:157-306; **type 40 currently falls through to the
  generic-dungeon `default` arm, RDS:306**).
- The DSB path bypasses the biome/grass scan entirely — a pond built at the clicked
  position in any dimension, floating or embedded, is faithful behavior. `buildNow`
  (LDP:481-486) keeps live RNG and an unclipped write window. On this path the
  modern water sources fluid-tick immediately (§12 S5) — a floating pond drains
  where 1.7.10's flag-2 pond water stayed metastable until a block update; PARITY
  note, same classification as Play Pool S3.

## 10. Mid-build world READS — classified

1. **None.** The builder contains no `func_147439_a`/`func_147437_c` call
   (GD:5383-5421 read in full) — no foundation probe, no terrain conditionals; every
   write is decided purely by loop indices. Nothing needs the in-memory-model
   treatment and nothing needs `terrainStateIfInChunk`.
2. **Tile-entity fetches** (GD:5392 spawners; GD:5398 chest) — self-reads of blocks
   written the line(s) before; absorbed by `piece.placeSpawner` /
   `piece.placeLootChest`. No deviation decision needed.
3. The placement-scan reads (OSW:1227-1228) live outside the builder and map into
   `SWAMP_GRASS_SURFACE` + the structure set (§7-8).

No NEEDS_DESIGN_RULING condition arises; every player-visible mapping choice below is
covered by a shipped precedent (§12 S3-S5).

## 11. RNG stream accounting

**One draw total in the builder** — the chest fill count `8 + nextInt(5)`
(GD:5400, `world.field_73012_v`), which moves to loot-JSON `rolls 8-12`; the pulls
themselves (item picks, slots, stack sizes) were `world.field_73012_v` draws inside
`func_76293_a` and also live in the JSON now. The ported generator therefore consumes
**zero** random draws; every per-chunk replay pass is trivially identical and the
stitching contract (pattern §1 step 3) is satisfied vacuously — same shape as the
shipped Leaf Monster generator.

Dispatch-layer rolls, all collapsing as usual: OSW:1218 gate `nextInt(275)` +
OSW:1224-1225 jitter on the chunk `random` → structure-set frequency +
`swampGrassSurfaceOrigin`; DSB:52 `nextInt(50)` on `world.field_73012_v` → the DSB
roll. No mixed-stream quirks beyond those (no `Math.random()` anywhere).

## 12. Surprises / MISSING-IN-PORT (court S-items)

- **S1 (MISSING-IN-PORT)**: `makeRubberDuckyPond` has no counterpart anywhere in
  `src/main/` (grep `Ducky`/`ducky`: only entity/item/spawn plumbing — the entity +
  renderer/model/attributes, spawn egg + spawn block items with their models/recipe/
  lang/loot, the caged item, OriginalSpawnGates/config, biome-modifier spawn JSONs,
  the SpawnOresPoolFeature block-pool row (:177), and rubber_ducky_spawn_egg ENTRIES
  inside two unrelated chest tables (challenge_tower_level5.json:1118,
  generic_dungeon.json:1008) — no structure/DungeonType/DSB case) and no
  `rubber_ducky_pond` JSON exists (grep `rubber_ducky_pond|RUBBER_DUCKY_POND`:
  zero hits). The worldgen chain link and DSB type 40 both fall through today
  (type 40 → generic dungeon via the RDS default arm, RDS:306).
- **S2 — write order is behavior**: tower first (spawners `+6` → chests `+5` → glass
  `+4` → water `+3`), pond LAST — the pond loop's `+1`/`+2` air layers sweep under
  the already-built tower and clear the terrain below it (GD:5409-5420). Preserve
  source order verbatim; do not "optimize" into bottom-up placement.
- **S3 — two chests, one filled** (GD:5396-5401): only the `(1, +5, 0)` chest is
  TE-fetched and loot-bound; the `(0, +5, 0)` chest is deliberately empty. 1.7.10
  auto-merged the adjacent pair into a double chest; modern default-state chests stay
  singles. Shipped precedent applies verbatim (PlayPoolGenerator class Javadoc
  "Double-chest cosmetics"; LeafMonsterDungeonGenerator.java:272-279):
  `piece.placeLootChest(cx + 1, cy + 5, cz, LOOT)` +
  `piece.place(cx, cy + 5, cz, Blocks.CHEST.defaultBlockState())` — accepted cosmetic
  deviation, do not "fix"/fill both. (Note the filled half here is the SECOND-placed
  `+1` chest, same as Play Pool's `x+1`.)
- **S4 — flowing_water meta 0 → water source**: the two `field_150358_i` caps
  (GD:5407-5408) flatten — like the meta-0 still-water writes — to the
  `minecraft:water` source state, so all four `+3` blocks are sources in the port
  (play_pool_spec.md §6 + S3, shipped in PlayPoolGenerator.java:92-100). The pond
  cascade widens by one block each side exactly as in 1.7.10.
- **S5 — flag-3 water writes** (GD:5405/5407-5408, the method's only non-flag-2
  writes): the original notified neighbors to start the cascade. `piece.place`
  writes flag 2 only, but modern `LiquidBlock.onPlace` self-schedules a fluid tick,
  so the cascade still starts — PARITY note, non-behavioral (play_pool S3 precedent).
  Flip side: the POND water (flag 2 in the original, GD:5415) also fluid-ticks in the
  port, so on uneven terrain edge cells overhanging a slope can drain where 1.7.10's
  stayed metastable until a block update — same PARITY classification, worldgen
  placement is unaffected (the anchor guarantees grass under the anchor column and
  Plains terrain is near-flat).
- **S6 — the pond is PERCHED, not dug**: the anchor is the AIR block above grass
  (OSW:1228-1229, no offset), and the pond layer is written AT rel 0 — a
  one-block-high sand-and-water pad sitting ON the surface. Nothing is written at or
  below `cposy − 1`: no basin, no floor — the pre-existing terrain (grass at the
  anchor column) is the pond bottom. `down = 1` in the ctor; do not excavate.
- **S7 — the totem floats with no support**: water/glass/chests/spawners hang
  `+3..+6` above the pond with air at `+1..+2` (cleared by the pond loop) and no
  column connecting them to the ground. Flag-2 writes keep it floating; do not add
  supports. Likewise the sand ring never falls at build time (no neighbor updates) —
  a later player update collapsing an overhanging sand edge is faithful.
- **S8 — spawn-gate interplay** (§4): orig `RubberDucky.func_70601_bi`
  (RubberDucky.java:508-526) grants a blanket bypass within a 6×6×5 box around a
  `"Rubber Ducky"`-named spawner, else `y >= 50` + daytime; the port mirrors it
  (EntityRubberDucky.java:382-389). Required for ponds anchored at Y 41..49 (the
  anchor window is 41..100, §1.1 step 4); do not "fix" the low-altitude spawners.
- **S9 — frequency twin of Leaf Monster**: same 1/275 gate, same exact-"Plains"
  biome, same scan (OSW:1196-1236 side by side). Two independent structure sets at
  spacing 17/8 with distinct salts (84359 vs 84371) reproduce the originals'
  independent rolls; no cross-structure coupling to model beyond the standard C7
  separation treatment.
- **S10 — glass is the full block** (`field_150359_w`, GD:5402-5403), not
  `field_150410_aZ` panes (contrast Stinky House windows) — no connection-state
  concerns; `Blocks.GLASS.defaultBlockState()`.
