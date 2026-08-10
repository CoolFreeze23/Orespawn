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

## MOD-010 — Boyfriend "bro mode" as a friendly-fire gate (DELIBERATE 2.0 CONTENT CANDIDATE)
- **Origin:** Phase-10 port invention, REMOVED from the parity build (2026-06-13,
  Phase D3). The port overrode `TamableAnimal.wantsToAttack` so that, with
  `boyfriendBroMode` enabled, a tamed Boyfriend refused to attack other tamed mobs
  sharing his owner (no pet-vs-pet friendly fire).
- **Why removed:** the original's `bro_mode` config (`orig OreSpawnMain.java:1481`,
  "BoyfriendBroMode", default 0) has no combat meaning at all — it only gates the
  Boyfriend's VOICE: 1-in-2 silence rolls on ambient/hurt lines, a silent death, and
  the `bb_happy` ambient variant (`orig Boyfriend.java:772,804,818,825`). Reusing the
  key for targeting behavior was an invented divergence. The config key itself stays
  (it is original) for the ENT-A-058 voice work.
- **Proposal:** if pet friendly-fire protection is wanted in 2.0, add it under its own
  config key (e.g. `petsProtectEachOther`), ideally for all tamed OreSpawn mobs rather
  than the Boyfriend alone.
- **Related findings:** ENT-A-055 (closed, Phase D3), ENT-A-058 (voice scope, open).

## MOD-011 — Config-driven per-tier weapon/armor/ore stats (TECH-DEBT / PARITY-GAP)
- **Origin:** ITEM-065 / PN-013 (2026-07-02, Phase D4). The original exposed every
  tier's durability, per-piece armor values, enchantability, damage and drop numbers
  through `get_armorstats`/`get_weaponstats`/`get_orestats` config bindings
  (`orig OreSpawnMain.java:1489-1517`); the port bakes the defaults into
  `ModArmorMaterials`/`ModToolTiers` because NeoForge 1.21.1 freezes those
  registries before config load.
- **Proposal for 2.0:** move armor materials to datapack-driven definitions (1.21.2+
  moves ArmorMaterial to a data registry, making this nearly free on upgrade), or
  apply config deltas at runtime via attribute modifiers on the finished items rather
  than registry values.
- **Related findings:** ITEM-065 (documented per PN-013), ENT-A-045 (stat values
  verified correct in Phase C).

## MOD-012 — Challenge Tower QoL pack: guaranteed prize level + climbable shafts (DELIBERATE 2.0 CONTENT CANDIDATE)
- **Origin:** pre-plan "QA fixes" REMOVED from the parity build (2026-08-08, Phase D5,
  WGEN-051/WGEN-052). The port (a) locked the tower difficulty roll to level 6 so
  every Challenge Tower topped out with the Royal Guardian Sword / Royal armor /
  Prince-or-Princess egg chests, and (b) added scaffolding columns under every
  decoration-room ceiling hole and in the level-6 dirt shaft so the sealed bedrock
  rooms were climbable without bringing blocks.
- **Original behavior:** `GenericDungeon.java:202-205` rolls level 1-6 (P(6) = 5/18 ≈
  28%), so most towers are shorter and prize-less; no climbable block exists anywhere
  in either castle variant — players tower up on their own blocks or ender-pearl.
- **Proposal for 2.0:** keep the original roll but surface the tower's level visibly
  (banner colour / height is already a tell), and reconsider a climb aid that fits the
  original aesthetic (e.g. exposed iron-bar rungs) gated behind a config default-off.
  If a guaranteed-prize variant is wanted, make it a rarer second structure rather
  than changing every tower.
- **Impact:** gameplay-visible (tower height distribution, loot availability,
  traversal difficulty). Effort: small (both changes are localized).
- **Related findings:** WGEN-051, WGEN-052 (both closed, Phase D5); WGEN-043
  (provenance, closed Phase C7).

## MOD-013 — Ancient Dried Egg rehydration block (DELIBERATE 2.0 CONTENT CANDIDATE)
- **Origin:** Phase-10/C7 port invention REMOVED with the SpawnOres pool restoration
  (2026-08-08, Phase D5, WGEN-005/PN-010). A standalone "Ancient Dried Egg" block
  generated at Y −32..32 (1/12 chunks); right-clicking it with a water bucket
  consumed the block and yielded a random egg from a 7-entry dino pool.
- **Original behavior:** no such block exists in 1.7.10. The "Ancient Dried … Spawn
  Egg" names belong to the ~105 per-mob SpawnOres blocks, and rehydration is a
  CRAFTING recipe (water bucket + block → that mob's egg, orig
  OreSpawnMain.java:2665-3021) — now fully restored.
- **Proposal for 2.0:** the in-world right-click rehydration is a nice diegetic touch;
  if revived, make it an alternate interaction on the REAL spawn-ore blocks (right
  click with water bucket = the crafting recipe's result in place) instead of a
  separate generic block, so the per-mob identity is kept. The removed
  `AncientDriedEggBlock` implementation is recoverable from git history.
- **Impact:** gameplay-visible but redundant with crafting; invisible to parity.
  Effort: small.
- **Related findings:** WGEN-005 (closed, Phase D5); ITEM-062 (closed, Phase D5).

## MOD-014 — Procedural spider overhaul (FLAGSHIP 2.0 CANDIDATE — EXPLICITLY POST-PARITY)
- **Scope:** replace the faithful 1.7.10 gait solver (the Phase D2 SpiderRobot/AntRobot
  leg-animation port, orig SpiderRobot.java:111-486 / AntRobot.java:156-510 — see
  ANIM-006, closed) with modern procedural IK leg animation: world-anchored feet,
  per-leg step targeting/overstretch relocation driven by inverse kinematics rather
  than the original's replicated solver. Pair it with MULTI-PART HITBOXES (the vanilla
  Ender Dragon parent/part pattern, already exercised in this repo by the
  King/Queen MultiHitBoxLib integration) so collision follows the model per-limb.
- **Applies to:** SpiderRobot and AntRobot first; potentially the organic spider-type
  mobs (Spider Driver's mount is the same rig; evaluate CaveFisher/Emperor Scorpion
  class rigs once the technique lands).
- **Reference implementation (technique only):**
  https://github.com/TheCymaera/minecraft-spider — a PAPER PLUGIN, reference-only for
  the IK/gait approach; VERIFY ITS LICENSE before reusing any actual code, and expect
  a from-scratch NeoForge implementation regardless (display-entity plugin tech does
  not transplant to a modded entity renderer).
- **Dependencies / touch points:** the D2 `RenderSpiderRobotInfo` architecture
  (entity-side leg state shared by AntRobot/SpiderRobot and their models —
  AntRobot.java:34/48/316, client/ModelSpiderRobot + ModelAntRobot); targeting,
  projectile collision, and damage routing once multi-part boxes exist (parent
  unhittable vs part-forwarding decisions, same questions BOSS-002/007 settled for
  the King/Queen); the existing GameTest entity suites (EntityLogicTests*,
  CoreStatTests' SpiderDriver armor/bite tests, the structure tests spawning pad
  robots) which assert against single-AABB queries and current spawn behavior.
- **Multi-part tech already in-repo (use it, don't hand-roll the dragon pattern):**
  the vendored MultiHitBoxLib + GeckoLib bridge is live on the Queen —
  `data/orespawn/multihitboxlib/hitbox_profiles/the_queen.json` declares
  bone-synched part boxes with per-part damage modifiers;
  `MixinGeoEntityRenderer` attaches `GeckolibBoneInformationCollectorLayer`,
  which reads `GeoBone#getWorldPosition` per render frame, ships
  `CPacketBoneInformation`, and `alignSynchedSubParts()` snaps each
  `MHLibPartEntity` to its bone next `aiStep`. Spider caveat: SpiderRobot/
  AntRobot are vanilla-model entities, so either migrate the rigs to GeckoLib
  to ride that layer, or feed MHLib part positions straight from the IK solver
  — the second is likely cleaner AND server-authoritative (the Queen path
  trusts client-sourced bone positions; a server-side IK solver would not).
- **Parity stance:** contradicts the 1.0 faithful-port guarantee BY DESIGN — the D2
  gait solver is the parity-correct behavior and stays the 1.0 ship. This overhaul is
  a 2.0 feature; strongly consider a config gate ("classic vs modern movement",
  default classic) so the faithful solver remains selectable.
- **Impact:** gameplay- and visually-transformative for the robot line; high effort
  (renderer, entity, AI, collision, tests). Effort: large.
- **Related findings:** ANIM-006 (closed, Phase D2 — the faithful solver this
  replaces); BOSS-002/BOSS-007 (closed — the multi-part precedent); ENT-A-013/014
  (AntRobot ride/stomp behaviors that must survive the swap).
