package danger.orespawn.item;

import danger.orespawn.util.OreSpawnEnchantHelper;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;

import java.util.Map;
import java.util.Set;

public class ItemOreSpawnArmor extends ArmorItem {
    private final String armorMaterialName;

    private record EnchantEntry(ResourceKey<Enchantment> key, int level) {}

    private record ArmorEnchants(EnchantEntry[] allPieces, EnchantEntry[] helmet, EnchantEntry[] boots) {}

    private static final Map<String, ArmorEnchants> ENCHANT_TABLE = Map.ofEntries(
        Map.entry("mobzilla", new ArmorEnchants(
            new EnchantEntry[]{
                new EnchantEntry(Enchantments.PROTECTION, 10),
                new EnchantEntry(Enchantments.FIRE_PROTECTION, 10),
                new EnchantEntry(Enchantments.BLAST_PROTECTION, 10),
                new EnchantEntry(Enchantments.PROJECTILE_PROTECTION, 10),
                new EnchantEntry(Enchantments.UNBREAKING, 5)
            },
            new EnchantEntry[]{
                new EnchantEntry(Enchantments.RESPIRATION, 1),
                new EnchantEntry(Enchantments.AQUA_AFFINITY, 2)
            },
            new EnchantEntry[]{ new EnchantEntry(Enchantments.FEATHER_FALLING, 10) }
        )),
        Map.entry("royal", new ArmorEnchants(
            new EnchantEntry[]{
                new EnchantEntry(Enchantments.PROTECTION, 10),
                new EnchantEntry(Enchantments.FIRE_PROTECTION, 10),
                new EnchantEntry(Enchantments.BLAST_PROTECTION, 10),
                new EnchantEntry(Enchantments.PROJECTILE_PROTECTION, 10),
                new EnchantEntry(Enchantments.UNBREAKING, 5)
            },
            new EnchantEntry[]{
                new EnchantEntry(Enchantments.RESPIRATION, 1),
                new EnchantEntry(Enchantments.AQUA_AFFINITY, 2)
            },
            new EnchantEntry[]{ new EnchantEntry(Enchantments.FEATHER_FALLING, 10) }
        )),
        Map.entry("ultimate", new ArmorEnchants(
            new EnchantEntry[]{
                new EnchantEntry(Enchantments.PROTECTION, 5),
                new EnchantEntry(Enchantments.FIRE_PROTECTION, 5),
                new EnchantEntry(Enchantments.BLAST_PROTECTION, 5),
                new EnchantEntry(Enchantments.PROJECTILE_PROTECTION, 5),
                new EnchantEntry(Enchantments.UNBREAKING, 3)
            },
            new EnchantEntry[]{
                new EnchantEntry(Enchantments.RESPIRATION, 2),
                new EnchantEntry(Enchantments.AQUA_AFFINITY, 3)
            },
            new EnchantEntry[]{ new EnchantEntry(Enchantments.FEATHER_FALLING, 3) }
        )),
        Map.entry("lavaeel", new ArmorEnchants(
            new EnchantEntry[]{
                new EnchantEntry(Enchantments.PROTECTION, 2),
                new EnchantEntry(Enchantments.BLAST_PROTECTION, 10),
                new EnchantEntry(Enchantments.UNBREAKING, 3)
            },
            new EnchantEntry[]{
                new EnchantEntry(Enchantments.RESPIRATION, 1),
                new EnchantEntry(Enchantments.AQUA_AFFINITY, 2)
            },
            new EnchantEntry[]{ new EnchantEntry(Enchantments.FEATHER_FALLING, 2) }
        )),
        Map.entry("mothscale", new ArmorEnchants(
            new EnchantEntry[]{
                new EnchantEntry(Enchantments.PROTECTION, 3),
                new EnchantEntry(Enchantments.FIRE_PROTECTION, 3),
                new EnchantEntry(Enchantments.BLAST_PROTECTION, 3),
                new EnchantEntry(Enchantments.UNBREAKING, 3)
            },
            new EnchantEntry[0],
            new EnchantEntry[]{ new EnchantEntry(Enchantments.FEATHER_FALLING, 5) }
        )),
        Map.entry("peacock", new ArmorEnchants(
            new EnchantEntry[0],
            new EnchantEntry[0],
            new EnchantEntry[]{ new EnchantEntry(Enchantments.FEATHER_FALLING, 10) }
        )),
        Map.entry("lapis", new ArmorEnchants(
            new EnchantEntry[]{
                new EnchantEntry(Enchantments.PROTECTION, 1),
                new EnchantEntry(Enchantments.PROJECTILE_PROTECTION, 1)
            },
            new EnchantEntry[]{
                new EnchantEntry(Enchantments.RESPIRATION, 1),
                new EnchantEntry(Enchantments.AQUA_AFFINITY, 1)
            },
            new EnchantEntry[0]
        )),
        Map.entry("experience", new ArmorEnchants(
            new EnchantEntry[]{
                new EnchantEntry(Enchantments.PROTECTION, 2),
                new EnchantEntry(Enchantments.BLAST_PROTECTION, 1)
            },
            new EnchantEntry[0],
            new EnchantEntry[]{ new EnchantEntry(Enchantments.FEATHER_FALLING, 1) }
        ))
    );

    private static final Set<String> GLIDE_MATERIALS = Set.of("royal", "peacock");
    private static final Set<String> GLIDE_BOOT_MATERIALS = Set.of("royal", "peacock");
    private static final double GLIDE_FALL_CAP = -0.1;
    private static final double QUEEN_FALL_CAP = -0.25;

    public ItemOreSpawnArmor(Holder<ArmorMaterial> material, Type type, Item.Properties properties, String armorMaterialName) {
        super(material, type, properties);
        this.armorMaterialName = armorMaterialName;
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        if (level.isClientSide) return;

        if (!OreSpawnEnchantHelper.hasAnyEnchantments(stack)) {
            ArmorEnchants enchants = ENCHANT_TABLE.get(armorMaterialName);
            if (enchants != null) {
                for (EnchantEntry e : enchants.allPieces()) {
                    OreSpawnEnchantHelper.applyEnchantment(stack, level, e.key(), e.level());
                }
                if (getType() == Type.HELMET) {
                    for (EnchantEntry e : enchants.helmet()) {
                        OreSpawnEnchantHelper.applyEnchantment(stack, level, e.key(), e.level());
                    }
                }
                if (getType() == Type.BOOTS) {
                    for (EnchantEntry e : enchants.boots()) {
                        OreSpawnEnchantHelper.applyEnchantment(stack, level, e.key(), e.level());
                    }
                }
            }
        }

        if (entity instanceof Player player && getType() == Type.CHESTPLATE) {
            ItemStack worn = player.getItemBySlot(EquipmentSlot.CHEST);
            if (worn == stack) {
                applyGlideEffect(player);
            }
        }
    }

    private void applyGlideEffect(Player player) {
        ItemStack boots = player.getItemBySlot(EquipmentSlot.FEET);
        if (boots.isEmpty() || !(boots.getItem() instanceof ItemOreSpawnArmor bootArmor)) return;
        String bootMat = bootArmor.armorMaterialName;

        double fallCap;
        if (GLIDE_MATERIALS.contains(armorMaterialName) && GLIDE_BOOT_MATERIALS.contains(bootMat)) {
            fallCap = GLIDE_FALL_CAP;
        } else if ("queen".equals(armorMaterialName) && "queen".equals(bootMat)) {
            fallCap = QUEEN_FALL_CAP;
        } else {
            return;
        }

        net.minecraft.world.phys.Vec3 motion = player.getDeltaMovement();
        if (motion.y < fallCap) {
            player.setDeltaMovement(motion.x, fallCap, motion.z);
        }
        player.fallDistance = 0.0f;
    }

    public String getArmorMaterialName() {
        return armorMaterialName;
    }
}
