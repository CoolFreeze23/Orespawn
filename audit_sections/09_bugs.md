# Phase 3 — Bug & Glitch Hunt (Port Code Only)

Scope: `src/main/java/danger/orespawn/**` + vendored `src/main/java/de/dertoaster/multihitboxlib/**`.
Sampled in depth: TheKing, TheQueen, Godzilla, Kraken, Mothra, GiantRobot, SpiderRobot, EntityWormLarge, EntityWormMedium, EntityWormSmall (chain), KingHead, QueenHead, OreSpawnPartEntity, ThePrince, ThePrinceTeen, ThePrinceAdult, ThePrincess, Dragon, EntityRat, Fairy, Girlfriend, Boyfriend, AttackSquid, EntityVortex, EntityAnt, EntityButterfly, EntityStinky, EntitySpyro, Alosaurus-family AI goals (Bug/Dinosaur/Basilisk/SeaViper/Scorpion/SpitBug/TrooperBug goals), CaveFisherAmbushGoal, PointysaurusStareGoal, plus handlers (KrakenRevengeHandler, MobzillaSpawnTracker, ModLavaDropHandler, ModSpawnControl), worldgen (OreSpawnChunkGenerator, CrystalStructures), block entities (CrystalFurnaceBlockEntity), blocks (RTPBlock, UtopiaPortalBlock), network (RiderInputPayload), loot (AddItemsLootModifier), client (OreSpawnClient, GirlfriendOverlay), registries (OreSpawnMod, ModEntityAttributes, OreSpawnConfig), and the MHLib mixins/event handlers.

---

## CRITICAL

### C1. MHLib `EntityEventHandler` registered on the MOD bus with GAME-bus events — startup crash
- **File:** `src/main/java/de/dertoaster/multihitboxlib/EntityEventHandler.java:15` (handlers at 19, 35, 44)
- **Scenario:** `@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD)` but the class subscribes to `EntityEvent.Size`, `PlayerEvent.StartTracking`, and `PlayerEvent.StopTracking`, which are GAME-bus events (not `IModBusEvent`). NeoForge throws `IllegalArgumentException` while registering listeners during mod construction → the game crashes on launch (or, if classloading is skipped, every MHLib hitbox-resize and start/stop-tracking hook is silently dead, breaking TheQueen's bone-tracked hitboxes and master-UUID sync for late-joining players).
- **Fix:** Remove `bus = EventBusSubscriber.Bus.MOD` so the class registers on the GAME bus.

### C2. MHLib `GameEventHandler` registered on the MOD bus with a GAME-bus event — startup crash
- **File:** `src/main/java/de/dertoaster/multihitboxlib/GameEventHandler.java:9` (handler at 13)
- **Scenario:** Same mismatch as C1 for `PlayerEvent.PlayerLoggedInEvent`. Crash at startup, or asset-synch enforcement never fires on player login.
- **Fix:** Remove `bus = EventBusSubscriber.Bus.MOD` from the annotation.

### C3. `EntityRat` — `UUID.fromString("")` ticking-entity crash for spawner/summoned rats
- **File:** `src/main/java/danger/orespawn/entity/EntityRat.java:139` (root cause at 110–116)
- **Scenario:** `readAdditionalSaveData` does `this.myOwner = tag.getString("MyOwner")`; for any NBT lacking that key (mob spawner `SpawnData`, `/summon orespawn:rat`, structure-embedded entities) `getString` returns `""`, which is neither `null` nor `"null"`. Next time the rat has no target, `customServerAiStep` runs `UUID.fromString(this.myOwner)` → `IllegalArgumentException: Invalid UUID string:` → "Ticking entity" server crash. The Crystal-dimension dungeons place rat spawners, so this fires in normal play. Bonus: `removeWhenFarAway` then also returns false, so such rats never despawn.
- **Fix:** Treat empty/invalid strings as no owner (`if (s.isEmpty()) myOwner = null;` and/or wrap `UUID.fromString` in try/catch like `Fairy.java:191-195` does).

### C4. Prince growth chain calls `tame(null)` when the owner is offline — NPE crash
- **Files:** `src/main/java/danger/orespawn/entity/ThePrince.java:241`, `ThePrinceTeen.java:207` and `:246`, `ThePrinceAdult.java:249`
- **Scenario:** `teen.tame(this.level().getPlayerByUUID(this.getOwnerUUID()))` — `getPlayerByUUID` returns null if the owner is not online. `TamableAnimal.tame(Player)` dereferences the player (`player.getUUID()`) → NPE → server crash. `ThePrince.customServerAiStep` (line 230) triggers `transformToTeen()` automatically once kill/fed/day counters pass thresholds, so a prince left in a chunk-loaded base transforms (and crashes the server) the moment its owner logs out.
- **Fix:** Null-check the resolved player and fall back to `teen.setOwnerUUID(this.getOwnerUUID()); teen.setTame(true, true);` instead of `tame(player)`.

### C5. `TheQueen.doHurtTarget` can `discard()` a ServerPlayer — player deleted without dying
- **File:** `src/main/java/danger/orespawn/entity/TheQueen.java:486–502`
- **Scenario:** When the health-tracked victim's health reaches 0, the Queen calls `healthTrackedEntity.discard()`. If the victim is a player, the player entity is removed without the death pipeline: no death screen, no drops, no respawn prompt — the client is left in a frozen/ghost state and the server has a player connection with no entity.
- **Fix:** Never `discard()` players; use `entity.die(damageSource)` / plain `hurt` overkill, and restrict the discard path to non-player mobs.

### C6. `Godzilla.doJumpDamage` uses `genericKill` — kills Creative/Spectator players
- **File:** `src/main/java/danger/orespawn/entity/Godzilla.java:422–441`
- **Scenario:** The landing shockwave applies `damageSources().genericKill()`, a damage type that bypasses invulnerability (it is the `/kill` source). A creative-mode or spectator player near Godzilla's jump landing is killed outright; spectators can be killed through walls.
- **Fix:** Use `damageSources().mobAttack(this)` and let vanilla invulnerability rules apply.

### C7. `SpiderRobot` save/load overridden as no-ops without `super` — living-entity data silently lost
- **File:** `src/main/java/danger/orespawn/entity/SpiderRobot.java:199–202`
- **Scenario:** `addAdditionalSaveData(CompoundTag) {}` / `readAdditionalSaveData(CompoundTag) {}` drop the `super` call, so all `LivingEntity`/`Mob` NBT (Health, ActiveEffects, attribute modifiers, PersistenceRequired, CanPickUpLoot, hand/armor items, leash) is never written or read. A half-killed SpiderRobot reloads at full health; a name-tagged/persistence-required one loses the flag and can despawn.
- **Fix:** Call `super.addAdditionalSaveData(tag)` / `super.readAdditionalSaveData(tag)` in both overrides.

---

## HIGH

### H1. `EntityWormLarge` respawns its 40-worm brood on every world reload
- **File:** `src/main/java/danger/orespawn/entity/EntityWormLarge.java:30` (spawn loop at 133–145)
- **Scenario:** `wormsSpawned` is not written in NBT. Each time the chunk reloads with the large worm alive, the flag is 0 again and `aiStep` spawns another 40 medium/small worms. A few relogs near a worm nest produces hundreds of entities → severe TPS loss and an effective entity bomb.
- **Fix:** Persist `wormsSpawned` in `addAdditionalSaveData`/`readAdditionalSaveData`.

### H2. `ModSpawnControl.NATURAL_SPAWNS` — non-thread-safe WeakHashMap-backed set touched from worker threads
- **File:** `src/main/java/danger/orespawn/ModSpawnControl.java:42`
- **Scenario:** `Collections.newSetFromMap(new WeakHashMap<>())` is mutated from `FinalizeSpawnEvent`, which fires on chunk-generation worker threads for `MobSpawnType.CHUNK_GENERATION` spawns, while the server thread reads/writes it in `EntityJoinLevelEvent`. Concurrent rehash of the WeakHashMap can corrupt it (infinite loop in `getEntry`) or throw `ConcurrentModificationException`.
- **Fix:** Wrap with `Collections.synchronizedSet(...)` or key off entity UUIDs in a `ConcurrentHashMap`.

### H3. `ThePrince`/`ThePrincess` — permanent `noPhysics` after being hurt (falls through the world)
- **Files:** `src/main/java/danger/orespawn/entity/ThePrince.java:144` (set at 179, 211), `ThePrincess.java:114` (set at 130, 153)
- **Scenario:** `tick()` sets `this.noPhysics = (activity == 2)`, and `hurt()` sets activity to 2. Nothing ever resets activity to a non-phasing state except the owner's sit-toggle interaction. A wild/untamed or unattended prince that takes one hit becomes permanently non-collidable and sinks through the terrain into the void. Activity is even persisted (`SpyroActivity`), so the bug survives relogs.
- **Fix:** Reset activity (e.g. to 1) when the attack/target ends (mirror the `setAttacking(0)` path), or stop mapping activity 2 to `noPhysics`.

### H4. `Kraken` directly sets a caught player's position/motion every tick — rubber-banding / movement kicks
- **File:** `src/main/java/danger/orespawn/entity/Kraken.java:285–301`
- **Scenario:** While `caught` is a player, the server forces `caught.setPos(...)`/`setDeltaMovement(...)` each tick. Player movement is client-authoritative, so the client keeps sending its own positions; the server's forced positions are not pushed through `connection.teleport`, producing violent rubber-banding and "moved too quickly"/"moved wrongly" kicks on stricter servers.
- **Fix:** For `ServerPlayer`, use `connection.teleport(...)` (or make the player a passenger of the tentacle part) instead of raw `setPos`.

### H5. `TheKing.hurt` silently deletes small attackers — removes other mods' and players' pets
- **File:** `src/main/java/danger/orespawn/entity/TheKing.java:951–953`
- **Scenario:** Any non-player `Monster` (bbWidth/height < 3.0) that damages TheKing is `discard()`ed — no death event, no drops, no `LivingDeathEvent`. A player's tamed mob or another mod's boss minion that lands a hit is wiped from the world with no feedback.
- **Fix:** Replace `discard()` with lethal damage (`hurt(genericKill)` on non-players) or restrict it to OreSpawn's own minion classes.

### H6. Worldgen cooldowns are static, cross-dimension, and cross-thread
- **Files:** `src/main/java/danger/orespawn/world/OreSpawnChunkGenerator.java:107`, `src/main/java/danger/orespawn/world/CrystalStructures.java:68`
- **Scenario:** `recentlyPlaced` is a static `AtomicInteger` shared by every dimension instance of the generator and by all parallel worldgen threads. Structure cooldowns in the Mining dimension suppress dungeons in Utopia/Crystal generated concurrently; results are also non-deterministic per seed (check-then-act race between the gate read and `updateAndGet`). No crash, but structure distribution is wrong and unreproducible.
- **Fix:** Make `recentlyPlaced` an instance field (per-generator) or key the cooldown by dimension, and accept the per-region randomness or derive it from chunk-seeded random.

---

## MEDIUM

### M1. `TheKing` — combat/AI state not persisted
- **File:** `src/main/java/danger/orespawn/entity/TheKing.java` (fields ~`lightningStreamCount`, `iceStreamCount`, `ticker`, `backoffTimer`, `largeEntityDetected`, `attackDamage`, `revengeTarget`, `headEntityFound`)
- **Scenario:** None of these are in `addAdditionalSaveData`/`readAdditionalSaveData`. Relogging mid-fight resets attack streams, backoff and rage state — the boss "forgets" the fight and its buffed attack damage.
- **Fix:** Serialize the combat fields that matter across relog (`attackDamage`, stream counters, `backoffTimer`).

### M2. `TheKing.dropCustomDeathLoot` — up to 300 random-registry item drops
- **File:** `src/main/java/danger/orespawn/entity/TheKing.java:1321–1339`
- **Scenario:** Two `while (j < 150)` loops draw random IDs from the item/block registries, only incrementing on non-air results. One kill dumps ~300 item entities (anything registered, from other mods too) → lag spike, duplicate-exploit-grade loot, and potential "creative-only/technical item" leakage.
- **Fix:** Cap drops to a small curated loot table (or vastly reduce counts) instead of raw registry sampling.

### M3. `Godzilla` — combat state not persisted
- **File:** `src/main/java/danger/orespawn/entity/Godzilla.java` (fields `ticker`, `streamCount`, `largeUnknownDetected`, `jumped`, `jumpTimer`, `headFound`)
- **Scenario:** Same as M1 — fire-stream and jump state reset on relog; a mid-air "jumped" Godzilla reloads with `jumped=false` and never runs its landing-damage path, leaving stale state.
- **Fix:** Persist `jumped`/`jumpTimer`/`streamCount` in NBT.

### M4. `TheQueen.mood` not persisted — angry queen reloads happy
- **File:** `src/main/java/danger/orespawn/entity/TheQueen.java:179` (mood logic ~707–720)
- **Scenario:** `mood` isn't saved. Relogging during a fight resets the Queen to her placid mood; with `QUEEN_ALWAYS_MAD` off, players can defuse her aggression by relogging.
- **Fix:** Write/read `mood` in the existing NBT methods.

### M5. `Kraken` — weather lock fights the vanilla weather cycle and isn't persisted
- **File:** `src/main/java/danger/orespawn/entity/Kraken.java:57` (decrement) and `:135` (`setWeatherParameters`)
- **Scenario:** Every time `weatherSet` hits 0 the server forces a thunderstorm again, overriding `/weather clear` and any other mod's weather; the timer isn't saved so a relog re-triggers the storm immediately. Multiple Krakens each re-arm it independently.
- **Fix:** Only set weather once per Kraken (flag in NBT) or check `level.isThundering()` before forcing.

### M6. `EntityVortex` — server-side `push`/launch velocities on players are not synced
- **File:** `src/main/java/danger/orespawn/entity/EntityVortex.java:184–187` (pull), `:244–273` (`skywardLaunch`)
- **Scenario:** Player movement is client-authoritative; `push()`/`setDeltaMovement()` on a `ServerPlayer` without `hurtMarked = true` (the comment's `hasImpulse` does not trigger a motion packet) is mostly invisible to the client. The signature tornado pull/launch works erratically — only when piggy-backing the knockback packet from a coincident `doHurtTarget`.
- **Fix:** Set `victim.hurtMarked = true` (players) after modifying `deltaMovement`.

### M7. `Dragon` ridden flight is moved server-side while the client owns vehicle movement
- **File:** `src/main/java/danger/orespawn/entity/Dragon.java:343–349` (server `move(SELF)`), `:355–360` (`travel` early-return)
- **Scenario:** With a controlling player passenger, vanilla expects the riding client to move the vehicle (`ServerboundMoveVehiclePacket`). The port instead returns early from `travel()` and moves the dragon in server `aiStep`, so the server and the riding client fight over position → visible jitter/rubber-banding while flying, worsening with latency.
- **Fix:** Implement rider movement in `travel()`/`tickRidden` (client-predicted) like vanilla horses, keeping server `move(SELF)` only for the riderless AI path.

### M8. Crystal structures truncated at chunk borders (silently)
- **Files:** `src/main/java/danger/orespawn/world/CrystalStructures.java` (large features, e.g. FairyCastleTree), swallow-catch at `OreSpawnChunkGenerator.java:264`
- **Scenario:** Feature-style generation writes blocks outside the 3×3 writable region during `applyBiomeDecoration`; the generator catches and ignores the resulting exceptions, so big trees/castles generate with sheared-off edges depending on chunk-generation order.
- **Fix:** Convert oversized pieces to Jigsaw/Structure pieces with proper bounding boxes, or clamp placement to the writable region.

### M9. `EntityVortex` scans all nearby LivingEntities every tick on both sides
- **File:** `src/main/java/danger/orespawn/entity/EntityVortex.java:101` (in `tick()`), repeated at `:176` (in `customServerAiStep`)
- **Scenario:** `findSomethingToAttack()` does a 32×20×32 `getEntitiesOfClass` + line-of-sight raycast per candidate, every tick, on server *and* client (client run is just for smoke particles). Several vortexes in a storm measurably hit frame and tick time.
- **Fix:** Cache the target for ~10 ticks and gate the client particle check on a cheaper distance test.

---

## LOW

### L1. `Mothra` — movement/heal state not persisted
- **File:** `src/main/java/danger/orespawn/entity/Mothra.java:41–45`
- **Scenario:** `lastX/Y/Z`, `stuckCount`, `healthTicker` reset on relog; stuck-detection and regen restart. Minor behavioral hiccup only.
- **Fix:** Persist `healthTicker` if regen cadence matters; otherwise accept.

### L2. `GiantRobot.reloadTicker` not persisted
- **File:** `src/main/java/danger/orespawn/entity/GiantRobot.java:38`
- **Scenario:** Relog during the rocket reload window lets the robot fire immediately. Cosmetic/balance only.
- **Fix:** Save the ticker in NBT.

### L3. `Kraken.enchantToolSilk` rolls Silk Touch I–V (illegal levels)
- **File:** `src/main/java/danger/orespawn/entity/Kraken.java:501`
- **Scenario:** Drops can carry Silk Touch > max level 1; anvils/grindstones and enchant-validation mods treat the stack as illegal.
- **Fix:** Clamp the level to `enchantment.getMaxLevel()`.

### L4. `Kraken` — `hitByPlayer`/`callReinforcements` not persisted
- **File:** `src/main/java/danger/orespawn/entity/Kraken.java` (fields near top)
- **Scenario:** Relogging mid-fight re-arms the reinforcement wave — a second squad of helpers can spawn.
- **Fix:** Persist both flags.

### L5. `TheQueen.myCanSee` truncates negative coordinates toward zero
- **File:** `src/main/java/danger/orespawn/entity/TheQueen.java:1189–1230`
- **Scenario:** `(int)(startx + dx)` rounds toward zero, so in negative-coordinate quadrants the LoS ray samples the wrong block column — Queen occasionally sees through (or fails to see past) corners.
- **Fix:** Use `Mth.floor`/`BlockPos.containing` for the sample positions.

### L6. `RTPBlock` spawns particles with `Level.addParticle` on the server — never visible
- **File:** `src/main/java/danger/orespawn/block/RTPBlock.java:77–81`
- **Scenario:** `entityInside` returns early on the client, then calls `level.addParticle` on the server `Level`, which is a no-op. The teleport burst effect never shows (sound works).
- **Fix:** Use `((ServerLevel) level).sendParticles(...)`.

### L7. `CrystalFurnaceBlockEntity` consumes bucket fuels without returning the container
- **File:** `src/main/java/danger/orespawn/gui/CrystalFurnaceBlockEntity.java:182–189`
- **Scenario:** Fueling with a lava bucket destroys the bucket (`fuel.shrink(1)`), unlike the vanilla furnace which leaves an empty bucket.
- **Fix:** After shrinking, place `fuel.getCraftingRemainingItem()` into the fuel slot if empty.

### L8. `EntityWormMedium` — `upcount`/`downcount` not persisted
- **File:** `src/main/java/danger/orespawn/entity/EntityWormMedium.java:23–24`
- **Scenario:** Burrow/emerge cycle resets on relog; purely cosmetic.
- **Fix:** Persist or accept.

### L9. `EntityVortex.tick` heals client-side
- **File:** `src/main/java/danger/orespawn/entity/EntityVortex.java:117–119`
- **Scenario:** `heal(1.0f)` runs on both sides with independent RNG — the client's local health copy briefly diverges from the synced value (visual only, next health sync corrects it).
- **Fix:** Gate the heal behind `!level().isClientSide`.

---

## Verified-OK notes (no action)

- `IMultipartEntity.setMasterUUID` (`de/dertoaster/multihitboxlib/api/IMultipartEntity.java:73-75`): the upstream MHLib null-UUID packet-encode crash is already patched in this vendored copy.
- `MobzillaSpawnTracker`: correct `SavedData` usage, Overworld-scoped, `setDirty()` called — no leak.
- `KrakenRevengeHandler`: stateless event handler, no unbounded collections.
- `OreSpawnPartEntity` lifecycle: parts are non-saving (`shouldBeSaved()==false`), rebuilt by parent, removed with the parent — no orphan-part leak found.
- `MixinLivingEntity.setId` chain with `TheKing`/`Godzilla` `setId` overrides: chained correctly; part IDs consistent on both sides.
- `ModEntityAttributes` (`ModEntityAttributes.java:21`): correctly on MOD bus for `EntityAttributeCreationEvent`/`RegisterSpawnPlacementsEvent`.
- `RiderInputPayload`, `AddItemsLootModifier`, `GirlfriendOverlay`, `OreSpawnClient` registrations: correct sides/buses.
- Fish food items (`ItemLavaEel` etc.) and bows: use-duration/finish/release handling present.
- `RenderSpiderRobotInfo` is a plain data class despite living in `entity.client` — instantiating it from common `SpiderRobot` code (line 47) does not crash a dedicated server, though moving it out of the client package would be cleaner.

## Counts

| Severity | Count |
|----------|-------|
| CRITICAL | 7 |
| HIGH     | 6 |
| MEDIUM   | 9 |
| LOW      | 9 |
| **Total**| **31** |
