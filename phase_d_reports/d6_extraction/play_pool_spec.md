# Play Pool — Extraction Spec (1.7.10 → NeoForge 1.21.1)

Source of truth: `reference_1_7_10_source/sources/danger/orespawn/GenericDungeon.java` —
`makePlayPool` (GD:1934-1957; next method `makeWaterDragonLair` starts GD:1959).
All coordinates are relative to the build origin `(cposx, cposy, cposz)` = the three int args.

Citation convention: `GD:NN` = GenericDungeon.java line NN, `OSW:NN` = OreSpawnWorld.java,
`OSM:NN` = OreSpawnMain.java, `DSB:NN` = DungeonSpawnerBlock.java. Port citations name the port file + line.

Mechanism decision (structure_conversion_pattern.md §0): `LegacyDungeonStructure` +
`PlayPoolGenerator` — multi-block build spanning up to 2 chunks in X (6-wide water row at a
random in-chunk column), needs `/locate` + set-frequency control. Not a ≤12-block same-chunk box
candidate (and chunk-hook additions are forbidden anyway).

No `NEEDS_DESIGN_RULING` flag — everything below fits the pattern doc's mechanisms. The only
non-catalog item is the placement anchor, which is a one-line +1 variant of the existing
`OCEAN_SURFACE` probe (see §7).

---

## 1. Entry points — EVERY call site (grep `makePlayPool` over the whole original tree: 2 callers)

| Caller | Signature used | Coords passed | Notes |
|---|---|---|---|
| `OreSpawnWorld.addPlayPool` (OSW:1148) | `OreSpawnMain.MyDungeon.makePlayPool(world, posX, posY, posZ)` | scan hit, **no Y offset** — `posY` is the AIR block directly above the water surface (contrast `addMonsterIsland` OSW:1410 and `addFrogPond` OSW:1168, which pass `posY - 1`) | worldgen path, vanilla-overworld Ocean only (§1.1) |
| `DungeonSpawnerBlock` type **12** (DSB:89-91) | `...makePlayPool(world, clickedX, clickedY, clickedZ)` | player-placed block pos, no offset | `type = world.rand.nextInt(50)` (DSB:52); single-builder index — the `if (type == 12)` block (DSB:89-91) contains exactly one call, verified in full |

### 1.1 `addPlayPool` — FULL method + return contract (OSW:1136-1154)

Dispatch context: `generateSurface` runs the overworld-dungeon picker only when
`OreSpawnMain.DisableOverworldDungeons == 0 && world.provider.dimensionId == 0 &&
recently_placed == 0` (OSW:284). Picker: `i = world.field_73012_v.nextInt(6)` (OSW:285 —
**world rand**, not the chunk `random`); **`i==0` → `addPlayPool(world, random, chunkX, chunkZ)`**
(OSW:286-288). Peers: 1 WaterDragonLair, 2 GoldFishBowl, 3 GirlfriendIsland, 4 MonsterIsland,
5 FrogPond (OSW:289-303). The picker result does not affect the `addANest → … → addBouncyCastle`
boolean chain that follows (OSW:304+); the i-branches are fire-and-forget.

`addPlayPool(world, random, chunkX, chunkZ)` step by step:

1. Gate: `random.nextInt(350) != 0 → return` (OSW:1137-1139) — 1/350, **chunk-provided `random`**.
2. Biome: `world.func_72807_a(chunkX, chunkZ)` (block coords = chunk corner) must have
   `biomeName` **exactly `"Ocean"`** (OSW:1140-1141) — excludes Deep Ocean, FrozenOcean, beaches.
3. Up to 4 attempts (OSW:1142): `posX = chunkX + random.nextInt(16)`,
   `posZ = chunkZ + random.nextInt(16)` (OSW:1143-1144). (`boolean which = false` OSW:1145 is a
   dead decompiler local — never read.)
4. Column scan `posY = 100` down to `41` inclusive (`posY > 40`, OSW:1146): continue unless
   `block(posX, posY, posZ) == air` AND `block(posX, posY-1, posZ) == Blocks.field_150355_j`
   (**still water** directly below) (OSW:1147).
5. Hit → `makePlayPool(world, posX, posY, posZ)`; `recently_placed = 50`; `return`
   (OSW:1148-1150).

**Return contract: `void`.** No boolean, no side effect on failure. `recently_placed = 50`
(the global 50-chunk cooldown, OSW:30) is set **only on a successful placement** — a failed
gate/biome/scan leaves the cooldown untouched and suppresses nothing else. Nothing downstream
branches on this method (unlike the WGEN-062 `addFairyTree` trap).

Effective odds: 1/6 × 1/350 = **1/2100 per eligible overworld chunk** (before the Ocean-biome
and scan-success filters, which map to the biome filter + placement-mode rejection in the port).

---

## 2. `makePlayPool` — the build, line by line (GD:1934-1957)

Write order as in source (spawners → chests+fill → water):

| # | What | Where (relative, inclusive) | Block / call | Flags | Cite |
|---|---|---|---|---|---|
| 1 | Spawner row, loop `i = 0..3` | `(0..3, +16, 0)` — 4 blocks along +X | `func_147465_d(..., Blocks.field_150474_ac, 0, 2)` then `getSpawnerTileEntity` → `func_98272_a("Attack Squid")` | 2 | GD:1940-1945 |
| 2 | Chest pair | `(+1, +17, 0)` and `(+2, +17, 0)` — on top of the two middle spawners | `func_147465_d(..., Blocks.field_150486_ae, 0, 2)` | 2 | GD:1946-1947 |
| 3 | Loot fill — **first chest only** | TE fetched at `(+1, +17, 0)` only | `WeightedRandomChestContent.func_76293_a(world.rand, SquidContentsList, chest, 3 + world.rand.nextInt(5))` → 3-7 pulls | — | GD:1948-1951 |
| 4 | Still-water row, loop `i = 0..3` | `(0..3, +18, 0)` | `Blocks.field_150355_j` (water, static) | **3** | GD:1952-1954 |
| 5 | Flowing-water caps | `(-1, +18, 0)` and `(+4, +18, 0)` | `Blocks.field_150358_i` (flowing_water, meta 0) | **3** | GD:1955-1956 |

Net shape: a floating 6-long water trough at `y+18` (X `-1..+4`, single Z column) pouring down
around a row of 4 Attack Squid spawners at `y+16` and a chest pair at `y+17`. Only the middle
two columns (`x+1`, `x+2`) are capped by chests; water falls at `x-1`, `x`, `x+3`, `x+4`.
Built ~17 blocks above the ocean surface (see §7 anchor), so the cascade drops into open sea.

RNG draws inside the builder, in order: **only** the chest fill count + fill pulls
(`world.rand`, GD:1950). Port moves all of it into the loot JSON (§4) → **the port generator
consumes ZERO random draws** — trivially stitch-safe (pattern doc §1 step 3). No conditional
draws, no entity spawns, no `Math.random()`.

Helper contracts used: `FastSetBlock` is NOT used here — every write is a direct
`world.func_147465_d` (setBlock). Spawner/chest writes use flag 2; water uses flag 3
(update + notify neighbours) — the method's only flag-3 writes, clearly intended to start the
water flowing. Port note (PARITY, non-behavioral): `piece.place` writes with
`UPDATE_CLIENTS` (flag 2) only, but modern `LiquidBlock.onPlace` self-schedules a fluid tick on
placement, so the cascade still starts without neighbour notifications; vanilla worldgen water
behaves identically.

---

## 3. World-block READS mid-build

**None.** The only world reads are the two post-placement tile-entity fetches
(`getSpawnerTileEntity` GD:86-95 via GD:1942, `getChestTileEntity` GD:75-84 via GD:1948),
already absorbed by the port's `placeSpawner` / `placeLootChest` helpers
(LegacyDungeonPiece.java:461-468, :379-397). No `func_147439_a` calls inside GD:1934-1957.
The RNG-stitching "never branch on world state" rule is satisfied with no in-memory model.

---

## 4. Loot — FULL transcription of `SquidContentsList` (GD:49)

Fill: `3 + world.rand.nextInt(5)` → **`rolls` uniform 3-7** (GD:1950), single pool.
Constructor semantics `(item, meta=0, minStack, maxStack, weight)`. **Total weight = 80.**

| # | 1.7.10 item (GD:49) | Modern / port mapping (cite) | min | max | weight |
|---|---|---|---|---|---|
| 1 | `Items.field_151100_aR` (dye, **meta 0 = ink sac**) | `minecraft:ink_sac` | 6 | 16 | 25 |
| 2 | `OreSpawnMain.MySquidZooka` (OSM:1759 "squidzookasmall", registered OSM:2302 "OreSpawn_SquidZooka") | port `ModItems.SQUID_ZOOKA` "squid_zooka" (ModItems.java:434-435) | 1 | 1 | 15 |
| 3 | `Items.field_151074_bl` | `minecraft:gold_nugget` | 5 | 15 | 15 |
| 4 | `Items.field_151078_bh` | `minecraft:rotten_flesh` | 6 | 16 | 25 |

New file: `RES:loot_table/chests/play_pool.json` — copy the `chests/monster_island.json` shape
(`"type": "minecraft:chest"`, uniform rolls 3-7, one weighted `minecraft:item` entry each with
`set_count` uniform; omit `set_count` for the 1/1 SquidZooka). Documented approximation
(pattern doc step 5): original random-slot placement could self-overwrite; a loot pool never
collides — slightly higher average yield, same per-pull distribution.

Bind via `piece.placeLootChest(x+1, y+17, z, PLAY_POOL_LOOT)` — **the `x+2` chest gets NO loot
table** (see surprise S1).

---

## 5. Spawner / mob mapping

| Spawner name | 1.7.10 registration | Port EntityType (cite) |
|---|---|---|
| `"Attack Squid"` ×4 | `AttackSquid.class` — `registerGlobalEntityID` OSM:3863, `registerModEntity` OSM:3867 | `ModEntities.ATTACK_SQUID` "attack_squid" (ModEntities.java:39-42) |

No direct entity spawns; no `spawnPersistent` calls needed.

---

## 6. Block palette

| 1.7.10 field | Modern block | Used for | Cite |
|---|---|---|---|
| `Blocks.field_150474_ac` | `minecraft:spawner` | 4 spawners at y+16 | GD:1941 |
| `Blocks.field_150486_ae` | `minecraft:chest` | 2 chests at y+17 | GD:1946-1947 |
| `Blocks.field_150355_j` | `minecraft:water` (source) | 4-block trough middle | GD:1953 |
| `Blocks.field_150358_i` (flowing_water, meta 0) | `minecraft:water` (source) — the 1.13 flattening maps both water blocks at meta 0 to the source state | 2 trough caps | GD:1955-1956 |

---

## 7. Footprint, DungeonType entry, placement

Footprint extents relative to `(cposx, cposy, cposz)`:

| Axis | Min | Max | Extent | Determined by |
|---|---|---|---|---|
| X | `-1` | `+4` | **6** | flowing-water caps (GD:1955-1956) |
| Y | `+16` | `+18` | **3** (all writes ≥ origin) | spawner row (GD:1941) / water row (GD:1953) |
| Z | `0` | `0` | **1** | every write at `cposz` exactly |

Suggested `DungeonType` entry (6-int asymmetric ctor `(minX, maxX, down, up, minZ, maxZ)`,
margins +1; up covers writes at +18):

```
PLAY_POOL(-2, 5, 1, 19, -1, 1, PlacementMode.OCEAN_SURFACE_AIR),
```

**PlacementMode: NEEDS_NEW_MODE — but it is a one-line sibling of `OCEAN_SURFACE`.**
The original scan, quoted (OSW:1146-1148):

```java
for (int posY = 100; posY > 40; --posY) {
    if (world.func_147439_a(posX, posY, posZ) != Blocks.field_150350_a || world.func_147439_a(posX, posY - 1, posZ) != Blocks.field_150355_j) continue;
    OreSpawnMain.MyDungeon.makePlayPool(world, posX, posY, posZ);
```

This is byte-for-byte the Monster Island / FrogPond scan (OSW:1409, OSW:1167 pattern) —
which `oceanSurfaceOrigin` already ports (LegacyDungeonStructure.java:188-208: 4 attempts,
random in-chunk column, `WORLD_SURFACE_WG − 1` water-surface band 40..99, ocean-floor <
first-free water check). The ONLY difference is the anchor handed to the builder:
Monster Island passes `posY − 1` (the water block — exactly what `OCEAN_SURFACE` returns),
Play Pool passes `posY` (the **air block above** the water). Recommended port: add
`OCEAN_SURFACE_AIR` to `PlacementMode` with a `findGenerationPoint` case that reuses
`oceanSurfaceOrigin(context)` and returns it `.above()` — do NOT bake a +1 into the generator's
offsets (that would shift the DSB `buildNow` path, whose origin is the clicked pos with no
offset, DSB:89-91). Fallback if a new mode is vetoed: reuse `OCEAN_SURFACE` and accept the
structure sitting 1 block lower in worldgen — a documented deviation; the DSB path stays exact
either way.

`findGenerationPoint`'s max-height rejection (LegacyDungeonStructure.java:74):
anchor ≤ 100, `100 + 19 + 4 = 123 < 320` — never trips.

---

## 8. Structure-set conversion + JSONs

Odds 1/2100 (§1.1) → C7 sqrt equivalence: spacing ≈ √2100 ≈ 45.8 → **spacing 46,
separation 23**. **Salt 84350** (assigned to this task; grep of
`RES:worldgen/structure_set/*.json` confirms current max in the block is 84345 — free.
Batch-1 range 84350-84355.)

- `RES:worldgen/structure/play_pool.json` — `"type": "orespawn:legacy_dungeon"`,
  `"dungeon_type": "PLAY_POOL"`, `"biomes": "minecraft:ocean"` (single biome, matching the
  exact-`"Ocean"` name check OSW:1141 — same treatment as `structure/monster_island.json:3`),
  `"step": "surface_structures"`, `"spawn_overrides": {}`, `"terrain_adaptation": "none"`.
- `RES:worldgen/structure_set/play_pool.json` — random_spread 46/23, salt 84350.
- The `recently_placed = 50` cooldown + the 1/6 picker coupling map onto set
  spacing/separation per the C7-approved approximation (pattern doc step 4) — do not
  reproduce the cross-structure coupling.

## 9. DungeonSpawnerBlock outcome

`TYPE_PLAY_POOL = 12` (orig DSB:89-91, single call, clicked pos unmodified) →
`case TYPE_PLAY_POOL -> { LegacyDungeonPiece.buildNow(server, pos, DungeonType.PLAY_POOL); yield true; }`
in `port:block/entity/RandomDungeonSpawnerBlockEntity.buildForType` (`buildNow` is `void`
(LegacyDungeonPiece.java:351) and `buildForType` returns `boolean`, so use the block-with-`yield true`
form every other `buildNow` case uses, e.g. TYPE_MONSTER_ISLAND at :180-184). Currently registered
cases: 0, 1, 2, 7, 21, 22, 23, 24, 27, 29, 30, 37, 38, 47 — RandomDungeonSpawnerBlockEntity.java:44-69,
121-188; **type 12 currently falls through to the generic-dungeon fallback** (`default ->
placeGenericDungeonAt`, :186). The DSB path
performs no ocean/biome/height validation (DSB:52, 89-91) — a Play Pool built on land, in a
cave, or underground is faithful behavior.

## 10. Surprises / MISSING-IN-PORT

- **S1 — only ONE of the two chests is filled.** Both chests are placed (GD:1946-1947) but the
  TE fetch + loot fill target `(x+1, y+17, z)` only (GD:1948-1951); the `x+2` chest is placed
  empty. In 1.7.10 the adjacent pair auto-merged into a double chest, so players saw one
  54-slot double chest with loot only in the west half. Port: loot table on the `x+1` chest
  only; plain chest at `x+2`. Do not "fix" by filling both.
- **S2 — double-chest cosmetics (adaptation decision).** Modern chests do not auto-merge:
  two default-state chests stay singles. `placeLootChest` supports a facing but not
  `ChestType` (LegacyDungeonPiece.java:390-403). Either extend the helper to set
  FACING=NORTH + LEFT/RIGHT so the pair reads as the original double chest (cosmetic-only),
  or accept two singles as a documented deviation. Loot behavior is identical either way.
- **S3 — flag-3 water writes** (GD:1953-1956), the method's only non-flag-2 writes — the
  original relied on neighbour notification to start the cascade. Non-issue in the port
  (modern fluids self-schedule ticks on placement); record as a PARITY note, not a deviation.
- **S4 — anchor is the AIR block above the water surface** (`posY`, OSW:1148), unlike the
  sibling ocean structures' `posY − 1` — the reason `OCEAN_SURFACE` alone is off by one (§7).
- **S5 — spawner activation risk (TESTING_CHECKLIST item).** The port registers AttackSquid
  spawn placement as `SpawnPlacementTypes.IN_WATER` (ModEntityAttributes.java:184); the
  spawners sit at y+16 with water only cascading past them. If the modern `BaseSpawner`
  placement check keeps them dormant in air, that is a behavior delta vs 1.7.10 (whose spawner
  check used `getCanSpawnHere`, orig AttackSquid.java:645-651: y ≥ 50 + daytime). Verify
  in-game; decide then whether a delta note suffices — do NOT pre-emptively change spawn rules.
- **S6 — dead local** `boolean which = false` in `addPlayPool` (OSW:1145) — decompiler
  artifact, never read; do not port.
- **S7 — mixed RNG sources** in the dispatch: the 6-way picker uses `world.rand` (OSW:285),
  the 1/350 gate and column picks use the chunk `random` (OSW:1137, 1143-1144), the builder's
  loot fill uses `world.rand` (GD:1950). All collapse into set-placement + JSON rolls in the
  port; the DSB path keeps live `world.rand` semantics via `buildNow`.
- **MISSING-IN-PORT:** `makePlayPool` has no counterpart anywhere in
  `src/main/java/danger/orespawn/` (grep `PlayPool|play_pool`: matches only in the two 1.7.10
  source copies). No `PLAY_POOL` DungeonType, no generator, no JSON trio, no DSB case 12.

## 11. RNG stitching order (port generator)

Nothing to stitch: after the loot fill moves to JSON, the generator draws zero randomness —
every pass over every intersecting chunk (box spans ≤ 2 chunks in X) replays an identical,
draw-free sequence. Keep it that way: place spawners → chests → water in source order,
all through `piece.placeSpawner` / `piece.placeLootChest` / `piece.place`.
