"""C7 WGEN-006/030/048 — anthill / unstable-anthill / veggie-patch wiring.

Citations (reference_1_7_10_source/sources/danger/orespawn/OreSpawnWorld.java):
- addAnts          :1472-1507 (1/30 gate, 4 attempts, redfreq picker)
- addUnstableAnts  :1572-1588 (1/30 gate, 3 attempts, Y20->3 scan)
- addVeggies       :1882-1921 (1/15 gate, 8 attempts, crop picker)
Call sites:
- Overworld generateSurface: addVeggies :279 (biome-gated River/Swampland :1887),
  addAnts(redfreq 4) :323 — every overworld chunk
- Utopia (DimensionID): generateSurface :41 (=> veggies + ants 4) plus a second
  addVeggies :46 when no huge tree generated
- Mining (DimensionID2): addAnts(2) twice :106-107, addVeggies :110
- Village (DimensionID3): addAnts(4) :118
- Islands (DimensionID4): addUnstableAnts :182
- Chaos (DimensionID6): addVeggies :205, addAnts(2) :206
"""
import json
import os

DATA = 'src/main/resources/data/orespawn/'
CF = DATA + 'worldgen/configured_feature/'
PF = DATA + 'worldgen/placed_feature/'
BM = DATA + 'neoforge/biome_modifier/'
BIOME = DATA + 'worldgen/biome/'

BIOME_CHECK = {'type': 'minecraft:biome'}
VEGETAL = 9  # GenerationStep.Decoration.VEGETAL_DECORATION ordinal


def w(path, data):
    with open(path, 'w', encoding='utf-8') as f:
        json.dump(data, f, indent=2)
        f.write('\n')


w(CF + 'anthill_common.json', {'type': 'orespawn:anthill', 'config': {'redfreq': 4}})
w(CF + 'anthill_frequent.json', {'type': 'orespawn:anthill', 'config': {'redfreq': 2}})
w(CF + 'unstable_anthill.json', {'type': 'orespawn:unstable_anthill', 'config': {}})
w(CF + 'veggie_patch.json', {'type': 'orespawn:veggie_patch', 'config': {}})

w(PF + 'anthill.json', {'feature': 'orespawn:anthill_common', 'placement': [BIOME_CHECK]})
w(PF + 'anthill_chaos.json', {'feature': 'orespawn:anthill_frequent', 'placement': [BIOME_CHECK]})
# Mining calls addAnts(2) twice per chunk -> count 2 independent runs
w(PF + 'anthill_mining.json', {'feature': 'orespawn:anthill_frequent',
                               'placement': [{'type': 'minecraft:count', 'count': 2}, BIOME_CHECK]})
w(PF + 'unstable_anthill.json', {'feature': 'orespawn:unstable_anthill', 'placement': [BIOME_CHECK]})
w(PF + 'veggie_patch.json', {'feature': 'orespawn:veggie_patch', 'placement': [BIOME_CHECK]})
# Utopia: generateSurface veggie call + the conditional second call (:46)
w(PF + 'veggie_patch_utopia.json', {'feature': 'orespawn:veggie_patch',
                                    'placement': [{'type': 'minecraft:count', 'count': 2}, BIOME_CHECK]})

w(BM + 'add_anthills.json', {'type': 'neoforge:add_features',
                             'biomes': '#minecraft:is_overworld',
                             'features': ['orespawn:anthill'],
                             'step': 'vegetal_decoration'})
w(BM + 'add_veggie_patches.json', {'type': 'neoforge:add_features',
                                   'biomes': ['minecraft:river', 'minecraft:swamp'],
                                   'features': ['orespawn:veggie_patch'],
                                   'step': 'vegetal_decoration'})

WIRES = {
    'utopia_plains': ['orespawn:anthill', 'orespawn:veggie_patch_utopia'],
    'village_biome': ['orespawn:anthill'],
    'mining_biome': ['orespawn:anthill_mining', 'orespawn:veggie_patch'],
    'chaos_biome': ['orespawn:anthill_chaos', 'orespawn:veggie_patch'],
    'island_biome': ['orespawn:unstable_anthill'],
}
for name, feats in WIRES.items():
    p = BIOME + name + '.json'
    d = json.load(open(p, encoding='utf-8'))
    while len(d['features']) <= VEGETAL:
        d['features'].append([])
    step = d['features'][VEGETAL]
    for f in feats:
        if f not in step:
            step.append(f)
    w(p, d)

print('surface features wired')
