/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.entity.EnumCreatureType
 *  net.minecraft.init.Biomes
 *  net.minecraft.world.biome.Biome
 *  net.minecraftforge.fml.common.registry.EntityRegistry
 *  net.minecraftforge.fml.common.registry.ForgeRegistries
 */
package danger.orespawn.init;

import danger.orespawn.entity.Beaver;
import danger.orespawn.entity.Bird;
import danger.orespawn.entity.Brutalfly;
import danger.orespawn.entity.Butterfly;
import danger.orespawn.entity.Cassowary;
import danger.orespawn.entity.CaveFisher;
import danger.orespawn.entity.Dragonfly;
import danger.orespawn.entity.Firefly;
import danger.orespawn.entity.Kyuubi;
import danger.orespawn.entity.Mantis;
import danger.orespawn.entity.Mosquito;
import danger.orespawn.entity.Mothra;
import danger.orespawn.entity.RedCow;
import danger.orespawn.entity.StinkBug;
import java.util.Collection;
import java.util.LinkedList;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.init.Biomes;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

public class EntitySpawns {
    public static void addSpawns() {
        Biome[] biomes = EntitySpawns.excludeEndNether();
        EntityRegistry.addSpawn(Bird.class, (int)30, (int)1, (int)3, (EnumCreatureType)EnumCreatureType.CREATURE, (Biome[])biomes);
        EntityRegistry.addSpawn(Butterfly.class, (int)15, (int)1, (int)1, (EnumCreatureType)EnumCreatureType.CREATURE, (Biome[])biomes);
        EntityRegistry.addSpawn(Firefly.class, (int)15, (int)1, (int)1, (EnumCreatureType)EnumCreatureType.CREATURE, (Biome[])biomes);
        EntityRegistry.addSpawn(CaveFisher.class, (int)15, (int)1, (int)1, (EnumCreatureType)EnumCreatureType.MONSTER, (Biome[])biomes);
        EntityRegistry.addSpawn(Dragonfly.class, (int)15, (int)1, (int)1, (EnumCreatureType)EnumCreatureType.CREATURE, (Biome[])EntitySpawns.swampsAndLakes());
        EntityRegistry.addSpawn(Mosquito.class, (int)15, (int)1, (int)1, (EnumCreatureType)EnumCreatureType.CREATURE, (Biome[])new Biome[]{Biomes.field_76780_h});
        EntityRegistry.addSpawn(Mantis.class, (int)10, (int)1, (int)1, (EnumCreatureType)EnumCreatureType.MONSTER, (Biome[])biomes);
        EntityRegistry.addSpawn(Brutalfly.class, (int)10, (int)1, (int)1, (EnumCreatureType)EnumCreatureType.MONSTER, (Biome[])EntitySpawns.forestsAndJungles());
        EntityRegistry.addSpawn(Kyuubi.class, (int)15, (int)1, (int)1, (EnumCreatureType)EnumCreatureType.MONSTER, (Biome[])EntitySpawns.nether());
        EntityRegistry.addSpawn(Mothra.class, (int)15, (int)1, (int)1, (EnumCreatureType)EnumCreatureType.MONSTER, (Biome[])EntitySpawns.extremeHills());
        EntityRegistry.addSpawn(Beaver.class, (int)15, (int)1, (int)1, (EnumCreatureType)EnumCreatureType.CREATURE, (Biome[])EntitySpawns.forestsAndJungles());
        EntityRegistry.addSpawn(Cassowary.class, (int)15, (int)1, (int)1, (EnumCreatureType)EnumCreatureType.CREATURE, (Biome[])EntitySpawns.extremeHills());
        EntityRegistry.addSpawn(RedCow.class, (int)15, (int)1, (int)1, (EnumCreatureType)EnumCreatureType.CREATURE, (Biome[])biomes);
        EntityRegistry.addSpawn(StinkBug.class, (int)15, (int)1, (int)1, (EnumCreatureType)EnumCreatureType.CREATURE, (Biome[])EntitySpawns.forestsAndJungles());
    }

    private static Biome[] excludeEndNether() {
        LinkedList<Biome> list = new LinkedList<Biome>();
        Collection biomes = ForgeRegistries.BIOMES.getValuesCollection();
        for (Biome biome : biomes) {
            if (biome.getRegistryName().toString().toLowerCase().contains("end") || biome.getRegistryName().toString().toLowerCase().contains("hell") || biome.getRegistryName().toString().toLowerCase().contains("void")) continue;
            list.add(biome);
        }
        return list.toArray(new Biome[0]);
    }

    private static Biome[] swampsAndLakes() {
        LinkedList<Biome> list = new LinkedList<Biome>();
        Collection biomes = ForgeRegistries.BIOMES.getValuesCollection();
        for (Biome biome : biomes) {
            if (!biome.getRegistryName().toString().toLowerCase().contains("swamp") && !biome.getRegistryName().toString().toLowerCase().contains("lake")) continue;
            list.add(biome);
        }
        return list.toArray(new Biome[0]);
    }

    private static Biome[] forestsAndJungles() {
        LinkedList<Biome> list = new LinkedList<Biome>();
        Collection biomes = ForgeRegistries.BIOMES.getValuesCollection();
        for (Biome biome : biomes) {
            if (!biome.getRegistryName().toString().toLowerCase().contains("forest") && !biome.getRegistryName().toString().toLowerCase().contains("jungle")) continue;
            list.add(biome);
        }
        return list.toArray(new Biome[0]);
    }

    private static Biome[] nether() {
        LinkedList<Biome> list = new LinkedList<Biome>();
        Collection biomes = ForgeRegistries.BIOMES.getValuesCollection();
        for (Biome biome : biomes) {
            if (!biome.getRegistryName().toString().toLowerCase().contains("hell")) continue;
            list.add(biome);
        }
        return list.toArray(new Biome[0]);
    }

    private static Biome[] extremeHills() {
        LinkedList<Biome> list = new LinkedList<Biome>();
        Collection biomes = ForgeRegistries.BIOMES.getValuesCollection();
        for (Biome biome : biomes) {
            if (!biome.getRegistryName().toString().toLowerCase().contains("extreme")) continue;
            list.add(biome);
        }
        return list.toArray(new Biome[0]);
    }
}

