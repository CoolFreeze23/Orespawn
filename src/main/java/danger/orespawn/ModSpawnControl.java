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
     */
    private static final Set<Entity> NATURAL_SPAWNS =
            Collections.newSetFromMap(new WeakHashMap<>());

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
                    Map.entry(ModEntities.JEFFERY.get(),              OreSpawnConfig.JEFFERY_ENABLE::get)
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
