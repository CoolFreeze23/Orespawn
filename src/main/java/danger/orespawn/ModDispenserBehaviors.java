package danger.orespawn;

import danger.orespawn.entity.Acid;
import danger.orespawn.entity.DeadIrukandji;
import danger.orespawn.entity.EntityThrownRock;
import danger.orespawn.entity.IceBall;
import danger.orespawn.entity.IrukandjiArrow;
import danger.orespawn.entity.LaserBall;
import danger.orespawn.entity.SunspotUrchin;
import danger.orespawn.entity.WaterBall;
import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;

/**
 * Dispenser behaviors for the mod's throwable projectiles.
 *
 * ORIG OreSpawnMain.java:5755-5773 registered 8 behavior classes
 * (MyDispenserBehaviorArrow/WDCharge/SunspotUrchin/Acid/Iceball/DeadIrukandji/
 * Laserball/Rock) — all subclasses of vanilla BehaviorProjectileDispense, which
 * launched the projectile with velocity 1.1 and inaccuracy 6.0 and played
 * aux effect 1002 (ORIG behavior classes are one-liners around func_82499_a).
 * The rock behavior (MyDispenserBehaviorRock.java:36-71) was shared by all
 * 12 rock items and stamped the thrown rock's type per item.
 */
public class ModDispenserBehaviors {

    @FunctionalInterface
    private interface ProjectileFactory {
        Projectile create(Level level, double x, double y, double z);
    }

    public static void register() {
        // orig OreSpawnMain.java:5755 + MyDispenserBehaviorArrow.java:18-22
        // (field_70251_a = 1 -> pickup allowed, like vanilla dispensed arrows)
        registerProjectile(ModItems.IRUKANDJI_ARROW.get(), (level, x, y, z) -> {
            IrukandjiArrow arrow = new IrukandjiArrow(level, x, y, z);
            arrow.pickup = AbstractArrow.Pickup.ALLOWED;
            return arrow;
        });
        // orig OreSpawnMain.java:5756 + MyDispenserBehaviorWDCharge.java
        registerProjectile(ModItems.WATER_BALL.get(), WaterBall::new);
        // orig OreSpawnMain.java:5757 + MyDispenserBehaviorSunspotUrchin.java
        registerProjectile(ModItems.SUNSPOT_URCHIN.get(), SunspotUrchin::new);
        // orig OreSpawnMain.java:5758 + MyDispenserBehaviorAcid.java
        registerProjectile(ModItems.ACID.get(), Acid::new);
        // orig OreSpawnMain.java:5759 + MyDispenserBehaviorIceball.java
        registerProjectile(ModItems.ICE_BALL.get(), IceBall::new);
        // orig OreSpawnMain.java:5760 + MyDispenserBehaviorDeadIrukandji.java
        // (the MyIrukandji item is the port's dead_irukandji)
        registerProjectile(ModItems.DEAD_IRUKANDJI.get(), DeadIrukandji::new);
        // orig OreSpawnMain.java:5761 + MyDispenserBehaviorLaserball.java
        registerProjectile(ModItems.LASER_BALL.get(), LaserBall::new);

        // orig OreSpawnMain.java:5762-5773 + MyDispenserBehaviorRock.java:36-71
        registerRock(ModItems.ROCK_SMALL.get(), 1);
        registerRock(ModItems.ROCK.get(), 2);
        registerRock(ModItems.ROCK_RED.get(), 3);
        registerRock(ModItems.ROCK_GREEN.get(), 4);
        registerRock(ModItems.ROCK_BLUE.get(), 5);
        registerRock(ModItems.ROCK_PURPLE.get(), 6);
        registerRock(ModItems.ROCK_SPIKEY.get(), 7);
        registerRock(ModItems.ROCK_TNT.get(), 8);
        registerRock(ModItems.ROCK_CRYSTAL_RED.get(), 9);
        registerRock(ModItems.ROCK_CRYSTAL_GREEN.get(), 10);
        registerRock(ModItems.ROCK_CRYSTAL_BLUE.get(), 11);
        registerRock(ModItems.ROCK_CRYSTAL_TNT.get(), 12);
    }

    private static void registerRock(Item item, int rockType) {
        registerProjectile(item, (level, x, y, z) -> {
            EntityThrownRock rock = new EntityThrownRock(level, x, y, z);
            rock.setRockType(rockType);
            return rock;
        });
    }

    private static void registerProjectile(Item item, ProjectileFactory factory) {
        DispenserBlock.registerBehavior(item, new DefaultDispenseItemBehavior() {
            @Override
            protected ItemStack execute(BlockSource source, ItemStack stack) {
                Level level = source.level();
                Position pos = DispenserBlock.getDispensePosition(source);
                Direction dir = source.state().getValue(DispenserBlock.FACING);
                Projectile projectile = factory.create(level, pos.x(), pos.y(), pos.z());
                // ORIG BehaviorProjectileDispense: velocity 1.1, inaccuracy 6.0,
                // +0.1 vertical bias
                projectile.shoot(dir.getStepX(), dir.getStepY() + 0.1f, dir.getStepZ(), 1.1f, 6.0f);
                level.addFreshEntity(projectile);
                stack.shrink(1);
                return stack;
            }

            @Override
            protected void playSound(BlockSource source) {
                // ORIG BehaviorProjectileDispense plays aux effect 1002 (bow sound)
                source.level().levelEvent(1002, source.pos(), 0);
            }
        });
    }
}
