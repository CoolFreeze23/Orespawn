# Urchin Spawner — Extraction Spec (1.7.10 → NeoForge 1.21.1)

**STATUS: RECONCILIATION — the structure is ALREADY PORTED** as a decoration-phase direct builder in
`port:world/CrystalStructures.java` (`tryPlaceUrchinSpawner` :508-526, `buildUrchinSpawner` :528-574).
Per structure_conversion_pattern.md §2 (Challenge Tower precedent) this spec is a line-by-line diff of
the port against the original, not a new-conversion design. **Verdict: the port is faithful except ONE
discrepancy (missing `UrchinEnable` config gate, §11.1). No LegacyDungeonStructure conversion should be
performed — see §8. Salt 84353 is not consumed.**

Source of truth: `reference_1_7_10_source/sources/danger/orespawn/GenericDungeon.java`,
method `makeUrchinSpawner` (GD:2578-2636), plus `OreSpawnWorld.addUrchinSpawner` (OSW:1648-1666).
Coordinates below are relative to build origin `(X0, Y0, Z0)` = the three int args of
`makeUrchinSpawner(World, int cposx, int cposy, int cposz)` (GD:2578). From the worldgen caller the
origin is the **air block directly above a CrystalGrass block** (OSW:1659) — Y0 is air, Y0−1 is grass.

Citation convention: `GD:NN` = GenericDungeon.java, `OSW:NN` = OreSpawnWorld.java,
`OSM:NN` = OreSpawnMain.java, `CS:NN` = port `src/main/java/danger/orespawn/world/CrystalStructures.java`.

---

## 1. Entry points — EVERY call site

Grep of the full original tree for `makeUrchinSpawner`: exactly ONE call site (OSW:1660).
**`DungeonSpawnerBlock.java` has NO urchin-spawner case** — this structure has **no DSB type number**
(the `nextInt(50)` table DSB:52-202 never reaches it). Nothing to add in
`RandomDungeonSpawnerBlockEntity.buildForType`.

### 1a. Worldgen dispatch (OSW:187-202, Crystal dimension `DimensionID5`)

```
if (!addFairyTree(...)) {                                   // OSW:188
    addCrystalTermites(...);                                // OSW:189
    if (recently_placed == 0) {                             // OSW:190
        if (!(addRotatorStation(...) || addUrchinSpawner(...)   // OSW:191 — SHORT-CIRCUIT chain
              || addCrystalHauntedHouse(...) || addRoundRotator(...))) {
            addCrystalBattleTower(...);                     // OSW:192
        }
        addIrukandji(...);                                  // OSW:194 — runs regardless of `placed`
    }
}
addCrystalChestsAndSpawners(...);                           // OSW:197 — always
if (world.rand.nextInt(4) == 1) addRocks(...);              // OSW:198-200 — always
```

`addUrchinSpawner` is only ATTEMPTED when the fairy-tree roll failed, the 50-chunk cooldown is idle,
AND `addRotatorStation` returned false. Its success suppresses the haunted house, round rotator, and
battle tower for that chunk. Port mirrors this chain exactly at CS:108-129 (cooldown per-generator
`AtomicInteger`, BUG-013 precedent, CS:89-95).

### 1b. `addUrchinSpawner(World, Random random, int chunkX, int chunkZ)` (OSW:1648-1666) — FULL return contract

1. `if (OreSpawnMain.UrchinEnable == 0) return false;` (OSW:1649-1651) — config kill-switch
   (`config.get(mobs, "UrchinEnable", 1)`, OSM:6391; also gates Utopia urchin spawn lists,
   BiomeGenUtopianPlains.java:228/447). **Returns BEFORE any RNG draw.**
2. `if (random.nextInt(180) != 0) return false;` (OSW:1652-1654) — 1/180 per-chunk gate.
3. Up to 3 attempts (OSW:1655): `posX = chunkX + random.nextInt(16)`, `posZ = chunkZ + random.nextInt(16)`
   (OSW:1656-1657); scan `posY` from **100 down to 51** (OSW:1658) for the FIRST (highest) Y where
   `isAirBlock(posX, posY, posZ) && getBlock(posX, posY-1, posZ) == OreSpawnMain.CrystalGrass`
   (OSW:1659).
4. On hit: `makeUrchinSpawner(world, posX, posY, posZ)`; `recently_placed = 50`; **return true**
   (OSW:1660-1662). No offset applied to the found position.
5. All 3 attempts dry → **return false** (OSW:1665). Unlike `addFairyTree` (WGEN-062), there is NO
   "true on failed scan" quirk — true means placed, false means not, nothing else happens on either path.

Note the RNG split: the caller's gate/positions use the populate `random` parameter; the BUILD uses
`world.field_73012_v` (world.rand) throughout (GD:2590-2594, 2632-2634). The port uses the single
decoration `RandomSource` for both — distribution-identical, seed-stability delta only (PARITY note).

---

## 2. Build geometry — `makeUrchinSpawner` (GD:2578-2636)

### 2a. Three crystal spikes (GD:2582-2611)

One loop `i = 0..2`, each drawing a leaning spike of square cross-section from the origin. Block per
spike (GD:2583-2589): `i=0` CrystalStone, `i=1` CrystalCrystal, `i=2` TigersEye.

Per-spike RNG draws, in order (all `world.rand`, GD:2590-2594):

| Draw | Formula | Range |
|---|---|---|
| `dx` | `nextFloat() − nextFloat()` | (−1.0, 1.0), triangular around 0 |
| `dz` | `nextFloat() − nextFloat()` | (−1.0, 1.0), triangular |
| `dy` | `0.5f + nextFloat()/2` | [0.5, 1.0) — always upward |
| `width` | `nextInt(2)` | 0 or 1 → cross-section 1×1 or 2×2 |
| `length` | `10 + width*3 + nextInt(5)`; **`length /= 2` when `i != 0`** (GD:2594-2597) | i=0: 10-17; i=1,2: 5-8 (integer division) |

Spike walk (GD:2598-2610): float cursor `(rx, ry, rz)` starts at `(X0, Y0, Z0)` — the FIRST layer is
written AT the origin air block. For `iy = 0..length` **inclusive**, write the full
`(width+1)×(width+1)` slab: for `ix = 0..width`, `iz = 0..width`:
`setBlockFast((int)(rx+ix), (int)ry, (int)(rz+iz), bid, 0, 2)` (GD:2604), then
`ry += dy; rx += dx; rz += dz` (GD:2607-2609). All three spikes share the same origin.

Port: CS:537-558 — identical draw order, identical formulas, `<=` loops, `(int)` casts,
`safeSetBlock(..., state)` = `level.setBlock(pos, state, 2)` with a Y build-height clamp (CS:953-958).

### 2b. Spawner column, base clear, chest (GD:2612-2635) — written AFTER the spikes, overwriting them

| # | What | Where | Detail | Cite |
|---|---|---|---|---|
| 1 | Spawner | `(X0, Y0+1, Z0)` | `mob_spawner` flag 2, `func_98272_a("Crystal Urchin")` | GD:2612-2616 |
| 2 | Spawner | `(X0, Y0+2, Z0)` | same | GD:2617-2621 |
| 3 | Spawner | `(X0, Y0+3, Z0)` | same | GD:2622-2626 |
| 4 | Base clear | `(X0, Y0, Z0)` | air, flag 2 — removes the spikes' shared first slab at the column | GD:2627 |
| 5 | Chest | `(X0, Y0−1, Z0)` | chest flag 2, then meta 2 (facing north/−Z) via `func_72921_c(..., 2, 3)` — **replaces the anchor CrystalGrass block** | GD:2628-2629 |

Chest fill (GD:2630-2635) — **direct fixed-slot `setItem`, NOT a `WeightedRandomChestContent` list**
(no loot field in GD:18-95 belongs to this structure; grep confirms `makeUrchinSpawner` touches no
`ContentsList`). Slot 0 stays empty:

| Slot | Item | Count | Cite |
|---|---|---|---|
| 1 | `OreSpawnMain.UrchinEgg` | `1 + world.rand.nextInt(5)` = 1-5 | GD:2632 |
| 2 | `OreSpawnMain.CrystalCoal` (block item) | `4 + world.rand.nextInt(16)` = 4-19 | GD:2633 |
| 3 | `OreSpawnMain.CrystalCoal` | `4 + world.rand.nextInt(16)` = 4-19 | GD:2634 |

Port: CS:560-573 — same order (3 spawners, air, chest, 3 setItem calls), `Blocks.CHEST.defaultBlockState()`
(default `FACING=NORTH` ≡ 1.7.10 meta 2), `RandomizableContainerBlockEntity.setItem(1..3)`.
**No loot-table JSON exists or is needed** — pattern-doc §Step 5 applies to weighted lists only; the
original's fixed slots are already exact in code.

---

## 3. Loot mapping table

| 1.7.10 item | Registration | Port mapping | Cite |
|---|---|---|---|
| `UrchinEgg` | `ItemSpawnEgg(BaseItemID+379, 312)` "eggurchin" (OSM:5591), "Spawn Crystal Urchin" (OSM:5309) | `ModItems.URCHIN_SPAWN_EGG` → `SpawnEggItem(ModEntities.URCHIN)` | port ModItems.java:962-963; used CS:570 |
| `CrystalCoal` | `OreCrystal(BaseBlockID+201, 0.6f, 6.0f, 20.0f)` "crystalcoal" (OSM:1865) | `ModBlocks.CRYSTAL_COAL` (block item) | port ModBlocks.java:86-88; used CS:571-572 |

No weights to total (fixed slots, §2b).

## 4. Spawner / mob mapping

| Spawner name | 1.7.10 registration | Port EntityType | Placement |
|---|---|---|---|
| `"Crystal Urchin"` | `EntityRegistry.registerGlobalEntityID(Urchin.class, "Crystal Urchin", UrchinID)` (OSM:4237), mod-entity OSM:4241 | `ModEntities.URCHIN` ("urchin", fire-immune, 0.5×0.5) (port ModEntities.java:157-160) | 3 stacked spawners `(X0, Y0+1..3, Z0)` via `placeSpawner` → `SpawnerBlockEntity.setEntityId` (CS:941-946, 560-562) |

No direct entity spawns.

## 5. Block palette

| 1.7.10 field | Original decl | Port block | Used for | Cites |
|---|---|---|---|---|
| `OreSpawnMain.CrystalStone` | `OreBasicStone(+200, 2.0f, 10.0f)` OSM:1864 | `ModBlocks.CRYSTAL_STONE` (ModBlocks.java:82-83) | spike i=0 | GD:2583 / CS:532 |
| `OreSpawnMain.CrystalCrystal` | `OreCrystalCrystal(+209, 0.4f, 12.0f, 40.0f)` OSM:1867 — volatile (1-in-10 explode on break) | `ModBlocks.CRYSTAL_CRYSTAL` (ModBlocks.java:91-94) | spike i=1 | GD:2585 / CS:533 |
| `OreSpawnMain.TigersEye` | `OreCrystalCrystal(+217, 0.5f, 15.0f, 60.0f)` OSM:1868 | `ModBlocks.TIGERS_EYE_ORE` (ModBlocks.java:95-98) | spike i=2 | GD:2588 / CS:534 |
| `Blocks.field_150474_ac` | vanilla | `minecraft:spawner` | 3 urchin spawners | GD:2612/2617/2622 |
| `Blocks.field_150350_a` | vanilla | `minecraft:air` | base clear | GD:2627 |
| `Blocks.field_150486_ae` | vanilla | `minecraft:chest` (facing north) | loot chest | GD:2628-2629 |
| `OreSpawnMain.CrystalGrass` | `CrystalGrass(+202, 0.6f, 2.0f)` OSM:1866 | `ModBlocks.CRYSTAL_GRASS` (ModBlocks.java:89-90) | placement anchor probe only (never written) | OSW:1659 / CS:518 |

Port `CrystalGrass extends TransparentBlock` with no blockstate properties, so CS:518's
`.equals(grassState)` state compare ≡ the original's block-identity compare.

## 6. Footprint extents (theoretical bounds, per pattern-doc step 2 "unbounded walk" rule)

Worst case: spike i=0 with `width=1`, `length=17`, `|dx|,|dz| → 1.0`, `dy → 1.0`:

| Axis | Min | Max | Reasoning |
|---|---|---|---|
| X | `X0 − 17` | `X0 + 18` | drift `17·|dx| < 17` each way; `+ix` adds up to +1 (GD:2604-2608) |
| Y | `Y0 − 1` | `Y0 + 16` | chest at Y0−1 (GD:2628); top slab `(int)(Y0 + 17·dy) ≤ Y0+16` since `dy < 1.0` |
| Z | `Z0 − 17` | `Z0 + 18` | symmetric with X |

Typical extent is far smaller (E[dx]=0); the bound matters only for the write-window note in §11.4.

## 7. World-block READS mid-build

- **Build body: zero.** `makeUrchinSpawner` contains no `func_147439_a`/`func_147437_c` calls; the only
  reads are `getSpawnerTileEntity`/`getChestTileEntity` on blocks it just placed itself
  (GD:2613/2618/2623/2630) — subsumed by `placeSpawner` / the `getBlockEntity` after `setBlock`.
- **Placement scan** (OSW:1659) reads terrain — legal in the port because the builder runs in the
  DECORATION phase (`OreSpawnChunkGenerator.applyBiomeDecoration` → `CrystalStructures.generate`,
  OreSpawnChunkGenerator.java:245-263), where neighbor chunks exist and reads are permitted per
  structure_conversion_pattern.md §4 "Read legality". The §1-step-3 RNG stitching contract does NOT
  apply — there is no per-chunk replay in this regime.

## 8. Mechanism verdict / DungeonType / structure set

**No `DungeonType` entry, no structure JSON, no structure set. Salt 84353 is NOT consumed — return it
to the free pool.**

Rationale (pattern doc §0 + §4 dispatch-shape rule): the sole trigger is an `OreSpawnWorld.add*`
decoration call, and it sits inside a four-way short-circuit chain sharing one cooldown and a
fairy-tree gate (OSW:188-194). The `LegacyDungeonStructure` pipeline cannot express "try urchin only
if rotator station failed, and suppress battle tower on success" — converting this one structure to a
structure set would decouple it from the chain and change frequencies of FOUR sibling structures. The
port already implements the whole chain faithfully as a decoration-phase direct builder
(`CrystalStructures.generate` CS:97-138, approved shape per pattern-doc §4 table row 2 and the
existing rotator/haunted-house/battle-tower precedents). Keep it.

(Contingency only, NOT recommended: a pipeline conversion would need `NEEDS_NEW_MODE` — the scan
"3 tries of random-in-chunk column, Y 100→51 descending, air-above-CrystalGrass accept" (OSW:1655-1659)
matches none of SURFACE_CENTER / LOWEST_SURFACE_36 / ISLANDS_GRASS / END_SURFACE / OCEAN_SURFACE, and
CrystalGrass block probes cannot be predicted by `getBaseHeight`.)

Frequency bookkeeping (already encoded in port code, no JSON): 1/180 per chunk (OSW:1652) ×
chain position × 50-chunk cooldown, biome = crystal dimension (`DimensionStyle.CRYSTAL`,
OreSpawnChunkGenerator.java:254).

## 9. DungeonSpawnerBlock outcome

None. `makeUrchinSpawner` is absent from `DungeonSpawnerBlock.java` (grep of the full original tree,
§1). No `buildForType` case; no type number.

## 10. Verification diff (port vs original, number-by-number)

| Item | Orig | Port | Match |
|---|---|---|---|
| 1/180 gate | OSW:1652 | CS:511 | ✔ |
| 3 attempts, `nextInt(16)` x/z | OSW:1655-1657 | CS:513-515 | ✔ |
| Y scan 100→51, air + CrystalGrass | OSW:1658-1659 | CS:516-518 | ✔ |
| `recently_placed = 50` on success | OSW:1661 | hoisted to CS:124-125 (BUG-013) | ✔ (documented) |
| Spike blocks i=0/1/2 | GD:2583-2589 | CS:531-535 | ✔ |
| dx/dz/dy/width/length draws + order | GD:2590-2594 | CS:538-544 | ✔ |
| `length /= 2` for i≠0 | GD:2595-2597 | CS:545 | ✔ |
| Inclusive `<=` loops, `(int)` casts, flag 2 | GD:2601-2610 | CS:548-557 | ✔ (safeSetBlock adds Y clamp) |
| 3 spawners "Crystal Urchin" @ +1..+3 | GD:2612-2626 | CS:560-562 | ✔ |
| Air @ origin | GD:2627 | CS:563 | ✔ |
| Chest @ −1, facing north | GD:2628-2629 | CS:567-568 (default NORTH) | ✔ |
| Slots 1/2/3, counts 1+n(5), 4+n(16)×2 | GD:2632-2634 | CS:570-572 | ✔ |
| `UrchinEnable == 0` early-out | OSW:1649-1651 | **ABSENT** | ✘ — §11.1 |

## 11. Surprises / MISSING-IN-PORT

1. **MISSING-IN-PORT — `UrchinEnable` config gate.** Orig OSW:1649-1651 returns false (before the
   1/180 roll) when `UrchinEnable == 0`. The port defines the flag (`OreSpawnConfig.URCHIN_ENABLE`,
   OreSpawnConfig.java:35/168, default true) and uses it for natural spawns
   (ModSpawnControl.java:83) but `tryPlaceUrchinSpawner` (CS:508-526) never checks it — with
   urchins disabled the port still generates urchin spawner structures (3 live spawners + spawn-egg
   loot). Fix: add `if (!OreSpawnConfig.URCHIN_ENABLE.get()) return false;` before CS:511.
   Note the orig early-out also skipped the 1/180 draw — irrelevant to the port's independent
   RandomSource, but keep the check FIRST to match the contract shape.
2. **Spikes are drawn first, then vandalized** — all three spikes start at the same origin block;
   the spawner column (Y0+1..3), base air, and chest overwrite whatever spike slabs landed in that
   column (GD write order §2b). Faithful; do not "protect" the spikes or reorder.
3. **The chest replaces the CrystalGrass anchor block** (GD:2628 at Y0−1 = the grass that satisfied
   the placement probe). Faithful.
4. **Write-window edge (documented deviation, accept):** the theoretical ±17 spike drift (§6) from an
   origin near the chunk edge can exceed the decoration write region by 1-2 blocks in the extreme
   tail; modern `WorldGenRegion` drops such writes with a log line, whereas 1.7.10 populate wrote
   into (and could cascade-generate) neighbors. Astronomically rare, cosmetic (a truncated spike
   tip), inherited by every CrystalStructures builder — no action.
5. **RNG source split in the original** (§1b): caller gates on the populate `Random`, build on
   `world.rand`. Port unifies on the decoration RandomSource — PARITY note, no distribution change.
6. **CrystalCrystal spike is live ordnance** — port `OreCrystalCrystal` keeps the original's 1-in-10
   explode-on-break (ModBlocks.java:91-94 comment); mining the middle spike near the spawners is a
   faithful hazard, not a bug report.
7. **No DSB path and no weighted loot list** — both unusual for a GenericDungeon `make*` method;
   verified by grep (§1, §2b), not assumption.
8. **safeSetBlock Y-clamp** (CS:953-958) is a no-op here (Y ≤ ~117 by construction) — harmless
   shared helper behavior.
