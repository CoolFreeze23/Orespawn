# OreSpawn Port — Audit Findings Register

**Companion to:** `AUDIT_INVENTORY.md` (full checklist). Detailed audit evidence in `audit_sections/01–10`.
**Scope:** every MISSING / PARTIAL / DIVERGENT / UNVERIFIED item, every bug, every optimization opportunity — each with file/line refs in both codebases and a concrete fix.
**Original:** `reference_1_7_10_source/sources/danger/orespawn/` · **Port:** `src/main/java/danger/orespawn/` + `src/main/resources/`
**Status: REPORT ONLY — nothing has been fixed. Awaiting go-ahead.**

---

## Summary

### How much is truly ported?

"PORTED" below means *verified equivalent by reading both implementations* — never inferred from a matching name. UNVERIFIED items are counted as not-ported.


| Category                                  | Verified PORTED | Total audited | % truly ported |
| ----------------------------------------- | --------------- | ------------- | -------------- |
| Entities (non-boss)                       | 27              | 130           | **21%**        |
| Boss sub-features                         | 58              | 110           | **53%**        |
| Blocks / items / tiers / recipes / config | ~139            | ~270          | **~51%**       |
| Worldgen / dimensions / structures        | 30              | 81            | **37%**        |
| Animations / events / GUI                 | 16              | 38            | **42%**        |
| **Overall**                               | **~270**        | **~629**      | **~43%**       |


Caveats: entity-level "PORTED" is strict (one divergent sub-feature demotes the entity); the recipe corpus was only spot-checked (~20 diffed; bulk marked UNVERIFIED — see ITEM findings); `% truly ported` would drop further if sub-feature rows were the unit for non-boss entities.

### Findings counts


| Type                    | Count   | Breakdown                                                                |
| ----------------------- | ------- | ------------------------------------------------------------------------ |
| Port-parity findings    | 543     | DIVERGENT 305 · PARTIAL 151 · MISSING 66 · UNVERIFIED 21                 |
| Bugs (Phase 3)          | 31      | CRITICAL 7 · HIGH 6 · MEDIUM 9 · LOW 9                                   |
| Optimizations (Phase 4) | 27      | HIGH 8 · MEDIUM 11 · LOW 8 (20 behavior-neutral / 4 affecting / 3 mixed) |
| **Total entries**       | **601** |                                                                          |


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
- **Resolution:** FIXED (2026-06-13, Phase D1 — all 103 original `func_70601_bi` gates now have `checkSpawnRules` ports built on `OriginalSpawnGates` + `ModDimensionKeys`; corpus in phase_d_reports/D1_original_spawn_rules.md, coverage verified by tools/d1_gate_diff.py (0 missing); pre-existing divergent gates rebuilt (tools/fix_preexisting_gates.py, audit in phase_d_reports/D1_preexisting_gate_audit.md); see FIX_LOG.md)

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
- **Resolution:** FIXED (2026-06-11, Phase C — Robot2-5/GiantRobot/ridden-Dragon/ridden-Player immunities restored in LaserBall.onHitEntity; TrooperBug/SpitBug remain unported (Phase D); see FIX_LOG.md and phase_c_reports/C1_entities_A_C.md) — CLOSED (2026-07-02, Phase D4 — TrooperBug/SpitBug acid immunity restored in LaserBall.onHitEntity: when isAcid, the projectile discards on impact with either bug)

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
- **Resolution:** FIXED (2026-08-11, Phase E4 ENT-A batch — village goal restored: MoveThroughVillageGoal(1.0, false, 4, no-doors) @1, POI-driven descendant of the removed door-list goal, mapping documented inline (orig Alien.java:62). AlienTorchSeekGoal rewritten as a faithful flagless one-shot port of orig Alien.java:328-343 + scan_it :243-304: cube shells r=2-14 (11/13 skipped, interior blind — bug kept), 1-in-30 cadence on non-attack (7/8) ticks, PlayNicely gate, torch/wall-torch/ExtremeTorch only (invented soul-torch match, cooldowns, 8-block cube, 2.5-block arrival-break removed); mobGriefing gates only the removal (instant at distSq<27, setBlock air flag 2), nav 1.0. Same-file parity: wander back to MyEntityAIWanderALot(10,1.0) (orig :63), swing dice nextInt(4)==0||nextInt(5)==1 (orig :320), PlayNicely gate in findSomethingToAttack (orig :367-369). TF-035: GenericTargetSorter swapped in (orig :41,59).)

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
- **Resolution:** FIXED (2026-06-13, Phase D1 — spawn-rule gate ported in checkSpawnRules citing orig Alien.java:397-434; weights/biomes half closed in Phase C (2026-06-11, Phase C — End spawn removed); see FIX_LOG.md)

### ENT-A-007 — Alien: jump boost invented in port

- **Status:** PARTIAL
- **Original:** `Alien.java` — no jump modification (and no fire immunity)
- **Port:** `entity/Alien.java` `jumpFromGround` — jump boost added (new behavior)
- **Fix:** delete the `jumpFromGround` override unless intentionally kept as a port feature.
- **Resolution:** VERIFIED-CORRECT (2026-08-11, Phase E4 ENT-A batch — audit misread the original: orig Alien.java:100-103 overrides func_70664_aZ (jump) with 'super.func_70664_aZ(); this.field_70181_x += 0.25;' — a +0.25 motionY boost on every jump. Port Alien.jumpFromGround adds exactly 0.25 after super.jumpFromGround() (port Alien.java jumpFromGround/JUMP_BOOST) — faithful, not invented. Fire-immunity half verified too: orig Alien.java:57 sets field_70178_ae=false (not fire-immune) and the port has no fireImmune() on the ALIEN EntityType nor any override. Citation comments added at the override and the JUMP_BOOST constant.)

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
- **Resolution:** FIXED (2026-08-11, Phase E4 ENT-A batch — invented targeting removed: NearestAttackableTargetGoal<Player> deleted and HurtByTargetGoal(this, Alosaurus.class).setAlertOthers() reverted to plain HurtByTargetGoal(this) (orig Alosaurus.java:54 — EntityAIHurtByTarget(this,false): no ignore list, no pack alert). Acquisition restored as the orig 1-in-5 tick scan in customServerAiStep (orig :159-179): PlayNicely-gated 12/5/12 box (:215-218), GenericTargetSorter order (:39,48 — TF-035 swap included), prey = everything living except isIgnoreable/Alosaurus/Cryolophosaurus/VelocityRaptor/no-line-of-sight/creative players (:182-212); the pick feeds the accepted DinosaurMeleeAttackGoal mapping via setTarget, and an empty scan clears it (:176-178). MoveThroughVillageGoal(1.0,false,4,no-doors) added @2 under the melee slot (orig :50 @1); wander/look goals renumbered, relative order preserved.)

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
- **Resolution:** FIXED (2026-06-13, Phase D1 — spawn-rule gate ported in checkSpawnRules citing orig Alosaurus.java:240-279; weights/biomes half closed in Phase C (2026-06-11, Phase C — audit corrected: orig has NO overworld addSpawn); see FIX_LOG.md)

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
- **Fix:** implement rider-controlled `travel()` replicating the original velocity/obstruction-climb model (compare the `entity/Elevator.java` `tickRidden` port for the pattern); restore leg animation from `updateLegs` data.
- **Note:** the `updateLegs` leg-animation half was closed in Phase D2 (full solver ported, ant constants, orig AntRobot.java:156-510 — see ANIM-006); the rider-controlled hover-physics half remains open (Phase E owner unchanged).
- **Resolution:** FIXED (2026-08-11, Phase E4 ENT-A batch — rider hover physics were absent (vanilla startRiding only). Ported orig AntRobot.java:659-877 as client-predicted tickRidden per the Elevator/B3 pattern: clamps ±0.85/±1.25 (:675-692), ridden hover probe 2.25 down +0.06/+0.03 else −0.02 (:743-751), obstruction-climb wedge depth 3+v*6, ±90° arc, 0.02/block ×0.05 (:766-782), yaw chase lag |1.85−v| clamped 0.01-0.9, pitch 0 (:783-815), heading sign vs rider facing with hand-typed PI (:816-834), throttle ±0.05 caps 0.3 fwd/0.25 rev (:835-863), double integration 0.98 then 0.8/0.98/0.8 (:864-872). Riderless hover +0.15/−0.002 plus second integration in travel() (:752-763,:869-872); seat 1.25 back, 0.55 up with rideTicker bobs (:548-558, TF-029 convention); isPushable false (:544-546). TF-035: GenericTargetSorter restored, unused as in orig (:43,:54). Port: entity/AntRobot.java:261-499.)

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
- **Resolution:** FIXED (2026-06-13, Phase D3 — the original's exact double roll restored (`nextInt(4)==0 || nextInt(5)==1` = 40%) as part of the ENT-A-019 watercanon port; see FIX_LOG.md)

### ENT-A-019 — AttackSquid: ranged `watercanon` attack missing

- **Status:** MISSING
- **Original:** `AttackSquid.java` — fires `InkSack` (1-in-3) or `WaterBall` (2-in-3), speed 1.4, spread 5.0
- **Port:** `entity/AttackSquid.java` — melee only
- **Fix:** add ranged attack: roll 1-in-3 InkSack else WaterBall, velocity 1.4, inaccuracy 5.0 (both projectile classes exist in port).
- **Resolution:** FIXED (2026-06-13, Phase D3 — `watercanon` ported line-for-line (1-in-5 fire roll, InkSack 1-in-3 else WaterBall, shoot 1.4f/5.0f, muzzle offsets and the original's yHeadRot/yRot mix preserved); see FIX_LOG.md)

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
- **Resolution:** FIXED (2026-06-13, Phase D1 — MobCategory MONSTER→WATER_CREATURE, IN_WATER spawn placement, river/swamp/ocean BM JSONs restored, checkSpawnRules y>=50+day per orig AttackSquid.java; see FIX_LOG.md)

### ENT-A-022 — AttackSquid: hurt() projectile exclusions missing

- **Status:** PARTIAL
- **Original:** `AttackSquid.java` — `hurt()` ignored WaterBall/WaterDragon damage from squids
- **Port:** `entity/AttackSquid.java` — exclusions absent
- **Fix:** in `hurt()`, return false when damage source is a WaterBall or WaterDragon originating from a squid.
- **Resolution:** FIXED (2026-08-11, Phase E4 ENT-A batch — hurt() lacked the WaterBall/WaterDragon exclusions (orig AttackSquid.java:373-378: true source func_76346_g instanceof WaterBall or WaterDragon returns false before any retaliation). Added both checks after the existing AttackSquid check, mapping func_76346_g to source.getEntity(); because the modern damage model carries the projectile as the DIRECT entity (the port's WaterBall passes an owner where the orig passed null), the WaterBall check also covers source.getDirectEntity() so WaterBall fire never lands, matching the orig's net behavior (the orig also blanked it target-side, WaterBall.java:47-52). Kraken revenge (orig :392-397) verified already centralized in KrakenRevengeHandler — intentional architecture, untouched. Port: entity/AttackSquid.java:102-121, exclusions :106-115.)

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
- **Resolution:** FIXED (2026-08-11, Phase E4 ENT-A batch — port had 3-goal lineup and Player-only targets. Restored orig BandP.java:56-61 lineup: MoveThroughVillageGoal(0.5,false)@0 (vanilla goal is the honest 1.14+ POI mapping, documented inline), stroll@1, LookAtPlayer@2, RandomLookAround@3, OpenDoorGoal(true)@4 with nav setCanOpenDoors, MoveIndoorsGoal@5 (roofed-shelter match per E3). Aggro filter extended to Villager/Girlfriend/Boyfriend with line-of-sight and PlayNicely gate (orig :226-255); 1-in-12 cadence already matched orig :181. TF-035 rider: GenericTargetSorter swapped in (orig :42,55). Port: entity/BandP.java registerGoals/isSuitableTarget/findSomethingToAttack.)

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
- **Resolution:** FIXED (2026-06-13, Phase D1 — spawn-rule gate ported in checkSpawnRules citing orig BandP.java:278-309; weights/biomes half closed in Phase C (2026-06-11, Phase C — plains/desert/savanna w20 1-2 modifier restored (hostile_band_p.json), removed from generic overworld list); see FIX_LOG.md)

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
- **Resolution:** FIXED (2026-06-13, Phase D1 — spawn-rule gate ported in checkSpawnRules citing orig Baryonyx.java:66-74; weights/biomes half closed in Phase C (2026-06-11, Phase C — Mining-dim w2 4-8 + Utopia w2 2-4 restored, overworld entry removed); see FIX_LOG.md)

### ENT-A-030 — Baryonyx: wheat breeding lost

- **Status:** PARTIAL
- **Original:** breeds with wheat or crystal apple
- **Port:** `entity/Baryonyx.java` — breeds with `CRYSTAL_APPLE` only
- **Fix:** include `Items.WHEAT` in `isFood`.
- **Resolution:** VERIFIED-CORRECT (2026-08-11, Phase E4 ENT-A batch — orig Baryonyx.java:240-242 func_70877_b: "return par1ItemStack.func_77973_b() == OreSpawnMain.MyCrystalApple;" — crystal apple ONLY. The isWheat helper (:236-238) is dead code: grep ".isWheat(" finds zero callers in the entire 1.7.10 source, and it tests Items.field_151034_e, the APPLE, not wheat (wheat is field_151015_O). Port entity/Baryonyx.java:170-173 isFood == CRYSTAL_APPLE matches the original exactly; adding WHEAT would have invented a breeding item. TF-035: orig Baryonyx.java contains no GenericTargetSorter — nothing to swap.)

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
- **Resolution:** FIXED (2026-08-11, Phase E4 ENT-A batch — audit partly corrected: orig DOES apply Slowness V 100t (Basilisk.java:372-374, to the scanned target in OR out of reach) and Poison on bite (:319-330, 1-in-3, 8/10/12/14s by difficulty). Inventions removed: BasiliskGazeAttackGoal deleted, NearestAttackableTargetGoal<Player> removed. Orig func_70619_bc scan restored in customServerAiStep: 1-in-5 cadence (:360), 24x7x24 GenericTargetSorter scan + PlayNicely gate (:416-420), filter excl. ignoreables/LoS-fail/Basilisk/LeafMonster/creative (:384-413), reach 6+width/2 (:364), swing 1/3-else-1/4 (:366), navigate 1.25 (:370). Poison relocated to doHurtTarget (:319-330). MoveThroughVillage@1 restored (:55); mygetMaxHealth 500->200 so the 1-in-75 heal gate (:379-381) works. TF-035: sorter per orig :43,53. This supersedes ENT-A-033's note that the gaze goal carries the effects.)

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
- **Port:** `hostile_basilisk__`* badlands+jungle, w3 1–1
- **Fix:** retarget modifiers to mushroom fields, jungle, old-growth taiga at original weights; restore night/buddy rules (ENT-SYS-002).
- **Resolution:** FIXED (2026-06-13, Phase D1 — spawn-rule gate ported in checkSpawnRules citing orig Basilisk.java:441-477; weights/biomes half closed in Phase C (2026-06-11, Phase C — audit corrected: orig biomes are jungle w3, jungleHills w2, birchForestHills w4 1-2, roofedForest w15 1-2 (OreSpawnMain.java:4877-4880), not mushroom/mega-taiga); see FIX_LOG.md)

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
- **Resolution:** FIXED (2026-08-11, Phase E4 ENT-A batch — lump add_overworld_creatures.json beaver row (w8 1-2, #minecraft:is_overworld) replaced by five per-biome modifiers per orig OreSpawnMain.java:4602-4607: river w10 2-4, forest w3 2-4, birch_forest w2 2-4, birchForestHills→old_growth_birch_forest w2 2-5, megaTaiga+taiga→old_growth_pine_taiga+taiga w5 2-5 (creature_red_cow__ TF-033 precedent; category CREATURE already correct in ModEntities). Returned as sharedEdits. TF-035 rider closed in the same pass: Beaver.findBuddy swapped plain distance comparator for GenericTargetSorter (orig Beaver.java:38,51,221) and dropped the port's invented self-exclusion filter — orig func_72872_a includes the caller, which always sorts first, so the 1-in-200 buddy stroll pathed to its own position; original bug kept.)

### ENT-A-039 — Beaver: wheat breeding lost

- **Status:** PARTIAL
- **Original:** breed wheat/crystal apple
- **Port:** crystal apple only
- **Fix:** add `Items.WHEAT` to `isFood`.
- **Resolution:** VERIFIED-CORRECT (2026-08-11, Phase E4 ENT-A batch — orig Beaver.java:300-302 func_70877_b: "return par1ItemStack.func_77973_b() == OreSpawnMain.MyCrystalApple;" — crystal apple ONLY. The isWheat helper (:296-298) is dead code with zero callers across the entire 1.7.10 source (grep ".isWheat(") and tests Items.field_151034_e, the APPLE, not wheat. In 1.7.10 EntityAnimal the only breeding-item hook is func_70877_b; nothing routes through isWheat. Port entity/Beaver.java isFood == CRYSTAL_APPLE matches the original exactly — adding WHEAT would invent a breeding item.)

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
- **Resolution:** FIXED (2026-08-11, Phase E4 ENT-A batch — port seek filter stopped at Player/Villager. Added Girlfriend and Boyfriend (orig Bee.java:319-322), corrected the creative check from getAbilities().invulnerable to instabuild (orig :312-315, field_75098_d is isCreativeMode), and added the missing PlayNicely aggression gate (orig :326-328). TF-035 rider: orig Bee.java:37,50 constructs GenericTargetSorter — port's plain Comparator.comparingDouble(distanceToSqr) swapped for new GenericTargetSorter(this). Port: entity/EntityBee.java findSomethingToAttack/isSuitableTarget; LoS and in-water exclusions already matched orig :306-311.)

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
- **Resolution:** FIXED (2026-06-13, Phase D1 — spawn-rule gate ported in checkSpawnRules citing orig Bee.java:253-287; weights/biomes half closed in Phase C (2026-06-11, Phase C — per-biome forest/jungle/birch/taiga/savanna modifiers w2-5 restored (OreSpawnMain.java:4709-4718), removed from generic overworld list); see FIX_LOG.md)

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
- **Resolution:** FIXED (2026-08-11, Phase E4 ENT-A batch — enchant IDs/levels already matched (KB5/Bane1/FA1 type-0, Unbreaking5 Royal, none Hammy; field_77337_m=knockback, 77336_l=bane, 77334_n=fireAspect, 77347_r=unbreaking, anchored via Boyfriend.java:947-948 vanilla fire-aspect formula and AttackSquid.java:188-242 tool-drop pattern), but two trigger paths diverged: port re-baked only when the stack had NO enchantments, while orig probes Knockback then Unbreaking levels and re-bakes when both read 0 (orig Bertha.java:45-58); and orig also bakes at craft time via func_77622_d (orig Bertha.java:35-43), which the port lacked. Port item/Bertha.java now probes KNOCKBACK→UNBREAKING in inventoryTick and adds onCraftedBy.)

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
- **Resolution:** FIXED (2026-08-11, Phase E4 ENT-A batch — port exploded 2.1 on EVERY type-3 impact (even entity hits and pvp-spared hits), with fire wrongly tied to mobGriefing. Orig: entity-hit branch detonates 1.5f inside the hit_type==3/distSq<64 damage guard (orig BerthaHit.java:106-108); the 2.1f blast is the no-entity-hit else-branch, also gated distSq(shooter)<64 (orig BerthaHit.java:110-112); both pass isFlaming=true, isSmoking=mobGriefing. Port entity/BerthaHit.java: 1.5f explosion restored inside onHitEntity's range-gated type-3 block; onHit 2.1f now gated on non-entity HitResult + owner distSq<64; fire=true with ExplosionInteraction.MOB (reads mobGriefing). Null owner skipped where orig would NPE. Stale test comment updated in EntityLogicTestsA.java, assertion unchanged.)

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
- **Resolution:** FIXED (2026-07-02, Phase D4 — pass-through immunities restored in canHitEntity (other BetterFireballs, Mothra, GodzillaHead, Royalty, plus Player/Dragon when notme is set) and the HP-halving exemption list restored in onHitEntity (Royalty, Godzilla, GodzillaHead, PitchBlack, Kraken) per orig BetterFireball; see FIX_LOG.md)

### ENT-A-053 — Boyfriend: armor floor, fire immunity, and size lost

- **Status:** PARTIAL
- **Original:** `Boyfriend.java:123,179-193,492` — HP 80, fire-immune, armor clamp 8–23 from worn gear; size 0.5×1.6
- **Port:** `entity/Boyfriend.java:84-89`, `ModEntities.java:391` — HP 80 ✓; no fire immunity, no min-armor-8 floor; size 0.6×1.8
- **Fix:** add `fireImmune()`; override armor getter to clamp 8–23 based on equipment; size 0.5×1.6.
- **Resolution:** FIXED (2026-08-11, Phase E4 ENT-A batch — armor floor/clamp restored: getArmorValue() sums worn ArmorItem defense over all equipment slots and clamps 8..23 (orig Boyfriend.java:179-193 func_70658_aO; port entity/Boyfriend.java getArmorValue). HP 80 already correct (orig :491-493). Size and fire immunity live in the shared registry: sharedEdits change ModEntities.java BOYFRIEND to .sized(0.5f,1.6f) (orig :122 func_70105_a) + .fireImmune() (orig :123 field_70178_ae=true), matching the Brutalfly ENT-A-060 precedent of setting both on the EntityType builder.)

### ENT-A-054 — Boyfriend: AI set diverged (ranged→melee, goals missing, tempt item changed)

- **Status:** DIVERGENT
- **Original:** `Boyfriend.java:127-148` — Tempt(cooked beef), `EntityAIArrowAttack(4, 1.25, 20t, 10.0f)`, Panic(6), OpenDoor(10), MoveIndoors(11); Jealousy target goals @4/5
- **Port:** `entity/Boyfriend.java:70-82` — Tempt(DIAMOND), `MeleeAttackGoal(4)`; no Panic/OpenDoor/MoveIndoors/Jealousy
- **Fix:** tempt with cooked beef; replace MeleeAttackGoal with a ranged-attack goal (see ENT-A-055); add Panic, OpenDoor, MoveIndoors and Jealousy goals.
- **Resolution:** FIXED (2026-08-11, Phase E3 — final remainders closed: the two Jealousy target goals ported as JealousyTargetGoal (tamed non-sitting owner-holding Boyfriend hunts UNTAMED Boyfriend rivals; tamed victims never targeted, orig MyEntityAIJealousy.java:31-48; per-goal ranges override follow-range like the orig's targetDistance) registered @4 (6.0, chance 5) and @5 (3.0, chance 15) per orig Boyfriend.java:146-147, PlayNicely-gated dynamically; MoveIndoors ported as MoveIndoorsGoal @11 — documented 1.21.1 behavioral match (roofed-shelter seeking at night/rain; the vanilla door/village framework the 1.7.10 goal used was removed in 1.14; mapping decision in the class Javadoc per plan ground rule 5). Ranged/melee/sounds half was ENT-A-055 (FIXED, D3). Girlfriend-side goal-list gaps (her jealousy pair, Panic@6, OpenDoor@10, MoveIndoors@11) noted for her open ENT-D partial in the E4 batch. See FIX_LOG Phase E. Previously: PARTIAL (2026-06-11, Phase C — tempt food cooked beef, PanicGoal(1.5)@6, OpenDoorGoal@10 + door navigation added; ranged attack (ENT-A-055), Jealousy goals, MoveIndoors remain (Phase D); see FIX_LOG.md and phase_c_reports/C1_entities_A_C.md))

### ENT-A-055 — Boyfriend: entire weapon/ranged system missing

- **Status:** MISSING
- **Original:** `Boyfriend.java:874-907,239-289` — fires `UltimateArrow` (2.0f, 1-in-4 crit, punch/flame aware) when holding UltimateBow, else throws `Shoes` projectile (1.8 speed, 4.0 spread); melee with held-item enchant math + 25t cooldown + `b_fight` sound
- **Port:** plain vanilla `MeleeAttackGoal`; no Shoes, no UltimateBow logic, no fight/taunt sounds
- **Fix:** implement `RangedAttackMob`: UltimateArrow when holding UltimateBow, Shoes projectile fallback; port held-item enchant melee math and `b_fight` sound.
- **Resolution:** FIXED (2026-06-13, Phase D3 — `RangedAttackMob` + `RangedAttackGoal(1.25, 20, 10.0f)` per orig goal 4; `performRangedAttack` fires UltimateArrow (UltimateBow held) or Shoes id 6; armed melee in `customServerAiStep` with the 25t cooldown, Big-Bertha 10-block reach, `b_fight`/`b_taunt` sounds. The invented BOYFRIEND_BRO_MODE combat gate was removed (archived as MOD-010 — orig bro_mode is voice-only, ENT-A-058 scope); see FIX_LOG.md)

### ENT-A-056 — Boyfriend: tamed poppy drop missing

- **Status:** PARTIAL
- **Original:** `Boyfriend.java:839-872` — tamed: 2–6 poppies; always 10–35 game controllers; all equipped gear
- **Port:** `boyfriend.json` — game controller 10–36; equipment ✓; no poppies
- **Fix:** add poppy 2–6 (condition: tamed) to loot or `dropCustomDeathLoot`; correct controller max 35.
- **Resolution:** FIXED (2026-08-11, Phase E4 ENT-A batch — controller max corrected 36→35 in boyfriend.json (orig Boyfriend.java:848-850, nextInt(26)+10 = 10..35); tamed-only drops ported in dropCustomDeathLoot: 2-6 poppies (nextInt(5)+2, orig :840-847) and ALL equipped gear at full count in the orig slot order held/boots/legs/chest/helmet (orig :854-871). No super call, so vanilla's 8.5% chance-based equipment roll is suppressed — a tamed Boyfriend always drops every piece and an untamed one drops none, exactly as orig func_70628_a. Port: entity/Boyfriend.java dropCustomDeathLoot; data/orespawn/loot_table/entities/boyfriend.json.)

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
- **Resolution:** FIXED (2026-08-11, Phase E4 ENT-A batch — full ambient tree restored in getAmbientSound per orig Boyfriend.java:768-812: bro-mode half-silence roll (:772-774), silence while fighting (:776-779), b_water when swimming (:780-782), then 3-in-4 surroundings block — silent below y=60 (:784-786), b_thunder (:787-789), b_rain (:790-792), b_dark 1-in-3 under a night sky else silent (:793-798) — falling through to tamed b_hurt / bb_happy (bro mode) / b_happy (:800-808). Also restored the bro-mode gates the D3 note assigned here: getHurtSound half-silence (:818-820) and getDeathSound full silence (:825-827). bro_mode maps to existing OreSpawnConfig.BOYFRIEND_BRO_MODE (default false = orig default 0, OreSpawnMain.java:1481). All events (b_water/b_thunder/b_rain/b_dark/bb_happy) already in sounds.json — no shared edit.)

### ENT-A-059 — Boyfriend: wet-skin, untame, voice toggle, health report, FrogPrince missing

- **Status:** PARTIAL
- **Original:** `Boyfriend.java` — untame via dead bush; voice off Ruby / on Amethyst; wet-skin system (18 swimshorts textures, wet_count 500); diamond-in-hand guard mode; health report chat; Peacock alt tame item; FrogPrince textures
- **Port:** `entity/Boyfriend.java` — none of these; skin cycle moved to DANDELION (dry only); new BOYFRIEND_BRO_MODE config
- **Fix:** port the item interactions (dead bush untame, Ruby/Amethyst voice toggle, Peacock tame), wet-skin texture state (wet_count 500), and health-report chat message.
- **Resolution:** FIXED (2026-08-11, Phase E4 ENT-A batch — ported: dead-bush untame (orig :567-581), Ruby voice-off/Amethyst voice-on (:582-611), cooked-peacock alt tame + breeding item (:536, :760-762; MyPeacock=cookedpeacock, OreSpawnMain.java:1873), leather/peacock-feather wet-aware skin cycle (:612-640) replacing the invented DANDELION cycle, wet_count-500 both-sides simulation (:498-502), diamond guard-sit inside the orig whole-stack give-item flow with OreSpawn-armor-only auto-slot (:657-698), diamond-block re-claim with no owner check (:701-714), empty-hand gear return in orig slot order 0-4 plus 'I have %d health' chat (:725-756) replacing the invented sit toggle; FrogPrince: setPrince (:291-293), 20-tick sync incl. orig save-the-watcher bug (:516/:521/:222), NBT WetGuyType/IsPrince, renderer wet/prince selection (:295-446; frogprince.png/frogprince2.png/swimshorts0-17 assets present). sharedEdit: Frog.java setPrince call (orig Frog.java:135).)

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
- **Resolution:** FIXED (2026-08-11, Phase E4 ENT-A batch — retarget now runs the orig terrain-descent scan (nine columns at x/z {-5,0,+5}, 19 deep; ground >10 below biases target y by dist-10+1; orig Brutalfly.java:175-191,204) and candidates must be air AND pass the eye-line clip probe from y+0.75 (orig :147-149,205-208), keeping the orig quirk that each candidate is written to currentFlightTarget before validation so 30 failures leave the last one (:193-210). Hunt filter regains Mothra/Vortex/MyUtils.isIgnoreable exclusions (orig :418-429) and findSomethingToAttack the PlayNicely gate (orig :444-446). TF-035: plain distance comparator swapped for GenericTargetSorter (orig :50,60,448). Port: EntityBrutalfly.java canSeeTarget/customServerAiStep/findSomethingToAttack/isSuitableTarget.)

### ENT-A-062 — Brutalfly: signature fireball barrage missing

- **Status:** MISSING
- **Original:** `Brutalfly.java:369-406` — Easy=SmallFireball, Normal=50/50 Small/BetterFireball, Hard=BetterFireball; +1 HP self-heal per shot; shoot odds 1-in-3 (1-in-2 hard); melee only within distSq 25
- **Port:** `EntityBrutalfly.java:141-158` — melee only
- **Fix:** implement `attackWithSomething`-style ranged logic with difficulty-keyed projectile choice, per-shot self-heal, and the original shoot odds.
- **Resolution:** FIXED (2026-06-13, Phase D3 — `attackWithSomething` ported (difficulty-keyed Small/BetterFireball, +1 HP per shot, distinct sounds); shoot odds 1-in-3 (1-in-2 Hard); the port's invented melee-on-player replaced by the original's ranged-only player engagement; mobs meleed close / canonned past 25 blocks; see FIX_LOG.md)

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
- **Resolution:** FIXED (2026-06-13, Phase D1 — spawn-rule gate ported in checkSpawnRules citing orig Brutalfly.java:290-329; weights/biomes half closed in Phase C (2026-06-11, Phase C — audit corrected: orig biomes are megaTaigaHills/extremeHillsPlus/mesaPlateau (OreSpawnMain.java:4839-4841), not savanna plateau); see FIX_LOG.md)

### ENT-A-065 — Brutalfly: hurt sound invented

- **Status:** PARTIAL
- **Original:** no living/hurt sound; death `random.explode`
- **Port:** hurt = GENERIC_HURT (added)
- **Fix:** return null/empty for hurt sound to match the original silent profile.
- **Resolution:** FIXED (2026-08-11, Phase E4 ENT-A batch — invented hurt sound removed: getHurtSound now returns null (@Nullable), matching orig Brutalfly.java:102-104 where func_70621_aR returns null; ambient was already null (orig :98-100) and death stays GENERIC_EXPLODE = orig random.explode (:106-108). LivingEntity.makeSound is null-safe, so the null return is silent exactly like 1.7.10; same idiom already used by the port's Boyfriend.java:432. Port: EntityBrutalfly.java getHurtSound.)

## Camarasaurus

### ENT-A-066 — Camarasaurus: hitbox grew

- **Status:** PARTIAL
- **Original:** `Camarasaurus.java:47` — size 0.5×1.2
- **Port:** `ModEntities.java:395` — size 1.4×2.6
- **Fix:** set dimensions 0.5×1.2.
- **Resolution:** FIXED (2026-08-11, Phase E4 ENT-A batch — port registered Camarasaurus at 1.4x2.6; orig is func_70105_a(0.5f, 1.2f) (Camarasaurus.java:47). ModEntities is shared, so the fix ships as a sharedEdit: CAMARASAURUS .sized(1.4f, 2.6f) -> .sized(0.5f, 1.2f) with the orig cite inline; the tiny box under the huge sauropod model is an original quirk kept per doctrine. No gametest asserts the old size, so no test updates.)

### ENT-A-067 — Camarasaurus: MoveIndoors missing; target goals invented

- **Status:** PARTIAL
- **Original:** `Camarasaurus.java:53-63` — goal 9 MoveIndoors; no owner-combat target goals
- **Port:** `entity/Camarasaurus.java:60-74` — MoveIndoors absent; OwnerHurtBy/OwnerHurt/HurtBy targets added
- **Fix:** add a MoveIndoors-equivalent goal; remove the added combat target goals (passive pet in original).
- **Resolution:** FIXED (2026-08-11, Phase E4 ENT-A batch — orig registers tasks 0-9 only, goal 9 EntityAIMoveIndoors, and NO target goals (Camarasaurus.java:53-62; field_70715_bh never touched); port had dropped MoveIndoors and invented OwnerHurtBy/OwnerHurt/HurtBy target goals (entity/Camarasaurus.java:73-75). Removed all three target goals and added goalSelector priority 9 -> danger.orespawn.entity.ai.MoveIndoorsGoal (E3 roofed-shelter mapping for the removed 1.14 village/door framework, mapping documented in that class's Javadoc), citing orig :62. TF-035: orig Camarasaurus.java contains no GenericTargetSorter — no sorter swap applies.)

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
- **Resolution:** FIXED (2026-08-11, Phase E4 ENT-A batch — orig interact is apple-tame 1-in-2, name-tag rename, sit toggle; no riding code exists anywhere in the class (Camarasaurus.java:219-278). Removed the port's invented ride branch in mobInteract (empty-hand <49 startRiding) plus getControllingPassenger/positionRider/tickRidden/getRiddenInput/getRiddenSpeed x1.5 and the riding-only isPushable override (port entity/Camarasaurus.java:237-305 pre-fix); interact order is now apple -> name tag -> sit toggle, matching orig. Unused Entity/LivingEntity/Vec3 imports dropped. Invention removed outright per doctrine, no config flag.)

### ENT-A-070 — Camarasaurus: tamed-only poppy drop replaced with always-bones

- **Status:** DIVERGENT
- **Original:** `Camarasaurus.java:303-312` — tamed only: 2–6 poppies; untamed drops nothing
- **Port:** `camarasaurus.json` — bone 3–6 always
- **Fix:** empty the default pool; drop poppy 2–6 only when tamed (code-side or loot condition).
- **Resolution:** FIXED (2026-06-11, Phase C — OreSpawnTamed NBT flag + entity_properties condition; poppies 2-6 tamed-only (orig Camarasaurus.java:303-312); see FIX_LOG.md and phase_c_reports/C1_entities_A_C.md)

### ENT-A-071 — Camarasaurus: spawning relocated to overworld

- **Status:** DIVERGENT
- **Original:** mining-dim chunk providers only; rules y≥50 + day (`func_70601_bi:78-83`)
- **Port:** `add_overworld_creatures.json` w2 1–1 + `companion_camarasaurus__`* jungle/savanna w1 1–1; no rules
- **Fix:** move spawns to the mining-dimension spawn lists; restore y≥50 + day rules; remove overworld modifiers.
- **Resolution:** FIXED (2026-06-13, Phase D1 — spawn-rule gate ported in checkSpawnRules citing orig Camarasaurus.java:78-83; weights/biomes half closed in Phase C (2026-06-11, Phase C — Mining-dim w1 2-4 restored, overworld modifiers removed); see FIX_LOG.md)

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
- **Resolution:** FIXED (2026-08-11, Phase E4 ENT-A batch — web-self-clear ported: makeStuckInBlock override mirrors 1.7.10 field_70134_J, raised only by cobwebs (orig CaterKiller.java:450; BlockWeb->setInWeb), and the AI step then sets every cobweb in the 5x6x5 feet box (x -2..2 / y -1..4 / z -2..2, toward-zero int coords) to air with update flag 3 before dropping the flag (orig :450-461), placed between metamorphosis (:438-448) and combat exactly as in orig. Deliberately NOT mobGriefing-gated: the original loop has no gate — only the tree-eat at :521 is gated — so the finding's '(mobGriefing-gated)' fix hint was wrong per parity doctrine. Port: EntityCaterKiller.java makeStuckInBlock + customServerAiStep.)

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
- **Port:** `hostile_cater_killer__`* forest/jungle/taiga/badlands w4 1–2; no rules
- **Fix:** drop badlands, restore original biome weights; add rules (ENT-SYS-002).
- **Resolution:** FIXED (2026-06-13, Phase D1 — spawn-rule gate ported in checkSpawnRules citing orig CaterKiller.java:585-624; weights/biomes half closed in Phase C (2026-06-11, Phase C — orig biome/weight spread restored (dark_forest w10, birch w6, forest-hills w4 etc., orig OreSpawnMain.java:4746-4754), badlands dropped); see FIX_LOG.md)

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
- **Resolution:** FIXED (2026-08-11, Phase E4 ENT-A batch — invented ceiling-ambush removed: CaveFisherAmbushGoal.java deleted, its priority-2 registration stripped and the hurt()-side abortAnchor call removed (orig CaveFisher.java:51-55 registers only swim/wander(14)/watch-player(8)/look-idle; :163-183 is a flat 1-in-8 ground scan with no ceiling logic; :185-191 hurt is cactus-immunity only). Remaining goals renumbered 0-4. Base cadence verified against orig :168-177: BugMeleeAttackGoal.Params.caveFisher() = cadence 8 (nextInt(8)==0 scan), swing nextInt(7)==0 || nextInt(8)==1 (~25%), nav speed 1.2, reach 2.83 ~ sqrt(8) matching distSq<8. Port: CaveFisher.java registerGoals/hurt.)

### ENT-A-080 — CaveFisher: prey selection inverted (passive-mob predator → player hunter)

- **Status:** DIVERGENT
- **Original:** `CaveFisher.java:193-228` — hunts players & animals; excludes CaveFisher/EnderReaper/EnderKnight/all EntityMob
- **Port:** targets Player only via goal
- **Fix:** add `NearestAttackableTargetGoal<Animal>` with the original exclusion filter (no monsters, no CaveFisher/EnderReaper/EnderKnight).
- **Resolution:** FIXED (2026-06-11, Phase C — passive-mob predation restored via NearestAttackableTargetGoal (orig CaveFisher.java:193-228 excludes all EntityMob); see FIX_LOG.md and phase_c_reports/C1_entities_A_C.md)

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
- **Resolution:** FIXED (2026-08-11, Phase E3 — the finding was mostly stale: the full rider-flight system shipped in Phase B3 (port Cephadrome RIDER_FLIGHT_CONFIG cites orig :703-835 number-for-number: hover probe 1.55, +0.07/+0.1 lift, 0.018 glide-fall, terrain-scan climb, throttle/friction; mount flow :893-904) and the invented porkchop-tame flag/TemptGoal were removed by TF-032 (E0). The two REAL residuals fixed now: hitbox restored to 2.5x2.25 (orig Cephadrome.java:73 setSize; ModEntities had 1.5x1.5) and the ridden wing-beat added — activity==1 plays orespawn MothraWings 0.5f every 22 ticks server-side (orig :652-659), distinct from the already-faithful 1-in-6 unridden ambient (orig :184-189). See FIX_LOG Phase E)

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
- **Resolution:** FIXED (2026-06-13, Phase D1 — MobCategory MISC→AMBIENT, ON_GROUND placement, snowy-biome BM JSON, gate with badmood spawner bypass per orig Cephadrome.java; see FIX_LOG.md)

## Chipmunk

### ENT-A-086 — Chipmunk: MoveIndoors missing; tempt item apple→wheat

- **Status:** PARTIAL
- **Original:** `Chipmunk.java:52-63` — Tempt(apple)@4; MoveIndoors@11
- **Port:** `entity/Chipmunk.java:56-68` — Tempt(WHEAT)@4; no MoveIndoors
- **Fix:** tempt with `Items.APPLE`; add MoveIndoors-equivalent goal.
- **Resolution:** FIXED (2026-08-11, Phase E4 ENT-A batch — tempt item restored to APPLE (orig Chipmunk.java:56, EntityAITempt speed 1.2 apple field_151034_e, no-scare; port had WHEAT) and MoveIndoorsGoal added at the orig lowest-priority slot 11 (orig Chipmunk.java:63 EntityAIMoveIndoors; E3 roofed-shelter mapping, same as Camarasaurus/BandP); port entity/Chipmunk.java registerGoals. TF-034 rider: lump chipmunk row (w12 1-3, all-overworld) replaced by nine per-biome modifiers per orig OreSpawnMain.java:4757-4765 — forest w8 3-6, forestHills→windswept_forest w5 3-6, jungle w4 3-6, plains w2 1-2, birch_forest w5 3-6, birchForestHills→old_growth_birch w4 3-6, roofedForest→dark_forest w10 2-5, megaTaiga→old_growth_pine_taiga w2 2-5, taiga w6 2-5 — plus ModEntities CREATURE→AMBIENT (orig rows are EnumCreatureType.ambient; ENT-A-085 precedent), all as sharedEdits.)

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
- **Resolution:** FIXED (2026-07-02, Phase D4 — `chipmunk.json` rebuilt from the orig drop table, including the tamed-only poppy drop (orig Chipmunk.java:231-242, handled in-code per the established tamed-gate convention); see FIX_LOG.md)

### ENT-A-089 — Chipmunk: jukebox-dance lineage lost

- **Status:** PARTIAL
- **Original:** extends `EntityCannonFodder` (jukebox dance behavior)
- **Port:** extends `TamableAnimal` directly
- **Fix:** re-parent to the port's EntityCannonFodder (or copy its dance handler) to restore dancing near jukeboxes.
- **Resolution:** FIXED (2026-08-11, Phase E4 ENT-A batch — re-parented port Chipmunk from TamableAnimal to the port's EntityCannonFodder (orig Chipmunk.java:40-41 "public class Chipmunk extends EntityCannonFodder"), restoring the fodder lineage: hat-tame interact chain, activated-guard combat, NameOne/NameTwo + hat NBT. Audit's "jukebox dance" premise is wrong — dancing is MyEntityAIDance, wired only in Girlfriend (orig Girlfriend.java:151-152); orig EntityCannonFodder contains no dance code, so no dance handler was copied. sharedEdits restore Chipmunk-relevant fidelity inside the shared parent: target ranking via GenericTargetSorter (orig EntityCannonFodder.java:42, TF-035 style) and the orig species combat row — Chipmunk swings on the 6-gate for 3.0 damage (orig EntityCannonFodder.java:355-358) instead of the port's hardcoded 7/4.0 defaults.)

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
- **Resolution:** FIXED (2026-08-11, Phase E4 ENT-A batch — port's 1-in-9 hunt used only getNearestPlayer(12); prey ecosystem restored: findSomethingToAttack with PlayNicely gate (orig CloudShark.java:246-248), 12x10x12 LivingEntity scan (:249) sorted by GenericTargetSorter (:250; field :37, ctor init :45 — TF-035 swap-in), plus isSuitableTarget whitelist (:202-243): Butterfly/Cockateil/Mosquito/Firefly/non-creative Player/GoldFish/CliffRacer with RockBase+EntityAnt pre-exclusions and self/dead/LOS guards; creative players fall through to false exactly as orig :233-242. Steer-at-prey and distSq<9 bite kept (orig :153-162). Port: entity/CloudShark.java:37,126-136,152-192.)

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
- **Resolution:** FIXED (2026-08-11, Phase E4 ENT-A batch — port used vanilla despawn (no removeWhenFarAway override); added override returning !level().isDay() behind an isPersistenceRequired guard, reproducing func_70692_ba (orig CloudShark.java:61-66: persistence check :62-64, !isDaytime :65) — despawning permitted only at night. Modern Mob.checkDespawn routes both the hard-distance and idle-random despawn paths through removeWhenFarAway, so the single override gates both, matching 1.7.10 canDespawn semantics; mirrors the port's EntityTerribleTerror precedent. Port: entity/CloudShark.java:84-91.)

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
- **Resolution:** FIXED (2026-08-11, Phase E4 ENT-A batch — customServerAiStep now ports func_70619_bc line-for-line: 35-try retarget loop accepting only AIR targets that pass the 0.75-eye block-only LOS ray (orig Cockateil.java:166-168, 193-210), zdir-before-xdir RNG order, flyup field + setFlyUp() hook (:38, 156-158), Islands stayup=2 bias (:179-181; DimensionID4 = Dimension-Islands, WorldProviderOreSpawn4.java:23), decompiler literals 0.699999/0.200000001 (:216), heading atan2-90 with yaw += wrapDegrees(delta)/3 via setYRot, zza=0.8 (:218-221, clobbered by moveControl exactly as orig moveHelper clobbered moveForward); tick() restores null-init-else-damp with (int)-cast coords (:143-150). sharedEdit restores this.setFlyUp() in RubyBird's ctor (orig RubyBird.java:19, sole caller). TF-034 spawn-row replacement in sharedEdits.)

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
- **Resolution:** FIXED (2026-06-11, Phase C — orig 10-slot weighted jackpot restored in coin.json; the CoinEgg slot stays empty until the item is ported (Phase D); see FIX_LOG.md and phase_c_reports/C1_entities_A_C.md) — CLOSED (2026-07-02, Phase D4 — the CoinEgg slot filled: coin.json's remaining jackpot slot now yields the ported coin spawn egg, completing the orig 10-slot table)

### ENT-A-099 — Coin: natural overworld spawning impossible

- **Status:** MISSING
- **Original:** `OreSpawnMain.java:4942-4947` — ambient w2 1–1 taiga/forest/jungle/birch/coldTaigaHills/megaTaiga; rules day, y≥50, none within 20
- **Port:** `dim_village_locals.json` only; MobCategory.MISC (`ModEntities.java:298`) — natural cycle never picks it
- **Fix:** change category to AMBIENT; create overworld biome modifier w2 1–1 for the six biomes; add rules.
- **Resolution:** FIXED (2026-06-13, Phase D1 — MobCategory MISC→AMBIENT, ON_GROUND placement, overworld BM JSON, gate day/y>=50/no-other-Coin per orig Coin.java:138-148; see FIX_LOG.md)

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
- **Resolution:** FIXED (2026-08-11, Phase E4 ENT-A batch — port's filter allowed only Player+Monster; restored the full orig predicate (orig Crab.java:379-418): line-of-sight gate (:389), non-creative players (:392-395), fellow-crab exemption (:396-398), monsters (:399-401), plus Lizard (:402), RubberDucky (:405), Villager (:408), Girlfriend (:411), Boyfriend (:414) and the MyUtils.isAttackableNonMob fallthrough (:417). Also restored the PlayNicely aggression gate in findSomethingToAttack (orig :421-423) and swapped the plain distance comparator for GenericTargetSorter per TF-035 (orig :43,58,425), matching the EntityVortex reference migration. Port: entity/Crab.java isSuitableTarget/findSomethingToAttack/ctor.)

### ENT-A-105 — Crab: scale-based sound pitch formula missing

- **Status:** PARTIAL
- **Original:** `Crab.java` — pitch 2.0 − 0.3/scale
- **Port:** `Crab.java:190-210` — vol 0.75 ✓ but pitch formula absent
- **Fix:** apply `getVoicePitch() = 2.0f - 0.3f/scale`.
- **Resolution:** FIXED (2026-08-11, Phase E4 ENT-A batch — port had vol 0.75 but kept the vanilla random pitch jitter; added getVoicePitch() = 2.0f − 0.3f × (1/scale) (orig Crab.java:172-174 func_70647_i), deterministic by size: scale 0.25 → 0.8, 0.5 → 1.4, 1.0 → 1.7. Applies to the leaves_hit hurt voice (orig :160-162); the scorpion_attack/scorpion_living swing sounds keep their explicit 1.5 pitch (orig :360-362), untouched. Port: entity/Crab.java getVoicePitch.)

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
- **Resolution:** FIXED (2026-08-11, Phase E4 ENT-A batch — port lacked the village goal; added vanilla MoveThroughVillageGoal at priority 2 (orig CreepingHorror.java:53 — EntityAIMoveThroughVillage(this, 1.0, false)): speed 1.0 and onlyAtNight=false are the orig arguments; distanceToPoi 4 is the vanilla Zombie value (the 1.7.10 ctor had no such knob); canDealWithDoors=false. Mapping decision documented in the registerGoals Javadoc per the MoveIndoorsGoal precedent: the 1.7.10 door-graph village framework was removed in the 1.14 rework, so vanilla's POI-based goal is the honest modern equivalent. Remaining goals re-aligned to orig priorities :51-57 — wander@3, watch-player@4, look-idle@5. Port: entity/CreepingHorror.java registerGoals.)

### ENT-A-108 — CreepingHorror: target exclusions and LOS check dropped

- **Status:** PARTIAL
- **Original:** `CreepingHorror.java:147-200` — excludes RockBase, EnderReaper, LeafMonster, Dragon, TerribleTerror, LurkingTerror, PitchBlack, Firefly, Island(s); LOS required
- **Port:** `CreepingHorror.java:130-135` — excludes self-kind only; no LOS
- **Fix:** restore the exclusion list and `hasLineOfSight` requirement in target selection.
- **Resolution:** FIXED (2026-08-11, Phase E4 ENT-A batch — port excluded only self-kind with no LOS; restored orig CreepingHorror.java:147-200: getSensing().hasLineOfSight gate (:157) and the full exclusion list — RockBase (:163), EnderReaper (:166), LeafMonster (:169), Dragon (:172), TerribleTerror (:175), LurkingTerror (:178), PitchBlack (:181), Firefly (:184), Island (:187), IslandToo (:190) — creative players exempt (:193-198), everything else fair game (:199). Also restored the PlayNicely aggression gate in findSomethingToAttack (orig :203-205) and swapped the plain distance comparator for GenericTargetSorter per TF-035 (orig :42,58,207). Port: entity/CreepingHorror.java isSuitableTarget/findSomethingToAttack/ctor.)

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
- **Resolution:** VERIFIED-CORRECT (2026-08-11, Phase E4 ENT-A batch — the flagged override no longer exists in CrystalCow.java: after ENT-A-114 re-parented it to RedCow, never-despawn comes by inheritance exactly as the orig — 'public class CrystalCow extends RedCow' (orig CrystalCow.java:13-14) inherits 'protected boolean func_70692_ba() { return false; }' (orig RedCow.java:40-42), faithfully ported as RedCow.removeWhenFarAway→false (port RedCow.java:31-34). The inheritance is documented in the CrystalCow header comment (port CrystalCow.java:15-16), satisfying the 'document if kept' remainder. Behavior is additionally identical on every path: 1.7.10 EntityAnimal.canDespawn and 1.21.1 Animal.removeWhenFarAway both already return false for animals.)

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
- **Resolution:** FIXED (2026-06-13, Phase D1 — spawn-rule gate ported in checkSpawnRules citing orig Dragon.java:598-611; weights/biomes half closed in Phase C (2026-06-11, Phase C — overworld entry removed, Island-biome w1 1–2 added (orig list is `setIslandCreatures`, not the Utopia boss list)); see FIX_LOG.md)

### ENT-D-005 — Dragon: custom wing-flap sound replaced

- **Status:** PARTIAL
- **Original:** custom wing flap sound (alongside `orespawn:roar`/`alo_hurt`/`alo_death`, which are kept)
- **Port:** flap = `SoundEvents.ENDER_DRAGON_FLAP`
- **Fix:** register and play the original orespawn flap sound asset instead of the vanilla ender-dragon flap.
- **Resolution:** FIXED (2026-08-11, Phase E4 ENT-D batch — flap sound restored: orig Dragon.java:641-648 plays custom "orespawn:MothraWings" at 0.5f/1.0f every 21 ticks while flying (activity==1), server-side; port tick() had substituted SoundEvents.ENDER_DRAGON_FLAP with otherwise-identical timing/volume/pitch. Swapped to the orespawn:mothrawings event (already in sounds.json line 611 with 3 variants — no shared edit needed) via SoundEvent.createVariableRangeEvent, matching the Mothra.java:172-173 idiom. TF-035 rider applied: orig Dragon.java:79 (field), :120 (ctor), :581 (Collections.sort) use GenericTargetSorter; port's plain Comparator.comparingDouble(this::distanceToSqr) replaced with new GenericTargetSorter(this).)

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
- **Resolution:** FIXED (2026-06-13, Phase D1 — spawn-rule gate ported in checkSpawnRules citing orig DungeonBeast.java:275-312; weights/biomes half closed in Phase C (2026-06-11, Phase C — dark_forest w20 2–4 + Crystal w30 4–6 + Chaos w2 1–5 done); see FIX_LOG.md)

## EasterBunny

### ENT-D-010 — EasterBunny: mob-egg-laying and carrot taming missing

- **Status:** MISSING
- **Original:** `EasterBunny.java` interact/update — lays mob eggs; tamed with carrot
- **Port:** `entity/EasterBunny.java` — neither present
- **Fix:** port the egg-laying tick (item/entity eggs per original logic) and carrot-based taming interaction.
- **Resolution:** FIXED (2026-07-02, Phase D4 — mob-egg laying ported with the full 115-entry mob→spawn-egg lookup (script-extracted from the orig table and mapped to the port's spawn-egg items) and carrot taming restored; natural spawns additionally gated to Easter via checkSpawnRules (ANIM-016); see FIX_LOG.md)

### ENT-D-011 — EasterBunny: spawn weights collapsed

- **Status:** DIVERGENT
- **Original:** `OreSpawnMain.java:4682-4688` — addSpawn w10/w5/w8 1–2 across 7 biomes
- **Port:** `add_overworld_creatures.json` w3 1–2 overworld-wide
- **Fix:** dedicated modifier reproducing the 7 original biomes at weights 10/8/5.
- **Resolution:** FIXED (2026-06-11, Phase C — 7-biome w10/8/5 modifiers done; the orig registration is gated on Easter day (April 20, OreSpawnMain.java:4570-4571,4681) → the seasonal-gates slice (Phase D; spawn-rule gate itself ported in D1, orig EasterBunny.java:67-77), see phase_c_reports/C2_entities_D_I.md) — CLOSED (2026-07-02, Phase D4 — the Easter-day gate is now live via SeasonalDates.isEaster() in EasterBunny.checkSpawnRules; ANIM-016)

## Elevator (Hoverboard)

### ENT-D-012 — Elevator/Hoverboard: hover hum remapped to beacon

- **Status:** DIVERGENT
- **Original:** `orespawn:hover` hum
- **Port:** `entity/HoverboardEntity.java` — `SoundEvents.BEACON_AMBIENT`
- **Fix:** register `orespawn:hover` and play it instead of the beacon ambient.
- **Resolution:** FIXED (2026-06-11, Phase C — see FIX_LOG.md and phase_c_reports/C2_entities_D_I.md. `HoverboardEntity` itself was removed in Phase D2 (ENT-D-066); the `orespawn:hover` hum now plays from `entity/Elevator.java`, same 1-in-80 dice / 0.45 vol / 55-tick cooldown, orig Elevator.java:297-303.)

### ENT-D-066 — Elevator/Hoverboard: duplicate invented hoverboard shipped alongside the faithful port (found 2026-07-02, Phase D2)

- **Status:** DIVERGENT
- **Original:** ONE item and ONE entity: `MyElevator` ("elevator", displayed **"Hoverboard"**, orig OreSpawnMain.java:1904/5174, recipe `WWW/DRD` planks+redstone+diamonds orig :5374) spawning the `Elevator` class registered as "Hoverboard" (orig OreSpawnMain.java:3879-3883).
- **Port:** TWO parallel ports: the `elevator` item/entity (original recipe; displayed "Elevator") AND a "Phase 10" `hoverboard` item + `HoverboardEntity` (creative-tab only, no recipe) with reinvented, non-original physics (sprint-key boost instead of the fly-up key, invented NaN guards/riderless drift, server-side `travel`).
- **Fix:** keep exactly one, faithful to the original; restore the "Hoverboard" display name.
- **Resolution:** FIXED (2026-07-02, Phase D2, owner decision — the Phase 10 duplicate removed entirely: `HoverboardEntity`/`HoverboardItem`/`HoverboardModel`/`HoverboardRenderer`, the `hoverboard` item/entity registrations, creative-tab row, item model and lang keys deleted; not archived to MODERNIZATION_NOTES because it duplicated an original feature rather than adding content. `elevator` item/entity display names corrected to "Hoverboard" per orig OreSpawnMain.java:5174/3880-3881. **World-compat impact:** placed `orespawn:hoverboard` entities and `orespawn:hoverboard` items in existing port worlds are dropped on load (unknown id). The faithful Elevator entity port itself is ANIM-012's D2 closure; see FIX_LOG.md and phase_d_reports/D2_gait_elevator.md)

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
- **Resolution:** FIXED (2026-06-13, Phase D1 — spawn-rule gate ported in checkSpawnRules citing orig EnderKnight.java:256-277; weights/biomes half closed in Phase C (2026-06-11, Phase C — 9-biome overworld modifiers + Chaos-dim w2 1–2 added, invented End entry removed); see FIX_LOG.md)

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
- **Resolution:** VERIFIED-CORRECT (2026-08-11, Phase E4 ENT-D batch — audit premise refuted: orig EnderReaper.java (full 288-line read) contains NO explosion — no func_72876_a/func_72885_a/newExplosion anywhere. Provocation reactions are screaming+teleports only: hurt() sets screaming (orig :242) and teleports 16x on indirect damage (:243-248); stare triggers "mob.endermen.stare" (:68-70); stared-at player within dist-sq 16 triggers teleportRandomly (:126-130) — all ported (port EnderReaper.java:96-113). Repo greps: none of the 12 orig explosion-creating files reference EnderReaper; none of the 23 orig EnderReaper-referencing files explode; no port shared handler ties an explosion to EnderReaper (RandomDungeonSpawnerBlockEntity:182 / EntityCage:296-300 matches are cosmetic GENERIC_EXPLODE sound/particles for egg placement and cage capture). Port correctly has no explosion.)

### ENT-D-021 — EnderReaper: overworld (w38 roofed forest) → End-only

- **Status:** DIVERGENT
- **Original:** `OreSpawnMain.java:4931-4939` — ambient w2/1 1–2 across 8 biomes + w38 2–4 Roofed Forest
- **Port:** `add_end_spawns.json` w4 1–1 End-only
- **Fix:** add overworld modifier with the 8 original biomes incl. dark_forest w38 2–4; remove/reduce the End entry.
- **Resolution:** FIXED (2026-06-13, Phase D1 — spawn-rule gate ported in checkSpawnRules citing orig EnderReaper.java:253-279; weights/biomes half closed in Phase C (2026-06-11, Phase C — 9-biome overworld modifiers + Island w25 2–4 + Chaos w1 1–1 added, invented End entry removed); see FIX_LOG.md)

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
- **Resolution:** FIXED (2026-08-11, Phase E4 ENT-D batch — invented Golden/Enchanted-Apple hats removed; orig interaction chain restored in port entity/EntityCannonFodder.java mobInteract: super-first (orig EntityCannonFodder.java:83-85), name-slot promotion to is_activated=2 incl. the slot-one-steal bug (:86-106), carrot=hat1 (:107-125), potato=hat3 (:126-144), quinoa=hat2 (:145-163) each taming to the SLOT-ONE name (:116) with hearts (:117), full heal (:118), persistence (:119); MyCornCob cloning via species table Ostrich/Lizard/Chipmunk/VelocityRaptor (:164-189, spawnCreature :205-214) copying owner/tame/setStuff (:177-181) with random.explode 0.75f/2.0f (:183); item-agnostic sit-toggle with heart/smoke events (:190-202). setStuff regains setPersistenceRequired (:221). Lizard/VelocityRaptor selected by exact-type checks until their batches re-parent them.)

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
- **Resolution:** FIXED (2026-08-11, Phase E4 ENT-D batch — water-skip ported into port entity/EntityThrownRock.java tick(): the probe position is captured BEFORE super.tick() with plain (int) truncation, orig bug kept (orig EntityThrownRock.java:291-293); when the probed block is still water and -0.55 < motionY < -0.15 with float-summed horizontal speed squared > 0.5f, vertical motion reflects and all three axes keep 3/4 velocity (orig :307-312, exact 3.0/4.0 math). Orig compares against field_150355_j (still water id 9) only — flowing id 8 never skipped — so the port gates on Blocks.WATER + FluidState.isSource(), the 1.21 equivalent of the old still block. Runs on both sides, as the orig did.)

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
- **Resolution:** FIXED (2026-06-13, Phase D1 — spawn-rule gate ported in checkSpawnRules citing orig Fairy.java:334-347; weights/biomes half closed in Phase C (2026-06-11, Phase C — overworld-wide entry replaced with dark_forest w25 2–4 (Crystal/Chaos dims already correct)); see FIX_LOG.md)

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
- **Resolution:** FIXED (2026-06-13, Phase D1 — spawn-rule gate ported in checkSpawnRules citing orig Flounder.java:219-230; weights/biomes half closed in Phase C (2026-06-11, Phase C — Utopia w2 2–4 + Crystal w5 6–8 added, invented ocean entry removed); see FIX_LOG.md)

## Frog

### ENT-D-033 — Frog: river/swamp focus → all overworld

- **Status:** DIVERGENT
- **Original:** `OreSpawnMain.java:4963-4967` — waterCreature w20 3–6 river, w20 2–6 swamp, ambient w2–3
- **Port:** `add_overworld_creatures.json` w10 1–2 (rules Y≥50/day/≤5 frogs kept)
- **Fix:** retarget modifier to river+swamp biomes w20 3–6 / 2–6.
- **Resolution:** FIXED (2026-06-13, Phase D1 — spawn-rule gate ported in checkSpawnRules citing orig Frog.java:240-251; weights/biomes half closed in Phase C (2026-06-11, Phase C — river w20+w3, swamp w20+w2, jungle w3 modifiers + Utopia w5 4–6 + Crystal w1 3–5 added, overworld-wide entry removed); see FIX_LOG.md)

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
- **Resolution:** FIXED (2026-06-13, Phase D1 — spawn-rule gate ported in checkSpawnRules citing orig GammaMetroid.java:328-365; weights/biomes half closed in Phase C (2026-06-11, Phase C — audit corrected: ChunkProviderOreSpawn2:386 is the MINING dim (w35 4–7) and the w1 1–1 list is `setChaosCreatures` (:513-514)); see FIX_LOG.md)

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
- **Resolution:** FIXED (2026-06-11, Phase C — the UNGATED block (OreSpawnMain.java:4783-4788: snowy_taiga w15 5–10, taiga w10 5–10, frozen_river w6 4–6, jungle w2 1–4, dark_forest w15 2–5) is now in JSON; invented cave/Chaos entries removed. The 22-biome w15 3–6 block (:4544-4565) is Halloween-only (Oct 31 gate :4518-4521) → the seasonal-gates slice (Phase D); the spawn-rule gate itself was ported in D1 (orig Ghost.java:145-160); see phase_c_reports/C2_entities_D_I.md) — CLOSED (2026-07-02, Phase D4 — the Halloween 22-biome block added as halloween_ghosts.json, runtime-gated by SeasonalDates.isHalloween() in Ghost.checkSpawnRules with the 5 ungated biomes exempt; ANIM-016)

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
- **Resolution:** FIXED (2026-06-11, Phase C — ungated block (OreSpawnMain.java:4790-4795, same five biomes/weights as Ghost) in JSON; invented cave/Chaos entries removed; Halloween 22-biome block (:4522-4543) → the seasonal-gates slice (Phase D); the spawn-rule gate itself was ported in D1 (orig GhostSkelly.java:173-188), see phase_c_reports/C2_entities_D_I.md) — CLOSED (2026-07-02, Phase D4 — same as ENT-D-039 for GhostSkelly: halloween_ghosts.json + SeasonalDates gate in checkSpawnRules; ANIM-016)

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
- **Resolution:** FIXED (2026-08-11, Phase E4 ENT-D batch — restored both dropped goals and removed the invented stroll: orig GiantRobot.java:54 MyEntityAIWanderALot(14,1.0) now at priority 1, replacing the port's WaterAvoidingRandomStrollGoal invention; orig :55 EntityAIMoveThroughVillage((double)0.9f,false) mapped to vanilla MoveThroughVillageGoal(0.9f,false,4,()->false) at priority 2 per the established Alien/Alosaurus mapping (POI-driven descendant; distanceToPoi 4 has no 1.7.10 analog; no door-breaking). Float/LookAt/RandomLook priorities 0/3/4 and HurtByTarget already matched orig :53,56-58. TF-035 rider applied: plain Comparator.comparingDouble(this::distanceToSqr) swapped for GenericTargetSorter, citing orig :39 (field), :51 (ctor), :347 (sort). Port: entity/GiantRobot.java:49-76,203.)

### ENT-D-044 — GiantRobot: signature LaserBall barrage missing

- **Status:** MISSING
- **Original:** `GiantRobot.java:264-283` — fires LaserBall: aims within 0.5 rad, reload 10 close (vol 2.5 pitch 1.0) / 25 + `setSpecial()` far >100 distSq (vol 3.5 pitch 0.5), launch offset y+10
- **Port:** melee only; `reloadTicker` field exists but no firing code
- **Fix:** implement the ranged attack in `customServerAiStep`: aim gate 0.5 rad, reload 10/25 by range, `setSpecial()` on far shots, y+10 launch offset, original volumes/pitches.
- **Resolution:** FIXED (2026-06-13, Phase D3 — `fireLaserBall` ported with the 0.5 rad aim gate (melee nested inside it per orig :256-263), reload 10/25 keyed on distSq 100, `setSpecial()` on far shots, muzzle/launch offsets and original volumes/pitches; see FIX_LOG.md)

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
- **Resolution:** FIXED (2026-06-13, Phase D1 — village roster entry was restored in C7 (w8 1-2 per BiomeGenUtopianPlains.java:289); checkSpawnRules gate ported this slice (orig GiantRobot.java:364-381); see FIX_LOG.md)

## Girlfriend

### ENT-D-047 — Girlfriend: Valentine's Day 800-HP mode missing

- **Status:** PARTIAL
- **Original:** `Girlfriend.java:569-571` — HP 800 on Valentine's Day (else 80)
- **Port:** `entity/Girlfriend.java:51-52,83-88` — HP 80 always
- **Fix:** check system date (Feb 14) on spawn/load and set max health 800 with heal.
- **Resolution:** FIXED (2026-08-11, Phase E4 ENT-D batch — D3 scaffolding verified: 800/80 mygetMaxHealth (orig Girlfriend.java:569-574), 2.5x8.0 ctor size (:142-144), rose-sword cure (:1081-1094), inWall immunity (:1078-1080) all present. Remainder completed: both mobInteract poppy heals now heal to getMaxHealth() — orig :630,:641-643 heal to mygetMaxHealth (800 mid-valentine); the flat-80 constant under/negative-healed the giant — and onSyncedDataUpdated now refreshDimensions on the feelingBetter accessor so clients shrink the cured giant (orig :600-607 force_sync resize). sharedEdit returned: ModEntities .sized(0.6,1.8) → (0.5,1.6) per orig :141 base size, which getDefaultDimensions falls back to. Port entity/Girlfriend.java:136-149,375-381,388-394.)

### ENT-D-048 — Girlfriend: dance, jealousy, Valentine targeting, door/indoor AI missing

- **Status:** PARTIAL
- **Original:** `Girlfriend.java:149-173` — `MyEntityAIDance(3)`, OpenDoor(10), MoveIndoors(11); targets MyValentineTarget ×2, MyEntityAIJealousy ×2
- **Port:** `entity/Girlfriend.java:70-80` — none wired (port's `MyEntityAIDance.java` exists but is NOT registered); no Jealousy/Valentine classes
- **Fix:** register `MyEntityAIDance` at priority 3; port Jealousy and ValentineTarget goal classes; add OpenDoor/MoveIndoors.
- **Resolution:** FIXED (2026-08-11, Phase E4 ENT-D batch — goal list completed vs orig Girlfriend.java:149-175: MyEntityAIDance @3 held in the public Dance field (orig :71,151-152; assigned in registerGoals during super-ctor, so no field initializer), PanicGoal(1.5)@6 (:155), OpenDoorGoal@10 + GroundPathNavigation.setCanOpenDoors in ctor (:159; Boyfriend idiom, port Boyfriend.java:95-98), MoveIndoorsGoal@11 (:160, documented mapping), JealousyTargetGoal(Girlfriend.class, 6.0,5)@4 and (3.0,15)@5 (:169-174, PlayNicely gated in-goal per Boyfriend :120-127). Dance made faithful: floor is gold/diamond/emerald+ruby/amethyst/titanium/uranium (orig MyEntityAIDance.java:30 — invented jukebox/noteblock/beacon REMOVED), lowest-id group-sync scan ported bug-for-bug (:127-137), motionY SET to 0.25 in moves 4/10 (:172,238), requiresUpdateEveryTick added; ambient voice hushes mid-dance (orig :858-860). ValentineTarget pair @1/2 verified from D3.)

### ENT-D-049 — Girlfriend: ranged UltimateArrow attack missing

- **Status:** MISSING
- **Original:** `Girlfriend.java` — `EntityAIArrowAttack(4, 1.25, 20t, 10.0f)` + IRangedAttackMob firing UltimateArrow
- **Port:** `MeleeAttackGoal(4)` only
- **Fix:** implement `RangedAttackMob` with an arrow-attack goal (speed 1.25, 20t interval, 10.0 range) firing UltimateArrow.
- **Resolution:** FIXED (2026-06-13, Phase D3 — `RangedAttackMob` + `RangedAttackGoal(1.25, 20, 10.0f)`; `performRangedAttack` fires UltimateArrow (UltimateBow held) or Shoes id 2-5; armed melee with the 25t cooldown, `o_`-prefixed sounds and the 1-in-200 target-clear roll ported alongside; see FIX_LOG.md)

### ENT-D-050 — Girlfriend: spawn hotspots flattened

- **Status:** DIVERGENT
- **Original:** `OreSpawnMain.java:4574-4585` — 12 addSpawn w2–30, groups up to 8–15
- **Port:** `companion_girlfriend.json` — overworld-wide w4 1–2
- **Fix:** replicate the 12 per-biome entries with original weights/groups (w30 8–15 hotspots).
- **Resolution:** FIXED (2026-06-13, Phase D1 — spawn-rule gate ported in checkSpawnRules citing orig Girlfriend.java:1100-1115; weights/biomes half closed in Phase C (2026-06-11, Phase C — all 12 orig entries replicated across 7 per-biome modifier files (beach w30 8–15 hotspot)); see FIX_LOG.md)

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
- **Resolution:** FIXED (2026-07-02, Phase D4 — `gold_fish.json` loot added per the orig GoldFish drop table; see FIX_LOG.md)

### ENT-D-053 — GoldFish: Utopia habitat → oceans/Chaos

- **Status:** DIVERGENT
- **Original:** Utopia lists w1/w5/w10 (`BiomeGenUtopianPlains.java:120,176,368`)
- **Port:** `add_ocean_spawns.json` w10 1–3 + `dim_chaos_locals.json` w10 2–4
- **Fix:** add GoldFish to the Utopia dimension lists at original weights; review whether ocean entry should remain.
- **Resolution:** FIXED (2026-06-13, Phase D1 — spawn-rule gate ported in checkSpawnRules citing orig GoldFish.java:153-155; weights/biomes half closed in Phase C (2026-06-11, Phase C — Utopia w1 1–1 + Island w5 2–4 added (Chaos w10 2–4 already correct), invented ocean entry removed); see FIX_LOG.md)

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
- **Resolution:** FIXED (2026-06-13, Phase D1 — spawn-rule gate ported in checkSpawnRules citing orig Hammerhead.java:277-316; weights/biomes half closed in Phase C (2026-06-11, Phase C — the orig w1 1–1 list is `setChaosCreatures` (BiomeGenUtopianPlains.java:462-464)); see FIX_LOG.md)

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
- **Resolution:** FIXED (2026-06-13, Phase D1 — spawn-rule gate ported in checkSpawnRules citing orig Irukandji.java:326-337; weights/biomes half closed in Phase C (2026-06-11, Phase C — the orig w4 2–3 list is the Crystal sub-biome water list (BiomeGenUtopianPlains.java:255-257)); see FIX_LOG.md)

## IrukandjiArrow

### ENT-D-064 — IrukandjiArrow: base damage parity unverified

- **Status:** UNVERIFIED
- **Original:** `IrukandjiArrow.java:173-180` — damage scaled by velocity + `nextInt(dmg/2+2)` crit bonus via custom `func_70239_b`; base value buried in decompiled arrow math
- **Port:** `entity/IrukandjiArrow.java` — extends `AbstractArrow`, base damage 6.0
- **Fix:** verification failed because the original base damage is entangled in CFR-decompiled velocity/crit math rather than a named constant. Evidence to resolve: trace `func_70239_b` callers in `IrukandjiArrow.java` (and the bow that fires it) to extract the seeded damage value, then compare to the port's 6.0. Do not assume parity.
- **Resolution:** FIXED (2026-08-11, Phase E2 — audit assumption inverted: the orig arrow is FLAT 100, never velocity-scaled (orig IrukandjiArrow.java:157 `var23 = 100.0f`; `func_70239_b` is an EMPTY override :269-270 so no caller reseeds it; `func_70242_d` returns 100 :272-273); crit adds nextInt(52) :172-173. Port rewritten: flat 100 + crit + the :158-170 ultimateSwordPvp guard (players/Girlfriend/Boyfriend/tamed no-sold) + Punch knockback + deflect-on-no-sell; the port's velocity-scaled 6.0 AND its three invented on-hit potion effects (no potion code exists in the orig arrow) removed. See FIX_LOG Phase E)

### ENT-D-065 — IrukandjiArrow: debuff durations/amplifiers not number-matched

- **Status:** PARTIAL
- **Original:** poison/weakness/slowness applied on hit; exact durations/amps in original constants not extracted
- **Port:** Poison + Weakness + Slowness, 200 ticks, amps 1/2
- **Fix:** read the original potion-application calls in `IrukandjiArrow.java` and align port durations/amplifiers; current 200t/amps 1–2 are plausible but unconfirmed.

---
- **Resolution:** FIXED (2026-08-11, Phase E4 ENT-D batch — superseded by ENT-D-064's E2 rewrite, which extracted the evidence this finding asked for: the ORIGINAL IrukandjiArrow applies NO potion effects at all — zero Potion/func_70690 references anywhere in orig IrukandjiArrow.java; its hit path is a flat 100 damage (:157) with a crit roll and pvp/tamed no-sell guard, nothing else. This finding's 'Original: poison/weakness/slowness applied on hit' premise was an unverified audit assumption; the port's Poison III/Weakness II/Slowness II 200t effects were the invention. They were removed with the E2 flat-100 rewrite (port entity/IrukandjiArrow.java — onHitEntity now mirrors orig :155-200 exactly), so there are no durations/amplifiers left to align. The Irukandji JELLYFISH's sting debuffs are a separate, already-terminal concern)

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
- **Resolution:** FIXED (2026-06-13, Phase D1 — rosters were restored by the Phase C7 dimension-roster rebuild (Robot1-5 + PitchBlack in village/chaos/island biome JSONs, SpiderDriver village w20 3-5); their `func_70601_bi` gates ported this slice (Robot1-5, PitchBlack, SpiderDriver, GiantRobot); see FIX_LOG.md)

### ENT-SYS2-004 — Systemic: original spawn gates (spawner blocks, darkness, Y-bands, crowd caps) absent

- **Status:** MISSING
- **Original:** per-entity `func_70601_bi` checks: spawner-block proximity, darkness, Y ranges, nearby-buddy caps, dimension checks
- **Port:** most biome-modifier spawns have no `checkSpawnRules` override. Affected (this register's scope): Kraken, LeafMonster, LurkingTerror, Mantis, Molenoid, Nastysaurus, Peacock (`findBuddies()` exists but never called, `Peacock.java:111-114`), Rat, Rotator, Tshirt, Scorpion
- **Fix:** add `checkSpawnRules` overrides per entity replicating darkness/Y/crowd gates; for spawner-driven mobs, gate natural spawning behind config or remove BM entry. Medium priority.
- **Resolution:** FIXED (2026-06-13, Phase D1 — same slice as ENT-SYS-002: Kraken/LeafMonster/LurkingTerror/Mantis/Molenoid/Nastysaurus/Peacock/Rat/Rotator/Tshirt/Scorpion gates all ported with original bounds; Peacock findBuddies cap now enforced; see FIX_LOG.md)

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
- **Resolution:** VERIFIED-CORRECT (2026-08-11, Phase E4 ENT-K batch — the claimed 120-tick window never existed. Orig Kraken.java:79 sets field_70174_ab=120 ONCE in the ctor, and that SRG is 1.7.10 Entity.fireResistance, a vestigial field vanilla never reads (INDEX.md's invulnerableTime row is wrong for it). The real per-hit gate is the custom timer, orig Kraken.java:1158-1161: `if (this.hurt_timer > 0) return false; this.hurt_timer = 30;`, decremented in updateAITasks orig :908-910 — matched verbatim by port Kraken.java hurt() (`hurtTimer = 30` gate) and the customServerAiStep decrement. Setting hurtTimer=120 would CREATE a 4x divergence; not applied. Analysis documented in-code. TF-035 rider applied: plain distance comparator swapped for GenericTargetSorter per orig Kraken.java:57,81.)

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
- **Resolution:** FIXED (2026-07-02, Phase D4 — fire immunity restored per orig Kyuubi.java (field_70178_ae = true), so its own fire attacks no longer self-damage; see FIX_LOG.md)

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
- **Resolution:** FIXED (2026-08-11, Phase E4 ENT-K batch — irukandji miss-drop restored: new onHitBlock drops one dead_irukandji (orig LaserBall.java:137-139 func_145779_a(MyIrukandji,1); item identity orig OreSpawnMain.java:1750,2299). Impact-effects block ported (orig :140-155): smoke/largesmoke/fireworksSpark burst x10, x20 when special (orig :141-149; the missing '- nextFloat()' on the smoke Z is reproduced), explode sound 0.5 vol / 1.0±0.5 pitch on every non-acid impact (orig :150), and spared-target early-returns now skip ALL effects (orig :78-132) — the port previously exploded special balls on immune robots/ridden dragons. Explosion fire flag corrected to false (orig :151-153: isSmoking=mobGriefing via ExplosionInteraction.MOB, isFlaming always false; port was passing mobGriefing as fire). In-flight reddust trail added, reproducing the 1.7.10 color-args-as-velocity bug (orig :182-183).)

## Lavafoam (block)

### ENT-K-011 — Lavafoam: Nether bonus drops missing

- **Status:** MISSING
- **Original:** ORIG `Lavafoam.java` — drops 5–14 items when broken in the Nether (dim −1)
- **Port:** `block\Lavafoam.java` — no loot override / dimension check
- **Fix:** add a loot table (or `spawnAfterBreak` override) granting 5–14 bonus items when `level.dimension() == Level.NETHER`.

---

## LeafMonster
- **Resolution:** FIXED (2026-07-02, Phase D4 — Nether bonus restored: breaking lavafoam in the Nether grants 5 + nextInt(5) + nextInt(5) = 5-13 XP via getExpDrop (orig Lavafoam.java:110-116); see FIX_LOG.md)

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
- **Resolution:** FIXED (2026-08-11, Phase E4 ENT-K batch — invented all-overworld w4/1-2 lump row removed from add_overworld_monsters.json; replaced by 7 per-biome hostile_leaf_monster__*.json reproducing all 8 orig ambient addSpawn rows (OreSpawnMain.java:4910-4917): jungle 5/2-6, forest 5/1-2, sparse_jungle=jungleHills 3/2-4, windswept_forest=forestHills 3/1-2, birch_forest 3/3-6, old_growth_birch=birchForestHills 2/3-6, taigas merged megaTaiga+taiga 2/2-5 per bee/beaver precedent. Gating half verified already correct: EntityLeafMonster.checkSpawnRules:182-194 matches orig LeafMonster.java:227-251 (spawner bypass -3..2/0..4, darkness, !daytime, Islands y<=20 else y>=50, <=4 buddies 20/10/20). TF-035: plain comparator swapped for GenericTargetSorter per orig :36/:48/:214.)

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
- **Resolution:** FIXED (2026-08-11, Phase E5 — EntityLeon.doHurtTarget ports orig Leon.java:275-301: Ender Dragon hit through a dragon part with an attacker-less explosion source (damageSources().explosion(null,null) = func_94539_a(null)+func_94540_d), 1-in-6 head part else body part (:279-288; body via getSubEntities — only head is public in 1.21.1), 55.0 damage, no knockback; x4 damage vs Kraken (:290-292); knockback 1.25/0.15-0.3 unchanged (:294-298). The audit's 'hurt window 10' misread orig :322 (hurt_timer=15, already ported); orig :83's maxHurtResistantTime=10 is NOT ported — invulnerableDuration is final in 1.21.1 and unobservable behind the 15-tick full-block gate; documented in the ctor)

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
- **Resolution:** FIXED (2026-08-11, Phase E5 — by the TF-030 consolidation: orespawn:leonopteryx now runs EntityLeon, which carries the full orig sound set via createVariableRangeEvent: leon_living ambient gated on activity==1 && riderless (orig Leon.java:208-216), leon_hit (:218-220), leon_death (:222-224), volume 1.75 (:226-228), pitch 0.85 (:230-232), the 20-tick mothrawings flap at 0.5f (:508-516). sounds.json already defines every event. The sound-less twin class Leonopteryx.java is deleted)

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
- **Resolution:** FIXED (2026-08-11, Phase E4 ENT-K batch — EntityLurkingTerror.isSuitableTarget rewritten to full orig exclusion list (LurkingTerror.java:271-348): own kind, RockBase, EnderReaper, LeafMonster, TerribleTerror, Mothra, CloudShark, Rotator, Bee, Mantis, CreepingHorror, Triffid (orig dupe-check :317/:338 noted in comment), PitchBlack, Dragon, Island, IslandToo, Butterfly, Firefly, creative players; everything else targetable. Audit's 'flying mobs' was a paraphrase — orig has no isNoGravity check, only this class list. Also restored missing PlayNicely gate in findSomethingToAttack (orig :350-353, matching LeafMonster port idiom) and TF-035 GenericTargetSorter swap (orig :48 field, :58 ctor, :355 sort).)

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
- **Resolution:** FIXED (2026-08-11, Phase E4 ENT-K batch — orig has NO addSpawn row for LurkingTerror anywhere (grepped whole reference source; spawner/egg paths only), so the port's overworld w2/1-1 lump row was invented — removed from add_overworld_monsters.json; no replacement BM files. Rule gates verified already correct: port checkSpawnRules (EntityLurkingTerror.java:204-215 pre-edit numbering) matches orig LurkingTerror.java:237-269 — spawner bypass -2..1/0..4, darkness, DAYTIME required (orig quirk, :255), 1-in-2 dice (:258), Chaos extra 1-in-6 (:261, DimensionID6→orespawn:chaos per ModDimensionKeys — audit's 'Islands-dim special' was wrong), no other LT in 32/16/32 (:264), y>=10 (:268).)

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
- **Resolution:** FIXED (2026-08-11, Phase E4 ENT-K batch — full orig prey list restored in EntityMantis.isSuitableTarget: non-creative players decided first (orig Mantis.java:327-330, instabuild), exclusions Irukandji/Skate/Flounder/Whale/Squid/WaterDragon/AttackSquid/TerribleTerror/LurkingTerror/CloudShark/Rotator/Bee/Mothra in original order (:331-372), then prey grants Monster (:373), EntityButterfly (:376), Cockateil (:379), Fairy (:382) and the MyUtils.isAttackableNonMob fallback (:391); the second player branch (:385-390) is unreachable dead code, noted not reproduced. findSomethingToAttack gains the missing PlayNicely gate (:395-397) and GenericTargetSorter per TF-035 (field :49, ctor :62, sort :399). Port: EntityMantis.java:226-276.)

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
- **Resolution:** FIXED (2026-08-11, Phase E2 — evidence gathered: orig MantisClaw = ItemSword(toolEMERALD) (OreSpawnMain.java:1661), emerald_stats dmg 6 (:1512), so 1.7.10 attack = 4+6 = 10; the audit's 'dmg 10' was a coincidence — orig MantisClaw.java:17/23 `weaponDamage` is a private field NOTHING reads. Port EMERALD tier carries dmg 6.0 exactly; +3 modern base per the accepted ENT-A-045 convention; durability 1000 ✓ (orig :25 override, port getMaxDamage). Fixed the one real divergence: class ctor passed AMETHYST (ench 70/amethyst repair) where orig uses toolEMERALD (ench 75/emerald repair) — now ModToolTiers.EMERALD. See FIX_LOG Phase E)

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
- **Resolution:** FIXED (2026-08-11, Phase E4 ENT-K batch — audit half-right: port's -3sin/+3cos clear column already pointed AHEAD (orig Molenoid.java:226-227); renamed clearPathAhead and completed with target-relative column base dir 0/1/2 (:230-238), classic-leaves clearing (:242, 4-variant field_150362_t mapping per Camarasaurus precedent), ungated mole-dirt self-clear (:245-246), PlayNicely gate (:239). Ported the missing speed-proportional mole-dirt spoil 6 blocks BEHIND the head (:207-225). throwBlocksAtTarget now places ModBlocks.MOLE_DIRT not vanilla DIRT, PlayNicely-gated, invented mobGriefing gate removed (:183-196). Also restored MyCanSee dig-vision raycast (:344-394), orig prey predicate incl. no-passive-animals (:251-275), PlayNicely + GenericTargetSorter in findSomethingToAttack (:278-282; TF-035 field :38, ctor :47). Port: EntityMolenoid.java:126-355.)

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
- **Resolution:** FIXED (2026-08-11, Phase E4 ENT-K batch — lump row w8/1-2 #minecraft:is_overworld removed from add_cave_spawns.json (sharedEdit; rotator row untouched); replaced by three per-biome convention files: ambient_molenoid__plains.json w2/1-2 (orig OreSpawnMain.java:4741, field_76772_c=plains), ambient_molenoid__savanna.json w2/1-1 (:4742, field_150588_X), ambient_molenoid__savanna_plateau.json w2/1-1 (:4743, field_150587_Y). Spawn-rule gates already present per ENT-SYS2-004 and verified against orig Molenoid.java:303-342: spawner bypass -3..2/0..4, darkness, y>=50, night, 2x2x3 air box, no other Molenoid within 16/8/16 (port EntityMolenoid.checkSpawnRules).)

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
- **Resolution:** FIXED (2026-08-11, Phase E4 ENT-K batch — difficulty variant restored in attackWithFireball: Easy=SmallFireball, Normal=coin-flip small/Better (nextInt(2)==0), Hard=always BetterFireball+setNotMe (orig Mothra.java:390-418,:407,:415); bow 0.75f / fuse 1.0f sounds (:394,:408,:416 — invented BLAZE_SHOOT replaced), MothraPeaceful+Peaceful guards (:382-387), post-shot 1 HP heal (:419-421). Variant's second half: attack roll 1-in-3, 1-in-2 on Hard (:165,:178-180 at :229,:242) — port hardcoded 3. Same-file parity per ENT-A-003 precedent: isSuitableTarget full filter (isIgnoreable, line-of-sight, 10 species exclusions, :424-483), PlayNicely gate in findSomethingToAttack (:486-488). TF-035: GenericTargetSorter swapped in (:60,:70). BetterFireball entity/registration already present (ModEntities.java:699); no shared edits.)

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
- **Resolution:** VERIFIED-CORRECT (2026-08-11, Phase E2 — not-a-feature, proof: zero hits for riddenByEntity/field_70153_n/func_70085_c in orig Mothra.java; neither codebase has rider logic; the audited prompt's claim had no source basis. Nothing implemented, per the finding's own instruction)

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
- **Resolution:** FIXED (2026-07-02, Phase D4 — termite hunting ported: nearest living, visible Termite targeting (orig Peacock.java:202-237), flat 6.0 mob-attack damage (:166-169), 1-in-200 revenge clear / peaceful gate (:181-200); see FIX_LOG.md)

### ENT-K-048 — Peacock: egg laying missing

- **Status:** MISSING
- **Original:** ORIG `Peacock.java` — laid `PeacockEgg` every 5000 ticks
- **Port:** `entity\Peacock.java` — absent
- **Fix:** add an egg-lay timer in `aiStep` (5000 ticks → spawn ItemEntity of peacock egg item; port the item if missing).
- **Resolution:** FIXED (2026-07-02, Phase D4 — egg laying ported: clear-air / first-half-of-day / 50<=y<=100 / at-most-2-buddies-within-16 gate (orig Peacock.java:101-119, restoring the never-called findBuddies()), 1-3 eggs at ±0-1 x/z y+1 (:171-179,197-199); Crystal Apple confirmed as the breeding item (:259-261); see FIX_LOG.md)

### ENT-K-049 — Peacock: spawn gates dead code, weights changed

- **Status:** PARTIAL
- **Original:** ORIG — w1/1-3 select biomes + daytime + Y 50–100 + ≤2 nearby
- **Port:** overworld w8/1-2 + crystal dim w5/4-8; `findBuddies()` exists (`Peacock.java:111-114`) but is never called
- **Fix:** call `findBuddies()` from a `checkSpawnRules` override (daytime + Y 50–100 + ≤2 nearby); lower overworld weight to ~1/1-3.
- **Resolution:** FIXED (2026-08-11, Phase E4 ENT-K batch — gate half already ported (Peacock.checkSpawnRules per orig Peacock.java:101-119, gametest i053); remainder closed: checkSpawnRules now calls the formerly-dead findBuddies() (orig :118, :263-266); TF-035 sorter restored (orig :45,:55,:226). Weights via sharedEdits: invented overworld lump row w8/1-2 removed from add_overworld_creatures.json; NEW ambient_peacock__badlands.json w1/1-3 per orig OreSpawnMain.java:4970 (mesa) + :4971 (mesaPlateau), both collapsing to modern badlands (hostile_brutalfly.json precedent); PEACOCK MobCategory CREATURE→AMBIENT (orig EnumCreatureType.ambient; chipmunk precedent ModEntities.java:315). Crystal w5/4-8 verified correct (BiomeGenUtopianPlains.java:216-217); chaos w2/2-4 matches :385-386 but sits in the creature list — systemic chaos_biome issue, noted.)

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
- **Resolution:** FIXED (2026-08-11, Phase E4 ENT-K batch — audit's 'damage multiplier' was imprecise; actual orig mechanics restored: (1) doHurtTarget dragon branch (orig PitchBlack.java:293-303) — explosion-typed source (:295-297 setExplosionSource(null)+setExplosion → damageSources().explosion(null,null)), 1-in-8 hits the dragon HEAD part (:298-299, full damage) else BODY part (:301, vanilla quarters it), for attack×scale = the per-tier ATTACK_DAMAGE base, returns true, no knockback; (2) customServerAiStep squared-melee-reach floor of 100 vs EnderDragon/Godzilla/GodzillaHead (orig :369-377) — previously missing, so the Nightmare could never actually land hits on those bosses. TF-035 rider: plain Comparator.comparingDouble(distanceToSqr) swapped for GenericTargetSorter (orig :54 field, :67 ctor).)

### ENT-K-053 — PitchBlack: minor drop extras missing

- **Status:** PARTIAL
- **Original:** ORIG — 1 nightmare_scale, 2–7 zoo_keeper, painting, random stick/feather/arrow/flesh/carrot extras
- **Port:** `LT pitch_black.json` — 3–8 bone, 1–3 nightmare_scale, xp bottle, 1–5 zoo_keeper
- **Fix:** adjust `pitch_black.json`: scale ×1, zoo_keeper 2–7, add the random junk-extras pool. (Natural spawning: see ENT-SYS2-003.)

---
- **Resolution:** FIXED (2026-08-11, Phase E4 ENT-K batch — port inventions removed: 3-8 bone, xp bottle, scale 1-3, flat zoo_keeper 1-5. pitch_black.json now holds only the fixed drops: 1 nightmare_scale (orig PitchBlack.java:590) + 1 painting (:591, field_151160_bD → minecraft:painting per repo-wide convention, cf. kraken.json/mothra.json). Scale-dependent drops restored in dropCustomDeathLoot (TheKing/B1 exception precedent — counts read getPitchBlackScale(), inexpressible in loot JSON): 3+rand(2+(int)(5t)) rotten flesh, each with a 1-in-10 feather/string/flint/raw-beef extra (orig :574-589); 2+(int)t+rand(2+(int)(5t)) Zoo Keepers (:592-595); all scattered ±rand(5)×scale at y+1 via dropItemRand (:562-570). Audit's 'stick/feather/arrow/flesh/carrot' extras claim wrong — verified extras are feather/string/flint/beef; '2-7 zoo_keeper' is only the t=0.5..1 envelope.)

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
- **Resolution:** FIXED (2026-06-13, Phase D1 — spawn-rule gate ported in checkSpawnRules citing orig Rat.java (Crystal air-pocket + buddy gates); weights/biomes half closed in Phase C (2026-06-11, Phase C — weights/biomes done: overworld-wide entry removed, swarm_rat__dark_forest.json w35/10-20 + swarm_rat__taiga.json w25/2-8 per orig OreSpawnMain.java:4977-4978); see FIX_LOG.md)

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
- **Resolution:** FIXED (2026-08-11, Phase E4 ENT-K batch — griefing returned to Robot2). customServerAiStep now ports orig Robot2.java:274-336: 1-in-6 PlayNicely-gated think-tick, 1.25-rad body-facing gate (:292-301), swing dice with 6x destroyBlock(target) on a landed hit (:304-309), destroyNearbyBlocks() every in-range tick (:310), and the idle 1-in-450 / 50-tick just_for_fun tantrum with 1-in-3 shredding (:320-335). destroyBlock (:232-261) and destroyNearbyBlocks (:263-272) ported exactly: obsidian/bedrock/quartz/spawner/redstone-block/iron-block/chest spared, blocks set to air with NO drops, mobGriefing-gated, (int)-truncation quirk kept. PlayNicely gate added to findSomethingToAttack (:382-384). TF-035 rider: GenericTargetSorter swapped in (orig :38,50). Robot4's invented relocation removed under ENT-K-069.

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
- **Resolution:** FIXED (2026-08-11, Phase E4 ENT-K batch — shot sound restored). fireLaserAt (port Robot3.java:137-152) now plays SoundEvents.FIREWORK_ROCKET_LAUNCH at 3.0f volume / 1.0f pitch on every shot, matching orig Robot3.java:273 world.playSoundAtEntity(this, "fireworks.launch", 3.0f, 1.0f), via the codebase's level().playSound(null, x, y, z, ..., SoundSource.HOSTILE, ...) idiom (cf. Dragon.java:507-509). TF-035 rider: plain distance comparator swapped for GenericTargetSorter (orig Robot3.java:39 field, :51 ctor).

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
- **Resolution:** FIXED (2026-08-11, Phase E4 ENT-K batch — hybrid ranged restored, invented griefing removed). customServerAiStep now ports orig Robot4.java:269-334: melee only inside (3+w/2)^2 with no reload cost (:294-296); otherwise a 0.5-rad HEAD-facing gate (:298-305) and fireLaserAt (:305-324): ball spawns 1.75 out at yRot+45deg, +2.0y (:306-309), 0.2x horizontal-distance arc boost (:313), shoot 2.0f/4.0f (:314); distSq>65 sets special + reload 30 + launch 3.5f/0.5f (:315-318), else reload 10 + 2.5f/1.0f (:319-322); setAttacking(1) :325, moveTo 0.75 :327. Deleted poundGroundInFront/isShatterable/pound fields (orig Robot4 breaks no blocks); restored orig aiStep client particles (:133-143 smoke y-jitter + attacking reddust), hurt ret=true (:350), PlayNicely gate (:386-388). TF-035: sorter swapped (orig :41,54).

### ENT-K-070 — Robot4: shielding is dead state

- **Status:** PARTIAL
- **Original:** ORIG `Robot4.java` — active shielding window after being hit
- **Port:** `entity\Robot4.java:252` — `DATA_SHIELDING` defined + checked in `hurt()` but no code ever calls `setShielding(1)`
- **Fix:** call `setShielding(1)` when hurt (tie to the existing 65-tick `wasAttackedTicker`) and clear when it expires.
- **Resolution:** VERIFIED-CORRECT (2026-08-11, Phase E4 ENT-K batch — audit premise wrong; dead state IS the original). The ONLY setShielding(1) caller in the entire original mod is the client model: ModelRobot4.java:447-451 "if ((double)newangle > (double)amp / 3.0) { e.setShielding(1); }" during the attack arm-pump. A client-side DataWatcher write never syncs to the server, so orig Robot4.java:339's shielding check always read 0 server-side — the real post-hit immunity is was_attacked_ticker=65 (:342). Port matches bug-for-bug: port ModelRobot4 setupAnim performs the identical client-local write, and Robot4.hurt gates "if (this.getShielding() != 0 || this.wasAttackedTicker != 0) return false;". The proposed setShielding(1)-on-hurt would invent a shield the original never had. Documented in Robot4.hurt javadoc and ModelRobot4 comment.

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
- **Resolution:** FIXED (2026-08-11, Phase E4 ENT-K batch — shot sound restored). fireLaserAt (port Robot5.java:127-142) now plays SoundEvents.FIREWORK_ROCKET_LAUNCH at 3.0f volume / 1.0f pitch on every shot, matching orig Robot5.java:245 world.playSoundAtEntity(this, "fireworks.launch", 3.0f, 1.0f), same level().playSound HOSTILE idiom as the ENT-K-067 Robot3 fix. TF-035 rider: plain distance comparator swapped for GenericTargetSorter (orig Robot5.java:39 field, :50 ctor).

## RockBase

### ENT-K-075 — RockBase: Crystal-dimension type lottery missing

- **Status:** PARTIAL
- **Original:** ORIG `RockBase.java:129-140` — Crystal-dimension branch forces types 9–12
- **Port:** `entity\RockBase.java:95-106` — single overworld lottery (1→12) only
- **Fix:** in the type-roll, branch on Crystal dimension and constrain types to 9–12.
- **Resolution:** FIXED (2026-08-11, Phase E4 ENT-K batch — tick() type lottery now branches on the Crystal dimension per orig RockBase.java:93-140: the non-Crystal path keeps the 1-12 rising-rarity rolls (orig :95-128), while inside DimensionID5 the mob is forced to crystal types — base 9, nextInt(3)==0 -> 10, nextInt(5)==0 -> 11, nextInt(10)==0 -> 12 (orig :129-140). Dimension test uses the established ModDimensionKeys.isIn(level, CRYSTAL) idiom (DimensionID5 -> orespawn:crystal per ModDimensionKeys javadoc). HP re-derivation 1+type/4 unchanged (orig :141-142). Port: entity/RockBase.java tick().)

### ENT-K-076 — RockBase: death drops missing + placed rocks lose type

- **Status:** MISSING
- **Original:** ORIG `RockBase.java:213-251` — `func_70645_a` drops the matching rock item (MySmallRock…MyCrystalTNTRock) per type on death
- **Port:** no death drop (no override, no `LT rock_base.json`); also `item\ItemRock.java:42-54` `useOn` never calls `placeRock(rockType)`, so a placed rock re-randomizes
- **Fix:** override `die()` to drop the item matching `getRockType()`; in `ItemRock.useOn`, call `placeRock(rockType)` after spawning. High priority — rock pickup loop is broken.
- **Resolution:** FIXED (2026-07-02, Phase D4 — RockBase death drop restored (one rock item matching the mob's type) and the type indexing realigned to the original 1-based 1-12 scheme across ItemRock/EntityThrownRock/RockBase, fixing a 0-vs-1-based mismatch that made placed rocks lose their type; see FIX_LOG.md)

### ENT-K-077 — RockBase: Y≥50 spawn rule missing

- **Status:** PARTIAL
- **Original:** ORIG `RockBase.java` `func_70601_bi` — Y≥50
- **Port:** no `checkSpawnRules` (MISC category, placed via ItemRock/`world\CrystalStructures.java`)
- **Fix:** add Y≥50 check in `checkSpawnRules` (low priority — placement is mostly structural).

---
- **Resolution:** VERIFIED-CORRECT (2026-08-11, Phase E4 ENT-K batch — the stated remainder is already implemented: port entity/RockBase.java overrides checkSpawnRules(LevelAccessor, MobSpawnType) with `return this.getY() >= 50.0;`, commented "orig RockBase.java:191-193 — y>=50". Orig func_70601_bi is `return !(this.field_70163_u < 50.0);` (orig RockBase.java:191-193), a full override with no super call and no other conditions — the port matches exactly, including replacing rather than AND-ing the vanilla check. Evidently added alongside the D4 ENT-K-076 RockBase work (same comment style as the die() override); the ledger entry was never updated. No code change made.)

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
- **Resolution:** FIXED (2026-08-11, Phase E4 ENT-K batch — isSuitableTarget rebuilt to the full orig predicate (orig Rotator.java:294-365): null/self/dead, MyUtils.isIgnoreable (:304), line of sight (:307), creative players (:310-315, aligned invulnerable->instabuild), then all sixteen spared species — Termite :316, Vortex :319, DungeonBeast :325, Peacock :328, CrystalCow :331, Irukandji :334, Skate :337, Whale :340, Flounder :343, Urchin :346, TerribleTerror :349, LurkingTerror :352, CloudShark :355, Mothra :358, Bee :361, Mantis :364 — plus Rotator itself :322; every species exists in the port. findSomethingToAttack gains the PlayNicely gate (orig :368-370) and swaps the plain distance comparator for GenericTargetSorter (TF-035 rider; orig field :49, ctor :60, sort :372). Port: entity/EntityRotator.java:209-255.)

### ENT-K-080 — Rotator: `was_spawnered` persistence missing

- **Status:** MISSING
- **Original:** ORIG `Rotator.java:255-273` — persisted when spawned from a "Rotator" spawner
- **Port:** no equivalent
- **Fix:** add a `wasSpawnered` boolean (NBT-saved) set when `MobSpawnType == SPAWNER`, and make it force `setPersistenceRequired()`.
- **Resolution:** FIXED (2026-07-02, Phase D4 — verified already implemented in slice D1's spawn-architecture work: `wasSpawnered` is set during checkSpawnRules, persisted to NBT, and consumed by the despawn exemption; the ledger entry was never updated, no D4 code change needed; see FIX_LOG.md)

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
- **Resolution:** FIXED (2026-08-11, Phase E4 ENT-K batch — squid prey restored: AttackSquid (orig RubberDucky.java:440-442) and vanilla Squid (:443-445) are suitable targets unconditionally — orig hunts squids at any kill count; the ledger's killCount>=5 framing applied only to players (:449-452, creative aligned to instabuild). Buddy-follow restored: transient buddy field (orig :51), 1-in-10 adoption while scanning a fellow ducky (:446-448), cleared each rescan (:470), 1-in-15 follow at speed 1.0 when no target (:404-407) and independent 1-in-20 follow (:410-412). findSomethingToAttack also gains the PlayNicely gate (:457-459), GenericTargetSorter (TF-035 rider; field :49, ctor :69, sort :461), and current-target retention via getTarget/setTarget (:465-469) replacing the getLastHurtByMob approximation. Port: entity/EntityRubberDucky.java.)

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
- **Resolution:** FIXED (2026-07-02, Phase D4 — `rubber_ducky.json` loot added per the orig RubberDucky drop table; see FIX_LOG.md)

### ENT-K-085 — RubberDucky: never spawns naturally

- **Status:** MISSING
- **Original:** `OreSpawnMain.java:4873-4874` — water biomes w4–10 / groups 4–20 + "Rubber Ducky" spawner gate + daytime + Y≥50 (`RubberDucky.java:508-526`)
- **Port:** no biome modifier entry
- **Fix:** create a BM JSON for water biomes, weight 4–10 / group 4–20, plus `checkSpawnRules` daytime + Y≥50. (Also listed in ENT-SYS2-003.)
- **Resolution:** FIXED (2026-06-13, Phase D1 — MobCategory CREATURE→WATER_CREATURE, IN_WATER placement, river/deep-ocean BM JSONs, gate spawner-bypass/y>=50/day per orig RubberDucky.java:508-526; see FIX_LOG.md)

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
- **Resolution:** FIXED (2026-08-11, Phase E4 ENT-S batch — port acquired targets via HurtBy + NearestAttackableTarget(Player) only. Restored the orig acquisition brain: customServerAiStep nextInt(6)==0 rescan (orig Scorpion.java:175-192) calling findSomethingToAttack — PlayNicely gate (:256-258), 8/3/8 scan box (:259), GenericTargetSorter sort (:44 field, :55 ctor, :260 sort; TF-035 rider satisfied) — and the full isSuitableTarget ladder (:203-253): VelocityRaptor/Spider(+CaveSpider)/Creeper prey, Ghost/GhostSkelly/scorpions/other Monsters excluded, creative players excluded, and the orig fallthrough-true (:252) that preys on ALL other livings (animals, villagers, survival players). Invented NAT(Player) goal removed (follow-range-24 plain-distance acquisition; orig only engages the 8-block sorted scan). HurtByTargetGoal kept (orig :62). Port: entity/EntityScorpion.java registerGoals, customServerAiStep, findSomethingToAttack, isSuitableTarget.)

### ENT-S-003 — Scorpion: attack sound + cactus immunity missing

- **Status:** PARTIAL
- **Original:** ORIG `Scorpion.java:182-201` — 1/3 chance `orespawn:scorpion_attack` on melee; cactus-immune
- **Port:** `entity\EntityScorpion.java` — neither present
- **Fix:** play `scorpion_attack` 1/3 in `doHurtTarget`; add cactus to `isInvulnerableTo`.
- **Resolution:** FIXED (2026-08-11, Phase E4 ENT-S batch — both halves were absent. (1) doHurtTarget override now rolls nextInt(3)==1 after every swing (hit or miss — orig ignores the func_70652_k return) and plays orespawn:scorpion_attack AT THE TARGET's position, vol 0.75 / pitch 1.5 (orig Scorpion.java:180-184; sounds.json already carries scorpion_attack at :944-946; createVariableRangeEvent idiom per Mothra.java:172-173, play-at-target idiom per Crab.java:268-272). (2) hurt() discards cactus damage before super is consulted (orig :195-201 returns false without calling super), via source.is(DamageTypes.CACTUS) — the EntitySpitBug.java:153-155 precedent for the same orig "cactus" string filter. Port: entity/EntityScorpion.java doHurtTarget, hurt.)

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
- **Resolution:** FIXED (2026-08-11, Phase E4 ENT-S batch — port lumped w15(2-4) over desert + #is_badlands + #is_savanna. Replaced with seven per-row files per the hostile_leaf_monster__* convention, one per orig addSpawn row: desert 15(3-6) OreSpawnMain.java:4901; dark_forest 28(2-4) :4902 roofedForest; savanna 15(3-5) :4903; savanna_plateau 15(2-4) :4904; badlands 6(1-3) :4905 mesa; wooded_badlands 4(1-3) :4906 mesaPlateau_F; eroded_badlands 5(3-6) :4907 mesaPlateau (eroded_badlands is the sole remaining 1.21 badlands member — mesaPlateau's nearest home, keeping family coverage with orig per-row weights). Tag lumps also over-covered windswept_savanna, absent from orig. Spawner/darkness/y<50 gates already live in EntityScorpion.checkSpawnRules citing orig Scorpion.java:281-299 (closed under ENT-SYS2-004). All changes returned as sharedEdits — biome_modifier JSONs are not editable in this lane.)

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
- **Resolution:** FIXED (2026-06-13, Phase D1 — spawn-rule gate ported in checkSpawnRules citing orig SeaMonster.java:544-570; weights/biomes half closed in Phase C (2026-06-11, Phase C — weights/biomes JSON half fixed); see FIX_LOG.md)

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
- **Resolution:** VERIFIED-CORRECT (2026-08-11, Phase E4 ENT-S batch — remainder already in port). Port Shoes.java:73-87 reproduces orig Shoes.java:57-78 damage table in exact order: base 2.0 (:59), id-6 heavy 6.0 (:60-62), Creeper +4 (:63-65), Girlfriend=1 (:66-68, port :79), Boyfriend=1 (:69-71, port :80), Player=0 (:72-74, port :81), valentines=10 last so it overrides all (:75-77, port :84 'if (SeasonalDates.isValentines()) damage = 10.0f;'). Girlfriend/Boyfriend are ported (same package). SeasonalDates.isValentines() = Feb 14, orig OreSpawnMain.java:4567-4569; real-time evaluation is the audit's accepted ANIM-016 deviation. Damage source thrown(this, owner) matches func_76356_a. No edits needed.

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
- **Resolution:** FIXED (2026-06-13, Phase D1 — spawn-rule gate ported in checkSpawnRules citing orig Skate.java:318-329; weights/biomes half closed in Phase C (2026-06-11, Phase C — weights/biomes JSON half fixed); see FIX_LOG.md)

---

## Slice (item)

### ENT-S-016 — Slice: enchantment identity unverified

- **Status:** UNVERIFIED
- **Original:** ORIG `Slice.java:33-43` — applies `Enchantment.field_77338_j` lvl 5 + `field_77336_l` lvl 1 (obfuscated ids)
- **Port:** port `Slice.java:23-28` — Sharpness 5 + Bane of Arthropods 1 in `inventoryTick`; levels (5/1) match
- **Fix:** verification failed because the 1.7.10 obfuscated enchantment fields were not mapped. Resolve by consulting MCP 1.7.10 mappings for `field_77338_j`/`field_77336_l`; if they map to something other than Sharpness/Bane, swap the port enchantments accordingly.

---
- **Resolution:** VERIFIED-CORRECT (2026-08-11, Phase E2 — the audit read DEAD CODE: `Slice.java` the class is never instantiated (zero `new Slice(` hits in the orig tree); the shipped Slice item is `MySlice = new Bertha(BaseItemID+314, toolBERTHA)` (OreSpawnMain.java:1646), so the port's ITEM-032 Bertha-clone treatment (KB5/Bane1/FireAspect1, tier BERTHA) is the faithful one. Enchantment field mapping proven as a byproduct, anchored by ITEM-031's verified Bertha bake: field_77337_m=Knockback, field_77336_l=Bane, field_77334_n=FireAspect, field_77347_r=Unbreaking, and the dead class's field_77338_j=Sharpness — the j..o suffixes run ids 16-21 consecutively)

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
- **Note:** the "procedural sine walk instead of IK" clause was closed in Phase D2 (full leg solver ported, orig SpiderRobot.java:111-486 — see ANIM-006); the flame attack + stomp-range remainder stays open (Phase E owner unchanged).
- **Resolution:** FIXED (2026-08-11, Phase E4 ENT-S batch — stomp + frontal attack restored). Stomp ported: 1/40 tick while ridden (orig SpiderRobot.java:590-592), PlayNicely-gated 20x8x20 scan hitting EVERY target in the 12-18 block ring (orig :896-952) at attack/10 damage with 0.6/0.1 knockback (orig :954-969). Frontal melee now uses the orig filter (orig :988-1038): Spider/SpiderDriver/CaveSpider exclusions, isIgnoreable (:1013), line-of-sight (:1016), 0.75-rad cone (:1030) with <6-block bypass that skips even the creative check (:1027, quirk kept). PEACEFUL gates restored (orig :590,:593). Exhaust particles get orig outward velocities and the dropped 1/10 fireworksSpark (orig :605-616). TF-035: sorter swapped to GenericTargetSorter (orig :50,:60), unused as the orig never sorts (:975-985). Port SpiderRobot.java:129-195,246-351.

### ENT-S-022 — SpiderRobot: boss bar added (not in original)

- **Status:** PARTIAL
- **Original:** ORIG — no boss bar (HUD overlay only)
- **Port:** port `SpiderRobot.java:49-50,86-99` — `ServerBossEvent` added alongside ported HUD
- **Fix:** decide: remove the boss bar for fidelity, or keep and document as intentional addition (a rideable vehicle with a boss bar is misleading).
- **Resolution:** FIXED (2026-08-11, Phase E4 ENT-S batch — invented boss bar REMOVED). Decision: removal per parity doctrine; a rideable vehicle with a boss bar is a confirmed invention. Orig SpiderRobot.java contains no BossStatus/boss-bar code anywhere — its status renders via the dedicated RenderSpiderRobotInfo HUD overlay (orig SpiderRobot.java:52), which the port keeps. Deleted the ServerBossEvent field (was port :50-51), the startSeenByPlayer/stopSeenByPlayer overrides (was :91-101), the setProgress call in customServerAiStep (was :105), and the now-unused ServerBossEvent/ServerPlayer/BossEvent/Component imports; parity comment left at the old field site (port SpiderRobot.java:51-55).

### ENT-S-023 — SpiderRobot: drops changed

- **Status:** DIVERGENT
- **Original:** ORIG `func_70628_a` — various blocks/items
- **Port:** `LT spider_robot.json` — iron 3–8 + string 2–5
- **Fix:** read ORIG `SpiderRobot.java` drop method and port the block/item list into `spider_robot.json`.
- **Resolution:** FIXED (2026-06-11, Phase C — see FIX_LOG.md and phase_c_reports/C4_entities_S_Z.md)

### ENT-S-088 — SpiderRobot: hitbox shrunk (audit gap — size was never examined)

- **Status:** DIVERGENT
- **Original:** ORIG `SpiderRobot.java:58` — `setSize(3.25f, 2.25f)` in the ctor; the ONLY size call in the class (no ridden/child/state resize anywhere), and 1.12.2 has no SpiderRobot entity (texture only), so 1.7.10 is the sole baseline
- **Port:** `ModEntities.java` — `.sized(2.0f, 1.5f)`, uncited. The ENT-S-020 stats row never listed size, so the audit never ruled on it — the same port-shrunk-box pattern it caught and fixed on Alien (ENT-A-002), Cephadrome (ENT-A-083) and PrinceTeen (BOSS-026). The S4 profile then codified 2.0×1.5 as "classic dims" without a parity check. Caught by the S7a reviewer as an out-of-scope escalation (FIX_LOG S7a).
- **Fix:** restore `.sized(3.25f, 2.25f)` with the orig citation; move the MHLib profile main size to [3.25, 2.25] in lockstep (Size-hook law — profile main size must equal the classic EntityType dims exactly); pin both modes' dims in `s4_part_counts_and_classic_zero` (the guard gap the drift exploited — the ant had s5b/i083 pins, the spider had none); rewrite the KNOWN_ISSUES mount-spot paragraph.
- **Note:** cross-check prompted by this gap — **AntRobot verified clean**: orig `AntRobot.java:52` `setSize(2.75f, 1.25f)` == port registration (cited in `ModEntities.java`) == profile `ant_robot.json` == i083 + s5b pins. Both robots' dims are now positively parity-cited, not assumed. Derived ranges self-correct with the restored width (they read live `getBbWidth()`): SpiderDriver mount-seek `(4+w/2)²` 5.0→5.625 and drive range `11+w/2` 12.0→12.625, both now the values the original computed from its 3.25 width; default eye height 0.85·h 1.275→1.9125 likewise matches the 1.7.10 default.
- **Resolution:** FIXED (2026-08-13, S7c — design ruling "original wins, per law"; see FIX_LOG.md S7c)

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
- **Resolution:** FIXED (2026-07-02, Phase D4 — SpitBug cactus + fall immunity ported from the orig damage-source filter; see FIX_LOG.md)

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
- **Resolution:** FIXED (2026-08-11, Phase E4 ENT-S batch — interaction ladder rebuilt to orig Spyro.java:213-335 order: dead-bush untame (:250-265; heal-to-200, owner cleared, byte 6), ICE-block extinguisher with orig chat line (:266-280) replacing the invented water-bucket branch, diamond→tamed-Dragon replacement (:281-300 via spawnCreature :715-724 — owner copied, custom name deliberately NOT copied; the audit's 'copy name' is absent from orig), flint&steel lighting given the :301-315 dist<16 gate/byte-6/chat line, name-tag rename (:316-325); natural 1-in-100000 growth into Dragon (:453-466) added to tick(). TF-035 rider: GenericTargetSorter restored (orig :55,:82,:702). Port entity/EntitySpyro.java:148-168,203-312,442. Orig identity confirmed: Spyro.java — ThePrince is an unrelated multi-part boss per INDEX.md.)

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
- **Resolution:** FIXED (2026-06-13, Phase D1 — spawn-rule gate ported in checkSpawnRules citing orig Spyro.java:407-412; weights/biomes half closed in Phase C (2026-06-11, Phase C — weights/biomes JSON half fixed); see FIX_LOG.md)

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
- **Resolution:** FIXED (2026-08-11, Phase E4 ENT-S batch — via sharedEdits: biome_modifier JSONs protected). Deleted the three lumped swarm_stink_bug__* modifiers (invented flat w8 plus invented biomes birch/flower/dark forest, sunflower_plains and is_jungle/is_taiga tags) and split into five per-biome files matching orig OreSpawnMain.java:4894-4898: creature_stink_bug__forest w10 (2-4) :4894; __jungle w8 (2-4) :4895; __windswept_forest w6 (2-4) :4896 taigaHills; __sparse_jungle w4 (2-4) :4897 jungleHills; __savanna w8 (2-5) :4898. Hills mappings follow the hostile_leaf_monster__* precedent (taigaHills→windswept_forest :4913, jungleHills→sparse_jungle :4912); creature_ prefix per port MobCategory.CREATURE registration. Chaos-dim w3 (2-4) row (BiomeGenUtopianPlains.java:389) verified already present in chaos_biome.json creature list.

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
- **Resolution:** FIXED (2026-07-02, Phase D4 — `stinky.json` tamed-only beef drop added (orig Stinky.java:257-266) via the established OreSpawnTamed NBT-flag convention; see FIX_LOG.md)

### ENT-S-035 — Stinky: Nether spawn dropped

- **Status:** DIVERGENT
- **Original:** `OreSpawnMain.java:4805-4808` — hell monster w2; mesa-variant ambient w1 ×3; island dim w2
- **Port:** `BM companion_stinky` forest/taiga w1 + dim_islands w2
- **Fix:** add a Nether BM entry w2 and mesa/badlands entries w1.
- **Resolution:** FIXED (2026-06-13, Phase D1 — spawn-rule gate ported in checkSpawnRules citing orig Stinky.java:286-291; weights/biomes half closed in Phase C (2026-06-11, Phase C — weights/biomes JSON half fixed); see FIX_LOG.md)

---

## SunspotUrchin (projectile)

### ENT-S-036 — SunspotUrchin: fire placement on block impact missing

- **Status:** MISSING
- **Original:** ORIG — places `Blocks.fire` at impact point
- **Port:** port `:48-58` — smoke particles + discard only
- **Fix:** in `onHitBlock`, place `Blocks.FIRE` at the hit face position (mobGriefing-gated).

---

## Termite
- **Resolution:** FIXED (2026-07-02, Phase D4 — verified already restored in Phase C6 as part of ITEM-053's projectile pass ("urchin fire restored"); the ledger entry was simply never updated, no D4 code change needed; see FIX_LOG.md)

### ENT-S-037 — Termite: spawn pathway unverified

- **Status:** UNVERIFIED
- **Original:** ORIG — no `addSpawn`; nest/structure-driven spawning
- **Port:** no biome modifier; config toggle `ModSpawnControl.java:59/68`; cluster gate ≤4 in 20×10×20, y≥50 (`:218-223`)
- **Fix:** verification failed because neither side's structure/nest spawn data was checked in the audit slice. Resolve by reading the port's structure/feature code (e.g. termite-nest worldgen or block tick spawners) and ORIG nest block classes; confirm termites still appear in-world, else wire a nest spawn.

---
- **Resolution:** VERIFIED-CORRECT (2026-08-11, Phase E2 — pathway parity proven: orig has NO Termite addSpawn (both `Termite.class` refs in OreSpawnMain.java are the entity registrations); spawning is block-driven, and the port wires the same pathway: CrystalAntBlock.java:64 (nest blocks emit termites), OreBasicStone.java:115 (termite troll stone erupts on break, ITEM-001/005), with worldgen placement via add_anthills.json + add_troll_blocks.json. Termites appear in-world through blocks exactly as in 1.7.10)

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
- **Resolution:** FIXED (2026-06-13, Phase D1 — spawn-rule gate ported in checkSpawnRules citing orig TerribleTerror.java:193-214; weights/biomes half closed in Phase C (2026-06-11, Phase C — weights/biomes JSON half fixed); see FIX_LOG.md)

---

## ThunderBolt (projectile)

### ENT-S-041 — ThunderBolt: royalty exemption missing

- **Status:** PARTIAL
- **Original:** ORIG — 40 dmg, ignores "royalty" entities (King/Queen family)
- **Port:** port `:19,46-59` — 40 total (2×20 split), ignite 1 s; no royalty exemption (orig explosion power unverified)
- **Fix:** skip damage when the hit entity implements the port's royalty marker (TheKing/TheQueen/Princes/Princess) so boss self-fire doesn't hurt peers.

---
- **Resolution:** FIXED (2026-08-11, Phase E4 ENT-S batch — royalty exemption ported with full skip semantics). Orig ThunderBolt.java:36-39: isRoyalty target -> setDead()+return BEFORE damage, particles, explode sound, 3.0 explosion, and lightning — the bolt vanishes silently. Port now bails in onHit() before super.onHit() (ThunderBolt.java:62-74): 'if (result instanceof EntityHitResult entityHit && MyUtils.isRoyalty(entityHit.getEntity())) { this.discard(); return; }', so onHitEntity and the entire explosion/lightning path never run. Port MyUtils.isRoyalty (util/MyUtils.java:9-19) matches orig MyUtils.java:46-75 roster (ThePrince/Teen/Adult, ThePrincess, TheKing, KingHead, TheQueen, QueenHead, PurplePower). Import danger.orespawn.util.MyUtils added (BetterFireball.java:3 idiom).

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
- **Resolution:** FIXED (2026-06-13, Phase D1 — spawn-rule gate ported in checkSpawnRules citing orig TRex.java:276-315; weights/biomes half closed in Phase C (2026-06-11, Phase C — weights/biomes JSON half fixed); see FIX_LOG.md)

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
- **Resolution:** FIXED (2026-07-02, Phase D4 — Triffid cactus + fall immunity ported from the orig damage-source filter; see FIX_LOG.md)

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
- **Resolution:** FIXED (2026-08-11, Phase E2 — original spawn source located: there is NONE. No addSpawn/SpawnListEntry for Triffid anywhere in OreSpawnMain.java or the BiomeGen* classes; all `Triffid` refs are entity/egg/cage registrations. The port's `add_overworld_monsters.json` w4 (1-2) row was an invented addition — removed per the standing invention ruling. Spawn egg / spawn block / cage remain, matching the orig's only pathways. See FIX_LOG Phase E)

### ENT-S-050 — Triffid: shell-lockout duration unverified

- **Status:** UNVERIFIED
- **Original:** ORIG — DataWatcher 21 OpenClosed; no-damage-while-closed; original lockout timer not read
- **Port:** port `:139-182` — 300-tick hurt lockout while closed, open rolls 1/80→1/8
- **Fix:** verification failed because the ORIG timer constant wasn't extracted. Resolve by reading ORIG `Triffid.java` OpenClosed timer logic and matching the port's 300-tick value to it.

---
- **Resolution:** VERIFIED-CORRECT (2026-08-11, Phase E2 — timer extracted: orig Triffid.java:224/:229 set `hurt_timer = 300` (blocked hits RE-ARM it :223-224; successful hits re-arm and close :229-230); open rolls nextInt(80)==2 then nextInt(8)==1 (:248-252). Port EntityTriffid matches all four: HURT_LOCKOUT_TICKS=300 (:35), blocked-hit re-arm (:151-155), post-hurt re-arm+close (:164), identical rolls (:201-202))

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
- **Resolution:** FIXED (2026-08-11, Phase E4 ENT-S batch — SpitBug summon ported into EntityTrooperBug.customServerAiStep: inside the orig 1-in-5 AI-tick gate (orig TrooperBug.java:413), a live target plus a further 1-in-30 roll (:441) summons ONE Spit Bug at the bug↔target midpoint with ±0-4 x/z scatter and +1.01 y (:442), via the spawnCreature idiom (:453-462 — moveTo with random yaw, addFreshEntity, playAmbientSound; same 1.21 pattern as EntityEmperorScorpion.java:174). Net cadence ≈1/150 per engaged server tick, matching orig; the look/leap/attack halves of that original block live in TrooperBugLeapAttackGoal. Audit's '1–2 minions on attack start' guess corrected against orig: exactly one, on an independent per-AI-tick roll, not attack-triggered.)

### ENT-S-053 — TrooperBug: cactus/fall immunity missing

- **Status:** PARTIAL
- **Original:** ORIG — cactus- and fall-immune
- **Port:** 20-tick i-frames only (`:139-155`)
- **Fix:** add CACTUS to `isInvulnerableTo`; override `causeFallDamage` → false.
- **Resolution:** FIXED (2026-08-11, Phase E4 ENT-S batch — cactus/fall filter added to EntityTrooperBug.hurt() after the hurt-timer lockout, mirroring orig TrooperBug.java:385-402 exactly: :390 filters damage-type names "cactus"/"fall" before super.attackEntityFrom, so they deal nothing and never arm the 20-tick timer nor trigger the retarget-on-hurt. Same reviewed idiom as sibling EntitySpitBug.java:151-156 (orig SpitBug.java:244): DamageTypeTags.IS_FALL + DamageTypes.CACTUS. Audit's suggested isInvulnerableTo/causeFallDamage mechanism deliberately not used — the in-hurt filter preserves the orig check order (timer first, then name filter). Gametest ent_s_053_trooperbug_cactus_fall_immunity added to EntityLogicTestsB.java asserting no cactus/fall damage, no timer arm (generic damage lands immediately after a cactus tick), and no blanket immunity.)

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
- **Resolution:** VERIFIED-CORRECT (2026-08-11, Phase E4 ENT-S batch — port EntityTshirt.java:61-68 already carries the full orig gate set from Tshirt.java:93-103: "if (!OriginalSpawnGates.isDaytime(level)) return false; if (this.getY() < 50.0) return false; return !OriginalSpawnGates.anyOtherNearby(this, level, EntityTshirt.class, 20.0, 8.0, 20.0);" — matching orig :94 !func_72935_r() reject, :97-99 y<50 reject, :100-102 findNearestEntityWithinAABB(Tshirt.class, box±20/8/20, this) must return null. Audit description corrected: func_72935_r is isDaytime, so the orig gate is DAY-only, not 'night-only'; the port matches the original. Village-dim row w2 (1-1) (BiomeGenUtopianPlains.java:324) verified present in village_biome.json creature spawner list.)

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
- **Resolution:** FIXED (2026-06-13, Phase D3 — audit description corrected against orig UltimateArrow.java: there is no ignite/knockback/trail; the real custom behaviors are (a) the UltimateSwordPvp-gated heal-instead-of-damage for players/Girlfriend/Boyfriend/tamed pets (+1 HP, arrow-hit sound, arrow consumed) and (b) passthrough of Elevators and ridden Cephadrome/Dragon/horses — both ported via `canHitEntity`/`onHitEntity`; see FIX_LOG.md)

---

## UltimateFishHook

### ENT-S-059 — UltimateFishHook: custom fish pools + lava fishing missing

- **Status:** MISSING
- **Original:** ORIG `UltimateFishHook.java:422-449` — weighted pools incl. `orespawn_fish` (BlueFish, PinkFish, RockFish, WoodFish, GreyFish) and `orespawn_lava_fish` (SunspotUrchin, LavaEel, SunFish, SparkFish, FireFish) when fishing in lava (`:431-434`)
- **Port:** port `UltimateFishHook.java:9-17` — vanilla `FishingHook` subclass, vanilla loot only; no lava support
- **Fix:** override the retrieve/loot logic: use a custom loot table including the five orespawn fish; detect lava at hook position and switch to the lava-fish table; make the hook lava-proof (`fireImmune`).
- **Resolution:** FIXED (2026-07-02, Phase D4 — rebuilt on the vanilla FishingHook using access transformers for nibble/currentState/catchingFish/shouldStopFishing: the orig weighted junk/treasure/vanilla-fish/OreSpawn-water-fish/lava-fish pools ported into getCatch with Luck-of-the-Sea/Lure scaling, lava fishing (buoyancy + bite state machine + lava-appropriate particles), fire-immune hook spawning EntityLavaLovingItem for lava catches, XP orb on retrieve, random durability damage + level-30 enchant on caught gear; the invented +3 luck/+2 lure-speed constructor bonuses were removed and the renderer switched to the vanilla FishingHookRenderer; see FIX_LOG.md)

### ENT-S-060 — UltimateFishHook: custom wait timers and reel-pull missing

- **Status:** PARTIAL
- **Original:** ORIG `:384-420` — custom wait timers, reel-from-distance pull
- **Port:** luck+3 / lure+2 only (`:14-16`; rod self-enchants, `UltimateFishingRod.java:24-29`)
- **Fix:** port the shortened wait timers and the long-distance reel pull into the hook subclass.

---
- **Resolution:** FIXED (2026-08-11, Phase E4 ENT-S batch — both halves ported into port UltimateFishHook.java. Timers: catchingFish override re-rolls each freshly set counter with the original's ranges — wait 50-300 minus Lure*20*5 read live off the held rod (orig :340-341; vanilla 100-600, FishingHook.java:384-385; Lure III can never go positive, bug kept), approach 100-200 (orig :337; vanilla 20-80, :381), bite window 10-30 (orig :300; vanilla 20-40, :353). Reel pull: pullEntity override restores the sqrt(distance)*0.08 vertical kick (orig :389-397) dropped by vanilla 1.21.1 (:519-525). TF-028 lava tick untouched; override applies in lava via the existing catchingFish call, matching orig's single state machine. Ledger cite correction: timers live at orig :286-342, not :384-420. Gametests ent_s060_bite_timer_rerolls / ent_s060_reel_pull_sqrt_kick added. Requires AT sharedEdit below.)

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
- **Resolution:** FIXED (2026-06-13, Phase D1 — spawn-rule gate ported in checkSpawnRules citing orig Urchin.java:298-332 (was_spawnered side effect included); weights/biomes half closed in Phase C (2026-06-11, Phase C — weights/biomes JSON half fixed); see FIX_LOG.md)

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
- **Resolution:** FIXED (2026-08-11, Phase E4 ENT-S batch — dead-bush untame ported per orig VelocityRaptor.java:264-281: untame first so health resets to the WILD mygetMaxHealth()=10 (:212,:266-267), ENT-S-063's tamed MAX_HEALTH 20 rolled back to base 10, owner cleared, byte 6, bush consumed; name-tag rename ported per :282-291 (unnamed tag renames to 'Name Tag' — orig quirk; named tags hit vanilla's item-first handler, mirroring 1.7.10's item-before-entity order). Orig client-side player walk-speed reset (:271-273) omitted — belongs to the unported rider-speed hack, flagged in notes. Port entity/VelocityRaptor.java:170-202. Sorter N/A: orig VelocityRaptor has no GenericTargetSorter (fields :40-44).)

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
- **Resolution:** FIXED (2026-06-13, Phase D1 — spawn-rule gate ported in checkSpawnRules citing orig VelocityRaptor.java:78-83; weights/biomes half closed in Phase C (2026-06-11, Phase C — weights/biomes JSON half fixed); see FIX_LOG.md)

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
- **Resolution:** FIXED (2026-06-13, Phase D1 — spawn-rule gate ported in checkSpawnRules citing orig Vortex.java:240-284 (was_spawnered side effect included); weights/biomes half closed in Phase C (2026-06-11, Phase C — weights/biomes JSON half fixed); see FIX_LOG.md)

---

## WaterBall (projectile)

### ENT-S-072 — WaterBall: target exemptions + item drop missing

- **Status:** PARTIAL
- **Original:** ORIG — 0 dmg vs WaterDragon/AttackSquid; drops MyWaterBall item on hit
- **Port:** port `:44-52` — mounted-player skip ported; WaterDragon/AttackSquid exemption and item drop missing (fire-extinguish added)
- **Fix:** early-return 0 damage for WaterDragon/AttackSquid targets; spawn the water_ball item entity on hit.

---
- **Resolution:** FIXED (2026-08-11, Phase E4 ENT-S batch — target exemptions ported; drop verified present). Orig WaterBall.java:47-61 exemptions return from func_70184_a before damage, particles, splash sound, and setDead — the ball flies through. Port onHit() now bails before super.onHit() (WaterBall.java:73-90): WaterDragon (orig :47-49), AttackSquid (orig :50-52), Dragon with getDragonType()!=0 (orig :53-55 — missed by the audit fix text, ported per doctrine; port Dragon.java:1121), and the mounted-player check (orig :56-61) moved from onHitEntity so it too flies through instead of splashing/discarding. 1/10 MyWaterBall drop already ported at :66-69 citing orig :63-65 (nextInt(10)==1, spawnAtLocation) — verified. clearFire matches orig :66. AttackSquid's hurt-side WaterBall blank (E4-A, AttackSquid.java:114) kept — both seams existed in orig (AttackSquid.java:373-375).

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
- **Resolution:** FIXED (2026-08-11, Phase E4 ENT-S batch — ranged branch restored as WaterCanonAttackGoal (port WaterDragon.java:368-453), a DinosaurMeleeAttackGoal subclass hooked at onOutOfMeleeRange so the TF-001/TF-026 goal stack and nav-agnostic follow are untouched. Reproduces orig WaterDragon.java:620-648 watercanon: 8-round stream_count bursts with 1-in-4 reload (:645-647), setAttacking(2) pose (:625), 1-in-15 SmallFireball rider aimed center-to-target (:626-631), ownerless WaterBall from muzzle yoff 1.75/xzoff 1.5 keeping the head-yaw-x/body-yaw-z asymmetry (:632-633), lift sqrt(dx²+dz²)*0.2 then shoot 1.4f/5.0f (:634-638), "random.bow" 0.75f→ARROW_SHOOT (:629,:639). Also restored hurt()'s WaterBall exemption (orig :476-478) so volleys never turn dragons on each other.)

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
- **Resolution:** FIXED (2026-08-11, Phase E4 ENT-S batch — sharedEdits: hostile_water_dragon__minecraft_is_river.json weight 2→5 (orig OreSpawnMain.java:4844 — addSpawn river w5) and hostile_water_dragon__direct.json (swamp+mangrove_swamp) weight 2→3 (orig OreSpawnMain.java:4845 — addSpawn swampland w3). Ocean and deep-ocean files verified already correct at w2 (orig :4846-4847); all four files keep minCount/maxCount 1/1 matching the orig addSpawn(…,1,1,waterCreature) rows.)

## WormSmall

### ENT-S-077 — WormSmall: boot-stealing missing

- **Status:** PARTIAL
- **Original:** ORIG `WormSmall.java:179-197` — within 1.5: 1/15 swing; 1/6 chance to rip off boots, damage durability/20, throw on ground
- **Port:** port `:135-146` — 1/15 swing only
- **Fix:** on successful close-range hit, 1/6 roll: remove target's FEET item, damage it `maxDamage/20`, spawn as ItemEntity.
- **Resolution:** FIXED (2026-08-11, Phase E4 ENT-S batch — the theft itself arrived with ENT-S-078/D4 and quote-verifies against orig WormSmall.java:185-195: 1-in-15 swing while upcount>0, inner 1-in-6, FEET strip, durability hit remaining/20 min 1, scatter y+3 at ±0-4 x/z (port EntityWormSmall.java:187-215). Remaining divergence hunted and fixed: target acquisition was getNearestPlayer(this, 1.5) — a 1.5 sphere — vs orig :179 func_72857_a over the bb inflated 1.5/4.0/1.5, losing the 4-block vertical reach; replaced with an exact AABB nearest-player scan (spectators skipped as a post-1.7.10 concept). Break-at-max drop guard kept per the D4 WormSmall/WormLarge convention. Existing gametest i052 (player at +1.0, inside the box) still valid.)

### ENT-S-078 — WormSmall: surface-block death check missing

- **Status:** MISSING
- **Original:** ORIG — dies if rising through non-grass/dirt/stone
- **Port:** rises through anything
- **Fix:** in the rise branch, check the block above; `discard()`/kill if it is not grass/dirt/stone.
- **Resolution:** FIXED (2026-07-02, Phase D4 — the surface-block check ported at every burrow-cycle step (orig WormSmall.java:107-110/124-127/139-142) with tall grass counting as air (:104-106); the 1-in-6 boots theft (:188-195) and night-only spawn gate (:214-216) restored with it; see FIX_LOG.md)

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
- **Resolution:** FIXED (2026-08-11, Phase E4 ENT-S batch — port EntityWormMedium.java:147-226 now reproduces orig WormMedium.java:186-221: PlayNicely gate (:186-188, was missing), WormSmall-within-8/8/8 stand-down (:189-192), player search via the orig bb.inflate(2.25, 8.0, 2.25) nearest scan (:193 — was a 2.25 sphere losing the 8-block vertical reach), creative nulled via instabuild (:194-196 — was wrongly the invulnerable flag), 1-in-15 swing while upcount>0 (:199-200), inner 1-in-6 theft stripping FEET else LEGS (:201-221), durability hit remaining/15 min 1 (:205-206/:214-215), scatter y+3 at ±0-4 x/z (:208/:217) via a stealAndScatter helper matching the WormLarge/10 convention. New gametest ent_s_082_worm_medium_boots_leggings_theft (EntityLogicTestsB) asserts the 1/90 rate band, boots-before-leggings order, item scatter, chaperone and playNicely gates.)

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
- **Resolution:** FIXED (2026-07-02, Phase D4 — theft ported line-by-line: 1-in-4 helmet-else-chestplate steal (orig WormLarge.java:210-230) and independent 1-in-4 held-item steal (:231-238) with the stolen stack zeroed and scattered as an item entity, PlayNicely gate (:192-198), nearest non-creative player within 8 (:199-202); death drops (worm tooth/painting/rotten flesh/leather, :352-377) and the "Large Worm" spawner bypass (:263-309) also restored; see FIX_LOG.md)

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
- **Resolution:** FIXED (2026-06-13, Phase D1 — MobCategory MONSTER→CREATURE, plains/savanna BM JSONs, gate with wormsSpawned side effect per orig WormLarge.java; see FIX_LOG.md)

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
- **Resolution:** FIXED (2026-08-11, Phase E4 BOSS batch — the sidecar is a FAITHFUL ORIGINAL, not a shim: orig KingHead.java:33 sizes it 19.9x10 and :147-149 teleports it 30 blocks along the GAZE (field_70759_as = yHeadRot) at y+12 — an invisible far-forward head surface separate from the model-local part system (orig Render* classes were empty stubs, so port shouldRender()=false is faithful). Restored: registration 3x3 -> 19.9x10 (ModEntities), teleport basis yBodyRot -> getYHeadRot() (port KingHead). The 1.0x damage-forward matches the orig. Spawn/despawn cadence was already faithful (headEntityFound gate = orig head_found, self-discard without a parent). The new BOSS-017 targeting gate fakes headEntityFound=1 while PlayNicely, suppressing the sidecar exactly as orig :985-988 did)

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
- **Resolution:** FIXED (2026-08-11, Phase E4 BOSS batch — same restoration as BOSS-003 for the Queen: orig QueenHead.java:33 = 19.9x10 (registration was 2x2), :147-149 = yHeadRot basis at 30/+12 (was yBodyRot). The mood==1 spawn gate was already faithful (orig TheQueen.java:552-553); the restored BOSS-017 `PlayNicely || isHappy` gate fakes headFound=1, suppressing the sidecar while nice/happy per orig :933-936. The sidecar's direct-to-TheQueen.hurt forward (bypassing MHLib part interception) matches the orig's damage-forward + mood=1-on-hurt behavior)

### BOSS-009 — TheQueen: happy-discharge Bird variant dropped

- **Status:** PARTIAL
- **Original:** ORIG `TheQueen.java:355,430` — happy discharge: 25 soil/flower transforms + 10 Butterfly OR Bird
- **Port:** `TheQueen.java:787-878`, `QueenMoodGoal` — transforms + 10 butterflies only
- **Fix:** in `QueenMoodGoal` happy branch, roll 50/50 between Butterfly and Bird (Cockateil/bird entity) per original.
- **Resolution:** FIXED (2026-08-11, Phase E4 BOSS batch — happy-discharge restored to orig TheQueen.java:424-430: 10 attempts at offsets x/z = nextInt(15)-nextInt(15), y = +nextInt(20); attempts SKIP unless the block is air; each survivor rolls nextInt(2) 50/50 Butterfly vs Cockateil — the orig registers Cockateil.class under the entity name "Bird" (OreSpawnMain.java:3831/:3835). Replaces the port's unconditional 10-butterfly loop with invented offsets (±10, y+5..14) and no air gate. Doc comments updated)

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
- **Resolution:** FIXED (2026-08-11, Phase E4 BOSS batch — same restoration for Godzilla: orig GodzillaHead.java:33 = 9.9x10 (registration was 3x3), :147-149 = yHeadRot basis at 17/+16 (was yBodyRot). The new BOSS-017 gate fakes headFound=1 while PlayNicely per orig Godzilla.java:524-527)

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
- **Resolution:** FIXED (2026-08-11, Phase E4 BOSS batch — the synced-but-never-consumed flag is now consumed everywhere the original consumed it, with per-call dynamic reads matching the orig's static-field checks: TARGETING — TheKing.findSomethingToAttack pacifies + fakes headEntityFound=1 (orig :985-988) and the revenge target nulls (orig :526-529); TheQueen gains the PlayNicely half of both `PlayNicely || isHappy` gates (orig :933-936, :531-534 — only isHappy had been ported); Godzilla.findSomethingToAttack gate (orig :524-527), per-pass target null (orig :356-359), jump-detection/landing-damage + both crushBlocks loops + front jump pulse gated (orig :290/:313/:334/:352), and removeWhenFarAway returns PlayNicely (orig :138 — a nice Godzilla may despawn); ThePrincess's gate already existed (port :584, orig :845-848 — that leg was stale). SIZE — constructor-time snapshot exactly like the orig (never resizes after spawn): King 22x24 -> 5.5x6 (orig :85-89), Queen -> 5.5x6 (orig :78-82; full size keeps the BOSS-007 16x12 parent), Godzilla -> 2.475x6.25 (orig :71-75); while shrunk, King/Godzilla serve NO part surfaces and become directly pickable (the orig single-small-box shape); RENDER — /4 model scale tracking the live synced flag (orig RenderTheKing.java:39-45 pattern) in TheKingRenderer/GodzillaRenderer (scale override) and QueenRenderer (GeckoLib preRender; MHLib bone parts follow the scaled bones); Godzilla gained the missing DATA_PLAY_NICELY watcher + per-tick sync (orig :101/:271). Suite: ConfigGateTests#boss017_play_nicely_gates covers ctor-snapshot dims/pickability/parts and the dynamic no-target/no-sidecar gate)

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
- **Resolution:** FIXED (2026-06-13, Phase D3 — ice block extinguishes / flint & steel relights with the original chat messages, owner-gated at <16 distSq, inserted before the sit toggle per orig order; see FIX_LOG.md)

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
- **Resolution:** FIXED (2026-06-13, Phase D3 — okToGrow gate removed from the natural-growth trigger; the field stays (saved, set by diamond-block and the teen's diamond regression via `setOkToGrow()`) because the original keeps it as the DIAMOND-item grow gate (BOSS-020); see FIX_LOG.md)

### BOSS-022 — ThePrince: ranged attack trio missing

- **Status:** MISSING
- **Original:** ORIG `ThePrince.java:634-663,782-853` — fireball / ThunderBolt / IceBall canons at 5–12 block range when fire enabled
- **Port:** none
- **Fix:** port the three-canon ranged attack (reuse TheKing's `firecanon`/`firecanonl`/`firecanoni` plumbing at baby scale), gated on `DATA_FIRE`.
- **Resolution:** FIXED (2026-06-13, Phase D3 — `firecanon` (big/small BetterFireball), `firecanonl` (×3 ThunderBolt), `firecanoni` (ice-making IceBall) ported at baby scale (muzzle xz 3.0 / y 1.0), fired from `doMovement`'s combat roll behind the 0.5 rad head-bearing gate, 5-12 block band, `DATA_FIRE` + not-in-water gated; see FIX_LOG.md)

### BOSS-023 — ThePrince: flight missing

- **Status:** MISSING
- **Original:** ORIG `ThePrince.java:585-725` — flying wander/owner-follow `do_movement` incl. owner-flying speedups
- **Port:** ground `MyEntityAIWander` (`:82`)
- **Fix:** port the flight movement (the codebase already has the pattern in EntitySpyro `:253-359` — reuse it).
- **Resolution:** FIXED (2026-06-13, Phase D3 — `do_movement` ported line-for-line (activity cycling, owner-flying 1.75×/3.5× speedups, flee-when-hurt retreat, flight-target rerolls, signum steering with 0.5/0.7 prods and yaw/3); activity 2 restores `noPhysics` (BUG-010 interim disable lifted) with the 0.6 y-damping in `aiStep`; closes PN-002 for the baby Prince; see FIX_LOG.md)

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
- **Resolution:** FIXED (2026-06-13, Phase D3 — the teen's full flight brain ported: `fly_without_rider` (orig :677-834 — vertical damping, 1-in-7 combat roll, bite + 5-19t fly-away, `shoot_somethingAt` canon volley <20 blocks behind the 0.5 rad gate, owner-anchored flight targets, terrain-following lift, signum steering with direct move), `always_do` (2 HP regen 1/250, settle rolls, owner creative-flight follow), ground spotting (1-in-10), hurt() immunities/fireball-pop/take-flight, wing sounds every 20t, and the original interaction set (mount, beef full heal, food ×10, ice/flint fire toggles, DIAMOND teen→baby regression restored — see BOSS-029 correction); see FIX_LOG.md)

### BOSS-029 — ThePrinceTeen: regression-to-baby added

- **Status:** DIVERGENT
- **Original:** ~~no shrink-back exists in 1.7.10~~ **CORRECTED (Phase D3):** orig `ThePrinceTeen.java:1230-1250` DOES have a shrink-back — a held DIAMOND reverts the teen to a baby "The Prince" with `set_ok_to_grow()`; the audit (and the Phase C resolution note) missed it. The port's divergence was the ITEM (gold ingot) not the feature.
- **Port:** `ThePrinceTeen.java:240-254` — gold ingot reverts teen → baby
- **Fix:** remove the gold-ingot regression (or document as intentional; note gold ingot also conflicts with BOSS-020's grow item).
- **Resolution:** FIXED (2026-06-11, Phase C — invented gold-ingot teen→baby regression removed) + RE-FIXED (2026-06-13, Phase D3 — the original DIAMOND teen→baby regression restored per orig :1230-1250, including `setOkToGrow()` on the spawned baby; see FIX_LOG.md)

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
- **Resolution:** FIXED (2026-06-13, Phase D3 — the adult's full flight brain ported: `fly_without_rider` (orig :657-814 — 1-in-6 combat roll, 10-block bite + fly-away, `shoot_something` canon volley <~24 blocks behind the 0.5 rad gate at muzzle xz 6.0 / y 3.5, wider owner spreads 8-23/0-11/20-34, terrain lift, signum steering), `always_do` (5 HP regen 1/250, settle rolls, owner creative-flight follow), ground spotting (1-in-10), hurt() immunities incl. the inWall no-damage take-flight branch, wing sounds every 30t, and the original interaction set (mount, beef full heal, food ×10 all at <36 distSq, ice/flint fire toggles, DIAMOND adult→teen regression restored; invented cake + gold-ingot branches removed — see adult_interact_dup); see FIX_LOG.md)

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
- **Resolution:** FIXED (2026-08-11, Phase E4 BOSS batch — legacy NBT migration added: readAdditionalSaveData falls back to the orig key "ThePrinceAdultGrow" (orig ThePrinceAdult.java:1318/:1326) when "PrinceGrow" is absent, so pre-rename saves keep their grow progress. Actual port lines were 1026/1035, not the audit's ~302)

## ThePrincess

### BOSS-038 — ThePrincess: all four core stats off

- **Status:** DIVERGENT
- **Original:** ORIG `ThePrincess.java:194-196,377-379,334-336,62,81` — HP 400, attack 9, armor 14, speed 0.32
- **Port:** `PORT\entity\ThePrincess.java:85-88,52,86` — HP 500, attack 10, armor 16, speed 0.3
- **Fix:** set MAX_HEALTH 400, ATTACK_DAMAGE 9, ARMOR 14, MOVEMENT_SPEED 0.32.
- **Resolution:** FIXED (2026-06-11, Phase B — HP/speed/armor fixed; attack verified correct at orig 10 (audit's 9 wrong), see FIX_LOG.md and phase_b_reports/B2_mobstats.md) — **Phase D3 clarification:** both were right: the orig ATTRIBUTE is 10 (orig :102) but orig melee dealt `getAttackStrength()`=9 (orig :377-379); the port now keeps the 10 attribute and deals 9 via `doHurtTarget` (see BOSS-041 resolution)

### BOSS-039 — ThePrincess: feeding heal flat instead of ×10

- **Status:** PARTIAL
- **Original:** ORIG `ThePrincess.java:224-226` — food heals `healAmount×10`
- **Port:** `ThePrincess.java:197-201` — flat 20
- **Fix:** heal `nutrition × 10` (same fix as BOSS-018).
- **Resolution:** FIXED (2026-06-13, Phase D3 — nutrition × 10 heal, only-when-hurt gate, heart particles per orig :224-240; the port's invented fedCount increment dropped (the original princess has no fed counter logic beyond the saved field); see FIX_LOG.md)

### BOSS-040 — ThePrincess: ranged canon trio missing

- **Status:** MISSING
- **Original:** ORIG `ThePrincess.java:730-748,863-909` — fire/lightning/ice canons
- **Port:** none
- **Fix:** shared canon implementation (BOSS-022 family).
- **Resolution:** FIXED (2026-06-13, Phase D3 — the trio ported at baby scale (muzzle xz 3.0 / y 1.0) behind the 0.5 rad head-bearing gate in `doMovement`'s 1-in-7 combat roll (5-12 block band, fire + not-in-water gated). The full power system came with it: attack_level charge (+1/tick, +4 in combat, zeroed while extinguished), DATA_POWER sync every 10 steps, client firework-spark aura >400, and the >500 discharge — 3 PurplePower orbs in combat, else the terraforming bloom (flowers/grass/dirt/cactus/lava-calming under mobGriefing + 2 Butterfly/Bird hatches); plus ice/flint fire toggles with the Princess-specific messages; see FIX_LOG.md)

### BOSS-041 — ThePrincess: flight missing

- **Status:** MISSING
- **Original:** ORIG — flying `do_movement`
- **Port:** ground wander
- **Fix:** port flight movement (reuse EntitySpyro pattern), same as BOSS-023. (PlayNicely targeting gate: BOSS-017.)
- **Resolution:** FIXED (2026-06-13, Phase D3 — `do_movement` ported line-for-line (same brain as the baby Prince: activity cycling, owner-flying speedups, flee-when-hurt, signum steering); activity 2 restores `noPhysics` with 0.6 y-damping and water buoyancy in `aiStep`; targeting restored to the original prey list (Monster/Mothra/Dragonfly/Mosquito, PlayNicely + Peaceful gates, royalty exempt) and melee fixed to the original 9.0 via `doHurtTarget` (the attribute stays 10 per orig :102 — BOSS-038's "attack 10 verified" refers to the attribute; actual melee used `getAttackStrength()`=9); closes PN-002; see FIX_LOG.md)

### BOSS-042 — ThePrincess: drops beef → diamond

- **Status:** DIVERGENT
- **Original:** ORIG `ThePrincess.java:342-349` — 1–4 beef
- **Port:** `LT the_princess.json` — 1–4 diamond
- **Fix:** rewrite `the_princess.json` to beef 1–4.
- **Resolution:** FIXED (2026-06-11, Phase C — see FIX_LOG.md and phase_c_reports/C5_bosses.md)

### BOSS-045 — ThePrinceTeen: invented cake growth shortcut (found 2026-06-13, Phase D3)

- **Status:** DIVERGENT
- **Original:** ORIG `ThePrinceTeen.java:1127-1273` (`func_70085_c`) — no cake interaction of any kind; the only instant-growth item is the DIAMOND BLOCK (:1133-1150).
- **Port:** a Phase-10 cake branch maxed `killCount`/`dayCount` to 1000 (duplicate of the diamond block's growth credit on a cheaper item).
- **Resolution:** FIXED (2026-06-13, Phase D3 — removed while porting the original interaction set (beef heal, food ×10, ice/flint fire toggles, diamond regression, sit toggle); not archived to MODERNIZATION_NOTES because it duplicated the diamond block's original function; see FIX_LOG.md)

### BOSS-046 — ThePrinceAdult: invented cake shortcut + gold-ingot regression (found 2026-06-13, Phase D3)

- **Status:** DIVERGENT
- **Original:** ORIG `ThePrinceAdult.java:1109-1249` (`func_70085_c`) — no cake and no gold-ingot interaction; the King-growth shortcut is the DIAMOND BLOCK (:1115-1127) and the regression item is a DIAMOND (adult → teen, :1207-1226).
- **Port:** Phase-10 branches: cake maxed `growCounter` to 288000 (duplicate of the diamond block) and a gold ingot reverted adult → teen (duplicate of the diamond regression on a different item).
- **Resolution:** FIXED (2026-06-13, Phase D3 — both removed while porting the original interaction set; the faithful DIAMOND adult→teen regression restored in their place; not archived to MODERNIZATION_NOTES because both duplicated original features; see FIX_LOG.md)

---

## Framework / Config

### BOSS-043 — Boss enable configs missing

- **Status:** PARTIAL
- **Original:** `OreSpawnMain.java:6434-6435` — `TheKingEnable` / `TheQueenEnable` gate boss spawning
- **Port:** `OreSpawnConfig.java` — no equivalents (`MOBZILLA_SINGLE_SPAWN` :128 exists for Godzilla; `FULL_POWER_KING_ENABLE` :159 repurposed)
- **Fix:** add `THE_KING_ENABLE`/`THE_QUEEN_ENABLE` booleans and consume them in the spawner blocks (ties into BOSS-005/012).
- **Resolution:** VERIFIED-CORRECT (2026-08-11, Phase E4 BOSS batch — stale finding, fully implemented by the BOSS-005/012 spawner work: THE_KING_ENABLE/THE_QUEEN_ENABLE defined at OreSpawnConfig.java:285-290 citing orig OreSpawnMain.java:6434-6435, consumed by both BossSpawnerBlock registrations (ModBlocks.java:420-425) AND the natural-spawn gates (ModSpawnControl.java:135-136), suite-covered by ConfigGateTests boss005/boss012 fizzle tests)

### BOSS-044 — MultiHitboxLib only used by TheQueen

- **Status:** PARTIAL
- **Original:** n/a (single AABB + sidecar in 1.7.10)
- **Port:** only `the_queen.json` exists in `RES\data\orespawn\multihitboxlib\hitbox_profiles\`; TheKing and Godzilla still use manual `OreSpawnPartEntity` positioning (`TheKing.java:428-432`, `Godzilla.java:196-227`)
- **Fix:** author `the_king.json` and `godzilla.json` MHLib hitbox profiles (bone-tracked, mirroring the Queen's damage-multiplier scheme) and migrate both bosses off manual part offsets — or document manual parts as final and delete the deprecated head sidecars (BOSS-003/008/014).

---
- **Resolution:** FIXED (2026-08-11, Phase E4 BOSS batch — decision documented per the finding's second option: the MANUAL OreSpawnPartEntity systems are FINAL for TheKing/Godzilla (their envelopes were verified under BOSS-002/BOSS-007), MHLib stays Queen-only, and the head sidecars are NOT deleted — BOSS-003/008/014 established they are faithful originals (the far-forward gaze-tracking head surface) restored to spec, orthogonal to the part systems. A uniform bone-synced MHLib migration for King/Godzilla is archived as the MOD-025 2.0 polish item)

## Register totals

- Total entries: 226 (ENT-SYS2: 4 · ENT-K: 89 · ENT-S: 87 · BOSS: 46; BOSS-045/046 added 2026-06-13, Phase D3)
- DIVERGENT: 130 · PARTIAL: 59 · MISSING: 31 · UNVERIFIED: 6

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


| Block       | ORIG hard/res (file:line)          | PORT hard/res                 | Target   |
| ----------- | ---------------------------------- | ----------------------------- | -------- |
| OreRuby     | 10.0/4.0 (`OreRuby.java:21-22`)    | 3.0/3.0 (`ModBlocks.java:19`) | 10.0/4.0 |
| OreAmethyst | 10.0/4.0 (`OreAmethyst.java` ctor) | 3.0/3.0 (`:21`)               | 10.0/4.0 |
| OreUranium  | 10.0/1.0 (`OreUranium.java:24-26`) | 3.0/3.0 (`:23`)               | 10.0/1.0 |
| OreTitanium | 15.0/5.0 (`OreTitanium.java` ctor) | 3.0/3.0 (`:25`)               | 15.0/5.0 |
| OreSalt     | 5.0/2.0 (`OreSalt.java:21-22`)     | 2.0/2.0 (`:27`)               | 5.0/2.0  |


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


| Block          | ORIG light/hard/res (`OreSpawnMain.java`)           | PORT current                          | Target                             |
| -------------- | --------------------------------------------------- | ------------------------------------- | ---------------------------------- |
| CrystalCoal    | light 9 (0.6), hard 6.0, res 20.0 (`:1865`)         | strength(0.6, 6.0), light 8 (`:72`)   | strength(6.0, 20.0), light 9       |
| CrystalCrystal | light 6 (0.4), hard 12.0, res 40.0 (`:1867`)        | strength(0.4, 12.0), light 12 (`:76`) | strength(12.0, 40.0), light 6      |
| TigersEye ore  | light 7 (0.5×15=7.5), hard 15.0, res 60.0 (`:1868`) | strength(0.5, 15.0), light 12 (`:78`) | strength(15.0, 60.0), light 7 or 8 |


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
- **Resolution:** FIXED (2026-06-12, Phase C — 400-tick fuse + table-driven nextInt(50) pool restored; structure builders beyond generic/ruby dungeon → WGEN-042 (Phase D). 2026-08-08, Phase D5 — outcomes 2 (EnormousCastle King, DSB:59-61), 23 (BasiliskMaze, DSB:122-124), 38 (NightmareRookery, DSB:167-169), 47 (EnormousCastleQ, DSB:194-196) wired via LegacyDungeonPiece.buildNow; 2026-08-08, Phase D6a — outcomes 0 (FairyTree), 1 (FairyCastleTree), 7 (Kyuubi), 24 (Hospital), 27 (EnderCastle), 29 (IncaPyramid), 30 (RobotLab), 37 (MonsterIsland) wired — 14 of 50 live; D6b batches 1-2: +outcomes 3/12/13/14/15/16/17/18/19/20/34 — 25 of 50 live (the prior "27" here was a miscount, corrected during batch 3's recount); D6b batch 3 (2026-08-10): +outcomes 26/28/35/39/44/46 — 31 of 50 live; D6b batch 4 (2026-08-10): the final 19 — six via new structure ports (5 haunted house, 11 ender knight dungeon, 40/43/48/49) and 13 via the sweep (9/10/32 direct; 31/36/41/42 offset-corrected buildNow; 25/33/45 CrystalStructures adapters; 4/6/8 feature buildAt cores) — **ALL 50 of 50 live**; see FIX_LOG.md and dsb_sweep_spec.md)

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
- **Resolution:** VERIFIED-CORRECT (2026-07-02, Phase D4 — RockBlock is dead code in 1.7.10: the class exists in the source but is never instantiated or registered anywhere (grep across OreSpawnMain and the full tree), so no block form ever existed in-game and there is nothing to port; see FIX_LOG.md)

### ITEM-023 — ZooCage: block form dropped

- **Status:** PARTIAL
- **Original:** `ZooCage.java` — cage blocks/entities
- **Port:** `ZooCageItem`/`EmptyCageItem`/`CagedMobItem` + EntityCage — capture flow modernized, block form dropped
- **Fix:** Accept the item-based modernization as design; if block parity is required, add a placed-cage block that renders/holds the captured mob NBT. Document the decision either way.
- **Resolution:** FIXED (2026-08-11, Phase E3 — decision documented + real divergence corrected. The orig ZooCage (ZooCage.java, 77 lines, full read) is an ITEM that instantly builds a quartz-floor/ceiling + glass-wall enclosure anchored at the player — there is NO cage block anywhere in 1.7.10, so no block form exists to port and the item-based capture flow (Phase C acceptance) is the faithful shape, closing the block-form question terminally. Divergence found during completion: the port passed cage_size 2/4/6/8/10 where the orig passes 3/5/9/13/17 (orig OreSpawnMain.java:1931-1935 — the item NAME numbers are not the ctor arg), shrinking three enclosures; fixed, full widths now 5/7/11/15/19 per orig ZooCage.java:31 half=size/2+1. See FIX_LOG Phase E)

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


| Item         | ORIG (file:line)                              | PORT (file:line)                                    | Target                |
| ------------ | --------------------------------------------- | --------------------------------------------------- | --------------------- |
| Sun Fish     | Fire Res 6000t (`ItemSunFish.java:24-48`)     | 600t (`item/ItemSunFish.java:19`)                   | Fire Res 6000t        |
| Fire Fish    | Fire Res 1200t (`ItemFireFish.java:26`)       | 600t (`item/ItemFireFish.java:19`)                  | Fire Res 1200t        |
| Lava Eel     | Fire Res 600t (`ItemLavaEel.java:26`)         | 1200t (`item/ItemLavaEel.java:19`)                  | Fire Res 600t         |
| Spark Fish   | Fire Res 100t (`ItemSparkFish.java:26`)       | 600t (`item/ItemSparkFish.java`)                    | Fire Res 100t         |
| Generic fish | 25% Hunger 20t (`ItemGenericFish.java:24-25`) | 25% Hunger 200t (`item/ItemGenericFish.java:18-19`) | Hunger 20t (keep 25%) |


- **Resolution:** FIXED (2026-06-12, Phase C — see FIX_LOG.md and phase_c_reports/C6_items_blocks.md)

### ITEM-029 — Special foods: potion effects entirely missing (systemic)

- **Status:** MISSING
- **Original:** `ItemSunFish.java:29-48` (shared effect dispatcher) — effects in table below
- **Port:** `ModItems.java:408,410,436,438` — registered as plain foods with no effects
- **Fix:** Add `FoodProperties.effect(...)` entries (or `finishUsingItem` overrides) per table:


| Item                     | Required effects (ORIG `ItemSunFish.java`)                                             |
| ------------------------ | -------------------------------------------------------------------------------------- |
| Butter Candy (`:29-32`)  | Speed + Jump Boost, 2000t                                                              |
| Cooked Bacon (`:33-36`)  | Regeneration + Strength, 2000t                                                         |
| Crystal Apple (`:37-40`) | Regeneration + Strength, 3000t                                                         |
| Heart "Love" (`:41-48`)  | Regen IV + Strength III + Fire Res III + Resistance II 6000t; Speed + Jump Boost 5000t |


## Special swords / melee
- **Resolution:** FIXED (2026-07-02, Phase D4 — orig ItemSunFish.java:29-48 effects restored via FoodProperties: Butter Candy Speed+Jump Boost 2000t, Cooked Bacon Regen+Strength 2000t, Crystal Apple Regen+Strength 3000t, Heart Regen IV/Strength III/Fire Res III/Resistance II 6000t + Speed/Jump 5000t; item renamed "Love" per orig lang; see FIX_LOG.md)

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

- **Resolution:** FIXED (2026-06-12, Phase C — projectile damage/velocity verified number-by-number and fixed (cooldown + Coin inventions removed, DeadIrukandji throw + urchin fire + WaterBall drop restored); Shoes & GameController throwables → Phase D; see FIX_LOG.md and phase_c_reports/C6_items_blocks.md) — CLOSED (2026-07-02, Phase D4 — Shoes & GameController throwables ported: ItemShoes drives all 5 shoe/controller items, the full per-target damage table restored incl. Girlfriend/Boyfriend 1.0f and the Valentine's-Day 10.0f override, reddust + snowballpoof impact particles)

### ITEM-054 — All 14 armor sets: durability ~1/15th and enchantability wrong (systemic)

- **Status:** DIVERGENT
- **Original:** `OreSpawnMain.java:1489-1502` (`get_armorstats(name, durability, head, chest, leg, boot, enchantability, ...)`) + `EnumHelper.addArmorMaterial` at `:1770-1783` — durability is a per-slot **multiplier** (helmet ×11, chest ×16, legs ×15, boots ×13; e.g. Ultimate helmet = 200×11 = 2200)
- **Port:** `ModItems.java:529-736` — durability multipliers ≈ orig/16 (Ultimate 13, Royal 125, Mobzilla 63, Queen 94); `ModArmorMaterials.java:43-98` — passes original *durability* as `enchantmentValue` (Ultimate 200, Royal 2000, Mobzilla 1000, Queen 1500, Amethyst 100...). Defense values are correct; the comment at `ModArmorMaterials.java:37-41` misstates what `ModItems` does.
- **Fix:** 1.21.1 `ArmorItem.Type.getDurability(mult)` uses the same per-slot bases (11/16/15/13), so set each set's durability multiplier to the **original durability value** and each `enchantmentValue` to the **original enchantability**, per this table:


| Set        | Durability multiplier (target) | Enchantability (target) |
| ---------- | ------------------------------ | ----------------------- |
| Ultimate   | 200                            | 100                     |
| Royal      | 2000                           | 200                     |
| Queen      | 1500                           | 150                     |
| Mobzilla   | 1000                           | 150                     |
| Amethyst   | 100                            | 40                      |
| Emerald    | 60                             | 40                      |
| Experience | 70                             | 50                      |
| Moth Scale | 50                             | 50                      |
| Lava Eel   | 40                             | 35                      |
| Pink       | 50                             | 40                      |
| Tigers Eye | 80                             | 55                      |
| Peacock    | 40                             | 30                      |
| Ruby       | 90                             | 40                      |
| Lapis      | 60                             | 60                      |


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


| Set                  | Correct values (ORIG)                                       | Port error                                     |
| -------------------- | ----------------------------------------------------------- | ---------------------------------------------- |
| Ultimate (`:1494`)   | Resp2, Aqua3, Prot5, Fire5, Blast5, Proj5, Unb0, Fall3      | adds Unbreaking 3 → remove                     |
| Mobzilla (`:1498`)   | Resp0, Aqua0, Prot10, Fire10, Blast10, Proj10, Unb5, Fall10 | adds Resp1/Aqua2 → remove                      |
| Moth Scale (`:1492`) | Prot3, Fire3, Blast3, Fall5, all else 0                     | adds Unbreaking 3 → remove                     |
| Lava Eel (`:1493`)   | Resp1, Aqua2, Prot3, Fire2, Blast10, Proj0, Unb0, Fall2     | Prot 2→3, add Fire Prot 2, remove Unbreaking 3 |


- **Resolution:** FIXED (2026-06-12, Phase C — see FIX_LOG.md and phase_c_reports/C6_items_blocks.md)

### ITEM-057 — Experience armor: XP-generation set effect missing

- **Status:** MISSING
- **Original:** `ExperienceSword.java:63-103` — ticking Experience Sword grants XP while the player wears Experience armor
- **Port:** absent — no XP-generation tick anywhere
- **Fix:** Implement the inventory-tick XP grant in the port's ExperienceSword (or a player-tick handler) scaling with worn Experience armor pieces, matching original tick rates/amounts. (Same root as ITEM-040.)
- **Resolution:** FIXED (2026-07-02, Phase D4 — the armor-set XP effect was already restored alongside ITEM-040 in C6 (sword and armor share the XP-bottle set-effect handler); D4 closed the item half: ItemExperienceTreeSeed placement/consumption ported faithfully and the invented leaf-harvest mechanic removed from BlockExperienceLeaves; the tree worldgen body itself is WGEN-045 (Phase D5); see FIX_LOG.md)

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
- **Resolution:** FIXED (2026-07-02, Phase D4 — `skate_bow.json` added per the orig registration (crystal-stick + string bow shape); see FIX_LOG.md)

### ITEM-061 — Crystal planks → chest / piston conversions absent

- **Status:** MISSING
- **Original:** `OreSpawnMain.java:3083-3085,3209` — crystal planks craft into vanilla chest / piston
- **Port:** absent — no such JSONs; port instead has oak_door/iron_door conversions (divergent substitutions)
- **Fix:** Add recipe JSONs for chest and piston accepting crystal planks (tag or explicit item); review whether the door conversions should remain.
- **Resolution:** FIXED (2026-07-02, Phase D4 — `chest_from_crystal_planks.json` added (orig OreSpawnMain.java:3083, duplicated at :3209). The audit's "piston" reading was a misidentification: field_151135_aq at :3084-3085 is the 1.7.10 wooden door, crafted in the 2x3 plank shape — the port's existing `oak_door_from_crystal_planks.json` already matches it faithfully, so the door conversion flagged as divergent is in fact correct; see FIX_LOG.md)

### ITEM-062 — Bulk recipe correspondence not diffed

- **Status:** UNVERIFIED
- **Original:** 381 registrations (189 shaped + 176 shapeless + 16 smelting), `OreSpawnMain.java:3000s`
- **Port:** 236 JSONs in `data/orespawn/recipe/` — only ~20 spot-checked
- **Fix:** Unverified because a full 381↔236 correspondence diff was never run. Resolve by scripting an extraction of all original `addRecipe`/`addShapelessRecipe`/`addSmelting` calls and matching each against a port JSON (mirrored left/center/right variants count as one).

## Dispenser behaviors

- **Resolution:** FIXED (2026-08-08, Phase D5 — the final remainder closed: all 116 water-bucket spawn-block→egg conversions (orig OreSpawnMain.java:2665-3021, shapeless water bucket + block → 1 egg, bucket returned via the modern crafting remainder) plus the 3 nine-part combines (Mobzilla/King/Queen full egg blocks, :2886/2892/2898), generated by `tools/d5_gen_spawn_ores.py` against the verified 119-row table; vanilla-mob outputs use modern vanilla eggs (incl. ender_dragon/iron_golem/snow_golem/wither — all present since 1.20.5), CriminalEgg → band_p_spawn_egg (WGEN-017), EnchantedCowEgg → enchanted_apple_cow_spawn_egg (the consolidated original). Previously PARTIAL (2026-06-12 Phase C recipe-corpus diff; 2026-07-02 Phase D4 six standalone recipes); see FIX_LOG.md)

### ITEM-063 — All 8 dispenser behaviors unimplemented (systemic)

- **Status:** MISSING
- **Original:** 8 classes: `MyDispenserBehaviorAcid`, `MyDispenserBehaviorIceball`, `MyDispenserBehaviorLaserball`, `MyDispenserBehaviorRock`, `MyDispenserBehaviorWDCharge`, `MyDispenserBehaviorDeadIrukandji`, `MyDispenserBehaviorArrow` (Irukandji arrow), `MyDispenserBehaviorSunspotUrchin`
- **Port:** `ModDispenserBehaviors.java:3-7` — empty stub ("will be added when entity projectile constructors are finalized")
- **Fix:** Implement `DispenserBlock.registerBehavior` for all 8 items, each spawning its projectile entity with the original velocity/offsets: acid, iceball, laserball, rock, WD charge, dead irukandji, irukandji arrow, sunspot urchin.

## Config
- **Resolution:** FIXED (2026-07-02, Phase D4 — `ModDispenserBehaviors` registers all 8 original behaviors (IrukandjiArrow with pickup-allowed, WaterBall, SunspotUrchin, Acid, IceBall, DeadIrukandji, LaserBall, plus the shared rock behavior stamped onto all 12 rock items with types 1-12; orig OreSpawnMain.java:5755-5773 + MyDispenserBehavior*.java) using the original BehaviorProjectileDispense numbers — velocity 1.1, inaccuracy 6.0, +0.1 vertical bias, aux effect 1002; see FIX_LOG.md)

### ITEM-064 — LESS_ORE defined but not wired

- **Status:** PARTIAL
- **Original:** `LessOre` config gates ore-generation multiplier (notably Mining dim 3× passes)
- **Port:** `OreSpawnConfig.java:139-141` — `LESS_ORE` exists with explicit TODO, affects nothing
- **Fix:** Wire `LESS_ORE` into the datapack/feature pipeline (e.g. select between normal and reduced placed-feature sets, and gate the Mining-dim density per WGEN-011).
- **Resolution:** FIXED (2026-06-12, Phase C — lessOre wired via the orespawn:less_ore_count placement modifier for overworld ore/troll-block veins; Mining-dim density gating → WGEN-011, itself FIXED in Phase C — nothing remained; CLOSED 2026-08-10 at the D close-out; see FIX_LOG.md and phase_c_reports/C6_items_blocks.md)

### ITEM-065 — Per-tier weapon/armor/ore stat overrides missing

- **Status:** MISSING
- **Original:** `OreSpawnMain.java:1491-1517` — `get_weaponstats`/`get_armorstats`/`get_orestats` exposed every stat number to the config file
- **Port:** absent — `WeaponStats`/`ArmorStats` records hardcoded
- **Fix:** Either add config bindings that override the record defaults at registration time, or document hardcoding as a deliberate platform decision (datapacks cover ore stats).

---

# PART B — Worldgen, Dimensions, Structures (file 07)

## Overworld ore generation
- **Resolution:** DEFERRED (2026-07-02, Phase D4 — the orig config-file per-tier weapon/armor/ore stat overrides cannot be replicated against NeoForge's frozen static item registries without registry mutation; the orig default values stay hardcoded (verified number-by-number in earlier slices), the platform decision is documented as PARITY_NOTES PN-013 and the config system as 2.0 modernization candidate MODERNIZATION_NOTES MOD-011; see FIX_LOG.md. **Owner approval recorded 2026-07-03 at the D4 checkpoint** — PN-013/MOD-011 stand as written)

### ITEM-066 — Invented Prince/Princess trophy eggs replaced the functional spawn eggs (found 2026-08-08, Phase D5)

- **Status:** DIVERGENT
- **Original:** `ThePrinceEgg`/`ThePrincessEgg` are functional `ItemSpawnEgg`s ("eggtheprince"/"eggtheprincess", OreSpawnMain.java:5616/5630); dropped by TheQueen (TheQueen.java:192), ThePrinceTeen (:318) and ThePrinceAdult (:314), and placed in the Challenge Tower prize chests (GenericDungeon.java:747/6949).
- **Port:** pre-plan "Phase 12" registered inert trophy items `prince_egg`/`princess_egg` and used them in all five consumer sites, so the drops and chest rewards could not actually spawn royalty.
- **Resolution:** FIXED (2026-08-08, Phase D5 — all consumers (the_queen/the_prince_teen/the_prince_adult loot tables + both tower prize chests) switched to `the_prince_spawn_egg`/`the_princess_spawn_egg`; the trophy items, their models and lang entries removed. Cosmetic delta: the port's spawn eggs use the mod-wide tinted template rather than the original per-egg `eggtheprince.png` texture (textures retained in-repo); see FIX_LOG.md)


### WGEN-001 — OreSpawn ore vein counts inflated, Y-floors extended to −64 (systemic)

- **Status:** DIVERGENT
- **Original:** `OreSpawnMain.get_orestats` defaults, executed by `ChunkOreGenerator.generateOresInChunk`
- **Port:** `data/orespawn/worldgen/placed_feature/ore_*.json` + `configured_feature/ore_*.json`
- **Fix:** Set placed-feature `count` and height range to original values (salt already exact):


| Ore                            | ORIG rate/clump/Y | PORT count/size/Y | Target                    |
| ------------------------------ | ----------------- | ----------------- | ------------------------- |
| Ruby (`ore_ruby.json`)         | 10 / 1 / Y0–50    | 8 / 2 / −64..50   | count 10, size 1, Y 0..50 |
| Uranium (`ore_uranium.json`)   | 3 / 4 / Y0–30     | 5 / 4 / −64..30   | count 3, Y 0..30          |
| Titanium (`ore_titanium.json`) | 3 / 4 / Y0–20     | 6 / 4 / −64..20   | count 3, Y 0..20          |
| Amethyst (`ore_amethyst.json`) | 2 / 6 / Y0–25     | 4 / 6 / −64..25   | count 2, Y 0..25          |


(If the deepslate layer should keep ores, document the −64 floor as a deliberate 1.21 adaptation instead.)

- **Resolution:** FIXED (2026-06-12, Phase C — new orespawn:vein_count placement reproduces the exact rate+nextInt(dice) / LessOre-divide / nextInt(128) Y-reject loop per ore; all ore_*.json rebuilt with original rates and Y windows; see FIX_LOG.md and phase_c_reports/C7_worldgen.md)

### WGEN-002 — Ruby: lava-adjacency placement mechanic dropped

- **Status:** DIVERGENT
- **Original:** `OreSpawnWorld.generateRuby` — rubies placed adjacent to lava pockets via a lava-seek loop
- **Port:** `placed_feature/ore_ruby.json` — standard random ore placement
- **Fix:** Implement a custom feature (or use a `block_predicate`-filtered placement) that only places ruby ore next to lava source blocks, mirroring the lava-seek loop.
- **Resolution:** FIXED (2026-06-12, Phase C — ore_ruby configured feature replaced with orespawn:ruby_lava_seek (rate 10+nextInt(5), Y≤0..50, lava-over-stone descent per OreSpawnWorld.java:879-892); see FIX_LOG.md and phase_c_reports/C7_worldgen.md)

### WGEN-003 — Block-of-Ruby ore generation absent

- **Status:** MISSING
- **Original:** `OreSpawnMain` BlockRuby_stats — 1 vein / clump 2 / Y0–15
- **Port:** absent — no feature for ruby-block veins
- **Fix:** Add configured+placed features generating `block_ruby` veins: count 1, size 2, Y 0..15, and include them in `add_ores.json`.
- **Resolution:** FIXED (2026-08-11, Phase E3 — orespawn:ore_block_ruby configured (minecraft:ore size 2, stone+deepslate targets -> block_ruby) + placed (orespawn:vein_count count 1, Y 0-15, less_ore_passes 0 — the whole boost block sits inside the orig's LessOre==0 gate) + add_ores.json entry; values verified against orig OreSpawnMain.java:1574 get_orestats(BlockRuby, 1, 2, 0, 15) and the loop at orig OreSpawnWorld.java:949-957. Mining-dim triple pass added too (ore_block_ruby_mining, passes 3/less_ore_passes 0, per orig ChunkOreGenerator sharing the same boost block + ChunkProviderOreSpawn2's 3-call pattern). See FIX_LOG Phase E)

### WGEN-004 — Vanilla-ore boost generation absent

- **Status:** MISSING
- **Original:** `ChunkOreGenerator.generateOresInChunk` — extra Diamond 4/6/Y0–30, Diamond Block 2/4/Y0–20, Emerald 4/6/Y0–40, Emerald Block 2/4/Y0–20, Gold 4/8/Y0–40, Gold Block 2/4/Y0–25 on top of vanilla
- **Port:** absent — only vanilla defaults generate
- **Fix:** Add six configured/placed features with the listed rate/clump/Y values and register via the `add_ores` biome modifier.
- **Resolution:** FIXED (2026-08-11, Phase E3 — six boost features added exactly per orig OreSpawnMain.java:1580-1585 stats and the OreSpawnWorld/ChunkOreGenerator boost loops: diamond 4/6/Y0-30, diamond_block 2/4/Y0-20, emerald 4/6/Y0-40, emerald_block 2/4/Y0-20, gold 4/8/Y0-40, gold_block 2/4/Y0-25; vein_count placement with less_ore_passes 0 (boosts vanish entirely under LessOre, matching the orig gate), plain count (no extra dice in the boost loops), deepslate ore variants on the deepslate target for the three vanilla ores. Overworld via add_ores.json AND the Mining-dim gap the implementation surfaced (mining_biome.json had NO boosts and zero emerald of any kind) closed with 7 *_mining placed features at passes 3/less_ore_passes 0. See FIX_LOG Phase E)

### WGEN-005 — SpawnOres system reduced from ~105 block types to 2

- **Status:** PARTIAL
- **Original:** `OreSpawnMain` SpawnOres stats + `ChunkOreGenerator` — 28 veins/chunk clump 4 Y50–128 (+30 veins on a 1/20 roll) over a pool of ≈105 spawn-block types (7 OreSpawn + 98 vanilla-mob)
- **Port:** `placed_feature/dragon_spawn_block.json`, `kraken_spawn_block.json` (each 1/24 chunks, Y −56..−10) + `add_ancient_dried_eggs.json` (1/12, Y −32..32)
- **Fix:** Decide scope: full parity needs the spawn-block pool restored (custom feature picking from the weighted 105-type pool at 28+/chunk, Y50–128); otherwise document the 2-boss-block reduction as a deliberate redesign.
- **Resolution:** FIXED (2026-08-08, Phase D5 — full pool restored: 106 new OreGenericEgg blocks registered (119-row master table, `phase_d_reports/d5_extraction/spawn_ores_spec.md` section 2) and `SpawnOresPoolFeature` reproduces the original roll exactly (28+nextInt(20/30) veins, +30 on 1/20, LessOre integer-div 3, Y-window 50..127 discard filter, 7-in-104 rare tier, exact nextInt(98)/nextInt(7) switch orders) in overworld + Utopia/Village/Chaos + Mining x3 passes per orig OreSpawnWorld.java:355-803 / ChunkOreGenerator.java:21-469 / ChunkProviderOreSpawn2.java:191-195; the interim Phase C7 dragon/kraken features + invented ancient-dried-egg block retired (PN-010 closed, rehydration archived MOD-013). Previously PARTIAL (2026-06-12, Phase C — deliberate interim redesign); see FIX_LOG.md and phase_d_reports/D5_structures_spawnores.md)

### WGEN-006 — AntHill surface blocks never world-placed

- **Status:** PARTIAL
- **Original:** `OreSpawnWorld.addAnts` (called at `:323`, redfreq=4) — ~4%/chunk anthill blocks (black/red/rainbow/unstable) in plains-type biomes
- **Port:** ant blocks registered (`ModBlocks.java:291-306`) but no placement feature; black/red ants natural-spawn instead (`add_overworld_creatures.json`)
- **Fix:** Add an anthill placed feature (~4%/chunk, plains biomes) placing the four ant-block types; this also restores rainbow/unstable ant access (see WGEN-048).
- **Resolution:** FIXED (2026-06-12, Phase C — new orespawn:anthill feature (1/30 gate, 4 attempts, redfreq picker per OreSpawnWorld.addAnts:1472-1507) wired to overworld (#is_overworld redfreq 4), Utopia/Village (4), Mining (2, x2), Chaos (2); see FIX_LOG.md and phase_c_reports/C7_worldgen.md)

### WGEN-007 — Wild crop patches (strawberry/corn/tomato) absent

- **Status:** MISSING
- **Original:** `OreSpawnWorld.generateSurface` — strawberry patches in forest biomes; corn (~1%) and tomato patches in plains
- **Port:** absent — crop blocks exist but have no wild generation
- **Fix:** Add three random-patch configured/placed features (strawberry → `#is_forest`; corn ~1%/chunk and tomato → plains) via biome modifiers.
- **Resolution:** FIXED (2026-08-11, Phase E3 — WildCropsFeature ports all three generators line-for-line: strawberries 1-in-20 gate, 5 attempts, Y100->41 air scan onto grass (orig OreSpawnWorld.java:961-978); corn 1-in-35 gate, 6/5/3 attempts by LessLag, 9-air-above column check, height 1+nextInt(5) with the corn_0/corn_1/corn_3 stalk grammar (orig :1023-1068; block mapping authoritative from BlockCorn.java:21-23 Javadoc); tomatoes 1-in-70, 5 attempts, height 1+nextInt(3), tomato_0/1/2/3 grammar (orig :1069-1110). Overworld: strawberries -> forest/windswept_forest/birch_forest/old_growth_birch_forest (the established C1 ForestHills mapping), corn+tomatoes -> plains exact. Dimensions per the orig gates: strawberries + corn + tomatoes in Utopia (utopia_plains.json), corn + tomatoes in Village (orig :966 excludes strawberries from DimensionID3). See FIX_LOG Phase E)

## Utopia dimension

### WGEN-008 — Utopia: veggie patches missing, altar frequency changed

- **Status:** PARTIAL
- **Original:** `OreSpawnWorld.java:42-51` — veggie patches among features; King/Queen Altar 1/2000 chunk roll after tree passes (`:2550`)
- **Port:** trees/altars present but no veggie patches; altars use `structure_set/royal_altars.json` random_spread spacing 64 / separation 32
- **Fix:** Add Utopia veggie-patch features (same crop set as overworld wild gen); tune royal_altars spacing if 64/32 measurably differs from the 1/2000-roll density in practice.
- **Resolution:** FIXED (2026-06-12, Phase C — veggie_patch_utopia (count 2) added to utopia_plains; royal_altars spacing corrected 48/24 → 45/22 (1/2000 roll, OreSpawnWorld.java:2550 → 45²=2025); see FIX_LOG.md and phase_c_reports/C7_worldgen.md)

### WGEN-009 — Utopia: cricket/frog spawn group sizes

- **Status:** UNVERIFIED
- **Original:** `BiomeGenUtopianPlains` default ctor — Cricket 5, Frog 5 (original min/max group sizes not extracted)
- **Port:** `utopia_plains.json` — cricket 5(4-6), frog 5(4-6)
- **Fix:** Unverified because the original group min/max were not read. Resolve by checking the `BiomeGenUtopianPlains` SpawnListEntry args for cricket/frog and aligning the JSON group sizes.

## Mining dimension

- **Resolution:** VERIFIED-CORRECT (2026-06-12, Phase C — orig BiomeGenUtopianPlains.java:132/:135: Cricket 5(4,6) ambient, Frog 5(4,6) water; port matches exactly; see FIX_LOG.md and phase_c_reports/C7_worldgen.md)

### WGEN-010 — Mining: vanilla dungeons/mineshafts/strongholds absent

- **Status:** PARTIAL
- **Original:** `ChunkProviderOreSpawn2` — vanilla dungeons ×8/chunk, mineshafts, strongholds, scattered features
- **Port:** `mining_biome.json` — caves/springs only; no vanilla structure sets apply to this biome
- **Fix:** Add `mining_biome` to the vanilla `has_structure` tags for mineshaft/stronghold, and add a monster-room-style feature at 8 attempts/chunk to the biome's features.
- **Resolution:** FIXED (2026-06-12, Phase C — mining/village biomes added to minecraft:has_structure/mineshaft+stronghold tags; lake_water_dim (1/4), lake_lava_dim (1/8), monster_room_dim (count 8) placed features added per ChunkProviderOreSpawn2 populate; see FIX_LOG.md and phase_c_reports/C7_worldgen.md)

### WGEN-011 — Mining: 3× ore density lost

- **Status:** DIVERGENT
- **Original:** `ChunkProviderOreSpawn2` — `generateOresInChunk` called up to 3× when `LessOre==0`, plus extra `generateRuby` and extra diamond/gold passes
- **Port:** `mining_biome.json` — same 1× rates as overworld
- **Fix:** Create mining-specific placed features at 3× count (gated by `LESS_ORE`, see ITEM-064) plus the extra ruby/diamond/gold passes, and reference them only from `mining_biome.json`.
- **Resolution:** FIXED (2026-06-12, Phase C — *_mining ore variants run the whole vein loop with passes:3 / less_ore_passes:1 (ChunkProviderOreSpawn2.java:191-195); ruby x3 via less_ore_count{3,1}; lapis boost 45x size7 + 25x size4 Y<50 LessOre==0-only restored (OreSpawnWorld.java:64-77); see FIX_LOG.md and phase_c_reports/C7_worldgen.md)

### WGEN-012 — Mining: dino/alien monster roster replaced

- **Status:** DIVERGENT
- **Original:** `ChunkProviderOreSpawn2.java:374-399` — Alosaurus 8(1-2), TRex 6(1-2), Nastysaurus 6(1-2), Pointysaurus 10(4-8), GammaMetroid 35(4-7), Alien 35(2-3), CaveFisher 35(4-8), Cryolophosaurus 26(4-7), Spyro 5(1-2) + biome defaults
- **Port:** `mining_biome.json` — rat 30(4-8), cave_fisher 10(1-3), molenoid 5(1-2), worms 8/4/2, creeping_horror 5(1-3), scorpion 3(1-2)
- **Fix:** Replace the monster list in `mining_biome.json` with the original roster/weights/groups (CaveFisher 35(4-8) etc.), keeping only entities that exist in the port; track unported entities separately.
- **Resolution:** FIXED (2026-06-12, Phase C — mining_biome monster roster rebuilt to the 9-entry dino/alien overlay (ChunkProviderOreSpawn2.java:374-399) + vanilla Extreme Hills defaults; invented rat/worms/molenoid/creeping_horror/scorpion/leonopteryx/firefly entries removed; see FIX_LOG.md and phase_c_reports/C7_worldgen.md)

### WGEN-013 — Mining: ambient spawns absent

- **Status:** MISSING
- **Original:** `ChunkProviderOreSpawn2.java:410-419` — VelocityRaptor 1(2-4), Dragonfly 2(1-3), Camarasaurus 1(2-4), Baryonyx 2(4-8)
- **Port:** `mining_biome.json` — firefly 5(1-3) only
- **Fix:** Add the four original ambient/creature entries with the listed weights/groups to `mining_biome.json` (for ported entities).
- **Resolution:** FIXED (2026-06-13, Phase D1 — verified already present: mining_biome.json carries VelocityRaptor w1 2-4, Dragonfly w2 1-3, Camarasaurus w1 2-4, Baryonyx w2 4-8 from the Phase C7 roster rebuild per ChunkProviderOreSpawn2.java:410-419; no change needed)

### WGEN-014 — Mining: BasiliskMaze/KyuubiDungeon/EnderKnight dungeon absent; BeeHive relocated

- **Status:** PARTIAL
- **Original:** `OreSpawnWorld:2031-2057` + Mining hooks — BasiliskMaze, KyuubiDungeon, BeeHive, ShadowDungeon, AlienWTF, EnderKnight, LeonNest, generic dungeon
- **Port:** only `shadow_dungeon`, `wtf_alien_dungeon`, `leonopteryx_nest` (each set 26/13) + generic dungeon; BeeHive moved to overworld forests
- **Fix:** Port BasiliskMaze (WGEN-037), KyuubiDungeon and EnderKnightDungeon (WGEN-042) as mining_biome structures; restore BeeHive to Mining or document relocation (WGEN-040).

## Village Mania dimension

- **Resolution:** FIXED (2026-06-12, Phase C — BeeHive restored to Mining (WGEN-040) and shadow/WTF/Leon frequencies corrected (WGEN-039); BasiliskMaze is WGEN-037 and KyuubiDungeon/EnderKnightDungeon are WGEN-042. CLOSED 2026-08-10, D close-out: maze D5, Kyuubi D6a, EnderKnightDungeon D6b batch 4 (LOWEST_GRASS_36 mining set 26/13); see FIX_LOG.md and phase_d_reports/phase_d_rollup.md)

### WGEN-015 — Village dimension generates no villages

- **Status:** MISSING
- **Original:** `MapGenMoreVillages.java:11-12` — spacing 9 / separation 7 (vanilla 32/8 → ~12× denser), enabled in dim 82 (`ChunkProviderOreSpawn3`); plus `BiomeManager.addVillageBiome` in `WorldProviderOreSpawn3`
- **Port:** absent — no `minecraft:villages` structure-set override, `orespawn:village_biome` not in any `has_structure/village` tag, no `data/minecraft/tags/worldgen` overrides at all
- **Fix:** Add `orespawn:village_biome` to `data/minecraft/tags/worldgen/biome/has_structure/village_plains.json` (override) and add a `data/minecraft/worldgen/structure_set/villages.json` override (or dimension-scoped set) with spacing 9 / separation 7.
- **Resolution:** FIXED (2026-07-02, Phase D1 — PN-012 approved (Option A): worldgen/structure/dim_village.json (vanilla plains jigsaw start pool in orespawn:village_biome) + structure_set/dim_villages.json spacing 9 / separation 7 per MapGenMoreVillages.java:11-12; village style is modern 1.21.1 jigsaw — the original delegated style to vanilla, OreSpawn only controlled density/placement, which are exact; see PARITY_NOTES PN-012 and FIX_LOG.md)

### WGEN-016 — Village: dimension style is a no-op placeholder

- **Status:** PARTIAL
- **Original:** `ChunkProviderOreSpawn3` — overworld noise + lakes + vanilla dungeons/mineshafts/strongholds
- **Port:** `DimensionStyle.java:50-52` — style VILLAGE = pass-through ("identical to DEFAULT for now")
- **Fix:** Add lakes/springs and vanilla underground structures (mineshaft/stronghold/monster rooms) to `village_biome.json` / its tags, mirroring WGEN-010.
- **Resolution:** FIXED (2026-06-12, Phase C — Village dimension style now populates per ChunkProviderOreSpawn3: vanilla mineshaft/stronghold/lakes/monster rooms (WGEN-010 mechanism), generic dungeon 1/16, anthills (redfreq 4), apple trees; villages themselves remain WGEN-015 (MISSING, Phase D); see FIX_LOG.md and phase_c_reports/C7_worldgen.md)

### WGEN-017 — Village: mob roster gaps and unverified weights

- **Status:** PARTIAL
- **Original:** `BiomeGenUtopianPlains.setVillageCreatures` — Robot1-5, Jeffery, SpiderDriver, Godzilla, Girlfriend, Boyfriend, cows, Butterfly, LunaMoth, Chipmunk, Cockateil, Tshirt, Coin, Criminal
- **Port:** `village_biome.json` + `dim_village_locals.json` — robots/cows/etc. present; **Jeffery, SpiderDriver, Godzilla, Criminal missing**; giant_robot/band_p added; robot weights UNVERIFIED vs original
- **Fix:** Add spawn entries for Jeffery, SpiderDriver, Godzilla, Criminal once those entities exist; diff robot_1-5 weights/groups against `setVillageCreatures` exact values and align.
- **Resolution:** FIXED (2026-06-12, Phase C — village roster rebuilt from setVillageCreatures + the un-reset Utopia ctor + vanilla defaults (lists are never cleared, BiomeGenUtopianPlains.java:272-332); spider_driver 20(3,5) and godzilla 2(1,1) added; audit corrected: Jeffery IS the port giant_robot (JefferyEnable→GiantRobot :289) and Criminal IS band_p (CriminalEnable→BandP :330) — neither was missing; invented beaver entry removed; see FIX_LOG.md and phase_c_reports/C7_worldgen.md)

### WGEN-018 — Village: DamselInDistress/SpiderHangout/RedAntHangout structures absent

- **Status:** PARTIAL
- **Original:** `OreSpawnWorld.java:118-128` — mosquitos, ants (redfreq 4), apple trees, generic dungeon, DamselInDistress, SpiderHangout, RedAntHangout
- **Port:** `greenhouse` (48/24), `robot_lab` (44/22), `white_house` (48/24) tagged village_biome + generic dungeon — the three original Village structures absent (the three present ones were Islands-dim structures, see WGEN-022)
- **Fix:** Port DamselInDistress, SpiderHangout, RedAntHangout as structures tagged `orespawn:village_biome` with sets approximating their original per-chunk roll densities.

## Islands dimension

- **Resolution:** FIXED (2026-06-12, Phase C — the divergent half fixed: greenhouse/robot_lab/white_house re-tagged to Islands (WGEN-022) so Village no longer hosts them; DamselInDistress/SpiderHangout/RedAntHangout were WGEN-042 owners. CLOSED 2026-08-10, D close-out: Damsel D6b batch 3, both Hangouts batch 4, all inline orespawn:village_biome; see FIX_LOG.md and phase_d_reports/phase_d_rollup.md)

### WGEN-019 — Islands: flat-plane terrain replaced by floating-islands noise

- **Status:** DIVERGENT
- **Original:** `ChunkProviderOreSpawn4` + `Island.java:64-79` — flat plane (bedrock y0 + dirt + grass); islands built dynamically by Island/IslandToo entities (small r3-6/d2-4, 1/40 large r6-10/d3-6)
- **Port:** `dimension/islands.json` — `minecraft:floating_islands` noise; island-builder entities additionally spawn (`dim_islands_locals.json`)
- **Fix:** Either restore the flat-plane generator (custom flat noise settings: bedrock+dirt+grass) so entity-built islands are the sole terrain, or remove/retune one of the two systems — currently both run, which neither matches original look nor static-noise intent.
- **Resolution:** FIXED (2026-06-12, Phase C — noise_settings/islands.json generates the original flat plane (bedrock Y0, dirt Y1-6, grass Y7 per ChunkProviderOreSpawn4.java:30-32); island_biome carvers/vanilla features emptied; see FIX_LOG.md and phase_c_reports/C7_worldgen.md)

### WGEN-020 — Islands: spawn weights not verified

- **Status:** UNVERIFIED
- **Original:** `setIslandCreatures` — full roster (Dragon, Stinky, CliffRacer, CloudShark, terrors, etc.); exact weights not extracted
- **Port:** `island_biome.json` + locals — full roster present with stated weights
- **Fix:** Unverified because original weight/group numbers were never read. Resolve by extracting `setIslandCreatures` SpawnListEntry args and diffing against the biome JSON.
- **Resolution:** FIXED (2026-06-12, Phase C — weights verified against setIslandCreatures (BiomeGenUtopianPlains.java:142-199): all 16 entries match; duplicate terrible_terror/ender_reaper entries (biome JSON + dim_islands_locals doubling the weights) consolidated; see FIX_LOG.md and phase_c_reports/C7_worldgen.md)

### WGEN-021 — Islands: ~13 D4 structures absent

- **Status:** MISSING
- **Original:** `OreSpawnWorld.java:134-198` — D4Castle, D4GenericDungeon, D4EnderCastle, D4IncaPyramid, D4RobotLab, D4Mini, D4RubyDungeon, D4CephadromeAltar, D4Greenhouse, D4NightmareRookery, D4StinkyHouse, D4WhiteHouse, Pumpkin, D4Rainbow, D4CloudShark, UnstableAnts placement
- **Port:** absent — nothing tagged `island_biome` (RobotLab/Greenhouse/WhiteHouse were moved to Village)
- **Fix:** Port the D4 structure builders as jigsaw/legacy-piece structures tagged `orespawn:island_biome` with sets matching the original per-chunk roll rates; restore unstable-ant block placement.
- **Resolution:** FIXED (2026-08-10, Phase D close-out — every Islands D4 builder is ported: EnormousCastle K/Q + NightmareRookery D5, Robot Lab/Greenhouse/White House re-tagged Islands in C and reconciled D6a, CloudShark b1, MiniDungeon/CephadromeAltar b2, StinkyHouse/Pumpkin/Rainbow b3; unstable anthills wired via configured/placed_feature/unstable_anthill.json into island_biome.json; see phase_d_reports/phase_d_rollup.md)

### WGEN-022 — Greenhouse/RobotLab/WhiteHouse relocated Islands → Village

- **Status:** DIVERGENT
- **Original:** D4 (Islands) structures (`OreSpawnWorld.java:134-198`)
- **Port:** Village-dim structure sets 48/24, 44/22, 48/24
- **Fix:** Re-tag the three structures to `orespawn:island_biome` (or duplicate into both dims if the Village placement is desired), restoring Islands as their home.

## Crystal dimension

- **Resolution:** FIXED (2026-06-12, Phase C — greenhouse/robot_lab/white_house biome tags re-set to orespawn:island_biome and spacing normalized to 44/22 (orig D4 roll 1/100 x 1/19 = 1/1900 ≈ 44², OreSpawnWorld.java:134-177); see FIX_LOG.md and phase_c_reports/C7_worldgen.md)

### WGEN-023 — Crystal spawn-block ores: 9 of 11 types are placeholders

- **Status:** PARTIAL
- **Original:** Crystal ore pass — pool of 11 spawn blocks (Urchin, Flounder, Skate, Rotator, Peacock, Fairy, DungeonBeast, Vortex, Rat, Whale, Irukandji), 25+rand(30)/chunk Y>45
- **Port:** `OreSpawnChunkGenerator.getSpawnBlockStates:527-542` — frequencies exact, but 9/11 types emit CRYSTAL_STONE placeholders; only CRYSTAL_FAIRY and CRYSTAL_RAT are real
- **Fix:** Register the 9 missing crystal spawn-block variants (Urchin, Flounder, Skate, Rotator, Peacock, DungeonBeast, Vortex, Whale, Irukandji) with break-to-spawn behavior and substitute them into `getSpawnBlockStates`.
- **Resolution:** FIXED (2026-06-12, Phase C — 11 OreGenericEgg blocks registered (ore_urchin..ore_irukandji) with original textures/assets/loot and wired into OreSpawnChunkGenerator.getSpawnBlockStates; audit corrected: OreGenericEgg drops XP on harvest (OreGenericEgg.java func_149690_a), it never had break-to-spawn behaviour; see FIX_LOG.md and phase_c_reports/C7_worldgen.md)

### WGEN-024 — Crystal: extra kyanite/pink-tourmaline veins double-generate

- **Status:** DIVERGENT
- **Original:** n/a — Kyanite *is* CrystalStone; Pink Tourmaline/TigersEye exist only as column formations
- **Port:** `add_crystal_dim_ores.json` — injects `ore_kyanite` (6×size6, Y−32..80) and `ore_pink_tourmaline` (6×size6) as standard veins on top of the column generators
- **Fix:** Remove `add_crystal_dim_ores.json` (or the tourmaline entry at minimum) to eliminate the no-counterpart veins and tourmaline double-generation; keep kyanite only if it stays as a deliberate "Phase 10" addition — document it.
- **Resolution:** FIXED (2026-06-12, Phase C — ore_pink_tourmaline configured/placed features deleted; ore_kyanite initially retained as a documented exception. SUPERSEDED 2026-06-13 by owner decision PN-009 Option A: the entire invented kyanite/pink-tourmaline branch removed from the parity build, crystal_stone display names restored to the original "Kyanite" family; design archived in MODERNIZATION_NOTES MOD-009; see FIX_LOG.md)

### WGEN-025 — Crystal structures: chest loot only approximated

- **Status:** DIVERGENT
- **Original:** WeightedRandomChestContent lists per structure (FairyTree/RotatorStation/Urchin/HauntedHouse/RoundRotator/BattleTower, maze chests)
- **Port:** `CrystalStructures.fillCrystalChest:838+` — inline ItemStack pickers approximating the loot
- **Fix:** Transcribe each original weighted chest list into a data-driven loot table (`loot_table/chests/crystal_*.json`) and reference them from the structure fill code.
- **Resolution:** FIXED (2026-06-12, Phase C — the weighted chest lists transcribed into data-driven loot tables (chests/crystal_chest, crystal_chest_maze, battle_tower_rat/dungeon_beast/urchin/rotator/vortex) referenced from CrystalStructures; fixed-content chests (rotator station, urchin spawner) keep their explicit original item lists; see FIX_LOG.md and phase_c_reports/C7_worldgen.md)

### WGEN-026 — Crystal entry: empty-inventory requirement dropped (Termite portal)

- **Status:** DIVERGENT
- **Original:** `Termite.java` — travel requires empty hand **and completely empty inventory**
- **Port:** `EntityTermite.java:46-48` — inherits `EntityAnt.mobInteract:103-134` empty-hand check only
- **Fix:** Override `mobInteract` in `EntityTermite` to additionally require `player.getInventory()` be empty (the dimension's intended "bring nothing in" rule).
- **Resolution:** FIXED (2026-06-12, Phase C — EntityTermite.mobInteract now requires an empty main inventory, offhand and armor before Crystal travel, with the original "Empty your inventory!"/"Take off your armor!" messages (orig Termite.java:96-107); return trip unchecked, as in the original; see FIX_LOG.md and phase_c_reports/C7_worldgen.md)

### WGEN-027 — Crystal: redundant structure JSONs risk double generation

- **Status:** DIVERGENT
- **Original:** n/a — maze/towers placed solely by chunk-generator code
- **Port:** `crystal_maze` structure_set (1/0) + `crystal_battle_tower` set (17/8) + placed_features `crystal_maze.json` (1/4), `crystal_battle_tower.json` (1/220), `crystal_tree*.json` exist **in addition to** the code paths (`CrystalStructures.java`, `CrystalMaze`)
- **Fix:** Delete the redundant structure-set/placed-feature JSONs (maze especially: every chunk via code + 1/4 feature + structure set) so each structure has exactly one placement mechanism.

## Chaos dimension

- **Resolution:** FIXED (2026-06-12, Phase C — redundant crystal_maze (spacing-1!) and crystal_battle_tower structure sets/structures/features and the dangling crystal_tree*/crystal_flowers JSONs deleted; the chunk-generator code path (OreSpawnChunkGenerator + CrystalStructures) is the single placement mechanism; see FIX_LOG.md and phase_c_reports/C7_worldgen.md)

### WGEN-028 — Chaos: nether-style terrain replaced by overworld noise

- **Status:** DIVERGENT
- **Original:** `ChunkProviderOreSpawn6` — nether-noise terrain, 128 high, stone base with nether-style caverns, scraggly trees
- **Port:** `orespawn:inland` overworld noise, style CHAOS = pass-through (`DimensionStyle.java:48-49`); no scraggly trees
- **Fix:** Point `dimension/chaos.json` at nether-like noise settings (e.g. derived from `minecraft:nether` with stone palette, height 128) and add the scraggly-tree pass for CHAOS.
- **Resolution:** FIXED (2026-06-12, Phase C — noise_settings/chaos.json replicates the nether-style 128-high terrain with the Y60-65 grass/dirt band per ChunkProviderOreSpawn6; see FIX_LOG.md and phase_c_reports/C7_worldgen.md)

### WGEN-029 — Chaos: spawn roster gaps and unverified weights

- **Status:** PARTIAL
- **Original:** `setChaosCreatures` — ~55 entries
- **Port:** `chaos_biome.json` (37 monsters + others) + `dim_chaos_locals.json` — **Bee, Cassowary, Dragonfly, Peacock, StinkBug, Ostrich, cows, Hydrolisc missing**; ghosts/vampire_butterfly added; per-entry weights UNVERIFIED
- **Fix:** Add the eight missing entity spawn entries (for ported entities); extract original weights from `setChaosCreatures` and align the JSON.
- **Resolution:** FIXED (2026-06-12, Phase C — chaos roster rebuilt to the full setChaosCreatures list (BiomeGenUtopianPlains.java:334-516): bee/cassowary/dragonfly/peacock/stink_bug/ostrich/chipmunk/cows added, alosaurus groups corrected to (1,1), invented vampire_butterfly and the weight-doubling dim_chaos_locals duplicates removed; see FIX_LOG.md and phase_c_reports/C7_worldgen.md)

### WGEN-030 — Chaos: veggie/ant features missing; challenge towers added

- **Status:** PARTIAL
- **Original:** `OreSpawnWorld.java:103-107` — butterflies/moths, veggies, ants ×2
- **Port:** generic dungeon 1/16 + `challenge_tower_king/queen` (36/18, no 1.7.10 counterpart — see WGEN-043)
- **Fix:** Add veggie-patch and ant-block features (×2 density) to `chaos_biome.json`, reusing the overworld features from WGEN-006/007.
- **Resolution:** FIXED (2026-06-12, Phase C — veggie_patch + anthill (redfreq 2) wired into chaos_biome per OreSpawnWorld.java:203-208; challenge towers moved out of Chaos to their original Islands home (WGEN-043); the invented chaos generic dungeon was already removed in the chunk-generator dispatch; see FIX_LOG.md and phase_c_reports/C7_worldgen.md)

### WGEN-031 — Chaos: sky/fog constants not compared

- **Status:** UNVERIFIED
- **Original:** `WorldProviderOreSpawn6` — custom fog/sky colors (constants not extracted)
- **Port:** `dimension_type/chaos.json` — `ambient_light: 0.3`, `has_raids: true`
- **Fix:** Unverified because the original provider's fog/sky color values were never read. Resolve by extracting `getFogColor`/sky color from `WorldProviderOreSpawn6` and configuring matching dimension special effects.

## Nether / End additions

- **Resolution:** FIXED (2026-06-12, Phase C — verified: WorldProviderOreSpawn6 overrides no sky/fog members, so vanilla overworld visuals are correct; dimension_type/chaos.json aligned (min_y 0 / height 256 / ambient_light 0.0); see FIX_LOG.md and phase_c_reports/C7_worldgen.md)

### WGEN-032 — Nether: Lavafoam ore + ruby generation absent

- **Status:** PARTIAL
- **Original:** `OreSpawnWorld.generateNether` — Lavafoam ore, ruby, nether ants, nether mosquitos
- **Port:** `add_nether_spawns.json` (spawns only) — no lavafoam/ruby nether features
- **Fix:** Add nether-targeted configured/placed features for Lavafoam and ruby veins and register via a `#minecraft:is_nether` biome modifier.
- **Resolution:** FIXED (2026-06-12, Phase C — lavafoam_nether (15+nextInt(10) size-6 veins, /3 LessOre) and ore_ruby_nether (5+nextInt(5) size-2) at Y10-117 in netherrack added via add_nether_ores modifier per OreSpawnWorld.generateNether:243-271; see FIX_LOG.md and phase_c_reports/C7_worldgen.md)

### WGEN-033 — End: Hospital and Ender Castle structures absent

- **Status:** PARTIAL
- **Original:** `OreSpawnWorld.generateEnd` — End ants, End knights/reapers, Hospital, Ender Castle
- **Port:** `add_end_spawns.json` (spawns only) — no Hospital/EnderCastle structures
- **Fix:** Port Hospital and EnderCastle as structures tagged to End biomes with sets approximating original densities (see also WGEN-042).

## Structures & dungeons

- **Resolution:** FIXED (2026-06-12, Phase C — End spawns verified present (add_end_spawns); Hospital and EnderCastle were WGEN-042 owners. CLOSED 2026-08-10, D close-out: both ported D6a (is_end sets 42/21 + 10/5); see FIX_LOG.md and phase_d_reports/phase_d_rollup.md)

### WGEN-034 — Generic Dungeon: spawner pool swapped, custom loot replaced by vanilla

- **Status:** DIVERGENT
- **Original:** `GenericDungeon.makeDungeon` + lists — spawner pool of 12 (Scorpion, Alien, Cryolophosaurus, WTF?, Kyuubi, Bee, CloudShark, LurkingTerror, TerribleTerror, Rotator, Rat, DungeonBeast); custom level1-5 chest lists
- **Port:** `world/GenericDungeon.java:22-34,121-126` — pool of 11 (Alien, CaveFisher, DungeonBeast, Scorpion, EmperorScorpion, TrooperBug, CaterKiller, Molenoid, Basilisk, StinkBug, Triffid); chest = vanilla `simple_dungeon` loot
- **Fix:** Restore the original 12-mob spawner pool (for ported entities) and transcribe the level1-5 chest lists into orespawn loot tables referenced by dungeon depth/level.
- **Resolution:** FIXED (2026-06-12, Phase C — spawner pool restored to the exact nextInt(12) ladder (GenericDungeon.java:141-177: Scorpion/Alien/Cryolophosaurus/WTF?/Kyuubi/Bee/Cloud Shark/Lurking Terror/Terrible Terror/Rotator/Rat/Dungeon Beast); chest now uses chests/generic_dungeon (91-entry transcription of chestContentsList, 5+nextInt(7) rolls); see FIX_LOG.md and phase_c_reports/C7_worldgen.md)

### WGEN-035 — Ruby Dungeon: placement model and loot changed

- **Status:** DIVERGENT
- **Original:** `OreSpawnWorld.addRubyDungeon:1998-2012` — 1/15 chunk, placed at **lava contact** Y5-50, in any dim that called it; chest: CageEmpty/Ruby/Bacon/ButterCandy/full ruby tool+armor set/ThunderStaff (`RubyBirdDungeon.java`)
- **Port:** `OreSpawnChunkGenerator:717-726`, `GenericDungeon.tryPlaceRubyDungeon:69-98` — Crystal dim only, fixed Y10-19 band; chest = vanilla `simple_dungeon`
- **Fix:** Restore lava-adjacent placement at Y5-50 and re-enable for overworld/Utopia callers; create a ruby-gear loot table (cage, ruby, bacon, butter candy, ruby tools+armor, thunder staff) and use it.
- **Resolution:** FIXED (2026-06-12, Phase C — placement restored to the original Utopia caller: 1/15 gate, 8 lava-seek attempts Y50→6 (OreSpawnWorld.addRubyDungeon:1998-2012), removed from Crystal; chest uses chests/ruby_dungeon (cage/ruby/bacon/butter candy/full ruby kit/thunder staff, 4+nextInt(7) rolls per RubyBirdDungeon.java:18); audit note: addRubyDungeon was only ever called from Utopia (OreSpawnWorld.java:49), not the overworld; see FIX_LOG.md and phase_c_reports/C7_worldgen.md)

### WGEN-036 — DungeonSpawnerBlock: structure pool 50 → 2

- **Status:** PARTIAL
- **Original:** `DungeonSpawnerBlock.java` — on tick spawns 1 of **50** structures (FairyTree → RedAntHangout list)
- **Port:** `RandomDungeonSpawnerBlockEntity.java:63-72` — 2 outcomes (ruby 1/4 else generic)
- **Fix:** Same root as ITEM-020 — expand the outcome table as structures land (WGEN-021/042); restore the 400t timer.
- **Resolution:** FIXED (2026-06-12, Phase C — the 400-tick timer is already faithful (RandomDungeonSpawnerBlockEntity TOTAL_DELAY=400 vs orig DungeonSpawnerBlock.java:39); expanding the 2-outcome table back to 50 was blocked on the Phase D structure ports. CLOSED 2026-08-10, D close-out: all 50 outcomes wired (ITEM-020); see FIX_LOG.md and phase_c_reports/C7_worldgen.md)

### WGEN-037 — BasiliskMaze absent

- **Status:** MISSING
- **Original:** `BasiliskMaze.java` — maze + Basilisk spawner + chest (diamond 15-25, gold 4-16, iron 2-20, CagedGirlfriend, uranium, titanium, fish, corn dog), Mining dim
- **Port:** absent — no code or JSON match
- **Fix:** Port BasiliskMaze as a mining_biome structure (legacy-piece or code path) with its spawner and a loot table transcribing the listed chest contents.
- **Resolution:** FIXED (2026-08-08, Phase D5 — full line-by-line port as `LegacyDungeonStructure` type BASILISK_MAZE (`BasiliskMazeGenerator`, spec `phase_d_reports/d5_extraction/basilisk_maze_spec.md`): randomized-Prim maze + castle + antechamber + pyramid/shaft entrance per orig BasiliskMaze.java:30-458; 3 persistent Basilisks (no spawner blocks); 31-entry chest list transcribed to `chests/basilisk_maze` incl. CagedGirlfriend via caged_mob+caged_entity component; mining_biome set 26/13 (the 1/95 x 1/7 rotation odds, WGEN-039 equivalence) with the original lowest-of-36-columns >Y40 -2 ground scan in findGenerationPoint; DungeonSpawnerBlock outcome 23 wired; see FIX_LOG.md and phase_d_reports/D5_structures_spawnores.md)

### WGEN-038 — NightmareDungeon absent

- **Status:** MISSING
- **Original:** `NightmareDungeon.java` — 25×12×25, RTPBlock floor, EmperorScorpion-or-Nightmare spawner, 2 chests of Ultimate/Experience/Amethyst gear + Bertha/Slice; reached via RTP mechanic
- **Port:** absent — only the `NightmareSword.java` item exists
- **Fix:** Port NightmareDungeon generation (triggered from the RTP teleport target, which also requires the ITEM-013 RTPBlock fix) with its spawner and gear-chest loot tables.
- **Resolution:** VERIFIED-CORRECT (2026-08-08, Phase D5 — the audit's premise is wrong: `NightmareDungeon` is DEAD CODE in 1.7.10, never instantiated anywhere in the tree (exhaustive-search proof in `phase_d_reports/d5_extraction/nightmare_spec.md` section 1; the class's only reference is its own declaration, and no RTP pathway builds dungeons). Generating it would invent behavior. The Nightmare structure that actually generated - `GenericDungeon.makeNightmareRookery` - is ported under WGEN-042 in this slice; see FIX_LOG.md)

### WGEN-039 — Shadow/AlienWTF/LeonNest: frequency equivalence unverified

- **Status:** UNVERIFIED
- **Original:** Mining-dim per-chunk roll placements (`addShadowDungeon` etc., exact roll values not extracted)
- **Port:** structure sets `shadow_dungeon`/`wtf_alien_dungeon`/`leonopteryx_nest`, each spacing 26 / separation 13
- **Fix:** Unverified because the original per-chunk roll odds were never extracted. Resolve by reading the three `add`* methods' roll constants and converting to equivalent random_spread spacing.
- **Resolution:** FIXED (2026-06-12, Phase C — verified the Mining rotation odds: recently_placed==0 && nextInt(95)==1 then nextInt(7) (OreSpawnWorld.java:79-101) → 1/665 per structure → spacing 26 (26²=676); sets corrected from 32/16 to 26/13; see FIX_LOG.md and phase_c_reports/C7_worldgen.md)

### WGEN-040 — BeeHive: relocated Mining → overworld forests

- **Status:** DIVERGENT
- **Original:** `OreSpawnWorld.addBeeHive:2031-2057` — Mining dim, lowest-grass-spot algorithm
- **Port:** `structure_set/beehive.json` + `BeehiveFeature.java` — overworld `#is_forest`/`is_jungle`, set 24/12 + feature 1/60
- **Fix:** Re-tag beehive placement to `orespawn:mining_biome` (optionally keeping forests too, documented), and consider the lowest-grass-spot site selection for fidelity.
- **Resolution:** FIXED (2026-06-12, Phase C — beehive structure re-tagged to orespawn:mining_biome at 26/13 (Mining rotation slot i==2, same 1/665 math as WGEN-039); the overworld forest skep remains as the separate small_beehive structure (addANest 50/50 branch, OreSpawnWorld.java:999-1021); see FIX_LOG.md and phase_c_reports/C7_worldgen.md)

### WGEN-041 — MantisNest: placement basis unverified

- **Status:** UNVERIFIED
- **Original:** placed via dungeon spawner / overworld hooks — exact original placement basis not extracted
- **Port:** overworld forests/jungles, set 24/12 + feature 1/80
- **Fix:** Unverified because the original placement call sites/odds were not pinned down. Resolve by locating MantisNest placement in `OreSpawnWorld`/`DungeonSpawnerBlock` and comparing rates/biomes.
- **Resolution:** VERIFIED-CORRECT (2026-06-12, Phase C — orig placement found: addANest (OreSpawnWorld.java:999-1021), 1/230 gate in Forest/ForestHills/Jungle/JungleHills/Birch biomes, 50/50 mantis hive vs small beehive → 1/460 each ≈ spacing 21 (21²=441); the port set is already 21/10 on #is_forest+#is_jungle (tag also covers dark/flower forest — noted in report); see FIX_LOG.md and phase_c_reports/C7_worldgen.md)

### WGEN-042 — ~25+ structure types absent (systemic)

- **Status:** MISSING
- **Original:** placed by OreSpawnWorld/D4 hooks & DungeonSpawnerBlock (loot lists in `GenericDungeon.java`): D4Castle, EnderCastle, IncaPyramid, Mini, CephadromeAltar, NightmareRookery, StinkyHouse, Rainbow, CloudShark dungeon, Pumpkin, BouncyCastle, MonsterIsland, GirlfriendIsland, PlayPool, WaterDragonLair, GoldFishBowl, Graveyard, SpitBugLair, Igloo, KyuubiDungeon, EnderKnightDungeon, Hospital, DamselInDistress, SpiderHangout, RedAntHangout, FrogPond, RubberDuckyPond, QueenAltar(D4), EnormousCastle(Q)
- **Port:** absent — only 17 structure JSONs + 2 dungeon code paths exist
- **Fix:** Port these builders incrementally (legacy-piece transcription like the royal altars), prioritizing those wired to gameplay (KyuubiDungeon/EnderKnightDungeon for Mining, Hospital/EnderCastle for End, D4 set for Islands); register each into the DungeonSpawnerBlock pool (ITEM-020/WGEN-036) as it lands.
- **Resolution:** FIXED (2026-08-08, Phase D5 — NightmareRookery ported (`NightmareRookeryGenerator` per orig GenericDungeon.java:5242-5312 + addD4NightmareRookery OSW:2253-2274, island_biome set 44/22 = the 1/100 x 1/19 D4 roll, DSB outcome 38) and the already-ported EnormousCastle/Q ('Challenge Towers') reconciled interior-and-placement to the originals (WGEN-051..056, ITEM-066). D5 also produced `phase_d_reports/structure_conversion_pattern.md` — the D6 playbook — and the full Islands i=nextInt(19) dispatch table (`enormous_castle_spec.md` section 12.3). 2026-08-08, Phase D6a — the six strong-model items landed: EnderCastle (GD:3207-3623 + End placement END_SURFACE/is_end + Islands i==7, DSB 27), IncaPyramid (GD:3735-4042, write-set model for ramp self-reads, DSB 29), KyuubiDungeon (GD:1095-1361, Mining rotation i==1, DSB 7), EnderDragonHospital (GD:2815-2991, 4 End Crystals — no dragon, End-only, DSB 24), MonsterIsland (GD:5170-5240, overworld ocean OCEAN_SURFACE, DSB 37), Robot Lab annex reconciliation (WGEN-058..061), FairyTree/FairyCastleTree DSB 0/1 + LessLag shrinks (+WGEN-062). D6b batches 1-2 (2026-08-08): PlayPool, CloudSharkDungeon, GoldFishBowl, SpitBugLair, UrchinSpawner gate, RotatorStation DSB, Igloo (builder+DSB; placement NEEDS_DESIGN_RULING, igloo_spec.md §7.3), EnderReaperGraveyard, WaterDragonLair, LeafMonsterDungeon, MiniDungeon, CephadromeAltar. D6b batch 3 (2026-08-10): BouncyCastle (GD:3106-3205, desert SAND_SURFACE_MINUS1, DSB 26), DamselInDistress (GD:3625-3733, Village dim VILLAGE_GRASS_SURFACE + direct Girlfriend spawn, DSB 28), GirlfriendIsland (GD:4962-5028, MonsterIsland twin, DSB 35), StinkyHouse (GD:5314-5381, Islands i==15, DSB 39), Pumpkin (GD:6041-6182, Islands i==17 ISLANDS_GRASS_AIR, DSB 44), Rainbow (GD:6260-6393, Islands i==18 SKY_BAND_70, DSB 46). D6b batch 4 (2026-08-10): SpiderHangout (GD:6989-7043, Village dim, DSB 48), RedAntHangout (GD:7045-7069, Village dim, DSB 49), FrogPond (GD:6018-6039, plains, DSB 43), RubberDuckyPond (GD:5383-5421, plains, DSB 40), plus the two sweep-surfaced unported builders HauntedHouse (GD:891-1010, overworld, DSB 5) and EnderKnightDungeon (GD:1794-1932, End+Mining dual via LOWEST_GRASS_36 override, DSB 11). Igloo worldgen placement carved out to WGEN-071 (Phase E) at the D close-out — every other structure is ported and verified; see FIX_LOG.md and phase_d_reports/phase_d_rollup.md)

### WGEN-043 — Challenge Towers: no 1.7.10 counterpart found

- **Status:** UNVERIFIED
- **Original:** none found in 1.7.10 source (possibly 1.12.2-era content)
- **Port:** `challenge_tower_king/queen` structures, chaos_biome sets 36/18 (KING_TOWER/QUEEN_TOWER 40,4,95)
- **Fix:** Unverified provenance. Resolve by checking 1.12.2 OreSpawn sources (or port docs) for the towers; if intentional new content, document; if not, remove from chaos_biome.

## Trees

- **Resolution:** FIXED (2026-06-12, Phase C — audit corrected: the towers ARE 1.7.10 content (GenericDungeon.makeEnormousCastle:191 / makeEnormousCastleQ:6393, placed by addD4Castle OreSpawnWorld.java:2203-2228 in the Islands dimension); biome tags moved chaos_biome → island_biome; the existing 36/18 spacing matches the 1/100 x 3/19 x 1/2 = 1/1267 per-tower odds; see FIX_LOG.md and phase_c_reports/C7_worldgen.md)

### WGEN-044 — DuplicatorTree generator absent

- **Status:** MISSING
- **Original:** `Trees.DuplicatorTree` — sapling/worldgen tree generator
- **Port:** absent — no feature or code (BlockDuplicatorLog re-interprets behavior, ITEM-027)
- **Fix:** Port `Trees.DuplicatorTree` as a `Feature`/TreeGrower wired to the duplicator sapling and the log's random tick.
- **Resolution:** FIXED (2026-08-08, Phase D6a — audit corrected in part: `BlockDuplicatorLog` was ALREADY faithful to Trees.DuplicatorTree line-by-line (one-write-per-tick trunk/cap/ring growth, 20×20 duplication with whole-BlockState copies subsuming orig block+meta T:171-178, config gate) — the finding's 'log re-interprets behavior' premise was wrong. The genuinely missing half was the WORLDGEN seed: `addVeggies` plants a lone Duplicator Log on a 1-in-N crop roll (orig OreSpawnWorld.java:1915-1916), which the port's VeggiePatchFeature stubbed out; the what==5 branch now places the seed log (roll drawn unconditionally, DUPLICATOR_TREE_ENABLE gate). Spec `phase_d_reports/d6_extraction/trees_spec.md`; see FIX_LOG.md)

### WGEN-045 — ExperienceTree generator absent

- **Status:** MISSING
- **Original:** `Trees.ExperienceTree`
- **Port:** absent — no feature/code (EXPERIENCE_SAPLING exists but uses a different/placeholder grower per file 06)
- **Fix:** Port `Trees.ExperienceTree` geometry as the grower for the experience sapling and any worldgen placement it had.
- **Resolution:** FIXED (2026-08-08, Phase D6a — `BlockExperiencePlant`'s self-declared placeholder grower replaced with the faithful Trees.ExperienceTree port (soil gate T:298-301, 2×2 oak trunk y+1..5 / y+7..18, crown 5+nextInt(6), makeLeaves 7×7×3 air-only T:184-194, growBranch 5-segment rolls T:245-292, growSmallBranch T:196-243); the trigger (nextInt(10)==1 growth tick, build at y−1) was already faithful. Live-tick context, so the original's world reads are legal and preserved. See FIX_LOG.md)

### WGEN-046 — SmallTree / ScragglyTreeWithBranches (overworld variants) absent

- **Status:** PARTIAL
- **Original:** `Trees.java` — overworld SmallTree and ScragglyTreeWithBranches variants
- **Port:** only Islands/Crystal scraggly variants exist
- **Fix:** Port the two overworld variants from `Trees.java` and wire them to their original overworld decoration call sites.
- **Resolution:** VERIFIED-CORRECT (2026-06-12, Phase C — audit corrected: 1.7.10 has NO overworld decoration call sites for these trees — Trees.ScragglyTreeWithBranches has no callers at all (dead code) and Trees.SmallTree is only invoked by the IslandToo entity (IslandToo.java:196/:419); the port matching the Islands/Crystal/Chaos chunk-provider variants only is correct; IslandToo planting behaviour → Phase D entity-behaviour owner; see FIX_LOG.md and phase_c_reports/C7_worldgen.md)

### WGEN-047 — Utopia tree frequencies not verified (Sky/Wind/Round/MagicApple)

- **Status:** UNVERIFIED
- **Original:** `OreSpawnWorld.addOtherTrees` (:2508+), `addHugeTree` (:1830-1863), `addAppleTrees` (:1792) — per-chunk roll values not extracted
- **Port:** `sky_tree.json`/`wind_tree.json` rarity 60, `round_tree.json` rarity 333, `magic_apple_tree.json` rarity 25 (utopia_plains)
- **Fix:** Unverified because original roll constants were never extracted. Resolve by reading the three methods' roll values and converting to equivalent rarity-filter values.

## Portals & teleporters

- **Resolution:** VERIFIED-CORRECT (2026-06-12, Phase C — rolls extracted and matched: addOtherTrees 1/30 x 1/2 → wind 60/count 4, sky 60/count 3 (OreSpawnWorld.java:2508-2547); addHugeTree 1/50 x 15% → round 1/333 (:1830-1874); addAppleTrees harmonic mean of 1/(15+freq) over freq 0..14 ≈ 1/21 → apple rarity 21/count 4 (:1792-1828); see FIX_LOG.md and phase_c_reports/C7_worldgen.md)

### WGEN-048 — Village/Islands unreachable in survival (rainbow/unstable ants)

- **Status:** PARTIAL
- **Original:** rainbow/unstable ants obtainable via anthill blocks placed by worldgen (`OreSpawnWorld.addAnts`)
- **Port:** portal code works (`EntityRainbowAnt.java:20` → VILLAGE, `EntityUnstableAnt.java:20` → ISLANDS) but neither ant has a natural spawn entry and their ant blocks never world-generate
- **Fix:** Restore anthill worldgen (WGEN-006) including rainbow/unstable ant blocks, or add natural spawn entries for both ants, so both dimensions are survival-reachable.
- **Resolution:** FIXED (2026-06-12, Phase C — rainbow-ant blocks now generate via the anthill feature special picker (1/redfreq then nextInt(4), OreSpawnWorld.java:1488-1499) in all anthill dimensions, and unstable-ant blocks via both the anthill picker and the Islands unstable_anthill feature (addUnstableAnts :1572-1588), so Village and Islands are survival-reachable; see FIX_LOG.md and phase_c_reports/C7_worldgen.md)

### WGEN-049 — Portal landing: tamed pets left behind

- **Status:** PARTIAL
- **Original:** `OreSpawnTeleporter.justPutMe` — scans Y1-180 for solid ground + 3 air; **teleports tamed pets too**
- **Port:** `EntityAnt.findSafeY:142-162` — top-down scan 256→min for solid + 2 air, fallback Y64; no pet co-teleport
- **Fix:** After teleporting the player, find nearby owned/tamed entities (same radius as original) and move them through the same `DimensionTransition`.
- **Resolution:** FIXED (2026-06-12, Phase C — EntityAnt.mobInteract co-teleports tamed, non-sitting pets owned by the player within the original 48x24x48 departure box through the same DimensionTransition (orig OreSpawnTeleporter.justPutMe:151-163); see FIX_LOG.md and phase_c_reports/C7_worldgen.md)

### WGEN-050 — Utopia Portal Block: no original counterpart

- **Status:** UNVERIFIED
- **Original:** `PortalBlock.java` is empty in 1.7.10 — travel was entity-based only
- **Port:** `UtopiaPortalBlock.java` — entityInside teleport, fixed y=max(min+1,64), unbreakable (`ModBlocks.java:176`)
- **Fix:** Unverified intent (new addition). Resolve by confirming with the port's design notes whether a placeable Utopia portal is wanted; if yes document it, if not remove the block or hide it from creative.

---

## Totals


| Status     | ITEM (file 06) | WGEN (file 07) | Total   |
| ---------- | -------------- | -------------- | ------- |
| DIVERGENT  | 33             | 14             | 47      |
| PARTIAL    | 19             | 17             | 36      |
| MISSING    | 7              | 11             | 18      |
| UNVERIFIED | 6              | 8              | 14      |
| **Total**  | **65**         | **50**         | **115** |


# Findings — Animations/Events, Bugs, Optimizations

Consolidated from `audit_sections/08_animations_events_gui.md` (ANIM), `audit_sections/09_bugs.md` (BUG), `audit_sections/10_optimizations.md` (OPT). Paths: original = `reference_1_7_10_source/sources/danger/orespawn/`, port = `src/main/java/danger/orespawn/` (MHLib under `src/main/java/de/dertoaster/multihitboxlib/`).

Entries: **79 total** — ANIM 20 (DIVERGENT 8 · PARTIAL 10 · MISSING 2) · BUG 32 (CRITICAL 7 · HIGH 7 · MEDIUM 9 · LOW 9; BUG-032 added 2026-06-13, Phase D3) · OPT 27 (HIGH 8 · MEDIUM 11 · LOW 8).

---

## ANIM — Animations, Events, GUI/HUD divergences

- **Resolution:** VERIFIED-CORRECT (2026-06-12, Phase C — confirmed a deliberate port addition (1.7.10 PortalBlock.java is empty; travel was entity-based); kept as a documented creative-only utility block, PARITY_NOTES.md entry added; see FIX_LOG.md and phase_c_reports/C7_worldgen.md)

### WGEN-051 — Challenge Tower: difficulty roll locked to 6 (found 2026-08-08, Phase D5)

- **Status:** DIVERGENT
- **Original:** `GenericDungeon.java:202-205` / `:6404-6407` — `level = 1 + nextInt(6); if (level <= 3 && nextInt(3) != 1) level += 3;` → P(1..3)=1/18 each, P(4..6)=5/18 each; only ~27.8% of towers are full-height level-6 prize towers.
- **Port:** a pre-plan "QA Fix (Endgame Loot Gate)" hardcoded `level = 6` in `LegacyDungeonPiece.generateChallengeTower` so every tower guaranteed the Royal loot.
- **Resolution:** FIXED (2026-08-08, Phase D5 — the original roll restored on the deterministic piece RNG; the guaranteed-prize idea archived as MODERNIZATION_NOTES MOD-012; see FIX_LOG.md)

### WGEN-052 — Challenge Tower: invented scaffolding climb columns (found 2026-08-08, Phase D5)

- **Status:** DIVERGENT
- **Original:** no ladders or climbable blocks anywhere in `makeEnormousCastle`/`Q` (verified: zero ladder references in GenericDungeon.java:191-786/6393-6987); the 1x1 bedrock holes are the only route.
- **Port:** "QA Traversal Fix" scaffolding columns under every decoration-room ceiling hole and in the decor-6 dirt shaft.
- **Resolution:** FIXED (2026-08-08, Phase D5 — both scaffolding sites removed; archived as part of MOD-012; see FIX_LOG.md)

### WGEN-053 — Challenge Tower: chest loot palettes replaced the level1-5 lists (found 2026-08-08, Phase D5)

- **Status:** DIVERGENT
- **Original:** `level1ContentsList`..`level5ContentsList` (GenericDungeon.java:57-61) — weighted lists totalling 165/235/235/255/1285, incl. the level-5 83-spawn-egg jackpot; fill `5 + nextInt(7)` stacks per chest (GD:750).
- **Port:** `fillChallengeContents` used invented 7-10-item unweighted palettes (netherite ingot, enchanted golden apple, lapis substitutions; level-5 reduced to 5 eggs).
- **Resolution:** FIXED (2026-08-08, Phase D5 — five loot tables `chests/challenge_tower_level1..5` transcribe the originals entry-for-entry (weights, stack ranges, rolls 5-11; totals verified 165/235/235/255/1285 by `tools/d5_gen_tower_loot.py`); CriminalEgg → band_p_spawn_egg per WGEN-017; see FIX_LOG.md and `phase_d_reports/d5_extraction/enormous_castle_spec.md` section 8)

### WGEN-054 — Challenge Tower: "Jumpy Bug" spawners mapped to the wrong mob (found 2026-08-08, Phase D5)

- **Status:** DIVERGENT
- **Original:** the decoration-room ladder's "Jumpy Bug" is `TrooperBug` (EntityList registration OreSpawnMain.java:3943); "Spit Bug" is a different mob (:3951).
- **Port:** `pickKingDecorMob`/`pickQueenDecorMob` returned `ENTITY_SPIT_BUG` for every Jumpy Bug slot.
- **Resolution:** FIXED (2026-08-08, Phase D5 — all four ladder sites now `ENTITY_TROOPER_BUG`; see FIX_LOG.md)

### WGEN-055 — Challenge Tower: placement diverges from addD4Castle (found 2026-08-08, Phase D5)

- **Status:** DIVERGENT
- **Original:** `OreSpawnWorld.addD4Castle` (OSW:2203-2228) — LessLag 50% gate, chunk-corner + nextInt(8) jitter, grass scan Y20→5, air-box check, one 50/50 King/Queen roll at 3/19 of the 1/100 D4 dispatch (≈1/1267 per variant); level-6 towers also scatter the buried Large Worm ring over x,z −28..+55 at y−1 (GD:362-374).
- **Port:** chunk-centre heightmap anchor with no LessLag/jitter; structure sets shipped at 44/22 although the Phase C7 WGEN-043 resolution documents the approved 36/18 = 1/1267 math; the symmetric ±40 piece bounding box silently clipped the worm ring's outer band at chunk borders.
- **Resolution:** FIXED (2026-08-08, Phase D5 — KING_TOWER/QUEEN_TOWER moved to the ISLANDS_GRASS placement mode (grass anchor + nextInt(8) jitter + LessLag gate), sets corrected to the approved 36/18, and the box extended asymmetric x −39..+57 / z −30..+57 / y −4..+85 to cover stair + skirt + worm ring; see FIX_LOG.md)

### WGEN-056 — Challenge Tower: chest facing metadata lost (found 2026-08-08, Phase D5)

- **Status:** DIVERGENT
- **Original:** `fill_chests`/`Q` stamp facing metadata 5/4/3/2 after placing each chest (GD:744/754/765/776) so all four face the room centre.
- **Port:** chests placed with the default state (facing north).
- **Resolution:** FIXED (2026-08-08, Phase D5 — facing-aware chest placement added to LegacyDungeonPiece; all four tower chests face the room centre again; see FIX_LOG.md)

### WGEN-057 — Structure-set salt collision: mantis_nest == royal_trees (found 2026-08-08, Phase D5)

- **Status:** DIVERGENT
- **Original:** n/a (port-side placement plumbing) — random_spread sets must use distinct salts or their placement grids correlate.
- **Port:** `structure_set/mantis_nest.json` and `structure_set/royal_trees.json` both used salt 84312.
- **Resolution:** FIXED (2026-08-08, Phase D5 — royal_trees re-salted to 84332 (next free value); pre-release, so no published worlds shift; see FIX_LOG.md)

### WGEN-058 — Robot Lab: invented chest palette (found 2026-08-08, Phase D6a)

- **Status:** DIVERGENT
- **Original:** `RobotContentsList` (GenericDungeon.java:37) — 23 weighted entries totalling 755 (incl. two deliberate duplicates, kits at weight 10, Ray Gun 35), fill `10 + nextInt(5)` per chest (GD:4344/4349).
- **Port:** an invented in-code 11-entry palette (falsely documented as "all weight 35") with `DROPPER`/`DISPENSER` additions, `CLOCK` in place of the repeater, and the comparator count locked to 1.
- **Resolution:** FIXED (2026-08-08, Phase D6a — palette deleted; both chests bind `chests/robot_lab.json` transcribing GD:37 entry-for-entry (23 entries, total 755, rolls 10-14) via facing-aware placeLootChest (meta 2 = NORTH, GD:4341/4346); see FIX_LOG.md)

### WGEN-059 — Robot Lab: Robo-mob spawner bindings swapped (found 2026-08-08, Phase D6a)

- **Status:** DIVERGENT
- **Original:** altar spawners = "Robo-Pounder" (= Robot2, OreSpawnMain.java:3695); treasure-room spawner = "Robo-Warrior" (= Robot4, :3711); pillar spawners = "Robo-Sniper" (= Robot5, :3719).
- **Port:** altar bound ROBOT_4 and treasure bound ROBOT_2 (swapped); pillars correct.
- **Resolution:** FIXED (2026-08-08, Phase D6a — bindings corrected with citations; see FIX_LOG.md)

### WGEN-060 — Robot Lab: build order erased the rear sniper spawners (found 2026-08-08, Phase D6a)

- **Status:** DIVERGENT
- **Original:** hangar carve FIRST (GenericDungeon.java:4084), THEN the six pillar spawners (:4085-4090).
- **Port:** pillars built before the hangar; the carve (i 10..19, j 1..3) overwrote the two rear sniper spawners with air every generation.
- **Resolution:** FIXED (2026-08-08, Phase D6a — original order restored; see FIX_LOG.md)

### WGEN-061 — Robot Lab: annex hardware divergences (found 2026-08-08, Phase D6a)

- **Status:** DIVERGENT
- **Original:** railway uses golden/powered rail (`field_150318_D`) with floor levers meta 5 unpowered (GD:4260-4293); assembly line uses quartz stairs meta 1 (WEST), white carpet, sticky piston meta 3 (SOUTH), floor lever meta 13 POWERED (GD:4295-4308); altar buttons meta 4 on the north wall (GD:4223-4258); entry = two adjacent iron doors placed north-facing with outer-jamb hinges (`ItemDoor.func_150924_a` dir 3, GD:4080-4081).
- **Port:** detector rails, wrong lever states, red carpet/wool substitutions, mis-faced piston, and doors mirrored 180° (FACING=SOUTH).
- **Resolution:** FIXED (2026-08-08, Phase D6a — all hardware restored to the original blocks/states, incl. the powered crusher lever and the NORTH-facing door pair (the last corrected by the D6a verification pass after the first fix mirrored it); see FIX_LOG.md)

### WGEN-062 — Fairy tree dispatch: scan-exhaustion return diverged (found 2026-08-08, Phase D6a)

- **Status:** DIVERGENT
- **Original:** `addFairyTree` returns TRUE when its Y 128→41 air-over-CrystalGrass scan finds no candidate (falls through to OreSpawnWorld.java:1995), so 1/5 of such chunks still suppress the termite/big-structure follow-ups with no tree placed; only the explicit 17×17/5×5 clearance failures (:1977/:1984) return false.
- **Port:** `CrystalStructures.tryPlaceFairyTree` returned false on scan exhaustion, letting follow-ups proceed.
- **Resolution:** FIXED (2026-08-08, Phase D6a — original return contract restored with citations; pattern-doc addendum updated ("port the FULL return contract"); see FIX_LOG.md. D6b batch-1 verification then caught a side effect of that restore — the suppress path was also arming the 50-chunk cooldown, which the original arms ONLY on the build path (OSW:1992) — fixed with a tri-state result (BUILT/SUPPRESS/NONE) in CrystalStructures)

### WGEN-063 — Greenhouse: plant table silently drifted (found 2026-08-10, D6b batch 4)

- **Status:** DIVERGENT
- **Original:** `makeGreenhouseDungeon` plant roll t==7 places reeds/sugar cane (GenericDungeon.java:5090-5092, field_150436_aH) and t==19 places MyRicePlant (:5123-5125); only t==8 rolls nothing.
- **Port:** case 7 returned PUMPKIN and case 19 fell to air under a Javadoc claiming "indices 8 and 19 are intentional gaps".
- **Resolution:** FIXED (2026-08-10, D6b batch 4 — case 7 → SUGAR_CANE, case 19 → ModBlocks.RICE_PLANT, Javadoc corrected; affects worldgen GREENHOUSE + DSB type 36; caught by the batch-4 cross-cutting verifier; see FIX_LOG.md)

### WGEN-064 — DisableOverworldDungeons config defined but never read (found 2026-08-10, D6b batch 4)

- **Status:** MISSING
- **Original:** `DisableOverworldDungeons == 0` gates the ENTIRE overworld dungeon dispatch — the 6-way rotation and the ahh fall-through chain (OreSpawnWorld.java:284-321).
- **Port:** `OreSpawnConfig.DISABLE_OVERWORLD_DUNGEONS` existed (OreSpawnConfig.java:131/281) but no code read it.
- **Resolution:** FIXED (2026-08-10, D close-out — worldgen-only gate in LegacyDungeonStructure.findGenerationPoint over the 11 wired overworld types (PLAY_POOL, WATER_DRAGON_LAIR, GOLD_FISH_BOWL, GIRLFRIEND_ISLAND, MONSTER_ISLAND, FROG_POND, HAUNTED_HOUSE, LEAF_MONSTER_DUNGEON, SPIT_BUG_LAIR, BOUNCY_CASTLE, RUBBER_DUCKY_POND); the DSB path stays ungated like the original; a future Igloo placement (WGEN-071) must honor it; see FIX_LOG.md)

### WGEN-065 — Royal altars: bounding box clipped skirt + air clear (found 2026-08-10, D6b batch 4, sweep flag F3)

- **Status:** DIVERGENT
- **Original:** the v=1..9 dirt skirt writes to origin−9 (GenericDungeon.java:4377-4382/5721-5726) and the j<=height+10 air clear to origin+58 (:4364/5708).
- **Port:** `KING_ALTAR/QUEEN_ALTAR(32, 4, 56)` — down 4 / up 56 dropped those writes in BOTH worldgen and buildNow.
- **Resolution:** FIXED (2026-08-10, D6b batch 4 — box widened to (32, 10, 59); piece RNG seeds from the box, so altar layouts reseed for existing seeds (pre-release, documented delta); see FIX_LOG.md)

### WGEN-066 — Alien WTF: box clipped the south Part room's far wall (found 2026-08-10, D6b batch 4, sweep flag F8)

- **Status:** DIVERGENT
- **Original:** the south Part room writes to z = origin−21 (GenericDungeon.java:1674 → makeAlienPart at sz−7 spanning 15).
- **Port:** symmetric ±20 box — the far Z wall plane was ALWAYS dropped on the buildNow path and on ~1/16 worldgen chunk alignments.
- **Resolution:** FIXED (2026-08-10, D6b batch 4 — box widened to the asymmetric (-20, 20, 25, 6, -22, 20); footprint re-derived X −19..+17, Z −21..+15 by the cross-cutting verifier; RNG-reseed delta documented; see FIX_LOG.md)

### WGEN-067 — Greenhouse: entry doors diverged (found 2026-08-10, D6b batch 4, sweep flag F1)

- **Status:** DIVERGENT
- **Original:** two full iron doors at width/2 and width/2−1 (ItemDoor dir=3 = NORTH), two stone lintels, two meta-4 stone buttons (GenericDungeon.java:5138-5147) — the same entry pattern as the Robot Lab hangar (GD:4076-4083).
- **Port:** a single hinge-only door column at the WRONG x (ox + length/2) with no second door, lintels, or buttons.
- **Resolution:** FIXED (2026-08-10, D close-out — full pattern transcribed with the D6a-verified robot-lab door trace (east leaf HINGE=LEFT, west leaf HINGE=RIGHT, FACING=NORTH); see FIX_LOG.md)

### WGEN-068 — White House: half a door and a mis-hung button (found 2026-08-10, D6b batch 4, sweep flag F2)

- **Status:** DIVERGENT
- **Original:** full 2-tall iron door via ItemDoor dir=3 + meta-4 button (GenericDungeon.java:5548-5551).
- **Port:** only the LOWER door half; button FACING=SOUTH, which attaches it to the wrong block (meta 4 = north per the robot-lab trace).
- **Resolution:** FIXED (2026-08-10, D close-out — upper half restored, button re-hung NORTH; see FIX_LOG.md)

### WGEN-069 — CrystalBattleTowerFeature: invented and dead (found 2026-08-10, D6b batch 4, sweep flag F4)

- **Status:** DIVERGENT
- **Original:** none — the faithful CrystalBattleTower port lives in CrystalStructures (GD:4831-4959 → CS builder); the Feature's loot was invented and no datapack JSON referenced it.
- **Resolution:** FIXED (2026-08-10, D6b batch 4 — class deleted + ModFeatures registration removed under the no-procedural-fabrication rule; DSB type 33 wired to the faithful CrystalStructures.buildCrystalBattleTowerAt adapter; see FIX_LOG.md)

### WGEN-070 — CrystalMazeFeature: dead duplicate registration (found 2026-08-10, D close-out, sweep flag F5)

- **Status:** DIVERGENT
- **Original:** CrystalMaze.java buildCrystalMaze (called per Crystal chunk at Y=25, ChunkProviderOreSpawn5.java:213-214) — REAL original code, so the F4 deletion rule does not apply.
- **Port:** the live faithful path is world/CrystalMaze via OreSpawnChunkGenerator.java:177 (WGEN-027 resolution made it the single placement mechanism); the parallel CrystalMazeFeature registration is datapack-orphaned AND divergent (stamps outer boundary walls, skips openCrystalMaze's perimeter carve, bedrock ordering differs).
- **Fix:** retire the Feature class + registration, or reconcile it to the original and re-wire — either way ONE mechanism should remain. Phase E owner (audit cleanup).
- **Resolution:** FIXED (2026-08-11, Phase E3 — the dead CrystalMazeFeature retired: class deleted, ModFeatures registration replaced with a tombstone comment. The live faithful path remains world/CrystalMaze via OreSpawnChunkGenerator (WGEN-027's single placement mechanism, orig ChunkProviderOreSpawn5.java:213-214); the retired Feature was datapack-orphaned (zero JSON references) AND divergent (stamped outer boundary walls, skipped openCrystalMaze's perimeter carve, different bedrock ordering). ONE mechanism remains, per the finding's requirement)

### WGEN-071 — Igloo: worldgen placement undecided (carved from WGEN-042 at the D close-out)

- **Status:** MISSING
- **Original:** addIgloo generates on snow-biome borders inside the overworld ahh chain (OreSpawnWorld.java:304-321 dispatch; scan per igloo_spec.md §7.3).
- **Port:** builder + DSB type 20 shipped (D6b batch 2); worldgen placement deliberately unwired — the border-biome/frequency mapping has no clean biome-tag equivalent (NEEDS_DESIGN_RULING, igloo_spec.md §7.3).
- **Fix:** decide the border mapping + frequency and add the JSON pair; the placement must honor the DisableOverworldDungeons gate (WGEN-064). Phase E owner.
- **Resolution:** FIXED (2026-08-11, Phase E3 — the section-7.3 border-mapping decision ruled and implemented as a DOUBLE MECHANICAL GATE with no invented frequency: (1) biome tag has_structure/igloo = minecraft:snowy_plains ONLY (the exact-name mapping of the orig's "Ice Plains" corner gate, which EXCLUDED Ice Plains Spikes); (2) the piece re-verifies the TRUE surface at generation time — air over a full minecraft:snow_block (orig OreSpawnWorld.java:1270-1272), trying the 4 jittered attempt columns and silently no-op'ing if none passes. Snow-BLOCK surfaces inside snowy_plains occur essentially only where ice_spikes floor noise bleeds across the border, so the original's border-artifact rarity emerges mechanically. New placement mode SNOW_SURFACE_MINUS2 (4 attempts chunk+nextInt(16), accept 41<=firstFree<=100, anchor firstFree-2, per igloo_spec section 7.2); structure_set spacing 15/separation 7/salt 84356 (section 8 C7-sqrt numbers, gate odds 1/220); IGLOO added to OVERWORLD_DUNGEON_TYPES honoring DisableOverworldDungeons (WGEN-064) — worldgen-only, DSB buildNow ungated per spec section 9. Documented mechanical deltas (all in Javadoc): relocation to the passing attempt column (box widened to -22..+23), snow LAYER admissible in the air cell only (cross-pass stability against the anchor chunk's later freeze_top_layer; cannot admit a strictly-rejected column), fixed-Y retry checks (stable against igloo self-writes), retries re-drawn from the piece RNG. Section 1.3 caveat retained: the 1.7.10-vanilla snow-block terrain claim is unverified against 1.7.10 vanilla itself. See FIX_LOG Phase E)

### ITEM-067 — DSB Robot Lab outcome built shifted from the clicked pos (found 2026-08-10, D6b batch 4, sweep flag F7)

- **Status:** DIVERGENT
- **Original:** makeRobotLab is corner-anchored at the passed position (GenericDungeon.java:4053-4059).
- **Port:** generateRobotLab recentres (ox = x−5, oz = z−25), and the DSB case passed the clicked pos raw — the live build landed (−5, 0, −25) off.
- **Resolution:** FIXED (2026-08-10, D6b batch 4 — case pre-offsets pos.offset(5, 0, 25), the same recentring-cancel treatment as the batch's King/Queen altar, Greenhouse, and White House cases; see FIX_LOG.md)

### ITEM-068 — Bee/Mantis feature chests dropped their facings (found 2026-08-10, D6b batch 4)

- **Status:** DIVERGENT
- **Original:** every chest in fill_beehive_chests (GD:860-889), fill_mantishive_chests (GD:1064-1093), and the SmallBeeHive chamber (GD:1446) carries meta 2-5 (inward-facing).
- **Port:** all placed default (north).
- **Resolution:** FIXED (2026-08-10, D close-out — facings restored per the metas (Beehive/Mantis: E/W/S/N inward ring; SmallBeehive: EAST); see FIX_LOG.md)

### ITEM-069 — Bee/Mantis loot substituted invented items for the egg entries (found 2026-08-10, D6b batch 4)

- **Status:** DIVERGENT
- **Original:** beeContentsList carries BeeEgg 2-8 weight 15 (GenericDungeon.java:55); mantisContentsList carries MantisEgg 2-4 weight 20 (:56).
- **Port:** GOLDEN_CARROT / SPIDER_EYE stand-ins justified by a false "no equivalent item exists" premise — ModItems registers BEE_SPAWN_EGG and MANTIS_SPAWN_EGG.
- **Resolution:** FIXED (2026-08-10, D close-out — spawn eggs restored at the original stacks/weights per the repo egg-item convention (stinky_house/rubber_ducky_pond/water_dragon_lair precedents); the in-code fills now agree with the shipped chests/beehive.json; see FIX_LOG.md)



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
- **Resolution:** FIXED (2026-06-12, Phase C — audit corrected: orig `RenderInfo` was never datawatcher-synced; it is a per-entity client scratch object (orig Kraken.java:58, RenderInfo.java:6-15) mutated by the model (orig ModelKraken.java:1045-1057). Port now attaches a per-entity `RenderInfo` to `Kraken` and drives ri1/ri2 from it, restoring independent per-Kraken twitch state; see FIX_LOG.md and phase_c_reports/C8_animations_gui.md)

### ANIM-003 — Rotator: 24-blade tri-axis gyroscope reduced to 3 flat Z-spinning blades

- **Status:** DIVERGENT
- **Original:** `ModelRotator.java:44-80` — each of 3 blade shapes rendered **8×** in a fan; fans spun on X, Y and Z axes via accumulating `ri.rf1 += 2°`.
- **Port:** `entity/client/RotatorModel.java:33-45` — each shape rendered once, all three spun around **Z only** at 1×/1.5×/2×; the signature 24-blade ball is gone.
- **Fix:** In `renderToBuffer`, render each blade part 8 times with 45° pose offsets, and assign the three fans X/Y/Z rotation axes with an accumulating angle field (2°/frame) as in the original.
- **Resolution:** FIXED (2026-06-12, Phase C — RotatorModel rewritten: each blade rendered 8× at 45° steps, fans spun on X/Y/Z by a per-entity accumulating rf1 (+2°/frame, wrap 359°, orig ModelRotator.java:44-80); see FIX_LOG.md and phase_c_reports/C8_animations_gui.md)

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
- **Resolution:** FIXED (2026-06-12, Phase C — full walk cycle (hip sway/bob, two-phase thigh/shin), two-pass shared-part leg/arm rendering, and getAttacking()-gated punch windmill restored per orig ModelGiantRobot.java:150-279; pose values recomputed per frame so no cross-frame state holder is needed (see ANIM-014); see FIX_LOG.md and phase_c_reports/C8_animations_gui.md)

### ANIM-006 — SpiderRobot: only 1 of 8 legs renders

- **Status:** DIVERGENT
- **Original:** `ModelSpiderRobot.java:302-411` — 8 legs posed **and rendered inside** the loop (renders at `:392-410`); jaw snap at `:412-427`.
- **Port:** `entity/client/ModelSpiderRobot.java:259-352` — keeps the 8-iteration pose loop but never renders inside it; `renderToBuffer` (`:372+`) draws once after the loop, so only leg i=7 is visible. Jaw snap ported (`:353-368`); gait simplified to a canned sine (`SpiderRobot.java:221-237`).
- **Fix:** Move the leg `render` call inside the 8-iteration pose loop (pose leg i, render leg i) as the original does, or maintain 8 distinct leg `ModelPart`s posed per index.
- **Resolution:** FIXED (2026-07-02, Phase D2 — the C8 remainder (canned-sine gait) closed: the original client-side leg solver ported line-by-line into `SpiderRobot` (`initLegData`/`getNewVelocity`/`updateLegs`/`findNewFooting`, orig SpiderRobot.java:111-486) and `AntRobot` (ant constants, orig AntRobot.java:156-510), `RenderSpiderRobotInfo` expanded to the full original field set; the C8 per-leg render loop already consumed these fields, so the models now draw the real inverse-kinematics walk; see FIX_LOG.md and phase_d_reports/D2_gait_elevator.md)

### ANIM-007 — Robot2: attack-gated arm poses replaced by constant windmill

- **Status:** PARTIAL
- **Original:** `ModelRobot2.java:133-153` — walk legs + attack-gated random arm poses via `getAttacking()`/`RenderInfo.ri1`.
- **Port:** `entity/client/ModelRobot2.java:129-148` — walk ported; arms windmill constantly at 20°/tick regardless of attack state.
- **Fix:** Gate the arm windmill behind the entity's `getAttacking()` synched accessor; pose arms at a resting angle when idle.
- **Resolution:** FIXED (2026-06-12, Phase C — per-entity RenderInfo.ri1 re-rolled at sine zero crossings, arms windmill only while getAttacking()!=0 with random arm selection 1..3 (orig ModelRobot2.java:139-170); legs corrected to time-driven frequency; see FIX_LOG.md and phase_c_reports/C8_animations_gui.md)

### ANIM-008 — Robot3: attack driver dropped (shares Robot2 pattern)

- **Status:** PARTIAL
- **Original:** `ModelRobot3.java` — `getAttacking()`-gated arm animation (same pattern as Robot2).
- **Port:** `entity/client/ModelRobot3.java` — no `getAttacking()` use (verified by absence in grep of attack-driver coverage, 08 §"Attack-driver coverage").
- **Fix:** Same as ANIM-007: read `getAttacking()` and gate the attack arm pose on it.
- **Resolution:** FIXED (2026-06-12, Phase C — ri1 latched at cosine zero crossings from getAttacking(), arm swing zeroed when idle (orig ModelRobot3.java:169-186); see FIX_LOG.md and phase_c_reports/C8_animations_gui.md)

### ANIM-009 — Robot4: attack-gated shield/cannon arm anims dropped

- **Status:** PARTIAL
- **Original:** `ModelRobot4.java` — walk + attack-gated shield/cannon arm animations keyed on `getAttacking()`.
- **Port:** `entity/client/ModelRobot4.java:417-459` — walk ported; right arm swings on a fixed always-on cycle, cannon arm frozen at a constant angle; no `getAttacking()` use.
- **Fix:** Restore the `getAttacking()` branch: idle pose when 0, shield raise + cannon aim cycle when attacking.
- **Resolution:** FIXED (2026-06-12, Phase C — shield arm pumps (|cos|·45°+0.75) and cannon aims (0.85 rad) only while getAttacking()!=0, resting at 0 when idle; cannon assembly follows the upper-arm pivot (orig ModelRobot4.java:439-500); shin rest offsets corrected; see FIX_LOG.md and phase_c_reports/C8_animations_gui.md)

### ANIM-010 — EntityRat: attack head pose dropped

- **Status:** PARTIAL
- **Original:** `ModelRat.java:116` — attack-vs-idle head bob.
- **Port:** `entity/client/RatModel.java:60-67` — walk + head yaw only.
- **Fix:** Read the rat's `getAttacking()` accessor in `setupAnim` and apply the original attack head-bob branch.
- **Resolution:** FIXED (2026-06-12, Phase C — audit corrected: orig ModelRat.java:116-120 animates the TAIL (yRot thrash, freq 1.5/amp 0.25π attacking vs 0.4/0.05π idle, tail2 follows tail1's tip), not the head; tail branch restored, leg phase signs fixed, unoriginal head yaw removed; see FIX_LOG.md and phase_c_reports/C8_animations_gui.md)

### ANIM-011 — Keybinds: fly-up default key changed (Left Alt → Space)

- **Status:** DIVERGENT
- **Original:** `KeyHandler.java:15-18` — one key "OreSpawn UP/FAST", LWJGL 56 = **Left Alt**.
- **Port:** `client/KeybindHandler.java:18-37, 54-62` — fly_up=**SPACE**, fly_down=LCTRL, special=G (two keys are new additions).
- **Fix:** Decide intentionally: either set fly_up default to `GLFW_KEY_LEFT_ALT` for parity, or document SPACE as a deliberate UX change (SPACE conflicts with vanilla mount-jump/dismount expectations).
- **Resolution:** FIXED (2026-06-12, Phase C — fly_up default set to Left Alt for parity with orig KeyHandler.java:15 (LWJGL 56); SPACE rejected because it collides with vanilla mount-jump/dismount; fly_down/special documented as port-only additions; see FIX_LOG.md and phase_c_reports/C8_animations_gui.md)

### ANIM-012 — Rider flight/jump controls missing for 6 of 7 original mounts

- **Status:** PARTIAL
- **Original:** 7 entities poll `flyup_keystate`: `Dragon`, `Leon`, `Cephadrome` (`Cephadrome.java:786-789`), `Ostrich` (jump/FAST, `Ostrich.java:470-474`), `Elevator`, `ThePrinceTeen`, `ThePrinceAdult`.
- **Port:** Only `Dragon` implements `RideableFlyer` (`Dragon.java:148, 344, 982`; grep `RideableFlyer` = 2 files). Port `Cephadrome.java`, `Ostrich.java`, `Leonopteryx.java` have no `travel`/`tickRidden`/`getControllingPassenger` riding control at all; Elevator/ThePrinceTeen/ThePrinceAdult likewise unhandled (`network/RiderInputPayload.java:31-51` falls back to a generic ±0.15 Δy only for `RideableFlyer`).
- **Fix:** Implement `RideableFlyer` (plus `getControllingPassenger`/`tickRidden`) on Cephadrome, Ostrich (jump/FAST semantics), Leonopteryx, Elevator, ThePrinceTeen, ThePrinceAdult so `RiderInputPayload` dispatch reaches them.
- **Resolution:** FIXED (2026-07-02, Phase D2 — 7 of 7: the Elevator remainder closed with a full port of the original hovercraft physics (orig Elevator.java:232-515) as client-predicted `tickRidden` + server-side world effects; `RideableFlyer` implemented so the fly-up key ("FAST", speed cap 0.85→1.85, orig :441-443) reaches it; 6 earlier mounts Phase B3 via `RiderFlightController`; see FIX_LOG.md and phase_d_reports/D2_gait_elevator.md)

### ANIM-013 — HUD: pointed-at-mob health bar reduced to owned-Girlfriend list

- **Status:** PARTIAL
- **Original:** `GirlfriendOverlayGui.java:75-447` — universal crosshair-target health bar (name + `girlfriendgui.png` 182×5 textured bar above hotbar) covering ~45 entity types incl. ownership-gated Girlfriend/Boyfriend, Princes, Dragon, bosses (King `:360-364`, Queen `:365-369`, Mobzilla `:335-339`), robots, big crabs; pointed-entity lookup `:105-114`; bar draw `:432-446`; config gate `:102`.
- **Port:** `client/GirlfriendOverlay.java:27-62` — top-left list of owned Girlfriends within 16 blocks only; flat-color bars; no crosshair targeting, no bosses/mounts/robots. Config gate ported (`:33`).
- **Fix:** Reimplement crosshair-entity resolution (pick entity via `Minecraft.crosshairPickEntity` or a ray trace), restore the textured 182×5 bar centered above the hotbar, and port the ~45-type eligibility list (ownership gates for Girlfriend/Boyfriend).
- **Resolution:** FIXED (2026-06-12, Phase C — GirlfriendOverlay rewritten as the crosshair-target HUD: vanilla pick entity + 16-block entity ray trace fallback (orig :105-114), full eligibility chain with ownership gates (Girlfriend/Boyfriend/Princes/Princess) and activity gates (Princes/Dragon/Cephadrome), custom-name-or-label, textured 182×5 bar from girlfriendgui.png (copied from orig assets) at y=25 (15 in water/armored) with 0xFF3434 name 10px above (orig :432-446), GUI_OVERLAY_ENABLE gate kept. Orig shoulder-Girlfriend passenger gate has no port equivalent (noted in class Javadoc); see FIX_LOG.md and phase_c_reports/C8_animations_gui.md)

### ANIM-014 — GiantRobot: `RenderGiantRobotInfo` walk-cycle state holder absent

- **Status:** MISSING
- **Original:** `ModelGiantRobot.java:154-167` — per-entity walk-cycle scratch data in `RenderGiantRobotInfo`.
- **Port:** absent — grep `RenderGiantRobotInfo` = 0 hits (consumed by the walk anim dropped in ANIM-005).
- **Fix:** Recreate `RenderGiantRobotInfo` as a per-entity data class (mirror `entity/client/RenderSpiderRobotInfo.java:3-25`) and feed it from `GiantRobot` tick as part of the ANIM-005 walk-cycle restoration.
- **Resolution:** VERIFIED-CORRECT (2026-07-02, Phase D2 — the holder carries no live cross-frame state, so recreating it would add no behavior: every `RenderGiantRobotInfo` field the model touches is written at orig ModelGiantRobot.java:162-167 and read back at :170-224 *within the same render call* (per-frame scratch, unlike the SpiderRobot's solver state); the only other write is `gpcounter = 2000000` at orig GiantRobot.java:80, which nothing ever reads (grep: no reader in ModelGiantRobot/RenderGiantRobot). The port's ANIM-005 walk cycle computes the identical formulas per frame in `ModelGiantRobot.setupAnim` (movescale clamp, hip sway/bob, two-phase thigh/shin constants 0.19634954084936207 / 0.6283185400806344 match orig :158-167 digit-for-digit), so the visible walk state already matches the original; see FIX_LOG.md and phase_d_reports/D2_gait_elevator.md)

### ANIM-015 — Crystal Furnace: cook speed 1.5× and signature crystal fuels inert

- **Status:** PARTIAL
- **Original:** `TileEntityCrystalFurnace.java:174-179` — cook = **150 ticks**; custom fuel table (`:226-277`): lava/CrystalCoal **20000**, CrystalTreeLog 800, CrystalPlanks 400, etc.
- **Port:** `gui/CrystalFurnaceBlockEntity.java:34` — cook = **100 ticks**; fuel via vanilla `fuel.getBurnTime(RecipeType.SMELTING)` (`:183`); no burn-time registration anywhere for Crystal Coal/Log/Planks (grep `getBurnTime|FurnaceFuel` = only this file), so they don't burn.
- **Fix:** Register burn times for Crystal Coal (20000), Crystal Tree Log (800), Crystal Planks (400) via `IItemExtension#getBurnTime` overrides or a `FurnaceFuelBurnTimeEvent` handler; set cook time back to 150 (or document 100 as an intended buff).
- **Resolution:** VERIFIED-CORRECT (2026-06-12, Phase C — duplicate of ITEM-016, already fixed in Phase C slice 6: cook 150 ticks and the orig fuel table (lava/CrystalCoal 20000, log 800, planks 400) live in CrystalFurnaceBlockEntity. Slice 8 additionally restored the fuel container-item remainder (lava bucket → empty bucket, orig TileEntityCrystalFurnace.java:165-170); see FIX_LOG.md and phase_c_reports/C8_animations_gui.md)

### ANIM-016 — Seasonal content: Halloween/Valentine's/Easter gates all absent

- **Status:** MISSING
- **Original:** `OreSpawnMain.java:4518-4521` — `GregorianCalendar` at init: Oct 31 → Ghost/GhostSkelly biome spawns (`:4521-4566`); Feb 14 → `valentines_day=1` (`:4567-4569`) consumed by `MyValentineTarget` AI (`Girlfriend.java:161-162`, `MyValentineTarget.java:47-50`); Apr 20 → `easter_day=1` gating EasterBunny spawns (`:4681`).
- **Port:** absent — grep `Calendar|Valentine|easter` = 0; Ghost/GhostSkelly/EasterBunny have plain config-gated spawns (`ModSpawnControl.java:57-58, 89`).
- **Fix:** Add a date check (`LocalDate.now()`) evaluated at server start/spawn-check time; gate Ghost/GhostSkelly and EasterBunny spawn rules on it and port `MyValentineTarget` (Girlfriend kiss-target goal) activated on Feb 14.
- **Resolution:** FIXED (2026-07-02, Phase D4 — seasonal gates ported and made live: `SeasonalDates` evaluates isHalloween/isValentines/isEaster from LocalDate at check time instead of the orig's once-at-init GregorianCalendar snapshot (deviation logged as PN-014). Halloween: the 22-biome Ghost/GhostSkelly block added as `halloween_ghosts.json`, runtime-gated in checkSpawnRules with the 5 year-round biomes exempt (closes ENT-D-039/041). Easter: EasterBunny spawn gate (closes ENT-D-011). Valentine's: Girlfriend 2.5x8.0 dimensions + 800 HP + girlfriendv texture + MyValentineTarget goal (players/Boyfriends while angry) + Rose Sword 1-in-4 cure with Love drops, persisted via feelingBetter NBT; see FIX_LOG.md)

### ANIM-017 — ExperienceCatcher: conversion mechanic entirely different

- **Status:** DIVERGENT
- **Original:** `ExperienceCatcher.java:29-62` — catches **one** orb (value ≥3, 80% chance) → drops Bottle o' Enchanting + string + stick; item consumed.
- **Port:** `item/ExperienceCatcher.java:24-61` — vacuums **all** orbs in r=3 → pays out emeralds/gold/diamonds by XP total.
- **Fix:** Either restore original semantics (single orb ≥3 value, 80% roll, Bottle o' Enchanting + string + stick, consume item) or sign off the redesign explicitly.
- **Resolution:** FIXED (2026-06-12, Phase C — original semantics restored: 1×2×1 click-column scan, first orb ≥3 passing the 80% roll → Bottle o' Enchanting + string + stick, catcher consumed unless creative; on miss the catcher is dropped at the click point and one removed (orig ExperienceCatcher.java:29-62); see FIX_LOG.md and phase_c_reports/C8_animations_gui.md)

### ANIM-018 — Per-mob spawn-disable flags: ~42 of ~100 enforced

- **Status:** PARTIAL
- **Original:** ~100 `XxxEnable` config flags gate `EntityRegistry.addSpawn` (grep `Enable = config.get` = 100, e.g. `OreSpawnMain.java:1519`).
- **Port:** `ModSpawnControl.java:53-101` maps **42** entity types; cancellation via `FinalizeSpawnEvent`+`EntityJoinLevelEvent` (`:109-135`). Bosses/water mobs unmapped; `KRAKEN_ENABLE` does not exist in port config at all (grep = 0).
- **Fix:** Extend the `ModSpawnControl` map to cover all ~100 original flags (add the missing config entries, incl. `KRAKEN_ENABLE`, which `KrakenRevengeHandler` should also respect).
- **Resolution:** FIXED (2026-06-12, Phase C — 56 missing config flags added (orig OreSpawnMain.java:6364-6465; BoyfriendEnable default false per orig :6430) and ~65 map entries wired in ModSpawnControl, covering bosses (Mobzilla/King/Queen/Kraken), water mobs, robots, ambients and all cow variants (CowEnable, orig :4609-4624); KrakenRevengeHandler now respects KRAKEN_ENABLE; see FIX_LOG.md and phase_c_reports/C8_animations_gui.md)

### ANIM-019 — Creeper Repellent: PurplePower target omitted

- **Status:** PARTIAL
- **Original:** `CreeperRepellent.java:94-126` — repels Creeper + EntityAnt + **PurplePower**; `KrakenRepellent.java:93-109` repels Kraken + EntityAnt.
- **Port:** `block/RepellentBlock.java:26-47` + `ModBlocks.java:131-136` — kraken predicate = Kraken‖EntityAnt ✓; creeper predicate = Creeper‖EntityAnt — PurplePower missing.
- **Fix:** Add `|| e instanceof PurplePower` to the creeper-repellent predicate in `ModBlocks.java:131-136`.
- **Resolution:** VERIFIED-CORRECT (2026-06-12, Phase C — audit stale: the ITEM-019 RepellentBlock rewrite already repels PurplePower in the creeper variant, including the orig type-10 scan-abort quirk (port block/RepellentBlock.java:109-114, orig CreeperRepellent.java:126-145); see phase_c_reports/C8_animations_gui.md)

### ANIM-020 — Dimension teleporter: 1 of 5+ destinations implemented

- **Status:** PARTIAL
- **Original:** `OreSpawnTeleporter.java:22-96` — custom placement for 5 dims (mining/crystal/chaos/village/islands + utopia).
- **Port:** only `block/UtopiaPortalBlock.java:23-50` (entityInside → utopia/back); no teleporter/portal code for the other dimensions exists.
- **Fix:** Implement portal blocks + placement logic for the remaining dimensions (mirror `UtopiaPortalBlock`), coordinating with the dimension-slice audit on which dims actually exist in the port.
- **Resolution:** VERIFIED-CORRECT (2026-06-12, Phase C — audit stale: the orig "teleporter" triggers are the rideable ants/termite/butterfly (orig EntityAnt.java:95, EntityRedAnt.java:83, EntityRainbowAnt.java:55, EntityUnstableAnt.java:55, Termite.java:108, EntityButterfly.java:276), and the port already implements all 6 destinations via EntityAnt.mobInteract + subclass getTargetDimension overrides and EntityButterfly (Chaos), with the OreSpawnTeleporter safe-landing scan (findSafeY) and tamed-pet transfer (WGEN-049). All 6 dimension JSONs exist; see phase_c_reports/C8_animations_gui.md)

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
- **Resolution:** VERIFIED-CORRECT (2026-08-11, Phase E1 — independently-verified faithful-match: orig TheKing.java:1031-1039 writeEntityToNBT persists ONLY KingHomeX/KingHomeZ/GuardMode/PlayerHits/IsEnd/EndCounter — none of the eight audit-named fields; the port persists the identical six keys (port :1358-1377). The audit's headline (buffed attackDamage forgotten) is false in BOTH versions: attdam is recomputed every tick from persisted PlayerHits + health (orig :244-255, port :466-477), and stream counters refill on the ticker within ~90 ticks. Adding persistence would diverge from 1.7.10)

### BUG-015 — `TheKing.dropCustomDeathLoot`: up to 300 random-registry item drops

- **Severity:** MEDIUM
- **Location:** `danger/orespawn/entity/TheKing.java:1321–1339`
- **Scenario:** Two `while (j < 150)` loops draw random IDs from the item/block registries (any mod's items, technical items included) — one kill dumps ~300 item entities: lag spike + exploit-grade loot.
- **Fix:** Replace registry sampling with a curated loot table (or a small whitelisted item pool) and cap total drops to a sane count (e.g. ≤32).
- **Resolution:** VERIFIED-CORRECT (2026-08-11, Phase E1 — independently-verified faithful-match: the ~300 random-registry drops ARE the original: orig TheKing.java:200-226 draws 150 uniform-random items + 150 blocks from the full registries, scattered at y+12 ±20 (dropItemRand :178-181); the port's byId(nextInt(size)) loops (:1318-1340) are the uniform-pick equivalent with AIR skips matching 1.7.10's instantly-dead null-item entities. The audit's curated-table fix would break parity. Opt-in cap archived as MOD-023)

### BUG-016 — `Godzilla`: combat state not persisted

- **Severity:** MEDIUM
- **Location:** `danger/orespawn/entity/Godzilla.java` (fields `ticker`, `streamCount`, `largeUnknownDetected`, `jumped`, `jumpTimer`, `headFound`)
- **Scenario:** Fire-stream and jump state reset on relog; a mid-air "jumped" Godzilla reloads with `jumped=false` and never runs its landing-damage path, leaving stale state.
- **Fix:** Persist `jumped`/`jumpTimer`/`streamCount` in NBT.
- **Resolution:** VERIFIED-CORRECT (2026-08-11, Phase E1 — independently-verified faithful-match: orig Godzilla.java has NO NBT overrides at all (zero func_70014_b/func_70037_a/NBTTagCompound hits in 1778 lines; extends vanilla EntityMob) — all seven fields reset on relog in 1.7.10 exactly as in the port, including the mid-air jumped reset skipping landing damage; stream_count self-refills every 100 ticks (orig :277-279, port :565))

### BUG-017 — `TheQueen.mood` not persisted — angry queen reloads happy

- **Severity:** MEDIUM
- **Location:** `danger/orespawn/entity/TheQueen.java:179` (mood logic ~707–720)
- **Scenario:** With `QUEEN_ALWAYS_MAD` off, relogging during a fight resets the Queen to placid — players can defuse aggression by relogging.
- **Fix:** Write/read `mood` in the existing NBT methods.
- **Resolution:** VERIFIED-CORRECT (2026-08-11, Phase E1 — independently-verified faithful-match: orig TheQueen.java:964-980 persists exactly KingHomeX/KingHomeZ/GuardMode/PlayerHits/MeanMode — mood was never saved, so the relog-to-placid defuse existed in 1.7.10; port :1320-1340 writes the identical five keys. MeanMode (always_mad) IS persisted in both, so QUEEN_ALWAYS_MAD queens stay mad — only transient mood resets, identically. MOD-022)

### BUG-018 — `Kraken` weather lock fights the vanilla weather cycle and isn't persisted

- **Severity:** MEDIUM
- **Location:** `danger/orespawn/entity/Kraken.java:57` (decrement), `:135` (`setWeatherParameters`)
- **Scenario:** Every `weatherSet` expiry re-forces a thunderstorm, overriding `/weather clear` and other mods; timer not saved so relog re-triggers immediately; multiple Krakens each re-arm independently.
- **Fix:** Set weather once per Kraken (persist a flag in NBT) and/or check `level.isThundering()` before forcing.
- **Resolution:** FIXED (2026-08-11, Phase E1 — MIXED verdict: the re-force loop, /weather-clear override, per-Kraken timers, and weatherSet non-persistence are ALL original (orig Kraken.java:171-185; NBT :189-197 persists only LongEnough) and stay. Two real port divergences fixed at port Kraken.java tick: duration 6000→300 (orig func_76080_g/func_76090_f(300) — storm dies ~15s after the Kraken stops re-arming) and the flag handling — orig never upgrades an existing plain rain to thunder (flags forced only in the !isRaining branch); port now mirrors both branches exactly. The missing PlayNicely gate (orig :171) belongs to BOSS-017 (E4). See FIX_LOG Phase E)

### BUG-019 — `EntityVortex`: server-side push/launch velocities on players not synced

- **Severity:** MEDIUM
- **Location:** `danger/orespawn/entity/EntityVortex.java:184–187` (pull), `:244–273` (`skywardLaunch`)
- **Scenario:** `push()`/`setDeltaMovement()` on a `ServerPlayer` without `hurtMarked = true` sends no motion packet — the signature tornado pull/launch works only erratically (piggy-backing knockback from coincident `doHurtTarget`).
- **Fix:** Set `victim.hurtMarked = true` for players after modifying `deltaMovement` in both code paths.
- **Resolution:** VERIFIED-CORRECT (2026-08-11, Phase E1 — independently-verified faithful-match: 1.7.10 func_70024_g/addVelocity never set velocityChanged, and updateAITasks ran server-only — the pull reached a player's screen ONLY as motion flushed by a same-tick damage packet (setBeenAttacked → S12), i.e. the erratic yank the audit complains about WAS the original gameplay. The port reproduces the identical delivery channel (push() + doHurtTarget→markHurt). Adding hurtMarked would create a smooth tractor pull that never existed — parity violation. skywardLaunch (:244-273) is stale: removed with ENT-S-069/070. Smooth-pull opt-in archived as MOD-024)

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
- **Resolution:** DEFERRED (2026-08-11, owner-approved — designated FIRST POST-BETA PATCH ITEM; does not block the beta). Verdict PORT-DEFECT confirmed, mechanism corrected during E1: WorldGenRegion silently drops setBlock calls beyond its 1-chunk write radius (~24 blocks); only FairyCastleTree (reach ~25-42 blocks, systematic shear) and FairyTree (<=2 blocks on max rolls, marginal) are affected — battle tower/rotator/haunted house/maze all fit. In 1.7.10 setBlockFast force-generated the target chunk so every write landed (OreSpawnMain.java:5833-5847). SHIPPED in E1 (observability): the OreSpawnChunkGenerator swallow-catch logs, CrystalStructures.safeSetBlock warns on dropped writes via ensureCanWrite, and the false no-truncation Javadoc was corrected. DEFERRED WORK (dedicated strong-model session): FairyCastleTree conversion to the LegacyDungeonStructure pipeline (royal-altar precedent, OreSpawnChunkGenerator.java:265-278) with findGenerationPoint reproducing addFairyTree (chunk-centre anchor, Y128->41 scan, 17x17 air + 5x5 CrystalGrass clearance, OSW:1968-1986, 1/5 castle-variant roll) AND re-derivation of the D5 dispatch coupling (fairy success suppresses termites/big structures and arms the 50-chunk cooldown, OSW:188-196/1992). DSB live-tick adapters unaffected. Player-facing note in KNOWN_ISSUES.md.
### BUG-022 — `EntityVortex` scans all nearby LivingEntities every tick on both sides

- **Severity:** MEDIUM
- **Location:** `danger/orespawn/entity/EntityVortex.java:101` (`tick()`), `:176` (`customServerAiStep`)
- **Scenario:** 32×20×32 `getEntitiesOfClass` + per-candidate LoS raycast every tick on server *and* client (client only needs it for smoke particles) — several vortexes measurably hit frame and tick time.
- **Fix:** Cache the target for ~10 ticks; gate the client particle check behind a cheap distance test. (Perf side covered by OPT-004.)
- **Resolution:** VERIFIED-CORRECT (2026-08-11, Phase E1 — independently-verified faithful-match: the every-tick both-sides scan is the original's own cadence: orig Vortex.java:114 (onUpdate, both sides — the :117 isRemote gate covers only particles) + :184 (updateAITasks) + :345-346 (same 32×20×32 box + sort + LoS). Caching is a behavior-affecting optimization → OPT-004 (Phase F). The two ADJACENT divergences this investigation surfaced (plain-distance sort vs GenericTargetSorter; missing PlayNicely gate + ignore-list) were real and are fixed under TF-035)

### BUG-023 — `Mothra`: movement/heal state not persisted

- **Severity:** LOW
- **Location:** `danger/orespawn/entity/Mothra.java:41–45`
- **Scenario:** `lastX/Y/Z`, `stuckCount`, `healthTicker` reset on relog; stuck-detection and regen restart. Minor hiccup only.
- **Fix:** Persist `healthTicker` if regen cadence matters; otherwise accept as-is.
- **Resolution:** VERIFIED-CORRECT (2026-08-11, Phase E1 — independently-verified faithful-match: orig Mothra.java:287-293 NBT overrides are empty super-delegates — lastX/Y/Z, stuck_count, health_ticker were never persisted in 1.7.10; port has no save overrides either, resetting identically (first post-reload heal after 100 ticks, then the 200-tick cycle). MOD-022)

### BUG-024 — `GiantRobot.reloadTicker` not persisted

- **Severity:** LOW
- **Location:** `danger/orespawn/entity/GiantRobot.java:38`
- **Scenario:** Relog during the rocket reload window lets the robot fire immediately. Balance-only.
- **Fix:** Save the ticker in NBT.
- **Resolution:** VERIFIED-CORRECT (2026-08-11, Phase E1 — independently-verified faithful-match: orig GiantRobot.java (382 lines, full read) has no NBT overrides; reload_ticker resets on relog in 1.7.10 exactly as the port's reloadTicker does (decrement/set sites match :238-240/:276/:279 vs port :106/:173/:177). MOD-022)

### BUG-025 — `Kraken.enchantToolSilk` rolls Silk Touch I–V (illegal levels)

- **Severity:** LOW
- **Location:** `danger/orespawn/entity/Kraken.java:501`
- **Scenario:** Drops can carry Silk Touch above max level 1; anvils/grindstones and validation mods treat the stack as illegal.
- **Fix:** Clamp the rolled level to `enchantment.getMaxLevel()`.
- **Resolution:** FIXED (2026-08-11, Phase E1 — superseded: the audited enchantToolSilk helper was deleted wholesale in the Phase B1 loot consolidation (drops now data-driven via kraken.json enchant_randomly, the documented PN-005 approximation). The Silk-Touch half WAS a real mistranslation — the original enchants FORTUNE I-V at 1/6 on pickaxes (orig Kraken.java:299-303 field_77346_s) and Silk Touch appears NOWHERE in the file. The audit's clamp-to-getMaxLevel fix is REJECTED: over-max levels (Unbreaking V, Fortune V, Feather Falling V-IX at :387/:535/:687/:796) are authentic 1.7.10 drops and must survive in MOD-007's exact dice implementation — recorded as a MOD-007 addendum so the mistake cannot return)

### BUG-026 — `Kraken`: `hitByPlayer`/`callReinforcements` not persisted

- **Severity:** LOW
- **Location:** `danger/orespawn/entity/Kraken.java` (fields near top)
- **Scenario:** Relogging mid-fight re-arms the reinforcement wave — a second squad can spawn.
- **Fix:** Persist both flags in NBT.
- **Resolution:** VERIFIED-CORRECT (2026-08-11, Phase E1 — independently-verified faithful-match: orig Kraken.java:189-197 persists only LongEnough — hit_by_player/call_reinforcements were never saved; port matches line-for-line (:477-486). The audit's scenario is also backwards: a relog below 1/4 HP permanently DISARMS the wave in both versions (the hit flag only re-arms while health > max/4, orig :1154/port :420-422, but the call requires health < max/8 and Krakens never heal). MOD-022)

### BUG-027 — `TheQueen.myCanSee` truncates negative coordinates toward zero

- **Severity:** LOW
- **Location:** `danger/orespawn/entity/TheQueen.java:1189–1230`
- **Scenario:** `(int)(startx + dx)` rounds toward zero, so in negative-coordinate quadrants the LoS ray samples the wrong block column — Queen occasionally sees through (or fails to see past) corners.
- **Fix:** Use `Mth.floor`/`BlockPos.containing` for the sample positions.
- **Resolution:** VERIFIED-CORRECT (2026-08-11, Phase E1 — independently-verified faithful-match: the (int) truncation toward zero IS the original: orig TheQueen.java:880-884 feeds func_147439_a plain (int) casts on the accumulated ray positions; the port mirrors the math step-for-step including pre-increment-then-cast ordering (:1231-1239). Mth.floor would change negative-quadrant LoS results — parity violation. MOD-024)

### BUG-028 — `RTPBlock` spawns particles via `Level.addParticle` on the server — never visible

- **Severity:** LOW
- **Location:** `danger/orespawn/block/RTPBlock.java:77–81`
- **Scenario:** `entityInside` returns early on the client then calls `level.addParticle` on the server `Level` (a no-op) — the teleport burst never shows (sound works).
- **Fix:** Use `((ServerLevel) level).sendParticles(...)`.
- **Resolution:** FIXED (2026-08-11, Phase E1 — real port defect: Level.addParticle has an EMPTY body on the server (verified in the decompiled 1.21.1 Level.java:465-466) and the method is server-gated, so the burst never rendered anywhere. In 1.7.10 the teleported player's OWN client drew it (orig RTPBlock.java:51-56 ran in the client movement replay; server spawnParticle was a no-op — other players never saw it). Fixed with the per-player ServerLevel.sendParticles overload (the faithful single-viewer delivery), and the particle mapping corrected: orig smoke=SMOKE, explode=POOF (small white puff — modern EXPLOSION is the large blast flash), reddust=red DUST (DUST_PLUME did not exist). Sound was already working. See FIX_LOG Phase E)

### BUG-029 — `CrystalFurnaceBlockEntity` consumes bucket fuels without returning the container

- **Severity:** LOW
- **Location:** `danger/orespawn/gui/CrystalFurnaceBlockEntity.java:182–189`
- **Scenario:** Fueling with a lava bucket destroys the bucket (`fuel.shrink(1)`), unlike the vanilla furnace which leaves an empty bucket.
- **Fix:** After shrinking, place `fuel.getCraftingRemainingItem()` into the fuel slot if it's empty.
- **Resolution:** VERIFIED-CORRECT (2026-08-11, Phase E1 — independently-verified faithful-match: stale finding — the bare shrink-without-remainder the audit cites was already replaced in an earlier pass: orig TileEntityCrystalFurnace.java:165-170 returns getContainerItem when the stack hits zero (and :306 whitelists extracting empty buckets from the fuel slot), and the port's serverTick :195-199 implements exactly that via getCraftingRemainingItem with a citing comment. Lava buckets leave empty buckets in both versions)

### BUG-030 — `EntityWormMedium`: `upcount`/`downcount` not persisted

- **Severity:** LOW
- **Location:** `danger/orespawn/entity/EntityWormMedium.java:23–24`
- **Scenario:** Burrow/emerge cycle resets on relog; purely cosmetic.
- **Fix:** Persist both counters, or accept.
- **Resolution:** VERIFIED-CORRECT (2026-08-11, Phase E1 — independently-verified faithful-match: orig WormMedium.java has no NBT overrides (274 lines) — upcount/downcount reset on relog in 1.7.10 and re-seed identically on the first near-player tick (orig :123 = port :121, 25+rand(75)). MOD-022)

### BUG-031 — `EntityVortex.tick` heals client-side

- **Severity:** LOW
- **Location:** `danger/orespawn/entity/EntityVortex.java:117–119`
- **Scenario:** `heal(1.0f)` runs on both sides with independent RNG — client's local health copy briefly diverges (visual only; next health sync corrects).
- **Fix:** Gate the heal behind `!level().isClientSide`.
- **Resolution:** VERIFIED-CORRECT (2026-08-11, Phase E1 — independently-verified faithful-match: orig Vortex.java:128-130 runs the 1-in-200 heal(1.0f) unguarded in onUpdate, which executed on BOTH sides in 1.7.10 with an independent client RNG — the transient client-health divergence is inherited original behavior, corrected by the next health sync in both engines. MOD-024)

### BUG-032 — 39 aggregate sound events missing from sounds.json (found 2026-06-13, Phase D3)

- **Severity:** HIGH
- **Location:** `src/main/resources/assets/orespawn/sounds.json` vs orig `reference_1_7_10_source/.../sounds.json`
- **Scenario:** the original defines aggregate events (one key listing several variant files — e.g. `mothrawings` → mothrawings1/2/3, `b_fight`, `o_hurt`, `robot_living`) that the port's sounds.json never declared, while entity code references the aggregate names. Every such `SoundEvent.createVariableRangeEvent` resolved to nothing and played silence — a silent failure with no log spam.
- **Fix:** declare the 39 missing aggregate keys with their original variant lists.
- **Resolution:** FIXED (2026-06-13, Phase D3 — all 39 aggregates added to sounds.json (lowercase keys, original variant membership); found while wiring the Boyfriend/Girlfriend fight/taunt sounds and Prince-family wing sounds; see FIX_LOG.md)

---

## OPT — Optimization proposals (from 10_optimizations.md)

### OPT-001 — MHLib `getHitboxProfile()` registry lookup per part per tick / per bone per frame

- **Impact:** HIGH
- **Location:** `de/dertoaster/multihitboxlib/api/IMultipartEntity.java:345-357` (impl), `init/MHLibDatapackLoaders.java:34-36`; hot call sites `IMultipartEntity.java:176, 210, 268, 370-391`, `client/IBoneInformationCollectorLayerCommonLogic.java:36-52`
- **Cost:** `BuiltInRegistries.ENTITY_TYPE.getKey()` + datapack-registry map lookup + `Optional` alloc × part count × tick rate (server) and × bone count × frame rate (client) for every multipart boss.
- **Proposal:** Cache `Optional<HitboxProfile>` in a field on the entity (via `IMHLibFieldAccessor`), populated in `mhlibOnConstructor`, invalidated on datapack reload.
- **Behavior:** neutral (identical results; only `/reload` invalidation must be wired)
- **Resolution:** FIXED (2026-08-11, Phase F — Per-entity Optional<HitboxProfile> cache guarded by a global generation stamp; /reload (AddReloadListenerEvent) and ServerStoppedEvent bump the generation and clear the static cache; hot alignment/AI/pickable/read sites hoist to one lookup per call; ICustomHitboxProfileSupplier path deliberately uncached. Registry content is immutable per instance, so staleness is only possible across a reload — exactly what the stamp covers)

### OPT-002 — MHLib sends a full multipart update packet every tick even when nothing moved

- **Impact:** HIGH
- **Location:** `de/dertoaster/multihitboxlib/mixin/entity/MixinServerEntity.java:24-33`; payload at `network/server/SPacketUpdateMultipart.java:29-31, 67-77`
- **Cost:** Per tick × per multipart entity × per tracking player: full pos/rot/size for all parts + `ArrayList` + one `PartDataHolder` record per part per tick.
- **Proposal:** Track last-sent part transforms; skip the send when no part moved beyond epsilon and no part data is dirty (or throttle unchanged syncs to every 10 ticks as keepalive).
- **Behavior:** neutral (positions identical for idle bosses; strict change-only send alters nothing visible)
- **Resolution:** FIXED (2026-08-11, Phase F — Change-only multipart sends: last-sent PartDataHolder list compared field-by-field (exact primitive equality, NaN=changed); unchanged payloads re-broadcast bit-identically for a linger window sized to drain client interpolation, then skipped; new trackers get their first packet same-tick via the addPairing inject. Moving bosses send at legacy cadence bit-identically. The finding's epsilon/throttle alternative deliberately NOT applied (would break bit-identity))

### OPT-003 — MHLib master client streams `CPacketBoneInformation` continuously regardless of change

- **Impact:** HIGH
- **Location:** `de/dertoaster/multihitboxlib/api/IMultipartEntity.java:283-319` (`updateSynching`); per-frame collection `client/IBoneInformationCollectorLayerCommonLogic.java:34-61`; builder allocs `network/client/CPacketBoneInformation.java:73-165`
- **Cost:** Per-tick C2S packet per mastered multipart entity + per-frame `synchronized tryAddBoneInformation` (`mixin/entity/MixinLivingEntity.java:134`) + Optional/HashSet churn per bone.
- **Proposal:** Diff bone info against the last sent packet and only send on change, plus a low-rate keepalive so the 10-tick master-timeout (`updateSynching:288`) doesn't rotate masters.
- **Behavior:** affecting (server-side hitbox positions for static poses update less often — no visible difference; keepalive required to preserve master-election behavior)
- **Resolution:** FIXED (2026-08-11, Phase F close, owner-ruled — Applied per 2026-08-11 ruling: apply WITH keepalive. Master client now diffs each built CPacketBoneInformation against the last-sent map (exact primitive equality, no epsilon, NaN=changed — mirrors OPT-002) and skips only provably identical payloads, re-sending an identical keepalive every 8 ticks — 2-tick margin under the 10-tick master timeout, so election never rotates from protocol silence. Moving/animating bones stream at legacy cadence bit-identically. Soundness: server retains the sync map between packets (each accepted packet replaces it wholesale; server-side master reset clears it), because mhlibAiStep formerly cleared it per tick and skipped ticks would have snapped synced parts to fallback offsets. Mastership change nulls the client cache (first payload after election at legacy timing))

### OPT-004 — EntityVortex: up to 3 ungated AABB scans per tick on both sides

- **Impact:** HIGH
- **Location:** `danger/orespawn/entity/EntityVortex.java:101` (`tick()`, client+server), `:176` (`customServerAiStep`), scan at `:277-281`
- **Cost:** 2–3 `getEntitiesOfClass(LivingEntity, inflate(16,10,16))` + full list sort per vortex per tick — worst ungated per-tick scan in the port.
- **Proposal:** Cache the found target in a field, rescan every 5 ticks, reuse the result between `tick()` (particles only need "has target") and `customServerAiStep()`.
- **Behavior:** affecting (pull/aggro and particle-onset latency goes from 0 to ≤5 ticks — flag for sign-off)
- **Resolution:** FIXED (2026-08-11 ruling: apply). EntityVortex now holds one cached pull target rescanned every 5 ticks via currentPullTarget(), shared between tick() (has-target only, for busyFighting and the particle burst) and customServerAiStep() — one 16x10x16 AABB scan per 5 ticks per side replaces 2-3 per tick. Per the ruling, a dead or removed cached target is invalidated IMMEDIATELY on the next lookup, so busyFighting and the pull stop the same tick the target dies and the despawn guards never see a stale corpse; fresh-target acquisition and suitability re-checks (LoS, creative) lag <=5 ticks, the accepted pull/aggro/particle-onset latency.

### OPT-005 — GirlfriendOverlay: entity scan + string concat every rendered frame

- **Impact:** HIGH
- **Location:** `danger/orespawn/client/GirlfriendOverlay.java:39-41` (per-frame AABB + `getEntitiesOfClass`), `:47`, `:59` (string concats)
- **Cost:** Per frame (60–240 Hz): AABB alloc, predicate entity query, list alloc, 2+ string allocs per girlfriend.
- **Proposal:** Move the scan to a `ClientTickEvent.Post` handler refreshing a cached list (with pre-formatted name/health strings) every 10 ticks; `render` only draws the cache.
- **Behavior:** neutral (HUD data at most 0.5 s stale — cosmetic latency only)
- **Resolution:** VERIFIED-CORRECT (2026-08-11, Phase F — finding premise stale — Premise stale: GirlfriendOverlay was rebuilt (crosshair-pick HUD) since the audit — the cited per-frame girlfriend-list AABB scan and per-girlfriend string concats no longer exist. Current code does one vanilla crosshairPickEntity read plus a single 16-block fallback entity ray per frame (parity with orig OreSpawnMain.java:5795-5831), builds strings only for the one pointed-at eligible entity, and short-circuits on the config gate first. The audit's 10-tick cached-scan proposal would now add crosshair lag absent today, so no change was made)

### OPT-006 — Kraken obstruction probe: 95 block reads every server tick, unconditionally

- **Impact:** HIGH
- **Location:** `danger/orespawn/entity/Kraken.java:188` (call), `:339-360` (`applyObstructionAvoidance`: 19×5 grid, `new BlockPos` per probe)
- **Cost:** ~95 `getBlockState` + ~95 `BlockPos` allocs per Kraken per tick; Krakens spawn in packs of 1–10 (`KrakenRevengeHandler`, reinforcements at `Kraken.java:247-258`).
- **Proposal:** Run the probe every 4–5 ticks with one `BlockPos.MutableBlockPos`, scaling the lift impulse by the interval to keep net buoyancy identical.
- **Behavior:** affecting (obstruction-response latency up to 5 ticks; impulse scaling keeps average motion equal — flag)
- **Resolution:** FIXED (2026-08-11 ruling: apply). Kraken.applyObstructionAvoidance now runs its 19x5 = 95-block probe once every 5 server ticks (within the ruled 4-5) using a single reused BlockPos.MutableBlockPos instead of 95 fresh BlockPos allocations per tick. The lift impulse — both the deltaMovement add and the direct setPos shift — is scaled by the interval (x5), so net buoyancy over any 5-tick window is identical, per the finding's own math the ruling accepts. Throttle story documented in-code: obstruction response may lag up to 4 ticks; the cooldown is not persisted (weatherSet convention), so a reloaded Kraken probes on its first AI step.

### OPT-007 — Worm chain: duplicate player/segment scans twice per tick with allocations

- **Impact:** HIGH
- **Location:** `danger/orespawn/entity/EntityWormLarge.java:104, 165`; `EntityWormSmall.java:87, 139`; worst `EntityWormMedium.java:90-97, 150-156` (`getNearestEntity` + fresh `TargetingConditions` + AABB, twice per tick)
- **Cost:** 2 player-list scans per worm per tick; Medium adds 2 entity scans + 2 `TargetingConditions` allocs per tick.
- **Proposal:** Hoist `TargetingConditions.forNonCombat()` to a `static final`; compute nearest-player/nearest-small-worm once per tick and share between `aiStep` and `customServerAiStep`.
- **Behavior:** neutral (hoist + same-tick sharing); any further throttling (every 2–4 ticks) is affecting (tracking latency) — flag separately
- **Resolution:** FIXED (2026-08-11 ruling: NEUTRAL HALF ONLY; throttle DECLINED — same-tick freshness preserved exactly). EntityWormLarge: the identical getNearestPlayer(this, 8.0) scans in aiStep and customServerAiStep now share one tick-stamped per-tick result; the stamp is the invalidation — the cache never crosses a tick. EntityWormMedium: the per-tick TargetingConditions.forNonCombat() allocation is hoisted to a never-mutated static final. Medium/Small scan-sharing was found inapplicable: the TF-035 vertical-reach rework made their customServerAiStep box scans semantically different from aiStep's spherical queries, so merging would alter target selection — not neutral; documented in-code in all three classes. No cross-tick throttling anywhere.

### OPT-008 — Crystal-dimension terrain rewrite scans the full world column with a BlockPos per block

- **Impact:** HIGH
- **Location:** `danger/orespawn/world/OreSpawnChunkGenerator.java:302-336` (`replaceTerrain`: ≈98k `new BlockPos` + `getBlockState` per chunk), `:345-377` (`fillShallowWater`: second column scan)
- **Cost:** ~100k+ short-lived `BlockPos` allocations and full-height state reads per chunk on worldgen worker threads.
- **Proposal:** Reuse one `BlockPos.MutableBlockPos` per column; start the downward scan at `chunk.getHeight(WORLD_SURFACE_WG, x, z)`; merge `fillShallowWater` into the same column walk.
- **Behavior:** neutral (identical block output; heightmap start is safe — everything above surface is air)
- **Resolution:** FIXED (2026-08-11, Phase F — replaceTerrain now walks each column once from the WORLD_SURFACE_WG heightmap (everything above is air by the heightmap's NOT_AIR predicate; all pipeline writers use chunk.setBlockState so WG heightmaps stay accurate) with one reusable MutableBlockPos, and fillShallowWater is fused in as an inline state machine engaging at its old Y70 start. Fill writes land only in the water/air span the replacement logic never writes, and hitSurface is forced true on fill to mirror the old pass seeing the crystal-grass cap first — block output identical, ~98k BlockPos allocations and ~250 air reads per column eliminated)

### OPT-009 — ~35 entity classes reset MOVEMENT_SPEED base value every tick

- **Impact:** MEDIUM
- **Location:** Representative: `Godzilla.java:222`, `ThePrinceAdult.java:121`, `Baryonyx.java:68`, `EntityKyuubi.java:64`, `Basilisk.java:92`, `Camarasaurus.java:85`, `Girlfriend.java:118`, `Alien.java:102`, `Dragon.java:284`, `Boyfriend.java:119`, `Nastysaurus.java:72`, `Cryolophosaurus.java:66`, `Pointysaurus.java:83`, `BandP.java:92`, `EntityRat.java:85`, `EntityRubberDucky.java:120`, `EntitySpyro.java:129`, `EasterBunny.java:65`, `EntityLeafMonster.java:72`, `EntityMolenoid.java:91`, `ThePrinceTeen.java:120`, `CreepingHorror.java:66`, `DungeonBeast.java:79`, `EntityLeon.java:298`, `Cephadrome.java:127`, `Crab.java:98-99`, `Peacock.java:66`, `Cassowary.java:57`, `Alosaurus.java:84`, `EntityStinky.java:145`, `TRex.java:79`, etc. (only `SeaViper.java:147-149`/`WaterDragon.java:149-151` are genuinely dynamic)
- **Cost:** Attribute-map lookup per entity per tick across ~140 entity types (no sync spam — `setBaseValue` early-exits on equal — but the lookup is pure waste for constants).
- **Proposal:** Delete the per-tick call for constant speeds and set the value in `createAttributes()`; for water/land mirrors (SeaViper, WaterDragon, Crab) cache the `AttributeInstance` and only call `setBaseValue` on medium change.
- **Behavior:** neutral
- **Resolution:** FIXED (2026-08-11, Phase F — 30 constant-speed entities: per-tick MOVEMENT_SPEED setBaseValue deleted from tick()/aiStep and asserted once in the constructor with the identical expression (ctor-once is exactly value-preserving even where the float-literal set differs in double bits from createAttributes, e.g. ThePrinceTeen 0.35f vs registered 0.32). 17 tick() overrides that became super-only were removed. 4 genuinely dynamic entities (Crab water/land×scale, SeaMonster, SeaViper, WaterDragon water/land) keep the per-tick write but through a constructor-cached AttributeInstance; setBaseValue's internal equality early-exit provides the guard-on-change)

### OPT-010 — Godzilla allocates a Vec3 array + Vec3 per part every tick

- **Impact:** MEDIUM
- **Location:** `danger/orespawn/entity/Godzilla.java:216-220`
- **Cost:** `new Vec3[allParts.length]` + one `Vec3` per part per tick (server + client).
- **Proposal:** Replace with three reusable `double[]` fields (or store prev positions on the parts themselves).
- **Behavior:** neutral
- **Resolution:** FIXED (2026-08-11, Phase F — Godzilla.tick() no longer allocates a Vec3[] plus one Vec3 per part per tick. Three final double[] scratch buffers (partOldX/Y/Z, sized once in the constructor to allParts.length) capture the pre-super.tick() part positions and restore xo/yo/zo/xOld/yOld/zOld afterwards — bit-identical values, zero per-tick allocation)

### OPT-011 — ~37 sound getters allocate a new SoundEvent + ResourceLocation on every call

- **Impact:** MEDIUM
- **Location:** Representative: `GiantRobot.java:157-171`, `Robot1.java`–`Robot5.java` (3 each), `ThePrincess.java`, `ThePrinceTeen.java`, `PitchBlack.java` (3 each), `SpiderRobot.java:149-150, 187-188`, `Ostrich.java`, `VelocityRaptor.java`, `Fairy.java`, `Lizard.java`, `Ghost.java`, `GhostSkelly.java`
- **Cost:** `SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(...))` per ambient/hurt/death sound query (ambient polled periodically per mob); two allocs + string handling per call; bypasses the 100 registered `ModSounds` entries.
- **Proposal:** Replace with the corresponding `ModSounds.X.get()` holder (or a `static final SoundEvent` per class).
- **Behavior:** neutral (same sound id; registered events also serialize properly to clients)
- **Resolution:** FIXED (2026-08-11, Phase F — All 200+ per-call SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(MOD_ID, "...")) sites across ~92 classes hoisted to private static final SoundEvent fields (SND_*) — the createVariableRangeEvent idiom kept verbatim, allocated once per class per the finding's neutral variant. Multi-line variants handled: Dragon's mothrawings wing-flap, Crab's conditional scorpion_attack/scorpion_living swing pick (cached both, kept the 1-in-3 roll), Boyfriend's orespawnSound(literal) helper calls (helper retained, now used only by static initializers). ModSounds registry lambdas intentionally untouched)

### OPT-012 — Oversized-weapon culling mixin: 8 deferred-holder item checks per entity per frame

- **Impact:** MEDIUM
- **Location:** `danger/orespawn/mixin/EntityCullingMixin.java:16-33`
- **Cost:** Inside `getBoundingBoxForCulling` for every entity (vanilla included) every frame: `getMainHandItem` + up to 8 `ModItems.X.get()` + `is()` checks + `inflate(5.0)` AABB alloc on match.
- **Proposal:** Replace the 8 checks with a single item-tag test (`mainHand.is(OVERSIZED_WEAPONS_TAG)`) or a lazily-built `static Set<Item>`; `stack.isEmpty()` early-out already exists.
- **Behavior:** neutral
- **Resolution:** FIXED (2026-08-11, Phase F — EntityCullingMixin now builds a lazily-initialized @Unique static Set<Item> of the 8 oversized weapons (resolved once from the deferred holders on first frame) and tests mainHand.getItem() membership — identity-equivalent to the removed is() chain, so the culled set is unchanged. Invalidation story documented in-code: the item registry is frozen at startup and registered Item instances are never replaced (resource/datapack reloads do not touch them), so the set cannot go stale for the JVM lifetime)

### OPT-013 — Twelve big-mob renderers disable frustum culling entirely

- **Impact:** MEDIUM
- **Location:** `entity/client/QueenRenderer.java:50-52`, plus `shouldRender` overrides in `TheKingRenderer`, `GodzillaRenderer`, `GodzillaHeadRenderer`, `KingHeadRenderer`, `QueenHeadRenderer`, `KrakenRenderer`, `MothraRenderer`, `DungeonBeastRenderer`, `SeaMonsterRenderer`, `PitchBlackRenderer`, `LeonopteryxRenderer`
- **Cost:** Full model render every frame whenever the entity is loaded, even fully off-screen — for the largest models in the mod.
- **Proposal:** Keep culling but size `Entity.getBoundingBoxForCulling()` (or `shouldRender` calling super with an inflated AABB, ~30 blocks) to the real part envelope instead of returning `true` unconditionally.
- **Behavior:** neutral (visually, if the inflated box covers the part envelope — flag for visual verification on wing/tail extremes)
- **Resolution:** VERIFIED-CORRECT (2026-08-11, Phase F — proposal precondition unmet, current code correct — Evaluated all 12 renderers; no cull box was provably covering. Nine active shouldRender→true overrides (Queen, King, Godzilla, Kraken, Mothra, DungeonBeast, SeaMonster, PitchBlack, Leon) render animated envelopes — GeckoLib/MHLib bone-driven parts or code-model limb rotations — whose maximum reach is not statically derivable from any constant, and an under-sized box causes visible edge pop-out (a behavior change the audit itself flags for visual verification, which this pass cannot perform). The three head renderers already return false. Left noCulling everywhere and documented the decision in each file per instruction)

### OPT-014 — Alien torch scan probes 4,913 blocks per scan (docs claim 256)

- **Impact:** MEDIUM
- **Location:** `danger/orespawn/entity/AlienTorchSeekGoal.java:134-153` (radius 8 → 17³ probes), throttle at `:70` (~every 30 ticks per alien)
- **Cost:** ~4,900 block reads per alien per ~30 ticks, multiplied by alien pack sizes.
- **Proposal:** Scan in expanding shells with early exit once a torch is found within the legacy ≈5-block break distance, or cap radius to the documented 256-candidate budget; keep the existing `MutableBlockPos`.
- **Behavior:** neutral for early-exit-on-nearest; affecting if radius is reduced (smaller seek range — flag)
- **Resolution:** VERIFIED-CORRECT (2026-08-11, Phase F — finding premise stale — Orig scan_it (1.7.10 Alien.java:243-304) never early-exits within a shell — it probes all six faces and returns found != 0. The only orig early-exit is the shell loop (Alien.java:333): 'for (int i = 2; i < 15 && !this.scan_it(..., i, i, i); ++i)' — stop at the first shell containing a torch. E4's AlienTorchSeekGoal.java:69 reproduces exactly that ('for (int i = 2; i < 15 && !this.scanShell(x, y, z, i); ++i)') plus a neutral distSq-before-read gate in checkTorch. The 17-cube premise is gone; nothing neutral left to add)

### OPT-015 — Godzilla block crushing re-resolves deferred block holders per block

- **Impact:** MEDIUM
- **Location:** `danger/orespawn/entity/Godzilla.java:405-418` (`crushBlocks`), `:374-402` (`isCrushable`: ~20 identity compares incl. 6 `ModBlocks.X.get()` resolutions per block), called twice every 4th tick over a 29×29 slice (`:611-626`)
- **Cost:** ~1,700 block reads + up to ~34k comparisons per 4 ticks while loaded; `BlockPos.containing` alloc per block.
- **Proposal:** Build a lazily-initialized `static Set<Block>` of non-crushables (resolve `ModBlocks` holders once) and iterate with a `MutableBlockPos`.
- **Behavior:** neutral
- **Resolution:** FIXED (2026-08-11, Phase F — Godzilla.isCrushable now consults a lazily-initialized static Set<Block> of the 22 non-crushables, resolving the six ModBlocks deferred holders once instead of per probed block; Set membership is identity equals/hashCode (Block overrides neither), exactly the old == chain. crushBlocks reuses one MutableBlockPos for probes (Mth.floor per axis = BlockPos.containing's arithmetic; Y floor hoisted — position cannot change mid-loop) and passes immutable() copies only at actual setBlock sites since Level.setBlock side effects may retain the pos. Cache invalidation: none needed — Blocks are registry singletons for the JVM lifetime; datapack reloads never swap Block objects)

### OPT-016 — King/Queen target scans sort the entire 80×64×80 entity list

- **Impact:** MEDIUM
- **Location:** `danger/orespawn/entity/TheKing.java:1149-1173` (sort at `:1162`), `TheQueen.java:1254-1262`; re-scan of the same box at `TheKing.java:1273-1285`
- **Cost:** Every ~3–5 ticks per boss: full `LivingEntity` query over a 160×128×160 region + O(n log n) sort, when only nearest-suitable + a "head exists" flag are needed.
- **Proposal:** Replace sort-then-scan with single-pass nearest-suitable selection (track min distance); reuse one scan result for `findSomethingToAttack`/`findNearestPlayer` within the same tick.
- **Behavior:** neutral (nearest-first selection preserved)
- **Resolution:** FIXED (2026-08-11, Phase F — TheKing/TheQueen scans no longer sort the full 160-block-box entity list. New TargetSelection helper (entity/ai) provides first() (single-pass min, strict-less so equal-weight ties keep the first-encountered element — matching List.sort stability) and firstMatch() (lazy index-heap ordered by (comparator, original index), reproducing stable-sort tie order bit-for-bit and invoking isSuitableTarget on the same candidates in the same order). Applied to King's isEnd==2 player pick, general scan (headEntityFound now via a plain any-KingHead pass — provably the old loop's net effect), findNearestPlayer, and Queen's findSomethingToAttack. doJumpDamage/doAreaDamage process-all sorts untouched)

### OPT-017 — Ghost / GhostSkelly poll getNearestPlayer every idle tick

- **Impact:** MEDIUM
- **Location:** `danger/orespawn/entity/Ghost.java:96-100`, `GhostSkelly.java:87-91` (runs whenever `attackCooldown == 0`; the second gated call at `Ghost.java:113-114`/`GhostSkelly.java:104-105` is fine)
- **Cost:** Player-list distance scan per ghost per tick (cheap per call but ungated; contact range ~2 blocks).
- **Proposal:** Early-exit with a squared-distance check against the cached flight-target player, or gate the fallback poll to every 5 ticks.
- **Behavior:** neutral for the distance-early-exit variant; affecting if throttled (≤5 ticks contact-damage latency — flag)
- **Resolution:** FIXED (2026-08-11, Phase F — Applied the strictly neutral subset: the per-tick Math.sqrt(CONTACT_DAMAGE_RANGE_SQ) in the ungated fallback poll is hoisted to a static final CONTACT_DAMAGE_RANGE in both Ghost and GhostSkelly. The audited "squared-distance early-exit against the cached flight-target player" was evaluated against current code and rejected as NOT behavior-neutral: the flight target is a BlockPos (no player is cached), and any cached-candidate shortcut changes which player takes contact damage the tick a different player drifts into range in multiplayer. The fresh getNearestPlayer poll therefore stays; the throttle variant was already flagged affecting and was not applied)

### OPT-018 — MHLib runs a hitbox-profile registry lookup in every LivingEntity constructor

- **Impact:** MEDIUM
- **Location:** `de/dertoaster/multihitboxlib/mixin/entity/MixinLivingEntity.java:74-76` → `IMultipartEntity.mhlibOnConstructor` (`IMultipartEntity.java:469-505`, up to 4 `getHitboxProfile()` calls)
- **Cost:** Registry `getKey` + datapack lookup × 4 for every living entity constructed JVM-wide (vanilla mobs included) — significant during chunk load / spawn waves.
- **Proposal:** Memoize per `EntityType` in a static `Map<EntityType<?>, Optional<HitboxProfile>>` invalidated on datapack reload; bail out on cached empty.
- **Behavior:** neutral
- **Resolution:** FIXED (2026-08-11, Phase F — getHitboxProfile(EntityType, RegistryAccess) memoized in a two-level ConcurrentHashMap keyed by Registry instance + EntityType (vanilla mobs bail on cached empty); constructor path collapsed to one lookup; invalidation via registry-identity keying + the OPT-001 event clears + a size guard; worldgen-thread safe)

### OPT-019 — MHLib part alignment allocates ~6 Vec3 per part per tick with linear `contains`

- **Impact:** MEDIUM
- **Location:** `de/dertoaster/multihitboxlib/api/IMultipartEntity.java:162-193` (`alignSubParts`: chained Vec3 allocs + `synchedBones().contains` linear scan per part), `:195-239` (`alignSynchedSubParts`: fallback `BoneInformation` alloc per synced bone per tick)
- **Cost:** Per part per tick for every multipart boss on the server.
- **Proposal:** Precompute a per-part `isSynched` boolean at construction; fold the rotation/scale/translate chain into inline double math; only build the fallback `BoneInformation` when the sync map lacks the bone.
- **Behavior:** neutral
- **Resolution:** FIXED (2026-08-11, Phase F — Part alignment allocation-free: rotation/scale/translate Vec3 chain folded to inline double math mirroring decompiled Vec3.xRot/yRot/zRot/scale/add verbatim (bit-identical by construction); sync-bone flags precomputed at part construction from the lifetime-fixed profile; value-reusing setScaling skips Tuple/Optional churn; fallback BoneInformation built only when the sync map lacks the bone)

### OPT-020 — Boss bars updated every tick

- **Impact:** LOW
- **Location:** `PitchBlack.java:344`, `Kraken.java:155`, `Mothra.java:237`, `Godzilla.java:570`, `TheQueen.java:642`, `SpiderRobot.java:99`, etc.
- **Cost:** Negligible — `ServerBossEvent.setProgress` only broadcasts on change.
- **Proposal:** No action needed; listed to close out the checklist item.
- **Behavior:** neutral (N/A — no change proposed)
- **Resolution:** VERIFIED-CORRECT (2026-08-11, Phase F — no change needed per the finding itself — Closed as proposed by the audit: no change needed — ServerBossEvent.setProgress already broadcasts only on change, so per-tick updates cost nothing meaningful)

### OPT-021 — Sort-then-first-match pattern in ~25 small mobs

- **Impact:** LOW
- **Location:** Representative: `Fairy.java:147`, `EntityMantis.java:232`, `BandP.java:218`, `EntityVortex.java:280`, `EntityLeon.java:557`, `Robot2`–`Robot5`, `GiantRobot.java:142`
- **Cost:** O(n log n) sort of a small list every scan where a single-pass min would do.
- **Proposal:** Shared single-pass nearest-suitable selection helper in a util class.
- **Behavior:** neutral
- **Resolution:** FIXED (2026-08-11, Phase F — All sort-then-first-match target scans (44 mechanical + 8 hand-converted sites: Beaver/Peacock sort+get(0), Frog/Gazelle stream findFirst, SpiderDriver x2, RubberDucky, DragonflyHuntGoal) now use the shared TargetSelection helper: firstMatch() lazy index-heap / first() single-pass min. Order equivalence to the removed stable sort is exact, including equal-weight ties (original-index tiebreak = List.sort stability) and identical predicate call order/count, so side-effecting predicates (LoS rays, Molenoid MyCanSee) see the same sequence. Beaver's self-at-distance-0 parity bug is preserved. Cost never exceeds the removed sort even when every candidate is rejected)

### OPT-022 — Armor auto-enchant check runs per armor piece per inventory tick

- **Impact:** LOW
- **Location:** `danger/orespawn/item/ItemOreSpawnArmor.java:130-150`
- **Cost:** One data-component presence check per OreSpawn armor stack per tick (cheap; enchant application itself is once).
- **Proposal:** Apply enchants in `onCraftedBy`/first pickup instead of polling `inventoryTick`, or gate the check to every 20 ticks.
- **Behavior:** affecting (`onCraftedBy` migration changes when loot/creative-given stacks get enchanted; the 20-tick gate delays first-tick enchanting by ≤1 s — flag whichever is chosen)
- **Resolution:** FIXED (2026-08-11 ruling: apply 20-TICK-GATE variant; onCraftedBy migration REJECTED). ItemOreSpawnArmor.inventoryTick now gates the auto-enchant presence poll behind entity.tickCount % 20 == 0, cutting the per-piece data-component check to once a second, staggered per holder rather than spiking on a global tick. A freshly obtained un-enchanted piece may sit plain for <=1 s before the poll lands — accepted by the ruling. The code comment records the rejection: onCraftedBy would change when loot-table/creative-given/pre-existing stacks get enchanted, and the poll is the self-healing contract for every acquisition path. Glide handling deliberately remains ungated per-tick.

### OPT-023 — Godzilla re-resolves the Mobzilla SavedData every tick

- **Impact:** LOW
- **Location:** `danger/orespawn/entity/Godzilla.java:579-581` → `MobzillaSpawnTracker.get():67-70`
- **Cost:** `overworld().getDataStorage().computeIfAbsent` map lookup per tick (markSpawned is idempotent-guarded).
- **Proposal:** Cache a local `markedSpawned` boolean on the entity and skip after first success.
- **Behavior:** neutral
- **Resolution:** FIXED (2026-08-11, Phase F — Godzilla no longer walks server→overworld→dataStorage.computeIfAbsent every tick. The MobzillaSpawnTracker instance is cached on the entity, keyed on its current ServerLevel and re-resolved if the level ever differs (dimension moves construct fresh entities, so the key check is belt-and-suspenders); SavedData instances are never evicted from data storage, so the reference cannot go stale within a server run. markSpawned() is still invoked every tick, deliberately preserving the port's continuous re-assert semantics (e.g. after an external reset()) rather than the audit's skip-after-first-success boolean, which would drop that re-assert)

### OPT-024 — Dragon rider-mode pushes via a broad `getEntities` query per tick

- **Impact:** LOW
- **Location:** `danger/orespawn/entity/Dragon.java:472-478`
- **Cost:** Per tick while ridden: AABB alloc + all-entity query (mirrors vanilla `pushEntities`, acceptable).
- **Proposal:** Optional: pass a `pushable` predicate into the query to skip the post-filter.
- **Behavior:** neutral
- **Resolution:** FIXED (2026-08-11, Phase F — Dragon.serverRiddenTick's push query now passes the loop's exact filter as the getEntities predicate (plus an explicit !isSpectator mirroring the EntitySelector.NO_SPECTATORS the 2-arg overload applied), so the returned list holds only entities that get pushed — same result set, no post-filter pass or oversized list. getFirstPassenger is hoisted to a local; equivalence argument (pushes only alter deltaMovement, so isRemoved/isPushable/passenger answers cannot change mid-loop) documented in-code)

### OPT-025 — Worldgen flora helpers allocate BlockPos pairs in descending column loops

- **Impact:** LOW
- **Location:** `danger/orespawn/world/OreSpawnChunkGenerator.java:613-620` (flowers), `:636-643` (rice), `:659-666` (quinoa), `:682-689` (termite mounds), `:203-211` (scraggly trees), `:559-587` (ore veins: `new BlockPos` + `BlockState.equals` per cell)
- **Cost:** Per chunk: a few thousand short-lived `BlockPos` allocations; `equals` instead of `==`/`is()` on interned states.
- **Proposal:** Reuse a `MutableBlockPos`; start scans at the heightmap; compare states with `.is(block)`.
- **Behavior:** neutral
- **Resolution:** FIXED (2026-08-11, Phase F — All descending flora/tree scans (crystal flowers, rice, quinoa, termite mounds, Islands scraggly trees, and the identical Chaos scraggly loop) now reuse one MutableBlockPos and start at min(origStart, WORLD_SURFACE_WG+1) — a hit requires non-air at y-1, so every skipped level was a guaranteed miss, and none of these y loops roll randomness, so RNG sequences and placements are bit-identical. generateOreVein reuses a cursor and compares states with == instead of .equals (BlockState has no equals override — identity either way, minus the virtual call))

### OPT-026 — TheKing line-of-sight: manual 20+-step block ray per candidate

- **Impact:** LOW
- **Location:** `danger/orespawn/entity/TheKing.java:1185-1225` (`MyCanSee`), called per candidate from `isSuitableTarget:1138`
- **Cost:** Up to ~20–60 `getBlockState` + `BlockPos.containing` allocs per candidate per scan (scan throttled ~1/3–5 ticks).
- **Proposal:** Evaluate `MyCanSee` only for the current best candidate (after the distance min-pass) and reuse a `MutableBlockPos` in the march.
- **Behavior:** neutral (if applied only to the selected candidate in the same order)
- **Resolution:** FIXED (2026-08-11, Phase F — Both halves of the finding applied to TheKing. (1) Via the OPT-016 TargetSelection restructure, isSuitableTarget — and thus the MyCanSee 20-60-step block ray — now runs only for the current best candidate in exact weighted-sorted order until one passes, never for the whole list, matching the finding's "selected candidate in the same order" neutrality condition. (2) MyCanSee itself reuses a single BlockPos.MutableBlockPos for the whole march instead of a BlockPos.containing allocation per step (identical floor semantics))

### OPT-027 — Spawn-cluster checks scan twice per spawn attempt

- **Impact:** LOW
- **Location:** Representative: `EntityAnt.java:208-214`, `EntityCricket.java:156-162`, `Chipmunk.java:211-219`, `EntityTermite.java:221-228`, `Frog.java:273-283` — each runs both a `size() <= N` check and a separate count over the same `inflate(20,10,20)` box
- **Cost:** Spawn-time only: duplicate entity query per `checkSpawnRules` call during spawn cycles.
- **Proposal:** Compute the count once and reuse for both checks.
- **Behavior:** neutral
- **Resolution:** VERIFIED-CORRECT (2026-08-11, Phase F — finding premise stale — The audited double scan no longer exists. Each checkSpawnRules now runs exactly one entity query: EntityAnt.java:235, EntityCricket.java:157, Chipmunk.java:226, EntityTermite.java:262, and Frog.java:286 (via OriginalSpawnGates.countBuddies). The adjacent findBuddies helpers are dead code — never called anywhere in src/main/java (only Peacock calls its own, once, as its sole scan). The proposal 'compute the count once and reuse' is already realized by the spawn-gate rebuild (D1 OriginalSpawnGates port))

---

## Field reports — beta.2 public play (2026-08-11)

### BUG-032 — Published jar missing the databuddy runtime dependency

- **Impact:** CRITICAL (field) — beta.2 crashes at mod construction on any
  install without databuddy: `NoClassDefFoundError
  net/commoble/databuddy/codec/RegistryDispatcher` from
  `MHLibHitboxTypes.<clinit>` (the vendored MHLib builds its hitbox-type
  registry through databuddy's RegistryDispatcher at class-init).
- **Location:** `build.gradle:141` (`implementation`-only dependency).
  First field report: owner instance, crash-2026-08-11_13.58.36-fml.txt.
- **Root cause of the miss:** dev and gametest classpaths carry
  implementation deps, so every gate was green while the shipped artifact
  was broken — the suite cannot see packaging bugs by construction.
- **Resolution:** FIXED (2026-08-11, beta.3 — databuddy jarJar'd into
  META-INF/jarjar with a [6.0.0.0,6.1.0) range; presence in the built jar
  verified structurally).

### BUG-033 — Structure-piece scratch state races under parallel worldgen

- **Impact:** CRITICAL (field) — game-freezing. `LegacyDungeonPiece` and
  `RoyalTreePiece` cached per-pass state (WorldGenLevel, a shared
  MutableBlockPos, six chunk-clip bounds) in plain instance fields, set at
  postProcess start and nulled in a finally. One piece is shared by every
  chunk it spans and postProcess for different chunks runs CONCURRENTLY —
  observed in the field under c2me's chunk workers and Distant Horizons'
  DH-World Gen threads. Race A: a finishing pass nulls the scratch state
  under a live pass → NPE in place() → "Error upgrading chunk to
  minecraft:features" → `Failed to load chunk 8,6` (orespawn:mining) →
  wedged chunk system, server unresponsive (the reported freeze). Race B:
  two live passes adopt each other's chunk-clip boxes → silent wrong-chunk
  writes.
- **Location:** `LegacyDungeonPiece.java` (fields formerly :461-468, NPE at
  :752 via BasiliskMazeGenerator.buildCastle:384), `RoyalTreePiece.java`
  (fields formerly :144-153). Field stack: owner instance latest.log
  14:55:20.
- **Why the suite missed it:** the GameTest server generates chunks with
  far less feature-stage parallelism; the race window needs concurrent
  passes over one piece.
- **Resolution:** FIXED (2026-08-11, beta.3 — both classes carry the pass
  state in a ThreadLocal PassCtx record; helper signatures unchanged so the
  ~35 per-structure generator classes are untouched. Sweep of
  world/structure/ found no other mutable instance state on
  place/postProcess paths — `runtimeRandomOverride` is safe because
  buildNow constructs a fresh piece per call).

### BUG-034 — DungeonBeast unspawnable: Params innerAttackRoll=0

- **Impact:** HIGH (field) — the DungeonBeast could never spawn in beta.2:
  `BugMeleeAttackGoal.Params.dungeonBeast()` shipped `innerAttackRoll=0`,
  the TF-026 construction guard threw, and NaturalSpawner logged "Failed to
  create mob" on every attempt (observed spamming both field sessions).
- **Location:** `BugMeleeAttackGoal.java:65`. Original values:
  reference DungeonBeast.java:172 (`nextInt(8)` swing cadence) and :177
  (`nextInt(7) == 0 || nextInt(8) == 1` — outer 7, inner 8; the goal
  already reproduces the original's `== 1` inner-roll quirk at
  BugMeleeAttackGoal.java:144-145).
- **How the slip survived:** the guard was added by TF-026 for exactly this
  class of slip — but no suite test constructed every entity type, so a
  guard trip at construction was invisible to the gates.
- **Resolution:** FIXED (2026-08-11, beta.3 — inner=8 per the original;
  EntityConstructionTests added — constructs all registered orespawn entity
  types every suite run, so this failure class is now a red gate).

### BUG-035 — Queen freezes mid-air: trigger-fired clips can never finish

- **Impact:** HIGH (field) — from her first melee onward, the Queen's whole
  model is permanently slaved to her last attack clip. The Actions
  controller's "one-off" triggers use thenPlay (= LoopType.DEFAULT = defer
  to the animation json), but the json declared bite/tail_whip_left/
  tail_whip_right as loop:true and roar as hold_on_last_frame. GeckoLib
  clears a triggered animation only when it FINISHES (state STOPPED;
  verified in AnimationController source and the pinned 4.8.4 bytecode —
  LOOP never terminates, HOLD parks PAUSED), so the trigger never clears,
  the controller never returns to its STOP predicate, and — since every
  Queen clip keyframes the identical 58-bone set and Actions is registered
  after Movement — the stuck clip overrides the entire model. Field
  symptom: frozen mid-roar (or endlessly replaying a swing) while hovering;
  a hit re-aggros her, the next trigger replaces the stuck clip, and she
  "starts back up". Parity note: the 1.7.10 Queen NEVER stops animating in
  any state (ModelTheQueen drives wings/tail/heads off ageInTicks
  unconditionally), so the freeze is also a parity break, not just a
  cosmetic bug in the port-invented GeckoLib layer (BOSS-010).
- **Location:** `assets/orespawn/animations/entity/the_queen.animation.json`
  (bite :24499, tail_whip_right :26756, tail_whip_left :31026,
  roar :35296) vs `TheQueen.java:1439-1444`.
- **Resolution:** FIXED (2026-08-11 — the four one-off attack clips set to
  loop:false so they finish, clear the trigger, and blend back (5t) to the
  Movement stance. death KEEPS hold_on_last_frame deliberately: its
  never-finishing hold is what keeps the corpse posed, and Movement STOPs
  on isDeadOrDying so nothing fights it. idle/attack keep loop:true.)
- **Pattern audit (owner-directed):** mechanical scan of every
  triggerableAnim in src/main/java against every animation json's loop
  declarations: TheQueen is the ONLY GeckoLib animatable in the codebase
  (registerControllers appears nowhere else; MHLib's glibplus trigger API
  is vendored but unused), so there are no sibling defects. Scan flags
  post-fix: 4 ok + death NEVER-FINISHES (intentional, documented above).

### BUG-036 — vendored MHLib demo profile gave vanilla creepers multipart hitboxes (FIXED 2026-08-11)

Found during S4 research (2.0 spider overhaul), live in public beta.2
and beta.3: the vendored MultiHitboxLib carried its upstream DEMO
profile `data/minecraft/multihitboxlib/hitbox_profiles/creeper.json`
into the shipped jar. Effect on every vanilla creeper: MHLib's
LivingEntity ctor mixin resolved the profile and built three
PartEntities (feet 0.5x / body 1.0x / head 2.0x damage modifiers), the
profile's main-hitbox canReceiveDamage=false made the creeper's own
body UNPICKABLE via IMultipartEntity.mhLibIsPickable (direct
crosshair/ray hits resolve only through the part boxes), and
isMultipartEntity() reported true — silent vanilla-behavior deviation,
against the parity law's hardest form (vanilla mobs must be untouched).
Not caught earlier because no suite test asserted vanilla-mob
neutrality and creeper combat still "worked" through the part
surfaces. FIX: the stowaway data file is deleted (data-only; MHLib
code untouched); gametest
VanillaParityTests.bug036_vanilla_creeper_has_no_mhlib_parts pins
zero parts + isMultipartEntity false + directly pickable so a future
vendoring refresh cannot reintroduce a demo profile. Ships with the
next release.

### BUG-037 — Invented wild royal spawns: Princess/Prince at world creation

*(Renumbered from BUG-036 at 2.0.0-beta.1 release prep — duplicate ID
assigned by parallel sessions; the creeper demo-profile finding above
keeps BUG-036. Commit 4ea395c's message retains the old number.)*

- **Impact:** HIGH (field, CrazyCraft 5.0 pack report) — a brand-new world
  could generate with ThePrincess (or ThePrince) standing at the player's
  feet. Three pieces: (1) companion_royalty.json (neoforge:add_spawns)
  put both royals in the CREATURE pool of every #minecraft:is_overworld
  biome at weight 1; (2) both are MobCategory.CREATURE, the category
  vanilla PRE-POPULATES into newly generated chunks
  (NaturalSpawner.spawnMobsForChunkGeneration, CHUNK_GENERATION) and
  makes persistent-by-category; (3) both checkSpawnRules return true
  unconditionally (faithful: orig ThePrincess.java:369-371 /
  ThePrince.java:381-383).
- **Original contract:** the royals appear in NO spawn list. The complete
  EntityRegistry.addSpawn roster (orig OreSpawnMain.java:4522+, 55
  classes) and the dimension-biome spawn architecture contain neither;
  they are obtainable via spawn egg, the Queen's death (orig :193), and
  structures. Wild spawning is invented content.
- **Provenance:** companion_royalty.json was added pre-audit (8928c67
  Phase 4E) alongside five sibling invented companion files
  (camarasaurus, gamma_metroid, leon, spyro, velocity_raptor) that the
  audit later deleted — royalty was the lone survivor; no audit finding
  ever covered it (grep-verified).
- **Resolution:** FIXED (2026-08-11 — companion_royalty.json deleted,
  option D: the original never had wild royals, so gating or narrowing
  it would preserve invented content. Spawn RULES untouched (faithful).
  Regression net: SpawnGateTests#bug037_no_wild_royalty_in_creature_pools
  asserts both royals absent from overworld CREATURE pools with the
  Girlfriend's faithful plains entry as positive control — girlfriends
  at world spawn on a new world ARE original behavior, chunk-gen
  pre-population of roster-backed creatures is faithful and untouched.)

### BUG-038 — 155 resource files tracked in git under uppercase names (fresh-clone texture/sound loss)

- **Impact:** HIGH, latent (every clone except the release machine) — git's
  index tracked 147 entity textures, 3 item textures, 1 block texture and 4
  sounds under uppercase names (`Kyuubi.png`, `GammaMetroid.png`, `Bird1.png`,
  `RayGun.png`, `oreMOTHRA.png`, `MothraWings1.ogg`, …) while the release
  checkout (Windows, `core.ignorecase=true`) had every file lowercase on
  disk. `ResourceLocation` paths are lowercase-only, so every Java reference
  (57 literal, more dynamically built) and every `sounds.json` entry is
  lowercase; the published jars carry 428 lowercase entity textures only
  because Gradle copied this checkout's disk names. A fresh clone — Linux,
  macOS, CI, or another Windows machine — writes the tracked uppercase
  names, the jar inherits them, and the case-sensitive jar filesystem misses
  the lookups: textureless mobs, silent Mothra wings.
- **Original contract:** n/a (port build hygiene). The 1.7.10 jar stored
  CamelCase names and 1.7.10's ResourceLocation tolerated them; 1.21.1's
  does not, so lowercase is the only possible intended name.
- **Provenance:** files were copied from the 1.7.10 reference with their
  CamelCase names and `git add`ed before being lowercased on disk; with
  `core.ignorecase=true` the case-only rename never registered. Surfaced by
  the 2026-08-31 orchestrator run (worker orespawn-9 counted 68 mismatched
  references and proposed a build-time alias generator — not adopted) and
  verified independently 2026-09-02: `git ls-files` vs `ls`, jar listing
  (0 uppercase entries), Java literal scan (0 uppercase literals),
  `provenance_byte_identical_assets.txt` (already lists every port path
  lowercase). The asset audit could not see it: every earlier check reads
  the disk.
- **Resolution:** FIXED (2026-09-02 — 155 index-only `git mv` renames;
  disk and jar byte-identical to shipped beta.4; no collisions, no
  uppercase directories. Guard: `tools/asset_audit.py` check 7
  (`RESOURCE_PATH_CASE` / `TEXTURE_REF_CASE`) reads the git INDEX and errors
  on any non-lowercase tracked resource path or any Java asset literal whose
  exact name git does not track; mutation-tested by re-uppercasing one
  tracked entry (audit → ERROR) and restoring it.)

### BUG-039 — Hoverboard rider appears seated in the owner's pack; original and port both declare standing (REPORT, 2026-09-02)

- **Impact:** LOW-visual, HIGH-confusion — the owner saw a seated rider on
  the hoverboard (`orespawn:elevator`) during the Phase G in-game look and
  asked for standing.
- **Original contract:** the rider STANDS. `reference_1_7_10_source/.../Elevator.java:121-123`
  is a hand-written `shouldRiderSit() { return false; }` — a Forge 1.7.10 API
  method the author overrode on purpose; the mount offset (`:161-163`,
  `:517-521`) puts a 1.7.10 player's feet exactly on the deck, consistent
  with standing. `RenderElevator.java:27-46` never touches the rider.
- **Port state:** ALREADY FAITHFUL IN CODE. `Elevator.java:154-158` returns
  `false` (present since the initial commit); `positionRider` (`:189-193`)
  places players at deck height (TF-029). The 1.21.1 hook is live: NeoForge's
  `IEntityExtension.shouldRiderSit()` is read by `LivingEntityRenderer`
  (`:56-57`) and is the only source of `HumanoidModel.riding`.
- **Most likely cause of the observation:** a player-animation mod in the
  owner's CrazyCraft 5.0 pack overriding the rider pose regardless of the
  vehicle's flag — the pack carries NotEnoughAnimations 1.9.3, Player Animator
  2.0.4, Serious Player Animations 1.2.0, Better Combat 2.4.0 and SittingPlus
  1.0.1. The DH & Iris instance carries none of them and is the clean test bed.
  Discriminator in-game: a seated render also clamps the torso to the board's
  facing; a standing render lets the torso turn freely while the board lags.
- **Classification:** PARITY BUG class by the owner's rubric (original stood),
  but the port code already matches; no port change is indicated until the
  cause is reproduced without the pose mods. If it reproduces there, reopen
  as a port bug; if not, record as third-party interaction (like the Hats
  Renewed finding in Slice 2).
- **Resolution:** OPEN — report only, per owner ("report before fixing,
  separate commits"). Next step: owner rides the board in the DH & Iris
  instance with the current jar.

### ENT-S-089 — Vortex: eight residual divergences from orig Vortex.java; "flies around doing nothing" is faithful idle + unmet trigger (REPORT, 2026-09-02)

- **Impact:** MEDIUM — the owner reports the Vortex flies around doing
  nothing. Side-by-side of `reference_1_7_10_source/.../Vortex.java` (404
  lines) and `EntityVortex.java` (331 lines): the AI shape is the same (no
  goals; wander + drag-pull + melee in the server tick; targeting via
  `findSomethingToAttack`), and the observation is explained WITHOUT a port
  bug: the target predicate rejects CREATIVE players (orig `:302-306`, port
  `:320`), requires a `LivingEntity` inside the 16/10/16 inflated box with
  line of sight, and excludes eleven species — a creative observer in an
  empty area is never a target, so no smoke burst, no pull, no hit, exactly
  as in 1.7.10 (the pull is also delivered through `hasImpulse`, invisible
  until a hit lands; ruled faithful in BUG-019). Discriminator: stand near it
  in SURVIVAL within ~10 blocks in the open.
- **Real divergences found (none previously recorded; none alone produces
  "does nothing", 1-3 blunt the fight once a target exists):**
  1. Hitbox `setSize(2.0f, 4.0f)` (orig `:50`) vs `.sized(1.0f, 1.5f)`
     (`ModEntities.java:281`) — shrinks the scan box and drops the LoS eye from
     ~3.4 to ~1.28 blocks, so the port fails line-of-sight over terrain the
     original cleared. Precedent for restoring: Cephadrome (AUDIT `:796`).
  2. Missing empty `collideWithEntity` override (orig `:98-99`): with
     `isPushable()` true and no `doPush`, `pushEntities()` shoves the victim
     out of the vortex at the range where the drag pull is strongest.
  3. Missing `canSeeTarget` clip probe on the wander candidate (orig
     `:146-148`, `:178-180`; port `:203` accepts on `isAir()` alone); the
     invented `stuckCount > 30` retarget (`:172-179`, `:191`) looks like a
     workaround for the symptom this creates.
  4. Missing `persistenceRequired` early return before the daytime discard
     (orig `:131-133`; port `:131`) — a name-tagged Vortex can vanish at dawn.
     Sibling `EntityRotator.java:115` has it.
  5. Missing `doesEntityNotTriggerPressurePlate -> true` (orig `:221-223`).
  6. Missing fixed voice pitch 1.0 (orig `:78-80`); vanilla randomizes +/-0.2.
  7. Smoke-particle drift sign inverted: port `:121,123` uses `dir + pi/2`
     where the original evaluates `dir - pi/2` (its `dir -= pi` happens inside
     the first cos at `:124`). Cosmetic.
  8. Wander retarget threshold `< 2.1` (orig `:165`) vs `< 4.0` (port
     `:191`), and the original's write-before-validate quirk (`:176`) dropped
     (port assigns only on success, `:204`). Cadence only.
- **Already-ruled, not divergences:** 5-tick target cache (OPT-004, ruled
  apply); 1-in-200 heal unguarded (BUG-031 faithful); pull via `hasImpulse`
  (BUG-019 faithful); skywardLaunch removed (ENT-S-069).
- **Tests:** `EntityLogicTestsB#i050_vortex_no_launch_drag_pull` proves the
  pull vector on one step; nothing exercises `findSomethingToAttack`, the
  wander loop, or LoS acquisition — exactly where 1-3 live.
- **Resolution:** FIXED (2026-09-02, owner's go, own commit). All eight
  restored in the classic entity with orig citations: `.sized(2.0f, 4.0f)`
  (`ModEntities`), empty `doPush`, the eye-line `canSeeTarget` clip probe
  (`ClipContext.Block.OUTLINE`, no fluids) on every wander candidate with the
  invented `lastX/Y/Z`/`stuckCount` removed, `!isPersistenceRequired()` on
  the daytime discard, `isIgnoringBlockTriggers -> true`, `getVoicePitch ->
  1.0f`, the particle tangent from the decremented `dir` (`dir - PI/2`), the
  `< 2.1` retarget threshold and the write-before-validate quirk (each
  candidate assigned before validation, 50 failures leave the last one).
  Tests (`VortexParityTests`): dims pin, two overlapping Vortexes never move
  each other, persistence-required survives a forced daytime discard roll
  while a non-persistent control is discarded, a Vortex on a stone pressure
  plate leaves it unpowered while a zombie powers its own, voice pitch
  exactly 1.0 over eight calls. Items 3, 7 and 8 have no deterministic
  server observable and are pinned by review of the cited lines. Test
  lessons recorded: `spawnWithNoFreeWill` strips goals but not
  `customServerAiStep` (`setNoAi(true)` is explicit), and `GameTestHelper.spawn`
  marks every mob persistence-required (the discard control is added by
  hand). The survival-vs-creative acquisition test proposed earlier is
  deferred: the OPT-004 cache and the 16/10/16 scan are unchanged by this
  fix.
- **SPLIT PRESENTED (2026-09-02, per owner: "any divergence without a
  recorded MOD entry is a parity bug"):** all EIGHT are PARITY BUGS WITH NO
  RECORD. Method: eight independent classifiers, each refuted by two
  independent readers (one sweeping every MOD-/BUG-/ENT-/OPT-/TF- record, one
  re-reading the port and reference lines); 16/16 refutations failed on the
  label (one corrected evidence only: the report-only FIX_LOG entry of this
  finding exists but is not a MOD or intent record). Facts established:
  - MOD records live in `MODERNIZATION_NOTES.md` (MOD-001..MOD-028); the
    only Vortex-scoped ones are MOD-024 sub-items for the smooth pull
    (BUG-019), the client-heal gate (BUG-031) and scan caching (OPT-004).
    None covers dimensions, pushing, wander line-of-sight, persistence,
    pressure plates, voice pitch, particle direction or the wander threshold.
  - `EntityVortex.java` is densely annotated wherever a divergence was
    deliberate (OPT-004/-011/-021 markers, `orig Vortex.java:NNN` citations at
    :46-54, :111, :132, :148, :224, :311); none of the eight sites carries any
    comment. `ModEntities.java:280` cites orig `:52` (fire immunity) two lines
    below the uncited `.sized(1.0f, 1.5f)`, the same shared boilerplate literal
    as robot_5 (`:139`) and baryonyx (`:302`): a copy-paste default, not a
    choice. Precedents fixed without a MOD record: ENT-S-088 (SpiderRobot
    dims), ENT-A-083 (Cephadrome dims), ENT-A-002, BOSS-026.
  - No MHLib hitbox profile exists for the Vortex (only ant_robot,
    spider_robot, the_queen), so the Size-hook law needs no second edit site.
  - Refuter finding beyond this entity: the dropped
    `doesEntityNotTriggerPressurePlate` (item 5, orig `func_145773_az`) recurs
    in roughly 35 other ported 1.7.10 classes (Rotator, Mothra, TheKing,
    TheQueen, WormLarge, ...). Logged as ENT-S-090 for a systemic sweep.
  - Proposed fix commit (awaiting the owner's go): restore `.sized(2.0f, 4.0f)`
    with the orig `:50` citation; empty `doPush`; the y+0.75 eye-line
    `canSeeTarget` probe on wander candidates with the invented
    `stuckCount`/`lastX/Y/Z` removed; `!isPersistenceRequired()` on the dawn
    discard (as `EntityRotator.java:115`); `isIgnoringBlockTriggers -> true`;
    `getVoicePitch -> 1.0f`; the particle tangent from the already-decremented
    `dir` (cos/sin(dir - pi/2)); the `< 2.1` threshold with the
    write-before-validate quirk. Tests: a dims pin (width 2.0 / height 4.0 on
    ENTITY_VORTEX, modelled on the ENT-S-088 spider pin), a no-push impulse
    test, a persistence-survives-dawn test, a pressure-plate test, a
    deterministic-pitch test, and a tangential-sign test for the particles.

### ENT-S-090 — `doesEntityNotTriggerPressurePlate` (orig `func_145773_az -> true`) dropped across ~35 ported entities (REPORT, 2026-09-02)

- **Impact:** LOW each, WIDE — the original overrides `func_145773_az()` to
  `true` on about thirty-five entities (Vortex, Rotator, Mothra, TheKing,
  TheQueen, WormLarge, ...), so they never press plates or trip wires. The
  port's `isIgnoringBlockTriggers` is only overridden where the audit already
  touched it. Surfaced by the ENT-S-089 refuters while checking item 5.
- **Sweep (2026-09-02):** the reference overrides `func_145773_az` in 35
  classes, 15 returning true (the rest return false, the default); the port
  had ZERO `isIgnoringBlockTriggers` overrides. The fifteen: Brutalfly,
  Butterfly, LunaMoth, Mosquito, Fairy, Firefly, Ghost, GhostSkelly, Mothra,
  PurplePower, Rotator, Vortex, WormLarge, WormMedium, WormSmall.
- **Resolution:** FIXED (2026-09-02, owner's go, own commit; Vortex landed
  with ENT-S-089). Each port entity gets `isIgnoringBlockTriggers -> true`
  with its reference line cited. Gametest `PressurePlateParityTests`
  (`empty_large`): every one of the fifteen is spawned in a stone pressure
  plate's block, the plate's own `entityInside` is invoked with it, and the
  plate must stay unpowered; a zombie on an identical plate is the control.
  Invoking `entityInside` directly is what makes fliers and noAi mobs count:
  a noAi mob never travels, so it never lands.

### BUG-040 — Coin renders nothing: the port's ModelCoin is not the 1.7.10 model (REPORT, 2026-09-02)

- **Impact:** HIGH-visual — the classic `orespawn:coin` entity is invisible in
  the port. Found by the Slice 4b harness: the visual leg's foreground was 0 on
  BOTH sides for every capture.
- **Evidence:** port `ModelCoin.createBodyLayer` is one 16x16x4 box at
  `texOffs(0, 0)` on a 512x512 sheet, so it samples the sheet's top-left
  40x20 texels; every one of those 800 texels in `coin.png` (byte-identical
  to `cointexture.png` and to the reference `Cointexture.png`) has alpha 0,
  and `entityCutoutNoCull` discards them. `setupAnim` spins `coin.yRot =
  ageInTicks * 0.1F`.
- **Original contract** (`reference_1_7_10_source/.../ModelCoin.java`,
  `RenderCoin.java:20`, `ClientProxyOreSpawn.java:491`): texture 512x512;
  ONE `ModelRenderer` at texture offset (0,0) with `addBox(-128, -128, 0,
  256, 256, 1)`, rotation point (0, -109, 0), `mirror = true`; yaw
  `cos(ageInTicks * 0.05 * wingspeed) * PI` with wingspeed 0.22 (an
  oscillation, not a spin); rendered at scale 0.125 with shadow 0.75 through
  `RenderCoin`. The 256x256 faces map the sheet's opaque 512x267 region.
- **Correction to the contract above:** the original sets `mirror = true`
  AFTER `addBox`; 1.7.10's `ModelBox` reads the flag in its constructor, so
  the quad is NOT mirrored. The parser encodes exactly that ordering rule.
- **Resolution:** FIXED (2026-09-02, own commit, owner's go). `ModelCoin` is
  the original quad (`addBox(-128, -128, 0, 256, 256, 1)` at `texOffs(0, 0)`,
  pivot `(0, -109, 0)`, sheet 512x512, not mirrored) with the original yaw
  `Mth.cos(ageInTicks * 0.05F * 0.22F) * PI`; `CoinRenderer` scales by 0.125
  in `scale()` (the 1.7.10 `preRenderCallback` slot) with shadow
  `0.75 * 0.125`. INDEPENDENT LEG: `tools/reference_geometry_leg.py` parses
  the decompiled 1.7.10 `ModelCoin.java` (never the port) and compares it
  with the compiled port dump; result PASS, 1 part
  matched (reference box origin [-128.0, -128.0, 0.0], size [256, 256, 1],
  uv [0, 0], mirror False, rotation point
  [0.0, -109.0, 0.0]). Wired as `s4ReferenceGeometry` before `s4Parity`,
  folded into the proof (`phase_g_reports/s4_proof/reference/`). The GeckoLib
  candidate `CoinGeoReplacement` (descriptor scale/shadow hooks, code-driven
  yaw) is back behind the switch, proven: geometry 0
  blocks, surface 168 vertex-samples exact,
  animation 0 rad over a full 571.2-tick
  oscillation, visual changed 0 (foreground now
  present on both sides), excluded pin 0.
- **Population question (owner):** answered in FIX_LOG "REFERENCE-GEOMETRY
  SURVEY" once the whole-population run completes.

### BUG-041 — 82 port models mirror their cubes; the 1.7.10 originals set `mirror` after `addBox`, where it is inert (REPORT, 2026-09-02)

- **Impact:** MEDIUM-visual, PORT-WIDE — every affected face samples its
  texture flipped horizontally relative to 1.7.10. Invisible on symmetric
  texels, visible wherever a texture is asymmetric (eyes, markings, text).
- **Evidence:** `phase_g_reports/reference_geometry_survey.md`. The 1.7.10
  Techne export order is `new ModelRenderer(this, u, v); addBox(...);
  setRotationPoint(...); setTextureSize(w, h); mirror = true; setRotation(...)`.
  `ModelBox` captures the part's mirror flag (and texture size) in its
  constructor, so both trailing assignments never reach the box. The port
  translated `mirror = true` into `CubeListBuilder.mirror()` before `addBox`,
  where it is effective. 82 of 87 surveyed models; 78 differ in
  nothing else; Kyuubi and Coin are exact.
- **Law 11 CLOSED (2026-09-02, owner's go):** no 1.7.10 jar exists under
  Prism; Mojang's official client was fetched through the version manifest
  (`https://piston-meta.mojang.com/mc/game/version_manifest_v2.json` -> `1.7.10.json`
  -> `https://launcher.mojang.com/v1/objects/e80d9b3bf5085002218d4be59e668bac718abbc6/client.jar`), SHA-1 `e80d9b3bf5085002218d4be59e668bac718abbc6` verified over
  5256245 bytes. Bytecode (obfuscated names): `ModelBox` is `bis`
  (constructor `(bix, int, int, float, float, float, int, int, int, float)`);
  `ModelRenderer` is `bix`; `ModelBase` is `bhr`. `bix.<init>(bhr, String)`
  copies the ModelBase's `t`/`u` (textureWidth/Height) into `bix.a`/`b` at
  construction; `bix.a(FFFIII)` (addBox) does
  `new bis(this, this.r, this.s, x, y, z, w, h, d, 0.0f)` passing only the
  texture offsets; `bis.<init>` itself reads `bix.i` (the mirror boolean) and
  `bix.a`/`bix.b` (the texture size) and bakes the vertices from them;
  `bix.b(II)` (setTextureSize) only stores `a`/`b`. Therefore `mirror = true`
  and `setTextureSize` executed after `addBox` never reach an existing box.
  The Techne export order the 3,542 reference stores follow is exactly that.
- **A/B model for the owner (before the mass change lands):** EnderReaper,
  the most asymmetric texture among the mirror-only models (mean texel
  difference against its own horizontal mirror 0.297 over 2,243 face texel
  pairs; next Fairy 0.180, Bee 0.137). Its port `ModelEnderReaper` carries
  66 `.mirror()` calls; the single-model drop lands in its own commit with
  the reference leg as proof; the owner compares the release jar (mirrored)
  against the new build in-game.
- **Resolution:** OPEN — report only. Proposed: one commit dropping
  `.mirror()` on every cube whose reference box was unmirrored (mechanical,
  from the survey JSON), the Phase G proofs regenerated (every proven species
  changes), and `reference_source` declared for all 78 so the leg pins them.
  Alternatively an owner MOD ruling that the port keeps the flipped mapping,
  recorded per model in the manifest. Owner's call.

### ENT-S-091 — Seven port models diverge from their 1.7.10 geometry beyond the mirror flag (REPORT, 2026-09-02)

- **Models:** CaterKiller (49 extra parts), Elevator (all pivots y 0 -> 24),
  Island and IslandToo (port model is a different rig), SeaViper (rebuilt:
  pivots and rotation axes), Skate (different rig), StinkBug (all 50 pivots,
  the reference's `+= 6.0f` adjustments). Details and readings in
  `phase_g_reports/reference_geometry_survey.md`.
- **Owner ruling (2026-09-02):** parity bugs; fix in classic with the
  reference leg as proof; Elevator's Q1 acceptance void; Island and Elevator
  re-prove and re-accept after.
- **Slice A, FIXED:** Island, IslandToo, Skate, Mosquito, Ghost and StinkBug
  rigs regenerated from the parsed 1.7.10 constructors
  (`tools/reference_to_layer_definition.py`), animations transcribed from
  the originals' render bodies (Island: nine cosines on three tumbling
  cubes, wingspeed 1.0; Skate: tail-tip flap; Mosquito: wing flap at 3.0;
  Ghost: four arm cosines), renderer scale and shadow set to the verified
  ENT-S-092 values (Skate 0.75/0.075, Mosquito 0.5/0.15, Ghost 0.65/0,
  StinkBug 0.85/0.2975), reference pins cleared so the leg requires an exact
  match; the Island and IslandToo GeckoLib candidates re-proven on the
  regenerated rigs (s4 proofs rewritten; both need the owner's re-acceptance).
- **Slice B, FIXED / RESOLVED (reads refuted once each, upheld):**
  - Elevator: EQUIVALENT RE-EXPRESSION, not a divergence (the living-only
    behaviours both paths inherited are ENT-S-094, fixed 2026-09-03). The 1.7.10
    RenderElevator is a plain Render (translate, yaw, hit wobble, a scale
    immediately undone, the (-1,-1,1) flip, no 24 px lift); the port's +24 px
    pivot bake (TF-029) cancelled the MobRenderer lift exactly. Made
    leg-exact without changing what renders: pivots restored to the
    original's 0, the vanilla lift cancelled in `ElevatorRenderer.scale()`
    (+1.501 in the flipped frame: exact cancellation), and the GeckoLib
    candidate translated 1.5 + 0.01 down after `applyRotations` (the
    converter puts a classic pivot 0 at geo y 24; GeckoLib's own 0.01 lift is
    issued right after applyRotations, bytecode 727 versus 396). The read's
    claim that the geo file would not change was wrong and is corrected
    here; the s4 proof is rewritten and Elevator re-proven. Re-acceptance
    requested (the owner voided the Q1 acceptance).
  - CaterKiller: EQUIVALENT RE-EXPRESSION. The original poses and draws the
    seven seg1 parts three times and the seven seg2 parts six times; the port
    materialises one ModelPart per draw (2x7 + 5x7 = 49 extra). The leg now
    takes `unrolled_parts` (multiplicity per reference part) and checks every
    copy for the reference's boxes and constructor placement and the exact
    arity (`UNROLL_ARITY` otherwise); the per-copy pivots and the seg2 phase
    accumulator, which the leg cannot see, are pinned by the read as
    `motion_read` in the manifest until CaterKiller enters Phase G. Its pin
    is now BUG-041's mirror only.
  - Tshirt: DIVERGES, FIXED. 1.7.10 drew a spinning two-quad advert (256x64
    banner and 128x128 body pivoting at (0, -128, 0)) with a DECLARED 512x256
    sheet over the 320x160 image, i.e. a 0.625 UV window, and a 0.33 render
    scale; the port had re-authored a three-part 320x160 rig at scale 1 with
    shadow 2.0. Restored, declared sheet kept over the same image (the Coin
    precedent), renderer SCALE 0.33 / SHADOW 0.33.
  - SeaViper: FIXED (slice C). Rig regenerated from the parsed constructor
    including the original's trailing +32 z shifts on all 34 parts (the port
    had dropped them, sitting two blocks forward of the hitbox); animation
    transcribed line for line from the original's render body and refuted by
    two independent readers with no defects. Defect found in the old port on
    the way: `field_82907_q` is `ModelRenderer.offsetZ` (proven from the
    1.7.10 bytecode: `glTranslatef(o, p, q)` before the rotation-point
    translate, unscaled), a pre-rotation translation in block units; the port
    had written it into `zRot`. It is folded into `z` as
    `initialPose.z + offsetZ * 16`, exact since both are parent-frame
    translations before the rotation. Reference pin cleared; exact required.

### ENT-S-094 — Elevator inherits living-renderer behaviour the 1.7.10 non-living Render never had (REPORT, 2026-09-02)

- **Found by the ENT-S-091 Elevator read:** the 1.7.10 Elevator entity extends
  EntityLiving but was drawn by a plain `Render` (`RenderElevator.java:19`),
  so it had no death Z-flip, no shaking/sleeping/upside-down branches and no
  hurt red overlay. Both port paths (`ElevatorRenderer` on MobRenderer;
  the GeckoLib candidate on GeoReplacedEntityRenderer) inherit all of them
  (`LivingEntityRenderer.setupRotations` and `getOverlayCoords`;
  `GeoReplacedEntityRenderer.applyRotations` death flip).
- **Resolution:** FIXED (2026-09-03, owner: "parity bug, fix in classic; the seam's
  base gets a per-species non-living mode so the candidate matches"). Classic
  `ElevatorRenderer`: `setupRotations` no longer calls the living version (no
  shaking jitter, no sleeping branch, no death Z-flip, no upside-down check),
  applying only `180 - yaw` plus the original hit wobble; the yaw is the ENTITY
  yaw lerped as the 1.7.10 RenderManager passed it, not the body yaw; the hurt
  red overlay is removed by rendering the model with `OverlayTexture.NO_OVERLAY`
  (`getOverlayCoords` is static and cannot be overridden); no name tag (the plain
  1.7.10 Render drew none). The leash line stays on BOTH paths: EntityRenderer's
  renderLeash is private and reachable only from render(), so it cannot be
  dropped short of copying LivingEntityRenderer.render (disclosed; options in
  FIX_LOG). GeckoLib seam: the descriptor
  interface gains `nonLivingRender()` (default false); the seam base consults it in
  `applyRotations` (yaw only, same entity-yaw lerp), `getPackedOverlay`
  (NO_OVERLAY) and `shouldShowName`; `ElevatorGeoReplacement` returns true. Every
  other species is unchanged. Bytecode-proven override points (LivingEntityRenderer
  1.21.1, GeoReplacedEntityRenderer 4.8.4) and both refuters upheld; residuals
  disclosed: invisibility still gates the render type on both paths (no hook short
  of copying render()), shadow radius keeps the engine's scale/age multipliers
  (0.25 at defaults, same as 1.7.10's render-size modifier). No gametest can see
  a renderer; the s4 visual leg re-proves both paths agree.
- **OWNER ACCEPTANCE (2026-09-03):** "residuals accepted and recorded. No name tag
  on the board." The leash line on both paths, the invisibility render-type
  gate and the engine's shadow multipliers stay as disclosed; `shouldShowName`
  returns false on both paths by ruling. CLOSED.
  Follow-up (refuted once, upheld): both paths draw at Mth.lerp(partialTicks, yRotO, getYRot()), the exact 1.7.10 RenderManager formula (bytecode: prev + (cur - prev) * partial, no wrap; the port's LivingEntity.tick keeps yRot - yRotO within 180 so rotLerp could not differ), instead of the lerped body yaw.

### ENT-S-092 — Renderer shadow radius and world scale diverge from the 1.7.10 registrations across the population (REPORT, 2026-09-02)

- **Evidence:** `phase_g_reports/renderer_sweep.json` (`tools/reference_renderer_sweep.py`).
  1.7.10 `RenderX(model, par2, par3)` passes `par2 * par3` as the shadow
  radius and scales by `par3`; the port renderers mostly pass `par2` alone
  (or a retuned literal) and omit the `par3` scale. Shadow differs in
  85 of 97 resolvable pairs (cosmetic); world scale differs in about
  41-48 (visible: e.g. Brutalfly 9.0 -> 1.0, Kraken 1.0 -> 3.0,
  Robot3 0.5 -> 1.0, Hydrolisc 0.65 -> 1.0, Fairy 0.35 -> 1.0, Irukandji
  0.25 -> 1.0, Peacock 1.0 -> 0.5). Coin's 0.125 was the first of these
  fixed (BUG-040).
- **METHOD VERIFIED (2026-09-02, owner's condition):** 49 renderers read
  against every scale path (renderer hooks, `Attributes.SCALE` repo-wide,
  EntityType, `getScale`/baby, model-level sizes, MHLib, config, mixins),
  each refuted once or twice; 61 of 63 refutations failed. World scale
  DIVERGES in 44 of 49 (largest: Brutalfly 9 -> 1, Kraken 1 -> 3,
  SeaMonster 1 -> 3, Robot3 0.5 -> 1, Tshirt 0.33 -> 1, Irukandji 0.25 -> 1,
  Fairy 0.35 -> 1, TheKing 2.1 -> 1, Hammerhead 2.5 -> 2, Godzilla 2 -> 3);
  matches via another path in 4 (Camarasaurus, EasterBunny, Mothra,
  Peacock) and outright in 1 (Basilisk). Shadow DIVERGES in 48 of 49.
  The sweep's shadow column held on every verified row; its scale column
  produced 5 false positives, so scale flags on the unread 47 shadow-only
  rows are not counted. Per-renderer findings with the exact fix and pin:
  `phase_g_reports/renderer_findings.md` (+ `.json`).
- **Resolution:** IN PROGRESS, BATCH 1a LANDED (2026-09-03, owner: "go, in
  batches; MOD-recorded renderers keep their values, the rest restore 1.7.10; pin
  renderer scale and shadow in the reference gate"). Truth table: all 133
  registrations read against the 1.7.10 RenderX constructor and
  preRenderCallback and every port scale path, refuted per chunk (84 diverge:
  44 in world scale, 84 in shadow; no MOD record covers any renderer).
  Batch 1a: 38 renderer files restored in the CoinRenderer house style
  (SCALE/SHADOW constants with the registration citation, shadow passed to
  super, scale applied in the `scale()` hook, the original's baby branches
  transcribed; Robot3 and Beaver GeckoLib descriptors matched): Brutalfly, Irukandji, Fairy, Cricket, Hydrolisc, Robot3, Beaver, Dragonfly, EmperorScorpion, ThePrinceTeen, Firefly, VelocityRaptor, Spyro, Cockateil, Scorpion, CaveFisher, SpitBug, CreepingHorror, TerribleTerror, Rat, RubberDucky, Urchin, CaterKiller, Hammerhead, LurkingTerror, GammaMetroid, AttackSquid, Chipmunk, Bee, Alien, TrooperBug, Mantis, HerculesBeetle, GhostSkelly, WaterDragon, Lizard, Flounder, PurplePower.
  Pin: `tools/reference_renderer_pins.py` + `tools/reference_renderer_pins.json`
  (re-parses the 1.7.10 registrations and constructors, checks the port constants
  and their use, statuses PASS/DIVERGES/PENDING/MOD/NOT_APPLICABLE/MANIFEST_DRIFT),
  wired as `referenceRenderers` under `check`. HELD for the owner (batch 1b):
  Kraken 3 -> 1, SeaMonster 3 -> 1, TheKing 1 -> 2.1, TheQueen 2 -> 1 (MHLib
  bone-tracked hit surfaces follow the rendered scale), Godzilla 3 -> 2 (MOD-025
  bone-synced hitboxes); their hit surfaces change with the render scale, so the
  consequences are presented before landing. NEXT: batch 2, the 40 shadow-only
  renderers. BATCH 2 LANDED (2026-09-03): the 40 shadow-only renderers carry
  `SHADOW` as the 1.7.10 product (Robot1/2/4, Vortex and RockBase descriptors
  matched; RockBase's shadow is the original's 0), refuted per chunk; the pin
  manifest holds 114 pins, 13 not-applicable and the five bosses pending. Crab
  and PitchBlack pin their shadow and declare a DYNAMIC scale axis (both sides
  scale by an entity getter). BATCH 1b LANDED (2026-09-03) for the four bosses
  whose hit surfaces do not follow the render scale (each boss read refuted
  once): Kraken 3 -> 1 (plain 4x15 AABB, grab is coordinate math), SeaMonster
  3 -> 1 (plain 5x5 AABB), TheKing 1 -> 2.1 (OreSpawnPartEntity parts are
  positioned by code offsets, not by the rendered bones), Godzilla 3 -> 2 (same;
  MOD-025 covers hitboxes, not render scale); the PlayNicely quarter-scale
  branches of TheKing/Godzilla transcribed. HELD: TheQueen 1 -> 2 (coupling
  PARTIAL): her MHLib bone-tracked parts follow the drawn geometry only when the
  scale is applied after GeckoLib's capture, so restoring the 1.7.10 size also
  relocates and resizes every head/tail/wing/leg hit surface onto the drawn
  body; the read's edit reorders the pose scale after the capture and keeps the
  hand-authored profile envelopes. Presented for the owner; the pin manifest
  keeps TheQueen PENDING. Kraken's 1.7.10 PlayNicely mode (1/3 scale paired
  with a 1.33x5 hitbox) does not exist in the port at all: filed as ENT-S-096.
  Recheck list: `phase_g_reports/ents092_recheck_list.md`; changelog
  note: `phase_g_reports/ents092_changelog_note.md`. Hitbox dimension
  divergences found on the way are filed as ENT-S-095.

### ENT-S-093 — Motion: per-entity selector/filter state collapsed into model-instance fields in 14 ports; sampled formula divergences (REPORT, 2026-09-02)

- **Evidence:** the reader survey (24 sampled pairs; details in
  `phase_g_reports/reference_geometry_survey.md`). Alien and Cephadrome are
  the confirmed cases: 1.7.10 keeps the reroll/latch state per entity
  (`RenderInfo`) and the port keeps it in the model instance, so every mob of
  that species on screen shares one latch and one filter. Twelve more ports
  have the same shape (EmperorScorpion, GhostSkelly, Leon, LurkingTerror,
  CaveFisher, Dragon, DungeonBeast, Nastysaurus, PitchBlack, ThePrinceTeen,
  Ostrich, Scorpion). Other sampled divergences: RubberDucky head/beak
  damping, Gazelle predicate, Cephadrome yaw source (body -> head), Mosquito
  and PurplePower rebuilt.
- **SPLIT PRESENTED (2026-09-02, per owner: "classify per model whether the
  original kept the same shared state; faithful cases stay"):** all FOURTEEN
  are PARITY BUGS — in every 1.7.10 original the latch/filter state lived in
  a per-entity `RenderInfo` (`renderdata` field, `getRenderInfo()` /
  `setRenderInfo()`), read and written by the model's render; none kept it
  on the ModelBase instance. Fourteen classifiers, 28 refutations: 26 failed,
  2 corrected evidence only (one notes the port's re-zeroed filter is dead
  code rather than shared state; still a parity bug). No faithful case.
  Additional divergences found while reading, per model:
  - Alien: PARITY_BUG_PER_ENTITY_LOST; reference state PER_ENTITY_RENDERINFO. Other divergences: Motion formulas themselves are a faithful 1:1 transcription — I found no amplitude/frequency/phase divergence. Verified matching pairs: leg swing `cos(ageInTicks*4*wingspeed)*PI*0.5*limbSwingAmount` (port ModelAlien.java:361 == ref ModelAlien.java:415); idle fan pose xRot=-1.85/zRot=0 (port :365-394 == ref :419-448); attacking fan phase ladder `cos(ageInTicks*1.22*wingspeed - N*0.5235988)*PI*0.1` for N=0..7 plus the 
  - CaveFisher: PARITY_BUG_PER_ENTITY_LOST; reference state PER_ENTITY_RENDERINFO. Other divergences: TWO further divergences, one of them severe:  (1) SEVERE — doLeftClaw/doRightClaw rotate the WRONG AXIS, on the WRONG PART SET, with the WRONG offsets and no abs(). Port C:\Homework\Projects\Orespawn\src\main\java\danger\orespawn\entity\client\ModelCaveFisher.java:540-550:   doLeftClaw: `LeftClawBase.yRot = -0.3f + angle; LeftClawTop.yRot = -0.3f + angle * 1.5f; LeftClawLow.yRot = -0.3f - angle;`   doRightClaw: `Righ
  - Cephadrome: PARITY_BUG_PER_ENTITY_LOST; reference state PER_ENTITY_RENDERINFO. Other divergences: One further divergence, in the same head-tracking block — the YAW SOURCE was swapped from body yaw to head yaw.  - Reference ModelCephadrome.java:470 — `f3 = (e.field_70126_B - e.field_70177_z) * 10.0f;`   `field_70126_B` = `prevRotationYaw`, `field_70177_z` = `rotationYaw` → this is the BODY yaw delta over the last tick. - Port ModelCephadrome.java:383 — `headYaw = (entity.yHeadRotO - entity.yHeadRot) * 10.0f;`   `y
  - Dragon: PARITY_BUG_PER_ENTITY_LOST; reference state PER_ENTITY_RENDERINFO. Other divergences: Two further setupAnim-vs-render formula divergences, both in C:\Homework\Projects\Orespawn\src\main\java\danger\orespawn\entity\client\ModelDragon.java:  (1) Head-yaw source term. Port :568 `headYaw = (entity.yHeadRotO - entity.yBodyRot) * 8.0F;` vs reference ModelDragon.java:539 `f3 = (e.field_70126_B - e.field_70177_z) * 8.0f;`. In 1.7.10 field_70126_B is Entity.prevRotationYaw and field_70177_z is Entity.rotationY
  - DungeonBeast: PARITY_BUG_PER_ENTITY_LOST; reference state PER_ENTITY_RENDERINFO. Other divergences: 1) MISSING 90-DEGREE YAW IN renderToBuffer (likely visible orientation bug). Reference ModelDungeonBeast.java:574 applies `GL11.glRotatef(90.0f, 0.0f, 1.0f, 0.0f)` immediately before rendering the part list (:575-634). The port's renderToBuffer (C:\Homework\Projects\Orespawn\src\main\java\danger\orespawn\entity\client\ModelDungeonBeast.java:511-572) applies no PoseStack rotation at all, and the geometry was NOT rebak
  - EmperorScorpion: PARITY_BUG_PER_ENTITY_LOST; reference state PER_ENTITY_RENDERINFO. Other divergences: 1) RNG source divergence (minor, same site): reference draws from the WORLD random — ModelEmperorScorpion.java:619-623 `e.field_70170_p.field_73012_v.nextInt(...)` — while the port draws from the ENTITY random, EmperorScorpionModel.java:619-623 `entity.getRandom().nextInt(...)`. Same ranges (20/25 idle, 4/3 attacking), so distribution is unchanged; only the stream differs. 2) Everything else is 1:1. wingspeed is fait
  - GhostSkelly: PARITY_BUG_PER_ENTITY_LOST; reference state PER_ENTITY_RENDERINFO. Other divergences: 1) RNG source change (cosmetic, matches project convention). Original ModelGhostSkelly.java:113 rolls off the WORLD RNG: `e.field_70170_p.field_73012_v.nextInt(3)`. Port GhostSkellyModel.java:113 rolls off the ENTITY RNG: `entity.getRandom().nextInt(3)`. Same 1-in-3 distribution, different stream. The port makes the identical substitution in the faithful Kraken port (ModelKraken.java:621-626), so this reads as an acc
  - Leon: PARITY_BUG_PER_ENTITY_LOST; reference state PER_ENTITY_RENDERINFO. Other divergences: Yaw-source divergence at the same site: orig ModelLeon.java:1014 computes `f3 = (e.field_70126_B - e.field_70177_z) * 8.0f` — field_70126_B is prevRenderYawOffset (yBodyRotO) and field_70177_z is rotationYaw (yRot / getYRot()). The port at LeonModel.java:1006 writes `netHeadYaw = (entity.yBodyRotO - entity.yBodyRot) * 8.0f`, substituting renderYawOffset (yBodyRot) for rotationYaw. yBodyRotO - yBodyRot is a body-rotat
  - LurkingTerror: PARITY_BUG_PER_ENTITY_LOST; reference state PER_ENTITY_RENDERINFO. Other divergences: Two, both in C:\Homework\Projects\Orespawn\src\main\java\danger\orespawn\entity\client\LurkingTerrorModel.java. (1) RNG source swap: the original drew from the world RNG `e.field_70170_p.field_73012_v.nextInt(...)` (ModelLurkingTerror.java:448, 451, 454, 457, 460, 463, 471), the port draws from the entity RNG `entity.getRandom().nextInt(...)` (LurkingTerrorModel.java:450, 453, 456, 459, 462, 465, 473). This is a deli
  - Nastysaurus: PARITY_BUG_PER_ENTITY_LOST; reference state PER_ENTITY_RENDERINFO. Other divergences: No other motion-formula divergence found; every other expression is a faithful 1:1 port. Verified line by line: head yaw chain (orig :453-473 vs port :402-422), attacking-branch jaw `Mth.cos(ageInTicks*0.85f*wingspeed)*PI*0.16f + 0.5f` (orig :475-476 vs port :425-426), idle-branch sin variant (orig :487-488 vs port :438-439), the `pi4 / 4.0f` fallback (orig :490 vs port :441), left-leg/claw block incl. the `-0.523f/-
  - Ostrich: PARITY_BUG_PER_ENTITY_LOST; reference state PER_ENTITY_RENDERINFO. Other divergences: Four further setupAnim-vs-original divergences, in rough severity order:  1. Hat gating lost entirely (visible bug). Original ModelOstrich.java:420-425 rendered Hat1 only when `o instanceof EntityCannonFodder && o.get_is_activated() != 0`, and Hat2 only when `o.get_is_activated() > 1`; `is_activated` defaults to 0 (EntityCannonFodder.java:36, accessor EntityCannonFodder.java:228-230), so a normal ostrich showed no ha
  - PitchBlack: PARITY_BUG_PER_ENTITY_LOST; reference state PER_ENTITY_RENDERINFO. Other divergences: No formula divergence. I normalized the reference render body (ModelPitchBlack.java:742-1037, obf field/method names mapped to Mojmap: field_78795_f→xRot, field_78796_g→yRot, field_78808_h→zRot, field_78800_c→x, field_78797_d→y, field_78798_e→z, func_76134_b→Mth.cos, func_76126_a→Mth.sin, f1→limbSwingAmount, f2→ageInTicks, f3→netHeadYaw) and diffed it against the port setupAnim body (ModelPitchBlack.java:642-937) wit
  - Scorpion: PARITY_BUG_PER_ENTITY_LOST; reference state PER_ENTITY_RENDERINFO. Other divergences: Three divergences beyond the shared-state issue.  (1) CONFIRMED — missing 0.75 model scale. Reference ClientProxyOreSpawn.java:433 registers `new RenderScorpion(new ModelScorpion(0.62f), 0.35f, 0.75f)`; RenderScorpion.java:22-25 stores par3 (0.75f) as `this.scale`, and RenderScorpion.java:39-45 applies it every frame via `GL11.glScalef(this.scale, this.scale, this.scale)` in the func_77041_b/preRenderScale hook. The 
  - ThePrinceTeen: PARITY_BUG_PER_ENTITY_LOST; reference state PER_ENTITY_RENDERINFO. Other divergences: 1) Yaw source changed from BODY yaw to HEAD yaw in the activity==1 (flight) branch. Reference ModelThePrinceTeen.java:669 uses `f3 = (c.field_70126_B - c.field_70177_z) * 10.0f;` — field_70126_B is Entity.prevRotationYaw and field_70177_z is Entity.rotationYaw, i.e. the BODY yaw delta. Port ModelThePrinceTeen.java:682 uses `yaw = (entity.yHeadRotO - entity.yHeadRot) * 10.0f;` — yHeadRotO/yHeadRot are the 1.7.10 field
- **Resolution:** FIXED (2026-09-03, owner: "go; per-entity state restored as the
  originals had it; the formula divergences use the SeaViper standard"). All 14
  species carry a per-entity `RenderInfo` again (Kraken pattern: `renderInfo`
  field + `getRenderInfo()` with the orig citations; models read and write it at
  the original's sites; the shared model-instance fields are gone). Formula
  divergences transcribed line for line, each draft upheld by two independent
  refuters before install: CaveFisher (1), Cephadrome (1), Dragon (2), DungeonBeast (1), Leon (1), Ostrich (3), ThePrinceTeen (1). Pose interfaces added for
  CaveFisher, Ostrich and PitchBlack (Slice 4b shape) so a future candidate or
  entity_state leg can drive them; `ProbeSubject` is not yet wired to them (no
  consumer exists). Gametest `RenderInfoParityTests` pins, per species plus a
  Kraken control, distinct non-null holders per entity, all-zero on spawn and
  independence across ticks. World-RNG versus entity-RNG stays (convention).
  Out of scope, noted by the drafts: GhostSkelly's constant 0.25-alpha tint
  (orig ModelGhostSkelly.java:123-127) versus the port's pass-through colour.


### ENT-S-095 — Entity hitbox dimensions diverge from the 1.7.10 `setSize` across the population (REPORT, 2026-09-03)

- **Evidence:** found in passing by the ENT-S-092 renderer reads (every scale path
  per entity, refuted per chunk). 1.7.10 renderer scale never touched the hitbox,
  so these are independent of the renderer fix. 51 entities with a numeric
  pair plus the batch-1a fixers' additional citations are tabulated in
  `phase_g_reports/hitbox_dims_findings.md` (+ `.json`); ~35 more notes mention a
  difference without a pair and need a read before they count. Largest: SeaMonster
  1.25x2.5 -> 5x5, Tshirt 4x4 -> 0.6x1.8, Urchin 1.35x2.1 -> 0.5x0.5, TrooperBug
  3x3.5 -> 1.2x1.5, Mantis 2.5x3.25 -> 0.8x1.8, HerculesBeetle 3.25x2.75 -> 1.2x1,
  EmperorScorpion 3.5x3 -> 1.5x1.5, the four ants 0.1x0.1 -> 0.4x0.4.
- **SPLIT PRESENTED + BATCH 1 LANDED (2026-09-03, owner: "MOD-recorded dims stay,
  the rest are parity bugs. Fix in batches; MHLib main size in lockstep; every
  change gets a both-modes dims-pin test"):** the full population sweep
  (`phase_g_reports/ents095_split.md` + `.json`, 145 registrations, 22 rows
  hand-verified) finds 67 divergent, NONE MOD-recorded, 66 parity bugs plus
  cannon_fodder, which 1.7.10 never registered (owner ruling). Batch 1 (the 63
  plain registrations: no MHLib profile, no PlayNicely size branch, no part
  entities) is restored to the 1.7.10 `setSize` literals in `ModEntities`
  with an orig citation per line (jeffery follows GiantRobot, RubyBird follows
  Cockateil; the four cows follow the vanilla 1.7.10 EntityCow constructor,
  confirmed 0.9 x 1.3 from the Mojang jar), refuted once, and pinned by
  `HitboxDimsParityTests` (63 tests, own batch: each entity spawned
  with PlayNicely off and on, width, height and live AABB asserted to 1e-4).
  Batch 2 pending: Godzilla 9.9 -> 10.0 (port registers 10.0 against its own
  9.9 comment) and Mothra 5x2 -> 6x3 (a source comment, not a MOD record: fix
  or write the record). Batch 3 pending: TheQueen, whose Java dims (16x12 /
  5.5x6) are overridden by MHLib's EntityEvent.Size hook to the profile main
  size 22x24 in BOTH modes, so the 1.7.10 PlayNicely 5.5x6 is dead; fix under
  the main-size law with a runtime both-modes pin. Left for rulings: the
  port-only apple_cow / golden_apple_cow (MOD-021) at 0.9x1.4 versus the cow
  line's 0.9x1.3; BetterFireball.setSmall() no longer shrinks the box to
  0.3125 (orig BetterFireball.java:84); crab / pitch_black / girlfriend are
  dynamic and match at steady state (PitchBlack's 10x14 construction box
  before finalizeSpawn is a spawn-fit divergence worth a separate look).
  RULING WANTED: red_ant and termite extend EntityAnt, whose constructor
  already called setSize(0.1, 0.1); 1.7.10 EntityAgeable.setSize is final and
  defers a second constructor call (proven from the Mojang jar, class rx), so
  the server box stayed 0.1x0.1 until an age change or reload while the
  class literal 0.2x0.2 was the client and persisted box. Batch 1 pins the
  class literal 0.2x0.2; say if the construction-time 0.1x0.1 is wanted.
- **BATCH 2 + RULINGS LANDED (2026-09-03):** Godzilla restores 9.9x25 (orig
  Godzilla.java:72; the PlayNicely quarter 2.475x6.25, orig :74, kept; both modes
  pinned in `BossDimsPlayNicelyTests`). Mothra restores 5x2 (orig Mothra.java:65).
  The port's source comment, verbatim:
  ```
    // 1.7.10 func_70105_a: Mothra = 5.0 x 2.0. We bump to 6 x 3 so the
    // wing PartEntities (which extend +/-6 sideways) read correctly against
    // the root hitbox during cross-biome target sweeps.
  ```
  It states a reason, so MOD-029 is filed as a proposal for a config-gated modern
  6x3 with that reason; the reason has no code behind it (the four part entities
  are placed from the root position and size themselves; only the hunt sweep
  and the natural-spawn gate inflate the root box, and both now match 1.7.10).
  Rulings recorded: red ant and termite pin 0.2x0.2; the 0.1x0.1 was a 1.7.10
  EntityAgeable constructor-ordering transient (the final setSize deferred the
  second constructor call until an age change) deliberately not reproduced.
  Apple cow and golden apple cow align to 0.9x1.3: their only recorded reason
  for 1.4 was "mirror the existing OreSpawn cow line" (MOD-021 says nothing
  about size), and that line is the vanilla 1.7.10 EntityCow 0.9x1.3.
  BetterFireball.setSmall() shrinks the box to 0.3125 again (orig
  BetterFireball.java:84) through the non-living `Entity#getDimensions` hook plus
  refreshDimensions, pinned. cannon_fodder stands port-only at 0.6x0.6: 1.7.10
  never registered EntityCannonFodder, so no parity target exists; documented
  and pinned so it cannot drift. Refuted once. Batch 3 (TheQueen) lands with
  the ENT-S-092 Queen restore.

### ENT-S-096 — Kraken has no PlayNicely mode (REPORT, 2026-09-03)

- **Evidence:** orig Kraken.java:70-76 sizes the boss 4x15 normally and 1.333x5
  while `OreSpawnMain.PlayNicely != 0`, and RenderKraken.java:39-45 draws it at
  scale/3 in that mode; the port Kraken has no PlayNicely accessor (grep), a fixed
  4x15 EntityType and no scale branch (found by the ENT-S-092 boss read, refuted
  once). TheKing and Godzilla carry their PlayNicely branches (BOSS-017).
- **Resolution:** FIXED (2026-09-03, owner: "go, with a gametest on the PlayNicely
  hitbox"). Kraken follows the BOSS-017 King/Godzilla pattern exactly: a
  constructor-time snapshot of `OreSpawnConfig.PLAY_NICELY` selects
  `getDefaultDimensions` 1.3333334x5 (orig Kraken.java:75, the original's float)
  or 4x15 (orig :73) and, like the original's constructor-only setSize, never
  resizes afterwards; a synched datum re-set every AI step from the live flag
  (orig :97, :914) drives `KrakenRenderer.scale()` at SCALE / 3 while nice (orig
  RenderKraken.java:39-45). Four gametests in their own batches
  (`KrakenPlayNicelyTests`: 4x15 off, 1.3333334x5 on, no resize on a live flip,
  datum tracks the flag). Refuted once. The renderer pin leg reads the ternary's
  default branch as SCALE 1.0. The 1.7.10 Kraken also gated four behaviours on
  PlayNicely; those are ENT-S-097.

### ENT-S-097 — Kraken's four behavioural PlayNicely gates are missing (REPORT, 2026-09-03)

- **Evidence:** found while porting ENT-S-096. In 1.7.10, while `PlayNicely != 0`,
  the Kraken skips the thunderstorm-summoning timer (orig Kraken.java:171), the
  random lightning bolt (:915), the prey search and grab (:961, :975-980) and
  `findSomethingToAttack` returns null (:1131-1133). The port's weather block,
  lightning roll, `searchForPrey()` call and `findSomethingToAttack()` carry no
  gate. No MOD record covers them.
- **Resolution:** FIXED (2026-09-03, owner: "go on all four gates, each with its own
  batched gametest under PlayNicely on and off"). All four transcribed without
  ambiguity, reading the LIVE config at each site as the 1.7.10 static and the
  King's BOSS-017 gates do (not the constructor snapshot, not the synched
  datum): the weather block is wrapped whole so the countdown freezes while
  nice (orig :171-186); the lightning roll is consumed before the flag vetoes
  it (orig :915); the prey call site gates grab and fallback together since
  `searchForPrey()` is the whole orig :961-982 branch; `findSomethingToAttack`
  returns null first (orig :1131-1133). `KrakenPlayNicelyGateTests`: four tests
  in four batches (weather timer frozen then resumed; forced-roll lightning
  vetoed then fired; forced-roll prey search skipped then caught; target search
  null then found), each restoring the flag and the weather on every path.
  Refuted once. Two pre-existing Kraken divergences seen on the way and left
  for the ledger: the port's nearest-player choice skips creative players where
  1.7.10 took the nearest of any mode and nulled a creative one, and
  `isSuitableTarget` keeps fewer exclusions than orig :1060-1128. The gametest
  server runs with doWeatherCycle=false, so any future long-lived Kraken test
  must restore the weather (TEST-003 checklist).

### ENT-S-098 — Shot BetterFireballs carry the vanilla fireball EntityType (REPORT, 2026-09-03)

- **Evidence:** found by the ENT-S-095 batch-2 refuter. Every shooter builds shots with
  `new BetterFireball(level, shooter, accel)` (17 sites: Mothra, Godzilla, TheKing,
  Dragon, ThePrince, ThePrincess, ...), whose constructor chains to
  `LargeFireball(level, shooter, movement, 1)` = `super(EntityType.FIREBALL, ...)`, so a
  shot BetterFireball is typed `minecraft:fireball`: the port's `better_fireball`
  registration (its `.sized`, `noSummon`, its renderer binding) governs only
  `EntityType#create` instances, clients construct shots as vanilla LargeFireballs,
  and NBT saves them as `minecraft:fireball`. The 0.3125 shrink still applies to
  shots (class-level override), but the renderer and registration do not.
- **Resolution:** OPEN — report only. Fix would be a constructor that passes the
  mod's own EntityType (as the (EntityType, Level) constructor does) plus a pin
  that a shot fireball's type is `orespawn:better_fireball`.
### TEST-003 — Config-flipping gametests in the concurrent default batch

- **Impact:** MEDIUM (suite reliability) — boss005/boss012 flip a global
  boss-enable across a 140-tick window then scan a 40-block radius, in
  the concurrent 50-test default batch. Bucket composition is a function
  of total test count, so any suite growth reshuffles neighbors; first
  detonation 2026-08-11 (boss005 false-failed after +3 uncommitted gait
  tests re-bucketed the batch). Broader exposure (unbatched .set()
  call sites, sweep 2026-08-11): EntityLogicTestsA (13), SpawnGateTests
  (12, synchronous-with-finally by design note), StructureTestsA (4).
- **Resolution:** PARTIAL (2026-08-11 — boss005 -> batch "bossGate005",
  boss012 -> "bossGate012" (the two proven-shape offenders; one test per
  batch, since same-batch tests run concurrently). REMAINING: audit the
  other unbatched .set() sites for window-shaped mutations — proposed as
  a follow-up ruling; the synchronous set-assert-restore-in-one-tick
  pattern (SpawnGateTests' documented contract) is believed safe.)
- **2026-09-03 detonation:** adding the 15 RenderInfoParityTests to the default
  batch reshuffled its 50-test buckets and two neighbours failed with the code
  otherwise identical: `bug003_rat_ai_ticks_and_despawns` (the summoned rat
  vanished: the despawn roll) and `dsb_item020_towers_maze_rookery` (a
  spawner-count outcome). Both passed again once the new tests got their own
  batch (`renderInfoParity`). Both are order-sensitive and belong on the
  TEST-003 follow-up list; new test classes should declare their own batch.
