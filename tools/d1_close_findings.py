"""D1 closure: update AUDIT_FINDINGS.md resolutions for the spawn-gate slice."""
import re

PATH = "AUDIT_FINDINGS.md"
DATE = "2026-06-13"

# PARTIALs whose only remainder was the spawn gate -> FIXED, with the original rule citation.
GATE_CLOSE = {
    "ENT-A-006": "Alien.java:397-434", "ENT-A-011": "Alosaurus.java:240-279",
    "ENT-A-027": "BandP.java:278-309", "ENT-A-029": "Baryonyx.java:66-74",
    "ENT-A-035": "Basilisk.java:441-477", "ENT-A-044": "Bee.java:253-287",
    "ENT-A-064": "Brutalfly.java:290-329", "ENT-A-071": "Camarasaurus.java:78-83",
    "ENT-A-077": "CaterKiller.java:585-624", "ENT-D-004": "Dragon.java:598-611",
    "ENT-D-009": "DungeonBeast.java:275-312", "ENT-D-018": "EnderKnight.java:256-277",
    "ENT-D-021": "EnderReaper.java:253-279", "ENT-D-030": "Fairy.java:334-347",
    "ENT-D-032": "Flounder.java:219-230", "ENT-D-033": "Frog.java:240-251",
    "ENT-D-036": "GammaMetroid.java:328-365", "ENT-D-050": "Girlfriend.java:1100-1115",
    "ENT-D-053": "GoldFish.java:153-155", "ENT-D-057": "Hammerhead.java:277-316",
    "ENT-D-063": "Irukandji.java:326-337", "ENT-K-060": "Rat.java (Crystal air-pocket + buddy gates)",
    "ENT-S-008": "SeaMonster.java:544-570", "ENT-S-015": "Skate.java:318-329",
    "ENT-S-029": "Spyro.java:407-412", "ENT-S-035": "Stinky.java:286-291",
    "ENT-S-040": "TerribleTerror.java:193-214", "ENT-S-044": "TRex.java:276-315",
    "ENT-S-062": "Urchin.java:298-332 (was_spawnered side effect included)",
    "ENT-S-067": "VelocityRaptor.java:78-83",
    "ENT-S-071": "Vortex.java:240-284 (was_spawnered side effect included)",
}

# Full replacement resolution lines.
SET_RESOLUTION = {
    "ENT-SYS-002": f"**Resolution:** FIXED ({DATE}, Phase D1 — all 103 original `func_70601_bi` gates now have `checkSpawnRules` ports built on `OriginalSpawnGates` + `ModDimensionKeys`; corpus in phase_d_reports/D1_original_spawn_rules.md, coverage verified by tools/d1_gate_diff.py (0 missing); pre-existing divergent gates rebuilt (tools/fix_preexisting_gates.py, audit in phase_d_reports/D1_preexisting_gate_audit.md); see FIX_LOG.md)",
    "ENT-SYS2-004": f"**Resolution:** FIXED ({DATE}, Phase D1 — same slice as ENT-SYS-002: Kraken/LeafMonster/LurkingTerror/Mantis/Molenoid/Nastysaurus/Peacock/Rat/Rotator/Tshirt/Scorpion gates all ported with original bounds; Peacock findBuddies cap now enforced; see FIX_LOG.md)",
    "ENT-SYS2-003": f"**Resolution:** FIXED ({DATE}, Phase D1 — rosters were restored by the Phase C7 dimension-roster rebuild (Robot1-5 + PitchBlack in village/chaos/island biome JSONs, SpiderDriver village w20 3-5); their `func_70601_bi` gates ported this slice (Robot1-5, PitchBlack, SpiderDriver, GiantRobot); see FIX_LOG.md)",
    "WGEN-013": f"**Resolution:** FIXED ({DATE}, Phase D1 — verified already present: mining_biome.json carries VelocityRaptor w1 2-4, Dragonfly w2 1-3, Camarasaurus w1 2-4, Baryonyx w2 4-8 from the Phase C7 roster rebuild per ChunkProviderOreSpawn2.java:410-419; no change needed)",
    "WGEN-015": f"**Resolution:** FIXED ({DATE}, Phase D1 — worldgen/structure/dim_village.json (vanilla plains jigsaw start pool, biome orespawn:village_biome) + worldgen/structure_set/dim_villages.json spacing 9 / separation 7 per MapGenMoreVillages.java:11-12; villages are modern 1.21.1 jigsaw villages, see PARITY_NOTES)",
}

text = open(PATH, encoding="utf-8").read()

for fid, cite in GATE_CLOSE.items():
    block = re.search(r"### " + fid + r"\b.*?(?=\n### |\Z)", text, re.S)
    assert block, fid
    old = re.search(r"- \*\*Resolution:\*\* PARTIAL \(([^\n]*)\)", block.group(0))
    assert old, fid
    prior = old.group(1).split(";")[0].strip()
    new_line = (f"- **Resolution:** FIXED ({DATE}, Phase D1 — spawn-rule gate ported in checkSpawnRules "
                f"citing orig {cite}; weights/biomes half closed in Phase C ({prior}); see FIX_LOG.md)")
    text = text.replace(block.group(0), block.group(0).replace(old.group(0), new_line), 1)

for fid, line in SET_RESOLUTION.items():
    block = re.search(r"### " + fid + r"\b.*?(?=\n### |\n---|\Z)", text, re.S)
    assert block, fid
    b = block.group(0)
    if "**Resolution:**" in b:
        nb = re.sub(r"- \*\*Resolution:\*\*[^\n]*", "- " + line, b, count=1)
    else:
        nb = b.rstrip() + "\n- " + line + "\n"
    text = text.replace(b, nb, 1)

# Seasonal-only remainders: re-point the PARTIAL note now that gates are done.
SEASONAL = {
    "ENT-D-011": ("ENT-SYS-002 (Phase D)", "the seasonal-gates slice (Phase D; spawn-rule gate itself ported in D1, orig EasterBunny.java:67-77)"),
    "ENT-D-039": ("ENT-SYS-002 (Phase D) along with the dark-spawn rules", "the seasonal-gates slice (Phase D); the spawn-rule gate itself was ported in D1 (orig Ghost.java:145-160)"),
    "ENT-D-041": ("ENT-SYS-002 (Phase D)", "the seasonal-gates slice (Phase D); the spawn-rule gate itself was ported in D1 (orig GhostSkelly.java:173-188)"),
}
for fid, (old_frag, new_frag) in SEASONAL.items():
    block = re.search(r"### " + fid + r"\b.*?(?=\n### |\Z)", text, re.S)
    assert block, fid
    assert old_frag in block.group(0), fid
    text = text.replace(block.group(0), block.group(0).replace(old_frag, new_frag, 1), 1)

# Category/spawn-entry findings closed this slice (no Resolution line existed).
APPEND_RESOLUTION = {
    "ENT-A-021": f"**Resolution:** FIXED ({DATE}, Phase D1 — MobCategory MONSTER→WATER_CREATURE, IN_WATER spawn placement, river/swamp/ocean BM JSONs restored, checkSpawnRules y>=50+day per orig AttackSquid.java; see FIX_LOG.md)",
    "ENT-A-085": f"**Resolution:** FIXED ({DATE}, Phase D1 — MobCategory MISC→AMBIENT, ON_GROUND placement, snowy-biome BM JSON, gate with badmood spawner bypass per orig Cephadrome.java; see FIX_LOG.md)",
    "ENT-A-099": f"**Resolution:** FIXED ({DATE}, Phase D1 — MobCategory MISC→AMBIENT, ON_GROUND placement, overworld BM JSON, gate day/y>=50/no-other-Coin per orig Coin.java:138-148; see FIX_LOG.md)",
    "ENT-K-085": f"**Resolution:** FIXED ({DATE}, Phase D1 — MobCategory CREATURE→WATER_CREATURE, IN_WATER placement, river/deep-ocean BM JSONs, gate spawner-bypass/y>=50/day per orig RubberDucky.java:508-526; see FIX_LOG.md)",
    "ENT-S-087": f"**Resolution:** FIXED ({DATE}, Phase D1 — MobCategory MONSTER→CREATURE, plains/savanna BM JSONs, gate with wormsSpawned side effect per orig WormLarge.java; see FIX_LOG.md)",
    "ENT-D-046": f"**Resolution:** FIXED ({DATE}, Phase D1 — village roster entry was restored in C7 (w8 1-2 per BiomeGenUtopianPlains.java:289); checkSpawnRules gate ported this slice (orig GiantRobot.java:364-381); see FIX_LOG.md)",
}
for fid, line in APPEND_RESOLUTION.items():
    block = re.search(r"### " + fid + r"\b.*?(?=\n### |\n---|\n## |\Z)", text, re.S)
    assert block, fid
    b = block.group(0)
    assert "**Resolution:**" not in b, fid + " already has resolution"
    nb = b.rstrip() + "\n- " + line + "\n"
    text = text.replace(b, nb, 1)

open(PATH, "w", encoding="utf-8").write(text)
print("updated", len(GATE_CLOSE) + len(SET_RESOLUTION) + len(SEASONAL) + len(APPEND_RESOLUTION), "findings")
