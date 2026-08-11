# OreSpawn — NeoForge 1.21.1 Port

> *"Just plain fun!"* — the classic 1.7.10 OreSpawn, rebuilt for modern
> Minecraft with 100% source-verified parity.

**Version:** 1.21.1-1.0.0-beta.3 · **Loader:** NeoForge 21.1+ · **Minecraft:** 1.21.1
**Status:** public beta — gameplay-complete, visual/audio polish in community review

---

## What is OreSpawn?

One of the most-played Minecraft mods of the 1.6/1.7 era: giant bosses
(Mobzilla, The King, the Kraken), six new dimensions reached by
right-clicking ants, the Big Bertha sword family, Girlfriends and
Boyfriends, dinosaurs, robot armies, and an ore-to-endgame power curve that
defined "overpowered fun" for a generation of players. The original, by
**TheyCallMeDanger**, stopped at Minecraft 1.7.10.

## What is this port?

An unofficial **preservation port** to NeoForge 1.21.1. Not a re-imagining:
the goal was the original, exactly — every stat, drop table, spawn rule, AI
quirk, and yes, every famous bug, verified line-by-line against the
original's source. 630 audited findings tracked to closure; a 150-test
automated regression suite guards the result.

- Faithful first: if 1.7.10 did something weird, this port does it too.
  Deliberate improvements are a separate, clearly-labeled 2.0 backlog.
- Modern underneath: data-driven recipes, loot, worldgen, and spawning;
  1.21.1-idiomatic code; the original's worst per-tick performance costs
  removed without behavior changes.

## Installing

1. Install [NeoForge](https://neoforged.net/) for Minecraft **1.21.1**
   (any 21.1.x).
2. Drop the OreSpawn jar into your `mods/` folder.
3. New world recommended for the beta. Existing 1.7.10 worlds are **not**
   upgradable (13 Minecraft versions apart); existing *port-beta* worlds
   carry forward.

## What you get

*(screenshots coming — placeholders below)*

| | |
|---|---|
| **Six dimensions** | Utopia, Mining, Village, Crystal, Islands, Chaos — via the dimension ants. *(screenshot)* |
| **Boss gauntlet** | Mobzilla, The King & Queen, Kraken, Mothra, Basilisk, the Prince line. *(screenshot)* |
| **The arsenal** | Big Bertha to Ultimate gear, the gemstone armory, Royal Guardian glide. *(screenshot)* |
| **Companions** | Girlfriends, Boyfriends, tameable everything, rideable robots and dragons. *(screenshot)* |
| **A living world** | 47 structures, wild crops, anthills, troll blocks, boosted ores. *(screenshot)* |

## Beta status — read this

The **game logic** is done and machine-verified. The **look and sound** of
~40 items (model scales, custom audio, ride feel, boss presentation) shipped
faithful-by-construction but await human eyes — that's this beta's job, and
yours if you want to help. [KNOWN_ISSUES.md](KNOWN_ISSUES.md) lists every
open question, what's already fixed, and which original quirks are *supposed*
to look wrong. The one known generation defect (sheared Fairy Castle Trees
in the Crystal dimension, ~1 in 25 chunks) is the designated first
post-beta patch.

## Roadmap

- **1.0.0**: after the castle-tree patch and the community visual pass.
- **2.0 "OreSpawn Modernized"** ([the backlog](MODERNIZATION_NOTES.md)):
  procedural spider with true multi-part hitboxes, bone-synced boss
  hitboxes, config toggles for the original's roughest edges, and the
  archived kyanite branch as optional content. Parity stays the default.

## License & Ownership

**OreSpawn — its code, art, and sounds — is the work of TheyCallMeDanger
and the original authors. All rights to the original remain theirs.**

This is an unofficial preservation port, published so the mod survives on
modern Minecraft; **permission is being sought** from the original authors.
No ownership of the original work is claimed by anyone involved in the
port. The port ships the original's textures and sounds (1,976 original
asset files, inventoried in
[provenance_byte_identical_assets.txt](provenance_byte_identical_assets.txt))
and, honestly noted: this repository also contains the decompiled 1.7.10
reference source (`reference_1_7_10_source/`) that the parity audit was
verified against. If you are an original author and want anything
changed or removed, it will be — immediately.

`mods.toml` license field: **All Rights Reserved** (the original's rights,
not ours).

## How this was made

The port is the output of a full audit-and-fix pipeline, and the receipts
ship in-repo: [AUDIT_FINDINGS.md](AUDIT_FINDINGS.md) (630 findings, each
with original file:line citations and a terminal resolution),
[FIX_LOG.md](FIX_LOG.md) (the phase-by-phase work record),
[PARITY_NOTES.md](PARITY_NOTES.md) (every intentional deviation and
preserved original bug), a 150-test GameTest suite, and a static asset
audit that runs on every build. The commit history is the audit trail —
each slice of work landed gated on a green build, asset audit, and test
suite.

## Credits

- **TheyCallMeDanger** and the original OreSpawn authors — the mod itself,
  2013-2015. All rights theirs.
- The NeoForge port team — the 1.21.1 rebuild, audit, and test suite.
- MultiHitboxLib (bundled, `de.dertoaster.multihitboxlib`) — bone-synced
  multi-part hitbox support for The Queen.
- The 1.7.10-era OreSpawn community and wiki — the documentation that made
  verification possible.

## Reporting issues

[Issues](https://github.com/CoolFreeze23/Orespawn/issues) — include the mod
version, exact steps, expected vs. seen, a screenshot/clip for visuals, and
the log for crashes. Side-by-side 1.7.10 comparisons are gold: one good
screenshot can close a beta item for everyone.
