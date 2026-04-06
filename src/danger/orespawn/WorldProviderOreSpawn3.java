/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.server.MinecraftServer
 *  net.minecraft.world.WorldProvider
 *  net.minecraft.world.WorldServer
 *  net.minecraft.world.biome.BiomeGenBase
 *  net.minecraft.world.biome.WorldChunkManagerHell
 *  net.minecraft.world.chunk.IChunkProvider
 *  net.minecraft.world.storage.WorldInfo
 *  net.minecraftforge.common.BiomeManager
 *  net.minecraftforge.common.DimensionManager
 */
package danger.orespawn;

import danger.orespawn.BiomeGenUtopianPlains;
import danger.orespawn.ChunkProviderOreSpawn3;
import danger.orespawn.OreSpawnMain;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.WorldServer;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.biome.WorldChunkManagerHell;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.storage.WorldInfo;
import net.minecraftforge.common.BiomeManager;
import net.minecraftforge.common.DimensionManager;

public class WorldProviderOreSpawn3
extends WorldProvider {
    private BiomeGenUtopianPlains MyPlains = (BiomeGenUtopianPlains)new BiomeGenUtopianPlains(OreSpawnMain.BiomeVillageID).func_76739_b(353825).func_76735_a("Villages").func_76732_a(0.7f, 0.5f);

    public String func_80007_l() {
        return "Dimension-VillageMania";
    }

    public boolean func_76567_e() {
        return true;
    }

    public void func_76572_b() {
        this.MyPlains.setVillageCreatures();
        this.field_76578_c = new WorldChunkManagerHell((BiomeGenBase)this.MyPlains, 0.5f);
        this.field_76578_c.func_76935_a(0, 0).func_76732_a(0.7f, 0.5f);
        this.field_76574_g = OreSpawnMain.DimensionID3;
        BiomeManager.addVillageBiome((BiomeGenBase)this.MyPlains, (boolean)true);
    }

    public void setWorldTime(long time) {
        WorldServer ws = DimensionManager.getWorld((int)this.field_76574_g);
        if (ws != null) {
            WorldInfo w = ws.func_72912_H();
            if (w != null) {
                if (time % 24000L > 12000L && ws.func_73056_e()) {
                    long i = time + 24000L;
                    i -= i % 24000L;
                    for (int j = 0; j < MinecraftServer.func_71276_C().field_71305_c.length; ++j) {
                        MinecraftServer.func_71276_C().field_71305_c[j].func_72877_b(i);
                    }
                } else {
                    super.setWorldTime(time);
                }
            } else {
                super.setWorldTime(time);
            }
        } else {
            super.setWorldTime(time);
        }
    }

    public IChunkProvider func_76555_c() {
        return new ChunkProviderOreSpawn3(this.field_76579_a, this.field_76579_a.func_72905_C(), true);
    }
}

