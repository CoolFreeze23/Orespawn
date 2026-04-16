/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.block.Block
 *  net.minecraft.block.material.Material
 */
package danger.orespawn.blocks;

import danger.orespawn.OreSpawnMain;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;

public class BlockBasic
extends Block {
    public BlockBasic(String name, Material material) {
        super(material);
        this.func_149663_c(name);
        this.setRegistryName(name);
        this.func_149647_a(OreSpawnMain.OreSpawnTab);
    }
}

