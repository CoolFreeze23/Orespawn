# Spider Hangout — Extraction Spec (1.7.10 → NeoForge 1.21.1)

Source of truth: `reference_1_7_10_source/sources/danger/orespawn/GenericDungeon.java` —
`makeSpiderHangout` (GD:6989-7043; next method `makeRedAntHangout` at GD:7045). Method
read IN FULL, plus the shared helper it uses (`getSpawnerTileEntity` GD:86-95; geometry
writes go through `OreSpawnMain.setBlockFast(..., meta 0, flags 2)`, GD:7005, def
OSM:5833). All coordinates are relative to the build origin `(cposx, cposy, cposz)` =
the three int args. No method-local size constants — every bound is a literal. Locals:
`i/j/k` reused across loops, `Entity var8` (the Robot Spider), `tileentitymobspawner`.

Citation convention: `GD:NN` = GenericDungeon.java, `OSW:NN` = OreSpawnWorld.java,
`OSM:NN` = OreSpawnMain.java, `DSB:NN` = DungeonSpawnerBlock.java, `SR:NN` = orig
`SpiderRobot.java`, `SD:NN` = orig `SpiderDriver.java`, `LDP:NN` =
`src/main/java/danger/orespawn/world/structure/LegacyDungeonPiece.java`, `LDS:NN` =
`.../LegacyDungeonStructure.java`, `RDS:NN` =
`src/main/java/danger/orespawn/block/entity/RandomDungeonSpawnerBlockEntity.java`.

Header traps / summary:
- **No chest, no loot** — the builder contains no `field_150486_ae` and no
  `func_76293_a` anywhere in GD:6989-7043. §3 is intentionally empty; do not invent
  loot.
- **One direct entity spawn** — a "Robot Spider" at the pad center (GD:7038-7042),
  extracted exactly in §4.1. Persistence mapping is the OPPOSITE of the Damsel
  Girlfriend ruling: use `spawnPersistent`, not `spawnEntity` (§4.1, §12 S5).
- **Zero mid-build world reads** beyond the 12 spawner tile-entity self-reads (§10).
- **Exactly one in-builder RNG draw** (the robot's yaw, GD:7040) — the geometry is
  fully deterministic (§11).
- Worldgen path is **Village dimension (DimensionID3)**, damsel-family scan, builder
  anchored at `posY − 1` (the grass block) — existing mode `VILLAGE_GRASS_SURFACE`
  fits exactly (§7).
- `SpiderDriverEnable == 0` config kill-switch on the worldgen path (OSW:1323-1325) —
  COURT item, precedents both ways (§12 S6).

---

## 1. Entry points — EVERY call site

Grep of the whole original tree for `makeSpiderHangout`: exactly two call sites
(OSW:1332 and DSB:198; GD:6989 is the definition; OSW:125 is the dispatch of the
worldgen wrapper `addSpiderHangout` OSW:1319). Reachable on both paths — not dead
code.

| Caller | Signature used | Coords passed | Notes |
|---|---|---|---|
| `OreSpawnWorld.addSpiderHangout` (OSW:1332) | `OreSpawnMain.MyDungeon.makeSpiderHangout(world, posX, posY - 1, posZ)` | scan hit **minus 1** — the origin is the GRASS BLOCK itself, not the air above (§1.1) | worldgen path, **Village dimension (DimensionID3) only** (§1.2) |
| `DungeonSpawnerBlock` type **48** (DSB:197-199) | `...makeSpiderHangout(world, clickedX, clickedY, clickedZ)` | player-placed block pos, no offset | roll `type = world.field_73012_v.nextInt(50)` (DSB:52), fires 400 ticks after placement (DSB:39), self + block above deleted first (DSB:50-51). The `if (type == 48)` block read IN FULL: **single builder call, nothing else** (DSB:197-199) — not a two-builder index. |

### 1.1 `addSpiderHangout` — FULL method + return contract (OSW:1319-1338)

```java
// OSW:1319-1338
public boolean addSpiderHangout(World world, Random random, int chunkX, int chunkZ) {
    if (random.nextInt(350) != 0) {
        return false;
    }
    if (OreSpawnMain.SpiderDriverEnable == 0) {
        return false;
    }
    for (int i = 0; i < 4; ++i) {
        int posX = chunkX + random.nextInt(16);
        int posZ = chunkZ + random.nextInt(16);
        boolean which = false;
        for (int posY = 100; posY > 40; --posY) {
            if (world.func_147439_a(posX, posY, posZ) != Blocks.field_150350_a
                || world.func_147439_a(posX, posY - 1, posZ) != Blocks.field_150349_c
                || !this.quickSpaceCheck(world, posX, posY - 1, posZ)) continue;
            OreSpawnMain.MyDungeon.makeSpiderHangout(world, posX, posY - 1, posZ);
            recently_placed = 50;
            return true;
        }
    }
    return false;
}
```

1. Odds gate: `random.nextInt(350) != 0 → return false` (OSW:1320-1322) — **1/350**,
   chunk `random`. Drawn BEFORE the config gate — with the config off the chunk stream
   still consumes this one draw (§11).
2. Config gate: `OreSpawnMain.SpiderDriverEnable == 0 → return false` (OSW:1323-1325).
   Declared OSM:414; config `config.get(mobs, "SpiderDriverEnable", 1)` — **default
   enabled** (OSM:6368). See §12 S6 for the port treatment (COURT item).
3. Up to 4 attempts (OSW:1326): `posX/posZ = chunk + random.nextInt(16)`
   (OSW:1327-1328). Dead local `which` (OSW:1329) — ignore.
4. Column scan `posY = 100` down to `41` inclusive (OSW:1330): require air at `posY`
   AND **grass block** (`field_150349_c`) at `posY − 1` AND
   `quickSpaceCheck(world, posX, posY - 1, posZ)` (OSW:1331).
5. Hit → `makeSpiderHangout(world, posX, posY - 1, posZ)` — **origin = the grass
   block** — then `recently_placed = 50`, `return true` (OSW:1332-1334).
6. All attempts miss → `return false` (OSW:1337).

**FULL return contract: `true` ONLY on an actual placement** (which also sets the
50-chunk global cooldown, OSW:1333). Odds fail, config-off, and scan fail all return
`false`. No WGEN-062-style early-true quirk. The caller ignores the return value
(§1.2) — chaining in this dimension is done with the `recently_placed` gates OUTSIDE
the methods.

**Line-for-line the `addDamselInDistress` shape** (OSW:1301-1317, the
`VILLAGE_GRASS_SURFACE` reference): the attempt loop, jitter, Y window, air-over-grass
test, `quickSpaceCheck` at `posY − 1`, the `posY − 1` anchor, cooldown and return
contract are IDENTICAL (compare OSW:1326-1337 vs OSW:1305-1316). The only differences
sit BEFORE the loop: 1/350 instead of 1/250, plus the config gate.

`quickSpaceCheck` (OSW:2625-2633): 12×12 clearance probe — every block at
`(posX−2..posX+9, argY+4, posZ−2..posZ+9)` must be air. Called here with
`argY = posY − 1`, so the probed plane is **anchor + 4**. Placement scan, not a
mid-build read → maps into the PlacementMode (§7). Note it is SMALLER than the 20×20
pad this structure clears (§12 S7).

### 1.2 Worldgen dispatch chain (complete)

- `OreSpawnWorld.generate` (OSW:32), `world.field_73011_w.field_76574_g ==
  OreSpawnMain.DimensionID3` branch (OSW:114-131): mosquitos (config-gated,
  OSW:115-117), `addAnts(..., 4)` (OSW:118), `addAppleTrees` (OSW:119),
  `addGenericDungeon` (unconditional, OSW:120), then
  `if (recently_placed == 0) addDamselInDistress` (OSW:121-123), then
  `if (recently_placed == 0) addSpiderHangout` (**OSW:124-126**), then
  `if (recently_placed == 0) addRedAntHangout` (OSW:127-129).
- **DimensionID3 = the Village dimension**: `BaseDimensionID + 2` (OSM:1597), provider
  `WorldProviderOreSpawn3` ("Dimension-VillageMania", WorldProviderOreSpawn3.java:23-25,
  `field_76574_g = DimensionID3` at :35), single biome `BiomeGenUtopianPlains` named
  "Villages" (WorldProviderOreSpawn3.java:21) → port biome `orespawn:village_biome`
  (damsel precedent: inline `"biomes"` in the structure JSON, §7).
- Effective odds: **1/350 per Village-dimension chunk** (× config default-on) before
  scan success. The `recently_placed == 0` pre-gate, the damsel/red-ant ordering
  coupling, and the 50-chunk cooldown map onto structure-set separation (C7
  approximation, pattern §1 step 4).

---

## 2. Geometry — per-loop tables (all ranges inclusive)

Write order: pad + clearing (loop A), then 12 spawners (loop B), then the Robot Spider
(§4.1). `i` = X, `j` = Y, `k` = Z throughout (no index swaps). No RNG anywhere in the
geometry.

### 2.1 Loop A — 20×21×20 pad + air box (GD:6995-7008)

`i = 0..19` (outer, X), `j = −1..19` (middle, Y), `k = 0..19` (inner, Z); per cell:
`blk = air` (GD:6998); `j == −1` → stone (GD:6999-7001); `j == 0` → gravel
(GD:7002-7004); write **unconditionally** via
`OreSpawnMain.setBlockFast(world, cposx+i, cposy+j, cposz+k, blk, 0, 2)` (GD:7005).
20×21×20 = 8400 writes, no draws.

| Where | Cells (rel) | Block | Cite |
|---|---|---|---|
| foundation slab | `(0..19, −1, 0..19)` | stone | GD:6999-7001 |
| floor slab | `(0..19, 0, 0..19)` | gravel — **replaces the anchor grass block level** | GD:7002-7004 |
| clearing | `(0..19, +1..+19, 0..19)` | air — carves everything 19 high above the pad | GD:6998, 7005 |

The footprint is entirely on the +X/+Z side of the origin (origin = the pad's
minimum-corner column, NOT its center). Nothing is written below `y −1`.

### 2.2 Loop B — 12 spawners: 3-high columns at the 4 pad corners (GD:7009-7037)

`j = 1..3` (GD:7009); per `j`, four spawner placements in source order, each
`func_147465_d(..., field_150474_ac, 0, 2)` + `getSpawnerTileEntity` +
`func_98272_a("Spider Driver")`:

| # per level | Position (rel) | Mob name string | Cite |
|---|---|---|---|
| 1 | `(0, j, 0)` | `"Spider Driver"` | GD:7010-7016 |
| 2 | `(19, j, 19)` | `"Spider Driver"` | GD:7017-7023 |
| 3 | `(19, j, 0)` | `"Spider Driver"` | GD:7024-7030 |
| 4 | `(0, j, 19)` | `"Spider Driver"` | GD:7031-7036 |

= **12 spawners total**, stacked `y +1..+3` at each corner, overwriting loop-A air,
standing on the gravel floor. The fourth placement uses `continue`-on-null
(GD:7035) instead of `if != null` — identical semantics to the other three (skip the
name bind if the TE fetch fails); no behavioral difference to port.

### 2.3 Direct spawn — 1 × Robot Spider at pad center (GD:7038-7042)

Extracted exactly in §4.1. Position `(cposx+10, cposy+1, cposz+10)`, random yaw.

Net shape: a flat 20×20 gravel arena on a stone base, carved 19 blocks high into the
Village terrain, with four 3-high columns of Spider Driver spawners at the corners and
one live rideable Robot Spider parked in the middle. No chest, no walls, no roof, no
lighting.

---

## 3. Loot — NONE (full transcription: empty)

`makeSpiderHangout` places **no chest and fills no container** — no
`field_150486_ae`, no `getChestTileEntity`, no `func_76293_a`, no
`WeightedRandomChestContent` list reference anywhere in GD:6989-7043 (method read in
full). No loot-table JSON is created for this structure; there is no fill formula, so
the one-table-per-(list, fill formula) rule is vacuously satisfied. Do not add loot
(§12 S2).

---

## 4. Mob / entity table

| Use | Name | 1.7.10 registration | Port EntityType (cite) |
|---|---|---|---|
| Spawner ×12 (GD:7015 etc.) | `"Spider Driver"` | `registerGlobalEntityID(SpiderDriver.class, "Spider Driver", SpiderDriverID)` OSM:4489, `registerModEntity` OSM:4493 | `ModEntities.SPIDER_DRIVER` "spider_driver" (ModEntities.java:686-688) |
| **DIRECT spawn ×1** (GD:7038-7042) | `"Robot Spider"` | `registerGlobalEntityID(SpiderRobot.class, "Robot Spider", SpiderRobotID)` OSM:4481, `registerModEntity` OSM:4485 | `ModEntities.SPIDER_ROBOT` "spider_robot" (ModEntities.java:627-629) |

### 4.1 The direct Robot Spider spawn — EXACT extraction (GD:7038-7042)

```java
// GD:7038-7042
var8 = EntityList.func_75620_a((String)"Robot Spider", (World)world);
if (var8 != null) {
    var8.func_70012_b((double)(cposx + 10), (double)(cposy + 1), (double)(cposz + 10),
                      world.field_73012_v.nextFloat() * 360.0f, 0.0f);
    world.func_72838_d(var8);
}
```

- **Position**: `(cposx + 10, cposy + 1, cposz + 10)` — the pad's center-ish column
  (10 is the center-biased cell of 0..19), standing on the gravel floor. The doubles
  are casts of INTS: the entity stands at the block CORNER, not the +0.5 center.
  Transcribe exactly — do not "fix" to center (damsel S9 precedent).
- **Yaw**: `world.field_73012_v.nextFloat() * 360.0f`, pitch 0 (`func_70012_b` =
  setLocationAndAngles). **The builder's ONLY RNG draw** (§11).
- **NBT / init**: NONE — no onSpawnWithEgg, no tags, no owner; a factory-fresh entity
  via `func_72838_d` (spawnEntityInWorld — no spawn-rule checks run on this path).
- **Persistence — ORIGINAL**: the spawn call itself sets no flag, but the CLASS pins
  every instance at construction: `func_70088_a` (entityInit) calls
  `this.func_110163_bv()` (SR:508-513, the call at SR:510), AND `func_70692_ba()
  → false` (canDespawn, SR:86-88; SpiderRobot extends EntityLiving, SR:41-42). The
  original robot can never despawn. (Quirk: SR's empty NBT overrides SR:852-856
  dropped the flag on save, but the class-level canDespawn=false made that moot.)
- **Persistence — PORT**: port `SpiderRobot extends Mob` (port SpiderRobot.java:41)
  has **NO** `removeWhenFarAway` override and **NO** persistence call in init — the
  BUG-007 rework deliberately removed the empty NBT overrides and relies on inherited
  save behavior (port SpiderRobot.java:208-211 comment). Persistence must therefore
  come from the SPAWN CALL: use **`piece.spawnPersistent(ModEntities.SPIDER_ROBOT.get(),
  ox + 10, oy + 1, oz + 10, yaw)`** (LDP:553-573) — the helper's documented purpose is
  exactly this ("plus the callers' func_110163_bv()", LDP:554-558) and pattern trap #9
  mandates it. `setPersistenceRequired` now SAVES correctly post-BUG-007, so the
  ported robot survives save/reload without despawning — the original's net behavior.
  **This is the opposite of the Damsel Girlfriend ruling** (damsel §4.1 used
  `spawnEntity` because the Girlfriend CLASS carries `removeWhenFarAway → false`);
  here the port class carries nothing, so the flag is load-bearing (§12 S5).
- Draw `yaw = random.nextFloat() * 360.0f` UNCONDITIONALLY in the generator before
  the call (RNG contract; the helper is chunk-gated internally).

### 4.2 Spawner-gate interplay — the Robot Spider ENABLES the spawners

Orig `SpiderDriver extends EntitySpider` (SD:31-32); its `func_70601_bi`
(getCanSpawnHere, SD:177-184) returns `true` whenever a `SpiderRobot` is inside the
driver's bounding box inflated by (24, 12, 24), else falls through to EntitySpider's
darkness rules. The pad's central Robot Spider therefore lets the 12 spawners fire in
broad daylight on an unlit gravel arena — the robot is functional, not decoration.
Kill/ride the robot away and the spawners degrade to ordinary spider light rules.
Port mirror is exact: `SpiderDriver.checkSpawnRules` (port SpiderDriver.java:176-181)
does the same 24/12/24 `SpiderRobot` search. Do not "fix" the unlit spawner arena
(§12 S4). (Port natural-spawn gating of Spider Drivers is config-mapped in
`ModSpawnControl.java:64` — see §12 S6.)

---

## 5. Block palette

| 1.7.10 field | Modern block | Used for | Cite |
|---|---|---|---|
| `Blocks.field_150350_a` | `minecraft:air` | 19-high clearing | GD:6998 |
| `Blocks.field_150348_b` | `minecraft:stone` | foundation slab at y −1 | GD:7000 |
| `Blocks.field_150351_n` | `minecraft:gravel` | 20×20 floor at y 0 | GD:7003 |
| `Blocks.field_150474_ac` | `minecraft:spawner` | 12 spawners | GD:7012/7019/7026/7033 |
| (placement scan only) `Blocks.field_150349_c` | grass block | air-above-grass anchor test | OSW:1331 |

Gravel is a falling block in both versions; flag-2 writes schedule no updates (as in
1.7.10), and every gravel cell rests on the stone slab below it, so nothing can fall
even when later neighbor updates arrive. No connection-state or facing concerns — no
fences, panes, or chests in this structure.

---

## 6. Footprint extents + suggested DungeonType

Relative to `(cposx, cposy, cposz)` (= the grass block on the worldgen path):

| Axis | Min | Max | Extent | Determined by |
|---|---|---|---|---|
| X | `0` | `+19` | **20** | loop A `i = 0..19` (GD:6995) |
| Y | `−1` | `+19` | **21** | loop A `j = −1..19` (GD:6996): stone at −1, clearing to +19 |
| Z | `0` | `+19` | **20** | loop A `k = 0..19` (GD:6997) |

Suggested entry (asymmetric 6-int ctor `(xMin, xMax, down, up, zMin, zMax, mode)`,
LDP:338, +1 margin; `down = 2` covers the y −1 stone slab plus margin):

```java
SPIDER_HANGOUT(-1, 20, 2, 20, -1, 20, PlacementMode.VILLAGE_GRASS_SURFACE),
```

## 7. Placement — **existing mode fits: `VILLAGE_GRASS_SURFACE`** (no new mode needed)

`addSpiderHangout`'s anchoring (§1.1, quoted in full) is line-for-line identical to
`addDamselInDistress`'s (OSW:1301-1317), which `VILLAGE_GRASS_SURFACE` ports
(`villageGrassSurfaceOrigin`, LDS:286-319): 4 attempts of `chunk + nextInt(16)` jitter
(LDS:301-303 ← OSW:1327-1328), air-over-grass scan window Y 100→41 via
`getBaseHeight` with the dry-column grass approximation (LDS:304-313 ← OSW:1330-1331),
anchor = `firstFree − 1`, the GRASS block itself (LDS:314 ← OSW:1332), and
`quickSpaceCheck` (OSW:2625-2633) approximated by `footprintClearAbove` (LDS:315,
343-...). The differences (1/350 odds, config gate) live OUTSIDE the anchoring: odds →
§8, config → §12 S6. Worth appending to the mode's Javadoc when wiring:
`addSpiderHangout` (OSW:1319-1338) is another exact user (and `addRedAntHangout`
OSW:1340-1356 will be the third).

JSON pair (copy the `damsel_in_distress` pair — same dimension, same anchor — and
rename):

- `RES:worldgen/structure/spider_hangout.json` — `"type": "orespawn:legacy_dungeon"`,
  `"dungeon_type": "SPIDER_HANGOUT"`, `"biomes": "orespawn:village_biome"` (inline,
  single-biome dimension, damsel/dim_village precedent), `"step":
  "surface_structures"`, `"terrain_adaptation": "none"`, `"spawn_overrides": {}`.
- No `has_structure` tag file needed (inline single biome).

## 8. Structure-set conversion

Effective odds: **1/350 per Village-dimension chunk** (OSW:1320) before scan success;
the `recently_placed == 0` pre-gate (OSW:124), the 50-chunk cooldown set on success
(OSW:1333), and the damsel→spider→red-ant ordering coupling map onto structure-set
separation (C7 approximation, pattern §1 step 4).

C7 sqrt equivalence: spacing ≈ √350 ≈ 18.7 → **spacing 19, separation 9**.
Salt: **84368** (assigned to this task; verified free — grep of
`RES:worldgen/structure_set/*.json` shows OreSpawn salts 84301-84367 in use, highest
84367 rainbow, plus the vanilla-style 10387399 on dim_villages; 84368 unused).

`RES:worldgen/structure_set/spider_hangout.json`: random_spread, spacing 19,
separation 9, salt 84368.

## 9. DungeonSpawnerBlock outcome

- Original: `if (type == 48)` → `makeSpiderHangout(world, clickedX, clickedY,
  clickedZ)` — one call, no offset, block read in full (DSB:197-199; not one of the
  `clickedY + 1` outliers, which are types 43/44/45, DSB:182-190).
- Port: add `TYPE_SPIDER_HANGOUT = 48` (cite DSB:197-199) and
  `case TYPE_SPIDER_HANGOUT -> LegacyDungeonPiece.buildNow(server, pos,
  DungeonType.SPIDER_HANGOUT)` in `RandomDungeonSpawnerBlockEntity.buildForType`
  (constants RDS:44-103, dispatch RDS:155-307; wired today: 0-3, 7, 12-24, 26-30, 34,
  35, 37-39, 44, 46, 47 — **type 48 currently falls through to the generic-dungeon
  default**, RDS:306).
- The DSB path bypasses the dimension check, the grass scan, the −1 offset, AND the
  `SpiderDriverEnable` gate — a 20×20×21 volume is carved and paved at the clicked
  position in any dimension (floating pad or arena punched into a mountainside are
  both faithful), the robot spawns, the 12 spawners fire. `buildNow` (LDP:481) keeps
  live RNG for the single yaw draw.

## 10. Mid-build world READS — classified

1. **Tile-entity fetches** (12×, GD:7013/7020/7027/7034 via `getSpawnerTileEntity`
   GD:86-95) — SELF-reads of spawner blocks written the line before; absorbed by
   `piece.placeSpawner` (LDP:609). No deviation decision needed.
2. **No terrain reads at all** — loop A writes unconditionally (GD:7005), no
   `func_147439_a`/`func_147437_c` anywhere in GD:6989-7043. Nothing to model, no
   `terrainStateIfInChunk` use, no FLAG.
3. The placement-scan reads (OSW:1330-1331 + `quickSpaceCheck` OSW:2625-2633) live
   outside the builder and map into `VILLAGE_GRASS_SURFACE` (§7).

## 11. RNG stream

Draws inside the builder, in order:

1. **Robot Spider yaw** `world.field_73012_v.nextFloat() * 360.0f` (GD:7040) — the
   ONLY in-builder draw. Port: draw UNCONDITIONALLY on the piece `RandomSource` in the
   generator, pass to `piece.spawnPersistent` (§4.1; the helper is chunk-gated
   internally, the draw is not). Trivially identical in every per-chunk replay pass.

Everything else draws nothing: loop A is unconditional geometry, loop B has no rolls,
there is no chest. Dispatch-layer draws (1/350 gate OSW:1320, 4× two `nextInt(16)`
jitters OSW:1327-1328, DSB:52 roll) collapse into structure-set frequency /
`villageGrassSurfaceOrigin` / the RDS roll as usual.

Stream-shape notes (PARITY, slice report):
- Gate order quirk: the 1/350 draw happens BEFORE the config gate (OSW:1320-1325), so
  a config-off world still consumes one chunk-random draw here, shifting
  `addRedAntHangout`'s stream downstream (OSW:127-129). Collapses into set frequency —
  no port action, documented only.
- Mixed sources: chunk `random` for gate/jitter, `world.field_73012_v` for the yaw
  (GD:7040) and the DSB roll (DSB:52). All collapse per the standard treatment; the
  port's per-piece seed makes the parked robot's facing seed-stable (DSB `buildNow`
  keeps live RNG).

## 12. Surprises / MISSING-IN-PORT / COURT items

- **S1 (MISSING-IN-PORT)**: `makeSpiderHangout` has no counterpart anywhere in
  `src/main/` (grep `SpiderHangout|SPIDER_HANGOUT|spider_hangout`: zero matches). No
  DungeonType, no generator, no JSON pair; worldgen OSW:125 and DSB type 48 both fall
  through today (RDS:306).
- **S2 — no loot is the design**: a bare unlit gravel arena whose only "reward" is the
  rideable Robot Spider itself (players can mount it — port SpiderRobot interaction
  code — and it enables the hostile spawners, §4.2). Do not add a chest, lighting, or
  walls.
- **S3 — spawner columns**: 3-high stacks (`y +1..+3`) at all four pad corners, 12
  spawners total, all `"Spider Driver"` (GD:7009-7037). Keep exact positions and the
  per-level placement order (0,0 → 19,19 → 19,0 → 0,19); the fourth's
  `continue`-on-null (GD:7035) is cosmetic decompiler output, same semantics.
- **S4 — the robot is load-bearing for the spawners** (§4.2): SpiderDriver's spawn
  rule passes near a SpiderRobot (SD:177-184; port SpiderDriver.java:176-181), which
  is why an unlit daylight arena spawns drivers at all. Do not relight, and keep the
  robot spawn even though it looks decorative.
- **S5 — persistence mapping inverts the damsel ruling** (§4.1): orig SpiderRobot
  pinned itself at entityInit (`func_110163_bv`, SR:510) and overrode canDespawn→false
  (SR:86-88); the PORT class carries neither (BUG-007 rework, port
  SpiderRobot.java:208-211), so the generator MUST use `piece.spawnPersistent`
  (LDP:553-573, pattern trap #9) — `spawnEntity` here would produce a robot that
  despawns. The flag now saves correctly post-BUG-007, matching the original's net
  never-despawn behavior.
- **S6 — COURT: `SpiderDriverEnable` worldgen gate** (OSW:1323-1325, default enabled
  OSM:6368). The port defines `SPIDER_DRIVER_ENABLE` (OreSpawnConfig.java:15, 148) but
  consumes it ONLY for natural-spawn gating (ModSpawnControl.java:64) — no structure
  code reads it. Precedents conflict: (a) rotator_station_spec.md ruled "Config gates
  dropped: RotatorEnable not ported — port-wide decision" for the identical `*Enable`
  shape in the CrystalStructures regime; (b) the LDS regime DOES read config in
  placement (`OreSpawnConfig.LESS_LAG`, LDS:374 inside `islandsGrassOrigin`), so a
  faithful per-type gate (`if SPIDER_HANGOUT && !SPIDER_DRIVER_ENABLE → return empty`
  in `findGenerationPoint`, LDS:67-98) is mechanically trivial. **Recommended default:
  follow the rotator ruling (drop the worldgen gate, PARITY-note it; config still
  governs natural Spider Driver spawns) — but this is player-visible when the config
  is off, so it goes to the court rather than being decided here.** Note the
  structure-set is static either way; only a code-side gate can honor the toggle.
  Whatever is ruled must also note the DSB path never honored the gate in 1.7.10
  (DSB:197-199 has no config check) — a DSB-side gate would INVENT behavior.
- **S7 — clearance probe smaller than the footprint**: `quickSpaceCheck` probes
  12×12 at anchor+4, offsets −2..+9 (OSW:2625-2633), but the builder clears 20×20×19
  starting AT the origin — the far +X/+Z pad edges (rel 10..19) are unprobed, so
  original hangouts routinely cut a sheer 19-high face into hillsides on those edges.
  The mode's `footprintClearAbove` (LDS:315, 343) samples the real footprint corners —
  slightly stricter than the original at the far edges; accepted mode-internal
  approximation (same delta already shipped for damsel). Do not add terrain adaptation.
- **S8 — anchor is the grass block; the pad replaces it**: the builder receives
  `posY − 1` (OSW:1332) and loop A writes gravel AT `j = 0` (GD:7002-7004) — the found
  grass block becomes the pad's corner gravel cell, stone fills `y −1`, and the arena
  sits flush with grade at the anchor column. Origin is the MINIMUM corner, not the
  center (contrast most GD builders): the structure extends only into +X/+Z
  (§6) — get the bounding box asymmetry right or /locate and separation will be
  center-biased.
- **S9 — corner-coordinate spawn**: the robot stands at integer corner coords
  `(+10, +1, +10)` (GD:7040), not block center. Transcribe exactly (damsel S9
  precedent).
- **S10 — twin structure**: `makeRedAntHangout` (GD:7045-...) + `addRedAntHangout`
  (OSW:1340-1356, 1/250 at OSW:1341 — and, VERIFIED this pass, **no config gate at
  all**: the method read in full contains no `*Enable` check, unlike
  `addSpiderHangout`'s OSW:1323-1325; `RedAntEnable` exists (OSM:424, default 1
  OSM:6378) but its consumers are the ant blocks / addAnts paths (AntBlock.java:75,
  CrystalAntBlock.java:75, OSW:1455/1473/1489), never this method) + DSB type 49
  (DSB:200-202) is the same family; when specced, expect the same mode and the same
  no-loot shape, but NO S6-style COURT item — there is no worldgen config gate on the
  red-ant path to rule on. Nothing is shared between the two (no loot lists exist to
  twin).
- **S11 — dead local**: `boolean which` (OSW:1329) is never read; ignore (same as
  damsel S3).
