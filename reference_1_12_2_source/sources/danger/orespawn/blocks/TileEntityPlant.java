/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.nbt.NBTTagCompound
 *  net.minecraft.tileentity.TileEntity
 */
package danger.orespawn.blocks;

import danger.orespawn.OreSpawnMain;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;

public class TileEntityPlant
extends TileEntity {
    private int age = 0;
    private int phase = 1;
    private int heightContribution = OreSpawnMain.OreSpawnRand.nextInt(5) + 3;

    public int getAge() {
        return this.age;
    }

    public void setAge(int x) {
        this.age = x;
    }

    public int getPhase() {
        return this.phase;
    }

    public void setPhase(int x) {
        this.phase = x;
    }

    public int getHeightContribution() {
        return this.heightContribution;
    }

    public void func_145839_a(NBTTagCompound nbt) {
        this.age = nbt.func_74762_e("Age");
        this.phase = nbt.func_74762_e("Phase");
        this.heightContribution = nbt.func_74762_e("HeightContribution");
        super.func_145839_a(nbt);
    }

    public NBTTagCompound func_189515_b(NBTTagCompound nbt) {
        nbt.func_74768_a("Age", this.age);
        nbt.func_74768_a("Phase", this.phase);
        nbt.func_74768_a("HeightContribution", this.heightContribution);
        return super.func_189515_b(nbt);
    }
}

