# OreSpawn for NeoForge 1.21.1 — 2.0.0-beta.8

Version `1.21.1-2.0.0-beta.8` (`gradle.properties`; `README.md` line 6), cut on 2026-09-20 on beta.7 for the `/kill` bypass and the hit boxes that follow the rigs. Release: https://github.com/CoolFreeze23/Orespawn/releases/tag/v1.21.1-2.0.0-beta.8

## Part one — for the player

Two fixes: `/kill` kills every OreSpawn creature again, and the King, the Kraken and Godzilla have hit boxes that follow their models.

**Fixed**
- `/kill @e` kills every OreSpawn creature, including the King, the Queen, Godzilla and the Kraken, and it works in the middle of a fight. Thirty creatures used to shrug the command off: some capped every hit, some ignored any hit for a second or two after the last one, and the boss heads passed it on to a body that capped it.
- The King's, the Kraken's and Godzilla's hit boxes sit on their models. The King's heads, necks, wings, legs and tail, the Kraken's head, body, fins and every tentacle, Godzilla's head, body, arms, legs and tail each have a box that follows the drawn part, the way the Queen's already did. The floating head boxes that never lined up with the models are gone.

**Changed**
- Hits on the King and Godzilla land by part, as on the Queen: heads take full damage, body, necks and legs half, wings and tail a quarter. The Kraken takes full damage anywhere, as it always did.
- Under PlayNicely the King, Godzilla and the Kraken now behave like the Queen: a small body box with small parts, hit through the parts.
- The Kraken no longer hurts or sets fire to itself with its own lightning.
- The King, the Queen and Godzilla no longer spawn the separate head creature from 1.7.10. One left in an old world disappears when it loads.

**Good to know**
- The big body box of each of the three (22 by 24 for the King, 4 by 15 for the Kraken, 9.9 by 25 for Godzilla) is still there as the collision box, but it no longer takes hits; the parts do. The parts are solid, so you can stand on Godzilla's tail.
- Long thin parts (necks, tentacles, wing membranes) read wider than they look, because part boxes are square in plan. The Kraken's tentacle boxes can lag the drawn tentacle by up to three blocks at the extremes of its wave.
- On a server with no player in render range the parts sit at their rest positions until someone comes close.
- Works in the worlds you already have.

**Install:** put `orespawn-1.21.1-2.0.0-beta.8.jar` in `mods/` (NeoForge 21.1, Minecraft 1.21.1, GeckoLib 4.7 or newer) and take the beta.7 jar out. Worlds carry over.

### In more detail

#### What beta.8 is

beta.8 is beta.7 plus two landings, both from a play session of 2026-09-20: ENT-S-172, the `/kill` bypass, and BOSS-047, hit boxes that follow the rigs. Nothing else moves. Both apply to worlds you already have; a KingHead, QueenHead or GodzillaHead saved by an earlier build discards itself when its chunk loads.

#### What changed

- **`/kill` kills every OreSpawn entity.** `/kill` is `Entity.kill()`, for a living entity `hurt(genericKill, Float.MAX_VALUE)`, a source in the damage-type tag `bypasses_invulnerability` (with the void, the whole tag in 1.21.1), the tag vanilla's own gates yield to (the Invulnerable flag, the totem, the Wither's spawn armour; the Ender Dragon answers `/kill` in its own `kill()`). Thirty of the port's damage overrides, transcriptions of 1.7.10 `attackEntityFrom` bodies whose `/kill` could not name a mob, capped the amount (the King, the Queen and Godzilla at 750; the Boyfriend, the Girlfriend, the Purple Power, the Velocity Raptor, the Hydrolisc and the tamed Gazelle at 10), refused it inside their own hit window (twenty species, the King's 20 ticks to the Sea Viper's 5), forwarded it to a body that capped it (the three head sidecars) or, the hoverboard, refused an attacker-less hit while ridden. Each now opens with the clause: a bypassing source goes straight to `super.hurt`, before the window, the cap and the attacker tests; the heads take it and pass it to the body; the ridden board excepts it. Nothing else in any method moved. *(ENT-S-172; `KillBypassTests` s172a-c)*
- **Hit boxes that follow the rigs.** The King, the Kraken and Godzilla carry MultiHitboxLib bone-synced hitbox profiles (26, 25 and 16 parts) written from their rigs at the 1.7.10 render scales by `tools/boss_hitbox_profiles.sh`, the Queen's ENT-S-092 derivation generalised to multi-bone parts under any render scale and render transform: each box on its drawn segment, pivots through MHLib's own rotation, the King's thin wing tips padded for the flap. The King and Godzilla keep their damage scheme in the Queen's form (heads 1.0, body, necks and legs 0.5, wings and tail 0.25); the Kraken's parts are all 1.0. The 1.7.10 envelopes stay as unpickable collision boxes. The replaced-entity renderer applies the render scale after GeckoLib's capture and switches a profiled species' synched bones to matrix tracking first, so the collector ships the drawn positions from the first frame (the drawn image is unchanged). The hand-placed `OreSpawnPartEntity` layouts and the three sidecar spawns are removed; the sidecar types stay for old saves and discard themselves; the Queen's PlayNicely size hook covers any profiled species; the Kraken's own storm and burn are refused at `hurt` as the original never took them; the royal fireballs screen a part's boss. Declared: the square footprint of the aabb part type; the Kraken's tentacle boxes drifting up to three blocks at the wave extremes (the render transform is not folded into the shipped rotation); server-only fallback boxes; solid parts; the Kraken's 4x15 envelope held for a later decision. *(BOSS-047; `BossHitboxProfileTests` s047a-e, `HitboxPartTests`, `ConfigGateTests` boss017)*

#### How to install

Put `orespawn-1.21.1-2.0.0-beta.8.jar` into the `mods` folder of a NeoForge 21.1 instance for Minecraft 1.21.1 together with GeckoLib 4.7 or newer, and take the beta.7 jar out; MultiHitboxLib and Databuddy are bundled in the jar. Existing worlds carry over.

## Part two — for the modder

### The bypass clause

`Entity.kill()` is `remove(KILLED)` for a non-living entity and `hurt(damageSources().genericKill(), Float.MAX_VALUE)` for a living one; in 1.21.1 `minecraft:generic_kill` and `minecraft:out_of_world` are the whole of `#minecraft:bypasses_invulnerability`. The port's damage overrides were transcriptions of 1.7.10 `attackEntityFrom` bodies, and 1.7.10's `/kill` took no target, so none of them had met a bypassing source. The sweep read every `hurt`, `isInvulnerableTo`, `actuallyHurt`, `kill` and `die` override under the entity package (added six windows the first pass had missed and the ridden hoverboard) and found thirty: six caps, seventeen hit windows, three cap-and-window bosses, three forwarding heads, one refusal. The clause is one `if` at the top of each: `source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)` returns `super.hurt(source, amount)`; the heads call `super.hurt` and then the body in range (a head that died alone would be revived by its own health mirror a tick later); the hoverboard's ridden refusal excepts the tag. Records: ENT-S-172. Pins: `KillBypassTests` s172a (145 registered types, living or not, killed and gone), s172b (every living type hit once and killed inside its window), s172c (the tamed Gazelle and Velocity Raptor, the ridden board).

### The hitbox profiles

Three MHLib profiles under `data/orespawn/multihitboxlib/hitbox_profiles/` — `the_king.json` (26 parts), `kraken.json` (25), `godzilla.json` (16) — written by `tools/boss_hitbox_profiles/BossPartProfileWriter.java` through `tools/boss_hitbox_profiles.sh` from the specs in `tools/boss_hitbox_specs/` (a standalone source compiled against the g1tool runtime classpath: the g1tool class output and `build.gradle` are both part of the G1 benchmark's provenance). The writer bakes the rig with GeckoLib 4.9.2's own loader and re-executes the replaced renderer's chain at body yaw 0 (the `entityRenderTranslations` capture, the render scale after it, `YP(180)`, the descriptor's render transform in its slot form — the Kraken's `XP 90`, which stands it upright with the tentacles below its feet — the seam's height compensation, GeckoLib's 0.01), tracks every bone's world matrix and the collector's summed rotation, and for each part takes the union of its member bones' drawn cubes: size `[max(x extent, z extent), y extent]`, position the anchor bone's world position (the fallback before the first bone packet), pivot `(anchor − segment bottom-centre)` inverse-rotated by the shipped rotation, so `MHLibPartEntity.applyInformation` puts the box on the drawn segment. `minHeight` pads a slab that swings (the King's outer wing panels, 4 and 6 blocks). On the entity side TheKing and Godzilla dropped their `OreSpawnPartEntity` layouts, all three implement `IMHLibSizeCallback` (0.25, 1/3, 0.25 while PlayNicely-shrunk), `TheQueen.PlayNicelySizeHook` scales any profiled `IMHLibSizeCallback` after MHLib's own size handler, the three head sidecars are no longer spawned and discard themselves on their first server tick, the Kraken refuses `LIGHTNING_BOLT` and `IS_FIRE` (its own storm falls on its tentacle parts), and `BetterFireball.canHitEntity` screens a part's parent. `OreSpawnGeoReplacedEntityRenderer` applies the descriptor scale in `scaleModelForRender` (after the capture; before, a scale inside the capture was cancelled out of every bone matrix and MHLib would have placed the King's heads 12 blocks out instead of 25) and pre-enables matrix tracking on the synched bones. The asset audit reads a registration to its `.build(...)` and a profile's rig from its descriptor. Records: BOSS-047 (geometry and renderer; entity and behaviour); MOD-025 resolved. Pins: `BossHitboxProfileTests` s047a-e; `HitboxPartTests.s4_part_parent_sweep_for_hud_unwrap` (26 King parts); `ConfigGateTests.boss017_play_nicely_gates` (the Queen's PlayNicely form for the King). Open: an orientable box type; folding the render transform into the shipped rotation; the Kraken's 4x15 envelope.

### The harness

Checks on the release tree: drift 0; `RESULT: 0 error(s), 0 advisory(ies), 4 acknowledged; draw order: 104 shipped geo: 103 seam + 1 outside-seam`, `G1 PARITY PASS` 2, 13 and 101 models, checked-in proof verified, `BUILD SUCCESSFUL in 10m 45s`; the gametest suite: `All 1317 required tests passed`, `BUILD SUCCESSFUL in 5m 10s` — 1309 tests before these fixes plus the three pinning the bypass clause and the five pinning the profiles.

### Third-party notices and credits

Unchanged from beta.5: see `phase_g_reports/RELEASE_NOTES_2.0.0-beta.5.md`, "Third-party notices" and "Credits" (MultiHitboxLib under LGPL-3.0, the MoreHitboxes portions under MIT, GeckoLib under MIT, Databuddy under MIT; the original mod by TheyCallMeDanger and the OreSpawn authors, 2013-2015).
