package danger.orespawn.client.armour;

import com.mojang.logging.LogUtils;
import danger.orespawn.OreSpawnMod;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * The modern armour style's client side (MOD-039), wired from OreSpawnClient. All three parts read the setting at
 * every use, so the switch is instant: the armour model (the set's 3D piece, or the classic model it was handed), the
 * texture (ItemOreSpawnArmor.getArmorTexture: the modern layer, the classic one, or the dog texture for a caller that
 * draws its own model, MOD-042) and the icon (the item models' {@code orespawn:armour_style} predicate picks the modern
 * model through their one overrides entry). Doggy Talents Next keeps a dog's texture for the session, so its dogs
 * follow a switch after a restart.
 */
public final class ArmourStyleClient {
    public static final String[] SETS = {"queen", "royal", "mobzilla", "ultimate", "emerald", "ruby", "amethyst",
            "lapis", "tigerseye", "pink", "experience", "mothscale", "lavaeel", "peacock"};
    public static final String[] PIECES = {"helmet", "chestplate", "leggings", "boots"};
    public static final ResourceLocation STYLE_PROPERTY =
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "armour_style");

    private ArmourStyleClient() {
    }

    /** Each set's pieces get the extension that hands the armour layer the set's modern model. */
    public static void registerExtensions(RegisterClientExtensionsEvent event) {
        int registered = 0;
        for (String set : SETS) {
            List<Item> items = new ArrayList<>();
            for (String piece : PIECES) {
                Item item = item(set, piece);
                if (item != Items.AIR) items.add(item);
            }
            if (items.isEmpty()) continue;
            event.registerItem(new SetExtensions(set), items.toArray(Item[]::new));
            registered += items.size();
        }
        if (registered != SETS.length * PIECES.length) {
            LogUtils.getLogger().warn("OreSpawn armour style: {} of {} armour items found", registered,
                    SETS.length * PIECES.length);
        }
    }

    /** The icons' predicate: 1 in the modern style, 0 in the classic one (run on the main thread at client setup). */
    public static void registerItemProperties() {
        for (String set : SETS) {
            for (String piece : PIECES) {
                Item item = item(set, piece);
                if (item != Items.AIR) {
                    ItemProperties.register(item, STYLE_PROPERTY,
                            (stack, level, entity, seed) -> ArmourStyleConfig.modern() ? 1.0F : 0.0F);
                }
            }
        }
    }

    /** The piece models are built again from the resources after a reload. */
    public static void registerReloadListener(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener((ResourceManagerReloadListener) manager -> ArmourModels.clear());
    }

    private static Item item(String set, String piece) {
        return BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, set + "_" + piece));
    }

    /**
     * One set's pieces: the modern model of the slot, posed and fitted as the wearer's own armour model, or the classic
     * model it was handed (ArmourPieces.handModel: the setting, a wearer whose layer draws this model with the item's
     * texture, the piece built; each call in the modern style recorded for the texture hook).
     */
    private record SetExtensions(String set) implements IClientItemExtensions {
        @Override
        public HumanoidModel<?> getHumanoidArmorModel(LivingEntity entity, ItemStack stack, EquipmentSlot slot,
                                                      HumanoidModel<?> original) {
            ArmourModel model = ArmourPieces.handModel(entity, stack, () -> ArmourModels.get(set, slot));
            if (model == null) return original;
            model.follow(original, slot);
            return model;
        }
    }
}
