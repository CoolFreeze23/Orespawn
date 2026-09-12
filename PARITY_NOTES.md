# PARITY_NOTES — Intentional Deviations & Replicated Original Quirks

Per `IMPLEMENTATION_PLAN.md` "Done means": every intentional deviation from the original
(target: near zero) and every original bug deliberately replicated for parity, with
justification. Cross-referenced to finding IDs and MODERNIZATION_NOTES entries.

---

## PN-001 — TheQueen: tracked-victim removal split player/mob (BUG-005)

- **Original:** `orig TheQueen.java:260-261, 340-341` removes ANY victim (players
included) via `setDead()` when its tracked HP hits 0.
- **Port:** Non-player mobs keep the original `discard()` (no drops/death event —
original quirk preserved). Players instead receive a lethal attributed hit so the
death pipeline runs. Deleting a `ServerPlayer` entity outright is a server-integrity
defect (ghost connection, no respawn), not a gameplay value.
- **See:** MOD-001 for the full modernization of the mob half.

## PN-002 — ThePrince/ThePrincess: activity 2 no longer enables noPhysics (BUG-010, temporary) — CLOSED 2026-06-13 (Phase D3)

- **Original:** `orig ThePrince.java:423` maps activity 2 (flying) to `noPhysics`, with
`do_movement()` providing the actual flight steering.
- **Port (interim):** Flight movement is not yet ported (Phase D scope), so the
noPhysics mapping is disabled — with it on and no steering, a hurt prince sank
through terrain into the void. The original's 1/100-per-tick land/fly re-roll
(`orig ThePrince.java:529-539`) IS ported, so the activity state machine matches.
When flight lands in Phase D this note must be revisited and the mapping restored.
- **CLOSED (2026-06-13, Phase D3):** `do_movement()` is ported for both the baby
Prince and the Princess (and `fly_without_rider` for Teen/Adult), so the
`noPhysics` mapping is restored exactly as the original had it: baby/princess
`noPhysics = activity == 2`, teen/adult `noPhysics = activity != 0`. The interim
disable is gone; MOD-003 remains the 2.0 candidate for collision-aware flight.

## PN-003 — TheKing: small-attacker deletion preserved (BUG-012)

- **Original:** `orig TheKing.java:824-826` deletes any attacking `EntityMob` with
bb area < 3.0 and ignores the hit. Port matches exactly (`Monster` + `discard()`);
players are structurally exempt (never `Monster`). Kept per ground rule 2; see MOD-002.

## PN-004 — Kraken grab: transport mechanism modernized, geometry identical (BUG-011)

- **Original:** Force-set position each tick for any caught entity.
- **Port:** Same hold point (15 blocks below the Kraken), same forced yaw, same
release/damage rolls — but caught players are moved via `ServerPlayer.connection.teleport`
  - `hurtMarked` because raw `setPos` on a client-authoritative player causes
  rubber-banding/kicks in 1.21.1. Non-players unchanged.

## PN-005 — Loot enchantment dice approximated by `enchant_randomly` (Phase B1)

- **Original:** Gear drops ran chains of independent per-enchantment rolls
(`if (rand.nextInt(6)==1) addEnchantment(X, 1+rand.nextInt(5))`, Unbreaking 1-in-2,
levels 2–5), producing 0–7 enchantments per item.
- **Port:** Loot JSON cannot express independent per-enchantment dice; every such item
carries one `minecraft:enchant_randomly` (always exactly one enchantment). Single
uniform approximation across every consolidated table — the alternative (keeping Java
drop code) would defeat the single-source-of-truth architecture. Exact-fidelity option
recorded as MOD-007.
- **See:** `phase_b_reports/B1_drops.md` "Enchantment translation note".

## PN-006 — Crab max health reads the Nightmare's stats table entry (Phase B2)

- **Original:** `orig Crab.java:137` reads `PitchBlack_stats.health` (250) — not
`Crab_stats.health` (180) — when applying scaled max health. A 1.7.10 copy-paste bug
that shipped; the port reproduces it (with a citation comment) because crab HP is
gameplay-defining. `Crab_stats` attack/defense are used correctly.
- **See:** `phase_b_reports/B2_mobstats.md` §Crab; MobStats Javadoc.

## PN-007 — Rider control: per-player payloads + fly-down key (Phase B3, carried forward)

- **Original:** One global `OreSpawnMain.flyup_keystate` shared by every player (an
original multiplayer bug) and a single UP/FAST key (Left Alt).
- **Port:** Per-player `RiderInputPayload` routed to the ridden entity (audited as the
sanctioned modernization), client-predicted mount movement (vanilla horse pattern)
instead of the original's server-side integration — same physics constants, cited
per mount in `RiderFlightController.Config`. The fly-down key (LCTRL) and the G
"special" key are port additions; G is currently a no-op on every mount (the port
Dragon's invented G-volley was removed for parity — orig ranged fire is strafe-driven).
- **See:** `phase_b_reports/B3_riders.md` §Architecture, §Task 8.

## PN-008 — Attribute-cap raise (port infrastructure, NOT a behavior deviation)

- **What:** Vanilla 1.21.1 hard-clamps the `MAX_HEALTH` attribute to 1024 and
`ATTACK_DAMAGE` to 2048 (`RangedAttribute.sanitizeValue`); 1.7.10 had no such caps.
Without intervention every big OreSpawn boss silently ran at 1024 HP. The port widens
both caps to 100000 at mod construction (`OreSpawnMod.java`) via the access transformer
line `public-f net.minecraft.world.entity.ai.attributes.RangedAttribute maxValue`
(`src/main/resources/META-INF/accesstransformer.cfg`).
- **Why infrastructure:** this restores the ORIGINAL values rather than deviating from
them — gameplay numbers are exactly the 1.7.10 table; only the modern engine's clamp
is lifted. No original behavior is altered.
- **Entities above the vanilla 1024 cap (true original values now in effect):**
TheKing 7000 (orig OreSpawnMain.java:6521) · TheQueen 6000 (:6522) · KingHead /
QueenHead 6000 (sidecar parts mirror their boss) · Godzilla 4000 ("Mobzilla", :6514) ·
GodzillaHead 4000 · ThePrinceAdult 3000 (orig ThePrinceAdult.java:226) ·
SpiderRobot 1500 (:6474) · ThePrinceTeen 1500 (orig ThePrinceTeen.java:230).
(Kraken is 1000 — under the cap; listed for completeness since the old port's
invented 3000 was being clamped before Phase B.)
- **Interop note:** the raise is global to the attribute, so other mods' entities may
also exceed 1024 if they set higher bases — benign, but worth remembering when
debugging cross-mod health oddities.

## PN-009 — CLOSED (2026-06-13): invented kyanite/pink-tourmaline branch fully removed

- **What:** The Phase-C7 version of this note retained the port-invented `ore_kyanite`
vein as a documented exception. Owner decision (Option A) superseded that: the entire
Phase-10 branch — `ore_kyanite`/`ore_pink_tourmaline` blocks, `kyanite`/
`pink_tourmaline` gems, the kyanite tool tier + 5 tools + 4-piece armor set, and all
13 associated recipes/loot/worldgen JSONs — is deleted from the parity build. The
complete design is archived in MODERNIZATION_NOTES MOD-009 as a 2.0 content candidate.
- **Why the original needed no exception:** 1.7.10's "Kyanite" is the display name of
the `CrystalStone` terrain block (`orig OreSpawnMain.java:3029`) — the entire Crystal
dimension floor is made of it (`ChunkProviderOreSpawn5.java:121,154-177`), so the
crystal-tool chain (CrystalStone + CrystalSticks → tools, `:3244-3252`; ×8 → Crystal
Furnace, `:3082`) was never supply-constrained and never depended on a gem item. The
port's `crystal_stone` already replicates all of that; its display strings were
restored to the original "Kyanite" / "Kyanite Sword/Pickaxe/Shovel/Hoe/Axe" names
(`:3239-3243`) as part of this closure.
- **Residual deviation:** none in-game. World-compat: invented branch items vanish from
pre-existing port worlds on load (recorded in MOD-009).

## PN-010 — CLOSED (2026-08-08, Phase D5): SpawnOres pool fully restored

> Historical note only. The interim Phase C7 redesign this entry documented
> (dragon/kraken-only spawn blocks + the invented ancient-dried-egg block) was
> retired in Phase D5: the full ~105-type pool now generates via
> `SpawnOresPoolFeature` with the original roll structure, all 116 water-bucket
> egg recipes exist (ITEM-062), and the ancient-dried-egg invention was removed
> (archived as MOD-013). No residual deviation beyond PN-016.
- **What (historical):** 1.7.10 generated 28+/chunk "spawn ore" veins at Y50-128 drawn
from a pool of ~105 spawn-block types (ChunkOreGenerator + OreSpawnMain SpawnOres
stats). The Phase C port generated only dragon/kraken boss spawn blocks (1/24 chunks
each) and ancient dried eggs (1/12) — a deliberate redesign kept for Phase C.
- **Why deferred then:** the pool depended on dozens of per-mob spawn blocks that were
Phase D scope (with WGEN-042's structure/block backlog).

## PN-011 — Utopia Portal Block kept as a creative-only utility (WGEN-050, Phase C7)

- **What:** 1.7.10 `PortalBlock.java` is an empty stub; dimension travel was entirely
entity-based (ants/termite/butterfly). The port's `UtopiaPortalBlock` (entityInside
teleport, unbreakable) is a port addition retained as a creative-only admin utility:
it is not generated in any world, has no recipe, and is obtainable only from the
creative menu.
- **Why:** harmless operator convenience; removing it would break existing port worlds
that placed it. Documented here so it is not mistaken for original content.

## PN-012 — APPROVED (2026-07-02): Village dimension villages are modern jigsaw villages (WGEN-015, Phase D1)

- **Original:** `MapGenMoreVillages.java:11-12` ran the vanilla **1.7.10** village
generator in the Village dimension at spacing 9 / separation 7 (~12× vanilla
density), producing 1.7.10-era plains villages (gravel paths, old house shapes,
pre-1.14 villager professions implied by era).
- **Port:** `worldgen/structure/dim_village.json` runs the vanilla **1.21.1** plains
jigsaw village (`minecraft:village/plains/town_centers` start pool) via
`structure_set/dim_villages.json` with the same spacing 9 / separation 7.
- **Why this mapping (owner-approved, Option A):** the original delegated village
  *style* to vanilla — `MapGenMoreVillages` overrode only the spacing/separation
  fields on the stock generator. OreSpawn's actual contribution was density and
  dimension placement (spacing 9 / separation 7), and those are exact in the port.
  The 1.7.10 procedural generator no longer exists (vanilla replaced it with jigsaw
  villages in 1.14), so "whatever vanilla villages look like" in 1.21.1 is the
  jigsaw plains village. No 1.7.10-style building templates are planned: the style
  was never OreSpawn content, so there is nothing to revisit post-parity.
- **Player-visible:** village *style* differs the same way vanilla villages differ
  between 1.7.10 and 1.21.1; village *frequency and location* match the original.

## PN-013 — APPROVED (2026-07-03): Per-tier stat config overrides hardcoded at original defaults (ITEM-065, Phase D4)

> Design ruling at the D4 checkpoint (2026-07-03): ITEM-065's DEFERRED status is
> approved; this note and MOD-011 stand as written.

- **Original:** `orig OreSpawnMain.java:1489-1517` — `get_armorstats`/`get_weaponstats`/
`get_orestats` read every armor/weapon/ore stat number from the Forge config file at
init, so server owners could rebalance any tier (defaults visible in the calls, e.g.
Ultimate armor 200 dur / 6-12-10-6 / ench 100).
- **Port:** `ModArmorMaterials`/`ModToolTiers` carry the original **default** values
verbatim (verified number-by-number in Phase C, e.g. ENT-A-045) but are baked in at
registration. NeoForge 1.21.1 registers armor materials and tool tiers statically at
mod construction, before any config (especially server configs) is loaded, so a
faithful config-override hook is not implementable without mutating frozen registries.
Gameplay with an untouched original config file is identical.
- **Decision:** hardcode at original defaults, per the audit's sanctioned fallback for
ITEM-065 ("document hardcoding as a deliberate platform decision"). Datapacks already
cover ore-drop tuning. Config-driven rebalancing recorded as MOD-011.

## PN-014 — APPROVED (2026-07-03): Seasonal date gates evaluate live instead of freezing at launch (ANIM-016, Phase D4)

> Design ruling at the D4 checkpoint (2026-07-03): this deviation carries the
> owner's explicit sign-off as an intentional behavior change — live `LocalDate`
> evaluation replaces the original's frozen at-init calendar. It is approved in
> its own right, not merely on the audit's recommendation.

- **Original:** `orig OreSpawnMain.java:4518-4521,4567-4571` — read a GregorianCalendar
once at mod init; holiday behavior (Oct 31 ghosts, Feb 14 giant Girlfriend, Apr 20
EasterBunny) froze for the whole session, and the holiday spawn *registrations*
happened at init, so a server started Oct 30 never saw Halloween at all.
- **Port:** `SeasonalDates` checks `LocalDate.now()` per query (spawn checks, AI
gates, renderer), per the audit's own fix recommendation for ANIM-016. Same dates,
including the original's hardcoded April 20 "Easter". The registration-time spawn adds
became static biome modifiers (`halloween_ghosts.json` + existing bunny files) gated
at spawn-rule time — the only datapack-compatible equivalent.

## PN-015 — Structure randomness is seed-stable in worldgen (Phase D5)

- **Original:** structure layouts drew from live RNG at build time — `world.rand`
throughout, and the BasiliskMaze corridor topology from raw unseeded `Math.random()`
(orig BasiliskMaze.java:232-234) — so no layout was reproducible from the world seed.
- **Port:** every LegacyDungeonStructure generator draws from the deterministic
per-position piece RandomSource (seeded from the piece bounding box). This is
REQUIRED by the multi-pass chunk-stitching pattern: the whole build replays once per
intersecting chunk and every pass must see identical rolls, or structures would have
visible seams at chunk borders. Per-instance layouts remain just as random; the only
delta is that a given world seed + position now always yields the same layout.
- **Exception kept faithful:** the Dungeon Spawner Block path builds with the live
level RNG (orig DungeonSpawnerBlock.java:52 used `world.rand`), so repeated
block-triggered builds do not repeat layouts (`LegacyDungeonPiece.buildNow`).
- **Player-visible:** no (nobody can observe non-reproducibility in normal play).

## PN-016 — SpawnOres veins use the modern stone-replaceables tag and step ordering (WGEN-005, Phase D5)

- **Original:** SpawnOres veins replaced bare `Blocks.stone` only (OSW:403/COG:642)
and ran as the FIRST block of the ore pass, before uranium/titanium/etc
(OSW:355→805 / COG:21→471 call order).
- **Port:** `SpawnOresPoolFeature` places vanilla ore veins against
`#minecraft:stone_ore_replaceables` at the `underground_ores` step — the same
treatment every other restored ore received in Phase C7 (WGEN-001). 1.7.10 had no
granite/diorite/andesite, so the tag widens the target set exactly the way the other
ores already do; intra-step ordering vs the other ore features follows biome-modifier
order rather than the original call order. All veins replace stone-family blocks
either way — the difference is not player-distinguishable.
- **Player-visible:** no.

## PN-017 — End-dimension placement mapping (EnderCastle + Hospital, Phase D6a)

- **Original:** both structures fire dimension-wide in the End (OSW:219-241 —
dimension-id gate, biome never inspected; 1.7.10 had one End biome and no outer
islands), with block scans: 3 attempts, air-on-end-stone in Y 90→11
(EnderCastle adds a 30×30 air plane at +8, Hospital a 12×12 at +4).
- **Port:** biome tag `#minecraft:is_end` (the faithful-to-CODE choice — the
dimension-wide gate + end-stone scan self-selects island terrain, which now
includes the 1.9+ outer-ring biomes; outer islands top out ~Y60-75, inside the
scan window) + the END_SURFACE placement mode (noise-heightmap anchor; void
columns rejected; clearance planes approximated by footprint corner/centre
surface sampling ≤ anchor+3 — conservative vs the castle's looser +8 plane).
Frequencies map to structure-set spacing (castle 14/7 ≈ 1/200; hospital 10/5 ≈
1/100); the End path's "recently_placed never set" quirk maps to independent
sets. Player-visible: castles/hospitals can also appear on outer End islands —
terrain that did not exist in 1.7.10; central-island behavior matches.

## PN-018 — Inca Pyramid ramps: pre-build terrain reads (Phase D6a)

- **Original:** ramp rails/treads/support pillars condition every write on
world air-reads (GD:3791-3873): pillars stop at terrain, treads skip occupied
cells, and docking into the pyramid's own steps is a read-after-write.
- **Port:** own-structure reads reproduced exactly via an in-memory write-set
model; pre-build terrain is unreadable under chunk stitching, so unrecorded
cells read as air — support pillars always fill to relative y 0 (replacing the
surface grass under ~200 outside-footprint ramp-lane cells even on the flat
Islands plane, where the original stopped atop the grass) and treads place
unconditionally. Worst case on the live Dungeon Spawner Block path over rough
terrain: ramps punch through instead of yielding. Sanctioned by the spec
(section 10); the flat-plane grass delta is cosmetic (under the ramp lanes).

## PN-019 — Monster Island ocean-biome mapping (Phase D6a)

- **Original:** corner-biome name check EXACTLY "Ocean" (OSW:1402-1403) —
excludes Deep Ocean, Frozen Ocean, beaches; 1.7.10 had no other ocean variants.
- **Port:** biome filter `minecraft:ocean` only. Modern lukewarm/cold/warm
variants (no 1.7.10 counterpart) are excluded — the narrow faithful reading;
the deep/frozen exclusions carry over exactly. Set 42/21 ≈ the 1/6 × 1/300 odds.

## PN-020 — The companions' target goals acquire inside vanilla's sphere, not 1.7.10's box (ENT-S-129 refuter A; ruled 2026-09-04 night: deliberately not reproduced)

- **Original:** `EntityAINearestAttackableTarget` on the Boyfriend and Girlfriend (orig Boyfriend.java:138-147,
Girlfriend.java:161-174, `targetDistance` 15 on the IMob task) scanned `boundingBox.expand(d, 4, d)` — a box — with
no distance test at acquisition; `EntityAITarget.continueExecuting` then released a target farther than `d` from the
mob (`getDistanceSqToEntity > d²`). A target whose centre lies inside the box but beyond `d` from the mob — the ring
between the sphere of radius `d` and the box's edges, up to `d` plus both half-widths along an axis — was acquired at
the end of one target pass and released by the next.
- **Port:** vanilla `NearestAttackableTargetGoal` scans the same inflated box and then `TargetingConditions.range(d)`
refuses anything beyond `d` at acquisition (the hold's release distance is the same `d`; ENT-S-129 restored the 15).
The ring is never acquired.
- **Why not reproduced (owner, 2026-09-04 night):** no player-visible signature — in 1.7.10 the ring target was
dropped before any attack step could act on it (no swing, sound or motion carried the difference; the T5 hold rows
measure from the mob's centre, the prey at 15.8, for this reason). Under the 2026-09-04 doctrine an engine-frame
difference without a player-visible signature is recorded, not coded.
- **Player-visible:** no.

## PN-021 — The Stinky's coal-ore hunt reads the modern engine's two coal ores as 1.7.10's one (ENT-S-119's mapping; T5b, 2026-09-05; both modes)

- **Original:** `Stinky.java:443-487` compared every probe of the six-face shell scan (the eat of :582-607, restored by
ENT-S-119) against the one coal ore of 1.7.10, `Blocks.field_150365_q` — the block every coal vein placed at every depth.
- **Port:** `EntityStinky.isCoalOre` (:404-412) tests the probe against `#minecraft:coal_ores` — `coal_ore` and
`deepslate_coal_ore` — instead of `Blocks.COAL_ORE` alone. 1.21.1 split the ore into two blocks by the stone it sits in: the
vanilla `ore_coal` features and the port's own `ore_boost_*.json` pairs (`stone_ore_replaceables → x_ore`,
`deepslate_ore_replaceables → deepslate_x_ore`) place the deepslate variant below y 0 and wherever deepslate stands in for
stone, so with the block alone a Stinky flying in the deep would starve of the ore 1.7.10 gave it everywhere.
- **Why this mapping:** the tag is the engine's own name for "the coal ore, whichever stone" — the same mapping the ore
generator uses to place it — and nothing else of the routine changes: the shells, the nearest-by-distSq pick, the walk, the
eat under distSq 12, the heal and the burp are ENT-S-119's transcription. No config key: the mapping holds in both modes.
- **Player-visible:** no — the Stinky eats deepslate coal ore where 1.7.10's single ore was everywhere; a Stinky in the
deepslate layers behaves as one at the surface.
- **Pin:** `StinkyIdleParityTests` row `pn021_22_stinky_443_deepslate_coal_ore_eat` — the row-1 probe with a deepslate coal ore
at origin + (1, 0, 0): found at distSq 1, walked to at 1.25, eaten to air, the heal of 1 and the burp at 0.5 / 1.5..1.7.

## PN-022 — The shared target sorter weighs a player's silhouette by the modern pose-sized hitbox (ENT-S-140; ruled 2026-09-05: the engine's, deliberately not reproduced; both modes)

- **Original:** orig GenericTargetSorter.java:20-33 divides an operand's distance² by `height * width` when the product
exceeds 1. 1.7.10's `EntityPlayer` was 0.6 × 1.8 in every pose but sleeping (0.2 × 0.2): sneaking set the flag and the
client camera drop only — `setSize` never ran for a crouch — so a player's silhouette was 1.08 in every pose and the
sorter divided its distance² by 1.08 whether the player stood, crouched or swam.
- **Port:** `entity/ai/GenericTargetSorter.java:42-45` evaluates the same formula on `getBbHeight() * getBbWidth()` — the
modern engine's live pose dimensions: standing 0.6 × 1.8 = 1.08 (divided), crouching 0.6 × 1.5 = 0.9 and swimming or
gliding 0.6 × 0.6 = 0.36 (undivided); sleeping 0.2 × 0.2 in both trees.
- **Why not reproduced (owner, 2026-09-05):** the pose-sized hitbox is the modern engine's — 1.21.1 resizes the player for
crouch, swim and elytra flight, which 1.7.10 could not — and OreSpawn's own contribution, the sorter formula, is
transcribed exactly. Under the 2026-09-04 doctrine the difference is the engine's part: recorded, not coded; no key.
- **Player-visible:** narrowly — a hunter with a standing player and a nearer crouching, swimming or gliding one in reach
may take the standing one where 1.7.10 took the nearer (the crouching-against-standing pair from the T4 refutation:
1.7.10 23.15 against 24.08, the port 25 against 24.08). The ENT-S-139 controls `s139_61` / `s139_62` (two standing players)
pin the formula on players; the ledger's Irukandji cell (:515) and the ENT-S-135 (c) record read under this entry.

## PN-023 — The Rotator candidate's fan advance rides GeckoLib's per-frame animation dedup: the gyroscope freezes behind the single-player pause screen (ENT-S-147; ruled 2026-09-06, Q8 (b): a recorded divergence, deliberately not reproduced)

- **Original / classic:** the classic `RotatorModel.renderToBuffer` advances the per-entity fan angle `rf1` by 2° on every
rendered frame (orig ModelRotator.java:75-78 did the same in `render`), so the gyroscope keeps turning behind the pause menu,
where the world still renders while nothing ticks.
- **Candidate:** `RotatorGeoReplacement` advances inside GeckoLib's code-driven hook, which `GeoModel.handleAnimations` (4.8.4)
skips at offset 170 whenever `tickCount + partialTick` equals the manager's last update time for the same instance — every
paused frame with one Rotator in view, duplicate partial ticks above ~1000 FPS, a same-frame shadow pass. Two or more Rotators in
view keep spinning (the instance alternates). The mirror edge: an invisible candidate keeps advancing where vanilla skips the
classic draw.
- **Why not reproduced (owner, 2026-09-06):** the signature lives only behind the pause menu (and on an invisible or
super-fast-frame edge); no renderer plumbing is spent on what sits behind the pause menu. The hook keeps its shape; if a
per-render effect ever carries a live signature, the fix shape in ENT-S-147 (a per-render-pass descriptor hook for the
advance, the pose kept pure) is the one to build.
- **Player-visible:** only on the pause screen and on the two edges above; the dev-switch candidate only.

## PN-024 — Catmull-Rom keyframes in the replacement seam evaluate with the textbook spline arguments, not GeckoLib 4.8.4's (LANDED 2026-09-12 with item 15 — the shipped Beaver clip plays through the repaired seam; Q9 (a) ruled the divergence 2026-09-06; a deliberate divergence from the library, reported upstream; the Queen's native model on stock semantics until her own ruling)

- **The library (GeckoLib 4.8.4, `javap -p -c` of the pinned jar `geckolib-neoforge-1.21.1-4.8.4.jar`, sha at
  `~/.gradle/.../eb854c8ec53ef922a5f3877a1aa4c1ce1352e0ce/`):** `BakedAnimationsAdapter.buildKeyframeStack` builds
  consecutive keyframes as `(length, start = the previous key's value, end = this key's value)` (offsets 418-450:
  the start operand is `aload 6`, the previous iteration's value, once a previous pair exists at 429-441) and
  `addSplineArgs` gives every CATMULLROM keyframe two easing arguments: `args[0] = i == 0 ? frame.startValue() :
  frames.get(i - 1).endValue()` (119-146) and `args[1] = i + 1 < size ? frames.get(i + 1).endValue() :
  frame.endValue()` (147-183). Because `frames.get(i - 1).endValue()` IS `frames.get(i).startValue()`, the evaluator
  (`EasingType$CatmullRomEasing.apply` 69-113 → `getPointOnSpline(t, args[0], start, end, args[1])`, the textbook
  `0.5 (2 P1 + (P2 − P0) t + (2 P0 − 5 P1 + 4 P2 − P3) t² + (3 P1 − P0 − 3 P2 + P3) t³)` at 0-69) sees P0 == P1 on
  every segment and P3 == P2 on the last: each segment starts with half the chord slope instead of the
  neighbour-derived tangent — a kink at every key, an O(h²) error like linear interpolation with a worse constant.
  Measured on the Beaver's gait (amplitude 1.414 rad): not within 2.5e-3 rad by 97 keys per bone (6.85e-3 at 97),
  where linear needs 54 and the repaired spline 15; the arithmetic model of the library's rule reproduces the
  measured numbers to four digits (`phase_g_reports/animation_contract/demo_results.json` B, F).
- **The port (this seam only):** `OreSpawnGeoReplacementModel.getAnimation` serves every clip a replacement's
  controller asks for from a copy of the loaded animation file whose CATMULLROM keyframes carry the textbook
  neighbours (`SplineRepair`: P0 = the key before the segment's start, P3 = the key after its end; a loop clip
  continues across its seam, a play-once clip clamps; only the easing arguments change; once per loaded file by
  identity, so a resource reload repairs the new bake; idempotent). GeckoLib's evaluator is untouched. The
  keyframe reference leg proves the repaired curve against the classic `ModelBeaver.setupAnim` at 2.5e-3 rad with
  15 / 13 / 8 catmullrom keys per bone (the fewest; one fewer fails), and pins the library's own arguments on the
  same clip at 15 / 13 / 8 as the before (`before_after.md`).
- **Why diverge (owner, 2026-09-06, Q9 (a)):** with the library's arguments the density statement would be the
  linear row (54 / 41 / 19 keys per bone — 3.6-4.5× the keys an artist would edit); with the repair an artist's
  catmullrom clip plays as Blockbench previews it (Blockbench's own catmullrom uses the true neighbours), so the
  preview and the game agree more, not less — the parametrisation stated (item 15 refuter A, D5): the repaired spline
  is C1 at every key in each segment's NORMALISED time (`EasingType.apply` evaluates the segment at
  `currentTick / transitionLength`); uniform keys (the generator's rule, every key at `L k / (N − 1)`) make it C1 in
  tick time too; non-uniform keys do not (the refuter's hand-check: keys at 0 / 0.2 / 0.5 / 1.0 give slopes 0.1418 vs
  0.0945 rad/tick on the two sides of key 1). The repair is scoped to the replacement seam: the Queen's native
  `QueenModel extends GeoModel` keeps stock semantics until her own ruling; a resource pack's clip on a replaced
  species takes the repaired curve like the mod's own. Reported upstream (`upstream_report.md` in the same drafts:
  the defect, the offsets, a minimal reproduction, the expected P0).
- **Player-visible:** only on a replaced species that ships catmullrom clips — the Beaver since 2026-09-12 (item 15
  landed: its transcription plays through the repaired arguments on the GeckoLib candidate behind the dev switch; the
  classic renderer, the default, is untouched); every other shipped clip file is empty; the dev-switch candidate only.
- **Pins:** `KeyframeLegTests.kf_004_spline_repair_arithmetic` (the library's P0 == P1 against the pinned jar; the
  repair's neighbours, periodic / clamped, idempotent, the anchor and linear frames untouched; GeckoLib's evaluator
  over the repaired arguments equals the textbook spline and over its own the kinked one); the harness's
  `keyframe_reference_leg` (density as an output under the repaired evaluator, before/after presented).

## PN-025 — Both renderers emit a box's six faces in 1.21.1's `ModelPart.Cube` order (DOWN, UP, WEST, NORTH, EAST, SOUTH), not 1.7.10's `ModelBox` order (+X, −X, −Y, +Y, −Z, +Z) (ENT-S-152; ruled a disclosure 2026-09-12, item 17: no vanilla fork)

- **The original:** 1.7.10's `ModelBox` (`bis.<init>` 365-772 in the client jar, verified 2026-09-06) stores and `render`
  walks a cube's quads as +X, −X, −Y, +Y, −Z, +Z; a mirrored box swaps x1 / x2 before the corners and reverses each quad's
  vertices. NeoForge 21.1.223's `ModelPart.Cube.<init>` (365-785) stores DOWN, UP, WEST, NORTH, EAST, SOUTH and `compile`
  emits them in that order; the two orders agree only on the two Z faces coming last, −Z before +Z. Full derivation:
  `phase_g_reports/ent_s_152_cube_face_order_2026-09-06.md`.
- **The port:** the classic renderer IS vanilla's `ModelPart.Cube`, and the GeckoLib candidate is permuted into the same
  order at bake by the face-order contract (`orespawn:cube_face_order`, written by the converter from the `ModelPart.Cube`
  order, applied by `FaceOrder` in the replacement seam, checked by the asset audit, proven per face by the harness — 972
  faces over 9 captures on PurplePower). So the two renderers match EACH OTHER exactly, and both follow 1.21.1, not 1.7.10.
- **Why not reproduced (owner, 2026-09-12, item 17):** reproducing 1.7.10's order means forking vanilla's cube for one
  species (the `LayerDefinition` / `CubeListBuilder` bake path is closed around it) and permuting the candidate into that
  order instead — out of proportion for a rim shimmer; a disclosure, not a choice.
- **Player-visible:** nothing for a cutout rig (the depth test decides whatever the emission order). On the PurplePower
  orb, drawn blended with the depth mask on, the order decides which of a spoke's OWN faces shows through the others where
  an end face and a side face overlap at a spoke's tip seen obliquely: 1.7.10 drew the two end faces first, 1.21.1 draws
  the ±Y sides first, so one 0.55-alpha layer of the 0.75-grey texel is on or off a few pixels per spoke tip — a slightly
  different shimmer; the rings' look is unchanged.
- **Pins:** the harness's `G1 FACE ORDER PASS` on every rig that declares `cube_face_order: "classic"` (PurplePower); the
  asset audit's `GECKO_GEO_FACE_ORDER_*` rules; `PurplePowerPoseTests` (`FaceOrder.apply` on a GeckoLib bake with the
  seam's fallback policy).

## PN-026 — The PurplePower orb's hurt / death flash: 1.7.10 drew no red (its second, untextured `GL_EQUAL` pass brightened the front layer toward flat grey); the port draws NO overlay on the orb on both renderers once reading (2) lands, and 1.21.1's red until then (ENT-S-153; ruled 2026-09-12, item 18: `NO_OVERLAY`, the ENT-S-094 shape, one line each side; the code change deferred with the parity lanes)

- **The original:** `RendererLivingEntity.doRender` (`boh.a`, verified against the 1.7.10 client jar 2026-09-06) drew a
  SECOND `mainModel.render` pass after the normal one — lightmap and texturing off, blend on, `glDepthFunc(GL_EQUAL)`,
  `glColor4f(brightness, 0, 0, 0.4f)` in the hurt / death branch — but orig `ModelPurplePower.render` (:55) called
  `glColor4f(0.75, 0.75, 0.75, 0.55)` at the top of its own `render`, INSIDE that second pass, after the red: the red never
  reached a vertex. The orb never flashed red; on a hit or while dying its front-most surfaces brightened toward the flat
  0.75 grey (one more 0.55-alpha untextured layer over the front layer). Full derivation:
  `phase_g_reports/ent_s_153_hurt_flash_2026-09-06.md`.
- **The port:** NeoForge 21.1.223 has one draw with a per-vertex overlay coordinate (`LivingEntityRenderer.getOverlayCoords`,
  `OverlayTexture.v(hurt)` = 3 when hurt or dying, the overlay rows the constant red at alpha 178/255) that
  `rendertype_entity_translucent.fsh` mixes into every fragment; GeckoLib 4.8.4's `GeoReplacedEntityRenderer.getPackedOverlay`
  packs the same for a `LivingEntity`. Until the ruled change lands the orb flashes ~30 % red on both renderers; the
  ruled shape is `OverlayTexture.NO_OVERLAY` for the orb on both renderers (the ENT-S-094 shape: one line in the classic
  renderer's overlay call, one in the descriptor's overlay hook), i.e. no flash at all — closer to 1.7.10 than the red,
  but not 1.7.10's grey pass.
- **Why not reproduced (owner, 2026-09-12, item 18):** reading (3) — the 1.7.10 grey second pass — needs a mod-defined
  equal-depth render type and an untextured shader for a hurt frame of one species: out of proportion. Reading (2) is the
  ruling; its code change is deferred with the parity lanes (the cost rules: no parity code until the artist-tier cut-over).
- **Player-visible:** in 1.7.10 the orb brightened toward flat grey for a hurt frame; in the port it flashes red today and
  will not flash at all once the change lands. The harness emulates no overlay on either side, so neither is in the proof.
- **Pins:** none today; with the change: `PurplePowerPoseTests` gains the overlay pin (the classic renderer's and the
  descriptor's overlay both `NO_OVERLAY` for the orb), harness-neutral.

## PN-027 — The replacement seam draws every cube with its quads' true transformed normals: GeckoLib 4.8.4's `RenderUtil.fixInvertedFlatCube` is not applied (ENT-S-161; ruled 2026-09-13, item 3; a deliberate divergence from the library, reported upstream; the Queen's native renderer on stock semantics until her own ruling)

- **The library (GeckoLib 4.8.4, `javap -p -c` of the pinned jar `geckolib-neoforge-1.21.1-4.8.4.jar`):** the default
  `GeoRenderer.renderCube` (offsets 0-126) translates to the cube's pivot, rotates and translates back (2 / 7 / 12),
  takes the pose's normal matrix (16-22) and a copy of the pose (24-38), and for every non-null quad (54-123)
  transforms the quad's normal by the normal matrix (76-93), calls `RenderUtil.fixInvertedFlatCube(cube, normal)` (98)
  and hands the result to `createVerticesOfQuad` (115). `fixInvertedFlatCube` (0-129) multiplies the TRANSFORMED
  normal's x by −1 when x < 0 and the cube's size is 0 in y or z (0-42), its y by −1 when y < 0 and the size is 0 in
  x or z (43-85), its z by −1 when z < 0 and the size is 0 in x or y (86-128) — component-wise, after the transform.
  For an axis-aligned zero-thickness cube the two real faces' normals have no off-axis component and survive (the
  Vortex's 128×64×0 plate: the s4 surface leg 0 normal delta); for a ROTATED one the flips produce a vector that is
  neither the classic renderer's normal nor a reflection of it, and the two faces are no longer opposite. Measured on
  the Firefly's `wing_left` (0×6×2 under the part's Z 0.698 / Y 0.0175 rad bind): classic (0.766, 0.643, −0.017) and
  (−0.766, −0.643, 0.017), GeckoLib (0.766, 0.643, +0.017) and (−0.766, +0.643, +0.017) — the first Tier-2 slice's
  `RENDERER MAPPING MISMATCH` at the ruled 1e-6 normal epsilon; the Cloud Shark's `leftfin` / `rightfin` (0×3×7) and
  the Gold Fish's `Pectoralfin1..4` (0×3×5) and `Bottomfin1/2` (0×5×2) the same; the Cloud Shark's `fins` (0×10×10)
  are axis-aligned at bind but swing about Y under their clip, so the helper flips a component at every animated
  sample (refuter B: a stock delta of 1.41, cured to 1e-7 by the override).
- **The port (the seam only):** `entity/client/TrueNormalCubeRenderer.render` is GeckoLib's `renderCube` statement for
  statement minus the one call — the pivot translation and rotation, the normal matrix, the pose copy, the per-quad
  loop and `createVerticesOfQuad` are the library's; the quad's normal transformed by the pose's normal matrix is what
  every vertex carries. `OreSpawnGeoReplacedEntityRenderer.renderCube` (the base of every replacement renderer)
  overrides to it, and the harness's `G1ModelProbe.CapturingGeoRenderer.renderCube` draws through the same static, so
  the proof draws what the seam draws. `QueenRenderer` (her native `GeoEntityRenderer`) is not on the seam's base and
  keeps stock semantics until her own ruling; the benchmark's and the Queen part probe's headless renderers keep the
  default too (the ruling names the capturing renderer only). Out of scope, noted (refuter B): a geo cube with GeckoLib's
  own `mirror: true` has its WEST / EAST vertex sets swapped by `VertexSet.verticesForQuad` while `GeoQuad.build` keeps
  `direction.step()` as the normal, so the swapped faces carry the INWARD normal where the classic `Polygon` keeps
  outward ones — untouched by this override; no converted rig sets it (the converter emits `modelpart_mirror` and bakes
  the mirror into per-face UVs; 0 of the shipped and dropped geos), and the Queen's one such cube is on her native
  renderer outside the harness.
- **Why diverge (owner, 2026-09-13, item 3):** the classic renderer (`ModelPart.Cube.compile` → `Polygon` normals,
  transformed by the pose) lights the true transformed normal, and that is the parity target; the library's helper is
  a display heuristic for Blockbench-authored flat planes that the converter's rigs do not need (their flat cubes are
  the 1.7.10 models' own zero-width boxes, rotated by their parts). Confined to the seam, one static; the alternative —
  the converter emitting a minimal thickness — would make the geo no longer the literal conversion.
- **Upstream report (the text prepared for the issue; filing it is the owner's, as with PN-024):** "GeckoLib 4.8.4 (`geckolib-neoforge-1.21.1-4.8.4`): `GeoRenderer.renderCube`
  calls `RenderUtil.fixInvertedFlatCube(cube, normal)` on the pose-TRANSFORMED quad normal (offset 98 after the
  `Matrix3f.transform` at 90). The helper flips a negative component when the cube is flat on one of the other two axes
  (bytecode 0-129). For a flat cube that is rotated by its bone (or by the cube's own rotation) the transformed normal
  has off-axis components, and flipping them component-wise yields a vector that is neither the untransformed
  reflection nor a unit-preserving reflection of the true normal, and the two real faces of the plate stop being
  opposite — the plate is lit inconsistently with the rest of the model. Minimal reproduction: a geo with one cube of
  size [0, 6, 2] on a bone rotated [0, 0, 40] (degrees); render it and log the normal handed to `createVerticesOfQuad`
  for the two X faces against `poseStack.last().normal().transform(quad.normal())`: expected (0.766, 0.643, 0) and
  (−0.766, −0.643, 0); observed the face whose transformed y is negative comes back with y negated — (−0.766, +0.643, 0)
  instead of (−0.766, −0.643, 0) — while the other face is untouched, so the two faces are no longer opposite (dot
  product −0.17, not −1); the x component is never touched for a cube flat in x (its flip needs a cube flat in y or z).
  A bone rotated [0, −1, −40] affects both faces (the Firefly's own bind). Expected fix: apply the heuristic to the
  cube's LOCAL quad normal before the transform (or only when the transformed normal is still axis-aligned), or make
  it opt-in per model."
- **Player-visible:** nothing for a default install (the classic renderers are the default). On the GeckoLib candidates
  no landed rig carries a rotated zero-thickness cube today; the Firefly, Cloud Shark and Gold Fish (which do) rejoin
  the next Tier-2 slice on this path. The helper conditions on POSE-space components, so in-game — where the stack
  carries the entity's yaw from `applyRotations` — even an unrotated flat cube was rewritten at generic yaws (the
  Vortex's `size.z == 0` plate: its x and y components flipped when negative), which the harness's bind pose without
  yaw never showed (refuter A): on the Vortex candidate behind the dev switch the plate's lighting now follows the
  classic renderer's at every yaw, where before it flipped at some.
- **Pins:** the surface leg of every proof tree draws through the capturing renderer's override, and the three trees
  verify unchanged under it — no normal a proof COMPARES was ever altered by the helper: the harness pose carries no
  yaw, the Vortex plate's two real faces stay (0, 0, ±1), and its four zero-area faces — which the helper did rewrite
  under `size.z == 0` — are excluded on both sides by `drop_zero_area_faces` (the 2026-09-02 ruling) and draw no pixel;
  the face-order leg, which would compare them, runs only for a rig shipping `orespawn:cube_face_order` (PurplePower,
  no flat cube) — refuter B, from the override build's own dumps. The Firefly / Cloud Shark / Gold Fish surface legs at
  1e-6 when they land.
