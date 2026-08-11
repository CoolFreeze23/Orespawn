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
- **Addendum (BUG-025, 2026-08-11):** when implementing the exact dice, the
  original Kraken pickaxe roll is **FORTUNE** uniform(1,5) at 1-in-6 (orig
  Kraken.java:299-303, field_77346_s — the old port helper's "Silk Touch I-V"
  was a mistranslation; field_77348_q/Silk Touch appears nowhere in the file),
  alongside Unbreaking uniform(2,5) at 1-in-2 and Efficiency uniform(1,5) at
  1-in-6. Do **NOT** clamp levels to getMaxLevel(): over-max enchants
  (Unbreaking V, Fortune V, Feather Falling V-IX at :387/:535/:687/:796) are
  authentic 1.7.10 OreSpawn drops; `minecraft:set_enchantments` /
  ItemEnchantments builders accept them (engine cap 255).
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
  - **Extractor (DNA extraction bench)** — the Phase-11 machine that consumed the
    extracting recipes above; removed 2026-08-11 per the MOD-020 ruling and archived
    here so the whole chain lives in one place. `extractor` block ("Extractor",
    strength 3.5, `requiresCorrectToolForDrops`, METAL sound, mineable/pickaxe) +
    `ExtractorBlockEntity`: a no-GUI, hopper-driven 1-input/1-output processor —
    top face = input slot, bottom face = output slot, sides locked; matched the
    input against the custom `orespawn:extracting` recipe type (`ExtractingRecipe`
    codec: `ingredient`/`result`/`processtime`, default 200 t), throttled to 1
    progress increment per server tick, held progress while the output was blocked,
    reset it on input change, and dropped contents on break. Crafted shaped from
    6 iron ingots + piston + redstone block ("I I"/"IPI"/"IRI"). Its third shipped
    recipe, `extracting_trex_dna` (`ancient_dried_egg` → `trex_tooth` @ 400 t,
    the "DNA extraction" use), was removed earlier with the ADE retirement in
    Phase D5. Art was placeholder-grade: no extractor_* textures ever shipped —
    the model was a `cube_all` aliased to vanilla `iron_block`. A 2.0 revival
    should bring real art, in-world feedback (or a GUI), and JEI-visible recipes. removal makes all branch blocks/items vanish from existing
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

## MOD-015 — Duplicator tree growth pacing config (UX / DELIBERATE DEVIATION CANDIDATE)
- **Original:** `orig BlockDuplicatorLog.java:25` (`func_149675_a(true)` — random
  ticks) + `orig Trees.java:121-182` — exactly ONE world write per random tick:
  soil below (:125-139, the y−2/y−3 probes are dead code), 2 trunk writes
  (:140-154), leaf cap (:155-159), 3×3 ring of 8 (:160-166), then one copied
  block per tick from the 5×5 footprint (±2 of the trunk, 20-try source /
  20-try air dest, :167-181). The port is write-for-write identical
  (`src/main/java/danger/orespawn/block/BlockDuplicatorLog.java:41-116`).
- **What's dated:** faithful pacing is genuinely slow: at `randomTickSpeed=3`
  each block random-ticks every 4096/3 ≈ 68.3 s mean, so the full tree
  (11 successful ticks) averages ~12.5 min (0.63 MC days; Erlang-11 P99 ≈
  21.5 min) and the first copy adds ~2 min more — and only if a source block
  sits INSIDE the ±2 footprint. The manual session (i007/ITEM-027) read this
  as "super slow" (sleeping skips the game clock with ~0 random ticks, which
  inflates the perceived MC-day count further).
- **Proposal:** add config `duplicator_tree_growth_steps` (int, default 1 =
  faithful): perform N growth/copy writes per `randomTick` call in
  `BlockDuplicatorLog.randomTick` (loop `duplicatorTree` N times), giving a
  linear speed-up (e.g. 4 → ~3 min mean to full tree) without touching the
  tick engine. Default keeps parity.
- **Impact:** pacing-visible only; trivial effort.
- **Related findings:** ITEM-027 (closed faithful-pacing, 2026-08-11);
  WGEN-044 (the one-write-per-random-tick closure this preserves).

## MOD-016 — Chainsaw attached-tree-only felling (UX / DELIBERATE DEVIATION CANDIDATE)
- **Original:** `orig UltimateSword.java:351-371` (`func_150894_a`) — a blind
  fixed box i=−5..5, j=−5..+10, k=−5..5 (11×16×11) centered on the broken
  block; every `canCrush`/`isLeaves` block inside is deleted + dropped with NO
  connectivity or attachment test; `:383-394` — the last-held-block leaf flag
  selects leaf-only mode. The port replicates the box exactly
  (`src/main/java/danger/orespawn/item/Chainsaw.java:142-158`, leaf flag
  `:216-224`), so neighboring trees inside the box are cut BY DESIGN — the
  user's attached-only preference (i008/ITEM-037) is a modernization request,
  not a parity defect.
- **Proposal:** add config `chainsaw_attached_only` (bool, default false =
  faithful 11×16×11 box). When true, `Chainsaw.mineBlock` replaces the box
  scan with a BFS flood-fill seeded at the broken block, expanding through
  26-neighbor-adjacent blocks that pass `canCrush` (or `isLeaves` in leaf
  mode), clamped to the original box extents (x/z ±5, y −5..+10) and a
  visited cap of 11·16·11 = 1936 so the worst case equals current behavior;
  only the connected tree drops. Drops/sound identical; standing neighbors
  untouched.
- **PROVENANCE NOTE (its own delta, predates this entry):** the port maps
  `canCrush`/`isLeaves` to modern `BlockTags.LOGS`/`LEAVES`
  (`Chainsaw.java:177-213`), which also cover acacia/dark-oak — 1.7.10's sets
  (`orig UltimateSword.java:253-349`) listed only `Blocks.log`/`leaves`
  (log2/leaves2 absent), so those families were NOT crushed originally. A
  reasonable family mapping already implied by the C6 report; recorded here
  as a deliberate tag-mapping divergence, independent of the felling-scope
  config above.
- **Impact:** gameplay-visible (multi-tree clearcuts vs single-tree felling);
  small effort.
- **Related findings:** ITEM-037 (tree-scope half closed faithful, 2026-08-11;
  held-model half still in the art recheck).

## MOD-017 — Instant Garden click-anchored Y (UX / DELIBERATE DEVIATION CANDIDATE)
- **Original:** `orig InstantGarden.java:41-43` (`pposy = (int)Player.posY` —
  server-side FEET Y), `:48-50` (`y = pposy`; the clicked Y parameter is NEVER
  read for placement), `:73-81` (garden grass floor at feetY−1, crops at
  feetY). The port is identical
  (`src/main/java/danger/orespawn/item/InstantGarden.java:39-42, 59-69,
  73-96`): both versions anchor the plot to the player's feet and ignore the
  clicked block's Y entirely. Clicking a block that sits AT foot level
  (upslope ground, side of a ledge) therefore puts the floor one below the
  clicked block — exactly the "1 block lower" of i010/ITEM-047, reproduced
  1:1 from 1.7.10.
- **Proposal:** add config `instant_garden_anchor_clicked` (bool, default
  false = faithful player-feet anchor). When true, `InstantGarden.useOn`
  computes `y = clicked.getY() + 1` when the clicked block is solid (garden
  grass floor placed AT the clicked block's Y, crop surface flush with the
  clicked block's top — the "same-Y" the user asked for), falling back to
  the feet anchor when clicking replaceable blocks (grass/snow). One-line
  change at `InstantGarden.java:41` behind the config; direction gate,
  layout and rows untouched.
- **Impact:** placement-feel only; trivial effort.
- **Related findings:** ITEM-047 (Y-level half closed faithful, 2026-08-11;
  crop-texture half still in the art recheck).

## MOD-018 — Rocks always throw on use (UX / QoL CANDIDATE)
- **Original:** 1.7.10 rocks were dual-mode items, never blocks:
  `orig ItemRock.java:29-73` (`func_77659_a` — throws an EntityThrownRock
  with a bow sound, but fires only when NOT pointing at a block in reach);
  `:75-128` (`func_77648_a` — fires FIRST whenever the crosshair is on a
  block within reach and PLACES a pet Rock entity at the clicked block
  ±0.5-centered, y+1.01, random yaw, consuming the item — with NO obstruction
  check, so clicking a wall spawns the rock inside the block above the
  clicked one). The port replicates the split with the same coordinates
  (`src/main/java/danger/orespawn/item/ItemRock.java:26-39` throw, `:45-65`
  place), quirks included — the i019 observation ("clicking ON a block within
  reach PLACES the rock (even inside glass); must aim at air to throw") is
  faithful behavior, which reads as a misfire in normal combat use.
- **Proposal:** add config `rocksAlwaysThrow` (bool, default false =
  faithful): when true, `ItemRock.useOn` delegates to the throw path so
  aiming at a nearby block no longer places a pet Rock; pet-Rock placement
  moves to sneak-use-on-block. Applies to all 12 ItemRock types incl.
  TNT/crystal variants.
- **Out of scope here (belongs to the i018 invisible-projectile fix):** the
  thrown rock renders invisible in flight (`NoopProjectileRenderer` on
  ENTITY_THROWN_ROCK, `OreSpawnClient.java:179`) vs orig
  `RenderThrownRock.java`'s camera-facing per-type sprite at scale 0.5; and
  the throw sound (port SNOWBALL_THROW fixed 0.4 pitch, `ItemRock.java:33`,
  vs orig `random.bow` vol 0.5 pitch `0.4/(rand*0.4+0.8)`).
- **Impact:** combat-feel; small effort.
- **Related findings:** ENT-D-025/026/027, ENT-K-076 (i019 placement half
  closed faithful, 2026-08-11); i018 (the shared invisible-thrown-item
  renderer defect, separate scope).

## MOD-019 — Experience gear self-repair / built-in mending (USER-REQUESTED 2.0 CANDIDATE)
- **Origin:** manual-session i009 request (ITEM-040/057 reconciliation) — the
  user expected Experience armor to be REPAIRED while holding an experience
  tool, plus mending-like behavior. The original has NEITHER:
  `orig ExperienceSword.java:55-103` is an XP trickle only — with the sword
  anywhere in the inventory, a 1-in-60 inventory-tick roll checks each worn
  Experience armor piece and grants +1 player XP per piece on per-piece
  sub-rolls (helmet 1/10, chest 1/20, leggings 1/30, boots 1/40) with a
  single portal particle at the piece's height. No durability repair, no
  sword drain, anywhere. The port reproduces the trickle faithfully (the
  2026-08-11 checklist amendment documents it), so any repair behavior is
  new content, not a fix.
- **Proposal:** config-gated candidate, DEFAULT OFF — e.g.
  `experience_gear_mending` (bool, default false): when true, the same
  1-in-60 trickle rolls repair damaged worn Experience pieces (and/or the
  held sword) mending-style instead of — or before — granting the +1 XP,
  converting the existing roll cadence into durability. Keeps the faithful
  trickle as the default; numbers to be tuned against vanilla Mending's
  2-durability-per-XP rate.
- **Impact:** gameplay-visible for the Experience tier; small effort.
- **Related findings:** ITEM-040/ITEM-057 (docs amended 2026-08-11 — the old
  "armor repairs / sword drains" checklist text was an extraction inference);
  the i009 RECONCILIATION PLAN procedure in TESTING_CHECKLIST §(e).

## MOD-020 — Extractor block: port invention REMOVED (ruling applied 2026-08-11)
- **Ruling applied 2026-08-11 — REMOVED per user decision**, under the standing
  no-procedural-fabrication ruling (same treatment as kyanite/MOD-009 and the
  Ancient Dried Egg block/MOD-013). The complete design is folded into MOD-009's
  kyanite 2.0 archive (see the "Extractor (DNA extraction bench)" sub-bullet there)
  so a 2.0 revival has the full picture in one place.
- **Origin:** Phase-11 port invention — no Extractor class, art, or mechanic exists
  anywhere in the 1.7.10 reference dump (verified 2026-08-11 during the asset audit
  and re-verified at removal; the block's Javadoc previously claimed a fictitious
  1.7.10 provenance, corrected the same day). Its companion "extracting_trex_dna"
  recipe was already removed as invented in Phase D5, and its remaining output chain
  (kyanite / pink tourmaline) was deleted with the MOD-009 branch — the block had
  no shipped recipes left to process.
- **What was removed:** the block/BE/recipe-type classes (`Extractor`,
  `ExtractorBlockEntity`, `ExtractingRecipe`, `ModRecipes` — the
  `orespawn:extracting` type + serializer existed solely for this block, so the
  whole `danger.orespawn.recipe` package is gone), the ModBlocks/ModItems/
  ModBlockEntities registrations, creative-tab entry, OreSpawnMod register call,
  lang key, blockstate + block/item models, crafting recipe, loot table, and the
  `minecraft:mineable/pickaxe` tag entry. No textures existed to delete — the
  2026-08-11 asset wave had pointed the model at vanilla `iron_block` per the
  no-invented-art rule rather than inventing extractor_* art. Full file list in
  FIX_LOG TF-031; asset_audit.py stays at 0 errors post-removal.
- **Impact:** as predicted — no worldgen places it, so world compat risk is limited
  to player-placed instances (stripped on load; same one-way break class as MOD-009).
- **Related findings:** PN-009 (invention-removal precedent), MOD-009 (archive
  location), MOD-013, FIX_LOG TF-031.

## MOD-021 — Phase 14 wiki-only mobs: optional content behind `phase14ContentEnable` (ruling applied 2026-08-11)
- **Category:** VANILLA-INTEGRATION / provenance
- **Ruling (2026-08-11):** VampireButterfly, AppleCow, and GoldenAppleCow are
  wiki-documented ("Added Mobs" page) but have **no class in the 586-file
  1.7.10 source dump** — they cannot ship enabled in a source-verified parity
  build, but their community provenance distinguishes them from pure port
  inventions (Extractor/MOD-020, kyanite/MOD-009). Disposition: code stays
  registered; default experience excludes them. `phase14ContentEnable`
  (default **false**, `[tweaks]`) gates natural spawns (ModSpawnControl —
  AppleCow/GoldenAppleCow additionally respect the original `CowEnable`,
  orig OreSpawnMain.java:4609) and creative spawn-egg visibility
  (ModCreativeTabs). They have no recipes. VampireButterfly has no natural
  spawn data at all (egg-only); its ModSpawnControl entry is a guard.
- **NOT gated — EnchantedAppleCow:** despite the Phase-14-era name, this is
  the original **EnchantedCow** (orig OreSpawnMain.java:3599; display name
  "Enchanted Golden Apple Cow", :2765/:5250 — the port id `enchanted_apple_cow`
  maps the display name). It keeps its source wiring: EntityCage :352 entry,
  EasterBunny EnchantedCowEgg drop, SpawnOres c30, dimension-biome spawns,
  and (TF-033) the restored overworld per-biome spawns.
- **Cephadrome porkchop-tame (removed, archived here):** the wiki "porkchop
  tame" was implemented Phase 14 as a persistent `DATA_TAMED` flag +
  porkchop-only feed gate + TemptGoal + player-aggro immunity. The source
  (orig Cephadrome.java:878-904) has **no tame state**: any raw
  beef/chicken/porkchop heals to full and arms a one-ride `wasfed` gate
  consumed on mount. Source restored as TF-032. 2.0 revival sketch: re-add
  `DATA_TAMED` behind this same flag, porkchop-exclusive, with the tame
  suppressing `shouldattack`/`badmood` permanently and enabling a saddle
  slot — the removed implementation in git history (pre-TF-032) is complete.
- **Impact:** default worlds lose 3 spawn-egg items from creative and the 2
  cows' natural spawns (wiki profile: all-overworld w6 1-2 / w2 1-1);
  existing saved entities are untouched (registrations remain). Flag-on
  restores the previous behavior exactly.
- **Related:** TF-032 (Cephadrome), TF-033 (cow overworld spawns), TF-034
  (lump-file residuals), MOD-009/MOD-020 (invention-removal precedents).

## MOD-022 — Transient combat state is intentionally unpersisted (TECH-DEBT, parity)
- **Category:** TECH-DEBT (faithful non-persistence, documented against relitigating)
- **Original behavior:** none of these classes override writeEntityToNBT for the
  fields in question, so all of it reset on relog/chunk-reload in 1.7.10:
  TheKing stream counters/backoff/ticker/attdam/revenge/head-scan (persists only
  KingHomeX/Z, GuardMode, PlayerHits, IsEnd, EndCounter — orig :1031-1039);
  TheQueen mood (persists KingHomeX/Z, GuardMode, PlayerHits, MeanMode —
  :964-980); Godzilla jump/stream/head state (NO NBT overrides at all);
  Kraken hitByPlayer/callReinforcements (persists only LongEnough, :189-197);
  Mothra stuck/heal tickers (empty super-delegates, :287-293); GiantRobot
  reloadTicker; WormMedium upcount/downcount (no overrides).
- **Port:** reproduces each save-set key-for-key. Verified independently in the
  Phase E1 triage (BUG-014/016/017/023/024/026/030 all VERIFIED-CORRECT).
- **2.0 proposal:** optional "relog-stable bosses" config persisting the King/
  Godzilla/Queen/Kraken combat state (QueenMood, ReloadTicker, etc.).
  Behavior-affecting; default must stay original. Effort: low. Notable quirk
  worth surfacing in any future changelog: a mid-fight relog below 1/4 HP
  permanently disarms the Kraken reinforcement wave in both versions.
- **Related:** BUG-014, BUG-016, BUG-017, BUG-023, BUG-024, BUG-026, BUG-030.

## MOD-023 — TheKing registry-dump death loot cap (BALANCE, opt-in)
- **Original (faithful, shipped):** ~300 random registry item entities on death
  — 150 random items + 150 random block-items from the ENTIRE registries
  (other mods' technical items included), scattered at y+12 within ±20
  (orig TheKing.java:200-226; port dropCustomDeathLoot :1318-1340 — code-side
  because it is inexpressible as loot JSON). Known lag spike + exploit-grade
  loot by design; BUG-015 closed VERIFIED-CORRECT.
- **2.0 proposal:** config toggle capping total drops (e.g. 32) and/or
  restricting sampling to a curated pool. Default remains original behavior.
- **Related:** BUG-015.

## MOD-024 — Modern-idiom opt-ins from the E1 triage (VANILLA-INTEGRATION)
Faithful-but-dated mechanics confirmed original in the BUG triage; each could
take an off-by-default modern variant in 2.0:
- **Vortex smooth pull** (BUG-019): 1.7.10 addVelocity never set
  velocityChanged, so players felt the pull only as damage-tick yanks; setting
  `hurtMarked = true` after push() would make a smooth tractor beam that never
  existed. Behavior-changing, needs sign-off.
- **Queen LoS floor-sampling** (BUG-027): myCanSee keeps the original's (int)
  truncation (wrong column at negative coords, orig :880-884); the modern fix
  is BlockPos.containing.
- **Kraken polite weather** (BUG-018 residue): skip the re-force when already
  thundering; the shipped fix already restored the orig 300-tick refresh and
  no-upgrade-of-plain-rain semantics.
- **Vortex client-heal gate** (BUG-031): wrap the 1-in-200 heal in
  !isClientSide — cosmetic only, both engines self-correct.
- **Vortex scan caching** (BUG-022): owned by OPT-004 (Phase F, behavior-
  affecting: aggro/particle onset latency).
- **Related:** BUG-018, BUG-019, BUG-022, BUG-027, BUG-031, OPT-004.

## MOD-025 — Uniform bone-synced hitboxes for King/Godzilla (TECH-DEBT, 2.0 polish)
- **Current (final for 1.x, BOSS-044):** TheKing and Godzilla use manual
  `OreSpawnPartEntity` layouts (envelopes verified in BOSS-002/007); TheQueen
  uses the MHLib bone-synced profile (hitbox_profiles/the_queen.json); the
  faithful 1.7.10 head sidecars (19.9x10 / 9.9x10 gaze-tracking boxes,
  BOSS-003/008/014) coexist with both.
- **Proposal:** author the_king.json / godzilla.json MHLib profiles with the
  Queen's damage-multiplier scheme, migrate off manual part offsets, and
  evaluate folding the sidecar role into a bone-tracked far-head part.
  Visual-fidelity work; needs in-game verification per boss.
- **Related:** BOSS-044, BOSS-002/007, MOD-014 (procedural spider flagship).

## MOD-026 — Gallop follow-up bundle: gait spec + COM/support-polygon gravity + tilt polish (2.0 spider, post-S5)

Banked from the reference creator's own video series
(phase_s_reports/reference_video_notes.md, ingested 2026-08-11) per the
owner's post-S3b ruling: S3b's scalar lift (grounded-fraction-capped PD
spring) is committed, gated and stability-proven, and the polygon system
was co-designed with gallop in the reference — so the two land together
as one follow-up, after S5, if pursued.

- **Gallop gait** (Q2-deferred): horizontal leg pairs with a dominant
  leg — when the dominant exits its trigger threshold BOTH legs of the
  pair swing together; cooldown between vertical pairs prevents
  leap-frogging; cooldown between horizontal pairs tunes gait feel;
  selection is a manual flag, never speed-automatic. (Video 2; matches
  the S1 §1 extraction.)
- **Gravity v2 — COM + support polygon** (the gravity half): pick a
  believable center of mass; build the polygon of grounded feet; COM
  inside → full counter-gravity, COM outside → the force applies at an
  angle from the CLOSEST POINT on the polygon, so the body tips toward
  its unsupported side and falls off ledges convincingly instead of
  hovering. Point-in-polygon via odd/even ray-cast. Replaces S3b's
  scalar sag (which never TIPS: four right feet grounded gives 50%
  straight-up lift where the polygon rolls the body leftward off the
  ledge). **Degenerate-polygon note:** with ≤2 grounded feet the
  polygon collapses to a segment or point — the inside test must treat
  degenerate polygons as "outside" (closest-point force still
  well-defined); the ray-cast parity test needs those cases pinned
  explicitly or 1-2-footed spiders divide by zero on the tip axis.
- **Spring-damper tilt** (optional polish, same bundle): replace/augment
  the 0.3 first-order low-pass with a damped spring so tilt overshoots
  and settles organically (the reference's accidental-feature wobble);
  natural home for hit-reaction tilt (damage impulse into the spring)
  if contact work ever extends to the spiders.
- **Per-segment initial rotation** (S5+ upgrade path, only if a rig
  demands it): promote S2's single scalar knee bias to a per-segment
  vector applied at the pre-straighten stage (Video 3's mechanism) —
  the ant or organic rigs are the candidates that could need it.

Parity stance: all of this is modern-mode-only polish; classic remains
untouched by law. The aim-offset composability note from the same doc
was folded into the S3 design amendment instead (live now, not banked).
