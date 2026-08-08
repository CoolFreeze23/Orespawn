# Cephadrome Altar — Extraction Spec (1.7.10 → NeoForge 1.21.1)

Source of truth: `reference_1_7_10_source/sources/danger/orespawn/GenericDungeon.java` —
`makeCephadromeAltar` (GD:4731-4829, next method `makeCrystalBattleTower` at GD:4831).
All coordinates are relative to the build origin `(cposx, cposy, cposz)` = the three int args.

Citation convention: `GD:NN` = GenericDungeon.java line NN, `OSW:NN` = OreSpawnWorld.java,
`OSM:NN` = OreSpawnMain.java, `DSB:NN` = DungeonSpawnerBlock.java, `LDS:NN` =
`src/main/java/danger/orespawn/world/structure/LegacyDungeonStructure.java`. Port citations
name the port file + line.

Shared plumbing:
- Every write is `OreSpawnMain.setBlockFast(world, x, y, z, block, 0, 2)` — flag-2 chunk
  write, meta always 0, no neighbor updates → the port's `piece.place(x, y, z, state)`
  helper (structure_conversion_pattern.md §1 step 3 table).
- The builder contains **no spawner, no chest, no entity spawn, no TE fetch, no
  `func_147439_a` block read, and zero RNG draws** (verified over GD:4731-4829) — it is a
  pure deterministic block-stamp, the same shape as Gold Fish Bowl (§10/§11 there).

---

## 1. Entry points (every call site — grep of the full original tree)

Grep `makeCephadromeAltar` over the whole reference tree: GD:4731 (definition), OSW:2196,
DSB:156 — exactly two call sites.

| Caller | Signature used | Coords passed | Notes |
|---|---|---|---|
| `OreSpawnWorld.addD4CephadromeAltar` (OSW:2196) | `OreSpawnMain.MyDungeon.makeCephadromeAltar(world, posX, posY, posZ)` | scan hit, **no Y offset** — posY is the GRASS block itself; loop 1's `j=0` cobble plate overwrites it | worldgen path, Islands (DimensionID4) only — D4 roll `i==12` (§1.2) |
| `DungeonSpawnerBlock` type **34** (DSB:155-157) | `...makeCephadromeAltar(world, clickedX, clickedY, clickedZ)` | player-placed block pos, no offset | `type = world.rand.nextInt(50)` (DSB:52), fires 400 ticks after placement (DSB:39), self+above deleted first (DSB:50-51). The `if (type == 34)` block read IN FULL: **single builder call, nothing else** (DSB:155-157) — not a two-builder index. |

### 1.1 `addD4CephadromeAltar` — FULL method + return contract (OSW:2187-2201)

1. LessLag gate: `if (OreSpawnMain.LessLag != 0 && random.nextInt(2) != 0) return false`
   (OSW:2188-2190) — 50% skip when LessLag is on; the `nextInt(2)` draw happens ONLY when
   LessLag is enabled (short-circuit `&&`).
2. Jitter: `posX = chunkX + random.nextInt(8)`, `posZ = chunkZ + random.nextInt(8)`
   (OSW:2191-2192) — **nextInt(8)**, not the overworld chains' nextInt(16). One attempt only.
3. Column scan `posY = 20` down to `5` inclusive (`posY > 4`, OSW:2193): require
   `world.func_147439_a(posX, posY, posZ) == Blocks.field_150349_c` (**grass block at the
   probed cell itself**, OSW:2194-2195 — not air-above-grass; contrast addSpitBug).
4. Hit → `makeCephadromeAltar(world, posX, posY, posZ)` (origin = the grass block),
   `recently_placed = 50`, `return true` (OSW:2196-2198).
5. Scan miss → `return false` (OSW:2200).

**Return contract: `true` only on an actual placement** (which also sets the 50-chunk
global cooldown); `false` on LessLag skip or scan miss. The ONLY caller (OSW:157)
**ignores the return value** — the `i==12` arm is a bare statement with no chaining and no
follow-on suppression, so there is no boolean coupling to port (no addFairyTree/WGEN-062
early-true quirk possible: nothing reads the result).

### 1.2 Worldgen dispatch chain (complete) — Islands D4 roll

- `generateSurface` Islands branch (OSW:132-185): gate
  `recently_placed == 0 && random.nextInt(100) == 0 && this.D4BigSpaceCheck(world,
  chunkX*16, 7, chunkZ*16)` (OSW:134), then `i = random.nextInt(19)` (OSW:135);
  **`i == 12` → `addD4CephadromeAltar(world, random, chunkX*16, chunkZ*16)`**
  (OSW:156-158). Full i→structure table: `phase_d_reports/d5_extraction/enormous_castle_spec.md`
  (pattern doc §1 step 4 says D6 reuses it for every remaining Islands structure).
- Effective odds: 1/100 × 1/19 = **1/1900 per Islands chunk** (single-outcome i), before
  the space check, LessLag skip, and grass scan.
- `D4BigSpaceCheck` (OSW:2655-2664): probes the 65×55 area `i = −25..39`, `k = −25..29`
  at `posY+4 = 11`, requiring every cell be air / vanilla log / MyAppleLeaves /
  MyScaryLeaves. Maps onto structure-set separation per the standing C7 approximation
  (pattern §1 step 4) — do not reproduce the cross-structure coupling.
- `recently_placed = 50` cooldown (OSW:2197) → same C7 separation approximation.

---

## 2. Geometry — per-loop table (all ranges inclusive)

Every loop is `for (i = -width; i <= width; ++i) for (k = -length; k <= length; ++k)`
writing at `(cposx+i, cposy+j, cposz+k)`. Two shared predicates:

- **cross**: `k == 0 || i == 0` — the two center rows.
- **corner**: `!(k != -length && k != length || i != -width && i != width)` ⇔
  `(|k| == length) && (|i| == width)` — the 4 corners only (De Morgan; same idiom as
  the Leon Nest / other GD builders).

Write order matters — loops 6 and 7 overwrite the air layers 3 and 4 (see §2.1, S3):

| # | j | Area (w×l) | Default | cross | corner | center | Cite |
|---|---|---|---|---|---|---|---|
| 1 | 0 | 9×9 (`i,k = -4..4`) | cobblestone | — | — | — | GD:4736-4744 |
| 2 | +1 | 7×7 (`-3..3`) | cobblestone | stone bricks | stone bricks | — | GD:4745-4760 |
| 3 | +2 | 7×7 | air | — | stone bricks | — | GD:4761-4772 |
| 4 | +3 | 7×7 | air | — | end stone | — | GD:4773-4784 |
| 5 | +4 | 7×7 | air | — | Extreme Torch | — | GD:4785-4796 |
| 6 | +2 | 5×5 (`-2..2`) | cobblestone | stone bricks | stone bricks | — | GD:4797-4812 |
| 7 | +3 | 3×3 (`-1..1`) | cobblestone | — | end stone | Eye-of-Ender block (`i==0 && k==0`) | GD:4813-4828 |

### 2.1 Overwrites (behavioral — replicate write order or place tiers after air layers)

- Loop 6 (5×5 solid at `j=+2`) overwrites the interior air loop 3 just wrote in that
  5×5 region; loop 3's surviving output is the 7×7 ring `|i|=3 or |k|=3` minus corners
  (air) plus the 4 stone-brick corners.
- Loop 7 (3×3 solid at `j=+3`) likewise overwrites loop 4's air in the 3×3 center;
  loop 4's surviving output is air over the `j=+3` layer outside the 3×3, with end-stone
  at the 7×7 corners.
- Nothing overwrites loop 5: `j=+4` is air across the 7×7 except the 4 torch corners.

### 2.2 Net shape

A 3-tier cobblestone ziggurat: 9×9 base plate at `j=0` (replacing the grass anchor),
7×7 tier at `j=+1` and 5×5 tier at `j=+2` (both with stone-brick center-cross inlays and
stone-brick corners), 3×3 cap at `j=+3` with end-stone corners and the **Eye-of-Ender
block** dead center at `(0, +3, 0)`. At the 7×7 corners `(±3, j, ±3)` four pillars rise
free of the upper tiers: stone brick (`j=+1`, part of tier 2), stone brick (`j=+2`),
end stone (`j=+3`), **Extreme Torch** on top (`j=+4`). The air defaults of loops 3-5 carve
a 7×7×3 headspace over the upper tiers (including one air block directly above the eye —
the cell where the player's summoning torch goes, §4);
the 9×9 base's outer ring (`|i|=4 or |k|=4`) gets NO air clearing above it (S4).

Dead code inside the builder: `boolean meta = false` (GD:4735) is never read; the loop-head
`bid` pre-assignments GD:4734, 4748, 4800, 4816 are immediately overwritten by the
per-cell assignments. Ignore. **GD:4739 is NOT dead** — loop 1 has no per-cell `bid`
assignment, so its 9×9 plate reads the GD:4739 cobblestone directly.

---

## 3. Loot — FULL transcription

**This builder places NO chest and fills NO loot.** GD:4731-4829 contains zero
`field_150486_ae` / `getChestTileEntity` / `WeightedRandomChestContent` calls. No
`loot_table/chests/*.json` is needed. Total weight: n/a. Do not invent loot (S2).

## 4. Spawner / mob mapping table

**No spawner and no direct entity spawn** — zero `field_150474_ac` /
`getSpawnerTileEntity` / `spawnCreature` calls in GD:4731-4829. The structure's namesake
mob arrives indirectly, but NOT from the four pillar torches. The torch's summon logic
lives in `func_149689_a` = onBlockPlacedBy (orig BlockExtremeTorch.java:66-101) and fires
only when BOTH hold: (a) the torch was just placed by an entity — `setBlockFast` never
triggers onBlockPlacedBy, so structure-placed torches are inert; (b) the block directly
BELOW the torch is `MyEyeOfEnderBlock` (orig :71). The altar's pillar torches sit on end
stone, failing (b) anyway. The actual mechanic: a **player places an Extreme Torch on the
central Eye-of-Ender block** — the builder leaves air at `(0,+4,0)` above the eye for
exactly this — which summons a Cephadrome at a random nearby valid spot (100 tries,
±4-block offset, orig :72-79) and consumes the torch on success (orig :97). Already
ported as ITEM-017: port `BlockExtremeTorch.setPlacedBy`
(`src/main/java/danger/orespawn/block/BlockExtremeTorch.java:50-95`, eye-below gate
:54-55, spawns `ModEntities.CEPHADROME`). That behavior lives in the block, not the
builder — nothing to port here beyond placing the torches and the eye block.
(Orig quirk, out of scope: client-side `func_149734_b` randomDisplayTick also calls
`func_149689_a` (orig :59); ITEM-017 deliberately does not replicate it.)

## 5. Block palette

| 1.7.10 field (meta 0 everywhere) | Modern block | Used for | Cite |
|---|---|---|---|
| `Blocks.field_150347_e` | `minecraft:cobblestone` | all three tiers + base plate | GD:4739/4751/4803/4819 |
| `Blocks.field_150417_aV` | `minecraft:stone_bricks` | cross inlays j+1/j+2, tier corners, pillar j+1..+2 | GD:4753/4756/4768/4805/4808 |
| `Blocks.field_150377_bs` | `minecraft:end_stone` | pillar j+3, 3×3-cap corners | GD:4780/4824 |
| `OreSpawnMain.ExtremeTorch` ("OreSpawn_ExtremeTorch", OSM:1927/2178) | port `ModBlocks.EXTREME_TORCH` "extreme_torch" (ModBlocks.java:134-135, a standing `BlockExtremeTorch`) | 4 pillar tops j+4 | GD:4792 |
| `OreSpawnMain.MyEyeOfEnderBlock` ("OreSpawn_EyeOfEnderBlock", OSM:1973/2187 — an `OreGenericEgg` XP-drop block) | port `ModBlocks.BLOCK_EYE_OF_ENDER` "block_eye_of_ender" (ModBlocks.java:59-60) | altar center (0,+3,0) | GD:4821 |
| `Blocks.field_150350_a` | `minecraft:air` | headspace defaults j+2..+4 | GD:4766/4778/4790 |

All flag-2 writes; the torches sit ON end stone (supported), so the floating-torch
concern from the pattern doc does not even arise — but `piece.place` preserves the
no-neighbor-update semantics regardless.

## 6. Footprint extents (relative to origin) + suggested DungeonType

| Axis | Min | Max | Extent | Determined by |
|---|---|---|---|---|
| X | `−4` | `+4` | **9** | loop 1 `width = 4` (GD:4736, 4740) |
| Y | `0` | `+4` | **5** | base plate `j=0` (GD:4738) / torch layer `j=4` (GD:4787) |
| Z | `−4` | `+4` | **9** | loop 1 `length = 4` (GD:4737, 4741) |

Suggested entry (6-int ctor `(minXOff, maxXOff, down, up, minZOff, maxZOff)` —
LegacyDungeonPiece.java:261-266; +1 margin each way, `down 1` covers the origin level):

```java
CEPHADROME_ALTAR(-5, 5, 1, 5, -5, 5, PlacementMode.ISLANDS_GRASS),
```

**PlacementMode: `ISLANDS_GRASS` — exact fit, no new mode needed.** The mode was written
for `addD4NightmareRookery` (LegacyDungeonPiece.java:189-196 javadoc), and
`addD4CephadromeAltar`'s scan is line-for-line the same shape: LessLag 50% skip
(OSW:2188 ↔ LDS:306-308), `chunk + nextInt(8)` jitter on both axes (OSW:2191-2192 ↔
LDS:310-311), grass scan Y 20→5 as a hard accept window (OSW:2193-2195 ↔ LDS:312-316,
`getBaseHeight − 1` with `grassY > 20 || grassY < 5 → reject`), anchor = the grass block
itself with no offset (OSW:2196 ↔ LDS:317).

## 7. Structure-set conversion

- Effective odds 1/100 × 1/19 = **1/1900 per Islands chunk** (§1.2) → C7 sqrt
  equivalence: spacing ≈ √1900 ≈ 43.6 → **spacing 44, separation 22** (the pattern doc's
  standing Islands-D4 single-outcome numbers, matching nightmare_rookery.json).
- **Salt 84361** (assigned to this task; grep of `RES:worldgen/structure_set/*.json` at
  verification date shows highest 843xx in use = **84354** (spit_bug_lair; gold_fish_bowl
  84352) — 84361 is free. The pattern doc's mantis_nest/royal_trees 84312 collision has
  since been resolved: royal_trees now 84332, mantis_nest keeps 84312).
- `D4BigSpaceCheck` + `recently_placed = 50` + LessLag skip → structure-set separation
  per the C7 approximation (the LessLag skip itself is still reproduced live inside
  `islandsGrassOrigin`, LDS:306-308).

JSON trio (copy the `nightmare_rookery` trio and rename — same dimension, same mode,
same spacing):

- `RES:worldgen/structure/cephadrome_altar.json` — `"type": "orespawn:legacy_dungeon"`,
  `"dungeon_type": "CEPHADROME_ALTAR"`, `"biomes": "#orespawn:has_structure/cephadrome_altar"`,
  `"step": "surface_structures"`, `"terrain_adaptation": "none"`, `"spawn_overrides": {}`.
- `RES:worldgen/structure_set/cephadrome_altar.json` — random_spread, spacing 44,
  separation 22, salt 84361.
- `RES:tags/worldgen/biome/has_structure/cephadrome_altar.json` — `orespawn:island_biome`.

## 8. DungeonSpawnerBlock outcome

- Original: `if (type == 34)` → `makeCephadromeAltar(world, clickedX, clickedY,
  clickedZ)` — one call, no offset, block read in full (DSB:155-157).
- Port: add `TYPE_CEPHADROME_ALTAR = 34` (cite DSB:155-157) and
  `case TYPE_CEPHADROME_ALTAR -> LegacyDungeonPiece.buildNow(server, pos,
  DungeonType.CEPHADROME_ALTAR)` in `src/main/java/danger/orespawn/block/entity/
  RandomDungeonSpawnerBlockEntity.buildForType`. **Type 34 currently falls through to the
  generic-dungeon fallback** (the `default` arm, RandomDungeonSpawnerBlockEntity.java:219).
- The DSB path faithfully bypasses the grass scan — an altar floating or embedded in
  terrain at the clicked position is original behavior (same ruling as FairyTree /
  Gold Fish Bowl).

## 9. World-block READS mid-build

**None.** GD:4731-4829 contains zero `func_147439_a` block reads and zero tile-entity
fetches — every statement is a `setBlockFast` write. The only world read in the whole
feature is `addD4CephadromeAltar`'s grass scan (OSW:2193-2195), which is **pre-build
terrain**, absorbed by the ISLANDS_GRASS placement mode (§6). The RNG-stitching "never
branch on world state" rule is satisfied with no in-memory model and no deviation
decision.

## 10. RNG stream

**Empty.** The builder consumes zero random draws (verified over GD:4731-4829) — geometry
and palette are all constants; every per-chunk replay pass is trivially identical, no
draw-order contract to maintain. The dispatch-layer draws (OSW:134 `nextInt(100)` gate,
OSW:135 `nextInt(19)` pick, OSW:2188 LessLag `nextInt(2)`, OSW:2191-2192 `nextInt(8)`
jitter, DSB:52 `nextInt(50)`) all live outside the builder and map to structure-set
frequency / `islandsGrassOrigin` / the DSB roll, as usual.

## 11. Surprises / MISSING-IN-PORT

- **S1 (MISSING-IN-PORT)**: `makeCephadromeAltar` has no counterpart anywhere in
  `src/main/java/danger/orespawn/` (grep `CEPHADROME` in the structure package: zero
  hits — only the entity, torch-summon, and overlay files). No DungeonType, no JSON trio;
  worldgen path and DSB type 34 both fall through today (34 → generic-dungeon fallback).
- **S2**: No chest, no spawner, no loot — the altar's payoff is functional: it is a
  **summoning pad**. The player places an Extreme Torch on the central Eye-of-Ender
  block to summon a Cephadrome (torch consumed on success — ITEM-017, mechanism
  corrected in §4); the four pillar torches are decorative (on end stone, and
  structure placement never fires onBlockPlacedBy/setPlacedBy). The Eye-of-Ender
  block is also a breakable bonus-XP block (`OreGenericEgg`, port
  OreGenericEgg.java:34-38). Do not invent loot or a spawner.
- **S3**: Write order is load-bearing twice: loop 6 re-solidifies the 5×5 of the `j=+2`
  air layer, loop 7 re-solidifies the 3×3 of the `j=+3` air layer (§2.1). Preserve order
  (or place the air layers first and tiers after — identical result).
- **S4**: The air clearing only covers the 7×7 footprint at `j=+2..+4`; the 9×9 base's
  outer ring gets no clearing above it, so hillside terrain can overhang the base rim.
  Faithful — do not widen the clearing.
- **S5**: The grass probe checks the scanned cell ITSELF for grass (OSW:2194-2195), and
  the builder's `j=0` plate then overwrites that grass with cobblestone — the altar sits
  flush IN the surface, not on top of it (contrast Spit Bug Lair's air-above-grass
  anchor). `islandsGrassOrigin`'s `getBaseHeight − 1` anchor reproduces this exactly.
- **S6**: `addD4CephadromeAltar`'s boolean return is ignored by its only caller
  (OSW:157) — full contract documented in §1.1 for completeness, but there is no
  chain/suppression coupling to port.
- **S7**: LessLag draw-order nuance: OSW:2188 short-circuits, so with LessLag off no
  `nextInt(2)` is drawn; `islandsGrassOrigin` (LDS:306-308) mirrors the same
  short-circuit. Nothing to change — noted so nobody "fixes" it into an unconditional
  draw.
- **S8**: 100% deterministic builder (§10) — second one in D6 after Gold Fish Bowl; a
  template COULD express it, but per pattern §0 it still goes through
  `LegacyDungeonStructure` (9×9 footprint can straddle chunk borders; /locate and DSB
  `buildNow` come free).
- **S9**: The Extreme Torch is a `noCollission` standing torch in the port
  (ModBlocks.java:134-135); place its default state at the pillar tops. It stands on end
  stone, so no floating-block adaptation is needed.
