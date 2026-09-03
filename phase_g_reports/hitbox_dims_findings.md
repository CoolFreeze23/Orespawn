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

Additional rows cited by the batch-2 fixers (2026-09-03):

- EntityLunaMoth: hitbox differs independently of shadow - orig EntityLunaMoth.java:27 func_70105_a(0.5f, 0.5f) vs port ModEntities.java:544 .sized(0.4f, 0.4f) (port reuses the butterfly box). Not touched (hitbox out of scope, ModEntities.java off-limits).
- Mothra: orig Mothra.java:65 func_70105_a(5.0f, 2.0f) vs port ModEntities.java:586 .sized(6.0f, 3.0f), deliberately enlarged per the comment at ModEntities.java:581-583 for the wing PartEntities. Renderer scale 10.0 does not touch the hitbox on either side. Not touched.
- Out-of-scope observations from the truth rows, not acted on: port ModelTRex.java:13 ANIM_SPEED = 1.0F replaces the 1.7.10 wingspeed 0.2f (ClientProxyOreSpawn.java:417) - a 5x leg-swing frequency difference; LunaMoth (moth_type 0) and Mothra lack the 1.7.10 creeper-armor scrolling glow pass (RenderButterfly.java:49-87) - presentation, not scale/shadow.
- Basilisk: no hitbox gap - port ModEntities.java:51 .sized(1.6f, 3.5f) equals reference Basilisk.java:49 setSize(1.6f, 3.5f).
- Kyuubi: reference Kyuubi.java:44 setSize(0.5f, 1.25f) vs port ModEntities.java:232 .sized(1.0f, 1.2f) - hitbox-only divergence, out of this batch's scope. Separate non-scale note from the truth row: reference ModelKyuubi.java:432 rotates the whole model 180 degrees about Y inside its push/pop; the port KyuubiModel/KyuubiRenderer does not replicate that facing flip.
- Robot1: reference Robot1.java:39 setSize(0.5f, 0.5f) vs port ModEntities.java:123 .sized(1.0f, 2.0f) - hitbox-only divergence (ModEntities line carries no orig citation), out of scope.
- Robot2: reference Robot2.java:45 setSize(3.0f, 6.2f) vs port ModEntities.java:127 .sized(1.5f, 2.5f) - hitbox-only divergence, out of scope.
- Robot4: reference Robot4.java:49 setSize(2.5f, 4.0f) vs port ModEntities.java:135 .sized(1.5f, 2.5f) - hitbox-only divergence, out of scope.
- All five edited files (and the three Geo descriptors) were pure CRLF before and after the edit; no ModEntities, entity, seam-base, or descriptor-interface files were touched.
- Dragon: reference Dragon.java:102 setSize(1.5f, 1.25f) vs port ModEntities.java:432-434 .sized(1.5f, 2.0f) - height differs (1.25 vs 2.0). Also: a port baby Dragon (reachable via AgeableMob.finalizeSpawn on the island biome's maxCount-2 creature spawner, or /summon with Age) gets a vanilla-halved hitbox and a MobRenderer-halved shadow (0.625) while the mesh draws at 1.0; 1.7.10 had no child branch for Dragon (constant 1.25 shadow, 1.0 scale).
- CliffRacer: port ModEntities.java:379-381 .sized(0.8f, 0.8f) vs 1.7.10 CliffRacer.java:26 func_70105_a(0.75f, 0.5f) - hitbox-size difference, no render-scale path involved.
- Triffid: port ModEntities.java:270-272 .sized(1.0f, 2.0f) vs 1.7.10 Triffid.java:49 func_70105_a(2.0f, 4.0f) - hitbox is half size in both dimensions. Separate non-hitbox observation from the truth table: reference ModelTriffid.render wraps all parts in GL11.glRotatef(-90, 0, 1, 0) (ModelTriffid.java:1401-1405), which the port TriffidModel does not reproduce (orientation, not scale/shadow; out of scope here).
- WormSmall: port ModEntities.java:286 .sized(0.5f, 0.5f) vs 1.7.10 WormSmall.java:27 func_70105_a(0.25f, 1.0f) - out of batch scope, not changed.
- WormMedium: port ModEntities.java:290 .sized(1.0f, 1.0f) vs 1.7.10 WormMedium.java:29 func_70105_a(0.5f, 2.0f) - out of batch scope, not changed.
- WormLarge: port ModEntities.java:297 .sized(1.5f, 1.5f) vs 1.7.10 WormLarge.java:42 func_70105_a(1.55f, 2.5f) - out of batch scope, not changed.
- GoldFish: port ModEntities.java:349 .sized(0.3f, 0.3f) vs 1.7.10 GoldFish.java:25 func_70105_a(0.75f, 0.5f) - out of batch scope, not changed. (A baby, if one ever existed, would get a halved hitbox via vanilla getAgeScale with no render-scale change; getBreedOffspring returns null on both sides.)
- LeafMonster: port .sized(0.8f, 1.5f) (ModEntities.java:236) vs 1.7.10 func_70105_a(1.0f, 2.5f) (reference LeafMonster.java:41) - not touched, out of batch scope.
- EnderKnight: port .sized(0.6f, 1.8f) (ModEntities.java:87) vs 1.7.10 func_70105_a(0.6f, 2.9f) (reference EnderKnight.java:37) - not touched, out of batch scope.
- EnderReaper: port .sized(0.6f, 2.5f) (ModEntities.java:91) vs 1.7.10 func_70105_a(0.7f, 2.9f) (reference EnderReaper.java:37) - not touched, out of batch scope.
- Rotator: port .sized(0.6f, 0.6f) (ModEntities.java:256) vs 1.7.10 func_70105_a(1.0f, 2.0f) (reference Rotator.java:56) - not touched, out of batch scope.
- DungeonBeast: port ModEntities.java:83 .sized(1.5f, 1.5f) vs 1.7.10 DungeonBeast.java:48 func_70105_a(1.15f, 1.1f) — hitbox diverges (not touched; outside this batch).
- Molenoid: port ModEntities.java:248 .sized(1.2f, 2.0f) vs 1.7.10 Molenoid.java:43 func_70105_a(3.9f, 2.6f) — hitbox diverges (not touched; outside this batch).
- EasterBunny: port hitbox 0.4x0.6 (ModEntities.java:333) differs from ref 0.5x0.75 (orig EasterBunny.java:35 setSize) - a .sized change, not a visual-scale path; out of batch scope.
- RockBase: port hitbox 0.5x0.5 (ModEntities.java:645) differs from ref 0.25x0.15 (orig RockBase.java:26) - a .sized change with no visual-scale effect; out of batch scope.
- Frog: port FrogRenderer render() halves the visual for isBaby() on top of vanilla's age-scaled hitbox (LivingEntity getDefaultDimensions x getAgeScale 0.5), whereas 1.7.10 halved only the hitbox and drew babies full-size; port-only visual divergence but practically unreachable since Frog.getBreedOffspring returns null in both versions. Left untouched per the 'leave scale code alone' instruction.
- GiantRobot: port EntityType .sized(4.0f, 10.0f) (ModEntities.java:95) vs orig setSize(3.0f, 9.75f) (reference GiantRobot.java:46) - hitbox-only delta, not touched by this batch.
- SpiderRobot / AntRobot (vertical placement, not hitbox or shadow): the 1.7.10 renderers bypassed the RendererLivingEntity pipeline entirely (no -24/16 = 1.5-block lift, RenderSpiderRobot.java:27-35 / RenderAntRobot.java:27-35), while the port runs MobRenderer's full pipeline with the -1.501 lift (ModernSpiderGait.VANILLA_RENDER_Y_OFFSET) and unchanged part pivots - a geometry-leg question flagged in the truth table, out of scope here.
- Crab: hitbox scales with getCrabScale in both eras via the entity (orig per-tick setSize Crab.java:133 / port getDefaultDimensions Crab.java:127-133) - faithfully ported entity-side behaviour, no renderer-side hitbox effect.
- Crab (pin-manifest, not hitbox): promoting the Crab entry to status=pin under the current pin semantics reports DIVERGES on the scale axis only (dynamic getCrabScale on both sides, expected_scale null); shadow axis passes. Needs a manifest ruling for the dynamic-scale axis before promotion.
