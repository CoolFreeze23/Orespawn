# OreSpawn for NeoForge 1.21.1 — 2.0.0-beta.6

Version `1.21.1-2.0.0-beta.6` (`gradle.properties`; `README.md` line 6), a hotfix on beta.5 cut on 2026-09-20 for the three reports filed on GitHub against beta.3. Release: https://github.com/CoolFreeze23/Orespawn/releases/tag/v1.21.1-2.0.0-beta.6

## Part one — for the player

A hotfix for the three reports filed against beta.3: Utopia's trees, Utopia's frogs, and the hitbox report.

**Fixed**
- Utopia's Sky trees and Wind trees no longer lose their branches at chunk borders. The Sky trees stand at their original height again and every Wind tree leans east, as in 1.7.10.
- Utopia's giant square-trunked and round-trunked trees are back, with their spiral steps, platforms, chests and Iron Golems. The platform tree's branches are leaf-rimmed discs again instead of thin lines.
- Frogs no longer spawn on dry land all over Utopia. A frog from a water list needs two-deep water, as in 1.7.10; in rivers and swamps they spawn in the water and on the banks.

**Good to know**
- The tree and frog fixes apply to newly generated chunks. Chunks you already have keep their shape.
- The hitboxes in the report (Ender Knight, Hercules Beetle, Jumpy Bug, Hammerhead, Vortex) were already fixed in beta.5. The Basilisk's box was right all along.

**Install:** put `orespawn-1.21.1-2.0.0-beta.6.jar` in `mods/` (NeoForge 21.1, Minecraft 1.21.1, GeckoLib 4.7 or newer) and take the beta.5 jar out. Worlds carry over.

### In more detail

#### What beta.6 is

beta.6 is beta.5 plus the fixes for the three player reports: Utopia's trees generate whole and complete again, Utopia's frogs stay in the water, and the hitbox report is answered. Nothing else moves. The worldgen fixes take effect in newly generated chunks; chunks generated before keep what they have.

#### What changed

- **Utopia's trees are whole again.** The Sky trees and the Wind trees generated with most of their branches missing: a
  tree that reaches past the next chunk had its outer blocks dropped by the game's chunk writer. They now generate as
  structures, written chunk by chunk, so every branch lands. With that the Sky trees stand at their 1.7.10 height again
  (their tops at the same level across the dimension, not a fixed height above the ground) and every Wind tree leans
  east, as in 1.7.10. *(WGEN-072)*
- **The big trees are back.** Utopia's giant square-trunked and round-trunked trees — the hollow towers with spiral
  steps, platforms, chests and Iron Golems on the branches — never generated in the port; only the rarer platform tree
  did, and that one's branches were lines where the original's are leaf-rimmed discs. All three generate now at the
  original's rates: one chunk in fifty rolls a big tree, one in four of those carries chests and golems, with the
  original's chest list. *(WGEN-073, WGEN-074)*
- **Frogs no longer flood Utopia.** The port let the every-tick water-creature spawn pass put frogs on dry land, and
  frogs never counted against that pass's cap. Frogs drawn from a water list now need two-deep water, as in 1.7.10; in
  rivers and swamps they spawn in the water and, as 1.7.10 also listed them there as land creatures, on the banks.
  *(ENT-S-170)*
- **Hitboxes (issue #3):** the Ender Knight, Hercules Beetle, Jumpy Bug, Hammerhead and Vortex boxes reported against
  beta.3 were restored to the originals in beta.5 (see that section); the Basilisk's was already right.

#### How to install

Put `orespawn-1.21.1-2.0.0-beta.6.jar` into the `mods` folder of a NeoForge 21.1 instance for Minecraft 1.21.1 together with GeckoLib 4.7 or newer, and take the beta.5 jar out; MultiHitboxLib and Databuddy are bundled in the jar. Existing worlds carry over.

## Part two — for the modder

### The Utopia trees, on the structure pipeline

1.7.10 planted its Utopia trees from the chunk populator, which could write into any loaded chunk: `OreSpawnWorld.addOtherTrees` (:2508-2547) rolled one chunk in thirty for a grove of up to four Wind trees or three Sky trees, and `addHugeTree` (:1830-1880) one chunk in fifty for one big tree — square (24%), circular (60%), round (15%) or royal (1%). A 1.21.1 feature writes through `WorldGenRegion`, whose `ensureCanWrite` drops every write farther than one chunk from the chunk being decorated; the feature ports of the Sky, Wind and round trees therefore lost every branch past that boundary (the failure BUG-021 records for the Crystal castle trees), and the square and circular trees had never been ported into worldgen at all. beta.6 moves all five onto the structure pipeline the royal trees already use: `UtopiaTreeStructure` (two structure JSONs, `utopia_tree_grove` and `utopia_huge_tree`, placed by `random_spread` sets with spacing 1 and separation 0, so every Utopia chunk makes the original's per-chunk roll in the original's draw order, LessLag gates included) and `UtopiaTreePiece` (one piece per tree; the five generators transcribed line for line from `Trees.java` and `ItemMagicApple.java` with the source line cited at every step, and reviewed against the source before the cut). A piece's `postProcess` runs the whole tree on every chunk it spans and lands only that chunk's cells, so the slices stitch; the tree's own draws come from a per-piece seed and never depend on a terrain read. The big trees' chests carry the magic apple's forty-entry list as three loot tables (`chests/utopia_huge_tree_branch`, `_floor`, `_disc`) and their critters are Iron Golems, as `spawnCreature(world, 99, ...)` made them. The round and circular trees' height counters follow the class file (the counter advances once per ring before the radius draw), not the decompiler's rendering of that line. The Sky tree's top is the original's absolute Y again (190-204) and every Wind tree leans +x, as `addOtherTrees` passed `dir = 0`. Records: WGEN-072, WGEN-073, WGEN-074. Pins: `UtopiaTreeTests` — the Sky and Wind geometry cell by cell against Trees.java, one pass equal to two chunk slices, both structures and their every-chunk sets registered, the round tree's first ring at the original's float rounding, the square tree's wall, steps, leaves and two-emerald apex, the circular tree's ring at the original's `(int)(v + 0.5)` rounding, chests and golems on the critter trees.

### The Frog's placement

1.7.10 placed a natural spawn by the creature type of the list it was drawn from (`SpawnerAnimals.canCreatureTypeSpawnAtLocation`): a water-creature entry needed two-deep liquid, an ambient or creature entry solid ground. 1.21.1 keys the placement by entity type, and the port had registered the Frog on the ground, so Utopia's water-creature list (w5 4-6, orig `BiomeGenUtopianPlains.java:135`) put frogs on land every tick with no cap, frogs counting as land creatures. `FrogSpawnPlacement`, the Frog's `SpawnPlacementType` now, reads the biome's lists at the position: the water rule where a water list carries the Frog, the ground rule where an ambient or creature list does, either where both do. The overworld entries (rivers and swamps water w20, rivers and jungles ambient w3, swamps ambient w2, orig `OreSpawnMain.java:4963-4967`) sit in those lists through `orespawn:add_spawns_in_category` (`ModBiomeModifiers`), a biome modifier that names its list, since NeoForge's `add_spawns` files an entry under the entity's own category. The invented grass-or-light predicate is gone for the Frog; its own rules (orig `Frog.java:240-251`: Y at least 50, daytime, the Crystal 1-in-20, at most five buddies) stand. Record: ENT-S-170. Pins: `FrogSpawnPlacementTests` — the registration, the shipped lists, the per-list rule on built cells.

### The hitbox report

Issue #3's six boxes: five were beta.3's uncited dimensions, restored to the 1.7.10 `setSize` in beta.5 (ENT-S-095 batch 1: Ender Knight 0.6 x 2.9, Hercules Beetle 3.25 x 2.75, the Jumpy Bug 3 x 3.5, Hammerhead 3 x 5; ENT-S-089: Vortex 2 x 4), with the renderer scales of ENT-S-092; the Basilisk's 1.6 x 3.5 matches `Basilisk.java:49` in both builds, its long body visual-only in 1.7.10 too.

### The harness

Checks on the release tree: drift 0; `RESULT: 0 error(s), 0 advisory(ies), 4 acknowledged; draw order: 104 shipped geo: 103 seam + 1 outside-seam`, `G1 PARITY PASS` 2, 13 and 101 models, checked-in proof verified, `BUILD SUCCESSFUL in 9m 18s`; the gametest suite: `All 1304 required tests passed`, `BUILD SUCCESSFUL in 4m 1s` — 1293 tests before this cut plus the eleven pinning the trees and the Frog.

### Third-party notices and credits

Unchanged from beta.5: see `phase_g_reports/RELEASE_NOTES_2.0.0-beta.5.md`, "Third-party notices" and "Credits" (MultiHitboxLib under LGPL-3.0, the MoreHitboxes portions under MIT, GeckoLib under MIT, Databuddy under MIT; the original mod by TheyCallMeDanger and the OreSpawn authors, 2013-2015).
