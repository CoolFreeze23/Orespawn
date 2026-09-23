# OreSpawn for NeoForge 1.21.1 — 2.0.0-beta.10

Version `1.21.1-2.0.0-beta.10` (`gradle.properties`; `README.md` line 6), cut on 2026-09-24 on beta.9 for the pair's swing timer. Release: https://github.com/CoolFreeze23/Orespawn/releases/tag/v1.21.1-2.0.0-beta.10

## Part one — for the player

The Girlfriend and the Boyfriend fight properly again: they swing their arms when they strike, throw or dance, and they keep throwing shoes through a fight.

**Fixed**
- The Girlfriend and the Boyfriend keep throwing shoes at their target through the whole fight. Before, their first swing (a hit, a throw or a dance move) never finished, and they never threw again after it.
- Their arms swing again when they hit, throw a shoe or dance. That swing was never drawn before.

![Before and after: the Girlfriend throwing shoes on beta.9 and on beta.10](https://raw.githubusercontent.com/CoolFreeze23/Orespawn/v1.21.1-2.0.0-beta.10/phase_g_reports/release_media/2.0.0-beta.10/pair_throws_before_after.gif)

*Top, beta.9: one shoe, then she only watches. Bottom, beta.10: she keeps throwing, her arm swinging with each shoe.*

**Good to know**
- A hit that comes right after a throw lands without a second arm swing, the way every Minecraft mob's swing works.
- Works in the worlds you already have.

**Install:** put `orespawn-1.21.1-2.0.0-beta.10.jar` in `mods/` (NeoForge 21.1, Minecraft 1.21.1, GeckoLib 4.7 or newer) and take the beta.9 jar out. Worlds carry over.

### In more detail

#### What beta.10 is

beta.10 is beta.9 plus one fix, ENT-S-175, the pair's swing timer, found while adding Better Combat attack animations for the pair in the OreSpawn Integrations companion mod. From this version on, every version's notes carry pictures: a before and after for what you can see change, and a short animation where the change is a motion.

#### What changed

The Girlfriend and the Boyfriend never finished a swing. Minecraft 1.21.1 only moves the swing along for monsters and players, and the pair are tameable animals; the 1.7.10 original moved it themselves, every tick, and the port had left that out. After their first swing they were stuck mid-swing forever: the arm never moved on screen, and the check that stops a throw in the middle of a swing refused every throw after that. They now move their own swing along each tick, as in 1.7.10, so a swing plays out and ends after six ticks, and the next throw comes when their throwing rhythm allows.

#### How to install

Put `orespawn-1.21.1-2.0.0-beta.10.jar` into the `mods` folder of a NeoForge 21.1 instance for Minecraft 1.21.1 together with GeckoLib 4.7 or newer, and take the beta.9 jar out; MultiHitboxLib and Databuddy are bundled in the jar. Existing worlds carry over.

## Part two — for the modder

### The pair's swing timer

1.21.1 calls `LivingEntity.updateSwingTime()` only from `Monster.aiStep()`, `Player.serverAiStep()` and the client's `RemotePlayer.aiStep()`; `LivingEntity`, `Mob`, `PathfinderMob`, `AgeableMob`, `Animal` and `TamableAnimal` never call it. The Girlfriend and the Boyfriend extend `TamableAnimal`, and the original ticks the swing itself at the head of `onLivingUpdate`, before its super call (orig Girlfriend.java:577 and Boyfriend.java:496, `func_82168_bl`, `updateArmSwingProgress`; the only two OreSpawn classes that call it). Without it, `swing()` left `swinging` true and `swingTime` at -1 on both sides for good: `attackAnim` stayed 0, so neither the classic renderer's nor the rig's hook drew the arm swing of a hit, a throw or a dance, and `performRangedAttack`'s `if (this.swinging) return;` (orig Girlfriend.java:977, Boyfriend.java:876) refused every throw after the first swing. Both `aiStep` overrides now call `this.updateSwingTime()` first, before `super.aiStep()`, as the original orders it. Every OreSpawn class whose 1.7.10 base ticked the swing (54, all through `EntityMob`) is a `Monster` in the port, so no other species was left without it.

With the timer running, vanilla's own restart guard in `LivingEntity.swing` applies to the pair: a swing restarts only from its second half, so a `swing()` within three ticks of another (a melee hit right after a throw, or the reverse) sends no animate packet; the hit and the throw themselves are unconditional. A client mod that plays its own animation per swing should key it on the animate packet (`LivingEntity.swing` on the client) rather than on the `swinging` field, which on beta.9 and earlier never cleared for the pair.

Six gametests, `PairSwingTests` s175a-f in the batches `pairSwingGirlfriend` and `pairSwingBoyfriend`: for each of the pair, the swing advances one step a tick and ends on the seventh update (a second tick per `aiStep` fails the pin at six); a shoe is thrown once a swing has ended; the guard still refuses a throw mid-swing. The shoes are counted around the thrower, since on 14 February the Girlfriend is the 2.5x8 giant and throws from above the test structure.

### The harness

Checks on the release tree: drift 0; `RESULT: 0 error(s), 0 advisory(ies), 4 acknowledged; draw order: 104 shipped geo: 103 seam + 1 outside-seam`, `G1 PARITY PASS` 2, 13 and 101 models, checked-in proof verified, `BUILD SUCCESSFUL in 9m 42s`; the gametest suite: `All 1338 required tests passed`, `BUILD SUCCESSFUL in 2m 52s` — 1332 tests at beta.9 plus the six pinning ENT-S-175.

### Third-party notices and credits

Unchanged from beta.5: see `phase_g_reports/RELEASE_NOTES_2.0.0-beta.5.md`, "Third-party notices" and "Credits" (MultiHitboxLib under LGPL-3.0, the MoreHitboxes portions under MIT, GeckoLib under MIT, Databuddy under MIT; the original mod by TheyCallMeDanger and the OreSpawn authors, 2013-2015).
