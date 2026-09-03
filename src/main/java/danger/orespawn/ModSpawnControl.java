package danger.orespawn;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.FinalizeSpawnEvent;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.function.Supplier;

/**
 * Prevents disabled OreSpawn mobs from spawning based on per-mob config toggles.
 *
 * Each configurable mob has a corresponding boolean "enable" flag in
 * {@link OreSpawnConfig}. When that flag is {@code false}, or when the global
 * {@link OreSpawnConfig#ALL_MOBS_DISABLE ALL_MOBS_DISABLE} flag is {@code true},
 * natural world spawns of that mob are canceled.
 *
 * Spawn eggs, {@code /summon} commands, spawner blocks, and entities loaded from
 * disk are never affected, so server operators and players always retain full
 * manual control over entity placement.
 *
 * <p><b>How it works:</b> A {@link FinalizeSpawnEvent} handler tags entities whose
 * spawn type is {@link MobSpawnType#NATURAL} or {@link MobSpawnType#CHUNK_GENERATION}.
 * The subsequent {@link EntityJoinLevelEvent} handler checks that tag together with
 * the config map, canceling the join if the mob is disabled.</p>
 */
@EventBusSubscriber(modid = OreSpawnMod.MOD_ID)
public class ModSpawnControl {

    /**
     * Entities flagged as natural spawns by {@link #onFinalizeSpawn}. Consumed
     * (removed) in {@link #onEntityJoinLevel}. Weak keys prevent memory leaks
     * if an entity is discarded before it ever joins a level.
     *
     * <p>Synchronized: {@link FinalizeSpawnEvent} fires on parallel chunk-generation
     * worker threads while {@link EntityJoinLevelEvent} runs on the server thread; an
     * unsynchronized WeakHashMap rehash under concurrent mutation can corrupt the map
     * or spin forever (BUG-009).</p>
     */
    private static final Set<Entity> NATURAL_SPAWNS =
            Collections.synchronizedSet(Collections.newSetFromMap(new WeakHashMap<>()));

    /**
     * Lazily-initialized mapping from each configurable entity type to a supplier
     * that reads its "enabled" config value. Built on first access so that all
     * {@link net.neoforged.neoforge.registries.DeferredHolder DeferredHolder}
     * references are resolved.
     */
    private static volatile Map<EntityType<?>, Supplier<Boolean>> spawnControls;

    private static Map<EntityType<?>, Supplier<Boolean>> getSpawnControls() {
        if (spawnControls == null) {
            spawnControls = Map.ofEntries(
                    Map.entry(ModEntities.ENTITY_MOSQUITO.get(),     OreSpawnConfig.MOSQUITO_ENABLE::get),
                    Map.entry(ModEntities.GHOST.get(),               OreSpawnConfig.GHOST_ENABLE::get),
                    Map.entry(ModEntities.GHOST_SKELLY.get(),        OreSpawnConfig.GHOST_SKELLY_ENABLE::get),
                    Map.entry(ModEntities.SPIDER_DRIVER.get(),       OreSpawnConfig.SPIDER_DRIVER_ENABLE::get),
                    Map.entry(ModEntities.MOTHRA.get(),              OreSpawnConfig.MOTHRA_ENABLE::get),
                    Map.entry(ModEntities.ENTITY_BRUTALFLY.get(),    OreSpawnConfig.BRUTALFLY_ENABLE::get),
                    Map.entry(ModEntities.NASTYSAURUS.get(),         OreSpawnConfig.NASTYSAURUS_ENABLE::get),
                    Map.entry(ModEntities.POINTYSAURUS.get(),        OreSpawnConfig.POINTYSAURUS_ENABLE::get),
                    Map.entry(ModEntities.ENTITY_CRICKET.get(),      OreSpawnConfig.CRICKET_ENABLE::get),
                    Map.entry(ModEntities.FROG.get(),                OreSpawnConfig.FROG_ENABLE::get),
                    Map.entry(ModEntities.ENTITY_ANT.get(),          OreSpawnConfig.BLACK_ANT_ENABLE::get),
                    Map.entry(ModEntities.ENTITY_RED_ANT.get(),      OreSpawnConfig.RED_ANT_ENABLE::get),
                    Map.entry(ModEntities.ENTITY_TERMITE.get(),      OreSpawnConfig.TERMITE_ENABLE::get),
                    Map.entry(ModEntities.ENTITY_UNSTABLE_ANT.get(), OreSpawnConfig.UNSTABLE_ANT_ENABLE::get),
                    Map.entry(ModEntities.ENTITY_RAINBOW_ANT.get(),  OreSpawnConfig.RAINBOW_ANT_ENABLE::get),
                    Map.entry(ModEntities.ALOSAURUS.get(),           OreSpawnConfig.ALOSAURUS_ENABLE::get),
                    Map.entry(ModEntities.HAMMERHEAD.get(),           OreSpawnConfig.HAMMERHEAD_ENABLE::get),
                    Map.entry(ModEntities.ENTITY_LEON.get(),         OreSpawnConfig.LEON_ENABLE::get),
                    // TF-030: both registry ids are the one consolidated
                    // Leon/Leonopteryx, so both obey the single leonEnable flag
                    // (orig has one "Leonopteryx" entry, OreSpawnMain.java:6523).
                    Map.entry(ModEntities.LEONOPTERYX.get(),         OreSpawnConfig.LEON_ENABLE::get),
                    Map.entry(ModEntities.ENTITY_CATER_KILLER.get(), OreSpawnConfig.CATERKILLER_ENABLE::get),
                    Map.entry(ModEntities.ENTITY_MOLENOID.get(),     OreSpawnConfig.MOLENOID_ENABLE::get),
                    Map.entry(ModEntities.TREX.get(),                OreSpawnConfig.TREX_ENABLE::get),
                    Map.entry(ModEntities.CRYOLOPHOSAURUS.get(),     OreSpawnConfig.CRYOLOPHOSAURUS_ENABLE::get),
                    Map.entry(ModEntities.URCHIN.get(),              OreSpawnConfig.URCHIN_ENABLE::get),
                    Map.entry(ModEntities.CAMARASAURUS.get(),        OreSpawnConfig.CAMARASAURUS_ENABLE::get),
                    Map.entry(ModEntities.CHIPMUNK.get(),            OreSpawnConfig.CHIPMUNK_ENABLE::get),
                    Map.entry(ModEntities.OSTRICH.get(),             OreSpawnConfig.OSTRICH_ENABLE::get),
                    Map.entry(ModEntities.GAZELLE.get(),             OreSpawnConfig.GAZELLE_ENABLE::get),
                    Map.entry(ModEntities.VELOCITY_RAPTOR.get(),     OreSpawnConfig.VELOCITY_RAPTOR_ENABLE::get),
                    Map.entry(ModEntities.ENTITY_HYDROLISC.get(),    OreSpawnConfig.HYDROLISC_ENABLE::get),
                    Map.entry(ModEntities.ENTITY_SPYRO.get(),        OreSpawnConfig.SPYRO_ENABLE::get),
                    Map.entry(ModEntities.BARYONYX.get(),            OreSpawnConfig.BARYONYX_ENABLE::get),
                    Map.entry(ModEntities.COCKATEIL.get(),           OreSpawnConfig.COCKATEIL_ENABLE::get),
                    Map.entry(ModEntities.CASSOWARY.get(),           OreSpawnConfig.CASSOWARY_ENABLE::get),
                    Map.entry(ModEntities.EASTER_BUNNY.get(),        OreSpawnConfig.EASTER_BUNNY_ENABLE::get),
                    Map.entry(ModEntities.PEACOCK.get(),             OreSpawnConfig.PEACOCK_ENABLE::get),
                    Map.entry(ModEntities.ENTITY_KYUUBI.get(),       OreSpawnConfig.KYUUBI_ENABLE::get),
                    Map.entry(ModEntities.CEPHADROME.get(),          OreSpawnConfig.CEPHADROME_ENABLE::get),
                    Map.entry(ModEntities.DRAGON.get(),              OreSpawnConfig.DRAGON_ENABLE::get),
                    Map.entry(ModEntities.ENTITY_GAMMA_METROID.get(),OreSpawnConfig.GAMMA_METROID_ENABLE::get),
                    Map.entry(ModEntities.BASILISK.get(),            OreSpawnConfig.BASILISK_ENABLE::get),
                    Map.entry(ModEntities.ENTITY_DRAGONFLY.get(),    OreSpawnConfig.DRAGONFLY_ENABLE::get),
                    Map.entry(ModEntities.JEFFERY.get(),             OreSpawnConfig.JEFFERY_ENABLE::get),
                    // Remaining flags from the original's ~100-entry table
                    // (orig OreSpawnMain.java:6364-6465), wired for ANIM-018.
                    Map.entry(ModEntities.ROCK_BASE.get(),           OreSpawnConfig.ROCK_ENABLE::get),
                    Map.entry(ModEntities.BAND_P.get(),              OreSpawnConfig.CRIMINAL_ENABLE::get),
                    Map.entry(ModEntities.ENTITY_RAT.get(),          OreSpawnConfig.RAT_ENABLE::get),
                    Map.entry(ModEntities.ENTITY_EMPEROR_SCORPION.get(), OreSpawnConfig.EMPEROR_SCORPION_ENABLE::get),
                    Map.entry(ModEntities.ENTITY_TROOPER_BUG.get(),  OreSpawnConfig.TROOPER_BUG_ENABLE::get),
                    Map.entry(ModEntities.ENTITY_SPIT_BUG.get(),     OreSpawnConfig.SPIT_BUG_ENABLE::get),
                    Map.entry(ModEntities.ENTITY_STINK_BUG.get(),    OreSpawnConfig.STINK_BUG_ENABLE::get),
                    Map.entry(ModEntities.ENTITY_SCORPION.get(),     OreSpawnConfig.SCORPION_ENABLE::get),
                    Map.entry(ModEntities.CAVE_FISHER.get(),         OreSpawnConfig.CAVE_FISHER_ENABLE::get),
                    Map.entry(ModEntities.ALIEN.get(),               OreSpawnConfig.ALIEN_ENABLE::get),
                    Map.entry(ModEntities.WATER_DRAGON.get(),        OreSpawnConfig.WATER_DRAGON_ENABLE::get),
                    Map.entry(ModEntities.SEA_MONSTER.get(),         OreSpawnConfig.SEA_MONSTER_ENABLE::get),
                    Map.entry(ModEntities.SEA_VIPER.get(),           OreSpawnConfig.SEA_VIPER_ENABLE::get),
                    Map.entry(ModEntities.ATTACK_SQUID.get(),        OreSpawnConfig.ATTACK_SQUID_ENABLE::get),
                    Map.entry(ModEntities.ROBOT_1.get(),             OreSpawnConfig.ROBOT1_ENABLE::get),
                    Map.entry(ModEntities.ROBOT_2.get(),             OreSpawnConfig.ROBOT2_ENABLE::get),
                    Map.entry(ModEntities.ROBOT_3.get(),             OreSpawnConfig.ROBOT3_ENABLE::get),
                    Map.entry(ModEntities.ROBOT_4.get(),             OreSpawnConfig.ROBOT4_ENABLE::get),
                    Map.entry(ModEntities.ROBOT_5.get(),             OreSpawnConfig.ROBOT5_ENABLE::get),
                    Map.entry(ModEntities.ENTITY_ROTATOR.get(),      OreSpawnConfig.ROTATOR_ENABLE::get),
                    Map.entry(ModEntities.ENTITY_VORTEX.get(),       OreSpawnConfig.VORTEX_ENABLE::get),
                    Map.entry(ModEntities.DUNGEON_BEAST.get(),       OreSpawnConfig.DUNGEON_BEAST_ENABLE::get),
                    Map.entry(ModEntities.KRAKEN.get(),              OreSpawnConfig.KRAKEN_ENABLE::get),
                    Map.entry(ModEntities.LIZARD.get(),              OreSpawnConfig.LIZARD_ENABLE::get),
                    Map.entry(ModEntities.ENTITY_RUBBER_DUCKY.get(), OreSpawnConfig.RUBBER_DUCKY_ENABLE::get),
                    Map.entry(ModEntities.GIRLFRIEND.get(),          OreSpawnConfig.GIRLFRIEND_ENABLE::get),
                    Map.entry(ModEntities.BOYFRIEND.get(),           OreSpawnConfig.BOYFRIEND_ENABLE::get),
                    Map.entry(ModEntities.FIREFLY.get(),             OreSpawnConfig.FIREFLY_ENABLE::get),
                    Map.entry(ModEntities.FAIRY.get(),               OreSpawnConfig.FAIRY_ENABLE::get),
                    Map.entry(ModEntities.ENTITY_BEE.get(),          OreSpawnConfig.BEE_ENABLE::get),
                    Map.entry(ModEntities.THE_KING.get(),            OreSpawnConfig.THE_KING_ENABLE::get),
                    Map.entry(ModEntities.THE_QUEEN.get(),           OreSpawnConfig.THE_QUEEN_ENABLE::get),
                    Map.entry(ModEntities.ENTITY_MANTIS.get(),       OreSpawnConfig.MANTIS_ENABLE::get),
                    Map.entry(ModEntities.ENTITY_STINKY.get(),       OreSpawnConfig.STINKY_ENABLE::get),
                    Map.entry(ModEntities.ENTITY_HERCULES_BEETLE.get(), OreSpawnConfig.HERCULES_BEETLE_ENABLE::get),
                    // orig OreSpawnMain.java:4609-4624 — CowEnable gates all
                    // custom cow variants' natural spawns.
                    Map.entry(ModEntities.RED_COW.get(),             OreSpawnConfig.COW_ENABLE::get),
                    Map.entry(ModEntities.GOLD_COW.get(),            OreSpawnConfig.COW_ENABLE::get),
                    // Phase E ruling (2026-08-11): AppleCow / GoldenAppleCow are
                    // wiki-only (no 1.7.10 source class) — natural spawns
                    // additionally require phase14ContentEnable, read as its
                    // EFFECTIVE value ([modern] enabled AND the key, via
                    // OreSpawnConfig.phase14ContentEnable(); master-override
                    // ruling 2026-09-04). EnchantedAppleCow
                    // is the original EnchantedCow (orig OreSpawnMain.java:3599,
                    // display name "Enchanted Golden Apple Cow" :2765) and stays
                    // on the plain CowEnable gate.
                    Map.entry(ModEntities.APPLE_COW.get(),
                            () -> OreSpawnConfig.phase14ContentEnable() && OreSpawnConfig.COW_ENABLE.get()),
                    Map.entry(ModEntities.GOLDEN_APPLE_COW.get(),
                            () -> OreSpawnConfig.phase14ContentEnable() && OreSpawnConfig.COW_ENABLE.get()),
                    Map.entry(ModEntities.ENCHANTED_APPLE_COW.get(), OreSpawnConfig.COW_ENABLE::get),
                    // Wiki-only hostile (same ruling); currently has no natural
                    // spawn data, the entry is a guard in case one is added.
                    Map.entry(ModEntities.VAMPIRE_BUTTERFLY.get(),
                            OreSpawnConfig::phase14ContentEnable),
                    Map.entry(ModEntities.CRYSTAL_COW.get(),         OreSpawnConfig.COW_ENABLE::get),
                    Map.entry(ModEntities.ENTITY_BUTTERFLY.get(),    OreSpawnConfig.BUTTERFLY_ENABLE::get),
                    Map.entry(ModEntities.ENTITY_LUNA_MOTH.get(),    OreSpawnConfig.MOTH_ENABLE::get),
                    Map.entry(ModEntities.ENTITY_TSHIRT.get(),       OreSpawnConfig.TSHIRT_ENABLE::get),
                    Map.entry(ModEntities.COIN.get(),                OreSpawnConfig.COIN_ENABLE::get),
                    Map.entry(ModEntities.CREEPING_HORROR.get(),     OreSpawnConfig.CREEPING_HORROR_ENABLE::get),
                    Map.entry(ModEntities.ENTITY_TERRIBLE_TERROR.get(), OreSpawnConfig.TERRIBLE_TERROR_ENABLE::get),
                    Map.entry(ModEntities.ENTITY_CLIFF_RACER.get(),  OreSpawnConfig.CLIFF_RACER_ENABLE::get),
                    Map.entry(ModEntities.ENTITY_TRIFFID.get(),      OreSpawnConfig.TRIFFID_ENABLE::get),
                    // orig OreSpawnMain.java:4630-4634 — WormEnable gates the
                    // large worm only (medium/small spawn by splitting).
                    Map.entry(ModEntities.ENTITY_WORM_LARGE.get(),   OreSpawnConfig.WORM_ENABLE::get),
                    Map.entry(ModEntities.CLOUD_SHARK.get(),         OreSpawnConfig.CLOUD_SHARK_ENABLE::get),
                    Map.entry(ModEntities.GOLD_FISH.get(),           OreSpawnConfig.GOLD_FISH_ENABLE::get),
                    Map.entry(ModEntities.ENTITY_LEAF_MONSTER.get(), OreSpawnConfig.LEAF_MONSTER_ENABLE::get),
                    Map.entry(ModEntities.ENDER_KNIGHT.get(),        OreSpawnConfig.ENDER_KNIGHT_ENABLE::get),
                    Map.entry(ModEntities.ENDER_REAPER.get(),        OreSpawnConfig.ENDER_REAPER_ENABLE::get),
                    Map.entry(ModEntities.BEAVER.get(),              OreSpawnConfig.BEAVER_ENABLE::get),
                    Map.entry(ModEntities.IRUKANDJI.get(),           OreSpawnConfig.IRUKANDJI_ENABLE::get),
                    Map.entry(ModEntities.SKATE.get(),               OreSpawnConfig.SKATE_ENABLE::get),
                    Map.entry(ModEntities.WHALE.get(),               OreSpawnConfig.WHALE_ENABLE::get),
                    Map.entry(ModEntities.FLOUNDER.get(),            OreSpawnConfig.FLOUNDER_ENABLE::get),
                    Map.entry(ModEntities.PITCH_BLACK.get(),         OreSpawnConfig.PITCH_BLACK_ENABLE::get),
                    Map.entry(ModEntities.ENTITY_LURKING_TERROR.get(), OreSpawnConfig.LURKING_TERROR_ENABLE::get),
                    Map.entry(ModEntities.GODZILLA.get(),            OreSpawnConfig.GODZILLA_ENABLE::get),
                    Map.entry(ModEntities.CRAB.get(),                OreSpawnConfig.CRAB_ENABLE::get)
            );
        }
        return spawnControls;
    }

    /**
     * Tags entities whose spawn type is {@link MobSpawnType#NATURAL} or
     * {@link MobSpawnType#CHUNK_GENERATION}. This fires before
     * {@link EntityJoinLevelEvent}, giving us a reliable way to distinguish
     * natural spawns from spawn eggs, commands, and spawner blocks.
     */
    @SubscribeEvent
    public static void onFinalizeSpawn(FinalizeSpawnEvent event) {
        MobSpawnType spawnType = event.getSpawnType();
        if (spawnType == MobSpawnType.NATURAL || spawnType == MobSpawnType.CHUNK_GENERATION) {
            NATURAL_SPAWNS.add(event.getEntity());
        }
    }

    /**
     * Cancels the level-join of any disabled OreSpawn mob that was flagged as a
     * natural spawn. Entities loaded from disk, placed by spawn eggs, or summoned
     * via commands pass through unconditionally.
     */
    @SubscribeEvent
    public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (event.loadedFromDisk()) return;

        Entity entity = event.getEntity();
        if (!NATURAL_SPAWNS.remove(entity)) return;

        Supplier<Boolean> enabledSupplier = getSpawnControls().get(entity.getType());
        if (enabledSupplier != null) {
            if (OreSpawnConfig.ALL_MOBS_DISABLE.get() || !enabledSupplier.get()) {
                event.setCanceled(true);
            }
        }
    }
}
