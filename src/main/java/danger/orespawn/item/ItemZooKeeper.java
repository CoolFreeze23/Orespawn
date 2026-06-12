package danger.orespawn.item;

import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * ZooKeeper, ported from 1.7.10 ItemZooKeeper.java:24-50. Left-clicking a mob
 * makes it persistent (never despawns) with smoke/explosion particle bursts
 * and an explosion sound; costs 2 durability per use on a 1-durability item,
 * so it breaks after a single use.
 */
public class ItemZooKeeper extends Item {
    public ItemZooKeeper(Item.Properties properties) {
        super(properties);
    }

    @Override
    public boolean onLeftClickEntity(ItemStack stack, Player player, Entity entity) {
        // orig ItemZooKeeper.java:25-38 — 8 rounds of smoke/explode/reddust
        // puffs in a 3-block spread around the target
        if (player.level() instanceof ServerLevel serverLevel) {
            for (int n = 0; n < 8; ++n) {
                sendSpreadParticle(serverLevel, entity, ParticleTypes.SMOKE);
                sendSpreadParticle(serverLevel, entity, ParticleTypes.POOF);
                sendSpreadParticle(serverLevel, entity, DustParticleOptions.REDSTONE);
            }
        }
        // orig ItemZooKeeper.java:39 — random.explode at 0.5 / 1.5
        player.level().playSound(null, player.blockPosition(), SoundEvents.GENERIC_EXPLODE.value(),
                SoundSource.PLAYERS, 0.5f, 1.5f);
        if (!(entity instanceof Mob mob)) {
            return false;
        }
        if (!player.level().isClientSide) {
            // orig ItemZooKeeper.java:44 — func_110163_bv = persistence, no despawn
            mob.setPersistenceRequired();
            // orig ItemZooKeeper.java:45 — 2 damage per use
            stack.hurtAndBreak(2, player, EquipmentSlot.MAINHAND);
        }
        return true;
    }

    /** orig spread pattern — nextFloat()*3 - nextFloat()*3 on x/z, 0.25 + nextFloat()*2 on y. */
    private static void sendSpreadParticle(ServerLevel level, Entity entity, ParticleOptions type) {
        double f1 = level.random.nextFloat() * 3.0f - level.random.nextFloat() * 3.0f;
        double f2 = 0.25f + level.random.nextFloat() * 2.0f;
        double f3 = level.random.nextFloat() * 3.0f - level.random.nextFloat() * 3.0f;
        level.sendParticles(type, entity.getX() + f1, entity.getY() + f2, entity.getZ() + f3, 1, 0, 0, 0, 0);
    }
}
