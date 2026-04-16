/*
 * Decompiled with CFR 0.152.
 */
package danger.orespawn;

import danger.orespawn.MyEntityAIWanderALot;
import danger.orespawn.OreSpawnMain;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIAvoidEntity;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAIMate;
import net.minecraft.entity.ai.EntityAIPanic;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class EasterBunny
extends EntityAnimal {
    private float moveSpeed = 0.45f;

    public EasterBunny(World par1World) {
        super(par1World);
        this.func_70105_a(0.5f, 0.75f);
        this.moveSpeed = 0.45f;
        this.field_70174_ab = 100;
        this.field_70728_aV = 5;
        this.func_70661_as().func_75491_a(true);
        this.field_70714_bg.func_75776_a(0, (EntityAIBase)new EntityAISwimming((EntityLiving)this));
        this.field_70714_bg.func_75776_a(1, (EntityAIBase)new EntityAIMate((EntityAnimal)this, 1.0));
        this.field_70714_bg.func_75776_a(2, (EntityAIBase)new EntityAIAvoidEntity((EntityCreature)this, EntityMob.class, 8.0f, 1.0, (double)1.4f));
        this.field_70714_bg.func_75776_a(3, (EntityAIBase)new EntityAIAvoidEntity((EntityCreature)this, EntityPlayer.class, 8.0f, 1.0, (double)1.4f));
        this.field_70714_bg.func_75776_a(4, (EntityAIBase)new EntityAIPanic((EntityCreature)this, 1.5));
        this.field_70714_bg.func_75776_a(5, (EntityAIBase)new EntityAIWatchClosest((EntityLiving)this, EntityLiving.class, 8.0f));
        this.field_70714_bg.func_75776_a(6, (EntityAIBase)new MyEntityAIWanderALot((EntityCreature)this, 16, 1.0));
        this.field_70714_bg.func_75776_a(7, (EntityAIBase)new EntityAILookIdle((EntityLiving)this));
    }

    protected void func_110147_ax() {
        super.func_110147_ax();
        this.func_110148_a(SharedMonsterAttributes.field_111267_a).func_111128_a((double)this.mygetMaxHealth());
        this.func_110148_a(SharedMonsterAttributes.field_111263_d).func_111128_a((double)this.moveSpeed);
        this.func_110140_aT().func_111150_b(SharedMonsterAttributes.field_111264_e);
        this.func_110148_a(SharedMonsterAttributes.field_111264_e).func_111128_a(8.0);
    }

    protected void func_70088_a() {
        super.func_70088_a();
    }

    public void func_70071_h_() {
        this.func_110148_a(SharedMonsterAttributes.field_111263_d).func_111128_a((double)this.moveSpeed);
        super.func_70071_h_();
    }

    public boolean func_70601_bi() {
        if (this.field_70163_u < 50.0) {
            return false;
        }
        if (!this.field_70170_p.func_72935_r()) {
            return false;
        }
        EasterBunny target = null;
        target = (EasterBunny)this.field_70170_p.func_72857_a(EasterBunny.class, this.field_70121_D.func_72314_b(32.0, 8.0, 32.0), (Entity)this);
        return target == null;
    }

    public boolean func_70650_aV() {
        return true;
    }

    public boolean func_70648_aU() {
        return false;
    }

    public int mygetMaxHealth() {
        return 10;
    }

    protected String func_70639_aQ() {
        return null;
    }

    protected String func_70621_aR() {
        return "orespawn:duck_hurt";
    }

    protected String func_70673_aS() {
        return "orespawn:duck_hurt";
    }

    protected float func_70599_aP() {
        return 0.4f;
    }

    protected Item func_146068_u() {
        return Items.field_151076_bf;
    }

    protected void func_70628_a(boolean par1, int par2) {
        int var3 = 0;
        var3 = this.field_70146_Z.nextInt(3);
        var3 += 2;
        for (int var4 = 0; var4 < var3; ++var4) {
            this.func_145779_a(Items.field_151076_bf, 1);
        }
    }

    protected void func_70629_bd() {
        if (this.field_70170_p.field_73012_v.nextInt(200) == 1) {
            this.func_70604_c(null);
        }
        super.func_70629_bd();
        if (this.field_70170_p.field_73012_v.nextInt(600) == 1) {
            this.LayAnEgg(1 + this.field_70170_p.field_73012_v.nextInt(3));
        }
    }

    private ItemStack LayAnEgg(int par1) {
        EntityItem var3 = null;
        int i = 0;
        Item index = null;
        int val = 0;
        ItemStack is = null;
        i = this.field_70170_p.field_73012_v.nextInt(115);
        switch (i) {
            case 5: {
                index = OreSpawnMain.GirlfriendEgg;
                break;
            }
            case 6: {
                index = OreSpawnMain.RedCowEgg;
                break;
            }
            case 7: {
                index = OreSpawnMain.GoldCowEgg;
                break;
            }
            case 8: {
                index = OreSpawnMain.EnchantedCowEgg;
                break;
            }
            case 9: {
                index = OreSpawnMain.MOTHRAEgg;
                break;
            }
            case 10: {
                index = OreSpawnMain.AloEgg;
                break;
            }
            case 11: {
                index = OreSpawnMain.CryoEgg;
                break;
            }
            case 12: {
                index = OreSpawnMain.CamaEgg;
                break;
            }
            case 13: {
                index = OreSpawnMain.VeloEgg;
                break;
            }
            case 14: {
                index = OreSpawnMain.HydroEgg;
                break;
            }
            case 15: {
                index = OreSpawnMain.BasilEgg;
                break;
            }
            case 16: {
                index = OreSpawnMain.DragonflyEgg;
                break;
            }
            case 17: {
                index = OreSpawnMain.EmperorScorpionEgg;
                break;
            }
            case 18: {
                index = OreSpawnMain.ScorpionEgg;
                break;
            }
            case 19: {
                index = OreSpawnMain.CaveFisherEgg;
                break;
            }
            case 20: {
                index = OreSpawnMain.SpyroEgg;
                break;
            }
            case 21: {
                index = OreSpawnMain.BaryonyxEgg;
                break;
            }
            case 22: {
                index = OreSpawnMain.GammaMetroidEgg;
                break;
            }
            case 23: {
                index = OreSpawnMain.CockateilEgg;
                break;
            }
            case 24: {
                index = OreSpawnMain.KyuubiEgg;
                break;
            }
            case 25: {
                index = OreSpawnMain.AlienEgg;
                break;
            }
            case 26: {
                index = OreSpawnMain.AttackSquidEgg;
                break;
            }
            case 27: {
                index = OreSpawnMain.WaterDragonEgg;
                break;
            }
            case 28: {
                index = OreSpawnMain.CephadromeEgg;
                break;
            }
            case 29: {
                index = OreSpawnMain.DragonEgg;
                break;
            }
            case 30: {
                index = OreSpawnMain.KrakenEgg;
                break;
            }
            case 31: {
                index = OreSpawnMain.LizardEgg;
                break;
            }
            case 32: {
                index = OreSpawnMain.BeeEgg;
                break;
            }
            case 33: {
                index = OreSpawnMain.TrooperBugEgg;
                break;
            }
            case 34: {
                index = OreSpawnMain.SpitBugEgg;
                break;
            }
            case 35: {
                index = OreSpawnMain.StinkBugEgg;
                break;
            }
            case 36: {
                index = OreSpawnMain.OstrichEgg;
                break;
            }
            case 37: {
                index = OreSpawnMain.GazelleEgg;
                break;
            }
            case 38: {
                index = OreSpawnMain.ChipmunkEgg;
                break;
            }
            case 39: {
                index = OreSpawnMain.CreepingHorrorEgg;
                break;
            }
            case 40: {
                index = OreSpawnMain.TerribleTerrorEgg;
                break;
            }
            case 41: {
                index = OreSpawnMain.CliffRacerEgg;
                break;
            }
            case 42: {
                index = OreSpawnMain.TriffidEgg;
                break;
            }
            case 43: {
                index = OreSpawnMain.PitchBlackEgg;
                break;
            }
            case 44: {
                index = OreSpawnMain.LurkingTerrorEgg;
                break;
            }
            case 45: {
                index = OreSpawnMain.GodzillaEgg;
                break;
            }
            case 46: {
                index = OreSpawnMain.SmallWormEgg;
                break;
            }
            case 47: {
                index = OreSpawnMain.MediumWormEgg;
                break;
            }
            case 48: {
                index = OreSpawnMain.LargeWormEgg;
                break;
            }
            case 49: {
                index = OreSpawnMain.CassowaryEgg;
                break;
            }
            case 50: {
                index = OreSpawnMain.CloudSharkEgg;
                break;
            }
            case 51: {
                index = OreSpawnMain.GoldFishEgg;
                break;
            }
            case 52: {
                index = OreSpawnMain.LeafMonsterEgg;
                break;
            }
            case 53: {
                index = OreSpawnMain.TshirtEgg;
                break;
            }
            case 54: {
                index = OreSpawnMain.EnderKnightEgg;
                break;
            }
            case 55: {
                index = OreSpawnMain.EnderReaperEgg;
                break;
            }
            case 56: {
                index = OreSpawnMain.BeaverEgg;
                break;
            }
            case 57: {
                index = OreSpawnMain.RotatorEgg;
                break;
            }
            case 58: {
                index = OreSpawnMain.VortexEgg;
                break;
            }
            case 59: {
                index = OreSpawnMain.PeacockEgg;
                break;
            }
            case 60: {
                index = OreSpawnMain.FairyEgg;
                break;
            }
            case 61: {
                index = OreSpawnMain.DungeonBeastEgg;
                break;
            }
            case 62: {
                index = OreSpawnMain.RatEgg;
                break;
            }
            case 63: {
                index = OreSpawnMain.FlounderEgg;
                break;
            }
            case 64: {
                index = OreSpawnMain.WhaleEgg;
                break;
            }
            case 65: {
                index = OreSpawnMain.IrukandjiEgg;
                break;
            }
            case 66: {
                index = OreSpawnMain.SkateEgg;
                break;
            }
            case 67: {
                index = OreSpawnMain.UrchinEgg;
                break;
            }
            case 68: {
                index = OreSpawnMain.Robot1Egg;
                break;
            }
            case 69: {
                index = OreSpawnMain.Robot2Egg;
                break;
            }
            case 70: {
                index = OreSpawnMain.Robot3Egg;
                break;
            }
            case 71: {
                index = OreSpawnMain.Robot4Egg;
                break;
            }
            case 72: {
                index = OreSpawnMain.GhostEgg;
                break;
            }
            case 73: {
                index = OreSpawnMain.GhostSkellyEgg;
                break;
            }
            case 74: {
                index = OreSpawnMain.BrownAntEgg;
                break;
            }
            case 75: {
                index = OreSpawnMain.RedAntEgg;
                break;
            }
            case 76: {
                index = OreSpawnMain.RainbowAntEgg;
                break;
            }
            case 77: {
                index = OreSpawnMain.UnstableAntEgg;
                break;
            }
            case 78: {
                index = OreSpawnMain.TermiteEgg;
                break;
            }
            case 79: {
                index = OreSpawnMain.ButterflyEgg;
                break;
            }
            case 80: {
                index = OreSpawnMain.MothEgg;
                break;
            }
            case 81: {
                index = OreSpawnMain.MosquitoEgg;
                break;
            }
            case 82: {
                index = OreSpawnMain.FireflyEgg;
                break;
            }
            case 83: {
                index = OreSpawnMain.TRexEgg;
                break;
            }
            case 84: {
                index = OreSpawnMain.HerculesEgg;
                break;
            }
            case 85: {
                index = OreSpawnMain.MantisEgg;
                break;
            }
            case 86: {
                index = OreSpawnMain.StinkyEgg;
                break;
            }
            case 87: {
                index = OreSpawnMain.Robot5Egg;
                break;
            }
            case 88: {
                index = OreSpawnMain.CoinEgg;
                break;
            }
            case 89: {
                index = OreSpawnMain.BoyfriendEgg;
                break;
            }
            case 90: {
                index = OreSpawnMain.TheKingEgg;
                break;
            }
            case 91: {
                index = OreSpawnMain.ThePrinceEgg;
                break;
            }
            case 92: {
                index = OreSpawnMain.EasterBunnyEgg;
                break;
            }
            case 93: {
                index = OreSpawnMain.MolenoidEgg;
                break;
            }
            case 94: {
                index = OreSpawnMain.SeaMonsterEgg;
                break;
            }
            case 95: {
                index = OreSpawnMain.SeaViperEgg;
                break;
            }
            case 96: {
                index = OreSpawnMain.CaterKillerEgg;
                break;
            }
            case 97: {
                index = OreSpawnMain.LeonEgg;
                break;
            }
            case 98: {
                index = OreSpawnMain.HammerheadEgg;
                break;
            }
            case 99: {
                index = OreSpawnMain.RubberDuckyEgg;
                break;
            }
            case 100: {
                index = OreSpawnMain.CrystalCowEgg;
                break;
            }
            case 101: {
                index = OreSpawnMain.CriminalEgg;
                break;
            }
            case 102: {
                index = OreSpawnMain.TheQueenEgg;
                break;
            }
            case 103: {
                index = OreSpawnMain.BrutalflyEgg;
                break;
            }
            case 104: {
                index = OreSpawnMain.NastysaurusEgg;
                break;
            }
            case 105: {
                index = OreSpawnMain.PointysaurusEgg;
                break;
            }
            case 106: {
                index = OreSpawnMain.CricketEgg;
                break;
            }
            case 107: {
                index = OreSpawnMain.ThePrincessEgg;
                break;
            }
            case 108: {
                index = OreSpawnMain.FrogEgg;
                break;
            }
            case 109: {
                index = OreSpawnMain.JefferyEgg;
                break;
            }
            case 110: {
                index = OreSpawnMain.AntRobotEgg;
                break;
            }
            case 111: {
                index = OreSpawnMain.SpiderRobotEgg;
                break;
            }
            case 112: {
                index = OreSpawnMain.SpiderDriverEgg;
                break;
            }
            case 113: {
                index = OreSpawnMain.CrabEgg;
                break;
            }
            default: {
                index = null;
            }
        }
        if (index == null) {
            return null;
        }
        is = new ItemStack(index, par1, val);
        var3 = new EntityItem(this.field_70170_p, this.field_70165_t + (double)OreSpawnMain.OreSpawnRand.nextInt(2) - (double)OreSpawnMain.OreSpawnRand.nextInt(2), this.field_70163_u + 1.0, this.field_70161_v + (double)OreSpawnMain.OreSpawnRand.nextInt(2) - (double)OreSpawnMain.OreSpawnRand.nextInt(2), is);
        if (var3 != null) {
            this.field_70170_p.func_72838_d((Entity)var3);
        }
        return is;
    }

    protected boolean func_70692_ba() {
        if (this.func_70631_g_()) {
            this.func_110163_bv();
            return false;
        }
        return !this.func_104002_bU();
    }

    public EntityAgeable func_90011_a(EntityAgeable entityageable) {
        return this.spawnBabyAnimal(entityageable);
    }

    public EasterBunny spawnBabyAnimal(EntityAgeable par1EntityAgeable) {
        return new EasterBunny(this.field_70170_p);
    }

    public boolean isWheat(ItemStack par1ItemStack) {
        return par1ItemStack != null && par1ItemStack.func_77973_b() == Items.field_151034_e;
    }

    public boolean func_70877_b(ItemStack par1ItemStack) {
        return par1ItemStack.func_77973_b() == OreSpawnMain.MyCrystalApple;
    }
}

