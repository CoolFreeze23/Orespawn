# Red Ant Hangout — Extraction Spec (1.7.10 → NeoForge 1.21.1)

Source of truth: `reference_1_7_10_source/sources/danger/orespawn/GenericDungeon.java` —
`makeRedAntHangout` (GD:7045-7069 — the FINAL method in the file; class brace GD:7070,
EOF GD:7071; previous method `makeSpiderHangout` GD:6989-7043). Method read IN FULL to
EOF. All coordinates are relative to the build origin `(cposx, cposy, cposz)` = the
three int args. No method-local constants, no helpers beyond
`OreSpawnMain.setBlockFast` — the whole builder is one triple loop plus one direct
entity spawn.

Citation convention: `GD:NN` = GenericDungeon.java, `OSW:NN` = OreSpawnWorld.java,
`OSM:NN` = OreSpawnMain.java, `DSB:NN` = DungeonSpawnerBlock.java, `AR:NN` = orig
`AntRobot.java`, `LDP:NN` = `src/main/java/danger/orespawn/world/structure/
LegacyDungeonPiece.java`, `LDS:NN` = `.../LegacyDungeonStructure.java`, `RDS:NN` =
`src/main/java/danger/orespawn/block/entity/RandomDungeonSpawnerBlockEntity.java`.

Shared plumbing:
- All geometry writes are `OreSpawnMain.setBlockFast(..., blk, meta 0, flags 2)`
  (GD:7060) → port `piece.place(x, y, z, state)` (LDP:513-518; pattern §1 step 3
  table).
- **No spawners, no chests, no loot** — the method places none (GD:7045-7069 contains
  no `field_150474_ac`, no `field_150486_ae`, no `WeightedRandomChestContent`). §3 is
  a no-op by design, not an omission.
- **One DIRECT entity spawn** — a "Robot Red Ant" at pad center (GD:7064-7068),
  extracted exactly in §4.1.
- **Zero mid-build world READS** — no `func_147439_a`/`func_147437_c` and no
  tile-entity fetches anywhere in GD:7045-7069 (§10).
- **Zero in-geometry RNG** — the only draw in the whole builder is the spawn yaw
  (GD:7066) (§11).

---

## 1. Entry points — EVERY call site

Grep of the whole original tree for `makeRedAntHangout`: exactly two call sites
(OSW:1350 and DSB:201; GD:7045 is the definition; the old pre-audit copies under
`src/danger/orespawn/` mirror the same three files and are not shipped code —
`src/main/java/` is the shipped tree).

| Caller | Signature used | Coords passed | Notes |
|---|---|---|---|
| `OreSpawnWorld.addRedAntHangout` (OSW:1350) | `OreSpawnMain.MyDungeon.makeRedAntHangout(world, posX, posY - 1, posZ)` | scan hit **minus 1** — the origin is the GRASS BLOCK itself, not the air above | worldgen path, **Village dimension (DimensionID3) only** (§1.2) |
| `DungeonSpawnerBlock` type **49** (DSB:200-202) | `...makeRedAntHangout(world, clickedX, clickedY, clickedZ)` | player-placed block pos, no offset | roll `type = world.field_73012_v.nextInt(50)` (DSB:52), fires 400 ticks after placement (DSB:39), self + block above deleted first (DSB:50-51). The `if (type == 49)` block read IN FULL: **single builder call, nothing else** (DSB:200-202) — the LAST outcome of the 50-entry table, not a two-builder index. |

### 1.1 `addRedAntHangout` — FULL method + return contract (OSW:1340-1356)

```java
// OSW:1340-1356
public boolean addRedAntHangout(World world, Random random, int chunkX, int chunkZ) {
    if (random.nextInt(250) != 0) {
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
            OreSpawnMain.MyDungeon.makeRedAntHangout(world, posX, posY - 1, posZ);
            recently_placed = 50;
            return true;
        }
    }
    return false;
}
```

1. Gate: `random.nextInt(250) != 0 → return false` (OSW:1341-1343) — 1/250, chunk
   `random`. Same gate value as `addDamselInDistress` (OSW:1302); contrast
   `addSpiderHangout`'s 1/350 + `SpiderDriverEnable` config gate (OSW:1320-1325 —
   RedAntHangout has NO config gate).
2. Up to 4 attempts (OSW:1344): `posX/posZ = chunk + random.nextInt(16)`
   (OSW:1345-1346). Dead local `which` (OSW:1347) — ignore.
3. Column scan `posY = 100` down to `41` inclusive (OSW:1348): require air at `posY`
   AND **grass block** (`field_150349_c`) at `posY − 1` AND
   `quickSpaceCheck(world, posX, posY - 1, posZ)` (OSW:1349).
4. Hit → `makeRedAntHangout(world, posX, posY - 1, posZ)` — **origin = the grass
   block** — then `recently_placed = 50`, `return true` (OSW:1350-1352).
5. All attempts miss → `return false` (OSW:1355).

**FULL return contract: `true` ONLY on an actual placement** (which also sets the
50-chunk global cooldown, OSW:1351). Gate fail and scan fail both return `false`. No
WGEN-062-style early-true quirk. The caller ignores the boolean (§1.2) — chaining in
this dimension is done with `recently_placed` gates OUTSIDE the methods.

The body is **line-for-line identical to `addDamselInDistress`** (OSW:1301-1317, the
`VILLAGE_GRASS_SURFACE` reference) — same gate value, same 4 attempts, same
Y 100→41 air-over-grass scan, same `quickSpaceCheck`, same `posY − 1` anchor; only
the builder called differs. `quickSpaceCheck` (OSW:2625-2633): 12×12 all-air plane at
`(posX−2..posX+9, anchor+4, posZ−2..posZ+9)` — placement scan, not a mid-build read →
maps into the PlacementMode (§7).

### 1.2 Worldgen dispatch chain (complete)

- `OreSpawnWorld.generate`, `world.field_73011_w.field_76574_g ==
  OreSpawnMain.DimensionID3` branch (OSW:114-131): mosquitos (config-gated,
  OSW:115-117), `addAnts(…, 4)` — one call, count arg 4 (OSW:118; contrast
  Utopia's two `(…, 2)` calls OSW:106-107) — `addAppleTrees` (OSW:119),
  `addGenericDungeon` (unconditional, OSW:120), then three
  `recently_placed == 0`-gated calls in order: `addDamselInDistress` (OSW:121-123),
  `addSpiderHangout` (OSW:124-126), **`addRedAntHangout` (OSW:127-129) — LAST in the
  chain**, so a same-chunk Damsel or SpiderHangout placement (each sets
  `recently_placed = 50`) suppresses it.
- **DimensionID3 = the Village dimension**: `BaseDimensionID + 2` (OSM:1597),
  provider `WorldProviderOreSpawn3` (OSM:5383-5384) → port biome
  `orespawn:village_biome` (single-biome dimension; inline-biome precedent
  `RES:worldgen/structure/damsel_in_distress.json`).
- Effective odds: **1/250 per Village-dimension chunk** before scan success. The
  `recently_placed == 0` pre-gate, the cross-structure ordering coupling, and the
  cooldown it sets all map onto structure-set separation (C7 approximation, pattern
  §1 step 4).

---

## 2. Geometry — one triple loop (all ranges inclusive)

`i = 0..15` (outer, X), `j = −1..15` (middle, Y — note the −1 start, GD:7048),
`k = 0..15` (inner, Z); write at `(cposx + i, cposy + j, cposz + k)`,
**unconditionally, air included** (GD:7060) — the full 16×17×16 volume is cleared or
replaced. Per cell, in source order:

| # | Rule (source order) | Result | Cite |
|---|---|---|---|
| 1 | default | air | GD:7050 |
| 2 | `j == −1` | stone — full 16×16 base slab one below origin | GD:7051-7053 |
| 3 | `j == 0` | gravel — full 16×16 floor at origin level | GD:7054-7055 |
| 4 | `j == 0 && !(i >= 3 && i <= 12 \|\| k >= 3 && k <= 12)` | `OreSpawnMain.MyRedAntBlock` (Red Ant Nest) — De Morgan: `(i ≤ 2 or i ≥ 13) AND (k ≤ 2 or k ≥ 13)` = the four 3×3 CORNER pads of the floor, 36 cells; the other 220 floor cells stay gravel | GD:7056-7058 |
| 5 | write `blk` unconditionally | `j = 1..15` clears a 16×16×15 air volume above the pad (terrain, trees, everything) | GD:7060 |

Expanded floor plan (`j = 0`): nest blocks exactly at `i ∈ {0,1,2,13,14,15} AND
k ∈ {0,1,2,13,14,15}`; gravel everywhere else in `0..15 × 0..15`.

Net shape: a flat 16×16 gravel pad flush with the Village-dimension grass (the pad
REPLACES the surface — grass level becomes gravel, one below becomes stone), with a
3×3 Red Ant Nest patch in each corner and 15 blocks of forced open air above; one
Robot Red Ant standing at pad center. No walls, no roof, no loot, no spawners.

Family note: `makeSpiderHangout` (GD:6989-7043) is the same pattern at 20×20 with a
plain gravel floor (no corner blocks, GD:7002-7004), twelve "Spider Driver" spawner
blocks stacked 3-high in each corner (GD:7009-7037), and a "Robot Spider" at
`(+10, +1, +10)` (GD:7038-7042). RedAntHangout swaps the spawner corners for
renewable Red Ant Nest blocks — port them independently; nothing is shared beyond
the shape.

---

## 3. Loot — NONE

`makeRedAntHangout` places **no chest and consumes no contents list**
(GD:7045-7069 read in full — no `field_150486_ae`, no `func_76293_a`). No
`RES:loot_table/chests/red_ant_hangout.json` is to be created; the mob content is
the direct Robot Red Ant (§4.1) plus the renewable corner nest blocks (§5).

---

## 4. Mob / entity table

| Use | Name | 1.7.10 registration | Port EntityType (cite) |
|---|---|---|---|
| **DIRECT spawn ×1** (GD:7064-7068) | `"Robot Red Ant"` | `registerGlobalEntityID(AntRobot.class, "Robot Red Ant", AntRobotID)` OSM:4505, `registerModEntity` OSM:4509 | `ModEntities.ANT_ROBOT` "ant_robot" (ModEntities.java:578-581, MobCategory.MISC, sized 2.75×1.25 ← orig AR:52) |

### 4.1 The direct Robot Red Ant spawn — EXACT extraction (GD:7064-7068)

```java
// GD:7046, 7064-7068
Entity var8 = null;
var8 = EntityList.func_75620_a((String)"Robot Red Ant", (World)world);
if (var8 != null) {
    var8.func_70012_b((double)(cposx + 8), (double)(cposy + 1), (double)(cposz + 8),
                      world.field_73012_v.nextFloat() * 360.0f, 0.0f);
    world.func_72838_d(var8);
}
```

- **Position**: `(cposx + 8, cposy + 1, cposz + 8)` — standing on the gravel floor
  at the pad's center column. The doubles are casts of INTS: the entity stands at
  the block CORNER, not the +0.5 center — transcribe exactly, do not "fix" to
  center (Damsel S9 precedent).
- **Yaw**: `world.field_73012_v.nextFloat() * 360.0f`, pitch 0 (`func_70012_b` =
  setLocationAndAngles). ONE RNG draw — the builder's only one (§11).
- **NBT / init**: NONE — no onSpawnWithEgg, no tags, no `setOwned` (the robot
  spawns UNOWNED, `owned = 0` AR:48 — an unowned AntRobot is hostile-neutral: it
  stomps and attacks nearby targets on its own AI, AR:105-146, but it can NOT be
  ridden: interact is a no-op while `owned == 0` (AR:908-910); mounting
  (AR:929-935, needs no rider + within 4 blocks) and iron-ingot healing
  (AR:911-928) are OWNED-only. Claiming a wild one takes the Wrench — left-click
  an unowned robot at ≤ half health → `setOwned()` + convert to an AntRobotKit
  item carrying its missing HP (orig ItemWrench.java:50-60, health gate :53-54);
  placing the kit respawns it OWNED (orig ItemSpiderRobotKit.java:52-55)).
  Placed via `func_72838_d` (spawnEntityInWorld — no spawn-rule checks run on
  this path).
- **Persistence**: the spawn site sets no flag, but the ENTITY CLASS self-persists
  twice over — `func_70692_ba() → false` (canDespawn, AR:80-82) AND entityInit
  calls `this.func_110163_bv()` (= setPersistenceRequired, AR:532-538, the call at
  AR:534) — so every 1.7.10 AntRobot carried `PersistenceRequired` in its saved
  NBT from birth and could never despawn.
- **Port mapping**: `piece.spawnPersistent(ModEntities.ANT_ROBOT.get(), ox + 8,
  oy + 1, oz + 8, yaw)` (LDP:553-573 — the gated, caller-draws-yaw helper whose
  Javadoc contract is exactly this shape: createEntityByName + setLocationAndAngles
  + spawnEntityInWorld + `func_110163_bv`). `spawnPersistent` writes the SAME
  `PersistenceRequired` NBT the original entity always had — unlike the Damsel's
  Girlfriend (class override only, no NBT flag → `spawnEntity`), here the flagged
  helper IS the faithful choice. Draw `yaw = random.nextFloat() * 360.0f`
  UNCONDITIONALLY in the generator before the call (RNG contract; the original's
  `var8 != null` guard cannot fail in the port — the EntityType is registered).
- **⚠ Port entity parity gap (S2)**: the port `AntRobot` (port
  entity/AntRobot.java:36, extends Mob, MISC category) carries NEITHER original
  persistence mechanism — no `removeWhenFarAway` override and no
  `setPersistenceRequired()` call anywhere in the file (grep verified; the ctor
  comment at port AntRobot.java:62-63 cites orig AR:532-535 for `initLegData` but
  dropped that block's sibling `func_110163_bv()` call). `spawnPersistent` makes
  THIS structure's robot despawn-proof regardless, but see §12 S2 for the other
  spawn paths.

---

## 5. Block palette

| 1.7.10 field | Modern block | Used for | Cite |
|---|---|---|---|
| `Blocks.field_150350_a` | `minecraft:air` | 16×16×15 clearing above the pad | GD:7050 |
| `Blocks.field_150348_b` | `minecraft:stone` | 16×16 base slab at `j = −1` | GD:7052 |
| `Blocks.field_150351_n` | `minecraft:gravel` | floor at `j = 0` (220 cells) | GD:7055 |
| `OreSpawnMain.MyRedAntBlock` | port `ModBlocks.RED_ANT_BLOCK` "red_ant_block" | four 3×3 corner pads (36 cells) | GD:7057 |
| (placement scan only) `Blocks.field_150349_c` | grass block | air-above-grass anchor test | OSW:1349 |

- `MyRedAntBlock` identity: `new AntBlock(BaseBlockID + 116).func_149663_c
  ("RedAntBlock")` OSM:6272, registered "OreSpawn_RedAntBlock" OSM:2189, display
  name "Red Ant Nest" OSM:3087. The block spawns "Red Ant" entities (config
  `RedAntEnable`, orig AntBlock.java:74-77). Port mapping precedent: the anthill
  feature already maps it — `ModBlocks.RED_ANT_BLOCK` (ModBlocks.java:562-564,
  `CrystalAntBlock` with `AntType.RED_ANT` → `ModEntities.ENTITY_RED_ANT`,
  CrystalAntBlock.java:62; established in AnthillFeature.java:68). Place
  `defaultBlockState()`.
- Gravel is a modern `FallingBlock`, but every `j = 0` gravel cell sits on the
  `j = −1` stone slab written by the same loop — always supported, on the worldgen
  AND the DSB path (a floating DSB build still writes its own stone base). No
  falling hazard, no deviation needed.
- All flag-2 writes (`piece.place` preserves the no-neighbor-update semantics).

---

## 6. Footprint extents + suggested DungeonType

Relative to `(cposx, cposy, cposz)` (= the grass block on the worldgen path). The
loops are ONE-SIDED — the origin is the pad's minimum (−X/−Z) corner, not its
center (contrast Damsel's ±4):

| Axis | Min | Max | Extent | Determined by |
|---|---|---|---|---|
| X | `0` | `+15` | **16** | `i = 0..15` (GD:7047) |
| Y | `−1` | `+15` | **17** | `j = −1..15` (GD:7048) — stone slab digs ONE below origin |
| Z | `0` | `+15` | **16** | `k = 0..15` (GD:7049) |

Suggested entry (asymmetric 6-int ctor `(xMin, xMax, down, up, zMin, zMax, mode)`,
+1 margin; `down = 2` covers the `j = −1` slab, GIRLFRIEND_ISLAND convention):

```java
RED_ANT_HANGOUT(-1, 16, 2, 16, -1, 16, PlacementMode.VILLAGE_GRASS_SURFACE),
```

## 7. Placement — **existing mode fits: `VILLAGE_GRASS_SURFACE`** (no new mode needed)

`addRedAntHangout`'s anchoring (§1.1, quoted in full) is **line-for-line identical**
to `addDamselInDistress`'s (OSW:1301-1317), which `VILLAGE_GRASS_SURFACE` ports
(`villageGrassSurfaceOrigin`, LDS:299-319; dispatch case LDS:90): 4 attempts of
`chunk + nextInt(16)` jitter (LDS:301-303 ← OSW:1344-1346), hard Y window 41..100
via `getBaseHeight` (LDS:304-308 ← OSW:1348), dry-column grass approximation
(LDS:309-313 ← OSW:1349's grass test), anchor at `firstFree − 1` = the grass block
(LDS:314 ← OSW:1350), and the `quickSpaceCheck` 12×12 air plane at anchor+4
(OSW:2625-2633) approximated by `footprintClearAbove` (LDS:315, LDS:343-359 — the
box-corner sampling uses this type's own extents, so the one-sided §6 box probes
0..15 correctly). The 1/250 gate and `recently_placed` coupling collapse into the
structure set (§8). Worth appending to the mode's Javadoc (LDS:286-298) when
wiring: `addRedAntHangout` (OSW:1340-1356) is another exact user (as is
`addSpiderHangout`, OSW:1319-1338, modulo its 1/350 + config gate).

JSON pair (copy the `damsel_in_distress` pair — same dimension, same anchor — and
rename):

- `RES:worldgen/structure/red_ant_hangout.json` — `"type":
  "orespawn:legacy_dungeon"`, `"dungeon_type": "RED_ANT_HANGOUT"`,
  `"biomes": "orespawn:village_biome"` (inline single biome, damsel precedent —
  §1.2), `"step": "surface_structures"`, `"terrain_adaptation": "none"`,
  `"spawn_overrides": {}`.
- No `has_structure` tag file needed (inline single biome).

## 8. Structure-set conversion

Effective odds: **1/250 per Village-dimension chunk** (OSW:1341) before scan
success; the `recently_placed == 0` pre-gate (OSW:127), the Damsel/SpiderHangout
ordering coupling (§1.2), and the cooldown set on success (OSW:1351) map onto
structure-set separation (C7 approximation, pattern §1 step 4).

C7 sqrt equivalence: spacing ≈ √250 ≈ 15.8 → **spacing 16, separation 8** (same
arithmetic and result as `damsel_in_distress.json`).
Salt: **84369** (assigned to this task; verified free — grep of
`RES:worldgen/structure_set/*.json` shows OreSpawn salts 84301-84367 in use, highest
84367 rainbow, plus the vanilla-style 10387399 on dim_villages; 84368/84369 unused
in JSON — 84368 is ASSIGNED by the parallel spider_hangout spec
(`spider_hangout_spec.md` §8: salt 84368, separation 9), do not take it).

`RES:worldgen/structure_set/red_ant_hangout.json`: random_spread, spacing 16,
separation 8, salt 84369.

## 9. DungeonSpawnerBlock outcome

- Original: `if (type == 49)` → `makeRedAntHangout(world, clickedX, clickedY,
  clickedZ)` — one call, no offset, block read in full (DSB:200-202; not a
  Y-offset outlier — those are only 43/44/45, DSB:182-190).
- Port: add `TYPE_RED_ANT_HANGOUT = 49` (cite DSB:200-202) and
  `case TYPE_RED_ANT_HANGOUT -> LegacyDungeonPiece.buildNow(server, pos,
  DungeonType.RED_ANT_HANGOUT)` in `RandomDungeonSpawnerBlockEntity.buildForType`
  (constants RDS:44-103, switch RDS:155-308; wired today: 0/1/2/3/7/12-24/26-30/
  34/35/37/38/39/44/46/47 — 31 outcomes; **type 49 currently falls through to the
  generic-dungeon default**, RDS:306).
- The DSB path bypasses the grass scan, dimension check, and −1 offset — the
  gravel pad sits with its floor AT the clicked position (stone one below), its own
  base slab keeping the gravel supported even midair, in any dimension. Faithful
  behavior; `buildNow` (LDP:481-486) keeps live RNG for the yaw.

## 10. Mid-build world READS — classified

1. **None.** GD:7045-7069 contains no `func_147439_a`/`func_147437_c` and — unlike
   every sibling in the family — no tile-entity fetches either (no spawners, no
   chests). The builder writes unconditionally everywhere.
2. `EntityList.func_75620_a` (GD:7064) constructs an entity; it is not a world
   read. Absorbed by `spawnPersistent`.
3. The placement-scan reads (OSW:1348-1349, quickSpaceCheck OSW:2625-2633) live
   outside the builder and map into `VILLAGE_GRASS_SURFACE` (§7).

No in-memory model, no `terrainStateIfInChunk`, no flags. No NEEDS_DESIGN_RULING
condition arises anywhere in this structure.

## 11. RNG stream

Draws inside the builder, in order:

1. **Geometry: ZERO draws.** Every cell's block (air/stone/gravel/nest) is a pure
   function of loop indices (GD:7050-7058) — the 16×17×16 loop consumes nothing.
2. **Yaw**: `world.field_73012_v.nextFloat() * 360.0f` (GD:7066) — the method's ONLY
   draw. Port: draw UNCONDITIONALLY on the piece RandomSource in the generator, then
   `piece.spawnPersistent(..., yaw)` (helper gates the write, never the draw —
   pattern §1 step 3 rule 1). The original's draw sat inside the `var8 != null`
   guard; in the port the type always constructs, so the unconditional draw is
   stream-identical (Damsel §4.1 precedent).

With one draw total, the stitching contract is trivially satisfied; every per-chunk
replay consumes the identical one-element sequence. Dispatch-layer rolls (OSW:1341
gate, OSW:1345-1346 jitter — chunk `random`; DSB:52 — world rand) collapse into
structure-set frequency / `villageGrassSurfaceOrigin` / the RDS roll (RDS:136) as
usual. PARITY note for the slice report: the original yaw came from shared live
`world.rand`; the port's per-piece seed makes the robot's facing seed-stable
(worldgen path only — `buildNow` keeps live RNG).

## 12. Surprises / MISSING-IN-PORT

- **S1 (MISSING-IN-PORT)**: `makeRedAntHangout` has no counterpart in `src/main/`
  (grep `RedAntHangout|RED_ANT_HANGOUT`: only the RDS Javadoc's mention of the
  50-outcome table, RDS:27). No DungeonType, no generator, no JSON pair; worldgen
  OSW:127-129 and DSB type 49 both fall through today (RDS:306).
- **S2 (COURT — port AntRobot persistence gap, spillover beyond this structure)**:
  the original AntRobot never despawns — `func_70692_ba() → false` (AR:80-82) plus
  entityInit's `func_110163_bv()` (AR:532-538) — but the port
  `entity/AntRobot.java` (extends Mob, MISC category ModEntities.java:578-581) has
  NO `removeWhenFarAway` override and NO `setPersistenceRequired()` call, so a port
  AntRobot despawns via the default `Mob.checkDespawn` path once a player is far
  enough away. This structure is safe because §4.1 mandates `spawnPersistent`
  (which stamps the same `PersistenceRequired` NBT the original always carried),
  but the OTHER spawn paths — `ant_robot_kit` (ModItems.java:633-636),
  `ant_robot_spawn_egg` (ModItems.java:1118-1119), ItemWrench kit recovery — spawn
  unflagged robots that the original would have kept forever. Restoring the class
  overrides (orig AR:80-82, AR:534) is a one-file entity fix outside this
  structure's scope; the port `SpiderRobot.java` has the same gap (relevant to the
  parallel spider_hangout spec, whose "Robot Spider" GD:7038-7042 is the same
  spawn shape).
- **S3 — the corner condition is a De Morgan trap**: GD:7056 reads
  `if (!(i >= 3 && i <= 12 || k >= 3 && k <= 12))` — nest blocks ONLY where
  `(i ≤ 2 or i ≥ 13) AND (k ≤ 2 or k ≥ 13)`: four 3×3 corner pads, 36 cells. A
  careless reading (e.g. treating it as a border ring) produces a visibly wrong
  floor. The `&&` binds tighter than `||`; transcribe the negation exactly.
- **S4 — anchor is the grass block itself** (OSW:1350, posY−1): the pad REPLACES
  the surface — gravel at former grass level, stone one below (`j = −1..0`,
  GD:7051-7055) — so the hangout sits flush with the terrain. This plus
  `quickSpaceCheck` is precisely the `VILLAGE_GRASS_SURFACE` shape; nothing new to
  port (§7).
- **S5 — origin is the pad's minimum corner, not its center** (loops 0..15,
  GD:7047-7049): the structure extends only toward +X/+Z from the anchor, and the
  robot's `+8,+8` spawn is the pad center. Keep the one-sided §6 box — do not
  recenter the loops or symmetrize the extents.
- **S6 — 15 layers of unconditional air** (GD:7050/7060): `j = 1..15` force-clears
  a 16×16×15 volume — trees, hills, buildings, everything above the pad vanishes.
  Faithful; do not add terrain-sparing conditions.
- **S7 — no spawners, no chest, no loot**: the family outlier. SpiderHangout packs
  twelve spawner blocks (GD:7009-7037); RedAntHangout's renewable mob content is
  the 36 Red Ant Nest corner blocks (spawn "Red Ant" per orig AntBlock.java:74-77,
  config-gated by `RedAntEnable` → port `OreSpawnConfig.RED_ANT_ENABLE`,
  CrystalAntBlock mapping §5) plus the one Robot Red Ant. Do not "complete" the
  structure with spawners or a chest.
- **S8 — the robot spawns UNOWNED** (`owned = 0`, AR:48; no `setOwned()` call at
  the spawn site): it aggros nearby targets on its own (AR:105-146) and is NOT
  rideable as found — interact no-ops while unowned (AR:908-910); riding is
  owned-only (AR:929-935). The structure is effectively a free CLAIMABLE robot
  guarded by ant nests: batter it below half health, wrench it into an
  AntRobotKit (ItemWrench.java:50-60), place the kit to respawn it owned and
  rideable (ItemSpiderRobotKit.java:52-55). Spawn factory-fresh; do not pre-own.
- **S9 — direct-spawn quirks to preserve exactly** (§4.1): integer corner
  coordinates (no +0.5), caller-drawn random yaw, pitch 0, no NBT.
  `spawnPersistent`'s extra `playAmbientSound()` (LDP:572) is inaudible during
  worldgen and an accepted helper-level addition on the DSB path (helper Javadoc,
  LDP:560-563).
- **S10 — dead local**: `boolean which` in the add method (OSW:1347) is never
  read. Ignore (same dead local as Damsel/SpiderHangout).
