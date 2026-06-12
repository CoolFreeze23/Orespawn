"""Phase C7 (WGEN-025/033/040) loot table generator.

Transcribes the 1.7.10 WeightedRandomChestContent arrays into 1.21.1 chest
loot tables. Source arrays:
  - GenericDungeon.chestContentsList   (orig GenericDungeon.java:62,  91 entries, rolls 5+nextInt(7) at :183)
  - RubyBirdDungeon.chestContentsList  (orig RubyBirdDungeon.java:18, 14 entries, rolls 4+nextInt(7) at :80)
  - Trees.CrystalChestContentsList     (orig Trees.java:19,           82 entries, rolls 1+nextInt(5) at Trees.java:488/612,
                                        rolls 1+nextInt(3) for maze chests at OreSpawnWorld.java:1762)
  - GenericDungeon.CrystalBattleTower*ContentsList (orig GenericDungeon.java:32-36,
                                        rolls 5+nextInt(5) at :4890/:4907/:4924/:4941, 6+nextInt(6) at :4958/:6256)
  - GenericDungeon.beeContentsList     (orig GenericDungeon.java:55, rolls 1+nextInt(5) at :869/:875/:881)

Each WeightedRandomChestContent(item, meta, min, max, weight) becomes a
weighted pool entry with set_count uniform(min,max); the original's N
random picks become a uniform rolls range on the pool.
"""
import json
import os

OUT = os.path.join('src', 'main', 'resources', 'data', 'orespawn', 'loot_table', 'chests')


def entry(item, weight, cmin=1, cmax=1):
    e = {"type": "minecraft:item", "name": item, "weight": weight}
    if cmax > 1:
        e["functions"] = [{
            "function": "minecraft:set_count",
            "count": {"type": "minecraft:uniform", "min": float(cmin), "max": float(cmax)},
        }]
    return e


def table(rolls_min, rolls_max, entries):
    return {
        "type": "minecraft:chest",
        "pools": [{
            "rolls": {"type": "minecraft:uniform", "min": float(rolls_min), "max": float(rolls_max)},
            "entries": entries,
        }],
    }


def egg(name, weight=15, cmin=1, cmax=4, ns='orespawn'):
    return entry(f"{ns}:{name}_spawn_egg", weight, cmin, cmax)


# --- chests/generic_dungeon (orig GenericDungeon.java:62, rolls 5-11 :183) ---
generic = [
    entry("orespawn:cooked_bacon", 20, 6, 12),          # MyBacon 6-12 w20
    entry("orespawn:butter_candy", 20, 6, 12),          # MyButterCandy 6-12 w20
    entry("minecraft:emerald", 15, 2, 8),               # Items.emerald 2-8 w15
    entry("orespawn:emerald_pickaxe", 15),
    entry("orespawn:emerald_shovel", 15),
    entry("orespawn:emerald_hoe", 15),
    entry("orespawn:emerald_axe", 15),
    entry("orespawn:emerald_sword", 15),
    entry("orespawn:emerald_chestplate", 15),
    entry("orespawn:emerald_leggings", 15),
    entry("orespawn:emerald_helmet", 15),
    entry("orespawn:emerald_boots", 15),
    entry("orespawn:moth_scale", 15, 2, 8),
    entry("orespawn:mothscale_chestplate", 15),
    entry("orespawn:mothscale_leggings", 15),
    entry("orespawn:mothscale_helmet", 15),
    entry("orespawn:mothscale_boots", 15),
    entry("orespawn:lava_eel", 15, 2, 8),
    entry("orespawn:lavaeel_chestplate", 15),
    entry("orespawn:lavaeel_leggings", 15),
    entry("orespawn:lavaeel_helmet", 15),
    entry("orespawn:lavaeel_boots", 15),
    entry("orespawn:experience_chestplate", 15),
    entry("orespawn:experience_leggings", 15),
    entry("orespawn:experience_helmet", 15),
    entry("orespawn:experience_boots", 15),
    entry("orespawn:experience_sword", 15),
    # Original used custom egg items for vanilla mobs; 1.21.1 has real spawn eggs.
    egg("wither_skeleton", ns='minecraft'),
    egg("ender_dragon", ns='minecraft'),
    egg("snow_golem", ns='minecraft'),
    egg("iron_golem", ns='minecraft'),
    egg("wither", ns='minecraft'),
    egg("red_cow"), egg("gold_cow"), egg("enchanted_apple_cow"),
    egg("mothra"), egg("alosaurus"), egg("cryolophosaurus"), egg("camarasaurus"),
    egg("velocity_raptor"), egg("hydrolisc"), egg("basilisk"), egg("dragonfly"),
    egg("emperor_scorpion"), egg("scorpion"), egg("cave_fisher"), egg("spyro"),
    egg("baryonyx"), egg("cockateil"), egg("gamma_metroid"), egg("kyuubi"),
    egg("alien"), egg("attack_squid"), egg("water_dragon"), egg("cephadrome"),
    egg("kraken"), egg("lizard"), egg("dragon"), egg("bee"),
    egg("trooper_bug"), egg("spit_bug"), egg("stink_bug"), egg("ostrich"),
    egg("gazelle"), egg("chipmunk"), egg("creeping_horror"), egg("terrible_terror"),
    egg("cliff_racer"), egg("triffid"), egg("pitch_black"), egg("lurking_terror"),
    egg("worm_small"), egg("worm_medium"), egg("worm_large"), egg("cassowary"),
    egg("molenoid"), egg("sea_monster"), egg("sea_viper"), egg("cater_killer"),
    egg("leon"), egg("hammerhead"), egg("rubber_ducky"), egg("nastysaurus"),
    egg("pointysaurus"), egg("brutalfly"), egg("cricket"), egg("frog"),
    egg("jeffery"), egg("spider_driver"), egg("crab"),
    entry("orespawn:cage_empty", 20, 3, 10),            # CageEmpty 3-10 w20
]

# --- chests/ruby_dungeon (orig RubyBirdDungeon.java:18, rolls 4-10 :80) ---
ruby = [
    entry("orespawn:cage_empty", 20, 3, 10),
    entry("orespawn:ruby", 15, 2, 8),
    entry("orespawn:cooked_bacon", 20, 6, 12),
    entry("orespawn:butter_candy", 20, 6, 12),
    entry("orespawn:ruby_pickaxe", 15),
    entry("orespawn:ruby_shovel", 15),
    entry("orespawn:ruby_hoe", 15),
    entry("orespawn:ruby_axe", 15),
    entry("orespawn:ruby_sword", 15),
    entry("orespawn:ruby_chestplate", 15),
    entry("orespawn:ruby_leggings", 15),
    entry("orespawn:ruby_helmet", 15),
    entry("orespawn:ruby_boots", 15),
    entry("orespawn:thunder_staff", 5),
]

# --- chests/crystal_chest[_maze] (orig Trees.java:19, 82 entries) ---
crystal = [
    entry("orespawn:crystal_termite_block", 10, 1, 5),
    entry("orespawn:crystal_flower_red", 10, 1, 10),
    entry("orespawn:crystal_flower_blue", 10, 1, 10),
    entry("orespawn:crystal_flower_green", 10, 1, 10),
    entry("orespawn:crystal_flower_yellow", 10, 1, 10),
    entry("orespawn:crystal_planks", 10, 1, 10),
    entry("orespawn:crystal_workbench", 10),
    entry("orespawn:crystal_furnace", 10),
    entry("orespawn:block_tigers_eye", 5, 1, 10),       # MyTigersEyeBlock w5
    entry("orespawn:crystal_stone", 10, 1, 10),
    entry("orespawn:crystal_rat", 10, 1, 10),
    entry("orespawn:crystal_fairy", 10, 1, 10),
    entry("orespawn:crystal_coal", 10, 1, 10),
    entry("orespawn:crystal_grass", 10, 1, 10),
    entry("orespawn:crystal_crystal", 10, 1, 10),
    entry("orespawn:crystal_torch", 10, 1, 10),
    entry("orespawn:crystal_leaves", 10, 1, 10),
    entry("orespawn:crystal_leaves_2", 10, 1, 10),
    entry("orespawn:crystal_leaves_3", 10, 1, 10),
    entry("orespawn:crystal_tree_log", 10, 1, 10),
    entry("orespawn:tigers_eye_ore", 5, 1, 10),         # TigersEye (ore block) w5
    entry("orespawn:crystal_wood_sword", 10),
    entry("orespawn:crystal_wood_axe", 10),
    entry("orespawn:crystal_wood_shovel", 10),
    entry("orespawn:crystal_wood_pickaxe", 10),
    entry("orespawn:crystal_wood_hoe", 10),
    entry("orespawn:crystal_pink_sword", 10),
    entry("orespawn:crystal_pink_axe", 10),
    entry("orespawn:crystal_pink_shovel", 10),
    entry("orespawn:crystal_pink_pickaxe", 10),
    entry("orespawn:crystal_pink_hoe", 10),
    entry("orespawn:tigers_eye_sword", 5),
    entry("orespawn:tigers_eye_axe", 5),
    entry("orespawn:tigers_eye_shovel", 5),
    entry("orespawn:tigers_eye_pickaxe", 5),
    entry("orespawn:tigers_eye_hoe", 5),
    entry("orespawn:crystal_stone_sword", 10),
    entry("orespawn:crystal_stone_axe", 10),
    entry("orespawn:crystal_stone_shovel", 10),
    entry("orespawn:crystal_stone_pickaxe", 10),
    entry("orespawn:crystal_stone_hoe", 10),
    entry("orespawn:tigers_eye_ingot", 5, 1, 5),
    entry("orespawn:crystal_pink_ingot", 10, 1, 5),
    entry("orespawn:crystal_apple", 10, 1, 5),
    entry("orespawn:peacock_feather", 10, 1, 5),
    entry("orespawn:cooked_peacock", 20, 1, 10),        # MyPeacock (cooked) w20
    entry("orespawn:raw_peacock", 20, 1, 10),
    entry("orespawn:rice", 20, 1, 10),
    entry("orespawn:quinoa", 20, 1, 10),
    entry("orespawn:pink_helmet", 10),
    entry("orespawn:pink_chestplate", 10),
    entry("orespawn:pink_leggings", 10),
    entry("orespawn:pink_boots", 10),
    entry("orespawn:tigerseye_helmet", 5),
    entry("orespawn:tigerseye_chestplate", 5),
    entry("orespawn:tigerseye_leggings", 5),
    entry("orespawn:tigerseye_boots", 5),
    entry("orespawn:peacock_helmet", 10),
    entry("orespawn:peacock_chestplate", 10),
    entry("orespawn:peacock_leggings", 10),
    entry("orespawn:peacock_boots", 10),
    egg("rotator", 10, 1, 5), egg("vortex", 10, 1, 5), egg("peacock", 10, 1, 5),
    egg("dungeon_beast", 10, 1, 5), egg("fairy", 10, 1, 5), egg("rat", 10, 1, 5),
    egg("flounder", 10, 1, 5), egg("whale", 10, 1, 5), egg("irukandji", 10, 1, 5),
    egg("skate", 10, 1, 5), egg("urchin", 10, 1, 5), egg("ghost", 10, 1, 5),
    egg("ghost_skelly", 10, 1, 5),
    entry("orespawn:skate_bow", 2),
    entry("orespawn:irukandji_arrow", 2, 5, 10),
    entry("orespawn:dead_irukandji", 5, 2, 8),          # MyIrukandji 2-8 w5
    entry("orespawn:ultimate_bow", 2),
    entry("orespawn:ultimate_sword", 2),
    entry("minecraft:iron_ingot", 10, 1, 4),
    entry("minecraft:oak_log", 10, 1, 4),               # Blocks.log meta 0
    entry("minecraft:golden_apple", 2, 1, 5),
]

# --- chests/battle_tower_* (orig GenericDungeon.java:32-36) ---
bt_rat = [
    entry("minecraft:cooked_porkchop", 35, 3, 10),      # field_151157_am
    entry("minecraft:beef", 35, 3, 10),                 # field_151082_bd (raw beef)
    entry("minecraft:cooked_chicken", 35, 3, 10),       # field_151077_bg
    entry("minecraft:cooked_cod", 35, 3, 10),           # field_151101_aQ (cooked_fished)
    entry("orespawn:blt_sandwich", 35, 4, 10),
    entry("orespawn:salad", 35, 4, 10),
    entry("orespawn:corn_dog", 35, 4, 10),
]
bt_beast = [
    entry("minecraft:ink_sac", 25, 6, 16),              # field_151100_aR meta 0 (dye/ink sac)
    entry("orespawn:squid_zooka", 25),
    entry("minecraft:gold_nugget", 15, 5, 15),
    entry("minecraft:rotten_flesh", 25, 6, 16),
]
bt_urchin = [
    entry("orespawn:pink_helmet", 10),
    entry("orespawn:pink_chestplate", 10),
    entry("orespawn:pink_leggings", 10),
    entry("orespawn:pink_boots", 10),
    entry("orespawn:fairy_sword", 15),
]
bt_rotator = [
    entry("orespawn:tigerseye_helmet", 10),
    entry("orespawn:tigerseye_chestplate", 10),
    entry("orespawn:tigerseye_leggings", 10),
    entry("orespawn:tigerseye_boots", 10),
    entry("orespawn:rat_sword", 15),
]
bt_vortex = [
    entry("orespawn:crystal_coal", 10, 6, 10),          # listed twice in orig
    entry("orespawn:crystal_coal", 10, 6, 10),
    entry("orespawn:tigers_eye_sword", 10),
    entry("orespawn:block_tigers_eye", 15, 4, 8),
    entry("orespawn:poison_sword", 15),
]

# --- chests/beehive (orig GenericDungeon.java:55, rolls 1-5 :869) ---
beehive = [
    entry("minecraft:sugar", 15, 2, 8),                 # field_151102_aT
    entry("minecraft:dandelion", 15, 4, 8),             # Blocks.yellow_flower
    entry("minecraft:gold_nugget", 15, 5, 15),
    entry("minecraft:paper", 15, 2, 8),                 # field_151121_aF
    entry("orespawn:fairy_sword", 10),
    entry("orespawn:pink_helmet", 10),
    entry("orespawn:pink_chestplate", 10),
    entry("orespawn:pink_leggings", 10),
    entry("orespawn:pink_boots", 10),
    entry("orespawn:butter_candy", 15, 2, 8),
    entry("orespawn:experience_catcher", 10, 4, 10),
    egg("bee", 15, 2, 8),
]

TABLES = {
    "generic_dungeon": table(5, 11, generic),           # 5+nextInt(7), GenericDungeon.java:183
    "ruby_dungeon": table(4, 10, ruby),                 # 4+nextInt(7), RubyBirdDungeon.java:80
    "crystal_chest": table(1, 5, crystal),              # 1+nextInt(5), Trees.java:488/612
    "crystal_chest_maze": table(1, 3, crystal),         # 1+nextInt(3), OreSpawnWorld.java:1762
    "battle_tower_rat": table(5, 9, bt_rat),            # 5+nextInt(5), GenericDungeon.java:4890
    "battle_tower_dungeon_beast": table(5, 9, bt_beast),
    "battle_tower_urchin": table(5, 9, bt_urchin),
    "battle_tower_rotator": table(5, 9, bt_rotator),
    "battle_tower_vortex": table(6, 11, bt_vortex),     # 6+nextInt(6), GenericDungeon.java:4958/6256
    "beehive": table(1, 5, beehive),                    # 1+nextInt(5), GenericDungeon.java:869
}

os.makedirs(OUT, exist_ok=True)
for name, data in TABLES.items():
    path = os.path.join(OUT, name + '.json')
    with open(path, 'w', encoding='utf-8', newline='\n') as f:
        json.dump(data, f, indent=2)
        f.write('\n')
    print('wrote', path)
