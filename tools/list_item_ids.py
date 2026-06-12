import re

t = open(r'src/main/java/danger/orespawn/ModItems.java', encoding='utf-8').read()
names = re.findall(r'register(?:Item)?\(\s*"([a-z0-9_]+)"', t)
want = ['sword', 'axe', 'shovel', 'pickaxe', 'hoe', 'helmet', 'chest', 'legging', 'boot', 'body',
        'egg', 'peacock', 'rice', 'quinoa', 'apple', 'ingot', 'arrow', 'bow', 'zooka', 'blt',
        'salad', 'corn', 'poison', 'rat_', 'fairy', 'kraken', 'irukandji', 'skate', 'ultimate',
        'tigers', 'pink', 'feather']
out = sorted({n for n in names if any(w in n for w in want)})
print('\n'.join(out))
