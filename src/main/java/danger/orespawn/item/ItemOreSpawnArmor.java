package danger.orespawn.item;

import danger.orespawn.OreSpawnConfig;
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

public class ItemOreSpawnArmor extends ArmorItem {
    private final String armorMaterialName;

    private record EnchantEntry(ResourceKey<Enchantment> key, int level) {}

    private record ArmorEnchants(EnchantEntry[] allPieces, EnchantEntry[] helmet, EnchantEntry[] boots) {}

    private static final Map<String, ArmorEnchants> ENCHANT_TABLE = Map.ofEntries(
        // orig OreSpawnMain.java:1498 — Mobzilla: prot10 fire10 blast10 proj10 unb5 fall10,
        // NO respiration/aqua affinity
        Map.entry("mobzilla", new ArmorEnchants(
            new EnchantEntry[]{
                new EnchantEntry(Enchantments.PROTECTION, 10),
                new EnchantEntry(Enchantments.FIRE_PROTECTION, 10),
                new EnchantEntry(Enchantments.BLAST_PROTECTION, 10),
                new EnchantEntry(Enchantments.PROJECTILE_PROTECTION, 10),
                new EnchantEntry(Enchantments.UNBREAKING, 5)
            },
            new EnchantEntry[0],
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
        // orig OreSpawnMain.java:1494 — Ultimate: resp2 aqua3 prot5 fire5 blast5 proj5 unb0 fall3
        Map.entry("ultimate", new ArmorEnchants(
            new EnchantEntry[]{
                new EnchantEntry(Enchantments.PROTECTION, 5),
                new EnchantEntry(Enchantments.FIRE_PROTECTION, 5),
                new EnchantEntry(Enchantments.BLAST_PROTECTION, 5),
                new EnchantEntry(Enchantments.PROJECTILE_PROTECTION, 5)
            },
            new EnchantEntry[]{
                new EnchantEntry(Enchantments.RESPIRATION, 2),
                new EnchantEntry(Enchantments.AQUA_AFFINITY, 3)
            },
            new EnchantEntry[]{ new EnchantEntry(Enchantments.FEATHER_FALLING, 3) }
        )),
        // orig OreSpawnMain.java:1493 — LavaEel: resp1 aqua2 prot3 fire2 blast10 proj0 unb0 fall2
        Map.entry("lavaeel", new ArmorEnchants(
            new EnchantEntry[]{
                new EnchantEntry(Enchantments.PROTECTION, 3),
                new EnchantEntry(Enchantments.FIRE_PROTECTION, 2),
                new EnchantEntry(Enchantments.BLAST_PROTECTION, 10)
            },
            new EnchantEntry[]{
                new EnchantEntry(Enchantments.RESPIRATION, 1),
                new EnchantEntry(Enchantments.AQUA_AFFINITY, 2)
            },
            new EnchantEntry[]{ new EnchantEntry(Enchantments.FEATHER_FALLING, 2) }
        )),
        // orig OreSpawnMain.java:1492 — MothScale: prot3 fire3 blast3 fall5, all else 0
        Map.entry("mothscale", new ArmorEnchants(
            new EnchantEntry[]{
                new EnchantEntry(Enchantments.PROTECTION, 3),
                new EnchantEntry(Enchantments.FIRE_PROTECTION, 3),
                new EnchantEntry(Enchantments.BLAST_PROTECTION, 3)
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

    // orig ItemOreSpawnArmor.java:348-349 — royal/peacock cap; :354-355 — queen cap
    private static final double GLIDE_FALL_CAP = -0.1;
    private static final double QUEEN_FALL_CAP = -0.25;

    public ItemOreSpawnArmor(Holder<ArmorMaterial> material, Type type, Item.Properties properties, String armorMaterialName) {
        super(material, type, properties);
        this.armorMaterialName = armorMaterialName;
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        if (level.isClientSide) return;

        // OPT-022 (ruled apply 2026-08-11 — 20-TICK-GATE variant): the
        // auto-enchant presence poll runs every 20 ticks instead of every
        // inventory tick, keyed to entity.tickCount so holders stagger
        // naturally rather than all polling on the same global tick. Gate
        // story: a freshly obtained un-enchanted piece can sit plain for
        // <=1 s (<=20 ticks) before the poll lands — accepted by the ruling.
        // The finding's alternative, migrating the enchant to
        // onCraftedBy/first-pickup, was REJECTED by the same ruling: it would
        // change WHEN loot-table, creative-given, and pre-existing stacks get
        // enchanted, and this poll is the contract that every acquisition
        // path (crafting, /give, dungeon loot, old saves) self-heals. The
        // glide handling below intentionally stays per-tick — it caps fall
        // velocity every tick and must not be gated.
        if (entity.tickCount % 20 == 0 && !OreSpawnEnchantHelper.hasAnyEnchantments(stack)) {
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

        // orig ItemOreSpawnArmor.java:343-358 — onArmorTick ran for EVERY worn
        // royal/peacock/queen piece (so peacock boots alone glide); replicate by
        // ticking the glide from any worn slot.
        if (entity instanceof Player player
                && player.getItemBySlot(getEquipmentSlot()) == stack) {
            applyGlideEffect(player);
        }
    }

    /**
     * Glide, ported from 1.7.10 ItemOreSpawnArmor.java:343-358. A worn royal or
     * peacock piece plus Royal boots (config-gated) or Peacock boots (NOT
     * config-gated, orig :347) caps falling speed at -0.1; a worn queen piece
     * plus Queen boots (config-gated, orig :353) caps it at -0.25. Both reset
     * fall distance.
     */
    private void applyGlideEffect(Player player) {
        boolean royalOrPeacockPiece = "royal".equals(armorMaterialName) || "peacock".equals(armorMaterialName);
        boolean queenPiece = "queen".equals(armorMaterialName);
        if (!royalOrPeacockPiece && !queenPiece) return;

        ItemStack boots = player.getItemBySlot(EquipmentSlot.FEET);
        if (boots.isEmpty() || !(boots.getItem() instanceof ItemOreSpawnArmor bootArmor)
                || bootArmor.getType() != Type.BOOTS) {
            return;
        }
        String bootMat = bootArmor.armorMaterialName;

        net.minecraft.world.phys.Vec3 motion = player.getDeltaMovement();
        if (royalOrPeacockPiece) {
            // orig :347 — RoyalBoots require RoyalGlideEnable; PeacockFeatherBoots never gated
            boolean glide = ("royal".equals(bootMat) && OreSpawnConfig.ROYAL_GLIDE_ENABLE.get())
                    || "peacock".equals(bootMat);
            if (!glide) return;
            if (motion.y < GLIDE_FALL_CAP) {
                player.setDeltaMovement(motion.x, GLIDE_FALL_CAP, motion.z);
            }
            player.fallDistance = 0.0f;
        } else {
            // orig :353 — queen glide requires QueenBoots and RoyalGlideEnable
            if (!"queen".equals(bootMat) || !OreSpawnConfig.ROYAL_GLIDE_ENABLE.get()) return;
            if (motion.y < QUEEN_FALL_CAP) {
                player.setDeltaMovement(motion.x, QUEEN_FALL_CAP, motion.z);
            }
            player.fallDistance = 0.0f;
        }
    }

    public String getArmorMaterialName() {
        return armorMaterialName;
    }
}
