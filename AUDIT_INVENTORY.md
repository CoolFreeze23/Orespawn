# OreSpawn Port Audit — Inventory & Status Checklist

**Original:** OreSpawn 1.7.10 (`reference_1_7_10_source/`, 586 classes, decompiled `orespawn-1.7.10-20.3`)
**Secondary reference:** 1.12.2 ConquerantFix (`reference_1_12_2_source/`)
**Port:** this repo (`src/main/java/danger/orespawn/`, NeoForge 1.21.1)
**Audit date:** 2026-06-11. Supersedes entity/worldgen scope of `ORESPAWN_PORTING_AUDIT.md` (2026-04-06).

**Detailed evidence** (per-feature tables, both-side values, file:line citations) lives in
`audit_sections/`:


| File                          | Scope                                                                    |
| ----------------------------- | ------------------------------------------------------------------------ |
| `01_entities_A_C.md`          | Entities Acid–CrystalCow (30)                                            |
| `02_entities_D_I.md`          | Entities DeadIrukandji–IrukandjiArrow (40)                               |
| `03_entities_K_R.md`          | Entities Kraken–RubyBird (29)                                            |
| `04_entities_S_Z.md`          | Entities Scorpion–WormSmall (31)                                         |
| `05_bosses.md`                | Multi-part bosses: King, Queen, Mobzilla/Godzilla, Prince line, Princess |
| `06_blocks_items.md`          | All blocks, items, tool/armor tiers, recipes, dispensers, config         |
| `07_worldgen.md`              | Ore gen, 6 dimensions, structures, dungeons, trees, portals, villages    |
| `08_animations_events_gui.md` | Animations, keybinds/rider control, HUD/GUI, events, networking          |
| `09_bugs.md`                  | Phase 3: bug & glitch hunt (31 findings by severity)                     |
| `10_optimizations.md`         | Phase 4: optimization proposals (27 findings by impact)                  |


**Legend:** `[x]` PORTED (verified equivalent) · `[~]` PARTIAL (exists, incomplete) ·
`[D]` DIVERGENT (behavior/values differ — both values cited in section file) ·
`[ ]` MISSING · `[?]` UNVERIFIED

---

## Roll-up


| Category                          | PORTED | PARTIAL | DIVERGENT | MISSING | UNVERIFIED | Total                                     |
| --------------------------------- | ------ | ------- | --------- | ------- | ---------- | ----------------------------------------- |
| Entities (non-boss, 130)          | 27     | 55      | 47        | 1       | 0          | 130                                       |
| Bosses (sub-features)             | 58     | 12      | 27        | 13      | 0          | 110                                       |
| Blocks/Items/Tiers/Recipes/Config | ~139   | 22      | 73        | 18      | 18+        | ~270                                      |
| Worldgen/Dimensions/Structures    | 30     | 16      | 15        | 17      | 3          | 81                                        |
| Animations/Events/GUI             | 16     | 10      | 9         | 3       | 0          | 38                                        |
| **Bugs found (Phase 3)**          | —      | —       | —         | —       | —          | **31** (7 critical, 6 high, 9 med, 9 low) |
| **Optimizations (Phase 4)**       | —      | —       | —         | —       | —          | **27** (8 high, 11 med, 8 low)            |


### Systemic problems (affect many items at once)

1. **Double drops** — ~25 entities (incl. King/Queen/Mobzilla) run a hardcoded
  `dropCustomDeathLoot` **and** a JSON loot table; every kill awards two loot sets.
2. **Port `MobStats.java` is dead code** — every entity hardcodes attributes that
  frequently disagree with original values; armor (defense) dropped to 0 for most mobs.
3. `**checkSpawnRules` gates broadly deleted** — day/night, altitude, darkness,
  crowd-cap and dimension conditions from original `getCanSpawnHere` are absent.
4. **No natural spawns for whole groups** — Robots 1–5, PitchBlack, RubberDucky, Coin,
  GiantRobot, Jeffery, AttackSquid, WormLarge, rainbow/unstable ants (which also makes
   Village & Islands dimensions unreachable in survival).
5. **Animation frequency mistranslation in ~39 models** — original constant `wingspeed`
  became `limbSwingAmount` inside trig arguments; idle/flying animations freeze or jitter.
6. **Armor durability/enchantability systematically wrong** — all 14 sets at ~1/15th
  original durability with original durability misread as enchantability.
7. **All 8 dispenser behaviors missing** — `ModDispenserBehaviors.java` is a stub.
8. **MultiHitboxLib event handlers registered on wrong bus** — startup crash risk
  (see `09_bugs.md` CRITICAL #1/#2).

---

## 1. Entities A–C (detail: `audit_sections/01_entities_A_C.md`)

- Acid (projectile)
- [D] Alien — torch destruction AI present but mistimed/divergent stats
- [D] Alosaurus — double drops; stat drift
- [D] AntRobot
- [D] AttackSquid — no natural spawn; y-gate inverted; watercanon attack missing
- [D] BandP
- [~] Baryonyx — double drops
- [D] Basilisk — double full loot sets
- [~] Beaver
- [D] Bee — double drops
- [D] Bertha (weapon-related) — damage rewritten 496→250
- [D] BerthaHit — pvp config gate became unconditional player immunity
- [D] BetterFireball — lost immunity whitelist (now hits Kraken/Royalty/allies)
- [~] Boyfriend
- [D] Brutalfly — ranged fireball volleys missing; HP 110→500
- [~] Camarasaurus
- [~] Cassowary — double drops
- [D] CaterKiller — metamorphosis inverted (despawns instead of transforming; Brutalfly on every death)
- [D] CaveFisher
- [D] Cephadrome — ~200-line ridden-flight physics missing; never spawns (MISC category)
- [~] Chipmunk
- [~] CliffRacer
- [D] CloudShark
- [~] Cockateil — birdtype never randomized; ruby drop gate lost
- [~] Coin — MISC category, no natural spawn (orig: 6 biomes)
- [D] Crab — scale system (0.25/0.5/1.0 → HP/XP/armor) gutted; water ecology inverted
- [D] CreepingHorror — dark/night/y≤15 spawn gates deleted
- Cricket
- [D] Cryolophosaurus
- [D] CrystalCow

## 2. Entities D–I (detail: `audit_sections/02_entities_D_I.md`)

- DeadIrukandji
- [~] Dragon — armor 14 missing; tame item/drops/spawn dim diverge
- Dragonfly
- [D] DungeonBeast — 65/12/6→60/10/4; roofed forest→badlands
- [~] EasterBunny — mob-egg-laying + carrot taming missing
- Elevator (hover sound → beacon hum)
- [D] EmperorScorpion — 350/35/20→300/20/0; enchanted loot de-enchanted
- [D] EnchantedCow — drops extended beyond original
- [D] EnderKnight — 60/12/6→80/15/0; overworld hotspots→End-only
- [D] EnderReaper — 90/18/8→120/20/0; overworld→End-only
- EntityAnt
- EntityButterfly
- [D] EntityCage — species whitelist replaced by universal NBT capture
- [~] EntityCannonFodder — hat teams 2 of original; corncob spawning missing
- EntityLavaLovingItem
- EntityLunaMoth
- EntityMosquito
- EntityRainbowAnt
- [D] EntityRedAnt — HP 1→2, speed 0.15→0.2
- [D] EntityThrownRock — 5 rock types wrong effects; typed rock-returns collapsed; glass-shatter/water-skip gone
- EntityUnstableAnt
- [~] Fairy — Crystal Torch drop → glowstone; spawn diluted
- [D] Firefly — ExtremeTorch drop → glowstone
- Flounder
- Frog
- [D] GammaMetroid — Crystal-dim swarms (w35, 4-7) → Nether singles (w3); 100/10/12→60/8/0
- Gazelle (poppy → mutton drop)
- Ghost (spawn weight 15→4)
- GhostSkelly
- [~] GiantRobot — LaserBall ranged attack missing; no natural spawn; stats ÷4
- [~] Girlfriend — dance/jealousy/valentine/ranged-arrow AI missing (MyEntityAIDance exists but unwired)
- [D] GoldCow
- [~] GoldFish — no drops at all (orig: gold items)
- [D] Hammerhead — attack 75→20; 4 unique drops lost; non-original boss bar added
- [D] HerculesBeetle — 250/30/19→200/15/0; enchanted-gear + MyBigHammer loot lost
- [D] Hydrolisc — HP 60→100; swamp/jungle→beach/river/ocean
- IceBall
- InkSack
- [D] Irukandji — attack 20→200 (10×); HP 1→5
- [~] IrukandjiArrow

## 3. Entities K–R (detail: `audit_sections/03_entities_K_R.md`)

- [~] Kraken — HP 1000→3000, ATK 40→80; spawner-only → every ocean w1; double drops
- [D] Kyuubi — lost fire immunity but self-ignites (burns to death); HP 125→30
- LaserBall
- [~] Lavafoam — death drop missing
- [~] LeafMonster
- [~] Leon — rider-controlled flight (speed 3.5, flyup) missing in both EntityLeon and Leonopteryx
- Lizard
- [~] LurkingTerror
- [~] Mantis — double drops
- [~] MantisClaw (item divergent)
- [~] Molenoid — double drops; stats halved
- [~] Mothra — double drops; some attacks partial
- [~] Nastysaurus — drops upgraded sticks/bones → 10 diamonds + 10 emeralds + 10 gold
- [~] Ostrich
- [~] Peacock — attack missing
- [~] PitchBlack — no natural spawn (orig Utopia lists)
- [~] Pointysaurus — double drops
- PurplePower
- [~] Rat — passive by default (new configs default true); swarm spawning reduced
- RedCow
- [~] Robot1 — no natural spawn
- [D] Robot2 — no natural spawn; AI partial
- [~] Robot3 — no natural spawn
- [D] Robot4 — LaserBall attack + MyRayGun drop missing; dead DATA_SHIELDING; HP 170→750
- [~] Robot5 — no natural spawn; HP 20→150
- [~] RockBase — death drop missing; ItemRock never calls placeRock(type) (re-randomizes)
- [~] Rotator — double drops
- [~] RubberDucky — no drops, no natural spawn; tame fish→wheat
- [~] RubyBird

## 4. Entities S–Z (detail: `audit_sections/04_entities_S_Z.md`)

- [D] Scorpion
- [~] SeaMonster — double drops; in-water speed boost computed but never applied
- [D] SeaViper
- [~] Shoes
- [D] Skate
- Slice (item)
- [~] SpiderDriver — never attacks (orig melee + Poison 60t @50%); no natural spawn
- [D] SpiderRobot — 1500/100/16 → 500/50/8; flame/spark frontal attack missing; invented boss bar
- [~] SpitBug — double drops
- [~] Spyro — diamond→Dragon evolution chain missing; untame/rename missing
- [~] StinkBug
- [~] Stinky
- [~] SunspotUrchin
- Termite
- [D] TerribleTerror
- ThunderBolt
- [D] TRex — HP 160→200, atk 22→30, armor 14→0; custom sounds → Ravager; uranium/titanium drops → beef/diamonds
- [~] Triffid
- [~] TrooperBug
- [~] Tshirt
- [~] UltimateArrow
- UltimateFishHook — entire custom fishing economy (5 lava fish + 5 custom fish, lava fishing) missing
- [~] Urchin
- [~] VelocityRaptor
- [D] Vortex — wrong dimension (orig night overworld + Island/Crystal/Chaos; port Nether-only); fire immunity lost; invented launch attack
- [~] WaterBall — item form missing
- [~] WaterDragon — ranged WaterBall/fireball volleys missing; HP 150→200
- Whale
- [~] WormSmall — armor-theft missing; now common daytime CREATURE spawn
- [~] WormMedium — item theft missing
- [~] WormLarge — theft missing; no natural spawn; invented nether star drop; double drops

## 5. Bosses (detail: `audit_sections/05_bosses.md`)

- [~/D] **TheKing** — stats 7000/350/21 → 6000/250/12; spawner y+8→y+1, guard-mode anchor missing, enable configs gone; PLAY_NICELY synced but never consumed; double drops
- [~/D] **TheQueen** — drops 56× beef/bone → 56× nether star + golden apple + XP bottle (massive buff) + double drops; invented invulnerable dormant/wake phase (60t); sidecar partial
- [~/D] **Godzilla/Mobzilla** — armor 21 missing; HP 4000→6000, atk 175→150; custom sounds → Ender Dragon; double drops; jump shockwave kills through Creative (bug)
- [~] **ThePrince** — ranged fire/lightning/ice canons + flight MISSING (now ground melee pet)
- [~] **ThePrinceTeen** — same missing ranged/riding; 4 stat divergences
- [~/D] **ThePrinceAdult** — armor missing; FULL_POWER_KING_ENABLE repurposed (orig gated Adult→King transform); royal sounds lost
- [~] **ThePrincess** — ranged + flight missing; 4 stat divergences

## 6. Blocks & Items (detail: `audit_sections/06_blocks_items.md`)

Highlights only — full per-item tables in the section file.

- **Dispenser behaviors** — all 8 (`MyDispenserBehavior`*) missing; `ModDispenserBehavior.java` stub
- [D] **All 14 armor sets** — ~1/15th durability (Ultimate helmet 143 vs 2200); enchantability uses original durability values (Royal 2000 vs 200)
- [D] **Overworld ruby/amethyst/uranium/titanium ores** — reuse `OreCrystal`, inherit 1/3 explode-on-break the originals never had
- [D] **Crystal-dim ores** — constructor arg shift: `(light,hardness,resistance)` read as `(hardness,resistance)`; CrystalCoal hardness 0.6 vs 6.0
- [D] **RTPBlock** — `stepOn` → `entityInside` on a full cube; likely never fires
- [D] **OreGenericEgg** — drops 5–11 copies of itself instead of 5–11 XP (item-dupe exploit)
- [D] **Uranium/titanium smelting** — full ingot (XP 0.7) instead of nugget (XP 0.3): 9× inflation
- [D] **Chainsaw** — AoE attack + 11×16×11 tree-crushing missing; **Royal Guardian Sword** Unbreaking 5 → Sharpness 5; **Battle/Queen Battle Axe** wrong enchant sets
- [D] **Sifter / ExperienceCatcher** — complete redesigns (160-entry loot table → ~8 vanilla items; orb-to-bottle → area XP vacuum)
- [D] **Foods** — Butter Candy / Cooked Bacon / Crystal Apple / Heart lost all effects; every effect-fish duration wrong (Sun Fish 6000t→600t, Spark Fish 100t→600t, Lava Eel inverted)
- [~] **Crystal Furnace** — custom fuels dead (Crystal Coal 20000t / Log 800t / Planks 400t unregistered); cook 100t vs 150t
- ~139 items verified equivalent (see tables) · [?] 18+ unverified (bulk recipes un-diffed)

## 7. Worldgen & Dimensions (detail: `audit_sections/07_worldgen.md`)

- Crystal dimension terrain, maze frequency, trees, flora — match 1:1
- Overworld dungeon gate (1/16 chunk, Y5–44, 50 cooldown, config-gated)
- Utopia spawn lists; entity-portal wiring; salt/troll ores
- **Village dimension villages** — `MapGenMoreVillages` (~12× density) has no counterpart; dimension has no villages
- **~25 structure types** — all D4/Islands structures (EnderCastle, IncaPyramid, NightmareRookery, CephadromeAltar, Rainbow…), BasiliskMaze, KyuubiDungeon, NightmareDungeon, EnderKnightDungeon
- **AntHill surface gen** + rainbow/unstable ant spawns → Village & Islands unreachable in survival
- [D] **DungeonSpawnerBlock pool** — 50 structures → 2 (generic/ruby)
- [D] **Dungeon chest loot** — original level1–5 / ruby-gear lists → vanilla `simple_dungeon`
- [D] **Mining dimension** — lost 3× ore density and dino/alien roster (Alosaurus/GammaMetroid/Alien/CaveFisher) → cave critters
- [D] **Chaos terrain** — placeholder overworld noise instead of nether-style 128-high caverns
- [~] **Crystal spawn-ore blocks** — frequencies exact but 9/11 generate as plain CrystalStone (placeholders)
- [D] **Crystal dim double-generation risk** — maze/towers/trees exist as both chunkgen code AND datapack features
- [D] **Termite→Crystal portal** — empty-inventory rule reduced to empty-hand check

## 8. Animations, Events, GUI (detail: `audit_sections/08_animations_events_gui.md`)

- [D] **~39 models**: `wingspeed`→`limbSwingAmount` frequency mistranslation (systemic)
- [D] **SpiderRobot model** — renders 1 of 8 legs
- [D] **GiantRobot model** — walk cycle + attack punch + duplicate limbs lost; `RenderGiantRobotInfo` HUD missing
- **Rider flight controls** — 6 of 7 original mounts have none (only Dragon implements `RideableFlyer`)
- [D] **Pointed-at-mob health HUD** (~45 entity types incl. bosses) → Girlfriend-only list
- **Seasonal mechanics** — Halloween ghost spawns, Valentine Girlfriend AI, Easter Bunny date gate
- [D] **Rotator model** — 24-blade tri-axis gyroscope → 3 flat Z-spinning blades
- [D] **Mothra** — flaps ~6.5× too fast; renders at half size
- [~] **Per-mob spawn-disable config** — ~42 of ~100 original flags enforced; `KrakenEnable` absent
- **TheQueen GeckoLib** — controllers ↔ JSON verified, names match exactly (only GeckoLib entity)

## 9. Bugs — Phase 3 (detail: `audit_sections/09_bugs.md`)

7 CRITICAL · 6 HIGH · 9 MEDIUM · 9 LOW. The critical set:

1. `multihitboxlib/EntityEventHandler` + `GameEventHandler` — game-bus events on MOD bus → startup crash; all MHLib hitbox sync dead
2. `EntityRat.java:139` — `UUID.fromString("")` on spawner-spawned rats → ticking-entity server crash (Crystal dungeons place rat spawners)
3. `ThePrince.java:241` (+Teen/Adult) — growth transform `tame(null)` when owner offline → NPE crash
4. `TheQueen.java:486-502` — `discard()` on ServerPlayer (player deleted, no respawn)
5. `Godzilla.java:422-441` — shockwave `genericKill` kills Creative/Spectator
6. `SpiderRobot.java:199-202` — NBT overrides without `super` → loses Health/effects/persistence every save
7. `EntityWormLarge.java:30` — `wormsSpawned` not saved → 40 fresh worms per chunk reload

## 10. Optimizations — Phase 4 (detail: `audit_sections/10_optimizations.md`)

8 HIGH · 11 MEDIUM · 8 LOW. All are proposals only; none applied. Top items:
MHLib per-tick registry lookups and always-on part-sync/bone packets; `EntityVortex`
ungated AABB scans+sorts per tick; Kraken 95-blockpos probe per tick; per-frame entity
scan + string concat in `GirlfriendOverlay`; ~100k `BlockPos` allocs per Crystal chunk;
~35 entities resetting MOVEMENT_SPEED base every tick; per-frame deferred-holder checks
in `EntityCullingMixin`. Items flagged behavior-affecting (scan throttling) are proposals
requiring sign-off.