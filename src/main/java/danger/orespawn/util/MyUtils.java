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

    public static boolean isIgnoreable(Entity entity) {
        return entity instanceof Ghost
                || entity instanceof GhostSkelly
                || entity instanceof CaveFisher
                || entity instanceof RockBase
                || entity instanceof Fairy
                || entity instanceof EntityButterfly
                || entity instanceof EntityMosquito
                || entity instanceof Firefly
                || entity instanceof EntityLunaMoth
                || entity instanceof Coin;
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
