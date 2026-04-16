/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.biome.Biome$BiomeProperties
 *  net.minecraft.world.biome.Biome$SpawnListEntry
 *  net.minecraft.world.biome.BiomeHills
 *  net.minecraft.world.biome.BiomeHills$Type
 */
package danger.orespawn.world.biome;

import danger.orespawn.entity.Alien;
import danger.orespawn.entity.Alosaurus;
import danger.orespawn.entity.Baryonyx;
import danger.orespawn.entity.Bird;
import danger.orespawn.entity.Butterfly;
import danger.orespawn.entity.Camarasaurus;
import danger.orespawn.entity.Cryolophosaurus;
import danger.orespawn.entity.GammaMetroid;
import danger.orespawn.entity.Nastysaurus;
import danger.orespawn.entity.Pointysaurus;
import danger.orespawn.entity.Spyro;
import danger.orespawn.entity.TRex;
import danger.orespawn.entity.VelocityRaptor;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeHills;

public class BiomeMiningDimension
extends BiomeHills {
    public BiomeMiningDimension() {
        super(BiomeHills.Type.NORMAL, new Biome.BiomeProperties("Mining Biome").func_185398_c(1.0f).func_185400_d(0.5f).func_185410_a(0.2f).func_185395_b(0.3f));
        this.field_76761_J.add(new Biome.SpawnListEntry(Alosaurus.class, 100, 1, 1));
        this.field_76761_J.add(new Biome.SpawnListEntry(TRex.class, 100, 1, 1));
        this.field_76761_J.add(new Biome.SpawnListEntry(Pointysaurus.class, 100, 1, 1));
        this.field_76761_J.add(new Biome.SpawnListEntry(Cryolophosaurus.class, 100, 1, 1));
        this.field_76761_J.add(new Biome.SpawnListEntry(Alien.class, 100, 1, 1));
        this.field_76762_K.clear();
        this.field_76762_K.add(new Biome.SpawnListEntry(Baryonyx.class, 200, 1, 1));
        this.field_76762_K.add(new Biome.SpawnListEntry(Camarasaurus.class, 250, 1, 1));
        this.field_76762_K.add(new Biome.SpawnListEntry(Bird.class, 255, 1, 2));
        this.field_76762_K.add(new Biome.SpawnListEntry(Butterfly.class, 100, 1, 1));
        this.field_76762_K.add(new Biome.SpawnListEntry(Spyro.class, 250, 1, 1));
        this.field_76762_K.add(new Biome.SpawnListEntry(GammaMetroid.class, 200, 1, 1));
        this.field_76762_K.add(new Biome.SpawnListEntry(Nastysaurus.class, 200, 1, 1));
        this.field_76762_K.add(new Biome.SpawnListEntry(VelocityRaptor.class, 200, 1, 1));
    }
}

