import io, sys
sys.stdout = io.TextIOWrapper(sys.stdout.buffer, encoding='utf-8', errors='replace')
txt = open('AUDIT_FINDINGS.md', encoding='utf-8').read()
for fid in ['ENT-A-054', 'ITEM-020', 'ITEM-023', 'ITEM-065']:
    i = txt.find('### ' + fid)
    j = txt.find('### ', i + 5)
    print(txt[i:j if j > 0 else i + 2000])
    print('=======')
