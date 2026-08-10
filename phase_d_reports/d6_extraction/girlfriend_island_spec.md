# Girlfriend Island — Extraction Spec (1.7.10 → NeoForge 1.21.1)

Source of truth: `reference_1_7_10_source/sources/danger/orespawn/GenericDungeon.java` —
`makeGirlfriendIsland` (GD:4962-5028, next method `makeGreenhouseDungeon` at GD:5030;
`makeMonsterIsland` follows at GD:5170). Method read IN FULL, including both TE helpers it
uses (`getSpawnerTileEntity` GD:86-95, `getChestTileEntity` GD:75-84) and `FastSetBlock`
(GD:187-189). All coordinates are relative to the build origin `(cposx, cposy, cposz)` =
the three int args.

Citation convention: `GD:NN` = GenericDungeon.java line NN, `OSW:NN` = OreSpawnWorld.java,
`OSM:NN` = OreSpawnMain.java, `DSB:NN` = DungeonSpawnerBlock.java, `RDS:NN` =
`src/main/java/danger/orespawn/block/entity/RandomDungeonSpawnerBlockEntity.java`,
`LDP:NN` = `src/main/java/danger/orespawn/world/structure/LegacyDungeonPiece.java`,
`LDS:NN` = `src/main/java/danger/orespawn/world/structure/LegacyDungeonStructure.java`,
`MIG:NN` = `src/main/java/danger/orespawn/world/structure/MonsterIslandGenerator.java`.

Shared plumbing:
- `FastSetBlock(world, x, y, z, block)` (GD:187-189) → flag-2 chunk write, no neighbor
  updates → port `piece.place(x, y, z, state)` (structure_conversion_pattern.md §1 step 3
  table).
- Spawners: `world.func_147465_d(x, y, z, Blocks.field_150474_ac, 0, 2)` then
  `getSpawnerTileEntity` + `func_98272_a(name)` → `piece.placeSpawner(x, y, z, type)`
  (LDP:545). Chests: `func_147465_d(..., field_150486_ae, 0, 2)` then
  `getChestTileEntity` + `WeightedRandomChestContent.func_76293_a` →
  `piece.placeLootChest(x, y, z, lootKey)` (LDP:463; meta-0 chest → default-facing
  overload, MIG precedent).
- **No mid-build world-BLOCK reads** anywhere in GD:4962-5028 (no `func_147439_a` /
  `func_147437_c` calls) — only the six post-write tile-entity fetches, absorbed by the
  piece helpers (§10). The one NEEDS_DESIGN_RULING condition flagged at verification —
  the shared-loot-list / differing-fill-counts conflict with DamselInDistress (§3, S8)
  — was RESOLVED by the batch-3 main-session ruling (§3): one table per (list, fill
  formula) pair, the Kyuubi precedent. Geometry, placement, and everything else in
  this task is pattern-covered.
- **This is Monster Island's twin**: byte-for-byte identical island/tree geometry and
  chest positions (compare GD:4969-4997 with GD:5181-5209 and GD:5018-5027 with
  GD:5230-5239 — the loot fills sit at the method end in BOTH), differing ONLY in the
  spawner mobs (fixed names instead of the 50/50 sea-monster pick, which is an extra
  up-front RNG draw Girlfriend Island lacks, GD:5176-5180) and the loot list bound
  (`DamselContentsList` GD:4968 vs `MonsterIslandContentsList` GD:5177).
  `MonsterIslandGenerator` (already shipped) is the direct implementation template.

---

## 1. Entry points — EVERY call site

Grep of the whole original tree for `makeGirlfriendIsland`: exactly two call sites
(OSW:1390, DSB:159; GD:4962 is the definition).

| Caller | Signature used | Coords passed | Notes |
|---|---|---|---|
| `OreSpawnWorld.addGirlfriendIsland` (OSW:1390) | `OreSpawnMain.MyDungeon.makeGirlfriendIsland(world, posX, posY - 1, posZ)` | scan hit, **Y offset −1** — cposy is the WATER-surface block, so the sand layer replaces surface water | worldgen path, vanilla-overworld exact-"Ocean" biome only (§1.1-1.2) |
| `DungeonSpawnerBlock` type **35** (DSB:158-160) | `...makeGirlfriendIsland(world, clickedX, clickedY, clickedZ)` | player-placed block pos, no offset | `type = world.field_73012_v.nextInt(50)` (DSB:52), fires 400 ticks after placement (DSB:39), self + block above deleted first (DSB:50-51). The `if (type == 35)` block read IN FULL: **single builder call, nothing else** (DSB:158-160) — not a two-builder index. |

### 1.1 `addGirlfriendIsland` — FULL method + return contract (OSW:1378-1396)

```java
// OSW:1378-1396
public void addGirlfriendIsland(World world, Random random, int chunkX, int chunkZ) {
    if (random.nextInt(300) != 0) {
        return;
    }
    BiomeGenBase b = world.func_72807_a(chunkX, chunkZ);
    if (b.field_76791_y.equals("Ocean")) {
        for (int i = 0; i < 4; ++i) {
            int posX = chunkX + random.nextInt(16);
            int posZ = chunkZ + random.nextInt(16);
            boolean which = false;
            for (int posY = 100; posY > 40; --posY) {
                if (world.func_147439_a(posX, posY, posZ) != Blocks.field_150350_a || world.func_147439_a(posX, posY - 1, posZ) != Blocks.field_150355_j) continue;
                OreSpawnMain.MyDungeon.makeGirlfriendIsland(world, posX, posY - 1, posZ);
                recently_placed = 50;
                return;
            }
        }
    }
}
```

1. Gate: `random.nextInt(300) != 0 → return` (OSW:1379) — 1/300, chunk-provided `random`,
   drawn BEFORE the biome check.
2. Biome: `world.func_72807_a(chunkX, chunkZ)` (chunk-corner block coords) must have
   `biomeName` **exactly `"Ocean"`** (OSW:1382-1383) — excludes Deep Ocean, FrozenOcean,
   beaches, rivers.
3. Up to 4 attempts (OSW:1384): `posX/posZ = chunk + random.nextInt(16)` (OSW:1385-1386).
   (Dead local `boolean which = false`, OSW:1387 — same dead local as addMonsterIsland
   OSW:1407 and addLeafMonster OSW:1205; ignore.)
4. Column scan `posY = 100` down to `41` inclusive (`posY > 40`, OSW:1388): require
   `block(posX, posY, posZ) == air` AND `block(posX, posY-1, posZ) ==
   Blocks.field_150355_j` (**still water directly below**) (OSW:1389).
5. Hit → `makeGirlfriendIsland(world, posX, posY - 1, posZ)` — **anchor is the
   water-surface block itself** — then `recently_placed = 50` and `return`
   (OSW:1390-1392).

**FULL return contract: the method is `void`.** Gate fail, biome fail, and
all-attempts-miss all return having done nothing; only an actual placement sets the
global 50-chunk cooldown `recently_placed = 50` (OSW:30, 37-39). There is no boolean
result and no WGEN-062-style early-success quirk to port — the caller (§1.2) ignores any
outcome and the fall-through `addANest…` chain below it runs regardless (it is gated on
`ahh`, which the pool-roll branch never sets).

This scan is **line-for-line identical** to `addMonsterIsland` (OSW:1398-1416) except
for the builder called — the hospital_monster_island spec §B7 already cross-referenced
it ("match the GirlfriendIsland treatment — identical scan at OSW:1378-1396").

### 1.2 Worldgen dispatch chain (complete)

- The whole overworld dungeon block is gated by `OreSpawnMain.DisableOverworldDungeons
  == 0 && world.field_73011_w.field_76574_g == 0 && recently_placed == 0` (OSW:284) —
  vanilla-overworld exclusive, config-disableable.
- Independent 6-way pool roll `i = world.field_73012_v.nextInt(6)` (OSW:285 —
  **world rand**, not the passed `random`): `i==0` addPlayPool, `i==1`
  addWaterDragonLair, `i==2` addGoldFishBowl, **`i==3` → addGirlfriendIsland
  (OSW:295-297)**, `i==4` addMonsterIsland, `i==5` addFrogPond. The fall-through chain
  (addANest → … → addRubberDuckyPond, OSW:304-321) is independent of this roll.
- Effective odds: 1/6 × 1/300 = **1/1800 per overworld chunk whose corner biome is
  exactly "Ocean"**, before scan success — identical arithmetic to Monster Island
  (hospital_monster_island spec §B7), which shipped as spacing 42 / separation 21
  (`RES:worldgen/structure_set/monster_island.json`, salt 84345).

---

## 2. Geometry — per-loop tables (all ranges inclusive)

Write order matters (later writes overwrite earlier ones): island body → canopy slab →
canopy tip → trunk → diagonal branches → 4 spawners → 2 chests.

### 2.1 Loop 1 — lens-shaped island body (GD:4969-4984)

For `i = -5..5` (GD:4969): half-width `k = 3` default (GD:4970), `k = 1` at `i = ±5`
(GD:4971-4973), `k = 2` at `i = ±4` (GD:4974-4976), `k = 2` at `i = ±3` (GD:4977-4979);
then for `j = -k..k` (GD:4980): **sand** at `(i, 0, j)` (GD:4981) and **stone** at
`(i, -1, j)` (GD:4982).

| Column band | Z half-width | Cells per layer | Cite |
|---|---|---|---|
| `i ∈ -2..2` | `±3` (7 wide) | 5×7 = 35 | GD:4970 |
| `i = ±3, ±4` | `±2` (5 wide) | 4×5 = 20 | GD:4974-4979 |
| `i = ±5` | `±1` (3 wide) | 2×3 = 6 | GD:4971-4973 |

61 sand + 61 stone cells. Because worldgen anchors at the water-surface block (§1.1
step 5), the sand layer replaces the top water block (island flush with sea level) and
the stone layer sits one below.

### 2.2 Loop 2 — canopy slab (GD:4985-4989)

`i = -2..2`, `j = -2..2`: **oak leaves** at `(i, +3, j)` — 5×5 slab, 25 cells.

### 2.3 Canopy tip (GD:4990)

Single **oak leaf** at `(0, +4, 0)`.

### 2.4 Trunk (GD:4991-4993)

**Oak log** at `(0, +3, 0)`, `(0, +2, 0)`, `(0, +1, 0)` — written top-down; the `+3`
log overwrites the canopy slab's center leaf.

### 2.5 Diagonal branches (GD:4994-4997)

**Oak log** at `(+1, +3, +1)`, `(-1, +3, -1)`, `(+1, +3, -1)`, `(-1, +3, +1)` — four
diagonals at canopy level, each overwriting a loop-2 leaf.

### 2.6 Spawners (GD:4998-5017)

Four spawners in the canopy at `j = +3`, orthogonally around the trunk, each
overwriting a loop-2 leaf. **Unlike Monster Island there is NO mob roll** — the four
names are fixed constants:

| # | Position (rel) | Mob name string | Cite |
|---|---|---|---|
| 1 | `(+1, +3, 0)` | `"Girlfriend"` | GD:4998-5002 |
| 2 | `(-1, +3, 0)` | `"Boyfriend"` | GD:5003-5007 |
| 3 | `(0, +3, +1)` | `"Gold Fish"` | GD:5008-5012 |
| 4 | `(0, +3, -1)` | `"Gold Fish"` | GD:5013-5017 |

### 2.7 Chests (GD:5018-5027)

Two chests flanking the trunk at `y+1`, each placed with meta 0 (no facing) and each
filled from `DamselContentsList` with `4 + world.field_73012_v.nextInt(5)` = **4-8
weighted pulls** into random slots (collisions overwrite — documented approximation,
pattern §1 step 5):

| Chest | Position (rel) | Fill | Cite |
|---|---|---|---|
| 1 | `(0, +1, -1)` | 4-8 pulls | GD:5018-5022 |
| 2 | `(0, +1, +1)` | 4-8 pulls | GD:5023-5027 |

The chests sit in open air on the sand, one block north/south of the trunk's `+1` log.
They are NOT adjacent to each other (trunk between them) — two independent single
chests, no double-chest concern.

Net shape: an 11×7 lens-shaped sand-over-stone islet flush with the ocean surface,
carrying a single stylized palm-ish oak tree (3-log trunk, 5×5 leaf canopy with four
diagonal log branches and a tip leaf) whose canopy hides a Girlfriend, a Boyfriend, and
two Gold Fish spawners, with two loot chests at the trunk's foot.

---

## 3. Loot — FULL transcription

`DamselContentsList` (GD:39) — constructor semantics `(item, meta=0, minStack,
maxStack, weight)`. **9 entries, total weight = 315** (9 × 35). Fill count per chest:
`4 + nextInt(5)` (GD:5021, 5026) → `pools[0].rolls` uniform **min 4, max 8**; two
chests reference the same table.

**The list is SHARED with `makeDamselInDistress`** (GD:3625, list bound at GD:3635;
worldgen OSW:1311, DSB type 28 at DSB:137-139) — but the FILL COUNTS DIFFER:
DamselInDistress pulls `10 + nextInt(5)` = 10-14 (GD:3725) vs 4-8 here. Pattern §1
step 5 mandates BOTH "one JSON per list" AND "rolls = the original fill count" —
rules that cannot both hold for this pair, and `placeLootChest` binds only a table
key (LDP:474-481), so per-structure rolls on one JSON are impossible. **The pattern
doc does not cover a list shared across structures with different fill counts →
NEEDS_DESIGN_RULING** (options: fork two JSONs with identical entries, or accept one
compromised rolls range). The sibling `damsel_in_distress_spec.md` §3 names the file
`chests/damsel_in_distress.json` (rolls 10-14) and tells this structure to "reference
this same JSON with its own rolls" — which the port plumbing cannot do; it also
misstates this method's fill as `5 + nextInt(5)` → "5-9" citing GD:4941 (the real
fill is `4 + nextInt(5)`, GD:5021/5026 — erratum flagged in that spec's court). No
`damsel*` loot table exists in the port yet.

> **RULING (D6b batch 3, main session — RESOLVED, not NEEDS_DESIGN_RULING):**
> fork two JSONs with identical entries. This is not a new decision — the shipped
> Kyuubi tables (`kyuubi_dungeon_blaze_{north,south,east,west}.json`, D6a) already
> represent one 1.7.10 list at four fill formulas as four tables differing only in
> `rolls`; a list shared across two STRUCTURES is the same shape. Pattern doc §1
> step 5 now codifies it: one table per (list, fill formula) pair. Ship
> `chests/damsel_in_distress.json` (rolls 10-14) and `chests/girlfriend_island.json`
> (rolls 4-8), entries identical per the table below, each with a `_shared_list`
> comment key naming its twin.

| # | 1.7.10 item | Modern / port mapping (cite) | min | max | weight |
|---|---|---|---|---|---|
| 1 | `Items.field_151035_b` | `minecraft:iron_pickaxe` | 1 | 1 | 35 |
| 2 | `Items.field_151040_l` | `minecraft:iron_sword` | 1 | 1 | 35 |
| 3 | `Items.field_151157_am` | `minecraft:cooked_porkchop` | 3 | 10 | 35 |
| 4 | `Items.field_151082_bd` | `minecraft:beef` (raw) | 3 | 10 | 35 |
| 5 | `Items.field_151077_bg` | `minecraft:cooked_chicken` | 3 | 10 | 35 |
| 6 | `Items.field_151101_aQ` | `minecraft:cooked_cod` | 3 | 10 | 35 |
| 7 | `OreSpawnMain.MyBLT` (OSM:1856, unloc "blt_sandwich") | port `ModItems.BLT_SANDWICH` "blt_sandwich" (ModItems.java:546) | 4 | 10 | 35 |
| 8 | `OreSpawnMain.MySalad` (OSM:1855, unloc "salad") | port `ModItems.SALAD` "salad" (ModItems.java:544) | 4 | 10 | 35 |
| 9 | `OreSpawnMain.MyCornDog` (OSM:1847, unloc "corndog_cooked") | port `ModItems.CORN_DOG` "corn_dog" (ModItems.java:520) | 4 | 10 | 35 |

Rows 3-9 are already exercised verbatim by the shipped
`RES:loot_table/chests/battle_tower_rat.json` (`CrystalBattleTowerRatContentsList`
GD:32 shares those seven entries with identical stack args and weights) — copy those
entry blocks; only the two 1/1 iron-tool entries are new. No data components needed
(nothing CagedGirlfriend-shaped in this list — the caged-girlfriend item lives in the
Basilisk Maze loot, BasiliskMaze.java:28, ported with `set_components` in
`RES:loot_table/chests/basilisk_maze.json`, not here).

---

## 4. Spawner / mob mapping table

| Spawner name | 1.7.10 class (registration) | Port EntityType (cite) |
|---|---|---|
| `"Girlfriend"` (×1, GD:5001) | `Girlfriend` — `registerGlobalEntityID(Girlfriend.class, "Girlfriend", GirlfriendID)` OSM:3575, `registerModEntity` OSM:3579 | `ModEntities.GIRLFRIEND` "girlfriend" (ModEntities.java:445-447) |
| `"Boyfriend"` (×1, GD:5006) | `Boyfriend` — `registerGlobalEntityID(Boyfriend.class, "Boyfriend", BoyfriendID)` OSM:4313 | `ModEntities.BOYFRIEND` "boyfriend" (ModEntities.java:417-419) |
| `"Gold Fish"` (×2, GD:5011/5016) | `GoldFish` — `registerGlobalEntityID(GoldFish.class, "Gold Fish", GoldFishID)` OSM:4103, `registerModEntity` OSM:4107 | `ModEntities.GOLD_FISH` "gold_fish" (ModEntities.java:344-346) |

**NO direct entity spawns.** The task brief flagged GirlfriendIsland as a likely
direct-spawner; verified against the full method: there is no `func_72838_d`
(spawnEntityInWorld), no `EntityList` lookup, no `func_70012_b` yaw call, no NBT write,
and no persistence call anywhere in GD:4962-5028 — the Girlfriend/Boyfriend presence is
**spawner blocks only**. (The direct `"Girlfriend"` entity spawn the pattern doc
mentions at GD:3728 belongs to `makeDamselInDistress`, a different structure/task.)
`piece.spawnPersistent` is NOT needed here.

---

## 5. Block palette

| 1.7.10 field | Modern block | Used for | Cite |
|---|---|---|---|
| `Blocks.field_150354_m` | `minecraft:sand` | island surface layer | GD:4981 |
| `Blocks.field_150348_b` | `minecraft:stone` | island underlayer | GD:4982 |
| `Blocks.field_150362_t` (meta 0) | `minecraft:oak_leaves` — **place with `PERSISTENT=true`** (adaptation, §12 S5) | canopy slab + tip | GD:4987, 4990 |
| `Blocks.field_150364_r` (meta 0) | `minecraft:oak_log` (default axis=y) | trunk + 4 diagonal branches | GD:4991-4997 |
| `Blocks.field_150474_ac` | `minecraft:spawner` | 4 spawners | GD:4998/5003/5008/5013 |
| `Blocks.field_150486_ae` | `minecraft:chest` | 2 loot chests | GD:5018, 5023 |
| (placement scan only) `Blocks.field_150355_j` | still water | water-surface anchor test | OSW:1389 |

Identical palette to Monster Island (`MonsterIslandGenerator` MIG:75-80, including the
PERSISTENT leaves adaptation) — reuse those BlockState constants verbatim.

---

## 6. Footprint extents + suggested DungeonType

Relative to `(cposx, cposy, cposz)`:

| Axis | Min | Max | Extent | Determined by |
|---|---|---|---|---|
| X | `-5` | `+5` | **11** | island body loop `i = -5..5` (GD:4969) |
| Y | `-1` | `+4` | **6** | stone underlayer (GD:4982) / canopy tip (GD:4990) |
| Z | `-3` | `+3` | **7** | half-width `k = 3` (GD:4970); canopy only reaches ±2 |

Byte-identical to Monster Island's footprint (hospital_monster_island spec §B8; shipped
as `MONSTER_ISLAND(-6, 6, 2, 5, -4, 4, PlacementMode.OCEAN_SURFACE)`, LDP:161).
Suggested entry (asymmetric 6-int ctor `(xMin, xMax, down, up, zMin, zMax, mode)`, +1
margin):

```java
GIRLFRIEND_ISLAND(-6, 6, 2, 5, -4, 4, PlacementMode.OCEAN_SURFACE),
```

Plus the dispatch case in `LegacyDungeonPiece.postProcess` (beside
`case MONSTER_ISLAND ->`, LDP:380):
`case GIRLFRIEND_ISLAND -> GirlfriendIslandGenerator.generate(this, origin, rng);`

## 7. Placement — existing mode fits: `OCEAN_SURFACE`, no new mode

`addGirlfriendIsland`'s scan (§1.1) is line-for-line identical to `addMonsterIsland`'s
(OSW:1378-1396 vs OSW:1398-1416), which `OCEAN_SURFACE` already ports
(`LegacyDungeonStructure.oceanSurfaceOrigin`, LDS:197-217, dispatched at LDS:71): up to
4 attempts of `chunk + nextInt(16)` jitter, Y 100→41 window for air directly above
still water, **anchor at `posY − 1`** (the water-surface block = noise first-free-Y
minus 1), land columns rejected via the `OCEAN_FLOOR_WG` heightmap. Its Javadoc
(LDS:187-196) cites exactly these OSW lines. The exact-"Ocean" corner-biome check maps
to the structure JSON's inline biome, per the shipped monster_island precedent. The
`recently_placed = 50` cooldown maps onto structure-set separation (C7 approximation,
pattern §1 step 4). Suggested comment-only follow-up: add GirlfriendIsland to
`oceanSurfaceOrigin`'s Javadoc user list.

JSON pair (copy the `monster_island` trio and rename — same dimension, same anchor,
same biome, same odds):

- `RES:worldgen/structure/girlfriend_island.json` — `"type":
  "orespawn:legacy_dungeon"`, `"dungeon_type": "GIRLFRIEND_ISLAND"`,
  `"biomes": "minecraft:ocean"` (inline vanilla biome — the original's exact-name
  `"Ocean"` check OSW:1383 excludes deep/frozen oceans; identical to
  `monster_island.json`), `"step": "surface_structures"`,
  `"terrain_adaptation": "none"`, `"spawn_overrides": {}`.
- No `has_structure` tag file needed (inline single-biome form, monster_island
  precedent).

## 8. Structure-set conversion

Effective odds: 1/6 (pool roll, OSW:285/295) × 1/300 (gate, OSW:1379) = **1/1800 per
overworld chunk** with an exact-"Ocean" corner biome, before scan success — the same
arithmetic as Monster Island, which shipped as spacing 42 / separation 21
(`RES:worldgen/structure_set/monster_island.json`).

C7 sqrt equivalence: spacing ≈ √1800 ≈ 42.4 → **spacing 42, separation 21**.
Salt: **84364** (assigned to this task; verified free — grep of
`RES:worldgen/structure_set/*.json` shows OreSpawn salts topping out at 84361
(cephadrome_altar), plus the vanilla-style 10387399 dim_villages; sibling d6 specs
claim 84362 bouncy_castle, 84363 damsel_in_distress, 84365 stinky_house, 84366
pumpkin, 84367 rainbow — 84364 collides with none).

`RES:worldgen/structure_set/girlfriend_island.json`: random_spread, spacing 42,
separation 21, salt 84364.

## 9. DungeonSpawnerBlock outcome

- Original: `if (type == 35)` → `makeGirlfriendIsland(world, clickedX, clickedY,
  clickedZ)` — one call, no offset, block read in full (DSB:158-160). Note the −1
  worldgen offset does NOT apply on this path — the island's sand layer lands at the
  clicked block's Y, one higher relative to terrain than the worldgen anchoring.
- Port: add `TYPE_GIRLFRIEND_ISLAND = 35` (cite DSB:158-160) and
  `case TYPE_GIRLFRIEND_ISLAND -> LegacyDungeonPiece.buildNow(server, pos,
  DungeonType.GIRLFRIEND_ISLAND)` in `RandomDungeonSpawnerBlockEntity.buildForType`
  (wired types today: 0/1/2/3/7/12/13/14/15/16/17/18/19/20/21/22/23/24/27/29/30/34/
  37/38/47, RDS:44-91 + RDS:145-261; **type 35 currently falls through to the
  generic-dungeon default**, RDS:262).
- The DSB path bypasses the ocean scan entirely — an island built on dry land (or
  midair) at the clicked position, sand and stone overwriting whatever is there, is
  faithful behavior.

## 10. Mid-build world READS — classified

1. **Tile-entity fetches only**: the four spawner TE fetches (GD:4999, 5004, 5009,
   5014) and two chest TE fetches (GD:5019, 5024) via `func_147438_o` — SELF-reads of
   blocks written the line before; absorbed by `piece.placeSpawner` /
   `piece.placeLootChest`. No deviation decision needed.
2. **No world-block read exists in GD:4962-5028** — no `func_147439_a`, no
   `func_147437_c`, no foundation probe (contrast the graveyard/leaf-monster skirts).
   Neither the in-memory-model route nor the `terrainStateIfInChunk` /
   royal-altar-skirt helper is needed; nothing to flag.

## 11. RNG stream

The only draws in the builder are the **2 chest fill counts**
(`4 + world.field_73012_v.nextInt(5)`, GD:5021 and GD:5026), both of which move into
the loot JSON's `rolls 4-8` (pattern §1 step 3 rule 3 / step 5). The ported generator
therefore consumes **zero** random draws — geometry, spawner positions, and mob names
are all constants (no Monster-Island-style mob pick), trivially stitching-safe in every
per-chunk replay pass. Dispatch-layer rolls (OSW:285 6-way pool on `world.rand`,
OSW:1379 gate + OSW:1385-1386 jitter on the chunk `random`, DSB:52 on `world.rand`)
collapse into structure-set frequency / the DSB roll as usual.

## 12. Surprises / MISSING-IN-PORT

- **S1 (MISSING-IN-PORT)**: `makeGirlfriendIsland` has no counterpart anywhere in
  `src/main/java/` or `src/main/resources/` (grep `GirlfriendIsland|GIRLFRIEND_ISLAND`:
  zero matches; the port's many `Girlfriend` hits are the entity/overlay/item ports).
  No DungeonType, no generator, no JSON pair, no loot table; DSB type 35 falls through
  to the generic-dungeon default (RDS:262).
- **S2**: **Monster Island's geometry twin** — GD:4969-4997 and GD:5018-5027 are
  identical (same loops, same cells, same chest spots) to GD:5181-5209/5230-5239. A
  faithful port is `MonsterIslandGenerator` minus the mob roll, with fixed spawner
  types and the damsel loot key. Do NOT share one generator with a flag — pattern §1
  step 3 mandates one generator class per structure — but copy freely.
- **S3**: **No direct entity spawns** despite the task-brief warning (§4) — spawner
  blocks only; no yaw draw, no NBT, no persistence handling exists in the method. Do
  not invent a spawned Girlfriend (that behavior belongs to `makeDamselInDistress`,
  GD:3728).
- **S4**: No RNG mob pick — where Monster Island's FIRST draw is its 50/50 mob roll
  (GD:5178), Girlfriend Island's four spawner names are constants. The ported
  generator's RNG stream is empty; do not copy the Monster Island roll.
- **S5**: Leaf decay hazard (pattern §4 trap): flag-2 writes never run updates and the
  canopy's outer ring sits > distance 1 from the nearest log; modern `LeavesBlock`
  would decay. Place all oak leaves `PERSISTENT=true` — same adaptation as
  `MonsterIslandGenerator` (MIG:77-79).
- **S6**: Worldgen anchors at the WATER block (`posY − 1`, OSW:1390): the sand pad
  replaces the ocean's surface-water layer and sits flush with sea level; the stone
  layer is submerged. The DSB path (no offset) instead puts the sand AT the clicked
  position. Both faithful; do not normalize.
- **S7**: Write-order overwrites within the canopy layer (`y+3`): 25 leaves first,
  then center trunk log, then 4 diagonal logs, then 4 spawners — final layer is 16
  leaves + 5 logs (center + diagonals) + 4 spawners. Preserving order (or the final
  per-cell result) both work; the shipped MonsterIslandGenerator preserves order —
  do the same.
- **S8 (was NEEDS_DESIGN_RULING — RESOLVED by the §3 ruling: fork two JSONs with
  identical entries, Kyuubi precedent, pattern §1 step 5 as amended)**:
  `DamselContentsList` is shared with
  `makeDamselInDistress` (GD:3635) but the fill counts differ (4-8 here, GD:5021/5026,
  vs 10-14 there, GD:3725). Pattern §1 step 5's two rules — one JSON per list, rolls =
  the original fill count — cannot both hold, and `placeLootChest` binds only a table
  key (LDP:474-481), so one JSON cannot carry per-structure rolls. The pattern doc does
  not cover this case; the sibling `damsel_in_distress_spec.md` (§3/S10) names the file
  `chests/damsel_in_distress.json` with rolls 10-14 and assumes this structure can
  reuse it "with its own rolls", which the plumbing cannot do (it also misquotes this
  method's fill as 5-9/GD:4941). Decision needed: fork two JSONs with identical
  entries, or one shared JSON with a compromised rolls range. Whatever lands, 7 of the
  9 entries can be copied from the shipped `battle_tower_rat.json`.
- **S9**: The 1/300 gate (OSW:1379) is drawn from the chunk `random` BEFORE the biome
  check — in non-Ocean chunks the draw still occurs but can never place; frequency
  conversion uses the gate × pool-roll odds with biome selectivity carried by the
  JSON's `biomes` field (same treatment as Monster Island).
- **S10**: `addGirlfriendIsland` is `void` and sets `recently_placed = 50` only on
  placement (OSW:1391) — no return-value coupling to port; the cooldown maps to
  structure-set separation (C7 approximation).
- **S11**: Mixed RNG sources in the dispatch layers (same shape as hospital spec S9 /
  graveyard S8): the 6-way pool roll uses `world.rand` (OSW:285) while the gate/jitter
  use the chunk-provided `random` (OSW:1379, 1385-1386); the builder's loot counts use
  `world.rand` (GD:5021, 5026). All collapse into structure-set frequency / loot rolls.
