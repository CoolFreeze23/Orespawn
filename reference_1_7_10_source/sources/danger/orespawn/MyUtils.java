/*
 * Decompiled with CFR 0.152.
 */
package danger.orespawn;

import danger.orespawn.Boyfriend;
import danger.orespawn.Cephadrome;
import danger.orespawn.Cockateil;
import danger.orespawn.Cricket;
import danger.orespawn.Dragon;
import danger.orespawn.Dragonfly;
import danger.orespawn.Elevator;
import danger.orespawn.EntityAnt;
import danger.orespawn.EntityButterfly;
import danger.orespawn.EntityMosquito;
import danger.orespawn.Firefly;
import danger.orespawn.GammaMetroid;
import danger.orespawn.Ghost;
import danger.orespawn.GhostSkelly;
import danger.orespawn.Girlfriend;
import danger.orespawn.KingHead;
import danger.orespawn.Leon;
import danger.orespawn.Mothra;
import danger.orespawn.PurplePower;
import danger.orespawn.QueenHead;
import danger.orespawn.RockBase;
import danger.orespawn.Spyro;
import danger.orespawn.Stinky;
import danger.orespawn.Termite;
import danger.orespawn.TheKing;
import danger.orespawn.ThePrince;
import danger.orespawn.ThePrinceAdult;
import danger.orespawn.ThePrinceTeen;
import danger.orespawn.ThePrincess;
import danger.orespawn.TheQueen;
import danger.orespawn.WaterDragon;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.passive.EntityVillager;

/*
 * Exception performing whole class analysis ignored.
 */
public class MyUtils {
    public static boolean isRoyalty(Entity e) {
        if (!(e instanceof EntityLivingBase)) {
            return false;
        }
        if (e instanceof ThePrince) {
            return true;
        }
        if (e instanceof ThePrinceTeen) {
            return true;
        }
        if (e instanceof ThePrinceAdult) {
            return true;
        }
        if (e instanceof ThePrincess) {
            return true;
        }
        if (e instanceof TheKing) {
            return true;
        }
        if (e instanceof KingHead) {
            return true;
        }
        if (e instanceof TheQueen) {
            return true;
        }
        if (e instanceof QueenHead) {
            return true;
        }
        return e instanceof PurplePower;
    }

    public static boolean isAttackableNonMob(EntityLivingBase par1EntityLiving) {
        if (par1EntityLiving instanceof EntityMob) {
            return true;
        }
        if (par1EntityLiving instanceof Mothra) {
            return true;
        }
        if (par1EntityLiving instanceof Leon) {
            return true;
        }
        if (par1EntityLiving instanceof Dragon) {
            return true;
        }
        if (par1EntityLiving instanceof Spyro) {
            return true;
        }
        if (MyUtils.isRoyalty((Entity)par1EntityLiving)) {
            return true;
        }
        if (par1EntityLiving instanceof GammaMetroid) {
            return true;
        }
        if (par1EntityLiving instanceof Cephadrome) {
            return true;
        }
        if (par1EntityLiving instanceof WaterDragon) {
            return true;
        }
        if (par1EntityLiving instanceof Girlfriend) {
            return true;
        }
        if (par1EntityLiving instanceof Boyfriend) {
            return true;
        }
        if (par1EntityLiving instanceof EntityVillager) {
            return true;
        }
        return par1EntityLiving instanceof Stinky;
    }

    public static boolean isIgnoreable(EntityLivingBase par1EntityLiving) {
        if (par1EntityLiving instanceof RockBase) {
            return true;
        }
        if (par1EntityLiving instanceof EntityAnt) {
            return true;
        }
        if (par1EntityLiving instanceof EntityButterfly) {
            return true;
        }
        if (par1EntityLiving instanceof EntityMosquito) {
            return true;
        }
        if (par1EntityLiving instanceof Dragonfly) {
            return true;
        }
        if (par1EntityLiving instanceof Firefly) {
            return true;
        }
        if (par1EntityLiving instanceof Cricket) {
            return true;
        }
        if (par1EntityLiving instanceof Cockateil) {
            return true;
        }
        if (par1EntityLiving instanceof Termite) {
            return true;
        }
        if (par1EntityLiving instanceof Ghost) {
            return true;
        }
        if (par1EntityLiving instanceof GhostSkelly) {
            return true;
        }
        return par1EntityLiving instanceof Elevator;
    }
}

