package danger.orespawn.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;

/**
 * Jeffery is a smaller, weaker variant of the Giant Robot. In the original
 * OreSpawn 1.7.10 mod, Jeffery was registered as a separate entity with its
 * own stat block — visually identical to the Giant Robot but significantly
 * less durable and dangerous, serving as a mid-tier robot encounter.
 */
public class Jeffery extends GiantRobot {

    public Jeffery(EntityType<? extends Jeffery> type, Level level) {
        super(type, level);
        this.xpReward = 250;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 1000.0)
                .add(Attributes.MOVEMENT_SPEED, 0.55)
                .add(Attributes.ATTACK_DAMAGE, 50.0)
                .add(Attributes.ARMOR, 6.0);
    }
}
