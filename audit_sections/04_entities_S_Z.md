# Entity Audit: S–Z Slice (Scorpion … WormSmall)

Paths: ORIG = `reference_1_7_10_source\sources\danger\orespawn\`, PORT = `src\main\java\danger\orespawn\`, LOOT = `src\main\resources\data\orespawn\loot_table\entities\`, BMOD = `src\main\resources\data\orespawn\neoforge\biome_modifier\`.
Original config-default stats from `ORIG\OreSpawnMain.java` `get_mobstats(name, health, attack, defense)`. Note: the port's `MobStats.java` record exists but **none of the S–Z entities reference it** — all port attributes are hardcoded in `createAttributes()`; the MobStats values (e.g. `SCORPION 30/5`, MobStats.java:63) are dead data.

---

### Scorpion → EntityScorpion

| Feature | ORIGINAL | PORT | Status |
|---|---|---|---|
| Stats | HP 15, atk 4, def 10 (OreSpawnMain.java:6518); speed 0.2 (Scorpion.java:46); xp 10 (Scorpion.java:52); size 0.85×0.55 | HP 20, speed 0.2, atk 4, follow 24, no armor (EntityScorpion.java:50-56); xp 10 (:30) | **DIVERGENT** — HP 15→20, armor 10→0 |
| AI goals | 0 Swim, 1 MoveThroughVillage, 2 WanderALot(14,1.0), 3 WatchClosest(Player,8), 4 LookIdle; target 1 HurtBy (Scorpion.java:57-62); custom attack tick: `nextInt(6)==0` gate, range²<9, swing `nextInt(5)==0||nextInt(6)==1`, chase 1.2 (:170-193); targets creepers/spiders/VelocityRaptor, not other Monsters (:203-253) | 0 Float, 1 BugMeleeAttackGoal `Params.scorpion()` = (1.2, 3.0, 6, 5, 6, 0, 8, 3) — identical dice (BugMeleeAttackGoal.java:49), 2 WanderALot(14,1.0), 3 LookAt(8), 4 RandomLook; target HurtBy + NearestAttackableTarget(Player) (EntityScorpion.java:34-48) | **PARTIAL** — cadence/dice ported exactly; MoveThroughVillage dropped; creeper/spider/raptor targeting dropped (players only) |
| Attack | Melee atk 4 + 1/3 chance `orespawn:scorpion_attack` sound (Scorpion.java:182-184); cactus-immune (:195-201) | Melee atk 4; no attack sound; no cactus immunity | **PARTIAL** — sound + cactus immunity missing |
| Drops | 1/10 each: gold nugget / uranium nugget / titanium nugget (Scorpion.java:148-160) | Loot `scorpion.json`: bone 1–3 (+looting) | **DIVERGENT** |
| Spawning | addSpawn ambient: desert 15(3-6), roofedForest 28(2-4), savanna 15(3-5), savPlateau 15(2-4), mesa 6, mesaPlat 4, mesaPlatF 5 (OreSpawnMain.java:4901-4907); Island/Crystal dims w2(1-3) (BiomeGenUtopianPlains.java:487); spawn only in darkness or y<50 or near "Scorpion" spawner (Scorpion.java:281-299) | BMOD `hostile_scorpion__direct/is_badlands/is_savanna`: w15 (2-4); ON_GROUND + Monster rules (ModEntityAttributes.java:189) | **PARTIAL** — desert/savanna/mesa covered (group sizes shrunk), roofedForest w28 + custom dims + spawner check missing |
| Sounds | hurt `orespawn:scorpion_hit`, death `orespawn:cryo_death`, vol 1.5 (Scorpion.java:132-141) | Same ids, vol 1.5 (EntityScorpion.java:79-93) | **PORTED** |
| Misc | Not interactable (returns false) | n/a | PORTED |

**Verdict: DIVERGENT** — combat dice faithfully ported but stats, drops, target selection and biome coverage all differ.

---

### SeaMonster → SeaMonster

| Feature | ORIGINAL | PORT | Status |
|---|---|---|---|
| Stats | HP 110, atk 14, def 8 (OreSpawnMain.java:6493); speed 0.25 land / 0.55 in water; xp 150 | HP 150, atk 15, speed 0.25 (SeaMonster.java:38-40); xp 150 (:49); `dynamicMoveSpeed` is computed but **never applied to the attribute** (:86) so water speed-up is inert | **DIVERGENT** — HP 110→150, atk 14→15; water speed boost is dead code |
| AI goals | Swim, WanderALot, WatchClosest; water-scan + path (scan_it); knockback on hit | Float, WanderALot(16,1.0), 2×LookAt, RandomLook, HurtBy (SeaMonster.java:53-60); water-scan ported (:128-147, scan radius 12, dryOut 1/40); attack loop in customServerAiStep `nextInt(5)==1`, range (4+w/2)², swing 1/4 (:149-172) | **PORTED** (equivalent loops) |
| Attack | Melee + knockback 0.6/0.1 (×2 player); hurt-cooldown; no friendly fire between SeaMonsters | doHurtTarget knockback 0.6/0.1 ×2 player (:90-102); hurtCooldown 8 (:109-111); SeaMonster friendly-fire blocked (:114); cactus immune (:106) | **PORTED** |
| Drops | Fish ×(9-14), SeaMonsterScale, chance of enchanted iron tools/armor | Loot `sea_monster.json`: scale + name_tag + cod 9–14 + 1-in-20 iron-gear/diamond/sponge pool; **plus** code `dropCustomDeathLoot`: heart_of_the_sea + 9–14 cod + 1/3 diamond (SeaMonster.java:207-212) | **DIVERGENT** — double drops (code+table); heart_of_the_sea/name_tag added; gear unenchanted |
| Spawning | addSpawn waterCreature: ocean w4, swamp w2 (OreSpawnMain.java:4850-4851) | BMOD `add_ocean_spawns`: w1 (1-1); IN_WATER placement; checkSpawnRules y≥50, ≤1 nearby (SeaMonster.java:237-241) | **DIVERGENT** — weight 4→1, swamp dropped |
| Sounds | `seamonster_living/_hit/_death` (SeaMonster orig :135-145) | Same ids (SeaMonster.java:216-234) | **PORTED** |

**Verdict: PARTIAL/DIVERGENT** — behavior loops faithful, stats inflated, drops doubled, water-speed mechanic broken.

---

### SeaViper → SeaViper

| Feature | ORIGINAL | PORT | Status |
|---|---|---|---|
| Stats | HP 160, atk 22, def 12 (OreSpawnMain.java:6494); water/land speed switch | HP 120, atk 12, water 0.75 / land 0.25 (SeaViper.java:46-49) | **DIVERGENT** — HP 160→120, atk 22→12 |
| AI goals | Swim, WanderALot, WatchClosest + scan_it water pathing; melee w/ potion on hit | Float, SeaViperBiteGoal (Presets.seaViper = 1.5,4.5,5,2,4,0,18,4), RandomSwim, WanderALot, LookAts, RandomLook; HurtBy + NAT(Player) (SeaViper.java:71-81); WaterBoundPathNavigation + SmoothSwimming controls (:60-62,94) | **PORTED** (modernized) |
| Attack | Melee + **Poison** effect on hit; knockback | Knockback 0.8/0.14 ×2 player (:154-170); on-hit effect = **Hunger 8 s, 1/2 roll** (SeaViperBiteGoal.java:22-26); cactus immune, no viper friendly fire, hurtCooldown 5 | **DIVERGENT** — Poison → Hunger |
| Drops | Fish, SeaViperTongue, enchanted gear chances | Loot `sea_viper.json`: tongue + name_tag + cooked_cod 9–14 + cod 9–14 + iron-gear pool; **plus** code: heart_of_the_sea + 9–14 × (cod+salmon) (SeaViper.java:254-261) | **DIVERGENT** — double drops, large fish inflation |
| Spawning | addSpawn waterCreature: ocean w3, deepOcean w2 (OreSpawnMain.java:4854-4855) | BMOD ocean w3 + deep_ocean w3; IN_WATER placement; rules y≥50 + in-water + ≤1 nearby (:289-294) | **PORTED** (deepOcean 2→3 trivial) |
| Sounds | `seaviper_living/_hit/_death` | Same ids (:265-283) | **PORTED** |

**Verdict: DIVERGENT** — stats halved, poison swapped for hunger, drops doubled.

---

### Shoes (projectile)

| Feature | ORIGINAL | PORT | Status |
|---|---|---|---|
| Damage | 2.0; ShoeId 6 → 6.0; +4 vs Creeper; =1.0 vs Girlfriend/Boyfriend; =0 vs Player; =10 on valentines_day (Shoes.java:57-78) | 2.0 / 6.0 heavy / +4 creeper / 0 player (Shoes.java port :22-57) | **PARTIAL** — Girlfriend/Boyfriend 1.0-clamp and valentines override missing |
| Visuals | 4× snowballpoof + reddust on impact; spin 20°/tick (:80-95) | 4× ITEM_SNOWBALL; spin 20°/tick (:61-78) | **PORTED** (reddust dropped) |
| Misc | ShoeId 2–5 random per throw via DataWatcher | DATA_SHOE_ID synced, default 2 (:19-47) | PORTED |

**Verdict: PARTIAL** — core damage table ported, special-target cases missing.

---

### Skate → Skate

| Feature | ORIGINAL | PORT | Status |
|---|---|---|---|
| Stats | HP 8, atk 8, def 4 (OreSpawnMain.java:6519) | HP 15, atk 4, speed 0.25 (Skate.java:35-37) | **DIVERGENT** — HP 8→15, atk 8→4 |
| AI goals | Swim, Wander, WatchClosest; scan_it water-seek; group spawn (findBuddies) | Float, MyEntityAIWander, LookAt, RandomLook, HurtBy (:48-54); water-scan + dryOut 1/25 (:94-113); attack loop 1/8 gate, swing 1/4 (:115-136) | **PORTED** |
| Drops | Raw fish (func_146068_u) | Loot `skate.json`: prismarine_shard 1–3 | **DIVERGENT** |
| Spawning | Island/Crystal dims waterCreature w2 (3-6) (BiomeGenUtopianPlains.java:259); buddies/y≥50 gates | BMOD `add_ocean_spawns` w6 (1-2); rules y≥50, nextInt(30)==1, ≤6 nearby (:182-187) — same gate constants as 1.7.10 | **DIVERGENT** domain (custom dims → vanilla oceans), gates ported |
| Sounds | death `orespawn:ratdead`, vol 0.33 | Same (:173-179) | **PORTED** |

**Verdict: DIVERGENT** — stats and drops differ; behavior ported.

---

### Slice (ItemSword — NOT an entity)

| Feature | ORIGINAL | PORT | Status |
|---|---|---|---|
| Class type | `ItemSword` (Slice.java orig) | `item\Slice.java` extends SwordItem | **PORTED** |
| Enchants | onCreated/onUsingTick: `Enchantment.field_77338_j` lvl 5 + `field_77336_l` lvl 1 (Slice.java:33-43) | inventoryTick: Sharpness 5 + Bane of Arthropods 1 (port Slice.java:23-28) | **UNVERIFIED** — obfuscated enchant ids not conclusively mapped; levels (5/1) match |
| Swing projectile | onEntitySwing spawns `BerthaHit` | onEntitySwing spawns BerthaHit, 1.5F power ×2 velocity, durability cost 1 (:31-50) | **PORTED** |
| Misc | onLeftClickEntity true vs players (no hit) | Same (:53-55) | PORTED |

**Verdict: PORTED** (enchant identity unverified).

---

### SpiderDriver → SpiderDriver

| Feature | ORIGINAL | PORT | Status |
|---|---|---|---|
| Stats | extends EntitySpider (vanilla spider stats); higher armor while mounted (SpiderDriver.java orig :96-…) | `Spider.createAttributes()` unchanged (port :39-41); no mounted-armor bonus | **PARTIAL** |
| AI goals | Seeks free SpiderRobot, mounts it, attacks nearby mobs **from the mount** | Mount-seek loop 1/5 (25×15×25 search, ride within (4+w/2)²) (port :54-65); when mounted only *looks at* targets 1/4 (:67-72) | **PARTIAL** — mounted *attack* never executed |
| Attack | Melee + Poison 60 ticks, 1/2 roll (orig :89-92) | None (no doHurtTarget path / no poison) | **MISSING** |
| Drops | Vanilla spider drops (string/eye) | Loot `spider_driver.json`: string 2–5 + spider_eye 0–2 | **PORTED** |
| Spawning | Village dim monster list w20 (3-5) (BiomeGenUtopianPlains.java:292) | No biome modifier entry; checkSpawnRules allows spawn near a SpiderRobot (:105-110); config toggle exists (ModSpawnControl.java:59) | **MISSING** natural spawn |
| Sounds | Vanilla spider | Vanilla spider | PORTED |

**Verdict: PARTIAL** — mounts robots but is harmless; no poison, no natural spawn.

---

### SpiderRobot → SpiderRobot

| Feature | ORIGINAL | PORT | Status |
|---|---|---|---|
| Stats | HP 1500, atk 100, def 16 (OreSpawnMain.java:6474) | HP 500, atk 50 (also hardcoded 50.0f in doHurtTarget), armor 8, speed 0.35 (port :68-74,161) | **DIVERGENT** — HP 1500→500, atk 100→50, armor 16→8 |
| AI / rider | Complex leg IK (initLegData/updateLegs/findNewFooting); rider-controlled; stomp attack (feetFindSomethingToHit) + frontal flame/spark attack; iron-ingot heal (+100 cap) | LookAt + RandomLook only (:63-66); when ridden: auto-attack nearest target within **12 blocks** 1/15 tick (:124-135); iron-ingot heal cap 100 PORTED (:172-179); rider mount via interact (:185-189); leg animation replaced by procedural sine walk in `getRenderSpiderRobotInfo()` (:221-237) | **PARTIAL** — frontal flame attack missing; stomp replaced by generic 12-block melee (longer range than vanilla reach) |
| HUD | RenderSpiderRobotInfo HUD overlay | `client\RenderSpiderRobotInfo` exists, fed via entity (:47, 221) — plus an added **boss bar** (:49-50, 86-99) not in original | **PARTIAL** (HUD ported; boss bar is new) |
| Immunities | fire/cactus/starve; no fall damage | inWall/cactus/inFire/onFire/magic/starve blocked (:106-113); no fall damage (:116-118); clearFire each tick | **PORTED** (magic immunity added) |
| Drops | Various blocks/items (orig func_70628_a) | Loot `spider_robot.json`: iron 3–8 + string 2–5 | **DIVERGENT** |
| Sounds | robot sounds | `robotspider` idle 1/80 w/ 125t cooldown, `robotspidermount` on mount (:148-152,187) | **PORTED** |
| Spawning | Not naturally spawned (built/structure) | No spawn entries | PORTED (n/a) |

**Verdict: DIVERGENT** — stats third-ed, signature flame attack missing, boss bar added.

---

### SpitBug → EntitySpitBug

| Feature | ORIGINAL | PORT | Status |
|---|---|---|---|
| Stats | HP 100, atk 10, def 12 (OreSpawnMain.java:6490) | HP 60, atk 8, speed 0.33, follow 32 (port :61-67); xp 50 | **DIVERGENT** — HP 100→60, atk 10→8, armor 12→0 |
| AI goals | Swim, MoveThroughVillage, Wander, Watch; watercanon + melee + jump | Float, SpitBugAcidAttackGoal (Params.spitBug = 0.5,3.0,5,6,7,0,12,7), WanderALot(14), LookAt, RandomLook; HurtBy + NAT(Player) (:46-59) | **PORTED** (village move dropped) |
| Attack | `watercanon()`: 8-round Acid burst, shoot(dx, dy+dist*0.2, dz, 1.1f, 6.0f), 1/7 re-arm; melee; leap at targets | Acid burst exactly ported: 8 rounds, 1.1f/6.0f, lift 0.2, 1/7 re-arm, ≤20 blk gate (SpitBugAcidAttackGoal.java:35-58); jump boost 0.75 + forward (port :110-121); knockback 0.5/0.1 (:124-140) | **PORTED** |
| Immunities | cactus + fall immune | Neither (hurt only has 15-tick i-frames :142-153) | **MISSING** |
| Drops | Amethyst nuggets (orig) | Loot `spit_bug.json`: slime_ball 1–3 | **DIVERGENT** |
| Spawning | addSpawn ambient swamp w6 (1-2) (OreSpawnMain.java:4891); Island/Crystal dims w2 (1-3) | BMOD `hostile_spit_bug`: swamp+mangrove w6 (1-2) | **PORTED** (dims dropped) |
| Sounds | clatter / crunch / emperorscorpion_death | Same ids, vol 0.75 (:83-108) | **PORTED** |

**Verdict: PARTIAL** — signature acid burst faithfully restored; stats/drops/immunities diverge.

---

### Spyro → EntitySpyro

| Feature | ORIGINAL | PORT | Status |
|---|---|---|---|
| Stats | HP 200, atk 5 (hardcoded; Spyro.java mygetMaxHealth=200) | HP 200, atk 5, speed 0.3 (port :90-95); xp 35 | **PORTED** |
| Taming | Beef (field_151082_bd), 1/2 chance, full heal (orig :219-248); untame w/ **dead bush** (:250-265); fireballs ON w/ flint&steel (:301-315), OFF w/ **ice block** (:266-280); **diamond → evolves to tamed Dragon** (:281-300); name-tag rename (:316-325); sit toggle | Beef 1/2 + heal PORTED (:179-195); flint&steel ON PORTED (:197-203); OFF via **water bucket** (:205-210); sit toggle PORTED; dead-bush untame, diamond→Dragon evolution, name-tag rename all absent | **PARTIAL** — evolution + untame + rename missing; extinguisher item changed |
| AI / flight | EntityTameable, flying via do_movement/currentFlightTarget, activity states, fireball (EntitySmallFireball) when lit, owner-follow incl. flying owner ×1.75/×3.5 speed | Full activity/flight port incl. ownerFlying speedFactor 1.75/3.5 (:253-359); fireball within 64 blk², rolls 1/10‖1/15 (:295-298); retreat when HP<25 % (:278-284); goals Float/Avoid/FollowOwner/Tempt(beef)/Panic/LookAt/Stroll/RandomLook + 4 target goals (:74-88) | **PORTED** |
| Drops | Apple on death (orig func_146068_u) | Loot `spyro.json`: blaze_powder 1–3 | **DIVERGENT** |
| Spawning | Island/Crystal/Mining dims (BiomeGenUtopianPlains; mining w1) | BMOD `companion_spyro__is_badlands/is_mountain` w1 (1-1); config toggle | **DIVERGENT** domain |
| Sounds | roar (flying) / duck_hurt / cryo_death | Same, ambient only when flying (:416-433) | **PORTED** |

**Verdict: PARTIAL** — flight/taming core solid; Dragon evolution chain missing.

---

### StinkBug → EntityStinkBug

| Feature | ORIGINAL | PORT | Status |
|---|---|---|---|
| Stats | HP 5, atk 0 (StinkBug.java) | HP 5, atk 0, speed 0.15 (port :52-57); xp 2 | **PORTED** |
| AI goals | Swim, Mate, Panic, Avoid(Player), Watch, Wander | Float(0), Breed(1), Panic(4), Avoid Player 4f (5), LookAt(6), WanderALot(8), RandomLook(9) (:41-50) | **PORTED** |
| Death gas | Applies **Poison** to entities within ~8 blocks on death | Applies **Hunger 300t** to LivingEntities in 8×5×8 on death (:82-95) | **DIVERGENT** — Poison → Hunger |
| Food | fish + CrystalApple | apple (:107-109) | **DIVERGENT** |
| Drops | MyDeadStinkBug | Loot `stink_bug.json`: dead_stink_bug ×1 | **PORTED** |
| Spawning | addSpawn ambient: forest 10(2-4), jungle 8(2-4), taigaHills 6(2-4), jungleHills 4(2-4), savanna 8(2-5) (OreSpawnMain.java:4894-4898); chaos dim w3 | BMOD swarm_stink_bug: forest-group/jungle/taiga w8 (2-4); placement Animal rules (ModEntityAttributes.java:289) | **PARTIAL** — biome set approximated, weights flattened to 8 |
| Sounds | death `orespawn:fart` | Same (:71-74) | **PORTED** |

**Verdict: PARTIAL** — faithful except gas effect and food swapped.

---

### Stinky → EntityStinky

| Feature | ORIGINAL | PORT | Status |
|---|---|---|---|
| Stats | HP 100, atk 10 (hardcoded) | HP 100, atk 10, speed 0.3 (port :94-99); xp 35 | **PORTED** |
| Taming | Beef 1/2 (orig :163); untame dead bush (:194); sit toggle | Beef 1/2 + heal (:197-213); sit toggle (:215-223); untame missing | **PARTIAL** |
| Item production | Front "burp": **coal** (orig :337); rear drop by 19 skin variants: blaze powder, rotten flesh, melon seeds, uranium nugget, wheat, reeds, torch, emerald, gold ingot, leaves, titanium nugget, apple seed, diamond, sand, cobble, bone, string, cherry seed, peach seed (orig :342-396) | Front: **bone** 1/1750 (:153-155); rear 1/2000 by 19 skins: diamond, chicken, iron, gold nugget, cookie, cake, flower pot, poisonous potato, gold ingot, sand, copper, apple, emerald, gravel, cobble, name tag, iron pickaxe, berries, melon (:396-420) | **DIVERGENT** — both lists rewritten |
| Flight / AI | Tameable flier, do_movement, skin randomization, flowers? (orig eats nothing) | Activity flight port w/ ownerFlying 1.75/3.5 (:291-378); skin re-roll 1/2000 (:174-176); **added** flower-eating heal (:266-289) | **PORTED**+ (flower eating is new) |
| Drops on death | Beef (orig :254,263) | Loot `stinky.json`: **empty** | **MISSING** |
| Spawning | addSpawn: hell monster w2; mesa-variant ambient w1 ×3 (OreSpawnMain.java:4805-4808); island dim w2 (BiomeGenUtopianPlains.java:167) | BMOD companion_stinky forest/taiga w1 + dim_islands w2 (1-2) | **DIVERGENT** — nether spawn dropped; island ported |
| Sounds | duck_hurt / cryo_death | Same, vol 0.6 (:474-489) | **PORTED** |

**Verdict: PARTIAL/DIVERGENT** — alive and flying, but its item-economy (the whole point of Stinky) is rewritten and death drop missing.

---

### SunspotUrchin (projectile)

| Feature | ORIGINAL | PORT | Status |
|---|---|---|---|
| Damage | 3 (6 vs Creeper); ignites target 5; self always on fire; spins | 3 / 6 creeper; player-immune; ignite 5 s; self-ignite 1 s/tick; spin 30°/tick (port :16-69) | **PORTED** |
| Block impact | Places `Blocks.fire` at impact point (orig) | No fire placement — only smoke particles + discard (:48-58) | **MISSING** |

**Verdict: PARTIAL** — entity damage ported, incendiary block effect missing.

---

### Termite → EntityTermite

| Feature | ORIGINAL | PORT | Status |
|---|---|---|---|
| Stats | HP 5, atk 2, size 0.2×0.2; extends EntityAnt | HP 5, atk 2, speed 0.2 (port :58-63); xp 1; extends EntityAnt | **PORTED** |
| AI | Panic, attack-on-collide players (1 dmg, throttled), wander; eats wood → dirt; right-click teleports player (empty inv) to Crystal dim / back | Panic(0), MeleeAttack(1), WanderALot(2), NAT(Player) (:51-56); collide-attack 1/15 for 1.0 dmg w/ 20t delay (:66-87); wood scan radius 8 → eats: 2/3 dirt, 1/3 air, mobGriefing-gated, heals 1 (:190-214); teleport via `getTargetDimension()=CRYSTAL` on EntityAnt base (:46-48) | **PORTED** (wood→air variant + griefing gate are new details) |
| Wood set | "wood" blocks generic | 16 explicit blocks: logs/planks/fence/bookshelf/crafting table/chest (:89-98) | PORTED (approx) |
| Drops | none | Loot `termite.json` empty | **PORTED** |
| Spawning | No addSpawn (nest/structure-driven) | No biome modifier; config toggle (ModSpawnControl.java:68); cluster gate ≤4 in 20×10×20, y≥50 (:218-223) | **UNVERIFIED** — neither side has natural spawn data confirmed; port structure spawning not checked in this slice |
| Sounds | none custom | none custom | PORTED |

**Verdict: PORTED** (spawn pathway unverified).

---

### TerribleTerror → EntityTerribleTerror

| Feature | ORIGINAL | PORT | Status |
|---|---|---|---|
| Stats | HP 10, atk 5, def 3 (OreSpawnMain.java:6520) | HP 20, atk 5, speed 0.1 (port :39-44); xp 10 | **DIVERGENT** — HP 10→20, armor 3→0 |
| AI | Random flight, attacks nearby mobs/players at close range; despawns at day | No goals; custom flight target loop (1/100 retarget, 0.4/0.7 accel) (:87-135); attack 1/9 scan 12×8×12, hit if dist²<6 (:113-121); day despawn 1/400 (:58-63) | **PORTED** |
| Targeting | excludes ghosts etc. | excludes TerribleTerror/RockBase/EnderReaper/CloudShark/Rotator/PitchBlack/CreepingHorror/Islands (:137-153) | PORTED (approx) |
| Drops | 1/3 each: rotten flesh / emerald / feather (orig TerribleTerror.java:313-322) | Loot: bone 1–2 + leather 0–1 + feather 0–1 | **DIVERGENT** — emerald gone |
| Spawning | Island dim monster w25 (3-6) (BiomeGenUtopianPlains.java:182); chaos w4 (2-6) (:412) | BMOD `add_overworld_monsters` w4 (1-2) — now spawns in the **vanilla overworld** | **DIVERGENT** domain |
| Sounds | terribleterror_living/_hit/_dead | Same ids, vol 0.45 (:166-187) | **PORTED** |

**Verdict: DIVERGENT** — flight/attack ported; stats, drops, spawn domain differ.

---

### ThunderBolt (projectile)

| Feature | ORIGINAL | PORT | Status |
|---|---|---|---|
| Damage | 40 to hit entity, sets on fire, ignores "royalty" entities | 40 total (2×20 split thrown+mobAttack), ignite 1 s (port :19,46-59); royalty exemption absent | **PARTIAL** |
| Impact | Explosion + EntityLightningBolt summon | Explosion power 3 (mobGriefing-gated) + lightning (:62-79) | **PORTED** (orig power unverified) |
| Visuals | firework spark trail | FIREWORK ×4/tick, LESS_LAG gated (:82-94) | **PORTED** |

**Verdict: PORTED** (royalty exemption missing — minor).

---

### TRex → TRex

| Feature | ORIGINAL | PORT | Status |
|---|---|---|---|
| Stats | HP 160, atk 22, def 14 (OreSpawnMain.java:6479); size 2.0×4.2 | HP 200, atk 30, KB-resist 0.8, follow 40, speed 0.38 (port :57-64); xp 150 | **DIVERGENT** — HP 160→200, atk 22→30, armor 14→0 |
| AI goals | Swim, MoveThroughVillage, WanderALot, Watch, LookIdle + customServerAi melee | Float, DinosaurMeleeAttackGoal `Presets.trex()` (1.25,4.0,5,4,5,0,20,6) (:48), Stroll, LookAt, RandomLook; HurtBy + NAT(Player) (:46-55) | **PORTED** (village move dropped) |
| Attack | Melee; knockback; cactus immune; **no taming/riding in 1.7.10** (task prompt incorrect) | Knockback 1.2/0.1 ×2 player (:140-154); cactus immune (:157-163); no taming/riding | **PORTED** |
| Drops | TRexTooth, bone, UraniumNugget, TitaniumNugget | Loot `trex.json`: tooth weighted 60/30/10 ×1/2/3 + name_tag + 7 beef + gold/iron nuggets 2–5 + xp bottle + diamond 4–7; code adds bone (:131-135) | **DIVERGENT** — uranium/titanium gone; beef/diamonds/name tag added |
| Spawning | No overworld addSpawn; Island/Crystal w1, Mining dim (BiomeGenUtopianPlains.java:496) | BMOD trex badlands+savanna w1 (1-1) **and** add_overworld_monsters w1 — all-overworld now | **DIVERGENT** domain |
| Sounds | trex_living / alo_hurt / trex_death (orig :98-108) | **RAVAGER_ROAR / RAVAGER_HURT / RAVAGER_DEATH** vanilla (port :88-103) | **DIVERGENT** |

**Verdict: DIVERGENT** — fight loop ported; numbers, custom sounds, and loot identity all changed.

---

### Triffid → EntityTriffid

| Feature | ORIGINAL | PORT | Status |
|---|---|---|---|
| Stats | HP 100, atk 20, def 12 (OreSpawnMain.java:6502) | HP 100, atk 8, speed 0.13 (port :52-57); xp 50 | **DIVERGENT** — atk 20→8, armor 12→0 |
| OpenClosed shell | DataWatcher 21; takes **no damage while OpenClosed==0** | DATA_OPEN_CLOSED + 300-tick hurt lockout; invulnerable while closed incl. isInvulnerableTo mirror, bypass tags honored (:139-182); open rolls 1/80→1/8 (:199-205) | **PORTED** (lockout duration not verified against orig timer) |
| Attack | Melee 20 when open; pathing toward hostiles; "spore attack" per prompt **not present in source** — melee only | 1/10 scan 10×8×10, open + face + hit if dist²<25 (:207-224) | **PORTED** (with atk 8) |
| Immunities | cactus + fall immune | Neither ported (no hurt-source filter, no fall override) | **MISSING** |
| Drops | GreenGoo, bone | Loot `triffid.json`: green_goo 4–9 + name_tag + vine 2–5; code 1/3 poisonous potato (:228-233) | **PARTIAL** — bone missing, vine/name_tag/potato added |
| Spawning | No addSpawn found (spawner/dim driven) | BMOD `add_overworld_monsters` w4 (1-2) | **UNVERIFIED** vs **added** overworld spawn |
| Sounds | triffid_living/_hit/_dead | Same (:87-104) | **PORTED** |

**Verdict: PARTIAL** — shell mechanic well ported; attack power third-ed, immunities gone.

---

### TrooperBug → EntityTrooperBug

| Feature | ORIGINAL | PORT | Status |
|---|---|---|---|
| Stats | HP 200, atk 20, def 15 (OreSpawnMain.java:6489) | HP 200, atk 16, speed 0.4, follow 32 (port :64-70); xp 150 | **DIVERGENT** — atk 20→16, armor 15→0 |
| AI / leap | Jump-at-target with knockback; **spawns SpitBugs when attacking** | Float, TrooperBugLeapAttackGoal (leap 1/10 between 4–8 blk, on ground) (:49-62, TrooperBugLeapAttackGoal.java:21-42), jump boost 1.15 + pos +1.5 (:113-128); SpitBug-summon **absent** | **PARTIAL** — leap ported, minion summon missing |
| Attack | Melee + knockback; cactus/fall immune | Knockback 1.8/0.2 ×2 player (:139-155); 20-tick i-frames; no cactus/fall immunity | **PARTIAL** |
| Drops | MyJumpyBugScale, bone, MyAmethyst, enchanted Amethyst tools/armor | Loot `trooper_bug.json`: jumpy_bug_scale + name_tag + amethyst_gem 2–6 + amethyst gear pool (1–5 rolls, unenchanted); code adds name_tag again (:180-184) | **PARTIAL** — gear unenchanted, bone missing, double name_tag |
| Spawning | addSpawn ambient swamp w3 (1-2) (OreSpawnMain.java:4887); savanna-mesa w1; island/crystal dims w1 | BMOD `hostile_trooper_bug`: swamp+mangrove w3 (1-2) | **PORTED** (dims dropped) |
| Sounds | clatter / crunch / emperorscorpion_death | Same, vol 1.5 (:86-110) | **PORTED** |

**Verdict: PARTIAL** — signature leap ported; SpitBug summon, immunities, enchanted loot missing.

---

### Tshirt → EntityTshirt

| Feature | ORIGINAL | PORT | Status |
|---|---|---|---|
| Stats | HP 1, atk 0, size 4×4, passive | HP 1, speed 0.0, atk 0 (port :24-28); xp 40 | **PORTED** (xp value unverified orig) |
| AI | none (passive statue-like) | No goals registered | **PORTED** |
| Drops | string | code drop: **leather** (:58-62); loot table empty | **DIVERGENT** |
| Spawning | Village dim w2 (1-1) (BiomeGenUtopianPlains.java:324); night-only + no other Tshirts nearby gate | BMOD `dim_village_locals` w2 (1-1); night/no-buddy gates absent | **PARTIAL** |
| Sounds | none | none | PORTED |
| Wearable? | Not wearable in 1.7.10 (drop-only mob) | n/a | PORTED |

**Verdict: PARTIAL** — drop item wrong (string→leather), spawn gates missing.

---

### UltimateArrow (projectile)

| Feature | ORIGINAL | PORT | Status |
|---|---|---|---|
| Damage | Scales with `OreSpawnMain.UltimateBowDamage` config (default 10, clamp 2–20, OreSpawnMain.java:1519-1529) × arrow velocity | Flat `setBaseDamage(12.0)` (port UltimateArrow.java:12-21) | **DIVERGENT** — config removed, base 10→12 |
| Fire / KB | Can ignite targets, custom knockback | None | **MISSING** |
| Tame-exempt | Skips player-owned tameables | None | **MISSING** |
| Particles | Trail particles | None | **MISSING** |

**Verdict: PARTIAL** — exists and registered (ModEntities.ULTIMATE_ARROW) but is a bare vanilla arrow with custom base damage.

---

### UltimateFishHook → UltimateFishHook

| Feature | ORIGINAL | PORT | Status |
|---|---|---|---|
| Fishing pools | Custom weighted lists: vanilla treasure/junk/fish + `orespawn_fish` (BlueFish, PinkFish, RockFish, WoodFish, GreyFish) + `orespawn_lava_fish` (SunspotUrchin, LavaEel, SunFish, SparkFish, FireFish) (UltimateFishHook.java orig :422-449) | Vanilla `FishingHook` subclass — vanilla loot only (port UltimateFishHook.java:9-17) | **MISSING** |
| Lava fishing | Fishing in lava yields lava-fish list (orig :431-434) | Not possible (vanilla hook burns/no lava logic) | **MISSING** |
| Stats boost | Custom wait timers, reel-from-distance pull (orig :384-420) | luck+3 / lure+2 passed to super (:14-16); rod self-enchants Luck of Sea 3 + Lure 2 (UltimateFishingRod.java:24-29) | **PARTIAL** |

**Verdict: PARTIAL/MISSING** — custom fish economy (incl. all five lava fish) absent.

---

### Urchin → Urchin

| Feature | ORIGINAL | PORT | Status |
|---|---|---|---|
| Stats | HP 25, atk 10, def 4 (OreSpawnMain.java:6484); fire-immune (field_70178_ae=true) | HP 30, atk 8, speed 0.3 (port :33-35); **not fire-immune** | **DIVERGENT** — HP 25→30, atk 10→8, fire immunity missing |
| AI / attack | Swim/Wander/Watch; contact damage + ignite | Float/WanderALot(14)/LookAt/RandomLook/HurtBy (:43-49); attack loop 1/8, range²<8, swing 1/7, ignite 5 s (:73-76,115-141); odd port-only self-`doHurtTarget(this)` in water (:100-102) | **PORTED** (self-hit-in-water is a port quirk) |
| Drops | MyCrystalPinkIngot, MyCrystalApple | Loot `urchin.json`: 33 % crystal_pink_ingot + 33 % crystal_apple | **PORTED** |
| Spawning | Island w15 (2-4) / Crystal w2 (1-5) dims; night spawner | BMOD `add_ocean_spawns` w6 (1-2); rules: time≥13000 only (:168-171); day despawn 1/400 (:106-112) | **DIVERGENT** domain |
| Sounds | kyuubi_living / glasshit / glassdead | Same, vol 1.1 (:144-165) | **PORTED** |

**Verdict: PARTIAL/DIVERGENT** — behavior ported, fire immunity and spawn domain lost.

---

### VelocityRaptor → VelocityRaptor

| Feature | ORIGINAL | PORT | Status |
|---|---|---|---|
| Stats | HP 10 (20 tamed), atk 2, speed 0.55 | HP 10, atk 2, speed 0.55 (port :73-78); no tamed HP bump | **PARTIAL** — tamed 20-HP missing |
| Taming | Apple (field_151034_e), random roll (orig :232); untame dead bush (:264); name tag (:282) | Apple 1/2 + full heal (:151-165); untame + rename missing | **PARTIAL** |
| Riding | **Not rideable** in 1.7.10 (EntityCannonFodder tameable) | Fully rideable: getControllingPassenger/tickRidden/getRiddenSpeed ×1.6 (:188-227) | **DIVERGENT** — riding is a port addition |
| AI | Mate/FollowOwner/Avoid/Tempt(apple)/Panic/Wander; eats plants (tallgrass/dandelion/poppy/deadbush/double-plant → air, heals) (orig :114-194); reduced fall dmg | Same goal set (:57-71); plant set = dandelion/poppy/red_tulip/oak_leaves/hay (:96-100), mobGriefing-gated, heal 2; fall dmg cap 2 over 3 blocks (:86-94); incoming damage capped at 10 (:81-83) | **PORTED** (plant list shifted; dmg-cap added) |
| Drops | Poppy (orig :335) | Loot `velocity_raptor.json`: bone 1–3 | **DIVERGENT** |
| Spawning | Jungle addSpawn? not found; Island/Crystal/Mining dims | BMOD companion_velocity_raptor jungle/savanna w2 (1-2) + add_overworld_creatures w4 (1-2); rules y≥50 + sky (:261-264) | **DIVERGENT** domain |
| Sounds | cryo_hurt / cryo_death (orig :314-318) | Same, vol 0.4 (:238-249) | **PORTED** |

**Verdict: PARTIAL** — tames and eats plants; riding invented, drops changed.

---

### Vortex → EntityVortex

| Feature | ORIGINAL | PORT | Status |
|---|---|---|---|
| Stats | HP 150, atk 26, def 10 (OreSpawnMain.java:6500); fire-immune | HP 200, atk 20, speed 0.35 (port :60-65); xp 200; **not fire-immune** | **DIVERGENT** — HP 150→200, atk 26→20, fire immunity missing |
| Pull mechanic | Continuous velocity pull (func_70024_g) toward itself, vertical ×2 for players | Pull within 9 blk: strength (10−d)×0.1 horiz, ×0.05 vert (×2 player), 20t winded-cooldown after being hit (:176-197) | **PORTED** |
| Attack | Melee 26 + drag; particles | Melee 1/8 in (4+w/2) range **plus new `skywardLaunch`** (+4.0 up, 30t cooldown) (:190-196, 244-274) | **DIVERGENT** — launch is a port invention; atk lowered |
| Flight | random flight, stuck detection | flight retarget 1/300 or stuck>30 (:156-174); smoke spiral particles (:101-115) | **PORTED** |
| Drops | VortexEye, bone, ingots/gems | Loot `vortex.json`: vortex_eye + xp bottle + gunpowder 3–8 + gold 1–3 | **DIVERGENT** — bone gone, gunpowder added |
| Spawning | Night overworld + Island w3 (1-2)/Crystal w1/Chaos dims (BiomeGenUtopianPlains.java:226,406) | BMOD `add_nether_spawns` w4 — spawns in the **Nether** only; day-despawn 1/500 (:121-126) | **DIVERGENT** — wrong dimension entirely |
| Sounds | vortexlive (living + death) | Same both (:74-88) | **PORTED** |

**Verdict: DIVERGENT** — pull ported, but stats, fire immunity, spawn dimension and a new launch attack all diverge.

---

### WaterBall (projectile)

| Feature | ORIGINAL | PORT | Status |
|---|---|---|---|
| Damage | 2 (5 vs Creeper); 0 vs WaterDragon/AttackSquid/mounted players; drops MyWaterBall item on hit | 2 / 5 creeper; mounted-player skip PORTED (:48); WaterDragon/AttackSquid exemption **missing**; item drop **missing**; extinguishes fire on target (added) (port :44-52) | **PARTIAL** |
| Visuals/sounds | splash/bubble particles + sound | Bubble+splash ×8, GENERIC_SPLASH 0.5 (:55-72); spin 30°/tick | **PORTED** |

**Verdict: PARTIAL**.

---

### WaterDragon → WaterDragon

| Feature | ORIGINAL | PORT | Status |
|---|---|---|---|
| Stats | HP 150, atk 20, def 8 (OreSpawnMain.java:6492); water speed switch | HP 200, atk 20 (hardcoded 20 in doHurtTarget), water 0.55/land 0.25 (port :56-59) | **DIVERGENT** — HP 150→200, armor 8→0 |
| Taming | Fish, 1/3 roll (orig WaterDragon.java:115); name-tag rename (:158); **not rideable** | Cod 1/3 + heal (:163-182); glass-block untame (:184-195); name-tag (:197-202); sit toggle; breeding added w/ owner inheritance (:347-355); not rideable | **PORTED**+ (untame/breeding additions unverified vs orig) |
| Attacks | Melee + **ranged WaterBall and EntitySmallFireball volleys** (orig :624-632) | Melee only via DinosaurMeleeAttackGoal `Presets.waterDragon()` (1.0,4.0,5,4,0,200,14,5); knockback 1.1/0.14 (:214-225); **no projectiles** | **PARTIAL** — both ranged attacks missing |
| AI | Tameable water creature, scan_it water-seek | Float/Breed/FollowOwner/Tempt(cod)/Dinosaur melee/RandomSwim/WanderALot/LookAt/RandomLook + HurtBy (:81-93); WaterBound nav + SmoothSwimming (:70-72); water-scan + dryOut 1/50 (:256-275) | **PORTED** |
| Drops | MyWaterDragonScale, bone, raw fish, enchanted tools/armor | Loot `water_dragon.json`: scale + name_tag + amethyst 2–5 + cod 9–14 + ultimate/iron gear pool; code adds heart_of_the_sea + 9–14 cod + 1/3 diamond (:310-315) | **DIVERGENT** — double drops; ultimate tools added; bone gone |
| Spawning | addSpawn waterCreature: river w5, swamp 3, ocean 2, deepOcean 2 (OreSpawnMain.java:4844-4847) | BMOD water_dragon river/ocean/deep_ocean/swamp+mangrove all w2 | **PARTIAL** — river 5→2, swamp 3→2 |
| Sounds | waterdragon_hurt / waterdragon_death (orig :249-253) | Same (:322-331) | **PORTED** |

**Verdict: PARTIAL** — solid tame/swim port; signature WaterBall/fireball volleys missing, stats inflated.

---

### Whale → Whale

| Feature | ORIGINAL | PORT | Status |
|---|---|---|---|
| Stats | HP 100, speed 0.35, atk 0, xp 40 (Whale.java orig :44-62,110-112) | HP 100, 0.35, atk 0 (port :63-68); xp 40 | **PORTED** |
| AI goals | Swim(0), Mate(1), Tempt fish 1.2(2), Panic 1.5(4), Watch 12(5), MyWander(6), LookIdle(7) (orig :48-54) | Float(0), Breed(1), Tempt cod 1.2(2), Panic 1.5(4), LookAt 12(5), MyEntityAIWander(6), RandomLook(7) (port :53-61) | **PORTED** — 1:1 priorities |
| Spray | spray_timer 250+rand250 → spray 25+rand25; 20 bubble/splash particles per tick (orig :69-99) | Identical constants (:74-107) | **PORTED** |
| Water-seek | scan_it radius 11/cap 4, dryOut −4 HP @1/25, heal+splash 1/50 in water (orig :215-252) | Same constants (dryOut 4.0 dmg 1/25) (:128-159) | **PORTED** |
| Drops | 20–44 raw fish (orig :143-150) | Loot `whale.json`: cod 20–44 | **PORTED** |
| Spawning | addSpawn deepOcean waterCreature w1 (1-2) (OreSpawnMain.java:4706); gates: y≥50, daytime, 1/50 roll, no buddies in 32×8×32 (orig :260-271) | BMOD ocean w2 (1-1) + IN_WATER placement; rules y≥50, day(<13000), 1/50, ≤0 buddies (:198-205) | **PORTED** (weight 1→2, ocean vs deepOcean) |
| Sounds | living "splash", hurt little_splat, death big_splat, vol 0.9 pitch 0.5 | GENERIC_SPLASH / little_splat / big_splat, 0.9/0.5 (:209-233) | **PORTED** |
| Breeding | Crystal apple food | crystal_apple (:236-238) | **PORTED** |

**Verdict: PORTED** — the most faithful port in this slice.

---

### WormSmall → EntityWormSmall

| Feature | ORIGINAL | PORT | Status |
|---|---|---|---|
| Stats | HP 10, atk 3, def 0 (OreSpawnMain.java:6504); speed 0.1; xp 0; noClip | HP 10, atk 3, speed 0.1 (port :30-35); xp 0; noPhysics | **PORTED** |
| Burrow cycle | Player within 8: while `upcount>0` decrement, at 0 set `downcount=100+rand(150)`; rise +0.15 vel/+0.1 y while block at y+0.25 non-air (dies in non-dirt/grass/stone); when down: `downcount--`, then `upcount=25+rand(50)`; sink check at y+2 (+0.2/+0.05); no player: `upcount=rand(50)`; gravity −0.01, x/z zeroed, y-vel ×0.75 (orig WormSmall.java:90-159) | Same structure & constants: up 25+rand50, down 100+rand150, initial 50, rise +0.15/+0.1 & +0.2/+0.05, idle +0.1/+0.05, gravity −0.01, ×0.75 damping (port :73-132) | **PORTED** — cycle constants exact |
| Attack | Within 1.5: 1/15 swing; **1/6 chance to rip off boots** (slot 1), damage them durability/20, throw on ground (orig :179-197) | Within 1.5: 1/15 swing (:135-146); **boot-stealing missing** | **PARTIAL** |
| inWall immunity | yes (orig :221-228) | yes (:158-163) | **PORTED** |
| Surface block check | dies if rising through non-grass/dirt/stone | Missing (rises through anything) | **MISSING** detail |
| Drops | none | Loot `worm_small.json`: dirt 0–2 | DIVERGENT (minor) |
| Spawning | No addSpawn; only spawned by WormLarge; spawn rule = night only (orig :214-216) | BMOD `add_overworld_creatures` **CREATURE** w10 (1-2) — natural daytime overworld spawning added; ON_GROUND Monster placement rules (ModEntityAttributes.java:215) | **DIVERGENT** — now common natural spawn, night gate gone |
| Sounds | hurt little_splat, no living/death | Same (:42-57) | **PORTED** |

**Verdict: PARTIAL** — burrow cycle exact; boot-theft and spawn discipline lost.

---

### WormMedium → EntityWormMedium

| Feature | ORIGINAL | PORT | Status |
|---|---|---|---|
| Stats | HP 30, atk 10, def 8 (OreSpawnMain.java:6505) | HP 30, atk 6, speed 0.1 (port :32-37) | **DIVERGENT** — atk 10→6, armor 8→0 |
| Burrow cycle | Same as small but up=25+rand(75), rise +0.2/+0.1, sink check y+3, damping ×0.65; defers to nearby WormSmall (orig :88-164) | Same constants incl. WormSmall defer (8-blk scan), up 25+rand75, ×0.65 (:75-143) | **PORTED** |
| Attack | 1/15 swing within 2.25; steals **boots or leggings**, durability/15 (orig :193-222) | 1/15 swing within 2.25 (:146-163); armor theft **missing** | **PARTIAL** |
| Drops | 2 rotten flesh + 2 leather (+rotten flesh pickItem) (orig :256-273) | Code: 2 rotten flesh + 2 **string** (:183-191); loot adds bone 1–2 + rotten 1–2 | **DIVERGENT** — leather→string + extra loot table |
| Spawning | Only via WormLarge / night rule | No natural spawn (correct — only WormLarge summons); ON_GROUND placement registered anyway | **PORTED** |
| Sounds | little_splat / big_splat | Same (:50-60) | **PORTED** |

**Verdict: PARTIAL**.

---

### WormLarge → EntityWormLarge

| Feature | ORIGINAL | PORT | Status |
|---|---|---|---|
| Stats | HP 90, atk 18, def 14 (OreSpawnMain.java:6506); xp 2050; speed 0.2 | HP 100, atk 15, speed 0.2 (port :46-51); xp 2050 (:34) | **DIVERGENT** — HP 90→100, atk 18→15, armor 14→0 |
| Minion spawn | Spawns 20× Small Worm + 20× Medium Worm once (`wormsSpawned` NBT) (orig :164-171, 323-331) | Identical: 20+20, NBT-persisted (:133-148, 199-208) | **PORTED** |
| AI / surface logic | Swim, MoveThroughVillage, WanderALot(16), Watch, LookIdle (orig :46-50); surfaces toward player, noClip toggling, damping ×0.85 | Float, Stroll, LookAt, RandomLook (:39-44); surface/noPhysics logic + ×0.85 (:89-131); chase + hit 1/10 within 3 blocks (:158-173) | **PORTED** (village move dropped) |
| Armor theft | Steals helmet (slot4) / chestplate (slot3) 1/4, and held item (slot0) 1/4, durability/10 (orig :206-239) | **Missing** | **MISSING** |
| Drops | WormTooth, painting, 6 rotten, 6 leather, 8 dirt, 16 gold nuggets, 5 diamond, 4 uranium nugget, 4 titanium nugget (orig :352-377) | Loot `worm_large.json`: worm_tooth, **saddle**, rotten 3–6, leather 3–6, dirt 4–8, gold nuggets 8–16, diamond 2–5, uranium 2–4, titanium 2–4; code **adds nether_star** + 6 rotten + 6 string + 16 spider_eye + 5 diamond (:211-226) | **DIVERGENT** — double drops; nether star/saddle/spider eyes invented; painting gone |
| Spawning | addSpawn creature: plains w25, savanna w15, savannaPlateau w10 (1-1) (OreSpawnMain.java:4631-4633); strict ground-solidity + no-other-WormLarge-in-32 + y≥50 + spawner checks (orig :263-309) | **No biome modifier entry** — never spawns naturally (only ON_GROUND placement registered, ModEntityAttributes.java:219) | **MISSING** |
| Sounds | hurt big_splat, death alo_death | Same (:64-74) | **PORTED** |
| inWall immunity | yes | yes (:191-196) | **PORTED** |

**Verdict: PARTIAL** — burrow boss loop and minion swarm ported; never spawns naturally and drops a nether star.

---

## Summary table

| Entity | Stats | AI | Attacks | Drops | Spawning | Sounds | Overall |
|---|---|---|---|---|---|---|---|
| Scorpion | DIVERGENT | PARTIAL | PARTIAL | DIVERGENT | PARTIAL | PORTED | DIVERGENT |
| SeaMonster | DIVERGENT | PORTED | PORTED | DIVERGENT | DIVERGENT | PORTED | PARTIAL |
| SeaViper | DIVERGENT | PORTED | DIVERGENT | DIVERGENT | PORTED | PORTED | DIVERGENT |
| Shoes | n/a | n/a | PARTIAL | n/a | n/a | PORTED | PARTIAL |
| Skate | DIVERGENT | PORTED | PORTED | DIVERGENT | DIVERGENT | PORTED | DIVERGENT |
| Slice (item) | n/a | n/a | PORTED | n/a | n/a | n/a | PORTED |
| SpiderDriver | PARTIAL | PARTIAL | MISSING | PORTED | MISSING | PORTED | PARTIAL |
| SpiderRobot | DIVERGENT | PARTIAL | PARTIAL | DIVERGENT | n/a | PORTED | DIVERGENT |
| SpitBug | DIVERGENT | PORTED | PORTED | DIVERGENT | PORTED | PORTED | PARTIAL |
| Spyro | PORTED | PORTED | PORTED | DIVERGENT | DIVERGENT | PORTED | PARTIAL |
| StinkBug | PORTED | PORTED | DIVERGENT | PORTED | PARTIAL | PORTED | PARTIAL |
| Stinky | PORTED | PORTED | n/a | DIVERGENT/MISSING | DIVERGENT | PORTED | PARTIAL |
| SunspotUrchin | n/a | n/a | PARTIAL | n/a | n/a | n/a | PARTIAL |
| Termite | PORTED | PORTED | PORTED | PORTED | UNVERIFIED | PORTED | PORTED |
| TerribleTerror | DIVERGENT | PORTED | PORTED | DIVERGENT | DIVERGENT | PORTED | DIVERGENT |
| ThunderBolt | n/a | n/a | PORTED | n/a | n/a | PORTED | PORTED |
| TRex | DIVERGENT | PORTED | PORTED | DIVERGENT | DIVERGENT | DIVERGENT | DIVERGENT |
| Triffid | DIVERGENT | PORTED | PARTIAL | PARTIAL | UNVERIFIED | PORTED | PARTIAL |
| TrooperBug | DIVERGENT | PARTIAL | PARTIAL | PARTIAL | PORTED | PORTED | PARTIAL |
| Tshirt | PORTED | PORTED | n/a | DIVERGENT | PARTIAL | PORTED | PARTIAL |
| UltimateArrow | DIVERGENT | n/a | PARTIAL | n/a | n/a | n/a | PARTIAL |
| UltimateFishHook | PARTIAL | n/a | n/a | MISSING | n/a | n/a | MISSING (custom fishing) |
| Urchin | DIVERGENT | PORTED | PORTED | PORTED | DIVERGENT | PORTED | PARTIAL |
| VelocityRaptor | PARTIAL | PORTED | DIVERGENT | DIVERGENT | DIVERGENT | PORTED | PARTIAL |
| Vortex | DIVERGENT | PORTED | DIVERGENT | DIVERGENT | DIVERGENT | PORTED | DIVERGENT |
| WaterBall | n/a | n/a | PARTIAL | MISSING (item) | n/a | PORTED | PARTIAL |
| WaterDragon | DIVERGENT | PORTED | PARTIAL | DIVERGENT | PARTIAL | PORTED | PARTIAL |
| Whale | PORTED | PORTED | n/a | PORTED | PORTED | PORTED | PORTED |
| WormSmall | PORTED | PORTED | PARTIAL | DIVERGENT | DIVERGENT | PORTED | PARTIAL |
| WormMedium | DIVERGENT | PORTED | PARTIAL | DIVERGENT | PORTED | PORTED | PARTIAL |
| WormLarge | DIVERGENT | PORTED | PARTIAL | DIVERGENT | MISSING | PORTED | PARTIAL |

Overall counts (31 entries): PORTED 4 · PARTIAL 16 · DIVERGENT 10 · MISSING 1.
