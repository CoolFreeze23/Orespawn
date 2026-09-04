# MODERNIZATION_NOTES — "OreSpawn Modernized" 2.0 Design Backlog

A design backlog that, since the 2026-09-04 rulings, also records what is live behind the
`[modern]` config: an entry whose heading says ACCEPTED / implemented is shipped behind its
key with the default the heading states; every other entry is planning output only. Per
`IMPLEMENTATION_PLAN.md`: the parity pass replicates original behavior faithfully (including
original bugs where they are gameplay-defining); this file collects everything that deserves
a curated redesign afterward.

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
- **Master-override amendment (2026-09-04):** `phase14ContentEnable` is effective only
  while `modern.enabled` is true (MOD-029's master-override ruling); the key keeps its
  name and place. The master defaults to true and defers to this key, so
  `phase14ContentEnable = true` alone is enough again unless the master was set false,
  which forces this content off together with every other 2.0 feature.
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

## MOD-027 — Ant rider seat raise (RIDE-FEEL, 2.0 sitting OBS-1)

The 2.0 verification sitting found the rider sits INSIDE the ant's
visual shell (F5 required to see anything). This is 1.0 PARITY by
construction: AntRobot.positionRider is not mode-gated — the same
faithful orig seat math (1.25 behind center, seat 0.55 with the
TF-029 player −0.5 offset ≈ 0.05 above the anchor) runs in classic
and modern alike, and the modern visual dynamics never move the
rider (ridden sag clamped ±0.15, visual-only). SHIPPED in S7a (2026-08-13): the
modern-only seat raise (+0.9, composed through the S3b body
transform) puts the rider on the ant's back; the classic seat stays
bit-identical to 1.0. The spider's seat turned out to be a LOST
PARITY FEATURE (the orig positionRider was never ported) — restored
for both modes in the same slice.

## MOD-028 — Per-segment leg part boxes (COMBAT-FEEL, 2.0 sitting OBS-2)

The sitting judged the one-box-per-leg hitboxes (design ruling Q3) "a
little misaligned" from the visual legs — the accepted design cost:
one axis-aligned cube on the lower-segment chord midpoint covers a
fraction of a three-segment leg and protrudes slightly off steep
segments. Upgrade candidate: two (or three) boxes per leg on the
per-segment chord midpoints — the solver already produces every
joint position each tick, so the feed generalizes directly; costs
8-16 more part entities per spider and a profile/registrar rev. The
S6a suite pins live boxes to the solver chord (s6_part_anchor_chord
_pin) so alignment drift is caught regardless.

## MOD-029 — Mothra enlarged root hitbox as the modern-mode default (ACCEPTED 2026-09-03: "modern-mode default; classic keeps 5x2")

- **Origin:** ENT-S-095 batch 2 restored Mothra's 1.7.10 root box 5.0 x 2.0 (orig
  Mothra.java:65). The port had registered 6 x 3 with this comment:
  ```
    // 1.7.10 func_70105_a: Mothra = 5.0 x 2.0. We bump to 6 x 3 so the
    // wing PartEntities (which extend +/-6 sideways) read correctly against
    // the root hitbox during cross-biome target sweeps.
  ```
- **Reason on record:** the wider root box was meant to keep the wing part entities
  "reading correctly" against the root during target sweeps. The code does not bear
  it out today: the parts are placed from the root position and size themselves, and
  the sweeps that inflate the root box (findSomethingToAttack, checkSpawnRules) now
  match 1.7.10 exactly. If the wider root is still wanted for feel, it is a modern
  option, not parity.
- **Ruling (2026-09-03):** "MOD-029: accepted as the modern-mode default; classic keeps
  5x2." Ruled the same day: "Artist animations are a 2.0 feature behind the modern
  config; classic stays code-driven parity" — so the switch is the modern config, not
  a per-feature key of its own.
- **Switch (implemented 2026-09-03; semantics amended by the master-override ruling of
  2026-09-04, see the bullet after the sub-keys):** no master modern switch existed before.
  The earlier 2.0 per-feature keys (`spiderMovement`, `mountCamera`, `phase14ContentEnable`; the
  MOD-024 candidates are unimplemented proposals, not keys) keep their names and their
  `[tweaks]` section, but are effective only while the master is on.
  `OreSpawnConfig` (COMMON spec) gains a `[modern]` section:
  - `modern.enabled` (`MODERN_ENABLED`, default **true**): the master. It defers to the
    per-feature keys, so a default config runs every modern-mode feature at its own
    key's default; `modern.enabled = false` forces each of them to its classic/off
    value at once, whatever its key says. Phase G artist animations will hang off this
    same master.
  - `modern.mothraWideRootHitbox` (`MODERN_MOTHRA_WIDE_ROOT_HITBOX`, default **true**):
    the MOD-029 sub-key. `OreSpawnConfig.mothraWideRootHitbox()` is the single
    `master && sub-key` evaluation.
- **Master-override ruling (owner, 2026-09-04):** "modern.enabled: master override only.
  Off forces all modern features off; on defers to existing per-feature keys, which keep
  their names. New features register under [modern]." Supersedes the earlier "untouched
  and independent of the master" shape. Implemented as one effective-value helper per
  modern feature in `OreSpawnConfig`, each the single read for its feature (a key is never
  consulted without the master): `spiderMovement()` (`SpiderMovement.CLASSIC` while the
  master is off, else `tweaks.spiderMovement`; routed through both robots' construction
  snapshots, still one read, so the S4 ctor-tear rule and BOSS-017 hold), `mountCamera()`
  (`MountCameraState.targetDistance`; a client-side read of the client's own COMMON file, so on a
  multiplayer client the master must be set locally as well), `phase14ContentEnable()` (the three `ModCreativeTabs`
  spawn-egg gates and the `ModSpawnControl` natural-spawn suppliers), `mothraWideRootHitbox()`
  unchanged. No per-feature key renamed, moved or re-defaulted; the `[modern] enabled` comment
  states the semantics and every per-feature key says "Effective only when [modern] enabled is
  true". **Default (owner ruling, later the same day):** "modern.enabled defaults to true in
  code — the master defers to per-feature keys by default and only forces classic when set
  false." The master was introduced 2026-09-03 with default false and flipped to true by that
  ruling. A default config therefore runs the modern robots (`tweaks.spiderMovement` default
  MODERN) with the riding camera (`tweaks.mountCamera` default true); `modern.enabled = false`
  is the one-line switch to the exact 1.7.10 experience for every 2.0 feature at once,
  `spiderMovement = "CLASSIC"` still works per feature, and `phase14ContentEnable = true` alone
  is enough again unless the master was set false. Pins:
  `ModernMasterOverrideTests` (own batch `modernMasterOverride`): the off/on truth table for
  all four helpers, the routed construction read (master off + key MODERN constructs
  classic robots with zero parts; master on constructs modern) and the code default
  (`MODERN_ENABLED.getDefault()` true; every value at its default reads MODERN with the
  camera on); every existing modern-mode gait test raises the master together with the key
  and restores both (AntGaitTests, HitboxPartTests, PartInteractTests, RideTests,
  S6LegFixTests, SpiderGaitTests).
- **Mothra wiring:** `Mothra#modernWideRoot` snapshots that value in the constructor and
  calls `refreshDimensions()` (the BOSS-017 King/Kraken PlayNicely-snapshot pattern,
  orig TheKing.java:85-89); `Mothra#getDefaultDimensions` returns
  `EntityDimensions.scalable(6, 3)` — exactly what the pre-batch-2 `.sized(6.0f, 3.0f)`
  registration produced — when the snapshot is true, else `super` (the registered
  classic 5.0 x 2.0). The `ModEntities` registration stays 5 x 2. A config flip reaches
  newly constructed/loaded Mothras only; live ones keep their box. The spec is COMMON,
  so, like the King/Kraken snapshot, each side reads its own file at construction.
- **What the wider root changes:** nothing in the four `OreSpawnPartEntity` parts (body
  4x3, wings 5x1.5, head 2x2 — own sizes, placed from the root POSITION in
  `positionPart`), and no code in the two root-box sweeps: `findSomethingToAttack`
  (inflate 15/20/15, orig :489) and `checkSpawnRules` (inflate 64/32/64, orig :329)
  read the live bounding box, so in modern mode each sweep is 0.5 wider per side and
  1.0 taller than 1.7.10 — exactly the old port behaviour, now behind the switch;
  classic mode keeps the 1.7.10 sweeps.
- **Pins:** `HitboxDimsParityTests#s095_mothra_dims_both_modes` (classic 5 x 2, both
  PlayNicely states) forces the master off around its pin and restores it, because on
  real defaults (master true since the ruling of 2026-09-04, sub-key true) a fresh Mothra is
  the modern 6 x 3; `MothraModernDimsTests` (own batch `mothraModernDims`) pins the modern box
  and the master/sub-key truth table with explicit flags; `ModernMasterOverrideTests` pins
  the master's default and deferral.
- **Related:** ENT-S-095 (batch 2), BOSS-017, MOD-024, Phase G ruling 2026-09-03.

## MOD-030 — OreSpawn throwables join `#minecraft:impact_projectiles` (VANILLA-INTEGRATION; ruling applied 2026-09-04)

- **Origin:** ENT-S-098's TAG MEMBERSHIP paragraph: the ThrowableProjectile family sat outside
  `minecraft:impact_projectiles`. Owner ruling 2026-09-04: "Throwables join impact_projectiles
  as vanilla-consistent behavior with no parity obligation; record as a MOD note."
- **What the tag does:** exactly one thing. `Projectile.mayBreak(Level)` is `type in
  #impact_projectiles && gamerule projectilesCanBreakBlocks` (1.21.1 bytecode), consulted by
  `DecoratedPotBlock`, `ChorusFlowerBlock` and `PointedDripstoneBlock` in `onProjectileHit`.
  Vanilla lists `#arrows`, firework rockets, snowballs, both fireballs, eggs, tridents, the dragon
  fireball, wither skulls and both wind charges.
- **Change:** `data/minecraft/tags/entity_type/impact_projectiles.json` (`replace: false`) adds
  `orespawn:laser_ball`, `acid`, `ice_ball`, `dead_irukandji` (LaserBall and its three
  subclasses), `water_ball`, `thunder_bolt`, `sunspot_urchin`, `ink_sack`, `shoes`, `thrown_rock`
  next to the existing `better_fireball`. NOT `bertha_hit` (the invisible swing proxy, never a
  flying object) and NOT `cage` (the capture bobber, whose landing is the capture). The arrows
  are untouched (they enter vanilla's list only via `#arrows`, ruled separately: no).
- **Effect:** a thrown laser / acid / ice ball, dead irukandji, water ball, thunderbolt, urchin,
  ink sack, shoe or rock that lands on a decorated pot, chorus flower or pointed dripstone breaks
  it when `projectilesCanBreakBlocks` is on, as a snowball or egg would. No damage, sound,
  particle or impact code changes. Those three blocks do not exist in 1.7.10, so there is no
  parity obligation; applies in both modes — vanilla consistency, not a 2.0 feature, so no config
  key and not under the `[modern]` master.
- **Pin:** `ProjectileTypeParityTests#tags_throwables_join_impact_projectiles_bertha_hit_and_cage_stay_out`
  (batch `projectileTypeParity`; snowball / fishing bobber as the vanilla controls).

## MOD-031 — Fireball fire respects mobGriefing (ACCEPTED 2026-09-04, implemented, default ON; classic stays 1.7.10)

- **Origin:** ENT-S-104's ruling: "104 restores 1.7.10 fire behavior and files a MOD proposal for a
  config-gated 'fire respects mobGriefing' option."
- **Ruling (owner, 2026-09-04):** "MOD-031: accepted as a modern option, default on; classic stays 1.7.10."
  The proposal had said default false; the ruling overrides it. With the master's default true (the
  master-override and default rulings of the same day) a default config runs the option.
- **Classic (implemented; the behaviour while the master or the key is off):** orig BetterFireball.java:261-263
  sets fire on the air side of a struck block for every shot, and :266 explodes with fire = true unconditionally;
  only block destruction follows `mobGriefing`. A server with mobGriefing off still gets fire from the bosses'
  fireballs — 1.7.10's exact behaviour, and what the port does with `modern.enabled = false` or
  `fireRespectsMobGriefing = false` (`BetterFireball.onHitBlock` / `onHit`, the ENT-S-104 paths: the same calls
  on that branch, no `canEntityGrief` call, no EntityMobGriefingEvent posted).
- **Switch (implemented 2026-09-04):** the `[modern]` key `fireRespectsMobGriefing`
  (`OreSpawnConfig.MODERN_FIRE_RESPECTS_MOB_GRIEFING`, `BooleanValue`, default **true** per the ruling), read
  only through the effective-value helper `OreSpawnConfig.fireRespectsMobGriefing()` = `MODERN_ENABLED && key`,
  next to `mothraWideRootHitbox()` (master-override ruling 2026-09-04: new features register under [modern]; the
  master off forces classic). Two gated sites, each reading the helper at impact (not snapshotted: a fireball
  lives 600 ticks at most; no BOSS-017 concern): `BetterFireball.onHitBlock` places the face fire only if
  `EventHooks.canEntityGrief(level, getOwner())` for every owner (the gate vanilla `LargeFireball.onHit` applies;
  `SmallFireball.onHitBlock` gates only Mob owners — the port is the stricter of the two): the gamerule,
  through EntityMobGriefingEvent for a non-null owner), and `BetterFireball.onHit` passes
  `fire = EventHooks.canEntityGrief(level, getOwner())` to `Level.explode` (vanilla `LargeFireball.onHit`'s
  flag), keeping the null source and the MOB interaction. Key comment: "MOD-031: OreSpawn fireballs place no
  fire beside the block they hit and their blast scatters no fire while the mobGriefing gamerule is off (as
  vanilla ghast and blaze shots behave). Only takes effect while modern.enabled is true; classic mode always
  lights fires as 1.7.10 did (orig BetterFireball.java:261-266). On by default (owner ruling 2026-09-04); set
  false to keep the 1.7.10 fire behaviour in modern mode too. Read at impact, so a change applies to the next
  shot that lands." The `[modern] enabled` comment and javadoc list the key and the helper with the other four.
- **Effect when effective (a default config):** with mobGriefing off, OreSpawn fireballs neither light the
  block they hit nor scatter fire from their blast; damage, the 5-second ignite of a hit entity (orig :227/:230
  — vanilla fireballs ignite targets regardless of the rule too) and the explosion itself are unchanged. With
  mobGriefing on, identical to classic.
- **Not covered:** explosion block destruction (already the gamerule's in both modes, through MOB).
- **Pin:** `FireballModernFireTests#mod031_fire_respects_mob_griefing_three_scenarios_in_sequence` (own batch
  `fireballModernFire`, timeoutTicks 400). The rule and the config are global and batch-mates run concurrently,
  so ONE test runs the three scenarios back to back — each a big shot (setBig, setNotMe) into the ENT-S-102
  obsidian wall over the ENT-S-104 dirt hearth, read 40 ticks after launch, the arena rebuilt between flights —
  and restores the master, the key and the rule in a finally that runs on every path: (a) master on + key on +
  mobGriefing off → no face fire (the cell is read at the blast's `ExplosionEvent.Start`, after onHitBlock and
  before any blast could touch it, and again after the window), explosion fire flag false (reflection on
  `Explosion.fire`, as the s104 pins), KEEP, wall and hearth intact; (b) master on + key on + mobGriefing on →
  face fire present at the blast's start, fire flag true, a DESTROY kind, the hearth gone and the face cell blown
  clear — the classic result of `s104_big_shot_with_mob_griefing_on_...`; (c) master off + key on + mobGriefing
  off → face fire present at the start and still standing after, fire flag true, KEEP, wall and hearth intact —
  classic stays 1.7.10 whatever the key says (the s104 mobGriefing-off result). Each scenario asserts its rule
  and config preconditions at launch and at the check, and the test pins the key's code default (true).
- **Harness consequence:** on a default config the option is effective, so
  `ProjectileTypeParityTests#s104_big_shot_with_mob_griefing_off_still_carries_fire_and_leaves_the_wall_intact`,
  which pins the classic rule-off result (face fire and fire flag with the rule off), has to force the key (or the
  master) off around its window and restore it — the `HitboxDimsParityTests#s095_mothra_dims_both_modes`
  idiom for MOD-029; the small-shot and rule-on s104 pins read the same in both modes.
- **Status:** IMPLEMENTED 2026-09-04 (the key, the helper, both gated sites, the pin); the classic branch is orig.

## MOD-032 — Godzilla spares its boss peers (ACCEPTED 2026-09-04 under the T9 ruling, implemented, default ON; classic stays 1.7.10)

- **Origin:** targeting ledger batch T9, row 8 (`phase_g_reports/targeting_survey_2026-09-04.md` §2 Godzilla
  "allies / species exclusions", §T9; the split `phase_g_reports/targeting_t9_split_2026-09-04.md` §1 row 8, §2 A1).
  Port `Godzilla.isSuitableTarget` refused, after the orig eight names, `target instanceof Mothra` and
  `MyUtils.isBigBoss(target) || MyUtils.isRoyalty(target)` under the comment "Don't pick fights with peers — Mothra,
  the royal couple, and other OreSpawn bosses are tracked separately so Mobzilla doesn't grief boss arenas and so
  co-existing bosses don't cancel each other out." Commit a87c0649 (Phase 4F): "Added Mothra, MyUtils.isBigBoss(),
  and MyUtils.isRoyalty() to the isSuitableTarget ignore list so co-existing bosses don't grief each other and
  Mobzilla doesn't tear up royal arenas." A documented reason; no AUDIT / FIX / MOD entry until this one.
- **Ruling (owner, 2026-09-04):** "T9: documented reason → MOD record behind modern; undocumented → removed from
  classic." Default ON, the MOD-029 / MOD-031 precedent.
- **Classic (implemented; the behaviour while the master or the key is off):** orig Godzilla.java:448-471 — the
  eight refusals (Godzilla, GodzillaHead, Creeper, Zombie, Spider, Skeleton, Ghost, GhostSkelly), then the creative
  test; the Nightmare, the Kraken and the nine royals (TheKing, TheQueen, KingHead, QueenHead, ThePrince,
  ThePrinceAdult, ThePrincess, ThePrinceTeen, PurplePower) are prey inside the 64×40×64 scan, as in 1.7.10.
  Mothra was never Mobzilla's prey in either tree: she extends EntityButterfly and the shared ignore screen
  (orig :442-444; port ENT-S-106) refuses EntityButterfly ahead of the species chain — the port's Mothra line is
  redundant in both modes and stays under the key with its comment (the split's §5 correction: the survey's
  "Mothra … were prey in 1.7.10" does not hold for Mothra).
- **Switch (implemented 2026-09-04):** the `[modern]` key `godzillaSparesBossPeers`
  (`OreSpawnConfig.MODERN_GODZILLA_SPARES_BOSS_PEERS`, `BooleanValue`, default **true**), read only through the
  effective-value helper `OreSpawnConfig.godzillaSparesBossPeers()` = `MODERN_ENABLED && key`, beside
  `mothraWideRootHitbox()` / `fireRespectsMobGriefing()`. One gated site, read live at every filter call (a filter:
  the MOD-031 impact-read shape; a change applies to the next pick, no BOSS-017 concern): `Godzilla.isSuitableTarget`
  wraps the two lines in `if (OreSpawnConfig.godzillaSparesBossPeers())`. The master's comment and javadoc list the
  key and the helper.
- **Effect when effective (a default config):** Mobzilla does not target the Nightmare, the Kraken, the King and
  Queen (and their heads), the Prince family or the Purple Power; with the key off (or the master off) it hunts them
  as 1.7.10 did.
- **Not covered:** the damage-side "large unknown" rules (`Godzilla.doHurtTarget` / `hurt`: a big attacker that is
  neither a big boss nor a royal is halved / takes a tenth) are not targeting rows and are untouched in both modes;
  the creative refusal and the villager priority of the scan are unchanged.
- **Safety:** none — no player, owner or Peaceful term; the creative gate is separate.
- **Pin:** `PortOnlyTargetingTests#mod032_godzilla_spares_boss_peers_modern_on`,
  `#mod032_godzilla_takes_boss_peers_key_off`, `#mod032_godzilla_takes_boss_peers_master_off` (own batch
  `portOnlyTargeting`, `empty_tall`; the IgnoreScreenParityTests row-11 rig: `isSuitableTarget` by reflection on a
  frozen Godzilla against a frozen PitchBlack and a frozen TheKing, a pig control both ways; the classic rows ask the
  same Godzilla before and after the live flip).
- **Status:** IMPLEMENTED 2026-09-04 (the key, the helper, the gated site, the pins); the classic branch is orig.

## MOD-033 — Companions defend their owner: the port-only owner / tame target goals — Phase 4E's eight and the Boyfriend / Girlfriend pair (ACCEPTED 2026-09-04 under the T9 ruling, implemented, default ON; classic stays 1.7.10)

- **Origin:** targeting ledger batch T9, rows 7, 9, 12–15 (GammaMetroid, Leon, Spyro, Stinky, ThePrince, ThePrincess)
  plus the ThePrinceAdult / ThePrinceTeen owner pair the survey parked in T3c (split §1, §2 A2, §3 B1/B3–B6, §4.4).
  Commit 27b66a39 (2026-04-17, "Phase 4E: Companions & Tameables — defense AI, riding, dim-aware spawns") registered
  `OwnerHurtByTargetGoal` + `OwnerHurtTargetGoal` + `HurtByTargetGoal` "so they retaliate alongside their owner" and
  a tame-gated `NearestAttackableTargetGoal<Monster>` "so they autonomously hunt hostiles once tamed" on these pets.
  **FLAG for the owner:** the commit message is Phase 4E's ONLY note — no code comment, nothing in AUDIT_FINDINGS,
  FIX_LOG, MODERNIZATION_NOTES, KNOWN_ISSUES or CHANGELOG names the owner defence. Under the ruling "Phase 4E's six
  are documented only if that phase's notes state intent", the message's stated intent ("defense AI … retaliate
  alongside their owner") is taken as the documentation; the alternative reading (a commit message is not a phase
  note) would send the six to classic removal with no record — the classic side is the same either way.
- **Ruling (owner, 2026-09-04):** the six become ONE record over all of them (the split's §4.4 alternative), the five
  inert ones noted as registered-but-unconsumed so a future consumer lights them up under the same key.
- **What is live and what is inert at HEAD:** Leon (`flyWithRider` reads the target slot first and bites it on its
  1-in-7 roll), ThePrinceAdult and ThePrinceTeen (their 1-in-6 / 1-in-7 combat roll reads the slot first) consume
  the slot — a tamed modern pet of these three avenges its owner's attacker and joins its owner's fights. The
  Gamma Metroid, Spyro, Stinky, the Prince and the Princess never read the slot (their combat bites their own
  `findSomethingToAttack` pick; Spyro's / Stinky's 1-in-200 `setTarget(null)` only clears it) — the goals are
  registered but unconsumed, no visible effect today. The extension of 2026-09-05: the Boyfriend and the Girlfriend
  consume the slot (their held-weapon melee in `customServerAiStep`, Boyfriend.java:337 / Girlfriend.java:376, and the
  `RangedAttackGoal` @4 read it) — a tamed modern Boyfriend or Girlfriend avenges and defends its owner; the Hydrolisc
  and the Velocity Raptor never read it (only their 1-in-200 `setTarget(null)`, EntityHydrolisc.java:114 /
  VelocityRaptor.java:140, touches the slot) — registered but unconsumed, as the five above.
- **Classic (implemented; the behaviour while the master or the key is off):** none of the port goals register —
  Leon keeps `HurtByTargetGoal` and the PlayNicely-gated IMob hunt (orig Leon.java:92-95); the Gamma Metroid keeps
  `HurtByTargetGoal` (orig GammaMetroid.java:67, at its port priority 3 — the only goal on the selector, so the number
  is immaterial); Spyro, Stinky, the Prince and the Princess register no target goals (orig Spyro.java:73-81,
  Stinky.java:67-77, ThePrince.java:86-92, ThePrincess.java:86-92 — tasks only); the Prince Adult and Teen keep
  `HurtByTargetGoal` and the IMob hunt (orig ThePrinceAdult.java:112-115, ThePrinceTeen.java:116-119). The extension of
  2026-09-05: the Hydrolisc and the Velocity Raptor register no target goals (orig Hydrolisc.java:51-60,
  VelocityRaptor.java:53-62 — tasks only; the port's `HurtByTargetGoal` was Phase 4E's too, so it goes under the key
  with the pair); the Boyfriend keeps the IMob hunt @3 and the two Jealousy goals @4 / @5 (orig Boyfriend.java:138-147;
  the Creeper hunt has no port counterpart, ENT-A-054), the Girlfriend keeps the two Valentine goals @1 / @2, the IMob
  hunt @5 and the two Jealousy goals @4 / @5 (orig Girlfriend.java:161-174) — every one at its port priority; and
  Leon's hunt selector is the bare Enemy test, `e -> e instanceof Enemy` (orig Leon.java:93 — an `EntityLiving.class`
  list through `IMob.mobSelector`, no further selector, no tame term; the ENT-S-124 form). A tamed 1.7.10
  pet fought only what its own scan or its revenge memory picked.
- **Leon's tame rule (implemented 2026-09-05; the ENT-S-124 refutation closed 2026-09-04, its refuter upheld):** the
  hunt's selector is built once in `registerGoals` from the same snapshot (`final boolean petsDefendOwner`, captured
  by the lambda — never read live; EntityLeon.java:165-182): modern
  `e -> e instanceof Enemy && (!this.isTame() || this.getTarget() == null)` (a tamed Leon's hunt does not overwrite a
  target it holds), classic `e -> e instanceof Enemy` (orig Leon.java:93 had no such term). Effect: monsters only.
- **Switch (implemented 2026-09-04):** the `[modern]` key `petsDefendOwner` (`OreSpawnConfig.MODERN_PETS_DEFEND_OWNER`,
  `BooleanValue`, default **true**), read only through `OreSpawnConfig.petsDefendOwner()` = `MODERN_ENABLED && key`.
  Twelve gated sites (eight on 2026-09-04, four more on 2026-09-05), each reading the helper ONCE in `registerGoals`
  (the Mob constructor: a construction snapshot, BOSS-017 pattern, the S4 single-read rule — a config change applies
  to newly spawned or loaded pets, not live ones): EntityLeon (the owner pair and, since 2026-09-05, the hunt's
  selector from the same snapshot), EntityGammaMetroid (the owner pair and the tame hunt around the kept
  `HurtByTargetGoal`), EntitySpyro / EntityStinky / ThePrince / ThePrincess (all four goals), ThePrinceAdult /
  ThePrinceTeen (the owner pair), EntityHydrolisc / VelocityRaptor (all three goals — the owner pair and
  `HurtByTargetGoal`; EntityHydrolisc.java:82-86, VelocityRaptor.java:77-81), Boyfriend / Girlfriend (the owner pair;
  Boyfriend.java:154-157, Girlfriend.java:218-221). Camarasaurus does not carry them: its `registerGoals` registers no
  target goals (Camarasaurus.java:66-81, tasks only, as orig Camarasaurus.java:53-62) — nothing to gate. The master's
  comment and javadoc, and the key's own comment, list the key, the helper and the species.
- **Safety:** the owner goals never target the owner (`TamableAnimal.canAttack` / `wantsToAttack`); classic lowers a
  tamed pet's aggression to 1.7.10's — a pet that no longer defends its owner in classic is the 1.7.10 behaviour.
  Nothing becomes unsafe in either mode. The extension lowers a classic Boyfriend's / Girlfriend's aggression to
  1.7.10's the same way (no owner defence; the hunt, Jealousy and Valentine goals are untouched) and changes nothing
  observable on the Hydrolisc and the Velocity Raptor.
- **Pin:** `PortOnlyTargetingTests#mod033_pets_defend_owner_modern_on`, `#mod033_pets_defend_owner_key_off`,
  `#mod033_pets_defend_owner_master_off` — each spawns the eight pets with their goals AFTER the flip and reads the
  target selector: modern → the owner pair present on all eight (plus `HurtByTargetGoal` and the tame
  `NearestAttackableTargetGoal<Monster>` on the Phase 4E five); classic → no owner goal anywhere, `HurtByTargetGoal`
  and the IMob hunt still on Leon / Adult / Teen, `HurtByTargetGoal` alone on the Metroid, nothing on Spyro, Stinky,
  the Prince and the Princess. The extension (2026-09-05): `#mod033_companions_defend_owner_modern_on`, `_key_off`,
  `_master_off` — the four companions spawned with their goals AFTER each flip, the whole target selector described
  as `priority:Goal<targetType>` and compared sorted: modern → the 1.7.10 goals plus the owner pair (plus
  `HurtByTargetGoal` on the Hydrolisc and the Raptor); classic → exactly the 1.7.10 goals (the Hydrolisc's and the
  Raptor's selectors empty; the Boyfriend's `3:NearestAttackableTargetGoal<Mob>`, `4:` and `5:JealousyTargetGoal<Boyfriend>`;
  the Girlfriend's `1:ValentineTargetGoal<Player>`, `2:ValentineTargetGoal<Boyfriend>`, `5:NearestAttackableTargetGoal<Mob>`,
  `4:` and `5:JealousyTargetGoal<Girlfriend>`). `#mod033_leon_tame_hunt_rule_modern_on`, `_key_off`, `_master_off` —
  a predicate pin per mode: a Leon spawned AFTER the flip at the IMobConventionTests spots, its `Entity.random` swapped
  for the ForcedRoll seam (the hunt's 1-in-5 acquisition roll pinned to fire), PlayNicely off, tamed and holding a
  frozen 1000-HP Zombie 8 blocks east as its target; the hunt goal's `canUse()` refuses her in modern (and takes her
  once the slot is emptied — the control) and takes her in classic, the goal's pick read back. Six rows, 15 → 21 in
  the batch. IMobConventionTests' `s124_17_leon_slime_tame_rule` row now asserts the modern default as a precondition
  (its refusal is MOD-033's modern branch).
- **Extension (ruled 2026-09-04, implemented 2026-09-05):** the same key reaches the four species the ledger has no
  block for — `EntityHydrolisc.java:82-86` and `VelocityRaptor.java:77-81` (Phase 4E, commit 27b66a39; all three goals
  under the key; inert, no slot reader) and `Boyfriend.java:154-157` / `Girlfriend.java:218-221` (the owner pair — commit 2b0c2cd, 2026-04-06, no stated
  intent, gated on the ruling — under the key; live: their melee reads the slot at :337 / :376 and their `RangedAttackGoal` @4 reads it; orig
  Boyfriend.java:138-147 and Girlfriend.java:161-174 register no owner task and no EntityAIHurtByTarget) — and Leon's
  tame rule (above). A tamed Boyfriend or Girlfriend in classic now fights only what its hunt, Jealousy or Valentine
  goals pick, the 1.7.10 behaviour. Camarasaurus was checked and carries none of the goals (see the Switch bullet).
- **Disclosure (2026-09-05, the T5b refuter):** in modern, Leon's 1-in-200 forget (EntityLeon.java:568-571) ends its
  revenge goal through `release()` but not a running `OwnerHurtByTargetGoal` / `OwnerHurtTargetGoal` (:167-168), whose
  own memory re-asserts the owner's attacker next tick — a facet of this record (1.7.10 had no owner goals), not a
  parity question; classic is unaffected.
- **Status:** IMPLEMENTED 2026-09-04 (the key, the helper, the eight gated sites, the pins); EXTENDED 2026-09-05 (the
  four companions outside the ledger, Leon's tame rule, six more pins); the classic branch is orig.

## MOD-034 — Pointysaurus eye-contact aggression (ACCEPTED 2026-09-04 under the T9 ruling, implemented, default ON; classic stays 1.7.10)

- **Origin:** targeting ledger batch T9, row 11 (split §1 row 11, §2 A3, §4.2). `PointysaurusStareGoal` @2 on the
  target selector: the nearest player within 32 blocks whose view vector points at the mob's eyes (dot > 0.97), with
  line of sight, not creative or spectator, becomes the target after a 5-tick delay; the `DinosaurMeleeAttackGoal`
  then chases and bites. Documented three times: the goal's javadoc ("The 1.7.10 Pointysaurus had no such mechanic …
  a modern enhancement layered on top … the Phase 10 brief"), the registration comment ("Phase 10 — Enderman-style
  eye-contact aggression …") and commit 21b8d0e8 ("Phase 10 - The Gadgets, Gems, & Genetics"). The split flagged that
  Phase 10's other inventions were removed under the no-procedural-fabrication ruling (MOD-009, MOD-013, MOD-020,
  PN-009); the owner ruled a record.
- **Classic (implemented):** the goal is not registered — orig Pointysaurus.java:50-55 registers no such task; the
  Pointysaurus attacks only from its 12×5×12 proximity scan (the port's `NearestAttackableTargetGoal<Player>` @3, the
  T3c geometry row) and from being hit (`HurtByTargetGoal` @1). The class `ai/PointysaurusStareGoal` stays; the
  ENT-S-115 PlayNicely `canUse` override on the registration stays as it is inside the gate.
- **Switch (implemented 2026-09-04):** the `[modern]` key `pointysaurusStareAggro`
  (`OreSpawnConfig.MODERN_POINTYSAURUS_STARE_AGGRO`, default **true**), read only through
  `OreSpawnConfig.pointysaurusStareAggro()` = `MODERN_ENABLED && key`, ONCE in `Pointysaurus.registerGoals`
  (construction snapshot, BOSS-017 pattern — a change applies to newly spawned or loaded Pointysaurs).
- **Safety:** none in either direction — the goal refuses creative / spectator players and, through `forCombat`,
  players in Peaceful; classic only lowers aggression against survival players.
- **Pin:** `PortOnlyTargetingTests#mod034_pointysaurus_stare_goal_modern_on`, `#mod034_pointysaurus_stare_goal_key_off`,
  `#mod034_pointysaurus_stare_goal_master_off` — a Pointysaurus spawned with its goals after the flip: the stare goal
  on the target selector in modern, absent in classic, `HurtByTargetGoal` and the player hunt present both ways.
- **Status:** IMPLEMENTED 2026-09-04.

## MOD-035 — Cryolophosaurus revenge chase (ACCEPTED 2026-09-04 under the T9 ruling, implemented, default ON; classic stays 1.7.10)

- **Origin:** targeting ledger batch T9, row 1 (split §1 row 1, §2 A4, §4.1). `DinosaurMeleeAttackGoal` @2 on the goal
  selector with `Presets.cryolophosaurus()` chases and bites whatever fills the target slot; only `HurtByTargetGoal`
  @1 ever fills it (the ported 1-in-5 hunt over the 9×2×9 box bites its own pick without `setTarget`), so the goal is
  a chase of the revenge target only. Documented by commit f5cb0ba5 (Phase 4C: "All dinosaurs now use modern
  MeleeAttackGoal-style combat via Goal selectors plus HurtByTargetGoal / NearestAttackableTargetGoal for target
  acquisition … Cryolophosaurus keeps its high forgetTargetRoll / low attack probability to preserve its timid 1.7.10
  personality.") and the ENT-A-009 acceptance of the mapping (AUDIT_FINDINGS.md, the Alosaurus record). The split
  flagged the documentation as generic / another species'; the owner ruled a record.
- **Classic (implemented):** the goal is not registered — orig Cryolophosaurus.java:51-57 has no attack task; the
  revenge target `EntityAIHurtByTarget` stores is never chased (only the 1-in-200 forgiveness reads it). The ported
  hunt (`customServerAiStep`, ENT-A-112) is untouched in both modes; the no-op `legacySetAttacking` callback stays.
- **Switch (implemented 2026-09-04):** the `[modern]` key `cryolophosaurusRevengeChase`
  (`OreSpawnConfig.MODERN_CRYOLOPHOSAURUS_REVENGE_CHASE`, default **true**), read only through
  `OreSpawnConfig.cryolophosaurusRevengeChase()` = `MODERN_ENABLED && key`, ONCE in `Cryolophosaurus.registerGoals`
  (construction snapshot, BOSS-017 pattern — a change applies to newly spawned or loaded Cryolophosaurs).
- **Safety:** none — the goal inherits only the revenge target; a creative attacker is dropped by
  `TargetGoal.canContinueToUse` → `canAttack` → `canBeSeenAsEnemy`; a Monster despawns in Peaceful. Classic lowers
  aggression (an attacker outside the 9×2×9 hunt box is remembered, not chased).
- **Pin:** `PortOnlyTargetingTests#mod035_cryolophosaurus_revenge_chase_modern_on`,
  `#mod035_cryolophosaurus_revenge_chase_key_off`, `#mod035_cryolophosaurus_revenge_chase_master_off` — a
  Cryolophosaurus spawned with its goals after the flip: the `DinosaurMeleeAttackGoal` on the goal selector in modern,
  absent in classic, `HurtByTargetGoal` present both ways.
- **Status:** IMPLEMENTED 2026-09-04.

## MOD-036 — The Girlfriend's Valentine rampage respects Peaceful and creative players: a deliberate parity exception, kept in BOTH modes, no key (owner safety ruling 2026-09-04)

- **Origin:** targeting ledger batch T7's deferred row (MyValentineTarget / Girlfriend; split §1 row 16, §2 A5, §4.5).
  The port's `ValentineTargetGoal` (Girlfriend.registerGoals, @1 for Player and @2 for Boyfriend) is a
  `NearestAttackableTargetGoal`, so its `TargetingConditions.forCombat()` refuse a player in Peaceful
  (`LivingEntity.canAttack`) and an invulnerable — creative or spectator — player (`Player.canBeSeenAsEnemy`), and
  `TargetGoal.canContinueToUse` drops a held player when the difficulty flips mid-rampage. 1.7.10's
  `MyValentineTarget` / `MyEntityAITarget.isSuitableTarget` (:96-98) took any player while `valentines_day != 0`,
  with no difficulty and no creative term: on Feb 14 the angry Girlfriend — an EntityTameable that persists in
  Peaceful — hunted players in Peaceful and creative players.
- **Documentation:** none found (the goal's javadoc cites only the owner rule; FIX_LOG, AUDIT_FINDINGS and commit
  d65b9b11 say nothing about Peaceful or creative) — by the letter of the T9 ruling a removal from classic.
- **Ruling (owner, 2026-09-04):** "Exception: the Girlfriend safety gates stay in both modes, recorded as a deliberate
  parity exception." Kept in both modes on the owner's safety ruling; the 1.7.10 rampage hunted Peaceful and creative
  players. No key: `modern.enabled = false` does NOT restore the 1.7.10 hunt. The engine zeroes her melee damage in
  Peaceful, but the chase, the aggression and the UltimateArrow path are not covered by that, and nothing covers
  creative players.
- **Code:** unchanged — the inherited `forCombat` conditions on `ValentineTargetGoal`; the goal's own selector (the
  owner / tamed-pet rule) is orig MyEntityAITarget.java:88-95 and not part of this record.
- **Pin:** `PortOnlyTargetingTests#mod036_girlfriend_valentine_gates_hold_in_classic` and
  `#mod036_girlfriend_valentine_gates_hold_in_modern` — under the `SeasonalDates` Feb-14 clock seam, a Girlfriend
  spawned with her goals (valentine-angry asserted), the `ValentineTargetGoal<Player>` read off her target selector
  and its `TargetingConditions` asked directly (the CephadromeGateTests shape): a creative mock player refused, a
  survival mock player accepted on NORMAL, the same survival player refused after the difficulty is flipped to
  PEACEFUL inside the test — with the master off (classic) and on (modern); difficulty, master and clock restored in
  a finally, players removed, spawns discarded.
- **Status:** RECORDED 2026-09-04 (a parity exception; nothing to implement, pinned in both modes).
