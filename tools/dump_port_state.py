"""Dump dim-locals biome modifiers + registered entity ids (C7 working tool)."""
import json
import re

BASE = 'src/main/resources/data/orespawn/neoforge/biome_modifier/'
MODS = ['dim_utopia_locals', 'dim_mining_locals', 'dim_islands_locals',
        'dim_chaos_locals', 'dim_village_locals', 'dim_crystal_locals',
        'add_nether_spawns', 'add_sky_spawns', 'add_overworld_creatures']
for m in MODS:
    try:
        data = json.load(open(BASE + m + '.json', encoding='utf-8'))
        print('==', m, json.dumps(data, separators=(',', ':')))
    except FileNotFoundError:
        print('==', m, 'MISSING')

src = open('src/main/java/danger/orespawn/ModEntities.java', encoding='utf-8').read()
ids = re.findall(r'register\(\s*"([a-z0-9_]+)"', src)
print('ENTITY IDS (%d):' % len(ids), sorted(ids))
