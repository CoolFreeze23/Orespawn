"""Compare original 1.7.10 func_70601_bi list vs port checkSpawnRules overrides."""
import pathlib
import re

orig = pathlib.Path("phase_d_reports/D1_original_spawn_rules.md").read_text(encoding="utf-8")
orig_entities = set(re.findall(r"^## (\S+)\.java", orig, re.M))

port_entities = set()
SRC = pathlib.Path("src/main/java/danger/orespawn")
for java in sorted(SRC.rglob("*.java")):
    text = java.read_text(encoding="utf-8", errors="replace")
    if re.search(r"(public|protected)[^\n]*checkSpawnRules\(", text):
        port_entities.add(java.stem)


def norm(name: str) -> str:
    return name.lower().removeprefix("entity")


port_norm = {norm(p) for p in port_entities}
missing = sorted(e for e in orig_entities if norm(e) not in port_norm)
print(f"Original rules: {len(orig_entities)}; port gates: {len(port_entities)}")
print(f"Originals still lacking a port gate ({len(missing)}):")
for m in missing:
    print(" ", m)
