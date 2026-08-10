# Leaf Monster Dungeon — Extraction Spec (1.7.10 → NeoForge 1.21.1)

Source of truth: `reference_1_7_10_source/sources/danger/orespawn/GenericDungeon.java` —
`makeLeafMonsterDungeon` (GD:2093-2227, next method `makeMiniDungeon` at GD:2229). All
coordinates are relative to the build origin `(cposx, cposy, cposz)` = the three int args.
Method read IN FULL, including both helpers it uses (`FastSetBlock` GD:187-189,
`getSpawnerTileEntity` GD:86-95, `getChestTileEntity` GD:75-84).

Citation convention: `GD:NN` = GenericDungeon.java line NN, `OSW:NN` = OreSpawnWorld.java,
`OSM:NN` = OreSpawnMain.java, `DSB:NN` = DungeonSpawnerBlock.java. Port citations name
file + line.

Shared plumbing:
- `FastSetBlock(world, x, y, z, block)` (GD:187-189) = `OreSpawnMain.setBlockFast(...,
  meta 0, flags 2)` — flag-2 chunk write, no neighbor updates → port
  `piece.place(x, y, z, state)` (structure_conversion_pattern.md §1 step 3 table).
- Spawners: `world.func_147465_d(..., field_150474_ac, 0, 2)` then
  `getSpawnerTileEntity` + `func_98272_a(name)` → `piece.placeSpawner(x, y, z, type)`.
- Chest fill: `getChestTileEntity` + `WeightedRandomChestContent.func_76293_a` →
  `piece.placeLootChest(...)` + loot JSON.
- RNG inside the builder: exactly ONE draw — the chest fill count
  `12 + world.field_73012_v.nextInt(5)` (GD:2225), which moves into the loot JSON's
  `rolls`. The ported generator body is RNG-free — trivially stitching-safe.
- World READS inside the builder: (a) the foundation probe `func_147439_a` (GD:2113) —
  a genuine PRE-BUILD TERRAIN read, see §9; (b) the five tile-entity fetches after
  spawner/chest writes (GD:2202, 2207, 2212, 2217, 2223) — self-reads absorbed by the
  piece helpers.

---

## 1. Entry points — EVERY call site

Grep of the whole original tree for `makeLeafMonsterDungeon`: exactly two call sites
(OSW:1208 and DSB:99; GD:2093 is the definition).

| Caller | Signature used | Coords passed | Notes |
|---|---|---|---|
| `OreSpawnWorld.addLeafMonster` (OSW:1208) | `OreSpawnMain.MyDungeon.makeLeafMonsterDungeon(world, posX, posY, posZ)` | scan hit, **no Y offset** — posY is the AIR block directly above grass | worldgen path, vanilla overworld Plains only (§1.1) |
| `DungeonSpawnerBlock` type **15** (DSB:98-100) | `...makeLeafMonsterDungeon(world, clickedX, clickedY, clickedZ)` | player-placed block pos, no offset | roll `type = world.field_73012_v.nextInt(50)` (DSB:52), fires 400 ticks after placement (DSB:39), self + block above deleted first (DSB:50-51). The `if (type == 15)` block read IN FULL: **single builder call, nothing else** (DSB:98-100) — not a two-builder index. |

### 1.1 `addLeafMonster` — FULL method + return contract (OSW:1196-1215)

1. Gate: `random.nextInt(275) != 0 → return false` (OSW:1197-1199) — 1/275, drawn from
   the chunk-provided `random` BEFORE the biome check.
2. Biome: `world.func_72807_a(chunkX, chunkZ)` (chunk-corner block coords) must have
   `biomeName` **exactly `"Plains"`** (OSW:1200-1201) — excludes Sunflower Plains and
   every other biome; else fall through to `return false`.
3. Up to 4 attempts (OSW:1202): `posX = chunkX + random.nextInt(16)`,
   `posZ = chunkZ + random.nextInt(16)` (OSW:1203-1204). (A local
   `boolean which = false` is declared and never used, OSW:1205.)
4. Column scan `posY = 100` down to `41` inclusive (`posY > 40`, OSW:1206): require
   `block(posX, posY, posZ) == air` AND `block(posX, posY-1, posZ) ==
   Blocks.field_150349_c` (**grass block directly below**) (OSW:1207).
5. Hit → `makeLeafMonsterDungeon(world, posX, posY, posZ)`, `recently_placed = 50`,
   `return true` (OSW:1208-1210).
6. All attempts miss → `return false` (OSW:1214).

**Return contract: `true` ONLY on an actual placement** (which also sets the 50-chunk
global cooldown, OSW:30/37-38). No addFairyTree-style early-true quirk (WGEN-062) —
gate fail, biome fail, and scan fail all return `false`, letting the chunk's chain
continue to `addSpitBug` etc.

### 1.2 Chain position (OSW:284-322)

The whole block is gated by `OreSpawnMain.DisableOverworldDungeons == 0 &&
world.field_73011_w.field_76574_g == 0 && recently_placed == 0` (OSW:284) —
vanilla-overworld exclusive, config-disableable. The independent 6-way pool roll
(OSW:285-303) precedes the fall-through chain; then `addANest` (1/230 gate,
OSW:999-1000) → `addHauntedHouse` (1/285, OSW:979-980) → **`addLeafMonster`
(OSW:307-309)** → `addSpitBug` → `addIgloo` → `addBouncyCastle` → `addRubberDuckyPond`
(OSW:304-321), each link only when every earlier link returned false. Suppression by
the two earlier links is ≤ 1/230 + 1/285 ≈ 0.8% before their own biome/scan filters —
negligible; the `recently_placed` coupling maps onto structure-set separation per the
C7-approved approximation (pattern §1 step 4).

---

## 2. Geometry — per-loop tables (all ranges inclusive)

Write order matters (later writes overwrite earlier ones): clearing → foundation →
tower shell → ladders → awning → plateau → crown walls → roof rings → spawners →
chests.

### 2.1 Loop 1 — yard/base clearing (GD:2102-2108)

`i = −2..+5`, `k = −3..+1`, `j = 0..+3` → **air** (8×5×4 = 160 cells). Clears the
ground-level yard on the door (north) side and the tower's own front columns
(`k = 0..1`). Terrain south/around the tower above `k = +2` at these heights is NOT
cleared — a tower embedded in a hillside is faithful.

### 2.2 Loop 2 — foundation roots, terrain-conditional (GD:2109-2118)

`i = 0..3`, `k = 0..3`, `j = −1..−4` (`j > -5`): read
`bid = world.func_147439_a(cposx+i, cposy+j, cposz+k)` (GD:2113); **if `bid` is air OR
tallgrass (`field_150329_H`)** → oak log (GD:2114-2115); otherwise leave terrain
untouched. Fills log "roots" under the 4×4 tower footprint only where the ground
falls away (slope/hole/plant); solid terrain — including the grass block directly
under the anchor column — is never replaced. This is the method's only genuine world
read; classification and port treatment in §9.

### 2.3 Loop 3 — 4×4 tower shell, 10 tall (GD:2119-2133)

`i = 0..3`, `k = 0..3`, `j = 0..9`, default block **oak log**, with three carve rules
applied in source order:

| Rule (source order) | Cells | Result | Cite |
|---|---|---|---|
| `j < 2 && (k==0 \|\| k==1) && (i==1 \|\| i==2)` | door: `(1..2, 0..1, 0..1)` | air | GD:2123-2125 |
| `k == 1 && (i==1 \|\| i==2)` | shaft: `(1..2, 0..9, 1)` | air | GD:2126-2128 |
| `k == 2 && (i==1 \|\| i==2)` | ladder cells `(1..2, 0..9, 2)` | `continue` — **never written by this loop** | GD:2129 |

Net: solid log walls; a 2-wide 2-tall doorway in the north face (`z = 0`) continuing
one cell deep (`z = 1`); a 2×1 air shaft (`k = 1`) running the full height `0..9`
(its `j = 9` opening is the plateau exit hole); interior column `k = 2` reserved for
loop 4's ladders; the back interior `k = 3`? — no: `k = 3` is the south WALL (all
log). The 4×4 interior is only `i = 1..2, k = 1..2`.

### 2.4 Loop 4 — ladder wall (GD:2134-2142)

Same triple loop; only `k == 2 && (i==1 \|\| i==2)`, `j = 0..9`:
`world.func_147465_d(..., Blocks.field_150468_ap, 2, 3)` — **ladder, meta 2 = facing
north**, i.e. attached to the south log wall at `k = 3` (written by loop 3 BEFORE this
loop, so the support exists), climbable from the `k = 1` shaft. Note **flags = 3**,
the method's only non-flag-2 write — behaviorally a no-op here (the neighbor check
passes; final state identical), so the port's flag-2 `piece.place` matches (§12 S4).
2×10 = 20 ladders.

### 2.5 Door awning (GD:2143-2144)

Oak leaves at `(+1, +2, −1)` and `(+2, +2, −1)` — two leaf blocks floating over the
doorway in the cleared yard (each side-adjacent to the log front wall at `z = 0`).

### 2.6 Loop 5 — `j = +9` plateau ring (GD:2145-2155)

`i = −3..+6`, `k = −3..+6`, skip the tower core (`i, k ∈ 0..3` → `continue`,
GD:2148): outermost rim (`i == −3 || i == 6 || k == −3 || k == 6`) = **oak leaves**,
everything else = **oak log**. A 10×10 platform at `j = 9` surrounding the tower top
(the tower's own `j = 9` layer was already written by loop 3 — log except the 2×1
shaft hole and the two top ladder cells).

### 2.7 Loop 6 — crown-room walls, `j = +10..+12` (GD:2156-2166)

`i = −3..+6`, `k = −3..+6`, `j = 10..12`: rim (`i/k == −3/6`) = **oak leaves**,
interior = **air** (also clears any terrain inside the room). A 10×10 leaf-walled
room, 3 tall, floored by loop 5's platform.

### 2.8 Loop 7 — `j = +13` roof ring layer (GD:2167-2179)

`i = −2..+5`, `k = −2..+5`, default **air**; then `i == −2 || i == 5 || k == −2 ||
k == 5` → **oak log** (GD:2171-2173); then `i == −1 || i == 4 || k == −1 || k == 4` →
**oak leaves** (GD:2174-2176 — this second `if` runs AFTER and overwrites the log
choice). Result: 8×8 layer, outer log square, inner leaf square, 4×4 air center —
EXCEPT the 8 outer-ring cells that also sit on an inner-ring line
(`(−2,−1), (−2,4), (5,−1), (5,4), (−1,−2), (4,−2), (−1,5), (4,5)`), which end up
**leaves**, visibly breaking the log square near its corners (§12 S6 — faithful).

### 2.9 Loops 8-10 — roof cap (GD:2180-2200)

| Loop | Layer | Area | Block | Cite |
|---|---|---|---|---|
| 8 | `j = +14` | `i, k = −1..+4` (6×6) | oak leaves | GD:2180-2186 |
| 9 | `j = +15` | `i, k = 0..+3` (4×4) | oak log | GD:2187-2193 |
| 10 | `j = +16` | `i, k = +1..+2` (2×2) | oak leaves | GD:2194-2200 |

### 2.10 Spawners (GD:2201-2220)

Four spawners, each `func_147465_d(..., field_150474_ac, 0, 2)` +
`func_98272_a("Leaf Monster")`, at the four inner corners of the crown room, standing
on the loop-5 platform (each overwrites a loop-6 interior air cell):

| # | Position (rel) | Mob | Cite |
|---|---|---|---|
| 1 | `(−2, +10, −2)` | `"Leaf Monster"` | GD:2201-2205 |
| 2 | `(+5, +10, +5)` | `"Leaf Monster"` | GD:2206-2210 |
| 3 | `(−2, +10, +5)` | `"Leaf Monster"` | GD:2211-2215 |
| 4 | `(+5, +10, −2)` | `"Leaf Monster"` | GD:2216-2220 |

### 2.11 Chests (GD:2221-2226)

TWO chest blocks at `(+1, +10, +5)` and `(+2, +10, +5)` (GD:2221-2222) — adjacent
along X against the south crown wall, which 1.7.10 auto-unified into one double chest
(`BlockChest.func_149726_b` runs on `func_147465_d` regardless of flags). **Only the
`(+1)` half is filled** (GD:2223-2226):
`WeightedRandomChestContent.func_76293_a(world.field_73012_v, LeafMonsterContentsList,
chest, 12 + world.field_73012_v.nextInt(5))` → **12-16 weighted pulls** into random
slots of that half's 27-slot inventory (collisions overwrite — documented
approximation, pattern §1 step 5). The `(+2)` half receives nothing.

Net shape: a 4×4 oak-log lookout tower 10 tall with a north door, interior ladder
shaft to a 10×10 log platform at +9, a 3-tall leaf-walled crown room holding four
Leaf Monster spawners and a double loot chest, capped by a stepped log/leaf roof
(+13..+16), with terrain-conditional log roots below.

---

## 3. Loot — FULL transcription

`LeafMonsterContentsList` (GD:46) — constructor semantics `(item, meta=0, minStack,
maxStack, weight)`. **9 entries** (including two intentional duplicate pairs — keep
all 9), **total weight = 255** (4×35 + 4×25 + 15).
`pools[0].rolls`: uniform **min 12, max 16** (from `12 + nextInt(5)`, GD:2225).

| # | 1.7.10 item | Modern / port mapping (cite) | min | max | weight |
|---|---|---|---|---|---|
| 1 | `Items.field_151162_bE` | `minecraft:flower_pot` (identity proven by the port's ItemSifter case 5, src/main/java/danger/orespawn/item/ItemSifter.java:147 ← orig ItemSifter.java:371; do NOT copy the greenhouse fill's APPLE mis-mapping, §12 S8) | 6 | 16 | 35 |
| 2 | `Blocks.field_150345_g` (sapling, meta 0) | `minecraft:oak_sapling` | 6 | 16 | 35 |
| 3 | `Items.field_151162_bE` (duplicate of #1) | `minecraft:flower_pot` | 6 | 16 | 35 |
| 4 | `Blocks.field_150345_g` (duplicate of #2) | `minecraft:oak_sapling` | 6 | 16 | 35 |
| 5 | `Blocks.field_150362_t` (leaves, meta 0) | `minecraft:oak_leaves` | 6 | 16 | 25 |
| 6 | `Blocks.field_150346_d` | `minecraft:dirt` | 6 | 16 | 25 |
| 7 | `Blocks.field_150364_r` (log, meta 0) | `minecraft:oak_log` | 6 | 16 | 25 |
| 8 | `OreSpawnMain.MyPoisonSword` | port `ModItems.POISON_SWORD` "poison_sword" (ModItems.java:410) | 1 | 1 | 15 |
| 9 | `Items.field_151078_bh` | `minecraft:rotten_flesh` | 6 | 16 | 25 |

→ `RES:loot_table/chests/leaf_monster_dungeon.json`, rolls uniform 12-16, one entry
per row above (duplicates preserved as separate entries — same total-weight effect).
Bound to the `(+1, +10, +5)` chest half only.

---

## 4. Spawner / mob mapping table

| Spawner name | 1.7.10 registration | Port EntityType (cite) |
|---|---|---|
| `"Leaf Monster"` (×4, GD:2204/2209/2214/2219) | `LeafMonster.class` — `registerGlobalEntityID(..., "Leaf Monster", LeafMonsterID)` OSM:4111, `registerModEntity` OSM:4115 | `ModEntities.ENTITY_LEAF_MONSTER` "leaf_monster" (ModEntities.java:234-236) |

No direct entity spawns — spawner blocks only. (EntityLeafMonster's port spawn gate
already whitelists "Leaf Monster" spawner spawns, EntityLeafMonster.java:181-193.)

---

## 5. Block palette

| 1.7.10 field (meta) | Modern block | Used for | Cite |
|---|---|---|---|
| `Blocks.field_150350_a` | `minecraft:air` | yard clearing, door, shaft, room interiors | GD:2105, 2124, 2127, 2159, 2170 |
| `Blocks.field_150364_r` meta 0 | `minecraft:oak_log` (default axis=y) | walls, foundation roots, plateau, roof rings | GD:2112, 2122, 2149, 2172, 2190 |
| `Blocks.field_150362_t` meta 0 | `minecraft:oak_leaves` — **place with `PERSISTENT=true`** (adaptation, §12 S5) | awning, plateau rim, crown walls, roof | GD:2143-2144, 2151, 2161, 2175, 2183, 2197 |
| `Blocks.field_150468_ap` meta 2 | `minecraft:ladder[facing=north]` | 2×10 ladder wall | GD:2138-2139 |
| `Blocks.field_150329_H` | `minecraft:short_grass` / `minecraft:fern` (block-level compare — see §9) | foundation probe READ target only | GD:2114 |
| `Blocks.field_150474_ac` | `minecraft:spawner` | 4 spawners | GD:2201/2206/2211/2216 |
| `Blocks.field_150486_ae` | `minecraft:chest` | double loot chest | GD:2221-2222 |
| (placement scan only) `Blocks.field_150349_c` | grass block | air-above-grass anchor test | OSW:1207 |

---

## 6. Footprint extents (relative to origin) + suggested DungeonType

| Axis | Min | Max | Extent | Determined by |
|---|---|---|---|---|
| X | `−3` | `+6` | **10** | plateau/crown loops `i = −3..6` (GD:2145, 2156) |
| Y | `−4` | `+16` | **21** | foundation `j > −5` (GD:2111) / roof cap `j = 16` (GD:2196) |
| Z | `−3` | `+6` | **10** | plateau/crown loops `k = −3..6` (GD:2146, 2157); yard clears to `k = −3` (GD:2103), awning at `z = −1` |

Suggested entry (asymmetric 6-int ctor, +1 margin each side):

```java
LEAF_MONSTER_DUNGEON(-4, 7, 5, 17, -4, 7, PlacementMode.SWAMP_GRASS_SURFACE),
```

## 7. Placement — **existing mode fits: `SWAMP_GRASS_SURFACE`** (no new mode needed)

`addLeafMonster`'s scan (OSW:1202-1212) is line-for-line identical to
`addSpitBug`'s (OSW:1244-1254), which `SWAMP_GRASS_SURFACE` already ports
(LegacyDungeonStructure.swampGrassSurfaceOrigin, LegacyDungeonStructure.java:249-267):
up to 4 attempts of `chunk + nextInt(16)` jitter, Y 100→41 window, air directly above
a grass block, anchor at the air block (no offset). The original scan, quoted for the
record:

```java
// OSW:1202-1212
for (int i = 0; i < 4; ++i) {
    int posX = chunkX + random.nextInt(16);
    int posZ = chunkZ + random.nextInt(16);
    boolean which = false;
    for (int posY = 100; posY > 40; --posY) {
        if (world.func_147439_a(posX, posY, posZ) != Blocks.field_150350_a
            || world.func_147439_a(posX, posY - 1, posZ) != Blocks.field_150349_c) continue;
        OreSpawnMain.MyDungeon.makeLeafMonsterDungeon(world, posX, posY, posZ);
        recently_placed = 50;
        return true;
    }
}
```

The mode is biome-agnostic (the swamp part of Spit Bug's placement lives in that
structure's biome tag, not the mode); the grass-below test is already approximated as
"dry column inside Y 41..100" (documented mapping delta on the mode). The biome
difference is carried entirely by this structure's JSON. Suggested follow-up (comment
only, no behavior change): note on `swampGrassSurfaceOrigin` / the enum constant's
Javadoc that it also serves the Plains fall-through-chain structures
(LeafMonsterDungeon; later Igloo/BouncyCastle/RubberDuckyPond candidates share the
same scan shape).

JSON pair (copy the `spit_bug_lair` structure/structure_set JSONs and rename — same
anchor mode; but swap its tag `biomes` field for an inline biome, see below):

- `RES:worldgen/structure/leaf_monster_dungeon.json` —
  `"type": "orespawn:legacy_dungeon"`, `"dungeon_type": "LEAF_MONSTER_DUNGEON"`,
  `"biomes": "minecraft:plains"` (inline vanilla biome — the original's exact-name
  `"Plains"` check, OSW:1201, excludes Sunflower Plains and meadows; mirror the
  Monster Island exact-"Ocean" treatment), `"step": "surface_structures"`,
  `"terrain_adaptation": "none"`, `"spawn_overrides": {}`.
- `RES:worldgen/structure_set/leaf_monster_dungeon.json` — §8.
- No biome tag file needed (inline single-biome `"biomes"`, matching the
  monster_island precedent — monster_island.json uses `"minecraft:ocean"` inline.
  NOTE: spit_bug_lair itself uses a tag,
  `"#orespawn:has_structure/spit_bug_lair"` +
  `RES:tags/worldgen/biome/has_structure/spit_bug_lair.json` — so if the trio is
  copied from spit_bug_lair, the `biomes` field must be changed to the inline form,
  or a `has_structure/leaf_monster_dungeon.json` tag containing only
  `minecraft:plains` added instead; either is precedented, inline is less files).

## 8. Structure-set conversion

Effective odds ≈ **1/275 per vanilla-overworld chunk** (OSW:1197) before the Plains
corner-biome filter and scan (biome selectivity is carried by the biome field; the
≤0.8% suppression from the two earlier chain links and the `recently_placed`/pool-roll
couplings are absorbed per the C7 approximation, pattern §1 step 4).

C7 sqrt equivalence: spacing ≈ √275 ≈ 16.6 → **spacing 17, separation 8**.
Salt: **84359** (assigned per task; verified free — grep of
`RES:worldgen/structure_set/*.json` at verification date shows highest OreSpawn salt
in use = 84354 (spit_bug_lair); all current OreSpawn salts are unique — the 84312
mantis_nest/royal_trees collision logged in D5 has since been fixed, royal_trees now
uses 84332).

`RES:worldgen/structure_set/leaf_monster_dungeon.json`: random_spread, spacing 17,
separation 8, salt 84359.

## 9. Mid-build world READS classified

1. **Foundation probe (GD:2113) — PRE-BUILD TERRAIN read, the real one.**
   `func_147439_a` at `j = −1..−4` under the 4×4 footprint, cells nothing else in the
   method writes before (loop 1 stops at `j = 0`) — so the read is of untouched
   terrain, and its outcome affects ONLY the write at the very same cell. Port per the
   established royal-altar dirt-skirt precedent (LegacyDungeonPiece.java:1740-1747,
   the pipeline's one sanctioned terrain-read shape): gate the read with
   `inChunk(x, y, z)`, then `pLevel.getBlockState(...)`, then conditional
   `place(...)`. Each cell is visited exactly once — by the pass owning its chunk,
   where terrain already exists at postProcess time — and the loop draws **zero RNG**,
   so pass divergence cannot desynchronize anything (the stitching contract's rule 2
   exists to protect the RNG stream and cross-cell state; neither is involved here).
   Condition mapping: `bid == air || bid == field_150329_H` compares the BLOCK only
   (any meta: 1.7.10 tallgrass metas 0 shrub/1 grass/2 fern) →
   `state.isAir() || state.is(Blocks.SHORT_GRASS) || state.is(Blocks.FERN)`
   (meta-0 "shrub" has no distinct modern block; documented sub-delta).
2. **Spawner TE fetches (GD:2202, 2207, 2212, 2217)** — self-reads at positions just
   written; absorbed by `piece.placeSpawner`.
3. **Chest TE fetch (GD:2223)** — self-read; absorbed by `piece.placeLootChest`.

No other `func_147439_a`/`func_147438_o` call exists in GD:2093-2227.

## 10. RNG stream

**One draw total** — the chest fill count (GD:2225), which moves to loot-JSON
`rolls 12-16`; the pulls themselves (item picks, slots, stack sizes) were
`world.field_73012_v` draws inside `func_76293_a` and also live in the JSON now. The
ported generator therefore consumes **zero** random draws; every per-chunk replay pass
is trivially identical. (Dispatch-layer rolls — OSW:1197 gate + OSW:1203-1204 jitter
on the chunk `random`, DSB:52 on `world.field_73012_v` — map to structure-set
placement / the DSB roll as usual.)

## 11. DungeonSpawnerBlock outcome

- Original: `if (type == 15)` → `makeLeafMonsterDungeon(world, clickedX, clickedY,
  clickedZ)` — one call, no offset, block read in full (DSB:98-100).
- Port: add constant `TYPE_LEAF_MONSTER_DUNGEON = 15` (cite DSB:98-100) and case
  `TYPE_LEAF_MONSTER_DUNGEON -> LegacyDungeonPiece.buildNow(server, pos,
  DungeonType.LEAF_MONSTER_DUNGEON)` in
  `src/main/java/danger/orespawn/block/entity/RandomDungeonSpawnerBlockEntity.buildForType`
  (currently types 0/1/2/3/7/12/14/17/19/21/22/23/24/27/29/30/37/38/47 are wired,
  RandomDungeonSpawnerBlockEntity.java:44-79, 131-221; **type 15 falls through to the
  generic-dungeon fallback** today, the `default` arm at :219).
- The DSB path bypasses the biome/grass scan AND the foundation probe's usual context
  — the terrain-conditional roots still run (live ServerLevel reads are legal there),
  and a tower floating or embedded wherever the spawner block sat is faithful
  behavior.

## 12. Surprises / MISSING-IN-PORT

- **S1 (MISSING-IN-PORT)**: `makeLeafMonsterDungeon` has no counterpart anywhere in
  `src/main/java/` (grep `LeafMonster`: only the entity, egg, spawn block, renderer,
  stats — no structure/DungeonType/DSB case). Worldgen path and DSB type 15 both fall
  through today. No DungeonType, no JSON trio, no loot table.
- **S2**: The foundation loop (GD:2109-2118) is this batch's first genuine
  **pre-build terrain read** inside a builder — not a self-read. It is portable
  without an in-memory model because it is RNG-free and strictly same-cell
  (read→write); the royal-altar dirt skirt (LegacyDungeonPiece.java:1740-1747) is the
  approved precedent. Do NOT reflexively apply pattern trap 2 ("model it in memory")
  — there is nothing to model; the terrain IS the input.
- **S3**: The tower has **no built floor**: at `j = 0` the interior is air/ladder, so
  the ground floor is the original terrain (worldgen: the grass block at `cposy − 1`,
  which the foundation probe deliberately skips because grass is neither air nor
  tallgrass). Do not add a floor.
- **S4**: The ladder writes are the method's only **flags = 3** writes (GD:2139,
  meta 2 = facing north); everything else is flag-2. Because loop 3 places the
  supporting `k = +3` log wall BEFORE loop 4 runs, the neighbor check is a no-op and
  the port's flag-2 `piece.place(x, y, z, LADDER north)` produces the identical final
  state — documented, no deviation.
- **S5**: Leaf decay hazard (pattern §4 trap): flag-2 writes never run updates, and a
  modern `LeavesBlock` keeps `DISTANCE=7` and decays on random ticks — the crown-room
  leaf walls sit on the plateau's LEAF rim and the upper courses exceed distance 7
  from any log. Place **all** oak leaves with `PERSISTENT=true` (same adaptation as
  BlockDuplicatorLog / BlockExperiencePlant, pattern §4).
- **S6**: In the `j = +13` ring layer the leaf-ring `if` runs after the log-ring `if`
  and overwrites it where both apply — the 8 cells where the outer log square meets an
  inner-ring line become leaves, visibly notching the log square near its corners
  (§2.8). Faithful; replicate the check order (or the final per-cell result), do not
  "repair".
- **S7**: Double chest, single fill: two chest blocks (GD:2221-2222), loot pulled into
  the `(+1, +10, +5)` half only (GD:2223); 1.7.10 auto-unified adjacent chests, modern
  chests need explicit state to join. Port suggestion: both halves
  `facing=NORTH` (into the room; south wall behind), `(+1)` = `ChestType.LEFT`,
  `(+2)` = `ChestType.RIGHT` (modern `getConnectedDirection`: LEFT's partner sits
  clockwise of facing = east), loot table bound to `(+1)` only, `(+2)` stays empty.
  `placeLootChest` currently supports facing but not ChestType
  (LegacyDungeonPiece.java:431-444) — add a ChestType-aware overload (same
  WGEN-056-style fidelity extension as the facing parameter) or place the `(+2)` half
  via `piece.place` with the full state.
- **S8**: `Items.field_151162_bE` = **flower pot** — proven by the port's own
  ItemSifter case-5 mapping (ItemSifter.java:147). The existing
  `fillGreenhouseChest` maps the SAME field to `Items.APPLE`
  (LegacyDungeonPiece.java:780, from `GreenhouseContentsList` GD:31) — a pre-existing
  mis-transcription in the port, out of this task's scope but flagged as a finding;
  do not propagate it into this loot table.
- **S9**: Two intentional duplicate loot entries (flower pot ×2, oak sapling ×2,
  GD:46) — transcribe all 9 entries as-is; the duplicates double those items'
  effective weight exactly as the original did (total 255).
- **S10**: `addLeafMonster`'s return contract is the plain one — `true` only on
  placement (sets `recently_placed = 50`); success skips the rest of that chunk's
  chain (SpitBug/Igloo/BouncyCastle/RubberDuckyPond). Dead local `which` (OSW:1205) —
  ignore.
- **S11**: Anchoring quirk (same as Spit Bug S6): origin Y is the air block ABOVE
  grass (OSW:1207-1208, no −1 offset), so the tower's `j = 0` course sits one block
  above the terrain surface; the doorway threshold is the grass itself. Keep the
  anchor at first-free, not surface.
