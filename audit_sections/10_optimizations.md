# Phase 4 — Optimization Review (proposals only, no code changed)

Scope: `src/main/java/danger/orespawn/**` (port code) + `src/main/java/de/dertoaster/multihitboxlib/**` (vendored lib, hot paths only).
Sampled: TheKing, TheQueen, Godzilla, GodzillaHead, Kraken, Mothra, GiantRobot, SpiderRobot, SpiderDriver, EntityRotator, EntityVortex, EntityWormSmall/Medium/Large, PitchBlack, Robot1–5, Alien, AlienBoss, BandP, EntityMantis, EntityLeon, Ghost, GhostSkelly, Urchin, Irukandji, Skate, SeaMonster, Hammerhead, CloudShark, EntityBrutalfly, EntityTermite, EntityRedAnt, Dragon, Fairy, EntityBee, EntityStinky, ThePrince family, Crab, Gazelle, Mosquito/CaveFisher/Dragonfly goals; all `*Handler` classes, `ModSpawnControl`, `MobzillaSpawnTracker`, `GirlfriendOverlay`, `OreSpawnItemRenderer`, `OreSpawnClient`, `EntityCullingMixin`, `world\` package, MHLib part-sync/network path.

General observation: most per-tick entity scans in the port are already probabilistically gated (`random.nextInt(N) == 0`), and entity renderers cache textures in `static final` fields. The findings below are the exceptions and the systemic patterns.

---

## HIGH

### H1. MHLib `getHitboxProfile()` does a registry lookup on every call — called per part per tick and per bone per frame

- **File:** `src/main/java/de/dertoaster/multihitboxlib/api/IMultipartEntity.java:345-357` (impl), `src/main/java/de/dertoaster/multihitboxlib/init/MHLibDatapackLoaders.java:34-36` (lookup)
- **Call sites in hot paths:** `IMultipartEntity.alignSubParts:176` (per part, per tick), `mhlibAiStep:268` (per tick), `alignSynchedSubParts:210` (per tick), `tryAddBoneInformation:370-391` (3–4 calls per bone, per render frame), `IBoneInformationCollectorLayerCommonLogic.onRenderBone:36-52` (per bone, per frame)
- **Cost:** `BuiltInRegistries.ENTITY_TYPE.getKey()` + datapack-registry map lookup + `Optional` allocation, multiplied by part count × tick rate on the server and bone count × frame rate on the client, for every multipart boss (Godzilla, TheKing, TheQueen, etc.).
- **Proposal:** Cache `Optional<HitboxProfile>` in a field on the entity (via `IMHLibFieldAccessor`), populated in `mhlibOnConstructor`, invalidated on datapack reload.
- **Behavior-neutral?** Yes (identical results; only invalidation on `/reload` must be wired).

### H2. MHLib sends a full multipart update packet every tick per tracked entity, even when nothing moved

- **File:** `src/main/java/de/dertoaster/multihitboxlib/mixin/entity/MixinServerEntity.java:24-33`; payload built in `src/main/java/de/dertoaster/multihitboxlib/network/server/SPacketUpdateMultipart.java:29-31, 67-77`
- **Cost:** Per tick × per multipart entity × per tracking player: one `SPacketUpdateMultipart` containing position/rot/size for *all* parts, plus `new ArrayList` + one `PartDataHolder` record per part per tick on the server.
- **Proposal:** Track last-sent part transforms; skip the send entirely when no part moved/rotated beyond epsilon and no part entity-data is dirty (or throttle unchanged syncs to every 10 ticks as a keepalive).
- **Behavior-neutral?** Effectively yes for idle bosses (positions identical); flag: moving parts unchanged, but a strict change-only send alters nothing visible.

### H3. MHLib master client streams `CPacketBoneInformation` continuously regardless of animation change

- **File:** `src/main/java/de/dertoaster/multihitboxlib/api/IMultipartEntity.java:283-319` (`updateSynching`, builds + sends every other client tick); bone collection per frame in `client/IBoneInformationCollectorLayerCommonLogic.java:34-61`; builder allocates `Optional`s + `HashSet`s per cycle (`network/client/CPacketBoneInformation.java:73-165`)
- **Cost:** Per-tick C2S packet per multipart entity the local player "masters", plus per-frame `synchronized tryAddBoneInformation` (`mixin/entity/MixinLivingEntity.java:134`) and Optional churn per bone.
- **Proposal:** Diff bone info against the last sent packet and only send on change (plus a low-rate keepalive so the 10-tick master-timeout in `updateSynching:288` doesn't rotate masters).
- **Behavior-affecting?** Mildly — server-side hitbox positions for *static* poses update less often (no visible difference); the keepalive is required to avoid changing master-election behavior.

### H4. EntityVortex runs up to 3 ungated AABB scans per tick (both sides)

- **File:** `src/main/java/danger/orespawn/entity/EntityVortex.java:101` (in `tick()`, runs client *and* server), `:176` (in `customServerAiStep()`), scan at `:277-281` = `getEntitiesOfClass(LivingEntity.class, inflate(16,10,16))` + full sort
- **Cost:** 2–3 entity scans + list sort per vortex per tick — the worst ungated per-tick scan found in the port.
- **Proposal:** Cache the found target in a field, rescan every 5 ticks, and reuse the result between `tick()` (particles only need "has target") and `customServerAiStep()`.
- **Behavior-affecting?** Yes — pull/aggro reaction latency goes from 0 to ≤5 ticks; particle onset delayed similarly. Flag for sign-off.

### H5. GirlfriendOverlay does an entity scan + string concat every rendered frame

- **File:** `src/main/java/danger/orespawn/client/GirlfriendOverlay.java:39-41` (per-frame `inflate(16)` AABB + `getEntitiesOfClass`), `:47` (`getCustomName().getString()`), `:59` (`health + "/" + maxHealth` concat)
- **Cost:** Per frame (60–240 Hz): AABB alloc, entity-list query with predicate, list alloc, 2+ string allocs per girlfriend.
- **Proposal:** Move the scan to a `ClientTickEvent.Post` handler that refreshes a static cached list (and pre-formatted name/health strings) every 10 ticks; `render` only draws the cache.
- **Behavior-neutral?** Visually neutral (HUD data at most 0.5 s stale); flag as cosmetic-latency only.

### H6. Kraken obstruction probe runs 95 block reads every server tick, unconditionally

- **File:** `src/main/java/danger/orespawn/entity/Kraken.java:188` (call in `customServerAiStep`), `:339-360` (`applyObstructionAvoidance`: 19×5 grid, `new BlockPos` per probe)
- **Cost:** ~95 `getBlockState` + ~95 `BlockPos` allocations per Kraken per tick (Krakens spawn in packs of 1–10 via `KrakenRevengeHandler` / reinforcements at `Kraken.java:247-258`).
- **Proposal:** Run the probe every 4–5 ticks and reuse one `BlockPos.MutableBlockPos`; the lift impulse can be scaled by the interval to keep net buoyancy identical.
- **Behavior-affecting?** Slightly (obstruction response latency up to 5 ticks); impulse-scaling keeps average motion equal. Flag.

### H7. Worm chain scans for players/worm-segments twice per tick with allocations

- **File:** `src/main/java/danger/orespawn/entity/EntityWormLarge.java:104` and `:165` (ungated `getNearestPlayer(8.0)` in both `aiStep` and `customServerAiStep`); `EntityWormSmall.java:87, 139` (same); `EntityWormMedium.java:90-97` and `:150-156` — worst: `getNearestEntity(EntityWormSmall.class, TargetingConditions.forNonCombat(), …, inflate(8.0))` allocates a fresh `TargetingConditions` + AABB and scans entities **twice per tick**
- **Cost:** 2 player-list scans per worm per tick; for Medium additionally 2 entity scans + 2 `TargetingConditions` allocations per tick.
- **Proposal:** Hoist `TargetingConditions.forNonCombat()` to a `static final`; compute the nearest-small-worm / nearest-player result once per tick and share between `aiStep` and `customServerAiStep` (they run in the same tick).
- **Behavior-neutral?** Hoisting + sharing within the same tick: neutral. Any further throttling (every 2–4 ticks) is behavior-affecting (tracking latency) — flag separately.

### H8. Crystal-dimension terrain rewrite scans the full world column and allocates a BlockPos per block

- **File:** `src/main/java/danger/orespawn/world/OreSpawnChunkGenerator.java:302-336` (`replaceTerrain`: 16×16 × full build height ≈ 98k `new BlockPos` + `getBlockState` per chunk), `:345-377` (`fillShallowWater`: second column scan, `new BlockPos` per Y)
- **Cost:** Per chunk generated in crystal-style dimensions: ~100k+ short-lived `BlockPos` allocations and full-height state reads, on worldgen worker threads.
- **Proposal:** Reuse one `BlockPos.MutableBlockPos` per column and start the downward scan at `chunk.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z)` instead of `getMaxBuildHeight()`; merge the `fillShallowWater` pass into the same column walk.
- **Behavior-neutral?** Yes (identical block output; heightmap start is safe because everything above surface is air).

---

## MEDIUM

### M1. ~35 entity classes reset MOVEMENT_SPEED base value every tick

- **File (representative):** `Godzilla.java:222`, `ThePrinceAdult.java:121`, `Baryonyx.java:68`, `EntityKyuubi.java:64`, `Basilisk.java:92`, `Camarasaurus.java:85`, `Girlfriend.java:118`, `Alien.java:102`, `Dragon.java:284`, `Boyfriend.java:119`, `Nastysaurus.java:72`, `Cryolophosaurus.java:66`, `Pointysaurus.java:83`, `BandP.java:92`, `EntityRat.java:85`, `EntityRubberDucky.java:120`, `EntitySpyro.java:129`, `EasterBunny.java:65`, `EntityLeafMonster.java:72`, `EntityMolenoid.java:91`, `ThePrinceTeen.java:120`, `CreepingHorror.java:66`, `DungeonBeast.java:79`, `EntityLeon.java:298`, `Cephadrome.java:127`, `Crab.java:98-99`, `Peacock.java:66`, `Cassowary.java:57`, `Alosaurus.java:84`, `EntityStinky.java:145`, `TRex.java:79`, etc. (`SeaViper.java:147-149` / `WaterDragon.java:149-151` are the only ones with genuinely dynamic values)
- **Cost:** Attribute-map lookup per entity per tick across ~140 entity types (vanilla `setBaseValue` early-exits on equal values, so no sync spam — but the lookup itself is pure waste for constant speeds).
- **Proposal:** For constant speeds, delete the per-tick call and set the value in `createAttributes()`; for the water/land mirrors (SeaViper, WaterDragon, Crab), cache the `AttributeInstance` in a field and only call `setBaseValue` when the medium changes.
- **Behavior-neutral?** Yes.

### M2. Godzilla allocates a Vec3 array + Vec3 per part every tick

- **File:** `src/main/java/danger/orespawn/entity/Godzilla.java:216-220`
- **Cost:** `new Vec3[allParts.length]` + one `Vec3` per part per tick (server + client).
- **Proposal:** Replace with three reusable `double[]` fields (or store prev positions on the parts themselves).
- **Behavior-neutral?** Yes.

### M3. ~37 sound getters allocate a new SoundEvent + ResourceLocation on every call

- **File (representative):** `GiantRobot.java:157-171`, `Robot1.java`, `Robot2.java`, `Robot3.java`, `Robot4.java`, `Robot5.java` (3 each), `ThePrincess.java`, `ThePrinceTeen.java`, `PitchBlack.java` (3 each), `SpiderRobot.java:149-150, 187-188`, `Ostrich.java`, `VelocityRaptor.java`, `Fairy.java`, `Lizard.java`, `Ghost.java`, `GhostSkelly.java`
- **Cost:** `SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(...))` per ambient/hurt/death sound query (ambient is polled by vanilla on a periodic schedule per mob). Two allocations + string handling per call; also bypasses the 100 already-registered `ModSounds` entries.
- **Proposal:** Replace with the corresponding `ModSounds.X.get()` holder (or a `static final SoundEvent` per class).
- **Behavior-neutral?** Yes (same sound id; registered events also serialize properly to clients).

### M4. Oversized-weapon culling mixin does 8 deferred-holder item checks per entity per frame

- **File:** `src/main/java/danger/orespawn/mixin/EntityCullingMixin.java:16-33`
- **Cost:** Runs inside `getBoundingBoxForCulling` for **every** entity (vanilla included) every frame: `getMainHandItem` + up to 8 `ModItems.X.get()` + `is()` checks, plus an `inflate(5.0)` AABB allocation when matched.
- **Proposal:** Replace the 8 checks with a single item-tag test (`mainHand.is(OVERSIZED_WEAPONS_TAG)`) or a lazily-built `static Set<Item>`; early-out via `stack.isEmpty()` already exists.
- **Behavior-neutral?** Yes.

### M5. Twelve big-mob renderers disable frustum culling entirely

- **File:** `QueenRenderer.java:50-52`, plus `shouldRender` overrides in `TheKingRenderer`, `GodzillaRenderer`, `GodzillaHeadRenderer`, `KingHeadRenderer`, `QueenHeadRenderer`, `KrakenRenderer`, `MothraRenderer`, `DungeonBeastRenderer`, `SeaMonsterRenderer`, `PitchBlackRenderer`, `LeonopteryxRenderer` (all in `src/main/java/danger/orespawn/entity/client/`)
- **Cost:** Full GeckoLib/vanilla model render every frame whenever the entity is loaded, even fully off-screen — for the largest models in the mod.
- **Proposal:** Keep culling but override `Entity.getBoundingBoxForCulling()` (or `GeoEntityRenderer#shouldRender` calling super with an inflated AABB) sized to the real part envelope (~30 blocks) instead of returning `true` unconditionally.
- **Behavior-neutral?** Visually neutral if the inflated box truly covers the part envelope; flag for visual verification on wing/tail extremes.

### M6. Alien torch scan probes 4,913 blocks per scan (docs claim 256)

- **File:** `src/main/java/danger/orespawn/entity/AlienTorchSeekGoal.java:134-153` (radius 8 → 17³ = 4,913 `getBlockState` per scan), throttle at `:70` (every ~30 ticks per alien)
- **Cost:** ~4,900 block reads per alien per ~30 ticks; multiplied by alien pack sizes.
- **Proposal:** Scan in expanding shells with early exit once a torch is found within the legacy ≈5-block "break" distance, or cap radius to match the documented 256-candidate budget; keep the `MutableBlockPos` (already used).
- **Behavior-affecting?** Early-exit on nearest: neutral. Radius reduction: behavior-affecting (smaller seek range) — flag.

### M7. Godzilla block crushing re-resolves deferred block holders per block

- **File:** `src/main/java/danger/orespawn/entity/Godzilla.java:405-418` (`crushBlocks`), `:374-402` (`isCrushable`: ~20 identity compares including 6 `ModBlocks.X.get()` deferred-holder resolutions per block), called twice every 4th tick over a 29×29 slice (`:611-626`)
- **Cost:** ~1,700 block reads + up to ~34k comparisons per 4 ticks while Godzilla is loaded; `BlockPos.containing` alloc per block.
- **Proposal:** Build a lazily-initialized `static Set<Block>` of non-crushables (resolving `ModBlocks` holders once) and iterate with a `MutableBlockPos`.
- **Behavior-neutral?** Yes.

### M8. King/Queen target scans sort the entire 80×64×80 entity list

- **File:** `src/main/java/danger/orespawn/entity/TheKing.java:1149-1173` (scan + `entities.sort` at `:1162`), `TheQueen.java:1254-1262` (`inflate(80,60,80)`), also `TheKing.java:1273-1285` (`msgToPlayers`/`findNearestPlayer` re-scan the same box)
- **Cost:** Every ~3–5 ticks per boss: full `LivingEntity` query over a 160×128×160 region + O(n log n) sort, when only the nearest suitable target + a "head exists" flag are needed.
- **Proposal:** Replace sort-then-scan with a single-pass nearest-suitable selection (track min distance); reuse one scan result for `findSomethingToAttack`/`findNearestPlayer` within the same tick.
- **Behavior-neutral?** Yes (nearest-first selection is preserved).

### M9. Ghost / GhostSkelly poll getNearestPlayer every idle tick

- **File:** `src/main/java/danger/orespawn/entity/Ghost.java:96-100`, `GhostSkelly.java:87-91` (runs whenever `attackCooldown == 0`, i.e. every tick while not recently attacking; second gated call at `Ghost.java:113-114` / `GhostSkelly.java:104-105` is fine)
- **Cost:** Player-list distance scan per ghost per tick (cheap per call, but ungated and the contact range is ~2 blocks).
- **Proposal:** Early-exit with a squared-distance check against the cached flight-target player, or gate the fallback poll to every 5 ticks.
- **Behavior-affecting?** Throttling adds ≤5 ticks of contact-damage latency — flag; the distance early-exit variant is neutral.

### M10. MHLib runs a hitbox-profile registry lookup in every LivingEntity constructor

- **File:** `src/main/java/de/dertoaster/multihitboxlib/mixin/entity/MixinLivingEntity.java:74-76` → `IMultipartEntity.mhlibOnConstructor` (`IMultipartEntity.java:469-505`, calls `getHitboxProfile()` up to 4×)
- **Cost:** Registry `getKey` + datapack lookup × 4 for **every** living entity constructed JVM-wide (vanilla mobs, projectile owners, etc.) — significant during chunk load / spawn waves.
- **Proposal:** Memoize "has profile" per `EntityType` in a static `Map<EntityType<?>, Optional<HitboxProfile>>` invalidated on datapack reload; bail out on the cached empty.
- **Behavior-neutral?** Yes.

### M11. MHLib part alignment allocates ~6 Vec3 per part per tick and does a linear `contains`

- **File:** `src/main/java/de/dertoaster/multihitboxlib/api/IMultipartEntity.java:162-193` (`alignSubParts`: chained `xRot/yRot/zRot/scale/add/subtract` Vec3 allocs + `new Vec3(scale,scale,scale)` per part; `synchedBones().contains(...)` linear scan per part), `:195-239` (`alignSynchedSubParts`: allocates a fallback `BoneInformation` per synced bone per tick even when the sync map has data)
- **Cost:** Per part per tick for every multipart boss on the server.
- **Proposal:** Precompute a per-part `isSynched` boolean at construction; fold the rotation/scale/translate chain into inline double math; only build the fallback `BoneInformation` when `syncMap` lacks the bone.
- **Behavior-neutral?** Yes.

---

## LOW

### L1. Boss bars updated every tick

- **File:** `PitchBlack.java:344`, `Kraken.java:155`, `Mothra.java:237`, `Godzilla.java:570`, `TheQueen.java:642`, `SpiderRobot.java:99`, etc.
- **Cost:** Negligible — `ServerBossEvent.setProgress` only broadcasts when the value changes. No action needed; listed to close out the checklist item.
- **Behavior-neutral?** N/A.

### L2. Sort-then-first-match pattern in ~25 small mobs

- **File (representative):** `Fairy.java:147`, `EntityMantis.java:232`, `BandP.java:218`, `EntityVortex.java:280`, `EntityLeon.java:557`, `Robot2-5`, `GiantRobot.java:142`
- **Cost:** O(n log n) sort of a small list every scan where a single-pass min would do.
- **Proposal:** Single-pass nearest-suitable selection helper shared via a util method.
- **Behavior-neutral?** Yes.

### L3. Armor auto-enchant check runs per armor piece per inventory tick

- **File:** `src/main/java/danger/orespawn/item/ItemOreSpawnArmor.java:130-150`
- **Cost:** One data-component presence check per OreSpawn armor stack per tick (cheap; enchant application itself happens once).
- **Proposal:** Apply enchants in `onCraftedBy`/on first pickup instead of polling in `inventoryTick`, or gate the check to every 20 ticks.
- **Behavior-affecting?** `onCraftedBy` migration changes when loot/creative-given stacks get enchanted (first tick vs. craft) — the 20-tick gate is neutral in practice but delays first-tick enchanting by ≤1 s; flag whichever is chosen.

### L4. Godzilla re-resolves the Mobzilla SavedData every tick

- **File:** `src/main/java/danger/orespawn/entity/Godzilla.java:579-581` → `MobzillaSpawnTracker.get():67-70`
- **Cost:** `overworld().getDataStorage().computeIfAbsent` map lookup per tick (markSpawned itself is idempotent-guarded).
- **Proposal:** Cache a local `markedSpawned` boolean on the entity and skip after the first success.
- **Behavior-neutral?** Yes.

### L5. Dragon rider-mode pushes via a broad `getEntities` query per tick

- **File:** `src/main/java/danger/orespawn/entity/Dragon.java:472-478`
- **Cost:** Per tick while ridden: AABB alloc + all-entity query (mirrors vanilla `pushEntities`, so acceptable).
- **Proposal:** Optional: pass a `pushable` predicate into the query to skip the post-filter.
- **Behavior-neutral?** Yes.

### L6. Worldgen flora helpers allocate BlockPos pairs in descending column loops

- **File:** `src/main/java/danger/orespawn/world/OreSpawnChunkGenerator.java:613-620` (flowers), `:636-643` (rice), `:659-666` (quinoa), `:682-689` (termite mounds), `:203-211` (scraggly trees), `:559-587` (ore veins: `new BlockPos` + `BlockState.equals` per cell)
- **Cost:** Per-chunk: a few thousand short-lived `BlockPos` allocations; `equals` instead of `==`/`is()` on interned states.
- **Proposal:** Reuse a `MutableBlockPos`; start scans at the heightmap; compare states with `.is(block)`.
- **Behavior-neutral?** Yes.

### L7. TheKing line-of-sight uses a manual 20+-step block ray per candidate

- **File:** `src/main/java/danger/orespawn/entity/TheKing.java:1185-1225` (`MyCanSee`), called per scan candidate from `isSuitableTarget:1138`
- **Cost:** Up to ~20–60 `getBlockState` + `BlockPos.containing` allocs per candidate per scan (scan itself throttled ~1/3–5 ticks).
- **Proposal:** Evaluate `MyCanSee` only for the current best candidate (after distance sort/min-pass), and reuse a `MutableBlockPos` in the march.
- **Behavior-neutral?** Yes if applied only to the selected candidate in the same order.

### L8. Spawn-cluster checks scan twice per spawn attempt

- **File (representative):** `EntityAnt.java:208-214`, `EntityCricket.java:156-162`, `Chipmunk.java:211-219`, `EntityTermite.java:221-228`, `Frog.java:273-283` — each has both a `size() <= N` check and a separate `size()` count over the same `inflate(20,10,20)` box
- **Cost:** Spawn-time only (not per-tick): duplicate entity query per `checkSpawnRules` call during spawn cycles.
- **Proposal:** Compute the count once and reuse for both checks.
- **Behavior-neutral?** Yes.

---

## Counts


| Impact | Findings |
| ------ | -------- |
| HIGH   | 8        |
| MEDIUM | 11       |
| LOW    | 8        |


## Notes on what was checked and found clean

- Event handlers (`KrakenRevengeHandler`, `ModLavaDropHandler`, `ModSpawnControl`, `MobzillaSpawnTracker`) are event-driven with lazy config maps — no per-tick work.
- `MobStats`/`ArmorStats`/`WeaponStats` are static record constants — no string lookups at runtime.
- Entity renderers cache `ResourceLocation` textures in `static final` fields; `OreSpawnItemRenderer` bakes models once and uses a `HashMap` per render — clean.
- `RepellentBlock` uses `randomTick` (not per-tick) for its 20-block repel scan — appropriate.
- Most mob target scans are gated behind `random.nextInt(N) == 0` (≈1 scan per N ticks average), matching the documented main-thread budget; `bossEvent.setProgress` per tick is broadcast-on-change in vanilla.
- `OreSpawnChunkGenerator`/`CrystalStructures` use `AtomicInteger` for cross-thread cooldowns (correct for the parallel worldgen pool); no `synchronized` bottlenecks found in worldgen.