package danger.orespawn;

import danger.orespawn.item.ItemOreSpawnWolfArmor;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.gameevent.GameEvent;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

import javax.annotation.Nullable;

/**
 * MOD-041: the sets' wolf armour on a wolf. Vanilla's wolf takes only its own wolf armour (Wolf.mobInteract asks for
 * Items.WOLF_ARMOR, and its shears take armour off only while Wolf.hasArmor(), which asks the same), so this does both
 * for ours, on vanilla's terms: a tamed wolf's owner puts a piece on with a click while the wolf is alive, grown and
 * wears none, and takes it off with shears unless a curse binds it (creative aside); the shears wear a point and the
 * piece drops. A wolf led by the clicking player is let go first, as vanilla's click does, and the armour goes on
 * vanilla's wolf only: a summoned one (Ars Nouveau's) vanishes with what it wears. The wolf's own interaction does not
 * run then (it does not sit down). It runs on both sides and answers as vanilla's wolf does, consume on the client and
 * success on the server, whose swing reaches the player and those around; the server makes the change.
 */
@EventBusSubscriber(modid = OreSpawnMod.MOD_ID)
public final class WolfArmourEvents {
    /** Doggy Talents Next's training treat, by its id: that mod is no dependency of ours. */
    private static final ResourceLocation TRAINING_TREAT = ResourceLocation.fromNamespaceAndPath("doggytalents",
            "training_treat");

    private WolfArmourEvents() {
    }

    /**
     * Doggy Talents Next trains a wolf into one of its dogs with its training treat (the client sends the training from
     * the click, the server makes the dog) and carries only vanilla's wolf armour over before it discards the wolf. So a
     * treat click on a wolf wearing ours that the treat would train (an untamed one, or the clicker's own) is held back
     * on the client, where that mod would send the training, and takes ours off on the server and drops it: no training
     * ever finds ours on the wolf, and the next click trains it. Ahead of that mod's handler, and on the server even where
     * another handler turned the click down.
     */
    @SubscribeEvent(priority = EventPriority.HIGH, receiveCanceled = true)
    public static void beforeTraining(PlayerInteractEvent.EntityInteract event) {
        if (!(event.getTarget() instanceof Wolf wolf)) return;
        ResourceLocation held = BuiltInRegistries.ITEM.getKey(event.getItemStack().getItem());
        if (wolf.level().isClientSide()) {
            // a click another handler already turned down sends no training: its answer stands
            if (!event.isCanceled() && trainingFindsOurs(wolf, event.getEntity(), held)) {
                event.setCanceled(true);
                event.setCancellationResult(InteractionResult.CONSUME);
            }
        } else {
            dropBeforeTraining(wolf, event.getEntity(), held);
        }
    }

    /**
     * Whether a click with the item of this id would train the wolf into a dog while it wears our armour: the training
     * treat, a living wolf, untamed or the clicker's own (that mod's own condition, its training of untamed wolves taken
     * as on, as it is by default; where it is off, an untamed wolf wearing ours, which only commands make, drops it all the
     * same).
     */
    public static boolean trainingFindsOurs(Wolf wolf, Player player, ResourceLocation held) {
        return TRAINING_TREAT.equals(held) && wolf.isAlive() && (!wolf.isTame() || wolf.isOwnedBy(player))
                && wolf.getBodyArmorItem().getItem() instanceof ItemOreSpawnWolfArmor;
    }

    /** On the server: whether a click with the item of this id took our armour off the wolf ahead of a training. */
    public static boolean dropBeforeTraining(Wolf wolf, Player player, ResourceLocation held) {
        if (wolf.level().isClientSide() || !trainingFindsOurs(wolf, player, held)) {
            return false;
        }
        ItemStack worn = wolf.getBodyArmorItem();
        wolf.setBodyArmorItem(ItemStack.EMPTY);
        wolf.spawnAtLocation(worn);
        return true;
    }

    @SubscribeEvent
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        if (!(event.getTarget() instanceof Wolf wolf)) return;
        InteractionResult result = interact(wolf, event.getEntity(), event.getItemStack(), event.getHand());
        if (result != null) {
            event.setCanceled(true);
            event.setCancellationResult(result);
        }
    }

    /** The interaction with a wolf, or null when it is none of ours and the wolf's own runs. */
    @Nullable
    public static InteractionResult interact(Wolf wolf, Player player, ItemStack held, InteractionHand hand) {
        if (!wolf.isAlive() || wolf.getLeashHolder() == player) return null;
        boolean client = wolf.level().isClientSide();
        if (held.getItem() instanceof ItemOreSpawnWolfArmor && wolf.getType() == EntityType.WOLF && wolf.isTame()
                && wolf.isOwnedBy(player) && wolf.getBodyArmorItem().isEmpty() && !wolf.isBaby()) {
            if (!client) {
                wolf.setBodyArmorItem(held.copyWithCount(1));
                held.consume(1, player);
                wolf.gameEvent(GameEvent.ENTITY_INTERACT, player);
            }
            return client ? InteractionResult.CONSUME : InteractionResult.SUCCESS;
        }
        ItemStack worn = wolf.getBodyArmorItem();
        if (held.canPerformAction(ItemAbilities.SHEARS_REMOVE_ARMOR) && worn.getItem() instanceof ItemOreSpawnWolfArmor
                && wolf.isOwnedBy(player)
                && (!EnchantmentHelper.has(worn, EnchantmentEffectComponents.PREVENT_ARMOR_CHANGE) || player.isCreative())) {
            if (!client) {
                held.hurtAndBreak(1, player, LivingEntity.getSlotForHand(hand));
                wolf.playSound(SoundEvents.ARMOR_UNEQUIP_WOLF);
                wolf.setBodyArmorItem(ItemStack.EMPTY);
                wolf.spawnAtLocation(worn);
                wolf.gameEvent(GameEvent.ENTITY_INTERACT, player);
            }
            return client ? InteractionResult.CONSUME : InteractionResult.SUCCESS;
        }
        return null;
    }
}
