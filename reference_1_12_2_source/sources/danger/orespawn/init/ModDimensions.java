/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.DimensionType
 *  net.minecraftforge.common.DimensionManager
 */
package danger.orespawn.init;

import danger.orespawn.util.Reference;
import danger.orespawn.world.dimension.mining.WorldProviderMiningDimension;
import net.minecraft.world.DimensionType;
import net.minecraftforge.common.DimensionManager;

public class ModDimensions {
    public static final DimensionType MINING = DimensionType.register((String)"Mining Dimension", (String)"_mining", (int)Reference.DimensionMiningID, WorldProviderMiningDimension.class, (boolean)false);

    public static void registerDimensions() {
        DimensionManager.registerDimension((int)Reference.DimensionMiningID, (DimensionType)MINING);
    }
}

