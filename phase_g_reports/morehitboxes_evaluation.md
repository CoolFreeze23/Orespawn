# MoreHitboxes vs the vendored MultiHitboxLib — per-feature engineering comparison (2026-09-03, v2)

## 1. Ruling and provenance

**Ruling (owner, 2026-09-03, `phase_g_reports/phase_g_scope_addendum_2026-09-03.md:35-46`):** this is not a
migration decision. MHLib stays. The goal is to identify what MoreHitboxes does better and port those pieces
into the vendored MHLib under MoreHitboxes' MIT license with attribution, choosing the most performant design
for each. Running both libraries is off the table unless the bytecode shows their mixin targets do not collide.
Report only; nothing in the repository changed.

**Verification status (2026-09-03):** refuted once, on the session model, after four launches had died on server-side
API errors before doing any work (the owner's ruling: rerun those, redo anything that ran on another model). The refuter
upheld the mixin table, the collision verdict, the rig counts, the per-bone allocation counts, the wire figures, the
culling and attribution facts and every Section 5 counter line, and found one blocking defect that this text now
carries: the orchestrator's interim correction ("harvest 1 has no server-visible behaviour change") was wrong, because
MHLib's once-per-tick stamp is a per-renderer field that wedges on a frame spanning two ticks and starves all but one
entity when several share a renderer (BUG-044, Section 3.6). Its minors are applied: per-frame totals restated (≈95k
allocations/s, not 107k), the bridge hook proven rather than plausible (OPT-028), MoreHitboxes' `setPos` count
corrected, the knockback mechanism re-attributed to `Entity.push` on a part that never integrates it, the Queen's
knockback resistance read (1.0), the cull radius given with box height, `require = 0` dropped as a differentiator, six
registered payloads, the dump inventory in Section 7 corrected. Every harvest slice still gets two refuters before wiring.

**Artifact provenance** (from the superseded first report's table, `phase_g_reports/morehitboxes_evaluation_v1_superseded.md:39-48`):
Modrinth project `more-hitboxes` (id BOVAW87Z, MIT); version `1.21.1-1.9.4-alpha-neoforge` (id 1Cu922wS,
published 2026-03-31); file `morehitboxes-neoforge-1.21.1-1.9.4-alpha.jar`, 117,838 bytes, sha512 0025360f…
/ sha1 fb49a064… (recomputed equal); source github.com/DarkPred/MoreHitboxes branch `1.21.1` head 88899b3;
built against GeckoLib 4.8.3 / NeoForge 21.1.66 (`gh_gradle_props_1211.txt:17-21`). Our pinned renderer is
GeckoLib 4.8.4 (`gradle.properties:17`).

Evidence locations used below: MoreHitboxes sources `scratchpad/morehitboxes/src/**` (fetched from 88899b3),
bytecode `scratchpad/morehitboxes/javap_mixins_verbose.txt` (annotations) / `javap_mixins_bodies.txt` /
`javap_internal_bodies.txt` (method bodies, `javap -c -p` over the shipped jar); GeckoLib 4.8.4 dumps
`scratchpad/morehitboxes/geckolib_GeoBone.txt`, `geckolib_GeoEntityRenderer.txt`, `scratchpad/GR.javap.txt`
(GeoRenderer), `GRER.javap.txt`; vanilla dumps `scratchpad/E.javap.txt` (Entity), `LE.javap.txt` (LivingEntity).
Vendored MHLib paths are relative to `src/main/java/de/dertoaster/multihitboxlib/`.

---

## 2. Mixin target collision

Registered sets: MHLib `src/main/resources/multihitboxlib.mixins.json:7-21` (7 common + 4 client, plugin
`MHLibPlugin` gates the three geckolib mixins on GeckoLib being loaded, `mixin/MHLibPlugin.java:20-24`; no
`injectors.defaultRequire`). MoreHitboxes `jar/morehitboxes.mixins.json` (9 common + 7 client,
`injectors.defaultRequire = 1`, refmap named but absent from the jar — mojmap runtime needs none).
Every mixin below is proven from the annotation dump (`javap_mixins_verbose.txt:<line>`) or the vendored source.

| Library | Mixin class | Target class | Target method + descriptor | Injection | require | remap | Shared? |
|---|---|---|---|---|---|---|---|
| MHLib | entity.MixinLivingEntity (`:105-111`) | LivingEntity | `<init>(EntityType;Level)V` | @Inject TAIL | cfg default | default | no |
| MHLib | entity.MixinLivingEntity (`:140-150`) | LivingEntity | `aiStep()V` (LE.javap:7261) | @Inject TAIL | cfg default | default | no (MoreHitboxes hooks `Mob.aiStep`, a different method that calls this one) |
| MHLib | entity.MixinLivingEntity (`:153-163`) | LivingEntity | `tick()V` (LE.javap:6532) | @Inject TAIL | cfg default | default | no |
| MHLib | entity.MixinLivingEntity (`:204-211`) | LivingEntity | `isPickable()Z` (LE.javap:8247) | @Inject RETURN, cancellable | cfg default | default | no |
| MHLib | entity.MixinLivingEntity (`:184-200`) | LivingEntity | `setId(I)V`, `isMultipartEntity()Z`, `getParts()[PartEntity` | method overrides (merged) | n/a | n/a | `setId`: MoreHitboxes injects `Entity.setId` RETURN — different class; MHLib's override calls `super.setId` so both would run, each re-id'ing its own parts |
| MHLib | entity.MixinServerEntity (`:80-121`) | ServerEntity | `sendDirtyEntityData()V` | @Inject HEAD | cfg default | default | no |
| MHLib | entity.MixinServerEntity (`:173-180`) | ServerEntity | `addPairing(ServerPlayer)V` | @Inject TAIL | cfg default | default | no |
| MHLib | geckolib.MixinGeoEntityRenderer (`:20,28-35`) | GeoEntityRenderer (priority MAX_VALUE) | `<init>(EntityRendererProvider$Context;GeoModel)V` (GER javap:30) | @Inject TAIL | cfg default | default | no |
| MHLib | geckolib.MixinGeoEntityRenderer (`:47-55`) | GeoEntityRenderer | `renderRecursively` bare name — matches the typed method `(PoseStack;Entity;GeoBone;RenderType;MultiBufferSource;VertexConsumer;ZFIII)V` (GER javap:611) and, by name, the synthetic bridge `(…GeoAnimatable…)` (GER javap:1438) | @Inject HEAD + TAIL | cfg default | false | **YES — MoreHitboxes GeoEntityRendererMixin** |
| MHLib | geckolib.MixinGeoRenderer (`:14,27-35`) | GeoRenderer (interface, priority MAX_VALUE-1) | default `renderRecursively(…GeoAnimatable…)V` (GR.javap:373) | @Inject HEAD + TAIL | cfg default | false | no (GeoEntityRenderer overrides it; never executes for the Queen) |
| MHLib | geckolib.MixinGeoReplacedEntityRenderer (`:20,28-35,47-55`) | GeoReplacedEntityRenderer (priority MAX_VALUE) | `<init>(Context;GeoModel;GeoAnimatable)V` (GRER javap:19); `renderRecursively` (GRER javap:661) | @Inject TAIL; HEAD + TAIL | cfg default | default; false | no (MoreHitboxes never touches the replaced renderer) |
| MHLib | minecraft.MixinCompressionDecoder (`:11-14`) | CompressionDecoder | `decode` | @ModifyConstant int MAXIMUM_UNCOMPRESSED_LENGTH | cfg default | default | no |
| MHLib | minecraft.MixinPacketBuffer (`:11-14`) | FriendlyByteBuf | `readNbt()CompoundTag` | @ModifyConstant long 2097152 | cfg default | default | no |
| MHLib | minecraft.client.MixinClientboundCustomPayloadPacket (`:11-16`) | ClientboundCustomPayloadPacket | `<init>` | @ModifyConstant int 1048576 | cfg default | default | no |
| MHLib | minecraft.MixinMinecraft (`:15-22`) | Minecraft | `<init>` at INVOKE `PackRepository.<init>([RepositorySource)` | @ModifyArg index 0 | cfg default | default | no (MoreHitboxes' MinecraftMixin hooks `startAttack`) |
| MHLib | accessor.AccessorClientLevel (`:21-27`) | ClientLevel | field `partEntities` | @Accessor | n/a | default | no |
| MHLib | accessor.AccessorEntityRenderer (`:9-14`) | EntityRenderer | field `entityRenderDispatcher` | @Accessor | n/a | default | no |
| MHLib | minecraft.client.MixinEntityRenderDispatcher (`:21-42`) | EntityRenderDispatcher | `renderHitbox` INVOKE `LevelRenderer.renderLineBox` ordinal 1, LocalCapture | @Inject | — | — | **NOT REGISTERED** (absent from `multihitboxlib.mixins.json`): dead source. If it were, it would share `renderHitbox` with MoreHitboxes' EntityRenderDispatcherMixin (ordinal 0 vs 1, distinct instructions) |
| MoreHitboxes | ClientEntityMixin (`verbose:360-370`) | Entity | `onClientRemoval()V` (E.javap:635) | @Inject RETURN | 1 (cfg) | default | no |
| MoreHitboxes | ClientMobMixin (`verbose:524-534`) | Mob | `aiStep()V` | @Inject RETURN | 1 | default | internal only (same target as MobMixin) |
| MoreHitboxes | EntityHitResultMixin (`verbose:662`) | EntityHitResult | — (adds field `moreHitboxes$part` + 2 methods) | @Unique field | n/a | n/a | no |
| MoreHitboxes | EntityMixin (`verbose:914-925`, `972-984`) | Entity | `onSyncedDataUpdated(EntityDataAccessor)V` (E.javap:8190) at INVOKE `Entity.refreshDimensions()V` (before, and shift AFTER), @Share LocalDoubleRef | @Inject ×2 | 1 | default | no |
| MoreHitboxes | EntityMixin (`verbose:1043-1053`) | Entity | `refreshDimensions()V` (E.javap:8218) | @Inject RETURN | 1 | default | no |
| MoreHitboxes | EntityMixin (`verbose:1099-1101`) | Entity | `getBoundingBoxForCulling()AABB` (E.javap:8480) | @ModifyReturnValue (MixinExtras) RETURN | 1 | default | **shared with OreSpawn's own `danger.orespawn.mixin.EntityCullingMixin`** (`:32-33`, @Inject RETURN cancellable, same target) — not a library, but same jar; both priority 1000, so application order falls to config order; harmless either way (each returns `original` for the other's entities) |
| MoreHitboxes | EntityMixin (`verbose:1147-1149`) | Entity | `setBoundingBox(AABB)V` (E.javap:8486, final) | @Inject RETURN | 1 | default | no |
| MoreHitboxes | EntityMixin (`verbose:1217-1219`) | Entity | `setId(I)V` (E.javap:543) | @Inject RETURN | 1 | default | see MHLib setId row |
| MoreHitboxes | EntityMixin (`verbose:1280-1282`) | Entity | `remove(RemovalReason)V` | @Inject RETURN | 1 | default | no |
| MoreHitboxes | EntityRenderDispatcherMixin (`verbose:1953-1966`) | EntityRenderDispatcher | `renderHitbox` at INVOKE `LevelRenderer.renderLineBox(PoseStack;VertexConsumer;AABB;FFFF)V` ordinal 0, shift AFTER | @Inject | 1 | default | no (MHLib's is unregistered) |
| MoreHitboxes | GeckoLibMobMixin (`verbose:2102`) | Mob | — (adds `moreHitboxes$renderTick` + 2 methods) | @Unique | n/a | n/a | no |
| MoreHitboxes | GeoEntityRendererMixin (`verbose:2523-2538`) | GeoEntityRenderer (priority 1000) | `renderRecursively(PoseStack;Entity;GeoBone;RenderType;MultiBufferSource;VertexConsumer;ZFIII)V` at INVOKE `GeoEntityRenderer.applyRenderLayersForBone(…)V` shift AFTER | @Inject | **0** | **false** | **YES — MHLib MixinGeoEntityRenderer** |
| MoreHitboxes | LevelMixin (`verbose:2798-2818`) | Level | `getEntities(Entity;AABB;Predicate)List` at INVOKE `LevelEntityGetter.get(AABB;Consumer)V` shift AFTER, @Local list | @Inject | 1 | default | internal only (same target as NeoForgeLevelMixin) |
| MoreHitboxes | MinecraftMixin (`verbose:3013-3024`) | Minecraft | `startAttack` at INVOKE `MultiPlayerGameMode.attack(Player;Entity)V` | @WrapOperation (MixinExtras) | 1 | default | no |
| MoreHitboxes | MobMixin (`verbose:3194-3204`) | Mob | `aiStep()V` | @Inject RETURN | 1 | default | internal only |
| MoreHitboxes | NeoForgeLevelMixin (`verbose:3459-3462`) | Level (@Debug export) | `getEntities(Entity;AABB;Predicate)List` at INVOKE `Predicate.test(Object)Z` | @WrapOperation | 1 | default | internal only |
| MoreHitboxes | NeoForgeLevelMixin (`verbose:3550-3573`) | Level | `getEntities(EntityTypeTest;AABB;Predicate)List` | @Inject RETURN, @Local list | 1 | default | no |
| MoreHitboxes | NeoForgeMobMixin (`verbose:3806`) | Mob | `getParts()[PartEntity`, `isMultipartEntity()Z` | method overrides (merged) | n/a | n/a | no direct: MHLib overrides the same two names on LivingEntity; a Mob-level override would shadow MHLib's for every Mob (the robots, the Queen) |
| MoreHitboxes | PlayerMixin (`verbose:3954-3965`) | Player | `attack(Entity)V` HEAD, argsOnly | @ModifyVariable | 1 | default | no |
| MoreHitboxes | PlayerMixin (`verbose:4005-4018`) | Player | `attack` at INVOKE `Entity.hurt(DamageSource;F)Z` | @ModifyReceiver (MixinExtras) | 1 | default | no |
| MoreHitboxes | ProjectileUtilMixin (`verbose:4107-4131`) | ProjectileUtil | `getEntityHitResult(Entity;Vec3;Vec3;AABB;Predicate;D)` and `(Level;Entity;Vec3;Vec3;AABB;Predicate;F)` | @ModifyReturnValue ×2 | 1 | default | no |
| MoreHitboxes | TransientEntitySectionManagerMixin (`verbose:4413-4432`) | TransientEntitySectionManager | `addEntity(EntityAccess)V` | @Inject HEAD (+ anonymous EntityInLevelCallback) | 1 | default | no |
| MoreHitboxes | AbstractClientPlayerMixin (`verbose:189`) | AbstractClientPlayer | `isCloseEnough(Entity;D)Z` (constant pool #7 references `Player.isCloseEnough`) | method override (merged) | n/a | n/a | no |
| OreSpawn | mixin.MountCameraMixin (`:33-47`) | Camera | `setup` at INVOKE `Camera.move(FFF)V` ordinal 0 | @WrapOperation | 1 | default | no |

**Verdict:** the two libraries collide on exactly one target method,
`GeoEntityRenderer.renderRecursively(PoseStack, Entity, GeoBone, …)`: MHLib injects HEAD and TAIL
(priority `Integer.MAX_VALUE`, applied last), MoreHitboxes injects after the `applyRenderLayersForBone` call
(priority 1000, `require = 0`). Both are non-cancelling callbacks at distinct instructions and neither library
uses @Overwrite/@Redirect on it, so Mixin would apply both and the runtime order inside one bone would be
MHLib-HEAD → applyRenderLayersForBone (MHLib collector layer) → MoreHitboxes callback → renderChildBones →
MHLib-TAIL. Mechanically loadable, but the ruling's bar ("targets don't collide") is not met, and the
semantic overlap is total (both reposition parts every tick — MHLib at `LivingEntity.aiStep` TAIL, MoreHitboxes at
`Mob.aiStep` RETURN, which runs after it and would win — and both read every bone every frame). Side by side
stays off the table; harvest into MHLib. Second-order finding (OPT-028): MHLib's bare-name selector also hooks the
synthetic bridge, and every bone enters through that bridge (GeoRenderer's erased `invokeinterface`, GR.javap:272/487
→ GER.javap:1438-1453 → the typed method), so MHLib's HEAD/TAIL push/pop runs twice per bone for every GeckoLib
entity; MoreHitboxes' full-descriptor selector cannot. The fix is Section 4 item 6.

---

## 3. Per-feature comparison

Rig facts used for counts: the Queen's geo has **110 bones, 130 cubes, max depth 10**, and the 10 synched
bones sit at depths 1-7 (`assets/orespawn/geo/entity/the_queen.geo.json`, counted; `the_queen.json:80-91`).
Robots: 8 (`spider_robot.json:57-64`) and 6 (`ant_robot.json:43-48`) server-fed parts, no synched bones,
vanilla MobRenderers, so the collector never runs for them (`IBoneInformationCollectorLayerCommonLogic.java:26-34,68-70`).

### 3.1 Trust model

**MoreHitboxes:** no client→server payload exists: `NetworkRegistry.java:12-23` registers one `playToClient`
payload (`SyncHitboxDataPayload`, hitbox table), the "reply" only finishes the configuration task
(`ClientPayloadHandler.java:13-21`, `SyncHitboxDataTask.java:19-22`). Server parts are placed at the static
datapack offset rotated by `yBodyRot` and scaled by `Mob.getScale()` (`MultiPart.java:59-60`; bytecode
`MultiPart.updatePosition` 86-157), every tick at `Mob.aiStep` RETURN on both sides (`MobMixin.java:14-22`).
Only the client has the `AnimationOverride` (`GeoEntityRendererMixin.java:31-35`, bytecode 60-105), so client
parts follow the animation and server parts never do. What the server validates: vanilla reach against the part
the client named (NeoForge `getEntityOrPart`; the part's server box is the static one), then
`NeoForgeMultiPart.hurt` → `partHurt(parent)` (`NeoForgeMultiPart.java:51-57`, bytecode 10-28). The client
side widens its own reach test to any part (`AbstractClientPlayerMixin.java:20-33`). No master, no election,
no plausibility bound, no server-side animation.

**MHLib:** master election among trackers (`api/IMultipartEntity.java:713-746`, re-election on a 10-tick silence
`:471-479`), the master client streams `CPacketBoneInformation` (`:452-559`, change-only with an 8-tick keepalive
`:445`), the server accepts it only from the master (`network/server/CPacketHandlerBoneInformation.java:43-52`),
replaces the sync map wholesale (`IMultipartEntity.java:129-150`) and applies it to the server hurtboxes each
tick (`alignSynchedSubParts :316-380`). Every client also places the Queen's parts from its own render when
`trust-client` is true (`client/IBoneInformationCollectorLayerCommonLogic.java:86-91`) — with
`max-deviation-from-server` at its 0.0 default (`entity/hitbox/SubPartConfig.java:24`; `the_queen.json` sets none)
the S2C part stream then overwrites them every tick anyway (`network/client/SPacketHandlerUpdateMultipart.java:32-43`:
`dist > 0` is always true). No plausibility bound (Slice 5 item 5, addendum `:31-34`).

**Cheaper:** per frame — n/a here (see 3.6/Section 5 for the capture cost that feeds it). Per tick — MoreHitboxes:
0 server-side trust work; MHLib: `updateSynching` (≤2 queue ops, `:471-487`) + per accepted packet 1 map clear +
10 `put` (`:138-146`) + `alignSynchedSubParts` ≈ 10 allocations per synced part (1 `BoneInformation` + 1 `Vec3` from
`bi.scale` `:375`, then `applyInformation` `entity/MHLibPartEntity.java:423-438`: 3 pivot `Vec3` + 1 scale + 1
subtract + `setPos` `:255-263` = 2 `EntityDimensions` + 1 `AABB`) ≈ 100 allocations/tick for the Queen. On the
wire — MoreHitboxes 0 B/tick; MHLib ≈ 1.2-1.6 KB/tick C2S while animating plus 468 B/tick per tracker S2C (3.6).
Correctness — MHLib only: it is the only one whose server hurtboxes follow the animation.

**Cost to port:** nothing to port — MoreHitboxes has no server-side trust mechanism to harvest. The Slice 5
plausibility bound stays an MHLib-native task: ≈80 lines in `CPacketHandlerBoneInformation.handleServer` /
`processBoneInformation` (clamp `|worldPos − parent.position()|` to a per-profile envelope radius, drop the packet
on breach; the radius can be the same per-profile constant Section 3.4 derives). Tests touched: none of the four
suites read bone packets; `HitboxPartTests.s4_master_election_gated_for_boneless_profiles` (`:361`),
`RideTests.s5_part_stream_gate_predicate` (`:458`), `AntGaitTests.s5b_ant_stream_gate_predicate` (`:501`) pin the
election/stream gates and stay green if the gate code is untouched. Chosen design: keep MHLib's model; add the
bound. Why: it is the only design that puts animated hurtboxes on the server at all.

### 3.2 Server-side part placement

**MoreHitboxes:** `MobMixin.tickCustomParts` at `Mob.aiStep` RETURN (`MobMixin.java:14-22`): per part
`updatePosition()` (`MultiPart.java:44-64`): 6 field writes, then either `parent.position().add(localPos)` (client,
override present: 1 `Vec3`) or `new Vec3(offset).yRot(-yBodyRot).scale(getScale())` + `add` (server: 4 `Vec3`,
bytecode 86-157), then `Entity.setPos(Vec3)` (E.javap:742-761: 1 `Vec3` in `setPosRaw` + 1 `AABB`; it never calls
`getDimensions`) = **6 allocations per part per tick on the server**, plus `AnchorDataInternal.updatePositions`
(4 `Vec3` per anchor, `:45-56`). `NeoForgeMobMixin.getParts()` allocates a fresh `PartEntity[]` on **every call**
(`NeoForgeMobMixin.java:22-32`, bytecode 26-35). Parts are `noPhysics` and never saved (`NeoForgeMultiPart.java:37,83-85`).
No pivot, no rotation, no size callback; the animation scale only changes dimensions when it changes
(`setOverride :103-110`).

**MHLib:** `alignSubParts` at `LivingEntity.aiStep` TAIL (`mixin/entity/MixinLivingEntity.java:140-150` →
`IMultipartEntity.java:395-436` → `:250-316`): OPT-019 inline double math, **0 allocations in the transform**,
`setScaling(float,float)` value-reusing (`MHLibPartEntity.java:409-417`), then `setPos` (`:255-263`): `getDimensions`
is called twice (`:260` and `:262`), each allocating one `EntityDimensions` via `baseSize.scale(a,b)` (`:337-340`), plus
one `AABB` and the `Vec3` that `Entity.setPosRaw` allocates (E.javap:9621-9626) = **4 allocations per part per tick**. Synced parts go through `alignSynchedSubParts` (3.1: ≈10 per part).
For the robots the whole static alignment is then overwritten by the gait solver (`entity/gait/ModernSpiderGait.java:816-821`,
`feedParts :1380-1396`, `positionLegPart :1404-1439`: 1 `double[]` + `setPos` 4 allocs per leg), so a modern spider
pays 8 × (4 + 5) = 72 allocations per tick, 32 of them wasted on the stomped MHLib pass.

**Cheaper:** per tick — MHLib (4 vs 6 allocations per part; 0 vs 4 `Vec3` in the math; no per-call `getParts()`
array). Per frame — n/a for MoreHitboxes; MHLib's trust-client `applyInformation` per synced bone is counted in 3.6.
On the wire — n/a (placement itself sends nothing).

**Cost to port:** nothing worth porting from MoreHitboxes. Two MHLib-native improvements surfaced by the comparison
(not harvests, no attribution needed): (a) skip `alignSubParts` for solver-fed profiles — a `server-fed: true`
profile flag (`entity/hitbox/HitboxProfile.java:19-31`, ≈6 lines) checked at `IMultipartEntity.java:406` (≈2 lines),
saving 32 allocations + the trig per robot per tick; tests `HitboxPartTests.s4_parts_track_solver_legs` (`:137`),
`AntGaitTests.s5b_ant_flat_walk/hover` (`:298,:372`), the NBT resettle test (`:297` — the S4 comment at
`HitboxPartTests.java:535` documents the stomp re-stack the flag would remove; that assertion must be re-read),
`RideTests.s7_seat_geometry` (`:190`); (b) call `getDimensions` once in `MHLibPartEntity.setPos` (`:260-262`, 2 lines,
−1 allocation per part per tick, bit-identical). Chosen design: keep MHLib's allocation-free alignment.

### 3.3 Attack boxes

**MoreHitboxes:** a hitbox element with `is_attack_box` (`HitboxDataLoader.java:70-72`) is registered by bone ref
(`EntityHitboxDataInternal.java:49-50`); `activateAttackBoxes(level, duration)` marks all of them active until
`gameTime + duration` (`AttackBoxDataInternal.java:50-53`, bytecode 0-32); each active box is moved to its bone's
**world** position on the first render pass of each tick (`GeoEntityRendererMixin.java:44-49`, bytecode 245-336:
`GeoBone.getWorldPosition` — 1 `Vector4f` + 1 `Vector3d` + 1 `Vec3` = 3 allocations, 1 matrix transform per active
box); `ClientMobMixin` at `Mob.aiStep` RETURN, client only (`ClientMobMixin.java:21-28`), runs `clientTick`
(`AttackBoxDataInternal.java:56-72`): per active box 1 `EntityDimensions` + 1 `AABB` + `intersects(localPlayer)`
(`NeoForgeDistUtil.java:13-20`); a hit calls `attackBoxHit(player)` whose default returns `true` and clears all boxes
(`MultiPartEntity.java:82-84`). No packet, no damage, no server state (`AttackBoxData.java:14`: "For now this is
entirely client side"). F3+B draws active boxes red/blue and the attack bounds blue
(`EntityRenderDispatcherMixin.java:40-58`). A separate `attackBounds` AABB (`MultiPartEntity.makeAttackBoundingBox :41-50`)
is recomputed on every `setBoundingBox` (`EntityMixin.java:60-66`) as a "reach" helper for AI.

**MHLib:** none. The Queen's melee is server-side and distance-gated: a contact attack is picked inside 900 sq
blocks (`entity/TheQueen.java:1155-1181`), damage lands on the impact frame if the victim is inside 1600 sq blocks
(`:810-819` → `doHurtTarget :603-644`); no part geometry is consulted.

**Cheaper:** per frame — MoreHitboxes: 3 allocations + 1 transform per active box, once per game tick, only during
an attack window; MHLib: n/a (no feature). Per tick — MoreHitboxes: 2 allocations + 1 intersect per active box on
each client; MHLib: n/a. On the wire — equal (0; MoreHitboxes' boxes never leave the client). Against the Queen's
handshake MoreHitboxes' feature is cosmetic only (it cannot damage, and it tests the local player only).

**Cost to port:** the harvestable pieces are the data shape and the activation-window API, not the resolution.
Chosen design (most performant, server-authoritative): a profile list `attack-boxes: [{name, part, duration}]`
where `part` names an existing synced part (`the_queen.json`: bite → `LHead/LHead4/LHead12`, tail whips →
`Tail1/Tail4/Tail7`); `IMultipartEntity.activateAttackWindow(name, ticks)` on the server; resolution inside the
Queen's existing impact-frame branch (`TheQueen.java:813-817`): keep the 1600-sq outer gate and add
`part.getBoundingBox().intersects(victim.getBoundingBox())` over the named parts — **0 allocations, ≤3 AABB
intersections per resolved attack, 0 bytes**, because the server already holds the animated part boxes MHLib
streams. Pieces: `HitboxProfile`/new `AttackBoxConfig` record (≈40 lines), `IMultipartEntity` window state +
accessor fields in `MixinLivingEntity`/`IMHLibFieldAccessor` (≈40), `TheQueen` gate (≈15), a registered
`MixinEntityRenderDispatcher` port of MoreHitboxes' F3+B drawing of active boxes (≈50, attributed), gametest
(≈60). Tests affected: none existing; new Queen melee test. Risk: this changes the Queen's melee from a 40-block
sphere to geometry — misses become possible; present as a ruling, keep the sphere as the outer gate. Gait solver /
replaced-renderer seam: untouched. Alternative rejected: porting MoreHitboxes' client-only boxes verbatim
(no effect on server damage).

### 3.4 Culling bounds

**MoreHitboxes:** `EntityMixin.changeCullBox` replaces `Entity.getBoundingBoxForCulling()` with a cached AABB when
custom parts exist (`EntityMixin.java:52-58`, bytecode 0-50: 5 interface calls, 0 allocations per frame). The box
is `makeBoundingBoxForCulling(frustumWidthRadius, frustumHeight)` (`MultiPartEntity.java:60-66`: a square prism of
half-width `r·scale` and height `h·scale` around `position()`), where `r = max(|pos.x|+w/2, |pos.z|+w/2)` and
`h = pos.y + height` over the static elements (`HitboxData.java:23-30`, `EntityHitboxDataInternal.java:59-73`).
It is recomputed together with the attack bounds on every `Entity.setBoundingBox` (`EntityMixin.java:60-66`), i.e.
on every position write: 2 AABB allocations per moved tick, both sides. Parts themselves are never rendered
(NeoForge `PartEntity` is not in the client entity list) and get no frustum test; F3+B draws the cull box magenta
(`EntityRenderDispatcherMixin.java:38-39`). `shouldRenderAtSqrDistance` is untouched.

**MHLib:** no cull hook at all. OreSpawn compensates by never culling the Queen: `QueenRenderer.shouldRender`
returns `true` (`entity/client/QueenRenderer.java:110-113`, OPT-013 note `:104-109`) and
`TheQueen.shouldRenderAtSqrDistance` returns `true` (`TheQueen.java:426-428`). Vanilla would otherwise use
`getBoundingBox()` (`Entity.getBoundingBoxForCulling`, E.javap:8480-8484; `LivingEntity` only special-cases a dragon
head, LE.javap:9770-9803) — the 22×24 main box, which the wing and tail parts leave by up to ~45 blocks
(`the_queen.json:67-69,152-156`). Consequence today: an off-screen Queen still pays the full GeckoLib render of
110 bones / 130 cubes **and** the collector cost of 3.6 on every client that has her loaded. MHLib parts are
`isInvisible()` (`MHLibPartEntity.java:292-296`); the custom-part-renderer pass iterates `getParts()` per frame
(`client/EntityRenderEventHandlerCommonLogic.java:32-61`, 10 instanceof checks, no draw).

**Cheaper:** per frame — MoreHitboxes (cached box, 0 allocations; the vanilla frustum test then skips the whole
render when the box is out of view). Per tick — MHLib (0) vs MoreHitboxes (2 allocations per moved tick). On the
wire — equal (0).

**Cost to port:** chosen design — per-profile precomputed conservative radius
`R = max over parts of (|position| + |pivot| + √(w²/2 + h²))` (the box's far corner, height included) and height, computed once when the profile is
resolved (`HitboxProfile` derived field via `LazyLoadField`, `util/LazyLoadField.java`), scaled by
`mhlibGetEntitySizeInternally` (`IMultipartEntity.java:382-393`); cache the AABB once per tick in `tickParts`
(`:597-606`, both sides) and return it from a `getBoundingBoxForCulling()` override added to `MixinLivingEntity`
(merged override, calling `super` when no profile) — **0 allocations per frame, 1 AABB per tick**, no per-position
recompute (cheaper than MoreHitboxes per tick, equal per frame). Then `QueenRenderer.shouldRender` drops to `super`
(real frustum test) and `TheQueen.shouldRenderAtSqrDistance` can stay. Pieces: `HitboxProfile` (≈15 lines),
`IMultipartEntity` + accessor fields (≈25), `MixinLivingEntity` override (≈12), `QueenRenderer` (−4), a unit check
that R bounds every rest-pose part box (≈30). Tests: no gametest renders; `QueenPartPlacementProbe` untouched.
Risk: a culled master stops streaming; after 10 ticks the master rotates (`IMultipartEntity.java:471-487`); if every
tracker has her culled the server parts fall back to rest offsets (`:357-374`) while nobody looks — acceptable with a
conservative R (for the Queen R ≈ 78 blocks: Lwing1, the extreme part, |pos| 19.84 + |pivot| 24.40 + far corner 33.5),
but it is the OPT-013
behaviour change and needs the owner's look. Gait solver / replaced-renderer seam: untouched (robots keep vanilla
culling; the override only fires with a resolved profile).

### 3.5 Projectile and melee hit detection

**MoreHitboxes:** parts are always pickable (`NeoForgeMultiPart.java:46-49`, bytecode `iconst_1`). NeoForge's
`Level.getEntities(Entity, AABB, Predicate)` part loop is wrapped so a part passes only if its parent also passes the
predicate (`NeoForgeLevelMixin.java:29-41`, bytecode 0-80: 1 extra `Predicate.test` per part per query, plus the `Object[2]` that MixinExtras'
`WrapOperation` allocates per wrapped `Predicate.test`, bodies:912/929), and
`LevelMixin` removes parts whose parent is the querying entity or fails the predicate — allocating a `HashSet` on
**every** `getEntities(Entity, AABB, Predicate)` call game-wide (`LevelMixin.java:24-34`, bytecode 0-7). The
`EntityTypeTest` overload (used by `getEntitiesOfClass`, i.e. most AI target scans) is extended to add the parent of
any intersecting part — iterating **every part entity in the level** and `list.contains(parent)` per hit
(`NeoForgeLevelMixin.java:43-54`, bytecode 0-110): O(parts × results) per call. Projectiles: both
`ProjectileUtil.getEntityHitResult` overloads have their return value rewritten so the hit entity becomes the
parent and the part rides along in a field mixed into `EntityHitResult` (`ProjectileUtilMixin.java:17-25`,
`ProjectileUtilOverride.java:11-21`: 1 new `EntityHitResult` per part hit; `EntityHitResultMixin.java:13-28`). Melee:
`Minecraft.startAttack` re-targets the attack to the part carried by the crosshair hit result
(`MinecraftMixin.java:27-34`), `Player.attack` swaps the argument to the parent at HEAD and swaps the receiver of the
inner `Entity.hurt` back to the part (`PlayerMixin.java:24-42`, bytecode 0-32 / 0-29), so knockback, fire aspect,
sweep and `setLastHurtMob` land on the parent while damage enters through `NeoForgeMultiPart.hurt` →
`partHurt` (no damage modifier concept). Client picking of lazily added parts is handled by putting parts into
their own entity sections (`TransientEntitySectionManagerMixin.java:32-72`).

**MHLib:** a part is pickable when collidable or a damage surface (`MHLibPartEntity.java:347-362`, the S4 fix),
`canBeHitByProjectile` = `isAlive && isPickable` (E.javap:4867-4878) follows; the parent's own pickability is gated
by `main-hitbox.canReceiveDamage` (`IMultipartEntity.java:773-781` via `MixinLivingEntity.java:204-211`: the Queen's
22×24 box is unpickable, `the_queen.json:92-96`). The hit entity stays the part; damage enters
`MHLibPartEntity.hurt` (`:304-324`: can-receive-damage, invulnerability, enabled, `× damageModifier`) →
`IMultipartEntity.hurt` (`:31-56`: the S4 environmental rule) → `entity.hurt`. Parts identify with the parent through
`is()` (`:343-345`), forward interactions (`:364-384`), and can be colliders (`:298-301`; every Queen part is
`collidable: true`). Reach is vanilla's, against the server part box, which for the Queen is the master-fed animated
one. Lazily built client parts are registered into NeoForge's `ClientLevel.partEntities` map
(`client/MHLibClientPartRegistration.java:23-36`, `mixin/accessor/AccessorClientLevel.java:21-27`) — no section hack.
Per pick: 0 extra allocations; per `isPickable` on the parent: one cached profile fetch (`:776`).
Known MHLib weaknesses surfaced by the comparison: (a) a piercing arrow that hit part A of the Queen has only A's id
in its ignore set, so it can hit her other 9 parts (up to piercing+1 modifier-scaled hits from one arrow — vanilla's
dragon parts behave the same); (b) `Player.attack` applies knockback, fire aspect, sweep and crit side effects to the
part entity, not the parent (fire still reaches her: the burning part's own `baseTick` damage routes up because her
main box cannot receive damage, `IMultipartEntity.java:48-54`; knockback is lost because `Entity.push` only sets
`deltaMovement` (E.javap:4583-4596), which a `PartEntity` never integrates — the server placement rewrites the position
every tick, and `MHLibPartEntity.tick` (`:96-114`) moves parts only on the client interpolation path).

**Cheaper:** per pick/hit — MHLib (0 extra allocations vs MoreHitboxes' 1 `EntityHitResult` per part hit + 1 `HashSet`
per `getEntities(Entity,…)` call game-wide + O(parts × results) on the `EntityTypeTest` overload). Per tick — MHLib
(no per-query overhead; MoreHitboxes' costs scale with every collision/AI/explosion query in the level). On the wire —
equal (vanilla interact/attack packets, one entity id either way).

**Cost to port:** two harvests, one rejection.
(1) `Player.attack` part→parent unwrapping — port MoreHitboxes' design as `mixin/minecraft/MixinPlayer` (MixinExtras
is already on our classpath: `danger/orespawn/mixin/MountCameraMixin.java:12-13`): @ModifyVariable argsOnly HEAD +
@ModifyReceiver on `Entity.hurt`, ≈45 lines + a json entry, 2 instanceof per attack. Tests: add a `HitboxPartTests`
case (player.attack(leg part): damage modifier still applied through `MHLibPartEntity.hurt`, parent receives the
push); `s4_part_damage_routes_one_to_one` (`:264`) unchanged. Risk: none for the Queen — her `KNOCKBACK_RESISTANCE` is 1.0
(`TheQueen.java:302`), so the unwrapped push is absorbed; the robots would take a push through a leg exactly as a
single-box mob takes it; gait solver / renderer seam untouched.
(2) Piercing / ignore-list correctness — do **not** port the hit-result rewrite (it would bypass MHLib's per-part damage
modifiers for every projectile, because `AbstractArrow.onHitEntity` would call `hurt` on the parent). Chosen design,
MHLib-native and inspired by MoreHitboxes' "parent must also pass" rule: an accessor for `AbstractArrow`'s piercing
ignore set and ≈12 lines in `IMultipartEntity.hurt` that, when `source.getDirectEntity()` is an arrow with an ignore
set, add the parent id and every sibling part id. O(parts) per hit, 0 per tick, 0 bytes. Tests:
`s4_arrow_hits_leg_part` (`:438`) extended with a Piercing arrow expecting exactly one hit. Risk: a Queen behaviour
change (no multi-hit from one piercing arrow) — present as a ruling; robots unaffected in practice (body pickable).
(3) Rejected: `LevelMixin` / `NeoForgeLevelMixin` / `TransientEntitySectionManagerMixin` (per-call allocations and
O(parts × results) scans; MHLib's registry path already works — `s4_arrow_hits_leg_part` proves the modern spider's
lazily built parts are hit live). Optional, low value: the crosshair-parent presentation (`MinecraftMixin`) — OreSpawn
already unwraps parts in the HUD (`s4_part_parent_sweep_for_hud_unwrap`, `:402`).

### 3.6 Network sync

**MoreHitboxes:** one payload, server→client, during the configuration phase only: the whole hitbox table
(`SyncHitboxDataTask.java:19-22`, `SyncHitboxDataPayload.java:32-42`), encoded as a map of `ResourceLocation` →
list of `HitboxData` with fixed binary fields (`HitboxData.writeBuf :38-48`: `writeUtf` name + 3 doubles + 2 floats +
`writeUtf` ref + 2 booleans = 34 bytes + the two strings per box). Per tick: **0 packets, 0 bytes** — part positions are
never synced; each side computes its own (3.1). The client's reply is an empty map (`ClientPayloadHandler.java:20`).
Client bone capture (what feeds the client parts): `GeoEntityRendererMixin.getBonePositions` runs per bone per
render pass but returns at bytecode 36 unless this is the first pass of the game tick
(`GeckoLibMobMixin.java:20-23`, updated from `GeoRenderEvent.Entity.Post`, `GeckoLibEvents.java:8-12`); on that pass
each bone costs 1 hash lookup (`EntityHitboxDataInternal.getCustomPart :127-129`) and, for a part bone,
`getLocalPosition` (GeoBone javap:620-645: 1 `Vector4f` + 1 transform + 1 `Vector3d`) + 1 `Vec3` + 1
`AnimationOverride` = **4 allocations, 1 transform**; non-part bones cost 2 more hash lookups + 1 interface call and
no allocation (bytecode 113-286). For a 10-part Queen: 40 allocations and 10 transforms per game tick
(≈ 800 allocations/s at 20 tps), ≈ 330 hash lookups per collecting pass.

**MHLib:** six registered payloads (`init/MHLibNetwork.java:42-48`); the three that carry the bone and part stream:
C2S `CPacketBoneInformation` from the master only: entity id + an NBT-encoded `Map<String, BoneInformation>`
(`network/client/CPacketBoneInformation.java:45-53` via `ByteBufCodecs.fromCodec`, `util/BoneInformation.java:12-20`).
Derived NBT size per bone with name length n: compound key 3+n, `bone` 9+n, `hidden` 10, `position` 40, `scaling`
39, `rotation` 40, end 1 = **142 + 2n bytes** (103 + 2n when `scaling` equals the (1,1,1) default and DFU omits it —
Section 7). The Queen's 10 names total 59 characters → 1,148-1,538 bytes + 4 (id) + 2 (root) + ≈36 (payload id
string) ≈ **1.2-1.6 KB per packet before zlib**, sent every tick the payload changes (an animating boss: every tick)
and every 8 ticks otherwise (`IMultipartEntity.java:445,535-551`). Dirty tracking: exact field equality against the
last-sent map (`:569-595`).
S2C `SPacketUpdateMultipart` per tracking player: 4 + 4 + parts × 42 + 4 bytes (`network/server/SPacketUpdateMultipart.java:50-65,94-111`:
3 doubles, 4 floats, 2 booleans per part, plus dirty entity data) = **432 bytes for the Queen (+≈36 payload id)**,
sent on every `sendDirtyEntityData` pass while any part field changed, re-broadcast bit-identically for a ≥10-tick
linger, then silent (`mixin/entity/MixinServerEntity.java:98-120`, `MHLibPartEntity.mhlibDataUnchangedSince :213-225`);
the robots send exactly one pairing seed per tracker (`IMultipartEntity.java:172-186`, `MixinServerEntity.java:98-100`).
S2C `SPacketSetMaster` on election only: 4 + an NBT-wrapped UUID string (`network/server/SPacketSetMaster.java:37-43`).
Profiles reach clients through a synchable datapack registry (`init/MHLibDatapackLoaders.java:62-63`,
`api/DatapackRegistry.java:44-56`) — once per login, like MoreHitboxes' table.
Client bone capture (the layer that feeds the packet): per rendered frame, on **every** client that renders the
Queen, for **every** bone of the 110: HEAD push of 2 `Vector3d` + 1 `Tuple` (`api/IMHLibExtendedRenderLayer.java:61-73`),
then `onRenderBone` (`client/IBoneInformationCollectorLayerCommonLogic.java:72-74`) reads the world position **before**
checking whether the bone is synched — `getBoneWorldPosition` calls `bone.getWorldPosition()` three times
(`client/geckolib/renderlayer/GeckolibBoneInformationCollectorLayer.java:186`; each call GeoBone javap:681-706 =
1 `Vector4f` + 1 `Matrix4f.transform` + 1 `Vector3d`) + 1 `Vec3` = **7 allocations and 3 transforms per bone**, then a
linear `List<String>.contains` over the 10 names (`:76`). Per synched bone additionally: one `getScaleVector()` +
`getRotationVector()` pair per frame for the trust-client apply (`:90`), and a second pair on the collecting pass only
(`:79` sits inside the `:78` tick gate and builds the arguments before `tryAddBoneInformation` decides it is not the
master at `IMultipartEntity.java:670`) — each `foldBodyYaw`
(`GeckolibBoneInformationCollectorLayer.java:107-128`) allocates 4 `double[3][3]` (16 arrays, `:130-146`) and 3
products (12 arrays, `:148-156`) + 1 `Vec3` = 29 allocations and 81 multiply-adds; then `applyInformation`
(`MHLibPartEntity.java:423-438`: 3 pivot rotations + 1 scale + 1 subtract + `setPos` 3 = 8); the master also runs the
builder (5 `Optional.of` + 1 `BoneInformation` + `HashSet.add`, `CPacketBoneInformation.java:89-158`) — but only on
the first render pass of each game tick: `tryAddBoneInformation` is gated on `getCurrentTick() == entity.tickCount`
(`IBoneInformationCollectorLayerCommonLogic.java:78`) and `onPostRender` advances the stamp to `tickCount + 1`
(`:102-103`), so the builder already runs once per tick while every read and apply above it runs every frame.
Derived per Queen per client: a collecting frame 110 × (3 + 7) + 10 × (2 + 29 + 8) + 10 × 29 ≈ **1,780 allocations**,
any other frame 110 × 10 + 10 × (2 + 29 + 8) ≈ **1,490**, with 330 matrix-vector transforms and 40-60 3×3 products per
frame — at 60 fps ≈ 95,000 allocations/s and 19,800 transforms/s per rendered Queen per client, plus ≈80 per tick on
the master for the builder; the bridge hook (OPT-028) adds another 330 push/pop allocations per frame on top. Every
non-multipart GeckoLib entity drawn by a `GeoEntityRenderer` pays the HEAD/TAIL push/pop per bone per frame too —
3 allocations, 6 with the bridge hook (`isBoneCollectionActive` is `true` on the GeoEntity path,
`GeckolibBoneInformationCollectorLayer.java:49-58`; only the `onRenderBone` body early-outs, `:63`).

**Cheaper:** per frame — MoreHitboxes by two orders of magnitude (≈13 vs ≈1,490-1,780 allocations per Queen frame;
10 vs 330 transforms per collecting pass). Per tick (server) — MoreHitboxes (no diff, no encode, no sync map). On the
wire — MoreHitboxes (0 B/tick vs ≈1.2-1.6 KB/tick C2S from the master + 468 B/tick per tracker S2C while moving).

**Cost to port:** (1) extending MHLib's existing once-per-tick builder gate to the matrix reads and the trust-client
applies (MoreHitboxes gates its whole bone pass that way), plus the descriptor-exact selector, are the top harvest
(Section 4, items 1 and 6): `mhlib$renderTick` in `MixinLivingEntity`/`IMHLibFieldAccessor` (≈15 lines), set from
`GeckolibEntityRenderEventHandler.onPostRenderEntity` (`:24-32`, ≈4), `isBoneCollectionActive` evaluated once per frame
in `onPreRender` and cached (≈15), non-synched bones skipped before any matrix read using a precomputed
`Set<String>` on the profile (≈12), one `getWorldSpaceMatrix()` read of the translation column (`m30/m31/m32`;
bit-identical to `transform(0,0,0,1)` for finite values) instead of three `getWorldPosition()` calls (≈8), one
allocation-free scalar `foldBodyYaw` computed once per synched bone and reused for both consumers (≈60), an
allocation-free depth-indexed `double[]` start/end stack (≈40). Derived result: a collecting frame ≈ 110 allocations,
0 transforms (translation reads only); non-collecting frames ≈ 0; ≈ 2,200 allocations/s per Queen per client instead
of ≈95,000. Tests: `QueenPartPlacementProbe` lifecycle check (`src/g1tool/java/danger/orespawn/g1/QueenPartPlacementProbe.java:587-671`)
pins seed identity and static invariants and must be rewritten for the ring stack; its fold self-test (`:345-385`)
keeps the `foldBodyYaw(double,double,double,double)` signature; robots' suites untouched (no collection);
`QueenPlayNicelyDimsTests` untouched (server fallback path). Behaviour change — yes, and a bug fix (BUG-044): today's
once-per-tick builder gate is a per-renderer stamp (`GeckolibBoneInformationCollectorLayer.java:35`, one layer per
renderer instance, advanced only on equality at `IBoneInformationCollectorLayerCommonLogic.java:102-103`). A client
frame during which the entity ticked twice leaves the stamp one behind for good (the gate and the advance both fail
and `tickCount` only grows), and with two Queens drawn by the same renderer the stamp follows whichever entity last
matched and starves the other; a wedged master's builder then ships an empty map (a size change is a send,
`IMultipartEntity.java:544-546`) and `processBoneInformation` (`:138`) clears the server map, leaving rest-pose server
hurtboxes for the session. The per-entity stamp (MoreHitboxes' `renderTick < tickCount`, `GeckoLibMobMixin.java:20-23`)
changes what the server receives in exactly those cases. MHLib change: two refuters by the proof rule, gametests that
pin the hitch case and the two-Queen case, the owner's look, and the Section 5 before/after.
(2) Compact binary C2S codec (Section 4 item 5): per bone 1 byte index into `profile.synchedBones()` order + 1 byte
hidden + 3 doubles + 3 doubles scale + 3 doubles rotation = 74 bytes (50 with float scale/rotation, but that breaks the
bit-identity law since the collector produces doubles) → 745 bytes for the Queen (10 × 74 + 4 entity id + 1 count) vs 1.2-1.6 KB, no NBT tree, no
string keys; ≈80 lines in `CPacketBoneInformation` (+ index→name resolution in the handler, ≈40). No existing test
covers the bytes; add a round-trip unit test. Risk: Queen only; robots never stream bones.

---

## 4. Harvest list

Ranked by benefit ÷ cost. "Design" is the one chosen for MHLib; MIT attribution applies where MoreHitboxes code
or its specific mechanism is reproduced (Section 6).

| # | What | Why better | Design chosen | Port cost | Risk: Queen melee handshake / robots' gait solver / replaced-renderer seam |
|---|---|---|---|---|---|
| 1 | **Once-per-game-tick bone collection** (`GeckoLibMobMixin` render-tick gate + `GeckoLibEvents` post-render stamp) | Performance: MHLib already builds the packet once per tick, but reads every bone's world matrix three times, folds and applies on every frame; gating those to the collecting pass (1 of ~3 frames at 60 fps) with the MHLib-native trims (skip non-synched bones before matrix reads, 1 matrix read, 1 fold, allocation-free stack) gives ≈ 50× fewer allocations per second per rendered Queen (Section 3.6); the per-entity stamp also fixes BUG-044 | render-tick stamp on the entity, `isBoneCollectionActive` cached per frame, matrix translation read, scalar fold, depth-indexed stack | ≈180 lines across `GeckolibBoneInformationCollectorLayer`, `IBoneInformationCollectorLayerCommonLogic`, `IMHLibExtendedRenderLayer`, `MixinLivingEntity`/`IMHLibFieldAccessor`, `HitboxProfile`; probe lifecycle rewrite ≈60 | Handshake: none (server-side, distance-gated). Gait: none (no collection for robots). Seam: `MixinGeoReplacedEntityRenderer` shares the layer — the gate must key on `grer.getCurrentEntity()` (`:51-53`). Behaviour change: it fixes BUG-044 (the per-renderer stamp that wedges on a two-tick frame and starves all but one Queen, 3.6) → two refuters, gametests for the hitch and two-Queen cases, owner look, Section 5 before/after |
| 2 | **`Player.attack` part→parent unwrapping** (`PlayerMixin`) | Correctness: knockback, fire aspect, sweep, crit and `setLastHurtMob` reach the parent; damage still enters through the part and its modifier | verbatim mechanism: @ModifyVariable argsOnly + @ModifyReceiver on `Entity.hurt` | ≈45 lines + json entry + 1 gametest | Handshake: none. Gait: none. Seam: none. Behaviour: parts become knockback conduits; the Queen absorbs it (`KNOCKBACK_RESISTANCE` 1.0, `TheQueen.java:302`), the robots take it like any single-box mob |
| 3 | **Conservative cull bounds** (`EntityMixin.changeCullBox` + `makeBoundingBoxForCulling`) | Performance: lets the vanilla frustum test skip an off-screen Queen's whole render and collector (today never culled, `QueenRenderer.java:110-113`) | per-profile radius computed once, AABB cached per tick, `getBoundingBoxForCulling` override on LivingEntity | ≈85 lines + `QueenRenderer` −4 | Handshake: none. Gait: none (profile-gated). Seam: none. Behaviour: OPT-013 re-ruling; culled masters rotate after 10 ticks; fallback offsets while unseen |
| 4 | **Piercing / ignore-list correctness** (inspired by `NeoForgeLevelMixin`'s parent rule; not its code) | Correctness: one piercing arrow can no longer hit up to piercing+1 parts of one Queen | arrow-side: add parent + sibling ids to the arrow's ignore set on a part hit (accessor + 12 lines) | ≈30 lines + extend `s4_arrow_hits_leg_part` | Handshake: none. Gait: none. Seam: none. Behaviour: fewer multi-hits on the Queen → ruling |
| 5 | **Fixed-layout binary bone payload** (shape of `HitboxData.writeBuf`) | Wire: ≈745 B vs 1.2-1.6 KB per bone packet, no NBT encode/decode | index-keyed bones, doubles kept (bit-identity) | ≈120 lines + codec round-trip test | Handshake: none. Gait: none (robots never stream bones). Seam: none |
| 6 | **Descriptor-exact `renderRecursively` selectors** (`GeoEntityRendererMixin.java:23`) | Performance/robustness: stops hooking the synthetic bridge (OPT-028: every bone enters through it, so the push/pop runs twice per bone today); pins the exact GeckoLib signature we depend on | replace the bare names in the three geckolib mixins with full descriptors | 6 lines | None; halves the per-bone push/pop for every GeckoLib entity |
| 7 | **Attack-box data shape + activation window + F3+B drawing** (`HitboxData.isAttackBox`, `activateAttackBoxes`, `EntityRenderDispatcherMixin.java:42-58`) | Feature the Queen lacks: geometric melee; MoreHitboxes' resolution is client-only and cannot damage, so only the shape and the debug draw are worth taking | server-side window resolved against MHLib's synced part AABBs inside the impact-frame branch | ≈200 lines incl. gametest | Handshake: **changes it** (sphere → geometry) → ruling, keep the sphere as outer gate. Gait: none. Seam: none |
| 8 | **`defaultRequire = 1`** (`jar/morehitboxes.mixins.json`, `injectors.defaultRequire`) | Robustness: MHLib's config sets no `defaultRequire`, so every MHLib injection carries Mixin's default 0 and no-ops silently if a target moves; MoreHitboxes fails fast for everything but its optional GeckoLib hook | `"injectors": {"defaultRequire": 1}` in `multihitboxlib.mixins.json`, the three GeckoLib hooks included (GeckoLib is a hard dependency of the port) | 1 line + a load check | None at runtime; a moved target fails at load instead of silently dropping MHLib |

**Not worth porting (with the reason):** server-side placement (`MultiPart.updatePosition`: 6 allocations per part
per tick vs MHLib's 3, and `getParts()` allocating per call — 3.2); the trust model (there is none to take — 3.1);
the profile sync (`SyncHitboxDataTask` is equal to MHLib's synchable datapack registry — 3.6); the hit-result
parent rewrite (`ProjectileUtilOverride`) — drops per-part damage modifiers on projectiles (3.5); `LevelMixin` /
`NeoForgeLevelMixin` / `TransientEntitySectionManagerMixin` — per-call `HashSet` allocations and O(parts × results)
scans on hot vanilla queries (3.5); anchors (`AnchorDataInternal`, a bone-attached rider position with 1-tick
extrapolation, `:59-77`) — the robots' seats are gait-derived on the server (`RideTests.s7_seat_geometry`) and the
Queen has no rider; `fixPosOnRefresh` (`EntityMixin.java:31-41`) — no OreSpawn multipart entity changes pose.

**What MoreHitboxes does worse or lacks (so nothing regresses if any harvest lands):** no server-side animated
hurtboxes and no way to get them (3.1); no per-part damage modifier (`SubPartConfig.damageModifier`,
`MHLibPartEntity.java:320`) — the Queen's 1.0/0.5/0.25 scheme has no home; no collidable parts (`noPhysics`, never a
collider — the Queen's parts are colliders, `the_queen.json:100`); no main-hitbox damage gating (`mhLibIsPickable`);
no pivots or part rotation (`applyInformation :424-427`); no per-entity size callback (`IMHLibSizeCallback`, the
PlayNicely 0.25); `Mob`-only API (`MultiPartEntity<T extends Mob & …>`); parts positioned by `getLocalPosition` with the same first-frame tracking gap
BUG-042 recorded (`GeoBone.getLocalSpaceMatrix` arms tracking on the read, javap:571-578); server parts static, so
reach validation is against rest geometry; `getParts()` allocation per call; per-query allocations in `Level.getEntities`;
attack boxes client-only; one alpha release line, single maintainer (`morehitboxes_evaluation_v1_superseded.md:100-104`).

---

## 5. Item 13 baseline fold (spawn-100 benchmark, MHLib side)

The G1 smoke harness cannot see any of this: its "candidate" renderer is a named `GeoRenderer` implementation
(`src/g1tool/java/danger/orespawn/g1/G1PerformanceBenchmark.java:793-824`) run outside the mod loader, so no MHLib mixin
or layer is present ("MHLib parts: 0" in every scene, `phase_g_reports/g1_proof/benchmark/README.md`). The protocol
already lists "MHLib packets per second" and "synced part count" as live-only metrics
(`tools/g1_performance_benchmark.json`, `live_only_metrics`, `deferred_live_scenes[1]`). Fold the following in so
Section 3's derived counts get measured numbers.

**Counters to add** (a `de.dertoaster.multihitboxlib.util.MHLibCounters` of `LongAdder`s, compiled in, active only
under `-Dmhlib.counters=true`, dumped to the log every 100 ticks per side, and readable by the gametest harness):

| Counter | Instrument at | Derived expectation per Queen |
|---|---|---|
| `client.bones_visited` | `IBoneInformationCollectorLayerCommonLogic.onRenderBone` entry (`:61`) | 110 per rendered frame |
| `client.recursive_start` / `recursive_end` | `IMHLibExtendedRenderLayer.onRenderRecursivelyStart/End` (`:61,:75`) | 220 per frame today (the bridge hook, OPT-028); 110 after harvest 6 |
| `client.world_pos_reads` | `GeckolibBoneInformationCollectorLayer.getBoneWorldPosition` (`:185-188`) | 330 per frame today |
| `client.folds` | `foldBodyYaw` (`:107`) | 10 per frame (the trust-client apply) + 10 on the collecting pass today |
| `client.bone_infos_built` / `apply_information` | `tryAddBoneInformation` after `:693`; `MHLibPartEntity.applyInformation` (`:423`) | 10 per tick on the master (the builder is gated to the first render pass of each tick); 10 per frame on every client (the trust-client apply) |
| `client.collector_ns` / `collector_alloc_bytes` | wrap `GeoRenderEvent.Entity.Pre`→`Post` in `GeckolibEntityRenderEventHandler` (`:24-39`) with `System.nanoTime()` and `ThreadMXBean.getCurrentThreadAllocatedBytes()` | the per-frame capture cost in ms and bytes |
| `net.c2s_bone_packets` / `c2s_bone_bytes` | `CPacketBoneInformation.send` (`:55-57`) with the encoded length (wrap `STREAM_CODEC.encode`) | ≈20/s while animating, ≈1.2-1.6 KB each |
| `net.s2c_update_packets` / `s2c_update_bytes` | `MixinServerEntity.mixinSendDirtyEntityData` both send sites (`:111-112,:119`) | ≈20/s × trackers while moving, 468 B each; robots: 1 per tracker |
| `net.set_master_packets` | `IMultipartEntity.setMasterUUID` (`:112-116`) | elections only |
| `server.align_sub_parts_parts` / `align_synched_parts` / `part_setpos` | `alignSubParts` loop (`:287`), `alignSynchedSubParts` loop (`:335`), `MHLibPartEntity.setPos` (`:255`) | Queen 0 / 10 / 10 per tick; spider 8 / 0 / 16 (8 stomped + 8 solver) |
| `server.placement_ns` | wrap `mhlibAiStep` (`:395-436`) and `ModernSpiderGait.feedParts` (`:1380`) | server part placement per tick |

**Isolation scenes** (same machine controls as the protocol's `live_acceptance_protocol`): (A) 100 Queens in view,
hostile idle; (B) 100 Queens loaded but off-screen (culling control — today they still render and collect);
(C) 100 modern SpiderRobots and (D) 100 modern AntRobots (server-fed parts, no collection, stream gated: the
`server.*` counters and the one-seed S2C behaviour); (E) 100 MHLib-free GeckoLib mobs (a Beaver candidate) to
measure the HEAD/TAIL push/pop tax on non-multipart GeckoLib entities; (F) 100 classic vanilla-renderer mobs as the
zero line. Record per scene: whole-client frame median/p95 and 1 % low, allocation rate (GC MXBean), server MSPT
median/p95, the counters above per second, and packets/bytes per direction per second. Headless companion: extend
`QueenPartPlacementProbe.runLifecycle`/`walkLayer` (`:587-671`), which already drives the real collector over the
baked rig, with the same `nanoTime`/allocated-bytes probe over 1,000 walks — it isolates the collector from GeckoLib's
own render and is where harvest 1's before/after is proven bit-for-bit. Regression threshold for the gate: proposed,
not adopted — a harvest must not raise `collector_ns` or `c2s_bone_bytes` per Queen, and must leave scene E's
allocation rate at or below the classic renderer's.

---

## 6. Attribution mechanics

MIT (`jar/LICENSE_morehitboxes`: "Copyright (c) 2024 DarkPred") requires that "the above copyright notice and this
permission notice shall be included in all copies or substantial portions of the Software"; derived work may be
relicensed but the notice must travel with the copied portions. Proposal:

1. `src/main/resources/META-INF/LICENSE-MoreHitboxes.txt` — DarkPred's full MIT text verbatim, so it ships inside
   the jar; a copy beside the vendored sources at `src/main/java/de/dertoaster/multihitboxlib/LICENSE-MoreHitboxes.txt`
   for readers of the tree.
2. Per-file header on every ported file (e.g. the future `mixin/minecraft/MixinPlayer.java`, the render-tick fields in
   `MixinLivingEntity`, the attack-box debug mixin): `// Portions derived from MoreHitboxes by DarkPred
   (https://github.com/DarkPred/MoreHitboxes, commit 88899b3), MIT License — see LICENSE-MoreHitboxes.txt`.
   Designs merely inspired by MoreHitboxes (harvests 3, 4, 5, the scalar fold) get a one-line "design after
   MoreHitboxes' <class>" comment, no license obligation.
3. Third-party listing: the repository root has **no** `LICENSE*`, `NOTICE*`, `THIRD_PARTY*` or `COPYING*` file
   (checked); `README.md:81-98` carries a "License & Ownership" section and `mods.toml` declares
   `license="${mod_license}"` = `All Rights Reserved` (`gradle.properties:24`). Add a "Third-party notices" subsection
   to `README.md` (or a new `THIRD_PARTY_NOTICES.md`) listing MoreHitboxes (MIT, DarkPred, portions), MultiHitboxLib
   (vendored, see the flag below), Databuddy (jar-in-jar'd, `build.gradle:391-398` — its license is not recorded in the
   repo and should be added to the same list), GeckoLib (dependency, not shipped).

Existing MHLib licensing flag, restated in one sentence (`FIX_LOG.md:4841`, the BUG-043 upstream check; the
superseded report itself carries no licensing line): upstream MultiHitboxLib's LICENSE file is LGPL-3.0 while its
gradle declared `mod_license=All Rights Reserved` and its README forbade jar-in-jar, forks and ports, and the upstream
repository has since been deleted — so the vendored copy ships under OreSpawn's "All Rights Reserved" field with no MHLib
license text, an unresolved question for the owner that the MIT harvest does not change.

---

## 7. What could not be verified

- Vanilla/NeoForge 1.21.1 bytecode for `Player.attack`, `ProjectileUtil.getEntityHitResult` (both overloads),
  `Level.getEntities` (and the order of NeoForge's part loop relative to `LevelMixin`'s injection point),
  `Minecraft.startAttack`, `ServerEntity.sendDirtyEntityData/addPairing`, `TransientEntitySectionManager.addEntity`,
  `Player.isCloseEnough`, the `CompressionDecoder`/`FriendlyByteBuf`/`ClientboundCustomPayloadPacket` constants, the
  server reach check in `ServerGamePacketListenerImpl`, and `AbstractArrow`'s piercing ignore-set field: the Minecraft
  and NeoForge jars live in the Gradle cache (off-limits). The scratchpad holds dumps of `Entity`, `LivingEntity`,
  `Mob` (`my_Mob.txt:1270-1273`: `invokespecial LivingEntity.aiStep` at offset 1, so the Mob RETURN ordering in
  Section 2 is verified), `EntityRenderDispatcher` (`mc1211/EntityRenderDispatcher.javap.txt`: the `renderLineBox`
  ordinals 0 and 1 in Section 2 are verified), `EntityRenderer`, `LivingEntityRenderer` and `MobRenderer`. Target
  existence for the rest is taken from the compiled mixins' constant pools and from MoreHitboxes' successful build,
  not from the targets themselves.
- Verified after the first draft, no longer open: MHLib's bare-name selectors hook the synthetic bridge and every bone
  enters through it (OPT-028); the `recursive_start` counter should read 220 per Queen frame today.
- Whether NeoForge's own `Player.attack` patch already unwraps `PartEntity` targets (no `Player` dump).
- DFU's `optionalFieldOf(name, default)` omitting default-equal values (affects the NBT estimate by 39 B per bone);
  the UUID string codec's exact NBT size; zlib ratios — all wire numbers are pre-compression.
- MoreHitboxes' wiki pages for attack boxes, anchors, hitbox format and datapacks returned 404 (`wiki/*.md`);
  behaviour was taken from sources and bytecode only. `GeoRenderLayer` itself has no dump; the per-bone dispatch was
  proven from `GeoRenderer.applyRenderLayersForBone` (GR.javap:303-328) instead.
- All per-frame, per-tick and byte figures are derived counts from source and bytecode; none is measured — that is
  what Section 5 exists for.
