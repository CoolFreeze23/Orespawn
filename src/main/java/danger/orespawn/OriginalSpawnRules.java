package danger.orespawn;

import java.util.Set;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.MobSpawnEvent;

/**
 * WGEN-086: the port's natural spawning reads the original's rules alone.
 *
 * <p>1.7.10's spawner (SpawnerAnimals.findChunksForSpawning) asked a drawn entity's getCanSpawnHere and nothing else.
 * An OreSpawn class that overrode it without returning its parent's dropped EntityLiving's own test (no entity in the
 * way, no liquid in the box) with the rest, which is how the original's water-listed species (the Whale, the Sea
 * Viper, the Frog, the Crab) spawned in water. 1.21's spawner asks the entity's checkSpawnRules and then
 * checkSpawnObstruction, whose liquid test refuses every water spawn of a class that is not a WaterAnimal; for those
 * species the transcription in checkSpawnRules is the whole answer here. The species whose original rule ran through
 * its parent's to EntityLiving's ({@link #INHERITED}) keep the game's checks.
 *
 * <p>1.7.10's chunk-generation spawner (SpawnerAnimals.performWorldGenSpawning) asked no rule at all, only a solid block
 * below (the placement, still asked): an OreSpawn species spawned with the chunk passes both checks here, as the
 * original's did. Spawner blocks keep the game's checks.
 */
@EventBusSubscriber(modid = OreSpawnMod.MOD_ID)
public final class OriginalSpawnRules {
    /**
     * The species whose original rule ran through its parent's: the cows and the Hydrolisc (EntityAnimal's, no override),
     * the Girlfriend and the Boyfriend (their overrides returned EntityAnimal's), the Spider Driver (EntitySpider's), and
     * the optional wiki cows.
     */
    private static final Set<String> INHERITED = Set.of("red_cow", "gold_cow", "enchanted_apple_cow", "crystal_cow",
            "hydrolisc", "girlfriend", "boyfriend", "spider_driver", "apple_cow", "golden_apple_cow");

    private OriginalSpawnRules() {
    }

    @SubscribeEvent
    public static void onPlacementCheck(MobSpawnEvent.SpawnPlacementCheck event) {
        if (event.getSpawnType() == MobSpawnType.CHUNK_GENERATION && isOreSpawn(event.getEntityType())) {
            event.setResult(MobSpawnEvent.SpawnPlacementCheck.Result.SUCCEED);
        }
    }

    @SubscribeEvent
    public static void onPositionCheck(MobSpawnEvent.PositionCheck event) {
        Mob mob = event.getEntity();
        if (event.getSpawner() != null || !isOreSpawn(mob.getType())) return;
        if (event.getSpawnType() == MobSpawnType.CHUNK_GENERATION) {
            event.setResult(MobSpawnEvent.PositionCheck.Result.SUCCEED);
        } else if (event.getSpawnType() == MobSpawnType.NATURAL && ownRule(mob.getType())) {
            event.setResult(mob.checkSpawnRules(event.getLevel(), MobSpawnType.NATURAL)
                    ? MobSpawnEvent.PositionCheck.Result.SUCCEED : MobSpawnEvent.PositionCheck.Result.FAIL);
        }
    }

    /** Whether the species' own transcribed rule is its whole natural-spawn rule (an OreSpawn species, not inherited). */
    public static boolean ownRule(EntityType<?> type) {
        return isOreSpawn(type) && !INHERITED.contains(BuiltInRegistries.ENTITY_TYPE.getKey(type).getPath());
    }

    private static boolean isOreSpawn(EntityType<?> type) {
        return OreSpawnMod.MOD_ID.equals(BuiltInRegistries.ENTITY_TYPE.getKey(type).getNamespace());
    }
}
