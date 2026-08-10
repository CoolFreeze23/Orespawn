# TESTING_CHECKLIST — in-game manual test session (reorganized 2026-08-10, session live)

Every "Pending manual tests" item from FIX_LOG.md (master list + C7 + C8 + D2 + D3 + D4
+ D5 + D6a + D6b sections), regrouped by test environment for one efficient play
session: **A creative flatworld → B boss arena → C survival/worldgen (now including all
Phase D structures) → D date-gated LAST**.

**Marking:** items get `[PASS]` / `[FAIL]` prefixes as results come in. FAILs get logged
in the Failure log with triage; fixes are batched after the session (nothing applied
mid-session unless it blocks testing).

**Client:** `gradlew runClient`. Finding #1 DID exist this session — see TEST-001 in the
Failure log (registry crash on world creation; fixed mid-session as a blocker). Config
file for flag tests: `runs/client/config/orespawn-common.toml`.

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
- **C7 termite gate + ant chain (WGEN-049)** — dimension access via the ant chain: `/summon orespawn:ant` (→ Utopia), `orespawn:red_ant` (→ Mining), `orespawn:termite` (→ Crystal), `orespawn:unstable_ant` (→ Islands). Right-click EMPTY-HANDED teleports; clicking the same ant type while already in its dimension returns you to the Overworld; portal cooldown between hops. Termite with items/armor held: "Empty your inventory!" / "Take off your armor!" chat gates, no teleport. A FOLLOWING tamed wolf within 48×24×48 travels along; a SITTING one stays.
- **C7 crystal egg ores** — Crystal dimension sphere shells: breaking oreurchin/orerat/etc. gives block item + occasional 5-9 XP, NO mob spawn.
- **C1 spawns** — Alien/Alosaurus/Camarasaurus/Baryonyx in Mining (Alosaurus/Baryonyx also Utopia), NOT End/overworld; Boyfriend beach hotspots; Bee/CaterKiller/Basilisk/Brutalfly/BandP per-biome.
- **C2 spawns** — Dragon/GoldFish/EnderReaper on Island; DungeonBeast/Flounder/Irukandji/Frog in Crystal; EnderKnight/EnderReaper/Hammerhead/GammaMetroid/DungeonBeast in Chaos; GammaMetroid swarms in Mining; EnderKnight/EnderReaper dark-forest hotspots, NONE in the End; Fairy only dark forest; Girlfriend beach groups 8-15; Hydrolisc swamp/jungle; Frog river/swamp; NO ocean Flounder/GoldFish/Irukandji/Hammerhead/Hydrolisc.
- **C3 spawns** — Kraken/Leon/Leonopteryx/RubyBird never natural; Kyuubi Nether weight doubled; Lizard river/swamp/ocean only; rat swarms in dark forests (10-20) + taigas only.
- **C4 spawns** — TRex only Chaos/Mining; TerribleTerror Island/Chaos; Urchin/Skate/Vortex Crystal(+Chaos); Spyro/VelocityRaptor Mining only; Stinky Nether/badlands/Island; SeaMonster ocean w4 + swamps; WormSmall never natural (day) — **D4: WormSmall spawns at NIGHT only** (ENT-S-078 gate).
- **ENT-A-110** — CreepingHorror naturally only in darkness at night below y=15 (or Chaos).
- **C8 spawn flags** — Set `krakenEnable=false`, `godzillaEnable=false`, `cowEnable=false`: no natural spawns of those (no AttackSquid revenge Krakens); eggs//summon still work.

---

*(The former "E. Untestable until D5/D6" section is RETIRED — everything in it landed
in D5-D6b and now lives in the C subsections below, except three items that are Phase E
WORK, not tests: ENT-A-083 Cephadrome flight, ENT-A-054 Boyfriend AI remainder, and
ITEM-023 ZooCage block form — see phase_d_rollup.md §3. WGEN-003/004/007 are also
Phase E features and not testable yet.)*

## C-D5 — structures + SpawnOres pool (D5, 2026-08-08)

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

## C-D6a — strong-model structures (D6a, 2026-08-08)

- **EnderCastle (End)** — `/execute in minecraft:the_end run tp @s 100 70 0`, `/locate structure orespawn:ender_castle_end`. Expect: 29x29 obsidian-plate castle on end stone (central OR outer islands — PN-017), 4 spiral-stair corner towers, rooftop lava pool + dragon-egg pedestal, Ender Knight/Reaper rooftop spawner pairs + pit + CaveFisher alcoves, 3 alcove chests (facing inward) rolling the ender/experience-catcher table (6-10 stacks), trophy ender chest EMPTY (plain block).
- **EnderCastle (Islands)** — `/locate structure orespawn:ender_castle_islands` in orespawn:islands: same castle at grass level.
- **IncaPyramid (Islands)** — `/locate structure orespawn:inca_pyramid`: 41x31 stepped pyramid, 4 torch-ended ramps (support pillars reach the ground — PN-018), lit-lamp temple checkerboard, 5 water altars, 4 Creeper Repellents, Molenoid spawner, trapdoor + ladder shaft, 24-grave graveyard (~1/3 with Ghost spawners, poppy/dandelion/poppy beds), all chests roll 10-14 stacks of the 480-weight table.
- **KyuubiDungeon (Mining)** — `/locate structure orespawn:kyuubi_dungeon`: sealed surface hut (enter via the 1x1 roof hole), 22-deep shaft with water brake, lava-walled corridor, boss room with altar + 4-tier ziggurat, 8-Blaze spawner ring, kyuubi chest (7-13 stacks) + 4 wall chests with DIFFERENT fill counts (4-8/3-7/5-9/6-10) incl. blaze-egg loot entries.
- **Robot Lab (Islands)** — freshly generated lab: BOTH rear sniper spawners exist behind the hangar wall; altar spawns Robo-Pounder, treasure room Robo-Warrior, pillars Robo-Sniper; railway has powered (golden) rails + unpowered floor levers; assembly-line sticky pistons face south under white carpet and are CRUSHING (their lever generates powered); entry doors face NORTH and open with the wall buttons; chests roll 10-14 of the 755-weight table (minecarts, kits, Ray Gun — no droppers/dispensers/clocks).
- **Hospital (End)** — `/locate structure orespawn:hospital`: 10x10 iron-bar cage, 4 End Crystals on bedrock caps (NO dragon), 8 spawners, chest at the corner (6-10 of the 210 table).
- **MonsterIsland (overworld)** — `/locate structure orespawn:monster_island` over PLAIN ocean only (not deep/frozen/warm — PN-019): floating lens island in the water surface, canopy tree, 4 spawners all of ONE randomly picked mob, 2 chests (4-8 of the 450 table).
- **DSB outcomes** — Random Dungeon Spawner can now also produce: fairy tree (0), fairy castle tree (1), kyuubi dungeon (7), hospital (24), ender castle (27), inca pyramid (29), robot lab (30), monster island (37) at the block.
- **Trees** — a lone Duplicator Log appears rarely in veggie patches (duplicatorTreeEnable on); the experience sapling grows the REAL experience tree (2x2 oak trunk, drooping branch crown); crystal fairy trees shrink with lessLag=1; fairy-tree chunks that fail the site scan still suppress termites (WGEN-062 quirk).

---

## C-D6b — mechanical structures, batches 1-4 (2026-08-08/10)

**Setup for this whole section:** overworld structures from a boat/elytra over fresh
chunks; Islands = `/execute in orespawn:islands run tp @s 0 20 0`; Village =
`/execute in orespawn:village run tp @s 0 100 0`; End via a portal or
`/execute in minecraft:the_end run tp @s 100 70 0`; Mining =
`/execute in orespawn:mining run tp @s 0 100 0`. Dungeon Spawner Block outcomes:
`/setblock ~2 ~ ~ orespawn:random_dungeon_block` then wait ~20 s (400-tick fuse,
ITEM-020) — the block + the block above vanish and one of the 50 outcomes builds.

- **PlayPool (overworld ocean)** — `/locate structure orespawn:play_pool`: floating platform 16 blocks above the sea with 4 Attack Squid spawners, twin chests (only the x+1 chest has loot: ink/squid-zooka/gold-nugget/flesh, 3-7 stacks), water channel with flowing caps.
- **CloudSharkDungeon (Islands sky)** — `/locate structure orespawn:cloud_shark_dungeon` then fly to Y150-159: tiny glowstone cluster with 4 Cloud Shark spawners + 1 chest (4-8 stacks incl. experience tree seeds).
- **GoldFishBowl (overworld ocean, WGEN-042)** — `/locate structure orespawn:gold_fish_bowl`: glass bowl on the surface: sand bed, water fill, 4 glowstone corners, Gold Fish spawner, open wall-top ring and unwritten base ring (faithful oddities), NO chest.
- **SpitBugLair (swamp, WGEN-042)** — `/locate structure orespawn:spit_bug_lair`: emerald-ore antenna, 3 Spit Bug spawners, loot chest; only in plain swamp (not mangrove).
- **UrchinSpawner (Crystal, WGEN-042)** — set `urchinEnable=false`, fly fresh Crystal chunks: no new urchin spawners generate.
- **RotatorStation / other DSB outcomes** — Random Dungeon Spawner can now produce types 3/12/13/14/15/16/17/18/19/20/34 (rotator station, play pool, water-dragon lair, cloud cluster, leaf tower, mini dungeon, gold fish bowl, graveyard, spit-bug lair, igloo, cephadrome altar) at the block.
- **EnderReaperGraveyard (End, WGEN-042)** — `/locate structure orespawn:graveyard` in the End: roofless iron-bar cage, 8 obsidian graves with flush chests (eye/poppy/dandelion/pearl 3-5 stacks), 4 Ender Reaper spawners; foundation skirt fills only air below (End terrain preserved).
- **WaterDragonLair (ocean, WGEN-042)** — `/locate structure orespawn:water_dragon_lair`: floating polar-math disc with iron annulus, lapis/spawn-egg rim courses, canopy tree, 4 Water Dragon spawners, chest (4-8 of the 145 table).
- **LeafMonsterDungeon (plains, WGEN-042)** — `/locate structure orespawn:leaf_monster_dungeon`: log tower with leaf crown, 4 Leaf Monster spawners, chest pair (one filled, 12-16 stacks), foundation roots leave the grass threshold intact.
- **MiniDungeon (Islands, WGEN-042)** — `/locate structure orespawn:mini_dungeon`: spawner ring at j=9 (12 spawners), corner caps, floor spawners, chest.
- **CephadromeAltar (Islands, WGEN-042)** — `/locate structure orespawn:cephadrome_altar`: altar per spec with its spawners.
- **Igloo (WGEN-042 / WGEN-071)** — DSB type 20 ONLY (no natural generation — placement is the one open structure item, WGEN-071/Phase E). Roll `/setblock ~2 ~ ~ orespawn:random_dungeon_block` until an igloo appears (or verify via other outcomes that 20 exists): snow/ice dome, west oak door, Rat/Ghost/Ghost-Pumpkin-Skelly spawners, north-facing chest with 16 independent 50% fixed items; at (+,+) build coordinates the apex has a 1x1 skylight, at negative coordinates it closes (faithful float quirk).
- **BouncyCastle (desert)** — `/locate structure orespawn:bouncy_castle`: lavafoam (bouncy, friction 1.1) castle sunk to the sand surface, 9 spawners (Silverfish/Rat/Scorpion mix), north-facing chest at (+3,+3,+3) with 6-10 stacks (cod/poppy/dandelion/pearl table, weight 180).
- **DamselInDistress (Village dim)** — `/locate structure orespawn:damsel_in_distress` in the Village dimension: 9×9 mossy-decay cottage with front gable, 7×4 iron-bar jail wall, 2 Scorpion spawners, north-facing chest (10-14 stacks: iron tools + foods), and a live Girlfriend standing in the jail cell (persistent by entity class — she must NOT despawn when you leave and return).
- **GirlfriendIsland (ocean)** — `/locate structure orespawn:girlfriend_island`: MonsterIsland's twin island/tree geometry, spawners Girlfriend/Boyfriend/Gold Fish×2 (fixed, no random pick), TWO chests each with 4-8 stacks of the damsel food/tool table.
- **StinkyHouse (Islands)** — `/locate structure orespawn:stinky_house`: 25×17 fenced yard (1/3 fence gaps, 1/10 bushes, never overwrites yard air), 13×10 house with corner-adjacent window panes and 1/10 wall decay, doorway force-cleared, Stink Bug + Stinky spawners, chest (8-12 stacks, weight-215 table incl. both stink eggs).
- **Pumpkin (Islands)** — `/locate structure orespawn:pumpkin`: 14-wide orange-terracotta jack-o'-lantern floating one block above grass, 48-cell carved face (eyes/nose/mouth) on the −z wall, green stem, interior plank candle with netherrack cap, twin fires, 2 Ghost Pumpkin Skelly spawners, NO chest.
- **Rainbow (Islands sky)** — `/locate structure orespawn:rainbow` then fly to Y70-89: 8 nested wool arches (red innermost → pink outermost) threading a raining cloud slab, 6 Cloud Shark spawners, roof double chest — BOTH halves filled (10-14 stacks each, weight-150 table incl. magic apple).
- **DSB batch-3 outcomes** — Random Dungeon Spawner can additionally produce types 26/28/35/39/44/46 (bouncy castle, damsel cottage, girlfriend island, stinky house, pumpkin at +1Y, rainbow) at the block.
- **SpiderHangout (Village dim)** — `/locate structure orespawn:spider_hangout`: 20×20 gravel pad on a stone slab, 12 Spider Driver spawners in 3-high corner columns, one persistent Robot Spider at the pad centre (no spawn sound), NO chest. With `spiderDriverEnable=false` no new hangouts generate (the Dungeon Spawner Block can still build one — faithful).
- **RedAntHangout (Village dim)** — `/locate structure orespawn:red_ant_hangout`: 16×16 gravel pad with four 3×3 red-ant-block corner pads, one persistent UNOWNED Robot Red Ant at centre (wrench-claim per its item flow), no spawners, no chest.
- **FrogPond (plains, WGEN-042)** — `/locate structure orespawn:frog_pond`: 7×7 still-water sheet sunk at the grass line, Frog spawner, centre riser with flowing cross, lily-pad cross above.
- **DisableOverworldDungeons gate (WGEN-064)** — set `disableOverworldDungeons=true`, fly fresh overworld chunks: NONE of the 11 overworld dungeon types generate (ponds, haunted house, bouncy castle, leaf tower, spit-bug lair, fish bowl, islands, play pool); `/setblock` a Random Dungeon block: outcomes still build (DSB never gated — faithful).
- **RubberDuckyPond (plains)** — `/locate structure orespawn:rubber_ducky_pond`: 12×11 sand-rimmed perched pond, glass-capped tower with 2 Rubber Ducky spawners, chest pair at +5 — ONLY the +1 chest has loot (13-entry table, 8-12 stacks); the other is empty (faithful oddity).
- **HauntedHouse (overworld)** — `/locate structure orespawn:haunted_house`: 7×7 plank house with glass clerestory band and east doorway, furnace/crafting-table/chest furniture row, spawner stack Rat/Ghost/Ghost Pumpkin Skelly; chest has 14 fixed slots each present 50% (porkchops/torches/coal/ore salt etc.).
- **EnderKnightDungeon (End + Mining)** — `/locate structure orespawn:ender_knight_dungeon_end` and `..._mining`: cobble dungeon with shelf rooms (28 random shelf sites), 2 floating Ender Knight spawners, chest (3-7 of the 5-entry table). Mining version sits ON the lowest grass surface (not sunk — contrast Basilisk Maze).
- **DSB full sweep** — the Random Dungeon Spawner now produces ALL 50 original outcomes, including: type 5 haunted house, 11 ender knight dungeon, 4/8 bee hives, 6 mantis nest, 25 crystal haunted house, 31/42 King/Queen altars (offset-corrected to the clicked pos), 33 crystal battle tower, 36 greenhouse, 40/43 ponds, 41 white house, 45 round rotator (+1Y), 48/49 hangouts. Royal altars/greenhouse/white house/robot lab DSB builds land exactly at the clicked position (recentring canceled).
- **Regression: greenhouse plants (WGEN-063)** — in any fresh Greenhouse (worldgen or DSB 36): plots roll sugar cane and rice among the crops as in 1.7.10; only 1 roll in 20 leaves a plot empty (never pumpkins).
- **Regression: greenhouse + white house doors (WGEN-067/068)** — Greenhouse entry: TWO full iron doors side by side (facing north) with stone lintels above the flanks and stone buttons on the outside wall; White House entry: FULL 2-tall iron door + outside button that sits ON the wall (not floating).
- **Regression: royal altars + Alien WTF boxes (WGEN-065/066)** — royal altar dirt skirts reach 9 deep and the air-clear reaches +58; the Alien WTF south Part room's far wall is complete (box widenings; worldgen layouts reseed for existing seeds — documented delta).
- **Regression: DSB recentring (ITEM-067)** — a Robot Lab rolled from the Random Dungeon block builds centered at the block position (previously shifted −5/−25), like the altars/greenhouse/white house cases.
- **Regression: bee/mantis chests (ITEM-068/069)** — in a BeeHive (Mining), SmallBeeHive, or MantisNest: chests FACE INWARD (not all north) and loot includes Bee/Mantis SPAWN EGGS (2-8 / 2-4 stacks), never golden carrots or spider eyes standing in.

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

## Failure log

*(appended during the session as FAILs come in)*

- **TEST-001 (FIXED mid-session — launch blocker)** — Client crashed on world creation:
  `IllegalStateException: Failed to load registries` — unbound placed_feature refs
  `orespawn:dragon_spawn_block_dim/_mining` + `orespawn:kraken_spawn_block_dim/_mining`.
  Triage: D5's WGEN-005 SpawnOres slice deleted the four interim dedicated
  dragon/kraken spawn-block placed features (the pool replaced them) but missed their
  references in the `features` arrays of chaos_biome/mining_biome/utopia_plains/
  village_biome. Never caught because gradle builds don't validate datapack registry
  closure and no client ran since D5. Fix applied (blocker rule): the 8 stale refs
  removed from the four biome JSONs; a mechanical scan of biome→placed_feature,
  placed→configured, biome-modifier→placed, set→structure, structure→tag→biome found
  zero other dangling refs. Client relaunched clean.
- **TEST-002 (OPEN — log observation, not yet gameplay-verified)** — Server startup
  logs `RegisterSpawnPlacementsEvent` errors for six OreSpawn entities that have biome
  spawn entries but no placement rules: `spit_bug`, `gamma_metroid`, `island_too`,
  `cliff_racer`, `red_ant`, `the_princess`. Consequence: NO spawn-location
  restrictions (can attempt to spawn midair/in fluids/in light where the original's
  1.7.10 `getCanSpawnHere` had ground/light checks). Triage pending: compare each
  original's canSpawnHere against ModSpawnControl registrations; likely fix is six
  registrations in the RegisterSpawnPlacementsEvent handler. Watch during section C
  spawn tests for these six floating/misplaced. Not fixed mid-session (not a
  blocker).
- **TEST-003 (FIXED mid-session — blocker: crashed Village, would crash Mining)** —
  "Server closed" on travel to the VILLAGE dimension (Rainbow Ant), reproduced twice;
  second repro (without Domestication Innovation — exonerated, jar restored) flushed
  the stack: `IllegalStateException: Requested chunk unavailable during world
  generation` at vanilla `LakeFeature.place` ice-freeze check → `getBiome` →
  `WorldGenRegion.getChunk`. Root cause: vanilla removed classic lakes in 1.19,
  leaving `LakeFeature` with a latent border defect — a lake whose in_square origin
  hugs the +x/+z chunk corner freeze-samples `getBiome(origin+15)`, and BiomeManager's
  zoom fuzz pushes the lookup ~4 more blocks: two chunks out, past the FEATURES
  stage's guaranteed region. The port's classic 1.7.10-style lakes (`lake_water_dim`
  rarity 4 + `lake_lava_dim`, C7 parity) run in Village AND Mining, so Village hit it
  within seconds of first generation (first crash additionally deadlocked the JVM in
  shutdown hooks — jstack in scratchpad — which is why no report file ever wrote).
  Fix applied (blocker rule): `SafeLakeFeature` — line-for-line copy of the decompiled
  vanilla feature (same RNG stream, same writes) with the freeze-check biome SAMPLE
  clamped into the decorated chunk (ice still places at the true position; identical
  result in these single-biome dims). Registered `orespawn:safe_lake`;
  `configured_feature/lake_water.json` re-typed, new `lake_lava.json` clone of the
  vanilla config, `lake_lava_dim.json` re-pointed. Build green.
- **TEST-004 (OPEN — triaged, fix proposed)** — Ant teleport buries the player in
  terrain on FIRST visit to a dimension: `EntityAnt.findSafeY` (EntityAnt.java:164)
  bails to a blind `max(seaLevel+1, 64)` when the destination chunk is not yet
  generated (`!level.hasChunk`), which lands inside stone in terrain-heavy dims.
  Original scanned real blocks because its teleporter force-generated the chunk.
  Proposed fix: `destLevel.getChunk(chunkX, chunkZ)` (synchronously generates) before
  the scan, then drop the hasChunk bail-out. Not a blocker (escapable).
- **TEST-005 (OPEN — triaged, fix proposed)** — WaterDragon is UNSPAWNABLE: its ctor
  throws `IllegalArgumentException: Unsupported mob type for FollowOwnerGoal`
  (WaterDragon.java:84) because vanilla 1.21 FollowOwnerGoal accepts only
  ground/flying navigation and WaterDragon uses water-bound navigation. Every natural
  spawn fails (8× "Failed to create mob" in this session's log before any testing
  began); a WaterDragonLair spawner activating may crash the server thread the same
  way (verify carefully — approach that structure's spawners LAST). Proposed fix: a
  navigation-agnostic follow goal (copy of FollowOwnerGoal without the ctor check, or
  the amphibious variant) matching the original's follow AI.