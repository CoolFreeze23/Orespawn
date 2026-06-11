# Phase C slice 3 — entities K–R (ENT-K-*) — 2026-06-11

Scope: all 26 `ENT-K-*` findings with Status DIVERGENT and no prior Resolution, plus 2
carried-forward PARTIAL remainders (ENT-K-051 PitchBlack scale model, ENT-K-068 Robot4 melee).
Total: 28. Counts: **25 FIXED · 2 VERIFIED-CORRECT · 1 PARTIAL** (spawn gates → ENT-SYS-002,
Phase D), 0 deferred.

Biome-mapping conventions follow `C1_entities_A_C.md` / `C2_entities_D_I.md`.
Loot/biome JSONs cannot carry comments — orig citations live in this report.

## Code fixes

| ID | Orig citation + value | Old port value | New port value | Notes |
|---|---|---|---|---|
| ENT-K-005 | `orig Kraken.java:199-204` ambient `"orespawn:kraken_living"` 1-in-5; `:210-212` death `"orespawn:alo_death"` | `Kraken.java:442,454` vanilla ELDER_GUARDIAN_AMBIENT/DEATH | `ModSounds.KRAKEN_LIVING` (1-in-5) / `ModSounds.ALO_DEATH` — both events already registered | |
| ENT-K-013 | `orig LeafMonster.java:178-207` — prey: EntityAnt, EntityButterfly, EntityLunaMoth, non-creative players; `:210-212` PlayNicely gate | width<1.0 heuristic targeted ANY small mob | explicit instanceof allow-list + `PLAY_NICELY` gate (`EntityLeafMonster.java`) | |
| ENT-K-019 | `orig Leon.java:986` raw BEEF 1-in-3 tame; `:1035` DEAD BUSH untame | tame beef (already correct); untame GLASS | untame → `Blocks.DEAD_BUSH` (`EntityLeon.java:734`) | **Audit corrected:** orig tame item is beef, not carrot (diamond-block instant-tame at :969 also already ported) |
| ENT-K-023 | `orig Lizard.java:184-227` `scan_it` matches WATER + FLOWING WATER only (`field_150355_j`/`field_150358_i`) | `scanForFire` sought LAVA/FIRE | `scanForWater` seeks `Blocks.WATER` (covers both modern fluid states); scan cadence/rings/1.33 nav speed unchanged | **Audit corrected:** orig never sought lava |
| ENT-K-033 | `orig MantisClaw.java:36-37` — `target.heal(-1)` (silent drain, no damage event) + `attacker.heal(+1)`, 1 durability | `target.hurt(magic, 1)` (invuln frames, hurt event) | drain via `setHealth(health-1)` guarded by `health > 0` (NeoForge `heal()` rejects negatives) | **Audit corrected:** no regen/wither effects exist in the orig |
| ENT-K-045 | `orig Ostrich.java:133-138` — super.hurt for every non-cactus source, cactus skipped, method always returns false | cactus skipped, non-cactus returned super's result | super.hurt for non-cactus, unconditional `return false` (orig quirk) | **VERIFIED-CORRECT** — audit misread the inverted condition: orig is immune to cactus ONLY, same as the port |
| ENT-K-046 | `orig Ostrich.java:283-294` — tamed → 2+nextInt(5)=2-6 poppies (`field_150328_O`); untamed → vanilla super = feather (`:279-281`) 0-2 (+looting) | `ostrich.json` chicken 1-3 always | NBT-branched JSON on `OreSpawnTamed` (flag written in `Ostrich.addAdditionalSaveData`, Gazelle convention): tamed → poppy 2-6; else feather 0-2 (+looting) | **Audit corrected:** no "sand" in the orig tamed drop |
| ENT-K-050 | `orig Peacock.java:259-261` — breeding item `MyCrystalApple` | `isFood` = wheat seeds | `ModItems.CRYSTAL_APPLE` (`Peacock.java:118`) | **Audit corrected:** audit claimed wheat |
| ENT-K-051 | `orig PitchBlack.java:99-141` — DISCRETE scale t ∈ {0.5,1,2,3,4} via cascading dice (start 0.5; 1/4→1, 1/8→2, 1/32→3, 1/64→4, later rolls override); NightmareSize 1-5 forces t; `:145,250` setSize(2.5t, 3.5t); `:389` flight speed 0.5+t/10 | tier scales {0.50,0.65,0.80,1.00,1.25}; hitbox base 2.0×3.0; uniform nextInt(5) tier roll | `SIZE_SCALE` = {0.5,1,2,3,4}; `BASE_WIDTH/HEIGHT` = 2.5/3.5; `finalizeSpawn` runs the orig dice cascade; wander offset `5*(int)scale` quirk (orig :346-347, t=0.5 adds 0) | **Audit corrected:** scale is NOT continuous; flight formula 0.5+t/10 was already the orig formula (audit conflated it with the 0.2+0.1t walk attribute fixed in Phase B); renderer/hitbox flow through `getPitchBlackScale()` |
| ENT-K-056 | `orig PurplePower.java:301-306` — type 2 → POISON 50t; type 3 → WEAKNESS 50t | type 2 HUNGER; type 3 POISON | type 2 POISON 50t; type 3 WEAKNESS 50t | **Audit corrected:** audit said type 2 = Weakness; that is type 3 |
| ENT-K-058 | `orig Rat.java:230-246` — RatPlayerFriendly/RatPetFriendly only gate rats with `myowner != null`; wild rats attack players AND pets regardless; `orig OreSpawnMain.java:1472-1473` both configs default 1 (true) | configs (default true) applied to ALL rats → wild rats permanently passive | both gates rescoped to `ownerUuid != null`; same-owner pet exclusion kept; config defaults stay true (= orig 1) | **Audit corrected:** orig defaults are TRUE — flipping them (the audit's fix) would diverge; the bug was the gate scope |
| ENT-K-068 | `orig Robot4.java:145-156` — `getAttackStrength` is dead code: never called anywhere in Robot4 (Spyro/Stinky/ThePrince call theirs from func_70652_k; Robot4's func_70652_k at :256-267 only adds knockback then super), and internally bugged (NORMAL/HARD branches nested inside the EASY branch — unreachable) | — | no change; melee = ATTACK_DAMAGE 12 (MobStats.ROBOT4, Phase B) | **VERIFIED-CORRECT** — the 15/20/25 difficulty melee does not exist in orig behavior |
| ENT-K-083 | `orig RubberDucky.java:242` raw fish 1-in-2 tame / tamed heal; `:273-287` dead bush untame | wheat tame/tempt; no untame | `Items.COD` tame + TemptGoal; dead-bush untame branch added (`EntityRubberDucky.java`) | |
| ENT-K-089 | `orig RubyBird.java:22-27` — `"orespawn:rubybird"` when day && !raining, else silent (no fallback) | inherited Cockateil `"birds"` | `getAmbientSound` override in `RubyBird.java`; `rubybird` event already in sounds.json | |

## Loot JSON fixes (citations)

| ID | Orig citation + value | Old port value | New port value |
|---|---|---|---|
| ENT-K-014 | `orig LeafMonster.java:144-153` — `func_146068_u` nextInt(3): oak log / leaves / ROTTEN FLESH; vanilla count 0-2 (+looting) | `leaf_monster.json` oak log 1-3 + bone 0-2 | single pool: equal-weight oak_log / oak_leaves / rotten_flesh, each 0-2 (+looting). **Audit corrected:** no stick in the orig list |
| ENT-K-027 | `orig LurkingTerror.java:368-377` — nextInt(3): raw BEEF (`field_151082_bd`) / FLINT (`field_151145_ak`) / FEATHER (`field_151008_G`); 0-2 (+looting) | bone 3-8 + 30% diamond 1-3 | one-of beef/flint/feather 0-2 (+looting). **Audit corrected:** audit claimed carrot/rotten flesh/feather |
| ENT-K-064 | `orig Robot2.java:165-221` — 2+nextInt(8)=2-9 iron BLOCK + 5+nextInt(6)=5-10 iron INGOT (`field_151042_j`) + 5+nextInt(10)=5-14 rolls of d15: 0 redstone, 1 repeater, 2 comparator, 3+8 redstone_block, 4 dispenser, 5 sticky_piston, 6 piston, 7 lever, 9 light_weighted_pressure_plate (`field_150445_bS`), 10-14 nothing | iron ingot 2-5 + 25% gold 0-2 | three pools: iron_block 2-9, iron_ingot 5-10, d15 table 5-14 rolls (redstone_block weight 2, empty weight 5). **Audit corrected:** the 5-10 drop is iron ingot, not coal |
| ENT-K-066 | `orig Robot3.java:166-219` — 5+nextInt(6)=5-10 drops of MyLaserBall ×4 + 5+nextInt(10)=5-14 rolls of the same d15 table | iron 3-6 + 20% diamond | pool 1: laser_ball ×4, rolls 5-10; pool 2: d15 table rolls 5-14 |
| ENT-K-071 | `orig Robot4.java:195-250` — 5+nextInt(10)=5-14 drops of MyLaserBall ×4 + MyRayGun ×1 + painting (`field_151160_bD`) ×1 + 10+nextInt(15)=10-24 rolls of the d15 table | iron 2-5 + redstone 2-5 | pools: laser_ball ×4 rolls 5-14; ray_gun ×1; painting ×1; d15 table rolls 10-24 |
| ENT-K-073 | `orig Robot5.java:138-190` — 5+nextInt(6)=5-10 drops of MyLaserBall ×4 + 2+nextInt(5)=2-6 rolls of the d15 table | iron 4-8 + gold 1-3 | pool 1: laser_ball ×4, rolls 5-10; pool 2: d15 table rolls 2-6 |
| ENT-K-059 | `orig Rat.java:140-142` — `func_146068_u` = ROTTEN FLESH (`field_151078_bh`); 0-2 (+looting) | bone 0-1 + rotten flesh 0-1 | rotten_flesh 0-2 (+looting). **Audit corrected:** audit claimed stick |
| ENT-K-081 | `orig Rotator.java:385-400` — nextInt(4): MyCrystalPinkIngot / MyTigersEyeIngot / CrystalCoal block / iron ingot; 0-2 (+looting) | iron nugget 2-5 + gunpowder 1-3 | equal-weight one-of crystal_pink_ingot / tigers_eye_ingot / crystal_coal / iron_ingot, each 0-2 (+looting) |

## Spawn biome-modifier fixes

| ID | Orig citation + value | Old port value | New port value | Notes |
|---|---|---|---|---|
| ENT-K-004 | orig Kraken has NO `addSpawn` (OreSpawnMain.java:3885-3891 registers the entity only) — spawner/summon/egg only; `func_70601_bi` (Y≥50 + open column) never reachable naturally | `add_ocean_spawns.json` kraken w1 1-1 | entry removed | FIXED — no natural spawning is full parity |
| ENT-K-009 | `orig OreSpawnMain.java:4801-4803` — Kyuubi hell w10 1-1 | `add_nether_spawns.json` w5 | w10 | |
| ENT-K-021 | orig Leon has NO `addSpawn`; spawn path = "Leonopteryx" mob spawners (orig Leon.java:452-465) placed by dungeons | `companion_leon.json` jungle w1 + Nether w2 + `boss_leonopteryx__minecraft_is_mountain.json` w1 | all three removed (2 files deleted, Nether entry deleted) | FIXED — LegacyDungeonPiece already places Leonopteryx spawners in the port |
| ENT-K-024 | `orig OreSpawnMain.java:4868-4870` — river w5 2-4, swamp w4 2-4, ocean w2 2-4 (waterCreature); `orig Lizard.java:368-370` getCanSpawnHere = Y≥50 only | `add_overworld_creatures.json` overworld-wide w10 1-2 | 3 `companion_lizard__{river,swamp,ocean}.json` w5/w4/w2, all 2-4; overworld entry removed | FIXED — the port's existing `checkSpawnRules` Y≥50 already equals the orig gate |
| ENT-K-060 | `orig OreSpawnMain.java:4977-4978` — roofedForest w35 10-20 + taiga w25 2-8 (ambient) | `add_overworld_monsters.json` overworld-wide w20 1-3 | `swarm_rat__dark_forest.json` w35 10-20 + `swarm_rat__taiga.json` w25 2-8; overworld entry removed | **PARTIAL** — darkness/spawner/Crystal-dim-air-pocket/≤8-buddy gates (orig Rat.java:302-339) → ENT-SYS-002 (Phase D) |
| ENT-K-088 | orig RubyBird = dungeon-spawner only (RubyBirdDungeon.java:74 "Ruby Bird" spawner; no addSpawn) | `dim_crystal_locals.json` crystal_plains w6 2-4 | entry removed | FIXED — port `GenericDungeon.tryPlaceRubyDungeon` places RUBY_BIRD spawners |

## VERIFIED-CORRECT (proof)

- **ENT-K-045** — orig `Ostrich.java:133-138`: `if (!source.equals("cactus")) super.hurt(...); return false;`
  — damage is applied for every non-cactus source and skipped for cactus, i.e. cactus is the ONLY
  immunity. The audit read this as "immune to everything except cactus", inverting it. The port
  already had cactus-only immunity; only the orig's unconditional `return false` quirk was added.
- **ENT-K-068 (remainder)** — orig `Robot4.java:145-156` `getAttackStrength` (15/20/25) is never
  invoked: grep of the orig tree shows Spyro/Stinky/ThePrince/ThePrincess call their versions from
  their attack methods, but Robot4's `func_70652_k` (:256-267) only adds knockback and defers to
  `super` (attribute damage). The method is also self-defeating: `var2 = 15` only when EASY, and the
  NORMAL/HARD checks are nested *inside* the EASY branch (unreachable). Effective orig melee =
  ATTACK_DAMAGE attribute = 12 (OreSpawnMain.java:6497), which the port has used since Phase B.

## PARTIAL / deferred (owner: ENT-SYS-002, Phase D)

| ID | Remainder |
|---|---|
| ENT-K-060 | Rat spawn gates: darkness + "Rat" spawner check + Crystal-dim Y≤50/air-pocket rule + ≤8-buddy cap (orig Rat.java:302-339) |

## Files changed

**Java:** `entity/Kraken.java`, `entity/EntityLeafMonster.java`, `entity/EntityLeon.java`,
`entity/Lizard.java`, `item/MantisClaw.java`, `entity/Ostrich.java`, `entity/Peacock.java`,
`entity/PitchBlack.java`, `entity/PurplePower.java`, `entity/EntityRat.java`,
`entity/EntityRubberDucky.java`, `entity/RubyBird.java`

**Loot JSONs:** `leaf_monster.json`, `lurking_terror.json`, `ostrich.json`, `rat.json`,
`robot_2.json`, `robot_3.json`, `robot_4.json`, `robot_5.json`, `rotator.json`

**Biome modifiers:** rewritten `add_ocean_spawns.json`, `add_nether_spawns.json`,
`add_overworld_creatures.json`, `add_overworld_monsters.json`, `dim_crystal_locals.json`;
new 3× `companion_lizard__*`, 2× `swarm_rat__*`; deleted `companion_leon.json`,
`boss_leonopteryx__minecraft_is_mountain.json`

## Build status

`.\gradlew.bat build --console=plain` — **BUILD SUCCESSFUL** (only pre-existing deprecation warnings).
