package danger.orespawn.util;

import danger.orespawn.entity.*;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.npc.Villager;

public class MyUtils {

    public static boolean isRoyalty(Entity entity) {
        return entity instanceof TheKing
                || entity instanceof TheQueen
                || entity instanceof KingHead
                || entity instanceof QueenHead
                || entity instanceof ThePrince
                || entity instanceof ThePrinceAdult
                || entity instanceof ThePrincess
                || entity instanceof ThePrinceTeen
                || entity instanceof PurplePower;
    }

    /**
     * orig MyUtils.java:117-152 — the shared ignore list every hunter's
     * {@code isSuitableTarget} screens with (Alosaurus, Basilisk, Brutalfly,
     * Rotator, Scorpion, Vortex, Mothra, SpiderRobot, TheKing, TheQueen and,
     * since ENT-S-100, the Kraken), in the original's order: RockBase (:118),
     * EntityAnt (:121), EntityButterfly (:124), EntityMosquito (:127),
     * Dragonfly (:130), Firefly (:133), Cricket (:136), Cockateil (:139),
     * Termite (:142), Ghost (:145), GhostSkelly (:148), Elevator (:151).
     * Subclasses ride along exactly as they did in 1.7.10 (same hierarchy in
     * both trees): the Red / Rainbow / Unstable ants and the Termite under
     * EntityAnt, the LunaMoth and Mothra under EntityButterfly.
     *
     * <p>ENT-S-101: the port list had dropped EntityAnt, Dragonfly, Cricket,
     * Cockateil, Termite and Elevator and added CaveFisher, Fairy, LunaMoth
     * and Coin without a record; membership restored to the original's. The
     * LunaMoth entry was redundant in both directions — it stays ignoreable
     * through EntityButterfly, as in 1.7.10.</p>
     */
    public static boolean isIgnoreable(Entity entity) {
        return entity instanceof RockBase            // orig :118
                || entity instanceof EntityAnt       // orig :121
                || entity instanceof EntityButterfly // orig :124
                || entity instanceof EntityMosquito  // orig :127
                || entity instanceof EntityDragonfly // orig :130 Dragonfly
                || entity instanceof Firefly         // orig :133
                || entity instanceof EntityCricket   // orig :136 Cricket
                || entity instanceof Cockateil       // orig :139
                || entity instanceof EntityTermite   // orig :142 Termite
                || entity instanceof Ghost           // orig :145
                || entity instanceof GhostSkelly     // orig :148
                || entity instanceof Elevator;       // orig :151
    }

    /**
     * orig MyUtils.java:77-115 — the shared "attackable non-mob" grant list the general hunters fall
     * through to after their own chains (Crab :417, Mantis :391, Molenoid :274, TheKing :981, TheQueen
     * :929, WaterDragon :679; Hammerhead, SeaMonster, SeaViper and CaterKiller when their scans return —
     * ledger T3a / T3b), in the original's order: EntityMob (:78), Mothra (:81), Leon (:84), Dragon (:87),
     * Spyro (:90), the royalty (:93 — {@link #isRoyalty}, orig :46-75: ThePrince, ThePrinceTeen,
     * ThePrinceAdult, ThePrincess, TheKing, KingHead, TheQueen, QueenHead, PurplePower), GammaMetroid
     * (:96), Cephadrome (:99), WaterDragon (:102), Girlfriend (:105), Boyfriend (:108), EntityVillager
     * (:111), Stinky (:114). The port classes: EntityMob → {@link Monster}, Leon → EntityLeon, Spyro →
     * EntitySpyro, GammaMetroid → EntityGammaMetroid, EntityVillager → {@link Villager}, Stinky →
     * EntityStinky; Mothra is an EntityButterfly here (an IMob EntityButterfly in 1.7.10, orig
     * Mothra.java:52) and {@code instanceof Mothra} names her either way.
     *
     * <p>ENT-S-128: the port list had granted EnderDragon, Kraken, Godzilla, GodzillaHead, Basilisk,
     * Cephadrome, TheKing and TheQueen. The Kraken, Godzilla, Basilisk, King and Queen were EntityMob in
     * 1.7.10 and are Monsters here — still granted, through the {@code Monster} term; the GodzillaHead (an
     * EntityLiving outside orig's list) and the EnderDragon (no orig counterpart in this helper) were
     * port-only grants, removed; the eleven dropped members are restored. ENT-S-110's inline copy in
     * EntityLeon carries the same membership.</p>
     */
    public static boolean isAttackableNonMob(Entity entity) {
        return entity instanceof Monster                 // orig :78 EntityMob
                || entity instanceof Mothra              // orig :81
                || entity instanceof EntityLeon          // orig :84 Leon
                || entity instanceof Dragon              // orig :87
                || entity instanceof EntitySpyro         // orig :90 Spyro
                || isRoyalty(entity)                     // orig :93
                || entity instanceof EntityGammaMetroid  // orig :96 GammaMetroid
                || entity instanceof Cephadrome          // orig :99
                || entity instanceof WaterDragon         // orig :102
                || entity instanceof Girlfriend          // orig :105
                || entity instanceof Boyfriend           // orig :108
                || entity instanceof Villager            // orig :111 EntityVillager
                || entity instanceof EntityStinky;       // orig :114 Stinky
    }

    public static boolean isAlly(Entity entity) {
        return entity instanceof EntityLurkingTerror
                || entity instanceof EnderReaper
                || entity instanceof EntityTerribleTerror
                || entity instanceof EntityLeafMonster
                || entity instanceof CreepingHorror
                || entity instanceof EntityTriffid
                || entity instanceof EntitySpyro;
    }

    public static boolean isBigBoss(Entity entity) {
        return entity instanceof Godzilla
                || entity instanceof GodzillaHead
                || entity instanceof PitchBlack
                || entity instanceof Kraken;
    }
}
