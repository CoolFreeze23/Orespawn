"""ITEM-062: rewrite divergent port recipe JSONs from the original definitions.

Only touches port files that (a) matched an original recipe by result id and
(b) differ in pattern/ingredients/count/kind. Files already matching an
original exactly are left alone. Absent originals are NOT added (Phase D).
"""
import json
import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).parent))
import recipe_diff as rd

# modern item to write for a 1.7.10 dye damage value
DYE_WRITE = {0: "minecraft:ink_sac", 1: "minecraft:red_dye", 2: "minecraft:green_dye",
             3: "minecraft:cocoa_beans", 4: "minecraft:lapis_lazuli", 5: "minecraft:purple_dye",
             6: "minecraft:cyan_dye", 7: "minecraft:light_gray_dye", 8: "minecraft:gray_dye",
             9: "minecraft:pink_dye", 10: "minecraft:lime_dye", 11: "minecraft:yellow_dye",
             12: "minecraft:light_blue_dye", 13: "minecraft:magenta_dye", 14: "minecraft:orange_dye",
             15: "minecraft:bone_meal"}


def write_id(ident: str) -> str:
    if ident.startswith("minecraft:dye#"):
        return DYE_WRITE[int(ident.split("#")[1])]
    return ident


def writable(ident: str) -> bool:
    if ident.startswith("minecraft:dye#"):
        return True
    ns, _, path = ident.partition(":")
    if ns == "minecraft":
        return "#" not in ident
    return path in rd.PORT_IDS


def shaped_json(canon, result_id, count):
    ids = []
    for row in canon:
        for c in row:
            if c and c not in ids:
                ids.append(c)
    letters = {ident: chr(ord("A") + i) for i, ident in enumerate(ids)}
    pattern = ["".join(letters[c] if c else " " for c in row) for row in canon]
    key = {letters[i]: {"item": write_id(i)} for i in ids}
    res = {"id": result_id}
    if count != 1:
        res["count"] = count
    return {"type": "minecraft:crafting_shaped", "pattern": pattern, "key": key, "result": res}


def shapeless_json(ingredients, result_id, count):
    res = {"id": result_id}
    if count != 1:
        res["count"] = count
    return {"type": "minecraft:crafting_shapeless",
            "ingredients": [{"item": write_id(i)} for i in ingredients],
            "result": res}


def smelting_json(inp, result_id, count, xp):
    res = {"id": result_id}
    if count != 1:
        res["count"] = count
    return {"type": "minecraft:smelting", "ingredient": {"item": write_id(inp)},
            "result": res, "experience": xp, "cookingtime": 200}


def save(name, data):
    path = rd.RECIPE_DIR / name
    path.write_text(json.dumps(data, indent=2) + "\n", encoding="utf-8")
    print("wrote", name)


def main():
    rd.build_stack_vars()
    shaped, shapeless, smelting = rd.parse_orig()
    port = rd.load_port()
    port_by_result = {}
    for name, data in port.items():
        rid, _ = rd.port_result(data)
        port_by_result.setdefault(rid, []).append(name)

    logical = {}
    for r in shaped:
        key = (r["result"], rd.canon_shaped(r["rows"], r["keys"]))
        logical.setdefault(key, []).append(r)

    # pass 1: find port files that already match an original exactly (do not rewrite)
    ok_files = set()
    for (result, canon), variants in logical.items():
        rid, cnt, _ = result
        for name in port_by_result.get(rid, []):
            data = port[name]
            if not data.get("type", "").endswith("crafting_shaped"):
                continue
            pkeys = {k: rd.ing_id(v) for k, v in data.get("key", {}).items()}
            _, pcount = rd.port_result(data)
            if rd.grid_equiv(canon, rd.canon_shaped(data.get("pattern", []), pkeys)) and pcount == cnt:
                ok_files.add(name)
    for r in shapeless:
        if not r["result"]:
            continue
        rid, cnt, _ = r["result"]
        for name in port_by_result.get(rid, []):
            data = port[name]
            if not data.get("type", "").endswith("crafting_shapeless"):
                continue
            pings = sorted(filter(None, (rd.ing_id(i) for i in data.get("ingredients", []))))
            _, pcount = rd.port_result(data)
            if rd.list_equiv(r["ingredients"], pings) and pcount == cnt:
                ok_files.add(name)
    for r in smelting:
        if not r["result"]:
            continue
        rid, cnt, _ = r["result"]
        for name in port_by_result.get(rid, []):
            data = port[name]
            if not data.get("type", "").endswith("smelting"):
                continue
            if rd.equiv(r["input"], rd.ing_id(data.get("ingredient"))) and data.get("experience") == r["xp"]:
                ok_files.add(name)

    rewritten = set()

    def claim(rid, kind_suffix):
        """First port file with this result/kind not already exact and not rewritten."""
        for name in port_by_result.get(rid, []):
            if name in ok_files or name in rewritten:
                continue
            if port[name].get("type", "").endswith(kind_suffix):
                return name
        return None

    skipped = []

    for (result, canon), variants in sorted(logical.items(), key=lambda kv: kv[1][0]["line"]):
        rid, cnt, _ = result
        if any(name in ok_files for name in port_by_result.get(rid, [])
               if port[name].get("type", "").endswith("crafting_shaped")):
            # an exact match exists; other variants of this result are left alone
            pass
        name = claim(rid, "crafting_shaped") or claim(rid, "crafting_shapeless")
        if name is None:
            continue
        ids = [c for row in canon for c in row if c]
        if not all(writable(i) for i in ids):
            skipped.append((variants[0]["line"], rid, "unwritable ingredient"))
            continue
        # only rewrite if this file actually differs from this orig
        data = port[name]
        if data.get("type", "").endswith("crafting_shaped"):
            pkeys = {k: rd.ing_id(v) for k, v in data.get("key", {}).items()}
            _, pcount = rd.port_result(data)
            if rd.grid_equiv(canon, rd.canon_shaped(data.get("pattern", []), pkeys)) and pcount == cnt:
                continue
        save(name, shaped_json(canon, rid, cnt))
        rewritten.add(name)

    for r in shapeless:
        if not r["result"]:
            continue
        rid, cnt, _ = r["result"]
        exact = [n for n in port_by_result.get(rid, []) if n in ok_files
                 and port[n].get("type", "").endswith("crafting_shapeless")]
        if exact:
            continue
        name = claim(rid, "crafting_shapeless") or claim(rid, "crafting_shaped")
        if name is None:
            continue
        if not all(writable(i) for i in r["ingredients"]):
            skipped.append((r["line"], rid, "unwritable ingredient"))
            continue
        save(name, shapeless_json(r["ingredients"], rid, cnt))
        rewritten.add(name)

    for r in smelting:
        if not r["result"]:
            continue
        rid, cnt, _ = r["result"]
        exact = [n for n in port_by_result.get(rid, []) if n in ok_files
                 and port[n].get("type", "").endswith("smelting")]
        if exact:
            continue
        name = claim(rid, "smelting") or claim(rid, "crafting_shapeless") or claim(rid, "crafting_shaped")
        if name is None:
            continue
        if not writable(r["input"]):
            skipped.append((r["line"], rid, "unwritable input"))
            continue
        save(name, smelting_json(r["input"], rid, cnt, r["xp"]))
        rewritten.add(name)

    print(f"\nrewritten: {len(rewritten)}")
    for line, rid, why in skipped:
        print(f"skipped line {line} {rid}: {why}")


if __name__ == "__main__":
    main()
