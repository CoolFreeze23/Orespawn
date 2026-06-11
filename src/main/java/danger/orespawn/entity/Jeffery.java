package danger.orespawn.entity;

import danger.orespawn.MobStats;

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
        // orig GiantRobot.java:48 — XP = Jeffery_stats.health / 2 = 550/2
        // (Jeffery is a named-skin alias of GiantRobot; identical stats).
        this.xpReward = 275;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                // orig OreSpawnMain.java:6476 — "Jeffery" 550 HP / 40 ATK / 18 armor
                .add(Attributes.MAX_HEALTH, MobStats.JEFFERY.maxHealth())
                .add(Attributes.MOVEMENT_SPEED, 0.55)
                .add(Attributes.ATTACK_DAMAGE, MobStats.JEFFERY.attackDamage())
                .add(Attributes.ARMOR, MobStats.JEFFERY.armor());
    }
}
