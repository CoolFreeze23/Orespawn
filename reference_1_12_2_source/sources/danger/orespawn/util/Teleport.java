/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.player.EntityPlayer
 *  net.minecraft.entity.player.EntityPlayerMP
 *  net.minecraft.init.Blocks
 *  net.minecraft.server.MinecraftServer
 *  net.minecraft.util.math.BlockPos
 *  net.minecraft.world.Teleporter
 *  net.minecraft.world.WorldServer
 */
package danger.orespawn.util;

import java.util.Objects;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Teleporter;
import net.minecraft.world.WorldServer;

public class Teleport
extends Teleporter {
    private final WorldServer world;
    private final double x;
    private final double y;
    private final double z;

    public Teleport(WorldServer world, double x, double y, double z) {
        super(world);
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public void func_180266_a(Entity entityIn, float rotationYaw) {
        this.world.func_180495_p(new BlockPos(this.x, this.y, this.z));
        entityIn.func_70107_b(this.x, this.y, this.z);
        entityIn.field_70159_w = 0.0;
        entityIn.field_70181_x = 0.0;
        entityIn.field_70179_y = 0.0;
    }

    public static void teleportToDimension(EntityPlayer player, int dimension, double x, double y, double z) {
        if (!(player instanceof EntityPlayerMP)) {
            return;
        }
        int oldDimension = player.func_130014_f_().field_73011_w.getDimension();
        MinecraftServer server = player.func_130014_f_().func_73046_m();
        assert (server != null);
        WorldServer worldServer = server.func_71218_a(oldDimension);
        WorldServer newDim = server.func_71218_a(dimension);
        int i = 0;
        for (i = 255; i > 0 && newDim.func_180495_p(new BlockPos(x, (double)i, z)) == Blocks.field_150350_a.func_176223_P(); --i) {
        }
        Objects.requireNonNull(worldServer.func_73046_m()).func_184103_al().transferPlayerToDimension((EntityPlayerMP)player, dimension, (Teleporter)new Teleport(worldServer, x, ++i, z));
        player.func_70634_a(x, (double)i, z);
    }
}

