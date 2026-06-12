package danger.orespawn.item;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * Experience Catcher (orig ExperienceCatcher.java:29-65).
 *
 * <p>Clicking a block scans a 1×2×1 column centered on the click point
 * (orig :33). The first XP orb worth ≥3 that passes an 80% catch roll
 * (orig :37 — {@code nextInt(5)==1} is the 20% miss) is consumed and converted
 * into a Bottle o' Enchanting, a piece of string, and a stick dropped at the
 * click point (orig :38-50); one catcher is consumed unless the player is in
 * creative (orig :51-53). If nothing is caught, the catcher itself is dropped
 * at the click point and one is always removed from the stack (orig :56-62).</p>
 */
public class ExperienceCatcher extends Item {
    public ExperienceCatcher(Item.Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        if (player != null) {
            // orig ExperienceCatcher.java:30 — swing on use.
            player.swing(context.getHand());
        }
        if (level.isClientSide) return InteractionResult.SUCCESS;
        if (player == null) return InteractionResult.PASS;

        BlockPos pos = context.getClickedPos();
        Vec3 click = context.getClickLocation();
        ItemStack held = context.getItemInHand();

        // orig ExperienceCatcher.java:33 — 1×2×1 column centered on the click
        // point's X/Z, spanning the clicked block's Y to Y+2.
        AABB searchColumn = new AABB(
                click.x - 0.5, pos.getY(), click.z - 0.5,
                click.x + 0.5, pos.getY() + 2.0, click.z + 0.5);
        List<ExperienceOrb> orbs = level.getEntitiesOfClass(ExperienceOrb.class, searchColumn);

        for (ExperienceOrb orb : orbs) {
            // orig ExperienceCatcher.java:37 — value ≥3 and 80% catch chance per orb.
            if (orb.getValue() < 3 || level.random.nextInt(5) == 1) continue;
            orb.discard();
            // orig ExperienceCatcher.java:40-50 — Bottle o' Enchanting + string + stick.
            dropAtClick(level, click, pos.getY(), new ItemStack(Items.EXPERIENCE_BOTTLE));
            dropAtClick(level, click, pos.getY(), new ItemStack(Items.STRING));
            dropAtClick(level, click, pos.getY(), new ItemStack(Items.STICK));
            // orig ExperienceCatcher.java:51-53 — consumed unless creative.
            if (!player.getAbilities().instabuild) {
                held.shrink(1);
            }
            return InteractionResult.SUCCESS;
        }

        // orig ExperienceCatcher.java:56-62 — no catch: the catcher is dropped
        // at the click point and one is always removed from the stack.
        dropAtClick(level, click, pos.getY(), new ItemStack(this));
        held.shrink(1);
        return InteractionResult.SUCCESS;
    }

    /** Spawns {@code stack} as an item entity at the click point, one block up (orig drop position {@code y+1}). */
    private static void dropAtClick(Level level, Vec3 click, int blockY, ItemStack stack) {
        level.addFreshEntity(new ItemEntity(level, click.x, blockY + 1.0, click.z, stack));
    }
}
