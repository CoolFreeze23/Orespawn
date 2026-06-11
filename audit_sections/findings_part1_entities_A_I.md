# Findings — Entities A–I

Sources: `audit_sections/01_entities_A_C.md`, `audit_sections/02_entities_D_I.md`.
Original = `reference_1_7_10_source\sources\danger\orespawn\` · Port = `src\main\java\danger\orespawn\` + `src\main\resources\data\orespawn\`.
Entries cover every audited row whose status is MISSING / PARTIAL / DIVERGENT / UNVERIFIED. Fully PORTED rows are excluded.

---

## Systemic findings

### ENT-SYS-001 — Systemic: double drops (loot-table JSON + `dropCustomDeathLoot` both fire)
- **Status:** DIVERGENT
- **Original:** each entity's `func_70628_a` — single drop source per entity
- **Port:** loot table JSON **and** a hardcoded `dropCustomDeathLoot` override both execute on death. Affected: Alosaurus, AttackSquid, Baryonyx, Basilisk, Bee, Cassowary (custom 2–4 feathers invented on top of correct chicken 2–4 JSON), CaterKiller, Cephadrome, CrystalCow, EnchantedCow (XP bottles + 20% book added), GoldCow (1–3 gold ingots added), Hammerhead (8 XP bottles + 6 bones added), HerculesBeetle (name_tag + 4–11 bones added)
- **Fix:** for each affected entity, pick ONE source: move all intended drops into the loot table JSON and delete the `dropCustomDeathLoot` override (preferred), matching the original 1.7.10 item lists/quantities. Per-entity content divergences are itemized in their own entries below.

### ENT-SYS-002 — Systemic: original `func_70601_bi` spawn-rule gates deleted (no `checkSpawnRules` in port)
- **Status:** MISSING
- **Original:** per-entity `func_70601_bi` enforced day/night, altitude (y thresholds), darkness, weather, buddy-count, clear-air, and dimension gates
- **Port:** biome-modifier JSONs exist but the rule overrides are absent (or weakened). Affected: Alien (underground y<50 & dark / dim4), Alosaurus (y>50, night, not raining, no other Alosaurus), Basilisk (night, spawner check, no buddy), BandP (night, y≥50, villager-count), Baryonyx (y>50, day, ≤8 buddies), Bee (day/clear-air/y>50), Brutalfly (y≥70, dark, night, 4×3×10 clear air, none within 64), Camarasaurus (y≥50 + day), Cassowary (day-only), CaterKiller (day, y≥50, 1-in-10 dice, leaf/air clearance, none within 48), CaveFisher (has y≤50 but darkness check dropped), Cockateil (day & (dim4 or y≥50)), Crab (day, y≥50, dim5 throttle), CreepingHorror (dark, night, dim6 or y≤15), Cryolophosaurus (dark and (night or y≤50))
- **Fix:** implement `checkSpawnRules`/`SpawnPlacements` predicates per entity reproducing the original gate list above; re-test natural spawning per entity afterwards.

### ENT-SYS-003 — Systemic: port comments falsely claim 1.7.10 parity
- **Status:** DIVERGENT
- **Original:** CaterKiller tree-eat heals 2.0 with pathing; CaterKiller timed metamorphosis spawns Brutalfly+Butterflies; Cryolophosaurus hunts proactively (1-in-5 scan over 9×2×9)
- **Port:** `entity/EntityCaterKiller.java` comments claim parity for invented heal values (5/10) and despawn-instead-of-transform; `entity/Cryolophosaurus.java` comment claims it "never had" proactive hunting (wrong)
- **Fix:** correct or delete the misleading comments when fixing the behaviors (see ENT-A-074, ENT-A-075, ENT-A-112); audit other port comments asserting parity before trusting them.

---

## Acid / LaserBall

### ENT-A-001 — Acid: inherited LaserBall entity-immunity list deleted
- **Status:** DIVERGENT
- **Original:** `LaserBall.java` — special immunities for TrooperBug/SpitBug/Robot2–5/GiantRobot when projectile is acid-type
- **Port:** `entity/LaserBall.java` — all entity immunities removed; Acid (and every LaserBall subclass) now damages those mobs
- **Fix:** restore the acid-immunity checks in the port `LaserBall` hit logic (skip damage when target is TrooperBug/SpitBug/Robot2–5/GiantRobot and acid flag set).

## Alien

### ENT-A-002 — Alien: combat stats and hitbox reduced
- **Status:** DIVERGENT
- **Original:** `OreSpawnMain.java:6491` — HP 100, atk 12, armor 8; `Alien.java` ctor — size 1.1×3.25
- **Port:** `entity/Alien.java` `createAttributes` — HP 80, armor 6; `ModEntities.java` — size 0.6×1.8
- **Fix:** set MAX_HEALTH 100, ARMOR 8 in `createAttributes`; set entity dimensions to 1.1×3.25 in `ModEntities`.

### ENT-A-003 — Alien: MoveThroughVillage goal missing; torch logic approximated
- **Status:** PARTIAL
- **Original:** `Alien.java` — `EntityAIMoveThroughVillage`; torch destruction in `func_70619_bc` scanning ≤15 blocks for torch/ExtremeTorch
- **Port:** `entity/Alien.java` — no village goal; `AlienTorchSeekGoal` adds mobGriefing check + throttle, different scan
- **Fix:** add a MoveThroughVillage-equivalent goal at original priority; align `AlienTorchSeekGoal` scan radius to 15 blocks and remove throttling beyond the original cadence.

### ENT-A-004 — Alien: hunger-effect duration no longer difficulty-scaled
- **Status:** DIVERGENT
- **Original:** `Alien.java` — Hunger for `var2*5` ticks, difficulty multipliers 6/8/10/12, 1-in-5 chance
- **Port:** `entity/Alien.java` — `MobEffects.HUNGER` fixed 30 ticks amp 0, 1-in-5
- **Fix:** compute duration as `difficultyMult * 5` ticks (mult 6/8/10/12 by difficulty) instead of fixed 30.

### ENT-A-005 — Alien: drop table replaced
- **Status:** DIVERGENT
- **Original:** `Alien.java` `func_70628_a` — gold nuggets 5–10, iron ingots 5–10, ender pearl 1, compass 1, clock 1
- **Port:** `loot_table/entities/alien.json` — gunpowder 5–10, iron 5–10, ender pearl 1–3; no compass/clock
- **Fix:** edit `alien.json`: gunpowder→gold nuggets 5–10, ender pearl count 1, add compass ×1 and clock ×1 pools.

### ENT-A-006 — Alien: spawn habitat moved from underground/Utopia to the End
- **Status:** DIVERGENT
- **Original:** `Alien.java` `func_70601_bi` — spawner tag "Alien", or dim 4 (Utopia), or underground y<50 & dark; no `addSpawn`
- **Port:** `add_end_spawns.json` — `#minecraft:is_end` weight 3, 1–1
- **Fix:** remove alien from `add_end_spawns.json`; add an overworld monster modifier plus `checkSpawnRules` for y<50 & dark, and add to the Utopia dimension spawn list.

### ENT-A-007 — Alien: jump boost invented in port
- **Status:** PARTIAL
- **Original:** `Alien.java` — no jump modification (and no fire immunity)
- **Port:** `entity/Alien.java` `jumpFromGround` — jump boost added (new behavior)
- **Fix:** delete the `jumpFromGround` override unless intentionally kept as a port feature.

## Alosaurus

### ENT-A-008 — Alosaurus: combat stats nearly halved
- **Status:** DIVERGENT
- **Original:** `OreSpawnMain.java:6473` — HP 110, atk 18, def 8
- **Port:** `entity/Alosaurus.java` `createAttributes` — HP 60, atk 15, armor 0
- **Fix:** set HP 110, ATTACK_DAMAGE 18, ARMOR 8 in `createAttributes`.

### ENT-A-009 — Alosaurus: village goal dropped, player-targeting invented
- **Status:** PARTIAL
- **Original:** `Alosaurus.java` — Swim, MoveThroughVillage, WanderALot(2,16), Watch(8), LookIdle; attack via `func_70619_bc` (1-in-5 find, ~40% swing); no standing player-target goal
- **Port:** `entity/Alosaurus.java` — no MoveThroughVillage; adds `NearestAttackableTargetGoal<Player>` and HurtBy(alertOthers)
- **Fix:** add MoveThroughVillage-equivalent; remove the always-on player target goal (original only acquired targets via its tick scan) or gate it to match original 1-in-5 acquisition.

### ENT-A-010 — Alosaurus: loot content invented (gunpowder + diamonds)
- **Status:** DIVERGENT
- **Original:** `Alosaurus.java` `func_70628_a` — 10 bones + 6 raw beef
- **Port:** `alosaurus.json` — gunpowder 5–10 + diamond 3–6; plus `dropCustomDeathLoot` 10 bones + 6 beef (double, see ENT-SYS-001)
- **Fix:** rewrite `alosaurus.json` to bone ×10 + beef ×6; delete the `dropCustomDeathLoot` override.

### ENT-A-011 — Alosaurus: spawn biomes/conditions changed
- **Status:** DIVERGENT
- **Original:** `addSpawn` multiple biomes; `func_70601_bi` y>50, night, not raining, no other Alosaurus
- **Port:** `hostile_alosaurus.json` `#minecraft:is_savanna` w1 2–3; conditions deleted (ENT-SYS-002)
- **Fix:** restore original biome list in the modifier; add spawn rules (y>50, night, !raining, no nearby Alosaurus).

## AntRobot

### ENT-A-012 — AntRobot: stats and hitbox rewritten
- **Status:** DIVERGENT
- **Original:** `OreSpawnMain.java:6475` — HP 300, atk 30, def 16; XP = health/2 = 150 (`AntRobot.java:58`); size 2.75×1.25
- **Port:** `entity/AntRobot.java:71-77` — HP 350, atk 35, armor 6; `ModEntities.java:548` size 2.0×3.0 (W/H swapped)
- **Fix:** set HP 300, ATTACK 30, ARMOR 16; dimensions 2.75 wide × 1.25 tall in `ModEntities`.

### ENT-A-013 — AntRobot: melee throttle removed (attack rate massively higher)
- **Status:** DIVERGENT
- **Original:** `AntRobot.java` `func_70619_bc:96-147` — 1-in-15 melee attempt at range <(6+w/2); melee damage = atk attribute
- **Port:** `entity/AntRobot.java:108-125` — melee fires every `customServerAiStep` tick in range; damage hardcoded 35.0
- **Fix:** gate melee behind `random.nextInt(15)==0`; read damage from ATTACK_DAMAGE attribute instead of hardcoding.

### ENT-A-014 — AntRobot: ridden 1-in-50 stomp missing
- **Status:** DIVERGENT
- **Original:** `AntRobot.java` `func_70071_h_:617-631` — when ridden: 1-in-50 stomp + 1-in-9 melee; stomp = atk/10 = 3.0
- **Port:** `entity/AntRobot.java` — ridden 1-in-9 melee only; stomp value 3.5
- **Fix:** add the 1-in-50 ridden stomp call; set stomp damage to atk/10.

### ENT-A-015 — AntRobot: drop table replaced (redstone-component loot lost)
- **Status:** DIVERGENT
- **Original:** `AntRobot.java` `func_70628_a:1112-1164` — 7–13 rolls of redstone/repeater/comparator/redstone block/dispenser/sticky piston/piston/lever/pressure plate/iron ingot
- **Port:** `ant_robot.json` — iron 3–8 + gold 1–3
- **Fix:** rewrite `ant_robot.json` as 7–13 rolls over a uniform pool of the 10 original redstone-component items.

### ENT-A-016 — AntRobot: custom ride/hover movement physics absent
- **Status:** PARTIAL
- **Original:** `AntRobot.java` `func_70636_d:659-877` — ridable with custom hover/walk physics (obstruction climb, velocity model); procedural leg animation (`updateLegs`)
- **Port:** `entity/AntRobot.java` — vanilla `startRiding` only; legs replaced with sine-wave approximation
- **Fix:** implement rider-controlled `travel()` replicating the original velocity/obstruction-climb model (compare HoverboardEntity port for pattern); restore leg animation from `updateLegs` data.

## AttackSquid

### ENT-A-017 — AttackSquid: HP tripled
- **Status:** DIVERGENT
- **Original:** `OreSpawnMain.java:6510` — HP 10, atk 8, def 0; size 1.0×1.25
- **Port:** `entity/AttackSquid.java` — HP 30; size 0.8×0.8
- **Fix:** set HP 10 in `createAttributes`; dimensions 1.0×1.25.

### ENT-A-018 — AttackSquid: melee swing odds drifted
- **Status:** PARTIAL
- **Original:** `AttackSquid.java` — melee 1-in-10 trigger, ~40% swing at distSq<9
- **Port:** `entity/AttackSquid.java` — 1-in-10 trigger, 1-in-4 (25%) swing
- **Fix:** raise swing roll to ~40% (e.g. `nextInt(5)<2`) to match original cadence.

### ENT-A-019 — AttackSquid: ranged `watercanon` attack missing
- **Status:** MISSING
- **Original:** `AttackSquid.java` — fires `InkSack` (1-in-3) or `WaterBall` (2-in-3), speed 1.4, spread 5.0
- **Port:** `entity/AttackSquid.java` — melee only
- **Fix:** add ranged attack: roll 1-in-3 InkSack else WaterBall, velocity 1.4, inaccuracy 5.0 (both projectile classes exist in port).

### ENT-A-020 — AttackSquid: drop table replaced
- **Status:** DIVERGENT
- **Original:** `AttackSquid.java` — 1–3 ink sacs; 1-in-50 enchanted gear roll; 1–3 fish
- **Port:** `attack_squid.json` — gunpowder 2–5, iron 1–3, gold 0–2@30%; plus custom 1–3 cod & 1-in-5 diamond (double, ENT-SYS-001)
- **Fix:** rewrite JSON: ink_sac 1–3, cod 1–3, 2% (1-in-50) enchanted-gear pool; delete custom death loot.

### ENT-A-021 — AttackSquid: no natural spawn; spawn rules inverted
- **Status:** MISSING
- **Original:** `addSpawn` rivers/oceans w12 6–10; `func_70601_bi`: y>50 & daylight
- **Port:** no biome modifier exists for attack_squid; `checkSpawnRules` y<50 && canSeeSky (inverted)
- **Fix:** create biome modifier for `#minecraft:is_river` + `#minecraft:is_ocean` w12 6–10; fix `checkSpawnRules` to y>50 && daylight.

### ENT-A-022 — AttackSquid: hurt() projectile exclusions missing
- **Status:** PARTIAL
- **Original:** `AttackSquid.java` — `hurt()` ignored WaterBall/WaterDragon damage from squids
- **Port:** `entity/AttackSquid.java` — exclusions absent
- **Fix:** in `hurt()`, return false when damage source is a WaterBall or WaterDragon originating from a squid.

## BandP (Burglar & Pickpocket)

### ENT-A-023 — BandP: all five stats rewritten
- **Status:** DIVERGENT
- **Original:** `OreSpawnMain.java:6480` — HP 100, atk 1, def 18 (runtime armor clamp 8–23); XP 1000; size 0.75×1.75
- **Port:** `entity/BandP.java` — HP 30, atk 5, armor 0, XP 10, size 0.6×1.0
- **Fix:** set HP 100, ATTACK 1, ARMOR 18 with the 8–23 worn-gear clamp; XP 1000; dimensions 0.75×1.75.

### ENT-A-024 — BandP: AI goals and target types pruned
- **Status:** PARTIAL
- **Original:** `BandP.java` — MoveThroughVillage, OpenDoor, MoveIndoors goals; targets Player/Villager/Girlfriend/Boyfriend (1-in-12 aggro)
- **Port:** `entity/BandP.java` — those 3 goals absent; targets Player only
- **Fix:** add MoveThroughVillage/OpenDoor/MoveIndoors equivalents; extend the 1-in-12 aggro scan to Villager, Girlfriend, Boyfriend.

### ENT-A-025 — BandP: stealing mechanic nerfed
- **Status:** DIVERGENT
- **Original:** `BandP.java` — melee steals item (armor first) into 100-slot `MymainInventory`, every hit; despawns if `got_stuff==0`
- **Port:** `entity/BandP.java` — `tryStealFromPlayer` 1-in-4 chance, 16-slot stash
- **Fix:** steal on every successful melee hit (remove 1-in-4 roll), prioritize armor slots, and expand stash to 100 slots.

### ENT-A-026 — BandP: drops nuggets→ingots
- **Status:** DIVERGENT
- **Original:** `BandP.java` — 10–14 leather; if `getWhat()==0` 2–4 Uranium+Titanium **nuggets**; all stolen items
- **Port:** `band_p.json` — leather 10–14, uranium **ingot** 0–3@50%, titanium **ingot** 0–3@50%
- **Fix:** change JSON entries to uranium_nugget / titanium_nugget 2–4 gated on the `getWhat()==0` condition (or move to code if loot conditions can't express it).

### ENT-A-027 — BandP: spawn conditions deleted
- **Status:** DIVERGENT
- **Original:** plains/desert/savanna w20 1–2; night, y≥50, villager-count condition
- **Port:** `add_overworld_monsters.json` w3 1–1; no conditions
- **Fix:** dedicated modifier for plains/desert/savanna w20 1–2; restore night + y≥50 + nearby-villager spawn rule (ENT-SYS-002).

## Baryonyx

### ENT-A-028 — Baryonyx: bones invented in loot
- **Status:** PARTIAL
- **Original:** `Baryonyx.java` `func_70628_a` — 2–6 raw beef only
- **Port:** `baryonyx.json` bones 2–5 **plus** custom 2–6 beef (double source, ENT-SYS-001)
- **Fix:** make `baryonyx.json` beef 2–6 only; remove the bones pool and the custom death loot.

### ENT-A-029 — Baryonyx: mining-dimension habitat → generic overworld
- **Status:** DIVERGENT
- **Original:** mining-dim biomes (`BiomeGenUtopianPlains`, `ChunkProviderOreSpawn2` w2 4–8); rules y>50, day, ≤8 buddies
- **Port:** `add_overworld_creatures.json` w3 1–1; conditions removed
- **Fix:** add Baryonyx to the port's mining-dimension spawn lists w2 4–8; restore day/y>50/buddy-cap rules.

### ENT-A-030 — Baryonyx: wheat breeding lost
- **Status:** PARTIAL
- **Original:** breeds with wheat or crystal apple
- **Port:** `entity/Baryonyx.java` — breeds with `CRYSTAL_APPLE` only
- **Fix:** include `Items.WHEAT` in `isFood`.

## Basilisk

### ENT-A-031 — Basilisk: HP more than doubled, armor cut, fire immunity lost
- **Status:** DIVERGENT
- **Original:** `OreSpawnMain.java:6487` — HP 200, atk 24, def 15; fire-immune (`field_70178_ae=true`)
- **Port:** `entity/Basilisk.java` — HP 500, atk 25, armor 8, KB-resist 0.8; fire immunity not set
- **Fix:** set HP 200, ATTACK 24, ARMOR 15; add `fireImmune()` to the entity type builder.

### ENT-A-032 — Basilisk: gaze/slowness aura invented
- **Status:** PARTIAL
- **Original:** `Basilisk.java` — plain melee acquisition in `func_70619_bc` (1-in-5 find, 1-in-3/1-in-4 swing); no aura, no poison
- **Port:** `BasiliskGazeAttackGoal` — Slowness V aura 6 blocks + Poison on bite (invented); adds `NearestAttackableTargetGoal<Player>`
- **Fix:** remove the gaze aura and bite poison (or keep behind a config flag); restore original tick-scan acquisition cadence.

### ENT-A-033 — Basilisk: melee slowness effect parameters changed
- **Status:** DIVERGENT
- **Original:** `Basilisk.java` — melee 24 + slowness (`Potion.field_76421_d`) 100 ticks amp 5, 1-in-3
- **Port:** `entity/Basilisk.java` — melee 25 + `MOVEMENT_SLOWDOWN` 200 ticks amp 0, 1-in-3
- **Fix:** apply MOVEMENT_SLOWDOWN for 100 ticks, amplifier 5.

### ENT-A-034 — Basilisk: loot content diverged
- **Status:** DIVERGENT
- **Original:** BasiliskScale 1, Item Frame 1, 12–17 emeralds, 8–12 cooked cod, 3–7 bonus rolls (1-in-15 enchanted emerald gear)
- **Port:** `basilisk.json` (scale, name_tag, emerald 12–17, raw cod 8–12) plus custom (scale, golden apple, emeralds, gold ingots, gear rolls) — double, ENT-SYS-001
- **Fix:** single JSON: scale ×1, item_frame ×1 (not name_tag), emerald 12–17, **cooked** cod 8–12, 3–7 bonus rolls with 1-in-15 enchanted emerald-gear pool; delete custom death loot.

### ENT-A-035 — Basilisk: spawn biomes changed
- **Status:** DIVERGENT
- **Original:** mushroom/jungle/mega-taiga w3–15; night + spawner check + no buddy
- **Port:** `hostile_basilisk__*` badlands+jungle, w3 1–1
- **Fix:** retarget modifiers to mushroom fields, jungle, old-growth taiga at original weights; restore night/buddy rules (ENT-SYS-002).

### ENT-A-036 — Basilisk: custom sounds replaced with vanilla ravager
- **Status:** DIVERGENT
- **Original:** `basilisk_living` (1-in-2), `alo_hurt`, `emperorscorpion_death`, vol 1.0
- **Port:** `RAVAGER_ROAR/HURT/DEATH`
- **Fix:** register/use `orespawn:basilisk_living`, `orespawn:alo_hurt`, `orespawn:emperorscorpion_death` at vol 1.0.

## Beaver

### ENT-A-037 — Beaver: drops swapped porkchops→leather
- **Status:** DIVERGENT
- **Original:** `Beaver.java` — 2–6 raw porkchops
- **Port:** `beaver.json` — 1–3 leather
- **Fix:** change `beaver.json` to porkchop 2–6.

### ENT-A-038 — Beaver: spawn weights/biomes flattened
- **Status:** PARTIAL
- **Original:** forest/jungle w10 2–4 (rules y 50–100 + ground-block check, which the port kept)
- **Port:** `add_overworld_creatures.json` w8 1–2
- **Fix:** dedicated forest/jungle modifier w10 2–4.

### ENT-A-039 — Beaver: wheat breeding lost
- **Status:** PARTIAL
- **Original:** breed wheat/crystal apple
- **Port:** crystal apple only
- **Fix:** add `Items.WHEAT` to `isFood`.

## Bee

### ENT-A-040 — Bee: combat stats and size cut
- **Status:** DIVERGENT
- **Original:** `OreSpawnMain.java:6466` — HP 80, atk 12, def 5; size 1.5×2.5
- **Port:** `entity/EntityBee.java` — HP 30, atk 6, armor 0; size 0.5×0.5
- **Fix:** set HP 80, ATTACK 12, ARMOR 5; dimensions 1.5×2.5.

### ENT-A-041 — Bee: Girlfriend/Boyfriend targets dropped
- **Status:** PARTIAL
- **Original:** `Bee.java` — flight seek targets Player/Villager/Girlfriend/Boyfriend
- **Port:** `entity/EntityBee.java` — Player/Villager only
- **Fix:** include Girlfriend and Boyfriend entities in the seek-target filter.

### ENT-A-042 — Bee: attack effect swapped Hunger→Poison
- **Status:** DIVERGENT
- **Original:** `Bee.java` — melee + Hunger (`field_76436_u`… per audit: Hunger) 50 ticks, 1-in-3
- **Port:** melee + POISON 50 ticks, 1-in-3
- **Fix:** apply `MobEffects.HUNGER` 50t instead of POISON.

### ENT-A-043 — Bee: drop table unrecognizable
- **Status:** DIVERGENT
- **Original:** 2–11 each: gold nuggets, butter candy, dandelion, sugar
- **Port:** `bee.json` (gunpowder 2–5, sugar 1–3, butter candy 0–2@50%) + custom spider eyes & red mushrooms (double, ENT-SYS-001)
- **Fix:** single JSON: gold_nugget 2–11, butter_candy 2–11, dandelion 2–11, sugar 2–11; delete custom death loot.

### ENT-A-044 — Bee: spawn biomes/weights changed
- **Status:** DIVERGENT
- **Original:** forest/taiga biomes w2–5 1–5; day/clear-air/y>50 or Utopia rules
- **Port:** `add_overworld_monsters.json` w8 1–3
- **Fix:** dedicated forest/taiga modifier w2–5 1–5; restore rules (ENT-SYS-002).

## Bertha (item)

### ENT-A-045 — Bertha: sword damage values changed
- **Status:** DIVERGENT
- **Original:** `Bertha.java` / `OreSpawnMain.java` `bertha_stats` — damage 496 (Royal 746, Hammy 82), durability 9000
- **Port:** `item/Bertha.java` — sword-tier values differ; kills tracked via new `ModDataComponents.BERTHA_KILLS`
- **Fix:** set tier/attribute so attack damage is 496 and durability 9000 (Royal/Hammy variants 746/82 — see ENT-A-048).

### ENT-A-046 — Bertha: enchant application path changed
- **Status:** PARTIAL
- **Original:** enchants applied in `onUsingTick`/`func_77622_d`
- **Port:** enchants via `OreSpawnEnchantHelper.inventoryTick`
- **Fix:** verify the helper applies the same enchant IDs/levels at the same trigger points; align if not.

### ENT-A-047 — Bertha: PvP config gate hardcoded
- **Status:** DIVERGENT
- **Original:** blocks hitting players/tamed only when config `big_bertha_pvp == 0`
- **Port:** blocks unconditionally
- **Fix:** add a `big_bertha_pvp` config option and gate the player/tamed-skip on it.

## BerthaHit (projectile)

### ENT-A-048 — BerthaHit: damage rewritten wholesale
- **Status:** DIVERGENT
- **Original:** `BerthaHit.java` — type 0 (Bertha) 496; type 2 (Royal) 746; type 3 (Hammy) 82
- **Port:** `entity/BerthaHit.java` — type 0: 250; type 2: 150; type 3: 100
- **Fix:** restore per-type damage constants 496 / 746 / 82.

### ENT-A-049 — BerthaHit: per-type range collapsed
- **Status:** DIVERGENT
- **Original:** distSq < 81 / 101 / 64 per type
- **Port:** single `CLOSE_RANGE_DAMAGE_SQ = 100` for all
- **Fix:** replace the constant with per-type thresholds 81 (t0), 101 (t2), 64 (t3).

### ENT-A-050 — BerthaHit: Hammy explosion radius branch lost
- **Status:** PARTIAL
- **Original:** type 3 explosion radius 1.5 **or** 2.1 (conditional) + mobGriefing
- **Port:** radius 2.1 only
- **Fix:** restore the conditional 1.5-radius branch from `BerthaHit.java`.

### ENT-A-051 — BerthaHit: PvP config gate hardcoded
- **Status:** DIVERGENT
- **Original:** skips players/tamed if `big_bertha_pvp==0`
- **Port:** skips unconditionally
- **Fix:** gate on the same `big_bertha_pvp` config as ENT-A-047.

## BetterFireball

### ENT-A-052 — BetterFireball: boss/robot HP-halving immunity list dropped
- **Status:** MISSING
- **Original:** `BetterFireball.java` — `notme` skips Player/Dragon/Mothra; Robots/GodzillaHead/Royalty/Kraken/PitchBlack exempt from HP-halving
- **Port:** `entity/BetterFireball.java` — `notme` flag declared but never read; exemption list absent
- **Fix:** wire the `notme` check into the hit handler and restore the exemption list before applying the w·h>30 HP-halving.

## Boyfriend

### ENT-A-053 — Boyfriend: armor floor, fire immunity, and size lost
- **Status:** PARTIAL
- **Original:** `Boyfriend.java:123,179-193,492` — HP 80, fire-immune, armor clamp 8–23 from worn gear; size 0.5×1.6
- **Port:** `entity/Boyfriend.java:84-89`, `ModEntities.java:391` — HP 80 ✓; no fire immunity, no min-armor-8 floor; size 0.6×1.8
- **Fix:** add `fireImmune()`; override armor getter to clamp 8–23 based on equipment; size 0.5×1.6.

### ENT-A-054 — Boyfriend: AI set diverged (ranged→melee, goals missing, tempt item changed)
- **Status:** DIVERGENT
- **Original:** `Boyfriend.java:127-148` — Tempt(cooked beef), `EntityAIArrowAttack(4, 1.25, 20t, 10.0f)`, Panic(6), OpenDoor(10), MoveIndoors(11); Jealousy target goals @4/5
- **Port:** `entity/Boyfriend.java:70-82` — Tempt(DIAMOND), `MeleeAttackGoal(4)`; no Panic/OpenDoor/MoveIndoors/Jealousy
- **Fix:** tempt with cooked beef; replace MeleeAttackGoal with a ranged-attack goal (see ENT-A-055); add Panic, OpenDoor, MoveIndoors and Jealousy goals.

### ENT-A-055 — Boyfriend: entire weapon/ranged system missing
- **Status:** MISSING
- **Original:** `Boyfriend.java:874-907,239-289` — fires `UltimateArrow` (2.0f, 1-in-4 crit, punch/flame aware) when holding UltimateBow, else throws `Shoes` projectile (1.8 speed, 4.0 spread); melee with held-item enchant math + 25t cooldown + `b_fight` sound
- **Port:** plain vanilla `MeleeAttackGoal`; no Shoes, no UltimateBow logic, no fight/taunt sounds
- **Fix:** implement `RangedAttackMob`: UltimateArrow when holding UltimateBow, Shoes projectile fallback; port held-item enchant melee math and `b_fight` sound.

### ENT-A-056 — Boyfriend: tamed poppy drop missing
- **Status:** PARTIAL
- **Original:** `Boyfriend.java:839-872` — tamed: 2–6 poppies; always 10–35 game controllers; all equipped gear
- **Port:** `boyfriend.json` — game controller 10–36; equipment ✓; no poppies
- **Fix:** add poppy 2–6 (condition: tamed) to loot or `dropCustomDeathLoot`; correct controller max 35.

### ENT-A-057 — Boyfriend: rich per-biome spawn list flattened
- **Status:** DIVERGENT
- **Original:** `OreSpawnMain.java:4588-4599` — beach w30 8–15, forest w10 3–6, hills w8 2–5, plains w5 2–3, river w10, stone beach w10, birch w5, roofed w5, mega taiga w5, taiga w5, savanna w2, savanna plateau w2
- **Port:** `companion_boyfriend.json` — `#minecraft:is_overworld` w4 1–2
- **Fix:** split modifier into per-biome entries with the original weights/group sizes (beach hotspot w30 8–15 especially).

### ENT-A-058 — Boyfriend: situational ambient voice lines missing
- **Status:** PARTIAL
- **Original:** `Boyfriend.java:768-812` — ambient `b_water`/`b_thunder`/`b_rain`/`b_dark`/`b_hurt`/`b_happy` (+`bb_happy` bro_mode)
- **Port:** `entity/Boyfriend.java:268-307` — only `b_hurt`/`b_happy` branch
- **Fix:** restore the weather/water/darkness ambient branches selecting `b_water`, `b_thunder`, `b_rain`, `b_dark` (and `bb_happy` when bro mode).

### ENT-A-059 — Boyfriend: wet-skin, untame, voice toggle, health report, FrogPrince missing
- **Status:** PARTIAL
- **Original:** `Boyfriend.java` — untame via dead bush; voice off Ruby / on Amethyst; wet-skin system (18 swimshorts textures, wet_count 500); diamond-in-hand guard mode; health report chat; Peacock alt tame item; FrogPrince textures
- **Port:** `entity/Boyfriend.java` — none of these; skin cycle moved to DANDELION (dry only); new BOYFRIEND_BRO_MODE config
- **Fix:** port the item interactions (dead bush untame, Ruby/Amethyst voice toggle, Peacock tame), wet-skin texture state (wet_count 500), and health-report chat message.

## Brutalfly

### ENT-A-060 — Brutalfly: HP ×4.5, attack raised, armor/size/fire-immunity lost
- **Status:** DIVERGENT
- **Original:** `OreSpawnMain.java:6470` — HP 110, atk 10, def 6; size 5.0×2.0 (`Brutalfly.java:55`); fire-immune (`:58`)
- **Port:** `EntityBrutalfly.java:40-45` — HP 500, atk 18, armor 0; size 1.2×1.2 (`ModEntities.java:192`); not fire-immune
- **Fix:** set HP 110, ATTACK 10, ARMOR 6; dimensions 5.0×2.0; `fireImmune()`.

### ENT-A-061 — Brutalfly: terrain-descent scan and exclusion list missing
- **Status:** PARTIAL
- **Original:** `Brutalfly.java:151-253` — drops target if >10 above ground (terrain-descent scan); hunt excludes Brutalfly/Mothra/Vortex/ignoreables
- **Port:** `EntityBrutalfly.java:100-174` — same flight skeleton; descent scan missing; Mothra/Vortex exclusions missing
- **Fix:** add ground-clearance check that releases targets >10 blocks above terrain; extend exclusion filter with Mothra and Vortex.

### ENT-A-062 — Brutalfly: signature fireball barrage missing
- **Status:** MISSING
- **Original:** `Brutalfly.java:369-406` — Easy=SmallFireball, Normal=50/50 Small/BetterFireball, Hard=BetterFireball; +1 HP self-heal per shot; shoot odds 1-in-3 (1-in-2 hard); melee only within distSq 25
- **Port:** `EntityBrutalfly.java:141-158` — melee only
- **Fix:** implement `attackWithSomething`-style ranged logic with difficulty-keyed projectile choice, per-shot self-heal, and the original shoot odds.

### ENT-A-063 — Brutalfly: death loot/butterfly burst replaced
- **Status:** DIVERGENT
- **Original:** `Brutalfly.java:339-353` — 53 gold nuggets + spawns 20 Butterflies + 20 largeexplode particles
- **Port:** `brutalfly.json` gunpowder 10–53 + custom 53 spider eyes; no butterflies
- **Fix:** JSON: gold_nugget ×53; in `die()` spawn 20 EntityButterfly + explosion particles; delete spider-eye custom loot.

### ENT-A-064 — Brutalfly: spawn biomes/rules changed
- **Status:** DIVERGENT
- **Original:** `OreSpawnMain.java:4839-4841` — ambient w2 1–1 ExtremeHillsPlus/SavannaPlateau/MesaPlateau; rules y≥70, dark, night, 4×3×10 clear air, none within 64
- **Port:** `add_overworld_monsters.json` w3 1–1; no checkSpawnRules
- **Fix:** dedicated modifier for windswept hills/savanna plateau/badlands plateau w2 1–1; restore rules (ENT-SYS-002).

### ENT-A-065 — Brutalfly: hurt sound invented
- **Status:** PARTIAL
- **Original:** no living/hurt sound; death `random.explode`
- **Port:** hurt = GENERIC_HURT (added)
- **Fix:** return null/empty for hurt sound to match the original silent profile.

## Camarasaurus

### ENT-A-066 — Camarasaurus: hitbox grew
- **Status:** PARTIAL
- **Original:** `Camarasaurus.java:47` — size 0.5×1.2
- **Port:** `ModEntities.java:395` — size 1.4×2.6
- **Fix:** set dimensions 0.5×1.2.

### ENT-A-067 — Camarasaurus: MoveIndoors missing; target goals invented
- **Status:** PARTIAL
- **Original:** `Camarasaurus.java:53-63` — goal 9 MoveIndoors; no owner-combat target goals
- **Port:** `entity/Camarasaurus.java:60-74` — MoveIndoors absent; OwnerHurtBy/OwnerHurt/HurtBy targets added
- **Fix:** add a MoveIndoors-equivalent goal; remove the added combat target goals (passive pet in original).

### ENT-A-068 — Camarasaurus: graze diet inverted (tree browser → crop raider)
- **Status:** DIVERGENT
- **Original:** `Camarasaurus.java:105-166` — eats leaves/vines/tallgrass/cactus/double_plant
- **Port:** `entity/Camarasaurus.java:99-103` `isEdibleBlock` — eats wheat/carrots/potatoes/short_grass/tall_grass
- **Fix:** change `isEdibleBlock` to leaves (`BlockTags.LEAVES`), vines, grass, cactus, and tall flowers; drop the crop blocks.

### ENT-A-069 — Camarasaurus: rideability invented
- **Status:** PARTIAL
- **Original:** `Camarasaurus.java:219-278` — tame with apple 1-in-2, name tag, sit toggle; not rideable
- **Port:** `entity/Camarasaurus.java:225-233` — adds player riding + `tickRidden` with ×1.5 speed
- **Fix:** remove the riding code (or keep behind a config flag documenting it as a port addition).

### ENT-A-070 — Camarasaurus: tamed-only poppy drop replaced with always-bones
- **Status:** DIVERGENT
- **Original:** `Camarasaurus.java:303-312` — tamed only: 2–6 poppies; untamed drops nothing
- **Port:** `camarasaurus.json` — bone 3–6 always
- **Fix:** empty the default pool; drop poppy 2–6 only when tamed (code-side or loot condition).

### ENT-A-071 — Camarasaurus: spawning relocated to overworld
- **Status:** DIVERGENT
- **Original:** mining-dim chunk providers only; rules y≥50 + day (`func_70601_bi:78-83`)
- **Port:** `add_overworld_creatures.json` w2 1–1 + `companion_camarasaurus__*` jungle/savanna w1 1–1; no rules
- **Fix:** move spawns to the mining-dimension spawn lists; restore y≥50 + day rules; remove overworld modifiers.

## CaterKiller

### ENT-A-072 — CaterKiller: stats and melee damage cut
- **Status:** DIVERGENT
- **Original:** `OreSpawnMain.java:6481` — HP 450, atk 32, def 19; size 2.9×4.6 (`CaterKiller.java:54-58`)
- **Port:** `EntityCaterKiller.java:82-88` — HP 350, atk 20, armor 0; size 1.5×1.0 (`ModEntities.java:196`)
- **Fix:** set HP 450, ATTACK 32, ARMOR 19; dimensions 2.9×4.6 (halve when PlayNicely config active, per original).

### ENT-A-073 — CaterKiller: web-self-clear missing
- **Status:** PARTIAL
- **Original:** `CaterKiller.java:430-531` — clears cobwebs it collides with (web-self-clear)
- **Port:** `EntityCaterKiller.java:175-209` — places webs under fleeing targets ✓ but never clears webs on self-collision
- **Fix:** when colliding with a cobweb block, remove it (mobGriefing-gated) as in the original loop.

### ENT-A-074 — CaterKiller: metamorphosis logic inverted
- **Status:** DIVERGENT
- **Original:** `CaterKiller.java:438-448` — damaged >2400t: spawns 1 Brutalfly + 10 Butterflies, explode sound, removes self; on death spawns 25 Butterflies (no Brutalfly)
- **Port:** `EntityCaterKiller.java:161-169,253-274` — timed transform silently discards (no spawns); every death spawns 1 Brutalfly + 3–5 Butterflies
- **Fix:** swap: timed transform spawns Brutalfly + 10 Butterflies + explode sound then discard; `die()` spawns 25 Butterflies and never a Brutalfly.

### ENT-A-075 — CaterKiller: tree-eat heal values invented
- **Status:** DIVERGENT
- **Original:** `CaterKiller.java:502-530` — scan ≤13 for leaves/vines/logs, path to it, eat at distSq<81, heal 2.0, odds 1-in-8 hurt / 1-in-30 idle
- **Port:** `EntityCaterKiller.java:218-242` — random 5×4×5 sample 1/s, heal 5 (leaf) / 10 (log), no pathing; extra 1-in-150 heal 2.0 added
- **Fix:** restore pathing scan (radius 13, eat range distSq<81), heal 2.0 flat, original trigger odds; remove invented heals and false parity comments (ENT-SYS-003).

### ENT-A-076 — CaterKiller: reward loot degraded
- **Status:** DIVERGENT
- **Original:** `CaterKiller.java:160-328` — CaterKillerJaw 1, Item Frame 1, 10 leather, 6 beef, 1–5 rolls 13/20 chance of ultimate sword/ruby/diamond block/enchanted ruby gear/ultimate bow
- **Port:** `cater_killer.json` — jaw, name_tag, leather 6–10, slime 3–6, rolls with diamond→emerald block swap; plus custom name tag + leather + bones (double, ENT-SYS-001)
- **Fix:** single JSON: jaw ×1, item_frame ×1, leather ×10, beef ×6, 1–5 rolls @13/20 over the original pool (ultimate sword, ruby, **diamond** block, enchanted ruby gear, ultimate bow); delete custom death loot.

### ENT-A-077 — CaterKiller: spawn biomes/rules changed
- **Status:** DIVERGENT
- **Original:** `OreSpawnMain.java:4746-4754` — ambient forest/jungle/hills/jungleEdge/birch/roofed/megaTaiga/taiga w2–10 1–2; rules day, y≥50, 1-in-10 dice, leaf/air clearance, none within 48
- **Port:** `hostile_cater_killer__*` forest/jungle/taiga/badlands w4 1–2; no rules
- **Fix:** drop badlands, restore original biome weights; add rules (ENT-SYS-002).

## CaveFisher

### ENT-A-078 — CaveFisher: stats raised, armor zeroed
- **Status:** DIVERGENT
- **Original:** `OreSpawnMain.java:6511` — HP 10, atk 4, def 4; size 1.35×0.75
- **Port:** `CaveFisher.java:64-70` — HP 25, atk 6, armor 0; size 0.8×0.8 (`ModEntities.java:48`)
- **Fix:** set HP 10, ATTACK 4, ARMOR 4; dimensions 1.35×0.75.

### ENT-A-079 — CaveFisher: ceiling-ambush goal invented
- **Status:** PARTIAL
- **Original:** `CaveFisher.java:163-183` — simple 1-in-8 scan, attack at distSq<8 with ~26% swing
- **Port:** `CaveFisherAmbushGoal` (new ceiling ambush) added
- **Fix:** remove the ambush goal or gate it behind config; ensure base attack cadence matches 1-in-8 scan / ~26% swing.

### ENT-A-080 — CaveFisher: prey selection inverted (passive-mob predator → player hunter)
- **Status:** DIVERGENT
- **Original:** `CaveFisher.java:193-228` — hunts players & animals; excludes CaveFisher/EnderReaper/EnderKnight/all EntityMob
- **Port:** targets Player only via goal
- **Fix:** add `NearestAttackableTargetGoal<Animal>` with the original exclusion filter (no monsters, no CaveFisher/EnderReaper/EnderKnight).

### ENT-A-081 — CaveFisher: nugget gamble drops replaced
- **Status:** DIVERGENT
- **Original:** `CaveFisher.java:141-153` — 1-in-6 each: gold nugget / uranium nugget / titanium nugget, else nothing
- **Port:** `cave_fisher.json` — string 2–5 + spider eye 0–1
- **Fix:** rewrite JSON: three independent pools, each item @ ~16.7% chance, count 1; remove string/spider-eye.

## Cephadrome

### ENT-A-082 — Cephadrome: target list inverted
- **Status:** DIVERGENT
- **Original:** `Cephadrome.java:515-573,404-432` — attacks Monsters, Mothra, untamed Leon/GammaMetroid/WaterDragon, EnderDragon (70 direct part hits), Kraken ×1.5 dmg; players only if hit_by_player/badmood
- **Port:** `entity/Cephadrome.java:227-242` — Monsters ✓ but Mothra/Leon/GammaMetroid/WaterDragon explicitly EXCLUDED; no EnderDragon/Kraken special damage
- **Fix:** flip the exclusions into inclusions (untamed only); add EnderDragon part-hit handling and ×1.5 Kraken damage.

### ENT-A-083 — Cephadrome: flying-mount system missing (signature feature)
- **Status:** MISSING
- **Original:** `Cephadrome.java:666-850,872-908` — feed meat → mount; full custom flight physics, flyup key, wing sound every 22t while ridden, obstruction climb
- **Port:** no riding; replaced by invented porkchop "taming" flag (`DATA_TAMED`, `mobInteract:268-307`) + TemptGoal(porkchop)
- **Fix:** implement rider-controlled flight in `travel()` (lift on jump-key, obstruction climb, 22t wing-beat sound) triggered by feeding meat; remove the invented tame flag/tempt goal. Also restore size 2.5×2.25 (`ModEntities.java:605` shrunk it to 1.5×1.5).

### ENT-A-084 — Cephadrome: gear-roll drops missing
- **Status:** PARTIAL
- **Original:** `Cephadrome.java:229-398` — 4–9 uranium + 4–9 titanium nuggets + 1–5 rolls (ruby gear/diamond/ThunderStaff/enchants, 12-in-20 ruby)
- **Port:** `cephadrome.json` (bone 3–8, gunpowder 2–5, diamond 0–2@25%) + custom nuggets (double, ENT-SYS-001)
- **Fix:** single JSON: uranium_nugget 4–9, titanium_nugget 4–9, 1–5 bonus rolls over the original ruby-gear/ThunderStaff pool; remove bone/gunpowder pools and custom loot.

### ENT-A-085 — Cephadrome: cannot spawn naturally
- **Status:** MISSING
- **Original:** `OreSpawnMain.java:4774-4775` — ambient w1 1–1 icePlains + coldTaiga; rules day, y≥50, clear air, none within 16
- **Port:** no biome modifier; MISC category (`ModEntities.java:605`)
- **Fix:** change MobCategory to AMBIENT/CREATURE; create biome modifier for snowy plains + snowy taiga w1 1–1; add the spawn rules.

## Chipmunk

### ENT-A-086 — Chipmunk: MoveIndoors missing; tempt item apple→wheat
- **Status:** PARTIAL
- **Original:** `Chipmunk.java:52-63` — Tempt(apple)@4; MoveIndoors@11
- **Port:** `entity/Chipmunk.java:56-68` — Tempt(WHEAT)@4; no MoveIndoors
- **Fix:** tempt with `Items.APPLE`; add MoveIndoors-equivalent goal.

### ENT-A-087 — Chipmunk: tame/untame items both changed
- **Status:** DIVERGENT
- **Original:** `Chipmunk.java:132-206` — tame apple 1-in-2; untame dead bush
- **Port:** `entity/Chipmunk.java:119-158` — tame wheat 1-in-2; untame glass
- **Fix:** switch tame item to apple, untame item to dead bush.

### ENT-A-088 — Chipmunk: all drops missing
- **Status:** MISSING
- **Original:** `Chipmunk.java:227,231-242` — untamed: wheat; tamed: 2–6 poppies
- **Port:** `chipmunk.json` — empty pools
- **Fix:** populate JSON: wheat ×1 (untamed); poppy 2–6 when tamed (loot condition or code).

### ENT-A-089 — Chipmunk: jukebox-dance lineage lost
- **Status:** PARTIAL
- **Original:** extends `EntityCannonFodder` (jukebox dance behavior)
- **Port:** extends `TamableAnimal` directly
- **Fix:** re-parent to the port's EntityCannonFodder (or copy its dance handler) to restore dancing near jukeboxes.

## CliffRacer

### ENT-A-090 — CliffRacer: gamble drops replaced with feathers
- **Status:** DIVERGENT
- **Original:** `CliffRacer.java:149-161` — 1-in-8 each: raw chicken / uranium nugget / titanium nugget
- **Port:** `cliff_racer.json` — feather 1–3
- **Fix:** rewrite JSON: three pools, each item @12.5% chance ×1; remove feathers.

## CloudShark

### ENT-A-091 — CloudShark: HP raised, armor zeroed
- **Status:** DIVERGENT
- **Original:** `OreSpawnMain.java:6512` — HP 15, atk 6, def 5; size 1.0×0.75
- **Port:** `CloudShark.java:42-47` — HP 20, armor 0; size 1.5×1.0 (`ModEntities.java:52`)
- **Fix:** set HP 15, ARMOR 5; dimensions 1.0×0.75.

### ENT-A-092 — CloudShark: prey ecosystem gone (player-only hunter)
- **Status:** PARTIAL
- **Original:** `CloudShark.java:202-243` — preys on Butterfly, Cockateil, Mosquito, Firefly, GoldFish, CliffRacer, Player
- **Port:** `entity/CloudShark.java:109-117` — Player only
- **Fix:** extend the 1-in-9 hunt scan to include the six prey species.

### ENT-A-093 — CloudShark: drops replaced
- **Status:** DIVERGENT
- **Original:** `CloudShark.java:263-275` — 1-in-3 each: paper / string / bone
- **Port:** `cloud_shark.json` — cod 3–8
- **Fix:** rewrite JSON: paper/string/bone pools each @33% ×1; remove cod.

### ENT-A-094 — CloudShark: persistence/despawn semantics changed
- **Status:** PARTIAL
- **Original:** `func_70692_ba` — despawns only at night (inverse-persistent by day)
- **Port:** vanilla despawn
- **Fix:** override `removeWhenFarAway`/despawn check to only allow despawning at night.

## Cockateil

### ENT-A-095 — Cockateil: bird-type variant never randomized
- **Status:** DIVERGENT
- **Original:** `Cockateil.java:82-86` — random type 0–5 at spawn → 6 textures
- **Port:** `entity/Cockateil.java:55-58` — DATA_BIRD_TYPE defaults 0, never randomized
- **Fix:** in `finalizeSpawn`, set bird type to `random.nextInt(6)`.

### ENT-A-096 — Cockateil: flight AI degraded (no yaw update, LOS/air checks and hooks gone)
- **Status:** PARTIAL
- **Original:** `Cockateil.java:170-222` — LOS+air target validation; `flyup` hook; dim-4 stayup bias; yaw/3 turn
- **Port:** `entity/Cockateil.java:101-138` — validation gone; yaw never updated (no setYRot); hooks gone
- **Fix:** apply `setYRot` with yaw/3 blending each tick; re-add air+LOS target validation and the dim-bias hook.

### ENT-A-097 — Cockateil: ruby drop no longer gated on bird type 5
- **Status:** DIVERGENT
- **Original:** `Cockateil.java:242-248` — ruby only if birdtype==5 & killedByPlayer & 1-in-3
- **Port:** `cockateil.json` — ruby @ killed_by_player && 33% from ANY bird
- **Fix:** move ruby drop to code gated on `getBirdType()==5` (or a loot condition reading the synched data), keep 1-in-3 + killed-by-player.

## Coin

### ENT-A-098 — Coin: jackpot loot table replaced with gold
- **Status:** DIVERGENT
- **Original:** `Coin.java:98-129` — 1 roll-of-10: diamond / uranium nugget / titanium nugget / emerald / emerald axe-shovel-pickaxe-hoe / CoinEgg / emerald sword default
- **Port:** `coin.json` — gold ingot 1–3
- **Fix:** rewrite JSON as a single roll over the original 10-entry pool (incl. CoinEgg and emerald tools/sword).

### ENT-A-099 — Coin: natural overworld spawning impossible
- **Status:** MISSING
- **Original:** `OreSpawnMain.java:4942-4947` — ambient w2 1–1 taiga/forest/jungle/birch/coldTaigaHills/megaTaiga; rules day, y≥50, none within 20
- **Port:** `dim_village_locals.json` only; MobCategory.MISC (`ModEntities.java:298`) — natural cycle never picks it
- **Fix:** change category to AMBIENT; create overworld biome modifier w2 1–1 for the six biomes; add rules.

## Crab

### ENT-A-100 — Crab: every stat rewritten; scale no longer drives HP/XP/size
- **Status:** DIVERGENT
- **Original:** `Crab.java:136-138`, `OreSpawnMain.java:6517,6524` — HP 250×scale, atk 24×scale, armor 16+2·scale, XP 400×scale, size 3.75×3.5·scale
- **Port:** `Crab.java:38-42,59-66,96-101` — HP fixed 100, atk 10×scale, armor 6, XP fixed 150; size fixed 0.8×0.6 (`ModEntities.java:56`)
- **Fix:** derive HP/XP/armor/dimensions from scale per original formulas (HP 250·scale, atk 24·scale, armor 16+2·scale, XP 400·scale, size 3.75×3.5·scale via `EntityDimensions.scalable`).

### ENT-A-101 — Crab: spawn-time scale randomization deleted (giant crabs never occur)
- **Status:** DIVERGENT
- **Original:** `Crab.java:74-98` — scale 0.25 base, 1-in-4→0.5, 1-in-8→1.0; spawner crabs 0.35
- **Port:** `Crab.java:69-73` — DATA_SCALE constant 25 (0.25), never randomized
- **Fix:** randomize scale in `finalizeSpawn` with the original 0.25/0.5/1.0 distribution (0.35 for spawner spawns).

### ENT-A-102 — Crab: water ecology inverted (water-seeker → water-avoider)
- **Status:** DIVERGENT
- **Original:** `Crab.java:306-339` — water-seek scan ≤12 → path 1.33; dry-out −1 HP @1-in-100, discard at 0
- **Port:** `WaterAvoidingRandomStrollGoal`; water-seek & dry-out missing
- **Fix:** replace WaterAvoidingStroll with plain stroll + ported water-seek scan and dry-out damage/discard.

### ENT-A-103 — Crab: melee damage cut and attack sounds missing
- **Status:** DIVERGENT
- **Original:** `Crab.java` — melee 24×scale; attack sounds `scorpion_attack`/`scorpion_living`; water-heal splash sound
- **Port:** `Crab.java:104-119` — melee 10×scale; no attack sounds, no splash
- **Fix:** set base melee 24×scale; play `orespawn:scorpion_attack`/`scorpion_living` on swings and splash on water heal.

### ENT-A-104 — Crab: target list pruned
- **Status:** PARTIAL
- **Original:** `Crab.java:379-418` — Player, Mobs, Lizard, RubberDucky, Villager, Girlfriend, Boyfriend
- **Port:** `Crab.java:182-188` — Player + Monster only
- **Fix:** add Lizard, RubberDucky, Villager, Girlfriend, Boyfriend to the suitable-target filter.

### ENT-A-105 — Crab: scale-based sound pitch formula missing
- **Status:** PARTIAL
- **Original:** `Crab.java` — pitch 2.0 − 0.3/scale
- **Port:** `Crab.java:190-210` — vol 0.75 ✓ but pitch formula absent
- **Fix:** apply `getVoicePitch() = 2.0f - 0.3f/scale`.

## CreepingHorror

### ENT-A-106 — CreepingHorror: stats and size doubled
- **Status:** DIVERGENT
- **Original:** `OreSpawnMain.java:6513` — HP 10, atk 3, def 2; size 0.75×0.5
- **Port:** `CreepingHorror.java:32-37,51-57` — HP 20, atk 6, armor 0; size 1.5×1.5 (`ModEntities.java:60`)
- **Fix:** set HP 10, ATTACK 3, ARMOR 2; dimensions 0.75×0.5.

### ENT-A-107 — CreepingHorror: MoveThroughVillage missing
- **Status:** PARTIAL
- **Original:** `CreepingHorror.java:51-57` — MoveThroughVillage@2
- **Port:** plain Stroll@2
- **Fix:** add MoveThroughVillage-equivalent at priority 2.

### ENT-A-108 — CreepingHorror: target exclusions and LOS check dropped
- **Status:** PARTIAL
- **Original:** `CreepingHorror.java:147-200` — excludes RockBase, EnderReaper, LeafMonster, Dragon, TerribleTerror, LurkingTerror, PitchBlack, Firefly, Island(s); LOS required
- **Port:** `CreepingHorror.java:130-135` — excludes self-kind only; no LOS
- **Fix:** restore the exclusion list and `hasLineOfSight` requirement in target selection.

### ENT-A-109 — CreepingHorror: drop quantity inflated
- **Status:** DIVERGENT
- **Original:** `CreepingHorror.java:119-128` — 1 of: rotten flesh / bone / string
- **Port:** `creeping_horror.json` — rotten flesh 2–5 + bone 1–3
- **Fix:** rewrite JSON as one roll picking a single rotten flesh OR bone OR string.

### ENT-A-110 — CreepingHorror: deep-cave/night spawn identity removed
- **Status:** DIVERGENT
- **Original:** `CreepingHorror.java:220-228` — rules: dark, night, and (dim6 or y≤15)
- **Port:** `add_overworld_monsters.json` w3 1–1; no rules — spawns anywhere monsters can
- **Fix:** add `checkSpawnRules` enforcing darkness + night + y≤15 (or the dungeon dimension); keep modifier weight.

## Cryolophosaurus

### ENT-A-111 — Cryolophosaurus: stats doubled, armor zeroed
- **Status:** DIVERGENT
- **Original:** `OreSpawnMain.java:6482` — HP 10, atk 3, def 1
- **Port:** `Cryolophosaurus.java:51-57` — HP 20, atk 5, armor 0
- **Fix:** set HP 10, ATTACK 3, ARMOR 1.

### ENT-A-112 — Cryolophosaurus: proactive hunting removed (retaliation-only)
- **Status:** DIVERGENT
- **Original:** `Cryolophosaurus.java:141-211` — proactive hunt 1-in-5 over 9×2×9; excludes Alosaurus/TRex/own kind/ghosts/CaveFisher/insects
- **Port:** `entity/Cryolophosaurus.java:34-44` — HurtBy only; comment falsely claims it never hunted (ENT-SYS-003)
- **Fix:** add proactive target scan (1-in-5 per tick, 9×2×9 box) with original exclusion list; fix the comment.

### ENT-A-113 — Cryolophosaurus: gamble drops replaced
- **Status:** DIVERGENT
- **Original:** `Cryolophosaurus.java:120-132` — 1-in-10: raw chicken / uranium nugget / titanium nugget, else nothing
- **Port:** `cryolophosaurus.json` — bone 2–5 + diamond 0–1@20%
- **Fix:** rewrite JSON: three pools each @10% ×1; remove bone/diamond.

## CrystalCow

### ENT-A-114 — CrystalCow: RedCow lineage lost
- **Status:** DIVERGENT
- **Original:** `CrystalCow.java` — extends RedCow (inherits its stats/behavior)
- **Port:** `entity/CrystalCow.java:15-22` — extends vanilla `Cow` with `Cow.createAttributes()`
- **Fix:** re-parent to the port's RedCow (or replicate RedCow stats/behavior) so inherited drops/attributes match.

### ENT-A-115 — CrystalCow: pink-ingot drop invented, vanilla apple lost
- **Status:** DIVERGENT
- **Original:** `CrystalCow.java:19-26` — 0–2(+looting) crystal apples + 1 apple + RedCow drops
- **Port:** `crystal_cow.json` (crystal apple 1–3 + leather 1) + custom 1–2 crystal pink ingots (double, ENT-SYS-001)
- **Fix:** JSON: crystal apple 0–2 (+looting), apple ×1, plus RedCow base drops; delete the pink-ingot custom loot.

### ENT-A-116 — CrystalCow: never-despawn flag invented
- **Status:** PARTIAL
- **Original:** no persistence override
- **Port:** `removeWhenFarAway` returns false (new)
- **Fix:** remove the override unless intentionally kept; document if kept.

---

# Entities D–I (file 02)

## Dragon

### ENT-D-001 — Dragon: armor attribute missing
- **Status:** PARTIAL
- **Original:** `Dragon.java` attribute init / `func_70658_aO` — armor 14 (HP 200, atk 35 match)
- **Port:** `entity/Dragon.java` `createAttributes` — no ARMOR attribute
- **Fix:** add `Attributes.ARMOR, 14` to `createAttributes`.

### ENT-D-002 — Dragon: tame item changed beef→bone
- **Status:** DIVERGENT
- **Original:** `Dragon.java` — raw beef tame
- **Port:** `entity/Dragon.java` `mobInteract` — `Items.BONE`, 1/5 chance, heal to full
- **Fix:** change tame item to `Items.BEEF` (verify original chance and replicate).

### ENT-D-003 — Dragon: drops beef→bones/diamonds (plus double source)
- **Status:** DIVERGENT
- **Original:** `Dragon.java` `func_70628_a` — raw beef
- **Port:** hardcoded bones 1–6 + `entities/dragon.json` diamonds 1–6 (+looting)
- **Fix:** single loot source: beef (match original count); delete the bone hardcode and diamond pool.

### ENT-D-004 — Dragon: Utopia-only spawn → overworld-wide
- **Status:** DIVERGENT
- **Original:** Utopia dimension boss list w1 1–2 (`BiomeGenUtopianPlains.java:164`); no overworld addSpawn
- **Port:** `add_overworld_creatures.json` w1 1–1 overworld-wide
- **Fix:** remove dragon from the overworld modifier; add to the Utopia dimension spawn list w1 1–2.

### ENT-D-005 — Dragon: custom wing-flap sound replaced
- **Status:** PARTIAL
- **Original:** custom wing flap sound (alongside `orespawn:roar`/`alo_hurt`/`alo_death`, which are kept)
- **Port:** flap = `SoundEvents.ENDER_DRAGON_FLAP`
- **Fix:** register and play the original orespawn flap sound asset instead of the vanilla ender-dragon flap.

### ENT-D-006 — Dragon: Magic Apple baby-spawn target renamed
- **Status:** DIVERGENT
- **Original:** Magic Apple spawns Spyro (named baby dragon)
- **Port:** apple spawns generic `BabyDragon`
- **Fix:** spawn the BabyDragon with the original Spyro identity/name (or implement a Spyro variant) when the Magic Apple is used.

## DungeonBeast

### ENT-D-007 — DungeonBeast: stats lowered
- **Status:** DIVERGENT
- **Original:** `OreSpawnMain.java:6501` — HP 65, atk 12, def 6
- **Port:** `entity/DungeonBeast.java:28-30,51-55` — HP 60, atk 10, armor 4
- **Fix:** set HP 65, ATTACK 12, ARMOR 6.

### ENT-D-008 — DungeonBeast: crystal-dimension drops → bones/gold
- **Status:** DIVERGENT
- **Original:** `func_70628_a` — Crystal-dimension items
- **Port:** `dungeon_beast.json` — bones 3–8 (+looting) + 50% gold ingots 1–4
- **Fix:** open original `DungeonBeast.java` `func_70628_a`, list the crystal items, and rewrite the JSON to match.

### ENT-D-009 — DungeonBeast: roofed forest → badlands relocation
- **Status:** DIVERGENT
- **Original:** `OreSpawnMain.java:4981` — addSpawn w20 2–4 ambient Roofed Forest; also spawners/Crystal dim
- **Port:** `hostile_dungeon_beast.json` — `#minecraft:is_badlands` w20 2–4
- **Fix:** change modifier biome to `minecraft:dark_forest` (roofed forest), keep w20 2–4; add Crystal-dimension spawn list entry.

## EasterBunny

### ENT-D-010 — EasterBunny: mob-egg-laying and carrot taming missing
- **Status:** MISSING
- **Original:** `EasterBunny.java` interact/update — lays mob eggs; tamed with carrot
- **Port:** `entity/EasterBunny.java` — neither present
- **Fix:** port the egg-laying tick (item/entity eggs per original logic) and carrot-based taming interaction.

### ENT-D-011 — EasterBunny: spawn weights collapsed
- **Status:** DIVERGENT
- **Original:** `OreSpawnMain.java:4682-4688` — addSpawn w10/w5/w8 1–2 across 7 biomes
- **Port:** `add_overworld_creatures.json` w3 1–2 overworld-wide
- **Fix:** dedicated modifier reproducing the 7 original biomes at weights 10/8/5.

## Elevator (Hoverboard)

### ENT-D-012 — Elevator/Hoverboard: hover hum remapped to beacon
- **Status:** DIVERGENT
- **Original:** `orespawn:hover` hum
- **Port:** `entity/HoverboardEntity.java` — `SoundEvents.BEACON_AMBIENT`
- **Fix:** register `orespawn:hover` and play it instead of the beacon ambient.

## EmperorScorpion

### ENT-D-013 — EmperorScorpion: stats cut, armor dropped
- **Status:** DIVERGENT
- **Original:** `OreSpawnMain.java:6488` — HP 350, atk 35, def 20
- **Port:** `EntityEmperorScorpion.java:83-89` — HP 300, atk 20, no armor
- **Fix:** set HP 350, ATTACK 35, ARMOR 20.

### ENT-D-014 — EmperorScorpion: minion-spawn cadence redesigned
- **Status:** DIVERGENT
- **Original:** rolled `nextInt(80)==1` per tick to spawn baby scorpions when population low
- **Port:** `EntityEmperorScorpion.java:52-60` — every 30+rand(10) ticks spawns `EntityScorpion` if <3 within 16 blocks, cap 6
- **Fix:** replace timer with per-tick `nextInt(80)==1` roll; replicate original population condition.

### ENT-D-015 — EmperorScorpion: loot de-enchanted, beef→slimeballs
- **Status:** DIVERGENT
- **Original:** `EmperorScorpion.java:181-315` — scale, painting, obsidian, raw beef, **enchanted** diamond gear/UltimateSword set
- **Port:** `emperor_scorpion.json` — scale, name_tag, obsidian 4–8, slimeballs 4–11, plain diamond-gear rolls
- **Fix:** JSON: painting (not name_tag), beef (not slimeballs), apply `enchant_randomly`/fixed enchants on the gear pool incl. UltimateSword.

## EnchantedCow

### ENT-D-016 — EnchantedCow: XP bottles + enchanted book invented
- **Status:** DIVERGENT
- **Original:** `EnchantedCow.java` `func_70628_a` — enchanted golden apples + golden apples + apples
- **Port:** `enchanted_apple_cow.json` (leather, golden_apple 1–2, enchanted_golden_apple 1) **plus** hardcoded `dropCustomDeathLoot`: 1–2 XP bottles always, 20% enchanted book (double, ENT-SYS-001)
- **Fix:** delete the `dropCustomDeathLoot` override; add plain apples to the JSON to match the original triple-apple table.

## EnderKnight

### ENT-D-017 — EnderKnight: stats inflated
- **Status:** DIVERGENT
- **Original:** `OreSpawnMain.java:6507` — HP 60, atk 12, def 6
- **Port:** `entity/EnderKnight.java` — HP 80, atk 15, no armor
- **Fix:** set HP 60, ATTACK 12, ARMOR 6.

### ENT-D-018 — EnderKnight: overworld habitat (incl. w20 roofed-forest hotspot) → End-only
- **Status:** DIVERGENT
- **Original:** `OreSpawnMain.java:4920-4928` — ambient w4 2–4 across 5 overworld biomes, w2 ×3, w20 Roofed Forest
- **Port:** `add_end_spawns.json` — `#minecraft:is_end` w8 1–2; no overworld spawns
- **Fix:** add overworld modifier with the original biome list (dark_forest w20 2–4 hotspot); remove or reduce the End entry.

## EnderReaper

### ENT-D-019 — EnderReaper: stats inflated
- **Status:** DIVERGENT
- **Original:** `OreSpawnMain.java:6508` — HP 90, atk 18, def 8
- **Port:** `entity/EnderReaper.java` — HP 120, atk 20, no armor
- **Fix:** set HP 90, ATTACK 18, ARMOR 8.

### ENT-D-020 — EnderReaper: provocation explosions unverified in port
- **Status:** PARTIAL
- **Original:** `EnderReaper.java` — explosions on provocation (plus teleport, which is ported)
- **Port:** `entity/EnderReaper.java` — explosion behavior not present in the port file; could live in a shared handler but none was found by the audit
- **Fix:** verification gap: search the port for any EnderReaper explosion trigger (e.g. event handlers); if truly absent, port the provocation-explosion logic from the original `EnderReaper.java`. Evidence to resolve: a port code path creating an explosion tied to EnderReaper provocation.

### ENT-D-021 — EnderReaper: overworld (w38 roofed forest) → End-only
- **Status:** DIVERGENT
- **Original:** `OreSpawnMain.java:4931-4939` — ambient w2/1 1–2 across 8 biomes + w38 2–4 Roofed Forest
- **Port:** `add_end_spawns.json` w4 1–1 End-only
- **Fix:** add overworld modifier with the 8 original biomes incl. dark_forest w38 2–4; remove/reduce the End entry.

## EntityCage

### ENT-D-022 — EntityCage: species whitelist replaced by universal NBT capture
- **Status:** DIVERGENT
- **Original:** `EntityCage.java:160-201,174` — `nextInt(10)>=2` (80%): per-type checks dropping matched `CagedSpiderDriver/CagedCaveSpider/CagedSpider/CagedCrab/CagedBat(×2)/CagedPig/...`; fail/player → `CageEmpty`
- **Port:** `entity/EntityCage.java` — discards **any** Mob, drops `CagedMobItem` with full NBT; can now cage mobs the original could not
- **Fix:** decide policy: either restore the species whitelist (reject non-listed mobs → CageEmpty), or keep universal capture but gate it behind a config default-off; the drop-item divergence resolves with the same choice.

## EntityCannonFodder

### ENT-D-023 — EntityCannonFodder: hat teams reduced to 2; corncob breeding missing
- **Status:** PARTIAL
- **Original:** `EntityCannonFodder.java` — multiple hat-item colors define teams; `MyCornCob` spawns new fodder entities
- **Port:** `entity/EntityCannonFodder.java` — Golden Apple → hat 1, Enchanted Golden Apple → hat 3 only; no corncob spawning
- **Fix:** restore the full hat-color item set and team-id mapping; implement MyCornCob interaction spawning a new CannonFodder.

## EntityRedAnt

### ENT-D-024 — EntityRedAnt: HP doubled, speed raised
- **Status:** DIVERGENT
- **Original:** `EntityRedAnt.java` — HP 1, speed 0.15, atk 1
- **Port:** `entity/EntityRedAnt.java` — HP 2.0, speed 0.2, atk 1.0
- **Fix:** set HP 1, MOVEMENT_SPEED 0.15.

## EntityThrownRock

### ENT-D-025 — EntityThrownRock: type-5 damage halved
- **Status:** DIVERGENT
- **Original:** `EntityThrownRock.java:79-216` — t5 = 10 (t1=2, t2–4=5, t6=20, t7/8=40, t9–11=150, t12=250)
- **Port:** `entity/EntityThrownRock.java:72-79` — t5 folded into the 5-damage band
- **Fix:** restore `case 5 -> 10` in the damage switch.

### ENT-D-026 — EntityThrownRock: five rock types have wrong effects
- **Status:** DIVERGENT
- **Original:** `:107-227` — t6 weakness 100; t9 fire 50t + weakness 100; t10 poison 200 + weakness 100; t11 slow 200 + weakness 100; t12 weakness 100 + explosion 5.1
- **Port:** `:94-122` — t6/9/11 wither 100; t9 lost ignite; t10 poison 200 only; t12 wither 100
- **Fix:** replace WITHER with WEAKNESS (100t) on t6/9/10/11/12; re-add t9 50t ignite, t10/t11 weakness secondary, t11 slow 200.

### ENT-D-027 — EntityThrownRock: typed rock recovery and glass-breaking lost
- **Status:** DIVERGENT
- **Original:** `:229-285` — breaks glass on impact; returns the specific rock item of its type (MySmallRock…MyCrystalTNTRock, 12 types)
- **Port:** `:129` — always pops generic `ModItems.ROCK`; no glass-breaking
- **Fix:** map rock type → corresponding ModItems rock item on drop; add glass-block break on impact (mobGriefing-gated).

### ENT-D-028 — EntityThrownRock: water-skipping physics missing
- **Status:** PARTIAL
- **Original:** `:290-313` — water-skip physics (alongside 30°/tick spin + 1000t lifetime, which are ported)
- **Port:** `:135-148` — spin/lifetime ✓, no water skip
- **Fix:** on water surface contact with sufficient horizontal velocity, reflect vertical motion (port the original skip branch).

## Fairy

### ENT-D-029 — Fairy: Crystal Torch drop → glowstone
- **Status:** DIVERGENT
- **Original:** `Fairy.java` drops — Crystal Torch
- **Port:** `fairy.json` — glowstone_dust 1–3 (+looting)
- **Fix:** change JSON to the port's Crystal Torch item ×1.

### ENT-D-030 — Fairy: roofed-forest hotspot diluted overworld-wide
- **Status:** DIVERGENT
- **Original:** `OreSpawnMain.java:4974` — w25 2–4 ambient Roofed Forest only; + Crystal dim w10 4–8, w5 2–4
- **Port:** `add_overworld_ambient.json` w5 1–3 ALL overworld (Crystal/Chaos dims kept)
- **Fix:** restrict the overworld modifier to `minecraft:dark_forest` w25 2–4.

## Firefly

### ENT-D-031 — Firefly: ExtremeTorch drop → glowstone
- **Status:** DIVERGENT
- **Original:** `Firefly.java` — drops ExtremeTorch
- **Port:** `firefly.json` — glowstone_dust 0–1
- **Fix:** change JSON to ExtremeTorch ×1.

## Flounder

### ENT-D-032 — Flounder: Utopia waters → vanilla oceans
- **Status:** DIVERGENT
- **Original:** Utopia water lists w2 2–4 / w5 6–8 (`BiomeGenUtopianPlains.java:126,253`)
- **Port:** `add_ocean_spawns.json` w8 1–3 `#minecraft:is_ocean`
- **Fix:** add Flounder to the Utopia-dimension water spawn lists at original weights (keep or drop ocean entry per design decision).

## Frog

### ENT-D-033 — Frog: river/swamp focus → all overworld
- **Status:** DIVERGENT
- **Original:** `OreSpawnMain.java:4963-4967` — waterCreature w20 3–6 river, w20 2–6 swamp, ambient w2–3
- **Port:** `add_overworld_creatures.json` w10 1–2 (rules Y≥50/day/≤5 frogs kept)
- **Fix:** retarget modifier to river+swamp biomes w20 3–6 / 2–6.

## GammaMetroid

### ENT-D-034 — GammaMetroid: stats cut ~40%
- **Status:** DIVERGENT
- **Original:** `OreSpawnMain.java:6486` — HP 100, atk 10, def 12
- **Port:** `entity/EntityGammaMetroid.java` — HP 60, atk 8, no armor
- **Fix:** set HP 100, ATTACK 10, ARMOR 12.

### ENT-D-035 — GammaMetroid: gold nuggets → gunpowder
- **Status:** DIVERGENT
- **Original:** `GammaMetroid.java:227-231` — gold nuggets + iron ingots
- **Port:** `gamma_metroid.json` — gunpowder 5–14 + iron 6–15
- **Fix:** change gunpowder pool to gold_nugget (match original counts from `GammaMetroid.java:227-231`).

### ENT-D-036 — GammaMetroid: Crystal-dim swarms → Nether/mountain singles
- **Status:** DIVERGENT
- **Original:** Crystal dimension list w35 4–7 (`ChunkProviderOreSpawn2.java:386`) + Utopia boss w1 (`BiomeGenUtopianPlains.java:514`)
- **Port:** `add_nether_spawns.json` w3 1–1 + `companion_gamma_metroid.json` mountains w1 1–1
- **Fix:** add to Crystal-dimension spawn list w35 4–7 and Utopia w1; remove the Nether entry.

## Gazelle

### ENT-D-037 — Gazelle: poppy + super drops → mutton
- **Status:** DIVERGENT
- **Original:** `Gazelle.java:347` — poppy + super drops
- **Port:** `gazelle.json` — mutton 1–3 (+looting)
- **Fix:** change JSON to poppy ×1 and port the "super drops" bonus from `Gazelle.java`; remove mutton.

## Ghost

### ENT-D-038 — Ghost: bone drops invented
- **Status:** DIVERGENT
- **Original:** `Ghost.java` — no notable drops
- **Port:** `ghost.json` — bone 0–2 (+looting)
- **Fix:** empty the loot pools.

### ENT-D-039 — Ghost: spawn density slashed (w15 ambient → w4 caves)
- **Status:** DIVERGENT
- **Original:** ~28 addSpawn ambient w2–15 grp up to 5–10 (`OreSpawnMain.java:4544+,4784-4788`)
- **Port:** `add_cave_spawns.json` w4 1–1 + `dim_chaos_locals.json` w15 3–6; dark-only rule
- **Fix:** raise overworld weight/groups toward the original w2–15 / 5–10 ambient distribution across its biome list.

## GhostSkelly

### ENT-D-040 — GhostSkelly: bone/arrow drops invented
- **Status:** DIVERGENT
- **Original:** no notable drops
- **Port:** `ghost_skelly.json` — bone 1–3 (+looting) + arrows 0–2
- **Fix:** empty the loot pools.

### ENT-D-041 — GhostSkelly: spawn density slashed
- **Status:** DIVERGENT
- **Original:** ~28 addSpawn ambient w2–15 (`OreSpawnMain.java:4522-4543,4791-4795`)
- **Port:** `add_cave_spawns.json` w4 1–1 + `dim_chaos_locals.json` w10 2–4
- **Fix:** restore original ambient weights/groups across the original biome list.

## GiantRobot

### ENT-D-042 — GiantRobot: stats ×4 and entity split into GiantRobot + Jeffery
- **Status:** DIVERGENT
- **Original:** ONE entity using `Jeffery_stats` — HP 550, atk 40, def 18 (`OreSpawnMain.java:6476`; `GiantRobot.java:63-65,97,105`); XP = health/2 = 275
- **Port:** `entity/GiantRobot.java:56-62` — HP 2000, atk 100, armor 12, XP 500; plus separate `entity/Jeffery.java:22-28` — HP 1000/50/6 XP 250
- **Fix:** set GiantRobot to HP 550, ATTACK 40, ARMOR 18, XP 275; either delete the Jeffery duplicate or make it a named-skin alias with identical stats.

### ENT-D-043 — GiantRobot: WanderALot and MoveThroughVillage goals dropped
- **Status:** PARTIAL
- **Original:** `GiantRobot.java:53-58` — swim(0), WanderALot(1,14,1.0), MoveThroughVillage(2), Watch(3), LookIdle(4)
- **Port:** `entity/GiantRobot.java:48-54` — Float, Stroll, LookAt, RandomLook only
- **Fix:** add `MyEntityAIWanderALot(1,14,1.0)` (class exists in port) and a MoveThroughVillage equivalent at priority 2.

### ENT-D-044 — GiantRobot: signature LaserBall barrage missing
- **Status:** MISSING
- **Original:** `GiantRobot.java:264-283` — fires LaserBall: aims within 0.5 rad, reload 10 close (vol 2.5 pitch 1.0) / 25 + `setSpecial()` far >100 distSq (vol 3.5 pitch 0.5), launch offset y+10
- **Port:** melee only; `reloadTicker` field exists but no firing code
- **Fix:** implement the ranged attack in `customServerAiStep`: aim gate 0.5 rad, reload 10/25 by range, `setSpecial()` on far shots, y+10 launch offset, original volumes/pitches.

### ENT-D-045 — GiantRobot: kit/RayGun drops lost
- **Status:** DIVERGENT
- **Original:** `GiantRobot.java:158-211` — 15–29× LaserBall(×4) + 10–19 random of {SpiderRobotKit, AntRobotKit, RayGun, redstone block, dispenser, sticky piston, piston, lever, iron block, piston-head}
- **Port:** `giant_robot.json` — iron 5–10 + 30% iron blocks 1–3
- **Fix:** rewrite JSON: laser-ball item 15–29 (×4 stacks) + 10–19 rolls over the 10-entry kit/component pool.

### ENT-D-046 — GiantRobot: no natural spawn
- **Status:** MISSING
- **Original:** Utopia monster list w8 1–2 (`BiomeGenUtopianPlains.java:289`); rules Y≥50, night, 5 air above (`GiantRobot.java:364-381`)
- **Port:** no biome modifier for giant_robot or jeffery (config toggle exists, `ModSpawnControl.java:97`)
- **Fix:** add GiantRobot to the Utopia dimension spawn list w8 1–2 with checkSpawnRules (Y≥50, night, 5 clear blocks above).

## Girlfriend

### ENT-D-047 — Girlfriend: Valentine's Day 800-HP mode missing
- **Status:** PARTIAL
- **Original:** `Girlfriend.java:569-571` — HP 800 on Valentine's Day (else 80)
- **Port:** `entity/Girlfriend.java:51-52,83-88` — HP 80 always
- **Fix:** check system date (Feb 14) on spawn/load and set max health 800 with heal.

### ENT-D-048 — Girlfriend: dance, jealousy, Valentine targeting, door/indoor AI missing
- **Status:** PARTIAL
- **Original:** `Girlfriend.java:149-173` — `MyEntityAIDance(3)`, OpenDoor(10), MoveIndoors(11); targets MyValentineTarget ×2, MyEntityAIJealousy ×2
- **Port:** `entity/Girlfriend.java:70-80` — none wired (port's `MyEntityAIDance.java` exists but is NOT registered); no Jealousy/Valentine classes
- **Fix:** register `MyEntityAIDance` at priority 3; port Jealousy and ValentineTarget goal classes; add OpenDoor/MoveIndoors.

### ENT-D-049 — Girlfriend: ranged UltimateArrow attack missing
- **Status:** MISSING
- **Original:** `Girlfriend.java` — `EntityAIArrowAttack(4, 1.25, 20t, 10.0f)` + IRangedAttackMob firing UltimateArrow
- **Port:** `MeleeAttackGoal(4)` only
- **Fix:** implement `RangedAttackMob` with an arrow-attack goal (speed 1.25, 20t interval, 10.0 range) firing UltimateArrow.

### ENT-D-050 — Girlfriend: spawn hotspots flattened
- **Status:** DIVERGENT
- **Original:** `OreSpawnMain.java:4574-4585` — 12 addSpawn w2–30, groups up to 8–15
- **Port:** `companion_girlfriend.json` — overworld-wide w4 1–2
- **Fix:** replicate the 12 per-biome entries with original weights/groups (w30 8–15 hotspots).

## GoldCow

### ENT-D-051 — GoldCow: hardcoded gold-ingot bonus invented
- **Status:** DIVERGENT
- **Original:** `GoldCow.java` — golden apples only
- **Port:** `gold_cow.json` (leather 1–3 + golden_apple 1) + hardcoded `dropCustomDeathLoot` 1–3 GOLD_INGOT (double, ENT-SYS-001)
- **Fix:** delete the `dropCustomDeathLoot` override; align JSON golden-apple count with `GoldCow.java`. (Note: port splits original into `gold_cow` + `golden_apple_cow` ids.)

## GoldFish

### ENT-D-052 — GoldFish: drops missing entirely
- **Status:** MISSING
- **Original:** `GoldFish.java` `func_146068_u` — gold-related items
- **Port:** `gold_fish.json` — empty pools, no custom loot
- **Fix:** read `GoldFish.java` `func_146068_u` for the exact item/count and populate the JSON (gold nugget per original).

### ENT-D-053 — GoldFish: Utopia habitat → oceans/Chaos
- **Status:** DIVERGENT
- **Original:** Utopia lists w1/w5/w10 (`BiomeGenUtopianPlains.java:120,176,368`)
- **Port:** `add_ocean_spawns.json` w10 1–3 + `dim_chaos_locals.json` w10 2–4
- **Fix:** add GoldFish to the Utopia dimension lists at original weights; review whether ocean entry should remain.

## Hammerhead

### ENT-D-054 — Hammerhead: attack cut 75→20, armor dropped
- **Status:** DIVERGENT
- **Original:** `OreSpawnMain.java:6477`; `Hammerhead.java:59-83` — HP 240, atk 75, def 20
- **Port:** `entity/Hammerhead.java:38-40` — HP 200, atk 20, no armor
- **Fix:** set HP 240, ATTACK 75, ARMOR 20.

### ENT-D-055 — Hammerhead: boss bar invented
- **Status:** DIVERGENT
- **Original:** no boss bar
- **Port:** `entity/Hammerhead.java:42` — `ServerBossEvent` added
- **Fix:** remove the boss bar (or keep behind config; Hammerhead is not a boss in the original).

### ENT-D-056 — Hammerhead: four unique reward items lost
- **Status:** DIVERGENT
- **Original:** `Hammerhead.java:126-147` — XP bottle, ExperienceCatcher, CreeperLauncher, CreeperRepellent, raw beef, ExperienceTreeSeed, MyHammy
- **Port:** `hammerhead.json` (prismarine 5–8 + experience_catcher 5–10) + hardcoded 8 XP bottles + 6 bones (double, ENT-SYS-001)
- **Fix:** single JSON: XP bottles, experience_catcher, creeper_launcher, creeper_repellent, beef, experience_tree_seed, hammy (counts from `Hammerhead.java:126-147`); delete hardcoded loot and prismarine/bones.

### ENT-D-057 — Hammerhead: Utopia → oceans relocation
- **Status:** DIVERGENT
- **Original:** Utopia monster list w1 1–1 (`BiomeGenUtopianPlains.java:463`)
- **Port:** `add_ocean_spawns.json` w3 1–1 (Y≥50 + no-buddy rules kept)
- **Fix:** add to Utopia dimension monster list w1 1–1; review the ocean entry.

## HerculesBeetle

### ENT-D-058 — HerculesBeetle: stats cut 20–50%
- **Status:** DIVERGENT
- **Original:** `OreSpawnMain.java:6468` — HP 250, atk 30, def 19
- **Port:** `EntityHerculesBeetle.java:53-58` — HP 200, atk 15, no armor
- **Fix:** set HP 250, ATTACK 30, ARMOR 19.

### ENT-D-059 — HerculesBeetle: BigHammer + enchanted gear loot gutted
- **Status:** DIVERGENT
- **Original:** `HerculesBeetle.java:141-288` — MyBigHammer + painting + raw beef + enchanted diamond gear set
- **Port:** `hercules_beetle.json` (bones 3–6 + gunpowder 2–5) + hardcoded name_tag + 4–11 bones (double, ENT-SYS-001)
- **Fix:** single JSON: big_hammer ×1, painting ×1, beef, enchanted diamond gear pool (enchant_randomly); delete hardcoded loot.

## Hydrolisc

### ENT-D-060 — Hydrolisc: HP buffed 66%, speed raised
- **Status:** DIVERGENT
- **Original:** `Hydrolisc.java:74` + mygetMaxHealth — HP ~60, atk 1.0, speed 0.2
- **Port:** `EntityHydrolisc.java:75-80` — HP 100, atk 1.0, speed 0.25
- **Fix:** set HP 60, MOVEMENT_SPEED 0.2.

### ENT-D-061 — Hydrolisc: swamp/jungle density → sparse coastal
- **Status:** DIVERGENT
- **Original:** `OreSpawnMain.java:4829-4832` — creature w25 3–6 swamp, w15 2–5 jungle, w10 1–3 jungleHills, w5 3–6
- **Port:** ocean w3 1–1 + beach w3 1–2 + river w3 1–2
- **Fix:** retarget modifiers to swamp w25 3–6 and jungle w15 2–5 (+jungle hills w10 1–3); drop ocean/beach.

## Irukandji

### ENT-D-062 — Irukandji: melee attack 10× original, HP 5×
- **Status:** DIVERGENT
- **Original:** `OreSpawnMain.java:6509`; `Irukandji.java:57-85` — HP 1, atk 20, def 0
- **Port:** `entity/Irukandji.java:35-37` — HP 5, atk 200.0 (note: 200 belongs only to the empty-hand retaliation, which is correctly ported separately)
- **Fix:** set HP 1, ATTACK_DAMAGE 20; keep the separate 200.0 empty-hand retaliation constant.

### ENT-D-063 — Irukandji: Utopia waters → oceans
- **Status:** DIVERGENT
- **Original:** Utopia water list w4 2–3 (`BiomeGenUtopianPlains.java:256`)
- **Port:** `add_ocean_spawns.json` w4 1–2 (Y≥50, 1/60 roll, ≤2 nearby rules kept)
- **Fix:** add to Utopia dimension water list w4 2–3; review ocean entry.

## IrukandjiArrow

### ENT-D-064 — IrukandjiArrow: base damage parity unverified
- **Status:** UNVERIFIED
- **Original:** `IrukandjiArrow.java:173-180` — damage scaled by velocity + `nextInt(dmg/2+2)` crit bonus via custom `func_70239_b`; base value buried in decompiled arrow math
- **Port:** `entity/IrukandjiArrow.java` — extends `AbstractArrow`, base damage 6.0
- **Fix:** verification failed because the original base damage is entangled in CFR-decompiled velocity/crit math rather than a named constant. Evidence to resolve: trace `func_70239_b` callers in `IrukandjiArrow.java` (and the bow that fires it) to extract the seeded damage value, then compare to the port's 6.0. Do not assume parity.

### ENT-D-065 — IrukandjiArrow: debuff durations/amplifiers not number-matched
- **Status:** PARTIAL
- **Original:** poison/weakness/slowness applied on hit; exact durations/amps in original constants not extracted
- **Port:** Poison + Weakness + Slowness, 200 ticks, amps 1/2
- **Fix:** read the original potion-application calls in `IrukandjiArrow.java` and align port durations/amplifiers; current 200t/amps 1–2 are plausible but unconfirmed.

---

## Tally

(See final response for counts by status.)
