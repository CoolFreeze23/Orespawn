package danger.orespawn;

import danger.orespawn.entity.*;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;

@EventBusSubscriber(modid = OreSpawnMod.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class ModEntityAttributes {

    @SubscribeEvent
    public static void registerAttributes(EntityAttributeCreationEvent event) {
        // Monster (hostile)
        event.put(ModEntities.ALIEN.get(), Alien.createAttributes().build());
        event.put(ModEntities.ALIEN_BOSS.get(), AlienBoss.createAttributes().build());
        event.put(ModEntities.ALOSAURUS.get(), Alosaurus.createAttributes().build());
        event.put(ModEntities.ATTACK_SQUID.get(), AttackSquid.createAttributes().build());
        event.put(ModEntities.BAND_P.get(), BandP.createAttributes().build());
        event.put(ModEntities.BASILISK.get(), Basilisk.createAttributes().build());
        event.put(ModEntities.CAVE_FISHER.get(), CaveFisher.createAttributes().build());
        event.put(ModEntities.CLOUD_SHARK.get(), CloudShark.createAttributes().build());
        event.put(ModEntities.CRAB.get(), Crab.createAttributes().build());
        event.put(ModEntities.CREEPING_HORROR.get(), CreepingHorror.createAttributes().build());
        event.put(ModEntities.CRYOLOPHOSAURUS.get(), Cryolophosaurus.createAttributes().build());
        event.put(ModEntities.DUNGEON_BEAST.get(), DungeonBeast.createAttributes().build());
        event.put(ModEntities.ENDER_KNIGHT.get(), EnderKnight.createAttributes().build());
        event.put(ModEntities.ENDER_REAPER.get(), EnderReaper.createAttributes().build());
        event.put(ModEntities.GIANT_ROBOT.get(), GiantRobot.createAttributes().build());
        event.put(ModEntities.JEFFERY.get(), Jeffery.createAttributes().build());
        event.put(ModEntities.HAMMERHEAD.get(), Hammerhead.createAttributes().build());
        event.put(ModEntities.IRUKANDJI.get(), Irukandji.createAttributes().build());
        event.put(ModEntities.NASTYSAURUS.get(), Nastysaurus.createAttributes().build());
        event.put(ModEntities.PITCH_BLACK.get(), PitchBlack.createAttributes().build());
        event.put(ModEntities.POINTYSAURUS.get(), Pointysaurus.createAttributes().build());
        event.put(ModEntities.ROBOT_1.get(), Robot1.createAttributes().build());
        event.put(ModEntities.ROBOT_2.get(), Robot2.createAttributes().build());
        event.put(ModEntities.ROBOT_3.get(), Robot3.createAttributes().build());
        event.put(ModEntities.ROBOT_4.get(), Robot4.createAttributes().build());
        event.put(ModEntities.ROBOT_5.get(), Robot5.createAttributes().build());
        event.put(ModEntities.SEA_MONSTER.get(), SeaMonster.createAttributes().build());
        event.put(ModEntities.SEA_VIPER.get(), SeaViper.createAttributes().build());
        event.put(ModEntities.SKATE.get(), Skate.createAttributes().build());
        event.put(ModEntities.TREX.get(), TRex.createAttributes().build());
        event.put(ModEntities.URCHIN.get(), Urchin.createAttributes().build());
        event.put(ModEntities.GODZILLA.get(), Godzilla.createAttributes().build());
        event.put(ModEntities.KRAKEN.get(), Kraken.createAttributes().build());
        event.put(ModEntities.THE_KING.get(), TheKing.createAttributes().build());
        event.put(ModEntities.THE_QUEEN.get(), TheQueen.createAttributes().build());
        event.put(ModEntities.ENTITY_BEE.get(), EntityBee.createAttributes().build());
        event.put(ModEntities.ENTITY_BRUTALFLY.get(), EntityBrutalfly.createAttributes().build());
        event.put(ModEntities.ENTITY_CATER_KILLER.get(), EntityCaterKiller.createAttributes().build());
        event.put(ModEntities.ENTITY_EMPEROR_SCORPION.get(), EntityEmperorScorpion.createAttributes().build());
        event.put(ModEntities.ENTITY_HERCULES_BEETLE.get(), EntityHerculesBeetle.createAttributes().build());
        event.put(ModEntities.ENTITY_KYUUBI.get(), EntityKyuubi.createAttributes().build());
        event.put(ModEntities.ENTITY_LEAF_MONSTER.get(), EntityLeafMonster.createAttributes().build());
        event.put(ModEntities.ENTITY_LURKING_TERROR.get(), EntityLurkingTerror.createAttributes().build());
        event.put(ModEntities.ENTITY_MANTIS.get(), EntityMantis.createAttributes().build());
        event.put(ModEntities.ENTITY_MOLENOID.get(), EntityMolenoid.createAttributes().build());
        event.put(ModEntities.ENTITY_RAT.get(), EntityRat.createAttributes().build());
        event.put(ModEntities.ENTITY_ROTATOR.get(), EntityRotator.createAttributes().build());
        event.put(ModEntities.ENTITY_SCORPION.get(), EntityScorpion.createAttributes().build());
        event.put(ModEntities.ENTITY_SPIT_BUG.get(), EntitySpitBug.createAttributes().build());
        event.put(ModEntities.ENTITY_TERRIBLE_TERROR.get(), EntityTerribleTerror.createAttributes().build());
        event.put(ModEntities.ENTITY_TRIFFID.get(), EntityTriffid.createAttributes().build());
        event.put(ModEntities.ENTITY_TROOPER_BUG.get(), EntityTrooperBug.createAttributes().build());
        event.put(ModEntities.ENTITY_VORTEX.get(), EntityVortex.createAttributes().build());
        event.put(ModEntities.ENTITY_WORM_SMALL.get(), EntityWormSmall.createAttributes().build());
        event.put(ModEntities.ENTITY_WORM_MEDIUM.get(), EntityWormMedium.createAttributes().build());
        event.put(ModEntities.ENTITY_WORM_LARGE.get(), EntityWormLarge.createAttributes().build());

        // Creature (passive)
        event.put(ModEntities.BARYONYX.get(), Baryonyx.createAttributes().build());
        event.put(ModEntities.BEAVER.get(), Beaver.createAttributes().build());
        event.put(ModEntities.CASSOWARY.get(), Cassowary.createAttributes().build());
        event.put(ModEntities.CHIPMUNK.get(), Chipmunk.createAttributes().build());
        event.put(ModEntities.COCKATEIL.get(), Cockateil.createAttributes().build());
        event.put(ModEntities.COIN.get(), Coin.createAttributes().build());
        event.put(ModEntities.EASTER_BUNNY.get(), EasterBunny.createAttributes().build());
        event.put(ModEntities.FLOUNDER.get(), Flounder.createAttributes().build());
        event.put(ModEntities.FROG.get(), Frog.createAttributes().build());
        event.put(ModEntities.GAZELLE.get(), Gazelle.createAttributes().build());
        event.put(ModEntities.GOLD_FISH.get(), GoldFish.createAttributes().build());
        event.put(ModEntities.ISLAND.get(), Island.createAttributes().build());
        event.put(ModEntities.ISLAND_TOO.get(), IslandToo.createAttributes().build());
        event.put(ModEntities.PEACOCK.get(), Peacock.createAttributes().build());
        event.put(ModEntities.WHALE.get(), Whale.createAttributes().build());
        event.put(ModEntities.ENTITY_ANT.get(), EntityAnt.createAttributes().build());
        event.put(ModEntities.ENTITY_CLIFF_RACER.get(), EntityCliffRacer.createAttributes().build());
        event.put(ModEntities.ENTITY_CRICKET.get(), EntityCricket.createAttributes().build());
        event.put(ModEntities.ENTITY_DRAGONFLY.get(), EntityDragonfly.createAttributes().build());
        event.put(ModEntities.ENTITY_RED_ANT.get(), EntityRedAnt.createAttributes().build());
        event.put(ModEntities.ENTITY_RAINBOW_ANT.get(), EntityRainbowAnt.createAttributes().build());
        event.put(ModEntities.ENTITY_STINK_BUG.get(), EntityStinkBug.createAttributes().build());
        event.put(ModEntities.ENTITY_TERMITE.get(), EntityTermite.createAttributes().build());
        event.put(ModEntities.ENTITY_TSHIRT.get(), EntityTshirt.createAttributes().build());
        event.put(ModEntities.ENTITY_UNSTABLE_ANT.get(), EntityUnstableAnt.createAttributes().build());

        // Creature (tameable)
        event.put(ModEntities.BOYFRIEND.get(), Boyfriend.createAttributes().build());
        event.put(ModEntities.CAMARASAURUS.get(), Camarasaurus.createAttributes().build());
        event.put(ModEntities.DRAGON.get(), Dragon.createAttributes().build());
        event.put(ModEntities.BABY_DRAGON.get(), BabyDragon.createAttributes().build());
        event.put(ModEntities.ENTITY_CANNON_FODDER.get(), EntityCannonFodder.createAttributes().build());
        event.put(ModEntities.ENTITY_GAMMA_METROID.get(), EntityGammaMetroid.createAttributes().build());
        event.put(ModEntities.GIRLFRIEND.get(), Girlfriend.createAttributes().build());
        event.put(ModEntities.ENTITY_HYDROLISC.get(), EntityHydrolisc.createAttributes().build());
        event.put(ModEntities.ENTITY_LEON.get(), EntityLeon.createAttributes().build());
        event.put(ModEntities.LEONOPTERYX.get(), Leonopteryx.createAttributes().build());
        event.put(ModEntities.LIZARD.get(), Lizard.createAttributes().build());
        event.put(ModEntities.OSTRICH.get(), Ostrich.createAttributes().build());
        event.put(ModEntities.ENTITY_RUBBER_DUCKY.get(), EntityRubberDucky.createAttributes().build());
        event.put(ModEntities.ENTITY_SPYRO.get(), EntitySpyro.createAttributes().build());
        event.put(ModEntities.ENTITY_STINKY.get(), EntityStinky.createAttributes().build());
        event.put(ModEntities.THE_PRINCE.get(), ThePrince.createAttributes().build());
        event.put(ModEntities.THE_PRINCE_ADULT.get(), ThePrinceAdult.createAttributes().build());
        event.put(ModEntities.THE_PRINCESS.get(), ThePrincess.createAttributes().build());
        event.put(ModEntities.THE_PRINCE_TEEN.get(), ThePrinceTeen.createAttributes().build());
        event.put(ModEntities.VELOCITY_RAPTOR.get(), VelocityRaptor.createAttributes().build());
        event.put(ModEntities.WATER_DRAGON.get(), WaterDragon.createAttributes().build());

        // Ambient
        event.put(ModEntities.ENTITY_BUTTERFLY.get(), EntityButterfly.createAttributes().build());
        event.put(ModEntities.ENTITY_LUNA_MOTH.get(), EntityLunaMoth.createAttributes().build());
        event.put(ModEntities.ENTITY_MOSQUITO.get(), EntityMosquito.createAttributes().build());
        event.put(ModEntities.FAIRY.get(), Fairy.createAttributes().build());
        event.put(ModEntities.FIREFLY.get(), Firefly.createAttributes().build());
        event.put(ModEntities.GHOST.get(), Ghost.createAttributes().build());
        event.put(ModEntities.GHOST_SKELLY.get(), GhostSkelly.createAttributes().build());
        event.put(ModEntities.MOTHRA.get(), Mothra.createAttributes().build());

        // Misc (mob)
        event.put(ModEntities.ANT_ROBOT.get(), AntRobot.createAttributes().build());
        event.put(ModEntities.ELEVATOR.get(), Elevator.createAttributes().build());
        event.put(ModEntities.HOVERBOARD.get(), HoverboardEntity.createAttributes().build());
        event.put(ModEntities.KING_HEAD.get(), KingHead.createAttributes().build());
        event.put(ModEntities.QUEEN_HEAD.get(), QueenHead.createAttributes().build());
        event.put(ModEntities.GODZILLA_HEAD.get(), GodzillaHead.createAttributes().build());
        event.put(ModEntities.PURPLE_POWER.get(), PurplePower.createAttributes().build());
        event.put(ModEntities.ROCK_BASE.get(), RockBase.createAttributes().build());
        event.put(ModEntities.SPIDER_ROBOT.get(), SpiderRobot.createAttributes().build());
        event.put(ModEntities.CEPHADROME.get(), Cephadrome.createAttributes().build());
        event.put(ModEntities.RED_COW.get(), RedCow.createAttributes().build());
        event.put(ModEntities.SPIDER_DRIVER.get(), SpiderDriver.createAttributes().build());
        event.put(ModEntities.CRYSTAL_COW.get(), CrystalCow.createAttributes().build());
        event.put(ModEntities.GOLD_COW.get(), GoldCow.createAttributes().build());
        event.put(ModEntities.ENCHANTED_COW.get(), EnchantedCow.createAttributes().build());
        event.put(ModEntities.APPLE_COW.get(), AppleCow.createAttributes().build());
        event.put(ModEntities.GOLDEN_APPLE_COW.get(), GoldenAppleCow.createAttributes().build());
        event.put(ModEntities.RUBY_BIRD.get(), RubyBird.createAttributes().build());
        event.put(ModEntities.VAMPIRE_BUTTERFLY.get(), VampireButterfly.createAttributes().build());
    }

    @SubscribeEvent
    public static void registerSpawnPlacements(RegisterSpawnPlacementsEvent event) {
        // Water mobs
        event.register(ModEntities.KRAKEN.get(), SpawnPlacementTypes.IN_WATER,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Monster::checkMonsterSpawnRules, RegisterSpawnPlacementsEvent.Operation.AND);
        event.register(ModEntities.SEA_VIPER.get(), SpawnPlacementTypes.IN_WATER,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Monster::checkMonsterSpawnRules, RegisterSpawnPlacementsEvent.Operation.AND);
        event.register(ModEntities.SEA_MONSTER.get(), SpawnPlacementTypes.IN_WATER,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Monster::checkMonsterSpawnRules, RegisterSpawnPlacementsEvent.Operation.AND);
        event.register(ModEntities.IRUKANDJI.get(), SpawnPlacementTypes.IN_WATER,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Monster::checkMonsterSpawnRules, RegisterSpawnPlacementsEvent.Operation.AND);
        event.register(ModEntities.WHALE.get(), SpawnPlacementTypes.IN_WATER,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Mob::checkMobSpawnRules, RegisterSpawnPlacementsEvent.Operation.AND);
        event.register(ModEntities.FLOUNDER.get(), SpawnPlacementTypes.IN_WATER,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Mob::checkMobSpawnRules, RegisterSpawnPlacementsEvent.Operation.AND);
        event.register(ModEntities.SKATE.get(), SpawnPlacementTypes.IN_WATER,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Mob::checkMobSpawnRules, RegisterSpawnPlacementsEvent.Operation.AND);

        // Cave mobs
        event.register(ModEntities.CAVE_FISHER.get(), SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Monster::checkMonsterSpawnRules, RegisterSpawnPlacementsEvent.Operation.AND);
        event.register(ModEntities.DUNGEON_BEAST.get(), SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Monster::checkMonsterSpawnRules, RegisterSpawnPlacementsEvent.Operation.AND);
        event.register(ModEntities.ENTITY_SCORPION.get(), SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Monster::checkMonsterSpawnRules, RegisterSpawnPlacementsEvent.Operation.AND);
        event.register(ModEntities.ENTITY_EMPEROR_SCORPION.get(), SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Monster::checkMonsterSpawnRules, RegisterSpawnPlacementsEvent.Operation.AND);

        // Overworld surface hostile mobs
        event.register(ModEntities.ALOSAURUS.get(), SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Monster::checkMonsterSpawnRules, RegisterSpawnPlacementsEvent.Operation.AND);
        event.register(ModEntities.NASTYSAURUS.get(), SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Monster::checkMonsterSpawnRules, RegisterSpawnPlacementsEvent.Operation.AND);
        event.register(ModEntities.POINTYSAURUS.get(), SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Monster::checkMonsterSpawnRules, RegisterSpawnPlacementsEvent.Operation.AND);
        event.register(ModEntities.TREX.get(), SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Monster::checkMonsterSpawnRules, RegisterSpawnPlacementsEvent.Operation.AND);
        event.register(ModEntities.CRYOLOPHOSAURUS.get(), SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Monster::checkMonsterSpawnRules, RegisterSpawnPlacementsEvent.Operation.AND);
        event.register(ModEntities.BASILISK.get(), SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Monster::checkMonsterSpawnRules, RegisterSpawnPlacementsEvent.Operation.AND);
        event.register(ModEntities.HAMMERHEAD.get(), SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Monster::checkMonsterSpawnRules, RegisterSpawnPlacementsEvent.Operation.AND);
        event.register(ModEntities.ENTITY_CATER_KILLER.get(), SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Monster::checkMonsterSpawnRules, RegisterSpawnPlacementsEvent.Operation.AND);
        event.register(ModEntities.ENTITY_TROOPER_BUG.get(), SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Monster::checkMonsterSpawnRules, RegisterSpawnPlacementsEvent.Operation.AND);
        event.register(ModEntities.ENTITY_MANTIS.get(), SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Monster::checkMonsterSpawnRules, RegisterSpawnPlacementsEvent.Operation.AND);
        event.register(ModEntities.ENTITY_WORM_SMALL.get(), SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Monster::checkMonsterSpawnRules, RegisterSpawnPlacementsEvent.Operation.AND);
        event.register(ModEntities.ENTITY_WORM_MEDIUM.get(), SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Monster::checkMonsterSpawnRules, RegisterSpawnPlacementsEvent.Operation.AND);
        event.register(ModEntities.ENTITY_WORM_LARGE.get(), SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Monster::checkMonsterSpawnRules, RegisterSpawnPlacementsEvent.Operation.AND);
        event.register(ModEntities.ENTITY_LEAF_MONSTER.get(), SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Monster::checkMonsterSpawnRules, RegisterSpawnPlacementsEvent.Operation.AND);
        event.register(ModEntities.ENTITY_TRIFFID.get(), SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Monster::checkMonsterSpawnRules, RegisterSpawnPlacementsEvent.Operation.AND);
        event.register(ModEntities.ENDER_KNIGHT.get(), SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Monster::checkMonsterSpawnRules, RegisterSpawnPlacementsEvent.Operation.AND);
        event.register(ModEntities.ENDER_REAPER.get(), SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Monster::checkMonsterSpawnRules, RegisterSpawnPlacementsEvent.Operation.AND);

        // Overworld surface passive/neutral mobs
        event.register(ModEntities.BARYONYX.get(), SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Animal::checkAnimalSpawnRules, RegisterSpawnPlacementsEvent.Operation.AND);
        event.register(ModEntities.CAMARASAURUS.get(), SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Animal::checkAnimalSpawnRules, RegisterSpawnPlacementsEvent.Operation.AND);
        event.register(ModEntities.VELOCITY_RAPTOR.get(), SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Animal::checkAnimalSpawnRules, RegisterSpawnPlacementsEvent.Operation.AND);
        event.register(ModEntities.CHIPMUNK.get(), SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Animal::checkAnimalSpawnRules, RegisterSpawnPlacementsEvent.Operation.AND);
        event.register(ModEntities.GAZELLE.get(), SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Animal::checkAnimalSpawnRules, RegisterSpawnPlacementsEvent.Operation.AND);
        event.register(ModEntities.OSTRICH.get(), SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Animal::checkAnimalSpawnRules, RegisterSpawnPlacementsEvent.Operation.AND);
        event.register(ModEntities.CASSOWARY.get(), SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Animal::checkAnimalSpawnRules, RegisterSpawnPlacementsEvent.Operation.AND);
        event.register(ModEntities.BEAVER.get(), SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Animal::checkAnimalSpawnRules, RegisterSpawnPlacementsEvent.Operation.AND);
        event.register(ModEntities.EASTER_BUNNY.get(), SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Animal::checkAnimalSpawnRules, RegisterSpawnPlacementsEvent.Operation.AND);
        event.register(ModEntities.LIZARD.get(), SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Animal::checkAnimalSpawnRules, RegisterSpawnPlacementsEvent.Operation.AND);

        // Crystal dimension passive mobs - use expanded check that also allows CrystalGrass
        event.register(ModEntities.PEACOCK.get(), SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, ModEntityAttributes::checkAnimalOrCrystalSpawnRules, RegisterSpawnPlacementsEvent.Operation.AND);
        event.register(ModEntities.COCKATEIL.get(), SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, ModEntityAttributes::checkAnimalOrCrystalSpawnRules, RegisterSpawnPlacementsEvent.Operation.AND);
        event.register(ModEntities.FROG.get(), SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, ModEntityAttributes::checkAnimalOrCrystalSpawnRules, RegisterSpawnPlacementsEvent.Operation.AND);
        event.register(ModEntities.CRYSTAL_COW.get(), SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, ModEntityAttributes::checkAnimalOrCrystalSpawnRules, RegisterSpawnPlacementsEvent.Operation.AND);

        // Ambient/flying mobs
        event.register(ModEntities.ENTITY_BUTTERFLY.get(), SpawnPlacementTypes.NO_RESTRICTIONS,
                Heightmap.Types.MOTION_BLOCKING, Mob::checkMobSpawnRules, RegisterSpawnPlacementsEvent.Operation.AND);
        event.register(ModEntities.ENTITY_DRAGONFLY.get(), SpawnPlacementTypes.NO_RESTRICTIONS,
                Heightmap.Types.MOTION_BLOCKING, Animal::checkAnimalSpawnRules, RegisterSpawnPlacementsEvent.Operation.AND);
        event.register(ModEntities.FIREFLY.get(), SpawnPlacementTypes.NO_RESTRICTIONS,
                Heightmap.Types.MOTION_BLOCKING, Mob::checkMobSpawnRules, RegisterSpawnPlacementsEvent.Operation.AND);
        event.register(ModEntities.ENTITY_LUNA_MOTH.get(), SpawnPlacementTypes.NO_RESTRICTIONS,
                Heightmap.Types.MOTION_BLOCKING, Mob::checkMobSpawnRules, RegisterSpawnPlacementsEvent.Operation.AND);
        event.register(ModEntities.ENTITY_MOSQUITO.get(), SpawnPlacementTypes.NO_RESTRICTIONS,
                Heightmap.Types.MOTION_BLOCKING, Mob::checkMobSpawnRules, RegisterSpawnPlacementsEvent.Operation.AND);
        event.register(ModEntities.GHOST.get(), SpawnPlacementTypes.NO_RESTRICTIONS,
                Heightmap.Types.MOTION_BLOCKING, Mob::checkMobSpawnRules, RegisterSpawnPlacementsEvent.Operation.AND);
        event.register(ModEntities.GHOST_SKELLY.get(), SpawnPlacementTypes.NO_RESTRICTIONS,
                Heightmap.Types.MOTION_BLOCKING, Mob::checkMobSpawnRules, RegisterSpawnPlacementsEvent.Operation.AND);

        // Flying mobs
        event.register(ModEntities.CLOUD_SHARK.get(), SpawnPlacementTypes.NO_RESTRICTIONS,
                Heightmap.Types.MOTION_BLOCKING, Monster::checkMonsterSpawnRules, RegisterSpawnPlacementsEvent.Operation.AND);
        event.register(ModEntities.MOTHRA.get(), SpawnPlacementTypes.NO_RESTRICTIONS,
                Heightmap.Types.MOTION_BLOCKING, Mob::checkMobSpawnRules, RegisterSpawnPlacementsEvent.Operation.AND);
        event.register(ModEntities.LEONOPTERYX.get(), SpawnPlacementTypes.NO_RESTRICTIONS,
                Heightmap.Types.MOTION_BLOCKING, Animal::checkAnimalSpawnRules, RegisterSpawnPlacementsEvent.Operation.AND);

        // Cave mobs
        event.register(ModEntities.ENTITY_MOLENOID.get(), SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Monster::checkMonsterSpawnRules, RegisterSpawnPlacementsEvent.Operation.AND);
        event.register(ModEntities.ENTITY_STINK_BUG.get(), SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Animal::checkAnimalSpawnRules, RegisterSpawnPlacementsEvent.Operation.AND);
        event.register(ModEntities.ALIEN.get(), SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Monster::checkMonsterSpawnRules, RegisterSpawnPlacementsEvent.Operation.AND);
        event.register(ModEntities.CREEPING_HORROR.get(), SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Monster::checkMonsterSpawnRules, RegisterSpawnPlacementsEvent.Operation.AND);
        event.register(ModEntities.ENTITY_LURKING_TERROR.get(), SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Monster::checkMonsterSpawnRules, RegisterSpawnPlacementsEvent.Operation.AND);

        // Phase 14 — overworld surface cow variants. Animal spawn rules
        // (grass + sky + brightness > 8) match the existing RedCow gate
        // so they only appear in the same green biomes as the others.
        event.register(ModEntities.APPLE_COW.get(), SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Animal::checkAnimalSpawnRules, RegisterSpawnPlacementsEvent.Operation.AND);
        event.register(ModEntities.GOLDEN_APPLE_COW.get(), SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Animal::checkAnimalSpawnRules, RegisterSpawnPlacementsEvent.Operation.AND);

        // Phase 14 — Vampire Butterfly is a flying hostile, gets the
        // NO_RESTRICTIONS placement (matches CloudShark / Mothra) and
        // checkAnyLightMonsterSpawnRules so it can spawn in the
        // permanently dim Chaos dimension regardless of light level.
        event.register(ModEntities.VAMPIRE_BUTTERFLY.get(), SpawnPlacementTypes.NO_RESTRICTIONS,
                Heightmap.Types.MOTION_BLOCKING, VampireButterfly::checkSpawnRules, RegisterSpawnPlacementsEvent.Operation.AND);
    }

    /**
     * Like Animal::checkAnimalSpawnRules but also allows spawning on CrystalGrass.
     * Used for passive mobs that appear in both the Overworld and Crystal dimension.
     */
    private static boolean checkAnimalOrCrystalSpawnRules(
            EntityType<? extends Animal> type, LevelAccessor level,
            MobSpawnType spawnType, BlockPos pos, RandomSource random) {
        BlockState below = level.getBlockState(pos.below());
        boolean validGround = below.is(Blocks.GRASS_BLOCK)
                || below.is(ModBlocks.CRYSTAL_GRASS.get());
        return validGround && level.getRawBrightness(pos, 0) > 8;
    }
}
