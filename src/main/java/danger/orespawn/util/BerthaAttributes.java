package danger.orespawn.util;

import danger.orespawn.OreSpawnMod;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.component.ItemAttributeModifiers;

/**
 * Centralized attribute builder for the Bertha-family melee weapons (Big Bertha,
 * Royal Guardian Sword, Battle Axe, Queen Battle Axe, Chainsaw, Attitude Adjuster).
 *
 * <p>1.21.1's {@link DataComponents#ATTRIBUTE_MODIFIERS} replaces the old
 * {@code Multimap<Attribute, AttributeModifier>} pattern used in 1.12.2 / 1.7.10.
 * {@link SwordItem#createAttributes(Tier, int, float)} only emits ATTACK_DAMAGE +
 * ATTACK_SPEED — we additionally need to inject {@link Attributes#ENTITY_INTERACTION_RANGE}
 * so swinging Bertha actually reaches as far as its swept-area projectile does.
 * 1.7.10 had no such concept (reach was a hard-coded value of 5.0); this is the
 * modern faithful translation.</p>
 */
public final class BerthaAttributes {

    private static final ResourceLocation BERTHA_REACH_ID =
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "bertha_reach");

    private BerthaAttributes() {}

    /**
     * Build attribute modifiers identical to {@link SwordItem#createAttributes} but
     * with an extra {@link Attributes#ENTITY_INTERACTION_RANGE} bonus for that authentic
     * "this thing is the size of a tree trunk" feel from the original mod.
     *
     * @param tier        weapon tier (provides base attack damage bonus)
     * @param damage      additional damage on top of the tier bonus
     * @param speed       attack speed (vanilla swords use -2.4f)
     * @param extraReach  extra blocks of entity interaction range; clamped to >=0
     */
    public static ItemAttributeModifiers createReachAttributes(Tier tier, int damage, float speed, double extraReach) {
        ItemAttributeModifiers.Builder builder = ItemAttributeModifiers.builder()
                .add(Attributes.ATTACK_DAMAGE,
                        new AttributeModifier(Item.BASE_ATTACK_DAMAGE_ID,
                                damage + tier.getAttackDamageBonus(),
                                AttributeModifier.Operation.ADD_VALUE),
                        EquipmentSlotGroup.MAINHAND)
                .add(Attributes.ATTACK_SPEED,
                        new AttributeModifier(Item.BASE_ATTACK_SPEED_ID, speed,
                                AttributeModifier.Operation.ADD_VALUE),
                        EquipmentSlotGroup.MAINHAND);
        if (extraReach > 0.0) {
            builder.add(Attributes.ENTITY_INTERACTION_RANGE,
                    new AttributeModifier(BERTHA_REACH_ID, extraReach,
                            AttributeModifier.Operation.ADD_VALUE),
                    EquipmentSlotGroup.MAINHAND);
        }
        return builder.build();
    }
}
