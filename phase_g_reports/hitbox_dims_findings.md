# ENT-S-095 — entity hitbox dimensions diverging from the 1.7.10 `setSize` (REPORT, 2026-09-03)

Found in passing by the ENT-S-092 renderer reads (every scale path per entity, refuted per chunk). 1.7.10 renderer scale
never touched the hitbox, so these are independent of the renderer fix. No MOD record covers them; by the standing rule
each is a parity bug unless a MOD record is written. Vortex was already fixed under ENT-S-089. Presented for the split.

| entity | 1.7.10 setSize (w x h) | port .sized (w x h) |
|---|---|---|
| Baryonyx | 1.5 x 2.8 | 1 x 1.5 |
| CliffRacer | 0.75 x 0.5 | 0.8 x 0.8 |
| Cockateil | 0.5 x 0.5 | 0.4 x 0.4 |
| Coin | 1.5 x 1.5 | 0.4 x 0.4 |
| Cricket | 0.1 x 0.1 | 0.4 x 0.4 |
| Dragon | 1.5 x 1.25 | 1.5 x 2 |
| Dragonfly | 1.5 x 0.5 | 0.4 x 0.4 |
| DungeonBeast | 1.15 x 1.1 | 1.5 x 1.5 |
| EmperorScorpion | 3.5 x 3 | 1.5 x 1.5 |
| EnderKnight | 0.6 x 2.9 | 0.6 x 1.8 |
| EnderReaper | 0.7 x 2.9 | 0.6 x 2.5 |
| EntityAnt | 0.1 x 0.1 | 0.4 x 0.4 |
| EntityLunaMoth | 0.5 x 0.5 | 0.4 x 0.4 |
| EntityMosquito | 0.2 x 0.2 | 0.3 x 0.3 |
| EntityRainbowAnt | 0.1 x 0.1 | 0.4 x 0.4 |
| EntityRedAnt | 0.2 x 0.2 | 0.4 x 0.4 |
| EntityUnstableAnt | 0.1 x 0.1 | 0.4 x 0.4 |
| Firefly | 0.4 x 0.8 | 0.2 x 0.2 |
| Flounder | 0.55 x 0.25 | 0.5 x 0.3 |
| GiantRobot | 3 x 9.75 | 4 x 10 |
| GoldFish | 0.75 x 0.5 | 0.3 x 0.3 |
| HerculesBeetle | 3.25 x 2.75 | 1.2 x 1 |
| Irukandji | 0.25 x 0.25 | 0.4 x 0.4 |
| Kyuubi | 0.5 x 1.25 | 1 x 1.2 |
| LeafMonster | 1 x 2.5 | 0.8 x 1.5 |
| Lizard | 1.5 x 1.25 | 0.6 x 0.6 |
| LurkingTerror | 1.75 x 1.25 | 1 x 1 |
| Mantis | 2.5 x 3.25 | 0.8 x 1.8 |
| Molenoid | 3.9 x 2.6 | 1.2 x 2 |
| Mothra | 5 x 2 | 6 x 3 |
| PurplePower | 0.75 x 0.75 | 0.5 x 0.5 |
| Rat | 0.25 x 0.5 | 0.4 x 0.4 |
| Robot1 | 0.5 x 0.5 | 1 x 2 |
| Robot4 | 2.5 x 4 | 1.5 x 2.5 |
| Robot5 | 1 x 2.25 | 1 x 1.5 |
| Rotator | 1 x 2 | 0.6 x 0.6 |
| RubyBird | 0.5 x 0.5 | 0.4 x 0.4 |
| Scorpion | 0.85 x 0.55 | 0.6 x 0.4 |
| SeaMonster | 1.25 x 2.5 | 5 x 5 |
| Skate | 0.75 x 0.25 | 0.8 x 0.4 |
| SpitBug | 2 x 2 | 0.8 x 0.8 |
| StinkBug | 0.55 x 0.55 | 0.4 x 0.4 |
| Termite | 0.2 x 0.2 | 0.3 x 0.3 |
| TerribleTerror | 1 x 0.75 | 0.5 x 0.5 |
| Triffid | 2 x 4 | 1 x 2 |
| TrooperBug | 3 x 3.5 | 1.2 x 1.5 |
| Tshirt | 4 x 4 | 0.6 x 1.8 |
| Urchin | 1.35 x 2.1 | 0.5 x 0.5 |
| WormLarge | 1.55 x 2.5 | 1.5 x 1.5 |
| WormMedium | 0.5 x 2 | 1 x 1 |
| WormSmall | 0.25 x 1 | 0.5 x 0.5 |

Additional rows cited by the batch-1a fixers (same evidence, phrased without a parsable pair):

- Irukandji: 1.7.10 Irukandji.java:42 func_70105_a(0.25f, 0.25f) vs port ModEntities.java:105-107 .sized(0.4f, 0.4f) — independent hitbox divergence, not touched (truth row hitbox_note).
- Cricket: 1.7.10 Cricket.java:24 func_70105_a(0.1f, 0.1f) vs port ModEntities.java:383-385 .sized(0.4f, 0.4f) — independent hitbox divergence, not touched (truth row hitbox_note).
- Robot3: 1.7.10 Robot3.java:46 setSize(2.5f, 5.0f) vs port ModEntities.java:129-131 .sized(2.0f, 3.0f). Not touched (out of scope for ENT-S-092).
- Dragonfly: 1.7.10 Dragonfly.java:40 func_70105_a(1.5f, 0.5f) vs port ModEntities.java:388-389 .sized(0.4f, 0.4f). Not touched.
- EmperorScorpion: 1.7.10 EmperorScorpion.java:59 func_70105_a(3.5f, 3.0f) vs port ModEntities.java:221-223 .sized(1.5f, 1.5f). Not touched.
- Firefly: 1.7.10 Firefly.java:29 func_70105_a(0.4f, 0.8f) vs port ModEntities.java:554-556 .sized(0.2f, 0.2f) - separate hitbox gap, not touched (truth row hitbox_note).
- Cockateil: 1.7.10 Cockateil.java:42 func_70105_a(0.5f, 0.5f) vs port ModEntities.java:319-321 .sized(0.4f, 0.4f) - separate hitbox gap, not touched.
- RubyBird: inherits 1.7.10 Cockateil.java:42 func_70105_a(0.5f, 0.5f) (RubyBird.java:9-31 has no own setSize) vs port ModEntities.java:706-708 .sized(0.4f, 0.4f) - separate hitbox gap, not touched.
- Scorpion: 1.7.10 Scorpion.java:50 func_70105_a(0.85f, 0.55f) vs port ModEntities.java:258-260 .sized(0.6f, 0.4f) - separate hitbox gap, not touched.
- VelocityRaptor: no hitbox divergence - 1.7.10 VelocityRaptor.java:48 func_70105_a(0.5f, 0.6f) equals port ModEntities.java:528-530 .sized(0.5f, 0.6f).
- Spyro: no hitbox divergence - 1.7.10 Spyro.java:67 func_70105_a(0.5f, 0.5f) equals port ModEntities.java:502-504 .sized(0.5f, 0.5f).
- SpitBug: 1.7.10 SpitBug.java:56 func_70105_a(2.0f, 2.0f) vs port ModEntities.java:264 .sized(0.8f, 0.8f) - hitbox differs (port 2.5x smaller per axis); verified in both sources this pass. Not touched (out of ENT-S-092 scope).
- TerribleTerror: 1.7.10 TerribleTerror.java:51 func_70105_a(1.0f, 0.75f) vs port ModEntities.java:268 .sized(0.5f, 0.5f) - hitbox differs; verified in both sources this pass. Not touched.
- Rat: 1.7.10 Rat.java:52 func_70105_a(0.25f, 0.5f) vs port ModEntities.java:252 .sized(0.4f, 0.4f) - hitbox differs (wider, shorter); verified in both sources this pass. Not touched.
- RubberDucky: 1.7.10 setSize(0.33f, 0.5f) (reference_1_7_10_source/sources/danger/orespawn/RubberDucky.java:63) vs port .sized(0.4f, 0.4f) (src/main/java/danger/orespawn/ModEntities.java:498-500). Not touched.
- Urchin: 1.7.10 setSize(1.35f, 2.1f) (reference_1_7_10_source/sources/danger/orespawn/Urchin.java:50) vs port .sized(0.5f, 0.5f) (src/main/java/danger/orespawn/ModEntities.java:157-160). Large divergence, not touched.
- Hammerhead: 1.7.10 setSize(3.0f, 5.0f) (reference_1_7_10_source/sources/danger/orespawn/Hammerhead.java:44) vs port .sized(2.8f, 1.8f) (src/main/java/danger/orespawn/ModEntities.java:101-103). Not touched.
- LurkingTerror: 1.7.10 setSize(1.75f, 1.25f) (reference_1_7_10_source/sources/danger/orespawn/LurkingTerror.java:53) vs port .sized(1.0f, 1.0f) (src/main/java/danger/orespawn/ModEntities.java:238-240). Not touched.
- TrooperBug: 1.7.10 TrooperBug.java:58 func_70105_a(3.0f, 3.5f) vs port ModEntities.java:275-276 .sized(1.2f, 1.5f) - large independent hitbox divergence, not touched by this batch.
- Mantis: 1.7.10 Mantis.java:57 setSize(2.5f, 3.25f) vs port ModEntities.java:242-244 .sized(0.8f, 1.8f) - large independent hitbox divergence, not touched by this batch.
- HerculesBeetle: 1.7.10 HerculesBeetle.java:46 setSize(3.25f, 2.75f) vs port ModEntities.java:225-227 .sized(1.2f, 1.0f) - large independent hitbox divergence, not touched by this batch.
- Lizard: 1.7.10 Lizard.java:57 setSize(1.5f, 1.25f) vs port ModEntities.java:488-490 .sized(0.6f, 0.6f) (truth row hitbox_note; citations reference_1_7_10_source/sources/danger/orespawn/Lizard.java:57, src/main/java/danger/orespawn/ModEntities.java:488-490)
- Flounder: 1.7.10 Flounder.java:39 setSize(0.55f, 0.25f) vs port ModEntities.java:335-337 .sized(0.5f, 0.3f) (truth row hitbox_note; citations reference_1_7_10_source/sources/danger/orespawn/Flounder.java:39, src/main/java/danger/orespawn/ModEntities.java:337)
- PurplePower: 1.7.10 PurplePower.java:40 func_70105_a(0.75f, 0.75f) vs port ModEntities.java:639-641 .sized(0.5f, 0.5f) (truth row hitbox_note; citations reference_1_7_10_source/sources/danger/orespawn/PurplePower.java:40, src/main/java/danger/orespawn/ModEntities.java:641)

Notes in the truth table that mention a hitbox difference without a numeric pair (need a read before they count):
Girlfriend, EnchantedCow, TRex, Basilisk, Hydrolisc, VelocityRaptor, Bee, Spyro, GammaMetroid, CaveFisher, Alien, WaterDragon,
AttackSquid, Robot2, Robot3, Ostrich, Island, IslandToo, CreepingHorror, Godzilla, Whale, Stinky, TheKing, TheQueen, ThePrince,
SeaViper, EasterBunny, CaterKiller, Hammerhead, BandP, RockBase, Brutalfly, Frog, SpiderDriver.

Source: scratch truth table of 133 registrations (`ents092_truth.json`, verified by independent refuters per chunk).
