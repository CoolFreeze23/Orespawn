# OreSpawn for NeoForge 1.21.1 — 2.0.0-beta.13

Version `1.21.1-2.0.0-beta.13` (`gradle.properties`; `README.md` line 6), on beta.12: the modern armour style, horse and wolf armour, spears with Mounts of Mayhem, and enchanting for OreSpawn's own gear. Release: https://github.com/CoolFreeze23/Orespawn/releases/tag/v1.21.1-2.0.0-beta.13

## Part one — for the player

A new look for OreSpawn's armour, armour for your horses and wolves, spears, and enchanting that works for OreSpawn's own swords, tools and armour.

**Added**
- A modern style for all 14 armour sets: 3D pieces with new textures and icons. Switch between modern and classic at any time in the OreSpawn Visuals screen (press O, or Config in the mod list). The choice is yours alone; other players keep their own.
- Horse armour for all 14 sets, with the set's armour value. Thirteen are crafted from the set's material in the shape of leather horse armour; the Royal Guardian one drops from the King and waits in his tower's prize chest.
- Wolf armour for all 14 sets, crafted and found the same way. It never wears out, and shears take it off again, as with vanilla wolf armour.
- Spears for the eight tool tiers (Ruby, Amethyst, Emerald, Ultimate, Crystal Pink, Tiger's Eye, Crystal Wood and Crystal Stone) when Mounts of Mayhem 1.9.8 is installed. They charge like that mod's spears, each with its tier's strength.
- Dogs from Doggy Talents Next wear OreSpawn armour in the modern style too.
- Royal Altars in Utopia sit into the land around them: a hillside is sloped back from the hall and a drop is banked up with grass, instead of a sheer cut wall and a dirt block. Turn it off with `altarTerrain = false` under `[modern]` for the original's flat pad.

**Fixed**
- Utopia, the Village, Crystal and Mining have the original's terrain again: gentle plains with ponds and lakes in Utopia and the Village, rougher hills in Crystal, extreme hills in Mining. No more mountains thousands of blocks out.
- Their water, ground and ores are the original's too, and their caves are made by the original's rules: lakes and low ground are full of water, lake floors have sand, gravel and clay, caves and ravines are as common and as deep as the original's with lava near the bottom (each falls where chance puts it, not where the original's did), new land rests on bedrock at Y0, and the ores are the original's, with no copper, granite, diorite or andesite. Mining has its bare stone peaks and its blue sky back, and its structures and the Village's spawn as often as in the original on the new terrain.
- Apple, cherry, peach, scary and experience leaves take the colour of the biome they grow in, as in the original, instead of grey.
- Utopia, the Village and Mining have the original's springs again (twice as many water springs, and the Village its lava springs), and lava lakes above sea level are as rare as in the original. A lake that starts over high ground settles onto it, as the original's did, and the Village's lakes keep out of its villages. Mining has its trees, flowers and grass back, and its waterfalls and lavafalls down the hillsides.
- OreSpawn's own ores (amethyst, salt, titanium, uranium, the troll blocks and the mob ores) come as often as in the original in Utopia, the Village and Mining; they came two to three times as often. Utopia and the Village also get the original's extra diamond, emerald and gold.
- Utopia and Crystal have the original's sky, water, grass and leaf colours.
- Utopia's trees no longer grow into each other or through the Royal Altars, and a huge tree no longer grows over a Queen tree. The altars stand where the original put them and clear the trees around their hall. Chaos has its trees back.
- The Fairy Castle Trees' floating platforms in the Crystal dimension, chests included, are whole across chunk edges.
- The Apple Cow, the Spider Driver and the T-Shirt spawn naturally again; before, they came out as pigs. Utopia, the Village and Chaos spawn the original's creatures, so the cows are as common there as in classic, and Enchanted Golden Apple Cows are back on mushroom islands. A mob turned off in the config no longer spawns as new chunks generate.
- The Apple Cow and the Golden Apple Cow have their original names back (they were listed as the Red Cow and the Gold Cow). The two optional cows from the classic wiki, off by default, are now the Orchard Cow (Wiki) and the Golden Orchard Cow (Wiki).
- The Kyuubi is see-through and walks head first again; the Gamma Metroid is see-through; the Triffid faces the way the original's did.
- The Princes' and the Princess's wings are see-through and tinted again; the Ghost and the Ghost Skelly are pale and see-through; baby Ostriches and Stink Bugs are half size; the Princess shows her attack texture; the Spider Driver has glowing eyes and turns over when it dies.
- Dungeons in Utopia, Mining and the Village are no longer cut off at chunk edges, and Utopia's ruby dungeons can be found again.
- OreSpawn's swords, tools and armour can be enchanted at the enchanting table and the anvil, as in the original. Each takes what its vanilla counterpart takes.
- The Ultimate Bow, the Skate Bow and the gadgets that wear out (the Ultimate Fishing Rod, Ray Gun, SquidZooka, Thunder Staff, Wrench, Sifter and NetherLost) take Unbreaking and Mending.
- OreSpawn gear dropped by Godzilla, the Kraken, the Basilisk, the Cater Killer, the Cephadrome and the Trooper Bug rolls the original mod's enchantments, with its chances and levels. Before, it always dropped plain.
- Gear that enchants itself, like the Ultimate tools and armour, checks for its own enchantments the way the original did, so a boss drop that rolled other enchantments still gets its own.
- The Emerald Pickaxe gives itself Silk Touch, as in the original, instead of Fortune.
- Big creatures' hit boxes keep up with them when they fly, jump, burrow or turn, and when a rider gets on. Before, they could trail a step behind, so a hit could miss.
- A flying Prince can be hit where it is. Before, its hit boxes stayed where it took off.
- A Dragon, Baby Dragon, Prince, Frog, Cricket or worm with the NoAI tag no longer takes off, flies, jumps or burrows on its own.
- A name-tagged Urchin no longer vanishes in daylight.
- A tamed Girlfriend no longer takes horse or wolf armour, which she could not show and you could not get back.

**The new look**

![Seven armour sets, classic and modern](https://raw.githubusercontent.com/CoolFreeze23/Orespawn/v1.21.1-2.0.0-beta.13/phase_g_reports/release_media/2.0.0-beta.13/armour_sets_1.jpg)

![The other seven sets, classic and modern](https://raw.githubusercontent.com/CoolFreeze23/Orespawn/v1.21.1-2.0.0-beta.13/phase_g_reports/release_media/2.0.0-beta.13/armour_sets_2.jpg)

*All 14 sets on armour stands, seven to a picture. Top: classic. Bottom: modern.*

![Three sets close up, classic and modern](https://raw.githubusercontent.com/CoolFreeze23/Orespawn/v1.21.1-2.0.0-beta.13/phase_g_reports/release_media/2.0.0-beta.13/armour_trio.jpg)

*The Royal Guardian, Queen and Ultimate sets close up. Top: classic. Bottom: modern.*

![The same three sets from behind](https://raw.githubusercontent.com/CoolFreeze23/Orespawn/v1.21.1-2.0.0-beta.13/phase_g_reports/release_media/2.0.0-beta.13/armour_trio_back.jpg)

*The same three in the modern style, from behind.*

![Horse armour](https://raw.githubusercontent.com/CoolFreeze23/Orespawn/v1.21.1-2.0.0-beta.13/phase_g_reports/release_media/2.0.0-beta.13/horse_armour.jpg)

*Horse armour for all 14 sets.*

![Wolf armour](https://raw.githubusercontent.com/CoolFreeze23/Orespawn/v1.21.1-2.0.0-beta.13/phase_g_reports/release_media/2.0.0-beta.13/wolf_armour.jpg)

*Wolf armour for all 14 sets.*

![The eight spears on armour stands](https://raw.githubusercontent.com/CoolFreeze23/Orespawn/v1.21.1-2.0.0-beta.13/phase_g_reports/release_media/2.0.0-beta.13/spears.jpg)

*The eight spears, each stand in its tier's armour where the tier has a set. Top: Ultimate, Emerald, Amethyst, Ruby. Bottom: Crystal Stone, Crystal Wood, Tiger's Eye, Crystal Pink.*

![The Ultimate spear in hand](https://raw.githubusercontent.com/CoolFreeze23/Orespawn/v1.21.1-2.0.0-beta.13/phase_g_reports/release_media/2.0.0-beta.13/spear_in_hand.jpg)

*The Ultimate spear in hand, all eight in the hotbar (with Mounts of Mayhem).*

![The new items](https://raw.githubusercontent.com/CoolFreeze23/Orespawn/v1.21.1-2.0.0-beta.13/phase_g_reports/release_media/2.0.0-beta.13/new_items.png)

*The new items: horse armour in the top two rows, wolf armour below, the spears on the right.*

![An enchanting table offering Sharpness for a Ruby Sword](https://raw.githubusercontent.com/CoolFreeze23/Orespawn/v1.21.1-2.0.0-beta.13/phase_g_reports/release_media/2.0.0-beta.13/enchanting.png)

*A Ruby Sword at the enchanting table. Before this version, OreSpawn's gear got no offers.*

![Royal Altars on a hillside and by a lake, the original's flat pad and fitted to the land](https://raw.githubusercontent.com/CoolFreeze23/Orespawn/v1.21.1-2.0.0-beta.13/phase_g_reports/release_media/2.0.0-beta.13/altar_ground.jpg)

*Royal Altars on a hillside and by a lake. Left: the original's flat pad, cut into the hill or standing on a block of dirt. Right: fitted to the land around it, as this version builds them.*

![The same place at seed 1007 in the original, beta.12 and beta.13](https://raw.githubusercontent.com/CoolFreeze23/Orespawn/v1.21.1-2.0.0-beta.13/phase_g_reports/release_media/2.0.0-beta.13/triple_terrain.jpg)

*The same place at the same seed in the original (1.7.10, left), the released beta.12 (middle) and this version (right): Utopia 8,000 blocks east of the spawn and Mining 2,000 blocks east. The land is the original's again; trees, plants, ponds, caves and buildings are placed by chance and fall differently.*

![The OreSpawn Visuals screen](https://raw.githubusercontent.com/CoolFreeze23/Orespawn/v1.21.1-2.0.0-beta.13/phase_g_reports/release_media/2.0.0-beta.13/visuals_screen.png)

*The OreSpawn Visuals screen, opened with O or the Config button in the mod list.*

**Good to know**
- Mounts of Mayhem 1.9.8 is optional and only needed for the spears. On a server, install it on the server and on every client. Without it there are no spears and nothing else changes.
- Horse armour, wolf armour, spears, the dogs' modern look and the altars' fitted ground can each be turned off under `[modern]` in `config/orespawn-common.toml`, or every modern feature at once with `enabled = false`. The armour style is your own setting, in the Visuals screen or `config/orespawn-client.toml`.
- Doggy Talents Next dogs can't wear the new wolf armour (they take only vanilla's): a training treat on a wolf wearing one drops the armour first. A Doggy Talents dog shows a change of style after a restart.
- Mobs whose armour GeckoLib draws, like Iron's Spells' wizards, keep the classic look in both styles.
- An Emerald Pickaxe from an earlier beta keeps its Fortune I and gains Silk Touch I.
- In Utopia, the Village and Mining the caves and ravines are made by the original's rules (as many, as deep, lava near the bottom) but fall in other places than the original's, and trees, plants, ponds and other things placed by chance land elsewhere too. Where one of them crosses a chunk edge, which lands last follows the order the game generates the chunks in, so two worlds of one seed can differ slightly there.
- Mining has more trees than the original (41 against 25 in the 400 x 400 blocks round the spawn): Minecraft 1.21's trees grow in spots the original's needed more room for. Utopia has about a fifth less tree cover than the original.
- Works with your existing worlds. In Utopia, the Village, Crystal and Mining, land an earlier beta generated keeps its terrain and everything built in it, from the deepest mine to the highest peak, and new land gets the original's, so expect a step or a cliff where they meet. Trees, altars and platforms already generated stay as they are.
- To get the original terrain everywhere in one of those dimensions, take out anything worth keeping there, close the world (with nobody left in that dimension) and delete the dimension's folder inside the world folder (`saves/<your world>/` in single player, `world/` on a server): `dimensions/orespawn/utopia`, `dimensions/orespawn/village`, `dimensions/orespawn/crystal` or `dimensions/orespawn/mining`. It generates fresh the next time anyone goes there.

**Install:** put `orespawn-1.21.1-2.0.0-beta.13.jar` in `mods/` (NeoForge 21.1, Minecraft 1.21.1, GeckoLib 4.7 or newer) and take the beta.12 jar out. For the spears, add Mounts of Mayhem 1.9.8.

### In more detail

#### What beta.13 is

beta.13 is beta.12 plus the modern armour style, horse armour, wolf armour, the Doggy Talents Next dog texture and spears, with enchanting that works for OreSpawn's own gear and the bosses' gear drops enchanted as in the original. Alongside: hit boxes that keep up with their creatures, mobs without AI that stay still, and fixes to the Urchin and the Girlfriend. It also brings back the original's terrain in Utopia, the Village, Crystal and Mining, down to their water, caves and bedrock, tints the fruit trees' leaves again, and fits the Royal Altars to the land around them.

#### What changed

**The armour style.** Every armour set now comes in two looks. The classic one is the original's, untouched. The modern one has real 3D pieces on the body, new textures with LabPBR maps for shader packs, and new icons. The setting is per player and never sent to the server, so on a server everyone picks their own. It changes at once, without reloading resources.

**Horse and wolf armour.** Each set's horse and wolf armour gives the set's chestplate armour value, toughness and knockback resistance, never wears out and stacks to one. Wolf armour cuts the damage a wolf takes, like a player's armour, instead of soaking it up until it breaks as vanilla's does. The Experience pieces are made with bottles o' enchanting around the Emerald one, and the Ultimate pieces are filled row by row like the Ultimate chestplate (iron, titanium, uranium). The King drops the Royal Guardian pieces and his challenge tower's chest holds them.

**Spears.** With Mounts of Mayhem installed, each tool tier has a spear. Hold right click to charge, as with that mod's spears; held, a spear reaches five blocks. A melee hit or a charge hit wears it a point, and it repairs with its tier's material. Spears take Unbreaking and Mending.

**Enchanting.** In Minecraft 1.21 the enchanting table and the anvil decide by item tags, and none of OreSpawn's items were in any, so they took nothing. They are in their vanilla classes' tags now: each sword takes what a vanilla sword takes, each pickaxe what a pickaxe takes, each helmet what a helmet takes, and so on. The Ultimate Bow and the Skate Bow keep the original's enchantability of 50.

**Boss loot.** The six bosses' OreSpawn gear rolls each enchantment on its own, as the original's drop code did: most at one chance in six with a level from 1 to 5, Unbreaking at one in two from 2 to 5, so a piece can carry none or several. Pieces the original dropped plain still drop plain. The gear that enchants itself adds its own set when the one enchantment it looks for is missing (for armour: when none of its own kinds is there), exactly as the original checked.

**The terrain.** Utopia, the Village, Crystal and Mining are built as the original built them, checked against a real 1.7.10 server at the same seed: the same hills, the same lakes full of water, sand, gravel and clay on the lake floors, caves and ravines as common and as deep as the original's with lava near the bottom, bedrock at Y0. Mining has its bare stone peaks and its blue sky, and its structures and the Village's spawn as often as the original's on this terrain.

**The Royal Altars.** A Royal Altar used to stand on a flat pad cut into the land: a sheer wall on a hillside, a block of dirt where the land falls away. Now the land round it is sloped to meet it, a block down for every block out, with grass on top; trees and their trunks are left standing. `altarTerrain = false` under `[modern]` gives the original's flat pad.

**Worlds from earlier betas.** Land an earlier beta generated in these four dimensions keeps everything, from its deepest mine to its highest peak: the dimensions keep the height range they had, Y-64 to 320, and the original's world is built inside it, from Y0 up on solid bedrock.

#### How to install

Put `orespawn-1.21.1-2.0.0-beta.13.jar` into the `mods` folder of a NeoForge 21.1 instance for Minecraft 1.21.1 together with GeckoLib 4.7 or newer, and take the beta.12 jar out; MultiHitboxLib and Databuddy are bundled in the jar. For the spears, add Mounts of Mayhem 1.9.8 (on a server: on the server and every client). Existing worlds carry over.

## Part two — for the modder

### The modern armour style (MOD-039)

Both looks ship inside the port; nothing is a resource pack or a companion jar. `[client] armourStyle = "modern" | "classic"` in `config/orespawn-client.toml` (`danger.orespawn.client.armour.ArmourStyleConfig`, `ModConfig.Type.CLIENT`, default modern) is read at every use, so a change shows at once: the worn model (an `IClientItemExtensions` per set hands out the set's `ArmourModel` or the classic model), the worn texture (`ItemOreSpawnArmor.getArmorTexture`: the modern layer, or null for the classic one) and the icon (the item predicate `orespawn:armour_style`, one overrides entry in each classic item model pointing into `models/item/modern/`). It is a cosmetic and sits outside `[modern] enabled`, which governs gameplay. The OreSpawn Visuals screen sets it; the O key (category OreSpawn, in game) and the mod list's Config button open it.

The pieces are built by the client's geometry builder from `assets/orespawn/armour_geo/` on the player's armour boxes. `ArmourModel` draws its rebuilt faces itself, as `ModelPart.render` and `Cube.compile` do, so a renderer that replaces cube drawing cannot put them on its own UV. A wearer whose armour model moves or grows those boxes (a zombie villager's taller head and robe, Guard Villagers' guards, piglins) gets each part laid onto its own box, so the piece sits where vanilla puts the classic one; a smaller box keeps the player's fit, so a drowned's chestplate never lands on its leggings. The modern texture is handed out only with the modern model, to the wearer and stack the model hook has just been asked about: a caller that asks for the texture alone and draws its own model gets the classic texture (or, in the modern style, the dog texture below). Four access-transformer entries open `ModelPart`'s `Cube.polygons`, `Polygon` (and its constructor) and `Vertex` for the box-UV rebuild; they are the change's only vanilla patch.

The six base armours (heavy plate, fine plate, light plate, gilt, mail, hide) are generated from vanilla's humanoid box sizes, each set its base plus its own details, its colours taken from its classic texture. 492 shipped files (geometry, textures with LabPBR maps, icons and models, the horse, wolf, dog and spear assets below included) are pinned by SHA-256 in `tools/armour_asset_pins.json`, with the 140 classic files they leave untouched; the asset audit's check 9 compares them on every build. The build also runs `armourModelProbe`: every piece drawn as the armour layer draws it, each vertex on its box UV with the light, overlay and colour passed, every face turned as vanilla turns it, each part's base box on the wearer's own armour box, for the player, an armour stand, a zombie villager, a guard-shaped model and a piglin, grown and as babies.

The sleeves', leggings' and boots' side faces took their colours from the classic texture back to front, so a stripe toward the front of a classic sleeve showed toward the back; they follow the classic look now (ITEM-072). Eleven sets changed, their dog textures with them; the Queen, Lava Eel and Peacock sets look as before.

Known limits: a piece whose geometry fails to build (a broken resource pack) draws its classic model and texture but keeps its modern icon; mobs whose armour GeckoLib's item armour layer draws (Iron's Spells' wizards, for one) wear the classic look in both styles, since that layer always draws the material's classic texture; on a wearer whose armour model has boxes of another size than the player's, the pieces are drawn as on the player; on a zombie villager the leggings' details (knee plates, fins, belt details) show through the robe, under which vanilla hides its leggings.

### Horse armour (MOD-040) and wolf armour (MOD-041)

Each set's horse and wolf armour is vanilla's `AnimalArmorItem` on the set's own armour material (`EQUESTRIAN` and `CANINE`): the body value the material already carries (the chestplate's), its toughness and knockback resistance, no durability, a stack of one. Recipes: vanilla's leather horse armour shape (`X X / XXX / X X`) and wolf armour shape (`X   / XXX / X X`) with the set's material item; the Experience pieces as its armour (eight bottles o' enchanting around the Emerald piece); the Ultimate pieces filled row by row as the Ultimate chestplate's recipe (iron, titanium, uranium). The Royal Guardian pieces have no recipe: a loot modifier on `orespawn:entities/the_king` drops them beside the chestplate at the same odds, and the King's challenge chest holds them beside it.

The horse's own armour layer draws ours from the material's texture (`textures/entity/horse/armor/horse_armor_<material>.png`, 64 × 64). The wolf needed more: vanilla's `Wolf.hasArmor()` asks for `Items.WOLF_ARMOR` alone, and it gates the wolf armour layer, the shears and the absorb-until-broken damage. Ours has its own layer on every wolf renderer (the `WOLF_ARMOR` model layer, the item's own 64 × 32 texture), one interaction event that lets the owner of a tamed, grown wolf put it on, and the shears take it off on vanilla's terms (the owner, a point of wear on the shears, vanilla's sound, the armour dropped). With `hasArmor()` false the wolf's damage is reduced by the body slot's armour, toughness and knockback resistance; nothing is absorbed and nothing wears.

`[modern] horseArmour` and `wolfArmour` (default true) turn the recipes and the King's drop (the `orespawn:modern` load condition), the challenge chest's piece and the creative-tab entries on and off; the items stay registered either way. A change reaches the recipes and the drop on `/reload` or a restart, the creative tab when the world is joined again, the chest in towers built after it.

Known limits: the horse texture covers the throat and leaves the belly, the ears and the mane bare, as vanilla's horse armour textures do. Doggy Talents Next's dogs cannot wear the new wolf armour (`Dog.handleSetWolfArmor` takes vanilla's item only); its training treat carries only vanilla's armour over, so a treat on a wolf wearing ours drops ours, and the next treat trains it. A summoned wolf (Ars Nouveau's, which vanishes with what it wears) never takes it; shears take it off any wolf.

### The Doggy Talents Next dog texture (MOD-042)

Doggy Talents Next draws OreSpawn armour on its dog model with the texture it asks the item for. In the modern style that caller now gets a 64 × 32 dog texture per set (the modern base in the classic layer-1 layout, with LabPBR maps), and the model hook hands dogs the classic model, so Doggy Talents Next's own helmet option draws its helmet with the dog texture. `[modern] dogArmour` (default true) turns it off. Known limits: Doggy Talents Next keeps each piece's texture for the session, so a change of style or key reaches dogs after a restart; its legacy armour render (`use_legacy_dog_armor_render`) draws its own textures; a heavy-class helmet's closed visor covers a dog's face; its leggings read the arm, helmet and boot regions too, as with the classic texture.

### Spears (MOD-043)

Built on Mounts of Mayhem's abstract `SpearItem` through its six-argument constructor, registered by one class (`danger.orespawn.compat.MayhemSpears`) only when `mounts_of_mayhem` is loaded; nothing of that mod is touched otherwise. The dependency is optional, `[1.9.8,)`, the tested version 1.9.8, with a log line at registration when a newer one is loaded. A world keeps its OreSpawn spears only while the mod is installed, and a server and its clients both need it.

One rule for the eight tiers (`SpearTier`): durability the tier's uses; attack damage the tier's attack bonus + 1 at the mod's attack speed (−2.8); the charge's base that same damage, and its speed multiplier, mounted bonus, hold and reload the mod's own row for the vanilla tier at the tier's mining level.

| Tier | Attack damage | Row | Durability |
|---|---:|---|---:|
| Ruby | +17 | diamond | 1,500 |
| Amethyst | +12 | diamond | 2,000 |
| Emerald | +7 | diamond | 1,300 |
| Ultimate | +37 | netherite | 3,000 |
| Crystal Pink | +8 | iron | 1,100 |
| Tiger's Eye | +9 | iron | 1,600 |
| Crystal Wood | +3 | wooden | 300 |
| Crystal Stone | +6 | stone | 800 |

The charge cycle is 1.9.8's as it is (the hold limit, the reload after a hit, one hit per charge). A melee hit wears a spear a point (and 1.9.8's charge hit does too); it repairs with the tier's material and plays the mod's hit sound, looked up by its id. The spears are in `#minecraft:spears` (the mod's five-block reach while one is held) and, as the mod's own, in `#minecraft:enchantable/durability` (Unbreaking, Mending, Curse of Vanishing), both as optional tag entries; the mod's Lunge enchantment is refused (`supportsEnchantment`), since only the mod's left-click jab reads it and these spears have none. Recipes: the mod's diagonal with the tier's material and two sticks (crystal sticks for the crystal tiers), the Ultimate one running titanium, uranium and iron down the diagonal like its sword, behind `neoforge:mod_loaded` and `orespawn:modern` (`[modern] spears`, default true, which also gates the creative-tab entries). In hand they use the mod's 3D spear of their row as the parent model with their own 32 × 32 texture and 16 × 16 icon, with LabPBR maps; nothing of the mod's assets is copied.

### Enchanting (ITEM-073)

1.21 decides what an item takes by tags (`supportsEnchantment`, `isPrimaryItemFor`, `getEnchantmentValue`), and the port put its items in none. Now: `minecraft:swords` (23 items), `axes`, `pickaxes`, `shovels` and `hoes` (8 each, the crystal shovels with the shovels); each armour piece in `enchantable/head_armor`, `chest_armor`, `leg_armor` or `foot_armor` (14 each) and in `equippable` (56), not in vanilla's four armour slot tags; `enchantable/durability` for the 56 armour pieces, the Ultimate Bow, the Skate Bow and the damageable gadgets (the Ultimate Fishing Rod, Ray Gun, SquidZooka, Thunder Staff, Wrench, Sifter and NetherLost). The Ultimate Bow and the Skate Bow keep the original's enchantability of 50 (orig UltimateBow.java:83-85, SkateBow.java:87-89) and take only the durability set, as in 1.7.10; Power, Punch, Flame and Infinity stay out. The self-enchanting swords, tools and armour keep their own enchantments and join their class's tags. Not changed: the Spider and Ant Robot Kits and the Zoo Keeper, which took Unbreaking from a book in 1.7.10, take nothing here, and the Creeper Launcher has no durability.

### Boss loot and the self-enchanting gear (ITEM-075, ITEM-076)

Read from the 1.7.10 jar (`dropFewItems` of each boss, javap): every enchantment call on a boss's gear sits under its own `nextInt(K) == 1` check, K six or two, at a level `A + nextInt(B)` or a constant; 516 checks over the six bosses, no other rule. The 77 OreSpawn gear entries the original enchanted now carry one `minecraft:set_enchantments` per die, under a `minecraft:random_chance` of 1/K, the level a constant or `uniform`; the 13 it dropped plain stay plain; `enchant_randomly` is gone from every OreSpawn entry of the six tables. Swords: Sharpness, Bane of Arthropods, Knockback, Looting and Fire Aspect 1/6 at 1-5, Unbreaking 1/2 at 2-5; helmets: the four protections 1/6 at 1-5, Respiration 1/6 at 1-2 or 1-5, Aqua Affinity 1/6 at 1-5, Unbreaking 1/2; chestplates and leggings: the protections and Unbreaking; boots: Feather Falling 1/6 at 5-9 and Unbreaking; pickaxes: Efficiency and Fortune 1/6 at 1-5 and Unbreaking; shovels, axes and hoes: Efficiency and Unbreaking. Levels above the vanilla maximum are the original's and kept.

The self-enchanting classes now key on their own enchantment as the original's did (`OreSpawnEnchantHelper.level`, `addIfAbsent`): the Experience Sword, NetherLost and Poison Sword on Sharpness, the Nightmare Sword on Knockback, the Ultimate Sword on Looting, the Ultimate Axe, Shovel, Hoe and Pickaxe on Efficiency, the Ultimate Fishing Rod on Unbreaking, the Ultimate Bow on Infinity, the Emerald Pickaxe on Silk Touch; the armour (`ItemOreSpawnArmor`) while none of its eight (the four protections, Respiration, Aqua Affinity, Unbreaking, Feather Falling) is on it. A piece whose dice rolled its key keeps its dice alone, as in 1.7.10; otherwise it adds its set beside what it rolled, an enchantment already present keeping its level. Before, any enchantment at all stopped the set. The Emerald Pickaxe gives itself Silk Touch I, the original's `field_77348_q`, instead of Fortune I (ITEM-076); one from an earlier beta keeps its Fortune I and gains Silk Touch I.

Declared: a sword whose two Sharpness dice both succeed keeps the first die's level, since 1.21 holds one level per enchantment (1.7.10 carried both entries); Aqua Affinity's levels above I, as the original rolled them, speed mining under water further in 1.21, where 1.7.10's Aqua Affinity was on or off; the vanilla gear in these tables keeps its one random enchantment.

### Hit boxes after late moves, and mobs without AI (TEST-020)

A multipart creature's hit boxes were placed during its AI step, so a creature that moved again later in the same tick carried them a move behind: a flying Dragon by its flight step, a Frog or Cricket by its jump, a burrowing worm, a Leaf Monster snapping to its block, Robot 1's spin, and every mob's body turn. A flying Prince skipped that step altogether, so once it took off its server-side hit boxes stayed where it had been. The boxes are now placed again after the creature's whole tick when it has moved, turned or changed size since, and again after a rider takes its seat.

A Dragon, Baby Dragon, Prince, Frog, Cricket or worm spawned without AI (the NoAI tag) could still take off, fly, jump or burrow on its own, after its hit boxes had been placed for the tick; the hit box sweep spawns its mobs that way and caught it now and then. These mobs now hold still without AI, as vanilla mobs do. With AI nothing changes: a rider still flies a Dragon or a Prince, the Dragon's passive heals still run and the large worm's brood still spawns. For development, `-PgametestOrigin=x,z` lays the game-test grid out from that origin (`GameTestOriginMixin`) in a world of its own under `build/gametest-origin/`, to repeat a run at the grid origin of one that failed.

### The Urchin and the Girlfriend (ENT-S-178, ENT-S-179)

A name-tagged Urchin, or any Urchin that must persist, could still vanish in daylight: the original skips its daytime despawn for such an Urchin (orig Urchin.java:97-99), and the port had lost that check. A tamed Girlfriend took horse and wolf armour into her body slot, where nothing draws it and an empty hand could not take it back; she refuses it now and it stays in the player's hand. Not changed: her other differences from the original (what she wears, the dandelion, the empty hand) are left for a later version.

### GitHub #6 and #7: the reported glitches, against the 1.7.10 source

**The terrain (WGEN-087).** The four dimensions stood on `orespawn:inland`, a copy of 1.18's overworld router. The original built them on its copy of the 1.7.10 overworld generator (`ChunkProviderOreSpawn.func_147423_a`, the same in ChunkProviderOreSpawn2, 3 and 5), one biome each: Utopia and the Village at BiomeGenBase's default height (0.1, 0.2), Crystal (0.1, 0.5), Mining extreme hills (1.0, 0.5). `LegacyTerrainNoise` (`orespawn:legacy_terrain`) is that density: vanilla's `BlendedNoise` (1.7.10's limit and main noises, octaves, scales and smear) plus the 16-octave depth noise, the biome's height and variation through the original's parabolic weighting (one biome, in its float steps) and the top slide, seeded from `LegacyRandomSource(seed)` in the original's draw order, so a seed gives its 1.7.10 world's terrain. `legacy_utopia`, `legacy_crystal`, `legacy_extreme_hills`: the 1.7.10 density interpolated between corners as the original, no aquifers, no ore veins. Heights over a 32,000-block square: Utopia 56 to 90, Crystal 52 to 109, Mining 69 to 124 (the same code gives vanilla 1.7.10's plains 60 to 75 and ocean floors about 47). Crystal's shallow-sea fill (written for the 1.18 noise's oceans) is gone; its lake floors below Y62 are crystal stone, as the original's.

**Utopia's trees and altars (WGEN-081 to WGEN-084).** The original's grove and apple-tree scans run from Y100 down while air, so a site under an earlier tree fails: the port's scans now stop at earlier trees. The Royal Altar needs the original's `quickReallyBigSpaceCheck` (the 60 x 60 plane eight blocks above the grass, terrain and trees), stands with its pad's corner on that grass, and places after Utopia's trees (`utopia_temple_king_altar` / `utopia_temple_queen_altar` in top_layer_modification), so its hall clears them as the original's did. The royal trees place after the huge trees (`utopia_royal_tree_king` / `utopia_royal_tree_queen`). Utopia and the Village have no vanilla trees (treesPerChunk -999); Chaos has one a chunk (`setChaosCreatures`). The old structure ids stay registered so older worlds load.

**The Crystal platforms (BUG-021) and the dungeons (WGEN-088).** A 1.21 decoration step writes one chunk round; a Fairy Castle Tree reaches 25 to 42 blocks. `DeferredWrites` keeps each far block, chest and spawner for its chunk (saved with the world) and lays it at that chunk's decoration, or on the server tick once an already-generated chunk is loaded. The dungeons of Utopia, Mining and the Village were built in the surface step (its own chunk only, and before any lava existed for the ruby dungeon's scan); they now place after decoration, as the original's OreSpawnWorld.generate ran after populate.

**The cows (ENT-S-190, WGEN-085, ENT-S-191).** red_cow, spider_driver and tshirt were MobCategory.MISC; `MobSpawnSettings.SpawnerData` turns a MISC entry into a pig. They are CREATURE / MONSTER, with placements: the three cows EntityAnimal's rule in play and any ground at chunk generation (1.7.10's chunk spawner asked only for solid ground); the Spider Driver its robot bypass or the dark; the T-Shirt its own rule. Utopia's, the Village's and Chaos's lists are BiomeGenUtopianPlains' (the ambient animals ambient, Utopia without monsters). `orespawn:remove_disabled_spawns` leaves a disabled mob out of every list at server start.

**The renderers (ENT-S-182 to ENT-S-188, ENT-S-162).** The Kyuubi (blend, 180-degree turn), the Gamma Metroid (blend), the Triffid (-90-degree turn); the Princes' and the Princess's blended wings (and her orbs) at 0.75 grey, 0.55 alpha in a second pass (`WingMembraneLayer`, the rigs' `secondPass`); the Ghosts' 0.75 grey at 0.25 alpha; the Ostrich's and the Stink Bug's halved babies; the Princess's attack texture; the Spider Driver on vanilla's SpiderRenderer.

### The terrain against the original, the leaves, the weights and the altars' ground

**The terrain against the real 1.7.10 (WGEN-093, WGEN-092).** The 1.7.10 terrain of WGEN-087 was checked against the original itself: a 1.7.10 server running OreSpawn 1.7.10-20.3 with a small helper mod that generates the four dimensions through their own chunk providers and dumps each column, at seed 1007, against the port at the same seed and chunks. The density was the original's (the ground's top identical in 96 to 98 % of the columns, the Village's 74 % for its villages), but the water, the surface, the depth and the caves were 1.18's: aquifers left most low ground dry (10 % of Utopia's under water against the original's 91 %), the world reached down to Y-64, the surface came from surface rules (Mining without its bare stone), the sand, gravel and clay discs were missing, and Mining's sky was grey. Now there are no aquifers (still water below Y63, as the original's terrain pass), and `LegacySurface` is the original's genBiomeTerrain block for block (grass and dirt, BiomeGenHills' stone where its noise is over 1.0, Crystal's crystal grass and stone) with the chunk random seeded as provideChunk and the surface noise rebuilt from the four simplex generators the terrain noise draws (`LegacyTerrainNoise.surfaceNoise`, with the original's floor and summation order). The original's world runs from Y0 to 256 on its bedrock, inside the build range the dimensions always had (Y-64 to 320): solid bedrock below Y0 in new chunks, nothing above Y256. The carvers carry the original's numbers: `orespawn:legacy_cave` in one chunk in seven, rooms half as tall, lava at Y9 and below, a stretch left out where water stands on its shell; `orespawn:legacy_canyon` in one in fifty, Y20 to 67; none in Crystal. Vanilla's sand, clay and gravel discs come at the original decorator's counts, and Mining has a 0.8/0.01 climate, sky 0x78A7FF and fog 0xC0D8FF. A test compares 46,080 columns with the original's chunk providers' dump: the terrain, the surface noise and every surfaced block are identical. The Crystal Fairy Trees' rarity (WGEN-092) is the original's: 1.7.10 built one Fairy Tree and no Fairy Castle Tree in the same 2,401 chunks, at a site where the port's site test passes too.

**The leaves (BUG-047).** The apple, cherry, peach, scary and experience leaf models take vanilla's tinted leaves model as parent, so the foliage colour registered for them reaches the blocks and their items.

**Mining's and the Village's structure weights (WGEN-091).** On the final terrain the builders find their sites at new rates (Mining's dungeons every time, the Leonopteryx nest 85 %, the Damsel 93 %, the Spider Hangout 73 %, the Red Ant Hangout 79 %). The weights are re-derived from them with WGEN-080's formula: Mining's seven 809 (were 858), the Damsel and the Red Ant Hangout 2252 (2520), the Spider Hangout 1608 (1800). Against a simulation of the original's rules over 40 worlds, every structure's count lies inside two sigma.

**The Royal Altars' ground (MOD-044, `modern.altarTerrain`).** The original cuts the altar's 61 x 61 envelope out of the land and lays its pad on a nine-block skirt of dirt. That leaves cut walls up to ten blocks high on a hillside and a dirt plinth where the land falls away (at seed 1007, eleven altars: walls 1 to 10 blocks, edges 2 to 10 blocks over the ground). With the option on, the altar's piece reaches 16 blocks past the envelope. After the build it brings each column within one block per block of distance of the pad's level: a hillside cut back with grass on the cut, a drop banked up with dirt under grass, the strip round the pad level with it, the skirt down to the ground. It works from the surface the chunk's decoration has reached. Plants and bushes on a cut go with it, a canopy or a branch over it stays, and a column with a trunk standing on it, or another build, keeps its ground. Where the altars go is unchanged, and the piece keeps its random, so the altar is the same block for block. Each altar keeps the shape it was laid out with. With the option off, or in classic mode, the altar gets the original's flat pad.

**The height range kept (WGEN-096).** A world from an earlier beta has these dimensions saved from Y-64 to 320 (Mining to 192); a dimension whose range shrank would drop what lies outside it, unread. So the four keep that range and build the original's world inside it. 1.21 resolves every placed feature's and carver's heights in the generator's range, the noise's (Y0 to 256) inside the build range, so the data-driven heights stand as before; `GenerationRange` keeps OreSpawn's own trees, dungeons, altars, hives, lakes and apple trees to the same range, new chunks get solid bedrock below Y0, and the springs are placed at absolute heights. Mobs spawn in new land from Y0 up, not in the bedrock under it, and old Mining land above Y192, which earlier betas never saved, takes Mining's own biome as it loads (no rain or plains mobs over old peaks). Three old dev worlds loaded and saved with it keep every section at both ends, and every height reads true.

**The original's springs, Mining's decoration, OreSpawn's ore pass and the colours (WGEN-099, WGEN-102, WGEN-097, WGEN-101).** Read from the 1.7.10 jar and OreSpawn's source. BiomeDecorator plants 50 water springs a chunk at `nextInt(nextInt(248) + 8)` and 20 lava springs, each in stone only (WorldGenLiquids): `spring_water_1710` and `spring_lava_1710` now do, the Village has its lava springs, and the Village's and Mining's lava lakes keep the original's gate, under Y63 or one in ten (`orespawn:below_or_chance`). Their lakes sink through air to the ground first, as WorldGenLakes does, so one drawn over Mining's hills lies on them; as in the original, a lake is skipped where the Village's villages reach its chunk (a structure in `#orespawn:lakes_avoid` referencing the chunk or its east, south or south-east neighbour), and only lava lakes are walled in stone. The original's Mining is vanilla's extreme hills: a tree in one chunk in ten (two in three a spruce, else an oak, a tenth of those big), two patches of dandelions or poppies and one of grass at the decorator's heights and spreads (`hills_decoration_1710`), and OreSpawn's falls of water and lava on the hillsides (`lava_and_water`). A spring or a fall runs once its land ticks; the original's ran as the land was made. OreSpawn's own ore pass (ChunkOreGenerator) ran as the chunk was built, before the biome's ores, and centred each vein eight blocks in from where it drew it, writing only into the chunk being built, so the part past the chunk's edge never appeared: the port now lays that vein (`orespawn:chunk_ore`, `vein_count`'s `chunk_window`, the spawn ores' `chunk_veins`) first in Utopia's, the Village's and Mining's ore step, in the pass's order, which brings their OreSpawn ores from two to three times the original's to the original's (Utopia's amethyst 1.7 a chunk against 1.6, salt 4.0 against 3.7; Mining's emerald 9.2 against 8.9, gold 14.3 against 14.2), and adds the pass's diamond, emerald, gold and ruby block boosts to Utopia and the Village. Utopia's colours are vanilla's for temperature 0.7 and rainfall 0.5 (sky 0x7AA6FF, grass 0x8CBD5F, foliage 0x70AB38), Crystal's for the 0.8 and 0.01 its world provider sets as the world loads (sky 0x79A7FF, grass 0xB1B762, foliage 0x9DA43B), both with vanilla's water.

**The original's ores (WGEN-095).** Utopia, the Village and Mining place BiomeDecorator.generateOres's set: dirt and gravel in veins of 32 at Y0 to 255, coal in veins of 16 to Y127, iron to Y63, gold to Y31, redstone and diamond to Y15, lapis round Y15, each at the original's count, replacing stone; Mining adds the extreme hills' single emeralds and silverfish stone. Against the real 1.7.10 world at the same seed the counts a chunk agree (Utopia's coal 165 against 173, iron 93 against 93, redstone 26 against 26), and copper, granite, diorite and andesite are gone.

### Tests and tooling

Two tests built a Nightmare Rookery and required a Pitch Black spawner, which the rookery places only on spires tall enough by chance (about 0.4 % of rookeries have none); the Islands ruby dungeon test required ruby ore, a 1-in-20 roll per wall cell. These builds now come from a fixed seed, so each run builds the same structure; the dungeon spawner block and world generation still use the world's own random source, and the structures' odds are unchanged. The Basilisk maze test counts Basilisks by class, so their hit box parts are no longer counted with them. `deployToPrism` copies the full jar, with Databuddy nested in it, instead of the `-slim` one. New tests pin the terrain against the original's dump (`gh6t_d`, 46,080 columns), the leaves' tint, the structure weights' site rates, the altars' fitted ground (through the piece's own code, fitted and classic on the same hillside) and the old-land repair.

### The pictures

In-game captures from the development client at 1600 × 900 in a superflat creative world at noon, the field of view at 40, with no shader pack. The horses and wolves are summoned without AI (a horse may still graze, as vanilla's do); the classic and modern pictures of each row are taken from the same spot with only the style switched. The enchanting table has bookshelves all round; the hint shown is its first offer's. The spears were captured with Mounts of Mayhem 1.9.8 installed. The Royal Altars are two of seed 1007's in Utopia, generated in a fresh world once with `altarTerrain` off and once on, seen from 50 blocks out and 58 up at the field of view's 70. The three-way terrain pictures are seed 1007 in OreSpawn 1.7.10-20.3 on Minecraft 1.7.10 (Forge 10.13.4.1614, in a world a 1.7.10 server generated), in the released beta.12 jar and in this version on NeoForge 21.1.248, each from the same place 28 blocks over the ground looking east, at noon, with 16 chunks and the field of view at 70.

### The harness

Checks on the release tree: drift 0; `RESULT: 0 error(s), 0 advisory(ies), 4 acknowledged; draw order: 104 shipped geo: 103 seam + 1 outside-seam`, `ARMOUR MODELS PASS: 56 pieces`, `G1 PARITY PASS` 2, 13 and 101 models, checked-in proof verified, `BUILD SUCCESSFUL in 7m 15s`; the gametest suite: `All 1422 required tests passed`, `BUILD SUCCESSFUL in 4m 23s` — 1357 tests at beta.12 plus those pinning the horse and wolf armour, the dog texture, the spears, the enchanting, the boss loot, the hit boxes and the mobs without AI, the Urchin and the Girlfriend, the 1.7.10 terrain column for column, Utopia's trees and altars, the Crystal platforms and the dungeons across chunk edges, the spawn lists, the altars' ground, the kept build range, and the original's springs, decoration, ore pass and colours. Databuddy 6.0.0.0 came from the copy nested in beta.12's jar, commoble.net's maven being unreachable at the cut; the jar nests the same file. The suite runs without Mounts of Mayhem; `-PgametestMayhem` runs the spears' rows with it.

### Third-party notices and credits

Unchanged from beta.5 (see `phase_g_reports/RELEASE_NOTES_2.0.0-beta.5.md`, "Third-party notices" and "Credits": MultiHitboxLib under LGPL-3.0, the MoreHitboxes portions under MIT, GeckoLib under MIT, Databuddy under MIT; the original mod by TheyCallMeDanger and the OreSpawn authors, 2013-2015), plus: Mounts of Mayhem ("Nautilus & Spears | Mounts of Mayhem", by Gospi, MIT) is an optional dependency, not shipped; OreSpawn's spears extend its `SpearItem` and take its spear models as parents, with their own textures.
