package danger.orespawn;

public record ArmorStats(
        int durability,
        int headProtection,
        int chestProtection,
        int legProtection,
        int bootProtection,
        int enchantability,
        int fireProtection,
        int blastProtection,
        int projectileProtection,
        int thorns,
        int featherFalling,
        int respiration,
        int aquaAffinity,
        int unbreaking
) {
    public static final ArmorStats AMETHYST = new ArmorStats(100, 4, 8, 7, 3, 40, 0, 0, 0, 0, 0, 0, 0, 0);
    public static final ArmorStats EMERALD = new ArmorStats(60, 3, 8, 6, 3, 40, 0, 0, 0, 0, 0, 0, 0, 0);
    public static final ArmorStats EXPERIENCE = new ArmorStats(70, 5, 9, 7, 4, 50, 0, 0, 2, 0, 1, 0, 0, 1);
    public static final ArmorStats MOTH_SCALE = new ArmorStats(50, 2, 7, 5, 2, 50, 0, 0, 3, 3, 3, 0, 0, 5);
    public static final ArmorStats LAVA_EEL = new ArmorStats(40, 2, 7, 5, 2, 35, 1, 2, 3, 2, 10, 0, 0, 2);
    public static final ArmorStats ULTIMATE = new ArmorStats(200, 6, 12, 10, 6, 100, 2, 3, 5, 5, 5, 5, 0, 3);
    public static final ArmorStats PINK = new ArmorStats(50, 3, 7, 5, 2, 40, 0, 0, 0, 0, 0, 0, 0, 0);
    public static final ArmorStats TIGERS_EYE = new ArmorStats(80, 4, 8, 7, 4, 55, 0, 0, 0, 0, 0, 0, 0, 0);
    public static final ArmorStats PEACOCK = new ArmorStats(40, 2, 5, 4, 2, 30, 0, 0, 0, 0, 0, 0, 0, 10);
    public static final ArmorStats MOBZILLA = new ArmorStats(1000, 7, 13, 11, 7, 150, 0, 0, 10, 10, 10, 10, 5, 10);
    public static final ArmorStats RUBY = new ArmorStats(90, 4, 9, 8, 4, 40, 0, 0, 0, 0, 0, 0, 0, 0);
    public static final ArmorStats ROYAL = new ArmorStats(2000, 8, 14, 12, 8, 200, 1, 2, 10, 10, 10, 10, 5, 10);
    public static final ArmorStats LAPIS = new ArmorStats(60, 2, 7, 5, 2, 60, 1, 1, 1, 0, 0, 1, 0, 0);
    public static final ArmorStats QUEEN = new ArmorStats(1500, 9, 16, 14, 9, 150, 0, 0, 0, 0, 0, 0, 0, 0);
}
