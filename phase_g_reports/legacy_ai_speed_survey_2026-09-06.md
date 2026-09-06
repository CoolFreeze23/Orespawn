# Legacy-AI movement-speed survey — every OreSpawn species under the modern mover (ENT-S-150 lane, 2026-09-06)

**Lane:** read-only survey, owner ruling 2026-09-06 item 23 (ENT-S-150 (ii) first; ENT-S-145's value after it). No edits anywhere but this file; no gradle; no state-changing git; every process under `timeout` and reaped (the stray check is §5).

**Question ruled:** 1.7.10's legacy movement loop (`EntityCreature.updateEntityActionState`, the mobs whose `isAIEnabled()` stayed false) fed the movement-speed attribute into a clamped forward input at a fixed 0.1f AI speed, while 1.21.1's mover scales by the attribute itself — so the Ender pair's +6.2 attacking boost is one case of a class. Survey the class before the value is ruled.

**Answer in one paragraph.** Verified against both jars (§1, javap offsets): the legacy loop's ground pace is *linear* in the attribute and capped — `v = 4.317·A blocks/s`, saturating at `4.405 blocks/s` once `0.98·A ≥ 1` (`A ≥ 1.0204`); the 1.7.10 *task* AI (the navigator/move-helper path every other OreSpawn mob used) and the 1.21.1 mover are the **same law**, *quadratic* below the clamp — `v = 43.17·s²` with `s = goalSpeedModifier × attribute` (because both `EntityLiving.setAIMoveSpeed` (1.7.10) and `Mob.setSpeed` (1.21.1) write the speed into the forward input as well as the friction scale). Consequently: (1) the 100-odd task-AI species carry across the port **1:1** wherever the port kept the attribute and the goal modifiers — and it did, number for number, for all but three (SpiderDriver, ThePrinceTeen, the port-only BabyDragon); (2) the **legacy class with attribute-driven movement has exactly two members, the Ender Knight and the Ender Reaper** (Island/IslandToo and the `EntityLiving` parts are legacy by class but never move by the attribute; the robots flip to legacy only while ridden, on a custom mover); (3) for those two the port's un-boosted pace already equals 1.7.10's *boosted* pace (Knight 4.42 vs 4.41 b/s) and is 3.2× / 3.7× 1.7.10's *idle* walk; (4) the ENT-S-145/150 candidate values "ADD_VALUE ≈ 0.67 / MULTIPLY_BASE ≈ 2.1 on the 0.32 base" rest on a linear reading of the modern mover and would give ≈42 blocks/s — the corrected mapping is in §3; (5) a second class the survey exposes: every default-travel swimmer moves at **half** its 1.7.10 pace in water (the engine's AI-mob water factor 0.04 became an attribute-free 0.02).

Sections: §1 engine facts (both sides, offsets); §2 the per-species table (115 rows) and its summary; §3 the mapping options for the owner; §4 findings to file (drafts in Appendix B, ENT-S-155 upward); §5 the stray-process check; Appendix A the offset quotes.

---

## 1. Engine facts (law-11 checked against the jars)

Jars: Mojang `minecraft-1.7.10-client.jar` (obfuscated; every name below was verified by shape, not by the hint list — the shapes are in Appendix A) and `neoforge-21.1.223.jar`. Ground numbers assume normal blocks (slipperiness / friction 0.6, so the per-tick horizontal velocity multiplier is `f = 0.6 × 0.91 = 0.546` and the acceleration factor `0.16277136 / f³ = 0.21600002 / 0.6³ = 1.000` on both sides).

### 1.1 The 1.7.10 legacy loop (a mob with `isAIEnabled() == false`)

- `sw.bk()` = `EntityLiving.isAIEnabled`: `0: iconst_0; 1: ireturn` — false unless overridden. `yg` (= `EntityMob`, `extends td`) has no override; `ya` (= `EntityEnderman`, the source of the +6.2 modifier — its static init `15: "Attacking speed boost"`, `17: ldc2_w 6.199999809265137d`) has none either; `td` (= `EntityCreature`) has none.
- `sv.e()` = `EntityLivingBase.onLivingUpdate`: `332: bk()`; `335: ifeq 367` → `345: "newAi"`, `351: bn()` (updateAITasks) — else `374: "oldAi"`, `380: bq()` (updateEntityActionState), `395-398: rotationYawHead = rotationYaw`. Then, for BOTH branches, `515-521: moveStrafing (bd) *= 0.98f`, `526-532: moveForward (be) *= 0.98f`, `548-555: e(FF)(bd, be)` = moveEntityWithHeading.
- `td.bq()` = `EntityCreature.updateEntityActionState`: `58: 16.0f` (the attack-path range); `62-101:` `bR()` findPlayerToAttack + `ahb.a(...)` getPathEntityToEntity; `298-308:` wander only while `entityAge (aU) < 100` (`bQ()` = updateWanderPath); `342-370:` no path (or a 1-in-100 tick) → `sw.bq()` (super) and `path = null`; **`552-566: getstatic yj.d` (movementSpeed) → `a(th)` getEntityAttribute → `ti.e()` getAttributeValue → `d2f` → `putfield be:F` — `moveForward = attribute`**; `569-601:` the yaw step clamped ±30°; from `604:` while `hasAttacked`, the same vector is rotated into strafe/forward (length preserved).
- `sw.bq()` = `EntityLiving.updateEntityActionState`: `1: sv.bq()` (which is only `aU++`, entityAge), `4-11: bd = 0, be = 0` — the input is zero whenever there is no path node.
- `sv.bl()` = `EntityLivingBase.getAIMoveSpeed`: `1: bk()`; `4: ifeq 12`; `7-8: getfield bp:F` (landMovementFactor) `11: freturn`; **`12: ldc 0.1f; 14: freturn`** — the fixed 0.1f for a non-AI mob.
- `sv.e(FF)` = `moveEntityWithHeading`, ground branch: `314-316: f2 = 0.91f`; `317-363:` on ground `f2 = block(x, floor(bb.minY) − 1, z).slipperiness (aji.K) × 0.91f`; `364-372: f3 = 0.16277136f / f2³`; **`374-388:` on ground `f4 = bl() × f3`**, else `393-397: f4 = aQ` (jumpMovementFactor, `0.02f` from the constructor `43-45`); `399-404: a(FFF)(strafe, forward, f4)`; `698-707: motionY −= 0.08`; `737-746: motionY *= 0.98`; **`749-768: motionX *= f2, motionZ *= f2`** (after the move).
- `sa.a(FFF)` = `Entity.moveFlying`: `0-7: n = strafe² + forward²`; `9-17:` return if `< 1e-4`; `18-23: n = sqrt(n)`; **`25-33: if (n < 1.0f) n = 1.0f`**; `35-39: n = f4 / n`; `41-50: strafe *= n, forward *= n`; `81-116: motionX/Z +=` the yaw-rotated pair. So the per-tick acceleration magnitude is `f4 × min(1, |input|)`: an input shorter than 1 is scaled as it is, a longer one is normalised.

**Legacy formula.** Per tick, while following a path node: input length `|input| = 0.98·A` (the attribute written at 552-566, damped once at 526-532); acceleration `a = 0.1 × 1.0 × min(1, 0.98·A)`; then the move; then `v ← v × 0.546`. Steady-state displacement per tick `d = a / (1 − 0.546) = 2.2026·a`, i.e.

`v_legacy(A) = 20 · 0.1 · min(1, 0.98·A) / 0.454 = 4.317·A blocks/s for A ≤ 1.0204, else 4.405 blocks/s` (the cap).

The Ender Knight: `A = 0.32 → 1.381 b/s` idle; with the +6.2 modifier `A = 6.52 → 0.98·A = 6.39 > 1 → 4.405 b/s` — the saturated "≈3.1× sprint" (3.19× for the Knight, 2.76× for the Reaper whose idle is `0.37 → 1.597`). Any `A ≥ 1.0204` gives the same 4.405; the cap is species-independent. (Check on the same law: a player's walk is `A = 0.1`, input 1×0.98 → `0.098 → 4.317 b/s`, the known 4.317 m/s.)

### 1.2 The 1.7.10 task AI (every OreSpawn mob overriding `isAIEnabled → true`)

- `tv.c()` = `EntityMoveHelper.onUpdateMoveHelper`: `156-169: speed × (yj.d → ti.e()) → d2f → sw.i(F)`.
- `sw.i(F)` = `EntityLiving.setAIMoveSpeed`: `2: sv.i(F)` (`putfield bp:F`, landMovementFactor) then `7: n(F)` = setMoveForward → `putfield be:F`. **The same number `s = speed × A` becomes both the friction scale (`bl()` returns `bp` for an AI mob) and the forward input.**
- `vv` = `PathNavigate`: `a(ayf, D)` setPath `52: putfield d:D` (the speed); `f()` onUpdateNavigation `74-77: getfield d → tv.a(DDDD)` setMoveTo — every tick while a path exists, so the input is refreshed each tick before the 0.98 damping.
- The goal speeds are the 1.7.10 task constructor arguments (`EntityAIWander(this, 1.0)`, `MyEntityAIWander(this, 0.75f)`, `navigator.tryMoveToEntity(e, 1.25)` …) — §2's columns.

**Task-AI formula.** `|input| = 0.98·s`, `a = s × 1.0 × min(1, 0.98·s)`, friction 0.546 →

`v_task(s) = 44.05 · s · min(1, 0.98·s) = 43.17·s² blocks/s for s ≤ 1.0204, then 44.05·s` (no cap; linear above the clamp).

### 1.3 The 1.21.1 mover (NeoForge 21.1.223)

- `MoveControl.tick`, MOVE_TO branch: `334-349: speedModifier × getAttributeValue(MOVEMENT_SPEED) → Mob.setSpeed(F)` (STRAFE branch `14-23`, `163-185` setSpeed / setZza / setXxa).
- `Mob.setSpeed(F)`: `2: LivingEntity.setSpeed(F)` (`putfield speed`) and **`7: setZza(F)`** — the same double role as 1.7.10's `sw.i(F)`.
- `PathNavigation.tick`: `198-201: MoveControl.setWantedPosition(x, y, z, speedModifier)` each tick; `moveTo(Path, D)`: `54: putfield speedModifier`. `MeleeAttackGoal`: `350-353: navigation.moveTo(target, speedModifier)`; `RandomStrollGoal`: `20-23: moveTo(x, y, z, speedModifier)`.
- `LivingEntity.aiStep`: `265-287: isEffectiveAi → serverAiStep()` (goals, navigation, move control); then `607-614: xxa *= 0.98f`, `619-626: zza *= 0.98f`; `729: travel(Vec3(xxa, yya, zza))`.
- `LivingEntity.travel`, ground: `1077-1105: f7 = getBlockState(getBlockPosBelowThatAffectsMyMovement()).getFriction(...)` (0.6); `1108-1126: f8 = onGround ? f7 × 0.91f : 0.91f`; **`1130-1132: handleRelativeFrictionAndCalculateMovement(travelVector, f7)`** — the *block* friction is passed, not `f8`; `1276-1314: setDeltaMovement(x × f8, y × 0.98, z × f8)` after the move.
- `handleRelativeFrictionAndCalculateMovement(Vec3, F)`: `3: getFrictionInfluencedSpeed(F)`; `7: moveRelative(F, Vec3)`; `30: move(SELF, delta)`; `34: return delta`.
- `getFrictionInfluencedSpeed(F)`: `0-3: onGround()`; **`7-20: getSpeed() × (0.21600002f / f³)`** (= `speed × 1.0` for `f = 0.6`); else `24-25: getFlyingSpeed()` (`21: 0.02f` unless player-controlled).
- `Entity.getInputVector(Vec3, F, F)`: `1-16:` `lengthSqr < 1e-7 → ZERO`; **`17-31: lengthSqr > 1.0 ? normalize() : as-is`**; `31-33: scale(speed)` — the same "clamp above 1, keep below 1" as `moveFlying`.

**Modern formula.** `s = speedModifier × A`; `zza = s` (Mob.setSpeed), damped to `0.98·s`; `a = s × min(1, 0.98·s)`; `v ← v × 0.546` after the move →

`v_modern(s) = 43.17·s² blocks/s for s ≤ 1.0204, then 44.05·s` — **identical to 1.7.10's task-AI law**, so a task-AI species with the same attribute and the same goal modifiers moves at the same pace in the port. It is *not* the legacy law: at `A = 0.32` the modern mover gives `4.42 b/s` where the legacy loop gave `1.38 b/s` — the "already at the boosted pace" in ENT-S-150 (4.421 vs 4.405; a coincidence of `0.32² × 43.17 ≈ 0.1 × 44.05`).

### 1.4 Water and air — the branches the ground law does not cover

- **Water, 1.7.10** (`sv.e(FF)` `36-47`): `a(FFF)(strafe, forward, bk() ? 0.04f : 0.02f)`, then `73-97: motion × 0.8`, `109: motionY −= 0.02`. An AI mob accelerates at `0.04 × min(1, 0.98·s)`, a legacy mob at `0.02 × min(1, 0.98·A)`; friction 0.8 → `d = 5a` → **`v_water,1.7.10(s) = 4.0 × min(1, 0.98·s) b/s`** for a task-AI mob (2.0 for a legacy one).
- **Water, 1.21.1** (`LivingEntity.travel` `162-167`): `f5 = getWaterSlowDown()` (0.8f), **`f6 = 0.02f`**; `173-176: WATER_MOVEMENT_EFFICIENCY` (× 0.5 off the ground, `183-194`); `199-230:` only if that attribute is > 0: `f5 += (0.546 − f5)·eff`, `f6 += (getSpeed() − f6)·eff`; `251-258: × NeoForgeMod.SWIM_SPEED` (1.0); `265: moveRelative(f6, travelVector)`; `327-336: × (f5, 0.8, f5)`. With the default efficiency 0 the movement-speed attribute is **not consulted** in water: **`v_water,modern(s) = 2.0 × min(1, 0.98·s) b/s`** — half of 1.7.10 for every mob that swims on the default `travel`. This is a class of its own (§2.2, §4 draft ENT-S-158).
- **Custom swimmers in the port** (SeaViper, WaterDragon): `SmoothSwimmingMoveControl.tick` `211-226: f10 = speedModifier × MOVEMENT_SPEED`; in water `232-249: setSpeed(f10 × inWaterSpeedModifier)` (the port passes `0.02f`), `384-389: zza = cos(xRot) × f10`, `396-402: yya = −sin(xRot) × f10`; their `travel` (SeaViper.java:172-181, WaterDragon.java:191-200) does `moveRelative(getSpeed(), vec)`, move, `× 0.9`. So `a = 0.02·s × min(1, 0.98·s)`, friction 0.9 → `d = 10a` → `v = 4.0 · s · min(1, 0.98·s)`: SeaViper wander (`s = 0.75`) 2.20 b/s vs 1.7.10's 2.94; its 1.5 chase (`s = 1.125`) 4.5 vs 4.0; WaterDragon wander (`s = 0.55`) 1.19 vs 2.16, chase 1.2 (`s = 0.66`) 1.71 vs 2.59, owner-follow 2.0 (`s = 1.1`) 4.4 vs 4.0.
- **Air** (not on ground): 1.7.10 `f4 = jumpMovementFactor 0.02` (`393-397`), 1.21.1 `getFlyingSpeed() = 0.02` (`getFrictionInfluencedSpeed 24-25`); friction 0.91 both → `v_air(s) = 4.44 × min(1, 0.98·s)` on both sides — identical, and moot for the OreSpawn flyers, which write `motionX/Y/Z` (port `setDeltaMovement`) directly in their own loops on both sides (the `F` rows of §2); the attribute never enters their flight. `FlyingMoveControl.tick` (`152-207`: `onGround ? MOVEMENT_SPEED : FLYING_SPEED`) is not used by any OreSpawn class.

### 1.5 Reference numbers (ground, blocks/s)

| A (attribute) | legacy loop | task AI 1.7.10 = modern, sm 1.0 | modern / legacy |
|---|---|---|---|
| 0.10 (player walk) | 0.432 | 0.432 | 1.00 (the fixed point) |
| 0.23 (vanilla zombie, `Zombie.createAttributes 12-15`) | 0.993 | 2.284 | 2.30 |
| 0.30 (vanilla enderman, `EnderMan.createAttributes 12-15`) | 1.295 | 3.885 | 3.00 |
| 0.32 (Ender Knight) | 1.381 | 4.421 | 3.20 |
| 0.37 (Ender Reaper) | 1.597 | 5.910 | 3.70 |
| 0.45 (vanilla enderman angry: 0.30 + 0.15 ADD_VALUE, `EnderMan` static init 13-25) | 1.943 | 8.742 | 4.50 |
| 0.62 (King / Queen; flyers) | 2.677 | 16.6 | 6.20 |
| 0.80 (1.7.10 EntitySpider `yn.aD 20-26`) | 3.454 | 27.6 | 8.00 |
| ≥ 1.0204 | 4.405 (cap) | 44.05·s (no cap) | — |

Player references on the modern law: walk 4.317 b/s, sprint 5.612 b/s (`0.13`).

### 1.6 Corrections to the ENT-S-145 / ENT-S-150 record text (for the orchestrator)

1. "the modern mover ... scales by the unclamped attribute" — true only above `s = 1.0204`; below it the input itself is `0.98·s` (Mob.setSpeed → setZza), so the pace is quadratic in `s`. The record's "≈6.52 blocks/tick toward ≈14 blocks/tick" for a transcribed 6.2 is right (`s = 6.52` is above the clamp: 14.36 b/tick, 287 b/s), but the two candidate values derived from a linear reading are not: **ADD_VALUE 0.67 on 0.32 (→ 0.99) gives 42.3 b/s and MULTIPLY_BASE 2.1 on 0.32 (→ 0.992) gives 42.5 b/s — 9.6× the 1.7.10 sprint**, not ≈3.1×. The corrected mapping is §3 (b).
2. "HEAD's un-boosted Knight already moves at ≈4.4 blocks/s, 1.7.10's boosted pace" — confirmed: 4.421 vs 4.405 b/s. The Reaper is 5.910 vs 4.405 (1.34×).
3. "1.7.10's legacy mover saturated the number ... to a ≈3.1× sprint (≈1.4 → ≈4.4 blocks/s)" — confirmed for the Knight (1.381 → 4.405, 3.19×); the Reaper is 1.597 → 4.405 (2.76×). The saturated pace is the cap, not a multiple of the base.
4. "every legacy-AI species" — the class with attribute-driven legacy movement is the two Ender mobs (§2.1). Vanilla 1.21.1's own EnderMan re-tune (0.15 ADD_VALUE on 0.30) is a 2.25× burst (3.89 → 8.74 b/s) under the quadratic law, not a reproduction of 1.7.10's 1.30 → 4.41 (3.4×).

---

## 2. Per-species table

Columns: `A` = movement-speed attribute base; `sm` = the goal / navigator speed modifier (wander / chase — the chase column is the species' own target-chase `moveTo` or attack-goal speed; follow / tempt / panic modifiers are in the notes); `v` by §1's laws (ground, normal blocks); `ratio = v_port / v_1.7.10`. Modes: **L** legacy loop (attribute → clamped input, fixed 0.1f), **T** task AI (navigator), **C** custom mover / no attribute-driven movement, **X** port-only species (no 1.7.10 number). Flags: **F** flyer by direct motion writes, **S** swims (the §1.4 water class applies in water), **R** ridden custom mover, **P** part / static, **L** legacy-loop member, **V** vanilla-inherited. Chase rows for the two L species: 1.7.10 = the +6.2 boosted (saturated) pace, port = `MeleeAttackGoal 1.0` with no modifier (ENT-S-145 HELD). Rows marked "row on land" / "row at scale 1" are the species whose attribute the game rewrites per tick (Crab, SeaMonster, SeaViper, WaterDragon, PitchBlack); their other states follow the same law with the other number. Generated by `scratchpad/survey150/gen_table.py` from the greps of §A.3.

| # | Port species | 1.7.10 class / mode | A_1.7.10 | sm wander / chase (1.7.10) | v_1.7.10 wander / chase (b/s) | A_port | sm wander / chase (port) | v_port wander / chase (b/s) | ratio wander / chase | flags | notes |
|---|---|---|---|---|---|---|---|---|---|---|---|
| 1 | Alien | Alien / T | 0.65 | 1.00 / 1.20 | 18.24 / 26.27 | 0.65 | 1.00 / 1.20 | 18.24 / 26.27 | 1.000 / 1.000 |  |  |
| 2 | AlienBoss | (none) / X | - | - | - / - | 0.55 | 1.00 / 1.20 | 13.06 / 18.81 | - / - | X | port-only subclass of Alien (its own header: 1.7.10 shipped no Alien Boss) |
| 3 | Alosaurus | Alosaurus / T | 0.35 | 1.00 / 1.25 | 5.29 / 8.26 | 0.35 | 1.00 / 1.25 | 5.29 / 8.26 | 1.000 / 1.000 |  | Presets.alosaurus() 1.25 |
| 4 | AntRobot | AntRobot / C | 0.30 | - | - / - | 0.30 | - / - | - / - | - / - | R | isAIEnabled = riddenByEntity == null; no move tasks; ridden mover writes motion (orig 16 sites; port travel override) |
| 5 | AttackSquid | AttackSquid / T | 0.25 | 1.00 / 1.20 | 2.70 / 3.89 | 0.25 | 1.00 / 1.20 | 2.70 / 3.89 | 1.000 / 1.000 | S | chase 1.2 / 1.33 to cell / buddy 1.0 both sides |
| 6 | BandP | BandP / T | 0.32 | 0.50 / 1.25 | 1.11 / 6.91 | 0.32 | 0.50 / 1.25 | 1.11 / 6.91 | 1.000 / 1.000 |  |  |
| 7 | Baryonyx | Baryonyx / T | 0.25 | 1.00 / 1.00 | 2.70 / 2.70 | 0.25 | 1.00 / 1.00 | 2.70 / 2.70 | 1.000 / 1.000 |  | panic 1.5 both; cell 1.0 |
| 8 | Basilisk | Basilisk / T | 0.40 | 1.00 / 1.25 | 6.91 / 10.79 | 0.40 | 1.00 / 1.25 | 6.91 / 10.79 | 1.000 / 1.000 |  |  |
| 9 | Beaver | Beaver / T | 0.20 | 1.00 / 1.00 | 1.73 / 1.73 | 0.20 | 1.00 / 1.00 | 1.73 / 1.73 | 1.000 / 1.000 |  | cell 1.0, buddy 0.5, panic 1.5 both |
| 10 | EntityBee | Bee / C | 0.32 | - | - / - | 0.32 | - / - | - / - | - / - | F | no navigator task either side; flight by direct motion writes |
| 11 | Boyfriend | Boyfriend / T | 0.30 | 0.75 / 1.25 | 2.19 / 6.07 | 0.30 | 0.75 / 1.25 | 2.19 / 6.07 | 1.000 / 1.000 |  | follow 1.4, tempt 1.25, panic 1.5 both |
| 12 | EntityBrutalfly | Brutalfly / C | 0.35 | - | - / - | 0.35 | - / - | - / - | - / - | F | direct motion writes |
| 13 | Camarasaurus | Camarasaurus / T | 0.20 | 1.00 / 1.00 | 1.73 / 1.73 | 0.20 | 1.00 / 1.00 | 1.73 / 1.73 | 1.000 / 1.000 |  | follow 2.0, tempt 1.2, panic 1.5; cell 1.0 |
| 14 | Cassowary | Cassowary / T | 0.25 | 1.00 / - | 2.70 / - | 0.25 | 1.00 / - | 2.70 / - | 1.000 / - |  | panic 1.5 both |
| 15 | EntityCaterKiller | CaterKiller / T | 0.35 | 1.00 / 1.25 | 5.29 / 8.26 | 0.35 | 1.00 / 1.25 | 5.29 / 8.26 | 1.000 / 1.000 |  | Params.caterKiller() 1.25; cell 1.0 |
| 16 | CaveFisher | CaveFisher / T | 0.20 | 1.00 / 1.20 | 1.73 / 2.49 | 0.20 | 1.00 / 1.20 | 1.73 / 2.49 | 1.000 / 1.000 |  | Params.caveFisher() 1.2 |
| 17 | Cephadrome | Cephadrome / T | 0.25 | 1.00 / 1.70 | 2.70 / 7.80 | 0.25 | 1.00 / 1.70 | 2.70 / 7.80 | 1.000 / 1.000 | R | chase 1.2 / 1.7 both; ridden flight is a custom mover (orig 14 motion sites; port travel returns while ridden) |
| 18 | Chipmunk | Chipmunk / T | 0.38 | 1.00 / 1.25 | 6.23 / 9.74 | 0.38 | 1.00 / 1.25 | 6.23 / 9.74 | 1.000 / 1.000 |  | CannonFodder chase 1.25, path 0.65; follow 2.0, tempt 1.2, panic 1.5 |
| 19 | EntityCliffRacer | CliffRacer / C | 0.33 | - | - / - | 0.33 | - / - | - / - | - / - | F | direct motion writes |
| 20 | CloudShark | CloudShark / C | 0.30 | - | - / - | 0.30 | - / - | - / - | - / - | F | direct motion writes |
| 21 | Cockateil / RubyBird | Cockateil / C | 0.33 | - | - / - | 0.33 | - / - | - / - | - / - | F | direct motion writes; no navigator task |
| 22 | Coin | Coin / C | 0.00 | - | - / - | 0.00 | - / - | - / - | - / - | P | speed 0 both sides |
| 23 | Crab | Crab / T | 0.55 | 1.00 / 1.20 | 13.06 / 18.81 | 0.55 | 1.00 / 1.20 | 13.06 / 18.81 | 1.000 / 1.000 | S | attribute = 0.55 (0.95 in water) x crab scale, per tick, both sides; chase 1.2 / 1.0, cell 1.33 |
| 24 | CreepingHorror | CreepingHorror / T | 0.25 | 1.00 / 1.25 | 2.70 / 4.22 | 0.25 | 1.00 / 1.25 | 2.70 / 4.22 | 1.000 / 1.000 |  | panic 1.35 both |
| 25 | EntityCricket | Cricket / T | 0.15 | 1.00 / - | 0.97 / - | 0.15 | 1.00 / - | 0.97 / - | 1.000 / - |  | panic 1.4; hop impulse by direct motion both sides |
| 26 | Cryolophosaurus | Cryolophosaurus / T | 0.25 | 1.00 / 1.25 | 2.70 / 4.22 | 0.25 | 1.00 / 1.25 | 2.70 / 4.22 | 1.000 / 1.000 |  | Presets.cryolophosaurus() 1.25; panic 1.35 |
| 27 | Dragon | Dragon / T | 0.32 | 0.75 / 1.20 | 2.49 / 6.37 | 0.32 | 0.75 / 1.20 | 2.49 / 6.37 | 1.000 / 1.000 | R F | follow 1.1, tempt 1.25; flight/ridden mover custom (orig 20 motion sites; port travel returns while ridden or activity 1) |
| 28 | BabyDragon | Dragon ("Baby Dragon" spawn name) / X | 0.32 | 0.75 / 1.20 | 2.49 / 6.37 | 0.30 | 0.75 / 1.20 | 2.19 / 5.60 | 0.879 / 0.879 | X R F | port-only subclass; 1.7.10's Baby Dragon is the Dragon class (Dragon.java:1354) at the Dragon's 0.32 |
| 29 | EntityDragonfly | Dragonfly / C | 0.33 | - | - / - | 0.33 | - / - | - / - | - / - | F | direct motion writes |
| 30 | DungeonBeast | DungeonBeast / T | 0.29 | 1.00 / 1.20 | 3.63 / 5.23 | 0.29 | 1.00 / 1.20 | 3.63 / 5.23 | 1.000 / 1.000 |  | Params.dungeonBeast() 1.2 |
| 31 | EasterBunny | EasterBunny / T | 0.45 | 1.00 / - | 8.74 / - | 0.45 | 1.00 / - | 8.74 / - | 1.000 / - |  | panic 1.5 both |
| 32 | Elevator | Elevator / C | 1.33 | - | - / - | 1.33 | - / - | - / - | - / - | P | EntityLiving legacy loop zeroes moveForward (sw.bq 4-11); moved by its own velocity fields both sides |
| 33 | EntityEmperorScorpion | EmperorScorpion / T | 0.35 | 1.00 / 1.20 | 5.29 / 7.62 | 0.35 | 1.00 / 1.20 | 5.29 / 7.62 | 1.000 / 1.000 |  | Params.emperorScorpion() 1.2 |
| 34 | EnderKnight | EnderKnight / L | 0.32 | (legacy: input = attribute) | 1.38 / 4.41 | 0.32 | 1.00 / 1.00 | 4.42 / 4.42 | 3.200 / 1.004 | L | LEGACY LOOP: orig +6.2 attacking modifier saturates the chase (see 3.1); port MeleeAttackGoal 1.0 + stroll 1.0, no modifier (ENT-S-145 HELD) |
| 35 | EnderReaper | EnderReaper / L | 0.37 | (legacy: input = attribute) | 1.60 / 4.41 | 0.37 | 1.00 / 1.00 | 5.91 / 5.91 | 3.700 / 1.342 | L | LEGACY LOOP: as the Knight |
| 36 | EntityAnt / Rainbow / Unstable | EntityAnt / T | 0.15 | 1.00 / - | 0.97 / - | 0.15 | 1.00 / - | 0.97 / - | 1.000 / - |  | panic 1.4 both |
| 37 | EntityRedAnt | EntityRedAnt / T | 0.20 | 1.00 / 1.00 | 1.73 / 1.73 | 0.20 | 1.00 / 1.00 | 1.73 / 1.73 | 1.000 / 1.000 |  | AttackOnCollide 1.0 -> MeleeAttackGoal 1.0 |
| 38 | EntityTermite | Termite / T | 0.20 | 1.00 / 1.00 | 1.73 / 1.73 | 0.20 | 1.00 / 1.00 | 1.73 / 1.73 | 1.000 / 1.000 |  | AttackOnCollide 1.0 -> MeleeAttackGoal 1.0; cell 1.0 |
| 39 | EntityButterfly / LunaMoth | EntityButterfly / C | 0.10 | - | - / - | 0.10 | - / - | - / - | - / - | F | direct motion writes |
| 40 | EntityMosquito | EntityMosquito / C | 0.10 | - | - / - | 0.10 | - / - | - / - | - / - | F | direct motion writes |
| 41 | Mothra | Mothra / C | 0.35 | - | - / - | 0.35 | - / - | - / - | - / - | F | direct motion writes |
| 42 | VampireButterfly | (none) / X | - | - | - / - | 0.18 | - / - | - / - | - / - | X F | port-only (MODERNIZATION_NOTES ruling 2026-08-11, phase14ContentEnable); AmbientFlightGoal |
| 43 | Fairy | Fairy / C | 0.10 | - | - / - | 0.10 | - / - | - / - | - / - | F | direct motion writes |
| 44 | Firefly | Firefly / C | 0.10 | - | - / - | 0.10 | - / - | - / - | - / - | F | direct motion writes |
| 45 | Flounder | Flounder / T | 0.25 | 1.00 / 1.00 | 2.70 / 2.70 | 0.25 | 1.00 / 1.00 | 2.70 / 2.70 | 1.000 / 1.000 | S | panic 1.5; cell 1.0 |
| 46 | Frog | Frog / T | 0.10 | 1.00 / 1.25 | 0.43 / 0.67 | 0.10 | 1.00 / 1.25 | 0.43 / 0.67 | 1.000 / 1.000 |  | panic 1.4; hop impulse direct both sides |
| 47 | EntityGammaMetroid | GammaMetroid / T | 0.15 | 1.00 / 1.25 | 0.97 / 1.52 | 0.15 | 1.00 / 1.25 | 0.97 / 1.52 | 1.000 / 1.000 |  | follow 2.0, tempt 1.2; cell 1.0 |
| 48 | Gazelle | Gazelle / T | 0.30 | 1.00 / 1.00 | 3.89 / 3.89 | 0.30 | 1.00 / 1.00 | 3.89 / 3.89 | 1.000 / 1.000 |  | follow 2.0, tempt 1.2, panic 1.5; cell 1.0, buddy 0.5 |
| 49 | Ghost | Ghost / C | 0.10 | - | - / - | 0.10 | - / - | - / - | - / - | F | direct motion writes |
| 50 | GhostSkelly | GhostSkelly / C | 0.10 | - | - / - | 0.10 | - / - | - / - | - / - | F | direct motion writes |
| 51 | GiantRobot / Jeffery | GiantRobot / T | 0.55 | 1.00 / 0.50 | 13.06 / 3.26 | 0.55 | 1.00 / 0.50 | 13.06 / 3.26 | 1.000 / 1.000 |  | chase 0.5 both; village 0.9 |
| 52 | Girlfriend | Girlfriend / T | 0.30 | 0.75 / 1.25 | 2.19 / 6.07 | 0.30 | 0.75 / 1.25 | 2.19 / 6.07 | 1.000 / 1.000 |  | follow 1.4, tempt 1.25, panic 1.5 |
| 53 | Godzilla | Godzilla / T | 0.75 | 1.00 / 1.20 | 24.28 / 34.97 | 0.75 | 1.00 / 1.20 | 24.28 / 34.97 | 1.000 / 1.000 |  | chase 1.0 / 1.2 both; parts by direct writes |
| 54 | GodzillaHead / KingHead / QueenHead | GodzillaHead etc. / C | 1.33 | - | - / - | 1.33 | - / - | - / - | - / - | P | EntityLiving legacy loop, moveForward 0; positioned by the owner both sides |
| 55 | GoldFish | GoldFish / C | 0.22 | - | - / - | 0.22 | - / - | - / - | - / - | F S | direct motion writes |
| 56 | Hammerhead | Hammerhead / T | 0.35 | 1.00 / 1.25 | 5.29 / 8.26 | 0.35 | 1.00 / 1.25 | 5.29 / 8.26 | 1.000 / 1.000 | S |  |
| 57 | EntityHerculesBeetle | HerculesBeetle / T | 0.25 | 1.00 / 1.20 | 2.70 / 3.89 | 0.25 | 1.00 / 1.20 | 2.70 / 3.89 | 1.000 / 1.000 |  | Params.herculesBeetle() 1.2 |
| 58 | EntityHydrolisc | Hydrolisc / T | 0.25 | 1.00 / 1.00 | 2.70 / 2.70 | 0.25 | 1.00 / 1.00 | 2.70 / 2.70 | 1.000 / 1.000 | S | follow 1.2, tempt 1.25, panic 1.5; cell 1.0 |
| 59 | Irukandji | Irukandji / T | 0.15 | 1.00 / 1.20 | 0.97 / 1.40 | 0.15 | 1.00 / 1.20 | 0.97 / 1.40 | 1.000 / 1.000 | S | cell 1.33 |
| 60 | Island / IslandToo | Island / IslandToo / L | 0.70 | (legacy: input = attribute) | 0.00 / 0.00 | 0.00 | - / - | 0.00 / 0.00 | - / - | L P | LEGACY LOOP by class (no isAIEnabled override; EntityAnimal) but onUpdate zeroes motion every tick (Island.java:48-52, IslandToo.java:41-45): never moves either side |
| 61 | Kraken | Kraken / C | 0.37 | - | - / - | 0.37 | - / - | - / - | - / - | S | direct motion writes; no navigator task |
| 62 | EntityKyuubi | Kyuubi / T | 0.25 | 1.00 / 1.25 | 2.70 / 4.22 | 0.25 | 1.00 / 1.25 | 2.70 / 4.22 | 1.000 / 1.000 |  | EntityAIWander 1.0 -> stroll 1.0; panic 1.35 |
| 63 | EntityLeafMonster | LeafMonster / T | 0.25 | - / 1.25 | - / 4.22 | 0.25 | - / 1.25 | - / 4.22 | - / 1.000 |  | no wander either side; panic 1.35 |
| 64 | EntityLeon | Leon / T | 0.25 | 0.75 / 1.20 | 1.52 / 3.89 | 0.25 | 0.75 / 1.20 | 1.52 / 3.89 | 1.000 / 1.000 | R F | follow 1.1, tempt 1.25; ridden flight custom |
| 65 | Lizard | Lizard / T | 0.30 | 1.00 / 1.20 | 3.89 / 5.60 | 0.30 | 1.00 / 1.20 | 3.89 / 5.60 | 1.000 / 1.000 | S | follow 2.0, tempt 1.25; cell 1.33, buddy 1.0 |
| 66 | EntityLurkingTerror | LurkingTerror / C | 0.25 | - | - / - | 0.25 | - / - | - / - | - / - | F | direct motion writes |
| 67 | EntityMantis | Mantis / C | 0.32 | - | - / - | 0.32 | - / - | - / - | - / - | F | direct motion writes; no navigator task |
| 68 | EntityMolenoid | Molenoid / T | 0.35 | 1.00 / 1.25 | 5.29 / 8.26 | 0.35 | 1.00 / 1.25 | 5.29 / 8.26 | 1.000 / 1.000 |  |  |
| 69 | Nastysaurus | Nastysaurus / T | 0.35 | 1.00 / 1.25 | 5.29 / 8.26 | 0.35 | 1.00 / 1.25 | 5.29 / 8.26 | 1.000 / 1.000 |  | Presets.nastysaurus() 1.25 |
| 70 | Ostrich | Ostrich / T | 0.38 | 1.00 / 1.25 | 6.23 / 9.74 | 0.38 | 1.00 / 1.25 | 6.23 / 9.74 | 1.000 / 1.000 | R | follow 2.0, tempt 1.2, panic 1.5; CannonFodder chase 1.25; ridden run custom (orig 16 motion sites) |
| 71 | Peacock | Peacock / T | 0.38 | 1.00 / 1.20 | 6.23 / 8.98 | 0.38 | 1.00 / 1.20 | 6.23 / 8.98 | 1.000 / 1.000 |  | panic 1.5 |
| 72 | PitchBlack | PitchBlack / T | 0.30 | 1.00 / - | 3.89 / - | 0.30 | 1.00 / - | 3.89 / - | 1.000 / - |  | attribute 0.2 + 0.1 x scale, scale in {0.5,1,2,3,4} -> {0.25,0.30,0.40,0.50,0.60}; port SIZE_SPEED identical (ENT-K-051 FIXED); row at scale 1 |
| 73 | Pointysaurus | Pointysaurus / T | 0.35 | 1.00 / 1.25 | 5.29 / 8.26 | 0.35 | 1.00 / 1.25 | 5.29 / 8.26 | 1.000 / 1.000 |  | Presets.pointysaurus() 1.25 |
| 74 | PurplePower | PurplePower / C | 0.25 | - | - / - | 0.25 | - / - | - / - | - / - | F | direct motion writes |
| 75 | EntityRat | Rat / T | 0.25 | 1.00 / 1.25 | 2.70 / 4.22 | 0.25 | 1.00 / 1.25 | 2.70 / 4.22 | 1.000 / 1.000 |  | chase 1.25 / player 1.75 both; panic 1.35 |
| 76 | Robot1 | Robot1 / T | 0.20 | 1.00 / 1.20 | 1.73 / 2.49 | 0.20 | 1.00 / 1.20 | 1.73 / 2.49 | 1.000 / 1.000 |  |  |
| 77 | Robot2 | Robot2 / T | 0.30 | 1.00 / 1.20 | 3.89 / 5.60 | 0.30 | 1.00 / 1.20 | 3.89 / 5.60 | 1.000 / 1.000 |  | chase 1.0 / 1.2 both |
| 78 | Robot3 | Robot3 / T | 0.35 | 1.00 / 0.50 | 5.29 / 1.32 | 0.35 | 1.00 / 0.50 | 5.29 / 1.32 | 1.000 / 1.000 |  |  |
| 79 | Robot4 | Robot4 / T | 0.34 | 1.00 / 0.75 | 4.99 / 2.81 | 0.34 | 1.00 / 0.75 | 4.99 / 2.81 | 1.000 / 1.000 |  | chase 0.75 / 1.2 both |
| 80 | Robot5 | Robot5 / T | 0.30 | 1.00 / 0.50 | 3.89 / 0.97 | 0.30 | 1.00 / 0.50 | 3.89 / 0.97 | 1.000 / 1.000 |  |  |
| 81 | RockBase | RockBase / C | 0.70 | - | - / - | 0.00 | - / - | - / - | - / - | P | EntityLiving legacy loop, moveForward 0 (attribute default 0.7 unused); port 0.0 |
| 82 | EntityRotator | Rotator / C | 0.25 | - | - / - | 0.25 | - / - | - / - | - / - | F | direct motion writes |
| 83 | EntityRubberDucky | RubberDucky / T | 0.22 | 1.00 / 1.20 | 2.09 / 3.01 | 0.22 | 1.00 / 1.20 | 2.09 / 3.01 | 1.000 / 1.000 | S | follow 2.0, tempt 1.25; cell 1.33, buddy 1.0 |
| 84 | EntityScorpion | Scorpion / T | 0.20 | 1.00 / 1.20 | 1.73 / 2.49 | 0.20 | 1.00 / 1.20 | 1.73 / 2.49 | 1.000 / 1.000 |  | Params.scorpion() 1.2 |
| 85 | SeaMonster | SeaMonster / T | 0.25 | 1.00 / 1.20 | 2.70 / 3.89 | 0.25 | 1.00 / 1.20 | 2.70 / 3.89 | 1.000 / 1.000 | S | attribute 0.25 / 0.55 in water per tick both sides; chase 1.0 / 1.2, cell 1.33; row on land |
| 86 | SeaViper | SeaViper / T | 0.25 | 1.00 / 1.50 | 2.70 / 6.07 | 0.25 | 1.00 / 1.50 | 2.70 / 6.07 | 1.000 / 1.000 | S | attribute 0.25 / 0.75 in water per tick both; Presets.seaViper() 1.5; port custom water travel (SmoothSwimmingMoveControl 0.02) - see 1.4; row on land |
| 87 | Skate | Skate / T | 0.25 | 1.00 / 1.20 | 2.70 / 3.89 | 0.25 | 1.00 / 1.20 | 2.70 / 3.89 | 1.000 / 1.000 | S | cell 1.33 |
| 88 | SpiderDriver | SpiderDriver (extends EntitySpider) / T | 0.80 | 0.65 / 0.55 | 11.67 / 8.36 | 0.30 | 0.65 / 0.55 | 1.64 / 1.18 | 0.141 / 0.141 |  | 1.7.10 base is EntitySpider's 0.8 (yn.aD 20-26), never overridden by SpiderDriver.java; port inherits vanilla 1.21.1 Spider.createAttributes 0.3 |
| 89 | SpiderRobot | SpiderRobot / C | 0.35 | - | - / - | 0.35 | - / - | - / - | - / - | R | as AntRobot |
| 90 | EntitySpitBug | SpitBug / T | 0.33 | 1.00 / 1.20 | 4.70 / 6.77 | 0.33 | 1.00 / 1.20 | 4.70 / 6.77 | 1.000 / 1.000 |  | chase 1.2 (orig :251) and Params.spitBug() 0.5 (orig :289) both; village 0.9 |
| 91 | EntitySpyro | Spyro / T | 0.30 | 0.75 / 1.25 | 2.19 / 6.07 | 0.30 | 0.75 / 1.25 | 2.19 / 6.07 | 1.000 / 1.000 | F | follow 1.15, tempt 1.25, panic 1.5; cell 1.0; tamed flight custom |
| 92 | EntityStinkBug | StinkBug / T | 0.15 | 1.00 / - | 0.97 / - | 0.15 | 1.00 / - | 0.97 / - | 1.000 / - |  | panic 1.5 |
| 93 | EntityStinky | Stinky / T | 0.30 | 0.75 / 1.25 | 2.19 / 6.07 | 0.30 | 0.75 / 1.25 | 2.19 / 6.07 | 1.000 / 1.000 | F | as Spyro; cell 1.25 |
| 94 | TRex | TRex / T | 0.38 | 1.00 / 1.25 | 6.23 / 9.74 | 0.38 | 1.00 / 1.25 | 6.23 / 9.74 | 1.000 / 1.000 |  | Presets.trex() 1.25 |
| 95 | EntityTerribleTerror | TerribleTerror / C | 0.10 | - | - / - | 0.10 | - / - | - / - | - / - | F | direct motion writes |
| 96 | TheKing | TheKing / C | 0.62 | - | - / - | 0.62 | - / - | - / - | - / - | F | no navigator task; flight by direct motion writes (port navigation.stop() only) |
| 97 | TheQueen | TheQueen / C | 0.62 | - | - / - | 0.62 | - / - | - / - | - / - | F | as the King |
| 98 | ThePrince | ThePrince / T | 0.32 | 0.75 / - | 2.49 / - | 0.32 | 0.75 / - | 2.49 / - | 1.000 / - | F | follow 1.15, tempt 1.25; flight custom |
| 99 | ThePrinceAdult | ThePrinceAdult / T | 0.36 | 0.75 / 1.20 | 3.15 / 8.06 | 0.36 | 0.75 / 1.20 | 3.15 / 8.06 | 1.000 / 1.000 | R F | follow 1.1, tempt 1.25 |
| 100 | ThePrinceTeen | ThePrinceTeen / T | 0.32 | 0.75 / 1.20 | 2.49 / 6.37 | 0.35 | 0.75 / 1.20 | 2.97 / 7.62 | 1.196 / 1.196 | R F | port constructor writes moveSpeed 0.35f over the registered 0.32 (ThePrinceTeen.java:108, :139; OPT-009 kept the 0.35f 'as value-preserving'); orig :87 (0.32f) / :139 |
| 101 | ThePrincess | ThePrincess / T | 0.32 | 0.75 / - | 2.49 / - | 0.32 | 0.75 / - | 2.49 / - | 1.000 / - | F | follow 1.15, tempt 1.25 |
| 102 | EntityTriffid | Triffid / T | 0.13 | - / 1.00 | - / 0.73 | 0.13 | - / 1.00 | - / 0.73 | - / 1.000 |  | cell 1.0 both; no wander |
| 103 | EntityTrooperBug | TrooperBug / T | 0.40 | 1.00 / 1.20 | 6.91 / 9.95 | 0.40 | 1.00 / 1.20 | 6.91 / 9.95 | 1.000 / 1.000 |  | Params.trooperBug() 1.2; village 0.9; leap impulse direct |
| 104 | EntityTshirt | Tshirt / C | 0.00 | - | - / - | 0.00 | - / - | - / - | - / - | P | speed 0 both |
| 105 | Urchin | Urchin / T | 0.30 | 1.00 / 1.20 | 3.89 / 5.60 | 0.30 | 1.00 / 1.20 | 3.89 / 5.60 | 1.000 / 1.000 |  |  |
| 106 | VelocityRaptor | VelocityRaptor / T | 0.55 | 0.90 / 1.50 | 10.58 / 29.38 | 0.55 | 0.90 / 1.50 | 10.58 / 29.38 | 1.000 / 1.000 |  | follow 1.5, tempt 1.25, panic 1.6; cell 1.0 |
| 107 | EntityVortex | Vortex / C | 0.35 | - | - / - | 0.35 | - / - | - / - | - / - | F | direct motion writes |
| 108 | WaterDragon | WaterDragon / T | 0.25 | 1.00 / 1.20 | 2.70 / 3.89 | 0.25 | 1.00 / 1.20 | 2.70 / 3.89 | 1.000 / 1.000 | S | attribute 0.25 / 0.55 in water per tick both; follow 2.0 (OwnerFollowAnyNavGoal), tempt 1.2; chase 1.0 / 1.2, cell 1.33; port custom water travel; row on land |
| 109 | Whale | Whale / T | 0.35 | 1.00 / 1.00 | 5.29 / 5.29 | 0.35 | 1.00 / 1.00 | 5.29 / 5.29 | 1.000 / 1.000 | S | tempt 1.2, panic 1.5; cell 1.0 |
| 110 | EntityWormLarge | WormLarge / T | 0.20 | 1.00 / 1.00 | 1.73 / 1.73 | 0.20 | 1.00 / 1.00 | 1.73 / 1.73 | 1.000 / 1.000 |  | target cell 1.0; hop impulse direct |
| 111 | EntityWormMedium | WormMedium / C | 0.10 | - | - / - | 0.10 | - / - | - / - | - / - |  | no navigator task either side; hops by direct motion |
| 112 | EntityWormSmall | WormSmall / C | 0.10 | - | - / - | 0.10 | - / - | - / - | - / - |  | as WormMedium |
| 113 | EntityCannonFodder (registered) | EntityCannonFodder / L? | - | - | - / - | 0.25 | - / - | - / - | - / - | L? | see the note in 2.2 |
| 114 | RedCow / CrystalCow / GoldCow / EnchantedAppleCow | RedCow (EntityCow) / T | - | - / - | - / - | - | - / - | - / - | - / - | V | vanilla cow numbers on both sides (1.7.10 EntityCow / 1.21.1 Cow, 0.2 each - UNVERIFIED here, not an OreSpawn number) |
| 115 | AppleCow / GoldenAppleCow | (none) / X | - | - | - / - | - | - / - | - / - | - / - | X V | port-only (the 2026-08-11 ruling), vanilla Cow attributes |

### 2.1 Summary by class

| Class | Rows | Members | Port vs 1.7.10 |
|---|---|---|---|
| **L — legacy loop, attribute-driven** | 2 | EnderKnight, EnderReaper | idle 3.20× / 3.70× too fast (4.42 / 5.91 vs 1.38 / 1.60 b/s); chase 1.00× / 1.34× of the saturated 4.41 b/s; the +6.2 modifier dropped (ENT-S-145, HELD) |
| L by class, never moving | 1 row (2 classes) | Island, IslandToo | motion zeroed every tick on both sides (Island.java:48-52, IslandToo.java:41-45; port `setDeltaMovement(Vec3.ZERO)`), attribute irrelevant |
| L-shaped parts (`EntityLiving`, `sw.bq` zeroes the input) | 3 rows (5 classes) | Elevator, GodzillaHead, KingHead, QueenHead, RockBase | positioned by their owner / velocity fields on both sides; the 1.33 / 0.7 attributes never drive them |
| L while ridden (`isAIEnabled = riddenByEntity == null`) | 2 | AntRobot, SpiderRobot | custom ridden movers on both sides (orig 16 motion sites each; port `travel` / `customServerAiStep`); no move task when unridden |
| **T — task AI** | 75 rows (≈ 85 classes) | everything else that walks | the same quadratic law on both sides; attribute and goal modifiers match number for number except **SpiderDriver** (0.8 → 0.3: 0.141×), **ThePrinceTeen** (0.32 → 0.35 effective: 1.196×), and the port-only **BabyDragon** (0.30 vs the Dragon's 0.32: 0.879×) |
| T, in water (default `travel`) | 13 flagged S | AttackSquid, Crab, Flounder, Hammerhead, Hydrolisc, Irukandji, Lizard, RubberDucky, SeaMonster, Skate, Whale (+ Beaver, GoldFish where they swim) | **0.50×** in water — the engine's 0.04 → 0.02 (§1.4); SeaViper / WaterDragon on their custom travel 0.55-1.1× |
| **C — custom mover** | 32 rows | the flyers (Bee … Vortex, King, Queen), the hoppers (WormMedium/Small), the parts | direct motion writes on both sides — the attribute does not enter their flight; where they also walk (Dragon, Leon, Spyro, Stinky, the Prince family, Cephadrome, Ostrich) the T law applies with matching numbers |
| **X — port-only** | 4 rows | AlienBoss, BabyDragon, VampireButterfly, AppleCow / GoldenAppleCow | no 1.7.10 number to compare (the 2026-08-11 ruling gates VampireButterfly / AppleCow / GoldenAppleCow behind `phase14ContentEnable`; AlienBoss carries its own header) |

Re-tune records found: **none** on any species' movement speed (MODERNIZATION_NOTES has no MOD entry on a speed; FIX_LOG's speed entries are the per-tick attribute writers — SeaMonster's water boost, OPT-009's constructor asserts; ENT-K-051 restored PitchBlack's five tiers to exactly `0.2 + 0.1 × scale`). Every port number in the table is the 1.7.10 number carried into the modern mover; for the T class that is correct by §1.2/1.3, for the L class it is the ENT-S-150 problem.

### 2.2 Notes on the edge rows

- **EntityCannonFodder** (`cannon_fodder`, registered in the port under the ENT-S-095 ruling as a port-only 0.6 × 0.6 entity): 1.7.10 never registered it (no `OreSpawnMain` reference; a base class of Chipmunk / Lizard / Ostrich / VelocityRaptor, whose own `isAIEnabled → true` overrides make them T). The base class itself has no `isAIEnabled` override and no speed base (`func_110147_ax` at :16-17 / :48-49 sets health and attack only → the `yj.d` default 0.7), so a *spawned* cannon fodder in 1.7.10 would have been a legacy-loop mob at 3.02 b/s — but none was spawnable. The port's 0.25 with the shared chase 1.25 / path 0.65 is a port-only number; not a finding.
- **SpiderDriver** — the only T species whose 1.7.10 base was a *legacy-era* number: `EntitySpider` (`yn`, no `isAIEnabled` override, `aD 20-26: 0.800000011920929`) was tuned for the legacy loop (0.8 → 3.45 b/s under §1.1), and SpiderDriver.java:51-53 flips the new AI on without re-tuning (no `applyEntityAttributes` in the file) — so in 1.7.10 the driver wandered at `s = 0.8 × 0.65 = 0.52 → 11.67 b/s`, chased its robot at `0.44 → 8.36 b/s`, and panicked at `1.2 → 52.9 b/s` (above the clamp). The port (SpiderDriver.java:84-85 `Spider.createAttributes()`, 1.21.1's 0.30 at `Spider.createAttributes 12-15`) gives 1.64 / 1.18 / 8.74 b/s: 0.141× / 0.141× / 0.165×. Draft ENT-S-155.
- **ThePrinceTeen** — BOSS-026 (FIXED, Phase B/C) set the registered speed to 0.32 (ThePrinceTeen.java:196), but the per-tick `setBaseValue(this.moveSpeed)` write that OPT-009 (Phase F, 2026-08-11) turned into a constructor assert (:139) reads the field `moveSpeed = 0.35f` (:108) — OPT-009's own resolution text records it ("ctor-once is exactly value-preserving even where the float-literal set differs ... e.g. ThePrinceTeen 0.35f vs registered 0.32") as value-preserving, which it is for the *port's* pre-existing per-tick write, not for BOSS-026's target. The effective attribute is 0.35: wander 2.98 vs 2.49 b/s, chase 7.62 vs 6.37 (1.196×). Draft ENT-S-156.
- **BabyDragon** (port-only subclass; 1.7.10's "Baby Dragon" is the Dragon class, Dragon.java:1354): `BABY_SPEED 0.30` vs the Dragon's 0.32 → 0.879× (12 %, under the 25 % bar; noted, not filed).
- **PitchBlack**: port `SIZE_SPEED {0.25, 0.30, 0.40, 0.50, 0.60}` (PitchBlack.java:122, applied :280) = orig `0.2 + 0.1 × scale` for `scale ∈ {0.5, 1, 2, 3, 4}` (PitchBlack.java:56/78-79, :247-248) — exact.
- **Crab / SeaMonster / SeaViper / WaterDragon**: the per-tick attribute writers (Crab.java:203-205 `0.55 / 0.95 in water × scale`; SeaMonster.java:125-132 `0.25 / 0.55`; SeaViper / WaterDragon `0.25 / 0.75` and `0.25 / 0.55`) match orig (Crab.java:130-131, SeaMonster.java:126, SeaViper.java:130, WaterDragon.java:224) — but the *water* pace is the §1.4 class, not the attribute.
- **Cows** (RedCow, CrystalCow, GoldCow, EnchantedAppleCow): vanilla `EntityCow` / `Cow` attributes on both sides (0.2, task AI in both — UNVERIFIED here: neither cow class was javap-checked; not an OreSpawn number).
- **Coin / Tshirt**: 0 on both sides. **WormMedium / WormSmall**: no navigator task on either side; they hop by direct motion.

---

## 3. The mapping — options for the owner

The class needing a mapping is the two L species; the T species need nothing (§1.3); the S class in water is a separate engine difference. Player references: walk 4.32, sprint 5.61 b/s.

### (a) Per-species base re-tune so the modern steady state equals 1.7.10's

Formula, port-wide: for a legacy-loop species `A' = √(0.1 · A_1.7.10)` (from `43.17·A'² = 4.317·A`), for a task-AI species `A' = A_1.7.10` (already the case). Applied:

| Species | 1.7.10 idle | `A'` (calm) | port today | 1.7.10 boosted | `A''` (boosted) |
|---|---|---|---|---|---|
| EnderKnight | 0.32 → 1.381 b/s | **0.1789** → 1.381 | 0.32 → 4.421 | 4.405 (cap) | **0.3194** → 4.405 |
| EnderReaper | 0.37 → 1.597 b/s | **0.1924** → 1.597 | 0.37 → 5.910 | 4.405 (cap) | **0.3194** → 4.405 |

The boosted value `A'' = √(0.10204) = 0.3194` is the same for both because the legacy cap is species-independent. (a) alone would make the chase 1.38 / 1.60 b/s — slower than 1.7.10's chase — so (a) needs (b). What a player sees under (a)+(b): the 1.7.10 sequence — a Knight ambling at 1.4 b/s (a third of walking pace), then a 4.4 b/s rush the instant it locks on (walking pace, outrun by a sprint); the Reaper 1.6 → 4.4. Cost: two `createAttributes` numbers, the modifier, the withdrawn ENT-S-145 pins re-based (28 rows); no other test pins a Knight/Reaper speed (the only `MOVEMENT_SPEED` pin in the gametests is the Kraken's 0.37, CoreStatTests.java:124).

For SpiderDriver the same option is its own `createAttributes` at 0.8 (1.7.10's number; 11.7 b/s wander — 2× a sprinting player — and a 52.9 b/s panic; "as 1.7.10", absurd as it is) — or the 1.21.1 Spider's 0.3 recorded as a MOD (§4).

### (b) The attacking boost as a modifier, with the arithmetic

The target is `A'' = 0.3194` (4.405 b/s) while a target is held. On the re-tuned bases of (a):

- Knight: `0.3194 / 0.1789 = 1.786` → **MULTIPLY_BASE +0.786**, or **ADD_VALUE +0.1406**.
- Reaper: `0.3194 / 0.1924 = 1.661` → **MULTIPLY_BASE +0.661**, or **ADD_VALUE +0.1271**.

One shared MULTIPLY_BASE cannot serve both (the 1.7.10 cap is absolute, the bases differ); ADD_VALUE mirrors orig's operation 0 but is also per-species. On the port's *current* bases (0.32 / 0.37) the multiplier that reaches 4.405 is 0.998 (Knight: nothing to add) and 0.863 (Reaper: a slow-down) — i.e. with today's bases there is no boost to apply, and the ruling's "≈2.1" would be a 9.6× overshoot (§1.6 item 1). The record's third candidate, vanilla's 0.15 ADD_VALUE, gives on the port's 0.32 base `0.47 → 9.54 b/s` (2.16× the port's own idle, 2.17× the 1.7.10 sprint) — the modern enderman's feel, not 1.7.10's.

### (c) Leave as is, with a PN entry per class (the engine's part)

What a player sees today: the Ender Knight walks *and* chases at 4.4 b/s (a walking player's pace, no burst on lock-on), the Reaper at 5.9 b/s (faster than a sprinting player — it cannot be outrun on foot, where 1.7.10's could, just); neither has 1.7.10's idle amble or its visible "calm → rush" moment. The SpiderDriver crawls at 1.6 b/s where 1.7.10's tore around at 11.7. Every default-travel swimmer swims at half pace (an Attack Squid 0.49 vs 0.98 b/s; a Hammerhead chasing at `s = 0.4375` 0.86 vs 1.71). A PN per class would say: "legacy-loop species (Ender Knight / Reaper) carry their 1.7.10 attribute into the modern mover: 3.2× / 3.7× the 1.7.10 idle walk, the 1.7.10 sprint pace as the idle pace, no attacking boost"; "swimmers: the modern engine's attribute-free 0.02 water factor halves the 1.7.10 pace"; "SpiderDriver on the 1.21.1 spider base".

### The species that differ most from 1.7.10 today (by ratio, ground unless noted)

1. SpiderDriver, panic 1.5 — 0.165× (52.9 → 8.7 b/s; above the clamp in 1.7.10)
2. SpiderDriver, wander 0.65 — 0.141× (11.67 → 1.64)
3. SpiderDriver, robot chase 0.55 — 0.141× (8.36 → 1.18)
4. EnderReaper, idle wander — 3.70× (1.60 → 5.91)
5. EnderKnight, idle wander — 3.20× (1.38 → 4.42)
6. Every default-travel swimmer, in water — 0.50× (e.g. AttackSquid 0.98 → 0.49, Whale 1.37 → 0.69, Hammerhead chase 1.71 → 0.86)
7. WaterDragon, in water (custom travel) — 0.55× wander (2.16 → 1.19), 0.66× chase, 1.1× owner-follow
8. SeaViper, in water (custom travel) — 0.75× wander (2.94 → 2.20), 1.125× bite chase
9. EnderReaper, chase — 1.34× (4.41 → 5.91)
10. ThePrinceTeen, wander and chase — 1.196× (2.49 → 2.98; 6.37 → 7.62)

Then the port-only BabyDragon at 0.879× of the Dragon it stands in for, and the Ender Knight's chase at 1.004×. Every other row is 1.000.

---

## 4. Findings to file

Four drafts (Appendix B), ENT-S-155 to ENT-S-158 (ENT-S-154 is the XS lane's). Modifiers the port dropped: only the Ender pair's +6.2 (ENT-S-145, already filed and HELD; ENT-S-157 carries its mapping); `EntityCreature`'s "Fleeing speed bonus" (`td.i`, ×2 MULTIPLY, applied by `td.bq` 33-45 while `fleeingTick > 0`) never fires for an `EntityMob` in 1.7.10 (only `EntityAnimal.attackEntityFrom` sets `fleeingTick`), so nothing in the L class lost it. Species off by more than 25 % without a MOD record: SpiderDriver (ENT-S-155), ThePrinceTeen (ENT-S-156), EnderKnight / EnderReaper idle (ENT-S-157), the swimmers in water (ENT-S-158).

---

## 5. Stray-process check

`tasklist | grep -i "javac\|python\|javap"` after the last javap and the table generator: **no matches** (grep exit 1). Three `java.exe` remain (PIDs 66928 / 35132 / 63232) — Gradle daemons / other worktrees' JVMs, not this lane's; every javap (12 invocations, one of them 76 classes in a single JVM) and the one `python gen_table.py` ran under `timeout` and exited 0.

---

## Appendix A — the offset quotes behind §1

### A.1 1.7.10 (obfuscated; identities by shape)

- `sv` = EntityLivingBase (has `e(FF)` with 0.91f / 0.16277136f / 0.08d and `bl()`); `sw` = EntityLiving (constructs `tu/tv/tt/tr/vv/vw` helpers, `bk()` false); `td` = EntityCreature (`bq()` with the 16.0f range, `yj.d` read, `sw.bq()` super call); `sa` = Entity (`a(FFF)` moveFlying); `yj` = SharedMonsterAttributes (static init: `generic.movementSpeed`, default `0.699999988079071`); `tv` = EntityMoveHelper (`c()` multiplies `yj.d` by its speed and calls `sw.i(F)`); `vv` = PathNavigate (`f()` hands its `d:D` speed to `tv.a(DDDD)`); `yg` = EntityMob (`extends td`, no `bk()`); `ya` = EntityEnderman (static init `15: "Attacking speed boost"`, `17: 6.199999809265137d`, strings `mob.endermen.stare/portal`, no `bk()`); `yn` = EntitySpider (`extends yg`, `aD`: health 16, speed `0.800000011920929`, datawatcher bit 16, strings `mob.spider.say/death`, no `bk()`); `yh` (`extends yq` = EntityZombie) carries the pigman's 0.45 modifier of the same name.
- `sv.bl()`: `0: aload_0; 1: invokevirtual bk()Z; 4: ifeq 12; 7: aload_0; 8: getfield bp:F; 11: freturn; 12: ldc 0.1f; 14: freturn`.
- `sw.bk()`: `0: iconst_0; 1: ireturn`.
- `sw.i(F)`: `2: invokespecial sv.i(F)V; 7: invokevirtual n(F)V`; `sv.i(F)`: `2: putfield bp:F`; `sw.n(F)`: `2: putfield be:F`.
- `td.bq()` 552-566: `aload_0; aload_0; getstatic yj.d; invokevirtual a(Lth;)Lti;; invokeinterface ti.e()D; d2f; putfield be:F`. 298-308: `getfield aU:I; bipush 100; if_icmpge 311; invokevirtual bQ()V`. 361-370: `invokespecial sw.bq()V; aconst_null; putfield bp; return`.
- `sw.bq()`: `1: invokespecial sv.bq()V` (`sv.bq`: `aU += 1`); `4-6: fconst_0; putfield bd:F`; `9-11: fconst_0; putfield be:F`.
- `sa.a(FFF)`: `25: fload 4; 27: fconst_1; 28: fcmpg; 29: ifge 35; 32: fconst_1; 33: fstore 4; 35: fload_3; 36: fload 4; 38: fdiv; 39: fstore 4; 41-50: fmul ×2`.
- `sv.e(FF)`: `314: ldc 0.91f`; `357: getfield aji.K:F; 360: ldc 0.91f; 362: fmul`; `364: ldc 0.16277136f; 366-371: fload_3 ×3 fmul fmul fdiv`; `374: getfield D:Z (onGround); 381-388: invokevirtual bl()F; fload 4; fmul`; `393-397: getfield aQ:F`; `404: invokevirtual a(FFF)V`; `703: ldc2_w 0.08d`; `742: ldc2_w 0.98d`; `754-757: fload_3 f2d dmul putfield v:D`; `765-768: … putfield x:D`. Water: `36: bk(); 42: ldc 0.04f; 47: ldc 0.02f; 49: a(FFF)`; `73/85/97: ldc2_w 0.8d`; `109: ldc2_w 0.02d`. Constructor `43: ldc 0.02f; 45: putfield aQ:F`.
- `sv.e()`: `332: bk(); 335: ifeq 367; 345: "newAi"; 351: bn(); 374: "oldAi"; 380: bq(); 395-398: getfield y → putfield aO`; `515-521: getfield bd; ldc 0.98f; fmul; putfield bd`; `526-532: be`; `537-543: bf × 0.9f`; `548-555: getfield bd; getfield be; invokevirtual e(FF)V`.
- `tv.c()`: `152-169: getstatic yj.d; … invokeinterface ti.e()D; dmul; d2f; invokevirtual sw.i(F)V`.
- `vv.a(ayf,D)`: `52: putfield d:D`; `vv.f()`: `74: getfield d:D; 77: invokevirtual tv.a(DDDD)V`.

### A.2 NeoForge 21.1.223

- `MoveControl.tick`: `334: getfield speedModifier; 341: getstatic Attributes.MOVEMENT_SPEED; 344: Mob.getAttributeValue; 349: Mob.setSpeed(F)`.
- `Mob.setSpeed(F)`: `2: invokespecial LivingEntity.setSpeed(F); 7: invokevirtual setZza(F)`; `LivingEntity.setSpeed`: `2: putfield speed`; `getSpeed`: `getfield speed`.
- `LivingEntity.aiStep`: `265: isEffectiveAi; 287: serverAiStep`; `607-614: getfield xxa; ldc 0.98f; fmul; putfield xxa`; `619-626: zza`; `729: travel`.
- `LivingEntity.travel`: `1077-1102: getBlockPosBelowThatAffectsMyMovement / getBlockState / BlockState.getFriction`; `1108: onGround; 1116: ldc 0.91f; 1119: fmul; 1123: ldc 0.91f`; `1130: fload 7; 1132: handleRelativeFrictionAndCalculateMovement`; `1301: ldc2_w 0.98d; 1314: setDeltaMovement(DDD)`. Water: `162: getWaterSlowDown (0.8f); 167: ldc 0.02f; 173: getstatic Attributes.WATER_MOVEMENT_EFFICIENCY; 176: getAttributeValue; 183-194: onGround / × 0.5f; 199-201: fcmpl / ifle 233; 206: ldc 0.54600006f; 221: getSpeed; 234-243: DOLPHINS_GRACE / 0.96f; 251: NeoForgeMod.SWIM_SPEED; 265: moveRelative; 276: move; 327: ldc2_w 0.8d; 336: setDeltaMovement`.
- `handleRelativeFrictionAndCalculateMovement`: `3: getFrictionInfluencedSpeed(F); 7: moveRelative(F, Vec3); 30: move(MoverType, Vec3); 34: getDeltaMovement`.
- `getFrictionInfluencedSpeed(F)`: `1: onGround; 4: ifeq 24; 8: getSpeed; 11: ldc_w 0.21600002f; 14-19: fload_1 ×3 fmul fmul fdiv; 20: fmul; 24-25: getFlyingSpeed`; `getFlyingSpeed`: `1-7: getControllingPassenger instanceof Player; 11-17: getSpeed × 0.1f; 21: ldc_w 0.02f`.
- `Entity.getInputVector`: `1: lengthSqr; 6: ldc2_w 1.0E-7d; 13: Vec3.ZERO; 17-20: dload_3; dconst_1; dcmpl; ifle 30; 24: normalize; 31-33: f2d; scale(D)`.
- `PathNavigation.tick`: `198: getfield speedModifier; 201: MoveControl.setWantedPosition(DDDD)`; `moveTo(Path,D)`: `54: putfield speedModifier`.
- `MeleeAttackGoal`: `350: getfield speedModifier; 353: PathNavigation.moveTo(Entity,D)`; `RandomStrollGoal`: `20: getfield speedModifier; 23: moveTo(DDDD)`.
- `FlyingMoveControl.tick`: `152: onGround; 159-169: speedModifier × MOVEMENT_SPEED; 180-190: speedModifier × FLYING_SPEED; 203: setSpeed`.
- `SmoothSwimmingMoveControl.tick`: `211-226: speedModifier × MOVEMENT_SPEED → fstore 10`; `232: isInWater; 242-249: fload 10 × inWaterSpeedModifier; setSpeed`; `384-389: fload 13 × fload 10 → putfield zza`; `396-402: fload 14 fneg × fload 10 → putfield yya`; `437-447: fload 10 × outsideWaterSpeedModifier × turning factor → setSpeed`.
- `EnderMan.createAttributes`: `12: MOVEMENT_SPEED; 15: ldc2_w 0.30000001192092896d`; static init: `13: SPEED_MODIFIER_ATTACKING_ID; 16: ldc2_w 0.15000000596046448d; 19: Operation.ADD_VALUE; 25: putstatic SPEED_MODIFIER_ATTACKING`. `Zombie.createAttributes`: `15: 0.23000000417232513d`. `Spider.createAttributes`: `6: 16.0d; 15: 0.30000001192092896d`.

### A.3 Source greps the table was built from (paths absolute, read-only)

- 1.7.10 `isAIEnabled` (`func_70650_aV`) overrides: 108 classes return true; `AntRobot.java:884` / `SpiderRobot.java:848` return `riddenByEntity == null`; **no override** in EnderKnight.java, EnderReaper.java, Island.java, IslandToo.java, Elevator.java, GodzillaHead.java, KingHead.java, QueenHead.java, RockBase.java, EntityCannonFodder.java. No OreSpawn class overrides `func_70626_be` (updateEntityActionState), `func_70612_e` (moveEntityWithHeading), `func_70689_ay` / `func_70659_e` (get/setAIMoveSpeed).
- 1.7.10 `field_111263_d` bases and `moveSpeed` fields: `C:\Homework\Projects\Orespawn\reference_1_7_10_source\sources\danger\orespawn\*.java` (EnderKnight.java:30/44/100-107, EnderReaper.java:30/44/100-107, SpiderDriver.java:38-39/51-53/71, PitchBlack.java:56/78-79/247-248, Island.java:48-52, IslandToo.java:41-45, the rest as in the table's notes). Modifier sites: only the Ender pair's `AttributeModifier` (`func_111121_a` / `func_111124_b` at :102-104).
- 1.7.10 tasks with a speed argument: `scratchpad/survey150/1710_tasks.txt` (524 task registrations); the direct-motion classification is the count of `field_70159_w` / `field_70179_y` assignments per class (`scratchpad/survey150` run log).
- Port: `C:\Homework\Projects\Orespawn\src\main\java\danger\orespawn\entity\*.java` — `MOVEMENT_SPEED` in `createAttributes` / constructors, the goal constructors' speed arguments, `moveTo(…, speed)` sites, `BugMeleeAttackGoal.Params` (ai/BugMeleeAttackGoal.java:71-83) and `DinosaurMeleeAttackGoal.Presets` (ai/DinosaurMeleeAttackGoal.java:33-44), the `travel` overrides (AntRobot:626, Cephadrome:237, Dragon:431, Elevator:553, EntityLeon:360, Ostrich:251, SeaViper:172, WaterDragon:191, ThePrinceAdult:376, ThePrinceTeen:380), `ModEntities.java` (145 registrations). The XS lane's files (EntityDragonfly / EntityStinky / EntitySpyro) were not read for facts beyond their attribute lines, which HEAD carries at 0.33 / 0.3 / 0.3.
- Records: AUDIT_FINDINGS.md ENT-S-145 (:9572-9598), ENT-S-150 (:9862-9865), ENT-S-138 (:8994), BOSS-026 (:3595-3601), ENT-K-051 (:2185-2191), OPT-009 (:5712-5719); MODERNIZATION_NOTES.md (no MOD on a species' speed; the 2026-08-11 invented-species ruling :442-451); FIX_LOG.md (:315 SeaMonster's water boost, :2354 OPT-009); gametests: the only `MOVEMENT_SPEED` value pin is CoreStatTests.java:124 (Kraken 0.37); MiscTargetingParityTests.java:848 and TargetReleaseParityTests.java:285 set 0.0 to freeze a mob.

---

## Appendix B — draft findings (ENT-S-155 … ENT-S-158)

### ENT-S-155 — The SpiderDriver walks on 1.21.1's spider base (0.3) where 1.7.10 inherited `EntitySpider`'s legacy-era 0.8 into the task AI it switched on: 0.14× the 1.7.10 pace (REPORT, 2026-09-06; found by the ENT-S-150 survey)
- **Evidence:** orig SpiderDriver.java:51-53 `func_70650_aV` → true (the task AI), :38-39 `EntityAIPanic(1.5)` / `MyEntityAIWander(0.65f)`, :71 `navigator.tryMoveToEntity(e, 0.55)`; no `func_110147_ax` in the file, so the base is `EntitySpider`'s — Mojang's 1.7.10 jar `yn.aD` 20-26: `yj.d ← 0.800000011920929` (a number tuned for the legacy loop `yn` itself ran: no `bk()` override). Under the 1.7.10 task law (§1.2 of the survey) the driver wandered at `0.8 × 0.65 = 0.52 → 11.7 blocks/s`, chased its robot at `0.44 → 8.4`, panicked at `1.2 → 52.9`. Port SpiderDriver.java:84-85 `return Spider.createAttributes()` — NeoForge 21.1.223 `Spider.createAttributes` 12-15: `MOVEMENT_SPEED 0.30000001192092896` — with the same modifiers (:78-79 panic 1.5 / stroll 0.65, :112 `moveTo(robot, 0.55)`): 1.64 / 1.18 / 8.74 blocks/s, 0.141× / 0.141× / 0.165×. Player-visible: a dismounted driver in 1.7.10 outran a sprinting player twice over; the port's crawls at a third of walking pace.
- **Resolution:** REPORT — for the owner: (i) parity, classic — SpiderDriver's own `createAttributes` at 0.8 (`Spider.createAttributes().add(MOVEMENT_SPEED, 0.8)`), one pin on the attribute base; or (ii) a MOD record keeping 1.21.1's 0.3 (the 1.7.10 pace is an artefact of a legacy-era number under a task-AI law, and 52.9 blocks/s on panic is a glitch, not a design). Effort XS either way. No test pins the value.

### ENT-S-156 — ThePrinceTeen's effective movement speed is 0.35, not the 0.32 BOSS-026 fixed: the OPT-009 constructor assert re-applies the pre-fix `moveSpeed = 0.35f` field over the registered base (REPORT, 2026-09-06; found by the ENT-S-150 survey)
- **Evidence:** orig ThePrinceTeen.java:87 `moveSpeed = 0.32f`, :139 `setBaseValue(moveSpeed)`. Port ThePrinceTeen.java:196 `.add(Attributes.MOVEMENT_SPEED, 0.32)` (BOSS-026, FIXED 2026-06-11: "speed 0.32") — but :108 `private final float moveSpeed = 0.35f` and :139 `this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(this.moveSpeed)` in the constructor, the OPT-009 (Phase F, 2026-08-11) conversion of the earlier per-tick write; OPT-009's resolution text names the case ("ThePrinceTeen 0.35f vs registered 0.32") as value-preserving — preserving the port's own per-tick 0.35, i.e. the BOSS-026 divergence survived under it. Effect (§1.3 law): wander 0.75 → 2.98 vs 2.49 blocks/s, follow 1.1 → 6.40 vs 5.35, chase 1.2 → 7.62 vs 6.37 (1.196×). ThePrincess.java:77 carries the same pattern in reverse (`moveSpeed = 0.3f`, unused: no constructor write; the registered 0.32 stands) — a dead field, not a divergence.
- **Resolution:** REPORT — parity, classic: the field to 0.32f (or the constructor line removed, the registered value standing), one pin `getAttributeBaseValue(MOVEMENT_SPEED) == 0.32` after construction. Effort XS. The ThePrincess dead field can go in the same hunk.

### ENT-S-157 — The Ender pair's idle walk under the modern mover is 3.2× / 3.7× 1.7.10's legacy walk — the ENT-S-150 class has exactly these two moving members, and the mapping that restores 1.7.10's calm → rush is a base re-tune plus the boost (REPORT, 2026-09-06; the ENT-S-150 survey's class finding)
- **Evidence:** the survey §1 (offsets) and §3. 1.7.10: `sw.bk()` false for both (no override in EnderKnight.java / EnderReaper.java; `yg` = EntityMob has none), so `td.bq` 552-566 wrote the attribute into `moveForward`, `sv.bl()` 12-14 returned the fixed 0.1f, `sa.a(FFF)` 25-39 clamped the input at 1: idle `4.317 × A` = 1.38 / 1.60 blocks/s, the +6.2 modifier (orig :30, :100-107) saturating at 4.405 (the cap, both species). Port EnderKnight.java:56-57 / EnderReaper.java:54-55 `MeleeAttackGoal(1.0)` + `WaterAvoidingRandomStrollGoal(1.0)` on :206 / :203 `0.32` / `0.37`: `MoveControl.tick` 334-349 → `Mob.setSpeed` (speed and zza) → `43.17 × A²` = 4.42 / 5.91 blocks/s for idle and chase alike — 3.20× / 3.70× the 1.7.10 idle, 1.00× / 1.34× the 1.7.10 sprint, and no burst on lock-on. The other legacy-by-class species never move by the attribute (Island / IslandToo zero their motion; the `EntityLiving` parts' input is zeroed by `sw.bq` 4-11; the robots' legacy state is the ridden custom mover), and every task-AI species carries over 1:1 (§1.2 = §1.3).
- **Resolution:** REPORT — the owner's mapping choice (§3): (a)+(b) bases `√(0.1·A)` = 0.1789 / 0.1924 with the attacking modifier to 0.3194 (MULTIPLY_BASE +0.786 / +0.661, or ADD_VALUE +0.1406 / +0.1271 — per species, the cap being absolute); the ENT-S-145 hunks re-applied with that number; or (c) as-is with a PN. Effort XS once ruled (two `createAttributes` numbers, the modifier constants, the s145 pins re-based). Not the record's "MULTIPLY_BASE ≈ 2.1 / ADD_VALUE ≈ 0.67 on 0.32" — those give ≈42 blocks/s under the quadratic law (§1.6).

### ENT-S-158 — Every OreSpawn swimmer on the default `travel` moves at half its 1.7.10 pace in water: the engine's AI-mob water factor 0.04 became an attribute-free 0.02 (REPORT, 2026-09-06; found by the ENT-S-150 survey)
- **Evidence:** 1.7.10 `sv.e(FF)` 36-47: `moveFlying(strafe, forward, bk() ? 0.04f : 0.02f)`, 73-97 `× 0.8`; every OreSpawn swimmer is `bk()` true → `v = 4.0 × min(1, 0.98·s)` blocks/s. NeoForge 21.1.223 `LivingEntity.travel` 162-167: `f6 = 0.02f`, blended toward `getSpeed()` only by `WATER_MOVEMENT_EFFICIENCY` (173-176, default 0), `265: moveRelative(f6, …)`, `327-336: × 0.8` → `v = 2.0 × min(1, 0.98·s)`: 0.50× for AttackSquid (0.98 → 0.49), Hammerhead (wander 1.37 → 0.69; chase 1.25 1.71 → 0.86), Skate, Flounder, Whale (1.37 → 0.69), SeaMonster (in-water attribute 0.55: 2.16 → 1.08), Irukandji, RubberDucky, Lizard, Hydrolisc, Crab (0.95 × scale in water: 3.72 → 1.86 at scale 1), and Beaver / GoldFish where they swim. The two custom swimmers are a different shape: SeaViper.java:100 / WaterDragon.java:114 `SmoothSwimmingMoveControl(…, 0.02f, 0.1f, …)` and their `travel` (:172-181 / :191-200, `moveRelative(getSpeed())`, `× 0.9`) give `4.0 × s × min(1, 0.98·s)`: SeaViper wander 2.20 vs 2.94 (0.75×), bite chase 4.5 vs 4.0; WaterDragon wander 1.19 vs 2.16 (0.55×), chase 1.71 vs 2.59, owner-follow 4.4 vs 4.0. The Ender pair in water are unchanged (0.02 on both sides — `bk()` false in 1.7.10). Player-visible: every sea mob is slower than 1.7.10 in the water it lives in; a Hammerhead no longer catches a swimming player (2.2 blocks/s) it caught in 1.7.10 (chase 1.71 vs the player's 2.2 — close; the port's 0.86 is not).
- **Resolution:** REPORT — for the owner: (i) a shared `travel` override for the S class reproducing the 0.04 factor in water (`moveRelative(0.04f, vec)` then `× 0.8`, the 1.7.10 shape; one pin per species on the in-water steady state); (ii) per-species `WATER_MOVEMENT_EFFICIENCY = 0.02 / (s − 0.02)` reproduces 1.7.10 at one `s` only (the goal modifiers vary it) — not recommended; (iii) a PN for the class. SeaViper / WaterDragon's custom controller numbers (0.02f / 0.1f) are a Phase B/C choice with no record found — a note either way. Effort S for (i) (13 species, one helper).
