package danger.orespawn.item;

import danger.orespawn.entity.AntRobot;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

import java.util.function.Supplier;

/**
 * Robot kit, ported from 1.7.10 ItemSpiderRobotKit.java:35-61. Spawns the
 * robot on top of the clicked block with health equal to the kit's remaining
 * durability (the wrench stores missing HP as item damage), carries the kit's
 * custom name onto the robot, and marks ant robots as owned.
 */
public class ItemSpiderRobotKit extends Item {
    private final Supplier<EntityType<? extends Mob>> robotType;

    public ItemSpiderRobotKit(Item.Properties properties, Supplier<EntityType<? extends Mob>> robotType) {
        super(properties);
        this.robotType = robotType;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        if (level.isClientSide) return InteractionResult.SUCCESS;

        Player player = context.getPlayer();
        if (player == null) return InteractionResult.PASS;

        ItemStack stack = context.getItemInHand();
        BlockPos pos = context.getClickedPos();
        Mob robot = this.robotType.get().create(level);
        if (robot != null) {
            // orig ItemSpiderRobotKit.java:45 — spawn at clicked +0.5 / +1.01 / +0.5
            robot.moveTo(pos.getX() + 0.5, pos.getY() + 1.01, pos.getZ() + 0.5, player.getYRot(), 0.0F);
            // orig ItemSpiderRobotKit.java:47 — health = maxDamage - kit damage
            robot.setHealth(stack.getMaxDamage() - stack.getDamageValue());
            // orig ItemSpiderRobotKit.java:48-50 — carry custom name
            if (stack.has(net.minecraft.core.component.DataComponents.CUSTOM_NAME)) {
                robot.setCustomName(stack.getHoverName());
            }
            // orig ItemSpiderRobotKit.java:52-55 — kit-spawned ant robots are owned
            if (robot instanceof AntRobot ant) {
                ant.setOwned();
            }
            level.addFreshEntity(robot);
            // orig ItemSpiderRobotKit.java:51 — random.explode 1.0 / 0.9-1.1
            level.playSound(null, player.blockPosition(), SoundEvents.GENERIC_EXPLODE.value(),
                    SoundSource.PLAYERS, 1.0F, level.random.nextFloat() * 0.2F + 0.9F);
        }

        // orig ItemSpiderRobotKit.java:57-59 — consume one unless creative
        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }
        return InteractionResult.CONSUME;
    }
}
