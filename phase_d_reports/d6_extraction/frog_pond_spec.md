# Frog Pond — Extraction Spec (1.7.10 → NeoForge 1.21.1)

Source of truth: `reference_1_7_10_source/sources/danger/orespawn/GenericDungeon.java` —
`makeFrogPond` (GD:6018-6039; next method `makePumpkin` at GD:6041). Method read IN FULL,
including the shared helper it uses (`getSpawnerTileEntity` GD:86-95). All coordinates are
relative to the build origin `(cposx, cposy, cposz)` = the three int args. One of the
smallest builders in the file: 1 spawner + 58 block writes, **no loops beyond one 7×7
double loop, no RNG, no chest, no loot, no direct entity spawns**.

Citation convention: `GD:NN` = GenericDungeon.java line NN, `OSW:NN` = OreSpawnWorld.java,
`OSM:NN` = OreSpawnMain.java, `DSB:NN` = DungeonSpawnerBlock.java, `RDS:NN` =
`src/main/java/danger/orespawn/block/entity/RandomDungeonSpawnerBlockEntity.java`,
`LDP:NN` = `src/main/java/danger/orespawn/world/structure/LegacyDungeonPiece.java`,
`LDS:NN` = `src/main/java/danger/orespawn/world/structure/LegacyDungeonStructure.java`.

Header traps (details in the numbered sections):

- **The two call sites hand DIFFERENT anchors**: worldgen passes `posY - 1` (the GRASS
  block — the pond top layer REPLACES the grass, flush with terrain, OSW:1168); the DSB
  passes `clickedY + 1` (one ABOVE the clicked block — the pond hovers a block up,
  DSB:183). Both exact, both faithful; document-don't-reconcile (§1, §9, S5).
- This builder never calls `OreSpawnMain.setBlockFast`. The spawner is a direct
  `func_147465_d(..., 0, 2)` (flag 2, GD:6020); **every other write is flag 3**
  (`func_147465_d(..., 0, 3)`, GD:6027-6038) — update + notify, deliberately starting the
  water flowing. Non-issue in the port (play_pool_spec.md S3 precedent): modern fluids
  self-schedule ticks on placement, so `piece.place` flag-2 writes behave identically (S3).
- `Blocks.field_150358_i` (flowing_water) at meta 0 flattens to the modern
  `minecraft:water` SOURCE state — same mapping the Play Pool shipped (play_pool_spec.md
  §5). The four "flowing" cross cells are sources after settling in 1.7.10 too (§5, S4).
- Builder draws **zero** randomness (GD:6018-6039 contains no `nextInt`/`rand` of any
  kind) — the RNG stitching contract is satisfied trivially (§11).
- **No chest, no loot** — §3 is intentionally empty. Do not invent a loot table (S2).

---

## 1. Entry points — EVERY call site

Grep of the whole original tree for `makeFrogPond`: exactly two call sites (OSW:1168 and
DSB:183; GD:6018 is the definition; the old pre-audit copy at
`src/danger/orespawn/GenericDungeon.java:6035` / `OreSpawnWorld.java:1186` /
`DungeonSpawnerBlock.java:193` mirrors the same three and is not shipped code).

| Caller | Signature used | Coords passed | Notes |
|---|---|---|---|
| `OreSpawnWorld.addFrogPond` (OSW:1168) | `OreSpawnMain.MyDungeon.makeFrogPond(world, posX, posY - 1, posZ)` | scan hit **minus one — cposy is the GRASS BLOCK itself**, the air is at posY | worldgen, overworld Plains only (§1.2) |
| `DungeonSpawnerBlock` type **43** (DSB:182-184) | `...makeFrogPond(world, clickedX, clickedY + 1, clickedZ)` | player-placed block pos **plus one** | roll `type = world.field_73012_v.nextInt(50)` (DSB:52), fires 400 ticks after placement (DSB:39 via `func_149674_a` DSB:46), self + block above deleted first (DSB:50-51). The `if (type == 43)` block read IN FULL: **single builder call, nothing else** (DSB:182-184). Part of the `clickedY + 1` outlier trio 43/44/45 (FrogPond DSB:183, Pumpkin DSB:186, RoundRotator DSB:189) — every other type passes `clickedY` unmodified (full dispatch DSB:52-202 re-verified, 50 blocks / 50 single calls). |

### 1.1 `addFrogPond` — FULL method + return contract (OSW:1156-1174)

```java
// OSW:1156-1174
public void addFrogPond(World world, Random random, int chunkX, int chunkZ) {
    if (random.nextInt(350) != 0) {
        return;
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
                OreSpawnMain.MyDungeon.makeFrogPond(world, posX, posY - 1, posZ);
                recently_placed = 50;
                return;
            }
        }
    }
}
```

1. Rarity gate: `random.nextInt(350) != 0 → return` (OSW:1157-1159) — 1/350, chunk-provided
   `random`.
2. Corner-biome gate: exact name `"Plains"` (OSW:1160-1161) — one biome, no Sunflower
   Plains, no hills variants.
3. Up to **4 attempts** (OSW:1162): `posX/posZ = chunk + random.nextInt(16)` (OSW:1163-1164).
   Dead local `boolean which = false` (OSW:1165) — decompiler artifact, never read (same as
   play_pool_spec.md S6); do not port.
4. Column scan `posY = 100` down to `41` inclusive (`posY > 40`, OSW:1166): require **air AT
   `posY` AND grass block at `posY - 1`** (OSW:1167). Non-matching layers just `continue`
   the scan (it can walk down through tree canopies).
5. First hit → `makeFrogPond(world, posX, posY - 1, posZ)` — **anchor = the grass block**,
   `recently_placed = 50`, `return` (OSW:1168-1170). A failed column falls through to the
   next attempt; 4 misses → method ends.

**FULL return contract: the method is `void`** — there is nothing for a caller to branch
on (contrast the `boolean` siblings `addLeafMonster` OSW:1196 / `addRubberDuckyPond`
OSW:1217, and the WGEN-062 early-`true` hazard class). Its only observable effects are the
build itself plus `recently_placed = 50` on success (OSW:1169); gate fail, biome fail, and
4 scan misses leave no state behind. The dispatch site (OSW:301-303) ignores it and is NOT
part of the `ahh` fallback chain (OSW:304-321), so nothing downstream depends on
success/failure.

### 1.2 Worldgen dispatch chain (complete)

- `generateSurface` (OSW:274-328): the shared overworld dungeon-picker block is gated by
  `OreSpawnMain.DisableOverworldDungeons == 0 && world.field_73011_w.field_76574_g == 0 &&
  recently_placed == 0` (OSW:284) — **overworld only**; the only other path is DSB type 43.
- 6-way picker: `i = world.field_73012_v.nextInt(6)` (OSW:285, WORLD rand not chunk
  random); **`i == 5` → `addFrogPond` (OSW:301-303)** — single-outcome slot. Siblings:
  i==0 play pool, 1 water dragon lair, 2 gold fish bowl, 3 girlfriend island, 4 monster
  island (OSW:286-300).
- Effective odds: 1/6 × 1/350 = **1/2100 per overworld chunk** before the Plains gate and
  scan success — identical arithmetic to Play Pool / Gold Fish Bowl / Water Dragon Lair
  (their sets shipped at spacing 46/23).
- `recently_placed` 50-chunk cooldown + the Plains/scan rejection map onto structure-set
  spacing/separation + biome tag (pattern §1 step 4, C7-approved).
- `DisableOverworldDungeons` gate: same treatment as the shipped siblings — the config
  applies to the whole rotation, ported at the chunk-generator/config level (see
  `OreSpawnChunkGenerator.java:717` note), not per-structure.

---

## 2. Geometry — rule by rule (source order; all ranges inclusive)

Everything is written unconditionally — no conditions, no draws. 5 write groups, 59 writes
total. Origin `(0,0,0)` = `(cposx, cposy, cposz)`.

| # | What | Cells (rel) | Block / flag | Cite |
|---|---|---|---|---|
| 1 | Spawner (written FIRST, before the water below it) | `(0, +2, 0)` | `field_150474_ac` mob spawner, **flag 2**, then TE fetch + `func_98272_a("Frog")` (skipped silently if TE null) | GD:6020-6024 |
| 2 | Pond sheet, 7×7 | `(i, 0, j)`, `i, j ∈ −3..+3` — **loop var `j` is the Z offset** (7×7 square, so no transposition risk) | still water `field_150355_j`, **flag 3** — 49 writes replacing whatever terrain layer is at cposy | GD:6025-6029 |
| 3 | Center riser | `(0, +1, 0)` | still water, flag 3 — a 1-block fountain bump above the sheet, directly beneath the spawner | GD:6030 |
| 4 | Flow cross at +1 | `(−1, +1, 0)`, `(+1, +1, 0)`, `(0, +1, −1)`, `(0, +1, +1)` | flowing water `field_150358_i` meta 0, flag 3 (4 writes) | GD:6031-6034 |
| 5 | Lily-pad cross at +2 | `(−1, +2, 0)`, `(+1, +2, 0)`, `(0, +2, −1)`, `(0, +2, +1)` | lily pad `field_150392_bi`, flag 3 (4 writes) — each sits on a group-4 water cell, ringing the spawner | GD:6035-6038 |

Net shape (worldgen anchor): a 7×7 water pond flush with the Plains grass (the grass top
layer is replaced), a plus-shaped water mound one block high at the center, four lily pads
floating on the mound around a mob spawner hovering at +2 on top of the center water
column. The +1 water sources spill outward over the sheet and off the rim after
placement — generated behavior in both versions (S9). Nothing is dug below the surface
(the sheet IS the surface layer) and nothing outside X/Z ±3 is written.

---

## 3. Loot — FULL transcription

**NONE.** `makeFrogPond` places no chest and touches no `WeightedRandomChestContent` list
(GD:6018-6039 read in full — the only tile entity is the spawner). No loot-table JSON is
created for this structure; do not invent one (S2).

---

## 4. Mob / entity tables

### 4.1 Spawner

| Spawner name | 1.7.10 registration | Port EntityType (cite) |
|---|---|---|
| `"Frog"` (GD:6023) | `Frog.class` — `registerGlobalEntityID(..., "Frog", FrogID)` OSM:4465, `registerModEntity` OSM:4469 | `ModEntities.FROG` "frog" (ModEntities.java:336-338) |

Spawn-gate interplay (note only, no change): port `Frog.checkSpawnRules` = orig
Frog.java:240-251 — `y >= 50`, daytime, ≤5 buddies within 20/8/20, extra 1-in-20 dice in
Crystal (Frog.java:277-285) — with **no near-spawner bypass** (unlike StinkBug). The
placement window is Y 41..100 (OSW:1166), so a pond anchored below Y50 has a dormant
spawner **in 1.7.10 and in the port alike** — faithful; do not "fix" (S8).

### 4.2 Direct entity spawns

**None.** No `func_72838_d`/`spawnEntityInWorld` anywhere in GD:6018-6039 — no position
arithmetic, yaw, NBT, or persistence semantics to extract.

---

## 5. Block palette

| 1.7.10 field | Modern block | Used for | Cite |
|---|---|---|---|
| `Blocks.field_150474_ac` | `minecraft:spawner` | 1 spawner (flag 2) | GD:6020 |
| `Blocks.field_150355_j` (meta 0) | `minecraft:water` (source, default state) | 7×7 sheet + center riser (flag 3) | GD:6027, 6030 |
| `Blocks.field_150358_i` (meta 0) | `minecraft:water` (source) — the 1.13 flattening maps both 1.7.10 water blocks at meta 0 to the source state; a meta-0 dynamic block settles into a static source anyway (play_pool_spec.md §5 precedent) | flow cross at +1 (flag 3) | GD:6031-6034 |
| `Blocks.field_150392_bi` | `minecraft:lily_pad` | lily cross at +2 (flag 3) | GD:6035-6038 |
| (placement scan only) `Blocks.field_150350_a` / `field_150349_c` | air / grass block | anchor test | OSW:1167 |

Lily-pad survival: modern `WaterlilyBlock.canSurvive` wants a water source (or ice) below;
the cell below each pad is a group-4 water source in the port, so the pads survive the
post-placement fluid updates. The initial flag-2 structure write bypasses the check, same
as 1.7.10's flag-3 `func_147465_d` did.

---

## 6. Footprint extents + suggested DungeonType

Relative to `(cposx, cposy, cposz)`:

| Axis | Min | Max | Extent | Determined by |
|---|---|---|---|---|
| X | `−3` | `+3` | **7** | pond sheet `i ∈ −3..+3` (GD:6025) |
| Y | `0` | `+2` | **3** | sheet at cposy (GD:6027); spawner/lilies at +2 (GD:6020, 6035-6038) — the sheet REPLACES the terrain layer at origin height, nothing below it |
| Z | `−3` | `+3` | **7** | pond sheet `j ∈ −3..+3` (GD:6026) |

Suggested entry (asymmetric 6-int ctor `(xMin, xMax, down, up, zMin, zMax, mode)`, +1
margin each side; `down = 1` = one margin block below the lowest write at rel 0,
MONSTER_ISLAND's margin convention, LDP:161):

```java
FROG_POND(-4, 4, 1, 3, -4, 4, PlacementMode.SAND_SURFACE_MINUS1),
```

Post-placement water flow runs past the box — irrelevant to the envelope (fluid ticks are
not structure writes; PLAY_POOL's cascade precedent, LDP:162-166).

## 7. Placement — **existing mode fits: `SAND_SURFACE_MINUS1`** (no new mode needed)

`addFrogPond`'s anchoring (§1.1) is byte-identical in shape to `addBouncyCastle`'s
(OSW:1280-1299), which `SAND_SURFACE_MINUS1` ports (LDS:81-89 →
`swampGrassSurfaceOrigin` LDS:266-284 `.below()`): corner-biome gate → the structure's
biome tag, 4 attempts of `chunk + nextInt(16)` jitter (LDS:268-270 ← OSW:1162-1164),
air-over-solid scan Y 100→41 as the hard window on `getBaseHeight` (LDS:271-275 ←
OSW:1166), the dry-column approximation (`WORLD_SURFACE_WG == OCEAN_FLOOR_WG`,
LDS:276-280) standing in for the block-identity test — here GRASS instead of the bouncy
castle's SAND, exactly as it already stands in for grass on the swamp/plains users — and
the anchor **one below the air block** (`.below()`, LDS:88 ← `posY - 1`, OSW:1168). The
mode's implementation is biome/block-agnostic (LDS:87-88); precedent for a Plains user of
this scan family: LEAF_MONSTER_DUNGEON on `SWAMP_GRASS_SURFACE` (LDP:187-190, anchor
`posY` — frog pond is its −1 sibling). Worth appending to the SAND_SURFACE_MINUS1 Javadoc
(LDP:284-294) when wiring: `addFrogPond` (OSW:1156-1174, grass/Plains) is another exact
user. The canopy case (the original scan walks down THROUGH tree cover to the air-over-grass
beneath it, OSW:1166-1167) costs nothing here: structure starts resolve before decoration,
so `getBaseHeight` is a pure noise prediction with no trees in it — the port anchors on the
same terrain surface the original found under the canopy, and no new delta arises. The
mode's only mapping delta remains the already-documented block-identity/dry-column
approximation (LDS:258-264). No per-JSON
`placement_mode` override needed (single dimension, single anchor).

JSON trio (copy the `leaf_monster_dungeon` trio — same dimension, same biome, same scan
family — and rename):

- `RES:worldgen/structure/frog_pond.json` — `"type": "orespawn:legacy_dungeon"`,
  `"dungeon_type": "FROG_POND"`, `"biomes": "#orespawn:has_structure/frog_pond"`,
  `"step": "surface_structures"`, `"terrain_adaptation": "none"`,
  `"spawn_overrides": {}`.
- `RES:worldgen/structure_set/frog_pond.json` — §8.
- `RES:tags/worldgen/biome/has_structure/frog_pond.json` — `["minecraft:plains"]`
  (exact-name `"Plains"` gate OSW:1161 → plains only, matching
  `has_structure/leaf_monster_dungeon.json`; no sunflower_plains, no meadow).

## 8. Structure-set conversion

Effective odds: 1/6 (world-rand picker, OSW:285, `i == 5` OSW:301) × 1/350 (chunk-random
gate, OSW:1157) = **1/2100 per overworld chunk** — the standard overworld 6-way rotation
arithmetic.

C7 sqrt equivalence: spacing ≈ √2100 ≈ 45.8 → **spacing 46, separation 23** (precedents:
`gold_fish_bowl.json`, `play_pool.json`, `water_dragon_lair.json` — all 46/23).
Salt: **84370** (assigned to this task; verified free — grep of
`RES:worldgen/structure_set/*.json` shows OreSpawn salts occupying 84301-84367 plus the
vanilla-style 10387399 on dim_villages).

`RES:worldgen/structure_set/frog_pond.json`: random_spread, spacing 46, separation 23,
salt 84370.

## 9. DungeonSpawnerBlock outcome

- Original: `if (type == 43)` → `makeFrogPond(world, clickedX, clickedY + 1, clickedZ)` —
  one call, **`+1` on Y**, block read in full (DSB:182-184). The trio 43/44/45 all pass
  `clickedY + 1` (DSB:183/186/189); type 44 (Pumpkin) is already wired with the
  established treatment.
- Port: add `TYPE_FROG_POND = 43` (cite DSB:182-184) and
  `case TYPE_FROG_POND -> LegacyDungeonPiece.buildNow(server, pos.above(),
  DungeonType.FROG_POND)` in `RandomDungeonSpawnerBlockEntity.buildForType`, following the
  `TYPE_PUMPKIN` `pos.above()` precedent verbatim (RDS:100-101, RDS:293-299). Wired types
  today: 0-3, 7, 12-24, 26-30, 34, 35, 37-39, 44, 46, 47 (RDS:44-103 + RDS:155-304);
  **type 43 currently falls through to the interim generic-dungeon default (RDS:305-306)**.
- Behavior at the DSB anchor: `cposy = clickedY + 1`, so the 7×7 sheet lands ONE ABOVE the
  cleared spawner-block cell (DSB:50-51 cleared `clickedY` and `clickedY + 1`) — on flat
  ground the pond hovers a block up and spills over its rim; underground it embeds in the
  wall. No ground gate, no biome/dimension check — faithful (S5). `buildNow` keeps live
  RNG semantics, which for this zero-draw builder changes nothing.

## 10. Mid-build world READS — classified

1. **None.** GD:6018-6039 contains no `func_147439_a`/`func_147437_c` — no terrain
   probes, no conditionals of any kind.
2. **Tile-entity fetch** (GD:6021, spawner) — self-read of the block written the line
   before; absorbed by `piece.placeSpawner` (LDP:609). Its null-guard (GD:6022) is the
   helper's own behavior.
3. The placement-scan reads (OSW:1167) live outside the builder and map into
   `SAND_SURFACE_MINUS1` + the biome tag (§7).

No `terrainStateIfInChunk` usage needed; no FLAG items; no NEEDS_DESIGN_RULING condition
arises.

## 11. RNG stream accounting

- **In-generator draws: ZERO.** `makeFrogPond` never touches `world.field_73012_v`, the
  chunk random, or `Math.random()` — every coordinate and block is a constant. The
  per-chunk replay passes trivially consume identical (empty) streams; the §1-step-3
  stitching contract is satisfied with nothing to restructure.
- **Dispatch-layer draws** (all collapse per pattern §1 step 4): the 6-way picker
  `world.rand.nextInt(6)` (OSW:285), the 1/350 gate + per-attempt `nextInt(16)`×2 jitter
  on the chunk random (OSW:1157, 1163-1164) → structure-set frequency +
  `swampGrassSurfaceOrigin`'s seeded 4-attempt jitter (LDS:268-270); DSB roll
  `world.rand.nextInt(50)` (DSB:52) → unchanged in `RandomDungeonSpawnerBlockEntity`.
- **Moves to JSON: nothing** (no loot, no fill counts).
- PARITY note (slice report): none of the usual seed-stability deltas apply — with zero
  builder draws, worldgen ponds and `buildNow` ponds are block-identical.

## 12. Surprises / court S-items

- **S1 (MISSING-IN-PORT)**: `makeFrogPond` has no counterpart anywhere in
  `src/main/java/` — grep `FrogPond|frog_pond|FROG_POND`: only a passing comment in
  `PlayPoolGenerator.java:28`; no DungeonType, no generator, no JSON trio, no DSB case 43
  (falls through to the generic-dungeon default, RDS:305-306). Worldgen `i == 5` slot and
  DSB type 43 both unserved today.
- **S2 — no loot, by design**: one of the few structures with no chest at all (§3). The
  temptation target is "frogs should drop something" — do not add a table.
- **S3 — flag-3 water writes** (GD:6027-6038; the spawner at GD:6020 is the method's only
  flag-2 write): the original relied on neighbor notification to start the water flowing.
  Non-issue in the port — modern fluids self-schedule ticks on placement, so `piece.place`
  flag-2 writes reproduce the cascade (established treatment: play_pool_spec.md S3).
  Record as PARITY note, not a deviation.
- **S4 — flowing_water meta 0 = source**: the four +1 cross cells (GD:6031-6034) flatten
  to `minecraft:water` source (play_pool_spec.md §5 precedent) — which is also what they
  settle into in 1.7.10. One palette line, zero visible delta.
- **S5 — the anchor duality is the whole trap of this structure**: worldgen hands the
  builder `posY − 1` (OSW:1168) so the water sheet REPLACES the grass layer and the pond
  sits flush; the DSB hands `clickedY + 1` (DSB:183) so the sheet floats one above the
  cleared block. The builder itself is offset-free — both quirks live entirely at the
  call sites and port as `.below()` inside `SAND_SURFACE_MINUS1` (LDS:88) and
  `pos.above()` in the RDS case (§9). Do not "normalize" either one.
- **S6 — dead local** `boolean which = false` (OSW:1165) — decompiler artifact, never
  read; do not port (play_pool_spec.md S6 precedent).
- **S7 — zero-draw builder**: no RNG anywhere in the geometry (§11). Any port draw added
  "for variety" (lily rotation, pond depth) would be invented content.
- **S8 — sub-Y50 ponds have dormant spawners in BOTH versions** (§4.1): placement window
  Y 41..100 (OSW:1166) vs Frog's `y >= 50` spawn rule (orig Frog.java:240-251, port
  Frog.java:277-285, no near-spawner bypass). Faithful — noted so nobody "fixes" it.
- **S9 — the fountain settles STABLE (AMENDED, TF-025 close-out 2026-08-11; the
  original bullet's "spills off the rim" was an extraction-time INFERENCE, not
  observed behavior — user-ratified)**: the center riser + flow cross at +1
  (GD:6030-6034) are five water sources one block above the sheet. Fluid
  mechanics — identical in 1.7.10 and 1.21.1 for this geometry — refuse a rim
  spill: flow advancing over the source sheet becomes FALLING water, which
  never propagates horizontally, so the cascade dies one block off the cross
  (flowing level-1 water at +2 on the +1 layer, air beyond), while the
  two-source rule promotes the convex 3×3 around the riser to new sources.
  Empirically confirmed by the TF-025 instrumented diagnostic (t=0..250 dump,
  FIX_LOG TESTING_FINDINGS): the pond reaches a stable state with all placed
  blocks intact. Do not flatten the mound, rim the pond, or expect water on
  the surrounding grass.
- **S10 — write order**: spawner first (GD:6020), then the water under and around it
  (GD:6025-6038). Order has no behavioral consequence (no reads, no draws), but keep it in
  the generator for number-by-number diff-ability (pattern §1 step 7.2).
- **S11 — loop-variable naming**: in the 7×7 sheet loop, `j` is the Z offset
  (GD:6025-6028), not Y as everywhere else in this file. Symmetric square, so a
  transposition cannot bite here — flagged only so the port's re-diff doesn't stumble.
