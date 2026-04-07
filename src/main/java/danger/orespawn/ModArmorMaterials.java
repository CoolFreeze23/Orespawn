package danger.orespawn;

import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.EnumMap;
import java.util.List;

public class ModArmorMaterials {
    public static final DeferredRegister<ArmorMaterial> ARMOR_MATERIALS =
            DeferredRegister.create(Registries.ARMOR_MATERIAL, OreSpawnMod.MOD_ID);

    private static EnumMap<ArmorItem.Type, Integer> defenseMap(int boots, int legs, int chest, int helmet) {
        return Util.make(new EnumMap<>(ArmorItem.Type.class), map -> {
            map.put(ArmorItem.Type.BOOTS, boots);
            map.put(ArmorItem.Type.LEGGINGS, legs);
            map.put(ArmorItem.Type.CHESTPLATE, chest);
            map.put(ArmorItem.Type.HELMET, helmet);
            map.put(ArmorItem.Type.BODY, chest);
        });
    }

    private static List<ArmorMaterial.Layer> layer(String name) {
        return List.of(new ArmorMaterial.Layer(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, name)));
    }

    public static final Holder<ArmorMaterial> ULTIMATE = ARMOR_MATERIALS.register("ultimate",
            () -> new ArmorMaterial(defenseMap(6, 10, 12, 6), 100,
                    SoundEvents.ARMOR_EQUIP_GENERIC, () -> Ingredient.of(ModItems.INGOT_URANIUM.get()), layer("ultimate"), 2.0F, 0F));

    public static final Holder<ArmorMaterial> MOBZILLA = ARMOR_MATERIALS.register("mobzilla",
            () -> new ArmorMaterial(defenseMap(7, 11, 13, 7), 150,
                    SoundEvents.ARMOR_EQUIP_GENERIC, () -> Ingredient.of(ModBlocks.BLOCK_MOBZILLA_SCALE.get()), layer("mobzilla"), 3.0F, 0.1F));

    public static final Holder<ArmorMaterial> LAVA_EEL = ARMOR_MATERIALS.register("lava_eel",
            () -> new ArmorMaterial(defenseMap(2, 5, 7, 2), 35,
                    SoundEvents.ARMOR_EQUIP_GENERIC, () -> Ingredient.of(ModItems.LAVA_EEL.get()), layer("lavaeel"), 0F, 0F));

    public static final Holder<ArmorMaterial> MOTH_SCALE = ARMOR_MATERIALS.register("moth_scale",
            () -> new ArmorMaterial(defenseMap(2, 5, 7, 2), 50,
                    SoundEvents.ARMOR_EQUIP_GENERIC, () -> Ingredient.of(ModItems.MOTH_SCALE.get()), layer("mothscale"), 0F, 0F));

    public static final Holder<ArmorMaterial> EMERALD = ARMOR_MATERIALS.register("emerald",
            () -> new ArmorMaterial(defenseMap(3, 6, 8, 3), 40,
                    SoundEvents.ARMOR_EQUIP_GENERIC, () -> Ingredient.of(Items.EMERALD), layer("emerald"), 0F, 0F));

    public static final Holder<ArmorMaterial> EXPERIENCE = ARMOR_MATERIALS.register("experience",
            () -> new ArmorMaterial(defenseMap(4, 7, 9, 5), 50,
                    SoundEvents.ARMOR_EQUIP_GENERIC, () -> Ingredient.of(Items.EXPERIENCE_BOTTLE), layer("experience"), 1.0F, 0F));

    public static final Holder<ArmorMaterial> RUBY = ARMOR_MATERIALS.register("ruby",
            () -> new ArmorMaterial(defenseMap(4, 8, 9, 4), 40,
                    SoundEvents.ARMOR_EQUIP_GENERIC, () -> Ingredient.of(ModItems.RUBY.get()), layer("ruby"), 1.0F, 0F));

    public static final Holder<ArmorMaterial> AMETHYST = ARMOR_MATERIALS.register("amethyst",
            () -> new ArmorMaterial(defenseMap(3, 7, 8, 4), 40,
                    SoundEvents.ARMOR_EQUIP_GENERIC, () -> Ingredient.of(ModItems.AMETHYST_GEM.get()), layer("amethyst"), 1.0F, 0F));

    public static final Holder<ArmorMaterial> PINK = ARMOR_MATERIALS.register("pink",
            () -> new ArmorMaterial(defenseMap(2, 5, 7, 3), 40,
                    SoundEvents.ARMOR_EQUIP_GENERIC, () -> Ingredient.of(ModItems.CRYSTAL_PINK_INGOT.get()), layer("pink"), 0F, 0F));

    public static final Holder<ArmorMaterial> TIGERS_EYE = ARMOR_MATERIALS.register("tigers_eye",
            () -> new ArmorMaterial(defenseMap(4, 7, 8, 4), 55,
                    SoundEvents.ARMOR_EQUIP_GENERIC, () -> Ingredient.of(ModItems.TIGERS_EYE_INGOT.get()), layer("tigerseye"), 1.0F, 0F));

    public static final Holder<ArmorMaterial> PEACOCK = ARMOR_MATERIALS.register("peacock",
            () -> new ArmorMaterial(defenseMap(2, 4, 5, 2), 30,
                    SoundEvents.ARMOR_EQUIP_GENERIC, () -> Ingredient.of(ModItems.PEACOCK_FEATHER.get()), layer("peacock"), 0F, 0F));

    public static final Holder<ArmorMaterial> ROYAL = ARMOR_MATERIALS.register("royal",
            () -> new ArmorMaterial(defenseMap(8, 12, 14, 8), 200,
                    SoundEvents.ARMOR_EQUIP_GENERIC, () -> Ingredient.of(ModItems.INGOT_URANIUM.get()), layer("royal"), 4.0F, 0.1F));

    public static final Holder<ArmorMaterial> LAPIS = ARMOR_MATERIALS.register("lapis",
            () -> new ArmorMaterial(defenseMap(2, 5, 7, 2), 60,
                    SoundEvents.ARMOR_EQUIP_GENERIC, () -> Ingredient.of(Items.LAPIS_LAZULI), layer("lapis"), 0F, 0F));

    public static final Holder<ArmorMaterial> QUEEN = ARMOR_MATERIALS.register("queen",
            () -> new ArmorMaterial(defenseMap(9, 14, 16, 9), 150,
                    SoundEvents.ARMOR_EQUIP_GENERIC, () -> Ingredient.of(ModItems.QUEEN_SCALE.get()), layer("queen"), 3.0F, 0.1F));

    public static void register(IEventBus eventBus) {
        ARMOR_MATERIALS.register(eventBus);
    }
}
