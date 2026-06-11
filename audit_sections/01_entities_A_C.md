# Entity Audit: A–C Slice (Acid … CrystalCow)

ORIGINAL = `reference_1_7_10_source\sources\danger\orespawn\` (CFR-decompiled 1.7.10).
PORT = `src\main\java\danger\orespawn\` + `src\main\resources\data\orespawn\` (NeoForge 1.21.1).
Original stats: `OreSpawnMain.get_mobstats(name, health, attack, defense)`.

---

### Acid

| Feature | Original | Port | Status |
|---|---|---|---|
| Stats | None of its own; projectile subclass of `LaserBall`, sets acid flag in ctor (`Acid.java`, `super.setAcid()`) | `entity/Acid.java` extends `LaserBall`, `this.setAcid()` in all ctors | PORTED |
| Attack/abilities | Inherits LaserBall: 16.0 dmg, `func_70015_d(1)` = 1 s ignite when not iceball, special immunities (TrooperBug/SpitBug/Robot2–5/GiantRobot when acid) | Inherits port LaserBall: 16.0 dmg, `igniteForSeconds(1)` ✓, **all entity immunities removed** (`entity/LaserBall.java`) | DIVERGENT (via parent: immunities) |
| Drops/Spawning/Sounds | n/a (projectile) | n/a | PORTED |

**Verdict: PORTED** as a subclass — but inherits LaserBall's divergences (ignite 1 tick→1 s; immunity list deleted).

---

### Alien

| Feature | Original | Port | Status |
|---|---|---|---|
| Stats | HP 100, atk 12, armor 8 (`OreSpawnMain.java:6491`), speed 0.65, XP 100, size 1.1×3.25 (`Alien.java` ctor) | HP 80, atk 12, armor 6, speed 0.65, XP 100 (`entity/Alien.java` `createAttributes`); size 0.6×1.8 (`ModEntities.java`) | DIVERGENT (HP 100→80, armor 8→6, size shrunk) |
| AI goals | Swim, `EntityAIMoveThroughVillage`, `MyEntityAIWanderALot(2,16)`, WatchClosest(8), LookIdle; target HurtBy; torch destruction in `func_70619_bc` (scan ≤15 blocks for torch/ExtremeTorch); self-heal 1HP @ `nextInt(40)==1` | `FloatGoal`, `AlienTorchSeekGoal`(1) (adds mobGriefing check + throttle), `WaterAvoidingRandomStrollGoal`, LookAt, RandomLook; HurtBy; `customServerAiStep` target scan AABB 12×4×12; self-heal 1HP/40t | PARTIAL (MoveThroughVillage missing; torch logic approximated) |
| Attack | Melee 12; Hunger `var2*5` ticks difficulty-scaled (6/8/10/12 mult), 1-in-5 chance; KB 1.1/0.1 (×2 vert vs player) | Melee 12; `MobEffects.HUNGER` fixed 30 ticks amp 0, 1-in-5; KB identical | DIVERGENT (effect duration not difficulty-scaled) |
| Drops | `func_70628_a`: gold nuggets 5–10, iron ingots 5–10, ender pearl 1, compass 1, clock 1 | `loot_table/entities/alien.json`: gunpowder 5–10, iron 5–10, ender pearl 1–3; no compass/clock | DIVERGENT |
| Spawning | No `addSpawn`; `func_70601_bi`: spawner tag "Alien", or dim 4 (Utopia), or underground y<50 & dark | Biome modifier `add_end_spawns.json` → `#minecraft:is_end` weight 3, 1–1; no `checkSpawnRules` | DIVERGENT (underground/dim4 → End) |
| Sounds | `alien_living` (1-in-4), `alien_hurt`, `alien_death`, vol 1.0 | same custom sounds, vol 1.0 | PORTED |
| Misc | fire immunity NOT set (field_70178_ae=false) | jump boost added in `jumpFromGround` (new) | PARTIAL |

**Verdict: DIVERGENT** — stats, drops, and spawn dimension all changed.

---

### Alosaurus

| Feature | Original | Port | Status |
|---|---|---|---|
| Stats | HP 110, atk 18, def 8 (`OreSpawnMain.java:6473`), speed 0.35, XP 40, size 1.9×3.6 | HP 60, atk 15, armor 0, follow 32, speed 0.35, XP 40 (`entity/Alosaurus.java` `createAttributes`) | DIVERGENT (110→60, 18→15, 8→0) |
| AI goals | Swim, MoveThroughVillage, `MyEntityAIWanderALot(2,16)`, WatchClosest(8), LookIdle; HurtBy; attack via `func_70619_bc` (1-in-5 find, ~40% swing) | Float, `DinosaurMeleeAttackGoal(1)` w/ `Presets.alosaurus()`, WanderALot, LookAt, RandomLook; HurtBy(alertOthers), `NearestAttackableTargetGoal<Player>` | PARTIAL (village goal gone; player-targeting added) |
| Attack | Melee; KB 1.2/0.1 (×2 player) | same KB values | PORTED |
| Drops | 10 bones + 6 raw beef (`func_70628_a`) | `alosaurus.json` (gunpowder 5–10 + diamond 3–6) **and** `dropCustomDeathLoot` 10 bones + 6 beef → double drop | DIVERGENT + double-drop bug |
| Spawning | `addSpawn` multiple biomes; `func_70601_bi`: y>50, night, not raining, no other Alosaurus | `hostile_alosaurus.json` `#minecraft:is_savanna` w1 2–3; conditions deleted | DIVERGENT |
| Sounds | `alo_living` (1-in-4)/`alo_hurt`/`alo_death`, vol 1.5 | identical | PORTED |

**Verdict: DIVERGENT** — combat stats cut nearly in half, bonus diamond loot invented, double drops.

---

### AntRobot

| Feature | Original (`AntRobot.java`) | Port (`entity/AntRobot.java`) | Status |
|---|---|---|---|
| Stats | HP 300, atk 30, def 16 (`OreSpawnMain.java:6475`), speed 0.3, XP = health/2 = 150 (`AntRobot.java:58`), size 2.75×1.25, fire-immune | HP 350, atk 35, armor 6, speed 0.3, XP 150 (`AntRobot.java:71-77`); size 2.0×3.0 (`ModEntities.java:548`) | DIVERGENT (HP 300→350, atk 30→35, armor 16→6, size W/H swapped+changed) |
| AI goals | WatchClosest(12)@1, LookIdle@2; unowned: 1-in-20 stomp scan, 1-in-15 melee attempt at range <(6+w/2), chase via `goThisWay(0.2·cos/sin)` (`func_70619_bc:96-147`) | LookAt(12)@1, RandomLook@2; same scan structure but **melee fires every customServerAiStep tick in range** (no 1-in-15 gate, `AntRobot.java:108-125`) | DIVERGENT (attack rate massively higher) |
| Attack | Melee = atk (30), KB 0.7/0.1; stomp (`feetattackEntityAsMob`) = atk/10 = 3.0, KB 0.6, ring 6–9 blocks; ridden: 1-in-50 stomp + 1-in-9 melee (`func_70071_h_:617-631`) | Melee hardcoded 35.0, KB 0.7/0.1; stomp 3.5 KB 0.6 ring 6–9; ridden 1-in-9 melee only — **1-in-50 ridden stomp missing** | DIVERGENT |
| Drops | 7–13 rolls of redstone/repeater/comparator/redstone block/dispenser/sticky piston/piston/lever/pressure plate/iron ingot (`func_70628_a:1112-1164`) | `ant_robot.json`: iron 3–8 + gold 1–3 | DIVERGENT |
| Spawning | none (item-summoned); never despawns | none; MISC category | PORTED |
| Sounds | `robotspider` idle while ridden (1-in-80, 0.35f), `robotspidermount` on mount 0.45f | identical | PORTED |
| Misc | Ridable with custom hover/walk physics (obstruction climb, velocity model, `func_70636_d:659-877`); immune inWall/cactus/inFire/onFire/magic/starve; no fall damage; heal ≤100 with iron ingot; complex procedural leg animation (`updateLegs`) | Riding = vanilla `startRiding` only — **custom hover/ride movement physics absent**; same damage immunities; iron heal ≤100 ✓; leg data replaced with sine-wave approximation | PARTIAL (ride physics missing) |

**Verdict: DIVERGENT** — stats shifted, ridden movement physics lost, melee throttle removed.

---

### AttackSquid

| Feature | Original | Port | Status |
|---|---|---|---|
| Stats | HP 10, atk 8, def 0 (`OreSpawnMain.java:6510`), speed 0.25, XP 15, size 1.0×1.25 | HP 30, atk 8, armor 0, XP 15; size 0.8×0.8 (`entity/AttackSquid.java`) | DIVERGENT (HP 10→30) |
| AI | Swim, WanderALot(1,16), Watch(8), LookIdle; HurtBy; water-seek scan + heal logic in `func_70619_bc`; melee 1-in-10 trigger, ~40% swing at distSq<9 | Float, WanderALot(1,16), LookAt, RandomLook; HurtBy; water-seek (12-block scan) + dry-out 1 dmg/25t; melee 1-in-10, 1-in-4 swing | PARTIAL |
| Attack | Melee 8 + ranged `watercanon`: `InkSack` (1-in-3) or `WaterBall` (2-in-3), speed 1.4 spread 5.0 | Melee only — **ranged attack missing** | PARTIAL (ranged MISSING) |
| Drops | 1–3 ink sacs; 1-in-50 enchanted gear roll; 1–3 fish | `attack_squid.json` (gunpowder 2–5, iron 1–3, gold 0–2@30%) + custom 1–3 cod & 1-in-5 diamond → double source | DIVERGENT + double-drop |
| Spawning | `addSpawn` rivers/oceans w12 6–10; `func_70601_bi`: y>50 & daylight | **No biome modifier exists** for attack_squid; `checkSpawnRules`: y<50 && canSeeSky (inverted vs original) | MISSING (no natural spawn) + DIVERGENT rules |
| Sounds | none/`squid_hurt`/`squid_death`, vol 1.0 | identical | PORTED |
| Misc | `wasshot` flag (SquidZooka); Kraken revenge 1-in-15 on player kill (1–3 Krakens, KrakenEnable, not crystal dim); hurt() ignored WaterBall/WaterDragon from squids | `wasshot` ✓; Kraken revenge moved to `KrakenRevengeHandler.java` ✓; hurt() projectile exclusions missing | PARTIAL |

**Verdict: DIVERGENT** — no natural spawns, no ranged attack, different drops, inverted spawn rules.

---

### BandP (Burglar & Pickpocket)

| Feature | Original | Port | Status |
|---|---|---|---|
| Stats | HP 100, atk 1, def 18 (`OreSpawnMain.java:6480`; runtime armor clamp 8–23), speed 0.32, XP 1000, size 0.75×1.75 | HP 30, atk 5, armor 0, XP 10, size 0.6×1.0 (`entity/BandP.java`) | DIVERGENT (all five values) |
| AI | MoveThroughVillage, WanderALot(1,16), Watch(10), LookIdle, OpenDoor, MoveIndoors; aggro in `func_70619_bc` (1-in-12), targets Player/Villager/Girlfriend/Boyfriend | WaterAvoidingStroll, LookAt, RandomLook; `customServerAiStep` 1-in-12 aggro; targets Player only | PARTIAL (3 goals + 3 target types missing) |
| Attack | Melee steals item (armor first) into 100-slot `MymainInventory`; despawns if `got_stuff==0` | `tryStealFromPlayer` 1-in-4, stash 16 slots; persistent when `gotStuff` | DIVERGENT (probability, stash 100→16) |
| Drops | 10–14 leather; if `getWhat()==0` 2–4 Uranium+Titanium **nuggets**; all stolen items | `band_p.json`: leather 10–14, uranium **ingot** 0–3@50%, titanium **ingot** 0–3@50%; stash dropped via `dropCustomDeathLoot` ✓ | DIVERGENT (nuggets→ingots) |
| Spawning | plains/desert/savanna w20 1–2; night, y≥50, villager-count condition | `add_overworld_monsters.json` w3 1–1; no conditions | DIVERGENT |
| Sounds | villager idle/hit/death, vol 1.5 | `SoundEvents.VILLAGER_*`, vol 1.5 | PORTED |

**Verdict: DIVERGENT** — every stat changed, stealing nerfed, spawn conditions deleted.

---

### Baryonyx

| Feature | Original | Port | Status |
|---|---|---|---|
| Stats | HP 40 (hardcoded `mygetMaxHealth`), atk 8, speed 0.25, XP 5, size 1.5×2.8 | identical values (`entity/Baryonyx.java` `createAttributes`) | PORTED |
| AI | Swim, Mate, Avoid(Mob), Panic, Watch(12), MyEntityAIWander, LookIdle; grass-graze heal (1-in-60 hurt / pathing scan 11, eat at distSq<12, mobGriefing) | Float, Breed, Avoid(Monster), Panic, LookAt(12), MyEntityAIWander, RandomLook; same graze logic incl. mobGriefing; target clear every 200t | PORTED |
| Attack | Melee 8, no effects | same | PORTED |
| Drops | 2–6 raw beef (`func_70628_a`) | `baryonyx.json` bones 2–5 **plus** custom 2–6 beef | PARTIAL (bones added; double source) |
| Spawning | mining-dim biomes (`BiomeGenUtopianPlains`, `ChunkProviderOreSpawn2` w2 4–8); y>50, day, ≤8 buddies | `add_overworld_creatures.json` w3 1–1; conditions removed | DIVERGENT |
| Sounds | none/`duck_hurt`/`duck_hurt`, vol 0.4 | identical | PORTED |
| Misc | breed wheat/crystal apple | breed `CRYSTAL_APPLE` only | PARTIAL |

**Verdict: PARTIAL** — core ported faithfully; drops augmented and spawn conditions dropped.

---

### Basilisk

| Feature | Original | Port | Status |
|---|---|---|---|
| Stats | HP 200, atk 24, def 15 (`OreSpawnMain.java:6487`), speed 0.4, XP 150, size 1.6×3.5, fire-immune (`field_70178_ae=true`) | HP 500, atk 25, armor 8, follow 48, KB-resist 0.8, XP 150 (`entity/Basilisk.java`); fire immunity not set | DIVERGENT (HP 200→500, armor 15→8, fire immunity lost) |
| AI | Swim, MoveThroughVillage, WanderALot(2,20), Watch(8), LookIdle; HurtBy; melee in `func_70619_bc` (1-in-5 find, 1-in-3/1-in-4 swing); heal 1HP/75t + 1-in-200 aiStep heal | Float, `BasiliskGazeAttackGoal`(1) (Slowness V aura 6 blocks + Poison on bite), Stroll, LookAt, RandomLook; HurtBy + NearestAttackable<Player>; heal 1HP/75t + 1HP/200t | PARTIAL (gaze aura invented) |
| Attack | Melee 24 + `Potion.field_76421_d` 100t amp 5, 1-in-3; KB 1.5/0.15 | Melee 25 + `MOVEMENT_SLOWDOWN` 200t amp 0 1-in-3; Poison handled in gaze goal; KB identical | DIVERGENT (effect type/duration/amp) |
| Drops | BasiliskScale 1, **Item Frame** 1, 12–17 emeralds, 8–12 cooked cod, 3–7 bonus rolls (1-in-15 enchanted emerald gear) | `basilisk.json` (scale, **name_tag**, emerald 12–17, raw cod 8–12) **plus** custom (scale, golden apple, 12–17 emeralds, 8–12 gold ingots, bonus gear rolls) | DIVERGENT + double-drop |
| Spawning | mushroom/jungle/mega-taiga w3–15; night, spawner check, no buddy | `hostile_basilisk__*` badlands+jungle, w3 1–1 | DIVERGENT |
| Sounds | `basilisk_living` (1-in-2)/`alo_hurt`/`emperorscorpion_death`, vol 1.0 | `RAVAGER_ROAR/HURT/DEATH` vanilla | DIVERGENT |

**Verdict: DIVERGENT** — HP more than doubled, custom sounds dropped, double drops, fire immunity lost.

---

### Beaver

| Feature | Original | Port | Status |
|---|---|---|---|
| Stats | HP 15 (hardcoded), atk 1, speed 0.2, XP 5, size 0.6×0.8 | identical (`entity/Beaver.java`) | PORTED |
| AI | Swim/Mate/Avoid(Mob,Player)/Panic/Watch(6)/WanderALot; wood chop+heal (1-in-30 hurt or 1-in-350; scan 11; recursive tree chop; mobGriefing), beaver-buddy seek | same structure, uses `BlockTags.LOGS/PLANKS/HAY_BLOCK` | PORTED |
| Drops | 2–6 raw porkchops | `beaver.json`: 1–3 leather | DIVERGENT |
| Spawning | forest/jungle w10 2–4; y 50–100, ground = dirt/grass/tallgrass/oak leaves | `add_overworld_creatures.json` w8 1–2; same `checkSpawnRules` conditions ✓ | PARTIAL (weights/biomes) |
| Sounds | none/`scorpion_hit`/`cryo_death`, vol 0.4; baby pitch 1.5 | identical | PORTED |
| Misc | breed wheat/crystal apple | crystal apple only | PARTIAL |

**Verdict: PARTIAL** — faithful behavior; drops swapped porkchops→leather.

---

### Bee

| Feature | Original | Port (`entity/EntityBee.java`) | Status |
|---|---|---|---|
| Stats | HP 80, atk 12, def 5 (`OreSpawnMain.java:6466`), speed 0.32, XP 25, size 1.5×2.5 | HP 30, atk 6, armor 0, XP 25, size 0.5×0.5 | DIVERGENT (all combat stats + size) |
| AI | No goals; custom flight (stuck count, random air target, seek Player/Villager/Girlfriend/Boyfriend); 1-in-4 self-damage in water | same flight logic; targets Player/Villager only; water self-damage ✓ | PARTIAL |
| Attack | Melee + Hunger (`field_76436_u`) 50t, 1-in-3 | Melee + **POISON** 50t, 1-in-3 | DIVERGENT (effect swapped) |
| Drops | 2–11 each: gold nuggets, butter candy, dandelion, sugar | `bee.json` (gunpowder 2–5, sugar 1–3, butter candy 0–2@50%) + custom 2–11 spider eyes & 2–11 red mushrooms | DIVERGENT |
| Spawning | forest/taiga biomes w2–5 1–5; day/clear-air/y>50 or Utopia | `add_overworld_monsters.json` w8 1–3 | DIVERGENT |
| Sounds | `beebuzz`/`dragonfly_hurt`/`alo_death`, vol 0.25 | identical | PORTED |

**Verdict: DIVERGENT** — combat power cut, effect type swapped, drops unrecognizable.

---

### Bertha (Item — Big Bertha sword)

| Feature | Original (`Bertha.java`) | Port (`item/Bertha.java`) | Status |
|---|---|---|---|
| Stats | Damage 496 (`OreSpawnMain.java` `bertha_stats`; Royal 746, Hammy 82), durability 9000 | Sword tier values differ; kills tracked via `ModDataComponents.BERTHA_KILLS` (new) | DIVERGENT |
| Abilities | `onEntitySwing` spawns `BerthaHit`; enchants applied in `onUsingTick`/`func_77622_d` | swing spawns `BerthaHit` ✓; enchants via `OreSpawnEnchantHelper.inventoryTick` | PARTIAL |
| PvP guard | blocks hitting players/tamed only when `big_bertha_pvp == 0` (config) | blocks **unconditionally** | DIVERGENT |

**Verdict: DIVERGENT** — config-gated PvP became hardcoded; projectile damage values changed (below).

---

### BerthaHit (Projectile)

| Feature | Original (`BerthaHit.java`) | Port (`entity/BerthaHit.java`) | Status |
|---|---|---|---|
| Damage | type 0 (Bertha): 496; type 2 (Royal): 746; type 3 (Hammy): 82 | type 0: 250; type 2: 150; type 3: 100 | DIVERGENT (–50% to –80%) |
| Range | distSq < 81 / 101 / 64 per type | single `CLOSE_RANGE_DAMAGE_SQ = 100` for all | DIVERGENT |
| Effects | type 0 ignite `func_70015_d(10)` = 10 s; type 3 explosion r1.5 or 2.1 + mobGriefing | `igniteForSeconds(10)` ✓; explosion r2.1 only (1.5 branch lost) + mobGriefing ✓ | PARTIAL (explosion radius branch lost) |
| KB | 2.25/0.35, 1.5/0.25, 1.25/0.65 | identical per type | PORTED |
| PvP | skips players/tamed if `big_bertha_pvp==0` | skips unconditionally | DIVERGENT |

**Verdict: DIVERGENT** — damage numbers rewritten wholesale (496/746/82 → 250/150/100).

---

### BetterFireball

| Feature | Original | Port | Status |
|---|---|---|---|
| Core | extends `EntityFireball`; small=0.3125f AABB; power 1/2/4; 600-tick lifetime | extends `LargeFireball`; small 0.25; power 1/2/4 ✓; 600t ✓ (`entity/BetterFireball.java`) | PORTED |
| Hit | dmg 10 (5 small), `func_70015_d(5)` = 5 s ignite; halves HP of mobs with w·h>30 | dmg same, `igniteForSeconds(5)` ✓; HP-halving ✓ | PORTED |
| Immunities | `notme` skips Player/Dragon/Mothra; Robots/GodzillaHead/Royalty/Kraken/PitchBlack exempt from halving | `notme` flag declared but **never read**; exemption list absent | MISSING (immunities) |

**Verdict: DIVERGENT** — boss/robot immunity list dropped (HP-halving now hits Kraken, Royalty, GodzillaHead, etc.).

---

### Boyfriend

| Feature | Original (`Boyfriend.java`) | Port (`entity/Boyfriend.java`) | Status |
|---|---|---|---|
| Stats | HP 80 (`Boyfriend.java:492`), atk 8 (`:176`), speed 0.3, XP 0 (`:149`), size 0.5×1.6, fire-immune (`:123`), armor clamp 8–23 from worn gear (`func_70658_aO:179-193`) | HP 80, atk 8, speed 0.3 (`Boyfriend.java:84-89`); size 0.6×1.8 (`ModEntities.java:391`); no fire immunity, no min-armor-8 floor | PARTIAL (armor floor + fire immunity lost) |
| AI goals | FollowOwner(1, 1.4/12/1.5), Tempt(2, **cooked beef**), **EntityAIArrowAttack(4, 1.25, 20t, 10.0f)** ranged, Swim(5), Panic(6), Watch(7), Wander(8), LookIdle(9), **OpenDoor(10), MoveIndoors(11)**; targets: Creeper@2, IMob@3, **Jealousy goals @4/5** (`Boyfriend.java:127-148`) | FollowOwner(1) ✓, Tempt(2, **DIAMOND**), **MeleeAttackGoal(4)**, Float(5), LookAt(7), Stroll(8), RandomLook(9); targets OwnerHurtBy/OwnerHurt/Nearest<Monster> (`Boyfriend.java:70-82`) | DIVERGENT (ranged→melee; Panic/OpenDoor/MoveIndoors/Jealousy missing; tempt item changed) |
| Attack | Ranged: `UltimateArrow` (2.0f, 1-in-4 crit, punch/flame enchant aware) when holding UltimateBow, else throws `Shoes` projectile (speed 1.8, spread 4.0) (`attackEntityWithRangedAttack:874-907`); melee with held-item enchant math + 25t cooldown + `b_fight` sound (`func_70629_bd:239-289`) | plain vanilla `MeleeAttackGoal`; no Shoes, no UltimateBow logic, no fight/taunt/woohoo sounds | MISSING (entire weapon/ranged system) |
| Drops | tamed: 2–6 poppies; always 10–35 game controllers; all equipped gear (`func_70628_a:839-872`) | `boyfriend.json`: game controller 10–36; vanilla equipment drops; no poppies | PARTIAL |
| Spawning | creature spawns: beach w30 8–15, forest w10 3–6, hills w8 2–5, plains w5 2–3, river w10, stone beach w10, birch w5, roofed w5, mega taiga w5, taiga w5, savanna w2, savanna plateau w2 (`OreSpawnMain.java:4588-4599`) | `companion_boyfriend.json`: `#minecraft:is_overworld` w4 1–2 | DIVERGENT |
| Sounds | situational ambient: `b_water`/`b_thunder`/`b_rain`/`b_dark`/`b_hurt`/`b_happy` (+`bb_happy` bro_mode); hurt `b_ow`; death `b_death_boyfriend`/`b_death_single`; vol 0.3; pitch (voice−5)·0.02+1 (`func_70639_aQ:768-812`) | only `b_hurt`/`b_happy` ambient branch; `b_ow`; both death sounds ✓; vol 0.3 ✓; pitch ✓ (`Boyfriend.java:268-307`) | PARTIAL (weather/water/dark lines missing) |
| Misc | Tame: cooked beef **or Peacock item** 1-in-3; untame: dead bush; voice off: Ruby; voice on: Amethyst; skin cycle: leather/peacock feather; **wet-skin system** (18 swimshorts textures, wet_count 500); diamond-in-hand = guard mode; health report chat; OreSpawn armor auto-equip; fall dmg cap 3; hurt cap 10; cactus-immune; Elevator passenger snap; FrogPrince textures | Tame cooked beef 1-in-3 ✓; skin cycle via **DANDELION** (dry only); sit toggle on empty hand; armor/weapon equip ✓ simplified; fall cap 3 ✓; hurt cap 10 + cactus ✓; `BOYFRIEND_BRO_MODE` config (new `wantsToAttack` override); auto-heal 1HP/150t ✓; **no wet skins, no untame, no voice toggle, no health report, no prince** | PARTIAL |

**Verdict: PARTIAL** — stats match but ranged combat, wet-skin system, and most item interactions are missing.

---

### Brutalfly

| Feature | Original (`Brutalfly.java`) | Port (`entity/EntityBrutalfly.java`) | Status |
|---|---|---|---|
| Stats | HP 110, atk 10, def 6 (`OreSpawnMain.java:6470`), speed 0.35, XP 100 (`Brutalfly.java:57`), size 5.0×2.0 (`:55`), fire-immune (`:58`) | HP 500, atk 18, armor 0, speed 0.35, XP 100 (`EntityBrutalfly.java:40-45`); size 1.2×1.2 (`ModEntities.java:192`); not fire-immune | DIVERGENT (HP 110→500, atk 10→18, armor & fire immunity & size lost) |
| AI | Flight AI in `func_70619_bc:151-253`: stuck counter, terrain-descent scan (drops target if >10 above ground), 1-in-6 player hunt 30×20×30, 1-in-3 fallback mob hunt; excludes Brutalfly/Mothra/Vortex/ignoreables | `customServerAiStep:100-174` same flight skeleton; terrain-descent scan **missing**; nearest-player 30 + mob fallback ✓; Mothra/Vortex exclusions missing | PARTIAL |
| Attack | **Ranged fireballs** (`attackWithSomething:369-406`): Easy=SmallFireball, Normal=50/50 Small/BetterFireball, Hard=BetterFireball; +1 HP self-heal per shot; shoot odds 1-in-3 (1-in-2 hard); melee only within 25 distSq | **No projectiles at all** — `doHurtTarget` melee at distSq<25 (`EntityBrutalfly.java:141-158`) | MISSING (ranged attack) |
| Drops | 53 gold nuggets + spawns **20 Butterflies** + 20 largeexplode particles (`func_70628_a:339-353`) | `brutalfly.json` gunpowder 10–53 + custom 53 **spider eyes** (`:197-202`); no butterflies | DIVERGENT |
| Spawning | ambient w2 1–1 in ExtremeHillsPlus/SavannaPlateau/MesaPlateau (`OreSpawnMain.java:4839-4841`); rules: spawner tag, y≥70, dark, night, 4×3×10 clear air, no other within 64 (`func_70601_bi:290-329`) | `add_overworld_monsters.json` w3 1–1; **no checkSpawnRules** | DIVERGENT |
| Sounds | no living/hurt; death `random.explode`; `MothraWings` every 30t; vol 1.5 | hurt = GENERIC_HURT (added); death GENERIC_EXPLODE ✓; `mothrawings`/30t ✓; vol 1.5 ✓ | PARTIAL |
| Misc | Brutalfly-vs-Brutalfly immune ✓; retarget to attacker pos ✓; heal 1HP/100t ✓; no fall dmg ✓ | all ✓ | PORTED |

**Verdict: DIVERGENT** — signature fireball barrage gone, HP ×4.5, butterfly death-burst gone.

---

### Camarasaurus

| Feature | Original (`Camarasaurus.java`) | Port (`entity/Camarasaurus.java`) | Status |
|---|---|---|---|
| Stats | HP 20 (`:212`), atk 1, speed 0.2, XP 5 (`:52`), size 0.5×1.2 (`:47`) | HP 20, atk 1, speed 0.2, XP 5 (`Camarasaurus.java:76-81`); size 1.4×2.6 (`ModEntities.java:395`) | PARTIAL (size grew 0.5×1.2→1.4×2.6) |
| AI | Swim0, Mate1, FollowOwner2(2.0/10/2), Avoid(Mob)3, Tempt(apple)4, Panic5, Watch6, MyEntityAIWander7, LookIdle8, **MoveIndoors9** (`:53-63`) | Float0, Breed1, FollowOwner2 ✓, Avoid3 ✓, Tempt(APPLE)4 ✓, Panic5 ✓, LookAt6 ✓, Wander7 ✓, RandomLook8; + OwnerHurtBy/OwnerHurt/HurtBy targets (new) (`:60-74`) | PARTIAL (MoveIndoors missing; target goals added) |
| Graze | eats **leaves/vines/tallgrass/cactus/double_plant** (`scan_it:105-166`), 1-in-20 hurt / 1-in-250 idle, eat<12 distSq, heal 1, burp, mobGriefing (`func_70629_bd:168-201`) | eats **wheat/carrots/potatoes/short_grass/tall_grass** (`isEdibleBlock:99-103`); same trigger odds/heal/burp/mobGriefing (`:141-184`) | DIVERGENT (food blocks: tree browse → crop raider) |
| Taming | apple, 1-in-2 (`func_70085_c:219-278`); name tag; sit toggle | apple 1-in-2 ✓; name tag ✓; sit toggle ✓; **adds player riding** (`mobInteract:225-233`, `tickRidden`, riding speed ×1.5) — not in original | PARTIAL (riding invented) |
| Drops | tamed only: 2–6 poppies (`func_70628_a:303-312`) | `camarasaurus.json`: bone 3–6 always | DIVERGENT |
| Spawning | none in OreSpawnMain (mining-dim chunk providers); rules y≥50 + day (`func_70601_bi:78-83`) | `add_overworld_creatures.json` w2 1–1 + `companion_camarasaurus__*` jungle/savanna w1 1–1; no rules | DIVERGENT |
| Sounds | none/`cryo_hurt`/`cryo_death` vol 0.4, baby pitch 1.5 (`:280-316`) | identical (`:296-322`) | PORTED |
| Misc | fall dmg −3 cap 2 ✓ | ✓ (`causeFallDamage:90-97`) | PORTED |

**Verdict: PARTIAL** — solid skeleton; graze diet & drops diverge, riding feature invented.

---

### Cassowary

| Feature | Original (`Cassowary.java`) | Port (`entity/Cassowary.java`) | Status |
|---|---|---|---|
| Stats | HP 10 (`:74`), atk 8 (`:53`), speed 0.25, XP 5 (`:36`), size 0.5×1.2 | identical; size ✓ (`ModEntities.java:282`) | PORTED |
| AI | Swim0, Mate1, Avoid(Mob)2, Avoid(Player)3, Panic4, Watch(EntityLiving,12)5, MyEntityAIWander6, LookIdle7; target-clear 1-in-200 (`:38-45,106-111`) | Float0, Breed1, Avoid(Monster)2, Avoid(Player)3, Panic4, LookAt(Mob,12)5, Wander6, RandomLook7; target-clear ✓ (`:37-46,62-67`) | PORTED |
| Drops | 2–4 raw chicken (`func_70628_a:97-104`) | `cassowary.json` chicken 2–4 **plus** custom 2–4 **feathers** (`dropCustomDeathLoot:92-98`) | PARTIAL (feathers added = double source) |
| Spawning | ambient w5–15 1–5 in hills/iceMtn/megaTaigaHills/birch/roofed/megaTaiga/savanna biomes (`OreSpawnMain.java:4671-4679`); day-only rule (`func_70601_bi:113-115`) | `add_overworld_creatures.json` w5 1–2; no day rule | PARTIAL |
| Sounds | none/`duck_hurt`/`duck_hurt` vol 0.4 | identical (`:70-89`) | PORTED |
| Misc | breed crystal apple ✓ | ✓ (`isFood:101-103`) | PORTED |

**Verdict: PARTIAL** — accurate port except doubled drops and lost spawn conditions.

---

### CaterKiller

| Feature | Original (`CaterKiller.java`) | Port (`entity/EntityCaterKiller.java`) | Status |
|---|---|---|---|
| Stats | HP 450, atk 32, def 19 (`OreSpawnMain.java:6481`), speed 0.35, XP 200 (`:60`), size 2.9×4.6 (1.45×2.3 PlayNicely) (`:54-58`) | HP 350, atk 20, armor 0, follow 40, XP 200 (`EntityCaterKiller.java:82-88`); size 1.5×1.0 (`ModEntities.java:196`) | DIVERGENT (450→350, 32→20, 19→0, size shrunk) |
| AI | Swim0, MoveThroughVillage1, WanderALot(2,16), Watch3, LookIdle4; HurtBy (`:63-68`); custom loop `func_70619_bc:430-531`: 1-in-4 target tick, web-place near fleeing target (1-in-4), web-self-clear on collision, tree-eat heal | Float0, `BugMeleeAttackGoal`1, WanderALot2, LookAt3, RandomLook4; HurtBy + NearestAttackable<Player> (`:71-80`); cobweb under chased player throttled 40t + mobGriefing (`:175-209`); **web-self-clear missing** | PARTIAL |
| Special: metamorphosis | If damaged for >2400t: spawns **1 Brutalfly + 10 Butterflies**, explode sound, removes self (`:438-448`); on death `func_70628_a` spawns **25 Butterflies** (no Brutalfly) | damaged 2400t → silently `discard()` (no spawns) (`:161-169`); **on ANY death** spawns 1 Brutalfly + 3–5 Butterflies (`die():253-274`) | DIVERGENT (logic inverted: death now always produces a Brutalfly; the timed transform produces nothing) |
| Tree-eat | scan ≤13 blocks for leaves/vines/logs/mod leaves, path to it, eat at distSq<81, heal **2.0**, 1-in-8 hurt / 1-in-30 idle, mobGriefing (`:502-530`) | random 5×4×5 sample 1/s, heal **5 (leaf) / 10 (log)**, no pathing (`tryEatNearbyTreeBlock:218-242`); + new random heal 2.0 @1-in-150 | DIVERGENT (heal values invented; comments claim 1.7.10 parity falsely) |
| Attack | melee 32, KB 1.2/0.1 (×2 player); swing odds 1-in-3 / 1-in-4 at range <(5+w/2) | melee 20 via goal; KB ✓ (`doHurtTarget:132-144`) | PARTIAL |
| Drops | CaterKillerJaw 1, Item Frame 1, 10 leather, 6 beef, 1–5 rolls 13/20 chance of ultimate sword/ruby/diamond block/enchanted ruby gear/ultimate bow (`func_70628_a:160-328`) | `cater_killer.json`: jaw 1, name_tag 1, leather 6–10, slime 3–6, 1–5 rolls (13 items + 7/20 empty); **plus** custom name tag + 10 leather + 6 bones (`dropCustomDeathLoot:277-286`) → double | DIVERGENT + double-drop (beef→slime/bones, diamond block→emerald block) |
| Spawning | ambient forest/jungle/hills/jungleEdge/birch/roofed/megaTaiga/taiga w2–10 1–2 (`OreSpawnMain.java:4746-4754`); rules: day, y≥50, 1-in-10 dice, leaf/air clearance, none within 48 (`func_70601_bi:585-624`) | `hostile_cater_killer__*` forest/jungle/taiga/badlands w4 1–2 + `__direct`; no rules | DIVERGENT |
| Sounds | `caterkiller_living` (1-in-3)/`caterkiller_hit`/`caterkiller_death`, vol 1.5 | identical (`:104-129`) | PORTED |

**Verdict: DIVERGENT** — stats cut, metamorphosis semantics inverted, double drops.

---

### CaveFisher

| Feature | Original (`CaveFisher.java`) | Port (`entity/CaveFisher.java`) | Status |
|---|---|---|---|
| Stats | HP 10, atk 4, def 4 (`OreSpawnMain.java:6511`), speed 0.2, XP 10 (`:46`), size 1.35×0.75 | HP 25, atk 6, armor 0, follow 16, XP 10 (`CaveFisher.java:64-70`); size 0.8×0.8 (`ModEntities.java:48`) | DIVERGENT (HP 10→25, atk 4→6, armor 4→0) |
| AI | Swim0, WanderALot(1,14), Watch2, LookIdle3; HurtBy (`:51-55`); 1-in-8 scan in `func_70619_bc:163-183`, attack at distSq<8 with ~26% swing | Float0, BugMeleeAttackGoal1, **CaveFisherAmbushGoal2 (new ceiling-ambush)**, WanderALot3, LookAt4, RandomLook5; HurtBy + NearestAttackable<Player> (`:49-62`) | PARTIAL (ambush invented) |
| Targets | **hunts passive mobs**: excludes CaveFisher/EnderReaper/EnderKnight/**all EntityMob**; attacks players & animals (`isSuitableTarget:193-228`) | targets Player (goal) — passive-hunter behavior absent | DIVERGENT (target inversion) |
| Drops | 1-in-6 each: gold nugget / uranium nugget / titanium nugget, else nothing (`func_146068_u:141-153`) | `cave_fisher.json`: string 2–5 + spider eye 0–1 | DIVERGENT |
| Spawning | no addSpawn (dungeon/cave dims); rules: dark + y≤50 (`func_70601_bi:256-275`) | `hostile_cave_fisher.json` `#is_overworld` w12 1–2; `checkSpawnRules` y≤50 only — darkness check missing (`:121-123`) | PARTIAL |
| Sounds | none/`cryo_hurt`/`cryo_death` vol 1.5 | identical (`:97-118`) | PORTED |
| Misc | cactus-immune (`func_70097_a:185-191`) | ✓ + ambush-abort on hurt (`:86-95`) | PORTED |

**Verdict: DIVERGENT** — stats changed, prey selection inverted (was a passive-mob predator), drops replaced.

---

### Cephadrome

| Feature | Original (`Cephadrome.java`) | Port (`entity/Cephadrome.java`) | Status |
|---|---|---|---|
| Stats | HP 300 (`:153`), atk 70 (`:104`), armor 16 (`:172`), speed 0.25, XP 200 (`:75`), size 2.5×2.25 (`:73`) | HP 300, atk 70, armor 16, follow 32, XP 200 (`Cephadrome.java:95-102`); size 1.5×1.5 (`ModEntities.java:605`) | PORTED (size shrunk) |
| AI | Swim0, WanderALot(1,16), Watch(9)2, LookIdle3; HurtBy (`:78-82`); 1-in-7 hunt in `func_70619_bc:462-513` (speed 1.7, melee range 6+w/2, Kraken horizontal-range special); heal 2 @1-in-100 | Float0, TemptGoal(porkchop)1 (new), WanderALot2, LookAt3, RandomLook4; HurtBy (`:81-93`); same 1-in-7 hunt loop + heal (`:194-225`) | PARTIAL |
| Targets | attacks Monsters, **Mothra, untamed Leon/GammaMetroid/WaterDragon, EnderDragon (70 dmg direct head/body part hit), Kraken ×1.5 dmg**; players only if hit_by_player/badmood/shouldattack (`isSuitableTarget:515-573`, `func_70652_k:404-432`) | Monsters ✓; **Mothra/Leon/GammaMetroid/WaterDragon explicitly EXCLUDED** (`:232`); no EnderDragon or Kraken special damage; player gating ✓ + tamed-skip (`:227-242`) | DIVERGENT (target list inverted) |
| Riding | **Flyable mount**: feed meat → mount; full custom flight physics, flyup key, wing sound every 22t while ridden, obstruction climb (`func_70636_d:666-850`, `func_70085_c:872-908`) | **No riding at all**; replaced by invented porkchop "taming" flag (`DATA_TAMED`, `mobInteract:268-307`) | MISSING (signature mount feature) |
| Attack | melee 70, KB 2.5/0.35 (×2 player) | identical (`doHurtTarget:158-173`) | PORTED |
| Drops | 4–9 uranium nuggets + 4–9 titanium nuggets + 1–5 rolls (ruby gear/diamond/ThunderStaff/enchants, 12-in-20 ruby) (`func_70628_a:229-398`) | `cephadrome.json` (bone 3–8, gunpowder 2–5, diamond 0–2@25%) + custom 4–9 uranium + 4–9 titanium nuggets (`:255-265`) — gear rolls missing | PARTIAL |
| Spawning | ambient w1 1–1 icePlains + coldTaiga (`OreSpawnMain.java:4774-4775`); rules: day, y≥50, clear air, none within 16 (`func_70601_bi:593-630`) | **no biome modifier** (MISC category, `ModEntities.java:605`) — cannot spawn naturally | MISSING |
| Sounds | `MothraWings` ambient (1-in-6 when not ridden)/`alo_hurt`/`alo_death`, vol 1.5; wing-beat loop while flying | ambient/hurt/death ✓ vol 1.5 (`:132-155`); flight wing loop n/a | PORTED (partial) |
| Misc | hurt_timer 25 i-frames, cactus-immune, NBT moods ✓ | ✓ (`:176-191`, `:328-347`) | PORTED |

**Verdict: DIVERGENT** — the flying-mount mechanic (its defining feature) is missing; targeting inverted; natural spawns gone.

---

### Chipmunk

| Feature | Original (`Chipmunk.java`) | Port (`entity/Chipmunk.java`) | Status |
|---|---|---|---|
| Stats | HP 5 (`:125`), atk 1, speed 0.38, XP 5 (`:51`), size 0.35×0.35 | identical (`Chipmunk.java:70-75`; `ModEntities.java:286`) | PORTED |
| AI | Swim0, Mate1, FollowOwner2, MyAvoid(Mob)3, **Tempt(apple)4**, Panic5, Avoid(Player)6, Watch(Player)7, Watch(Living)8, WanderALot(10)9, LookIdle10, MoveIndoors11 (`:52-63`); dirt/farmland dig 1-in-600 + mobGriefing; heal 1-in-250 (`func_70629_bd:99-114`) | Float0, Breed1, FollowOwner2, Avoid(Monster)3, **Tempt(WHEAT)4**, Panic5, Avoid(Player)6, LookAt7, LookAt(Mob)8, WanderALot9, RandomLook10 (`:56-68`); dig+heal ✓ (`:88-109`) | PARTIAL (MoveIndoors missing; tempt apple→wheat) |
| Taming | **apple** 1-in-2; untame **dead bush**; name tag; sit toggle (`func_70085_c:132-206`) | **wheat** 1-in-2 (`:119-142`); untame **glass** (`:144-158`); name tag ✓; sit ✓ | DIVERGENT (both items changed) |
| Drops | untamed: wheat; tamed: 2–6 poppies (`func_146068_u:227`, `func_70628_a:231-242`) | `chipmunk.json`: **empty pools** — drops nothing | MISSING |
| Spawning | ambient forest w8 3–6, hills w5, jungle w4, plains w2, birch/roofed/taiga etc (`OreSpawnMain.java:4757-4765`); rules y≥50 & ≤2 buddies (`:248-258`) | `add_overworld_creatures.json` w12 1–3; `checkSpawnRules` y≥50 & ≤2 buddies ✓ (`:216-220`) | PORTED (weights differ) |
| Sounds | none/`scorpion_hit`/`cryo_death` vol 0.4, baby pitch 1.5 | identical (`:182-208`) | PORTED |
| Misc | parent `EntityCannonFodder` (jukebox dance behavior) | extends `TamableAnimal` directly — dance behavior lost | PARTIAL |

**Verdict: PARTIAL** — faithful except taming items swapped and all drops missing.

---

### CliffRacer

| Feature | Original (`CliffRacer.java`) | Port (`entity/EntityCliffRacer.java`) | Status |
|---|---|---|---|
| Stats | HP 5 (`:73`), atk 1, speed 0.33, XP 5 (`:28`), size 0.75×0.5 | identical attrs (`:33-38`); size 0.8×0.8 (`ModEntities.java:350`) | PORTED |
| AI | flight: retarget 1-in-300 or distSq<2.1, target must be air **and pass LOS check** (`canSeeTarget`), motion 0.4/0.7 dampers, yaw/6 (`func_70619_bc:89-129`); y×0.6 damp per tick | same retarget odds/motion/yaw (`:53-98`); air check only, **LOS check dropped**; y×0.6 ✓ (`:46-50`) | PORTED (minor) |
| Drops | 1-in-8 each: raw chicken / uranium nugget / titanium nugget (`func_146068_u:149-161`) | `cliff_racer.json`: feather 1–3 | DIVERGENT |
| Spawning | rules y≥50 (`:145-147`); spawned via dimension chunk providers | `add_overworld_creatures.json` w5 1–2 + `dim_chaos_locals` w30 3–6 + `dim_islands_locals` w20 3–6; `checkSpawnRules` y≥50 ✓ (`:115-118`) | PORTED |
| Sounds | `cliffracer` ambient, no hurt/death, vol 0.45 | identical (`:120-140`) | PORTED |
| Misc | no fall damage ✓, pushable ✓ | ✓ | PORTED |

**Verdict: PARTIAL** — accurate port, only the drop table diverges.

---

### CloudShark

| Feature | Original (`CloudShark.java`) | Port (`entity/CloudShark.java`) | Status |
|---|---|---|---|
| Stats | HP 15, atk 6, def 5 (`OreSpawnMain.java:6512`), speed 0.3, XP 5 (`:42`), size 1.0×0.75 | HP 20, atk 6, armor 0 (`CloudShark.java:42-47`); size 1.5×1.0 (`ModEntities.java:52`) | DIVERGENT (HP 15→20, armor 5→0) |
| AI | flight w/ altitude band 120–140 (±2 bias), retarget 1-in-300 or <2.1, LOS check; 1-in-9 hunt scan (`func_70619_bc:116-173`) | identical flight incl. altitude band (`:77-131`); 1-in-9 player-only hunt | PARTIAL |
| Targets | Butterfly, Cockateil, Mosquito, Firefly, GoldFish, CliffRacer, Player (`isSuitableTarget:202-243`) | **Player only** (`:109-117`) | PARTIAL (prey list missing) |
| Attack | melee 6 at distSq<9 | ✓ | PORTED |
| Drops | 1-in-3 each: paper / string / bone (`func_146068_u:263-275`) | `cloud_shark.json`: cod 3–8 | DIVERGENT |
| Spawning | `func_70601_bi` always true; registered in sky-dim chunk provider | `add_sky_spawns.json` `#is_overworld` w2 1–1 + `dim_chaos/islands_locals`; `checkSpawnRules` true ✓ | PORTED |
| Sounds | `splash` ambient / `little_splat` / `big_splat`, vol 0.25 | GENERIC_SPLASH / little_splat / big_splat, 0.25 | PORTED |
| Misc | retarget to attacker on hurt ✓; despawns at day (orig `func_70692_ba` despawn only at night... inverse persistent) | hurt-retarget ✓; vanilla despawn | PARTIAL |

**Verdict: DIVERGENT** — stats and drops changed; prey ecosystem (eats birds/fish/insects) gone.

---

### Cockateil

| Feature | Original (`Cockateil.java`) | Port (`entity/Cockateil.java`) | Status |
|---|---|---|---|
| Stats | HP 2 (`:128`), atk 1, speed 0.33, XP 2 (`:44`), size 0.5×0.5 | identical (`:47-52`); size 0.4×0.4 (`ModEntities.java:290`) | PORTED |
| Bird type | random 0–5 at spawn (`func_70088_a:82-86`) → 6 textures | DATA_BIRD_TYPE defaults **0, never randomized** (`defineSynchedData:55-58`) — every bird is type 0 | DIVERGENT (variant system broken) |
| AI | flight retarget when stuck>40/1-in-250/<4.1; LOS+air check; `flyup` hook; dim-4 stayup bias; yaw/3 turn (`func_70619_bc:170-222`) | same retarget odds (`:101-138`); air/LOS validation gone, **yaw never updated** (no setYRot), flyup/dim hooks gone | PARTIAL |
| Drops | feather; if birdtype==5 & killedByPlayer & 1-in-3 → **Ruby** (`func_146068_u:242-248`) | `cockateil.json`: feather 1–2 + ruby @ killed_by_player && 33% — **not gated on type 5** | DIVERGENT (ruby drops from any bird) |
| Spawning | ambient in 17 biomes w5–35 (`OreSpawnMain.java:4811-4826`); rules: day & (dim4 or y≥50) (`func_70601_bi:232-240`) | `add_overworld_creatures.json` w8 1–3; **no checkSpawnRules** | PARTIAL |
| Sounds | `birds` ambient when day & not raining / `duck_hurt` ×2, vol 0.55 | identical (`:140-165`) | PORTED |
| Misc | no fall damage ✓; killedByPlayer flag ✓; NBT BirdType ✓ | ✓ (`:86-98,178-189`) | PORTED |

**Verdict: PARTIAL** — skin variant randomization broken, ruby drop gating lost, spawn rules dropped.

---

### Coin

| Feature | Original (`Coin.java`) | Port (`entity/Coin.java`) | Status |
|---|---|---|---|
| Stats | HP 1 (`:54`), atk 0, speed 0.0, XP 10 (`:28`), size 1.5×1.5 | HP 1, atk 0, speed 0, XP 10 (`:31-36`); size 0.4×0.4 (`ModEntities.java:298`) | PORTED (size shrunk) |
| AI | LookIdle only (`:30`) | RandomLookAround only (`:27-29`) | PORTED |
| Drops | 1 roll-of-10: diamond / uranium nugget / titanium nugget / emerald / emerald axe-shovel-pickaxe-hoe / **CoinEgg** / emerald sword default (`func_70628_a:98-129`) | `coin.json`: gold ingot 1–3 | DIVERGENT (entire jackpot table replaced) |
| Spawning | ambient w2 1–1 in taiga/forest/jungle/birch/coldTaigaHills/megaTaiga (`OreSpawnMain.java:4942-4947`); rules: day, y≥50, none within 20 (`func_70601_bi:138-148`) | `dim_village_locals.json` w2 1–1; **MobCategory.MISC** (`ModEntities.java:298`) so natural spawn cycle never picks it | MISSING (natural overworld spawns) |
| Sounds | all null, vol 1.0 | all null | PORTED |
| Misc | non-interactable ✓ | `mobInteract` PASS ✓ | PORTED |

**Verdict: PARTIAL** — entity works but the loot-piñata table and overworld spawning are gone.

---

### Crab

| Feature | Original (`Crab.java`) | Port (`entity/Crab.java`) | Status |
|---|---|---|---|
| Stats | HP = `PitchBlack_stats.health(250)` × scale (`mygetMaxHealth:136-138`, `OreSpawnMain.java:6517`), atk 24×scale (`OreSpawnMain.java:6524` Crab_stats=180/24/16), armor 16+2·scale, XP 400×scale, size 3.75×3.5·scale, speed 0.55 (0.95 in water) | HP fixed 100, atk 10×scale, armor 6, XP fixed 150, speed ✓ water-boost ✓ (`Crab.java:38-42,59-66,96-101`); size 0.8×0.6 fixed (`ModEntities.java:56`) | DIVERGENT (every number; scale no longer affects HP/XP/size) |
| Scale | random per-spawn: 0.25 base, 1-in-4→0.5, 1-in-8→1.0 (`func_70088_a:74-98`); spawner crabs 0.35 | DATA_SCALE constant 25 (0.25), **never randomized** (`defineSynchedData:69-73`) | DIVERGENT (giant crabs never occur) |
| AI | Swim0, WanderALot(1,16), Watch(10)2, Watch(Living,8)3, LookIdle4; HurtBy (`:59-64`); water-seek scan ≤12 blocks → path 1.33, else **dry-out −1 HP @1-in-100 and discard at 0** (`func_70619_bc:306-339`); 1-in-5 hunt, swing ~45%, water heal 4×scale 1-in-120 + splash sound | Float0, WaterAvoidingStroll1(!), LookAt2, RandomLook3; HurtBy (`:51-57`); **water-seek & dry-out missing** (avoids water instead); hunt 1-in-5 + 1-in-4 swing ✓; water heal ✓ no splash sound (`:139-170`) | DIVERGENT (water ecology inverted: water-seeker → water-avoider) |
| Attack | melee 24×scale, KB 1.15·scale / 0.48·scale (×2 player); attack sounds `scorpion_attack`/`scorpion_living` | melee 10×scale, KB ✓ scaled (`doHurtTarget:104-119`); attack sounds missing | DIVERGENT |
| Targets | Player, Mobs, Lizard, RubberDucky, Villager, Girlfriend, Boyfriend (`isSuitableTarget:379-418`) | Player + Monster only (`:182-188`) | PARTIAL |
| Drops | 4–11 raw crab meat ×scale (`func_70628_a:190-199`) | `crab.json` raw_crab_meat 4–12 (unscaled) | PORTED (≈, scale lost) |
| Spawning | waterCreature ocean w2 3–6, swamp w1 3–6, stoneBeach w1 2–4 (`OreSpawnMain.java:4858-4860`); rules: day, y≥50, dim5 throttle (`func_70601_bi:456-486`) | `add_ocean_spawns.json` w8 1–3; no rules | PARTIAL |
| Sounds | none/`leaves_hit`/none, vol 0.75, pitch 2.0−0.3/scale | leaves_hit ✓ vol 0.75 ✓; pitch formula missing (`:190-210`) | PARTIAL |
| Misc | hurt_timer 8, cactus-immune, crab-vs-crab immune ✓ | all ✓ (`:121-136`) | PORTED |

**Verdict: DIVERGENT** — size/HP scaling system gutted, water-seeking inverted, damage numbers cut.

---

### CreepingHorror

| Feature | Original (`CreepingHorror.java`) | Port (`entity/CreepingHorror.java`) | Status |
|---|---|---|---|
| Stats | HP 10, atk 3, def 2 (`OreSpawnMain.java:6513`), speed 0.25, XP 5 (`:49`), size 0.75×0.5 | HP 20, atk 6, armor 0, follow 16, XP 5 (`:32-37,51-57`); size 1.5×1.5 (`ModEntities.java:60`) | DIVERGENT (HP ×2, atk ×2, size ×2) |
| AI | Swim0, Panic1(1.35), MoveThroughVillage2, WanderALot(10)3, Watch4, LookIdle5; HurtBy (`:51-57`); hunt 1-in-5, swing at distSq<5 odds 1-in-12/1-in-14 (`func_70619_bc:130-145`); daytime self-despawn 1-in-500 before tick 11000 (`func_70071_h_:84-97`) | Float0, Panic1 ✓, Stroll2, LookAt3, RandomLook4; HurtBy; same hunt loop ✓ (`:100-118`); same despawn ✓ (`:64-74`) | PARTIAL (MoveThroughVillage missing) |
| Targets | excludes self-kind, RockBase, EnderReaper, LeafMonster, Dragon, TerribleTerror, LurkingTerror, PitchBlack, Firefly, Island(s); LOS required (`:147-200`) | excludes self-kind only; **no LOS check** (`:130-135`) | PARTIAL |
| Drops | 1 of: rotten flesh / bone / string (`func_146068_u:119-128`) | `creeping_horror.json`: rotten flesh 2–5 + bone 1–3 | DIVERGENT (quantity inflated) |
| Spawning | rules: dark, night, and (dim6 or y≤15) (`func_70601_bi:220-228`) | `add_overworld_monsters.json` w3 1–1; **no checkSpawnRules** — spawns anywhere monsters can | DIVERGENT (deep-cave gating lost) |
| Sounds | `creepinghorror_living`/`_hit`/`_dead`, vol 0.65 | identical (`:76-97`) | PORTED |

**Verdict: DIVERGENT** — stats doubled and the "deep underground / night horror" spawn identity removed.

---

### Cricket

| Feature | Original (`Cricket.java`) | Port (`entity/EntityCricket.java`) | Status |
|---|---|---|---|
| Stats | HP 3 (`:91`), atk 0, speed 0.15, XP 1 (`:25`), size 0.1×0.1 | identical attrs (`:47-52`); size 0.4×0.4 (`ModEntities.java:354`) | PORTED (size 0.1→0.4) |
| AI | Panic0(1.4), WanderALot(8)1 (`:27-28`); random hop every ≥50t 1-in-50 (`jumpAround:56-64`) | Float0, Panic1 ✓, WanderALot2 ✓; identical hop math (`:68-99`) | PORTED |
| Drops | none (`func_70628_a` empty) | `cricket.json` empty pools ✓ | PORTED |
| Spawning | ambient 11 biomes w1–3 1–8 (`OreSpawnMain.java:4950-4960`); rules y≥30 & ≤5 buddies (`:137-147`) | `swarm_cricket.json` plains/forest biomes w8 1–3; `checkSpawnRules` y≥30 & ≤5 buddies ✓ (`:153-158`) | PORTED |
| Sounds | `cricket` 1-in-2 + singing sync 40t, vol 0.7, silent steps, no hurt/death | identical incl. singing data sync (`:102-131`) | PORTED |
| Misc | no fall damage ✓ | ✓ | PORTED |

**Verdict: PORTED** — faithful port.

---

### Cryolophosaurus

| Feature | Original (`Cryolophosaurus.java`) | Port (`entity/Cryolophosaurus.java`) | Status |
|---|---|---|---|
| Stats | HP 10, atk 3, def 1 (`OreSpawnMain.java:6482`), speed 0.25, XP 10 (`:49`), size 0.75×0.75 | HP 20, atk 5, armor 0, follow 16 (`:51-57`); XP 10 ✓; size ✓ (`ModEntities.java:64`) | DIVERGENT (HP 10→20, atk 3→5, armor 1→0) |
| AI | Swim0, Panic1, MoveThroughVillage2, WanderALot(10)3, Watch4, LookIdle5; HurtBy; **proactive hunt** 1-in-5 over 9×2×9, swing 1-in-12/1-in-14 at distSq<5; excludes Alosaurus/TRex/own kind/ghosts/CaveFisher/insects (`func_70619_bc:141-156`, `isSuitableTarget:158-211`) | Float0, Panic1 ✓, `DinosaurMeleeAttackGoal`2 (`Presets.cryolophosaurus()` — claims same dice), WanderALot3, LookAt4, RandomLook5; HurtBy only — **proactive hunting absent** (comment claims it never had it, which is wrong) (`:34-44`) | DIVERGENT (was proactively aggressive toward animals/players) |
| Drops | 1-in-10: raw chicken / uranium nugget / titanium nugget, else nothing (`func_146068_u:120-132`) | `cryolophosaurus.json`: bone 2–5 + diamond 0–1@20% | DIVERGENT |
| Spawning | rules: dark and (night or y≤50) (`func_70601_bi:231-236`) | 4 biome modifiers (taiga w2 1–2, is_cold, c_cold_overworld, direct); no checkSpawnRules | PARTIAL |
| Sounds | `cryo_living` (1-in-6)/`cryo_hurt`/`cryo_death`, vol 0.75 | identical (`:70-94`) | PORTED |

**Verdict: DIVERGENT** — stats doubled, aggression model reduced to retaliation-only, drops replaced.

---

### CrystalCow

| Feature | Original (`CrystalCow.java`) | Port (`entity/CrystalCow.java`) | Status |
|---|---|---|---|
| Base class | extends **RedCow** (inherits its stats/behavior) | extends vanilla `Cow` with `Cow.createAttributes()` (`:15-22`) | DIVERGENT (RedCow lineage lost) |
| Drops | 0–2(+looting) crystal apples + 1 apple + RedCow drops (`func_70628_a:19-26`) | `crystal_cow.json` (crystal apple 1–3 + leather 1) + custom 1–2 **crystal pink ingots** (`:25-31`) — ingots invented, vanilla apple gone | DIVERGENT |
| Breeding | breeds to CrystalCow ✓ | ✓ (`getBreedOffspring:40-42`) | PORTED |
| Spawning | crystal-dimension chunk provider | `dim_crystal_locals.json` w1 1–4 | PORTED (approx.) |
| Misc | — | never despawns (`removeWhenFarAway` false, new) | PARTIAL |

**Verdict: DIVERGENT** — parentage and drop table both changed; pink-ingot drop is an invention.

---

## Summary

| Entity | Overall status |
|---|---|
| Acid | PORTED |
| Alien | DIVERGENT |
| Alosaurus | DIVERGENT |
| AntRobot | DIVERGENT |
| AttackSquid | DIVERGENT |
| BandP | DIVERGENT |
| Baryonyx | PARTIAL |
| Basilisk | DIVERGENT |
| Beaver | PARTIAL |
| Bee | DIVERGENT |
| Bertha (item) | DIVERGENT |
| BerthaHit | DIVERGENT |
| BetterFireball | DIVERGENT |
| Boyfriend | PARTIAL |
| Brutalfly | DIVERGENT |
| Camarasaurus | PARTIAL |
| Cassowary | PARTIAL |
| CaterKiller | DIVERGENT |
| CaveFisher | DIVERGENT |
| Cephadrome | DIVERGENT |
| Chipmunk | PARTIAL |
| CliffRacer | PARTIAL |
| CloudShark | DIVERGENT |
| Cockateil | PARTIAL |
| Coin | PARTIAL |
| Crab | DIVERGENT |
| CreepingHorror | DIVERGENT |
| Cricket | PORTED |
| Cryolophosaurus | DIVERGENT |
| CrystalCow | DIVERGENT |

**Counts: PORTED 2 · PARTIAL 9 · DIVERGENT 19 · MISSING 0 (entity-level) · UNVERIFIED 0**

Recurring systemic issues across the slice:
1. **Double drops** — loot-table JSON + `dropCustomDeathLoot` both fire (Alosaurus, AttackSquid, Basilisk, Cassowary, CaterKiller, Cephadrome, Bee, Baryonyx).
2. **Ignite durations are equivalent** — 1.7.10 `func_70015_d(n)` (setFire) takes seconds; the port's `igniteForSeconds(n)` with the same n is a correct mapping. Damage-value divergences in the same projectiles stand.
3. **`checkSpawnRules` frequently omitted**, deleting day/night, altitude, darkness, and buddy-count gates.
4. **Stats rewritten** rather than copied for most monsters (usually flattened armor to 0 and re-rolled HP/attack).
5. **Custom port comments claiming "matches 1.7.10"** are often wrong (EntityCaterKiller tree-eat heal values, despawn-vs-metamorphosis; Cryolophosaurus "never proactive" claim).
