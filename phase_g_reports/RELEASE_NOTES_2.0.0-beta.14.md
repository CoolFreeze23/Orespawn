# OreSpawn for NeoForge 1.21.1 — 2.0.0-beta.14

Version `1.21.1-2.0.0-beta.14` (`gradle.properties`; `README.md` line 6), on beta.13: the original's plants, ores, colours and trees in its dimensions, the bosses' vanilla gear on the original's enchantments, and creatures and items that look and move as they did in the original. Release: https://github.com/CoolFreeze23/Orespawn/releases/tag/v1.21.1-2.0.0-beta.14

## Part one — for the player

The original's dimensions grow its flowers, mushrooms, reeds, colours and trees again, the bosses' gear rolls the original's enchantments, and a few creatures and items look and move as they did.

**Fixed**
- Utopia, the Village, Chaos, Crystal and Mining grow the original's flowers, grass, mushrooms, reeds and pumpkins, about as many as the original's and where it let them grow: flowers and grass wherever their soil takes them, mushrooms where the original's light was dim enough, under trees and in caves. Before, Utopia, the Village and Chaos had 1.21's flowers, with tulips and others the original never had and few dandelions and poppies, and Crystal and Mining had no mushrooms or reeds.
- Chaos's ores are the original's: OreSpawn's own ores and the vanilla ones as often as in the original, and no copper. Before, OreSpawn's ores came three times as often there.
- The lava in the caves of Utopia, the Village and Mining stands one block higher, as the original's did, and Mining's rubies are as common as the original's.
- The Village, Islands, Chaos, Crystal and Mining have the original's climate and its sky, water, grass and leaf colours. Chaos's red sky and water are now an option: `chaosRed = true` under `[modern]`, with the modern options on.
- The dirt round a lake's shore turns to grass where the sky reaches it, as in the original.
- The orchards of Utopia and the Village grow cherry and peach trees too, one orchard in ten each, smaller than an apple tree and with their own blossom, as in the original. Before, every orchard grew apple trees.
- Utopia's huge trees, King and Queen trees, orchards and groves, and the Village's orchards, grow in the chunks the original grew them in for the same seed, most of them on the very same spot. Before, the same trees grew at the same odds but elsewhere, so a place could be much more or less wooded than in the original.
- The Village's lakes keep out of its villages' buildings and streets only, as the original's did, instead of every chunk a village came near, so there is water round the villages again.
- Godzilla's and the Kraken's diamond, iron and golden swords, tools and armour roll the original's enchantments, with its chances and levels, as their OreSpawn gear already did, instead of one random enchantment that could be a curse or Mending.
- A boss sword that rolls Sharpness twice adds the two levels, as the original's damage counted both rolls.
- Aqua Affinity comes at level I on the bosses' helmets and on the Ultimate, Royal and Lava Eel helmets, as the original's worked the same at any level; above I it made mining under water faster than on land.
- The Zoo Keeper takes Unbreaking from a book at the anvil, as in the original, which can spare it a capture.
- Godzilla, the Kraken and the Attack Squid drop a golden carrot where the original did, instead of a glistering melon slice.
- An invisible mob no longer shows its wing membranes, glowing orbs and other see-through parts.
- Thrown shoes, rocks, urchins, cages and the water, laser and ice balls, ink sacks, acid and dead Irukandji spin in flight as in the original; the cage turns at the original's speed.
- The Cater Killer, Godzilla, the King, the Queen and the Kraken are drawn at their PlayNicely size by the server's setting, not the player's own, and from the moment they appear.
- The Squid Zooka is held as in the original: resting level on the shoulder in third person and pointing ahead in first person, at the original's size.

**Good to know**
- Only newly generated land changes. In Utopia, the Village, Chaos, Crystal and Mining, land an earlier beta generated keeps its plants, trees and ores; new land gets the original's.
- Chaos's red sky and water from earlier betas are an option now: `chaosRed = true` under `[modern]` in `config/orespawn-common.toml`, with the modern options on. Without it Chaos has the original's colours.
- The trees of Utopia and the Village grow where the original's grew for the same seed, most of them on the very same spot; where the original's other plants stood in the way of a tree, it grows a little apart. The orchards grow about four fifths of the original's fruit trees, the cause not yet found. Caves, plants and ponds still fall where chance puts them.
- The Village's 1.21 villages are about four times the size of the original's and reshape the ground round them, so with the lakes kept out of their buildings and streets there is about half the original's water round them.
- A helmet that already has Aqua Affinity II or higher keeps it; new drops and new helmets come at I.
- The Squid Zooka is as big as the original's: about six blocks long on the shoulder, and in first person its back reaches behind the view.

**Install:** put `orespawn-1.21.1-2.0.0-beta.14.jar` in `mods/` (NeoForge 21.1, Minecraft 1.21.1, GeckoLib 4.7 or newer) and take the beta.13 jar out. For the spears, add Mounts of Mayhem 1.9.8.

### In more detail

#### What beta.14 is

beta.14 is beta.13 with the rest of the original's decoration in its five dimensions (plants, ores, lava, colours, the lakes' grass and the trees' places), the bosses' gear and the Zoo Keeper as the original had them, and render fixes found by an audit against the original's renderers.

#### What changed

**The plants.** The original planted its dimensions with Minecraft 1.7.10's own decorator: so many flower and grass patches a chunk (four and six in Utopia and the Village, two and four in Chaos), mushrooms, ten tries at reeds by water and a pumpkin patch, each under 1.7.10's rules. This version plants them the same way, by the same rules: a flower or tall grass grows wherever its soil is, and a mushroom where the original's light was under 13 with solid ground under it, so they come under trees, in caves and in Crystal's maze. Against the original at the same seed the flowers and grass agree within a few per cent.

**The trees.** The original grew Utopia's and the Village's trees chunk by chunk from a random seeded the way Forge seeded it, after the chunk's vegetable and critter patches. This version seeds and draws the same way, so the same trees stand in the same chunks for the same seed, most on the very spot. The orchards grow cherry and peach trees as well as apple trees.

**The bosses' gear.** Godzilla's and the Kraken's vanilla swords, tools and armour now roll the original's enchantments one by one, as their OreSpawn gear already did; a sword's two Sharpness rolls add up, as the original's damage counted both; Aqua Affinity comes at I.

**The renderers.** An invisible mob hides its see-through parts; the projectiles the original spun spin again; the PlayNicely size follows the server; the Squid Zooka is held as the original's.

#### How to install

Put `orespawn-1.21.1-2.0.0-beta.14.jar` into the `mods` folder of a NeoForge 21.1 instance for Minecraft 1.21.1 together with GeckoLib 4.7 or newer, and take the beta.13 jar out; MultiHitboxLib and Databuddy are bundled in the jar. For the spears, add Mounts of Mayhem 1.9.8 (on a server: on the server and every client). Existing worlds carry over.

## Part two — for the modder

### The original's plants (WGEN-109, WGEN-110, WGEN-113, WGEN-103)

Utopia's, the Village's and Chaos's flowers, grass, mushrooms, reeds and pumpkins were vanilla 1.21's plant features (at seed 1007, Utopia's dandelions 78 in the 625 chunks round the spawn against the original's 994; the Village's tulips and double plants, which the original never had), and Crystal and Mining had none of the decorator's mushrooms or reeds. `LegacyPlantsFeature` (`orespawn:legacy_plants`; `legacy_plants_utopian`, `_chaos`, `_crystal`, `_hills`) is 1.7.10's `BiomeDecorator.genDecorations` for the plants with BiomeGenUtopianPlains' counts: the flower and grass patches, the mushroom patches, the ten reed tries and the pumpkin patch whatever the counts, from eight blocks in as the decorator, at its heights from 1.7.10's height map (`LegacyLight.height`: the first cell over the highest block of non-zero opacity). The rules are 1.7.10's: a flower or tall grass needs only the block under it (BlockBush, BlockTallGrass); a mushroom needs mycelium or podzol, or a light under 13 (`LegacyLight.sky`, Chunk.generateSkylightMap: from 15 down the column, each block taking its opacity, a clear block one once the light is under 15, leaves 1, water and ice 3) over an opaque block, leaves counting as one, as 1.7.10 cached them. The original's trees in Utopia and the Village were written after the decorator with no height map or light update, so its plants never saw them: `#orespawn:decoration_unseen` keeps the plants' heights, light and the grass's sink blind to every log and leaf there. Mining's extreme-hills decoration (`hills_decoration_1710`) takes the same heights. Over 81 x 81 chunks at seed 1007 against the original: Utopia's dandelions 9,038 against 8,722, poppies 4,502 against 4,481, tall grass 307,381 against 303,682, mushrooms 343 and 129 against 276 and 147, reeds 272 against 206; Mining's flowers and grass within 1 to 10 %. Two counts stay off: Utopia's pumpkin patches about 2.5 times the original's and Crystal's maze mushrooms about 1.45 times, the causes not yet found (WGEN-114).

### Chaos's ores, the caves' lava and Mining's ruby (WGEN-104, WGEN-105)

Chaos's ore step is the other dimensions' (beta.13's WGEN-097 and WGEN-095): OreSpawn's clipped ore pass first, then 1.7.10's vanilla ores and discs, no copper. At seed 1007 against the original, a chunk: salt 5.0 against 4.5, the spawn ores 4.7 against 4.7, coal 78.6 against 79.0, iron 19.7 against 20.6, gold 1.0 against 1.0. The legacy caves and canyons fill lava to Y10, as 1.7.10's MapGenCaves and MapGenRavine tested `y < 10` one block under the cell they wrote; Mining's ruby ore, which follows the lava on stone, places after the springs and the decoration's plants (`ore_ruby_mining`), so it is as common as the original's (0.7 a chunk in both).

### The climates and colours (WGEN-106)

The Village, Islands, Chaos, Crystal and Mining take the temperature, rainfall and colours their 1.7.10 biomes and world providers set, and the sky, water, grass and foliage colours vanilla derives from them. Chaos's red sky and water from earlier betas are `orespawn:chaos_red_colours` (`chaos_red.json`), applied at server start when `[modern] chaosRed` is on (default off) with the modern options on.

### The lakes' grass and the Village's lakes (WGEN-107, WGEN-094)

`SafeLakeFeature` turns a lake's shore dirt to grass where the sky reaches it, as WorldGenLakes did. The original kept a lake out of a chunk when a village reached its population window; the Village's villages are vanilla's plains jigsaw villages, about four times the original's, and the port kept a lake out of every chunk their structure references reached (610 of the 625 round the spawn at seed 1007, against the original's 290). A lake is now skipped where a village's piece, not its whole box, meets the window (the chunk's starts and its east, south and south-east neighbours', `SafeLakeFeature.inVillageLand`).

### The trees' places and the orchards (WGEN-108)

The original's Utopia and Village trees came from a Forge world generator (OreSpawnWorld), each chunk's random seeded by GameRegistry.generateWorld (`Random(seed)`, two longs shifted right by 3, `xSeed * chunkX + zSeed * chunkZ ^ seed`) and drawn after the chunk's surface patches (Utopia's strawberries, corn, tomatoes, vegetables, butterflies, mosquitoes and ants; the Village's mosquitoes and ants). The port's came from 1.21's structure seed, so the same trees at the same odds fell in other chunks. `UtopiaTreeStructure.forgeRandom`, `utopiaRandom` and `villageRandom` replay the original's seed and draws (the surface patches' grass checks read from the terrain): at seed 1007, seven in ten of the original's tall trunks stand on the very spot. The orchards grow cherry and peach trees one orchard in ten each (`UtopiaTreeStructure.Fruit`), each with its own crown and blossom (`cherry_leaves`, `peach_leaves`), as ItemAppleSeed's trees. The orchards hold about four fifths of the original's fruit trees, the cause not yet found.

### The bosses' gear, the Zoo Keeper and the golden carrot (ITEM-077, ITEM-078, ITEM-079, ITEM-074, ITEM-080)

Read from the 1.7.10 server jar: EnchantmentHelper's melee bonus walks every enchantment entry, so two Sharpness entries strike as their sum, and the Aqua Affinity modifier is on or off. In the six bosses' tables a later `set_enchantments` of an enchantment the entry already set carries `"add": true` (15 entries), Aqua Affinity is set at 1 (and in the Royal, Ultimate and Lava Eel helmets' own sets), and Godzilla's and the Kraken's 54 diamond, iron and golden gear entries roll the original's dice in place of `enchant_randomly`, each entry's dice read from its case of the original's drop code. The Zoo Keeper takes Unbreaking through `supportsEnchantment` and has two points of durability: 1.7.10 broke an item when its damage passed its maximum, 1.21 when it reaches it, so with two a capture it breaks after one capture as the original's did and Unbreaking spares it as often. Godzilla's, the Kraken's and the Attack Squid's tables drop `minecraft:golden_carrot` where the original's code dropped `Items.field_151150_bK`.

### The renderers (ENT-S-194 to ENT-S-197, ENT-S-199)

The GeckoLib seam's second pass (`OreSpawnGeoReplacedEntityRenderer.SecondPassLayer`) returns for an invisible entity, as the classic `WingMembraneLayer` does; 1.7.10 drew those parts inside the model's render, which it skipped for an invisible mob. The ten projectiles the original drew through RenderSpinner and RenderThrownRock (a camera-facing quad turned about the view axis by the entity's pitch, which each turns every tick: the shoes and the cage 20 degrees, the rock, the urchin, the water ball and the ink sack 30, the laser, ice and acid balls and the dead Irukandji 50) are drawn by `SpinnerRenderer`, ThrownItemRenderer's drawing with that turn; the cage turns 20 degrees a tick, as the original's. The Cater Killer keeps the server's PlayNicely in synced entity data, as the original's datawatcher 21, for its two renderers, which read the client's config before; it and Godzilla, the King, the Queen and the Kraken set that copy as they are made on the server, so a new one is drawn at its size before its first AI tick. The Squid Zooka's holds were worked out from both versions' hand chains (1.7.10's RenderPlayer block branch, Forge's equipped-block offset, RenderSquidZooka and the model's own 180-degree turn at full unit scale, against 1.21's ItemInHandLayer, ItemInHandRenderer and the item's display): `squid_zooka.json`'s third- and first-person transforms put every corner of its boxes within 4e-6 blocks of the original's, and the left hand mirrors the right as the game does.

### Tests and tooling

Three tests that read detached world generators and held a server tick for seconds run their scans off the server thread, as the altar scans did, and every such scan now stops when its test ends instead of running on (TEST-025). The Queen's wake-up test holds her at her spawn: from her first tick she flies toward a random point up to 120 blocks off, and once she left the test's force-loaded chunks she stopped being ticked (TEST-029). New tests pin the plants' light and height rules, the orchards' fruit, the trees' seeding, the Village's lakes, the bosses' dice and the added Sharpness, the Zoo Keeper at the anvil and its durability, the golden carrot, and the PlayNicely copies.

### The harness

Checks on the release tree: drift 0; `RESULT: 0 error(s), 0 advisory(ies), 4 acknowledged; draw order: 104 shipped geo: 103 seam + 1 outside-seam`, `ARMOUR MODELS PASS: 56 pieces`, `G1 PARITY PASS` 2, 13 and 101 models, checked-in proof verified, `BUILD SUCCESSFUL in 7m 19s`; the gametest suite: `All 1431 required tests passed`, `BUILD SUCCESSFUL in 4m 12s` — 1422 tests at beta.13 plus those pinning the plants' light and height rules, the orchards' fruit, the trees' seeding, the Village's lakes, the bosses' dice and the added Sharpness, the Zoo Keeper at the anvil and its durability, the golden carrot and the PlayNicely copies. Databuddy 6.0.0.0 came from the copy nested in beta.12's released jar, commoble.net's maven being unreachable at the cut; the jar nests the same file, and its `META-INF/jarjar/metadata.json` is beta.13's byte for byte. The suite runs without Mounts of Mayhem; `-PgametestMayhem` runs the spears' rows with it.

### Third-party notices and credits

Unchanged from beta.13 (see `phase_g_reports/RELEASE_NOTES_2.0.0-beta.13.md`): MultiHitboxLib under LGPL-3.0, the MoreHitboxes portions under MIT, GeckoLib under MIT, Databuddy under MIT; the original mod by TheyCallMeDanger and the OreSpawn authors, 2013-2015; Mounts of Mayhem ("Nautilus & Spears | Mounts of Mayhem", by Gospi, MIT) is an optional dependency, not shipped.
