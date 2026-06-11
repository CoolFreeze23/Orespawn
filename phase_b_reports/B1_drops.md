# Phase B1 — Double-Drop Architectural Consolidation

**Mandate:** the loot-table JSON (`src/main/resources/data/orespawn/loot_table/entities/*.json`) is the single
source of truth for all item death-drops. Every `dropCustomDeathLoot` override that spawned items was deleted
and each entity's JSON was rewritten to the exact original 1.7.10 drop list. Non-item death behavior was moved
to `die()`. Exceptions are documented individually below.

Citations refer to `reference_1_7_10_source/sources/danger/orespawn/<File>.java` ("orig").
"e" = item received the original's probabilistic per-enchantment rolls, expressed in JSON as
`minecraft:enchant_randomly` (see "Enchantment translation note" below).

## Enchantment translation note (applies to every "e" entry)

Original gear drops ran chains of `if (rand.nextInt(6)==1) addEnchantment(X, 1+rand.nextInt(5))`
(Unbreaking at 1-in-2, level 2–5). A loot JSON cannot reproduce per-enchantment independent dice, so every
such item carries one `minecraft:enchant_randomly` function. This is the single uniform approximation used
across all tables; the alternative (keeping Java code) would defeat the architecture. Divergence: items are
always enchanted with exactly one enchantment instead of 0–7 independent rolls.

---

## Per-entity consolidation table

### Cow family

| Entity | Orig citation | Original drop list | JSON now | Deleted from code | Divergences removed |
|---|---|---|---|---|---|
| RedCow | orig RedCow.java:17-23 | apple 0–2 (+looting), then vanilla cow drops (leather 0–2, beef 1–3, smelts on fire) | exactly that | `dropCustomDeathLoot` | invented wheat bonus (ENT-K-061), double apples |
| GoldCow | orig GoldCow.java:18-24 | gold ingot 0–2 (+looting) + vanilla cow drops | exactly that | `dropCustomDeathLoot` | hardcoded gold-ingot double layer (ENT-D-051) |
| CrystalCow | orig CrystalCow.java:19-25 | apple 0–2 (+looting) + vanilla cow drops | exactly that | `dropCustomDeathLoot` | invented pink-ingot (ENT-A-115) |
| EnchantedAppleCow (orig EnchantedCow) | orig EnchantedCow.java:26-34 | apple 0–3 (+looting), golden apple ×2, enchanted golden apple ×1, + vanilla cow drops | exactly that | `dropCustomDeathLoot` | invented XP bottles + 20% enchanted book (ENT-D-016) |
| AppleCow | port-original entity (no 1.7.10 counterpart) | n/a — modeled on orig RedCow.java:17-23 apple pattern | apple 0–2 + cow drops | `dropCustomDeathLoot` | double layer |
| GoldenAppleCow | port-original entity (no 1.7.10 counterpart) | n/a — modeled on orig EnchantedCow.java golden-apple pattern | golden apple + cow drops | `dropCustomDeathLoot` | double layer |

### Simple creatures

| Entity | Orig citation | Original drop list | JSON now | Deleted from code | Divergences removed |
|---|---|---|---|---|---|
| RubyBird | orig RubyBird drop logic (getDropItem/conditional) | 1-in-2 ruby else feather | exactly that | `dropCustomDeathLoot` | ruby double-drop (ENT-K-087) |
| Cassowary | orig Cassowary.java:93-97 | chicken (vanilla pattern) | chicken | `dropCustomDeathLoot` | invented 2–4 feathers (ENT-SYS-001) |
| Nastysaurus | orig Nastysaurus.java:156-170 | iron ingot ×10, rotten flesh ×10, leather ×10, string ×10 | exactly that | `dropCustomDeathLoot` | invented 40-valuables table (ENT-K-043) |
| Pointysaurus | orig Pointysaurus.java:127-141 | leather ×10, beef ×6, rotten flesh ×6, string ×6 | exactly that | `dropCustomDeathLoot` | invented diamonds (ENT-K-055) |
| Alosaurus | orig Alosaurus.java:117-126 | bone ×10, beef ×6 | exactly that | `dropCustomDeathLoot` | invented gunpowder + diamonds (ENT-A-010) |
| Flounder | orig Flounder.java:98-102 | raw fish ×1 → minecraft:cod | cod ×1 | `dropCustomDeathLoot` | double layer |
| EntityTshirt | orig Tshirt.java:82 | string | string | `dropCustomDeathLoot` | leather substitution (ENT-S-055) |
| Bee | orig Bee.java:108-117 | gold nugget 2–11, butter candy 2–11, dandelion 2–11, sugar 2–11 | exactly that | `dropCustomDeathLoot` | gunpowder/spider-eye/mushroom inventions (ENT-A-043) |
| Baryonyx | orig Baryonyx.java:109-113 | beef 2–6 | beef 2–6 | `dropCustomDeathLoot` | invented bones pool (ENT-A-028) |
| EntityKyuubi | orig Kyuubi.java:131/231 | coal, redstone block, quartz block (orig counts) | exactly that | `dropCustomDeathLoot` | swapped/doubled drops (ENT-K-008) |

### Worms, insects, mid-tier

| Entity | Orig citation | Original drop list | JSON now | Deleted from code | Divergences removed |
|---|---|---|---|---|---|
| EntityWormMedium | orig WormMedium.java:256-265 | rotten flesh ×2, leather ×2 | exactly that | `dropCustomDeathLoot` | extra pools (ENT-S-083) |
| EntityWormLarge | orig WormLarge.java:343-352 | worm tooth, painting, fixed valuables list | exactly that | `dropCustomDeathLoot` | nether star/spider eyes/saddle inventions (ENT-S-086) |
| TRex | orig TRex.java:119-128 | trex tooth, painting, beef, paired uranium+titanium nuggets | exactly that | `dropCustomDeathLoot` | name_tag/diamond/xp inventions (ENT-S-043) |
| EntityTriffid | orig Triffid.java:186-204 | green goo + painting (orig list) | exactly that | `dropCustomDeathLoot` | name_tag/vine/potato inventions (ENT-S-048) |
| EntityMolenoid | orig Molenoid.java:116-125 | molenoid nose ×1, painting, gold nugget, beef (orig counts) | exactly that | `dropCustomDeathLoot` | nose-double + substitutions (ENT-K-036) |
| EntityMantis | orig Mantis.java:120-129 | mantis claw, painting, gold nugget, uranium/titanium nuggets, diamond (orig counts) | exactly that | `dropCustomDeathLoot` | gold-ingot substitution + double path (ENT-K-031) |
| EntityBrutalfly | orig Brutalfly.java:339 (onDeath) | gold nugget drops; 20 Butterflies + particles on death | gold nugget | `dropCustomDeathLoot`; butterfly burst moved to `die()` with citation | replaced loot (ENT-A-063) |
| Mothra | orig Mothra.java:341 (onDeath) | painting, gold nugget, moth scale, blaze rod, nether star (orig counts); 20 Luna Moths + particles on death | exactly that | `dropCustomDeathLoot`; moth swarm moved to `die()` with citation | substitutions/doubling, restored moth swarm (ENT-K-040) |

### Gear-table entities

| Entity | Orig citation | Original drop list | JSON now | Deleted from code | Divergences removed |
|---|---|---|---|---|---|
| EntityCaterKiller | orig CaterKiller.java:146-160 | jaw ×1, item frame, leather ×10, beef ×6, 1–5 bonus rolls (13/20 hit) over ultimate sword / ruby / diamond block / enchanted-ruby-gear / ultimate bow pool; 25 Butterflies on death | weighted pool replicating the orig switch; butterflies in `die()` | `dropCustomDeathLoot` + helpers | name_tag, slime, emerald-block swap (ENT-A-076) |
| WaterDragon | orig WaterDragon.java:264-278 | scale + weighted ultimate/iron gear table (orig weights) | exactly that, gear e | `dropCustomDeathLoot` + helpers | ultimate-tool additions, name_tag (ENT-S-075) |
| SeaMonster | orig SeaMonster.java:156-170 | scale + fish + d-N iron-gear table | exactly that, gear e | `dropCustomDeathLoot` + helpers | name_tag/heart-of-the-sea, unenchanted gear (ENT-S-007) |
| SeaViper | orig SeaViper.java:160-174 | tongue + fish 9–14 + iron-gear rolls | exactly that, gear e | `dropCustomDeathLoot` + helpers | fish inflation, name_tag (ENT-S-011) |
| EntityEmperorScorpion | orig EmperorScorpion.java:167-181 | scale + diamond/ultimate gear rolls | exactly that, gear e | `dropCustomDeathLoot` + helpers | beef/slimeball de-theming (ENT-D-015) |
| EntityTrooperBug | orig TrooperBug.java:186-200 | jumpy bug scale + amethyst gear/block rolls | exactly that, gear e (incl. `orespawn:block_amethyst`) | `dropCustomDeathLoot` + helpers | double name_tag, missing enchants (ENT-S-054) |
| EntityHerculesBeetle | orig HerculesBeetle.java:127-141 | big hammer + diamond-gear rolls | exactly that, gear e | `dropCustomDeathLoot` + helpers | name_tag + 4–11 bones invention (ENT-D-059, ENT-SYS-001) |
| Basilisk | orig Basilisk.java:137-151 | scale ×1, item frame ×1, emerald 12–17, cooked fish 8–12, 3–7 rolls @1-in-15 emerald-gear pool | exactly that, gear e | `dropCustomDeathLoot`, `enchantItem`, `dropItemRand` | name_tag, raw-vs-cooked, golden apple/gold ingot layer (ENT-A-034) |
| AttackSquid | orig AttackSquid.java:155-169 | ink sac 1–3, cod 1–3, 1-in-50 gold-gear pool | exactly that, gear e | `dropCustomDeathLoot` | gunpowder/iron/gold + cod/diamond double layer (ENT-A-020) |
| Hammerhead | orig Hammerhead.java:126-149 | xp bottle ×8, experience catcher ×10, creeper launcher ×16, creeper repellent ×4, beef ×6, experience tree seed ×2, 1-in-3 MyHammy | all but MyHammy (MISSING-ITEM) | `dropCustomDeathLoot` (invented 8 xp bottles + 6 bones) | restored the four unique reward items (ENT-D-056); removed bone invention |

### Bosses

| Entity | Orig citation | Original drop list | JSON now | Deleted from code | Divergences removed |
|---|---|---|---|---|---|
| Kraken | orig Kraken.java:236-871 | kraken tooth ×1, painting ×1, ink sac 120–279, 5–14 rolls of the d53 gear table (cases 0–52: ultimate sword/bow/axe/pickaxe/shovel, diamond + diamond gear e, iron + iron gear e, gold + golden gear e, glistering melon, golden apple, enchanted golden apple, experience sword/armor e, amethyst tools e, amethyst block) | exact d53 reproduced as one pool with `rolls: uniform(5,14)` and 53 weight-1 entries | `dropCustomDeathLoot`, `dropItemRand`, `enchantItem`, 6 enchant-pattern helpers | golden apple + 120–279 **cooked cod** (was ink sacs!), emerald block (was diamond block), gold ingot (was iron ingot), name_tag/saddle (were gold nugget/gold ingot), chainmail (was golden armor), diamond default-case (ENT-K-003) |
| Godzilla | orig Godzilla.java:820-1775 | painting ×1, godzilla scale 50–79, **raw beef 100–259**, **bone 50–109**, 25–39 rolls of the d80 gear table (cases 0–75 incl. diamond/iron/gold/experience/amethyst/ruby/ultimate gear + armor sets e, blocks; cases 72,76–79 empty) | exact d80 as one pool with `rolls: uniform(25,39)`, weighted entries + `minecraft:empty` weight 5 for the 5 dead slots | `dropCustomDeathLoot`, `dropLootByRoll` (80-case switch), `dropItemRand`, `dropItemRandAt` (unused), `enchantItem`, 6 enchant helpers | nether star (was painting), emeralds (were beef), xp bottles (were bone), ender pearl/name_tag swaps inside the table, emerald default-case (BOSS-015) |
| TheQueen | orig TheQueen.java:190-200 | royal guardian sword ×1, prince egg ×1, 56× each of queen scale / raw beef / bone / rotten flesh; spawns The Princess | exactly that | `dropCustomDeathLoot`, `dropItemRand`; Princess spawn moved to `die()` (orig TheQueen.java:193 cited) | 56× xp bottles / golden apples / nether stars (BOSS-011) |
| TheKing | orig TheKing.java:183-227 | royal helmet/chest/legs/boots + royal guardian sword ×1 each; 150 random item-registry draws + 150 random block-registry draws; spawns The Prince | royal gear + sword (the deterministic part) | royal-gear drops removed from code (now JSON); Prince spawn moved to `die()` (orig TheKing.java:187 cited) | prince_egg (orig King never dropped an egg — that is the Queen's), diamond 30–80 / gold 20–50 / iron 20–50 invented pools (BOSS-004) |

### Audit-list entity without code override

| Entity | Orig citation | Action |
|---|---|---|
| Dragon | orig Dragon.java:342-347 — raw beef 1–6, nothing else | `dragon.json` rewritten to beef uniform(1,6) (was diamond w/ looting). Code-side deletion is a HANDOFF (see below) since `Dragon.java` is excluded from this task. |

---

## Exceptions (architecture not uniformly applicable)

1. **TheKing** (`entity/TheKing.java` `dropCustomDeathLoot`) — orig TheKing.java:193-226 samples the
   **entire item registry** and the **entire block registry** at random, 150 draws each. A loot JSON cannot
   enumerate "every registered item/block including other mods'". The registry-sampling code path is retained;
   the deterministic royal-gear drops were moved into `the_king.json` and the invented JSON pools were removed.
   This is the exception pre-authorized in the task statement.
2. **BandP** (`entity/BandP.java` `dropCustomDeathLoot`) — two parts of orig BandP.java:146-165 are
   inexpressible in JSON: (a) it drops its **runtime stolen-item stash** (`MymainInventory`), which is dynamic
   entity state; (b) the 2–4 paired uranium+titanium nuggets are gated on `getWhat()==0` (the bear-vs-pickpocket
   variant stored in synched entity data), which vanilla loot conditions cannot read. Both stay in the override
   (with orig citations); the unconditional emerald 10–14 drop moved to `band_p.json`.
3. **Enchantment fidelity** (all gear tables) — see "Enchantment translation note" above: independent
   per-enchantment dice replaced by one `enchant_randomly` per item. Uniform, single, documented approximation.

Non-exceptions (moved to `die()`, since they are not item drops): Mothra's 20-moth burst, Brutalfly's
20-butterfly burst, CaterKiller's 25-butterfly burst, TheQueen's Princess spawn, TheKing's Prince spawn.

## MISSING-ITEM list

| Orig item | Wanted by | Status |
|---|---|---|
| `MyHammy` ("Hammy" Bertha variant, orig OreSpawnMain.java:1648) | Hammerhead 1-in-3 drop (orig Hammerhead.java:146-148) | not registered in port `ModItems.java`; omitted from `hammerhead.json`, no substitute invented |
| Experience pickaxe/axe/shovel (orig ExperiencePickaxe etc.) | (noted during survey; not in any in-scope drop table — orig King/Kraken/Godzilla tables only use experience sword/armor, which ARE registered) | informational only |

## Audit findings closed

- **ENT-SYS-001** — every listed in-scope entity now has exactly one drop source (JSON); grep shows the only
  remaining `dropCustomDeathLoot` overrides in `entity/` are TheKing + BandP (documented exceptions) and the
  excluded files (Dragon, Cephadrome, EntityLeon, Leonopteryx, Ostrich).
- **ENT-SYS2-001** — same evidence for the K–Z/boss list (Kraken, Kyuubi, Mantis, Molenoid, Mothra,
  Nastysaurus, Pointysaurus, RedCow, RubyBird, TRex, Triffid, TrooperBug, SeaMonster, SeaViper, WaterDragon,
  WormMedium, WormLarge, TheKing, TheQueen, Godzilla all consolidated; Leon excluded/handoff).
- **ENT-A-010** Alosaurus — `alosaurus.json` = bone ×10 + beef ×6; override deleted.
- **ENT-A-020** AttackSquid — JSON = ink/cod/1-in-50 gold gear e; override deleted.
- **ENT-A-028** Baryonyx — JSON = beef 2–6 only.
- **ENT-A-034** Basilisk — JSON per orig incl. cooked fish + emerald-gear rolls e; code helpers deleted.
- **ENT-A-043** Bee — JSON = gold nugget/butter candy/dandelion/sugar 2–11 each.
- **ENT-A-063** Brutalfly — butterfly burst restored in `die()`; loot = gold nugget per orig.
- **ENT-A-076** CaterKiller — JSON per orig pool (diamond block restored); butterflies in `die()`.
- **ENT-A-115** CrystalCow — pink ingot removed, apple restored.
- **ENT-D-015** EmperorScorpion — enchanted diamond/ultimate gear restored e.
- **ENT-D-016** EnchantedCow/EnchantedAppleCow — xp-bottle/book inventions removed.
- **ENT-D-051** GoldCow — single gold-ingot source with looting per orig.
- **ENT-D-056** Hammerhead — experience catcher ×10, creeper launcher ×16, creeper repellent ×4,
  experience tree seed ×2 restored (MyHammy = MISSING-ITEM).
- **ENT-D-059** HerculesBeetle — big hammer + enchanted diamond gear restored e.
- **ENT-K-003** Kraken — full d53 table restored; ink sac 120–279 + painting back (cooked cod/golden apple removed).
- **ENT-K-008** Kyuubi — single source, orig items.
- **ENT-K-031** Mantis — single path, beef per orig.
- **ENT-K-036** Molenoid — nose drops exactly once.
- **ENT-K-040** Mothra — moth swarm restored in `die()`; drop substitutions removed.
- **ENT-K-043** Nastysaurus — VERIFIED-CORRECT against orig Nastysaurus.java:156-170 (10× iron ingot /
  rotten flesh / leather / string).
- **ENT-K-055** Pointysaurus — **audit's claimed original list is WRONG**: audit says "10 bone + 6 carrot +
  6 stick + 6 arrow", but orig Pointysaurus.java:127-141 drops 10 leather + 6 beef + 6 rotten flesh +
  6 string (fields 151116_aA=leather, 151082_bd=beef, 151078_bh=rotten_flesh, 151007_F=string). JSON follows
  the verified source, not the audit.
- **ENT-K-061** RedCow — wheat invention removed.
- **ENT-K-087** RubyBird — ruby drops at most once.
- **ENT-S-007/S-011** SeaMonster/SeaViper — single path, gear enchanted e, name_tag/heart-of-the-sea removed.
- **ENT-S-043** TRex — tooth/painting/beef/nugget pairs per orig; inventions removed.
- **ENT-S-048** Triffid — green goo + painting per orig; potato path deleted.
- **ENT-S-054** TrooperBug — amethyst gear enchanted e; name_tag removed; `orespawn:block_amethyst` id fixed
  (was orphan `orespawn:amethyst_block`).
- **ENT-S-055** Tshirt — string restored.
- **ENT-S-075** WaterDragon — ultimate-tool additions removed; orig table restored e.
- **ENT-S-083/S-086** WormMedium/WormLarge — single path; nether star/spider eyes/saddle removed.
- **BOSS-004** TheKing — JSON = royal set only; registry sampling kept (documented exception); prince_egg
  and diamond/gold/iron pools removed.
- **BOSS-011** TheQueen — 56-roll pool reverted to queen scale/beef/bone/rotten flesh; royal sword +
  prince egg in JSON only; Princess spawn in `die()`.
- **BOSS-015** Godzilla — full re-theme reversed (painting/beef/bone restored); exact d80 table with 5
  empty slots reproduced in JSON; entire 80-case code switch deleted.

## Handoff items (files owned by another workstream — NOT edited)

1. **Dragon.java** — delete `dropCustomDeathLoot` (port Dragon.java:233-240, drops 1–6 bone) and its
   `dropItemRand` helper if then unused. `dragon.json` already rewritten to the orig beef 1–6
   (orig Dragon.java:342-347), so until the override is deleted Dragon double-drops (beef JSON + bone code).
2. **Cephadrome.java / EntityLeon.java / Leonopteryx.java / Ostrich.java** — still override
   `dropCustomDeathLoot` (ENT-SYS-001/ENT-SYS2-001 members); same consolidation needed by their owner.
   Their JSONs were left untouched per the exclusion.

## Files changed

**Java (override deleted / logic moved):** RedCow, AppleCow, GoldCow, CrystalCow, GoldenAppleCow,
EnchantedAppleCow, RubyBird, Cassowary, Nastysaurus, Alosaurus, Pointysaurus, Flounder, EntityWormMedium,
EntityWormLarge, TRex, Baryonyx, EntityTriffid, EntityMolenoid, Bee, EntityMantis, EntityKyuubi, EntityTshirt,
EntityBrutalfly, Mothra, EntityCaterKiller, WaterDragon, SeaMonster, SeaViper, EntityEmperorScorpion,
EntityTrooperBug, EntityHerculesBeetle, Basilisk, AttackSquid, Hammerhead, Kraken, Godzilla, TheQueen, TheKing
(exception kept, trimmed), BandP (exception kept, parity-fixed) — all in `src/main/java/danger/orespawn/entity/`.

**Loot JSONs rewritten/created:** red_cow, apple_cow, gold_cow, crystal_cow, golden_apple_cow,
enchanted_apple_cow, ruby_bird, cassowary, nastysaurus, alosaurus, pointysaurus, flounder, worm_medium,
worm_large, trex, baryonyx, triffid, molenoid, bee, mantis, kyuubi, tshirt, brutalfly, mothra, cater_killer,
water_dragon, sea_monster, sea_viper, emperor_scorpion, trooper_bug, hercules_beetle, basilisk, attack_squid,
hammerhead, band_p, kraken, godzilla, the_queen, the_king, dragon — all in
`src/main/resources/data/orespawn/loot_table/entities/`. All ids verified against `ModEntities.java`
registry names (loot-table id defaults to the entity registry name); no orphan files created.

**Report:** `phase_b_reports/B1_drops.md` (this file).

## Build status

`.\gradlew.bat compileJava --console=plain`: **8 compile errors remain, none in Phase B1 files.** All 8 are in
the other workstream's concurrent rider-flight work (`entity/ai/RiderFlightController.java` — new untracked file —
plus `travelRidden` overrides in Dragon.java, Cephadrome.java, EntityLeon.java, Leonopteryx.java, Ostrich.java,
ThePrinceAdult.java, ThePrinceTeen.java, all files excluded from / untouched by this task). Every file edited
in Phase B1 compiles cleanly (javac reports no errors in them; IDE lints clean). JSONs follow the 1.21.1
`minecraft:entity` schema conventions of the existing valid files in the folder.
