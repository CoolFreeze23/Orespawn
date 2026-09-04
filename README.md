# OreSpawn — NeoForge 1.21.1 Port

> *"Just plain fun!"* — the classic 1.7.10 OreSpawn, rebuilt for modern
> Minecraft with 100% source-verified parity.

**Version:** 1.21.1-2.0.0-beta.1 · **Loader:** NeoForge 21.1+ · **Minecraft:** 1.21.1
**Status:** public beta — the 2.0 robot overhaul is live; the 1.0 parity
core underneath is stable, with visual/audio polish in community review

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
original's source. 630 audited findings tracked to closure; a 192-test
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
| **Modern robots** *(new in 2.0, on by default)* | Procedural spider & ant: legs that really plant and climb, per-leg hitboxes, a steerable spider saddle with a smart riding camera — what a default config runs (`spiderMovement` defaults to `MODERN`; the `modern.enabled` master defaults to true and defers to the per-feature keys). `modern.enabled = false` is the one-line switch to the exact 1.7.10 experience for every 2.0 feature at once; `spiderMovement = "CLASSIC"` switches just the robots. *(screenshot)* |
| **Fireballs respect `mobGriefing`** *(new in 2.0, on by default)* | OreSpawn fireballs place no fire beside the block they hit and scatter no blast fire while the gamerule is off, as ghast and blaze shots behave (`[modern] fireRespectsMobGriefing`; `false`, or `modern.enabled = false`, keeps the 1.7.10 fire-always behaviour). *(MOD-031)* |
| **Smarter targeting** *(new in 2.0, on by default)* | Tamed companions avenge and defend their owner (`[modern] petsDefendOwner`), Mobzilla spares its fellow bosses and the royal family (`godzillaSparesBossPeers`), the Pointysaurus locks on to whoever stares at it (`pointysaurusStareAggro`), and the Cryolophosaurus chases whoever hurt it (`cryolophosaurusRevengeChase`). Each key `false`, or `modern.enabled = false`, restores the 1.7.10 behaviour. *(MOD-032..035)* |

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

- **2.0.0-beta.1 (this build)**: the Procedural Spider Overhaul —
  see [CHANGELOG.md](CHANGELOG.md). Classic 1.7.10 behavior remains
  one config line away and is regression-tested on every build.
- **Next**: the Queen Coherence pass (flight-state and animation
  polish for The Queen), plus the remaining 2.0 backlog
  ([MODERNIZATION_NOTES.md](MODERNIZATION_NOTES.md)) — bone-synced
  boss hitboxes for King/Godzilla, config toggles for the original's
  roughest edges, the archived kyanite branch as optional content.
- **1.0 parity line**: the castle-tree patch (BUG-021) remains the
  designated parity fix and rides into a future build.

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

### Third-party notices

The port bundles or derives from these third-party works; their notices
travel with it as listed here.

- **MoreHitboxes** by DarkPred — MIT License. Portions are ported into
  the vendored MultiHitboxLib: the per-entity render-tick gate
  (`de.dertoaster.multihitboxlib.util.RenderTickGate` and the collector's
  pass gate in `IBoneInformationCollectorLayerCommonLogic`), design after
  `GeckoLibMobMixin` at commit 88899b3 of
  https://github.com/DarkPred/MoreHitboxes. License text:
  `src/main/resources/META-INF/LICENSE-MoreHitboxes.txt` (ships inside the
  jar) and `src/main/java/de/dertoaster/multihitboxlib/LICENSE-MoreHitboxes.txt`.
- **MultiHitboxLib** by DerToaster — vendored into
  `src/main/java/de/dertoaster/multihitboxlib/` with local modifications
  (recorded in AUDIT_FINDINGS.md and FIX_LOG.md; upstream credits: DerToaster,
  Meme Man, 19__). The upstream LICENSE text governs the vendored portion:
  the GNU Lesser General Public License, version 3 (29 June 2007), shipped
  verbatim as `src/main/resources/META-INF/LICENSE-MultiHitboxLib.txt`
  (inside the jar) and
  `src/main/java/de/dertoaster/multihitboxlib/LICENSE-MultiHitboxLib.txt`.
  Upstream's build metadata declared `All Rights Reserved` and its README
  added terms of use; the upstream repository and mod pages have since been
  deleted; no contact with the author was made (the owner's decision, 2026-09-04),
  and the LICENSE text governs.
- **Databuddy** by Commoble — MIT License, as stated by its upstream
  repository https://github.com/Commoble/databuddy ("Copyright (c) 2020 Joseph
  Bettendorff aka Commoble"; the nested jar and its POM carry no license text
  of their own, so this line is sourced from upstream). Shipped jar-in-jar as
  `META-INF/jarjar/databuddy-1.21-6.0.0.0.jar` (BUG-032); the nested jar
  and its Maven POM carry no license file of their own.
- **GeckoLib** by Gecko and contributors — MIT License ("Copyright (c)
  2024 GeckoThePecko", the `LICENSE_GeckoLib 4` file inside the GeckoLib jar
  itself). A runtime dependency, not shipped in this jar.

## How this was made

The port is the output of a full audit-and-fix pipeline, and the receipts
ship in-repo: [AUDIT_FINDINGS.md](AUDIT_FINDINGS.md) (630 findings, each
with original file:line citations and a terminal resolution),
[FIX_LOG.md](FIX_LOG.md) (the phase-by-phase work record),
[PARITY_NOTES.md](PARITY_NOTES.md) (every intentional deviation and
preserved original bug), a 192-test GameTest suite, and a static asset
audit that runs on every build. The commit history is the audit trail —
each slice of work landed gated on a green build, asset audit, and test
suite.

## Credits

- **TheyCallMeDanger** and the original OreSpawn authors — the mod itself,
  2013-2015. All rights theirs.
- The NeoForge port team — the 1.21.1 rebuild, audit, and test suite.
- **DerToaster** — MultiHitboxLib (bundled as `de.dertoaster.multihitboxlib`,
  LGPL-3.0; see the third-party notices) — bone-synced multi-part hitbox
  support for The Queen.
- The 1.7.10-era OreSpawn community and wiki — the documentation that made
  verification possible.

## Reporting issues

[Issues](https://github.com/CoolFreeze23/Orespawn/issues) — include the mod
version, exact steps, expected vs. seen, a screenshot/clip for visuals, and
the log for crashes. Side-by-side 1.7.10 comparisons are gold: one good
screenshot can close a beta item for everyone.
