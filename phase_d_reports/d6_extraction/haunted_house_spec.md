# Haunted House (OVERWORLD) — Extraction Spec (1.7.10 → NeoForge 1.21.1)

Source of truth: `reference_1_7_10_source/sources/danger/orespawn/GenericDungeon.java` —
`makeHauntedHouse` (GD:891-1010; next method `makeMantisHive` at GD:1012). Method read
IN FULL, including the shared helpers it uses (`getSpawnerTileEntity` GD:86-95,
`getChestTileEntity` GD:75-84). All coordinates are relative to the build origin
`(cposx, cposy, cposz)` = the three int args.

**Provenance note: this structure was NOT on the original 22-structure D6b list — it
surfaced in batch 4's DSB sweep as an unported builder** (DSB type 5, worldgen ahh-chain
link 2). **Distinct from CrystalHauntedHouse** (`makeCrystalHauntedHouse`, GD:2993 —
Crystal-dimension palette, already ported as `CrystalStructures.buildCrystalHauntedHouse`,
port CrystalStructures.java:673-721). This spec covers the vanilla-OVERWORLD original
only.

Citation convention: `GD:NN` = GenericDungeon.java line NN, `OSW:NN` = OreSpawnWorld.java,
`OSM:NN` = OreSpawnMain.java, `DSB:NN` = DungeonSpawnerBlock.java, `IS:NN` =
InstantShelter.java, `RDS:NN` =
`src/main/java/danger/orespawn/block/entity/RandomDungeonSpawnerBlockEntity.java`,
`LDP:NN` = `src/main/java/danger/orespawn/world/structure/LegacyDungeonPiece.java`,
`LDS:NN` = `src/main/java/danger/orespawn/world/structure/LegacyDungeonStructure.java`.

Method-local constants: `length = 3` (GD:902), `width = 3` (GD:903), `height = 3`
(GD:904), `deltax = 1` (GD:905, **used** — selects the doorway wall and the furniture
axis), `deltaz = 0` (GD:896, never reassigned — used as the zero in the furniture
arithmetic), `stuffdir = 2` (GD:906, **used** — furnace/chest facing meta). Dead locals:
`boolean bid/dirx/dirz` (GD:897-899); `x/z/y` are plain copies of `cposx/cposz/cposy`
(GD:907-909). Client guard `if (world.field_72995_K) return;` (GD:910-912) — server-only
no-op for both port paths, nothing to port.

Shared plumbing / header traps:

- **Shell + furniture writes use `func_147449_b` (1.7.10 `World.setBlock(x,y,z,block)`,
  default FLAG 3 — WITH neighbor updates), and facing metadata is stamped after with
  `func_72921_c(..., meta, 3)`** (GD:917/921/926/930/933/936, 943-949) — NOT the
  `setBlockFast`/flag-2 route the stinky/bouncy siblings used. The palette contains no
  attachable or gravity-affected block, so flag 3 vs flag 2 has zero behavioral
  consequence here; port through `piece.place(x, y, z, state)` like every other
  generator (structure_conversion_pattern.md §1 step 3 table) and carry the facing in
  the BlockState (§2.2, §5).
- Spawners: `func_147465_d(..., field_150474_ac, 0, 2)` + `getSpawnerTileEntity` +
  `func_98272_a(name)` (GD:995-1009) → `piece.placeSpawner(x, y, z, type)`.
- Chest: `func_147449_b` + meta 2 via `func_72921_c` (GD:948-949), then
  `getChestTileEntity` + **14 fixed-slot 50% fills** (GD:950-994) — NO
  `WeightedRandomChestContent` list → `piece.placeLootChest(x, y, z, Direction.NORTH,
  lootKey)` (facing overload, LDP:538-545; WGEN-056) + a 14-pool `random_chance 0.5`
  loot JSON (igloo-spec §3 precedent). §3.
- **Zero terrain reads in the builder** — grep of GD:891-1010 for
  `func_147439_a`/`func_147437_c`: no hits. The only reads are the chest TE fetch
  (GD:950) and three spawner TE fetches (GD:996/1001/1006) — self-reads absorbed by the
  helpers. §10.
- Builder RNG: exactly the 14 chest-slot `nextInt(2)` draws (GD:952-991), all of which
  move into the loot JSON — the ported generator body is **RNG-free**, trivially
  stitching-safe (§11).

---

## 1. Entry points — EVERY call site

Grep of the whole original tree for `makeHauntedHouse`: exactly two call sites
(OSW:990 and DSB:69; GD:891 is the definition). Grep of the port `src/main/java/` for
`HauntedHouse|HAUNTED`: only the Crystal variant (CrystalStructures.java) — the
overworld structure has no port presence (§12 S1).

| Caller | Signature used | Coords passed | Notes |
|---|---|---|---|
| `OreSpawnWorld.addHauntedHouse` (OSW:990) | `OreSpawnMain.MyDungeon.makeHauntedHouse(world, posX, posY, posZ)` | scan hit, **no offset — posY is the AIR block above the grass**, not the grass itself (§1.1) | worldgen path, vanilla overworld only, Plains/Taiga/Swampland (§1.2) |
| `DungeonSpawnerBlock` type **5** (DSB:68-70) | `...makeHauntedHouse(world, clickedX, clickedY, clickedZ)` | player-placed block pos, no offset | roll `type = world.field_73012_v.nextInt(50)` (DSB:52), fires 400 ticks after placement (DSB:39), self + block above deleted first (DSB:50-51). The `if (type == 5)` block read IN FULL: **single builder call, nothing else** (DSB:68-70) — not a two-builder index (neighbors: 4 = BeeHive DSB:65-67, 6 = MantisHive DSB:71-73). |

### 1.1 `addHauntedHouse` — FULL method + return contract (OSW:979-997)

```java
// OSW:979-997
public boolean addHauntedHouse(World world, Random random, int chunkX, int chunkZ) {
    if (random.nextInt(285) != 0) {
        return false;
    }
    BiomeGenBase b = world.func_72807_a(chunkX, chunkZ);
    if (b.field_76791_y.equals("Plains") || b.field_76791_y.equals("Taiga") || b.field_76791_y.equals("Swampland")) {
        for (int i = 0; i < 5; ++i) {
            int posX = chunkX + random.nextInt(16);
            int posZ = chunkZ + random.nextInt(16);
            for (int posY = 100; posY > 40 && world.func_147437_c(posX, posY, posZ); --posY) {
                if (world.func_147439_a(posX, posY - 1, posZ) != Blocks.field_150349_c) continue;
                OreSpawnMain.MyDungeon.makeHauntedHouse(world, posX, posY, posZ);
                recently_placed = 50;
                return true;
            }
        }
    }
    return false;
}
```

1. Gate: `random.nextInt(285) != 0 → return false` (OSW:980-982) — **1/285**, drawn from
   the chunk-provided `random` BEFORE the biome check.
2. Biome: `func_72807_a(chunkX, chunkZ)` (chunk-corner block coords) must be exactly
   `"Plains"`, `"Taiga"`, or `"Swampland"` (OSW:983-984); else fall through to
   `return false`.
3. Up to **5 attempts** (OSW:985 — one more than the 4-attempt leaf monster/spit
   bug/bouncy siblings): `posX/posZ = chunk + random.nextInt(16)` (OSW:986-987).
4. Column scan `posY = 100` down, **descending only while the current block is AIR**
   (`posY > 40 && func_147437_c(posX, posY, posZ)`, OSW:988) — an air-descent loop, not
   the siblings' full-window scan-with-continue. Consequences: if `(posX, 100, posZ)` is
   not air (terrain above y = 100), the attempt fails immediately; the scan stops at the
   lowest air block of the column; effective anchor window is posY 41..100.
5. Body: require `block(posX, posY − 1, posZ) == grass block` (`field_150349_c`,
   OSW:989). Non-grass surface → `continue` decrements posY into the solid block, the
   loop condition fails, the attempt ends.
6. Hit → `makeHauntedHouse(world, posX, posY, posZ)` — **anchor is the AIR block above
   the grass, no offset** — then `recently_placed = 50`, `return true` (OSW:990-992).
7. Gate fail, biome fail, or all 5 attempts miss → `return false` (OSW:996).

**FULL return contract: `true` ONLY on an actual placement** (which also sets the
50-chunk global cooldown, OSW:991). No WGEN-062-style early-true quirk.

### 1.2 Chain position — the ahh-chain link and its gates (OSW:284-322)

The full chain anatomy is documented in `bouncy_castle_spec.md` §1.2 (cross-referenced,
not repeated): outer gate `OreSpawnMain.DisableOverworldDungeons == 0 &&
world.field_73011_w.field_76574_g == 0 && recently_placed == 0` (OSW:284), the
independent 6-way pool roll first (OSW:285-303), then the fall-through chain
(OSW:304-321). HauntedHouse's own pins:

- `addHauntedHouse` is link **2 of 7** (OSW:304-306): it runs only when `addANest`
  (link 1, 1/230 gate, OSW:1000) returned false; its own success suppresses links 3-7
  (addLeafMonster, addSpitBug, addIgloo, addBouncyCastle, addRubberDuckyPond).
- Upstream suppression ≈ 1/230 ≈ 0.4% (addANest's gate, before its own biome/scan
  filters) — negligible; the `recently_placed` coupling maps onto structure-set
  separation per the C7-approved approximation (pattern §1 step 4).
- Effective own odds: **1/285 per vanilla-overworld chunk** (OSW:980) before the
  3-biome filter and scan (§8).

---

## 2. Geometry — per-loop tables (all ranges inclusive)

One triple loop builds the shell (GD:913-939), then 3 furniture blocks + the chest fill
(GD:940-994), then 3 stacked spawners (GD:995-1009). The whole build occupies
`(−3..+3, +0..+4, −3..+3)` relative to origin — **the floor is written AT origin level
y+0** (which in worldgen is the air block above the grass; the grass below is never
touched).

### 2.1 Shell loop — 7×7×5 house (GD:913-939)

`i = −3..+3` (X, `-width..width`), `j = −3..+3` (Z, `-length..length`), `k = 0..+4`
(Y, `0..height+1`). Per cell the rules run in source order — first match wins
(if/continue chain, no overwrites):

| # | Rule (source order) | Result | Cite |
|---|---|---|---|
| 1 | `k == height+1` (= 4) | oak planks (`field_150344_f`) — full 7×7 flat roof, including over walls | GD:916-919 |
| 2 | `k == 0` | cobblestone (`field_150347_e`) — full 7×7 floor, including under walls | GD:920-923 |
| 3 | perimeter (`i == ±3 \|\| j == ±3`) and `k == height` (= 3) | glass **block** (`field_150359_w`) — a full perimeter window band one below the roof, corners included | GD:924-928 |
| 4 | perimeter and `(k == 1 \|\| k == 2) && i == deltax*width && j == deltaz*length` → `i == +3 && j == 0` | air — 1-wide, 2-tall doorway centered in the **+X (east) wall** | GD:929-932 |
| 5 | perimeter, remaining `k = 1..2` cells | oak planks — the wall proper | GD:933-934 |
| 6 | interior (everything else), `k = 1..3` | air — clears terrain inside | GD:936 |

All via `func_147449_b` (flag-3 write — header trap note). The write is **unconditional
over the whole 7×7×5 box**: terrain inside becomes air, the surface air layer at y+0
becomes the cobble floor. Nothing outside the box is touched — no yard, no foundation;
on a slope the house half-embeds, over a dip it overhangs.

Net shell: 7×7 cobblestone floor at y+0, oak-plank walls at y+1..+2, a full glass ring
at y+3, flat plank roof at y+4, one 1×2 doorway at `(+3, +1..+2, 0)`. Interior air
volume: `(−2..+2, +1..+3, −2..+2)` — the glass band is perimeter-only (rule 3 requires
a perimeter cell), so the interior k = 3 layer is rule-6 air, not glass.

### 2.2 Furniture row — furnace, crafting table, chest (GD:940-949)

Setup `i = 2; k = 1; j = length − 1 = 2` (GD:940-942), then positions via
`(x + i*deltax + j*deltaz, y + k, z + i*deltaz + j*deltax)`. With `deltax = 1,
deltaz = 0, j = 2, k = 1` this collapses to **`(x + i, y + 1, z + 2)`** — a row along
the interior south side (z = +2, one in from the j = +3 wall), on the floor:

| # | Position (rel) | Block | Facing | Cite |
|---|---|---|---|---|
| 1 | `(+2, +1, +2)` | furnace (`field_150460_al`, unlit) | meta 2 = **north** (`func_72921_c(..., stuffdir, 3)`) | GD:943-944 |
| 2 | `(+1, +1, +2)` | crafting table (`field_150462_ai`) | — | GD:945-946 |
| 3 | `(0, +1, +2)` | chest (`field_150486_ae`) | meta 2 = **north** | GD:947-949 |

All face north (−Z), fronts toward the room interior. Port: furnace as
`Blocks.FURNACE.defaultBlockState().setValue(FurnaceBlock.FACING, Direction.NORTH)` via
`piece.place`; chest via the `placeLootChest` facing overload (LDP:538-545 — WGEN-056:
originals stamped chest facing after placement; a default state loses it).

### 2.3 Chest fill — 14 fixed slots, 50% each (GD:950-994)

TE fetch GD:950, then inside `if (chest != null)` (GD:951) each of slots 0-13 is filled
by an INDEPENDENT `world.field_73012_v.nextInt(2) == 0` roll — full transcription in §3.

### 2.4 Spawners — 3, stacked vertically at the house center (GD:995-1009)

Each: `func_147465_d(..., field_150474_ac, 0, 2)` + `func_98272_a(name)`. All three sit
in the center column `(cposx, cposz)` = rel `(0, ?, 0)`, one per Y level — a floor-to-
glass-band spawner STACK, all in loop-interior air:

| # | Position (rel) | Mob name string | Cite |
|---|---|---|---|
| 1 | `(0, +1, 0)` | `"Rat"` | GD:995-999 |
| 2 | `(0, +2, 0)` | `"Ghost"` | GD:1000-1004 |
| 3 | `(0, +3, 0)` | `"Ghost Pumpkin Skelly"` | GD:1005-1009 |

Net shape: a snug 7×7 InstantShelter-style cottage (cobble floor, plank walls, glass
clerestory, flat roof, east door, furnace + crafting table + kit chest along the south
wall) whose center is a three-high Rat/Ghost/Ghost-Pumpkin-Skelly spawner column — a
starter home gone bad.

---

## 3. Loot — FULL transcription

**No `WeightedRandomChestContent` list.** The chest is filled by **14 INDEPENDENT
`nextInt(2) == 0` rolls (50% each), each into a FIXED slot 0-13** (GD:952-993) — the
igloo/InstantShelter fill shape (igloo spec §3). Weight totals: n/a (no weighted pool);
expected stacks = 7, min 0, max 14.

The list is the **InstantShelter kit** (orig IS:121-134, port
`item/InstantShelter.java:107-120`) **including its slot-12 Ore Salt block** (which the
igloo omits), at 50% per slot instead of IS's guaranteed fill, with **cooked** porkchop
where IS has raw — and WITHOUT the igloo's three appended gold-nugget slots. Same fill
FORMULA as igloo (per-slot 1/2) but a different LIST (salt present, nuggets absent) →
its own table per the amended one-table-per-(list, fill formula) rule (pattern §1
step 5); cross-reference the kit family (IS deterministic fill, igloo.json, this file,
and the code-side crystal variant CrystalStructures.java:1056-1080) in a `_kit_family`
comment key.

| # | Slot | 1.7.10 item (qty) | Modern / port mapping (cite) | Chance |
|---|---|---|---|---|
| 1 | 0 | `Items.field_151111_aL` ×1 | `minecraft:compass` (IS port :107) | 1/2 (GD:952-954) |
| 2 | 1 | `Items.field_151148_bJ` ×1 (empty map) | `minecraft:map` (IS port :108; igloo spec §3 row 2) | 1/2 (GD:955-957) |
| 3 | 2 | `Items.field_151157_am` ×8 | `minecraft:cooked_porkchop` (damsel spec §3 row 3) — the COOKED field; IS slot 2 is raw `field_151147_al` (IS port :109) — the two lists differ here, igloo-spec-noted | 1/2 (GD:958-960) |
| 4 | 3 | `Blocks.field_150478_aa` ×32 (torch block item) | `minecraft:torch` (IS port :110) | 1/2 (GD:961-963) |
| 5 | 4 | `Items.field_151044_h` ×16 | `minecraft:coal` (IS port :111) | 1/2 (GD:964-966) |
| 6 | 5 | `Items.field_151104_aV` ×1 (bed item) | `minecraft:red_bed` (flattening default; IS port :112) | 1/2 (GD:967-969) |
| 7 | 6 | `Items.field_151104_aV` ×1 | `minecraft:red_bed` | 1/2 (GD:970-972) |
| 8 | 7 | `Items.field_151135_aq` ×1 (wooden door item, D4 report :79) | `minecraft:oak_door` (IS port :114) | 1/2 (GD:973-975) |
| 9 | 8 | `Items.field_151035_b` ×1 | `minecraft:iron_pickaxe` (IS port :115) | 1/2 (GD:976-978) |
| 10 | 9 | `Items.field_151040_l` ×1 | `minecraft:iron_sword` (IS port :116) | 1/2 (GD:979-981) |
| 11 | 10 | `Items.field_151036_c` ×1 | `minecraft:iron_axe` (IS port :117) | 1/2 (GD:982-984) |
| 12 | 11 | `Items.field_151133_ar` ×1 | `minecraft:bucket` (IS port :118) | 1/2 (GD:985-987) |
| 13 | 12 | `OreSpawnMain.MyOreSaltBlock` ×4 (OSM:917/1840 "oresalt", registered OSM:2100) | `orespawn:ore_salt` — `ModBlocks.ORE_SALT` (ModBlocks.java:28-30; IS port :119 cross-proof) | 1/2 (GD:988-990) |
| 14 | 13 | `Blocks.field_150486_ae` ×1 | `minecraft:chest` (IS port :120) | 1/2 (GD:991-993) |

→ `RES:loot_table/chests/haunted_house.json`: **14 pools**, each `rolls: 1`, one item
entry with `minecraft:random_chance 0.5` (fixed `set_count` where qty > 1: 8/32/16/4).
This reproduces the independent-Bernoulli distribution exactly; only the fixed SLOT
POSITIONS are lost to the loot system's sequential fill — documented approximation,
igloo-spec §3 precedent (no collisions exist to begin with).

---

## 4. Spawner / mob mapping table

Same trio as the igloo (igloo spec §4) — cites re-verified against source:

| Spawner name | 1.7.10 registration | Port EntityType (cite) |
|---|---|---|
| `"Rat"` (GD:998) | `Rat.class` — `registerGlobalEntityID(..., "Rat", RatID)` OSM:4197, `registerModEntity` OSM:4201 | `ModEntities.ENTITY_RAT` "rat" (ModEntities.java:250-252) |
| `"Ghost"` (GD:1003) | `Ghost.class` — OSM:4047, OSM:4051 | `ModEntities.GHOST` "ghost" (ModEntities.java:549-551) |
| `"Ghost Pumpkin Skelly"` (GD:1008) | `GhostSkelly.class` — OSM:4055, OSM:4059 | `ModEntities.GHOST_SKELLY` "ghost_skelly" (ModEntities.java:555-557) |

**No direct entity spawns** — spawner blocks only (no `spawnEntityInWorld` anywhere in
GD:891-1010; no yaw/NBT/persistence handling to extract).

---

## 5. Block palette

| 1.7.10 field (meta) | Modern block | Used for | Cite |
|---|---|---|---|
| `Blocks.field_150344_f` (meta 0) | `minecraft:oak_planks` | roof + walls | GD:917, 933 |
| `Blocks.field_150347_e` | `minecraft:cobblestone` | floor | GD:921 |
| `Blocks.field_150359_w` | `minecraft:glass` (the full BLOCK — not pane) | perimeter window band at y+3 | GD:926 |
| `Blocks.field_150350_a` | `minecraft:air` | doorway + interior clearing | GD:930, 936 |
| `Blocks.field_150460_al` meta 2 | `minecraft:furnace[facing=north, lit=false]` | furniture | GD:943-944 |
| `Blocks.field_150462_ai` | `minecraft:crafting_table` | furniture | GD:946 |
| `Blocks.field_150486_ae` meta 2 | `minecraft:chest[facing=north]` | 1 loot chest | GD:948-949 |
| `Blocks.field_150474_ac` | `minecraft:spawner` | 3 spawners | GD:995, 1000, 1005 |
| (placement scan only) `Blocks.field_150349_c` | grass block | air-above-grass anchor test | OSW:989 |

No connection-state or attachment concerns (glass blocks, not panes; no fences/torches).
Furnace/chest facing carried in the BlockState (§2.2, WGEN-056).

---

## 6. Footprint extents (relative to origin) + suggested DungeonType

| Axis | Min | Max | Extent | Determined by |
|---|---|---|---|---|
| X | `−3` | `+3` | **7** | `i = -width..width`, width 3 (GD:903, 913) |
| Y | `0` | `+4` | **5** | `k = 0..height+1`, height 3 (GD:904, 915); nothing below y+0 |
| Z | `−3` | `+3` | **7** | `j = -length..length`, length 3 (GD:902, 914) |

Suggested entry (6-int ctor `(xMin, xMax, down, up, zMin, zMax, mode)`, LDP:338-347,
+1 margin; `down = 1` = the no-dig convention, writes start at y+0):

```java
HAUNTED_HOUSE(-4, 4, 1, 5, -4, 4, PlacementMode.SWAMP_GRASS_SURFACE),
```

## 7. Placement — **existing mode fits: `SWAMP_GRASS_SURFACE`** (no new mode needed)

`addHauntedHouse`'s anchoring (§1.1) is the `addSpitBugLair`/`addLeafMonster` shape
that `SWAMP_GRASS_SURFACE` ports (`swampGrassSurfaceOrigin`, LDS:266-284; mode Javadoc
LDP:274-283): 1-in-N gate → structure set, exact-name corner-biome check → biome tag,
`chunk + nextInt(16)` jitter (LDS:269-270 ← OSW:986-987), anchor at the AIR block found
by the Y 100→41 air-over-grass scan — `getBaseHeight` `firstFree` with the hard
`41..100` window (LDS:271-275 ← OSW:988) and the dry-column stand-in for the grass
test (LDS:276-280 ← OSW:989, documented mapping delta established with the mode). The
anchor offset matches exactly: the builder receives `posY`, the air block, no offset
(OSW:990) — precisely what the mode returns (LDS:281). The mode is biome-agnostic by
design (LDP:188-189 comment at LEAF_MONSTER_DUNGEON); biome selectivity lives in the
tag. Two structure-specific deltas to document when wiring (both absorbed, neither
player-visible beyond the C7-family frequency approximation — no NEEDS_* condition):

1. **5 jitter attempts, not 4** (OSW:985 vs LDS:268's shared 4-attempt loop) — a
   marginal hit-rate difference on rough terrain, same approximation family as the
   probe-volume differences ISLANDS_GRASS already absorbs (stinky spec §7).
2. **Air-descent scan** (`posY > 40 && func_147437_c(...)`, OSW:988) instead of the
   siblings' full-window scan — its only observable consequence (attempt fails when
   terrain tops y = 100) is exactly reproduced by the mode's `firstFree > 100 →
   continue` rejection (LDS:275).

Biome mapping (1.7.10 exact names OSW:984 → modern), with the standard merged-biome
deltas (bouncy spec §7 "Desert" precedent):

- `"Plains"` → `minecraft:plains` (leaf monster tag precedent,
  `has_structure/leaf_monster_dungeon.json`). "Sunflower Plains" was a separate 1.7.10
  name and is a separate modern biome — excluded in both eras, faithful; `meadow` is
  new, excluded.
- `"Taiga"` → `minecraft:taiga`. 1.7.10 exact-name excluded "TaigaHills"; modern taiga
  absorbed taiga_hills (1.18) — slightly wider coverage, no separate modern biome to
  exclude. "Mega Taiga"/"Cold Taiga" → modern `old_growth_*_taiga`/`snowy_taiga`
  remain separate biomes, excluded — faithful.
- `"Swampland"` → `minecraft:swamp` (spit bug precedent, LDS:256-257 — mangrove has no
  1.7.10 analogue, excluded).

JSON trio (copy the `leaf_monster_dungeon` trio — same chain, same mode — and rename;
three biomes → tag file, not the inline string bouncy used for its single biome):

- `RES:worldgen/structure/haunted_house.json` — `"type": "orespawn:legacy_dungeon"`,
  `"dungeon_type": "HAUNTED_HOUSE"`,
  `"biomes": "#orespawn:has_structure/haunted_house"`, `"step": "surface_structures"`,
  `"terrain_adaptation": "none"`, `"spawn_overrides": {}`.
- `RES:worldgen/structure_set/haunted_house.json` — §8.
- `RES:tags/worldgen/biome/has_structure/haunted_house.json` —
  `["minecraft:plains", "minecraft:taiga", "minecraft:swamp"]`.

## 8. Structure-set conversion

Effective odds: **1/285 per vanilla-overworld chunk** (OSW:980) before the 3-biome
filter and scan (biome selectivity carried by the `biomes` tag; the ≈0.4% addANest
upstream suppression and the `recently_placed`/pool-roll couplings are absorbed per the
C7 approximation, pattern §1 step 4).

C7 sqrt equivalence: spacing ≈ √285 ≈ 16.9 → **spacing 17, separation 8** — the same
pair the 1/275 leaf monster ships (`leaf_monster_dungeon.json`, 17/8).

Salt: **84372** (assigned to this task; verified free — grep of
`RES:worldgen/structure_set/*.json` at spec date shows shipped OreSpawn salts spanning
84301-84367 plus the vanilla-style 10387399 on dim_villages, all unique; sibling
batch-4 specs on disk claim 84368 spider_hangout, 84369 red_ant_hangout, 84370
frog_pond, 84371 rubber_ducky_pond, 84373/84374 ender_knight_dungeon — 84372 collides
with nothing).

`RES:worldgen/structure_set/haunted_house.json`: random_spread, spacing 17,
separation 8, salt 84372.

## 9. DungeonSpawnerBlock outcome

- Original: `if (type == 5)` → `makeHauntedHouse(world, clickedX, clickedY, clickedZ)`
  — one call, no offset, block read in full (DSB:68-70).
- Port: add `TYPE_HAUNTED_HOUSE = 5` (cite DSB:68-70) and
  `case TYPE_HAUNTED_HOUSE -> LegacyDungeonPiece.buildNow(server, pos,
  DungeonType.HAUNTED_HOUSE)` in `RandomDungeonSpawnerBlockEntity.buildForType`
  (wired types today: 0/1/2/3/7/12-24/26/27/28/29/30/34/35/37/38/39/44/46/47 — 31 of
  50, constants RDS:44-103, cases RDS:157-304; **type 5 currently falls through to the
  generic-dungeon `default` arm, RDS:306** — a player rolling 5 today gets a generic
  dungeon instead of the haunted house, see §12 S1).
- The DSB path bypasses the biome/grass scan entirely — a haunted house built with its
  cobble floor AT the clicked position, in any dimension, floating or embedded, is
  faithful behavior. `buildNow` keeps live RNG for the chest (via the loot table's
  seeded resolution at open — same treatment as every other converted structure).

## 10. Mid-build world READS — classified

1. **Terrain reads: NONE.** Grep of GD:891-1010 for `func_147439_a`/`func_147437_c`
   returns zero hits — no foundation probe, no terrain conditionals, no read-after-
   write geometry. Nothing to model, no `terrainStateIfInChunk`, nothing to FLAG.
2. **Tile-entity fetches** — chest GD:950, spawners GD:996/1001/1006 — self-reads of
   blocks written the line(s) before; absorbed by `piece.placeLootChest` /
   `piece.placeSpawner`. No deviation decision needed.
3. The placement-scan reads (OSW:988-989) live outside the builder and map into
   `SWAMP_GRASS_SURFACE` + the biome tag (§7).
4. `world.field_72995_K` (GD:910) is a side check, not a block read.

No NEEDS_DESIGN_RULING condition arises.

## 11. RNG stream

- **Builder draws: exactly 14** — the chest-slot `nextInt(2)` rolls (GD:952-991, on
  `world.field_73012_v`, executed only inside the `chest != null` self-read guard).
  ALL of them move into the loot JSON as per-pool `random_chance 0.5` (§3). The ported
  generator body therefore consumes **zero** random draws — geometry, furniture,
  facings, and all three spawner positions/names are constants; every per-chunk replay
  pass is trivially identical (same shape as bouncy castle §10).
- **Dispatch-layer draws** (collapse as usual): OSW:980 gate + OSW:986-987 jitter
  (chunk-provided `random`) → structure set + `swampGrassSurfaceOrigin`; DSB:52
  `nextInt(50)` (`world.field_73012_v`) → the port's DSB roll.
- PARITY note (slice report): the original chest fill drew from live shared
  `world.rand`; the port resolves the loot table with the loot-seed machinery at chest
  open — per-chest contents remain random, distribution identical (independent 1/2 per
  slot). No layout RNG exists to become seed-stable.

## 12. Surprises / MISSING-IN-PORT

- **S1 (MISSING-IN-PORT, wrong behavior TODAY)**: the overworld `makeHauntedHouse` has
  no counterpart anywhere in `src/main/java/` (grep `HauntedHouse|HAUNTED`: only the
  Crystal variant in CrystalStructures.java). Worldgen chain link OSW:304-306 falls
  through (no haunted houses generate), and **DSB type 5 currently builds a GENERIC
  DUNGEON via the `default` arm** (RDS:306) — not merely absent but actively
  substituted on the player-facing spawner-block path.
- **S2 — do not confuse with (or reuse) the Crystal variant**: `makeCrystalHauntedHouse`
  (GD:2993, DSB type 25, OSW:1677) is a different structure — crystal palette,
  different chest odds (2/3 rolls on two slots, a guaranteed Kraken Repellent), other
  dimension, decoration-phase port regime. Its port
  (`CrystalStructures.buildCrystalHauntedHouse`, :673-721) is a useful geometry
  cross-check (same 7×7×5 shell shape, same furniture row `(+2/+1/0, +1, +2)`, same
  center spawner stack — confirming this spec's arithmetic) but shares no code path
  with this structure.
- **S3 — InstantShelter kinship**: the geometry is the InstantShelter player-buildable
  house with the orientation HARDCODED (`deltax = 1`, `stuffdir = 2`, GD:905-906 —
  east door, north-facing furniture; IS computes these from player yaw) and the chest
  is the IS kit at 50% per slot WITH the salt slot (§3). The structure is literally a
  starter home overrun by Rat/Ghost/Ghost-Pumpkin-Skelly. Keep the cooked (not raw)
  porkchop — the one item where the two kits differ.
- **S4 — the spawner STACK**: all three spawners occupy one vertical column at the
  house center, `(0, +1..+3, 0)` (GD:995-1009) — not spread around the room. Faithful;
  do not redistribute.
- **S5 — flag-3 writes**: the shell and furniture use `func_147449_b`/`func_72921_c`
  (default flag 3, neighbor updates) rather than the siblings' `setBlockFast` flag-2
  (header trap). No palette block cares; port through `piece.place` and note the
  flag delta — do not try to reproduce flag-3 semantics.
- **S6 — furniture/chest facing is content** (meta 2 = north, GD:944/949): use the
  `placeLootChest` facing overload (LDP:538-545, WGEN-056) and set the furnace
  BlockState FACING — the crystal-variant port dropped the furnace facing
  (defaultBlockState, CrystalStructures.java:707); do not copy that simplification
  here.
- **S7 — unconditional 7×7×5 box write** (GD:913-939): terrain inside is cleared, the
  floor replaces the surface air layer, nothing outside is touched — no yard, no
  foundation skirt. On a slope the house half-buries; over a hollow it overhangs.
  Do not add either.
- **S8 — glass band, not windows**: rule 3 (GD:925-928) makes the ENTIRE k = 3
  perimeter ring glass blocks, corners included — a clerestory band, not punched
  windows, and glass BLOCKS, not panes (contrast stinky house's 8 corner panes).
- **S9 — step-up entrance**: the worldgen anchor is the AIR block above grass
  (OSW:990, no offset — unlike bouncy's −1 sand anchor or stinky's grass anchor), so
  the cobble floor occupies former surface air and the doorway bottom (y+1) sits two
  above the outside grass — entering is a 1-block step up onto the floor. Faithful;
  matches `SWAMP_GRASS_SURFACE`'s air-block return exactly (§7).
- **S10 — scan-shape deltas vs the mode**: 5 attempts (not 4) and the air-descent loop
  (OSW:985, 988) — both documented and absorbed in §7; no new mode, no
  NEEDS_DESIGN_RULING.
- **S11 — mixed RNG sources in the layers**: chain gate + jitter use the chunk
  `random` (OSW:980-987); the chest fill and DSB roll use `world.field_73012_v`
  (GD:952-991, DSB:52). All collapse per §11.
- **S12 — DSB type number is LOW (5)**: it sits in the tree/early-dungeon band of the
  50-table, between BeeHive (4) and MantisHive (6) (DSB:65-73) — batch-4 sweep
  provenance (header); the block was read in full and is single-call.
