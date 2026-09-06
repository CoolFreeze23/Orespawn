## Mob sizes and shadows restored to the original (ENT-S-092)

Every mob renderer now applies the exact world scale and shadow radius the 1.7.10 mod registered
(`RenderX(model, shadow, scale)`), which the port had mostly dropped or retuned. The visible ones:

- **Brutalfly** is now 9x bigger (1 -> 9).
- **Irukandji** is now 0.25x its previous size (1 -> 0.25).
- **Fairy** is now 0.35x its previous size (1 -> 0.35).
- **Robot3** is now 0.5x its previous size (1 -> 0.5).
- **Cricket** is now 0.5x its previous size (1 -> 0.5).
- **Hydrolisc** is now 0.65x its previous size (1 -> 0.65).
- **Dragonfly** is now 1.5x bigger (1 -> 1.5).
- **EmperorScorpion** is now 1.5x bigger (1 -> 1.5).
- **ThePrinceTeen** is now 1.47x bigger (0.85 -> 1.25).
- **Firefly** is now 0.75x its previous size (1 -> 0.75).
- **VelocityRaptor** is now 0.75x its previous size (1 -> 0.75).
- **Spyro** is now 0.75x its previous size (1 -> 0.75).
- **Cockateil** is now 0.75x its previous size (1 -> 0.75).
- **RubyBird** is now 0.75x its previous size (1 -> 0.75).
- **Scorpion** is now 0.75x its previous size (1 -> 0.75).
- **CaveFisher** is now 0.75x its previous size (1 -> 0.75).
- **SpitBug** is now 0.75x its previous size (1 -> 0.75).
- **CreepingHorror** is now 0.75x its previous size (1 -> 0.75).
- **TerribleTerror** is now 0.75x its previous size (1 -> 0.75).
- **Beaver** is now 0.75x its previous size (1 -> 0.75).
- **Rat** is now 0.75x its previous size (1 -> 0.75).
- **RubberDucky** is now 0.75x its previous size (1 -> 0.75).

Baby branches the original had (Beaver, Hydrolisc, RubberDucky, GammaMetroid, Chipmunk, Lizard, Flounder,
VelocityRaptor, WaterDragon) are transcribed from the original renderers. Shadow radii follow the original
`shadow x scale` products for all species (40 shadow-only corrections land in the next batch). The boss group
(Kraken, Sea Monster, The King, The Queen, Godzilla) is handled separately because their hit surfaces follow
the rendered bones. Hitboxes are unchanged in this build (see ENT-S-095).

## Hitboxes restored to the original (ENT-S-095, batch 1)

Sixty-three mobs get back the exact hitbox the 1.7.10 mod gave them, so hits, spawn fit and
collision match the original again. The visible ones: the Tshirt board is 4x4 (was 0.6x1.8),
Molenoid 3.9x2.6, Emperor Scorpion 3.5x3, Hercules Beetle 3.25x2.75, Robot 2 3x6.2, Mantis
2.5x3.25, Trooper Bug 3x3.5, Urchin 1.35x2.1, Triffid 2x4, Coin 1.5x1.5; the Sea Monster's
box shrinks from 5x5 to 1.25x2.5, and the ants, cricket, irukandji and rat are small again.
The Kraken also regained its PlayNicely mode (1.33x5 box and a third-size draw while the
setting is on), and its PlayNicely behaviour gates (no storms, lightning, grabs or hunts while
the setting is on). Godzilla is 9.9 wide again (was 10), Mothra's box is back to 5x2 (was 6x3),
the apple cows match the cow line at 0.9x1.3, and a small fireball is small again (0.3125).

The Queen is back to her original size (twice what the port drew), her shadow matches, her PlayNicely
box is 5.5x6 as in 1.7.10, and her hit parts follow her bones from the first frame instead of
snapping to her feet for a frame after every model reload.

A fired BetterFireball is now the mod's own entity again (it used to be typed as a vanilla fireball
in flight and in saves), and the modern config gains its first switch: with `[modern] enabled`
on, Mothra keeps the port's wider 6x3 root box; classic stays at the original 5x2.

`modern.enabled` is the new master switch for every 2.0 feature. It defaults to true and
defers to the per-feature keys, so a default config keeps the modern robot spiders and ants
(`spiderMovement = "MODERN"`, the key's default) with the riding camera, as before. Set
`modern.enabled = false` for the classic everything: `spiderMovement`, `mountCamera`,
`phase14ContentEnable` and `mothraWideRootHitbox` drop to their 1.7.10 / off values at once,
whatever they are set to (`spiderMovement = "CLASSIC"` still switches the robots alone). New
modern features register under `[modern]`.

One blast per fireball: a boss's big fireball used to explode twice on impact (the vanilla fireball's
blast, then OreSpawn's own), and the small fireballs Dragons and the royal family spit exploded although
they never did in 1.7.10; every shot now explodes exactly once, at its own power, and small shots not at
all. OreSpawn's thrown projectiles (laser, acid and ice balls, water balls, thunderbolts, urchins, ink
sacks, shoes, rocks) now break decorated pots, chorus flowers and dripstone the way vanilla snowballs do
under `projectilesCanBreakBlocks`. The Ultimate and Skate bows' arrows stay outside vanilla's `#arrows`
tag, so vanilla's Power and Punch math and the "Take Aim" advancement do not apply to them, as in 1.7.10,
where the bows never applied Power.

The Queen's hit surfaces no longer freeze in the rest pose after a lag spike or when two Queens are drawn.
MHLib's once-per-tick bone collection was gated by a single stamp per renderer that wedged for good on a
frame during which the entity ticked twice and starved every Queen but one; the stamp now lives on each
entity and collects whenever the entity has ticked since its last pass (BUG-044, design after MoreHitboxes,
MIT, see the README's third-party notices). The GeckoLib render hook also no longer fires twice per bone
(OPT-028: it hooked a synthetic bridge as well as the real method), halving that per-bone cost for every
GeckoLib mob. `-Dmhlib.counters=true` logs the collection counters every 100 client ticks.

Kraken targeting and the shared "leave it alone" list now match 1.7.10. The Kraken's prey check honours
the original exclusions again: it never grabs squids, Attack Squids, other Krakens, Spyro, chickens,
chipmunks, stink bugs or Mothra; it spares any Dragon, Cephadrome, Leonopteryx or Prince that someone is
riding; it skips everything on the shared spare list; and it no longer snatches flying survival players
(a survival player on the ground is still prey, creative players are never targeted). As in the original,
the nearest player decides the player grab, so a creative player standing closer than a survival one
shields them. A victim that dies in the Kraken's grip is carried through its death animation before the
tentacles let go, as in 1.7.10. The Kraken's health-keyed behaviour (turning on the player who hits it,
fleeing upward when badly hurt, calling reinforcements, despawning when far away) now keys off its real
1000 max health instead of a stale 3000. The shared spare list used by the Alosaurus, Basilisk,
Brutalfly, Rotator, Scorpion, Vortex, Mothra, Spider Robot, King and Queen is back to the original
twelve: rock bases, ants of every colour and termites, butterflies (luna moths and Mothra included),
mosquitoes, dragonflies, fireflies, crickets, cockatiels, ghosts, ghost skellies and elevators are left
alone; cave fishers, fairies and coins are once more fair game.

The Ultimate Bow's Punch lands again, and the bosses' fireballs light fires. An ultimate arrow now shoves the
mob it hits along its flight line by the 1.7.10 amount (0.6 blocks per tick per Punch level, so 1.2 for the
bow's own Punch II, with the small hop); 1.21.1 keys Punch on a tag the arrow is deliberately outside of, so
the bow's self-applied Punch never landed. A fireball from Mothra, Godzilla, the King, the Queen, a Dragon or
the royal family sets fire to the air beside the block it strikes again, and its blast always scatters fire,
with block destruction alone following `mobGriefing`, as the original did; the port had wired `mobGriefing`
to the fire instead. A config-gated "fire respects mobGriefing" option is proposed as MOD-031 but not built;
classic stays 1.7.10.

Hunters leave the small fry alone again. In 1.7.10 every OreSpawn hunter ran its prey through one shared
ignore list (rock bases, ants and termites, butterflies with the Luna Moth and Mothra included, mosquitoes,
dragonflies, fireflies, crickets, cockatiels, ghosts, ghost skellies and the Elevator) before it weighed
anything else. Only eleven hunters kept that screen in the port; the other twenty-six (Mobzilla, the
Nightmare, the Leonopteryx, the Kyuubi, the Giant Robot and the five robots, the Ant Robot, the Rat, the
Triffid, the Purple Power, the Gamma Metroid, the Spider Driver, the Cave Fisher, the Dungeon Beast, the
Emperor Scorpion, the Hercules Beetle, the Nastysaurus, the Pointysaurus, the Spit Bug, the T. Rex, the
Trooper Bug and the Crystal Urchin) would chase a butterfly or an ant, or wander off after a passing ghost.
All 38 of the original's call sites are back in their original place in each hunter's check order.
Separately, the Leonopteryx and the Cephadrome now recognise a creative-mode player the way the original
did (creative mode itself, not "cannot be hurt"), so a survival player who is invulnerable for some other
reason is hunted like anyone else, exactly as in 1.7.10. The Kraken's tie between two equally distant players
now falls the 1.7.10 way (the last one scanned).

Nine hunters hunt as they did in 1.7.10 again: the Cave Fisher, Dungeon Beast, Emperor Scorpion, Hercules Beetle,
Nastysaurus, Spit Bug, T. Rex, Trooper Bug and Crystal Urchin consider every living thing inside the original's
search box (10x3x10 to 32x8x32 around the hunter), ranked by the original sorter and screened by the original chain
(the shared ignore list, line of sight, allied species, creative players) on the original tick cadence, instead of
players only. Villagers, golems, water and ambient creatures and OreSpawn's own non-monster species are prey again,
and prey that leaves the box or line of sight is dropped as it was.

Classic parity, targeting: nine more hunters (Cryolophosaurus, Brutalfly for both its filter and its fireball
strafe, Gamma Metroid, Kyuubi, Leaf Monster, Lurking Terror, Rat, Terrible Terror, Triffid) now read the 1.7.10
creative check as creative mode rather than invulnerability: a survival player made invulnerable by other means is
prey again and a creative player never is. The Cephadrome ignores everything on Peaceful, and an unfed shark's
warning stalk after a refused ride lasts one scan, as in 1.7.10. A wild Leonopteryx hunts like 1.7.10 again: it
no longer attacks every living thing in reach but takes monsters, non-creative players and the original's short
list of attackable non-mobs (villagers, the royal family, Cephadromes, Water Dragons, Dragons, Spyros, Gamma
Metroids, Girlfriends and Boyfriends, Stinkies), so farm animals are safe, and the Play Nicely setting switches its
hunting off entirely. Irukandji arrows no longer punch players: the Skate Bow's Punch knockback lands on mobs only.
The Nightmare leaves the Danger Dimension's own creatures alone again: Ender Reapers, Leaf Monsters, Terrible and
Lurking Terrors, Creeping Horrors, Islands and Triffids are off its prey list.

Boss fireballs respect `mobGriefing` (new in 2.0, on by default). With the gamerule off, an OreSpawn fireball no
longer lights the block it hits or scatters fire from its blast, the way ghast and blaze shots behave; damage, the
ignite of whatever it hits, and the explosion itself are unchanged, and with the rule on nothing changes.
`fireRespectsMobGriefing = false` under `[modern]` (or `modern.enabled = false`) restores the 1.7.10 fire-always
behaviour (MOD-031).

Targeting parity, wave 1 (safety first). On Peaceful the Ant Robot (on foot and ridden), the Cephadrome, the
Dragonfly, wild Gamma Metroids and the Purple Power now stand down exactly as in 1.7.10: the Ant Robot's
stomps and bites, the Cephadrome's hunt (even against whoever hurt it), the Dragonfly's and the Metroid's prey
scans and bites are switched off, and a Purple Power vanishes on its first tick in Peaceful; none of these
despawns on its own, so the port had kept hunting there. With Play Nicely on, OreSpawn's hunters now stop
picking new fights exactly as in 1.7.10 — the Cephadrome, the dragons and their princes, the Leonopteryx, the
five robots, the sea monsters, the Boyfriend and Girlfriend and every bug and beast that used to keep hunting
on a "play nicely" server — reading the flag live, so flipping it takes effect at once; the Hammerhead lets a
grudge rest for the pass as it did, the Nastysaurus and T. Rex drop a prey they had picked themselves (a
grudge they hold is still fought, as before), and Mobzilla no longer forgets the target it was chasing every
time the flag is on.

With Play Nicely on, the Stinky no longer eats blocks while idling (flowers in this port, coal ore in 1.7.10 —
see ENT-S-119) and the Gamma Metroid no longer eats stone, as in 1.7.10 — the two griefing habits outside
target selection the setting used to switch off — read live, so flipping the setting takes effect at once (the
Metroid's stone-eat still also follows `mobGriefing`, as before).

Targeting parity, wave 2. The Attack Squid and the Water Dragon hunt on their own again exactly as in 1.7.10 —
the squid picking through its whitelist (players, zombies, villagers, spiders, lizards, the Girlfriend and
Boyfriend), adopting another squid as a buddy and turning on everything once a Squid Zooka has fired it; the
Water Dragon taking monsters, non-creative swimmers and the shared non-mob prey, sparing its own kind, nothing
but monsters once tamed and nothing at all as a baby — the Dragon once more goes after any nearby monster
continuously (and only with Play Nicely off), and the Islands' vampire butterfly bites players and horses
again. OreSpawn's hunters no longer lock on through walls: the five robots, the Fairy, Giant Robot, Lizard,
Purple Power, Nightmare, Skate, Irukandji, Spider Driver and Ant Robot again look for a clear eye line before
choosing a target, exactly where 1.7.10 checked it, and Spyro, Stinky, the Prince and the Princess again run
1.7.10's second, feet-level ray before biting — so a target on a ledge above or below them, or behind a
parapet their eyes see over, is left alone as it was.

Conventions and the Stinky's idle routine. Hunters take every hostile again. The Leonopteryx, both Princes,
the Boyfriend, the Girlfriend and the Dragon go after anything the original counted as a monster — slimes,
magma cubes and the Ender Dragon included — where the port's target goals had only taken creatures of
vanilla's `Monster` class. The flying Stinky again tunnels toward the nearest coal ore around it (burping when
it eats one), stops eating while told to sit, picks fights while idling as it did in 1.7.10, and — like the
baby dragon — no longer flies toward a spot it cannot see.

Four new `[modern]` switches, on by default (MOD-032..035): tamed companions avenge and defend their owner (`petsDefendOwner`), Mobzilla spares its fellow bosses and the royal family (`godzillaSparesBossPeers`), the Pointysaurus locks on to whoever stares at it (`pointysaurusStareAggro`) and the Cryolophosaurus chases whoever hurt it (`cryolophosaurusRevengeChase`) — each `false`, or `modern.enabled = false`, is the 1.7.10 behaviour; the Mantis loses two target goals that never did anything; and the Valentine's Day Girlfriend keeps leaving Peaceful and creative players alone in both modes (MOD-036, a deliberate exception).

OreSpawn mobs no longer see through grass, flowers, torches and other collision-less blocks — 1.7.10's line of sight stopped on any block with a selection box, the port had inherited vanilla's collision-only ray, and now every OreSpawn hunter (and the Prince's, Kraken's, Brutalfly's and Cockateil's flight rays) reads the 1.7.10 ray while vanilla mobs keep their own.

Every OreSpawn hunter's prey list is 1.7.10's again: the Mantis, Molenoid, Crab, King, Queen and Water Dragon once more go for Leonopteryxes, Dragons, Spyros, Gamma Metroids, Water Dragons, Girlfriends and Boyfriends, villagers, Stinkies and (all but the royals themselves) the royal family, and no longer for a Godzilla Head or the Ender Dragon by a port-only grant; the Dragonfly hunts only ants, butterflies, cockateils, mosquitoes, fireflies and — unless `dragonflyHorseFriendly` is on — horses, and leaves chickens, bats and baby animals alone; the Lizard takes Attack Squids; the Purple Power spares tamed pets (except the two lethal orb types) and the royal family; the Rat leaves Irukandjis, Skates, Whales, Flounders and Dungeon Beasts alone; the Terrible Terror spares the nine more of its Danger-Dimension kin it used to hunt; the Triffid hunts every monster except the seven it always spared and no longer the Dragon; and the Boyfriend and Girlfriend leave zombified piglins and endermen alone and go for Mothra.

OreSpawn's hunters now let go the way 1.7.10 did: a scan's pick is chased only while it is still found, the revenge grudges are forgotten on the original dice (inside the hunting pass, on the grudge alone, and for good), the Nastysaurus, T. Rex and Pointysaurus remember an attacker through walls and at any distance, the Ender Knight and Reaper drop their target at daybreak and never by distance, the creatures whose 1.7.10 revenge task was a dead letter — Alosaurus, Cave Fisher, Dungeon Beast, Urchin, Scorpion, Hammerhead — no longer chase whoever hit them, the Ant Robot and Crab hold grudges against mobs only, Robot2s stop calling their kind, the Cryolophosaurus keeps its single forgiveness, the Boyfriend and Girlfriend guard a 15-block reach, the Dragonfly bites once per pass, the Nastysaurus, T. Rex, Pointysaurus and Sea Viper stand down under Play Nicely, and a ridden Ant Robot or an active Nightmare keeps its first look at you — seen or unseen — until it lands or calms down, the Nightmare now healing on its old dice and settling when it finds nothing.

Companions defend their owner, extended (MOD-033). The `petsDefendOwner` switch now also covers the Boyfriend and the Girlfriend: while it is on (the default), a tamed Boyfriend or Girlfriend goes after whoever hurts its owner and joins its owner's fights, as Leon and the Princes already did; with the key `false`, or `modern.enabled = false`, they fight only what their own monster hunt, jealousy or Valentine's Day goals pick, exactly as in 1.7.10. The Hydrolisc and the Velocity Raptor carry the same owner goals under the key (nothing in their behaviour reads them yet, so there is no visible change either way), and a tamed Leon that already has a target no longer drops it for the nearest monster while the key is on — with it off, Leon's hunt is the 1.7.10 one and takes any monster in sight. The Camarasaurus never had these goals and is unchanged.

Targeting parity, T5b. Five more hunters let a grudge go for good: the Robo-Gunner, Robo-Warrior and Robo-Sniper forget whoever hit them on their 1-in-50 as they did in 1.7.10 and no longer pick the same grudge straight back up a tick later, and a Leonopteryx or a Water Dragon on its 1-in-200 likewise (the Water Dragon rolls it every tick again, target or none, instead of only while it is fighting). A Dragon, Leonopteryx or Prince no longer goes after creepers through its vanilla hunt — 1.7.10's engine refused creepers, as it still refuses ghasts, for every vanilla target task — while its own eyes still take them, as they did; the Boyfriend and Girlfriend keep hunting creepers, as 1.7.10's own task let them. A boxed-in Stinky or Spyro flies at the last spot it tried instead of re-rolling fifty flight targets, and casting fifty block rays, every tick, exactly as 1.7.10 did. And the Stinky tunnels toward deepslate coal ore as well as coal ore — the modern game's two blocks for 1.7.10's one (PN-021).

Targeting parity, wave 3. Every hunter on the wave's list now tells a creative player apart the 1.7.10 way — by creative
mode itself, never by "cannot be hurt": the Ender Knight and Ender Reaper again hunt a survival player who is invulnerable
for some other reason and never a creative one, and keep hold of such a player as they did; the CaterKiller, Sea Viper and
Pointysaurus again pick such a player; the Brutalfly and Mothra go back to hunting nearby mobs while a creative player
stands close (1.7.10 set the creative player aside and went for the mobs; the port had frozen on the player), and Mothra
only strafes a player she can actually see. Fourteen hunters search exactly the space 1.7.10 gave them: the Cater Killer,
Hammerhead, Sea Monster and Sea Viper hunt every living thing in their original boxes again — monsters, villagers and the
original's short list of attackable non-mobs, not players alone — through their original checks (the Cater Killer's own
block-walk sight test, each one's refusal of its own kind, creative players never), on their original dice (the Cater
Killer every 1-in-4 tick, the Sea Viper 1-in-5), and let a pick go the moment the next pass no longer finds it; the
Brutalfly and Mothra strafe a player anywhere in their original box (a corner 42 / 35 blocks out) and no longer one 30 / 25
blocks straight overhead, the Irukandji and Skate the same for their small boxes; the Dragonfly hunts on the ticks it is
NOT re-picking a flight target, so it hunts about every 12 ticks again instead of once in some three minutes (and counts
that target as reached at the original's distance — about a block and a half, not two); the Ender Knight and Ender Reaper
look at the single nearest player every pass, of any mode, and attack only if THAT player is staring at them — a friend
standing closer shields you, as in 1.7.10 — and their 64 / 81 blocks are plain distance (sneaking no longer shrinks it);
the Boyfriend and Girlfriend scan their original 15-block box every pass (not a 15-block sphere on a 1-in-10 roll), have
their creeper hunt back (20 blocks, ahead of the monster hunt), and leave prey they cannot path to alone, as the original's
nearbyOnly did — except creepers, ghasts, Mothra and Valentine players, which the original granted before the reach test;
the Girlfriend's Valentine's Day hunt is the original 16x4x16 box and, as in the original, takes a player she cannot see;
the unridden Ant Robot's hunt box is 24x12x24 again (a quarter of that when ridden); the Nightmare's heal-branch reset and
the grounded Stinky's hunt, restored earlier, are confirmed and pinned. Hunting ranges: a wild Leonopteryx and the Prince,
adult or teen, only go after monsters within 16 blocks again and give up the chase past 16 — the port had them picking
fights, and keeping them up, 40, 64 and 32 blocks out — and they look for one on every pass again instead of one pass in
five; the Pointysaurus notices a player inside its original 12-block box (5 blocks up or down) instead of anywhere within
24 — a player on a ledge well above it is ignored again, one at the box's corner is not — and looks for one at its
original rate (one tick in six, not one in ten); how far they walk, fly or follow their owner is unchanged, and a tamed
pet's defence of its owner (`petsDefendOwner`) and the Pointysaurus's stare (`pointysaurusStareAggro`) are not affected.
Who gets picked first: seventeen more hunters rank their prey the 1.7.10 way again — the Cephadrome, Cryolophosaurus,
Dragonfly, Fairy, Frog, Gamma Metroid, Kyuubi, a wild Leonopteryx's own eyes, the Lizard, Purple Power, Rat, Robot 1, the
Spider Driver (both the robot it climbs onto and the prey it fights), Stinky, Terrible Terror, the teen Prince and the
Triffid. With several candidates in reach, 1.7.10 halved a creeper's squared distance (a creeper 7 blocks off ranks as one
4.95 off) and let a big creature outrank a smaller one standing closer (its size divides the squared distance), so a
creeper draws these hunters first and a large mob nearby beats a small one closer in; the port had simply taken the
nearest. The Boyfriend and Girlfriend prefer creepers the same way in their monster hunt (the squared distance halved, no
size term), exactly as their own 1.7.10 task did; their Valentine's Day hunts still take the nearest. Two equally distant
players facing a Brutalfly's or Mothra's strafe are settled the 1.7.10 way (the last one scanned) — unchanged since the
last batch, now covered by tests. And the odds and ends: the Ender Knight and Ender Reaper have their enderman reflexes
back — stare at one from close by and it blinks away, keep your distance from one that has marked you and after a second
and a half it blinks to your side, and it screams and hisses its stare sound at you as it locks on, exactly as in 1.7.10 —
looking for you every tick again, not every other; the Ender Knight has its enderman rules back too — it attacks only a
player who looks it in the body with a clear line of sight, and a carved pumpkin on your head hides you from it entirely,
as it already did from the Reaper. The Hammerhead and the Irukandji bite on the original two dice (about one time in two
and two in five, instead of one in three and one in four). Lizards pair up again: a Lizard that notices another now and
then adopts it as a buddy and tags along. And one Luna Moth in four — those carrying the butterfly's hidden vampire type
(`butterfly_type == 1`, set by the inherited constructor), whatever its moth skin — hunts players and horses in the
Islands the way the butterfly it descends from does, while still chasing torches under cover. With that, wave 3 of the
targeting parity work is complete: creative-player handling, where the hunters look, how far they look, whom they pick
first, and the odds and ends above are all the original's again.

Memory no longer grows for the whole session with every GeckoLib-drawn OreSpawn mob that has ever been in view.
The animation state the new-style renderers keep per mob (the Beaver, Coin, Elevator, Islands, the five robots,
the Rock Base and the Vortex, when the developer switch draws them through GeckoLib) used to stay in memory for
every mob ever drawn until the game was closed; it is now dropped the moment a mob leaves the loaded world around
you, and cleared entirely when you leave a world, change dimension or are transferred to another server. A mob that comes back into view simply
starts fresh, exactly like one you had never seen. Nothing visible changes. For the curious, `-Dmhlib.counters=true`
now also prints how many of these states are held (`orespawn.geo.managers_held`) and how many were dropped in the
last 100 ticks (`orespawn.geo.evictions`) on the existing counters line.

Nothing player-visible changes in the classic renderers: the Rotator and PurplePower draw exactly as before. For reviewers, the Phase G developer switch (`-Dorespawn.dev.geckolibRenderers=…`) gains two more species — `rotator` and `purple_power` — whose GeckoLib candidates reproduce the classic gyroscope fans and spoke rings through the parity harness's new render-instance expansion (one bone per classic draw).

Targeting parity, wave 4. The last of the targeting follow-ups, and one weapon. The Leonopteryx and the Girlfriend
weigh their fights the 1.7.10 way again: a Leonopteryx that sees a monster goes for it even while it is nursing a
grudge against whoever hit it (the port had let the grudge block its hunt), and a Girlfriend goes for a monster within
reach ahead of an untamed rival near her owner (the port had it the other way round). The Boyfriend and the
Girlfriend hunt only once tamed, as in 1.7.10 — a wild one ignores creepers and monsters until you tame it — and a
Girlfriend told to sit stops hunting (a sitting Boyfriend still does, as in the original); their Valentine's Day
tempers are unchanged. The Pounder, Gunner, Warrior and Sniper robots that forget a grudge on their 1-in-50 do it
before they act, so the pass that forgets an attacker no longer turns, walks or shoots at it once more. A tamed
Stinky flies the 1.7.10 box around its owner (a tighter box near an owner on the ground, a small one around an owner
in flight) instead of the wide box a wild one wanders. Four flyers read their own cell and their flight target the
1.7.10 way below zero and beyond the origin (the Brutalfly, Mothra, the Dragonfly and the butterflies — one block at
most, in where they turn and where the Dragonfly aims at prey). An Ender Knight caught in rain or water blinks away
as its Reaper cousin already did, and both Ender mobs play the enderman's portal sound at both ends of every teleport,
as they did in 1.7.10. The Luna Moth flies its own 1.7.10 pattern again — a wider wander, a slightly slower climb and
a faster flight — and looks for a torch to settle over the way it used to: at night, on most ticks, whether or not the
sky is open, in every direction (the port had looked only under cover, only when re-picking a flight target, and only
east or west). And the Chainsaw's sweep — every living thing within five blocks on a left-click — tells whom it can
see the 1.7.10 way in classic: a walk from the player's waist to the target's middle that any non-air block stops, so
a mob standing in grass, crops or water, or behind a torch, a step or a snow layer, is spared as it was, while one
past a trunk corner is caught. Modern mode keeps the port's line of sight (the vanilla collision ray, which sweeps
through grass, fluids and torches and is stopped by solid corners) under a new `[modern]` switch,
`chainsawSweepVanillaSight`, on by default (MOD-037); `false`, or `modern.enabled = false`, is the 1.7.10 walk.

Dev-only (Phase G slice (d), 2026-09-06): under -Dorespawn.dev.bench=true a /orespawn bench command (op level 2) spawns the six spawn-100 isolation scenes (a wedge inside the camera's field of view, every mob under 180 blocks), samples frame time, CPU, allocation, GC, server MSPT, the MHLib counters and packet bytes for a run, and writes paired classic/candidate reports (with a coverage object and a working-tree reading) to phase_g_reports/benchmark/live/; -Dmhlib.counters=true now also dumps eleven more MHLib counters (four client, seven server, one INFO line per 100 server ticks); no player-facing change.

Nothing player-visible: slice (f) adds a developer tool under tools/ (the artist handoff package generator and its tests) and no shipped asset, class or config changes.

- Dev-only (Phase G slice (c), the G2 root-order contract, 2026-09-06, landed): nothing changes for a player — the classic renderers stay the default and the GeckoLib candidates stay behind `-Dorespawn.dev.geckolibRenderers`. With that switch on, the overlapping parts of a candidate rig (Island, IslandToo, PurplePower, Robot1-5, RockBase, Rotator, Beaver, Elevator, Vortex, Coin) now resolve exactly as the classic renderer draws them: each candidate rig carries the classic draw order (`orespawn:bone_draw_order` in its geo) and the shared replacement model sorts every bake into it before its first draw, so where two faces of one mob share a plane the candidate shows the face the classic renderer shows. A candidate rig that a resource pack re-exports without the order draws in GeckoLib's own order with one logged warning instead of crashing the client; a rig whose order is present and wrong logs an error once and falls back the same way (the reading of "loud" presented to the owner); the build's asset audit refuses a candidate rig shipped without the key.
