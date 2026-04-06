/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.EntityLivingBase
 *  net.minecraft.entity.boss.EntityDragon
 *  net.minecraft.entity.boss.EntityDragonPart
 *  net.minecraft.entity.boss.EntityWither
 *  net.minecraft.entity.monster.EntityBlaze
 *  net.minecraft.entity.monster.EntityCaveSpider
 *  net.minecraft.entity.monster.EntityCreeper
 *  net.minecraft.entity.monster.EntityEnderman
 *  net.minecraft.entity.monster.EntityGhast
 *  net.minecraft.entity.monster.EntityIronGolem
 *  net.minecraft.entity.monster.EntityMagmaCube
 *  net.minecraft.entity.monster.EntityPigZombie
 *  net.minecraft.entity.monster.EntitySilverfish
 *  net.minecraft.entity.monster.EntitySkeleton
 *  net.minecraft.entity.monster.EntitySlime
 *  net.minecraft.entity.monster.EntitySnowman
 *  net.minecraft.entity.monster.EntitySpider
 *  net.minecraft.entity.monster.EntityWitch
 *  net.minecraft.entity.monster.EntityZombie
 *  net.minecraft.entity.passive.EntityBat
 *  net.minecraft.entity.passive.EntityChicken
 *  net.minecraft.entity.passive.EntityCow
 *  net.minecraft.entity.passive.EntityHorse
 *  net.minecraft.entity.passive.EntityMooshroom
 *  net.minecraft.entity.passive.EntityOcelot
 *  net.minecraft.entity.passive.EntityPig
 *  net.minecraft.entity.passive.EntitySheep
 *  net.minecraft.entity.passive.EntitySquid
 *  net.minecraft.entity.passive.EntityVillager
 *  net.minecraft.entity.passive.EntityWolf
 *  net.minecraft.entity.player.EntityPlayer
 *  net.minecraft.entity.projectile.EntityThrowable
 *  net.minecraft.util.MovingObjectPosition
 *  net.minecraft.world.World
 */
package danger.orespawn;

import danger.orespawn.Alien;
import danger.orespawn.Alosaurus;
import danger.orespawn.AttackSquid;
import danger.orespawn.BandP;
import danger.orespawn.Baryonyx;
import danger.orespawn.Basilisk;
import danger.orespawn.Beaver;
import danger.orespawn.Bee;
import danger.orespawn.Boyfriend;
import danger.orespawn.Brutalfly;
import danger.orespawn.Camarasaurus;
import danger.orespawn.Cassowary;
import danger.orespawn.CaterKiller;
import danger.orespawn.CaveFisher;
import danger.orespawn.Cephadrome;
import danger.orespawn.Chipmunk;
import danger.orespawn.CliffRacer;
import danger.orespawn.CloudShark;
import danger.orespawn.Cockateil;
import danger.orespawn.Crab;
import danger.orespawn.CreepingHorror;
import danger.orespawn.Cricket;
import danger.orespawn.Cryolophosaurus;
import danger.orespawn.CrystalCow;
import danger.orespawn.Dragon;
import danger.orespawn.Dragonfly;
import danger.orespawn.DungeonBeast;
import danger.orespawn.EasterBunny;
import danger.orespawn.EmperorScorpion;
import danger.orespawn.EnchantedCow;
import danger.orespawn.EnderKnight;
import danger.orespawn.EnderReaper;
import danger.orespawn.Fairy;
import danger.orespawn.Firefly;
import danger.orespawn.Flounder;
import danger.orespawn.Frog;
import danger.orespawn.GammaMetroid;
import danger.orespawn.Gazelle;
import danger.orespawn.Girlfriend;
import danger.orespawn.GoldCow;
import danger.orespawn.GoldFish;
import danger.orespawn.Hammerhead;
import danger.orespawn.HerculesBeetle;
import danger.orespawn.Hydrolisc;
import danger.orespawn.Irukandji;
import danger.orespawn.Kraken;
import danger.orespawn.Kyuubi;
import danger.orespawn.LeafMonster;
import danger.orespawn.Leon;
import danger.orespawn.Lizard;
import danger.orespawn.LurkingTerror;
import danger.orespawn.Mantis;
import danger.orespawn.Molenoid;
import danger.orespawn.Mothra;
import danger.orespawn.Nastysaurus;
import danger.orespawn.OreSpawnMain;
import danger.orespawn.Ostrich;
import danger.orespawn.Peacock;
import danger.orespawn.PitchBlack;
import danger.orespawn.Pointysaurus;
import danger.orespawn.Rat;
import danger.orespawn.RedCow;
import danger.orespawn.Rotator;
import danger.orespawn.RubberDucky;
import danger.orespawn.Scorpion;
import danger.orespawn.SeaMonster;
import danger.orespawn.SeaViper;
import danger.orespawn.Skate;
import danger.orespawn.SpiderDriver;
import danger.orespawn.SpitBug;
import danger.orespawn.Spyro;
import danger.orespawn.StinkBug;
import danger.orespawn.Stinky;
import danger.orespawn.TRex;
import danger.orespawn.TerribleTerror;
import danger.orespawn.Triffid;
import danger.orespawn.TrooperBug;
import danger.orespawn.Urchin;
import danger.orespawn.VelocityRaptor;
import danger.orespawn.Vortex;
import danger.orespawn.WaterDragon;
import danger.orespawn.Whale;
import danger.orespawn.WormLarge;
import danger.orespawn.WormMedium;
import danger.orespawn.WormSmall;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.entity.boss.EntityDragonPart;
import net.minecraft.entity.boss.EntityWither;
import net.minecraft.entity.monster.EntityBlaze;
import net.minecraft.entity.monster.EntityCaveSpider;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.entity.monster.EntityGhast;
import net.minecraft.entity.monster.EntityIronGolem;
import net.minecraft.entity.monster.EntityMagmaCube;
import net.minecraft.entity.monster.EntityPigZombie;
import net.minecraft.entity.monster.EntitySilverfish;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.entity.monster.EntitySlime;
import net.minecraft.entity.monster.EntitySnowman;
import net.minecraft.entity.monster.EntitySpider;
import net.minecraft.entity.monster.EntityWitch;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.passive.EntityBat;
import net.minecraft.entity.passive.EntityChicken;
import net.minecraft.entity.passive.EntityCow;
import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.entity.passive.EntityMooshroom;
import net.minecraft.entity.passive.EntityOcelot;
import net.minecraft.entity.passive.EntityPig;
import net.minecraft.entity.passive.EntitySheep;
import net.minecraft.entity.passive.EntitySquid;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.passive.EntityWolf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;

public class EntityCage
extends EntityThrowable {
    private float my_rotation = 0.0f;
    public int my_index = 160;
    private World throwerWorld = null;
    private EntityPlayer thrower = null;

    public EntityCage(World par1World) {
        super(par1World);
        this.throwerWorld = par1World;
    }

    public EntityCage(World par1World, int i) {
        super(par1World);
        this.throwerWorld = par1World;
        this.my_index = i;
    }

    public EntityCage(World par1World, EntityPlayer par2EntityLiving, int i) {
        super(par1World, (EntityLivingBase)par2EntityLiving);
        this.throwerWorld = par1World;
        this.thrower = par2EntityLiving;
        this.my_index = i;
        if (this.thrower.field_70170_p != null) {
            this.throwerWorld = this.thrower.field_70170_p;
        }
    }

    public int getCageIndex() {
        return this.my_index;
    }

    protected void func_70184_a(MovingObjectPosition par1MovingObjectPosition) {
        if (par1MovingObjectPosition.field_72308_g != null && this.field_70146_Z.nextInt(10) >= 2) {
            Girlfriend gf;
            if (this.throwerWorld != null) {
                for (int var3 = 0; var3 < 4; ++var3) {
                    this.throwerWorld.func_72869_a("smoke", par1MovingObjectPosition.field_72308_g.field_70165_t, par1MovingObjectPosition.field_72308_g.field_70163_u + 0.25, par1MovingObjectPosition.field_72308_g.field_70161_v, 0.0, 0.0, 0.0);
                    this.throwerWorld.func_72869_a("explode", par1MovingObjectPosition.field_72308_g.field_70165_t, par1MovingObjectPosition.field_72308_g.field_70163_u + 0.25, par1MovingObjectPosition.field_72308_g.field_70161_v, 0.0, 0.0, 0.0);
                    this.throwerWorld.func_72869_a("reddust", par1MovingObjectPosition.field_72308_g.field_70165_t, par1MovingObjectPosition.field_72308_g.field_70163_u + 0.25, par1MovingObjectPosition.field_72308_g.field_70161_v, 0.0, 0.0, 0.0);
                }
                if (this.thrower != null) {
                    this.throwerWorld.func_72956_a((Entity)this.thrower, "random.explode", 1.0f, 1.5f);
                }
            }
            if (par1MovingObjectPosition.field_72308_g instanceof EntityPlayer) {
                if (!this.field_70170_p.field_72995_K) {
                    this.func_145779_a(OreSpawnMain.CageEmpty, 1);
                    this.func_70106_y();
                }
                return;
            }
            if (par1MovingObjectPosition.field_72308_g instanceof SpiderDriver) {
                par1MovingObjectPosition.field_72308_g.func_70106_y();
                this.func_145779_a(OreSpawnMain.CagedSpiderDriver, 1);
            } else if (par1MovingObjectPosition.field_72308_g instanceof EntitySpider) {
                if (par1MovingObjectPosition.field_72308_g instanceof EntityCaveSpider) {
                    par1MovingObjectPosition.field_72308_g.func_70106_y();
                    this.func_145779_a(OreSpawnMain.CagedCaveSpider, 1);
                } else {
                    par1MovingObjectPosition.field_72308_g.func_70106_y();
                    this.func_145779_a(OreSpawnMain.CagedSpider, 1);
                }
            }
            if (par1MovingObjectPosition.field_72308_g instanceof Crab) {
                par1MovingObjectPosition.field_72308_g.func_70106_y();
                this.func_145779_a(OreSpawnMain.CagedCrab, 1);
            }
            if (par1MovingObjectPosition.field_72308_g instanceof EntityBat) {
                par1MovingObjectPosition.field_72308_g.func_70106_y();
                this.func_145779_a(OreSpawnMain.CagedBat, 2);
            }
            if (par1MovingObjectPosition.field_72308_g instanceof EntityPig) {
                par1MovingObjectPosition.field_72308_g.func_70106_y();
                this.func_145779_a(OreSpawnMain.CagedPig, 1);
            }
            if (par1MovingObjectPosition.field_72308_g instanceof EntitySquid) {
                par1MovingObjectPosition.field_72308_g.func_70106_y();
                this.func_145779_a(OreSpawnMain.CagedSquid, 1);
            }
            if (par1MovingObjectPosition.field_72308_g instanceof EntityChicken) {
                par1MovingObjectPosition.field_72308_g.func_70106_y();
                this.func_145779_a(OreSpawnMain.CagedChicken, 1);
            }
            if (par1MovingObjectPosition.field_72308_g instanceof EntityCreeper) {
                par1MovingObjectPosition.field_72308_g.func_70106_y();
                this.func_145779_a(OreSpawnMain.CagedCreeper, 1);
            }
            if (par1MovingObjectPosition.field_72308_g instanceof EntityHorse) {
                par1MovingObjectPosition.field_72308_g.func_70106_y();
                this.func_145779_a(OreSpawnMain.CagedHorse, 1);
            }
            if (par1MovingObjectPosition.field_72308_g instanceof EntitySkeleton) {
                EntitySkeleton sk = (EntitySkeleton)par1MovingObjectPosition.field_72308_g;
                if (sk.func_82202_m() != 0) {
                    this.func_145779_a(OreSpawnMain.CagedWitherSkeleton, 1);
                } else {
                    this.func_145779_a(OreSpawnMain.CagedSkeleton, 1);
                }
                par1MovingObjectPosition.field_72308_g.func_70106_y();
            }
            if (par1MovingObjectPosition.field_72308_g instanceof EntityZombie) {
                if (par1MovingObjectPosition.field_72308_g instanceof EntityPigZombie) {
                    par1MovingObjectPosition.field_72308_g.func_70106_y();
                    this.func_145779_a(OreSpawnMain.CagedZombiePigman, 1);
                } else {
                    par1MovingObjectPosition.field_72308_g.func_70106_y();
                    this.func_145779_a(OreSpawnMain.CagedZombie, 1);
                }
            }
            if (par1MovingObjectPosition.field_72308_g instanceof EntitySlime) {
                if (par1MovingObjectPosition.field_72308_g instanceof EntityMagmaCube) {
                    par1MovingObjectPosition.field_72308_g.func_70106_y();
                    this.func_145779_a(OreSpawnMain.CagedMagmaCube, 1);
                } else {
                    par1MovingObjectPosition.field_72308_g.func_70106_y();
                    this.func_145779_a(OreSpawnMain.CagedSlime, 1);
                }
            }
            if (par1MovingObjectPosition.field_72308_g instanceof EntityGhast) {
                if (this.field_70146_Z.nextInt(10) < 2) {
                    if (!this.field_70170_p.field_72995_K) {
                        this.func_145779_a(OreSpawnMain.CageEmpty, 1);
                        this.func_70106_y();
                    }
                    return;
                }
                par1MovingObjectPosition.field_72308_g.func_70106_y();
                this.func_145779_a(OreSpawnMain.CagedGhast, 1);
            }
            if (par1MovingObjectPosition.field_72308_g instanceof EntityEnderman) {
                if (this.field_70146_Z.nextInt(10) < 2) {
                    if (!this.field_70170_p.field_72995_K) {
                        this.func_145779_a(OreSpawnMain.CageEmpty, 1);
                        this.func_70106_y();
                    }
                    return;
                }
                par1MovingObjectPosition.field_72308_g.func_70106_y();
                this.func_145779_a(OreSpawnMain.CagedEnderman, 1);
            }
            if (par1MovingObjectPosition.field_72308_g instanceof EntitySilverfish) {
                par1MovingObjectPosition.field_72308_g.func_70106_y();
                this.func_145779_a(OreSpawnMain.CagedSilverfish, 2);
            }
            if (par1MovingObjectPosition.field_72308_g instanceof EntityWitch) {
                par1MovingObjectPosition.field_72308_g.func_70106_y();
                this.func_145779_a(OreSpawnMain.CagedWitch, 1);
            }
            if (par1MovingObjectPosition.field_72308_g instanceof EntitySheep) {
                par1MovingObjectPosition.field_72308_g.func_70106_y();
                this.func_145779_a(OreSpawnMain.CagedSheep, 1);
            }
            if (par1MovingObjectPosition.field_72308_g instanceof EntityWolf) {
                par1MovingObjectPosition.field_72308_g.func_70106_y();
                this.func_145779_a(OreSpawnMain.CagedWolf, 1);
            }
            if (par1MovingObjectPosition.field_72308_g instanceof EntityOcelot) {
                par1MovingObjectPosition.field_72308_g.func_70106_y();
                this.func_145779_a(OreSpawnMain.CagedOcelot, 1);
            }
            if (par1MovingObjectPosition.field_72308_g instanceof EntityBlaze) {
                par1MovingObjectPosition.field_72308_g.func_70106_y();
                this.func_145779_a(OreSpawnMain.CagedBlaze, 1);
            }
            if (par1MovingObjectPosition.field_72308_g instanceof Girlfriend && !(gf = (Girlfriend)par1MovingObjectPosition.field_72308_g).func_70909_n()) {
                par1MovingObjectPosition.field_72308_g.func_70106_y();
                this.func_145779_a(OreSpawnMain.CagedGirlfriend, 1);
            }
            if (par1MovingObjectPosition.field_72308_g instanceof Boyfriend && !(gf = (Boyfriend)par1MovingObjectPosition.field_72308_g).func_70909_n()) {
                par1MovingObjectPosition.field_72308_g.func_70106_y();
                this.func_145779_a(OreSpawnMain.CagedBoyfriend, 1);
            }
            if (par1MovingObjectPosition.field_72308_g instanceof EntityDragon) {
                if (this.field_70146_Z.nextInt(10) < 5) {
                    if (!this.field_70170_p.field_72995_K) {
                        this.func_145779_a(OreSpawnMain.CageEmpty, 1);
                        this.func_70106_y();
                    }
                    return;
                }
                EntityDragon dr = (EntityDragon)par1MovingObjectPosition.field_72308_g;
                dr.func_70106_y();
                this.func_145779_a(OreSpawnMain.CagedEnderDragon, 1);
            }
            if (par1MovingObjectPosition.field_72308_g instanceof EntityDragonPart) {
                if (this.field_70146_Z.nextInt(10) < 5) {
                    if (!this.field_70170_p.field_72995_K) {
                        this.func_145779_a(OreSpawnMain.CageEmpty, 1);
                        this.func_70106_y();
                    }
                    return;
                }
                EntityDragonPart dp = (EntityDragonPart)par1MovingObjectPosition.field_72308_g;
                EntityDragon dr = (EntityDragon)dp.field_70259_a;
                dr.func_70106_y();
                this.func_145779_a(OreSpawnMain.CagedEnderDragon, 1);
            }
            if (par1MovingObjectPosition.field_72308_g instanceof EntitySnowman) {
                par1MovingObjectPosition.field_72308_g.func_70106_y();
                this.func_145779_a(OreSpawnMain.CagedSnowGolem, 1);
            }
            if (par1MovingObjectPosition.field_72308_g instanceof EntityIronGolem) {
                par1MovingObjectPosition.field_72308_g.func_70106_y();
                this.func_145779_a(OreSpawnMain.CagedIronGolem, 1);
            }
            if (par1MovingObjectPosition.field_72308_g instanceof EntityWither) {
                if (this.field_70146_Z.nextInt(10) < 2) {
                    if (!this.field_70170_p.field_72995_K) {
                        this.func_145779_a(OreSpawnMain.CageEmpty, 1);
                        this.func_70106_y();
                    }
                    return;
                }
                par1MovingObjectPosition.field_72308_g.func_70106_y();
                this.func_145779_a(OreSpawnMain.CagedWitherBoss, 1);
            }
            if (par1MovingObjectPosition.field_72308_g instanceof CrystalCow) {
                par1MovingObjectPosition.field_72308_g.func_70106_y();
                this.func_145779_a(OreSpawnMain.CagedCrystalCow, 1);
                if (!this.field_70170_p.field_72995_K) {
                    this.func_70106_y();
                }
                return;
            }
            if (par1MovingObjectPosition.field_72308_g instanceof EnchantedCow) {
                par1MovingObjectPosition.field_72308_g.func_70106_y();
                this.func_145779_a(OreSpawnMain.CagedEnchantedCow, 1);
                if (!this.field_70170_p.field_72995_K) {
                    this.func_70106_y();
                }
                return;
            }
            if (par1MovingObjectPosition.field_72308_g instanceof GoldCow) {
                par1MovingObjectPosition.field_72308_g.func_70106_y();
                this.func_145779_a(OreSpawnMain.CagedGoldCow, 1);
                if (!this.field_70170_p.field_72995_K) {
                    this.func_70106_y();
                }
                return;
            }
            if (par1MovingObjectPosition.field_72308_g instanceof RedCow) {
                par1MovingObjectPosition.field_72308_g.func_70106_y();
                this.func_145779_a(OreSpawnMain.CagedRedCow, 1);
                if (!this.field_70170_p.field_72995_K) {
                    this.func_70106_y();
                }
                return;
            }
            if (par1MovingObjectPosition.field_72308_g instanceof EntityCow) {
                if (par1MovingObjectPosition.field_72308_g instanceof EntityMooshroom) {
                    par1MovingObjectPosition.field_72308_g.func_70106_y();
                    this.func_145779_a(OreSpawnMain.CagedMooshroom, 1);
                } else {
                    par1MovingObjectPosition.field_72308_g.func_70106_y();
                    this.func_145779_a(OreSpawnMain.CagedCow, 1);
                }
                if (!this.field_70170_p.field_72995_K) {
                    this.func_70106_y();
                }
                return;
            }
            if (par1MovingObjectPosition.field_72308_g instanceof EntityVillager) {
                par1MovingObjectPosition.field_72308_g.func_70106_y();
                this.func_145779_a(OreSpawnMain.CagedVillager, 1);
                if (!this.field_70170_p.field_72995_K) {
                    this.func_70106_y();
                }
                return;
            }
            if (par1MovingObjectPosition.field_72308_g instanceof Mothra) {
                if (this.field_70146_Z.nextInt(10) < 4) {
                    if (!this.field_70170_p.field_72995_K) {
                        this.func_145779_a(OreSpawnMain.CageEmpty, 1);
                        this.func_70106_y();
                    }
                    return;
                }
                par1MovingObjectPosition.field_72308_g.func_70106_y();
                this.func_145779_a(OreSpawnMain.CagedMOTHRA, 1);
            }
            if (par1MovingObjectPosition.field_72308_g instanceof Alosaurus) {
                if (this.field_70146_Z.nextInt(10) < 4) {
                    if (!this.field_70170_p.field_72995_K) {
                        this.func_145779_a(OreSpawnMain.CageEmpty, 1);
                        this.func_70106_y();
                    }
                    return;
                }
                par1MovingObjectPosition.field_72308_g.func_70106_y();
                this.func_145779_a(OreSpawnMain.CagedAlo, 1);
            }
            if (par1MovingObjectPosition.field_72308_g instanceof Cryolophosaurus) {
                par1MovingObjectPosition.field_72308_g.func_70106_y();
                this.func_145779_a(OreSpawnMain.CagedCryo, 1);
            }
            if (par1MovingObjectPosition.field_72308_g instanceof Camarasaurus) {
                par1MovingObjectPosition.field_72308_g.func_70106_y();
                this.func_145779_a(OreSpawnMain.CagedCama, 1);
            }
            if (par1MovingObjectPosition.field_72308_g instanceof VelocityRaptor) {
                par1MovingObjectPosition.field_72308_g.func_70106_y();
                this.func_145779_a(OreSpawnMain.CagedVelo, 1);
            }
            if (par1MovingObjectPosition.field_72308_g instanceof Hydrolisc) {
                par1MovingObjectPosition.field_72308_g.func_70106_y();
                this.func_145779_a(OreSpawnMain.CagedHydro, 1);
            }
            if (par1MovingObjectPosition.field_72308_g instanceof Basilisk) {
                if (this.field_70146_Z.nextInt(10) < 6) {
                    if (!this.field_70170_p.field_72995_K) {
                        this.func_145779_a(OreSpawnMain.CageEmpty, 1);
                        this.func_70106_y();
                    }
                    return;
                }
                par1MovingObjectPosition.field_72308_g.func_70106_y();
                this.func_145779_a(OreSpawnMain.CagedBasil, 1);
            }
            if (par1MovingObjectPosition.field_72308_g instanceof Dragonfly) {
                par1MovingObjectPosition.field_72308_g.func_70106_y();
                this.func_145779_a(OreSpawnMain.CagedDragonfly, 2);
            }
            if (par1MovingObjectPosition.field_72308_g instanceof EmperorScorpion) {
                if (this.field_70146_Z.nextInt(10) < 7) {
                    if (!this.field_70170_p.field_72995_K) {
                        this.func_145779_a(OreSpawnMain.CageEmpty, 1);
                        this.func_70106_y();
                    }
                    return;
                }
                par1MovingObjectPosition.field_72308_g.func_70106_y();
                this.func_145779_a(OreSpawnMain.CagedEmperorScorpion, 1);
            }
            if (par1MovingObjectPosition.field_72308_g instanceof Cephadrome) {
                if (this.field_70146_Z.nextInt(10) < 7) {
                    if (!this.field_70170_p.field_72995_K) {
                        this.func_145779_a(OreSpawnMain.CageEmpty, 1);
                        this.func_70106_y();
                    }
                    return;
                }
                par1MovingObjectPosition.field_72308_g.func_70106_y();
                this.func_145779_a(OreSpawnMain.CagedCephadrome, 1);
            }
            if (par1MovingObjectPosition.field_72308_g instanceof Dragon) {
                if (this.field_70146_Z.nextInt(10) < 7) {
                    if (!this.field_70170_p.field_72995_K) {
                        this.func_145779_a(OreSpawnMain.CageEmpty, 1);
                        this.func_70106_y();
                    }
                    return;
                }
                par1MovingObjectPosition.field_72308_g.func_70106_y();
                this.func_145779_a(OreSpawnMain.CagedDragon, 1);
            }
            if (par1MovingObjectPosition.field_72308_g instanceof Scorpion) {
                par1MovingObjectPosition.field_72308_g.func_70106_y();
                this.func_145779_a(OreSpawnMain.CagedScorpion, 1);
            }
            if (par1MovingObjectPosition.field_72308_g instanceof CaveFisher) {
                par1MovingObjectPosition.field_72308_g.func_70106_y();
                this.func_145779_a(OreSpawnMain.CagedCaveFisher, 1);
            }
            if (par1MovingObjectPosition.field_72308_g instanceof Spyro) {
                par1MovingObjectPosition.field_72308_g.func_70106_y();
                this.func_145779_a(OreSpawnMain.CagedSpyro, 1);
            }
            if (par1MovingObjectPosition.field_72308_g instanceof Baryonyx) {
                par1MovingObjectPosition.field_72308_g.func_70106_y();
                this.func_145779_a(OreSpawnMain.CagedBaryonyx, 1);
            }
            if (par1MovingObjectPosition.field_72308_g instanceof GammaMetroid) {
                par1MovingObjectPosition.field_72308_g.func_70106_y();
                this.func_145779_a(OreSpawnMain.CagedGammaMetroid, 1);
            }
            if (par1MovingObjectPosition.field_72308_g instanceof Cockateil) {
                par1MovingObjectPosition.field_72308_g.func_70106_y();
                this.func_145779_a(OreSpawnMain.CagedCockateil, 4);
            }
            if (par1MovingObjectPosition.field_72308_g instanceof AttackSquid) {
                par1MovingObjectPosition.field_72308_g.func_70106_y();
                this.func_145779_a(OreSpawnMain.CagedAttackSquid, 6);
            }
            if (par1MovingObjectPosition.field_72308_g instanceof Kyuubi) {
                if (this.field_70146_Z.nextInt(10) < 3) {
                    if (!this.field_70170_p.field_72995_K) {
                        this.func_145779_a(OreSpawnMain.CageEmpty, 1);
                        this.func_70106_y();
                    }
                    return;
                }
                par1MovingObjectPosition.field_72308_g.func_70106_y();
                this.func_145779_a(OreSpawnMain.CagedKyuubi, 1);
            }
            if (par1MovingObjectPosition.field_72308_g instanceof WaterDragon) {
                if (this.field_70146_Z.nextInt(10) < 6) {
                    if (!this.field_70170_p.field_72995_K) {
                        this.func_145779_a(OreSpawnMain.CageEmpty, 1);
                        this.func_70106_y();
                    }
                    return;
                }
                par1MovingObjectPosition.field_72308_g.func_70106_y();
                this.func_145779_a(OreSpawnMain.CagedWaterDragon, 1);
            }
            if (par1MovingObjectPosition.field_72308_g instanceof Kraken) {
                if (this.field_70146_Z.nextInt(100) < 95) {
                    if (!this.field_70170_p.field_72995_K) {
                        this.func_145779_a(OreSpawnMain.CageEmpty, 1);
                        this.func_70106_y();
                    }
                    return;
                }
                par1MovingObjectPosition.field_72308_g.func_70106_y();
                this.func_145779_a(OreSpawnMain.CagedKraken, 1);
            }
            if (par1MovingObjectPosition.field_72308_g instanceof Lizard) {
                if (this.field_70146_Z.nextInt(10) < 2) {
                    if (!this.field_70170_p.field_72995_K) {
                        this.func_145779_a(OreSpawnMain.CageEmpty, 1);
                        this.func_70106_y();
                    }
                    return;
                }
                par1MovingObjectPosition.field_72308_g.func_70106_y();
                this.func_145779_a(OreSpawnMain.CagedLizard, 1);
            }
            if (par1MovingObjectPosition.field_72308_g instanceof Alien) {
                if (this.field_70146_Z.nextInt(10) < 5) {
                    if (!this.field_70170_p.field_72995_K) {
                        this.func_145779_a(OreSpawnMain.CageEmpty, 1);
                        this.func_70106_y();
                    }
                    return;
                }
                par1MovingObjectPosition.field_72308_g.func_70106_y();
                this.func_145779_a(OreSpawnMain.CagedAlien, 1);
            }
            if (par1MovingObjectPosition.field_72308_g instanceof Bee) {
                if (this.field_70146_Z.nextInt(10) < 3) {
                    if (!this.field_70170_p.field_72995_K) {
                        this.func_145779_a(OreSpawnMain.CageEmpty, 1);
                        this.func_70106_y();
                    }
                    return;
                }
                par1MovingObjectPosition.field_72308_g.func_70106_y();
                this.func_145779_a(OreSpawnMain.CagedBee, 1);
            }
            if (par1MovingObjectPosition.field_72308_g instanceof Firefly) {
                par1MovingObjectPosition.field_72308_g.func_70106_y();
                this.func_145779_a(OreSpawnMain.CagedFirefly, 1);
            }
            if (par1MovingObjectPosition.field_72308_g instanceof Chipmunk) {
                par1MovingObjectPosition.field_72308_g.func_70106_y();
                this.func_145779_a(OreSpawnMain.CagedChipmunk, 1);
            }
            if (par1MovingObjectPosition.field_72308_g instanceof Gazelle) {
                par1MovingObjectPosition.field_72308_g.func_70106_y();
                this.func_145779_a(OreSpawnMain.CagedGazelle, 1);
            }
            if (par1MovingObjectPosition.field_72308_g instanceof Ostrich) {
                par1MovingObjectPosition.field_72308_g.func_70106_y();
                this.func_145779_a(OreSpawnMain.CagedOstrich, 1);
            }
            if (par1MovingObjectPosition.field_72308_g instanceof TrooperBug) {
                if (this.field_70146_Z.nextInt(10) < 6) {
                    if (!this.field_70170_p.field_72995_K) {
                        this.func_145779_a(OreSpawnMain.CageEmpty, 1);
                        this.func_70106_y();
                    }
                    return;
                }
                par1MovingObjectPosition.field_72308_g.func_70106_y();
                this.func_145779_a(OreSpawnMain.CagedTrooper, 1);
            }
            if (par1MovingObjectPosition.field_72308_g instanceof SpitBug) {
                if (this.field_70146_Z.nextInt(10) < 3) {
                    if (!this.field_70170_p.field_72995_K) {
                        this.func_145779_a(OreSpawnMain.CageEmpty, 1);
                        this.func_70106_y();
                    }
                    return;
                }
                par1MovingObjectPosition.field_72308_g.func_70106_y();
                this.func_145779_a(OreSpawnMain.CagedSpit, 1);
            }
            if (par1MovingObjectPosition.field_72308_g instanceof StinkBug) {
                par1MovingObjectPosition.field_72308_g.func_70106_y();
                this.func_145779_a(OreSpawnMain.CagedStink, 1);
            }
            if (par1MovingObjectPosition.field_72308_g instanceof CreepingHorror) {
                par1MovingObjectPosition.field_72308_g.func_70106_y();
                this.func_145779_a(OreSpawnMain.CagedCreepingHorror, 1);
            }
            if (par1MovingObjectPosition.field_72308_g instanceof TerribleTerror) {
                par1MovingObjectPosition.field_72308_g.func_70106_y();
                this.func_145779_a(OreSpawnMain.CagedTerribleTerror, 1);
            }
            if (par1MovingObjectPosition.field_72308_g instanceof CliffRacer) {
                par1MovingObjectPosition.field_72308_g.func_70106_y();
                this.func_145779_a(OreSpawnMain.CagedCliffRacer, 1);
            }
            if (par1MovingObjectPosition.field_72308_g instanceof Triffid) {
                if (this.field_70146_Z.nextInt(10) < 6) {
                    if (!this.field_70170_p.field_72995_K) {
                        this.func_145779_a(OreSpawnMain.CageEmpty, 1);
                        this.func_70106_y();
                    }
                    return;
                }
                par1MovingObjectPosition.field_72308_g.func_70106_y();
                this.func_145779_a(OreSpawnMain.CagedTriffid, 1);
            }
            if (par1MovingObjectPosition.field_72308_g instanceof PitchBlack) {
                if (this.field_70146_Z.nextInt(10) < 7) {
                    if (!this.field_70170_p.field_72995_K) {
                        this.func_145779_a(OreSpawnMain.CageEmpty, 1);
                        this.func_70106_y();
                    }
                    return;
                }
                par1MovingObjectPosition.field_72308_g.func_70106_y();
                this.func_145779_a(OreSpawnMain.CagedPitchBlack, 1);
            }
            if (par1MovingObjectPosition.field_72308_g instanceof LurkingTerror) {
                par1MovingObjectPosition.field_72308_g.func_70106_y();
                this.func_145779_a(OreSpawnMain.CagedLurkingTerror, 1);
            }
            if (par1MovingObjectPosition.field_72308_g instanceof WormSmall) {
                par1MovingObjectPosition.field_72308_g.func_70106_y();
                this.func_145779_a(OreSpawnMain.CagedSmallWorm, 1);
            }
            if (par1MovingObjectPosition.field_72308_g instanceof WormMedium) {
                par1MovingObjectPosition.field_72308_g.func_70106_y();
                this.func_145779_a(OreSpawnMain.CagedMediumWorm, 1);
            }
            if (par1MovingObjectPosition.field_72308_g instanceof Cassowary) {
                par1MovingObjectPosition.field_72308_g.func_70106_y();
                this.func_145779_a(OreSpawnMain.CagedCassowary, 1);
            }
            if (par1MovingObjectPosition.field_72308_g instanceof CloudShark) {
                par1MovingObjectPosition.field_72308_g.func_70106_y();
                this.func_145779_a(OreSpawnMain.CagedCloudShark, 1);
            }
            if (par1MovingObjectPosition.field_72308_g instanceof GoldFish) {
                par1MovingObjectPosition.field_72308_g.func_70106_y();
                this.func_145779_a(OreSpawnMain.CagedGoldFish, 1);
            }
            if (par1MovingObjectPosition.field_72308_g instanceof LeafMonster) {
                par1MovingObjectPosition.field_72308_g.func_70106_y();
                this.func_145779_a(OreSpawnMain.CagedLeafMonster, 1);
            }
            if (par1MovingObjectPosition.field_72308_g instanceof WormLarge) {
                if (this.field_70146_Z.nextInt(10) < 5) {
                    if (!this.field_70170_p.field_72995_K) {
                        this.func_145779_a(OreSpawnMain.CageEmpty, 1);
                        this.func_70106_y();
                    }
                    return;
                }
                par1MovingObjectPosition.field_72308_g.func_70106_y();
                this.func_145779_a(OreSpawnMain.CagedLargeWorm, 1);
            }
            if (par1MovingObjectPosition.field_72308_g instanceof EnderKnight) {
                if (this.field_70146_Z.nextInt(10) < 3) {
                    if (!this.field_70170_p.field_72995_K) {
                        this.func_145779_a(OreSpawnMain.CageEmpty, 1);
                        this.func_70106_y();
                    }
                    return;
                }
                par1MovingObjectPosition.field_72308_g.func_70106_y();
                this.func_145779_a(OreSpawnMain.CagedEnderKnight, 1);
            }
            if (par1MovingObjectPosition.field_72308_g instanceof EnderReaper) {
                if (this.field_70146_Z.nextInt(10) < 2) {
                    if (!this.field_70170_p.field_72995_K) {
                        this.func_145779_a(OreSpawnMain.CageEmpty, 1);
                        this.func_70106_y();
                    }
                    return;
                }
                par1MovingObjectPosition.field_72308_g.func_70106_y();
                this.func_145779_a(OreSpawnMain.CagedEnderReaper, 1);
            }
            if (par1MovingObjectPosition.field_72308_g instanceof Beaver) {
                par1MovingObjectPosition.field_72308_g.func_70106_y();
                this.func_145779_a(OreSpawnMain.CagedBeaver, 1);
            }
            if (par1MovingObjectPosition.field_72308_g instanceof Urchin) {
                par1MovingObjectPosition.field_72308_g.func_70106_y();
                this.func_145779_a(OreSpawnMain.CagedUrchin, 1);
            }
            if (par1MovingObjectPosition.field_72308_g instanceof Flounder) {
                par1MovingObjectPosition.field_72308_g.func_70106_y();
                this.func_145779_a(OreSpawnMain.CagedFlounder, 1);
            }
            if (par1MovingObjectPosition.field_72308_g instanceof Skate) {
                par1MovingObjectPosition.field_72308_g.func_70106_y();
                this.func_145779_a(OreSpawnMain.CagedSkate, 1);
            }
            if (par1MovingObjectPosition.field_72308_g instanceof Rotator) {
                par1MovingObjectPosition.field_72308_g.func_70106_y();
                this.func_145779_a(OreSpawnMain.CagedRotator, 1);
            }
            if (par1MovingObjectPosition.field_72308_g instanceof Peacock) {
                par1MovingObjectPosition.field_72308_g.func_70106_y();
                this.func_145779_a(OreSpawnMain.CagedPeacock, 1);
            }
            if (par1MovingObjectPosition.field_72308_g instanceof Fairy) {
                par1MovingObjectPosition.field_72308_g.func_70106_y();
                this.func_145779_a(OreSpawnMain.CagedFairy, 1);
            }
            if (par1MovingObjectPosition.field_72308_g instanceof DungeonBeast) {
                par1MovingObjectPosition.field_72308_g.func_70106_y();
                this.func_145779_a(OreSpawnMain.CagedDungeonBeast, 1);
            }
            if (par1MovingObjectPosition.field_72308_g instanceof Vortex) {
                if (this.field_70146_Z.nextInt(10) < 3) {
                    if (!this.field_70170_p.field_72995_K) {
                        this.func_145779_a(OreSpawnMain.CageEmpty, 1);
                        this.func_70106_y();
                    }
                    return;
                }
                par1MovingObjectPosition.field_72308_g.func_70106_y();
                this.func_145779_a(OreSpawnMain.CagedVortex, 1);
            }
            if (par1MovingObjectPosition.field_72308_g instanceof Rat) {
                par1MovingObjectPosition.field_72308_g.func_70106_y();
                this.func_145779_a(OreSpawnMain.CagedRat, 1);
            }
            if (par1MovingObjectPosition.field_72308_g instanceof Whale) {
                if (this.field_70146_Z.nextInt(10) < 2) {
                    if (!this.field_70170_p.field_72995_K) {
                        this.func_145779_a(OreSpawnMain.CageEmpty, 1);
                        this.func_70106_y();
                    }
                    return;
                }
                par1MovingObjectPosition.field_72308_g.func_70106_y();
                this.func_145779_a(OreSpawnMain.CagedWhale, 1);
            }
            if (par1MovingObjectPosition.field_72308_g instanceof Irukandji) {
                par1MovingObjectPosition.field_72308_g.func_70106_y();
                this.func_145779_a(OreSpawnMain.CagedIrukandji, 1);
            }
            if (par1MovingObjectPosition.field_72308_g instanceof Stinky) {
                par1MovingObjectPosition.field_72308_g.func_70106_y();
                this.func_145779_a(OreSpawnMain.CagedStinky, 1);
            }
            if (par1MovingObjectPosition.field_72308_g instanceof Mantis) {
                if (this.field_70146_Z.nextInt(10) < 3) {
                    if (!this.field_70170_p.field_72995_K) {
                        this.func_145779_a(OreSpawnMain.CageEmpty, 1);
                        this.func_70106_y();
                    }
                    return;
                }
                par1MovingObjectPosition.field_72308_g.func_70106_y();
                this.func_145779_a(OreSpawnMain.CagedMantis, 1);
            }
            if (par1MovingObjectPosition.field_72308_g instanceof TRex) {
                if (this.field_70146_Z.nextInt(10) < 4) {
                    if (!this.field_70170_p.field_72995_K) {
                        this.func_145779_a(OreSpawnMain.CageEmpty, 1);
                        this.func_70106_y();
                    }
                    return;
                }
                par1MovingObjectPosition.field_72308_g.func_70106_y();
                this.func_145779_a(OreSpawnMain.CagedTRex, 1);
            }
            if (par1MovingObjectPosition.field_72308_g instanceof HerculesBeetle) {
                if (this.field_70146_Z.nextInt(10) < 5) {
                    if (!this.field_70170_p.field_72995_K) {
                        this.func_145779_a(OreSpawnMain.CageEmpty, 1);
                        this.func_70106_y();
                    }
                    return;
                }
                par1MovingObjectPosition.field_72308_g.func_70106_y();
                this.func_145779_a(OreSpawnMain.CagedHercules, 1);
            }
            if (par1MovingObjectPosition.field_72308_g instanceof EasterBunny) {
                par1MovingObjectPosition.field_72308_g.func_70106_y();
                this.func_145779_a(OreSpawnMain.CagedEasterBunny, 1);
            }
            if (par1MovingObjectPosition.field_72308_g instanceof CaterKiller) {
                if (this.field_70146_Z.nextInt(10) < 7) {
                    if (!this.field_70170_p.field_72995_K) {
                        this.func_145779_a(OreSpawnMain.CageEmpty, 1);
                        this.func_70106_y();
                    }
                    return;
                }
                par1MovingObjectPosition.field_72308_g.func_70106_y();
                this.func_145779_a(OreSpawnMain.CagedCaterKiller, 1);
            }
            if (par1MovingObjectPosition.field_72308_g instanceof Molenoid) {
                if (this.field_70146_Z.nextInt(10) < 5) {
                    if (!this.field_70170_p.field_72995_K) {
                        this.func_145779_a(OreSpawnMain.CageEmpty, 1);
                        this.func_70106_y();
                    }
                    return;
                }
                par1MovingObjectPosition.field_72308_g.func_70106_y();
                this.func_145779_a(OreSpawnMain.CagedMolenoid, 1);
            }
            if (par1MovingObjectPosition.field_72308_g instanceof SeaMonster) {
                if (this.field_70146_Z.nextInt(10) < 3) {
                    if (!this.field_70170_p.field_72995_K) {
                        this.func_145779_a(OreSpawnMain.CageEmpty, 1);
                        this.func_70106_y();
                    }
                    return;
                }
                par1MovingObjectPosition.field_72308_g.func_70106_y();
                this.func_145779_a(OreSpawnMain.CagedSeaMonster, 1);
            }
            if (par1MovingObjectPosition.field_72308_g instanceof SeaViper) {
                if (this.field_70146_Z.nextInt(10) < 4) {
                    if (!this.field_70170_p.field_72995_K) {
                        this.func_145779_a(OreSpawnMain.CageEmpty, 1);
                        this.func_70106_y();
                    }
                    return;
                }
                par1MovingObjectPosition.field_72308_g.func_70106_y();
                this.func_145779_a(OreSpawnMain.CagedSeaViper, 1);
            }
            if (par1MovingObjectPosition.field_72308_g instanceof RubberDucky) {
                par1MovingObjectPosition.field_72308_g.func_70106_y();
                this.func_145779_a(OreSpawnMain.CagedRubberDucky, 1);
            }
            if (par1MovingObjectPosition.field_72308_g instanceof Leon) {
                if (this.field_70146_Z.nextInt(10) < 7) {
                    if (!this.field_70170_p.field_72995_K) {
                        this.func_145779_a(OreSpawnMain.CageEmpty, 1);
                        this.func_70106_y();
                    }
                    return;
                }
                par1MovingObjectPosition.field_72308_g.func_70106_y();
                this.func_145779_a(OreSpawnMain.CagedLeon, 1);
            }
            if (par1MovingObjectPosition.field_72308_g instanceof Hammerhead) {
                if (this.field_70146_Z.nextInt(10) < 7) {
                    if (!this.field_70170_p.field_72995_K) {
                        this.func_145779_a(OreSpawnMain.CageEmpty, 1);
                        this.func_70106_y();
                    }
                    return;
                }
                par1MovingObjectPosition.field_72308_g.func_70106_y();
                this.func_145779_a(OreSpawnMain.CagedHammerhead, 1);
            }
            if (par1MovingObjectPosition.field_72308_g instanceof BandP) {
                par1MovingObjectPosition.field_72308_g.func_70106_y();
                this.func_145779_a(OreSpawnMain.CagedCriminal, 1);
            }
            if (par1MovingObjectPosition.field_72308_g instanceof Cricket) {
                par1MovingObjectPosition.field_72308_g.func_70106_y();
                this.func_145779_a(OreSpawnMain.CagedCricket, 1);
            }
            if (par1MovingObjectPosition.field_72308_g instanceof Frog) {
                par1MovingObjectPosition.field_72308_g.func_70106_y();
                this.func_145779_a(OreSpawnMain.CagedFrog, 1);
            }
            if (par1MovingObjectPosition.field_72308_g instanceof Brutalfly) {
                if (this.field_70146_Z.nextInt(10) < 5) {
                    if (!this.field_70170_p.field_72995_K) {
                        this.func_145779_a(OreSpawnMain.CageEmpty, 1);
                        this.func_70106_y();
                    }
                    return;
                }
                par1MovingObjectPosition.field_72308_g.func_70106_y();
                this.func_145779_a(OreSpawnMain.CagedBrutalfly, 1);
            }
            if (par1MovingObjectPosition.field_72308_g instanceof Nastysaurus) {
                if (this.field_70146_Z.nextInt(10) < 7) {
                    if (!this.field_70170_p.field_72995_K) {
                        this.func_145779_a(OreSpawnMain.CageEmpty, 1);
                        this.func_70106_y();
                    }
                    return;
                }
                par1MovingObjectPosition.field_72308_g.func_70106_y();
                this.func_145779_a(OreSpawnMain.CagedNastysaurus, 1);
            }
            if (par1MovingObjectPosition.field_72308_g instanceof Pointysaurus) {
                if (this.field_70146_Z.nextInt(10) < 2) {
                    if (!this.field_70170_p.field_72995_K) {
                        this.func_145779_a(OreSpawnMain.CageEmpty, 1);
                        this.func_70106_y();
                    }
                    return;
                }
                par1MovingObjectPosition.field_72308_g.func_70106_y();
                this.func_145779_a(OreSpawnMain.CagedPointysaurus, 1);
            }
        } else if (!this.field_70170_p.field_72995_K) {
            this.func_145779_a(OreSpawnMain.CageEmpty, 1);
        }
        if (!this.field_70170_p.field_72995_K) {
            this.func_70106_y();
        }
    }

    public void func_70071_h_() {
        super.func_70071_h_();
        this.my_rotation += 20.0f;
        while (this.my_rotation > 360.0f) {
            this.my_rotation -= 360.0f;
        }
        this.field_70125_A = this.field_70127_C = this.my_rotation;
    }
}

