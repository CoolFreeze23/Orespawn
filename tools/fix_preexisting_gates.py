"""D1 batch 3: replace pre-existing checkSpawnRules overrides that diverged from
the original func_70601_bi rules (see phase_d_reports/D1_preexisting_gate_audit.md).
Godzilla and Mothra keep their documented config-gated adaptations. Urchin is
edited by hand (was_spawnered side effect)."""
import pathlib
import re

SRC = pathlib.Path("src/main/java/danger/orespawn/entity")

SIG = ("    @Override\n"
       "    public boolean checkSpawnRules(net.minecraft.world.level.LevelAccessor level,\n"
       "                                   net.minecraft.world.entity.MobSpawnType spawnType) {\n")

GATES = {
    "EntityButterfly.java": ("orig EntityButterfly.java:283-310 — \"Butterfly\" spawner bypass (forces type 1); feet-block air; daytime; Islands always allowed; otherwise y>=50.", [
        "if (OriginalSpawnGates.nearOwnSpawner(this, level)) {",
        "    this.setButterflyType(1);",
        "    return true;",
        "}",
        "if (!level.getBlockState(this.blockPosition()).isAir()) return false;",
        "if (!OriginalSpawnGates.isDaytime(level)) return false;",
        "if (danger.orespawn.ModDimensionKeys.isIn(level, danger.orespawn.ModDimensionKeys.ISLANDS)) return true;",
        "return this.getY() >= 50.0;",
    ]),
    "EntityDragonfly.java": ("orig Dragonfly.java:187-192 — y>=50; daytime.", [
        "if (this.getY() < 50.0) return false;",
        "return OriginalSpawnGates.isDaytime(level);",
    ]),
    "EntityLunaMoth.java": ("orig EntityLunaMoth.java:168-180 — feet-block air; night; Islands always allowed; otherwise y>=50.", [
        "if (!level.getBlockState(this.blockPosition()).isAir()) return false;",
        "if (OriginalSpawnGates.isDaytime(level)) return false;",
        "if (danger.orespawn.ModDimensionKeys.isIn(level, danger.orespawn.ModDimensionKeys.ISLANDS)) return true;",
        "return this.getY() >= MIN_SPAWN_Y;",
    ]),
    "EntityStinkBug.java": ("orig StinkBug.java:136-151 — \"Stink Bug\" spawner bypass; y>=50.", [
        "if (OriginalSpawnGates.nearOwnSpawner(this, level)) return true;",
        "return this.getY() >= 50.0;",
    ]),
    "Firefly.java": ("orig Firefly.java:161-176 — feet-block air; night; at most 10 buddies within 20/8/20; Islands always allowed; otherwise y>=50.", [
        "if (!level.getBlockState(this.blockPosition()).isAir()) return false;",
        "if (OriginalSpawnGates.isDaytime(level)) return false;",
        "if (OriginalSpawnGates.countBuddies(this, level, Firefly.class, 20.0, 8.0, 20.0) > 10) return false;",
        "if (danger.orespawn.ModDimensionKeys.isIn(level, danger.orespawn.ModDimensionKeys.ISLANDS)) return true;",
        "return this.getY() >= 50.0;",
    ]),
    "Flounder.java": ("orig Flounder.java:219-230 — y>=50; daytime; 1-in-20 dice; at most 10 buddies within 16/8/16.", [
        "if (this.getY() < 50.0) return false;",
        "if (!OriginalSpawnGates.isDaytime(level)) return false;",
        "if (this.random.nextInt(20) != 1) return false;",
        "return OriginalSpawnGates.countBuddies(this, level, Flounder.class, 16.0, 8.0, 16.0) <= 10;",
    ]),
    "Frog.java": ("orig Frog.java:240-251 — y>=50; daytime; extra 1-in-20 dice in Crystal; at most 5 buddies within 20/8/20.", [
        "if (this.getY() < 50.0) return false;",
        "if (!OriginalSpawnGates.isDaytime(level)) return false;",
        "if (danger.orespawn.ModDimensionKeys.isIn(level, danger.orespawn.ModDimensionKeys.CRYSTAL)",
        "        && this.random.nextInt(20) != 1) return false;",
        "return OriginalSpawnGates.countBuddies(this, level, Frog.class, 20.0, 8.0, 20.0) <= 5;",
    ]),
    "Ghost.java": ("orig Ghost.java:145-160 — \"Ghost\" spawner bypass (x/z -2..+1); night.", [
        "if (OriginalSpawnGates.nearOwnSpawner(this, level, -2, 1, 0, 4)) return true;",
        "return !OriginalSpawnGates.isDaytime(level);",
    ]),
    "GhostSkelly.java": ("orig GhostSkelly.java:173-188 — \"Ghost Pumpkin Skelly\" spawner bypass (x/z -2..+1); night.", [
        "if (OriginalSpawnGates.nearOwnSpawner(this, level, -2, 1, 0, 4)) return true;",
        "return !OriginalSpawnGates.isDaytime(level);",
    ]),
    "Hammerhead.java": ("orig Hammerhead.java:277-316 — \"Hammerhead\" spawner bypass; darkness; y>=50; night; clear-air column; no other Hammerhead within 16/8/16.", [
        "if (OriginalSpawnGates.nearOwnSpawner(this, level)) return true;",
        "if (!OriginalSpawnGates.isDarkEnough(this, level)) return false;",
        "if (this.getY() < 50.0) return false;",
        "if (OriginalSpawnGates.isDaytime(level)) return false;",
        "if (!OriginalSpawnGates.airBox(this, level, -1, 0, 1, 5, -1, 0)) return false;",
        "return !OriginalSpawnGates.anyOtherNearby(this, level, Hammerhead.class, 16.0, 8.0, 16.0);",
    ]),
    "Irukandji.java": ("orig Irukandji.java:326-337 — y>=50; daytime; 1-in-60 dice; at most 2 buddies within 16/8/16.", [
        "if (this.getY() < 50.0) return false;",
        "if (!OriginalSpawnGates.isDaytime(level)) return false;",
        "if (this.random.nextInt(60) != 1) return false;",
        "return OriginalSpawnGates.countBuddies(this, level, Irukandji.class, 16.0, 8.0, 16.0) <= 2;",
    ]),
    "Ostrich.java": ("orig Ostrich.java:325-338 — y>=50; daytime; 1-in-4 dice; no other Ostrich within 16/6/16.", [
        "if (this.getY() < 50.0) return false;",
        "if (!OriginalSpawnGates.isDaytime(level)) return false;",
        "if (this.random.nextInt(4) != 1) return false;",
        "return !OriginalSpawnGates.anyOtherNearby(this, level, Ostrich.class, 16.0, 6.0, 16.0);",
    ]),
    "SeaMonster.java": ("orig SeaMonster.java:544-570 — \"Sea Monster\" spawner bypass; y>=50; night; darkness; no other SeaMonster within 16/5/16.", [
        "if (OriginalSpawnGates.nearOwnSpawner(this, level)) return true;",
        "if (this.getY() < 50.0) return false;",
        "if (OriginalSpawnGates.isDaytime(level)) return false;",
        "if (!OriginalSpawnGates.isDarkEnough(this, level)) return false;",
        "return !OriginalSpawnGates.anyOtherNearby(this, level, SeaMonster.class, 16.0, 5.0, 16.0);",
    ]),
    "SeaViper.java": ("orig SeaViper.java:561-584 — \"Sea Viper\" spawner bypass; y>=50; daytime; no other SeaViper within 16/5/16.", [
        "if (OriginalSpawnGates.nearOwnSpawner(this, level)) return true;",
        "if (this.getY() < 50.0) return false;",
        "if (!OriginalSpawnGates.isDaytime(level)) return false;",
        "return !OriginalSpawnGates.anyOtherNearby(this, level, SeaViper.class, 16.0, 5.0, 16.0);",
    ]),
    "Skate.java": ("orig Skate.java:318-329 — y>=50; daytime; 1-in-30 dice; at most 6 buddies within 16/8/16.", [
        "if (this.getY() < 50.0) return false;",
        "if (!OriginalSpawnGates.isDaytime(level)) return false;",
        "if (this.random.nextInt(30) != 1) return false;",
        "return OriginalSpawnGates.countBuddies(this, level, Skate.class, 16.0, 8.0, 16.0) <= 6;",
    ]),
    "VelocityRaptor.java": ("orig VelocityRaptor.java:78-83 — y>=50; daytime.", [
        "if (this.getY() < 50.0) return false;",
        "return OriginalSpawnGates.isDaytime(level);",
    ]),
    "WaterDragon.java": ("orig WaterDragon.java:716-739 — \"Water Dragon\" spawner bypass; y>=50; daytime; no other WaterDragon within 16/5/16.", [
        "if (OriginalSpawnGates.nearOwnSpawner(this, level)) return true;",
        "if (this.getY() < 50.0) return false;",
        "if (!OriginalSpawnGates.isDaytime(level)) return false;",
        "return !OriginalSpawnGates.anyOtherNearby(this, level, WaterDragon.class, 16.0, 5.0, 16.0);",
    ]),
    "Whale.java": ("orig Whale.java:260-271 — y>=50; daytime; 1-in-50 dice; no buddies within 32/8/32.", [
        "if (this.getY() < 50.0) return false;",
        "if (!OriginalSpawnGates.isDaytime(level)) return false;",
        "if (this.random.nextInt(50) != 1) return false;",
        "return OriginalSpawnGates.countBuddies(this, level, Whale.class, 32.0, 8.0, 32.0) <= 0;",
    ]),
}

PATTERN = re.compile(
    r"(/\*\*(?:[^*]|\*(?!/))*?\*/\s+)?@Override\s+public boolean checkSpawnRules\((?:[^{])*?\{.*?\n    \}",
    re.S)

for filename, (citation, body) in GATES.items():
    path = SRC / filename
    text = path.read_text(encoding="utf-8")
    m = PATTERN.search(text)
    assert m, f"no existing gate found in {filename}"
    repl = ("/** " + citation + " */\n"
            "    @Override\n"
            "    public boolean checkSpawnRules(net.minecraft.world.level.LevelAccessor level,\n"
            "                                   net.minecraft.world.entity.MobSpawnType spawnType) {\n")
    for line in body:
        repl += "        " + line + "\n"
    repl += "    }"
    new_text = text[:m.start()] + repl + text[m.end():]
    path.write_text(new_text, encoding="utf-8")
    print("replaced:", filename)

print("done:", len(GATES))
