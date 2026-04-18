# OreSpawn — NeoForge 1.21.1 Port

> **A faithful, modernized port of TheyCallMeDanger's legendary 1.7.10 OreSpawn mod to NeoForge 1.21.1.**
> Bringing back the Titans, the Big Bertha, the six dimensions, and the bestiary that defined an era of Minecraft modding — rebuilt from the ground up against the modern data-driven worldgen, attribute, and registry pipelines.

[NeoForge](https://neoforged.net/)
[Minecraft](https://minecraft.net/)
[Status](https://github.com/CoolFreeze23/Orespawn/releases)
[License]()

---

## Quick Start

```bash
git clone https://github.com/CoolFreeze23/Orespawn.git
cd Orespawn
./gradlew build           # produces build/libs/orespawn-1.21.1-1.0.0-beta.1.jar
./gradlew runClient       # launch a dev client for testing
```

Drop the produced JAR into your NeoForge 1.21.1 mods folder. No additional dependencies required.

---

# Project Roadmap & Deep Backlog

> Cross-referenced against the [Namu Wiki Orespawn](https://en.namu.wiki/w/Orespawn) and
> [Added Mobs](https://en.namu.wiki/w/Orespawn/%EC%B6%94%EA%B0%80%EB%90%98%EB%8A%94%20%EB%AA%B9)
> reference pages — the most complete public catalogue of the original 1.7.10 mod.

## Completion Metrics (1.0.0-beta.1)

> Every "Wiki Total" below is a **strict integer count of entries explicitly enumerated**
> on the Namu Wiki [Orespawn](https://en.namu.wiki/w/Orespawn) and
> [Added Mobs](https://en.namu.wiki/w/Orespawn/%EC%B6%94%EA%B0%80%EB%90%98%EB%8A%94%20%EB%AA%B9)
> pages — no approximations, no tildes. The "Implemented" column counts how many of
> those exact wiki entries are present in our 1.21.1 codebase. Counts in the
> *Workspace Totals* footer below show our absolute registry sizes (which exceed the
> Wiki because we ship spawn eggs, food variants, projectiles, and bonus content).


| Subsystem                                                                 | Implemented | Wiki Total | % Complete |
| ------------------------------------------------------------------------- | ----------- | ---------- | ---------- |
| **Dimensions** (Utopia / Mining / Village / Crystal / Chaos / Danger)     | 6           | 6          | **100.0%** |
| **Armor Sets** (Wiki §6.2)                                                | 10          | 10         | **100.0%** |
| **Top-tier Weapons** (Wiki §6.1)                                          | 24          | 25         | **96.0%**  |
| **Bosses & Titans** (Wiki §13 + §14 + §15, excluding Scorpion sub-mob)    | 13          | 13         | **100.0%** |
| **Hostile / Neutral Mobs** (Wiki mobs page §2–§13.8 minus bosses & tames) | 53          | 56         | **94.6%**  |
| **Companion / Tame Mobs** (Wiki-listed pets only)                         | 6           | 7          | **85.7%**  |
| **General Utility Items** (Wiki §6, items above §6.1)                     | 16          | 18         | **88.9%**  |
| **Dungeons / Hand-Built Structures** (Wiki §5, exhaustive)                | 3           | 15         | **20.0%**  |


**Strict Wiki coverage: 131 / 148 enumerated entries = 88.5% mechanical completeness.**

### Workspace Totals (Absolute Registry Counts)

These are the actual sizes of our registries — they exceed the Wiki because we ship
many entries the Wiki doesn't enumerate (spawn eggs, projectiles, food variants,
bonus content, multi-slot armor pieces, etc.).


| Registry                                      | Count   |
| --------------------------------------------- | ------- |
| Entity types (`ModEntities.ENTITY_TYPES`)     | **141** |
| Item types (`ModItems.ITEMS`)                 | **314** |
| Block types (`ModBlocks.BLOCKS`)              | **96**  |
| Crafting / smelting recipes                   | **217** |
| Entity loot tables                            | **117** |
| Biome modifier JSONs                          | **89**  |
| Worldgen configured features                  | **12**  |
| Dimensions (`data/orespawn/dimension/*.json`) | **6**   |


> **Why the wiki count is the canonical baseline.** The Wiki *does not* enumerate
> every block, food item, spawn egg, or projectile — only headline gameplay
> entries. Comparing our 314 items to "all items the Wiki ever mentions" would
> be apples-to-oranges. Comparing our implementation to *exact wiki entries*
> gives the only honest "% of original mod content shipped" number.

## Major Systems Successfully Ported

- **The Titans.** The King, The Queen, Mobzilla, Kraken, Mothra, Emperor Scorpion,
Hercules Beetle, Basilisk, Hammerhead, Cephadrome, Caterkiller, Vortex, Jeffery —
all with multi-part hitboxes, ServerBossEvent boss bars, fireball/lightning/ice-breath
attack stacks, and faithful 1.7.10 health-pool / damage-cap mechanics.
- **The Big Bertha Arsenal.** Big Bertha, Sliced, Royal Guardian Sword, Queen Scale
Battle Axe, Battle Axe, Big Hammer, SLYR Chainsaw, Ultimate Sword/Bow, Skate Bow,
Irukandji Arrows, Ray Gun (with redstone reload), Nightmare Sword, Mantis Claw,
the Crystal Dimension gem swords (Pink Tourmaline, Tigers Eye, Ruby, Amethyst,
Crystal Stone, Crystal Wood), the Squid Zooka, the Thunder Staff, and the
classic Rose Sword + Poison Sword + Experience Sword.
- **The Six Dimensions.** Utopia, Mining, Village, Crystal, Chaos, and the
floating-island Danger Dimension — all reachable via the canonical right-click-the-ant
mechanic, with a custom `OreSpawnChunkGenerator`, dedicated biomes, dimension-style
surface rules, and dimension-locked spawn pools.
- **The Zoo Cage / Miner's Dream Utility Belt.** All five sizes of Zoo Cage
(`zoo_cage_2/4/6/8/10`), Miner's Dream pickaxe with explosion-mining algorithm,
Wrench-based Red Ant Robot / Spider Robot taming kits, Instant Garden / Instant
Shelter / Instant Island deployables, the Zoo Keeper anti-despawn item, the
Sifter, and the Step Up / Step Down / Step Across walking aids.
- **The Royal Altars.** King and Queen altar structures (`king_spawner` /
`queen_spawner` blocks driven by `RoyalAltars` chunk hook + the canonical
Magic Apple item as an explicit summoner).
- **The Companion Roster.** Boyfriend, Girlfriend, The Prince (3 growth stages),
The Princess, Stinky, Spyro, Leon, Velocity Raptor, Camarasaurus, Hydrolisc,
Gamma Metroid — all with proper `TamableAnimal` taming items, follow/sit/defend
goals, and 1.7.10-faithful interaction recipes. (Note: only the 7 Wiki-listed
tames — Dragon, Water Dragon, Cephadrome, Prince ×3, Princess — count toward
the 85.7% companion-completion metric above. Boyfriend / Girlfriend / Stinky
/ Spyro / Leon / Velocity Raptor / Camarasaurus / Hydrolisc / Gamma Metroid
are real 1.7.10 mobs that the Namu Wiki page never enumerated, so they ship
as bonus content rather than as percentage credit.)
- **Faithful Power Curve.** Emerald → Experience → Amethyst → Pink Tourmaline →
Tigers Eye → Ruby → Ultimate → Mobzilla → Royal Guardian → Queen Scale armor
ladder with exact 1.7.10 defense values, durabilities, and recipe layouts.

---

## Deep Backlog (What Is Still Missing)

> Grouped by system, in priority order. Items marked **[v1.1]** are flagged for
> the next minor release; items marked **[v2.0]** require non-trivial new
> subsystems (custom structure generators, jigsaw pools, gameplay framework).

### 1. Missing Bosses & Minibosses

- ~~**Nightmare** (5 size variants: 125 HP / 250 HP / 500 HP / 750 HP / 1000 HP)
with size-scaled attack power (15 → 120) and defense (10 → 18). The
flagship Danger Dimension boss.~~ **DONE (Phase 8)** — `pitch_black`
refactored into 5 discrete `EntityDataAccessor`-tracked size tiers with
`refreshDimensions` hitbox scaling and per-tier attribute pools.
- ~~**Leonopteryx** — Avatar-inspired flying boss, drops Battle Axe + Kraken
Repellent.~~ **DONE (Phase 8)** — Mine Dimension flying boss with
flight navigation, raw-beef taming, ServerBossEvent boss bar, and
biome-modifier spawns in vanilla mountain biomes.
- ~~**Cave Fisher** is registered but its proper trip-wire / pulley hostile
cave-ambush AI is unfinished — currently a static placeholder.~~
**DONE (Phase 8)** — `CaveFisherAmbushGoal` implements the legacy
ceiling-anchor + drop-on-player ambush; `hurt()` aborts the anchor on
damage so it can't be cheesed mid-descent.
- ~~**Emperor Scorpion**: missing the surrounding-Scorpion summoner aura
(currently spawns with neighbors but does not periodically replenish them).~~
**DONE (Phase 8)** — TPS-throttled (~30-tick interval) summoner aura
that maintains a minimum `Scorpion` neighbor count without entity-cramming.
- **Kraken**: rain-on-spawn weather hook + the legacy `attack_squid` death
revenge spawn (random chance to spawn a Kraken when an Attack Squid is
killed by a player). **[v1.1]**
- ~~**Robo Pounder**: ground-tearing attack (digs holes when arms swing into
terrain) — currently only does melee damage.~~ **DONE (Phase 9)** —
Mobzilla-style throttled (`POUND_BLOCK_INTERVAL_TICKS = 30`) front-arc
block destruction with bedrock / tile-entity / unbreakable whitelist.
- ~~**Triffid**: invincibility-while-shell-closed reaction window — the open /
closed defense state machine matching the 1.7.10 timing.~~
**DONE (Phase 8)** — open/closed state machine with full damage
nullification while closed (except `BYPASSES_INVULNERABILITY` damage so
`/kill` and void still work).
- **The Queen**: Purple Power 1000-HP invincible follower entity that fires
80%-defense-ignoring stars on contact. Currently the Queen owns a
simplified `purple_power` projectile. **[v1.1]**
- **The King**: optional `FullPowerKingEnable` config flag — when an Adult
Prince is fed sufficient diamonds it should grow into an invincible white
King variant. Code path missing but referenced in OreSpawn.cfg legacy
docs. **[v2.0]**

### 2. The Unported / Underported Roster

- **Criminal** — the Banker / Police / Pharmacist humanoid bandits that
steal armor pieces on contact. White House dungeon spawn. Not in the
entity registry. **[v1.1]**
- **Vampire Butterfly** — hostile butterfly variant in the Danger
Dimension. Currently we only have the passive `butterfly` ambient mob.
Should be a separate registration with attack AI + blood-drain effect.
**[v1.1]**
- **Apple Cow / Golden Apple Cow** — the regular surface cows that drop
apples / golden apples. We have `red_cow`, `gold_cow`, `enchanted_cow`,
`crystal_cow` (the dimension-local cows) but not the standard overworld
apple-dropping variants. **[v1.1]**
- **Dinosaur AI Polish** — Pointysaurus eye-contact aggression trigger
(matches Enderman behaviour); ~~Allosaurus group-spawn coordination~~
**DONE (Phase 9)** — `HurtByTargetGoal#setAlertOthers()` pack alert,
biome modifier spawn count bumped to 2-3; ~~T-Rex's Big-Bertha-tooth
drop weight tuning~~ **DONE (Phase 9)** — weighted loot table pool
(60% × 1, 30% × 2, 10% × 3 teeth, ~4–5 kills per Big Bertha craft).
Pointysaurus eye-contact remains. **[v1.1]**
- ~~**Robo-Army Polish** — separate `Robo Pounder` / `Robo Gunner` /
`Robo Spinner` / `Robo Warrior` AI files instead of `robot_1..5`
generic shells.~~ **DONE (Phase 9)** — `Robot1..5` refactored into
distinct role AIs (Spinner/Warrior/Gunner/Pounder/Sniper) with
signature behaviours (kamikaze yaw-spin self-detonation, pack-alert
aggro, `LaserBall` projectile fire, throttled ground-tearing, long-range
sniper lock). Registry IDs preserved for save compat.
- ~~**CaterKiller**: tree-eating health-regen behaviour + scheduled
transformation into Brutalfly + butterflies on death.~~
**DONE (Phase 9)** — chews leaves (+5 HP) / logs (+10 HP) on a 20-tick
throttle, dies into a Brutalfly + 3-5 standard butterflies, drops
`minecraft:cobweb` under the chased player on a 40-tick cadence.
- **Cephadrome** taming via raw pork-chop — currently only listens for
generic meat. **[v1.1]**
- **Crab** size variants: only one `crab` registration. 1.7.10 has small
(62 HP), medium (125 HP), and large (250 HP) Rainbow Crabs, each with
escalating defense. Wiki documents all 3 as separate stat blocks. **[v1.1]**
- ~~**Baby Dragon** — separate from the adult `Dragon` registration.~~
**DONE (Phase 9)** — distinct `BabyDragon` entity (50 HP, 5 ATK,
0.30 speed, pre-fledged DragonFire=1 from spawn) with own spawn egg,
loot table, lang entries, and renderer (0.45× scale). The adult
`Dragon` apple-interaction now spawns a `BabyDragon` instead of the
legacy `EntitySpyro`.
- **Iron Golems on Goodness/Queen Trees** — should aggro players who
attack the gem leaves (vanilla iron golems instead behave like village
defenders). **[v1.1]**
- **Triple ant categories** — termites turning wood blocks into dirt /
removing them entirely (the canonical reason houses can't be wood) is
partially implemented. The "diamond troll block" / "emerald troll block"
that summon fire-ant / termite swarms when mined: blocks exist
(`red_ant_troll`, `termite_troll`) but the summon-on-break behaviour is
stubbed. **[v1.1]**
- ~~**Caterkiller cobweb-on-chase** — uses vanilla path slowdown, should
drop actual `minecraft:cobweb` blocks under the player.~~
**DONE (Phase 9)** — see CaterKiller entry above; cobweb is placed
into the chased player's standing tile on a throttled cadence,
gated behind `doMobGriefing`.
- ~~**Nightmare Lair** spawner-block mob (separate from the Pitch Black)
with 5-size lottery on spawn.~~ **DONE (Phase 8)** — the Pitch Black
entity now is the Nightmare with the 5-tier size lottery applied at
finalizeSpawn time.

### 3. Missing Dungeons & Worldgen

- **Challenge Tower (King Variant)** — the 7-floors-up / 8-floors-down
tower with the canonical floor-by-floor mob layout (Cloud Shark →
Rotator → Bee → Mantis → Mothra → Mothra → Nightmare ascending; Worm →
T-Rex → Basilisk → Hercules → Jumpy/Spit → Hammerhead → Emperor Scorpion
descending). Drops Royal Guardian Greatsword + full armor + Prince egg.
**[v2.0]**
- **Queen Challenge Tower** — alternate harder variant that drops Royal
Guardian Greatsword + Queen Scale armor + Princess egg. **[v2.0]**
- **Crystal Battle Tower** — Crystal Dimension dungeon with Crystal Monster
spawners + Vortex spawner on the top floor. **[v2.0]**
- **Crystal Dimension Mazes** — the labyrinthine tunnel networks generated
under the crystal landmass. **[v2.0]**
- **Mantis Nest** — group-of-3-or-4 mantis spawn cluster on plains
surface. Currently mantis spawns are evenly distributed via biome
modifier, no nest geometry. **[v1.1]**
- **Beehive** — mossy-stone + sponge box with bee spawners (forest /
jungle / savannah / mine). **[v1.1]**
- **Shadow Dungeon** — Mine Dimension dungeon containing Nightmare
spawners + Ender Reaper spawners. **[v1.1]**
- **Greenhouse** — Danger Dimension structure with Triffid spawners on the
roof and many decorative crops inside. **[v1.1]**
- **White House** — Criminal spawner dungeon. **[v1.1]**
- **Robot Lab** — Village Dimension structure that hosts Robo Warrior,
Robo Spinner, and Robo Pounder spawners. **[v1.1]**
- **Leonopteryx Dungeon** — Mine Dimension nest with the Leonopteryx
spawner in the centre. **[v1.1]**
- **The Goodness Tree** — the gem-leaf giant tree (gold core, emerald
trunk, ruby/amethyst/titanium/uranium leaves) with The King at the top.
Currently we only place the standard Utopia tree; the Goodness Tree
variant is stubbed. **[v1.1]**
- **The Queen's Tree** — ruby-stem variant with The Queen on top. **[v1.1]**
- **Basilisk Dungeon** — jungle structure containing Ultimate gear chest

- Basilisk spawners. **[v1.1]**

- **WTF–Alien Dungeon** — Mine Dimension structure with Alien-swarm
spawners and the ultimate gear chest. **[v1.1]**
- **Ender Dungeon** — overworld structure with Ender Knight + Ender
Reaper spawners. **[v1.1]**
- **Sea Viper's Dungeon** — underwater structure. **[v1.1]**
- **Sea Monster's Dungeon** — underwater swamp-edge structure. **[v1.1]**
- **Mantis Lair** — separate from Nest; subterranean mantis hideout.
**[v1.1]**
- **Ruined Inca Castle** — Danger Dimension landmark that spawns 3
Molenoids in a group. **[v1.1]**
- **Rotator Dungeon** — Crystal-themed flying spawner cluster. **[v1.1]**
- **Scorpion / Rat Dungeon** — desert / mesa structure. **[v1.1]**
- **Random Dungeon item** — `random_dungeon` block places, but the random
dungeon templates that should pop up after a few seconds are absent.
Currently it's a no-op marker. **[v1.1]**
- **Diamond / Emerald Troll Block** worldgen — currently
`red_ant_troll` / `termite_troll` exist as item entries but are never
placed by the chunk generator. **[v1.1]**

### 4. Missing Utility & Gadgets

- **Hoverboard** — the canonical jetpack-equivalent flying board. No item,
no entity. **[v1.1]**
- **Ancient Dried Egg / Fossil** — the underground fossil block that
converts into a mob spawn egg when combined with a water bucket. We have
spawn eggs for every entity, but the fossil-mining flow itself is
absent. **[v1.1]**
- **Coin economy** — Coins spawn but the canonical "right-click coin to get
a useful item from the loot table" reward pool is minimal compared to
1.7.10's broader randomized gear selection. **[v1.1]**
- **Extractor** — the multi-block ore-purification gadget. **[v2.0]**
- **Duplicator Tree (functional)** — the `duplicator_log` block exists
but does not yet duplicate adjacent saplings/items as in 1.7.10.
**[v1.1]**
- **Kyanite Sword + Kyanite Armor + Kyanite Ore** — the Crystal Dimension's
blue gem branch is missing entirely (no `kyanite_*` items, no recipes, no
`ore_kyanite` block). Wiki §6.1 lists the Kyanite Sword as a top-tier
blade. **[v1.1]**
- **Pink Tourmaline gem item + ore block** — we ship the `crystal_pink_*`
armor and the `crystal_pink_sword`, but there is no `pink_tourmaline`
raw gem item and no `ore_pink_tourmaline` block. The current swords are
crafted from `crystal_flower_*` substitutes; restore the canonical
gem-from-ore path. **[v1.1]**
- **Crystal Lumber / Crystal Stick** — the Wood Crystal Sword recipe in
Wiki §6.1 explicitly requires Crystal Lumber + Crystal Sticks. We craft
the sword from substitute materials today; add the proper intermediate
items + their crafting recipes. **[v1.1]**
- **Critter Cage filled-variant projectile pickup** — `EntityCage` flies
but does not yet transform into a `critter_cage_<mob>` filled item on
impact. (See `ORESPAWN_PORTING_AUDIT.md` for the call-site detail.)
**[v1.1]**
- **Big Bertha component drops** — the wiki recipe for the Big Bertha
requires 16 specific boss-drop components (Mantis Claw, Water Dragon
Scale, Green Slime, Molenoid's Nose, Sea Monster Scale, Mothra Scale,
Basilisk Scale, Nightmare Scale, Emperor Scorpion Shell, Jumpy Bug
Shell, Kraken's Bite, Worm's Bite, T-Rex's Bite, Caterkiller's Mandible,
Sea Viper's Tongue, Eye of Vortex). Audit each boss's loot table and
fill any gaps so the recipe is satisfiable end-to-end. **[v1.1]**
- **Crystal Apple specifics** — the Wiki specifies "5 HP recovery, 0.85
hunger recovery, Strength + Regeneration for 150 seconds." Our current
`crystal_apple` food values may not match exactly; verify and align.
**[v1.1]**
- **Notch Apple compatibility** — Wiki notes the Crystal Apple must be
"highly compatible with gold block apples" (i.e. enchanted golden apple
cooldown sharing). Verify our cooldown grouping. **[v1.1]**

### 5. Misc Polish & Engineering Debt

- **Spawn-placement registration** — several entities trigger a non-fatal
`RegisterSpawnPlacementsEvent` warning (giant_robot, brutalfly, fairy,
red_ant). They still spawn correctly, but should declare ground / water
/ no-restriction placements explicitly. **[v1.1]**
- **OreGenericEgg behaviour** — 1.12.2's mob-themed ores (alosaurus_ore,
trex_ore, baryonyx_ore, …) spawn the matching entity when broken. In
1.21.1 these registrations exist as plain ore blocks with no spawn
side-effect. **[v1.1]**
- **Worm burrow / surface-attack cycle** — `WormSmall`/`WormMedium`/
`WormLarge` are flat Mob entities without the canonical down-count /
up-count burrow timer. **[v1.1]**
- **Alien torch-destruction** — 15-block torch search + mob-griefing
gating not yet wired up. **[v1.1]**
- **Chaos Awakens cross-pollination guards** — explicitly tag overlapping
entity IDs (mantis, basilisk, hercules_beetle) so Chaos Awakens can
coexist without registry collisions. **[v1.1]**

---

## Known Quirks & 1.21.1 Differences

> If you played the original 1.7.10 mod, here is what we changed and why.
> Each change exists either to obey a modern Minecraft API constraint or to
> avoid a TPS / save-corruption hazard that the legacy implementation simply
> didn't worry about.

### Zoo Cages now retain mob NBT

In 1.7.10 the Zoo Cage stored only an entity ID string and re-spawned a fresh
mob when broken. In 1.21.1 we use `DataComponentType<CompoundTag>` so the cage
preserves the **full mob NBT** — health, custom name, owner UUID, age, breed,
inventory, and any modded data. Cages of tamed wolves stay tamed; cages of
named `red_cow` keep their nametag. The trade-off is that cage stacks no
longer collapse into a single ItemStack (each tagged cage is a unique stack),
which is consistent with how vanilla 1.21 handles bucketed mobs.

### The Magic Apple is the canonical King / Queen spawner

The 1.7.10 mod allowed The King and The Queen to spawn ambiently inside
Goodness Trees / Queen Trees during world generation. On a modern dedicated
server with chunk pre-generators (and the way our new `noise_router` aggressively
loads chunks), that ambient spawn was a TPS-killer and a gameplay surprise that
players couldn't opt into. The fix:

- **The Magic Apple** (recipe documented in JEI) is now the explicit summoner.
Right-click while holding it on bare ground inside the Utopia Dimension to
spawn either The King or The Queen, depending on the altar type beneath.
- The **King's Altar** and **Queen's Altar** still generate naturally in the
Utopia Dimension as advertised in the 1.7.10 docs — they hold the altar
spawn block and act as the structural marker, but they **don't auto-spawn
the boss** any more. Use the Magic Apple inside the altar room to begin the
fight.

This restores the 1.7.10 progression beat ("find the altar → trigger the boss
fight on your terms") without the chunk-load hazard.

### Mobzilla obeys a 5 Hz block-breaking throttle

In 1.7.10, every step Mobzilla took ran an instant block-destruction sweep that
turned grass to dirt and chewed through every tile in his path on the same tick.
With modern light propagation, BE updates, and `ChunkAccess` synchronization, a
single tick of legacy Mobzilla movement could allocate megabytes of light /
heightmap / lighting-engine work and cause server stalls of 200 ms+.

Our port keeps the same visual behaviour (grass turns to dirt, leaves get
shredded, fragile blocks pop) but **throttles the destruction tick to 5 Hz**
(once every 4 game ticks) and batches block updates by chunk before flushing.
Players don't notice the difference visually, but the dedicated-server TPS
budget is preserved even when Mobzilla is rampaging through a built-up village.

### Custom dimensions use a constant-`continentalness` JSON override

Modern Minecraft (1.18+) introduced the `noise_router` system, which threads
**continentalness, erosion, and ridge** noise into the terrain shape so the
overworld feels like real continents and oceans. That's gorgeous in vanilla,
but the 1.7.10 dimensions were built on `BiomeProviderSingle` — they assumed
**one biome stretches infinitely with no oceans**. Plug a single-biome dimension
into the modern noise router and you get archipelagos instead of the endless
plains the King's Altar was designed for.

Our solution lives entirely in JSON (no Java hacks, no post-fill stone
patches):

- A new noise settings file `data/orespawn/worldgen/noise_settings/inland.json`
(a copy of vanilla `minecraft:overworld`) rewires its `noise_router` to point
the `continents` density function at a brand-new
`data/orespawn/worldgen/density_function/inland/continents.json` that simply
returns the **constant value `1.0`** ("deep inland").
- `depth.json`, `factor.json`, `jaggedness.json`, `offset.json`, and
`sloped_cheese.json` are mirrored under `inland/` and rewired to consume the
inland continents value.
- Utopia, Mining, Village, Crystal, and Chaos dimensions all set
`"settings": "orespawn:inland"` in their dimension JSONs.
- The Islands (Danger) dimension keeps `minecraft:floating_islands` because
archipelago geometry is on-theme there.

The result: surface rules, biome blending, ore generation, and cave carvers
**all behave exactly as they would in a real overworld inland** — grass on top,
dirt below, stone underneath, no raw-stone patches, and **no oceans**. That's
what 1.7.10 looked like, and that's what you get now.

### Multi-part bosses use `OreSpawnPartEntity`

The 1.7.10 mod faked multi-part hitboxes by spawning sidecar `KingHead` /
`QueenHead` / `GodzillaHead` entities that teleported next to the parent
every tick. Modern Minecraft has first-class multi-part support via
`PartEntity` (used by the Ender Dragon). Our port introduces
`OreSpawnPartEntity` — bosses own a list of part entities that share the same
network sync packet, ride the parent's matrix transform every tick, and route
attack damage through `ServerLevel#getEntities`. The legacy `_head` registries
still exist for save compatibility but are invisible no-op stubs (zero-cost
renderers).

### Ranged-weapon cooldowns are server-authoritative

The Skate Bow / Ray Gun / Squid Zooka / Big Bertha all use
`player.getCooldowns().addCooldown()` server-side now (not client-side
`onItemUse` timing). This prevents a long-standing 1.7.10 exploit where
right-click-spam on a laggy connection bypassed the cooldown.

### Spawn weights are now `MobCategory`-aware

NeoForge's `add_spawns` biome modifier respects `MobCategory` mob caps. The
1.7.10 weight tables we ported faithfully sometimes pushed mobs (e.g. Coin,
T-Shirt) into the wrong category (CREATURE) where they'd never spawn under the
modern animal cap. We re-classified those as `MISC` so they spawn correctly,
without changing the documented weight values.

---

## Architecture

```
src/main/java/danger/orespawn/
├── ModEntities.java           139 entity registrations
├── ModItems.java              314 item registrations
├── ModBlocks.java              96 block registrations
├── ModArmorMaterials.java     10 armor tiers
├── ModToolTiers.java          full tool tier ladder
├── ModCreativeTabs.java       categorized creative inventory
├── ModSounds.java             audio registry
├── ModDataComponents.java     1.21 component types (Zoo Cage NBT, etc.)
├── world/
│   ├── OreSpawnChunkGenerator.java   custom dimension generator
│   ├── DimensionStyle.java           enum: DEFAULT/CRYSTAL/ISLANDS/CHAOS/VILLAGE/UTOPIA/MINING
│   ├── RoyalAltars.java              King/Queen altar placement
│   └── ...
├── entity/                    ~120 entity classes
├── entity/client/             ~75 model + renderer classes
├── item/                      weapon, armor, food, tool implementations
├── block/                     custom block behaviour (plants, ores, altars)
├── network/                   custom packets (ranged attacks, taming)
└── loot/                      loot modifiers
```

```
src/main/resources/data/orespawn/
├── dimension/                 6 dimension JSONs
├── worldgen/
│   ├── biome/                 custom dimension biomes
│   ├── noise_settings/        inland.json (no-ocean override)
│   ├── density_function/inland/  continents=1.0 chain
│   ├── configured_feature/    ores, crystal trees, kraken/dragon spawn blocks
│   └── placed_feature/        ore placements
├── neoforge/biome_modifier/   89 spawn / structure modifiers
├── loot_table/entities/       117 mob loot tables
├── loot_table/blocks/         block loot tables
├── recipe/                    217 crafting / smelting recipes
└── tags/                      block / item / entity tags
```

---

## Credits

- **Original mod**: TheyCallMeDanger (2013–2015), 1.5.2 → 1.7.10 → unfinished 1.12.2.
- **Reference fork**: `Orespawn-1.12.2-V0.8-ConquerantFix` (decompiled with CFR 0.152
into `reference_1_12_2_source/`).
- **1.7.10 source baseline**: full deobfuscated source under `reference_1_7_10_source/`.
- **Namu Wiki community** for maintaining the most complete public catalogue of the
original mod's content, used here as the canonical baseline for completion metrics.
- **NeoForge port**: this project, modernized for 1.21.1.

## License

All Rights Reserved — see `gradle.properties`. Original OreSpawn assets remain the
property of TheyCallMeDanger.

## See Also

- `ORESPAWN_PORTING_AUDIT.md` — the engineering-grade audit of every subsystem
(race conditions, missing tile-entity hooks, render-thread safety) used to
drive the v1.1 and v2.0 backlogs above.

