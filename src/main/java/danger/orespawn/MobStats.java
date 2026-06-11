package danger.orespawn;

/**
 * Port of the original 1.7.10 mob-stats table.
 *
 * <p>The single source of truth in the original mod is the block of 59
 * {@code get_mobstats(config, mobs, "Name", health, attack, defense)} calls at
 * {@code orig OreSpawnMain.java:6466-6525}. The three ints are the canonical
 * DEFAULT values; every constant below copies them exactly, one constant per
 * original call, each citing its exact original line.</p>
 *
 * <p>{@code armor} is the original {@code defense} — armor points on the same
 * scale as modern {@link net.minecraft.world.entity.ai.attributes.Attributes#ARMOR}.</p>
 *
 * <p><b>Config clamping NOT yet ported.</b> The original
 * {@code get_mobstats} method ({@code orig OreSpawnMain.java:6066-6096}) also
 * read per-mob config overrides and clamped them (health to
 * {@code [health/2, health*2]}, attack to {@code [attack/2, attack*2]},
 * defense to {@code [defense-4, defense+4]} then {@code [0, 22]}). That
 * config layer is deferred to the config findings; only the defaults are
 * ported here.</p>
 *
 * <p><b>Name mappings</b> (original config key → port entity):
 * "Mobzilla" → Godzilla, "Nightmare" → PitchBlack, "Leonopteryx" → Leon,
 * "Jeffery" → GiantRobot, "BandP" → BandP (Burglar &amp; Pickpocket).</p>
 *
 * <p><b>Entity-side overrides in the original</b> (table entry NOT applied
 * verbatim by the entity class):</p>
 * <ul>
 *   <li>{@link #LEON}: orig {@code Leon.java} ignores the table — it hardcodes
 *       HP 250 ({@code Leon.java:169}), attack 55.0 ({@code Leon.java:117})
 *       and armor 16 ({@code Leon.java:192}). Port Leon entities use those
 *       hardcoded values, not this constant.</li>
 *   <li>{@link #CRAB}: orig {@code Crab.java} multiplies by the crab's scale —
 *       and, due to an original bug kept for parity, its max health reads
 *       {@code PitchBlack_stats.health} (250), not {@code Crab_stats.health}
 *       ({@code Crab.java:137}). Attack is {@code Crab_stats.attack × scale}
 *       ({@code Crab.java:71}) and armor is
 *       {@code Crab_stats.defense + 2 × scale} ({@code Crab.java:141}).</li>
 *   <li>{@link #PITCH_BLACK}: orig {@code PitchBlack.java} multiplies by the
 *       nightmare's scale — HP {@code 250 × scale} ({@code PitchBlack.java:239}),
 *       attack {@code 30 × scale} ({@code PitchBlack.java:80}), armor
 *       {@code 10 + 2 × scale} ({@code PitchBlack.java:190}).</li>
 *   <li>{@link #THE_KING} / {@link #THE_QUEEN} / {@link #GODZILLA}: armor
 *       (defense) is phase/situation boosted by the original entity
 *       ({@code TheKing.java:856-864}, {@code TheQueen.java:819-827},
 *       {@code Godzilla.java:145-150}); the table value is the base.</li>
 * </ul>
 */
public record MobStats(
        double maxHealth,
        double attackDamage,
        double armor
) {
    /** orig OreSpawnMain.java:6466 — get_mobstats("Bee", 80, 12, 5) */
    public static final MobStats BEE = new MobStats(80, 12, 5);
    /** orig OreSpawnMain.java:6467 — get_mobstats("Mantis", 120, 16, 10) */
    public static final MobStats MANTIS = new MobStats(120, 16, 10);
    /** orig OreSpawnMain.java:6468 — get_mobstats("HerculesBeetle", 250, 30, 19) */
    public static final MobStats HERCULES_BEETLE = new MobStats(250, 30, 19);
    /** orig OreSpawnMain.java:6469 — get_mobstats("Mothra", 150, 12, 8) */
    public static final MobStats MOTHRA = new MobStats(150, 12, 8);
    /** orig OreSpawnMain.java:6470 — get_mobstats("Brutalfly", 110, 10, 6) */
    public static final MobStats BRUTALFLY = new MobStats(110, 10, 6);
    /** orig OreSpawnMain.java:6471 — get_mobstats("Nastysaurus", 200, 32, 17) */
    public static final MobStats NASTYSAURUS = new MobStats(200, 32, 17);
    /** orig OreSpawnMain.java:6472 — get_mobstats("Pointysaurus", 80, 10, 16) */
    public static final MobStats POINTYSAURUS = new MobStats(80, 10, 16);
    /** orig OreSpawnMain.java:6473 — get_mobstats("Alosaurus", 110, 18, 8) */
    public static final MobStats ALOSAURUS = new MobStats(110, 18, 8);
    /** orig OreSpawnMain.java:6474 — get_mobstats("SpiderRobot", 1500, 100, 16) */
    public static final MobStats SPIDER_ROBOT = new MobStats(1500, 100, 16);
    /** orig OreSpawnMain.java:6475 — get_mobstats("AntRobot", 300, 30, 16) */
    public static final MobStats ANT_ROBOT = new MobStats(300, 30, 16);
    /**
     * orig OreSpawnMain.java:6476 — get_mobstats("Jeffery", 550, 40, 18).
     * Consumed by orig GiantRobot.java (the giant robot IS "Jeffery") —
     * port GiantRobot and its Jeffery skin alias both use this.
     */
    public static final MobStats JEFFERY = new MobStats(550, 40, 18);
    /** orig OreSpawnMain.java:6477 — get_mobstats("Hammerhead", 240, 75, 20) */
    public static final MobStats HAMMERHEAD = new MobStats(240, 75, 20);
    /** orig OreSpawnMain.java:6478 — get_mobstats("Molenoid", 200, 18, 12) */
    public static final MobStats MOLENOID = new MobStats(200, 18, 12);
    /** orig OreSpawnMain.java:6479 — get_mobstats("TRex", 160, 22, 14) */
    public static final MobStats TREX = new MobStats(160, 22, 14);
    /** orig OreSpawnMain.java:6480 — get_mobstats("BandP", 100, 1, 18) (Burglar &amp; Pickpocket) */
    public static final MobStats BANDP = new MobStats(100, 1, 18);
    /** orig OreSpawnMain.java:6481 — get_mobstats("CaterKiller", 450, 32, 19) */
    public static final MobStats CATERKILLER = new MobStats(450, 32, 19);
    /** orig OreSpawnMain.java:6482 — get_mobstats("Cryolophosaurus", 10, 3, 1) */
    public static final MobStats CRYOLOPHOSAURUS = new MobStats(10, 3, 1);
    /** orig OreSpawnMain.java:6483 — get_mobstats("Rat", 5, 3, 1) */
    public static final MobStats RAT = new MobStats(5, 3, 1);
    /** orig OreSpawnMain.java:6484 — get_mobstats("Urchin", 25, 10, 4) */
    public static final MobStats URCHIN = new MobStats(25, 10, 4);
    /** orig OreSpawnMain.java:6485 — get_mobstats("Kyuubi", 125, 10, 10) */
    public static final MobStats KYUUBI = new MobStats(125, 10, 10);
    /** orig OreSpawnMain.java:6486 — get_mobstats("GammaMetroid", 100, 10, 12) */
    public static final MobStats GAMMA_METROID = new MobStats(100, 10, 12);
    /** orig OreSpawnMain.java:6487 — get_mobstats("Basilisk", 200, 24, 15) */
    public static final MobStats BASILISK = new MobStats(200, 24, 15);
    /** orig OreSpawnMain.java:6488 — get_mobstats("EmperorScorpion", 350, 35, 20) */
    public static final MobStats EMPEROR_SCORPION = new MobStats(350, 35, 20);
    /** orig OreSpawnMain.java:6489 — get_mobstats("TrooperBug", 200, 20, 15) */
    public static final MobStats TROOPER_BUG = new MobStats(200, 20, 15);
    /** orig OreSpawnMain.java:6490 — get_mobstats("SpitBug", 100, 10, 12) */
    public static final MobStats SPIT_BUG = new MobStats(100, 10, 12);
    /** orig OreSpawnMain.java:6491 — get_mobstats("Alien", 100, 12, 8) */
    public static final MobStats ALIEN = new MobStats(100, 12, 8);
    /** orig OreSpawnMain.java:6492 — get_mobstats("WaterDragon", 150, 20, 8) */
    public static final MobStats WATER_DRAGON = new MobStats(150, 20, 8);
    /** orig OreSpawnMain.java:6493 — get_mobstats("SeaMonster", 110, 14, 8) */
    public static final MobStats SEA_MONSTER = new MobStats(110, 14, 8);
    /** orig OreSpawnMain.java:6494 — get_mobstats("SeaViper", 160, 22, 12) */
    public static final MobStats SEA_VIPER = new MobStats(160, 22, 12);
    /** orig OreSpawnMain.java:6495 — get_mobstats("Robot2", 200, 22, 18) */
    public static final MobStats ROBOT2 = new MobStats(200, 22, 18);
    /** orig OreSpawnMain.java:6496 — get_mobstats("Robot3", 80, 16, 14) */
    public static final MobStats ROBOT3 = new MobStats(80, 16, 14);
    /** orig OreSpawnMain.java:6497 — get_mobstats("Robot4", 170, 12, 18) */
    public static final MobStats ROBOT4 = new MobStats(170, 12, 18);
    /** orig OreSpawnMain.java:6498 — get_mobstats("Robot5", 20, 5, 6) */
    public static final MobStats ROBOT5 = new MobStats(20, 5, 6);
    /** orig OreSpawnMain.java:6499 — get_mobstats("Rotator", 35, 10, 8) */
    public static final MobStats ROTATOR = new MobStats(35, 10, 8);
    /** orig OreSpawnMain.java:6500 — get_mobstats("Vortex", 150, 26, 10) */
    public static final MobStats VORTEX = new MobStats(150, 26, 10);
    /** orig OreSpawnMain.java:6501 — get_mobstats("DungeonBeast", 65, 12, 6) */
    public static final MobStats DUNGEON_BEAST = new MobStats(65, 12, 6);
    /** orig OreSpawnMain.java:6502 — get_mobstats("Triffid", 100, 20, 12) */
    public static final MobStats TRIFFID = new MobStats(100, 20, 12);
    /** orig OreSpawnMain.java:6503 — get_mobstats("LurkingTerror", 30, 6, 5) */
    public static final MobStats LURKING_TERROR = new MobStats(30, 6, 5);
    /** orig OreSpawnMain.java:6504 — get_mobstats("WormSmall", 10, 3, 0) */
    public static final MobStats WORM_SMALL = new MobStats(10, 3, 0);
    /** orig OreSpawnMain.java:6505 — get_mobstats("WormMedium", 30, 10, 8) */
    public static final MobStats WORM_MEDIUM = new MobStats(30, 10, 8);
    /** orig OreSpawnMain.java:6506 — get_mobstats("WormLarge", 90, 18, 14) */
    public static final MobStats WORM_LARGE = new MobStats(90, 18, 14);
    /** orig OreSpawnMain.java:6507 — get_mobstats("EnderKnight", 60, 12, 6) */
    public static final MobStats ENDER_KNIGHT = new MobStats(60, 12, 6);
    /** orig OreSpawnMain.java:6508 — get_mobstats("EnderReaper", 90, 18, 8) */
    public static final MobStats ENDER_REAPER = new MobStats(90, 18, 8);
    /** orig OreSpawnMain.java:6509 — get_mobstats("Irukandji", 1, 20, 0) */
    public static final MobStats IRUKANDJI = new MobStats(1, 20, 0);
    /** orig OreSpawnMain.java:6510 — get_mobstats("AttackSquid", 10, 8, 0) */
    public static final MobStats ATTACK_SQUID = new MobStats(10, 8, 0);
    /** orig OreSpawnMain.java:6511 — get_mobstats("CaveFisher", 10, 4, 4) */
    public static final MobStats CAVE_FISHER = new MobStats(10, 4, 4);
    /** orig OreSpawnMain.java:6512 — get_mobstats("CloudShark", 15, 6, 5) */
    public static final MobStats CLOUD_SHARK = new MobStats(15, 6, 5);
    /** orig OreSpawnMain.java:6513 — get_mobstats("CreepingHorror", 10, 3, 2) */
    public static final MobStats CREEPING_HORROR = new MobStats(10, 3, 2);
    /** orig OreSpawnMain.java:6514 — get_mobstats("Mobzilla", 4000, 175, 21) (port name: Godzilla) */
    public static final MobStats GODZILLA = new MobStats(4000, 175, 21);
    /** orig OreSpawnMain.java:6515 — get_mobstats("Kraken", 1000, 40, 10) */
    public static final MobStats KRAKEN = new MobStats(1000, 40, 10);
    /** orig OreSpawnMain.java:6516 — get_mobstats("LeafMonster", 6, 2, 1) */
    public static final MobStats LEAF_MONSTER = new MobStats(6, 2, 1);
    /**
     * orig OreSpawnMain.java:6517 — get_mobstats("Nightmare", 250, 30, 10)
     * (port name: PitchBlack). Entity multiplies by its scale — see class javadoc.
     */
    public static final MobStats PITCH_BLACK = new MobStats(250, 30, 10);
    /** orig OreSpawnMain.java:6518 — get_mobstats("Scorpion", 15, 4, 10) */
    public static final MobStats SCORPION = new MobStats(15, 4, 10);
    /** orig OreSpawnMain.java:6519 — get_mobstats("Skate", 8, 8, 4) */
    public static final MobStats SKATE = new MobStats(8, 8, 4);
    /** orig OreSpawnMain.java:6520 — get_mobstats("TerribleTerror", 10, 5, 3) */
    public static final MobStats TERRIBLE_TERROR = new MobStats(10, 5, 3);
    /** orig OreSpawnMain.java:6521 — get_mobstats("TheKing", 7000, 350, 21) */
    public static final MobStats THE_KING = new MobStats(7000, 350, 21);
    /** orig OreSpawnMain.java:6522 — get_mobstats("TheQueen", 6000, 225, 21) */
    public static final MobStats THE_QUEEN = new MobStats(6000, 225, 21);
    /**
     * orig OreSpawnMain.java:6523 — get_mobstats("Leonopteryx", 150, 20, 8)
     * (port name: Leon). NOTE: the original entity OVERRIDES this table entry —
     * orig Leon.java hardcodes HP 250 / ATK 55 / armor 16 (see class javadoc).
     * Kept for completeness; port Leon entities cite the entity-file values.
     */
    public static final MobStats LEON = new MobStats(150, 20, 8);
    /**
     * orig OreSpawnMain.java:6524 — get_mobstats("Crab", 180, 24, 16).
     * NOTE: the original entity uses PitchBlack's health (250) instead of this
     * 180 — original bug kept for parity (see class javadoc).
     */
    public static final MobStats CRAB = new MobStats(180, 24, 16);
}
