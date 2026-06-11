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

### ENT-SYS2-002 — Systemic: port `MobStats.java` is dead code contradicting hardcoded attributes
- **Status:** DIVERGENT
- **Original:** `OreSpawnMain.java` `get_mobstats` (lines ~6460–6525) — config-driven stats applied to every entity
- **Port:** `MobStats.java` constants (e.g. KRAKEN 500/30 `MobStats.java:59`, KYUUBI 120/16 `:29`, LURKING_TERROR 150/20 `:47`, RAT 15/3, ROTATOR 80/10, SCORPION 30/5 `:63`) referenced by NO entity in slices K–Z; all `createAttributes()` values hardcoded and differing
- **Fix:** either wire every `createAttributes()` to MobStats constants (and correct constants to the 1.7.10 values) or delete `MobStats.java`. Medium priority; do before fixing per-entity stats so fixes land in one place.

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

### ENT-K-004 — Kraken: natural ocean spawning added (was spawner/summon-only)
- **Status:** DIVERGENT
- **Original:** ORIG `Kraken.java` `func_70601_bi` — no `addSpawn`; required open-sky column + Y>50, spawner/summon only
- **Port:** `BM add_ocean_spawns.json` — weight 1/1-1 natural ocean spawn, no `checkSpawnRules` override; a 3000-HP boss spawns naturally
- **Fix:** remove Kraken from `add_ocean_spawns.json`, or add a `checkSpawnRules` override requiring open sky + Y>50 + config gate.

### ENT-K-005 — Kraken: custom sounds replaced with vanilla elder guardian
- **Status:** DIVERGENT
- **Original:** ORIG `Kraken.java` — `orespawn:kraken_living` (1/5), `orespawn:alo_death`
- **Port:** `entity\Kraken.java:431-446` — vanilla `ELDER_GUARDIAN_AMBIENT` (1/5) / `ELDER_GUARDIAN_DEATH`
- **Fix:** register/use `ModSounds.KRAKEN_LIVING` and `ALO_DEATH` in `getAmbientSound`/`getDeathSound`.

---

## Kyuubi

### ENT-K-006 — Kyuubi: HP gutted 125→30
- **Status:** DIVERGENT
- **Original:** `OreSpawnMain.java:6485` — HP 125, ATK 10 (melee 3 via `getAttackStrength`), def 10, speed 0.25, xp 30
- **Port:** `entity\EntityKyuubi.java:50-55` — HP 30, ATK 3, speed 0.25, xp 30
- **Fix:** set MAX_HEALTH 125, ATTACK_DAMAGE 10 (keep effective melee 3 if replicating `getAttackStrength`), ARMOR 10.

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

### ENT-K-009 — Kyuubi: Nether spawn weight halved
- **Status:** DIVERGENT
- **Original:** `OreSpawnMain.java:4802` — Nether weight 10/1-1
- **Port:** `BM add_nether_spawns.json` — Nether weight 5/1-1
- **Fix:** set weight back to 10 in `add_nether_spawns.json`.

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

### ENT-K-013 — LeafMonster: prey list broadened
- **Status:** DIVERGENT
- **Original:** ORIG `LeafMonster.java` — targets ants, butterflies, luna moths, non-creative players
- **Port:** `entity\EntityLeafMonster.java:139-146` — targets players + anything with BbWidth<1.0
- **Fix:** replace width heuristic with an explicit class allow-list (EntityAnt, Butterfly, LunaMoth equivalents, Player).

### ENT-K-014 — LeafMonster: drops changed
- **Status:** DIVERGENT
- **Original:** ORIG `LeafMonster.java` — random leaves/log/stick
- **Port:** `LT leaf_monster.json` — 1–3 oak log + 0–2 bone
- **Fix:** rewrite `leaf_monster.json` to random leaves OR log OR stick (one of, weighted), no bone.

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

### ENT-K-017 — Leon: rider-controlled flight missing
- **Status:** MISSING
- **Original:** ORIG `Leon.java` `fly_with_rider` — full rider-controlled flight, speed up to 3.5, vertical control via `flyup_keystate`
- **Port:** `entity\EntityLeon.java:183-205` — `tickRidden`/`getRiddenInput` give ground movement only at 1.8× walk speed; Leonopteryx not rideable at all
- **Fix:** implement flying mount in `tickRidden`: set `setNoGravity(true)` while ridden, map jump key to vertical ascent, speed cap 3.5. High priority — signature feature.

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

### ENT-K-020 — Leon: drops replaced with diamonds/gold
- **Status:** DIVERGENT
- **Original:** ORIG `Leon.java` — 4–9 raw chicken, 16–21 feather, 2–7 KrakenRepellent, 1/5 MyBattleAxe
- **Port:** `entity\EntityLeon.java:686-696` — 4–9 diamond + 16–21 gold ingot, plus `LT leon.json` bones/leather; `LT leonopteryx.json` battle_axe 100% (orig 20%) + kraken_repellent 1–2 (orig 2–7)
- **Fix:** code path → 4–9 chicken + 16–21 feather + 2–7 kraken_repellent + 20% battle_axe; leonopteryx.json → battle_axe 20%, kraken_repellent 2–7; remove duplicate path (ENT-SYS2-001).

### ENT-K-021 — Leon: natural spawning added (incl. Nether)
- **Status:** DIVERGENT
- **Original:** ORIG — spawner-gated only ("Leonopteryx" spawner, Y>50, no nearby Leons)
- **Port:** `BM companion_leon.json` jungle w1 + `add_nether_spawns.json` Nether w2; leonopteryx mountains w1
- **Fix:** remove leon from `add_nether_spawns.json` (a Pandora-themed mount in the Nether is wrong); gate jungle/mountain spawns behind `checkSpawnRules` (Y>50, no nearby Leons).

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

### ENT-K-024 — Lizard: spawn domain widened
- **Status:** DIVERGENT
- **Original:** `OreSpawnMain.java:4868-4870` — water biomes, weight 2–5/2-4
- **Port:** `BM add_overworld_creatures.json` — all-overworld w10/1-2, `checkSpawnRules` Y≥50 (`Lizard.java:258-260`)
- **Fix:** restrict the BM to river/swamp/beach-tagged biomes at w2–5/2-4.

---

## LurkingTerror

### ENT-K-025 — LurkingTerror: stats changed
- **Status:** DIVERGENT
- **Original:** `OreSpawnMain.java:6503` — HP 30, ATK 6 (melee 5), def 5, speed 0.25, xp 20
- **Port:** `entity\EntityLurkingTerror.java:42-47` — HP 40, ATK 5, no armor
- **Fix:** set MAX_HEALTH 30, ATTACK_DAMAGE 6, ARMOR 5.

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

---

## Molenoid

### ENT-K-034 — Molenoid: stats halved
- **Status:** DIVERGENT
- **Original:** `OreSpawnMain.java:6478` — HP 200, ATK 18, def 12, speed 0.35, xp 40
- **Port:** `entity\EntityMolenoid.java:62-68` — HP 100, ATK 10, no armor
- **Fix:** set MAX_HEALTH 200, ATTACK_DAMAGE 18, ARMOR 12.

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

### ENT-K-043 — Nastysaurus: drop table inflated to 40 valuables
- **Status:** DIVERGENT
- **Original:** ORIG `Nastysaurus.java` — 10 coal + 10 stick + 10 bone + 10 arrow
- **Port:** code `Nastysaurus.java:134-148` — 10 gold + 10 emerald + 10 diamond + 10 iron; PLUS `LT nastysaurus.json` bones/gunpowder
- **Fix:** replace code drops with 10 coal + 10 stick + 10 bone + 10 arrow (or move into the JSON and delete the code path, ENT-SYS2-001).

---

## Ostrich

### ENT-K-044 — Ostrich: rideable but unsteerable
- **Status:** PARTIAL
- **Original:** ORIG `Ostrich.java` — full rider movement + jump via `flyup_keystate`
- **Port:** `entity\Ostrich.java:93-99,109` — `player.startRiding` works and AI suspends, but no `tickRidden`/`getRiddenInput`/`getControllingPassenger`
- **Fix:** implement `getControllingPassenger`, `getRiddenInput` (rider WASD), `getRiddenSpeed`, and jump handling.

### ENT-K-045 — Ostrich: damage-immunity rule inverted
- **Status:** DIVERGENT
- **Original:** ORIG `Ostrich.java` `func_70097_a` — immune to everything EXCEPT cactus (cactus was its only weakness)
- **Port:** `entity\Ostrich.java:67-71` — immune TO cactus only, vulnerable to everything else
- **Fix:** invert `isInvulnerableTo`: return true for all sources except `DamageTypes.CACTUS`.

### ENT-K-046 — Ostrich: tamed drop table changed
- **Status:** DIVERGENT
- **Original:** ORIG `Ostrich.java` — tamed → 2–6 flower/sand items; else default
- **Port:** `LT ostrich.json` — 1–3 chicken
- **Fix:** restore tamed-conditional drop (2–6 flower/sand) via loot condition on tamed flag or code path; untamed → vanilla-equivalent default.

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

---

## PitchBlack (Nightmare)

### ENT-K-051 — PitchBlack: continuous scale discretized + speed model changed
- **Status:** DIVERGENT
- **Original:** `OreSpawnMain.java:6517` — continuous scale 0.5–4.0 (config `NightmareSize`); HP 250×scale, ATK 30×scale, def 10+2×scale, speed 0.2+0.1×scale
- **Port:** `entity\PitchBlack.java:87-91`, `OreSpawnConfig.java:145` — 5 discrete tiers HP 125–1000, ATK 15–120, armor 10–18; flight speed 0.5+scale/10. Stat envelope matches at extremes; discretization + speed formula diverge
- **Fix:** accept tiers (well-matched envelope) but align speed formula to 0.2+0.1×scale, or restore continuous scale from config.

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

### ENT-K-055 — Pointysaurus: drops inflated to diamonds
- **Status:** DIVERGENT
- **Original:** ORIG `Pointysaurus.java` — 10 bone + 6 carrot + 6 stick + 6 arrow
- **Port:** code `Pointysaurus.java:150-164` — 10 diamond + 6 beef + 6 emerald + 6 iron; PLUS `LT pointysaurus.json` bones (double path)
- **Fix:** restore 10 bone + 6 carrot + 6 stick + 6 arrow in one path; delete the other (ENT-SYS2-001).

---

## PurplePower

### ENT-K-056 — PurplePower: potion type 2 swapped
- **Status:** DIVERGENT
- **Original:** ORIG `PurplePower.java` — attack type 2 applies Weakness
- **Port:** `entity\PurplePower.java:159` — type 2 applies HUNGER
- **Fix:** change the type-2 effect at `PurplePower.java:159` to `MobEffects.WEAKNESS`.

---

## Rat

### ENT-K-057 — Rat: stats changed
- **Status:** DIVERGENT
- **Original:** `OreSpawnMain.java:6483` — HP 5, ATK 3, def 1, speed 0.25, xp 5
- **Port:** `entity\EntityRat.java:56-61` — HP 10, ATK 2
- **Fix:** set MAX_HEALTH 5, ATTACK_DAMAGE 3, ARMOR 1.

### ENT-K-058 — Rat: default-passive configs flip original hostility
- **Status:** DIVERGENT
- **Original:** ORIG `Rat.java` — wild rats attacked players
- **Port:** `OreSpawnConfig.java:143-144` — `RAT_PLAYER_FRIENDLY`/`RAT_PET_FRIENDLY` default TRUE → never attacks players/pets (`EntityRat.java:160-183`)
- **Fix:** flip both config defaults to false (keep configs for opt-in friendliness).

### ENT-K-059 — Rat: drop changed
- **Status:** DIVERGENT
- **Original:** ORIG `Rat.java` — stick
- **Port:** `LT rat.json` — 0–1 bone + 0–1 rotten flesh
- **Fix:** rewrite `rat.json` to drop 1 stick.

### ENT-K-060 — Rat: swarm spawning lost
- **Status:** DIVERGENT
- **Original:** `OreSpawnMain.java:4977-4978` — weight 25–35 / group 2–20 in select biomes + Crystal-dim air-pocket checks + ≤8 nearby
- **Port:** BM overworld-wide w20/1-3, no checks
- **Fix:** set group size 2–20 (cap via ≤8-nearby `checkSpawnRules`) and restrict biome set.

---

## RedCow

### ENT-K-061 — RedCow: invented wheat bonus drop
- **Status:** DIVERGENT
- **Original:** ORIG `RedCow.java` — vanilla cow drops + 1–2 bonus leather-class item
- **Port:** `LT red_cow.json` 1–3 leather + code `RedCow.java:23-30` 1–3 wheat (both fire)
- **Fix:** drop the wheat code path; keep cow drops + 1–2 bonus leather in the JSON (ENT-SYS2-001).

---

## Robot2 (Robo-Pounder)

### ENT-K-062 — Robot2: HP 2.5×
- **Status:** DIVERGENT
- **Original:** `OreSpawnMain.java:6495` — HP 200, ATK 22, def 18, speed 0.3, xp 100
- **Port:** `entity\Robot2.java:64-70` — HP 500, ATK 30, armor 8
- **Fix:** set MAX_HEALTH 200, ATTACK_DAMAGE 22, ARMOR 18.

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

---

## Robot3 (Robo-Gunner)

### ENT-K-065 — Robot3: HP ×3.75
- **Status:** DIVERGENT
- **Original:** `OreSpawnMain.java:6496` — HP 80, ATK 16, def 14, speed 0.35, xp 60
- **Port:** `entity\Robot3.java:62-68` — HP 300, ATK 20, armor 6
- **Fix:** set MAX_HEALTH 80, ATTACK_DAMAGE 16, ARMOR 14.

### ENT-K-066 — Robot3: LaserBall ammo drop missing
- **Status:** DIVERGENT
- **Original:** ORIG `Robot3.java` — 5–10 × MyLaserBall(4) + random table
- **Port:** `LT robot_3.json` — 3–6 iron ingot + 20% diamond; LaserBall item exists (`ModItems.java:335`) but is never dropped
- **Fix:** add `orespawn:laser_ball` 5–10 to `robot_3.json`.

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

---

## Robot5 (Robo-Sniper)

### ENT-K-072 — Robot5: HP ×7.5 (largest stat inflation in slice)
- **Status:** DIVERGENT
- **Original:** `OreSpawnMain.java:6498` — HP 20, ATK 5, def 6, speed 0.3, xp 20
- **Port:** `entity\Robot5.java:63-68` — HP 150, ATK 15, armor 4
- **Fix:** set MAX_HEALTH 20, ATTACK_DAMAGE 5, ARMOR 6.

### ENT-K-073 — Robot5: ammo drop missing
- **Status:** DIVERGENT
- **Original:** ORIG `Robot5.java` — 5–10 LaserBall(4) + randoms
- **Port:** `LT robot_5.json` — 4–8 iron + 1–3 gold
- **Fix:** add `orespawn:laser_ball` 5–10 to `robot_5.json`.

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

### ENT-K-087 — RubyBird: ruby can drop twice
- **Status:** DIVERGENT
- **Original:** ORIG drop unverified from decompile (dungeon-loot oriented)
- **Port:** code 1/3 ruby (`RubyBird.java:28-33`) PLUS `LT ruby_bird.json` feather 1–2 + 33% ruby — up to two rubies per kill
- **Fix:** delete the code drop; keep `ruby_bird.json` as the single path (ENT-SYS2-001). Verify ORIG `RubyBird.java` drop method before final tuning.

### ENT-K-088 — RubyBird: spawn model changed (dungeon-only → natural)
- **Status:** DIVERGENT
- **Original:** ORIG — RubyBirdDungeon placement only (`func_70601_bi` true)
- **Port:** `BM dim_crystal_locals.json` — crystal_plains natural spawn w6/2-4
- **Fix:** if RubyBird dungeons are ported, remove the natural BM entry; otherwise keep as the substitute and document.

### ENT-K-089 — RubyBird: bespoke sound unused
- **Status:** DIVERGENT
- **Original:** ORIG `RubyBird.java` — `orespawn:rubybird` when not raining
- **Port:** inherits Cockateil `orespawn:birds` (`Cockateil.java:142-146`); rubybird sound asset unused
- **Fix:** override `getAmbientSound` in `RubyBird.java` to return the rubybird sound when `!level.isRaining()`.

---

## Scorpion

### ENT-S-001 — Scorpion: HP up, armor lost
- **Status:** DIVERGENT
- **Original:** `OreSpawnMain.java:6518`, `Scorpion.java:46,52` — HP 15, atk 4, def 10, speed 0.2, xp 10
- **Port:** `entity\EntityScorpion.java:50-56` — HP 20, atk 4, no armor
- **Fix:** set MAX_HEALTH 15, ARMOR 10.

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

### ENT-S-007 — SeaMonster: double drops + additions, gear unenchanted
- **Status:** DIVERGENT
- **Original:** ORIG — fish ×(9-14), SeaMonsterScale, chance of ENCHANTED iron tools/armor
- **Port:** `LT sea_monster.json` (scale + name_tag + cod 9–14 + 1/20 gear pool) PLUS code `SeaMonster.java:207-212` (heart_of_the_sea + 9–14 cod + 1/3 diamond)
- **Fix:** delete the code path (ENT-SYS2-001); remove name_tag/heart_of_the_sea; add `enchant_randomly` to the gear pool entries.

### ENT-S-008 — SeaMonster: spawn weight 4→1, swamp dropped
- **Status:** DIVERGENT
- **Original:** `OreSpawnMain.java:4850-4851` — waterCreature ocean w4, swamp w2
- **Port:** `BM add_ocean_spawns` — w1 (1-1)
- **Fix:** raise ocean weight to 4; add swamp entry w2.

---

## SeaViper

### ENT-S-009 — SeaViper: stats halved
- **Status:** DIVERGENT
- **Original:** `OreSpawnMain.java:6494` — HP 160, atk 22, def 12
- **Port:** `SeaViper.java:46-49` — HP 120, atk 12, no armor
- **Fix:** set MAX_HEALTH 160, ATTACK_DAMAGE 22, ARMOR 12.

### ENT-S-010 — SeaViper: Poison on-hit replaced by Hunger
- **Status:** DIVERGENT
- **Original:** ORIG `SeaViper.java` — melee applies Poison
- **Port:** `entity\ai\SeaViperBiteGoal.java:22-26` — Hunger 8 s, 1/2 roll
- **Fix:** change the effect in `SeaViperBiteGoal` to `MobEffects.POISON` (keep duration/roll).

### ENT-S-011 — SeaViper: double drops + fish inflation
- **Status:** DIVERGENT
- **Original:** ORIG — fish, SeaViperTongue, enchanted-gear chances
- **Port:** `LT sea_viper.json` (tongue + name_tag + cooked_cod 9–14 + cod 9–14 + gear) PLUS code `SeaViper.java:254-261` (heart_of_the_sea + 9–14 × cod+salmon)
- **Fix:** delete the code path (ENT-SYS2-001); single fish pool 9–14; drop name_tag; enchant the gear pool.

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

### ENT-S-014 — Skate: drop changed
- **Status:** DIVERGENT
- **Original:** ORIG `Skate.java` `func_146068_u` — raw fish
- **Port:** `LT skate.json` — prismarine_shard 1–3
- **Fix:** rewrite `skate.json` to cod ×1.

### ENT-S-015 — Skate: spawn domain moved to vanilla oceans
- **Status:** DIVERGENT
- **Original:** `BiomeGenUtopianPlains.java:259` — Island/Crystal dims waterCreature w2 (3-6)
- **Port:** `BM add_ocean_spawns` w6 (1-2); gates (y≥50, 1/30, ≤6 nearby) ported (`Skate.java:182-187`)
- **Fix:** if Island/Crystal dims exist in port, move skate spawns there at w2 (3-6); else reduce ocean weight to 2, group 3-6, and document the domain substitution.

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

### ENT-S-018 — SpiderDriver: never attacks from the mount
- **Status:** PARTIAL
- **Original:** ORIG — attacks nearby mobs from the mounted SpiderRobot
- **Port:** port `SpiderDriver.java:67-72` — when mounted only *looks at* targets 1/4
- **Fix:** in the mounted branch, call `doHurtTarget` on targets within reach instead of only `getLookControl().setLookAt`.

### ENT-S-019 — SpiderDriver: melee + poison missing entirely
- **Status:** MISSING
- **Original:** ORIG `SpiderDriver.java:89-92` — melee + Poison 60 ticks, 1/2 roll
- **Port:** no `doHurtTarget` path, no poison
- **Fix:** override `doHurtTarget`: vanilla spider damage + 1/2 chance Poison (60 ticks). (Natural spawn: see ENT-SYS2-003.)

---

## SpiderRobot

### ENT-S-020 — SpiderRobot: stats third-ed
- **Status:** DIVERGENT
- **Original:** `OreSpawnMain.java:6474` — HP 1500, atk 100, def 16
- **Port:** port `SpiderRobot.java:68-74,161` — HP 500, atk 50 (hardcoded 50.0f in `doHurtTarget`), armor 8
- **Fix:** set MAX_HEALTH 1500, ATTACK_DAMAGE 100 (and the hardcoded 50.0f → 100.0f), ARMOR 16.

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

---

## SpitBug

### ENT-S-024 — SpitBug: stats reduced
- **Status:** DIVERGENT
- **Original:** `OreSpawnMain.java:6490` — HP 100, atk 10, def 12
- **Port:** port `EntitySpitBug.java:61-67` — HP 60, atk 8, no armor
- **Fix:** set MAX_HEALTH 100, ATTACK_DAMAGE 10, ARMOR 12.

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

### ENT-S-029 — Spyro: spawn domain moved
- **Status:** DIVERGENT
- **Original:** Island/Crystal/Mining dims (`BiomeGenUtopianPlains`; mining w1)
- **Port:** `BM companion_spyro__is_badlands/is_mountain` w1 (1-1)
- **Fix:** if the custom dims exist in port, add Spyro to their spawn lists at w1; otherwise keep substitute biomes and document.

---

## StinkBug

### ENT-S-030 — StinkBug: death gas Poison → Hunger
- **Status:** DIVERGENT
- **Original:** ORIG `StinkBug.java` — Poison to entities within ~8 blocks on death
- **Port:** port `EntityStinkBug.java:82-95` — Hunger 300t in 8×5×8
- **Fix:** change the effect to `MobEffects.POISON` (keep radius/duration).

### ENT-S-031 — StinkBug: food changed
- **Status:** DIVERGENT
- **Original:** ORIG — fish + CrystalApple
- **Port:** port `EntityStinkBug.java:107-109` — apple
- **Fix:** change `isFood` to cod + crystal_apple (ModItems).

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

### ENT-S-039 — TerribleTerror: emerald drop gone
- **Status:** DIVERGENT
- **Original:** ORIG `TerribleTerror.java:313-322` — 1/3 each: rotten flesh / emerald / feather
- **Port:** LT — bone 1–2 + leather 0–1 + feather 0–1
- **Fix:** rewrite loot table: three independent 1/3 entries for rotten_flesh, emerald, feather.

### ENT-S-040 — TerribleTerror: spawn domain moved to vanilla overworld
- **Status:** DIVERGENT
- **Original:** `BiomeGenUtopianPlains.java:182,412` — Island dim monster w25 (3-6); chaos w4 (2-6)
- **Port:** `BM add_overworld_monsters` w4 (1-2)
- **Fix:** if Island/Chaos dims exist in port, move spawns there (w25/3-6, w4/2-6); else document overworld substitution and consider group 2-6.

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

### ENT-S-043 — TRex: loot identity changed
- **Status:** DIVERGENT
- **Original:** ORIG — TRexTooth, bone, UraniumNugget, TitaniumNugget
- **Port:** `LT trex.json` — tooth (60/30/10 ×1/2/3) + name_tag + 7 beef + gold/iron nuggets 2–5 + xp bottle + diamond 4–7; code adds bone (`:131-135`)
- **Fix:** rewrite `trex.json`: tooth + bone + uranium nugget + titanium nugget; delete name_tag/beef/diamond/xp; drop the code bone path (ENT-SYS2-001).

### ENT-S-044 — TRex: spawn domain moved overworld-wide
- **Status:** DIVERGENT
- **Original:** no overworld `addSpawn`; Island/Crystal w1, Mining dim (`BiomeGenUtopianPlains.java:496`)
- **Port:** `BM` trex badlands+savanna w1 (1-1) AND `add_overworld_monsters` w1
- **Fix:** remove TRex from `add_overworld_monsters`; keep (or dim-gate) the badlands/savanna entries.

### ENT-S-045 — TRex: custom sounds replaced with ravager
- **Status:** DIVERGENT
- **Original:** ORIG `:98-108` — `trex_living` / `alo_hurt` / `trex_death`
- **Port:** port `:88-103` — RAVAGER_ROAR / RAVAGER_HURT / RAVAGER_DEATH
- **Fix:** use `ModSounds.TREX_LIVING/ALO_HURT/TREX_DEATH` (trex_death already registered — used by TheKing).

---

## Triffid

### ENT-S-046 — Triffid: attack third-ed, armor lost
- **Status:** DIVERGENT
- **Original:** `OreSpawnMain.java:6502` — HP 100, atk 20, def 12
- **Port:** port `EntityTriffid.java:52-57` — HP 100, atk 8, no armor
- **Fix:** set ATTACK_DAMAGE 20, ARMOR 12.

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

---

## Tshirt

### ENT-S-055 — Tshirt: drop string → leather
- **Status:** DIVERGENT
- **Original:** ORIG — string
- **Port:** code drop leather (`EntityTshirt.java:58-62`); loot table empty
- **Fix:** change the code drop to `Items.STRING` (or move to the loot table and delete the code drop).

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

### ENT-S-062 — Urchin: spawn domain moved to vanilla oceans
- **Status:** DIVERGENT
- **Original:** Island w15 (2-4) / Crystal w2 (1-5) dims; night spawner
- **Port:** `BM add_ocean_spawns` w6 (1-2); rules time≥13000 (`:168-171`)
- **Fix:** if Island/Crystal dims exist in port, move spawns there at original weights; else document the ocean substitution.

---

## VelocityRaptor

### ENT-S-063 — VelocityRaptor: tamed HP bump missing
- **Status:** PARTIAL
- **Original:** ORIG — HP 10 wild, 20 when tamed
- **Port:** port `VelocityRaptor.java:73-78` — HP 10 always
- **Fix:** on successful tame, set MAX_HEALTH base to 20 and heal to full.

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

### ENT-S-066 — VelocityRaptor: drop changed
- **Status:** DIVERGENT
- **Original:** ORIG `:335` — poppy
- **Port:** `LT velocity_raptor.json` — bone 1–3
- **Fix:** rewrite to poppy ×1.

### ENT-S-067 — VelocityRaptor: spawn domain moved
- **Status:** DIVERGENT
- **Original:** Island/Crystal/Mining dims (jungle addSpawn not found)
- **Port:** `BM companion_velocity_raptor` jungle/savanna w2 (1-2) + `add_overworld_creatures` w4 (1-2); rules y≥50 + sky (`:261-264`)
- **Fix:** remove from `add_overworld_creatures` (keep themed jungle/savanna entries); add custom-dim entries if those dims exist.

---

## Vortex

### ENT-S-068 — Vortex: stats changed, fire immunity missing
- **Status:** DIVERGENT
- **Original:** `OreSpawnMain.java:6500` — HP 150, atk 26, def 10; fire-immune
- **Port:** port `EntityVortex.java:60-65` — HP 200, atk 20, no armor, not fire-immune
- **Fix:** set MAX_HEALTH 150, ATTACK_DAMAGE 26, ARMOR 10; add `fireImmune()`.

### ENT-S-069 — Vortex: invented skyward-launch attack
- **Status:** DIVERGENT
- **Original:** ORIG — melee 26 + drag only
- **Port:** port `:190-196,244-274` — melee plus new `skywardLaunch` (+4.0 up, 30t cooldown)
- **Fix:** remove `skywardLaunch` (or config-gate it); rely on the ported pull + melee.

### ENT-S-070 — Vortex: drops changed
- **Status:** DIVERGENT
- **Original:** ORIG — VortexEye, bone, ingots/gems
- **Port:** `LT vortex.json` — vortex_eye + xp bottle + gunpowder 3–8 + gold 1–3
- **Fix:** rewrite: vortex_eye + bone + the original ingot/gem pool; drop xp bottle/gunpowder.

### ENT-S-071 — Vortex: spawns in the wrong dimension
- **Status:** DIVERGENT
- **Original:** night overworld + Island w3 (1-2)/Crystal w1/Chaos dims (`BiomeGenUtopianPlains.java:226,406`)
- **Port:** `BM add_nether_spawns` w4 — Nether only
- **Fix:** remove vortex from `add_nether_spawns`; add overworld monster entry (night via `checkSpawnRules`, day-despawn already ported `:121-126`) + custom-dim entries if available.

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

### ENT-S-080 — WormSmall: natural daytime spawning added
- **Status:** DIVERGENT
- **Original:** ORIG — no `addSpawn`; only spawned by WormLarge; spawn rule = night only (`:214-216`)
- **Port:** `BM add_overworld_creatures` CREATURE w10 (1-2), ON_GROUND placement
- **Fix:** remove WormSmall from `add_overworld_creatures` (WormLarge already summons 20); if kept, add night-only `checkSpawnRules`.

---

## WormMedium

### ENT-S-081 — WormMedium: attack reduced, armor lost
- **Status:** DIVERGENT
- **Original:** `OreSpawnMain.java:6505` — HP 30, atk 10, def 8
- **Port:** port `EntityWormMedium.java:32-37` — HP 30, atk 6, no armor
- **Fix:** set ATTACK_DAMAGE 10, ARMOR 8.

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

---

## WormLarge

### ENT-S-084 — WormLarge: stats changed
- **Status:** DIVERGENT
- **Original:** `OreSpawnMain.java:6506` — HP 90, atk 18, def 14
- **Port:** port `EntityWormLarge.java:46-51` — HP 100, atk 15, no armor
- **Fix:** set MAX_HEALTH 90, ATTACK_DAMAGE 18, ARMOR 14.

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

### BOSS-002 — TheKing: hitbox much smaller, no PlayNicely shrink
- **Status:** DIVERGENT
- **Original:** ORIG `TheKing.java:86-88` — 22×24 (5.5×6 if PlayNicely)
- **Port:** `ModEntities.java:180-182` — 6×12 parent + 5 parts (partial compensation)
- **Fix:** enlarge the parent dimensions and/or part AABBs to approximate the 22×24 envelope; implement PlayNicely shrink (see BOSS-017).

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

### BOSS-005 — TheKing: spawner block loses fuse, height, guard mode, enable gate
- **Status:** DIVERGENT
- **Original:** ORIG `KingSpawnerBlock.java:43-89` — 100-tick scheduled fuse; spawns at y+8 with `setGuardMode(1)` (home-leash anchor); gated by `TheKingEnable`
- **Port:** `PORT\block\BossSpawnerBlock.java:44-57` (generic, `ModBlocks.java:152-154`) — randomTick (unbounded delay), y+1, `MobSpawnType.EVENT`, no guard mode, no enable gate
- **Fix:** in `BossSpawnerBlock`: schedule a 100-tick block tick on placement; spawn at y+8; call the King's guard-mode setter; add a `THE_KING_ENABLE` config check.

---

## TheQueen

### BOSS-006 — TheQueen: attack nerfed, armor nerfed + phase scaling dropped
- **Status:** DIVERGENT
- **Original:** `OreSpawnMain.java:6522`, ORIG `TheQueen.java:99,817-828` — attack 225; armor 21 base with +2/+3/+5 phase scaling
- **Port:** `PORT\entity\TheQueen.java:157,257,158-259` — attack 200; flat armor 10, no scaling override
- **Fix:** set ATTACK_DAMAGE 225, ARMOR 21; port the +2/+3/+5 phase armor bonuses (mirror TheKing's ported structure at `TheKing.java:975-984`).

### BOSS-007 — TheQueen: hitbox smaller, no PlayNicely shrink
- **Status:** DIVERGENT
- **Original:** ORIG `TheQueen.java:79-81` — 22×24 (5.5×6 PlayNicely)
- **Port:** `ModEntities.java:184-186` — 16×12 + MHLib parts (`TheQueen.java:425`)
- **Fix:** verify the MHLib profile covers the 22×24 envelope; implement PlayNicely shrink (BOSS-017).

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

### BOSS-011 — TheQueen: drops massively buffed + doubled
- **Status:** DIVERGENT
- **Original:** ORIG `TheQueen.java:190-199` — Royal sword ×1, PrinceEgg ×1, ThePrincess spawn, then 56× {QueenScale, beef, bone, rotten flesh}
- **Port:** code `TheQueen.java:405-421` — 56× {QueenScale, XP bottle, golden apple, NETHER STAR}; PLUS `LT the_queen.json` royal_guardian_sword + prince_egg + 30–56 queen_scale + 10–30 diamond/string/bone
- **Fix:** code path: revert the 56-roll pool to {queen_scale, beef, bone, rotten_flesh} (removes up to 56 nether stars/golden apples); move royal sword + prince_egg into ONE path and delete the duplicate (ENT-SYS2-001).

### BOSS-012 — TheQueen: spawner block degraded (same four deviations as King's)
- **Status:** DIVERGENT
- **Original:** ORIG `QueenSpawnerBlock.java:55,66-67,81-89` — 100-tick fuse, y+8, `TheQueenEnable` gate, `setGuardMode(1)`
- **Port:** generic `BossSpawnerBlock` (`ModBlocks.java:155-157`) — randomTick, y+1, no gate, no guard mode
- **Fix:** same as BOSS-005, with a `THE_QUEEN_ENABLE` config.

---

## Godzilla (Mobzilla)

### BOSS-013 — Godzilla: HP buffed, attack nerfed, armor entirely missing
- **Status:** DIVERGENT
- **Original:** `OreSpawnMain.java:6514`, ORIG `Godzilla.java:93,95,145` — HP 4000, attack 175, armor 21 (`func_70658_aO`)
- **Port:** `PORT\entity\Godzilla.java:62,111-119` — HP 6000, attack 150, NO armor attribute or override
- **Fix:** set MAX_HEALTH 4000, ATTACK_DAMAGE 175, add ARMOR 21 to `createAttributes()`. (Jump-landing AoE at `Godzilla.java:594-603` scales from attack and self-corrects.)

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

### BOSS-016 — Godzilla: custom sounds replaced with vanilla ender dragon
- **Status:** DIVERGENT
- **Original:** ORIG `Godzilla.java:178-188` — `orespawn:godzilla_living` (1/5) / `alo_hurt` / `godzilla_death`
- **Port:** `Godzilla.java:261-276` — ENDER_DRAGON_GROWL / ENDER_DRAGON_HURT / ENDER_DRAGON_DEATH
- **Fix:** register/use `ModSounds.GODZILLA_LIVING/ALO_HURT/GODZILLA_DEATH`.

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

### BOSS-025 — ThePrince: drops beef → diamond
- **Status:** DIVERGENT
- **Original:** ORIG `ThePrince.java:354-361` — 1–4 beef
- **Port:** `LT the_prince.json` — 1–4 diamond
- **Fix:** rewrite `the_prince.json` to beef 1–4.

---

## ThePrinceTeen

### BOSS-026 — ThePrinceTeen: stats divergent (HP/armor/speed/XP/size)
- **Status:** DIVERGENT
- **Original:** ORIG `ThePrinceTeen.java:229-231,252-254,87,105,103` — HP 1500, armor 18, speed 0.32, XP 300, size 3.25×4.25
- **Port:** `PORT\entity\ThePrinceTeen.java:90,88-95,58,68`, `ModEntities.java:472-474` — HP 1000, NO armor, speed 0.35, XP 500, size 2×3
- **Fix:** set MAX_HEALTH 1500, add ARMOR 18, speed 0.32, XP 300; resize EntityType to 3.25×4.25.

### BOSS-027 — ThePrinceTeen: riding missing
- **Status:** MISSING
- **Original:** ORIG `ThePrinceTeen.java:1157` — saddle-free mount
- **Port:** no riding
- **Fix:** add `mobInteract` startRiding + `getControllingPassenger`/`tickRidden` ground movement.

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

---

## ThePrinceAdult

### BOSS-030 — ThePrinceAdult: armor missing
- **Status:** MISSING
- **Original:** ORIG `ThePrinceAdult.java:248-250` — armor 20
- **Port:** `PORT\entity\ThePrinceAdult.java:86-93` — no ARMOR attribute
- **Fix:** add ARMOR 20 to `createAttributes()`.

### BOSS-031 — ThePrinceAdult: size shrunk
- **Status:** DIVERGENT
- **Original:** ORIG `ThePrinceAdult.java:100` — 6.25×10.25
- **Port:** `ModEntities.java:464-466` — 4×6
- **Fix:** resize EntityType to 6.25×10.25 (and check model scale).

### BOSS-032 — ThePrinceAdult: King-transform config gate dropped
- **Status:** DIVERGENT
- **Original:** ORIG `ThePrinceAdult.java:400-404` — transform→TheKing gated `activity==0 && no rider && !peaceful && tamed && FullPowerKingEnable!=0`, growcounter>288000
- **Port:** `ThePrinceAdult.java:176-182,220-227` — gated `isTame && !hardcore` only; `FULL_POWER_KING_ENABLE` repurposed as King damage ×2 (`TheKing.java:893-896`)
- **Fix:** re-add a `FULL_POWER_KING_ENABLE`-style gate on the transform (and the no-rider/!peaceful checks); if the ×2 King damage stays, give it its own config key.

### BOSS-033 — ThePrinceAdult: riding missing
- **Status:** MISSING
- **Original:** ORIG `ThePrinceAdult.java:1134` — mountable
- **Port:** no riding
- **Fix:** same as BOSS-027.

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

### BOSS-036 — ThePrinceAdult: King-tier sounds replaced
- **Status:** DIVERGENT
- **Original:** ORIG `ThePrinceAdult.java:270-281` — king_living / king_hit / trex_death
- **Port:** `ThePrinceAdult.java:272-287` — roar / alo_hurt / alo_death
- **Fix:** switch to `ModSounds.KING_LIVING/KING_HIT/TREX_DEATH` (already registered for TheKing).

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
