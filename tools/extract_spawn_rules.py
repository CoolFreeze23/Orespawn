"""Extract every func_70601_bi (getCanSpawnHere) override body from the 1.7.10
decompiled sources into one reference file for the Phase D1 spawn-gate port."""
import pathlib
import re

SRC = pathlib.Path("reference_1_7_10_source/sources/danger/orespawn")
OUT = pathlib.Path("phase_d_reports/D1_original_spawn_rules.md")
OUT.parent.mkdir(exist_ok=True)

sections = []
for java in sorted(SRC.glob("*.java")):
    text = java.read_text(encoding="utf-8", errors="replace")
    lines = text.splitlines()
    for i, line in enumerate(lines):
        if "func_70601_bi" not in line or "public" not in line:
            continue
        # capture the method body by brace counting
        depth = 0
        body = []
        for j in range(i, len(lines)):
            body.append(f"{j + 1:5d}| {lines[j]}")
            depth += lines[j].count("{") - lines[j].count("}")
            if depth == 0 and j > i:
                break
        sections.append(f"## {java.name} (lines {i + 1}-{j + 1})\n\n```java\n" + "\n".join(body) + "\n```\n")
        break

OUT.write_text(
    "# D1 reference — every original `func_70601_bi` (getCanSpawnHere) override\n\n"
    "Auto-extracted from `reference_1_7_10_source/sources/danger/orespawn/` by\n"
    "`tools/extract_spawn_rules.py`. Line numbers are original-file lines, cite as\n"
    "`orig <File>.java:<line>`.\n\n" + "\n".join(sections),
    encoding="utf-8",
)
print(f"Extracted {len(sections)} overrides -> {OUT}")
