import re
import sys

ids = sys.argv[1:]
text = open('AUDIT_FINDINGS.md', encoding='utf-8').read()
blocks = re.split(r'(?=^### )', text, flags=re.M)
for b in blocks:
    m = re.match(r'### (WGEN-\d+)', b)
    if m and m.group(1) in ids:
        print(b.strip()[:3000])
        print('=' * 70)
