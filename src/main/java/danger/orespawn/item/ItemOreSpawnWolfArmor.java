package danger.orespawn.item;

import danger.orespawn.OreSpawnMod;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.AnimalArmorItem;
import net.minecraft.world.item.ArmorMaterial;

/**
 * MOD-041: a set's wolf armour, vanilla's wolf armour (a canine AnimalArmorItem) on the set's armour material: the
 * chestplate's armour value (the material's body value), the material's toughness and knockback resistance, no
 * durability, one to a stack, put on with vanilla's wolf armour's sound. Vanilla's canine texture path leaves the
 * material out, so each set gives its own, {@code textures/entity/wolf/armor/wolf_armor_<material>.png}. A wolf puts on
 * only Items.WOLF_ARMOR itself, and its armour layer and shears act only while Wolf.hasArmor(), which asks for that item
 * too, so WolfArmourEvents puts this on and takes it off and the client's WolfArmourLayer draws it; with hasArmor()
 * false the wolf's damage is cut by the armour's values and none of it is absorbed into the armour.
 */
public class ItemOreSpawnWolfArmor extends AnimalArmorItem {
    private final ResourceLocation texture;

    public ItemOreSpawnWolfArmor(Holder<ArmorMaterial> material, Properties properties) {
        super(material, BodyType.CANINE, false, properties);
        this.texture = ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID,
                "textures/entity/wolf/armor/wolf_armor_" + material.unwrapKey().orElseThrow().location().getPath()
                        + ".png");
    }

    @Override
    public ResourceLocation getTexture() {
        return texture;
    }

    @Override
    public Holder<SoundEvent> getEquipSound() {
        return SoundEvents.ARMOR_EQUIP_WOLF;
    }
}
