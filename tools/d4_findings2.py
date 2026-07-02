import io, sys, re
sys.stdout = io.TextIOWrapper(sys.stdout.buffer, encoding='utf-8', errors='replace')
txt = open('AUDIT_FINDINGS.md', encoding='utf-8').read()
for fid in ['ANIM-016','ENT-D-011','ENT-D-039','ENT-D-041','ENT-A-052','ENT-A-001','ITEM-053','ENT-S-036','ENT-K-080','ENT-A-083','ITEM-064','ITEM-065']:
    for m in re.finditer(r'^.*' + fid + r'.*$', txt, re.M):
        line = m.group(0).strip()
        if len(line) < 600:
            print(line)
    print('---')
