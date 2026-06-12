"""C7 WGEN-001/002/011/024/032 — regenerate ore vein datapack JSONs.

All values cited from reference_1_7_10_source:
- OreSpawnMain.java:1573-1585  (get_orestats defaults)
- OreSpawnWorld.java:805-877   (overworld generateOres per-ore loops)
- OreSpawnWorld.java:879-893   (overworld ruby lava-seek, rate+nextInt(5))
- OreSpawnWorld.java:330-347   (generateRuby, rate+nextInt(7); Mining x3 via :57-63)
- OreSpawnWorld.java:64-77     (Mining lapis 45x size7 + 25x size4, Y<50, LessOre==0 only)
- OreSpawnWorld.java:243-271   (generateNether: lavafoam 15+nextInt(10) size6,
                                ruby 5+nextInt(5) size2, Y nextInt(108)+10, /3 LessOre)
- ChunkOreGenerator.java:471-544 (per-chunk dim pass: same per-ore math)
- ChunkProviderOreSpawn2.java:191-195 (Mining calls the pass 1x, +2x if LessOre==0)
"""
import json
import os

DATA = 'src/main/resources/data/orespawn/'
CF = DATA + 'worldgen/configured_feature/'
PF = DATA + 'worldgen/placed_feature/'
BM = DATA + 'neoforge/biome_modifier/'
BIOME = DATA + 'worldgen/biome/'


def w(path, data):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, 'w', encoding='utf-8') as f:
        json.dump(data, f, indent=2)
        f.write('\n')


def vein(count, extra, min_y, max_y, divisor=None, passes=None, less_ore_passes=None):
    m = {'type': 'orespawn:vein_count', 'count': count, 'min_y': min_y, 'max_y': max_y}
    if extra:
        m['extra_dice'] = extra
    if divisor is not None:
        m['less_ore_divisor'] = divisor
    if passes is not None:
        m['passes'] = passes
    if less_ore_passes is not None:
        m['less_ore_passes'] = less_ore_passes
    return m


IN_SQUARE = {'type': 'minecraft:in_square'}
BIOME_CHECK = {'type': 'minecraft:biome'}

# ---- overworld OreSpawn ores (OreSpawnWorld.generateOres) ----------------
# (name, rate, extra_dice, min_y, max_y, divisor)
OVERWORLD = [
    ('ore_uranium', 3, 9, 0, 30, 3),    # :805-816, stats :1575 (3/4/0-30)
    ('ore_titanium', 3, 9, 0, 20, 3),   # :818-829, stats :1576 (3/4/0-20)
    ('ore_amethyst', 2, 12, 0, 25, 3),  # :831-842, stats :1577 (2/6/0-25)
    ('ore_salt', 5, 9, 50, 127, 3),     # :844-855, stats :1578 (5/12/50-128; nextInt(128) caps at 127)
    ('red_ant_troll', 4, 4, 5, 50, 2),  # :857-867
    ('termite_troll', 4, 4, 5, 50, 2),  # :868-877
]
for name, rate, extra, lo, hi, div in OVERWORLD:
    w(PF + name + '.json', {'feature': 'orespawn:' + name,
                            'placement': [vein(rate, extra, lo, hi, div), IN_SQUARE, BIOME_CHECK]})
    # Mining variant: same loop executed 3x when LessOre==0, 1x (divided) otherwise
    # (ChunkProviderOreSpawn2.java:191-195 + ChunkOreGenerator.java:471-544)
    w(PF + name + '_mining.json', {'feature': 'orespawn:' + name,
                                   'placement': [vein(rate, extra, lo, hi, div, passes=3, less_ore_passes=1),
                                                 IN_SQUARE, BIOME_CHECK]})

# ---- ruby: lava-seek feature (WGEN-002) -----------------------------------
# overworld: generateOres :879-893 — rate 10 + nextInt(5), Y window 0..50, no LessOre
w(CF + 'ore_ruby.json', {'type': 'orespawn:ruby_lava_seek',
                         'config': {'attempts_base': 10, 'attempts_spread': 5, 'max_y': 50}})
w(PF + 'ore_ruby.json', {'feature': 'orespawn:ore_ruby', 'placement': [BIOME_CHECK]})
# mining: generateRuby :330-347 — rate 10 + nextInt(7); called once, plus twice
# more when LessOre==0 (:57-63)
w(CF + 'ore_ruby_mining.json', {'type': 'orespawn:ruby_lava_seek',
                                'config': {'attempts_base': 10, 'attempts_spread': 7, 'max_y': 50}})
w(PF + 'ore_ruby_mining.json', {'feature': 'orespawn:ore_ruby_mining',
                                'placement': [{'type': 'orespawn:less_ore_count', 'count': 3, 'less_ore_count': 1},
                                              BIOME_CHECK]})

# ---- mining lapis boost (WGEN-011; OreSpawnWorld.java:64-77) ---------------
lapis_targets = [
    {'target': {'predicate_type': 'minecraft:tag_match', 'tag': 'minecraft:stone_ore_replaceables'},
     'state': {'Name': 'minecraft:lapis_ore'}},
    {'target': {'predicate_type': 'minecraft:tag_match', 'tag': 'minecraft:deepslate_ore_replaceables'},
     'state': {'Name': 'minecraft:deepslate_lapis_ore'}},
]
w(CF + 'ore_lapis_mining_large.json', {'type': 'minecraft:ore',
                                       'config': {'size': 7, 'discard_chance_on_air_exposure': 0.0,
                                                  'targets': lapis_targets}})
w(CF + 'ore_lapis_mining_small.json', {'type': 'minecraft:ore',
                                       'config': {'size': 4, 'discard_chance_on_air_exposure': 0.0,
                                                  'targets': lapis_targets}})
# 45 attempts size 7 + 25 attempts size 4; Y=nextInt(128) accepted only < 50;
# the entire block is skipped when LessOre != 0 (less_ore_passes 0)
w(PF + 'ore_lapis_mining_large.json', {'feature': 'orespawn:ore_lapis_mining_large',
                                       'placement': [vein(45, 0, 0, 49, less_ore_passes=0), IN_SQUARE, BIOME_CHECK]})
w(PF + 'ore_lapis_mining_small.json', {'feature': 'orespawn:ore_lapis_mining_small',
                                       'placement': [vein(25, 0, 0, 49, less_ore_passes=0), IN_SQUARE, BIOME_CHECK]})

# ---- nether ores (WGEN-032; OreSpawnWorld.java:243-271) --------------------
nether_rack = {'predicate_type': 'minecraft:block_match', 'block': 'minecraft:netherrack'}
w(CF + 'lavafoam_nether.json', {'type': 'minecraft:ore',
                                'config': {'size': 6, 'discard_chance_on_air_exposure': 0.0,
                                           'targets': [{'target': nether_rack, 'state': {'Name': 'orespawn:lavafoam'}}]}})
w(CF + 'ore_ruby_nether.json', {'type': 'minecraft:ore',
                                'config': {'size': 2, 'discard_chance_on_air_exposure': 0.0,
                                           'targets': [{'target': nether_rack, 'state': {'Name': 'orespawn:ore_ruby'}}]}})
# lavafoam: 15+nextInt(10) veins (LessOre /3), Y = nextInt(108)+10 (no rejection)
w(PF + 'lavafoam_nether.json', {'feature': 'orespawn:lavafoam_nether',
                                'placement': [
                                    {'type': 'orespawn:less_ore_count',
                                     'count': {'type': 'minecraft:uniform', 'min_inclusive': 15, 'max_inclusive': 24},
                                     'less_ore_divisor': 3},
                                    IN_SQUARE,
                                    {'type': 'minecraft:height_range',
                                     'height': {'type': 'minecraft:uniform', 'min_inclusive': {'absolute': 10}, 'max_inclusive': {'absolute': 117}}},
                                    BIOME_CHECK]})
# ruby: 5+nextInt(5) veins (LessOre /3), same Y band
w(PF + 'ore_ruby_nether.json', {'feature': 'orespawn:ore_ruby_nether',
                                'placement': [
                                    {'type': 'orespawn:less_ore_count',
                                     'count': {'type': 'minecraft:uniform', 'min_inclusive': 5, 'max_inclusive': 9},
                                     'less_ore_divisor': 3},
                                    IN_SQUARE,
                                    {'type': 'minecraft:height_range',
                                     'height': {'type': 'minecraft:uniform', 'min_inclusive': {'absolute': 10}, 'max_inclusive': {'absolute': 117}}},
                                    BIOME_CHECK]})
w(BM + 'add_nether_ores.json', {'type': 'neoforge:add_features',
                                'biomes': '#minecraft:is_nether',
                                'features': ['orespawn:lavafoam_nether', 'orespawn:ore_ruby_nether'],
                                'step': 'underground_ores'})

# ---- WGEN-024: remove pink tourmaline veins, keep kyanite (documented) -----
for p in [CF + 'ore_pink_tourmaline.json', PF + 'ore_pink_tourmaline.json']:
    if os.path.exists(p):
        os.remove(p)
mod = json.load(open(BM + 'add_crystal_dim_ores.json', encoding='utf-8'))
mod['features'] = ['orespawn:ore_kyanite']
w(BM + 'add_crystal_dim_ores.json', mod)

# ---- biome feature lists ---------------------------------------------------
MINING_SWAP = {
    'orespawn:ore_ruby': 'orespawn:ore_ruby_mining',
    'orespawn:ore_amethyst': 'orespawn:ore_amethyst_mining',
    'orespawn:ore_uranium': 'orespawn:ore_uranium_mining',
    'orespawn:ore_titanium': 'orespawn:ore_titanium_mining',
    'orespawn:ore_salt': 'orespawn:ore_salt_mining',
}
p = BIOME + 'mining_biome.json'
d = json.load(open(p, encoding='utf-8'))
step6 = [MINING_SWAP.get(f, f) for f in d['features'][6]]
for extra in ['orespawn:red_ant_troll_mining', 'orespawn:termite_troll_mining',
              'orespawn:ore_lapis_mining_large', 'orespawn:ore_lapis_mining_small']:
    if extra not in step6:
        step6.append(extra)
# dedupe, preserving order
seen = set()
d['features'][6] = [f for f in step6 if not (f in seen or seen.add(f))]
w(p, d)

# utopia/village/chaos: generateOresInChunk runs 1x and has NO ruby pass
# (ChunkOreGenerator.java has no Ruby loop; ruby is overworld/mining/nether only)
for b in ['utopia_plains', 'village_biome', 'chaos_biome']:
    p = BIOME + b + '.json'
    d = json.load(open(p, encoding='utf-8'))
    feats = d['features'][6]
    feats = [f for f in feats if f != 'orespawn:ore_ruby']
    for troll in ['orespawn:red_ant_troll', 'orespawn:termite_troll']:
        if troll not in feats:
            feats.append(troll)
    d['features'][6] = feats
    w(p, d)

print('ore features regenerated')
