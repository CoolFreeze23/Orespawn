"""D1 batch 2: insert the remaining original func_70601_bi spawn-gate ports as
checkSpawnRules overrides. EntityVortex is edited by hand (was_spawnered side
effect, like Rotator). Same conventions as insert_spawn_gates.py.
"""
import pathlib

SRC = pathlib.Path("src/main/java/danger/orespawn/entity")

SIG = ("    @Override\n"
       "    public boolean checkSpawnRules(net.minecraft.world.level.LevelAccessor level,\n"
       "                                   net.minecraft.world.entity.MobSpawnType spawnType) {\n")
END = "    }\n"

GATES = {
    "Boyfriend.java": ("orig Boyfriend.java:978-993 — \"Boyfriend\" spawner bypass, else the vanilla creature rules.", [
        "if (OriginalSpawnGates.nearOwnSpawner(this, level)) return true;",
        "return super.checkSpawnRules(level, spawnType);",
    ]),
    "Girlfriend.java": ("orig Girlfriend.java:1100-1115 — \"Girlfriend\" spawner bypass, else the vanilla creature rules.", [
        "if (OriginalSpawnGates.nearOwnSpawner(this, level)) return true;",
        "return super.checkSpawnRules(level, spawnType);",
    ]),
    "Dragon.java": ("orig Dragon.java:598-611 — daytime; no other Dragon within 16/6/16; Islands always allowed; otherwise y>=50.", [
        "if (!OriginalSpawnGates.isDaytime(level)) return false;",
        "if (OriginalSpawnGates.anyOtherNearby(this, level, Dragon.class, 16.0, 6.0, 16.0)) return false;",
        "if (danger.orespawn.ModDimensionKeys.isIn(level, danger.orespawn.ModDimensionKeys.ISLANDS)) return true;",
        "return this.getY() >= 50.0;",
    ]),
    "DungeonBeast.java": ("orig DungeonBeast.java:275-312 — \"Dungeon Beast\" spawner bypass; darkness; in Crystal only 25<=y<=28 with >=6 air blocks in the 3x3 ring one above the feet.", [
        "if (OriginalSpawnGates.nearOwnSpawner(this, level)) return true;",
        "if (!OriginalSpawnGates.isDarkEnough(this, level)) return false;",
        "if (danger.orespawn.ModDimensionKeys.isIn(level, danger.orespawn.ModDimensionKeys.CRYSTAL)) {",
        "    if (this.getY() > 28.0 || this.getY() < 25.0) return false;",
        "    int sc = 0;",
        "    net.minecraft.core.BlockPos feet = this.blockPosition();",
        "    for (int dz = -1; dz <= 1; dz++) {",
        "        for (int dx = -1; dx <= 1; dx++) {",
        "            if (level.getBlockState(feet.offset(dx, 1, dz)).isAir()) sc++;",
        "        }",
        "    }",
        "    if (sc < 6) return false;",
        "}",
        "return true;",
    ]),
    "EasterBunny.java": ("orig EasterBunny.java:67-77 — y>=50; daytime; no other EasterBunny within 32/8/32.", [
        "if (this.getY() < 50.0) return false;",
        "if (!OriginalSpawnGates.isDaytime(level)) return false;",
        "return !OriginalSpawnGates.anyOtherNearby(this, level, EasterBunny.class, 32.0, 8.0, 32.0);",
    ]),
    "EntityEmperorScorpion.java": ("orig EmperorScorpion.java:529-559 — combined scan of x/z -2..+1, y +2..+4: own spawner anywhere in the box passes, any non-air block fails; then darkness; night; y>=50; no other EmperorScorpion within 20/6/20.", [
        "net.minecraft.core.BlockPos feet = this.blockPosition();",
        "for (int dz = -2; dz <= 1; dz++) {",
        "    for (int dx = -2; dx <= 1; dx++) {",
        "        for (int dy = 2; dy <= 4; dy++) {",
        "            net.minecraft.core.BlockPos p = feet.offset(dx, dy, dz);",
        "            if (OriginalSpawnGates.isOwnSpawner(this, level, p)) return true;",
        "            if (!level.getBlockState(p).isAir()) return false;",
        "        }",
        "    }",
        "}",
        "if (!OriginalSpawnGates.isDarkEnough(this, level)) return false;",
        "if (OriginalSpawnGates.isDaytime(level)) return false;",
        "if (this.getY() < 50.0) return false;",
        "return !OriginalSpawnGates.anyOtherNearby(this, level, EntityEmperorScorpion.class, 20.0, 6.0, 20.0);",
    ]),
    "EnderKnight.java": ("orig EnderKnight.java:256-277 — \"Ender Knight\" spawner bypass; darkness; night; y>=30.", [
        "if (OriginalSpawnGates.nearOwnSpawner(this, level)) return true;",
        "if (!OriginalSpawnGates.isDarkEnough(this, level)) return false;",
        "if (OriginalSpawnGates.isDaytime(level)) return false;",
        "return this.getY() >= 30.0;",
    ]),
    "EnderReaper.java": ("orig EnderReaper.java:253-279 — \"Ender Reaper\" spawner bypass; darkness; night; y>=30; no other EnderReaper within 16/8/16.", [
        "if (OriginalSpawnGates.nearOwnSpawner(this, level)) return true;",
        "if (!OriginalSpawnGates.isDarkEnough(this, level)) return false;",
        "if (OriginalSpawnGates.isDaytime(level)) return false;",
        "if (this.getY() < 30.0) return false;",
        "return !OriginalSpawnGates.anyOtherNearby(this, level, EnderReaper.class, 16.0, 8.0, 16.0);",
    ]),
    "EntityGammaMetroid.java": ("orig GammaMetroid.java:328-365 — spawner bypass (orig tag \"WTF?\"); darkness; Islands always allowed; y<=50; clear air above.", [
        "if (OriginalSpawnGates.nearOwnSpawner(this, level)) return true;",
        "if (!OriginalSpawnGates.isDarkEnough(this, level)) return false;",
        "if (danger.orespawn.ModDimensionKeys.isIn(level, danger.orespawn.ModDimensionKeys.ISLANDS)) return true;",
        "if (this.getY() > 50.0) return false;",
        "return OriginalSpawnGates.airBox(this, level, -1, 0, 1, 3, -1, 0);",
    ]),
    "EntityHerculesBeetle.java": ("orig HerculesBeetle.java:442-481 — \"Hercules Beetle\" spawner bypass; darkness; night; y>=50; clear-air box; no other HerculesBeetle within 16/6/16.", [
        "if (OriginalSpawnGates.nearOwnSpawner(this, level)) return true;",
        "if (!OriginalSpawnGates.isDarkEnough(this, level)) return false;",
        "if (OriginalSpawnGates.isDaytime(level)) return false;",
        "if (this.getY() < 50.0) return false;",
        "if (!OriginalSpawnGates.airBox(this, level, -2, 1, 2, 4, -2, 1)) return false;",
        "return !OriginalSpawnGates.anyOtherNearby(this, level, EntityHerculesBeetle.class, 16.0, 6.0, 16.0);",
    ]),
    "EntityKyuubi.java": ("orig Kyuubi.java:222-224 — always allowed.", [
        "return true;",
    ]),
    "EntityLeon.java": ("orig Leon.java:452-478 — \"Leonopteryx\" spawner bypass; 1-in-16 dice; daytime; no other Leon within 48/16/48; y>=50.", [
        "if (OriginalSpawnGates.nearOwnSpawner(this, level)) return true;",
        "if (this.getRandom().nextInt(16) != 0) return false;",
        "if (!OriginalSpawnGates.isDaytime(level)) return false;",
        "if (OriginalSpawnGates.anyOtherNearby(this, level, EntityLeon.class, 48.0, 16.0, 48.0)) return false;",
        "return this.getY() >= 50.0;",
    ]),
    "Pointysaurus.java": ("orig Pointysaurus.java:275-312 — \"Pointysaurus\" spawner bypass; darkness; y>=50; night; clear-air column.", [
        "if (OriginalSpawnGates.nearOwnSpawner(this, level)) return true;",
        "if (!OriginalSpawnGates.isDarkEnough(this, level)) return false;",
        "if (this.getY() < 50.0) return false;",
        "if (OriginalSpawnGates.isDaytime(level)) return false;",
        "return OriginalSpawnGates.airBox(this, level, -1, 0, 1, 5, -1, 0);",
    ]),
    "PurplePower.java": ("orig PurplePower.java:226-228 — always allowed.", [
        "return true;",
    ]),
    "RockBase.java": ("orig RockBase.java:191-193 — y>=50.", [
        "return this.getY() >= 50.0;",
    ]),
    "RubyBird.java": ("orig RubyBird.java:29-31 — always allowed.", [
        "return true;",
    ]),
    "EntitySpitBug.java": ("orig SpitBug.java:396-430 — \"Spit Bug\" spawner bypass; daytime only on a 2-in-20 dice; darkness; clear-air box.", [
        "if (OriginalSpawnGates.nearOwnSpawner(this, level)) return true;",
        "if (OriginalSpawnGates.isDaytime(level) && this.getRandom().nextInt(20) > 1) return false;",
        "if (!OriginalSpawnGates.isDarkEnough(this, level)) return false;",
        "return OriginalSpawnGates.airBox(this, level, -2, 1, 1, 3, -2, 1);",
    ]),
    "EntitySpyro.java": ("orig Spyro.java:407-412 — daytime; y>=50.", [
        "if (!OriginalSpawnGates.isDaytime(level)) return false;",
        "return this.getY() >= 50.0;",
    ]),
    "EntityStinky.java": ("orig Stinky.java:286-291 — daytime; at most 2 buddies within 20/10/20 (findBuddies, Stinky.java:705-708).", [
        "if (!OriginalSpawnGates.isDaytime(level)) return false;",
        "return OriginalSpawnGates.countBuddies(this, level, EntityStinky.class, 20.0, 10.0, 20.0) <= 2;",
    ]),
    "TRex.java": ("orig TRex.java:276-315 — \"T. Rex\" spawner bypass; darkness; y>=50; night; clear-air column; no other TRex within 24/12/24.", [
        "if (OriginalSpawnGates.nearOwnSpawner(this, level)) return true;",
        "if (!OriginalSpawnGates.isDarkEnough(this, level)) return false;",
        "if (this.getY() < 50.0) return false;",
        "if (OriginalSpawnGates.isDaytime(level)) return false;",
        "if (!OriginalSpawnGates.airBox(this, level, -1, 1, 1, 5, -1, 1)) return false;",
        "return !OriginalSpawnGates.anyOtherNearby(this, level, TRex.class, 24.0, 12.0, 24.0);",
    ]),
    "EntityTerribleTerror.java": ("orig TerribleTerror.java:193-214 — \"Terrible Terror\" spawner bypass (x/z -2..+1); darkness; night; Chaos always allowed, otherwise y<=40.", [
        "if (OriginalSpawnGates.nearOwnSpawner(this, level, -2, 1, 0, 4)) return true;",
        "if (!OriginalSpawnGates.isDarkEnough(this, level)) return false;",
        "if (OriginalSpawnGates.isDaytime(level)) return false;",
        "if (danger.orespawn.ModDimensionKeys.isIn(level, danger.orespawn.ModDimensionKeys.CHAOS)) return true;",
        "return this.getY() <= 40.0;",
    ]),
    "TheKing.java": ("orig TheKing.java:847-849 — always allowed.", [
        "return true;",
    ]),
    "ThePrince.java": ("orig ThePrince.java:381-383 — always allowed.", [
        "return true;",
    ]),
    "ThePrinceAdult.java": ("orig ThePrinceAdult.java:541-543 — never spawns naturally (growth stage, spawned by promotion only).", [
        "return false;",
    ]),
    "ThePrinceTeen.java": ("orig ThePrinceTeen.java:561-563 — never spawns naturally (growth stage, spawned by promotion only).", [
        "return false;",
    ]),
    "ThePrincess.java": ("orig ThePrincess.java:369-371 — always allowed.", [
        "return true;",
    ]),
    "TheQueen.java": ("orig TheQueen.java:813-815 — always allowed.", [
        "return true;",
    ]),
    "EntityTriffid.java": ("orig Triffid.java:355-357 — always allowed.", [
        "return true;",
    ]),
    "EntityTrooperBug.java": ("orig TrooperBug.java:536-570 — \"Jumpy Bug\" spawner bypass; darkness; daytime only on a 2-in-20 dice; clear-air box.", [
        "if (OriginalSpawnGates.nearOwnSpawner(this, level)) return true;",
        "if (!OriginalSpawnGates.isDarkEnough(this, level)) return false;",
        "if (OriginalSpawnGates.isDaytime(level) && this.getRandom().nextInt(20) > 1) return false;",
        "return OriginalSpawnGates.airBox(this, level, -2, 1, 1, 4, -2, 1);",
    ]),
    "EntityWormMedium.java": ("orig WormMedium.java:240-242 — night only.", [
        "return !OriginalSpawnGates.isDaytime(level);",
    ]),
    "EntityWormSmall.java": ("orig WormSmall.java:214-216 — night only.", [
        "return !OriginalSpawnGates.isDaytime(level);",
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
