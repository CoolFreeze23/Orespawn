import re

t = open("AUDIT_FINDINGS.md", encoding="utf-8").read()
for b in re.split(r"(?=### )", t):
    m = re.match(r"### (ITEM-\d+)[^\n]*", b)
    if not m:
        continue
    st = re.search(r"\*\*Status:\*\* (\S+)", b)
    res = re.search(r"\*\*Resolution:\*\* ([^\n]*)", b)
    title = b.splitlines()[0][4:]
    status = st.group(1) if st else "?"
    resolution = res.group(1)[:60] if res else "none"
    print(f"{m.group(1)} | {status} | {resolution} | {title[:70]}")
