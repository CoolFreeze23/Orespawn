"""TASK 0: verify uncommitted markdown diffs are purely cosmetic.

Normalizes away formatter-style changes (whitespace runs, table cell padding,
table separator dash counts, trailing whitespace, line wrapping is NOT normalized
line-by-line so we compare the whole-file token stream) and reports any real
content difference.
"""
import re
import subprocess
import sys

FILES = [
    "IMPLEMENTATION_PLAN.md", "MODERNIZATION_NOTES.md", "ORESPAWN_PORTING_AUDIT.md",
    "README.md",
    "phase_b_reports/B2_mobstats.md", "phase_b_reports/B3_riders.md",
    "phase_b_reports/B4_animations.md",
    "phase_c_reports/C1_entities_A_C.md", "phase_c_reports/C2_entities_D_I.md",
    "phase_c_reports/C3_entities_K_R.md", "phase_c_reports/C4_entities_S_Z.md",
    "phase_c_reports/C5_bosses.md", "phase_c_reports/C6_items_blocks.md",
    "phase_c_reports/C6_recipe_diff.md", "phase_c_reports/C7_worldgen.md",
    "phase_c_reports/C8_animations_gui.md",
    "phase_d_reports/D1_original_spawn_rules.md",
    "phase_d_reports/D1_preexisting_gate_audit.md",
    "reference_1_7_10_source/INDEX.md",
]


def normalize(text: str) -> str:
    text = text.replace("\r\n", "\n")
    # Collapse markdown table separator rows (|---|:---:| etc.) to a canonical form
    text = re.sub(r"^\|[\s\-:|]+\|$", "|---|", text, flags=re.M)
    # Collapse all whitespace runs to a single space (kills padding + rewrapping)
    text = re.sub(r"\s+", " ", text)
    return text.strip()


dirty = []
for f in FILES:
    old = subprocess.run(["git", "show", f"HEAD:{f}"], capture_output=True).stdout.decode("utf-8", "replace")
    new = open(f, encoding="utf-8").read()
    if normalize(old) == normalize(new):
        print(f"COSMETIC-ONLY: {f}")
    else:
        print(f"CONTENT CHANGE: {f}")
        dirty.append(f)
        # show first few token-level differences
        o, n = normalize(old), normalize(new)
        for i in range(min(len(o), len(n))):
            if o[i] != n[i]:
                print("  first divergence at char", i)
                print("  OLD ...", o[max(0, i - 80):i + 120].replace("\n", " "))
                print("  NEW ...", n[max(0, i - 80):i + 120].replace("\n", " "))
                break
        else:
            print("  one is a prefix of the other; length", len(o), "vs", len(n))
            longer = o if len(o) > len(n) else n
            print("  tail:", longer[min(len(o), len(n)):min(len(o), len(n)) + 200])

print()
print("files with real content changes:", len(dirty))
sys.exit(0)
