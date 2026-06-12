"""WGEN-023 — generate assets for the 11 crystal spawn-block (OreGenericEgg) ores.

Original blocks: OreSpawnMain.java:6326-6336 — OreGenericEgg with unlocalized
names oreurchin..oreirukandji, textures OreSpawn:ore<mob>.png. Used by
ChunkProviderOreSpawn5.generateCrystalOres:586-633 (nextInt(11) pool, Y>45).
"""
import json
import os
import shutil

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
ASSETS = os.path.join(ROOT, 'src/main/resources/assets/orespawn')
DATA = os.path.join(ROOT, 'src/main/resources/data/orespawn')
REF_TEX = os.path.join(ROOT, 'reference_1_7_10_source/assets/orespawn/textures/blocks')

# (port block id, orig flat texture name, display name)
BLOCKS = [
    ('ore_urchin', 'oreurchin', 'Urchin Egg'),
    ('ore_flounder', 'oreflounder', 'Flounder Egg'),
    ('ore_skate', 'oreskate', 'Skate Egg'),
    ('ore_rotator', 'orerotator', 'Rotator Egg'),
    ('ore_peacock', 'orepeacock', 'Peacock Egg'),
    ('ore_fairy', 'orefairy', 'Fairy Egg'),
    ('ore_dungeon_beast', 'oredungeonbeast', 'Dungeon Beast Egg'),
    ('ore_vortex', 'orevortex', 'Vortex Egg'),
    ('ore_rat', 'orerat', 'Rat Egg'),
    ('ore_whale', 'orewhale', 'Whale Egg'),
    ('ore_irukandji', 'oreirukandji', 'Irukandji Egg'),
]


def write_json(path, data):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, 'w', encoding='utf-8') as f:
        json.dump(data, f, indent=2)
        f.write('\n')


for block_id, tex, display in BLOCKS:
    # texture copy
    src = os.path.join(REF_TEX, tex + '.png')
    dst = os.path.join(ASSETS, 'textures/blocks', tex + '.png')
    os.makedirs(os.path.dirname(dst), exist_ok=True)
    shutil.copyfile(src, dst)

    write_json(os.path.join(ASSETS, 'blockstates', block_id + '.json'),
               {'variants': {'': {'model': f'orespawn:block/{block_id}'}}})
    write_json(os.path.join(ASSETS, 'models/block', block_id + '.json'),
               {'parent': 'minecraft:block/cube_all',
                'textures': {'all': f'orespawn:blocks/{tex}'}})
    write_json(os.path.join(ASSETS, 'models/item', block_id + '.json'),
               {'parent': f'orespawn:block/{block_id}'})
    write_json(os.path.join(DATA, 'loot_table/blocks', block_id + '.json'),
               {'type': 'minecraft:block',
                'pools': [{'rolls': 1,
                           'entries': [{'type': 'minecraft:item', 'name': f'orespawn:{block_id}'}],
                           'conditions': [{'condition': 'minecraft:survives_explosion'}]}]})

# lang merge
lang_path = os.path.join(ASSETS, 'lang/en_us.json')
lang = json.load(open(lang_path, encoding='utf-8'))
for block_id, tex, display in BLOCKS:
    lang[f'block.orespawn.{block_id}'] = display
with open(lang_path, 'w', encoding='utf-8') as f:
    json.dump(lang, f, indent=2, ensure_ascii=False)
    f.write('\n')

print('done:', len(BLOCKS), 'blocks')
