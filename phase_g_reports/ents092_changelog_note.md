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

`modern.enabled` is now the master override for every 2.0 feature. Off (the default) forces
`spiderMovement`, `mountCamera`, `phase14ContentEnable` and `mothraWideRootHitbox` to their
classic/off values whatever they are set to; on defers to each key, which keeps its name and
place. On a default config this means robot spiders and ants now run the classic 1.7.10 gait
and the riding camera is off until `modern.enabled = true` (previously `spiderMovement = MODERN`,
the key's default, was enough on its own). New modern features register under `[modern]`.

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
