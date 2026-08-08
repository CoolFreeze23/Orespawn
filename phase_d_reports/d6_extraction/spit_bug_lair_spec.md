# Spit Bug Lair — Extraction Spec (1.7.10 → NeoForge 1.21.1)

Source of truth: `reference_1_7_10_source/sources/danger/orespawn/GenericDungeon.java` —
`makeSpitBugLair` (GD:2638-2696). All coordinates are relative to the build origin
`(cposx, cposy, cposz)` = the three int args. Method-local constants: `green = 5`
(GD:2640), `dark_green = 13` (GD:2641), `width = 9` (GD:2642).

Citation convention: `GD:NN` = GenericDungeon.java line NN, `OSW:NN` = OreSpawnWorld.java,
`OSM:NN` = OreSpawnMain.java, `DSB:NN` = DungeonSpawnerBlock.java. Port citations name the
port file + line.

Shared plumbing:
- `OreSpawnMain.setBlockFast(world, x, y, z, block, meta, 2)` — flag-2 chunk write, no
  neighbor updates → the port's `piece.place(...)` helper (structure_conversion_pattern.md
  step 3 table).
- Spawners/chest via `world.func_147465_d(..., 0, 2)` then TE fetch through
  `getSpawnerTileEntity` (GD:86-95) / `getChestTileEntity` (GD:75-84) — both are plain
  `func_147438_o` (getTileEntity) fetches at positions just written. These TE fetches
  (GD:2659, 2664, 2669, 2692) are the **only world reads inside the builder**; there is no
  `func_147439_a` (getBlock) call anywhere in GD:2638-2696. Every read is a self-read
  reproducible from the write set and is already absorbed by the port's
  `piece.placeSpawner` / `piece.placeLootChest` helpers. **No deviation decision needed.**
- RNG inside the builder: exactly ONE draw, the chest fill count `4 +
  world.field_73012_v.nextInt(4)` (GD:2694), which moves into the loot JSON's `rolls`
  (pattern §1 step 3 rule 3). The generator body is therefore RNG-free —
  trivially stitching-safe.

---

## 1. Entry points (every call site — grep of the full original tree)

| Caller | Signature used | Coords passed | Notes |
|---|---|---|---|
| `OreSpawnWorld.addSpitBug` (OSW:1250) | `OreSpawnMain.MyDungeon.makeSpitBugLair(world, posX, posY, posZ)` | scan hit, **no Y offset** — posY is the AIR block directly above grass, so the platform floor replaces that air one block above ground | worldgen path, vanilla overworld Swampland only (§7) |
| `DungeonSpawnerBlock` type **19** (DSB:110-112) | `...makeSpitBugLair(world, clickedX, clickedY, clickedZ)` | player-placed block pos, no offset | `type = world.rand.nextInt(50)` (DSB:52), fires 400 ticks after placement (DSB:39). The `if (type == 19)` block read IN FULL: **single builder call, nothing else** (DSB:110-112) — not a two-builder index. |

No other call site exists (grep `makeSpitBugLair` over the whole reference tree: OSW:1250
and DSB:111 only).

### `addSpitBug` — FULL method + return contract (OSW:1238-1257)

1. Gate: `random.nextInt(190) != 0 → return false` (OSW:1239) — 1/190, drawn from the
   chunk-provided `random` BEFORE the biome check.
2. Biome: `world.func_72807_a(chunkX, chunkZ)` (chunk-corner block coords) must have
   `biomeName` **exactly `"Swampland"`** (OSW:1242-1243) — excludes Swampland M and all
   non-swamp biomes; else fall through to `return false`.
3. Up to 4 attempts (OSW:1244): `posX = chunkX + random.nextInt(16)`,
   `posZ = chunkZ + random.nextInt(16)` (OSW:1245-1246). (A local `boolean which = false`
   is declared and never used, OSW:1247.)
4. Column scan `posY = 100` down to `41` inclusive (`posY > 40`, OSW:1248): require
   `block(posX, posY, posZ) == air` AND `block(posX, posY-1, posZ) ==
   Blocks.field_150349_c` (**grass block directly below**) (OSW:1249).
5. Hit → `makeSpitBugLair(world, posX, posY, posZ)`, `recently_placed = 50`,
   `return true` (OSW:1250-1252).
6. All attempts miss → `return false` (OSW:1256).

**Return contract: `true` ONLY on an actual placement** (which also sets the 50-chunk
global cooldown, OSW:30/37-39). No addFairyTree-style early-true quirk (WGEN-062) — the
1/190 gate failing, the biome failing, or the scan failing all return `false`, letting the
chunk's chain continue.

### Chain position (OSW:284-322)

The whole block is gated by `OreSpawnMain.DisableOverworldDungeons == 0 &&
world.provider.dimensionId == 0 && recently_placed == 0` (OSW:284) — vanilla-overworld
exclusive, config-disableable. Inside it, `i = world.field_73012_v.nextInt(6)` picks one
of the six "pool" structures (OSW:285-303 — independent of this chain, but the draw
precedes it), then the fall-through chain runs: `addANest` (1/230 gate, OSW:1000) →
`addHauntedHouse` (1/285, OSW:980) → `addLeafMonster` (1/275, OSW:1197) → **`addSpitBug`
(OSW:311)** → `addIgloo` → `addBouncyCastle` → `addRubberDuckyPond` (OSW:304-321), each
link only when every earlier link returned false. Suppression of addSpitBug by the three
earlier links is ≤ ~1.2% before their own biome/scan filters — negligible; the
`recently_placed` coupling maps onto structure-set separation per the C7-approved
approximation (pattern §1 step 4).

---

## 2. Geometry — per-loop tables (all ranges inclusive)

Write order matters (later writes overwrite earlier ones — see §2.5 and surprises S2/S3):
loop 1 fin → emerald-ore antenna → 3 spawners → loop 2 platform → chest.

### 2.1 Loop 1 — triangular fin ("tent ridge") in the XY plane at `z = 0` (GD:2647-2654)

For `i = 0..8`, at BOTH `x = +i` and `x = −i` (the `i = 0` pair writes the same three
cells twice — harmless):

| Cell (rel) | Block | Cite |
|---|---|---|
| `(±i, 11−i, 0)` | green terracotta (stained clay meta 13) | GD:2648, 2651 |
| `(±i, 10−i, 0)` | green terracotta (meta 13) | GD:2649, 2652 |
| `(±i, 9−i, 0)` | mossy cobblestone | GD:2650, 2653 |

Net: a 17-wide, 1-thick A-frame fin — a mossy-cobblestone diagonal from `(0, +9)` down to
`(±8, +1)`, with two green-terracotta courses stacked on top of each mossy step (apex
terracotta at `(0, +11)`).

### 2.2 Emerald-ore antenna — center column (GD:2655-2657)

`(0, +12, 0)`, `(0, +11, 0)`, `(0, +10, 0)` = **`Blocks.field_150412_bA` meta 0 =
`minecraft:emerald_ore`** (identity confirmed: same field is the block fed to
emerald-ore `WorldGenMinable`/`generateBlockOre` at OSW:919 and ChunkOreGenerator.java:570,
and the port's MantisNestFeature.java:98 comment records the same mapping). The `+11` and
`+10` writes overwrite the loop-1 apex terracotta (S2).

### 2.3 Spawner column (GD:2658-2672)

Three stacked spawners in the fin's center column, each `func_147465_d(...,
field_150474_ac, 0, 2)` then `func_98272_a("Spit Bug")`:

| # | Position (rel) | Mob name string | Cite | Overwrites |
|---|---|---|---|---|
| 1 | `(0, +9, 0)` | `"Spit Bug"` | GD:2658-2662 | loop-1 apex mossy cobblestone |
| 2 | `(0, +8, 0)` | `"Spit Bug"` | GD:2663-2667 | nothing (terrain) |
| 3 | `(0, +7, 0)` | `"Spit Bug"` | GD:2668-2672 | nothing (terrain) |

### 2.4 Loop 2 — taxicab-diamond platform (GD:2674-2690)

For `i = 0..8`, `j = −i..+i`, mirrored `x' = −8+i` (west) and `x' = +8−i` (east); at
`i = 8` both mirrors collapse to `x' = 0` (center cells written twice, identical). In
dx/dz terms the union covers exactly the diamond `|dx| + |dz| ≤ 8`:

| Where | Cell (rel) | Block | Cite |
|---|---|---|---|
| whole diamond | `(dx, 0, dz)` | lime terracotta (stained clay meta 5) | GD:2676-2677 |
| rim only (`j == ±i` ⇔ `|dx|+|dz| == 8`) | `(dx, +1, dz)` | green terracotta (meta 13) | GD:2679-2680 |
| rim only | `(dx, +2, dz)` | chiseled stone bricks (stonebrick meta 3) | GD:2681-2682 |
| interior (`|dx|+|dz| < 8`) | `(dx, +1, dz)` and `(dx, +2, dz)` | air | GD:2685-2688 |

(A local `boolean k = false` at GD:2673 is never used.)

### 2.5 Loop-2 overwrites of the fin (behavioral — replicate write order)

At `z = 0` loop 2 revisits the fin's lowest cells (loop 2 runs AFTER loop 1):

- `(±8, +1, 0)`: rim rule → loop-1 mossy cobblestone → **green terracotta** (GD:2679-2680).
- `(±8, +2, 0)`: rim rule → loop-1 green terracotta → **chiseled stone bricks** (GD:2681-2682).
- `(±7, +1..+2, 0)`: interior rule → **air**, erasing loop-1's mossy step at `(±7, +2, 0)`
  (GD:2685-2688). The mossy diagonal is therefore visibly broken at its two lowest steps
  on each side (S3).
- `(0, +1..+2, 0)`: interior rule → air (clears the chest cell before the chest goes in).

### 2.6 Chest (GD:2691-2695)

One chest at `(0, +1, 0)` (`func_147465_d(..., field_150486_ae, 0, 2)`), filled with
`WeightedRandomChestContent.func_76293_a(world.rand, SpitBugContentsList, chest,
4 + world.rand.nextInt(4))` → **4-7 weighted pulls** into random slots (collisions
overwrite — documented approximation, pattern §1 step 5).

Net shape: a lime-terracotta diamond platform (radius 8, taxicab) with a green-terracotta
+ chiseled-stone-brick rim wall 2 high, a 17-wide 1-thick A-frame fin across it at `z = 0`,
three Spit Bug spawners stacked in the fin's core at `+7..+9`, a 3-block emerald-ore
antenna at `+10..+12`, and one loot chest at the center floor.

---

## 3. Loot — FULL transcription

`SpitBugContentsList` (GD:42) — constructor semantics `(item, meta=0, minStack, maxStack,
weight)`. 15 entries, **total weight = 295** (35 + 3×25 + 9×15 + 2×25).
`pools[0].rolls`: uniform **min 4, max 7** (from `4 + nextInt(4)`, GD:2694).

| # | 1.7.10 item | Modern / port mapping (cite) | min | max | weight |
|---|---|---|---|---|---|
| 1 | `Items.field_151078_bh` | `minecraft:rotten_flesh` | 6 | 16 | 35 |
| 2 | `Items.field_151115_aP` (raw fish, meta 0) | `minecraft:cod` | 6 | 16 | 25 |
| 3 | `Items.field_151103_aS` | `minecraft:bone` | 6 | 16 | 25 |
| 4 | `Items.field_151007_F` | `minecraft:string` | 6 | 16 | 25 |
| 5 | `OreSpawnMain.MyAmethystPickaxe` ("amethystpickaxe", OSM:1672) | port `ModItems.AMETHYST_PICKAXE` "amethyst_pickaxe" (ModItems.java:300) | 1 | 1 | 15 |
| 6 | `OreSpawnMain.MyAmethystShovel` ("amethystshovel", OSM:1674) | `ModItems.AMETHYST_SHOVEL` "amethyst_shovel" (ModItems.java:302) | 1 | 1 | 15 |
| 7 | `OreSpawnMain.MyAmethystHoe` ("amethysthoe", OSM:1676) | `ModItems.AMETHYST_HOE` "amethyst_hoe" (ModItems.java:304) | 1 | 1 | 15 |
| 8 | `OreSpawnMain.MyAmethystAxe` ("amethystaxe", OSM:1677) | `ModItems.AMETHYST_AXE` "amethyst_axe" (ModItems.java:306) | 1 | 1 | 15 |
| 9 | `OreSpawnMain.MyAmethystSword` ("amethystsword", OSM:1671) | `ModItems.AMETHYST_SWORD` "amethyst_sword" (ModItems.java:298) | 1 | 1 | 15 |
| 10 | `OreSpawnMain.AmethystBody` ("amethyst_chest", OSM:1809) | `ModItems.AMETHYST_CHESTPLATE` "amethyst_chestplate" (ModItems.java:781) | 1 | 1 | 15 |
| 11 | `OreSpawnMain.AmethystLegs` ("amethyst_leggings", OSM:1810) | `ModItems.AMETHYST_LEGGINGS` "amethyst_leggings" (ModItems.java:784) | 1 | 1 | 15 |
| 12 | `OreSpawnMain.AmethystHelmet` ("amethyst_helmet", OSM:1808) | `ModItems.AMETHYST_HELMET` "amethyst_helmet" (ModItems.java:778) | 1 | 1 | 15 |
| 13 | `OreSpawnMain.AmethystBoots` ("amethyst_boots", OSM:1811) | `ModItems.AMETHYST_BOOTS_ARMOR` "amethyst_boots" (ModItems.java:787) | 1 | 1 | 15 |
| 14 | `OreSpawnMain.InstantGarden` ("instantgarden", OSM:1937) | `ModItems.INSTANT_GARDEN` "instant_garden" (ModItems.java:648) | 2 | 4 | 25 |
| 15 | `OreSpawnMain.InstantShelter` ("instantshelter", OSM:1936) | `ModItems.INSTANT_SHELTER` "instant_shelter" (ModItems.java:646) | 2 | 4 | 25 |

→ `RES:loot_table/chests/spit_bug_lair.json`, rolls uniform 4-7, one entry per row above.

---

## 4. Spawner / mob mapping table

| Spawner name | 1.7.10 class (registration) | Port EntityType (cite) |
|---|---|---|
| `"Spit Bug"` (×3, GD:2661/2666/2671) | `SpitBug` — `registerGlobalEntityID(SpitBug.class, "Spit Bug", SpitBugID)` OSM:3951, `registerModEntity` OSM:3955 | `ModEntities.ENTITY_SPIT_BUG` "spit_bug" (ModEntities.java:262-264) |

No direct entity spawns — spawner blocks only.

---

## 5. Block palette

| 1.7.10 field (meta) | Modern block | Used for | Cite |
|---|---|---|---|
| `Blocks.field_150406_ce` meta 13 (`dark_green`) | `minecraft:green_terracotta` | fin upper courses, platform rim y+1 | GD:2648-2652, 2679-2680 |
| `Blocks.field_150406_ce` meta 5 (`green`) | `minecraft:lime_terracotta` | platform floor y+0 | GD:2676-2677 |
| `Blocks.field_150341_Y` | `minecraft:mossy_cobblestone` | fin diagonal | GD:2650, 2653 |
| `Blocks.field_150412_bA` | `minecraft:emerald_ore` (NOT emerald block — see §2.2) | 3-block antenna | GD:2655-2657 |
| `Blocks.field_150417_aV` meta 3 | `minecraft:chiseled_stone_bricks` | platform rim y+2 | GD:2681-2682 |
| `Blocks.field_150350_a` | `minecraft:air` | platform interior y+1..+2 | GD:2685-2688 |
| `Blocks.field_150474_ac` | `minecraft:spawner` | 3 spawners | GD:2658/2663/2668 |
| `Blocks.field_150486_ae` | `minecraft:chest` | 1 loot chest | GD:2691 |

Note the misleading local names: `green = 5` is LIME terracotta, `dark_green = 13` is
GREEN terracotta (1.7.10 stained-clay color indices).

---

## 6. Footprint extents (relative to origin) + suggested DungeonType

| Axis | Min | Max | Extent | Determined by |
|---|---|---|---|---|
| X | `−8` | `+8` | **17** | fin `i = 8` (GD:2647-2653) / diamond radius 8 (GD:2676) |
| Y | `0` | `+12` | **13** | platform floor y+0 (GD:2676) / antenna top y+12 (GD:2655) |
| Z | `−8` | `+8` | **17** | diamond `j = ±8` at `i = 8` (GD:2675); fin occupies only z = 0 |

Suggested entry (asymmetric 6-int ctor, +1 margin):

```java
SPIT_BUG_LAIR(-9, 9, 1, 13, -9, 9, PlacementMode.SWAMP_GRASS_SURFACE),  // NEEDS_NEW_MODE — see §7
```

## 7. Placement — **NEEDS_NEW_MODE** (with SURFACE_CENTER as the documented fallback)

None of the existing modes reproduces the original scan (quoted in full):

```java
// OSW:1244-1254
for (int i = 0; i < 4; ++i) {
    int posX = chunkX + random.nextInt(16);
    int posZ = chunkZ + random.nextInt(16);
    boolean which = false;
    for (int posY = 100; posY > 40; --posY) {
        if (world.func_147439_a(posX, posY, posZ) != Blocks.field_150350_a
            || world.func_147439_a(posX, posY - 1, posZ) != Blocks.field_150349_c) continue;
        OreSpawnMain.MyDungeon.makeSpitBugLair(world, posX, posY, posZ);
        recently_placed = 50;
        return true;
    }
}
```

Why the existing five don't fit: `SURFACE_CENTER` is a bare chunk-center heightmap probe
with no grass/dry-land check — in a 1.21 swamp (large water sheets at sea level) it would
anchor the platform ON the water surface, a case the original's grass-below requirement
explicitly rejects; `ISLANDS_GRASS` is hardwired to the Islands flat plane (Y 20→5 scan +
LessLag skip, LegacyDungeonPiece.java:175-182); `LOWEST_SURFACE_36` sinks −2 with the maze's
Y window; `END_SURFACE`/`OCEAN_SURFACE` are wrong dimensions/media.

Suggested new mode `SWAMP_GRASS_SURFACE` (add per pattern §1 step 4: enum constant + case
in `LegacyDungeonStructure.findGenerationPoint`): up to 4 attempts of chunk + `nextInt(16)`
jitter; anchor = `getBaseHeight(x, z, WORLD_SURFACE_WG) ` (first free block = the original's
air-above-surface hit), accept only when `41 ≤ anchorY ≤ 100` (the `posY > 40` window is
behavior — trap 7) AND the column is dry: `WORLD_SURFACE_WG == OCEAN_FLOOR_WG` at that
column (grass-below approximated as "no water column"; exact grass identity is not
predictable pre-terrain — document as the mode's mapping delta, same style as
END_SURFACE's clearance approximation, LegacyDungeonPiece.java:183-195).

- Biome tag `RES:tags/worldgen/biome/has_structure/spit_bug_lair.json`: **`minecraft:swamp`
  only** (original exact-name `"Swampland"`, OSW:1243; `minecraft:mangrove_swamp` has no
  1.7.10 analogue — excluded, mirroring Monster Island's exact-"Ocean" treatment).
- `RES:worldgen/structure/spit_bug_lair.json`: `"type": "orespawn:legacy_dungeon"`,
  `"dungeon_type": "SPIT_BUG_LAIR"`, `"step": "surface_structures"`,
  `"spawn_overrides": {}`.

## 8. Structure-set conversion

Effective odds ≈ **1/190 per vanilla-overworld chunk** (OSW:1239) before the Swampland
corner-biome filter and scan (biome selectivity is carried by the biome tag; the ≤1.2%
suppression from the three earlier chain links and the `recently_placed`/pool-roll
couplings are absorbed per the C7 approximation, pattern §1 step 4).

C7 sqrt equivalence: spacing ≈ √190 ≈ 13.8 → **spacing 14, separation 7**.
Salt: **84354** (assigned; verified free — current max in
`RES:worldgen/structure_set/*.json` is 84345, batch-1 range 84350-84355).

`RES:worldgen/structure_set/spit_bug_lair.json`: random_spread, spacing 14,
separation 7, salt 84354.

## 9. DungeonSpawnerBlock outcome

- Original: `type == 19` → `makeSpitBugLair(world, clickedX, clickedY, clickedZ)` — the
  full block is one call, no offset, no second builder (DSB:110-112).
- Port: add `TYPE_SPIT_BUG_LAIR = 19` and
  `case TYPE_SPIT_BUG_LAIR -> LegacyDungeonPiece.buildNow(server, pos, DungeonType.SPIT_BUG_LAIR)`
  in `src/main/java/danger/orespawn/block/entity/RandomDungeonSpawnerBlockEntity.buildForType`
  (currently types 0/1/2/7/21/22/23/24/27/29/30/37/38/47 are wired,
  RandomDungeonSpawnerBlockEntity.java:44-69, 121-188; **type 19 falls through to the
  generic-dungeon fallback** today, the `default` arm at :186-187). The DSB path bypasses the biome/grass scan entirely —
  a floating or terrain-embedded lair at the clicked position is faithful behavior.

## 10. Surprises / MISSING-IN-PORT

- **S1 (MISSING-IN-PORT)**: `makeSpitBugLair` exists nowhere in `src/main/java/` (grep:
  zero matches); DSB type 19 currently builds a generic dungeon via the fallback. No
  DungeonType, no JSON trio, no loot table.
- **S2**: The 3-block antenna is **emerald ORE** (`field_150412_bA`), not emerald block —
  the same field the mod feeds to its emerald-ore worldgen (OSW:919). Its lower two blocks
  overwrite the fin's apex terracotta (GD:2655-2657 after GD:2648-2649), and spawner #1
  overwrites the apex mossy cobblestone (GD:2658 after GD:2650). Preserve write order or
  place antenna/spawners last — same result.
- **S3**: Loop 2 (platform, GD:2674-2690) runs AFTER loop 1 (fin) and partially erases the
  fin's bottom: at `x = ±7, z = 0` the interior-air rule deletes the mossy step at y+2;
  at `x = ±8, z = 0` the rim rule converts mossy→green terracotta (y+1) and
  terracotta→chiseled stone bricks (y+2). The broken-ridge look is original behavior —
  do not "repair" it.
- **S4**: The only RNG draw in the whole builder is the chest fill count (GD:2694), which
  moves to loot-JSON `rolls 4-7` — the ported generator consumes zero random draws, so the
  RNG stitching contract is satisfied vacuously. No mid-build world-block reads (§ shared
  plumbing); no in-memory model needed.
- **S5**: `addSpitBug`'s return contract is the plain one — `true` only on placement (sets
  `recently_placed = 50`); no addFairyTree-style early-true quirk (contrast WGEN-062).
  Success skips the rest of that chunk's chain (Igloo/BouncyCastle/RubberDuckyPond).
- **S6**: Anchoring quirk: the origin Y is the air block ABOVE grass (OSW:1249-1250, no −1
  offset), so the lime floor sits one block above the terrain surface, resting on grass —
  the platform reads as slightly raised. Keep the anchor at first-free, not surface.
- **S7**: Misleading source constants: `green = 5` is lime terracotta, `dark_green = 13`
  is green terracotta (§5). Two dead locals (`which` OSW:1247, `k` GD:2673) — ignore.
- **S8**: Placement needs a new mode (`SWAMP_GRASS_SURFACE`, §7) — existing modes either
  target other dimensions or would place the lair on swamp water the original rejects.
