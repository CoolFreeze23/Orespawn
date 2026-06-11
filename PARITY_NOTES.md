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

## PN-002 — ThePrince/ThePrincess: activity 2 no longer enables noPhysics (BUG-010, temporary)
- **Original:** `orig ThePrince.java:423` maps activity 2 (flying) to `noPhysics`, with
  `do_movement()` providing the actual flight steering.
- **Port (interim):** Flight movement is not yet ported (Phase D scope), so the
  noPhysics mapping is disabled — with it on and no steering, a hurt prince sank
  through terrain into the void. The original's 1/100-per-tick land/fly re-roll
  (`orig ThePrince.java:529-539`) IS ported, so the activity state machine matches.
  When flight lands in Phase D this note must be revisited and the mapping restored.

## PN-003 — TheKing: small-attacker deletion preserved (BUG-012)
- **Original:** `orig TheKing.java:824-826` deletes any attacking `EntityMob` with
  bb area < 3.0 and ignores the hit. Port matches exactly (`Monster` + `discard()`);
  players are structurally exempt (never `Monster`). Kept per ground rule 2; see MOD-002.

## PN-004 — Kraken grab: transport mechanism modernized, geometry identical (BUG-011)
- **Original:** Force-set position each tick for any caught entity.
- **Port:** Same hold point (15 blocks below the Kraken), same forced yaw, same
  release/damage rolls — but caught players are moved via `ServerPlayer.connection.teleport`
  + `hurtMarked` because raw `setPos` on a client-authoritative player causes
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
