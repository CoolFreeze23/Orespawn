# OreSpawn for NeoForge 1.21.1 — 2.0.0-beta.3

Hotfix for a world-generation crash.

- **Village/Mining dimension lake crash fixed.** Generating a new chunk
  with a classic lake could kill the whole server with
  `IllegalStateException: Requested chunk unavailable during world
  generation` when Serene Seasons is installed. Our lake feature already
  clamped its own biome lookups to the guaranteed worldgen region, but
  Serene Seasons redirects the freeze check *inside* vanilla's
  `Biome.shouldFreeze` to a seasonal hook that performs its own
  unclamped biome lookup — which can sample a chunk outside the region
  when a lake hugs a chunk corner. The freeze check is now inlined with
  all sampling on the clamped position, out of reach of the redirect.
  Generation-time ice now reflects the biome's base climate rather than
  the season on the day the chunk happened to generate (the saner
  behavior anyway).

# OreSpawn for NeoForge 1.21.1 — 2.0.0-beta.2

A field-report patch: everything in it came from walking around a real
modded world and asking "did the original actually do this?" Worlds
carry forward from any earlier beta.

## The ant & termite overhaul

- **Ant armageddon fixed.** Ants and Red Ants were spawning naturally
  in every overworld biome at cow-level frequency — invented content;
  the original 1.7.10 registered natural spawns for 55 creatures and
  the ants were never among them. They come from anthills, ambushes,
  and eggs, exactly like the original. On top of that the port's ants
  were immortal: the original let them despawn like monsters, and
  that's restored — so the swarms your world has already accumulated
  will clear themselves out as you play.
- **Anthills no longer disguise themselves as copper blocks.** The
  red/rainbow/unstable nests rendered as bare orange cubes and the
  termite nest wore the Crystal-dimension texture by mistake. All five
  overworld nests use the original antnest look again, with the
  original's biome grass tint, so they sit in the landscape like the
  grassy mounds they always were.
- **Ants are ant-sized again.** The original rendered ants at quarter
  scale (Red Ants and Termites at 0.35×); the port lost the scale-down
  and drew them 4× too big, which also made the classic leg-scurry
  animation look broken. Original sizes and shadows restored.
- **Termites multiply while eating your house again** — the original's
  replication-on-eat (with its own 10-termite crowd cap) had been
  dropped in the port. Wood still turns to dirt or vanishes under
  mobGriefing, exactly per the original's dice.
- **One deliberate deviation, clearly labeled:** a nest block now skips
  its 2-7 ant burst once 10+ ants are already nearby, so idling next
  to an anthill can't snowball into hundreds of entities. The original
  relied on despawning alone; with modern render distances that still
  let populations pile up within despawn range.

## Also in this build

- **The Mining dimension has daylight again.** The original's Mining
  dimension is an open-sky mountain world with a day/night cycle and
  sleepable beds; the port had declared it a skylight-less ceiling
  world at 0.1 ambient light, rendering it near-black. Chunks you
  explored before this fix were saved without skylight data and may
  stay dark until the light engine touches them (or you visit fresh
  terrain) — new chunks are properly sunlit.

---

# OreSpawn for NeoForge 1.21.1 — 2.0.0-beta.1 "OreSpawn Modernized"

The first public build of OreSpawn 2.0. The 1.0 line replicated 1.7.10
exactly; 2.0 is where the deliberate improvements live — clearly
labeled, and always one config line away from pure 1.7.10. Worlds
carry forward from any 1.0 beta.

## The headline: the Procedural Spider Overhaul

- **The Giant Robot Spider and Robot Ant walk for real now.** With the
  default `spiderMovement = "MODERN"`, the legs are procedural and
  genuinely plant: feet stay put in the world while the body moves,
  find footing on stairs, slabs and ledges, contract on narrow
  bridges, and dangle-and-re-step when there's nothing to stand on.
  The body rides its legs — tilting up slopes, sagging when footing
  collapses, settling level on flat ground.
- **Every leg is a real, hittable surface.** All 8 spider legs and 6
  ant legs take hits and deal exactly body damage — no new weak
  points — and the crosshair health bar now works when you aim at a
  leg (and, bonus, on The King's giant body parts, which never showed
  a bar before).
- **The spider is RIDEABLE — actually steerable — for the first time
  ever.** In 1.7.10 and the 1.0 port you could sit on the spider but
  never drive it. Modern mode gives the saddle real steering (full
  speed forward, half strafe, quarter reverse), stairs feel right
  from the saddle, and clicking a LEG mounts you — no more hunting
  for the body between the legs.
- **A smart riding camera.** Mounting glides the camera back so the
  huge body sits low in your view, with collision handling for caves
  and tree cover; dismounting snaps instantly back to vanilla.
- **Classic is one config line away.** `spiderMovement = "CLASSIC"` is
  the exact 1.7.10 robots, bit-identical — the same client-side leg
  animation, body-only hitbox, and famously unsteerable saddle. The
  automated suite runs green in BOTH modes on every build (192 tests
  under MODERN and under CLASSIC), so preservation stays a tested
  promise, not a checkbox.

## Also fixed in this build

- **The Queen no longer freezes mid-air** (or endlessly repeats one
  attack swing) after her first melee — her attack animations now
  finish and blend back into her flying stance, and she stays animated
  through combat lulls like the original always did. Her death pose
  still holds. *(BUG-035)*
- **Vanilla creepers had stowaway hitboxes** in beta.2/beta.3: a
  bundled-library demo file gave every vanilla creeper invisible extra
  hit surfaces (head hits could deal double damage). Removed; an
  automated test now pins "no OreSpawn parts on vanilla mobs".
  *(BUG-036)*
- **The Princess and The Prince no longer spawn wild** — they could
  even appear right at world spawn on a brand-new world. Leftover
  invented content; in the original they only come from spawn eggs,
  the Queen's death, and structures. Girlfriends and Boyfriends still
  roam wild exactly like 1.7.10. *(BUG-037)*
- **Riders sit on the spider properly again.** The classic seat had
  been wrong since the very first beta (riders sat half-buried in the
  body); the faithful 1.7.10 seat is restored in both modes, composed
  with the modern body motion so it stays right on slopes and sags.
  *(S7a)*
- **The spider's body hitbox is the original's again**: the full
  3.25×2.25 box from 1.7.10, restored after the 1.0 port shipped a
  shrunken, never-audited 2.0×1.5 box — a much easier target to click
  and to hit. *(ENT-S-088)*

## Known issues & tuning — beta players are the tuners now

[KNOWN_ISSUES.md](KNOWN_ISSUES.md) has the full list. The short
version:

- **Mid-swing legs lead your view on laggy servers.** An airborne,
  stepping leg is a moving target that tracks the server's swing — at
  high ping, lead it slightly or just hit the body, which pays exactly
  the same. Planted legs are always precisely where they look.
- **Tuning feedback wanted.** At sustained sprint the spider's legs
  churn faster than a clean walk cycle (the body never slows down, so
  steps convert into quick forced lifts) — and the Robot Ant's step
  tempo is a first-pass tune. If a stride reads wrong to you, that
  report is exactly what this beta is for.
- The ant's dangling legs during hover-flight are intended; robot ants
  still only obey their owner; and modern spiders genuinely trample
  grass under a rider (with mobGriefing on) — the original tried to,
  but its client-side trample rarely stuck on servers.

## What's next

The **Queen Coherence pass**: the freeze fix in this build came out of
a full review of her animation stack, and the follow-ups that review
surfaced — a real calm-vs-aggro flight state like the original's,
wake-up polish, attack cadence — are the teased next milestone.

# OreSpawn for NeoForge 1.21.1 — 1.0.0-beta.3

A same-day hotfix for three problems caught by the first real-world play
sessions of beta.2. Worlds carry forward; nothing else changed.

- **Fixed: launch crash on clean installs.** beta.2 only launched if some
  other mod happened to provide the `databuddy` library; without it the
  game crashed during mod loading. The library now ships inside the
  OreSpawn jar.
- **Fixed: game freezes near Basilisk Mazes and royal trees** — chunks stop
  loading, blocks stop breaking, and the log says `Failed to load chunk`.
  This hit hardest in the Mining dimension and with parallel chunk engines
  (c2me, Distant Horizons' distant generation). It was a thread-safety bug
  in our structure generation; frozen worlds are safe, and the failed
  chunks regenerate cleanly on your next visit.
- **Fixed: the Dungeon Beast never spawned.** A bad attack-timing constant
  made every spawn attempt fail (with "Failed to create mob" log spam).
  Restored to the original's values, checked against the 1.7.10 source.
- The test suite now constructs every mob type on every build, so an
  unspawnable mob can never ship silently again.

---

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
