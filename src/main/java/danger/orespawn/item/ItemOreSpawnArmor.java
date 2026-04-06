package danger.orespawn.item;

import danger.orespawn.util.OreSpawnEnchantHelper;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;

public class ItemOreSpawnArmor extends ArmorItem {
    private final String armorMaterialName;

    public ItemOreSpawnArmor(Holder<ArmorMaterial> material, Type type, Item.Properties properties, String armorMaterialName) {
        super(material, type, properties);
        this.armorMaterialName = armorMaterialName;
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        if (!level.isClientSide && !OreSpawnEnchantHelper.hasAnyEnchantments(stack)) {
            OreSpawnEnchantHelper.applyEnchantment(stack, level, Enchantments.PROTECTION, 4);
            OreSpawnEnchantHelper.applyEnchantment(stack, level, Enchantments.FIRE_PROTECTION, 4);
            OreSpawnEnchantHelper.applyEnchantment(stack, level, Enchantments.BLAST_PROTECTION, 4);
            OreSpawnEnchantHelper.applyEnchantment(stack, level, Enchantments.PROJECTILE_PROTECTION, 4);
            OreSpawnEnchantHelper.applyEnchantment(stack, level, Enchantments.UNBREAKING, 3);

            if (getType() == Type.BOOTS) {
                OreSpawnEnchantHelper.applyEnchantment(stack, level, Enchantments.FEATHER_FALLING, 4);
            }
            if (getType() == Type.HELMET) {
                OreSpawnEnchantHelper.applyEnchantment(stack, level, Enchantments.RESPIRATION, 3);
                OreSpawnEnchantHelper.applyEnchantment(stack, level, Enchantments.AQUA_AFFINITY, 1);
            }
        }
    }

    public String getArmorMaterialName() {
        return armorMaterialName;
    }
}
