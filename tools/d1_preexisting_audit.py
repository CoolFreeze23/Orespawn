"""List port checkSpawnRules gates NOT written by the D1 insert scripts or the
known manual edits, pairing each with its original rule from the D1 corpus."""
import pathlib
import re

SRC = pathlib.Path("src/main/java/danger/orespawn/entity")

inserted = set()
for tool in ["tools/insert_spawn_gates.py", "tools/insert_spawn_gates_d1b.py"]:
    text = pathlib.Path(tool).read_text(encoding="utf-8")
    inserted |= {m.replace(".java", "") for m in re.findall(r'"(\w+\.java)": \(', text)}

manual = {"AttackSquid", "CaveFisher", "Crab", "EntityRotator", "EntityRat",
          "Cephadrome", "EntityWormLarge", "PitchBlack", "EntityVortex"}

orig = pathlib.Path("phase_d_reports/D1_original_spawn_rules.md").read_text(encoding="utf-8")
orig_map = {}
for m in re.finditer(r"^## (\S+)\.java \(lines ([0-9-]+)\)\n\n```java\n(.*?)```", orig, re.S | re.M):
    orig_map[m.group(1).lower().replace("entity", "")] = (m.group(1), m.group(2), m.group(3))

for java in sorted(SRC.rglob("*.java")):
    text = java.read_text(encoding="utf-8", errors="replace")
    if not re.search(r"(public|protected)[^\n]*checkSpawnRules\(", text):
        continue
    stem = java.stem
    if stem in inserted or stem in manual:
        continue
    key = stem.lower().removeprefix("entity")
    has_orig = key in orig_map
    print(f"### {stem}  (original rule: {'%s.java:%s' % orig_map[key][:2] if has_orig else 'NONE'})")
    gate = re.search(r"(/\*\*.*?\*/\s+)?@Override\s+public boolean checkSpawnRules.*?\n    \}", text, re.S)
    print("PORT:")
    print(gate.group(0) if gate else "??")
    if has_orig:
        print("ORIG:")
        print(orig_map[key][2])
    print()
