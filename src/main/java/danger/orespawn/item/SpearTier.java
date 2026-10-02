package danger.orespawn.item;

import danger.orespawn.ModToolTiers;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Tier;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Supplier;

/**
 * MOD-043: OreSpawn's spears, one per tool tier, built on Mounts of Mayhem's spear and registered only while that mod
 * is loaded (danger.orespawn.compat.MayhemSpears; this table names none of its classes, so it loads without it). Each
 * follows one rule: durability is the tier's uses; the melee damage its attack bonus + 1 at the mod's attack speed
 * (-2.8); the charge's base is that melee damage, and its speed multiplier, vehicle bonus, hold and reload ticks are the
 * mod's own spear row for the vanilla tier at the tier's mining level. The in-hand model is that row's 3D spear with
 * OreSpawn's own texture.
 */
public enum SpearTier {
    RUBY("ruby", ModToolTiers.RUBY, Row.DIAMOND),
    AMETHYST("amethyst", ModToolTiers.AMETHYST, Row.DIAMOND),
    EMERALD("emerald", ModToolTiers.EMERALD, Row.DIAMOND),
    ULTIMATE("ultimate", ModToolTiers.ULTIMATE, Row.NETHERITE),
    CRYSTAL_PINK("crystal_pink", ModToolTiers.CRYSTAL_PINK, Row.IRON),
    TIGERS_EYE("tigers_eye", ModToolTiers.TIGERS_EYE, Row.IRON),
    CRYSTAL_WOOD("crystal_wood", ModToolTiers.CRYSTAL_WOOD, Row.WOODEN),
    CRYSTAL_STONE("crystal_stone", ModToolTiers.CRYSTAL_STONE, Row.STONE);

    /** The spears' attack speed, the mod's own. */
    public static final double ATTACK_SPEED = -2.8;

    private static final Map<SpearTier, Supplier<? extends Item>> REGISTERED = new EnumMap<>(SpearTier.class);

    private final String name;
    private final Tier tier;
    private final Row row;

    SpearTier(String name, Tier tier, Row row) {
        this.name = name;
        this.tier = tier;
        this.row = row;
    }

    /** The item's id, {@code <tier>_spear}. */
    public String id() {
        return name + "_spear";
    }

    public Tier tier() {
        return tier;
    }

    public Row row() {
        return row;
    }

    /** The tier's uses. */
    public int durability() {
        return tier.getUses();
    }

    /** The melee damage the spear adds, the tier's attack bonus + 1; also the charge's base. */
    public double melee() {
        return tier.getAttackDamageBonus() + 1.0;
    }

    /** The spear is registered (Mounts of Mayhem is loaded): its item, for the creative tab. */
    public static synchronized void registered(SpearTier tier, Supplier<? extends Item> item) {
        REGISTERED.put(tier, item);
    }

    /** The spears registered, in the table's order; none without Mounts of Mayhem. */
    public static synchronized List<Item> registeredItems() {
        List<Item> out = new ArrayList<>();
        for (Supplier<? extends Item> item : REGISTERED.values()) {
            out.add(item.get());
        }
        return Collections.unmodifiableList(out);
    }

    /**
     * Mounts of Mayhem's spear rows, by vanilla tier: the charge's speed multiplier, the bonus while riding, the most
     * ticks a charge is held and the ticks to reload after a hit, as the mod gives its own spears.
     */
    public enum Row {
        WOODEN(1.0, 1.0, 150, 40),
        STONE(2.0, 1.1, 130, 40),
        IRON(3.0, 1.25, 85, 40),
        DIAMOND(4.0, 1.4, 50, 45),
        NETHERITE(5.0, 1.5, 40, 50);

        private final double multiplier;
        private final double vehicleBonus;
        private final int holdTicks;
        private final int reloadTicks;

        Row(double multiplier, double vehicleBonus, int holdTicks, int reloadTicks) {
            this.multiplier = multiplier;
            this.vehicleBonus = vehicleBonus;
            this.holdTicks = holdTicks;
            this.reloadTicks = reloadTicks;
        }

        public double multiplier() {
            return multiplier;
        }

        public double vehicleBonus() {
            return vehicleBonus;
        }

        public int holdTicks() {
            return holdTicks;
        }

        public int reloadTicks() {
            return reloadTicks;
        }

        /** The mod's 3D spear model for this row, which OreSpawn's spears of the row take as their in-hand parent. */
        public String model() {
            return "mounts_of_mayhem:custom/" + name().toLowerCase(Locale.ROOT) + "_spear";
        }
    }
}
