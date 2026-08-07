"""D5 ledger patch: AUDIT_FINDINGS.md resolutions + new findings.

Applies, exactly once each (asserts on every anchor):
  1. WGEN-037 FIXED (BasiliskMaze ported)
  2. WGEN-038 VERIFIED-CORRECT (dead-code proof)
  3. WGEN-005 PARTIAL -> FIXED (SpawnOres pool restored)
  4. ITEM-062 PARTIAL -> FIXED (116 water recipes + 3 combines)
  5. WGEN-042 -> PARTIAL (rookery + tower reconciliation done; ~20 remain, D6)
  6. ITEM-020 resolution extended (DSB outcomes 2/23/38/47)
  7. New findings WGEN-051..057 (tower reconciliation + salt collision)
  8. New finding ITEM-066 (trophy prince/princess eggs)
Also bumps tools/ledger_reconcile.py TOTAL_EXPECTED 605 -> 613.
"""
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
p = ROOT / "AUDIT_FINDINGS.md"
t = p.read_text(encoding="utf-8")
orig_len = len(t)


def replace_once(old: str, new: str) -> None:
    global t
    assert t.count(old) == 1, f"anchor not unique/found: {old[:80]!r}"
    t = t.replace(old, new)


# 1. WGEN-037
replace_once(
    "- **Fix:** Port BasiliskMaze as a mining_biome structure (legacy-piece or code path) with its spawner and a loot table transcribing the listed chest contents.\n",
    "- **Fix:** Port BasiliskMaze as a mining_biome structure (legacy-piece or code path) with its spawner and a loot table transcribing the listed chest contents.\n"
    "- **Resolution:** FIXED (2026-08-08, Phase D5 — full line-by-line port as `LegacyDungeonStructure` type BASILISK_MAZE (`BasiliskMazeGenerator`, spec `phase_d_reports/d5_extraction/basilisk_maze_spec.md`): randomized-Prim maze + castle + antechamber + pyramid/shaft entrance per orig BasiliskMaze.java:30-458; 3 persistent Basilisks (no spawner blocks); 31-entry chest list transcribed to `chests/basilisk_maze` incl. CagedGirlfriend via caged_mob+caged_entity component; mining_biome set 26/13 (the 1/95 x 1/7 rotation odds, WGEN-039 equivalence) with the original lowest-of-36-columns >Y40 -2 ground scan in findGenerationPoint; DungeonSpawnerBlock outcome 23 wired; see FIX_LOG.md and phase_d_reports/D5_structures_spawnores.md)\n")

# 2. WGEN-038
replace_once(
    "- **Fix:** Port NightmareDungeon generation (triggered from the RTP teleport target, which also requires the ITEM-013 RTPBlock fix) with its spawner and gear-chest loot tables.\n",
    "- **Fix:** Port NightmareDungeon generation (triggered from the RTP teleport target, which also requires the ITEM-013 RTPBlock fix) with its spawner and gear-chest loot tables.\n"
    "- **Resolution:** VERIFIED-CORRECT (2026-08-08, Phase D5 — the audit's premise is wrong: `NightmareDungeon` is DEAD CODE in 1.7.10, never instantiated anywhere in the tree (exhaustive-search proof in `phase_d_reports/d5_extraction/nightmare_spec.md` section 1; the class's only reference is its own declaration, and no RTP pathway builds dungeons). Generating it would invent behavior. The Nightmare structure that actually generated - `GenericDungeon.makeNightmareRookery` - is ported under WGEN-042 in this slice; see FIX_LOG.md)\n")

# 3. WGEN-005
replace_once(
    "- **Resolution:** PARTIAL (2026-06-12, Phase C — the 2-boss-block + ancient-dried-egg reduction is documented as a deliberate redesign (PARITY_NOTES.md); restoring the full ~105-type SpawnOres pool at 28+/chunk Y50-128 is Phase D (structures/spawn-block pool owner, with WGEN-042); see FIX_LOG.md and phase_c_reports/C7_worldgen.md)",
    "- **Resolution:** FIXED (2026-08-08, Phase D5 — full pool restored: 106 new OreGenericEgg blocks registered (119-row master table, `phase_d_reports/d5_extraction/spawn_ores_spec.md` section 2) and `SpawnOresPoolFeature` reproduces the original roll exactly (28+nextInt(20/30) veins, +30 on 1/20, LessOre integer-div 3, Y-window 50..127 discard filter, 7-in-104 rare tier, exact nextInt(98)/nextInt(7) switch orders) in overworld + Utopia/Village/Chaos + Mining x3 passes per orig OreSpawnWorld.java:355-803 / ChunkOreGenerator.java:21-469 / ChunkProviderOreSpawn2.java:191-195; the interim Phase C7 dragon/kraken features + invented ancient-dried-egg block retired (PN-010 closed, rehydration archived MOD-013). Previously PARTIAL (2026-06-12, Phase C — deliberate interim redesign); see FIX_LOG.md and phase_d_reports/D5_structures_spawnores.md)")

# 4. ITEM-062
replace_once(
    "- **Resolution:** PARTIAL (2026-06-12, Phase C — all 381 original registrations diffed by script (phase_c_reports/C6_recipe_diff.md): 201 logical recipes verified/fixed, 16 invented recipe JSONs removed. 2026-07-02, Phase D4 — the six diffed-absent standalone recipes added (skate bow, chest from crystal planks, red bed from crystal planks, raw corn dog, bucket from pink-tourmaline ingots, cobweb from string). Remainder: the 116 water-bucket spawn-block→egg conversions (OreSpawnMain.java:2667-3000s), blocked on the ~105-type SpawnOres block pool owned by WGEN-005 → Phase D5 structure/spawn-block slice; see FIX_LOG.md)",
    "- **Resolution:** FIXED (2026-08-08, Phase D5 — the final remainder closed: all 116 water-bucket spawn-block→egg conversions (orig OreSpawnMain.java:2665-3021, shapeless water bucket + block → 1 egg, bucket returned via the modern crafting remainder) plus the 3 nine-part combines (Mobzilla/King/Queen full egg blocks, :2886/2892/2898), generated by `tools/d5_gen_spawn_ores.py` against the verified 119-row table; vanilla-mob outputs use modern vanilla eggs (incl. ender_dragon/iron_golem/snow_golem/wither — all present since 1.20.5), CriminalEgg → band_p_spawn_egg (WGEN-017), EnchantedCowEgg → enchanted_apple_cow_spawn_egg (the consolidated original). Previously PARTIAL (2026-06-12 Phase C recipe-corpus diff; 2026-07-02 Phase D4 six standalone recipes); see FIX_LOG.md)")

# 5. WGEN-042
replace_once(
    "- **Fix:** Port these builders incrementally (legacy-piece transcription like the royal altars), prioritizing those wired to gameplay (KyuubiDungeon/EnderKnightDungeon for Mining, Hospital/EnderCastle for End, D4 set for Islands); register each into the DungeonSpawnerBlock pool (ITEM-020/WGEN-036) as it lands.",
    "- **Fix:** Port these builders incrementally (legacy-piece transcription like the royal altars), prioritizing those wired to gameplay (KyuubiDungeon/EnderKnightDungeon for Mining, Hospital/EnderCastle for End, D4 set for Islands); register each into the DungeonSpawnerBlock pool (ITEM-020/WGEN-036) as it lands.\n"
    "- **Resolution:** PARTIAL (2026-08-08, Phase D5 — NightmareRookery ported (`NightmareRookeryGenerator` per orig GenericDungeon.java:5242-5312 + addD4NightmareRookery OSW:2253-2274, island_biome set 44/22 = the 1/100 x 1/19 D4 roll, DSB outcome 38) and the already-ported EnormousCastle/Q ('Challenge Towers') reconciled interior-and-placement to the originals (WGEN-051..056, ITEM-066). D5 also produced `phase_d_reports/structure_conversion_pattern.md` — the D6 playbook — and the full Islands i=nextInt(19) dispatch table (`enormous_castle_spec.md` section 12.3). Remaining ~20 structures → Phase D6; see FIX_LOG.md)")

# 6. ITEM-020
replace_once(
    "- **Resolution:** PARTIAL (2026-06-12, Phase C — 400-tick fuse + table-driven nextInt(50) pool restored; structure builders beyond generic/ruby dungeon → WGEN-042 (Phase D); see FIX_LOG.md and phase_c_reports/C6_items_blocks.md)",
    "- **Resolution:** PARTIAL (2026-06-12, Phase C — 400-tick fuse + table-driven nextInt(50) pool restored; structure builders beyond generic/ruby dungeon → WGEN-042 (Phase D). 2026-08-08, Phase D5 — outcomes 2 (EnormousCastle King, DSB:59-61), 23 (BasiliskMaze, DSB:122-124), 38 (NightmareRookery, DSB:167-169), 47 (EnormousCastleQ, DSB:194-196) wired via LegacyDungeonPiece.buildNow; remaining outcomes land with their D6 structures; see FIX_LOG.md)")

# 7. New WGEN-051..057
wgen50_res = "- **Resolution:** VERIFIED-CORRECT (2026-06-12, Phase C — confirmed a deliberate port addition (1.7.10 PortalBlock.java is empty; travel was entity-based); kept as a documented creative-only utility block, PARITY_NOTES.md entry added; see FIX_LOG.md and phase_c_reports/C7_worldgen.md)"
new_wgen = wgen50_res + """

### WGEN-051 — Challenge Tower: difficulty roll locked to 6 (found 2026-08-08, Phase D5)

- **Status:** DIVERGENT
- **Original:** `GenericDungeon.java:202-205` / `:6404-6407` — `level = 1 + nextInt(6); if (level <= 3 && nextInt(3) != 1) level += 3;` → P(1..3)=1/18 each, P(4..6)=5/18 each; only ~27.8% of towers are full-height level-6 prize towers.
- **Port:** a pre-plan "QA Fix (Endgame Loot Gate)" hardcoded `level = 6` in `LegacyDungeonPiece.generateChallengeTower` so every tower guaranteed the Royal loot.
- **Resolution:** FIXED (2026-08-08, Phase D5 — the original roll restored on the deterministic piece RNG; the guaranteed-prize idea archived as MODERNIZATION_NOTES MOD-012; see FIX_LOG.md)

### WGEN-052 — Challenge Tower: invented scaffolding climb columns (found 2026-08-08, Phase D5)

- **Status:** DIVERGENT
- **Original:** no ladders or climbable blocks anywhere in `makeEnormousCastle`/`Q` (verified: zero ladder references in GenericDungeon.java:191-786/6393-6987); the 1x1 bedrock holes are the only route.
- **Port:** "QA Traversal Fix" scaffolding columns under every decoration-room ceiling hole and in the decor-6 dirt shaft.
- **Resolution:** FIXED (2026-08-08, Phase D5 — both scaffolding sites removed; archived as part of MOD-012; see FIX_LOG.md)

### WGEN-053 — Challenge Tower: chest loot palettes replaced the level1-5 lists (found 2026-08-08, Phase D5)

- **Status:** DIVERGENT
- **Original:** `level1ContentsList`..`level5ContentsList` (GenericDungeon.java:57-61) — weighted lists totalling 165/235/235/255/1285, incl. the level-5 83-spawn-egg jackpot; fill `5 + nextInt(7)` stacks per chest (GD:750).
- **Port:** `fillChallengeContents` used invented 7-10-item unweighted palettes (netherite ingot, enchanted golden apple, lapis substitutions; level-5 reduced to 5 eggs).
- **Resolution:** FIXED (2026-08-08, Phase D5 — five loot tables `chests/challenge_tower_level1..5` transcribe the originals entry-for-entry (weights, stack ranges, rolls 5-11; totals verified 165/235/235/255/1285 by `tools/d5_gen_tower_loot.py`); CriminalEgg → band_p_spawn_egg per WGEN-017; see FIX_LOG.md and `phase_d_reports/d5_extraction/enormous_castle_spec.md` section 8)

### WGEN-054 — Challenge Tower: "Jumpy Bug" spawners mapped to the wrong mob (found 2026-08-08, Phase D5)

- **Status:** DIVERGENT
- **Original:** the decoration-room ladder's "Jumpy Bug" is `TrooperBug` (EntityList registration OreSpawnMain.java:3943); "Spit Bug" is a different mob (:3951).
- **Port:** `pickKingDecorMob`/`pickQueenDecorMob` returned `ENTITY_SPIT_BUG` for every Jumpy Bug slot.
- **Resolution:** FIXED (2026-08-08, Phase D5 — all four ladder sites now `ENTITY_TROOPER_BUG`; see FIX_LOG.md)

### WGEN-055 — Challenge Tower: placement diverges from addD4Castle (found 2026-08-08, Phase D5)

- **Status:** DIVERGENT
- **Original:** `OreSpawnWorld.addD4Castle` (OSW:2203-2228) — LessLag 50% gate, chunk-corner + nextInt(8) jitter, grass scan Y20→5, air-box check, one 50/50 King/Queen roll at 3/19 of the 1/100 D4 dispatch (≈1/1267 per variant); level-6 towers also scatter the buried Large Worm ring over x,z −28..+55 at y−1 (GD:362-374).
- **Port:** chunk-centre heightmap anchor with no LessLag/jitter; structure sets shipped at 44/22 although the Phase C7 WGEN-043 resolution documents the approved 36/18 = 1/1267 math; the symmetric ±40 piece bounding box silently clipped the worm ring's outer band at chunk borders.
- **Resolution:** FIXED (2026-08-08, Phase D5 — KING_TOWER/QUEEN_TOWER moved to the ISLANDS_GRASS placement mode (grass anchor + nextInt(8) jitter + LessLag gate), sets corrected to the approved 36/18, and the box extended asymmetric x −39..+57 / z −30..+57 / y −4..+85 to cover stair + skirt + worm ring; see FIX_LOG.md)

### WGEN-056 — Challenge Tower: chest facing metadata lost (found 2026-08-08, Phase D5)

- **Status:** DIVERGENT
- **Original:** `fill_chests`/`Q` stamp facing metadata 5/4/3/2 after placing each chest (GD:744/754/765/776) so all four face the room centre.
- **Port:** chests placed with the default state (facing north).
- **Resolution:** FIXED (2026-08-08, Phase D5 — facing-aware chest placement added to LegacyDungeonPiece; all four tower chests face the room centre again; see FIX_LOG.md)

### WGEN-057 — Structure-set salt collision: mantis_nest == royal_trees (found 2026-08-08, Phase D5)

- **Status:** DIVERGENT
- **Original:** n/a (port-side placement plumbing) — random_spread sets must use distinct salts or their placement grids correlate.
- **Port:** `structure_set/mantis_nest.json` and `structure_set/royal_trees.json` both used salt 84312.
- **Resolution:** FIXED (2026-08-08, Phase D5 — royal_trees re-salted to 84332 (next free value); pre-release, so no published worlds shift; see FIX_LOG.md)
"""
replace_once(wgen50_res, new_wgen)

# 8. ITEM-066
i65_res_tail = "and the config system as 2.0 modernization candidate MODERNIZATION_NOTES MOD-011; see FIX_LOG.md. **Owner approval recorded 2026-07-03 at the D4 checkpoint** — PN-013/MOD-011 stand as written)"
replace_once(i65_res_tail, i65_res_tail + """

### ITEM-066 — Invented Prince/Princess trophy eggs replaced the functional spawn eggs (found 2026-08-08, Phase D5)

- **Status:** DIVERGENT
- **Original:** `ThePrinceEgg`/`ThePrincessEgg` are functional `ItemSpawnEgg`s ("eggtheprince"/"eggtheprincess", OreSpawnMain.java:5616/5630); dropped by TheQueen (TheQueen.java:192), ThePrinceTeen (:318) and ThePrinceAdult (:314), and placed in the Challenge Tower prize chests (GenericDungeon.java:747/6949).
- **Port:** pre-plan "Phase 12" registered inert trophy items `prince_egg`/`princess_egg` and used them in all five consumer sites, so the drops and chest rewards could not actually spawn royalty.
- **Resolution:** FIXED (2026-08-08, Phase D5 — all consumers (the_queen/the_prince_teen/the_prince_adult loot tables + both tower prize chests) switched to `the_prince_spawn_egg`/`the_princess_spawn_egg`; the trophy items, their models and lang entries removed. Cosmetic delta: the port's spawn eggs use the mod-wide tinted template rather than the original per-egg `eggtheprince.png` texture (textures retained in-repo); see FIX_LOG.md)
""")

p.write_text(t, encoding="utf-8")
print(f"AUDIT_FINDINGS.md: {orig_len} -> {len(t)} chars")

# 9. ledger_reconcile.py total bump
lr = ROOT / "tools/ledger_reconcile.py"
lt = lr.read_text(encoding="utf-8")
old = """# 601 audit IDs + ENT-D-066 (duplicate hoverboard, found 2026-07-02 in Phase D2)
# + BOSS-045/BOSS-046 (invented teen/adult interactions) + BUG-032 (39 missing
#   aggregate sound events), all found 2026-06-13 in Phase D3.
TOTAL_EXPECTED = 605"""
assert old in lt
lt = lt.replace(old, """# 601 audit IDs + ENT-D-066 (duplicate hoverboard, found 2026-07-02 in Phase D2)
# + BOSS-045/BOSS-046 (invented teen/adult interactions) + BUG-032 (39 missing
#   aggregate sound events), all found 2026-06-13 in Phase D3.
# + WGEN-051..057 (Challenge Tower reconciliation: level lock, scaffolding,
#   loot palettes, Jumpy Bug mob, placement, chest facing; structure-set salt
#   collision) + ITEM-066 (trophy prince/princess eggs), found 2026-08-08 in
#   Phase D5.
TOTAL_EXPECTED = 613""")
lr.write_text(lt, encoding="utf-8")
print("ledger_reconcile.py TOTAL_EXPECTED -> 613")
