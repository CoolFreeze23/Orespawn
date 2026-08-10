# Igloo — Extraction Spec (1.7.10 → NeoForge 1.21.1)

Source of truth: `reference_1_7_10_source/sources/danger/orespawn/GenericDungeon.java` —
`makeIgloo` (GD:2698-2813, next method `makeEnderDragonHospital` at GD:2815). All
coordinates are relative to the build origin `(cposx, cposy, cposz)` = the three int args.
Method read IN FULL (GD:2698-2813); locals `tileentitymobspawner`/`chest` (GD:2702-2703)
are plain TE handles, no dead RNG.

Citation convention: `GD:NN` = GenericDungeon.java line NN, `OSW:NN` = OreSpawnWorld.java,
`OSM:NN` = OreSpawnMain.java, `DSB:NN` = DungeonSpawnerBlock.java, `IS:NN` =
InstantShelter.java. Port citations name file + line.

Shared plumbing:
- `FastSetBlock(world, x, y, z, block)` (GD:187-189) → `setBlockFast(..., meta 0, flags 2)`
  → port `piece.place(x, y, z, state)` (structure_conversion_pattern.md §1 step 3 table).
- Spawners: `world.func_147465_d(x, y, z, field_150474_ac, 0, 2)` + `getSpawnerTileEntity`
  (GD:86-95) + `func_98272_a(name)` → port `piece.placeSpawner`.
- Chest: `func_147465_d(..., field_150486_ae, 2, 2)` (**meta 2 = facing north**) +
  `getChestTileEntity` (GD:75-84) → port `piece.placeLootChest` (note the facing, §2.6).
- **The float circle idiom** (all six shell loops): `curx = (float)(currad *
  Math.cos(Math.toRadians(curdeg)))`, cell X = `(int)((float)cposx + curx + 0.5f)` (e.g.
  GD:2706-2708). `(int)` truncates toward zero, so the canonical cell tables below hold for
  **positive** world coordinates; on a negative axis every non-integral sum shifts +1 toward
  zero (see §2.7 and S5). Port precedent: transcribe the idiom VERBATIM (bit-identical float
  math, drift preserved) — the drift-preserving origin-added form is exactly what
  `port:world/CrystalStructures.buildCrystalBattleTower` (CrystalStructures.java:813-870,
  idiom at :822-826/:831-840) does: `(int)(cx + curx + 0.5f)`. **Do NOT copy
  `port:world/feature/CrystalBattleTowerFeature.stampDisc`/`stampRing`
  (CrystalBattleTowerFeature.java:151-159 / :169+)** — those compute the offset first
  (`(int)(r * Math.cos(...) + 0.5f)`, then `centre.offset(dx, 0, dz)`), so truncation
  happens around zero: canonical cells on the positive arc but +1-shifted on the negative
  arc, independent of world quadrant — a zero-centered hybrid that is NOT bit-identical to
  the original at any origin. Verbatim (origin-added) transcription is deterministic (no
  RNG, no world reads), so every per-chunk replay pass is identical — RNG contract
  satisfied.

---

## 1. Entry points (EVERY call site — grep `makeIgloo|addIgloo` over the whole original tree)

Exactly two `makeIgloo` call sites: OSW:1271 and DSB:114. One `addIgloo` call site: OSW:314.

| Caller | Signature used | Coords passed | Notes |
|---|---|---|---|
| `OreSpawnWorld.addIgloo` (OSW:1271) | `OreSpawnMain.MyDungeon.makeIgloo(world, posX, posY - 2, posZ)` | scan hit, **Y offset −2** (posY = the air block directly above a snow block; cposy = one BELOW the surface block) | worldgen path, vanilla overworld "Ice Plains" only (§1.1) |
| `DungeonSpawnerBlock` type **20** (DSB:113-115) | `...makeIgloo(world, clickedX, clickedY, clickedZ)` | player-placed block pos, **no −2 offset** | `type = world.rand.nextInt(50)` (DSB:52), fires 400 ticks after placement (DSB:39), self+above deleted first (DSB:50-51). The `if (type == 20)` block read IN FULL: **single builder call, nothing else** (DSB:113-115) — not a two-builder index. |

### 1.1 `addIgloo` — FULL method + return contract (OSW:1259-1278)

1. Gate: `random.nextInt(220) != 0 → return false` (OSW:1260-1262) — 1/220, chunk-provided
   `random`, drawn BEFORE the biome check.
2. Biome: `world.func_72807_a(chunkX, chunkZ)` — chunk-CORNER block coords, since
   `generateSurface` receives `chunkX * 16` (OSW:41) — must have `field_76791_y` **exactly
   `"Ice Plains"`** (OSW:1263-1264). Excludes "Ice Plains Spikes", "Ice Mountains",
   "Cold Taiga", etc.; else fall through to `return false` (OSW:1277).
3. Up to 4 attempts (OSW:1265): `posX = chunkX + random.nextInt(16)`, `posZ = chunkZ +
   random.nextInt(16)` (OSW:1266-1267). (`boolean which = false`, OSW:1268, is a dead
   local — never read.)
4. Column scan `posY = 100` down to `41` inclusive (`posY > 40`, OSW:1269): require
   `block(posX, posY, posZ) == air` AND `block(posX, posY-1, posZ) ==
   Blocks.field_150433_aE` (**SNOW BLOCK directly below** — the full block, NOT the snow
   layer `field_150431_aC`) (OSW:1270).
5. Hit → `makeIgloo(world, posX, posY - 2, posZ)`, `recently_placed = 50`, `return true`
   (OSW:1271-1273).
6. All attempts / biome miss → `return false` (OSW:1277).

**Return contract: `true` ONLY on an actual placement** (which also sets the global
50-chunk cooldown, OSW:30). No addFairyTree-style early-true quirk (WGEN-062) — gate,
biome, or scan failure all return `false`, letting `addBouncyCastle` →
`addRubberDuckyPond` still run for the chunk (OSW:316-321).

### 1.2 Chain position (OSW:284-322)

Whole block gated by `OreSpawnMain.DisableOverworldDungeons == 0 &&
world.provider.dimensionId == 0 && recently_placed == 0` (OSW:284) — vanilla-overworld
exclusive, config-disableable. The 6-way pool roll (OSW:285-303) runs first, then the
fall-through chain: `addANest` (1/230) → `addHauntedHouse` (1/285) → `addLeafMonster`
(1/275) → `addSpitBug` (1/190) → **`addIgloo` (OSW:314)** → `addBouncyCastle` →
`addRubberDuckyPond`, each link only when every earlier link returned false. Suppression
by the four earlier links is ≈ 1.7% (1/230 + 1/285 + 1/275 + 1/190 ≈ 0.0168) before their
own biome/scan filters — negligible;
the `recently_placed` coupling maps onto structure-set separation (C7 approximation,
pattern §1 step 4).

### 1.3 The snow-block reachability problem (feeds §7's NEEDS_DESIGN_RULING)

The gate biome and the scanned surface are mutually inconsistent in vanilla 1.7.10
terrain (**vanilla-behavior claim from 1.7.10 knowledge — vanilla sources are NOT in the
reference tree; verify before acting on it**): plain "Ice Plains" (`BiomeGenSnow`,
non-spikes) has a GRASS top block under a thin snow LAYER (`field_150431_aC`), which the
scan rejects (it demands the snow BLOCK `field_150433_aE`); snow-block surfaces belong to
the "Ice Plains Spikes" mutation, which the exact-name corner gate excludes. Natural
worldgen success paths are therefore edge cases: (a) the corner sits in Ice Plains while
the jittered column (up to +15 blocks) crosses into a bordering Ice Plains Spikes patch
whose surface IS snow blocks; (b) snow blocks previously placed by other generation
(including another igloo's own shell). The 1.7.10 igloo was, by construction, a rare
biome-border structure — NOT a common Ice Plains structure. The DSB type-20 path
(§9) works everywhere regardless.

---

## 2. Geometry — per-loop tables

All six shell loops iterate `curdeg = 0 → <360` in float steps (exactly representable —
no accumulation drift) and rasterize a circle of radius `currad` via the float idiom
(shared plumbing). Cell lists below are the canonical positive-coordinate rasterization
(computed with Java float semantics); `(dx, dz)` relative to origin. Write order matters
(§2.5, §2.8).

### 2.1 Loop 1 — dome wall, radius 6, 5° steps (GD:2705-2711)

72 iterations → **40 unique cells**; per visited cell, three stacked writes:
`y+1` snow block (GD:2708), `y+2` ice (GD:2709), `y+3` snow block (GD:2710).

```
(6,0) (6,1) (6,2) (5,3) (5,4) (4,4) (4,5) (3,5) (2,6) (1,6) (0,6) (-1,6) (-2,6)
(-3,5) (-4,5) (-4,4) (-5,4) (-5,3) (-6,2) (-6,1) (-6,0) (-6,-1) (-6,-2) (-5,-3)
(-5,-4) (-4,-4) (-4,-5) (-3,-5) (-2,-6) (-1,-6) (0,-6) (1,-6) (2,-6) (3,-5) (4,-5)
(4,-4) (5,-4) (5,-3) (6,-2) (6,-1)
```

### 2.2 Loop 2 — setback ring, radius 5, 5° steps, `y+4` ice (GD:2712-2717)

**36 unique cells** (single course):

```
(5,0) (5,1) (5,2) (4,3) (4,4) (3,4) (2,5) (1,5) (0,5) (-1,5) (-2,5) (-2,4) (-3,4)
(-4,4) (-4,3) (-5,2) (-5,1) (-5,0) (-5,-1) (-5,-2) (-4,-2) (-4,-3) (-4,-4) (-3,-4)
(-2,-4) (-2,-5) (-1,-5) (0,-5) (1,-5) (2,-5) (3,-4) (4,-4) (4,-3) (4,-2) (5,-2) (5,-1)
```

(Rasterization is mildly asymmetric — 0.5-boundary rounding, e.g. `(-2,4)` AND `(-2,5)`
both present on the NW arc. Faithful; transcribe, do not "fix".)

### 2.3 Loops 3-6 — flat roof cap at `y+5`, four concentric rings (GD:2718-2741)

| Loop | Radius | Step | Block | Cite | Unique cells |
|---|---|---|---|---|---|
| 3 | 4 | 5° | snow block | GD:2718-2723 | 32 |
| 4 | 3 | 10° | ice | GD:2724-2729 | 20 |
| 5 | 2 | 15° | snow block | GD:2730-2735 | 16 |
| 6 | 1 | 15° | ice | GD:2736-2741 | 8 |

Cell lists:

```
r=4 snow: (4,0) (4,1) (4,2) (3,2) (3,3) (2,3) (2,4) (1,4) (0,4) (-1,4) (-2,4) (-2,3)
          (-3,3) (-3,2) (-4,2) (-4,1) (-4,0) (-4,-1) (-4,-2) (-3,-2) (-3,-3) (-2,-3)
          (-2,-4) (-1,-4) (0,-4) (1,-4) (2,-4) (2,-3) (3,-3) (3,-2) (4,-2) (4,-1)
r=3 ice:  (3,0) (3,1) (3,2) (2,2) (2,3) (1,3) (0,3) (-1,3) (-2,2) (-3,2) (-3,1) (-3,0)
          (-3,-1) (-2,-2) (-1,-3) (0,-3) (1,-3) (2,-3) (2,-2) (3,-1)
r=2 snow: (2,0) (2,1) (1,1) (1,2) (0,2) (-1,2) (-1,1) (-2,1) (-2,0) (-2,-1) (-1,-1)
          (-1,-2) (0,-2) (1,-2) (1,-1) (2,-1)
r=1 ice:  (1,0) (1,1) (0,1) (-1,1) (-1,0) (-1,-1) (0,-1) (1,-1)
```

Cap overwrites (later loop wins): loop 4 (ice) overwrites loop 3's snow at **(3,2),
(−3,2), (2,3), (2,−3)**; loop 6 (ice) overwrites loop 5's snow at **(1,1), (−1,1),
(−1,−1), (1,−1)**. The cap covers every cell of the disc except **(0,0) — a permanent
1×1 skylight hole at the dome apex** (no loop ever writes dx=dz=0; r=1's nearest
approach is the 8-cell ring). See S3.

### 2.4 Doorway + door, west wall (GD:2742-2745)

Door column X = `(int)((float)cposx − 6.0f + 0.5f)` = canonical `cposx − 6`, Z =
`(int)((float)cposz + 0.5f)` = canonical `cposz` — the same float idiom as the shell, so
it shifts WITH the shell on negative axes (§2.7). That column is wall cell `(−6, 0)`
(snow/ice/snow at y+1..y+3, §2.1):

| # | Cell (rel) | Write | Cite |
|---|---|---|---|
| 1 | `(−6, 0, 0)` | oak planks (`field_150344_f` meta 0 via FastSetBlock) — buried sill | GD:2742 |
| 2 | `(−6, +1, 0)` | air (clears wall snow) | GD:2743 |
| 3 | `(−6, +2, 0)` | air (clears wall ice) | GD:2744 |
| 4 | `(−6, +1..+2, 0)` | `ItemDoor.func_150924_a(world, x, cposy+1, z, 2, field_150466_ao)` — oak door, direction 2 = **facing west** (outward) | GD:2745 |

Vanilla `ItemDoor.func_150924_a` (placeDoorBlock; vanilla 1.7.10 helper, NOT in the
reference tree) writes bottom half `meta = 2` and top half `meta = 8 | hinge`, choosing
the hinge by READING the two flanking columns (door z±1, at door-y and door-y+1) for
solidity/same-block — then fires `func_147459_d` neighbor notifications on both halves.
Classification (§10): the flanking columns are this builder's own wall cells `(−6, ±1)`
(snow y+1 counts as normal-cube, ice y+2 does not — translucent material), so both sides
tie (1 = 1) and both same-block checks fail → **hinge = LEFT, deterministically, from the
write set alone**. Port: model in memory — place
`minecraft:oak_door[facing=west, hinge=left, open=false, powered=false]` lower at
`(−6,+1,0)` and upper at `(−6,+2,0)` via `piece.place`; no world read, no neighbor
notify needed (the door sits on the solid plank sill, so vanilla's pop-off check is moot).
The wall snow at `(−6, +3, 0)` stays — the doorway lintel (see S2).

### 2.5 Spawners (GD:2746-2760) — interior, at surface level

Three spawners at `y+1`, each `func_147465_d(..., field_150474_ac, 0, 2)` +
`func_98272_a(name)`. Plain int arithmetic — these do NOT shift on negative axes (S5).

| # | Position (rel) | Mob name | Cite |
|---|---|---|---|
| 1 | `(+2, +1, −4)` | `"Rat"` | GD:2746-2750 |
| 2 | `(−1, +1, +1)` | `"Ghost"` | GD:2751-2755 |
| 3 | `(+3, +1, +4)` | `"Ghost Pumpkin Skelly"` | GD:2756-2760 |

All three are strictly inside the radius-6 wall (distances 4.47 / 1.41 / 5.0). At the
worldgen anchor (`cposy = posY − 2`), `y+1` IS the terrain surface-block level, so each
spawner replaces a surface snow block, flush with the floor (S4).

### 2.6 Chest (GD:2761-2812)

One chest at `(−3, +1, −3)` (inside, distance 4.24), placed `func_147465_d(...,
field_150486_ae, 2, 2)` — **meta 2 = facing north** (GD:2761) — then filled slot-by-slot
with 16 independent 50% rolls (§3). Port: `piece.placeLootChest` with the loot table of
§3; if the helper supports a facing, use `minecraft:chest[facing=north]`.

### 2.7 Negative-coordinate truncation drift (shell + door only)

`(int)` truncation toward zero: on a NEGATIVE axis, every non-integral float sum lands one
cell toward zero relative to the canonical (floor) table — in practice the whole shell,
doorway, plank, and door translate **+1 on each negative axis** (per-axis; mixed-sign
quadrants shift on the negative axis only). The spawners and chest (plain int math,
§2.5-2.6) do NOT shift, so the fixtures sit one block off-center inside the shell in
negative quadrants — faithful 1.7.10 behavior, reproduced automatically by verbatim
transcription (shared plumbing). Footprint (§6) covers the union of both cases.

### 2.8 Net shape + write-order notes

A hollow stepped snow dome: 3-course wall (snow/ice/snow, y+1..+3) of radius 6, ice
setback ring radius 5 at y+4, flat cap at y+5 of alternating snow/ice rings with a 1×1
apex skylight; oak door facing west in the wall at `(−6, 0)` over a buried plank sill;
NO floor and NO interior clearing — the interior is untouched terrain (at worldgen: snow
surface at y+1 level, natural air above); one Rat + one Ghost + one Ghost Pumpkin Skelly
spawner sunk flush into the floor; one north-facing kit chest. Order-sensitive
overwrites: cap ice-over-snow cells (§2.3) and doorway air/door over wall cells (§2.4)
— keep the original loop order (or place door + cap ice last; same result).

---

## 3. Loot — FULL transcription

**No `WeightedRandomChestContent` list.** The chest is filled by 16 INDEPENDENT
`world.field_73012_v.nextInt(2) == 0` rolls (50% each), each into a FIXED slot
(GD:2763-2812). Slot 12 is deliberately skipped — the list is the InstantShelter kit
(orig IS:121-134, port `item/InstantShelter.java:107-120`) whose slot 12 is the Ore Salt
block; the igloo omits it and appends gold nuggets. Weight totals: **n/a** (no weighted
pool); expected stacks = 8, min 0, max 16.

| # | Slot | 1.7.10 item (qty) | Modern / port mapping (cite) | Chance |
|---|---|---|---|---|
| 1 | 0 | `Items.field_151111_aL` ×1 | `minecraft:compass` (IS port :107) | 1/2 (GD:2764-2766) |
| 2 | 1 | `Items.field_151148_bJ` ×1 (empty map) | `minecraft:map` (IS port :108; alien.json:15) | 1/2 (GD:2767-2769) |
| 3 | 2 | `Items.field_151157_am` ×8 | `minecraft:cooked_porkchop` — NOTE: the COOKED field; the shelter's slot 2 uses raw `field_151147_al` → the two lists differ here | 1/2 (GD:2770-2772) |
| 4 | 3 | `Blocks.field_150478_aa` ×32 | `minecraft:torch` (IS port :110) | 1/2 (GD:2773-2775) |
| 5 | 4 | `Items.field_151044_h` ×16 | `minecraft:coal` (IS port :111) | 1/2 (GD:2776-2778) |
| 6 | 5 | `Items.field_151104_aV` ×1 | `minecraft:red_bed` (flattening default; IS port :112) | 1/2 (GD:2779-2781) |
| 7 | 6 | `Items.field_151104_aV` ×1 | `minecraft:red_bed` | 1/2 (GD:2782-2784) |
| 8 | 7 | `Items.field_151135_aq` ×1 (wooden door item, D4 report :79) | `minecraft:oak_door` (IS port :114) | 1/2 (GD:2785-2787) |
| 9 | 8 | `Items.field_151035_b` ×1 | `minecraft:iron_pickaxe` (IS port :115) | 1/2 (GD:2788-2790) |
| 10 | 9 | `Items.field_151040_l` ×1 | `minecraft:iron_sword` (IS port :116) | 1/2 (GD:2791-2793) |
| 11 | 10 | `Items.field_151036_c` ×1 | `minecraft:iron_axe` (IS port :117) | 1/2 (GD:2794-2796) |
| 12 | 11 | `Items.field_151133_ar` ×1 | `minecraft:bucket` (IS port :118) | 1/2 (GD:2797-2799) |
| 13 | 13 | `Blocks.field_150486_ae` ×1 | `minecraft:chest` (IS port :120) | 1/2 (GD:2800-2802) |
| 14 | 14 | `Items.field_151074_bl` ×6 | `minecraft:gold_nugget` (play_pool spec :113) | 1/2 (GD:2803-2805) |
| 15 | 15 | `Items.field_151074_bl` ×8 | `minecraft:gold_nugget` | 1/2 (GD:2806-2808) |
| 16 | 16 | `Items.field_151074_bl` ×10 | `minecraft:gold_nugget` | 1/2 (GD:2809-2811) |

→ `RES:loot_table/chests/igloo.json`: **16 pools**, each `rolls: 1`, one item entry with
`minecraft:random_chance 0.5` (fixed `set_count` where qty > 1). This reproduces the
independent-Bernoulli distribution exactly; only the fixed SLOT POSITIONS (and the
empty-slot-12 look) are lost to the loot system's sequential fill — documented
approximation, same family as the pattern §1 step 5 slot-collision note (here there are
no collisions to begin with).

---

## 4. Spawner / mob mapping table

| Spawner name | 1.7.10 registration | Port EntityType (cite) |
|---|---|---|
| `"Rat"` (GD:2749) | `Rat.class` — `registerGlobalEntityID` OSM:4197, `registerModEntity` OSM:4201 | `ModEntities.ENTITY_RAT` "rat" (ModEntities.java:250-252) |
| `"Ghost"` (GD:2754) | `Ghost.class` — OSM:4047, OSM:4051 | `ModEntities.GHOST` "ghost" (ModEntities.java:549-551) |
| `"Ghost Pumpkin Skelly"` (GD:2759) | `GhostSkelly.class` — OSM:4055, OSM:4059 | `ModEntities.GHOST_SKELLY` "ghost_skelly" (ModEntities.java:555-557) |

No direct entity spawns — spawner blocks only.

---

## 5. Block palette

| 1.7.10 field (meta) | Modern block | Used for | Cite |
|---|---|---|---|
| `Blocks.field_150433_aE` | `minecraft:snow_block` (the FULL block — not `snow` the layer) | wall courses y+1/y+3, cap rings r4/r2 | GD:2708/2710, 2722, 2734; scan OSW:1270 |
| `Blocks.field_150432_aD` | `minecraft:ice` | wall course y+2, setback ring y+4, cap rings r3/r1 | GD:2709, 2716, 2728, 2740 |
| `Blocks.field_150344_f` meta 0 | `minecraft:oak_planks` | door sill at y+0 | GD:2742 |
| `Blocks.field_150350_a` | `minecraft:air` | doorway punch y+1..+2 | GD:2743-2744 |
| `Blocks.field_150466_ao` | `minecraft:oak_door` (facing=west, hinge=left, §2.4) | door | GD:2745 |
| `Blocks.field_150474_ac` | `minecraft:spawner` | 3 spawners | GD:2746/2751/2756 |
| `Blocks.field_150486_ae` meta 2 | `minecraft:chest[facing=north]` | 1 loot chest | GD:2761 |

All builder writes are flag-2 (no neighbor updates) except the original door helper's two
trailing neighbor notifications (§2.4) — the port drops them (safe: solid sill below).

---

## 6. Footprint extents (relative to origin) + suggested DungeonType

| Axis | Min | Max | Extent | Determined by |
|---|---|---|---|---|
| X | `−6` (canonical) / `−5`..`+7` (negative-axis drift) → union `−6..+7` | — | 14 | wall r=6 (GD:2705-2708) + §2.7 +1 drift |
| Y | `0` | `+5` | **6** | door sill y+0 (GD:2742) / roof cap y+5 (GD:2722) |
| Z | union `−6..+7` (same as X) | — | 14 | wall r=6 + §2.7 |

Suggested entry (asymmetric 6-int ctor `(minXOff, maxXOff, down, up, minZOff, maxZOff)`,
+1 margin; `down 1` covers the y+0 sill with margin):

```java
IGLOO(-7, 8, 1, 6, -7, 8, PlacementMode.SNOW_SURFACE_MINUS2),  // NEEDS_NEW_MODE — and see §7 NEEDS_DESIGN_RULING
```

---

## 7. Placement — NEEDS_NEW_MODE, and **NEEDS_DESIGN_RULING** on the biome/frequency decision

### 7.1 The original scan, quoted in full (OSW:1265-1275)

```java
for (int i = 0; i < 4; ++i) {
    int posX = chunkX + random.nextInt(16);
    int posZ = chunkZ + random.nextInt(16);
    boolean which = false;
    for (int posY = 100; posY > 40; --posY) {
        if (world.func_147439_a(posX, posY, posZ) != Blocks.field_150350_a
            || world.func_147439_a(posX, posY - 1, posZ) != Blocks.field_150433_aE) continue;
        OreSpawnMain.MyDungeon.makeIgloo(world, posX, posY - 2, posZ);
        recently_placed = 50;
        return true;
    }
}
```

### 7.2 Mechanical mode (covered by the pattern)

None of the eight existing modes fits: `SWAMP_GRASS_SURFACE` (LegacyDungeonStructure.java:
249-267) is byte-identical in scan SHAPE (4 attempts, `chunk + nextInt(16)` jitter,
Y 100→41 window, block-below identity test) but anchors at `firstFree` — the igloo anchors
at **`firstFree − 2`** — and its doc/tag are swamp-specific. Suggested new mode
`SNOW_SURFACE_MINUS2` (enum constant + `findGenerationPoint` case, pattern §1 step 4):
4 attempts of `chunk + nextInt(16)` jitter; accept `41 ≤ firstFree ≤ 100` (the
`posY > 40` window is behavior — trap 7) AND dry column (`WORLD_SURFACE_WG ==
OCEAN_FLOOR_WG`, the established block-below approximation); anchor = `firstFree − 2`.

### 7.3 The uncovered decision — **NEEDS_DESIGN_RULING, stopping here per the pattern authority**

Every prior biome-tag conversion (swamp grass, ocean water, end stone) mapped a scan
whose target block IS the gate biome's normal surface, so "biome tag + dry column"
carried the original's selectivity. The igloo is the first structure where the gate biome
("Ice Plains") and the scanned surface (snow BLOCK) are mutually inconsistent (§1.3):
faithful 1.7.10 frequency is ~zero inside plain Ice Plains and concentrated on Ice
Plains Spikes borders. The candidate resolutions change player-visible frequency by
orders of magnitude, and the pattern doc has no rule for choosing:

- (a) tag `minecraft:snowy_plains` (the exact-name mapping of "Ice Plains", spit-bug
  style) + dry-column approximation → igloos become a NORMAL snowy-plains structure —
  frequency the original never had (invented-behavior risk, same family as the
  NightmareDungeon trap);
- (b) tag `minecraft:ice_spikes` → matches the terrain the original actually accepted
  (snow-block surfaces) but contradicts the exact-name corner gate, which EXCLUDED
  "Ice Plains Spikes";
- (c) both tags with odds discounted toward the border-artifact rate — no C7 equivalence
  exists for "biome-border corner mismatch" frequency.

Also flagged in §1.3: the underlying vanilla-terrain claim needs verification against
1.7.10 vanilla (not in the reference tree). Geometry, loot, mobs, DSB wiring (§9), and
the mode mechanics (§7.2) are unaffected — only the structure/biome-tag/structure-set
JSON trio waits on this ruling.

---

## 8. Structure-set conversion (provisional numbers, pending §7.3)

Gate odds 1/220 per qualifying chunk (OSW:1260) → C7 sqrt equivalence: spacing ≈ √220 ≈
14.8 → **spacing 15, separation 7** — valid only under resolution (a)/(b); resolution (c)
re-derives spacing from the chosen effective odds. **Salt 84356** (assigned to this task;
grep of `RES:worldgen/structure_set/*.json` on extraction date: highest in use 84354
(spit_bug_lair), batch-1 assignments 84350-84355, known 84312 collision logged in D5 —
84356 free). `"step": "surface_structures"`, `"terrain_adaptation": "none"`,
`"spawn_overrides": {}` (trap 8).

---

## 9. DungeonSpawnerBlock outcome

- Original: `if (type == 20)` → `makeIgloo(world, clickedX, clickedY, clickedZ)` — one
  call, no −2 offset, block read in full (DSB:113-115).
- Port: add `TYPE_IGLOO = 20` (cite DSB:113-115) and
  `case TYPE_IGLOO -> { LegacyDungeonPiece.buildNow(server, pos, DungeonType.IGLOO); yield true; }`
  in `src/main/java/danger/orespawn/block/entity/RandomDungeonSpawnerBlockEntity.buildForType`.
  **Type 20 currently falls through to the generic-dungeon `default`**
  (RandomDungeonSpawnerBlockEntity.java:219). The DSB path bypasses biome, scan, AND the
  −2 sink — a shell perched on or embedded in whatever terrain surrounds the clicked
  position, with un-cleared interior, is faithful behavior (same ruling as
  FairyTree/GoldFishBowl). This case is NOT blocked by §7.3 — it can be wired
  immediately once the DungeonType exists.

---

## 10. Mid-build world READS, classified

1. TE fetches after spawner/chest writes (`getSpawnerTileEntity` GD:2747/2752/2757,
   `getChestTileEntity` GD:2762 — both plain `func_147438_o`): **self-reads** of blocks
   just written; absorbed by `piece.placeSpawner` / `piece.placeLootChest`.
2. `ItemDoor.func_150924_a`'s hinge probes (§2.4): reads the two flanking columns at
   door-y/door-y+1 — all four cells are THIS builder's wall writes (snow y+1 / ice y+2 at
   `(−6, ±1)`), so the result (hinge LEFT) is derivable from the write set: **self-read →
   modeled in memory as a constant** (BasiliskMaze-style, pattern §1 step 3 rule 2).
3. **No pre-build terrain reads.** There is no `func_147439_a` anywhere in GD:2698-2813;
   the un-cleared interior "floor" is whatever terrain exists — the builder never
   inspects it.

## 11. RNG stream

Inside the builder: exactly **16 draws**, all `world.field_73012_v.nextInt(2)` chest-slot
gates (GD:2764-2810), all moving into the loot JSON's `random_chance` conditions (§3).
The ported generator therefore consumes **zero** random draws — geometry, door, spawner
mobs, and chest position are constants — and every per-chunk replay pass is trivially
identical. Dispatch-layer rolls (OSW:1260 gate on the chunk `random`, DSB:52 on
`world.rand`) map to structure-set frequency / the DSB roll as usual.

## 12. Surprises / MISSING-IN-PORT

- **S1 (MISSING-IN-PORT)**: grep `Igloo|igloo` over `src/main/` — zero matches. No
  DungeonType, no generator, no JSON trio, no loot table; DSB type 20 builds a generic
  dungeon via the fallback today (RandomDungeonSpawnerBlockEntity.java:219).
- **S2 (crouch-height doorway)**: at the worldgen anchor the interior/exterior surface
  block sits at y+1 (the −2 sink), so a standing player occupies cells y+2 AND y+3 — but
  the doorway punch clears only y+1..y+2 (GD:2743-2744) and the wall snow at `(−6,+3,0)`
  (the lintel) is never removed. The 2-tall door is sunk one block into the ground:
  effective clearance above the floor is ONE block, and entering upright requires
  breaking the lintel (or the door swallows the bottom cell below foot level). Faithful —
  do not "fix" by raising the door or clearing y+3.
- **S3 (apex skylight)**: no loop writes `(0,0)` at y+5 (§2.3) — the dome always has a
  1×1 chimney hole at the apex. Snow/rain falls through; faithful.
- **S4 (no floor, no interior clearing)**: the builder writes no floor and clears no
  interior air; spawners and chest replace surface blocks flush with the terrain floor
  (worldgen) or embed wherever the DSB anchor puts them. The plank sill (y+0) is buried
  one block below the surface.
- **S5 (quadrant drift)**: the float `(int)`-truncation idiom shifts the SHELL + DOOR +1
  per negative axis while the int-math spawners/chest stay fixed (§2.7) — interior
  fixture offsets differ per world quadrant. Verbatim transcription (CrystalStructures
  precedent) reproduces it bit-identically; the §6 box covers the union.
- **S6 (kit-chest sibling)**: the chest list is the InstantShelter kit with slot 12 (Ore
  Salt) omitted, slot 2 swapped raw→cooked porkchop, and three gold-nugget slots
  appended (§3) — 16 independent 50% rolls, NOT a weighted list; no
  `WeightedRandomChestContent` anywhere in the method.
- **S7 (near-dead worldgen path)**: the "Ice Plains" corner gate + snow-BLOCK-below scan
  are mutually inconsistent on plain Ice Plains terrain (§1.3) — natural igloos were a
  rare biome-border artifact. The port's biome-tag/frequency decision is
  **NEEDS_DESIGN_RULING** (§7.3); geometry/loot/DSB porting is not blocked.
- **S8 (door helper side effects)**: `ItemDoor.func_150924_a` is the only non-flag-2
  write path (two neighbor notifications) and the only in-method world-read besides TE
  fetches; the port replaces it with two modeled `piece.place` door-half writes
  (facing=west, hinge=left, §2.4).
- **S9**: chest facing north via block meta 2 (GD:2761) — the one non-zero block meta in
  the method besides the door's; `FastSetBlock` hardcodes meta 0 for everything else.
- **S10**: dead local `boolean which` (OSW:1268) — ignore. The igloo (unlike its chain
  siblings) has no LessLag involvement and no `D4BigSpaceCheck`/`quickSpaceCheck` probe.

> **S3 CORRECTION (D6b batch-2 verification, 2026-08-08):** the "permanent 1x1
> apex skylight" claim is quadrant-dependent, not unconditional. Float32
> simulation of the original idiom at all four origin sign quadrants shows the
> r=1 (and at (-,-) origins the r=2) roof ring lands cells ON the apex whenever
> origin x or z is negative — the skylight exists only at (+,+) origins. The
> port reproduces the original bit-for-bit in every quadrant; only this spec
> claim and the generator's initial Javadoc were wrong (both corrected).
