/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.entity.player.EntityPlayer
 */
package danger.orespawn.util.premium;

import net.minecraft.entity.player.EntityPlayer;

public class PremiumChecker {
    static String urlString = "https://raw.githubusercontent.com/Sevenblade11/Test/main/something.dat";
    private static String[] viableUUIDs;
    private static final boolean DEV_USER = false;
    private static final boolean PREMIUM_BUILD = true;

    public static boolean CheckUser(EntityPlayer player) {
        return true;
    }

    public static void Init() {
    }
}

