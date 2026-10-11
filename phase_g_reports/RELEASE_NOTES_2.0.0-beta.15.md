# OreSpawn for NeoForge 1.21.1 — 2.0.0-beta.15

Version `1.21.1-2.0.0-beta.15` (`gradle.properties`; `README.md` line 6), on beta.14: the original's spawn lists, OreSpawn's ores and the dimensions' dungeons the same from one seed every time, the altars' /locate and new land fast again, the robots on their legs, Chaos without caves and red again by default, the flying Islands, the orchards wearing round, the Queen's drop and the Islands' structures apart (GitHub #9 and #8). Release: https://github.com/CoolFreeze23/Orespawn/releases/tag/v1.21.1-2.0.0-beta.15

## Part one — for the player

OreSpawn's creatures spawn from the original's lists again, one seed gives the same OreSpawn ores every time, the Queen drops her loot and the Islands' structures stand apart (GitHub #9 and #8), the flying Islands fly, and Chaos is red again by default.

<!-- pictures -->

### beta.15 in pictures

**One seed, the same OreSpawn ores.** Two worlds made from the same seed now get the same OreSpawn ores, in the overworld, the Nether and OreSpawn's dimensions. Before, 1.21's ore placement checked the ground's height before each vein, so the same chunk could come out with whole blobs of dirt, gravel and ore in one world and plain stone in the other. Minecraft's own overworld ores and trees can still differ between two worlds of one seed, as they do without OreSpawn.

![The same Utopia chunks generated twice by one build, before and after the fix: orange where the two runs disagree, none after](https://raw.githubusercontent.com/CoolFreeze23/Orespawn/v1.21.1-2.0.0-beta.15/phase_g_reports/release_media/2.0.0-beta.15/ore_before_after.png)

<details><summary>The numbers</summary>

| Runs (seed 1007, Utopia, x 0 to 319, z -32 to 47) | Build | Cells with a vein in one run and stone in the other | Columns |
|---|---|---|---|
| s1007ao / s1007ap | 298b096a (beta.14's ore placement) | 5,394 | 1,425 |
| s1007ux1 / s1007ux2 | 84d218c8 (the fix) | 0 | 0 |

</details>

![Chunks of 500 that came out differently in two runs of one seed, before and after the fix](https://raw.githubusercontent.com/CoolFreeze23/Orespawn/v1.21.1-2.0.0-beta.15/phase_g_reports/release_media/2.0.0-beta.15/graph_lost_decoration.png)

<details><summary>The numbers</summary>

| Runs | Build | Dimension | Chunks with a vein lost | Chunks different |
|---|---|---|---|---|
| s1007v/s1007x | 9206e566 | Utopia | 66 | 111 |
| s1007w/s1007y | 298b096a | Utopia | 68 | 106 |
| s1007ao/s1007ap | 298b096a | Utopia | 74 | 109 |
| s1007tr1/s1007tr2 | 298b096a | Utopia | 75 | 107 |
| s1007ux1/s1007ux2 | 84d218c8 | Utopia | 0 | 41 |
| s1007mx1/s1007mx2 | 84d218c8 | Mining | 0 | 47 |
| s1007my1/s1007my2 | 04070f73 | Mining | 0 | 47 |

</details>

**The Royal Altars' /locate.** About a second at most from anywhere (1.13 s the slowest), where beta.14 took up to seven and a half.

![/locate for the King's and Queen's altars in beta.13, beta.14 and beta.15 against the 5-second budget](https://raw.githubusercontent.com/CoolFreeze23/Orespawn/v1.21.1-2.0.0-beta.15/phase_g_reports/release_media/2.0.0-beta.15/graph_locate.png)

<details><summary>The numbers</summary>

| Version | Run | Altar | From x, z | ms | Answer |
|---|---|---|---|---|---|
| beta.13 | s1007i | king | 0, 0 | 1,075.7 | none in range |
| beta.13 | s1007i | queen | 0, 0 | 343.4 | found |
| beta.14 | v14l1 | king | 0, 0 | 3,044.4 | found |
| beta.14 | v14l1 | queen | 0, 0 | 4,676.4 | found |
| beta.14 | v14l1 | king | 20000, 0 | 7,556.0 | found |
| beta.14 | v14l1 | queen | 20000, 0 | 941.9 | found |
| beta.14 | v14l1 | king | 0, 20000 | 2,957.1 | found |
| beta.14 | v14l1 | queen | 0, 20000 | 5,022.5 | found |
| beta.14 | v14l1 | king | -20000, 0 | 7,340.8 | none in range |
| beta.14 | v14l1 | queen | -20000, 0 | 402.4 | found |
| beta.14 | v14l1 | king | 0, -20000 | 7,137.5 | none in range |
| beta.14 | v14l1 | queen | 0, -20000 | 7,175.1 | none in range |
| beta.15 | v15l1 | king | 0, 0 | 742.9 | found |
| beta.15 | v15l1 | queen | 0, 0 | 699.1 | found |
| beta.15 | v15l1 | king | 20000, 0 | 1,126.4 | found |
| beta.15 | v15l1 | queen | 20000, 0 | 215.7 | found |
| beta.15 | v15l1 | king | 0, 20000 | 558.4 | found |
| beta.15 | v15l1 | queen | 0, 20000 | 824.2 | found |
| beta.15 | v15l1 | king | -20000, 0 | 1,063.2 | none in range |
| beta.15 | v15l1 | queen | -20000, 0 | 178.3 | found |
| beta.15 | v15l1 | king | 0, -20000 | 1,004.6 | none in range |
| beta.15 | v15l1 | queen | 0, -20000 | 945.9 | none in range |

</details>

**New land comes in sooner.** A fresh area of Utopia's first chunk takes about a third of the time beta.14 took.

![The first chunk of a fresh area and of a fresh server, beta.14 against beta.15](https://raw.githubusercontent.com/CoolFreeze23/Orespawn/v1.21.1-2.0.0-beta.15/phase_g_reports/release_media/2.0.0-beta.15/graph_first_chunk.png)

<details><summary>The numbers (ms)</summary>

| Version | Run | Areas' first chunks (by area, sorted by x, z) | Server's first chunk | Flight median / p99 |
|---|---|---|---|---|
| beta.14 | v14a1 | 582.7, 760.4, 732.8, 767.2, 499.2, 564.5, 536.5, 583.4 | 1098.3 | 43.3 / 122.4 |
| beta.14 | v14a2 | 521.3, 631.8, 680.2, 613.8, 567.7, 515.7, 927.3, 555.4 | 1181.3 | 41.6 / 114.5 |
| beta.15 | v15a1 | 175.3, 204.2, 235.4, 193.8, 201.5, 198.8, 184.8, 251.4 | 911.0 | 39.4 / 101.9 |
| beta.15 | v15a2 | 217.4, 213.7, 303.9, 262.1, 210.0, 259.2, 204.0, 307.1 | 834.4 | 27.1 / 95.1 |

</details>

**The creatures back on the original's lists.** In their biomes, in the original's list (land, water, monster or ambient), at its weights and group sizes.

![OreSpawn's spawn list entries in the overworld and the Nether against the original's, beta.14 and beta.15](https://raw.githubusercontent.com/CoolFreeze23/Orespawn/v1.21.1-2.0.0-beta.15/phase_g_reports/release_media/2.0.0-beta.15/graph_spawn_lists.png)

<details><summary>The numbers</summary>

|  | beta.14 | beta.15 |
|---|---|---|
| The original's entries, as the original had them | 70 | 233 |
| The original's entries, weight or group changed | 13 | 3 |
| The original's entries, missing or in another list | 153 | 0 |
| Entries the original never had, in its biomes | 617 | 0 |
| Entries in biomes 1.21 added | 268 | 104 |

</details>

**Chaos without caves**, as the original's Chaos had none.

![One row of Chaos's floating land from the side in 1.7.10, beta.14 and beta.15](https://raw.githubusercontent.com/CoolFreeze23/Orespawn/v1.21.1-2.0.0-beta.15/phase_g_reports/release_media/2.0.0-beta.15/chaos_cutaway.png)

<details><summary>The numbers</summary>

| Chaos at seed 1007, chunks -12 to 12 | Cells of ground (stone, dirt, grass, ore, sand) | Alike with 1.7.10 (of the cells either has ground) | Air where the original has ground |
|---|---|---|---|
| 1.7.10 (ref1007-ic) | 5,201,304 | — | — |
| beta.14 (v14c, 8a3fb2ad) | 5,095,787 | 97.95 % | 102,006 |
| beta.15 (v15c, 64adbeaf) | 5,187,511 | 99.72 % | 10,452 (chance placements, not caves) |

</details>

**The Ant and Spider Robots stand on their legs**, where beta.14 drew them a block and a half above.

![The Ant and Spider Robots in 1.7.10, beta.14 and beta.15, with the modern and the classic legs](https://raw.githubusercontent.com/CoolFreeze23/Orespawn/v1.21.1-2.0.0-beta.15/phase_g_reports/release_media/2.0.0-beta.15/robots.jpg)

**The flying Islands rise from the Island block again, and fly**: they drift with their blocks, as the original's did.

![An Island block making an Island Too, which then drifts; beta.14's block does nothing](https://raw.githubusercontent.com/CoolFreeze23/Orespawn/v1.21.1-2.0.0-beta.15/phase_g_reports/release_media/2.0.0-beta.15/islands.jpg)

**The Queen drops her loot again** ([#9](https://github.com/CoolFreeze23/Orespawn/issues/9)): killed by a player, her drop falls from 12 over her and lies scattered round her as in the original, with her experience, and the Princess comes.

![The Queen killed by the player in beta.15: her drop falling, landed round her, and after she is gone](https://raw.githubusercontent.com/CoolFreeze23/Orespawn/v1.21.1-2.0.0-beta.15/phase_g_reports/release_media/2.0.0-beta.15/queen_drop.jpg)

**The Islands' structures apart** ([#8](https://github.com/CoolFreeze23/Orespawn/issues/8)): the original never built a structure where an earlier one stood in its way; beta.15 turns such a spot away again, so two towers can no longer join. The towers come as often as before, the King's and the Queen's at even odds.

![The Islands' structures from above in beta.14 and beta.15, seed 1007: three Queen towers built into each other in beta.14, apart in beta.15](https://raw.githubusercontent.com/CoolFreeze23/Orespawn/v1.21.1-2.0.0-beta.15/phase_g_reports/release_media/2.0.0-beta.15/towers_map.png)

<details><summary>The numbers</summary>

| Census of islands_structures (seed 1007, 240 by 240 cells of 9 by 9 chunks) | Build | Structures | Towers (King / Queen) | Pairs of towers whose boxes meet | The closest two towers |
|---|---|---|---|---|---|
| c14isl | 8a3fb2ad (beta.14) | 29,839 | 4,656 (2,326 / 2,330) | 159 | 27 blocks apart |
| c15isl2 | feb82864 (the fix) | 29,935 | 4,664 (2,311 / 2,353) | 70 | 65 blocks apart (a tower and its platform 52 across) |

The fix turns away 1,341 of 31,277 picks (4.3%); the original's space checks turned away about 4.5% (WGEN-080's simulation).

</details>

### beta.14 in pictures

beta.14 shipped without pictures; here are its changes a player can see.

**The cherry and peach orchards**, grown where the original grew them, and since beta.15 wearing round as the original's once the land is lived in.

![Utopia's orchard in 1.7.10, beta.14 and beta.15, lived in, and beta.15 fresh](https://raw.githubusercontent.com/CoolFreeze23/Orespawn/v1.21.1-2.0.0-beta.15/phase_g_reports/release_media/2.0.0-beta.15/orchard.jpg)

![Utopia's trees: the original's trunk places over the port's, seed 1007](https://raw.githubusercontent.com/CoolFreeze23/Orespawn/v1.21.1-2.0.0-beta.15/phase_g_reports/release_media/2.0.0-beta.15/trunk_map.png)

<details><summary>The numbers</summary>

| Utopia, seed 1007, chunks -20 to 20 | 1.7.10 (ref1007-w161) | beta.14 and beta.15 (t15w, ad3ac989) | At the same place |
|---|---|---|---|
| Huge and royal trunk columns | 2,964 | 3,347 | 2,126 (72 %) |
| Fruit-tree bases | 113 | 127 | 68 (60 %) |

beta.14 and beta.15 grow the same trees: in the flight strip both runs generated (v14a1 on 8a3fb2ad, v15a1 on 64adbeaf), 745 of 747 trunk columns and 29 of 30 fruit-tree bases at the same place.

</details>

![The plants and orchards against the original's](https://raw.githubusercontent.com/CoolFreeze23/Orespawn/v1.21.1-2.0.0-beta.15/phase_g_reports/release_media/2.0.0-beta.15/graph_plants.png)

<details><summary>The numbers (port / 1.7.10)</summary>

| Measure | Counts | Port for one of the original's |
|---|---|---|
| Utopia: dandelions | 9,038 / 8,722 | 1.04 |
| Utopia: poppies | 4,502 / 4,481 | 1.00 |
| Utopia: tall grass | 307,381 / 303,682 | 1.01 |
| Utopia: brown mushrooms | 343 / 276 | 1.24 |
| Utopia: red mushrooms | 129 / 147 | 0.88 |
| Utopia: reeds | 272 / 206 | 1.32 |
| Utopia: pumpkins, three seeds | 369 / 165; 277 / 285; 281 / 246 | 2.24, 0.97, 1.14 |
| Mining: dandelions | 2,980 / 3,025 | 0.99 |
| Mining: poppies | 1,540 / 1,401 | 1.10 |
| Mining: tall grass | 39,006 / 38,664 | 1.01 |
| Mining: brown mushrooms | 343 / 268 | 1.28 |
| Mining: red mushrooms | 114 / 132 | 0.86 |
| Mining: reeds | 36 / 39 | 0.92 |
| Mining: pumpkins | 45 / 27 | 1.67 |
| Crystal: reeds | 31 / 52 | 0.60 |
| Crystal: the maze's mushrooms, two seeds | 709 / 471; 501 / 593 | 1.51, 0.84 |
| Orchards: fruit trees (crowns) | 5,431 / 5,325 | 1.02 |
| Orchards: fruit trees of 50 leaves or more | 3,525 / 3,498 | 1.01 |
| Orchards: fruit leaves a chunk | 57.4 / 60.7 | 0.95 |

</details>

**Chaos's colours**: beta.14 gave it the original's; since beta.15 its red sky and water are the default again, and the original's colours are the option.

![Chaos at its spawn in 1.7.10, beta.14, beta.15 (the default, red) and beta.15 with chaosRed = false (the original's colours)](https://raw.githubusercontent.com/CoolFreeze23/Orespawn/v1.21.1-2.0.0-beta.15/phase_g_reports/release_media/2.0.0-beta.15/chaos_colours.jpg)

**The Squid Zooka is held as in the original**: level on the shoulder in third person, pointing ahead in first person.

![The Squid Zooka held in 1.7.10 and beta.14, in third and first person](https://raw.githubusercontent.com/CoolFreeze23/Orespawn/v1.21.1-2.0.0-beta.15/phase_g_reports/release_media/2.0.0-beta.15/zooka.jpg)

**Thrown things spin in flight** as in the original: the rock, the cage, the ink sack, the ice ball and the dead Irukandji, hung still here, a frame about every three game ticks.

![Five of the spinning projectiles turning in place](https://raw.githubusercontent.com/CoolFreeze23/Orespawn/v1.21.1-2.0.0-beta.15/phase_g_reports/release_media/2.0.0-beta.15/spin.gif)

<!-- /pictures -->

**Reported on GitHub**
- [#9](https://github.com/CoolFreeze23/Orespawn/issues/9), the Queen: she drops her loot and experience again, and what she fires or summons ends with her.
- [#8](https://github.com/CoolFreeze23/Orespawn/issues/8), the Islands' towers: no longer built into each other; the King's and the Queen's come at even odds, as in the original.
- [#6](https://github.com/CoolFreeze23/Orespawn/issues/6), Utopia's big trees: checked against the original at the same seed; since beta.14 they grow where its did, no closer together and no smaller.

**Changed**
- Chaos is red again by default: its red sky, fog and water and its own grass and leaf colour, as before beta.14. The original's colours are the option now: `chaosRed = false` under `[modern]` in `config/orespawn-common.toml`; classic mode has them too.

**Fixed**
- `/locate` for the King and Queen altars answers in about a second at most again (1.13 s the slowest, from the spawn and from 20,000 blocks out each way). In beta.14 a search took up to seven and a half seconds, and the server stood still while it searched.
- OreSpawn's creatures spawn in the overworld and the Nether from the original's own lists: in its biomes, as often and in groups of its sizes. Most of its monsters (the Mantis, the Bee, the Scorpions, the Ender Knight and many more) were drawn from the same list as zombies and skeletons, so they came far less often than in the original; they come as often as they did.
- The Mantis, the Cater Killer, the Sea Viper and the Irukandji spawn in the open by day again, as in the original, where they could only appear in dark places; the Cloud Shark and the Large Worm no longer wait for the dark, and no creature needs grass or light the original did not ask for.
- The Whale, the Flounder, the Skate, the Irukandji, the Sea Monster, the Attack Squid, the Rubber Ducky, the Water Dragon and the Frog spawn in water again, and the Crab and the Lizard come out of the water as the original's did.
- The Gazelle, the Cliff Racer, the Cloud Shark, the Cave Fisher, the Creeping Horror, the Rotator and the dinosaurs no longer spawn all over the overworld, nor the Ostrich and the Cassowary beyond the original's few biomes: the original kept them to their own dimensions. They are an option: `extraSpawns = true` under `[modern]`, with the modern options on.
- Mining has its butterflies, moths, cockatiels, cassowaries, Mothra and the Ender Knights and Reapers again, and the Islands' dragons, cliff racers, cloud sharks, gold fish and Stinkies spawn as the original's did.
- Caves and ravines in Utopia, the Village, Crystal and Mining stop short of water standing above them up to the original's height (Y248), where they stopped looking at Y120, so a tunnel under a lake on high ground no longer cuts into it.
- The Ant and Spider Robots stand on the ground again: they were drawn a block and a half above where their legs stood, and showed a death tilt, a red hurt flash and a name tag the original's robots never had. A rider sits in the original's seat on them.
- Chaos has no caves or ravines, as the original's Chaos had none. Earlier betas' caves and canyons are an option: `chaosCaves = true` under `[modern]`, with the modern options on (new land only).
- A new part of Utopia comes in sooner: the first chunk of a fresh area takes about a third of the time beta.14 took (a median of 212 ms against 583 in the same conditions), as the huge trees', royal trees' and groves' rolls no longer read the land in the many chunks where it could not change them. The trees stand where they did.
- The flying Island and Island Too rise again from the Island block, as in the original: the Islands leave one on their grass now and then, and as it ticks it sends up one to three flying islands and is gone. And they fly: each drifts on its heading, its blocks moving with it, and blows up where it runs into another. The Island is the original's end stone and diamond ore under mycelium and mushrooms (it was sand), the Island Too a stepped slab of stone with its ore, under grass with now and then water, flowers and a small tree; they no longer vanish when no one is near, and drop the Island block.
- The apple, cherry and peach trees wear round as the original's: a leaf with no trunk or branch close below it falls (with now and then its fruit), so the orchards' crowns no longer stay great square blocks of leaves once the land around them is lived in. The experience and scary leaves keep the same rule.
- The butterfly, firefly, mosquito and moth plants no longer show their texture's empty corners; the fruit leaves stay see-through on Fast graphics too, as the original's did.
- Utopia, Mining, the Village, Chaos, the overworld's and the Nether's OreSpawn ores generate the same from one seed every time: 1.21's ore placement checked the ground's height before drawing each vein, so one vein checked differently moved every later vein of that ore in the chunk, and two worlds of one seed came out with whole blobs of dirt, gravel and ore in one and plain stone in the other. Every vein is drawn now, as the original's generator drew them. Mining's and the Village's dungeons are made repeatable too, a change of the port's own: the original's dungeon tries shared one random, so a room that was built moved the tries after it, as 1.21's do; each try now draws from a random of its own, the rooms as many, as big and as stocked as before.
- The Queen drops her loot and her 25,000 experience when killed, scattered round her as in the original: the Royal Guardian Sword, the Prince's egg, queen scales, beef, bones and rotten flesh, and the Princess comes. A blast from her own fireballs or purple orbs in her last moments healed her back to life for a moment, and her second death dropped nothing. Every blast heals her again, as in the original, whoever set it off. ([#9](https://github.com/CoolFreeze23/Orespawn/issues/9))
- What the Queen fires or summons ends with her: her fireballs, lightning bolts and purple orbs no longer go on falling after the fight, also where they flew into land no one was near. Any creature's fireball now ends when the creature that fired it is gone, as the original's did. ([#9](https://github.com/CoolFreeze23/Orespawn/issues/9))
- The Islands' structures are no longer built into each other, two towers joined among them: the original never built a structure where an earlier one stood in its way, and beta.15 turns such a spot away again. The towers come as often as before, one in about 1,000 chunks, the King's and the Queen's at even odds. ([#8](https://github.com/CoolFreeze23/Orespawn/issues/8))

**Good to know**
- 1.21's newer biomes take the creatures of the biome they grew from: the warm and cold oceans the ocean's, the bamboo jungle the jungle's, the mangrove swamp the swamp's, the meadows, groves and peaks the mountains', the new Nether biomes the Nether's. The cherry grove and the cave biomes have none of OreSpawn's.
- Chaos land an earlier beta generated keeps its caves; new Chaos land has none.
- Islands land an earlier beta generated keeps its structures, joined towers too. New Islands land places its structures afresh, so a structure not yet generated may stand a little elsewhere or be another kind.
- A config file beta.14 wrote keeps `chaosRed = false`, beta.14's default, so Chaos keeps the original's colours there. For the red, change that line under `[modern]` in `config/orespawn-common.toml` to `chaosRed = true`.
- The Halloween Ghosts and the Easter Bunny are listed when the world is opened on 31 October or 20 April, as the original listed them.
- Fruit, experience and scary leaves fall when no log stands within reach below them, leaves a player placed included (the original had no exception): builds from earlier betas that used them away from logs lose them, and old orchards wear round once their chunks tick.
- A flying Island or Island Too drifting into land or a build replaces what it meets with its own blocks, and blows up where the Island meets stone or the Island Too meets end stone; they no longer despawn. Killing one stops it (it drops the Island block).

**Install:** put `orespawn-1.21.1-2.0.0-beta.15.jar` in `mods/` (NeoForge 21.1, Minecraft 1.21.1, GeckoLib 4.7 or newer) and take the beta.14 jar out. For the spears, add Mounts of Mayhem 1.9.8.

### In more detail

#### What beta.15 is

beta.15 is beta.14 with the original's spawn lists, the ores and dungeons made repeatable from one seed, the altars' searches and new land fast again, the original's flying Islands, orchards, robots and Chaos without caves, the GitHub issues #9 and #8 fixed and #6 checked against the original, and Chaos's red back as the default; and pictures of what changed, beta.14's too.

#### What changed

**The spawn lists.** The original listed many of its monsters in the same list as bats, so they spawned beside them whatever the number of zombies and skeletons about, and many of its creatures in the water list; each species had its own spawning rule. This version rebuilds the overworld's and the Nether's lists from the original's own registrations, keeps each creature in the list the original put it in, and judges each by its own rule alone.

**The altars' /locate.** beta.14 grew Utopia's trees from the random the original drew them from, after the chunk's vegetable and critter patches, and the altar's quick check began reading the ground for those patches in every chunk a search passed. It now works out, without the ground, every way the patches could have left the random, and reads the ground only in the few chunks where one of them lets the altar's roll through. Where the altars stand is unchanged.

**Utopia's tree rolls.** A chunk's first generation in a new area asks for the structure starts of every chunk within reach, some 360 of them, one after another, and Utopia's huge tree, royal trees and grove each replayed the chunk's vegetable and critter patches on the ground before their own one-in-fifty and one-in-thirty rolls, which turn most chunks away whatever the ground holds. The rolls now run first on every way the patches could have left the random, and the ground is read only where one lets them through. A fresh area's first chunk takes about a third of beta.14's time; the trees stand where they did.

#### How to install

Put `orespawn-1.21.1-2.0.0-beta.15.jar` into the `mods` folder of a NeoForge 21.1 instance for Minecraft 1.21.1 together with GeckoLib 4.7 or newer, and take the beta.14 jar out; MultiHitboxLib and Databuddy are bundled in the jar. For the spears, add Mounts of Mayhem 1.9.8 (on a server: on the server and every client). Existing worlds carry over.

## Part two — for the modder

### The spawn lists (WGEN-086)

The original's 348 `EntityRegistry.addSpawn` calls are replayed as Forge 10.13.4.1614 runs them (an entry of the same class takes the new weight and group, and a new entry is added whatever) into `original_spawns.json`, an `orespawn:spawn_table` (ModBiomeModifiers): entries in named lists, so an entity sits in the list the original gave it whatever its own category, each with its biomes and a condition read as the server starts (`ordinary`, `halloween`, `easter`; `extra_spawns`). The lists are mapped to 1.21's biomes through the game's 1.18 upgrade (a species' entries from the first ancestor that lists it) and by descent for the newer biomes, and match the 1.7.10 reference server's lists for every species on by default. `ListedSpawnPlacement` (ENT-S-170's Frog placement, renamed) places every listed species by the list a natural spawn drew it from (`NaturalSpawnerMixin` passes the list's category; other callers read the biome's lists), water cells counted by their fluid; `OriginalSpawnRules` makes natural spawning read an own-rule species' `checkSpawnRules` alone (1.21's obstruction test refused every water spawn of a class that is not a WaterAnimal) and a chunk's spawning read no rule, as 1.7.10's `performWorldGenSpawning` did; no registered predicate adds a rule the original lacked. Earlier betas' extra overworld spawns: `extra_spawns.json`, behind `[modern] extraSpawns` (default false). Tests: `SpawnTableTests` (wgen086a to d), the date tests on the dated entries.

### The robots' render chain (ENT-S-198)

The original's RenderAntRobot and RenderSpiderRobot never ran the living render chain: from the entity's anchor they turned the model 180 - yaw about Y, flipped it (-1, -1, 1) and drew it, the legs posed by the entity's own solver. The port drew them through LivingEntityRenderer, whose translate of (0, -1.501, 0) after the flip put every part, the solver's planted feet too, 1.501 blocks higher, with the death tilt, the hurt overlay and the name tag besides. Both renderers now draw the original's chain (`OriginalRobotChain.modelToAnchor`, free of client classes so the test `s198a` reads the very matrix), with the model's original arguments (0, 0, -0.1, 0, 0); the modern gait's tilt turns about the model's pivot at the anchor (`ModernSpiderGait.MODEL_PIVOT_Y_OFFSET` 0), which is also the seat's.

### Chaos's caves (WGEN-112)

The original's Chaos (ChunkProviderOreSpawn6) ran no MapGenCaves or MapGenRavine; `chaos_biome.json` had listed vanilla's cave, extra cave and canyon carvers. It lists none now, and `orespawn:chaos_caves` (`chaos_caves.json`) adds the three back while `[modern] chaosCaves` is on (default false), read as the world loads. Test `wgen112a`.

### The carvers' water check (WGEN-111)

`LegacyCarvers.waterOnShell`, the check the legacy cave and canyon carvers make before carving a stretch, looked for water on the shell of the stretch's box up to Y120; MapGenCaves and MapGenRavine clamp the box's top at 248. Now 248; test `wgen111a`.

### The altars' early roll (WGEN-115)

`UtopiaTreeStructure.altarRollReached`, the altar structure's check before it asks for the chunk's pass, replays the chunk's surface patches before its tree rolls (beta.14, WGEN-108), and each patch place read the ground through the column probe: about 1.3 reads a chunk, so a `/locate` for an altar with none in its 201 x 201 chunks took 6.5 to 6.7 s on a dedicated server (about 163 µs a chunk). A place's answer only decides whether its own draws follow, so it only changes how far the chunk's java.util.Random has gone: `patchStates` gives every LCG state the patches can leave it in (a set grown place by place, one state in most chunks), and the tree rolls on no grass and the 1-in-2,000 draw run on each; the ground is read only where one passes. On seed 1007's dedicated server the whole-square search now takes 0.57 to 0.85 s, and every answer is the same. `utopiaRandom` gains an overload with the LessLag and mob settings given, for the test.

### Utopia's tree rolls (WGEN-116)

A chunk's first generation asks for the structure starts of every chunk its structure references reach (19 x 19), and the game runs a level's chunk steps one at a time. The royal_trees, utopia_huge_trees and utopia_tree_groves sets ask every chunk (spacing 1), and each structure drew the chunk's random through the surface patches (`utopiaRandom`, reading the ground through the column probe at a patch's places when its gate passes) before its own gates: addHugeTree's one in fifty, which the royal trees share, and addOtherTrees' one in thirty after the huge roll and the apple trees. `hugeRollReached` and `groveReached` now run those gates (`hugeGatesPass`, `groveGatesPass`, shared with hugeRoll and grove) on every state `patchStates` gives, the grove's after the huge roll and the apple trees on no grass (where either grows a tree the pass grows no grove, and where neither finds grass they draw as on no grass); the ground is read only where one passes. `chunkPatchStates` keeps the last chunk's states on each thread for its six structure starts. `LegacyTerrainReader`'s column key, x << 32 | z, hashed to x ^ z and crowded a square of columns into a few of the cache's bins; it goes through fastutil's `HashCommon.mix` now (a bijection, so every column keeps its own entry). Over 361 fresh chunks the structure starts took 137 to 169 ms before and 41 to 50 ms after; the first chunk of a far, fresh area 302 to 462 ms before and 167 to 268 ms after; the trees, groves and altars start where they did.

### The Queen's death (BUG-050, #9)

The port had the Queen's loot table, her 25,000 experience and the Princess, and a plain kill dropped them. Her `hurt` turns a blast into healing (orig :776) before vanilla's own dead-or-dying refusal, and two blasts with no entity behind them come with her fight: her big fireballs (`BetterFireball.onHit`'s `explode(null, ...)`, aimed at the player beside her hit boxes) and her purple powers. One in her twenty death ticks set her health back up; she lived on with 1.21's `dead` flag set, and her next death ran `LivingEntity.die`'s `!this.dead` guard: no loot, no experience. `TheQueen.hurt` now refuses anything once she is dead or dying. The blast clause reads `DamageTypeTags.IS_EXPLOSION` (1.21 types a blast an entity set off `PLAYER_EXPLOSION`, which `DamageTypes.EXPLOSION` missed). `spawnAtLocation` places her death drop as the original's `dropItemRand` (one item an entity, 12 over her, `nextInt(20) - nextInt(20)` round, through the death's capture). `BetterFireball` ends when its shooter is gone, as the original's (:104; 1.21's `AbstractHurtingProjectile.tick` lets a shot whose owner is gone fly on); her thunder bolts, fireballs and purple powers end once she is dead or gone, the tie saved (`EndsWithOwner`, `Caster`), a change of the port's own (the original never tied her bolts and purple powers to her). Tests `bug050a`, `bug050b`.

### The Islands' space checks (WGEN-120, #8)

WGEN-080's one roll, `islands_structures` (9 by 9 chunks a spot, a separation of one), put the original's space checks into its weights as the 4.5% of builds they turned away and refused none, so two spots a chunk apart built into each other. `IslandsSpaceCheck` turns a spot away where an earlier spot among its eight neighbours is built and its structure's envelope meets `D4BigSpaceCheck`'s box at Y11 (x -25 to 39, z -25 to 29 from the chunk's corner) or, for a tower, `addD4Castle`'s 18 over its grass (x -20 to 32, z -4 to 32); a seeded mix of each cell stands in for the original's populate order, which followed the player. `LegacyDungeonStructure.findGenerationPoint` asks it, so the structure pass, /locate and the census agree; the weights carry the original's odds before the checks again (towers 429, the generic dungeon 1,144, the others 286). Over 57,600 cells at seed 1007 it turns away 4.3% of the picks; towers 4,656 before and 4,664 after, pairs of towers whose envelopes meet 159 and 70, the closest two 27 and 65 blocks apart (a tower and its platform 52 across). Tests `w078a`, `w078c`.

### Chaos's red the default (MOD-045)

`[modern] chaosRed` defaults to true; with the modern options off, or the key false, Chaos keeps the original's colours. Test `wgen101a`.

### Tests and tooling

`bug050a` and `bug050b`: the Queen killed by a player drops the 226 items one by one 12 over her and within 19 blocks round, her 25,000 experience and the Princess, and a blast in her death ticks heals nothing; both kinds of blast heal her; her bolt, fireball and purple power go once she dies, a pig's fireball once the pig is gone, a fireball with no shooter stays.

`w078c`: on a detached Islands generator over 32 by 32 cells, no built spot has an earlier built neighbour in its checks, every pick turned away has one, and 2 to 8% are turned away.

The worldgen probe gains a `census DIM SET CELLS` step (every cell's spot, first pick and site as the structure pass decides them, written to a TSV), and `tools/ref1710/tree_pairs.py` measures the huge and royal trees' spacing and size in two worlds of one seed.

`wgen115a`: at every LessLag and mosquito and ant setting, over 2,500 chunks and four grounds, the state utopiaRandom leaves its random in is one of `patchStates`' (its next long worked out from the LCG apart from the mod); the early roll answers as before over 80,000 chunks on two grounds, reading the ground in under one chunk in a hundred where the patches read it in about one in five.

`wgen116a`: over 40,000 chunks and four grounds, no huge tree, royal tree or grove where the early checks say none can grow; they let a chunk through in under half the chunks whose patches read the ground before; the kept patch states are the chunk's at every setting.

### The harness

Checks on the release tree: drift 0; `RESULT: 0 error(s), 0 advisory(ies), 4 acknowledged; draw order: 104 shipped geo: 103 seam + 1 outside-seam`, `ARMOUR MODELS PASS: 56 pieces`, `G1 PARITY PASS` 2, 13 and 101 models, checked-in proof verified, `BUILD SUCCESSFUL in 9m 23s`; the gametest suite: `All 1452 required tests passed`, `BUILD SUCCESSFUL in 5m 8s` — 1431 tests at beta.14 plus those pinning the spawn lists, the robots' render chain, Chaos's caves and colours, the carvers' water check, the altars' and Utopia's early rolls, the repeatable ores and dungeons, the flying Islands, the fruit leaves, the leaves' and bug plants' render types, the Queen's death and the Islands' space checks. Built with the local Databuddy fallback while commoble.net's maven answers 522 (TEST-027); the jar nests the same Databuddy and jarjar metadata as beta.14's.

### Third-party notices and credits

Unchanged from beta.14.
