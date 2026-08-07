"""D5 (WGEN-005 + ITEM-062): generate the SpawnOres pool artifacts.

Restores the original ~105-type spawn-ore system per
phase_d_reports/d5_extraction/spawn_ores_spec.md:

- assets (blockstate / block model / item model) + block loot table for every
  spawn-ore block the port lacks (106 new; kraken/dragon + the 11 crystal
  egg-ores already exist),
- lang entries using the ORIGINAL "Ancient Dried <Mob> Spawn Egg" display
  strings (parsed straight from orig OreSpawnMain.java's addNameForObject
  lines, OSM:2665-3021 + :2884-2899), including renaming the 13 existing
  blocks whose Phase C names deviated (spec §7.4),
- all 116 water-bucket conversion recipes + the 3 nine-part combines
  (OSM:2665-3021; shapeless water bucket + block -> 1 egg; bucket remainder
  is automatic in modern crafting),
- Java fragments (scratch output, inserted manually): ModBlocks/ModItems
  registrations, creative-tab accepts, and the exact c0-c97 / R0-R6 pool
  arrays for SpawnOresPoolFeature (order verified against BOTH
  OreSpawnWorld.java:371-801 and ChunkOreGenerator.java:37-467).

Texture keys are parsed from the ctor lines (OSM:6236-6360); the one
uppercase key oreMOTHRA is lowercased to match the port's oremothra.png
(1.21 lowercase resource rule, spec §7).

Egg outputs resolve against ModItems.java registrations and FAIL LOUDLY on
any miss. Mapping decisions baked in (all recorded in the D5 report):
CriminalEgg -> band_p_spawn_egg (WGEN-017: Criminal = BandP);
EnchantedCowEgg -> enchanted_apple_cow_spawn_egg (the Phase 14 consolidation
target of the original EnchantedCow); vanilla-mob eggs use modern vanilla
ids (all four ender_dragon/iron_golem/snow_golem/wither eggs exist in
1.21.1 — verified against the client jar's lang index).
"""
import json
import re
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
OSM = ROOT / "reference_1_7_10_source/sources/danger/orespawn/OreSpawnMain.java"
MODITEMS = ROOT / "src/main/java/danger/orespawn/ModItems.java"
RES = ROOT / "src/main/resources"
SCRATCH = ROOT / "phase_d_reports/d5_extraction/tmp"

# ---------------------------------------------------------------------------
# Master table: original field suffix -> (port block registry name, egg output
# item id or None, pool slot ('cN' / 'rN' / None), has water recipe).
# Row order = OSM declaration order :534-652 (spec §2). "existing" rows map to
# the port's already-registered block names.
# ---------------------------------------------------------------------------
ROWS = [
    # (orig field w/o My…SpawnBlock, port block name, egg id, slot, water?)
    ("Spider", "spider_spawn_block", "minecraft:spider_spawn_egg", "c0", True),
    ("Bat", "bat_spawn_block", "minecraft:bat_spawn_egg", "c1", True),
    ("Cow", "cow_spawn_block", "minecraft:cow_spawn_egg", "c2", True),
    ("Pig", "pig_spawn_block", "minecraft:pig_spawn_egg", "c3", True),
    ("Squid", "squid_spawn_block", "minecraft:squid_spawn_egg", "c4", True),
    ("Chicken", "chicken_spawn_block", "minecraft:chicken_spawn_egg", "c5", True),
    ("Creeper", "creeper_spawn_block", "minecraft:creeper_spawn_egg", "c6", True),
    ("Skeleton", "skeleton_spawn_block", "minecraft:skeleton_spawn_egg", "c7", True),
    ("Zombie", "zombie_spawn_block", "minecraft:zombie_spawn_egg", "c8", True),
    ("Slime", "slime_spawn_block", "minecraft:slime_spawn_egg", "c9", True),
    ("Ghast", "ghast_spawn_block", "minecraft:ghast_spawn_egg", "c10", True),
    ("ZombiePigman", "zombified_piglin_spawn_block", "minecraft:zombified_piglin_spawn_egg", "c11", True),
    ("Enderman", "enderman_spawn_block", "minecraft:enderman_spawn_egg", "c12", True),
    ("CaveSpider", "cave_spider_spawn_block", "minecraft:cave_spider_spawn_egg", "c13", True),
    ("Silverfish", "silverfish_spawn_block", "minecraft:silverfish_spawn_egg", "c14", True),
    ("MagmaCube", "magma_cube_spawn_block", "minecraft:magma_cube_spawn_egg", "c15", True),
    ("Witch", "witch_spawn_block", "minecraft:witch_spawn_egg", "c16", True),
    ("Sheep", "sheep_spawn_block", "minecraft:sheep_spawn_egg", "c17", True),
    ("Wolf", "wolf_spawn_block", "minecraft:wolf_spawn_egg", "c18", True),
    ("Mooshroom", "mooshroom_spawn_block", "minecraft:mooshroom_spawn_egg", "c19", True),
    ("Ocelot", "ocelot_spawn_block", "minecraft:ocelot_spawn_egg", "c20", True),
    ("Blaze", "blaze_spawn_block", "minecraft:blaze_spawn_egg", "c21", True),
    ("WitherSkeleton", "wither_skeleton_spawn_block", "minecraft:wither_skeleton_spawn_egg", "c22", True),
    ("EnderDragon", "ender_dragon_spawn_block", "minecraft:ender_dragon_spawn_egg", "c23", True),
    ("SnowGolem", "snow_golem_spawn_block", "minecraft:snow_golem_spawn_egg", "c24", True),
    ("IronGolem", "iron_golem_spawn_block", "minecraft:iron_golem_spawn_egg", "c25", True),
    ("WitherBoss", "wither_spawn_block", "minecraft:wither_spawn_egg", "c26", True),
    ("Girlfriend", "girlfriend_spawn_block", "orespawn:girlfriend_spawn_egg", "c27", True),
    ("Boyfriend", "boyfriend_spawn_block", "orespawn:boyfriend_spawn_egg", "c85", True),
    ("RedCow", "red_cow_spawn_block", "orespawn:red_cow_spawn_egg", "c28", True),
    ("CrystalCow", "crystal_cow_spawn_block", "orespawn:crystal_cow_spawn_egg", None, True),
    ("Villager", "villager_spawn_block", "minecraft:villager_spawn_egg", "c95", True),
    ("GoldCow", "gold_cow_spawn_block", "orespawn:gold_cow_spawn_egg", "c29", True),
    ("EnchantedCow", "enchanted_apple_cow_spawn_block", "orespawn:enchanted_apple_cow_spawn_egg", "c30", True),
    ("MOTHRA", "mothra_spawn_block", "orespawn:mothra_spawn_egg", "c31", True),
    ("Alo", "alosaurus_spawn_block", "orespawn:alosaurus_spawn_egg", "c32", True),
    ("Cryo", "cryolophosaurus_spawn_block", "orespawn:cryolophosaurus_spawn_egg", "c33", True),
    ("Cama", "camarasaurus_spawn_block", "orespawn:camarasaurus_spawn_egg", "c34", True),
    ("Velo", "velocity_raptor_spawn_block", "orespawn:velocity_raptor_spawn_egg", "c35", True),
    ("Hydro", "hydrolisc_spawn_block", "orespawn:hydrolisc_spawn_egg", "c36", True),
    ("Basil", "basilisk_spawn_block", "orespawn:basilisk_spawn_egg", "c37", True),
    ("Dragonfly", "dragonfly_spawn_block", "orespawn:dragonfly_spawn_egg", "c38", True),
    ("EmperorScorpion", "emperor_scorpion_spawn_block", "orespawn:emperor_scorpion_spawn_egg", "c39", True),
    ("Scorpion", "scorpion_spawn_block", "orespawn:scorpion_spawn_egg", "c40", True),
    ("CaveFisher", "cave_fisher_spawn_block", "orespawn:cave_fisher_spawn_egg", "c41", True),
    ("Spyro", "spyro_spawn_block", "orespawn:spyro_spawn_egg", "c42", True),
    ("Baryonyx", "baryonyx_spawn_block", "orespawn:baryonyx_spawn_egg", "c43", True),
    ("GammaMetroid", "gamma_metroid_spawn_block", "orespawn:gamma_metroid_spawn_egg", "c44", True),
    ("Cockateil", "cockateil_spawn_block", "orespawn:cockateil_spawn_egg", "c45", True),
    ("Kyuubi", "kyuubi_spawn_block", "orespawn:kyuubi_spawn_egg", "c46", True),
    ("Alien", "alien_spawn_block", "orespawn:alien_spawn_egg", "c47", True),
    ("AttackSquid", "attack_squid_spawn_block", "orespawn:attack_squid_spawn_egg", "c48", True),
    ("WaterDragon", "water_dragon_spawn_block", "orespawn:water_dragon_spawn_egg", "c49", True),
    ("Kraken", "kraken_spawn_block", "orespawn:kraken_spawn_egg", "c50", True),          # existing block
    ("Lizard", "lizard_spawn_block", "orespawn:lizard_spawn_egg", "c51", True),
    ("Cephadrome", "cephadrome_spawn_block", "orespawn:cephadrome_spawn_egg", "c52", True),
    ("Dragon", "dragon_spawn_block", "orespawn:dragon_spawn_egg", "c53", True),          # existing block
    ("Bee", "bee_spawn_block", "orespawn:bee_spawn_egg", "c54", True),
    ("Horse", "horse_spawn_block", "minecraft:horse_spawn_egg", "c55", True),
    ("TrooperBug", "trooper_bug_spawn_block", "orespawn:trooper_bug_spawn_egg", "c56", True),
    ("SpitBug", "spit_bug_spawn_block", "orespawn:spit_bug_spawn_egg", "c57", True),
    ("StinkBug", "stink_bug_spawn_block", "orespawn:stink_bug_spawn_egg", "c58", True),
    ("Ostrich", "ostrich_spawn_block", "orespawn:ostrich_spawn_egg", "c59", True),
    ("Gazelle", "gazelle_spawn_block", "orespawn:gazelle_spawn_egg", "c60", True),
    ("Chipmunk", "chipmunk_spawn_block", "orespawn:chipmunk_spawn_egg", "c61", True),
    ("CreepingHorror", "creeping_horror_spawn_block", "orespawn:creeping_horror_spawn_egg", "c62", True),
    ("TerribleTerror", "terrible_terror_spawn_block", "orespawn:terrible_terror_spawn_egg", "c63", True),
    ("CliffRacer", "cliff_racer_spawn_block", "orespawn:cliff_racer_spawn_egg", "c64", True),
    ("Triffid", "triffid_spawn_block", "orespawn:triffid_spawn_egg", "c65", True),
    ("PitchBlack", "pitch_black_spawn_block", "orespawn:pitch_black_spawn_egg", "c66", True),
    ("LurkingTerror", "lurking_terror_spawn_block", "orespawn:lurking_terror_spawn_egg", "c67", True),
    ("GodzillaPart", "godzilla_part_spawn_block", None, "c68", False),
    ("Godzilla", "godzilla_spawn_block", "orespawn:godzilla_spawn_egg", "c69", True),
    ("TheKingPart", "the_king_part_spawn_block", None, "c86", False),
    ("TheQueenPart", "the_queen_part_spawn_block", None, "c97", False),
    ("TheKing", "the_king_spawn_block", "orespawn:the_king_spawn_egg", None, True),
    ("TheQueen", "the_queen_spawn_block", "orespawn:the_queen_spawn_egg", None, True),
    ("SmallWorm", "worm_small_spawn_block", "orespawn:worm_small_spawn_egg", "c70", True),
    ("MediumWorm", "worm_medium_spawn_block", "orespawn:worm_medium_spawn_egg", "c71", True),
    ("LargeWorm", "worm_large_spawn_block", "orespawn:worm_large_spawn_egg", "c72", True),
    ("Cassowary", "cassowary_spawn_block", "orespawn:cassowary_spawn_egg", "c73", True),
    ("CloudShark", "cloud_shark_spawn_block", "orespawn:cloud_shark_spawn_egg", "c74", True),
    ("GoldFish", "gold_fish_spawn_block", "orespawn:gold_fish_spawn_egg", "c75", True),
    ("LeafMonster", "leaf_monster_spawn_block", "orespawn:leaf_monster_spawn_egg", "c76", True),
    ("Tshirt", "tshirt_spawn_block", "orespawn:tshirt_spawn_egg", "c77", True),
    ("EnderKnight", "ender_knight_spawn_block", "orespawn:ender_knight_spawn_egg", "c78", True),
    ("EnderReaper", "ender_reaper_spawn_block", "orespawn:ender_reaper_spawn_egg", "c79", True),
    ("Beaver", "beaver_spawn_block", "orespawn:beaver_spawn_egg", "c80", True),
    ("Urchin", "ore_urchin", "orespawn:urchin_spawn_egg", None, True),                   # existing (Crystal pool)
    ("Flounder", "ore_flounder", "orespawn:flounder_spawn_egg", None, True),             # existing
    ("Skate", "ore_skate", "orespawn:skate_spawn_egg", None, True),                      # existing
    ("Rotator", "ore_rotator", "orespawn:rotator_spawn_egg", None, True),                # existing
    ("Peacock", "ore_peacock", "orespawn:peacock_spawn_egg", None, True),                # existing
    ("Fairy", "ore_fairy", "orespawn:fairy_spawn_egg", None, True),                      # existing
    ("DungeonBeast", "ore_dungeon_beast", "orespawn:dungeon_beast_spawn_egg", None, True),  # existing
    ("Vortex", "ore_vortex", "orespawn:vortex_spawn_egg", None, True),                   # existing
    ("Rat", "ore_rat", "orespawn:rat_spawn_egg", None, True),                            # existing
    ("Whale", "ore_whale", "orespawn:whale_spawn_egg", None, True),                      # existing
    ("Irukandji", "ore_irukandji", "orespawn:irukandji_spawn_egg", None, True),          # existing
    ("TRex", "trex_spawn_block", "orespawn:trex_spawn_egg", "c81", True),
    ("Hercules", "hercules_beetle_spawn_block", "orespawn:hercules_beetle_spawn_egg", "c82", True),
    ("Mantis", "mantis_spawn_block", "orespawn:mantis_spawn_egg", "c83", True),
    ("Stinky", "stinky_spawn_block", "orespawn:stinky_spawn_egg", "c84", True),
    ("EasterBunny", "easter_bunny_spawn_block", "orespawn:easter_bunny_spawn_egg", "c87", True),
    ("CaterKiller", "cater_killer_spawn_block", "orespawn:cater_killer_spawn_egg", "c88", True),
    ("Molenoid", "molenoid_spawn_block", "orespawn:molenoid_spawn_egg", "c89", True),
    ("SeaMonster", "sea_monster_spawn_block", "orespawn:sea_monster_spawn_egg", "c90", True),
    ("SeaViper", "sea_viper_spawn_block", "orespawn:sea_viper_spawn_egg", "c91", True),
    ("Leon", "leon_spawn_block", "orespawn:leon_spawn_egg", "c92", True),
    ("Hammerhead", "hammerhead_spawn_block", "orespawn:hammerhead_spawn_egg", "c93", True),
    ("RubberDucky", "rubber_ducky_spawn_block", "orespawn:rubber_ducky_spawn_egg", "c94", True),
    ("Criminal", "band_p_spawn_block", "orespawn:band_p_spawn_egg", "c96", True),
    ("Brutalfly", "brutalfly_spawn_block", "orespawn:brutalfly_spawn_egg", "r0", True),
    ("Nastysaurus", "nastysaurus_spawn_block", "orespawn:nastysaurus_spawn_egg", "r1", True),
    ("Pointysaurus", "pointysaurus_spawn_block", "orespawn:pointysaurus_spawn_egg", "r2", True),
    ("Cricket", "cricket_spawn_block", "orespawn:cricket_spawn_egg", "r3", True),
    ("Frog", "frog_spawn_block", "orespawn:frog_spawn_egg", "r4", True),
    ("SpiderDriver", "spider_driver_spawn_block", "orespawn:spider_driver_spawn_egg", "r5", True),
    ("Crab", "crab_spawn_block", "orespawn:crab_spawn_egg", "r6", True),
]

EXISTING = {
    "kraken_spawn_block", "dragon_spawn_block",
    "ore_urchin", "ore_flounder", "ore_skate", "ore_rotator", "ore_peacock",
    "ore_fairy", "ore_dungeon_beast", "ore_vortex", "ore_rat", "ore_whale",
    "ore_irukandji",
}

# The three nine-part combines (OSM:2886/2892/2898).
PART_COMBINES = [
    ("godzilla_spawn_block", "godzilla_part_spawn_block"),
    ("the_king_spawn_block", "the_king_part_spawn_block"),
    ("the_queen_spawn_block", "the_queen_part_spawn_block"),
]

osm_text = OSM.read_text(encoding="utf-8", errors="replace")

# field -> texture key (ctor lines, OSM:6236-6360)
tex_by_field = dict(re.findall(
    r"(My\w+SpawnBlock)\s*=\s*\(OreGenericEgg\)new OreGenericEgg\([^)]*\)\.func_149663_c\(\"([^\"]+)\"\)",
    osm_text))
# field -> en_US display name
name_by_field = dict(re.findall(
    r"addNameForObject\(\(Object\)(My\w+SpawnBlock),\s*\"en_US\",\s*\"([^\"]+)\"\)",
    osm_text))

# ModItems registry names, for egg-id validation.
moditems_text = MODITEMS.read_text(encoding="utf-8", errors="replace")
port_item_names = set(re.findall(r"ITEMS\.register(?:SimpleItem|SimpleBlockItem)?\s*\(\s*\"([a-z0-9_]+)\"", moditems_text))

errors = []
resolved = []
for field_suffix, block_name, egg, slot, water in ROWS:
    field = f"My{field_suffix}SpawnBlock"
    tex = tex_by_field.get(field)
    disp = name_by_field.get(field)
    if tex is None:
        errors.append(f"no ctor/texture parsed for {field}")
    if disp is None:
        errors.append(f"no display name parsed for {field}")
    if egg and egg.startswith("orespawn:") and egg.split(":")[1] not in port_item_names:
        errors.append(f"egg item not registered in ModItems: {egg} (for {block_name})")
    resolved.append((field, block_name, tex.lower() if tex else None, disp, egg, slot, water))
if errors:
    raise SystemExit("FATAL:\n  " + "\n  ".join(errors))
assert len(resolved) == 119, len(resolved)
common = [r for r in resolved if r[5] and r[5].startswith("c")]
rare = [r for r in resolved if r[5] and r[5].startswith("r")]
assert len(common) == 98 and len(rare) == 7, (len(common), len(rare))
assert sorted(int(r[5][1:]) for r in common) == list(range(98))
assert sorted(int(r[5][1:]) for r in rare) == list(range(7))

# --- Emit per-block assets + loot for NEW blocks -----------------------------
new_rows = [r for r in resolved if r[1] not in EXISTING]
assert len(new_rows) == 106, len(new_rows)
for field, name, tex, disp, egg, slot, water in new_rows:
    (RES / f"assets/orespawn/blockstates/{name}.json").write_text(json.dumps(
        {"variants": {"": {"model": f"orespawn:block/{name}"}}}, indent=2) + "\n")
    (RES / f"assets/orespawn/models/block/{name}.json").write_text(json.dumps(
        {"parent": "minecraft:block/cube_all",
         "textures": {"all": f"orespawn:blocks/{tex}"}}, indent=2) + "\n")
    (RES / f"assets/orespawn/models/item/{name}.json").write_text(json.dumps(
        {"parent": f"orespawn:block/{name}"}, indent=2) + "\n")
    (RES / f"data/orespawn/loot_table/blocks/{name}.json").write_text(json.dumps(
        {"type": "minecraft:block",
         "pools": [{"rolls": 1,
                    "entries": [{"type": "minecraft:item", "name": f"orespawn:{name}"}],
                    "conditions": [{"condition": "minecraft:survives_explosion"}]}]},
        indent=2) + "\n")

# --- Lang merge --------------------------------------------------------------
lang_path = RES / "assets/orespawn/lang/en_us.json"
lang = json.loads(lang_path.read_text(encoding="utf-8"))
renamed = []
for field, name, tex, disp, egg, slot, water in resolved:
    key = f"block.orespawn.{name}"
    if lang.get(key) != disp:
        if key in lang:
            renamed.append(f"{key}: '{lang[key]}' -> '{disp}'")
        lang[key] = disp
lang_path.write_text(json.dumps(lang, indent=2, ensure_ascii=False) + "\n", encoding="utf-8")

# --- Recipes -----------------------------------------------------------------
recipe_dir = RES / "data/orespawn/recipe"
water_count = 0
for field, name, tex, disp, egg, slot, water in resolved:
    if not water:
        continue
    assert egg, name
    out_file = recipe_dir / f"{egg.split(':')[1]}_from_{name}.json"
    out_file.write_text(json.dumps({
        "type": "minecraft:crafting_shapeless",
        "ingredients": [{"item": "minecraft:water_bucket"}, {"item": f"orespawn:{name}"}],
        "result": {"id": egg, "count": 1},
    }, indent=2) + "\n")
    water_count += 1
assert water_count == 116, water_count
for full, part in PART_COMBINES:
    (recipe_dir / f"{full}_from_parts.json").write_text(json.dumps({
        "type": "minecraft:crafting_shapeless",
        "ingredients": [{"item": f"orespawn:{part}"}] * 9,
        "result": {"id": f"orespawn:{full}", "count": 1},
    }, indent=2) + "\n")

# --- Java fragments ----------------------------------------------------------
SCRATCH.mkdir(exist_ok=True)


def const(name: str) -> str:
    return name.upper()


with open(SCRATCH / "d5_modblocks_fragment.java", "w", encoding="utf-8") as fh:
    for field, name, tex, disp, egg, slot, water in new_rows:
        fh.write(f"    public static final DeferredBlock<Block> {const(name)} = BLOCKS.register(\"{name}\",\n"
                 f"            () -> new OreGenericEgg(BlockBehaviour.Properties.of().strength(0.5f, 1.0f).sound(SoundType.GRAVEL)));\n")
with open(SCRATCH / "d5_moditems_fragment.java", "w", encoding="utf-8") as fh:
    for field, name, tex, disp, egg, slot, water in new_rows:
        fh.write(f"    public static final DeferredItem<BlockItem> {const(name)}_ITEM = ITEMS.registerSimpleBlockItem(\"{name}\", ModBlocks.{const(name)});\n")
with open(SCRATCH / "d5_tabs_fragment.java", "w", encoding="utf-8") as fh:
    for field, name, tex, disp, egg, slot, water in new_rows:
        fh.write(f"                        output.accept(ModItems.{const(name)}_ITEM.get());\n")
with open(SCRATCH / "d5_pool_arrays_fragment.java", "w", encoding="utf-8") as fh:
    fh.write("    // COMMON pool — exact nextInt(98) switch order, orig OreSpawnWorld.java:406-801\n"
             "    // == ChunkOreGenerator.java:72-467 (verified identical; spec §2/§3).\n")
    ordered = sorted(common, key=lambda r: int(r[5][1:]))
    for r in ordered:
        fh.write(f"            ModBlocks.{const(r[1])},   // {r[5]} — orig {r[0]}\n")
    fh.write("    // RARE pool — nextInt(7), orig OreSpawnWorld.java:371-402 == ChunkOreGenerator.java:37-68.\n")
    for r in sorted(rare, key=lambda r: int(r[5][1:])):
        fh.write(f"            ModBlocks.{const(r[1])},   // {r[5]} — orig {r[0]}\n")

print(f"new blocks: {len(new_rows)}; water recipes: {water_count}; part combines: {len(PART_COMBINES)}")
print("lang renames of existing keys:")
for r in renamed:
    print("  ", r)
