# MODERNIZATION_NOTES — "OreSpawn Modernized" 2.0 Design Backlog

Planning output only — **nothing here is implemented**. Per `IMPLEMENTATION_PLAN.md`:
the parity pass replicates original behavior faithfully (including original bugs where
they are gameplay-defining); this file collects everything that deserves a curated
redesign afterward.

Categories: **ORIGINAL-BUG** · **BALANCE** · **VANILLA-INTEGRATION** · **UX** · **TECH-DEBT**
Each entry: original behavior (file:line), what's wrong/dated, concrete modern proposal,
impact estimate, related finding IDs.

---

## MOD-001 — TheQueen deletes mobs without a death pipeline (ORIGINAL-BUG)
- **Original:** `orig TheQueen.java:260-261, 340-341` — when the Queen's health-tracked
  victim reaches 0 HP it is removed via `setDead()`: no death event, no drops, no XP,
  no `LivingDeathEvent` for other mods.
- **What's wrong:** Tamed pets and other mods' entities vanish silently; kill-tracking
  mods/advancements never fire. (The player-facing half of this was fixed as BUG-005;
  the mob half is preserved for parity.)
- **Proposal:** Replace `discard()` with a lethal `hurt(mobAttack(queen), Float.MAX_VALUE)`
  so the vanilla death pipeline runs, or at minimum fire `LivingDeathEvent`.
- **Impact:** Gameplay-visible (missing drops/XP); trivial effort.
- **Related findings:** BUG-005.

## MOD-002 — TheKing silently deletes small attackers (ORIGINAL-BUG)
- **Original:** `orig TheKing.java:824-826` — any `EntityMob` with bb area < 3.0 that
  damages TheKing is `setDead()`-ed and the hit ignored.
- **What's wrong:** Player-tamed mobs and other mods' minions are wiped with no
  feedback, drops, or death event — reads as a bug/grief to modern players.
- **Proposal:** Apply lethal attributed damage instead of removal, or restrict the wipe
  to OreSpawn's own minion classes; consider a knockback + damage response instead of
  deletion entirely.
- **Impact:** Gameplay-visible; trivial effort.
- **Related findings:** BUG-012.

## MOD-003 — Royal pets' "flight" is random noPhysics drifting (TECH-DEBT)
- **Original:** `orig ThePrince.java:423, 502-504, 529-551` (and `ThePrincess`
  equivalents) — activity 2 sets `noPhysics` and damps motion while `do_movement()`
  steers toward a random flight target; state flips on 1/100-per-tick rolls.
- **What's wrong:** Physics-less flight lets the pet clip through terrain; the random
  state machine is opaque to players ("why did my prince fly away after I hit it?").
- **Proposal:** When porting flight (Phase D), implement it as proper `FlyingMoveControl`
  + navigation with collision, keeping the original's state timings; surface the state
  via animation instead of physics toggles.
- **Impact:** Gameplay-visible; moderate effort.
- **Related findings:** BUG-010, the Prince/Princess flight MISSING findings.

## MOD-004 — Kraken grab force-moves players (UX / ORIGINAL-BUG)
- **Original:** 1.7.10 Kraken held a caught entity 15 blocks below itself by force-setting
  position each tick.
- **What's wrong:** Even with the BUG-011 teleport fix, per-tick forced teleports +
  forced yaw are disorienting and fight other teleport mechanics (ender pearls, /home).
- **Proposal:** Model the grab as riding a (invisible) tentacle part entity so vanilla
  passenger sync handles movement, with a grab-escape mechanic (damage threshold).
- **Impact:** Gameplay-visible; moderate effort.
- **Related findings:** BUG-011.

## MOD-005 — Crab health copy-paste bug (ORIGINAL-BUG)
- **Original:** `orig Crab.java:137` applies `PitchBlack_stats.health` (250) instead of
  `Crab_stats.health` (180) to the scaled crab. Preserved for parity (PN-006).
- **Proposal:** Use `MobStats.CRAB.maxHealth()` once the parity pass is frozen; pure
  one-line change, rebalances big crabs from 250·scale to 180·scale HP.
- **Impact:** Balance-visible; trivial effort.
- **Related findings:** ENT-A-100.

## MOD-006 — TheKing's registry-sampling death drops (BALANCE / VANILLA-INTEGRATION)
- **Original:** `orig TheKing.java:193-226` — 150 random draws from the ENTIRE item
  registry + 150 from the block registry, including other mods' items (creative-only,
  technical, or broken-in-survival stacks). Kept code-side for parity (the documented
  Phase B1 architecture exception).
- **Proposal:** Replace with a curated high-value loot table, or filter the sampling
  (exclude technical/creative-only items, respect `FeatureFlags`); also consider a
  datapack-extensible reward pool.
- **Impact:** Gameplay-defining for the final boss; low effort.
- **Related findings:** BOSS-004, ENT-SYS2-001.

## MOD-007 — Exact loot enchantment dice (TECH-DEBT)
- **Original:** Independent per-enchantment rolls on dropped gear (0–7 enchantments,
  Unbreaking-biased); Phase B1 approximates with one `enchant_randomly` per item (PN-005).
- **Proposal:** Register a custom global loot function (`LootItemFunctionType`) that
  reproduces the original dice chains exactly, then reference it from the JSONs — keeps
  the single-source-of-truth architecture AND exact fidelity.
- **Impact:** Loot-distribution fidelity; small effort, isolated.
- **Related findings:** ENT-SYS-001/ENT-SYS2-001 gear tables.

## MOD-008 — `MyHammy` reward item not ported (TECH-DEBT)
- **Original:** `orig OreSpawnMain.java:1648` registers "Hammy" (Big Bertha variant);
  `orig Hammerhead.java:146-148` drops it 1-in-3. The port has no such item, so the
  drop was omitted rather than substituted (Phase B1 MISSING-ITEM).
- **Proposal:** Port the item in Phase D (items/weapons category), then add the 1-in-3
  entry back to `hammerhead.json`.
- **Impact:** Missing signature weapon; effort tied to the weapons pass.
- **Related findings:** ENT-D-056.

## MOD-009 — Kyanite gem branch + Pink Tourmaline (DELIBERATE 2.0 CONTENT CANDIDATE)
- **Origin:** Phase-10 port invention, REMOVED from the parity build per the PN-009
  Option-A decision (2026-06-13). 1.7.10 has no kyanite gem/ore/armor and no pink
  tourmaline: its "Kyanite" is just the display name of the CrystalStone terrain block
  (`orig OreSpawnMain.java:3029`), whose tool chain the port keeps as `crystal_stone`.
  Archived here verbatim so it can be reintroduced post-parity as intentional content.
- **Complete design (as removed):**
  - **Blocks** — both `TransparentCrystalBlock`, strength 3.0/6.0, light 6,
    `requiresCorrectToolForDrops`, `noOcclusion`; tags `minecraft:mineable/pickaxe` +
    `minecraft:needs_iron_tool`; loot = own gem ×1, Silk Touch → the ore block;
    `cube_all` models (`blocks/orekyanite`, `blocks/orepinktourmaline` — texture files
    were never shipped):
    - `ore_kyanite` "Kyanite Ore"
    - `ore_pink_tourmaline` "Pink Tourmaline Ore"
  - **Items** — `kyanite` "Kyanite" gem, `pink_tourmaline` "Pink Tourmaline" gem, plus
    the two BlockItems.
  - **Tool tier** — `SimpleTier(INCORRECT_FOR_DIAMOND_TOOL, durability 1300,
    speed 11.0, bonus 7.5, enchantability 60, repair = kyanite)`; positioned between
    Crystal Pink (1100) and Tigers Eye (1600) on the Crystal-dimension power curve.
  - **Tools** (crafted vanilla-shape from kyanite gem + `crystal_wood_stick` handles):
    Sword (atk 3, speed −2.4), Pickaxe (1.0, −2.8), Shovel (1.5, −3.0),
    Hoe (−3.0, 0.0), Axe (5.0, −3.0).
  - **Armor** — material: defense 3/6/8/4 (boots/legs/chest/helmet map), enchantability
    70, toughness 1.0, knockback 0, repair = kyanite, layer `"kyanite"` (layer texture
    never shipped); 4 pieces at durability multiplier 4, vanilla shapes from the gem.
  - **Worldgen** — `ore_kyanite`: size-6 veins targeting `crystal_stone` +
    `#stone_ore_replaceables`, 6/chunk, uniform Y −32..80, `underground_ores` step,
    `orespawn:crystal_plains` only, wired via the `add_crystal_dim_ores` biome modifier.
    The matching `ore_pink_tourmaline` vein was already deleted in Phase C7 (WGEN-024).
  - **Recipes** — extracting: `ore_kyanite` → 2 kyanite @ 200 t, `ore_pink_tourmaline`
    → 2 pink_tourmaline @ 200 t; shapeless 1:1 conversions both ways between
    `pink_tourmaline` and `crystal_pink_ingot`; 5 tool + 4 armor crafting recipes.
- **World-compat impact:** removal makes all branch blocks/items vanish from existing
  port worlds (unknown-registry entries are stripped on load); reintroducing the same
  IDs later will NOT restore already-stripped items, so this is a one-way break for
  affected saves. Reintroduction in 2.0 should note that.
- **Proposal:** reintroduce post-parity as an explicit addition, ideally with real
  textures (block, gem, tools, armor layer) and a JEI-visible extracting use.
- **Related findings:** WGEN-024 (resolved), PN-009 (closed by this removal).
