"""D5 (WGEN-053): generate the five Challenge Tower chest loot tables.

Transcribes GenericDungeon.java's level1ContentsList..level5ContentsList
(GD:57-61) into data/orespawn/loot_table/chests/challenge_tower_level{1-5}.json.
Entry data (item constant, min, max, weight) comes from the verified spec
phase_d_reports/d5_extraction/enormous_castle_spec.md SS8.1-8.5; port registry
names are resolved from ModItems.java/ModBlocks.java registrations so a typo'd
constant fails loudly instead of silently emitting a dead item id.

Fill formula: 5 + nextInt(7) weighted pulls per chest (GD:750) -> rolls
uniform 5..11. The original's random-slot collision overwrite is dropped
(documented approximation, same as every chest-list conversion since C6).

Mapping notes:
- "CriminalEgg" -> band_p_spawn_egg: 1.7.10 "Criminal" is the port's BandP
  (audit correction WGEN-017, Phase C7).
- The five vanilla-mob eggs (WitherSkeleton/EnderDragon/SnowGolem/IronGolem/
  WitherBoss) map to the modern vanilla spawn eggs (all exist since 1.20.5).
"""
import json
import re
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
MODITEMS = ROOT / "src/main/java/danger/orespawn/ModItems.java"
MODBLOCKS = ROOT / "src/main/java/danger/orespawn/ModBlocks.java"
OUT_DIR = ROOT / "src/main/resources/data/orespawn/loot_table/chests"

REG_RE = re.compile(
    r"(?:DeferredItem|DeferredBlock)<[^>]*(?:<[^>]*>)?[^>]*>\s+(\w+)\s*=\s*"
    r"(?:ITEMS|BLOCKS)\.(?:register|registerSimpleItem|registerSimpleBlockItem|registerBlock|registerSimpleBlock)\s*\(\s*\"([a-z0-9_]+)\"",
    re.S)


def load_registry(path: Path) -> dict[str, str]:
    return {m.group(1): m.group(2) for m in REG_RE.finditer(path.read_text(encoding="utf-8"))}


NAMES = load_registry(MODITEMS)
# Block items share the block's registry name (repellents are BlockItems).
for const, name in load_registry(MODBLOCKS).items():
    NAMES.setdefault(const, name)


def item_id(ref: str) -> str:
    if ref.startswith("minecraft:"):
        return ref
    if ref not in NAMES:
        raise SystemExit(f"UNRESOLVED constant: {ref}")
    return "orespawn:" + NAMES[ref]


def entry(ref, weight, mn=1, mx=1):
    e = {"type": "minecraft:item", "name": item_id(ref), "weight": weight}
    if (mn, mx) != (1, 1):
        e["functions"] = [{
            "function": "minecraft:set_count",
            "count": {"type": "minecraft:uniform", "min": float(mn), "max": float(mx)},
        }]
    return e


# ---- level1ContentsList (GD:57) — total weight 165 ----
LEVEL1 = [
    ("minecraft:emerald", 15, 2, 8),
    ("MINERS_DREAM", 15, 4, 8),
    ("EMERALD_PICKAXE", 15), ("EMERALD_SHOVEL", 15), ("EMERALD_HOE", 15),
    ("EMERALD_AXE", 15), ("EMERALD_SWORD", 15),
    ("EMERALD_CHESTPLATE", 15), ("EMERALD_LEGGINGS", 15),
    ("EMERALD_HELMET", 15), ("EMERALD_BOOTS_ARMOR", 15),
]

# ---- level2ContentsList (GD:58) — total weight 235 ----
# (the two identical experience_bottle entries mirror the original's
#  duplicate list elements)
LEVEL2 = [
    ("minecraft:experience_bottle", 15, 2, 8),
    ("minecraft:experience_bottle", 15, 2, 8),
    ("CREEPER_LAUNCHER", 15, 2, 10),
    ("PINK_HELMET", 10), ("PINK_CHESTPLATE", 10),
    ("PINK_LEGGINGS", 10), ("PINK_BOOTS", 10),
    ("FAIRY_SWORD", 15),
    ("EMERALD_PICKAXE", 15), ("EMERALD_SHOVEL", 15), ("EMERALD_HOE", 15),
    ("EMERALD_AXE", 15), ("EMERALD_SWORD", 15),
    ("EXPERIENCE_CHESTPLATE", 15), ("EXPERIENCE_LEGGINGS", 15),
    ("EXPERIENCE_HELMET", 15), ("EXPERIENCE_BOOTS", 15),
]

# ---- level3ContentsList (GD:59) — total weight 235 ----
LEVEL3 = [
    ("SQUID_ZOOKA", 15), ("RAT_SWORD", 15),
    ("AMETHYST_GEM", 15, 2, 8),
    ("minecraft:ink_sac", 15, 2, 8),   # field_151100_aR meta 0 = ink sac
    ("TIGERSEYE_HELMET", 10), ("TIGERSEYE_CHESTPLATE", 10),
    ("TIGERSEYE_LEGGINGS", 10), ("TIGERSEYE_BOOTS", 10),
    ("AMETHYST_PICKAXE", 15), ("AMETHYST_SHOVEL", 15), ("AMETHYST_HOE", 15),
    ("AMETHYST_AXE", 15), ("AMETHYST_SWORD", 15),
    ("AMETHYST_CHESTPLATE", 15), ("AMETHYST_LEGGINGS", 15),
    ("AMETHYST_HELMET", 15), ("AMETHYST_BOOTS_ARMOR", 15),
]

# ---- level4ContentsList (GD:60) — total weight 255 ----
LEVEL4 = [
    ("RUBY", 15, 2, 8),
    ("MAGIC_APPLE", 15),
    ("RAY_GUN", 15),
    ("CREEPER_REPELLENT", 15, 4, 10),
    ("KRAKEN_REPELLENT", 15, 4, 10),
    ("EXPERIENCE_CATCHER", 15, 4, 10),
    ("ZOO_KEEPER", 15, 10, 16),
    ("RUBY_PICKAXE", 15), ("RUBY_SHOVEL", 15), ("RUBY_HOE", 15),
    ("RUBY_AXE", 15), ("RUBY_SWORD", 15),
    ("THUNDER_STAFF", 15),
    ("RUBY_CHESTPLATE", 15), ("RUBY_LEGGINGS", 15),
    ("RUBY_HELMET", 15), ("RUBY_BOOTS_ARMOR", 15),
]

# ---- level5ContentsList (GD:61) — 87 entries, total weight 1285 ----
# Declaration order preserved. Eggs are 1-4 x weight 15 unless noted.
L5_EGGS = [
    "minecraft:wither_skeleton_spawn_egg", "minecraft:ender_dragon_spawn_egg",
    "minecraft:snow_golem_spawn_egg", "minecraft:iron_golem_spawn_egg",
    "minecraft:wither_spawn_egg",
    "RED_COW_SPAWN_EGG", "GOLD_COW_SPAWN_EGG", "ENCHANTED_APPLE_COW_SPAWN_EGG",
    "MOTHRA_SPAWN_EGG", "ALOSAURUS_SPAWN_EGG", "CRYOLOPHOSAURUS_SPAWN_EGG",
    "CAMARASAURUS_SPAWN_EGG", "VELOCITY_RAPTOR_SPAWN_EGG", "HYDROLISC_SPAWN_EGG",
    "BASILISK_SPAWN_EGG", "DRAGONFLY_SPAWN_EGG", "EMPEROR_SCORPION_SPAWN_EGG",
    "SCORPION_SPAWN_EGG", "CAVE_FISHER_SPAWN_EGG", "SPYRO_SPAWN_EGG",
    "BARYONYX_SPAWN_EGG", "COCKATEIL_SPAWN_EGG", "GAMMA_METROID_SPAWN_EGG",
    "KYUUBI_SPAWN_EGG", "ALIEN_SPAWN_EGG", "ATTACK_SQUID_SPAWN_EGG",
    "WATER_DRAGON_SPAWN_EGG", "CEPHADROME_SPAWN_EGG", "KRAKEN_SPAWN_EGG",
    "LIZARD_SPAWN_EGG", "DRAGON_SPAWN_EGG", "BEE_SPAWN_EGG",
    "TROOPER_BUG_SPAWN_EGG", "SPIT_BUG_SPAWN_EGG", "STINK_BUG_SPAWN_EGG",
    "OSTRICH_SPAWN_EGG", "GAZELLE_SPAWN_EGG", "CHIPMUNK_SPAWN_EGG",
    "CREEPING_HORROR_SPAWN_EGG", "TERRIBLE_TERROR_SPAWN_EGG",
    "CLIFF_RACER_SPAWN_EGG", "TRIFFID_SPAWN_EGG", "PITCH_BLACK_SPAWN_EGG",
    "LURKING_TERROR_SPAWN_EGG", "WORM_SMALL_SPAWN_EGG", "WORM_MEDIUM_SPAWN_EGG",
    "WORM_LARGE_SPAWN_EGG", "TREX_SPAWN_EGG", "GODZILLA_SPAWN_EGG",
    "MANTIS_SPAWN_EGG", "HERCULES_BEETLE_SPAWN_EGG", "VORTEX_SPAWN_EGG",
    "RAT_SPAWN_EGG", "DUNGEON_BEAST_SPAWN_EGG", "FAIRY_SPAWN_EGG",
    "WHALE_SPAWN_EGG", "SKATE_SPAWN_EGG", "IRUKANDJI_SPAWN_EGG",
    "ROBOT_1_SPAWN_EGG", "ROBOT_2_SPAWN_EGG", "ROBOT_3_SPAWN_EGG",
    "ROBOT_4_SPAWN_EGG", "ROBOT_5_SPAWN_EGG",
    "BAND_P_SPAWN_EGG",  # CriminalEgg — 1.7.10 Criminal = BandP (WGEN-017)
    "COIN_SPAWN_EGG", "BOYFRIEND_SPAWN_EGG",
    ("EASTER_BUNNY_SPAWN_EGG", 5),  # weight 5, the list's one rare egg
    "MOLENOID_SPAWN_EGG", "SEA_MONSTER_SPAWN_EGG", "SEA_VIPER_SPAWN_EGG",
    "CATER_KILLER_SPAWN_EGG", "LEON_SPAWN_EGG", "HAMMERHEAD_SPAWN_EGG",
    "RUBBER_DUCKY_SPAWN_EGG", "NASTYSAURUS_SPAWN_EGG", "POINTYSAURUS_SPAWN_EGG",
    "BRUTALFLY_SPAWN_EGG", "CRICKET_SPAWN_EGG", "FROG_SPAWN_EGG",
    ("ANT_ROBOT_KIT", 10, 1, 1), ("SPIDER_ROBOT_KIT", 10, 1, 1),
    "JEFFERY_SPAWN_EGG", "SPIDER_DRIVER_SPAWN_EGG", "CRAB_SPAWN_EGG",
    "CASSOWARY_SPAWN_EGG",
]

LEVEL5 = [("NIGHTMARE_SWORD", 15), ("POISON_SWORD", 15)]
for spec in L5_EGGS:
    if isinstance(spec, tuple):
        if len(spec) == 4:
            LEVEL5.append(spec)                       # kits: 1/1, weight 10
        else:
            LEVEL5.append((spec[0], spec[1], 1, 4))   # EasterBunny: 1-4, weight 5
    else:
        LEVEL5.append((spec, 15, 1, 4))

EXPECTED = {1: (11, 165), 2: (17, 235), 3: (17, 235), 4: (17, 255), 5: (87, 1285)}

for n, entries in ((1, LEVEL1), (2, LEVEL2), (3, LEVEL3), (4, LEVEL4), (5, LEVEL5)):
    built = [entry(*e) for e in entries]
    count, total = len(built), sum(e["weight"] for e in built)
    assert (count, total) == EXPECTED[n], f"level{n}: {count} entries / weight {total}, expected {EXPECTED[n]}"
    table = {
        "type": "minecraft:chest",
        "pools": [{
            # GD:750 — 5 + nextInt(7) weighted pulls per chest.
            "rolls": {"type": "minecraft:uniform", "min": 5.0, "max": 11.0},
            "entries": built,
        }],
    }
    out = OUT_DIR / f"challenge_tower_level{n}.json"
    out.write_text(json.dumps(table, indent=2) + "\n", encoding="utf-8")
    print(f"{out.name}: {count} entries, total weight {total}")
