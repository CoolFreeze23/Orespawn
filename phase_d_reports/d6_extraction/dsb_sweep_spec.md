# DSB Sweep Spec — batch 4: the 13 no-new-generator Dungeon Spawner outcomes

Mapping spec for wiring the 13 remaining `DungeonSpawnerBlock` outcomes that
need NO new generator into
`port:block/entity/RandomDungeonSpawnerBlockEntity.buildForType`
(pattern doc §1 step 6). Every original method cited below was read IN FULL
from `orig:DungeonSpawnerBlock.java` (dispatch DSB:46-203; file is 228 lines) and
`orig:GenericDungeon.java`; every port callee was read in full from
`port:world/structure/LegacyDungeonPiece.java`, `port:world/CrystalStructures.java`,
and `port:world/feature/{Beehive,SmallBeehive,MantisNest,CrystalBattleTower}Feature.java`.

Paths: `orig:` = `reference_1_7_10_source/sources/danger/orespawn/`,
`port:` = `src/main/java/danger/orespawn/`. DSB = `orig:DungeonSpawnerBlock.java`,
GD = `orig:GenericDungeon.java`, LDP = `port:world/structure/LegacyDungeonPiece.java`,
CS = `port:world/CrystalStructures.java`, RDS =
`port:block/entity/RandomDungeonSpawnerBlockEntity.java`.

---

## 0. Baseline re-verification (independent, against RDS as it stands)

Read directly from `RDS.buildForType` (RDS:155-308). Wired cases:

| RDS case | orig type | RDS case | orig type |
|---|---|---|---|
| TYPE_FAIRY_TREE | 0 | TYPE_IGLOO | 20 |
| TYPE_FAIRY_CASTLE_TREE | 1 | TYPE_GENERIC_DUNGEON | 21 |
| TYPE_ENORMOUS_CASTLE_KING | 2 | TYPE_RUBY_DUNGEON | 22 |
| TYPE_ROTATOR_STATION | 3 | TYPE_BASILISK_MAZE | 23 |
| TYPE_KYUUBI_DUNGEON | 7 | TYPE_HOSPITAL | 24 |
| TYPE_PLAY_POOL | 12 | TYPE_BOUNCY_CASTLE | 26 |
| TYPE_WATER_DRAGON_LAIR | 13 | TYPE_ENDER_CASTLE | 27 |
| TYPE_CLOUD_SHARK_DUNGEON | 14 | TYPE_DAMSEL_IN_DISTRESS | 28 |
| TYPE_LEAF_MONSTER_DUNGEON | 15 | TYPE_INCA_PYRAMID | 29 |
| TYPE_MINI_DUNGEON | 16 | TYPE_ROBOT_LAB | 30 |
| TYPE_GOLD_FISH_BOWL | 17 | TYPE_CEPHADROME_ALTAR | 34 |
| TYPE_ENDER_REAPER_GRAVEYARD | 18 | TYPE_GIRLFRIEND_ISLAND | 35 |
| TYPE_SPIT_BUG_LAIR | 19 | TYPE_MONSTER_ISLAND | 37 |
| | | TYPE_NIGHTMARE_ROOKERY | 38 |
| | | TYPE_STINKY_HOUSE | 39 |
| | | TYPE_PUMPKIN | 44 |
| | | TYPE_RAINBOW | 46 |
| | | TYPE_ENORMOUS_CASTLE_QUEEN | 47 |

Wired set = {0,1,2,3,7,12,13,14,15,16,17,18,19,20,21,22,23,24,26,27,28,29,30,
34,35,37,38,39,44,46,47} — **31 outcomes, matching the claimed baseline
exactly** (0-3, 7, 12-24, 26-30, 34, 35, 37-39, 44, 46, 47). Unwired = 19.
Of those, **6 are parallel-structure scope** (5 HauntedHouse, 11
EnderKnightDungeon, 40 RubberDuckyPond, 43 FrogPond, 48 SpiderHangout,
49 RedAntHangout — NOT this batch) leaving **this batch's 13**:
4, 6, 8, 9, 10, 25, 31, 32, 33, 36, 41, 42, 45.

DSB dispatch re-verified against the FULL file (DSB:53-202): all 50
`if (type == N)` blocks fire exactly ONE builder call each. The only
Y-offset outliers in the whole table are types 43/44/45, which pass
`clickedY + 1` (DSB:183/186/189). Of this batch, only **45** is affected.

DSB shared preamble that RDS already reproduces (RDS:130-137): both the
spawner cell and the cell above are set to air BEFORE the roll
(DSB:50-51), then `world.rand.nextInt(50)` picks the outcome (DSB:52).
All 13 builders below therefore run against a live `ServerLevel` with the
level RNG — the original validated NOTHING before any of these calls
(no ground gate, no biome gate, no Y clamp), so floating / buried /
dimension-mismatched builds are faithful behavior (same ruling as the
FairyTree precedent, pattern doc §4).

---

## A. Direct DungeonType wiring — 7 outcomes via `LegacyDungeonPiece.buildNow`

`buildNow` (LDP:481-486) constructs the piece at the passed origin, swaps in
`level.random` (live RNG, matching DSB's `world.rand`), and runs one
`postProcess` pass with the whole bounding box as the write window. The ONLY
thing each case must get right is the origin: **four of the seven port
generators re-anchor origin at the structure's visual centre for `/locate`,
while the originals were corner-anchored at the passed cpos** — those four
need a constant X/Z offset so the build lands exactly where the original's
clicked-pos build did. Y needs no adjustment anywhere in group A (every
original anchored its own Y at the passed cposy, and every port generator
does the same with `origin.getY()`).

### A.1 — type 9 → SHADOW (offset 0,0,0)

- DSB:80-82 → `makeShadowDungeon(world, clickedX, clickedY, clickedZ)`.
- Original GD:1453-1537 (read in full, + `fill_shadow_chests` GD:1539-1568):
  CORNER-anchored — both stepped pyramids iterate `cposx + i + xoff` /
  `cposz + k + zoff` from the passed corner, lower pyramid `cposy - yoff`
  (GD:1478), upper `cposy + yoff` (GD:1527). **No scan, no relocation, no
  world reads.**
- Port `generateShadowDungeon` (LDP:636-707, Javadoc LDP:625-635 cites GD
  1453-1537 — same method): uses `origin.getX()/getY()/getZ()` directly as
  the corner (LDP:643-645), byte-matching loops. NO centering shift.
- Mob note (verified, no flag): original spawner names "Nightmare" /
  "Ender Reaper" (GD:1487-1489) → `ModEntities.PITCH_BLACK` /
  `ENDER_REAPER` (LDP:673-675). `PitchBlack` IS the port's Nightmare —
  original class `orig:PitchBlack.java` registered the "Nightmare" mob;
  `port:entity/PitchBlack.java:46` "The Nightmare".
- **Wiring**: `case TYPE_SHADOW_DUNGEON -> buildNow(server, pos, DungeonType.SHADOW)`.
- Box check: build spans origin+0..+18 X/Z, −9..+9 Y; box ±20 / −12 / +20
  (LDP:83) covers fully.

### A.2 — type 10 → ALIEN_WTF (offset 0,0,0)

- DSB:83-85 → `makeAlienWTFDungeon(world, clickedX, clickedY, clickedZ)`.
- Original GD:1570-1691 (read in full, + `makePart` GD:1693-1791): the
  builder itself shifts `cposy -= depth - 3` (= −17) at GD:1580 and builds
  everything relative to that — antenna at clickedY−17..−13, shaft up to
  clickedY+2, four Part rooms around the shaft floor. That −17 is INTERNAL
  to the builder, not a caller adjustment. No scan, no world reads.
- Port `generateAlienWtfDungeon` (LDP:1551-1613, Javadoc LDP:1527-1550 cites
  GD 1570-1691 — same method): `antennaY = cposy - (depth - 3)` (LDP:1564)
  reproduces GD:1580 internally; origin used raw (LDP:1556-1558). NO
  centering shift.
- **Wiring**: `case TYPE_ALIEN_WTF_DUNGEON -> buildNow(server, pos, DungeonType.ALIEN_WTF)`.
- Box check (CORRECTED by the independent re-derivation): deepest write
  antennaY = origin−17 (box down 25 ✓, LDP:93); X-widest is the west Part
  room `makeAlienPart(sx - 7, ..., 13, ..., dx=-1, ...)` at sx−7−12 =
  origin−19 (±20 ✓); but the SOUTH Part room `makeAlienPart(sx, antennaY,
  sz - 7, 15, 8, -1, -1, 4)` (GD:1674 → LDP:1611) spans z = (origin−7) −
  k, k ∈ [0,15) → **z reaches origin−21, OUTSIDE the box's minZOff −20**.
  The wiring offset is still 0,0,0 (the defect is the enum box, not the
  anchor) — see flag F8.

### A.3 — type 31 → KING_ALTAR (offset **+25, 0, +25**)

- DSB:146-148 → `makeKingAltar(world, clickedX, clickedY, clickedZ)`.
- Original GD:4353-4405 (read in full, + `makekingcolumn` GD:4407-4474,
  `makekingbackground` GD:4476-4523, `makekingcenteraltar` GD:4525-4675):
  CORNER-anchored — grass pad `cposx + i, i ∈ [0,51)` (GD:4373-4376), air
  envelope −5..+55 around that corner. One world READ: the dirt-skirt probe
  `func_147439_a(cposx+i, cposy-v, cposz+k)` for v=1..9 backfills
  air/tallgrass/water with dirt (GD:4377-4382) — a terrain conditional that
  is FAITHFUL to keep and is live-legal on the DSB path.
- Port `generateRoyalAltar(rng, true)` (LDP:1814-1878, Javadoc LDP:1797-1813
  cites GD 4353 — same method): **CENTERED** — `ox = cposx - 25; oz =
  cposz - 25` (LDP:1821-1822, "for a centred /locate hit"). The skirt read
  is reproduced as the sanctioned read-cell==write-cell probe
  (LDP:1847-1854).
- To make `ox == clickedX && oz == clickedZ` (the original's corner at the
  clicked pos): pass `origin = pos.offset(25, 0, 25)`.
- **Wiring**: `case TYPE_KING_ALTAR -> buildNow(server, pos.offset(25, 0, 25), DungeonType.KING_ALTAR)`.
- Box check: build spans ox−5..ox+55 = origin−30..origin+30; box ±32
  (LDP:101) ✓. (Y clipping issue pre-exists both paths — see flag F3.)

### A.4 — type 32 → LEONOPTERYX_NEST (offset 0,0,0)

- DSB:149-151 → `makeLeonNest(world, clickedX, clickedY, clickedZ)`.
- Original GD:4677-4729 (read in full): **CENTER-anchored** — hemisphere
  `cposx + i, i ∈ [-rad, rad]` descending `cposy - j` (GD:4685-4715), air
  pocket above (GD:4716-4723), spawner at `cposy - (rad-4)` = clickedY−6
  (GD:4724). No scan, no world reads (the shell palette rolls
  `world.rand.nextInt(6)` per cell, GD:4692 — live RNG on this path ✓).
- Port `generateLeonopteryxNest` (LDP:1751-1793, Javadoc LDP:1740-1750 cites
  GD 4677-4729 — same method): origin used raw as the same centre
  (LDP:1754-1756), spawner at `cposy - (rad - 4)` (LDP:1792). NO shift.
- **Wiring**: `case TYPE_LEON_NEST -> buildNow(server, pos, DungeonType.LEONOPTERYX_NEST)`.
- Box check: ±10 reach, −10/+5 Y; box ±12 / −10 / +5 (LDP:96) ✓ exactly.

### A.5 — type 36 → GREENHOUSE (offset **+11, 0, +7**)

- DSB:161-163 → `makeGreenhouseDungeon(world, clickedX, clickedY, clickedZ)`.
- Original GD:5030-5168 (read in full): CORNER-anchored — box `cposx + i,
  i ∈ [0,23)` (length along X), `cposz + k, k ∈ [0,15)` (width along Z),
  floor at cposy+0 (GD:5042-5130). No scan, no world reads; per-tile crop
  RNG (GD:5064-5126) is live RNG here ✓.
- Port `generateGreenhouse` (LDP:770-848, Javadoc LDP:761-769 cites GD
  5030-5168 — same method): **CENTERED** — `ox = cposx - length/2 =
  cposx - 11; oz = cposz - width/2 = cposz - 7` (LDP:780-781, "/locate puts
  the player inside").
- To restore the original corner at the clicked pos: `origin =
  pos.offset(11, 0, 7)`.
- **Wiring**: `case TYPE_GREENHOUSE_DUNGEON -> buildNow(server, pos.offset(11, 0, 7), DungeonType.GREENHOUSE)`.
- Box check: build spans origin−11..+11 X, −7..+7 Z, cposy−0..+13; box ±24 /
  −2 / +14 (LDP:84) ✓. (Door divergence pre-exists both paths — flag F1.)

### A.6 — type 41 → WHITE_HOUSE (offset **+12, 0, +9**)

- DSB:176-178 → `makeWhiteHouse(world, clickedX, clickedY, clickedZ)`.
- Original GD:5423-5434 (read in full, + all helpers `makefountain`
  GD:5436-5464, `makewalkway` :5466-5485, `makewhbase` :5487-5505,
  `makewhwalls` :5507-5552, `makewhroof` :5554-5597, `makewhinterior`
  :5599-5694): pure dispatcher anchored on the passed cpos — helpers at
  cposx−5/+15/+7/−4/−3/−1, cposz−15/−6/−5/−3. Footprint X −5..+22,
  Z −15..+19 relative to the clicked pos. No scan, no world reads.
- Port `generateWhiteHouse` (LDP:1304-1323, Javadoc LDP:1283-1303 cites GD
  5423 + helpers — same methods): **CENTERED** — `ox = cposx - 12; oz =
  cposz - 9` (LDP:1312-1313, "natural pivot is +12,+9 from the SW corner"),
  then dispatches with exactly the original's relative offsets
  (LDP:1315-1322).
- To restore the original anchor at the clicked pos: `origin =
  pos.offset(12, 0, 9)`.
- **Wiring**: `case TYPE_WHITE_HOUSE -> buildNow(server, pos.offset(12, 0, 9), DungeonType.WHITE_HOUSE)`.
- Box check: build spans origin−17..+10 X, −24..+10 Z, up to +21 Y; box ±48 /
  −2 / +25 (LDP:90) ✓. (Door upper-half divergence pre-exists — flag F2.)

### A.7 — type 42 → QUEEN_ALTAR (offset **+25, 0, +25**)

- DSB:179-181 → `makeQueenAltar(world, clickedX, clickedY, clickedZ)`.
- Original GD:5697-5748 (read in full, + `makequeencolumn` GD:5751-5814,
  `makequeenbackground` :5817-5863, `makequeencenteraltar` :5866-6015):
  structural twin of makeKingAltar — same corner anchor, same −5..+55 air
  envelope, same v=1..9 dirt-skirt world read (GD:5721-5726), obsidian/
  redstone/amethyst palette, TheQueenEgg in the apex chest slot 13
  (GD:6010-6014).
- Port `generateRoyalAltar(rng, false)` (LDP:1814-1878 — unified king/queen
  method, Javadoc cites GD 5697): same `ox/oz = cpos − 25` centering as A.3;
  queen palette + `THE_QUEEN_SPAWN_EGG` (LDP:2048-2051).
- **Wiring**: `case TYPE_QUEEN_ALTAR -> buildNow(server, pos.offset(25, 0, 25), DungeonType.QUEEN_ALTAR)`.

---

## B. CrystalStructures adapters — 2 outcomes (+1 redirected from group C, see C.4)

Precedent (read in full): `CS.buildRotatorStationAt` (CS:506-509, used by RDS
type 3 at RDS:220-221) — a thin public `(ServerLevel, RandomSource, BlockPos)`
wrapper that calls the shared private builder at the block position,
**skipping the worldgen `tryPlace*` scan entirely** (the DSB validates
nothing, CS:495-505 Javadoc). Both new adapters follow it verbatim; the
private builders are typed on `WorldGenLevel`, which `ServerLevel` satisfies
(pattern doc §4 worked example).

### B.1 — type 25 → `buildCrystalHauntedHouseAt` (offset 0,0,0)

- DSB:128-130 → `makeCrystalHauntedHouse(world, clickedX, clickedY, clickedZ)`.
  (NOT makeHauntedHouse GD:891 — that is type 5, parallel scope.)
- Original GD:2993-3104 (read in full): **CENTER-anchored** — walls
  `x ± width(3), z ± length(3)`, floor at y+0, roof at y+4 (GD:3015-3041);
  furnace/workbench/chest at x+2/x+1/x+0, y+1, z+2 (GD:3042-3051); hand-
  placed slot loot with per-slot rolls (GD:3052-3088, NOT a
  WeightedRandomChestContent list); Rat/Ghost/"Ghost Pumpkin Skelly"
  spawners stacked at the exact clicked column cposy+1..+3 (GD:3089-3103).
  **No scan, no relocation, no world reads.**
- Port private `CS.buildCrystalHauntedHouse(level, random, x, y, z)`
  (CS:673-721): center-anchored at (x,y,z) identically; slot loot via
  `fillHauntedHouseChest` (CS:1056-1080) matching GD:3052-3088 slot-by-slot;
  spawners at (x, y+1..y+3) (CS:718-720). Loot/chest handling is entirely
  inside the builder — the adapter adds nothing.
- Worldgen caller `tryPlaceCrystalHauntedHouse` (CS:653-671) carries the
  1/230 gate + 3-attempt jitter + Y100→51 air-over-crystal-grass scan (orig
  OSW:1669-1684) — **the adapter must skip ALL of it** (DSB semantics =
  build at the given pos).
- **New adapter** (place beside buildRotatorStationAt, CS:506):

  ```java
  /** RDS outcome type 25 (orig DungeonSpawnerBlock.java:128-130): makeCrystalHauntedHouse
   *  at the cleared spawner pos — no scan, no Y adjustment (GD:2993-3104 anchors on the
   *  passed centre). @return always true — the original builds unconditionally. */
  public static boolean buildCrystalHauntedHouseAt(ServerLevel level, RandomSource random, BlockPos pos) {
      buildCrystalHauntedHouse(level, random, pos.getX(), pos.getY(), pos.getZ());
      return true;
  }
  ```

- **Wiring**: `case TYPE_CRYSTAL_HAUNTED_HOUSE -> CrystalStructures.buildCrystalHauntedHouseAt(server, server.random, pos)`.

### B.2 — type 45 → `buildRoundRotatorAt` at **pos.above()** (DSB passes clickedY + 1)

- DSB:188-190 → `makeRoundRotator(world, clickedX, **clickedY + 1**, clickedZ)`
  — one of only three +1 outliers in the whole table (43/44/45).
- Original GD:6184-6258 (read in full): **CENTER-anchored vertical ring** in
  the X/Y plane at z = cposz — bedrock ring radius 6 and pink ring radius 2
  around centre (cposx, cposy+6) (GD:6193-6205); 4 Rotator + 4 Dungeon Beast
  spawners on the ring axes (GD:6206-6245); coal cross + centre chest with
  `CrystalBattleTowerVortexContentsList` at 6+nextInt(6) (GD:6246-6257).
  **No scan, no relocation, no world reads.** The +6 float is internal to
  the builder.
- Port private `CS.buildRoundRotator(level, random, posX, posY, posZ)`
  (CS:749-786): `centerY = posY + 6` (CS:751) reproduces the internal
  offset; chest bound to `BATTLE_TOWER_VORTEX_LOOT` (CS:785) — the ONE
  table shared with the battle tower's Vortex floor, correct under the
  (list, fill formula) rule: same list GD:36, same 6+nextInt(6) fill at
  GD:4958 and GD:6256. Loot/chest handling entirely inside the builder.
- Worldgen caller `tryPlaceRoundRotator` (CS:728-746) carries the 1/150
  gate + jitter + Y100→51 scan (orig OSW:1632-1645) — adapter skips all.
- **New adapter**:

  ```java
  /** RDS outcome type 45 (orig DungeonSpawnerBlock.java:188-190): makeRoundRotator at
   *  clickedY + 1 — the ONLY +1 outlier in this batch; the caller (RDS) passes
   *  pos.above(), mirroring the shipped TYPE_PUMPKIN precedent (RDS:293-299).
   *  No scan, no other adjustment (GD:6184-6258 floats itself +6 internally).
   *  @return always true — the original builds unconditionally. */
  public static boolean buildRoundRotatorAt(ServerLevel level, RandomSource random, BlockPos pos) {
      buildRoundRotator(level, random, pos.getX(), pos.getY(), pos.getZ());
      return true;
  }
  ```

- **Wiring**: `case TYPE_ROUND_ROTATOR -> CrystalStructures.buildRoundRotatorAt(server, server.random, pos.above())`
  — the `+1` lives at the call site (TYPE_PUMPKIN precedent, RDS:293-299),
  keeping the adapter's contract "build at the given pos".

---

## C. Feature-backed — 3 outcomes via extracted build cores, 1 redirect

**Key finding that shapes all of group C: none of the four Feature classes
anchors `place()` at `ctx.origin()`.** All four re-anchor through a
heightmap probe and veto on bounds/terrain:

| Feature | Re-anchor inside `place()` | Vetoes |
|---|---|---|
| `BeehiveFeature` | `getHeightmapPos(WORLD_SURFACE_WG, origin).above(3)` (Beehive:95-96) | build-height bounds (Beehive:99-100) |
| `SmallBeehiveFeature` | `getHeightmapPos(WORLD_SURFACE_WG, origin)` (SmallBeehive:111-112) | bounds (SmallBeehive:116-117) |
| `MantisNestFeature` | `getHeightmapPos(WORLD_SURFACE_WG, origin)` (MantisNest:82-87) | bounds (MantisNest:93-94) |
| `CrystalBattleTowerFeature` | `getHeightmapPos(WORLD_SURFACE_WG, origin)` (CBTFeature:79-80) | bounds + solid-footing (CBTFeature:87-93) |

The re-anchor is a WORLDGEN semantic (mirroring `addBeeHive`'s lowest-grass+3
/ `addANest`'s grass probe). The DSB semantic is different: DSB:65-79 passes
`clickedY` raw and the originals build there immediately — underground, in a
cave, on a sky platform, wherever the player put the block. **Therefore
resolving the configured feature (`orespawn:beehive` / `orespawn:small_beehive`
/ `orespawn:mantis_nest`, all `NoneFeatureConfiguration {}` per
`RES:worldgen/configured_feature/*.json`) and calling `place()` — or calling
`feature.place` with an inline config — CANNOT reproduce the clicked-pos
build**: place a spawner in a cave and the hive teleports to the surface;
place it high and the +3/bounds vetoes change outcomes. Neither
configured-feature invocation style is correct here.

The correct wiring, and the one that needs no new generator, is the same
shared-core refactor group B's builders already embody: **extract each
Feature's build core (everything after the anchor+veto preamble) into a
public static `buildAt(WorldGenLevel level, java.util.Random random,
BlockPos cpos)`, have `place()` keep its anchor/veto preamble and delegate,
and have RDS call `buildAt` directly at the clicked pos.** The cores are
verbatim loop transcriptions of the original `make*` methods anchored on
`cpos` — extraction changes zero behavior on the worldgen path. RNG bridge:
`place()` already builds `new java.util.Random(ctx.random().nextLong())`
(Beehive:89, SmallBeehive:106, MantisNest:80); the RDS call sites do the
same from the level RNG: `new java.util.Random(server.random.nextLong())`
(live, non-repeating — matches DSB's `world.rand` contract). Skip the
bounds vetoes on the DSB path: the originals had none, and
`ServerLevel.setBlock` silently no-ops outside build height, which is
exactly the 1.7.10 out-of-range behavior.

### C.1 — type 4 → `BeehiveFeature.buildAt(server, rng, pos)` (offset 0,0,0)

- DSB:65-67 → `makeBeeHive(world, clickedX, clickedY, clickedZ)`.
- Original GD:812-858 (read in full, + `fill_beehive_chests` GD:860-889):
  CORNER-anchored at the passed cpos (+X/+Z), building DOWNWARD — 10×5×10
  air chamber at cposy..cposy−4 (GD:821-827), coal-ore floor at cposy−30
  (GD:828-833), alternating coal/gold-ore shaft walls cposy−1..−29
  (GD:835-849), 4 Bee spawners on the centre column (GD:851-856), 4 chests
  every 2nd row with `beeContentsList` at **1+nextInt(5)** (GD:869/875/881/887).
  **No scan, no world reads, immediate build.** Internal terrain
  conditionals: none.
- Port `BeehiveFeature.place` (Beehive:87-150, read in full): steps 1-5
  (Beehive:110-149) are a verbatim transcription anchored on `cpos` —
  matching GD:812-858 loop-for-loop (in-code weighted fill `fillBeeLoot`
  Beehive:176-207 = beeContentsList at 1+nextInt(5)). Only the preamble
  (heightmap+3 + bounds, Beehive:95-100) is worldgen-specific.
- **Refactor**: extract Beehive:102-149 into
  `public static boolean buildAt(WorldGenLevel level, java.util.Random random, BlockPos cpos)`
  (returns true unconditionally); `place()` computes `cpos =
  getHeightmapPos(...).above(3)`, keeps its two bounds vetoes, then
  `return buildAt(level, random, cpos)`.
- **Wiring**: `case TYPE_BEE_HIVE -> BeehiveFeature.buildAt(server, new java.util.Random(server.random.nextLong()), pos)`.
- Anchor-match verdict: buildAt(cpos = clicked pos) ≡
  makeBeeHive(clickedX, clickedY, clickedZ). **Exact. Not NEEDS_DESIGN_RULING.**

### C.2 — type 8 → `SmallBeehiveFeature.buildAt(server, rng, pos)` (offset 0,0,0)

- DSB:77-79 → `makeSmallBeeHive(world, clickedX, clickedY, clickedZ)`.
- Original GD:1363-1451 (read in full): CORNER-anchored at the passed cpos
  (+X/+Z), building UPWARD — canopy air halo at cposy+14..+20 (GD:1376-1382),
  honeycomb(sponge) trunk-top + tapered mossy columns down from cposy+14
  (GD:1383-1400), three 2-layer skep tiers (GD:1401-1423), roof (GD:1424-1429),
  doorway carve at `j = height*2/3 + 1` = +15 (GD:1430-1437), 3 Bee spawners
  at (+1, j+0..2, +1) (GD:1438-1443), ONE chest at (width/2, j, width/2)
  with `beeContentsList` at **7+nextInt(5)** (GD:1444-1450). No scan, no
  world reads. (Same list as C.1 at a different fill count — if these
  in-code fills ever migrate to JSON, the (list, fill) rule mandates two
  tables; see flag F6.)
- Port `SmallBeehiveFeature.place` (SmallBeehive:104-210, read in full):
  steps 1-7 (SmallBeehive:119-209) verbatim on `cpos`, including the legacy
  j-reassignment quirk (SmallBeehive:184-187 = GD:1430); fill
  `fillBeeLoot(chest, random, 7 + random.nextInt(5))` (SmallBeehive:207).
  Preamble = heightmap anchor + bounds (SmallBeehive:108-117).
- **Refactor**: extract SmallBeehive:119-209 into `buildAt` as in C.1;
  `place()` keeps anchor + vetoes and delegates.
- **Wiring**: `case TYPE_SMALL_BEE_HIVE -> SmallBeehiveFeature.buildAt(server, new java.util.Random(server.random.nextLong()), pos)`.
- Anchor-match verdict: **exact**.

### C.3 — type 6 → `MantisNestFeature.buildAt(server, rng, pos)` (offset 0,0,0)

- DSB:71-73 → `makeMantisHive(world, clickedX, clickedY, clickedZ)`.
- Original GD:1012-1062 (read in full, + `fill_mantishive_chests`
  GD:1064-1093): CORNER-anchored at the passed cpos (+X/+Z) — 13×20×13 air
  chamber UP from cposy (GD:1021-1027), stepped pyramid DOWN (13→1,
  `cposy - yoff`, gold/emerald-ore rings, GD:1031-1052), 4 chests at the
  width 11/9/7 steps with `mantisContentsList` at **3+nextInt(7)**
  (GD:1045-1047 → :1073-1091), 3 Mantis spawners on the cap after the
  xoff/yoff/zoff decrement (GD:1053-1061). No scan, no world reads.
- Port `MantisNestFeature.place` (MantisNest:78-151, read in full): steps
  1-4 (MantisNest:96-150) verbatim on `cpos`, including the decrement quirk
  (MantisNest:142-144) and the cap-spawner coordinates
  `cpos.offset(xoff, j - yoff, yoff)` (MantisNest:148 = GD:1057's
  `cposz + yoff` — yes, the original really uses yoff as the Z offset).
  In-code fill `fillMantisLoot` (MantisNest:182-213) = mantisContentsList
  at 3+nextInt(7). Preamble = heightmap anchor + bounds (MantisNest:82-94).
- **Refactor**: extract MantisNest:96-150 into `buildAt` as in C.1.
- **Wiring**: `case TYPE_MANTIS_HIVE -> MantisNestFeature.buildAt(server, new java.util.Random(server.random.nextLong()), pos)`.
- Anchor-match verdict: **exact**.

### C.4 — type 33 → REDIRECT to `CrystalStructures.buildCrystalBattleTowerAt` (offset 0,0,0)

- DSB:152-154 → `makeCrystalBattleTower(world, clickedX, clickedY, clickedZ)`.
- Original GD:4831-4959 (read in full): **CENTER-anchored** cylinder —
  polar-swept discs/rings around (cposx, cposz), base disc at cposy+0, walls
  to cposy+20, CrystalCrystal rim cposy+21..22 (GD:4842-4874); five floors
  at cposy+1/6/11/16/21, each = centre chest + two stacked spawners
  (Rat / Dungeon Beast / Crystal Urchin / Rotator / Vortex), chest lists
  `CrystalBattleTower{Rat,DungeonBeast,Urchin,Rotator}ContentsList` at
  **5+nextInt(5)** and `...VortexContentsList` at **6+nextInt(6)**
  (GD:4875-4959). No scan, no world reads, no relocation.
- The repo contains TWO ports of this method; they are NOT equivalent:
  1. `port:world/feature/CrystalBattleTowerFeature` (read in full, 240
     lines): Phase 13A "modernized" — heightmap re-anchor + solid-footing
     VETO (CBTFeature:79-93, rejects air/liquid below — a terrain veto the
     original never had), and **invented per-floor chest contents**
     (`fillFloorChest` CBTFeature:194-239: fixed-slot food/dye/armor
     palettes that match none of the five original weighted lists). It is
     also **datapack-dead**: no configured_feature / placed_feature /
     structure JSON references `orespawn:crystal_battle_tower` (verified by
     grep over `RES:` — zero hits).
  2. `CS.buildCrystalBattleTower` (CS:813-870 + `placeBattleTowerFloor`
     CS:872-878, read in full): the FAITHFUL transcription — identical polar
     loops (CS:819-855 = GD:4842-4874), five floors with the correct mob
     ladder and the real loot tables `battle_tower_{rat,dungeon_beast,
     urchin,rotator,vortex}` whose rolls encode 5+nextInt(5) / 6+nextInt(6)
     (CS:857-869). This is the LIVE crystal-worldgen path
     (`CS.generate` → `tryPlaceCrystalBattleTower` CS:793-810, 1/280 gate,
     orig OSW:1686-1703).
- Verdict on the Feature per the task's test: its `place()` neither anchors
  at `ctx.origin()` nor reproduces the original loot — wiring RDS to it
  would ship a fabricated interior. **Not NEEDS_DESIGN_RULING** — the
  faithful builder already exists; the DSB wiring simply goes through
  CrystalStructures like B.1/B.2:

  ```java
  /** RDS outcome type 33 (orig DungeonSpawnerBlock.java:152-154): makeCrystalBattleTower
   *  at the cleared spawner pos — centre-anchored, base disc at pos.getY() (GD:4831-4959),
   *  no scan, no Y adjustment. NOT CrystalBattleTowerFeature — that class is the
   *  datapack-dead Phase 13A modernization with invented loot (see dsb_sweep_spec §C.4).
   *  @return always true — the original builds unconditionally. */
  public static boolean buildCrystalBattleTowerAt(ServerLevel level, RandomSource random, BlockPos pos) {
      buildCrystalBattleTower(level, random, pos.getX(), pos.getY(), pos.getZ());
      return true;
  }
  ```

- **Wiring**: `case TYPE_CRYSTAL_BATTLE_TOWER -> CrystalStructures.buildCrystalBattleTowerAt(server, server.random, pos)`.
- Loot: entirely inside the builder via the five existing
  `battle_tower_*` tables — nothing new needed.

---

## Output table — buildForType wiring for batch 4

RDS constants keyed by ORIGINAL index with orig line citations, per the
established style (RDS:44-103).

| type | RDS constant (new) | port call | X/Y/Z offset on `pos` | cite (orig → port) |
|---|---|---|---|---|
| 4 | TYPE_BEE_HIVE | `BeehiveFeature.buildAt(server, new Random(server.random.nextLong()), pos)` | 0,0,0 | DSB:65-67; GD:812-858 → Beehive:102-149 (extracted core) |
| 6 | TYPE_MANTIS_HIVE | `MantisNestFeature.buildAt(server, new Random(server.random.nextLong()), pos)` | 0,0,0 | DSB:71-73; GD:1012-1062 → MantisNest:96-150 (extracted core) |
| 8 | TYPE_SMALL_BEE_HIVE | `SmallBeehiveFeature.buildAt(server, new Random(server.random.nextLong()), pos)` | 0,0,0 | DSB:77-79; GD:1363-1451 → SmallBeehive:119-209 (extracted core) |
| 9 | TYPE_SHADOW_DUNGEON | `buildNow(server, pos, SHADOW)` | 0,0,0 | DSB:80-82; GD:1453-1537 → LDP:636-707 |
| 10 | TYPE_ALIEN_WTF_DUNGEON | `buildNow(server, pos, ALIEN_WTF)` | 0,0,0 | DSB:83-85; GD:1570-1691 → LDP:1551-1613 |
| 25 | TYPE_CRYSTAL_HAUNTED_HOUSE | `CrystalStructures.buildCrystalHauntedHouseAt(server, server.random, pos)` | 0,0,0 | DSB:128-130; GD:2993-3104 → CS:673-721 (new adapter) |
| 31 | TYPE_KING_ALTAR | `buildNow(server, pos.offset(25, 0, 25), KING_ALTAR)` | **+25,0,+25** | DSB:146-148; GD:4353-4405 → LDP:1814-1878 (port centres, LDP:1821-1822) |
| 32 | TYPE_LEON_NEST | `buildNow(server, pos, LEONOPTERYX_NEST)` | 0,0,0 | DSB:149-151; GD:4677-4729 → LDP:1751-1793 |
| 33 | TYPE_CRYSTAL_BATTLE_TOWER | `CrystalStructures.buildCrystalBattleTowerAt(server, server.random, pos)` | 0,0,0 | DSB:152-154; GD:4831-4959 → CS:813-870 (new adapter; NOT the Feature — §C.4) |
| 36 | TYPE_GREENHOUSE_DUNGEON | `buildNow(server, pos.offset(11, 0, 7), GREENHOUSE)` | **+11,0,+7** | DSB:161-163; GD:5030-5168 → LDP:770-848 (port centres, LDP:780-781) |
| 41 | TYPE_WHITE_HOUSE | `buildNow(server, pos.offset(12, 0, 9), WHITE_HOUSE)` | **+12,0,+9** | DSB:176-178; GD:5423-5434 → LDP:1304-1323 (port centres, LDP:1312-1313) |
| 42 | TYPE_QUEEN_ALTAR | `buildNow(server, pos.offset(25, 0, 25), QUEEN_ALTAR)` | **+25,0,+25** | DSB:179-181; GD:5697-5748 → LDP:1814-1878 (port centres, LDP:1821-1822) |
| 45 | TYPE_ROUND_ROTATOR | `CrystalStructures.buildRoundRotatorAt(server, server.random, pos.above())` | **0,+1,0** (DSB:189 `clickedY + 1`) | DSB:188-190; GD:6184-6258 → CS:749-786 (new adapter; +1 at call site per TYPE_PUMPKIN precedent RDS:293-299) |

New code surface: 3 public adapters in CS (§B.1, §B.2, §C.4), 3 core
extractions in the Feature classes (§C.1-C.3), 13 switch cases + constants
in RDS. **No new generators, no new JSONs, no new loot tables, no new salts**
(all group-A worldgen JSONs already exist under `RES:worldgen/structure/`;
group B/C loot is in-code or already-shipped tables).

## Coverage after batch 4

- After batch 4 alone: 31 + 13 = **44 wired**; unwired = **{5, 11, 40, 43,
  48, 49}** — exactly the six being ported as full structures in parallel.
- After batch 4 + the parallel batch: **NONE unwired** (50/50).
- The RDS `default -> placeGenericDungeonAt` fallback (RDS:306) becomes
  dead once all 50 are wired; recommend the parallel batch (whichever lands
  last) keeps it as a documented safety net rather than deleting — the
  switch is on `nextInt(50)` so it is provably unreachable, but it is the
  established interim contract.

## Flags (pre-existing divergences observed during full reads — none block the wiring; F1-F6 affect worldgen and DSB paths equally, F7-F9 are noted per-flag)

- **F1 — Greenhouse door divergence**: original places TWO iron doors at
  `cposx + width/2` (+7) and `+6` on the Z=0 wall plus two stone lintels
  and two stone buttons (GD:5138-5147); port carves the air at +7/+6 but
  places a single door column at `ox + length/2` (+11) and omits lintels +
  buttons (LDP:831-838). Candidate reconciliation finding.
- **F2 — White House door**: original places a full 2-tall iron door via
  `ItemDoor.func_150924_a` (GD:5550); port places only the LOWER half
  (LDP:1420). Minor.
- **F3 — Royal altar box clipping**: `KING_ALTAR/QUEEN_ALTAR(32, 4, 56)`
  (LDP:101-102) — downExtent 4 clips the original's v=1..9 dirt skirt
  (GD:4377-4382/5721-5726) below origin−4, and upExtent 56 clips the top
  2 rows of the j≤height+10 air clear (GD:4364/5708). The gated `place()`
  drops those writes in BOTH worldgen and buildNow.
- **F4 — `CrystalBattleTowerFeature` is dead + unfaithful**: registered in
  ModFeatures (ModFeatures:89-90) but referenced by zero datapack JSONs;
  loot is invented (CBTFeature:194-239). Superseded by CS:813-870.
  Deletion candidate under the no-procedural-fabrication rule (same
  treatment as ChallengeTowerFeature / UfoCrashSiteFeature,
  ModFeatures:49-84). Do NOT wire RDS to it.
- **F5 — `CrystalMazeFeature`** likewise has no datapack references
  (grep over `RES:` — zero hits); same audit-cleanup candidacy as F4.
  (Not otherwise examined — out of scope.)
- **F6 — bee list at two fill counts**: `beeContentsList` (GD:55) is
  consumed at 1+nextInt(5) (deep hive, GD:869) and 7+nextInt(5) (skep,
  GD:1449). Currently both fills are in-code (Beehive:176-207 /
  SmallBeehive:219-249) so the one-table-per-(list,fill) rule is not yet
  engaged; if these migrate to JSON, TWO tables are required with a
  `_shared_list` cross-reference (pattern §1 step 5 as amended).
- **F8 — ALIEN_WTF box clips the south Part room's far wall** (found by
  the independent verify pass): `ALIEN_WTF(20, 25, 6)` (LDP:93) has
  minZOff −20, but the south Part room (GD:1674 → LDP:1611,
  `makeAlienPart(sx, antennaY, sz - 7, 15, 8, -1, -1, 4)`) writes down to
  z = origin−21 (its far Z wall plane, plus that row of floor/ceiling/air
  carve — chests/spawners are all at z ≥ −20). `buildNow` uses the piece
  bounding box as its write window (LDP:484-485), so the DSB path ALWAYS
  drops that wall plane; the worldgen path drops it only when the box edge
  lands on a chunk border (~1/16 of placements → open-walled south rooms
  with seams). Fix candidate: widen to the asymmetric
  `ALIEN_WTF(-20, 20, 25, 6, -22, 20, PlacementMode.SURFACE_CENTER)`
  (actual footprint X −19..+17, Z −21..+15, +1 margin). NOTE the piece
  RandomSource is seeded from the bounding box (pattern doc §1 step 3), so
  widening changes worldgen layouts for existing seeds — land it with the
  wiring batch and document the delta, same treatment as F3.
- **F9 — RDS:45 comment mis-cite (FIXED in place during this verify
  pass)**: the `TYPE_ROTATOR_STATION = 3` constant was annotated
  "orig DungeonSpawnerBlock.java:71-73" — that is type 6 (makeMantisHive,
  this batch's C.3); type 3 is DSB:62-64. Corrected in RDS so the batch-4
  constants (which key on DSB citations) don't inherit the bad anchor.
- **F7 — ROBOT_LAB (type 30, ALREADY WIRED) has the same offset gap this
  batch fixes for 31/36/41/42**: the original `makeRobotLab` is
  corner-anchored at the passed cpos (`cposx + i`, i<10 / `cposz + k`, k<20,
  GD:4053-4059 — no internal recentring), but `generateRobotLab` recentres
  `ox = cposx - 5; oz = cposz - 25` (LDP:955-956, the documented "invented
  recentring", robot_lab_audit_spec.md §18 item 10). The shipped RDS case
  passes `pos` raw (RDS:209-213), so the DSB build lands shifted
  (−5, 0, −25) from the original's clicked-pos build. Follow-up: change the
  existing case to `buildNow(server, pos.offset(5, 0, 25), ROBOT_LAB)` —
  out of this batch's 13 but the identical defect class, discovered while
  deriving the group-A offsets.
