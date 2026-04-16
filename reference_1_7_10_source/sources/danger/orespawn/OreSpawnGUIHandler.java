/*
 * Decompiled with CFR 0.152.
 */
package danger.orespawn;

import cpw.mods.fml.common.network.IGuiHandler;
import danger.orespawn.ContainerCrystalFurnace;
import danger.orespawn.ContainerCrystalWorkbench;
import danger.orespawn.CrystalFurnaceGUI;
import danger.orespawn.CrystalWorkbenchGUI;
import danger.orespawn.TileEntityCrystalFurnace;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class OreSpawnGUIHandler
implements IGuiHandler {
    public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        TileEntity tileEntity = world.func_147438_o(x, y, z);
        switch (ID) {
            case 0: {
                if (!(tileEntity instanceof TileEntityCrystalFurnace)) break;
                return new ContainerCrystalFurnace(player.field_71071_by, (TileEntityCrystalFurnace)tileEntity);
            }
            case 1: {
                return new ContainerCrystalWorkbench(player.field_71071_by, world, x, y, z);
            }
        }
        return null;
    }

    public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        TileEntity tileEntity = world.func_147438_o(x, y, z);
        switch (ID) {
            case 0: {
                if (!(tileEntity instanceof TileEntityCrystalFurnace)) break;
                return new CrystalFurnaceGUI(player.field_71071_by, (TileEntityCrystalFurnace)tileEntity);
            }
            case 1: {
                return new CrystalWorkbenchGUI(player.field_71071_by, world, x, y, z);
            }
        }
        return null;
    }
}

