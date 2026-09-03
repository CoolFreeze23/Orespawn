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
box is 5.5x6 as in 1.7.10, and her hit parts now actually follow her bones: a port bug had left them
at her feet whenever another player's client was the one streaming her pose.

