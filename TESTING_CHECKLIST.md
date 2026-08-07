# TESTING_CHECKLIST — in-game manual test session (2026-07-03)

Every "Pending manual tests" item from FIX_LOG.md (master list + C7 + C8 + D2 + D3 + D4
sections), regrouped by test environment for one efficient play session.

**Marking:** `[ ]` open → `[PASS]` / `[FAIL]` as results come in. FAILs get logged at the
bottom with triage; fixes are batched after the session (nothing applied mid-session
unless it blocks testing).

**Client:** `gradlew runClient` running, launched clean — no startup crash (finding #1
does not exist). Config file for flag tests: `runs/client/config/orespawn-common.toml`.

---

## Session setup (once)

Create/open a **creative superflat** world (default preset is fine), then:

```
/gamemode creative
/difficulty easy          (peaceful blocks most hostile tests; Alien poison check NEEDS easy)
/time set day
/gamerule doDaylightCycle false
/gamerule mobGriefing true    (Princess bloom, Duplicator Log need it)
/weather clear
```

Useful everywhere:

- Kill arena leftovers: `/kill @e[type=!minecraft:player]`
- One-hit tool for drop tests: `/give @s orespawn:royal_guardian_sword` (player-kill loot
triggers fine from creative; re-verify in survival only if a drop FAILs)
- Dimension hops: `/execute in orespawn:mining run tp @s 0 100 0` (also `utopia`,
`crystal`, `village`, `islands`, `chaos`; overworld = `minecraft:overworld`)

---

## A. Creative flatworld

### A1 — Stability

- **BUG-003** — `/summon orespawn:rat`, walk >130 blocks away. Expect: no server crash on rat AI tick, rat despawns normally.
- **BUG-004** — Tame a Prince (diamond block), give diamond to grow baby→teen→adult, then quit-to-title and rejoin mid-transformation window. Expect: transformation completes, still tamed to you, no NPE in the log.

### A2 — Items & blocks

- **ITEM-011/012** — `/give @s orespawn:pizza_item` (place), `/give @s orespawn:duct_tape_item`. Expect: LEFT-click pizza eats a slice; LEFT-click with duct tape repairs held damaged gear.
- **ITEM-013/014** — `/setblock ~ ~ ~2 orespawn:block_teleport` then step on it; `/setblock ~ ~ ~2 orespawn:mole_dirt` and walk on. Expect: RTP random-teleports you; mole dirt sinks your feet slightly.
- **ITEM-016 + C8 furnace** — `/setblock ~ ~ ~2 orespawn:crystal_furnace`; cook with crystal coal, then burn a lava bucket. Expect: 7.5 s cooks; crystal coal ~133 smelts; the EMPTY BUCKET stays in the fuel slot.
- **ITEM-019** — `/give @s orespawn:kraken_repellent` + `orespawn:creeper_repellent` (place), summon a creeper 15 blocks away. Expect: continuous push every ~0.5 s within radius 20, not once a minute.
- **ITEM-027** — Place `orespawn:duplicator_log` on grass with a small build ~5 blocks away. Expect: tree grows block-by-block, then ~20 nearby blocks are copied into the 5×5 area.
- **ITEM-037** — `/give @s orespawn:chainsaw`; swing at a mob crowd, then break a log in a big tree (`/setblock` an oak). Expect: 56 AoE dmg r=5 with saw sound; log break crushes an 11×16×11 wood/leaf volume.
- **ITEM-040 + ITEM-057** — Wear damaged experience armor (`/give @s orespawn:experience_chestplate`, damage it), hold `orespawn:experience_sword` in hotbar. Expect: armor slowly repairs while the sword drains.
- **ITEM-047/048/049** — `/give @s orespawn:instant_garden`, `orespawn:instant_shelter`, `orespawn:step_up`, `orespawn:step_down`, `orespawn:step_across`. Expect: 18×15 garden w/ 8 crops+reeds+melons+3 water channels; 7×7 furnished shelter with 14-item chest; steppers build 8-way from look yaw with extreme torches every 8, stop at obstructions, explosion fx, item kept in creative.
- **ITEM-050/051/052** — `/give @s orespawn:zoo_keeper`, `orespawn:sifter`, `orespawn:wrench`, `orespawn:ant_robot_kit`. Expect: ZooKeeper 1-use persistence; Sifter rolls tables on water/sand/gravel/dirt/grass (mod fish from water!); Wrench refuses healthy unowned AntRobots; kit re-spawns robot with carried-over health/name.
- **ITEM-059** — Smelt `orespawn:ore_uranium` / `ore_titanium`. Expect: a NUGGET each (9 nuggets → ingot), not an ingot.
- **ITEM-001/005** — `/setblock` + break `orespawn:ore_ruby` / `ore_amethyst` (no Silk Touch), then `orespawn:red_ant_troll` / `termite_troll`. Expect: ore never explodes; troll blocks erupt 15-20 mobs even WITH Silk Touch.
- **ITEM-003/021** — Break `ore_uranium`/`ore_titanium` placed below y=40 and above; break `orespawn:block_ender_pearl` egg block. Expect: XP only below y=40; egg block pops 5-9 XP ~half the time, never duplicates itself.
- **ITEM-029 (D4)** — Eat `orespawn:butter_candy`, `cooked_bacon`, `crystal_apple`, `heart` ("Love"). Expect: Speed+Jump 100s / Regen+Strength 100s / Regen+Strength 150s / Regen IV+Strength III+FireRes III+Resist II 300s + Speed/Jump 250s.
- **ITEM-060/061/062 (D4)** — Craft: skate bow (crystal sticks+string), chest + red bed from crystal planks, raw corn dog, bucket from 3 pink tourmaline ingots, cobweb from string. Expect: all five craft; oak door from crystal planks also still works.
- **C8 ExperienceCatcher** — `/give @s orespawn:experience_catcher`; drop XP (`/xp add @s 10 levels`, die, or kill mobs), click the ground under an orb worth ≥3. Expect: ~80% Bottle o' Enchanting + string + stick, catcher consumed; on a miss the catcher drops back at your feet.

### A3 — Projectiles, throwables, dispensers

- **ITEM-063 (D4) dispensers** — `/setblock ~2 ~ ~ minecraft:dispenser` + button; load `orespawn:irukandji_arrow`, `water_ball`, `sunspot_urchin` (item), `acid`, `ice_ball`, `dead_irukandji`, `laser_ball`, then each rock type. Expect: all fire as projectiles (bow sound, arrow is pickup-able); dispensed rocks keep their per-type damage/behavior.
- **ENT-D-025/026/027 + ENT-K-076 rocks** — Throw `orespawn:rock_small`(t1) … `rock_crystal_tnt`(t12); hit glass and mobs. Expect: t5 deals 10; t6, t9-12 apply WEAKNESS (not Wither); t9 ignites ~50 s; block impact shatters glass 3×3×3 with glassdead sound and returns the SAME rock type; entity hits return nothing.
- **ITEM-053** — Throw `orespawn:dead_irukandji` (throwable), `water_ball` (hits drop pickup ~10%), `sunspot_urchin` (lights blocks on fire — also ENT-S-036), spam `laser_ball`/`ice_ball` (no cooldown). Throw `red_heels`/`black_heels`/`slippers`/`boots_shoes`/`game_controller` at mobs. Expect: shoes fly, hit with poof + reddust particles, per-target damage (1.0 on Girlfriend/Boyfriend).
- **ITEM-043 + ENT-S-057** — `/give @s orespawn:ultimate_bow`. Expect: fires instantly (no charge), self-enchants Power 5 (not 10), ~25% crits; full-draw hit ≈ ceil(3×ultimateBowDamage config); halving the config halves damage.
- **ENT-A-045-051 Bertha** — `/give @s orespawn:big_bertha`; swing near mixed mobs incl. a tamed pet and a second player if available (`bigBerthaPvp` config). Expect: swing projectiles one-shot in range (496/746/82 by item); Girlfriend/Boyfriend NEVER hit; players/tamed respect the config.
- **ENT-K-033** — `/give @s orespawn:fairy_sword`? (No —) MantisClaw: hit a mob repeatedly. Expect: each hit silently drains 1 extra HP (no second hurt flash) and heals you 1.
- **ITEM-058** — Wear `orespawn:peacock_boots` and glide off a pillar with `royalGlideEnable=false`; then royal/queen boots with it true. Expect: peacock boots glide regardless; royal/queen only with the flag.

### A4 — Mob behavior & taming

- **ENT-A-004** — Let an Alien melee you on easy, then normal. Expect: POISON 2 s easy / 1.5 s otherwise — never Hunger.
- **ENT-A-013/014** — AntRobot melee cadence visibly throttled; ride it near mobs. Expect: occasional ~3.0 stomp damage to nearby mobs while ridden.
- **ENT-A-025** — Let a BandP hit you wearing armor + carrying items. Expect: it steals an item on EVERY successful hit, armor first.
- **ENT-A-054** — Boyfriend follows held cooked beef, panics when hit, opens wooden doors.
- **ENT-A-074/075** — Damage a CaterKiller, wait ~2 min near trees. Expect: transforms into Brutalfly + 10 Butterflies (explosion sound); eats leaves/logs to heal 2.0 with occasional burp.
- **ENT-A-080** — CaveFisher near cows/pigs. Expect: hunts passive animals, not only players.
- **ENT-A-082** — Cephadrome near Mothra / untamed Leon / GammaMetroid / WaterDragon; hit a Kraken with it. Expect: attacks those targets; Kraken takes 1.5× damage.
- **ENT-A-087** — Right-click Chipmunk with apples. Expect: tames at 50% per apple; dead bush releases it.
- **ENT-A-095/097** — Summon ~10 Cockateils. Expect: random bird types; only type-5 birds can drop rubies (player kill, 1-in-3).
- **ENT-A-112** — Cryolophosaurus + nearby prey animals. Expect: proactively chases prey, not retaliation-only.
- **ENT-D-002/006** — Dragon: raw beef tames/heals (1-in-5), bones ignored; diamond on a TAMED dragon → tamed Spyro replaces the adult.
- **ENT-D-014** — Emperor Scorpion in combat. Expect: occasionally spawns a baby scorpion midway to its target (uncapped — can flood).
- **ENT-D-022** — `/give @s orespawn:cage_empty` ×10. Throw at: yourself (bounces back empty), Creeper (always cages), Ghast/Enderman (~~20% escape), Kraken (~~95% escape), Bat (2 caged bats), Cockateil (4), AttackSquid (6), villager (cage consumed + returned), Iron Golem (cageable), tamed Girlfriend/Boyfriend (eats the cage, no drop).
- **ENT-D-037 / ENT-K-046** — Kill tamed vs untamed Gazelle and Ostrich. Expect: tamed 2-6 poppies; untamed Gazelle 0-2 beef, untamed Ostrich 0-2 feathers.
- **ENT-K-013** — LeafMonster among mixed small mobs. Expect: attacks only Ants/Butterflies/LunaMoths/players; never hunts with `playNicely=true`.
- **ENT-K-019/083** — Leon: dead bush untames (glass does NOT); RubberDucky tames/tempts with raw cod (not wheat), unames with dead bush.
- **ENT-K-023** — Lizard on dry land near water + near lava. Expect: periodically paths to the WATER, never toward lava/fire.
- **ENT-K-050 / ENT-S-031** — Peacock and StinkBug breed with `orespawn:crystal_apple` ONLY (regular apples do nothing).
- **ENT-K-051** — Summon ~~30 Nightmares. Expect: mostly tiny (t=0.5), big t=4 rare (~~1.5%), hitbox+model grow together up to 10×14; config `nightmareSize=5` forces max.
- **ENT-K-056** — Princess PurplePower orbs (or arena): type 2 poisons, type 3 weakens, 2.5 s each.
- **ENT-K-058** — Wild rat attacks you/pets with default configs; a rat with an owner never attacks its owner.
- **ENT-S-010** — SeaViper bite. Expect: Poison ~6 s (8 s easy), never Hunger.
- **ENT-S-030** — Kill a StinkBug in a crowd (incl. mobs well above it). Expect: NAUSEA (not hunger) on everything nearby.
- **ENT-S-033** — Tamed Stinky idles. Expect: occasional coal burp out the front, skin-matched item fart out the back (blaze powder for skin 0), with sounds.
- **ENT-S-065** — Right-click tamed Velocity Raptor empty-handed. Expect: does NOT mount; shift-click sit toggle still works.
- **ENT-S-069** — Vortex melee. Expect: victim is never launched skyward; drag pull still works.
- **ENT-S-085 (D4)** — Wear a helmet+chestplate, hold an item, stand near WormLarge. Expect: 1-in-4 steals helmet (chestplate if bare-headed), independent 1-in-4 steals held item; stolen gear scatters as item entities; none of it while `playNicely=true`.
- **ENT-S-078 (D4)** — WormSmall at night wearing boots. Expect: 1-in-6 boots theft; worm pops when it surfaces (tall grass counts as air).
- **ENT-K-047/048 (D4)** — Peacock + summoned Termites; then a lone Peacock at `/time set 1000`, y 50-100. Expect: hunts the termite (flat 6.0 hits); lays 1-3 eggs in the first half of the day when ≤2 other peacocks within 16.
- **ENT-D-010 (D4)** — `/summon orespawn:easter_bunny` (summon bypasses the Easter gate), wait ~30 s. Expect: 1-in-600/tick lays a stack of 1-3 random spawn eggs from the 115-type table; breeds with Crystal Apple (NO carrot taming — the audit's carrot claim was wrong).

### A5 — Drop spot-checks (kill each once; creative kills count as player kills)

- **C1 loot** — Alien (spider eyes/flint/map/clock/compass), AntRobot (redstone jackpot), Beaver (0-2 porkchop), tamed Camarasaurus (2-6 poppies; untamed none), CaveFisher/CliffRacer/CloudShark/Cryolophosaurus/CreepingHorror (gamble drops), Coin (10-slot jackpot — **D4: slot can now yield `coin_spawn_egg`**, ENT-A-098).
- **C2 loot** — DungeonBeast (25% each: pink ingot/crystal apple/oak log/nothing, ×0-2), Fairy (crystal torch), Firefly (extreme torch), GammaMetroid (5-14 gold nuggets + 6-15 iron), Ghost/GhostSkelly (NOTHING), GiantRobot (~60-116 laser balls + 10-19 kit/component rolls incl. detector rails).
- **C3 loot** — LeafMonster (log OR leaves OR flesh), LurkingTerror (beef/flint/feather), Rat (rotten flesh), Robot2 (2-9 iron blocks + 5-10 ingots + redstone parts), Robot3/5 (20-40 laser balls + parts), Robot4 (20-56 laser balls + RayGun + painting + parts), Rotator (ONE of pink ingot/tigers-eye ingot/crystal coal/iron).
- **C4 loot** — Scorpion (~10% each gold/uranium/titanium nugget, often nothing), Skate (string), SpiderRobot (14-27 redstone components), SpitBug (1-3 amethyst), tamed Spyro (1-4 beef; untamed nothing), TerribleTerror (flesh/emerald/feather), tamed VelocityRaptor (2-6 poppies), Vortex (eye + painting + 5-11 mixed), WormSmall (nothing).
- **D4 drops** — Chipmunk (orig table + poppy only when tamed), GoldFish, RubberDucky, Stinky (beef only when tamed). Lavafoam: break in overworld (0 XP) vs Nether (5-13 XP) — `/setblock ~ ~ ~2 orespawn:lavafoam` in each.
- **BOSS-025/035/042** — ThePrince (1-4 beef), ThePrincess (1-4 beef), ThePrinceAdult (1 `orespawn:prince_egg`) — no diamonds/gold.

### A6 — Immunities

- **ENT-A-031/060, ENT-S-061/068** — Drop Basilisk, Brutalfly, Urchin, Vortex into a lava pit. Expect: no fire damage on any.
- **ENT-K-045** — Ostrich pushed into cactus. Expect: no cactus damage; normal damage otherwise.
- **ENT-K-007 (D4)** — Kyuubi standing in its own fire attack / lava. Expect: fully fire-immune, no self-damage.
- **ENT-S-025 / ENT-S-047 (D4)** — SpitBug and Triffid vs cactus and long falls. Expect: immune to both.
- **ENT-A-052 / ENT-A-001 (D4)** — Brutalfly BetterFireballs pass through Mothra/other fireballs/royalty; acid (spit at TrooperBug/SpitBug) vanishes harmlessly on them.

### A7 — Riding & movement

- **B3 riding** — Mount and fly/ride each: Dragon (no rubber-banding — BUG-020), Leon, Leonopteryx, Cephadrome (feed it first), Ostrich (FAST jump on UP key), tamed PrinceTeen + Adult (strafe keys fire the canon trio). If a second player is available: observed movement is smooth.
- **B3 SpiderDriver** — armor reads 8 mounted / 20 on foot (`/attribute`); attacks with poison while mounted on SpiderRobot.
- **C8 controls** — Default binds: LEFT ALT (not Space) = fly up / sprint on ridden Dragon/Cephadrome/Ostrich.
- **D2 Hoverboard physics** — `/summon orespawn:elevator` (displays "Hoverboard"): W/S throttle, Left Alt FAST boost, climbs terrain, pitch grows with speed.
- **D2 Hoverboard crash** — Ride into a wall above ~0.75 speed. Expect: shatters into sticks + 2 diamonds.
- **D2 Hoverboard malfunction** — Sustained high speed. Expect: rare malfunction (explosions/smoke, speed bleed, ~2.2 s).
- **D2 Hoverboard skins + guards** — Click with `orespawn:ultimate_sword`: cycles 10 skins. Mob punches can't destroy it while ridden. Hum (`orespawn:hover`, ENT-D-012 — randomized variants, not beacon) only while ridden.
- **D2 registry** — Creative tab has exactly ONE Hoverboard entry; `/summon orespawn:hoverboard` FAILS (id is `elevator`; intentional).

### A8 — Animations, models, HUD (visual)

- **B4 idle animations** — Bee/Mothra/Urchin/Kyuubi etc. keep animating while you stand still (no frozen wings); Mothra renders 10× scale, slow flaps.
- **C8 Rotator** — spins as a 24-blade tri-axis gyroscope ball, not three flat blades.
- **C8 Kraken** — two Krakens side by side: mouth-twitch cycles differ; twitch harder in combat.
- **C8/D2 GiantRobot** — walks with BOTH legs + arms visible, hip bob; aggro → windmill punch + shoulder twist; D2: walk-state solver, no moonwalk.
- **C8/D2 Spider/AntRobot legs** — SpiderRobot renders all 8 legs, AntRobot all 6; D2: feet PLANT in the world and step ahead of body movement (no synchronized sine paddling); legs relocate when overstretched; ridden SpiderRobot occasionally flattens grass to dirt.
- **C8 Robot2/3/4 arms** — Robot2 idle arms at sides, random windmill only in combat; Robot3 swings only in combat; Robot4 shield pumps + cannon raises/follows only in combat.
- **C8 Rat** — tail sways calm / thrashes attacking; head does NOT turn.
- **ENT-A-036 / ENT-K-005 / ENT-S-045 / BOSS-016 sounds** — Basilisk, Kraken (growl + alo_death), TRex (trex/alo set), Godzilla (godzilla_living/alo_hurt/godzilla_death): custom sounds, no Ravager/Elder-Guardian/Ender-Dragon audio.
- **C8 HUD** — Crosshair on TheKing/robot/big crab: textured health bar above hotbar + red name; named pets show custom name; YOUR Girlfriend shows a bar, someone else's doesn't; bar shifts up 10px swimming/armored; `guiOverlayEnable=false` hides it.
- **ENT-A-002/012/017/023/040/060/072/078/091/106 hitboxes** — F3+B on Alien/AntRobot/AttackSquid/BandP/Bee/Brutalfly/CaterKiller/CaveFisher/CloudShark/CreepingHorror vs the size table in `phase_c_reports/C1_entities_A_C.md`; CaterKiller halves with `playNicely=true`.
- **ENT-A-100/101/102/103 crabs** — natural-ish sizes vary (¼/½/full, rare giants; spawner crabs all 0.35); crab walks to water, dry-out damage away from it, scorpion sounds on melee, splash heal in water.

### A9 — Ultimate Rod lava fishing (ENT-S-059, D4)

- **Hook + lava** — `/give @s orespawn:ultimate_fishing_rod`; dig a 3×3 lava pool. Expect: bobber floats IN lava (fire-immune), bite cycle with lava particles works like water fishing.
- **Pools** — Fish ~10 catches (water and lava). Expect: junk / treasure / vanilla fish / OreSpawn fish pools; lava adds lava-fish; caught items fly to you and SURVIVE the lava (LavaLovingItem); gear arrives pre-damaged with a level-30 enchant; XP orb at your feet per catch; rod keeps working (doesn't dismiss the custom hook).

### A10 — Ranged-attack set (D3)

- **ENT-A-055 / ENT-D-049** — Hand Boyfriend/Girlfriend an `orespawn:ultimate_bow`: arrows fly (heal allies if PvP off); without bow: shoes fly. Melee b_fight/o_fight sounds; taunts at 4-7 blocks.
- **ENT-A-018/019** — AttackSquid: ink/water projectiles beyond 3 blocks, melee inside.
- **ENT-A-062** — Brutalfly fireball type follows difficulty (Easy small / Hard BetterFireball), heals itself +1 per shot.
- **ENT-D-044** — GiantRobot fires laser volleys only once its head faces you; slower "special" lasers from >10 blocks.
- **D3 Prince family** — Babies/Princess randomly take off and land; hurt pets <25% HP flee airborne; canon trio fires only in the 5-12 block band while fire is lit; ice block / flint&steel toggle fire with chat messages; teen/adult fly to a distant owner, bite-and-break-off combat, audible wing flaps; **diamond regressions**: DIAMOND item on tamed teen→baby, adult→teen (BOSS-018/020/029: cooked beef heals 80; gold ingot and cake do NOTHING).
- **BOSS-024** — Baby Prince hunts Butterflies/Cockateils/Dragonflies/Mosquitoes/Mothra, not just monsters.
- **D3 Princess power** — In combat, discharge >500 → 3 PurplePower orbs; at peace, terraforming bloom (flowers/grass/cactus/lava→water, 2 Butterfly/Cockateil hatches) under mobGriefing; firework-spark aura when charged.

---

## B. Boss arena (build a big flat pen away from your base, or use a fresh superflat)

**True-HP verification via crosshair bar + `/data`** (vanilla caps display at 1024 — these
must read their real values):


| Boss                       | Summon                              | Expected Health |
| -------------------------- | ----------------------------------- | --------------- |
| TheKing                    | `/summon orespawn:the_king`         | **7000**        |
| TheQueen                   | `/summon orespawn:the_queen`        | **6000**        |
| Godzilla                   | `/summon orespawn:godzilla`         | **4000**        |
| ThePrinceAdult             | `/summon orespawn:the_prince_adult` | **3000**        |
| ThePrinceTeen              | `/summon orespawn:the_prince_teen`  | **1500**        |
| SpiderRobot                | `/summon orespawn:spider_robot`     | **1500**        |
| Kraken (sanity, under cap) | `/summon orespawn:kraken`           | **1000**        |


Check: `/data get entity @n[type=orespawn:the_king] Health`

- **B2 caps** — the three big ones read 7000 / 6000 / 4000 (not 1024).
- **B2 stats** — spot-check 3-4 mobs: `/attribute @n[type=orespawn:kraken] minecraft:generic.armor base get` vs `phase_b_reports/B2_mobstats.md`.
- **BUG-005** — TheQueen melees a SURVIVAL you to 0 HP: normal death screen/drops/respawn. Bonus: a low-HP mob victim vanishes with no drops (original quirk preserved).
- **BUG-006** — Stand at Godzilla's jump landing in Creative and Spectator: NO damage; in Survival the shockwave hits.
- **BOSS-002/007** — F3+B on King and Queen: 22-wide × 24-tall envelope; parts take/route damage (King parent unhittable, Queen parts glued to bones); models not lost inside the box.
- **BOSS-005/012** — Place `orespawn:king_spawner` / `queen_spawner`: ~5 s fuse, spawner + block above turn to air, boss appears 8 blocks up with living sound, stays leashed near spawn. With `theKingEnable=false`/`theQueenEnable=false`: block fizzles, nothing spawns.
- **BOSS-006** — Queen below 2/3 HP with <10 player hits takes visibly reduced damage (armor 23 via overridden getter — `/attribute` won't show it).
- **BOSS-010** — First hit on a dormant (blue) Queen deals normal damage during the 3 s wake-up; she can fight/be hurt during it.
- **BOSS-026/031** — F3+B: PrinceTeen 3.25×4.25, PrinceAdult 6.25×10.25; rider seats correct.
- **BOSS-032** — Tamed idle riderless Adult with `fullPowerKingEnable=true` transforms (diamond-block fast-path) into a King with the "Prepare to die!" sequence; config false → never; King does NOT deal doubled damage from that config.
- **BOSS-036** — PrinceAdult: king_living only while aggro+riderless, king_hit on hurt, trex_death on death.
- **B1 boss drops (Survival kills)** — Kraken (120-279 ink sacs + d53 gear, NO cooked cod), Godzilla (painting/beef/bone, NO emeralds), TheQueen (56× scale/beef/bone/flesh + Princess spawns), TheKing (royal set + 300 random registry items + Prince spawns), Mothra (20-moth burst), Dragon (1-6 beef, no bones). Nothing drops twice.

---

## C. Survival/worldgen sanity (new world or unexplored chunks; survival for spawn tests)

- **C7 vein rates** — `/execute in orespawn:mining run tp @s 0 100 0`, mine at Y10-20: uranium/titanium ≈3× overworld density; salt only ABOVE Y50; amethyst only BELOW Y25.
- **ITEM-064** — Set `lessOre=true`, generate NEW chunks: ~1/3 uranium/titanium/amethyst/salt veins, ~1/2 troll blocks.
- **C7 ruby lava-seek** — Overworld cave lakes <Y50: ruby ore in stone directly UNDER lava, never free-floating; Nether has lavafoam veins + occasional nether ruby.
- **C7 Islands terrain** — `/execute in orespawn:islands run tp @s 0 80 0`: flat grass plane at Y7, no carvers/lakes; unstable anthills near the surface.
- **C7 Chaos terrain** — 128-high stone world, grass band ~Y60-65, vanilla sky; chaos roster spawns; no dungeons/towers.
- **C7 structures** — `/locate structure orespawn:beehive` (Mining only), `orespawn:greenhouse`/`robot_lab`/`white_house`/`challenge_tower_king` (Islands only), `orespawn:royal_altars` (Utopia ~45-chunk grid), `minecraft:mineshaft` + `stronghold` resolve in Mining and Village.
- **C7 dungeons** — Utopia lava pools <Y50: ruby-brick dungeon (ruby kit/ThunderStaff chest pool); generic cobble dungeons in Utopia/Mining/Village/Islands — break the spawner block, 12-mob-ladder mob appears after ~20 s (ITEM-020's 400t fuse).
- **C7 Mining lakes/rooms** — caves contain vanilla-style water/lava lakes and monster rooms.
- **C7 termite gate** — Right-click a termite with items/armor: "Empty your inventory!" / "Take off your armor!"; empty+bare: Crystal teleport. Ant teleport takes a FOLLOWING tamed wolf along, not a sitting one.
- **C7 crystal egg ores** — Crystal dimension sphere shells: breaking oreurchin/orerat/etc. gives block item + occasional 5-9 XP, NO mob spawn.
- **C1 spawns** — Alien/Alosaurus/Camarasaurus/Baryonyx in Mining (Alosaurus/Baryonyx also Utopia), NOT End/overworld; Boyfriend beach hotspots; Bee/CaterKiller/Basilisk/Brutalfly/BandP per-biome.
- **C2 spawns** — Dragon/GoldFish/EnderReaper on Island; DungeonBeast/Flounder/Irukandji/Frog in Crystal; EnderKnight/EnderReaper/Hammerhead/GammaMetroid/DungeonBeast in Chaos; GammaMetroid swarms in Mining; EnderKnight/EnderReaper dark-forest hotspots, NONE in the End; Fairy only dark forest; Girlfriend beach groups 8-15; Hydrolisc swamp/jungle; Frog river/swamp; NO ocean Flounder/GoldFish/Irukandji/Hammerhead/Hydrolisc.
- **C3 spawns** — Kraken/Leon/Leonopteryx/RubyBird never natural; Kyuubi Nether weight doubled; Lizard river/swamp/ocean only; rat swarms in dark forests (10-20) + taigas only.
- **C4 spawns** — TRex only Chaos/Mining; TerribleTerror Island/Chaos; Urchin/Skate/Vortex Crystal(+Chaos); Spyro/VelocityRaptor Mining only; Stinky Nether/badlands/Island; SeaMonster ocean w4 + swamps; WormSmall never natural (day) — **D4: WormSmall spawns at NIGHT only** (ENT-S-078 gate).
- **ENT-A-110** — CreepingHorror naturally only in darkness at night below y=15 (or Chaos).
- **C8 spawn flags** — Set `krakenEnable=false`, `godzillaEnable=false`, `cowEnable=false`: no natural spawns of those (no AttackSquid revenge Krakens); eggs//summon still work.

---

## D. Date-gated tests (LAST — they change your system clock)

**How the gates work (PN-014):** the port evaluates `LocalDate.now()` **live at every
check** — nothing is frozen at launch. You do NOT need to close the client: change the
Windows clock (turn OFF "Set time automatically"), and spawn attempts pick up the new
date within seconds. Caveats:

1. **Girlfriend reads the date when she's constructed/loaded** — a Girlfriend already
  standing in the world won't turn giant when the clock flips. Summon a FRESH one (or
   leave/rejoin the world) after changing the date.
2. Do these after everything else, then reset the clock. If Gradle acts up about clock
  skew afterwards, `.\gradlew --stop` restarts the daemon.
3. The mod's "Easter" is the original's fixed **April 20** (not the real movable feast).

- **ANIM-016 Halloween (closes ENT-D-039/041)** — Set clock to **Oct 31**, night, in a plains/forest (survival, mob spawning on). Expect: Ghost + GhostSkelly natural spawns across the 22-biome list; back on a normal date they spawn ONLY in the 5 year-round biomes (snowy taiga, taiga, frozen river, jungle, dark forest). Drop: nothing (C2).
- **ANIM-016 Valentine's (ENT-D-011-family)** — Set clock to **Feb 14**, `/summon orespawn:girlfriend` (fresh!). Expect: GIANT (2.5×8, renders 5×) angry Girlfriend, 800 HP (`/data get`), `girlfriendv` texture, attacks players and Boyfriends (not her owner/pets), immune to inWall damage, `o_hurt` ambient. Hit her with `orespawn:rose_sword`: each hit drops a "Love"; ~1-in-4 cures her (shrinks, calms, extra Love shower). Thrown shoes deal **10** damage today (ITEM-053).
- **ANIM-016 Easter (closes ENT-D-011)** — Set clock to **Apr 20**. Expect: EasterBunny NATURAL spawns appear (any other date: none — verify with a spawn-friendly area first). Egg-laying itself is date-independent (tested in A4 via /summon).

---

## E. Untestable until D5/D6 — stays on the pending list for the final pre-release pass

- **WGEN-005 / ITEM-062 remainder** — the ~105-type SpawnOres block pool and its 116
water-bucket→spawn-egg conversion recipes (D5 spawn-block slice).
- **WGEN-042 / ITEM-020 remainder** — the 48 dungeon structure builders beyond
generic/ruby (D5); the 12-mob spawner ladder itself IS testable (section C).
- **WGEN-014/018/033 + WGEN-003/004/007/021/037/038/044** — missing structures/features
(D5).
- **WGEN-045** — Experience-tree growth from the planted seed (D5; seed placement itself
is testable in A2).
- **ENT-A-083** — Cephadrome flying-mount signature system (D5/D6; basic B3 ride test
still applies).
- **ENT-A-054 remainder** — Boyfriend Jealousy goals + MoveIndoors (Phase D remainder).
- **ITEM-023 remainder** — ZooCage placed-block form.

---

## D5 — structures + SpawnOres pool (added 2026-08-08)

### D5a — Mining dimension

- **WGEN-037** — `/execute in orespawn:mining run tp @s 0 100 0`, then `/locate structure orespawn:basilisk_maze` and tp there. Expect: sandstone step-pyramid marker sunk ~2 blocks into terrain; 4×4 bedrock shaft with 2×2 core and spiral obsidian parkour steps descending 20-29 blocks; iron-ore-walled antechamber with 3 Extreme Torches; 30×30 obsidian maze (2-wide corridors, solvable west→east, lava traps in the floor); 30×30 taller chamber with 3 persistent Basilisks, RTP floor traps, 2-4 chests on the east wall (torch 3 above each) rolling diamonds 15-25 / cages / uranium / fish / ultimate gear per the 31-entry table.
- Walk >130 blocks from the maze chamber and return: the 3 Basilisks must NOT have despawned (persistence).

### D5b — Islands dimension

- **WGEN-042 (rookery)** — `/execute in orespawn:islands run tp @s 0 20 0`, `/locate structure orespawn:nightmare_rookery`. Expect: cluster of jagged stone spires in two wandering ridges; roughly 1-in-10 spires truncate at 19 blocks with a chest on top and a Nightmare (Pitch Black) spawner sitting directly on the chest; chests roll dead stink bugs / black+scary flowers / nightmare eggs / robot kits / bones / string / flesh / XP bottles (4-8 stacks).
- **WGEN-051/055** — `/locate structure orespawn:challenge_tower_king` (and _queen): tower heights VARY (most towers stop below 6 floors; full towers with the Nightmare cap ≈ 28%); towers sit on the grass plane (not floating on a heightmap ledge); with `lessLag=1` roughly half as many towers/rookeries generate in fresh chunks.
- **WGEN-052/053/056 + ITEM-066** — inside any tower floor room: NO scaffolding anywhere; all 4 chests face the room centre; non-prize floors roll the faithful tier lists (emerald kit / experience+pink / amethyst+tigers-eye / ruby+utility / the 83-spawn-egg jackpot at level 5). On a level-6 tower's BOTTOM floor: west chest = The Prince (King) / The Princess (Queen) SPAWN EGG that actually spawns the royal, east = Royal/Queen helmet+chestplate, north = leggings+boots, south = Royal Guardian Sword; 4 RTP blocks around the central spawner column.
- **WGEN-054** — level-2+ tower centre rooms at the right difficulty spawn Jumpy Bugs (TrooperBug — jumps, no spit attack).
- **WGEN-055 (worm ring)** — around a FULL-height tower, dig at surface−1 out to ~55 blocks east/±55 north-south of the base: buried Large Worm spawners scattered outside the castle, with no rectangular cutoff at chunk borders.
- **ITEM-020** — place 4+ Random Dungeon Spawner blocks; observed outcomes should now include the King tower (2), Basilisk maze (23), rookery (38), Queen tower (47) at the block position.

### D5c — SpawnOres + recipes (overworld / Utopia / Village / Chaos / Mining)

- **WGEN-005** — fly through NEW chunks at Y50+ mining into stone: spawn-ore veins (clump ~4) of many different mobs appear frequently (~24/chunk overworld, ~27 dims); Mining dim noticeably denser (×3); Islands and Crystal (other than its own 11 egg ores) get NONE; Nether/End get none.
- Break any spawn ore: drops itself; ~50% of breaks pop 5-9 XP; Silk Touch: block, no XP; nothing ever spawns from breaking.
- Lang: every spawn ore reads "Ancient Dried <Mob> Spawn Egg" (incl. the renamed 11 crystal egg ores + kraken/dragon).
- **ITEM-062** — craft water bucket + spawn block for a sample across the table (spider→vanilla spider egg, wither boss→wither egg, criminal→BandP egg, enchanted cow→Enchanted Apple Cow egg, kraken→Kraken egg, urchin ore→Urchin egg): 1 egg + EMPTY BUCKET returned. 9× Mobzilla/King/Queen part blocks → full egg block → + water → boss egg. Mobzilla FULL egg blocks also mine from worldgen; King/Queen full blocks never do (parts only).
- **LessOre=1** — new chunks: spawn-ore veins cut to ~1/3.
- Confirm the interim content is GONE: no `orespawn:ancient_dried_egg` block anywhere (worldgen or creative tab), and dragon/kraken spawn blocks now appear via the pool at Y50+ (not as deep single blocks).

---

## Failure log

*(appended during the session as FAILs come in)*