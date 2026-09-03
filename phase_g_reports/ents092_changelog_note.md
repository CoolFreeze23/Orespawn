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
