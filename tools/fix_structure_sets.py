"""C7 WGEN-008/022/027/039/040/043 — structure set/tag corrections.

Frequency math (random_spread expected density ~ 1/spacing^2 chunks):
- Mining rotation (orig OreSpawnWorld.java:79-101): recently_placed==0 &&
  nextInt(95)==1 gate, then nextInt(7) picks 1 of 7 structures
  -> each ~ 1/(95*7) = 1/665 chunks -> spacing 26 (26^2=676). Applies to
  shadow dungeon, alien WTF, Leonopteryx nest (WGEN-039) and the relocated
  big BeeHive (WGEN-040).
- Islands D4 rotation (orig OreSpawnWorld.java:134-177): nextInt(100)==0
  gate + nextInt(19) pick:
  - i<3 -> D4Castle (challenge towers, 50/50 King/Queen via :2219)
    -> per tower 1/100 * 3/19 * 1/2 = 1/1267 -> spacing 36 (36^2=1296) - the
    port's existing 36/18 kept, only the biome re-tagged chaos -> islands
    (WGEN-043: the towers ARE 1.7.10 content, makeEnormousCastle/Q,
    GenericDungeon.java:191/6393, placed by addD4Castle :2203).
  - i==9 RobotLab, i==13 Greenhouse, i==16 WhiteHouse -> each
    1/100 * 1/19 = 1/1900 -> spacing 44 (44^2=1936) (WGEN-022, re-tagged
    village -> islands).
- Utopia King/Queen Altar (orig OreSpawnWorld.java:2549-2571): nextInt(2000)==1
  -> set of two structures at spacing 45 (45^2=2025) (WGEN-008).
"""
import json
import os

WG = 'src/main/resources/data/orespawn/worldgen/'
TAGS = 'src/main/resources/data/orespawn/tags/worldgen/biome/has_structure/'


def set_spacing(name, spacing, separation):
    p = WG + 'structure_set/' + name + '.json'
    d = json.load(open(p, encoding='utf-8'))
    d['placement']['spacing'] = spacing
    d['placement']['separation'] = separation
    json.dump(d, open(p, 'w', encoding='utf-8'), indent=2)


def set_tag(name, values):
    p = TAGS + name + '.json'
    d = json.load(open(p, encoding='utf-8'))
    d['values'] = values
    json.dump(d, open(p, 'w', encoding='utf-8'), indent=2)


# WGEN-039 — mining rotation structures: 1/665 -> 26/13 (was 32/16)
for s in ['shadow_dungeon', 'wtf_alien_dungeon', 'leonopteryx_nest']:
    set_spacing(s, 26, 13)

# WGEN-040 — big BeeHive back to Mining (orig rotation slot i==2), same 1/665
set_spacing('beehive', 26, 13)
set_tag('beehive', ['orespawn:mining_biome'])

# WGEN-022 — Greenhouse/RobotLab/WhiteHouse home dimension is Islands (D4)
for s in ['greenhouse', 'robot_lab', 'white_house']:
    set_spacing(s, 44, 22)
    set_tag(s, ['orespawn:island_biome'])

# WGEN-043 — Challenge towers are D4Castle content -> Islands
for s in ['challenge_tower_king', 'challenge_tower_queen']:
    set_tag(s, ['orespawn:island_biome'])

# WGEN-008 — royal altars 1/2000 -> 45/22 (was 48/24)
set_spacing('royal_altars', 45, 22)

# WGEN-027 — crystal maze / battle tower: chunk-generator code is the single
# placement mechanism; the JSON structure sets double-generated (maze set was
# spacing 1 = every chunk on top of the code carve).
for f in ['structure_set/crystal_maze.json', 'structure_set/crystal_battle_tower.json',
          'structure/crystal_maze.json', 'structure/crystal_battle_tower.json',
          'placed_feature/crystal_maze.json', 'placed_feature/crystal_battle_tower.json',
          'configured_feature/crystal_maze.json', 'configured_feature/crystal_battle_tower.json',
          'placed_feature/crystal_tree.json', 'placed_feature/crystal_tree_2.json',
          'placed_feature/crystal_tree_3.json',
          'configured_feature/crystal_tree.json', 'configured_feature/crystal_tree_2.json',
          'configured_feature/crystal_tree_3.json',
          'placed_feature/crystal_flowers.json', 'configured_feature/crystal_flowers.json']:
    p = WG + f
    if os.path.exists(p):
        os.remove(p)
for f in ['crystal_maze.json', 'crystal_battle_tower.json']:
    p = TAGS + f
    if os.path.exists(p):
        os.remove(p)

print('structure sets fixed')
