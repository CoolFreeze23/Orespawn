# 05 — Multi-Part Bosses Audit (1.7.10 → NeoForge 1.21.1)

Scope: TheKing (+KingHead, KingSpawnerBlock), TheQueen (+QueenHead, QueenSpawnerBlock), Godzilla/Mobzilla (+GodzillaHead), ThePrince → ThePrinceTeen → ThePrinceAdult, ThePrincess, OreSpawnPartEntity / MultiHitboxLib framework, BetterFireball/ThunderBolt/IceBall/PurplePower usage.

Paths abbreviated: `ORIG` = `reference_1_7_10_source/sources/danger/orespawn/`, `PORT` = `src/main/java/danger/orespawn/`, `RES` = `src/main/resources/`.

Original config-driven stats (`ORIG/OreSpawnMain.java`): `TheKing 7000/350/21` (:6521), `TheQueen 6000/225/21` (:6522), `Mobzilla(Godzilla) 4000/175/21` (:6514). Flags: `PlayNicely` (:1485), `FullPowerKingEnable` (:1488), `TheKingEnable` (:6434), `TheQueenEnable` (:6435).

---

## TheKing

### Stats

| Stat | Original | Port | Status |
|---|---|---|---|
| Max health | 7000 — `OreSpawnMain.java:6521`, applied `TheKing.java:104` | 6000 — `PORT/entity/TheKing.java:109,219` | **DIVERGENT** |
| Attack damage | 350 base — `OreSpawnMain.java:6521`, `ORIG/TheKing.java:106` | 250 — `TheKing.java:110,221` | **DIVERGENT** |
| Attack phase scaling | ×2/×4/×8/×16 at <2/3, <1/2, <1/4, <1/8 HP if `player_hit_count<10` — `ORIG/TheKing.java:244–254` | identical thresholds/multipliers — `PORT/entity/TheKing.java:460–472` | PORTED |
| Armor (defense) | 21 base; 25 if large entity detected; +1/+2/+3 phase bonuses — `ORIG/TheKing.java:851–865` | 12 base; same 25/+1/+2/+3 structure — `PORT/entity/TheKing.java:111,975–984` | **DIVERGENT** (base 21→12; structure ported) |
| Move speed | 0.62 — `ORIG/TheKing.java:105` | 0.62 — `TheKing.java:112,220` | PORTED |
| XP | 25000 — `ORIG/TheKing.java:91` | 25000 — `TheKing.java:146` | PORTED |
| Size | 22×24 (5.5×6 if PlayNicely) — `ORIG/TheKing.java:86–88` | 6×12 parent + 5 parts — `PORT/ModEntities.java:180–182` | **DIVERGENT** (parent hitbox much smaller; partially compensated by parts; no PlayNicely shrink) |
| Damage-in cap | 750 — `ORIG/TheKing.java:809–811` | 750 — `TheKing.java:936` | PORTED |
| Passive heal | 1/30 tick heal 5 (+200 if large foe); top-up to 2000 HP if hits<10 — `ORIG/TheKing.java:678–686` | identical — `TheKing.java:568–576` | PORTED |

### Head / part mechanics

| Item | Original | Port | Status |
|---|---|---|---|
| Head sidecar | Standalone 19.9×10 EntityLiving; teleports each tick to `(x−30·sin(yHeadRot), y+12, z+30·cos)`; speed 1.33; forwards damage to nearest TheKing via AABB; HP mirrored — `ORIG/KingHead.java:33,42,69–89,147–157` | `KingHead` retained `@Deprecated`, same offsets but uses `yBodyRot` not `yHeadRot`, registered at **3×3** size — `PORT/entity/KingHead.java:61–63,107–111`, `ModEntities.java:581–583`; still spawned by AI (`TheKing.java:765–772`) | PARTIAL (offset basis yHeadRot→yBodyRot; hitbox 19.9×10 → 3×3) |
| PartEntity system | n/a (single AABB + sidecar) | 5 parts body/head/wingL/wingR/tail, offsets head (0,11,−5) etc. — `TheKing.java:428–432`; damage mult head 1.0, body 0.5, others 0.25+1.0 — `TheKing.java:376–390`; engine-level via `OreSpawnPartEntity` (`PORT/entity/OreSpawnPartEntity.java`) | PORTED (new mechanism, intended upgrade) |

### Attack state machine

| Attack | Original | Port | Status |
|---|---|---|---|
| Melee | `attdam` + knockback ks=3.3, up 0.25+rand; ×10 dmg + half-HP for entities >30 area; EnderDragon part hits — `ORIG/TheKing.java:270–291` | mirrored incl. big-entity rule and dragon special — `TheKing.java` doHurtTarget (~860–930); FULL_POWER 2× added (`:893–896`) | PORTED (with added FULL_POWER multiplier — see config) |
| Fireball stream (`firecanon`) | ammo 10, refill `ticker%80` — `ORIG/TheKing.java:473–474,695–723`; BetterFireball really-big + 6 spread | refill `%80`→10 — `TheKing.java:557,988–1028` | PORTED |
| Lightning stream (`firecanonl`) | ammo 5, refill `%90` — `:476–477,726–753` (3 ThunderBolts) | `%90`→5 — `TheKing.java:558,1029–1057` | PORTED |
| Ice stream (`firecanoni`) | ammo 8, refill `%70` — `:479–480,757–785` (IceBall) | `%70`→8 — `TheKing.java:559,1058+` | PORTED |
| Area damage | `doJumpDamage` r15 atk/4 near + forward r15 atk/2 knock — `ORIG/TheKing.java:1071–1080` (half magic/half fall dmg) | same structure — `TheKing.java:805–817,1088–1113` | PORTED (absolute dmg lower b/c base attack 350→250) |
| End-game dialogue (isEnd=1) | scripted, endCounter→500 then "Prepare to die!", isEnd=2 — `ORIG/TheKing.java:364–447` | `KingEndGameGoal` + `aiStepEndGameDialogue`, 500-tick — `TheKing.java:592–660`, `PORT/entity/ai/KingEndGameGoal.java` | PORTED |
| Enraged (isEnd=2) | ammo maxed, hit-cooldown 10, guard off, PurplePower trail when attacking — `ORIG/TheKing.java:449–461,657–666` | identical overrides — `TheKing.java:535–544,845–860` | PORTED |
| Anti-cheese | home leash 120 (`tooFarFromHome`), revenge-target jump to attacker ≤Y230, hurt-cooldown 20, cactus/inWall immune, explosion behavior, no fall dmg, lightning immune | mirrored — `TheKing.java:932–971,1227+` | PORTED |
| Target rules | players (not creative), Horse, Monster, EnderDragon, isEnd-2 player priority, royalty excluded, 144 home radius — `ORIG/TheKing.java:939–1000` | mirrored — `TheKing.java:1118–1173` | PORTED |
| PlayNicely gate | `findSomethingToAttack` returns null when `PlayNicely!=0` (`ORIG/TheKing.java:985`); size shrink (`:85–88`) | `DATA_PLAY_NICELY` synced (`TheKing.java:528`) but **never consumed** — no targeting gate, no scale (renderer `SCALE=1.0F`, `PORT/entity/client/TheKingRenderer.java:45`) | **PARTIAL** (flag wired, zero behavioral effect) |

### Drops

| Original | Port | Status |
|---|---|---|
| Spawn ThePrince at y+10; Royal armor set + Royal sword; 150 random registry items + 150 random blocks — `ORIG/TheKing.java:183–227` | Code: identical (prince, 5 royal pieces, 150+150 random) — `PORT/entity/TheKing.java:1305–1340`. **Plus** loot table adds royal set again + royal_guardian_sword + prince_egg + 30–80 diamond + 20–50 gold + 20–50 iron — `RES/data/orespawn/loot_table/entities/the_king.json:1–41` | **DIVERGENT** (double-drop: loot table duplicates the hardcoded royal set; diamond/gold/iron and prince_egg not in original) |

### Spawner (KingSpawnerBlock)

| Original | Port | Status |
|---|---|---|
| On placement schedules block update in 100 ticks (`ORIG/KingSpawnerBlock.java:51–56`); spawn gated by `TheKingEnable` (`:66`); spawns at **y+8** with `setGuardMode(1)` (`:67,81–89`); clears self + block above; firework particles (`:43–48`) | `BossSpawnerBlock` generic: spawns on **randomTick** (unbounded delay), at **y+1**, `MobSpawnType.EVENT`, no guard mode, no enable gate — `PORT/block/BossSpawnerBlock.java:44–57`, registered `PORT/ModBlocks.java:152–154`; particles ported (`:60–69`) | **DIVERGENT** (spawn height y+8→y+1; guardMode(1) missing → no home-leash anchoring; 100-tick fuse → random tick; TheKingEnable gate missing) |

### Sounds / Boss bar / Animations

| Item | Original | Port | Status |
|---|---|---|---|
| Sounds | `orespawn:king_living` / `king_hit` / `trex_death` — `ORIG/TheKing.java:151–161` | `ModSounds.KING_LIVING/KING_HIT/TREX_DEATH` — `PORT/entity/TheKing.java:281–293` | PORTED |
| Boss bar | none in 1.7.10 (no IBossDisplayData) | `ServerBossEvent` RED, add/remove on seen — `TheKing.java:114–118` + start/stopSeenByPlayer | PORTED (additive, OK) |
| Animations | procedural vanilla model (RenderTheKing) | vanilla `ModelTheKing` + wing translucency pass, `DATA_ATTACKING`/`DATA_IS_END` synced for pose — `TheKingRenderer.java`, `OreSpawnClient.java:71,235` | PORTED (equivalent pipeline; no GeckoLib needed) |

**Verdict TheKing: PARTIAL/DIVERGENT** — behavior machine faithfully ported, but all three core stats nerfed (7000/350/21 → 6000/250/12), loot double-dips, PlayNicely is a no-op, spawner block loses guard-mode and height.

---

## TheQueen

### Stats

| Stat | Original | Port | Status |
|---|---|---|---|
| Max health | 6000 — `OreSpawnMain.java:6522`, `ORIG/TheQueen.java:97,177` | 6000 — `PORT/entity/TheQueen.java:155,255` | PORTED |
| Attack | 225 — `OreSpawnMain.java:6522`, `ORIG/TheQueen.java:99` | 200 — `TheQueen.java:157,257` | **DIVERGENT** |
| Attack phase scaling | ×20/×100/×500/×1000 at <3/4, <1/2, <1/3, <1/4 HP — `ORIG/TheQueen.java:221–231` | identical — `TheQueen.java:451–462` | PORTED |
| Armor | 21 base, +2/+3/+5 phase scaling — `ORIG/TheQueen.java:817–828` | flat 10, **no scaling override** — `TheQueen.java:158,259` | **DIVERGENT** (21→10, scaling missing) |
| Speed | 0.62 — `ORIG/TheQueen.java:98` | 0.62 — `TheQueen.java:156` | PORTED |
| XP | 25000 — `ORIG/TheQueen.java:84` | 25000 — `TheQueen.java:195` | PORTED |
| Size | 22×24 (5.5×6 PlayNicely) — `ORIG/TheQueen.java:79–81` | 16×12 + MHLib parts — `ModEntities.java:184–186`, `TheQueen.java:425` | **DIVERGENT** (no PlayNicely shrink) |
| Damage-in cap / heal | cap 750; 1/32 heal 5 (+50 if hits<10); 2000-floor — `ORIG/TheQueen.java:673+` | identical — `TheQueen.java:551,747–755` | PORTED |

### Head / part mechanics

| Item | Original | Port | Status |
|---|---|---|---|
| Head sidecar | 19.9×10; teleport `(x−30·sin(yHeadRot), y+12, z+30·cos)`; speed 1.33; AABB damage-forward (`ORIG/QueenHead.java`, same pattern as KingHead :33,42,147–157) | retained `@Deprecated` 2×2 entity, yBodyRot basis — `PORT/entity/QueenHead.java:92–99`, `ModEntities.java:585–587`; spawned only when `mood==1` — `TheQueen.java:971–978` (orig same gate `ORIG/TheQueen.java:552–554`) | PARTIAL (size 19.9×10→2×2; rotation basis changed) |
| MHLib multi-hitbox | n/a | 10 bone-tracked parts: 3 heads ×1.0 dmg, body/legs ×0.5, wings/tail ×0.25; parent AABB cannot be hit (`canReceiveDamage:false`) — `RES/data/orespawn/multihitboxlib/hitbox_profiles/the_queen.json:48–174` | PORTED (new mechanism; damage multipliers consistent with King's part scheme) |

### Attack state machine

| Attack | Original | Port | Status |
|---|---|---|---|
| Melee | contact <900 distSq, anti-heal tracker, big-entity ¾-HP rule, knockback 2.75/0.2 — `ORIG/TheQueen.java:255–295,618–623` | same values, but melee deferred 8–16 ticks for GeckoLib windup (`bite`/`tail`/`roar`) — `TheQueen.java:485–526,1032–1045,690–699` | PORTED (added windup delay, minor timing divergence) |
| Area damage | r15 atk/4 self (1/2 chance), r15 atk/2 forward+knock (1/3) — `ORIG/TheQueen.java:618–628` | identical — `TheQueen.java:1020–1052` | PORTED |
| Fireball stream | ammo 10 refill `%60`; 1 really-big + 6 spread — `ORIG/TheQueen.java:458–459,629–643` | identical — `TheQueen.java:726–728,1129–1163` | PORTED |
| Lightning stream | ammo 6 refill `%70`; 3 ThunderBolts — `:461–462,644–655` | identical — `TheQueen.java:729–731,1165–1187` | PORTED |
| Mood system | mad on hurt; happy reset 1/500 at full HP; `always_mad` NBT; happy = +10 attack_level/tick — `ORIG/TheQueen.java:445–449,775` | identical — `TheQueen.java:557,712–720`, `MeanMode` NBT `:1315` | PORTED |
| Mood discharge (attackLevel>1000) | mad: 15/45 PurplePower bombs; happy: 25 soil/flower transforms (mobGriefing-gated) + 10 Butterfly **or Bird** — `ORIG/TheQueen.java:355,430` | mad 15/45 PurplePower; happy transforms + **10 butterflies only** — `TheQueen.java:787–878`, `QueenMoodGoal` | PARTIAL (Bird spawn variant dropped) |
| Follow-King-when-happy | within 64×32×64 — orig same block | `TheQueen.java:936–945` | PORTED |
| Anti-cheese | home leash 120/144, revenge jump, hurt timer 20, explosion heals half, kills small monsters that hit her, cactus/inWall immune | mirrored — `TheQueen.java:529–601` | PORTED |
| **Dormant wake-up phase** | **does not exist** in 1.7.10 | First hit deals 0 dmg, starts 60-tick invulnerable `idle_to_attack` transition; dormant blue → aggro red — `TheQueen.java:129–135,538–546` | **DIVERGENT** (added mechanic; absorbs damage the original would take) |
| PlayNicely | size shrink + behavior gates (`ORIG/TheQueen.java:79–81`) | synced only, never consumed — `TheQueen.java:734` | **PARTIAL** (no-op) |

### Drops

| Original | Port | Status |
|---|---|---|
| Royal sword ×1, PrinceEgg ×1, spawn ThePrincess at y+10, then 56× {QueenScale, **beef, bone, rotten flesh**} — `ORIG/TheQueen.java:190–199` | Code: QueenScale+PrinceEgg+Princess + 56× {QueenScale, **XP bottle, golden apple, NETHER STAR**} — `PORT/entity/TheQueen.java:405–421`. Plus loot table: royal_guardian_sword, prince_egg, 30–56 queen_scale, 10–30 diamond/string/bone — `RES/.../the_queen.json` | **DIVERGENT** (56 nether stars + 56 golden apples + 56 XP bottles replace junk drops = enormous buff; loot double-dips with json table; royal sword moved to loot table) |

### Spawner (QueenSpawnerBlock)

Same pattern as King: orig 100-tick fuse, y+8, `TheQueenEnable` gate, `setGuardMode(1)` — `ORIG/QueenSpawnerBlock.java:55,66–67,81–89`. Port `QUEEN_SPAWNER` uses generic `BossSpawnerBlock` (randomTick, y+1, no guard, no gate) — `ModBlocks.java:155–157`. **DIVERGENT** (same four deviations as King spawner).

### Sounds / Boss bar / Animations

| Item | Original | Port | Status |
|---|---|---|---|
| Sounds | `king_living` / `king_hit` / `trex_death` — `ORIG/TheQueen.java:158–167`; Mothra wing flap every 30 ticks | identical + `MOTHRAWINGS1` flap `>30` — `TheQueen.java:337–354,439–445` | PORTED |
| Boss bar | none orig | `ServerBossEvent` PINK — `TheQueen.java:160–161,609–619` | PORTED (additive) |
| Animations | procedural model | GeckoLib: 8 states in `RES/assets/orespawn/animations/entity/the_queen.animation.json` (idle :4, idle_to_attack :8300, attack :16203, bite :24499, tail_whip_right :26756, tail_whip_left :31026, roar :35296, death :43592); server triggers via `triggerAnim` + synced `IS_AWAKE`/`TRANSITION_TICKS` — `TheQueen.java:1365–1391` | PORTED (every melee variant + death has state & server-side trigger; ranged attacks have no dedicated anim, same as original) |

**Verdict TheQueen: PARTIAL/DIVERGENT** — combat loop and mood system faithfully ported with the best part-tracking in the mod, but attack 225→200, armor 21→10 (scaling dropped), drops massively buffed + duplicated, and an invulnerable wake-up phase added that did not exist.

---

## Godzilla (Mobzilla)

### Stats

| Stat | Original | Port | Status |
|---|---|---|---|
| Max health | 4000 — `OreSpawnMain.java:6514`, `ORIG/Godzilla.java:93,141` | 6000 — `PORT/entity/Godzilla.java:62,113` | **DIVERGENT** |
| Attack | 175 — `OreSpawnMain.java:6514`, `ORIG/Godzilla.java:95` | 150 — `Godzilla.java:115` | **DIVERGENT** |
| Armor | 21 via `func_70658_aO` — `ORIG/Godzilla.java:145` | **no ARMOR attribute, no override** — `Godzilla.java:111–119` | **MISSING** |
| Speed | 0.75 — `ORIG/Godzilla.java:58,94` | 0.75 — `Godzilla.java:75,114` | PORTED |
| XP | 10000 — `ORIG/Godzilla.java:77` | 10000 — `Godzilla.java:92` | PORTED |
| Size | 9.9×25 (2.475×6.25 PlayNicely) — `ORIG/Godzilla.java:72–74` | 10×25, no PlayNicely variant — `ModEntities.java:158–160` | PORTED (PlayNicely shrink missing) |
| Damage-in | cap 750; big-attacker ÷10 + 50-tick cd — orig equivalent | `Godzilla.java:65,737–765` | PORTED |

### Head / part mechanics

| Item | Original | Port | Status |
|---|---|---|---|
| Head sidecar | 9.9×10; teleport `(x−17·sin(yHeadRot), y+16, z+17·cos)`; dmg-forward AABB — `ORIG/GodzillaHead.java:33,147–157` | retained `@Deprecated`, same 17/16 offsets (yBodyRot), registered **3×3** — `PORT/entity/GodzillaHead.java:96–118`, `ModEntities.java:589–591`; still spawned — `Godzilla.java:642–649` | PARTIAL (9.9×10→3×3) |
| Parts | n/a | 4 parts bodyLow 8×8 / bodyUp 6×6 / head 5×5 / tail 4×4; offsets (0,2,0)(0,12,0)(0,20,−6)(0,4,10); dmg head 1.0 / body 0.5 / else 0.25+1 — `Godzilla.java:95–99,196–227` | PORTED |

### Attack state machine

| Attack | Original | Port | Status |
|---|---|---|---|
| Jump-landing AoE | r10 atk(175), r15 atk/2, r25 atk/4, ×1.5 if hard fall — `ORIG/Godzilla.java:292–305` | r10 **150**, r15 75, r25 37.5, ×1.5 — `Godzilla.java:594–603` | PORTED structure / DIVERGENT base (follows the 175→150 nerf) |
| Jump-at-target | 1/(20−5·large) chance, 30-tick cd — `:380` | identical — `Godzilla.java:658–661` | PORTED |
| Lightning attack | 1/65 at distSq>300: 100 dmg + ignite 5 s + explosion + 2 bolts — `:378,816–818` | identical — `Godzilla.java:489–507,655–656` | PORTED |
| Fire cannon | ammo 8 refill `%100`; really-big + 5 spread fireballs from (yHead, +19, 22 fwd) — `:64,278,706–726` | identical — `Godzilla.java:80,585,445–485` | PORTED |
| Block crushing | every tick, 2 zones (self + 16 fwd), xzrange 14/10, mobGriefing-gated, protect-list — orig crush logic | throttled to every 4th tick, same ranges and protect-list incl. mod blocks — `Godzilla.java:377–418,605–626` (documented perf change) | PORTED (intentional 4× throttle = minor timing divergence) |
| Villager priority targeting | villagers first, skips creeper/zombie/spider/skeleton, peer-boss truce | identical — `Godzilla.java:511–552` | PORTED |
| Melee | big-entity half-HP + atk×10; dragon special 75; knockback 3.2/0.3(×2 player) — orig `:270 ff` analog | `Godzilla.java:704–733` | PORTED |
| Spawn rules | night, sky, y≥50, 16×10×16 air pocket, 1/40 roll, global one-spawn flag — orig spawn check `:570` | identical + persisted `MobzillaSpawnTracker` behind `MOBZILLA_SINGLE_SPAWN` config — `Godzilla.java:140–170,579–581` | PORTED |
| Heal | 1/35 heal 5 — `:417` | identical — `Godzilla.java:697–699` | PORTED |

### Drops

| Original | Port | Status |
|---|---|---|
| **Painting** ×1; 50–79 GodzillaScale; 100–259 **beef**; 50–109 **bone**; 25–39 rolls of d80 gear table (≈75 enchanted-gear cases) — `ORIG/Godzilla.java:820–838+` | Code: **Nether Star** ×1; 50–79 scales; 100–259 **emeralds**; 50–109 **XP bottles**; 25–39 d80 rolls incl. mod gear — `PORT/entity/Godzilla.java:769–877`. Loot table additionally drops saddle, 50–79 scales again, 100–259 beef, 50–109 bone, 25–39 gear rolls — `RES/.../godzilla.json` | **DIVERGENT** (painting→nether star, beef→emeralds, bone→XP bottles; AND full double-drop: code+json each give a complete loot set) |

### Sounds / Boss bar / Animations

| Item | Original | Port | Status |
|---|---|---|---|
| Sounds | `orespawn:godzilla_living` (1/5) / `alo_hurt` / `godzilla_death` — `ORIG/Godzilla.java:178–188` | vanilla `ENDER_DRAGON_GROWL` (1/5) / `ENDER_DRAGON_HURT` / `ENDER_DRAGON_DEATH` — `Godzilla.java:261–276` | **DIVERGENT** (custom sounds replaced with vanilla dragon) |
| Boss bar | none orig | `ServerBossEvent` PURPLE "Mobzilla" — `Godzilla.java:71–72,557–566` | PORTED (additive) |
| Animations | procedural | vanilla `ModelGodzilla`, `DATA_ATTACKING` synced — `OreSpawnClient.java:69,233` | PORTED |

**Verdict Godzilla: PARTIAL/DIVERGENT** — AI ported with high fidelity, but HP buffed 4000→6000, attack nerfed 175→150, armor 21 entirely missing, custom sounds dropped, loot doubled and re-themed.

---

## ThePrince (baby)

| Feature | Original | Port | Status |
|---|---|---|---|
| Health/attack/armor/speed/XP | 500 / 10 / 16 / 0.32 / 50 — `ORIG/ThePrince.java:185–187,389–391,346–348,94,99–102` | 500 / 10 / 16 / 0.32 / 50 — `PORT/entity/ThePrince.java:91–98,70` | PORTED |
| Size | 0.75×1.25 — `:80` | 0.75×1.25 — `ModEntities.java:460–462` | PORTED |
| Auto-tame nearest player ≤10 blocks + full heal | `ORIG/ThePrince.java:522–528` | `ThePrince.java:199–206` | PORTED |
| Diamond-block: tame + counters→1000 + okToGrow | `:195–206` | `:270–282` | PORTED |
| Feeding | any food heals **healAmount×10**, ++fedCount — `:215–224` | flat 20 HP, ++fedCount — `:306–318` | **DIVERGENT** |
| Fire toggle | flint&steel → fire ON (`:250–258`), ice block → fire OFF (`:233–241`), chat messages | absent (DATA_FIRE exists but no interaction sets it) | **MISSING** |
| Grow trigger item | **diamond** when ok_to_grow — `:267–278` | **gold ingot** — `:297–303`; cake added to max counters `:285–295` (not in orig) | **DIVERGENT** |
| Growth condition | `kill>25 && fed>10 && day>10` (no okToGrow gate) — `:556` | same + `okToGrow != 0` gate — `:230` | PARTIAL (extra gate means natural growth can never trigger without diamond-block/cake) |
| Ranged attacks | fireball / ThunderBolt / IceBall canons at 5–12 block range when fire enabled — `:634–663,782–853` | **none** | **MISSING** |
| Flight movement | flying wander/owner-follow `do_movement` incl. owner-flying speedups — `:585–725` | ground `MyEntityAIWander` — `:82` | **MISSING** |
| Target list | Monsters **and** Mothra/Butterfly/Cockateil/Dragonfly/Mosquito — `:746–761`; PlayNicely gate `:765` | Monsters only; insects/Mothra explicitly excluded; no PlayNicely — `:247–254` | **DIVERGENT** |
| Drops | 1–4 **beef** — `:354–361` | 1–4 **diamond** — `RES/.../the_prince.json` | **DIVERGENT** |
| Sounds | roar (only when attacking) / duck_hurt / cryo_death, vol 0.6 — `:324–344` | identical — `:333–353` | PORTED |
| NBT | SpyroKill/Fed/Day — `:121–134` | + SpyroActivity/Fire/Grow — `:366–385` | PORTED |

**Verdict ThePrince: PARTIAL** — stats and taming chain solid; ranged attacks, flight, and fire-toggle interactions missing; growth/feed items changed.

## ThePrinceTeen

| Feature | Original | Port | Status |
|---|---|---|---|
| Health | 1500 — `ORIG/ThePrinceTeen.java:229–231` | 1000 — `PORT/entity/ThePrinceTeen.java:90` | **DIVERGENT** |
| Attack | 50 | 50 — `:92,144` | PORTED |
| Armor | 18 — `:252–254` | **none** (no ARMOR attribute) — `:88–95` | **MISSING** |
| Speed | 0.32 — `:87` | 0.35 — `:58,91` | **DIVERGENT** |
| XP | 300 — `:105` | 500 — `:68` | **DIVERGENT** |
| Size | 3.25×4.25 — `:103` | 2×3 — `ModEntities.java:472–474` | **DIVERGENT** |
| Growth | `kill>25 && day>10` → Adult, owner carries — `:406` | identical — `:178–181,201–211` | PORTED |
| Rideable | saddle-free mount — `:1157` | **no riding** | **MISSING** |
| Ranged fire/lightning/ice canons | present (same trio pattern) | **none** | **MISSING** |
| Regression item | n/a (orig: no shrink-back) | gold ingot → back to baby — `:240–254` | DIVERGENT (added) |
| Drops | PrinceEgg ×1 — `:317–319` | prince_egg ×1 — `RES/.../the_prince_teen.json` | PORTED |
| Sounds | roar / alo_hurt / alo_death — `:274–285` | identical — `:265–267` | PORTED |

**Verdict ThePrinceTeen: PARTIAL** — growth chain and drops correct; HP/speed/XP/size divergent, armor, riding and projectiles missing.

## ThePrinceAdult

| Feature | Original | Port | Status |
|---|---|---|---|
| Health/attack/speed/XP | 3000 / 100 / 0.36 / 3000 — `ORIG/ThePrinceAdult.java:225–227, 86, 102` | identical — `PORT/entity/ThePrinceAdult.java:88–90,66` | PORTED |
| Armor | 20 — `:248–250` | **none** — `:86–93` | **MISSING** |
| Size | 6.25×10.25 — `:100` | 4×6 — `ModEntities.java:464–466` | **DIVERGENT** |
| Transform→TheKing | gated `activity==0 && no rider && !peaceful && tamed && FullPowerKingEnable!=0`, growcounter>288000 — `:400–404` | gated `isTame && !hardcore`, growcounter>288000; **no FullPowerKingEnable check** — `:176–182,220–227` | **DIVERGENT** (config gate dropped; flag repurposed as King damage ×2 — `TheKing.java:893–896`) |
| Cake → instant grow | growcounter=288000 — `:1118` | identical — `:234–241` | PORTED |
| Rideable | mount — `:1134` | **no riding** | **MISSING** |
| Ranged canons | present | **none** | **MISSING** |
| Drops | PrinceEgg ×1 — `:313–315` | 5–15 diamond + 3–8 gold — `RES/.../the_prince_adult.json` | **DIVERGENT** (prince egg lost) |
| Sounds | **king_living / king_hit / trex_death** — `:270–281` | roar / alo_hurt / alo_death — `:272–287` | **DIVERGENT** |
| NBT growcounter | `ThePrinceAdultGrow` — `:1318` | `PrinceGrow` — `:302` | PARTIAL (old saves lose grow progress) |

**Verdict ThePrinceAdult: PARTIAL/DIVERGENT.**

## ThePrincess

| Feature | Original | Port | Status |
|---|---|---|---|
| Health | 400 — `ORIG/ThePrincess.java:194–196` | 500 — `PORT/entity/ThePrincess.java:85` | **DIVERGENT** |
| Attack | 9 — `:377–379` | 10 — `:87` | **DIVERGENT** |
| Armor | 14 — `:334–336` | 16 — `:88` | **DIVERGENT** |
| Speed | 0.32 (overrides 0.3 field) — `:62,81` | 0.3 — `:52,86` | **DIVERGENT** |
| XP / size | 50 / 0.75×1.25 — `:94,80` | identical — `:62`, `ModEntities.java:468–470` | PORTED |
| Taming | auto-tame ≤10 blocks (`:525–526`); diamond block (`:204–206`); food heals ×10 (`:224–226`) | auto-tame + diamond block ported (`:141–148,181–195`); food heals flat 20 (`:197–201`) | PARTIAL |
| Breeding | `func_90011_a` returns null (no breeding) — `:373–375` | `getBreedOffspring` null — `:220` | PORTED |
| Ranged canons | fire/lightning/ice trio — `:730–748,863–909` | **none** | **MISSING** |
| Flight | flying do_movement | ground wander | **MISSING** |
| PlayNicely target gate | `:846` | absent | MISSING |
| Drops | 1–4 beef — `:342–349` | 1–4 diamond — `RES/.../the_princess.json` | **DIVERGENT** |
| Sounds | roar / duck_hurt / cryo_death — `:319–328` | identical — `:211–216` | PORTED |
| Spawned on Queen death | `ORIG/TheQueen.java:193` | `PORT/entity/TheQueen.java:410–414` | PORTED |

**Verdict ThePrincess: PARTIAL** — all four core stats off by small amounts, projectiles/flight missing.

---

## Framework / Registration / Config cross-checks

| Item | Evidence | Status |
|---|---|---|
| `OreSpawnPartEntity` | forwards `hurt` to parent via `MultipartBoss.hurtFromPart`, non-collidable, pickable-when-alive, not saved, `is()` identity unification — `PORT/entity/OreSpawnPartEntity.java` | PORTED (sound replacement for sidecars) |
| ModEntities registration | godzilla :158, the_king :180, the_queen :184, the_prince :460, the_prince_adult :464, the_princess :468, the_prince_teen :472, king_head :581, queen_head :585, godzilla_head :589 — `PORT/ModEntities.java` | PORTED |
| Renderers | GODZILLA :69, THE_KING :71, THE_QUEEN :72 (GeckoLib QueenRenderer), princes :137–140, heads :159–161 — `PORT/OreSpawnClient.java` | PORTED |
| Config flags | `PLAY_NICELY` `OreSpawnConfig.java:156` (synced, unused — see King/Queen); `FULL_POWER_KING_ENABLE` :159 (repurposed); `MOBZILLA_SINGLE_SPAWN` :128 (new, replaces `godzilla_has_spawned`); **no `TheKingEnable`/`TheQueenEnable` equivalents** | PARTIAL |
| MultiHitboxLib vendored | `de.dertoaster.multihitboxlib` present; only `the_queen.json` profile among bosses — `RES/data/orespawn/multihitboxlib/hitbox_profiles/` | PARTIAL (King/Godzilla still use manual part positioning, not bone-tracked) |
| BetterFireball / ThunderBolt / IceBall / PurplePower | used by King/Queen/Godzilla port code identically to original call sites | PORTED (verified at call sites; projectile internals out of slice) |

---

## Summary table

| Boss | Stats | Head/parts | Attacks | Drops | Spawner | Sounds | Anim/Boss bar | Overall |
|---|---|---|---|---|---|---|---|---|
| TheKing | DIVERGENT (3 nerfs) | PARTIAL | PORTED (PlayNicely PARTIAL) | DIVERGENT (double) | DIVERGENT | PORTED | PORTED | **PARTIAL/DIVERGENT** |
| TheQueen | DIVERGENT (atk, armor) | PORTED (MHLib) / sidecar PARTIAL | PORTED + added wake-phase DIVERGENT | DIVERGENT (buffed + double) | DIVERGENT | PORTED | PORTED | **PARTIAL/DIVERGENT** |
| Godzilla | DIVERGENT (HP+, atk−, armor MISSING) | PORTED / sidecar PARTIAL | PORTED | DIVERGENT (double + re-themed) | n/a | DIVERGENT | PORTED | **PARTIAL/DIVERGENT** |
| ThePrince | PORTED | n/a | MISSING (ranged+flight) | DIVERGENT | n/a | PORTED | PORTED | **PARTIAL** |
| ThePrinceTeen | DIVERGENT (4 stats) | n/a | MISSING (ranged+riding) | PORTED | n/a | PORTED | PORTED | **PARTIAL** |
| ThePrinceAdult | PARTIAL (armor MISSING) | n/a | MISSING (ranged+riding); transform gate DIVERGENT | DIVERGENT | n/a | DIVERGENT | PORTED | **PARTIAL/DIVERGENT** |
| ThePrincess | DIVERGENT (4 stats) | n/a | MISSING (ranged+flight) | DIVERGENT | n/a | PORTED | PORTED | **PARTIAL** |

Sub-feature status tally: **PORTED 58 · PARTIAL 12 · DIVERGENT 27 · MISSING 13 · UNVERIFIED 0** (110 audited line items).
