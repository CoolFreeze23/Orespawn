/*
 * Decompiled with CFR 0.152.
 */
package danger.orespawn;

import danger.orespawn.BiomeGenUtopianPlains;
import danger.orespawn.ChunkProviderOreSpawn6;
import danger.orespawn.OreSpawnMain;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.WorldServer;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.biome.WorldChunkManagerHell;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.storage.WorldInfo;
import net.minecraftforge.common.DimensionManager;

public class WorldProviderOreSpawn6
extends WorldProvider {
    private BiomeGenUtopianPlains MyPlains = (BiomeGenUtopianPlains)new BiomeGenUtopianPlains(OreSpawnMain.BiomeChaosID).func_76739_b(353825).func_76735_a("Chaos").func_76732_a(0.7f, 0.5f);

    public String func_80007_l() {
        return "Dimension-Chaos";
    }

    public boolean func_76567_e() {
        return true;
    }

    public void func_76572_b() {
        this.MyPlains.setChaosCreatures();
        this.field_76578_c = new WorldChunkManagerHell((BiomeGenBase)this.MyPlains, 0.01f);
        this.field_76578_c.func_76935_a(0, 0).func_76732_a(0.8f, 0.01f);
        this.field_76574_g = OreSpawnMain.DimensionID4;
        this.field_76574_g = OreSpawnMain.DimensionID6;
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
        return new ChunkProviderOreSpawn6(this.field_76579_a, this.field_76579_a.func_72905_C());
    }
}

