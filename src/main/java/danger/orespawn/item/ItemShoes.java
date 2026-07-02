package danger.orespawn.item;

import danger.orespawn.entity.Shoes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * Port of orig ItemShoes.java:18-43 — the five throwable "shoes" (Red Heels 2,
 * Black Heels 3, Slippers 4, Boots 5, Game Controller 6). Right-click consumes
 * one (unless creative), plays the bow sound, and throws a {@link Shoes}
 * projectile carrying the shoe id (orig :28-37). The 1.7.10 EntityThrowable
 * owner-ctor launched along the player's look at velocity 1.5/inaccuracy 1.0,
 * mapped to shootFromRotation.
 */
public class ItemShoes extends Item {
    private final int shoeId;

    public ItemShoes(Item.Properties properties, int shoeId) {
        super(properties);
        this.shoeId = shoeId;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.ARROW_SHOOT, SoundSource.NEUTRAL,
                0.5f, 0.4f / (level.random.nextFloat() * 0.4f + 0.8f));
        if (!level.isClientSide) {
            Shoes shoes = new Shoes(level, player, this.shoeId);
            shoes.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0f, 1.5f, 1.0f);
            level.addFreshEntity(shoes);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }
}
