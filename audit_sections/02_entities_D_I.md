# Entity Audit — Slice D–I (40 entities)

Original = `reference_1_7_10_source\sources\danger\orespawn\` (1.7.10). Port = `src\main\java\danger\orespawn\` (NeoForge 1.21.1).
Original config-default stats come from `OreSpawnMain.get_mobstats(name, health, attack, defense)` (OreSpawnMain.java:6468-6509). Original spawn registrations: `EntityRegistry.addSpawn` in OreSpawnMain.java:4522-4981 and dimension spawn lists in `BiomeGenUtopianPlains.java` / `ChunkProviderOreSpawn2.java`.

---

### DeadIrukandji
| Feature | Original | Port | Status |
|---|---|---|---|
| Stats | Projectile, no attributes; extends LaserBall, calls `setIrukandji()` (DeadIrukandji.java:16-36) | Extends `LaserBall`, calls `setIrukandji()` (entity/DeadIrukandji.java) | PORTED |
| AI goals | n/a (projectile) | n/a | PORTED |
| Attack/abilities | Irukandji-type LaserBall: 100 dmg on player hit | `LaserBall.IRUKANDJI_DAMAGE = 100.0f` (entity/LaserBall.java) | PORTED |
| Drops | none | none | PORTED |
| Spawning | Registered `registerModEntity("DeadIrukandji", 64, 1, true)` (OreSpawnMain.java:3442) | `ModEntities.java:712` MISC, 0.25×0.25, noSummon | PORTED |
| Sounds | none | none | PORTED |

**Verdict: PORTED** — thin subclass faithfully reproduced.

---

### Dragon
| Feature | Original | Port | Status |
|---|---|---|---|
| Stats | HP 200, speed 0.32, attack 35, armor 14 (Dragon.java attribute init / `func_70658_aO`) | HP 200, speed 0.32, attack 35, follow 40, KB-resist 0.5; **no ARMOR attribute** (entity/Dragon.java `createAttributes`) | PARTIAL (armor 14 missing) |
| AI goals | Custom tick: owner-follow, rider flight, target logic | FloatGoal(0), SitWhenOrderedTo, FollowOwnerGoal(1.1,12,2), TemptGoal(BONE,1.25), Stroll, LookAt, RandomLook; HurtByTarget | PORTED (modernized) |
| Attack/abilities | Melee 35 (2× vs Kraken); rider-controlled flight; fire/ice/water projectiles; type-switch fire↔ice | `doHurtTarget` 35 dmg, 2× vs Kraken + knockback; `handleRiderFlight`; SmallFireball/BetterFireball (fire) vs WaterBall/IceBall (ice); Lava/Water bucket type switch; flint-and-steel light, gunpowder supercharge, soul sand extinguish | PORTED |
| Taming | Raw beef tame | `Items.BONE`, 1/5 chance, heal to full (entity/Dragon.java `mobInteract`) | DIVERGENT (item changed beef→bone) |
| Drops | Raw beef (`func_70628_a`) | Hardcoded bones 1–6 + loot table `entities/dragon.json` = diamonds 1–6 (+looting) | DIVERGENT (beef→bones/diamonds) |
| Spawning | Utopia dimension boss list w1 1–2 (BiomeGenUtopianPlains.java:164); no overworld addSpawn | `add_overworld_creatures.json` w1 1–1 overworld-wide | DIVERGENT (overworld vs Utopia-only) |
| Sounds | `orespawn:roar`, `alo_hurt`, `alo_death`, custom wing flap | Roar/alo_hurt/alo_death kept; flap = `SoundEvents.ENDER_DRAGON_FLAP` | PARTIAL |
| Misc | Magic Apple spawns Spyro (baby dragon) | Apple spawns `BabyDragon` | DIVERGENT (item) |

**Verdict: PARTIAL** — core flight/riding/combat ported; armor attribute, tame item, drops, and spawn dimension diverge.

---

### Dragonfly
| Feature | Original | Port | Status |
|---|---|---|---|
| Stats | HP 10, speed 0.33, attack 2 (Dragonfly.java) | HP 10.0, speed 0.33, attack 2.0, xp 5 (entity/EntityDragonfly.java:49-51) | PORTED |
| AI goals | Custom tick: ambient flight + prey scan (10×6×10), hunts bbWidth<0.6, attacks horses | `DragonflyHuntGoal` extends `AmbientFlightGoal`: 1/12 retarget roll, 10×6×10 scan, MAX_PREY_WIDTH 0.6, hurt at distSq<6 (entity/ai/DragonflyHuntGoal.java:32-35) | PORTED |
| Attack/abilities | Melee 2 on small insects/horses | Same; horses excluded when `DRAGONFLY_HORSE_FRIENDLY` config on (new toggle) | PORTED |
| Drops | none | `dragonfly.json` empty pools | PORTED |
| Spawning | addSpawn w5 3–5 swamp, w4 1–2 river (OreSpawnMain.java:4798-4799) | `swarm_dragonfly__direct.json` swamp/mangrove/river w5 1–3; jungle tag w5 1–3 | PORTED (group sizes 3–5→1–3) |
| Sounds | none | none | PORTED |

**Verdict: PORTED** — exact stat and hunt-radius parity; spawn biomes broadened slightly.

---

### DungeonBeast
| Feature | Original | Port | Status |
|---|---|---|---|
| Stats | HP 65, attack 12, defense 6 (OreSpawnMain.java:6501); speed 0.29 hardcoded | HP 60, attack 10, armor 4, speed 0.29, follow 24, xp 60 (entity/DungeonBeast.java:28-30,51-55) | DIVERGENT (65/12/6 → 60/10/4) |
| AI goals | swim, melee cadence in custom tick, wander | FloatGoal(0), `BugMeleeAttackGoal` dungeonBeast params (1.2 speed, 2.83 reach, cadence 8, rolls 7/0) (1), Stroll(2), LookAt(3), RandomLook(4); HurtBy(1), NearestTarget Player(2) | PORTED |
| Attack/abilities | Melee with cadence rolls | Same cadence preserved via BugMeleeAttackGoal.Params.dungeonBeast() | PORTED |
| Drops | Crystal-dimension items (`func_70628_a` crystal drops) | `dungeon_beast.json`: bones 3–8 (+looting) + 50% gold ingots 1–4 | DIVERGENT (crystal items → bones/gold) |
| Spawning | addSpawn w20 2–4 ambient **Roofed Forest** (field_150585_R) (OreSpawnMain.java:4981); also mob spawners / Crystal dim | `hostile_dungeon_beast.json`: `#minecraft:is_badlands` w20 2–4 | DIVERGENT (roofed forest → badlands) |
| Sounds | orespawn hurt/death strings | Equivalent `orespawn:` SoundEvents in port file | PORTED |

**Verdict: DIVERGENT** — behavior ported but stats lowered, drops replaced, spawn biome relocated.

---

### EasterBunny
| Feature | Original | Port | Status |
|---|---|---|---|
| Stats | HP 10, speed 0.45, attack 8 (EasterBunny.java:52-55) | HP 10, speed 0.45, attack 8.0, xp 5 (entity/EasterBunny.java:31-32,53-55) | PORTED |
| AI goals | Avoid hostiles/players, wander | Float(0), Breed(1), Avoid Monster(2), Avoid Player(3), Panic(4), LookAt(5), Stroll(6), RandomLook(7) | PORTED |
| Attack/abilities | **Lays mob eggs; tamed with carrot** (EasterBunny.java interact/update) | No egg-laying, no taming in port file | MISSING (egg-laying + carrot taming) |
| Drops | Raw chicken (`func_145779_a(Items.field_151076_bf)` EasterBunny.java:116) | `easter_bunny.json`: chicken 2–4 (+looting) | PORTED (count increased 1→2–4) |
| Spawning | addSpawn w10/w5/w8 1–2 across 7 biomes (OreSpawnMain.java:4682-4688) | `add_overworld_creatures.json` w3 1–2 overworld-wide | DIVERGENT (weights 10/8/5 → 3) |
| Sounds | none notable | none | PORTED |

**Verdict: PARTIAL** — signature mob-egg-laying and carrot taming absent.

---

### Elevator (→ HoverboardEntity)
| Feature | Original | Port | Status |
|---|---|---|---|
| Stats | HP 60, speed constants (max fwd 0.85 / rev 0.35), 10 colors | `HoverboardEntity`: HP 60.0, MOVEMENT_SPEED 0.0 (physics-driven), KB-resist 1.0; same speed constants MAX_FORWARD 0.85 / REVERSE 0.35 | PORTED |
| AI goals | none (rider physics) | none; `travel()` physics replicates yaw blend, hover lift, friction | PORTED |
| Attack/abilities | Rider control, crash damage, destroyed on item collision | Rider flight, fall-damage immune, crash at high speed drops sticks+diamonds; UltimateSword changes color (10 variants) | PORTED |
| Drops | Sticks/diamonds on crash | Same (hardcoded in crash handler); `elevator.json` empty | PORTED |
| Spawning | `registerModEntity("Hoverboard", 128, 1, true)` (OreSpawnMain.java:3883); item-spawned only | `ModEntities.java:561` hoverboard MISC 1.25×0.4 tracking 128; spawned by `HoverboardItem` | PORTED |
| Sounds | `orespawn:hover` hum | `SoundEvents.BEACON_AMBIENT` (entity/HoverboardEntity.java) | DIVERGENT (sound remapped) |
| Misc | single entity | Port also registers a separate simplified `Elevator` entity (ModEntities.java:553) — stub | PORTED (extra stub harmless) |

**Verdict: PORTED** — physics faithfully reproduced in HoverboardEntity; hover sound remapped to beacon hum.

---

### EmperorScorpion (→ EntityEmperorScorpion)
| Feature | Original | Port | Status |
|---|---|---|---|
| Stats | HP 350, attack 35, defense 20 (OreSpawnMain.java:6488); speed 0.35 | HP 300, attack 20, **no armor**, speed 0.35, follow 40, xp 200 (entity/EntityEmperorScorpion.java:83-89) | DIVERGENT (350/35/20 → 300/20/0) |
| AI goals | swim, melee cadence, wander-a-lot | Float(0), `EmperorScorpionPoisonGoal`(1) (Params: 1.2 speed, reach 6, cadence 4, rolls 4/6, forget 100), MyEntityAIWanderALot(2,14,1.0), LookAt(3), RandomLook(4); HurtBy(1), NearestTarget Player(2) | PORTED |
| Attack/abilities | Melee + 1/3 chance poison 90t; spawns baby scorpions when population low | Poison 1/3, 90 ticks, amp 0 (entity/ai/EmperorScorpionPoisonGoal.java:20-22); knockback ks=3.0/0.2; minion aura: every 30+rand(10) ticks spawns `EntityScorpion` if <3 within 16 blocks, cap 6 (EntityEmperorScorpion.java:52-60) — original rolled `nextInt(80)==1` per tick | DIVERGENT (aura cadence redesigned; documented in port comments) |
| Drops | Scale, painting, obsidian, raw beef, **enchanted** diamond gear/UltimateSword set (EmperorScorpion.java:181-315) | `emperor_scorpion.json`: scale, name_tag, obsidian 4–8, slimeballs 4–11, 1–5 rolls diamond-gear pool (7/20 empty weight) — **no enchantments**, raw beef→slimeballs | DIVERGENT |
| Spawning | addSpawn w1 desert, w2 savanna ambient (OreSpawnMain.java:4883-4884) | `hostile_emperor_scorpion__direct.json` desert w2 1–1; badlands tag w2 1–1 | PORTED (≈, badlands added) |
| Sounds | alo_hurt, emperorscorpion_death | Same `orespawn:alo_hurt` / `emperorscorpion_death` (EntityEmperorScorpion.java:112-121) | PORTED |

**Verdict: DIVERGENT** — combat pattern and poison ported exactly; stats −15%/−43%, armor dropped, loot de-enchanted, minion mechanic re-timed.

---

### EnchantedCow (→ EnchantedAppleCow)
| Feature | Original | Port | Status |
|---|---|---|---|
| Stats | Vanilla-cow stats (extends RedCow) | Extends vanilla `Cow` | PORTED |
| AI goals | Cow AI | Vanilla cow goals | PORTED |
| Attack/abilities | none | none | PORTED |
| Drops | Enchanted golden apples + golden apples + apples (EnchantedCow.java `func_70628_a`) | `enchanted_apple_cow.json`: leather 1–4, golden_apple 1–2, enchanted_golden_apple 1; **plus hardcoded `dropCustomDeathLoot`: 1–2 XP bottles always, 20% enchanted book** (entity/EnchantedAppleCow.java) | DIVERGENT (extra XP bottles + book) |
| Spawning | addSpawn w3/3/5/15 grp 2–4/2–5/3–6 (OreSpawnMain.java:4620-4623) + Utopia w5 2–4, Village w4 2–4 (BiomeGenUtopianPlains.java:105,309) | `add_overworld_creatures.json` w1 1–1; `dim_utopia_locals.json` w5 2–4; `dim_village_locals.json` w4 2–4 | PORTED (dims exact; overworld weight 3–15→1) |
| Sounds | cow | cow | PORTED |

**Verdict: DIVERGENT** — minor: drop list extended beyond original.

---

### EnderKnight
| Feature | Original | Port | Status |
|---|---|---|---|
| Stats | HP 60, attack 12, defense 6 (OreSpawnMain.java:6507) | HP 80, attack 15, speed 0.32, follow 64, no armor (entity/EnderKnight.java) | DIVERGENT (60/12/6 → 80/15/0) |
| AI goals | melee, teleport in custom tick | Float(0), MeleeAttack, Stroll, LookAt, RandomLook; HurtBy, NearestTarget Player | PORTED |
| Attack/abilities | Enderman-style teleport (fire/indirect damage), attacks starers | Teleport on fire or indirect magic/projectile damage; portal particles (entity/EnderKnight.java) | PORTED |
| Drops | Ender pearls | `ender_knight.json`: ender_pearl 1–3 (+looting), 0–1 ender_eye killed-by-player | PORTED |
| Spawning | addSpawn ambient w4 2–4 across 5 overworld biomes, w2 ×3, **w20 Roofed Forest** (OreSpawnMain.java:4920-4928) | `add_end_spawns.json`: `#minecraft:is_end` w8 1–2 — **no overworld spawns** | DIVERGENT (overworld → End-only) |
| Sounds | enderman sounds | Enderman ambient/scream/hurt/death | PORTED |

**Verdict: DIVERGENT** — teleport AI faithful; stats inflated and habitat moved from overworld (incl. w20 roofed forest hotspot) to the End.

---

### EnderReaper
| Feature | Original | Port | Status |
|---|---|---|---|
| Stats | HP 90, attack 18, defense 8 (OreSpawnMain.java:6508); step height 1 | HP 120, attack 20, speed 0.37, follow 81, step 1.0 (entity/EnderReaper.java) | DIVERGENT (90/18/8 → 120/20/0) |
| AI goals | as EnderKnight | Same goal set as port EnderKnight | PORTED |
| Attack/abilities | Teleportation, attacks starers, explosions on provocation | Teleport + portal particles ported; explosion behavior not present in port file | PARTIAL (explosions unverified in port) |
| Drops | Ender pearls | `ender_reaper.json`: ender_pearl 2–5 (+looting), 0–2 ender_eye killed-by-player | PORTED |
| Spawning | addSpawn ambient w2/1 1–2 across 8 biomes + **w38 2–4 Roofed Forest** (OreSpawnMain.java:4931-4939) | `add_end_spawns.json` w4 1–1 End-only | DIVERGENT (overworld → End-only) |
| Sounds | enderman sounds | Enderman set | PORTED |

**Verdict: DIVERGENT** — same relocation + stat inflation pattern as EnderKnight.

---

### EntityAnt
| Feature | Original | Port | Status |
|---|---|---|---|
| Stats | HP 1, speed 0.15, attack 0 | HP 1.0, speed 0.15, attack 0.0 (entity/EntityAnt.java) | PORTED |
| AI goals | MyEntityAIWanderALot | PanicGoal + `MyEntityAIWanderALot` (ported class) | PORTED |
| Attack/abilities | Right-click bare hand → teleport player to Utopia | Same; `getTargetDimension()` = Utopia; `findSafeY` landing | PORTED |
| Drops | none | `ant.json` empty | PORTED |
| Spawning | `registerModEntity("Ant", 16, 1)` (OreSpawnMain.java:3659); spawned near anthill blocks | `add_overworld_creatures.json` w12 1–4; `checkSpawnRules` Y≥50 + ≤4 ants nearby | PORTED (mechanism modernized) |
| Sounds | silent | silent, no footsteps | PORTED |

**Verdict: PORTED.**

---

### EntityButterfly
| Feature | Original | Port | Status |
|---|---|---|---|
| Stats | HP 2, speed 0.1, attack 0 | HP 2.0, speed 0.1, attack 0.0 (entity/EntityButterfly.java) | PORTED |
| AI goals | custom flight tick | `AmbientFlightGoal` (butterfly preset) | PORTED |
| Attack/abilities | Right-click bare hand → Chaos dimension teleport | Same | PORTED |
| Drops | none | `butterfly.json` empty | PORTED |
| Spawning | 16 addSpawn entries, w5–30 grp up to 5–15 (OreSpawnMain.java:4636-4651) | `swarm_butterfly__*` plains/flower/meadow w20 2–5 + forest/jungle/taiga tags | PORTED (consolidated) |
| Sounds | none | none | PORTED |

**Verdict: PORTED.**

---

### EntityCage
| Feature | Original | Port | Status |
|---|---|---|---|
| Mechanic | On entity hit with `nextInt(10)>=2` (80%): explicit per-type checks — discards mob, drops matching `CagedSpiderDriver/CagedCaveSpider/CagedSpider/CagedCrab/CagedBat(×2)/CagedPig/...` item (EntityCage.java:160-201); on fail/player → `CageEmpty` (EntityCage.java:174) | On hit, ~80% effective: discards **any** Mob, drops `CagedMobItem` carrying full NBT; player hit or miss → `ModItems.CAGE_EMPTY` (entity/EntityCage.java) | DIVERGENT (whitelist → universal capture w/ NBT) |
| Drops | Filled cage item per species or empty cage | Generic filled cage (NBT) or empty cage | DIVERGENT |
| Sounds | `random.explode` 1.0/1.5 + explode particle | Particle + sound on hit, equivalent | PORTED |

**Verdict: DIVERGENT** — capture transform exists (required) but universal NBT capture supersedes the original species whitelist; can now cage mobs the original could not.

---

### EntityCannonFodder
| Feature | Original | Port | Status |
|---|---|---|---|
| Stats | HP 20, speed 0.25, attack 4 | HP 20.0, speed 0.25, attack 4.0 (entity/EntityCannonFodder.java) | PORTED |
| AI goals | team combat in custom tick | TamableAnimal goals + activated combat vs monsters / enemy fodder | PORTED |
| Attack/abilities | **Hat items (multiple colors) define teams**; fights other-color fodder; `MyCornCob` spawns new entity | Golden Apple → hat 1, Enchanted Golden Apple → hat 3 only; attacks monsters or different-hat fodder; sit/guard patrol block; **no corncob spawning** | PARTIAL (hat colors reduced to 2; corncob breeding missing) |
| Drops | none notable | `cannon_fodder.json` empty | PORTED |
| Spawning | base class for Ostrich/Lizard/Chipmunk/VelocityRaptor; not naturally spawned itself | Registered CREATURE 0.6×0.6 (ModEntities.java:412); no biome modifier | PORTED |
| Sounds | n/a | n/a | PORTED |

**Verdict: PARTIAL** — team system trimmed from N hat colors to 2 apple-based teams; corncob replication mechanic absent.

---

### EntityLavaLovingItem
| Feature | Original | Port | Status |
|---|---|---|---|
| Mechanic | Extends EntityItem, fire-immune | Extends `ItemEntity`, fire-immune (entity/EntityLavaLovingItem.java) | PORTED |

**Verdict: PORTED.**

---

### EntityLunaMoth
| Feature | Original | Port | Status |
|---|---|---|---|
| Stats | HP 2, speed 0.1, attack 0 (extends Butterfly) | Same via EntityButterfly base | PORTED |
| AI goals | Seeks light/torches in dark (custom tick) | `LunaMothFlightGoal` (torch-seeking) | PORTED |
| Drops | Moth scale | `luna_moth.json`: `orespawn:moth_scale` 0–1 (+looting) | PORTED |
| Spawning | 15 addSpawn w8–20 (OreSpawnMain.java:4654-4668) | `swarm_luna_moth__direct.json` 6 forest biomes w15 2–5 + jungle/taiga tags | PORTED (consolidated) |
| Sounds | none | none | PORTED |

**Verdict: PORTED.**

---

### EntityMosquito
| Feature | Original | Port | Status |
|---|---|---|---|
| Stats | HP 2, speed 0.1, attack 0 | Same (entity/EntityMosquito.java) | PORTED |
| AI goals | targets players (custom flight) | `MosquitoFlightGoal` (player-seeking) | PORTED |
| Drops | none | `mosquito.json` empty | PORTED |
| Spawning | addSpawn w30/20/20 grp 5–10 swamp/jungle(s), w15 2–5 roofed forest (OreSpawnMain.java:4778-4781) | `swarm_mosquito__direct.json` swamp/mangrove w25 3–8 + jungle tag | PORTED (≈) |
| Sounds | mosquito buzz | `orespawn:mosquito` ambient | PORTED |

**Verdict: PORTED.**

---

### EntityRainbowAnt
| Feature | Original | Port | Status |
|---|---|---|---|
| Stats | HP 1, speed 0.15, attack 0 | Same (entity/EntityRainbowAnt.java) | PORTED |
| Abilities | Teleports player to Village dimension | Same via `getTargetDimension()` | PORTED |
| Spawning | `registerModEntity` only (OreSpawnMain.java:3675); spawned by anthill | No biome modifier (anthill/item spawned) | PORTED |

**Verdict: PORTED.**

---

### EntityRedAnt
| Feature | Original | Port | Status |
|---|---|---|---|
| Stats | HP 1, speed 0.15, attack 1 (EntityRedAnt.java) | HP 2.0, speed 0.2, attack 1.0 (entity/EntityRedAnt.java) | DIVERGENT (HP 1→2, speed 0.15→0.2) |
| AI goals | wander + player aggression | + MeleeAttackGoal + NearestAttackableTargetGoal(Player) | PORTED |
| Attack/abilities | Mining-dimension teleport; bites players | Teleport to Mining dim; contact 1.0 dmg per 20t with 1/15 land chance | PORTED |
| Drops | none | `red_ant.json` empty | PORTED |
| Spawning | registerModEntity (OreSpawnMain.java:3667) | `add_overworld_creatures.json` w8 1–3 | PORTED |
| Sounds | silent | silent | PORTED |

**Verdict: DIVERGENT** — minor stat drift (HP and speed doubled/raised).

---

### EntityThrownRock
| Feature | Original (EntityThrownRock.java) | Port (entity/EntityThrownRock.java) | Status |
|---|---|---|---|
| Type damage | t1=2, t2–4=5, **t5=10**, t6=20, t7/8=40, t9–11=150, t12=250 (orig:79-216) | t1=2, t2–5=5, t6=20, t7/8=40, t9–11=150, t12=250 (port:72-79) | DIVERGENT (t5 10→5) |
| Effects | t3 fire 20s; t4 poison 100; t5 slow 100; **t6 weakness 100**; **t9 fire 50t + weakness 100**; **t10 poison 200 + weakness 100**; **t11 slow 200 + weakness 100**; **t12 weakness 100 + explosion 5.1** (orig:107-227) | t3 fire 20s; t4 poison 100; t5 slow 100; **t6/9/11 wither 100**; t10 poison 200 only; **t12 wither 100** + explosion 5.1 (port:94-122) | DIVERGENT (weakness→wither; t9 lost ignite; t10/t11 lost secondary effects) |
| Explosions | t8 = 2.1, t12 = 5.1 (orig:167,227) | Same 2.1 / 5.1 (port:32-33) | PORTED |
| Block/return item | Breaks glass on impact + returns the **specific rock item** of its type (MySmallRock…MyCrystalTNTRock) (orig:229-285) | Always pops generic `ModItems.ROCK` (port:129); no glass-breaking | DIVERGENT (12 typed returns → 1 generic; glass shatter missing) |
| Tick | 30°/tick spin, 1000-tick lifetime, water-skip physics (orig:290-313) | Spin + 1000-tick lifetime ported; **water-skipping missing** (port:135-148) | PARTIAL |

**Verdict: DIVERGENT** — core throw/damage skeleton ported; five rock types have wrong effects, typed rock recovery and water-skipping lost.

---

### EntityUnstableAnt
| Feature | Original | Port | Status |
|---|---|---|---|
| Stats | HP 1, speed 0.15, attack 0 | Same | PORTED |
| Abilities | Islands-dimension teleport | Same | PORTED |
| Spawning | registerModEntity (OreSpawnMain.java:3683) | no natural spawn (matches) | PORTED |

**Verdict: PORTED.**

---

### Fairy
| Feature | Original | Port | Status |
|---|---|---|---|
| Stats | HP 40, speed 0.1, attack 3 (Fairy.java:68-71, contact dmg 2.0 line 126) | HP 40.0, speed 0.1, attack 3.0 (entity/Fairy.java) | PORTED |
| AI goals | custom tick: flight, monster targeting, owner-follow/teleport, `myowner` UUID taming | `customServerAiStep` flight + targeting + owner follow/teleport; `myowner` UUID string kept; heals owner; `myBlink` | PORTED |
| Drops | Crystal Torch (Fairy.java drops) | `fairy.json`: glowstone_dust 1–3 (+looting) | DIVERGENT (Crystal Torch → glowstone) |
| Spawning | addSpawn **w25 2–4 ambient Roofed Forest only** (OreSpawnMain.java:4974) + Crystal dim w10 4–8, w5 2–4 (BiomeGenUtopianPlains.java:214,371) | `add_overworld_ambient.json` w5 1–3 ALL overworld; `dim_crystal_locals.json` w10 4–8; `dim_chaos_locals.json` w5 2–4 | DIVERGENT (roofed-forest hotspot → diluted overworld-wide) |
| Sounds | rat_hit hurt, big_splat death | `orespawn:rat_hit` / `big_splat` | PORTED |

**Verdict: PARTIAL** — taming/follow/heal ported with UUID system; drop item and overworld spawn distribution diverge.

---

### Firefly
| Feature | Original | Port | Status |
|---|---|---|---|
| Stats | HP 1, speed 0.1, attack 0 | Same (entity/Firefly.java) | PORTED |
| AI goals | blink + flight | `AmbientFlightGoal` firefly preset + `blinker` light flash; daytime despawn unless sheltered | PORTED |
| Drops | **ExtremeTorch** (Firefly.java drop) | `firefly.json`: glowstone_dust 0–1 | DIVERGENT (ExtremeTorch → glowstone) |
| Spawning | 13 addSpawn w10–15 grp 2–10 (OreSpawnMain.java:4691-4703) | `swarm_firefly__direct.json` 5 dark-forest biomes w15 3–10 + forest/jungle tags | PORTED (consolidated) |
| Sounds | none | none | PORTED |
| Spawn rules | dark, count cap | Y≥50, sheltered-dark, ≤10 fireflies nearby | PORTED |

**Verdict: DIVERGENT** — only the drop item changed; everything else equivalent.

---

### Flounder
| Feature | Original | Port | Status |
|---|---|---|---|
| Stats | HP 5, speed 0.25, attack 0 | HP 5, speed 0.25, attack 0.0 (entity/Flounder.java) | PORTED |
| AI goals | flee players, seek water | Float(0), Breed, AvoidEntity(Player), Panic, LookAt, MyEntityAIWander, RandomLook; water-seek + in-water heal | PORTED |
| Drops | Raw fish | `flounder.json`: cod 1–2 (+looting) | PORTED |
| Spawning | Utopia water lists w2 2–4 / w5 6–8 (BiomeGenUtopianPlains.java:126,253) | `add_ocean_spawns.json` w8 1–3 `#minecraft:is_ocean` | DIVERGENT (Utopia → overworld oceans) |
| Sounds | splash/little_splat/ratdead | GENERIC_SPLASH ambient, `little_splat` hurt, `ratdead` death | PORTED |

**Verdict: PORTED** — habitat relocated to vanilla oceans (Utopia lists also exist for other mobs; minor).

---

### Frog
| Feature | Original | Port | Status |
|---|---|---|---|
| Stats | HP 8, speed 0.1, attack 3 (melee in tick) | HP 8, speed 0.1, attack attr 0.0 but melee handled in `customServerAiStep` (entity/Frog.java) | PORTED |
| AI goals | jumping, insect hunting | Float(0), Panic, MyEntityAIWander; insect targeting (EntityAnt/Butterfly/Cricket/Mosquito/Firefly/WormSmall), heals on kill; random jumping | PORTED |
| Attack/abilities | Shift+right-click → Boyfriend/Girlfriend transform | 50/50 Boyfriend/Girlfriend transform + particles/sound | PORTED |
| Drops | Slimeball | `frog.json`: slime_ball 2–4 | PORTED (1→2–4) |
| Spawning | addSpawn waterCreature w20 3–6 river, w20 2–6 swamp, ambient w2–3 (OreSpawnMain.java:4963-4967) | `add_overworld_creatures.json` w10 1–2; `checkSpawnRules` Y≥50, daytime, ≤5 frogs | DIVERGENT (river/swamp focus → all overworld) |
| Sounds | frog / scorpion_hit / big_splat | `orespawn:frog` / `scorpion_hit` / `big_splat` | PORTED |

**Verdict: PORTED** — full mechanic parity incl. transform easter egg; spawn distribution flattened.

---

### GammaMetroid (→ EntityGammaMetroid)
| Feature | Original | Port | Status |
|---|---|---|---|
| Stats | HP 100, attack 10, defense 12 (OreSpawnMain.java:6486) | HP 60.0, attack 8.0, speed 0.15, no armor (entity/EntityGammaMetroid.java) | DIVERGENT (100/10/12 → 60/8/0) |
| AI goals | block-eating heal, taming, custom tick | Float, Breed, FollowOwner, Tempt(iron ingot), Stroll, LookAt, RandomLook; OwnerHurtBy/OwnerHurt/HurtBy/NearestTarget(Monster); stone-eating + heal in `customServerAiStep` | PORTED |
| Taming | Iron-related taming | `Items.IRON_INGOT` 1/3 chance; glass to untame; name tag | PORTED |
| Drops | Gold nuggets + iron ingots (GammaMetroid.java:227-231) | `gamma_metroid.json`: gunpowder 5–14 + iron 6–15 | DIVERGENT (gold nuggets → gunpowder) |
| Spawning | Crystal dimension list **w35 4–7** (ChunkProviderOreSpawn2.java:386) + Utopia boss w1 (BiomeGenUtopianPlains.java:514) | `add_nether_spawns.json` w3 1–1 + `companion_gamma_metroid.json` mountains w1 1–1 | DIVERGENT (Crystal dim swarms → Nether/mountains singles) |
| Sounds | wtf_living etc. | `orespawn:wtf_living` / `duck_hurt` / `alo_death` | PORTED |

**Verdict: DIVERGENT** — tame/eat/heal loop ported; stats cut ~40%, drops and habitat replaced.

---

### Gazelle
| Feature | Original | Port | Status |
|---|---|---|---|
| Stats | HP 15, speed 0.3, attack 0 (Gazelle.java:74-77) | HP 15, speed 0.3, attack 0.0 (entity/Gazelle.java) | PORTED |
| AI goals | flee, graze, follow owner | Float, Breed, FollowOwner, Avoid(Monster+Player), Tempt(wheat), Panic, LookAt, MyEntityAIWander, RandomLook | PORTED |
| Abilities | Wheat taming; eats plants to heal; buddies | Wheat 1/2 tame; eats carrots/potatoes/short+tall grass; buddy seeking; glass untame; name tag | PORTED |
| Drops | Poppy (Gazelle.java:347) + super drops | `gazelle.json`: mutton 1–3 (+looting) | DIVERGENT (poppy → mutton) |
| Spawning | Utopia w10 2–4 (BiomeGenUtopianPlains.java:91) | `add_overworld_creatures.json` w8 2–4 + utopia w10 2–4 + village w10 2–4 | PORTED (Utopia exact; overworld added) |
| Sounds | scorpion_hit / cryo_death | Same | PORTED |

**Verdict: PORTED** — drop item changed (poppy→mutton), otherwise faithful.

---

### Ghost
| Feature | Original | Port | Status |
|---|---|---|---|
| Stats | HP 2, speed 0.1, attack 2 | HP 2.0, speed 0.1, attack 2.0, follow 32 (entity/Ghost.java) | PORTED |
| AI goals | float toward player, contact damage, noclip | LookAt + NearestTarget(Player); float-to-player + contact damage in `customServerAiStep`; `noPhysics`; inWall immune | PORTED |
| Drops | none notable | `ghost.json`: bone 0–2 (+looting) | DIVERGENT (added drops) |
| Spawning | ~28 addSpawn ambient w2–15 grp up to 5–10 (OreSpawnMain.java:4544+,4784-4788) | `add_cave_spawns.json` w4 1–1 overworld + `dim_chaos_locals.json` w15 3–6; spawn rule: dark only | DIVERGENT (broad ambient w15 → cave w4) |
| Sounds | ghost_sound | `orespawn:ghost_sound` | PORTED |

**Verdict: PORTED** — behavior exact; spawn density much lower and bones added.

---

### GhostSkelly
| Feature | Original | Port | Status |
|---|---|---|---|
| Stats | HP 5, speed 0.1, attack 4 | HP 5.0, speed 0.1, attack 4.0, follow 32 | PORTED |
| AI goals | as Ghost | Same as port Ghost | PORTED |
| Drops | none notable | `ghost_skelly.json`: bone 1–3 (+looting) + arrows 0–2 | DIVERGENT (added) |
| Spawning | ~28 addSpawn ambient w2–15 (OreSpawnMain.java:4522-4543,4791-4795) | `add_cave_spawns.json` w4 1–1 + `dim_chaos_locals.json` w10 2–4 | DIVERGENT (density) |
| Sounds | chain_rattles | `orespawn:chain_rattles` | PORTED |

**Verdict: PORTED** — same pattern as Ghost.

---

### GiantRobot
| Feature | Original | Port | Status |
|---|---|---|---|
| Stats | Uses `Jeffery_stats` = HP 550, attack 40, defense 18 (OreSpawnMain.java:6476; GiantRobot.java:63-65,97,105); xp = health/2 = 275; speed 0.55; fire-immune | HP 2000, attack 100, armor 12, speed 0.55, xp 500 (entity/GiantRobot.java:56-62); port also adds separate `Jeffery` entity HP 1000/50/6 xp 250 (entity/Jeffery.java:22-28) | DIVERGENT (550/40/18 → 2000/100/12; original had ONE entity) |
| AI goals | swim(0), WanderALot(1,14,1.0), MoveThroughVillage(2), Watch(3), LookIdle(4); HurtBy(1) (GiantRobot.java:53-58) | Float(0), Stroll(1), LookAt(3), RandomLook(4); HurtBy(1) — MoveThroughVillage and WanderALot dropped (port:48-54) | PARTIAL |
| Attack/abilities | **Fires LaserBall projectiles**: aims within 0.5 rad, reload 10 (close, vol 2.5 pitch 1.0) / 25 + `setSpecial()` (far >100 distSq, vol 3.5 pitch 0.5), launch offset y+10 (GiantRobot.java:264-283); melee ks 2.2/0.25; cactus-immune | Melee only (ks 2.2/0.25 ported, port:81-93); `reloadTicker` field exists but **no LaserBall firing code**; cactus-immune ported (port:130) | PARTIAL (entire ranged laser attack MISSING) |
| Drops | 15–29× LaserBall(×4) + 10–19 random of {SpiderRobotKit, AntRobotKit, RayGun, redstone block, dispenser, sticky piston, piston, lever, iron block, piston-head} (GiantRobot.java:158-211) | `giant_robot.json`: iron 5–10 + 30% iron blocks 1–3 | DIVERGENT (robot kits/ray gun lost) |
| Spawning | Monster list w8 1–2 (BiomeGenUtopianPlains.java:289); spawn rule Y≥50, night, 5 air above (GiantRobot.java:364-381) | **No biome modifier for giant_robot or jeffery**; config toggle exists (ModSpawnControl.java:97) | MISSING (no natural spawn) |
| Sounds | robot_living (1/4), robot_hurt, robot_death | Same three (port:157-171) | PORTED |

**Verdict: PARTIAL** — melee shell ported; signature laser barrage, kit drops, and natural spawning all absent; stats inflated ~4×.

---

### Girlfriend
| Feature | Original | Port | Status |
|---|---|---|---|
| Stats | HP 80 (**800 on Valentine's Day**, Girlfriend.java:569-571), speed 0.3, attack 8 (lines 200-203) | HP 80, speed 0.3, attack 8.0 (entity/Girlfriend.java:51-52,83-88) | PARTIAL (Valentine 800 HP missing) |
| AI goals | MyEntityAIFollowOwner(1, 1.4,12,1.5), Tempt poppy(2), **MyEntityAIDance(3)**, **EntityAIArrowAttack(4, 1.25, 20t, 10.0f)**, Swim(5), Panic(6), Watch(7), MyEntityAIWander(8), LookIdle(9), **OpenDoor(10), MoveIndoors(11)**; targets: **MyValentineTarget ×2, MyEntityAINearestAttackableTarget creeper/EntityLiving, MyEntityAIJealousy ×2** (Girlfriend.java:149-173) | FollowOwner(1, 1.4,12,1.5), Tempt poppy(2), **MeleeAttackGoal(4)**, Float(5), LookAt(7), Stroll(8), RandomLook(9); OwnerHurtBy/OwnerHurt/NearestTarget(Monster) (port:70-80) — `MyEntityAIDance.java` exists in port but is NOT wired to Girlfriend; no Jealousy/Valentine classes | PARTIAL (dance, jealousy, valentine, door/indoor AI missing) |
| Attack/abilities | **Ranged UltimateArrow attack** (EntityAIArrowAttack + IRangedAttackMob); equipping armor/weapons; food healing ×5; poppy tame 1/3; dandelion skin cycle (41 skins) | Melee only; armor/weapon equipping ported (port:194-211); food heal nutrition×5 (port:186); poppy tame 1/3 (port:153); dandelion cycles 41 skins (port:171-179) | PARTIAL (ranged arrow attack missing) |
| Drops | Shoes + game controller | `girlfriend.json`: boots_shoes 4–20 + 50% game_controller 1–5 | PORTED |
| Spawning | 12 addSpawn w2–30 grp up to 8–15 (OreSpawnMain.java:4574-4585) | `companion_girlfriend.json` overworld-wide w4 1–2 | DIVERGENT (w30 8–15 hotspots → flat w4 1–2) |
| Sounds | o_happy/o_hurt/o_ow/o_death_girlfriend/o_death_single + voice pitch system | All five sounds + voice 0–9 pitch + voiceEnable ported (port:267-306) | PORTED |
| Misc | Cap incoming damage 10, cactus immune, fall cap 3 | Same (port:248-263) | PORTED |

**Verdict: PARTIAL** — interaction/equipment/sound systems faithful; loses ranged combat, dance, jealousy, Valentine's mode, and door navigation.

---

### GoldCow
| Feature | Original | Port | Status |
|---|---|---|---|
| Stats | vanilla cow (extends RedCow) | extends vanilla `Cow` | PORTED |
| Drops | Golden apples (GoldCow.java) | `gold_cow.json`: leather 1–3 + golden_apple 1; **plus hardcoded `dropCustomDeathLoot` 1–3 GOLD_INGOT** (entity/GoldCow.java) | DIVERGENT (gold ingots added) |
| Spawning | addSpawn w5 2–6/2–5 ×4 biomes (OreSpawnMain.java:4616-4619) + Utopia w8 2–6, Village w6 2–6 (BiomeGenUtopianPlains.java:104,308) | utopia w8 2–6, village w6 2–6 exact; overworld natural spawn via `golden_apple_cow` w2 1–1 | PORTED (dims exact) |

**Verdict: DIVERGENT** — minor: hardcoded gold-ingot bonus not in original. (Note: port splits original GoldCow into `gold_cow` + `golden_apple_cow` ids.)

---

### GoldFish
| Feature | Original | Port | Status |
|---|---|---|---|
| Stats | HP 6 (mygetMaxHealth), speed 0.22, attack 1.0 (GoldFish.java:33-36) | HP 6, speed 0.22, attack 1.0, xp 5 (entity/GoldFish.java:26,43-47) | PORTED |
| AI goals | aerial wander between Y 120–140 with bias, vertical damping 0.6 | Identical: LOW 120 / HIGH 140, ±2 bias, retarget 1/300 or <2.1 blocks, motion blend 0.4/0.7/0.2, yaw/6 (port:27-111) | PORTED |
| Attack/abilities | none (passive flier) | none; no fall damage; pushable | PORTED |
| Drops | Gold-related items (GoldFish.java `func_146068_u`) | `gold_fish.json`: **empty pools**, no `dropCustomDeathLoot` | MISSING (drops) |
| Spawning | Utopia lists w1/w5/w10 (BiomeGenUtopianPlains.java:120,176,368) | `add_ocean_spawns.json` w10 1–3 + `dim_chaos_locals.json` w10 2–4 | DIVERGENT (Utopia → oceans/Chaos) |
| Sounds | splash | GENERIC_SPLASH ×2 + `little_splat` death (port:116-128) | PORTED |

**Verdict: PARTIAL** — flight logic is a number-perfect port; drops absent and habitat moved.

---

### Hammerhead
| Feature | Original | Port | Status |
|---|---|---|---|
| Stats | HP 240, attack 75, defense 20 (OreSpawnMain.java:6477; Hammerhead.java:59-83) | HP 200, speed 0.35, attack 20.0, **no armor**, xp 350 (entity/Hammerhead.java:38-40) | DIVERGENT (240/75/20 → 200/20/0) |
| AI goals | wanderALot + custom revenge/nearest-player tick | Float(0), MyEntityAIWanderALot(2,16,1.0), LookAt(3), RandomLook(4); HurtBy(1); custom tick: revenge target, nearest player 18 blocks, melee range 7+bb/2, attack roll 1/3 (port:121-159) | PORTED |
| Attack/abilities | Melee + knockback | doHurtTarget ks 1.1/0.85 (×2 vs player); cactus-immune; **port adds boss bar** (ServerBossEvent, port:42) | DIVERGENT (boss bar invented) |
| Drops | XP bottle, ExperienceCatcher, CreeperLauncher, CreeperRepellent, raw beef, ExperienceTreeSeed, MyHammy (Hammerhead.java:126-147) | `hammerhead.json`: prismarine 5–8 + experience_catcher 5–10; hardcoded 8 XP bottles + 6 bones (port:162-165) | DIVERGENT (CreeperLauncher/Repellent/Hammy/seed lost) |
| Spawning | Utopia monster list w1 1–1 (BiomeGenUtopianPlains.java:463) | `add_ocean_spawns.json` w3 1–1; `checkSpawnRules` Y≥50, no other Hammerhead in 16 blocks | DIVERGENT (Utopia → oceans) |
| Sounds | hammerhead_living (1/3), alo_hurt, hammerhead_death | Same three (port:169-187) | PORTED |

**Verdict: DIVERGENT** — attack damage cut 75→20; loot table loses 4 unique items; boss bar added.

---

### HerculesBeetle (→ EntityHerculesBeetle)
| Feature | Original | Port | Status |
|---|---|---|---|
| Stats | HP 250, attack 30, defense 19 (OreSpawnMain.java:6468) | HP 200, attack 15, speed 0.25, follow 24, no armor, xp 200 (entity/EntityHerculesBeetle.java:53-58) | DIVERGENT (250/30/19 → 200/15/0) |
| AI goals | melee cadence, wanderALot | Float(0), BugMeleeAttackGoal herculesBeetle (1.2/5.0/4/3/4)(1), WanderALot(2,14,1.0), LookAt(3), RandomLook(4); HurtBy(1), NearestTarget Player(2) | PORTED |
| Attack/abilities | Vertical "gore" toss ks 0.45 vs 1.25 (×2 player); fire-immune; hurt-timer | Same math preserved verbatim (port:107-123); hurtTimer 20; regen 1/150 +2 HP | PORTED |
| Drops | MyBigHammer + painting + raw beef + **enchanted diamond gear set** (HerculesBeetle.java:141-288) | `hercules_beetle.json`: bones 3–6 + gunpowder 2–5; hardcoded name_tag + 4–11 bones (port:149-156) | DIVERGENT (BigHammer + enchanted gear lost) |
| Spawning | 7 addSpawn w2–5 1–2 (OreSpawnMain.java:4732-4738) | `hostile_hercules_beetle__direct.json` 6 forest/taiga biomes w3 1–2 + jungle tag | PORTED (≈) |
| Sounds | alo_hurt, hercules_death | Same (port:82-91) | PORTED |

**Verdict: DIVERGENT** — combat feel preserved exactly; stats −20%/−50% and reward loot gutted.

---

### Hydrolisc (→ EntityHydrolisc)
| Feature | Original | Port | Status |
|---|---|---|---|
| Stats | Attack hardcoded 1.0 (Hydrolisc.java:74); HP ~60 via mygetMaxHealth; speed 0.2 | HP 100.0, attack 1.0, speed 0.25, xp 5 (entity/EntityHydrolisc.java:75-80) | DIVERGENT (HP 60→100, speed 0.2→0.25) |
| AI goals | water-seek scan, owner heal, taming | Float(0), Breed(1), Avoid Monster(2), FollowOwner(3), Tempt cod(4), Panic(5), LookAt(6), Stroll(7), RandomLook(8); OwnerHurtBy/OwnerHurt/HurtBy; expanding-shell water scan (radius 11) ported (port:116-174) | PORTED |
| Abilities | Heals owner at own HP cost; tame with raw fish | Owner heal 1.0 (self-hurt 1.0) when HP>20, 1/10 roll; cod tame 1/2; glass untame; name tag; sit toggle; damage cap 10; fall cap 2 | PORTED |
| Drops | Raw fish ×N (Hydrolisc.java:324-330) | `hydrolisc.json`: cod 2–7 (+looting) | PORTED |
| Spawning | addSpawn creature w25 3–6 swamp, w15 2–5 jungle, w10 1–3 jungleHills, w5 3–6 (OreSpawnMain.java:4829-4832) | ocean w3 1–1 + beach w3 1–2 + river w3 1–2 | DIVERGENT (swamp/jungle w25 → beach/river/ocean w3) |
| Sounds | cryo_hurt / cryo_death | Same (port:273-282) | PORTED |

**Verdict: DIVERGENT** — care-taking mechanics fully ported; HP buffed 66%, spawn biomes and density changed significantly.

---

### IceBall
| Feature | Original | Port | Status |
|---|---|---|---|
| Mechanic | LaserBall variant; on impact converts nearby blocks to ice with random ±4 jitter (IceBall.java:54-78); avoids Royalty | Extends `LaserBall`, `setIceBall()`, optional ice-block creation on hit (entity/IceBall.java); LaserBall handles damage/fire/explosion for iceball type | PORTED |
| Drops/sounds | n/a | n/a | PORTED |

**Verdict: PORTED.**

---

### InkSack
| Feature | Original | Port | Status |
|---|---|---|---|
| Mechanic | Damage on hit; 1/2 chance blindness `100 + 50*nextInt(8)` ticks (InkSack.java:60-62) | Damage (bonus vs Creepers) + blindness with variable duration; hit sound (entity/InkSack.java) | PORTED |
| Registration | projectile | MISC 0.25×0.25 noSummon (ModEntities.java:680) | PORTED |

**Verdict: PORTED.**

---

### Irukandji
| Feature | Original | Port | Status |
|---|---|---|---|
| Stats | HP 1, attack 20, defense 0 (OreSpawnMain.java:6509; Irukandji.java:57-85) | HP 5, attack 200.0, speed 0.15, xp 50 (entity/Irukandji.java:35-37) | DIVERGENT (HP 1→5, attack 20→200) |
| AI goals | water-seek, player-hunt tick | Float(0), MyEntityAIWander(1), LookAt(2), RandomLook(3); HurtBy(1); water scan radius 12 + dry-out damage 1/25; player hunt ≤6 blocks, attack roll 1/4 at distSq<3 (port:97-147) | PORTED |
| Attack/abilities | **Empty-hand attacker takes 200 dmg** (Irukandji.java:130,143); immune to other Irukandji | Empty main hand → 200.0 retaliation, attack negated (port:86-89); Irukandji-immune (port:85) | PORTED |
| Drops | MyIrukandji item | `irukandji.json`: `orespawn:dead_irukandji` 1 (+looting) | PORTED (renamed item) |
| Spawning | Utopia water list w4 2–3 (BiomeGenUtopianPlains.java:256) | `add_ocean_spawns.json` w4 1–2; rule Y≥50, 1/60 roll, ≤2 nearby | DIVERGENT (Utopia → oceans) |
| Sounds | little_splat / ratdead | Same (port:191-199) | PORTED |

**Verdict: DIVERGENT** — signature empty-hand punishment exact; base melee attack is 10× original (20→200) and HP 5× — every armed melee hit now near-lethal.

---

### IrukandjiArrow
| Feature | Original | Port | Status |
|---|---|---|---|
| Damage | Arrow damage scaled by velocity + `nextInt(dmg/2+2)` crit bonus (IrukandjiArrow.java:173-180), custom `func_70239_b` | Extends `AbstractArrow`, base damage 6.0 (entity/IrukandjiArrow.java) | UNVERIFIED (orig base damage value buried in decompiled arrow math; port 6.0 plausible but not number-matched) |
| Effects | Poison/weakness/slowness on hit (orig potion applications) | Poison + Weakness + Slowness, 200 ticks, amps 1/2 | PARTIAL (durations/amps not verified against orig constants) |
| Registration | projectile | MISC 0.5×0.5 noSummon (ModEntities.java:720) | PORTED |

**Verdict: PARTIAL** — arrow + triple-debuff present; exact damage/duration parity unconfirmed from decompiled source.

---

## Summary table

| Entity | Overall | Worst finding |
|---|---|---|
| DeadIrukandji | PORTED | — |
| Dragon | PARTIAL | armor 14 missing; tame item, drops, spawn dim diverge |
| Dragonfly | PORTED | — |
| DungeonBeast | DIVERGENT | stats 65/12/6→60/10/4; roofed forest→badlands |
| EasterBunny | PARTIAL | mob-egg-laying + carrot taming missing |
| Elevator | PORTED | hover sound → beacon hum |
| EmperorScorpion | DIVERGENT | 350/35/20→300/20/0; loot de-enchanted |
| EnchantedCow | DIVERGENT | drops extended (XP bottles, book) |
| EnderKnight | DIVERGENT | 60/12/6→80/15/0; overworld→End-only |
| EnderReaper | DIVERGENT | 90/18/8→120/20/0; overworld→End-only |
| EntityAnt | PORTED | — |
| EntityButterfly | PORTED | — |
| EntityCage | DIVERGENT | universal NBT capture replaces species whitelist |
| EntityCannonFodder | PARTIAL | hat teams reduced to 2; corncob spawning missing |
| EntityLavaLovingItem | PORTED | — |
| EntityLunaMoth | PORTED | — |
| EntityMosquito | PORTED | — |
| EntityRainbowAnt | PORTED | — |
| EntityRedAnt | DIVERGENT | HP 1→2, speed 0.15→0.2 |
| EntityThrownRock | DIVERGENT | 5 rock types wrong effects; typed returns lost |
| EntityUnstableAnt | PORTED | — |
| Fairy | PARTIAL | Crystal Torch drop → glowstone; spawn diluted |
| Firefly | DIVERGENT | ExtremeTorch drop → glowstone |
| Flounder | PORTED | — |
| Frog | PORTED | — |
| GammaMetroid | DIVERGENT | 100/10/12→60/8/0; Crystal dim w35→Nether w3 |
| Gazelle | PORTED | poppy drop → mutton |
| Ghost | PORTED | spawn density w15→w4 |
| GhostSkelly | PORTED | spawn density |
| GiantRobot | PARTIAL | LaserBall ranged attack MISSING; no natural spawn; stats ×4 |
| Girlfriend | PARTIAL | dance/jealousy/valentine/ranged-arrow AI missing |
| GoldCow | DIVERGENT | hardcoded gold ingots added |
| GoldFish | PARTIAL | drops missing entirely |
| Hammerhead | DIVERGENT | attack 75→20; 4 unique drops lost; boss bar added |
| HerculesBeetle | DIVERGENT | 250/30/19→200/15/0; enchanted-gear loot lost |
| Hydrolisc | DIVERGENT | HP 60→100; swamp/jungle→beach/river/ocean |
| IceBall | PORTED | — |
| InkSack | PORTED | — |
| Irukandji | DIVERGENT | attack 20→200 (10×), HP 1→5 |
| IrukandjiArrow | PARTIAL | damage/duration parity unverified |

**Counts: PORTED 17 · PARTIAL 8 · DIVERGENT 15 · MISSING 0 · UNVERIFIED 0** (sub-features marked MISSING/UNVERIFIED noted inline).
