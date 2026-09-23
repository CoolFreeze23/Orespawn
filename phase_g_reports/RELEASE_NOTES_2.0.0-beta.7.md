# OreSpawn for NeoForge 1.21.1 — 2.0.0-beta.7

Version `1.21.1-2.0.0-beta.7` (`gradle.properties`; `README.md` line 6), cut on 2026-09-20 on beta.6 for the despawn rule. Release: https://github.com/CoolFreeze23/Orespawn/releases/tag/v1.21.1-2.0.0-beta.7

## Part one — for the player

One fix: creatures you leave behind despawn again, the way they did in 1.7.10. That ends the frog pile-up in Utopia that beta.6 had left in place.

**Fixed**
- Frogs, crickets and T-shirts no longer build up forever. Once you are more than 128 blocks away they despawn, as in the original; the ones near you stay.
- Hoverboards, Ant Robots, Spider Robots, the worms, the Purple Power, the Rock Base and the boss heads no longer vanish when you walk away from them.

**Changed**
- Wild, grown Baryonyx, Cassowary, Flounder, Stink Bug and Whale despawn when left behind. Their young stay for good.
- Wild adult Camarasaurus, Chipmunk and Water Dragon despawn when left behind. Tamed ones stay.
- A wild, unridden Prince (teen or adult) despawns when left behind. Tame or name-tag the ones you want to keep.
- Gold Fish despawn only at night. Pitch Black, Creeping Horror and Firefly only by day. The Lurking Terror only while it is not attacking.
- A baby Easter Bunny, Peacock, Lizard, Ostrich, Rubber Ducky or Velocity Raptor is kept for good once the game has checked on it, as the original kept it.

**Good to know**
- Works in the worlds you already have, chunk by chunk as they load.
- Nothing else changed in this build.

**Install:** put `orespawn-1.21.1-2.0.0-beta.7.jar` in `mods/` (NeoForge 21.1, Minecraft 1.21.1, GeckoLib 4.7 or newer) and take the beta.6 jar out. Worlds carry over.

### In more detail

#### What beta.7 is

beta.7 is beta.6 plus one rule: creatures you leave behind despawn as they did in 1.7.10, which ends the frog pile-up in Utopia that beta.6's placement fix had left in place. Nothing else moves. It applies to creatures already in your world as their chunks load: the ones far from you go, the ones near you stay.

#### What changed

- **Left-behind creatures despawn as they did in 1.7.10.** Frogs, crickets and T-shirts never despawned in the port, so
  Utopia filled with every frog that ever spawned near you (2,753 in one eleven-minute session); beyond 128 blocks they
  despawn again, as in the original, and the ones near you stay. The same rule, transcribed from each original, now
  covers the wild, grown Baryonyx, Cassowary, Flounder, Stink Bug and Whale (their young stay), the wild adult
  Camarasaurus, Chipmunk and Water Dragon (tamed ones stay), and a wild, unridden Prince teen or adult. In the other
  direction the worms, the Ant and Spider Robots, the hoverboard, the Purple Power, the Rock Base and the boss heads no
  longer vanish when left behind. The Gold Fish despawns only by night; the Pitch Black, the Creeping Horror and the
  Firefly only by day; the Lurking Terror only while idle. A young Easter Bunny, Peacock, Lizard, Ostrich, Rubber Ducky
  or Velocity Raptor that meets the check is kept for good, as the original kept it. *(ENT-S-171)*

#### How to install

Put `orespawn-1.21.1-2.0.0-beta.7.jar` into the `mods` folder of a NeoForge 21.1 instance for Minecraft 1.21.1 together with GeckoLib 4.7 or newer, and take the beta.6 jar out; MultiHitboxLib and Databuddy are bundled in the jar. Existing worlds carry over.

## Part two — for the modder

### The despawn rule

1.7.10 asked every mob `canDespawn()` from `EntityLiving.despawnEntity`: the persistent were kept; the rest were removed at once beyond 128 blocks from the nearest player, and at one chance in 800 per tick beyond 32 blocks after 600 idle ticks. 1.21.1's `Mob.checkDespawn` has the same structure and distances and asks `removeWhenFarAway` instead; the vanilla defaults differ by class (Mob and Monster true, Animal and TamableAnimal false), and OreSpawn's originals override the rule in 109 classes. The port carried 63 of those overrides. A sweep of all 109 against the port's effective answer, completed by review for the multi-clause originals, found 35 mismatches, each now an override transcribed clause for clause with its original line cited: thirteen animals the original despawns when wild and grown (the Frog, the Cricket and the T-shirt outright; the Baryonyx, Cassowary, Flounder, Stink Bug and Whale with the original's child clause, which makes a child persistent for life; the Camarasaurus, Chipmunk and Water Dragon with the child and tamed clauses; the Prince's teen and adult forms with the ridden and tamed clauses, in place of an uncited never-despawn from the port's first commit); eleven the original never despawns and the port's Mob or Monster default did (the three worms, the Ant Robot, the Spider Robot, the hoverboard, the Purple Power, the Rock Base, the Godzilla, King and Queen heads — a parked hoverboard was being removed 128 blocks out); five rules of clock and state (the Gold Fish by night only; the Pitch Black, the Creeping Horror and the Firefly by day only, the Firefly's uncited open-sky rule gone; the Lurking Terror only while not attacking); and six child clauses completed (the Easter Bunny's and the Peacock's restored; the Lizard's, the Ostrich's, the Rubber Ducky's and the Velocity Raptor's given back the original's `setPersistenceRequired()`). `Level.isDay()` stands for 1.7.10 `World.isDaytime()`; no port dimension type sets a fixed time. The frog case in numbers: a Utopia test save on beta.6 held 2,753 Frogs after eleven minutes with at most 11 in a chunk, so beta.6's placement and the original's five-buddies cap held and the pile-up was the ones left behind. Record: ENT-S-171. Pins: `DespawnRuleTests` — the thirteen that despawn, the eleven that never do, the tamed, ridden and attacking clauses, the four clock rules against the level's `isDay`, the child clause on all fourteen that carry it; every entity built unspawned, since the test helper's `spawn` marks mobs persistent.

### The harness

Checks on the release tree: drift 0; `RESULT: 0 error(s), 0 advisory(ies), 4 acknowledged; draw order: 104 shipped geo: 103 seam + 1 outside-seam`, `G1 PARITY PASS` 2, 13 and 101 models, checked-in proof verified, `BUILD SUCCESSFUL in 10m 1s`; the gametest suite: `All 1309 required tests passed`, `BUILD SUCCESSFUL in 4m 42s` — 1304 tests before this cut plus the five pinning the despawn rule.

### Third-party notices and credits

Unchanged from beta.5: see `phase_g_reports/RELEASE_NOTES_2.0.0-beta.5.md`, "Third-party notices" and "Credits" (MultiHitboxLib under LGPL-3.0, the MoreHitboxes portions under MIT, GeckoLib under MIT, Databuddy under MIT; the original mod by TheyCallMeDanger and the OreSpawn authors, 2013-2015).
