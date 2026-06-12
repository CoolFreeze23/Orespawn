"""Debug helper: print orig vs port grids/lists for diffed recipes."""
import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).parent))
import recipe_diff as rd

rd.build_stack_vars()
shaped, shapeless, smelting = rd.parse_orig()
port = rd.load_port()

seen = set()
for r in shaped:
    res = r["result"]
    if not res:
        continue
    rid = res[0]
    for name, data in port.items():
        prid, pcount = rd.port_result(data)
        if prid != rid or not data.get("type", "").endswith("crafting_shaped"):
            continue
        pkeys = {k: rd.ing_id(v) for k, v in data.get("key", {}).items()}
        oc = rd.canon_shaped(r["rows"], r["keys"])
        pc = rd.canon_shaped(data.get("pattern", []), pkeys)
        if not rd.grid_equiv(oc, pc) and (rid, name) not in seen:
            seen.add((rid, name))
            print(f"== SHAPED {name} (orig line {r['line']}) {rid}")
            print("  orig:", oc)
            print("  port:", pc)
        break

for r in shapeless:
    res = r["result"]
    if not res:
        continue
    rid = res[0]
    for name, data in port.items():
        prid, pcount = rd.port_result(data)
        if prid != rid or not data.get("type", "").endswith("crafting_shapeless"):
            continue
        pings = sorted(filter(None, (rd.ing_id(i) for i in data.get("ingredients", []))))
        if not rd.list_equiv(r["ingredients"], pings) and (rid, name) not in seen:
            seen.add((rid, name))
            print(f"== SHAPELESS {name} (orig line {r['line']}) {rid}")
            print("  orig:", r["ingredients"])
            print("  port:", pings)
        break
