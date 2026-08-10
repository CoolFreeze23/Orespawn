# Bouncy Castle — Extraction Spec (1.7.10 → NeoForge 1.21.1)

Source of truth: `reference_1_7_10_source/sources/danger/orespawn/GenericDungeon.java` —
`makeBouncyCastle` (GD:3106-3205, next method `makeEnderCastle` at GD:3207). All
coordinates are relative to the build origin `(cposx, cposy, cposz)` = the three int
args. Method read IN FULL, including the helpers it uses (`getSpawnerTileEntity`
GD:86-95, `getChestTileEntity` GD:75-84; block writes go through
`OreSpawnMain.setBlockFast(..., meta, 2)` directly, GD:3151).

Citation convention: `GD:NN` = GenericDungeon.java line NN, `OSW:NN` = OreSpawnWorld.java,
`OSM:NN` = OreSpawnMain.java, `DSB:NN` = DungeonSpawnerBlock.java, `RDS:NN` =
`src/main/java/danger/orespawn/block/entity/RandomDungeonSpawnerBlockEntity.java`,
`LDP:NN` = `src/main/java/danger/orespawn/world/structure/LegacyDungeonPiece.java`,
`LDS:NN` = `src/main/java/danger/orespawn/world/structure/LegacyDungeonStructure.java`.

Method-local constants: `length = 4` (GD:3118), `width = 4` (GD:3119), `height = 5`
(GD:3120). Dead locals declared and never meaningfully used: `deltax/deltaz`
(GD:3107-3108, `deltax = true` GD:3121), `dirx/dirz` (GD:3111-3112), `stuffdir = 2`
(GD:3113/3122), the copies `x/z/y = cposx/cposz/cposy` (GD:3123-3125) — ignore all.

Shared plumbing:
- Block loop writes: `OreSpawnMain.setBlockFast(world, x, y, z, bid, meta, 2)`
  (GD:3151) — flag-2 chunk write, no neighbor updates → port
  `piece.place(x, y, z, state)` (structure_conversion_pattern.md §1 step 3 table).
- Spawners: `world.func_147465_d(..., field_150474_ac, 0, 2)` then
  `getSpawnerTileEntity` + `func_98272_a(name)` → `piece.placeSpawner(x, y, z, type)`.
- Chest: `func_147465_d(..., field_150486_ae, 2, 2)` (**meta 2 = facing north**) then
  `getChestTileEntity` + `WeightedRandomChestContent.func_76293_a` →
  `piece.placeLootChest(x, y, z, Direction.NORTH, lootKey)` (facing overload exists,
  LDP:474) + loot JSON.
- RNG inside the builder: exactly ONE draw — the chest fill count
  `6 + world.field_73012_v.nextInt(5)` (GD:3203), which moves into the loot JSON's
  `rolls`. The ported generator body is RNG-free — trivially stitching-safe.
- World READS inside the builder: only the ten tile-entity fetches after
  spawner/chest writes (§9) — self-reads absorbed by the piece helpers. **No terrain
  reads at all** (no `func_147439_a`/`func_147437_c` anywhere in GD:3106-3205).
- Client guard `if (world.field_72995_K) return;` (GD:3126-3128) — server-side no-op
  guard; both port paths (structure postProcess, DSB `buildNow`) are server-only by
  construction, nothing to port.

---

## 1. Entry points — EVERY call site

Grep of the whole original tree for `makeBouncyCastle`: exactly two call sites
(OSW:1292 and DSB:132; GD:3106 is the definition; the old pre-audit copies under
`src/danger/orespawn/` mirror the same and are not shipped code).

| Caller | Signature used | Coords passed | Notes |
|---|---|---|---|
| `OreSpawnWorld.addBouncyCastle` (OSW:1292) | `OreSpawnMain.MyDungeon.makeBouncyCastle(world, posX, posY - 1, posZ)` | scan hit, **Y offset −1** — posY is the AIR block above sand, so cposy = the top SAND block itself | worldgen path, vanilla overworld exact-"Desert" only (§1.1) |
| `DungeonSpawnerBlock` type **26** (DSB:131-133) | `...makeBouncyCastle(world, clickedX, clickedY, clickedZ)` | player-placed block pos, **no offset** | roll `type = world.field_73012_v.nextInt(50)` (DSB:52), fires 400 ticks after placement (DSB:39), self + block above deleted first (DSB:50-51). The `if (type == 26)` block read IN FULL: **single builder call, nothing else** (DSB:131-133) — not a two-builder index (neighbors: 25 = CrystalHauntedHouse DSB:128-130, 27 = EnderCastle DSB:134-136). |

### 1.1 `addBouncyCastle` — FULL method + return contract (OSW:1280-1299)

```java
// OSW:1280-1299
public boolean addBouncyCastle(World world, Random random, int chunkX, int chunkZ) {
    if (random.nextInt(230) != 0) {
        return false;
    }
    BiomeGenBase b = world.func_72807_a(chunkX, chunkZ);
    if (b.field_76791_y.equals("Desert")) {
        for (int i = 0; i < 4; ++i) {
            int posX = chunkX + random.nextInt(16);
            int posZ = chunkZ + random.nextInt(16);
            boolean which = false;
            for (int posY = 100; posY > 40; --posY) {
                if (world.func_147439_a(posX, posY, posZ) != Blocks.field_150350_a || world.func_147439_a(posX, posY - 1, posZ) != Blocks.field_150354_m) continue;
                OreSpawnMain.MyDungeon.makeBouncyCastle(world, posX, posY - 1, posZ);
                recently_placed = 50;
                return true;
            }
        }
    }
    return false;
}
```

1. Gate: `random.nextInt(230) != 0 → return false` (OSW:1281-1283) — 1/230, drawn from
   the chunk-provided `random` BEFORE the biome check.
2. Biome: `world.func_72807_a(chunkX, chunkZ)` (chunk-corner block coords) must have
   `biomeName` **exactly `"Desert"`** (OSW:1284-1285) — excludes 1.7.10 "DesertHills";
   else fall through to `return false`.
3. Up to 4 attempts (OSW:1286): `posX/posZ = chunk + random.nextInt(16)`
   (OSW:1287-1288). (Dead local `boolean which = false`, OSW:1289 — ignore.)
4. Column scan `posY = 100` down to `41` inclusive (`posY > 40`, OSW:1290): require
   `block(posX, posY, posZ) == air` AND `block(posX, posY-1, posZ) ==
   Blocks.field_150354_m` (**sand directly below**) (OSW:1291).
5. Hit → `makeBouncyCastle(world, posX, posY - 1, posZ)` — **anchor is the sand block,
   NOT the air above it** — then `recently_placed = 50`, `return true` (OSW:1292-1294).
6. Biome mismatch or all attempts miss → `return false` (OSW:1298).

**FULL return contract: `true` ONLY on an actual placement** (which also sets the
50-chunk global cooldown). Gate fail, biome fail, and scan fail all return `false`,
letting the chunk's chain continue to `addRubberDuckyPond`. No addFairyTree-style
early-true quirk (WGEN-062).

### 1.2 Chain position — the exact `ahh`-chain link and its gates (OSW:284-322)

The whole block is gated by `OreSpawnMain.DisableOverworldDungeons == 0 &&
world.field_73011_w.field_76574_g == 0 && recently_placed == 0` (OSW:284) —
vanilla-overworld exclusive, config-disableable, skipped entirely while the global
cooldown counts down. Inside it, the independent 6-way pool roll
`world.field_73012_v.nextInt(6)` (OSW:285-303: PlayPool/WaterDragonLair/GoldFishBowl/
GirlfriendIsland/MonsterIsland/FrogPond) runs FIRST and does not touch `ahh`; then the
fall-through chain:

```java
// OSW:304-321
if (!(ahh = this.addANest(world, random, chunkX, chunkZ))) {
    ahh = this.addHauntedHouse(world, random, chunkX, chunkZ);
}
if (!ahh) { ahh = this.addLeafMonster(world, random, chunkX, chunkZ); }
if (!ahh) { ahh = this.addSpitBug(world, random, chunkX, chunkZ); }
if (!ahh) { ahh = this.addIgloo(world, random, chunkX, chunkZ); }
if (!ahh) {
    ahh = this.addBouncyCastle(world, random, chunkX, chunkZ);   // OSW:316-318
}
if (!ahh) { ahh = this.addRubberDuckyPond(world, random, chunkX, chunkZ); }
```

`addBouncyCastle` is link **6 of 7** — it runs only when addANest (1/230 gate,
OSW:999-1000), addHauntedHouse (1/285, OSW:979-980), addLeafMonster (1/275, OSW:1197),
addSpitBug (1/190, OSW:1238), and addIgloo (1/220, OSW:1260) ALL returned false; its
own success suppresses only addRubberDuckyPond. Upstream suppression ≈ 1/230 + 1/285 +
1/275 + 1/190 + 1/220 ≈ 2.1% before those links' own biome/scan filters — negligible;
the `recently_placed` coupling maps onto structure-set separation per the C7-approved
approximation (pattern §1 step 4).

---

## 2. Geometry — per-loop tables (all ranges inclusive)

ONE triple loop builds the entire shell (GD:3129-3154), then 9 spawners + 1 chest are
overlaid. Loop ranges: `i = −4..+4` (X, `-width..width`), `j = −4..+4` (Z,
`-length..length`), `k = 0..+4` (Y, `k < height`). Per cell the rules run in source
order, later assignments overwriting earlier ones:

| # | Rule (source order) | Cells | Result | Cite |
|---|---|---|---|---|
| 1 | default | all | air, meta 0 | GD:3132-3133 |
| 2 | `k == 4 \|\| k == 0` | ceiling + floor slabs | Lavafoam | GD:3134-3136 |
| 3 | `i == ±4` | east/west walls (full height) | Lavafoam | GD:3137-3139 |
| 4 | `j == ±4` | north/south walls (full height) | Lavafoam | GD:3140-3142 |
| 5 | `(i == ±4) && (j == ±4)` (decompiled `!(i != -width && i != width \|\| j != -length && j != length)`) | the 4 corner columns, full height `k = 0..4` | stained clay **meta 14 = red** | GD:3143-3146 |
| 6 | `(k == 1 \|\| k == 2) && i == 0 && j == −4` | door: `(0, +1..+2, −4)` | air (overwrites rule 4's wall) | GD:3147-3150 |

Write: `setBlockFast(cposx+i, cposy+k, cposz+j, bid, meta, 2)` (GD:3151) —
**unconditional over the whole 9×9×5 box**, so terrain inside the box (dune bumps,
cacti) is cleared to air and the top sand layer under the box is replaced by the
Lavafoam floor. Nothing outside the box is touched — a castle half-embedded in a dune
side is faithful.

Net shell: a 9×9 footprint, 5-tall hollow box; Lavafoam floor (`y+0`), ceiling
(`y+4`), and perimeter walls; four full-height red-stained-clay corner pillars; a
1-wide 2-tall doorway at `(0, +1..+2, −4)` in the −z (north) wall. Interior air:
`(−3..+3, +1..+3, −3..+3)`. Because the worldgen anchor is the top sand block (§1.1),
the floor sits flush with the surrounding desert surface and the doorway is walk-in
level.

### 2.1 Spawners — 9 total, three clusters of 3, all at `y+3` (GD:3155-3199)

Each: `func_147465_d(..., field_150474_ac, 0, 2)` + `func_98272_a(name)`. All float at
`k = 3` (one below the ceiling), one block inside a wall (`width−1 = 3`,
`length−1 = 3`). Every cell was loop interior air — no overwrite of walls. Each
cluster repeats the same Silverfish/Rat/Scorpion trio:

| # | Position (rel) | Mob name string | Cite |
|---|---|---|---|
| 1 | `(−1, +3, +3)` | `"Silverfish"` | GD:3155-3159 |
| 2 | `(0, +3, +3)` | `"Rat"` | GD:3160-3164 |
| 3 | `(+1, +3, +3)` | `"Scorpion"` | GD:3165-3169 |
| 4 | `(+3, +3, −1)` | `"Silverfish"` | GD:3170-3174 |
| 5 | `(+3, +3, 0)` | `"Rat"` | GD:3175-3179 |
| 6 | `(+3, +3, +1)` | `"Scorpion"` | GD:3180-3184 |
| 7 | `(−3, +3, −1)` | `"Silverfish"` | GD:3185-3189 |
| 8 | `(−3, +3, 0)` | `"Rat"` | GD:3190-3194 |
| 9 | `(−3, +3, +1)` | `"Scorpion"` | GD:3195-3199 |

Clusters: south wall interior (`z = +3`, x −1..+1), east wall interior (`x = +3`,
z −1..+1), west wall interior (`x = −3`, z −1..+1). The north (door) wall has none.

### 2.2 Chest (GD:3200-3204)

ONE chest at `(+3, +3, +3)` — the interior corner cell diagonal from the door, floating
at `y+3` like the spawners — placed with **meta 2 = facing north** (GD:3200). Not
adjacent to any spawner (gaps at `(+2, +3, +3)` and `(+3, +3, +2)`), so it stays a
single chest. Fill: `WeightedRandomChestContent.func_76293_a(world.field_73012_v,
BouncyContentsList, chest, 6 + world.field_73012_v.nextInt(5))` (GD:3203) → **6-10
weighted pulls** into random slots (collisions overwrite — documented approximation,
pattern §1 step 5).

---

## 3. Loot — FULL transcription

`BouncyContentsList` (GD:41) — constructor semantics `(item, meta=0, minStack,
maxStack, weight)`. **7 entries, total weight = 180** (35 + 5×25 + 20).
`pools[0].rolls`: uniform **min 6, max 10** (from `6 + nextInt(5)`, GD:3203). One
chest → one loot table JSON.

| # | 1.7.10 item | Modern / port mapping | min | max | weight |
|---|---|---|---|---|---|
| 1 | `Items.field_151078_bh` | `minecraft:rotten_flesh` | 6 | 16 | 35 |
| 2 | `Items.field_151115_aP` (raw fish, meta 0) | `minecraft:cod` | 6 | 16 | 25 |
| 3 | `Items.field_151103_aS` | `minecraft:bone` | 6 | 16 | 25 |
| 4 | `Items.field_151007_F` | `minecraft:string` | 6 | 16 | 25 |
| 5 | `Item.func_150898_a(Blocks.field_150328_O)` (red flower, meta 0) | `minecraft:poppy` | 6 | 16 | 25 |
| 6 | `Item.func_150898_a(Blocks.field_150327_N)` (yellow flower) | `minecraft:dandelion` | 6 | 16 | 25 |
| 7 | `Items.field_151079_bi` | `minecraft:ender_pearl` | 2 | 4 | 20 |

→ `RES:loot_table/chests/bouncy_castle.json`, rolls uniform 6-10, one entry per row,
`set_count` uniform per the min/max columns. All seven items are vanilla — no
port-item lookups needed. (Rows 5/6 are the same poppy/dandelion mapping the graveyard
list used, graveyard spec §3.)

---

## 4. Spawner / mob mapping table

| Spawner name | 1.7.10 class (registration) | Port EntityType (cite) |
|---|---|---|
| `"Silverfish"` (×3, GD:3158/3173/3188) | **vanilla** EntitySilverfish (1.7.10 mob name "Silverfish") | `EntityType.SILVERFISH` (`minecraft:silverfish`) |
| `"Rat"` (×3, GD:3163/3178/3193) | `Rat` — `registerGlobalEntityID(Rat.class, "Rat", RatID)` OSM:4197, `registerModEntity` OSM:4201 | `ModEntities.ENTITY_RAT` "rat" (ModEntities.java:250-252) |
| `"Scorpion"` (×3, GD:3168/3183/3198) | `Scorpion` — `registerGlobalEntityID(Scorpion.class, "Scorpion", ScorpionID)` OSM:3791, `registerModEntity` OSM:3795 | `ModEntities.ENTITY_SCORPION` "scorpion" (ModEntities.java:258-260) |

**No direct entity spawns** — spawner blocks only (no `spawnEntityInWorld` anywhere in
GD:3106-3205; no yaw/NBT/persistence handling to extract).

---

## 5. Block palette

| 1.7.10 field (meta) | Modern block | Used for | Cite |
|---|---|---|---|
| `Blocks.field_150350_a` | `minecraft:air` | interior + doorway | GD:3132, 3149 |
| `OreSpawnMain.MyLavafoamBlock` (registered OSM:1610, "lavafoam") | **`ModBlocks.LAVAFOAM`** `orespawn:lavafoam` (ModBlocks.java:63-65 — the bouncy block: friction 1.1, pushes/damages, ITEM-009) | floor, ceiling, all walls | GD:3135, 3138, 3141 |
| `Blocks.field_150406_ce` meta 14 | `minecraft:red_terracotta` (stained hardened clay, meta 14 = red) | 4 corner pillars | GD:3144-3145 |
| `Blocks.field_150474_ac` | `minecraft:spawner` | 9 spawners | GD:3155/3160/3165/3170/3175/3180/3185/3190/3195 |
| `Blocks.field_150486_ae` meta 2 | `minecraft:chest[facing=north]` | 1 loot chest | GD:3200 |
| (placement scan only) `Blocks.field_150354_m` | sand | air-above-sand anchor test | OSW:1291 |

The Lavafoam walls/floor/ceiling are the structure's whole gimmick — the "bouncy" in
Bouncy Castle. The port block already implements the bounce/damage behavior.

---

## 6. Footprint extents (relative to origin) + suggested DungeonType

| Axis | Min | Max | Extent | Determined by |
|---|---|---|---|---|
| X | `−4` | `+4` | **9** | `i = -width..width`, width 4 (GD:3119, 3129) |
| Y | `0` | `+4` | **5** | `k = 0..height−1`, height 5 (GD:3120, 3131); nothing below y+0 |
| Z | `−4` | `+4` | **9** | `j = -length..length`, length 4 (GD:3118, 3130) |

Suggested entry (6-int ctor `(xMin, xMax, down, up, zMin, zMax, mode)`, +1 margin):

```java
BOUNCY_CASTLE(-5, 5, 1, 5, -5, 5, PlacementMode.SAND_SURFACE_MINUS1),  // NEEDS_NEW_MODE — §7
```

## 7. Placement — **NEEDS_NEW_MODE** (`SAND_SURFACE_MINUS1`), scan quoted

The scan (§1.1, quoted in full there) is the exact `addLeafMonster`/`addSpitBug` shape
that `SWAMP_GRASS_SURFACE` ports (`swampGrassSurfaceOrigin`, LDS:249-267): 1-in-N
gate, exact-name biome check, up to 4 attempts of `chunk + nextInt(16)` jitter,
Y 100→41 window, air directly above a specific surface block — **but the anchor is
`posY − 1`, the surface block itself** (OSW:1292), where `SWAMP_GRASS_SURFACE` anchors
at the air block (`firstFree`). No existing mode anchors at `firstFree − 1`
(SURFACE_CENTER: chunk-center, no jitter/window, anchors at firstFree, LDS:96-106;
all others are ocean/end/sky/islands shapes). Original scan lines for the record:

```java
// OSW:1290-1292
for (int posY = 100; posY > 40; --posY) {
    if (world.func_147439_a(posX, posY, posZ) != Blocks.field_150350_a
        || world.func_147439_a(posX, posY - 1, posZ) != Blocks.field_150354_m) continue;
    OreSpawnMain.MyDungeon.makeBouncyCastle(world, posX, posY - 1, posZ);
```

Suggested new mode `SAND_SURFACE_MINUS1` (pattern §1 step 4 explicitly covers adding
modes — no NEEDS_DESIGN_RULING condition arises): clone `swampGrassSurfaceOrigin` —
4 jitter attempts, hard `41 ≤ firstFree ≤ 100` window (behavior, trap 7), dry column
(`WORLD_SURFACE_WG == OCEAN_FLOOR_WG`, the established block-below approximation; sand
identity itself is carried by the desert biome tag exactly as grass was by swamp) —
and return `new BlockPos(x, firstFree - 1, z)`. This is the igloo-spec precedent
(igloo §7 proposed `SNOW_SURFACE_MINUS2` the same way), but WITHOUT igloo's
NEEDS_DESIGN_RULING — igloo's blocker was its snow-biome-border frequency/biome
question (LDP:176-181 comment); Desert here is a clean exact-name inline biome map
with direct precedent (leaf monster "Plains", spit bug "Swampland", monster island
"Ocean"). Documented delta: 1.7.10 "Desert" excluded "DesertHills"; modern 1.21.1
merged desert hills into `minecraft:desert` (1.18), so coverage is slightly wider —
no separate modern biome exists to exclude.

JSON pair (copy the `leaf_monster_dungeon` trio-of-two and rename — same chain, same
inline-biome shape):

- `RES:worldgen/structure/bouncy_castle.json` — `"type": "orespawn:legacy_dungeon"`,
  `"dungeon_type": "BOUNCY_CASTLE"`, `"biomes": "minecraft:desert"` (inline vanilla
  biome, monster_island/leaf_monster precedent — no tag file), `"step":
  "surface_structures"`, `"terrain_adaptation": "none"`, `"spawn_overrides": {}`.
- `RES:worldgen/structure_set/bouncy_castle.json` — §8.

## 8. Structure-set conversion

Effective odds ≈ **1/230 per vanilla-overworld chunk** (OSW:1281) before the Desert
corner-biome filter and scan (biome selectivity carried by the `biomes` field; the
≈2.1% suppression from the five earlier chain links and the `recently_placed`/pool-roll
couplings are absorbed per the C7 approximation, pattern §1 step 4).

C7 sqrt equivalence: spacing ≈ √230 ≈ 15.2 → **spacing 15, separation 7**.
Salt: **84362** (assigned per task; verified free — grep of
`RES:worldgen/structure_set/*.json` at verification date shows the 843xx dungeon-salt
block topping out at 84361 (cephadrome_altar); the only salt outside that block is
`dim_villages.json`'s 10387399; all OreSpawn salts currently unique. Sibling d6 specs
claim 84350-84361 and 84363-84367 — 84362 collides with none; 84353/84355 were
returned unconsumed by urchin_spawner/rotator_station and remain free).

`RES:worldgen/structure_set/bouncy_castle.json`: random_spread, spacing 15,
separation 7, salt 84362.

## 9. Mid-build world READS classified

1. **Spawner TE fetches (GD:3156, 3161, 3166, 3171, 3176, 3181, 3186, 3191, 3196)** —
   self-reads at positions written the line before; absorbed by `piece.placeSpawner`.
2. **Chest TE fetch (GD:3201)** — self-read; absorbed by `piece.placeLootChest`.
3. **There are no other reads.** No `func_147439_a`/`func_147437_c` call exists in
   GD:3106-3205 — no terrain probe, no foundation loop, no in-memory model needed, no
   sanctioned-helper (`terrainStateIfInChunk`) use, nothing to flag. (`field_72995_K`
   at GD:3126 is a side check, not a block read.)

## 10. RNG stream

**One draw total** — the chest fill count (GD:3203), which moves to loot-JSON
`rolls 6-10` (the pulls themselves were `world.field_73012_v` draws inside
`func_76293_a` and also live in the JSON now). The ported generator consumes **zero**
random draws; every per-chunk replay pass is trivially identical — geometry, all 9
spawner positions and names, and the chest are constants. (Dispatch-layer rolls —
OSW:1281 gate + OSW:1287-1288 jitter on the chunk `random`, DSB:52 on
`world.field_73012_v` — map to structure-set placement / the DSB roll as usual.)

## 11. DungeonSpawnerBlock outcome

- Original: `if (type == 26)` → `makeBouncyCastle(world, clickedX, clickedY,
  clickedZ)` — one call, no offset, block read in full (DSB:131-133).
- Port: add constant `TYPE_BOUNCY_CASTLE = 26` (cite DSB:131-133) and
  `case TYPE_BOUNCY_CASTLE -> LegacyDungeonPiece.buildNow(server, pos,
  DungeonType.BOUNCY_CASTLE)` in `RandomDungeonSpawnerBlockEntity.buildForType`
  (wired types today: 0/1/2/3/7/12/13/14/15/16/17/18/19/20/21/22/23/24/27/29/30/34/
  37/38/47, RDS:44-91 + RDS:145-260; **type 26 currently falls through to the
  generic-dungeon `default` arm**, RDS:262).
- The DSB path bypasses the biome/sand scan AND the −1 anchor offset — the castle
  builds with its floor AT the clicked position (one higher relative to ground than
  the worldgen form), floating or embedded wherever the spawner block sat. Faithful
  behavior.

## 12. Surprises / MISSING-IN-PORT

- **S1 (MISSING-IN-PORT)**: `makeBouncyCastle` has no counterpart anywhere in
  `src/main/java/` (grep `Bouncy|BOUNCY` over src/: zero matches outside the old
  unshipped `src/danger/` copy). No DungeonType, no generator, no JSON pair, no loot
  table; worldgen link OSW:316-318 and DSB type 26 both fall through today.
- **S2 (anchor −1 — the one placement quirk)**: worldgen calls the builder at
  `posY − 1`, the top SAND block (OSW:1292) — unlike the leaf monster/spit bug
  siblings (anchor at the air block). The floor therefore REPLACES the surface sand
  layer and sits flush with the desert floor; the doorway (`y+1..+2`) is walk-in
  level. This is why no existing PlacementMode fits → NEEDS_NEW_MODE
  `SAND_SURFACE_MINUS1` (§7).
- **S3**: The whole shell is **Lavafoam** (`orespawn:lavafoam`, OSM:1610 →
  ModBlocks.LAVAFOAM), the mod's bouncy/damaging block — floor, ceiling, and walls.
  The building is literally a bouncy castle; do not substitute wool/clay.
- **S4**: Corner pillars are stained clay meta 14 = **red terracotta**, full height
  including floor and ceiling corners (rule 5 runs after rules 2-4 and overwrites
  them, GD:3143-3146).
- **S5**: All 9 spawners AND the chest float at `y+3`, one block below the ceiling —
  flag-2 writes, no support needed (same floating-tile pattern as the original;
  `piece.place`/helpers preserve it). The chest faces north (meta 2, GD:3200), toward
  the door wall; use the `placeLootChest` facing overload (LDP:474).
- **S6**: Spawner trio repeats per cluster — Silverfish (vanilla!), Rat, Scorpion —
  on the south, east, and west interior walls; the door (north) wall has none. A
  vanilla-mob spawner ("Silverfish") in an OreSpawn structure is faithful — map to
  `EntityType.SILVERFISH`, do not swap in an OreSpawn mob.
- **S7**: The box write is UNCONDITIONAL over 9×9×5 (GD:3151 runs for every cell) —
  terrain inside becomes air, the floor replaces sand, and nothing outside the box is
  cleared. No foundation skirt, no yard clearing: on a dune slope the castle
  half-buries; over a hollow it overhangs on nothing. Do not add either.
- **S8**: Builder-level dead code: `deltax/deltaz/dirx/dirz/stuffdir/x/y/z`
  (GD:3107-3125) and the scan's `boolean which` (OSW:1289) — ignore all. The client
  guard (GD:3126-3128) has no port equivalent needed.
- **S9**: `addBouncyCastle`'s return contract is the plain one — `true` only on
  placement (sets `recently_placed = 50`, suppressing only addRubberDuckyPond in its
  own chunk); `false` otherwise. No WGEN-062-style early-true coupling.
- **S10**: Chest fill is 6-10 pulls (GD:3203) — smaller than the leaf monster's 12-16;
  ender pearls are the only entry with a non-6-16 stack range (2-4, weight 20).
  Total weight 180 — verify against the JSON in step-7 audit.
