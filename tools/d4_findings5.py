import io, sys
sys.stdout = io.TextIOWrapper(sys.stdout.buffer, encoding='utf-8', errors='replace')
txt = open('AUDIT_FINDINGS.md', encoding='utf-8').read()
ids = ['ITEM-063','ITEM-029','ITEM-057','ITEM-022','ENT-K-076','ITEM-060','ITEM-061','ITEM-062',
       'ENT-K-007','ENT-S-025','ENT-S-047','ENT-A-088','ENT-D-052','ENT-K-084','ENT-S-034',
       'ENT-K-011','ENT-A-098','ENT-S-085','ENT-S-078','ENT-K-047','ENT-K-048','ENT-D-010',
       'ENT-S-059','ENT-S-036','ENT-K-080','ITEM-053','ENT-A-001','ENT-A-052']
for fid in ids:
    i = txt.find('### ' + fid)
    if i < 0:
        print(fid, 'NOT FOUND'); continue
    end = txt.find('\n', i)
    block_end = txt.find('### ', i + 5)
    block = txt[i:block_end if block_end > 0 else i+1500]
    has_res = '**Resolution:**' in block
    print(txt[i+4:end], '| hasResolution:', has_res)
