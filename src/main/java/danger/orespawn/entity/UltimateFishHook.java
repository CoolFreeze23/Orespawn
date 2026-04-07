package danger.orespawn.entity;

import danger.orespawn.ModEntities;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.level.Level;

public class UltimateFishHook extends FishingHook {
    public UltimateFishHook(EntityType<? extends UltimateFishHook> type, Level level) {
        super(type, level);
    }

    public UltimateFishHook(Player player, Level level, int luck, int lureSpeed) {
        super(player, level, luck + 3, lureSpeed + 2);
    }
}
