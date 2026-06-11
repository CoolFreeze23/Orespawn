# Phase C slice 2 — entities D–I (ENT-D-*) — 2026-06-11

Scope: all 34 `ENT-D-*` findings with Status DIVERGENT and no prior Resolution.
Counts: **18 FIXED**, **1 VERIFIED-CORRECT**, **15 PARTIAL** (spawn-rule / date gates → ENT-SYS-002, Phase D), 0 deferred.

Biome-mapping conventions follow `C1_entities_A_C.md`, extended here:
taigaHills → `taiga`, extremeHills → `windswept_hills`, extremeHillsEdge → `windswept_gravelly_hills`,
coldTaiga → `snowy_taiga` (coldTaigaHills merges into the same id), frozenRiver → `frozen_river`.
Loot/biome JSONs cannot carry comments — orig citations live in this report.

## Code fixes

| ID | Orig citation + value | Old port value | New port value | Notes |
|---|---|---|---|---|
| ENT-D-002 | `orig Dragon.java:1212-1219,1245-1252` — raw beef (`field_151082_bd`) tempts/tames (1-in-5) and heals to full | `Dragon.java` TemptGoal/isFood/mobInteract used `Items.BONE` | all four sites use `Items.BEEF` | tame chance/heal already matched |
| ENT-D-006 | `orig Dragon.java:1351-1369` — a **DIAMOND** spawns a `Spyro` ("Baby Dragon" is the Spyro registry name); adult discarded; tame transferred | apple spawned `BabyDragon` with type/fire copy | diamond spawns `ModEntities.ENTITY_SPYRO`, no type/fire transfer, adult discarded | **Audit corrected:** trigger is diamond, not a "Magic Apple" |
| ENT-D-012 | `orig Elevator.java:300-302` — `"orespawn:hover"` 0.45 vol / 1.0 pitch; orig sounds.json combines hover1–6 | `HoverboardEntity` played `BEACON_AMBIENT` | plays `ModSounds.HOVER` (new combined event over hover1–6 in `sounds.json`/`ModSounds.java`) | cooldown 55t unchanged |
| ENT-D-014 | `orig EmperorScorpion.java:408,437-438` — inside `nextInt(4)==0` AI gate, `nextInt(20)==1` while a target exists spawns ONE Scorpion at the self/target midpoint (±nextInt(5) jitter, y mid +1.01); no cap, no cooldown | 30+rand(10)-tick timer, <3-within-16 condition, cap 6 | direct port of the orig dice/midpoint spawn; all caps/cooldowns removed | **Audit corrected:** no "population low" condition exists in the orig |
| ENT-D-022 | `orig EntityCage.java:159-938` — 20% base failure (entity hit → cage lost, NOT empty-cage); players → CageEmpty; ~100-species whitelist w/ per-species escape dice (Ghast/Enderman/Wither/Whale/Lizard/EnderReaper/Pointysaurus 20%, Kyuubi/Bee/SpitBug/Vortex/Mantis/SeaMonster/EnderKnight 30%, Mothra/Alosaurus/TRex/SeaViper 40%, EnderDragon/WormLarge/Alien/Molenoid/HerculesBeetle/Brutalfly 50%, Basilisk/WaterDragon/Triffid/TrooperBug 60%, EmperorScorpion/Cephadrome/Dragon/PitchBlack/CaterKiller/Leon/Hammerhead/Nastysaurus 70%, Kraken 95%); multi-drops Bat×2 (:197), Silverfish×2 (:270), Dragonfly×2 (:448), Cockateil×4 (:505), AttackSquid×6 (:509); tamed GF/BF uncapturable (:292,:296); EnderDragonPart captures parent (:312-324); unlisted species eat the cage with no drop (:935-937) | universal NBT capture of ANY mob, 80% success, always +empty cage on impact | full whitelist chain (`captureSpecFor`) with escape dice as `nextInt(100)` percents, multi-count NBT-cloned `CagedMobItem`s, player/tamed-GF/BF/unlisted branches, block hits → CageEmpty | port cow splits (AppleCow etc.) fall into the orig `EntityCow` branch; double `onHit` empty-cage bug also removed |
| ENT-D-025 | `orig EntityThrownRock.java:123-124` — t5 = 10 dmg, ks 0.1 (t7 :149-151 ks 0.2; only t8 :159-161 gets 0.5) | t5 folded into 5-dmg band; t7 had 0.5 ks | `case 5 -> 10 dmg / 0.1 ks`; t7 40 dmg without t8's knockback | |
| ENT-D-026 | `orig :145,178,180,196,212,225` — t6/9/10/11/12 = WEAKNESS 100t; t9 also setFire(50); t10 poison 200; t11 slow 200 | WITHER on t6/9/11; t9 ignite 2.5 s; t10 poison only | WEAKNESS 100t on t6/9/10/11/12; t9 `igniteForSeconds(50)`; t11 slow 200 kept | |
| ENT-D-027 | `orig :229-285` — block-impact branch breaks glass/glass-pane in 3×3×3, plays `"orespawn:glassdead"` (combined glassdead1/2), returns the type-specific rock (12 types); entity hits drop nothing | always popped generic `ROCK`, no glass-break | full glass-break + typed recovery (`ROCK_SMALL`…`ROCK_CRYSTAL_TNT`); `GLASSDEAD` combined event registered | orig has no mobGriefing gate — none added |
| ENT-D-037 | `orig Gazelle.java:341-352` — tamed: 2+nextInt(5)=2–6 poppies; untamed: vanilla super → beef (`func_146068_u` :337-339) 0–2 (+looting) | `gazelle.json` mutton 1–3 | NBT-branched JSON: `OreSpawnTamed` → poppy 2–6; else beef 0–2 (+looting); `Gazelle.addAdditionalSaveData` writes the flag | same convention as Camarasaurus |
| ENT-D-055 | `orig Hammerhead.java:37` — plain `EntityMob`, no BossStatus hooks | `ServerBossEvent` + add/remove/progress hooks | boss bar fully removed | |

## Loot JSON fixes (citations)

| ID | Orig citation + value | Old port value | New port value |
|---|---|---|---|
| ENT-D-008 | `orig DungeonBeast.java:145-157` — `func_146068_u` picks once via `nextInt(4)`: 1=MyCrystalPinkIngot, 2=MyCrystalApple, 3=oak log (`field_150364_r`), 0=null; vanilla `func_70628_a` count 0–2 (+looting) | `dungeon_beast.json` bones 3–8 + 50% gold 1–4 | single pool: equal-weight crystal_pink_ingot / crystal_apple / oak_log / empty, each 0–2 (+looting) |
| ENT-D-029 | `orig Fairy.java:168-170` — CrystalTorch via `func_146068_u`; ambient default count 0–2 (+looting) | `fairy.json` glowstone_dust 1–3 | `orespawn:crystal_torch` 0–2 (+looting) |
| ENT-D-031 | `orig Firefly.java:91-93` — ExtremeTorch; default count 0–2 (+looting) | `firefly.json` glowstone_dust 0–1 | `orespawn:extreme_torch` 0–2 (+looting) |
| ENT-D-035 | `orig GammaMetroid.java:223-233` — 5+nextInt(10)=5–14 gold nuggets + 6+nextInt(10)=6–15 iron ingots | gunpowder 5–14 + iron 6–15 | gold_nugget 5–14 + iron_ingot 6–15 |
| ENT-D-038/040 | orig Ghost/GhostSkelly extend `EntityAmbientCreature`, no drop override → nothing | bones (+arrows for Skelly) | empty pools |
| ENT-D-045 | `orig GiantRobot.java:158-211` — 15+nextInt(15)=15–29 drops of MyLaserBall ×4; then 10+nextInt(10)=10–19 rolls of `nextInt(12)`: 0 SpiderRobotKit, 1 AntRobotKit, 2 MyRayGun, 3 redstone_block, 4 dispenser, 5 sticky_piston, 6 piston, 7 lever, 8 iron_block, 9 **detector_rail** (`field_150319_E`), 10/11 nothing | iron 5–10 + 30% iron blocks | pool 1: laser_ball ×4, rolls 15–29; pool 2: rolls 10–19 over 10 weight-1 items + weight-2 empty |

## Spawn biome-modifier fixes (PARTIAL where orig `getCanSpawnHere` gates exist — owner ENT-SYS-002, Phase D)

| ID | Orig citation + value | Old port value | New port value | Notes |
|---|---|---|---|---|
| ENT-D-004 | `orig BiomeGenUtopianPlains.java:163-165` (`setIslandCreatures`, ambient) — Dragon w1 1–2; NO overworld addSpawn | `add_overworld_creatures.json` w1 1–1 overworld-wide | overworld entry removed; `dim_islands_locals.json` w1 1–2 | **Audit corrected:** the list is the Island sub-biome, not a "boss list" |
| ENT-D-009 | `orig OreSpawnMain.java:4981` roofedForest w20 2–4 ambient + `BiomeGenUtopianPlains.java:231-233` Crystal w30 4–6 + `:450-452` Chaos w2 1–5 | `#minecraft:is_badlands` w20 2–4 | `hostile_dungeon_beast.json` → `minecraft:dark_forest`; Crystal + Chaos dim entries added | |
| ENT-D-011 | `orig OreSpawnMain.java:4681-4689` — plains/forest/forestHills w10, birch/birchHills/megaTaiga w5, taiga w8, all 1–2; whole block gated on **April 20** (`:4518-4520,4570-4571`: GregorianCalendar month==3 day==20) | overworld-wide w3 1–2 | 3 `companion_easter_bunny__*.json` files (plains/forest/windswept_forest w10; birch_forest/old_growth_birch/old_growth_pine w5; taiga w8) | date gate → ENT-SYS-002; **note:** "Easter" is hardcoded April 20 |
| ENT-D-018 | `orig OreSpawnMain.java:4920-4928` ambient — extremeHills/Edge/forest/forestHills/jungleHills w4 2–4, plains/river/desert w2 2–4, roofedForest w20 2–4; + `BiomeGenUtopianPlains.java:456-458` Chaos w2 1–2; NO End spawn | `add_end_spawns.json` w8 1–2 only | 3 `hostile_ender_knight__*.json` files + Chaos entry; End entry removed | invented End habitat deleted |
| ENT-D-021 | `orig OreSpawnMain.java:4931-4939` ambient — extremeHills/Edge/forestHills/jungleHills w2 1–2, forest/plains/river/desert w1 1–2, roofedForest **w38 2–4**; + Island `:193-195` w25 2–4 + Chaos `:459-461` w1 1–1; NO End spawn | `add_end_spawns.json` w4 1–1 only | 3 `hostile_ender_reaper__*.json` files + Island + Chaos entries; End entry removed (file deleted — it held only the two invented entries) | |
| ENT-D-030 | `orig OreSpawnMain.java:4974` — roofedForest w25 2–4 ambient ONLY (+ Crystal `:213-215` w10 4–8, Chaos `:370-372` w5 2–4, already correct in port) | `add_overworld_ambient.json` w5 1–3 ALL overworld | `swarm_fairy__dark_forest.json` w25 2–4; `add_overworld_ambient.json` deleted (held only fairy) | |
| ENT-D-032 | `orig BiomeGenUtopianPlains.java:125-127` Utopia water w2 2–4 + `:252-254` Crystal water w5 6–8; NO ocean spawn | `add_ocean_spawns.json` w8 1–3 | Utopia + Crystal dim entries; ocean entry removed | |
| ENT-D-033 | `orig OreSpawnMain.java:4963-4967` — river waterCreature w20 3–6 + ambient w3 3–6; swamp waterCreature w20 2–6 + ambient w2 2–6; jungle ambient w3 3–6; + Utopia `:134-136` w5 4–6 + Crystal `:261-263` w1 3–5 | overworld-wide w10 1–2 | 3 `companion_frog__*.json` files (orig creature-type split collapses into NeoForge's single category — both entries kept per biome) + dim entries | port Y≥50/day/≤5 rules stay (≈ orig gates) |
| ENT-D-036 | `orig ChunkProviderOreSpawn2.java:385-386` — **Mining** dim w35 4–7 + `BiomeGenUtopianPlains.java:513-514` (`setChaosCreatures`) w1 1–1 | Nether w3 1–1 + invented `companion_gamma_metroid.json` mountains w1 | `dim_mining_locals.json` w35 4–7 + Chaos entry; Nether entry + companion file deleted | **Audit corrected:** ChunkProviderOreSpawn2 is the Mining dim (matches slice-1 ENT-A-006), and "Utopia boss" is the Chaos list |
| ENT-D-039 | UNGATED `orig OreSpawnMain.java:4783-4788` — coldTaiga w15 5–10, taigaHills w10 5–10, frozenRiver w6 4–6, jungle w2 1–4, roofedForest w15 2–5; GATED `:4544-4565` 22 biomes w15 3–6 **Halloween-only** (`:4518-4521` month==9 day==31); NO Chaos-dim spawn (`setChaosCreatures` has no Ghost) | `add_cave_spawns.json` w4 1–1 + `dim_chaos_locals.json` w15 3–6 | 5 `swarm_ghosts__*.json` files (shared with GhostSkelly); cave + Chaos entries removed | Halloween block + dark-rule gates → ENT-SYS-002 |
| ENT-D-041 | same structure as ENT-D-039 (`:4790-4795` ungated; `:4522-4543` Halloween) | cave w4 1–1 + Chaos w10 2–4 | same 5 shared files; invented entries removed | |
| ENT-D-050 | `orig OreSpawnMain.java:4574-4585` — beach w30 8–15; forest/river/stoneBeach w10 3–6; forestHills w8 2–5; plains w5 2–3; birchForest w5 2–4; birchHills/megaTaiga/taiga w5 2–5; savanna/savannaPlateau w2 1–3 | `companion_girlfriend.json` overworld-wide w4 1–2 | 7 per-biome files mirroring the slice-1 boyfriend layout | Utopia/Village dim entries (`BiomeGenUtopianPlains.java:96-98,300-302`) intentionally not added, matching the slice-1 ENT-A-057 boyfriend treatment |
| ENT-D-053 | `orig BiomeGenUtopianPlains.java:119-121` Utopia w1 1–1 + `:175-177` Island w5 2–4 + `:367-369` Chaos w10 2–4 | ocean w10 1–3 + Chaos w10 2–4 | Utopia + Island entries added; ocean entry removed; Chaos kept | |
| ENT-D-057 | `orig BiomeGenUtopianPlains.java:462-464` (`setChaosCreatures`) — w1 1–1; NO ocean spawn | `add_ocean_spawns.json` w3 1–1 | Chaos entry added; ocean entry removed | **Audit corrected:** the list is the Chaos sub-biome |
| ENT-D-061 | `orig OreSpawnMain.java:4829-4832` creature — swamp w25 3–6, jungle w15 2–5, jungleHills w10 1–3, stoneBeach w5 3–6 | ocean w3 + beach w3 + river w3 files | 4 `companion_hydrolisc__{swamp,jungle,sparse_jungle,stony_shore}.json`; old beach/river files + ocean entry deleted | FIXED — orig Hydrolisc has no `getCanSpawnHere` override |
| ENT-D-063 | `orig BiomeGenUtopianPlains.java:255-257` Crystal water w4 2–3; NO ocean spawn | `add_ocean_spawns.json` w4 1–2 | Crystal entry added; ocean entry removed | **Audit corrected:** "Utopia waters" is the Crystal sub-biome water list |

## VERIFIED-CORRECT (proof)

- **ENT-D-003** — orig `Dragon.java:342-347` drops 1+nextInt(6) = 1–6 raw beef. Port `dragon.json`
  already drops `minecraft:beef` 1–6 and `Dragon.java` contains no hardcoded bone/diamond drop
  (the former `dropCustomDeathLoot` bone override was removed in an earlier phase). No change needed.

## PARTIAL / deferred (owner: ENT-SYS-002, Phase D unless noted)

| ID | Remainder |
|---|---|
| ENT-D-004 | Dragon `getCanSpawnHere` gates |
| ENT-D-009 | DungeonBeast spawn-rule gates |
| ENT-D-011 | April-20 (easter_day) date gate |
| ENT-D-018 / ENT-D-021 | EnderKnight/EnderReaper spawn-rule gates |
| ENT-D-030 | Fairy spawn-rule gates |
| ENT-D-032 | Flounder spawn-rule gates |
| ENT-D-033 | Frog gates (port already approximates: Y≥50/day/≤5) |
| ENT-D-036 | GammaMetroid spawn-rule gates |
| ENT-D-039 / ENT-D-041 | Halloween (Oct 31) 22-biome w15 3–6 block + dark-spawn rules |
| ENT-D-050 | Girlfriend spawn-rule gates |
| ENT-D-053 | GoldFish spawn-rule gates |
| ENT-D-057 | Hammerhead gates (port Y≥50 + no-buddy rules kept) |
| ENT-D-063 | Irukandji gates (port 1/60 roll + ≤2-nearby rules kept) |

## Files changed

**Java:** `entity/Dragon.java`, `entity/EntityCage.java`, `entity/EntityEmperorScorpion.java`,
`entity/EntityThrownRock.java`, `entity/Gazelle.java`, `entity/Hammerhead.java`,
`entity/HoverboardEntity.java`, `ModSounds.java`

**Assets:** `assets/orespawn/sounds.json` (combined `hover` + `glassdead` events)

**Loot JSONs:** `dungeon_beast.json`, `dragon.json` (verified only), `fairy.json`, `firefly.json`,
`gamma_metroid.json`, `gazelle.json`, `ghost.json`, `ghost_skelly.json`, `giant_robot.json`

**Biome modifiers:** rewritten `add_ocean_spawns.json`, `add_cave_spawns.json`, `add_nether_spawns.json`,
`add_overworld_creatures.json`, `hostile_dungeon_beast.json`, `companion_girlfriend.json`,
`dim_utopia_locals.json`, `dim_crystal_locals.json`, `dim_chaos_locals.json`, `dim_islands_locals.json`,
`dim_mining_locals.json`; new 3× `companion_easter_bunny__*`, 3× `companion_frog__*`,
6× `companion_girlfriend__*`, 4× `companion_hydrolisc__*`, 3× `hostile_ender_knight__*`,
3× `hostile_ender_reaper__*`, `swarm_fairy__dark_forest.json`, 5× `swarm_ghosts__*`; deleted
`add_end_spawns.json`, `add_overworld_ambient.json`, `companion_gamma_metroid.json`,
2× old `companion_hydrolisc__minecraft_is_*.json`

## Build status

`.\gradlew.bat build --console=plain` — **BUILD SUCCESSFUL** (only pre-existing deprecation warnings).
