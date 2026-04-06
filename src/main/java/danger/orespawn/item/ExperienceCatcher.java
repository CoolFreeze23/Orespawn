package danger.orespawn.item;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import java.util.List;

public class ExperienceCatcher extends Item {
    public ExperienceCatcher(Item.Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        if (level.isClientSide) return InteractionResult.SUCCESS;

        Player player = context.getPlayer();
        if (player == null) return InteractionResult.PASS;

        BlockPos pos = context.getClickedPos();
        AABB searchArea = new AABB(pos).inflate(3.0);
        List<ExperienceOrb> orbs = level.getEntitiesOfClass(ExperienceOrb.class, searchArea);

        int totalXp = 0;
        for (ExperienceOrb orb : orbs) {
            totalXp += orb.getValue();
            orb.discard();
        }

        if (totalXp > 0) {
            int emeralds = totalXp / 10;
            int gold = (totalXp % 10) / 5;
            int diamonds = totalXp / 50;

            if (emeralds > 0) {
                player.getInventory().add(new ItemStack(Items.EMERALD, Math.min(emeralds, 64)));
            }
            if (gold > 0) {
                player.getInventory().add(new ItemStack(Items.GOLD_INGOT, Math.min(gold, 64)));
            }
            if (diamonds > 0) {
                player.getInventory().add(new ItemStack(Items.DIAMOND, Math.min(diamonds, 64)));
            }

            level.playSound(null, player.blockPosition(), SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 1.0F, 1.0F);
        }

        context.getItemInHand().shrink(1);
        return InteractionResult.SUCCESS;
    }
}
