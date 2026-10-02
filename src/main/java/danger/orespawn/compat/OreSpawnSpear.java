package danger.orespawn.compat;

import danger.orespawn.item.SpearTier;
import net.gospi.mountsofmayhem.item.SpearItem;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;

/**
 * MOD-043: one tier's spear on Mounts of Mayhem's SpearItem (its charge, hold and reload as the mod runs them), with the
 * tier's numbers (SpearTier) passed in whole, so the mod's own tables never apply. Beyond the mod's spear: the tier's
 * durability, a point of wear per melee hit and repair with the tier's material, as the mod's own spears do, and its
 * hit sound, looked up by id when it plays; no left-click jab, and so no Lunge.
 */
public class OreSpawnSpear extends SpearItem {
    private static final ResourceLocation HIT = ResourceLocation.fromNamespaceAndPath(MayhemSpears.MOD_ID, "item.spear.hit");
    /** The mod's Lunge: it powers the left-click jab alone, which these spears do not have. */
    public static final ResourceLocation LUNGE = ResourceLocation.fromNamespaceAndPath(MayhemSpears.MOD_ID, "lunge");

    private final SpearTier tier;

    public OreSpawnSpear(SpearTier tier) {
        super(new Item.Properties().durability(tier.durability())
                        .attributes(createAttackAttributes(tier.melee(), SpearTier.ATTACK_SPEED)),
                tier.row().holdTicks(), tier.row().reloadTicks(), tier.melee(), tier.row().multiplier(),
                tier.row().vehicleBonus());
        this.tier = tier;
    }

    public SpearTier spearTier() {
        return tier;
    }

    @Override
    public boolean isValidRepairItem(ItemStack stack, ItemStack repair) {
        return tier.tier().getRepairIngredient().test(repair);
    }

    /**
     * Every enchantment the spears' tags allow (Unbreaking and Mending through enchantable/durability, as the mod's own
     * spears) except Lunge, which #minecraft:spears would let an anvil put on for nothing.
     */
    @Override
    public boolean supportsEnchantment(ItemStack stack, Holder<Enchantment> enchantment) {
        return !enchantment.is(LUNGE) && super.supportsEnchantment(stack, enchantment);
    }

    /** A melee hit: the mod's hit sound where the target stands; the wear follows in postHurtEnemy. */
    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        SoundEvent hit = BuiltInRegistries.SOUND_EVENT.get(HIT);
        if (hit != null && !target.level().isClientSide()) {
            target.level().playSound(null, target.blockPosition(), hit, SoundSource.NEUTRAL, 1.0F, 1.0F);
        }
        return true;
    }

    @Override
    public void postHurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        stack.hurtAndBreak(1, attacker, EquipmentSlot.MAINHAND);
    }
}
