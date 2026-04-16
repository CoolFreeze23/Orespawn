# OreSpawn 1.7.10 Reference (`reference_1_7_10_source/`)

This folder is a **read-only** 1:1 reference for the original full-feature
OreSpawn 1.7.10 jar (`orespawn-1.7.10-20.3`), decompiled with CFR 0.152.

> **Do not modify anything in this folder.** Always treat it as read-only. Add
> notes in `ORESPAWN_PORTING_AUDIT.md` (project root) instead.

## Role in the Dual-Reference Policy

Per the policy defined in `reference_1_12_2_source/INDEX.md`, this tree is the
**authoritative reference for everything the 1.12.2 ConquerantFix fork
stripped**. That is almost all of the mod's signature content:

- **Multi-part boss entities**: `TheKing` / `KingHead`, `TheQueen` / `QueenHead`, `Godzilla` / `GodzillaHead`, `ThePrince` / `ThePrinceTeen` / `ThePrinceAdult`, `ThePrincess`
- **Custom dimensions**: Crystal (`WorldProviderOreSpawn5`, `ChunkProviderOreSpawn5`), Utopia, Chaos, Village, Islands — see `WorldProviderOreSpawn[2-6]` and matching chunk providers
- **Structures**: `Trees` (Fairy Tree, Fairy Castle Tree, Giant Oak, Birch, Spruce), `BasiliskMaze`, `CrystalMaze`, `GenericDungeon`, `DungeonSpawnerBlock`, `RubyBirdDungeon`, `RTPBlock`, `Island`, `IslandToo`
- **Full 146-entity roster** (dinos, bots, mythical, fish, bosses)
- **GUI/TileEntity systems**: `CrystalWorkbench`, `CrystalFurnace`, `TileEntityCrystalFurnace`, `ContainerCrystalWorkbench`, `ContainerCrystalFurnace`, `CrystalWorkbenchGUI`, `CrystalFurnaceGUI`, `RenderInfo` / `RenderSpiderRobotInfo` / `RenderGiantRobotInfo` HUD overlays
- **Sidecar-style multi-part** logic: in 1.7.10 the "head" of a boss is **not** an `EntityDragonPart` but a standalone `EntityLiving` that teleports itself to the parent every tick (see `KingHead.func_70071_h_`). This is the pattern that `danger.orespawn.entity.OreSpawnPartEntity` replaces with a modern `PartEntity<T>` in 1.21.1.

For architecture/patterns (registries, AI goals, JSON loot tables, recipe
JSONs) prefer `reference_1_12_2_source/`.

## Contents

| Path                                   | Count | Notes                                                                 |
|----------------------------------------|------:|-----------------------------------------------------------------------|
| `sources/danger/orespawn/*.java`       |   586 | Decompiled 1.7.10 Java source (CFR 0.152, flat package, no subfolders) |
| `assets/**`                            |  1369 | Textures (`.png`), sounds (`.ogg`), language JSON                     |
| `_tools/cfr-0.152.jar`                 |     1 | Decompiler used (same copy as 1.12.2 reference)                       |
| `_tools/orespawn-1.7.10.jar`           |     1 | Repacked copy of the original contents (since the source was a bare zip) |
| `mcmod.info`                           |     1 | Original mod metadata                                                 |

### Notable legacy package layout

1.7.10's OreSpawn uses a **flat package** (`danger.orespawn.*`) with no
subpackages for entities, items, blocks, or world-gen. Names are typically
unprefixed (e.g. `Alien.java`, `TheKing.java`, `CrystalMaze.java`). When you
search for a concept, start with a case-insensitive glob on the class-name
guess rather than navigating subfolders.

## CFR Name-Mapping Cheatsheet (1.7.10 flavour)

The 1.7.10 jar was distributed against MCP-obfuscated Mojang names, so CFR
output is riddled with `func_*` / `field_*` identifiers. The common ones:

| CFR field / method         | Mojang (1.21.1)                  | Meaning                           |
|----------------------------|----------------------------------|-----------------------------------|
| `field_70165_t`            | `getX()`                         | entity X                          |
| `field_70163_u`            | `getY()`                         | entity Y                          |
| `field_70161_v`            | `getZ()`                         | entity Z                          |
| `field_70177_z`            | `getYRot()`                      | yaw                               |
| `field_70759_as`           | `yHeadRot`                       | head yaw                          |
| `field_70170_p`            | `level()`                        | owning Level                      |
| `field_72995_K`            | `isClientSide`                   | level side flag                   |
| `field_70121_D`            | `getBoundingBox()`               | entity AABB                       |
| `field_70159_w/x/y`        | `getDeltaMovement().{x,y,z}`     | motion vector                     |
| `field_70145_X`            | `noPhysics`                      | disable collision                 |
| `field_70160_al`           | `hasImpulse` / `horizontalCollision` | (varies; read context)          |
| `field_70178_ae`           | `fireImmune()` / fire-immune flag | legacy "immune to fire"           |
| `field_70153_n`            | `getVehicle()`                   | what this entity is riding on     |
| `field_70174_ab`           | `invulnerableTime` / `hurtTime`  | iframes                           |
| `field_70128_L`            | `isRemoved()`                    | removed flag                      |
| `func_70071_h_()`          | `tick()`                         | per-tick hook                     |
| `func_70088_a()`           | `defineSynchedData(Builder)`     | data-watcher registration         |
| `func_110147_ax()`         | `createAttributes()` / `registerGoals()` | init attributes (MC1.7 had no goal selector split) |
| `func_70097_a(src, amt)`   | `hurt(DamageSource, float)`      | take damage                       |
| `func_70105_a(w, h)`       | `setSize(w, h)` / `EntityDimensions.scalable` | AABB dimensions         |
| `func_70107_b(x, y, z)`    | `setPos(x, y, z)`                | teleport                          |
| `func_70101_b(yaw, pitch)` | `setRot(yaw, pitch)`             | set rotation                      |
| `func_70606_j(hp)`         | `setHealth(hp)`                  | set HP                            |
| `func_110143_aJ()`         | `getHealth()`                    | get HP                            |
| `func_110148_a(attr)`      | `getAttribute(attr)`             | attribute accessor                |
| `func_111128_a(value)`     | `setBaseValue(value)`            | attribute value                   |
| `func_72872_a(cls, bb)`    | `getEntitiesOfClass(cls, bb)`    | AABB entity search                |
| `func_72314_b(x, y, z)`    | `inflate(x, y, z)`               | expand AABB                       |
| `func_76138_g(f)`          | `Mth.wrapDegrees(f)`             | angle normalisation               |
| `func_76355_l()`           | `getMsgId()` (DamageSource)      | e.g. `"inWall"`, `"cactus"`       |
| `func_76346_g()`           | `getEntity()` (DamageSource)     | who caused the damage             |
| `func_76364_f()`           | `getDirectEntity()` (DamageSource) | direct damaging entity          |
| `func_70106_y()`           | `discard()`                      | delete entity (no drops)          |
| `func_70015_d(ticks)`      | `setRemainingFireTicks(ticks)` / `clearFire()` | set on fire / reset   |
| `field_72307_f`            | `hitVec` / `getLocation()`       | RayTraceResult hit point          |

## Re-decompiling

If you need a fresh decompilation (e.g. the reference was deleted or you want
to re-run with different CFR flags):

```powershell
# From workspace root
java -jar reference_1_7_10_source\_tools\cfr-0.152.jar `
     reference_1_7_10_source\_tools\orespawn-1.7.10.jar `
     --outputdir reference_1_7_10_source\sources `
     --silent true --comments false
```

The zip shipped without a jar wrapper, so the original file layout (bare
`danger/`, `assets/`, `META-INF/`, `mcmod.info` at the zip root) was
repackaged into `_tools/orespawn-1.7.10.jar` for CFR's consumption.

## When NOT to use this reference

- When 1.12.2 has a cleaner equivalent (see the dual-reference policy)
- For **JSON-based** loot tables, recipes, advancements — 1.7.10 predates data-driven Forge entirely; use 1.12.2 (`assets/data/orespawn/loot_tables/generic_dungeon.json`) or write new JSON from scratch
- For anything involving `RegistryEvent.Register<T>` — that's 1.12.2+ only
- For anything involving `CapabilityManager` / `Capability<T>` — use 1.12.2

## Key files for the in-progress port

| Subsystem                | Start here                                                          |
|--------------------------|---------------------------------------------------------------------|
| Multi-part bosses        | `TheKing.java`, `KingHead.java`, `TheQueen.java`, `QueenHead.java`, `Godzilla.java`, `GodzillaHead.java` |
| Crystal dimension        | `WorldProviderOreSpawn5.java`, `ChunkProviderOreSpawn5.java`, `CrystalMaze.java`, `BiomeGenUtopianPlains.java`, `Trees.java`, `CrystalGrass.java`, `CrystalWood.java`, `BlockCrystal*.java` |
| Utopia dimension         | `WorldProviderOreSpawn4.java`, `ChunkProviderOreSpawn4.java`, `BiomeGenUtopianPlains.java` |
| Chaos dimension          | `WorldProviderOreSpawn3.java`, `ChunkProviderOreSpawn3.java`        |
| Island dimension         | `WorldProviderOreSpawn6.java`, `ChunkProviderOreSpawn6.java`, `Island.java`, `IslandToo.java`, `IslandBlock.java` |
| Mining dimension         | `WorldProviderOreSpawn2.java`, `ChunkProviderOreSpawn2.java`, `ChunkOreGenerator.java` |
| Custom dungeons          | `GenericDungeon.java`, `BasiliskMaze.java`, `CrystalMaze.java`, `DungeonSpawnerBlock.java`, `RubyBirdDungeon.java` |
| Sidecar "part" pattern   | `KingHead.java`, `QueenHead.java`, `GodzillaHead.java` — see the `func_70071_h_` per-tick teleport block |
| Riding / controls        | `RiderControl.java`, `RiderControlMessage.java`, `RiderControlMessageHandler.java` |
| Fairy taming             | `Fairy.java` (see `myowner` UUID string field) |
| GUI framework            | `CrystalWorkbenchGUI.java`, `CrystalFurnaceGUI.java`, `GirlfriendOverlayGui.java`, `ContainerCrystalWorkbench.java`, `ContainerCrystalFurnace.java` |
