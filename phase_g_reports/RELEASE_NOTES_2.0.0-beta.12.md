# OreSpawn for NeoForge 1.21.1 — 2.0.0-beta.12

Version `1.21.1-2.0.0-beta.12` (`gradle.properties`; `README.md` line 6), cut on 2026-09-28 on beta.11 for worldgen fixes from player reports. Release: https://github.com/CoolFreeze23/Orespawn/releases/tag/v1.21.1-2.0.0-beta.12

## Part one — for the player

Fixes to how OreSpawn builds its world, from your reports. Only newly generated land changes.

**Fixed**
- King and Queen trees no longer have other trees growing up through their trunks. ([#4](https://github.com/CoolFreeze23/Orespawn/issues/4))
- Fences, iron bars and glass panes in OreSpawn's buildings connect to each other again, instead of standing as loose posts. ([#5](https://github.com/CoolFreeze23/Orespawn/issues/5))
- Structures spawn as often as in the original mod. Most were spawning too often, some up to four times too often. ([#5](https://github.com/CoolFreeze23/Orespawn/issues/5))
- Structures are spread out like in the original. No more clumps, and the King's and Queen's towers no longer come in pairs. ([#5](https://github.com/CoolFreeze23/Orespawn/issues/5))
- King and Queen trees are a bit more common again and their altars a bit rarer, both as in the original.
- The Islands' ruby dungeon spawns again.
- A few structures sat in odd spots or a block too high. They now sit where the original put them.
- Villages in the Village dimension spawn in exactly the same places as in the original, for the same world seed.
- Bee hives and mantis hives in the overworld only spawn in forests and jungles, like the original. The `DisableOverworldDungeons` setting now turns them off too.

**Before and after**

![Before and after: a King tree](https://raw.githubusercontent.com/CoolFreeze23/Orespawn/v1.21.1-2.0.0-beta.12/phase_g_reports/release_media/2.0.0-beta.12/king_tree_before_after.jpg)

*A King tree in the same world. Before: a tall tree grows straight up its trunk. After: the trunk stands clear.*

![Before and after: a tower's railings](https://raw.githubusercontent.com/CoolFreeze23/Orespawn/v1.21.1-2.0.0-beta.12/phase_g_reports/release_media/2.0.0-beta.12/tower_railings_before_after.jpg)

*A challenge tower's balcony. Before: every railing fence stands alone. After: the railings connect.*

![Before and after: a tower's iron bars](https://raw.githubusercontent.com/CoolFreeze23/Orespawn/v1.21.1-2.0.0-beta.12/phase_g_reports/release_media/2.0.0-beta.12/tower_bars_before_after.jpg)

*The same tower's cage. Before: the iron bars are thin separate posts. After: they join into walls.*

![Before and after: where the Islands' towers spawn](https://raw.githubusercontent.com/CoolFreeze23/Orespawn/v1.21.1-2.0.0-beta.12/phase_g_reports/release_media/2.0.0-beta.12/islands_towers_map.jpg)

*Where the Islands' big towers spawn, in the same world. Before: too many, often in pairs. After: fewer, one at a time, like the original.*

![Before and after: where the Mining dimension's dungeons spawn](https://raw.githubusercontent.com/CoolFreeze23/Orespawn/v1.21.1-2.0.0-beta.12/phase_g_reports/release_media/2.0.0-beta.12/mining_map.jpg)

*Where the Mining dimension's dungeons spawn, in the same world. Before: too many, in clusters. After: spread out like the original.*

**Good to know**
- Only newly generated land changes. Buildings and trees already in your world stay as they are, so explore new chunks to see the difference.
- A structure right at the edge of explored land can be cut off where it meets old chunks.
- Works with your existing worlds.

**Install:** put `orespawn-1.21.1-2.0.0-beta.12.jar` in `mods/` (NeoForge 21.1, Minecraft 1.21.1, GeckoLib 4.7 or newer) and take the beta.11 jar out.

### In more detail

#### What beta.12 is

beta.12 is beta.11 plus worldgen fixes for the two issues players filed against it: #4, other trees growing into the King trees, and #5, fences, bars and panes that do not connect, with the aside that the towers came more often than in 1.7.10. That aside led to checking every dimension's structures against the original: how often each one comes, where it stands, and how they spread out. All of it applies to land generated from this version on.

#### What changed

**The King and Queen trees.** The original grew Utopia's extra trees one chunk at a time, in a fixed order, and a chunk with a big tree got nothing else. The port grew them all separately, so tall trees could sprout right at a King tree's trunk. It now follows the original's order, and the big trees are placed last, so they win where trees meet.

**The fences, bars and panes.** In Minecraft 1.21 a fence or pane stores which neighbours it connects to. The port's buildings placed them without working that out, so they stood as loose posts. They now connect as the chunk finishes generating.

**How often structures spawn.** After building a structure, the original skipped its structure rolls for the next 49 chunks. The port had left that pause out, so most structures spawned too often, some far too often. They now spawn at the original's rate, and the Islands' missing ruby dungeon is back.

**How structures spread out.** That same pause kept structures from spawning right next to each other, while two of the same kind could still end up close. The port had given each structure its own placement grid, which made clumps, rows and paired towers. Each dimension's structures now share one roll, like the original.

**Where structures sit.** Eight structures sat at the middle of their chunk instead of the spot the original searched for, some a block too high. They now search as the original did. The Village's villages use the original's own placement and land in exactly its chunks.

#### How to install

Put `orespawn-1.21.1-2.0.0-beta.12.jar` into the `mods` folder of a NeoForge 21.1 instance for Minecraft 1.21.1 together with GeckoLib 4.7 or newer, and take the beta.11 jar out; MultiHitboxLib and Databuddy are bundled in the jar. Existing worlds carry over; the fixes apply to chunks generated from now on.

## Part two — for the modder

### Utopia's chunk pass (WGEN-075, WGEN-077)

orig OreSpawnWorld.java:42-46, the Utopia pass of the `IWorldGenerator` (it runs after the chunk provider's decoration):

```java
if (!this.addHugeTree(world, random, chunkX * 16, chunkZ * 16, chunk)) {
    if (!this.addAppleTrees(...) && !this.addOtherTrees(...) && recently_placed == 0) {
        this.addKingAltar(...);
    }
    this.addVeggies(...);
}
```

`addHugeTree` makes the royal trees (its 1% branch) and the big square, circular and round trees; `addAppleTrees` (:1792-1828) returns true when it made at least one tree, which skips `addOtherTrees`, the Wind and Sky tree grove (1 in 30, up to four Wind trees or three Sky trees rooted 3-12 blocks into the chunk); the King altar (`nextInt(2000) == 1`) rolls only when neither grew anything. All of it draws from the populator's one random. The port had the royal trees in the `royal_trees` set (spacing 96), the huge trees and the grove in their own spacing-1 sets, all three at `vegetal_decoration`, `MagicAppleTreeFeature` as a vegetal feature at rarity 21 and count 4 (the average of the original's odds), and the royal altars in their own set; nothing tied any of them to another. A King's trunk stands 4 to 11 blocks into its chunk (the huge roll's site), so a grove in the same chunk rooted within a few blocks of it; and since the structures of one step are placed in registry order, the grove came after the big trees and wrote its trunks and branches (unconditional writes: orig Trees.java:69 and :23 for the Wind tree, :106-108 and :81 for the Sky tree) through them.

- `UtopiaTreeStructure.chunkPass(seed, chunk, probe)` replays the pass on one `WorldgenRandom(LegacyRandomSource(0))` given `setLargeFeatureSeed(seed, x, z)`, the random the structure pass gives a structure's context for that chunk, so its first draws are exactly the huge and royal structures'. The huge roll first (`hugeRoll`, its royal branch included); when it grows a tree of any kind, nothing else of the pass grows. Otherwise the apple trees' draws follow on the same random, exactly as `addAppleTrees` makes them (`UtopiaTreeStructure.appleTrees`: `freq = (|cx| + |cz|) % 15`, the count `2 + nextInt(2 + (15 - freq) / 2)` and the leaves draw before the gate `nextInt(15 + freq)`, the LessLag cuts, trees at `2 + nextInt(12)` into the chunk on the grass the column probe finds in the window 50-100), the grove's only when the apple trees grew none, and the altar's only when the grove grew none too (`altarRoll`). Drawing the rolls one after another on one random also keeps them as independent as the original's consecutive draws; two rolls that each began on a fresh copy of the chunk's random would share their first draw, and the huge gate (`% 50`) would then pass in a fifth of the grove chunks (`% 30`), not a fiftieth.
- The grove structure takes its pieces from the pass. `MagicAppleTreeFeature` runs once per chunk (placement: biome only) and grows the pass's apple trees in Utopia, or the plain apple roll in the Village, which runs `addAppleTrees` on its own (:119); each tree goes on the real grass at the probe's height. The column probe reads the chunk generator's base column (`UtopiaTreeStructure.probe(generator, heights, randomState)`), so the feature's replay agrees with the structures chunk for chunk. The King and Queen trees are the huge roll's royal branch and the altars the pass's altar roll (see WGEN-080 below).
- `royal_tree_king`, `royal_tree_queen` and `utopia_huge_tree` generate at `top_layer_modification`, the step after `vegetal_decoration`, so they are placed after every vegetal feature and the grove: where a grove, an apple tree or a veggie patch from a neighbouring chunk meets a big tree, the big tree's blocks win (in 1.7.10 the chunk that populated last won). Among themselves the big trees keep registry order, as before: the King and the Queen are placed first, so a huge tree rooted in the next chunk overwrites them where the two meet (in 1.7.10, again, the chunk that populated last).

Declared: a tree rooted in a neighbouring chunk can still reach under a big tree's branches, as the report remembers from 1.7.10; the original's cherry and peach leaves (two draws in ten of the leaves roll) grow as apple leaves, since the port has no such blocks.

Six gametests, `UtopiaTreeTests`: w075a the three big trees' step and the grove's; w075b over 60,000 chunks of a flat probe, never two kinds of tree in a chunk, the huge pass equal to the huge structure's decision, and the apple and grove counts within five sigma of what independent consecutive draws give (the shared-first-draw design falls a sixth short); w075c the royal trees as the huge roll's royal branch (one chunk in 5,000 with grass in every column, King or Queen at even odds, nothing else of the pass in their chunks); w075d a royal start in a chunk refusing the magic apple tree and the veggie patch; w077a the apple roll (two to `3 + (15 - freq) / 2` trees at 2-13 into the chunk, none where the probe refuses, the gate one chunk in 15 at freq 0 and one in 29 at freq 14); w077b on a detached Utopia generator, the grove structure's answer equal to the pass chunk for chunk, and the King altar refused wherever the pass grows a tree.

Measured in a natural world (two copies of one test world, one generating with beta.11 and one with beta.12): the King tree in chunk (-21383, 3105), a King chunk in both versions whose beta.11 chunk also rolled a Wind grove, had 545 logs of other trees within 16 blocks of its trunk (from the ground to 60 up) on beta.11 and 0 on beta.12.

### Fences, panes, bars and walls join (WGEN-076)

The legacy structures (`LegacyDungeonPiece`) write with `UPDATE_CLIENTS | UPDATE_KNOWN_SHAPE`, no neighbour shape updates, which TF-021 relies on: a fragile plant settled against its neighbours can erase itself. So every fence, pane, bar and wall kept the unjoined default state it was placed with. 1.7.10 computed the joins from the neighbours when it rendered the block (`BlockFence.canConnectFenceTo`, `BlockPane.canPaneConnectTo`, `BlockWall.canConnectWallTo`) and stored nothing, so the original's structures showed them joined.

`LegacyDungeonPiece.place` now hands every `CrossCollisionBlock` (fences, glass panes, iron bars) and `WallBlock` it writes to the chunk's post-processing (`ChunkAccess.markPosForPostprocessing`): when the chunk is promoted to a live chunk, `LevelChunk.postProcessGeneration` runs `Block.updateFromNeighbourShapes` on it against the finished structure, as vanilla's `StructurePiece` does for its fences and bars. A structure built into a live world (`buildNow` on a `ServerLevel`, which the random dungeon spawner block uses) collects the same blocks and settles them, with their joining neighbours, once the build is done. Every other block keeps the suppressed updates. The stairs are left as placed: no legacy structure sets two side by side.

Three gametests, `StructureJoinTests`: w076a the Damsel in Distress jail's 28 iron bars joined east and west and not into the open cells, every joiner in the building settled; w076b the Mini Dungeon's cage and staircase railings settled, the first railing joined to the next step's planks; w076c only the fences, panes, bars and walls are settled, the plants and every other block keep the suppressed updates.

Measured in the same pair of worlds: the King's challenge tower in chunk (-1220, -130) of the Islands, a tower both versions build on the same blocks, has 2,219 fences, bars and panes; on beta.11 all 2,219 of them differ from what their neighbours make them, on beta.12 none.

### The rates (WGEN-078, WGEN-079)

The original's rolls ran behind one cooldown (orig OreSpawnWorld.java:30, 37-38): `recently_placed` is set to 50 by every builder that builds (the King altar sets 100), `generate` counts it down once per populated chunk, and the rolls behind it run only at 0. In a dimension whose builders build with probability p per chunk, the rolls therefore run in 1 / (1 + 49p) of the chunks. It held the Islands' roll (:134-176: `recently_placed == 0 && nextInt(100) == 0 && D4BigSpaceCheck`, then `nextInt(19)` over the builders: 0-2 `addD4Castle`, King or Queen at even odds at :2219, 3-6 the generic dungeon, 7-18 one structure each, the ruby dungeon among them), so an Islands build came one chunk in 149, not one in 100. It held the overworld's pass (:284-321: `world.rand.nextInt(6)` picks one of six builders, then the first-success chain of seven), Mining's rotation (:79-104: `nextInt(95) == 1`, then `nextInt(7)`) and the Village's three builders (:120-129). The End's pass (:215-228: `world.rand.nextInt(4)` picks one of four builders) neither checks nor sets it, Crystal's already runs the original's counter (`CrystalStructures`), and Chaos and the Nether have no structures.

The overworld's builders are tied to biomes, so each structure's cooldown is taken from the builders that share its biome: plains carries four (the frog pond, the haunted house, the leaf monster dungeon and the rubber ducky pond, one build in 89 chunks between them, so the roll runs in 64% of plains chunks), swamp two (the haunted house and the spit bug lair, 70%), forest, desert, snowy plains and taiga one each (82-85%), the ocean five at small odds (89%); the haunted house, in plains, taiga and swamp, takes a weighted 72%. Mining's and the Village's builders were measured on the port's own generators (400 chunks each): six of Mining's seven find their lowest-surface or lowest-grass site in 89% of chunks, the Leonopteryx nest its grass above Y80 in 34%, so the rotation runs in 70% of chunks; the Village's damsel, spider and red ant builders find theirs in 58%, 49% and 53%, so they run in 78%.

The original's space checks against real blocks also turned a build away where an earlier structure stood in the plane they tested: `D4BigSpaceCheck` (:2655-2664, a 65 × 55 plane at Y11 from the chunk corner's -25 to +39 and +29) and `quickSpaceCheck` (:2625-2633, a 12 × 12 plane four above each attempt's ground). Played out with the structures' footprints over 80 simulated worlds, they kept 95.5% of the Islands' builds, 98% of the End's and all of the Village's. The terrain side of those checks is the port's site search, as before.

The port had converted the nominal odds, one structure set each (a tower 1/1267 at 36/18, an Islands one-slot structure 1/1900 at 44/22 and the generic dungeon one chunk in 475 from the chunk generator, Mining's seven 26/13, and so on), so an Islands structure came 1.49 times as often as in the original, the overworld's plains structures up to 1.5 times, the swamp, desert and snowy ones up to 1.4, the ocean ones up to 1.15, Mining's seven 1.4 and the Village's three 1.25; the Leonopteryx nest, whose site it took at the chunk centre, about four times. The ruby dungeon (`addD4RubyDungeon`) had no Islands placement at all. The royal trees came one chunk in 9,216 where the original's 1-in-50 huge roll with its 1% royal branch gives one in 5,000, and the royal altars, placed without the pass's no-tree condition or the altar's site, one chunk in 2,025 where the original gives about one in 3,500.

| Dimension | Structure | 1.7.10 roll | 1.7.10: one in | beta.11 | beta.12 |
|---|---|---|---:|---|---|
| Overworld | `play_pool` | ocean: 1/6 pick × 1/350 (:1136-1154) | 2,361 | 46/23 (1 in 2,116) | `overworld_pool` 16/1, 1,084 of 10,000 (1 in 2,362) |
| Overworld | `water_dragon_lair` | ocean: 1/6 × 1/350 (:1358-1376) | 2,361 | 46/23 (1 in 2,116) | `overworld_pool` 16/1, 1,084 of 10,000 (1 in 2,362) |
| Overworld | `gold_fish_bowl` | ocean: 1/6 × 1/350 (:1176-1194) | 2,361 | 46/23 (1 in 2,116) | `overworld_pool` 16/1, 1,084 of 10,000 (1 in 2,362) |
| Overworld | `girlfriend_island` | ocean: 1/6 × 1/300 (:1378-1396) | 2,024 | 42/21 (1 in 1,764) | `overworld_pool` 16/1, 1,265 of 10,000 (1 in 2,024) |
| Overworld | `monster_island` | ocean: 1/6 × 1/300 (:1398-1416) | 2,024 | 42/21 (1 in 1,764) | `overworld_pool` 16/1, 1,265 of 10,000 (1 in 2,024) |
| Overworld | `frog_pond` | plains: 1/6 × 1/350 (:1156-1174) | 3,258 | 46/23 (1 in 2,116) | `overworld_pool` 16/1, 786 of 10,000 (1 in 3,257) |
| Overworld | `small_beehive` | forest/birch/jungle: 1/230, then hive or mantis 50/50 (:999-1021) | 558 | 21/10 (1 in 441) | `overworld_chain` 6/1, 645 of 10,000 (1 in 558) |
| Overworld | `mantis_nest` | the same roll's other half | 558 | 21/10 (1 in 441) | `overworld_chain` 6/1, 645 of 10,000 (1 in 558) |
| Overworld | `haunted_house` | plains/taiga/swamp: 1/285 (:979-997) | 397 | 17/8 (1 in 289) | `overworld_chain` 6/1, 907 of 10,000 (1 in 397) |
| Overworld | `leaf_monster_dungeon` | plains: 1/275 (:1196-1215) | 427 | 17/8 (1 in 289) | `overworld_chain` 6/1, 844 of 10,000 (1 in 427) |
| Overworld | `spit_bug_lair` | swamp: 1/190 (:1238-1257) | 272 | 14/7 (1 in 196) | `overworld_chain` 6/1, 1,325 of 10,000 (1 in 272) |
| Overworld | `igloo` | snowy plains: 1/220 (:1259-1278) | 269 | 15/7 (1 in 225) | `overworld_chain` 6/1, 1,338 of 10,000 (1 in 269) |
| Overworld | `bouncy_castle` | desert: 1/230 (:1280-1299) | 279 | 15/7 (1 in 225) | `overworld_chain` 6/1, 1,290 of 10,000 (1 in 279) |
| Overworld | `rubber_ducky_pond` | plains: 1/275 (:1217-1236) | 427 | 17/8 (1 in 289) | `overworld_chain` 6/1, 844 of 10,000 (1 in 427) |
| Mining | `basilisk_maze` | 1/95 × 1/7 (:79-101) | 944 | 26/13 (1 in 676) | `mining_structures` 9/0, 858 of 10,000 (1 in 944) |
| Mining | `kyuubi_dungeon` | 1/95 × 1/7 | 944 | 26/13 (1 in 676) | `mining_structures` 9/0, 858 of 10,000 (1 in 944) |
| Mining | `beehive` | 1/95 × 1/7 | 944 | 26/13 (1 in 676) | `mining_structures` 9/0, 858 of 10,000 (1 in 944) |
| Mining | `shadow_dungeon` | 1/95 × 1/7 | 944 | 26/13 (1 in 676) | `mining_structures` 9/0, 858 of 10,000 (1 in 944) |
| Mining | `wtf_alien_dungeon` | 1/95 × 1/7 | 944 | 26/13 (1 in 676) | `mining_structures` 9/0, 858 of 10,000 (1 in 944) |
| Mining | `ender_knight_dungeon_mining` | 1/95 × 1/7 | 944 | 26/13 (1 in 676) | `mining_structures` 9/0, 858 of 10,000 (1 in 944) |
| Mining | `leonopteryx_nest` | 1/95 × 1/7 | 944 | 26/13 (1 in 676) | `mining_structures` 9/0, 858 of 10,000 (1 in 944) |
| Village | `damsel_in_distress` | 1/250 (:1301-1317) | 321 | 16/8 (1 in 256) | `village_structures` 9/0, 2,520 of 10,000 (1 in 321) |
| Village | `spider_hangout` | 1/350 (:1319-1338) | 450 | 19/9 (1 in 361) | `village_structures` 9/0, 1,800 of 10,000 (1 in 450) |
| Village | `red_ant_hangout` | 1/250 (:1340-1356) | 321 | 16/8 (1 in 256) | `village_structures` 9/0, 2,520 of 10,000 (1 in 321) |
| End | `ender_knight_dungeon_end` | 1/4 pick × 1/25 (:1512-1525), 98% past the space check | 102 | 10/5 (1 in 100) | `end_structures` 2/0, 392 of 10,000 (1 in 102) |
| End | `graveyard` | 1/4 × 1/25 (:1527-1540), 98% | 102 | 10/5 (1 in 100) | `end_structures` 2/0, 392 of 10,000 (1 in 102) |
| End | `hospital` | 1/4 × 1/25 (:1542-1555), 98% | 102 | 10/5 (1 in 100) | `end_structures` 2/0, 392 of 10,000 (1 in 102) |
| End | `ender_castle_end` | 1/4 × 1/50 (:1557-1570), 98% | 204 | 14/7 (1 in 196) | `end_structures` 2/0, 196 of 10,000 (1 in 204) |
| Islands | `challenge_tower_king` | 1/100, 3 of 19 slots, King or Queen (:2203-2228), 95.5% past the space check | 1,976 | 36/18 (1 in 1,296) | `islands_structures` 9/1, 410 of 10,000 (1 in 1,976) |
| Islands | `challenge_tower_queen` | the same slots' other half | 1,976 | 36/18 (1 in 1,296) | `islands_structures` 9/1, 410 of 10,000 (1 in 1,976) |
| Islands | `islands_generic_dungeon` | 1/100, 4 of 19 slots (:2438-2452), 95.5% | 741 | the chunk generator, 1 in 475 | `islands_structures` 9/1, 1,093 of 10,000 (1 in 741) |
| Islands | `islands_ruby_dungeon` | 1/100, 1 of 19 slots (:2171-2185), 95.5% | 2,964 | none | `islands_structures` 9/1, 273 of 10,000 (1 in 2,967) |
| Islands | `ender_castle_islands` | 1/100, 1 of 19 slots, 95.5% (and each of the other ten one-slot structures) | 2,964 | 44/22 (1 in 1,936) | `islands_structures` 9/1, 273 of 10,000 (1 in 2,967) |
| Islands | `cloud_shark_dungeon` | 1/300, outside the roll, no cooldown (:179-181) | 300 | 17/8 (1 in 289) | `cloud_shark_dungeon` 1/0, 1 of 300 (1 in 300) |
| Utopia | `royal_tree_king`, `royal_tree_queen` | the huge roll 1/50, its royal branch 1/100, King or Queen 50/50 (:1830-1880) | 5,000 where the grass holds | `royal_trees` 96/48 (1 in 9,216) | `royal_trees`: the huge roll's own royal branch, every chunk |
| Utopia | `king_altar`, `queen_altar` | 1/2000 in a chunk whose pass grew no tree, King or Queen 50/50 (:2549-2571), the roll behind its own cooldown in 97% | about 2,060 before the site | `royal_altars` 45/22 (1 in 2,025) | `royal_altars`: the pass's own altar roll, 97% of chunks, every chunk asked |
| Village | `dim_village` | MapGenMoreVillages 9/7, salt 10387312 (MapGenMoreVillages.java:11-33) | 81 | 9/7, salt 10387399 | `dim_villages` 9/7, salt 10387312: the original's chunks |

Beside the rates:

- **The nests.** addANest builds a small bee hive or a mantis hive at even odds (:1010) in exactly "Forest", "ForestHills", "Birch Forest", "Birch Forest Hills", "Jungle" and "JungleHills" (:1004): both hives stand on the 1.21 forest, birch forest and jungle (the port's `#minecraft:is_forest` and `#minecraft:is_jungle` had added the flower forest, dark forest, grove, old growth birch forest, sparse jungle and bamboo jungle), and `DisableOverworldDungeons`, which gated the whole pass (:284), now covers them (`FeatureStructure`'s `overworld_dungeon`).
- **The villages** (`MapGenMoreVillages`: spacing 9, separation 7, `World.setRandomSeed(x, z, 10387312)`, the biome answer ignored) are the same algorithm as `random_spread`; with the original's salt, 10387312, the port's set picks exactly its chunks.
- **The sites.** Eight structures took a chunk-centre site where the original's builder scanned: the shadow and WTF alien dungeons (`LOWEST_GRASS_36`, :2143-2169 and :2059-2085, built at lowestY), the bee hive (`FeatureStructure`'s `lowest_grass_36` anchor, :2031-2057; its feature built one block above the original's lowestY + 3, now two above the surface), the Leonopteryx nest (`HIGHEST_GRASS_36`, :2115-2141), the Islands' greenhouse and White House (`ISLANDS_GRASS`, :2230-2251 and :2299-2320, built on the grass rather than a block above it), the royal altars (`UTOPIA_ALTAR`, :2549-2571: eight attempts at chunk + 3 + nextInt(10) for grass in Y51-100, built on the grass) and the royal trees (the huge roll's three attempts at chunk + 4 + nextInt(8) for grass in Y51-127, built on the grass). The nests take addANest's five attempts for grass under air (`grass_attempts_5`). The Islands' two box dungeons are structures now (`ISLANDS_GENERIC_DUNGEON`, `ISLANDS_RUBY_DUNGEON`) on the D4 grass scan with their builders' LessLag cuts (one in four and one in two).

Not changed: the ocean structures keep PN-019's reading of 1.7.10's exact "Ocean" as the 1.21 temperate ocean alone, not its warm, lukewarm and cold variants.

### One roll per dimension (WGEN-080)

The rates alone left the spread wrong. A structure set of one structure each spaced every kind on a grid of its own: no two of a kind ever closer than the set's separation, any two kinds as close as chance put them, and sets of one spacing on neighbouring salts lined up against each other cell after cell (the King's and Queen's towers stood in pairs about a hundred blocks apart; Mining's seven sets, on salts 84302-84374, stood in clusters in every cell). The original's pause after every build did the opposite: it kept any two structures of a dimension apart for a while, whatever their kind, while two of the same kind could still stand close.

Each dimension's roll is one structure set now, as the original's was one roll (`StructurePicks`):

| Set | Spacing / separation | Holds | Nothing |
|---|---|---|---:|
| `overworld_pool` | 16 / 1 | the six-way pick's six structures | 3,432 of 10,000 |
| `overworld_chain` | 6 / 1 | the chain's seven builders, the nests as their two hives | 2,162 of 10,000 |
| `mining_structures` | 9 / 0 | the rotation's seven | 3,994 of 10,000 |
| `village_structures` | 9 / 0 | the three builders | 3,160 of 10,000 |
| `end_structures` | 2 / 0 | the four builders | 8,628 of 10,000 |
| `islands_structures` | 9 / 1 | the roll's nineteen slots: the two towers, the generic and ruby dungeons, the eleven other structures | 4,811 of 10,000 |
| `cloud_shark_dungeon` | 1 / 0 | the cloud shark | 299 of 300 |

Each spot of a set picks one structure at a weight that gives it the original's odds, and `orespawn:nothing` (`NothingStructure`, no biome, never generates) at the rest, the share of spots the original's roll left empty. The spacings are the ones whose spread came closest to the original's in a simulation of its rolls, cooldown and space checks on the same ground, the chunks loaded as a player flying back and forth loads them (view distance 10). The pick is final, as the original's roll was: vanilla's `ChunkGenerator.createStructures` tries a set's other structures in turn when its first pick finds no site, which the original never did, so a mixin on `ChunkGenerator.tryGenerateStructure` (`StructurePickMixin`) passes over every structure of these sets but vanilla's first pick (`setLargeFeatureSeed(seed, x, z)`, then `nextInt` of the total weight). A picked structure that finds no site leaves the spot empty. Commands and structure searches are left alone; a search that stops at a spot where another structure was picked finds no start there when the chunk loads, and moves on.

Utopia's King and Queen trees are the huge roll's royal branch itself (`UtopiaTreeStructure.hugeRoll`: after the type roll's 0, the next draw gives the King on 0 and the Queen otherwise), and its altars the pass's altar roll (`altarRoll`), each asked in every chunk (`royal_trees` and `royal_altars`, spacing 1); the altar's cooldown of 100 lets the roll through in 97% of chunks (a salted draw of the chunk's own). Vanilla's `frequency` is not used anywhere: its roll seeds with the salt and the chunk's coordinates in shuffled order, the chunk's z entering at weight one, so with a spacing of 1 neighbouring chunks along z roll nearly the same number (98% of the End's passes would have come in streaks).

Against the original, over 40 worlds of the simulation (counts in the mapped square; the share of structures with another of any kind within four chunks; the share with one of their own kind within eight):

| Dimension | Structures: 1.7.10 / beta.11 / beta.12 | Another within 4 chunks | Same kind within 8 |
|---|---|---|---|
| Overworld | 362 / 483 / 364 | 8% / 25% / 9% | 31% / 8% / 35% |
| Mining | 223 / 363 / 220 | 10% / 78% / 10% | 11% / 0% / 10% |
| Village | 73 / 94 / 75 | 7% / 41% / 6% | 19% / 0% / 16% |
| The End | 312 / 320 / 315 | 79% / 92% / 79% | 79% / 38% / 79% |
| Utopia | 184 / 246 / 181 | 1% / 1% / 2% | 3% / 0% / 5% |
| Islands | 947 / 1,382 / 939 | 7% / 46% / 7% | 8% / 8% / 7% |

![1.7.10, beta.11 and beta.12: where each dimension's structures stand](https://raw.githubusercontent.com/CoolFreeze23/Orespawn/v1.21.1-2.0.0-beta.12/phase_g_reports/release_media/2.0.0-beta.12/dimensions_map.jpg)

*Where each dimension's structures stand for one world seed. Left, 1.7.10: the original's own rules played out in the simulation on the same ground. Middle, beta.11. Right, beta.12.*

Declared: the original's cooldown followed the order chunks populated in, so its spread depended on how the player explored; the spacings match the simulated player's. The space checks' refusals come in as a share of each set's odds, not as a refusal of the spots nearest an earlier structure. The End's and the Islands' builders' other space-check refusals (hills, pillars) are the port's site searches, as before.

Eight gametests, `StructurePlacementTests`: w080a every set's spacing, separation, structures in order and odds within half a percent of the original's; w080b the nests on the original's biomes at even odds in the chain; w079c the villages against the original's own placement test over 57,600 chunks; w080c the pass over a patch of chunks keeping to vanilla's first draw, written out, in every one-pick set; w080d vanilla's structure pass run on a detached Mining generator at 256 of the set's spots, each building its first pick where that structure finds its site and nothing otherwise; w079e the sites' modes; w079f on a detached Mining generator, the Leonopteryx nest's site against a replica of addLeonNest's scan and the bee hive on the same lowest grass as the ender knight dungeon; w080e on a detached Utopia generator, the King and Queen trees exactly where the huge roll, written out from the original, takes its royal branch, and the altars where the pass picks them. Two more, `IslandsRotationTests`: w078a the Islands' one set, its nineteen slots in the original's order and odds, the old sets gone; w078b the two box dungeons on the D4 scan's site and built with their spawners, ruby ore and chests.

### The pictures

The before and after pictures come from copies of one test world, one opened with beta.11 and one with beta.12, each generating the spot fresh from the same seed, with the camera at the same place and the time fixed at noon. The King tree is the one in chunk (-21383, 3105), a King chunk in both versions (found from the seed), whose beta.11 chunk also rolled a Wind grove; its trunk stands a few blocks apart in the two, beta.11 building at the chunk's centre and beta.12 at the huge roll's site. The tower is the King's challenge tower in chunk (-1220, -130), which both versions build on the same blocks. The Islands map is computed from the same seed with each version's placement; the Islands' plane builds in every chunk its sets pick. The dimensions map sets the original's own rules, played out in the simulation above, beside beta.11's and beta.12's placement for the same seed, all three on the same ground (in the overworld, strips of one biome each); the original's column is one simulated world, since its rolls came from a random that no seed replays.

### The harness

Checks on the release tree: drift 0; `RESULT: 0 error(s), 0 advisory(ies), 4 acknowledged; draw order: 104 shipped geo: 103 seam + 1 outside-seam`, `G1 PARITY PASS` 2, 13 and 101 models, checked-in proof verified, `BUILD SUCCESSFUL in 9m 38s`; the gametest suite: `All 1357 required tests passed`, `BUILD SUCCESSFUL in 5m 26s` — 1338 tests at beta.11 plus the nineteen pinning WGEN-075 to WGEN-080.

### Third-party notices and credits

Unchanged from beta.5: see `phase_g_reports/RELEASE_NOTES_2.0.0-beta.5.md`, "Third-party notices" and "Credits" (MultiHitboxLib under LGPL-3.0, the MoreHitboxes portions under MIT, GeckoLib under MIT, Databuddy under MIT; the original mod by TheyCallMeDanger and the OreSpawn authors, 2013-2015).
