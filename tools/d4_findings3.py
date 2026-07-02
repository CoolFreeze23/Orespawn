import io, sys, re
sys.stdout = io.TextIOWrapper(sys.stdout.buffer, encoding='utf-8', errors='replace')
txt = open('AUDIT_FINDINGS.md', encoding='utf-8').read()
for fid in ['ANIM-016','ENT-D-011','ENT-D-039','ENT-D-041','ENT-A-052','ENT-A-001','ITEM-053','ENT-S-036','ENT-K-080','ENT-A-083','ITEM-064']:
    i = txt.find('### ' + fid)
    if i < 0:
        i = txt.find(fid)
    j = txt.find('### ', i + 5)
    print(txt[i:j if j > 0 else i + 2500])
    print('=======')
