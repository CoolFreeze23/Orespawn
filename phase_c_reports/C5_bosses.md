# Phase C slice 5 — Bosses (BOSS- series)

Scope: all `BOSS-` findings with **Status: DIVERGENT** and no prior resolution (16),
plus the two carried-forward PARTIAL remainders BOSS-006 (Queen phase armor) and
BOSS-026 (PrinceTeen size). 18 findings closed, all FIXED.

Every original value below was re-verified in the 1.7.10 CFR source before the fix.

## Fixed findings

| ID | Original (citation → value) | Old port (file:line) | New port | Notes |
|----|------------------------------|----------------------|----------|-------|
| BOSS-002 | orig `TheKing.java:86` — `setSize(22.0f, 24.0f)` | `ModEntities.java:193-194` `.sized(6.0f, 12.0f)` | `.sized(22.0f, 24.0f)` | Parent envelope restored; damage still routes through the 5 `OreSpawnPartEntity` regions (parent `isPickable()==false`). Bonus parity: `MyCanSee` start height `height*7/8` (port `TheKing.java:1196`) now evaluates to y+21 as in orig (`TheKing.java:879`). PlayNicely 5.5×6 shrink stays with BOSS-017. |
| BOSS-005 | orig `KingSpawnerBlock.java:51-56` 100-tick fuse on place; `:62-71` spawn at y+8, both blocks → air; `:88` `setGuardMode(1)`; `:66` `TheKingEnable` gate | `block/BossSpawnerBlock.java:44-57` — randomTick only, y+1, no guard mode, no gate, block above untouched | `BossSpawnerBlock` rewritten: `onPlace` schedules 100-tick fuse, `tick`+`randomTick` detonate (orig kept setTickRandomly(true) too), spawner + block above → air even when gated off, spawn at configurable y-offset (8) with random yaw + `playAmbientSound()` (orig :85-87), `setGuardMode(1)` for King/Queen, gate supplier checked before spawn | `ModBlocks.java` KING_SPAWNER now passes `8, THE_KING_ENABLE`. Not ported: orig's break-block→spawn quirk (`func_149664_b`/`func_149718_j` call updateTick — could double-spawn in 1.7.10); documented here instead. Orig spawns at the integer block corner; port keeps +0.5 centering (cosmetic, 22-block-wide boss). |
| BOSS-006 | orig `TheQueen.java:817-828` (`func_70658_aO`) — defense+2 below 2/3 max HP, +3 below 1/2, +5 below 1/3, each gated `player_hit_count < 10` | `TheQueen.java` — no `getArmorValue()` override (flat attribute 21 from Phase B) | `getArmorValue()` override added, branch-for-branch | Quirk preserved & documented: orig checks the +2 condition first and it is a superset of the others, so +3/+5 are unreachable — effective bonus is always +2. Mirrors TheKing's ported structure (which uses +1/+2/+3 with the same dead-branch shape). |
| BOSS-007 | orig `TheQueen.java:79` — `setSize(22.0f, 24.0f)` | `ModEntities.java:197-198` `.sized(12.0f, 16.0f)`; MHLib `the_queen.json` main-hitbox `[16, 12]` (profile overrides EntityType via `EntityEventHandler`) | `.sized(22.0f, 24.0f)` + profile main-hitbox `[22, 24]` | MHLib main-hitbox `size` is `[width, height]` (`EntityDimensions.scalable(x, y)` in `EntityEventHandler.java:37-38`), so the live box was 16w×12h. Bone-synced part boxes unchanged (they track the rendered model). PlayNicely shrink → BOSS-017. |
| BOSS-010 | 1.7.10 has no dormant phase — orig `TheQueen.java` `func_70097_a` damages from the first hit | `TheQueen.java:536-544` — first hit zeroed + 60 ticks of total invulnerability (`idle_to_attack` window), target cleared | First hit now only triggers the cosmetic 60-tick wake-up animation (`setTransitionTicks`), then falls through to the normal damage path; no absorption, no target clearing | The Geckolib blue→red phase shift is retained as pure visuals; `IS_AWAKE`/`TRANSITION_TICKS` gate nothing but the animation controller and texture. |
| BOSS-012 | orig `QueenSpawnerBlock.java:51-56,62-71,88` — identical pattern to King's, `TheQueenEnable` gate | generic `BossSpawnerBlock` (randomTick, y+1, no gate/guard) | shared `BossSpawnerBlock` fix; QUEEN_SPAWNER passes `8, THE_QUEEN_ENABLE` | Verified orig Queen block byte-for-byte matches the King pattern (fuse 100, y+8, guard 1, gate). |
| BOSS-016 | orig `Godzilla.java:176-189` — `orespawn:godzilla_living` (1-in-5) / `orespawn:alo_hurt` / `orespawn:godzilla_death` | `Godzilla.java:259-274` — ENDER_DRAGON_GROWL/HURT/DEATH | the three orespawn sound events (1-in-5 ambient gate kept) | All three sounds already registered in `assets/orespawn/sounds.json`; no registration work needed. |
| BOSS-018 | orig `ThePrince.java:215-224` — any food heals `healAmount × 10` (only if below max) | `ThePrince.java:336-338` — flat `heal(20.0f)` | `heal(food.nutrition() * 10.0f)` | 1.7.10 `func_150905_g` and 1.21 `FoodProperties.nutrition()` are the same hunger-point unit (e.g. cooked beef = 8 in both). |
| BOSS-020 | orig `ThePrince.java:267-286` — DIAMOND triggers the teen transform (owner, dist²<16, `ok_to_grow != 0`); no cake interaction exists | `ThePrince.java:311-329` — GOLD_INGOT grow trigger + invented CAKE counter-maxing shortcut | grow trigger → `Items.DIAMOND`; cake branch deleted | Gold ingot freed up (it was also colliding with the teen's invented regression, BOSS-029). |
| BOSS-024 | orig `ThePrince.java:746-761` — suitable targets: `EntityMob` OR Mothra/Butterfly/Cockateil/Dragonfly/Mosquito | `ThePrince.java:273-280` — Monsters only; the five prey classes explicitly returned `false` | prey classes return `true` | PlayNicely targeting gate (orig :765) remains BOSS-017's scope. |
| BOSS-025 | orig `ThePrince.java:354-361` — `nextInt(4)+1` × beef | `LT the_prince.json` — 1–4 diamond | 1–4 `minecraft:beef` | |
| BOSS-026 | orig `ThePrinceTeen.java:103` — `setSize(3.25f, 4.25f)` | `ModEntities.java:491-492` `.sized(2.0f, 3.0f)` | `.sized(3.25f, 4.25f)` | Completes the Phase B PARTIAL (HP/armor/speed/XP were fixed in B2). Rider seat offset (+2.75) was already orig-correct and is size-independent. |
| BOSS-029 | orig `ThePrinceTeen.java:1127-1230` (`func_70085_c`) — no teen→baby interaction exists | `ThePrinceTeen.java:575-596` — gold ingot spawned a baby ThePrince and discarded the teen | regression block removed | Port invention; orig growth is strictly one-way (baby→teen→adult). |
| BOSS-031 | orig `ThePrinceAdult.java:100` — `setSize(6.25f, 10.25f)` | `ModEntities.java:483-484` `.sized(4.0f, 6.0f)` | `.sized(6.25f, 10.25f)` | Mounted y-offset 9.25 (orig :295-297) already ported; model scale vs the new box needs an in-game look (pending manual test). |
| BOSS-032 | orig `ThePrinceAdult.java:400-412` — growcounter ticks/transform fires only when `activity==0 && riddenByEntity==null && !PEACEFUL && isTamed() && FullPowerKingEnable != 0`, threshold 288000; `:408` `king.setFree()` | `ThePrinceAdult.java:463-469` — gate was `isTame() && !hardcore`; no `setFree()`; `FULL_POWER_KING_ENABLE` repurposed as King ×2 damage (`TheKing.java:901-903,1095`) | full orig gate restored; `transformToKing()` calls `king.setFree()` (starts the isEnd=1 → full-power sequence); invented King ×2 melee/AoE modifiers removed | Chose removal over re-keying for the ×2 (it was a port invention; orig's "full power King" is the isEnd=2 phase reached via setFree). `FULL_POWER_KING_ENABLE` now means exactly what `FullPowerKingEnable` meant in 1.7.10. The invented `!hardcore` term dropped. |
| BOSS-035 | orig `ThePrinceAdult.java:313-315` — PrinceEgg ×1 | `LT the_prince_adult.json` — 5–15 diamond + 3–8 gold | `orespawn:prince_egg` ×1 | |
| BOSS-036 | orig `ThePrinceAdult.java:265-281` — ambient `king_living` only when `activity==1` && riderless (null while sitting); hurt `king_hit`; death `trex_death` | `ThePrinceAdult.java:599-614` — roar / alo_hurt / alo_death, ambient unconditional | orig trio with the orig ambient gating | Sound volume (orig :283-285 = 0.85f vs port 1.5f) is outside the cited range and untouched. |
| BOSS-042 | orig `ThePrincess.java:342-349` — `nextInt(4)+1` × beef | `LT the_princess.json` — 1–4 diamond | 1–4 `minecraft:beef` | |

## VERIFIED-CORRECT (audit wrong, port right)

None in this slice — every audited original value checked out this time. Two
audit imprecisions worth noting (didn't change the outcome):

- **BOSS-006**: the audit presents +2/+3/+5 as live "phase scaling"; in the orig
  the +3/+5 branches are dead code (the +2 health condition is checked first and
  is a superset). Ported verbatim anyway per the quirk-preservation rule.
- **Scope brief**: the slice description mentioned "SpiderRobot/GiantRobot bosses,
  Leonopteryx-as-boss" — no such BOSS-series findings exist; the BOSS- series
  covers King/Queen/Godzilla/Prince-family/Princess + framework only. The 16
  DIVERGENT-unresolved IDs matched the briefed count exactly.

## PARTIAL / deferred (owner)

- **PlayNicely shrink + targeting gates** (King/Queen/Godzilla sizes, Princess
  targeting) — owned by BOSS-017 (still open PARTIAL, not in this slice's scope).
- **Spawner break-trigger quirk** — orig `KingSpawnerBlock.func_149664_b/func_149718_j`
  also fired the spawn on block break/"canBlockStay" checks (and could double-spawn);
  intentionally not ported, documented above (BOSS-005/012 notes).
- **BOSS-043 (enable configs)** — `THE_KING_ENABLE`/`THE_QUEEN_ENABLE` were added
  here as a dependency of BOSS-005/012, which closes most of that PARTIAL finding,
  but BOSS-043's own resolution is left to its owning slice.

## Files changed

- `src/main/java/danger/orespawn/ModEntities.java` — King/Queen 22×24, teen 3.25×4.25, adult 6.25×10.25
- `src/main/java/danger/orespawn/ModBlocks.java` — spawner registrations gain y-offset 8 + enable gates
- `src/main/java/danger/orespawn/OreSpawnConfig.java` — `THE_KING_ENABLE`, `THE_QUEEN_ENABLE` (default true, orig OreSpawnMain.java:6434-6435)
- `src/main/java/danger/orespawn/block/BossSpawnerBlock.java` — fuse/y+8/guard/gate rewrite
- `src/main/java/danger/orespawn/entity/TheKing.java` — invented FULL_POWER ×2 modifiers removed
- `src/main/java/danger/orespawn/entity/TheQueen.java` — phase armor override; dormant invulnerability removed
- `src/main/java/danger/orespawn/entity/Godzilla.java` — custom sounds
- `src/main/java/danger/orespawn/entity/ThePrince.java` — heal ×10, diamond grow, cake removed, prey list
- `src/main/java/danger/orespawn/entity/ThePrinceTeen.java` — gold-ingot regression removed
- `src/main/java/danger/orespawn/entity/ThePrinceAdult.java` — transform gate, setFree, King-tier sounds
- `src/main/resources/data/orespawn/multihitboxlib/hitbox_profiles/the_queen.json` — main hitbox 22×24
- `src/main/resources/data/orespawn/loot_table/entities/the_prince.json` — beef 1–4
- `src/main/resources/data/orespawn/loot_table/entities/the_princess.json` — beef 1–4
- `src/main/resources/data/orespawn/loot_table/entities/the_prince_adult.json` — prince_egg ×1

## Build status

`.\gradlew.bat build --console=plain` — **BUILD SUCCESSFUL** (2026-06-12).
