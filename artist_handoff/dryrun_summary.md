# Dry-run summary — 0.2.9 (the full folder, 2026-09-13, fourth set, items 5, 7 and 8: every artist-tier species packaged - a rig not yet in-game from the reference leg's converter output, bone names final; the Tier-1 bosses' intended locked bones from the design's section 6 rendered in section 7 and validated against the geo; the README's priority table the full list, bosses first, closing with the deliverable count)

Output: `artist_handoff`

## Counts (the deliverable)

- Tier 1: 26 registries over 20 rigs (a shared rig counted once); not yet in-game: 25 registries over 19 rigs.
- Tier 2: 73 registries over 68 rigs (a shared rig counted once); not yet in-game: 43 registries over 43 rigs.
- Tier 3: 13 registries over 12 rigs (a shared rig counted once).
- Folders: 112 (one per registry); files: 995 (989 in the entity folders + the 6 package-wide files).
- Rig sources: reference-leg converter output 68, shipped 44.
- Packaged without a seed (the SEED_MISSING fallback: the display name from the registry, empty authored sections): 0.
- Artist-tier species with no rig to package: 4 — giant_robot (the converter refused reference_giantrobot (ValueError: reference_giantrobot capture bind draws [...], which are not cube-bearing geo bones (a part drawn more than once without render_instances, or a part the rig lacks))); jeffery (the converter refused reference_giantrobot (ValueError: reference_giantrobot capture bind draws [...], which are not cube-bearing geo bones (a part drawn more than once without render_instances, or a part the rig lacks))); boyfriend (no entry whose class is ModelBoyfriend in reference_model_proofs.json — no reference-leg geo can exist for it until the reference leg covers the model); girlfriend (no entry whose class is ModelGirlfriend in reference_model_proofs.json — no reference-leg geo can exist for it until the reference leg covers the model).

| entity | tier | rig | files | bones | cubes | clips shipped | clips in SPEC | goals | flags | attacking | strike/launch sites | textures | locked | unlabelled | round-trip | effort | warnings |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| alien | Tier 1 (boss) | reference leg (not yet in-game) | 8 | 55 | 55 | 0 | 16 | 7 | 1 | STATE | 2 | 1 | 16 (0 keyed by the shipped clips; warn) | 0 | EQUAL, order kept | 38.8 h | 2 |
| alien_boss | Tier 1 (boss) | reference leg (not yet in-game) | 8 | 55 | 55 | 0 | 16 | 1 | 0 | NONE | 1 | 1 | 16 (0 keyed by the shipped clips; warn) | 0 | EQUAL, order kept | 37.2 h | 0 |
| alosaurus | Tier 2 | reference leg (not yet in-game) | 8 | 21 | 21 | 0 | 12 | 6 | 1 | STATE | 1 | 1 | 0 | 0 | EQUAL, order kept | 20.2 h | 1 |
| attack_squid | Tier 2 | reference leg (not yet in-game) | 8 | 9 | 9 | 0 | 13 | 5 | 1 | STATE | 2 | 1 | 0 | 0 | EQUAL, order kept | 18.8 h | 0 |
| band_p | Tier 2 | reference leg (not yet in-game) | 9 | 7 | 7 | 0 | 12 | 6 | 1 | NONE | 1 | 2 | 0 | 0 | EQUAL, order kept | 15.4 h | 2 |
| basilisk | Tier 1 (boss) | reference leg (not yet in-game) | 8 | 21 | 21 | 0 | 10 | 6 | 1 | STATE | 2 | 1 | 8 (0 keyed by the shipped clips; warn) | 0 | EQUAL, order kept | 24.6 h | 1 |
| cave_fisher | Tier 2 | reference leg (not yet in-game) | 8 | 75 | 75 | 0 | 10 | 5 | 1 | NONE | 1 | 1 | 0 | 0 | EQUAL, order kept | 29 h | 1 |
| cloud_shark | Tier 2 | shipped | 9 | 8 | 8 | 0 | 11 | 0 | 0 | NONE | 1 | 1 | 0 | 0 | EQUAL, order kept | 15.6 h | 0 |
| crab | Tier 2 | reference leg (not yet in-game) | 8 | 25 | 25 | 0 | 14 | 5 | 2 | STATE | 1 | 1 | 0 | 0 | EQUAL, order kept | 23 h | 0 |
| creeping_horror | Tier 2 | reference leg (not yet in-game) | 8 | 26 | 26 | 0 | 13 | 7 | 0 | NONE | 1 | 1 | 0 | 0 | EQUAL, order kept | 21.2 h | 1 |
| cryolophosaurus | Tier 2 | reference leg (not yet in-game) | 8 | 20 | 20 | 0 | 9 | 7 | 0 | NONE | 1 | 1 | 0 | 0 | EQUAL, order kept | 16 h | 0 |
| dungeon_beast | Tier 2 | reference leg (not yet in-game) | 8 | 64 | 64 | 0 | 14 | 5 | 1 | NONE | 1 | 1 | 0 | 0 | EQUAL, order kept | 30.8 h | 0 |
| ender_knight | Tier 2 | reference leg (not yet in-game) | 8 | 40 | 40 | 0 | 11 | 7 | 1 | NONE | 0 | 1 | 0 | 0 | EQUAL, order kept | 23 h | 2 |
| ender_reaper | Tier 2 | reference leg (not yet in-game) | 8 | 66 | 66 | 0 | 12 | 7 | 1 | NONE | 0 | 1 | 0 | 0 | EQUAL, order kept | 28.2 h | 2 |
| hammerhead | Tier 1 (boss) | reference leg (not yet in-game) | 8 | 37 | 37 | 0 | 12 | 4 | 1 | STATE | 2 | 1 | 18 (0 keyed by the shipped clips; warn) | 0 | EQUAL, order kept | 30.1 h | 0 |
| irukandji | Tier 2 | shipped | 9 | 9 | 9 | 0 | 14 | 5 | 1 | STATE | 1 | 1 | 0 | 0 | EQUAL, order kept | 19.8 h | 1 |
| nastysaurus | Tier 2 | reference leg (not yet in-game) | 8 | 59 | 59 | 0 | 13 | 6 | 1 | NONE | 1 | 1 | 0 | 0 | EQUAL, order kept | 27.8 h | 1 |
| pitch_black | Tier 1 (boss) | reference leg (not yet in-game) | 8 | 101 | 101 | 0 | 16 | 4 | 4 | STATE | 1 | 1 | 14 (0 keyed by the shipped clips; warn) | 0 | EQUAL, order kept | 47.1 h | 0 |
| pointysaurus | Tier 2 | reference leg (not yet in-game) | 8 | 30 | 30 | 0 | 13 | 8 | 1 | NONE | 1 | 1 | 0 | 0 | EQUAL, order kept | 22 h | 2 |
| robot_1 | Tier 3 | shipped | 9 | 27 | 27 | 0 | 0 | 5 | 1 | STATE | 0 | 1 | 0 | 20 | EQUAL, order kept | 0 h | 20 |
| robot_2 | Tier 3 | shipped | 9 | 15 | 15 | 0 | 0 | 5 | 1 | STATE | 1 | 1 | 0 | 4 | EQUAL, order kept | 0 h | 4 |
| robot_3 | Tier 3 | shipped | 9 | 19 | 19 | 0 | 0 | 5 | 1 | MIXED | 1 | 1 | 0 | 0 | EQUAL, order kept | 0 h | 1 |
| robot_4 | Tier 3 | shipped | 9 | 56 | 56 | 0 | 0 | 5 | 2 | EVENT | 3 | 1 | 0 | 0 | EQUAL, order kept | 0 h | 0 |
| robot_5 | Tier 3 | shipped | 9 | 11 | 11 | 0 | 0 | 5 | 1 | MIXED | 1 | 1 | 0 | 0 | EQUAL, order kept | 0 h | 1 |
| sea_monster | Tier 1 (boss) | reference leg (not yet in-game) | 8 | 23 | 23 | 0 | 14 | 6 | 1 | STATE | 2 | 1 | 13 (0 keyed by the shipped clips; warn) | 0 | EQUAL, order kept | 32.5 h | 0 |
| sea_viper | Tier 2 | reference leg (not yet in-game) | 8 | 34 | 34 | 0 | 13 | 8 | 1 | NONE | 1 | 1 | 0 | 0 | EQUAL, order kept | 23.8 h | 3 |
| skate | Tier 2 | shipped | 9 | 3 | 3 | 0 | 8 | 5 | 1 | STATE | 1 | 1 | 0 | 0 | EQUAL, order kept | 12.6 h | 1 |
| trex | Tier 1 (boss) | reference leg (not yet in-game) | 8 | 27 | 27 | 0 | 11 | 6 | 1 | NONE | 1 | 1 | 8 (0 keyed by the shipped clips; warn) | 0 | EQUAL, order kept | 25.6 h | 0 |
| urchin | Tier 2 | reference leg (not yet in-game) | 8 | 17 | 17 | 0 | 13 | 4 | 1 | STATE | 3 | 1 | 0 | 0 | EQUAL, order kept | 19.4 h | 0 |
| godzilla | Tier 1 (boss) | reference leg (not yet in-game) | 8 | 71 | 71 | 0 | 18 | 5 | 2 | STATE | 6 | 1 | 12 (0 keyed by the shipped clips; warn) | 0 | EQUAL, order kept | 44.1 h | 0 |
| kraken | Tier 1 (boss) | reference leg (not yet in-game) | 8 | 111 | 111 | 0 | 18 | 2 | 2 | STATE? | 1 | 1 | 14 (0 keyed by the shipped clips; warn) | 0 | EQUAL, order kept | 50.1 h | 1 |
| the_king | Tier 1 (boss) | reference leg (not yet in-game) | 8 | 119 | 119 | 0 | 23 | 5 | 3 | STATE | 5 | 1 | 15 (0 keyed by the shipped clips; warn) | 0 | EQUAL, order kept | 60.3 h | 2 |
| the_queen | Tier 1 (boss; the design's 'done' row) | shipped | 9 | 110 | 130 | 8 | 8 | 5 | 6 | STATE | 6 | 2 | 27 (26 keyed by the shipped clips; warn) | 0 | EQUAL, order kept | 27.5 h | 1 |
| bee | Tier 2 | shipped | 9 | 23 | 23 | 0 | 16 | 0 | 1 | STATE | 2 | 1 | 0 | 0 | EQUAL, order kept | 24.6 h | 0 |
| brutalfly | Tier 2 | shipped | 9 | 14 | 14 | 2 | 7 | 0 | 0 | NONE | 3 | 1 | 0 | 0 | EQUAL, order kept | 11.8 h | 0 |
| cater_killer | Tier 2 | shipped | 9 | 80 | 80 | 0 | 21 | 6 | 1 | NONE | 2 | 1 | 0 | 0 | EQUAL, order kept | 40 h | 0 |
| emperor_scorpion | Tier 1 (boss) | reference leg (not yet in-game) | 8 | 78 | 78 | 0 | 12 | 6 | 1 | NONE | 1 | 1 | 48 (0 keyed by the shipped clips; warn) | 0 | EQUAL, order kept | 34.7 h | 1 |
| hercules_beetle | Tier 2 | shipped | 9 | 37 | 37 | 0 | 9 | 6 | 1 | NONE | 2 | 1 | 0 | 0 | EQUAL, order kept | 19.4 h | 0 |
| kyuubi | Tier 2 | reference leg (not yet in-game) | 8 | 42 | 42 | 0 | 15 | 6 | 0 | NONE | 2 | 1 | 0 | 0 | EQUAL, order kept | 26.4 h | 0 |
| leaf_monster | Tier 2 | reference leg (not yet in-game) | 8 | 5 | 5 | 0 | 12 | 3 | 1 | STATE | 1 | 1 | 0 | 0 | EQUAL, order kept | 17 h | 0 |
| lurking_terror | Tier 2 | reference leg (not yet in-game) | 8 | 59 | 59 | 0 | 15 | 0 | 1 | STATE | 1 | 1 | 0 | 0 | EQUAL, order kept | 30.8 h | 0 |
| mantis | Tier 2 | reference leg (not yet in-game) | 8 | 36 | 36 | 0 | 10 | 0 | 1 | STATE | 2 | 1 | 0 | 0 | EQUAL, order kept | 21.2 h | 0 |
| molenoid | Tier 2 | reference leg (not yet in-game) | 8 | 37 | 37 | 0 | 12 | 5 | 1 | STATE | 2 | 1 | 0 | 0 | EQUAL, order kept | 22.4 h | 0 |
| rat | Tier 2 | reference leg (not yet in-game) | 8 | 12 | 12 | 0 | 11 | 6 | 1 | STATE | 1 | 1 | 0 | 0 | EQUAL, order kept | 16.4 h | 0 |
| rotator | Tier 3 | shipped | 9 | 27 | 24 | 0 | 0 | 0 | 0 | NONE | 1 | 1 | 0 | 0 | EQUAL, order kept | 0 h | 0 |
| scorpion | Tier 2 | reference leg (not yet in-game) | 8 | 22 | 22 | 0 | 13 | 5 | 1 | NONE | 2 | 1 | 0 | 0 | EQUAL, order kept | 20.4 h | 1 |
| spit_bug | Tier 2 | reference leg (not yet in-game) | 8 | 93 | 93 | 0 | 11 | 6 | 1 | NONE | 3 | 1 | 0 | 0 | EQUAL, order kept | 32.6 h | 1 |
| terrible_terror | Tier 2 | shipped | 9 | 21 | 21 | 0 | 11 | 0 | 0 | NONE | 1 | 1 | 0 | 0 | EQUAL, order kept | 18.2 h | 0 |
| triffid | Tier 2 | reference leg (not yet in-game) | 8 | 178 | 178 | 0 | 11 | 4 | 2 | STATE | 1 | 1 | 0 | 0 | EQUAL, order kept | 50.6 h | 0 |
| trooper_bug | Tier 2 | reference leg (not yet in-game) | 8 | 134 | 134 | 0 | 19 | 6 | 1 | NONE | 1 | 1 | 0 | 0 | EQUAL, order kept | 48.8 h | 2 |
| vortex | Tier 3 | shipped | 9 | 1 | 1 | 0 | 0 | 0 | 0 | NONE | 1 | 1 | 0 | 0 | EQUAL, order kept | 0 h | 0 |
| worm_small | Tier 2 | shipped | 9 | 3 | 3 | 0 | 11 | 0 | 0 | NONE | 1 | 1 | 0 | 0 | EQUAL, order kept | 14.6 h | 0 |
| worm_medium | Tier 2 | shipped | 9 | 8 | 8 | 0 | 13 | 0 | 0 | NONE | 1 | 1 | 0 | 0 | EQUAL, order kept | 17.6 h | 0 |
| worm_large | Tier 2 | shipped | 9 | 23 | 23 | 0 | 13 | 4 | 0 | NONE | 1 | 1 | 0 | 0 | EQUAL, order kept | 20.6 h | 0 |
| baryonyx | Tier 2 | reference leg (not yet in-game) | 8 | 52 | 52 | 0 | 9 | 7 | 0 | NONE | 0 | 1 | 0 | 0 | EQUAL, order kept | 22.4 h | 1 |
| beaver | Tier 2 | shipped | 9 | 9 | 9 | 4 | 11 | 8 | 0 | NONE | 0 | 1 | 0 | 0 | EQUAL, order kept | 12.8 h | 0 |
| cassowary | Tier 2 | reference leg (not yet in-game) | 8 | 12 | 12 | 0 | 8 | 8 | 0 | NONE | 0 | 1 | 0 | 0 | EQUAL, order kept | 13.4 h | 1 |
| chipmunk | Tier 2 | reference leg (not yet in-game) | 8 | 18 | 18 | 0 | 9 | 12 | 0 | NONE | 0 | 1 | 0 | 0 | EQUAL, order kept | 15.6 h | 2 |
| cockateil | Tier 2 | shipped | 14 | 16 | 16 | 6 | 14 | 0 | 1 | NONE | 0 | 6 | 0 | 0 | EQUAL, order kept | 11.2 h | 0 |
| coin | Tier 3 | shipped | 9 | 1 | 1 | 0 | 0 | 1 | 0 | NONE | 0 | 1 | 0 | 0 | EQUAL, order kept | 0 h | 0 |
| easter_bunny | Tier 2 | reference leg (not yet in-game) | 8 | 13 | 13 | 0 | 9 | 8 | 0 | NONE | 0 | 1 | 0 | 0 | EQUAL, order kept | 14.6 h | 0 |
| flounder | Tier 2 | reference leg (not yet in-game) | 8 | 6 | 6 | 0 | 7 | 7 | 0 | NONE | 0 | 1 | 0 | 0 | EQUAL, order kept | 11.2 h | 1 |
| frog | Tier 2 | reference leg (not yet in-game) | 8 | 10 | 10 | 0 | 13 | 3 | 1 | NONE | 1 | 1 | 0 | 0 | EQUAL, order kept | 18 h | 1 |
| gazelle | Tier 2 | reference leg (not yet in-game) | 8 | 34 | 34 | 0 | 12 | 10 | 0 | NONE | 0 | 1 | 0 | 0 | EQUAL, order kept | 20.8 h | 3 |
| gold_fish | Tier 2 | shipped | 9 | 16 | 16 | 7 | 16 | 0 | 0 | NONE | 0 | 1 | 0 | 0 | EQUAL, order kept | 11.2 h | 0 |
| island | Tier 3 | shipped | 9 | 3 | 3 | 0 | 0 | 0 | 0 | NONE | 0 | 1 | 0 | 0 | EQUAL, order kept | 0 h | 0 |
| island_too | Tier 3 | shipped | 9 | 3 | 3 | 0 | 0 | 0 | 0 | NONE | 0 | 1 | 0 | 0 | EQUAL, order kept | 0 h | 0 |
| peacock | Tier 2 | reference leg (not yet in-game) | 8 | 16 | 16 | 0 | 10 | 7 | 0 | NONE | 0 | 1 | 0 | 0 | EQUAL, order kept | 15.2 h | 1 |
| whale | Tier 2 | reference leg (not yet in-game) | 8 | 14 | 14 | 0 | 10 | 7 | 0 | NONE | 0 | 1 | 0 | 0 | EQUAL, order kept | 15.8 h | 2 |
| ant | Tier 2 | shipped | 9 | 20 | 20 | 3 | 8 | 2 | 0 | NONE | 0 | 1 | 0 | 0 | EQUAL, order kept | 12 h | 0 |
| cliff_racer | Tier 2 | shipped | 9 | 8 | 8 | 2 | 6 | 0 | 0 | NONE | 0 | 1 | 0 | 0 | EQUAL, order kept | 9.6 h | 0 |
| cricket | Tier 2 | shipped | 9 | 11 | 11 | 0 | 8 | 3 | 1 | NONE | 0 | 1 | 0 | 0 | EQUAL, order kept | 13.2 h | 0 |
| dragonfly | Tier 2 | shipped | 9 | 26 | 26 | 3 | 9 | 1 | 0 | NONE | 1 | 1 | 0 | 0 | EQUAL, order kept | 14.2 h | 1 |
| red_ant | Tier 2 | shipped | 9 | 20 | 20 | 3 | 9 | 4 | 0 | NONE | 1 | 1 | 0 | 0 | EQUAL, order kept | 13 h | 0 |
| rainbow_ant | Tier 2 | shipped | 9 | 20 | 20 | 3 | 8 | 2 | 0 | NONE | 0 | 1 | 0 | 0 | EQUAL, order kept | 12 h | 0 |
| stink_bug | Tier 2 | reference leg (not yet in-game) | 8 | 50 | 50 | 0 | 14 | 7 | 0 | NONE | 0 | 1 | 0 | 0 | EQUAL, order kept | 26 h | 0 |
| termite | Tier 2 | shipped | 9 | 20 | 20 | 3 | 9 | 4 | 0 | NONE | 1 | 1 | 0 | 0 | EQUAL, order kept | 13 h | 0 |
| tshirt | Tier 2 | shipped | 9 | 2 | 2 | 2 | 5 | 0 | 0 | NONE | 0 | 1 | 0 | 0 | EQUAL, order kept | 7.4 h | 0 |
| unstable_ant | Tier 2 | shipped | 9 | 20 | 20 | 3 | 8 | 2 | 0 | NONE | 0 | 1 | 0 | 0 | EQUAL, order kept | 12 h | 0 |
| camarasaurus | Tier 2 | reference leg (not yet in-game) | 8 | 21 | 21 | 0 | 10 | 10 | 0 | NONE | 0 | 1 | 0 | 0 | EQUAL, order kept | 17.2 h | 3 |
| dragon | Tier 1 (boss) | reference leg (not yet in-game) | 9 | 55 | 55 | 0 | 17 | 9 | 4 | STATE | 9 | 2 | 11 (0 keyed by the shipped clips; warn) | 0 | EQUAL, order kept | 41.8 h | 3 |
| baby_dragon | Tier 1 (boss) | reference leg (not yet in-game) | 9 | 55 | 55 | 0 | 15 | 0 | 0 | NONE | 0 | 2 | 11 (0 keyed by the shipped clips; warn) | 0 | EQUAL, order kept | 37.2 h | 0 |
| cannon_fodder | Tier 2 | shipped | 9 | 6 | 6 | 0 | 8 | 0 | 2 | NONE | 0 | 1 | 0 | 0 | EQUAL, order kept | 12.2 h | 0 |
| gamma_metroid | Tier 2 | shipped | 9 | 21 | 21 | 0 | 11 | 11 | 0 | NONE | 1 | 1 | 0 | 0 | EQUAL, order kept | 18.2 h | 2 |
| hydrolisc | Tier 2 | reference leg (not yet in-game) | 8 | 40 | 40 | 0 | 13 | 12 | 0 | NONE | 0 | 1 | 0 | 0 | EQUAL, order kept | 24 h | 2 |
| leon | Tier 1 (boss) | reference leg (not yet in-game) | 8 | 98 | 98 | 0 | 22 | 10 | 3 | STATE | 2 | 1 | 22 (0 keyed by the shipped clips; warn) | 0 | EQUAL, order kept | 55.7 h | 2 |
| leonopteryx | Tier 1 (boss) | reference leg (not yet in-game) | 8 | 98 | 98 | 0 | 22 | 10 | 3 | STATE | 2 | 1 | 22 (0 keyed by the shipped clips; warn) | 0 | EQUAL, order kept | 55.7 h | 2 |
| lizard | Tier 2 | reference leg (not yet in-game) | 8 | 71 | 71 | 0 | 11 | 8 | 1 | STATE | 1 | 1 | 0 | 0 | EQUAL, order kept | 29.2 h | 3 |
| ostrich | Tier 2 | reference leg (not yet in-game) | 8 | 38 | 38 | 0 | 13 | 10 | 0 | NONE | 0 | 1 | 0 | 0 | EQUAL, order kept | 22.6 h | 2 |
| rubber_ducky | Tier 2 | shipped | 9 | 8 | 8 | 0 | 10 | 8 | 2 | STATE | 1 | 1 | 0 | 0 | EQUAL, order kept | 15.6 h | 2 |
| spyro | Tier 2 | reference leg (not yet in-game) | 8 | 37 | 37 | 0 | 13 | 12 | 2 | NONE | 2 | 1 | 0 | 0 | EQUAL, order kept | 23.4 h | 2 |
| stinky | Tier 2 | reference leg (not yet in-game) | 8 | 20 | 20 | 0 | 13 | 12 | 3 | NONE | 1 | 1 | 0 | 0 | EQUAL, order kept | 20 h | 2 |
| the_prince | Tier 1 (boss) | reference leg (not yet in-game) | 8 | 35 | 35 | 0 | 15 | 10 | 3 | STATE | 4 | 1 | 10 (0 keyed by the shipped clips; warn) | 0 | EQUAL, order kept | 35.8 h | 3 |
| the_prince_adult | Tier 1 (boss) | reference leg (not yet in-game) | 8 | 119 | 119 | 0 | 23 | 10 | 6 | STATE | 8 | 1 | 17 (0 keyed by the shipped clips; warn) | 0 | EQUAL, order kept | 60.3 h | 3 |
| the_princess | Tier 1 (boss) | reference leg (not yet in-game) | 8 | 37 | 37 | 0 | 19 | 10 | 4 | STATE | 4 | 1 | 10 (0 keyed by the shipped clips; warn) | 0 | EQUAL, order kept | 42 h | 3 |
| the_prince_teen | Tier 1 (boss) | reference leg (not yet in-game) | 8 | 71 | 71 | 0 | 15 | 10 | 6 | STATE | 8 | 1 | 16 (0 keyed by the shipped clips; warn) | 0 | EQUAL, order kept | 41.1 h | 3 |
| velocity_raptor | Tier 2 | reference leg (not yet in-game) | 8 | 34 | 34 | 0 | 16 | 12 | 0 | NONE | 0 | 1 | 0 | 0 | EQUAL, order kept | 24.8 h | 2 |
| water_dragon | Tier 1 (boss) | reference leg (not yet in-game) | 8 | 24 | 24 | 0 | 16 | 10 | 1 | UNCLASSIFIED | 2 | 1 | 11 (0 keyed by the shipped clips; warn) | 0 | EQUAL, order kept | 34.1 h | 4 |
| butterfly | Tier 1 (boss) | reference leg (not yet in-game) | 11 | 10 | 10 | 0 | 7 | 1 | 1 | NONE | 1 | 4 | 0 | 0 | EQUAL, order kept | 18.5 h | 1 |
| luna_moth | Tier 1 (boss) | reference leg (not yet in-game) | 11 | 10 | 10 | 0 | 6 | 1 | 0 | NONE | 0 | 4 | 0 | 0 | EQUAL, order kept | 17 h | 1 |
| mosquito | Tier 2 | shipped | 9 | 5 | 5 | 2 | 6 | 1 | 0 | NONE | 0 | 1 | 0 | 0 | EQUAL, order kept | 9 h | 1 |
| fairy | Tier 2 | shipped | 17 | 15 | 15 | 0 | 13 | 2 | 1 | NONE | 1 | 9 | 0 | 0 | EQUAL, order kept | 19 h | 0 |
| firefly | Tier 2 | shipped | 9 | 12 | 12 | 2 | 6 | 1 | 0 | NONE | 0 | 1 | 0 | 0 | EQUAL, order kept | 10.4 h | 0 |
| ghost | Tier 2 | reference leg (not yet in-game) | 8 | 3 | 3 | 0 | 13 | 2 | 0 | NONE | 1 | 1 | 0 | 0 | EQUAL, order kept | 16.6 h | 0 |
| ghost_skelly | Tier 2 | reference leg (not yet in-game) | 8 | 10 | 10 | 0 | 14 | 2 | 0 | NONE | 1 | 1 | 0 | 0 | EQUAL, order kept | 19 h | 0 |
| mothra | Tier 1 (boss) | reference leg (not yet in-game) | 8 | 10 | 10 | 0 | 7 | 0 | 0 | NONE | 2 | 1 | 4 (0 keyed by the shipped clips; warn) | 0 | EQUAL, order kept | 18.5 h | 0 |
| vampire_butterfly | Tier 1 (boss) | reference leg (not yet in-game) | 8 | 10 | 10 | 0 | 7 | 3 | 1 | NONE | 1 | 1 | 0 | 0 | EQUAL, order kept | 18.5 h | 0 |
| elevator | Tier 3 | shipped | 18 | 5 | 5 | 0 | 0 | 0 | 5 | NONE | 0 | 10 | 0 | 5 | EQUAL, order kept | 0 h | 5 |
| purple_power | Tier 3 | shipped | 13 | 27 | 18 | 0 | 0 | 0 | 1 | NONE | 1 | 5 | 0 | 0 | EQUAL, order kept | 0 h | 0 |
| rock_base | Tier 3 | shipped | 18 | 22 | 22 | 0 | 0 | 0 | 1 | NONE | 0 | 10 | 0 | 0 | EQUAL, order kept | 0 h | 0 |
| cephadrome | Tier 1 (boss) | reference leg (not yet in-game) | 8 | 50 | 50 | 0 | 16 | 5 | 2 | STATE | 1 | 1 | 15 (0 keyed by the shipped clips; warn) | 0 | EQUAL, order kept | 39.5 h | 0 |
| ruby_bird | Tier 2 | shipped | 14 | 16 | 16 | 6 | 14 | 0 | 0 | NONE | 0 | 6 | 0 | 0 | EQUAL, order kept | 11.2 h | 0 |

TEXTURE_MAP: 428 shipped, 338 unique payloads, 86 duplicate groups, 90 redundant names, 262 referenced by Java, 42 strays; law series {'boyfriend': True, 'elevator': True, 'girlfriend': True, 'swimshorts': True}.

## Warnings

- [global] ARTIST_TIER_UNPACKAGED: 4 artist-tier species have no rig to package (no shipped geo, no reference-leg geo) — giant_robot: the converter refused reference_giantrobot (ValueError: reference_giantrobot capture bind draws [...], which are not cube-bearing geo bones (a part drawn more than once without render_instances, or a part the rig lacks)); jeffery: the converter refused reference_giantrobot (ValueError: reference_giantrobot capture bind draws [...], which are not cube-bearing geo bones (a part drawn more than once without render_instances, or a part the rig lacks)); boyfriend: no entry whose class is ModelBoyfriend in reference_model_proofs.json — no reference-leg geo can exist for it until the reference leg covers the model; girlfriend: no entry whose class is ModelGirlfriend in reference_model_proofs.json — no reference-leg geo can exist for it until the reference leg covers the model
- [alien] GOAL_UNCLASSIFIED: goal MoveThroughVillageGoal (Alien.java:92) has no dictionary entry
- [alien] GOAL_UNCLASSIFIED: goal AlienTorchSeekGoal (Alien.java:96) has no dictionary entry
- [alosaurus] GOAL_UNCLASSIFIED: goal MoveThroughVillageGoal (Alosaurus.java:84) has no dictionary entry
- [band_p] GOAL_UNCLASSIFIED: goal MoveThroughVillageGoal (BandP.java:78) has no dictionary entry
- [band_p] GOAL_UNCLASSIFIED: goal OpenDoorGoal (BandP.java:83) has no dictionary entry
- [basilisk] GOAL_UNCLASSIFIED: goal MoveThroughVillageGoal (Basilisk.java:81) has no dictionary entry
- [cave_fisher] WISHLIST_UNACCEPTED: wishlist names clip(s) ['aggro_idle'] that the manifest does not accept
- [creeping_horror] GOAL_UNCLASSIFIED: goal MoveThroughVillageGoal (CreepingHorror.java:78) has no dictionary entry
- [ender_knight] WISHLIST_UNACCEPTED: wishlist names clip(s) ['attack'] that the manifest does not accept
- [ender_knight] WISHLIST_UNACCEPTED: wishlist names clip(s) ['aggro_idle'] that the manifest does not accept
- [ender_reaper] WISHLIST_UNACCEPTED: wishlist names clip(s) ['aggro_idle'] that the manifest does not accept
- [ender_reaper] WISHLIST_UNACCEPTED: wishlist names clip(s) ['attack'] that the manifest does not accept
- [irukandji] GOAL_UNCLASSIFIED: goal MyEntityAIWander (Irukandji.java:82) has no dictionary entry
- [nastysaurus] WISHLIST_UNACCEPTED: wishlist names clip(s) ['aggro_idle'] that the manifest does not accept
- [pointysaurus] GOAL_UNCLASSIFIED: goal PointysaurusStareGoal (Pointysaurus.java:84) has no dictionary entry
- [pointysaurus] WISHLIST_UNACCEPTED: wishlist names clip(s) ['aggro_idle'] that the manifest does not accept
- [robot_1] BONE_UNLABELLED: bone Shape1 has no glossary label
- [robot_1] BONE_UNLABELLED: bone Shape10 has no glossary label
- [robot_1] BONE_UNLABELLED: bone Shape11 has no glossary label
- [robot_1] BONE_UNLABELLED: bone Shape12 has no glossary label
- [robot_1] BONE_UNLABELLED: bone Shape13 has no glossary label
- [robot_1] BONE_UNLABELLED: bone Shape14 has no glossary label
- [robot_1] BONE_UNLABELLED: bone Shape15 has no glossary label
- [robot_1] BONE_UNLABELLED: bone Shape15a has no glossary label
- [robot_1] BONE_UNLABELLED: bone Shape16 has no glossary label
- [robot_1] BONE_UNLABELLED: bone Shape17 has no glossary label
- [robot_1] BONE_UNLABELLED: bone Shape18 has no glossary label
- [robot_1] BONE_UNLABELLED: bone Shape2 has no glossary label
- [robot_1] BONE_UNLABELLED: bone Shape2a has no glossary label
- [robot_1] BONE_UNLABELLED: bone Shape3 has no glossary label
- [robot_1] BONE_UNLABELLED: bone Shape4 has no glossary label
- [robot_1] BONE_UNLABELLED: bone Shape5 has no glossary label
- [robot_1] BONE_UNLABELLED: bone Shape6 has no glossary label
- [robot_1] BONE_UNLABELLED: bone Shape7 has no glossary label
- [robot_1] BONE_UNLABELLED: bone Shape8 has no glossary label
- [robot_1] BONE_UNLABELLED: bone Shape9 has no glossary label
- [robot_2] BONE_UNLABELLED: bone Shape3 has no glossary label
- [robot_2] BONE_UNLABELLED: bone Shape6 has no glossary label
- [robot_2] BONE_UNLABELLED: bone Shape7 has no glossary label
- [robot_2] BONE_UNLABELLED: bone Shape8 has no glossary label
- [robot_3] ATTACKING_UNCLASSIFIED: DATA_ATTACKING classified MIXED: a 10-tick pulse per in-range think tick: the flag is raised at line(s) [155] every 35 ticks (reloadTicker reset) while a target is in range — before the line-of-sight gate, so it pulses whether or not a shot fires — and cleared when reloadTicker < 25 at line(s) [144] AND cleared on target loss at line(s) [162]
- [robot_5] ATTACKING_UNCLASSIFIED: DATA_ATTACKING classified MIXED: a 5-tick pulse per in-range think tick: the flag is raised at line(s) [143] every 20 ticks (reloadTicker reset) while a target is in range — before the line-of-sight gate, so it pulses whether or not a shot fires — and cleared when reloadTicker < 15 at line(s) [131] AND cleared on target loss at line(s) [152]
- [sea_viper] GOAL_UNCLASSIFIED: goal SeaViperBiteGoal (SeaViper.java:119) has no dictionary entry
- [sea_viper] GOAL_UNCLASSIFIED: goal RandomSwimmingGoal (SeaViper.java:120) has no dictionary entry
- [sea_viper] WISHLIST_UNACCEPTED: wishlist names clip(s) ['aggro_idle'] that the manifest does not accept
- [skate] GOAL_UNCLASSIFIED: goal MyEntityAIWander (Skate.java:76) has no dictionary entry
- [kraken] ATTACKING_UNCLASSIFIED: DATA_ATTACKING classified STATE?: cleared unconditionally at line(s) [450] (the enclosing branch decides; read the source)
- [the_king] GOAL_UNCLASSIFIED: goal KingEndGameGoal (TheKing.java:215) has no dictionary entry
- [the_king] GOAL_UNCLASSIFIED: goal KingPrimaryGoal (TheKing.java:216) has no dictionary entry
- [the_queen] LOCKED_BONES_KEYED: the shipped clips key 26 of the 27 SPEC-locked bones (LHead, LHead12, LHead4, Lwing1, NeckL1, NeckL13...): allowed and warned — Keying a locked bone is allowed; the checker warns, and the hitbox part follows the bone in-game (the consequence, so keep such keys deliberate). Renaming, re-parenting or deleting a locked bone is refused.
- [emperor_scorpion] GOAL_UNCLASSIFIED: goal EmperorScorpionPoisonGoal (EntityEmperorScorpion.java:96) has no dictionary entry
- [scorpion] WISHLIST_UNACCEPTED: wishlist names clip(s) ['aggro_idle'] that the manifest does not accept
- [spit_bug] GOAL_UNCLASSIFIED: goal SpitBugAcidAttackGoal (EntitySpitBug.java:86) has no dictionary entry
- [trooper_bug] GOAL_UNCLASSIFIED: goal TrooperBugLeapAttackGoal (EntityTrooperBug.java:88) has no dictionary entry
- [trooper_bug] WISHLIST_UNACCEPTED: wishlist names clip(s) ['aggro_idle'] that the manifest does not accept
- [baryonyx] GOAL_UNCLASSIFIED: goal MyEntityAIWander (Baryonyx.java:62) has no dictionary entry
- [cassowary] GOAL_UNCLASSIFIED: goal MyEntityAIWander (Cassowary.java:51) has no dictionary entry
- [chipmunk] GOAL_UNCLASSIFIED: goal FollowOwnerGoal (Chipmunk.java:67) has no dictionary entry
- [chipmunk] GOAL_UNCLASSIFIED: goal TemptGoal (Chipmunk.java:71) has no dictionary entry
- [flounder] GOAL_UNCLASSIFIED: goal MyEntityAIWander (Flounder.java:61) has no dictionary entry
- [frog] GOAL_UNCLASSIFIED: goal MyEntityAIWander (Frog.java:67) has no dictionary entry
- [gazelle] GOAL_UNCLASSIFIED: goal FollowOwnerGoal (Gazelle.java:72) has no dictionary entry
- [gazelle] GOAL_UNCLASSIFIED: goal TemptGoal (Gazelle.java:74) has no dictionary entry
- [gazelle] GOAL_UNCLASSIFIED: goal MyEntityAIWander (Gazelle.java:78) has no dictionary entry
- [peacock] WISHLIST_UNACCEPTED: wishlist names clip(s) ['attack'] that the manifest does not accept
- [whale] GOAL_UNCLASSIFIED: goal TemptGoal (Whale.java:62) has no dictionary entry
- [whale] GOAL_UNCLASSIFIED: goal MyEntityAIWander (Whale.java:65) has no dictionary entry
- [dragonfly] GOAL_UNCLASSIFIED: goal DragonflyHuntGoal (EntityDragonfly.java:65) has no dictionary entry
- [camarasaurus] GOAL_UNCLASSIFIED: goal FollowOwnerGoal (Camarasaurus.java:69) has no dictionary entry
- [camarasaurus] GOAL_UNCLASSIFIED: goal TemptGoal (Camarasaurus.java:71) has no dictionary entry
- [camarasaurus] GOAL_UNCLASSIFIED: goal MyEntityAIWander (Camarasaurus.java:74) has no dictionary entry
- [dragon] GOAL_UNCLASSIFIED: goal SitWhenOrderedToGoal (Dragon.java:141) has no dictionary entry
- [dragon] GOAL_UNCLASSIFIED: goal FollowOwnerGoal (Dragon.java:142) has no dictionary entry
- [dragon] GOAL_UNCLASSIFIED: goal TemptGoal (Dragon.java:144) has no dictionary entry
- [gamma_metroid] GOAL_UNCLASSIFIED: goal FollowOwnerGoal (EntityGammaMetroid.java:78) has no dictionary entry
- [gamma_metroid] GOAL_UNCLASSIFIED: goal TemptGoal (EntityGammaMetroid.java:79) has no dictionary entry
- [hydrolisc] GOAL_UNCLASSIFIED: goal FollowOwnerGoal (EntityHydrolisc.java:70) has no dictionary entry
- [hydrolisc] GOAL_UNCLASSIFIED: goal TemptGoal (EntityHydrolisc.java:71) has no dictionary entry
- [leon] GOAL_UNCLASSIFIED: goal FollowOwnerGoal (EntityLeon.java:154) has no dictionary entry
- [leon] GOAL_UNCLASSIFIED: goal TemptGoal (EntityLeon.java:155) has no dictionary entry
- [leonopteryx] GOAL_UNCLASSIFIED: goal FollowOwnerGoal (EntityLeon.java:154) has no dictionary entry
- [leonopteryx] GOAL_UNCLASSIFIED: goal TemptGoal (EntityLeon.java:155) has no dictionary entry
- [lizard] GOAL_UNCLASSIFIED: goal FollowOwnerGoal (Lizard.java:80) has no dictionary entry
- [lizard] GOAL_UNCLASSIFIED: goal TemptGoal (Lizard.java:82) has no dictionary entry
- [lizard] WISHLIST_UNACCEPTED: wishlist names clip(s) ['aggro_idle'] that the manifest does not accept
- [ostrich] GOAL_UNCLASSIFIED: goal FollowOwnerGoal (Ostrich.java:129) has no dictionary entry
- [ostrich] GOAL_UNCLASSIFIED: goal TemptGoal (Ostrich.java:131) has no dictionary entry
- [rubber_ducky] GOAL_UNCLASSIFIED: goal FollowOwnerGoal (EntityRubberDucky.java:86) has no dictionary entry
- [rubber_ducky] GOAL_UNCLASSIFIED: goal TemptGoal (EntityRubberDucky.java:87) has no dictionary entry
- [spyro] GOAL_UNCLASSIFIED: goal MyEntityAIFollowOwner (EntitySpyro.java:95) has no dictionary entry
- [spyro] GOAL_UNCLASSIFIED: goal TemptGoal (EntitySpyro.java:96) has no dictionary entry
- [stinky] GOAL_UNCLASSIFIED: goal MyEntityAIFollowOwner (EntityStinky.java:102) has no dictionary entry
- [stinky] GOAL_UNCLASSIFIED: goal TemptGoal (EntityStinky.java:103) has no dictionary entry
- [the_prince] GOAL_UNCLASSIFIED: goal FollowOwnerGoal (ThePrince.java:108) has no dictionary entry
- [the_prince] GOAL_UNCLASSIFIED: goal TemptGoal (ThePrince.java:109) has no dictionary entry
- [the_prince] GOAL_UNCLASSIFIED: goal MyEntityAIWander (ThePrince.java:111) has no dictionary entry
- [the_prince_adult] GOAL_UNCLASSIFIED: goal FollowOwnerGoal (ThePrinceAdult.java:138) has no dictionary entry
- [the_prince_adult] GOAL_UNCLASSIFIED: goal TemptGoal (ThePrinceAdult.java:139) has no dictionary entry
- [the_prince_adult] GOAL_UNCLASSIFIED: goal MyEntityAIWander (ThePrinceAdult.java:140) has no dictionary entry
- [the_princess] GOAL_UNCLASSIFIED: goal FollowOwnerGoal (ThePrincess.java:106) has no dictionary entry
- [the_princess] GOAL_UNCLASSIFIED: goal TemptGoal (ThePrincess.java:107) has no dictionary entry
- [the_princess] GOAL_UNCLASSIFIED: goal MyEntityAIWander (ThePrincess.java:109) has no dictionary entry
- [the_prince_teen] GOAL_UNCLASSIFIED: goal FollowOwnerGoal (ThePrinceTeen.java:149) has no dictionary entry
- [the_prince_teen] GOAL_UNCLASSIFIED: goal TemptGoal (ThePrinceTeen.java:150) has no dictionary entry
- [the_prince_teen] GOAL_UNCLASSIFIED: goal MyEntityAIWander (ThePrinceTeen.java:151) has no dictionary entry
- [velocity_raptor] GOAL_UNCLASSIFIED: goal FollowOwnerGoal (VelocityRaptor.java:64) has no dictionary entry
- [velocity_raptor] GOAL_UNCLASSIFIED: goal TemptGoal (VelocityRaptor.java:66) has no dictionary entry
- [water_dragon] GOAL_UNCLASSIFIED: goal TemptGoal (WaterDragon.java:133) has no dictionary entry
- [water_dragon] GOAL_UNCLASSIFIED: goal WaterCanonAttackGoal (WaterDragon.java:134) has no dictionary entry
- [water_dragon] GOAL_UNCLASSIFIED: goal RandomSwimmingGoal (WaterDragon.java:135) has no dictionary entry
- [water_dragon] ATTACKING_UNCLASSIFIED: DATA_ATTACKING classified UNCLASSIFIED: the clear sites' guards match neither the pulse nor the target-loss pattern; other clears at line(s) [622] (guards: NOT(if (this.streamCount > 0))) — the owner reads them
- [butterfly] GOAL_UNCLASSIFIED: goal ButterflyIslandsHuntGoal (EntityButterfly.java:74) has no dictionary entry
- [luna_moth] GOAL_UNCLASSIFIED: goal LunaMothFlightGoal (EntityLunaMoth.java:55) has no dictionary entry
- [mosquito] GOAL_UNCLASSIFIED: goal MosquitoFlightGoal (EntityMosquito.java:49) has no dictionary entry
- [elevator] BONE_UNLABELLED: bone shape1 has no glossary label
- [elevator] BONE_UNLABELLED: bone shape2 has no glossary label
- [elevator] BONE_UNLABELLED: bone shape3 has no glossary label
- [elevator] BONE_UNLABELLED: bone shape4 has no glossary label
- [elevator] BONE_UNLABELLED: bone shape5 has no glossary label
