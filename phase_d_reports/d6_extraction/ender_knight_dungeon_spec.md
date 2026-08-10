# Ender Knight Dungeon — Extraction Spec (1.7.10 → NeoForge 1.21.1)

Source of truth: `reference_1_7_10_source/sources/danger/orespawn/GenericDungeon.java` —
`makeEnderKnightDungeon` (GD:1794-1901) + its private helper `makeShelves` (GD:1903-1932;
`makeShelves` is called ONLY from inside `makeEnderKnightDungeon` — grep of the whole
original tree hits GD:1837/1858/1890 and nothing else). Next method `makePlayPool` at
GD:1934. Both methods read IN FULL, plus the shared helpers `FastSetBlock` (GD:187-189),
`getSpawnerTileEntity` (GD:86-95), `getChestTileEntity` (GD:75-84).

Citation convention: `GD:NN` = GenericDungeon.java, `OSW:NN` = OreSpawnWorld.java,
`OSM:NN` = OreSpawnMain.java, `DSB:NN` = DungeonSpawnerBlock.java, `AF:NN` =
`AUDIT_FINDINGS.md`, `ME:NN` = `src/main/java/danger/orespawn/ModEntities.java`,
`LDP:NN` = `src/main/java/danger/orespawn/world/structure/LegacyDungeonPiece.java`,
`LDS:NN` = `.../LegacyDungeonStructure.java`, `RDS:NN` =
`src/main/java/danger/orespawn/block/entity/RandomDungeonSpawnerBlockEntity.java`.
The old pre-audit copy under `src/danger/orespawn/` (GenericDungeon.java:1811,
OreSpawnWorld.java:1539/:2126, DungeonSpawnerBlock.java:97) mirrors the reference and is
not shipped code.

**Header traps / summary**

- **Dual-dimension structure** — worldgen in BOTH the End (`addEndKnights`, OSW:1512-1525)
  and the Mining dimension (`addEnderKnight`, OSW:2087-2113). Ender-castle precedent
  applies: enum default mode + per-JSON `placement_mode` override (LDS:39-48), TWO
  structure sets, salts **84373 (End) + 84374 (Mining)** (§7-8).
- **The Mining anchor needs a NEW PlacementMode** (`NEEDS_NEW_MODE`, §7.2): the scan is
  the LOWEST_SURFACE_36 shape but with a grass-only accept and **NO −2 sink**
  (OSW:2097/2108 vs addBasiliskMaze OSW:2583/2594). Reusing LOWEST_SURFACE_36 would bury
  the doorway 2 blocks — player-visible.
- The builder MUTATES `cposx`/`cposz` as it walks east (GD:1807/1819-1820/1839/1841/
  1872-1873/1892-1893) — §2 gives every write in origin-relative coordinates; transcribe
  from the table, not from the raw variable names.
- Ledger note: the task briefing cited "WGEN-046" as naming EnderKnightDungeon a
  mining_biome structure owner — that is a mis-cite (WGEN-046 is the SmallTree/Scraggly
  trees finding, AF:4712-4718). The actual owners are **WGEN-014** (AF:4436-4445, Fix:
  "Port ... KyuubiDungeon and EnderKnightDungeon (WGEN-042) as mining_biome structures",
  AF:4441) and **WGEN-042** (AF:4677-4683, which lists EnderKnightDungeon at AF:4680 and
  prioritizes "KyuubiDungeon/EnderKnightDungeon for Mining", AF:4682). Neither ledger
  entry mentions the End path — the `addEndKnights` call site is a sweep discovery (§12
  S2).
- Zero terrain reads in the builder; zero RNG in the geometry — the only draws are the 28
  shelf-site rolls (§11). No direct entity spawns anywhere in GD:1794-1932.

---

## 1. Entry points — EVERY call site

Grep of the whole original tree for `makeEnderKnightDungeon`: exactly three call sites
(OSW:1521, OSW:2108, DSB:87; GD:1794 is the definition).

| Caller | Coords passed | Notes |
|---|---|---|
| `OreSpawnWorld.addEndKnights` (OSW:1521) | `(posX, posY, posZ)` — posY is the **AIR** block sitting directly on end stone | The End (dim 1) worldgen (§1.1) |
| `OreSpawnWorld.addEnderKnight` (OSW:2108) | `(lowestX, lowestY, lowestZ)` — lowestY is the **GRASS** block itself, **no offset** | Mining-dimension worldgen (§1.2) |
| `DungeonSpawnerBlock` type **11** (DSB:86-88) | `(clickedX, clickedY, clickedZ)`, no offset | roll `type = world.field_73012_v.nextInt(50)` (DSB:52), fires 400 ticks after placement (DSB:39), self + block above deleted first (DSB:50-51). The `if (type == 11)` block read IN FULL: **single builder call, nothing else** (DSB:86-88) — not a two-builder index, no Y offset (contrast types 43/44/45). |

### 1.1 `addEndKnights` — FULL method + return contract (OSW:1512-1525)

```java
// OSW:1512-1525
public void addEndKnights(World world, Random random, int chunkX, int chunkZ) {
    if (random.nextInt(25) != 0) {
        return;
    }
    for (int i = 0; i < 3; ++i) {
        int posX = chunkX + random.nextInt(16);
        int posZ = chunkZ + random.nextInt(16);
        for (int posY = 90; posY > 10; --posY) {
            if (!world.func_147437_c(posX, posY, posZ) || world.func_147439_a(posX, posY - 1, posZ) != Blocks.field_150377_bs || !this.quickSpaceCheck(world, posX, posY, posZ)) continue;
            OreSpawnMain.MyDungeon.makeEnderKnightDungeon(world, posX, posY, posZ);
            return;
        }
    }
}
```

1. Odds gate: `random.nextInt(25) != 0 → return` (OSW:1513-1515), chunk-provided
   `random`.
2. Up to **3 attempts** (OSW:1516): `posX/posZ = chunk + random.nextInt(16)`
   (OSW:1517-1518).
3. Top-down scan `posY = 90` down to `11` inclusive (`posY > 10`, OSW:1519). Accept the
   first Y where ALL of (OSW:1520):
   - block at `(posX, posY, posZ)` is **air** (`func_147437_c`);
   - block below is **end stone** (`field_150377_bs`);
   - `quickSpaceCheck(posX, posY, posZ)` — the 12×12 plane at `posY + 4`, offsets
     `i, k ∈ −2..9`, is all air (OSW:2625-2633).
4. On success: `makeEnderKnightDungeon(world, posX, posY, posZ)` then `return`
   (OSW:1521-1522) — `cposy` is the AIR block on end stone; the room floor (§2) replaces
   that air layer, sitting ON the end stone.

**FULL return contract: `void`.** At most one placement per call; early `return` on the
first successful column; falls off the end silently when the gate fails or all 3 attempts
miss. **`recently_placed` is neither consulted nor set** on this path (like the End side
of the ender castle, ender_castle_spec.md §11.1), **no LessLag gate**, and no
`D4BigSpaceCheck`. Nothing observes success or failure — no WGEN-062-style contract quirk
to preserve. Byte-identical in shape to `addEndReapers` (OSW:1527-1540) and `addHospital`
(OSW:1542-1555) — two of the THREE shipped `END_SURFACE` users (ENDER_REAPER_GRAVEYARD
LDP:184, HOSPITAL LDP:156; the third, ENDER_CASTLE LDP:140, maps `addEnderCastle`, whose
anchor differs only by its 1/50 gate OSW:1558 and the 30×30 `quickBigSpaceCheck`
OSW:1565).

Dispatch chain: `OreSpawnWorld.generate` `case 1:` → `generateEnd` (OSW:219-222,
dimension-gated, not biome-gated); `generateEnd` rolls `i = world.field_73012_v.nextInt(4)`
(**WORLD rand, not the chunk random** — OSW:228, same mixed-stream quirk as the ender
castle) and **`i == 0` → `addEndKnights`** (OSW:229-231). Effective odds: 1/4 × 1/25 =
**1/100 per End chunk** before the scan — the same arithmetic as the hospital and the
reaper graveyard.

### 1.2 `addEnderKnight` — FULL method + return contract (OSW:2087-2113)

```java
// OSW:2087-2113
public boolean addEnderKnight(World world, Random random, int chunkX, int chunkZ) {
    int lowestY = 128;
    int lowestX = chunkX;
    int lowestZ = chunkZ;
    boolean found = false;
    for (int i = 0; i < 16; i += 3) {
        block1: for (int j = 0; j < 16; j += 3) {
            int posX = chunkX + i;
            int posZ = chunkZ + j;
            for (int posY = 128; posY > 30; --posY) {
                if (world.func_147439_a(posX, posY + 1, posZ) != Blocks.field_150350_a || world.func_147439_a(posX, posY, posZ) != Blocks.field_150349_c) continue;
                if (posY >= lowestY) continue block1;
                lowestY = posY;
                lowestX = posX;
                lowestZ = posZ;
                found = true;
                continue block1;
            }
        }
    }
    if (found && lowestY > 40) {
        OreSpawnMain.MyDungeon.makeEnderKnightDungeon(world, lowestX, lowestY, lowestZ);
        recently_placed = 50;
        return true;
    }
    return false;
}
```

1. **No RNG at all** in the method — a deterministic 36-column scan: `i, j ∈
   {0, 3, 6, 9, 12, 15}` (OSW:2092-2095).
2. Per column, descend `posY = 128` down to `31` (`posY > 30`, OSW:2096); accept the
   first Y where the block at `posY + 1` is **air** AND the block at `posY` is **grass
   block** (`field_150349_c`, OSW:2097) — i.e. the column's grass surface.
3. Track the strictly LOWEST such surface; ties keep the first-seen column
   (`posY >= lowestY → continue`, OSW:2098).
4. After the full scan: `found && lowestY > 40` → `makeEnderKnightDungeon(world,
   lowestX, lowestY, lowestZ)` — **the grass block itself, NO −2 sink** — then
   `recently_placed = 50; return true` (OSW:2107-2111). Else `return false` (OSW:2112).

**FULL return contract: `true` ONLY on an actual placement** (which also sets the
50-chunk global cooldown); `false` on scan miss or `lowestY <= 40`. The dispatch call
site ignores the return value (OSW:96-98). No LessLag gate, no clearance probe.

Scan-shape comparison (the §7.2 NEEDS_NEW_MODE evidence): `addBasiliskMaze`
(OSW:2573-2597, the `LOWEST_SURFACE_36` reference) is identical EXCEPT (a) its accept is
any-solid (`!= air`, OSW:2583) where EnderKnight requires grass (OSW:2097), and (b) it
builds at `lowestY − 2` (OSW:2594) where EnderKnight builds at `lowestY` (OSW:2108).
`addKyuubiDungeon` (OSW:2599-2623) matches BasiliskMaze (any-solid, −2 at OSW:2620).
`addAlienWTF` (OSW:2059-2085) and `addBeeHive` (scan at OSW:2040-2050, build
`lowestY + 3` OSW:2052) match ENDERKNIGHT's grass accept; AlienWTF also uses offset 0
(OSW:2080).

Dispatch chain: Mining dimension (`DimensionID2`, OSW:55); rotation gate
`recently_placed == 0 && random.nextInt(95) == 1` then `i = random.nextInt(7)`
(OSW:79-80); **`i == 5` → `addEnderKnight`** (OSW:96-98). When the gate fails the chunk
gets `addGenericDungeon` instead (OSW:102-104). Effective odds: 1/95 × 1/7 = **1/665 per
Mining chunk** — the standard rotation arithmetic (pattern §1 step 4; Kyuubi is the i==1
slot at the same odds).

### 1.3 Port status

**MISSING-IN-PORT — all three entry points.** Grep of `src/main/java` for
`makeEnderKnightDungeon|ENDER_KNIGHT_DUNGEON|ender_knight_dungeon`: zero hits. No
`DungeonType` entry, no structure/set/tag JSONs, and DSB type 11 currently falls through
to the generic-dungeon default (RDS:306). Ledger owners: WGEN-014 (AF:4436-4445) +
WGEN-042 (AF:4677-4683).

---

## 2. Geometry — rule by rule

`makeEnderKnightDungeon(world, cposx, cposy, cposz)` (GD:1794). Locals: `height = 6`
(GD:1798), `zwidth` re-assigned per section (GD:1799/1809/1821/1842/1874/1894). The
method INCREMENTS `cposx` and DECREMENTS/INCREMENTS `cposz` as it walks; every position
below is **relative to the ORIGINAL `(cposx, cposy, cposz)` args** (X0/Y0/Z0). All
geometry writes go through `FastSetBlock` (flag-2, GD:187-189) → `piece.place`; the
spawners/chests use `func_147465_d(..., 0, 2)` + TE fetch → `piece.placeSpawner` /
`piece.placeLootChest`.

The build is a west→east sequence of full YZ slabs; every cell of every slab is written
(air included), so terrain inside the envelope is fully replaced — EXCEPT the cells
called out in §12 S6.

### 2.1 Slice map (all ranges inclusive, rel. X0/Y0/Z0)

| Section | Rel X | Rel Z | Rel Y | Content (rule order per cell) | Cite |
|---|---|---|---|---|---|
| A — entrance cut | `0..3` | `0..4` | `0..4` | air, unconditional (4×5×5 clearing; `++cposx` after each slice, GD:1807) | GD:1801-1808 |
| B — front wall | `4` | `0..4` | `0..5` | obsidian; then `k==2 && 1<=j<=3` → air (1-wide, 3-tall doorway at Z+2, Y+1..+3) | GD:1810-1818 (door GD:1813-1815) |
| — | | | | `++cposx` (→X0+5), `--cposz` (→Z0−1) | GD:1819-1820 |
| C — 7-wide room slice | `5` | `−1..+5` (`k=0..6`) | `0..5` | 1: air. 2: `j==0 \|\| j==5` → obsidian. 3: `j==0 && 0<k<6` → **end stone** (floor). 4: `k==0 \|\| k==6` → obsidian (Z walls, full height). Shelf sites (§2.2) at `k ∈ {1,2,4,5}` → rel Z `{0,+1,+3,+4}` | GD:1822-1838 (shelf gate GD:1836) |
| — | | | | `--cposz` (→Z0−2) | GD:1839 |
| D — 9-wide main room, 5 slices | `6..10` (m=0..4, `++cposx` first, GD:1841) | `−2..+6` (`k=0..8`) | `0..5` | same 4 rules, end-stone floor at `0<k<8`; Z walls `k∈{0,8}`. Shelf sites at `k ∈ {1,2,6,7}` → rel Z `{−1,0,+4,+5}` (GD:1857-1859). **Spawners** at `m==2 && k==4` (GD:1860-1869, §2.3) | GD:1840-1871 |
| — | | | | `++cposz` (→Z0−1), `++cposx` (→X0+11) | GD:1872-1873 |
| E — 7-wide room slice | `11` | `−1..+5` | `0..5` | identical to C, shelf sites `k ∈ {1,2,4,5}` → rel Z `{0,+1,+3,+4}` | GD:1875-1891 (shelf gate GD:1889-1890) |
| — | | | | `++cposz` (→Z0), `++cposx` (→X0+12) | GD:1892-1893 |
| F — back wall | `12` | `0..4` | `0..5` | obsidian, unconditional (solid 5×6 slab) | GD:1895-1900 |

Net shape: a 4-long, 5×5 air approach cut leading to a 1×3 doorway in an obsidian front
wall, opening into a 6-tall obsidian room with an **end-stone floor**, octagonal plan
(7-wide at X+5/X+11, 9-wide at X+6..+10), sealed by a solid obsidian back wall. Interior
walking level is Y+1 (floor slabs at Y+0). The doorway lane (rel Z +2) runs clear from
the entrance to the spawner column at room center.

### 2.2 `makeShelves` (GD:1903-1932) — 28 sites

Called with `(sliceX, cposy + 1, sliceZ)` — base Y is **floor + 1** — at the 28 interior
columns listed in §2.1 (C: 4, D: 5×4 = 20, E: 4), in strict loop order. Per site, one
draw `i = world.field_73012_v.nextInt(4)` (GD:1906) selects:

| `i` | Result | Cite |
|---|---|---|
| 0 | **chest** at the base cell (`func_147465_d` + TE fetch), filled `3 + nextInt(5)` = **3..7** pulls of `KnightContentsList` (§3) | GD:1908-1917 (fill GD:1915) |
| 1 | **bookshelf** column, height `1 + nextInt(4)` = 1..4, base upward (Y+1..≤Y+4) | GD:1918-1924 |
| 2 | **cobweb** column, height `1 + nextInt(4)` = 1..4 | GD:1925-1931 |
| 3 | **nothing written** — the slice loop's interior air stands | GD:1906 (falls through all three ifs) |

The shelf columns overwrite interior-air cells the same slab loop wrote moments earlier
— pure writes, no reads (§10). Max column top is Y+4, one below the Y+5 ceiling.
Expected yield ≈ 7 chests per dungeon (28 × 1/4), but 0..28 possible.

### 2.3 Spawners — 2, stacked at room center (GD:1860-1869)

At `m == 2 && k == 4` in section D → rel `(X+8, ·, Z+2)`, the exact room center on the
doorway lane:

| # | Position (rel) | Mob name string | Cite |
|---|---|---|---|
| 1 | `(8, +2, 2)` | `"Ender Knight"` | GD:1861-1865 |
| 2 | `(8, +3, 2)` | `"Ender Knight"` | GD:1866-1869 |

Both `func_147465_d(..., field_150474_ac, 0, 2)` + `getSpawnerTileEntity` +
`func_98272_a` — default vanilla spawner NBT beyond the entity id. Note the spawners sit
at Y+2/Y+3, floating in the interior air column (nothing beneath them at Y+1) — faithful,
flag-2 writes hold them.

---

## 3. Loot — FULL transcription

`KnightContentsList` (GD:50) — constructor semantics `(item, meta, minStack, maxStack,
weight)`. **5 entries, total weight = 95** (20+20+15+15+25). Consumed at exactly ONE
call site and ONE fill formula: `3 + nextInt(5)` (GD:1915) → `pools[0].rolls` uniform
**min 3, max 7**. Grep of the whole original tree: `KnightContentsList` appears only at
GD:50 (declaration) and GD:1911 (this use) — no shared-list twin, so ONE table per the
(list, fill formula) rule (pattern §1 step 5 as amended).

| # | 1.7.10 item | Modern / port mapping | min | max | weight |
|---|---|---|---|---|---|
| 1 | `Items.field_151121_aF` | `minecraft:paper` | 2 | 8 | 20 |
| 2 | `Item.func_150898_a(Blocks.field_150344_f)` (oak planks, meta 0) | `minecraft:oak_planks` | 4 | 8 | 20 |
| 3 | `Items.field_151061_bv` | `minecraft:ender_eye` | 2 | 8 | 15 |
| 4 | `Items.field_151079_bi` | `minecraft:ender_pearl` | 2 | 8 | 15 |
| 5 | `Items.field_151078_bh` | `minecraft:rotten_flesh` | 6 | 16 | 25 |

→ `RES:loot_table/chests/ender_knight_dungeon.json`, rolls uniform 3-7, one entry per
row, `set_count` uniform per row. All vanilla items — no data components, no
MISSING-IN-PORT items. Documented approximation (pattern §1 step 5): original pulls
landed in random chest slots with overwrite collisions; a loot pool never collides.

---

## 4. Spawner / mob mapping table

| Spawner name | 1.7.10 registration | Port EntityType |
|---|---|---|
| `"Ender Knight"` (GD:1864, 1869) | `EnderKnight.class` — `registerGlobalEntityID(..., "Ender Knight", EnderKnightID)` OSM:4125, `registerModEntity` OSM:4129 | `ModEntities.ENDER_KNIGHT` "ender_knight" (ME:85-87) — same mapping the ender castle uses (ender_castle_spec.md §9) |

**No direct entity spawns** anywhere in GD:1794-1932 (no `spawnEntityInWorld`) — spawner
blocks only; no yaw/NBT/persistence extraction needed.

Spawn-gate compatibility: the port `EnderKnight.checkSpawnRules`
(src/main/java/danger/orespawn/entity/EnderKnight.java:132-140, orig
EnderKnight.java:256-277) is darkness + night + `y >= 30`, **with the near-own-spawner
bypass** (EnderKnight.java:136) — so both spawners function inside the lit-or-dark room
regardless of time. The Mining anchor (`lowestY > 40`) always satisfies the y>=30 floor;
the End anchor window (Y 11..90) admits placements below Y30 (spawners then sit at
Y+2/+3 = 13..31), where ONLY the near-own-spawner bypass keeps the spawners functional
(corrected in verification — the earlier claim that both anchors satisfy the floor was
wrong for End anchors at Y 11..29). No change needed in code — noted so nobody "fixes"
the daytime gate or the y-floor against the dungeon.

---

## 5. Block palette

| 1.7.10 field | Modern block | Used for | Cite |
|---|---|---|---|
| `Blocks.field_150350_a` | `minecraft:air` | entrance cut, doorway, room interior | GD:1804, 1814, 1824 |
| `Blocks.field_150343_Z` | `minecraft:obsidian` | front/back walls, Z walls, ceiling, floor edge cells | GD:1812, 1826, 1832, 1847, 1853, 1879, 1885, 1897 |
| `Blocks.field_150377_bs` | `minecraft:end_stone` | room floor interior (`j==0 && 0<k<zwidth−1`) | GD:1829, 1850, 1882 |
| `Blocks.field_150474_ac` | `minecraft:spawner` | 2 spawners | GD:1861, 1866 |
| `Blocks.field_150486_ae` | `minecraft:chest` | shelf chests (i==0 roll) | GD:1912 |
| `Blocks.field_150342_X` | `minecraft:bookshelf` | shelf columns (i==1 roll) | GD:1919 |
| `Blocks.field_150321_G` | `minecraft:cobweb` | shelf columns (i==2 roll) | GD:1926 |
| (placement scans only) `field_150377_bs` end stone / `field_150349_c` grass block | — | End / Mining anchor tests | OSW:1520 / OSW:2097 |

All plain `defaultBlockState()` blocks — no connection-state, facing, or decay concerns
(chest facing: the original places meta 0 via `func_147465_d(..., 0, 2)`; default state,
matching every existing generator's chest treatment).

---

## 6. Footprint extents + suggested DungeonType

Relative to `(cposx, cposy, cposz)`:

| Axis | Min | Max | Extent | Determined by |
|---|---|---|---|---|
| X | `0` | `+12` | **13** | entrance cut at X+0 (GD:1801-1808) … back wall at X0+12 after 12 net `++cposx` (GD:1893-1900) |
| Y | `+0` | `+5` | **6** | floor slabs at `cposy + 0` (`j==0`); ceiling `j==5` (`height−1`, GD:1798); shelves top out at +4 (§2.2); **nothing below origin level** |
| Z | `−2` | `+6` | **9** | section D `k − 2`, `k = 0..8` (GD:1839-1843) |

Suggested entry (asymmetric 6-int ctor `(xMin, xMax, down, up, zMin, zMax, mode)`, +1
margin; `down = 1` per the no-dig convention of MINI_DUNGEON/CEPHADROME_ALTAR/
STINKY_HOUSE, LDP:192/194/208):

```java
ENDER_KNIGHT_DUNGEON(-1, 13, 1, 6, -3, 7, PlacementMode.END_SURFACE),
```

Default mode = the End path, mirroring ENDER_CASTLE's default-END_SURFACE +
islands-override precedent (LDP:136-140); the Mining JSON overrides per §7.2. Dispatch
case goes in the `postProcess` switch beside the other D6 entries (switch LDP:421-455;
batch-3 cases LDP:449-454).

## 7. Placement — two dimensions, ender_castle override precedent

Per-JSON `placement_mode` override (LDS:39-48, `findGenerationPoint` reads
`placementOverride.orElse(dungeonType.placement)`, LDS:66) — shipped for
`ender_castle_islands.json` (`"placement_mode": "ISLANDS_GRASS"`).

### 7.1 End path — **existing mode fits: `END_SURFACE`** (no new mode)

`addEndKnights`' anchoring (§1.1) is byte-identical in shape to `addEndReapers`
(OSW:1527-1540) and `addHospital` (OSW:1542-1555), two of the three structures
`endSurfaceOrigin` already ports (LDS:186-202; the third END_SURFACE user,
ENDER_CASTLE, maps `addEnderCastle` OSW:1557-1570 — same shape but a 1/50 gate and the
30×30 `quickBigSpaceCheck`): 3 attempts of `chunk + nextInt(16)` jitter (LDS:188-190 ←
OSW:1516-1518), air-on-end-stone anchor via first-free-Y with the hard Y 11..90 window
(LDS:195-197 ← OSW:1519-1520), and the `quickSpaceCheck` 12×12 air plane at +4
(OSW:2625-2633) approximated by `footprintClearAbove` (LDS:198, 343-359 — the same
pairing the hospital's identical probe uses). Worth appending to the END_SURFACE Javadoc
(LDS:170-185 currently names only addEnderCastle/addHospital): `addEndKnights`
(OSW:1512-1525) and `addEndReapers` (OSW:1527-1540) are two more exact users.

The anchor IS the air block (builder floor replaces it, sitting on end stone) — exactly
what `endSurfaceOrigin` returns. No adjustment.

### 7.2 Mining path — **NEEDS_NEW_MODE** (`LOWEST_GRASS_36`, suggested)

No existing mode matches `addEnderKnight`'s anchor. The original scan, quoted in full in
§1.2 (OSW:2087-2113), is the `LOWEST_SURFACE_36` shape with two deltas:

1. **Accept condition**: grass block at `posY` (OSW:2097), not any-solid
   (addBasiliskMaze OSW:2583). Under `getBaseHeight` both collapse to the same
   noise-surface probe — grass identity is not predictable pre-terrain; the Mining
   surface is grass, and the delta is documented the same way SWAMP_GRASS_SURFACE
   documents its grass approximation (LDS:260-264). NOT the blocking issue.
2. **Anchor offset**: `lowestY` with **NO −2 sink** (OSW:2108), where
   `lowestSurfaceOrigin` bakes in `lowestSurfaceY - 2` (LDS:166 ← OSW:2594). This IS
   player-visible: sinking the build 2 blocks buries the Y+1..+3 doorway bottom and the
   floor, and beheads nothing (the room would poke +3 instead of +5) — a different
   structure on the ground. Do not reuse LOWEST_SURFACE_36; do not compensate inside the
   generator (the DSB path shares the generator and takes the clicked pos raw).

Suggested new mode: `LOWEST_GRASS_36` — a copy of `lowestSurfaceOrigin` (LDS:137-167:
same 6×6 offsets {0,3,6,9,12,15}, same Y 31..128 window mapped to the noise surface,
same strictly-lowest/first-seen-wins accounting, same `lowestY > 40` hard gate, same
no-RNG determinism) returning `new BlockPos(lowestX, lowestSurfaceY, lowestZ)` — no
`− 2`. Javadoc should cite addEnderKnight (OSW:2087-2113) and note the grass-accept
approximation plus the two future users: `addAlienWTF` (OSW:2059-2085, identical scan
and offset — the current `ALIEN_WTF` entry still anchors SURFACE_CENTER, LDP:93, a
pre-D5 reconciliation candidate; do NOT rewire it in this slice) and `addBeeHive`
(OSW:2031-2057, same scan at `lowestY + 3`, OSW:2052).

`recently_placed` (gate OSW:79, set OSW:2109) collapses into structure-set separation
(C7 treatment, pattern §1 step 4).

### 7.3 JSON trios (ender_castle naming precedent: `_end` / `_mining` suffixes)

- `RES:worldgen/structure/ender_knight_dungeon_end.json` — `"type":
  "orespawn:legacy_dungeon"`, `"dungeon_type": "ENDER_KNIGHT_DUNGEON"`, `"biomes":
  "#minecraft:is_end"` (the shipped End choice — ender_castle_end.json, hospital.json,
  graveyard.json all use the full `is_end` tag; the faithful-to-code option per
  ender_castle_spec.md §11.1, adopted by all three precedents), `"step":
  "surface_structures"`, `"terrain_adaptation": "none"`, `"spawn_overrides": {}`, **no
  placement_mode key** (enum default END_SURFACE).
- `RES:worldgen/structure/ender_knight_dungeon_mining.json` — same plus
  `"placement_mode": "LOWEST_GRASS_36"` and `"biomes":
  "#orespawn:has_structure/ender_knight_dungeon_mining"`.
- `RES:tags/worldgen/biome/has_structure/ender_knight_dungeon_mining.json` —
  `["orespawn:mining_biome"]` (matching `has_structure/kyuubi_dungeon.json`; WGEN-014's
  "mining_biome structures" instruction, AF:4441).
- Two structure sets — §8. (No tag file needed for the End side; `#minecraft:is_end` is
  vanilla.)

## 8. Structure-set conversion (C7 sqrt equivalence)

**End set**: 1/4 (`generateEnd` roll, OSW:228-231) × 1/25 (OSW:1513) = **1/100 per End
chunk** → spacing √100 = 10 → **spacing 10, separation 5** — identical to the shipped
1/100 End precedents `hospital.json` and `graveyard.json` (both 10/5).
`RES:worldgen/structure_set/ender_knight_dungeon_end.json`: random_spread, spacing 10,
separation 5, **salt 84373**.

**Mining set**: 1/95 (OSW:79) × 1/7 (OSW:80, i==5) = **1/665 per Mining chunk** →
spacing √665 ≈ 25.8 → **spacing 26, separation 13** — identical to the mining-rotation
precedent `kyuubi_dungeon.json` (26/13). The 3-attempt/36-column scans and
`recently_placed` collapse into separation per C7.
`RES:worldgen/structure_set/ender_knight_dungeon_mining.json`: random_spread, spacing 26,
separation 13, **salt 84374** (the reserved second salt — the two-set condition is met).

Salt check (grep `RES:worldgen/structure_set/*.json`, 2026-08-10): OreSpawn salts in use
are 84301-84367 plus 10387399 (dim_villages); 84373 and 84374 are free, as assigned.

## 9. DungeonSpawnerBlock outcome

- Original: `if (type == 11)` → `makeEnderKnightDungeon(world, clickedX, clickedY,
  clickedZ)` — one call, no offset, block read in full (DSB:86-88).
- Port: add `// orig DungeonSpawnerBlock.java:86-88 — makeEnderKnightDungeon (single
  call)` `private static final int TYPE_ENDER_KNIGHT_DUNGEON = 11;` and
  `case TYPE_ENDER_KNIGHT_DUNGEON -> LegacyDungeonPiece.buildNow(server, pos,
  DungeonType.ENDER_KNIGHT_DUNGEON)` in `RandomDungeonSpawnerBlockEntity.buildForType`
  (constants RDS:44-103, switch RDS:155-307; wired types today: 0/1/2/3/7/12-24/26-30/
  34/35/37/38/39/44/46/47 — **type 11 currently falls through to the generic-dungeon
  default, RDS:306**).
- The DSB path bypasses both dimension gates and both scans entirely — an Ender Knight
  dungeon built from the clicked position in any dimension, floating or embedded, with
  its end-stone floor and entrance cut carving whatever is there, is faithful behavior.
  `buildNow` keeps live RNG, so shelf contents differ per placement as in 1.7.10.

## 10. Mid-build world READS — classified

1. **Terrain reads: none.** GD:1794-1932 contains no `func_147439_a`/`func_147437_c`
   call. Every `blk` comparison is against the local just assigned; every cell of every
   slab is written unconditionally (air included) — there are no write-skips decided by
   world state.
2. **Tile-entity fetches** (GD:1862, 1867 spawners; GD:1913 chest) — self-reads of
   blocks written the line before; absorbed by `piece.placeSpawner` /
   `piece.placeLootChest`. No deviation decision needed.
3. `makeShelves` overwriting the slab loop's interior air (§2.2) is write-after-write
   within one generator pass — not a read; no in-memory model needed.
4. The placement-scan reads (OSW:1519-1520 + quickSpaceCheck OSW:2625-2633;
   OSW:2096-2097) live outside the builder and map into END_SURFACE / the §7.2 new mode.

No `terrainStateIfInChunk` usage needed; no NEEDS_DESIGN_RULING condition arises.

## 11. RNG stream accounting

All builder draws use `world.field_73012_v` (GD:1906, 1915, 1920, 1927). In the port:

- **Stays in the generator** (piece RandomSource, identical in every per-chunk replay):
  the 28 shelf-site draws, in strict source order (C k=1,2,4,5 → D m=0..4 × k=1,2,6,7 →
  E k=1,2,4,5): per site one unconditional `nextInt(4)` (GD:1906), plus one `nextInt(4)`
  height draw IFF the first draw was 1 or 2 (GD:1920/1927). The conditional draw depends
  only on the earlier draw, never on world state or `inChunk` — pattern §1 step 3 rule 1
  is satisfied with the draws left in place. The geometry itself and the spawners draw
  nothing. Fixed 28-site count in every pass.
- **Moves to JSON**: the chest fill `3 + nextInt(5)` (GD:1915) → loot rolls 3-7, plus all
  in-fill draws inside `func_76293_a`. (Removing these from the builder stream is safe:
  every pass removes them identically.)
- **Collapses into placement/set machinery**: End — the WORLD-rand `nextInt(4)`
  dimension roll (OSW:228, mixed-stream quirk as documented for the ender castle), the
  chunk-rand 1/25 gate (OSW:1513) and per-attempt `nextInt(16)` jitter (OSW:1517-1518,
  reproduced by `endSurfaceOrigin`, LDS:188-190). Mining — the 1/95 + 1/7 rotation rolls
  (OSW:79-80); `addEnderKnight` itself draws nothing, and neither does the suggested
  mode (matching `lowestSurfaceOrigin`'s zero-draw determinism). DSB — the live
  `nextInt(50)` roll (DSB:52 → RDS:136).
- PARITY note (slice report): original shelf rolls came from shared live `world.rand`;
  the port's per-piece seed makes each dungeon's shelf layout seed-stable. The DSB
  `buildNow` path keeps live-RNG behavior.

## 12. Surprises / court S-items / MISSING-IN-PORT

- **S1 (MISSING-IN-PORT)**: builder, DungeonType, all three entry points, all JSONs —
  nothing exists in the port (§1.3). Worldgen End i==0, Mining i==5, and DSB type 11 all
  fall through today.
- **S2 — the End path is an audit-ledger gap**: WGEN-014/WGEN-042 name
  EnderKnightDungeon only as a MINING structure (AF:4441, 4682 — the ledger's End list
  is "Hospital/EnderCastle", plus the graveyard); `addEndKnights` (OSW:1512-1525) makes
  it a dual-dimension structure. This is why it surfaced in the sweep despite not being
  on the 22-list. (The briefing's "WGEN-046" pointer is a mis-cite — WGEN-046 is trees,
  AF:4712-4718; cite WGEN-014/WGEN-042.)
- **S3 — NEEDS_NEW_MODE (the one flag)**: the Mining anchor needs a no-sink
  lowest-grass-of-36 mode (§7.2). Everything else fits shipped machinery. Not a
  NEEDS_DESIGN_RULING situation: the mode is a 2-line variant of `lowestSurfaceOrigin`
  with the original quoted in §1.2.
- **S4 — end-stone floor everywhere**: the room floor is END STONE in both dimensions
  (GD:1829/1850/1882) — in the Mining dimension that imports end stone into a grass
  world. Signature content; do not "correct" it to stone/grass.
- **S5 — anchor semantics differ per dimension**: End `cposy` = the AIR block on end
  stone (OSW:1520); Mining `cposy` = the GRASS block itself (OSW:2097/2108). The same
  builder therefore sits one block lower relative to the surface in Mining (floor
  replaces the grass) than in the End (floor replaces the air above the end stone). Both
  are faithful; the two placement modes encode exactly this difference — do not
  "harmonize" with a Y offset anywhere.
- **S6 — unwritten cells INSIDE the §6 extents rectangle** (corrected in verification —
  the earlier list of 8 seam cells was incomplete): the slabs only ever cover X0..3+X4
  Z `0..4`, X5+X11 Z `−1..+5`, X6..10 Z `−2..+6`, X12 Z `0..4`, so within the
  X `0..12` × Z `−2..+6` rectangle the following columns are NEVER written (any Y):
  - entrance flank: `X+0..+4` × Z `{−2, −1, +5, +6}` (20 columns — sections A/B span
    only Z 0..4, GD:1802/1810);
  - room-corner seams: `(X+5, Z−2)`, `(X+5, Z+6)`, `(X+11, Z−2)`, `(X+11, Z+6)`
    (C/E span Z −1..+5, GD:1821-1822/1874-1875);
  - back-wall flank: `X+12` × Z `{−2, −1, +5, +6}` (F spans only Z 0..4, GD:1894-1895);
  - partial column: `Y+5` over the entrance cut (X 0..3 × Z 0..4 — section A writes
    Y 0..4 only, GD:1803; every other section writes Y 0..5).
  Of these, the 8 cells flush against the room's stepped walls — `(X+4, Z−1)`,
  `(X+4, Z+5)`, `(X+5, Z−2)`, `(X+5, Z+6)`, `(X+11, Z−2)`, `(X+11, Z+6)`,
  `(X+12, Z−1)`, `(X+12, Z+5)` — are sealed off diagonally by the adjacent slabs'
  Z-walls, so pre-existing terrain survives there flush against the outside of the
  room (visible as embedded terrain columns on DSB/embedded placements); the rest sit
  clear of the plan. Nothing at all is written outside the §6 envelope. Faithful — no
  clear-envelope loop exists.
- **S7 — 1-block step at the door**: the entrance cut clears Y+0..+4 (walking surface =
  the block BELOW origin level), while the room floor is AT Y+0 (walking surface Y+1);
  the doorway starts at Y+1. Players step up once entering. Keep the cut's 5-block
  height (one less than the room's 6).
- **S8 — floating spawners**: the two Ender Knight spawners hang at Y+2/Y+3 over the
  doorway lane with air beneath (§2.3) — flag-2 writes, no support. Do not "seat" them
  on the floor.
- **S9 — shelf rolls can block nothing or the walls' worth**: `i==3` writes nothing
  (1/4 per site), and chest/bookshelf/web only ever occupy the 28 wall-adjacent interior
  columns — the doorway lane (Z+2 in C/E frames) and the room's middle stripe carry no
  shelf sites. Preserve the exact site list and order (§11 depends on it).
- **S10 — no LessLag, no cooldown on the End path**: neither worldgen path consults
  LessLag; only the Mining path touches `recently_placed` (§1.1/1.2). Do not copy the
  Islands adds' gates in.
- **S11 — spawn-gate interplay** (§4): the port EnderKnight's darkness/night/y>=30
  rules are bypassed near its own spawner (EnderKnight.java:136) — required for the
  room to function; noted so the bypass isn't "cleaned up".
- **S12 — future users of the new mode**: `addAlienWTF` (OSW:2059-2085) and `addBeeHive`
  (OSW:2031-2057, +3) share the grass scan; ALIEN_WTF's current SURFACE_CENTER anchor
  (LDP:93) is a latent reconciliation item — out of scope here, do not rewire in this
  slice.
