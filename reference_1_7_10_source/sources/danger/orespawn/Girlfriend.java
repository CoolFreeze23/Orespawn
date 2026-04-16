/*
 * Decompiled with CFR 0.152.
 */
package danger.orespawn;

import danger.orespawn.Boyfriend;
import danger.orespawn.Elevator;
import danger.orespawn.ItemOreSpawnArmor;
import danger.orespawn.MyEntityAIDance;
import danger.orespawn.MyEntityAIFollowOwner;
import danger.orespawn.MyEntityAIJealousy;
import danger.orespawn.MyEntityAINearestAttackableTarget;
import danger.orespawn.MyEntityAIWander;
import danger.orespawn.MyValentineTarget;
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
import net.minecraft.entity.item.EntityItem;
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

public class Girlfriend
extends EntityTameable
implements IRangedAttackMob {
    public int which_girl = this.field_70146_Z.nextInt(41);
    public int which_wet_girl = this.field_70146_Z.nextInt(18);
    public int wet_count = 0;
    private int auto_heal = 200;
    private int force_sync = 50;
    private int fight_sound_ticker = 0;
    private int taunt_sound_ticker = 0;
    private int had_target = 0;
    private int voice = this.field_70146_Z.nextInt(10);
    private int is_princess = 0;
    public MyEntityAIDance Dance = null;
    private float moveSpeed = 0.3f;
    private int voice_enable = 1;
    public int passenger = 0;
    public int feelingBetter = 0;
    private static final ResourceLocation DryTexture0 = new ResourceLocation("orespawn", "girlfriend0.png");
    private static final ResourceLocation DryTexture1 = new ResourceLocation("orespawn", "girlfriend1.png");
    private static final ResourceLocation DryTexture2 = new ResourceLocation("orespawn", "girlfriend2.png");
    private static final ResourceLocation DryTexture3 = new ResourceLocation("orespawn", "girlfriend3.png");
    private static final ResourceLocation DryTexture4 = new ResourceLocation("orespawn", "girlfriend4.png");
    private static final ResourceLocation DryTexture5 = new ResourceLocation("orespawn", "girlfriend5.png");
    private static final ResourceLocation DryTexture6 = new ResourceLocation("orespawn", "girlfriend6.png");
    private static final ResourceLocation DryTexture7 = new ResourceLocation("orespawn", "girlfriend7.png");
    private static final ResourceLocation DryTexture8 = new ResourceLocation("orespawn", "girlfriend8.png");
    private static final ResourceLocation DryTexture9 = new ResourceLocation("orespawn", "girlfriend9.png");
    private static final ResourceLocation DryTexture10 = new ResourceLocation("orespawn", "girlfriend10.png");
    private static final ResourceLocation DryTexture11 = new ResourceLocation("orespawn", "girlfriend11.png");
    private static final ResourceLocation DryTexture12 = new ResourceLocation("orespawn", "girlfriend12.png");
    private static final ResourceLocation DryTexture13 = new ResourceLocation("orespawn", "girlfriend13.png");
    private static final ResourceLocation DryTexture14 = new ResourceLocation("orespawn", "girlfriend14.png");
    private static final ResourceLocation DryTexture15 = new ResourceLocation("orespawn", "girlfriend15.png");
    private static final ResourceLocation DryTexture16 = new ResourceLocation("orespawn", "girlfriend16.png");
    private static final ResourceLocation DryTexture17 = new ResourceLocation("orespawn", "girlfriend17.png");
    private static final ResourceLocation DryTexture18 = new ResourceLocation("orespawn", "girlfriend18.png");
    private static final ResourceLocation DryTexture19 = new ResourceLocation("orespawn", "girlfriend19.png");
    private static final ResourceLocation DryTexture20 = new ResourceLocation("orespawn", "girlfriend20.png");
    private static final ResourceLocation DryTexture21 = new ResourceLocation("orespawn", "girlfriend21.png");
    private static final ResourceLocation DryTexture22 = new ResourceLocation("orespawn", "girlfriend22.png");
    private static final ResourceLocation DryTexture23 = new ResourceLocation("orespawn", "girlfriend23.png");
    private static final ResourceLocation DryTexture24 = new ResourceLocation("orespawn", "girlfriend24.png");
    private static final ResourceLocation DryTexture25 = new ResourceLocation("orespawn", "girlfriend25.png");
    private static final ResourceLocation DryTexture26 = new ResourceLocation("orespawn", "girlfriend26.png");
    private static final ResourceLocation DryTexture27 = new ResourceLocation("orespawn", "girlfriend27.png");
    private static final ResourceLocation DryTexture28 = new ResourceLocation("orespawn", "girlfriend28.png");
    private static final ResourceLocation DryTexture29 = new ResourceLocation("orespawn", "girlfriend29.png");
    private static final ResourceLocation DryTexture30 = new ResourceLocation("orespawn", "girlfriend30.png");
    private static final ResourceLocation DryTexture31 = new ResourceLocation("orespawn", "girlfriend31.png");
    private static final ResourceLocation DryTexture32 = new ResourceLocation("orespawn", "girlfriend32.png");
    private static final ResourceLocation DryTexture33 = new ResourceLocation("orespawn", "girlfriend33.png");
    private static final ResourceLocation DryTexture34 = new ResourceLocation("orespawn", "girlfriend34.png");
    private static final ResourceLocation DryTexture35 = new ResourceLocation("orespawn", "girlfriend35.png");
    private static final ResourceLocation DryTexture36 = new ResourceLocation("orespawn", "girlfriend36.png");
    private static final ResourceLocation DryTexture37 = new ResourceLocation("orespawn", "girlfriend37.png");
    private static final ResourceLocation DryTexture38 = new ResourceLocation("orespawn", "girlfriend38.png");
    private static final ResourceLocation DryTexture39 = new ResourceLocation("orespawn", "girlfriend39.png");
    private static final ResourceLocation DryTexture40 = new ResourceLocation("orespawn", "girlfriend40.png");
    private static final ResourceLocation ValentineTexture = new ResourceLocation("orespawn", "girlfriendv.png");
    private static final ResourceLocation WetTexture0 = new ResourceLocation("orespawn", "bikini0.png");
    private static final ResourceLocation WetTexture1 = new ResourceLocation("orespawn", "bikini1.png");
    private static final ResourceLocation WetTexture2 = new ResourceLocation("orespawn", "bikini2.png");
    private static final ResourceLocation WetTexture3 = new ResourceLocation("orespawn", "bikini3.png");
    private static final ResourceLocation WetTexture4 = new ResourceLocation("orespawn", "bikini4.png");
    private static final ResourceLocation WetTexture5 = new ResourceLocation("orespawn", "bikini5.png");
    private static final ResourceLocation WetTexture6 = new ResourceLocation("orespawn", "bikini6.png");
    private static final ResourceLocation WetTexture7 = new ResourceLocation("orespawn", "bikini7.png");
    private static final ResourceLocation WetTexture8 = new ResourceLocation("orespawn", "bikini8.png");
    private static final ResourceLocation WetTexture9 = new ResourceLocation("orespawn", "bikini9.png");
    private static final ResourceLocation WetTexture10 = new ResourceLocation("orespawn", "bikini10.png");
    private static final ResourceLocation WetTexture11 = new ResourceLocation("orespawn", "bikini11.png");
    private static final ResourceLocation WetTexture12 = new ResourceLocation("orespawn", "bikini12.png");
    private static final ResourceLocation WetTexture13 = new ResourceLocation("orespawn", "bikini13.png");
    private static final ResourceLocation WetTexture14 = new ResourceLocation("orespawn", "bikini14.png");
    private static final ResourceLocation WetTexture15 = new ResourceLocation("orespawn", "bikini15.png");
    private static final ResourceLocation WetTexture16 = new ResourceLocation("orespawn", "bikini16.png");
    private static final ResourceLocation WetTexture17 = new ResourceLocation("orespawn", "bikini17.png");
    private static final ResourceLocation PrincessTexture1 = new ResourceLocation("orespawn", "FrogPrincess.png");
    private static final ResourceLocation PrincessTexture2 = new ResourceLocation("orespawn", "FrogPrincess2.png");

    public Girlfriend(World par1World) {
        super(par1World);
        this.func_70105_a(0.5f, 1.6f);
        if (OreSpawnMain.valentines_day != 0) {
            this.func_70105_a(2.5f, 8.0f);
        }
        this.field_70178_ae = true;
        this.field_70174_ab = 100;
        this.func_70661_as().func_75491_a(false);
        this.func_70904_g(false);
        this.field_70714_bg.func_75776_a(1, (EntityAIBase)new MyEntityAIFollowOwner((EntityTameable)this, 1.4f, 12.0f, 1.5f));
        this.field_70714_bg.func_75776_a(2, (EntityAIBase)new EntityAITempt((EntityCreature)this, 1.25, Item.func_150898_a((Block)Blocks.field_150328_O), false));
        this.Dance = new MyEntityAIDance(this);
        this.field_70714_bg.func_75776_a(3, (EntityAIBase)this.Dance);
        this.field_70714_bg.func_75776_a(4, (EntityAIBase)new EntityAIArrowAttack((IRangedAttackMob)this, 1.25, 20, 10.0f));
        this.field_70714_bg.func_75776_a(5, (EntityAIBase)new EntityAISwimming((EntityLiving)this));
        this.field_70714_bg.func_75776_a(6, (EntityAIBase)new EntityAIPanic((EntityCreature)this, 1.5));
        this.field_70714_bg.func_75776_a(7, (EntityAIBase)new EntityAIWatchClosest((EntityLiving)this, EntityPlayer.class, 6.0f));
        this.field_70714_bg.func_75776_a(8, (EntityAIBase)new MyEntityAIWander((EntityCreature)this, 0.75f));
        this.field_70714_bg.func_75776_a(9, (EntityAIBase)new EntityAILookIdle((EntityLiving)this));
        this.field_70714_bg.func_75776_a(10, (EntityAIBase)new EntityAIOpenDoor((EntityLiving)this, true));
        this.field_70714_bg.func_75776_a(11, (EntityAIBase)new EntityAIMoveIndoors((EntityCreature)this));
        this.field_70715_bh.func_75776_a(1, (EntityAIBase)new MyValentineTarget((EntityLiving)this, EntityPlayer.class, 16.0f, 0, true, true));
        this.field_70715_bh.func_75776_a(2, (EntityAIBase)new MyValentineTarget((EntityLiving)this, Boyfriend.class, 16.0f, 0, true, true));
        if (OreSpawnMain.PlayNicely == 0) {
            this.field_70715_bh.func_75776_a(2, (EntityAIBase)new MyEntityAINearestAttackableTarget((EntityLiving)this, EntityCreeper.class, 20.0f, 0, true, true, IMob.field_82192_a));
        }
        if (OreSpawnMain.PlayNicely == 0) {
            this.field_70715_bh.func_75776_a(3, (EntityAIBase)new MyEntityAINearestAttackableTarget((EntityLiving)this, EntityLiving.class, 15.0f, 0, true, true, IMob.field_82192_a));
        }
        if (OreSpawnMain.PlayNicely == 0) {
            this.field_70715_bh.func_75776_a(4, (EntityAIBase)new MyEntityAIJealousy((EntityTameable)this, Girlfriend.class, 6.0f, 5, true));
        }
        if (OreSpawnMain.PlayNicely == 0) {
            this.field_70715_bh.func_75776_a(5, (EntityAIBase)new MyEntityAIJealousy((EntityTameable)this, Girlfriend.class, 3.0f, 15, true));
        }
        this.field_70728_aV = 0;
    }

    protected void func_70088_a() {
        super.func_70088_a();
        this.which_girl = this.field_70146_Z.nextInt(41);
        this.field_70180_af.func_75682_a(20, (Object)this.which_girl);
        this.wet_count = 0;
        this.which_wet_girl = this.field_70146_Z.nextInt(18);
        this.field_70180_af.func_75682_a(22, (Object)this.which_wet_girl);
        this.voice = this.field_70146_Z.nextInt(10);
        this.field_70180_af.func_75682_a(21, (Object)this.voice);
        this.field_70180_af.func_75682_a(23, (Object)this.voice_enable);
        this.field_70180_af.func_75682_a(24, (Object)this.is_princess);
        this.field_70180_af.func_75682_a(25, (Object)this.feelingBetter);
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
        par1NBTTagCompound.func_74768_a("IsPrincess", this.field_70180_af.func_75679_c(24));
        par1NBTTagCompound.func_74768_a("feelingBetter", this.field_70180_af.func_75679_c(25));
    }

    public void func_70037_a(NBTTagCompound par1NBTTagCompound) {
        super.func_70037_a(par1NBTTagCompound);
        this.which_girl = par1NBTTagCompound.func_74762_e("GirlType");
        this.setTameSkin(this.which_girl);
        this.which_wet_girl = par1NBTTagCompound.func_74762_e("WetGirlType");
        this.setWetTameSkin(this.which_wet_girl);
        this.voice = par1NBTTagCompound.func_74762_e("GirlVoice");
        this.field_70180_af.func_75692_b(21, (Object)this.voice);
        this.voice_enable = par1NBTTagCompound.func_74762_e("GirlVoiceEnable");
        this.field_70180_af.func_75692_b(23, (Object)this.voice_enable);
        this.is_princess = par1NBTTagCompound.func_74762_e("IsPrincess");
        this.field_70180_af.func_75692_b(24, (Object)this.is_princess);
        this.feelingBetter = par1NBTTagCompound.func_74762_e("feelingBetter");
        this.field_70180_af.func_75692_b(25, (Object)this.feelingBetter);
        if (OreSpawnMain.valentines_day != 0 && this.feelingBetter != 0) {
            this.func_70105_a(0.5f, 1.6f);
        }
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
        if (this.field_70170_p.field_73012_v.nextInt(200) == 1) {
            this.func_70624_b(null);
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
                                    this.field_70170_p.func_72956_a((Entity)this, "orespawn:o_fight", 0.5f, this.func_70647_i());
                                }
                                this.fight_sound_ticker = 3;
                            }
                            this.had_target = 1;
                        }
                    } else if (this.func_70032_d((Entity)victim) < 7.0f && stack.func_77973_b() != OreSpawnMain.MyUltimateBow) {
                        --this.taunt_sound_ticker;
                        if (this.taunt_sound_ticker <= 0) {
                            if (!this.field_70170_p.field_72995_K && this.voice_enable != 0) {
                                this.field_70170_p.func_72956_a((Entity)this, "orespawn:o_taunt", 0.5f, this.func_70647_i());
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
                        this.field_70170_p.func_72956_a((Entity)this, "orespawn:o_woohoo", 0.4f, this.func_70647_i());
                    }
                }
            }
        }
    }

    public void setPrincess(int par1) {
        this.is_princess = par1;
    }

    public ResourceLocation getTexture() {
        if (OreSpawnMain.valentines_day != 0 && this.feelingBetter == 0) {
            return ValentineTexture;
        }
        if (this.wet_count <= 0) {
            int txture = this.getTameSkin();
            if (this.is_princess == 1) {
                return PrincessTexture1;
            }
            if (this.is_princess == 2) {
                return PrincessTexture2;
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
            if (txture == 28) {
                return DryTexture28;
            }
            if (txture == 29) {
                return DryTexture29;
            }
            if (txture == 30) {
                return DryTexture30;
            }
            if (txture == 31) {
                return DryTexture31;
            }
            if (txture == 32) {
                return DryTexture32;
            }
            if (txture == 33) {
                return DryTexture33;
            }
            if (txture == 34) {
                return DryTexture34;
            }
            if (txture == 35) {
                return DryTexture35;
            }
            if (txture == 36) {
                return DryTexture36;
            }
            if (txture == 37) {
                return DryTexture37;
            }
            if (txture == 38) {
                return DryTexture38;
            }
            if (txture == 39) {
                return DryTexture39;
            }
            if (txture == 40) {
                return DryTexture40;
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
        this.which_girl = par1;
    }

    public int getWetTameSkin() {
        return this.field_70180_af.func_75679_c(22);
    }

    public void setWetTameSkin(int par1) {
        this.field_70180_af.func_75692_b(22, (Object)par1);
        this.which_wet_girl = par1;
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
        if (OreSpawnMain.valentines_day != 0 && this.feelingBetter == 0) {
            return 800;
        }
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
            if (this.mygetMaxHealth() > this.getGirlfriendHealth()) {
                this.func_70691_i(1.0f);
            }
            this.auto_heal = 100;
        }
        --this.force_sync;
        if (this.force_sync <= 0) {
            this.force_sync = 20;
            if (!this.field_70170_p.field_72995_K) {
                this.field_70180_af.func_75692_b(21, (Object)this.voice);
                this.field_70180_af.func_75692_b(23, (Object)this.voice_enable);
                this.field_70180_af.func_75692_b(24, (Object)this.is_princess);
                this.field_70180_af.func_75692_b(25, (Object)this.feelingBetter);
                this.func_70904_g(this.func_70906_o());
            } else {
                this.voice = this.getVoice();
                this.voice_enable = this.field_70180_af.func_75679_c(23);
                int nowfeeling = this.field_70180_af.func_75679_c(25);
                if (nowfeeling != this.feelingBetter && nowfeeling != 0) {
                    this.feelingBetter = nowfeeling;
                    this.func_70105_a(0.5f, 1.6f);
                }
            }
        }
    }

    public int getGirlfriendHealth() {
        return (int)this.func_110143_aJ();
    }

    public boolean func_70085_c(EntityPlayer par1EntityPlayer) {
        ItemStack var2 = par1EntityPlayer.field_71071_by.func_70448_g();
        if (var2 != null && var2.field_77994_a <= 0) {
            par1EntityPlayer.field_71071_by.func_70299_a(par1EntityPlayer.field_71071_by.field_70461_c, (ItemStack)null);
            var2 = null;
        }
        if (var2 != null && (var2.func_77973_b() == Item.func_150898_a((Block)Blocks.field_150328_O) || var2.func_77973_b() == Item.func_150898_a((Block)OreSpawnMain.CrystalFlowerRedBlock)) && par1EntityPlayer.func_70068_e((Entity)this) < 16.0) {
            if (!this.func_70909_n()) {
                if (!this.field_70170_p.field_72995_K) {
                    if (this.field_70146_Z.nextInt(3) == 0) {
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
        if (this.func_70909_n() && var2 != null && (var2.func_77973_b() == Item.func_150898_a((Block)Blocks.field_150327_N) || var2.func_77973_b() == Item.func_150898_a((Block)OreSpawnMain.CrystalFlowerYellowBlock)) && par1EntityPlayer.func_70068_e((Entity)this) < 16.0 && this.func_152114_e((EntityLivingBase)par1EntityPlayer)) {
            if (!this.field_70170_p.field_72995_K) {
                if (this.wet_count > 0 || this.func_70090_H() || this.func_70058_J()) {
                    ++this.which_wet_girl;
                    if (this.which_wet_girl > 17) {
                        this.which_wet_girl = 0;
                    }
                    this.setWetTameSkin(this.which_wet_girl);
                    this.field_70170_p.func_72960_a((Entity)this, (byte)7);
                    if (this.func_70090_H() || this.func_70058_J()) {
                        this.wet_count = 500;
                    }
                } else {
                    ++this.which_girl;
                    if (this.which_girl > 40) {
                        this.which_girl = 0;
                    }
                    this.setTameSkin(this.which_girl);
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
                        if (itm == OreSpawnMain.CrystalPinkHelmet || itm == OreSpawnMain.TigersEyeHelmet) {
                            v4 = this.func_71124_b(4);
                            this.func_70062_b(4, var2);
                            this.func_70062_b(0, v4);
                        }
                        if (itm == OreSpawnMain.CrystalPinkBody || itm == OreSpawnMain.TigersEyeBody) {
                            v4 = this.func_71124_b(3);
                            this.func_70062_b(3, var2);
                            this.func_70062_b(0, v4);
                        }
                        if (itm == OreSpawnMain.CrystalPinkLegs || itm == OreSpawnMain.TigersEyeLegs) {
                            v4 = this.func_71124_b(2);
                            this.func_70062_b(2, var2);
                            this.func_70062_b(0, v4);
                        }
                        if (itm == OreSpawnMain.CrystalPinkBoots || itm == OreSpawnMain.TigersEyeBoots) {
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
                healthMessage = String.format("I have %d health. Thank you for asking! xoxo", this.getGirlfriendHealth());
                par1EntityPlayer.func_146105_b((IChatComponent)new ChatComponentText(healthMessage));
            }
            return true;
        }
        return super.func_70085_c(par1EntityPlayer);
    }

    public boolean isWheat(ItemStack par1ItemStack) {
        return par1ItemStack != null && par1ItemStack.func_77973_b() == Item.func_150898_a((Block)Blocks.field_150328_O);
    }

    protected boolean func_70692_ba() {
        return false;
    }

    protected String func_70639_aQ() {
        if (this.func_70906_o() || this.voice_enable == 0) {
            return null;
        }
        if (this.Dance.is_dancing != 0) {
            return null;
        }
        if (this.field_70146_Z.nextInt(11) == 1) {
            EntityLivingBase victim = this.func_70638_az();
            if (victim != null) {
                return null;
            }
            if (this.func_70090_H() || this.func_70058_J()) {
                return "orespawn:o_water";
            }
            if (this.field_70146_Z.nextInt(4) != 0) {
                if (this.field_70163_u < 60.0) {
                    return null;
                }
                if (this.field_70170_p.func_72911_I()) {
                    return "orespawn:o_thunder";
                }
                if (this.field_70170_p.func_72896_J()) {
                    return "orespawn:o_rain";
                }
                if (!this.field_70170_p.func_72935_r() && this.field_70170_p.func_72937_j((int)this.field_70165_t, (int)this.field_70163_u, (int)this.field_70161_v)) {
                    if (this.field_70170_p.field_73012_v.nextInt(3) == 0) {
                        return "orespawn:o_dark";
                    }
                    return null;
                }
            }
            if (this.func_70909_n()) {
                if ((float)this.mygetMaxHealth() > this.func_110143_aJ() || OreSpawnMain.valentines_day != 0 && this.feelingBetter == 0) {
                    return "orespawn:o_hurt";
                }
                return "orespawn:o_happy";
            }
            return null;
        }
        return null;
    }

    protected String func_70621_aR() {
        if (this.voice_enable == 0) {
            return null;
        }
        return "orespawn:o_ow";
    }

    protected String func_70673_aS() {
        return this.func_70909_n() ? "orespawn:o_death_girlfriend" : "orespawn:o_death_single";
    }

    protected float func_70599_aP() {
        return 0.3f;
    }

    protected Item func_146068_u() {
        return Item.func_150898_a((Block)Blocks.field_150328_O);
    }

    private void dropItemRand(Item index, int par1) {
        EntityItem var3 = new EntityItem(this.field_70170_p, this.field_70165_t + (double)OreSpawnMain.OreSpawnRand.nextInt(4) - (double)OreSpawnMain.OreSpawnRand.nextInt(4), this.field_70163_u + 1.0, this.field_70161_v + (double)OreSpawnMain.OreSpawnRand.nextInt(4) - (double)OreSpawnMain.OreSpawnRand.nextInt(4), new ItemStack(index, par1, 0));
        this.field_70170_p.func_72838_d((Entity)var3);
    }

    protected void func_70628_a(boolean par1, int par2) {
        int var4;
        int var3 = 0;
        if (this.func_70909_n()) {
            var3 = this.field_70146_Z.nextInt(5);
            var3 += 2;
            for (int var42 = 0; var42 < var3; ++var42) {
                this.func_145779_a(Item.func_150898_a((Block)Blocks.field_150328_O), 1);
            }
        }
        Item v6 = OreSpawnMain.MyItemShoes;
        Item v7 = OreSpawnMain.MyItemShoes_1;
        Item v8 = OreSpawnMain.MyItemShoes_2;
        Item v9 = OreSpawnMain.MyItemShoes_3;
        var3 = this.field_70146_Z.nextInt(16);
        var3 += 4;
        for (var4 = 0; var4 < var3; ++var4) {
            this.func_145779_a(v6, 1);
        }
        var3 = this.field_70146_Z.nextInt(16);
        var3 += 4;
        for (var4 = 0; var4 < var3; ++var4) {
            this.func_145779_a(v7, 1);
        }
        var3 = this.field_70146_Z.nextInt(16);
        var3 += 4;
        for (var4 = 0; var4 < var3; ++var4) {
            this.func_145779_a(v8, 1);
        }
        var3 = this.field_70146_Z.nextInt(16);
        var3 += 4;
        for (var4 = 0; var4 < var3; ++var4) {
            this.func_145779_a(v9, 1);
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
            Shoes var2 = new Shoes(this.field_70170_p, (EntityLivingBase)this, 2 + this.field_70146_Z.nextInt(4));
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
                    var2 += (float)this.field_70146_Z.nextInt((int)var2 / 2 + 2);
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
            Item it;
            EntityPlayer eb;
            ItemStack ist;
            Entity e;
            if (par1DamageSource.func_76355_l().equals("inWall") && OreSpawnMain.valentines_day != 0) {
                return ret;
            }
            if (OreSpawnMain.valentines_day != 0 && !this.field_70170_p.field_72995_K && this.feelingBetter == 0 && (e = par1DamageSource.func_76346_g()) != null && e instanceof EntityPlayer && (ist = (eb = (EntityPlayer)e).func_71045_bC()) != null && (it = ist.func_77973_b()) == OreSpawnMain.MyRoseSword) {
                if (this.field_70170_p.field_73012_v.nextInt(4) == 1) {
                    this.feelingBetter = 1;
                    this.func_70624_b(null);
                    this.func_70105_a(0.5f, 1.6f);
                    this.func_110148_a(SharedMonsterAttributes.field_111267_a).func_111128_a((double)this.mygetMaxHealth());
                    int morelove = this.field_70170_p.field_73012_v.nextInt(10);
                    for (int i = 0; i < 10 + morelove; ++i) {
                        this.dropItemRand(OreSpawnMain.MyLove, 1);
                    }
                } else {
                    this.dropItemRand(OreSpawnMain.MyLove, 1);
                }
            }
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
                    if (s == null || !s.equals("Girlfriend")) continue;
                    return true;
                }
            }
        }
        return super.func_70601_bi();
    }
}

