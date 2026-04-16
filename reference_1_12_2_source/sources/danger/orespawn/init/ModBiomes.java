/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.biome.Biome
 *  net.minecraftforge.common.BiomeDictionary
 *  net.minecraftforge.common.BiomeDictionary$Type
 *  net.minecraftforge.common.BiomeManager
 *  net.minecraftforge.common.BiomeManager$BiomeType
 *  net.minecraftforge.fml.common.registry.ForgeRegistries
 *  net.minecraftforge.registries.IForgeRegistryEntry
 */
package danger.orespawn.init;

import danger.orespawn.world.biome.BiomeMiningDimension;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.BiomeManager;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistryEntry;

public class ModBiomes {
    public static final Biome MINING_BIOME = new BiomeMiningDimension();

    public static void registerBiomes() {
        ModBiomes.initBiome(MINING_BIOME, "Mining Biome", BiomeManager.BiomeType.WARM, BiomeDictionary.Type.HILLS);
    }

    private static Biome initBiome(Biome biome, String name, BiomeManager.BiomeType biomeType, BiomeDictionary.Type ... types) {
        biome.setRegistryName(name);
        ForgeRegistries.BIOMES.register((IForgeRegistryEntry)biome);
        System.out.println("Registered " + name + " biome");
        BiomeDictionary.addTypes((Biome)biome, (BiomeDictionary.Type[])types);
        BiomeManager.addSpawnBiome((Biome)biome);
        return biome;
    }
}

