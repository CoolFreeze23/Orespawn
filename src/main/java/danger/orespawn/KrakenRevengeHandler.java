package danger.orespawn;

import danger.orespawn.entity.AttackSquid;
import danger.orespawn.entity.Kraken;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;

/**
 * 1.7.10 fidelity: in {@code AttackSquid.func_70097_a}, when an AttackSquid was
 * killed by a player, OreSpawn rolled {@code nextInt(15)==0}. On a hit it
 * spawned 1–3 Krakens at {@code y=170} within ±4 XZ of the squid — a "revenge"
 * mechanic that punished griefers who farmed the surface squids. The roll was
 * skipped inside {@code DimensionID5} (the Crystal dimension) and gated on
 * {@code KrakenEnable} + {@code wasshot==0} (Krakens already mid-hop).
 *
 * <p>We replicate that hook here on the GAME bus so the death-side logic stays
 * in one place rather than being smeared across {@link AttackSquid#hurt} and
 * its custom death path.</p>
 */
@EventBusSubscriber(modid = OreSpawnMod.MOD_ID)
public class KrakenRevengeHandler {

    private static final ResourceKey<Level> CRYSTAL_DIMENSION =
            ResourceKey.create(net.minecraft.core.registries.Registries.DIMENSION,
                    ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "crystal"));

    private static final int REVENGE_DICE = 15;
    private static final double SPAWN_Y = 170.0;
    private static final int SPAWN_XZ_JITTER = 4;

    @SubscribeEvent
    public static void onSquidDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof AttackSquid squid)) return;
        if (squid.level().isClientSide) return;
        if (!(squid.level() instanceof ServerLevel level)) return;

        // Player kills only — environmental drowning / dryout is not "revenge worthy".
        if (!(event.getSource().getEntity() instanceof Player)) return;

        // Crystal dimension exempt: 1.7.10 explicitly skipped DimensionID5 to
        // keep the dimension-locked AttackSquid encounter from devolving into
        // a Kraken farm.
        if (level.dimension().equals(CRYSTAL_DIMENSION)) return;

        if (OreSpawnConfig.ALL_MOBS_DISABLE.get()) return;
        if (level.random.nextInt(REVENGE_DICE) != 0) return;

        int spawnCount = 1 + level.random.nextInt(3);
        for (int i = 0; i < spawnCount; i++) {
            Kraken kraken = ModEntities.KRAKEN.get().create(level);
            if (kraken == null) continue;
            double offsetX = squid.getX() + level.random.nextInt(SPAWN_XZ_JITTER * 2 + 1) - SPAWN_XZ_JITTER;
            double offsetZ = squid.getZ() + level.random.nextInt(SPAWN_XZ_JITTER * 2 + 1) - SPAWN_XZ_JITTER;
            kraken.moveTo(offsetX, SPAWN_Y, offsetZ, level.random.nextFloat() * 360.0f, 0.0f);
            kraken.finalizeSpawn(level, level.getCurrentDifficultyAt(kraken.blockPosition()),
                    MobSpawnType.EVENT, null);
            level.addFreshEntity(kraken);
        }
    }
}
