"""ITEM-062 bulk recipe correspondence diff (Phase C slice 6).

Extracts every GameRegistry.addRecipe / addShapelessRecipe / addSmelting call
from the original 1.7.10 OreSpawnMain.java, normalizes identifiers, and matches
each against the port's recipe JSONs under data/orespawn/recipe/.

Mirrored shaped variants (same result + same char->ingredient map, pattern only
shifted left/center/right) are collapsed into one logical recipe, per the audit
instruction for ITEM-062.

Output: phase_c_reports/C6_recipe_diff.md
"""
import json
import re
from collections import defaultdict
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
ORIG = ROOT / "reference_1_7_10_source/sources/danger/orespawn/OreSpawnMain.java"
SRC_MAIN = ROOT / "src/main/java/danger/orespawn"
RECIPE_DIR = ROOT / "src/main/resources/data/orespawn/recipe"
OUT = ROOT / "phase_c_reports/C6_recipe_diff.md"

# MCP 1.7.10 srg->name mapping for the 44 vanilla fields used in recipe lines.
VANILLA = {
    "Blocks.field_150321_G": "minecraft:cobweb",          # web
    "Blocks.field_150328_O": "minecraft:poppy",           # red_flower
    "Blocks.field_150335_W": "minecraft:tnt",
    "Blocks.field_150339_S": "minecraft:iron_block",
    "Blocks.field_150344_f": "minecraft:oak_planks",      # planks
    "Blocks.field_150347_e": "minecraft:cobblestone",
    "Blocks.field_150359_w": "minecraft:glass",
    "Blocks.field_150368_y": "minecraft:lapis_block",
    "Blocks.field_150371_ca": "minecraft:quartz_block",
    "Blocks.field_150424_aL": "minecraft:netherrack",
    "Blocks.field_150434_aF": "minecraft:cactus",
    "Blocks.field_150451_bX": "minecraft:redstone_block",
    "Blocks.field_150478_aa": "minecraft:torch",
    "Blocks.field_150486_ae": "minecraft:chest",
    "Items.field_151007_F": "minecraft:string",
    "Items.field_151015_O": "minecraft:wheat",
    "Items.field_151016_H": "minecraft:gunpowder",
    "Items.field_151025_P": "minecraft:bread",
    "Items.field_151034_e": "minecraft:apple",
    "Items.field_151042_j": "minecraft:iron_ingot",
    "Items.field_151044_h": "minecraft:coal",
    "Items.field_151045_i": "minecraft:diamond",
    "Items.field_151054_z": "minecraft:bowl",
    "Items.field_151055_y": "minecraft:stick",
    "Items.field_151061_bv": "minecraft:ender_eye",
    "Items.field_151062_by": "minecraft:experience_bottle",
    "Items.field_151063_bx": "minecraft:spawn_egg",
    "Items.field_151069_bo": "minecraft:glass_bottle",
    "Items.field_151076_bf": "minecraft:chicken",
    "Items.field_151079_bi": "minecraft:ender_pearl",
    "Items.field_151100_aR": "minecraft:dye",
    "Items.field_151101_aQ": "minecraft:cooked_cod",      # cooked_fished
    "Items.field_151102_aT": "minecraft:sugar",
    "Items.field_151104_aV": "minecraft:red_bed",         # bed
    "Items.field_151121_aF": "minecraft:paper",
    "Items.field_151123_aH": "minecraft:slime_ball",
    "Items.field_151117_aB": "minecraft:milk_bucket",
    "Items.field_151131_as": "minecraft:water_bucket",
    "Items.field_151133_ar": "minecraft:bucket",
    "Items.field_151135_aq": "minecraft:oak_door",        # wooden_door item
    "Items.field_151137_ax": "minecraft:redstone",
    "Items.field_151147_al": "minecraft:porkchop",
    "Items.field_151156_bN": "minecraft:nether_star",
    "Items.field_151166_bC": "minecraft:emerald",
    "Items.field_151172_bF": "minecraft:carrot",
}

# 1.7.10 dye damage values -> modern item(s) accepted as equivalent
DYE_META = {
    0: {"minecraft:ink_sac", "minecraft:black_dye"},
    1: {"minecraft:red_dye"},
    2: {"minecraft:green_dye"},
    3: {"minecraft:cocoa_beans", "minecraft:brown_dye"},
    4: {"minecraft:lapis_lazuli", "minecraft:blue_dye"},
    5: {"minecraft:purple_dye"},
    6: {"minecraft:cyan_dye"},
    7: {"minecraft:light_gray_dye"},
    8: {"minecraft:gray_dye"},
    9: {"minecraft:pink_dye"},
    10: {"minecraft:lime_dye"},
    11: {"minecraft:yellow_dye"},
    12: {"minecraft:light_blue_dye"},
    13: {"minecraft:magenta_dye"},
    14: {"minecraft:orange_dye"},
    15: {"minecraft:bone_meal", "minecraft:white_dye"},
}

# tag id -> vanilla item(s) considered equivalent
TAG_EQUIV = {
    "#c:ingots/iron": {"minecraft:iron_ingot"},
    "#c:gems/diamond": {"minecraft:diamond"},
    "#c:gems/emerald": {"minecraft:emerald"},
    "#c:rods/wooden": {"minecraft:stick"},
    "#minecraft:planks": {"minecraft:oak_planks"},
    "#c:dusts/redstone": {"minecraft:redstone"},
    "#minecraft:beds": {"minecraft:red_bed"},
    "#c:strings": {"minecraft:string"},
    "#c:slimeballs": {"minecraft:slime_ball"},
}

MANUAL_ALIAS = {
    "step_accross": "step_across",
    "buttered_and_salted_popcorn": "buttered_salted_popcorn",
    "hammy": "attitude_adjuster",      # orig lang: MyHammy -> "Attitude Adjuster"
    "bertha": "big_bertha",
    "blt": "blt_sandwich",
    "amethyst": "amethyst_gem",
    "bacon": "cooked_bacon",
    "peacock": "cooked_peacock",
    "crab_meat": "cooked_crab_meat",
    "apple_seed": "apple_tree_seed",
    "cherry_seed": "cherry_tree_seed",
    "peach_seed": "peach_tree_seed",
    "crystal_pink_helmet": "pink_helmet",
    "crystal_pink_body": "pink_chestplate",
    "crystal_pink_legs": "pink_leggings",
    "crystal_pink_boots": "pink_boots",
    "peacock_feather_helmet": "peacock_helmet",
    "peacock_feather_body": "peacock_chestplate",
    "peacock_feather_legs": "peacock_leggings",
    "peacock_feather_boots": "peacock_boots",
    "pizza_item": "pizza_item",
    "irukandji": "dead_irukandji",     # orig MyIrukandji = the dead irukandji item
    "cherry": "cherries",
    "tigers_eye": "tigers_eye_ore",    # orig TigersEye block = the ore
    "dt": "duplicator_log",            # orig MyDT = Duplicator Tree Wood
}

# 1.7.10 vanilla spawn-egg damage values -> modern spawn egg item ids
EGG_META = {
    50: "creeper", 51: "skeleton", 52: "spider", 54: "zombie", 55: "slime",
    56: "ghast", 57: "zombified_piglin", 58: "enderman", 59: "cave_spider",
    60: "silverfish", 61: "blaze", 62: "magma_cube", 65: "bat", 66: "witch",
    90: "pig", 91: "sheep", 92: "cow", 93: "chicken", 94: "squid", 95: "wolf",
    96: "mooshroom", 98: "ocelot", 100: "horse", 120: "villager",
}


def camel_to_snake(name: str) -> str:
    name = re.sub(r"^My", "", name)
    s = re.sub(r"(?<=[a-z0-9])(?=[A-Z])|(?<=[A-Z])(?=[A-Z][a-z])", "_", name)
    return s.lower()


def load_port_ids():
    ids = set()
    for f in (SRC_MAIN / "ModItems.java", SRC_MAIN / "ModBlocks.java"):
        for m in re.finditer(r'register\w*\(\s*"([a-z0-9_]+)"', f.read_text(encoding="utf-8")):
            ids.add(m.group(1))
    return ids


PORT_IDS = load_port_ids()
COLLAPSED = {}
for pid in PORT_IDS:
    COLLAPSED.setdefault(pid.replace("_", ""), pid)


def resolve_mod(snake: str) -> str:
    """Map a snake_cased orig field name to an actual port registry id."""
    cands = [snake]
    if snake in MANUAL_ALIAS:
        cands.append(MANUAL_ALIAS[snake])
    if snake.endswith("_block"):
        cands.append(snake[:-6])
        cands.append("block_" + snake[:-6])
    if snake.endswith("_body"):
        cands.append(snake[:-5] + "_chestplate")
    if snake.endswith("_legs"):
        cands.append(snake[:-5] + "_leggings")
    expanded = []
    for c in list(cands):
        expanded.append(c)
        if c.endswith("_chestplate") or c.endswith("_leggings") or c.endswith("_helmet") or c.endswith("_boots"):
            base, _, part = c.rpartition("_")
            expanded.append(base.replace("_", "") + "_" + part)
    if snake.endswith("_egg") and not snake.endswith("_spawn_egg"):
        base = snake[:-4]
        expanded.append(base + "_spawn_egg")
    for c in expanded:
        if c in PORT_IDS:
            return "orespawn:" + c
    collapsed = snake.replace("_", "")
    if collapsed in COLLAPSED:
        return "orespawn:" + COLLAPSED[collapsed]
    if snake.endswith("_egg") and not snake.endswith("_spawn_egg"):
        # vanilla-entity custom egg items (e.g. WitherSkeletonEgg) -> modern vanilla egg
        return "minecraft:" + snake[:-4] + "_spawn_egg"
    return "orespawn:" + snake


def map_token(name: str, meta: int | None) -> str:
    """Map an orig identifier (vanilla field or mod field) + meta to an id string."""
    if name.startswith(("Items.field", "Blocks.field")):
        base = VANILLA.get(name, name)
        if base == "minecraft:dye":
            return f"minecraft:dye#{meta or 0}"
        if base == "minecraft:spawn_egg":
            ent = EGG_META.get(meta or 0)
            return f"minecraft:{ent}_spawn_egg" if ent else f"minecraft:spawn_egg#{meta}"
        return base
    if name in STACK_VARS:
        return STACK_VARS[name]
    return resolve_mod(camel_to_snake(name.split(".")[-1]))


ITEMSTACK_RE = re.compile(
    r"new ItemStack\(\s*(?:\(Block\)|\(Item\))?\s*([A-Za-z_][A-Za-z0-9_.]*)"
    r"(?:\s*,\s*(\d+))?(?:\s*,\s*(\d+))?\s*\)"
)

# local `ItemStack Foo = new ItemStack(Bar...)` variables (egg stacks, RayStack, ...)
STACK_VARS: dict[str, str] = {}


def build_stack_vars():
    decl = re.compile(r"ItemStack\s+([A-Za-z_]\w*)\s*=\s*new ItemStack\(\s*(?:\(Block\)|\(Item\))?\s*([A-Za-z_][A-Za-z0-9_.]*)")
    for m in decl.finditer(ORIG.read_text(encoding="utf-8", errors="replace")):
        var, target = m.group(1), m.group(2)
        if target.startswith(("Items.field", "Blocks.field")):
            STACK_VARS[var] = VANILLA.get(target, target)
        else:
            STACK_VARS[var] = resolve_mod(camel_to_snake(target.split(".")[-1]))


def parse_itemstack(text: str):
    m = ITEMSTACK_RE.search(text)
    if not m:
        return None
    name, cnt, meta = m.group(1), int(m.group(2) or 1), int(m.group(3)) if m.group(3) else None
    return map_token(name, meta), cnt, meta


def parse_orig():
    shaped, shapeless, smelting = [], [], []
    for lineno, line in enumerate(ORIG.read_text(encoding="utf-8", errors="replace").splitlines(), 1):
        s = line.strip()
        if "addSmelting" in s:
            inner = s[s.index("addSmelting(") + len("addSmelting("):]
            first = inner.split(",")[0]
            fm = re.search(r"([A-Za-z_][A-Za-z0-9_.]*)\s*$", first)
            inp = map_token(fm.group(1), None) if fm else first
            out = parse_itemstack(inner[inner.index(","):])
            xp = re.search(r"\(float\)([\d.]+)f", s)
            smelting.append({"line": lineno, "input": inp, "result": out,
                             "xp": float(xp.group(1)) if xp else None})
        elif "addShapelessRecipe" in s:
            res = parse_itemstack(s)
            body = s[s.index("Object[]{") + len("Object[]{"):]
            ings = []
            for m in ITEMSTACK_RE.finditer(body):
                meta = int(m.group(3)) if m.group(3) else None
                for _ in range(1):
                    ings.append(map_token(m.group(1), meta))
            stripped = ITEMSTACK_RE.sub("", body)
            for m in re.finditer(r"(?:\(Block\)|\(Item\)|\(Object\))?\s*([A-Za-z_][A-Za-z0-9_.]*)\s*[,}]", stripped):
                t = m.group(1)
                if t in ("new", "Object", "Character", "ItemStack") or t.endswith("valueOf"):
                    continue
                if t.startswith(("Items.field", "Blocks.field")) or re.match(r"^[A-Z]", t.split(".")[-1]):
                    ings.append(map_token(t, None))
            shapeless.append({"line": lineno, "result": res, "ingredients": sorted(ings)})
        elif "GameRegistry.addRecipe" in s:
            res = parse_itemstack(s)
            body = s[s.index("Object[]{") + len("Object[]{"):]
            rows = re.findall(r'"([^"]*)"', body)
            keys = {}
            for m in re.finditer(
                    r"Character\.valueOf\(\s*'(.)'\s*\)\s*,\s*(?:\(Object\))?\s*"
                    r"(?:new ItemStack\(\s*)?(?:\(Block\)|\(Item\))?\s*([A-Za-z_][A-Za-z0-9_.]*)"
                    r"(?:\s*,\s*\d+\s*,\s*(\d+))?", body):
                ch, name, meta = m.group(1), m.group(2), int(m.group(3)) if m.group(3) else None
                keys[ch] = map_token(name, meta)
            shaped.append({"line": lineno, "result": res, "rows": rows, "keys": keys})
    return shaped, shapeless, smelting


def canon_shaped(rows, keys):
    """Canonical grid ignoring horizontal/vertical shifts (mirror-shift variants)."""
    grid = list(rows)
    width = max((len(r) for r in grid), default=0)
    grid = [r.ljust(width) for r in grid]
    while grid and grid[0].strip() == "":
        grid.pop(0)
    while grid and grid[-1].strip() == "":
        grid.pop()
    if grid:
        cols = list(zip(*grid))
        while cols and all(c == " " for c in cols[0]):
            cols.pop(0)
        while cols and all(c == " " for c in cols[-1]):
            cols.pop()
        grid = ["".join(r) for r in zip(*cols)] if cols else []
    return tuple(tuple(keys.get(ch) if ch != " " else None for ch in row) for row in grid)


def equiv(orig_id, port_id) -> bool:
    """True when the orig identifier and port ingredient id are gameplay-equivalent."""
    if orig_id is None or port_id is None:
        return orig_id == port_id
    if orig_id == port_id:
        return True
    for pid in port_id.split("|"):
        if pid == orig_id:
            return True
        if orig_id.startswith("minecraft:dye#"):
            meta = int(orig_id.split("#")[1])
            if pid in DYE_META.get(meta, set()):
                return True
        if pid.startswith("#") and orig_id in TAG_EQUIV.get(pid, set()):
            return True
    return False


def grid_equiv(orig_grid, port_grid) -> bool:
    if len(orig_grid) != len(port_grid):
        return False
    for orow, prow in zip(orig_grid, port_grid):
        if len(orow) != len(prow):
            return False
        for o, p in zip(orow, prow):
            if not equiv(o, p):
                return False
    return True


def list_equiv(orig_list, port_list) -> bool:
    if len(orig_list) != len(port_list):
        return False
    remaining = list(port_list)
    for o in orig_list:
        for i, p in enumerate(remaining):
            if equiv(o, p):
                remaining.pop(i)
                break
        else:
            return False
    return True


def load_port():
    return {f.name: json.loads(f.read_text(encoding="utf-8-sig"))
            for f in sorted(RECIPE_DIR.glob("*.json"))}


def port_result(data):
    r = data.get("result", {})
    if isinstance(r, str):
        return r, 1
    return r.get("id") or r.get("item"), r.get("count", 1)


def ing_id(ing):
    if isinstance(ing, dict):
        return ing.get("item") or ("#" + ing["tag"] if "tag" in ing else None)
    if isinstance(ing, list):
        return "|".join(sorted(filter(None, (ing_id(i) for i in ing))))
    return ing


def main():
    build_stack_vars()
    shaped, shapeless, smelting = parse_orig()

    logical = {}
    for r in shaped:
        key = (r["result"], canon_shaped(r["rows"], r["keys"]))
        logical.setdefault(key, []).append(r)

    port = load_port()
    port_by_result = defaultdict(list)
    for name, data in port.items():
        rid, _ = port_result(data)
        if rid:
            port_by_result[rid].append(name)

    matched_port = set()
    rows_out = []

    def find_port(result_id, kind):
        out = []
        for name in port_by_result.get(result_id, []):
            t = port[name].get("type", "")
            if (kind == "shaped" and t.endswith("crafting_shaped")) or \
               (kind == "shapeless" and t.endswith("crafting_shapeless")) or \
               (kind == "smelting" and t.endswith("smelting")):
                out.append(name)
        return out

    def find_other_kind(result_id, kind):
        """Any port recipe with the same result but a different recipe type."""
        same = set(find_port(result_id, kind))
        return [n for n in port_by_result.get(result_id, []) if n not in same]

    def mark_variants(result_id, kind):
        for n in find_port(result_id, kind):
            matched_port.add(n)

    for (result, canon), variants in sorted(logical.items(), key=lambda kv: kv[1][0]["line"]):
        rid, cnt, meta = result if result else ("?", 0, 0)
        line = variants[0]["line"]
        label = f"{rid} x{cnt}"
        cands = find_port(rid, "shaped")
        if not cands:
            other = find_other_kind(rid, "shaped")
            if other:
                matched_port.update(other)
                rows_out.append(("SHAPED", line, label, other[0],
                                 f"KIND DIFF (orig shaped -> port {port[other[0]].get('type')})"))
            else:
                rows_out.append(("SHAPED", line, label, f"{len(variants)} variant(s)", "NO PORT RECIPE"))
            continue
        best = None
        for name in cands:
            data = port[name]
            pkeys = {k: ing_id(v) for k, v in data.get("key", {}).items()}
            pcanon = canon_shaped(data.get("pattern", []), pkeys)
            _, pcount = port_result(data)
            notes = []
            if not grid_equiv(canon, pcanon):
                notes.append("pattern/ingredient diff")
            if pcount != cnt:
                notes.append(f"count {cnt}->{pcount}")
            if not notes:
                best = (name, "OK")
                break
            if best is None:
                best = (name, "; ".join(notes))
        mark_variants(rid, "shaped")
        rows_out.append(("SHAPED", line, label, best[0], best[1]))

    for r in shapeless:
        rid, cnt, meta = r["result"] if r["result"] else ("?", 0, 0)
        label = f"{rid} x{cnt}"
        cands = find_port(rid, "shapeless")
        if not cands:
            other = find_other_kind(rid, "shapeless")
            if other:
                matched_port.update(other)
                rows_out.append(("SHAPELESS", r["line"], label, other[0],
                                 f"KIND DIFF (orig shapeless -> port {port[other[0]].get('type')})"))
            else:
                rows_out.append(("SHAPELESS", r["line"], label, ",".join(r["ingredients"]), "NO PORT RECIPE"))
            continue
        best = None
        for name in cands:
            pings = sorted(filter(None, (ing_id(i) for i in port[name].get("ingredients", []))))
            _, pcount = port_result(port[name])
            notes = []
            if not list_equiv(r["ingredients"], pings):
                notes.append(f"ingredients {r['ingredients']} -> {pings}")
            if pcount != cnt:
                notes.append(f"count {cnt}->{pcount}")
            if not notes:
                best = (name, "OK")
                break
            if best is None:
                best = (name, "; ".join(notes))
        mark_variants(rid, "shapeless")
        rows_out.append(("SHAPELESS", r["line"], label, best[0], best[1]))

    for r in smelting:
        rid, cnt, _ = r["result"] if r["result"] else ("?", 0, 0)
        label = f"{rid} x{cnt}"
        cands = find_port(rid, "smelting")
        if not cands:
            other = find_other_kind(rid, "smelting")
            if other:
                matched_port.update(other)
                rows_out.append(("SMELTING", r["line"], label, other[0],
                                 f"KIND DIFF (orig smelting -> port {port[other[0]].get('type')})"))
            else:
                rows_out.append(("SMELTING", r["line"], label, r["input"], "NO PORT RECIPE"))
            continue
        # prefer the candidate whose input matches (several recipes share one result)
        same_input = [n for n in cands if equiv(r["input"], ing_id(port[n].get("ingredient")))]
        name = same_input[0] if same_input else cands[0]
        data = port[name]
        notes = []
        if not same_input:
            notes.append(f"input {r['input']} -> {ing_id(data.get('ingredient'))}")
        if r["xp"] is not None and data.get("experience") != r["xp"]:
            notes.append(f"xp {r['xp']} -> {data.get('experience')}")
        matched_port.add(name)
        rows_out.append(("SMELTING", r["line"], label, name, "; ".join(notes) if notes else "OK"))

    unmatched_port = sorted(set(port) - matched_port)

    ok = sum(1 for r in rows_out if r[4] == "OK")
    miss = sum(1 for r in rows_out if r[4] == "NO PORT RECIPE")
    diff = len(rows_out) - ok - miss

    with OUT.open("w", encoding="utf-8") as f:
        f.write("# ITEM-062 bulk recipe correspondence diff\n\n")
        f.write("Generated by `phase_c_reports/scripts/recipe_diff.py` (Phase C slice 6).\n\n")
        f.write(f"Original registrations: {len(shaped)} shaped ({len(logical)} logical after "
                f"collapsing shifted variants) + {len(shapeless)} shapeless + {len(smelting)} smelting"
                f" = {len(shaped) + len(shapeless) + len(smelting)} lines\n\n")
        f.write(f"Port recipe JSONs: {len(port)}\n\n")
        f.write(f"Logical originals matched OK: {ok} | with diffs: {diff} | no port recipe: {miss}\n")
        f.write(f"Port JSONs with no original counterpart: {len(unmatched_port)}\n\n")
        f.write("## Per-original results\n\n| kind | orig line | result | match | status |\n|---|---|---|---|---|\n")
        for k, line, res, match, status in rows_out:
            f.write(f"| {k} | {line} | {res} | {match} | {status} |\n")
        f.write("\n## Port JSONs with no original counterpart\n\n")
        for name in unmatched_port:
            rid, _ = port_result(port[name])
            f.write(f"- `{name}` -> {rid} ({port[name].get('type', '?')})\n")
    print(f"wrote {OUT}")
    print(f"orig lines {len(shaped) + len(shapeless) + len(smelting)}, logical shaped {len(logical)}, "
          f"OK {ok}, diffs {diff}, missing {miss}, port-only {len(unmatched_port)}")


if __name__ == "__main__":
    main()
