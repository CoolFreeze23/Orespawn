> SUPERSEDED the same day by the owner's clarification ("not a migration decision; MHLib stays; identify what
> MoreHitboxes does better and port those pieces into the vendored MHLib under MIT with attribution"). The
> per-feature comparison that replaces this file is `morehitboxes_evaluation.md`; this copy is kept because
> that report cites its provenance table and upstream-activity paragraph by line number (+4 lines from this header).

# MoreHitboxes (DarkPred) as a replacement for the vendored MultiHitboxLib — evaluation (2026-09-03)

Owner ruling: evaluate `morehitboxes` 1.21.1-1.9.4-alpha (MIT, GeckoLib optional >= 4.5.1); report the trust
model from bytecode, the server-side part placement API for the robots' solver, attack-box support against the
Queen's melee handshake, mixin conflicts with the replaced-renderer seam, the migration cost for the three
profiles, and upstream activity. Recommend; do not migrate. Report only; nothing in the repo changed.

## Recommendation: keep the vendored MHLib; defer MoreHitboxes

Deciding factors, in order:

1. **Trust-model regression for the Queen.** MoreHitboxes never carries animation to the server: the
   server's authoritative hurtboxes are the static datapack layout rotated by body yaw, while the client
   draws animated boxes and picks which part it hit; the server validates only vanilla reach against the
   static boxes. MHLib today applies the elected master client's bone stream server-side, so arrows and
   melee resolve against the animated pose. Neither library provides the server-fed evaluator the Slice 5
   question asks for, and as a sink for such an evaluator the two APIs are equivalent.
2. **Feature loss to re-implement in OreSpawn:** per-part damage modifiers, collidable parts (not
   reproducible: the part is never a collider), main-hitbox damage gating, the environmental-damage rule,
   pivots (the Queen would need ten new hitbox bones in her geo and a full placement re-fit), the per-entity
   size callback (only the vanilla scale attribute, which also feeds the renderer), the classic-mode
   zero-parts gate, the client part-registry path.
3. **Attack boxes, the one feature MHLib lacks, are client-only** (they test only the local player and send
   nothing), so they cannot touch the Queen's server-side, distance-gated melee handshake.
4. **The vendored copy is audited, optimized and pinned** by about twenty gametests and the placement probe,
   with BUG-042/043 just fixed; MoreHitboxes' 1.21.1 line is two alpha builds from a single maintainer with
   a `require = 0` renderer hook that fails silently on a GeckoLib change.
5. **Migration cost is several days for a lateral move.** Revisit only if the Slice 5 server evaluator ships
   and the team wants to shed MHLib's client-streaming machinery, or if MoreHitboxes leaves alpha on 1.21.1
   with server-synced animation; neither is on its roadmap.

## Artifact and provenance

| Item | Value |
|---|---|
| Modrinth project | `more-hitboxes` (id BOVAW87Z; the slug `morehitboxes` returns 404), MIT, client and server required |
| Version | `1.21.1-1.9.4-alpha-neoforge` (id 1Cu922wS), alpha, published 2026-03-31, 815 downloads; a fabric twin exists |
| File | `morehitboxes-neoforge-1.21.1-1.9.4-alpha.jar`, 117,838 bytes, from cdn.modrinth.com |
| Hash check | sha512 and sha1 recomputed equal the version metadata (0025360f… / fb49a064…) |
| Dependencies | neoforge [21,), minecraft [1.21.1, 1.22), geckolib [4.5.1,) optional AFTER |
| Source | github.com/DarkPred/MoreHitboxes, branch `1.21.1` head 88899b3 (no tag or release for the 1.21.1 builds), MIT |
| Built against | GeckoLib 4.8.3, NeoForge 21.1.66, Java 21 |

## Trust model (from the shipped bytecode)

- One network payload exists, `morehitboxes:sync_hitbox_data`, server to client, carrying the hitbox
  table; the client's reply only completes the configuration task. No client-to-server payload exists.
- Part positions: a common-side Mob mixin moves every part at the end of the mob's AI step to the static
  offset rotated by body yaw, times the vanilla scale; an animation override (from the client render pass)
  replaces that, client-side only.
- What the server accepts: vanilla interaction packets naming a part id; reach is validated against the
  part's static server box; damage is routed to the parent with the part reference. No plausibility bound,
  no master election, no rate limit, no server-side animation evaluation.

## Server-side part placement API (for the robots' solver)

Usable and equivalent in power to today's path: parts resolved by name from the entity's hitbox data,
positioned by `setPos` after the mob's tick exactly as the gait solver does now on MHLib parts, with the
library's static update at the AI-step return as the stomp the feed overwrites, as today. There is no part
rotation, pivot, hidden flag, size callback, collidable flag, main-hitbox size, or zero-parts construction;
the classic-mode gate and the client lazy-build path would become OreSpawn code.

## Attack boxes versus the Queen's melee handshake

Attack boxes are activated for a duration, follow their bone on the client, and test intersection with the
local player only, calling a client-side hook; no damage, no packet, dead state on the server. The Queen's
handshake is server-side and distance-gated (bite, tail, roar timers, then `doHurtTarget` within range) and
never consults part geometry, so attack boxes could drive only local cosmetics. A geometric server-side
melee needs the Slice 5 evaluator under either library.

## Renderer seam and GeckoLib compatibility

One GeckoLib mixin: an injection into `GeoEntityRenderer.renderRecursively` after `applyRenderLayersForBone`,
`remap = false`, `require = 0`; verified applicable on the pinned 4.8.4 by descriptor. It never touches
`GeoReplacedEntityRenderer`, so the replaced-renderer seam is untouched and also unserved (replaced species
would need OreSpawn's own bone-to-part hook). The three vendored MHLib GeckoLib mixins and the collector
layer would go. Coexistence of both libraries is untested; only an atomic cutover is sane. Bytecode fact
found on the way: GeoBone's matrix getters arm tracking on first read, which downgraded BUG-042 to a
one-frame gap (recorded as an amendment).

## Migration cost for the three profiles

- Data: MHLib profiles (blocks, pivots, sync and trust flags, damage modifiers, main hitbox) map to
  `data/orespawn/hitboxes/<entity>.json` elements in pixels with bone refs; pivots, modifiers, collidable,
  can-receive-damage, main-hitbox and all sync fields have no equivalent.
- ant_robot and spider_robot: six and eight leg elements; the classic zero-parts gate must be rebuilt.
- the_queen: ten elements; the fitted pivots need ten new hitbox bones in the geo and a placement re-proof;
  damage modifiers and main-box gating move into code; collidable parts are not reproducible.
- Code: delete 98 vendored files, the mixin config, the second mod entry, the access transformer and the
  databuddy dependency; rewrite TheQueen, QueenRenderer, SpiderRobot, AntRobot, ModernSpiderGait; keep the
  client part-registry mixin as OreSpawn code.
- Tests and probes: HitboxPartTests, AntGaitTests, QueenPlayNicelyDimsTests, RideTests assertions and the
  placement probe, about 2,500 lines, plus the owner's re-acceptance.

## Upstream activity

Single maintainer, responsive but quiet: last push 2026-03-31, five 1.21.1 commits (port, plugin update,
three fixes the day of release), eight issues all closed, nine stars, no GitHub releases, a one-line
changelog and a one-page wiki. The author states the library is limited to their own use cases.

Local artefacts (jar, extracted classes, javap dumps, fetched sources, Modrinth and GitHub JSON) are under the
session scratchpad `morehitboxes/` directory; nothing was added to the repo.
