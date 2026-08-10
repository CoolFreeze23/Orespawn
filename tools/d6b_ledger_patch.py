"""D6b close-out ledger patch: close WGEN-042/ITEM-020 + 6 D-owned findings,
add new findings WGEN-063..071 / ITEM-067..069, totals 618 -> 630."""
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
p = ROOT / "AUDIT_FINDINGS.md"
t = p.read_text(encoding="utf-8")


def replace_once(old, new):
    global t
    assert t.count(old) == 1, f"anchor problem: {old[:70]!r}"
    t = t.replace(old, new)


# ---- 1. Close WGEN-042 (state word + Igloo carve-out) ----
replace_once(
    "- **Resolution:** PARTIAL (2026-08-08, Phase D5 — NightmareRookery ported",
    "- **Resolution:** FIXED (2026-08-08, Phase D5 — NightmareRookery ported")
replace_once(
    "Remaining open: Igloo worldgen placement only (igloo_spec.md §7.3 NEEDS_DESIGN_RULING); see FIX_LOG.md)",
    "Igloo worldgen placement carved out to WGEN-071 (Phase E) at the D close-out — every other structure is ported and verified; see FIX_LOG.md and phase_d_reports/phase_d_rollup.md)")

# ---- 2. Close ITEM-020 ----
replace_once(
    "- **Resolution:** PARTIAL (2026-06-12, Phase C — 400-tick fuse + table-driven nextInt(50) pool restored;",
    "- **Resolution:** FIXED (2026-06-12, Phase C — 400-tick fuse + table-driven nextInt(50) pool restored;")

# ---- 3. Close WGEN-014 / 018 / 033 / 036 / ITEM-064 (state word + close note) ----
replace_once(
    "- **Resolution:** PARTIAL (2026-06-12, Phase C — BeeHive restored to Mining (WGEN-040)",
    "- **Resolution:** FIXED (2026-06-12, Phase C — BeeHive restored to Mining (WGEN-040)")
replace_once(
    "BasiliskMaze is WGEN-037 and KyuubiDungeon/EnderKnightDungeon are WGEN-042 — both Phase D structure owners; see FIX_LOG.md and phase_c_reports/C7_worldgen.md)",
    "BasiliskMaze is WGEN-037 and KyuubiDungeon/EnderKnightDungeon are WGEN-042. CLOSED 2026-08-10, D close-out: maze D5, Kyuubi D6a, EnderKnightDungeon D6b batch 4 (LOWEST_GRASS_36 mining set 26/13); see FIX_LOG.md and phase_d_reports/phase_d_rollup.md)")
replace_once(
    "- **Resolution:** PARTIAL (2026-06-12, Phase C — the divergent half fixed: greenhouse/robot_lab/white_house re-tagged",
    "- **Resolution:** FIXED (2026-06-12, Phase C — the divergent half fixed: greenhouse/robot_lab/white_house re-tagged")
replace_once(
    "DamselInDistress/SpiderHangout/RedAntHangout remain Phase D (WGEN-042 structure owner); see FIX_LOG.md and phase_c_reports/C7_worldgen.md)",
    "DamselInDistress/SpiderHangout/RedAntHangout were WGEN-042 owners. CLOSED 2026-08-10, D close-out: Damsel D6b batch 3, both Hangouts batch 4, all inline orespawn:village_biome; see FIX_LOG.md and phase_d_reports/phase_d_rollup.md)")
replace_once(
    "- **Resolution:** PARTIAL (2026-06-12, Phase C — End spawns verified present (add_end_spawns);",
    "- **Resolution:** FIXED (2026-06-12, Phase C — End spawns verified present (add_end_spawns);")
replace_once(
    "Hospital and EnderCastle structures remain Phase D (WGEN-042 structure owner); see FIX_LOG.md and phase_c_reports/C7_worldgen.md)",
    "Hospital and EnderCastle were WGEN-042 owners. CLOSED 2026-08-10, D close-out: both ported D6a (is_end sets 42/21 + 10/5); see FIX_LOG.md and phase_d_reports/phase_d_rollup.md)")
replace_once(
    "- **Resolution:** PARTIAL (2026-06-12, Phase C — the 400-tick timer is already faithful (RandomDungeonSpawnerBlockEntity",
    "- **Resolution:** FIXED (2026-06-12, Phase C — the 400-tick timer is already faithful (RandomDungeonSpawnerBlockEntity")
replace_once(
    "expanding the 2-outcome table back to 50 is blocked on the Phase D structure ports (WGEN-021/037/038/042); see FIX_LOG.md and phase_c_reports/C",
    "expanding the 2-outcome table back to 50 was blocked on the Phase D structure ports. CLOSED 2026-08-10, D close-out: all 50 outcomes wired (ITEM-020); see FIX_LOG.md and phase_c_reports/C")
replace_once(
    "- **Resolution:** PARTIAL (2026-06-12, Phase C — lessOre wired via the orespawn:less_ore_count placement modifier",
    "- **Resolution:** FIXED (2026-06-12, Phase C — lessOre wired via the orespawn:less_ore_count placement modifier")
replace_once(
    "Mining-dim density gating → WGEN-011 (Phase D); see FIX_LOG.md and phase_c_reports/C6_items_blocks.md)",
    "Mining-dim density gating → WGEN-011, itself FIXED in Phase C — nothing remained; CLOSED 2026-08-10 at the D close-out; see FIX_LOG.md and phase_c_reports/C6_items_blocks.md)")

# ---- 4. WGEN-021: add the missing Resolution line ----
replace_once(
    "- **Fix:** Port the D4 structure builders as jigsaw/legacy-piece structures tagged `orespawn:island_biome` with sets matching the original per-chunk roll rates; restore unstable-ant block placement.",
    "- **Fix:** Port the D4 structure builders as jigsaw/legacy-piece structures tagged `orespawn:island_biome` with sets matching the original per-chunk roll rates; restore unstable-ant block placement.\n"
    "- **Resolution:** FIXED (2026-08-10, Phase D close-out — every Islands D4 builder is ported: EnormousCastle K/Q + NightmareRookery D5, Robot Lab/Greenhouse/White House re-tagged Islands in C and reconciled D6a, CloudShark b1, MiniDungeon/CephadromeAltar b2, StinkyHouse/Pumpkin/Rainbow b3; unstable anthills wired via configured/placed_feature/unstable_anthill.json into island_biome.json; see phase_d_reports/phase_d_rollup.md)")

# ---- 5. New findings WGEN-063..071 + ITEM-067..069, appended after WGEN-062 ----
w62 = "tri-state result (BUILT/SUPPRESS/NONE) in CrystalStructures)"
assert t.count(w62) == 1
t = t.replace(w62, w62 + """

### WGEN-063 — Greenhouse: plant table silently drifted (found 2026-08-10, D6b batch 4)

- **Status:** DIVERGENT
- **Original:** `makeGreenhouseDungeon` plant roll t==7 places reeds/sugar cane (GenericDungeon.java:5090-5092, field_150436_aH) and t==19 places MyRicePlant (:5123-5125); only t==8 rolls nothing.
- **Port:** case 7 returned PUMPKIN and case 19 fell to air under a Javadoc claiming "indices 8 and 19 are intentional gaps".
- **Resolution:** FIXED (2026-08-10, D6b batch 4 — case 7 → SUGAR_CANE, case 19 → ModBlocks.RICE_PLANT, Javadoc corrected; affects worldgen GREENHOUSE + DSB type 36; caught by the batch-4 cross-cutting verifier; see FIX_LOG.md)

### WGEN-064 — DisableOverworldDungeons config defined but never read (found 2026-08-10, D6b batch 4)

- **Status:** MISSING
- **Original:** `DisableOverworldDungeons == 0` gates the ENTIRE overworld dungeon dispatch — the 6-way rotation and the ahh fall-through chain (OreSpawnWorld.java:284-321).
- **Port:** `OreSpawnConfig.DISABLE_OVERWORLD_DUNGEONS` existed (OreSpawnConfig.java:131/281) but no code read it.
- **Resolution:** FIXED (2026-08-10, D close-out — worldgen-only gate in LegacyDungeonStructure.findGenerationPoint over the 11 wired overworld types (PLAY_POOL, WATER_DRAGON_LAIR, GOLD_FISH_BOWL, GIRLFRIEND_ISLAND, MONSTER_ISLAND, FROG_POND, HAUNTED_HOUSE, LEAF_MONSTER_DUNGEON, SPIT_BUG_LAIR, BOUNCY_CASTLE, RUBBER_DUCKY_POND); the DSB path stays ungated like the original; a future Igloo placement (WGEN-071) must honor it; see FIX_LOG.md)

### WGEN-065 — Royal altars: bounding box clipped skirt + air clear (found 2026-08-10, D6b batch 4, sweep flag F3)

- **Status:** DIVERGENT
- **Original:** the v=1..9 dirt skirt writes to origin−9 (GenericDungeon.java:4377-4382/5721-5726) and the j<=height+10 air clear to origin+58 (:4364/5708).
- **Port:** `KING_ALTAR/QUEEN_ALTAR(32, 4, 56)` — down 4 / up 56 dropped those writes in BOTH worldgen and buildNow.
- **Resolution:** FIXED (2026-08-10, D6b batch 4 — box widened to (32, 10, 59); piece RNG seeds from the box, so altar layouts reseed for existing seeds (pre-release, documented delta); see FIX_LOG.md)

### WGEN-066 — Alien WTF: box clipped the south Part room's far wall (found 2026-08-10, D6b batch 4, sweep flag F8)

- **Status:** DIVERGENT
- **Original:** the south Part room writes to z = origin−21 (GenericDungeon.java:1674 → makeAlienPart at sz−7 spanning 15).
- **Port:** symmetric ±20 box — the far Z wall plane was ALWAYS dropped on the buildNow path and on ~1/16 worldgen chunk alignments.
- **Resolution:** FIXED (2026-08-10, D6b batch 4 — box widened to the asymmetric (-20, 20, 25, 6, -22, 20); footprint re-derived X −19..+17, Z −21..+15 by the cross-cutting verifier; RNG-reseed delta documented; see FIX_LOG.md)

### WGEN-067 — Greenhouse: entry doors diverged (found 2026-08-10, D6b batch 4, sweep flag F1)

- **Status:** DIVERGENT
- **Original:** two full iron doors at width/2 and width/2−1 (ItemDoor dir=3 = NORTH), two stone lintels, two meta-4 stone buttons (GenericDungeon.java:5138-5147) — the same entry pattern as the Robot Lab hangar (GD:4076-4083).
- **Port:** a single hinge-only door column at the WRONG x (ox + length/2) with no second door, lintels, or buttons.
- **Resolution:** FIXED (2026-08-10, D close-out — full pattern transcribed with the D6a-verified robot-lab door trace (east leaf HINGE=LEFT, west leaf HINGE=RIGHT, FACING=NORTH); see FIX_LOG.md)

### WGEN-068 — White House: half a door and a mis-hung button (found 2026-08-10, D6b batch 4, sweep flag F2)

- **Status:** DIVERGENT
- **Original:** full 2-tall iron door via ItemDoor dir=3 + meta-4 button (GenericDungeon.java:5548-5551).
- **Port:** only the LOWER door half; button FACING=SOUTH, which attaches it to the wrong block (meta 4 = north per the robot-lab trace).
- **Resolution:** FIXED (2026-08-10, D close-out — upper half restored, button re-hung NORTH; see FIX_LOG.md)

### WGEN-069 — CrystalBattleTowerFeature: invented and dead (found 2026-08-10, D6b batch 4, sweep flag F4)

- **Status:** DIVERGENT
- **Original:** none — the faithful CrystalBattleTower port lives in CrystalStructures (GD:4831-4959 → CS builder); the Feature's loot was invented and no datapack JSON referenced it.
- **Resolution:** FIXED (2026-08-10, D6b batch 4 — class deleted + ModFeatures registration removed under the no-procedural-fabrication rule; DSB type 33 wired to the faithful CrystalStructures.buildCrystalBattleTowerAt adapter; see FIX_LOG.md)

### WGEN-070 — CrystalMazeFeature: dead duplicate registration (found 2026-08-10, D close-out, sweep flag F5)

- **Status:** DIVERGENT
- **Original:** CrystalMaze.java buildCrystalMaze (called per Crystal chunk at Y=25, ChunkProviderOreSpawn5.java:213-214) — REAL original code, so the F4 deletion rule does not apply.
- **Port:** the live faithful path is world/CrystalMaze via OreSpawnChunkGenerator.java:177 (WGEN-027 resolution made it the single placement mechanism); the parallel CrystalMazeFeature registration is datapack-orphaned AND divergent (stamps outer boundary walls, skips openCrystalMaze's perimeter carve, bedrock ordering differs).
- **Fix:** retire the Feature class + registration, or reconcile it to the original and re-wire — either way ONE mechanism should remain. Phase E owner (audit cleanup).

### WGEN-071 — Igloo: worldgen placement undecided (carved from WGEN-042 at the D close-out)

- **Status:** MISSING
- **Original:** addIgloo generates on snow-biome borders inside the overworld ahh chain (OreSpawnWorld.java:304-321 dispatch; scan per igloo_spec.md §7.3).
- **Port:** builder + DSB type 20 shipped (D6b batch 2); worldgen placement deliberately unwired — the border-biome/frequency mapping has no clean biome-tag equivalent (NEEDS_DESIGN_RULING, igloo_spec.md §7.3).
- **Fix:** decide the border mapping + frequency and add the JSON pair; the placement must honor the DisableOverworldDungeons gate (WGEN-064). Phase E owner.

### ITEM-067 — DSB Robot Lab outcome built shifted from the clicked pos (found 2026-08-10, D6b batch 4, sweep flag F7)

- **Status:** DIVERGENT
- **Original:** makeRobotLab is corner-anchored at the passed position (GenericDungeon.java:4053-4059).
- **Port:** generateRobotLab recentres (ox = x−5, oz = z−25), and the DSB case passed the clicked pos raw — the live build landed (−5, 0, −25) off.
- **Resolution:** FIXED (2026-08-10, D6b batch 4 — case pre-offsets pos.offset(5, 0, 25), the same recentring-cancel treatment as the batch's King/Queen altar, Greenhouse, and White House cases; see FIX_LOG.md)

### ITEM-068 — Bee/Mantis feature chests dropped their facings (found 2026-08-10, D6b batch 4)

- **Status:** DIVERGENT
- **Original:** every chest in fill_beehive_chests (GD:860-889), fill_mantishive_chests (GD:1064-1093), and the SmallBeeHive chamber (GD:1446) carries meta 2-5 (inward-facing).
- **Port:** all placed default (north).
- **Resolution:** FIXED (2026-08-10, D close-out — facings restored per the metas (Beehive/Mantis: E/W/S/N inward ring; SmallBeehive: EAST); see FIX_LOG.md)

### ITEM-069 — Bee/Mantis loot substituted invented items for the egg entries (found 2026-08-10, D6b batch 4)

- **Status:** DIVERGENT
- **Original:** beeContentsList carries BeeEgg 2-8 weight 15 (GenericDungeon.java:55); mantisContentsList carries MantisEgg 2-4 weight 20 (:56).
- **Port:** GOLDEN_CARROT / SPIDER_EYE stand-ins justified by a false "no equivalent item exists" premise — ModItems registers BEE_SPAWN_EGG and MANTIS_SPAWN_EGG.
- **Resolution:** FIXED (2026-08-10, D close-out — spawn eggs restored at the original stacks/weights per the repo egg-item convention (stinky_house/rubber_ducky_pond/water_dragon_lair precedents); the in-code fills now agree with the shipped chests/beehive.json; see FIX_LOG.md)""")

p.write_text(t, encoding="utf-8")
print("AUDIT_FINDINGS.md patched.")

# ---- 6. Reconcile script: total + explicit Phase E re-owning ----
rp = ROOT / "tools" / "ledger_reconcile.py"
r = rp.read_text(encoding="utf-8")
assert r.count("TOTAL_EXPECTED = 618") == 1
r = r.replace("TOTAL_EXPECTED = 618", "TOTAL_EXPECTED = 630")
old_owner = '''def expected_owner(fid: str, status: str, res: str | None) -> str:
    if res == "PARTIAL":
        return "Phase D"'''
new_owner = '''# D close-out (2026-08-10): Phase D owns zero open findings. These open
# IDs were explicitly re-owned to Phase E in phase_d_reports/phase_d_rollup.md
# §3 (entity/feature work outside the structure phase, or new close-out
# findings assigned to E).
PHASE_E_REOWNED = {
    "ENT-A-054", "ENT-A-083", "WGEN-003", "WGEN-004", "WGEN-007",
    "ITEM-023", "WGEN-070", "WGEN-071",
}


def expected_owner(fid: str, status: str, res: str | None) -> str:
    if fid in PHASE_E_REOWNED:
        return "Phase E"
    if res == "PARTIAL":
        return "Phase D"'''
assert r.count(old_owner) == 1
r = r.replace(old_owner, new_owner)
rp.write_text(r, encoding="utf-8")
print("ledger_reconcile.py updated (630, Phase E re-owning).")
