package danger.orespawn;

public record OreStats(
        int veinSize,
        int veinsPerChunk,
        int minHeight,
        int maxHeight
) {
    public static final OreStats RUBY = new OreStats(10, 1, 0, 50);
    public static final OreStats BLOCK_RUBY = new OreStats(1, 2, 0, 15);
    public static final OreStats URANIUM = new OreStats(3, 4, 0, 30);
    public static final OreStats TITANIUM = new OreStats(3, 4, 0, 20);
    public static final OreStats AMETHYST = new OreStats(2, 6, 0, 25);
    public static final OreStats SALT = new OreStats(5, 12, 50, 128);
    public static final OreStats SPAWN_ORES = new OreStats(28, 4, 50, 128);
    public static final OreStats DIAMOND = new OreStats(4, 6, 0, 30);
    public static final OreStats BLOCK_DIAMOND = new OreStats(2, 4, 0, 20);
    public static final OreStats EMERALD_ORE = new OreStats(4, 6, 0, 40);
    public static final OreStats BLOCK_EMERALD = new OreStats(2, 4, 0, 20);
    public static final OreStats GOLD = new OreStats(4, 8, 0, 40);
    public static final OreStats BLOCK_GOLD = new OreStats(2, 4, 0, 25);
}
