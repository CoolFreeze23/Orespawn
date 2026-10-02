package danger.orespawn.client.armour;

import danger.orespawn.OreSpawnConfig;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import software.bernie.geckolib.animatable.GeoAnimatable;

import java.lang.ref.WeakReference;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * Whether a piece draws in the modern style: the setting, its modern model built, and a wearer whose armour layer
 * draws the piece's own model and texture together. A piece whose geometry failed to build keeps its classic look
 * whole, model and texture alike, so the classic model is never drawn with the modern atlas; so does a piece on a mob
 * GeckoLib draws. A Doggy Talents Next dog is handed the classic model. A caller whose wearer and stack the model hook
 * has not just recorded (one that asks for the texture alone, a dog's armour among them) gets the set's dog texture
 * (the modern base in the classic layout) while modern.dogArmour is on, the classic texture otherwise. No client
 * classes here: the armour item's texture hook asks it.
 */
public final class ArmourPieces {
    private static final String DOGGY_TALENTS = "doggytalents";
    private static final Set<String> UNAVAILABLE = ConcurrentHashMap.newKeySet();
    /**
     * The wearer and the stack the model hook was last asked about in the modern style, held weakly, and whether it
     * handed them the modern model.
     */
    private static volatile Asked asked = new Asked(new WeakReference<>(null), new WeakReference<>(null), false);

    private record Asked(WeakReference<Object> wearer, WeakReference<Object> stack, boolean modern) {
        boolean about(Object wearer, Object stack) {
            return wearer != null && stack != null && this.wearer.get() == wearer && this.stack.get() == stack;
        }
    }

    private ArmourPieces() {
    }

    /** The piece of a slot ("helmet", "chestplate", "leggings", "boots"), or null for a slot armour does not use. */
    public static String piece(EquipmentSlot slot) {
        return switch (slot) {
            case HEAD -> "helmet";
            case CHEST -> "chestplate";
            case LEGS -> "leggings";
            case FEET -> "boots";
            default -> null;
        };
    }

    /**
     * Whether the model hook takes part for this wearer: one whose armour is drawn on the model the hook hands, with the
     * texture the item gives. A call without a wearer and a mob GeckoLib draws keep the classic look, model and texture
     * alike (the handshake needs a wearer; GeckoLib's layer draws the classic texture); a Doggy Talents Next dog is
     * handed the classic model, and {@link #dogTexture} answers for its texture.
     */
    private static boolean modernWearer(Object wearer) {
        return wearer != null && !drawnByGeckoLib(wearer) && !doggyTalentsDog(wearer);
    }

    /**
     * Whether the modern texture may go with this wearer's piece: the style, a wearer the model hook takes part for and
     * the piece built ({@link #modernTexture} adds that the hook handed this wearer and stack the modern model).
     */
    public static boolean modern(String set, EquipmentSlot slot, Entity wearer) {
        return modernWearer(wearer) && ArmourStyleConfig.modern() && !UNAVAILABLE.contains(set + "_" + piece(slot));
    }

    /**
     * The model hook: the piece's modern model, which {@code model} builds or looks up (null when its geometry failed),
     * or null for the classic model the hook was handed. In the modern style each call for a wearer the hook takes part
     * for is recorded, the classic model handed as well, so the texture hook can tell the layer that asked from a caller
     * that asks for the texture alone; nothing is built or recorded otherwise.
     */
    public static <M> M handModel(Entity wearer, Object stack, Supplier<M> model) {
        return handModel(ArmourStyleConfig.modern(), wearer, stack, model);
    }

    /** {@link #handModel(Entity, Object, Supplier)}'s rule with the setting given. */
    public static <M> M handModel(boolean modernStyle, Object wearer, Object stack, Supplier<M> model) {
        if (!modernStyle || !modernWearer(wearer)) return null;
        M handed = model.get();
        asked(wearer, stack, handed != null);
        return handed;
    }

    /**
     * The texture hook's answer: the modern texture only for the wearer and stack the model hook was just asked about
     * and handed the modern model (an armour layer asks for the model, then for each layer's texture). A caller that
     * asks for the texture alone draws it on a model of its own, laid out for the classic texture (Doggy Talents Next's
     * dog armour), so it never gets the modern atlas: {@link #dogTexture} answers it.
     */
    public static boolean modernTexture(String set, EquipmentSlot slot, Entity wearer, ItemStack stack) {
        return modern(set, slot, wearer) && issuedFor(wearer, stack);
    }

    /**
     * MOD-042, the texture hook's answer to a caller whose wearer and stack the model hook has not just recorded and
     * that draws the texture on a model of its own laid out for the classic layer 1, such as Doggy Talents Next's dog
     * armour (it asks for the texture alone, and its dogs are never recorded), which asks once per item and keeps the
     * answer for the session: in the modern style with modern.dogArmour on, the set's dog texture. A wearer and stack
     * the model hook recorded keep the texture of the model it handed them, so a piece whose geometry failed stays
     * classic where it was asked for; the dog texture is the modern base's boxes in the classic layout and uses none of
     * the set's geometry, so a dog takes it all the same. A mob GeckoLib draws stays classic, as above, and so does a
     * call without a wearer.
     */
    public static boolean dogTexture(Entity wearer, ItemStack stack) {
        return wearer != null && dogTexture(ArmourStyleConfig.modern(), OreSpawnConfig.dogArmour(), wearer, stack);
    }

    /** {@link #dogTexture(Entity, ItemStack)}'s rule with the settings given. */
    public static boolean dogTexture(boolean modernStyle, boolean dogArmour, Object wearer, Object stack) {
        return modernStyle && dogArmour && wearer != null && !drawnByGeckoLib(wearer) && !askedFor(wearer, stack);
    }

    /** The model hook, asked about this wearer's stack, hands the modern model, or the classic one. */
    private static void asked(Object wearer, Object stack, boolean modern) {
        asked = new Asked(new WeakReference<>(wearer), new WeakReference<>(stack), modern);
    }

    /** Whether the model hook was last asked about this wearer's stack (the same objects). */
    public static boolean askedFor(Object wearer, Object stack) {
        return asked.about(wearer, stack);
    }

    /** Whether the model hook, last asked about this wearer's stack, handed them the modern model. */
    public static boolean issuedFor(Object wearer, Object stack) {
        Asked last = asked;
        return last.modern() && last.about(wearer, stack);
    }

    /**
     * A wearer whose armour GeckoLib's item armour layer draws (a GeoAnimatable mob, such as Iron's Spells' wizards):
     * that layer asks the item for its model but always draws the material's classic texture, so the modern model
     * would be drawn on the classic texture there.
     */
    public static boolean drawnByGeckoLib(Object wearer) {
        return wearer instanceof GeoAnimatable;
    }

    /**
     * A Doggy Talents Next dog: that mod draws its armour on models of its own laid out for the classic texture, with
     * the texture it asked for once per item. Under its third-party helmet option it also asks the model hook for a
     * helmet, handing it a dummy: any other model it gets back it draws with that texture, the dummy itself sends it to
     * its own helmet models. So the hook hands a dog the classic model it was given, unrecorded, and the dog's texture
     * is the dog texture or the classic one.
     */
    public static boolean doggyTalentsDog(Object wearer) {
        return wearer instanceof Entity entity && doggyTalentsType(EntityType.getKey(entity.getType()));
    }

    /** Whether an entity type is Doggy Talents Next's (by its namespace). */
    public static boolean doggyTalentsType(ResourceLocation type) {
        return type != null && DOGGY_TALENTS.equals(type.getNamespace());
    }

    static void unavailable(String key) {
        UNAVAILABLE.add(key);
    }

    static void clear() {
        UNAVAILABLE.clear();
    }
}
