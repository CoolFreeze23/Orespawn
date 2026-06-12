package danger.orespawn.item;

import danger.orespawn.ModItems;
import danger.orespawn.entity.AntRobot;
import danger.orespawn.entity.SpiderRobot;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * Wrench, ported from 1.7.10 ItemWrench.java:29-94. Left-clicking an unridden
 * SpiderRobot disassembles it into a Spider Robot Kit; an unridden AntRobot
 * only disassembles if it is owned, or unowned and below half health (which
 * claims it first). The dropped kit carries the robot's missing health as
 * item damage. Costs 2 durability per use.
 */
public class ItemWrench extends Item {
    public ItemWrench(Item.Properties properties) {
        super(properties);
    }

    @Override
    public boolean onLeftClickEntity(ItemStack stack, Player player, Entity entity) {
        if (entity instanceof SpiderRobot robot && !robot.isVehicle()) {
            // orig ItemWrench.java:30-49 — spider robot disassembles freely
            float missing = robot.getMaxHealth() - robot.getHealth();
            robot.discard();
            dropKit(player.level(), robot, new ItemStack(ModItems.SPIDER_ROBOT_KIT.get()), (int) missing);
            playDisassembleEffects(player, entity);
        } else if (entity instanceof AntRobot robot && !robot.isVehicle()) {
            // orig ItemWrench.java:50-57 — unowned ant robots resist unless
            // below half health; succeeding claims them before disassembly
            if (robot.getOwned() == 0) {
                if (robot.getHealth() / robot.getMaxHealth() > 0.5f) {
                    return false;
                }
                robot.setOwned();
            }
            float missing = robot.getMaxHealth() - robot.getHealth();
            robot.discard();
            dropKit(player.level(), robot, new ItemStack(ModItems.ANT_ROBOT_KIT.get()), (int) missing);
            playDisassembleEffects(player, entity);
        } else {
            return false;
        }
        // orig ItemWrench.java:79 — 2 damage per use
        if (!player.level().isClientSide) {
            stack.hurtAndBreak(2, player, EquipmentSlot.MAINHAND);
        }
        return true;
    }

    /** orig ItemWrench.java:86-94 — kit drops one above the robot, carrying missing HP as damage. */
    private static void dropKit(Level level, LivingEntity robot, ItemStack kit, int damage) {
        if (level.isClientSide) return;
        kit.setDamageValue(damage);
        level.addFreshEntity(new ItemEntity(level, robot.getX(), robot.getY() + 1.0, robot.getZ(), kit));
    }

    /** orig ItemWrench.java:35-49 — 8 rounds of smoke/explode/reddust puffs plus explosion sound. */
    private static void playDisassembleEffects(Player player, Entity entity) {
        if (player.level() instanceof ServerLevel serverLevel) {
            for (int n = 0; n < 8; ++n) {
                sendSpreadParticle(serverLevel, entity, ParticleTypes.SMOKE);
                sendSpreadParticle(serverLevel, entity, ParticleTypes.POOF);
                sendSpreadParticle(serverLevel, entity, DustParticleOptions.REDSTONE);
            }
        }
        // orig ItemWrench.java:49 — random.explode at 0.5 / 1.5
        player.level().playSound(null, player.blockPosition(), SoundEvents.GENERIC_EXPLODE.value(),
                SoundSource.PLAYERS, 0.5f, 1.5f);
    }

    /** orig spread pattern — nextFloat()*3 - nextFloat()*3 on x/z, 0.25 + nextFloat()*2 on y. */
    private static void sendSpreadParticle(ServerLevel level, Entity entity, ParticleOptions type) {
        double f1 = level.random.nextFloat() * 3.0f - level.random.nextFloat() * 3.0f;
        double f2 = 0.25f + level.random.nextFloat() * 2.0f;
        double f3 = level.random.nextFloat() * 3.0f - level.random.nextFloat() * 3.0f;
        level.sendParticles(type, entity.getX() + f1, entity.getY() + f2, entity.getZ() + f3, 1, 0, 0, 0, 0);
    }
}
