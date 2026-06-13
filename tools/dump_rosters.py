import json

for name in ["village_biome", "utopia_plains", "island_biome", "crystal_plains", "chaos_biome", "mining_biome"]:
    data = json.load(open(f"src/main/resources/data/orespawn/worldgen/biome/{name}.json", encoding="utf-8"))
    print(f"=== {name}")
    for cat, entries in data.get("spawners", {}).items():
        if not entries:
            continue
        row = ", ".join(f"{e['type'].split(':')[1]} w{e['weight']} {e['minCount']}-{e['maxCount']}" for e in entries)
        print(f"  {cat}: {row}")
