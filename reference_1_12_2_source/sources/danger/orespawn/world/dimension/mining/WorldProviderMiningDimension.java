/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.DimensionType
 *  net.minecraft.world.WorldProvider
 *  net.minecraft.world.WorldServer
 *  net.minecraft.world.biome.BiomeProvider
 *  net.minecraft.world.biome.BiomeProviderSingle
 *  net.minecraft.world.gen.IChunkGenerator
 *  net.minecraft.world.storage.WorldInfo
 *  net.minecraftforge.common.DimensionManager
 */
package danger.orespawn.world.dimension.mining;

import danger.orespawn.init.ModBiomes;
import danger.orespawn.init.ModDimensions;
import danger.orespawn.util.Reference;
import danger.orespawn.world.dimension.mining.ChunkGeneratorMiningDimension;
import net.minecraft.world.DimensionType;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.WorldServer;
import net.minecraft.world.biome.BiomeProvider;
import net.minecraft.world.biome.BiomeProviderSingle;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraft.world.storage.WorldInfo;
import net.minecraftforge.common.DimensionManager;

public class WorldProviderMiningDimension
extends WorldProvider {
    public WorldProviderMiningDimension() {
        this.field_76578_c = new BiomeProviderSingle(ModBiomes.MINING_BIOME);
    }

    public DimensionType func_186058_p() {
        return ModDimensions.MINING;
    }

    public boolean func_76567_e() {
        return true;
    }

    public IChunkGenerator func_186060_c() {
        return new ChunkGeneratorMiningDimension(this.field_76579_a, this.getSeed(), (BiomeProvider)new BiomeProviderSingle(ModBiomes.MINING_BIOME));
    }

    public boolean func_76569_d() {
        return true;
    }

    public void setWorldTime(long time) {
        WorldInfo wi;
        WorldServer ws = DimensionManager.getWorld((int)Reference.DimensionMiningID);
        if (ws != null && (wi = ws.func_72912_H()) != null && time % 24000L > 12000L && ws.func_73056_e()) {
            long newTime = time + 24000L;
            newTime -= newTime % 24000L;
            for (int i = 0; i < ws.func_73046_m().field_71305_c.length; ++i) {
                ws.func_73046_m().field_71305_c[i].func_72877_b(newTime);
            }
            return;
        }
        super.setWorldTime(time);
    }
}

