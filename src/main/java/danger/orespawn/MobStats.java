package danger.orespawn;

public record MobStats(
        double maxHealth,
        double attackDamage,
        double movementSpeed,
        double followRange
) {
    public static final MobStats BEE = new MobStats(10, 2, 0.3, 16);
    public static final MobStats MANTIS = new MobStats(30, 6, 0.3, 24);
    public static final MobStats HERCULES_BEETLE = new MobStats(40, 8, 0.3, 24);
    public static final MobStats MOTHRA = new MobStats(200, 15, 0.4, 48);
    public static final MobStats BRUTALFLY = new MobStats(60, 10, 0.35, 32);
    public static final MobStats NASTYSAURUS = new MobStats(50, 8, 0.3, 24);
    public static final MobStats POINTYSAURUS = new MobStats(50, 8, 0.3, 24);
    public static final MobStats ALOSAURUS = new MobStats(80, 12, 0.35, 32);
    public static final MobStats SPIDER_ROBOT = new MobStats(100, 14, 0.3, 32);
    public static final MobStats ANT_ROBOT = new MobStats(80, 12, 0.3, 32);
    public static final MobStats JEFFERY = new MobStats(30, 4, 0.3, 24);
    public static final MobStats HAMMERHEAD = new MobStats(40, 6, 0.35, 32);
    public static final MobStats LEON = new MobStats(80, 12, 0.35, 32);
    public static final MobStats CATERKILLER = new MobStats(100, 15, 0.3, 32);
    public static final MobStats MOLENOID = new MobStats(60, 8, 0.3, 24);
    public static final MobStats TREX = new MobStats(150, 20, 0.35, 48);
    public static final MobStats BANDP = new MobStats(30, 4, 0.3, 24);
    public static final MobStats CRYOLOPHOSAURUS = new MobStats(70, 10, 0.3, 32);
    public static final MobStats RAT = new MobStats(15, 3, 0.35, 16);
    public static final MobStats URCHIN = new MobStats(20, 4, 0.3, 16);
    public static final MobStats KYUUBI = new MobStats(120, 16, 0.4, 48);
    public static final MobStats GAMMA_METROID = new MobStats(80, 12, 0.35, 32);
    public static final MobStats BASILISK = new MobStats(200, 25, 0.4, 48);
    public static final MobStats EMPEROR_SCORPION = new MobStats(150, 20, 0.35, 48);
    public static final MobStats TROOPER_BUG = new MobStats(40, 6, 0.3, 24);
    public static final MobStats SPIT_BUG = new MobStats(30, 4, 0.3, 24);
    public static final MobStats ALIEN = new MobStats(100, 14, 0.35, 32);
    public static final MobStats WATER_DRAGON = new MobStats(200, 20, 0.35, 48);
    public static final MobStats SEA_MONSTER = new MobStats(120, 16, 0.3, 48);
    public static final MobStats SEA_VIPER = new MobStats(80, 12, 0.35, 32);
    public static final MobStats ROBOT2 = new MobStats(60, 8, 0.3, 24);
    public static final MobStats ROBOT3 = new MobStats(80, 10, 0.3, 24);
    public static final MobStats ROBOT4 = new MobStats(100, 12, 0.3, 24);
    public static final MobStats ROBOT5 = new MobStats(120, 14, 0.3, 24);
    public static final MobStats ROTATOR = new MobStats(80, 10, 0.35, 32);
    public static final MobStats VORTEX = new MobStats(100, 14, 0.35, 32);
    public static final MobStats DUNGEON_BEAST = new MobStats(200, 25, 0.35, 48);
    public static final MobStats TRIFFID = new MobStats(60, 8, 0.3, 24);
    public static final MobStats LURKING_TERROR = new MobStats(150, 20, 0.3, 48);
    public static final MobStats WORM_SMALL = new MobStats(40, 6, 0.25, 16);
    public static final MobStats WORM_MEDIUM = new MobStats(80, 12, 0.25, 24);
    public static final MobStats WORM_LARGE = new MobStats(160, 20, 0.25, 32);
    public static final MobStats ENDER_KNIGHT = new MobStats(80, 12, 0.35, 32);
    public static final MobStats ENDER_REAPER = new MobStats(120, 16, 0.35, 32);
    public static final MobStats IRUKANDJI = new MobStats(10, 2, 0.3, 16);
    public static final MobStats ATTACK_SQUID = new MobStats(40, 6, 0.3, 24);
    public static final MobStats CAVE_FISHER = new MobStats(40, 6, 0.3, 24);
    public static final MobStats CLOUD_SHARK = new MobStats(60, 8, 0.35, 32);
    public static final MobStats CREEPING_HORROR = new MobStats(100, 14, 0.3, 32);
    public static final MobStats GODZILLA = new MobStats(4000, 100, 0.35, 64);
    public static final MobStats KRAKEN = new MobStats(500, 30, 0.3, 64);
    public static final MobStats LEAF_MONSTER = new MobStats(30, 4, 0.3, 16);
    public static final MobStats PITCH_BLACK = new MobStats(200, 25, 0.35, 48);
    public static final MobStats CRAB = new MobStats(20, 3, 0.3, 16);
    public static final MobStats SCORPION = new MobStats(30, 5, 0.3, 24);
    public static final MobStats SKATE = new MobStats(15, 2, 0.3, 16);
    public static final MobStats TERRIBLE_TERROR = new MobStats(40, 6, 0.35, 24);
    public static final MobStats THE_KING = new MobStats(7000, 150, 0.4, 64);
    public static final MobStats THE_QUEEN = new MobStats(6000, 125, 0.4, 64);
}
