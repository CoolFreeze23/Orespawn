import re

for path, names in [
    (r'reference_1_7_10_source/sources/danger/orespawn/GenericDungeon.java',
     ['chestContentsList', 'beeContentsList']),
    (r'reference_1_7_10_source/sources/danger/orespawn/RubyBirdDungeon.java',
     ['chestContentsList']),
]:
    text = open(path, encoding='utf-8').read()
    for name in names:
        m = re.search(re.escape(name) + r'\s*=\s*new WeightedRandomChestContent\[\]\{(.*?)\};', text, re.S)
        if not m:
            print(f'--- {name} in {path}: NOT FOUND ---')
            continue
        line_no = text[:m.start()].count('\n') + 1
        entries = re.findall(r'new WeightedRandomChestContent\((.*?)\)(?=, new|$)', m.group(1))
        print(f'--- {path.split("/")[-1]} {name} (line {line_no}, {len(entries)} entries) ---')
        for e in entries:
            print('  ', ' '.join(e.split()))
