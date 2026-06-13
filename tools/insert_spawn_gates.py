"""D1 one-off: insert original func_70601_bi spawn-gate ports as checkSpawnRules
overrides into entity classes that have none yet. Entities with side effects in
their gates (Crab, Rotator, WormLarge, PitchBlack, Cephadrome, Rat) and entities
with existing overrides to fix (AttackSquid, CaveFisher) are edited by hand.

Uses fully-qualified types in signatures and package-local OriginalSpawnGates
calls so no import edits are needed.
"""
import pathlib

SRC = pathlib.Path("src/main/java/danger/orespawn/entity")

SIG = ("    @Override\n"
       "    public boolean checkSpawnRules(net.minecraft.world.level.LevelAccessor level,\n"
       "                                   net.minecraft.world.entity.MobSpawnType spawnType) {\n")
END = "    }\n"

GATES = {
    # file -> (citation comment, body lines)
    "Alien.java": ("orig Alien.java:397-434 — spawner bypass; darkness; Islands always allowed; y<=50; 3x3x3 clear air above.", [
        "if (OriginalSpawnGates.nearOwnSpawner(this, level)) return true;",
        "if (!OriginalSpawnGates.isDarkEnough(this, level)) return false;",
        "if (danger.orespawn.ModDimensionKeys.isIn(level, danger.orespawn.ModDimensionKeys.ISLANDS)) return true;",
        "if (this.getY() > 50.0) return false;",
        "return OriginalSpawnGates.airBox(this, level, -1, 1, 1, 3, -1, 1);",
    ]),
    "Alosaurus.java": ("orig Alosaurus.java:240-279 — spawner bypass; darkness; y>=50; night; clear-air column; no other Alosaurus within 16/8/16.", [
        "if (OriginalSpawnGates.nearOwnSpawner(this, level)) return true;",
        "if (!OriginalSpawnGates.isDarkEnough(this, level)) return false;",
        "if (this.getY() < 50.0) return false;",
        "if (OriginalSpawnGates.isDaytime(level)) return false;",
        "if (!OriginalSpawnGates.airBox(this, level, -1, 0, 1, 5, -1, 0)) return false;",
        "return !OriginalSpawnGates.anyOtherNearby(this, level, Alosaurus.class, 16.0, 8.0, 16.0);",
    ]),
    "BandP.java": ("orig BandP.java:278-309 — \"Criminal\" spawner bypass; daytime; y>=100 (the y>=50 check is shadowed); no other BandP within 32/12/32; requires a villager within 36/12/36.", [
        "if (OriginalSpawnGates.nearOwnSpawner(this, level)) return true;",
        "if (!OriginalSpawnGates.isDaytime(level)) return false;",
        "if (this.getY() < 50.0) return false;",
        "if (this.getY() < 100.0) return false;",
        "if (OriginalSpawnGates.anyOtherNearby(this, level, BandP.class, 32.0, 12.0, 32.0)) return false;",
        "return !level.getEntitiesOfClass(net.minecraft.world.entity.npc.Villager.class,",
        "        this.getBoundingBox().inflate(36.0, 12.0, 36.0)).isEmpty();",
    ]),
    "Baryonyx.java": ("orig Baryonyx.java:66-74 — y>=50; daytime; at most 8 buddies within 20/10/20.", [
        "if (this.getY() < 50.0) return false;",
        "if (!OriginalSpawnGates.isDaytime(level)) return false;",
        "return OriginalSpawnGates.countBuddies(this, level, Baryonyx.class, 20.0, 10.0, 20.0) <= 8;",
    ]),
    "Basilisk.java": ("orig Basilisk.java:441-477 — spawner bypass; darkness; night; clear air above; no other Basilisk within 20/6/20.", [
        "if (OriginalSpawnGates.nearOwnSpawner(this, level)) return true;",
        "if (!OriginalSpawnGates.isDarkEnough(this, level)) return false;",
        "if (OriginalSpawnGates.isDaytime(level)) return false;",
        "if (!OriginalSpawnGates.airBox(this, level, -1, 1, 1, 4, -1, 1)) return false;",
        "return !OriginalSpawnGates.anyOtherNearby(this, level, Basilisk.class, 20.0, 6.0, 20.0);",
    ]),
    "EntityBee.java": ("orig Bee.java:253-287 — Islands always allowed; spawner bypass; clear air above; y>=50; daytime.", [
        "if (danger.orespawn.ModDimensionKeys.isIn(level, danger.orespawn.ModDimensionKeys.ISLANDS)) return true;",
        "if (OriginalSpawnGates.nearOwnSpawner(this, level, -2, 1, 0, 4)) return true;",
        "if (!OriginalSpawnGates.airBox(this, level, -1, 1, 1, 4, -1, 1)) return false;",
        "if (this.getY() < 50.0) return false;",
        "return OriginalSpawnGates.isDaytime(level);",
    ]),
    "EntityBrutalfly.java": ("orig Brutalfly.java:290-329 — spawner bypass; y>=70; darkness; night; 6x9x8 clear-air volume; no other Brutalfly within 64/32/64.", [
        "if (OriginalSpawnGates.nearOwnSpawner(this, level, -2, 2, 1, 3)) return true;",
        "if (this.getY() < 70.0) return false;",
        "if (!OriginalSpawnGates.isDarkEnough(this, level)) return false;",
        "if (OriginalSpawnGates.isDaytime(level)) return false;",
        "if (!OriginalSpawnGates.airBox(this, level, -3, 2, 1, 9, -4, 3)) return false;",
        "return !OriginalSpawnGates.anyOtherNearby(this, level, EntityBrutalfly.class, 64.0, 32.0, 64.0);",
    ]),
    "Camarasaurus.java": ("orig Camarasaurus.java:78-83 — y>=50 and daytime.", [
        "if (this.getY() < 50.0) return false;",
        "return OriginalSpawnGates.isDaytime(level);",
    ]),
    "Cassowary.java": ("orig Cassowary.java:113-115 — daytime only.", [
        "return OriginalSpawnGates.isDaytime(level);",
    ]),
    "EntityCaterKiller.java": ("orig CaterKiller.java:585-624 — spawner bypass; y>=50; 1-in-10 dice; daytime; air/leaves/logs clearance above; no other CaterKiller within 48/16/48.", [
        "if (OriginalSpawnGates.nearOwnSpawner(this, level)) return true;",
        "if (this.getY() < 50.0) return false;",
        "if (this.getRandom().nextInt(10) != 0) return false;",
        "if (!OriginalSpawnGates.isDaytime(level)) return false;",
        "if (!OriginalSpawnGates.boxMatches(this, level, -1, 1, 1, 4, -1, 1,",
        "        s -> s.isAir() || s.is(net.minecraft.tags.BlockTags.LEAVES) || s.is(net.minecraft.tags.BlockTags.LOGS))) return false;",
        "return !OriginalSpawnGates.anyOtherNearby(this, level, EntityCaterKiller.class, 48.0, 16.0, 48.0);",
    ]),
    "Cockateil.java": ("orig Cockateil.java:232-240 — daytime; Islands always allowed; otherwise y>=50.", [
        "if (!OriginalSpawnGates.isDaytime(level)) return false;",
        "if (danger.orespawn.ModDimensionKeys.isIn(level, danger.orespawn.ModDimensionKeys.ISLANDS)) return true;",
        "return this.getY() >= 50.0;",
    ]),
    "Cryolophosaurus.java": ("orig Cryolophosaurus.java:231-236 — darkness, then night OR y<=50 (daytime cave spawns allowed).", [
        "if (!OriginalSpawnGates.isDarkEnough(this, level)) return false;",
        "return !OriginalSpawnGates.isDaytime(level) || this.getY() <= 50.0;",
    ]),
    "Coin.java": ("orig Coin.java:138-148 — daytime; y>=50; no other Coin within 20/8/20.", [
        "if (!OriginalSpawnGates.isDaytime(level)) return false;",
        "if (this.getY() < 50.0) return false;",
        "return !OriginalSpawnGates.anyOtherNearby(this, level, Coin.class, 20.0, 8.0, 20.0);",
    ]),
    "GiantRobot.java": ("orig GiantRobot.java:364-381 — y>=50; night; air/short-grass clearance above; darkness.", [
        "if (this.getY() < 50.0) return false;",
        "if (OriginalSpawnGates.isDaytime(level)) return false;",
        "if (!OriginalSpawnGates.boxMatches(this, level, -1, 1, 1, 5, -1, 0,",
        "        s -> s.isAir() || s.is(net.minecraft.world.level.block.Blocks.SHORT_GRASS))) return false;",
        "return OriginalSpawnGates.isDarkEnough(this, level);",
    ]),
    "Kraken.java": ("orig Kraken.java:1183-1197 — y>=50; air/short-grass clearance above the spawn column.", [
        "if (this.getY() < 50.0) return false;",
        "return OriginalSpawnGates.boxMatches(this, level, -1, 0, 1, 5, -1, 1,",
        "        s -> s.isAir() || s.is(net.minecraft.world.level.block.Blocks.SHORT_GRASS));",
    ]),
    "EntityLeafMonster.java": ("orig LeafMonster.java:227-251 — \"Leaf Monster\" spawner bypass; darkness; night; Islands y<=20 / elsewhere y>=50; at most 4 buddies within 20/10/20.", [
        "if (OriginalSpawnGates.nearOwnSpawner(this, level)) return true;",
        "if (!OriginalSpawnGates.isDarkEnough(this, level)) return false;",
        "if (OriginalSpawnGates.isDaytime(level)) return false;",
        "if (danger.orespawn.ModDimensionKeys.isIn(level, danger.orespawn.ModDimensionKeys.ISLANDS)) {",
        "    if (this.getY() > 20.0) return false;",
        "} else if (this.getY() < 50.0) {",
        "    return false;",
        "}",
        "return OriginalSpawnGates.countBuddies(this, level, EntityLeafMonster.class, 20.0, 10.0, 20.0) <= 4;",
    ]),
    "EntityLurkingTerror.java": ("orig LurkingTerror.java:237-269 — spawner bypass; darkness; DAYTIME required; 1-in-2 dice; extra 1-in-6 dice in Chaos; no other within 32/16/32; y>=10.", [
        "if (OriginalSpawnGates.nearOwnSpawner(this, level, -2, 1, 0, 4)) return true;",
        "if (!OriginalSpawnGates.isDarkEnough(this, level)) return false;",
        "if (!OriginalSpawnGates.isDaytime(level)) return false;",
        "if (this.getRandom().nextInt(2) != 1) return false;",
        "if (danger.orespawn.ModDimensionKeys.isIn(level, danger.orespawn.ModDimensionKeys.CHAOS)",
        "        && this.getRandom().nextInt(6) != 0) return false;",
        "if (OriginalSpawnGates.anyOtherNearby(this, level, EntityLurkingTerror.class, 32.0, 16.0, 32.0)) return false;",
        "return this.getY() >= 10.0;",
    ]),
    "EntityMantis.java": ("orig Mantis.java:263-302 — spawner bypass; clear-air volume; extra 1-in-6 dice in Chaos; y>=50; daytime; no other Mantis within 32/16/32.", [
        "if (OriginalSpawnGates.nearOwnSpawner(this, level, -2, 2, 1, 3)) return true;",
        "if (!OriginalSpawnGates.airBox(this, level, -2, 1, 1, 5, -2, 1)) return false;",
        "if (danger.orespawn.ModDimensionKeys.isIn(level, danger.orespawn.ModDimensionKeys.CHAOS)",
        "        && this.getRandom().nextInt(6) != 0) return false;",
        "if (this.getY() < 50.0) return false;",
        "if (!OriginalSpawnGates.isDaytime(level)) return false;",
        "return !OriginalSpawnGates.anyOtherNearby(this, level, EntityMantis.class, 32.0, 16.0, 32.0);",
    ]),
    "EntityMolenoid.java": ("orig Molenoid.java:303-342 — spawner bypass; darkness; y>=50; night; clear air above; no other Molenoid within 16/8/16.", [
        "if (OriginalSpawnGates.nearOwnSpawner(this, level)) return true;",
        "if (!OriginalSpawnGates.isDarkEnough(this, level)) return false;",
        "if (this.getY() < 50.0) return false;",
        "if (OriginalSpawnGates.isDaytime(level)) return false;",
        "if (!OriginalSpawnGates.airBox(this, level, -1, 0, 1, 3, -1, 0)) return false;",
        "return !OriginalSpawnGates.anyOtherNearby(this, level, EntityMolenoid.class, 16.0, 8.0, 16.0);",
    ]),
    "Nastysaurus.java": ("orig Nastysaurus.java:304-343 — spawner bypass; darkness; y>=50; night; clear air above; no other Nastysaurus within 16/8/16.", [
        "if (OriginalSpawnGates.nearOwnSpawner(this, level)) return true;",
        "if (!OriginalSpawnGates.isDarkEnough(this, level)) return false;",
        "if (this.getY() < 50.0) return false;",
        "if (OriginalSpawnGates.isDaytime(level)) return false;",
        "if (!OriginalSpawnGates.airBox(this, level, -1, 0, 1, 5, -1, 0)) return false;",
        "return !OriginalSpawnGates.anyOtherNearby(this, level, Nastysaurus.class, 16.0, 8.0, 16.0);",
    ]),
    "Peacock.java": ("orig Peacock.java:101-119 — clear air above; first half of the day only; 50<=y<=100; at most 2 buddies within 16/10/16 (restores the never-called findBuddies()).", [
        "if (!OriginalSpawnGates.airBox(this, level, -1, 0, 1, 2, -1, 0)) return false;",
        "if (level.dayTime() % 24000L > 12000L) return false;",
        "if (this.getY() < 50.0 || this.getY() > 100.0) return false;",
        "return OriginalSpawnGates.countBuddies(this, level, Peacock.class, 16.0, 10.0, 16.0) <= 2;",
    ]),
    "Robot1.java": ("orig Robot1.java:226-234 — y>=50; darkness; night.", [
        "if (this.getY() < 50.0) return false;",
        "if (!OriginalSpawnGates.isDarkEnough(this, level)) return false;",
        "return !OriginalSpawnGates.isDaytime(level);",
    ]),
    "Robot2.java": ("orig Robot2.java:403-437 — \"Robo-Pounder\" spawner bypass; y>=50; night; air/short-grass clearance above; darkness.", [
        "if (OriginalSpawnGates.nearOwnSpawner(this, level)) return true;",
        "if (this.getY() < 50.0) return false;",
        "if (OriginalSpawnGates.isDaytime(level)) return false;",
        "if (!OriginalSpawnGates.boxMatches(this, level, -1, 1, 1, 5, -1, 0,",
        "        s -> s.isAir() || s.is(net.minecraft.world.level.block.Blocks.SHORT_GRASS))) return false;",
        "return OriginalSpawnGates.isDarkEnough(this, level);",
    ]),
    "Robot3.java": ("orig Robot3.java:343-360 — y>=50; night; air/short-grass clearance above; darkness.", [
        "if (this.getY() < 50.0) return false;",
        "if (OriginalSpawnGates.isDaytime(level)) return false;",
        "if (!OriginalSpawnGates.boxMatches(this, level, -1, 1, 1, 5, -1, 0,",
        "        s -> s.isAir() || s.is(net.minecraft.world.level.block.Blocks.SHORT_GRASS))) return false;",
        "return OriginalSpawnGates.isDarkEnough(this, level);",
    ]),
    "Robot4.java": ("orig Robot4.java:415-449 — \"Robo-Warrior\" spawner bypass; y>=50; night; air/short-grass clearance above; darkness.", [
        "if (OriginalSpawnGates.nearOwnSpawner(this, level)) return true;",
        "if (this.getY() < 50.0) return false;",
        "if (OriginalSpawnGates.isDaytime(level)) return false;",
        "if (!OriginalSpawnGates.boxMatches(this, level, -1, 1, 1, 5, -1, 0,",
        "        s -> s.isAir() || s.is(net.minecraft.world.level.block.Blocks.SHORT_GRASS))) return false;",
        "return OriginalSpawnGates.isDarkEnough(this, level);",
    ]),
    "Robot5.java": ("orig Robot5.java:317-351 — \"Robo-Sniper\" spawner bypass; y>=50; night; shorter (y+1..+2) air/short-grass clearance; darkness.", [
        "if (OriginalSpawnGates.nearOwnSpawner(this, level)) return true;",
        "if (this.getY() < 50.0) return false;",
        "if (OriginalSpawnGates.isDaytime(level)) return false;",
        "if (!OriginalSpawnGates.boxMatches(this, level, -1, 1, 1, 2, -1, 0,",
        "        s -> s.isAir() || s.is(net.minecraft.world.level.block.Blocks.SHORT_GRASS))) return false;",
        "return OriginalSpawnGates.isDarkEnough(this, level);",
    ]),
    "EntityRubberDucky.java": ("orig RubberDucky.java:508-526 — \"Rubber Ducky\" spawner bypass; y>=50; daytime.", [
        "if (OriginalSpawnGates.nearOwnSpawner(this, level)) return true;",
        "if (this.getY() < 50.0) return false;",
        "return OriginalSpawnGates.isDaytime(level);",
    ]),
    "EntityScorpion.java": ("orig Scorpion.java:281-299 — spawner bypass; darkness; then night OR y<=50 (daytime cave spawns allowed).", [
        "if (OriginalSpawnGates.nearOwnSpawner(this, level)) return true;",
        "if (!OriginalSpawnGates.isDarkEnough(this, level)) return false;",
        "return !OriginalSpawnGates.isDaytime(level) || this.getY() <= 50.0;",
    ]),
    "EntityTshirt.java": ("orig Tshirt.java:93-103 — daytime; y>=50; no other Tshirt within 20/8/20.", [
        "if (!OriginalSpawnGates.isDaytime(level)) return false;",
        "if (this.getY() < 50.0) return false;",
        "return !OriginalSpawnGates.anyOtherNearby(this, level, EntityTshirt.class, 20.0, 8.0, 20.0);",
    ]),
}

for filename, (citation, body) in GATES.items():
    path = SRC / filename
    text = path.read_text(encoding="utf-8")
    assert "checkSpawnRules" not in text, f"{filename} already has checkSpawnRules"
    last_brace = text.rstrip().rfind("\n}")
    assert last_brace > 0, filename
    method = "\n    /** " + citation + " */\n" + SIG
    for line in body:
        method += "        " + line + "\n"
    method += END
    new_text = text[:last_brace] + "\n" + method + text[last_brace + 1:]
    path.write_text(new_text, encoding="utf-8")
    print("inserted:", filename)

print("done:", len(GATES))
