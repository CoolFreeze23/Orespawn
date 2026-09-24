# OreSpawn for NeoForge 1.21.1 — 2.0.0-beta.11

Version `1.21.1-2.0.0-beta.11` (`gradle.properties`; `README.md` line 6), cut on 2026-09-24 on beta.10 for tidier config text. Release: https://github.com/CoolFreeze23/Orespawn/releases/tag/v1.21.1-2.0.0-beta.11

## Part one — for the player

A tidy-up release: the option descriptions in the config file read in plain words. Nothing in the game plays differently.

**Changed**
- Three option descriptions in `config/orespawn-common.toml` are rewritten in plain words: the fireball fire rule (`fireRespectsMobGriefing`), the Chainsaw's sight check (`chainsawSweepVanillaSight`) and the artist animations switch (`artistAnimations`).

![The three config options as they read in beta.11](https://raw.githubusercontent.com/CoolFreeze23/Orespawn/v1.21.1-2.0.0-beta.11/phase_g_reports/release_media/2.0.0-beta.11/config_descriptions.png)

*How the three options read in `config/orespawn-common.toml` from beta.11 on.*

**Good to know**
- Your settings stay as they are: the game rewrites only the descriptions in your existing config file, the first time it loads it.
- Works in the worlds you already have.

**Install:** put `orespawn-1.21.1-2.0.0-beta.11.jar` in `mods/` (NeoForge 21.1, Minecraft 1.21.1, GeckoLib 4.7 or newer) and take the beta.10 jar out. Worlds carry over.

### In more detail

#### What beta.11 is

beta.11 is beta.10 with its text tidied: the three config descriptions above, the notes carried in the multi-part hit box profiles and the model data, and the comments in the source. No behaviour changes.

#### How to install

Put `orespawn-1.21.1-2.0.0-beta.11.jar` into the `mods` folder of a NeoForge 21.1 instance for Minecraft 1.21.1 together with GeckoLib 4.7 or newer, and take the beta.10 jar out; MultiHitboxLib and Databuddy are bundled in the jar. Existing worlds and config files carry over.

## Part two — for the modder

### The config descriptions

NeoForge rewrites a config file's comments from the spec when it loads the file (a comment that differs from the spec's is corrected and the file saved), so an existing `orespawn-common.toml` picks up the new descriptions of `modern.fireRespectsMobGriefing` (MOD-031), `modern.chainsawSweepVanillaSight` (MOD-037) and `modern.artistAnimations` (MOD-038) on the first load, with every value kept. The keys, their defaults and what they do are unchanged.

### The harness

Checks on the release tree: drift 0; `RESULT: 0 error(s), 0 advisory(ies), 4 acknowledged; draw order: 104 shipped geo: 103 seam + 1 outside-seam`, `G1 PARITY PASS` 2, 13 and 101 models, checked-in proof verified, `BUILD SUCCESSFUL in 7m 5s`; the gametest suite: `All 1338 required tests passed`, `BUILD SUCCESSFUL in 2m 8s` — the same 1338 tests as beta.10.

### Third-party notices and credits

Unchanged from beta.5: see `phase_g_reports/RELEASE_NOTES_2.0.0-beta.5.md`, "Third-party notices" and "Credits" (MultiHitboxLib under LGPL-3.0, the MoreHitboxes portions under MIT, GeckoLib under MIT, Databuddy under MIT; the original mod by TheyCallMeDanger and the OreSpawn authors, 2013-2015).
