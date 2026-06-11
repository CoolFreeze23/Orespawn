# Phase C slice 1 — Entities A–C (ENT-A series), 2026-06-11

Scope: all 48 `ENT-A-*` findings with Status DIVERGENT and no prior Resolution, plus 13
carried-forward PARTIAL remainders (002, 012, 017, 023, 031, 040, 060, 072, 078, 091, 100, 103, 106).
Total: 61. Build: **GREEN** (`gradlew build` — BUILD SUCCESSFUL, NeoForge 21.1, Java 21).

Tally: **FIXED 46 · VERIFIED-CORRECT 3 · PARTIAL 12 · deferred 0**

---

## Carried-forward dimension / fire-immunity remainders (all FIXED)

| ID | Orig citation + value | Old port value | New port value | Notes |
|---|---|---|---|---|
| ENT-A-002 | `orig Alien.java:52` setSize(1.1, 3.25) | `ModEntities.java` 0.6×1.8 | `.sized(1.1f, 3.25f)` (ModEntities.java:19) | stats half closed in Phase B |
| ENT-A-012 | `orig AntRobot.java:52` setSize(2.75, 1.25) | 2.0×3.0 (W/H swapped) | `.sized(2.75f, 1.25f)` (ModEntities.java:566) | |
| ENT-A-017 | `orig AttackSquid.java:65` setSize(1.0, 1.25) | 0.8×0.8 | `.sized(1.0f, 1.25f)` (ModEntities.java:39) | |
| ENT-A-023 | `orig BandP.java:51` setSize(0.75, 1.75) | 0.6×1.0 | `.sized(0.75f, 1.75f)` (ModEntities.java:44) | **Audit corrected:** orig BandP has NO worn-gear armor clamp 8–23 (its `func_70658_aO` is flat); no clamp added |
| ENT-A-031 | `orig Basilisk.java:52` `field_70178_ae = true` | not fire-immune | `.fireImmune()` (ModEntities.java:50) | |
| ENT-A-040 | `orig Bee.java:45` setSize(1.5, 2.5) | 0.5×0.5 | `.sized(1.5f, 2.5f)` (ModEntities.java:201) | |
| ENT-A-060 | `orig Brutalfly.java:55,58` setSize(5.0, 2.0), fire-immune | 1.2×1.2, burns | `.sized(5.0f, 2.0f).fireImmune()` (ModEntities.java:206-207) | |
| ENT-A-072 | `orig CaterKiller.java:54-58` 2.9×4.6, halved when PlayNicely | 1.5×1.0, no config link | `.sized(2.9f, 4.6f)` (ModEntities.java:214) + `EntityCaterKiller#getDefaultDimensions` (1.45×2.3 when `PLAY_NICELY`) | |
| ENT-A-078 | `orig CaveFisher.java:44` setSize(1.35, 0.75) | 0.8×0.8 | `.sized(1.35f, 0.75f)` (ModEntities.java:55) | |
| ENT-A-091 | `orig CloudShark.java:41` setSize(1.0, 0.75) | 1.5×1.0 | `.sized(1.0f, 0.75f)` (ModEntities.java:60) | |
| ENT-A-100 | `orig Crab.java:133` tick() setSize(2.5×scale, 3.5×scale) | fixed 0.8×0.6 | `.sized(2.5f, 3.5f)` + `Crab#getDefaultDimensions` scaling on `DATA_SCALE`, `refreshDimensions()` on sync | **Audit corrected:** the 3.75 width is the spawn-time value (orig :97) but tick() overwrites it with 2.5×scale every tick — 2.5 is the live base |
| ENT-A-103 | `orig Crab.java:358-364` scorpion_attack/scorpion_living on swing, splash on heal | silent | sounds wired in `Crab#customServerAiStep` | carried-forward sound remainder |
| ENT-A-106 | `orig CreepingHorror.java:47` setSize(0.75, 0.5) | 1.5×1.5 | `.sized(0.75f, 0.5f)` (ModEntities.java:72) | |

## Loot JSON fixes

| ID | Orig citation + value | Old port value | New port value | Notes |
|---|---|---|---|---|
| ENT-A-005 | `orig Alien.java func_70628_a` — spider eye 5–10, flint 5–10, map ×1, clock ×1, compass ×1 | gunpowder 5–10 + iron 5–10 + ender pearl 1–3 | `alien.json` rewritten to the orig items | **Audit corrected:** audit claimed gold nuggets/iron/ender pearl; the obfuscated fields are spider eye (`field_151070_bp`), flint (`field_151145_ak`), map (`field_151148_bJ`) |
| ENT-A-015 | `orig AntRobot.java func_70628_a` — 7–13 rolls of a d10 redstone-component table | iron/redstone simple pools | `ant_robot.json` weighted jackpot, 7–13 rolls | |
| ENT-A-026 | `orig BandP.java` drops emeralds (`field_151166_bC`) | audit claimed leather | unchanged | **VERIFIED-CORRECT** — audit wrong; port already drops emeralds |
| ENT-A-037 | `orig Beaver.java` — vanilla EntityAnimal porkchop 0–2 (+looting) | leather | `beaver.json` porkchop 0–2 +looting | **Audit corrected:** audit claimed 2–6; vanilla `getDropItem` mechanism yields 0–2 |
| ENT-A-070 | `orig Camarasaurus.java:303-312` — 2–6 poppies, tamed only | always bones | `camarasaurus.json` poppy 2–6 gated on `OreSpawnTamed` NBT (flag written in `addAdditionalSaveData`) | |
| ENT-A-081 | `orig CaveFisher.java:141-153` — 1-in-6 each gold/uranium/titanium nugget | string 2–5 + spider eye | `cave_fisher.json` three 16.7% pools, 0–2 +looting | counts: orig uses vanilla 0–2 drop core |
| ENT-A-090 | `orig CliffRacer.java` — 1-in-8 raw chicken / uranium / titanium nugget | feathers | `cliff_racer.json` rewritten | |
| ENT-A-093 | `orig CloudShark.java` — 1-in-3 paper / string / bone | other items | `cloud_shark.json` rewritten | |
| ENT-A-097 | `orig Cockateil.java` — ruby when BirdType 5 + player kill + 1-in-3, else feathers | ruby ungated | `cockateil.json` alternatives pool with `BirdType` NBT predicate + `killed_by_player` + `random_chance` 1/3 | |
| ENT-A-098 | `orig Coin.java` — 10-slot weighted jackpot (emerald tools, diamonds, nuggets, CoinEgg) | gold | `coin.json` 10-slot table; CoinEgg slot empty | **PARTIAL** — CoinEgg item unported (Phase D) |
| ENT-A-109 | `orig CreepingHorror.java` — 1-in-3 rotten flesh / bone / string | inflated quantities | `creeping_horror.json` 0–2 +looting | |
| ENT-A-113 | `orig Cryolophosaurus.java:120-132` — 1-in-10 raw chicken / uranium / titanium nugget | bone 2–5 + diamond 20% | `cryolophosaurus.json` rewritten | |

## Behavior fixes

| ID | Orig citation + value | Old port value | New port value | Notes |
|---|---|---|---|---|
| ENT-A-001 | `orig LaserBall.java:83-132` — acid spares TrooperBug/SpitBug/Robot2–5/GiantRobot; ridden Dragons/players spared | all immunities removed | `LaserBall#onHitEntity` discards on Robot2–5/GiantRobot (non-ice/acid), ridden Dragon, ridden Player | **PARTIAL** — TrooperBug/SpitBug unported (Phase D) |
| ENT-A-004 | `orig Alien.java:200-210` — POISON `var2*5` ticks (8 on Easy, 6 otherwise; 10/12 branches unreachable) | HUNGER fixed 30t | POISON, 40t Easy / 30t else, quirk branches preserved | **Audit corrected:** effect is Poison (`field_76436_u`), not Hunger |
| ENT-A-013 | `orig AntRobot.java:130-145` — unridden melee gated 1-in-15/tick | every tick | 1-in-15 gate restored | |
| ENT-A-014 | `orig AntRobot.java:617-619,1000` — ridden 1-in-50 stomp, damage attack/10 | no stomp; flat 3.5 | stomp restored; damage `ATTACK_DAMAGE/10` (= 3.0) | |
| ENT-A-025 | `orig BandP.java:46,185-218` — steal on every hit, 100-slot stash, armor first | 1-in-4 chance, 16 slots | every hit, `STASH_SIZE = 100` | |
| ENT-A-033 | `orig Basilisk.java:316-330` — bite carries no Slowness (gaze goal handles debuffs) | bite also applied Slowness | Slowness removed from `doHurtTarget` | |
| ENT-A-036 | `orig Basilisk.java` sounds — basilisk_living / alo_hurt / emperorscorpion_death | vanilla Ravager sounds | custom sounds wired (all exist in `sounds.json`) | |
| ENT-A-042 | `orig Bee.java` — POISON on hit | audit claimed Hunger | unchanged | **VERIFIED-CORRECT** — orig uses `field_76436_u` (Poison); port already matches |
| ENT-A-054 | `orig Boyfriend.java:127-148` — Tempt(cooked beef), ArrowAttack, Panic(1.5)@6, OpenDoor@10, MoveIndoors@11, Jealousy@4/5 | Tempt(DIAMOND), melee only | cooked-beef tempt, `PanicGoal(1.5)`@6, `OpenDoorGoal`@10 + door navigation | **PARTIAL** — ranged attack (ENT-A-055), Jealousy goals, MoveIndoors → Phase D |
| ENT-A-068 | `orig Camarasaurus.java:114` — eats leaves/vine/tall grass/cactus/double plants | wrong block list | `isEdibleBlock` rewritten to the orig diet | |
| ENT-A-074 | `orig CaterKiller.java:438-448` — 2400t damaged timer → 1 Brutalfly + 10 Butterflies + explosion sound, then remove | silent discard (claimed parity) | metamorphosis implemented in `customServerAiStep`; non-reset ticker quirk kept | also closes the CaterKiller half of ENT-SYS-003's comment complaints |
| ENT-A-075 | `orig CaterKiller.java:502-530` — 1-in-8 hurt / 1-in-30 idle, PlayNicely==0, nearest tree block ≤12, eat at distSq<81, heal 2.0, 1-in-20 burp | invented random-munch 5/10 HP + 1-in-150 heal 2.0 | orig logic restored (`findNearestTreeBlock`), inventions removed | leaf/log tags + vine cover the orig block list incl. mod leaves |
| ENT-A-080 | `orig CaveFisher.java:193-228` — preys on players & all non-monster living | players only | added `NearestAttackableTargetGoal<Animal>`@3 | orig excludes EntityMob/CaveFisher/EnderReaper/EnderKnight — Animal targeting reproduces that set |
| ENT-A-082 | `orig Cephadrome.java:404-432,515-573` — targets Monsters, Mothra, untamed Leon/GammaMetroid/WaterDragon, EnderDragon (70 part-hit), Kraken ×1.5 | those mobs explicitly excluded | inclusions restored; EnderDragon hit with explosion-typed 70; Kraken ×1.5 | tamed-player gate (Phase-14 invention) left for ENT-A-083 (MISSING, Phase D) |
| ENT-A-087 | `orig Chipmunk.java:141,172` — tame apple (1-in-2); untame dead bush | wheat / glass | apple / dead bush | |
| ENT-A-095 | `orig Cockateil.java:82-86` — BirdType = nextInt(6) at spawn | never randomized | `finalizeSpawn` override | |
| ENT-A-101 | `orig Crab.java:74-98` — scale dice 0.25/0.5/1.0; spawner-spawned fixed 0.35 | no randomization | `finalizeSpawn` implements the dice + spawner case | |
| ENT-A-102 | `orig Crab.java:314-338` — seeks water, dries out (damage), discards if none found | `WaterAvoidingRandomStrollGoal` (inverted!) | `RandomStrollGoal` + water-seek scan + dry-out damage/discard | |
| ENT-A-110 | `orig CreepingHorror.java:220-228` — dark + night + (dim6 Chaos or y≤15) | no rules | `checkSpawnRules` override enforcing all three | |
| ENT-A-112 | `orig Cryolophosaurus.java:141-211` — 1-in-5 proactive hunt over 9×2×9, exclusion list, 1-in-200 forgiveness | retaliation-only; comment claimed it never hunted | `customServerAiStep` scan + `isSuitableTarget`/`findSomethingToAttack`; comment fixed | closes the Cryolophosaurus half of ENT-SYS-003's comment complaints |
| ENT-A-114 | `orig CrystalCow.java:13-14` — extends RedCow | extended vanilla `Cow` | re-parented to port `RedCow` (inherits 1-in-200 forgiveness + never-despawn) | |

## Bertha / BerthaHit

| ID | Orig citation + value | Old port value | New port value | Notes |
|---|---|---|---|---|
| ENT-A-045 | `orig OreSpawnMain.java` `get_weaponstats` — Bertha 9000 dur/496 dmg, Royal 10000/746, Hammy ("Attitude") 2000/82 | audit claimed values differ | unchanged | **VERIFIED-CORRECT** — `ModToolTiers.BERTHA/ROYAL/HAMMY` already match exactly |
| ENT-A-047 | `orig Bertha.java:65-76` — skip list only while `big_bertha_pvp == 0`; spares players, Girlfriend, Boyfriend, tamed | players config-gated but tamed skipped unconditionally; GF/BF missing | whole list gated on `BIG_BERTHA_PVP`; GF/BF added | config already existed (`bigBerthaPvp`, default false = orig 0) |
| ENT-A-048 | `orig BerthaHit.java:76-105` — damage 496 (t0) / 746 (t2) / 82 (t3), from bertha/royal/hammy stats | 250 / 150 / 100 | 496 / 746 / 82 | |
| ENT-A-049 | `orig BerthaHit.java:76-105` — distSq-to-owner gates 81 / 101 / 64 per type | single 100 | per-type `maxRangeSq` 81 / 101 / 64 | explosion-radius branch (1.5 vs 2.1) is ENT-A-050 (PARTIAL, out of scope) |
| ENT-A-051 | `orig BerthaHit.java:68-75` — `pvp==0 && player \|\| GF \|\| BF` — precedence makes GF/BF spared unconditionally; players/tamed only at pvp==0 | players/tamed spared unconditionally | GF/BF discarded always; players/tamed only when `!BIG_BERTHA_PVP` | orig operator-precedence quirk preserved deliberately |

## Spawn biome-modifier fixes (all PARTIAL — JSON half done; `checkSpawnRules` gates owned by ENT-SYS-002, Phase D)

| ID | Orig citation + value | Old port value | New port value | Notes |
|---|---|---|---|---|
| ENT-A-006 | `orig ChunkProviderOreSpawn2.java:389` — Mining-dim monster list w35 2–3; gates spawner-tag/dim4/y<50&dark | `add_end_spawns.json` (the End!) w3 | removed from End; `dim_mining_locals.json` w35 2–3 | **Audit corrected:** audit said "no addSpawn" — the Mining-dim spawn-list entry exists |
| ENT-A-011 | `orig ChunkProviderOreSpawn2.java:374` w8 1–2 + `BiomeGenUtopianPlains.java:415` w1 1–1; NO overworld addSpawn | savanna w1 2–3 + overworld w3 | overworld modifiers removed; Mining w8 1–2 + Utopia w1 1–1 | **Audit corrected:** audit claimed "addSpawn multiple biomes" — none exists in orig |
| ENT-A-027 | `orig OreSpawnMain.java:4626-4628` — plains/desert/savanna w20 1–2 (ambient) | generic overworld w3 1–1 | `hostile_band_p.json` plains/desert/savanna w20 1–2 | night/y≥50/villager rules → ENT-SYS-002 |
| ENT-A-029 | `orig ChunkProviderOreSpawn2.java:419` w2 4–8 + `BiomeGenUtopianPlains.java:374` w2 2–4 | generic overworld w3 1–1 | Mining w2 4–8 + Utopia w2 2–4; overworld entry removed | day/y>50/≤8-buddies rules → ENT-SYS-002 |
| ENT-A-035 | `orig OreSpawnMain.java:4877-4880` — jungle w3, jungleHills w2, birchForestHills w4 1–2, roofedForest w15 1–2 | badlands+jungle w3 1–1 | `hostile_basilisk__{jungle,sparse_jungle,old_growth_birch,dark_forest}.json` w3/2/4/15 | **Audit corrected:** biomes are NOT mushroom/mega-taiga (`field_150582_Q`=birchForestHills, `field_150585_R`=roofedForest) |
| ENT-A-044 | `orig OreSpawnMain.java:4709-4718` — forest w2 1–2, jungle w5 3–5, jungleHills w5 2–5, birch w3 2–4, megaTaiga/taiga w5 1–2, savanna w3 1–1, savannaPlateau w2 1–1 | generic overworld w8 1–3 | seven `hostile_bee__*.json` per-biome files | day/clear-air/y>50 rules → ENT-SYS-002 |
| ENT-A-057 | `orig OreSpawnMain.java:4588-4599` — beach w30 8–15, forest/river/stoneBeach w10 3–6, forestHills w8 2–5, plains w5 2–3, birch w5 2–4, birchHills/megaTaiga/taiga w5 2–5, savanna(+plateau) w2 1–3 | `#is_overworld` w4 1–2 | seven `companion_boyfriend*.json` per-biome files | FIXED (no spawn-rule gate in orig Boyfriend) |
| ENT-A-064 | `orig OreSpawnMain.java:4839-4841` — megaTaigaHills/extremeHillsPlus/mesaPlateau w2 1–1 | generic overworld w3 1–1 | `hostile_brutalfly.json` old_growth_spruce_taiga/windswept_forest/badlands w2 1–1 | **Audit corrected:** no savanna plateau in the orig list; y≥70/dark/night/clear-air/64-radius rules → ENT-SYS-002 |
| ENT-A-071 | `orig ChunkProviderOreSpawn2.java:416` — Mining-dim ambient w1 2–4 only | overworld w2 1–1 + jungle/savanna companions | Mining w1 2–4; all overworld modifiers removed | y≥50+day rules → ENT-SYS-002 |
| ENT-A-077 | `orig OreSpawnMain.java:4746-4754` — forest w2 1–1, forestHills/jungleHills w4 1–2, jungle w2 1–2, birch w6 1–2, birchHills/megaTaiga/taiga w2 1–2, roofedForest w10 1–2 | forest/jungle/taiga/badlands w4 1–2 | seven `hostile_cater_killer__*.json` files; badlands dropped | day/y≥50/dice/clearance rules → ENT-SYS-002 |

**Biome-mapping conventions** (1.18 removed the `*Hills` biomes): forestHills → `windswept_forest`,
jungleHills → `sparse_jungle`, birchForestHills → `old_growth_birch_forest`,
megaTaigaHills → `old_growth_spruce_taiga`, mesaPlateau → `badlands`, stoneBeach → `stony_shore`,
megaTaiga → `old_growth_pine_taiga`, roofedForest → `dark_forest`. Documented per file.

## VERIFIED-CORRECT (audit wrong, with proof)

- **ENT-A-026** — orig `BandP.func_70628_a` drops `Items.field_151166_bC` = **emerald** (cross-checked in
  `OreSpawnMain.java` item usage), not leather. Port `band_p.json` already drops emeralds.
- **ENT-A-042** — orig `Bee.java` applies potion `field_76436_u` = **Poison** on hit, not Hunger. Port
  already applies Poison with the orig difficulty-scaled duration.
- **ENT-A-045** — orig `get_weaponstats(...)` in `OreSpawnMain.java` registers Bertha (9000 uses, 496 dmg,
  ench 100), Royal (10000, 746, 150→100), Hammy (2000, 82). `ModToolTiers.BERTHA/ROYAL/HAMMY` carry
  exactly those numbers; `BerthaAttributes.createReachAttributes` adds the vanilla sword +3 like
  `SwordItem.createAttributes`.

## PARTIAL / deferred (owner: Phase D unless noted)

| ID | Remainder | Owner |
|---|---|---|
| ENT-A-001 | TrooperBug / SpitBug acid immunity (entities unported) | Phase D |
| ENT-A-006 | spawn-rule gates (dark / y<50 / Utopia dim) | ENT-SYS-002 (Phase D) |
| ENT-A-011 | spawn rules (y>50, night, !raining, buddy check) | ENT-SYS-002 (Phase D) |
| ENT-A-027 | night + y≥50 + nearby-villager rule | ENT-SYS-002 (Phase D) |
| ENT-A-029 | day / y>50 / ≤8-buddy rules | ENT-SYS-002 (Phase D) |
| ENT-A-035 | night + spawner check + buddy rule | ENT-SYS-002 (Phase D) |
| ENT-A-044 | day / clear-air / y>50-or-Utopia rules | ENT-SYS-002 (Phase D) |
| ENT-A-054 | ranged attack (needs ENT-A-055 weapon system), Jealousy goals, MoveIndoors | Phase D |
| ENT-A-064 | y≥70 / dark / night / clear-air / 64-block-radius rules | ENT-SYS-002 (Phase D) |
| ENT-A-071 | y≥50 + day rules | ENT-SYS-002 (Phase D) |
| ENT-A-077 | day / y≥50 / 1-in-10 dice / clearance / 48-radius rules | ENT-SYS-002 (Phase D) |
| ENT-A-098 | CoinEgg jackpot slot (item unported) | Phase D |

## Files changed

**Java:** `ModEntities.java`, `entity/Alien.java`, `entity/AntRobot.java`, `entity/BandP.java`,
`entity/Basilisk.java`, `entity/BerthaHit.java`, `entity/Boyfriend.java`, `entity/Camarasaurus.java`,
`entity/CaveFisher.java`, `entity/Cephadrome.java`, `entity/Chipmunk.java`, `entity/Cockateil.java`,
`entity/Crab.java`, `entity/CreepingHorror.java`, `entity/CrystalCow.java`, `entity/Cryolophosaurus.java`,
`entity/EntityCaterKiller.java`, `entity/LaserBall.java`, `item/Bertha.java`

**Loot JSONs:** `alien.json`, `ant_robot.json`, `beaver.json`, `camarasaurus.json`, `cave_fisher.json`,
`cliff_racer.json`, `cloud_shark.json`, `cockateil.json`, `coin.json`, `creeping_horror.json`,
`cryolophosaurus.json`

**Biome modifiers:** rewritten `add_end_spawns.json`, `add_overworld_monsters.json`,
`add_overworld_creatures.json`, `dim_utopia_locals.json`, `companion_boyfriend.json`; new
`dim_mining_locals.json`, `hostile_band_p.json`, `hostile_brutalfly.json`, 6× `companion_boyfriend__*.json`,
4× `hostile_basilisk__*.json`, 7× `hostile_bee__*.json`, 7× `hostile_cater_killer__*.json`; deleted
`hostile_alosaurus.json`, 2× `companion_camarasaurus__*.json`, 2× old `hostile_basilisk__*.json`,
5× old `hostile_cater_killer__*.json`

## Build status

`.\gradlew.bat build --console=plain` → **BUILD SUCCESSFUL** (only pre-existing deprecation warnings).
