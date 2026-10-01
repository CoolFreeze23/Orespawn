package danger.orespawn.client.armour;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import software.bernie.geckolib.animatable.GeoAnimatable;

import java.lang.ref.WeakReference;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Whether a piece draws in the modern style: the setting, its modern model built, and a wearer whose armour layer
 * draws the piece's own model and texture together. A piece whose geometry failed to build keeps its classic look
 * whole, model and texture alike, so the classic model is never drawn with the modern atlas; so does a piece on a mob
 * GeckoLib draws, and a piece whose texture is asked for without its model. No client classes here: the armour item's
 * texture hook asks it.
 */
public final class ArmourPieces {
    private static final Set<String> UNAVAILABLE = ConcurrentHashMap.newKeySet();
    /** The wearer and the stack the model hook last handed the modern model for, held weakly. */
    private static volatile Issued issued = new Issued(new WeakReference<>(null), new WeakReference<>(null));

    private record Issued(WeakReference<Object> wearer, WeakReference<Object> stack) {
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
     * The model hook's answer: the modern model, or the classic one it was handed. A call without a wearer keeps the
     * classic look, as its texture would (the handshake needs a wearer).
     */
    public static boolean modern(String set, EquipmentSlot slot, Entity wearer) {
        return wearer != null && ArmourStyleConfig.modern() && !UNAVAILABLE.contains(set + "_" + piece(slot))
                && !drawnByGeckoLib(wearer);
    }

    /**
     * The texture hook's answer: the modern texture only for the wearer and stack the model hook has just handed the
     * modern model (an armour layer asks for the model, then for each layer's texture). A caller that asks for the
     * texture alone draws it on a model of its own, laid out for the classic texture (Doggy Talents Next's dog armour),
     * so it gets the classic one.
     */
    public static boolean modernTexture(String set, EquipmentSlot slot, Entity wearer, ItemStack stack) {
        return modern(set, slot, wearer) && issuedFor(wearer, stack);
    }

    /** The model hook hands the modern model for this wearer's stack. */
    public static void issued(Object wearer, Object stack) {
        issued = new Issued(new WeakReference<>(wearer), new WeakReference<>(stack));
    }

    /** Whether the modern model was last handed for this wearer's stack (the same objects). */
    public static boolean issuedFor(Object wearer, Object stack) {
        Issued last = issued;
        return wearer != null && stack != null && last.wearer().get() == wearer && last.stack().get() == stack;
    }

    /**
     * A wearer whose armour GeckoLib's item armour layer draws (a GeoAnimatable mob, such as Iron's Spells' wizards):
     * that layer asks the item for its model but always draws the material's classic texture, so the modern model
     * would be drawn on the classic texture there.
     */
    public static boolean drawnByGeckoLib(Object wearer) {
        return wearer instanceof GeoAnimatable;
    }

    static void unavailable(String key) {
        UNAVAILABLE.add(key);
    }

    static void clear() {
        UNAVAILABLE.clear();
    }
}
