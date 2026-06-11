# Phase B3 — Rider Flight + Mounted Attacks

Build: `.\gradlew.bat compileJava` → **BUILD SUCCESSFUL** (only pre-existing
`EventBusSubscriber.bus` deprecation warnings remain; no new warnings/errors).

## Architecture

All mounts now use the idiomatic 1.21.1 LivingEntity riding API with
client-predicted movement (vanilla horse pattern):

- `getControllingPassenger()` returns the riding player (with the original's
  ownership gates per mount).
- `tickRidden(Player, Vec3)` runs the full original ridden physics on the
  controlling client only (`isControlledByLocalInstance()`); `travelRidden`
  itself is **private** in 1.21.1 and cannot be overridden, so each mount also
  short-circuits `travel(Vec3)` while player-ridden to prevent vanilla from
  integrating the motion a second time.
- The shared physics live in the new
  `entity/ai/RiderFlightController.java` — one line-by-line port of the
  original's hand-rolled ridden-movement block (motion clamp ±2.0, ground
  hover, forward terrain-follow scan, rise cap, speed-lagged yaw + speed
  pitch, fly-up lift / FAST jump, smoothed W/S throttle, post-move friction
  and gravity), parameterized per mount by a `Config` record whose every value
  is cited against the original source in Javadoc.
- Vertical keys: `client/KeybindHandler.java` applies the key state directly
  to the local vehicle each tick (prediction) and sends `RiderInputPayload`
  while held plus one all-false payload on release; the server handler
  (`network/RiderInputPayload.java:50-69`) stores the state on the vehicle via
  `RideableFlyer.setRiderVerticalInput(up, down)` for remote-observer
  consistency, keeping the generic ±0.15 Δy fallback for non-flyer vehicles
  (Elevator). This extends — not redesigns — the sanctioned per-player
  modernization of the original global `OreSpawnMain.flyup_keystate`.
- Server-side ridden duties that are *not* movement (projectiles, pushing,
  mounted auto-melee, rider-removal ejection) run in per-entity
  `serverRiddenTick(...)` from `aiStep`/`customServerAiStep`.

---

## Task 1 — ENT-K-017: Leon rider flight (+ Leonopteryx)

**Original** (`reference_1_7_10_source/.../Leon.java`):
- Ridden physics in the ridden branch of `onLivingUpdate` :741-889 — hover
  probe 1.55 (lift +0.03 motion / +0.1 pos, glide-fall 0.018, :758-765),
  terrain scan `3 + v*7` @ 0.05/block ×0.07 (:767-779), rise cap 2.0
  (:780-782), yaw lag `|1.85 − v|` clamped 0.01–0.9 above v=0.01 (:799-810),
  fly-up `+0.035 + v*0.038` via `flyup_keystate` (:827-830), throttle
  0.028+0.06 bonus (max_speed 1.15 > 1.0 gate; :843-846, max_speed :703)
  ramped via `deltasmooth`, reverse cap 0.35 @ −0.02 (:855-856), friction
  0.985/0.94/0.985 (:887-889). The often-quoted **3.5** is the *wild* flight
  speed; ridden max is **1.15**.
- `fly_with_rider` (:486-528) is the mounted **auto-melee** (attack scan while
  ridden), not the movement code.
- Rider seat: `updateRiderPosition` forward 0.65 (:943-948).

**Port** (`src/main/java/danger/orespawn/entity/EntityLeon.java`):
- `RIDER_FLIGHT_CONFIG` :69-77 (cited per value), controller instance :79-80.
- `getControllingPassenger` :195 (owner only), `positionRider` :210 (0.65
  fwd / 0.85·height up), `tickRidden` :225 + `travel` guard :238,
  `setRiderVerticalInput` :253, `serverRiddenTick` :394 (push box +
  `flyWithRider` :417 mounted auto-melee + rider-removal eject).
- Replaces the interim ground-only 1.8× walk control.

**Decision — Leonopteryx is rideable.** The port registers Leonopteryx as a
separate entity, but the original `Leon.java` is one mob; the wild variant is
the same creature. `Leonopteryx.java` gets identical riding: config :80-89
(delegates citations to EntityLeon), `getControllingPassenger` :199 (tamed
owner), `positionRider` :212, `tickRidden` :226 + `travel` guard, `mobInteract`
empty-hand mounting for tamed owners :260, `serverRiddenTick` :460, and its
wild y-damping is suppressed while ridden.

## Task 2 — ENT-K-044: Ostrich steering + jump/FAST

**Original** (`Ostrich.java` :401-535): runner, not flier — no hover; upward
terrain scan `1 + v*10` @ 0.075/block applied 1:1 (:417-429), rise cap 4.0
(:430-432), yaw lag 1.85 (:448-459), **FAST jump**: flyup key triggers a
single `+1.0 + v*6.0` hop with a 20-tick latch that only counts down after key
release (:470-478) — the "FAST" semantics are the velocity-scaled jump, not a
horizontal speed boost. Throttle 0.045 ramped to max 0.75 (:368, :491-498),
reverse 0.25 @ −0.03 (:500-501), gravity 0.25 + friction 0.95/0.85/0.95
(:532-535).

**Port** (`Ostrich.java`): `RIDER_RUN_CONFIG` :53-61 (jumpMode=true,
obstructionScansUp=true), `getControllingPassenger` :155 (any rider),
`positionRider` :168 (−0.15 fwd, 1.4 up), `tickRidden` :183 + `travel` guard,
`setRiderVerticalInput` :211 (up only; a runner has no descend control).

## Task 3 — Cephadrome rider flight

**Original** (`Cephadrome.java` :703-835): hovering sand-shark — strong hover
lift +0.07/+0.1 @ probe 1.55, glide-fall 0.018 (:720-727), terrain scan
`2 + v*6` @ 0.04/block ×0.09 (:728-741), rise cap 2.0 (:742-744), yaw lag 1.5
above v=0.1 (:760-771), pitch *inverts* while rising (`360 − 2v`, :776),
fly-up `+0.04 + v*0.05` (:786-789), throttle 0.03+0.05 applied **instantly**
(no deltasmooth; max_speed 1.15 > 0.85 gate, :800-809, max_speed :673),
reverse 0.35 @ −0.03 (:807-808), friction 0.985/0.94/0.985 (:833-835).
Riding gated on `wasfed`; `PLAY_NICELY` off forces `wasfed = 1`.

**Port** (`Cephadrome.java`): `RIDER_FLIGHT_CONFIG` :73-81
(invertPitchWhenRising=true, smoothAccel=false), no-gravity while ridden +
`serverRiddenTick` from `tick()` :168-171, `getControllingPassenger` :184 (any
rider — original had no owner gate), `positionRider` :196 (0.75 fwd, 2.5 up),
`tickRidden` :210 + `travel` guard, `setRiderVerticalInput` :238,
`serverRiddenTick` :249 (push + eject; the original has **no** mounted
auto-attack for Cephadrome), `mobInteract` empty-hand mounting when fed :395.

## Task 4 — BOSS-027 / BOSS-033: ThePrinceTeen / ThePrinceAdult

**Original**:
- `ThePrinceTeen.java` — ridden flight :879-1087 (hover probe 1.25, scan
  3+v*7 @ 0.05×0.07, rise cap 2.0, yaw lag 1.85, fly-up `+0.035 + v*0.046`
  :961-964, throttle 0.025 ramped to max 0.95 :843/:978, reverse 0.35 @
  −0.02, friction 0.985/0.94/0.985 :1085-1087); `func_70085_c` :1157 —
  saddle-free mounting gated on tamed + owner; strafe keys while ridden fire
  the three-head canon trio (fireball / iceball / thunderbolt cycle).
- `ThePrinceAdult.java` — same structure :859-1069; fly-up
  `+0.045 + v*0.066` :941-944, throttle 0.035+0.07 (max_speed 1.05 > 1.0
  bonus gate :958-959), `func_70085_c` :1134; adult fireballs are `setBig()`.

**Port**:
- `ThePrinceTeen.java`: config :67-75, `getControllingPassenger` :206 (tamed
  owner), `positionRider` (0.65 fwd, 2.75 up), `tickRidden` :238 + `travel`
  guard :252, `serverRiddenTick` :278 (strafe → `fireCanonTrio` :312, push,
  `flyWithRider` :371 auto-melee + `shootSomethingAt` :400 random-head canon,
  eject), `mobInteract` :531 (diamond-block taming + empty-hand mounting for
  tamed owners), wild ground combat skipped while ridden.
- `ThePrinceAdult.java`: config :69-77, same structure — `tickRidden` :242,
  `travel` guard :256, `serverRiddenTick` :282, `fireCanonTrio` :316
  (BetterFireball `setBig()`), `flyWithRider` :377, `shootSomethingAt` :406,
  `mobInteract` :524.
- **BUG-010 not regressed**: only RIDDEN movement was added; the wild
  autonomous flight (activity 2) of the baby ThePrince/ThePrincess stays
  disabled (Phase D), and their `noPhysics = false` interim fix is untouched
  (`ThePrince.java`/`ThePrincess.java` not modified for flight).

## Task 5 — BUG-020: Dragon client-predicted riding

**Original** (`Dragon.java` :919-1165): hover probe 1.25 (+0.03/+0.1, fall
0.018, :935-942), scan 3+v*7 @ 0.05×0.07 (:944-956), rise cap 2.0, yaw lag
1.85 (:975-986), fly-up `+0.03 + v*0.036` (:1001-1004), throttle 0.025 ramped
(max_speed 0.95 :882 — under the >1.0 bonus gate :1018), reverse 0.35 @
−0.02, friction 0.985/0.94/0.985 (:1163-1165).

**Port** (`Dragon.java`): the old server-side `handleRiderFlight` in `aiStep`
(rubber-banding) was replaced by `tickRidden` :406 running
`RiderFlightController` (config :73-81) on the riding client; `travel` :387
skips both wild-AI flight (activity 1) and player-ridden movement. The server
keeps only non-movement duties in `serverRiddenTick` :420: strafe projectiles
(`handleRiderProjectiles` — fire dragon: strafe-right small fireball 0.15
accel "random.bow" 10t, strafe-left regular **non-big** fireball 0.1 accel
"random.fuse" 20t, orig :1068-1114; water dragon: WaterBall / special IceBall,
orig :1121-1159), push box 2.25/2.0/2.25 (orig :1166-1172), mounted auto-melee
`fly_with_rider` (orig :486-518), rider-removal eject (orig :1174-1176).
Speeds preserved number-for-number (cited in `RIDER_FLIGHT_CONFIG` Javadoc).

## Task 6 — ENT-S-017/018/019: SpiderDriver

**Original** (`SpiderDriver.java`): `getTotalArmorValue` :96-101 — **8 armor
while mounted, 20 on foot**; mounted branch :74-83 steers the robot
(`goThisWay` 0.35·cos/0.35·sin) toward prey outside `11 + width/2`;
`attackEntity` :86-94 — melee on `attackTime = 16` cooldown plus Poison 60
ticks on a 1-in-2 roll.

**Port** (`SpiderDriver.java`): armor as an `AttributeModifier` with stable id
`orespawn:spider_driver_armor` (`updateArmorModifier` :59, re-synced every
server tick from `customServerAiStep` :95 — covers spawn/load/forced dismount
without override sprawl; vanilla spider base armor is 0 so the modifier
carries the full 8/20). Mounted combat :119-143: look at target, steer the
`SpiderRobot` by setting its ΔM to 0.35·cos/0.35·sin when out of range, and
within melee reach `doHurtTarget` + Poison(60t, amp 0) at 1-in-2 on a 16-tick
cooldown — the mounted branch now actually attacks instead of only looking.
(The port's `SpiderRobot` has no `goThisWay`; direct ΔM steering reproduces it
without touching the other workstream's file.)

## Task 7 — Leon / Cephadrome / Leonopteryx drops

`dropCustomDeathLoot` overrides **deleted** from `EntityLeon.java`,
`Cephadrome.java` (Leonopteryx had none); loot JSON is now the single source
of truth.

- **Orig `Leon.java` `func_70628_a`**: chicken 4-9, feather 16-21, kraken
  repellent 2-7, battle axe at 1-in-5. →
  `data/orespawn/loot_table/entities/leon.json` rewritten to exactly that;
  `leonopteryx.json` rewritten identically (same original mob; removed the
  invented experience-bottle/other drops).
- **Orig `Cephadrome.java` `func_70628_a`**: uranium nuggets 4-9, titanium
  nuggets 4-9, then 1-5 rolls of a switch over {ruby sword (plain), diamond,
  thunder staff, randomly-enchanted ruby sword/shovel/pickaxe/axe, ruby gem,
  gold ingot, empty} → `cephadrome.json` rewritten as a 1-5-roll weighted pool
  mirroring the switch case-for-case (ruby sword appears twice: plain and
  enchanted, matching the two original cases).

## Task 8 — "Special" key (G) verification

Checked every original ridden branch for projectile firing:
- **Dragon**: ranged fire is **strafe-key** driven (orig :1060-1161), not the
  UP key — ported in `handleRiderProjectiles`; `Dragon.riderSpecial` is now a
  **no-op** (the port previously fired an invented big-fireball volley on G —
  corrected, documented at `Dragon.java:1180-1186`).
- **ThePrinceTeen/Adult**: canon trio is also **strafe** driven (orig Teen
  :1010-1080 / Adult :990-1060) — ported in `fireCanonTrio`.
- **Leon/Cephadrome/Ostrich**: no ridden ranged attacks in the original —
  none added. `RideableFlyer.riderSpecial` stays a default no-op; no invented
  special attacks anywhere.

## Task 9 — Elevator

The port **does** have `entity/Elevator.java` (plus `ElevatorRenderer`), but
it is outside this phase's file ownership and is not a `RideableFlyer`; it
continues to use the payload handler's generic ±0.15 Δy fallback
(`RiderInputPayload.java:59-66`), which matches its original
flyup-consumer behavior closely enough until its owner ports it properly.
Noted; no changes made.

---

## Audit findings closed

| ID | Evidence (one line) |
|----|---------------------|
| ENT-K-017 | EntityLeon full ridden flight, orig :741-889 values in `RIDER_FLIGHT_CONFIG` (EntityLeon.java:69-77), client-predicted via tickRidden :225 |
| ENT-K-044 | Ostrich steering + FAST jump (+1.0 + v·6.0, 20t latch, orig :470-478) — Ostrich.java:53-61, :183 |
| BOSS-027 | ThePrinceTeen saddle-free mount (orig :1157) + ridden flight (orig :879-1087) — ThePrinceTeen.java:238, :531 |
| BOSS-033 | ThePrinceAdult same (orig :1134, :859-1069) — ThePrinceAdult.java:242, :524 |
| BUG-020 | Dragon movement moved off the server into tickRidden (Dragon.java:406) with travel guard :387; speeds unchanged |
| ENT-S-017 | 8/20 mounted/on-foot armor via stable AttributeModifier — SpiderDriver.java:59 (orig :96-101) |
| ENT-S-018 | Mounted robot steering 0.35·cos/sin at range 11+w/2 — SpiderDriver.java:124-133 (orig :74-83) |
| ENT-S-019 | Mounted melee doHurtTarget + Poison 60t 1-in-2 on 16t cooldown — SpiderDriver.java:135-141 (orig :86-94) |
| Leon/Cephadrome double-drop | `dropCustomDeathLoot` deleted; leon.json / leonopteryx.json / cephadrome.json rewritten to orig `func_70628_a` lists |

**UNVERIFIED**: none for the assigned scope. (Elevator riding behavior was not
verified against its original — out of ownership, Phase D.)

## Decisions / deviations

1. **Leonopteryx is rideable** — same original mob as Leon; identical config
   and ownership gate (tamed owner). Documented in Leonopteryx.java:78-89.
2. **`tickRidden` instead of `travelRidden`** — 1.21.1 made `travelRidden`
   private; the physics run in `tickRidden` (still called from the private
   `travelRidden` flow) with a `travel()` short-circuit to avoid double
   integration. Functionally identical to the vanilla-horse pattern.
3. **Fly-down key** — port addition (the original had only UP); it mirrors the
   UP lift negatively in `RiderFlightController.tick` and is documented there.
4. **Dragon riderSpecial corrected to no-op** — original had no G-key action.
5. **Ostrich taming food (wheat vs orig apple)** — pre-existing port choice,
   out of this task's scope; not changed.
6. **Elevator untouched** — exists in port but owned elsewhere; generic
   fallback continues to serve it.

## Files changed

- `src/main/java/danger/orespawn/entity/ai/RiderFlightController.java` (new)
- `src/main/java/danger/orespawn/entity/EntityLeon.java`
- `src/main/java/danger/orespawn/entity/Leonopteryx.java`
- `src/main/java/danger/orespawn/entity/Cephadrome.java`
- `src/main/java/danger/orespawn/entity/Ostrich.java`
- `src/main/java/danger/orespawn/entity/Dragon.java`
- `src/main/java/danger/orespawn/entity/ThePrinceTeen.java`
- `src/main/java/danger/orespawn/entity/ThePrinceAdult.java`
- `src/main/java/danger/orespawn/entity/SpiderDriver.java`
- `src/main/java/danger/orespawn/client/KeybindHandler.java`
- `src/main/java/danger/orespawn/network/RiderInputPayload.java`
- `src/main/resources/data/orespawn/loot_table/entities/leon.json`
- `src/main/resources/data/orespawn/loot_table/entities/leonopteryx.json`
- `src/main/resources/data/orespawn/loot_table/entities/cephadrome.json`

Build status: **BUILD SUCCESSFUL** (`compileJava`, Jun 11 2026).
