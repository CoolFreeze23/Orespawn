"""Dump every existing checkSpawnRules override in the port for D1 comparison."""
import pathlib
import re

SRC = pathlib.Path("src/main/java/danger/orespawn")
for java in sorted(SRC.rglob("*.java")):
    text = java.read_text(encoding="utf-8", errors="replace")
    lines = text.splitlines()
    for i, line in enumerate(lines):
        if "checkSpawnRules" in line and ("public" in line or "protected" in line) and "(" in line:
            depth = 0
            out = []
            for j in range(i, len(lines)):
                out.append(f"{j + 1:4d}| {lines[j]}")
                depth += lines[j].count("{") - lines[j].count("}")
                if depth == 0 and j > i:
                    break
            print(f"### {java.relative_to(SRC)}")
            print("\n".join(out))
            print()
            break
