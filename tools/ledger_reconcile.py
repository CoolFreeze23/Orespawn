"""Ledger reconciliation for AUDIT_FINDINGS.md.

Parses every finding ID, tabulates audit status vs. resolution state, and checks
the phase-ownership expectations agreed on 2026-06-13:
  - resolved-PARTIAL findings        -> Phase D (remainder named in the resolution)
  - untouched MISSING                -> Phase D
  - untouched PARTIAL / UNVERIFIED   -> Phase E
  - BUG-014..031 (medium/low bugs)   -> Phase E (BUG-020 closed in Phase B3)
  - OPT-001..027                     -> Phase F
All 601 IDs must be accounted for: terminal + open == 601.
"""
import re
import collections
import sys

TOTAL_EXPECTED = 601
TERMINAL = {"FIXED", "VERIFIED-CORRECT", "DEFERRED"}

with open("AUDIT_FINDINGS.md", encoding="utf-8") as fh:
    text = fh.read()

entries = {}
for block in re.split(r"(?m)^### ", text)[1:]:
    header = block.split("\n", 1)[0]
    match = re.match(r"([A-Z][A-Za-z0-9-]*-\d+[a-z]?)", header)
    if not match:
        continue
    fid = match.group(1)
    if fid in entries:
        print("DUPLICATE ID:", fid)
    status_match = re.search(r"\*\*Status:\*\*\s*([A-Z-]+)", block)
    res_match = re.search(r"\*\*Resolution:\*\*\s*([A-Z-]+)", block)
    entries[fid] = (
        status_match.group(1) if status_match else "NO-STATUS",
        res_match.group(1) if res_match else None,
    )


def expected_owner(fid: str, status: str, res: str | None) -> str:
    if res == "PARTIAL":
        return "Phase D"
    if fid.startswith("OPT-"):
        return "Phase F"
    if fid.startswith("BUG-"):
        return "Phase E"  # BUG-014..031 assignment, 2026-06-13 (FIX_LOG)
    if status == "MISSING":
        return "Phase D"
    return "Phase E"  # untouched PARTIAL / UNVERIFIED


print("Total IDs parsed:", len(entries))
assert len(entries) == TOTAL_EXPECTED, f"expected {TOTAL_EXPECTED}, parsed {len(entries)}"

res_counter = collections.Counter(res or "OPEN(no-resolution)" for _, res in entries.values())
print("Resolution states:", dict(res_counter))

open_by_owner = collections.defaultdict(lambda: collections.Counter())
open_ids = collections.defaultdict(list)
for fid, (status, res) in sorted(entries.items()):
    if res in TERMINAL:
        continue
    owner = expected_owner(fid, status, res)
    key = f"{status} / res={res}"
    open_by_owner[owner][key] += 1
    open_ids[(owner, key)].append(fid)

print("\nOpen (non-terminal) by owner:")
for owner in sorted(open_by_owner):
    total = sum(open_by_owner[owner].values())
    print(f"\n  {owner}: {total}")
    for key, count in sorted(open_by_owner[owner].items()):
        print(f"    {key}: {count}")
        ids = open_ids[(owner, key)]
        for i in range(0, len(ids), 12):
            print("       ", " ".join(ids[i : i + 12]))

terminal_count = sum(1 for _, res in entries.values() if res in TERMINAL)
open_count = len(entries) - terminal_count
print(f"\nTerminal: {terminal_count}  Open: {open_count}  (sum {terminal_count + open_count})")
if terminal_count + open_count != TOTAL_EXPECTED:
    sys.exit("LEDGER MISMATCH")
