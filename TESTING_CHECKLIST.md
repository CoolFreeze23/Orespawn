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

- **[AUTOMATED: CrashReproTests#bug003_rat_ai_ticks_and_despawns]** **BUG-003** — `/summon orespawn:rat`, walk >130 blocks away. Expect: no server crash on rat AI tick, rat despawns normally.
- **BUG-004** — Tame a Prince (diamond block), give diamond to grow baby→teen→adult, then quit-to-title and rejoin mid-transformation window. Expect: transformation completes, still tamed to you, no NPE in the log.

### A2 — Items & blocks

- **[AUTOMATED: MiscTests#item011_012_pizza_ducttape_left_click]** **ITEM-011/012** — `/give @s orespawn:pizza_item` (place), `/give @s orespawn:duct_tape` (REWORDED 2026-08-11 per TF-027 — the old `duct_tape_item` give-line handed out the inert twin, and "LEFT-click repairs held gear" was never the 1.7.10 flow). Expect: LEFT-click pizza eats a slice; duct tape is cake-style — right-click the ground to PLACE the tape block, then left- OR right-click the placed tape with the damaged item (count 1) in your MAIN hand: repairs maxDamage/6 (min 1) per click, 6 slices then the block vanishes (orig ItemDuctTape.java:26-66 + BlockDuctTape.java:87-117).
- **[AUTOMATED: MiscTests#item013_014_rtp_mole_dirt]** **ITEM-013/014** — `/setblock ~ ~ ~2 orespawn:block_teleport` then step on it; `/setblock ~ ~ ~2 orespawn:mole_dirt` and walk on. Expect: RTP random-teleports you; mole dirt sinks your feet slightly.
- **[AUTOMATED: MiscTests#item016_crystal_furnace]** **ITEM-016 + C8 furnace** — `/setblock ~ ~ ~2 orespawn:crystal_furnace`; cook with crystal coal, then burn a lava bucket. Expect: 7.5 s cooks; crystal coal ~133 smelts; the EMPTY BUCKET stays in the fuel slot.
- **[AUTOMATED: MiscTests#item019_repellent_cadence]** **ITEM-019** — `/give @s orespawn:kraken_repellent` + `orespawn:creeper_repellent` (place), summon a creeper 15 blocks away. Expect: continuous push every ~0.5 s within radius 20, not once a minute.
- **[AUTOMATED: StructureTestsA#i007_duplicator_log_progressive_growth_and_copy]** **ITEM-027** — Place `orespawn:duplicator_log` on grass with a source block INSIDE the 5×5 copy footprint (≤2 blocks from the trunk — AMENDED 2026-08-11: the old "~5 blocks away" advice put the build out of range, so copying couldn't start). Expect: tree grows block-by-block (one write per random tick — ~12.5 min mean to full tree at randomTickSpeed 3), then ~20 nearby blocks are copied into the 5×5 area.
- **ITEM-037** — `/give @s orespawn:chainsaw`; swing at a mob crowd, then break a log in a big tree (`/setblock` an oak). Expect: 56 AoE dmg r=5 with saw sound; log break crushes an 11×16×11 wood/leaf volume.
- **[AUTOMATED: MiscTests#item040_057_experience_sword_armor_xp]** **ITEM-040 + ITEM-057** — AMENDED 2026-08-11 (the old "armor repairs / sword drains" text was an extraction inference — orig ExperienceSword.java:55-103 repairs and drains NOTHING): with `orespawn:experience_sword` anywhere in inventory and Experience armor worn, a 1-in-60 tick roll grants +1 player XP per piece (helmet 1/10, chest 1/20, leggings 1/30, boots 1/40) with a single portal particle at the piece's height. Trickle rate full set ≈ 4 XP/min; invisible in creative (no XP bar).
- **ITEM-047/048/049** — `/give @s orespawn:instant_garden`, `orespawn:instant_shelter`, `orespawn:step_up`, `orespawn:step_down`, `orespawn:step_across`. Expect: 18×15 garden w/ 8 crops+reeds+melons+3 water channels; 7×7 furnished shelter with 14-item chest; steppers build 8-way from look yaw with extreme torches every 8, stop at obstructions, explosion fx, item kept in creative.
- **[AUTOMATED: MiscTests#item050_052_zookeeper_wrench_kit, MiscTests#item051_sifter_tables]** **ITEM-050/051/052** — `/give @s orespawn:zoo_keeper`, `orespawn:sifter`, `orespawn:wrench`, `orespawn:ant_robot_kit`. Expect: ZooKeeper 1-use persistence; Sifter rolls tables on water/sand/gravel/dirt/grass (mod fish from water!); Wrench refuses healthy unowned AntRobots; kit re-spawns robot with carried-over health/name.
- **[AUTOMATED: RecipeTests#item059_ore_smelts_to_nugget_not_ingot]** **ITEM-059** — Smelt `orespawn:ore_uranium` / `ore_titanium`. Expect: a NUGGET each (9 nuggets → ingot), not an ingot.
- **[AUTOMATED: MiscTests#item001_005_gem_ores_troll_blocks]** **ITEM-001/005** — `/setblock` + break `orespawn:ore_ruby` / `ore_amethyst` (no Silk Touch), then `orespawn:red_ant_troll` / `termite_troll`. Expect: ore never explodes; troll blocks erupt 15-20 mobs even WITH Silk Touch.
- **[AUTOMATED: LootTests#i014_uranium_titanium_xp_gate_mechanism, LootTests#i014_egg_block_xp_mechanism_and_no_duplication, LootTests#i014_i131_break_pops_xp_end_to_end]** **ITEM-003/021** — Break `ore_uranium`/`ore_titanium` placed below y=40 and above; break `orespawn:block_ender_pearl` egg block. Expect: XP only below y=40; egg block pops 5-9 XP ~half the time, never duplicates itself.
- **[AUTOMATED: MiscTests#item029_special_food_effects]** **ITEM-029 (D4)** — Eat `orespawn:butter_candy`, `cooked_bacon`, `crystal_apple`, `heart` ("Love"). Expect: Speed+Jump 100s / Regen+Strength 100s / Regen+Strength 150s / Regen IV+Strength III+FireRes III+Resist II 300s + Speed/Jump 250s.
- **[AUTOMATED: RecipeTests#item060_061_062_d4_crafting_set]** **ITEM-060/061/062 (D4)** — Craft: skate bow (crystal sticks+string), chest + red bed from crystal planks, raw corn dog, bucket from 3 pink tourmaline ingots, cobweb from string. Expect: all five craft; oak door from crystal planks also still works.
- **[AUTOMATED: MiscTests#c8_experience_catcher]** **C8 ExperienceCatcher** — `/give @s orespawn:experience_catcher`; drop XP (`/xp add @s 10 levels`, die, or kill mobs), click the ground under an orb worth ≥3. Expect: ~80% Bottle o' Enchanting + string + stick, catcher consumed; on a miss the catcher drops back at your feet.

### A3 — Projectiles, throwables, dispensers

- **ITEM-063 (D4) dispensers** — `/setblock ~2 ~ ~ minecraft:dispenser` + button; load `orespawn:irukandji_arrow`, `water_ball`, `sunspot_urchin` (item), `acid`, `ice_ball`, `dead_irukandji`, `laser_ball`, then each rock type. Expect: all fire as projectiles (bow sound, arrow is pickup-able); dispensed rocks keep their per-type damage/behavior.
- **ENT-D-025/026/027 + ENT-K-076 rocks** — Throw `orespawn:rock_small`(t1) … `rock_crystal_tnt`(t12); hit glass and mobs. Expect: t5 deals 10; t6, t9-12 apply WEAKNESS (not Wither); t9 ignites ~50 s; block impact shatters glass 3×3×3 with glassdead sound and returns the SAME rock type; entity hits return nothing.
- **ITEM-053** — Throw `orespawn:dead_irukandji` (throwable), `water_ball` (hits drop pickup ~10%), `sunspot_urchin` (lights blocks on fire — also ENT-S-036), spam `laser_ball`/`ice_ball` (no cooldown). Throw `red_heels`/`black_heels`/`slippers`/`boots_shoes`/`game_controller` at mobs. Expect: shoes fly, hit with poof + reddust particles, per-target damage (1.0 on Girlfriend/Boyfriend).
- **[AUTOMATED: EntityLogicTestsA#ultimate_bow_instant_power5_crit_damage]** **ITEM-043 + ENT-S-057** — `/give @s orespawn:ultimate_bow`. Expect: fires instantly (no charge), self-enchants Power 5 (not 10), ~25% crits; full-draw hit ≈ ceil(3×ultimateBowDamage config); halving the config halves damage.
- **[AUTOMATED: EntityLogicTestsA#big_bertha_shockwave_damage_and_pvp]** **ENT-A-045-051 Bertha** — `/give @s orespawn:big_bertha`; swing near mixed mobs incl. a tamed pet and a second player if available (`bigBerthaPvp` config). Expect: swing projectiles one-shot in range (496/746/82 by item); Girlfriend/Boyfriend NEVER hit; players/tamed respect the config.
- **[AUTOMATED: EntityLogicTestsA#mantis_claw_silent_drain]** **ENT-K-033** — `/give @s orespawn:fairy_sword`? (No —) MantisClaw: hit a mob repeatedly. Expect: each hit silently drains 1 extra HP (no second hurt flash) and heals you 1.
- **[AUTOMATED: ConfigGateTests#item058_glide_boots_config_gate]** **ITEM-058** — Wear `orespawn:peacock_boots` and glide off a pillar with `royalGlideEnable=false`; then royal/queen boots with it true. Expect: peacock boots glide regardless; royal/queen only with the flag.

### A4 — Mob behavior & taming

- **[AUTOMATED: EntityLogicTestsA#alien_poison_easy_normal]** **ENT-A-004** — Let an Alien melee you on easy, then normal. Expect: POISON 2 s easy / 1.5 s otherwise — never Hunger.
- **[AUTOMATED: EntityLogicTestsA#antrobot_melee_cadence, EntityLogicTestsA#antrobot_ridden_stomp]** **ENT-A-013/014** — AntRobot melee cadence visibly throttled; ride it near mobs. Expect: occasional ~3.0 stomp damage to nearby mobs while ridden.
- **[AUTOMATED: EntityLogicTestsA#bandp_steals_every_hit_armor_first]** **ENT-A-025** — Let a BandP hit you wearing armor + carrying items. Expect: it steals an item on EVERY successful hit, armor first.
- **[AUTOMATED: EntityLogicTestsA#boyfriend_tempt_panic_door]** **ENT-A-054** — Boyfriend follows held cooked beef, panics when hit, opens wooden doors.
- **ENT-A-074/075** — Damage a CaterKiller with a real sword (>1 net damage through its 19 armor — iron+ qualifies; the health+1<max gate never opens on light creative punches), then stay within 32 blocks and wait ~2 min (2400 server ticks — REWORDED 2026-08-11: trees only gate the SEPARATE ENT-A-075 heal, not the transform; orig CaterKiller.java:438-448 has no tree condition). Expect: transforms into Brutalfly + 10 Butterflies (explosion sound); eats leaves/logs to heal 2.0 with occasional burp.
- **[AUTOMATED: EntityLogicTestsA#cavefisher_hunts_passives]** **ENT-A-080** — CaveFisher near cows/pigs. Expect: hunts passive animals, not only players.
- **[AUTOMATED: EntityLogicTestsA#cephadrome_targets_and_kraken_bonus]** **ENT-A-082** — Cephadrome near Mothra / untamed Leon / GammaMetroid / WaterDragon; hit a Kraken with it. Expect: attacks those targets; Kraken takes 1.5× damage.
- **[AUTOMATED: EntityLogicTestsA#chipmunk_apple_tame_dead_bush_release]** **ENT-A-087** — Right-click Chipmunk with apples. Expect: tames at 50% per apple; dead bush releases it.
- **[AUTOMATED: LootTests#i033_cockateil_variants_and_type5_ruby]** **ENT-A-095/097** — Summon ~10 Cockateils. Expect: random bird types; only type-5 birds can drop rubies (player kill, 1-in-3).
- **[AUTOMATED: EntityLogicTestsA#cryolophosaurus_proactive_hunt]** **ENT-A-112** — Cryolophosaurus + nearby prey animals. Expect: proactively chases prey, not retaliation-only.
- **[AUTOMATED: EntityLogicTestsA#dragon_beef_tame_heal_bone_diamond]** **ENT-D-002/006** — Dragon: raw beef tames/heals (1-in-5), bones ignored; diamond on a TAMED dragon → tamed Spyro replaces the adult.
- **[AUTOMATED: EntityLogicTestsA#emperor_scorpion_baby_flood]** **ENT-D-014** — Emperor Scorpion in combat. Expect: occasionally spawns a baby scorpion midway to its target (uncapped — can flood).
- **[AUTOMATED: EntityLogicTestsA#entity_cage_capture_matrix]** **ENT-D-022** — `/give @s orespawn:cage_empty` ×10. Throw at: yourself (bounces back empty), Creeper (always cages), Ghast/Enderman (~~20% escape), Kraken (~~95% escape), Bat (2 caged bats), Cockateil (4), AttackSquid (6), villager (cage consumed + returned), Iron Golem (cageable), tamed Girlfriend/Boyfriend (eats the cage, no drop).
- **[AUTOMATED: LootTests#i038_gazelle_ostrich_tamed_vs_untamed_kills]** **ENT-D-037 / ENT-K-046** — Kill tamed vs untamed Gazelle and Ostrich. Expect: tamed 2-6 poppies; untamed Gazelle 0-2 beef, untamed Ostrich 0-2 feathers.
- **[AUTOMATED: EntityLogicTestsA#leaf_monster_prey_allowlist_play_nicely]** **ENT-K-013** — LeafMonster among mixed small mobs. Expect: attacks only Ants/Butterflies/LunaMoths/players; never hunts with `playNicely=true`.
- **[AUTOMATED: EntityLogicTestsA#leon_ducky_tame_untame_tempt]** **ENT-K-019/083** — Leon: dead bush untames (glass does NOT); RubberDucky tames/tempts with raw cod (not wheat), unames with dead bush.
- **[AUTOMATED: EntityLogicTestsA#lizard_seeks_water_never_lava]** **ENT-K-023** — Lizard on dry land near water + near lava. Expect: periodically paths to the WATER, never toward lava/fire.
- **[AUTOMATED: EntityLogicTestsA#crystal_apple_breeding_peacock_stinkbug]** **ENT-K-050 / ENT-S-031** — Peacock and StinkBug breed with `orespawn:crystal_apple` ONLY (regular apples do nothing).
- **ENT-K-051** — Summon ~~30 Nightmares. Expect: mostly tiny (t=0.5), big t=4 rare (~~1.5%), hitbox+model grow together up to 10×14; config `nightmareSize=5` forces max.
- **[AUTOMATED: EntityLogicTestsA#purple_power_orb_effects]** **ENT-K-056** — Princess PurplePower orbs (or arena): type 2 poisons, type 3 weakens, 2.5 s each.
- **[AUTOMATED: EntityLogicTestsA#rat_wild_aggression_owner_immunity]** **ENT-K-058** — Wild rat attacks you/pets with default configs; a rat with an owner never attacks its owner.
- **[AUTOMATED: EntityLogicTestsA#sea_viper_poison_duration]** **ENT-S-010** — SeaViper bite. Expect: Poison ~6 s (8 s easy), never Hunger.
- **[AUTOMATED: EntityLogicTestsB#i047_stinkbug_death_nausea_aoe]** **ENT-S-030** — Kill a StinkBug in a crowd (incl. mobs well above it). Expect: NAUSEA (not hunger) on everything nearby.
- **ENT-S-033** — Tamed Stinky idles. Expect: occasional coal burp out the front, skin-matched item fart out the back (blaze powder for skin 0), with sounds.
- **[AUTOMATED: EntityLogicTestsB#i049_velocity_raptor_no_mount_sit_toggle]** **ENT-S-065** — Right-click tamed Velocity Raptor empty-handed. Expect: does NOT mount; shift-click sit toggle still works.
- **[AUTOMATED: EntityLogicTestsB#i050_vortex_no_launch_drag_pull]** **ENT-S-069** — Vortex melee. Expect: victim is never launched skyward; drag pull still works.
- **[AUTOMATED: EntityLogicTestsB#i051_worm_large_gear_theft]** **ENT-S-085 (D4)** — Wear a helmet+chestplate, hold an item, stand near WormLarge. Expect: 1-in-4 steals helmet (chestplate if bare-headed), independent 1-in-4 steals held item; stolen gear scatters as item entities; none of it while `playNicely=true`.
- **[AUTOMATED: EntityLogicTestsB#i052_worm_small_boots_theft_and_pop]** **ENT-S-078 (D4)** — WormSmall at night wearing boots. Expect: 1-in-6 boots theft; worm pops when it surfaces (tall grass counts as air).
- **[AUTOMATED: EntityLogicTestsB#i053_peacock_hunts_termite_and_eggs]** **ENT-K-047/048 (D4)** — Peacock + summoned Termites; then a lone Peacock at `/time set 1000`, y 50-100. Expect: hunts the termite (flat 6.0 hits); lays 1-3 eggs in the first half of the day when ≤2 other peacocks within 16.
- **[AUTOMATED: EntityLogicTestsB#i054_easter_bunny_eggs_and_breeding]** **ENT-D-010 (D4)** — `/summon orespawn:easter_bunny` (summon bypasses the Easter gate), wait ~30 s. Expect: 1-in-600/tick lays a stack of 1-3 random spawn eggs from the 115-type table; breeds with Crystal Apple (NO carrot taming — the audit's carrot claim was wrong).

### A5 — Drop spot-checks (kill each once; creative kills count as player kills)

- **[AUTOMATED: LootTests#i055_c1_drop_tables]** **C1 loot** — Alien (spider eyes/flint/map/clock/compass), AntRobot (redstone jackpot), Beaver (0-2 porkchop), tamed Camarasaurus (2-6 poppies; untamed none), CaveFisher/CliffRacer/CloudShark/Cryolophosaurus/CreepingHorror (gamble drops), Coin (10-slot jackpot — **D4: slot can now yield `coin_spawn_egg`**, ENT-A-098).
- **[AUTOMATED: LootTests#i056_c2_drop_tables]** **C2 loot** — DungeonBeast (25% each: pink ingot/crystal apple/oak log/nothing, ×0-2), Fairy (crystal torch), Firefly (extreme torch), GammaMetroid (5-14 gold nuggets + 6-15 iron), Ghost/GhostSkelly (NOTHING), GiantRobot (~60-116 laser balls + 10-19 kit/component rolls incl. detector rails).
- **[AUTOMATED: LootTests#i057_c3_drop_tables]** **C3 loot** — LeafMonster (log OR leaves OR flesh), LurkingTerror (beef/flint/feather), Rat (rotten flesh), Robot2 (2-9 iron blocks + 5-10 ingots + redstone parts), Robot3/5 (20-40 laser balls + parts), Robot4 (20-56 laser balls + RayGun + painting + parts), Rotator (ONE of pink ingot/tigers-eye ingot/crystal coal/iron).
- **[AUTOMATED: LootTests#i058_c4_drop_tables]** **C4 loot** — Scorpion (~10% each gold/uranium/titanium nugget, often nothing), Skate (string), SpiderRobot (14-27 redstone components), SpitBug (1-3 amethyst), tamed Spyro (1-4 beef; untamed nothing), TerribleTerror (flesh/emerald/feather), tamed VelocityRaptor (2-6 poppies), Vortex (eye + painting + 5-11 mixed), WormSmall (nothing).
- **[AUTOMATED: LootTests#i059_d4_pet_drop_tables_and_stinky_kills, LootTests#i059_lavafoam_xp_overworld_vs_nether]** **D4 drops** — Chipmunk (orig table + poppy only when tamed), GoldFish, RubberDucky, Stinky (beef only when tamed). Lavafoam: break in overworld (0 XP) vs Nether (5-13 XP) — `/setblock ~ ~ ~2 orespawn:lavafoam` in each.
- **[AUTOMATED: LootTests#i060_prince_family_drops]** **BOSS-025/035/042** — ThePrince (1-4 beef), ThePrincess (1-4 beef), ThePrinceAdult (1 `orespawn:prince_egg`) — no diamonds/gold.

### A6 — Immunities

- **[AUTOMATED: EntityLogicTestsB#i061_lava_immunity_basilisk_brutalfly_urchin_vortex]** **ENT-A-031/060, ENT-S-061/068** — Drop Basilisk, Brutalfly, Urchin, Vortex into a lava pit. Expect: no fire damage on any.
- **[AUTOMATED: EntityLogicTestsB#i062_ostrich_cactus_immunity]** **ENT-K-045** — Ostrich pushed into cactus. Expect: no cactus damage; normal damage otherwise.
- **[AUTOMATED: EntityLogicTestsB#i063_kyuubi_fire_immunity]** **ENT-K-007 (D4)** — Kyuubi standing in its own fire attack / lava. Expect: fully fire-immune, no self-damage.
- **[AUTOMATED: EntityLogicTestsB#i064_spitbug_triffid_cactus_fall_immunity]** **ENT-S-025 / ENT-S-047 (D4)** — SpitBug and Triffid vs cactus and long falls. Expect: immune to both.
- **[AUTOMATED: EntityLogicTestsB#i065_fireball_passthrough_acid_vanish]** **ENT-A-052 / ENT-A-001 (D4)** — Brutalfly BetterFireballs pass through Mothra/other fireballs/royalty; acid (spit at TrooperBug/SpitBug) vanishes harmlessly on them.

### A7 — Riding & movement

- **B3 riding** — Mount and fly/ride each: Dragon (no rubber-banding — BUG-020), Leon, Leonopteryx, Cephadrome (feed it first), Ostrich (FAST jump on UP key), tamed PrinceTeen + Adult (strafe keys fire the canon trio). If a second player is available: observed movement is smooth.
- **[AUTOMATED: CoreStatTests#b3_spider_driver_armor_riding_state, CoreStatTests#b3_spider_driver_mounted_bite_poisons]** **B3 SpiderDriver** — armor reads 8 mounted / 20 on foot (`/attribute`); attacks with poison while mounted on SpiderRobot.
- **C8 controls** — Default binds: LEFT ALT (not Space) = fly up / sprint on ridden Dragon/Cephadrome/Ostrich.
- **D2 Hoverboard physics** — `/summon orespawn:elevator` (displays "Hoverboard"): W/S throttle, Left Alt FAST boost, climbs terrain, pitch grows with speed.
- **D2 Hoverboard crash** — Ride into a wall above ~0.75 speed. Expect: shatters into sticks + 2 diamonds.
- **D2 Hoverboard malfunction** — Sustained high speed. Expect: rare malfunction (explosions/smoke, speed bleed, ~2.2 s).
- **D2 Hoverboard skins + guards** — Click with `orespawn:ultimate_sword`: cycles 10 skins. Mob punches can't destroy it while ridden. Hum (`orespawn:hover`, ENT-D-012 — randomized variants, not beacon) only while ridden.
- **[AUTOMATED: MiscTests#d2_hoverboard_registry]** **D2 registry** — Creative tab has exactly ONE Hoverboard entry; `/summon orespawn:hoverboard` FAILS (id is `elevator`; intentional).

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
- **[AUTOMATED: EntityLogicTestsB#i083_hitbox_size_table]** **ENT-A-002/012/017/023/040/060/072/078/091/106 hitboxes** — F3+B on Alien/AntRobot/AttackSquid/BandP/Bee/Brutalfly/CaterKiller/CaveFisher/CloudShark/CreepingHorror vs the size table in `phase_c_reports/C1_entities_A_C.md`; CaterKiller halves with `playNicely=true`.
- **ENT-A-100/101/102/103 crabs** — natural-ish sizes vary (¼/½/full, rare giants; spawner crabs all 0.35); crab walks to water, dry-out damage away from it, scorpion sounds on melee, splash heal in water.

### A9 — Ultimate Rod lava fishing (ENT-S-059, D4)

- **Hook + lava** — `/give @s orespawn:ultimate_fishing_rod`; dig a 3×3 lava pool. Expect: bobber floats IN lava (fire-immune), bite cycle with lava particles works like water fishing.
- **[AUTOMATED: LootTests#i086_fishing_pools_water_and_lava, LootTests#i086_fishing_gear_hook_lifecycle_lava_survival]** **Pools** — Fish ~10 catches (water and lava). Expect: junk / treasure / vanilla fish / OreSpawn fish pools; lava adds lava-fish; caught items fly to you and SURVIVE the lava (LavaLovingItem); gear arrives pre-damaged with a level-30 enchant; XP orb at your feet per catch; rod keeps working (doesn't dismiss the custom hook).

### A10 — Ranged-attack set (D3)

- **ENT-A-055 / ENT-D-049** — Hand Boyfriend/Girlfriend an `orespawn:ultimate_bow`: arrows fly (heal allies if PvP off); without bow: shoes fly. Melee b_fight/o_fight sounds; taunts at 4-7 blocks.
- **[AUTOMATED: EntityLogicTestsB#i088_attacksquid_range_gate]** **ENT-A-018/019** — AttackSquid: ink/water projectiles beyond 3 blocks, melee inside.
- **[AUTOMATED: EntityLogicTestsB#i089_brutalfly_difficulty_fireballs]** **ENT-A-062** — Brutalfly fireball type follows difficulty (Easy small / Hard BetterFireball), heals itself +1 per shot.
- **[AUTOMATED: EntityLogicTestsB#i090_giantrobot_laser_gate]** **ENT-D-044** — GiantRobot fires laser volleys only once its head faces you; slower "special" lasers from >10 blocks.
- **D3 Prince family** — Babies/Princess randomly take off and land; hurt pets <25% HP flee airborne; canon trio fires only in the 5-12 block band while fire is lit; ice block / flint&steel toggle fire with chat messages; teen/adult fly to a distant owner, bite-and-break-off combat, audible wing flaps; **diamond regressions**: DIAMOND item on tamed teen→baby, adult→teen (BOSS-018/020/029: cooked beef heals 80; gold ingot and cake do NOTHING).
- **[AUTOMATED: EntityLogicTestsB#i092_baby_prince_prey_targeting]** **BOSS-024** — Baby Prince hunts Butterflies/Cockateils/Dragonflies/Mosquitoes/Mothra, not just monsters.
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

- **[AUTOMATED: CoreStatTests#b2_boss_health_table_and_caps]** **B2 caps** — the three big ones read 7000 / 6000 / 4000 (not 1024).
- **[AUTOMATED: CoreStatTests#b2_stats_spot_checks, CoreStatTests#b2_boss_health_table_and_caps]** **B2 stats** — spot-check 3-4 mobs: `/attribute @n[type=orespawn:kraken] minecraft:generic.armor base get` vs `phase_b_reports/B2_mobstats.md`.
- **BUG-005** — TheQueen melees a SURVIVAL you to 0 HP: normal death screen/drops/respawn. Bonus: a low-HP mob victim vanishes with no drops (original quirk preserved).
- **[AUTOMATED: CrashReproTests#bug006_godzilla_shockwave_respects_gamemode_invulnerability]** **BUG-006** — Stand at Godzilla's jump landing in Creative and Spectator: NO damage; in Survival the shockwave hits.
- **BOSS-002/007** — F3+B on King and Queen: 22-wide × 24-tall envelope; parts take/route damage (King parent unhittable, Queen parts glued to bones); models not lost inside the box.
- **BOSS-005/012** — Place `orespawn:king_spawner` / `queen_spawner`: ~5 s fuse, spawner + block above turn to air, boss appears 8 blocks up with living sound, stays leashed near spawn. With `theKingEnable=false`/`theQueenEnable=false`: block fizzles, nothing spawns.
- **[AUTOMATED: CoreStatTests#boss006_queen_phase_armor_boost]** **BOSS-006** — Queen below 2/3 HP with <10 player hits takes visibly reduced damage (armor 23 via overridden getter — `/attribute` won't show it).
- **[AUTOMATED: EntityLogicTestsB#i101_queen_dormant_wakeup_damage]** **BOSS-010** — First hit on a dormant (blue) Queen deals normal damage during the 3 s wake-up; she can fight/be hurt during it.
- **BOSS-026/031** — F3+B: PrinceTeen 3.25×4.25, PrinceAdult 6.25×10.25; rider seats correct.
- **BOSS-032** — Tamed idle riderless Adult with `fullPowerKingEnable=true` transforms (diamond-block fast-path) into a King with the "Prepare to die!" sequence; config false → never; King does NOT deal doubled damage from that config.
- **BOSS-036** — PrinceAdult: king_living only while aggro+riderless, king_hit on hurt, trex_death on death.
- **[AUTOMATED: LootTests#i105_boss_drops_kraken_godzilla_queen_mothra_dragon, LootTests#i105_boss_drops_the_king_registry_shower]** **B1 boss drops (Survival kills)** — Kraken (120-279 ink sacs + d53 gear, NO cooked cod), Godzilla (painting/beef/bone, NO emeralds), TheQueen (56× scale/beef/bone/flesh + Princess spawns), TheKing (royal set + 300 random registry items + Prince spawns), Mothra (20-moth burst), Dragon (1-6 beef, no bones). Nothing drops twice.

---

## C. Survival/worldgen sanity (new world or unexplored chunks; survival for spawn tests)

- **C7 vein rates** — `/execute in orespawn:mining run tp @s 0 100 0`, mine at Y10-20: uranium/titanium ≈3× overworld density; salt only ABOVE Y50; amethyst only BELOW Y25.
- **[AUTOMATED: ConfigGateTests#item064_less_ore_vein_ratios]** **ITEM-064** — Set `lessOre=true`, generate NEW chunks: ~1/3 uranium/titanium/amethyst/salt veins, ~1/2 troll blocks.
- **C7 ruby lava-seek** — Overworld cave lakes <Y50: ruby ore in stone directly UNDER lava, never free-floating; Nether has lavafoam veins + occasional nether ruby.
- **C7 Islands terrain** — `/execute in orespawn:islands run tp @s 0 80 0`: flat grass plane at Y7, no carvers/lakes; unstable anthills near the surface.
- **C7 Chaos terrain** — 128-high stone world, grass band ~Y60-65, vanilla sky; chaos roster spawns; no dungeons/towers.
- **C7 structures** — `/locate structure orespawn:beehive` (Mining only), `orespawn:greenhouse`/`robot_lab`/`white_house`/`challenge_tower_king` (Islands only), `orespawn:royal_altars` (Utopia ~45-chunk grid), `minecraft:mineshaft` + `stronghold` resolve in Mining and Village.
- **C7 dungeons** — Utopia lava pools <Y50: ruby-brick dungeon (ruby kit/ThunderStaff chest pool); generic cobble dungeons in Utopia/Mining/Village/Islands — break the spawner block, 12-mob-ladder mob appears after ~20 s (ITEM-020's 400t fuse).
- **C7 Mining lakes/rooms** — caves contain vanilla-style water/lava lakes and monster rooms.
- **C7 termite gate + ant chain (WGEN-049)** — dimension access via the ant chain: `/summon orespawn:ant` (→ Utopia), `orespawn:red_ant` (→ Mining), `orespawn:termite` (→ Crystal), `orespawn:unstable_ant` (→ Islands). Right-click EMPTY-HANDED teleports; clicking the same ant type while already in its dimension returns you to the Overworld; portal cooldown between hops. Termite with items/armor held: "Empty your inventory!" / "Take off your armor!" chat gates, no teleport. A FOLLOWING tamed wolf within 48×24×48 travels along; a SITTING one stays.
- **[AUTOMATED: LootTests#i115_crystal_egg_ore_breaks]** **C7 crystal egg ores** — Crystal dimension sphere shells: breaking oreurchin/orerat/etc. gives block item + occasional 5-9 XP, NO mob spawn.
- **C1 spawns** — Alien/Alosaurus/Camarasaurus/Baryonyx in Mining (Alosaurus/Baryonyx also Utopia), NOT End/overworld; Boyfriend beach hotspots; Bee/CaterKiller/Basilisk/Brutalfly/BandP per-biome.
- **C2 spawns** — Dragon/GoldFish/EnderReaper on Island; DungeonBeast/Flounder/Irukandji/Frog in Crystal; EnderKnight/EnderReaper/Hammerhead/GammaMetroid/DungeonBeast in Chaos; GammaMetroid swarms in Mining; EnderKnight/EnderReaper dark-forest hotspots, NONE in the End; Fairy only dark forest; Girlfriend beach groups 8-15; Hydrolisc swamp/jungle; Frog river/swamp; NO ocean Flounder/GoldFish/Irukandji/Hammerhead/Hydrolisc.
- **C3 spawns** — Kraken/Leon/Leonopteryx/RubyBird never natural; Kyuubi Nether weight doubled; Lizard river/swamp/ocean only; rat swarms in dark forests (10-20) + taigas only.
- **C4 spawns** — TRex only Chaos/Mining; TerribleTerror Island/Chaos; Urchin/Skate/Vortex Crystal(+Chaos); Spyro/VelocityRaptor Mining only; Stinky Nether/badlands/Island; SeaMonster ocean w4 + swamps; WormSmall never natural (day) — **D4: WormSmall spawns at NIGHT only** (ENT-S-078 gate).
- **ENT-A-110** — CreepingHorror naturally only in darkness at night below y=15 (or Chaos).
- **[AUTOMATED: SpawnGateTests#c8_spawn_flags_gate_natural_spawns_not_eggs, SpawnGateTests#c8_kraken_revenge_honors_kraken_enable]** **C8 spawn flags** — Set `krakenEnable=false`, `godzillaEnable=false`, `cowEnable=false`: no natural spawns of those (no AttackSquid revenge Krakens); eggs//summon still work.

---

*(The former "E. Untestable until D5/D6" section is RETIRED — everything in it landed
in D5-D6b and now lives in the C subsections below, except three items that are Phase E
WORK, not tests: ENT-A-083 Cephadrome flight, ENT-A-054 Boyfriend AI remainder, and
ITEM-023 ZooCage block form — see phase_d_rollup.md §3. WGEN-003/004/007 are also
Phase E features and not testable yet.)*

## C-D5 — structures + SpawnOres pool (D5, 2026-08-08)

### D5a — Mining dimension

- **[AUTOMATED: StructureTestsA#i122_basilisk_maze_content_and_sink]** **WGEN-037** — `/execute in orespawn:mining run tp @s 0 100 0`, then `/locate structure orespawn:basilisk_maze` and tp there. Expect: sandstone step-pyramid marker sunk ~2 blocks into terrain; 4×4 bedrock shaft with 2×2 core and spiral obsidian parkour steps descending 20-29 blocks; iron-ore-walled antechamber with 3 Extreme Torches; 30×30 obsidian maze (2-wide corridors, solvable west→east, lava traps in the floor); 30×30 taller chamber with 3 persistent Basilisks, RTP floor traps, 2-4 chests on the east wall (torch 3 above each) rolling diamonds 15-25 / cages / uranium / fish / ultimate gear per the 31-entry table.
- **[AUTOMATED: EntityLogicTestsB#i123_maze_basilisk_persistence]** Walk >130 blocks from the maze chamber and return: the 3 Basilisks must NOT have despawned (persistence).

### D5b — Islands dimension

- **WGEN-042 (rookery)** — `/execute in orespawn:islands run tp @s 0 20 0`, `/locate structure orespawn:nightmare_rookery`. Expect: cluster of jagged stone spires in two wandering ridges; roughly 1-in-10 spires truncate at 19 blocks with a chest on top and a Nightmare (Pitch Black) spawner sitting directly on the chest; chests roll dead stink bugs / black+scary flowers / nightmare eggs / robot kits / bones / string / flesh / XP bottles (4-8 stacks).
- **WGEN-051/055** — `/locate structure orespawn:challenge_tower_king` (and _queen): tower heights VARY (most towers stop below 6 floors; full towers with the Nightmare cap ≈ 28%); towers sit on the grass plane (not floating on a heightmap ledge); with `lessLag=1` roughly half as many towers/rookeries generate in fresh chunks.
- **[AUTOMATED: StructureTestsA#i126_challenge_tower_level6_prizes]** **WGEN-052/053/056 + ITEM-066** — inside any tower floor room: NO scaffolding anywhere; all 4 chests face the room centre; non-prize floors roll the faithful tier lists (emerald kit / experience+pink / amethyst+tigers-eye / ruby+utility / the 83-spawn-egg jackpot at level 5). On a level-6 tower's BOTTOM floor: west chest = The Prince (King) / The Princess (Queen) SPAWN EGG that actually spawns the royal, east = Royal/Queen helmet+chestplate, north = leggings+boots, south = Royal Guardian Sword; 4 RTP blocks around the central spawner column.
- **[AUTOMATED: StructureTestsA#i127_tower_centre_rooms_spawn_jumpy_bug]** **WGEN-054** — level-2+ tower centre rooms at the right difficulty spawn Jumpy Bugs (TrooperBug — jumps, no spit attack).
- **WGEN-055 (worm ring)** — around a FULL-height tower, dig at surface−1 out to ~55 blocks east/±55 north-south of the base: buried Large Worm spawners scattered outside the castle, with no rectangular cutoff at chunk borders.
- **[AUTOMATED: DsbOutcomeTests#dsb_item020_towers_maze_rookery, DsbOutcomeTests#dsb_fuse_400_ticks]** **ITEM-020** — place 4+ Random Dungeon Spawner blocks; observed outcomes should now include the King tower (2), Basilisk maze (23), rookery (38), Queen tower (47) at the block position.

### D5c — SpawnOres + recipes (overworld / Utopia / Village / Chaos / Mining)

- **WGEN-005** — fly through NEW chunks at Y50+ mining into stone: spawn-ore veins (clump ~4) of many different mobs appear frequently (~24/chunk overworld, ~27 dims); Mining dim noticeably denser (×3); Islands and Crystal (other than its own 11 egg ores) get NONE; Nether/End get none.
- **[AUTOMATED: LootTests#i131_spawn_ore_breaks, LootTests#i014_i131_break_pops_xp_end_to_end]** Break any spawn ore: drops itself; ~50% of breaks pop 5-9 XP; Silk Touch: block, no XP; nothing ever spawns from breaking.
- **[AUTOMATED: MiscTests#d5c_spawn_ore_lang]** Lang: every spawn ore reads "Ancient Dried <Mob> Spawn Egg" (incl. the renamed 11 crystal egg ores + kraken/dragon).
- **[AUTOMATED: RecipeTests#item062_all_116_water_bucket_egg_conversions, RecipeTests#item062_nine_part_combines_and_pool_asymmetry]** **ITEM-062** — craft water bucket + spawn block for a sample across the table (spider→vanilla spider egg, wither boss→wither egg, criminal→BandP egg, enchanted cow→Enchanted Apple Cow egg, kraken→Kraken egg, urchin ore→Urchin egg): 1 egg + EMPTY BUCKET returned. 9× Mobzilla/King/Queen part blocks → full egg block → + water → boss egg. Mobzilla FULL egg blocks also mine from worldgen; King/Queen full blocks never do (parts only).
- **[AUTOMATED: ConfigGateTests#item064_less_ore_spawn_ores_pool_ratio]** **LessOre=1** — new chunks: spawn-ore veins cut to ~1/3.
- **[AUTOMATED: MiscTests#d5c_interim_content_gone]** Confirm the interim content is GONE: no `orespawn:ancient_dried_egg` block anywhere (worldgen or creative tab), and dragon/kraken spawn blocks now appear via the pool at Y50+ (not as deep single blocks).

---

## C-D6a — strong-model structures (D6a, 2026-08-08)

- **EnderCastle (End)** — `/execute in minecraft:the_end run tp @s 100 70 0`, `/locate structure orespawn:ender_castle_end`. Expect: 29x29 obsidian-plate castle on end stone (central OR outer islands — PN-017), 4 spiral-stair corner towers, rooftop lava pool + dragon-egg pedestal, Ender Knight/Reaper rooftop spawner pairs + pit + CaveFisher alcoves, 3 alcove chests (facing inward) rolling the ender/experience-catcher table (6-10 stacks), trophy ender chest EMPTY (plain block).
- **[AUTOMATED: StructureTestsA#i137_ender_castle_islands_grass_anchor]** **EnderCastle (Islands)** — `/locate structure orespawn:ender_castle_islands` in orespawn:islands: same castle at grass level.
- **[AUTOMATED: StructureTestsA#i138_inca_pyramid_content]** **IncaPyramid (Islands)** — `/locate structure orespawn:inca_pyramid`: 41x31 stepped pyramid, 4 torch-ended ramps (support pillars reach the ground — PN-018), lit-lamp temple checkerboard, 5 water altars, 4 Creeper Repellents, Molenoid spawner, trapdoor + ladder shaft, 24-grave graveyard (~1/3 with Ghost spawners, poppy/dandelion/poppy beds), all chests roll 10-14 stacks of the 480-weight table.
- **[AUTOMATED: StructureTestsA#i139_kyuubi_dungeon_content]** **KyuubiDungeon (Mining)** — `/locate structure orespawn:kyuubi_dungeon`: sealed surface hut (enter via the 1x1 roof hole), 22-deep shaft with water brake, lava-walled corridor, boss room with altar + 4-tier ziggurat, 8-Blaze spawner ring, kyuubi chest (7-13 stacks) + 4 wall chests with DIFFERENT fill counts (4-8/3-7/5-9/6-10) incl. blaze-egg loot entries.
- **[AUTOMATED: StructureTestsA#i140_robot_lab_content_and_redstone]** **Robot Lab (Islands)** — freshly generated lab: BOTH rear sniper spawners exist behind the hangar wall; altar spawns Robo-Pounder, treasure room Robo-Warrior, pillars Robo-Sniper; railway has powered (golden) rails + unpowered floor levers; assembly-line sticky pistons face south under white carpet and are CRUSHING (their lever generates powered); entry doors face NORTH and open with the wall buttons; chests roll 10-14 of the 755-weight table (minecarts, kits, Ray Gun — no droppers/dispensers/clocks).
- **[AUTOMATED: StructureTestsA#i141_hospital_content]** **Hospital (End)** — `/locate structure orespawn:hospital`: 10x10 iron-bar cage, 4 End Crystals on bedrock caps (NO dragon), 8 spawners, chest at the corner (6-10 of the 210 table).
- **[AUTOMATED: StructureTestsA#i142_monster_island_content]** **MonsterIsland (overworld)** — `/locate structure orespawn:monster_island` over PLAIN ocean only (not deep/frozen/warm — PN-019): floating lens island in the water surface, canopy tree, 4 spawners all of ONE randomly picked mob, 2 chests (4-8 of the 450 table).
- **[AUTOMATED: DsbOutcomeTests#dsb_d6a_strong_models]** **DSB outcomes** — Random Dungeon Spawner can now also produce: fairy tree (0), fairy castle tree (1), kyuubi dungeon (7), hospital (24), ender castle (27), inca pyramid (29), robot lab (30), monster island (37) at the block.
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

- **[AUTOMATED: StructureTestsA#i145_play_pool_content]** **PlayPool (overworld ocean)** — `/locate structure orespawn:play_pool`: floating platform 16 blocks above the sea with 4 Attack Squid spawners, twin chests (only the x+1 chest has loot: ink/squid-zooka/gold-nugget/flesh, 3-7 stacks), water channel with flowing caps.
- **[AUTOMATED: StructureTestsA#i146_cloud_shark_dungeon_sky_band]** **CloudSharkDungeon (Islands sky)** — `/locate structure orespawn:cloud_shark_dungeon` then fly to Y150-159: tiny glowstone cluster with 4 Cloud Shark spawners + 1 chest (4-8 stacks incl. experience tree seeds).
- **[AUTOMATED: StructureTestsB#i147_gold_fish_bowl_content]** **GoldFishBowl (overworld ocean, WGEN-042)** — `/locate structure orespawn:gold_fish_bowl`: glass bowl on the surface: sand bed, water fill, 4 glowstone corners, Gold Fish spawner, open wall-top ring and unwritten base ring (faithful oddities), NO chest.
- **[AUTOMATED: StructureTestsB#i148_spit_bug_lair_content_and_biome]** **SpitBugLair (swamp, WGEN-042)** — `/locate structure orespawn:spit_bug_lair`: emerald-ore antenna, 3 Spit Bug spawners, loot chest; only in plain swamp (not mangrove).
- **[AUTOMATED: ConfigGateTests#wgen042_urchin_spawner_enable_gate]** **UrchinSpawner (Crystal, WGEN-042)** — set `urchinEnable=false`, fly fresh Crystal chunks: no new urchin spawners generate.
- **[AUTOMATED: DsbOutcomeTests#dsb_d6b_mechanical]** **RotatorStation / other DSB outcomes** — Random Dungeon Spawner can now produce types 3/12/13/14/15/16/17/18/19/20/34 (rotator station, play pool, water-dragon lair, cloud cluster, leaf tower, mini dungeon, gold fish bowl, graveyard, spit-bug lair, igloo, cephadrome altar) at the block.
- **[AUTOMATED: StructureTestsB#i151_ender_reaper_graveyard_content]** **EnderReaperGraveyard (End, WGEN-042)** — `/locate structure orespawn:graveyard` in the End: roofless iron-bar cage, 8 obsidian graves with flush chests (eye/poppy/dandelion/pearl 3-5 stacks), 4 Ender Reaper spawners; foundation skirt fills only air below (End terrain preserved).
- **[AUTOMATED: StructureTestsB#i152_water_dragon_lair_content]** **WaterDragonLair (ocean, WGEN-042)** — `/locate structure orespawn:water_dragon_lair`: floating polar-math disc with iron annulus, lapis/spawn-egg rim courses, canopy tree, 4 Water Dragon spawners, chest (4-8 of the 145 table).
- **[AUTOMATED: StructureTestsB#i153_leaf_monster_dungeon_content]** **LeafMonsterDungeon (plains, WGEN-042)** — `/locate structure orespawn:leaf_monster_dungeon`: log tower with leaf crown, 4 Leaf Monster spawners, chest pair (one filled, 12-16 stacks), foundation roots leave the grass threshold intact.
- **[AUTOMATED: StructureTestsB#i154_mini_dungeon_content]** **MiniDungeon (Islands, WGEN-042)** — `/locate structure orespawn:mini_dungeon`: spawner ring at j=9 (12 spawners), corner caps, floor spawners, chest.
- **[AUTOMATED: StructureTestsB#i155_cephadrome_altar_content]** **CephadromeAltar (Islands, WGEN-042)** — `/locate structure orespawn:cephadrome_altar`: altar per spec with its spawners.
- **[AUTOMATED: DsbOutcomeTests#dsb_igloo_detail]** **Igloo (WGEN-042 / WGEN-071)** — DSB type 20, and as of the E3 WGEN-071 fix ALSO natural generation: `/locate structure orespawn:igloo` in snowy_plains — igloos surface only on snow-BLOCK columns (in practice near ice_spikes borders; the double mechanical gate reproduces the original's border-artifact rarity, spec §7.3). Roll `/setblock ~2 ~ ~ orespawn:random_dungeon_block` until an igloo appears (or verify via other outcomes that 20 exists): snow/ice dome, west oak door, Rat/Ghost/Ghost-Pumpkin-Skelly spawners, north-facing chest with 16 independent 50% fixed items; at (+,+) build coordinates the apex has a 1x1 skylight, at negative coordinates it closes (faithful float quirk).
- **[AUTOMATED: StructureTestsB#i157_bouncy_castle_content]** **BouncyCastle (desert)** — `/locate structure orespawn:bouncy_castle`: lavafoam (bouncy, friction 1.1) castle sunk to the sand surface, 9 spawners (Silverfish/Rat/Scorpion mix), north-facing chest at (+3,+3,+3) with 6-10 stacks (cod/poppy/dandelion/pearl table, weight 180).
- **[MOSTLY AUTOMATED — manual: Village LEVEL-existence — the orespawn:village dimension must load on a real (non-GameTest) server; rest in StructureTestsB#i158_damsel_in_distress_content]** **DamselInDistress (Village dim)** — `/locate structure orespawn:damsel_in_distress` in the Village dimension: 9×9 mossy-decay cottage with front gable, 7×4 iron-bar jail wall, 2 Scorpion spawners, north-facing chest (10-14 stacks: iron tools + foods), and a live Girlfriend standing in the jail cell (persistent by entity class — she must NOT despawn when you leave and return).
- **[AUTOMATED: StructureTestsB#i159_girlfriend_island_twin_and_content]** **GirlfriendIsland (ocean)** — `/locate structure orespawn:girlfriend_island`: MonsterIsland's twin island/tree geometry, spawners Girlfriend/Boyfriend/Gold Fish×2 (fixed, no random pick), TWO chests each with 4-8 stacks of the damsel food/tool table.
- **[AUTOMATED: StructureTestsB#i160_stinky_house_content_and_stats]** **StinkyHouse (Islands)** — `/locate structure orespawn:stinky_house`: 25×17 fenced yard (1/3 fence gaps, 1/10 bushes, never overwrites yard air), 13×10 house with corner-adjacent window panes and 1/10 wall decay, doorway force-cleared, Stink Bug + Stinky spawners, chest (8-12 stacks, weight-215 table incl. both stink eggs).
- **[AUTOMATED: StructureTestsB#i161_pumpkin_content]** **Pumpkin (Islands)** — `/locate structure orespawn:pumpkin`: 14-wide orange-terracotta jack-o'-lantern floating one block above grass, 48-cell carved face (eyes/nose/mouth) on the −z wall, green stem, interior plank candle with netherrack cap, twin fires, 2 Ghost Pumpkin Skelly spawners, NO chest.
- **[MOSTLY AUTOMATED — manual: SKY_BAND_70 anchor — rainbow must generate at Y70-89 in the real orespawn:islands generator; rest in StructureTestsC#rainbow_islands_sky_i162]** **Rainbow (Islands sky)** — `/locate structure orespawn:rainbow` then fly to Y70-89: 8 nested wool arches (red innermost → pink outermost) threading a raining cloud slab, 6 Cloud Shark spawners, roof double chest — BOTH halves filled (10-14 stacks each, weight-150 table incl. magic apple).
- **[AUTOMATED: DsbOutcomeTests#dsb_batch3_outcomes]** **DSB batch-3 outcomes** — Random Dungeon Spawner can additionally produce types 26/28/35/39/44/46 (bouncy castle, damsel cottage, girlfriend island, stinky house, pumpkin at +1Y, rainbow) at the block.
- **[MOSTLY AUTOMATED — manual: spiderDriverEnable worldgen-gate check — positive control needs the real Village generator; rest in StructureTestsC#spider_hangout_village_i164]** **SpiderHangout (Village dim)** — `/locate structure orespawn:spider_hangout`: 20×20 gravel pad on a stone slab, 12 Spider Driver spawners in 3-high corner columns, one persistent Robot Spider at the pad centre (no spawn sound), NO chest. With `spiderDriverEnable=false` no new hangouts generate (the Dungeon Spawner Block can still build one — faithful).
- **[AUTOMATED: StructureTestsC#red_ant_hangout_village_i165]** **RedAntHangout (Village dim)** — `/locate structure orespawn:red_ant_hangout`: 16×16 gravel pad with four 3×3 red-ant-block corner pads, one persistent UNOWNED Robot Red Ant at centre (wrench-claim per its item flow), no spawners, no chest.
- **[AUTOMATED — red: TF-025 pending verdict]** **FrogPond (plains, WGEN-042)** — `/locate structure orespawn:frog_pond`: 7×7 still-water sheet sunk at the grass line, Frog spawner, centre riser with flowing cross, lily-pad cross above.
- **[AUTOMATED: ConfigGateTests#wgen064_disable_overworld_dungeons_gate]** **DisableOverworldDungeons gate (WGEN-064)** — set `disableOverworldDungeons=true`, fly fresh overworld chunks: NONE of the 11 overworld dungeon types generate (ponds, haunted house, bouncy castle, leaf tower, spit-bug lair, fish bowl, islands, play pool); `/setblock` a Random Dungeon block: outcomes still build (DSB never gated — faithful).
- **[AUTOMATED: StructureTestsC#rubber_ducky_pond_plains_i168]** **RubberDuckyPond (plains)** — `/locate structure orespawn:rubber_ducky_pond`: 12×11 sand-rimmed perched pond, glass-capped tower with 2 Rubber Ducky spawners, chest pair at +5 — ONLY the +1 chest has loot (13-entry table, 8-12 stacks); the other is empty (faithful oddity).
- **[AUTOMATED: StructureTestsC#haunted_house_overworld_i169]** **HauntedHouse (overworld)** — `/locate structure orespawn:haunted_house`: 7×7 plank house with glass clerestory band and east doorway, furnace/crafting-table/chest furniture row, spawner stack Rat/Ghost/Ghost Pumpkin Skelly; chest has 14 fixed slots each present 50% (porkchops/torches/coal/ore salt etc.).
- **[MOSTLY AUTOMATED — manual: LOWEST_GRASS_36 Mining no-sink anchor — needs the real orespawn:mining generator; rest in StructureTestsC#ender_knight_dungeon_i170]** **EnderKnightDungeon (End + Mining)** — `/locate structure orespawn:ender_knight_dungeon_end` and `..._mining`: cobble dungeon with shelf rooms (28 random shelf sites), 2 floating Ender Knight spawners, chest (3-7 of the 5-entry table). Mining version sits ON the lowest grass surface (not sunk — contrast Basilisk Maze).
- **[AUTOMATED: DsbOutcomeTests#dsb_full_sweep_all_50]** **DSB full sweep** — the Random Dungeon Spawner now produces ALL 50 original outcomes, including: type 5 haunted house, 11 ender knight dungeon, 4/8 bee hives, 6 mantis nest, 25 crystal haunted house, 31/42 King/Queen altars (offset-corrected to the clicked pos), 33 crystal battle tower, 36 greenhouse, 40/43 ponds, 41 white house, 45 round rotator (+1Y), 48/49 hangouts. Royal altars/greenhouse/white house/robot lab DSB builds land exactly at the clicked position (recentring canceled).
- **[AUTOMATED: StructureTestsC#greenhouse_plants_regression_i172]** **Regression: greenhouse plants (WGEN-063)** — in any fresh Greenhouse (worldgen or DSB 36): plots roll sugar cane and rice among the crops as in 1.7.10; only 1 roll in 20 leaves a plot empty (never pumpkins).
- **[AUTOMATED: StructureTestsC#greenhouse_white_house_doors_i173]** **Regression: greenhouse + white house doors (WGEN-067/068)** — Greenhouse entry: TWO full iron doors side by side (facing north) with stone lintels above the flanks and stone buttons on the outside wall; White House entry: FULL 2-tall iron door + outside button that sits ON the wall (not floating).
- **[AUTOMATED: StructureTestsC#royal_altars_alien_wtf_boxes_i174]** **Regression: royal altars + Alien WTF boxes (WGEN-065/066)** — royal altar dirt skirts reach 9 deep and the air-clear reaches +58; the Alien WTF south Part room's far wall is complete (box widenings; worldgen layouts reseed for existing seeds — documented delta).
- **[AUTOMATED: DsbOutcomeTests#dsb_recentring_robot_lab]** **Regression: DSB recentring (ITEM-067)** — a Robot Lab rolled from the Random Dungeon block builds centered at the block position (previously shifted −5/−25), like the altars/greenhouse/white house cases.
- **[AUTOMATED: StructureTestsC#bee_mantis_chests_i176]** **Regression: bee/mantis chests (ITEM-068/069)** — in a BeeHive (Mining), SmallBeeHive, or MantisNest: chests FACE INWARD (not all north) and loot includes Bee/Mantis SPAWN EGGS (2-8 / 2-4 stacks), never golden carrots or spider eyes standing in.

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

- **[AUTOMATED: DateGateTests#halloween_seasonal_biome_gate, DateGateTests#halloween_year_round_biomes]** **ANIM-016 Halloween (closes ENT-D-039/041)** — Set clock to **Oct 31**, night, in a plains/forest (survival, mob spawning on). Expect: Ghost + GhostSkelly natural spawns across the 22-biome list; back on a normal date they spawn ONLY in the 5 year-round biomes (snowy taiga, taiga, frozen river, jungle, dark forest). Drop: nothing (C2).
- **ANIM-016 Valentine's (ENT-D-011-family)** — Set clock to **Feb 14**, `/summon orespawn:girlfriend` (fresh!). Expect: GIANT (2.5×8, renders 5×) angry Girlfriend, 800 HP (`/data get`), `girlfriendv` texture, attacks players and Boyfriends (not her owner/pets), immune to inWall damage, `o_hurt` ambient. Hit her with `orespawn:rose_sword`: each hit drops a "Love"; ~1-in-4 cures her (shrinks, calms, extra Love shower). Thrown shoes deal **10** damage today (ITEM-053).
- **[AUTOMATED: DateGateTests#easter_bunny_gate]** **ANIM-016 Easter (closes ENT-D-011)** — Set clock to **Apr 20**. Expect: EasterBunny NATURAL spawns appear (any other date: none — verify with a spawn-friendly area first). Egg-laying itself is date-independent (tested in A4 via /summon).

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
- **TEST-002 (RESOLVED 2026-08-11, E6 — the six + two siblings registered;
  WIDER SWEEP QUEUED: the actual startup error lists 38 entities, see the
  E8 boundary report)** — Server startup
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
- **TEST-004 (FIXED 2026-08-11 — user-ruled final pre-beta code change; covered
  by MiscTests#test004_ant_teleport_first_visit_safe_y, which force-generates a
  never-visited chunk and asserts the landing contract)** — Ant teleport buries the player in
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

---

## SHORTEST MANUAL SESSION (post-automation)

Everything NOT listed here is covered by the 144-test GameTest suite (see the
per-item **[AUTOMATED: ...]** marks above; suite state 2026-08-10: 142 green,
2 red — frog_pond_plains_i166 / TF-025 pending verdict, plus the temporary
tf025_diag_frog_pond_cascade diagnostic). Below is ONLY what still needs human
eyes, grouped for one sitting and ordered for minimal world/dimension
switching. Item texts are copied verbatim from the sections above (setup
commands, expected behavior, finding ids); trailing *(iNNN)* tags give the
testing_session item id.

### (a) Creative flatworld — visual / feel / audio (one superflat sitting)

Use the "Session setup (once)" commands at the top of this file. Everything
here is judged by eye, ear, or feel in a real client — the mechanics halves are
already asserted headlessly where possible.

- **[FIXED-IN-CODE — visual confirmation pending]** **BUG-004** — Tame a Prince (diamond block — `/summon orespawn:the_prince`, tame: right-click with DIAMOND BLOCK, instant tame + full heal); the tame maxes the growth counters, so the baby transforms to a teen instantly — no separate diamond feed (faithful to 1.7.10; TF-024). Quit-to-title and rejoin mid-transformation window. Expect: instant baby→teen transform completes, still tamed to you, no NPE in the log. *(i002 — text as amended per TF-024; FAILED 2026-08-10, see recheck list in (e))*
- **[FIXED-IN-CODE — visual confirmation pending]** **ITEM-037** — `/give @s orespawn:chainsaw`; swing at a mob crowd (e.g. `/summon minecraft:cow` a few times), then break a log in a big tree (`/setblock` an oak — e.g. `/setblock ~2 ~ ~2 minecraft:oak_log`, LEFT-click it with the chainsaw). Expect: 56 AoE dmg r=5 with saw sound; log break crushes an 11×16×11 wood/leaf volume. *(i008 — manual for the saw sound; FAILED 2026-08-10, see (e))*
- **[FIXED-IN-CODE — visual confirmation pending]** **ITEM-047/048/049** — `/give @s orespawn:instant_garden`, `orespawn:instant_shelter`, `orespawn:step_up`, `orespawn:step_down`, `orespawn:step_across` (right-click the ground to deploy each). Expect: 18×15 garden w/ 8 crops+reeds+melons+3 water channels; 7×7 furnished shelter with 14-item chest; steppers build 8-way from look yaw with extreme torches every 8, stop at obstructions, explosion fx, item kept in creative. *(i010 — manual for the explosion fx; FAILED 2026-08-10, see (e))*
- **ITEM-063 (D4) dispensers** — `/setblock ~2 ~ ~ minecraft:dispenser` + button; load `orespawn:irukandji_arrow`, `water_ball`, `sunspot_urchin` (item), `acid`, `ice_ball`, `dead_irukandji`, `laser_ball`, then each rock type (`orespawn:rock_small`, `rock`, `rock_red`, `rock_green`, `rock_blue`, `rock_purple`, `rock_spikey`, `rock_tnt`, `rock_crystal_red`, `rock_crystal_green`, `rock_crystal_blue`, `rock_crystal_tnt` — all `/give`-able). Expect: all fire as projectiles (bow sound, arrow is pickup-able); dispensed rocks keep their per-type damage/behavior. *(i018 — manual for the bow sound as heard)*
- **[FIXED-IN-CODE — visual confirmation pending]** **ENT-D-025/026/027 + ENT-K-076 rocks** — Throw `orespawn:rock_small`(t1) … `rock_crystal_tnt`(t12) (throw = right-click AT AIR; tier order: t1 `rock_small`, t2 `rock`, t3 `rock_red`, t4 `rock_green`, t5 `rock_blue`, t6 `rock_purple`, t7 `rock_spikey`, t8 `rock_tnt`, t9 `rock_crystal_red`, t10 `rock_crystal_green`, t11 `rock_crystal_blue`, t12 `rock_crystal_tnt`); hit glass and mobs. PLACEMENT NOTE (i019 verdict, CLOSED FAITHFUL 2026-08-11): clicking ON a block within reach PLACES a pet Rock entity instead of throwing — even into obstructed spaces — exactly as in 1.7.10 (orig ItemRock.java:75-128, no clearance check; always-throw preference → MOD-018). The in-flight INVISIBILITY of the thrown rock is the separate i018 renderer item, handled there. Expect: t5 deals 10; t6, t9-12 apply WEAKNESS (not Wither); t9 ignites ~50 s; block impact shatters glass 3×3×3 with glassdead sound and returns the SAME rock type; entity hits return nothing. *(i019 — manual for the glassdead sound)*
- **[FIXED-IN-CODE — visual confirmation pending]** **ITEM-053** — Throw `orespawn:dead_irukandji` (throwable), `water_ball` (hits drop pickup ~10%), `sunspot_urchin` (lights blocks on fire — also ENT-S-036), spam `laser_ball`/`ice_ball` (no cooldown). Throw `red_heels`/`black_heels`/`slippers`/`boots_shoes`/`game_controller` at mobs. Expect: shoes fly, hit with poof + reddust particles, per-target damage (1.0 on Girlfriend/Boyfriend). *(i020 — manual for the poof + reddust particles)*
- **ENT-A-074/075** — CLOSED FAITHFUL (2026-08-11): the i029 FAIL was procedural, not a port defect — the port transform is a line-for-line replica of orig CaterKiller.java:438-448 (health+1<max gate, ticker>2400, Brutalfly +4y, explosion, 10 Butterflies, discard; verified against decompiled NeoForge Mob.serverAiStep — customServerAiStep fires every server tick), and the old "near trees" wording wrongly implied the transform needs trees (that's the separate ENT-A-075 heal, orig :502-530). RETEST PROTOCOL: `/summon orespawn:cater_killer` on any world (superflat fine); hit it once with a real SWORD (>1 net damage after its 19 armor — any iron+ sword qualifies; bare-fist creative punches net ~0.25 and never open the gate); STAY WITHIN 32 BLOCKS and DON'T DIE (despawn/out-of-range while dead is how the 2026-08-11 session missed it at /tick rate 10000); expect explosion + 1 Brutalfly + 10 Butterflies after 2400 server ticks (exactly 2 min at rate 20, seconds at high tick rate). Manual half remaining: the burp/explosion sounds as heard. *(i029)*
- **[KNOWN-UNVERIFIED]** **ENT-K-051** — Summon ~~30 Nightmares (`/summon orespawn:pitch_black` — the Nightmare's registry id; config in `runs/client/config/orespawn-common.toml`). Expect: mostly tiny (t=0.5), big t=4 rare (~~1.5%), hitbox+model grow together up to 10×14; config `nightmareSize=5` forces max. *(i043 — manual for the model growing with the hitbox)* — If broken, a player would notice: big Nightmares could keep a tiny model (or a tiny one a huge hitbox) — model and hitbox could visibly disagree as they grow.
- **[KNOWN-UNVERIFIED]** **ENT-S-033** — Tamed Stinky idles (`/summon orespawn:stinky`; tame: right-click with RAW BEEF, 1-in-2 per beef). Expect: occasional coal burp out the front, skin-matched item fart out the back (blaze powder for skin 0), with sounds. *(i048 — manual for the sounds)* — If broken, a player would notice: a tamed Stinky could idle silently — no coal burps out the front, no item farts out the back, no sounds.
- **[FIXED-IN-CODE — visual confirmation pending]** **Hook + lava** — `/give @s orespawn:ultimate_fishing_rod`; dig a 3×3 lava pool. Expect: bobber floats IN lava (fire-immune), bite cycle with lava particles works like water fishing. *(i085 — manual for the lava bite particles)*
- **[KNOWN-UNVERIFIED]** **ENT-A-055 / ENT-D-049** — Hand Boyfriend/Girlfriend an `orespawn:ultimate_bow` (`/summon orespawn:boyfriend` / `orespawn:girlfriend`; tame first — Boyfriend: COOKED BEEF 1-in-3, Girlfriend: POPPY 1-in-3; hand the bow: right-click the tamed mob while HOLDING the bow — they equip it and swap back anything they held; EMPTY-HAND right-click takes it back): arrows fly (heal allies if PvP off); without bow: shoes fly. Melee b_fight/o_fight sounds; taunts at 4-7 blocks. *(i087 — manual for the sounds/taunts as heard)* — If broken, a player would notice: Girlfriend/Boyfriend could fight in silence — no b_fight/o_fight melee sounds and no taunts at 4-7 blocks.
- **[KNOWN-UNVERIFIED]** **D3 Prince family** — (ids: `/summon orespawn:the_prince` baby / `the_princess` / `the_prince_teen` / `the_prince_adult`; tame any: right-click with DIAMOND BLOCK, instant; mount teen/adult: EMPTY-HAND right-click when tamed) Babies/Princess randomly take off and land; hurt pets <25% HP flee airborne; canon trio fires only in the 5-12 block band while fire is lit; ice block / flint&steel toggle fire with chat messages (ICE BLOCK = off, FLINT AND STEEL = on); teen/adult fly to a distant owner, bite-and-break-off combat, audible wing flaps; **diamond regressions**: DIAMOND item on tamed teen→baby, adult→teen (BOSS-018/020/029: cooked beef heals 80; gold ingot and cake do NOTHING). *(i091 — manual for audible wing flaps + distant-owner flight)* — If broken, a player would notice: Prince-family wing flaps could be inaudible and a tamed teen/adult could fail to fly back to a distant owner.
- **[KNOWN-UNVERIFIED]** **D3 Princess power** — (`/summon orespawn:the_princess`; tame: right-click with DIAMOND BLOCK, instant) In combat, discharge >500 → 3 PurplePower orbs; at peace, terraforming bloom (flowers/grass/cactus/lava→water, 2 Butterfly/Cockateil hatches) under mobGriefing; firework-spark aura when charged. *(i093 — manual for the firework-spark aura)* — If broken, a player would notice: a fully charged Princess could show no firework-spark aura.
- **[KNOWN-UNVERIFIED]** **B3 riding** — Mount and fly/ride each (mount = EMPTY-HAND right-click once tamed): Dragon (no rubber-banding — BUG-020) [`/summon orespawn:dragon`; tame: RAW BEEF, 1-in-5 per beef], Leon [`/summon orespawn:leon`; tame: DIAMOND BLOCK instant, or RAW BEEF 1-in-3], Leonopteryx [`/summon orespawn:leonopteryx`; tame: any MEAT (raw beef canon), 1-in-3 per feed], Cephadrome (feed it first) [`/summon orespawn:cephadrome`; tame: RAW PORKCHOP, instant; then feed BEEF/COOKED BEEF/PORKCHOP/FEATHER before EACH mount — an unfed one refuses and stalks you], Ostrich (FAST jump on UP key) [`/summon orespawn:ostrich`; tame: WHEAT, 1-in-2], tamed PrinceTeen + Adult (strafe keys fire the canon trio) [`/summon orespawn:the_prince_teen` / `the_prince_adult`; tame: DIAMOND BLOCK, instant]. If a second player is available: observed movement is smooth. *(i066 — ride feel)* — If broken, a player would notice: the ride could feel floaty/misaligned or rubber-band while flying.
- **[KNOWN-UNVERIFIED]** **C8 controls** — Default binds: LEFT ALT (not Space) = fly up / sprint on ridden Dragon/Cephadrome/Ostrich. *(i068 — client keybinds)* — If broken, a player would notice: Left Alt could fail to fly up / sprint on a ridden Dragon/Cephadrome/Ostrich (or Space could wrongly do it).
- **[FIXED-IN-CODE — visual confirmation pending]** **D2 Hoverboard physics** — `/summon orespawn:elevator` (displays "Hoverboard"; board it with a plain right-click): W/S throttle, Left Alt FAST boost, climbs terrain, pitch grows with speed. *(i069)*
- **[KNOWN-UNVERIFIED]** **D2 Hoverboard crash** — Ride into a wall above ~0.75 speed (board: `/summon orespawn:elevator`, right-click to mount). Expect: shatters into sticks + 2 diamonds. (Harness-limit manual procedure: above 0.75 b/t expect 6-15 sticks + exactly 2 diamonds, rider ejected, no Hoverboard item.) *(i070)* — If broken, a player would notice: a full-speed wall crash could fail to shatter the board into 6-15 sticks + exactly 2 diamonds.
- **[KNOWN-UNVERIFIED]** **D2 Hoverboard malfunction** — Sustained high speed (ride the summoned `orespawn:elevator` at full W throttle). Expect: rare malfunction (explosions/smoke, speed bleed, ~2.2 s). *(i071)* — If broken, a player would notice: sustained top speed could never trigger the rare malfunction (explosions/smoke, ~2.2 s speed bleed).
- **[KNOWN-UNVERIFIED]** **D2 Hoverboard skins + guards** — Click the `/summon orespawn:elevator` board with `orespawn:ultimate_sword` (`/give @s orespawn:ultimate_sword`): cycles 10 skins. Mob punches can't destroy it while ridden. Hum (`orespawn:hover`, ENT-D-012 — randomized variants, not beacon) only while ridden. *(i072 — manual for the hover hum)* — If broken, a player would notice: the ultimate-sword 10-skin cycle could fail, or the hover hum could be missing or play while not ridden.
- **[KNOWN-UNVERIFIED]** **B4 idle animations** — Bee/Mothra/Urchin/Kyuubi etc. (`/summon orespawn:bee` / `mothra` / `urchin` / `kyuubi`) keep animating while you stand still (no frozen wings); Mothra renders 10× scale, slow flaps. *(i074)* — If broken, a player would notice: mobs could freeze mid-pose while you stand still (no idle wing flaps) and Mothra could render small instead of 10x.
- **C8 Rotator** — (`/summon orespawn:rotator`) spins as a 24-blade tri-axis gyroscope ball, not three flat blades. *(i075)*
- **[KNOWN-UNVERIFIED]** **C8 Kraken** — two Krakens side by side (`/summon orespawn:kraken` twice, in water): mouth-twitch cycles differ; twitch harder in combat. *(i076)* — If broken, a player would notice: the two Krakens' mouth-twitch cycles could run in identical lockstep and never intensify in combat.
- **C8/D2 GiantRobot** — (`/summon orespawn:giant_robot`) walks with BOTH legs + arms visible, hip bob; aggro → windmill punch + shoulder twist; D2: walk-state solver, no moonwalk. *(i077)*
- **C8/D2 Spider/AntRobot legs** — (`/summon orespawn:spider_robot` / `orespawn:ant_robot`; ride the SpiderRobot: plain right-click mounts it — IRON INGOT in hand heals instead) SpiderRobot renders all 8 legs, AntRobot all 6; D2: feet PLANT in the world and step ahead of body movement (no synchronized sine paddling); legs relocate when overstretched; ridden SpiderRobot occasionally flattens grass to dirt. *(i078 — the grass-to-dirt flattening is server-side, the leg IK is the manual part)*
- **C8 Robot2/3/4 arms** — (`/summon orespawn:robot_2` / `robot_3` / `robot_4`) Robot2 idle arms at sides, random windmill only in combat; Robot3 swings only in combat; Robot4 shield pumps + cannon raises/follows only in combat. *(i079)*
- **[FIXED-IN-CODE — visual confirmation pending]** **C8 Rat** — (`/summon orespawn:rat`) tail sways calm / thrashes attacking; head does NOT turn. *(i080)*
- **[KNOWN-UNVERIFIED]** **ENT-A-036 / ENT-K-005 / ENT-S-045 / BOSS-016 sounds** — Basilisk, Kraken (growl + alo_death), TRex (trex/alo set), Godzilla (godzilla_living/alo_hurt/godzilla_death) (`/summon orespawn:basilisk` / `kraken` / `trex` / `godzilla`): custom sounds, no Ravager/Elder-Guardian/Ender-Dragon audio. *(i081)* — If broken, a player would notice: these mobs could fall back to vanilla Ravager/Elder-Guardian/Ender-Dragon audio instead of their custom sounds.
- **C8 HUD** — Crosshair on TheKing/robot/big crab (`/summon orespawn:the_king` / `robot_2` / `crab`): textured health bar above hotbar + red name; named pets show custom name (NAME TAG on a tamed pet); YOUR Girlfriend shows a bar, someone else's doesn't (`/summon orespawn:girlfriend`; tame: POPPY, 1-in-3); bar shifts up 10px swimming/armored; `guiOverlayEnable=false` hides it. *(i082)*
- **ENT-A-100/101/102/103 crabs** — (`/summon orespawn:crab` ~10× for the size spread) natural-ish sizes vary (¼/½/full, rare giants; spawner crabs all 0.35); crab walks to water, dry-out damage away from it, scorpion sounds on melee, splash heal in water. *(i084 — manual for the scorpion melee sounds)*

Boss-arena tail of the same sitting (big flat pen on the same superflat):

- **[KNOWN-UNVERIFIED]** **BUG-005** — (`/summon orespawn:the_queen`, then `/gamemode survival`) TheQueen melees a SURVIVAL you to 0 HP: normal death screen/drops/respawn. Bonus: a low-HP mob victim vanishes with no drops (original quirk preserved). *(i096 — death screen/respawn flow needs the real client)* — If broken, a player would notice: dying to TheQueen could skip the normal death screen, drops, or respawn flow.
- **[KNOWN-UNVERIFIED]** **BOSS-002/007** — (`/summon orespawn:the_king` + `/summon orespawn:the_queen`) F3+B on King and Queen: 22-wide × 24-tall envelope; parts take/route damage (King parent unhittable, Queen parts glued to bones); models not lost inside the box. *(i098 — manual for "models not lost inside the box")* — If broken, a player would notice: the King/Queen models could get lost inside their 22x24 hitboxes, or body parts could fail to take/route damage.
- **[KNOWN-UNVERIFIED]** **BOSS-005/012** — Place `orespawn:king_spawner` / `queen_spawner` (`/give @s orespawn:king_spawner` / `orespawn:queen_spawner`, place by hand; flags in `runs/client/config/orespawn-common.toml`): ~5 s fuse, spawner + block above turn to air, boss appears 8 blocks up with living sound, stays leashed near spawn. With `theKingEnable=false`/`theQueenEnable=false`: block fizzles, nothing spawns. *(i099 — manual for the living sound as heard; the enable-fizzle config half is exercised in ConfigGateTests)* — If broken, a player would notice: the boss spawner could fizzle silently or the boss could appear without its living sound.
- **[KNOWN-UNVERIFIED]** **BOSS-026/031** — (`/summon orespawn:the_prince_teen` / `the_prince_adult`; for the rider seat: tame with DIAMOND BLOCK, mount EMPTY-HAND) F3+B: PrinceTeen 3.25×4.25, PrinceAdult 6.25×10.25; rider seats correct. *(i102 — manual for the rider-seat judgment)* — If broken, a player would notice: the rider could sit misaligned — floating above or sunk into the Prince mounts.
- **[KNOWN-UNVERIFIED]** **BOSS-032** — (`/summon orespawn:the_prince_adult`; tame: right-click with DIAMOND BLOCK; flag in `runs/client/config/orespawn-common.toml`) Tamed idle riderless Adult with `fullPowerKingEnable=true` transforms (diamond-block fast-path) into a King with the "Prepare to die!" sequence; config false → never; King does NOT deal doubled damage from that config. *(i103 — manual for the scripted sequence presentation)* — If broken, a player would notice: the 'Prepare to die!' King transformation sequence could present wrong or never trigger.
- **[KNOWN-UNVERIFIED]** **BOSS-036** — PrinceAdult (`/summon orespawn:the_prince_adult`; aggro it from survival for the living sound): king_living only while aggro+riderless, king_hit on hurt, trex_death on death. *(i104 — sounds as heard; the selection logic getters are covered elsewhere)* — If broken, a player would notice: the Prince Adult could aggro silently or play the wrong hurt/death sounds.

### (b) Dimension travel (survival where spawn tests require it)

Route for minimal switching: overworld (+ a Nether dip) → ant-chain hops
(Utopia → Mining → Crystal → Islands) → Village → Chaos → End. Use the
C-section tp commands; survival for spawn sweeps.

- **[KNOWN-UNVERIFIED]** **C7 ruby lava-seek** — Overworld cave lakes <Y50: ruby ore in stone directly UNDER lava, never free-floating; Nether has lavafoam veins + occasional nether ruby. *(i108)* — If broken, a player would notice: ruby ore could float free instead of hugging the stone under cave lava, and the Nether could lack lavafoam veins.
- **[KNOWN-UNVERIFIED]** **WGEN-005** — fly through NEW chunks at Y50+ mining into stone (hop template: `/execute in orespawn:mining run tp @s 0 100 0` — same pattern for `utopia` / `crystal` / `islands`): spawn-ore veins (clump ~4) of many different mobs appear frequently (~24/chunk overworld, ~27 dims); Mining dim noticeably denser (×3); Islands and Crystal (other than its own 11 egg ores) get NONE; Nether/End get none. *(i130)* — If broken, a player would notice: spawn-ore veins could be missing, far too rare, or appear in dimensions that should have none.
- **[KNOWN-UNVERIFIED]** **Trees** — a lone Duplicator Log appears rarely in veggie patches (duplicatorTreeEnable on); the experience sapling (`/give @s orespawn:experience_tree_seed`, plant it) grows the REAL experience tree (2x2 oak trunk, drooping branch crown); crystal fairy trees shrink with lessLag=1; fairy-tree chunks that fail the site scan still suppress termites (WGEN-062 quirk). *(i144)* — If broken, a player would notice: the experience sapling could grow a wrong-shaped tree and Duplicator Logs could never appear in veggie patches.
- **[KNOWN-UNVERIFIED]** **C1 spawns** — (hop: `/execute in orespawn:mining run tp @s 0 100 0`, same pattern for `utopia`) Alien/Alosaurus/Camarasaurus/Baryonyx in Mining (Alosaurus/Baryonyx also Utopia), NOT End/overworld; Boyfriend beach hotspots; Bee/CaterKiller/Basilisk/Brutalfly/BandP per-biome. *(i116)* — If broken, a player would notice: these mobs could spawn in the wrong dimensions/biomes — or not at all where they should.
- **[KNOWN-UNVERIFIED]** **C2 spawns** — Dragon/GoldFish/EnderReaper on Island; DungeonBeast/Flounder/Irukandji/Frog in Crystal; EnderKnight/EnderReaper/Hammerhead/GammaMetroid/DungeonBeast in Chaos; GammaMetroid swarms in Mining; EnderKnight/EnderReaper dark-forest hotspots, NONE in the End; Fairy only dark forest; Girlfriend beach groups 8-15; Hydrolisc swamp/jungle; Frog river/swamp; NO ocean Flounder/GoldFish/Irukandji/Hammerhead/Hydrolisc. (Hops: `/execute in orespawn:islands run tp @s 0 80 0`, same pattern for `crystal` / `chaos`.) *(i117)* — If broken, a player would notice: beach/dark-forest hotspots could be empty, or ocean spawns could appear where none belong.
- **[KNOWN-UNVERIFIED]** **C3 spawns** — Kraken/Leon/Leonopteryx/RubyBird never natural; Kyuubi Nether weight doubled; Lizard river/swamp/ocean only; rat swarms in dark forests (10-20) + taigas only. *(i118)* — If broken, a player would notice: Kraken/Leon/Leonopteryx could spawn naturally when they never should, or dark-forest rat swarms could be missing.
- **[KNOWN-UNVERIFIED]** **C4 spawns** — TRex only Chaos/Mining; TerribleTerror Island/Chaos; Urchin/Skate/Vortex Crystal(+Chaos); Spyro/VelocityRaptor Mining only; Stinky Nether/badlands/Island; SeaMonster ocean w4 + swamps; WormSmall never natural (day) — **D4: WormSmall spawns at NIGHT only** (ENT-S-078 gate). (Hops: `/execute in orespawn:chaos run tp @s 0 100 0`, same pattern for `mining` / `crystal` / `islands`.) *(i119)* — If broken, a player would notice: mobs could spawn in the wrong dimensions, or WormSmall could appear in daylight.
- **[KNOWN-UNVERIFIED]** **ENT-A-110** — CreepingHorror naturally only in darkness at night below y=15 (or Chaos — `/execute in orespawn:chaos run tp @s 0 100 0`). *(i120 — NOTE: classified AUTOMATABLE but no GameTest exists in the suite yet; keep manual until a SpawnGateTests method lands)* — If broken, a player would notice: CreepingHorror could appear in daylight or above y=15 outside Chaos.
- **[KNOWN-UNVERIFIED]** **C7 termite gate + ant chain (WGEN-049)** — dimension access via the ant chain: `/summon orespawn:ant` (→ Utopia), `orespawn:red_ant` (→ Mining), `orespawn:termite` (→ Crystal), `orespawn:unstable_ant` (→ Islands). Right-click EMPTY-HANDED teleports; clicking the same ant type while already in its dimension returns you to the Overworld; portal cooldown between hops. Termite with items/armor held: "Empty your inventory!" / "Take off your armor!" chat gates, no teleport. A FOLLOWING tamed wolf within 48×24×48 travels along; a SITTING one stays. *(i114 — TEST-004 FIXED 2026-08-11: findSafeY now force-generates the destination chunk; mechanism GameTest-covered, confirm the live cross-dimension flow here)* — If broken, a player would notice: the termite inventory/armor gates could fail, or a first-visit arrival could still misplace you (regression — the fix is in this build).
- **[KNOWN-UNVERIFIED]** **C7 vein rates** — `/execute in orespawn:mining run tp @s 0 100 0`, mine at Y10-20: uranium/titanium ≈3× overworld density; salt only ABOVE Y50; amethyst only BELOW Y25. *(i106)* — If broken, a player would notice: ore rates could be off — no 3x uranium/titanium boost, or salt/amethyst at the wrong depths.
- **[KNOWN-UNVERIFIED]** **C7 Mining lakes/rooms** — (`/execute in orespawn:mining run tp @s 0 100 0`, then cave-dive) caves contain vanilla-style water/lava lakes and monster rooms. *(i113)* — If broken, a player would notice: Mining caves could be barren — no water/lava lakes and no monster rooms.
- **[KNOWN-UNVERIFIED]** **C7 structures** — `/locate structure orespawn:beehive` (Mining only), `orespawn:greenhouse`/`robot_lab`/`white_house`/`challenge_tower_king` (Islands only), `orespawn:royal_altars` (Utopia ~45-chunk grid), `minecraft:mineshaft` + `stronghold` resolve in Mining and Village. *(i111)* — If broken, a player would notice: /locate could fail — whole structures could be missing from their home dimensions.
- **[KNOWN-UNVERIFIED]** **C7 dungeons** — (`/execute in orespawn:utopia run tp @s 0 100 0`) Utopia lava pools <Y50: ruby-brick dungeon (ruby kit/ThunderStaff chest pool); generic cobble dungeons in Utopia/Mining/Village/Islands — break the spawner block, 12-mob-ladder mob appears after ~20 s (ITEM-020's 400t fuse). *(i112)* — If broken, a player would notice: ruby-brick/cobble dungeons could be missing, or the broken spawner could never produce its mob.
- **[KNOWN-UNVERIFIED]** **C7 Islands terrain** — `/execute in orespawn:islands run tp @s 0 80 0`: flat grass plane at Y7, no carvers/lakes; unstable anthills near the surface. *(i109)* — If broken, a player would notice: the Islands dimension could generate carved/flooded terrain instead of the flat Y7 grass plane.
- **[KNOWN-UNVERIFIED]** **WGEN-042 (rookery)** — `/execute in orespawn:islands run tp @s 0 20 0`, `/locate structure orespawn:nightmare_rookery`. Expect: cluster of jagged stone spires in two wandering ridges; roughly 1-in-10 spires truncate at 19 blocks with a chest on top and a Nightmare (Pitch Black) spawner sitting directly on the chest; chests roll dead stink bugs / black+scary flowers / nightmare eggs / robot kits / bones / string / flesh / XP bottles (4-8 stacks). *(i124 — ridge/spire look + 1-in-10 distribution are the manual halves)* — If broken, a player would notice: the rookery could generate with malformed spires or without its 1-in-10 chest-and-spawner spires.
- **[KNOWN-UNVERIFIED]** **WGEN-051/055** — (in the Islands dim: `/execute in orespawn:islands run tp @s 0 80 0` first) `/locate structure orespawn:challenge_tower_king` (and _queen): tower heights VARY (most towers stop below 6 floors; full towers with the Nightmare cap ≈ 28%); towers sit on the grass plane (not floating on a heightmap ledge); with `lessLag=1` roughly half as many towers/rookeries generate in fresh chunks. *(i125)* — If broken, a player would notice: challenge towers could float off the ground or all generate at the same height.
- **[KNOWN-UNVERIFIED]** **WGEN-055 (worm ring)** — around a FULL-height tower (find one: `/locate structure orespawn:challenge_tower_king` in orespawn:islands), dig at surface−1 out to ~55 blocks east/±55 north-south of the base: buried Large Worm spawners scattered outside the castle, with no rectangular cutoff at chunk borders. *(i128)* — If broken, a player would notice: the buried worm-spawner ring could be missing or cut off in a rectangle at chunk borders.
- **[KNOWN-UNVERIFIED]** **SpiderDriverEnable worldgen gate (positive control)** — while in the Village dimension (`/execute in orespawn:village run tp @s 0 100 0`; flag in `runs/client/config/orespawn-common.toml`): with `spiderDriverEnable=true` confirm a SpiderHangout generates in fresh Village chunks (positive control), then set `spiderDriverEnable=false` and fly NEW chunks: no new hangouts generate (the Dungeon Spawner Block can still build one — faithful). *(i164 manual sub-check — see (c))* — If broken, a player would notice: hangouts could keep generating with spiderDriverEnable off (or never generate with it on).
- **[KNOWN-UNVERIFIED]** **C7 Chaos terrain** — (`/execute in orespawn:chaos run tp @s 0 100 0`) 128-high stone world, grass band ~Y60-65, vanilla sky; chaos roster spawns; no dungeons/towers. *(i110)* — If broken, a player would notice: the Chaos dimension could generate wrong — no 128-high stone world, no Y60-65 grass band, or stray dungeons/towers.
- **[KNOWN-UNVERIFIED]** **EnderCastle (End)** — `/execute in minecraft:the_end run tp @s 100 70 0`, `/locate structure orespawn:ender_castle_end`. Expect: 29x29 obsidian-plate castle on end stone (central OR outer islands — PN-017), 4 spiral-stair corner towers, rooftop lava pool + dragon-egg pedestal, Ender Knight/Reaper rooftop spawner pairs + pit + CaveFisher alcoves, 3 alcove chests (facing inward) rolling the ender/experience-catcher table (6-10 stacks), trophy ender chest EMPTY (plain block). *(i136 — the End siting claim is the manual half; interior content is covered by the buildNow tests)* — If broken, a player would notice: the Ender Castle could be missing from the End or generate floating/malformed on the islands.

### (c) Harness-limit sub-checks for the 4 MOSTLY-AUTOMATED items

Everything else in these four items is green in the suite; only the named
sub-check needs eyes (per FIX_LOG "Harness-limit reclassifications").

- **[KNOWN-UNVERIFIED]** **DamselInDistress (Village dim) — level existence** — confirm the orespawn:village dimension actually loads on a real server: `/execute in orespawn:village run tp @s 0 100 0` must work (server.getLevel(orespawn:village) non-null). *(i158 — rest automated in StructureTestsB#i158_damsel_in_distress_content)* — If broken, a player would notice: the Village dimension could fail to load at all — any teleport there would error out.
- **[KNOWN-UNVERIFIED]** **Rainbow (Islands sky) — SKY_BAND_70 anchor** — `/locate structure orespawn:rainbow` in orespawn:islands then fly to Y70-89: the rainbow generates in that band. *(i162 — rest automated in StructureTestsC#rainbow_islands_sky_i162)* — If broken, a player would notice: the rainbow could generate outside its Y70-89 sky band or not at all.
- **[KNOWN-UNVERIFIED]** **SpiderHangout (Village dim) — spiderDriverEnable worldgen gate** — covered at the Village stop in (b) above. *(i164 — rest automated in StructureTestsC#spider_hangout_village_i164)* — If broken, a player would notice: hangouts could keep generating with spiderDriverEnable off (or never generate with it on).
- **[KNOWN-UNVERIFIED]** **EnderKnightDungeon (Mining) — LOWEST_GRASS_36 no-sink anchor** — `/locate structure orespawn:ender_knight_dungeon_mining` in orespawn:mining: the dungeon sits ON the lowest grass surface (not sunk — contrast Basilisk Maze). *(i170 — rest automated in StructureTestsC#ender_knight_dungeon_i170)* — If broken, a player would notice: the Mining version could generate sunk into the terrain instead of sitting on the lowest grass surface.

### (d) Date-gated in-client checks (LAST — they change your system clock)

Caveats per "How the gates work" in section D: dates are evaluated live at
every check; the Girlfriend reads the date when constructed/loaded, so summon
a FRESH one after flipping the clock; reset the clock afterwards
(`.\gradlew --stop` if Gradle complains about skew). Halloween and Easter
gates are automated in DateGateTests via the injected-date seam — only
Valentine's still needs the client.

- **[KNOWN-UNVERIFIED]** **ANIM-016 Valentine's (ENT-D-011-family)** — Set clock to **Feb 14**, `/summon orespawn:girlfriend` (fresh!). Expect: GIANT (2.5×8, renders 5×) angry Girlfriend, 800 HP (`/data get`), `girlfriendv` texture, attacks players and Boyfriends (not her owner/pets), immune to inWall damage, `o_hurt` ambient. Hit her with `orespawn:rose_sword`: each hit drops a "Love"; ~1-in-4 cures her (shrinks, calms, extra Love shower). Thrown shoes deal **10** damage today (ITEM-053). *(i178 — manual for the giant render, girlfriendv texture, and o_hurt ambient)* — If broken, a player would notice: the Feb-14 Girlfriend could spawn normal-sized, with the wrong texture, or without her o_hurt ambient instead of the giant angry variant.

### (e) Recheck after art fixes — user FAIL notes from the 2026-08-10 sitting

The 11 FAILs recorded in testing_session/results.json. Where the logic half is
automated (and green after the 2026-08-10 fix batch), only the reported
visual/feel issue needs a recheck once art/model fixes land.

- **[FIXED-IN-CODE — visual confirmation pending]** **RECHECK BUG-004 (Prince adult model)** — user: "adult texture is glitched and not rendering properly." (`/summon orespawn:the_prince_adult` to inspect the adult model directly.) Transform flow itself is faithful per TF-024. *(i002)*
- **[FIXED-IN-CODE — visual confirmation pending]** **RECHECK ITEM-011/012 (pizza + duct tape)** — user: "pizza renders as a full 16x16 pixel block"; duct tape: "nothing repaired". Duct-tape half TRIAGED → TF-027 (PORT_BUG, FIXED 2026-08-11): the recipe and this row's old give-line both yielded the inert twin item `duct_tape_item`, and the old "LEFT-click repairs held gear" expectation was never the 1.7.10 flow. REWORDED retest: `/give @s orespawn:pizza_item` — place it, LEFT-click eats a slice; `/give @s orespawn:duct_tape` — right-click the ground to PLACE the tape block, then left- or right-click the placed tape with the damaged item (count 1) in your MAIN hand: maxDamage/6 repair per click, 6 slices. Left-click logic automated (MiscTests#item011_012_pizza_ducttape_left_click); recheck the pizza texture, eat feedback, and the in-client place-then-click repair. *(i003)*
- **RECHECK ITEM-013/014 (mole dirt) — CLOSED CONFIRMED-INTENDED (2026-08-11)** — user: "acts pretty much like soulsand ... makes me slow when walking on it. dont know if thats intended." It is: soul-sand-like sink (collision top at 14/16 = 0.125 below full height) + 0.3× horizontal drag every tick inside are the exact 1.7.10 values (orig MoleDirtBlock.java:33-43; port MoleDirtBlock.java:27, 50-53 matches 1:1 — the drag is actually harsher than soul sand's 0.4 speed factor), and the block evaporates on a random tick in both versions (temporary mole spoil). No preference for change expressed → no MOD entry; if the slow ever feels too punishing, softening the drag (0.3 → 0.5) becomes a MOD entry, not a fix. *(i004)*
- **[FIXED-IN-CODE — visual confirmation pending]** **RECHECK ITEM-016 (crystal furnace GUI)** — user: "works but no furnace progress bar. it never progresses but things are still smelting." (`/setblock ~ ~ ~2 orespawn:crystal_furnace`; fuel: `/give @s orespawn:crystal_coal`.) BE cook/burn logic automated; the GUI progress arrow is the recheck. *(i005)*
- **[FIXED-IN-CODE — visual confirmation pending]** **RECHECK ITEM-019 (repellent texture)** — user: "texture isnt likea torch, its a full 16x16 block so it looks weird." (`/give @s orespawn:kraken_repellent` + `orespawn:creeper_repellent`, place both.) Push cadence automated. *(i006)*
- **RECHECK ITEM-027 (duplicator pacing) — CLOSED FAITHFUL-PACING (2026-08-11)** — user: "super slow. i placed the log down, then it took like 2 mincraft days to grow the tree." The port is write-for-write identical to the original and cannot be slower (orig Trees.java:121-182 / port BlockDuplicatorLog.java:57-116 — one write per random tick in both). The math: at randomTickSpeed 3 each block ticks every 4096/3 ≈ 68.3 s mean, full tree = 11 successful ticks ≈ **12.5 min mean (0.63 MC days; P99 ≈ 21.5 min)**, first copy ≈ +2 min more WITH a source block inside the 5×5 (±2 of the trunk). The observed ~2 MC days is observational inflation: sleeping skips ~10 min of game-clock per night with ~0 s of random ticks, leaving simulation distance pauses the log while the day counter runs, and this row's old "small build ~5 blocks away" instruction put the copy source OUT of the ±2 footprint, so copying couldn't start. RETEST (if desired): stay awake, within simulation distance, source block ≤2 blocks from the trunk — expect a full tree well inside one MC day. Speed preference → MOD-015 (growth-steps config, default faithful). *(i007)*
- **[FIXED-IN-CODE — visual confirmation pending]** **RECHECK ITEM-037 (chainsaw held model + tree scope)** — user: "it also cuts down nearby trees ... holding it its a little strange. like its sideways ... in f5 its a litle glitched." TREE-SCOPE HALF CLOSED FAITHFUL (2026-08-11): the original fells everything wood/leaf-like inside a blind 11×16×11 box around the broken block with no attachment test (orig UltimateSword.java:351-371; port Chainsaw.java:142-158 identical) — neighboring trees are cut by design; the attached-only preference → MOD-016 (config'd BFS felling, default faithful). HELD-MODEL HALF still open for the art recheck: `/give @s orespawn:chainsaw`, inspect first- and third-person held model. *(i008)*
- **RECHECK ITEM-040 + ITEM-057 (experience armor/sword) — RECONCILIATION PLAN** *(i009)* — Resolution of "nothing happens": the original NEVER repaired armor or drained the sword (orig ExperienceSword.java:55-103 — the old checklist text was an inference error); the real mechanism is an XP trickle + portal motes, which is invisible in creative (no XP bar) and near-invisible at chest-only rates (~1 XP/min, one particle per ~3 s). The green test drives `inventoryTick` directly; this check verifies the LIVE tick path. Procedure (~3 min, SURVIVAL):
  1. `/gamemode survival`, safe flat spot, `/xp set @s 0 points`, `/xp set @s 0 levels`.
  2. `/give @s orespawn:experience_sword` (hotbar is fine — any inventory slot ticks) + give and WEAR the full experience armor set (helmet/chest/leggings/boots: `/give @s orespawn:experience_helmet` / `experience_chestplate` / `experience_leggings` / `experience_boots`).
  3. Note `/xp query @s points`, stand still 2 minutes (watch for single portal motes rising at head/chest/leg/boot heights every few seconds), then `/xp query @s points` again. EXPECT: ≈ 6-10 points gained (full set ≈ 4 XP/min; ±wide variance is normal — anything clearly > 0 passes).
  4. Negative controls per the original: pre-damage the chestplate — its durability must NOT change; the sword's durability must NOT drain; remove all armor and repeat step 3 → 0 XP gained.
  5. Verdict: XP > 0 + particles → PASS (port faithful; docs were wrong — already amended above). XP = 0 after 3+ min with full set in survival → the LIVE inventoryTick path diverges from the tested direct-call path: log FAIL with the exact minutes waited; that becomes a new TF finding (suspect: tick wiring, not the roll math — the roll math is suite-proven).
- **[FIXED-IN-CODE — visual confirmation pending]** **RECHECK ITEM-047/048/049 (garden textures + Y level)** — user: "the crop textures are glitched. it also seems the garden spawns 1 block lower." Y-LEVEL HALF CLOSED FAITHFUL (2026-08-11): both versions anchor the plot to the PLAYER'S FEET and ignore the clicked block's Y entirely (orig InstantGarden.java:41-50 — `y = (int)posY`, clicked Y never read; port InstantGarden.java:39-42 identical) — grass floor at feetY−1, crops at feetY, so clicking a block that sits at foot level (upslope/ledge) puts the floor one below the clicked block, exactly as in 1.7.10. Same-Y preference → MOD-017 (click-anchored Y config, default faithful). CROP-TEXTURE HALF still open for the art recheck: `/give @s orespawn:instant_garden`, right-click the ground. *(i010)*
- **[FIXED-IN-CODE — visual confirmation pending]** **RECHECK ITEM-001/005 (gem ore drops)** — user: "ruby and amethyst both drop jsuit the straight ore. both ores dont smelt either." SMELTING SUB-CHECK DROPPED (2026-08-11): 1.7.10 had NO ruby/amethyst smelting recipes — the complete original addSmelting list (orig OreSpawnMain.java:3092-3117: uranium/titanium/salt blocks, corn cob, corn dog, bacon, CrystalCrystal, TigersEye, peacock, crab, 6 fish) has no entry for either gem ore, because the ores drop gems (and XP) directly on break; the raw ore block was Silk-Touch-only and furnace-less even in 1.7.10. "Doesn't smelt" is faithful. Retest is drops only: `/setblock ~ ~ ~2 orespawn:ore_ruby` / `orespawn:ore_amethyst`, break WITHOUT Silk Touch — root causes TF-017 + TF-022 FIXED in the batch; confirm in-client (1-2 gems on non-silk break, XP pops). *(i013)*
- **RECHECK ITEM-060/061/062 (crafting set)** — user note cut off at "bow crafing works," (remaining recipes unverified in-client). RecipeTests green; spot-check the other four crafts in-client (per ITEM-060/061/062: chest + red bed from crystal planks, raw corn dog, bucket from 3 pink tourmaline ingots, cobweb from string; oak door from crystal planks should also still work). *(i016)*
