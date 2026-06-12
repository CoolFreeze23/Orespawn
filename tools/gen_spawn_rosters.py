"""C7 WGEN-009/012/017/020/029 — rebuild dimension spawn rosters.

Authority: reference_1_7_10_source/sources/danger/orespawn/BiomeGenUtopianPlains.java
- ctor (Utopia)            :88-140
- setIslandCreatures       :142-199  (lists RESET — no vanilla defaults)
- setCrystalCreatures      :201-270  (out of C7 scope, untouched)
- setVillageCreatures      :272-332  (lists NOT reset — Utopia ctor + vanilla defaults remain)
- setChaosCreatures        :334-516  (lists RESET)
and ChunkProviderOreSpawn2.java:365-429 (Mining monster/ambient overlays on top of
vanilla Extreme Hills defaults; WorldProviderOreSpawn2 uses
WorldChunkManagerHell(BiomeGenBase.extremeHills), which keeps the 1.7.10
BiomeGenBase default spawn lists).

1.7.10 BiomeGenBase defaults (kept by Utopia/Village/Mining):
- creature: sheep 12(4,4), pig 10(4,4), chicken 10(4,4), cow 8(4,4)
- monster: spider 100(4,4), zombie 100(4,4), skeleton 100(4,4), creeper 100(4,4),
  slime 100(4,4), enderman 10(1,4), witch 5(1,1)
- waterCreature: squid 10(4,4) — ambient: bat 10(8,8)

The dim_*_locals biome modifiers previously duplicated several biome-JSON
entries (doubling their weights) and carried port inventions
(utopia alosaurus/baryonyx, chaos vampire_butterfly, mining rat/worms/etc.);
the rosters are consolidated into the biome JSONs and those modifiers removed.
"""
import json
import os

BIOME = 'src/main/resources/data/orespawn/worldgen/biome/'
BM = 'src/main/resources/data/orespawn/neoforge/biome_modifier/'


def e(type_, weight, mn, mx):
    ns = type_ if ':' in type_ else 'orespawn:' + type_
    return {'type': ns, 'weight': weight, 'minCount': mn, 'maxCount': mx}


VANILLA_CREATURE = [e('minecraft:sheep', 12, 4, 4), e('minecraft:pig', 10, 4, 4),
                    e('minecraft:chicken', 10, 4, 4), e('minecraft:cow', 8, 4, 4)]
VANILLA_MONSTER = [e('minecraft:spider', 100, 4, 4), e('minecraft:zombie', 100, 4, 4),
                   e('minecraft:skeleton', 100, 4, 4), e('minecraft:creeper', 100, 4, 4),
                   e('minecraft:slime', 100, 4, 4), e('minecraft:enderman', 10, 1, 4),
                   e('minecraft:witch', 5, 1, 1)]
VANILLA_WATER = [e('minecraft:squid', 10, 4, 4)]
VANILLA_AMBIENT = [e('minecraft:bat', 10, 8, 8)]

# Utopia ctor customs (BiomeGenUtopianPlains.java:88-136). Categories follow
# the port's existing placement (small critters under "creature") to keep the
# 1.21 mob-cap pools the port already balanced; weights/groups are the
# original SpawnListEntry args.
UTOPIA_CREATURE = [
    e('gazelle', 10, 2, 4),            # :91
    e('girlfriend', 5, 2, 3),          # :97
    e('boyfriend', 5, 2, 3),           # :100
    e('red_cow', 10, 4, 8),            # :103
    e('gold_cow', 8, 2, 6),            # :104
    e('enchanted_apple_cow', 5, 2, 4), # :105
    e('chipmunk', 3, 1, 2),            # :114
    e('cockateil', 10, 2, 4),          # :117
    e('gold_fish', 1, 1, 1),           # :120
    e('coin', 2, 1, 1),                # :129
    e('cricket', 5, 4, 6),             # :132
]
UTOPIA_AMBIENT = [
    e('firefly', 15, 3, 6),            # :94
    e('butterfly', 20, 3, 6),          # :108
    e('luna_moth', 10, 1, 5),          # :111
]
UTOPIA_WATER = [
    e('whale', 1, 1, 1),               # :123
    e('flounder', 2, 2, 4),            # :126
    e('frog', 5, 4, 6),                # :135
]

# Village additions (setVillageCreatures :272-332) — appended on TOP of the
# Utopia ctor list and vanilla defaults (lists are not reset).
VILLAGE_MONSTER = [
    e('robot_1', 25, 4, 8),            # :274
    e('robot_2', 16, 2, 8),            # :277
    e('robot_3', 12, 2, 4),            # :280
    e('robot_4', 8, 1, 2),             # :283
    e('robot_5', 20, 4, 8),            # :286
    e('giant_robot', 8, 1, 2),         # :289 (JefferyEnable -> GiantRobot)
    e('spider_driver', 20, 3, 5),      # :292
    e('godzilla', 2, 1, 1),            # :295
    e('band_p', 15, 1, 2),             # :330 (CriminalEnable -> BandP)
]
VILLAGE_EXTRA_CREATURE = [
    e('girlfriend', 1, 2, 3),          # :301 (second entry alongside ctor's 5)
    e('boyfriend', 1, 2, 3),           # :304
    e('red_cow', 8, 4, 8),             # :307
    e('gold_cow', 6, 2, 6),            # :308
    e('enchanted_apple_cow', 4, 2, 4), # :309
    e('chipmunk', 5, 1, 2),            # :318
    e('cockateil', 15, 2, 4),          # :321
    e('tshirt', 2, 1, 1),              # :324
    e('coin', 2, 1, 1),                # :327
]
VILLAGE_EXTRA_AMBIENT = [
    e('firefly', 10, 3, 6),            # :298
    e('butterfly', 25, 3, 6),          # :312
    e('luna_moth', 20, 1, 5),          # :315
]

# Islands (setIslandCreatures :142-199) — reset lists, no vanilla defaults.
# island/island_too retained: port mechanism for the original addIslands
# floating-island entities (placement itself is WGEN-021, Phase D).
ISLAND_MONSTER = [
    e('creeping_horror', 60, 4, 8),    # :179
    e('terrible_terror', 25, 3, 6),    # :182
    e('lurking_terror', 1, 1, 1),      # :185
    e('pitch_black', 15, 3, 6),        # :188
    e('leaf_monster', 35, 2, 4),       # :191
    e('ender_reaper', 25, 2, 4),       # :194
    e('hercules_beetle', 5, 1, 2),     # :197
]
ISLAND_CREATURE = [
    e('island', 1, 1, 1),
    e('island_too', 1, 1, 1),
    e('dragon', 1, 1, 2),              # :164
    e('stinky', 2, 1, 2),              # :167
    e('cliff_racer', 20, 3, 6),        # :170
    e('cloud_shark', 1, 1, 1),         # :173
    e('gold_fish', 5, 2, 4),           # :176
]
ISLAND_AMBIENT = [
    e('butterfly', 5, 2, 6),           # :152
    e('cockateil', 4, 1, 2),           # :155
    e('luna_moth', 5, 2, 4),           # :158
    e('firefly', 10, 4, 8),            # :161
]

# Chaos (setChaosCreatures :334-516) — reset lists, no vanilla defaults.
CHAOS_MONSTER = [
    e('vortex', 1, 1, 2),              # :406
    e('pitch_black', 1, 1, 2),         # :409
    e('terrible_terror', 4, 2, 6),     # :412
    e('alosaurus', 1, 1, 1),           # :415
    e('basilisk', 1, 1, 1),            # :418
    e('robot_1', 5, 2, 8),             # :421
    e('robot_2', 2, 1, 4),             # :424
    e('robot_3', 2, 1, 4),             # :427
    e('robot_4', 1, 1, 2),             # :430
    e('robot_5', 2, 3, 5),             # :433
    e('cater_killer', 1, 1, 1),        # :436
    e('cave_fisher', 5, 1, 5),         # :439
    e('creeping_horror', 5, 1, 5),     # :442
    e('cryolophosaurus', 5, 1, 5),     # :445
    e('urchin', 2, 1, 5),              # :448
    e('dungeon_beast', 2, 1, 5),       # :451
    e('emperor_scorpion', 1, 1, 1),    # :454
    e('ender_knight', 2, 1, 2),        # :457
    e('ender_reaper', 1, 1, 1),        # :460
    e('hammerhead', 1, 1, 1),          # :463
    e('hercules_beetle', 1, 1, 1),     # :466
    e('trooper_bug', 1, 1, 1),         # :469
    e('molenoid', 1, 1, 1),            # :472
    e('mothra', 1, 1, 1),              # :475
    e('brutalfly', 1, 1, 1),           # :478
    e('rat', 10, 1, 10),               # :481
    e('rotator', 1, 1, 3),             # :484
    e('scorpion', 2, 1, 3),            # :487
    e('spit_bug', 2, 1, 3),            # :490
    e('nastysaurus', 1, 1, 1),         # :493
    e('trex', 1, 1, 1),                # :496
    e('leaf_monster', 2, 1, 4),        # :499
    e('pointysaurus', 2, 1, 4),        # :502
    e('leon', 1, 1, 1),                # :505
    e('mantis', 1, 1, 1),              # :508
    e('lurking_terror', 1, 1, 1),      # :511
    e('gamma_metroid', 1, 1, 1),       # :514
]
CHAOS_CREATURE = [
    e('beaver', 1, 1, 2),              # :398
    e('red_cow', 3, 2, 4),             # :401
    e('gold_cow', 2, 2, 4),            # :402
    e('enchanted_apple_cow', 1, 2, 4), # :403
    e('baryonyx', 2, 2, 4),            # :374 (creature-registered in port)
    e('bee', 2, 2, 4),                 # :377
    e('cassowary', 2, 2, 4),           # :380
    e('dragonfly', 2, 2, 4),           # :383
    e('peacock', 2, 2, 4),             # :386
    e('stink_bug', 3, 2, 4),           # :389
    e('ostrich', 1, 1, 2),             # :392
    e('chipmunk', 1, 1, 2),            # :395
    e('cockateil', 10, 2, 4),          # :356
    e('gold_fish', 10, 2, 4),          # :368
    e('cliff_racer', 30, 3, 6),        # :362
    e('cloud_shark', 2, 1, 1),         # :365
    e('fairy', 5, 2, 4),               # :371
]
CHAOS_AMBIENT = [
    e('butterfly', 20, 3, 6),          # :350
    e('luna_moth', 10, 1, 5),          # :353
    e('firefly', 15, 3, 6),            # :359
]

# Mining (ChunkProviderOreSpawn2.java:374-399 monster overlay,
# :409-419 ambient overlay; underlying biome = vanilla Extreme Hills).
MINING_MONSTER = [
    e('alosaurus', 8, 1, 2),           # :374
    e('trex', 6, 1, 2),                # :377
    e('nastysaurus', 6, 1, 2),         # :380
    e('pointysaurus', 10, 4, 8),       # :383
    e('gamma_metroid', 35, 4, 7),      # :386
    e('alien', 35, 2, 3),              # :389
    e('cave_fisher', 35, 4, 8),        # :392
    e('cryolophosaurus', 26, 4, 7),    # :395
    e('spyro', 5, 1, 2),               # :398
]
MINING_AMBIENT = [
    e('velocity_raptor', 1, 2, 4),     # :410
    e('dragonfly', 2, 1, 3),           # :413
    e('camarasaurus', 1, 2, 4),        # :416
    e('baryonyx', 2, 4, 8),            # :419
]


def write_spawners(biome_file, spawners):
    p = BIOME + biome_file
    d = json.load(open(p, encoding='utf-8'))
    d['spawners'] = spawners
    with open(p, 'w', encoding='utf-8') as f:
        json.dump(d, f, indent=2)
        f.write('\n')


write_spawners('utopia_plains.json', {
    'monster': VANILLA_MONSTER,
    'creature': VANILLA_CREATURE + UTOPIA_CREATURE,
    'ambient': VANILLA_AMBIENT + UTOPIA_AMBIENT,
    'water_creature': VANILLA_WATER + UTOPIA_WATER,
    'water_ambient': [], 'underground_water_creature': [], 'axolotls': [], 'misc': [],
})
write_spawners('village_biome.json', {
    'monster': VANILLA_MONSTER + VILLAGE_MONSTER,
    'creature': VANILLA_CREATURE + UTOPIA_CREATURE + VILLAGE_EXTRA_CREATURE,
    'ambient': VANILLA_AMBIENT + UTOPIA_AMBIENT + VILLAGE_EXTRA_AMBIENT,
    'water_creature': VANILLA_WATER + UTOPIA_WATER,
    'water_ambient': [], 'underground_water_creature': [], 'axolotls': [], 'misc': [],
})
write_spawners('island_biome.json', {
    'monster': ISLAND_MONSTER,
    'creature': ISLAND_CREATURE,
    'ambient': ISLAND_AMBIENT,
    'water_creature': [], 'water_ambient': [], 'underground_water_creature': [], 'axolotls': [], 'misc': [],
})
write_spawners('chaos_biome.json', {
    'monster': CHAOS_MONSTER,
    'creature': CHAOS_CREATURE,
    'ambient': CHAOS_AMBIENT,
    'water_creature': [], 'water_ambient': [], 'underground_water_creature': [], 'axolotls': [], 'misc': [],
})
write_spawners('mining_biome.json', {
    'monster': VANILLA_MONSTER + MINING_MONSTER,
    'creature': VANILLA_CREATURE,
    'ambient': VANILLA_AMBIENT + MINING_AMBIENT,
    'water_creature': VANILLA_WATER,
    'water_ambient': [], 'underground_water_creature': [], 'axolotls': [], 'misc': [],
})

# locals modifiers consolidated into the biome JSONs above
for f in ['dim_utopia_locals', 'dim_village_locals', 'dim_islands_locals',
          'dim_chaos_locals', 'dim_mining_locals']:
    p = BM + f + '.json'
    if os.path.exists(p):
        os.remove(p)

print('spawn rosters rebuilt')
