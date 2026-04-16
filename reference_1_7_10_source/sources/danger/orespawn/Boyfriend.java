/*
 * Decompiled with CFR 0.152.
 */
package danger.orespawn;

import danger.orespawn.Elevator;
import danger.orespawn.ItemOreSpawnArmor;
import danger.orespawn.MyEntityAIFollowOwner;
import danger.orespawn.MyEntityAIJealousy;
import danger.orespawn.MyEntityAINearestAttackableTarget;
import danger.orespawn.MyEntityAIWander;
import danger.orespawn.OreSpawnMain;
import danger.orespawn.Shoes;
import danger.orespawn.UltimateArrow;
import net.minecraft.block.Block;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IRangedAttackMob;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIArrowAttack;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAIMoveIndoors;
import net.minecraft.entity.ai.EntityAIOpenDoor;
import net.minecraft.entity.ai.EntityAIPanic;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAITempt;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.Potion;
import net.minecraft.tileentity.TileEntityMobSpawner;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.DamageSource;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

public class Boyfriend
extends EntityTameable
implements IRangedAttackMob {
    public int which_guy = this.field_70146_Z.nextInt(28);
    public int which_wet_guy = 0;
    public int wet_count = 0;
    private int auto_heal = 200;
    private int force_sync = 50;
    private int fight_sound_ticker = 0;
    private int taunt_sound_ticker = 0;
    private int had_target = 0;
    private int voice = this.field_70146_Z.nextInt(10);
    private float moveSpeed = 0.3f;
    private int voice_enable = 1;
    public int passenger = 0;
    private int is_prince = 0;
    private static final ResourceLocation DryTexture0 = new ResourceLocation("orespawn", "boyfriend0.png");
    private static final ResourceLocation DryTexture1 = new ResourceLocation("orespawn", "boyfriend1.png");
    private static final ResourceLocation DryTexture2 = new ResourceLocation("orespawn", "boyfriend2.png");
    private static final ResourceLocation DryTexture3 = new ResourceLocation("orespawn", "boyfriend3.png");
    private static final ResourceLocation DryTexture4 = new ResourceLocation("orespawn", "boyfriend4.png");
    private static final ResourceLocation DryTexture5 = new ResourceLocation("orespawn", "boyfriend5.png");
    private static final ResourceLocation DryTexture6 = new ResourceLocation("orespawn", "boyfriend6.png");
    private static final ResourceLocation DryTexture7 = new ResourceLocation("orespawn", "boyfriend7.png");
    private static final ResourceLocation DryTexture8 = new ResourceLocation("orespawn", "boyfriend8.png");
    private static final ResourceLocation DryTexture9 = new ResourceLocation("orespawn", "boyfriend9.png");
    private static final ResourceLocation DryTexture10 = new ResourceLocation("orespawn", "boyfriend10.png");
    private static final ResourceLocation DryTexture11 = new ResourceLocation("orespawn", "boyfriend11.png");
    private static final ResourceLocation DryTexture12 = new ResourceLocation("orespawn", "boyfriend12.png");
    private static final ResourceLocation DryTexture13 = new ResourceLocation("orespawn", "boyfriend13.png");
    private static final ResourceLocation DryTexture14 = new ResourceLocation("orespawn", "boyfriend14.png");
    private static final ResourceLocation DryTexture15 = new ResourceLocation("orespawn", "boyfriend15.png");
    private static final ResourceLocation DryTexture16 = new ResourceLocation("orespawn", "boyfriend16.png");
    private static final ResourceLocation DryTexture17 = new ResourceLocation("orespawn", "boyfriend17.png");
    private static final ResourceLocation DryTexture18 = new ResourceLocation("orespawn", "boyfriend18.png");
    private static final ResourceLocation DryTexture19 = new ResourceLocation("orespawn", "boyfriend19.png");
    private static final ResourceLocation DryTexture20 = new ResourceLocation("orespawn", "boyfriend20.png");
    private static final ResourceLocation DryTexture21 = new ResourceLocation("orespawn", "boyfriend21.png");
    private static final ResourceLocation DryTexture22 = new ResourceLocation("orespawn", "boyfriend22.png");
    private static final ResourceLocation DryTexture23 = new ResourceLocation("orespawn", "boyfriend23.png");
    private static final ResourceLocation DryTexture24 = new ResourceLocation("orespawn", "boyfriend24.png");
    private static final ResourceLocation DryTexture25 = new ResourceLocation("orespawn", "boyfriend25.png");
    private static final ResourceLocation DryTexture26 = new ResourceLocation("orespawn", "boyfriend26.png");
    private static final ResourceLocation DryTexture27 = new ResourceLocation("orespawn", "boyfriend27.png");
    private static final ResourceLocation WetTexture0 = new ResourceLocation("orespawn", "swimshorts0.png");
    private static final ResourceLocation WetTexture1 = new ResourceLocation("orespawn", "swimshorts1.png");
    private static final ResourceLocation WetTexture2 = new ResourceLocation("orespawn", "swimshorts2.png");
    private static final ResourceLocation WetTexture3 = new ResourceLocation("orespawn", "swimshorts3.png");
    private static final ResourceLocation WetTexture4 = new ResourceLocation("orespawn", "swimshorts4.png");
    private static final ResourceLocation WetTexture5 = new ResourceLocation("orespawn", "swimshorts5.png");
    private static final ResourceLocation WetTexture6 = new ResourceLocation("orespawn", "swimshorts6.png");
    private static final ResourceLocation WetTexture7 = new ResourceLocation("orespawn", "swimshorts7.png");
    private static final ResourceLocation WetTexture8 = new ResourceLocation("orespawn", "swimshorts8.png");
    private static final ResourceLocation WetTexture9 = new ResourceLocation("orespawn", "swimshorts9.png");
    private static final ResourceLocation WetTexture10 = new ResourceLocation("orespawn", "swimshorts10.png");
    private static final ResourceLocation WetTexture11 = new ResourceLocation("orespawn", "swimshorts11.png");
    private static final ResourceLocation WetTexture12 = new ResourceLocation("orespawn", "swimshorts12.png");
    private static final ResourceLocation WetTexture13 = new ResourceLocation("orespawn", "swimshorts13.png");
    private static final ResourceLocation WetTexture14 = new ResourceLocation("orespawn", "swimshorts14.png");
    private static final ResourceLocation WetTexture15 = new ResourceLocation("orespawn", "swimshorts15.png");
    private static final ResourceLocation WetTexture16 = new ResourceLocation("orespawn", "swimshorts16.png");
    private static final ResourceLocation WetTexture17 = new ResourceLocation("orespawn", "swimshorts17.png");
    private static final ResourceLocation PrinceTexture1 = new ResourceLocation("orespawn", "FrogPrince.png");
    private static final ResourceLocation PrinceTexture2 = new ResourceLocation("orespawn", "FrogPrince2.png");

    public Boyfriend(World par1World) {
        super(par1World);
        this.which_wet_guy = this.field_70146_Z.nextInt(18);
        this.func_70105_a(0.5f, 1.6f);
        this.field_70178_ae = true;
        this.field_70174_ab = 100;
        this.func_70661_as().func_75491_a(false);
        this.func_70904_g(false);
        this.field_70714_bg.func_75776_a(1, (EntityAIBase)new MyEntityAIFollowOwner((EntityTameable)this, 1.4f, 12.0f, 1.5f));
        this.field_70714_bg.func_75776_a(2, (EntityAIBase)new EntityAITempt((EntityCreature)this, 1.25, Items.field_151083_be, false));
        this.field_70714_bg.func_75776_a(4, (EntityAIBase)new EntityAIArrowAttack((IRangedAttackMob)this, 1.25, 20, 10.0f));
        this.field_70714_bg.func_75776_a(5, (EntityAIBase)new EntityAISwimming((EntityLiving)this));
        this.field_70714_bg.func_75776_a(6, (EntityAIBase)new EntityAIPanic((EntityCreature)this, 1.5));
        this.field_70714_bg.func_75776_a(7, (EntityAIBase)new EntityAIWatchClosest((EntityLiving)this, EntityPlayer.class, 6.0f));
        this.field_70714_bg.func_75776_a(8, (EntityAIBase)new MyEntityAIWander((EntityCreature)this, 0.75f));
        this.field_70714_bg.func_75776_a(9, (EntityAIBase)new EntityAILookIdle((EntityLiving)this));
        this.field_70714_bg.func_75776_a(10, (EntityAIBase)new EntityAIOpenDoor((EntityLiving)this, true));
        this.field_70714_bg.func_75776_a(11, (EntityAIBase)new EntityAIMoveIndoors((EntityCreature)this));
        if (OreSpawnMain.PlayNicely == 0) {
            this.field_70715_bh.func_75776_a(2, (EntityAIBase)new MyEntityAINearestAttackableTarget((EntityLiving)this, EntityCreeper.class, 20.0f, 0, true, true, IMob.field_82192_a));
        }
        if (OreSpawnMain.PlayNicely == 0) {
            this.field_70715_bh.func_75776_a(3, (EntityAIBase)new MyEntityAINearestAttackableTarget((EntityLiving)this, EntityLiving.class, 15.0f, 0, true, true, IMob.field_82192_a));
        }
        if (OreSpawnMain.PlayNicely == 0) {
            this.field_70715_bh.func_75776_a(4, (EntityAIBase)new MyEntityAIJealousy((EntityTameable)this, Boyfriend.class, 6.0f, 5, true));
        }
        if (OreSpawnMain.PlayNicely == 0) {
            this.field_70715_bh.func_75776_a(5, (EntityAIBase)new MyEntityAIJealousy((EntityTameable)this, Boyfriend.class, 3.0f, 15, true));
        }
        this.field_70728_aV = 0;
    }

    protected void func_70088_a() {
        super.func_70088_a();
        this.which_guy = this.field_70146_Z.nextInt(28);
        this.field_70180_af.func_75682_a(20, (Object)this.which_guy);
        this.wet_count = 0;
        this.which_wet_guy = this.field_70146_Z.nextInt(18);
        this.field_70180_af.func_75682_a(22, (Object)this.which_wet_guy);
        this.voice = this.field_70146_Z.nextInt(10);
        this.field_70180_af.func_75682_a(21, (Object)this.voice);
        this.field_70180_af.func_75682_a(23, (Object)this.voice_enable);
        this.field_70180_af.func_75682_a(24, (Object)this.is_prince);
        this.auto_heal = 200;
        this.force_sync = 50;
        this.fight_sound_ticker = 0;
        this.taunt_sound_ticker = 0;
        this.had_target = 0;
        this.func_70904_g(false);
    }

    protected void func_110147_ax() {
        super.func_110147_ax();
        this.func_110148_a(SharedMonsterAttributes.field_111267_a).func_111128_a((double)this.mygetMaxHealth());
        this.func_110148_a(SharedMonsterAttributes.field_111263_d).func_111128_a((double)this.moveSpeed);
        this.func_110140_aT().func_111150_b(SharedMonsterAttributes.field_111264_e);
        this.func_110148_a(SharedMonsterAttributes.field_111264_e).func_111128_a(8.0);
    }

    public int func_70658_aO() {
        int i = 0;
        for (ItemStack itemstack : this.func_70035_c()) {
            if (itemstack == null || !(itemstack.func_77973_b() instanceof ItemArmor)) continue;
            int l = ((ItemArmor)itemstack.func_77973_b()).field_77879_b;
            i += l;
        }
        if (i < 8) {
            i = 8;
        }
        if (i > 23) {
            i = 23;
        }
        return i;
    }

    public void func_70071_h_() {
        EntityLivingBase e;
        this.func_110148_a(SharedMonsterAttributes.field_111263_d).func_111128_a((double)this.moveSpeed);
        super.func_70071_h_();
        this.passenger = 0;
        if (this.func_70909_n() && !this.func_70906_o() && (e = this.func_70902_q()) != null && e instanceof EntityPlayer) {
            EntityPlayer p = (EntityPlayer)e;
            Entity r = e.field_70154_o;
            if (r != null && r instanceof Elevator) {
                float f = -0.45f;
                this.func_70107_b(r.field_70165_t - (double)f * Math.sin(Math.toRadians(r.field_70177_z)), r.field_70163_u, r.field_70161_v + (double)f * Math.cos(Math.toRadians(r.field_70177_z)));
                this.field_70177_z = r.field_70177_z;
                this.field_70125_A = r.field_70125_A;
                this.field_70721_aZ = 0.0f;
                this.field_70754_ba = 0.0f;
                this.field_70143_R = 0.0f;
                this.passenger = 1;
            }
        }
    }

    public void func_70014_b(NBTTagCompound par1NBTTagCompound) {
        super.func_70014_b(par1NBTTagCompound);
        par1NBTTagCompound.func_74768_a("GirlType", this.getTameSkin());
        par1NBTTagCompound.func_74768_a("WetGirlType", this.getWetTameSkin());
        par1NBTTagCompound.func_74768_a("GirlVoice", this.field_70180_af.func_75679_c(21));
        par1NBTTagCompound.func_74768_a("GirlVoiceEnable", this.field_70180_af.func_75679_c(23));
        par1NBTTagCompound.func_74768_a("IsPrince", this.field_70180_af.func_75679_c(24));
    }

    public void func_70037_a(NBTTagCompound par1NBTTagCompound) {
        super.func_70037_a(par1NBTTagCompound);
        this.which_guy = par1NBTTagCompound.func_74762_e("GirlType");
        this.setTameSkin(this.which_guy);
        this.which_wet_guy = par1NBTTagCompound.func_74762_e("WetGirlType");
        this.setWetTameSkin(this.which_wet_guy);
        this.voice = par1NBTTagCompound.func_74762_e("GirlVoice");
        this.field_70180_af.func_75692_b(21, (Object)this.voice);
        this.voice_enable = par1NBTTagCompound.func_74762_e("GirlVoiceEnable");
        this.field_70180_af.func_75692_b(23, (Object)this.voice_enable);
        this.is_prince = par1NBTTagCompound.func_74762_e("IsPrince");
        this.field_70180_af.func_75692_b(24, (Object)this.is_prince);
    }

    protected void func_70629_bd() {
        super.func_70629_bd();
        ItemStack stack = this.getCurrentEquippedItem();
        EntityLivingBase victim = this.func_70638_az();
        if (OreSpawnMain.PlayNicely != 0) {
            victim = null;
        }
        if (this.field_70170_p.field_73012_v.nextInt(100) == 1) {
            this.func_70604_c(null);
        }
        if (stack != null && !this.func_70906_o()) {
            if (victim != null) {
                if (victim instanceof EntityLivingBase && this.func_70694_bm() != null) {
                    if (this.func_70032_d((Entity)victim) < 4.0f || stack.func_77973_b() == OreSpawnMain.MyBertha && this.func_70032_d((Entity)victim) < 10.0f) {
                        --this.field_70724_aR;
                        if (this.field_70724_aR <= 0) {
                            this.field_70724_aR = 25;
                            this.func_71038_i();
                            this.attackTargetEntityWithCurrentItem((Entity)victim);
                            --this.fight_sound_ticker;
                            if (this.fight_sound_ticker <= 0) {
                                if (!this.field_70170_p.field_72995_K && this.voice_enable != 0) {
                                    this.field_70170_p.func_72956_a((Entity)this, "orespawn:b_fight", 0.5f, this.func_70647_i());
                                }
                                this.fight_sound_ticker = 3;
                            }
                            this.had_target = 1;
                        }
                    } else if (this.func_70032_d((Entity)victim) < 7.0f && stack.func_77973_b() != OreSpawnMain.MyUltimateBow) {
                        --this.taunt_sound_ticker;
                        if (this.taunt_sound_ticker <= 0) {
                            if (!this.field_70170_p.field_72995_K && this.voice_enable != 0) {
                                this.field_70170_p.func_72956_a((Entity)this, "orespawn:b_taunt", 0.5f, this.func_70647_i());
                            }
                            this.taunt_sound_ticker = 300;
                        }
                        this.func_70661_as().func_75497_a((Entity)victim, 1.25);
                    }
                }
            } else {
                this.fight_sound_ticker = 0;
                this.field_70724_aR = 0;
                if (this.had_target != 0) {
                    this.had_target = 0;
                    if (!this.field_70170_p.field_72995_K && this.voice_enable != 0) {
                        this.field_70170_p.func_72956_a((Entity)this, "orespawn:b_woohoo", 0.4f, this.func_70647_i());
                    }
                }
            }
        }
    }

    public void setPrince(int par1) {
        this.is_prince = par1;
    }

    public ResourceLocation getTexture() {
        if (this.wet_count <= 0) {
            int txture = this.getTameSkin();
            if (this.is_prince == 1) {
                return PrinceTexture1;
            }
            if (this.is_prince == 2) {
                return PrinceTexture2;
            }
            if (txture == 0) {
                return DryTexture0;
            }
            if (txture == 1) {
                return DryTexture1;
            }
            if (txture == 2) {
                return DryTexture2;
            }
            if (txture == 3) {
                return DryTexture3;
            }
            if (txture == 4) {
                return DryTexture4;
            }
            if (txture == 5) {
                return DryTexture5;
            }
            if (txture == 6) {
                return DryTexture6;
            }
            if (txture == 7) {
                return DryTexture7;
            }
            if (txture == 8) {
                return DryTexture8;
            }
            if (txture == 9) {
                return DryTexture9;
            }
            if (txture == 10) {
                return DryTexture10;
            }
            if (txture == 11) {
                return DryTexture11;
            }
            if (txture == 12) {
                return DryTexture12;
            }
            if (txture == 13) {
                return DryTexture13;
            }
            if (txture == 14) {
                return DryTexture14;
            }
            if (txture == 15) {
                return DryTexture15;
            }
            if (txture == 16) {
                return DryTexture16;
            }
            if (txture == 17) {
                return DryTexture17;
            }
            if (txture == 18) {
                return DryTexture18;
            }
            if (txture == 19) {
                return DryTexture19;
            }
            if (txture == 20) {
                return DryTexture20;
            }
            if (txture == 21) {
                return DryTexture21;
            }
            if (txture == 22) {
                return DryTexture22;
            }
            if (txture == 23) {
                return DryTexture23;
            }
            if (txture == 24) {
                return DryTexture24;
            }
            if (txture == 25) {
                return DryTexture25;
            }
            if (txture == 26) {
                return DryTexture26;
            }
            if (txture == 27) {
                return DryTexture27;
            }
        } else {
            int temp = this.getWetTameSkin();
            if (temp == 0) {
                return WetTexture0;
            }
            if (temp == 1) {
                return WetTexture1;
            }
            if (temp == 2) {
                return WetTexture2;
            }
            if (temp == 3) {
                return WetTexture3;
            }
            if (temp == 4) {
                return WetTexture4;
            }
            if (temp == 5) {
                return WetTexture5;
            }
            if (temp == 6) {
                return WetTexture6;
            }
            if (temp == 7) {
                return WetTexture7;
            }
            if (temp == 8) {
                return WetTexture8;
            }
            if (temp == 9) {
                return WetTexture9;
            }
            if (temp == 10) {
                return WetTexture10;
            }
            if (temp == 11) {
                return WetTexture11;
            }
            if (temp == 12) {
                return WetTexture12;
            }
            if (temp == 13) {
                return WetTexture13;
            }
            if (temp == 14) {
                return WetTexture14;
            }
            if (temp == 15) {
                return WetTexture15;
            }
            if (temp == 16) {
                return WetTexture16;
            }
            if (temp == 17) {
                return WetTexture17;
            }
        }
        return null;
    }

    public int getTameSkin() {
        return this.field_70180_af.func_75679_c(20);
    }

    public int getVoice() {
        return this.field_70180_af.func_75679_c(21);
    }

    public void setTameSkin(int par1) {
        this.field_70180_af.func_75692_b(20, (Object)par1);
        this.which_guy = par1;
    }

    public int getWetTameSkin() {
        return this.field_70180_af.func_75679_c(22);
    }

    public void setWetTameSkin(int par1) {
        this.field_70180_af.func_75692_b(22, (Object)par1);
        this.which_wet_guy = par1;
    }

    public boolean func_70650_aV() {
        return true;
    }

    public boolean func_70648_aU() {
        return true;
    }

    protected void func_70069_a(float par1) {
        float i = MathHelper.func_76123_f((float)(par1 - 3.0f));
        if (i > 0.0f) {
            if (i > 3.0f) {
                this.func_85030_a("damage.fallbig", 1.0f, 1.0f);
                i = 3.0f;
            } else {
                this.func_85030_a("damage.fallsmall", 1.0f, 1.0f);
            }
            this.func_70097_a(DamageSource.field_76379_h, i);
        }
    }

    public int mygetMaxHealth() {
        return 80;
    }

    public void func_70636_d() {
        this.func_82168_bl();
        super.func_70636_d();
        if (this.func_70090_H() || this.func_70058_J()) {
            this.wet_count = 500;
        } else if (this.wet_count > 0) {
            --this.wet_count;
        }
        --this.auto_heal;
        if (this.auto_heal <= 0) {
            if (this.mygetMaxHealth() > this.getBoyfriendHealth()) {
                this.func_70691_i(1.0f);
            }
            this.auto_heal = 150;
        }
        --this.force_sync;
        if (this.force_sync <= 0) {
            this.force_sync = 20;
            if (!this.field_70170_p.field_72995_K) {
                this.field_70180_af.func_75692_b(21, (Object)this.voice);
                this.field_70180_af.func_75692_b(23, (Object)this.voice_enable);
                this.field_70180_af.func_75692_b(24, (Object)this.is_prince);
                this.func_70904_g(this.func_70906_o());
            } else {
                this.voice = this.getVoice();
                this.voice_enable = this.field_70180_af.func_75679_c(23);
                this.is_prince = this.field_70180_af.func_75679_c(24);
            }
        }
    }

    public int getBoyfriendHealth() {
        return (int)this.func_110143_aJ();
    }

    public boolean func_70085_c(EntityPlayer par1EntityPlayer) {
        ItemStack var2 = par1EntityPlayer.field_71071_by.func_70448_g();
        if (var2 != null && var2.field_77994_a <= 0) {
            par1EntityPlayer.field_71071_by.func_70299_a(par1EntityPlayer.field_71071_by.field_70461_c, (ItemStack)null);
            var2 = null;
        }
        if (var2 != null && (var2.func_77973_b() == Items.field_151083_be || var2.func_77973_b() == OreSpawnMain.MyPeacock) && par1EntityPlayer.func_70068_e((Entity)this) < 16.0) {
            if (!this.func_70909_n()) {
                if (!this.field_70170_p.field_72995_K) {
                    if (this.field_70170_p.field_73012_v.nextInt(3) == 0) {
                        this.func_70903_f(true);
                        this.func_152115_b(par1EntityPlayer.func_110124_au().toString());
                        this.func_70908_e(true);
                        this.field_70170_p.func_72960_a((Entity)this, (byte)7);
                        this.func_70691_i((float)this.mygetMaxHealth() - this.func_110143_aJ());
                    } else {
                        this.func_70908_e(false);
                        this.field_70170_p.func_72960_a((Entity)this, (byte)6);
                    }
                }
            } else if (this.func_152114_e((EntityLivingBase)par1EntityPlayer)) {
                if (this.field_70170_p.field_72995_K) {
                    this.func_70908_e(true);
                    this.field_70170_p.func_72960_a((Entity)this, (byte)7);
                }
                if ((float)this.mygetMaxHealth() > this.func_110143_aJ()) {
                    this.func_70691_i((float)this.mygetMaxHealth() - this.func_110143_aJ());
                }
            }
            if (!par1EntityPlayer.field_71075_bZ.field_75098_d) {
                --var2.field_77994_a;
                if (var2.field_77994_a <= 0) {
                    par1EntityPlayer.field_71071_by.func_70299_a(par1EntityPlayer.field_71071_by.field_70461_c, (ItemStack)null);
                }
            }
            return true;
        }
        if (this.func_70909_n() && var2 != null && var2.func_77973_b() == Item.func_150898_a((Block)Blocks.field_150330_I) && par1EntityPlayer.func_70068_e((Entity)this) < 16.0 && this.func_152114_e((EntityLivingBase)par1EntityPlayer)) {
            if (!this.field_70170_p.field_72995_K) {
                this.func_70903_f(false);
                this.func_152115_b("");
                this.func_70908_e(false);
                this.field_70170_p.func_72960_a((Entity)this, (byte)6);
            }
            if (!par1EntityPlayer.field_71075_bZ.field_75098_d) {
                --var2.field_77994_a;
                if (var2.field_77994_a <= 0) {
                    par1EntityPlayer.field_71071_by.func_70299_a(par1EntityPlayer.field_71071_by.field_70461_c, (ItemStack)null);
                }
            }
            return true;
        }
        if (this.func_70909_n() && var2 != null && var2.func_77973_b() == OreSpawnMain.MyRuby && par1EntityPlayer.func_70068_e((Entity)this) < 16.0 && this.func_152114_e((EntityLivingBase)par1EntityPlayer)) {
            if (!this.field_70170_p.field_72995_K) {
                this.voice_enable = 0;
                this.field_70180_af.func_75692_b(23, (Object)this.voice_enable);
                this.func_70908_e(true);
                this.field_70170_p.func_72960_a((Entity)this, (byte)7);
            }
            if (!par1EntityPlayer.field_71075_bZ.field_75098_d) {
                --var2.field_77994_a;
                if (var2.field_77994_a <= 0) {
                    par1EntityPlayer.field_71071_by.func_70299_a(par1EntityPlayer.field_71071_by.field_70461_c, (ItemStack)null);
                }
            }
            return true;
        }
        if (this.func_70909_n() && var2 != null && var2.func_77973_b() == OreSpawnMain.MyAmethyst && par1EntityPlayer.func_70068_e((Entity)this) < 16.0 && this.func_152114_e((EntityLivingBase)par1EntityPlayer)) {
            if (!this.field_70170_p.field_72995_K) {
                this.voice_enable = 1;
                this.field_70180_af.func_75692_b(23, (Object)this.voice_enable);
                this.func_70908_e(true);
                this.field_70170_p.func_72960_a((Entity)this, (byte)7);
            }
            if (!par1EntityPlayer.field_71075_bZ.field_75098_d) {
                --var2.field_77994_a;
                if (var2.field_77994_a <= 0) {
                    par1EntityPlayer.field_71071_by.func_70299_a(par1EntityPlayer.field_71071_by.field_70461_c, (ItemStack)null);
                }
            }
            return true;
        }
        if (this.func_70909_n() && var2 != null && (var2.func_77973_b() == Items.field_151116_aA || var2.func_77973_b() == OreSpawnMain.MyPeacockFeather) && par1EntityPlayer.func_70068_e((Entity)this) < 16.0 && this.func_152114_e((EntityLivingBase)par1EntityPlayer)) {
            if (!this.field_70170_p.field_72995_K) {
                if (this.wet_count > 0 || this.func_70090_H() || this.func_70058_J()) {
                    ++this.which_wet_guy;
                    if (this.which_wet_guy > 17) {
                        this.which_wet_guy = 0;
                    }
                    this.setWetTameSkin(this.which_wet_guy);
                    this.field_70170_p.func_72960_a((Entity)this, (byte)7);
                    if (this.func_70090_H() || this.func_70058_J()) {
                        this.wet_count = 500;
                    }
                } else {
                    ++this.which_guy;
                    if (this.which_guy > 27) {
                        this.which_guy = 0;
                    }
                    this.setTameSkin(this.which_guy);
                    this.field_70170_p.func_72960_a((Entity)this, (byte)7);
                }
            }
            if (!par1EntityPlayer.field_71075_bZ.field_75098_d) {
                --var2.field_77994_a;
                if (var2.field_77994_a <= 0) {
                    par1EntityPlayer.field_71071_by.func_70299_a(par1EntityPlayer.field_71071_by.field_70461_c, (ItemStack)null);
                }
            }
            return true;
        }
        if (this.func_70909_n() && var2 != null && this.func_152114_e((EntityLivingBase)par1EntityPlayer) && par1EntityPlayer.func_70068_e((Entity)this) < 16.0) {
            if (var2.func_77973_b() instanceof ItemFood) {
                if (!this.field_70170_p.field_72995_K) {
                    ItemFood var3 = (ItemFood)var2.func_77973_b();
                    if ((float)this.mygetMaxHealth() > this.func_110143_aJ()) {
                        this.func_70691_i((float)(var3.func_150905_g(var2) * 5));
                    }
                    this.func_70908_e(true);
                    this.field_70170_p.func_72960_a((Entity)this, (byte)7);
                }
                if (!par1EntityPlayer.field_71075_bZ.field_75098_d) {
                    --var2.field_77994_a;
                    if (var2.field_77994_a <= 0) {
                        par1EntityPlayer.field_71071_by.func_70299_a(par1EntityPlayer.field_71071_by.field_70461_c, (ItemStack)null);
                    }
                }
            } else {
                if (!this.field_70170_p.field_72995_K) {
                    this.func_70908_e(true);
                    this.field_70170_p.func_72960_a((Entity)this, (byte)7);
                }
                ItemStack var3 = this.getCurrentEquippedItem();
                this.func_70062_b(0, var2);
                if (var2.func_77973_b() == Items.field_151045_i) {
                    this.func_70904_g(true);
                } else {
                    this.func_70904_g(false);
                }
                if (var3 != null) {
                    par1EntityPlayer.field_71071_by.func_70299_a(par1EntityPlayer.field_71071_by.field_70461_c, var3);
                } else {
                    par1EntityPlayer.field_71071_by.func_70299_a(par1EntityPlayer.field_71071_by.field_70461_c, (ItemStack)null);
                    Item itm = var2.func_77973_b();
                    if (itm instanceof ItemOreSpawnArmor) {
                        ItemStack v4;
                        if (itm == OreSpawnMain.EmeraldHelmet || itm == OreSpawnMain.AmethystHelmet || itm == OreSpawnMain.UltimateHelmet) {
                            v4 = this.func_71124_b(4);
                            this.func_70062_b(4, var2);
                            this.func_70062_b(0, v4);
                        }
                        if (itm == OreSpawnMain.EmeraldBody || itm == OreSpawnMain.AmethystBody || itm == OreSpawnMain.UltimateBody) {
                            v4 = this.func_71124_b(3);
                            this.func_70062_b(3, var2);
                            this.func_70062_b(0, v4);
                        }
                        if (itm == OreSpawnMain.EmeraldLegs || itm == OreSpawnMain.AmethystLegs || itm == OreSpawnMain.UltimateLegs) {
                            v4 = this.func_71124_b(2);
                            this.func_70062_b(2, var2);
                            this.func_70062_b(0, v4);
                        }
                        if (itm == OreSpawnMain.EmeraldBoots || itm == OreSpawnMain.AmethystBoots || itm == OreSpawnMain.UltimateBoots) {
                            v4 = this.func_71124_b(1);
                            this.func_70062_b(1, var2);
                            this.func_70062_b(0, v4);
                        }
                    }
                }
            }
            return true;
        }
        if (this.func_70909_n() && var2 != null && var2.func_77973_b() == Item.func_150898_a((Block)Blocks.field_150484_ah) && par1EntityPlayer.func_70068_e((Entity)this) < 16.0) {
            this.func_70904_g(false);
            this.func_70903_f(true);
            this.func_152115_b(par1EntityPlayer.func_110124_au().toString());
            this.func_70908_e(true);
            this.field_70170_p.func_72960_a((Entity)this, (byte)7);
            if (!par1EntityPlayer.field_71075_bZ.field_75098_d) {
                --var2.field_77994_a;
                if (var2.field_77994_a <= 0) {
                    par1EntityPlayer.field_71071_by.func_70299_a(par1EntityPlayer.field_71071_by.field_70461_c, (ItemStack)null);
                }
            }
            return true;
        }
        if (this.func_70909_n() && var2 != null && var2.func_77973_b() == Items.field_151057_cb && par1EntityPlayer.func_70068_e((Entity)this) < 16.0 && this.func_152114_e((EntityLivingBase)par1EntityPlayer)) {
            this.func_94058_c(var2.func_82833_r());
            if (!par1EntityPlayer.field_71075_bZ.field_75098_d) {
                --var2.field_77994_a;
                if (var2.field_77994_a <= 0) {
                    par1EntityPlayer.field_71071_by.func_70299_a(par1EntityPlayer.field_71071_by.field_70461_c, (ItemStack)null);
                }
            }
            return true;
        }
        if (this.func_70909_n() && var2 == null && par1EntityPlayer.func_70068_e((Entity)this) < 16.0 && this.func_152114_e((EntityLivingBase)par1EntityPlayer)) {
            ItemStack var3 = this.func_71124_b(0);
            int it = 0;
            if (var3 == null) {
                var3 = this.func_71124_b(++it);
            }
            if (var3 == null) {
                var3 = this.func_71124_b(++it);
            }
            if (var3 == null) {
                var3 = this.func_71124_b(++it);
            }
            if (var3 == null) {
                var3 = this.func_71124_b(++it);
            }
            if (var3 != null) {
                par1EntityPlayer.field_71071_by.func_70299_a(par1EntityPlayer.field_71071_by.field_70461_c, var3);
                this.func_70062_b(it, null);
                this.func_70904_g(false);
                if (!this.field_70170_p.field_72995_K) {
                    this.field_70170_p.func_72960_a((Entity)this, (byte)6);
                }
            } else if (!this.field_70170_p.field_72995_K) {
                this.func_70904_g(false);
                this.func_70908_e(true);
                this.field_70170_p.func_72960_a((Entity)this, (byte)7);
                String healthMessage = new String();
                healthMessage = String.format("I have %d health. Thanks for asking!", this.getBoyfriendHealth());
                par1EntityPlayer.func_146105_b((IChatComponent)new ChatComponentText(healthMessage));
            }
            return true;
        }
        return super.func_70085_c(par1EntityPlayer);
    }

    public boolean isWheat(ItemStack par1ItemStack) {
        return par1ItemStack != null && (par1ItemStack.func_77973_b() == Items.field_151083_be || par1ItemStack.func_77973_b() == OreSpawnMain.MyPeacock);
    }

    protected boolean func_70692_ba() {
        return false;
    }

    protected String func_70639_aQ() {
        if (this.func_70906_o() || this.voice_enable == 0) {
            return null;
        }
        if (OreSpawnMain.bro_mode != 0 && this.field_70170_p.field_73012_v.nextInt(2) == 1) {
            return null;
        }
        if (this.field_70170_p.field_73012_v.nextInt(11) == 1) {
            EntityLivingBase victim = this.func_70638_az();
            if (victim != null) {
                return null;
            }
            if (this.func_70090_H() || this.func_70058_J()) {
                return "orespawn:b_water";
            }
            if (this.field_70170_p.field_73012_v.nextInt(4) != 0) {
                if (this.field_70163_u < 60.0) {
                    return null;
                }
                if (this.field_70170_p.func_72911_I()) {
                    return "orespawn:b_thunder";
                }
                if (this.field_70170_p.func_72896_J()) {
                    return "orespawn:b_rain";
                }
                if (!this.field_70170_p.func_72935_r() && this.field_70170_p.func_72937_j((int)this.field_70165_t, (int)this.field_70163_u, (int)this.field_70161_v)) {
                    if (this.field_70170_p.field_73012_v.nextInt(3) == 0) {
                        return "orespawn:b_dark";
                    }
                    return null;
                }
            }
            if (this.func_70909_n()) {
                if ((float)this.mygetMaxHealth() > this.func_110143_aJ()) {
                    return "orespawn:b_hurt";
                }
                if (OreSpawnMain.bro_mode != 0) {
                    return "orespawn:bb_happy";
                }
                return "orespawn:b_happy";
            }
            return null;
        }
        return null;
    }

    protected String func_70621_aR() {
        if (this.voice_enable == 0) {
            return null;
        }
        if (OreSpawnMain.bro_mode != 0 && this.field_70170_p.field_73012_v.nextInt(2) == 1) {
            return null;
        }
        return "orespawn:b_ow";
    }

    protected String func_70673_aS() {
        if (OreSpawnMain.bro_mode != 0) {
            return null;
        }
        return this.func_70909_n() ? "orespawn:b_death_boyfriend" : "orespawn:b_death_single";
    }

    protected float func_70599_aP() {
        return 0.3f;
    }

    protected Item func_146068_u() {
        return Item.func_150898_a((Block)Blocks.field_150328_O);
    }

    protected void func_70628_a(boolean par1, int par2) {
        int var3 = 0;
        if (this.func_70909_n()) {
            var3 = this.field_70146_Z.nextInt(5);
            var3 += 2;
            for (int var4 = 0; var4 < var3; ++var4) {
                this.func_145779_a(Item.func_150898_a((Block)Blocks.field_150328_O), 1);
            }
        }
        Item v6 = OreSpawnMain.MyItemGameController;
        var3 = this.field_70170_p.field_73012_v.nextInt(26);
        var3 += 10;
        for (int var4 = 0; var4 < var3; ++var4) {
            this.func_145779_a(v6, 1);
        }
        if (this.func_70909_n()) {
            ItemStack var5 = this.getCurrentEquippedItem();
            if (var5 != null && var5.field_77994_a > 0) {
                this.func_145779_a(var5.func_77973_b(), var5.field_77994_a);
            }
            if ((var5 = this.func_71124_b(1)) != null && var5.field_77994_a > 0) {
                this.func_145779_a(var5.func_77973_b(), var5.field_77994_a);
            }
            if ((var5 = this.func_71124_b(2)) != null && var5.field_77994_a > 0) {
                this.func_145779_a(var5.func_77973_b(), var5.field_77994_a);
            }
            if ((var5 = this.func_71124_b(3)) != null && var5.field_77994_a > 0) {
                this.func_145779_a(var5.func_77973_b(), var5.field_77994_a);
            }
            if ((var5 = this.func_71124_b(4)) != null && var5.field_77994_a > 0) {
                this.func_145779_a(var5.func_77973_b(), var5.field_77994_a);
            }
        }
    }

    public void attackEntityWithRangedAttack(EntityLivingBase par1EntityLiving) {
        ItemStack it = null;
        if (this.field_82175_bq) {
            return;
        }
        it = this.getCurrentEquippedItem();
        if (it != null && it.func_77973_b() == OreSpawnMain.MyUltimateBow) {
            int var10;
            UltimateArrow var8 = new UltimateArrow(this.field_70170_p, (EntityLiving)this, par1EntityLiving, 2.0f, 10.0f);
            if (this.field_70170_p.field_73012_v.nextInt(4) == 1) {
                var8.func_70243_d(true);
            }
            if ((var10 = EnchantmentHelper.func_77506_a((int)Enchantment.field_77344_u.field_77352_x, (ItemStack)it)) > 0) {
                var8.func_70240_a(var10);
            }
            if (EnchantmentHelper.func_77506_a((int)Enchantment.field_77343_v.field_77352_x, (ItemStack)it) > 0) {
                var8.func_70015_d(100);
            }
            it.func_77972_a(1, (EntityLivingBase)this);
            this.field_70170_p.func_72956_a((Entity)this, "random.bow", 1.0f, 1.0f / (this.field_70170_p.field_73012_v.nextFloat() * 0.4f + 1.2f) + 0.5f);
            var8.field_70251_a = 2;
            this.field_70170_p.func_72838_d((Entity)var8);
        } else {
            Shoes var2 = new Shoes(this.field_70170_p, (EntityLivingBase)this, 6);
            double var3 = par1EntityLiving.field_70165_t - this.field_70165_t;
            double var5 = par1EntityLiving.field_70163_u + (double)par1EntityLiving.func_70047_e() - 1.1 - var2.field_70163_u;
            double var7 = par1EntityLiving.field_70161_v - this.field_70161_v;
            float var9 = MathHelper.func_76133_a((double)(var3 * var3 + var7 * var7)) * 0.2f;
            var2.func_70186_c(var3, var5 + (double)var9, var7, 1.8f, 4.0f);
            this.field_70170_p.func_72956_a((Entity)this, "random.bow", 0.75f, 1.0f / (this.func_70681_au().nextFloat() * 0.4f + 0.8f));
            this.field_70170_p.func_72838_d((Entity)var2);
        }
        this.func_71038_i();
    }

    public ItemStack getCurrentEquippedItem() {
        return this.func_71124_b(0);
    }

    public void attackTargetEntityWithCurrentItem(Entity par1Entity) {
        ItemStack stack = this.getCurrentEquippedItem();
        if (stack != null) {
            float var2 = 0.0f;
            if (this.func_70644_a(Potion.field_76420_g)) {
                var2 += (float)(3 << this.func_70660_b(Potion.field_76420_g).func_76458_c());
            }
            if (this.func_70644_a(Potion.field_76437_t)) {
                var2 -= (float)(2 << this.func_70660_b(Potion.field_76437_t).func_76458_c());
            }
            int var3 = 0;
            float var4 = (float)this.func_110148_a(SharedMonsterAttributes.field_111264_e).func_111126_e();
            if (par1Entity instanceof EntityLiving) {
                var4 += EnchantmentHelper.func_77512_a((EntityLivingBase)this, (EntityLivingBase)((EntityLivingBase)par1Entity));
                var3 += EnchantmentHelper.func_77507_b((EntityLivingBase)this, (EntityLivingBase)((EntityLiving)par1Entity));
            }
            if (this.func_70051_ag()) {
                ++var3;
            }
            if (var2 > 0.0f || var4 > 0.0f) {
                int var8;
                boolean var6;
                boolean var5;
                boolean bl = var5 = this.field_70143_R > 0.0f && !this.field_70122_E && !this.func_70617_f_() && !this.func_70090_H() && !this.func_70058_J() && !this.func_70644_a(Potion.field_76440_q) && this.field_70154_o == null && par1Entity instanceof EntityLiving;
                if (var5) {
                    var2 += (float)this.field_70170_p.field_73012_v.nextInt((int)var2 / 2 + 2);
                }
                if ((var6 = par1Entity.func_70097_a(DamageSource.func_76358_a((EntityLivingBase)this), var2 += var4)) && var3 > 0) {
                    par1Entity.func_70024_g((double)(-MathHelper.func_76126_a((float)(this.field_70177_z * (float)Math.PI / 180.0f)) * (float)var3 * 0.5f), 0.1, (double)(MathHelper.func_76134_b((float)(this.field_70177_z * (float)Math.PI / 180.0f)) * (float)var3 * 0.5f));
                    this.field_70159_w *= 0.6;
                    this.field_70179_y *= 0.6;
                    this.func_70031_b(false);
                }
                ItemStack var7 = this.getCurrentEquippedItem();
                if (par1Entity instanceof EntityLiving && (var8 = EnchantmentHelper.func_77506_a((int)Enchantment.field_77334_n.field_77352_x, (ItemStack)var7)) > 0 && var6) {
                    par1Entity.func_70015_d(var8 * 4);
                }
            }
        }
    }

    protected float func_70647_i() {
        return (float)(this.voice - 5) * 0.02f + 1.0f;
    }

    public EntityAgeable func_90011_a(EntityAgeable var1) {
        return null;
    }

    public void func_82196_d(EntityLivingBase entityliving, float f) {
        this.attackEntityWithRangedAttack(entityliving);
    }

    public boolean func_70097_a(DamageSource par1DamageSource, float par2) {
        boolean ret = false;
        float p2 = par2;
        if (p2 > 10.0f) {
            p2 = 10.0f;
        }
        if (!par1DamageSource.func_76355_l().equals("cactus")) {
            ret = super.func_70097_a(par1DamageSource, p2);
        }
        return ret;
    }

    public boolean func_70601_bi() {
        for (int k = -3; k < 3; ++k) {
            for (int j = -3; j < 3; ++j) {
                for (int i = 0; i < 5; ++i) {
                    Block bid = this.field_70170_p.func_147439_a((int)this.field_70165_t + j, (int)this.field_70163_u + i, (int)this.field_70161_v + k);
                    if (bid != Blocks.field_150474_ac) continue;
                    TileEntityMobSpawner tileentitymobspawner = null;
                    tileentitymobspawner = (TileEntityMobSpawner)this.field_70170_p.func_147438_o((int)this.field_70165_t + j, (int)this.field_70163_u + i, (int)this.field_70161_v + k);
                    String s = tileentitymobspawner.func_145881_a().func_98276_e();
                    if (s == null || !s.equals("Boyfriend")) continue;
                    return true;
                }
            }
        }
        return super.func_70601_bi();
    }
}

