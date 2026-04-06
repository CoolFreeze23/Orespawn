/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.World
 */
package danger.orespawn;

import danger.orespawn.Cockateil;
import net.minecraft.world.World;

public class RubyBird
extends Cockateil {
    public RubyBird(World par1World) {
        super(par1World);
    }

    @Override
    protected void func_70088_a() {
        super.func_70088_a();
        this.birdtype = 5;
        this.setBirdType(this.birdtype);
        this.setFlyUp();
    }

    @Override
    protected String func_70639_aQ() {
        if (this.field_70170_p.func_72935_r() && !this.field_70170_p.func_72896_J()) {
            return "orespawn:rubybird";
        }
        return null;
    }

    @Override
    public boolean func_70601_bi() {
        return true;
    }
}

