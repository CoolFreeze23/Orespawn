import re

text = open('AUDIT_FINDINGS.md', encoding='utf-8').read()
blocks = re.split(r'(?=^### )', text, flags=re.M)
for b in blocks:
    m = re.match(r'### (WGEN-\d+)([^\n]*)', b)
    if not m:
        continue
    has_res = '- **Resolution:**' in b
    status = re.search(r'\*\*Status:\*\*\s*([A-Z-]+)', b)
    st = status.group(1) if status else '?'
    if not has_res:
        print(f'{m.group(1)} [{st}]{m.group(2)}')
