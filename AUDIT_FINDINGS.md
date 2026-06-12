# OreSpawn Port — Audit Findings Register

**Companion to:** `AUDIT_INVENTORY.md` (full checklist). Detailed audit evidence in `audit_sections/01–10`.
**Scope:** every MISSING / PARTIAL / DIVERGENT / UNVERIFIED item, every bug, every optimization opportunity — each with file/line refs in both codebases and a concrete fix.
**Original:** `reference_1_7_10_source/sources/danger/orespawn/` · **Port:** `src/main/java/danger/orespawn/` + `src/main/resources/`
**Status: REPORT ONLY — nothing has been fixed. Awaiting go-ahead.**

---

## Summary

### How much is truly ported?

"PORTED" below means *verified equivalent by reading both implementations* — never inferred from a matching name. UNVERIFIED items are counted as not-ported.

| Category | Verified PORTED | Total audited | % truly ported |
|---|---|---|---|
| Entities (non-boss) | 27 | 130 | **21%** |
| Boss sub-features | 58 | 110 | **53%** |
| Blocks / items / tiers / recipes / config | ~139 | ~270 | **~51%** |
| Worldgen / dimensions / structures | 30 | 81 | **37%** |
| Animations / events / GUI | 16 | 38 | **42%** |
| **Overall** | **~270** | **~629** | **~43%** |

Caveats: entity-level "PORTED" is strict (one divergent sub-feature demotes the entity); the recipe corpus was only spot-checked (~20 diffed; bulk marked UNVERIFIED — see ITEM findings); `% truly ported` would drop further if sub-feature rows were the unit for non-boss entities.

### Findings counts

| Type | Count | Breakdown |
|---|---|---|
| Port-parity findings | 543 | DIVERGENT 305 · PARTIAL 151 · MISSING 66 · UNVERIFIED 21 |
| Bugs (Phase 3) | 31 | CRITICAL 7 · HIGH 6 · MEDIUM 9 · LOW 9 |
| Optimizations (Phase 4) | 27 | HIGH 8 · MEDIUM 11 · LOW 8 (20 behavior-neutral / 4 affecting / 3 mixed) |
| **Total entries** | **601** | |

### Top 10 most severe issues

1. **Startup crash: MultiHitboxLib handlers on wrong event bus** — `multihitboxlib/EntityEventHandler.java:15`, `GameEventHandler.java:9` subscribe game-bus events (`EntityEvent.Size`, `PlayerLoggedInEvent`) on the MOD bus → `IllegalArgumentException` at launch; all multipart hitbox sync dead. (BUG-001/002)
2. **Server crash: spawner-spawned Rats** — `EntityRat.java:139` `UUID.fromString("")` on empty `myOwner` → ticking-entity crash; Crystal dungeons place rat spawners, so this fires in normal play. (BUG-003)
3. **Server crash: Prince growth chain** — `ThePrince.java:241` (+Teen:207/246, Adult:249) calls `tame(getPlayerByUUID(owner))` with owner offline → NPE during auto-triggered counter-based growth. (BUG-004)
4. **Player-state destruction** — `TheQueen.java:486-502` `discard()`s a `ServerPlayer` (player deleted, no respawn screen, ghost connection); `Godzilla.java:422-441` shockwave `genericKill` bypasses Creative/Spectator invulnerability. (BUG-005/006)
5. **Save corruption** — `SpiderRobot.java:199-202` NBT overrides skip `super` → health/effects/persistence wiped every save; `EntityWormLarge.java:30` `wormsSpawned` unsaved → ~40 fresh worms per chunk reload (entity bomb). (BUG-007/008)
6. **Systemic double drops** — ~25 entities incl. TheKing/TheQueen/Mobzilla run hardcoded `dropCustomDeathLoot` **and** a JSON loot table; every kill awards two full loot sets. (ENT-SYS-001, ENT-SYS2-001, BOSS findings)
7. **Stats integrity collapse** — port `MobStats.java` is dead code; hardcoded attributes drift up to 10× from original (Irukandji attack 20→200, Kraken HP 1000→3000, TheKing 7000/350/21→6000/250/12, armor dropped to 0 on most mobs). (ENT-SYS entries + per-entity)
8. **Signature mechanics missing** — rider flight on 6 of 7 mounts (Cephadrome/Leon/Ostrich/Leonopteryx/Elevator/Prince line; only Dragon flies), all Prince/Princess ranged canons, Worm armor-theft, custom fishing economy, EasterBunny egg-laying, Spyro→Dragon evolution. (ANIM-/ENT- findings)
9. **Worldgen gaps make content unreachable** — Village dimension has no villages (MapGenMoreVillages unported), Village & Islands unreachable in survival (no ant spawns/AntHill gen), ~25 structure types missing, dungeon spawner pool 50→2, dungeon loot replaced with vanilla `simple_dungeon`. (WGEN findings)
10. **Systemic quality regressions** — `wingspeed`→`limbSwingAmount` animation mistranslation across 39 model files (idle anims freeze/jitter); all 14 armor sets at ~1/15 durability with durability misread as enchantability; all 8 dispenser behaviors missing; OreGenericEgg self-duplication exploit. (ANIM-001, ITEM-054, ITEM-063, ITEM-/block findings)

---

*The remainder of this file is the complete findings register, in four parts. Entry IDs are stable for referencing in fix work: ENT-SYS/ENT-A/ENT-D (part 1), ENT-SYS2/ENT-K/ENT-S/BOSS (part 2), ITEM/WGEN (part 3), ANIM/BUG/OPT (part 4).*


---

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
- **Resolution:** FIXED (2026-06-11, Phase B — drops consolidated into loot-table JSONs; TheKing/BandP remain documented code exceptions, see FIX_LOG.md and phase_b_reports/B1_drops.md)

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
- **Resolution:** FIXED (2026-06-11, Phase C — see FIX_LOG.md and phase_c_reports/C4_entities_S_Z.md)

---

## Acid / LaserBall

### ENT-A-001 — Acid: inherited LaserBall entity-immunity list deleted
- **Status:** DIVERGENT
- **Original:** `LaserBall.java` — special immunities for TrooperBug/SpitBug/Robot2–5/GiantRobot when projectile is acid-type
- **Port:** `entity/LaserBall.java` — all entity immunities removed; Acid (and every LaserBall subclass) now damages those mobs
- **Fix:** restore the acid-immunity checks in the port `LaserBall` hit logic (skip damage when target is TrooperBug/SpitBug/Robot2–5/GiantRobot and acid flag set).
- **Resolution:** PARTIAL (2026-06-11, Phase C — Robot2-5/GiantRobot/ridden-Dragon/ridden-Player immunities restored in LaserBall.onHitEntity; TrooperBug/SpitBug remain unported (Phase D); see FIX_LOG.md and phase_c_reports/C1_entities_A_C.md)

## Alien

### ENT-A-002 — Alien: combat stats and hitbox reduced
- **Status:** DIVERGENT
- **Original:** `OreSpawnMain.java:6491` — HP 100, atk 12, armor 8; `Alien.java` ctor — size 1.1×3.25
- **Port:** `entity/Alien.java` `createAttributes` — HP 80, armor 6; `ModEntities.java` — size 0.6×1.8
- **Fix:** set MAX_HEALTH 100, ARMOR 8 in `createAttributes`; set entity dimensions to 1.1×3.25 in `ModEntities`.
- **Resolution:** FIXED (2026-06-11, Phase C — hitbox remainder closed in Phase C: 1.1x3.25 set in ModEntities; see FIX_LOG.md and phase_c_reports/C1_entities_A_C.md)

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
- **Resolution:** FIXED (2026-06-11, Phase C — audit corrected: orig applies POISON (not Hunger), duration 40t Easy / 30t otherwise; see FIX_LOG.md and phase_c_reports/C1_entities_A_C.md)

### ENT-A-005 — Alien: drop table replaced
- **Status:** DIVERGENT
- **Original:** `Alien.java` `func_70628_a` — gold nuggets 5–10, iron ingots 5–10, ender pearl 1, compass 1, clock 1
- **Port:** `loot_table/entities/alien.json` — gunpowder 5–10, iron 5–10, ender pearl 1–3; no compass/clock
- **Fix:** edit `alien.json`: gunpowder→gold nuggets 5–10, ender pearl count 1, add compass ×1 and clock ×1 pools.
- **Resolution:** FIXED (2026-06-11, Phase C — audit corrected: orig drops spider eye 5-10, flint 5-10, map/clock/compass x1; alien.json rewritten; see FIX_LOG.md and phase_c_reports/C1_entities_A_C.md)

### ENT-A-006 — Alien: spawn habitat moved from underground/Utopia to the End
- **Status:** DIVERGENT
- **Original:** `Alien.java` `func_70601_bi` — spawner tag "Alien", or dim 4 (Utopia), or underground y<50 & dark; no `addSpawn`
- **Port:** `add_end_spawns.json` — `#minecraft:is_end` weight 3, 1–1
- **Fix:** remove alien from `add_end_spawns.json`; add an overworld monster modifier plus `checkSpawnRules` for y<50 & dark, and add to the Utopia dimension spawn list.
- **Resolution:** PARTIAL (2026-06-11, Phase C — End spawn removed; audit corrected: orig has a Mining-dim spawn entry w35 2-3 (ChunkProviderOreSpawn2.java:389), restored in dim_mining_locals.json; spawn-rule gates (dark/y<50/Utopia) tracked by ENT-SYS-002 (Phase D); see FIX_LOG.md and phase_c_reports/C1_entities_A_C.md)

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
- **Resolution:** FIXED (2026-06-11, Phase B — see FIX_LOG.md and phase_b_reports/B2_mobstats.md)

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
- **Resolution:** FIXED (2026-06-11, Phase B — see FIX_LOG.md and phase_b_reports/B1_drops.md)

### ENT-A-011 — Alosaurus: spawn biomes/conditions changed
- **Status:** DIVERGENT
- **Original:** `addSpawn` multiple biomes; `func_70601_bi` y>50, night, not raining, no other Alosaurus
- **Port:** `hostile_alosaurus.json` `#minecraft:is_savanna` w1 2–3; conditions deleted (ENT-SYS-002)
- **Fix:** restore original biome list in the modifier; add spawn rules (y>50, night, !raining, no nearby Alosaurus).
- **Resolution:** PARTIAL (2026-06-11, Phase C — audit corrected: orig has NO overworld addSpawn; spawns were Mining dim w8 1-2 + Utopia plains w1 1-1, both restored, overworld modifiers removed; spawn rules tracked by ENT-SYS-002 (Phase D); see FIX_LOG.md and phase_c_reports/C1_entities_A_C.md)

## AntRobot

### ENT-A-012 — AntRobot: stats and hitbox rewritten
- **Status:** DIVERGENT
- **Original:** `OreSpawnMain.java:6475` — HP 300, atk 30, def 16; XP = health/2 = 150 (`AntRobot.java:58`); size 2.75×1.25
- **Port:** `entity/AntRobot.java:71-77` — HP 350, atk 35, armor 6; `ModEntities.java:548` size 2.0×3.0 (W/H swapped)
- **Fix:** set HP 300, ATTACK 30, ARMOR 16; dimensions 2.75 wide × 1.25 tall in `ModEntities`.
- **Resolution:** FIXED (2026-06-11, Phase C — hitbox remainder closed in Phase C: 2.75x1.25 set in ModEntities; see FIX_LOG.md and phase_c_reports/C1_entities_A_C.md)

### ENT-A-013 — AntRobot: melee throttle removed (attack rate massively higher)
- **Status:** DIVERGENT
- **Original:** `AntRobot.java` `func_70619_bc:96-147` — 1-in-15 melee attempt at range <(6+w/2); melee damage = atk attribute
- **Port:** `entity/AntRobot.java:108-125` — melee fires every `customServerAiStep` tick in range; damage hardcoded 35.0
- **Fix:** gate melee behind `random.nextInt(15)==0`; read damage from ATTACK_DAMAGE attribute instead of hardcoding.
- **Resolution:** FIXED (2026-06-11, Phase C — unridden melee gated on 1-in-15 per tick (orig AntRobot.java:130-145); see FIX_LOG.md and phase_c_reports/C1_entities_A_C.md)

### ENT-A-014 — AntRobot: ridden 1-in-50 stomp missing
- **Status:** DIVERGENT
- **Original:** `AntRobot.java` `func_70071_h_:617-631` — when ridden: 1-in-50 stomp + 1-in-9 melee; stomp = atk/10 = 3.0
- **Port:** `entity/AntRobot.java` — ridden 1-in-9 melee only; stomp value 3.5
- **Fix:** add the 1-in-50 ridden stomp call; set stomp damage to atk/10.
- **Resolution:** FIXED (2026-06-11, Phase C — ridden 1-in-50 stomp restored, damage = ATTACK_DAMAGE/10 (orig AntRobot.java:617-619,1000); see FIX_LOG.md and phase_c_reports/C1_entities_A_C.md)

### ENT-A-015 — AntRobot: drop table replaced (redstone-component loot lost)
- **Status:** DIVERGENT
- **Original:** `AntRobot.java` `func_70628_a:1112-1164` — 7–13 rolls of redstone/repeater/comparator/redstone block/dispenser/sticky piston/piston/lever/pressure plate/iron ingot
- **Port:** `ant_robot.json` — iron 3–8 + gold 1–3
- **Fix:** rewrite `ant_robot.json` as 7–13 rolls over a uniform pool of the 10 original redstone-component items.
- **Resolution:** FIXED (2026-06-11, Phase C — ant_robot.json rewritten to the orig 7-13 rolls of d10 redstone-component table; see FIX_LOG.md and phase_c_reports/C1_entities_A_C.md)

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
- **Resolution:** FIXED (2026-06-11, Phase C — dims remainder closed in Phase C: 1.0x1.25 set in ModEntities; see FIX_LOG.md and phase_c_reports/C1_entities_A_C.md)

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
- **Resolution:** FIXED (2026-06-11, Phase B — see FIX_LOG.md and phase_b_reports/B1_drops.md)

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
- **Resolution:** FIXED (2026-06-11, Phase C — size remainder closed in Phase C: 0.75x1.75 set in ModEntities; audit corrected: orig BandP has no worn-gear armor clamp (flat defense), so none was added; see FIX_LOG.md and phase_c_reports/C1_entities_A_C.md)

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
- **Resolution:** FIXED (2026-06-11, Phase C — steals on every successful melee hit, 100-slot stash (orig BandP.java:46,185-218); see FIX_LOG.md and phase_c_reports/C1_entities_A_C.md)

### ENT-A-026 — BandP: drops nuggets→ingots
- **Status:** DIVERGENT
- **Original:** `BandP.java` — 10–14 leather; if `getWhat()==0` 2–4 Uranium+Titanium **nuggets**; all stolen items
- **Port:** `band_p.json` — leather 10–14, uranium **ingot** 0–3@50%, titanium **ingot** 0–3@50%
- **Fix:** change JSON entries to uranium_nugget / titanium_nugget 2–4 gated on the `getWhat()==0` condition (or move to code if loot conditions can't express it).
- **Resolution:** VERIFIED-CORRECT (2026-06-11, Phase C — audit wrong: orig drops emeralds (field_151166_bC), port band_p.json already matches; see phase_c_reports/C1_entities_A_C.md)

### ENT-A-027 — BandP: spawn conditions deleted
- **Status:** DIVERGENT
- **Original:** plains/desert/savanna w20 1–2; night, y≥50, villager-count condition
- **Port:** `add_overworld_monsters.json` w3 1–1; no conditions
- **Fix:** dedicated modifier for plains/desert/savanna w20 1–2; restore night + y≥50 + nearby-villager spawn rule (ENT-SYS-002).
- **Resolution:** PARTIAL (2026-06-11, Phase C — plains/desert/savanna w20 1-2 modifier restored (hostile_band_p.json), removed from generic overworld list; night/y>=50/villager rules tracked by ENT-SYS-002 (Phase D); see FIX_LOG.md and phase_c_reports/C1_entities_A_C.md)

## Baryonyx

### ENT-A-028 — Baryonyx: bones invented in loot
- **Status:** PARTIAL
- **Original:** `Baryonyx.java` `func_70628_a` — 2–6 raw beef only
- **Port:** `baryonyx.json` bones 2–5 **plus** custom 2–6 beef (double source, ENT-SYS-001)
- **Fix:** make `baryonyx.json` beef 2–6 only; remove the bones pool and the custom death loot.
- **Resolution:** FIXED (2026-06-11, Phase B — see FIX_LOG.md and phase_b_reports/B1_drops.md)

### ENT-A-029 — Baryonyx: mining-dimension habitat → generic overworld
- **Status:** DIVERGENT
- **Original:** mining-dim biomes (`BiomeGenUtopianPlains`, `ChunkProviderOreSpawn2` w2 4–8); rules y>50, day, ≤8 buddies
- **Port:** `add_overworld_creatures.json` w3 1–1; conditions removed
- **Fix:** add Baryonyx to the port's mining-dimension spawn lists w2 4–8; restore day/y>50/buddy-cap rules.
- **Resolution:** PARTIAL (2026-06-11, Phase C — Mining-dim w2 4-8 + Utopia w2 2-4 restored, overworld entry removed; day/y>50/buddy-cap rules tracked by ENT-SYS-002 (Phase D); see FIX_LOG.md and phase_c_reports/C1_entities_A_C.md)

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
- **Resolution:** FIXED (2026-06-11, Phase C — fire-immunity remainder closed in Phase C: fireImmune() set in ModEntities (orig Basilisk.java:52); see FIX_LOG.md and phase_c_reports/C1_entities_A_C.md)

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
- **Resolution:** FIXED (2026-06-11, Phase C — extraneous Slowness removed from the bite; gaze goal carries the orig effects; see FIX_LOG.md and phase_c_reports/C1_entities_A_C.md)

### ENT-A-034 — Basilisk: loot content diverged
- **Status:** DIVERGENT
- **Original:** BasiliskScale 1, Item Frame 1, 12–17 emeralds, 8–12 cooked cod, 3–7 bonus rolls (1-in-15 enchanted emerald gear)
- **Port:** `basilisk.json` (scale, name_tag, emerald 12–17, raw cod 8–12) plus custom (scale, golden apple, emeralds, gold ingots, gear rolls) — double, ENT-SYS-001
- **Fix:** single JSON: scale ×1, item_frame ×1 (not name_tag), emerald 12–17, **cooked** cod 8–12, 3–7 bonus rolls with 1-in-15 enchanted emerald-gear pool; delete custom death loot.
- **Resolution:** FIXED (2026-06-11, Phase B — see FIX_LOG.md and phase_b_reports/B1_drops.md)

### ENT-A-035 — Basilisk: spawn biomes changed
- **Status:** DIVERGENT
- **Original:** mushroom/jungle/mega-taiga w3–15; night + spawner check + no buddy
- **Port:** `hostile_basilisk__*` badlands+jungle, w3 1–1
- **Fix:** retarget modifiers to mushroom fields, jungle, old-growth taiga at original weights; restore night/buddy rules (ENT-SYS-002).
- **Resolution:** PARTIAL (2026-06-11, Phase C — audit corrected: orig biomes are jungle w3, jungleHills w2, birchForestHills w4 1-2, roofedForest w15 1-2 (OreSpawnMain.java:4877-4880), not mushroom/mega-taiga; modifiers retargeted; night/spawner/buddy rules tracked by ENT-SYS-002 (Phase D); see FIX_LOG.md and phase_c_reports/C1_entities_A_C.md)

### ENT-A-036 — Basilisk: custom sounds replaced with vanilla ravager
- **Status:** DIVERGENT
- **Original:** `basilisk_living` (1-in-2), `alo_hurt`, `emperorscorpion_death`, vol 1.0
- **Port:** `RAVAGER_ROAR/HURT/DEATH`
- **Fix:** register/use `orespawn:basilisk_living`, `orespawn:alo_hurt`, `orespawn:emperorscorpion_death` at vol 1.0.
- **Resolution:** FIXED (2026-06-11, Phase C — basilisk_living / alo_hurt / emperorscorpion_death wired (orig Basilisk.java sound methods); see FIX_LOG.md and phase_c_reports/C1_entities_A_C.md)

## Beaver

### ENT-A-037 — Beaver: drops swapped porkchops→leather
- **Status:** DIVERGENT
- **Original:** `Beaver.java` — 2–6 raw porkchops
- **Port:** `beaver.json` — 1–3 leather
- **Fix:** change `beaver.json` to porkchop 2–6.
- **Resolution:** FIXED (2026-06-11, Phase C — audit corrected: orig porkchop drop is vanilla EntityAnimal 0-2 (+looting), not 2-6; beaver.json rewritten; see FIX_LOG.md and phase_c_reports/C1_entities_A_C.md)

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
- **Resolution:** FIXED (2026-06-11, Phase C — size remainder closed in Phase C: 1.5x2.5 set in ModEntities; see FIX_LOG.md and phase_c_reports/C1_entities_A_C.md)

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
- **Resolution:** VERIFIED-CORRECT (2026-06-11, Phase C — audit wrong: orig Bee applies POISON (field_76436_u), port already applies Poison; see phase_c_reports/C1_entities_A_C.md)

### ENT-A-043 — Bee: drop table unrecognizable
- **Status:** DIVERGENT
- **Original:** 2–11 each: gold nuggets, butter candy, dandelion, sugar
- **Port:** `bee.json` (gunpowder 2–5, sugar 1–3, butter candy 0–2@50%) + custom spider eyes & red mushrooms (double, ENT-SYS-001)
- **Fix:** single JSON: gold_nugget 2–11, butter_candy 2–11, dandelion 2–11, sugar 2–11; delete custom death loot.
- **Resolution:** FIXED (2026-06-11, Phase B — see FIX_LOG.md and phase_b_reports/B1_drops.md)

### ENT-A-044 — Bee: spawn biomes/weights changed
- **Status:** DIVERGENT
- **Original:** forest/taiga biomes w2–5 1–5; day/clear-air/y>50 or Utopia rules
- **Port:** `add_overworld_monsters.json` w8 1–3
- **Fix:** dedicated forest/taiga modifier w2–5 1–5; restore rules (ENT-SYS-002).
- **Resolution:** PARTIAL (2026-06-11, Phase C — per-biome forest/jungle/birch/taiga/savanna modifiers w2-5 restored (OreSpawnMain.java:4709-4718), removed from generic overworld list; day/clear-air/y>50 rules tracked by ENT-SYS-002 (Phase D); see FIX_LOG.md and phase_c_reports/C1_entities_A_C.md)

## Bertha (item)

### ENT-A-045 — Bertha: sword damage values changed
- **Status:** DIVERGENT
- **Original:** `Bertha.java` / `OreSpawnMain.java` `bertha_stats` — damage 496 (Royal 746, Hammy 82), durability 9000
- **Port:** `item/Bertha.java` — sword-tier values differ; kills tracked via new `ModDataComponents.BERTHA_KILLS`
- **Fix:** set tier/attribute so attack damage is 496 and durability 9000 (Royal/Hammy variants 746/82 — see ENT-A-048).
- **Resolution:** VERIFIED-CORRECT (2026-06-11, Phase C — ModToolTiers already matches orig get_weaponstats: Bertha 9000 dur/496 dmg, Royal 10000/746, Hammy 2000/82; see phase_c_reports/C1_entities_A_C.md)

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
- **Resolution:** FIXED (2026-06-11, Phase C — whole skip list (players, Girlfriend, Boyfriend, tamed pets) now gated on bigBerthaPvp (orig Bertha.java:65-76); see FIX_LOG.md and phase_c_reports/C1_entities_A_C.md)

## BerthaHit (projectile)

### ENT-A-048 — BerthaHit: damage rewritten wholesale
- **Status:** DIVERGENT
- **Original:** `BerthaHit.java` — type 0 (Bertha) 496; type 2 (Royal) 746; type 3 (Hammy) 82
- **Port:** `entity/BerthaHit.java` — type 0: 250; type 2: 150; type 3: 100
- **Fix:** restore per-type damage constants 496 / 746 / 82.
- **Resolution:** FIXED (2026-06-11, Phase C — BerthaHit damages restored to 496/746/82 per hit type (orig BerthaHit.java:76-105); see FIX_LOG.md and phase_c_reports/C1_entities_A_C.md)

### ENT-A-049 — BerthaHit: per-type range collapsed
- **Status:** DIVERGENT
- **Original:** distSq < 81 / 101 / 64 per type
- **Port:** single `CLOSE_RANGE_DAMAGE_SQ = 100` for all
- **Fix:** replace the constant with per-type thresholds 81 (t0), 101 (t2), 64 (t3).
- **Resolution:** FIXED (2026-06-11, Phase C — per-type distSq ranges 81/101/64 restored (orig BerthaHit.java:76-105); see FIX_LOG.md and phase_c_reports/C1_entities_A_C.md)

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
- **Resolution:** FIXED (2026-06-11, Phase C — BerthaHit pvp gate restored incl. orig precedence quirk (Girlfriend/Boyfriend spared unconditionally); see FIX_LOG.md and phase_c_reports/C1_entities_A_C.md)

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
- **Resolution:** PARTIAL (2026-06-11, Phase C — tempt food cooked beef, PanicGoal(1.5)@6, OpenDoorGoal@10 + door navigation added; ranged attack (ENT-A-055), Jealousy goals, MoveIndoors remain (Phase D); see FIX_LOG.md and phase_c_reports/C1_entities_A_C.md)

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
- **Resolution:** FIXED (2026-06-11, Phase C — full per-biome list restored (beach w30 8-15 etc., orig OreSpawnMain.java:4588-4599); 1.18-removed hills biomes mapped to nearest modern equivalents; see FIX_LOG.md and phase_c_reports/C1_entities_A_C.md)

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
- **Resolution:** FIXED (2026-06-11, Phase C — remainder closed in Phase C: 5.0x2.0 + fireImmune() set in ModEntities; see FIX_LOG.md and phase_c_reports/C1_entities_A_C.md)

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
- **Resolution:** FIXED (2026-06-11, Phase B — see FIX_LOG.md and phase_b_reports/B1_drops.md)

### ENT-A-064 — Brutalfly: spawn biomes/rules changed
- **Status:** DIVERGENT
- **Original:** `OreSpawnMain.java:4839-4841` — ambient w2 1–1 ExtremeHillsPlus/SavannaPlateau/MesaPlateau; rules y≥70, dark, night, 4×3×10 clear air, none within 64
- **Port:** `add_overworld_monsters.json` w3 1–1; no checkSpawnRules
- **Fix:** dedicated modifier for windswept hills/savanna plateau/badlands plateau w2 1–1; restore rules (ENT-SYS-002).
- **Resolution:** PARTIAL (2026-06-11, Phase C — audit corrected: orig biomes are megaTaigaHills/extremeHillsPlus/mesaPlateau (OreSpawnMain.java:4839-4841), not savanna plateau; modifier set to old_growth_spruce_taiga/windswept_forest/badlands w2 1-1; altitude/dark/night/clear-air/64-radius rules tracked by ENT-SYS-002 (Phase D); see FIX_LOG.md and phase_c_reports/C1_entities_A_C.md)

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
- **Resolution:** FIXED (2026-06-11, Phase C — diet restored to leaves/vine/tall-grass/cactus/double-plants (orig Camarasaurus.java:114); see FIX_LOG.md and phase_c_reports/C1_entities_A_C.md)

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
- **Resolution:** FIXED (2026-06-11, Phase C — OreSpawnTamed NBT flag + entity_properties condition; poppies 2-6 tamed-only (orig Camarasaurus.java:303-312); see FIX_LOG.md and phase_c_reports/C1_entities_A_C.md)

### ENT-A-071 — Camarasaurus: spawning relocated to overworld
- **Status:** DIVERGENT
- **Original:** mining-dim chunk providers only; rules y≥50 + day (`func_70601_bi:78-83`)
- **Port:** `add_overworld_creatures.json` w2 1–1 + `companion_camarasaurus__*` jungle/savanna w1 1–1; no rules
- **Fix:** move spawns to the mining-dimension spawn lists; restore y≥50 + day rules; remove overworld modifiers.
- **Resolution:** PARTIAL (2026-06-11, Phase C — Mining-dim w1 2-4 restored, overworld modifiers removed; y>=50+day rules tracked by ENT-SYS-002 (Phase D); see FIX_LOG.md and phase_c_reports/C1_entities_A_C.md)

## CaterKiller

### ENT-A-072 — CaterKiller: stats and melee damage cut
- **Status:** DIVERGENT
- **Original:** `OreSpawnMain.java:6481` — HP 450, atk 32, def 19; size 2.9×4.6 (`CaterKiller.java:54-58`)
- **Port:** `EntityCaterKiller.java:82-88` — HP 350, atk 20, armor 0; size 1.5×1.0 (`ModEntities.java:196`)
- **Fix:** set HP 450, ATTACK 32, ARMOR 19; dimensions 2.9×4.6 (halve when PlayNicely config active, per original).
- **Resolution:** FIXED (2026-06-11, Phase C — size remainder closed in Phase C: 2.9x4.6 in ModEntities plus PlayNicely half-size via getDefaultDimensions (orig CaterKiller.java:54-58); see FIX_LOG.md and phase_c_reports/C1_entities_A_C.md)

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
- **Resolution:** FIXED (2026-06-11, Phase C — 2400t damaged timer now spawns 1 Brutalfly + 10 Butterflies with explosion sound before discard (orig CaterKiller.java:438-448); see FIX_LOG.md and phase_c_reports/C1_entities_A_C.md)

### ENT-A-075 — CaterKiller: tree-eat heal values invented
- **Status:** DIVERGENT
- **Original:** `CaterKiller.java:502-530` — scan ≤13 for leaves/vines/logs, path to it, eat at distSq<81, heal 2.0, odds 1-in-8 hurt / 1-in-30 idle
- **Port:** `EntityCaterKiller.java:218-242` — random 5×4×5 sample 1/s, heal 5 (leaf) / 10 (log), no pathing; extra 1-in-150 heal 2.0 added
- **Fix:** restore pathing scan (radius 13, eat range distSq<81), heal 2.0 flat, original trigger odds; remove invented heals and false parity comments (ENT-SYS-003).
- **Resolution:** FIXED (2026-06-11, Phase C — tree-eat heal restored: 1-in-8 hurt / 1-in-30 idle, PlayNicely-gated, nearest tree block within 12, eat at distSq<81 for 2.0 heal + 1-in-20 burp (orig CaterKiller.java:502-530); invented 5/10-HP random-munch and 1-in-150 heal removed; see FIX_LOG.md and phase_c_reports/C1_entities_A_C.md)

### ENT-A-076 — CaterKiller: reward loot degraded
- **Status:** DIVERGENT
- **Original:** `CaterKiller.java:160-328` — CaterKillerJaw 1, Item Frame 1, 10 leather, 6 beef, 1–5 rolls 13/20 chance of ultimate sword/ruby/diamond block/enchanted ruby gear/ultimate bow
- **Port:** `cater_killer.json` — jaw, name_tag, leather 6–10, slime 3–6, rolls with diamond→emerald block swap; plus custom name tag + leather + bones (double, ENT-SYS-001)
- **Fix:** single JSON: jaw ×1, item_frame ×1, leather ×10, beef ×6, 1–5 rolls @13/20 over the original pool (ultimate sword, ruby, **diamond** block, enchanted ruby gear, ultimate bow); delete custom death loot.
- **Resolution:** FIXED (2026-06-11, Phase B — see FIX_LOG.md and phase_b_reports/B1_drops.md)

### ENT-A-077 — CaterKiller: spawn biomes/rules changed
- **Status:** DIVERGENT
- **Original:** `OreSpawnMain.java:4746-4754` — ambient forest/jungle/hills/jungleEdge/birch/roofed/megaTaiga/taiga w2–10 1–2; rules day, y≥50, 1-in-10 dice, leaf/air clearance, none within 48
- **Port:** `hostile_cater_killer__*` forest/jungle/taiga/badlands w4 1–2; no rules
- **Fix:** drop badlands, restore original biome weights; add rules (ENT-SYS-002).
- **Resolution:** PARTIAL (2026-06-11, Phase C — orig biome/weight spread restored (dark_forest w10, birch w6, forest-hills w4 etc., orig OreSpawnMain.java:4746-4754), badlands dropped; day/y>=50/dice/clearance rules tracked by ENT-SYS-002 (Phase D); see FIX_LOG.md and phase_c_reports/C1_entities_A_C.md)

## CaveFisher

### ENT-A-078 — CaveFisher: stats raised, armor zeroed
- **Status:** DIVERGENT
- **Original:** `OreSpawnMain.java:6511` — HP 10, atk 4, def 4; size 1.35×0.75
- **Port:** `CaveFisher.java:64-70` — HP 25, atk 6, armor 0; size 0.8×0.8 (`ModEntities.java:48`)
- **Fix:** set HP 10, ATTACK 4, ARMOR 4; dimensions 1.35×0.75.
- **Resolution:** FIXED (2026-06-11, Phase C — size remainder closed in Phase C: 1.35x0.75 set in ModEntities; see FIX_LOG.md and phase_c_reports/C1_entities_A_C.md)

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
- **Resolution:** FIXED (2026-06-11, Phase C — passive-mob predation restored via NearestAttackableTargetGoal<Animal> (orig CaveFisher.java:193-228 excludes all EntityMob); see FIX_LOG.md and phase_c_reports/C1_entities_A_C.md)

### ENT-A-081 — CaveFisher: nugget gamble drops replaced
- **Status:** DIVERGENT
- **Original:** `CaveFisher.java:141-153` — 1-in-6 each: gold nugget / uranium nugget / titanium nugget, else nothing
- **Port:** `cave_fisher.json` — string 2–5 + spider eye 0–1
- **Fix:** rewrite JSON: three independent pools, each item @ ~16.7% chance, count 1; remove string/spider-eye.
- **Resolution:** FIXED (2026-06-11, Phase C — gold/uranium/titanium nugget gamble restored in cave_fisher.json (1-in-6 each, 0-2 +looting); see FIX_LOG.md and phase_c_reports/C1_entities_A_C.md)

## Cephadrome

### ENT-A-082 — Cephadrome: target list inverted
- **Status:** DIVERGENT
- **Original:** `Cephadrome.java:515-573,404-432` — attacks Monsters, Mothra, untamed Leon/GammaMetroid/WaterDragon, EnderDragon (70 direct part hits), Kraken ×1.5 dmg; players only if hit_by_player/badmood
- **Port:** `entity/Cephadrome.java:227-242` — Monsters ✓ but Mothra/Leon/GammaMetroid/WaterDragon explicitly EXCLUDED; no EnderDragon/Kraken special damage
- **Fix:** flip the exclusions into inclusions (untamed only); add EnderDragon part-hit handling and ×1.5 Kraken damage.
- **Resolution:** FIXED (2026-06-11, Phase C — targets now include Mothra, untamed Leon/GammaMetroid/WaterDragon, EnderDragon; EnderDragon takes 70 via explosion-typed hit, Kraken x1.5 (orig Cephadrome.java:404-432,515-573); see FIX_LOG.md and phase_c_reports/C1_entities_A_C.md)

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
- **Resolution:** FIXED (2026-06-11, Phase B — dropCustomDeathLoot deleted; cephadrome.json rewritten to the orig nugget + 1-5-roll gear table, see phase_b_reports/B3_riders.md Task 7)

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
- **Resolution:** FIXED (2026-06-11, Phase C — tame item apple (1-in-2), untame item dead bush (orig Chipmunk.java:141,172); see FIX_LOG.md and phase_c_reports/C1_entities_A_C.md)

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
- **Resolution:** FIXED (2026-06-11, Phase C — raw chicken/uranium/titanium nugget gamble restored in cliff_racer.json (1-in-8 each, 0-2 +looting); see FIX_LOG.md and phase_c_reports/C1_entities_A_C.md)

## CloudShark

### ENT-A-091 — CloudShark: HP raised, armor zeroed
- **Status:** DIVERGENT
- **Original:** `OreSpawnMain.java:6512` — HP 15, atk 6, def 5; size 1.0×0.75
- **Port:** `CloudShark.java:42-47` — HP 20, armor 0; size 1.5×1.0 (`ModEntities.java:52`)
- **Fix:** set HP 15, ARMOR 5; dimensions 1.0×0.75.
- **Resolution:** FIXED (2026-06-11, Phase C — size remainder closed in Phase C: 1.0x0.75 set in ModEntities; see FIX_LOG.md and phase_c_reports/C1_entities_A_C.md)

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
- **Resolution:** FIXED (2026-06-11, Phase C — paper/string/bone gamble restored in cloud_shark.json (1-in-3 each, 0-2 +looting); see FIX_LOG.md and phase_c_reports/C1_entities_A_C.md)

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
- **Resolution:** FIXED (2026-06-11, Phase C — finalizeSpawn randomizes BirdType nextInt(6) for fresh spawns (orig Cockateil.java:82-86); see FIX_LOG.md and phase_c_reports/C1_entities_A_C.md)

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
- **Resolution:** FIXED (2026-06-11, Phase C — ruby drop gated on BirdType 5 + player kill + 1-in-3 via NBT entity_properties in cockateil.json; see FIX_LOG.md and phase_c_reports/C1_entities_A_C.md)

## Coin

### ENT-A-098 — Coin: jackpot loot table replaced with gold
- **Status:** DIVERGENT
- **Original:** `Coin.java:98-129` — 1 roll-of-10: diamond / uranium nugget / titanium nugget / emerald / emerald axe-shovel-pickaxe-hoe / CoinEgg / emerald sword default
- **Port:** `coin.json` — gold ingot 1–3
- **Fix:** rewrite JSON as a single roll over the original 10-entry pool (incl. CoinEgg and emerald tools/sword).
- **Resolution:** PARTIAL (2026-06-11, Phase C — orig 10-slot weighted jackpot restored in coin.json; the CoinEgg slot stays empty until the item is ported (Phase D); see FIX_LOG.md and phase_c_reports/C1_entities_A_C.md)

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
- **Resolution:** FIXED (2026-06-11, Phase C — remainder closed in Phase C: scale-driven dims via getDefaultDimensions (2.5x3.5 x scale — orig Crab.java:133 tick() setSize overrides the 3.75 spawn-time width every tick, so 2.5 is the live value); see FIX_LOG.md and phase_c_reports/C1_entities_A_C.md)

### ENT-A-101 — Crab: spawn-time scale randomization deleted (giant crabs never occur)
- **Status:** DIVERGENT
- **Original:** `Crab.java:74-98` — scale 0.25 base, 1-in-4→0.5, 1-in-8→1.0; spawner crabs 0.35
- **Port:** `Crab.java:69-73` — DATA_SCALE constant 25 (0.25), never randomized
- **Fix:** randomize scale in `finalizeSpawn` with the original 0.25/0.5/1.0 distribution (0.35 for spawner spawns).
- **Resolution:** FIXED (2026-06-11, Phase C — finalizeSpawn restores the 0.25/0.5/1.0 scale dice and the fixed 0.35 spawner scale (orig Crab.java:74-98); see FIX_LOG.md and phase_c_reports/C1_entities_A_C.md)

### ENT-A-102 — Crab: water ecology inverted (water-seeker → water-avoider)
- **Status:** DIVERGENT
- **Original:** `Crab.java:306-339` — water-seek scan ≤12 → path 1.33; dry-out −1 HP @1-in-100, discard at 0
- **Port:** `WaterAvoidingRandomStrollGoal`; water-seek & dry-out missing
- **Fix:** replace WaterAvoidingStroll with plain stroll + ported water-seek scan and dry-out damage/discard.
- **Resolution:** FIXED (2026-06-11, Phase C — RandomStrollGoal (no water avoidance), water-seek scan, dry-out damage and self-discard restored (orig Crab.java:314-338); see FIX_LOG.md and phase_c_reports/C1_entities_A_C.md)

### ENT-A-103 — Crab: melee damage cut and attack sounds missing
- **Status:** DIVERGENT
- **Original:** `Crab.java` — melee 24×scale; attack sounds `scorpion_attack`/`scorpion_living`; water-heal splash sound
- **Port:** `Crab.java:104-119` — melee 10×scale; no attack sounds, no splash
- **Fix:** set base melee 24×scale; play `orespawn:scorpion_attack`/`scorpion_living` on swings and splash on water heal.
- **Resolution:** FIXED (2026-06-11, Phase C — remainder closed in Phase C: scorpion_attack/scorpion_living on melee swings, splash sound on water heal (orig Crab.java:358-364); see FIX_LOG.md and phase_c_reports/C1_entities_A_C.md)

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
- **Resolution:** FIXED (2026-06-11, Phase C — size remainder closed in Phase C: 0.75x0.5 set in ModEntities; see FIX_LOG.md and phase_c_reports/C1_entities_A_C.md)

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
- **Resolution:** FIXED (2026-06-11, Phase C — rotten flesh/bone/string gamble restored in creeping_horror.json (1-in-3 each, 0-2 +looting); see FIX_LOG.md and phase_c_reports/C1_entities_A_C.md)

### ENT-A-110 — CreepingHorror: deep-cave/night spawn identity removed
- **Status:** DIVERGENT
- **Original:** `CreepingHorror.java:220-228` — rules: dark, night, and (dim6 or y≤15)
- **Port:** `add_overworld_monsters.json` w3 1–1; no rules — spawns anywhere monsters can
- **Fix:** add `checkSpawnRules` enforcing darkness + night + y≤15 (or the dungeon dimension); keep modifier weight.
- **Resolution:** FIXED (2026-06-11, Phase C — checkSpawnRules enforces darkness + night + (Chaos dim or y<=15) (orig CreepingHorror.java:220-228); see FIX_LOG.md and phase_c_reports/C1_entities_A_C.md)

## Cryolophosaurus

### ENT-A-111 — Cryolophosaurus: stats doubled, armor zeroed
- **Status:** DIVERGENT
- **Original:** `OreSpawnMain.java:6482` — HP 10, atk 3, def 1
- **Port:** `Cryolophosaurus.java:51-57` — HP 20, atk 5, armor 0
- **Fix:** set HP 10, ATTACK 3, ARMOR 1.
- **Resolution:** FIXED (2026-06-11, Phase B — see FIX_LOG.md and phase_b_reports/B2_mobstats.md)

### ENT-A-112 — Cryolophosaurus: proactive hunting removed (retaliation-only)
- **Status:** DIVERGENT
- **Original:** `Cryolophosaurus.java:141-211` — proactive hunt 1-in-5 over 9×2×9; excludes Alosaurus/TRex/own kind/ghosts/CaveFisher/insects
- **Port:** `entity/Cryolophosaurus.java:34-44` — HurtBy only; comment falsely claims it never hunted (ENT-SYS-003)
- **Fix:** add proactive target scan (1-in-5 per tick, 9×2×9 box) with original exclusion list; fix the comment.
- **Resolution:** FIXED (2026-06-11, Phase C — proactive 1-in-5 hunt over 9x2x9 with the orig exclusion list and timid bite dice restored; misleading comment fixed (orig Cryolophosaurus.java:141-211); see FIX_LOG.md and phase_c_reports/C1_entities_A_C.md)

### ENT-A-113 — Cryolophosaurus: gamble drops replaced
- **Status:** DIVERGENT
- **Original:** `Cryolophosaurus.java:120-132` — 1-in-10: raw chicken / uranium nugget / titanium nugget, else nothing
- **Port:** `cryolophosaurus.json` — bone 2–5 + diamond 0–1@20%
- **Fix:** rewrite JSON: three pools each @10% ×1; remove bone/diamond.
- **Resolution:** FIXED (2026-06-11, Phase C — raw chicken/uranium/titanium nugget gamble restored in cryolophosaurus.json (1-in-10 each, 0-2 +looting); see FIX_LOG.md and phase_c_reports/C1_entities_A_C.md)

## CrystalCow

### ENT-A-114 — CrystalCow: RedCow lineage lost
- **Status:** DIVERGENT
- **Original:** `CrystalCow.java` — extends RedCow (inherits its stats/behavior)
- **Port:** `entity/CrystalCow.java:15-22` — extends vanilla `Cow` with `Cow.createAttributes()`
- **Fix:** re-parent to the port's RedCow (or replicate RedCow stats/behavior) so inherited drops/attributes match.
- **Resolution:** FIXED (2026-06-11, Phase C — CrystalCow re-parented to RedCow, inheriting the 1-in-200 forgiveness tick and never-despawn (orig CrystalCow.java:13-14); see FIX_LOG.md and phase_c_reports/C1_entities_A_C.md)

### ENT-A-115 — CrystalCow: pink-ingot drop invented, vanilla apple lost
- **Status:** DIVERGENT
- **Original:** `CrystalCow.java:19-26` — 0–2(+looting) crystal apples + 1 apple + RedCow drops
- **Port:** `crystal_cow.json` (crystal apple 1–3 + leather 1) + custom 1–2 crystal pink ingots (double, ENT-SYS-001)
- **Fix:** JSON: crystal apple 0–2 (+looting), apple ×1, plus RedCow base drops; delete the pink-ingot custom loot.
- **Resolution:** FIXED (2026-06-11, Phase B — see FIX_LOG.md and phase_b_reports/B1_drops.md)

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
- **Resolution:** FIXED (2026-06-11, Phase B — see FIX_LOG.md and phase_b_reports/B2_mobstats.md)

### ENT-D-002 — Dragon: tame item changed beef→bone
- **Status:** DIVERGENT
- **Original:** `Dragon.java` — raw beef tame
- **Port:** `entity/Dragon.java` `mobInteract` — `Items.BONE`, 1/5 chance, heal to full
- **Fix:** change tame item to `Items.BEEF` (verify original chance and replicate).
- **Resolution:** FIXED (2026-06-11, Phase C — see FIX_LOG.md and phase_c_reports/C2_entities_D_I.md)

### ENT-D-003 — Dragon: drops beef→bones/diamonds (plus double source)
- **Status:** DIVERGENT
- **Original:** `Dragon.java` `func_70628_a` — raw beef
- **Port:** hardcoded bones 1–6 + `entities/dragon.json` diamonds 1–6 (+looting)
- **Fix:** single loot source: beef (match original count); delete the bone hardcode and diamond pool.
- **Resolution:** VERIFIED-CORRECT (2026-06-11, Phase C — port `dragon.json` already drops beef 1–6 matching orig Dragon.java:342-347 and no hardcoded bone/diamond source remains, see phase_c_reports/C2_entities_D_I.md)

### ENT-D-004 — Dragon: Utopia-only spawn → overworld-wide
- **Status:** DIVERGENT
- **Original:** Utopia dimension boss list w1 1–2 (`BiomeGenUtopianPlains.java:164`); no overworld addSpawn
- **Port:** `add_overworld_creatures.json` w1 1–1 overworld-wide
- **Fix:** remove dragon from the overworld modifier; add to the Utopia dimension spawn list w1 1–2.
- **Resolution:** PARTIAL (2026-06-11, Phase C — overworld entry removed, Island-biome w1 1–2 added (orig list is `setIslandCreatures`, not the Utopia boss list); spawn-rule gates → ENT-SYS-002 (Phase D), see phase_c_reports/C2_entities_D_I.md)

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
- **Resolution:** FIXED (2026-06-11, Phase C — audit corrected: the orig trigger is a DIAMOND (Dragon.java:1351-1369), not a Magic Apple, and it spawns the Spyro class; see FIX_LOG.md and phase_c_reports/C2_entities_D_I.md)

## DungeonBeast

### ENT-D-007 — DungeonBeast: stats lowered
- **Status:** DIVERGENT
- **Original:** `OreSpawnMain.java:6501` — HP 65, atk 12, def 6
- **Port:** `entity/DungeonBeast.java:28-30,51-55` — HP 60, atk 10, armor 4
- **Fix:** set HP 65, ATTACK 12, ARMOR 6.
- **Resolution:** FIXED (2026-06-11, Phase B — see FIX_LOG.md and phase_b_reports/B2_mobstats.md)

### ENT-D-008 — DungeonBeast: crystal-dimension drops → bones/gold
- **Status:** DIVERGENT
- **Original:** `func_70628_a` — Crystal-dimension items
- **Port:** `dungeon_beast.json` — bones 3–8 (+looting) + 50% gold ingots 1–4
- **Fix:** open original `DungeonBeast.java` `func_70628_a`, list the crystal items, and rewrite the JSON to match.
- **Resolution:** FIXED (2026-06-11, Phase C — see FIX_LOG.md and phase_c_reports/C2_entities_D_I.md)

### ENT-D-009 — DungeonBeast: roofed forest → badlands relocation
- **Status:** DIVERGENT
- **Original:** `OreSpawnMain.java:4981` — addSpawn w20 2–4 ambient Roofed Forest; also spawners/Crystal dim
- **Port:** `hostile_dungeon_beast.json` — `#minecraft:is_badlands` w20 2–4
- **Fix:** change modifier biome to `minecraft:dark_forest` (roofed forest), keep w20 2–4; add Crystal-dimension spawn list entry.
- **Resolution:** PARTIAL (2026-06-11, Phase C — dark_forest w20 2–4 + Crystal w30 4–6 + Chaos w2 1–5 done; spawn-rule gates → ENT-SYS-002 (Phase D), see phase_c_reports/C2_entities_D_I.md)

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
- **Resolution:** PARTIAL (2026-06-11, Phase C — 7-biome w10/8/5 modifiers done; the orig registration is gated on Easter day (April 20, OreSpawnMain.java:4570-4571,4681) → ENT-SYS-002 (Phase D), see phase_c_reports/C2_entities_D_I.md)

## Elevator (Hoverboard)

### ENT-D-012 — Elevator/Hoverboard: hover hum remapped to beacon
- **Status:** DIVERGENT
- **Original:** `orespawn:hover` hum
- **Port:** `entity/HoverboardEntity.java` — `SoundEvents.BEACON_AMBIENT`
- **Fix:** register `orespawn:hover` and play it instead of the beacon ambient.
- **Resolution:** FIXED (2026-06-11, Phase C — see FIX_LOG.md and phase_c_reports/C2_entities_D_I.md)

## EmperorScorpion

### ENT-D-013 — EmperorScorpion: stats cut, armor dropped
- **Status:** DIVERGENT
- **Original:** `OreSpawnMain.java:6488` — HP 350, atk 35, def 20
- **Port:** `EntityEmperorScorpion.java:83-89` — HP 300, atk 20, no armor
- **Fix:** set HP 350, ATTACK 35, ARMOR 20.
- **Resolution:** FIXED (2026-06-11, Phase B — see FIX_LOG.md and phase_b_reports/B2_mobstats.md)

### ENT-D-014 — EmperorScorpion: minion-spawn cadence redesigned
- **Status:** DIVERGENT
- **Original:** rolled `nextInt(80)==1` per tick to spawn baby scorpions when population low
- **Port:** `EntityEmperorScorpion.java:52-60` — every 30+rand(10) ticks spawns `EntityScorpion` if <3 within 16 blocks, cap 6
- **Fix:** replace timer with per-tick `nextInt(80)==1` roll; replicate original population condition.
- **Resolution:** FIXED (2026-06-11, Phase C — audit corrected: orig has NO population condition; it is a `nextInt(4)==0 && nextInt(20)==1` roll while a target exists, spawning at the self/target midpoint (EmperorScorpion.java:408,437-438); see phase_c_reports/C2_entities_D_I.md)

### ENT-D-015 — EmperorScorpion: loot de-enchanted, beef→slimeballs
- **Status:** DIVERGENT
- **Original:** `EmperorScorpion.java:181-315` — scale, painting, obsidian, raw beef, **enchanted** diamond gear/UltimateSword set
- **Port:** `emperor_scorpion.json` — scale, name_tag, obsidian 4–8, slimeballs 4–11, plain diamond-gear rolls
- **Fix:** JSON: painting (not name_tag), beef (not slimeballs), apply `enchant_randomly`/fixed enchants on the gear pool incl. UltimateSword.
- **Resolution:** FIXED (2026-06-11, Phase B — see FIX_LOG.md and phase_b_reports/B1_drops.md)

## EnchantedCow

### ENT-D-016 — EnchantedCow: XP bottles + enchanted book invented
- **Status:** DIVERGENT
- **Original:** `EnchantedCow.java` `func_70628_a` — enchanted golden apples + golden apples + apples
- **Port:** `enchanted_apple_cow.json` (leather, golden_apple 1–2, enchanted_golden_apple 1) **plus** hardcoded `dropCustomDeathLoot`: 1–2 XP bottles always, 20% enchanted book (double, ENT-SYS-001)
- **Fix:** delete the `dropCustomDeathLoot` override; add plain apples to the JSON to match the original triple-apple table.
- **Resolution:** FIXED (2026-06-11, Phase B — see FIX_LOG.md and phase_b_reports/B1_drops.md)

## EnderKnight

### ENT-D-017 — EnderKnight: stats inflated
- **Status:** DIVERGENT
- **Original:** `OreSpawnMain.java:6507` — HP 60, atk 12, def 6
- **Port:** `entity/EnderKnight.java` — HP 80, atk 15, no armor
- **Fix:** set HP 60, ATTACK 12, ARMOR 6.
- **Resolution:** FIXED (2026-06-11, Phase B — see FIX_LOG.md and phase_b_reports/B2_mobstats.md)

### ENT-D-018 — EnderKnight: overworld habitat (incl. w20 roofed-forest hotspot) → End-only
- **Status:** DIVERGENT
- **Original:** `OreSpawnMain.java:4920-4928` — ambient w4 2–4 across 5 overworld biomes, w2 ×3, w20 Roofed Forest
- **Port:** `add_end_spawns.json` — `#minecraft:is_end` w8 1–2; no overworld spawns
- **Fix:** add overworld modifier with the original biome list (dark_forest w20 2–4 hotspot); remove or reduce the End entry.
- **Resolution:** PARTIAL (2026-06-11, Phase C — 9-biome overworld modifiers + Chaos-dim w2 1–2 added, invented End entry removed; spawn-rule gates → ENT-SYS-002 (Phase D), see phase_c_reports/C2_entities_D_I.md)

## EnderReaper

### ENT-D-019 — EnderReaper: stats inflated
- **Status:** DIVERGENT
- **Original:** `OreSpawnMain.java:6508` — HP 90, atk 18, def 8
- **Port:** `entity/EnderReaper.java` — HP 120, atk 20, no armor
- **Fix:** set HP 90, ATTACK 18, ARMOR 8.
- **Resolution:** FIXED (2026-06-11, Phase B — see FIX_LOG.md and phase_b_reports/B2_mobstats.md)

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
- **Resolution:** PARTIAL (2026-06-11, Phase C — 9-biome overworld modifiers + Island w25 2–4 + Chaos w1 1–1 added, invented End entry removed; spawn-rule gates → ENT-SYS-002 (Phase D), see phase_c_reports/C2_entities_D_I.md)

## EntityCage

### ENT-D-022 — EntityCage: species whitelist replaced by universal NBT capture
- **Status:** DIVERGENT
- **Original:** `EntityCage.java:160-201,174` — `nextInt(10)>=2` (80%): per-type checks dropping matched `CagedSpiderDriver/CagedCaveSpider/CagedSpider/CagedCrab/CagedBat(×2)/CagedPig/...`; fail/player → `CageEmpty`
- **Port:** `entity/EntityCage.java` — discards **any** Mob, drops `CagedMobItem` with full NBT; can now cage mobs the original could not
- **Fix:** decide policy: either restore the species whitelist (reject non-listed mobs → CageEmpty), or keep universal capture but gate it behind a config default-off; the drop-item divergence resolves with the same choice.
- **Resolution:** FIXED (2026-06-11, Phase C — full species whitelist restored with per-species escape dice and multi-count drops (Bat ×2, Silverfish ×2, Dragonfly ×2, Cockateil ×4, AttackSquid ×6); unlisted mobs eat the cage with no drop, players return CageEmpty, tamed GF/BF uncapturable; see phase_c_reports/C2_entities_D_I.md)

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
- **Resolution:** VERIFIED-CORRECT (2026-06-11, Phase B — audit value wrong: orig EntityRedAnt.java:53,34 is HP 2 / speed 0.2, port already matches, see phase_b_reports/B2_mobstats.md)

## EntityThrownRock

### ENT-D-025 — EntityThrownRock: type-5 damage halved
- **Status:** DIVERGENT
- **Original:** `EntityThrownRock.java:79-216` — t5 = 10 (t1=2, t2–4=5, t6=20, t7/8=40, t9–11=150, t12=250)
- **Port:** `entity/EntityThrownRock.java:72-79` — t5 folded into the 5-damage band
- **Fix:** restore `case 5 -> 10` in the damage switch.
- **Resolution:** FIXED (2026-06-11, Phase C — see FIX_LOG.md and phase_c_reports/C2_entities_D_I.md)

### ENT-D-026 — EntityThrownRock: five rock types have wrong effects
- **Status:** DIVERGENT
- **Original:** `:107-227` — t6 weakness 100; t9 fire 50t + weakness 100; t10 poison 200 + weakness 100; t11 slow 200 + weakness 100; t12 weakness 100 + explosion 5.1
- **Port:** `:94-122` — t6/9/11 wither 100; t9 lost ignite; t10 poison 200 only; t12 wither 100
- **Fix:** replace WITHER with WEAKNESS (100t) on t6/9/10/11/12; re-add t9 50t ignite, t10/t11 weakness secondary, t11 slow 200.
- **Resolution:** FIXED (2026-06-11, Phase C — see FIX_LOG.md and phase_c_reports/C2_entities_D_I.md)

### ENT-D-027 — EntityThrownRock: typed rock recovery and glass-breaking lost
- **Status:** DIVERGENT
- **Original:** `:229-285` — breaks glass on impact; returns the specific rock item of its type (MySmallRock…MyCrystalTNTRock, 12 types)
- **Port:** `:129` — always pops generic `ModItems.ROCK`; no glass-breaking
- **Fix:** map rock type → corresponding ModItems rock item on drop; add glass-block break on impact (mobGriefing-gated).
- **Resolution:** FIXED (2026-06-11, Phase C — 12-type rock recovery + 3×3×3 glass/glass-pane break with `orespawn:glassdead` sound; orig has no mobGriefing gate so none added; see phase_c_reports/C2_entities_D_I.md)

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
- **Resolution:** FIXED (2026-06-11, Phase C — crystal_torch 0–2 (+looting), the orig uses the vanilla `func_70628_a` count, not ×1; see phase_c_reports/C2_entities_D_I.md)

### ENT-D-030 — Fairy: roofed-forest hotspot diluted overworld-wide
- **Status:** DIVERGENT
- **Original:** `OreSpawnMain.java:4974` — w25 2–4 ambient Roofed Forest only; + Crystal dim w10 4–8, w5 2–4
- **Port:** `add_overworld_ambient.json` w5 1–3 ALL overworld (Crystal/Chaos dims kept)
- **Fix:** restrict the overworld modifier to `minecraft:dark_forest` w25 2–4.
- **Resolution:** PARTIAL (2026-06-11, Phase C — overworld-wide entry replaced with dark_forest w25 2–4 (Crystal/Chaos dims already correct); spawn-rule gates → ENT-SYS-002 (Phase D), see phase_c_reports/C2_entities_D_I.md)

## Firefly

### ENT-D-031 — Firefly: ExtremeTorch drop → glowstone
- **Status:** DIVERGENT
- **Original:** `Firefly.java` — drops ExtremeTorch
- **Port:** `firefly.json` — glowstone_dust 0–1
- **Fix:** change JSON to ExtremeTorch ×1.
- **Resolution:** FIXED (2026-06-11, Phase C — extreme_torch 0–2 (+looting), the orig uses the vanilla `func_70628_a` count, not ×1; see phase_c_reports/C2_entities_D_I.md)

## Flounder

### ENT-D-032 — Flounder: Utopia waters → vanilla oceans
- **Status:** DIVERGENT
- **Original:** Utopia water lists w2 2–4 / w5 6–8 (`BiomeGenUtopianPlains.java:126,253`)
- **Port:** `add_ocean_spawns.json` w8 1–3 `#minecraft:is_ocean`
- **Fix:** add Flounder to the Utopia-dimension water spawn lists at original weights (keep or drop ocean entry per design decision).
- **Resolution:** PARTIAL (2026-06-11, Phase C — Utopia w2 2–4 + Crystal w5 6–8 added, invented ocean entry removed; spawn-rule gates → ENT-SYS-002 (Phase D), see phase_c_reports/C2_entities_D_I.md)

## Frog

### ENT-D-033 — Frog: river/swamp focus → all overworld
- **Status:** DIVERGENT
- **Original:** `OreSpawnMain.java:4963-4967` — waterCreature w20 3–6 river, w20 2–6 swamp, ambient w2–3
- **Port:** `add_overworld_creatures.json` w10 1–2 (rules Y≥50/day/≤5 frogs kept)
- **Fix:** retarget modifier to river+swamp biomes w20 3–6 / 2–6.
- **Resolution:** PARTIAL (2026-06-11, Phase C — river w20+w3, swamp w20+w2, jungle w3 modifiers + Utopia w5 4–6 + Crystal w1 3–5 added, overworld-wide entry removed; spawn-rule gates → ENT-SYS-002 (Phase D), see phase_c_reports/C2_entities_D_I.md)

## GammaMetroid

### ENT-D-034 — GammaMetroid: stats cut ~40%
- **Status:** DIVERGENT
- **Original:** `OreSpawnMain.java:6486` — HP 100, atk 10, def 12
- **Port:** `entity/EntityGammaMetroid.java` — HP 60, atk 8, no armor
- **Fix:** set HP 100, ATTACK 10, ARMOR 12.
- **Resolution:** FIXED (2026-06-11, Phase B — see FIX_LOG.md and phase_b_reports/B2_mobstats.md)

### ENT-D-035 — GammaMetroid: gold nuggets → gunpowder
- **Status:** DIVERGENT
- **Original:** `GammaMetroid.java:227-231` — gold nuggets + iron ingots
- **Port:** `gamma_metroid.json` — gunpowder 5–14 + iron 6–15
- **Fix:** change gunpowder pool to gold_nugget (match original counts from `GammaMetroid.java:227-231`).
- **Resolution:** FIXED (2026-06-11, Phase C — gold_nugget 5–14 + iron_ingot 6–15 per orig GammaMetroid.java:223-233; see phase_c_reports/C2_entities_D_I.md)

### ENT-D-036 — GammaMetroid: Crystal-dim swarms → Nether/mountain singles
- **Status:** DIVERGENT
- **Original:** Crystal dimension list w35 4–7 (`ChunkProviderOreSpawn2.java:386`) + Utopia boss w1 (`BiomeGenUtopianPlains.java:514`)
- **Port:** `add_nether_spawns.json` w3 1–1 + `companion_gamma_metroid.json` mountains w1 1–1
- **Fix:** add to Crystal-dimension spawn list w35 4–7 and Utopia w1; remove the Nether entry.
- **Resolution:** PARTIAL (2026-06-11, Phase C — audit corrected: ChunkProviderOreSpawn2:386 is the MINING dim (w35 4–7) and the w1 1–1 list is `setChaosCreatures` (:513-514); Mining + Chaos entries added, Nether entry and invented mountain companion removed; spawn-rule gates → ENT-SYS-002 (Phase D), see phase_c_reports/C2_entities_D_I.md)

## Gazelle

### ENT-D-037 — Gazelle: poppy + super drops → mutton
- **Status:** DIVERGENT
- **Original:** `Gazelle.java:347` — poppy + super drops
- **Port:** `gazelle.json` — mutton 1–3 (+looting)
- **Fix:** change JSON to poppy ×1 and port the "super drops" bonus from `Gazelle.java`; remove mutton.
- **Resolution:** FIXED (2026-06-11, Phase C — audit corrected: poppies 2–6 only when TAMED (Gazelle.java:341-352); untamed = vanilla super = beef 0–2 (+looting) via `func_146068_u` (:337-339). Port writes an `OreSpawnTamed` NBT flag and the JSON branches on it; see phase_c_reports/C2_entities_D_I.md)

## Ghost

### ENT-D-038 — Ghost: bone drops invented
- **Status:** DIVERGENT
- **Original:** `Ghost.java` — no notable drops
- **Port:** `ghost.json` — bone 0–2 (+looting)
- **Fix:** empty the loot pools.
- **Resolution:** FIXED (2026-06-11, Phase C — pools emptied; orig Ghost extends EntityAmbientCreature with no drop override; see phase_c_reports/C2_entities_D_I.md)

### ENT-D-039 — Ghost: spawn density slashed (w15 ambient → w4 caves)
- **Status:** DIVERGENT
- **Original:** ~28 addSpawn ambient w2–15 grp up to 5–10 (`OreSpawnMain.java:4544+,4784-4788`)
- **Port:** `add_cave_spawns.json` w4 1–1 + `dim_chaos_locals.json` w15 3–6; dark-only rule
- **Fix:** raise overworld weight/groups toward the original w2–15 / 5–10 ambient distribution across its biome list.
- **Resolution:** PARTIAL (2026-06-11, Phase C — the UNGATED block (OreSpawnMain.java:4783-4788: snowy_taiga w15 5–10, taiga w10 5–10, frozen_river w6 4–6, jungle w2 1–4, dark_forest w15 2–5) is now in JSON; invented cave/Chaos entries removed. The 22-biome w15 3–6 block (:4544-4565) is Halloween-only (Oct 31 gate :4518-4521) → ENT-SYS-002 (Phase D) along with the dark-spawn rules; see phase_c_reports/C2_entities_D_I.md)

## GhostSkelly

### ENT-D-040 — GhostSkelly: bone/arrow drops invented
- **Status:** DIVERGENT
- **Original:** no notable drops
- **Port:** `ghost_skelly.json` — bone 1–3 (+looting) + arrows 0–2
- **Fix:** empty the loot pools.
- **Resolution:** FIXED (2026-06-11, Phase C — pools emptied; orig GhostSkelly extends EntityAmbientCreature with no drop override; see phase_c_reports/C2_entities_D_I.md)

### ENT-D-041 — GhostSkelly: spawn density slashed
- **Status:** DIVERGENT
- **Original:** ~28 addSpawn ambient w2–15 (`OreSpawnMain.java:4522-4543,4791-4795`)
- **Port:** `add_cave_spawns.json` w4 1–1 + `dim_chaos_locals.json` w10 2–4
- **Fix:** restore original ambient weights/groups across the original biome list.
- **Resolution:** PARTIAL (2026-06-11, Phase C — ungated block (OreSpawnMain.java:4790-4795, same five biomes/weights as Ghost) in JSON; invented cave/Chaos entries removed; Halloween 22-biome block (:4522-4543) → ENT-SYS-002 (Phase D), see phase_c_reports/C2_entities_D_I.md)

## GiantRobot

### ENT-D-042 — GiantRobot: stats ×4 and entity split into GiantRobot + Jeffery
- **Status:** DIVERGENT
- **Original:** ONE entity using `Jeffery_stats` — HP 550, atk 40, def 18 (`OreSpawnMain.java:6476`; `GiantRobot.java:63-65,97,105`); XP = health/2 = 275
- **Port:** `entity/GiantRobot.java:56-62` — HP 2000, atk 100, armor 12, XP 500; plus separate `entity/Jeffery.java:22-28` — HP 1000/50/6 XP 250
- **Fix:** set GiantRobot to HP 550, ATTACK 40, ARMOR 18, XP 275; either delete the Jeffery duplicate or make it a named-skin alias with identical stats.
- **Resolution:** FIXED (2026-06-11, Phase B — see FIX_LOG.md and phase_b_reports/B2_mobstats.md)

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
- **Resolution:** FIXED (2026-06-11, Phase C — audit corrected: case 9 (`field_150319_E`) is the DETECTOR RAIL, not a piston head, and the `nextInt(12)` pool has 2/12 empty outcomes (preserved with an empty weight-2 entry); see phase_c_reports/C2_entities_D_I.md)

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
- **Resolution:** PARTIAL (2026-06-11, Phase C — all 12 orig entries replicated across 7 per-biome modifier files (beach w30 8–15 hotspot); spawn-rule gates → ENT-SYS-002 (Phase D), see phase_c_reports/C2_entities_D_I.md)

## GoldCow

### ENT-D-051 — GoldCow: hardcoded gold-ingot bonus invented
- **Status:** DIVERGENT
- **Original:** `GoldCow.java` — golden apples only
- **Port:** `gold_cow.json` (leather 1–3 + golden_apple 1) + hardcoded `dropCustomDeathLoot` 1–3 GOLD_INGOT (double, ENT-SYS-001)
- **Fix:** delete the `dropCustomDeathLoot` override; align JSON golden-apple count with `GoldCow.java`. (Note: port splits original into `gold_cow` + `golden_apple_cow` ids.)
- **Resolution:** FIXED (2026-06-11, Phase B — see FIX_LOG.md and phase_b_reports/B1_drops.md)

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
- **Resolution:** PARTIAL (2026-06-11, Phase C — Utopia w1 1–1 + Island w5 2–4 added (Chaos w10 2–4 already correct), invented ocean entry removed; spawn-rule gates → ENT-SYS-002 (Phase D), see phase_c_reports/C2_entities_D_I.md)

## Hammerhead

### ENT-D-054 — Hammerhead: attack cut 75→20, armor dropped
- **Status:** DIVERGENT
- **Original:** `OreSpawnMain.java:6477`; `Hammerhead.java:59-83` — HP 240, atk 75, def 20
- **Port:** `entity/Hammerhead.java:38-40` — HP 200, atk 20, no armor
- **Fix:** set HP 240, ATTACK 75, ARMOR 20.
- **Resolution:** FIXED (2026-06-11, Phase B — see FIX_LOG.md and phase_b_reports/B2_mobstats.md)

### ENT-D-055 — Hammerhead: boss bar invented
- **Status:** DIVERGENT
- **Original:** no boss bar
- **Port:** `entity/Hammerhead.java:42` — `ServerBossEvent` added
- **Fix:** remove the boss bar (or keep behind config; Hammerhead is not a boss in the original).
- **Resolution:** FIXED (2026-06-11, Phase C — `ServerBossEvent` and its player/progress hooks removed; see phase_c_reports/C2_entities_D_I.md)

### ENT-D-056 — Hammerhead: four unique reward items lost
- **Status:** DIVERGENT
- **Original:** `Hammerhead.java:126-147` — XP bottle, ExperienceCatcher, CreeperLauncher, CreeperRepellent, raw beef, ExperienceTreeSeed, MyHammy
- **Port:** `hammerhead.json` (prismarine 5–8 + experience_catcher 5–10) + hardcoded 8 XP bottles + 6 bones (double, ENT-SYS-001)
- **Fix:** single JSON: XP bottles, experience_catcher, creeper_launcher, creeper_repellent, beef, experience_tree_seed, hammy (counts from `Hammerhead.java:126-147`); delete hardcoded loot and prismarine/bones.
- **Resolution:** FIXED (2026-06-11, Phase B — four unique reward items restored; MyHammy is a MISSING-ITEM (tracked as MOD-008), see FIX_LOG.md and phase_b_reports/B1_drops.md)

### ENT-D-057 — Hammerhead: Utopia → oceans relocation
- **Status:** DIVERGENT
- **Original:** Utopia monster list w1 1–1 (`BiomeGenUtopianPlains.java:463`)
- **Port:** `add_ocean_spawns.json` w3 1–1 (Y≥50 + no-buddy rules kept)
- **Fix:** add to Utopia dimension monster list w1 1–1; review the ocean entry.
- **Resolution:** PARTIAL (2026-06-11, Phase C — the orig w1 1–1 list is `setChaosCreatures` (BiomeGenUtopianPlains.java:462-464); Chaos-dim entry added, invented ocean entry removed; spawn-rule gates → ENT-SYS-002 (Phase D), see phase_c_reports/C2_entities_D_I.md)

## HerculesBeetle

### ENT-D-058 — HerculesBeetle: stats cut 20–50%
- **Status:** DIVERGENT
- **Original:** `OreSpawnMain.java:6468` — HP 250, atk 30, def 19
- **Port:** `EntityHerculesBeetle.java:53-58` — HP 200, atk 15, no armor
- **Fix:** set HP 250, ATTACK 30, ARMOR 19.
- **Resolution:** FIXED (2026-06-11, Phase B — see FIX_LOG.md and phase_b_reports/B2_mobstats.md)

### ENT-D-059 — HerculesBeetle: BigHammer + enchanted gear loot gutted
- **Status:** DIVERGENT
- **Original:** `HerculesBeetle.java:141-288` — MyBigHammer + painting + raw beef + enchanted diamond gear set
- **Port:** `hercules_beetle.json` (bones 3–6 + gunpowder 2–5) + hardcoded name_tag + 4–11 bones (double, ENT-SYS-001)
- **Fix:** single JSON: big_hammer ×1, painting ×1, beef, enchanted diamond gear pool (enchant_randomly); delete hardcoded loot.
- **Resolution:** FIXED (2026-06-11, Phase B — see FIX_LOG.md and phase_b_reports/B1_drops.md)

## Hydrolisc

### ENT-D-060 — Hydrolisc: HP buffed 66%, speed raised
- **Status:** DIVERGENT
- **Original:** `Hydrolisc.java:74` + mygetMaxHealth — HP ~60, atk 1.0, speed 0.2
- **Port:** `EntityHydrolisc.java:75-80` — HP 100, atk 1.0, speed 0.25
- **Fix:** set HP 60, MOVEMENT_SPEED 0.2.
- **Resolution:** VERIFIED-CORRECT (2026-06-11, Phase B — audit value wrong: orig Hydrolisc.java:210,39 is HP 100 / speed 0.25, port already matches; missing ARMOR 10 sub-gap found and fixed, see phase_b_reports/B2_mobstats.md)

### ENT-D-061 — Hydrolisc: swamp/jungle density → sparse coastal
- **Status:** DIVERGENT
- **Original:** `OreSpawnMain.java:4829-4832` — creature w25 3–6 swamp, w15 2–5 jungle, w10 1–3 jungleHills, w5 3–6
- **Port:** ocean w3 1–1 + beach w3 1–2 + river w3 1–2
- **Fix:** retarget modifiers to swamp w25 3–6 and jungle w15 2–5 (+jungle hills w10 1–3); drop ocean/beach.
- **Resolution:** FIXED (2026-06-11, Phase C — swamp w25 3–6, jungle w15 2–5, sparse_jungle w10 1–3, stony_shore w5 3–6 per-biome files; ocean/beach/river entries removed; orig Hydrolisc has NO getCanSpawnHere override so no gate remainder; see phase_c_reports/C2_entities_D_I.md)

## Irukandji

### ENT-D-062 — Irukandji: melee attack 10× original, HP 5×
- **Status:** DIVERGENT
- **Original:** `OreSpawnMain.java:6509`; `Irukandji.java:57-85` — HP 1, atk 20, def 0
- **Port:** `entity/Irukandji.java:35-37` — HP 5, atk 200.0 (note: 200 belongs only to the empty-hand retaliation, which is correctly ported separately)
- **Fix:** set HP 1, ATTACK_DAMAGE 20; keep the separate 200.0 empty-hand retaliation constant.
- **Resolution:** FIXED (2026-06-11, Phase B — see FIX_LOG.md and phase_b_reports/B2_mobstats.md)

### ENT-D-063 — Irukandji: Utopia waters → oceans
- **Status:** DIVERGENT
- **Original:** Utopia water list w4 2–3 (`BiomeGenUtopianPlains.java:256`)
- **Port:** `add_ocean_spawns.json` w4 1–2 (Y≥50, 1/60 roll, ≤2 nearby rules kept)
- **Fix:** add to Utopia dimension water list w4 2–3; review ocean entry.
- **Resolution:** PARTIAL (2026-06-11, Phase C — the orig w4 2–3 list is the Crystal sub-biome water list (BiomeGenUtopianPlains.java:255-257); Crystal-dim entry added, invented ocean entry removed; spawn-rule gates → ENT-SYS-002 (Phase D), see phase_c_reports/C2_entities_D_I.md)

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


---

# Findings — Entities K–Z & Bosses

Consolidated from `audit_sections\03_entities_K_R.md`, `04_entities_S_Z.md`, `05_bosses.md`.
Paths: ORIG = `reference_1_7_10_source\sources\danger\orespawn\`, PORT = `src\main\java\danger\orespawn\`, LT = `src\main\resources\data\orespawn\loot_table\entities\`, BM = `src\main\resources\data\orespawn\neoforge\biome_modifier\`.

---

## Systemic

### ENT-SYS2-001 — Systemic: double drops (code path + loot table both fire)
- **Status:** DIVERGENT
- **Original:** per-entity `func_70628_a`/`func_146068_u` — single drop path per entity
- **Port:** entities have BOTH a `dropCustomDeathLoot` override AND a loot-table JSON; both execute on death. Affected: Kraken, Kyuubi, Leon, Mantis, Molenoid, Mothra, Nastysaurus, Pointysaurus, RedCow, RubyBird, TRex, Triffid, TrooperBug, SeaMonster, SeaViper, WaterDragon, WormMedium, WormLarge, TheKing, TheQueen, Godzilla
- **Fix:** for each listed entity pick ONE source of truth (prefer the loot-table JSON); delete the redundant `dropCustomDeathLoot` override or empty the duplicate JSON pools. High priority — affects game economy on every kill.
- **Resolution:** FIXED (2026-06-11, Phase B — drops consolidated into loot-table JSONs; TheKing/BandP remain documented code exceptions, see FIX_LOG.md and phase_b_reports/B1_drops.md)

### ENT-SYS2-002 — Systemic: port `MobStats.java` is dead code contradicting hardcoded attributes
- **Status:** DIVERGENT
- **Original:** `OreSpawnMain.java` `get_mobstats` (lines ~6460–6525) — config-driven stats applied to every entity
- **Port:** `MobStats.java` constants (e.g. KRAKEN 500/30 `MobStats.java:59`, KYUUBI 120/16 `:29`, LURKING_TERROR 150/20 `:47`, RAT 15/3, ROTATOR 80/10, SCORPION 30/5 `:63`) referenced by NO entity in slices K–Z; all `createAttributes()` values hardcoded and differing
- **Fix:** either wire every `createAttributes()` to MobStats constants (and correct constants to the 1.7.10 values) or delete `MobStats.java`. Medium priority; do before fixing per-entity stats so fixes land in one place.
- **Resolution:** FIXED (2026-06-11, Phase B — see FIX_LOG.md and phase_b_reports/B2_mobstats.md)

### ENT-SYS2-003 — Systemic: custom-dimension spawn lists not ported → zero natural spawns
- **Status:** MISSING
- **Original:** Utopia/Island/Crystal/Village dim spawn lists (`BiomeGenUtopianPlains.java`) + spawner gates: Robot1 w25/4-8 + w5/2-8 ("Bomb-Omb"), Robot2 w16/2-8 + w2/1-4 ("Robo-Pounder"), Robot3 w12/2-4 + w2/1-4, Robot4 w8/1-2 + w1/1-2, Robot5 w20/4-8 + w2/3-5 ("Robo-Sniper"), PitchBlack (Utopia DimensionID6 + "Nightmare" spawner), SpiderDriver (Village w20/3-5, `BiomeGenUtopianPlains.java:292`)
- **Port:** absent — no biome-modifier entry for any of these seven entities; spawn egg/summon only
- **Fix:** create BM JSONs mapping each entity to the port's analog dimensions/biomes (or themed vanilla biomes) with the original weights/group sizes. High priority — these mobs are unencounterable.

### ENT-SYS2-004 — Systemic: original spawn gates (spawner blocks, darkness, Y-bands, crowd caps) absent
- **Status:** MISSING
- **Original:** per-entity `func_70601_bi` checks: spawner-block proximity, darkness, Y ranges, nearby-buddy caps, dimension checks
- **Port:** most biome-modifier spawns have no `checkSpawnRules` override. Affected (this register's scope): Kraken, LeafMonster, LurkingTerror, Mantis, Molenoid, Nastysaurus, Peacock (`findBuddies()` exists but never called, `Peacock.java:111-114`), Rat, Rotator, Tshirt, Scorpion
- **Fix:** add `checkSpawnRules` overrides per entity replicating darkness/Y/crowd gates; for spawner-driven mobs, gate natural spawning behind config or remove BM entry. Medium priority.

---

## Kraken

### ENT-K-001 — Kraken: stats tripled
- **Status:** DIVERGENT
- **Original:** `OreSpawnMain.java:6515`, `Kraken.java` ctor — HP 1000, ATK 40, def 10, speed 0.37, xp 500
- **Port:** `entity\Kraken.java:66,76-84` — HP 3000, ATK 80, armor 8, speed 0.5, kb-res 1.0, xp 500
- **Fix:** set `createAttributes()` to MAX_HEALTH 1000, ATTACK_DAMAGE 40, ARMOR 10, MOVEMENT_SPEED 0.37.
- **Resolution:** FIXED (2026-06-11, Phase B — see FIX_LOG.md and phase_b_reports/B2_mobstats.md)

### ENT-K-002 — Kraken: hurt-invulnerability window 4× shorter
- **Status:** PARTIAL
- **Original:** ORIG `Kraken.java` — `field_70174_ab = 120` after hurt
- **Port:** `entity\Kraken.java:409` — `hurtTimer = 30`; Kraken can be re-damaged 4× faster
- **Fix:** set `hurtTimer = 120` at `Kraken.java:409`.

### ENT-K-003 — Kraken: drop substitutions + extra loot-table layer
- **Status:** DIVERGENT
- **Original:** ORIG `Kraken.java` death drops — KrakenTooth, painting (`field_151160_bD`), 120–279 ink sac/dye (`field_151100_aR`), 5–14 rolls of 53-case enchanted-gear table
- **Port:** `entity\Kraken.java:529-602` — golden apple instead of painting, 120–279 cooked cod instead of ink sac; PLUS `LT kraken.json` adds 2nd KrakenTooth, xp bottle, 120–279 prismarine shard, 5–15 diamond, 5–15 gold ingot, 10% ultimate_sword
- **Fix:** restore ink sacs (120–279) and a painting in the code path; delete the entire extra `kraken.json` layer (see ENT-SYS2-001).
- **Resolution:** FIXED (2026-06-11, Phase B — see FIX_LOG.md and phase_b_reports/B1_drops.md)

### ENT-K-004 — Kraken: natural ocean spawning added (was spawner/summon-only)
- **Status:** DIVERGENT
- **Original:** ORIG `Kraken.java` `func_70601_bi` — no `addSpawn`; required open-sky column + Y>50, spawner/summon only
- **Port:** `BM add_ocean_spawns.json` — weight 1/1-1 natural ocean spawn, no `checkSpawnRules` override; a 3000-HP boss spawns naturally
- **Fix:** remove Kraken from `add_ocean_spawns.json`, or add a `checkSpawnRules` override requiring open sky + Y>50 + config gate.
- **Resolution:** FIXED (2026-06-11, Phase C — see FIX_LOG.md and phase_c_reports/C3_entities_K_R.md)

### ENT-K-005 — Kraken: custom sounds replaced with vanilla elder guardian
- **Status:** DIVERGENT
- **Original:** ORIG `Kraken.java` — `orespawn:kraken_living` (1/5), `orespawn:alo_death`
- **Port:** `entity\Kraken.java:431-446` — vanilla `ELDER_GUARDIAN_AMBIENT` (1/5) / `ELDER_GUARDIAN_DEATH`
- **Fix:** register/use `ModSounds.KRAKEN_LIVING` and `ALO_DEATH` in `getAmbientSound`/`getDeathSound`.
- **Resolution:** FIXED (2026-06-11, Phase C — see FIX_LOG.md and phase_c_reports/C3_entities_K_R.md)

---

## Kyuubi

### ENT-K-006 — Kyuubi: HP gutted 125→30
- **Status:** DIVERGENT
- **Original:** `OreSpawnMain.java:6485` — HP 125, ATK 10 (melee 3 via `getAttackStrength`), def 10, speed 0.25, xp 30
- **Port:** `entity\EntityKyuubi.java:50-55` — HP 30, ATK 3, speed 0.25, xp 30
- **Fix:** set MAX_HEALTH 125, ATTACK_DAMAGE 10 (keep effective melee 3 if replicating `getAttackStrength`), ARMOR 10.
- **Resolution:** FIXED (2026-06-11, Phase B — see FIX_LOG.md and phase_b_reports/B2_mobstats.md)

### ENT-K-007 — Kyuubi: fire immunity lost — self-damaging
- **Status:** MISSING
- **Original:** ORIG `Kyuubi.java` — `field_70178_ae = true` (fire-immune), invuln window 1000
- **Port:** `entity\EntityKyuubi.java` — no `fireImmune()` override, yet sets itself on fire every ~10 ticks (`EntityKyuubi.java:78`); wild Kyuubi burns itself to death and hurts itself in water
- **Fix:** override `fireImmune()` to return true (or set `EntityType.Builder.fireImmune()` at registration). High priority — entity is self-destructing.

### ENT-K-008 — Kyuubi: drops swapped + doubled
- **Status:** DIVERGENT
- **Original:** ORIG `Kyuubi.java` — 10 gold nugget, 3 redstone block, 4 quartz block, uranium/titanium nuggets
- **Port:** `entity\EntityKyuubi.java:142-153` — 10 gold ingot + 3 TNT + 4 redstone block; PLUS `LT kyuubi.json` 2–5 ruby + 3–8 blaze powder
- **Fix:** restore gold nugget ×10, redstone block ×3, quartz block ×4, uranium/titanium nuggets in one path; remove the other (ENT-SYS2-001).
- **Resolution:** FIXED (2026-06-11, Phase B — see FIX_LOG.md and phase_b_reports/B1_drops.md)

### ENT-K-009 — Kyuubi: Nether spawn weight halved
- **Status:** DIVERGENT
- **Original:** `OreSpawnMain.java:4802` — Nether weight 10/1-1
- **Port:** `BM add_nether_spawns.json` — Nether weight 5/1-1
- **Fix:** set weight back to 10 in `add_nether_spawns.json`.
- **Resolution:** FIXED (2026-06-11, Phase C — see FIX_LOG.md and phase_c_reports/C3_entities_K_R.md)

---

## LaserBall (projectile)

### ENT-K-010 — LaserBall: irukandji miss-drop and special-type effects missing
- **Status:** PARTIAL
- **Original:** ORIG `LaserBall.java` — irukandji-type ball drops `MyIrukandji` item when hitting nothing; special-type had extra effects beyond explosion
- **Port:** `entity\LaserBall.java` — no item drop on miss; special-type extra effects not reproduced
- **Fix:** in `onHitBlock`/miss-discard path, spawn the irukandji item entity for irukandji-type balls; port the original special-type extra effects.

---

## Lavafoam (block)

### ENT-K-011 — Lavafoam: Nether bonus drops missing
- **Status:** MISSING
- **Original:** ORIG `Lavafoam.java` — drops 5–14 items when broken in the Nether (dim −1)
- **Port:** `block\Lavafoam.java` — no loot override / dimension check
- **Fix:** add a loot table (or `spawnAfterBreak` override) granting 5–14 bonus items when `level.dimension() == Level.NETHER`.

---

## LeafMonster

### ENT-K-012 — LeafMonster: stats buffed ~3×
- **Status:** DIVERGENT
- **Original:** `OreSpawnMain.java:6516` — HP 6, ATK 2 (melee dealt 6), def 1, speed 0.25, xp 5
- **Port:** `entity\EntityLeafMonster.java:44-49` — HP 20, ATK 5, speed 0.25, xp 5
- **Fix:** set MAX_HEALTH 6, ATTACK_DAMAGE 2 (melee 6 if replicating original melee constant), ARMOR 1.
- **Resolution:** FIXED (2026-06-11, Phase B — see FIX_LOG.md and phase_b_reports/B2_mobstats.md)

### ENT-K-013 — LeafMonster: prey list broadened
- **Status:** DIVERGENT
- **Original:** ORIG `LeafMonster.java` — targets ants, butterflies, luna moths, non-creative players
- **Port:** `entity\EntityLeafMonster.java:139-146` — targets players + anything with BbWidth<1.0
- **Fix:** replace width heuristic with an explicit class allow-list (EntityAnt, Butterfly, LunaMoth equivalents, Player).
- **Resolution:** FIXED (2026-06-11, Phase C — see FIX_LOG.md and phase_c_reports/C3_entities_K_R.md)

### ENT-K-014 — LeafMonster: drops changed
- **Status:** DIVERGENT
- **Original:** ORIG `LeafMonster.java` — random leaves/log/stick
- **Port:** `LT leaf_monster.json` — 1–3 oak log + 0–2 bone
- **Fix:** rewrite `leaf_monster.json` to random leaves OR log OR stick (one of, weighted), no bone.
- **Resolution:** FIXED (2026-06-11, Phase C — audit's claimed list wrong: verified orig LeafMonster.java:144-153 one-of is log/leaves/ROTTEN FLESH (no stick); see phase_c_reports/C3_entities_K_R.md)

### ENT-K-015 — LeafMonster: spawn gating lost, weight changed
- **Status:** PARTIAL
- **Original:** ORIG `LeafMonster.java` — spawner-gated + darkness + ≤4 buddies + dimension checks
- **Port:** `BM add_overworld_monsters.json` — overworld monsters w4/1-2, no `checkSpawnRules`
- **Fix:** add `checkSpawnRules` (darkness + ≤4 nearby LeafMonsters); see ENT-SYS2-004.

---

## Leon (Leonopteryx)

### ENT-K-016 — Leon: armor missing on EntityLeon; Leonopteryx stats invented
- **Status:** PARTIAL
- **Original:** ORIG `Leon.java` — hardcoded HP 250 / ATK 55 / armor 16
- **Port:** `entity\EntityLeon.java:91-98` — HP 250, ATK 55, speed 0.25, kb-res 0.8, **no armor**; `entity\Leonopteryx.java:71-105` — HP 300/ATK 40/speed 0.4, xp 120 (invented)
- **Fix:** add ARMOR 16 to `EntityLeon.createAttributes()`; align Leonopteryx to 250/55/16 or document it as an intentional separate boss.
- **Resolution:** FIXED (2026-06-11, Phase B — see FIX_LOG.md and phase_b_reports/B2_mobstats.md)

### ENT-K-017 — Leon: rider-controlled flight missing
- **Status:** MISSING
- **Original:** ORIG `Leon.java` `fly_with_rider` — full rider-controlled flight, speed up to 3.5, vertical control via `flyup_keystate`
- **Port:** `entity\EntityLeon.java:183-205` — `tickRidden`/`getRiddenInput` give ground movement only at 1.8× walk speed; Leonopteryx not rideable at all
- **Fix:** implement flying mount in `tickRidden`: set `setNoGravity(true)` while ridden, map jump key to vertical ascent, speed cap 3.5. High priority — signature feature.
- **Resolution:** FIXED (2026-06-11, Phase B — see FIX_LOG.md and phase_b_reports/B3_riders.md)

### ENT-K-018 — Leon: special-damage rules missing
- **Status:** PARTIAL
- **Original:** ORIG `Leon.java:220` — 4× damage vs Kraken; Ender-Dragon-part hit handling; hurt window 10
- **Port:** `entity\EntityLeon.java:260-268` — 55 melee + knockback only; hurt window 15
- **Fix:** in `doHurtTarget`, multiply damage ×4 when target is Kraken; add EnderDragon part handling; set hurt window 10.

### ENT-K-019 — Leon: taming items changed
- **Status:** DIVERGENT
- **Original:** ORIG `Leon.java` — 1/3 tame with carrot; specific untame item
- **Port:** `entity\EntityLeon.java:598-612` — 1/3 tame with beef; untame with glass (`:628`); Leonopteryx tames with any `ItemTags.MEAT`
- **Fix:** change tame item to carrot at `EntityLeon.java:598-612`; restore the original untame item (verify ORIG `Leon.java` untame item).
- **Resolution:** FIXED (2026-06-11, Phase C — audit wrong on tame item: orig Leon.java:986 tames with raw BEEF 1-in-3 (port already correct); untame fixed glass → dead bush per orig Leon.java:1035; see phase_c_reports/C3_entities_K_R.md)

### ENT-K-020 — Leon: drops replaced with diamonds/gold
- **Status:** DIVERGENT
- **Original:** ORIG `Leon.java` — 4–9 raw chicken, 16–21 feather, 2–7 KrakenRepellent, 1/5 MyBattleAxe
- **Port:** `entity\EntityLeon.java:686-696` — 4–9 diamond + 16–21 gold ingot, plus `LT leon.json` bones/leather; `LT leonopteryx.json` battle_axe 100% (orig 20%) + kraken_repellent 1–2 (orig 2–7)
- **Fix:** code path → 4–9 chicken + 16–21 feather + 2–7 kraken_repellent + 20% battle_axe; leonopteryx.json → battle_axe 20%, kraken_repellent 2–7; remove duplicate path (ENT-SYS2-001).
- **Resolution:** FIXED (2026-06-11, Phase B — dropCustomDeathLoot deleted; leon.json/leonopteryx.json rewritten to the orig list (chicken 4-9, feather 16-21, kraken repellent 2-7, battle axe 1-in-5), see phase_b_reports/B3_riders.md Task 7)

### ENT-K-021 — Leon: natural spawning added (incl. Nether)
- **Status:** DIVERGENT
- **Original:** ORIG — spawner-gated only ("Leonopteryx" spawner, Y>50, no nearby Leons)
- **Port:** `BM companion_leon.json` jungle w1 + `add_nether_spawns.json` Nether w2; leonopteryx mountains w1
- **Fix:** remove leon from `add_nether_spawns.json` (a Pandora-themed mount in the Nether is wrong); gate jungle/mountain spawns behind `checkSpawnRules` (Y>50, no nearby Leons).
- **Resolution:** FIXED (2026-06-11, Phase C — orig has NO addSpawn for Leon (spawner/summon only); all three invented natural-spawn entries removed; Leonopteryx dungeon spawners already exist in LegacyDungeonPiece; see phase_c_reports/C3_entities_K_R.md)

### ENT-K-022 — Leonopteryx: hurt/death/living sounds missing
- **Status:** PARTIAL
- **Original:** ORIG `Leon.java` — `leon_living`/`leon_hit`/`leon_death` + `mothrawings` flaps
- **Port:** `entity\Leonopteryx.java` — wing flaps only; no hurt/death/living overrides (EntityLeon has them, `EntityLeon.java:226-313`)
- **Fix:** add `getAmbientSound`/`getHurtSound`/`getDeathSound` returning the leon_* ModSounds in `Leonopteryx.java`.

---

## Lizard

### ENT-K-023 — Lizard: water-seeking replaced by fire-seeking
- **Status:** DIVERGENT
- **Original:** ORIG `Lizard.java` `scan_it` — sought water/lava blocks
- **Port:** `entity\Lizard.java:149-178` — `scanForFire` seeks LAVA/FIRE blocks only
- **Fix:** extend `scanForFire` to also target water blocks (restore original water/lava set), or rename + restore the original scan target list.
- **Resolution:** FIXED (2026-06-11, Phase C — audit half-wrong: orig Lizard.java:184-227 scan_it seeks WATER ONLY (still + flowing), no lava; port rewritten to scanForWater; see phase_c_reports/C3_entities_K_R.md)

### ENT-K-024 — Lizard: spawn domain widened
- **Status:** DIVERGENT
- **Original:** `OreSpawnMain.java:4868-4870` — water biomes, weight 2–5/2-4
- **Port:** `BM add_overworld_creatures.json` — all-overworld w10/1-2, `checkSpawnRules` Y≥50 (`Lizard.java:258-260`)
- **Fix:** restrict the BM to river/swamp/beach-tagged biomes at w2–5/2-4.
- **Resolution:** FIXED (2026-06-11, Phase C — per-biome BMs river w5/swamp w4/ocean w2, all 2-4, per orig OreSpawnMain.java:4868-4870; orig getCanSpawnHere is Y≥50 only, which the port already enforces; see phase_c_reports/C3_entities_K_R.md)

---

## LurkingTerror

### ENT-K-025 — LurkingTerror: stats changed
- **Status:** DIVERGENT
- **Original:** `OreSpawnMain.java:6503` — HP 30, ATK 6 (melee 5), def 5, speed 0.25, xp 20
- **Port:** `entity\EntityLurkingTerror.java:42-47` — HP 40, ATK 5, no armor
- **Fix:** set MAX_HEALTH 30, ATTACK_DAMAGE 6, ARMOR 5.
- **Resolution:** FIXED (2026-06-11, Phase B — see FIX_LOG.md and phase_b_reports/B2_mobstats.md)

### ENT-K-026 — LurkingTerror: target exclusion list reduced
- **Status:** PARTIAL
- **Original:** ORIG `LurkingTerror.java` — excluded flying mobs and a long list of OreSpawn species
- **Port:** `entity\EntityLurkingTerror.java:191-197` — excludes only other LurkingTerrors
- **Fix:** add exclusions for flying mobs (`entity.isNoGravity()`/flying flag) and the original species list to the target predicate.

### ENT-K-027 — LurkingTerror: drops changed
- **Status:** DIVERGENT
- **Original:** ORIG `LurkingTerror.java` — random carrot / rotten flesh / feather
- **Port:** `LT lurking_terror.json` — 3–8 bone + 30% 1–3 diamond
- **Fix:** rewrite `lurking_terror.json` to one-of carrot/rotten_flesh/feather; remove diamonds.
- **Resolution:** FIXED (2026-06-11, Phase C — audit's claimed list wrong: verified orig LurkingTerror.java:368-377 one-of is raw BEEF/FLINT/FEATHER; see phase_c_reports/C3_entities_K_R.md)

### ENT-K-028 — LurkingTerror: spawn domain/gating changed
- **Status:** PARTIAL
- **Original:** ORIG — spawner-gated + light + Y>10 + no nearby LTs + Islands-dim special
- **Port:** `BM add_overworld_monsters.json` — overworld w2/1-1, no rules
- **Fix:** add `checkSpawnRules` (light + Y>10 + no nearby LurkingTerror); see ENT-SYS2-004.

---

## Mantis

### ENT-K-029 — Mantis: stats reduced ~17%
- **Status:** DIVERGENT
- **Original:** `OreSpawnMain.java:6467` — HP 120, ATK 16, def 10, speed 0.32, xp 100
- **Port:** `entity\EntityMantis.java:58-63` — HP 100, ATK 12, no armor
- **Fix:** set MAX_HEALTH 120, ATTACK_DAMAGE 16, ARMOR 10.
- **Resolution:** FIXED (2026-06-11, Phase B — see FIX_LOG.md and phase_b_reports/B2_mobstats.md)

### ENT-K-030 — Mantis: butterfly-prey behavior lost
- **Status:** PARTIAL
- **Original:** ORIG `Mantis.java` — targets players, mobs, butterflies, Cockateil, Fairy; avoided water/mantises/many species
- **Port:** `entity\EntityMantis.java:239-248` — players + Monster only, excludes Mantis/Bee/in-water
- **Fix:** add Butterfly/Cockateil (and Fairy if ported) to the target predicate.

### ENT-K-031 — Mantis: drop substitutions + double path
- **Status:** DIVERGENT
- **Original:** ORIG `Mantis.java` — mantis_claw×2, painting, gunpowder 2–11, uranium 1–3, titanium 1–3, raw beef 2–5
- **Port:** `LT mantis.json` — gold ingot 2–4 instead of beef, xp bottle instead of painting; PLUS code (`EntityMantis.java:215-226`) name tag + 2–11 spider eye + 2–4 gold ingot
- **Fix:** keep only `mantis.json`; change gold ingot → raw beef 2–5; delete the code drop layer (ENT-SYS2-001).
- **Resolution:** FIXED (2026-06-11, Phase B — see FIX_LOG.md and phase_b_reports/B1_drops.md)

---

## MantisClaw (item)

### ENT-K-032 — MantisClaw: damage/durability vs original unverified
- **Status:** UNVERIFIED
- **Original:** ORIG `MantisClaw.java` — ItemSword, dmg 10, 1000 durability
- **Port:** `item\MantisClaw.java:12` — SwordItem on `ModToolTiers.AMETHYST`; effective damage/durability come from the tier and were not compared
- **Fix:** verification failed because the audit did not read `ModToolTiers.AMETHYST` values. Resolve by reading `ModToolTiers.java` (AMETHYST attack bonus + uses) and comparing to 10 dmg / 1000 uses; adjust the tier or use a dedicated constructor if off.

### ENT-K-033 — MantisClaw: lifesteal mechanic simplified
- **Status:** DIVERGENT
- **Original:** ORIG `MantisClaw.java` — applies negative-regen effect to target + positive-regen to attacker (effect-based drain)
- **Port:** `item\MantisClaw.java:16-23` — flat 1.0 magic dmg to target + 1.0 heal per hit
- **Fix:** in `hurtEnemy`, apply a short Regeneration effect to the attacker and a Wither/negative-regen analog to the target, matching original durations.
- **Resolution:** FIXED (2026-06-11, Phase C — audit wrong: orig MantisClaw.java:36-37 uses NO potion effects, it is a one-shot heal(-1) silent drain on the target + heal(+1) on the attacker; port now drains via setHealth (no damage event/invuln frames); see phase_c_reports/C3_entities_K_R.md)

---

## Molenoid

### ENT-K-034 — Molenoid: stats halved
- **Status:** DIVERGENT
- **Original:** `OreSpawnMain.java:6478` — HP 200, ATK 18, def 12, speed 0.35, xp 40
- **Port:** `entity\EntityMolenoid.java:62-68` — HP 100, ATK 10, no armor
- **Fix:** set MAX_HEALTH 200, ATTACK_DAMAGE 18, ARMOR 12.
- **Resolution:** FIXED (2026-06-11, Phase B — see FIX_LOG.md and phase_b_reports/B2_mobstats.md)

### ENT-K-035 — Molenoid: digging direction inverted, MoleDirtBlock missing
- **Status:** PARTIAL
- **Original:** ORIG `Molenoid.java` — places `MyMoleDirtBlock` while moving + destroys dirt/grass/sand/gravel AHEAD
- **Port:** `entity\EntityMolenoid.java:169-186` — `clearPathBehind()` destroys 3-high BEHIND; `throwBlocksAtTarget` (`:151-167`) places vanilla DIRT near target; both mobGriefing-gated
- **Fix:** change `clearPathBehind` to clear ahead of movement direction; port `MoleDirtBlock` (or keep vanilla dirt but place along the dug path, not at target).

### ENT-K-036 — Molenoid: drops doubled (nose drops twice) + substitutions
- **Status:** DIVERGENT
- **Original:** ORIG `Molenoid.java` — nose + painting + 10 gunpowder + 6 carrot
- **Port:** code `EntityMolenoid.java:209-219` — nose + name tag + 10 leather + 6 bone; PLUS `LT molenoid.json` 2nd nose + 2nd name tag + 6–10 rotten flesh + 3–6 slime
- **Fix:** keep one path: nose ×1 + 10 gunpowder + 6 carrot (painting optional); delete the other (ENT-SYS2-001).
- **Resolution:** FIXED (2026-06-11, Phase B — see FIX_LOG.md and phase_b_reports/B1_drops.md)

### ENT-K-037 — Molenoid: spawn weight 4×
- **Status:** PARTIAL
- **Original:** `OreSpawnMain.java:4741-4743` — weight 2/1-2 select biomes + spawner/darkness/clearance gates
- **Port:** `BM add_cave_spawns.json` — overworld w8/1-2, no gates
- **Fix:** reduce weight to 2 in `add_cave_spawns.json`; gates per ENT-SYS2-004.

---

## Mothra

### ENT-K-038 — Mothra: HP ×3.3, ATK ×2.5
- **Status:** DIVERGENT
- **Original:** `OreSpawnMain.java:6469` — HP 150, ATK 12, def 8, speed 0.35, xp 100
- **Port:** `entity\Mothra.java:68-74` — HP 500, ATK 30, no armor
- **Fix:** set MAX_HEALTH 150, ATTACK_DAMAGE 12, ARMOR 8 (or document multipart-boss rebalance intentionally).
- **Resolution:** FIXED (2026-06-11, Phase B — see FIX_LOG.md and phase_b_reports/B2_mobstats.md)

### ENT-K-039 — Mothra: BetterFireball difficulty variant missing
- **Status:** PARTIAL
- **Original:** ORIG `Mothra.java` — fired `BetterFireball` on normal/hard difficulty
- **Port:** `entity\Mothra.java:211-221` — `SmallFireball` only
- **Fix:** in the fireball spawn, branch on `level.getDifficulty()` and fire `BetterFireball` for NORMAL/HARD.

### ENT-K-040 — Mothra: death moth-swarm missing + drop substitutions/doubling
- **Status:** DIVERGENT
- **Original:** ORIG `Mothra.java` — painting, 53 gunpowder, 25 moth scale, 3 blaze rod, nether star, + 20 Moth entities spawned on death
- **Port:** code `Mothra.java:300-305` — nether star + 53 xp bottle + 3 emerald; `LT mothra.json` xp bottle + 25–53 gunpowder + 15–25 moth scale + arrows (double path); no moths spawned
- **Fix:** in `die()`, spawn 20 Moth entities at death position; consolidate drops to one path: 53 gunpowder + 25 moth scale + 3 blaze rod + nether star.
- **Resolution:** FIXED (2026-06-11, Phase B — see FIX_LOG.md and phase_b_reports/B1_drops.md)

### ENT-K-041 — Mothra: "flight + rider control" claim unverifiable
- **Status:** UNVERIFIED
- **Original:** ORIG `Mothra.java` — no rider logic found in the decompile read
- **Port:** `entity\Mothra.java` — no rider logic either
- **Fix:** verification failed because the audited prompt asserted a rider feature that neither codebase shows. Resolve by grepping ORIG `Mothra.java` for `riddenByEntity`/`func_70085_c` and the 1.7.10 changelog; if truly absent, close as not-a-feature. Do not implement without evidence.

---

## Nastysaurus

### ENT-K-042 — Nastysaurus: HP halved, armor gone
- **Status:** DIVERGENT
- **Original:** `OreSpawnMain.java:6471` — HP 200, ATK 32, def 17, speed 0.35, xp 40
- **Port:** `entity\Nastysaurus.java:51-57` — HP 100, ATK 25, no armor
- **Fix:** set MAX_HEALTH 200, ATTACK_DAMAGE 32, ARMOR 17.
- **Resolution:** FIXED (2026-06-11, Phase B — see FIX_LOG.md and phase_b_reports/B2_mobstats.md)

### ENT-K-043 — Nastysaurus: drop table inflated to 40 valuables
- **Status:** DIVERGENT
- **Original:** ORIG `Nastysaurus.java` — 10 coal + 10 stick + 10 bone + 10 arrow
- **Port:** code `Nastysaurus.java:134-148` — 10 gold + 10 emerald + 10 diamond + 10 iron; PLUS `LT nastysaurus.json` bones/gunpowder
- **Fix:** replace code drops with 10 coal + 10 stick + 10 bone + 10 arrow (or move into the JSON and delete the code path, ENT-SYS2-001).
- **Resolution:** VERIFIED-CORRECT (2026-06-11, Phase B — audit value wrong: verified orig Nastysaurus.java:156-170 drops 10× iron ingot/rotten flesh/leather/string; JSON consolidated to exactly that, see phase_b_reports/B1_drops.md)

---

## Ostrich

### ENT-K-044 — Ostrich: rideable but unsteerable
- **Status:** PARTIAL
- **Original:** ORIG `Ostrich.java` — full rider movement + jump via `flyup_keystate`
- **Port:** `entity\Ostrich.java:93-99,109` — `player.startRiding` works and AI suspends, but no `tickRidden`/`getRiddenInput`/`getControllingPassenger`
- **Fix:** implement `getControllingPassenger`, `getRiddenInput` (rider WASD), `getRiddenSpeed`, and jump handling.
- **Resolution:** FIXED (2026-06-11, Phase B — see FIX_LOG.md and phase_b_reports/B3_riders.md)

### ENT-K-045 — Ostrich: damage-immunity rule inverted
- **Status:** DIVERGENT
- **Original:** ORIG `Ostrich.java` `func_70097_a` — immune to everything EXCEPT cactus (cactus was its only weakness)
- **Port:** `entity\Ostrich.java:67-71` — immune TO cactus only, vulnerable to everything else
- **Fix:** invert `isInvulnerableTo`: return true for all sources except `DamageTypes.CACTUS`.
- **Resolution:** VERIFIED-CORRECT (2026-06-11, Phase C — audit misread orig Ostrich.java:133-138: super.hurt runs for every NON-cactus source, i.e. the Ostrich is immune to cactus ONLY, exactly what the port does; the orig always-return-false quirk was additionally aligned; see phase_c_reports/C3_entities_K_R.md)

### ENT-K-046 — Ostrich: tamed drop table changed
- **Status:** DIVERGENT
- **Original:** ORIG `Ostrich.java` — tamed → 2–6 flower/sand items; else default
- **Port:** `LT ostrich.json` — 1–3 chicken
- **Fix:** restore tamed-conditional drop (2–6 flower/sand) via loot condition on tamed flag or code path; untamed → vanilla-equivalent default.
- **Resolution:** FIXED (2026-06-11, Phase C — verified orig Ostrich.java:283-294: tamed → 2-6 POPPY (red_flower, no sand); untamed → vanilla default = feather 0-2 (+looting, orig :279-281); NBT-branched ostrich.json on OreSpawnTamed like Gazelle; see phase_c_reports/C3_entities_K_R.md)

---

## Peacock

### ENT-K-047 — Peacock: termite hunting missing
- **Status:** MISSING
- **Original:** ORIG `Peacock.java` — `targetTasks` NearestAttackableTarget(Termite) + melee 6
- **Port:** `entity\Peacock.java` — no target selector, no attack at all
- **Fix:** add `NearestAttackableTargetGoal<EntityTermite>` + `MeleeAttackGoal` with attack damage 6.

### ENT-K-048 — Peacock: egg laying missing
- **Status:** MISSING
- **Original:** ORIG `Peacock.java` — laid `PeacockEgg` every 5000 ticks
- **Port:** `entity\Peacock.java` — absent
- **Fix:** add an egg-lay timer in `aiStep` (5000 ticks → spawn ItemEntity of peacock egg item; port the item if missing).

### ENT-K-049 — Peacock: spawn gates dead code, weights changed
- **Status:** PARTIAL
- **Original:** ORIG — w1/1-3 select biomes + daytime + Y 50–100 + ≤2 nearby
- **Port:** overworld w8/1-2 + crystal dim w5/4-8; `findBuddies()` exists (`Peacock.java:111-114`) but is never called
- **Fix:** call `findBuddies()` from a `checkSpawnRules` override (daytime + Y 50–100 + ≤2 nearby); lower overworld weight to ~1/1-3.

### ENT-K-050 — Peacock: breeding item changed
- **Status:** DIVERGENT
- **Original:** ORIG `Peacock.java` — wheat
- **Port:** `entity\Peacock.java:117-119` — wheat seeds
- **Fix:** change `isFood` to `Items.WHEAT`.
- **Resolution:** FIXED (2026-06-11, Phase C — audit wrong: orig Peacock.java:259-261 breeding item is MyCrystalApple, not wheat; isFood now ModItems.CRYSTAL_APPLE; see phase_c_reports/C3_entities_K_R.md)

---

## PitchBlack (Nightmare)

### ENT-K-051 — PitchBlack: continuous scale discretized + speed model changed
- **Status:** DIVERGENT
- **Original:** `OreSpawnMain.java:6517` — continuous scale 0.5–4.0 (config `NightmareSize`); HP 250×scale, ATK 30×scale, def 10+2×scale, speed 0.2+0.1×scale
- **Port:** `entity\PitchBlack.java:87-91`, `OreSpawnConfig.java:145` — 5 discrete tiers HP 125–1000, ATK 15–120, armor 10–18; flight speed 0.5+scale/10. Stat envelope matches at extremes; discretization + speed formula diverge
- **Fix:** accept tiers (well-matched envelope) but align speed formula to 0.2+0.1×scale, or restore continuous scale from config.
- **Resolution:** FIXED (2026-06-11, Phase C — remainder closed: audit wrong about "continuous" scale — orig PitchBlack.java:99-141 rolls the SAME five discrete values t ∈ {0.5,1,2,3,4} via cascading dice (1/4→1, 1/8→2, 1/32→3, 1/64→4); port SIZE_SCALE corrected from {0.5..1.25} to the orig values, hitbox to 2.5t×3.5t (orig :145), spawn dice now the orig cascade, and the AI flight speed 0.5+t/10 matches orig :389 verbatim; see phase_c_reports/C3_entities_K_R.md)

### ENT-K-052 — PitchBlack: bonus damage vs dragons missing
- **Status:** PARTIAL
- **Original:** ORIG `PitchBlack.java` — bonus damage vs EntityDragon/Godzilla
- **Port:** `entity\PitchBlack.java:296-313` — melee + scaled knockback only
- **Fix:** in `doHurtTarget`, add the original damage multiplier when target is EnderDragon or Godzilla.

### ENT-K-053 — PitchBlack: minor drop extras missing
- **Status:** PARTIAL
- **Original:** ORIG — 1 nightmare_scale, 2–7 zoo_keeper, painting, random stick/feather/arrow/flesh/carrot extras
- **Port:** `LT pitch_black.json` — 3–8 bone, 1–3 nightmare_scale, xp bottle, 1–5 zoo_keeper
- **Fix:** adjust `pitch_black.json`: scale ×1, zoo_keeper 2–7, add the random junk-extras pool. (Natural spawning: see ENT-SYS2-003.)

---

## Pointysaurus

### ENT-K-054 — Pointysaurus: ATK doubled, armor lost
- **Status:** DIVERGENT
- **Original:** `OreSpawnMain.java:6472` — HP 80, ATK 10, def 16, speed 0.35, xp 40
- **Port:** `entity\Pointysaurus.java:62-68` — HP 80, ATK 20, no armor
- **Fix:** set ATTACK_DAMAGE 10, ARMOR 16.
- **Resolution:** FIXED (2026-06-11, Phase B — see FIX_LOG.md and phase_b_reports/B2_mobstats.md)

### ENT-K-055 — Pointysaurus: drops inflated to diamonds
- **Status:** DIVERGENT
- **Original:** ORIG `Pointysaurus.java` — 10 bone + 6 carrot + 6 stick + 6 arrow
- **Port:** code `Pointysaurus.java:150-164` — 10 diamond + 6 beef + 6 emerald + 6 iron; PLUS `LT pointysaurus.json` bones (double path)
- **Fix:** restore 10 bone + 6 carrot + 6 stick + 6 arrow in one path; delete the other (ENT-SYS2-001).
- **Resolution:** FIXED (2026-06-11, Phase B — audit's claimed original list wrong; fixed to the verified orig Pointysaurus.java:127-141 list: 10 leather + 6 beef + 6 rotten flesh + 6 string, see phase_b_reports/B1_drops.md)

---

## PurplePower

### ENT-K-056 — PurplePower: potion type 2 swapped
- **Status:** DIVERGENT
- **Original:** ORIG `PurplePower.java` — attack type 2 applies Weakness
- **Port:** `entity\PurplePower.java:159` — type 2 applies HUNGER
- **Fix:** change the type-2 effect at `PurplePower.java:159` to `MobEffects.WEAKNESS`.
- **Resolution:** FIXED (2026-06-11, Phase C — audit wrong: orig PurplePower.java:301-306 type 2 = POISON 50t, type 3 = WEAKNESS 50t; both port effects corrected (were Hunger/Poison); see phase_c_reports/C3_entities_K_R.md)

---

## Rat

### ENT-K-057 — Rat: stats changed
- **Status:** DIVERGENT
- **Original:** `OreSpawnMain.java:6483` — HP 5, ATK 3, def 1, speed 0.25, xp 5
- **Port:** `entity\EntityRat.java:56-61` — HP 10, ATK 2
- **Fix:** set MAX_HEALTH 5, ATTACK_DAMAGE 3, ARMOR 1.
- **Resolution:** FIXED (2026-06-11, Phase B — see FIX_LOG.md and phase_b_reports/B2_mobstats.md)

### ENT-K-058 — Rat: default-passive configs flip original hostility
- **Status:** DIVERGENT
- **Original:** ORIG `Rat.java` — wild rats attacked players
- **Port:** `OreSpawnConfig.java:143-144` — `RAT_PLAYER_FRIENDLY`/`RAT_PET_FRIENDLY` default TRUE → never attacks players/pets (`EntityRat.java:160-183`)
- **Fix:** flip both config defaults to false (keep configs for opt-in friendliness).
- **Resolution:** FIXED (2026-06-11, Phase C — audit's fix wrong: orig OreSpawnMain.java:1472-1473 defaults BOTH configs to 1 (true); the real divergence was the port applying them to ALL rats — orig Rat.java:230-246 gates both configs on myowner != null, so WILD rats attack players/pets regardless; predicate rescoped to owned rats, defaults kept true; see phase_c_reports/C3_entities_K_R.md)

### ENT-K-059 — Rat: drop changed
- **Status:** DIVERGENT
- **Original:** ORIG `Rat.java` — stick
- **Port:** `LT rat.json` — 0–1 bone + 0–1 rotten flesh
- **Fix:** rewrite `rat.json` to drop 1 stick.
- **Resolution:** FIXED (2026-06-11, Phase C — audit wrong: orig Rat.java:140-142 getDropItem is ROTTEN FLESH (field_151078_bh), vanilla count 0-2 (+looting); rat.json rewritten accordingly; see phase_c_reports/C3_entities_K_R.md)

### ENT-K-060 — Rat: swarm spawning lost
- **Status:** DIVERGENT
- **Original:** `OreSpawnMain.java:4977-4978` — weight 25–35 / group 2–20 in select biomes + Crystal-dim air-pocket checks + ≤8 nearby
- **Port:** BM overworld-wide w20/1-3, no checks
- **Fix:** set group size 2–20 (cap via ≤8-nearby `checkSpawnRules`) and restrict biome set.
- **Resolution:** PARTIAL (2026-06-11, Phase C — weights/biomes done: overworld-wide entry removed, swarm_rat__dark_forest.json w35/10-20 + swarm_rat__taiga.json w25/2-8 per orig OreSpawnMain.java:4977-4978; darkness/spawner/Crystal-air-pocket/≤8-buddy gates → ENT-SYS-002 (Phase D); see phase_c_reports/C3_entities_K_R.md)

---

## RedCow

### ENT-K-061 — RedCow: invented wheat bonus drop
- **Status:** DIVERGENT
- **Original:** ORIG `RedCow.java` — vanilla cow drops + 1–2 bonus leather-class item
- **Port:** `LT red_cow.json` 1–3 leather + code `RedCow.java:23-30` 1–3 wheat (both fire)
- **Fix:** drop the wheat code path; keep cow drops + 1–2 bonus leather in the JSON (ENT-SYS2-001).
- **Resolution:** FIXED (2026-06-11, Phase B — see FIX_LOG.md and phase_b_reports/B1_drops.md)

---

## Robot2 (Robo-Pounder)

### ENT-K-062 — Robot2: HP 2.5×
- **Status:** DIVERGENT
- **Original:** `OreSpawnMain.java:6495` — HP 200, ATK 22, def 18, speed 0.3, xp 100
- **Port:** `entity\Robot2.java:64-70` — HP 500, ATK 30, armor 8
- **Fix:** set MAX_HEALTH 200, ATTACK_DAMAGE 22, ARMOR 18.
- **Resolution:** FIXED (2026-06-11, Phase B — see FIX_LOG.md and phase_b_reports/B2_mobstats.md)

### ENT-K-063 — Robot2: signature block-destruction griefing missing (moved to Robot4)
- **Status:** PARTIAL
- **Original:** ORIG `Robot2.java` — destroys blocks around self/target (PlayNicely-gated)
- **Port:** `entity\Robot2.java` — melee only; the griefing was relocated to port Robot4 (`Robot4.java:118-206`)
- **Fix:** move/copy the ground-pound terrain destruction from Robot4 back into Robot2 (mobGriefing-gated), restoring each robot's original identity.

### ENT-K-064 — Robot2: drops slashed
- **Status:** DIVERGENT
- **Original:** ORIG `Robot2.java` — 2–9 iron BLOCK + 5–10 coal + large random table
- **Port:** `LT robot_2.json` — 2–5 iron ingot + 25% 0–2 gold ingot
- **Fix:** rewrite `robot_2.json`: iron_block 2–9 + coal 5–10 + port the random table.
- **Resolution:** FIXED (2026-06-11, Phase C — audit half-wrong: orig Robot2.java:165-221 drops 2-9 iron BLOCK + 5-10 iron INGOT (not coal) + 5-14 rolls of a d15 redstone-component table; robot_2.json rewritten to all three pools; see phase_c_reports/C3_entities_K_R.md)

---

## Robot3 (Robo-Gunner)

### ENT-K-065 — Robot3: HP ×3.75
- **Status:** DIVERGENT
- **Original:** `OreSpawnMain.java:6496` — HP 80, ATK 16, def 14, speed 0.35, xp 60
- **Port:** `entity\Robot3.java:62-68` — HP 300, ATK 20, armor 6
- **Fix:** set MAX_HEALTH 80, ATTACK_DAMAGE 16, ARMOR 14.
- **Resolution:** FIXED (2026-06-11, Phase B — see FIX_LOG.md and phase_b_reports/B2_mobstats.md)

### ENT-K-066 — Robot3: LaserBall ammo drop missing
- **Status:** DIVERGENT
- **Original:** ORIG `Robot3.java` — 5–10 × MyLaserBall(4) + random table
- **Port:** `LT robot_3.json` — 3–6 iron ingot + 20% diamond; LaserBall item exists (`ModItems.java:335`) but is never dropped
- **Fix:** add `orespawn:laser_ball` 5–10 to `robot_3.json`.
- **Resolution:** FIXED (2026-06-11, Phase C — robot_3.json rewritten: 5-10 rolls of laser_ball ×4 + 5-14 rolls of the orig d15 redstone table (orig Robot3.java:166-219); see phase_c_reports/C3_entities_K_R.md)

### ENT-K-067 — Robot3: shot sound missing
- **Status:** PARTIAL
- **Original:** ORIG `Robot3.java` — `fireworks.launch` on each shot
- **Port:** `entity\Robot3.java:121-130` — `fireLaserAt` plays no sound
- **Fix:** play `SoundEvents.FIREWORK_ROCKET_LAUNCH` in `fireLaserAt`.

---

## Robot4 (Robo-Warrior)

### ENT-K-068 — Robot4: HP ×4.4
- **Status:** DIVERGENT
- **Original:** `OreSpawnMain.java:6497` — HP 170, ATK 12 (melee 15/20/25 by difficulty), def 18, speed 0.34, xp 120
- **Port:** `entity\Robot4.java:91-97` — HP 750, ATK 40, armor 10
- **Fix:** set MAX_HEALTH 170, ARMOR 18; implement difficulty-scaled melee 15/20/25.
- **Resolution:** VERIFIED-CORRECT (2026-06-11, Phase C — remainder closed: audit wrong about the 15/20/25 melee — orig Robot4.java:145-156 getAttackStrength is NEVER CALLED (unlike Spyro/Stinky/ThePrince which invoke theirs) and is internally bugged (the NORMAL/HARD branches are nested inside the EASY branch, unreachable); orig melee = ATTACK_DAMAGE attribute 12, which the port already uses after Phase B; no code change; see phase_c_reports/C3_entities_K_R.md)

### ENT-K-069 — Robot4: ranged LaserBall attack missing
- **Status:** PARTIAL
- **Original:** ORIG `Robot4.java` — hybrid melee/ranged: LaserBall normal + special variants
- **Port:** `entity\Robot4.java` — melee bruiser only; also carries Robot2's relocated griefing (`:118-206`)
- **Fix:** add a ranged LaserBall attack loop (normal + special types) mirroring Robot3's `fireLaserAt` plumbing; return the griefing to Robot2 (ENT-K-063).

### ENT-K-070 — Robot4: shielding is dead state
- **Status:** PARTIAL
- **Original:** ORIG `Robot4.java` — active shielding window after being hit
- **Port:** `entity\Robot4.java:252` — `DATA_SHIELDING` defined + checked in `hurt()` but no code ever calls `setShielding(1)`
- **Fix:** call `setShielding(1)` when hurt (tie to the existing 65-tick `wasAttackedTicker`) and clear when it expires.

### ENT-K-071 — Robot4: RayGun + ammo drops missing
- **Status:** DIVERGENT
- **Original:** ORIG `Robot4.java` — 5–14 LaserBall(4) + MyRayGun + painting + randoms
- **Port:** `LT robot_4.json` — 2–5 iron + 2–5 redstone
- **Fix:** add `orespawn:laser_ball` 5–14 + `orespawn:ray_gun` ×1 to `robot_4.json`.
- **Resolution:** FIXED (2026-06-11, Phase C — robot_4.json rewritten per orig Robot4.java:195-250: 5-14 rolls of laser_ball ×4 + ray_gun ×1 + painting ×1 + 10-24 rolls of the d15 redstone table; see phase_c_reports/C3_entities_K_R.md)

---

## Robot5 (Robo-Sniper)

### ENT-K-072 — Robot5: HP ×7.5 (largest stat inflation in slice)
- **Status:** DIVERGENT
- **Original:** `OreSpawnMain.java:6498` — HP 20, ATK 5, def 6, speed 0.3, xp 20
- **Port:** `entity\Robot5.java:63-68` — HP 150, ATK 15, armor 4
- **Fix:** set MAX_HEALTH 20, ATTACK_DAMAGE 5, ARMOR 6.
- **Resolution:** FIXED (2026-06-11, Phase B — see FIX_LOG.md and phase_b_reports/B2_mobstats.md)

### ENT-K-073 — Robot5: ammo drop missing
- **Status:** DIVERGENT
- **Original:** ORIG `Robot5.java` — 5–10 LaserBall(4) + randoms
- **Port:** `LT robot_5.json` — 4–8 iron + 1–3 gold
- **Fix:** add `orespawn:laser_ball` 5–10 to `robot_5.json`.
- **Resolution:** FIXED (2026-06-11, Phase C — robot_5.json rewritten per orig Robot5.java:138-190: 5-10 rolls of laser_ball ×4 + 2-6 rolls of the d15 redstone table; see phase_c_reports/C3_entities_K_R.md)

### ENT-K-074 — Robot5: shot sound missing
- **Status:** PARTIAL
- **Original:** ORIG `Robot5.java` — `fireworks.launch` per shot
- **Port:** `entity\Robot5.java:124-133` — `fireLaserAt` silent
- **Fix:** play `SoundEvents.FIREWORK_ROCKET_LAUNCH` in `fireLaserAt`.

---

## RockBase

### ENT-K-075 — RockBase: Crystal-dimension type lottery missing
- **Status:** PARTIAL
- **Original:** ORIG `RockBase.java:129-140` — Crystal-dimension branch forces types 9–12
- **Port:** `entity\RockBase.java:95-106` — single overworld lottery (1→12) only
- **Fix:** in the type-roll, branch on Crystal dimension and constrain types to 9–12.

### ENT-K-076 — RockBase: death drops missing + placed rocks lose type
- **Status:** MISSING
- **Original:** ORIG `RockBase.java:213-251` — `func_70645_a` drops the matching rock item (MySmallRock…MyCrystalTNTRock) per type on death
- **Port:** no death drop (no override, no `LT rock_base.json`); also `item\ItemRock.java:42-54` `useOn` never calls `placeRock(rockType)`, so a placed rock re-randomizes
- **Fix:** override `die()` to drop the item matching `getRockType()`; in `ItemRock.useOn`, call `placeRock(rockType)` after spawning. High priority — rock pickup loop is broken.

### ENT-K-077 — RockBase: Y≥50 spawn rule missing
- **Status:** PARTIAL
- **Original:** ORIG `RockBase.java` `func_70601_bi` — Y≥50
- **Port:** no `checkSpawnRules` (MISC category, placed via ItemRock/`world\CrystalStructures.java`)
- **Fix:** add Y≥50 check in `checkSpawnRules` (low priority — placement is mostly structural).

---

## Rotator

### ENT-K-078 — Rotator: stats reduced
- **Status:** DIVERGENT
- **Original:** `OreSpawnMain.java:6499`, `Rotator.java:64-69` — HP 35, ATK 10, def 8, speed 0.25, xp 35
- **Port:** `entity\EntityRotator.java:42-47` — HP 30, ATK 5, no armor
- **Fix:** set MAX_HEALTH 35, ATTACK_DAMAGE 10, ARMOR 8.
- **Resolution:** FIXED (2026-06-11, Phase B — see FIX_LOG.md and phase_b_reports/B2_mobstats.md)

### ENT-K-079 — Rotator: target exclusion list reduced from 16 to 4 species
- **Status:** PARTIAL
- **Original:** ORIG `Rotator.java` — excluded Termite, Vortex, DungeonBeast, CrystalCow, Irukandji, Skate, Whale, Flounder, Urchin, TerribleTerror, LurkingTerror, CloudShark, Mothra, Bee, Mantis, etc.
- **Port:** `entity\EntityRotator.java:168-179` — excludes Rotator/Peacock/CloudShark/TerribleTerror only
- **Fix:** extend the exclusion predicate to the full original species list (those that exist in the port).

### ENT-K-080 — Rotator: `was_spawnered` persistence missing
- **Status:** MISSING
- **Original:** ORIG `Rotator.java:255-273` — persisted when spawned from a "Rotator" spawner
- **Port:** no equivalent
- **Fix:** add a `wasSpawnered` boolean (NBT-saved) set when `MobSpawnType == SPAWNER`, and make it force `setPersistenceRequired()`.

### ENT-K-081 — Rotator: crystal-ingot drops lost
- **Status:** DIVERGENT
- **Original:** ORIG `Rotator.java:385-400` — 1 of {CrystalPinkIngot, TigersEyeIngot, CrystalCoal block, iron ingot}
- **Port:** `LT rotator.json` — 2–5 iron nugget + 1–3 gunpowder
- **Fix:** rewrite `rotator.json` as a one-of pool: crystal_pink_ingot / tigers_eye_ingot / crystal_coal block / iron_ingot.
- **Resolution:** FIXED (2026-06-11, Phase C — verified orig Rotator.java:385-400; rotator.json rewritten to the equal-weight one-of pool, each 0-2 (+looting, vanilla drop core); see phase_c_reports/C3_entities_K_R.md)

---

## RubberDucky

### ENT-K-082 — RubberDucky: squid prey + buddy-follow dropped
- **Status:** PARTIAL
- **Original:** ORIG `RubberDucky.java:440-448` — at killCount≥5 also hunted EntitySquid/AttackSquid; followed buddy ducks
- **Port:** `entity\EntityRubberDucky.java:292-301` — players only at killCount≥5
- **Fix:** add Squid/AttackSquid to the vengeance target scan; restore buddy-follow movement bias.

### ENT-K-083 — RubberDucky: tame item changed, untame missing
- **Status:** DIVERGENT
- **Original:** ORIG `RubberDucky.java:242-287` — raw fish 1/2 tame; untame with dead bush
- **Port:** `entity\EntityRubberDucky.java:177-195` — wheat 1/2 tame; no untame; Tempt item also fish→wheat (`:72`)
- **Fix:** switch tame/tempt item to `Items.COD` (raw fish analog); add dead-bush untame interaction.
- **Resolution:** FIXED (2026-06-11, Phase C — verified orig RubberDucky.java:242 (raw fish, 1-in-2) and :273 (dead bush untame); tame/tempt switched to COD, untame added; see phase_c_reports/C3_entities_K_R.md)

### ENT-K-084 — RubberDucky: drops nothing
- **Status:** MISSING
- **Original:** ORIG `RubberDucky.java:223-231` — 50% feather, else 50% RubberDuckyEgg
- **Port:** `LT rubber_ducky.json` has empty pools; no code drops
- **Fix:** populate `rubber_ducky.json`: 50% feather / 50% rubber_ducky egg item (port the egg item if missing).

### ENT-K-085 — RubberDucky: never spawns naturally
- **Status:** MISSING
- **Original:** `OreSpawnMain.java:4873-4874` — water biomes w4–10 / groups 4–20 + "Rubber Ducky" spawner gate + daytime + Y≥50 (`RubberDucky.java:508-526`)
- **Port:** no biome modifier entry
- **Fix:** create a BM JSON for water biomes, weight 4–10 / group 4–20, plus `checkSpawnRules` daytime + Y≥50. (Also listed in ENT-SYS2-003.)

---

## RubyBird

### ENT-K-086 — RubyBird: stat overrides invented
- **Status:** DIVERGENT
- **Original:** ORIG `RubyBird.java` — inherits Cockateil stats (birdtype 5)
- **Port:** `entity\RubyBird.java:21-25` — HP 12 / speed 0.25 override (port Cockateil base 2 HP / 0.33, `Cockateil.java:48-51`)
- **Fix:** remove the overrides so RubyBird inherits Cockateil attributes (after Cockateil's own stats are validated).
- **Resolution:** FIXED (2026-06-11, Phase B — see FIX_LOG.md and phase_b_reports/B2_mobstats.md)

### ENT-K-087 — RubyBird: ruby can drop twice
- **Status:** DIVERGENT
- **Original:** ORIG drop unverified from decompile (dungeon-loot oriented)
- **Port:** code 1/3 ruby (`RubyBird.java:28-33`) PLUS `LT ruby_bird.json` feather 1–2 + 33% ruby — up to two rubies per kill
- **Fix:** delete the code drop; keep `ruby_bird.json` as the single path (ENT-SYS2-001). Verify ORIG `RubyBird.java` drop method before final tuning.
- **Resolution:** FIXED (2026-06-11, Phase B — see FIX_LOG.md and phase_b_reports/B1_drops.md)

### ENT-K-088 — RubyBird: spawn model changed (dungeon-only → natural)
- **Status:** DIVERGENT
- **Original:** ORIG — RubyBirdDungeon placement only (`func_70601_bi` true)
- **Port:** `BM dim_crystal_locals.json` — crystal_plains natural spawn w6/2-4
- **Fix:** if RubyBird dungeons are ported, remove the natural BM entry; otherwise keep as the substitute and document.
- **Resolution:** FIXED (2026-06-11, Phase C — RubyBird dungeons ARE ported (GenericDungeon.tryPlaceRubyDungeon places a RUBY_BIRD spawner); the invented crystal_plains natural-spawn entry removed from dim_crystal_locals.json; see phase_c_reports/C3_entities_K_R.md)

### ENT-K-089 — RubyBird: bespoke sound unused
- **Status:** DIVERGENT
- **Original:** ORIG `RubyBird.java` — `orespawn:rubybird` when not raining
- **Port:** inherits Cockateil `orespawn:birds` (`Cockateil.java:142-146`); rubybird sound asset unused
- **Fix:** override `getAmbientSound` in `RubyBird.java` to return the rubybird sound when `!level.isRaining()`.
- **Resolution:** FIXED (2026-06-11, Phase C — orig RubyBird.java:22-27 plays "orespawn:rubybird" when DAY && !raining (else silent, no fallback to birds); override added; see phase_c_reports/C3_entities_K_R.md)

---

## Scorpion

### ENT-S-001 — Scorpion: HP up, armor lost
- **Status:** DIVERGENT
- **Original:** `OreSpawnMain.java:6518`, `Scorpion.java:46,52` — HP 15, atk 4, def 10, speed 0.2, xp 10
- **Port:** `entity\EntityScorpion.java:50-56` — HP 20, atk 4, no armor
- **Fix:** set MAX_HEALTH 15, ARMOR 10.
- **Resolution:** FIXED (2026-06-11, Phase B — see FIX_LOG.md and phase_b_reports/B2_mobstats.md)

### ENT-S-002 — Scorpion: creeper/spider/raptor targeting dropped
- **Status:** PARTIAL
- **Original:** ORIG `Scorpion.java:203-253` — targets creepers, spiders, VelocityRaptor (not other Monsters)
- **Port:** `entity\EntityScorpion.java:34-48` — HurtBy + NearestAttackableTarget(Player) only
- **Fix:** add `NearestAttackableTargetGoal` entries for Creeper, Spider, VelocityRaptor.

### ENT-S-003 — Scorpion: attack sound + cactus immunity missing
- **Status:** PARTIAL
- **Original:** ORIG `Scorpion.java:182-201` — 1/3 chance `orespawn:scorpion_attack` on melee; cactus-immune
- **Port:** `entity\EntityScorpion.java` — neither present
- **Fix:** play `scorpion_attack` 1/3 in `doHurtTarget`; add cactus to `isInvulnerableTo`.

### ENT-S-004 — Scorpion: nugget drops replaced with bone
- **Status:** DIVERGENT
- **Original:** ORIG `Scorpion.java:148-160` — 1/10 each: gold nugget / uranium nugget / titanium nugget
- **Port:** `LT scorpion.json` — bone 1–3 (+looting)
- **Fix:** rewrite `scorpion.json`: three 10%-chance entries for gold/uranium/titanium nuggets.
- **Resolution:** FIXED (2026-06-11, Phase C — see FIX_LOG.md and phase_c_reports/C4_entities_S_Z.md)

### ENT-S-005 — Scorpion: biome coverage shrunk
- **Status:** PARTIAL
- **Original:** `OreSpawnMain.java:4901-4907` — desert 15(3-6), roofedForest 28(2-4), savanna 15(3-5), savPlateau 15(2-4), mesa 6/4/5; + spawner/darkness/y<50 gate (`Scorpion.java:281-299`)
- **Port:** `BM hostile_scorpion__direct/is_badlands/is_savanna` — w15 (2-4) only
- **Fix:** add a dark-forest BM entry w28(2-4); restore desert group 3-6 and mesa weights 4–6; gates per ENT-SYS2-004.

---

## SeaMonster

### ENT-S-006 — SeaMonster: stats up + water speed-boost dead code
- **Status:** DIVERGENT
- **Original:** `OreSpawnMain.java:6493` — HP 110, atk 14, def 8; speed 0.25 land / 0.55 in water
- **Port:** `SeaMonster.java:38-40,86` — HP 150, atk 15; `dynamicMoveSpeed` computed but never applied to the attribute → water speed-up inert
- **Fix:** set MAX_HEALTH 110, ATTACK_DAMAGE 14, ARMOR 8; in `aiStep`, write `dynamicMoveSpeed` into `Attributes.MOVEMENT_SPEED` (0.55 in water).
- **Resolution:** FIXED (2026-06-11, Phase C — remainder closed in Phase C; see FIX_LOG.md and phase_c_reports/C4_entities_S_Z.md)

### ENT-S-007 — SeaMonster: double drops + additions, gear unenchanted
- **Status:** DIVERGENT
- **Original:** ORIG — fish ×(9-14), SeaMonsterScale, chance of ENCHANTED iron tools/armor
- **Port:** `LT sea_monster.json` (scale + name_tag + cod 9–14 + 1/20 gear pool) PLUS code `SeaMonster.java:207-212` (heart_of_the_sea + 9–14 cod + 1/3 diamond)
- **Fix:** delete the code path (ENT-SYS2-001); remove name_tag/heart_of_the_sea; add `enchant_randomly` to the gear pool entries.
- **Resolution:** FIXED (2026-06-11, Phase B — see FIX_LOG.md and phase_b_reports/B1_drops.md)

### ENT-S-008 — SeaMonster: spawn weight 4→1, swamp dropped
- **Status:** DIVERGENT
- **Original:** `OreSpawnMain.java:4850-4851` — waterCreature ocean w4, swamp w2
- **Port:** `BM add_ocean_spawns` — w1 (1-1)
- **Fix:** raise ocean weight to 4; add swamp entry w2.
- **Resolution:** PARTIAL (2026-06-11, Phase C — weights/biomes JSON half fixed; spawn-rule gates → ENT-SYS-002 (Phase D); see FIX_LOG.md and phase_c_reports/C4_entities_S_Z.md)

---

## SeaViper

### ENT-S-009 — SeaViper: stats halved
- **Status:** DIVERGENT
- **Original:** `OreSpawnMain.java:6494` — HP 160, atk 22, def 12
- **Port:** `SeaViper.java:46-49` — HP 120, atk 12, no armor
- **Fix:** set MAX_HEALTH 160, ATTACK_DAMAGE 22, ARMOR 12.
- **Resolution:** FIXED (2026-06-11, Phase B — see FIX_LOG.md and phase_b_reports/B2_mobstats.md)

### ENT-S-010 — SeaViper: Poison on-hit replaced by Hunger
- **Status:** DIVERGENT
- **Original:** ORIG `SeaViper.java` — melee applies Poison
- **Port:** `entity\ai\SeaViperBiteGoal.java:22-26` — Hunger 8 s, 1/2 roll
- **Fix:** change the effect in `SeaViperBiteGoal` to `MobEffects.POISON` (keep duration/roll).
- **Resolution:** FIXED (2026-06-11, Phase C — see FIX_LOG.md and phase_c_reports/C4_entities_S_Z.md)

### ENT-S-011 — SeaViper: double drops + fish inflation
- **Status:** DIVERGENT
- **Original:** ORIG — fish, SeaViperTongue, enchanted-gear chances
- **Port:** `LT sea_viper.json` (tongue + name_tag + cooked_cod 9–14 + cod 9–14 + gear) PLUS code `SeaViper.java:254-261` (heart_of_the_sea + 9–14 × cod+salmon)
- **Fix:** delete the code path (ENT-SYS2-001); single fish pool 9–14; drop name_tag; enchant the gear pool.
- **Resolution:** FIXED (2026-06-11, Phase B — see FIX_LOG.md and phase_b_reports/B1_drops.md)

---

## Shoes (projectile)

### ENT-S-012 — Shoes: special-target damage cases missing
- **Status:** PARTIAL
- **Original:** ORIG `Shoes.java:57-78` — damage clamped to 1.0 vs Girlfriend/Boyfriend; 10 on valentines_day
- **Port:** port `Shoes.java:22-57` — 2.0 / 6.0 heavy / +4 creeper / 0 player only
- **Fix:** add the Girlfriend/Boyfriend 1.0 clamp (if those entities are ported) and the valentines-day (Feb 14 system date) 10-damage override.

---

## Skate

### ENT-S-013 — Skate: stats swapped
- **Status:** DIVERGENT
- **Original:** `OreSpawnMain.java:6519` — HP 8, atk 8, def 4
- **Port:** `Skate.java:35-37` — HP 15, atk 4
- **Fix:** set MAX_HEALTH 8, ATTACK_DAMAGE 8, ARMOR 4.
- **Resolution:** FIXED (2026-06-11, Phase B — see FIX_LOG.md and phase_b_reports/B2_mobstats.md)

### ENT-S-014 — Skate: drop changed
- **Status:** DIVERGENT
- **Original:** ORIG `Skate.java` `func_146068_u` — raw fish
- **Port:** `LT skate.json` — prismarine_shard 1–3
- **Fix:** rewrite `skate.json` to cod ×1.
- **Resolution:** FIXED (2026-06-11, Phase C — see FIX_LOG.md and phase_c_reports/C4_entities_S_Z.md)

### ENT-S-015 — Skate: spawn domain moved to vanilla oceans
- **Status:** DIVERGENT
- **Original:** `BiomeGenUtopianPlains.java:259` — Island/Crystal dims waterCreature w2 (3-6)
- **Port:** `BM add_ocean_spawns` w6 (1-2); gates (y≥50, 1/30, ≤6 nearby) ported (`Skate.java:182-187`)
- **Fix:** if Island/Crystal dims exist in port, move skate spawns there at w2 (3-6); else reduce ocean weight to 2, group 3-6, and document the domain substitution.
- **Resolution:** PARTIAL (2026-06-11, Phase C — weights/biomes JSON half fixed; spawn-rule gates → ENT-SYS-002 (Phase D); see FIX_LOG.md and phase_c_reports/C4_entities_S_Z.md)

---

## Slice (item)

### ENT-S-016 — Slice: enchantment identity unverified
- **Status:** UNVERIFIED
- **Original:** ORIG `Slice.java:33-43` — applies `Enchantment.field_77338_j` lvl 5 + `field_77336_l` lvl 1 (obfuscated ids)
- **Port:** port `Slice.java:23-28` — Sharpness 5 + Bane of Arthropods 1 in `inventoryTick`; levels (5/1) match
- **Fix:** verification failed because the 1.7.10 obfuscated enchantment fields were not mapped. Resolve by consulting MCP 1.7.10 mappings for `field_77338_j`/`field_77336_l`; if they map to something other than Sharpness/Bane, swap the port enchantments accordingly.

---

## SpiderDriver

### ENT-S-017 — SpiderDriver: mounted armor bonus missing
- **Status:** PARTIAL
- **Original:** ORIG `SpiderDriver.java:96+` — higher armor while mounted
- **Port:** port `SpiderDriver.java:39-41` — plain `Spider.createAttributes()`, no mounted bonus
- **Fix:** add/remove an ARMOR attribute modifier when mounting/dismounting a SpiderRobot.
- **Resolution:** FIXED (2026-06-11, Phase B — see FIX_LOG.md and phase_b_reports/B3_riders.md)

### ENT-S-018 — SpiderDriver: never attacks from the mount
- **Status:** PARTIAL
- **Original:** ORIG — attacks nearby mobs from the mounted SpiderRobot
- **Port:** port `SpiderDriver.java:67-72` — when mounted only *looks at* targets 1/4
- **Fix:** in the mounted branch, call `doHurtTarget` on targets within reach instead of only `getLookControl().setLookAt`.
- **Resolution:** FIXED (2026-06-11, Phase B — see FIX_LOG.md and phase_b_reports/B3_riders.md)

### ENT-S-019 — SpiderDriver: melee + poison missing entirely
- **Status:** MISSING
- **Original:** ORIG `SpiderDriver.java:89-92` — melee + Poison 60 ticks, 1/2 roll
- **Port:** no `doHurtTarget` path, no poison
- **Fix:** override `doHurtTarget`: vanilla spider damage + 1/2 chance Poison (60 ticks). (Natural spawn: see ENT-SYS2-003.)
- **Resolution:** FIXED (2026-06-11, Phase B — see FIX_LOG.md and phase_b_reports/B3_riders.md)

---

## SpiderRobot

### ENT-S-020 — SpiderRobot: stats third-ed
- **Status:** DIVERGENT
- **Original:** `OreSpawnMain.java:6474` — HP 1500, atk 100, def 16
- **Port:** port `SpiderRobot.java:68-74,161` — HP 500, atk 50 (hardcoded 50.0f in `doHurtTarget`), armor 8
- **Fix:** set MAX_HEALTH 1500, ATTACK_DAMAGE 100 (and the hardcoded 50.0f → 100.0f), ARMOR 16.
- **Resolution:** FIXED (2026-06-11, Phase B — see FIX_LOG.md and phase_b_reports/B2_mobstats.md)

### ENT-S-021 — SpiderRobot: frontal flame attack missing, stomp genericized
- **Status:** PARTIAL
- **Original:** ORIG — stomp attack (`feetFindSomethingToHit`) + frontal flame/spark attack; leg IK
- **Port:** port `SpiderRobot.java:124-135` — when ridden, generic auto-attack within 12 blocks 1/15 tick; no flame attack; procedural sine walk instead of IK (`:221-237`)
- **Fix:** add the frontal flame/spark attack (particles + fire damage cone ahead); constrain melee to stomp range under the feet instead of 12 blocks.

### ENT-S-022 — SpiderRobot: boss bar added (not in original)
- **Status:** PARTIAL
- **Original:** ORIG — no boss bar (HUD overlay only)
- **Port:** port `SpiderRobot.java:49-50,86-99` — `ServerBossEvent` added alongside ported HUD
- **Fix:** decide: remove the boss bar for fidelity, or keep and document as intentional addition (a rideable vehicle with a boss bar is misleading).

### ENT-S-023 — SpiderRobot: drops changed
- **Status:** DIVERGENT
- **Original:** ORIG `func_70628_a` — various blocks/items
- **Port:** `LT spider_robot.json` — iron 3–8 + string 2–5
- **Fix:** read ORIG `SpiderRobot.java` drop method and port the block/item list into `spider_robot.json`.
- **Resolution:** FIXED (2026-06-11, Phase C — see FIX_LOG.md and phase_c_reports/C4_entities_S_Z.md)

---

## SpitBug

### ENT-S-024 — SpitBug: stats reduced
- **Status:** DIVERGENT
- **Original:** `OreSpawnMain.java:6490` — HP 100, atk 10, def 12
- **Port:** port `EntitySpitBug.java:61-67` — HP 60, atk 8, no armor
- **Fix:** set MAX_HEALTH 100, ATTACK_DAMAGE 10, ARMOR 12.
- **Resolution:** FIXED (2026-06-11, Phase B — see FIX_LOG.md and phase_b_reports/B2_mobstats.md)

### ENT-S-025 — SpitBug: cactus + fall immunity missing
- **Status:** MISSING
- **Original:** ORIG `SpitBug.java` — cactus- and fall-immune
- **Port:** neither; only 15-tick i-frames in `hurt()` (`:142-153`)
- **Fix:** add CACTUS to `isInvulnerableTo` and override `causeFallDamage` to return false.

### ENT-S-026 — SpitBug: drops changed
- **Status:** DIVERGENT
- **Original:** ORIG — amethyst nuggets
- **Port:** `LT spit_bug.json` — slime_ball 1–3
- **Fix:** rewrite `spit_bug.json` to amethyst nugget drops (count per ORIG `SpitBug.java` drop method).
- **Resolution:** FIXED (2026-06-11, Phase C — see FIX_LOG.md and phase_c_reports/C4_entities_S_Z.md)

---

## Spyro

### ENT-S-027 — Spyro: Dragon evolution, untame, rename missing; extinguisher changed
- **Status:** PARTIAL
- **Original:** ORIG `Spyro.java:250-325` — dead-bush untame; ice block turns fireballs OFF; diamond → evolves into tamed Dragon; name-tag rename
- **Port:** port `EntitySpyro.java:179-210` — beef tame + flint&steel ON ported; OFF via water bucket; evolution/untame/rename absent
- **Fix:** add diamond-interaction → replace with tamed Dragon entity (copy owner/name); dead-bush untame; name-tag rename; switch extinguisher from water bucket to ice block.

### ENT-S-028 — Spyro: drop changed
- **Status:** DIVERGENT
- **Original:** ORIG `func_146068_u` — apple on death
- **Port:** `LT spyro.json` — blaze_powder 1–3
- **Fix:** rewrite `spyro.json` to apple ×1.
- **Resolution:** FIXED (2026-06-11, Phase C — see FIX_LOG.md and phase_c_reports/C4_entities_S_Z.md)

### ENT-S-029 — Spyro: spawn domain moved
- **Status:** DIVERGENT
- **Original:** Island/Crystal/Mining dims (`BiomeGenUtopianPlains`; mining w1)
- **Port:** `BM companion_spyro__is_badlands/is_mountain` w1 (1-1)
- **Fix:** if the custom dims exist in port, add Spyro to their spawn lists at w1; otherwise keep substitute biomes and document.
- **Resolution:** PARTIAL (2026-06-11, Phase C — weights/biomes JSON half fixed; spawn-rule gates → ENT-SYS-002 (Phase D); see FIX_LOG.md and phase_c_reports/C4_entities_S_Z.md)

---

## StinkBug

### ENT-S-030 — StinkBug: death gas Poison → Hunger
- **Status:** DIVERGENT
- **Original:** ORIG `StinkBug.java` — Poison to entities within ~8 blocks on death
- **Port:** port `EntityStinkBug.java:82-95` — Hunger 300t in 8×5×8
- **Fix:** change the effect to `MobEffects.POISON` (keep radius/duration).
- **Resolution:** FIXED (2026-06-11, Phase C — see FIX_LOG.md and phase_c_reports/C4_entities_S_Z.md)

### ENT-S-031 — StinkBug: food changed
- **Status:** DIVERGENT
- **Original:** ORIG — fish + CrystalApple
- **Port:** port `EntityStinkBug.java:107-109` — apple
- **Fix:** change `isFood` to cod + crystal_apple (ModItems).
- **Resolution:** FIXED (2026-06-11, Phase C — see FIX_LOG.md and phase_c_reports/C4_entities_S_Z.md)

### ENT-S-032 — StinkBug: spawn weights flattened
- **Status:** PARTIAL
- **Original:** `OreSpawnMain.java:4894-4898` — forest 10(2-4), jungle 8(2-4), taigaHills 6(2-4), jungleHills 4(2-4), savanna 8(2-5); chaos dim w3
- **Port:** `BM swarm_stink_bug` — forest-group/jungle/taiga all w8 (2-4)
- **Fix:** split per-biome weights: forest 10, jungle 8, taiga 6, savanna 8(2-5).

---

## Stinky

### ENT-S-033 — Stinky: item-production economy rewritten
- **Status:** DIVERGENT
- **Original:** ORIG `Stinky.java:337-396` — front burp: coal; rear drop by 19 skin variants: blaze powder, rotten flesh, melon seeds, uranium nugget, wheat, reeds, torch, emerald, gold ingot, leaves, titanium nugget, apple seed, diamond, sand, cobble, bone, string, cherry seed, peach seed
- **Port:** port `EntityStinky.java:153-155,396-420` — front: bone 1/1750; rear 1/2000: diamond, chicken, iron, gold nugget, cookie, cake, flower pot, poisonous potato, gold ingot, sand, copper, apple, emerald, gravel, cobble, name tag, iron pickaxe, berries, melon
- **Fix:** restore front burp = coal; map the rear 19-skin list back to the original items (substituting ported analogs for uranium/titanium nuggets and seeds).
- **Resolution:** FIXED (2026-06-11, Phase C — see FIX_LOG.md and phase_c_reports/C4_entities_S_Z.md)

### ENT-S-034 — Stinky: death drop missing
- **Status:** MISSING
- **Original:** ORIG `Stinky.java:254,263` — beef on death
- **Port:** `LT stinky.json` — empty
- **Fix:** add beef ×1 to `stinky.json`.

### ENT-S-035 — Stinky: Nether spawn dropped
- **Status:** DIVERGENT
- **Original:** `OreSpawnMain.java:4805-4808` — hell monster w2; mesa-variant ambient w1 ×3; island dim w2
- **Port:** `BM companion_stinky` forest/taiga w1 + dim_islands w2
- **Fix:** add a Nether BM entry w2 and mesa/badlands entries w1.
- **Resolution:** PARTIAL (2026-06-11, Phase C — weights/biomes JSON half fixed; spawn-rule gates → ENT-SYS-002 (Phase D); see FIX_LOG.md and phase_c_reports/C4_entities_S_Z.md)

---

## SunspotUrchin (projectile)

### ENT-S-036 — SunspotUrchin: fire placement on block impact missing
- **Status:** MISSING
- **Original:** ORIG — places `Blocks.fire` at impact point
- **Port:** port `:48-58` — smoke particles + discard only
- **Fix:** in `onHitBlock`, place `Blocks.FIRE` at the hit face position (mobGriefing-gated).

---

## Termite

### ENT-S-037 — Termite: spawn pathway unverified
- **Status:** UNVERIFIED
- **Original:** ORIG — no `addSpawn`; nest/structure-driven spawning
- **Port:** no biome modifier; config toggle `ModSpawnControl.java:59/68`; cluster gate ≤4 in 20×10×20, y≥50 (`:218-223`)
- **Fix:** verification failed because neither side's structure/nest spawn data was checked in the audit slice. Resolve by reading the port's structure/feature code (e.g. termite-nest worldgen or block tick spawners) and ORIG nest block classes; confirm termites still appear in-world, else wire a nest spawn.

---

## TerribleTerror

### ENT-S-038 — TerribleTerror: HP doubled, armor lost
- **Status:** DIVERGENT
- **Original:** `OreSpawnMain.java:6520` — HP 10, atk 5, def 3
- **Port:** port `EntityTerribleTerror.java:39-44` — HP 20, atk 5, no armor
- **Fix:** set MAX_HEALTH 10, ARMOR 3.
- **Resolution:** FIXED (2026-06-11, Phase B — see FIX_LOG.md and phase_b_reports/B2_mobstats.md)

### ENT-S-039 — TerribleTerror: emerald drop gone
- **Status:** DIVERGENT
- **Original:** ORIG `TerribleTerror.java:313-322` — 1/3 each: rotten flesh / emerald / feather
- **Port:** LT — bone 1–2 + leather 0–1 + feather 0–1
- **Fix:** rewrite loot table: three independent 1/3 entries for rotten_flesh, emerald, feather.
- **Resolution:** FIXED (2026-06-11, Phase C — see FIX_LOG.md and phase_c_reports/C4_entities_S_Z.md)

### ENT-S-040 — TerribleTerror: spawn domain moved to vanilla overworld
- **Status:** DIVERGENT
- **Original:** `BiomeGenUtopianPlains.java:182,412` — Island dim monster w25 (3-6); chaos w4 (2-6)
- **Port:** `BM add_overworld_monsters` w4 (1-2)
- **Fix:** if Island/Chaos dims exist in port, move spawns there (w25/3-6, w4/2-6); else document overworld substitution and consider group 2-6.
- **Resolution:** PARTIAL (2026-06-11, Phase C — weights/biomes JSON half fixed; spawn-rule gates → ENT-SYS-002 (Phase D); see FIX_LOG.md and phase_c_reports/C4_entities_S_Z.md)

---

## ThunderBolt (projectile)

### ENT-S-041 — ThunderBolt: royalty exemption missing
- **Status:** PARTIAL
- **Original:** ORIG — 40 dmg, ignores "royalty" entities (King/Queen family)
- **Port:** port `:19,46-59` — 40 total (2×20 split), ignite 1 s; no royalty exemption (orig explosion power unverified)
- **Fix:** skip damage when the hit entity implements the port's royalty marker (TheKing/TheQueen/Princes/Princess) so boss self-fire doesn't hurt peers.

---

## TRex

### ENT-S-042 — TRex: stats buffed, armor lost
- **Status:** DIVERGENT
- **Original:** `OreSpawnMain.java:6479` — HP 160, atk 22, def 14
- **Port:** port `TRex.java:57-64` — HP 200, atk 30, no armor
- **Fix:** set MAX_HEALTH 160, ATTACK_DAMAGE 22, ARMOR 14.
- **Resolution:** FIXED (2026-06-11, Phase B — see FIX_LOG.md and phase_b_reports/B2_mobstats.md)

### ENT-S-043 — TRex: loot identity changed
- **Status:** DIVERGENT
- **Original:** ORIG — TRexTooth, bone, UraniumNugget, TitaniumNugget
- **Port:** `LT trex.json` — tooth (60/30/10 ×1/2/3) + name_tag + 7 beef + gold/iron nuggets 2–5 + xp bottle + diamond 4–7; code adds bone (`:131-135`)
- **Fix:** rewrite `trex.json`: tooth + bone + uranium nugget + titanium nugget; delete name_tag/beef/diamond/xp; drop the code bone path (ENT-SYS2-001).
- **Resolution:** FIXED (2026-06-11, Phase B — see FIX_LOG.md and phase_b_reports/B1_drops.md)

### ENT-S-044 — TRex: spawn domain moved overworld-wide
- **Status:** DIVERGENT
- **Original:** no overworld `addSpawn`; Island/Crystal w1, Mining dim (`BiomeGenUtopianPlains.java:496`)
- **Port:** `BM` trex badlands+savanna w1 (1-1) AND `add_overworld_monsters` w1
- **Fix:** remove TRex from `add_overworld_monsters`; keep (or dim-gate) the badlands/savanna entries.
- **Resolution:** PARTIAL (2026-06-11, Phase C — weights/biomes JSON half fixed; spawn-rule gates → ENT-SYS-002 (Phase D); see FIX_LOG.md and phase_c_reports/C4_entities_S_Z.md)

### ENT-S-045 — TRex: custom sounds replaced with ravager
- **Status:** DIVERGENT
- **Original:** ORIG `:98-108` — `trex_living` / `alo_hurt` / `trex_death`
- **Port:** port `:88-103` — RAVAGER_ROAR / RAVAGER_HURT / RAVAGER_DEATH
- **Fix:** use `ModSounds.TREX_LIVING/ALO_HURT/TREX_DEATH` (trex_death already registered — used by TheKing).
- **Resolution:** FIXED (2026-06-11, Phase C — see FIX_LOG.md and phase_c_reports/C4_entities_S_Z.md)

---

## Triffid

### ENT-S-046 — Triffid: attack third-ed, armor lost
- **Status:** DIVERGENT
- **Original:** `OreSpawnMain.java:6502` — HP 100, atk 20, def 12
- **Port:** port `EntityTriffid.java:52-57` — HP 100, atk 8, no armor
- **Fix:** set ATTACK_DAMAGE 20, ARMOR 12.
- **Resolution:** FIXED (2026-06-11, Phase B — see FIX_LOG.md and phase_b_reports/B2_mobstats.md)

### ENT-S-047 — Triffid: cactus + fall immunity missing
- **Status:** MISSING
- **Original:** ORIG — cactus- and fall-immune
- **Port:** no hurt-source filter, no fall override
- **Fix:** add CACTUS to `isInvulnerableTo`; override `causeFallDamage` → false.

### ENT-S-048 — Triffid: drop composition changed
- **Status:** PARTIAL
- **Original:** ORIG — GreenGoo, bone
- **Port:** `LT triffid.json` — green_goo 4–9 + name_tag + vine 2–5; code 1/3 poisonous potato (`:228-233`)
- **Fix:** keep green_goo, add bone; remove name_tag/vine and the code potato path (ENT-SYS2-001).
- **Resolution:** FIXED (2026-06-11, Phase B — see FIX_LOG.md and phase_b_reports/B1_drops.md)

### ENT-S-049 — Triffid: overworld spawn added vs unverified original
- **Status:** UNVERIFIED
- **Original:** no `addSpawn` found in decompile (presumed spawner/dim driven)
- **Port:** `BM add_overworld_monsters` w4 (1-2)
- **Fix:** verification failed because the original spawn source wasn't located. Resolve by grepping ORIG `OreSpawnMain.java`/`BiomeGenUtopianPlains.java` for "Triffid" spawn registrations; if truly absent, the port's w4 overworld spawn is an addition to be removed or config-gated.

### ENT-S-050 — Triffid: shell-lockout duration unverified
- **Status:** UNVERIFIED
- **Original:** ORIG — DataWatcher 21 OpenClosed; no-damage-while-closed; original lockout timer not read
- **Port:** port `:139-182` — 300-tick hurt lockout while closed, open rolls 1/80→1/8
- **Fix:** verification failed because the ORIG timer constant wasn't extracted. Resolve by reading ORIG `Triffid.java` OpenClosed timer logic and matching the port's 300-tick value to it.

---

## TrooperBug

### ENT-S-051 — TrooperBug: attack reduced, armor lost
- **Status:** DIVERGENT
- **Original:** `OreSpawnMain.java:6489` — HP 200, atk 20, def 15
- **Port:** port `EntityTrooperBug.java:64-70` — HP 200, atk 16, no armor
- **Fix:** set ATTACK_DAMAGE 20, ARMOR 15.
- **Resolution:** FIXED (2026-06-11, Phase B — see FIX_LOG.md and phase_b_reports/B2_mobstats.md)

### ENT-S-052 — TrooperBug: SpitBug minion summon missing
- **Status:** PARTIAL
- **Original:** ORIG — spawns SpitBugs when attacking
- **Port:** leap ported (`TrooperBugLeapAttackGoal.java:21-42`); no minion summon
- **Fix:** on attack start (or hurt), spawn 1–2 EntitySpitBug near the TrooperBug, matching original cadence (read ORIG `TrooperBug.java` for exact roll).

### ENT-S-053 — TrooperBug: cactus/fall immunity missing
- **Status:** PARTIAL
- **Original:** ORIG — cactus- and fall-immune
- **Port:** 20-tick i-frames only (`:139-155`)
- **Fix:** add CACTUS to `isInvulnerableTo`; override `causeFallDamage` → false.

### ENT-S-054 — TrooperBug: gear unenchanted, bone missing, double name_tag
- **Status:** PARTIAL
- **Original:** ORIG — MyJumpyBugScale, bone, MyAmethyst, ENCHANTED Amethyst tools/armor
- **Port:** `LT trooper_bug.json` — scale + name_tag + amethyst_gem 2–6 + amethyst gear (unenchanted); code adds name_tag again (`:180-184`)
- **Fix:** add `enchant_randomly` to gear entries; add bone; remove name_tags and the code path (ENT-SYS2-001).
- **Resolution:** FIXED (2026-06-11, Phase B — see FIX_LOG.md and phase_b_reports/B1_drops.md)

---

## Tshirt

### ENT-S-055 — Tshirt: drop string → leather
- **Status:** DIVERGENT
- **Original:** ORIG — string
- **Port:** code drop leather (`EntityTshirt.java:58-62`); loot table empty
- **Fix:** change the code drop to `Items.STRING` (or move to the loot table and delete the code drop).
- **Resolution:** FIXED (2026-06-11, Phase B — see FIX_LOG.md and phase_b_reports/B1_drops.md)

### ENT-S-056 — Tshirt: night/no-buddy spawn gates absent
- **Status:** PARTIAL
- **Original:** `BiomeGenUtopianPlains.java:324` — Village dim w2 (1-1), night-only + no other Tshirts nearby
- **Port:** `BM dim_village_locals` w2 (1-1), no gates
- **Fix:** add `checkSpawnRules`: night-time + no Tshirt within range (see ENT-SYS2-004).

---

## UltimateArrow (projectile)

### ENT-S-057 — UltimateArrow: config-scaled damage replaced by flat 12
- **Status:** DIVERGENT
- **Original:** `OreSpawnMain.java:1519-1529` — `UltimateBowDamage` config (default 10, clamp 2–20) × arrow velocity
- **Port:** port `UltimateArrow.java:12-21` — flat `setBaseDamage(12.0)`
- **Fix:** add an `ULTIMATE_BOW_DAMAGE` config (default 10, clamp 2–20) and use it as base damage.
- **Resolution:** FIXED (2026-06-11, Phase C — see FIX_LOG.md and phase_c_reports/C4_entities_S_Z.md)

### ENT-S-058 — UltimateArrow: ignite, knockback, tame-exempt, trail particles missing
- **Status:** MISSING
- **Original:** ORIG — ignites targets, custom knockback, skips player-owned tameables, trail particles
- **Port:** none of the four behaviors
- **Fix:** port `onHitEntity`: ignite + extra knockback, early-return for `TamableAnimal` with owner; add a per-tick particle trail.

---

## UltimateFishHook

### ENT-S-059 — UltimateFishHook: custom fish pools + lava fishing missing
- **Status:** MISSING
- **Original:** ORIG `UltimateFishHook.java:422-449` — weighted pools incl. `orespawn_fish` (BlueFish, PinkFish, RockFish, WoodFish, GreyFish) and `orespawn_lava_fish` (SunspotUrchin, LavaEel, SunFish, SparkFish, FireFish) when fishing in lava (`:431-434`)
- **Port:** port `UltimateFishHook.java:9-17` — vanilla `FishingHook` subclass, vanilla loot only; no lava support
- **Fix:** override the retrieve/loot logic: use a custom loot table including the five orespawn fish; detect lava at hook position and switch to the lava-fish table; make the hook lava-proof (`fireImmune`).

### ENT-S-060 — UltimateFishHook: custom wait timers and reel-pull missing
- **Status:** PARTIAL
- **Original:** ORIG `:384-420` — custom wait timers, reel-from-distance pull
- **Port:** luck+3 / lure+2 only (`:14-16`; rod self-enchants, `UltimateFishingRod.java:24-29`)
- **Fix:** port the shortened wait timers and the long-distance reel pull into the hook subclass.

---

## Urchin

### ENT-S-061 — Urchin: stats changed, fire immunity missing
- **Status:** DIVERGENT
- **Original:** `OreSpawnMain.java:6484` — HP 25, atk 10, def 4; fire-immune (`field_70178_ae=true`)
- **Port:** port `Urchin.java:33-35` — HP 30, atk 8, not fire-immune
- **Fix:** set MAX_HEALTH 25, ATTACK_DAMAGE 10, ARMOR 4; add `fireImmune()` override.
- **Resolution:** FIXED (2026-06-11, Phase C — remainder closed in Phase C; see FIX_LOG.md and phase_c_reports/C4_entities_S_Z.md)

### ENT-S-062 — Urchin: spawn domain moved to vanilla oceans
- **Status:** DIVERGENT
- **Original:** Island w15 (2-4) / Crystal w2 (1-5) dims; night spawner
- **Port:** `BM add_ocean_spawns` w6 (1-2); rules time≥13000 (`:168-171`)
- **Fix:** if Island/Crystal dims exist in port, move spawns there at original weights; else document the ocean substitution.
- **Resolution:** PARTIAL (2026-06-11, Phase C — weights/biomes JSON half fixed; spawn-rule gates → ENT-SYS-002 (Phase D); see FIX_LOG.md and phase_c_reports/C4_entities_S_Z.md)

---

## VelocityRaptor

### ENT-S-063 — VelocityRaptor: tamed HP bump missing
- **Status:** PARTIAL
- **Original:** ORIG — HP 10 wild, 20 when tamed
- **Port:** port `VelocityRaptor.java:73-78` — HP 10 always
- **Fix:** on successful tame, set MAX_HEALTH base to 20 and heal to full.
- **Resolution:** FIXED (2026-06-11, Phase B — see FIX_LOG.md and phase_b_reports/B2_mobstats.md)

### ENT-S-064 — VelocityRaptor: untame + rename missing
- **Status:** PARTIAL
- **Original:** ORIG `:264,282` — dead-bush untame; name-tag rename
- **Port:** apple tame ported (`:151-165`); untame/rename absent
- **Fix:** add dead-bush untame interaction and name-tag rename handling.

### ENT-S-065 — VelocityRaptor: riding is a port invention
- **Status:** DIVERGENT
- **Original:** ORIG — NOT rideable in 1.7.10 (EntityCannonFodder tameable)
- **Port:** port `:188-227` — fully rideable (`getControllingPassenger`/`tickRidden`/speed ×1.6)
- **Fix:** decide: remove riding for fidelity, or keep as documented enhancement (config-gate if keeping).
- **Resolution:** FIXED (2026-06-11, Phase C — see FIX_LOG.md and phase_c_reports/C4_entities_S_Z.md)

### ENT-S-066 — VelocityRaptor: drop changed
- **Status:** DIVERGENT
- **Original:** ORIG `:335` — poppy
- **Port:** `LT velocity_raptor.json` — bone 1–3
- **Fix:** rewrite to poppy ×1.
- **Resolution:** FIXED (2026-06-11, Phase C — see FIX_LOG.md and phase_c_reports/C4_entities_S_Z.md)

### ENT-S-067 — VelocityRaptor: spawn domain moved
- **Status:** DIVERGENT
- **Original:** Island/Crystal/Mining dims (jungle addSpawn not found)
- **Port:** `BM companion_velocity_raptor` jungle/savanna w2 (1-2) + `add_overworld_creatures` w4 (1-2); rules y≥50 + sky (`:261-264`)
- **Fix:** remove from `add_overworld_creatures` (keep themed jungle/savanna entries); add custom-dim entries if those dims exist.
- **Resolution:** PARTIAL (2026-06-11, Phase C — weights/biomes JSON half fixed; spawn-rule gates → ENT-SYS-002 (Phase D); see FIX_LOG.md and phase_c_reports/C4_entities_S_Z.md)

---

## Vortex

### ENT-S-068 — Vortex: stats changed, fire immunity missing
- **Status:** DIVERGENT
- **Original:** `OreSpawnMain.java:6500` — HP 150, atk 26, def 10; fire-immune
- **Port:** port `EntityVortex.java:60-65` — HP 200, atk 20, no armor, not fire-immune
- **Fix:** set MAX_HEALTH 150, ATTACK_DAMAGE 26, ARMOR 10; add `fireImmune()`.
- **Resolution:** FIXED (2026-06-11, Phase C — remainder closed in Phase C; see FIX_LOG.md and phase_c_reports/C4_entities_S_Z.md)

### ENT-S-069 — Vortex: invented skyward-launch attack
- **Status:** DIVERGENT
- **Original:** ORIG — melee 26 + drag only
- **Port:** port `:190-196,244-274` — melee plus new `skywardLaunch` (+4.0 up, 30t cooldown)
- **Fix:** remove `skywardLaunch` (or config-gate it); rely on the ported pull + melee.
- **Resolution:** FIXED (2026-06-11, Phase C — see FIX_LOG.md and phase_c_reports/C4_entities_S_Z.md)

### ENT-S-070 — Vortex: drops changed
- **Status:** DIVERGENT
- **Original:** ORIG — VortexEye, bone, ingots/gems
- **Port:** `LT vortex.json` — vortex_eye + xp bottle + gunpowder 3–8 + gold 1–3
- **Fix:** rewrite: vortex_eye + bone + the original ingot/gem pool; drop xp bottle/gunpowder.
- **Resolution:** FIXED (2026-06-11, Phase C — see FIX_LOG.md and phase_c_reports/C4_entities_S_Z.md)

### ENT-S-071 — Vortex: spawns in the wrong dimension
- **Status:** DIVERGENT
- **Original:** night overworld + Island w3 (1-2)/Crystal w1/Chaos dims (`BiomeGenUtopianPlains.java:226,406`)
- **Port:** `BM add_nether_spawns` w4 — Nether only
- **Fix:** remove vortex from `add_nether_spawns`; add overworld monster entry (night via `checkSpawnRules`, day-despawn already ported `:121-126`) + custom-dim entries if available.
- **Resolution:** PARTIAL (2026-06-11, Phase C — weights/biomes JSON half fixed; spawn-rule gates → ENT-SYS-002 (Phase D); see FIX_LOG.md and phase_c_reports/C4_entities_S_Z.md)

---

## WaterBall (projectile)

### ENT-S-072 — WaterBall: target exemptions + item drop missing
- **Status:** PARTIAL
- **Original:** ORIG — 0 dmg vs WaterDragon/AttackSquid; drops MyWaterBall item on hit
- **Port:** port `:44-52` — mounted-player skip ported; WaterDragon/AttackSquid exemption and item drop missing (fire-extinguish added)
- **Fix:** early-return 0 damage for WaterDragon/AttackSquid targets; spawn the water_ball item entity on hit.

---

## WaterDragon

### ENT-S-073 — WaterDragon: HP up, armor lost
- **Status:** DIVERGENT
- **Original:** `OreSpawnMain.java:6492` — HP 150, atk 20, def 8
- **Port:** port `WaterDragon.java:56-59` — HP 200, atk 20 (hardcoded), no armor
- **Fix:** set MAX_HEALTH 150, ARMOR 8.
- **Resolution:** FIXED (2026-06-11, Phase B — see FIX_LOG.md and phase_b_reports/B2_mobstats.md)

### ENT-S-074 — WaterDragon: ranged WaterBall + fireball volleys missing
- **Status:** PARTIAL
- **Original:** ORIG `WaterDragon.java:624-632` — melee + ranged WaterBall and EntitySmallFireball volleys
- **Port:** melee only via `DinosaurMeleeAttackGoal` (`:214-225`)
- **Fix:** add a ranged-attack goal firing WaterBall (and SmallFireball) volleys at the original cadence/range.

### ENT-S-075 — WaterDragon: double drops + ultimate tools added
- **Status:** DIVERGENT
- **Original:** ORIG — MyWaterDragonScale, bone, raw fish, enchanted tools/armor
- **Port:** `LT water_dragon.json` (scale + name_tag + amethyst 2–5 + cod 9–14 + ultimate/iron gear) PLUS code `:310-315` (heart_of_the_sea + 9–14 cod + 1/3 diamond)
- **Fix:** delete the code path (ENT-SYS2-001); remove ultimate-tools and name_tag from the JSON; add bone; enchant the gear pool.
- **Resolution:** FIXED (2026-06-11, Phase B — see FIX_LOG.md and phase_b_reports/B1_drops.md)

### ENT-S-076 — WaterDragon: river/swamp spawn weights lowered
- **Status:** PARTIAL
- **Original:** `OreSpawnMain.java:4844-4847` — river w5, swamp 3, ocean 2, deepOcean 2
- **Port:** `BM water_dragon` — all w2
- **Fix:** raise river to 5 and swamp to 3 in the BM JSONs.

---

## WormSmall

### ENT-S-077 — WormSmall: boot-stealing missing
- **Status:** PARTIAL
- **Original:** ORIG `WormSmall.java:179-197` — within 1.5: 1/15 swing; 1/6 chance to rip off boots, damage durability/20, throw on ground
- **Port:** port `:135-146` — 1/15 swing only
- **Fix:** on successful close-range hit, 1/6 roll: remove target's FEET item, damage it `maxDamage/20`, spawn as ItemEntity.

### ENT-S-078 — WormSmall: surface-block death check missing
- **Status:** MISSING
- **Original:** ORIG — dies if rising through non-grass/dirt/stone
- **Port:** rises through anything
- **Fix:** in the rise branch, check the block above; `discard()`/kill if it is not grass/dirt/stone.

### ENT-S-079 — WormSmall: drop added
- **Status:** DIVERGENT
- **Original:** ORIG — no drops
- **Port:** `LT worm_small.json` — dirt 0–2
- **Fix:** empty `worm_small.json` pools (minor).
- **Resolution:** FIXED (2026-06-11, Phase C — see FIX_LOG.md and phase_c_reports/C4_entities_S_Z.md)

### ENT-S-080 — WormSmall: natural daytime spawning added
- **Status:** DIVERGENT
- **Original:** ORIG — no `addSpawn`; only spawned by WormLarge; spawn rule = night only (`:214-216`)
- **Port:** `BM add_overworld_creatures` CREATURE w10 (1-2), ON_GROUND placement
- **Fix:** remove WormSmall from `add_overworld_creatures` (WormLarge already summons 20); if kept, add night-only `checkSpawnRules`.
- **Resolution:** FIXED (2026-06-11, Phase C — see FIX_LOG.md and phase_c_reports/C4_entities_S_Z.md)

---

## WormMedium

### ENT-S-081 — WormMedium: attack reduced, armor lost
- **Status:** DIVERGENT
- **Original:** `OreSpawnMain.java:6505` — HP 30, atk 10, def 8
- **Port:** port `EntityWormMedium.java:32-37` — HP 30, atk 6, no armor
- **Fix:** set ATTACK_DAMAGE 10, ARMOR 8.
- **Resolution:** FIXED (2026-06-11, Phase B — see FIX_LOG.md and phase_b_reports/B2_mobstats.md)

### ENT-S-082 — WormMedium: boots/leggings theft missing
- **Status:** PARTIAL
- **Original:** ORIG `:193-222` — steals boots or leggings, durability/15
- **Port:** port `:146-163` — swing only
- **Fix:** on hit, roll to strip FEET or LEGS slot, damage durability/15, drop as ItemEntity.

### ENT-S-083 — WormMedium: drops changed + doubled
- **Status:** DIVERGENT
- **Original:** ORIG `:256-273` — 2 rotten flesh + 2 leather
- **Port:** code 2 rotten flesh + 2 string (`:183-191`); loot table adds bone 1–2 + rotten 1–2
- **Fix:** single path: 2 rotten flesh + 2 leather; remove the extra table pools (ENT-SYS2-001).
- **Resolution:** FIXED (2026-06-11, Phase B — see FIX_LOG.md and phase_b_reports/B1_drops.md)

---

## WormLarge

### ENT-S-084 — WormLarge: stats changed
- **Status:** DIVERGENT
- **Original:** `OreSpawnMain.java:6506` — HP 90, atk 18, def 14
- **Port:** port `EntityWormLarge.java:46-51` — HP 100, atk 15, no armor
- **Fix:** set MAX_HEALTH 90, ATTACK_DAMAGE 18, ARMOR 14.
- **Resolution:** FIXED (2026-06-11, Phase B — see FIX_LOG.md and phase_b_reports/B2_mobstats.md)

### ENT-S-085 — WormLarge: helmet/chestplate/held-item theft missing
- **Status:** MISSING
- **Original:** ORIG `:206-239` — steals helmet/chestplate 1/4 and held item 1/4, durability/10
- **Port:** absent
- **Fix:** on melee hit, 1/4 roll each: strip HEAD or CHEST slot and MAINHAND, damage durability/10, drop as ItemEntity.

### ENT-S-086 — WormLarge: drops doubled + nether star/saddle invented
- **Status:** DIVERGENT
- **Original:** ORIG `:352-377` — WormTooth, painting, 6 rotten, 6 leather, 8 dirt, 16 gold nuggets, 5 diamond, 4 uranium nugget, 4 titanium nugget
- **Port:** `LT worm_large.json` (tooth + SADDLE + rotten 3–6 + leather 3–6 + dirt 4–8 + gold nuggets 8–16 + diamond 2–5 + uranium 2–4 + titanium 2–4) PLUS code (`:211-226`) NETHER STAR + 6 rotten + 6 string + 16 spider_eye + 5 diamond
- **Fix:** delete the code path entirely (nether star/spider eyes are inventions); remove saddle from the JSON; bump counts to original fixed values (ENT-SYS2-001).
- **Resolution:** FIXED (2026-06-11, Phase B — see FIX_LOG.md and phase_b_reports/B1_drops.md)

### ENT-S-087 — WormLarge: never spawns naturally
- **Status:** MISSING
- **Original:** `OreSpawnMain.java:4631-4633` — creature plains w25, savanna w15, savannaPlateau w10 (1-1) + ground-solidity/no-other-in-32/y≥50/spawner gates (`:263-309`)
- **Port:** no biome modifier entry (only ON_GROUND placement registered, `ModEntityAttributes.java:219`)
- **Fix:** create a BM JSON: plains w25, savanna w15, savanna plateau w10, group 1-1; add `checkSpawnRules` (solid ground, no WormLarge in 32, y≥50).

---

## TheKing

### BOSS-001 — TheKing: core stats nerfed (HP/ATK/armor)
- **Status:** DIVERGENT
- **Original:** `OreSpawnMain.java:6521`, ORIG `TheKing.java:104,106,851-865` — HP 7000, attack 350, armor 21 base (25 vs large entities, +1/+2/+3 phase bonuses)
- **Port:** `PORT\entity\TheKing.java:109-112,219-221,975-984` — HP 6000, attack 250, armor 12 base (25/+1/+2/+3 structure ported)
- **Fix:** set MAX_HEALTH 7000, ATTACK_DAMAGE 250→350, base armor 12→21 at `TheKing.java:109-111`. (Phase scaling and AoE damage inherit the correction automatically.)
- **Resolution:** FIXED (2026-06-11, Phase B — see FIX_LOG.md and phase_b_reports/B2_mobstats.md)

### BOSS-002 — TheKing: hitbox much smaller, no PlayNicely shrink
- **Status:** DIVERGENT
- **Original:** ORIG `TheKing.java:86-88` — 22×24 (5.5×6 if PlayNicely)
- **Port:** `ModEntities.java:180-182` — 6×12 parent + 5 parts (partial compensation)
- **Fix:** enlarge the parent dimensions and/or part AABBs to approximate the 22×24 envelope; implement PlayNicely shrink (see BOSS-017).
- **Resolution:** FIXED (2026-06-11, Phase C — parent resized 22×24 per orig TheKing.java:86; PlayNicely shrink stays with BOSS-017; see FIX_LOG.md and phase_c_reports/C5_bosses.md)

### BOSS-003 — TheKing: KingHead sidecar degraded
- **Status:** PARTIAL
- **Original:** ORIG `KingHead.java:33,42,69-89,147-157` — 19.9×10 sidecar; teleports to `(x−30·sin(yHeadRot), y+12, z+30·cos)`; forwards damage to parent
- **Port:** `PORT\entity\KingHead.java:61-63,107-111`, `ModEntities.java:581-583` — `@Deprecated` but still spawned by AI (`TheKing.java:765-772`); uses `yBodyRot` instead of `yHeadRot`; registered 3×3
- **Fix:** either stop spawning KingHead (rely on the 5-part system) or fix it: size 19.9×10 and offset from `yHeadRot`. Don't ship both half-working.

### BOSS-004 — TheKing: loot double-dips + invented additions
- **Status:** DIVERGENT
- **Original:** ORIG `TheKing.java:183-227` — spawn ThePrince at y+10; Royal armor set + Royal sword; 150 random registry items + 150 random blocks
- **Port:** code identical (`TheKing.java:1305-1340`) PLUS `LT the_king.json:1-41` adds the royal set again + royal_guardian_sword + prince_egg + 30–80 diamond + 20–50 gold + 20–50 iron
- **Fix:** delete (or empty) `the_king.json` — the code path already reproduces the original drops exactly (ENT-SYS2-001).
- **Resolution:** FIXED (2026-06-11, Phase B — invented JSON pools removed, royal set in JSON only; registry sampling kept as documented exception, see FIX_LOG.md and phase_b_reports/B1_drops.md)

### BOSS-005 — TheKing: spawner block loses fuse, height, guard mode, enable gate
- **Status:** DIVERGENT
- **Original:** ORIG `KingSpawnerBlock.java:43-89` — 100-tick scheduled fuse; spawns at y+8 with `setGuardMode(1)` (home-leash anchor); gated by `TheKingEnable`
- **Port:** `PORT\block\BossSpawnerBlock.java:44-57` (generic, `ModBlocks.java:152-154`) — randomTick (unbounded delay), y+1, `MobSpawnType.EVENT`, no guard mode, no enable gate
- **Fix:** in `BossSpawnerBlock`: schedule a 100-tick block tick on placement; spawn at y+8; call the King's guard-mode setter; add a `THE_KING_ENABLE` config check.
- **Resolution:** FIXED (2026-06-11, Phase C — all four deviations closed; see FIX_LOG.md and phase_c_reports/C5_bosses.md)

---

## TheQueen

### BOSS-006 — TheQueen: attack nerfed, armor nerfed + phase scaling dropped
- **Status:** DIVERGENT
- **Original:** `OreSpawnMain.java:6522`, ORIG `TheQueen.java:99,817-828` — attack 225; armor 21 base with +2/+3/+5 phase scaling
- **Port:** `PORT\entity\TheQueen.java:157,257,158-259` — attack 200; flat armor 10, no scaling override
- **Fix:** set ATTACK_DAMAGE 225, ARMOR 21; port the +2/+3/+5 phase armor bonuses (mirror TheKing's ported structure at `TheKing.java:975-984`).
- **Resolution:** FIXED (2026-06-11, Phase C — +2/+3/+5 phase armor override ported incl. the orig's unreachable +3/+5 branches (the +2 condition is a superset, effective bonus is always +2); see FIX_LOG.md and phase_c_reports/C5_bosses.md)

### BOSS-007 — TheQueen: hitbox smaller, no PlayNicely shrink
- **Status:** DIVERGENT
- **Original:** ORIG `TheQueen.java:79-81` — 22×24 (5.5×6 PlayNicely)
- **Port:** `ModEntities.java:184-186` — 16×12 + MHLib parts (`TheQueen.java:425`)
- **Fix:** verify the MHLib profile covers the 22×24 envelope; implement PlayNicely shrink (BOSS-017).
- **Resolution:** FIXED (2026-06-11, Phase C — main hitbox 16×12 → 22×24 (profile + EntityType) per orig TheQueen.java:79; PlayNicely shrink stays with BOSS-017; see FIX_LOG.md and phase_c_reports/C5_bosses.md)

### BOSS-008 — TheQueen: QueenHead sidecar degraded
- **Status:** PARTIAL
- **Original:** ORIG `QueenHead.java` (pattern `KingHead.java:33,42,147-157`) — 19.9×10, yHeadRot-based teleport, damage-forward
- **Port:** `PORT\entity\QueenHead.java:92-99`, `ModEntities.java:585-587` — `@Deprecated` 2×2 entity, yBodyRot basis; spawned only when `mood==1` (`TheQueen.java:971-978`, gate matches orig)
- **Fix:** since MHLib parts already track 3 heads at ×1.0 damage, stop spawning QueenHead; otherwise restore 19.9×10 + yHeadRot.

### BOSS-009 — TheQueen: happy-discharge Bird variant dropped
- **Status:** PARTIAL
- **Original:** ORIG `TheQueen.java:355,430` — happy discharge: 25 soil/flower transforms + 10 Butterfly OR Bird
- **Port:** `TheQueen.java:787-878`, `QueenMoodGoal` — transforms + 10 butterflies only
- **Fix:** in `QueenMoodGoal` happy branch, roll 50/50 between Butterfly and Bird (Cockateil/bird entity) per original.

### BOSS-010 — TheQueen: invulnerable dormant wake-up phase added
- **Status:** DIVERGENT
- **Original:** does not exist in 1.7.10 — first hit dealt normal damage
- **Port:** `TheQueen.java:129-135,538-546` — first hit deals 0 dmg and starts a 60-tick invulnerable `idle_to_attack` transition (dormant blue → aggro red)
- **Fix:** decide: remove the free invulnerability window (apply the first hit's damage after wake-up) for fidelity, or keep and document; at minimum don't zero the triggering hit.
- **Resolution:** FIXED (2026-06-11, Phase C — invulnerability window removed entirely; wake-up animation kept as a purely cosmetic trigger, all hits damage normally as in 1.7.10; see FIX_LOG.md and phase_c_reports/C5_bosses.md)

### BOSS-011 — TheQueen: drops massively buffed + doubled
- **Status:** DIVERGENT
- **Original:** ORIG `TheQueen.java:190-199` — Royal sword ×1, PrinceEgg ×1, ThePrincess spawn, then 56× {QueenScale, beef, bone, rotten flesh}
- **Port:** code `TheQueen.java:405-421` — 56× {QueenScale, XP bottle, golden apple, NETHER STAR}; PLUS `LT the_queen.json` royal_guardian_sword + prince_egg + 30–56 queen_scale + 10–30 diamond/string/bone
- **Fix:** code path: revert the 56-roll pool to {queen_scale, beef, bone, rotten_flesh} (removes up to 56 nether stars/golden apples); move royal sword + prince_egg into ONE path and delete the duplicate (ENT-SYS2-001).
- **Resolution:** FIXED (2026-06-11, Phase B — see FIX_LOG.md and phase_b_reports/B1_drops.md)

### BOSS-012 — TheQueen: spawner block degraded (same four deviations as King's)
- **Status:** DIVERGENT
- **Original:** ORIG `QueenSpawnerBlock.java:55,66-67,81-89` — 100-tick fuse, y+8, `TheQueenEnable` gate, `setGuardMode(1)`
- **Port:** generic `BossSpawnerBlock` (`ModBlocks.java:155-157`) — randomTick, y+1, no gate, no guard mode
- **Fix:** same as BOSS-005, with a `THE_QUEEN_ENABLE` config.
- **Resolution:** FIXED (2026-06-11, Phase C — shared BossSpawnerBlock fix, THE_QUEEN_ENABLE added; see FIX_LOG.md and phase_c_reports/C5_bosses.md)

---

## Godzilla (Mobzilla)

### BOSS-013 — Godzilla: HP buffed, attack nerfed, armor entirely missing
- **Status:** DIVERGENT
- **Original:** `OreSpawnMain.java:6514`, ORIG `Godzilla.java:93,95,145` — HP 4000, attack 175, armor 21 (`func_70658_aO`)
- **Port:** `PORT\entity\Godzilla.java:62,111-119` — HP 6000, attack 150, NO armor attribute or override
- **Fix:** set MAX_HEALTH 4000, ATTACK_DAMAGE 175, add ARMOR 21 to `createAttributes()`. (Jump-landing AoE at `Godzilla.java:594-603` scales from attack and self-corrects.)
- **Resolution:** FIXED (2026-06-11, Phase B — see FIX_LOG.md and phase_b_reports/B2_mobstats.md)

### BOSS-014 — Godzilla: GodzillaHead sidecar degraded
- **Status:** PARTIAL
- **Original:** ORIG `GodzillaHead.java:33,147-157` — 9.9×10; teleport `(x−17·sin(yHeadRot), y+16, z+17·cos)`; damage-forward AABB
- **Port:** `PORT\entity\GodzillaHead.java:96-118`, `ModEntities.java:589-591` — `@Deprecated`, same 17/16 offsets but yBodyRot, registered 3×3; still spawned (`Godzilla.java:642-649`)
- **Fix:** stop spawning it (4-part system already includes a 5×5 head at ×1.0 dmg, `Godzilla.java:95-99,196-227`) or restore 9.9×10 + yHeadRot.

### BOSS-015 — Godzilla: drops re-themed + full double drop
- **Status:** DIVERGENT
- **Original:** ORIG `Godzilla.java:820-838+` — painting ×1; 50–79 GodzillaScale; 100–259 beef; 50–109 bone; 25–39 rolls of d80 enchanted-gear table
- **Port:** code `Godzilla.java:769-877` — nether star ×1, scales, 100–259 EMERALDS, 50–109 XP BOTTLES, gear rolls; PLUS `LT godzilla.json` drops a second complete set (saddle + scales + beef + bone + gear rolls)
- **Fix:** keep ONE path (prefer code); revert emeralds→beef and xp bottles→bone; delete the JSON duplicate and its saddle (ENT-SYS2-001).
- **Resolution:** FIXED (2026-06-11, Phase B — see FIX_LOG.md and phase_b_reports/B1_drops.md)

### BOSS-016 — Godzilla: custom sounds replaced with vanilla ender dragon
- **Status:** DIVERGENT
- **Original:** ORIG `Godzilla.java:178-188` — `orespawn:godzilla_living` (1/5) / `alo_hurt` / `godzilla_death`
- **Port:** `Godzilla.java:261-276` — ENDER_DRAGON_GROWL / ENDER_DRAGON_HURT / ENDER_DRAGON_DEATH
- **Fix:** register/use `ModSounds.GODZILLA_LIVING/ALO_HURT/GODZILLA_DEATH`.
- **Resolution:** FIXED (2026-06-11, Phase C — orespawn:godzilla_living (1-in-5)/alo_hurt/godzilla_death wired (all three already in sounds.json); see FIX_LOG.md and phase_c_reports/C5_bosses.md)

---

## PlayNicely (cross-boss)

### BOSS-017 — PlayNicely flag is a no-op across all bosses
- **Status:** PARTIAL
- **Original:** `OreSpawnMain.java:1485` `PlayNicely` — gates targeting (`ORIG/TheKing.java:985`, `ThePrincess.java:846`) and shrinks boss sizes (King/Queen 22×24→5.5×6, Godzilla 9.9×25→2.475×6.25)
- **Port:** `OreSpawnConfig.java:156` `PLAY_NICELY` exists and is synced (`TheKing.java:528`, `TheQueen.java:734`) but never consumed: no targeting gate, no scale change (renderer `SCALE=1.0F`, `TheKingRenderer.java:45`); ThePrincess gate absent
- **Fix:** consume the synced flag: (1) return null from boss `findTarget`/targeting goals when PlayNicely is set, (2) apply the dimension/render scale shrink for King/Queen/Godzilla, (3) restore ThePrincess's targeting gate. One shared helper, applied in all four classes.

---

## ThePrince (baby)

### BOSS-018 — ThePrince: feeding heal formula changed
- **Status:** DIVERGENT
- **Original:** ORIG `ThePrince.java:215-224` — any food heals `healAmount×10`, ++fedCount
- **Port:** `PORT\entity\ThePrince.java:306-318` — flat 20 HP, ++fedCount
- **Fix:** heal `foodProperties.getNutrition() × 10` instead of flat 20.
- **Resolution:** FIXED (2026-06-11, Phase C — see FIX_LOG.md and phase_c_reports/C5_bosses.md)

### BOSS-019 — ThePrince: fire toggle interactions missing
- **Status:** MISSING
- **Original:** ORIG `ThePrince.java:233-258` — flint&steel → fire ON, ice block → fire OFF, with chat messages
- **Port:** `DATA_FIRE` exists but no interaction sets it
- **Fix:** in `mobInteract`, handle FLINT_AND_STEEL (`setFire(1)`) and ICE (`setFire(0)`) + player messages.

### BOSS-020 — ThePrince: grow-trigger item changed
- **Status:** DIVERGENT
- **Original:** ORIG `ThePrince.java:267-278` — DIAMOND triggers growth when ok_to_grow
- **Port:** `ThePrince.java:297-303` — GOLD INGOT; cake added to max counters (`:285-295`, not in orig)
- **Fix:** change the grow item to `Items.DIAMOND`; remove or document the cake shortcut.
- **Resolution:** FIXED (2026-06-11, Phase C — grow item now DIAMOND; invented cake shortcut removed from the baby; see FIX_LOG.md and phase_c_reports/C5_bosses.md)

### BOSS-021 — ThePrince: natural growth blocked by extra okToGrow gate
- **Status:** PARTIAL
- **Original:** ORIG `ThePrince.java:556` — grows when `kill>25 && fed>10 && day>10` (no okToGrow gate)
- **Port:** `ThePrince.java:230` — same condition AND `okToGrow != 0` — natural growth can never trigger without diamond-block/cake
- **Fix:** drop the `okToGrow` term from the natural-growth condition at `ThePrince.java:230`.

### BOSS-022 — ThePrince: ranged attack trio missing
- **Status:** MISSING
- **Original:** ORIG `ThePrince.java:634-663,782-853` — fireball / ThunderBolt / IceBall canons at 5–12 block range when fire enabled
- **Port:** none
- **Fix:** port the three-canon ranged attack (reuse TheKing's `firecanon`/`firecanonl`/`firecanoni` plumbing at baby scale), gated on `DATA_FIRE`.

### BOSS-023 — ThePrince: flight missing
- **Status:** MISSING
- **Original:** ORIG `ThePrince.java:585-725` — flying wander/owner-follow `do_movement` incl. owner-flying speedups
- **Port:** ground `MyEntityAIWander` (`:82`)
- **Fix:** port the flight movement (the codebase already has the pattern in EntitySpyro `:253-359` — reuse it).

### BOSS-024 — ThePrince: target list narrowed
- **Status:** DIVERGENT
- **Original:** ORIG `ThePrince.java:746-761` — Monsters AND Mothra/Butterfly/Cockateil/Dragonfly/Mosquito; PlayNicely gate (`:765`)
- **Port:** `ThePrince.java:247-254` — Monsters only; insects/Mothra explicitly excluded; no PlayNicely
- **Fix:** add the insect/Mothra prey classes back to targeting; PlayNicely per BOSS-017.
- **Resolution:** FIXED (2026-06-11, Phase C — Mothra/Butterfly/Cockateil/Dragonfly/Mosquito restored as prey; PlayNicely gate stays with BOSS-017; see FIX_LOG.md and phase_c_reports/C5_bosses.md)

### BOSS-025 — ThePrince: drops beef → diamond
- **Status:** DIVERGENT
- **Original:** ORIG `ThePrince.java:354-361` — 1–4 beef
- **Port:** `LT the_prince.json` — 1–4 diamond
- **Fix:** rewrite `the_prince.json` to beef 1–4.
- **Resolution:** FIXED (2026-06-11, Phase C — see FIX_LOG.md and phase_c_reports/C5_bosses.md)

---

## ThePrinceTeen

### BOSS-026 — ThePrinceTeen: stats divergent (HP/armor/speed/XP/size)
- **Status:** DIVERGENT
- **Original:** ORIG `ThePrinceTeen.java:229-231,252-254,87,105,103` — HP 1500, armor 18, speed 0.32, XP 300, size 3.25×4.25
- **Port:** `PORT\entity\ThePrinceTeen.java:90,88-95,58,68`, `ModEntities.java:472-474` — HP 1000, NO armor, speed 0.35, XP 500, size 2×3
- **Fix:** set MAX_HEALTH 1500, add ARMOR 18, speed 0.32, XP 300; resize EntityType to 3.25×4.25.
- **Resolution:** FIXED (2026-06-11, Phase C — size 3.25×4.25 applied in ModEntities per orig ThePrinceTeen.java:103, completing the Phase B stat fixes; see FIX_LOG.md and phase_c_reports/C5_bosses.md)

### BOSS-027 — ThePrinceTeen: riding missing
- **Status:** MISSING
- **Original:** ORIG `ThePrinceTeen.java:1157` — saddle-free mount
- **Port:** no riding
- **Fix:** add `mobInteract` startRiding + `getControllingPassenger`/`tickRidden` ground movement.
- **Resolution:** FIXED (2026-06-11, Phase B — see FIX_LOG.md and phase_b_reports/B3_riders.md)

### BOSS-028 — ThePrinceTeen: ranged canon trio missing
- **Status:** MISSING
- **Original:** ORIG — fire/lightning/ice canons (same trio pattern as ThePrince)
- **Port:** none
- **Fix:** port the three-canon attack (shared implementation with BOSS-022).

### BOSS-029 — ThePrinceTeen: regression-to-baby added
- **Status:** DIVERGENT
- **Original:** no shrink-back exists in 1.7.10
- **Port:** `ThePrinceTeen.java:240-254` — gold ingot reverts teen → baby
- **Fix:** remove the gold-ingot regression (or document as intentional; note gold ingot also conflicts with BOSS-020's grow item).
- **Resolution:** FIXED (2026-06-11, Phase C — invented gold-ingot teen→baby regression removed (orig ThePrinceTeen.java:1127-1230 has no shrink-back); see FIX_LOG.md and phase_c_reports/C5_bosses.md)

---

## ThePrinceAdult

### BOSS-030 — ThePrinceAdult: armor missing
- **Status:** MISSING
- **Original:** ORIG `ThePrinceAdult.java:248-250` — armor 20
- **Port:** `PORT\entity\ThePrinceAdult.java:86-93` — no ARMOR attribute
- **Fix:** add ARMOR 20 to `createAttributes()`.
- **Resolution:** FIXED (2026-06-11, Phase B — see FIX_LOG.md and phase_b_reports/B2_mobstats.md)

### BOSS-031 — ThePrinceAdult: size shrunk
- **Status:** DIVERGENT
- **Original:** ORIG `ThePrinceAdult.java:100` — 6.25×10.25
- **Port:** `ModEntities.java:464-466` — 4×6
- **Fix:** resize EntityType to 6.25×10.25 (and check model scale).
- **Resolution:** FIXED (2026-06-11, Phase C — resized per orig ThePrinceAdult.java:100; model scale needs an in-game look (pending manual test); see FIX_LOG.md and phase_c_reports/C5_bosses.md)

### BOSS-032 — ThePrinceAdult: King-transform config gate dropped
- **Status:** DIVERGENT
- **Original:** ORIG `ThePrinceAdult.java:400-404` — transform→TheKing gated `activity==0 && no rider && !peaceful && tamed && FullPowerKingEnable!=0`, growcounter>288000
- **Port:** `ThePrinceAdult.java:176-182,220-227` — gated `isTame && !hardcore` only; `FULL_POWER_KING_ENABLE` repurposed as King damage ×2 (`TheKing.java:893-896`)
- **Fix:** re-add a `FULL_POWER_KING_ENABLE`-style gate on the transform (and the no-rider/!peaceful checks); if the ×2 King damage stays, give it its own config key.
- **Resolution:** FIXED (2026-06-11, Phase C — full orig gate restored (activity 0 + riderless + !Peaceful + tamed + FULL_POWER_KING_ENABLE) and transform now calls king.setFree() per orig :408; the invented ×2 King-damage repurposing was removed rather than re-keyed; see FIX_LOG.md and phase_c_reports/C5_bosses.md)

### BOSS-033 — ThePrinceAdult: riding missing
- **Status:** MISSING
- **Original:** ORIG `ThePrinceAdult.java:1134` — mountable
- **Port:** no riding
- **Fix:** same as BOSS-027.
- **Resolution:** FIXED (2026-06-11, Phase B — see FIX_LOG.md and phase_b_reports/B3_riders.md)

### BOSS-034 — ThePrinceAdult: ranged canon trio missing
- **Status:** MISSING
- **Original:** ORIG — fire/lightning/ice canons
- **Port:** none
- **Fix:** shared implementation with BOSS-022/028.

### BOSS-035 — ThePrinceAdult: PrinceEgg drop lost
- **Status:** DIVERGENT
- **Original:** ORIG `ThePrinceAdult.java:313-315` — PrinceEgg ×1
- **Port:** `LT the_prince_adult.json` — 5–15 diamond + 3–8 gold
- **Fix:** rewrite `the_prince_adult.json` to prince_egg ×1.
- **Resolution:** FIXED (2026-06-11, Phase C — see FIX_LOG.md and phase_c_reports/C5_bosses.md)

### BOSS-036 — ThePrinceAdult: King-tier sounds replaced
- **Status:** DIVERGENT
- **Original:** ORIG `ThePrinceAdult.java:270-281` — king_living / king_hit / trex_death
- **Port:** `ThePrinceAdult.java:272-287` — roar / alo_hurt / alo_death
- **Fix:** switch to `ModSounds.KING_LIVING/KING_HIT/TREX_DEATH` (already registered for TheKing).
- **Resolution:** FIXED (2026-06-11, Phase C — king_living (only while aggro + riderless + not sitting, per orig :265-273)/king_hit/trex_death; see FIX_LOG.md and phase_c_reports/C5_bosses.md)

### BOSS-037 — ThePrinceAdult: grow-counter NBT key renamed — old saves lose progress
- **Status:** PARTIAL
- **Original:** ORIG `ThePrinceAdult.java:1318` — `ThePrinceAdultGrow`
- **Port:** `ThePrinceAdult.java:302` — `PrinceGrow`
- **Fix:** in `readAdditionalSaveData`, fall back to reading `ThePrinceAdultGrow` when `PrinceGrow` is absent (one-line legacy migration).

---

## ThePrincess

### BOSS-038 — ThePrincess: all four core stats off
- **Status:** DIVERGENT
- **Original:** ORIG `ThePrincess.java:194-196,377-379,334-336,62,81` — HP 400, attack 9, armor 14, speed 0.32
- **Port:** `PORT\entity\ThePrincess.java:85-88,52,86` — HP 500, attack 10, armor 16, speed 0.3
- **Fix:** set MAX_HEALTH 400, ATTACK_DAMAGE 9, ARMOR 14, MOVEMENT_SPEED 0.32.
- **Resolution:** FIXED (2026-06-11, Phase B — HP/speed/armor fixed; attack verified correct at orig 10 (audit's 9 wrong), see FIX_LOG.md and phase_b_reports/B2_mobstats.md)

### BOSS-039 — ThePrincess: feeding heal flat instead of ×10
- **Status:** PARTIAL
- **Original:** ORIG `ThePrincess.java:224-226` — food heals `healAmount×10`
- **Port:** `ThePrincess.java:197-201` — flat 20
- **Fix:** heal `nutrition × 10` (same fix as BOSS-018).

### BOSS-040 — ThePrincess: ranged canon trio missing
- **Status:** MISSING
- **Original:** ORIG `ThePrincess.java:730-748,863-909` — fire/lightning/ice canons
- **Port:** none
- **Fix:** shared canon implementation (BOSS-022 family).

### BOSS-041 — ThePrincess: flight missing
- **Status:** MISSING
- **Original:** ORIG — flying `do_movement`
- **Port:** ground wander
- **Fix:** port flight movement (reuse EntitySpyro pattern), same as BOSS-023. (PlayNicely targeting gate: BOSS-017.)

### BOSS-042 — ThePrincess: drops beef → diamond
- **Status:** DIVERGENT
- **Original:** ORIG `ThePrincess.java:342-349` — 1–4 beef
- **Port:** `LT the_princess.json` — 1–4 diamond
- **Fix:** rewrite `the_princess.json` to beef 1–4.
- **Resolution:** FIXED (2026-06-11, Phase C — see FIX_LOG.md and phase_c_reports/C5_bosses.md)

---

## Framework / Config

### BOSS-043 — Boss enable configs missing
- **Status:** PARTIAL
- **Original:** `OreSpawnMain.java:6434-6435` — `TheKingEnable` / `TheQueenEnable` gate boss spawning
- **Port:** `OreSpawnConfig.java` — no equivalents (`MOBZILLA_SINGLE_SPAWN` :128 exists for Godzilla; `FULL_POWER_KING_ENABLE` :159 repurposed)
- **Fix:** add `THE_KING_ENABLE`/`THE_QUEEN_ENABLE` booleans and consume them in the spawner blocks (ties into BOSS-005/012).

### BOSS-044 — MultiHitboxLib only used by TheQueen
- **Status:** PARTIAL
- **Original:** n/a (single AABB + sidecar in 1.7.10)
- **Port:** only `the_queen.json` exists in `RES\data\orespawn\multihitboxlib\hitbox_profiles\`; TheKing and Godzilla still use manual `OreSpawnPartEntity` positioning (`TheKing.java:428-432`, `Godzilla.java:196-227`)
- **Fix:** author `the_king.json` and `godzilla.json` MHLib hitbox profiles (bone-tracked, mirroring the Queen's damage-multiplier scheme) and migrate both bosses off manual part offsets — or document manual parts as final and delete the deprecated head sidecars (BOSS-003/008/014).

---

## Register totals

- Total entries: 224 (ENT-SYS2: 4 · ENT-K: 89 · ENT-S: 87 · BOSS: 44)
- DIVERGENT: 128 · PARTIAL: 59 · MISSING: 31 · UNVERIFIED: 6


---

# Findings — Blocks, Items, Recipes, Worldgen

Source: `audit_sections/06_blocks_items.md` (ITEM-###) and `audit_sections/07_worldgen.md` (WGEN-###).
ORIG = `reference_1_7_10_source/sources/danger/orespawn/`, PORT = `src/main/java/danger/orespawn/` + `src/main/resources/data/`.
Only MISSING / PARTIAL / DIVERGENT / UNVERIFIED items are listed; fully PORTED items are omitted.

---

# PART A — Blocks, Items, Recipes (file 06)

## Overworld ore blocks

### ITEM-001 — OreRuby/OreAmethyst: explosion behavior wrongly applied to overworld ores (systemic)
- **Status:** DIVERGENT
- **Original:** `OreRuby.java:21-22`, `OreAmethyst.java` ctor — overworld ruby/amethyst ores never exploded; only crystal-dimension CrystalCoal had volatile break behavior
- **Port:** `block/OreCrystal.java:49` — `ModBlocks` registers RUBY_ORE (`ModBlocks.java:19`), AMETHYST_ORE (`:21`), and ORE_KYANITE/ORE_PINK_TOURMALINE (`:87,94`) as `OreCrystal` → 1/3 chance to explode on break
- **Fix:** Register overworld ruby/amethyst (and kyanite/pink tourmaline) as plain `DropExperienceBlock`/dedicated non-volatile classes; reserve the `OreCrystal` explode-on-break behavior for crystal-dimension CrystalCoal only.
- **Resolution:** FIXED (2026-06-12, Phase C — see FIX_LOG.md and phase_c_reports/C6_items_blocks.md)

### ITEM-002 — Overworld ores: hardness/resistance values (systemic)
- **Status:** DIVERGENT
- **Original:** per-class ctors — values in table below
- **Port:** `ModBlocks.java:19,21,23,25,27` — values in table below
- **Fix:** Set `strength(hardness, resistance)` in `ModBlocks` to the original values:

| Block | ORIG hard/res (file:line) | PORT hard/res | Target |
|---|---|---|---|
| OreRuby | 10.0/4.0 (`OreRuby.java:21-22`) | 3.0/3.0 (`ModBlocks.java:19`) | 10.0/4.0 |
| OreAmethyst | 10.0/4.0 (`OreAmethyst.java` ctor) | 3.0/3.0 (`:21`) | 10.0/4.0 |
| OreUranium | 10.0/1.0 (`OreUranium.java:24-26`) | 3.0/3.0 (`:23`) | 10.0/1.0 |
| OreTitanium | 15.0/5.0 (`OreTitanium.java` ctor) | 3.0/3.0 (`:25`) | 15.0/5.0 |
| OreSalt | 5.0/2.0 (`OreSalt.java:21-22`) | 2.0/2.0 (`:27`) | 5.0/2.0 |
- **Resolution:** FIXED (2026-06-12, Phase C — see FIX_LOG.md and phase_c_reports/C6_items_blocks.md)

### ITEM-003 — OreUranium/OreTitanium: Y-level-gated XP drop lost
- **Status:** DIVERGENT
- **Original:** `OreUranium.java:24-26`, `OreTitanium.java` — XP drops only when broken below Y40
- **Port:** `loot_table/blocks/*.json` via `ModBlocks.java:23,25` — JSON loot/XP is Y-independent
- **Fix:** Override `getExpDrop` in `block/OreUranium.java`/`block/OreTitanium.java` to return XP only when `pos.getY() < 40` (return 0 otherwise).

## Crystal-dimension blocks
- **Resolution:** FIXED (2026-06-12, Phase C — see FIX_LOG.md and phase_c_reports/C6_items_blocks.md)

### ITEM-004 — Crystal ores: constructor parameter shift (systemic)
- **Status:** DIVERGENT
- **Original:** `OreSpawnMain.java:1865-1868` — ctor signature `(id, lightValue, hardness, resistance)`
- **Port:** `ModBlocks.java:72,76,78` — light/hardness values were read as `(hardness, resistance)`, dropping resistance and inventing light levels
- **Fix:** Re-register the three blocks with correct values from the table:

| Block | ORIG light/hard/res (`OreSpawnMain.java`) | PORT current | Target |
|---|---|---|---|
| CrystalCoal | light 9 (0.6), hard 6.0, res 20.0 (`:1865`) | strength(0.6, 6.0), light 8 (`:72`) | strength(6.0, 20.0), light 9 |
| CrystalCrystal | light 6 (0.4), hard 12.0, res 40.0 (`:1867`) | strength(0.4, 12.0), light 12 (`:76`) | strength(12.0, 40.0), light 6 |
| TigersEye ore | light 7 (0.5×15=7.5), hard 15.0, res 60.0 (`:1868`) | strength(0.5, 15.0), light 12 (`:78`) | strength(15.0, 60.0), light 7 or 8 |
- **Resolution:** FIXED (2026-06-12, Phase C — see FIX_LOG.md and phase_c_reports/C6_items_blocks.md)

### ITEM-005 — RedAntTroll/TermiteTroll: spawn count nerf + Silk Touch escape
- **Status:** DIVERGENT
- **Original:** `OreBasicStone.java:24+` (regs `OreSpawnMain.java:1877-1878`) — breaking spawns **15-20** mobs; no Silk Touch bypass
- **Port:** `block/OreBasicStone.java` (regs `ModBlocks.java:111,113`) — spawns **3-5**; Silk Touch bypass added
- **Fix:** Change spawn count to `15 + random.nextInt(6)` (15-20) and remove the Silk Touch bypass (or keep it only behind a documented config flag).
- **Resolution:** FIXED (2026-06-12, Phase C — see FIX_LOG.md and phase_c_reports/C6_items_blocks.md)

### ITEM-006 — CrystalGrass: plant-sustain behavior
- **Status:** UNVERIFIED
- **Original:** `OreSpawnMain.java:1866` + CrystalGrass class — should sustain crystal-dim plants (flowers/rice/quinoa)
- **Port:** `ModBlocks.java:74`, `block/CrystalGrass.java` — strength matches; whether crystal plants can be placed/survive on it was not compared
- **Fix:** Unverified because the audit only compared strength values. Resolve by testing `canSurvive`/`mayPlaceOn` for crystal flowers, rice, and quinoa on CrystalGrass and comparing to original `canPlaceBlockOn` logic.

## Storage / decorative blocks
- **Resolution:** FIXED (2026-06-12, Phase C — see FIX_LOG.md and phase_c_reports/C6_items_blocks.md)

### ITEM-007 — Gem storage blocks: strength + light emission (systemic)
- **Status:** DIVERGENT
- **Original:** `BlockRuby.java:23-25`, `BlockUranium.java`, `BlockTitanium.java`, `BlockCrystal.java` — all 4.0/4.0 with light 6 (0.4)
- **Port:** `ModBlocks.java:31,33,35,37,42,44` — 5.0/6.0, no light
- **Fix:** For BLOCK_RUBY, BLOCK_AMETHYST, BLOCK_URANIUM, BLOCK_TITANIUM, BLOCK_CRYSTAL_PINK, BLOCK_TIGERS_EYE: set `strength(4.0F, 4.0F).lightLevel(s -> 6)`.
- **Resolution:** FIXED (2026-06-12, Phase C — see FIX_LOG.md and phase_c_reports/C6_items_blocks.md)

### ITEM-008 — BlockMobzillaScale: wrong contact effect
- **Status:** DIVERGENT
- **Original:** `BlockRuby.java` (mobzilla variant) — applies **Strength** effect on contact
- **Port:** `block/BlockRuby.java:23+` (isMobzillaScale branch) — applies **FIRE_RESISTANCE**; also 5.0/6.0 strength
- **Fix:** Change the isMobzillaScale effect to `MobEffects.DAMAGE_BOOST` (Strength); also align strength per ITEM-007.
- **Resolution:** FIXED (2026-06-12, Phase C — see FIX_LOG.md and phase_c_reports/C6_items_blocks.md)

### ITEM-009 — Lavafoam: hardness 10× off, slipperiness lost
- **Status:** DIVERGENT
- **Original:** `Lavafoam.java:23-26` — 5.0/5.0, slipperiness **1.1**, pushes entities + speed-scaled damage
- **Port:** `ModBlocks.java:52`, `block/Lavafoam.java:29` — 0.5/0.5, no friction set; push/damage ported
- **Fix:** Set `strength(5.0F, 5.0F).friction(1.1F)` on the Lavafoam registration.
- **Resolution:** FIXED (2026-06-12, Phase C — see FIX_LOG.md and phase_c_reports/C6_items_blocks.md)

### ITEM-010 — BlockEnderPearl/BlockEyeOfEnder: strength values
- **Status:** UNVERIFIED
- **Original:** registered in `OreSpawnMain` — ctor values not extracted by the audit
- **Port:** `ModBlocks.java:46,48` — 3.0/3.0
- **Fix:** Unverified because original ctor hardness/resistance were never read. Resolve by opening the original BlockEnderPearl/BlockEyeOfEnder classes and comparing to the port's 3.0/3.0.

## Functional blocks
- **Resolution:** FIXED (2026-06-12, Phase C — see FIX_LOG.md and phase_c_reports/C6_items_blocks.md)

### ITEM-011 — BlockPizza: left-click eating missing
- **Status:** PARTIAL
- **Original:** `BlockPizza.java:30+` — eat slice via right- **and** left-click, 4 food/0.2 sat per slice
- **Port:** `block/BlockPizza.java:34` — right-click only (nutrition values match)
- **Fix:** Add a left-click handler (`attack()` override or `PlayerInteractEvent.LeftClickBlock` listener) that consumes a slice identically to the right-click path.
- **Resolution:** FIXED (2026-06-12, Phase C — see FIX_LOG.md and phase_c_reports/C6_items_blocks.md)

### ITEM-012 — BlockDuctTape: left-click repair missing
- **Status:** PARTIAL
- **Original:** `BlockDuctTape.java` — repairs held item on right- and left-click
- **Port:** `block/BlockDuctTape.java` — right-click repair only (USES property)
- **Fix:** Add left-click repair path mirroring the right-click logic (same event approach as ITEM-011).
- **Resolution:** FIXED (2026-06-12, Phase C — see FIX_LOG.md and phase_c_reports/C6_items_blocks.md)

### ITEM-013 — RTPBlock: teleport trigger likely dead
- **Status:** DIVERGENT
- **Original:** `RTPBlock.java:25` — teleports on **stepOn** (`func_149724_b`) with explosion fx
- **Port:** `block/RTPBlock.java:33`, `ModBlocks.java:62` — uses `entityInside`, but the block is a full cube so `entityInside` may never fire
- **Fix:** Replace `entityInside` with a `stepOn` override (`Block#stepOn`), which fires for entities standing on full cubes.
- **Resolution:** FIXED (2026-06-12, Phase C — see FIX_LOG.md and phase_c_reports/C6_items_blocks.md)

### ITEM-014 — MoleDirtBlock: collision shape
- **Status:** PARTIAL
- **Original:** `MoleDirtBlock.java:39` — lowered collision box (entities sink in); despawn + slow behavior
- **Port:** `block/MoleDirtBlock.java:22`, `ModBlocks.java:66` — despawn/slow ported, but full-cube collision
- **Fix:** Override `getCollisionShape` to return a lowered box (e.g. `Block.box(0,0,0,16,14,16)` matching the original offset) so entities sink like soul sand.
- **Resolution:** FIXED (2026-06-12, Phase C — see FIX_LOG.md and phase_c_reports/C6_items_blocks.md)

### ITEM-015 — CrystalFurnace (block): lit light level
- **Status:** PARTIAL
- **Original:** `CrystalFurnace.java:48` — active light 0.6 = level 9
- **Port:** `ModBlocks.java:122` — LIT light 13
- **Fix:** Change lit light level from 13 to 9 in the `lightLevel` lambda.
- **Resolution:** FIXED (2026-06-12, Phase C — see FIX_LOG.md and phase_c_reports/C6_items_blocks.md)

### ITEM-016 — CrystalFurnace (BE): cook speed + custom fuels unusable
- **Status:** DIVERGENT
- **Original:** `TileEntityCrystalFurnace.java:175` — cook time 150t; custom fuel values: CrystalCoal 20000, CrystalTreeLog 800, CrystalPlanks 400
- **Port:** `gui/CrystalFurnaceBlockEntity.java:45` — `CRYSTAL_SMELT_DURATION_TICKS=100`; fuel = vanilla `getBurnTime(SMELTING)` only, so crystal fuels have burn time 0
- **Fix:** Set cook duration to 150; register burn times (FurnaceFuel events / item `burnTime`) for CrystalCoal=20000, CrystalTreeLog=800, CrystalPlanks=400, or check them explicitly in the BE's fuel lookup.
- **Resolution:** FIXED (2026-06-12, Phase C — see FIX_LOG.md and phase_c_reports/C6_items_blocks.md)

### ITEM-017 — ExtremeTorch: Cephadrome spawn position
- **Status:** PARTIAL
- **Original:** `BlockExtremeTorch.java` — summons Cephadrome **randomly nearby** when torch is on an EyeOfEnder block
- **Port:** `block/BlockExtremeTorch.java:41` — summons at the torch position itself
- **Fix:** Offset the spawn position by a random nearby delta (match original random offsets) instead of spawning at the torch block.
- **Resolution:** FIXED (2026-06-12, Phase C — see FIX_LOG.md and phase_c_reports/C6_items_blocks.md)

### ITEM-018 — CrystalTorch: placement logic absent
- **Status:** PARTIAL
- **Original:** `BlockCrystalTorch.java` — custom `canPlaceTorchOn` allowing placement on crystal blocks
- **Port:** `block/BlockCrystalTorch.java:24` — standard `TorchBlock` (particles ported), no custom placement support
- **Fix:** Override `canSurvive`/support check to also accept crystal blocks (CrystalStone, CrystalCrystal, etc.) as valid supports.
- **Resolution:** FIXED (2026-06-12, Phase C — see FIX_LOG.md and phase_c_reports/C6_items_blocks.md)

### ITEM-019 — Kraken/Creeper Repellent: repel cadence too slow (systemic)
- **Status:** PARTIAL
- **Original:** `KrakenRepellent.java:82-124`, `CreeperRepellent.java` — repels Kraken+EntityAnt (resp. creepers) within 20 blocks every frequent tick, force ∝ distance
- **Port:** `block/RepellentBlock.java:31,44-61`, `ModBlocks.java:132,135` — repel runs on **randomTick** (~every 68s average)
- **Fix:** Convert to a BlockEntity ticker (or scheduled tick re-queue every 10-20t) applying the same predicate repel; verify radius=20 and target sets (Kraken+Ant / Creeper) match the originals.
- **Resolution:** FIXED (2026-06-12, Phase C — see FIX_LOG.md and phase_c_reports/C6_items_blocks.md)

### ITEM-020 — DungeonSpawnerBlock: 50 structures → 2, 400t → 200t
- **Status:** DIVERGENT
- **Original:** `DungeonSpawnerBlock.java:46+` — after 400 ticks spawns 1 of **50** structure types (FairyTree → RedAntHangout list)
- **Port:** `block/entity/RandomDungeonSpawnerBlockEntity.java:63-72`, `ModBlocks.java:166` — 200-tick countdown, then 1-in-4 ruby dungeon else generic dungeon (2 outcomes)
- **Fix:** Restore the 400-tick delay and expand the outcome pool toward the original 50-entry structure list as structures are ported (see WGEN-042); at minimum make the pool table-driven so new structures register into it.
- **Resolution:** PARTIAL (2026-06-12, Phase C — 400-tick fuse + table-driven nextInt(50) pool restored; structure builders beyond generic/ruby dungeon → WGEN-042 (Phase D); see FIX_LOG.md and phase_c_reports/C6_items_blocks.md)

### ITEM-021 — OreGenericEgg: XP bonus became item-dupe exploit
- **Status:** DIVERGENT
- **Original:** `OreGenericEgg.java:18-19` — 50% chance to drop **5-11 XP** on break
- **Port:** `block/OreGenericEgg.java:38`, `ModBlocks.java:147,149` — 50% chance drops **5-11 extra copies of the egg block** (infinite egg duplication)
- **Fix:** Replace the extra-item drop with `popExperience(level, pos, 5 + random.nextInt(7))`.
- **Resolution:** FIXED (2026-06-12, Phase C — see FIX_LOG.md and phase_c_reports/C6_items_blocks.md)

### ITEM-022 — RockBlock: block form absent
- **Status:** MISSING
- **Original:** `RockBlock.java` — block form of the Rock mob
- **Port:** absent — no corresponding block in `ModBlocks` (Rock mob/items may cover it — UNVERIFIED)
- **Fix:** Determine whether the Rock entity replicates the disguised-block behavior; if not, register a RockBlock with the original stats and the wake-to-mob behavior.

### ITEM-023 — ZooCage: block form dropped
- **Status:** PARTIAL
- **Original:** `ZooCage.java` — cage blocks/entities
- **Port:** `ZooCageItem`/`EmptyCageItem`/`CagedMobItem` + EntityCage — capture flow modernized, block form dropped
- **Fix:** Accept the item-based modernization as design; if block parity is required, add a placed-cage block that renders/holds the captured mob NBT. Document the decision either way.

## Plants, crops, leaves
- **Resolution:** PARTIAL (2026-06-12, Phase C — item-based capture flow accepted as the documented modernization; placed-cage block form → Phase D; see FIX_LOG.md and phase_c_reports/C6_items_blocks.md)

### ITEM-024 — Crop blocks: per-stage drop counts unconfirmed
- **Status:** PARTIAL
- **Original:** `BlockStrawberry`/`BlockRice`/`BlockRadish`/`BlockQuinoa`/`BlockTomato`/`BlockLettuce` — 4-stage `BlockCrops` with in-class drop logic
- **Port:** `CropBlock` subclasses + `loot_table/blocks/*.json` — growth ported; per-stage drop counts UNVERIFIED against JSON
- **Fix:** Diff each original class's `quantityDropped`/stage logic against the six loot JSONs; set count ranges and age conditions in the JSONs to match.
- **Resolution:** FIXED (2026-06-12, Phase C — see FIX_LOG.md and phase_c_reports/C6_items_blocks.md)

### ITEM-025 — BlockCorn: stalk height cap lost
- **Status:** DIVERGENT
- **Original:** `BlockCorn.java:49+` — multi-block stalk, height capped at 4-7, lower-stalk progression
- **Port:** `block/BlockCorn.java:64` — grows upward without any height cap (infinite stacking)
- **Fix:** Add a height check in the growth path: count stalk blocks below and stop growth at a per-plant cap of `4 + random(4)` (4-7).
- **Resolution:** FIXED (2026-06-12, Phase C — see FIX_LOG.md and phase_c_reports/C6_items_blocks.md)

### ITEM-026 — BlockAppleLeaves: night transform dimension lock lost
- **Status:** DIVERGENT
- **Original:** `BlockAppleLeaves.java:59` — night transform to ScaryLeaves **only in DimensionID4** (Islands)
- **Port:** `block/BlockAppleLeaves.java:53` — transforms at night in **any** dimension
- **Fix:** Gate the transform on `level.dimension() == ModDimensions.ISLANDS` before swapping to ScaryLeaves.
- **Resolution:** FIXED (2026-06-12, Phase C — see FIX_LOG.md and phase_c_reports/C6_items_blocks.md)

### ITEM-027 — BlockDuplicatorLog: behavior re-interpreted
- **Status:** PARTIAL
- **Original:** `BlockDuplicatorLog.java:37` — random tick calls `OreSpawnTrees.DuplicatorTree`
- **Port:** `block/BlockDuplicatorLog.java:48` — sapling/item duplication + tree growth, gated by `DUPLICATOR_TREE_ENABLE` (documented re-interpretation)
- **Fix:** Port the original `Trees.DuplicatorTree` generator (see WGEN-044) and have the log's random tick invoke it, keeping the config gate.

## Food effects
- **Resolution:** FIXED (2026-06-12, Phase C — see FIX_LOG.md and phase_c_reports/C6_items_blocks.md)

### ITEM-028 — Fish foods: effect durations all wrong (systemic)
- **Status:** DIVERGENT
- **Original:** fish item classes — durations in table below
- **Port:** `item/Item*Fish.java` — durations in table below
- **Fix:** Set each effect duration to the original value:

| Item | ORIG (file:line) | PORT (file:line) | Target |
|---|---|---|---|
| Sun Fish | Fire Res 6000t (`ItemSunFish.java:24-48`) | 600t (`item/ItemSunFish.java:19`) | Fire Res 6000t |
| Fire Fish | Fire Res 1200t (`ItemFireFish.java:26`) | 600t (`item/ItemFireFish.java:19`) | Fire Res 1200t |
| Lava Eel | Fire Res 600t (`ItemLavaEel.java:26`) | 1200t (`item/ItemLavaEel.java:19`) | Fire Res 600t |
| Spark Fish | Fire Res 100t (`ItemSparkFish.java:26`) | 600t (`item/ItemSparkFish.java`) | Fire Res 100t |
| Generic fish | 25% Hunger 20t (`ItemGenericFish.java:24-25`) | 25% Hunger 200t (`item/ItemGenericFish.java:18-19`) | Hunger 20t (keep 25%) |
- **Resolution:** FIXED (2026-06-12, Phase C — see FIX_LOG.md and phase_c_reports/C6_items_blocks.md)

### ITEM-029 — Special foods: potion effects entirely missing (systemic)
- **Status:** MISSING
- **Original:** `ItemSunFish.java:29-48` (shared effect dispatcher) — effects in table below
- **Port:** `ModItems.java:408,410,436,438` — registered as plain foods with no effects
- **Fix:** Add `FoodProperties.effect(...)` entries (or `finishUsingItem` overrides) per table:

| Item | Required effects (ORIG `ItemSunFish.java`) |
|---|---|
| Butter Candy (`:29-32`) | Speed + Jump Boost, 2000t |
| Cooked Bacon (`:33-36`) | Regeneration + Strength, 2000t |
| Crystal Apple (`:37-40`) | Regeneration + Strength, 3000t |
| Heart "Love" (`:41-48`) | Regen IV + Strength III + Fire Res III + Resistance II 6000t; Speed + Jump Boost 5000t |

## Special swords / melee

### ITEM-030 — Ultimate Sword: Unbreaking/Fire Aspect levels swapped
- **Status:** PARTIAL
- **Original:** `UltimateSword.java:44-59` — baked Unbreaking **3**, Fire Aspect **2** (Sharp/Smite/Bane=magic 5, KB 3, Looting 3 all correct)
- **Port:** `item/UltimateSword.java:32-39` — Unbreaking **2**, Fire Aspect **3**
- **Fix:** Swap the two values: Unbreaking 3, Fire Aspect 2.
- **Resolution:** FIXED (2026-06-12, Phase C — see FIX_LOG.md and phase_c_reports/C6_items_blocks.md)

### ITEM-031 — Big Bertha: Fire Aspect replaced by Sweeping Edge; reach added
- **Status:** PARTIAL
- **Original:** `Bertha.java:35-43` — baked KB5 / Bane1 / **Fire Aspect 1**; dur 9000 (`:31`); no reach bonus
- **Port:** `ModItems.java:272-275`, `item/Bertha.java:49` — KB5 / Bane1 / **Sweeping Edge 1**; +2.0 reach and kill-counter tooltip added
- **Fix:** Replace Sweeping Edge 1 with Fire Aspect 1 in the baked enchant list; decide (and document) whether the added reach/tooltip stay as deliberate enhancements.
- **Resolution:** FIXED (2026-06-12, Phase C — see FIX_LOG.md and phase_c_reports/C6_items_blocks.md)

### ITEM-032 — Slice: baked enchants missing
- **Status:** DIVERGENT
- **Original:** `Bertha.java` clone — dur 9000, same baked enchants as Bertha (KB5/Bane1/FireAspect1)
- **Port:** `ModItems.java:276-278`, `item/Slice.java` — no baked enchants passed
- **Fix:** Pass the Bertha enchant set (KB5, Bane1, FireAspect1) to the Slice registration.
- **Resolution:** FIXED (2026-06-12, Phase C — see FIX_LOG.md and phase_c_reports/C6_items_blocks.md)

### ITEM-033 — Royal Guardian Sword: wrong enchant identity + durability
- **Status:** DIVERGENT
- **Original:** `Bertha.java:36-37` (field_77347_r = unbreaking) — baked **Unbreaking 5**; dur **9000** override; dmg 750, hitType 2
- **Port:** `ModItems.java:279-282` — baked **Sharpness 5**; dur 10000 (tier)
- **Fix:** Change baked enchant from Sharpness 5 to Unbreaking 5; override durability to 9000 (or adjust the ROYAL tier use only for this item).
- **Resolution:** FIXED (2026-06-12, Phase C — see FIX_LOG.md and phase_c_reports/C6_items_blocks.md)

### ITEM-034 — Attitude Adjuster: durability 9000 → 2000
- **Status:** PARTIAL
- **Original:** `Bertha.java:31` durability override path — dur **9000** (dmg 86, no enchants, hitType 3 all correct)
- **Port:** `ModItems.java` HAMMY tier — dur 2000
- **Fix:** Override item durability to 9000 (original used the Bertha 9000 override despite tier 2000).
- **Resolution:** FIXED (2026-06-12, Phase C — see FIX_LOG.md and phase_c_reports/C6_items_blocks.md)

### ITEM-035 — Queen Battle Axe: wrong class lineage (enchants, shockwave, durability)
- **Status:** DIVERGENT
- **Original:** `UltimateSword` class — baked Looting 3 + Unbreaking 3; dur 3000; no shockwave; dmg 666
- **Port:** `ModItems.java:287-290` — built on Bertha class: Sharpness 5 + swing shockwave; dur 2200 (tier)
- **Fix:** Rebase on the UltimateSword-style item: baked Looting3 + Unbreaking3, durability 3000, remove shockwave.
- **Resolution:** FIXED (2026-06-12, Phase C — see FIX_LOG.md and phase_c_reports/C6_items_blocks.md)

### ITEM-036 — Battle Axe: wrong enchants, added shockwave, durability
- **Status:** DIVERGENT
- **Original:** `UltimateSword.java:56-58` — baked Looting 3 + Unbreaking 3; dur 3000; dmg 50
- **Port:** `ModItems.java:283-286` — KB5/Bane1/Sweeping1 via Bertha class + shockwave; dur 1500
- **Fix:** Same as ITEM-035: Looting3 + Unbreaking3, durability 3000, no shockwave.
- **Resolution:** FIXED (2026-06-12, Phase C — see FIX_LOG.md and phase_c_reports/C6_items_blocks.md)

### ITEM-037 — Chainsaw: signature mechanics missing, wrong enchants
- **Status:** DIVERGENT
- **Original:** `UltimateSword.java:63-394` — **no** baked enchants; left-click AoE (5-block radius, dmg 56), crushes wood/leaves in 11×16×11 on block break, saw sound + particles
- **Port:** `ModItems.java:291-294` — KB5/Bane1/Sweeping1 + Bertha shockwave; no AoE, no tree-crushing, no sound
- **Fix:** Remove baked enchants and shockwave; implement left-click AoE damage (r=5, dmg 56), 11×16×11 wood/leaf crush on block break, and the saw sound/particle loop.
- **Resolution:** FIXED (2026-06-12, Phase C — see FIX_LOG.md and phase_c_reports/C6_items_blocks.md)

### ITEM-038 — Nightmare Sword: durability + enchant identity
- **Status:** DIVERGENT
- **Original:** `NightmareSword.java:26,30-34` — dur **1200** override; baked Sharp1 / KB3 / **Fire Aspect 1**
- **Port:** `item/NightmareSword.java:22-24` — dur 1800 (tier); Sharp1 / KB3 / **Sweeping 1**
- **Fix:** Override durability to 1200; replace Sweeping 1 with Fire Aspect 1.
- **Resolution:** FIXED (2026-06-12, Phase C — see FIX_LOG.md and phase_c_reports/C6_items_blocks.md)

### ITEM-039 — Poison Sword: Weakness replaced by Hunger
- **Status:** DIVERGENT
- **Original:** `PoisonSword.java:50-59` — on-hit Poison + Wither + **Weakness**, 10-19s each
- **Port:** `item/PoisonSword.java:30-37` — Poison + Wither + **Hunger**
- **Fix:** Replace `MobEffects.HUNGER` with `MobEffects.WEAKNESS` in the on-hit effect list.
- **Resolution:** FIXED (2026-06-12, Phase C — see FIX_LOG.md and phase_c_reports/C6_items_blocks.md)

### ITEM-040 — Experience Sword: enchant identity, durability, armor-XP tick
- **Status:** DIVERGENT
- **Original:** `ExperienceSword.java:30-139` — dur **1400**; baked Sharp2 + **Unbreaking 3**; +10 XP/hit; bonus dmg = playerLevel/2; inventory tick grants XP while wearing Experience armor (`:63-103`)
- **Port:** `item/ExperienceSword.java:27-28,33-61` — dur 1300 (tier); Sharp2 + **Looting 3**; XP/bonus dmg ported; armor-XP tick MISSING
- **Fix:** Durability 1400; swap Looting3 → Unbreaking3; implement `inventoryTick` granting XP when the holder wears Experience armor pieces (per original rates, see also ITEM-057).
- **Resolution:** FIXED (2026-06-12, Phase C — see FIX_LOG.md and phase_c_reports/C6_items_blocks.md)

### ITEM-041 — Big Hammer: durability 9000 → 2000
- **Status:** PARTIAL
- **Original:** `BigHammer.java:25` — dur **9000** (launch-up behavior ported correctly)
- **Port:** `item/BigHammer.java:16-22` — dur 2000 (tier)
- **Fix:** Override durability to 9000.
- **Resolution:** FIXED (2026-06-12, Phase C — see FIX_LOG.md and phase_c_reports/C6_items_blocks.md)

### ITEM-042 — Mantis Claw: durability 1000 → 2000
- **Status:** PARTIAL
- **Original:** `MantisClaw.java:25` — dur **1000** (lifesteal -1/+1 ported correctly)
- **Port:** `item/MantisClaw.java:16-23` — dur 2000 (tier)
- **Fix:** Override durability to 1000.

## Ranged / gadgets
- **Resolution:** FIXED (2026-06-12, Phase C — see FIX_LOG.md and phase_c_reports/C6_items_blocks.md)

### ITEM-043 — Ultimate Bow: Power level + fire model
- **Status:** DIVERGENT
- **Original:** `UltimateBow.java:29-34,46-64` — baked **Power 5** (fixed), Flame 3, Punch 2, Infinity 1; fires **instantly** at velocity 3.0, 1/4 crit chance
- **Port:** `item/UltimateBow.java:28-32,37-58` — Power = `UltimateBowDamage` config (**10**); requires charge-up, crit at full pull or 1/4
- **Fix:** Bake Power 5 (config-independent) or default `UltimateBowDamage` to 5; restore instant-fire at velocity 3.0 with 1/4 crit (no charge), or document the charge model as a deliberate change.
- **Resolution:** FIXED (2026-06-12, Phase C — see FIX_LOG.md and phase_c_reports/C6_items_blocks.md)

### ITEM-044 — Skate Bow: behavior not compared
- **Status:** UNVERIFIED
- **Original:** `SkateBow.java` — recipe + custom arrows
- **Port:** `item/SkateBow.java` — dur 300
- **Fix:** Unverified because the audit never diffed firing behavior/arrow type/durability. Resolve by comparing `SkateBow.java` (orig) projectile, velocity, and durability against the port class. Recipe is separately MISSING (ITEM-060).
- **Resolution:** FIXED (2026-06-12, Phase C — see FIX_LOG.md and phase_c_reports/C6_items_blocks.md)

### ITEM-045 — Ultimate Fishing Rod: hook behavior not compared
- **Status:** UNVERIFIED
- **Original:** dur 3000 fishing rod with possible custom hook logic
- **Port:** `ModItems.java:319` — dur 3000
- **Fix:** Unverified because only durability was checked. Resolve by diffing the original rod class's hook/loot behavior against the port item.
- **Resolution:** FIXED (2026-06-12, Phase C — see FIX_LOG.md and phase_c_reports/C6_items_blocks.md)

### ITEM-046 — Magic Apple: structure geometry condensed
- **Status:** PARTIAL
- **Original:** `ItemMagicApple` usage — tree gen 80/19/1 rolls, King (diamond cap) / Queen (amethyst cap) spawns
- **Port:** `item/ItemMagicApple.java:69-141` — same rolls + boss triggers, condensed tree geometry
- **Fix:** If 1:1 parity is required, port the original tree-build geometry; otherwise document the condensed geometry as accepted (rolls/triggers already match).
- **Resolution:** FIXED (2026-06-12, Phase C — 80/19/1 rolls + King/Queen triggers verified to original; condensed tree geometry documented per audit option B; see FIX_LOG.md and phase_c_reports/C6_items_blocks.md)

### ITEM-047 — Instant Garden: layout and crop set replaced
- **Status:** DIVERGENT
- **Original:** `InstantGarden.java:26-147` — 18×15 plot with radish/lettuce/carrot/water/potato/wheat/tomato/corn/strawberry/reeds/melon rows, 10-high clearing
- **Port:** `item/InstantGarden.java:20-62` — 11×11 farmland with wheat/carrot/potato + fence
- **Fix:** Rebuild to the original 18×15 row layout (including mod crops radish/lettuce/tomato/corn/strawberry, water channels, reeds, melon) with 10-high clear.
- **Resolution:** FIXED (2026-06-12, Phase C — see FIX_LOG.md and phase_c_reports/C6_items_blocks.md)

### ITEM-048 — Instant Shelter: size, materials, loot replaced
- **Status:** DIVERGENT
- **Original:** `InstantShelter.java:28-150` — 7×7×~5 cobble-floor/plank shelter, direction-aware, door, chest with original contents
- **Port:** `item/InstantShelter.java:24-75` — 5×5×5 all-oak box; crafting table/furnace/chest (bread, torch, coal, wood pick, wood sword)
- **Fix:** Rebuild to the original 7×7 directional cobble/plank design with door and the original chest loot list.
- **Resolution:** FIXED (2026-06-12, Phase C — see FIX_LOG.md and phase_c_reports/C6_items_blocks.md)

### ITEM-049 — StepUp/StepDown/StepAcross: pathing semantics changed
- **Status:** DIVERGENT
- **Original:** `StepUp.java:26-99` (and Down/Across) — 8-way including diagonals; cobble path stops at obstruction (max 33); **ExtremeTorch** every 8 blocks; explosion fx
- **Port:** `item/StepUp.java:22-60` — 4-way cardinal only; always 33 long; vanilla torch every 3; stone sound; consumes item even in creative
- **Fix:** Add diagonal directions (8-way), stop at first obstruction, place ExtremeTorch every 8 blocks, restore explosion fx, and skip consumption in creative.
- **Resolution:** FIXED (2026-06-12, Phase C — see FIX_LOG.md and phase_c_reports/C6_items_blocks.md)

### ITEM-050 — ZooKeeper: persistence became AI-freeze
- **Status:** DIVERGENT
- **Original:** `ItemZooKeeper.java:44` — makes mob **persistent** (`func_110163_bv`, no despawn); dur 1, damage 2/use
- **Port:** `item/ItemZooKeeper.java:22` — sets **NoAi(true)**, freezing the mob; dur 256
- **Fix:** Replace `setNoAi(true)` with `setPersistenceRequired()`; restore original durability semantics (tiny durability, 2 damage per use).
- **Resolution:** FIXED (2026-06-12, Phase C — see FIX_LOG.md and phase_c_reports/C6_items_blocks.md)

### ITEM-051 — Sifter: loot tables gutted
- **Status:** DIVERGENT
- **Original:** `ItemSifter.java:35-471` — dur 600; sifts **water** (160-entry table: 6 mod fish, 4 shoes, ruby/amethyst/diamond...) plus sand/gravel/dirt/grass 60-entry tables incl. salt/scales/mod flowers
- **Port:** `item/ItemSifter.java:24-69` — dur 256; dirt/sand/gravel/soul-sand only; vanilla-only 100-roll table; no water sifting
- **Fix:** Restore dur 600; re-implement the original per-substrate weighted tables (water 160-table + four 60-tables) as data-driven loot tables including mod items.
- **Resolution:** FIXED (2026-06-12, Phase C — see FIX_LOG.md and phase_c_reports/C6_items_blocks.md)

### ITEM-052 — Wrench: ownership rules + kit damage lost
- **Status:** PARTIAL
- **Original:** `ItemWrench.java:29-80` — dur 100; SpiderRobot disassembles freely; AntRobot requires owner or HP<50%; resulting kit keeps damage
- **Port:** `item/ItemWrench.java:23-46` — dur 256; disassembles either robot unconditionally; kit damage not carried
- **Fix:** Restore dur 100; add the AntRobot owner-or-HP<50% guard; copy remaining HP into the dropped kit's damage value.
- **Resolution:** FIXED (2026-06-12, Phase C — see FIX_LOG.md and phase_c_reports/C6_items_blocks.md)

### ITEM-053 — Projectile/misc items: damage values not compared
- **Status:** UNVERIFIED
- **Original:** ItemRock ×12, Water-/Laser-/Ice-Ball, Acid, Irukandji, SunspotUrchin, NetherLost, robot kits, Coin, shoes, game controller, spawn eggs
- **Port:** registered with same stack sizes; behavior classes exist
- **Fix:** Unverified because per-projectile damage/velocity values were never diffed. Resolve by comparing each projectile entity/item class's damage constants between codebases.

## Armor materials
- **Resolution:** PARTIAL (2026-06-12, Phase C — projectile damage/velocity verified number-by-number and fixed (cooldown + Coin inventions removed, DeadIrukandji throw + urchin fire + WaterBall drop restored); Shoes & GameController throwables → Phase D; see FIX_LOG.md and phase_c_reports/C6_items_blocks.md)

### ITEM-054 — All 14 armor sets: durability ~1/15th and enchantability wrong (systemic)
- **Status:** DIVERGENT
- **Original:** `OreSpawnMain.java:1489-1502` (`get_armorstats(name, durability, head, chest, leg, boot, enchantability, ...)`) + `EnumHelper.addArmorMaterial` at `:1770-1783` — durability is a per-slot **multiplier** (helmet ×11, chest ×16, legs ×15, boots ×13; e.g. Ultimate helmet = 200×11 = 2200)
- **Port:** `ModItems.java:529-736` — durability multipliers ≈ orig/16 (Ultimate 13, Royal 125, Mobzilla 63, Queen 94); `ModArmorMaterials.java:43-98` — passes original *durability* as `enchantmentValue` (Ultimate 200, Royal 2000, Mobzilla 1000, Queen 1500, Amethyst 100...). Defense values are correct; the comment at `ModArmorMaterials.java:37-41` misstates what `ModItems` does.
- **Fix:** 1.21.1 `ArmorItem.Type.getDurability(mult)` uses the same per-slot bases (11/16/15/13), so set each set's durability multiplier to the **original durability value** and each `enchantmentValue` to the **original enchantability**, per this table:

| Set | Durability multiplier (target) | Enchantability (target) |
|---|---|---|
| Ultimate | 200 | 100 |
| Royal | 2000 | 200 |
| Queen | 1500 | 150 |
| Mobzilla | 1000 | 150 |
| Amethyst | 100 | 40 |
| Emerald | 60 | 40 |
| Experience | 70 | 50 |
| Moth Scale | 50 | 50 |
| Lava Eel | 40 | 35 |
| Pink | 50 | 40 |
| Tigers Eye | 80 | 55 |
| Peacock | 40 | 30 |
| Ruby | 90 | 40 |
| Lapis | 60 | 60 |
- **Resolution:** FIXED (2026-06-12, Phase C — see FIX_LOG.md and phase_c_reports/C6_items_blocks.md)

### ITEM-055 — ArmorStats record: positionally mislabeled fields
- **Status:** DIVERGENT
- **Original:** `ArmorStats.java:13-20` (orig) — enchant parameter order: resp, aqua, prot, fire, blast, proj, unb, fall
- **Port:** `ArmorStats.java:10-17` — field names (fireProtection/blastProtection/.../thorns) hold values from the wrong positions; values were copied in original order, and only the hardcoded `ENCHANT_TABLE` is used at runtime, so this is currently latent
- **Fix:** Rename the record components to the original order (respiration, aquaAffinity, protection, fireProtection, blastProtection, projectileProtection, unbreaking, featherFalling) so any future consumer reads correct values.
- **Resolution:** FIXED (2026-06-12, Phase C — see FIX_LOG.md and phase_c_reports/C6_items_blocks.md)

### ITEM-056 — Baked armor enchants: 4 sets diverge (systemic)
- **Status:** DIVERGENT
- **Original:** `ItemOreSpawnArmor.java:81-153` driven by stats at `OreSpawnMain.java:1491-1502` (order: resp, aqua, prot, fire, blast, proj, unb, fall)
- **Port:** `item/ItemOreSpawnArmor.java:28-117` `ENCHANT_TABLE`
- **Fix:** Correct the four rows of `ENCHANT_TABLE` to the original values (Royal/Peacock/Lapis/Experience and the no-enchant sets are already exact):

| Set | Correct values (ORIG) | Port error |
|---|---|---|
| Ultimate (`:1494`) | Resp2, Aqua3, Prot5, Fire5, Blast5, Proj5, Unb0, Fall3 | adds Unbreaking 3 → remove |
| Mobzilla (`:1498`) | Resp0, Aqua0, Prot10, Fire10, Blast10, Proj10, Unb5, Fall10 | adds Resp1/Aqua2 → remove |
| Moth Scale (`:1492`) | Prot3, Fire3, Blast3, Fall5, all else 0 | adds Unbreaking 3 → remove |
| Lava Eel (`:1493`) | Resp1, Aqua2, Prot3, Fire2, Blast10, Proj0, Unb0, Fall2 | Prot 2→3, add Fire Prot 2, remove Unbreaking 3 |
- **Resolution:** FIXED (2026-06-12, Phase C — see FIX_LOG.md and phase_c_reports/C6_items_blocks.md)

### ITEM-057 — Experience armor: XP-generation set effect missing
- **Status:** MISSING
- **Original:** `ExperienceSword.java:63-103` — ticking Experience Sword grants XP while the player wears Experience armor
- **Port:** absent — no XP-generation tick anywhere
- **Fix:** Implement the inventory-tick XP grant in the port's ExperienceSword (or a player-tick handler) scaling with worn Experience armor pieces, matching original tick rates/amounts. (Same root as ITEM-040.)

### ITEM-058 — Peacock boots glide: config gating changed
- **Status:** PARTIAL
- **Original:** `ItemOreSpawnArmor.java:343-358` — Peacock boots glide worked regardless of `RoyalGlideEnable`
- **Port:** `item/ItemOreSpawnArmor.java:160-189` — all glide (incl. Peacock) gated on `ROYAL_GLIDE_ENABLE`
- **Fix:** Exempt the Peacock-boots glide path from the `ROYAL_GLIDE_ENABLE` check (or document the unified gate as intentional).

## Recipes
- **Resolution:** FIXED (2026-06-12, Phase C — see FIX_LOG.md and phase_c_reports/C6_items_blocks.md)

### ITEM-059 — Uranium/Titanium smelting: 9× output inflation
- **Status:** DIVERGENT
- **Original:** `OreSpawnMain.java:3092,3094` — ore smelts to a **nugget**, XP 0.3
- **Port:** `recipe/uranium_ingot_smelting.json:3-5` (+ titanium analog) — ore smelts to a full **ingot**, XP 0.7
- **Fix:** Change both smelting JSONs' result to the nugget item and `"experience": 0.3` (ingot = 9 nuggets via crafting, as original).
- **Resolution:** FIXED (2026-06-12, Phase C — see FIX_LOG.md and phase_c_reports/C6_items_blocks.md)

### ITEM-060 — Skate Bow recipe absent
- **Status:** MISSING
- **Original:** `OreSpawnMain.java:3160` — Skate Bow crafting recipe
- **Port:** absent — no `skate_bow` JSON in `data/orespawn/recipe/`
- **Fix:** Add `recipe/skate_bow.json` reproducing the original shaped pattern/ingredients from `:3160`.

### ITEM-061 — Crystal planks → chest / piston conversions absent
- **Status:** MISSING
- **Original:** `OreSpawnMain.java:3083-3085,3209` — crystal planks craft into vanilla chest / piston
- **Port:** absent — no such JSONs; port instead has oak_door/iron_door conversions (divergent substitutions)
- **Fix:** Add recipe JSONs for chest and piston accepting crystal planks (tag or explicit item); review whether the door conversions should remain.

### ITEM-062 — Bulk recipe correspondence not diffed
- **Status:** UNVERIFIED
- **Original:** 381 registrations (189 shaped + 176 shapeless + 16 smelting), `OreSpawnMain.java:3000s`
- **Port:** 236 JSONs in `data/orespawn/recipe/` — only ~20 spot-checked
- **Fix:** Unverified because a full 381↔236 correspondence diff was never run. Resolve by scripting an extraction of all original `addRecipe`/`addShapelessRecipe`/`addSmelting` calls and matching each against a port JSON (mirrored left/center/right variants count as one).

## Dispenser behaviors
- **Resolution:** PARTIAL (2026-06-12, Phase C — all 381 original registrations diffed by script (phase_c_reports/C6_recipe_diff.md): 201 logical recipes verified/fixed, 16 invented recipe JSONs removed; absent recipe families (spawn-block→egg conversions etc.) → Phase D; see FIX_LOG.md and phase_c_reports/C6_items_blocks.md)

### ITEM-063 — All 8 dispenser behaviors unimplemented (systemic)
- **Status:** MISSING
- **Original:** 8 classes: `MyDispenserBehaviorAcid`, `MyDispenserBehaviorIceball`, `MyDispenserBehaviorLaserball`, `MyDispenserBehaviorRock`, `MyDispenserBehaviorWDCharge`, `MyDispenserBehaviorDeadIrukandji`, `MyDispenserBehaviorArrow` (Irukandji arrow), `MyDispenserBehaviorSunspotUrchin`
- **Port:** `ModDispenserBehaviors.java:3-7` — empty stub ("will be added when entity projectile constructors are finalized")
- **Fix:** Implement `DispenserBlock.registerBehavior` for all 8 items, each spawning its projectile entity with the original velocity/offsets: acid, iceball, laserball, rock, WD charge, dead irukandji, irukandji arrow, sunspot urchin.

## Config

### ITEM-064 — LESS_ORE defined but not wired
- **Status:** PARTIAL
- **Original:** `LessOre` config gates ore-generation multiplier (notably Mining dim 3× passes)
- **Port:** `OreSpawnConfig.java:139-141` — `LESS_ORE` exists with explicit TODO, affects nothing
- **Fix:** Wire `LESS_ORE` into the datapack/feature pipeline (e.g. select between normal and reduced placed-feature sets, and gate the Mining-dim density per WGEN-011).
- **Resolution:** PARTIAL (2026-06-12, Phase C — lessOre wired via the orespawn:less_ore_count placement modifier for overworld ore/troll-block veins; Mining-dim density gating → WGEN-011 (Phase D); see FIX_LOG.md and phase_c_reports/C6_items_blocks.md)

### ITEM-065 — Per-tier weapon/armor/ore stat overrides missing
- **Status:** MISSING
- **Original:** `OreSpawnMain.java:1491-1517` — `get_weaponstats`/`get_armorstats`/`get_orestats` exposed every stat number to the config file
- **Port:** absent — `WeaponStats`/`ArmorStats` records hardcoded
- **Fix:** Either add config bindings that override the record defaults at registration time, or document hardcoding as a deliberate platform decision (datapacks cover ore stats).

---

# PART B — Worldgen, Dimensions, Structures (file 07)

## Overworld ore generation

### WGEN-001 — OreSpawn ore vein counts inflated, Y-floors extended to −64 (systemic)
- **Status:** DIVERGENT
- **Original:** `OreSpawnMain.get_orestats` defaults, executed by `ChunkOreGenerator.generateOresInChunk`
- **Port:** `data/orespawn/worldgen/placed_feature/ore_*.json` + `configured_feature/ore_*.json`
- **Fix:** Set placed-feature `count` and height range to original values (salt already exact):

| Ore | ORIG rate/clump/Y | PORT count/size/Y | Target |
|---|---|---|---|
| Ruby (`ore_ruby.json`) | 10 / 1 / Y0–50 | 8 / 2 / −64..50 | count 10, size 1, Y 0..50 |
| Uranium (`ore_uranium.json`) | 3 / 4 / Y0–30 | 5 / 4 / −64..30 | count 3, Y 0..30 |
| Titanium (`ore_titanium.json`) | 3 / 4 / Y0–20 | 6 / 4 / −64..20 | count 3, Y 0..20 |
| Amethyst (`ore_amethyst.json`) | 2 / 6 / Y0–25 | 4 / 6 / −64..25 | count 2, Y 0..25 |

(If the deepslate layer should keep ores, document the −64 floor as a deliberate 1.21 adaptation instead.)

### WGEN-002 — Ruby: lava-adjacency placement mechanic dropped
- **Status:** DIVERGENT
- **Original:** `OreSpawnWorld.generateRuby` — rubies placed adjacent to lava pockets via a lava-seek loop
- **Port:** `placed_feature/ore_ruby.json` — standard random ore placement
- **Fix:** Implement a custom feature (or use a `block_predicate`-filtered placement) that only places ruby ore next to lava source blocks, mirroring the lava-seek loop.

### WGEN-003 — Block-of-Ruby ore generation absent
- **Status:** MISSING
- **Original:** `OreSpawnMain` BlockRuby_stats — 1 vein / clump 2 / Y0–15
- **Port:** absent — no feature for ruby-block veins
- **Fix:** Add configured+placed features generating `block_ruby` veins: count 1, size 2, Y 0..15, and include them in `add_ores.json`.

### WGEN-004 — Vanilla-ore boost generation absent
- **Status:** MISSING
- **Original:** `ChunkOreGenerator.generateOresInChunk` — extra Diamond 4/6/Y0–30, Diamond Block 2/4/Y0–20, Emerald 4/6/Y0–40, Emerald Block 2/4/Y0–20, Gold 4/8/Y0–40, Gold Block 2/4/Y0–25 on top of vanilla
- **Port:** absent — only vanilla defaults generate
- **Fix:** Add six configured/placed features with the listed rate/clump/Y values and register via the `add_ores` biome modifier.

### WGEN-005 — SpawnOres system reduced from ~105 block types to 2
- **Status:** PARTIAL
- **Original:** `OreSpawnMain` SpawnOres stats + `ChunkOreGenerator` — 28 veins/chunk clump 4 Y50–128 (+30 veins on a 1/20 roll) over a pool of ≈105 spawn-block types (7 OreSpawn + 98 vanilla-mob)
- **Port:** `placed_feature/dragon_spawn_block.json`, `kraken_spawn_block.json` (each 1/24 chunks, Y −56..−10) + `add_ancient_dried_eggs.json` (1/12, Y −32..32)
- **Fix:** Decide scope: full parity needs the spawn-block pool restored (custom feature picking from the weighted 105-type pool at 28+/chunk, Y50–128); otherwise document the 2-boss-block reduction as a deliberate redesign.

### WGEN-006 — AntHill surface blocks never world-placed
- **Status:** PARTIAL
- **Original:** `OreSpawnWorld.addAnts` (called at `:323`, redfreq=4) — ~4%/chunk anthill blocks (black/red/rainbow/unstable) in plains-type biomes
- **Port:** ant blocks registered (`ModBlocks.java:291-306`) but no placement feature; black/red ants natural-spawn instead (`add_overworld_creatures.json`)
- **Fix:** Add an anthill placed feature (~4%/chunk, plains biomes) placing the four ant-block types; this also restores rainbow/unstable ant access (see WGEN-048).

### WGEN-007 — Wild crop patches (strawberry/corn/tomato) absent
- **Status:** MISSING
- **Original:** `OreSpawnWorld.generateSurface` — strawberry patches in forest biomes; corn (~1%) and tomato patches in plains
- **Port:** absent — crop blocks exist but have no wild generation
- **Fix:** Add three random-patch configured/placed features (strawberry → `#is_forest`; corn ~1%/chunk and tomato → plains) via biome modifiers.

## Utopia dimension

### WGEN-008 — Utopia: veggie patches missing, altar frequency changed
- **Status:** PARTIAL
- **Original:** `OreSpawnWorld.java:42-51` — veggie patches among features; King/Queen Altar 1/2000 chunk roll after tree passes (`:2550`)
- **Port:** trees/altars present but no veggie patches; altars use `structure_set/royal_altars.json` random_spread spacing 64 / separation 32
- **Fix:** Add Utopia veggie-patch features (same crop set as overworld wild gen); tune royal_altars spacing if 64/32 measurably differs from the 1/2000-roll density in practice.

### WGEN-009 — Utopia: cricket/frog spawn group sizes
- **Status:** UNVERIFIED
- **Original:** `BiomeGenUtopianPlains` default ctor — Cricket 5, Frog 5 (original min/max group sizes not extracted)
- **Port:** `utopia_plains.json` — cricket 5(4-6), frog 5(4-6)
- **Fix:** Unverified because the original group min/max were not read. Resolve by checking the `BiomeGenUtopianPlains` SpawnListEntry args for cricket/frog and aligning the JSON group sizes.

## Mining dimension

### WGEN-010 — Mining: vanilla dungeons/mineshafts/strongholds absent
- **Status:** PARTIAL
- **Original:** `ChunkProviderOreSpawn2` — vanilla dungeons ×8/chunk, mineshafts, strongholds, scattered features
- **Port:** `mining_biome.json` — caves/springs only; no vanilla structure sets apply to this biome
- **Fix:** Add `mining_biome` to the vanilla `has_structure` tags for mineshaft/stronghold, and add a monster-room-style feature at 8 attempts/chunk to the biome's features.

### WGEN-011 — Mining: 3× ore density lost
- **Status:** DIVERGENT
- **Original:** `ChunkProviderOreSpawn2` — `generateOresInChunk` called up to 3× when `LessOre==0`, plus extra `generateRuby` and extra diamond/gold passes
- **Port:** `mining_biome.json` — same 1× rates as overworld
- **Fix:** Create mining-specific placed features at 3× count (gated by `LESS_ORE`, see ITEM-064) plus the extra ruby/diamond/gold passes, and reference them only from `mining_biome.json`.

### WGEN-012 — Mining: dino/alien monster roster replaced
- **Status:** DIVERGENT
- **Original:** `ChunkProviderOreSpawn2.java:374-399` — Alosaurus 8(1-2), TRex 6(1-2), Nastysaurus 6(1-2), Pointysaurus 10(4-8), GammaMetroid 35(4-7), Alien 35(2-3), CaveFisher 35(4-8), Cryolophosaurus 26(4-7), Spyro 5(1-2) + biome defaults
- **Port:** `mining_biome.json` — rat 30(4-8), cave_fisher 10(1-3), molenoid 5(1-2), worms 8/4/2, creeping_horror 5(1-3), scorpion 3(1-2)
- **Fix:** Replace the monster list in `mining_biome.json` with the original roster/weights/groups (CaveFisher 35(4-8) etc.), keeping only entities that exist in the port; track unported entities separately.

### WGEN-013 — Mining: ambient spawns absent
- **Status:** MISSING
- **Original:** `ChunkProviderOreSpawn2.java:410-419` — VelocityRaptor 1(2-4), Dragonfly 2(1-3), Camarasaurus 1(2-4), Baryonyx 2(4-8)
- **Port:** `mining_biome.json` — firefly 5(1-3) only
- **Fix:** Add the four original ambient/creature entries with the listed weights/groups to `mining_biome.json` (for ported entities).

### WGEN-014 — Mining: BasiliskMaze/KyuubiDungeon/EnderKnight dungeon absent; BeeHive relocated
- **Status:** PARTIAL
- **Original:** `OreSpawnWorld:2031-2057` + Mining hooks — BasiliskMaze, KyuubiDungeon, BeeHive, ShadowDungeon, AlienWTF, EnderKnight, LeonNest, generic dungeon
- **Port:** only `shadow_dungeon`, `wtf_alien_dungeon`, `leonopteryx_nest` (each set 26/13) + generic dungeon; BeeHive moved to overworld forests
- **Fix:** Port BasiliskMaze (WGEN-037), KyuubiDungeon and EnderKnightDungeon (WGEN-042) as mining_biome structures; restore BeeHive to Mining or document relocation (WGEN-040).

## Village Mania dimension

### WGEN-015 — Village dimension generates no villages
- **Status:** MISSING
- **Original:** `MapGenMoreVillages.java:11-12` — spacing 9 / separation 7 (vanilla 32/8 → ~12× denser), enabled in dim 82 (`ChunkProviderOreSpawn3`); plus `BiomeManager.addVillageBiome` in `WorldProviderOreSpawn3`
- **Port:** absent — no `minecraft:villages` structure-set override, `orespawn:village_biome` not in any `has_structure/village` tag, no `data/minecraft/tags/worldgen` overrides at all
- **Fix:** Add `orespawn:village_biome` to `data/minecraft/tags/worldgen/biome/has_structure/village_plains.json` (override) and add a `data/minecraft/worldgen/structure_set/villages.json` override (or dimension-scoped set) with spacing 9 / separation 7.

### WGEN-016 — Village: dimension style is a no-op placeholder
- **Status:** PARTIAL
- **Original:** `ChunkProviderOreSpawn3` — overworld noise + lakes + vanilla dungeons/mineshafts/strongholds
- **Port:** `DimensionStyle.java:50-52` — style VILLAGE = pass-through ("identical to DEFAULT for now")
- **Fix:** Add lakes/springs and vanilla underground structures (mineshaft/stronghold/monster rooms) to `village_biome.json` / its tags, mirroring WGEN-010.

### WGEN-017 — Village: mob roster gaps and unverified weights
- **Status:** PARTIAL
- **Original:** `BiomeGenUtopianPlains.setVillageCreatures` — Robot1-5, Jeffery, SpiderDriver, Godzilla, Girlfriend, Boyfriend, cows, Butterfly, LunaMoth, Chipmunk, Cockateil, Tshirt, Coin, Criminal
- **Port:** `village_biome.json` + `dim_village_locals.json` — robots/cows/etc. present; **Jeffery, SpiderDriver, Godzilla, Criminal missing**; giant_robot/band_p added; robot weights UNVERIFIED vs original
- **Fix:** Add spawn entries for Jeffery, SpiderDriver, Godzilla, Criminal once those entities exist; diff robot_1-5 weights/groups against `setVillageCreatures` exact values and align.

### WGEN-018 — Village: DamselInDistress/SpiderHangout/RedAntHangout structures absent
- **Status:** PARTIAL
- **Original:** `OreSpawnWorld.java:118-128` — mosquitos, ants (redfreq 4), apple trees, generic dungeon, DamselInDistress, SpiderHangout, RedAntHangout
- **Port:** `greenhouse` (48/24), `robot_lab` (44/22), `white_house` (48/24) tagged village_biome + generic dungeon — the three original Village structures absent (the three present ones were Islands-dim structures, see WGEN-022)
- **Fix:** Port DamselInDistress, SpiderHangout, RedAntHangout as structures tagged `orespawn:village_biome` with sets approximating their original per-chunk roll densities.

## Islands dimension

### WGEN-019 — Islands: flat-plane terrain replaced by floating-islands noise
- **Status:** DIVERGENT
- **Original:** `ChunkProviderOreSpawn4` + `Island.java:64-79` — flat plane (bedrock y0 + dirt + grass); islands built dynamically by Island/IslandToo entities (small r3-6/d2-4, 1/40 large r6-10/d3-6)
- **Port:** `dimension/islands.json` — `minecraft:floating_islands` noise; island-builder entities additionally spawn (`dim_islands_locals.json`)
- **Fix:** Either restore the flat-plane generator (custom flat noise settings: bedrock+dirt+grass) so entity-built islands are the sole terrain, or remove/retune one of the two systems — currently both run, which neither matches original look nor static-noise intent.

### WGEN-020 — Islands: spawn weights not verified
- **Status:** UNVERIFIED
- **Original:** `setIslandCreatures` — full roster (Dragon, Stinky, CliffRacer, CloudShark, terrors, etc.); exact weights not extracted
- **Port:** `island_biome.json` + locals — full roster present with stated weights
- **Fix:** Unverified because original weight/group numbers were never read. Resolve by extracting `setIslandCreatures` SpawnListEntry args and diffing against the biome JSON.

### WGEN-021 — Islands: ~13 D4 structures absent
- **Status:** MISSING
- **Original:** `OreSpawnWorld.java:134-198` — D4Castle, D4GenericDungeon, D4EnderCastle, D4IncaPyramid, D4RobotLab, D4Mini, D4RubyDungeon, D4CephadromeAltar, D4Greenhouse, D4NightmareRookery, D4StinkyHouse, D4WhiteHouse, Pumpkin, D4Rainbow, D4CloudShark, UnstableAnts placement
- **Port:** absent — nothing tagged `island_biome` (RobotLab/Greenhouse/WhiteHouse were moved to Village)
- **Fix:** Port the D4 structure builders as jigsaw/legacy-piece structures tagged `orespawn:island_biome` with sets matching the original per-chunk roll rates; restore unstable-ant block placement.

### WGEN-022 — Greenhouse/RobotLab/WhiteHouse relocated Islands → Village
- **Status:** DIVERGENT
- **Original:** D4 (Islands) structures (`OreSpawnWorld.java:134-198`)
- **Port:** Village-dim structure sets 48/24, 44/22, 48/24
- **Fix:** Re-tag the three structures to `orespawn:island_biome` (or duplicate into both dims if the Village placement is desired), restoring Islands as their home.

## Crystal dimension

### WGEN-023 — Crystal spawn-block ores: 9 of 11 types are placeholders
- **Status:** PARTIAL
- **Original:** Crystal ore pass — pool of 11 spawn blocks (Urchin, Flounder, Skate, Rotator, Peacock, Fairy, DungeonBeast, Vortex, Rat, Whale, Irukandji), 25+rand(30)/chunk Y>45
- **Port:** `OreSpawnChunkGenerator.getSpawnBlockStates:527-542` — frequencies exact, but 9/11 types emit CRYSTAL_STONE placeholders; only CRYSTAL_FAIRY and CRYSTAL_RAT are real
- **Fix:** Register the 9 missing crystal spawn-block variants (Urchin, Flounder, Skate, Rotator, Peacock, DungeonBeast, Vortex, Whale, Irukandji) with break-to-spawn behavior and substitute them into `getSpawnBlockStates`.

### WGEN-024 — Crystal: extra kyanite/pink-tourmaline veins double-generate
- **Status:** DIVERGENT
- **Original:** n/a — Kyanite *is* CrystalStone; Pink Tourmaline/TigersEye exist only as column formations
- **Port:** `add_crystal_dim_ores.json` — injects `ore_kyanite` (6×size6, Y−32..80) and `ore_pink_tourmaline` (6×size6) as standard veins on top of the column generators
- **Fix:** Remove `add_crystal_dim_ores.json` (or the tourmaline entry at minimum) to eliminate the no-counterpart veins and tourmaline double-generation; keep kyanite only if it stays as a deliberate "Phase 10" addition — document it.

### WGEN-025 — Crystal structures: chest loot only approximated
- **Status:** DIVERGENT
- **Original:** WeightedRandomChestContent lists per structure (FairyTree/RotatorStation/Urchin/HauntedHouse/RoundRotator/BattleTower, maze chests)
- **Port:** `CrystalStructures.fillCrystalChest:838+` — inline ItemStack pickers approximating the loot
- **Fix:** Transcribe each original weighted chest list into a data-driven loot table (`loot_table/chests/crystal_*.json`) and reference them from the structure fill code.

### WGEN-026 — Crystal entry: empty-inventory requirement dropped (Termite portal)
- **Status:** DIVERGENT
- **Original:** `Termite.java` — travel requires empty hand **and completely empty inventory**
- **Port:** `EntityTermite.java:46-48` — inherits `EntityAnt.mobInteract:103-134` empty-hand check only
- **Fix:** Override `mobInteract` in `EntityTermite` to additionally require `player.getInventory()` be empty (the dimension's intended "bring nothing in" rule).

### WGEN-027 — Crystal: redundant structure JSONs risk double generation
- **Status:** DIVERGENT
- **Original:** n/a — maze/towers placed solely by chunk-generator code
- **Port:** `crystal_maze` structure_set (1/0) + `crystal_battle_tower` set (17/8) + placed_features `crystal_maze.json` (1/4), `crystal_battle_tower.json` (1/220), `crystal_tree*.json` exist **in addition to** the code paths (`CrystalStructures.java`, `CrystalMaze`)
- **Fix:** Delete the redundant structure-set/placed-feature JSONs (maze especially: every chunk via code + 1/4 feature + structure set) so each structure has exactly one placement mechanism.

## Chaos dimension

### WGEN-028 — Chaos: nether-style terrain replaced by overworld noise
- **Status:** DIVERGENT
- **Original:** `ChunkProviderOreSpawn6` — nether-noise terrain, 128 high, stone base with nether-style caverns, scraggly trees
- **Port:** `orespawn:inland` overworld noise, style CHAOS = pass-through (`DimensionStyle.java:48-49`); no scraggly trees
- **Fix:** Point `dimension/chaos.json` at nether-like noise settings (e.g. derived from `minecraft:nether` with stone palette, height 128) and add the scraggly-tree pass for CHAOS.

### WGEN-029 — Chaos: spawn roster gaps and unverified weights
- **Status:** PARTIAL
- **Original:** `setChaosCreatures` — ~55 entries
- **Port:** `chaos_biome.json` (37 monsters + others) + `dim_chaos_locals.json` — **Bee, Cassowary, Dragonfly, Peacock, StinkBug, Ostrich, cows, Hydrolisc missing**; ghosts/vampire_butterfly added; per-entry weights UNVERIFIED
- **Fix:** Add the eight missing entity spawn entries (for ported entities); extract original weights from `setChaosCreatures` and align the JSON.

### WGEN-030 — Chaos: veggie/ant features missing; challenge towers added
- **Status:** PARTIAL
- **Original:** `OreSpawnWorld.java:103-107` — butterflies/moths, veggies, ants ×2
- **Port:** generic dungeon 1/16 + `challenge_tower_king/queen` (36/18, no 1.7.10 counterpart — see WGEN-043)
- **Fix:** Add veggie-patch and ant-block features (×2 density) to `chaos_biome.json`, reusing the overworld features from WGEN-006/007.

### WGEN-031 — Chaos: sky/fog constants not compared
- **Status:** UNVERIFIED
- **Original:** `WorldProviderOreSpawn6` — custom fog/sky colors (constants not extracted)
- **Port:** `dimension_type/chaos.json` — `ambient_light: 0.3`, `has_raids: true`
- **Fix:** Unverified because the original provider's fog/sky color values were never read. Resolve by extracting `getFogColor`/sky color from `WorldProviderOreSpawn6` and configuring matching dimension special effects.

## Nether / End additions

### WGEN-032 — Nether: Lavafoam ore + ruby generation absent
- **Status:** PARTIAL
- **Original:** `OreSpawnWorld.generateNether` — Lavafoam ore, ruby, nether ants, nether mosquitos
- **Port:** `add_nether_spawns.json` (spawns only) — no lavafoam/ruby nether features
- **Fix:** Add nether-targeted configured/placed features for Lavafoam and ruby veins and register via a `#minecraft:is_nether` biome modifier.

### WGEN-033 — End: Hospital and Ender Castle structures absent
- **Status:** PARTIAL
- **Original:** `OreSpawnWorld.generateEnd` — End ants, End knights/reapers, Hospital, Ender Castle
- **Port:** `add_end_spawns.json` (spawns only) — no Hospital/EnderCastle structures
- **Fix:** Port Hospital and EnderCastle as structures tagged to End biomes with sets approximating original densities (see also WGEN-042).

## Structures & dungeons

### WGEN-034 — Generic Dungeon: spawner pool swapped, custom loot replaced by vanilla
- **Status:** DIVERGENT
- **Original:** `GenericDungeon.makeDungeon` + lists — spawner pool of 12 (Scorpion, Alien, Cryolophosaurus, WTF?, Kyuubi, Bee, CloudShark, LurkingTerror, TerribleTerror, Rotator, Rat, DungeonBeast); custom level1-5 chest lists
- **Port:** `world/GenericDungeon.java:22-34,121-126` — pool of 11 (Alien, CaveFisher, DungeonBeast, Scorpion, EmperorScorpion, TrooperBug, CaterKiller, Molenoid, Basilisk, StinkBug, Triffid); chest = vanilla `simple_dungeon` loot
- **Fix:** Restore the original 12-mob spawner pool (for ported entities) and transcribe the level1-5 chest lists into orespawn loot tables referenced by dungeon depth/level.

### WGEN-035 — Ruby Dungeon: placement model and loot changed
- **Status:** DIVERGENT
- **Original:** `OreSpawnWorld.addRubyDungeon:1998-2012` — 1/15 chunk, placed at **lava contact** Y5-50, in any dim that called it; chest: CageEmpty/Ruby/Bacon/ButterCandy/full ruby tool+armor set/ThunderStaff (`RubyBirdDungeon.java`)
- **Port:** `OreSpawnChunkGenerator:717-726`, `GenericDungeon.tryPlaceRubyDungeon:69-98` — Crystal dim only, fixed Y10-19 band; chest = vanilla `simple_dungeon`
- **Fix:** Restore lava-adjacent placement at Y5-50 and re-enable for overworld/Utopia callers; create a ruby-gear loot table (cage, ruby, bacon, butter candy, ruby tools+armor, thunder staff) and use it.

### WGEN-036 — DungeonSpawnerBlock: structure pool 50 → 2
- **Status:** PARTIAL
- **Original:** `DungeonSpawnerBlock.java` — on tick spawns 1 of **50** structures (FairyTree → RedAntHangout list)
- **Port:** `RandomDungeonSpawnerBlockEntity.java:63-72` — 2 outcomes (ruby 1/4 else generic)
- **Fix:** Same root as ITEM-020 — expand the outcome table as structures land (WGEN-021/042); restore the 400t timer.

### WGEN-037 — BasiliskMaze absent
- **Status:** MISSING
- **Original:** `BasiliskMaze.java` — maze + Basilisk spawner + chest (diamond 15-25, gold 4-16, iron 2-20, CagedGirlfriend, uranium, titanium, fish, corn dog), Mining dim
- **Port:** absent — no code or JSON match
- **Fix:** Port BasiliskMaze as a mining_biome structure (legacy-piece or code path) with its spawner and a loot table transcribing the listed chest contents.

### WGEN-038 — NightmareDungeon absent
- **Status:** MISSING
- **Original:** `NightmareDungeon.java` — 25×12×25, RTPBlock floor, EmperorScorpion-or-Nightmare spawner, 2 chests of Ultimate/Experience/Amethyst gear + Bertha/Slice; reached via RTP mechanic
- **Port:** absent — only the `NightmareSword.java` item exists
- **Fix:** Port NightmareDungeon generation (triggered from the RTP teleport target, which also requires the ITEM-013 RTPBlock fix) with its spawner and gear-chest loot tables.

### WGEN-039 — Shadow/AlienWTF/LeonNest: frequency equivalence unverified
- **Status:** UNVERIFIED
- **Original:** Mining-dim per-chunk roll placements (`addShadowDungeon` etc., exact roll values not extracted)
- **Port:** structure sets `shadow_dungeon`/`wtf_alien_dungeon`/`leonopteryx_nest`, each spacing 26 / separation 13
- **Fix:** Unverified because the original per-chunk roll odds were never extracted. Resolve by reading the three `add*` methods' roll constants and converting to equivalent random_spread spacing.

### WGEN-040 — BeeHive: relocated Mining → overworld forests
- **Status:** DIVERGENT
- **Original:** `OreSpawnWorld.addBeeHive:2031-2057` — Mining dim, lowest-grass-spot algorithm
- **Port:** `structure_set/beehive.json` + `BeehiveFeature.java` — overworld `#is_forest`/`is_jungle`, set 24/12 + feature 1/60
- **Fix:** Re-tag beehive placement to `orespawn:mining_biome` (optionally keeping forests too, documented), and consider the lowest-grass-spot site selection for fidelity.

### WGEN-041 — MantisNest: placement basis unverified
- **Status:** UNVERIFIED
- **Original:** placed via dungeon spawner / overworld hooks — exact original placement basis not extracted
- **Port:** overworld forests/jungles, set 24/12 + feature 1/80
- **Fix:** Unverified because the original placement call sites/odds were not pinned down. Resolve by locating MantisNest placement in `OreSpawnWorld`/`DungeonSpawnerBlock` and comparing rates/biomes.

### WGEN-042 — ~25+ structure types absent (systemic)
- **Status:** MISSING
- **Original:** placed by OreSpawnWorld/D4 hooks & DungeonSpawnerBlock (loot lists in `GenericDungeon.java`): D4Castle, EnderCastle, IncaPyramid, Mini, CephadromeAltar, NightmareRookery, StinkyHouse, Rainbow, CloudShark dungeon, Pumpkin, BouncyCastle, MonsterIsland, GirlfriendIsland, PlayPool, WaterDragonLair, GoldFishBowl, Graveyard, SpitBugLair, Igloo, KyuubiDungeon, EnderKnightDungeon, Hospital, DamselInDistress, SpiderHangout, RedAntHangout, FrogPond, RubberDuckyPond, QueenAltar(D4), EnormousCastle(Q)
- **Port:** absent — only 17 structure JSONs + 2 dungeon code paths exist
- **Fix:** Port these builders incrementally (legacy-piece transcription like the royal altars), prioritizing those wired to gameplay (KyuubiDungeon/EnderKnightDungeon for Mining, Hospital/EnderCastle for End, D4 set for Islands); register each into the DungeonSpawnerBlock pool (ITEM-020/WGEN-036) as it lands.

### WGEN-043 — Challenge Towers: no 1.7.10 counterpart found
- **Status:** UNVERIFIED
- **Original:** none found in 1.7.10 source (possibly 1.12.2-era content)
- **Port:** `challenge_tower_king/queen` structures, chaos_biome sets 36/18 (KING_TOWER/QUEEN_TOWER 40,4,95)
- **Fix:** Unverified provenance. Resolve by checking 1.12.2 OreSpawn sources (or port docs) for the towers; if intentional new content, document; if not, remove from chaos_biome.

## Trees

### WGEN-044 — DuplicatorTree generator absent
- **Status:** MISSING
- **Original:** `Trees.DuplicatorTree` — sapling/worldgen tree generator
- **Port:** absent — no feature or code (BlockDuplicatorLog re-interprets behavior, ITEM-027)
- **Fix:** Port `Trees.DuplicatorTree` as a `Feature`/TreeGrower wired to the duplicator sapling and the log's random tick.

### WGEN-045 — ExperienceTree generator absent
- **Status:** MISSING
- **Original:** `Trees.ExperienceTree`
- **Port:** absent — no feature/code (EXPERIENCE_SAPLING exists but uses a different/placeholder grower per file 06)
- **Fix:** Port `Trees.ExperienceTree` geometry as the grower for the experience sapling and any worldgen placement it had.

### WGEN-046 — SmallTree / ScragglyTreeWithBranches (overworld variants) absent
- **Status:** PARTIAL
- **Original:** `Trees.java` — overworld SmallTree and ScragglyTreeWithBranches variants
- **Port:** only Islands/Crystal scraggly variants exist
- **Fix:** Port the two overworld variants from `Trees.java` and wire them to their original overworld decoration call sites.

### WGEN-047 — Utopia tree frequencies not verified (Sky/Wind/Round/MagicApple)
- **Status:** UNVERIFIED
- **Original:** `OreSpawnWorld.addOtherTrees` (:2508+), `addHugeTree` (:1830-1863), `addAppleTrees` (:1792) — per-chunk roll values not extracted
- **Port:** `sky_tree.json`/`wind_tree.json` rarity 60, `round_tree.json` rarity 333, `magic_apple_tree.json` rarity 25 (utopia_plains)
- **Fix:** Unverified because original roll constants were never extracted. Resolve by reading the three methods' roll values and converting to equivalent rarity-filter values.

## Portals & teleporters

### WGEN-048 — Village/Islands unreachable in survival (rainbow/unstable ants)
- **Status:** PARTIAL
- **Original:** rainbow/unstable ants obtainable via anthill blocks placed by worldgen (`OreSpawnWorld.addAnts`)
- **Port:** portal code works (`EntityRainbowAnt.java:20` → VILLAGE, `EntityUnstableAnt.java:20` → ISLANDS) but neither ant has a natural spawn entry and their ant blocks never world-generate
- **Fix:** Restore anthill worldgen (WGEN-006) including rainbow/unstable ant blocks, or add natural spawn entries for both ants, so both dimensions are survival-reachable.

### WGEN-049 — Portal landing: tamed pets left behind
- **Status:** PARTIAL
- **Original:** `OreSpawnTeleporter.justPutMe` — scans Y1-180 for solid ground + 3 air; **teleports tamed pets too**
- **Port:** `EntityAnt.findSafeY:142-162` — top-down scan 256→min for solid + 2 air, fallback Y64; no pet co-teleport
- **Fix:** After teleporting the player, find nearby owned/tamed entities (same radius as original) and move them through the same `DimensionTransition`.

### WGEN-050 — Utopia Portal Block: no original counterpart
- **Status:** UNVERIFIED
- **Original:** `PortalBlock.java` is empty in 1.7.10 — travel was entity-based only
- **Port:** `UtopiaPortalBlock.java` — entityInside teleport, fixed y=max(min+1,64), unbreakable (`ModBlocks.java:176`)
- **Fix:** Unverified intent (new addition). Resolve by confirming with the port's design notes whether a placeable Utopia portal is wanted; if yes document it, if not remove the block or hide it from creative.

---

## Totals

| Status | ITEM (file 06) | WGEN (file 07) | Total |
|---|---|---|---|
| DIVERGENT | 33 | 14 | 47 |
| PARTIAL | 19 | 17 | 36 |
| MISSING | 7 | 11 | 18 |
| UNVERIFIED | 6 | 8 | 14 |
| **Total** | **65** | **50** | **115** |

# Findings — Animations/Events, Bugs, Optimizations

Consolidated from `audit_sections/08_animations_events_gui.md` (ANIM), `audit_sections/09_bugs.md` (BUG), `audit_sections/10_optimizations.md` (OPT). Paths: original = `reference_1_7_10_source/sources/danger/orespawn/`, port = `src/main/java/danger/orespawn/` (MHLib under `src/main/java/de/dertoaster/multihitboxlib/`).

Entries: **78 total** — ANIM 20 (DIVERGENT 8 · PARTIAL 10 · MISSING 2) · BUG 31 (CRITICAL 7 · HIGH 6 · MEDIUM 9 · LOW 9) · OPT 27 (HIGH 8 · MEDIUM 11 · LOW 8).

---

## ANIM — Animations, Events, GUI/HUD divergences

### ANIM-001 — Systemic: `wingspeed` → `limbSwingAmount` frequency mistranslation (39 model files)
- **Status:** DIVERGENT
- **Original:** e.g. `ModelButterfly.java:96`, `ModelBee.java:14, 40, 188`, `ModelUrchin.java:14, 34, 155-158` — trig **frequency** multiplied by `this.wingspeed`, a constructor **constant** (usually 1.0; per-reuse, e.g. Mothra 0.2). Idle/hover animations run continuously.
- **Port:** 39 files in `entity/client/` multiply frequency by `limbSwingAmount` (runtime movement amount, ~0 at idle): animations freeze solid when idle/hovering and phase-jitter during speed changes. Verified by grep `ageInTicks \* k \* limbSwingAmount` (hits in parentheses): `KyuubiModel(31)`, `ModelUrchin(21)`, `ModelAttackSquid(20)`, `ModelDungeonBeast(17)`, `TrooperBugModel(14)`, `ModelWaterDragon(10)`, `ModelPitchBlack(10)`, `BeeModel(8)`, `StinkBugModel(8)`, `LeonModel(8)`, `EmperorScorpionModel(7)`, `ModelSeaViper(7)`, `ModelSeaMonster(7)`, `ModelGoldFish(6)`, `SpitBugModel(6)`, `ModelCockateil(5)`, `ScorpionModel(5)`, `ModelCaveFisher(4)`, `ModelBandP(4)`, `ModelHammerhead(4)`, `VelocityRaptorModel(4)`, `ModelCloudShark(4)`, `HydroliscModel(4)`, `MolenoidModel(4)`, `ModelEnderReaper(4)`, `MantisModel(4)`, `ModelEasterBunny(3)`, `LizardModel(3)`, `ModelEnderKnight(3)`, `ModelAlien(3)`, `HerculesBeetleModel(2)`, `OstrichModel(2)`, `TriffidModel(2)`, `LurkingTerrorModel(2)`, `DragonflyModel(2)`, `BrutalflyModel(1)`, `ModelPeacock(1)`, `ButterflyModel(1)`, `TshirtModel(1)`. Includes the table-row cases BeeModel (`BeeModel.java:186-214`, all 8 frequency terms), ModelUrchin (`ModelUrchin.java:137-161`, where `limbSwingAmount` is double-applied: frequency **and** amplitude — the original only scaled amplitude by `f1`), and ButterflyModel (`ButterflyModel.java:96`, shared by Butterfly/LunaMoth/Mothra). Counter-examples done right: `ModelTheKing`/`ModelGodzilla`/`ModelDragon`/`ModelTRex` use a constant `WING_SPEED`/`ANIM_SPEED`.
- **Fix:** In all 39 files, replace `limbSwingAmount` inside `Mth.cos/sin` frequency arguments with a per-model `static final float WING_SPEED` matching the original ctor constant (1.0 default; Mothra 0.2, LunaMoth 0.75); keep `limbSwingAmount` only where the original multiplied **amplitude** by `f1` (e.g. restore Urchin to frequency×const, amplitude×limbSwingAmount).
- **Resolution:** FIXED (2026-06-11, Phase B — wingspeed frequency constants restored in every affected model (incl. Bee, Urchin double-application, shared ButterflyModel via ctor param); zero remaining misuses, see FIX_LOG.md and phase_b_reports/B4_animations.md)

### ANIM-002 — Kraken: fin twitch driver demoted to client-local random
- **Status:** DIVERGENT
- **Original:** `ModelKraken.java:1029-1058`; `Kraken.java:58, 105, 123-132` — fin twitch keyed on **server-synced** `RenderInfo.ri1`.
- **Port:** `entity/client/ModelKraken.java:594-628` (`:128, 619-622`) — `ri1` is now client-local random; tentacle/fin anims otherwise ported, `getAttacking()` synced (`Kraken.java:317, 387, 627-631`). Cosmetic-only divergence.
- **Fix:** Either accept (visual-only), or add a synched int to `Kraken` mirroring original `ri1` and read it in the model.

### ANIM-003 — Rotator: 24-blade tri-axis gyroscope reduced to 3 flat Z-spinning blades
- **Status:** DIVERGENT
- **Original:** `ModelRotator.java:44-80` — each of 3 blade shapes rendered **8×** in a fan; fans spun on X, Y and Z axes via accumulating `ri.rf1 += 2°`.
- **Port:** `entity/client/RotatorModel.java:33-45` — each shape rendered once, all three spun around **Z only** at 1×/1.5×/2×; the signature 24-blade ball is gone.
- **Fix:** In `renderToBuffer`, render each blade part 8 times with 45° pose offsets, and assign the three fans X/Y/Z rotation axes with an accumulating angle field (2°/frame) as in the original.

### ANIM-004 — Mothra: renders at half size (flap-speed identity covered by ANIM-001)
- **Status:** DIVERGENT
- **Original:** `ClientProxyOreSpawn.java:405-411` — Mothra rendered at scale **10.0** with wingspeed 0.2 (slow majestic flap).
- **Port:** `entity/client/MothraRenderer.java:24` — scale **5.0**; `ButterflyModel.java:96` flaps at ~6.5× intended speed (see ANIM-001).
- **Fix:** Change `MothraRenderer` scale from 5.0 to 10.0; restore wingspeed 0.2 via the ANIM-001 constant fix.
- **Resolution:** FIXED (2026-06-11, Phase B — MothraRenderer scale 5.0 corrected to 10.0; wingspeed 0.2 restored via ButterflyModel param, see FIX_LOG.md and phase_b_reports/B4_animations.md)

### ANIM-005 — GiantRobot: walk cycle, attack windmill, and duplicate-part limbs all dropped
- **Status:** DIVERGENT
- **Original:** `ModelGiantRobot.java:150-279` (attack at `:230-240`) — full walk cycle (hip bob + 2-phase legs by re-rendering shared parts at both positions), punch-windmill arms when `getAttacking()!=0`, state in `RenderGiantRobotInfo`.
- **Port:** `entity/client/ModelGiantRobot.java:150-161` — only head look + tiny idle arm sway; `renderToBuffer` (`:164-183`) draws each part once, so the second leg/arm of each pair never renders.
- **Fix:** Re-render each shared limb part at both leg/arm positions inside `renderToBuffer` (or duplicate the parts), reinstate the walk-cycle pose math from the original, and gate windmill arms on the entity's `getAttacking()` accessor (see also ANIM-014 for the missing state holder).

### ANIM-006 — SpiderRobot: only 1 of 8 legs renders
- **Status:** DIVERGENT
- **Original:** `ModelSpiderRobot.java:302-411` — 8 legs posed **and rendered inside** the loop (renders at `:392-410`); jaw snap at `:412-427`.
- **Port:** `entity/client/ModelSpiderRobot.java:259-352` — keeps the 8-iteration pose loop but never renders inside it; `renderToBuffer` (`:372+`) draws once after the loop, so only leg i=7 is visible. Jaw snap ported (`:353-368`); gait simplified to a canned sine (`SpiderRobot.java:221-237`).
- **Fix:** Move the leg `render` call inside the 8-iteration pose loop (pose leg i, render leg i) as the original does, or maintain 8 distinct leg `ModelPart`s posed per index.

### ANIM-007 — Robot2: attack-gated arm poses replaced by constant windmill
- **Status:** PARTIAL
- **Original:** `ModelRobot2.java:133-153` — walk legs + attack-gated random arm poses via `getAttacking()`/`RenderInfo.ri1`.
- **Port:** `entity/client/ModelRobot2.java:129-148` — walk ported; arms windmill constantly at 20°/tick regardless of attack state.
- **Fix:** Gate the arm windmill behind the entity's `getAttacking()` synched accessor; pose arms at a resting angle when idle.

### ANIM-008 — Robot3: attack driver dropped (shares Robot2 pattern)
- **Status:** PARTIAL
- **Original:** `ModelRobot3.java` — `getAttacking()`-gated arm animation (same pattern as Robot2).
- **Port:** `entity/client/ModelRobot3.java` — no `getAttacking()` use (verified by absence in grep of attack-driver coverage, 08 §"Attack-driver coverage").
- **Fix:** Same as ANIM-007: read `getAttacking()` and gate the attack arm pose on it.

### ANIM-009 — Robot4: attack-gated shield/cannon arm anims dropped
- **Status:** PARTIAL
- **Original:** `ModelRobot4.java` — walk + attack-gated shield/cannon arm animations keyed on `getAttacking()`.
- **Port:** `entity/client/ModelRobot4.java:417-459` — walk ported; right arm swings on a fixed always-on cycle, cannon arm frozen at a constant angle; no `getAttacking()` use.
- **Fix:** Restore the `getAttacking()` branch: idle pose when 0, shield raise + cannon aim cycle when attacking.

### ANIM-010 — EntityRat: attack head pose dropped
- **Status:** PARTIAL
- **Original:** `ModelRat.java:116` — attack-vs-idle head bob.
- **Port:** `entity/client/RatModel.java:60-67` — walk + head yaw only.
- **Fix:** Read the rat's `getAttacking()` accessor in `setupAnim` and apply the original attack head-bob branch.

### ANIM-011 — Keybinds: fly-up default key changed (Left Alt → Space)
- **Status:** DIVERGENT
- **Original:** `KeyHandler.java:15-18` — one key "OreSpawn UP/FAST", LWJGL 56 = **Left Alt**.
- **Port:** `client/KeybindHandler.java:18-37, 54-62` — fly_up=**SPACE**, fly_down=LCTRL, special=G (two keys are new additions).
- **Fix:** Decide intentionally: either set fly_up default to `GLFW_KEY_LEFT_ALT` for parity, or document SPACE as a deliberate UX change (SPACE conflicts with vanilla mount-jump/dismount expectations).

### ANIM-012 — Rider flight/jump controls missing for 6 of 7 original mounts
- **Status:** PARTIAL
- **Original:** 7 entities poll `flyup_keystate`: `Dragon`, `Leon`, `Cephadrome` (`Cephadrome.java:786-789`), `Ostrich` (jump/FAST, `Ostrich.java:470-474`), `Elevator`, `ThePrinceTeen`, `ThePrinceAdult`.
- **Port:** Only `Dragon` implements `RideableFlyer` (`Dragon.java:148, 344, 982`; grep `RideableFlyer` = 2 files). Port `Cephadrome.java`, `Ostrich.java`, `Leonopteryx.java` have no `travel`/`tickRidden`/`getControllingPassenger` riding control at all; Elevator/ThePrinceTeen/ThePrinceAdult likewise unhandled (`network/RiderInputPayload.java:31-51` falls back to a generic ±0.15 Δy only for `RideableFlyer`).
- **Fix:** Implement `RideableFlyer` (plus `getControllingPassenger`/`tickRidden`) on Cephadrome, Ostrich (jump/FAST semantics), Leonopteryx, Elevator, ThePrinceTeen, ThePrinceAdult so `RiderInputPayload` dispatch reaches them.
- **Resolution:** PARTIAL (2026-06-11, Phase B — 6 of 7 mounts done via shared `RiderFlightController` + client-predicted riding: Dragon (refactored, BUG-020), Leon/Leonopteryx, Cephadrome, Ostrich, ThePrinceTeen, ThePrinceAdult; see FIX_LOG.md and phase_b_reports/B3_riders.md. Elevator remains on the generic ±0.15 Δy fallback — Phase D backlog.)

### ANIM-013 — HUD: pointed-at-mob health bar reduced to owned-Girlfriend list
- **Status:** PARTIAL
- **Original:** `GirlfriendOverlayGui.java:75-447` — universal crosshair-target health bar (name + `girlfriendgui.png` 182×5 textured bar above hotbar) covering ~45 entity types incl. ownership-gated Girlfriend/Boyfriend, Princes, Dragon, bosses (King `:360-364`, Queen `:365-369`, Mobzilla `:335-339`), robots, big crabs; pointed-entity lookup `:105-114`; bar draw `:432-446`; config gate `:102`.
- **Port:** `client/GirlfriendOverlay.java:27-62` — top-left list of owned Girlfriends within 16 blocks only; flat-color bars; no crosshair targeting, no bosses/mounts/robots. Config gate ported (`:33`).
- **Fix:** Reimplement crosshair-entity resolution (pick entity via `Minecraft.crosshairPickEntity` or a ray trace), restore the textured 182×5 bar centered above the hotbar, and port the ~45-type eligibility list (ownership gates for Girlfriend/Boyfriend).

### ANIM-014 — GiantRobot: `RenderGiantRobotInfo` walk-cycle state holder absent
- **Status:** MISSING
- **Original:** `ModelGiantRobot.java:154-167` — per-entity walk-cycle scratch data in `RenderGiantRobotInfo`.
- **Port:** absent — grep `RenderGiantRobotInfo` = 0 hits (consumed by the walk anim dropped in ANIM-005).
- **Fix:** Recreate `RenderGiantRobotInfo` as a per-entity data class (mirror `entity/client/RenderSpiderRobotInfo.java:3-25`) and feed it from `GiantRobot` tick as part of the ANIM-005 walk-cycle restoration.

### ANIM-015 — Crystal Furnace: cook speed 1.5× and signature crystal fuels inert
- **Status:** PARTIAL
- **Original:** `TileEntityCrystalFurnace.java:174-179` — cook = **150 ticks**; custom fuel table (`:226-277`): lava/CrystalCoal **20000**, CrystalTreeLog 800, CrystalPlanks 400, etc.
- **Port:** `gui/CrystalFurnaceBlockEntity.java:34` — cook = **100 ticks**; fuel via vanilla `fuel.getBurnTime(RecipeType.SMELTING)` (`:183`); no burn-time registration anywhere for Crystal Coal/Log/Planks (grep `getBurnTime|FurnaceFuel` = only this file), so they don't burn.
- **Fix:** Register burn times for Crystal Coal (20000), Crystal Tree Log (800), Crystal Planks (400) via `IItemExtension#getBurnTime` overrides or a `FurnaceFuelBurnTimeEvent` handler; set cook time back to 150 (or document 100 as an intended buff).

### ANIM-016 — Seasonal content: Halloween/Valentine's/Easter gates all absent
- **Status:** MISSING
- **Original:** `OreSpawnMain.java:4518-4521` — `GregorianCalendar` at init: Oct 31 → Ghost/GhostSkelly biome spawns (`:4521-4566`); Feb 14 → `valentines_day=1` (`:4567-4569`) consumed by `MyValentineTarget` AI (`Girlfriend.java:161-162`, `MyValentineTarget.java:47-50`); Apr 20 → `easter_day=1` gating EasterBunny spawns (`:4681`).
- **Port:** absent — grep `Calendar|Valentine|easter` = 0; Ghost/GhostSkelly/EasterBunny have plain config-gated spawns (`ModSpawnControl.java:57-58, 89`).
- **Fix:** Add a date check (`LocalDate.now()`) evaluated at server start/spawn-check time; gate Ghost/GhostSkelly and EasterBunny spawn rules on it and port `MyValentineTarget` (Girlfriend kiss-target goal) activated on Feb 14.

### ANIM-017 — ExperienceCatcher: conversion mechanic entirely different
- **Status:** DIVERGENT
- **Original:** `ExperienceCatcher.java:29-62` — catches **one** orb (value ≥3, 80% chance) → drops Bottle o' Enchanting + string + stick; item consumed.
- **Port:** `item/ExperienceCatcher.java:24-61` — vacuums **all** orbs in r=3 → pays out emeralds/gold/diamonds by XP total.
- **Fix:** Either restore original semantics (single orb ≥3 value, 80% roll, Bottle o' Enchanting + string + stick, consume item) or sign off the redesign explicitly.

### ANIM-018 — Per-mob spawn-disable flags: ~42 of ~100 enforced
- **Status:** PARTIAL
- **Original:** ~100 `XxxEnable` config flags gate `EntityRegistry.addSpawn` (grep `Enable = config.get` = 100, e.g. `OreSpawnMain.java:1519`).
- **Port:** `ModSpawnControl.java:53-101` maps **42** entity types; cancellation via `FinalizeSpawnEvent`+`EntityJoinLevelEvent` (`:109-135`). Bosses/water mobs unmapped; `KRAKEN_ENABLE` does not exist in port config at all (grep = 0).
- **Fix:** Extend the `ModSpawnControl` map to cover all ~100 original flags (add the missing config entries, incl. `KRAKEN_ENABLE`, which `KrakenRevengeHandler` should also respect).

### ANIM-019 — Creeper Repellent: PurplePower target omitted
- **Status:** PARTIAL
- **Original:** `CreeperRepellent.java:94-126` — repels Creeper + EntityAnt + **PurplePower**; `KrakenRepellent.java:93-109` repels Kraken + EntityAnt.
- **Port:** `block/RepellentBlock.java:26-47` + `ModBlocks.java:131-136` — kraken predicate = Kraken‖EntityAnt ✓; creeper predicate = Creeper‖EntityAnt — PurplePower missing.
- **Fix:** Add `|| e instanceof PurplePower` to the creeper-repellent predicate in `ModBlocks.java:131-136`.

### ANIM-020 — Dimension teleporter: 1 of 5+ destinations implemented
- **Status:** PARTIAL
- **Original:** `OreSpawnTeleporter.java:22-96` — custom placement for 5 dims (mining/crystal/chaos/village/islands + utopia).
- **Port:** only `block/UtopiaPortalBlock.java:23-50` (entityInside → utopia/back); no teleporter/portal code for the other dimensions exists.
- **Fix:** Implement portal blocks + placement logic for the remaining dimensions (mirror `UtopiaPortalBlock`), coordinating with the dimension-slice audit on which dims actually exist in the port.

---

## BUG — Port-code bugs (from 09_bugs.md)

### BUG-001 — MHLib `EntityEventHandler` on MOD bus with GAME-bus events
- **Severity:** CRITICAL
- **Resolution:** FIXED (2026-06-11, Phase A — see FIX_LOG.md)
- **Location:** `de/dertoaster/multihitboxlib/EntityEventHandler.java:15` (handlers at 19, 35, 44)
- **Scenario:** `@EventBusSubscriber(bus = MOD)` subscribing to `EntityEvent.Size`/`PlayerEvent.StartTracking`/`StopTracking` (GAME-bus) → `IllegalArgumentException` during mod construction → launch crash (or, if not classloaded, hitbox-resize and tracking hooks silently dead).
- **Fix:** Remove `bus = EventBusSubscriber.Bus.MOD` so the class registers on the GAME bus.

### BUG-002 — MHLib `GameEventHandler` on MOD bus with a GAME-bus event
- **Severity:** CRITICAL
- **Resolution:** FIXED (2026-06-11, Phase A — see FIX_LOG.md)
- **Location:** `de/dertoaster/multihitboxlib/GameEventHandler.java:9` (handler at 13)
- **Scenario:** Same mismatch as BUG-001 for `PlayerEvent.PlayerLoggedInEvent` — startup crash, or asset-synch enforcement never fires on login.
- **Fix:** Remove `bus = EventBusSubscriber.Bus.MOD` from the annotation.

### BUG-003 — `EntityRat`: `UUID.fromString("")` ticking-entity crash for spawner/summoned rats
- **Severity:** CRITICAL
- **Resolution:** FIXED (2026-06-11, Phase A — see FIX_LOG.md)
- **Location:** `danger/orespawn/entity/EntityRat.java:139` (root cause at 110–116)
- **Scenario:** NBT lacking `MyOwner` yields `""` from `getString`; `customServerAiStep` then runs `UUID.fromString("")` → `IllegalArgumentException` → server crash. Crystal-dimension dungeons place rat spawners, so this fires in normal play; such rats also never despawn.
- **Fix:** In `readAdditionalSaveData`, treat empty/`"null"` strings as no owner (`if (s.isEmpty()) myOwner = null;`) and wrap `UUID.fromString` in try/catch as `Fairy.java:191-195` does.

### BUG-004 — Prince growth chain calls `tame(null)` when owner offline — NPE crash
- **Severity:** CRITICAL
- **Resolution:** FIXED (2026-06-11, Phase A — see FIX_LOG.md)
- **Location:** `danger/orespawn/entity/ThePrince.java:241`, `ThePrinceTeen.java:207, 246`, `ThePrinceAdult.java:249`
- **Scenario:** `getPlayerByUUID` returns null for an offline owner; `TamableAnimal.tame(null)` NPEs. `ThePrince.customServerAiStep:230` auto-transforms once counters pass thresholds, so a chunk-loaded prince crashes the server the moment its owner logs out.
- **Fix:** Null-check the resolved player; on null, fall back to `setOwnerUUID(this.getOwnerUUID()); setTame(true, true);` instead of `tame(player)` at all four sites.

### BUG-005 — `TheQueen.doHurtTarget` can `discard()` a ServerPlayer
- **Severity:** CRITICAL
- **Resolution:** FIXED (2026-06-11, Phase A — see FIX_LOG.md)
- **Location:** `danger/orespawn/entity/TheQueen.java:486–502`
- **Scenario:** When the health-tracked victim hits 0 HP the Queen calls `discard()`; for a player this removes the entity without the death pipeline — no death screen/drops/respawn, ghost connection on the server.
- **Fix:** Restrict the discard path to non-player mobs; for players (and ideally all victims) apply lethal `hurt`/`die(damageSource)` instead.

### BUG-006 — `Godzilla.doJumpDamage` uses `genericKill` — kills Creative/Spectator players
- **Severity:** CRITICAL
- **Resolution:** FIXED (2026-06-11, Phase A — see FIX_LOG.md)
- **Location:** `danger/orespawn/entity/Godzilla.java:422–441`
- **Scenario:** Landing shockwave uses `damageSources().genericKill()` (the `/kill` source, bypasses invulnerability) — creative/spectator players near the landing die outright, spectators even through walls.
- **Fix:** Use `damageSources().mobAttack(this)` so vanilla invulnerability rules apply.

### BUG-007 — `SpiderRobot` save/load no-ops without `super` — living-entity data lost
- **Severity:** CRITICAL
- **Resolution:** FIXED (2026-06-11, Phase A — see FIX_LOG.md)
- **Location:** `danger/orespawn/entity/SpiderRobot.java:199–202`
- **Scenario:** Empty `addAdditionalSaveData`/`readAdditionalSaveData` overrides drop the `super` call: Health, effects, attribute modifiers, PersistenceRequired, equipment, leash never persist. Half-killed robots reload at full HP; name-tagged ones can despawn.
- **Fix:** Call `super.addAdditionalSaveData(tag)` / `super.readAdditionalSaveData(tag)` in both overrides.

### BUG-008 — `EntityWormLarge` respawns its 40-worm brood on every world reload
- **Severity:** HIGH
- **Resolution:** VERIFIED-CORRECT (2026-06-11, Phase A — see FIX_LOG.md)
- **Location:** `danger/orespawn/entity/EntityWormLarge.java:30` (spawn loop at 133–145)
- **Scenario:** `wormsSpawned` not written to NBT; each chunk reload with the large worm alive spawns another 40 medium/small worms — a few relogs produce hundreds of entities (entity bomb, TPS loss).
- **Fix:** Persist `wormsSpawned` in `addAdditionalSaveData`/`readAdditionalSaveData`.

### BUG-009 — `ModSpawnControl.NATURAL_SPAWNS`: non-thread-safe set mutated from worker threads
- **Severity:** HIGH
- **Resolution:** FIXED (2026-06-11, Phase A — see FIX_LOG.md)
- **Location:** `danger/orespawn/ModSpawnControl.java:42`
- **Scenario:** WeakHashMap-backed set mutated from `FinalizeSpawnEvent` on chunk-gen worker threads while the server thread touches it in `EntityJoinLevelEvent` — concurrent rehash can corrupt the map (infinite loop in `getEntry`) or throw CME.
- **Fix:** Wrap with `Collections.synchronizedSet(...)`, or key off entity UUIDs in a `ConcurrentHashMap`-backed set.

### BUG-010 — `ThePrince`/`ThePrincess`: permanent `noPhysics` after being hurt
- **Severity:** HIGH
- **Resolution:** FIXED (2026-06-11, Phase A — see FIX_LOG.md)
- **Location:** `danger/orespawn/entity/ThePrince.java:144` (set at 179, 211), `ThePrincess.java:114` (set at 130, 153)
- **Scenario:** `tick()` maps activity 2 → `noPhysics`; `hurt()` sets activity 2 and nothing resets it except the owner sit-toggle. A wild/unattended prince that takes one hit sinks through terrain into the void; activity persists (`SpyroActivity`) so it survives relogs.
- **Fix:** Reset activity to 1 when the attack/target ends (mirror the `setAttacking(0)` path), or stop mapping activity 2 to `noPhysics`.

### BUG-011 — `Kraken` force-sets a caught player's position/motion every tick
- **Severity:** HIGH
- **Resolution:** FIXED (2026-06-11, Phase A — see FIX_LOG.md)
- **Location:** `danger/orespawn/entity/Kraken.java:285–301`
- **Scenario:** Server-side `setPos`/`setDeltaMovement` on a client-authoritative player every tick → violent rubber-banding and "moved too quickly/wrongly" kicks on strict servers.
- **Fix:** For `ServerPlayer`, use `connection.teleport(...)` (or make the player a passenger of the tentacle part) instead of raw `setPos`.

### BUG-012 — `TheKing.hurt` silently deletes small attackers
- **Severity:** HIGH
- **Resolution:** VERIFIED-CORRECT (2026-06-11, Phase A — see FIX_LOG.md)
- **Location:** `danger/orespawn/entity/TheKing.java:951–953`
- **Scenario:** Any non-player `Monster` under 3.0 bb that damages TheKing is `discard()`ed — no death event, drops, or `LivingDeathEvent`; players' tamed mobs and other mods' minions are wiped with no feedback.
- **Fix:** Replace `discard()` with lethal damage (`hurt(genericKill)` on non-players) or restrict the wipe to OreSpawn's own minion classes.

### BUG-013 — Worldgen cooldowns are static, cross-dimension, and cross-thread
- **Severity:** HIGH
- **Resolution:** FIXED (2026-06-11, Phase A — see FIX_LOG.md)
- **Location:** `danger/orespawn/world/OreSpawnChunkGenerator.java:107`, `world/CrystalStructures.java:68`
- **Scenario:** Static `AtomicInteger recentlyPlaced` shared across all dimension instances and parallel worldgen threads: Mining-dim cooldowns suppress Utopia/Crystal dungeons; check-then-act race makes structure distribution non-deterministic per seed.
- **Fix:** Make `recentlyPlaced` an instance field (per-generator) or key the cooldown by dimension; derive randomness from chunk-seeded random for determinism.

### BUG-014 — `TheKing`: combat/AI state not persisted
- **Severity:** MEDIUM
- **Location:** `danger/orespawn/entity/TheKing.java` (fields `lightningStreamCount`, `iceStreamCount`, `ticker`, `backoffTimer`, `largeEntityDetected`, `attackDamage`, `revengeTarget`, `headEntityFound`)
- **Scenario:** None saved to NBT — relogging mid-fight resets attack streams, backoff, and the buffed `attackDamage`; the boss "forgets" the fight.
- **Fix:** Serialize `attackDamage`, stream counters, and `backoffTimer` in `addAdditionalSaveData`/`readAdditionalSaveData`.

### BUG-015 — `TheKing.dropCustomDeathLoot`: up to 300 random-registry item drops
- **Severity:** MEDIUM
- **Location:** `danger/orespawn/entity/TheKing.java:1321–1339`
- **Scenario:** Two `while (j < 150)` loops draw random IDs from the item/block registries (any mod's items, technical items included) — one kill dumps ~300 item entities: lag spike + exploit-grade loot.
- **Fix:** Replace registry sampling with a curated loot table (or a small whitelisted item pool) and cap total drops to a sane count (e.g. ≤32).

### BUG-016 — `Godzilla`: combat state not persisted
- **Severity:** MEDIUM
- **Location:** `danger/orespawn/entity/Godzilla.java` (fields `ticker`, `streamCount`, `largeUnknownDetected`, `jumped`, `jumpTimer`, `headFound`)
- **Scenario:** Fire-stream and jump state reset on relog; a mid-air "jumped" Godzilla reloads with `jumped=false` and never runs its landing-damage path, leaving stale state.
- **Fix:** Persist `jumped`/`jumpTimer`/`streamCount` in NBT.

### BUG-017 — `TheQueen.mood` not persisted — angry queen reloads happy
- **Severity:** MEDIUM
- **Location:** `danger/orespawn/entity/TheQueen.java:179` (mood logic ~707–720)
- **Scenario:** With `QUEEN_ALWAYS_MAD` off, relogging during a fight resets the Queen to placid — players can defuse aggression by relogging.
- **Fix:** Write/read `mood` in the existing NBT methods.

### BUG-018 — `Kraken` weather lock fights the vanilla weather cycle and isn't persisted
- **Severity:** MEDIUM
- **Location:** `danger/orespawn/entity/Kraken.java:57` (decrement), `:135` (`setWeatherParameters`)
- **Scenario:** Every `weatherSet` expiry re-forces a thunderstorm, overriding `/weather clear` and other mods; timer not saved so relog re-triggers immediately; multiple Krakens each re-arm independently.
- **Fix:** Set weather once per Kraken (persist a flag in NBT) and/or check `level.isThundering()` before forcing.

### BUG-019 — `EntityVortex`: server-side push/launch velocities on players not synced
- **Severity:** MEDIUM
- **Location:** `danger/orespawn/entity/EntityVortex.java:184–187` (pull), `:244–273` (`skywardLaunch`)
- **Scenario:** `push()`/`setDeltaMovement()` on a `ServerPlayer` without `hurtMarked = true` sends no motion packet — the signature tornado pull/launch works only erratically (piggy-backing knockback from coincident `doHurtTarget`).
- **Fix:** Set `victim.hurtMarked = true` for players after modifying `deltaMovement` in both code paths.

### BUG-020 — `Dragon` ridden flight moved server-side while the client owns vehicle movement
- **Severity:** MEDIUM
- **Location:** `danger/orespawn/entity/Dragon.java:343–349` (server `move(SELF)`), `:355–360` (`travel` early-return)
- **Scenario:** With a controlling player passenger, vanilla expects the riding client to move the vehicle; the port moves the dragon in server `aiStep` instead → server and client fight over position → jitter/rubber-banding while flying, worse with latency.
- **Fix:** Implement rider movement in `travel()`/`tickRidden` (client-predicted, like vanilla horses); keep server `move(SELF)` only for the riderless AI path.
- **Resolution:** FIXED (2026-06-11, Phase B — see FIX_LOG.md and phase_b_reports/B3_riders.md)

### BUG-021 — Crystal structures truncated at chunk borders (silently)
- **Severity:** MEDIUM
- **Location:** `danger/orespawn/world/CrystalStructures.java` (large features, e.g. FairyCastleTree); swallow-catch at `OreSpawnChunkGenerator.java:264`
- **Scenario:** Feature-style generation writes outside the 3×3 writable region during `applyBiomeDecoration`; exceptions are caught and ignored, so big trees/castles generate with sheared-off edges depending on chunk order.
- **Fix:** Convert oversized pieces to Jigsaw/Structure pieces with proper bounding boxes, or clamp placement to the writable region.

### BUG-022 — `EntityVortex` scans all nearby LivingEntities every tick on both sides
- **Severity:** MEDIUM
- **Location:** `danger/orespawn/entity/EntityVortex.java:101` (`tick()`), `:176` (`customServerAiStep`)
- **Scenario:** 32×20×32 `getEntitiesOfClass` + per-candidate LoS raycast every tick on server *and* client (client only needs it for smoke particles) — several vortexes measurably hit frame and tick time.
- **Fix:** Cache the target for ~10 ticks; gate the client particle check behind a cheap distance test. (Perf side covered by OPT-004.)

### BUG-023 — `Mothra`: movement/heal state not persisted
- **Severity:** LOW
- **Location:** `danger/orespawn/entity/Mothra.java:41–45`
- **Scenario:** `lastX/Y/Z`, `stuckCount`, `healthTicker` reset on relog; stuck-detection and regen restart. Minor hiccup only.
- **Fix:** Persist `healthTicker` if regen cadence matters; otherwise accept as-is.

### BUG-024 — `GiantRobot.reloadTicker` not persisted
- **Severity:** LOW
- **Location:** `danger/orespawn/entity/GiantRobot.java:38`
- **Scenario:** Relog during the rocket reload window lets the robot fire immediately. Balance-only.
- **Fix:** Save the ticker in NBT.

### BUG-025 — `Kraken.enchantToolSilk` rolls Silk Touch I–V (illegal levels)
- **Severity:** LOW
- **Location:** `danger/orespawn/entity/Kraken.java:501`
- **Scenario:** Drops can carry Silk Touch above max level 1; anvils/grindstones and validation mods treat the stack as illegal.
- **Fix:** Clamp the rolled level to `enchantment.getMaxLevel()`.

### BUG-026 — `Kraken`: `hitByPlayer`/`callReinforcements` not persisted
- **Severity:** LOW
- **Location:** `danger/orespawn/entity/Kraken.java` (fields near top)
- **Scenario:** Relogging mid-fight re-arms the reinforcement wave — a second squad can spawn.
- **Fix:** Persist both flags in NBT.

### BUG-027 — `TheQueen.myCanSee` truncates negative coordinates toward zero
- **Severity:** LOW
- **Location:** `danger/orespawn/entity/TheQueen.java:1189–1230`
- **Scenario:** `(int)(startx + dx)` rounds toward zero, so in negative-coordinate quadrants the LoS ray samples the wrong block column — Queen occasionally sees through (or fails to see past) corners.
- **Fix:** Use `Mth.floor`/`BlockPos.containing` for the sample positions.

### BUG-028 — `RTPBlock` spawns particles via `Level.addParticle` on the server — never visible
- **Severity:** LOW
- **Location:** `danger/orespawn/block/RTPBlock.java:77–81`
- **Scenario:** `entityInside` returns early on the client then calls `level.addParticle` on the server `Level` (a no-op) — the teleport burst never shows (sound works).
- **Fix:** Use `((ServerLevel) level).sendParticles(...)`.

### BUG-029 — `CrystalFurnaceBlockEntity` consumes bucket fuels without returning the container
- **Severity:** LOW
- **Location:** `danger/orespawn/gui/CrystalFurnaceBlockEntity.java:182–189`
- **Scenario:** Fueling with a lava bucket destroys the bucket (`fuel.shrink(1)`), unlike the vanilla furnace which leaves an empty bucket.
- **Fix:** After shrinking, place `fuel.getCraftingRemainingItem()` into the fuel slot if it's empty.

### BUG-030 — `EntityWormMedium`: `upcount`/`downcount` not persisted
- **Severity:** LOW
- **Location:** `danger/orespawn/entity/EntityWormMedium.java:23–24`
- **Scenario:** Burrow/emerge cycle resets on relog; purely cosmetic.
- **Fix:** Persist both counters, or accept.

### BUG-031 — `EntityVortex.tick` heals client-side
- **Severity:** LOW
- **Location:** `danger/orespawn/entity/EntityVortex.java:117–119`
- **Scenario:** `heal(1.0f)` runs on both sides with independent RNG — client's local health copy briefly diverges (visual only; next health sync corrects).
- **Fix:** Gate the heal behind `!level().isClientSide`.

---

## OPT — Optimization proposals (from 10_optimizations.md)

### OPT-001 — MHLib `getHitboxProfile()` registry lookup per part per tick / per bone per frame
- **Impact:** HIGH
- **Location:** `de/dertoaster/multihitboxlib/api/IMultipartEntity.java:345-357` (impl), `init/MHLibDatapackLoaders.java:34-36`; hot call sites `IMultipartEntity.java:176, 210, 268, 370-391`, `client/IBoneInformationCollectorLayerCommonLogic.java:36-52`
- **Cost:** `BuiltInRegistries.ENTITY_TYPE.getKey()` + datapack-registry map lookup + `Optional` alloc × part count × tick rate (server) and × bone count × frame rate (client) for every multipart boss.
- **Proposal:** Cache `Optional<HitboxProfile>` in a field on the entity (via `IMHLibFieldAccessor`), populated in `mhlibOnConstructor`, invalidated on datapack reload.
- **Behavior:** neutral (identical results; only `/reload` invalidation must be wired)

### OPT-002 — MHLib sends a full multipart update packet every tick even when nothing moved
- **Impact:** HIGH
- **Location:** `de/dertoaster/multihitboxlib/mixin/entity/MixinServerEntity.java:24-33`; payload at `network/server/SPacketUpdateMultipart.java:29-31, 67-77`
- **Cost:** Per tick × per multipart entity × per tracking player: full pos/rot/size for all parts + `ArrayList` + one `PartDataHolder` record per part per tick.
- **Proposal:** Track last-sent part transforms; skip the send when no part moved beyond epsilon and no part data is dirty (or throttle unchanged syncs to every 10 ticks as keepalive).
- **Behavior:** neutral (positions identical for idle bosses; strict change-only send alters nothing visible)

### OPT-003 — MHLib master client streams `CPacketBoneInformation` continuously regardless of change
- **Impact:** HIGH
- **Location:** `de/dertoaster/multihitboxlib/api/IMultipartEntity.java:283-319` (`updateSynching`); per-frame collection `client/IBoneInformationCollectorLayerCommonLogic.java:34-61`; builder allocs `network/client/CPacketBoneInformation.java:73-165`
- **Cost:** Per-tick C2S packet per mastered multipart entity + per-frame `synchronized tryAddBoneInformation` (`mixin/entity/MixinLivingEntity.java:134`) + Optional/HashSet churn per bone.
- **Proposal:** Diff bone info against the last sent packet and only send on change, plus a low-rate keepalive so the 10-tick master-timeout (`updateSynching:288`) doesn't rotate masters.
- **Behavior:** affecting (server-side hitbox positions for static poses update less often — no visible difference; keepalive required to preserve master-election behavior)

### OPT-004 — EntityVortex: up to 3 ungated AABB scans per tick on both sides
- **Impact:** HIGH
- **Location:** `danger/orespawn/entity/EntityVortex.java:101` (`tick()`, client+server), `:176` (`customServerAiStep`), scan at `:277-281`
- **Cost:** 2–3 `getEntitiesOfClass(LivingEntity, inflate(16,10,16))` + full list sort per vortex per tick — worst ungated per-tick scan in the port.
- **Proposal:** Cache the found target in a field, rescan every 5 ticks, reuse the result between `tick()` (particles only need "has target") and `customServerAiStep()`.
- **Behavior:** affecting (pull/aggro and particle-onset latency goes from 0 to ≤5 ticks — flag for sign-off)

### OPT-005 — GirlfriendOverlay: entity scan + string concat every rendered frame
- **Impact:** HIGH
- **Location:** `danger/orespawn/client/GirlfriendOverlay.java:39-41` (per-frame AABB + `getEntitiesOfClass`), `:47`, `:59` (string concats)
- **Cost:** Per frame (60–240 Hz): AABB alloc, predicate entity query, list alloc, 2+ string allocs per girlfriend.
- **Proposal:** Move the scan to a `ClientTickEvent.Post` handler refreshing a cached list (with pre-formatted name/health strings) every 10 ticks; `render` only draws the cache.
- **Behavior:** neutral (HUD data at most 0.5 s stale — cosmetic latency only)

### OPT-006 — Kraken obstruction probe: 95 block reads every server tick, unconditionally
- **Impact:** HIGH
- **Location:** `danger/orespawn/entity/Kraken.java:188` (call), `:339-360` (`applyObstructionAvoidance`: 19×5 grid, `new BlockPos` per probe)
- **Cost:** ~95 `getBlockState` + ~95 `BlockPos` allocs per Kraken per tick; Krakens spawn in packs of 1–10 (`KrakenRevengeHandler`, reinforcements at `Kraken.java:247-258`).
- **Proposal:** Run the probe every 4–5 ticks with one `BlockPos.MutableBlockPos`, scaling the lift impulse by the interval to keep net buoyancy identical.
- **Behavior:** affecting (obstruction-response latency up to 5 ticks; impulse scaling keeps average motion equal — flag)

### OPT-007 — Worm chain: duplicate player/segment scans twice per tick with allocations
- **Impact:** HIGH
- **Location:** `danger/orespawn/entity/EntityWormLarge.java:104, 165`; `EntityWormSmall.java:87, 139`; worst `EntityWormMedium.java:90-97, 150-156` (`getNearestEntity` + fresh `TargetingConditions` + AABB, twice per tick)
- **Cost:** 2 player-list scans per worm per tick; Medium adds 2 entity scans + 2 `TargetingConditions` allocs per tick.
- **Proposal:** Hoist `TargetingConditions.forNonCombat()` to a `static final`; compute nearest-player/nearest-small-worm once per tick and share between `aiStep` and `customServerAiStep`.
- **Behavior:** neutral (hoist + same-tick sharing); any further throttling (every 2–4 ticks) is affecting (tracking latency) — flag separately

### OPT-008 — Crystal-dimension terrain rewrite scans the full world column with a BlockPos per block
- **Impact:** HIGH
- **Location:** `danger/orespawn/world/OreSpawnChunkGenerator.java:302-336` (`replaceTerrain`: ≈98k `new BlockPos` + `getBlockState` per chunk), `:345-377` (`fillShallowWater`: second column scan)
- **Cost:** ~100k+ short-lived `BlockPos` allocations and full-height state reads per chunk on worldgen worker threads.
- **Proposal:** Reuse one `BlockPos.MutableBlockPos` per column; start the downward scan at `chunk.getHeight(WORLD_SURFACE_WG, x, z)`; merge `fillShallowWater` into the same column walk.
- **Behavior:** neutral (identical block output; heightmap start is safe — everything above surface is air)

### OPT-009 — ~35 entity classes reset MOVEMENT_SPEED base value every tick
- **Impact:** MEDIUM
- **Location:** Representative: `Godzilla.java:222`, `ThePrinceAdult.java:121`, `Baryonyx.java:68`, `EntityKyuubi.java:64`, `Basilisk.java:92`, `Camarasaurus.java:85`, `Girlfriend.java:118`, `Alien.java:102`, `Dragon.java:284`, `Boyfriend.java:119`, `Nastysaurus.java:72`, `Cryolophosaurus.java:66`, `Pointysaurus.java:83`, `BandP.java:92`, `EntityRat.java:85`, `EntityRubberDucky.java:120`, `EntitySpyro.java:129`, `EasterBunny.java:65`, `EntityLeafMonster.java:72`, `EntityMolenoid.java:91`, `ThePrinceTeen.java:120`, `CreepingHorror.java:66`, `DungeonBeast.java:79`, `EntityLeon.java:298`, `Cephadrome.java:127`, `Crab.java:98-99`, `Peacock.java:66`, `Cassowary.java:57`, `Alosaurus.java:84`, `EntityStinky.java:145`, `TRex.java:79`, etc. (only `SeaViper.java:147-149`/`WaterDragon.java:149-151` are genuinely dynamic)
- **Cost:** Attribute-map lookup per entity per tick across ~140 entity types (no sync spam — `setBaseValue` early-exits on equal — but the lookup is pure waste for constants).
- **Proposal:** Delete the per-tick call for constant speeds and set the value in `createAttributes()`; for water/land mirrors (SeaViper, WaterDragon, Crab) cache the `AttributeInstance` and only call `setBaseValue` on medium change.
- **Behavior:** neutral

### OPT-010 — Godzilla allocates a Vec3 array + Vec3 per part every tick
- **Impact:** MEDIUM
- **Location:** `danger/orespawn/entity/Godzilla.java:216-220`
- **Cost:** `new Vec3[allParts.length]` + one `Vec3` per part per tick (server + client).
- **Proposal:** Replace with three reusable `double[]` fields (or store prev positions on the parts themselves).
- **Behavior:** neutral

### OPT-011 — ~37 sound getters allocate a new SoundEvent + ResourceLocation on every call
- **Impact:** MEDIUM
- **Location:** Representative: `GiantRobot.java:157-171`, `Robot1.java`–`Robot5.java` (3 each), `ThePrincess.java`, `ThePrinceTeen.java`, `PitchBlack.java` (3 each), `SpiderRobot.java:149-150, 187-188`, `Ostrich.java`, `VelocityRaptor.java`, `Fairy.java`, `Lizard.java`, `Ghost.java`, `GhostSkelly.java`
- **Cost:** `SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(...))` per ambient/hurt/death sound query (ambient polled periodically per mob); two allocs + string handling per call; bypasses the 100 registered `ModSounds` entries.
- **Proposal:** Replace with the corresponding `ModSounds.X.get()` holder (or a `static final SoundEvent` per class).
- **Behavior:** neutral (same sound id; registered events also serialize properly to clients)

### OPT-012 — Oversized-weapon culling mixin: 8 deferred-holder item checks per entity per frame
- **Impact:** MEDIUM
- **Location:** `danger/orespawn/mixin/EntityCullingMixin.java:16-33`
- **Cost:** Inside `getBoundingBoxForCulling` for every entity (vanilla included) every frame: `getMainHandItem` + up to 8 `ModItems.X.get()` + `is()` checks + `inflate(5.0)` AABB alloc on match.
- **Proposal:** Replace the 8 checks with a single item-tag test (`mainHand.is(OVERSIZED_WEAPONS_TAG)`) or a lazily-built `static Set<Item>`; `stack.isEmpty()` early-out already exists.
- **Behavior:** neutral

### OPT-013 — Twelve big-mob renderers disable frustum culling entirely
- **Impact:** MEDIUM
- **Location:** `entity/client/QueenRenderer.java:50-52`, plus `shouldRender` overrides in `TheKingRenderer`, `GodzillaRenderer`, `GodzillaHeadRenderer`, `KingHeadRenderer`, `QueenHeadRenderer`, `KrakenRenderer`, `MothraRenderer`, `DungeonBeastRenderer`, `SeaMonsterRenderer`, `PitchBlackRenderer`, `LeonopteryxRenderer`
- **Cost:** Full model render every frame whenever the entity is loaded, even fully off-screen — for the largest models in the mod.
- **Proposal:** Keep culling but size `Entity.getBoundingBoxForCulling()` (or `shouldRender` calling super with an inflated AABB, ~30 blocks) to the real part envelope instead of returning `true` unconditionally.
- **Behavior:** neutral (visually, if the inflated box covers the part envelope — flag for visual verification on wing/tail extremes)

### OPT-014 — Alien torch scan probes 4,913 blocks per scan (docs claim 256)
- **Impact:** MEDIUM
- **Location:** `danger/orespawn/entity/AlienTorchSeekGoal.java:134-153` (radius 8 → 17³ probes), throttle at `:70` (~every 30 ticks per alien)
- **Cost:** ~4,900 block reads per alien per ~30 ticks, multiplied by alien pack sizes.
- **Proposal:** Scan in expanding shells with early exit once a torch is found within the legacy ≈5-block break distance, or cap radius to the documented 256-candidate budget; keep the existing `MutableBlockPos`.
- **Behavior:** neutral for early-exit-on-nearest; affecting if radius is reduced (smaller seek range — flag)

### OPT-015 — Godzilla block crushing re-resolves deferred block holders per block
- **Impact:** MEDIUM
- **Location:** `danger/orespawn/entity/Godzilla.java:405-418` (`crushBlocks`), `:374-402` (`isCrushable`: ~20 identity compares incl. 6 `ModBlocks.X.get()` resolutions per block), called twice every 4th tick over a 29×29 slice (`:611-626`)
- **Cost:** ~1,700 block reads + up to ~34k comparisons per 4 ticks while loaded; `BlockPos.containing` alloc per block.
- **Proposal:** Build a lazily-initialized `static Set<Block>` of non-crushables (resolve `ModBlocks` holders once) and iterate with a `MutableBlockPos`.
- **Behavior:** neutral

### OPT-016 — King/Queen target scans sort the entire 80×64×80 entity list
- **Impact:** MEDIUM
- **Location:** `danger/orespawn/entity/TheKing.java:1149-1173` (sort at `:1162`), `TheQueen.java:1254-1262`; re-scan of the same box at `TheKing.java:1273-1285`
- **Cost:** Every ~3–5 ticks per boss: full `LivingEntity` query over a 160×128×160 region + O(n log n) sort, when only nearest-suitable + a "head exists" flag are needed.
- **Proposal:** Replace sort-then-scan with single-pass nearest-suitable selection (track min distance); reuse one scan result for `findSomethingToAttack`/`findNearestPlayer` within the same tick.
- **Behavior:** neutral (nearest-first selection preserved)

### OPT-017 — Ghost / GhostSkelly poll getNearestPlayer every idle tick
- **Impact:** MEDIUM
- **Location:** `danger/orespawn/entity/Ghost.java:96-100`, `GhostSkelly.java:87-91` (runs whenever `attackCooldown == 0`; the second gated call at `Ghost.java:113-114`/`GhostSkelly.java:104-105` is fine)
- **Cost:** Player-list distance scan per ghost per tick (cheap per call but ungated; contact range ~2 blocks).
- **Proposal:** Early-exit with a squared-distance check against the cached flight-target player, or gate the fallback poll to every 5 ticks.
- **Behavior:** neutral for the distance-early-exit variant; affecting if throttled (≤5 ticks contact-damage latency — flag)

### OPT-018 — MHLib runs a hitbox-profile registry lookup in every LivingEntity constructor
- **Impact:** MEDIUM
- **Location:** `de/dertoaster/multihitboxlib/mixin/entity/MixinLivingEntity.java:74-76` → `IMultipartEntity.mhlibOnConstructor` (`IMultipartEntity.java:469-505`, up to 4 `getHitboxProfile()` calls)
- **Cost:** Registry `getKey` + datapack lookup × 4 for every living entity constructed JVM-wide (vanilla mobs included) — significant during chunk load / spawn waves.
- **Proposal:** Memoize per `EntityType` in a static `Map<EntityType<?>, Optional<HitboxProfile>>` invalidated on datapack reload; bail out on cached empty.
- **Behavior:** neutral

### OPT-019 — MHLib part alignment allocates ~6 Vec3 per part per tick with linear `contains`
- **Impact:** MEDIUM
- **Location:** `de/dertoaster/multihitboxlib/api/IMultipartEntity.java:162-193` (`alignSubParts`: chained Vec3 allocs + `synchedBones().contains` linear scan per part), `:195-239` (`alignSynchedSubParts`: fallback `BoneInformation` alloc per synced bone per tick)
- **Cost:** Per part per tick for every multipart boss on the server.
- **Proposal:** Precompute a per-part `isSynched` boolean at construction; fold the rotation/scale/translate chain into inline double math; only build the fallback `BoneInformation` when the sync map lacks the bone.
- **Behavior:** neutral

### OPT-020 — Boss bars updated every tick
- **Impact:** LOW
- **Location:** `PitchBlack.java:344`, `Kraken.java:155`, `Mothra.java:237`, `Godzilla.java:570`, `TheQueen.java:642`, `SpiderRobot.java:99`, etc.
- **Cost:** Negligible — `ServerBossEvent.setProgress` only broadcasts on change.
- **Proposal:** No action needed; listed to close out the checklist item.
- **Behavior:** neutral (N/A — no change proposed)

### OPT-021 — Sort-then-first-match pattern in ~25 small mobs
- **Impact:** LOW
- **Location:** Representative: `Fairy.java:147`, `EntityMantis.java:232`, `BandP.java:218`, `EntityVortex.java:280`, `EntityLeon.java:557`, `Robot2`–`Robot5`, `GiantRobot.java:142`
- **Cost:** O(n log n) sort of a small list every scan where a single-pass min would do.
- **Proposal:** Shared single-pass nearest-suitable selection helper in a util class.
- **Behavior:** neutral

### OPT-022 — Armor auto-enchant check runs per armor piece per inventory tick
- **Impact:** LOW
- **Location:** `danger/orespawn/item/ItemOreSpawnArmor.java:130-150`
- **Cost:** One data-component presence check per OreSpawn armor stack per tick (cheap; enchant application itself is once).
- **Proposal:** Apply enchants in `onCraftedBy`/first pickup instead of polling `inventoryTick`, or gate the check to every 20 ticks.
- **Behavior:** affecting (`onCraftedBy` migration changes when loot/creative-given stacks get enchanted; the 20-tick gate delays first-tick enchanting by ≤1 s — flag whichever is chosen)

### OPT-023 — Godzilla re-resolves the Mobzilla SavedData every tick
- **Impact:** LOW
- **Location:** `danger/orespawn/entity/Godzilla.java:579-581` → `MobzillaSpawnTracker.get():67-70`
- **Cost:** `overworld().getDataStorage().computeIfAbsent` map lookup per tick (markSpawned is idempotent-guarded).
- **Proposal:** Cache a local `markedSpawned` boolean on the entity and skip after first success.
- **Behavior:** neutral

### OPT-024 — Dragon rider-mode pushes via a broad `getEntities` query per tick
- **Impact:** LOW
- **Location:** `danger/orespawn/entity/Dragon.java:472-478`
- **Cost:** Per tick while ridden: AABB alloc + all-entity query (mirrors vanilla `pushEntities`, acceptable).
- **Proposal:** Optional: pass a `pushable` predicate into the query to skip the post-filter.
- **Behavior:** neutral

### OPT-025 — Worldgen flora helpers allocate BlockPos pairs in descending column loops
- **Impact:** LOW
- **Location:** `danger/orespawn/world/OreSpawnChunkGenerator.java:613-620` (flowers), `:636-643` (rice), `:659-666` (quinoa), `:682-689` (termite mounds), `:203-211` (scraggly trees), `:559-587` (ore veins: `new BlockPos` + `BlockState.equals` per cell)
- **Cost:** Per chunk: a few thousand short-lived `BlockPos` allocations; `equals` instead of `==`/`is()` on interned states.
- **Proposal:** Reuse a `MutableBlockPos`; start scans at the heightmap; compare states with `.is(block)`.
- **Behavior:** neutral

### OPT-026 — TheKing line-of-sight: manual 20+-step block ray per candidate
- **Impact:** LOW
- **Location:** `danger/orespawn/entity/TheKing.java:1185-1225` (`MyCanSee`), called per candidate from `isSuitableTarget:1138`
- **Cost:** Up to ~20–60 `getBlockState` + `BlockPos.containing` allocs per candidate per scan (scan throttled ~1/3–5 ticks).
- **Proposal:** Evaluate `MyCanSee` only for the current best candidate (after the distance min-pass) and reuse a `MutableBlockPos` in the march.
- **Behavior:** neutral (if applied only to the selected candidate in the same order)

### OPT-027 — Spawn-cluster checks scan twice per spawn attempt
- **Impact:** LOW
- **Location:** Representative: `EntityAnt.java:208-214`, `EntityCricket.java:156-162`, `Chipmunk.java:211-219`, `EntityTermite.java:221-228`, `Frog.java:273-283` — each runs both a `size() <= N` check and a separate count over the same `inflate(20,10,20)` box
- **Cost:** Spawn-time only: duplicate entity query per `checkSpawnRules` call during spawn cycles.
- **Proposal:** Compute the count once and reuse for both checks.
- **Behavior:** neutral

