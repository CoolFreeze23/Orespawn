"""D6a ledger patch: resolutions, new findings WGEN-058..062, totals 613 -> 618."""
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
p = ROOT / "AUDIT_FINDINGS.md"
t = p.read_text(encoding="utf-8")


def replace_once(old, new):
    global t
    assert t.count(old) == 1, f"anchor problem: {old[:70]!r}"
    t = t.replace(old, new)


# 1. WGEN-044
replace_once(
    "- **Fix:** Port `Trees.DuplicatorTree` as a `Feature`/TreeGrower wired to the duplicator sapling and the log's random tick.",
    "- **Fix:** Port `Trees.DuplicatorTree` as a `Feature`/TreeGrower wired to the duplicator sapling and the log's random tick.\n"
    "- **Resolution:** FIXED (2026-08-08, Phase D6a — audit corrected in part: `BlockDuplicatorLog` was ALREADY faithful to Trees.DuplicatorTree line-by-line (one-write-per-tick trunk/cap/ring growth, 20×20 duplication with whole-BlockState copies subsuming orig block+meta T:171-178, config gate) — the finding's 'log re-interprets behavior' premise was wrong. The genuinely missing half was the WORLDGEN seed: `addVeggies` plants a lone Duplicator Log on a 1-in-N crop roll (orig OreSpawnWorld.java:1915-1916), which the port's VeggiePatchFeature stubbed out; the what==5 branch now places the seed log (roll drawn unconditionally, DUPLICATOR_TREE_ENABLE gate). Spec `phase_d_reports/d6_extraction/trees_spec.md`; see FIX_LOG.md)")

# 2. WGEN-045
replace_once(
    "- **Fix:** Port `Trees.ExperienceTree` geometry as the grower for the experience sapling and any worldgen placement it had.",
    "- **Fix:** Port `Trees.ExperienceTree` geometry as the grower for the experience sapling and any worldgen placement it had.\n"
    "- **Resolution:** FIXED (2026-08-08, Phase D6a — `BlockExperiencePlant`'s self-declared placeholder grower replaced with the faithful Trees.ExperienceTree port (soil gate T:298-301, 2×2 oak trunk y+1..5 / y+7..18, crown 5+nextInt(6), makeLeaves 7×7×3 air-only T:184-194, growBranch 5-segment rolls T:245-292, growSmallBranch T:196-243); the trigger (nextInt(10)==1 growth tick, build at y−1) was already faithful. Live-tick context, so the original's world reads are legal and preserved. See FIX_LOG.md)")

# 3. WGEN-042 extension
old42 = "Remaining ~20 structures → Phase D6; see FIX_LOG.md)"
assert t.count(old42) == 1
t = t.replace(old42,
    "2026-08-08, Phase D6a — the six strong-model items landed: EnderCastle (GD:3207-3623 + End placement END_SURFACE/is_end + Islands i==7, DSB 27), IncaPyramid (GD:3735-4042, write-set model for ramp self-reads, DSB 29), KyuubiDungeon (GD:1095-1361, Mining rotation i==1, DSB 7), EnderDragonHospital (GD:2815-2991, 4 End Crystals — no dragon, End-only, DSB 24), MonsterIsland (GD:5170-5240, overworld ocean OCEAN_SURFACE, DSB 37), Robot Lab annex reconciliation (WGEN-058..061), FairyTree/FairyCastleTree DSB 0/1 + LessLag shrinks (+WGEN-062). Remaining ~16 mechanical structures → Phase D6b; see FIX_LOG.md)")

# 4. ITEM-020 extension
old20 = "remaining outcomes land with their D6 structures; see FIX_LOG.md)"
assert t.count(old20) == 1
t = t.replace(old20,
    "2026-08-08, Phase D6a — outcomes 0 (FairyTree), 1 (FairyCastleTree), 7 (Kyuubi), 24 (Hospital), 27 (EnderCastle), 29 (IncaPyramid), 30 (RobotLab), 37 (MonsterIsland) wired — 14 of 50 live; remaining outcomes land with their D6b structures; see FIX_LOG.md)")

# 5. New findings after WGEN-057
w57 = "- **Resolution:** FIXED (2026-08-08, Phase D5 — royal_trees re-salted to 84332 (next free value); pre-release, so no published worlds shift; see FIX_LOG.md)"
assert t.count(w57) == 1
t = t.replace(w57, w57 + """

### WGEN-058 — Robot Lab: invented chest palette (found 2026-08-08, Phase D6a)

- **Status:** DIVERGENT
- **Original:** `RobotContentsList` (GenericDungeon.java:37) — 23 weighted entries totalling 755 (incl. two deliberate duplicates, kits at weight 10, Ray Gun 35), fill `10 + nextInt(5)` per chest (GD:4344/4349).
- **Port:** an invented in-code 11-entry palette (falsely documented as "all weight 35") with `DROPPER`/`DISPENSER` additions, `CLOCK` in place of the repeater, and the comparator count locked to 1.
- **Resolution:** FIXED (2026-08-08, Phase D6a — palette deleted; both chests bind `chests/robot_lab.json` transcribing GD:37 entry-for-entry (23 entries, total 755, rolls 10-14) via facing-aware placeLootChest (meta 2 = NORTH, GD:4341/4346); see FIX_LOG.md)

### WGEN-059 — Robot Lab: Robo-mob spawner bindings swapped (found 2026-08-08, Phase D6a)

- **Status:** DIVERGENT
- **Original:** altar spawners = "Robo-Pounder" (= Robot2, OreSpawnMain.java:3695); treasure-room spawner = "Robo-Warrior" (= Robot4, :3711); pillar spawners = "Robo-Sniper" (= Robot5, :3719).
- **Port:** altar bound ROBOT_4 and treasure bound ROBOT_2 (swapped); pillars correct.
- **Resolution:** FIXED (2026-08-08, Phase D6a — bindings corrected with citations; see FIX_LOG.md)

### WGEN-060 — Robot Lab: build order erased the rear sniper spawners (found 2026-08-08, Phase D6a)

- **Status:** DIVERGENT
- **Original:** hangar carve FIRST (GenericDungeon.java:4084), THEN the six pillar spawners (:4085-4090).
- **Port:** pillars built before the hangar; the carve (i 10..19, j 1..3) overwrote the two rear sniper spawners with air every generation.
- **Resolution:** FIXED (2026-08-08, Phase D6a — original order restored; see FIX_LOG.md)

### WGEN-061 — Robot Lab: annex hardware divergences (found 2026-08-08, Phase D6a)

- **Status:** DIVERGENT
- **Original:** railway uses golden/powered rail (`field_150318_D`) with floor levers meta 5 unpowered (GD:4260-4293); assembly line uses quartz stairs meta 1 (WEST), white carpet, sticky piston meta 3 (SOUTH), floor lever meta 13 POWERED (GD:4295-4308); altar buttons meta 4 on the north wall (GD:4223-4258); entry = two adjacent iron doors placed north-facing with outer-jamb hinges (`ItemDoor.func_150924_a` dir 3, GD:4080-4081).
- **Port:** detector rails, wrong lever states, red carpet/wool substitutions, mis-faced piston, and doors mirrored 180° (FACING=SOUTH).
- **Resolution:** FIXED (2026-08-08, Phase D6a — all hardware restored to the original blocks/states, incl. the powered crusher lever and the NORTH-facing door pair (the last corrected by the D6a verification pass after the first fix mirrored it); see FIX_LOG.md)

### WGEN-062 — Fairy tree dispatch: scan-exhaustion return diverged (found 2026-08-08, Phase D6a)

- **Status:** DIVERGENT
- **Original:** `addFairyTree` returns TRUE when its Y 128→41 air-over-CrystalGrass scan finds no candidate (falls through to OreSpawnWorld.java:1995), so 1/5 of such chunks still suppress the termite/big-structure follow-ups with no tree placed; only the explicit 17×17/5×5 clearance failures (:1977/:1984) return false.
- **Port:** `CrystalStructures.tryPlaceFairyTree` returned false on scan exhaustion, letting follow-ups proceed.
- **Resolution:** FIXED (2026-08-08, Phase D6a — original return contract restored with citations; pattern-doc addendum updated ("port the FULL return contract"); see FIX_LOG.md)
""")

p.write_text(t, encoding="utf-8")
print("AUDIT_FINDINGS patched")

lr = ROOT / "tools/ledger_reconcile.py"
lt = lr.read_text(encoding="utf-8")
old = "TOTAL_EXPECTED = 613"
assert old in lt
lt = lt.replace(
    "#   Phase D5.\nTOTAL_EXPECTED = 613",
    "#   Phase D5.\n# + WGEN-058..062 (Robot Lab reconciliation: loot palette, mob bindings,\n#   build order, annex hardware; fairy-tree return contract), found 2026-08-08\n#   in Phase D6a.\nTOTAL_EXPECTED = 618")
lr.write_text(lt, encoding="utf-8")
print("ledger_reconcile TOTAL_EXPECTED -> 618")
