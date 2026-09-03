package danger.orespawn.util;

import danger.orespawn.entity.*;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;

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

    public static boolean isAttackableNonMob(Entity entity) {
        return entity instanceof EnderDragon
                || entity instanceof Kraken
                || entity instanceof Godzilla
                || entity instanceof GodzillaHead
                || entity instanceof Basilisk
                || entity instanceof Cephadrome
                || entity instanceof TheKing
                || entity instanceof TheQueen;
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
