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

## PN-010 — SpawnOres pool reduced to boss spawn blocks + ancient dried eggs (WGEN-005, Phase C7)

- **What:** 1.7.10 generated 28+/chunk "spawn ore" veins at Y50-128 drawn from a pool of
~105 spawn-block types (ChunkOreGenerator + OreSpawnMain SpawnOres stats). The port
currently generates only dragon/kraken boss spawn blocks (1/24 chunks each) and
ancient dried eggs (1/12) — a deliberate redesign kept for Phase C.
- **Why deferred:** the pool depends on dozens of per-mob spawn blocks that are
Phase D scope (with WGEN-042's structure/block backlog). Owner: Phase D
spawn-block pool restoration.

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
