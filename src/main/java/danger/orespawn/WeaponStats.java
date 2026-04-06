package danger.orespawn;

public record WeaponStats(
        int harvestLevel,
        int maxUses,
        float efficiency,
        float damage,
        int enchantability
) {
    public static final WeaponStats ULTIMATE = new WeaponStats(10, 3000, 15, 36, 100);
    public static final WeaponStats NIGHTMARE = new WeaponStats(3, 1800, 12, 26, 60);
    public static final WeaponStats BERTHA = new WeaponStats(3, 9000, 15, 496, 100);
    public static final WeaponStats CRYSTAL_WOOD = new WeaponStats(2, 300, 3, 2, 15);
    public static final WeaponStats CRYSTAL_STONE = new WeaponStats(3, 800, 6, 5, 45);
    public static final WeaponStats CRYSTAL_PINK = new WeaponStats(4, 1100, 10, 7, 65);
    public static final WeaponStats TIGERS_EYE = new WeaponStats(4, 1600, 12, 8, 75);
    public static final WeaponStats RUBY = new WeaponStats(5, 1500, 11, 16, 85);
    public static final WeaponStats AMETHYST = new WeaponStats(4, 2000, 11, 11, 70);
    public static final WeaponStats EMERALD = new WeaponStats(3, 1300, 10, 6, 75);
    public static final WeaponStats ROYAL = new WeaponStats(3, 10000, 15, 746, 150);
    public static final WeaponStats ATTITUDE = new WeaponStats(5, 2000, 15, 82, 100);
    public static final WeaponStats BATTLE_AXE = new WeaponStats(3, 1500, 15, 46, 75);
    public static final WeaponStats CHAINSAW = new WeaponStats(3, 1500, 10, 56, 75);
    public static final WeaponStats QUEEN_BATTLE_AXE = new WeaponStats(3, 2200, 15, 662, 100);
}
