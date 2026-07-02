# Phase D — slice D3: ranged attacks + Prince-family flight

Findings closed: ENT-S-058, ENT-A-055, ENT-D-049, ENT-A-019 (+ENT-A-018),
ENT-A-062, ENT-D-044, BOSS-019/021/022/023 (baby Prince), BOSS-028 (teen),
BOSS-034 (adult), BOSS-039/040/041 (princess). New findings (all closed in-slice):
BUG-032 (missing aggregate sounds), BOSS-045/BOSS-046 (invented teen/adult
interactions). BOSS-029 audit note corrected + diamond regression restored.
PN-002 closed. MOD-010 archived (invented bro-mode friendly-fire gate).

## D3-0 — BUG-032: 39 missing aggregate sound events

The original's sounds.json defines aggregate keys that pick one of several
files (e.g. `mothrawings` → mothrawings1/2/3). The port's sounds.json only had
the individual numbered entries, so every code reference to an aggregate name
resolved to nothing and played silence. All 39 aggregates added (`b_fight`,
`b_hurt`, `b_taunt`, `bb_happy`, `o_*` Girlfriend set, `robot_living`,
`mothrawings`, etc.), keys lowercase to satisfy modern ResourceLocation rules.

## D3a — UltimateArrow (ENT-S-058, audit corrected)

The audit claimed ignite/knockback/trail; orig UltimateArrow.java has none of
those. What it actually has, now ported:

| Port site | Original | Content |
|---|---|---|
| `canHitEntity` | orig UltimateArrow.java:37-49 | passthrough: never hit an Elevator; never hit a Cephadrome/Dragon/horse that is being ridden |
| `onHitEntity` | orig UltimateArrow.java:51-79 | with UltimateSwordPvp OFF, hitting a player / Girlfriend / Boyfriend / tamed pet HEALS 1.0 (arrow-hit sound, pitch 1.2/(0.2r+0.9)) and discards the arrow; otherwise normal arrow damage |

## D3b — Boyfriend / Girlfriend weapon systems (ENT-A-055, ENT-D-049)

Both originals are `IRangedAttackMob`s with `EntityAIArrowAttack(1.25, 20, 10)`
plus a hand-rolled armed-melee block in their AI tick. Port: `RangedAttackMob`
+ `RangedAttackGoal`, melee in `customServerAiStep`.

| Port site | Original | Content |
|---|---|---|
| `Boyfriend.performRangedAttack` | orig Boyfriend.java:214-243 | Ultimate Bow in hand → UltimateArrow 2.0f velocity, 1-in-4 crit, bow sound; else Shoes id 6, 1.8f/4.0f spread |
| `Boyfriend.customServerAiStep` melee | orig Boyfriend.java:245-292 | 25t cooldown; reach 2×bb-width (10 blocks holding Big Bertha); `b_fight` per swing; `b_taunt` at 4-7 blocks (1-in-40); 1-in-100 revenge forgiveness |
| `Girlfriend.performRangedAttack` | orig Girlfriend.java:216-245 | same, Shoes id = 2 + nextInt(4) |
| `Girlfriend.customServerAiStep` melee | orig Girlfriend.java:247-296 | same with `o_fight`/`o_taunt`; 1-in-200 target clear |

Removed invented content: the port's `Boyfriend.wantsToAttack` override used the
`BOYFRIEND_BRO_MODE` config as a pet friendly-fire gate. The original's
`bro_mode` (orig OreSpawnMain.java:1481) only gates his VOICE (silence rolls on
ambient/hurt/death lines, `bb_happy` variant — orig Boyfriend.java:772-825).
Config key kept (original, ENT-A-058 voice scope); the gate archived as MOD-010.

## D3c — AttackSquid watercanon (ENT-A-019, ENT-A-018)

| Port site | Original | Content |
|---|---|---|
| `watercanon` | orig AttackSquid.java:206-238 | 1-in-5 roll per AI tick when target beyond melee; InkSack 1-in-3 else WaterBall; velocity deltas ÷ dist × 1.4f, spread 5.0f; muzzle +1.1 fwd (yHeadRot) / −0.3 y quirk preserved (body yRot mix); `squidhiss` |
| melee roll | orig AttackSquid.java:196-204 | `nextInt(4)==0 || nextInt(5)==1` (40%) — port previously used a single 1-in-4 |

## D3d — Brutalfly barrage (ENT-A-062)

| Port site | Original | Content |
|---|---|---|
| `attackWithSomething` | orig Brutalfly.java:214-252 | Easy → vanilla SmallFireball; Normal → 50-50; Hard → BetterFireball; +1.0 self-heal per shot; distinct launch sounds |
| shoot odds | orig Brutalfly.java:188-212 | 1-in-3 per AI tick (1-in-2 on Hard); players engaged at range only (line of sight required) — port's invented melee-on-player removed; mobs melee <25 blocks else ranged |

## D3e — GiantRobot LaserBall barrage (ENT-D-044)

| Port site | Original | Content |
|---|---|---|
| aim gate | orig GiantRobot.java:256-263 | fire only when head bearing within 0.5 rad of target; the melee check is NESTED inside the gate (original structure kept) |
| `fireLaserBall` | orig GiantRobot.java:265-297 | muzzle +2.6 fwd / +3.1 y; distSq > 100 → `setSpecial()`, reload 25, launch pitch 0.6; near → plain ball, reload 10, pitch 1.0; FIREWORK_ROCKET_LAUNCH |

## D3f — Prince family flight + canon trios (PN-002 closed)

All four royals get their original flight brains back; activity ≠ ground maps
to `noPhysics` exactly as in 1.7.10 (BUG-010's interim disable lifted — the
original really does ghost through terrain while flying; MOD-003 stays the 2.0
candidate for collision-aware flight).

### Baby Prince (BOSS-019/021/022/023)

| Port site | Original | Content |
|---|---|---|
| `doMovement` | orig ThePrince.java:585-725 | activity cycle 1/100 (1/20 → fly); owner-flying speedups ×1.75 / ×3.5; flee airborne when hurt <25% HP; flight-target rerolls (owner-anchored); signum steering 0.5/0.7 prods, yaw/3 |
| `firecanon`/`firecanonl`/`firecanoni` | orig ThePrince.java:782-853 | BetterFireball / ThunderBolt / IceBall; muzzle xz 3.0 / y 1.0; 0.5 rad bearing gate; 5-12 block band; lit (`DATA_FIRE`) + dry gates |
| `tick`/`aiStep` | orig ThePrince.java:420-505 | `noPhysics = activity==2`; 0.6 y-damping; water buoyancy +0.07 |
| `customServerAiStep` | orig ThePrince.java:507-583 | revenge forgiveness via `setLastHurtByMob(null)`; passive heal; auto-tame ≤10 blocks; growth kill>25 && fed>10 && day>10 (okToGrow gate DROPPED — BOSS-021); day counting |
| `mobInteract` | orig ThePrince.java:190-307 | ice block extinguishes / flint relights (chat messages); diamond block tames unconditionally |
| targeting | orig ThePrince.java:727-780 | original prey list; PlayNicely + Peaceful gates |

### Teen (BOSS-028, BOSS-029 corrected, BOSS-045 new)

| Port site | Original | Content |
|---|---|---|
| `flyWithoutRider` | orig ThePrinceTeen.java:677-834 | damping ternary (unreachable 0.61 arm kept verbatim); 1-in-7 combat roll: bite ≤8 blocks + 5-19t fly-away, else `shoot_somethingAt` <20 blocks; owner-anchored targets 5-18 (0-5 if owner flying, 16-25 wild) with line-of-sight; terrain lift 0.05/block×0.05; signum steering + direct `move()` |
| `shootSomethingAt` | orig ThePrinceTeen.java:947-1019 | canon trio, 0.5 rad gate |
| `alwaysDo` | orig ThePrinceTeen.java:435-461 | 2 HP regen 1/250; 1/250 forgiveness; owner creative-flight follow; 1/50 settle (1/15 keeps flying) |
| flight bypass | orig ThePrinceTeen.java:849-857 | while flying, vanilla goals/travel skipped — mapped to an `aiStep` override (`Mob.serverAiStep` is final in 1.21.1); clients keep vanilla lerp |
| `hurt` | orig ThePrinceTeen.java:343-393 | cactus/fire/lava/inWall immune; fireballs pop; teen/Spyro immune; sit-break + take-flight; hurt_timer 20; tamed-vs-player no retaliation |
| `mobInteract` | orig ThePrinceTeen.java:1127-1273 | diamond block steal-tames + instant adult; empty-hand mount; beef full heal / food ×10; ice/flint toggles; **DIAMOND teen→baby regression** (audit + Phase C note said none existed — orig :1230-1250 disproves; `ThePrince.setOkToGrow()` added); sit toggle grounds |
| targeting | orig ThePrinceTeen.java:496-555 | 25/20/25 box; prey incl. Mothra/Kraken/untamed Leon/WaterDragon/GammaMetroid; royalty exempt |

BOSS-045: the port's invented CAKE growth shortcut removed (duplicated the
diamond block; orig has no cake branch).

### Adult (BOSS-034, BOSS-046 new)

Same brain, adult numbers (orig ThePrinceAdult.java:389-441, 657-814,
1109-1249, 1329-1363): 1-in-6 combat roll, 10-block bite, volley to ~24 blocks
(muzzle xz 6.0 / y 3.5), target spreads 8-23 / 0-11 / 20-34, 5 HP regen, wing
sound every 30t (teen 20t), owner >30 blocks launches flight, inWall = no
damage but take-flight, all interactions distSq <36 + owner-only, **DIAMOND
adult→teen regression restored**. BOSS-046: invented cake shortcut + gold-ingot
regression removed (both duplicated original diamond-item features).

### Princess (BOSS-039/040/041; BOSS-038 clarified)

| Port site | Original | Content |
|---|---|---|
| `doMovement` + canon trio | orig ThePrincess.java:672-812, 863-934 | identical brain/scale to the baby Prince |
| power system | orig ThePrincess.java:518-628 | attack_level +1/tick (+4 in combat, 0 while extinguished); `DATA_POWER` synced every 10 steps; client firework-spark aura >400; discharge >500 → in combat `firePurplePower` (3 orbs, type 1-3, 3× her motion), else `bloom` |
| `bloom` | orig ThePrincess.java:550-626 | 5 column probes under mobGriefing: flowers (vanilla + 6 OreSpawn kinds) on grass; dirt→grass; stone→dirt cover; sand→cactus/dirt; lava→water; 2 Butterfly/Cockateil hatches (orig "Bird" = Cockateil, orig OreSpawnMain.java:3831) |
| melee | orig ThePrincess.java:377-379 | 9.0 via `doHurtTarget` + kill counting. BOSS-038 clarification: the attribute IS 10 (orig :102) — audit and Phase B were both half right; attribute kept at 10, melee deals 9 |
| `mobInteract` | orig ThePrincess.java:198-294 | food heal nutrition ×10 (BOSS-039 — invented fedCount++ dropped); ice/flint toggles with Princess messages; diamond block steal-tames |
| `hurt` | orig ThePrincess.java:296-333 | inWall/cactus no damage; any real hit breaks sit + takes flight |

## Mapping deltas (documented in class Javadoc, not player-visible)

- Teen/adult flight bypass is an `aiStep` override (orig replaced `onLivingUpdate`
  wholesale; `Mob.serverAiStep` is final in 1.21.1). Clients run vanilla lerp,
  matching the original's hand-rolled client lerp.
- 1.7.10 raw `setBlock` terraforming → `setBlockAndUpdate`; still + flowing lava
  (two 1.7.10 block IDs) → the single modern lava block, both → water.
- Girlfriend/Boyfriend arrows are real `UltimateArrow` entities using the modern
  `AbstractArrow` pipeline (pickup disallowed, as orig's despawn-on-ground).

## Ledger / build

- 410 terminal (390 FIXED + 20 VERIFIED-CORRECT) / 195 open, total 605
  (602 + BOSS-045 + BOSS-046 + BUG-032). `tools/ledger_reconcile.py` green.
- `.\gradlew.bat build` green (see FIX_LOG entry).
