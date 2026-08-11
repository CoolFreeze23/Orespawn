# OreSpawn for NeoForge 1.21.1 — 1.0.0-beta.2

The classic 1.7.10 OreSpawn, rebuilt for modern Minecraft. This is the first
public build of a full parity port: every mob, boss, weapon, dimension, and
weird little quirk of the original, verified line-by-line against the
original's decompiled source — 630 audited findings, all closed.

## Highlights

- **All six dimensions are back**: Utopia, Mining, Village, Crystal, Islands,
  and Chaos — reached the classic way, by right-clicking the dimension ants
  (empty-handed!). First-visit arrivals land safely on the surface now.
- **The full boss roster**: The King (7,000 HP, as he should be), The Queen,
  Mobzilla, the Kraken and his thunderstorms, Mothra, the Basilisk, and the
  Prince line all the way to the "Prepare to die!" transformation.
- **The Big Bertha arsenal**: Big Bertha, Slice, the Royal Guardian Sword,
  Battle Axes, the Chainsaw, Ultimate gear, the no-charge Ultimate Bow, and
  the gemstone armory with authentic 1.7.10 power creep — over-enchanted
  drops (Unbreaking V, Feather Falling IX) included, on purpose.
- **Girlfriends, Boyfriends, and pets**: taming, moods, jealousy, dances,
  wet skins, the Frog Prince kiss, Cephadrome feed-to-ride, rideable
  AntRobots with the original hover physics, and the Valentine's Day
  girlfriend event (Feb 14 — you have been warned).
- **The world is alive**: 47 structures generate naturally — Basilisk Mazes,
  Nightmare Rookeries, Challenge Towers, the Ender Castle, village-border
  igloos, fairy trees — plus wild corn, tomatoes, and strawberries, anthills,
  troll-block ambushes, and the classic vanilla-ore boost veins.
- **Runs like 2026, not 2014**: modern data-driven recipes/loot/worldgen,
  a 150-test automated regression suite, and a performance pass that removed
  the original's worst per-tick costs without changing a single behavior.

## Crash fixes (things the original or early port builds broke)

- Fixed a server crash when a Rat spawned from a mob spawner.
- Fixed the WaterDragon crashing the game the moment it spawned.
- Fixed a world-corrupting crash when generating lakes in the Village and
  Mining dimensions (a latent vanilla bug the old code tripped).
- Fixed a startup crash from leftover references to removed content.
- Fixed the Prince's tame-transformation crashing when its owner logged out.
- Fixed Godzilla's landing shockwave damaging Creative and Spectator players.
- Fixed bosses one-shotting players into a corrupted death state.

## Parity fixes, by category

The port was audited finding-by-finding against the original's decompiled
source. Summarized here; the complete ID-by-ID record with citations lives in
[FIX_LOG.md](FIX_LOG.md) and [AUDIT_FINDINGS.md](AUDIT_FINDINGS.md).

- **Entities (250+ findings)**: stats reconciled to the original's real
  values, drop tables rebuilt item-for-item, spawn biomes and weights
  restored per-biome, AI goals un-pruned (village pathing, torch-stealing
  Aliens, jealousy, MoveIndoors), invented behaviors removed (rideable
  Camarasaurus, Basilisk slowness auras, boss bars the original never had).
- **Bosses (46)**: multi-part hitboxes with the original's far-forward head
  sidecars, PlayNicely support (quarter-size peaceful bosses), the Queen's
  happy discharge spawning Butterflies *and* Cockateils, the King's infamous
  ~300-random-item death shower — yes, that's original behavior.
- **Items & blocks (80+)**: break-XP through the modern enchantment pipeline,
  Crystal Furnace timings, duct-tape repair, zoo cages at the original's
  five sizes, wall-mountable repellents, the flat-100-damage Irukandji arrow.
- **Worldgen (70+)**: all ~25 missing structures ported byte-for-byte,
  the SpawnOres vein pool (105 egg types), per-biome ore rates with the
  LessOre config honored, dungeon loot with the original's exact dice.
- **Animations & rendering (40+)**: the 39-model animation mistranslation
  fixed, Mothra's 10× scale, crop cross-rendering, projectiles visible in
  flight, the Leonopteryx consolidated to one properly-scaled creature.

## Known issues

This is a **beta**. The game logic has been through the automated suite and
hands-on play, but much of the visual/audio polish is deliberately delegated
to community feedback — see [KNOWN_ISSUES.md](KNOWN_ISSUES.md) for the full
list, including every "is this supposed to look like that?" item we want
reports on. The headline item:

- **Fairy Castle Trees in the Crystal dimension can generate with sheared-off
  edges at chunk boundaries** (~1 in 25 Crystal chunks rolls one). This is
  the designated **first post-beta patch** — scoped, scheduled, and not a
  blocker. (BUG-021)

## What's next (the 2.0 teaser)

The parity pass replicated the original faithfully — bugs and all. The 2.0
backlog ([MODERNIZATION_NOTES.md](MODERNIZATION_NOTES.md)) is where the
deliberate improvements live: a procedural spider with real multi-part
hitboxes, bone-synced hitboxes for every giant boss, config toggles for the
original's roughest edges (the King's loot shower, relog-stable boss fights,
smooth tornado pulls), and the archived kyanite content branch as optional
new-content. None of it ships until the community has had its say on 1.0.

## Credits

Original OreSpawn by **TheyCallMeDanger** and the original authors
(2013-2015) — all rights to the original remain theirs. See the License &
Ownership section of the README. Ported and modernized for NeoForge 1.21.1.
