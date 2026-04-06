package danger.orespawn.item;

import net.minecraft.core.Holder;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

public class ItemOreSpawnArmor extends ArmorItem {
    private final String armorMaterialName;

    public ItemOreSpawnArmor(Holder<ArmorMaterial> material, Type type, Item.Properties properties, String armorMaterialName) {
        super(material, type, properties);
        this.armorMaterialName = armorMaterialName;
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        // TODO: Enchantments are data-driven in 1.21.1, need registry lookup
        // if (!level.isClientSide && stack.getEnchantmentLevel(Enchantments.ALL_DAMAGE_PROTECTION) <= 0) {
        //     stack.enchant(Enchantments.ALL_DAMAGE_PROTECTION, 4);
        //     stack.enchant(Enchantments.FIRE_PROTECTION, 4);
        //     stack.enchant(Enchantments.BLAST_PROTECTION, 4);
        //     stack.enchant(Enchantments.PROJECTILE_PROTECTION, 4);
        //     stack.enchant(Enchantments.UNBREAKING, 3);
        //
        //     if (getType() == Type.BOOTS) {
        //         stack.enchant(Enchantments.FALL_PROTECTION, 4);
        //     }
        //     if (getType() == Type.HELMET) {
        //         stack.enchant(Enchantments.RESPIRATION, 3);
        //         stack.enchant(Enchantments.AQUA_AFFINITY, 1);
        //     }
        // }
    }

    public String getArmorMaterialName() {
        return armorMaterialName;
    }
}
