# Phase C slice 4 — entities S–Z (ENT-S-* + ENT-SYS-003) — 2026-06-11

Scope: all 27 `ENT-S-*` findings with Status DIVERGENT and no prior Resolution, plus
ENT-SYS-003 (the one unresolved DIVERGENT `ENT-SYS-*` block), plus 3 carried-forward PARTIAL
remainders (ENT-S-006 SeaMonster water speed, ENT-S-061 Urchin fire immunity, ENT-S-068 Vortex
fire immunity). Total: 31. Counts: **22 FIXED · 9 PARTIAL** (spawn gates → ENT-SYS-002, Phase D),
0 VERIFIED-CORRECT, 0 deferred.

Biome-mapping conventions follow `C1_entities_A_C.md` / `C2_entities_D_I.md` / `C3_entities_K_R.md`.
Loot/biome JSONs cannot carry comments — orig citations live in this report.

## Carried-forward remainders (all FIXED)

| ID | Orig citation + value | Old port value | New port value | Notes |
|---|---|---|---|---|
| ENT-S-006 | `orig SeaMonster.java:126` — onLivingUpdate sets moveSpeed 0.55 in water / 0.25 on land; `:93` — onUpdate writes it into MOVEMENT_SPEED every tick | `dynamicMoveSpeed` computed in `aiStep` but never applied | `SeaMonster.aiStep` now writes `dynamicMoveSpeed` into `Attributes.MOVEMENT_SPEED` each tick | stats half closed in Phase B |
| ENT-S-061 | `orig Urchin.java:54` — `field_70178_ae = true` | not fire-immune | `.fireImmune()` (ModEntities.java urchin entry) | stats half closed in Phase B |
| ENT-S-068 | `orig Vortex.java:52` — `field_70178_ae = true` | not fire-immune | `.fireImmune()` (ModEntities.java vortex entry) | stats half closed in Phase B |

## Code fixes

| ID | Orig citation + value | Old port value | New port value | Notes |
|---|---|---|---|---|
| ENT-S-010 | `orig SeaViper.java:337,347-356` — POISON (`field_76436_u`) `var2*20` ticks on a 1/2 roll; var2 = 6, set to 8 on EASY (NORMAL/HARD branches nested inside the EASY check — unreachable decompile quirk) | HUNGER, flat 8 s, 1/2 roll (`SeaViperBiteGoal.java:24`) | POISON, 8 s EASY / 6 s otherwise, 1/2 roll; quirk preserved | **Audit corrected:** duration is 6 s (8 s easy), not flat 8 s |
| ENT-S-030 | `orig StinkBug.java:95-103` — on lethal hurt, CONFUSION/nausea (`field_76431_k`) 300t to all living in x±8 / y −5..+10 / z±8 | HUNGER 300t in `inflate(8,5,8)` (y ±5) | `MobEffects.CONFUSION` 300t, exact orig AABB (−5..+10 vertical) | **Audit corrected:** the effect is nausea, not Poison |
| ENT-S-031 | `orig StinkBug.java:173-175` — breeding item = MyCrystalApple ONLY; the raw-fish `isWheat()` (`:169-171`) is never called (no tempt goal registered, constructor `:47-54`) | `isFood` = apple | `isFood` = `ModItems.CRYSTAL_APPLE` | **Audit corrected:** "fish + CrystalApple" conflated dead code; effective food = crystal apple only |
| ENT-S-033 | `orig Stinky.java:335-338` — 1-in-1750: `random.burp` + COAL dropped front; `:339-398` — 1-in-2000: `orespawn:fart` (vol 1.0, pitch 1.5) + 19-skin rear item table (blaze powder, rotten flesh, melon seeds, uranium nugget, wheat, reeds, torch, emerald, gold ingot, oak leaves, titanium nugget, apple seed, diamond, sand, cobble, bone, string, cherry seed, peach seed) | front bone, silent; rear 19-item invented table (diamond, chicken, iron, …) | front COAL + `SoundEvents.PLAYER_BURP`; rear table mapped 1:1 to the orig items (`ModItems.URANIUM_NUGGET/TITANIUM_NUGGET/APPLE_TREE_SEED/CHERRY_TREE_SEED/PEACH_TREE_SEED` for the mod items) + `orespawn:fart` pitch 1.5 | aggregate `fart` event (random of fart1–9, orig assets/orespawn/sounds.json) added to port sounds.json — also un-dangles StinkBug's death-sound reference |
| ENT-S-045 | `orig TRex.java:96-109` — `orespawn:trex_living` 1-in-4 / `orespawn:alo_hurt` / `orespawn:trex_death` | RAVAGER_ROAR/HURT/DEATH | `ModSounds.TREX_LIVING` (1-in-4) / `ALO_HURT` / `TREX_DEATH` — all already registered | |
| ENT-S-057 | `orig UltimateArrow.java:157,279` — impact damage = ceil(velocity × `UltimateBowDamage`); config default 10, clamp 2-20 (`orig OreSpawnMain.java:1519-1530`); `orig UltimateBow.java:30-33` — fixed self-enchants Power 5 / Flame 3 / Punch 2 / Infinity 1 | flat `setBaseDamage(12.0)`; bow Power level = config value | base damage = `OreSpawnConfig.ULTIMATE_BOW_DAMAGE` (already defined, default 10, clamp 2-20 — matches orig); bow Power restored to the orig fixed 5 | modern `AbstractArrow` applies the same ceil(speed × base) formula. Residual: vanilla 1.21 Power adds its own bonus on top (orig's custom hit code ignored Power); documented, not worked around |
| ENT-S-065 | `orig VelocityRaptor.java:223-294` (`func_70085_c`) — apple tame / dead-bush untame / name-tag rename; NO mount interaction (plain EntityCannonFodder tameable) | empty-hand ride branch + `getControllingPassenger`/`positionRider`/`tickRidden`/`getRiddenInput`/`getRiddenSpeed` (×1.6)/`isPushable` | all riding code removed; sit toggle and tame flow untouched | riding was a port invention; removal does not touch Phase B `RiderFlightController` work (other entities) |
| ENT-S-069 | `orig Vortex.java:195-197` — within range, 1-in-8 roll → plain `func_70652_k` melee; no launch attack exists | melee plus `skywardLaunch` (+4.0 up, 30t cooldown) with a comment claiming it is "the signature 1.7.10 attack" | `skywardLaunch`, its constants/cooldown, and the false comment removed; melee + pull retained | the false-parity comment also fell under ENT-SYS-003 |
| ENT-SYS-003 | orig CaterKiller heals 2.0 with pathing (`:502-530`), metamorphoses (`:438-448`); orig Cryolophosaurus hunts proactively (`:141-211`) | port comments claimed parity for invented values / claimed hunting "never existed" | CaterKiller + Cryolophosaurus comments corrected with the behavior fixes in C1 (ENT-A-074/075/112); the remaining false-parity comment found in this slice (Vortex `skywardLaunch`) removed with ENT-S-069 | comment audits have run incrementally in every slice since |

## Loot JSON fixes (citations)

| ID | Orig citation + value | Old port value | New port value |
|---|---|---|---|
| ENT-S-004 | `orig Scorpion.java:148-160` — `func_146068_u` d10: 0 gold nugget (`field_151074_bl`) / 1 UraniumNugget / 2 TitaniumNugget / else null; vanilla count core 0-2 (+looting) | bone 1-3 | one weighted pool: gold/uranium/titanium nugget w1 each + empty w7, each 0-2 (+looting) |
| ENT-S-014 | `orig Skate.java:122-124` — `func_146068_u` = STRING (`field_151007_F`); 0-2 (+looting) | prismarine_shard 1-3 | string 0-2 (+looting). **Audit corrected:** orig drop is string, not raw fish |
| ENT-S-023 | `orig SpiderRobot.java:1079-1126` — 14+nextInt(14)=14-27 rolls of the d15 redstone table: 0 redstone, 1 repeater, 2 comparator, 3+8 redstone_block, 4 dispenser, 5 sticky_piston, 6 piston, 7 lever, 9 light_weighted_pressure_plate, 10-14 nothing | iron 3-8 + string 2-5 | d15 table (redstone_block w2, empty w5), rolls 14-27 — same table as Robot2-5 (C3) |
| ENT-S-026 | `orig SpitBug.java:208-213` — `func_70628_a` = 1+nextInt(3)=1-3 × MyAmethyst; the d10 nugget `func_146068_u` (`:184-196`) is dead code (func_70628_a overrides, no super call) | slime_ball 1-3 | `orespawn:amethyst_gem` 1-3. **Audit corrected:** amethyst GEMS, not "amethyst nuggets" |
| ENT-S-028 | `orig Spyro.java:378-387` — `func_70628_a`: TAMED → 1+nextInt(4)=1-4 raw BEEF; untamed → nothing (`func_146068_u` beef at `:374-376` is dead code) | blaze_powder 1-3 always | `OreSpawnTamed`-gated beef 1-4, no untamed pool; flag written in `EntitySpyro.addAdditionalSaveData` (Gazelle/Ostrich convention). **Audit corrected:** orig drop is tamed-only beef, not apple |
| ENT-S-039 | `orig TerribleTerror.java:313-322` — `func_146068_u` d3: rotten flesh / emerald / feather; 0-2 (+looting) | bone 1-2 + leather 0-1 + feather 0-1 | equal-weight one-of rotten_flesh / emerald / feather, 0-2 (+looting) |
| ENT-S-066 | `orig VelocityRaptor.java:329-338` — `func_70628_a`: TAMED → 2+nextInt(5)=2-6 poppies (`field_150328_O`); untamed → nothing | bone 1-3 always | `OreSpawnTamed`-gated poppy 2-6, no untamed pool; flag added in `VelocityRaptor.addAdditionalSaveData`. **Audit corrected:** tamed-only + count 2-6 |
| ENT-S-070 | `orig Vortex.java:369-399` — VortexEye ×1 + PAINTING (`field_151160_bD`) ×1 + 5+nextInt(7)=5-11 rolls of d10: 0 stick, 1 TigersEyeIngot, 2 CrystalPinkIngot, 3 iron ingot, 4 UraniumNugget, 6 TitaniumNugget, 7 MyIrukandji (= "deadirukandji"), 8 CrystalCoal block, 5/9 nothing | vortex_eye + xp bottle + gunpowder 3-8 + gold 1-3 | vortex_eye ×1 + painting ×1 + d10 pool rolls 5-11 (stick / tigers_eye_ingot / crystal_pink_ingot / iron_ingot / uranium_nugget / titanium_nugget / dead_irukandji / crystal_coal w1 each, empty w2). **Audit corrected:** no bone; painting + stick/iron/irukandji were unlisted |
| ENT-S-079 | `orig WormSmall.java:230-232` — `func_146068_u` = null → no drops | dirt 0-2 | empty pools |

## Spawn biome-modifier fixes

Where orig `func_70601_bi` gates exist and are not (fully) ported, the finding is **PARTIAL**
with the gate remainder owned by ENT-SYS-002 (Phase D). Weights/biomes JSON half is complete.

| ID | Orig citation + value | Old port value | New port value | Status |
|---|---|---|---|---|
| ENT-S-008 | `orig OreSpawnMain.java:4849-4852` — SeaMonster waterCreature ocean w4 1-1 + swamp w2 1-1 | `add_ocean_spawns` w1 1-1 | ocean w4 1-1; new `hostile_sea_monster__minecraft_is_swamp.json` w2 1-1 | PARTIAL — "Sea Monster" spawner-proximity gate (orig `:544-557`) → ENT-SYS-002; y≥50 + ≤1-buddy already ported |
| ENT-S-015 | `orig BiomeGenUtopianPlains.java:258-259` (setCrystalCreatures) — Skate CRYSTAL dim waterCreature w2 3-6; no other spawn source | `add_ocean_spawns` w6 1-2 | ocean entry removed; `dim_crystal_locals` skate w2 3-6 | PARTIAL — daytime gate (orig Skate.java:322-324) not ported (y≥50 / 1-in-30 / ≤6-buddy are, port `:184-189`) → ENT-SYS-002. **Audit corrected:** Crystal only, not "Island/Crystal" |
| ENT-S-029 | `orig ChunkProviderOreSpawn2.java:397-398` — Spyro MINING dim w5 1-2; no other spawn source | `companion_spyro__is_badlands/is_mountain` w1 1-1 | both files deleted; `dim_mining_locals` spyro w5 1-2 | PARTIAL — day + y≥50 gate (orig Spyro.java:407-412) → ENT-SYS-002. **Audit corrected:** Mining w5 only, not "Island/Crystal/Mining w1" |
| ENT-S-035 | `orig OreSpawnMain.java:4804-4808` — Stinky hell monster w2 1-1 + mesa/mesaPlateau/mesaPlateau_F ambient w1 1-1 each; `orig BiomeGenUtopianPlains.java:166-167` Island ambient w2 1-2 | `companion_stinky` forest/taiga w1 + `dim_islands` w2 1-2 | forest/taiga files deleted; `add_nether_spawns` stinky w2 1-1; new `companion_stinky__badlands.json` w2 1-1 (mesa+mesaPlateau merged) + `companion_stinky__wooded_badlands.json` w1 1-1 (mesaPlateau_F); Island entry already correct | PARTIAL — day + ≤2-buddy gate (orig Stinky.java:286-291) → ENT-SYS-002 |
| ENT-S-040 | `orig BiomeGenUtopianPlains.java:181-182` — TerribleTerror ISLAND monster w25 3-6; `:411-412` CHAOS w4 2-6; no overworld addSpawn | `add_overworld_monsters` w4 1-2 | overworld entry removed; `dim_islands_locals` w25 3-6 + `dim_chaos_locals` w4 2-6 | PARTIAL — dark + night + (dim6 or y≤40) + spawner gates (orig `:193-213`) → ENT-SYS-002 |
| ENT-S-044 | `orig BiomeGenUtopianPlains.java:495-496` — TRex CHAOS w1 1-1; `orig ChunkProviderOreSpawn2.java:376-377` MINING w6 1-2; no overworld addSpawn | `hostile_trex__badlands/savanna` w1 + `add_overworld_monsters` w1 | all three removed (2 files deleted); `dim_chaos_locals` trex w1 1-1 + `dim_mining_locals` trex w6 1-2 | PARTIAL — dark + y≥50 + spawner gates (orig TRex.java:276-299) → ENT-SYS-002. **Audit corrected:** dims are Chaos+Mining(w6), not "Island/Crystal w1" |
| ENT-S-062 | `orig BiomeGenUtopianPlains.java:228-229` — Urchin CRYSTAL w15 2-4; `:447-448` CHAOS w2 1-5; no ocean spawns | `add_ocean_spawns` w6 1-2 | ocean entry removed; `dim_crystal_locals` urchin w15 2-4 + `dim_chaos_locals` w2 1-5 | PARTIAL — spawner-proximity + surface-scan gates (orig Urchin.java:298-330) → ENT-SYS-002; night rule already ported (port `:170-173`). **Audit corrected:** Crystal+Chaos, not "Island/Crystal" |
| ENT-S-067 | `orig ChunkProviderOreSpawn2.java:409-410` — VelocityRaptor MINING dim ambient w1 2-4; no other spawn source | `companion_velocity_raptor` jungle/savanna w2 + `add_overworld_creatures` w4 | all three removed (2 files deleted); `dim_mining_locals` velocity_raptor w1 2-4 | PARTIAL — orig gate is y≥50 + DAYTIME (orig VelocityRaptor.java:78-83); port checks y≥50 + canSeeSky → day half → ENT-SYS-002. **Audit corrected:** Mining only, not "Island/Crystal/Mining" |
| ENT-S-071 | `orig BiomeGenUtopianPlains.java:225-226` (setCrystalCreatures) — Vortex CRYSTAL w3 1-2; `:405-406` CHAOS w1 1-2; NO overworld and NO Nether addSpawn exists | `add_nether_spawns` w4 — Nether only | Nether entry removed; `dim_crystal_locals` vortex w3 1-2 + `dim_chaos_locals` w1 1-2 | PARTIAL — spawner + block-scan gates (orig Vortex.java:240-260+) → ENT-SYS-002. **Audit corrected:** no "night overworld" spawn exists in the orig; sections are Crystal+Chaos, not Island/Crystal |
| ENT-S-080 | orig WormSmall has NO `addSpawn` (grep of OreSpawnMain/BiomeGenUtopianPlains/ChunkProviderOreSpawn2) — only WormLarge summons it | `add_overworld_creatures` w10 1-2 | entry removed | FIXED — no natural spawning is full parity |

Biome mapping used (per C1 conventions): mesa → `minecraft:badlands`, mesaPlateau → `minecraft:badlands`
(merged with mesa as w2), mesaPlateau_F → `minecraft:wooded_badlands` (direct biome ids, not the
`#is_badlands` tag, to keep orig per-biome weights exact); hell → `#minecraft:is_nether`;
ocean → `#minecraft:is_ocean`; swamp → `#minecraft:is_swamp`.

Note: `BiomeGenUtopianPlains` section map verified against the decompile — constructor (Utopia),
`setIslandCreatures` :142, `setCrystalCreatures` :201, `setVillageCreatures` :272,
`setChaosCreatures` :334. Several audit rows attributed entries one section early (the source of
the Island/Crystal mislabels corrected above).

## PARTIAL / deferred (owner: ENT-SYS-002, Phase D)

| ID | Remainder |
|---|---|
| ENT-S-008 | SeaMonster "Sea Monster"-spawner-proximity gate |
| ENT-S-015 | Skate daytime gate |
| ENT-S-029 | Spyro day + y≥50 gate |
| ENT-S-035 | Stinky day + ≤2-buddy gate |
| ENT-S-040 | TerribleTerror dark/night/(dim6 or y≤40)/spawner gates |
| ENT-S-044 | TRex dark/y≥50/spawner gates |
| ENT-S-062 | Urchin spawner-proximity + surface-scan gates |
| ENT-S-067 | VelocityRaptor daytime gate (port substitutes canSeeSky) |
| ENT-S-071 | Vortex spawner + block-scan gates |

## Files changed

**Java:** `ModEntities.java` (urchin/vortex `.fireImmune()`), `entity/SeaMonster.java`,
`entity/ai/SeaViperBiteGoal.java`, `entity/EntityStinkBug.java`, `entity/EntityStinky.java`,
`entity/TRex.java`, `entity/UltimateArrow.java`, `item/UltimateBow.java`,
`entity/VelocityRaptor.java`, `entity/EntitySpyro.java`, `entity/EntityVortex.java`

**Assets:** `assets/orespawn/sounds.json` (aggregate `fart` event)

**Loot JSONs:** `scorpion.json`, `skate.json`, `spider_robot.json`, `spit_bug.json`, `spyro.json`,
`terrible_terror.json`, `velocity_raptor.json`, `vortex.json`, `worm_small.json`

**Biome modifiers:** rewritten `add_ocean_spawns.json`, `add_overworld_monsters.json`,
`add_overworld_creatures.json`, `add_nether_spawns.json`, `dim_crystal_locals.json`,
`dim_chaos_locals.json`, `dim_islands_locals.json`, `dim_mining_locals.json`; new
`hostile_sea_monster__minecraft_is_swamp.json`, `companion_stinky__badlands.json`,
`companion_stinky__wooded_badlands.json`; deleted 2× `hostile_trex__*`, 2× `companion_spyro__*`,
2× `companion_velocity_raptor__*`, 2× `companion_stinky__minecraft_is_*`

## Build status

`.\gradlew.bat build --console=plain` — **BUILD SUCCESSFUL** (only the 4 pre-existing
EventBusSubscriber deprecation warnings).
