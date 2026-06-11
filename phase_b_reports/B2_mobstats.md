# Phase B2 — MobStats Reconciliation Report

Date: 2026-06-11
Scope: make `MobStats.java` the single source of truth for the original `get_mobstats` table
(`orig OreSpawnMain.java:6466-6525`), wire every table entity's `createAttributes()` to it,
reconcile xpReward and hardcoded melee floats, fix the vanilla attribute caps, and resolve
all stats-only audit findings.

Conventions used below:
- "orig" = `reference_1_7_10_source/sources/danger/orespawn/...`
- "port" = `src/main/java/danger/orespawn/...`
- Old port line numbers are pre-edit; CONFIRMED = port value already matched the original (left as-is, citation comment added where useful).

---

## 1. MobStats.java rewrite

`port MobStats.java` rewritten as `record MobStats(double maxHealth, double attackDamage, double armor)`
with exactly 59 constants, one per `get_mobstats(config, mobs, "Name", health, attack, defense)` call,
each Javadoc'd with its exact original line (orig OreSpawnMain.java:6466 BEE … :6524 CRAB).
Name mappings documented in the class Javadoc: "Mobzilla"→Godzilla, "Nightmare"→PitchBlack,
"Leonopteryx"→Leon, "Jeffery"→GiantRobot (and its Jeffery skin alias), "BandP"→port `BandP`
(orig `BandP.java:68,94,98` consumes `BandP_stats` directly — the burglar/pickpocket entity).
Config-driven clamping (orig OreSpawnMain.java:6066-6096: health/2..health×2, attack/2..attack×2,
defense−4..defense+4 clamped 0..22) is **deferred** to the config findings and noted as such.

Entity-side overrides documented in the Javadoc (table entry NOT applied verbatim by the original entity):
- **LEON**: orig Leon.java hardcodes HP 250 (:169), ATK 55 (:117), armor 16 (:192) — table says 150/20/8.
- **CRAB**: orig Crab.java:137 reads `PitchBlack_stats.health` (250) ×scale instead of Crab's 180 (original bug, kept); ATK = `Crab_stats.attack`×scale (:71); armor = `Crab_stats.defense`+2×scale (:141).
- **PITCH_BLACK**: HP 250×scale (:239), ATK 30×scale (:80), armor 10+2×scale (:190), scale t ∈ {0.5,1,2,3,4} (:99-141).
- **THE_KING / THE_QUEEN / GODZILLA**: armor situationally boosted by the entity (TheKing.java:856-864, TheQueen.java:819-827, Godzilla.java:145-150); table value is the base.

---

## 2. Attribute-cap fix (critical bug)

Vanilla 1.21.1 `Attributes.MAX_HEALTH` is a `RangedAttribute` clamped to **1024** and
`Attributes.ATTACK_DAMAGE` to **2048** (`RangedAttribute.sanitizeValue` → `Mth.clamp(value, minValue, maxValue)`,
re-read on every evaluation). The port set boss health up to 7000 with no widening, so TheKing (7000),
TheQueen (6000), Godzilla (4000), Kraken (1000→fine, but the old port set 3000), SpiderRobot (1500) etc.
silently clamped to 1024 HP.

Fix:
1. `src/main/resources/META-INF/accesstransformer.cfg` — stale "searge names" header comment replaced
   (NeoForge 1.21.1 ATs use Mojang names; the existing `ENTITY_COUNTER` line already was one), and added:
   `public-f net.minecraft.world.entity.ai.attributes.RangedAttribute maxValue`
2. `port OreSpawnMod.java` constructor (earliest safe point, before any entity can be constructed):
   `((RangedAttribute) Attributes.MAX_HEALTH.value()).maxValue = 100000.0;` and the same for
   `ATTACK_DAMAGE`. A field write is sufficient because `sanitizeValue` reads `maxValue` on each call.
   100000 comfortably covers the largest original value incl. TheQueen's in-fight attack multipliers.

Build verified: AT applies and `gradlew compileJava` succeeds.

---

## 3. Reconciliation table

One row per stat value changed or confirmed. ATK/HP/ARMOR are attribute base values;
XP = `xpReward` (orig `field_70728_aV` = experienceValue).

### Table entities (get_mobstats consumers)

| Entity (port file) | Stat | Orig citation (value) | Old port value (file:line) | New port value |
|---|---|---|---|---|
| EntityBee | HP | OreSpawnMain.java:6466 (80) | 30.0 (EntityBee.java:53) | 80 (MobStats.BEE) |
| EntityBee | ATK | OreSpawnMain.java:6466 (12) | 6.0 (EntityBee.java:55) | 12 (MobStats.BEE) |
| EntityBee | ARMOR | OreSpawnMain.java:6466 (5) | absent | 5 (MobStats.BEE) |
| EntityBee | XP | orig Bee.java:47 (25) | 25 (EntityBee.java:49) | CONFIRMED 25 |
| EntityMantis | HP | OreSpawnMain.java:6467 (120) | 100.0 (EntityMantis.java:~50) | 120 (MobStats.MANTIS) |
| EntityMantis | ATK | OreSpawnMain.java:6467 (16) | 12.0 | 16 (MobStats.MANTIS) |
| EntityMantis | ARMOR | OreSpawnMain.java:6467 (10) | absent | 10 (MobStats.MANTIS) |
| EntityMantis | SPEED | orig Mantis.java:68 (0.32) | 0.32 | CONFIRMED 0.32 |
| EntityMantis | XP | orig Mantis.java:59 (100) | 100 (EntityMantis.java:44) | CONFIRMED 100 |
| EntityHerculesBeetle | HP | OreSpawnMain.java:6468 (250) | 200.0 | 250 (MobStats.HERCULES_BEETLE) |
| EntityHerculesBeetle | ATK | OreSpawnMain.java:6468 (30) | 15.0 | 30 (MobStats.HERCULES_BEETLE) |
| EntityHerculesBeetle | ARMOR | OreSpawnMain.java:6468 (19) | absent | 19 (MobStats.HERCULES_BEETLE) |
| EntityHerculesBeetle | XP | orig HerculesBeetle.java:48 (200) | 200 (EntityHerculesBeetle.java:40) | CONFIRMED 200 |
| Mothra | HP | OreSpawnMain.java:6469 (150) | 500.0 (Mothra.java:~) | 150 (MobStats.MOTHRA) |
| Mothra | ATK | OreSpawnMain.java:6469 (12) | 30.0 | 12 (MobStats.MOTHRA) |
| Mothra | ARMOR | OreSpawnMain.java:6469 (8) | absent | 8 (MobStats.MOTHRA) |
| Mothra | XP | orig Mothra.java:67 (100) | 100 (port Mothra.java:60) | CONFIRMED 100 |
| EntityBrutalfly | HP | OreSpawnMain.java:6470 (110) | 500.0 | 110 (MobStats.BRUTALFLY) |
| EntityBrutalfly | ATK | OreSpawnMain.java:6470 (10) | 18.0 | 10 (MobStats.BRUTALFLY) |
| EntityBrutalfly | ARMOR | OreSpawnMain.java:6470 (6) | absent | 6 (MobStats.BRUTALFLY) |
| EntityBrutalfly | SPEED | orig Brutalfly.java:51 (0.35) | 0.35 | CONFIRMED 0.35 |
| EntityBrutalfly | XP | orig Brutalfly.java:57 (100) | 100 (EntityBrutalfly.java:39) | CONFIRMED 100 |
| Nastysaurus | HP | OreSpawnMain.java:6471 (200) | 100.0 | 200 (MobStats.NASTYSAURUS) |
| Nastysaurus | ATK | OreSpawnMain.java:6471 (32) | 25.0 | 32 (MobStats.NASTYSAURUS) |
| Nastysaurus | ARMOR | OreSpawnMain.java:6471 (17) | absent | 17 (MobStats.NASTYSAURUS) |
| Nastysaurus | XP | orig Nastysaurus.java:50 (40) | 40 (port Nastysaurus.java:38) | CONFIRMED 40 |
| Pointysaurus | HP | OreSpawnMain.java:6472 (80) | 80.0 | CONFIRMED 80 (now MobStats.POINTYSAURUS) |
| Pointysaurus | ATK | OreSpawnMain.java:6472 (10) | 20.0 | 10 (MobStats.POINTYSAURUS) |
| Pointysaurus | ARMOR | OreSpawnMain.java:6472 (16) | absent | 16 (MobStats.POINTYSAURUS) |
| Pointysaurus | XP | orig Pointysaurus.java:47 (40) | 40 (port Pointysaurus.java:39) | CONFIRMED 40 |
| Alosaurus | HP | OreSpawnMain.java:6473 (110) | 60.0 | 110 (MobStats.ALOSAURUS) |
| Alosaurus | ATK | OreSpawnMain.java:6473 (18) | 15.0 | 18 (MobStats.ALOSAURUS) |
| Alosaurus | ARMOR | OreSpawnMain.java:6473 (8) | absent | 8 (MobStats.ALOSAURUS) |
| Alosaurus | XP | orig Alosaurus.java:46 (40) | 40 (port Alosaurus.java:41) | CONFIRMED 40 |
| SpiderRobot | HP | OreSpawnMain.java:6474 (1500) | 500.0 (SpiderRobot.java:~78) | 1500 (MobStats.SPIDER_ROBOT) |
| SpiderRobot | ATK | OreSpawnMain.java:6474 (100) | 50.0 | 100 (MobStats.SPIDER_ROBOT) |
| SpiderRobot | ARMOR | OreSpawnMain.java:6474 (16) | 8.0 | 16 (MobStats.SPIDER_ROBOT) |
| SpiderRobot | melee float | orig SpiderRobot.java:83 (uses attack attribute) | hardcoded 50.0f in doHurtTarget | `(float) getAttributeValue(ATTACK_DAMAGE)` |
| SpiderRobot | XP | orig SpiderRobot.java:64 (`health/2` = 750) | 200 (SpiderRobot.java:60) | 750 |
| AntRobot | HP | OreSpawnMain.java:6475 (300) | 350.0 (AntRobot.java:73) | 300 (MobStats.ANT_ROBOT) |
| AntRobot | ATK | OreSpawnMain.java:6475 (30) | 35.0 (AntRobot.java:75) | 30 (MobStats.ANT_ROBOT) |
| AntRobot | ARMOR | OreSpawnMain.java:6475 (16) | 6.0 (AntRobot.java:76) | 16 (MobStats.ANT_ROBOT) |
| AntRobot | melee float | orig AntRobot.java:1079 (`AntRobot_stats.attack`) | hardcoded 35.0f (port AntRobot.java:196) | `(float) getAttributeValue(ATTACK_DAMAGE)` |
| AntRobot | XP | orig AntRobot.java:58 (`health/2` = 150) | 150 (AntRobot.java:63) | CONFIRMED 150 |
| GiantRobot | HP | OreSpawnMain.java:6476 "Jeffery" (550) | 2000.0 | 550 (MobStats.JEFFERY) |
| GiantRobot | ATK | OreSpawnMain.java:6476 (40) | 100.0 | 40 (MobStats.JEFFERY) |
| GiantRobot | ARMOR | OreSpawnMain.java:6476 (18) | 12.0 | 18 (MobStats.JEFFERY) |
| GiantRobot | SPEED | orig GiantRobot.java:42 (0.55) | 0.55 | CONFIRMED 0.55 |
| GiantRobot | XP | orig GiantRobot.java:48 (`Jeffery_stats.health/2` = 275) | 500 (GiantRobot.java:46) | 275 |
| Jeffery (skin alias) | HP | OreSpawnMain.java:6476 (550) | 1000.0 | 550 (MobStats.JEFFERY) |
| Jeffery | ATK | OreSpawnMain.java:6476 (40) | 50.0 | 40 (MobStats.JEFFERY) |
| Jeffery | ARMOR | OreSpawnMain.java:6476 (18) | 6.0 | 18 (MobStats.JEFFERY) |
| Jeffery | XP | orig GiantRobot.java:48 (275) | 250 (Jeffery.java:23) | 275 |
| Hammerhead | HP | OreSpawnMain.java:6477 (240) | 200 (Hammerhead.java:38) | 240 (MobStats.HAMMERHEAD) |
| Hammerhead | ATK | OreSpawnMain.java:6477 (75) | 20.0 (Hammerhead.java:40) | 75 (MobStats.HAMMERHEAD) |
| Hammerhead | ARMOR | OreSpawnMain.java:6477 (20) | absent | 20 (MobStats.HAMMERHEAD) |
| Hammerhead | XP | orig Hammerhead.java:46 (350) | 350 (port Hammerhead.java:49) | CONFIRMED 350 |
| EntityMolenoid | HP | OreSpawnMain.java:6478 (200) | 100.0 | 200 (MobStats.MOLENOID) |
| EntityMolenoid | ATK | OreSpawnMain.java:6478 (18) | 10.0 | 18 (MobStats.MOLENOID) |
| EntityMolenoid | ARMOR | OreSpawnMain.java:6478 (12) | absent | 12 (MobStats.MOLENOID) |
| EntityMolenoid | XP | orig Molenoid.java:45 (40) | 40 (EntityMolenoid.java:52) | CONFIRMED 40 |
| TRex | HP | OreSpawnMain.java:6479 (160) | 200.0 | 160 (MobStats.TREX) |
| TRex | ATK | OreSpawnMain.java:6479 (22) | 30.0 | 22 (MobStats.TREX) |
| TRex | ARMOR | OreSpawnMain.java:6479 (14) | absent | 14 (MobStats.TREX) |
| TRex | XP | orig TRex.java:48 (150) | 150 (port TRex.java:39) | CONFIRMED 150 |
| BandP | HP | OreSpawnMain.java:6480 (100); orig BandP.java:94 | 30 (BandP.java:38) | 100 (MobStats.BANDP) |
| BandP | ATK | OreSpawnMain.java:6480 (1); orig BandP.java:68 | 5 (BandP.java:39) | 1 (MobStats.BANDP) |
| BandP | ARMOR | OreSpawnMain.java:6480 (18); orig BandP.java:98 | absent | 18 (MobStats.BANDP) |
| BandP | XP | orig BandP.java:53 (1000) | 10 (BandP.java:51) | 1000 |
| EntityCaterKiller | HP | OreSpawnMain.java:6481 (450) | 350.0 | 450 (MobStats.CATERKILLER) |
| EntityCaterKiller | ATK | OreSpawnMain.java:6481 (32) | 20.0 | 32 (MobStats.CATERKILLER) |
| EntityCaterKiller | ARMOR | OreSpawnMain.java:6481 (19) | absent | 19 (MobStats.CATERKILLER) |
| EntityCaterKiller | XP | orig CaterKiller.java:60 (200) | 200 (EntityCaterKiller.java:67) | CONFIRMED 200 |
| Cryolophosaurus | HP | OreSpawnMain.java:6482 (10) | 20.0 | 10 (MobStats.CRYOLOPHOSAURUS) |
| Cryolophosaurus | ATK | OreSpawnMain.java:6482 (3) | 5.0 | 3 (MobStats.CRYOLOPHOSAURUS) |
| Cryolophosaurus | ARMOR | OreSpawnMain.java:6482 (1) | absent | 1 (MobStats.CRYOLOPHOSAURUS) |
| Cryolophosaurus | XP | orig Cryolophosaurus.java:49 (10) | 10 (port :25) | CONFIRMED 10 |
| EntityRat | HP | OreSpawnMain.java:6483 (5) | 10.0 | 5 (MobStats.RAT) |
| EntityRat | ATK | OreSpawnMain.java:6483 (3) | 2.0 | 3 (MobStats.RAT) |
| EntityRat | ARMOR | OreSpawnMain.java:6483 (1) | absent | 1 (MobStats.RAT) |
| EntityRat | XP | orig Rat.java:54 (5) | 5 (EntityRat.java:49) | CONFIRMED 5 |
| Urchin | HP | OreSpawnMain.java:6484 (25) | 30 (Urchin.java:33) | 25 (MobStats.URCHIN) |
| Urchin | ATK | OreSpawnMain.java:6484 (10) | 8.0 (Urchin.java:35) | 10 (MobStats.URCHIN) |
| Urchin | ARMOR | OreSpawnMain.java:6484 (4) | absent | 4 (MobStats.URCHIN) |
| Urchin | XP | orig Urchin.java:52 (20) | 20 (port Urchin.java:39) | CONFIRMED 20 |
| EntityKyuubi | HP | OreSpawnMain.java:6485 (125) | 30.0 | 125 (MobStats.KYUUBI) |
| EntityKyuubi | ATK | OreSpawnMain.java:6485 (10) | 3.0 | 10 (MobStats.KYUUBI) |
| EntityKyuubi | ARMOR | OreSpawnMain.java:6485 (10) | absent | 10 (MobStats.KYUUBI) |
| EntityKyuubi | XP | orig Kyuubi.java:46 (30) | 30 (EntityKyuubi.java:37) | CONFIRMED 30 |
| EntityGammaMetroid | HP | OreSpawnMain.java:6486 (100) | 60.0 | 100 (MobStats.GAMMA_METROID) |
| EntityGammaMetroid | ATK | OreSpawnMain.java:6486 (10) | 8.0 | 10 (MobStats.GAMMA_METROID) |
| EntityGammaMetroid | ARMOR | OreSpawnMain.java:6486 (12) | absent | 12 (MobStats.GAMMA_METROID) |
| EntityGammaMetroid | XP | orig GammaMetroid.java:57 (20) | 20 (port :57) | CONFIRMED 20 |
| Basilisk | HP | OreSpawnMain.java:6487 (200) | 500.0 (Basilisk.java:71) | 200 (MobStats.BASILISK) |
| Basilisk | ATK | OreSpawnMain.java:6487 (24) | 25.0 | 24 (MobStats.BASILISK) |
| Basilisk | ARMOR | OreSpawnMain.java:6487 (15) | 8.0 | 15 (MobStats.BASILISK) |
| Basilisk | XP | orig Basilisk.java:50 (150) | 150 (port Basilisk.java:49) | CONFIRMED 150 |
| EntityEmperorScorpion | HP | OreSpawnMain.java:6488 (350) | 300.0 | 350 (MobStats.EMPEROR_SCORPION) |
| EntityEmperorScorpion | ATK | OreSpawnMain.java:6488 (35) | 20.0 | 35 (MobStats.EMPEROR_SCORPION) |
| EntityEmperorScorpion | ARMOR | OreSpawnMain.java:6488 (20) | absent | 20 (MobStats.EMPEROR_SCORPION) |
| EntityEmperorScorpion | XP | orig EmperorScorpion.java:61 (200) | 200 (port :64) | CONFIRMED 200 |
| EntityTrooperBug | HP | OreSpawnMain.java:6489 (200) | 200.0 | CONFIRMED 200 (now MobStats.TROOPER_BUG) |
| EntityTrooperBug | ATK | OreSpawnMain.java:6489 (20) | 16.0 | 20 (MobStats.TROOPER_BUG) |
| EntityTrooperBug | ARMOR | OreSpawnMain.java:6489 (15) | absent | 15 (MobStats.TROOPER_BUG) |
| EntityTrooperBug | XP | orig TrooperBug.java:60 (150) | 150 (port :46) | CONFIRMED 150 |
| EntitySpitBug | HP | OreSpawnMain.java:6490 (100) | 60.0 | 100 (MobStats.SPIT_BUG) |
| EntitySpitBug | ATK | OreSpawnMain.java:6490 (10) | 8.0 | 10 (MobStats.SPIT_BUG) |
| EntitySpitBug | ARMOR | OreSpawnMain.java:6490 (12) | absent | 12 (MobStats.SPIT_BUG) |
| EntitySpitBug | XP | orig SpitBug.java:58 (50) | 50 (port :43) | CONFIRMED 50 |
| Alien | HP | OreSpawnMain.java:6491 (100) | 80 (Alien.java:39) | 100 (MobStats.ALIEN) |
| Alien | ATK | OreSpawnMain.java:6491 (12) | 12 (Alien.java:40) | CONFIRMED 12 (now MobStats.ALIEN) |
| Alien | ARMOR | OreSpawnMain.java:6491 (8) | 6.0 (Alien.java:71) | 8 (MobStats.ALIEN) |
| Alien | XP | orig Alien.java:55 (100) | 100 (port Alien.java:48) | CONFIRMED 100 |
| WaterDragon | HP | OreSpawnMain.java:6492 (150) | 200 (WaterDragon.java:56) | 150 (MobStats.WATER_DRAGON) |
| WaterDragon | ATK | OreSpawnMain.java:6492 (20) | 20.0 (WaterDragon.java:59) | CONFIRMED 20 (now MobStats.WATER_DRAGON) |
| WaterDragon | ARMOR | OreSpawnMain.java:6492 (8) | absent | 8 (MobStats.WATER_DRAGON) |
| WaterDragon | melee float | attribute-driven | hardcoded `(float) ATTACK_DAMAGE` constant | `(float) getAttributeValue(ATTACK_DAMAGE)` |
| WaterDragon | XP | orig WaterDragon.java:64 (100) | 100 (port :67) | CONFIRMED 100 |
| SeaMonster | HP | OreSpawnMain.java:6493 (110) | 150 (SeaMonster.java:38) | 110 (MobStats.SEA_MONSTER) |
| SeaMonster | ATK | OreSpawnMain.java:6493 (14) | 15.0 (SeaMonster.java:40) | 14 (MobStats.SEA_MONSTER) |
| SeaMonster | ARMOR | OreSpawnMain.java:6493 (8) | absent | 8 (MobStats.SEA_MONSTER) |
| SeaMonster | XP | orig SeaMonster.java:52 (150) | 150 (port :49) | CONFIRMED 150 |
| SeaViper | HP | OreSpawnMain.java:6494 (160) | 120 (SeaViper.java:46) | 160 (MobStats.SEA_VIPER) |
| SeaViper | ATK | OreSpawnMain.java:6494 (22) | 12.0 (SeaViper.java:49) | 22 (MobStats.SEA_VIPER) |
| SeaViper | ARMOR | OreSpawnMain.java:6494 (12) | absent | 12 (MobStats.SEA_VIPER) |
| SeaViper | XP | orig SeaViper.java:56 (120) | 120 (port :57) | CONFIRMED 120 |
| Robot2 | HP | OreSpawnMain.java:6495 (200) | 500.0 (Robot2.java:66) | 200 (MobStats.ROBOT2) |
| Robot2 | ATK | OreSpawnMain.java:6495 (22) | 30.0 | 22 (MobStats.ROBOT2) |
| Robot2 | ARMOR | OreSpawnMain.java:6495 (18) | 8.0 | 18 (MobStats.ROBOT2) |
| Robot2 | XP | orig Robot2.java:47 (100) | 100 (port :51) | CONFIRMED 100 |
| Robot3 | HP | OreSpawnMain.java:6496 (80) | 300.0 (Robot3.java:64) | 80 (MobStats.ROBOT3) |
| Robot3 | ATK | OreSpawnMain.java:6496 (16) | 20.0 | 16 (MobStats.ROBOT3) |
| Robot3 | ARMOR | OreSpawnMain.java:6496 (14) | 6.0 | 14 (MobStats.ROBOT3) |
| Robot3 | XP | orig Robot3.java:48 (60) | 60 (port :49) | CONFIRMED 60 |
| Robot4 | HP | OreSpawnMain.java:6497 (170) | 750.0 (Robot4.java:93) | 170 (MobStats.ROBOT4) |
| Robot4 | ATK | OreSpawnMain.java:6497 (12) | 40.0 | 12 (MobStats.ROBOT4) |
| Robot4 | ARMOR | OreSpawnMain.java:6497 (18) | 10.0 | 18 (MobStats.ROBOT4) |
| Robot4 | XP | orig Robot4.java:51 (120) | 120 (port :78) | CONFIRMED 120 |
| Robot5 | HP | OreSpawnMain.java:6498 (20) | 150.0 (Robot5.java:65) | 20 (MobStats.ROBOT5) |
| Robot5 | ATK | OreSpawnMain.java:6498 (5) | 15.0 | 5 (MobStats.ROBOT5) |
| Robot5 | ARMOR | OreSpawnMain.java:6498 (6) | 4.0 | 6 (MobStats.ROBOT5) |
| Robot5 | XP | orig Robot5.java:47 (20) | 20 (port :50) | CONFIRMED 20 |
| EntityRotator | HP | OreSpawnMain.java:6499 (35) | 30.0 | 35 (MobStats.ROTATOR) |
| EntityRotator | ATK | OreSpawnMain.java:6499 (10) | 5.0 | 10 (MobStats.ROTATOR) |
| EntityRotator | ARMOR | OreSpawnMain.java:6499 (8) | absent | 8 (MobStats.ROTATOR) |
| EntityRotator | XP | orig Rotator.java:57 (35) | 35 (port :34) | CONFIRMED 35 |
| EntityVortex | HP | OreSpawnMain.java:6500 (150) | 200.0 | 150 (MobStats.VORTEX) |
| EntityVortex | ATK | OreSpawnMain.java:6500 (26) | 20.0 | 26 (MobStats.VORTEX) |
| EntityVortex | ARMOR | OreSpawnMain.java:6500 (10) | absent | 10 (MobStats.VORTEX) |
| EntityVortex | XP | orig Vortex.java:51 (200) | 200 (port :57) | CONFIRMED 200 |
| DungeonBeast | HP | OreSpawnMain.java:6501 (65) | 60 (DungeonBeast.java:29) | 65 (MobStats.DUNGEON_BEAST) |
| DungeonBeast | ATK | OreSpawnMain.java:6501 (12) | 10 (DungeonBeast.java:30) | 12 (MobStats.DUNGEON_BEAST) |
| DungeonBeast | ARMOR | OreSpawnMain.java:6501 (6) | 4.0 | 6 (MobStats.DUNGEON_BEAST) |
| DungeonBeast | XP | orig DungeonBeast.java:50 (60) | 60 (port :34) | CONFIRMED 60 |
| EntityTriffid | HP | OreSpawnMain.java:6502 (100) | 100.0 | CONFIRMED 100 (now MobStats.TRIFFID) |
| EntityTriffid | ATK | OreSpawnMain.java:6502 (20) | 8.0 | 20 (MobStats.TRIFFID) |
| EntityTriffid | ARMOR | OreSpawnMain.java:6502 (12) | absent | 12 (MobStats.TRIFFID) |
| EntityTriffid | XP | orig Triffid.java:51 (50) | 50 (port :41) | CONFIRMED 50 |
| EntityLurkingTerror | HP | OreSpawnMain.java:6503 (30) | 40.0 | 30 (MobStats.LURKING_TERROR) |
| EntityLurkingTerror | ATK | OreSpawnMain.java:6503 (6) | 5.0 | 6 (MobStats.LURKING_TERROR) |
| EntityLurkingTerror | ARMOR | OreSpawnMain.java:6503 (5) | absent | 5 (MobStats.LURKING_TERROR) |
| EntityLurkingTerror | XP | orig LurkingTerror.java:55 (20) | 20 (port :39) | CONFIRMED 20 |
| EntityWormSmall | HP | OreSpawnMain.java:6504 (10) | 10.0 | CONFIRMED 10 (now MobStats.WORM_SMALL) |
| EntityWormSmall | ATK | OreSpawnMain.java:6504 (3) | 3.0 | CONFIRMED 3 (now MobStats.WORM_SMALL) |
| EntityWormSmall | ARMOR | OreSpawnMain.java:6504 (0) | absent | 0 (MobStats.WORM_SMALL) |
| EntityWormSmall | XP | orig WormSmall.java:29 (0) | 0 (port :26) | CONFIRMED 0 |
| EntityWormMedium | HP | OreSpawnMain.java:6505 (30) | 30.0 | CONFIRMED 30 (now MobStats.WORM_MEDIUM) |
| EntityWormMedium | ATK | OreSpawnMain.java:6505 (10) | 6.0 | 10 (MobStats.WORM_MEDIUM) |
| EntityWormMedium | ARMOR | OreSpawnMain.java:6505 (8) | absent | 8 (MobStats.WORM_MEDIUM) |
| EntityWormMedium | XP | orig WormMedium.java:31 (0) | 0 (port :28) | CONFIRMED 0 |
| EntityWormLarge | HP | OreSpawnMain.java:6506 (90) | 100.0 | 90 (MobStats.WORM_LARGE) |
| EntityWormLarge | ATK | OreSpawnMain.java:6506 (18) | 15.0 | 18 (MobStats.WORM_LARGE) |
| EntityWormLarge | ARMOR | OreSpawnMain.java:6506 (14) | absent | 14 (MobStats.WORM_LARGE) |
| EntityWormLarge | XP | orig WormLarge.java:44 (2050 — weird stays weird) | 2050 (port :34) | CONFIRMED 2050 |
| EnderKnight | HP | OreSpawnMain.java:6507 (60) | 80 (EnderKnight.java:31) | 60 (MobStats.ENDER_KNIGHT) |
| EnderKnight | ATK | OreSpawnMain.java:6507 (12) | 15 (EnderKnight.java:32) | 12 (MobStats.ENDER_KNIGHT) |
| EnderKnight | ARMOR | OreSpawnMain.java:6507 (6) | absent | 6 (MobStats.ENDER_KNIGHT) |
| EnderReaper | HP | OreSpawnMain.java:6508 (90) | 120 (EnderReaper.java:29) | 90 (MobStats.ENDER_REAPER) |
| EnderReaper | ATK | OreSpawnMain.java:6508 (18) | 20 (EnderReaper.java:30) | 18 (MobStats.ENDER_REAPER) |
| EnderReaper | ARMOR | OreSpawnMain.java:6508 (8) | absent | 8 (MobStats.ENDER_REAPER) |
| Irukandji | HP | OreSpawnMain.java:6509 (1) | 5 (Irukandji.java:35) | 1 (MobStats.IRUKANDJI) |
| Irukandji | ATK | OreSpawnMain.java:6509 (20) | 200.0 (Irukandji.java:37 — audit right: the 200 belongs only to the empty-hand retaliation, orig Irukandji.java:96) | 20 (MobStats.IRUKANDJI) |
| Irukandji | ARMOR | OreSpawnMain.java:6509 (0) | absent | 0 (MobStats.IRUKANDJI) |
| Irukandji | XP | orig Irukandji.java:44 (50) | 50 (port :48) | CONFIRMED 50 |
| AttackSquid | HP | OreSpawnMain.java:6510 (10) | 30 (AttackSquid.java:39) | 10 (MobStats.ATTACK_SQUID) |
| AttackSquid | ATK | OreSpawnMain.java:6510 (8) | 8.0 (AttackSquid.java:41) | CONFIRMED 8 (now MobStats.ATTACK_SQUID) |
| AttackSquid | ARMOR | OreSpawnMain.java:6510 (0) | absent | 0 (MobStats.ATTACK_SQUID) |
| AttackSquid | XP | orig AttackSquid.java:67 (15) | 15 (port :52) | CONFIRMED 15 |
| CaveFisher | HP | OreSpawnMain.java:6511 (10) | 25 (CaveFisher.java:31) | 10 (MobStats.CAVE_FISHER) |
| CaveFisher | ATK | OreSpawnMain.java:6511 (4) | 6.0 (CaveFisher.java:33) | 4 (MobStats.CAVE_FISHER) |
| CaveFisher | ARMOR | OreSpawnMain.java:6511 (4) | absent | 4 (MobStats.CAVE_FISHER) |
| CaveFisher | XP | orig CaveFisher.java:46 (10) | 10 (port :45) | CONFIRMED 10 |
| CloudShark | HP | OreSpawnMain.java:6512 (15) | 20 (CloudShark.java:24) | 15 (MobStats.CLOUD_SHARK) |
| CloudShark | ATK | OreSpawnMain.java:6512 (6) | 6.0 (CloudShark.java:25) | CONFIRMED 6 (now MobStats.CLOUD_SHARK) |
| CloudShark | ARMOR | OreSpawnMain.java:6512 (5) | absent | 5 (MobStats.CLOUD_SHARK) |
| CloudShark | XP | orig CloudShark.java:42 (5) | 5 (port :35) | CONFIRMED 5 |
| CreepingHorror | HP | OreSpawnMain.java:6513 (10) | 20 (CreepingHorror.java:32) | 10 (MobStats.CREEPING_HORROR) |
| CreepingHorror | ATK | OreSpawnMain.java:6513 (3) | 6 (CreepingHorror.java:33) | 3 (MobStats.CREEPING_HORROR) |
| CreepingHorror | ARMOR | OreSpawnMain.java:6513 (2) | absent | 2 (MobStats.CREEPING_HORROR) |
| CreepingHorror | XP | orig CreepingHorror.java:49 (5) | 5 (port :37) | CONFIRMED 5 |
| Godzilla | HP | OreSpawnMain.java:6514 "Mobzilla" (4000) | 6000.0 (Godzilla.java:113) | 4000 (MobStats.GODZILLA) |
| Godzilla | ATK | OreSpawnMain.java:6514 (175) | 150.0 | 175 (MobStats.GODZILLA) |
| Godzilla | ARMOR | OreSpawnMain.java:6514 (21); boost to 25 vs "large unknown" is orig Godzilla.java:145-150 | absent | 21 base (MobStats.GODZILLA) |
| Godzilla | SPEED | orig Godzilla.java:107 (0.75) | 0.75 | CONFIRMED 0.75 |
| Godzilla | XP | orig Godzilla.java:77 (10000) | 10000 (port :92) | CONFIRMED 10000 |
| Kraken | HP | OreSpawnMain.java:6515 (1000) | 3000.0 (Kraken.java:78) | 1000 (MobStats.KRAKEN) |
| Kraken | ATK | OreSpawnMain.java:6515 (40) | 80.0 | 40 (MobStats.KRAKEN) |
| Kraken | ARMOR | OreSpawnMain.java:6515 (10) | 8.0 | 10 (MobStats.KRAKEN) |
| Kraken | SPEED | orig Kraken.java:90 (0.37) | 0.5 | 0.37 |
| Kraken | XP | orig Kraken.java:78 (500) | 500 (port :66) | CONFIRMED 500 |
| EntityLeafMonster | HP | OreSpawnMain.java:6516 (6) | 20.0 | 6 (MobStats.LEAF_MONSTER) |
| EntityLeafMonster | ATK | OreSpawnMain.java:6516 (2) | 5.0 | 2 (MobStats.LEAF_MONSTER) |
| EntityLeafMonster | ARMOR | OreSpawnMain.java:6516 (1) | absent | 1 (MobStats.LEAF_MONSTER) |
| EntityLeafMonster | XP | orig LeafMonster.java:43 (5) | 5 (port :34) | CONFIRMED 5 |
| PitchBlack | HP base | OreSpawnMain.java:6517 "Nightmare" (250) | 500.0 envelope (PitchBlack.java:131) | 250 (MobStats.PITCH_BLACK) |
| PitchBlack | ATK base | OreSpawnMain.java:6517 (30) | 50.0 | 30 (MobStats.PITCH_BLACK) |
| PitchBlack | ARMOR base | OreSpawnMain.java:6517 (10) | 6.0 | 10 (MobStats.PITCH_BLACK) |
| PitchBlack | per-tier HP | orig PitchBlack.java:239 (250×t, t∈{0.5,1,2,3,4}) | {125,250,500,750,1000} | CONFIRMED {125,250,500,750,1000} |
| PitchBlack | per-tier ATK | orig PitchBlack.java:80 (30×t) | {15,30,60,90,120} | CONFIRMED {15,30,60,90,120} |
| PitchBlack | per-tier ARMOR | orig PitchBlack.java:190 (10+2t → {11,12,14,16,18}) | {10,12,14,16,18} (tier-1 wrong) | {11,12,14,16,18} |
| PitchBlack | per-tier SPEED | orig PitchBlack.java:79 (0.2+0.1t → {0.25,0.3,0.4,0.5,0.6}) | fixed 0.3 for all tiers | per-tier {0.25,0.3,0.4,0.5,0.6} |
| PitchBlack | per-tier XP | orig PitchBlack.java:143,152 (100×t → {50,100,200,300,400}) | {200,250,350,450,600} | {50,100,200,300,400} |
| EntityScorpion | HP | OreSpawnMain.java:6518 (15) | 20.0 | 15 (MobStats.SCORPION) |
| EntityScorpion | ATK | OreSpawnMain.java:6518 (4) | 4.0 | CONFIRMED 4 (now MobStats.SCORPION) |
| EntityScorpion | ARMOR | OreSpawnMain.java:6518 (10) | absent | 10 (MobStats.SCORPION) |
| EntityScorpion | XP | orig Scorpion.java:52 (10) | 10 (port :30) | CONFIRMED 10 |
| Skate | HP | OreSpawnMain.java:6519 (8) | 15 (Skate.java:35) | 8 (MobStats.SKATE) |
| Skate | ATK | OreSpawnMain.java:6519 (8) | 4.0 (Skate.java:37) | 8 (MobStats.SKATE) |
| Skate | ARMOR | OreSpawnMain.java:6519 (4) | absent | 4 (MobStats.SKATE) |
| Skate | XP | orig Skate.java:45 (10) | 10 (port :44) | CONFIRMED 10 |
| EntityTerribleTerror | HP | OreSpawnMain.java:6520 (10) | 20.0 | 10 (MobStats.TERRIBLE_TERROR) |
| EntityTerribleTerror | ATK | OreSpawnMain.java:6520 (5) | 5.0 | CONFIRMED 5 (now MobStats.TERRIBLE_TERROR) |
| EntityTerribleTerror | ARMOR | OreSpawnMain.java:6520 (3) | absent | 3 (MobStats.TERRIBLE_TERROR) |
| EntityTerribleTerror | XP | orig TerribleTerror.java:53 (10) | 10 (port :31) | CONFIRMED 10 |
| TheKing | HP | OreSpawnMain.java:6521 (7000) | 6000 (TheKing.java:109) | 7000 (MobStats.THE_KING) |
| TheKing | ATK | OreSpawnMain.java:6521 (350) | 250.0 (TheKing.java:110) | 350 (MobStats.THE_KING) — phase ×2/×4/×8/×16 multipliers retained on the new base |
| TheKing | ARMOR | OreSpawnMain.java:6521 (21 base; +1..+3/25 boosts orig TheKing.java:856-864) | 12 (TheKing.java:111) | 21 base (MobStats.THE_KING); getArmorValue boosts retained |
| TheKing | SPEED | orig TheKing.java:105 (0.62) | 0.62 | CONFIRMED 0.62 |
| TheKing | XP | orig TheKing.java:91 (25000) | 25000 (port :146) | CONFIRMED 25000 |
| TheQueen | HP | OreSpawnMain.java:6522 (6000) | 6000 (TheQueen.java:155) | CONFIRMED 6000 (now MobStats.THE_QUEEN) |
| TheQueen | ATK | OreSpawnMain.java:6522 (225) | 200.0 (TheQueen.java:157) | 225 (MobStats.THE_QUEEN) — phase multipliers retained on the new base |
| TheQueen | ARMOR | OreSpawnMain.java:6522 (21 base; boosts orig TheQueen.java:819-827) | 10 (TheQueen.java:158) | 21 base (MobStats.THE_QUEEN) |
| TheQueen | SPEED | orig TheQueen.java:98 (0.62) | 0.62 | CONFIRMED 0.62 |
| TheQueen | XP | orig TheQueen.java:84 (25000) | 25000 (port :195) | CONFIRMED 25000 |
| EntityLeon | HP | orig Leon.java:169 (250 — entity override of table 150) | 250.0 (EntityLeon.java:93) | CONFIRMED 250 |
| EntityLeon | ATK | orig Leon.java:117 (55) | 55.0 (EntityLeon.java:95) | CONFIRMED 55 |
| EntityLeon | ARMOR | orig Leon.java:192 (16) | absent | 16 |
| EntityLeon | SPEED | orig Leon.java:81 (0.25) | 0.25 | CONFIRMED 0.25 |
| EntityLeon | XP | orig Leon.java:82 (300) | 300 (EntityLeon.java:73) | CONFIRMED 300 |
| Leonopteryx | HP | orig Leon.java:169 (250) | 300.0 (Leonopteryx.java:71 — invented) | 250 |
| Leonopteryx | ATK | orig Leon.java:117 (55) | 40.0 (Leonopteryx.java:72) | 55 |
| Leonopteryx | ARMOR | orig Leon.java:192 (16) | absent | 16 |
| Leonopteryx | XP | orig Leon.java:82 (300) | 120 (Leonopteryx.java:89) | 300 |
| Crab | HP | orig Crab.java:137 (`PitchBlack_stats.health`=250 ×scale — original bug kept) | flat 100 (Crab.java:38, never scaled) | 250×scale via applyScaleStats() |
| Crab | ATK | orig Crab.java:71 (`Crab_stats.attack`=24 ×scale; OreSpawnMain.java:6524) | flat 10 (Crab.java:39) | 24×scale via applyScaleStats() |
| Crab | ARMOR | orig Crab.java:141 (16 + 2×scale) | flat 6.0 (Crab.java:65) | 16+2×scale via applyScaleStats() |
| Crab | melee float | attribute-driven (orig uses scaled attack) | `BASE_ATTACK × scale` literal (Crab.java:105) | `(float) getAttributeValue(ATTACK_DAMAGE)` |
| Crab | XP | orig Crab.java:95,116 (400×scale) | fixed 150 (Crab.java:46) | 400×scale via applyScaleStats() |

### Non-table entities (stats findings, fixed from orig entity files)

| Entity (port file) | Stat | Orig citation (value) | Old port value (file:line) | New port value |
|---|---|---|---|---|
| Dragon | ARMOR | orig Dragon.java:215 (14) | absent (Dragon.java:100-107) | 14 |
| Dragon | HP | orig Dragon.java:192 (200) | 200.0 | CONFIRMED 200 |
| Dragon | ATK | orig Dragon.java:140 (35) | 35.0 | CONFIRMED 35 |
| ThePrincess | HP | orig ThePrincess.java:195 (400) | 500.0 (ThePrincess.java:85) | 400 |
| ThePrincess | SPEED | orig ThePrincess.java:81 (0.32) | 0.3 (ThePrincess.java:86) | 0.32 |
| ThePrincess | ARMOR | orig ThePrincess.java:335 (14) | 16.0 (ThePrincess.java:88) | 14 |
| ThePrincess | ATK | orig ThePrincess.java:102 (10) | 10.0 | CONFIRMED 10 |
| ThePrincess | XP | orig ThePrincess.java:94 (50) | 50 (port :62) | CONFIRMED 50 |
| ThePrince | HP | orig ThePrince.java:186 (500) | 500.0 | CONFIRMED 500 |
| ThePrince | ATK | orig ThePrince.java:102 (10) | 10.0 | CONFIRMED 10 |
| ThePrince | ARMOR | orig ThePrince.java:347 (16) | 16.0 | CONFIRMED 16 |
| ThePrince | SPEED | orig ThePrince.java:81 (0.32) | 0.32 | CONFIRMED 0.32 |
| ThePrince | XP | orig ThePrince.java:94 (50) | 50 (port :70) | CONFIRMED 50 |
| ThePrinceTeen | HP | orig ThePrinceTeen.java:230 (1500) | 1000.0 (ThePrinceTeen.java:90) | 1500 |
| ThePrinceTeen | SPEED | orig ThePrinceTeen.java:87 (0.32) | 0.35 (ThePrinceTeen.java:91) | 0.32 |
| ThePrinceTeen | ARMOR | orig ThePrinceTeen.java:253 (18) | absent | 18 |
| ThePrinceTeen | ATK | orig ThePrinceTeen.java:141 (50) | 50.0 | CONFIRMED 50 |
| ThePrinceTeen | XP | orig ThePrinceTeen.java:105 (300) | 500 (port :68) | 300 |
| ThePrinceAdult | ARMOR | orig ThePrinceAdult.java:249 (20) | absent (ThePrinceAdult.java:86-92) | 20 |
| ThePrinceAdult | HP | orig ThePrinceAdult.java:226 (3000) | 3000.0 | CONFIRMED 3000 |
| ThePrinceAdult | ATK | orig ThePrinceAdult.java:137 (100) | 100.0 | CONFIRMED 100 |
| ThePrinceAdult | SPEED | orig ThePrinceAdult.java:86 (0.36) | 0.36 | CONFIRMED 0.36 |
| ThePrinceAdult | XP | orig ThePrinceAdult.java:102 (3000) | 3000 (port :66) | CONFIRMED 3000 |
| RubyBird | HP/SPEED | orig RubyBird.java has NO attribute overrides — inherits Cockateil (HP 2, speed 0.33, ATK 1; orig Cockateil.java:51-54,128) | HP 12 / speed 0.25 overrides (RubyBird.java:21-25, invented) | overrides removed; inherits `Cockateil.createAttributes()` |
| VelocityRaptor | tamed HP | orig VelocityRaptor.java:212 (`tamed ? 20 : 10`) | 10 always (VelocityRaptor.java:75) | MAX_HEALTH base→20 + heal-to-full on tame |
| EntityHydrolisc | ARMOR | orig Hydrolisc.java:161 (10) | absent (EntityHydrolisc.java:75-79) | 10 |
| EntityHydrolisc | HP | orig Hydrolisc.java:210 (100) | 100.0 | CONFIRMED 100 (audit's "60" is wrong) |
| EntityHydrolisc | SPEED | orig Hydrolisc.java:39 (0.25) | 0.25 | CONFIRMED 0.25 (audit's "0.2" is wrong) |
| EntityHydrolisc | XP | orig Hydrolisc.java:61 (5) | 5 (port :54) | CONFIRMED 5 |
| EntityRedAnt | HP | orig EntityRedAnt.java:53 (2) | 2.0 | CONFIRMED 2 (audit's "1" is wrong) |
| EntityRedAnt | SPEED | orig EntityRedAnt.java:34 (0.2) | 0.2 | CONFIRMED 0.2 (audit's "0.15" is wrong) |
| EntityRedAnt | XP | orig EntityRedAnt.java:35 (1) | 1 (port :23) | CONFIRMED 1 |

---

## 4. Audit finding IDs — disposition

FIXED (stats portion; non-stats portions of the same finding — sizes, immunities, sounds, drops, AI — left for their owners):

- **ENT-SYS2-002** — MobStats dead code: now the live source of truth; all 59 table consumers reference it.
- **ENT-A-002** Alien — HP 80→100, armor 6→8 per table 6491 (ATK 12 already correct).
- **ENT-A-008** Alosaurus — 60/15→110/18/8 per table 6473.
- **ENT-A-012** AntRobot — 350/35/6→300/30/16 per table 6475.
- **ENT-A-017** AttackSquid — HP 30→10 per table 6510.
- **ENT-A-023** BandP — 30/5/0 xp10→100/1/18 xp1000 (orig BandP.java:53,68,94,98).
- **ENT-A-031** Basilisk — 500/25/8→200/24/15 per table 6487 (fire immunity not mine).
- **ENT-A-040** Bee — 30/6→80/12/5 per table 6466 (size not mine).
- **ENT-A-060** Brutalfly — 500/18→110/10/6 per table 6470.
- **ENT-A-072** CaterKiller — 350/20→450/32/19 per table 6481.
- **ENT-A-078** CaveFisher — 25/6→10/4/4 per table 6511.
- **ENT-A-091** CloudShark — 20→15 HP, armor 5 added per table 6512.
- **ENT-A-100** Crab — scale now drives HP (250×s, PitchBlack bug preserved), ATK (24×s), armor (16+2s), XP (400×s) per orig Crab.java:71,95,116,137,141. (Size/scale-randomization at spawn not mine.)
- **ENT-A-103** Crab melee — damage now reads the (scaled) ATTACK_DAMAGE attribute.
- **ENT-A-106** CreepingHorror — 20/6→10/3/2 per table 6513 (size not mine).
- **ENT-A-111** Cryolophosaurus — 20/5/0→10/3/1 per table 6482.
- **ENT-D-001** Dragon — ARMOR 14 added (orig Dragon.java:215).
- **ENT-D-007** DungeonBeast — 60/10/4→65/12/6 per table 6501.
- **ENT-D-013** EmperorScorpion — 300/20→350/35/20 per table 6488.
- **ENT-D-017** EnderKnight — 80/15→60/12/6 per table 6507.
- **ENT-D-019** EnderReaper — 120/20→90/18/8 per table 6508.
- **ENT-D-034** GammaMetroid — 60/8→100/10/12 per table 6486.
- **ENT-D-042** GiantRobot/Jeffery — both aligned to "Jeffery" 550/40/18, xp 275 (orig GiantRobot.java:48).
- **ENT-D-054** Hammerhead — ATK 20→75, armor 20 added, HP 200→240 per table 6477.
- **ENT-D-058** HerculesBeetle — 200/15→250/30/19 per table 6468.
- **ENT-D-062** Irukandji — HP 5→1, ATK 200→20 per table 6509 (200.0f belongs only to the empty-hand retaliation, orig Irukandji.java:96, which is preserved).
- **ENT-K-001** Kraken — 3000/80/8 spd0.5→1000/40/10 spd0.37 (table 6515; orig Kraken.java:90). Audit values verified correct.
- **ENT-K-006** Kyuubi — 30/3→125/10/10 per table 6485.
- **ENT-K-012** LeafMonster — 20/5→6/2/1 per table 6516.
- **ENT-K-016** Leon — ARMOR 16 added to EntityLeon; Leonopteryx aligned to orig Leon's 250/55/16, xp 120→300.
- **ENT-K-025** LurkingTerror — 40/5→30/6/5 per table 6503.
- **ENT-K-029** Mantis — 100/12→120/16/10 per table 6467.
- **ENT-K-034** Molenoid — 100/10→200/18/12 per table 6478.
- **ENT-K-038** Mothra — 500/30→150/12/8 per table 6469.
- **ENT-K-042** Nastysaurus — 100/25→200/32/17 per table 6471.
- **ENT-K-051** PitchBlack — per-tier speed restored (0.2+0.1t, orig PitchBlack.java:79); tier-1 armor 10→11; XP table corrected to 100×t. (The discrete-tier visual model itself is out of scope.)
- **ENT-K-054** Pointysaurus — ATK 20→10, armor 16 added per table 6472.
- **ENT-K-057** Rat — 10/2→5/3/1 per table 6483.
- **ENT-K-062** Robot2 — 500/30/8→200/22/18 per table 6495.
- **ENT-K-065** Robot3 — 300/20/6→80/16/14 per table 6496.
- **ENT-K-068** Robot4 — 750/40/10→170/12/18 per table 6497.
- **ENT-K-072** Robot5 — 150/15/4→20/5/6 per table 6498.
- **ENT-K-078** Rotator — 30/5→35/10/8 per table 6499.
- **ENT-K-086** RubyBird — invented overrides removed; inherits Cockateil stats (orig RubyBird.java defines none).
- **ENT-S-001** Scorpion — 20→15 HP, armor 10 added per table 6518.
- **ENT-S-006** SeaMonster — 150/15→110/14/8 per table 6493 (speed-boost dead code not mine).
- **ENT-S-009** SeaViper — 120/12→160/22/12 per table 6494.
- **ENT-S-013** Skate — 15/4→8/8/4 per table 6519 (the audit's "swapped" reading confirmed: orig is HP 8 / ATK 8).
- **ENT-S-020** SpiderRobot — 500/50/8→1500/100/16 per table 6474; melee float → attribute; xp 200→750.
- **ENT-S-024** SpitBug — 60/8→100/10/12 per table 6490.
- **ENT-S-038** TerribleTerror — 20→10 HP, armor 3 added per table 6520.
- **ENT-S-042** TRex — 200/30→160/22/14 per table 6479.
- **ENT-S-046** Triffid — ATK 8→20, armor 12 added per table 6502.
- **ENT-S-051** TrooperBug — ATK 16→20, armor 15 added per table 6489.
- **ENT-S-061** Urchin — 30/8→25/10/4 per table 6484 (fire immunity not mine).
- **ENT-S-063** VelocityRaptor — tamed max HP 20 applied on tame (orig VelocityRaptor.java:212).
- **ENT-S-068** Vortex — 200/20→150/26/10 per table 6500 (fire immunity not mine).
- **ENT-S-073** WaterDragon — 200→150 HP, armor 8 added per table 6492; melee float → attribute.
- **ENT-S-081** WormMedium — ATK 6→10, armor 8 added per table 6505.
- **ENT-S-084** WormLarge — 100/15→90/18/14 per table 6506.
- **BOSS-001** TheKing — 6000/250/12→7000/350/21 per table 6521; phase attack multipliers and getArmorValue boosts retained on the corrected bases.
- **BOSS-006** TheQueen — ATK 200→225, armor 10→21 per table 6522 (HP 6000 already correct).
- **BOSS-013** Godzilla — HP 6000→4000, ATK 150→175, armor 21 added per table 6514 ("Mobzilla").
- **BOSS-026** ThePrinceTeen — HP 1000→1500, speed 0.35→0.32, armor 18 added, xp 500→300 (orig ThePrinceTeen.java:87,105,230,253).
- **BOSS-030** ThePrinceAdult — armor 20 added (orig ThePrinceAdult.java:249).
- **BOSS-038** ThePrincess — HP 500→400, speed 0.3→0.32, armor 16→14 (orig ThePrincess.java:81,195,335).

VERIFIED-CORRECT (audit's claimed original value wrong; port left as-is, proof cited):

- **ENT-D-024** EntityRedAnt — audit claims orig HP 1 / speed 0.15; orig EntityRedAnt.java:53 returns **2** and :34 sets moveSpeed **0.2f**. Port already 2 / 0.2 / 1.0 → correct.
- **ENT-D-060** Hydrolisc — audit claims orig HP ~60 / speed 0.2; orig Hydrolisc.java:210 returns **100** and :39 sets moveSpeed **0.25f**. Port already 100 / 0.25 / 1.0 → correct. (Missing ARMOR 10, orig Hydrolisc.java:161, was a real gap the audit missed — added.)

Out of scope / not stats (skipped deliberately): ENT-A-013 (attack-rate throttle = AI), ENT-A-019/ENT-K-069/BOSS-022 (missing ranged attacks = AI), ENT-A-050/052 (projectiles), ENT-A-053/059 (Boyfriend armor-from-gear clamp, fire immunity, size = behavior/size), ENT-D-016/051, ENT-K-061, ENT-A-114/115/116 (cow drops/lineage), ENT-K-017..022 (Leon flight/taming/drops/spawns/sounds), ENT-S-003/017/018/021/064/069, ENT-K-087/088/089, ENT-SYS2-001 (double drops), sizes and spawn rules everywhere.

---

## 5. Unverified

- **AlienBoss** (port `AlienBoss.java`, xp 250, HP/ATK unverified): no original `AlienBoss.java` exists in the 1.7.10 decompile — it is a port-side composite with no original counterpart, so there is nothing to reconcile against. Left untouched.
- **Crab spawn-time scale randomization**: orig Crab.java:78-93 rolls t ∈ {0.25, 0.5, 1.0} at spawn; the port currently leaves the synched default 0.25 and only reads scale from NBT. The *stat formulas* now follow scale correctly; the spawn roll itself is spawn logic owned by another phase and flagged here rather than changed.
- **Boyfriend/Girlfriend** ("BandP" was investigated for them): they are NOT the BandP consumers — orig Boyfriend/Girlfriend hardcode HP 80 (port matches) and the orig `BandP.java` entity consumes `BandP_stats`. No stats change made beyond the BandP entity itself; their armor-from-gear clamp (ENT-A-053) is behavior outside this phase.

---

## 6. Files changed

- `src/main/java/danger/orespawn/MobStats.java` (rewritten, 59 constants + Javadoc)
- `src/main/java/danger/orespawn/OreSpawnMod.java` (attribute-cap raise)
- `src/main/resources/META-INF/accesstransformer.cfg` (RangedAttribute.maxValue AT + comment fix)
- Entities (`src/main/java/danger/orespawn/entity/`): EntityBee, EntityMantis, EntityHerculesBeetle, Mothra, EntityBrutalfly, Nastysaurus, Pointysaurus, Alosaurus, SpiderRobot, AntRobot, GiantRobot, Jeffery, Hammerhead, EntityMolenoid, TRex, BandP, EntityCaterKiller, Cryolophosaurus, EntityRat, Urchin, EntityKyuubi, EntityGammaMetroid, Basilisk, EntityEmperorScorpion, EntityTrooperBug, EntitySpitBug, Alien, WaterDragon, SeaMonster, SeaViper, Robot2, Robot3, Robot4, Robot5, EntityRotator, EntityVortex, DungeonBeast, EntityTriffid, EntityLurkingTerror, EntityWormSmall, EntityWormMedium, EntityWormLarge, EnderKnight, EnderReaper, Irukandji, AttackSquid, CaveFisher, CloudShark, CreepingHorror, Godzilla, Kraken, EntityLeafMonster, PitchBlack, EntityScorpion, Skate, EntityTerribleTerror, TheKing, TheQueen, EntityLeon, Leonopteryx, Crab, Dragon, ThePrincess, ThePrince (comment only), ThePrinceTeen, ThePrinceAdult, RubyBird, VelocityRaptor, EntityHydrolisc

Build status: `.\gradlew.bat compileJava` → **BUILD SUCCESSFUL** (only pre-existing deprecation warnings).
