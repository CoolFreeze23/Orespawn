/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  net.minecraft.command.CommandBase
 *  net.minecraft.command.CommandException
 *  net.minecraft.command.ICommandSender
 *  net.minecraft.entity.player.EntityPlayer
 *  net.minecraft.server.MinecraftServer
 *  net.minecraft.util.math.BlockPos
 *  net.minecraft.util.text.ITextComponent
 *  net.minecraft.util.text.TextComponentString
 *  net.minecraft.util.text.TextFormatting
 */
package danger.orespawn.commands;

import com.google.common.collect.Lists;
import danger.orespawn.util.Teleport;
import java.util.List;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;

public class CommandDimensionTeleport
extends CommandBase {
    private final List<String> aliases = Lists.newArrayList((Object[])new String[]{"orespawn", "tpdim", "dimtp"});

    public void func_184881_a(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        int dim;
        if (args.length < 1) {
            return;
        }
        try {
            dim = Integer.parseInt(args[0]);
        }
        catch (NumberFormatException e) {
            sender.func_145747_a((ITextComponent)new TextComponentString(TextFormatting.GRAY + "Dimension ID Invalid"));
            return;
        }
        if (sender instanceof EntityPlayer) {
            BlockPos senderPos = sender.func_180425_c();
            Teleport.teleportToDimension((EntityPlayer)sender, dim, senderPos.func_177958_n(), senderPos.func_177956_o(), senderPos.func_177952_p());
            EntityPlayer player = (EntityPlayer)sender;
            player.field_71093_bK = dim;
        }
    }

    public List<String> func_71514_a() {
        return this.aliases;
    }

    public String func_71517_b() {
        return "dimensiontp";
    }

    public String func_71518_a(ICommandSender arg0) {
        return "tpdimension <id>";
    }
}

