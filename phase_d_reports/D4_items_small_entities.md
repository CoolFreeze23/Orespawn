# Phase D — slice D4: items/blocks/small-entity batch (2026-07-02)

Scope per the approved plan: ITEM-022/029/057/060/061/063/065, ANIM-016,
ENT-A-052/088, ENT-D-010/052, ENT-K-007/011/047/048/076/080/084,
ENT-S-025/034/036/047/059/078/085, plus the reconcile pool's D4-assigned PARTIAL
remainders ENT-A-001, ENT-A-098, ITEM-053, ITEM-062, ENT-D-011/039/041.
ENT-D-044 (GiantRobot laser) was already closed in D3 and was not redone.

Verification gate: full `.\gradlew.bat build` → BUILD SUCCESSFUL.
Ledger after slice: 442 terminal / 163 open of 605 (`tools/ledger_reconcile.py` green).

---

## 1. Dispenser behaviors — ITEM-063 (FIXED)

Port: `ModDispenserBehaviors.java`, called from mod init.

| Item | Original registration | Original behavior class |
|---|---|---|
| Irukandji Arrow | OreSpawnMain.java:5755 | MyDispenserBehaviorArrow.java:18-22 (pickup allowed) |
| WaterBall | :5756 | MyDispenserBehaviorWDCharge.java |
| SunspotUrchin | :5757 | MyDispenserBehaviorSunspotUrchin.java |
| Acid | :5758 | MyDispenserBehaviorAcid.java |
| IceBall | :5759 | MyDispenserBehaviorIceball.java |
| Dead Irukandji | :5760 | MyDispenserBehaviorDeadIrukandji.java |
| LaserBall | :5761 | MyDispenserBehaviorLaserball.java |
| 12 rock items | :5762-5773 | MyDispenserBehaviorRock.java:36-71 (stamps rock type 1-12) |

All original behaviors are one-liners around vanilla `BehaviorProjectileDispense`
(`func_82499_a`): velocity 1.1, inaccuracy 6.0, +0.1 vertical bias, aux effect 1002.
The port reproduces those numbers in a shared `DefaultDispenseItemBehavior`.
Projectile `(Level, x, y, z)` position constructors were added where missing
(Acid, DeadIrukandji, IceBall, IrukandjiArrow, LaserBall, SunspotUrchin,
EntityThrownRock).

## 2. Special-food effects — ITEM-029 (FIXED)

Original: `ItemSunFish.java:29-48` (shared food class, per-item switch).
Port: `FoodProperties.Builder.effect(...)` in `ModItems.java`.

| Food | Effects | Duration |
|---|---|---|
| Butter Candy | Speed I + Jump Boost I | 2000t |
| Cooked Bacon | Regeneration I + Strength I | 2000t |
| Crystal Apple | Regeneration I + Strength I | 3000t |
| Heart ("Love") | Regen IV + Strength III + Fire Res III + Resistance II; Speed I + Jump I | 6000t; 5000t |

The Heart's display name was corrected to the original **"Love"** (en_us.json).

## 3. Experience set — ITEM-057 (FIXED)

- The armor-set XP effect was already restored alongside ITEM-040 in C6 (sword and
  armor share the XP-bottle set-effect handler); nothing new was needed there.
- Item half closed in D4: `ItemExperienceTreeSeed` placement/consumption ported
  faithfully (place on valid soil, consume one seed).
- Invented Phase-10 leaf-harvest mechanic removed from `BlockExperienceLeaves`
  (the original leaves have no right-click harvest).
- The experience-tree growth/worldgen body (`generateExperienceTree`) is owned by
  WGEN-045 → slice D5.

## 4. Rock family — ITEM-022 (VERIFIED-CORRECT), ENT-K-076 (FIXED)

- **ITEM-022:** `RockBlock` is dead code in 1.7.10 — the class exists in the source
  tree but is never instantiated or registered (verified by scripted grep across
  OreSpawnMain and the full tree). No block form ever existed in-game; nothing to port.
- **ENT-K-076:** RockBase death drop restored — one rock item matching the mob's
  type (orig RockBase.java). While porting this, a systemic 0-vs-1-based indexing
  mismatch surfaced: `ItemRock` passed types 0-11 while `EntityThrownRock`'s damage
  switch and `RockBase`'s randomization used the original 1-12 scheme. `ModItems`
  registrations realigned to 1-12, so thrown-rock damage, placed-rock identity, and
  the death drop all resolve the same type.

## 5. Recipes — ITEM-060 (FIXED), ITEM-061 (FIXED), ITEM-062 (PARTIAL, narrowed)

- `skate_bow.json` (ITEM-060) — orig registration ported.
- `chest_from_crystal_planks.json` (ITEM-061) — orig OreSpawnMain.java:3083; the
  duplicate registration at :3209 is the same recipe, one JSON covers both.
- **Audit correction (ITEM-061):** the audit read :3084-3085 as a *piston*
  conversion. `field_151135_aq` is the 1.7.10 **wooden door**, and the shape is the
  2x3 plank door recipe — which the port's existing
  `oak_door_from_crystal_planks.json` already matched. The "divergent door
  substitution" flagged by the audit was in fact faithful; no piston recipe exists.
- ITEM-062 additions this slice: `red_bed_from_crystal_planks.json`,
  `raw_corn_dog.json`, `bucket_from_crystal_pink_ingot.json`,
  `cobweb_from_string.json` (plus the two above).
- ITEM-062 remainder: the 116 water-bucket spawn-block→spawn-egg conversions
  (OreSpawnMain.java:2667 ff.) are blocked on the ~105-type SpawnOres block pool
  owned by WGEN-005 → D5 structure/spawn-block slice.

## 6. Per-tier stat overrides — ITEM-065 (DEFERRED)

The original read per-tier weapon/armor/ore stats from its config file at init and
constructed items with those values. NeoForge 1.21.1 item registration is static and
registries freeze before any config phase, so faithful config-driven overrides would
require mutating frozen registries. Decision: keep the original *default* values
hardcoded (each verified number-by-number in earlier slices), document the platform
decision as **PN-013**, and archive the config system as 2.0 candidate **MOD-011**.

## 7. Immunities — ENT-K-007, ENT-S-025, ENT-S-047 (FIXED)

- Kyuubi: `fireImmune()` on the entity type (orig Kyuubi.java:47-48,
  fireResistance 1000 + isImmuneToFire) — its own fire attacks no longer damage it.
- SpitBug and Triffid: cactus + fall immunity per the orig damage-source filters.

## 8. Drops — ENT-A-088, ENT-D-052, ENT-K-084, ENT-S-034, ENT-K-011, ENT-A-098 (FIXED)

| Entity | Port artifact | Notes |
|---|---|---|
| Chipmunk | `loot_table/entities/chipmunk.json` + in-code tamed poppy | orig Chipmunk.java:231-242; tamed-only drop follows the established in-code convention |
| GoldFish | `gold_fish.json` | orig drop table |
| RubberDucky | `rubber_ducky.json` | orig drop table |
| Stinky | `stinky.json` + `OreSpawnTamed` NBT flag | tamed-only beef drop (orig Stinky.java:257-266), same convention as Gazelle/Camarasaurus |
| Lavafoam | `Lavafoam.getExpDrop` | Nether-only 5 + nextInt(5) + nextInt(5) XP (orig Lavafoam.java:110-116) |
| Coin | `coin.json` slot 8 | jackpot's empty CoinEgg slot now yields the ported coin spawn egg — closes the C1 PARTIAL (ENT-A-098) |

New item models: `coin_spawn_egg.json`, `tshirt_spawn_egg.json`.

## 9. Worms — ENT-S-085, ENT-S-078 (FIXED)

- **WormLarge** (`EntityWormLarge.java`): PlayNicely gate (orig WormLarge.java:192-198),
  nearest non-creative player within 8 (:199-202), 1-in-4 helmet-else-chestplate
  steal (:210-230), independent 1-in-4 held-item steal (:231-238) — the stolen stack
  is zeroed in the slot and scattered as an item entity, matching the orig
  `func_77972_a` ordering; death drops worm tooth / painting / 6 rotten flesh /
  6 leather (:352-377); "Large Worm" spawner bypass (:263-309).
- **WormSmall** (`EntityWormSmall.java`): surface-block check at every burrow-cycle
  step (orig WormSmall.java:107-110/124-127/139-142), tall grass counts as air
  (:104-106), 1-in-6 boots theft with damage (:188-195), night-only spawning
  (:214-216).

## 10. Peacock — ENT-K-047, ENT-K-048 (FIXED)

- Termite hunting: nearest living, visible Termite (orig Peacock.java:202-237),
  flat 6.0 mob-attack damage (:166-169), 1-in-200 revenge clear / nothing on
  peaceful (:181-200).
- Egg laying: clear air above, first half of the day, 50 ≤ y ≤ 100, at most 2
  buddies within 16/10/16 (:101-119 — restores the never-called `findBuddies()`),
  1-3 eggs at ±0-1 x/z, y+1 (:171-179,197-199).
- Breeding item confirmed as Crystal Apple (:259-261).

## 11. EasterBunny — ENT-D-010 (FIXED)

- Carrot taming restored.
- Mob-egg laying ported with the **full 115-entry** mob→spawn-egg lookup: the orig
  table was script-extracted (`tools/d4_bunny_patch.py`) and each
  `OreSpawnMain.XxxEgg` mapped to the port's `ModItems.XXX_SPAWN_EGG`.
- Natural spawns Easter-gated via `checkSpawnRules` (see §13).

## 12. UltimateFishHook — ENT-S-059 (FIXED)

Rebuilt on vanilla `FishingHook`. Access transformers added
(`META-INF/accesstransformer.cfg`): `nibble`, `currentState`, `catchingFish`,
`shouldStopFishing`, and the `FishHookState` inner enum.

- `shouldStopFishing` override recognizes the Ultimate Fishing Rod in either hand.
- Lava fishing: buoyancy in lava, the orig bite state machine driven through
  `catchingFish`, lava-appropriate particles.
- Hook is `fireImmune()` (orig UltimateFishHook.java:76-77, fireResistance 3000);
  lava catches spawn a fire-immune `EntityLavaLovingItem`.
- `getCatch` ports the orig weighted pools — junk, treasure, vanilla fish, OreSpawn
  water fish, OreSpawn lava fish — with Luck-of-the-Sea/Lure enchant scaling;
  caught gear receives random durability damage and a level-30 enchant per orig;
  XP orb dropped at the player on retrieve; `Stats.FISH_CAUGHT` awarded.
- Invented Phase-10 `+3 luck / +2 lure speed` constructor bonuses removed.
- Renderer switched from the port's no-op to vanilla `FishingHookRenderer`
  (generic over hook entities; bobber + line render correctly).

## 13. Seasonal gates — ANIM-016 (FIXED); closes ENT-D-011/039/041

- New `util/SeasonalDates`: `isHalloween()` / `isValentines()` / `isEaster()` from
  `LocalDate.now()` **at check time**. The original evaluated GregorianCalendar once
  at mod init, freezing the flags for the whole session; live evaluation is the
  deliberate deviation logged as **PN-014**.
- **Halloween (ENT-D-039/041):** the orig 22-biome Ghost/GhostSkelly w15 3-6 block
  (OreSpawnMain.java:4544-4565/:4522-4543, gate :4518-4521) added as
  `biome_modifier/halloween_ghosts.json` (20 modern biomes after mapping,
  deduplicated), runtime-gated in both entities' `checkSpawnRules`; the 5 year-round
  biomes are exempt via `OriginalSpawnGates.inYearRoundGhostBiome()`.
- **Easter (ENT-D-011):** EasterBunny `checkSpawnRules` denies natural spawns unless
  `isEaster()` (orig registration gate OreSpawnMain.java:4570-4571,4681).
- **Valentine's Day (Girlfriend):** giant angry variant per orig Girlfriend.java —
  2.5x8.0 `getDefaultDimensions`, 800 max HP, `girlfriendv.png` texture at 5x render
  scale (`GirlfriendRenderer`), `MyValentineTarget` goal (players + Boyfriends while
  angry; owner and tamed pets filtered), `FollowOwnerGoal` suspended, inWall damage
  immunity, `o_hurt` ambient voice; Rose Sword hits have a 1-in-4 cure (clear target,
  resize, drop Love items — misses still drop one Love). State persisted as
  `feelingBetter` NBT and synced (`DATA_FEELING_BETTER`) for the client renderer.

## 14. Projectile immunity lists — ENT-A-052 (FIXED), ENT-A-001 (closed)

- **BetterFireball:** `canHitEntity` pass-throughs — other BetterFireballs, Mothra,
  GodzillaHead, Royalty, plus Player/Dragon when `notme` is set; `onHitEntity`
  HP-halving exemptions — Royalty, Godzilla, GodzillaHead, PitchBlack, Kraken.
- **LaserBall (acid):** TrooperBug/SpitBug immunity restored — an acid projectile
  discards on impact with either bug (closes the C1 PARTIAL on ENT-A-001).

## 15. Shoes & GameController throwables — ITEM-053 (closed)

New `item/ItemShoes.java` drives all 5 items (red heels, black heels, slippers,
boots, game controller) — consume, throw sound, spawn `Shoes` entity with the item's
shoe id. `Shoes.onHitEntity` now carries the full original per-target damage table,
including Girlfriend/Boyfriend at 1.0f and the Valentine's-Day 10.0f override
(via `SeasonalDates`); impact plays both snowballpoof and reddust particles.

## 16. Verification-only closures — ENT-S-036, ENT-K-080 (FIXED, no code change)

- SunspotUrchin fire placement on block impact was restored in **C6** during
  ITEM-053's projectile pass; the ledger entry was never updated.
- Rotator `wasSpawnered` persistence was implemented in **D1**: set during
  `checkSpawnRules`, written to NBT, consumed by the despawn exemption.

## 17. Documentation

- `PARITY_NOTES.md`: **PN-013** (per-tier stat config hardcoded — platform decision),
  **PN-014** (seasonal gates evaluate live).
- `MODERNIZATION_NOTES.md`: **MOD-011** (config-driven per-tier stats, 2.0 candidate).
- Ledger patch: `tools/d4_ledger_patch.py` (26 resolutions appended, 6 PARTIALs
  closed, ITEM-062 narrowed). Reconcile green: 442 terminal / 163 open of 605.
