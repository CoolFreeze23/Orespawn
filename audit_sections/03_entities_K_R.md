# Entity Audit — Slice 03: Kraken → RubyBird

Paths abbreviated: ORIG = `reference_1_7_10_source\sources\danger\orespawn\`, PORT = `src\main\java\danger\orespawn\`, LT = `src\main\resources\data\orespawn\loot_table\entities\`, BM = `src\main\resources\data\orespawn\neoforge\biome_modifier\`.
Original stats from `OreSpawnMain.java` `get_mobstats` (lines ~6460–6525) unless hardcoded in class. Note: several port entities have BOTH a `dropCustomDeathLoot` override AND a loot-table JSON — both fire, producing **double drops**; flagged per entity. Port `MobStats.java` exists but is **unused** by every entity in this slice (all attributes hardcoded in `createAttributes()`).

---

### Kraken
ORIG `Kraken.java` | PORT `entity\Kraken.java`

| Feature | Status | Detail |
|---|---|---|
| Stats | DIVERGENT | ORIG: HP 1000, ATK 40, def 10, speed 0.37, xp 500 (`OreSpawnMain.java:6515`, `Kraken.java` ctor). PORT: HP 3000, ATK 80, armor 8, speed 0.5, knockback-res 1.0, xp 500 (`Kraken.java:66,76-84`). 3× HP, 2× ATK. Port `MobStats.KRAKEN`=500/30 (`MobStats.java:59`) unused. |
| AI goals | PORTED | LookIdle(1)+HurtBy(1) → `RandomLookAroundGoal(1)`/`HurtByTargetGoal(1)` (`Kraken.java:71-74`). Flight target logic, grab-and-carry-to-Y200, drop/release, obstruction-avoid climb, 10-Kraken reinforcement at <1/8 HP above Y130 all ported (`Kraken.java:154-360`). NEW: boss bar (49), random lightning 1/400 (162-168). |
| Attack/abilities | PORTED (minor div) | Grab: catch within dist²<30, teleport target to Y−15, `doHurtTarget` 1/50 (`Kraken.java:284-311` ≈ ORIG `attackWithSomething`). Hurt-invuln window: ORIG `field_70174_ab=120`; PORT `hurtTimer=30` (`Kraken.java:409`) — 4× faster re-damage. Storm: every 100 ticks both (ORIG tick loop; PORT `Kraken.java:132-138`). |
| Drops | DIVERGENT | Double path. Code (`Kraken.java:529-602`): KrakenTooth, golden apple (ORIG: painting `field_151160_bD`), 120–279 **cooked cod** (ORIG: 120–279 ink sac/dye `field_151100_aR`), 5–14 rolls of 53-case enchanted-gear table (ported, ModItems substitutions). PLUS `LT kraken.json`: 2nd KrakenTooth, xp bottle, 120–279 prismarine shard, 5–15 diamond, 5–15 gold ingot, 10% ultimate_sword — entire extra layer not in ORIG. |
| Spawning | DIVERGENT | ORIG: no `addSpawn`; `func_70601_bi` requires open sky column + Y>50 (spawner/summon only). PORT: natural ocean spawn weight 1/1-1 (`BM add_ocean_spawns.json`), **no** `checkSpawnRules` override. A 3000-HP boss now naturally spawns in oceans. |
| Sounds | DIVERGENT | ORIG: `orespawn:kraken_living` (1/5), `orespawn:alo_death`. PORT: vanilla `ELDER_GUARDIAN_AMBIENT` (1/5) / `ELDER_GUARDIAN_DEATH` (`Kraken.java:431-446`). |
| Misc | PORTED | Fire-immune ✔, no fall damage ✔, lightning-immune ✔, despawn-above-Y256 ✔, `longEnough` NBT ✔. `KrakenRevengeHandler.java` exists in port (extra system, not in ORIG slice scope). |

**Verdict: PARTIAL/DIVERGENT — behavior faithfully ported but stats tripled, drops doubled via dual loot paths, sounds replaced with vanilla, and it natural-spawns where original never did.**

---

### Kyuubi
ORIG `Kyuubi.java` | PORT `entity\EntityKyuubi.java`

| Feature | Status | Detail |
|---|---|---|
| Stats | DIVERGENT | ORIG: HP 125, ATK 10 (melee `getAttackStrength`=3), def 10, speed 0.25, xp 30 (`OreSpawnMain.java:6485`). PORT: HP 30, ATK 3, speed 0.25, xp 30 (`EntityKyuubi.java:50-55`). HP nerfed 125→30. Port `MobStats.KYUUBI`=120/16 (`MobStats.java:29`) unused. |
| AI goals | PORTED | ORIG: Swim(0) Panic(1) MoveThroughVillage(2) Wander(3) WatchClosest(4) LookIdle(5) / HurtBy(1). PORT: Float(0) Panic(1) Stroll(2) LookAt(3) LookAround(4) / HurtBy(1) (`EntityKyuubi.java:41-48`). MoveThroughVillage dropped (acceptable 1.21 mapping). Fireball loop ported: 1/10 chance per AI step, range² 64, `SmallFireball` (`EntityKyuubi.java:100-117`); target clear 1/200 ✔. |
| Attack/abilities | PORTED | SmallFireball volley + weak melee (3). |
| Drops | DIVERGENT | Double path. ORIG: 10 gold nugget, 3 redstone block, 4 quartz block, + uranium/titanium nuggets. PORT code (`EntityKyuubi.java:142-153`): 10 gold ingot + 3 TNT + 4 redstone block. PLUS `LT kyuubi.json`: 2–5 ruby + 3–8 blaze powder. |
| Spawning | DIVERGENT | ORIG: Nether weight 10/1-1 (`OreSpawnMain.java:4802`). PORT: Nether weight 5/1-1 (`BM add_nether_spawns.json`). Weight halved. |
| Sounds | PORTED | `kyuubi_living` / `alo_hurt` / `alo_death` ✔ (`EntityKyuubi.java:155-171`). |
| Misc | DIVERGENT | ORIG fire-immune (`field_70178_ae=true`, invuln window 1000). PORT has **no `fireImmune()` override** yet sets itself on fire every ~10 ticks (`EntityKyuubi.java:78`) and hurts itself in water — a wild Kyuubi slowly burns itself to death. |

**Verdict: DIVERGENT — fireball behavior ported but HP gutted (125→30), fire immunity lost (self-damaging), drops doubled and swapped.**

---

### LaserBall (projectile)
ORIG `LaserBall.java` | PORT `entity\LaserBall.java` (+ `item\ItemLaserBall.java`)

| Feature | Status | Detail |
|---|---|---|
| Stats | PORTED | 16 dmg, 200-tick lifetime ✔ (`LaserBall.java:19,16`). Irukandji variant 100 dmg + instant discard ✔ (64-68). |
| Behavior | PORTED | Spin rotation, fire-on-hit 1s unless iceball ✔ (70-73), explosion 3.0 for special/ice gated on mobGriefing ✔ (79-84), explode sound ✔, per-variant particle counts ✔. |
| Missing | PARTIAL | ORIG: irukandji ball drops `MyIrukandji` item when it hits nothing — PORT: no drop on miss. ORIG special-type extra effects beyond explosion not reproduced. |
| Item | PORTED+ | `ItemLaserBall.java`: throwable item (5-tick cooldown) — NEW convenience; ORIG `MyLaserBall` item dropped by robots existed but port robots no longer drop it (see Robot3/4/5). |

**Verdict: PORTED with minor gaps (irukandji miss-drop missing).**

---

### Lavafoam (block)
ORIG `Lavafoam.java` (block) | PORT `block\Lavafoam.java`, registered `ModBlocks.java:51`

| Feature | Status | Detail |
|---|---|---|
| Push/damage | PORTED | Quadrant-based radial push 0.45 + tangential ×1.35, damage = horizontal speed when >1.0 (`Lavafoam.java:57-92` ≈ ORIG `func_149670_a`). |
| Particles | PORTED | smoke/reddust → SMOKE/DUST_PLUME face particles 1/20 (`Lavafoam.java:33-54`). |
| Nether bonus drops | MISSING | ORIG drops 5–14 items when broken in the Nether (dim −1); PORT has no loot override / dimension check. |

**Verdict: PORTED except Nether bonus-drop rule.**

---

### LeafMonster
ORIG `LeafMonster.java` | PORT `entity\EntityLeafMonster.java`

| Feature | Status | Detail |
|---|---|---|
| Stats | DIVERGENT | ORIG: HP 6, ATK 2 (melee dealt 6), def 1, speed 0.25, xp 5 (`OreSpawnMain.java:6516`). PORT: HP 20, ATK 5, speed 0.25, xp 5 (`EntityLeafMonster.java:44-49`). |
| AI goals | PORTED | Swim/Panic/HurtBy ✔ (`EntityLeafMonster.java:38-42`). Block-grid snap + 90° yaw lock while idle ported (`tick()`, 71-91 ≈ ORIG `func_70071_h_`). Attack cadence 1/4 step, melee at dist²<5 ✔. |
| Targeting | DIVERGENT | ORIG: ants, butterflies, luna moths, non-creative players. PORT: players + anything with BbWidth<1.0 (`EntityLeafMonster.java:139-146`) — broader. |
| Drops | DIVERGENT | ORIG: random leaves/log/stick. PORT `LT leaf_monster.json`: 1–3 oak log + 0–2 bone. |
| Spawning | PARTIAL | ORIG: spawner-gated + darkness + ≤4 buddies + dimension checks. PORT: overworld monsters weight 4/1-2 (`BM add_overworld_monsters.json`), no `checkSpawnRules`. |
| Sounds | PORTED | `leaves_hit` / `leaves_death`, no ambient ✔; capped fall damage ✔ (94-101). |

**Verdict: PARTIAL — core idle-disguise behavior ported; stats buffed ~3×, original prey list and spawn gating lost.**

---

### Leon (Leonopteryx)
ORIG `Leon.java` (registered "Leonopteryx", hardcoded HP 250 / ATK 55 / armor 16) | PORT has **two** entities: `entity\EntityLeon.java` ("leon") and `entity\Leonopteryx.java` ("leonopteryx")

| Feature | Status | Detail |
|---|---|---|
| Stats | PARTIAL | EntityLeon: HP 250, ATK 55, speed 0.25, xp 300, kb-res 0.8 (`EntityLeon.java:91-98`) — matches ORIG except **armor 16 MISSING**. Leonopteryx: HP 300/ATK 40/speed 0.4, xp 120 (`Leonopteryx.java:71-105`) — invented stat block. |
| AI goals | PORTED | EntityLeon: Float(0) FollowOwner(1) Tempt(2,BEEF) Stroll(3) LookAt(4) LookAround(5) / OwnerHurtBy(1) OwnerHurt(2) HurtBy(3) NearestMonster(4) (`EntityLeon.java:78-89`) ≈ ORIG Swim(0) FollowOwner(1) Tempt(2,carrot→beef) Wander(3) Watch(4) LookIdle(5) / NearestIMob(1) HurtBy(2). Riderless flight (`flyWithoutRider`, 387-551) is a faithful port incl. stuck/unstick, owner-follow flight, ownerFlying speed 1.75/3.5. |
| Rider flight | MISSING | ORIG `fly_with_rider`: full rider-controlled FLIGHT (speed up to 3.5, `flyup_keystate` vertical control). PORT: `tickRidden`/`getRiddenInput` give **ground movement only** at 1.8× walk speed (`EntityLeon.java:183-205`). Flying mount feature lost. |
| Attack/abilities | PARTIAL | 55 melee + knockback ✔ (260-268). MISSING: 4× damage vs Kraken (220), Ender-Dragon-part handling. Self-heal 1/250×2 ✔ (358). Hurt window 15 vs ORIG 10 (minor). |
| Taming | DIVERGENT | Diamond block insta-tame ✔ (584-596). 1/3 tame item: ORIG carrot → PORT **beef** (598-612). Untame item: PORT glass (628) — ORIG used different item. Leonopteryx: 1/3 tame with any `ItemTags.MEAT`, **not rideable at all**. |
| Drops | DIVERGENT | ORIG: 4–9 raw chicken, 16–21 feather, 2–7 KrakenRepellent, 1/5 MyBattleAxe. PORT EntityLeon code: 4–9 **diamond** + 16–21 **gold ingot** (`EntityLeon.java:686-696`) + `LT leon.json` bones/leather. Leonopteryx `LT leonopteryx.json`: battle_axe 100% (ORIG 20%) + kraken_repellent 1–2 (ORIG 2–7) + xp bottles. |
| Spawning | DIVERGENT | ORIG: spawner-gated only ("Leonopteryx" spawner, Y>50, no nearby Leons). PORT: leon natural-spawns jungle w1 (`BM companion_leon.json`) + Nether w2 (`add_nether_spawns.json`!); leonopteryx mountains w1 with boss bar. |
| Sounds | PORTED | EntityLeon: `leon_living`/`leon_hit`/`leon_death` + `mothrawings` flap >20 ticks ✔ (226-313). Leonopteryx: wings only, no hurt/death/living — PARTIAL. |

**Verdict: PARTIAL — split into two half-Leons: EntityLeon has the stats/sounds/taming but can't fly with a rider and drops diamonds/gold; Leonopteryx has the boss presence but isn't rideable. The signature flying-mount mechanic is missing from both.**

---

### Lizard
ORIG `Lizard.java` | PORT `entity\Lizard.java`

| Feature | Status | Detail |
|---|---|---|
| Stats | PORTED | HP 30, melee 6, speed 0.3, xp 15 both (ORIG hardcoded; PORT `Lizard.java:86-91`, `doHurtTarget`=6.0 at 107). |
| AI goals | PORTED | Float(0) FollowOwner(1) Breed(2) Tempt(3,COD≈raw fish) WanderALot(4) LookAt(5) LookAround(5) / HurtBy(1) (`Lizard.java:69-78`) — matches ORIG priorities. Fed-fish temporary buddy/follow (3000–5000 ticks) ✔ (112-123,185). Self-heal 1/300 ✔. Prey: Spider/CaveSpider/Chicken ✔; ORIG also AttackSquid — PORT omits (125-132). Cactus-immune ✔. |
| Block-seek | DIVERGENT | ORIG `scan_it` sought **water/lava**; PORT `scanForFire` seeks **LAVA/FIRE** blocks only (`Lizard.java:149-178`) — water-seeking replaced by fire-seeking. |
| Drops | PORTED | None both (ORIG `func_146068_u`=null; `LT lizard.json` empty pools). |
| Spawning | DIVERGENT | ORIG: water biomes weight 2–5/2-4 (`OreSpawnMain.java:4868-4870`). PORT: all-overworld weight 10/1-2 (`BM add_overworld_creatures.json`), `checkSpawnRules` Y≥50 (`Lizard.java:258-260`). |
| Sounds | PORTED | none / `alo_hurt` / `alo_death` ✔. |

**Verdict: PORTED with two divergences (fire- vs water-seeking, overworld-wide spawning).**

---

### LurkingTerror
ORIG `LurkingTerror.java` | PORT `entity\EntityLurkingTerror.java`

| Feature | Status | Detail |
|---|---|---|
| Stats | DIVERGENT | ORIG: HP 30, ATK 6 (melee 5), def 5, speed 0.25, xp 20 (`OreSpawnMain.java:6503`). PORT: HP 40, ATK 5, speed 0.25, xp 20 (`EntityLurkingTerror.java:42-47`). Port `MobStats.LURKING_TERROR`=150/20 (`MobStats.java:47`) unused. |
| AI | PORTED | No registered goals (matches ORIG); custom flight wander + chase in `customServerAiStep` (`EntityLurkingTerror.java:95-158`), melee at dist²<6, retaliation-flight-target on hurt ✔, stuck counter (new but harmless). |
| Targeting | PARTIAL | ORIG excluded flying mobs and a long list of OreSpawn species; PORT excludes only other LurkingTerrors (191-197). |
| Drops | DIVERGENT | ORIG: random carrot / rotten flesh / feather. PORT `LT lurking_terror.json`: 3–8 bone + 30% 1–3 diamond. |
| Spawning | PARTIAL | ORIG: spawner-gated + light + Y>10 + no nearby LTs + Islands-dim special. PORT: overworld monsters w2/1-1, no rules. |
| Sounds | PORTED | `lurkinghorror_living` / `lurkinghorror_hit` / `lurkinghorror_dead` ✔ (68-85). |

**Verdict: PORTED behavior, DIVERGENT drops/stats, spawn gating lost.**

---

### Mantis
ORIG `Mantis.java` | PORT `entity\EntityMantis.java`

| Feature | Status | Detail |
|---|---|---|
| Stats | DIVERGENT | ORIG: HP 120, ATK 16, def 10, speed 0.32, xp 100 (`OreSpawnMain.java:6467`). PORT: HP 100, ATK 12, speed 0.32, xp 100 (`EntityMantis.java:58-63`). |
| AI | PORTED | Custom flight (random target 8+rand(9) offsets, stuck>50 reset, yaw chase) ≈ ORIG `func_70619_bc` (`EntityMantis.java:119-192`); melee within 5+width/2 ✔; heal 1/100 ✔; water self-damage 1/20 ✔ (113-115); retaliation target on hurt ✔. NEW: `NearestAttackableTargetGoal<Player>` (55). |
| Targeting | PARTIAL | ORIG: players, mobs, butterflies, Cockateil, Fairy; avoided water/mantises/many species. PORT: players + Monster, excludes Mantis/Bee/in-water (239-248) — butterfly-prey behavior lost. |
| Drops | DIVERGENT | Double path. `LT mantis.json`: mantis_claw×2 ✔, xp bottle (ORIG painting), gunpowder 2–11 ✔, uranium 1–3 ✔, titanium 1–3 ✔, gold ingot 2–4 (ORIG raw beef 2–5). PLUS code (`EntityMantis.java:215-226`): name tag + 2–11 spider eye + 2–4 gold ingot — extra layer. |
| Spawning | PORTED-ish | ORIG weight 1–5/1-1..4 across biomes (`OreSpawnMain.java:4721-4729`) + spawner gate + Y>50 + clearance. PORT: w5/1-2 in forest/jungle/badlands/savanna/taiga/swamp tags (`BM hostile_mantis__*.json`); no gating checks. |
| Sounds | PORTED | `beebuzz` / `dragonfly_hurt` / `alo_death` ✔ (84-101). |

**Verdict: PORTED flight/combat; stats ~−17%, drops doubled, prey list narrowed.**

---

### MantisClaw (item)
ORIG `MantisClaw.java` (ItemSword, dmg 10, 1000 dur, regen-drain lifesteal) | PORT `item\MantisClaw.java`

| Feature | Status | Detail |
|---|---|---|
| Item | PORTED | SwordItem on `ModToolTiers.AMETHYST` (`MantisClaw.java:12`) — damage/durability come from tier, not verified equal to ORIG 10/1000 (UNVERIFIED exact numbers). |
| Lifesteal | DIVERGENT | ORIG: applies negative-regen to target + positive-regen to attacker (effect-based drain). PORT: flat 1.0 magic dmg to target + 1.0 heal per hit (`MantisClaw.java:16-23`). Mechanic simplified. |

**Verdict: PARTIAL — exists with simplified lifesteal.**

---

### Molenoid
ORIG `Molenoid.java` | PORT `entity\EntityMolenoid.java`

| Feature | Status | Detail |
|---|---|---|
| Stats | DIVERGENT | ORIG: HP 200, ATK 18, def 12, speed 0.35, xp 40 (`OreSpawnMain.java:6478`). PORT: HP 100, ATK 10, speed 0.35, xp 40 (`EntityMolenoid.java:62-68`). Halved. |
| AI goals | PORTED | Float(0) WanderALot(2) LookAt(3) LookAround(4) / HurtBy(1) (`EntityMolenoid.java:54-60`) ≈ ORIG minus MoveThroughVillage. Attack loop 1/4 with melee <16 dist² ✔ + knockback ✔ (104-118). inWall-immune ✔ (96-101). |
| Digging | PARTIAL | ORIG: places `MyMoleDirtBlock` while moving + destroys dirt/grass/sand/gravel **ahead**. PORT: `clearPathBehind()` destroys dirt/grass/gravel/sand 3-high **behind** (169-186) and `throwBlocksAtTarget` places vanilla DIRT near target (151-167). MoleDirtBlock missing; direction inverted; both mobGriefing-gated ✔. |
| Drops | DIVERGENT | Double path. ORIG: nose + painting + 10 gunpowder + 6 carrot. PORT code: nose + name tag + 10 leather + 6 bone (209-219). PLUS `LT molenoid.json`: 2nd nose + 2nd name tag + 6–10 rotten flesh + 3–6 slime. Nose drops twice. |
| Spawning | PORTED-ish | ORIG weight 2/1-2 select biomes (`OreSpawnMain.java:4741-4743`) + spawner/darkness/clearance gates. PORT: overworld w8/1-2 (`BM add_cave_spawns.json`), no gates. 4× weight. |
| Sounds | PORTED | `molenoid_living` 1/3 ✔, `molenoid_hit`, `molenoid_death` (222-246). |

**Verdict: PARTIAL — combat/digging approximated (direction flipped, custom block lost), stats halved, drops doubled.**

---

### Mothra
ORIG `Mothra.java` (extends EntityButterfly) | PORT `entity\Mothra.java` (extends EntityButterfly, multipart)

| Feature | Status | Detail |
|---|---|---|
| Stats | DIVERGENT | ORIG: HP 150, ATK 12, def 8, speed 0.35, xp 100 (`OreSpawnMain.java:6469`). PORT: HP 500, ATK 30, speed 0.35, xp 100 (`Mothra.java:68-74`). 3.3× HP. Port `MobStats.MOTHRA`=200/15 unused. |
| AI | PORTED | Flight wander (8+rand(20) offsets, stuck>50) ≈ ORIG; chase player within 25, fireball 1/3 (`Mothra.java:236-297`); heal 1/200 ✔ (173-177). NEW: 4-part multipart hitbox w/ per-part damage multipliers (47-129), boss bar, `MOTHRA_PEACEFUL` config. |
| Attack | PARTIAL | `SmallFireball` ✔ (211-221); ORIG fired `BetterFireball` on normal/hard difficulty — variant MISSING. |
| Drops | DIVERGENT | ORIG: painting, 53 gunpowder, 25 moth scale, 3 blaze rod, nether star, + **20 Moth entities spawned on death**. PORT code: nether star + 53 xp bottle + 3 emerald (300-305); `LT mothra.json`: xp bottle + 25–53 gunpowder + 15–25 moth scale + arrows (double path). 20-moth death-swarm MISSING. |
| Spawning | PORTED | Spawner-gate preserved behind `MOTHRA_REQUIRES_SPAWNER` config + no-nearby-Mothra in 64 + Y≥70/sky fallback (`Mothra.java:317-343`); biome entries: badlands w2 (`BM boss_mothra_badlands.json`), nether w2, chaos w1 ≈ ORIG `OreSpawnMain.java:4835-4836` w2/1-1. Best spawn-fidelity in slice. |
| Sounds | PORTED | `mothrawings` every 30 ticks ✔ (165-172); death `GENERIC_EXPLODE` ≈ "random.explode" ✔. |
| Misc | UNVERIFIED | Prompt mentions "Mothra flight+rider control" — no rider logic found in ORIG decompile read; none in PORT. |

**Verdict: PORTED structurally (best spawn parity in slice) but HP×3.3, BetterFireball and death-moth-swarm missing, drops doubled.**

---

### Nastysaurus
ORIG `Nastysaurus.java` | PORT `entity\Nastysaurus.java`

| Feature | Status | Detail |
|---|---|---|
| Stats | DIVERGENT | ORIG: HP 200, ATK 32, def 17, speed 0.35, xp 40 (`OreSpawnMain.java:6471`). PORT: HP 100, ATK 25, speed 0.35, xp 40 (`Nastysaurus.java:51-57`). HP halved, armor gone. |
| AI | PORTED | Float(0) DinosaurMeleeAttackGoal(1) (preset: speed 1.25, reach 4.5, cadence 5/4/5, heal 1/250, kb 8.0 — `DinosaurMeleeAttackGoal.java:35`) WanderALot(2) LookAt(3) LookAround(4) / HurtBy(1) NearestPlayer(2) (`Nastysaurus.java:39-49`). Knockback on hit ✔ (102-117). Cactus-immune ✔. |
| Drops | DIVERGENT | ORIG: 10 coal + 10 stick + 10 bone + 10 arrow. PORT code: **10 gold + 10 emerald + 10 diamond + 10 iron** (`Nastysaurus.java:134-148`) — massive buff — plus `LT nastysaurus.json` bones/gunpowder (double path). |
| Spawning | PORTED-ish | ORIG: custom-dim chunk provider w6/1-2 + spawner gate + darkness + clearance. PORT: overworld w3/1-1 + swamp w1 (`BM add_overworld_monsters.json`, `hostile_nastysaurus__*.json`). |
| Sounds | PORTED | `alo_living` 1/4 ✔ / `alo_hurt` / `alo_death` (77-95). |

**Verdict: PARTIAL — AI fine; HP halved and drop table inflated from sticks/bones to 10 diamonds+10 emeralds+10 gold per kill.**

---

### Ostrich
ORIG `Ostrich.java` | PORT `entity\Ostrich.java`

| Feature | Status | Detail |
|---|---|---|
| Stats | PORTED | HP 25, ATK 6, speed 0.38, xp 10 both (`Ostrich.java:60-65`). |
| AI goals | PORTED | Float(0) Breed(1) FollowOwner(2) Avoid-Monster(3) Tempt-wheat(4) Panic(5) LookAt(6,7) Stroll(8) LookAround(9) (`Ostrich.java:47-58`) ≈ ORIG (ORIG also avoided Players and had MoveIndoors — dropped). Heal 1/250 ✔, target clear 1/200 ✔. |
| Riding | PARTIAL | `player.startRiding` on empty hand ✔ (93-99) and AI suspends with passenger (109), but **no `tickRidden`/`getRiddenInput`/`getControllingPassenger`** — rider cannot steer. ORIG had full rider movement + jump via `flyup_keystate`. |
| Damage immunity | DIVERGENT | ORIG `func_70097_a` returned false for everything **except** cactus (cactus was its only weakness). PORT inverts: immune **to** cactus only, vulnerable to everything else (`Ostrich.java:67-71`). |
| Taming | PORTED | Wheat, 1/2 chance, full heal ✔ (77-91). |
| Drops | DIVERGENT | ORIG: tamed → 2–6 flower/sand items; else default. PORT `LT ostrich.json`: 1–3 chicken. |
| Spawning | PORTED-ish | ORIG w1/1-1 select biomes (`OreSpawnMain.java:4768-4771`) + Y>50 + no nearby. PORT: overworld w6/1-2 + `checkSpawnRules` Y≥50, sky, 1/4 roll, no nearby Ostrich (`Ostrich.java:146-153`). |
| Sounds | PORTED | none / `cryo_hurt` / `cryo_death` ✔; no fall damage ✔. |

**Verdict: PARTIAL — tame/avoid AI good, but rideability is mount-without-controls and the damage-immunity rule is inverted.**

---

### Peacock
ORIG `Peacock.java` | PORT `entity\Peacock.java`

| Feature | Status | Detail |
|---|---|---|
| Stats | PORTED | HP 15, ATK 4, speed 0.38, xp 8 (`Peacock.java:53-58`). |
| AI goals | PORTED | Float(0) Breed(1) Avoid-Monster(2) Avoid-Player(3) Panic(4) Stroll(5) LookAround(6) (`Peacock.java:43-51`) ≈ ORIG. Blink animation ported (`tick()`, 64-79). |
| Termite hunting | MISSING | ORIG `targetTasks` NearestAttackableTarget(Termite) + melee 6 — PORT has no target selector / no attack at all. |
| Egg laying | MISSING | ORIG laid `PeacockEgg` every 5000 ticks — absent in PORT. |
| Drops | PORTED | `LT peacock.json`: raw_peacock 1 + 33% 2nd + 50% feather ≈ ORIG 1–2 raw peacock + 1 feather. |
| Spawning | PARTIAL | ORIG w1/1-3 select biomes + daytime + Y 50–100 + ≤2 nearby. PORT: overworld w8/1-2 + crystal dim w5/4-8; `findBuddies()` exists (111-114) but is **never called** — all gates lost. |
| Sounds | PORTED | `peacocklive` 1/8 / `peacockhit` / `peacockdead` ✔ (86-104). |
| Breeding | DIVERGENT | ORIG wheat; PORT wheat **seeds** (`Peacock.java:117-119`). |

**Verdict: PARTIAL — passive behaviors ported; termite-hunting and egg-laying missing, spawn gates dead code.**

---

### PitchBlack (Nightmare)
ORIG `PitchBlack.java` | PORT `entity\PitchBlack.java`

| Feature | Status | Detail |
|---|---|---|
| Stats | DIVERGENT (redesign) | ORIG: continuous scale 0.5–4.0 (config `NightmareSize`); HP 250×scale, ATK 30×scale, def 10+2×scale, speed 0.2+0.1×scale, xp 200 (`OreSpawnMain.java:6517` "Nightmare" 250/30/10). PORT: 5 discrete tiers HP 125–1000, ATK 15–120, armor 10–18, xp 200–600 (`PitchBlack.java:87-91`), `NIGHTMARE_SIZE` config 0–5 (`OreSpawnConfig.java:145`). Envelope similar; ORIG max scale 4.0 → HP 1000 matches tier 5, but ORIG ATK at scale 4 = 120 ✔ — actually well matched; flagged divergent only for discretization + speed model (0.5+scale/10 flight vs 0.2+0.1×scale). |
| AI | PORTED | Ground idle (Activity 0, stroll goals) ↔ flight mode (Activity 1) ✔; flight chase + melee 5+width/2+scale ✔ (343-398); damageTicker 20 vs ORIG 25 (minor); retaliation flight target ✔; Fscale NBT migration handled (272-293). |
| Attack | PARTIAL | Melee + scaled knockback ✔ (296-313). ORIG bonus damage vs EntityDragon/Godzilla — MISSING. |
| Drops | PORTED-ish | `LT pitch_black.json`: 3–8 bone, 1–3 nightmare_scale (ORIG 1), xp bottle (ORIG painting), 1–5 zoo_keeper (ORIG 2–7) — close; ORIG random stick/feather/arrow/flesh/carrot extras dropped. |
| Spawning | MISSING | ORIG spawned in Utopia (DimensionID6) + "Nightmare" spawner gate + darkness + scale-based clearance. PORT: **no biome modifier entry, no checkSpawnRules** — only spawn egg/summon. |
| Sounds | PORTED | `pitchblack_living` 1/5 / `pitchblack_hit` / `pitchblack_dead` + `mothrawings` flaps ✔ (316-453). |

**Verdict: PARTIAL — well-engineered tier port of scaling and AI, but it never spawns naturally and lost dragon-slayer bonus damage.**

---

### Pointysaurus
ORIG `Pointysaurus.java` | PORT `entity\Pointysaurus.java` (+ `entity\ai\PointysaurusStareGoal.java`)

| Feature | Status | Detail |
|---|---|---|
| Stats | DIVERGENT | ORIG: HP 80, ATK 10, def 16, speed 0.35, xp 40 (`OreSpawnMain.java:6472`). PORT: HP 80 ✔, ATK **20**, speed 0.35, no armor (`Pointysaurus.java:62-68`). ATK doubled, armor 16 dropped. |
| AI | PORTED+ | Float(0) DinoMelee(1) (preset reach 4.0, cadence 6/5/6, heal 1/250 — `DinosaurMeleeAttackGoal.java:36`) WanderALot(2) LookAt(3) LookAround(4) / HurtBy(1) StareGoal(2) NearestPlayer(3) (`Pointysaurus.java:41-60`). Player-only targeting preserved ✔. NEW: Enderman-style stare-aggro goal not in ORIG. |
| Drops | DIVERGENT | ORIG: 10 bone + 6 carrot + 6 stick + 6 arrow. PORT code: **10 diamond + 6 beef + 6 emerald + 6 iron** (`Pointysaurus.java:150-164`) + `LT pointysaurus.json` bones (double path). |
| Spawning | PORTED-ish | ORIG: dim chunk provider 10/4-8 + Utopia 2/1-4 + spawner gates. PORT: jungle w1 + overworld w3 (`BM hostile_pointysaurus.json`, `add_overworld_monsters.json`). |
| Sounds | PORTED | `alo_living` 1/4 / `alo_hurt` / `alo_death` ✔; voice pitch 1.5 (new). |

**Verdict: PARTIAL — solid AI port with an invented stare mechanic; ATK doubled, armor lost, drops inflated to 10 diamonds.**

---

### PurplePower
ORIG `PurplePower.java` | PORT `entity\PurplePower.java`

| Feature | Status | Detail |
|---|---|---|
| Stats | PORTED | HP 1000, ATK 500, speed 0.25, armor 25, noPhysics, xp 35 (`PurplePower.java:35-51`) — exact match. |
| AI | PORTED | Goal-less float-wander + pursue + contact detonation (104-142) ≈ ORIG `func_70619_bc`; arrow-immunity ✔ (170); incoming damage capped at 10 (171) — ORIG equivalent behavior; self-expire 1/2500 with type-10 explosion 9.1 ✔ (95-100). |
| Attack | PORTED (1 div) | Type 0/10: `setHealth(h/4−1)` + hurt maxHealth/8 + explosion ✔ (148-154). Types: 1=fire ✔, 3=poison ✔, **2 = HUNGER in port vs weakness in ORIG** (159). |
| Drops | PORTED | None both. |
| Spawning | PORTED | Registered MISC (`ModEntities.java:593-595`); spawned by TheKing/TheQueen (royalty) — matches ORIG usage. |
| Sounds | PORTED | None both. |

**Verdict: PORTED — closest 1:1 match in the slice (one potion-type swap).**

---

### Rat
ORIG `Rat.java` | PORT `entity\EntityRat.java`

| Feature | Status | Detail |
|---|---|---|
| Stats | DIVERGENT | ORIG: HP 5, ATK 3, def 1, speed 0.25, xp 5 (`OreSpawnMain.java:6483`). PORT: HP 10, ATK 2 (`EntityRat.java:56-61`). Port `MobStats.RAT`=15/3 unused. |
| AI | PORTED | Float(0) Panic(1) WanderALot(3) LookAt(4) LookAround(5) / HurtBy(1) (`EntityRat.java:47-54`) ≈ ORIG (MoveThroughVillage dropped). Owner-follow >8 + teleport >16 ✔ (138-151). Heal 1/250 ✔. |
| Targeting | DIVERGENT | NEW configs `RAT_PLAYER_FRIENDLY` / `RAT_PET_FRIENDLY` **default true** (`OreSpawnConfig.java:143-144`) → wild rats never attack players or pets by default; ORIG rats attacked players (`EntityRat.java:160-183`). |
| Drops | DIVERGENT | ORIG: stick. PORT `LT rat.json`: 0–1 bone + 0–1 rotten flesh. |
| Spawning | DIVERGENT | ORIG weight 25–35 / group 2–20 in select biomes + Crystal-dim air-pocket checks + ≤8 nearby (`OreSpawnMain.java:4977-4978`). PORT: overworld-wide w20/1-3, no checks. Swarm character lost. |
| Sounds | PORTED | `ratlive` / `rathit` / `ratdead` ✔ (195-212). inWall-immune ✔. |

**Verdict: PARTIAL — core AI ported; default-passive config flips original hostility, swarm spawning (packs of up to 20) reduced to 1–3.**

---

### RedCow
ORIG `RedCow.java` | PORT `entity\RedCow.java`

| Feature | Status | Detail |
|---|---|---|
| Stats | PORTED | Vanilla cow attributes both (`RedCow.java:19-21`). |
| AI | PORTED | Cow inherit + target clear 1/200 ✔ + `removeWhenFarAway` false ✔ (32-43). |
| Drops | DIVERGENT | ORIG: super (cow) + 1–2 bonus leather-class item. PORT: `LT red_cow.json` 1–3 leather + code 1–3 **wheat** (`RedCow.java:23-30`) — wheat bonus invented. |
| Spawning | PORTED-ish | ORIG w8/4-8 and w5/2-5 select biomes (`OreSpawnMain.java:4610-4615`). PORT: overworld w6/1-2 + utopia w10/4-8 + village w8/4-8 (`BM dim_utopia_locals.json`, `dim_village_locals.json`). |
| Sounds | PORTED | Inherited cow sounds both. |

**Verdict: PORTED with minor drop divergence.**

---

### Robot1 (Bomb-Omb)
ORIG `Robot1.java` | PORT `entity\Robot1.java`

| Feature | Status | Detail |
|---|---|---|
| Stats | PORTED | HP 5, ATK 4, speed 0.2, xp 5 (`Robot1.java:63-68`) — exact (ORIG hardcoded 5/4/2; armor 2 dropped, negligible). |
| AI | PORTED | Wander + LookAt goals (55-61) ≈ ORIG; kamikaze: target scan 1/8, explode 2.5 at dist²<5 w/ 1/18 fuse roll then discard (90-123) ≈ ORIG (ORIG exploded deterministically when close — port adds fuse randomness, minor). NEW spin-acceleration animation. |
| Drops | PORTED | `LT robot_1.json` 3–8 iron nugget ≈ ORIG default nugget drop. |
| Spawning | MISSING | ORIG: Utopia `BiomeGenUtopianPlains` w25/4-8 + w5/2-8 + "Bomb-Omb" spawner gate. PORT: **no biome modifier entry** — egg/summon only. |
| Sounds | PORTED | `kyuubi_living` / `scorpion_hit` / `robot1_death` ✔ (148-161). |

**Verdict: PORTED behavior; never spawns naturally.**

---

### Robot2 (Robo-Pounder orig / "RoboWarrior" port)
ORIG `Robot2.java` | PORT `entity\Robot2.java`

| Feature | Status | Detail |
|---|---|---|
| Stats | DIVERGENT | ORIG: HP 200, ATK 22, def 18, speed 0.3, xp 100 (`OreSpawnMain.java:6495`). PORT: HP **500**, ATK **30**, armor 8 (`Robot2.java:64-70`). |
| AI | PARTIAL | Melee loop w/ cadence `nextInt(5)==0||nextInt(6)==1` ✔ (94-128); pack-alert HurtBy (61, new). **Block destruction around self/target (PlayNicely-gated) MISSING** — that signature griefing moved to port Robot4 instead. |
| Drops | DIVERGENT | ORIG: 2–9 iron **block** + 5–10 coal + large random table. PORT `LT robot_2.json`: 2–5 iron ingot + 25% 0–2 gold ingot. Massive reduction. |
| Spawning | MISSING | ORIG Utopia w16/2-8 + w2/1-4 + "Robo-Pounder" spawner. PORT: none. |
| Sounds | PORTED | `robot_living` 1/4 / `robot_hurt` / `robot_death` ✔. |

**Verdict: DIVERGENT — 2.5× HP buff, griefing behavior reassigned to Robot4, drops slashed, no natural spawns.**

---

### Robot3 (Robo-Gunner)
ORIG `Robot3.java` | PORT `entity\Robot3.java`

| Feature | Status | Detail |
|---|---|---|
| Stats | DIVERGENT | ORIG: HP 80, ATK 16, def 14, speed 0.35, xp 60 (`OreSpawnMain.java:6496`). PORT: HP **300**, ATK 20, armor 6 (`Robot3.java:62-68`). 3.75× HP. |
| AI | PORTED | 35-tick reload ✔ (`Robot3.java:99` = ORIG reload_ticker 35); LaserBall at LOS within 16 blocks (87-130). |
| Drops | DIVERGENT | ORIG: 5–10 × MyLaserBall(4) + random table. PORT `LT robot_3.json`: 3–6 iron ingot + 20% diamond. **LaserBall ammo drop missing** (item exists: `ModItems.java:335`). |
| Spawning | MISSING | ORIG Utopia w12/2-4 + w2/1-4 + spawner. PORT: none. |
| Sounds | PARTIAL | robot_living/hurt/death ✔; ORIG `fireworks.launch` on shot — PORT `fireLaserAt` plays **no sound** (121-130). |

**Verdict: PARTIAL — gunner loop faithful; HP ×3.75, ammo drops and shot sound missing, no spawns.**

---

### Robot4 (Robo-Warrior orig / "RoboPounder" port)
ORIG `Robot4.java` | PORT `entity\Robot4.java`

| Feature | Status | Detail |
|---|---|---|
| Stats | DIVERGENT | ORIG: HP 170, ATK 12 (melee 15/20/25 by difficulty), def 18, speed 0.34, xp 120 (`OreSpawnMain.java:6497`). PORT: HP **750**, ATK **40**, armor 10 (`Robot4.java:91-97`). 4.4× HP. |
| AI | PARTIAL | Melee + 2.0 knockback ✔ (208-218); wasAttackedTicker 65-tick post-hit invulnerability (250-262) ≈ ORIG shielding window. **Ranged LaserBall attack (normal+special) MISSING** — ORIG Robot4 was hybrid melee/ranged. NEW: throttled ground-pounding terrain destruction (118-206) — actually ORIG **Robot2's** griefing relocated here. Difficulty-scaled melee MISSING. |
| Shielding | PARTIAL | `DATA_SHIELDING` defined + checked in `hurt()` (252) but **no code ever calls `setShielding(1)`** — dead state. |
| Drops | DIVERGENT | ORIG: 5–14 LaserBall(4) + **MyRayGun** + painting + randoms. PORT `LT robot_4.json`: 2–5 iron + 2–5 redstone. RayGun drop missing. |
| Spawning | MISSING | ORIG Utopia w8/1-2 + w1/1-2 + spawner. PORT: none. |
| Sounds | PORTED | robot_living/hurt/death ✔; smoke particles ✔ (121-128). |

**Verdict: DIVERGENT — re-rolled into a melee bruiser: ranged attack, ray-gun drop and active shielding all missing; HP ×4.4.**

---

### Robot5 (Robo-Sniper)
ORIG `Robot5.java` | PORT `entity\Robot5.java`

| Feature | Status | Detail |
|---|---|---|
| Stats | DIVERGENT | ORIG: HP 20, ATK 5, def 6, speed 0.3, xp 20 (`OreSpawnMain.java:6498`). PORT: HP **150**, ATK 15, armor 4 (`Robot5.java:63-68`). 7.5× HP — biggest stat inflation in slice. |
| AI | PORTED | 20-tick reload ✔ (`Robot5.java:100` = ORIG), 30-block detection/firing ✔ (104, 142), plant-and-shoot ✔. |
| Drops | DIVERGENT | ORIG: 5–10 LaserBall(4) + randoms. PORT `LT robot_5.json`: 4–8 iron + 1–3 gold. Ammo drop missing. |
| Spawning | MISSING | ORIG Utopia w20/4-8 + w2/3-5 + "Robo-Sniper" spawner. PORT: none. |
| Sounds | PARTIAL | robot set ✔; `fireworks.launch` shot sound missing (`fireLaserAt` 124-133 silent). |

**Verdict: PARTIAL — sniper loop exact; HP ×7.5, ammo drops, shot sound and spawns missing.**

---

### RockBase
ORIG `RockBase.java` | PORT `entity\RockBase.java`

| Feature | Status | Detail |
|---|---|---|
| Stats | PORTED | HP = 1 + type/4 ✔ (`RockBase.java:54,107` = ORIG `:70,141`); fire-immune dropped (minor); xp 0. |
| Type lottery | PARTIAL | Overworld lottery 1→12 with identical odds ✔ (`RockBase.java:95-106` = ORIG `:96-128`). ORIG Crystal-dimension branch (forces types 9–12, ORIG `:129-140`) **MISSING** — port has single lottery. |
| Rotation lock / particles | PORTED | Yaw/pitch zeroed each tick ✔ (86-88); flame/villager/smoke/firework particles per type ✔ (115-124 = ORIG `:147-159`). |
| Drops | MISSING | ORIG `func_70645_a` (`RockBase.java:213-251`) drops the matching rock item (MySmallRock…MyCrystalTNTRock) per type on death. PORT: **no death drop at all** (no override, no `LT rock_base.json`) — picking up a placed rock destroys it. Also `ItemRock.useOn` (`item\ItemRock.java:42-54`) never calls `placeRock(rockType)`, so a placed rock re-randomizes its type. |
| Spawning | PARTIAL | ORIG `func_70601_bi` Y≥50; PORT none (MISC category, placed via ItemRock/`world\CrystalStructures.java`). |
| Sounds | PORTED-ish | ORIG `random.pop` on hit → PORT `ITEM_PICKUP` (59-65); silent otherwise ✔. |

**Verdict: PARTIAL — looks right, but death drops are entirely missing and placed rocks lose their type: the rock pickup loop is broken.**

---

### Rotator
ORIG `Rotator.java` | PORT `entity\EntityRotator.java`

| Feature | Status | Detail |
|---|---|---|
| Stats | DIVERGENT | ORIG: HP 35, ATK 10, def 8, speed 0.25, xp 35 (`OreSpawnMain.java:6499`; `Rotator.java:64-69`). PORT: HP 30, ATK 5, no armor, xp 35 (`EntityRotator.java:42-47`). Port `MobStats.ROTATOR`=80/10 unused. |
| AI | PORTED | Goal-less; flight wander 8+rand(10) offsets ✔ (122-139 = ORIG `:191-208`); circling attack — flight target offset 2.5 blocks perpendicular to victim ✔ (140-152 = ORIG `:209-215`); melee at dist²<9 ✔; daytime self-despawn 1/400 ✔ (82-87 = ORIG `:169-172`); firework particles incl. directional stream at target ✔ (62-80); arrow immunity ✔ (101-105 = ORIG `:242-247`). `noPhysics=true` (35) — ORIG had `func_145773_az` (noclip in blocks) similar. |
| Target filter | PARTIAL | ORIG excluded 16 species (Termite, Vortex, DungeonBeast, CrystalCow, Irukandji, Skate, Whale, Flounder, Urchin, TerribleTerror, LurkingTerror, CloudShark, Mothra, Bee, Mantis…). PORT excludes Rotator/Peacock/CloudShark/TerribleTerror only (168-179). |
| `was_spawnered` | MISSING | ORIG persisted when spawned from a "Rotator" spawner (`Rotator.java:255-273`); no equivalent. |
| Drops | DIVERGENT | ORIG: 1 of {CrystalPinkIngot, TigersEyeIngot, CrystalCoal block, iron ingot} (`Rotator.java:385-400`). PORT `LT rotator.json`: 2–5 iron nugget + 1–3 gunpowder. |
| Spawning | PARTIAL | ORIG: spawner gate OR (dark + 3×3×2 clearance + night) (`Rotator.java:255-288`). PORT: overworld cave-spawns w5/1-1 (`BM add_cave_spawns.json`), no rules. |
| Sounds | PORTED | `vortexlive` / `glasshit` / `glassdead` ✔ (192-208; ORIG living sound `"vortexlive"` lacked namespace — port normalizes). |

**Verdict: PORTED behavior (flight/circle/despawn all faithful); stats/drops divergent, crystal-ingot drops lost.**

---

### RubberDucky
ORIG `RubberDucky.java` | PORT `entity\EntityRubberDucky.java`

| Feature | Status | Detail |
|---|---|---|
| Stats | PORTED | HP 5 ✔ (`EntityRubberDucky.java:79-84` = ORIG `mygetMaxHealth`=5), speed 0.22 ✔, melee 1.0 (2.0 at killCount≥5) ✔ (287-290 = ORIG `:415-422`), xp 15 ✔. |
| AI goals | PORTED | Float(0) Breed(1) FollowOwner(2) Tempt(3) WanderALot(4) LookAt(5) LookAround(6) / HurtBy(1) (`:68-77` ≈ ORIG `:70-78`). Water buoyancy ✔ (122-127 = ORIG `:128-133`). Water-seek scan ✔ (238-284 = ORIG `scan_it`). KillCount decay 1/200 ✔, heal 1/300 ✔, attack loop 1/5 ✔ (204-236 = ORIG `:362-413`). |
| Vengeance respawn | PORTED | Player kill at killCount<10 → respawn replacement duck w/ inherited killCount, 20 placement tries ✔ (136-172 = ORIG `:136-169`); attacks players at killCount≥5 ✔ (292-301 = ORIG `:449-452`). |
| Targeting | PARTIAL | ORIG also hunted `EntitySquid`/`AttackSquid` (`:440-445`) — PORT only players at killCount≥5; buddy-following duck behavior (ORIG `:446-448`) dropped. |
| Taming | DIVERGENT | ORIG: raw fish 1/2 tame (`:242-272`), untame with **dead bush** (`:273-287`). PORT: **wheat** 1/2 tame (177-195), no untame item; Tempt item fish→wheat (72). |
| Drops | MISSING | ORIG `func_146068_u`: 50% feather, else 50% RubberDuckyEgg (`:223-231`). PORT `LT rubber_ducky.json` has **empty pools** and no code drops. |
| Spawning | MISSING | ORIG: water biomes w4–10 / groups 4–20 (`OreSpawnMain.java:4873-4874`) + "Rubber Ducky" spawner gate + daytime + Y≥50 (`:508-526`). PORT: **no biome modifier entry** — never spawns. |
| Sounds | PORTED | `duck_hurt` ×3, living 1/10 ✔ (339-364 = ORIG `:200-213`). |

**Verdict: PARTIAL — signature vengeance-respawn mechanic faithfully ported, but the duck drops nothing, never spawns naturally, and is tamed with wheat instead of fish.**

---

### RubyBird
ORIG `RubyBird.java` (extends Cockateil, birdtype 5) | PORT `entity\RubyBird.java` (extends Cockateil)

| Feature | Status | Detail |
|---|---|---|
| Stats | DIVERGENT | ORIG: inherits Cockateil. PORT: HP 12 / speed 0.25 override (`RubyBird.java:21-25`; port Cockateil base 2 HP / 0.33 — `Cockateil.java:48-51`). Values invented. |
| AI | PORTED | Inherits Cockateil both. |
| Drops | DIVERGENT | PORT: code 1/3 ruby (`RubyBird.java:28-33`) **plus** `LT ruby_bird.json` feather 1–2 + 33% ruby (killed-by-player) — up to two rubies; ORIG drop unverified from decompile (dungeon-loot oriented). |
| Spawning | DIVERGENT | ORIG: RubyBirdDungeon placement only (`func_70601_bi` true). PORT: crystal_plains natural spawn w6/2-4 (`BM dim_crystal_locals.json`). |
| Sounds | DIVERGENT | ORIG: `orespawn:rubybird` when not raining. PORT: inherits Cockateil `orespawn:birds` (`Cockateil.java:142-146`) — rubybird sound asset unused. |

**Verdict: PARTIAL — exists and breeds true; bespoke sound lost, drops doubled, spawn model changed.**

---

## Summary Table

| Entity | Stats | AI | Attacks | Drops | Spawning | Sounds | Overall |
|---|---|---|---|---|---|---|---|
| Kraken | DIVERGENT | PORTED | PORTED | DIVERGENT (double) | DIVERGENT | DIVERGENT | PARTIAL |
| Kyuubi | DIVERGENT | PORTED | PORTED | DIVERGENT (double) | DIVERGENT | PORTED | DIVERGENT |
| LaserBall | PORTED | PORTED | PORTED | PARTIAL | n/a | PORTED | PORTED |
| Lavafoam | n/a | n/a | PORTED | MISSING | n/a | n/a | PARTIAL |
| LeafMonster | DIVERGENT | PORTED | DIVERGENT | DIVERGENT | PARTIAL | PORTED | PARTIAL |
| Leon | PARTIAL | PORTED | PARTIAL | DIVERGENT | DIVERGENT | PORTED | PARTIAL |
| Lizard | PORTED | PORTED | PORTED | PORTED | DIVERGENT | PORTED | PORTED |
| LurkingTerror | DIVERGENT | PORTED | PORTED | DIVERGENT | PARTIAL | PORTED | PARTIAL |
| Mantis | DIVERGENT | PORTED | PORTED | DIVERGENT (double) | PORTED-ish | PORTED | PARTIAL |
| MantisClaw | UNVERIFIED | n/a | DIVERGENT | n/a | n/a | n/a | PARTIAL |
| Molenoid | DIVERGENT | PORTED | PORTED | DIVERGENT (double) | PORTED-ish | PORTED | PARTIAL |
| Mothra | DIVERGENT | PORTED | PARTIAL | DIVERGENT (double) | PORTED | PORTED | PARTIAL |
| Nastysaurus | DIVERGENT | PORTED | PORTED | DIVERGENT (double) | PORTED-ish | PORTED | PARTIAL |
| Ostrich | PORTED | PORTED | DIVERGENT | DIVERGENT | PORTED-ish | PORTED | PARTIAL |
| Peacock | PORTED | PARTIAL | MISSING | PORTED | PARTIAL | PORTED | PARTIAL |
| PitchBlack | DIVERGENT | PORTED | PARTIAL | PORTED-ish | MISSING | PORTED | PARTIAL |
| Pointysaurus | DIVERGENT | PORTED+ | PORTED | DIVERGENT (double) | PORTED-ish | PORTED | PARTIAL |
| PurplePower | PORTED | PORTED | PORTED (1 div) | PORTED | PORTED | PORTED | PORTED |
| Rat | DIVERGENT | PORTED | DIVERGENT (config) | DIVERGENT | DIVERGENT | PORTED | PARTIAL |
| RedCow | PORTED | PORTED | n/a | DIVERGENT | PORTED-ish | PORTED | PORTED |
| Robot1 | PORTED | PORTED | PORTED | PORTED | MISSING | PORTED | PARTIAL |
| Robot2 | DIVERGENT | PARTIAL | PARTIAL | DIVERGENT | MISSING | PORTED | DIVERGENT |
| Robot3 | DIVERGENT | PORTED | PORTED | DIVERGENT | MISSING | PARTIAL | PARTIAL |
| Robot4 | DIVERGENT | PARTIAL | PARTIAL | DIVERGENT | MISSING | PORTED | DIVERGENT |
| Robot5 | DIVERGENT | PORTED | PORTED | DIVERGENT | MISSING | PARTIAL | PARTIAL |
| RockBase | PORTED | PARTIAL | n/a | MISSING | PARTIAL | PORTED | PARTIAL |
| Rotator | DIVERGENT | PORTED | PORTED | DIVERGENT | PARTIAL | PORTED | PARTIAL |
| RubberDucky | PORTED | PORTED | PORTED | MISSING | MISSING | PORTED | PARTIAL |
| RubyBird | DIVERGENT | PORTED | n/a | DIVERGENT | DIVERGENT | DIVERGENT | PARTIAL |

**Overall counts (29 audited):** PORTED 4 (LaserBall, Lizard, PurplePower, RedCow) · PARTIAL 22 · DIVERGENT 3 (Kyuubi, Robot2, Robot4) · MISSING 0 (every original has a port counterpart, incl. Lavafoam block and MantisClaw item).

**Systemic issues:** (1) ~10 entities have both `dropCustomDeathLoot` AND a loot table → double drops; (2) port `MobStats.java` is dead code — all entities hardcode different values; (3) Robots 1–5, PitchBlack and RubberDucky have zero natural-spawn entries (no Utopia-dimension spawn lists ported); (4) most original `getCanSpawnHere` gates (spawner blocks, darkness, Y-bands, crowd caps) are absent.
