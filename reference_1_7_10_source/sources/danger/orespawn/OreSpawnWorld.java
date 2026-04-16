/*
 * Decompiled with CFR 0.152.
 */
package danger.orespawn;

import cpw.mods.fml.common.IWorldGenerator;
import danger.orespawn.ItemAppleSeed;
import danger.orespawn.ItemMagicApple;
import danger.orespawn.OreSpawnMain;
import danger.orespawn.Trees;
import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLeaves;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.IInventory;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.tileentity.TileEntityMobSpawner;
import net.minecraft.util.WeightedRandomChestContent;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.feature.WorldGenMinable;

public class OreSpawnWorld
implements IWorldGenerator {
    public static int recently_placed = 50;

    public void generate(Random random, int chunkX, int chunkZ, World world, IChunkProvider chunkGenerator, IChunkProvider chunkProvider) {
        if (world.field_72995_K) {
            return;
        }
        Chunk chunk = world.func_72964_e(chunkX, chunkZ);
        if (recently_placed > 0) {
            --recently_placed;
        }
        if (world.field_73011_w.field_76574_g == OreSpawnMain.DimensionID) {
            this.generateSurface(world, random, chunkX * 16, chunkZ * 16);
            if (!this.addHugeTree(world, random, chunkX * 16, chunkZ * 16, chunk)) {
                if (!this.addAppleTrees(world, random, chunkX * 16, chunkZ * 16, chunk) && !this.addOtherTrees(world, random, chunkX * 16, chunkZ * 16) && recently_placed == 0) {
                    this.addKingAltar(world, random, chunkX * 16, chunkZ * 16);
                }
                this.addVeggies(world, random, chunkX * 16, chunkZ * 16);
            }
            boolean rbd = false;
            rbd = this.addRubyDungeon(world, random, chunkX * 16, chunkZ * 16);
            if (!rbd) {
                this.addGenericDungeon(world, random, chunkX * 16, chunkZ * 16);
            }
            return;
        }
        if (world.field_73011_w.field_76574_g == OreSpawnMain.DimensionID2) {
            int i;
            this.generateRuby(world, random, chunkX * 16, chunkZ * 16);
            if (OreSpawnMain.LessOre == 0) {
                int randPosZ;
                int randPosY;
                int randPosX;
                this.generateRuby(world, random, chunkX * 16, chunkZ * 16);
                this.generateRuby(world, random, chunkX * 16, chunkZ * 16);
                for (i = 0; i < 45; ++i) {
                    randPosX = chunkX * 16 + random.nextInt(16);
                    randPosY = random.nextInt(128);
                    randPosZ = chunkZ * 16 + random.nextInt(16);
                    if (randPosY >= 50) continue;
                    new WorldGenMinable(Blocks.field_150369_x, 7).func_76484_a(world, random, randPosX, randPosY, randPosZ);
                }
                for (i = 0; i < 25; ++i) {
                    randPosX = chunkX * 16 + random.nextInt(16);
                    randPosY = random.nextInt(128);
                    randPosZ = chunkZ * 16 + random.nextInt(16);
                    if (randPosY >= 50) continue;
                    new WorldGenMinable(Blocks.field_150369_x, 4).func_76484_a(world, random, randPosX, randPosY, randPosZ);
                }
            }
            if (recently_placed == 0 && random.nextInt(95) == 1) {
                i = random.nextInt(7);
                if (i == 0) {
                    this.addBasiliskMaze(world, random, chunkX * 16, chunkZ * 16);
                }
                if (i == 1) {
                    this.addKyuubiDungeon(world, random, chunkX * 16, chunkZ * 16);
                }
                if (i == 2) {
                    this.addBeeHive(world, random, chunkX * 16, chunkZ * 16);
                }
                if (i == 3) {
                    this.addShadowDungeon(world, random, chunkX * 16, chunkZ * 16);
                }
                if (i == 4) {
                    this.addAlienWTF(world, random, chunkX * 16, chunkZ * 16);
                }
                if (i == 5) {
                    this.addEnderKnight(world, random, chunkX * 16, chunkZ * 16);
                }
                if (i == 6) {
                    this.addLeonNest(world, random, chunkX * 16, chunkZ * 16);
                }
            } else {
                this.addGenericDungeon(world, random, chunkX * 16, chunkZ * 16);
            }
            this.addLavaAndWater(world, random, chunkX * 16, chunkZ * 16);
            this.addAnts(world, random, chunkX * 16, chunkZ * 16, 2);
            this.addAnts(world, random, chunkX * 16, chunkZ * 16, 2);
            this.addMosquitos(world, random, chunkX * 16, chunkZ * 16);
            this.addMosquitos(world, random, chunkX * 16, chunkZ * 16);
            this.addVeggies(world, random, chunkX * 16, chunkZ * 16);
            this.addRocks(world, random, chunkX * 16, chunkZ * 16);
            return;
        }
        if (world.field_73011_w.field_76574_g == OreSpawnMain.DimensionID3) {
            if (OreSpawnMain.MosquitoEnable != 0) {
                this.addMosquitos(world, random, chunkX, chunkZ);
            }
            this.addAnts(world, random, chunkX * 16, chunkZ * 16, 4);
            this.addAppleTrees(world, random, chunkX * 16, chunkZ * 16, chunk);
            this.addGenericDungeon(world, random, chunkX * 16, chunkZ * 16);
            if (recently_placed == 0) {
                this.addDamselInDistress(world, random, chunkX * 16, chunkZ * 16);
            }
            if (recently_placed == 0) {
                this.addSpiderHangout(world, random, chunkX * 16, chunkZ * 16);
            }
            if (recently_placed == 0) {
                this.addRedAntHangout(world, random, chunkX * 16, chunkZ * 16);
            }
            return;
        }
        if (world.field_73011_w.field_76574_g == OreSpawnMain.DimensionID4) {
            int i;
            if (recently_placed == 0 && random.nextInt(100) == 0 && this.D4BigSpaceCheck(world, chunkX * 16, 7, chunkZ * 16)) {
                i = random.nextInt(19);
                if (i < 3) {
                    this.addD4Castle(world, random, chunkX * 16, chunkZ * 16);
                } else if (i < 7) {
                    this.addD4GenericDungeon(world, random, chunkX * 16, chunkZ * 16);
                } else {
                    if (i == 7) {
                        this.addD4EnderCastle(world, random, chunkX * 16, chunkZ * 16);
                    }
                    if (i == 8) {
                        this.addD4IncaPyramid(world, random, chunkX * 16, chunkZ * 16);
                    }
                    if (i == 9) {
                        this.addD4RobotLab(world, random, chunkX * 16, chunkZ * 16);
                    }
                    if (i == 10) {
                        this.addD4Mini(world, random, chunkX * 16, chunkZ * 16);
                    }
                    if (i == 11) {
                        this.addD4RubyDungeon(world, random, chunkX * 16, chunkZ * 16);
                    }
                    if (i == 12) {
                        this.addD4CephadromeAltar(world, random, chunkX * 16, chunkZ * 16);
                    }
                    if (i == 13) {
                        this.addD4Greenhouse(world, random, chunkX * 16, chunkZ * 16);
                    }
                    if (i == 14) {
                        this.addD4NightmareRookery(world, random, chunkX * 16, chunkZ * 16);
                    }
                    if (i == 15) {
                        this.addD4StinkyHouse(world, random, chunkX * 16, chunkZ * 16);
                    }
                    if (i == 16) {
                        this.addD4WhiteHouse(world, random, chunkX * 16, chunkZ * 16);
                    }
                    if (i == 17) {
                        this.addPumpkin(world, random, chunkX * 16, chunkZ * 16);
                    }
                    if (i == 18) {
                        this.addD4Rainbow(world, random, chunkX * 16, chunkZ * 16);
                    }
                }
            }
            if ((i = random.nextInt(300)) == 0) {
                this.addD4CloudShark(world, random, chunkX * 16, chunkZ * 16);
            }
            this.addUnstableAnts(world, random, chunkX * 16, chunkZ * 16);
            this.addIslands(world, random, chunkX * 16, chunkZ * 16);
            this.addD4Rocks(world, random, chunkX * 16, chunkZ * 16);
            return;
        }
        if (world.field_73011_w.field_76574_g == OreSpawnMain.DimensionID5) {
            if (!this.addFairyTree(world, random, chunkX * 16, chunkZ * 16)) {
                this.addCrystalTermites(world, random, chunkX * 16, chunkZ * 16);
                if (recently_placed == 0) {
                    if (!(this.addRotatorStation(world, random, chunkX * 16, chunkZ * 16) || this.addUrchinSpawner(world, random, chunkX * 16, chunkZ * 16) || this.addCrystalHauntedHouse(world, random, chunkX * 16, chunkZ * 16) || this.addRoundRotator(world, random, chunkX * 16, chunkZ * 16))) {
                        this.addCrystalBattleTower(world, random, chunkX * 16, chunkZ * 16);
                    }
                    this.addIrukandji(world, random, chunkX * 16, chunkZ * 16);
                }
            }
            this.addCrystalChestsAndSpawners(world, random, chunkX * 16, chunkZ * 16);
            if (world.field_73012_v.nextInt(4) == 1) {
                this.addRocks(world, random, chunkX * 16, chunkZ * 16);
            }
            return;
        }
        if (world.field_73011_w.field_76574_g == OreSpawnMain.DimensionID6) {
            this.addButterfliesAndMoths(world, random, chunkX * 16, chunkZ * 16);
            this.addVeggies(world, random, chunkX * 16, chunkZ * 16);
            this.addAnts(world, random, chunkX * 16, chunkZ * 16, 2);
            return;
        }
        switch (world.field_73011_w.field_76574_g) {
            case -1: {
                this.generateNether(world, random, chunkX * 16, chunkZ * 16);
                break;
            }
            case 0: {
                this.generateSurface(world, random, chunkX * 16, chunkZ * 16);
                this.generateOres(world, random, chunkX * 16, chunkZ * 16);
                break;
            }
            case 1: {
                this.generateEnd(world, random, chunkX * 16, chunkZ * 16);
                break;
            }
        }
    }

    private void generateEnd(World world, Random random, int chunkX, int chunkZ) {
        this.addEndAnts(world, random, chunkX, chunkZ);
        int i = world.field_73012_v.nextInt(4);
        if (i == 0) {
            this.addEndKnights(world, random, chunkX, chunkZ);
        }
        if (i == 1) {
            this.addEndReapers(world, random, chunkX, chunkZ);
        }
        if (i == 2) {
            this.addHospital(world, random, chunkX, chunkZ);
        }
        if (i == 3) {
            this.addEnderCastle(world, random, chunkX, chunkZ);
        }
    }

    private void generateNether(World world, Random random, int chunkX, int chunkZ) {
        int randPosZ;
        int randPosY;
        int randPosX;
        int i;
        if (OreSpawnMain.MosquitoEnable != 0) {
            this.addNetherMosquitos(world, random, chunkX, chunkZ);
        }
        this.addNetherAnts(world, random, chunkX, chunkZ);
        int patchy = 15 + random.nextInt(10);
        if (OreSpawnMain.LessOre != 0) {
            patchy /= 3;
        }
        for (i = 0; i < patchy; ++i) {
            randPosX = 3 + chunkX + random.nextInt(13);
            randPosY = random.nextInt(108) + 10;
            randPosZ = 3 + chunkZ + random.nextInt(13);
            new WorldGenMinable(OreSpawnMain.MyLavafoamBlock, 6, Blocks.field_150424_aL).func_76484_a(world, random, randPosX, randPosY, randPosZ);
        }
        patchy = 5 + random.nextInt(5);
        if (OreSpawnMain.LessOre != 0) {
            patchy /= 3;
        }
        for (i = 0; i < patchy; ++i) {
            randPosX = 3 + chunkX + random.nextInt(13);
            randPosY = random.nextInt(108) + 10;
            randPosZ = 3 + chunkZ + random.nextInt(13);
            new WorldGenMinable(OreSpawnMain.MyOreRubyBlock, 2, Blocks.field_150424_aL).func_76484_a(world, random, randPosX, randPosY, randPosZ);
        }
    }

    public void generateSurface(World world, Random random, int chunkX, int chunkZ) {
        boolean ahh = false;
        this.addStrawberries(world, random, chunkX, chunkZ);
        this.addCorn(world, random, chunkX, chunkZ);
        this.addTomatoes(world, random, chunkX, chunkZ);
        this.addVeggies(world, random, chunkX, chunkZ);
        this.addButterfliesAndMoths(world, random, chunkX, chunkZ);
        if (OreSpawnMain.MosquitoEnable != 0) {
            this.addMosquitos(world, random, chunkX, chunkZ);
        }
        if (OreSpawnMain.DisableOverworldDungeons == 0 && world.field_73011_w.field_76574_g == 0 && recently_placed == 0) {
            int i = world.field_73012_v.nextInt(6);
            if (i == 0) {
                this.addPlayPool(world, random, chunkX, chunkZ);
            }
            if (i == 1) {
                this.addWaterDragonLair(world, random, chunkX, chunkZ);
            }
            if (i == 2) {
                this.addGoldFishBowl(world, random, chunkX, chunkZ);
            }
            if (i == 3) {
                this.addGirlfriendIsland(world, random, chunkX, chunkZ);
            }
            if (i == 4) {
                this.addMonsterIsland(world, random, chunkX, chunkZ);
            }
            if (i == 5) {
                this.addFrogPond(world, random, chunkX, chunkZ);
            }
            if (!(ahh = this.addANest(world, random, chunkX, chunkZ))) {
                ahh = this.addHauntedHouse(world, random, chunkX, chunkZ);
            }
            if (!ahh) {
                ahh = this.addLeafMonster(world, random, chunkX, chunkZ);
            }
            if (!ahh) {
                ahh = this.addSpitBug(world, random, chunkX, chunkZ);
            }
            if (!ahh) {
                ahh = this.addIgloo(world, random, chunkX, chunkZ);
            }
            if (!ahh) {
                ahh = this.addBouncyCastle(world, random, chunkX, chunkZ);
            }
            if (!ahh) {
                ahh = this.addRubberDuckyPond(world, random, chunkX, chunkZ);
            }
        }
        this.addAnts(world, random, chunkX, chunkZ, 4);
        BiomeGenBase b = world.func_72807_a(chunkX, chunkZ);
        if (b.field_76791_y.equals("River") || b.field_76791_y.equals("Extreme Hills") || b.field_76791_y.equals("Desert")) {
            this.addRocks(world, random, chunkX, chunkZ);
        }
    }

    public void generateRuby(World world, Random random, int chunkX, int chunkZ) {
        if (OreSpawnMain.Ruby_stats.rate <= 0) {
            return;
        }
        int patchy = OreSpawnMain.Ruby_stats.rate + random.nextInt(7);
        block0: for (int i = 0; i < patchy; ++i) {
            int randPosX = 3 + chunkX + random.nextInt(10);
            int randPosY = random.nextInt(128);
            int randPosZ = 3 + chunkZ + random.nextInt(10);
            if (randPosY > OreSpawnMain.Ruby_stats.maxdepth || randPosY < OreSpawnMain.Ruby_stats.mindepth) continue;
            for (int m = randPosY; m > 5; --m) {
                Block bid = world.func_147439_a(randPosX, m, randPosZ);
                if (bid != Blocks.field_150353_l && bid != Blocks.field_150356_k || (bid = world.func_147439_a(randPosX, m - 1, randPosZ)) != Blocks.field_150348_b) continue;
                OreSpawnMain.setBlockFast((World)world, (int)randPosX, (int)(m - 1), (int)randPosZ, (Block)OreSpawnMain.MyOreRubyBlock, (int)0, (int)2);
                continue block0;
            }
        }
    }

    public void generateOres(World world, Random random, int chunkX, int chunkZ) {
        int randPosZ;
        int randPosY;
        int randPosX;
        int i;
        int patchy;
        if (OreSpawnMain.SpawnOres_stats.rate > 0) {
            patchy = OreSpawnMain.SpawnOres_stats.rate + random.nextInt(20);
            if (random.nextInt(20) == 0) {
                patchy += 30;
            }
            if (OreSpawnMain.LessOre != 0) {
                patchy /= 3;
            }
            for (i = 0; i < patchy; ++i) {
                Block b;
                int j;
                randPosX = 3 + chunkX + random.nextInt(10);
                randPosY = random.nextInt(128);
                randPosZ = 3 + chunkZ + random.nextInt(10);
                if (randPosY > OreSpawnMain.SpawnOres_stats.maxdepth || randPosY < OreSpawnMain.SpawnOres_stats.mindepth) continue;
                if (random.nextInt(104) < 7) {
                    j = random.nextInt(7);
                    b = Blocks.field_150350_a;
                    switch (j) {
                        case 0: {
                            b = OreSpawnMain.MyBrutalflySpawnBlock;
                            break;
                        }
                        case 1: {
                            b = OreSpawnMain.MyNastysaurusSpawnBlock;
                            break;
                        }
                        case 2: {
                            b = OreSpawnMain.MyPointysaurusSpawnBlock;
                            break;
                        }
                        case 3: {
                            b = OreSpawnMain.MyCricketSpawnBlock;
                            break;
                        }
                        case 4: {
                            b = OreSpawnMain.MyFrogSpawnBlock;
                            break;
                        }
                        case 5: {
                            b = OreSpawnMain.MySpiderDriverSpawnBlock;
                            break;
                        }
                        case 6: {
                            b = OreSpawnMain.MyCrabSpawnBlock;
                            break;
                        }
                    }
                    new WorldGenMinable(b, OreSpawnMain.SpawnOres_stats.clumpsize).func_76484_a(world, random, randPosX, randPosY, randPosZ);
                    continue;
                }
                j = random.nextInt(98);
                b = Blocks.field_150350_a;
                switch (j) {
                    case 0: {
                        b = OreSpawnMain.MySpiderSpawnBlock;
                        break;
                    }
                    case 1: {
                        b = OreSpawnMain.MyBatSpawnBlock;
                        break;
                    }
                    case 2: {
                        b = OreSpawnMain.MyCowSpawnBlock;
                        break;
                    }
                    case 3: {
                        b = OreSpawnMain.MyPigSpawnBlock;
                        break;
                    }
                    case 4: {
                        b = OreSpawnMain.MySquidSpawnBlock;
                        break;
                    }
                    case 5: {
                        b = OreSpawnMain.MyChickenSpawnBlock;
                        break;
                    }
                    case 6: {
                        b = OreSpawnMain.MyCreeperSpawnBlock;
                        break;
                    }
                    case 7: {
                        b = OreSpawnMain.MySkeletonSpawnBlock;
                        break;
                    }
                    case 8: {
                        b = OreSpawnMain.MyZombieSpawnBlock;
                        break;
                    }
                    case 9: {
                        b = OreSpawnMain.MySlimeSpawnBlock;
                        break;
                    }
                    case 10: {
                        b = OreSpawnMain.MyGhastSpawnBlock;
                        break;
                    }
                    case 11: {
                        b = OreSpawnMain.MyZombiePigmanSpawnBlock;
                        break;
                    }
                    case 12: {
                        b = OreSpawnMain.MyEndermanSpawnBlock;
                        break;
                    }
                    case 13: {
                        b = OreSpawnMain.MyCaveSpiderSpawnBlock;
                        break;
                    }
                    case 14: {
                        b = OreSpawnMain.MySilverfishSpawnBlock;
                        break;
                    }
                    case 15: {
                        b = OreSpawnMain.MyMagmaCubeSpawnBlock;
                        break;
                    }
                    case 16: {
                        b = OreSpawnMain.MyWitchSpawnBlock;
                        break;
                    }
                    case 17: {
                        b = OreSpawnMain.MySheepSpawnBlock;
                        break;
                    }
                    case 18: {
                        b = OreSpawnMain.MyWolfSpawnBlock;
                        break;
                    }
                    case 19: {
                        b = OreSpawnMain.MyMooshroomSpawnBlock;
                        break;
                    }
                    case 20: {
                        b = OreSpawnMain.MyOcelotSpawnBlock;
                        break;
                    }
                    case 21: {
                        b = OreSpawnMain.MyBlazeSpawnBlock;
                        break;
                    }
                    case 22: {
                        b = OreSpawnMain.MyWitherSkeletonSpawnBlock;
                        break;
                    }
                    case 23: {
                        b = OreSpawnMain.MyEnderDragonSpawnBlock;
                        break;
                    }
                    case 24: {
                        b = OreSpawnMain.MySnowGolemSpawnBlock;
                        break;
                    }
                    case 25: {
                        b = OreSpawnMain.MyIronGolemSpawnBlock;
                        break;
                    }
                    case 26: {
                        b = OreSpawnMain.MyWitherBossSpawnBlock;
                        break;
                    }
                    case 27: {
                        b = OreSpawnMain.MyGirlfriendSpawnBlock;
                        break;
                    }
                    case 28: {
                        b = OreSpawnMain.MyRedCowSpawnBlock;
                        break;
                    }
                    case 29: {
                        b = OreSpawnMain.MyGoldCowSpawnBlock;
                        break;
                    }
                    case 30: {
                        b = OreSpawnMain.MyEnchantedCowSpawnBlock;
                        break;
                    }
                    case 31: {
                        b = OreSpawnMain.MyMOTHRASpawnBlock;
                        break;
                    }
                    case 32: {
                        b = OreSpawnMain.MyAloSpawnBlock;
                        break;
                    }
                    case 33: {
                        b = OreSpawnMain.MyCryoSpawnBlock;
                        break;
                    }
                    case 34: {
                        b = OreSpawnMain.MyCamaSpawnBlock;
                        break;
                    }
                    case 35: {
                        b = OreSpawnMain.MyVeloSpawnBlock;
                        break;
                    }
                    case 36: {
                        b = OreSpawnMain.MyHydroSpawnBlock;
                        break;
                    }
                    case 37: {
                        b = OreSpawnMain.MyBasilSpawnBlock;
                        break;
                    }
                    case 38: {
                        b = OreSpawnMain.MyDragonflySpawnBlock;
                        break;
                    }
                    case 39: {
                        b = OreSpawnMain.MyEmperorScorpionSpawnBlock;
                        break;
                    }
                    case 40: {
                        b = OreSpawnMain.MyScorpionSpawnBlock;
                        break;
                    }
                    case 41: {
                        b = OreSpawnMain.MyCaveFisherSpawnBlock;
                        break;
                    }
                    case 42: {
                        b = OreSpawnMain.MySpyroSpawnBlock;
                        break;
                    }
                    case 43: {
                        b = OreSpawnMain.MyBaryonyxSpawnBlock;
                        break;
                    }
                    case 44: {
                        b = OreSpawnMain.MyGammaMetroidSpawnBlock;
                        break;
                    }
                    case 45: {
                        b = OreSpawnMain.MyCockateilSpawnBlock;
                        break;
                    }
                    case 46: {
                        b = OreSpawnMain.MyKyuubiSpawnBlock;
                        break;
                    }
                    case 47: {
                        b = OreSpawnMain.MyAlienSpawnBlock;
                        break;
                    }
                    case 48: {
                        b = OreSpawnMain.MyAttackSquidSpawnBlock;
                        break;
                    }
                    case 49: {
                        b = OreSpawnMain.MyWaterDragonSpawnBlock;
                        break;
                    }
                    case 50: {
                        b = OreSpawnMain.MyKrakenSpawnBlock;
                        break;
                    }
                    case 51: {
                        b = OreSpawnMain.MyLizardSpawnBlock;
                        break;
                    }
                    case 52: {
                        b = OreSpawnMain.MyCephadromeSpawnBlock;
                        break;
                    }
                    case 53: {
                        b = OreSpawnMain.MyDragonSpawnBlock;
                        break;
                    }
                    case 54: {
                        b = OreSpawnMain.MyBeeSpawnBlock;
                        break;
                    }
                    case 55: {
                        b = OreSpawnMain.MyHorseSpawnBlock;
                        break;
                    }
                    case 56: {
                        b = OreSpawnMain.MyTrooperBugSpawnBlock;
                        break;
                    }
                    case 57: {
                        b = OreSpawnMain.MySpitBugSpawnBlock;
                        break;
                    }
                    case 58: {
                        b = OreSpawnMain.MyStinkBugSpawnBlock;
                        break;
                    }
                    case 59: {
                        b = OreSpawnMain.MyOstrichSpawnBlock;
                        break;
                    }
                    case 60: {
                        b = OreSpawnMain.MyGazelleSpawnBlock;
                        break;
                    }
                    case 61: {
                        b = OreSpawnMain.MyChipmunkSpawnBlock;
                        break;
                    }
                    case 62: {
                        b = OreSpawnMain.MyCreepingHorrorSpawnBlock;
                        break;
                    }
                    case 63: {
                        b = OreSpawnMain.MyTerribleTerrorSpawnBlock;
                        break;
                    }
                    case 64: {
                        b = OreSpawnMain.MyCliffRacerSpawnBlock;
                        break;
                    }
                    case 65: {
                        b = OreSpawnMain.MyTriffidSpawnBlock;
                        break;
                    }
                    case 66: {
                        b = OreSpawnMain.MyPitchBlackSpawnBlock;
                        break;
                    }
                    case 67: {
                        b = OreSpawnMain.MyLurkingTerrorSpawnBlock;
                        break;
                    }
                    case 68: {
                        b = OreSpawnMain.MyGodzillaPartSpawnBlock;
                        break;
                    }
                    case 69: {
                        b = OreSpawnMain.MyGodzillaSpawnBlock;
                        break;
                    }
                    case 70: {
                        b = OreSpawnMain.MySmallWormSpawnBlock;
                        break;
                    }
                    case 71: {
                        b = OreSpawnMain.MyMediumWormSpawnBlock;
                        break;
                    }
                    case 72: {
                        b = OreSpawnMain.MyLargeWormSpawnBlock;
                        break;
                    }
                    case 73: {
                        b = OreSpawnMain.MyCassowarySpawnBlock;
                        break;
                    }
                    case 74: {
                        b = OreSpawnMain.MyCloudSharkSpawnBlock;
                        break;
                    }
                    case 75: {
                        b = OreSpawnMain.MyGoldFishSpawnBlock;
                        break;
                    }
                    case 76: {
                        b = OreSpawnMain.MyLeafMonsterSpawnBlock;
                        break;
                    }
                    case 77: {
                        b = OreSpawnMain.MyTshirtSpawnBlock;
                        break;
                    }
                    case 78: {
                        b = OreSpawnMain.MyEnderKnightSpawnBlock;
                        break;
                    }
                    case 79: {
                        b = OreSpawnMain.MyEnderReaperSpawnBlock;
                        break;
                    }
                    case 80: {
                        b = OreSpawnMain.MyBeaverSpawnBlock;
                        break;
                    }
                    case 81: {
                        b = OreSpawnMain.MyTRexSpawnBlock;
                        break;
                    }
                    case 82: {
                        b = OreSpawnMain.MyHerculesSpawnBlock;
                        break;
                    }
                    case 83: {
                        b = OreSpawnMain.MyMantisSpawnBlock;
                        break;
                    }
                    case 84: {
                        b = OreSpawnMain.MyStinkySpawnBlock;
                        break;
                    }
                    case 85: {
                        b = OreSpawnMain.MyBoyfriendSpawnBlock;
                        break;
                    }
                    case 86: {
                        b = OreSpawnMain.MyTheKingPartSpawnBlock;
                        break;
                    }
                    case 87: {
                        b = OreSpawnMain.MyEasterBunnySpawnBlock;
                        break;
                    }
                    case 88: {
                        b = OreSpawnMain.MyCaterKillerSpawnBlock;
                        break;
                    }
                    case 89: {
                        b = OreSpawnMain.MyMolenoidSpawnBlock;
                        break;
                    }
                    case 90: {
                        b = OreSpawnMain.MySeaMonsterSpawnBlock;
                        break;
                    }
                    case 91: {
                        b = OreSpawnMain.MySeaViperSpawnBlock;
                        break;
                    }
                    case 92: {
                        b = OreSpawnMain.MyLeonSpawnBlock;
                        break;
                    }
                    case 93: {
                        b = OreSpawnMain.MyHammerheadSpawnBlock;
                        break;
                    }
                    case 94: {
                        b = OreSpawnMain.MyRubberDuckySpawnBlock;
                        break;
                    }
                    case 95: {
                        b = OreSpawnMain.MyVillagerSpawnBlock;
                        break;
                    }
                    case 96: {
                        b = OreSpawnMain.MyCriminalSpawnBlock;
                        break;
                    }
                    case 97: {
                        b = OreSpawnMain.MyTheQueenPartSpawnBlock;
                        break;
                    }
                }
                new WorldGenMinable(b, OreSpawnMain.SpawnOres_stats.clumpsize).func_76484_a(world, random, randPosX, randPosY, randPosZ);
            }
        }
        if (OreSpawnMain.Uranium_stats.rate > 0) {
            patchy = OreSpawnMain.Uranium_stats.rate + random.nextInt(9);
            if (OreSpawnMain.LessOre != 0) {
                patchy /= 3;
            }
            for (i = 0; i < patchy; ++i) {
                randPosX = 3 + chunkX + random.nextInt(10);
                randPosY = random.nextInt(128);
                randPosZ = 3 + chunkZ + random.nextInt(10);
                if (randPosY > OreSpawnMain.Uranium_stats.maxdepth || randPosY < OreSpawnMain.Uranium_stats.mindepth) continue;
                new WorldGenMinable(OreSpawnMain.MyOreUraniumBlock, OreSpawnMain.Uranium_stats.clumpsize).func_76484_a(world, random, randPosX, randPosY, randPosZ);
            }
        }
        if (OreSpawnMain.Titanium_stats.rate > 0) {
            patchy = OreSpawnMain.Titanium_stats.rate + random.nextInt(9);
            if (OreSpawnMain.LessOre != 0) {
                patchy /= 3;
            }
            for (i = 0; i < patchy; ++i) {
                randPosX = 3 + chunkX + random.nextInt(10);
                randPosY = random.nextInt(128);
                randPosZ = 3 + chunkZ + random.nextInt(10);
                if (randPosY > OreSpawnMain.Titanium_stats.maxdepth || randPosY < OreSpawnMain.Titanium_stats.mindepth) continue;
                new WorldGenMinable(OreSpawnMain.MyOreTitaniumBlock, OreSpawnMain.Titanium_stats.clumpsize).func_76484_a(world, random, randPosX, randPosY, randPosZ);
            }
        }
        if (OreSpawnMain.Amethyst_stats.rate > 0) {
            patchy = OreSpawnMain.Amethyst_stats.rate + random.nextInt(12);
            if (OreSpawnMain.LessOre != 0) {
                patchy /= 3;
            }
            for (i = 0; i < patchy; ++i) {
                randPosX = 3 + chunkX + random.nextInt(10);
                randPosY = random.nextInt(128);
                randPosZ = 3 + chunkZ + random.nextInt(10);
                if (randPosY > OreSpawnMain.Amethyst_stats.maxdepth || randPosY < OreSpawnMain.Amethyst_stats.mindepth) continue;
                new WorldGenMinable(OreSpawnMain.MyOreAmethystBlock, OreSpawnMain.Amethyst_stats.clumpsize).func_76484_a(world, random, randPosX, randPosY, randPosZ);
            }
        }
        if (OreSpawnMain.Salt_stats.rate > 0) {
            patchy = OreSpawnMain.Salt_stats.rate + random.nextInt(9);
            if (OreSpawnMain.LessOre != 0) {
                patchy /= 3;
            }
            for (i = 0; i < patchy; ++i) {
                randPosX = 3 + chunkX + random.nextInt(10);
                randPosY = random.nextInt(128);
                randPosZ = 3 + chunkZ + random.nextInt(10);
                if (randPosY > OreSpawnMain.Salt_stats.maxdepth || randPosY < OreSpawnMain.Salt_stats.mindepth) continue;
                new WorldGenMinable(OreSpawnMain.MyOreSaltBlock, OreSpawnMain.Salt_stats.clumpsize).func_76484_a(world, random, randPosX, randPosY, randPosZ);
            }
        }
        patchy = 4 + random.nextInt(4);
        if (OreSpawnMain.LessOre != 0) {
            patchy /= 2;
        }
        for (i = 0; i < patchy; ++i) {
            randPosX = 3 + chunkX + random.nextInt(10);
            randPosY = random.nextInt(128);
            randPosZ = 3 + chunkZ + random.nextInt(10);
            if (randPosY > 50 || randPosY < 5) continue;
            new WorldGenMinable(OreSpawnMain.RedAntTroll, 4).func_76484_a(world, random, randPosX, randPosY, randPosZ);
        }
        patchy = 4 + random.nextInt(4);
        if (OreSpawnMain.LessOre != 0) {
            patchy /= 2;
        }
        for (i = 0; i < patchy; ++i) {
            randPosX = 3 + chunkX + random.nextInt(10);
            randPosY = random.nextInt(128);
            randPosZ = 3 + chunkZ + random.nextInt(10);
            if (randPosY > 50 || randPosY < 5) continue;
            new WorldGenMinable(OreSpawnMain.TermiteTroll, 4).func_76484_a(world, random, randPosX, randPosY, randPosZ);
        }
        if (OreSpawnMain.Ruby_stats.rate > 0) {
            patchy = OreSpawnMain.Ruby_stats.rate + random.nextInt(5);
            block116: for (i = 0; i < patchy; ++i) {
                randPosX = 3 + chunkX + random.nextInt(10);
                randPosY = random.nextInt(128);
                randPosZ = 3 + chunkZ + random.nextInt(10);
                if (randPosY > OreSpawnMain.Ruby_stats.maxdepth || randPosY < OreSpawnMain.Ruby_stats.mindepth) continue;
                for (int m = randPosY; m > 5; --m) {
                    Block bid = world.func_147439_a(randPosX, m, randPosZ);
                    if (bid != Blocks.field_150353_l && bid != Blocks.field_150356_k || (bid = world.func_147439_a(randPosX, m - 1, randPosZ)) != Blocks.field_150348_b) continue;
                    OreSpawnMain.setBlockFast((World)world, (int)randPosX, (int)(m - 1), (int)randPosZ, (Block)OreSpawnMain.MyOreRubyBlock, (int)0, (int)2);
                    continue block116;
                }
            }
        }
        if (OreSpawnMain.LessOre == 0) {
            if (OreSpawnMain.Diamond_stats.rate > 0) {
                for (i = 0; i < OreSpawnMain.Diamond_stats.rate; ++i) {
                    randPosX = 3 + chunkX + random.nextInt(10);
                    randPosY = random.nextInt(128);
                    randPosZ = 3 + chunkZ + random.nextInt(10);
                    if (randPosY > OreSpawnMain.Diamond_stats.maxdepth || randPosY < OreSpawnMain.Diamond_stats.mindepth) continue;
                    new WorldGenMinable(Blocks.field_150482_ag, OreSpawnMain.Diamond_stats.clumpsize).func_76484_a(world, random, randPosX, randPosY, randPosZ);
                }
            }
            if (OreSpawnMain.BlkDiamond_stats.rate > 0) {
                for (i = 0; i < OreSpawnMain.BlkDiamond_stats.rate; ++i) {
                    randPosX = 3 + chunkX + random.nextInt(10);
                    randPosY = random.nextInt(128);
                    randPosZ = 3 + chunkZ + random.nextInt(10);
                    if (randPosY > OreSpawnMain.BlkDiamond_stats.maxdepth || randPosY < OreSpawnMain.BlkDiamond_stats.mindepth) continue;
                    new WorldGenMinable(Blocks.field_150484_ah, OreSpawnMain.BlkDiamond_stats.clumpsize).func_76484_a(world, random, randPosX, randPosY, randPosZ);
                }
            }
            if (OreSpawnMain.Emerald_stats.rate > 0) {
                for (i = 0; i < OreSpawnMain.Emerald_stats.rate; ++i) {
                    randPosX = 3 + chunkX + random.nextInt(10);
                    randPosY = random.nextInt(128);
                    randPosZ = 3 + chunkZ + random.nextInt(10);
                    if (randPosY > OreSpawnMain.Emerald_stats.maxdepth || randPosY < OreSpawnMain.Emerald_stats.mindepth) continue;
                    new WorldGenMinable(Blocks.field_150412_bA, OreSpawnMain.Emerald_stats.clumpsize).func_76484_a(world, random, randPosX, randPosY, randPosZ);
                }
            }
            if (OreSpawnMain.BlkEmerald_stats.rate > 0) {
                for (i = 0; i < OreSpawnMain.BlkEmerald_stats.rate; ++i) {
                    randPosX = 3 + chunkX + random.nextInt(10);
                    randPosY = random.nextInt(128);
                    randPosZ = 3 + chunkZ + random.nextInt(10);
                    if (randPosY > OreSpawnMain.BlkEmerald_stats.maxdepth || randPosY < OreSpawnMain.BlkEmerald_stats.mindepth) continue;
                    new WorldGenMinable(Blocks.field_150475_bE, OreSpawnMain.BlkEmerald_stats.clumpsize).func_76484_a(world, random, randPosX, randPosY, randPosZ);
                }
            }
            if (OreSpawnMain.Gold_stats.rate > 0) {
                for (i = 0; i < OreSpawnMain.Gold_stats.rate; ++i) {
                    randPosX = 3 + chunkX + random.nextInt(10);
                    randPosY = random.nextInt(128);
                    randPosZ = 3 + chunkZ + random.nextInt(10);
                    if (randPosY > OreSpawnMain.Gold_stats.maxdepth || randPosY < OreSpawnMain.Gold_stats.mindepth) continue;
                    new WorldGenMinable(Blocks.field_150352_o, OreSpawnMain.Gold_stats.clumpsize).func_76484_a(world, random, randPosX, randPosY, randPosZ);
                }
            }
            if (OreSpawnMain.BlkGold_stats.rate > 0) {
                for (i = 0; i < OreSpawnMain.BlkGold_stats.rate; ++i) {
                    randPosX = 3 + chunkX + random.nextInt(10);
                    randPosY = random.nextInt(128);
                    randPosZ = 3 + chunkZ + random.nextInt(10);
                    if (randPosY > OreSpawnMain.BlkGold_stats.maxdepth || randPosY < OreSpawnMain.BlkGold_stats.mindepth) continue;
                    new WorldGenMinable(Blocks.field_150340_R, OreSpawnMain.BlkGold_stats.clumpsize).func_76484_a(world, random, randPosX, randPosY, randPosZ);
                }
            }
            if (OreSpawnMain.BlkRuby_stats.rate > 0) {
                for (i = 0; i < OreSpawnMain.BlkRuby_stats.rate; ++i) {
                    randPosX = 3 + chunkX + random.nextInt(10);
                    randPosY = random.nextInt(128);
                    randPosZ = 3 + chunkZ + random.nextInt(10);
                    if (randPosY > OreSpawnMain.BlkRuby_stats.maxdepth || randPosY < OreSpawnMain.BlkRuby_stats.mindepth) continue;
                    new WorldGenMinable(OreSpawnMain.MyBlockRubyBlock, OreSpawnMain.BlkRuby_stats.clumpsize).func_76484_a(world, random, randPosX, randPosY, randPosZ);
                }
            }
        }
    }

    public void addStrawberries(World world, Random random, int chunkX, int chunkZ) {
        if (random.nextInt(20) != 0) {
            return;
        }
        BiomeGenBase b = world.func_72807_a(chunkX, chunkZ);
        if (world.field_73011_w.field_76574_g == OreSpawnMain.DimensionID || b.field_76791_y.equals("Forest") || b.field_76791_y.equals("ForestHills") || b.field_76791_y.equals("Birch Forest Hills") || b.field_76791_y.equals("Birch Forest")) {
            block0: for (int i = 0; i < 5; ++i) {
                int posX = chunkX + random.nextInt(16);
                int posZ = chunkZ + random.nextInt(16);
                for (int posY = 100; posY > 40 && world.func_147437_c(posX, posY, posZ); --posY) {
                    if (world.func_147439_a(posX, posY - 1, posZ) != Blocks.field_150349_c) continue;
                    OreSpawnMain.setBlockFast((World)world, (int)posX, (int)posY, (int)posZ, (Block)OreSpawnMain.MyStrawberryPlant, (int)0, (int)2);
                    continue block0;
                }
            }
        }
    }

    public boolean addHauntedHouse(World world, Random random, int chunkX, int chunkZ) {
        if (random.nextInt(285) != 0) {
            return false;
        }
        BiomeGenBase b = world.func_72807_a(chunkX, chunkZ);
        if (b.field_76791_y.equals("Plains") || b.field_76791_y.equals("Taiga") || b.field_76791_y.equals("Swampland")) {
            for (int i = 0; i < 5; ++i) {
                int posX = chunkX + random.nextInt(16);
                int posZ = chunkZ + random.nextInt(16);
                for (int posY = 100; posY > 40 && world.func_147437_c(posX, posY, posZ); --posY) {
                    if (world.func_147439_a(posX, posY - 1, posZ) != Blocks.field_150349_c) continue;
                    OreSpawnMain.MyDungeon.makeHauntedHouse(world, posX, posY, posZ);
                    recently_placed = 50;
                    return true;
                }
            }
        }
        return false;
    }

    public boolean addANest(World world, Random random, int chunkX, int chunkZ) {
        if (random.nextInt(230) != 0) {
            return false;
        }
        BiomeGenBase b = world.func_72807_a(chunkX, chunkZ);
        if (b.field_76791_y.equals("Forest") || b.field_76791_y.equals("ForestHills") || b.field_76791_y.equals("Jungle") || b.field_76791_y.equals("JungleHills") || b.field_76791_y.equals("Birch Forest") || b.field_76791_y.equals("Birch Forest Hills")) {
            for (int i = 0; i < 5; ++i) {
                int posX = chunkX + random.nextInt(16);
                int posZ = chunkZ + random.nextInt(16);
                for (int posY = 128; posY > 40 && world.func_147437_c(posX, posY, posZ); --posY) {
                    if (world.func_147439_a(posX, posY - 1, posZ) != Blocks.field_150349_c) continue;
                    if (random.nextInt(2) == 0) {
                        OreSpawnMain.MyDungeon.makeSmallBeeHive(world, posX, posY, posZ);
                    } else {
                        OreSpawnMain.MyDungeon.makeMantisHive(world, posX, posY, posZ);
                    }
                    recently_placed = 50;
                    return true;
                }
            }
        }
        return false;
    }

    public void addCorn(World world, Random random, int chunkX, int chunkZ) {
        boolean is_all_air = true;
        int nc = 6;
        if (random.nextInt(35) != 1) {
            return;
        }
        if (OreSpawnMain.LessLag == 1) {
            nc = 5;
        }
        if (OreSpawnMain.LessLag == 2) {
            nc = 3;
        }
        BiomeGenBase b = world.func_72807_a(chunkX, chunkZ);
        if (world.field_73011_w.field_76574_g == OreSpawnMain.DimensionID || world.field_73011_w.field_76574_g == OreSpawnMain.DimensionID3 || b.field_76791_y.equals("Plains")) {
            block0: for (int j = 0; j < nc; ++j) {
                int posX = chunkX + random.nextInt(16);
                int posZ = chunkZ + random.nextInt(16);
                is_all_air = true;
                for (int posY = 100; posY > 40 && world.func_147437_c(posX, posY, posZ); --posY) {
                    int i;
                    if (world.func_147439_a(posX, posY - 1, posZ) != Blocks.field_150349_c) continue;
                    for (i = 1; i < 10; ++i) {
                        if (world.func_147437_c(posX, posY + i, posZ)) continue;
                        is_all_air = false;
                    }
                    if (!is_all_air) continue block0;
                    int corn_height = random.nextInt(5);
                    if (++corn_height == 1) {
                        OreSpawnMain.setBlockFast((World)world, (int)posX, (int)posY, (int)posZ, (Block)OreSpawnMain.MyCornPlant1, (int)0, (int)2);
                    }
                    if (corn_height == 2) {
                        OreSpawnMain.setBlockFast((World)world, (int)posX, (int)posY, (int)posZ, (Block)OreSpawnMain.MyCornPlant2, (int)0, (int)2);
                        OreSpawnMain.setBlockFast((World)world, (int)posX, (int)(posY + 1), (int)posZ, (Block)OreSpawnMain.MyCornPlant1, (int)0, (int)2);
                    }
                    if (corn_height <= 2) continue block0;
                    OreSpawnMain.setBlockFast((World)world, (int)posX, (int)posY, (int)posZ, (Block)OreSpawnMain.MyCornPlant2, (int)0, (int)2);
                    for (i = 1; i < corn_height; ++i) {
                        OreSpawnMain.setBlockFast((World)world, (int)posX, (int)(posY + i), (int)posZ, (Block)OreSpawnMain.MyCornPlant4, (int)0, (int)2);
                    }
                    OreSpawnMain.setBlockFast((World)world, (int)posX, (int)(posY + corn_height), (int)posZ, (Block)OreSpawnMain.MyCornPlant1, (int)0, (int)2);
                    continue block0;
                }
            }
        }
    }

    public void addTomatoes(World world, Random random, int chunkX, int chunkZ) {
        boolean is_all_air = true;
        if (random.nextInt(70) != 1) {
            return;
        }
        BiomeGenBase b = world.func_72807_a(chunkX, chunkZ);
        if (world.field_73011_w.field_76574_g == OreSpawnMain.DimensionID || world.field_73011_w.field_76574_g == OreSpawnMain.DimensionID3 || b.field_76791_y.equals("Plains")) {
            block0: for (int j = 0; j < 5; ++j) {
                int posX = chunkX + random.nextInt(16);
                int posZ = chunkZ + random.nextInt(16);
                is_all_air = true;
                for (int posY = 100; posY > 40 && world.func_147437_c(posX, posY, posZ); --posY) {
                    int i;
                    if (world.func_147439_a(posX, posY - 1, posZ) != Blocks.field_150349_c) continue;
                    for (i = 1; i < 10; ++i) {
                        if (world.func_147437_c(posX, posY + i, posZ)) continue;
                        is_all_air = false;
                    }
                    if (!is_all_air) continue block0;
                    int corn_height = random.nextInt(3);
                    if (++corn_height == 1) {
                        OreSpawnMain.setBlockFast((World)world, (int)posX, (int)posY, (int)posZ, (Block)OreSpawnMain.MyTomatoPlant1, (int)0, (int)2);
                    }
                    if (corn_height == 2) {
                        OreSpawnMain.setBlockFast((World)world, (int)posX, (int)posY, (int)posZ, (Block)OreSpawnMain.MyTomatoPlant2, (int)0, (int)2);
                        OreSpawnMain.setBlockFast((World)world, (int)posX, (int)(posY + 1), (int)posZ, (Block)OreSpawnMain.MyTomatoPlant1, (int)0, (int)2);
                    }
                    if (corn_height <= 2) continue block0;
                    OreSpawnMain.setBlockFast((World)world, (int)posX, (int)posY, (int)posZ, (Block)OreSpawnMain.MyTomatoPlant3, (int)0, (int)2);
                    for (i = 1; i < corn_height; ++i) {
                        OreSpawnMain.setBlockFast((World)world, (int)posX, (int)(posY + i), (int)posZ, (Block)OreSpawnMain.MyTomatoPlant4, (int)0, (int)2);
                    }
                    OreSpawnMain.setBlockFast((World)world, (int)posX, (int)(posY + corn_height), (int)posZ, (Block)OreSpawnMain.MyTomatoPlant1, (int)0, (int)2);
                    continue block0;
                }
            }
        }
    }

    public void addButterfliesAndMoths(World world, Random random, int chunkX, int chunkZ) {
        if (random.nextInt(10 + OreSpawnMain.LessLag * 2) != 0) {
            return;
        }
        BiomeGenBase b = world.func_72807_a(chunkX, chunkZ);
        if (world.field_73011_w.field_76574_g == OreSpawnMain.DimensionID || world.field_73011_w.field_76574_g == OreSpawnMain.DimensionID6 || b.field_76791_y.equals("Forest") || b.field_76791_y.equals("ForestHills") || b.field_76791_y.equals("River") || b.field_76791_y.equals("Jungle") || b.field_76791_y.equals("JungleHills") || b.field_76791_y.equals("Swampland") || b.field_76791_y.equals("Birch Forest") || b.field_76791_y.equals("Birch Forest Hills") || b.field_76791_y.equals("Roofed Forest")) {
            block0: for (int i = 0; i < 4; ++i) {
                int posX = chunkX + random.nextInt(16);
                int posZ = chunkZ + random.nextInt(16);
                int which = 0;
                for (int posY = 100; posY > 40 && world.func_147437_c(posX, posY, posZ); --posY) {
                    if (world.func_147439_a(posX, posY - 1, posZ) != Blocks.field_150349_c) continue;
                    which = random.nextInt(3);
                    if (which == 0) {
                        OreSpawnMain.setBlockFast((World)world, (int)posX, (int)posY, (int)posZ, (Block)OreSpawnMain.MyButterflyPlant, (int)0, (int)2);
                        continue block0;
                    }
                    if (which == 1) {
                        OreSpawnMain.setBlockFast((World)world, (int)posX, (int)posY, (int)posZ, (Block)OreSpawnMain.MyMothPlant, (int)0, (int)2);
                        continue block0;
                    }
                    OreSpawnMain.setBlockFast((World)world, (int)posX, (int)posY, (int)posZ, (Block)OreSpawnMain.MyFireflyPlant, (int)0, (int)2);
                    continue block0;
                }
            }
        }
    }

    public void addPlayPool(World world, Random random, int chunkX, int chunkZ) {
        if (random.nextInt(350) != 0) {
            return;
        }
        BiomeGenBase b = world.func_72807_a(chunkX, chunkZ);
        if (b.field_76791_y.equals("Ocean")) {
            for (int i = 0; i < 4; ++i) {
                int posX = chunkX + random.nextInt(16);
                int posZ = chunkZ + random.nextInt(16);
                boolean which = false;
                for (int posY = 100; posY > 40; --posY) {
                    if (world.func_147439_a(posX, posY, posZ) != Blocks.field_150350_a || world.func_147439_a(posX, posY - 1, posZ) != Blocks.field_150355_j) continue;
                    OreSpawnMain.MyDungeon.makePlayPool(world, posX, posY, posZ);
                    recently_placed = 50;
                    return;
                }
            }
        }
    }

    public void addFrogPond(World world, Random random, int chunkX, int chunkZ) {
        if (random.nextInt(350) != 0) {
            return;
        }
        BiomeGenBase b = world.func_72807_a(chunkX, chunkZ);
        if (b.field_76791_y.equals("Plains")) {
            for (int i = 0; i < 4; ++i) {
                int posX = chunkX + random.nextInt(16);
                int posZ = chunkZ + random.nextInt(16);
                boolean which = false;
                for (int posY = 100; posY > 40; --posY) {
                    if (world.func_147439_a(posX, posY, posZ) != Blocks.field_150350_a || world.func_147439_a(posX, posY - 1, posZ) != Blocks.field_150349_c) continue;
                    OreSpawnMain.MyDungeon.makeFrogPond(world, posX, posY - 1, posZ);
                    recently_placed = 50;
                    return;
                }
            }
        }
    }

    public void addGoldFishBowl(World world, Random random, int chunkX, int chunkZ) {
        if (random.nextInt(350) != 0) {
            return;
        }
        BiomeGenBase b = world.func_72807_a(chunkX, chunkZ);
        if (b.field_76791_y.equals("Ocean")) {
            for (int i = 0; i < 4; ++i) {
                int posX = chunkX + random.nextInt(16);
                int posZ = chunkZ + random.nextInt(16);
                boolean which = false;
                for (int posY = 100; posY > 40; --posY) {
                    if (world.func_147439_a(posX, posY, posZ) != Blocks.field_150350_a || world.func_147439_a(posX, posY - 1, posZ) != Blocks.field_150355_j) continue;
                    OreSpawnMain.MyDungeon.makeGoldFishBowl(world, posX, posY - 1, posZ);
                    recently_placed = 50;
                    return;
                }
            }
        }
    }

    public boolean addLeafMonster(World world, Random random, int chunkX, int chunkZ) {
        if (random.nextInt(275) != 0) {
            return false;
        }
        BiomeGenBase b = world.func_72807_a(chunkX, chunkZ);
        if (b.field_76791_y.equals("Plains")) {
            for (int i = 0; i < 4; ++i) {
                int posX = chunkX + random.nextInt(16);
                int posZ = chunkZ + random.nextInt(16);
                boolean which = false;
                for (int posY = 100; posY > 40; --posY) {
                    if (world.func_147439_a(posX, posY, posZ) != Blocks.field_150350_a || world.func_147439_a(posX, posY - 1, posZ) != Blocks.field_150349_c) continue;
                    OreSpawnMain.MyDungeon.makeLeafMonsterDungeon(world, posX, posY, posZ);
                    recently_placed = 50;
                    return true;
                }
            }
        }
        return false;
    }

    public boolean addRubberDuckyPond(World world, Random random, int chunkX, int chunkZ) {
        if (random.nextInt(275) != 0) {
            return false;
        }
        BiomeGenBase b = world.func_72807_a(chunkX, chunkZ);
        if (b.field_76791_y.equals("Plains")) {
            for (int i = 0; i < 4; ++i) {
                int posX = chunkX + random.nextInt(16);
                int posZ = chunkZ + random.nextInt(16);
                boolean which = false;
                for (int posY = 100; posY > 40; --posY) {
                    if (world.func_147439_a(posX, posY, posZ) != Blocks.field_150350_a || world.func_147439_a(posX, posY - 1, posZ) != Blocks.field_150349_c) continue;
                    OreSpawnMain.MyDungeon.makeRubberDuckyPond(world, posX, posY, posZ);
                    recently_placed = 50;
                    return true;
                }
            }
        }
        return false;
    }

    public boolean addSpitBug(World world, Random random, int chunkX, int chunkZ) {
        if (random.nextInt(190) != 0) {
            return false;
        }
        BiomeGenBase b = world.func_72807_a(chunkX, chunkZ);
        if (b.field_76791_y.equals("Swampland")) {
            for (int i = 0; i < 4; ++i) {
                int posX = chunkX + random.nextInt(16);
                int posZ = chunkZ + random.nextInt(16);
                boolean which = false;
                for (int posY = 100; posY > 40; --posY) {
                    if (world.func_147439_a(posX, posY, posZ) != Blocks.field_150350_a || world.func_147439_a(posX, posY - 1, posZ) != Blocks.field_150349_c) continue;
                    OreSpawnMain.MyDungeon.makeSpitBugLair(world, posX, posY, posZ);
                    recently_placed = 50;
                    return true;
                }
            }
        }
        return false;
    }

    public boolean addIgloo(World world, Random random, int chunkX, int chunkZ) {
        if (random.nextInt(220) != 0) {
            return false;
        }
        BiomeGenBase b = world.func_72807_a(chunkX, chunkZ);
        if (b.field_76791_y.equals("Ice Plains")) {
            for (int i = 0; i < 4; ++i) {
                int posX = chunkX + random.nextInt(16);
                int posZ = chunkZ + random.nextInt(16);
                boolean which = false;
                for (int posY = 100; posY > 40; --posY) {
                    if (world.func_147439_a(posX, posY, posZ) != Blocks.field_150350_a || world.func_147439_a(posX, posY - 1, posZ) != Blocks.field_150433_aE) continue;
                    OreSpawnMain.MyDungeon.makeIgloo(world, posX, posY - 2, posZ);
                    recently_placed = 50;
                    return true;
                }
            }
        }
        return false;
    }

    public boolean addBouncyCastle(World world, Random random, int chunkX, int chunkZ) {
        if (random.nextInt(230) != 0) {
            return false;
        }
        BiomeGenBase b = world.func_72807_a(chunkX, chunkZ);
        if (b.field_76791_y.equals("Desert")) {
            for (int i = 0; i < 4; ++i) {
                int posX = chunkX + random.nextInt(16);
                int posZ = chunkZ + random.nextInt(16);
                boolean which = false;
                for (int posY = 100; posY > 40; --posY) {
                    if (world.func_147439_a(posX, posY, posZ) != Blocks.field_150350_a || world.func_147439_a(posX, posY - 1, posZ) != Blocks.field_150354_m) continue;
                    OreSpawnMain.MyDungeon.makeBouncyCastle(world, posX, posY - 1, posZ);
                    recently_placed = 50;
                    return true;
                }
            }
        }
        return false;
    }

    public boolean addDamselInDistress(World world, Random random, int chunkX, int chunkZ) {
        if (random.nextInt(250) != 0) {
            return false;
        }
        for (int i = 0; i < 4; ++i) {
            int posX = chunkX + random.nextInt(16);
            int posZ = chunkZ + random.nextInt(16);
            boolean which = false;
            for (int posY = 100; posY > 40; --posY) {
                if (world.func_147439_a(posX, posY, posZ) != Blocks.field_150350_a || world.func_147439_a(posX, posY - 1, posZ) != Blocks.field_150349_c || !this.quickSpaceCheck(world, posX, posY - 1, posZ)) continue;
                OreSpawnMain.MyDungeon.makeDamselInDistress(world, posX, posY - 1, posZ);
                recently_placed = 50;
                return true;
            }
        }
        return false;
    }

    public boolean addSpiderHangout(World world, Random random, int chunkX, int chunkZ) {
        if (random.nextInt(350) != 0) {
            return false;
        }
        if (OreSpawnMain.SpiderDriverEnable == 0) {
            return false;
        }
        for (int i = 0; i < 4; ++i) {
            int posX = chunkX + random.nextInt(16);
            int posZ = chunkZ + random.nextInt(16);
            boolean which = false;
            for (int posY = 100; posY > 40; --posY) {
                if (world.func_147439_a(posX, posY, posZ) != Blocks.field_150350_a || world.func_147439_a(posX, posY - 1, posZ) != Blocks.field_150349_c || !this.quickSpaceCheck(world, posX, posY - 1, posZ)) continue;
                OreSpawnMain.MyDungeon.makeSpiderHangout(world, posX, posY - 1, posZ);
                recently_placed = 50;
                return true;
            }
        }
        return false;
    }

    public boolean addRedAntHangout(World world, Random random, int chunkX, int chunkZ) {
        if (random.nextInt(250) != 0) {
            return false;
        }
        for (int i = 0; i < 4; ++i) {
            int posX = chunkX + random.nextInt(16);
            int posZ = chunkZ + random.nextInt(16);
            boolean which = false;
            for (int posY = 100; posY > 40; --posY) {
                if (world.func_147439_a(posX, posY, posZ) != Blocks.field_150350_a || world.func_147439_a(posX, posY - 1, posZ) != Blocks.field_150349_c || !this.quickSpaceCheck(world, posX, posY - 1, posZ)) continue;
                OreSpawnMain.MyDungeon.makeRedAntHangout(world, posX, posY - 1, posZ);
                recently_placed = 50;
                return true;
            }
        }
        return false;
    }

    public void addWaterDragonLair(World world, Random random, int chunkX, int chunkZ) {
        if (random.nextInt(350) != 0) {
            return;
        }
        BiomeGenBase b = world.func_72807_a(chunkX, chunkZ);
        if (b.field_76791_y.equals("Ocean")) {
            for (int i = 0; i < 4; ++i) {
                int posX = chunkX + random.nextInt(16);
                int posZ = chunkZ + random.nextInt(16);
                boolean which = false;
                for (int posY = 100; posY > 40; --posY) {
                    if (world.func_147439_a(posX, posY, posZ) != Blocks.field_150350_a || world.func_147439_a(posX, posY - 1, posZ) != Blocks.field_150355_j) continue;
                    OreSpawnMain.MyDungeon.makeWaterDragonLair(world, posX, posY - 1, posZ);
                    recently_placed = 50;
                    return;
                }
            }
        }
    }

    public void addGirlfriendIsland(World world, Random random, int chunkX, int chunkZ) {
        if (random.nextInt(300) != 0) {
            return;
        }
        BiomeGenBase b = world.func_72807_a(chunkX, chunkZ);
        if (b.field_76791_y.equals("Ocean")) {
            for (int i = 0; i < 4; ++i) {
                int posX = chunkX + random.nextInt(16);
                int posZ = chunkZ + random.nextInt(16);
                boolean which = false;
                for (int posY = 100; posY > 40; --posY) {
                    if (world.func_147439_a(posX, posY, posZ) != Blocks.field_150350_a || world.func_147439_a(posX, posY - 1, posZ) != Blocks.field_150355_j) continue;
                    OreSpawnMain.MyDungeon.makeGirlfriendIsland(world, posX, posY - 1, posZ);
                    recently_placed = 50;
                    return;
                }
            }
        }
    }

    public void addMonsterIsland(World world, Random random, int chunkX, int chunkZ) {
        if (random.nextInt(300) != 0) {
            return;
        }
        BiomeGenBase b = world.func_72807_a(chunkX, chunkZ);
        if (b.field_76791_y.equals("Ocean")) {
            for (int i = 0; i < 4; ++i) {
                int posX = chunkX + random.nextInt(16);
                int posZ = chunkZ + random.nextInt(16);
                boolean which = false;
                for (int posY = 100; posY > 40; --posY) {
                    if (world.func_147439_a(posX, posY, posZ) != Blocks.field_150350_a || world.func_147439_a(posX, posY - 1, posZ) != Blocks.field_150355_j) continue;
                    OreSpawnMain.MyDungeon.makeMonsterIsland(world, posX, posY - 1, posZ);
                    recently_placed = 50;
                    return;
                }
            }
        }
    }

    public void addMosquitos(World world, Random random, int chunkX, int chunkZ) {
        if (random.nextInt(25 + OreSpawnMain.LessLag * 2) != 0) {
            return;
        }
        if ((world.field_73011_w.field_76574_g == OreSpawnMain.DimensionID || world.field_73011_w.field_76574_g == OreSpawnMain.DimensionID3) && random.nextInt(3) != 0) {
            return;
        }
        BiomeGenBase b = world.func_72807_a(chunkX, chunkZ);
        if (world.field_73011_w.field_76574_g == OreSpawnMain.DimensionID || world.field_73011_w.field_76574_g == OreSpawnMain.DimensionID2 || world.field_73011_w.field_76574_g == OreSpawnMain.DimensionID3 || b.field_76791_y.equals("Jungle") || b.field_76791_y.equals("Swampland")) {
            block0: for (int i = 0; i < 2; ++i) {
                int posX = chunkX + random.nextInt(16);
                int posZ = chunkZ + random.nextInt(16);
                for (int posY = 100; posY > 40 && world.func_147437_c(posX, posY, posZ); --posY) {
                    if (world.func_147439_a(posX, posY - 1, posZ) != Blocks.field_150349_c) continue;
                    OreSpawnMain.setBlockFast((World)world, (int)posX, (int)posY, (int)posZ, (Block)OreSpawnMain.MyMosquitoPlant, (int)0, (int)2);
                    continue block0;
                }
            }
        }
    }

    public void addNetherMosquitos(World world, Random random, int chunkX, int chunkZ) {
        if (random.nextInt(25) != 0) {
            return;
        }
        block0: for (int i = 0; i < 3; ++i) {
            int posX = chunkX + random.nextInt(16);
            int posZ = chunkZ + random.nextInt(16);
            for (int posY = 90; posY > 20; --posY) {
                if (!world.func_147437_c(posX, posY, posZ) || world.func_147439_a(posX, posY - 1, posZ) != Blocks.field_150424_aL) continue;
                OreSpawnMain.setBlockFast((World)world, (int)posX, (int)posY, (int)posZ, (Block)OreSpawnMain.MyMosquitoPlant, (int)0, (int)2);
                continue block0;
            }
        }
    }

    public void addNetherAnts(World world, Random random, int chunkX, int chunkZ) {
        if (OreSpawnMain.RedAntEnable == 0) {
            return;
        }
        if (random.nextInt(25) != 0) {
            return;
        }
        block0: for (int i = 0; i < 3; ++i) {
            int posX = chunkX + random.nextInt(16);
            int posZ = chunkZ + random.nextInt(16);
            for (int posY = 90; posY > 20; --posY) {
                if (!world.func_147437_c(posX, posY, posZ) || world.func_147439_a(posX, posY - 1, posZ) != Blocks.field_150424_aL) continue;
                OreSpawnMain.setBlockFast((World)world, (int)posX, (int)(posY - 1), (int)posZ, (Block)OreSpawnMain.MyRedAntBlock, (int)0, (int)2);
                continue block0;
            }
        }
    }

    public void addAnts(World world, Random random, int chunkX, int chunkZ, int redfreq) {
        if (OreSpawnMain.RedAntEnable == 0 && OreSpawnMain.BlackAntEnable == 0 && OreSpawnMain.RainbowAntEnable == 0 && OreSpawnMain.UnstableAntEnable == 0) {
            return;
        }
        if (redfreq < 2) {
            redfreq = 2;
        }
        if (random.nextInt(30 + OreSpawnMain.LessLag * 4) != 0) {
            return;
        }
        block0: for (int i = 0; i < 4; ++i) {
            int posX = chunkX + random.nextInt(16);
            int posZ = chunkZ + random.nextInt(16);
            for (int posY = 100; posY > 40 && world.func_147437_c(posX, posY, posZ); --posY) {
                if (world.func_147439_a(posX, posY - 1, posZ) != Blocks.field_150349_c) continue;
                if (random.nextInt(redfreq) == 0) {
                    int which = random.nextInt(4);
                    if (which == 0 && OreSpawnMain.RedAntEnable != 0) {
                        OreSpawnMain.setBlockFast((World)world, (int)posX, (int)(posY - 1), (int)posZ, (Block)OreSpawnMain.MyRedAntBlock, (int)0, (int)2);
                    }
                    if (which == 1 && OreSpawnMain.RainbowAntEnable != 0) {
                        OreSpawnMain.setBlockFast((World)world, (int)posX, (int)(posY - 1), (int)posZ, (Block)OreSpawnMain.MyRainbowAntBlock, (int)0, (int)2);
                    }
                    if (which == 2 && OreSpawnMain.UnstableAntEnable != 0) {
                        OreSpawnMain.setBlockFast((World)world, (int)posX, (int)(posY - 1), (int)posZ, (Block)OreSpawnMain.MyUnstableAntBlock, (int)0, (int)2);
                    }
                    if (which != 3 || OreSpawnMain.TermiteEnable == 0) continue block0;
                    OreSpawnMain.setBlockFast((World)world, (int)posX, (int)(posY - 1), (int)posZ, (Block)OreSpawnMain.TermiteBlock, (int)0, (int)2);
                    continue block0;
                }
                if (OreSpawnMain.BlackAntEnable == 0) continue block0;
                OreSpawnMain.setBlockFast((World)world, (int)posX, (int)(posY - 1), (int)posZ, (Block)OreSpawnMain.MyAntBlock, (int)0, (int)2);
                continue block0;
            }
        }
    }

    public void addEndAnts(World world, Random random, int chunkX, int chunkZ) {
    }

    public void addEndKnights(World world, Random random, int chunkX, int chunkZ) {
        if (random.nextInt(25) != 0) {
            return;
        }
        for (int i = 0; i < 3; ++i) {
            int posX = chunkX + random.nextInt(16);
            int posZ = chunkZ + random.nextInt(16);
            for (int posY = 90; posY > 10; --posY) {
                if (!world.func_147437_c(posX, posY, posZ) || world.func_147439_a(posX, posY - 1, posZ) != Blocks.field_150377_bs || !this.quickSpaceCheck(world, posX, posY, posZ)) continue;
                OreSpawnMain.MyDungeon.makeEnderKnightDungeon(world, posX, posY, posZ);
                return;
            }
        }
    }

    public void addEndReapers(World world, Random random, int chunkX, int chunkZ) {
        if (random.nextInt(25) != 0) {
            return;
        }
        for (int i = 0; i < 3; ++i) {
            int posX = chunkX + random.nextInt(16);
            int posZ = chunkZ + random.nextInt(16);
            for (int posY = 90; posY > 10; --posY) {
                if (!world.func_147437_c(posX, posY, posZ) || world.func_147439_a(posX, posY - 1, posZ) != Blocks.field_150377_bs || !this.quickSpaceCheck(world, posX, posY, posZ)) continue;
                OreSpawnMain.MyDungeon.makeEnderReaperGraveyard(world, posX, posY, posZ);
                return;
            }
        }
    }

    public void addHospital(World world, Random random, int chunkX, int chunkZ) {
        if (random.nextInt(25) != 0) {
            return;
        }
        for (int i = 0; i < 3; ++i) {
            int posX = chunkX + random.nextInt(16);
            int posZ = chunkZ + random.nextInt(16);
            for (int posY = 90; posY > 10; --posY) {
                if (!world.func_147437_c(posX, posY, posZ) || world.func_147439_a(posX, posY - 1, posZ) != Blocks.field_150377_bs || !this.quickSpaceCheck(world, posX, posY, posZ)) continue;
                OreSpawnMain.MyDungeon.makeEnderDragonHospital(world, posX, posY, posZ);
                return;
            }
        }
    }

    public void addEnderCastle(World world, Random random, int chunkX, int chunkZ) {
        if (random.nextInt(50) != 0) {
            return;
        }
        for (int i = 0; i < 3; ++i) {
            int posX = chunkX + random.nextInt(16);
            int posZ = chunkZ + random.nextInt(16);
            for (int posY = 90; posY > 10; --posY) {
                if (!world.func_147437_c(posX, posY, posZ) || world.func_147439_a(posX, posY - 1, posZ) != Blocks.field_150377_bs || !this.quickBigSpaceCheck(world, posX, posY, posZ)) continue;
                OreSpawnMain.MyDungeon.makeEnderCastle(world, posX, posY, posZ);
                return;
            }
        }
    }

    public void addUnstableAnts(World world, Random random, int chunkX, int chunkZ) {
        if (OreSpawnMain.UnstableAntEnable == 0) {
            return;
        }
        if (random.nextInt(30) != 0) {
            return;
        }
        block0: for (int i = 0; i < 3; ++i) {
            int posX = chunkX + random.nextInt(16);
            int posZ = chunkZ + random.nextInt(16);
            for (int posY = 20; posY > 2 && world.func_147437_c(posX, posY, posZ); --posY) {
                if (world.func_147439_a(posX, posY - 1, posZ) != Blocks.field_150349_c) continue;
                OreSpawnMain.setBlockFast((World)world, (int)posX, (int)(posY - 1), (int)posZ, (Block)OreSpawnMain.MyUnstableAntBlock, (int)0, (int)2);
                continue block0;
            }
        }
    }

    public void addCrystalTermites(World world, Random random, int chunkX, int chunkZ) {
        if (OreSpawnMain.TermiteEnable == 0) {
            return;
        }
        if (random.nextInt(40) != 0) {
            return;
        }
        block0: for (int i = 0; i < 3; ++i) {
            int posX = chunkX + random.nextInt(16);
            int posZ = chunkZ + random.nextInt(16);
            for (int posY = 100; posY > 50; --posY) {
                if (!world.func_147437_c(posX, posY, posZ) || world.func_147439_a(posX, posY - 1, posZ) != OreSpawnMain.CrystalGrass) continue;
                OreSpawnMain.setBlockFast((World)world, (int)posX, (int)(posY - 1), (int)posZ, (Block)OreSpawnMain.CrystalTermiteBlock, (int)0, (int)2);
                continue block0;
            }
        }
    }

    public boolean addRotatorStation(World world, Random random, int chunkX, int chunkZ) {
        if (OreSpawnMain.RotatorEnable == 0) {
            return false;
        }
        if (random.nextInt(150) != 0) {
            return false;
        }
        for (int i = 0; i < 3; ++i) {
            int posX = chunkX + random.nextInt(16);
            int posZ = chunkZ + random.nextInt(16);
            for (int posY = 100; posY > 50; --posY) {
                if (!world.func_147437_c(posX, posY, posZ) || world.func_147439_a(posX, posY - 1, posZ) != OreSpawnMain.CrystalGrass) continue;
                OreSpawnMain.MyDungeon.makeRotatorStation(world, posX, posY, posZ);
                recently_placed = 50;
                return true;
            }
        }
        return false;
    }

    public boolean addRoundRotator(World world, Random random, int chunkX, int chunkZ) {
        if (OreSpawnMain.RotatorEnable == 0) {
            return false;
        }
        if (random.nextInt(150) != 0) {
            return false;
        }
        for (int i = 0; i < 3; ++i) {
            int posX = chunkX + random.nextInt(16);
            int posZ = chunkZ + random.nextInt(16);
            for (int posY = 100; posY > 50; --posY) {
                if (!world.func_147437_c(posX, posY, posZ) || world.func_147439_a(posX, posY - 1, posZ) != OreSpawnMain.CrystalGrass) continue;
                OreSpawnMain.MyDungeon.makeRoundRotator(world, posX, posY, posZ);
                recently_placed = 50;
                return true;
            }
        }
        return false;
    }

    public boolean addUrchinSpawner(World world, Random random, int chunkX, int chunkZ) {
        if (OreSpawnMain.UrchinEnable == 0) {
            return false;
        }
        if (random.nextInt(180) != 0) {
            return false;
        }
        for (int i = 0; i < 3; ++i) {
            int posX = chunkX + random.nextInt(16);
            int posZ = chunkZ + random.nextInt(16);
            for (int posY = 100; posY > 50; --posY) {
                if (!world.func_147437_c(posX, posY, posZ) || world.func_147439_a(posX, posY - 1, posZ) != OreSpawnMain.CrystalGrass) continue;
                OreSpawnMain.MyDungeon.makeUrchinSpawner(world, posX, posY, posZ);
                recently_placed = 50;
                return true;
            }
        }
        return false;
    }

    public boolean addCrystalHauntedHouse(World world, Random random, int chunkX, int chunkZ) {
        if (random.nextInt(230) != 0) {
            return false;
        }
        for (int i = 0; i < 3; ++i) {
            int posX = chunkX + random.nextInt(16);
            int posZ = chunkZ + random.nextInt(16);
            for (int posY = 100; posY > 50; --posY) {
                if (!world.func_147437_c(posX, posY, posZ) || world.func_147439_a(posX, posY - 1, posZ) != OreSpawnMain.CrystalGrass) continue;
                OreSpawnMain.MyDungeon.makeCrystalHauntedHouse(world, posX, posY, posZ);
                recently_placed = 50;
                return true;
            }
        }
        return false;
    }

    public boolean addCrystalBattleTower(World world, Random random, int chunkX, int chunkZ) {
        if (random.nextInt(280) != 0) {
            return false;
        }
        for (int i = 0; i < 3; ++i) {
            int posX = chunkX + random.nextInt(16);
            int posZ = chunkZ + random.nextInt(16);
            for (int posY = 100; posY > 50; --posY) {
                if (!world.func_147437_c(posX, posY, posZ) || world.func_147439_a(posX, posY - 1, posZ) != OreSpawnMain.CrystalGrass) continue;
                OreSpawnMain.MyDungeon.makeCrystalBattleTower(world, posX, posY, posZ);
                recently_placed = 50;
                return true;
            }
        }
        return false;
    }

    public void addIrukandji(World world, Random random, int chunkX, int chunkZ) {
        if (OreSpawnMain.IrukandjiEnable == 0) {
            return;
        }
        if (random.nextInt(80) != 0) {
            return;
        }
        for (int i = 0; i < 3; ++i) {
            int posX = chunkX + random.nextInt(16);
            int posZ = chunkZ + random.nextInt(16);
            for (int posY = 100; posY > 50; --posY) {
                if (!world.func_147437_c(posX, posY, posZ) || world.func_147439_a(posX, posY - 1, posZ) != Blocks.field_150355_j) continue;
                OreSpawnMain.setBlockFast((World)world, (int)posX, (int)posY, (int)posZ, (Block)Blocks.field_150474_ac, (int)0, (int)2);
                TileEntityMobSpawner tileentitymobspawner = (TileEntityMobSpawner)world.func_147438_o(posX, posY, posZ);
                if (tileentitymobspawner != null) {
                    tileentitymobspawner.func_145881_a().func_98272_a("Irukandji");
                }
                return;
            }
        }
    }

    public void addCrystalChestsAndSpawners(World world, Random random, int chunkX, int chunkZ) {
        Block bid = Blocks.field_150350_a;
        for (int i = 0; i < 3; ++i) {
            int posZ;
            int posY;
            int posX = 1 + chunkX + random.nextInt(14);
            if (!world.func_147437_c(posX, posY = 25, posZ = 1 + chunkZ + random.nextInt(14))) continue;
            bid = world.func_147439_a(posX, posY, posZ);
            if (bid != Blocks.field_150350_a) break;
            bid = world.func_147439_a(posX + 1, posY, posZ);
            if (bid == Blocks.field_150350_a) {
                this.addCrystalChest(world, posX, posY, posZ, 5);
                break;
            }
            bid = world.func_147439_a(posX - 1, posY, posZ);
            if (bid == Blocks.field_150350_a) {
                this.addCrystalChest(world, posX, posY, posZ, 4);
                break;
            }
            bid = world.func_147439_a(posX, posY, posZ + 1);
            if (bid == Blocks.field_150350_a) {
                this.addCrystalChest(world, posX, posY, posZ, 2);
                break;
            }
            bid = world.func_147439_a(posX, posY, posZ - 1);
            if (bid != Blocks.field_150350_a) break;
            this.addCrystalChest(world, posX, posY, posZ, 3);
            break;
        }
    }

    public void addCrystalChest(World world, int x, int y, int z, int dir) {
        int i = world.field_73012_v.nextInt(3);
        if (i == 0) {
            OreSpawnMain.setBlockFast((World)world, (int)x, (int)y, (int)z, (Block)Blocks.field_150486_ae, (int)0, (int)2);
            world.func_72921_c(x, y, z, dir, 3);
            TileEntityChest chest = (TileEntityChest)world.func_147438_o(x, y, z);
            if (chest != null) {
                WeightedRandomChestContent.func_76293_a((Random)world.field_73012_v, (WeightedRandomChestContent[])Trees.CrystalChestContentsList, (IInventory)chest, (int)(1 + world.field_73012_v.nextInt(3)));
            }
        } else {
            OreSpawnMain.setBlockFast((World)world, (int)x, (int)y, (int)z, (Block)Blocks.field_150474_ac, (int)0, (int)2);
            TileEntityMobSpawner tileentitymobspawner = (TileEntityMobSpawner)world.func_147438_o(x, y, z);
            if (tileentitymobspawner != null) {
                int t = world.field_73012_v.nextInt(2);
                if (t == 0) {
                    tileentitymobspawner.func_145881_a().func_98272_a("Dungeon Beast");
                }
                if (t == 1) {
                    tileentitymobspawner.func_145881_a().func_98272_a("Rat");
                }
            }
        }
    }

    public void addIslands(World world, Random random, int chunkX, int chunkZ) {
        int posX = 2 + chunkX + random.nextInt(12);
        int posZ = 2 + chunkZ + random.nextInt(12);
        if (random.nextInt(10 + OreSpawnMain.LessLag * 2) != 1) {
            return;
        }
        for (int posY = 20; posY > 2 && world.func_147437_c(posX, posY, posZ); --posY) {
            if (world.func_147439_a(posX, posY - 1, posZ) != Blocks.field_150349_c) continue;
            OreSpawnMain.setBlockFast((World)world, (int)posX, (int)posY, (int)posZ, (Block)OreSpawnMain.MyIslandBlock, (int)0, (int)2);
            break;
        }
    }

    public boolean addAppleTrees(World world, Random random, int chunkX, int chunkZ, Chunk chunk) {
        int freq = Math.abs(chunkX / 16) + Math.abs(chunkZ / 16);
        int howmany = 2;
        int which = 0;
        boolean added = false;
        howmany += random.nextInt(2 + (15 - (freq %= 15)) / 2);
        which = random.nextInt(10);
        if (random.nextInt(15 + freq) != 0) {
            return false;
        }
        if (OreSpawnMain.LessLag == 1) {
            howmany /= 2;
        }
        if (OreSpawnMain.LessLag == 2 && (howmany /= 4) < 1) {
            return false;
        }
        block0: for (int i = 0; i < howmany; ++i) {
            int posX = 2 + chunkX + random.nextInt(12);
            int posZ = 2 + chunkZ + random.nextInt(12);
            for (int posY = 100; posY > 50 && world.func_147437_c(posX, posY, posZ); --posY) {
                if (world.func_147439_a(posX, posY - 1, posZ) != Blocks.field_150349_c) continue;
                ItemAppleSeed a = (ItemAppleSeed)OreSpawnMain.MyAppleSeed;
                if (which < 8) {
                    a.makeTree(world, posX, posY - 1, posZ, OreSpawnMain.MyAppleLeaves, chunk);
                }
                if (which == 8) {
                    a.makeTree(world, posX, posY - 1, posZ, OreSpawnMain.MyCherryLeaves, chunk);
                }
                if (which == 9) {
                    a.makeTree(world, posX, posY - 1, posZ, OreSpawnMain.MyPeachLeaves, chunk);
                }
                added = true;
                continue block0;
            }
        }
        return added;
    }

    public boolean addHugeTree(World world, Random random, int chunkX, int chunkZ, Chunk chunk) {
        boolean made_one = false;
        if (random.nextInt(50) != 0) {
            return false;
        }
        if (OreSpawnMain.LessLag == 1 && random.nextInt(2) != 0) {
            return false;
        }
        if (OreSpawnMain.LessLag == 2 && random.nextInt(4) != 0) {
            return false;
        }
        block0: for (int i = 0; i < 3 && !made_one; ++i) {
            int posX = 4 + chunkX + random.nextInt(8);
            int posZ = 4 + chunkZ + random.nextInt(8);
            for (int posY = 127; posY > 50 && !made_one; --posY) {
                int rand_treetype;
                if (!world.func_147437_c(posX, posY, posZ) || world.func_147439_a(posX, posY - 1, posZ) != Blocks.field_150349_c) continue;
                ItemMagicApple a = (ItemMagicApple)OreSpawnMain.MagicApple;
                int tree_type = random.nextInt(4);
                int tree_radius = 6 - random.nextInt(2);
                boolean no_critters = false;
                BlockLeaves leaf_type = Blocks.field_150362_t;
                if (random.nextInt(100) > 25) {
                    no_critters = true;
                }
                if ((rand_treetype = random.nextInt(100)) > 75) {
                    if (tree_type != 3 && random.nextInt(20) == 0) {
                        leaf_type = OreSpawnMain.MyAppleLeaves;
                    }
                    a.MakeBigSquareTree(world, posX, posY - 1, posZ, Blocks.field_150364_r, (Block)leaf_type, Blocks.field_150341_Y, tree_type, tree_radius, no_critters, chunk);
                } else if (rand_treetype == 0) {
                    tree_radius = 6;
                    no_critters = true;
                    if (random.nextInt(2) == 0) {
                        a.MakeBigSquareTree(world, posX, posY - 1, posZ, Blocks.field_150340_R, Blocks.field_150475_bE, Blocks.field_150484_ah, -1, tree_radius, no_critters, chunk);
                    } else {
                        a.MakeBigSquareTree(world, posX, posY - 1, posZ, Blocks.field_150343_Z, OreSpawnMain.MyBlockRubyBlock, OreSpawnMain.MyBlockAmethystBlock, -1, tree_radius, no_critters, chunk);
                    }
                } else if (rand_treetype > 15) {
                    tree_radius = 6 - random.nextInt(3);
                    a.MakeBigCircularTree(world, posX, posY - 1, posZ, Blocks.field_150364_r, (Block)leaf_type, Blocks.field_150341_Y, tree_type, tree_radius, no_critters, chunk);
                } else {
                    tree_radius = 6 - random.nextInt(3);
                    a.MakeBigRoundTree(world, posX, posY - 1, posZ, Blocks.field_150364_r, (Block)leaf_type, Blocks.field_150341_Y, tree_type, tree_radius, chunk);
                }
                made_one = true;
                continue block0;
            }
        }
        return made_one;
    }

    public void addVeggies(World world, Random random, int chunkX, int chunkZ) {
        if (random.nextInt(15) != 0) {
            return;
        }
        BiomeGenBase b = world.func_72807_a(chunkX, chunkZ);
        if (world.field_73011_w.field_76574_g == OreSpawnMain.DimensionID || world.field_73011_w.field_76574_g == OreSpawnMain.DimensionID2 || world.field_73011_w.field_76574_g == OreSpawnMain.DimensionID6 || b.field_76791_y.equals("River") || b.field_76791_y.equals("Swampland")) {
            block0: for (int i = 0; i < 8; ++i) {
                int posX = chunkX + random.nextInt(16);
                int posZ = chunkZ + random.nextInt(16);
                for (int posY = 100; posY > 40 && world.func_147437_c(posX, posY, posZ); --posY) {
                    if (world.func_147439_a(posX, posY - 1, posZ) != Blocks.field_150349_c) continue;
                    int what = random.nextInt(6);
                    if (what == 0) {
                        OreSpawnMain.setBlockFast((World)world, (int)posX, (int)posY, (int)posZ, (Block)Blocks.field_150459_bM, (int)0, (int)2);
                        continue block0;
                    }
                    if (what == 1) {
                        OreSpawnMain.setBlockFast((World)world, (int)posX, (int)posY, (int)posZ, (Block)Blocks.field_150469_bN, (int)0, (int)2);
                        continue block0;
                    }
                    if (what == 2) {
                        OreSpawnMain.setBlockFast((World)world, (int)posX, (int)posY, (int)posZ, (Block)OreSpawnMain.MyRadishPlant, (int)0, (int)2);
                        continue block0;
                    }
                    if (what == 3) {
                        OreSpawnMain.setBlockFast((World)world, (int)posX, (int)posY, (int)posZ, (Block)OreSpawnMain.MyLettucePlant1, (int)0, (int)2);
                        continue block0;
                    }
                    if (what == 4) {
                        if (random.nextInt(10) != 0) continue block0;
                        OreSpawnMain.setBlockFast((World)world, (int)posX, (int)posY, (int)posZ, (Block)Blocks.field_150394_bc, (int)0, (int)2);
                        continue block0;
                    }
                    if (random.nextInt(50) != 1 || OreSpawnMain.enableduplicatortree == 0) continue block0;
                    OreSpawnMain.setBlockFast((World)world, (int)posX, (int)posY, (int)posZ, (Block)OreSpawnMain.MyDT, (int)0, (int)2);
                    continue block0;
                }
            }
        }
    }

    public void addRocks(World world, Random random, int chunkX, int chunkZ) {
        if (random.nextInt(5) != 0) {
            return;
        }
        if (OreSpawnMain.RockEnable == 0) {
            return;
        }
        int howmany = 3 + random.nextInt(10);
        block0: for (int i = 0; i < howmany; ++i) {
            int posX = chunkX + random.nextInt(16);
            int posZ = chunkZ + random.nextInt(16);
            for (int posY = 110; posY > 40 && world.func_147439_a(posX, posY, posZ) == Blocks.field_150350_a; --posY) {
                Block bid = world.func_147439_a(posX, posY - 1, posZ);
                if (bid != Blocks.field_150349_c && bid != Blocks.field_150354_m && bid != OreSpawnMain.CrystalGrass) continue;
                this.spawnCreature(world, "Rock", (double)posX, (double)posY, (double)posZ);
                continue block0;
            }
        }
    }

    public void addD4Rocks(World world, Random random, int chunkX, int chunkZ) {
        if (random.nextInt(7) != 0) {
            return;
        }
        if (OreSpawnMain.RockEnable == 0) {
            return;
        }
        int howmany = 3 + random.nextInt(10);
        block0: for (int i = 0; i < howmany; ++i) {
            int posX = chunkX + random.nextInt(16);
            int posZ = chunkZ + random.nextInt(16);
            for (int posY = 20; posY > 5 && world.func_147439_a(posX, posY, posZ) == Blocks.field_150350_a; --posY) {
                if (world.func_147439_a(posX, posY - 1, posZ) != Blocks.field_150349_c) continue;
                this.spawnCreature(world, "Rock", (double)posX, (double)posY, (double)posZ);
                continue block0;
            }
        }
    }

    public boolean addFairyTree(World world, Random random, int chunkX, int chunkZ) {
        int posX = chunkX + 8;
        int posZ = chunkZ + 8;
        if (random.nextInt(5) != 0) {
            return false;
        }
        for (int posY = 128; posY > 40; --posY) {
            Block bid;
            int j;
            int i;
            if (!world.func_147437_c(posX, posY, posZ) || world.func_147439_a(posX, posY - 1, posZ) != OreSpawnMain.CrystalGrass) continue;
            for (i = -8; i <= 8; ++i) {
                for (j = -8; j <= 8; ++j) {
                    bid = world.func_147439_a(posX + i, posY, posZ + j);
                    if (bid == Blocks.field_150350_a) continue;
                    return false;
                }
            }
            for (i = -2; i <= 2; ++i) {
                for (j = -2; j <= 2; ++j) {
                    bid = world.func_147439_a(posX + i, posY - 1, posZ + j);
                    if (bid == OreSpawnMain.CrystalGrass) continue;
                    return false;
                }
            }
            if (random.nextInt(5) != 1) {
                OreSpawnMain.OreSpawnTrees.FairyTree(world, posX, posY - 1, posZ);
            } else {
                OreSpawnMain.OreSpawnTrees.FairyCastleTree(world, posX, posY, posZ);
            }
            recently_placed = 50;
            break;
        }
        return true;
    }

    public boolean addRubyDungeon(World world, Random random, int chunkX, int chunkZ) {
        if (random.nextInt(15) != 0) {
            return false;
        }
        for (int i = 0; i < 8; ++i) {
            int posX = chunkX + random.nextInt(8);
            int posZ = chunkZ + random.nextInt(8);
            for (int posY = 50; posY > 5; --posY) {
                if (world.func_147439_a(posX, posY, posZ) != Blocks.field_150353_l) continue;
                OreSpawnMain.RubyDungeon.makeDungeon(world, posX, posY, posZ);
                return true;
            }
        }
        return false;
    }

    public boolean addGenericDungeon(World world, Random random, int chunkX, int chunkZ) {
        if (random.nextInt(16) != 0) {
            return false;
        }
        if (OreSpawnMain.LessLag == 1 && random.nextInt(2) != 0) {
            return false;
        }
        if (OreSpawnMain.LessLag == 2 && random.nextInt(4) != 0) {
            return false;
        }
        int posX = chunkX + random.nextInt(4);
        int posZ = chunkZ + random.nextInt(4);
        int posY = 5 + random.nextInt(40);
        OreSpawnMain.MyDungeon.makeDungeon(world, posX, posY, posZ);
        return true;
    }

    public boolean addBeeHive(World world, Random random, int chunkX, int chunkZ) {
        int lowestY = 128;
        int lowestX = chunkX;
        int lowestZ = chunkZ;
        boolean found = false;
        for (int i = 0; i < 16; i += 3) {
            block1: for (int j = 0; j < 16; j += 3) {
                int posX = chunkX + i;
                int posZ = chunkZ + j;
                for (int posY = 128; posY > 30; --posY) {
                    if (world.func_147439_a(posX, posY + 1, posZ) != Blocks.field_150350_a || world.func_147439_a(posX, posY, posZ) != Blocks.field_150349_c) continue;
                    if (posY >= lowestY) continue block1;
                    lowestY = posY;
                    lowestX = posX;
                    lowestZ = posZ;
                    found = true;
                    continue block1;
                }
            }
        }
        if (found && lowestY > 40) {
            OreSpawnMain.MyDungeon.makeBeeHive(world, lowestX, lowestY + 3, lowestZ);
            recently_placed = 50;
            return true;
        }
        return false;
    }

    public boolean addAlienWTF(World world, Random random, int chunkX, int chunkZ) {
        int lowestY = 128;
        int lowestX = chunkX;
        int lowestZ = chunkZ;
        boolean found = false;
        for (int i = 0; i < 16; i += 3) {
            block1: for (int j = 0; j < 16; j += 3) {
                int posX = chunkX + i;
                int posZ = chunkZ + j;
                for (int posY = 128; posY > 30; --posY) {
                    if (world.func_147439_a(posX, posY + 1, posZ) != Blocks.field_150350_a || world.func_147439_a(posX, posY, posZ) != Blocks.field_150349_c) continue;
                    if (posY >= lowestY) continue block1;
                    lowestY = posY;
                    lowestX = posX;
                    lowestZ = posZ;
                    found = true;
                    continue block1;
                }
            }
        }
        if (found && lowestY > 40) {
            OreSpawnMain.MyDungeon.makeAlienWTFDungeon(world, lowestX, lowestY, lowestZ);
            recently_placed = 50;
            return true;
        }
        return false;
    }

    public boolean addEnderKnight(World world, Random random, int chunkX, int chunkZ) {
        int lowestY = 128;
        int lowestX = chunkX;
        int lowestZ = chunkZ;
        boolean found = false;
        for (int i = 0; i < 16; i += 3) {
            block1: for (int j = 0; j < 16; j += 3) {
                int posX = chunkX + i;
                int posZ = chunkZ + j;
                for (int posY = 128; posY > 30; --posY) {
                    if (world.func_147439_a(posX, posY + 1, posZ) != Blocks.field_150350_a || world.func_147439_a(posX, posY, posZ) != Blocks.field_150349_c) continue;
                    if (posY >= lowestY) continue block1;
                    lowestY = posY;
                    lowestX = posX;
                    lowestZ = posZ;
                    found = true;
                    continue block1;
                }
            }
        }
        if (found && lowestY > 40) {
            OreSpawnMain.MyDungeon.makeEnderKnightDungeon(world, lowestX, lowestY, lowestZ);
            recently_placed = 50;
            return true;
        }
        return false;
    }

    public boolean addLeonNest(World world, Random random, int chunkX, int chunkZ) {
        int highestY = 30;
        int highestX = chunkX;
        int highestZ = chunkZ;
        boolean found = false;
        for (int i = 0; i < 16; i += 3) {
            block1: for (int j = 0; j < 16; j += 3) {
                int posX = chunkX + i;
                int posZ = chunkZ + j;
                for (int posY = 128; posY > 80; --posY) {
                    if (world.func_147439_a(posX, posY + 1, posZ) != Blocks.field_150350_a || world.func_147439_a(posX, posY, posZ) != Blocks.field_150349_c) continue;
                    if (posY <= highestY) continue block1;
                    highestY = posY + 1;
                    highestX = posX;
                    highestZ = posZ;
                    found = true;
                    continue block1;
                }
            }
        }
        if (found && highestY > 80) {
            OreSpawnMain.MyDungeon.makeLeonNest(world, highestX, highestY, highestZ);
            recently_placed = 50;
            return true;
        }
        return false;
    }

    public boolean addShadowDungeon(World world, Random random, int chunkX, int chunkZ) {
        int lowestY = 128;
        int lowestX = chunkX;
        int lowestZ = chunkZ;
        boolean found = false;
        for (int i = 0; i < 16; i += 3) {
            block1: for (int j = 0; j < 16; j += 3) {
                int posX = chunkX + i;
                int posZ = chunkZ + j;
                for (int posY = 128; posY > 30; --posY) {
                    if (world.func_147439_a(posX, posY + 1, posZ) != Blocks.field_150350_a || world.func_147439_a(posX, posY, posZ) != Blocks.field_150349_c) continue;
                    if (posY >= lowestY) continue block1;
                    lowestY = posY;
                    lowestX = posX;
                    lowestZ = posZ;
                    found = true;
                    continue block1;
                }
            }
        }
        if (found && lowestY > 40) {
            OreSpawnMain.MyDungeon.makeShadowDungeon(world, lowestX, lowestY, lowestZ);
            recently_placed = 50;
            return true;
        }
        return false;
    }

    public boolean addD4RubyDungeon(World world, Random random, int chunkX, int chunkZ) {
        if (OreSpawnMain.LessLag != 0 && random.nextInt(2) != 0) {
            return false;
        }
        int posX = chunkX + random.nextInt(8);
        int posZ = chunkZ + random.nextInt(8);
        for (int posY = 20; posY > 4; --posY) {
            Block bid = world.func_147439_a(posX, posY, posZ);
            if (bid != Blocks.field_150349_c) continue;
            OreSpawnMain.RubyDungeon.makeDungeon(world, posX, posY, posZ);
            recently_placed = 50;
            return true;
        }
        return false;
    }

    public boolean addD4CephadromeAltar(World world, Random random, int chunkX, int chunkZ) {
        if (OreSpawnMain.LessLag != 0 && random.nextInt(2) != 0) {
            return false;
        }
        int posX = chunkX + random.nextInt(8);
        int posZ = chunkZ + random.nextInt(8);
        for (int posY = 20; posY > 4; --posY) {
            Block bid = world.func_147439_a(posX, posY, posZ);
            if (bid != Blocks.field_150349_c) continue;
            OreSpawnMain.MyDungeon.makeCephadromeAltar(world, posX, posY, posZ);
            recently_placed = 50;
            return true;
        }
        return false;
    }

    public boolean addD4Castle(World world, Random random, int chunkX, int chunkZ) {
        if (OreSpawnMain.LessLag != 0 && random.nextInt(2) != 0) {
            return false;
        }
        int posX = chunkX + random.nextInt(8);
        int posZ = chunkZ + random.nextInt(8);
        for (int posY = 20; posY > 4; --posY) {
            Block bid = world.func_147439_a(posX, posY, posZ);
            if (bid != Blocks.field_150349_c) continue;
            for (int x = -20; x < 33; ++x) {
                for (int z = -4; z < 33; ++z) {
                    bid = world.func_147439_a(posX + x, posY + 18, posZ + z);
                    if (bid == Blocks.field_150350_a) continue;
                    return false;
                }
            }
            if (random.nextInt(2) == 1) {
                OreSpawnMain.MyDungeon.makeEnormousCastle(world, posX, posY, posZ);
            } else {
                OreSpawnMain.MyDungeon.makeEnormousCastleQ(world, posX, posY, posZ);
            }
            recently_placed = 50;
            return true;
        }
        return false;
    }

    public boolean addD4Greenhouse(World world, Random random, int chunkX, int chunkZ) {
        if (OreSpawnMain.LessLag != 0 && random.nextInt(2) != 0) {
            return false;
        }
        int posX = chunkX + random.nextInt(8);
        int posZ = chunkZ + random.nextInt(8);
        for (int posY = 20; posY > 4; --posY) {
            Block bid = world.func_147439_a(posX, posY, posZ);
            if (bid != Blocks.field_150349_c) continue;
            for (int x = -2; x < 25; ++x) {
                for (int z = -4; z < 25; ++z) {
                    bid = world.func_147439_a(posX + x, posY + 18, posZ + z);
                    if (bid == Blocks.field_150350_a) continue;
                    return false;
                }
            }
            OreSpawnMain.MyDungeon.makeGreenhouseDungeon(world, posX, posY, posZ);
            recently_placed = 50;
            return true;
        }
        return false;
    }

    public boolean addD4NightmareRookery(World world, Random random, int chunkX, int chunkZ) {
        if (OreSpawnMain.LessLag != 0 && random.nextInt(2) != 0) {
            return false;
        }
        int posX = chunkX + random.nextInt(8);
        int posZ = chunkZ + random.nextInt(8);
        for (int posY = 20; posY > 4; --posY) {
            Block bid = world.func_147439_a(posX, posY, posZ);
            if (bid != Blocks.field_150349_c) continue;
            for (int x = -5; x < 25; ++x) {
                for (int z = -4; z < 5; ++z) {
                    bid = world.func_147439_a(posX + x, posY + 18, posZ + z);
                    if (bid == Blocks.field_150350_a) continue;
                    return false;
                }
            }
            OreSpawnMain.MyDungeon.makeNightmareRookery(world, posX, posY, posZ);
            recently_placed = 50;
            return true;
        }
        return false;
    }

    public boolean addD4StinkyHouse(World world, Random random, int chunkX, int chunkZ) {
        if (OreSpawnMain.LessLag != 0 && random.nextInt(2) != 0) {
            return false;
        }
        int posX = chunkX + random.nextInt(8);
        int posZ = chunkZ + random.nextInt(8);
        for (int posY = 20; posY > 4; --posY) {
            Block bid = world.func_147439_a(posX, posY, posZ);
            if (bid != Blocks.field_150349_c) continue;
            for (int x = -8; x < 20; ++x) {
                for (int z = -8; z < 20; ++z) {
                    bid = world.func_147439_a(posX + x, posY + 18, posZ + z);
                    if (bid == Blocks.field_150350_a) continue;
                    return false;
                }
            }
            OreSpawnMain.MyDungeon.makeStinkyHouse(world, posX, posY, posZ);
            recently_placed = 50;
            return true;
        }
        return false;
    }

    public boolean addD4WhiteHouse(World world, Random random, int chunkX, int chunkZ) {
        if (OreSpawnMain.LessLag != 0 && random.nextInt(2) != 0) {
            return false;
        }
        int posX = chunkX + random.nextInt(8);
        int posZ = chunkZ + random.nextInt(8);
        for (int posY = 20; posY > 4; --posY) {
            Block bid = world.func_147439_a(posX, posY, posZ);
            if (bid != Blocks.field_150349_c) continue;
            for (int x = -20; x < 30; ++x) {
                for (int z = -20; z < 300; ++z) {
                    bid = world.func_147439_a(posX + x, posY + 18, posZ + z);
                    if (bid == Blocks.field_150350_a) continue;
                    return false;
                }
            }
            OreSpawnMain.MyDungeon.makeWhiteHouse(world, posX, posY, posZ);
            recently_placed = 50;
            return true;
        }
        return false;
    }

    public boolean addD4EnderCastle(World world, Random random, int chunkX, int chunkZ) {
        if (OreSpawnMain.LessLag != 0 && random.nextInt(2) != 0) {
            return false;
        }
        int posX = chunkX + random.nextInt(8);
        int posZ = chunkZ + random.nextInt(8);
        for (int posY = 20; posY > 4; --posY) {
            Block bid = world.func_147439_a(posX, posY, posZ);
            if (bid != Blocks.field_150349_c) continue;
            for (int x = -5; x < 25; ++x) {
                for (int z = -5; z < 25; ++z) {
                    bid = world.func_147439_a(posX + x, posY + 18, posZ + z);
                    if (bid == Blocks.field_150350_a) continue;
                    return false;
                }
            }
            OreSpawnMain.MyDungeon.makeEnderCastle(world, posX, posY, posZ);
            recently_placed = 50;
            return true;
        }
        return false;
    }

    public boolean addD4IncaPyramid(World world, Random random, int chunkX, int chunkZ) {
        if (OreSpawnMain.LessLag != 0 && random.nextInt(2) != 0) {
            return false;
        }
        int posX = chunkX + random.nextInt(8);
        int posZ = chunkZ + random.nextInt(8);
        for (int posY = 20; posY > 4; --posY) {
            Block bid = world.func_147439_a(posX, posY, posZ);
            if (bid != Blocks.field_150349_c) continue;
            for (int x = -10; x < 50; ++x) {
                for (int z = -10; z < 40; ++z) {
                    bid = world.func_147439_a(posX + x, posY + 18, posZ + z);
                    if (bid == Blocks.field_150350_a) continue;
                    return false;
                }
            }
            OreSpawnMain.MyDungeon.makeIncaPyramid(world, posX, posY, posZ);
            recently_placed = 50;
            return true;
        }
        return false;
    }

    public boolean addD4RobotLab(World world, Random random, int chunkX, int chunkZ) {
        if (OreSpawnMain.LessLag != 0 && random.nextInt(2) != 0) {
            return false;
        }
        int posX = chunkX + random.nextInt(8);
        int posZ = chunkZ + random.nextInt(8);
        for (int posY = 20; posY > 4; --posY) {
            Block bid = world.func_147439_a(posX, posY, posZ);
            if (bid != Blocks.field_150349_c) continue;
            for (int x = -5; x < 60; ++x) {
                for (int z = -5; z < 70; ++z) {
                    bid = world.func_147439_a(posX + x, posY + 4, posZ + z);
                    if (bid == Blocks.field_150350_a || bid == Blocks.field_150364_r || bid == OreSpawnMain.MyAppleLeaves || bid == OreSpawnMain.MyScaryLeaves || bid == Blocks.field_150350_a) continue;
                    return false;
                }
            }
            OreSpawnMain.MyDungeon.makeRobotLab(world, posX, posY, posZ);
            recently_placed = 50;
            return true;
        }
        return false;
    }

    public boolean addD4Mini(World world, Random random, int chunkX, int chunkZ) {
        if (OreSpawnMain.LessLag != 0 && random.nextInt(2) != 0) {
            return false;
        }
        int posX = chunkX + random.nextInt(8);
        int posZ = chunkZ + random.nextInt(8);
        for (int posY = 20; posY > 4; --posY) {
            Block bid = world.func_147439_a(posX, posY, posZ);
            if (bid != Blocks.field_150349_c) continue;
            OreSpawnMain.MyDungeon.makeMiniDungeon(world, posX, posY, posZ);
            recently_placed = 50;
            return true;
        }
        return false;
    }

    public boolean addPumpkin(World world, Random random, int chunkX, int chunkZ) {
        if (OreSpawnMain.LessLag != 0 && random.nextInt(2) != 0) {
            return false;
        }
        int posX = chunkX + random.nextInt(8);
        int posZ = chunkZ + random.nextInt(8);
        for (int posY = 20; posY > 4; --posY) {
            Block bid = world.func_147439_a(posX, posY, posZ);
            if (bid != Blocks.field_150349_c) continue;
            OreSpawnMain.MyDungeon.makePumpkin(world, posX, posY + 1, posZ);
            recently_placed = 50;
            return true;
        }
        return false;
    }

    public boolean addD4CloudShark(World world, Random random, int chunkX, int chunkZ) {
        int posX = 4 + chunkX + random.nextInt(8);
        int posZ = 4 + chunkZ + random.nextInt(8);
        OreSpawnMain.MyDungeon.makeCloudSharkDungeon(world, posX, 150 + world.field_73012_v.nextInt(10), posZ);
        return true;
    }

    public boolean addD4Rainbow(World world, Random random, int chunkX, int chunkZ) {
        int posX = 4 + chunkX + random.nextInt(8);
        int posZ = 4 + chunkZ + random.nextInt(8);
        OreSpawnMain.MyDungeon.makeRainbow(world, posX, 70 + world.field_73012_v.nextInt(20), posZ);
        recently_placed = 50;
        return true;
    }

    public boolean addD4GenericDungeon(World world, Random random, int chunkX, int chunkZ) {
        if (OreSpawnMain.LessLag != 0 && random.nextInt(4) != 0) {
            return false;
        }
        int posX = chunkX + random.nextInt(8);
        int posZ = chunkZ + random.nextInt(8);
        for (int posY = 20; posY > 4; --posY) {
            Block bid = world.func_147439_a(posX, posY, posZ);
            if (bid != Blocks.field_150349_c) continue;
            OreSpawnMain.MyDungeon.makeDungeon(world, posX, posY, posZ);
            recently_placed = 50;
            return true;
        }
        return false;
    }

    public void addLavaAndWater(World world, Random random, int chunkX, int chunkZ) {
        if (random.nextInt(5) != 0) {
            return;
        }
        block0: for (int i = 0; i < 6; ++i) {
            int posX = chunkX + random.nextInt(16);
            int posZ = chunkZ + random.nextInt(16);
            for (int posY = 128; posY > 75 && world.func_147437_c(posX, posY, posZ); --posY) {
                if (world.func_147439_a(posX, posY - 1, posZ) != Blocks.field_150349_c) continue;
                Block bid = world.func_147439_a(posX, posY - 2, posZ);
                if (bid != Blocks.field_150346_d && bid != Blocks.field_150348_b) continue block0;
                int air = 0;
                int non_air = 0;
                bid = world.func_147439_a(posX + 1, posY - 1, posZ);
                if (bid == Blocks.field_150350_a) {
                    ++air;
                }
                if (bid == Blocks.field_150346_d || bid == Blocks.field_150348_b || bid == Blocks.field_150349_c) {
                    ++non_air;
                }
                if ((bid = world.func_147439_a(posX - 1, posY - 1, posZ)) == Blocks.field_150350_a) {
                    ++air;
                }
                if (bid == Blocks.field_150346_d || bid == Blocks.field_150348_b || bid == Blocks.field_150349_c) {
                    ++non_air;
                }
                if ((bid = world.func_147439_a(posX, posY - 1, posZ + 1)) == Blocks.field_150350_a) {
                    ++air;
                }
                if (bid == Blocks.field_150346_d || bid == Blocks.field_150348_b || bid == Blocks.field_150349_c) {
                    ++non_air;
                }
                if ((bid = world.func_147439_a(posX, posY - 1, posZ - 1)) == Blocks.field_150350_a) {
                    ++air;
                }
                if (bid == Blocks.field_150346_d || bid == Blocks.field_150348_b || bid == Blocks.field_150349_c) {
                    ++non_air;
                }
                if (air == 0 || non_air == 0) continue block0;
                int what = random.nextInt(2);
                if (what == 0) {
                    OreSpawnMain.setBlockFast((World)world, (int)posX, (int)posY, (int)posZ, (Block)Blocks.field_150358_i, (int)0, (int)3);
                    OreSpawnMain.setBlockFast((World)world, (int)posX, (int)(posY - 1), (int)posZ, (Block)Blocks.field_150355_j, (int)0, (int)3);
                    OreSpawnMain.setBlockFast((World)world, (int)posX, (int)(posY - 2), (int)posZ, (Block)Blocks.field_150355_j, (int)0, (int)3);
                } else {
                    OreSpawnMain.setBlockFast((World)world, (int)posX, (int)posY, (int)posZ, (Block)Blocks.field_150356_k, (int)0, (int)3);
                    OreSpawnMain.setBlockFast((World)world, (int)posX, (int)(posY - 1), (int)posZ, (Block)Blocks.field_150353_l, (int)0, (int)3);
                    OreSpawnMain.setBlockFast((World)world, (int)posX, (int)(posY - 2), (int)posZ, (Block)Blocks.field_150353_l, (int)0, (int)3);
                }
                return;
            }
        }
    }

    public boolean addOtherTrees(World world, Random random, int chunkX, int chunkZ) {
        int nc = 5;
        int count = 0;
        if (random.nextInt(30) != 0) {
            return false;
        }
        if (OreSpawnMain.LessLag == 1) {
            if (random.nextInt(2) != 0) {
                return false;
            }
            nc = 4;
        }
        if (OreSpawnMain.LessLag == 2) {
            if (random.nextInt(4) != 0) {
                return false;
            }
            nc = 3;
        }
        if (world.field_73011_w.field_76574_g == OreSpawnMain.DimensionID) {
            int dir = 0;
            int what = random.nextInt(2);
            block0: for (int i = 0; i < nc; ++i) {
                int posX = 3 + chunkX + random.nextInt(10);
                int posZ = 3 + chunkZ + random.nextInt(10);
                for (int posY = 100; posY > 50 && world.func_147437_c(posX, posY, posZ); --posY) {
                    if (world.func_147439_a(posX, posY - 1, posZ) != Blocks.field_150349_c) continue;
                    ++count;
                    if (what == 0) {
                        OreSpawnMain.OreSpawnTrees.WindTree(world, posX, posY - 1, posZ, dir);
                        if (count < 4) continue block0;
                        return true;
                    }
                    OreSpawnMain.OreSpawnTrees.SkyTree(world, posX, posY - 1, posZ);
                    if (count < 3) continue block0;
                    return true;
                }
            }
        }
        return count > 0;
    }

    public boolean addKingAltar(World world, Random random, int chunkX, int chunkZ) {
        if (random.nextInt(2000) != 1) {
            return false;
        }
        for (int i = 0; i < 8; ++i) {
            int posX = 3 + chunkX + random.nextInt(10);
            int posZ = 3 + chunkZ + random.nextInt(10);
            for (int posY = 100; posY > 50; --posY) {
                if (!world.func_147437_c(posX, posY, posZ) || world.func_147439_a(posX, posY - 1, posZ) != Blocks.field_150349_c) continue;
                if (!this.quickReallyBigSpaceCheck(world, posX, posY - 1, posZ)) {
                    return false;
                }
                if (random.nextInt(2) == 0) {
                    OreSpawnMain.MyDungeon.makeKingAltar(world, posX, posY - 1, posZ);
                } else {
                    OreSpawnMain.MyDungeon.makeQueenAltar(world, posX, posY - 1, posZ);
                }
                recently_placed = 100;
                return true;
            }
        }
        return false;
    }

    public void addBasiliskMaze(World world, Random random, int chunkX, int chunkZ) {
        int lowestY = 128;
        int lowestX = chunkX;
        int lowestZ = chunkZ;
        boolean found = false;
        for (int i = 0; i < 16; i += 3) {
            block1: for (int j = 0; j < 16; j += 3) {
                int posX = chunkX + i;
                int posZ = chunkZ + j;
                for (int posY = 128; posY > 30; --posY) {
                    if (world.func_147439_a(posX, posY + 1, posZ) != Blocks.field_150350_a || world.func_147439_a(posX, posY, posZ) == Blocks.field_150350_a) continue;
                    if (posY >= lowestY) continue block1;
                    lowestY = posY;
                    lowestX = posX;
                    lowestZ = posZ;
                    found = true;
                    continue block1;
                }
            }
        }
        if (found && lowestY > 40) {
            OreSpawnMain.BMaze.buildBasiliskMaze(world, lowestX, lowestY - 2, lowestZ);
            recently_placed = 50;
        }
    }

    public void addKyuubiDungeon(World world, Random random, int chunkX, int chunkZ) {
        int lowestY = 128;
        int lowestX = chunkX;
        int lowestZ = chunkZ;
        boolean found = false;
        for (int i = 0; i < 16; i += 3) {
            block1: for (int j = 0; j < 16; j += 3) {
                int posX = chunkX + i;
                int posZ = chunkZ + j;
                for (int posY = 128; posY > 30; --posY) {
                    if (world.func_147439_a(posX, posY + 1, posZ) != Blocks.field_150350_a || world.func_147439_a(posX, posY, posZ) == Blocks.field_150350_a) continue;
                    if (posY >= lowestY) continue block1;
                    lowestY = posY;
                    lowestX = posX;
                    lowestZ = posZ;
                    found = true;
                    continue block1;
                }
            }
        }
        if (found && lowestY > 40) {
            OreSpawnMain.MyDungeon.makeKyuubiDungeon(world, lowestX, lowestY - 2, lowestZ);
            recently_placed = 50;
        }
    }

    private boolean quickSpaceCheck(World world, int posX, int posY, int posZ) {
        for (int i = -2; i < 10; ++i) {
            for (int k = -2; k < 10; ++k) {
                if (world.func_147439_a(posX + i, posY + 4, posZ + k) == Blocks.field_150350_a) continue;
                return false;
            }
        }
        return true;
    }

    private boolean quickBigSpaceCheck(World world, int posX, int posY, int posZ) {
        for (int i = -5; i < 25; ++i) {
            for (int k = -5; k < 25; ++k) {
                if (world.func_147439_a(posX + i, posY + 8, posZ + k) == Blocks.field_150350_a) continue;
                return false;
            }
        }
        return true;
    }

    private boolean quickReallyBigSpaceCheck(World world, int posX, int posY, int posZ) {
        for (int i = -5; i < 55; ++i) {
            for (int k = -5; k < 55; ++k) {
                if (world.func_147439_a(posX + i, posY + 8, posZ + k) == Blocks.field_150350_a) continue;
                return false;
            }
        }
        return true;
    }

    private boolean D4BigSpaceCheck(World world, int posX, int posY, int posZ) {
        for (int i = -25; i < 40; ++i) {
            for (int k = -25; k < 30; ++k) {
                Block bid = world.func_147439_a(posX + i, posY + 4, posZ + k);
                if (bid == Blocks.field_150350_a || bid == Blocks.field_150364_r || bid == OreSpawnMain.MyAppleLeaves || bid == OreSpawnMain.MyScaryLeaves) continue;
                return false;
            }
        }
        return true;
    }

    private Entity spawnCreature(World par0World, String par1, double par2, double par4, double par6) {
        Entity var8 = null;
        var8 = EntityList.func_75620_a((String)par1, (World)par0World);
        if (var8 != null) {
            if (par2 > 0.0) {
                par2 += 0.5;
            }
            if (par2 < 0.0) {
                par2 -= 0.5;
            }
            if (par6 > 0.0) {
                par6 += 0.5;
            }
            if (par6 < 0.0) {
                par6 -= 0.5;
            }
            var8.func_70012_b(par2, par4 + 0.01, par6, par0World.field_73012_v.nextFloat() * 360.0f, 0.0f);
            par0World.func_72838_d(var8);
            ((EntityLiving)var8).func_70642_aH();
        }
        return var8;
    }
}

