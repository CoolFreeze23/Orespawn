import re
import sys

t = open("AUDIT_FINDINGS.md", encoding="utf-8").read()
for b in re.split(r"(?=### )", t):
    m = re.match(r"### (ITEM-\d+)", b)
    if m:
        sys.stdout.write(b.strip()[:1200] + "\n\n" + "=" * 90 + "\n")
