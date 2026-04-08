package danger.orespawn;

import danger.orespawn.client.OreSpawnItemRenderer;
import danger.orespawn.entity.client.*;
import danger.orespawn.gui.CrystalFurnaceScreen;
import danger.orespawn.gui.CrystalWorkbenchScreen;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;

@Mod(value = OreSpawnMod.MOD_ID, dist = Dist.CLIENT)
public class OreSpawnClient {

    public OreSpawnClient() {
    }

    @EventBusSubscriber(modid = OreSpawnMod.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientEvents {

        @SubscribeEvent
        public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
            // Monster (hostile)
            event.registerEntityRenderer(ModEntities.ALIEN.get(), AlienRenderer::new);
            event.registerEntityRenderer(ModEntities.ALOSAURUS.get(), AlosaurusRenderer::new);
            event.registerEntityRenderer(ModEntities.ATTACK_SQUID.get(), AttackSquidRenderer::new);
            event.registerEntityRenderer(ModEntities.BAND_P.get(), BandPRenderer::new);
            event.registerEntityRenderer(ModEntities.CAVE_FISHER.get(), CaveFisherRenderer::new);
            event.registerEntityRenderer(ModEntities.CLOUD_SHARK.get(), CloudSharkRenderer::new);
            event.registerEntityRenderer(ModEntities.CRAB.get(), CrabRenderer::new);
            event.registerEntityRenderer(ModEntities.CREEPING_HORROR.get(), CreepingHorrorRenderer::new);
            event.registerEntityRenderer(ModEntities.CRYOLOPHOSAURUS.get(), CryolophosaurusRenderer::new);
            event.registerEntityRenderer(ModEntities.DUNGEON_BEAST.get(), DungeonBeastRenderer::new);
            event.registerEntityRenderer(ModEntities.ENDER_KNIGHT.get(), EnderKnightRenderer::new);
            event.registerEntityRenderer(ModEntities.ENDER_REAPER.get(), EnderReaperRenderer::new);
            event.registerEntityRenderer(ModEntities.GIANT_ROBOT.get(), GiantRobotRenderer::new);
            event.registerEntityRenderer(ModEntities.JEFFERY.get(), GiantRobotRenderer::new);
            event.registerEntityRenderer(ModEntities.HAMMERHEAD.get(), HammerheadRenderer::new);
            event.registerEntityRenderer(ModEntities.IRUKANDJI.get(), IrukandjiRenderer::new);
            event.registerEntityRenderer(ModEntities.NASTYSAURUS.get(), NastysaurusRenderer::new);
            event.registerEntityRenderer(ModEntities.PITCH_BLACK.get(), PitchBlackRenderer::new);
            event.registerEntityRenderer(ModEntities.POINTYSAURUS.get(), PointysaurusRenderer::new);
            event.registerEntityRenderer(ModEntities.ROBOT_1.get(), Robot1Renderer::new);
            event.registerEntityRenderer(ModEntities.ROBOT_2.get(), Robot2Renderer::new);
            event.registerEntityRenderer(ModEntities.ROBOT_3.get(), Robot3Renderer::new);
            event.registerEntityRenderer(ModEntities.ROBOT_4.get(), Robot4Renderer::new);
            event.registerEntityRenderer(ModEntities.ROBOT_5.get(), Robot5Renderer::new);
            event.registerEntityRenderer(ModEntities.SEA_MONSTER.get(), SeaMonsterRenderer::new);
            event.registerEntityRenderer(ModEntities.SEA_VIPER.get(), SeaViperRenderer::new);
            event.registerEntityRenderer(ModEntities.SKATE.get(), SkateRenderer::new);
            event.registerEntityRenderer(ModEntities.TREX.get(), TRexRenderer::new);
            event.registerEntityRenderer(ModEntities.URCHIN.get(), UrchinRenderer::new);
            event.registerEntityRenderer(ModEntities.BASILISK.get(), BasiliskRenderer::new);
            event.registerEntityRenderer(ModEntities.GODZILLA.get(), GodzillaRenderer::new);
            event.registerEntityRenderer(ModEntities.KRAKEN.get(), KrakenRenderer::new);
            event.registerEntityRenderer(ModEntities.THE_KING.get(), TheKingRenderer::new);
            event.registerEntityRenderer(ModEntities.THE_QUEEN.get(), TheQueenRenderer::new);
            event.registerEntityRenderer(ModEntities.ENTITY_BEE.get(), BeeRenderer::new);
            event.registerEntityRenderer(ModEntities.ENTITY_BRUTALFLY.get(), BrutalflyRenderer::new);
            event.registerEntityRenderer(ModEntities.ENTITY_CATER_KILLER.get(), CaterKillerRenderer::new);
            event.registerEntityRenderer(ModEntities.ENTITY_EMPEROR_SCORPION.get(), EmperorScorpionRenderer::new);
            event.registerEntityRenderer(ModEntities.ENTITY_HERCULES_BEETLE.get(), HerculesBeetleRenderer::new);
            event.registerEntityRenderer(ModEntities.ENTITY_KYUUBI.get(), KyuubiRenderer::new);
            event.registerEntityRenderer(ModEntities.ENTITY_LEAF_MONSTER.get(), LeafMonsterRenderer::new);
            event.registerEntityRenderer(ModEntities.ENTITY_LURKING_TERROR.get(), LurkingTerrorRenderer::new);
            event.registerEntityRenderer(ModEntities.ENTITY_MANTIS.get(), MantisRenderer::new);
            event.registerEntityRenderer(ModEntities.ENTITY_MOLENOID.get(), MolenoidRenderer::new);
            event.registerEntityRenderer(ModEntities.ENTITY_RAT.get(), RatRenderer::new);
            event.registerEntityRenderer(ModEntities.ENTITY_ROTATOR.get(), RotatorRenderer::new);
            event.registerEntityRenderer(ModEntities.ENTITY_SCORPION.get(), ScorpionRenderer::new);
            event.registerEntityRenderer(ModEntities.ENTITY_SPIT_BUG.get(), SpitBugRenderer::new);
            event.registerEntityRenderer(ModEntities.ENTITY_TERRIBLE_TERROR.get(), TerribleTerrorRenderer::new);
            event.registerEntityRenderer(ModEntities.ENTITY_TRIFFID.get(), TriffidRenderer::new);
            event.registerEntityRenderer(ModEntities.ENTITY_TROOPER_BUG.get(), TrooperBugRenderer::new);
            event.registerEntityRenderer(ModEntities.ENTITY_VORTEX.get(), VortexRenderer::new);
            event.registerEntityRenderer(ModEntities.ENTITY_WORM_SMALL.get(), WormSmallRenderer::new);
            event.registerEntityRenderer(ModEntities.ENTITY_WORM_MEDIUM.get(), WormMediumRenderer::new);
            event.registerEntityRenderer(ModEntities.ENTITY_WORM_LARGE.get(), WormLargeRenderer::new);

            // Creature (passive)
            event.registerEntityRenderer(ModEntities.BARYONYX.get(), BaryonyxRenderer::new);
            event.registerEntityRenderer(ModEntities.BEAVER.get(), BeaverRenderer::new);
            event.registerEntityRenderer(ModEntities.CASSOWARY.get(), CassowaryRenderer::new);
            event.registerEntityRenderer(ModEntities.CHIPMUNK.get(), ChipmunkRenderer::new);
            event.registerEntityRenderer(ModEntities.COCKATEIL.get(), CockateilRenderer::new);
            event.registerEntityRenderer(ModEntities.COIN.get(), CoinRenderer::new);
            event.registerEntityRenderer(ModEntities.EASTER_BUNNY.get(), EasterBunnyRenderer::new);
            event.registerEntityRenderer(ModEntities.FLOUNDER.get(), FlounderRenderer::new);
            event.registerEntityRenderer(ModEntities.GOLD_FISH.get(), GoldFishRenderer::new);
            event.registerEntityRenderer(ModEntities.ISLAND.get(), IslandRenderer::new);
            event.registerEntityRenderer(ModEntities.ISLAND_TOO.get(), IslandTooRenderer::new);
            event.registerEntityRenderer(ModEntities.PEACOCK.get(), PeacockRenderer::new);
            event.registerEntityRenderer(ModEntities.WHALE.get(), WhaleRenderer::new);
            event.registerEntityRenderer(ModEntities.FROG.get(), FrogRenderer::new);
            event.registerEntityRenderer(ModEntities.GAZELLE.get(), GazelleRenderer::new);
            event.registerEntityRenderer(ModEntities.ENTITY_ANT.get(), AntRenderer::new);
            event.registerEntityRenderer(ModEntities.ENTITY_CLIFF_RACER.get(), CliffRacerRenderer::new);
            event.registerEntityRenderer(ModEntities.ENTITY_CRICKET.get(), CricketRenderer::new);
            event.registerEntityRenderer(ModEntities.ENTITY_DRAGONFLY.get(), DragonflyRenderer::new);
            event.registerEntityRenderer(ModEntities.ENTITY_RED_ANT.get(), RedAntRenderer::new);
            event.registerEntityRenderer(ModEntities.ENTITY_RAINBOW_ANT.get(), RainbowAntRenderer::new);
            event.registerEntityRenderer(ModEntities.ENTITY_STINK_BUG.get(), StinkBugRenderer::new);
            event.registerEntityRenderer(ModEntities.ENTITY_TERMITE.get(), TermiteRenderer::new);
            event.registerEntityRenderer(ModEntities.ENTITY_TSHIRT.get(), TshirtRenderer::new);
            event.registerEntityRenderer(ModEntities.ENTITY_UNSTABLE_ANT.get(), UnstableAntRenderer::new);

            // Creature (tameable)
            event.registerEntityRenderer(ModEntities.BOYFRIEND.get(), BoyfriendRenderer::new);
            event.registerEntityRenderer(ModEntities.CAMARASAURUS.get(), CamarasaurusRenderer::new);
            event.registerEntityRenderer(ModEntities.DRAGON.get(), DragonRenderer::new);
            event.registerEntityRenderer(ModEntities.ENTITY_CANNON_FODDER.get(), CannonFodderRenderer::new);
            event.registerEntityRenderer(ModEntities.ENTITY_GAMMA_METROID.get(), GammaMetroidRenderer::new);
            event.registerEntityRenderer(ModEntities.GIRLFRIEND.get(), GirlfriendRenderer::new);
            event.registerEntityRenderer(ModEntities.ENTITY_HYDROLISC.get(), HydroliscRenderer::new);
            event.registerEntityRenderer(ModEntities.ENTITY_LEON.get(), LeonRenderer::new);
            event.registerEntityRenderer(ModEntities.LIZARD.get(), LizardRenderer::new);
            event.registerEntityRenderer(ModEntities.OSTRICH.get(), OstrichRenderer::new);
            event.registerEntityRenderer(ModEntities.ENTITY_RUBBER_DUCKY.get(), RubberDuckyRenderer::new);
            event.registerEntityRenderer(ModEntities.ENTITY_SPYRO.get(), SpyroRenderer::new);
            event.registerEntityRenderer(ModEntities.ENTITY_STINKY.get(), StinkyRenderer::new);
            event.registerEntityRenderer(ModEntities.THE_PRINCE.get(), ThePrinceRenderer::new);
            event.registerEntityRenderer(ModEntities.THE_PRINCE_ADULT.get(), ThePrinceAdultRenderer::new);
            event.registerEntityRenderer(ModEntities.THE_PRINCESS.get(), ThePrincessRenderer::new);
            event.registerEntityRenderer(ModEntities.THE_PRINCE_TEEN.get(), ThePrinceTeenRenderer::new);
            event.registerEntityRenderer(ModEntities.VELOCITY_RAPTOR.get(), VelocityRaptorRenderer::new);
            event.registerEntityRenderer(ModEntities.WATER_DRAGON.get(), WaterDragonRenderer::new);

            // Ambient
            event.registerEntityRenderer(ModEntities.ENTITY_BUTTERFLY.get(), ButterflyRenderer::new);
            event.registerEntityRenderer(ModEntities.ENTITY_LUNA_MOTH.get(), LunaMothRenderer::new);
            event.registerEntityRenderer(ModEntities.ENTITY_MOSQUITO.get(), MosquitoRenderer::new);
            event.registerEntityRenderer(ModEntities.FAIRY.get(), FairyRenderer::new);
            event.registerEntityRenderer(ModEntities.FIREFLY.get(), FireflyRenderer::new);
            event.registerEntityRenderer(ModEntities.GHOST.get(), GhostRenderer::new);
            event.registerEntityRenderer(ModEntities.GHOST_SKELLY.get(), GhostSkellyRenderer::new);
            event.registerEntityRenderer(ModEntities.MOTHRA.get(), MothraRenderer::new);

            // Misc (mob)
            event.registerEntityRenderer(ModEntities.ANT_ROBOT.get(), AntRobotRenderer::new);
            event.registerEntityRenderer(ModEntities.ELEVATOR.get(), ElevatorRenderer::new);
            event.registerEntityRenderer(ModEntities.KING_HEAD.get(), KingHeadRenderer::new);
            event.registerEntityRenderer(ModEntities.QUEEN_HEAD.get(), QueenHeadRenderer::new);
            event.registerEntityRenderer(ModEntities.GODZILLA_HEAD.get(), GodzillaHeadRenderer::new);
            event.registerEntityRenderer(ModEntities.PURPLE_POWER.get(), PurplePowerRenderer::new);
            event.registerEntityRenderer(ModEntities.ROCK_BASE.get(), RockBaseRenderer::new);
            event.registerEntityRenderer(ModEntities.SPIDER_ROBOT.get(), SpiderRobotRenderer::new);
            event.registerEntityRenderer(ModEntities.CEPHADROME.get(), CephadromeRenderer::new);
            event.registerEntityRenderer(ModEntities.RED_COW.get(), RedCowRenderer::new);
            event.registerEntityRenderer(ModEntities.SPIDER_DRIVER.get(), SpiderDriverRenderer::new);
            event.registerEntityRenderer(ModEntities.CRYSTAL_COW.get(), CrystalCowRenderer::new);
            event.registerEntityRenderer(ModEntities.GOLD_COW.get(), GoldCowRenderer::new);
            event.registerEntityRenderer(ModEntities.ENCHANTED_COW.get(), EnchantedCowRenderer::new);
            event.registerEntityRenderer(ModEntities.RUBY_BIRD.get(), CockateilRenderer::new);

            // Projectiles
            event.registerEntityRenderer(ModEntities.BETTER_FIREBALL.get(), NoopProjectileRenderer::new);
            event.registerEntityRenderer(ModEntities.BERTHA_HIT.get(), NoopProjectileRenderer::new);
            event.registerEntityRenderer(ModEntities.ENTITY_CAGE.get(), NoopProjectileRenderer::new);
            event.registerEntityRenderer(ModEntities.ENTITY_THROWN_ROCK.get(), NoopProjectileRenderer::new);
            event.registerEntityRenderer(ModEntities.INK_SACK.get(), NoopProjectileRenderer::new);
            event.registerEntityRenderer(ModEntities.LASER_BALL.get(), NoopProjectileRenderer::new);
            event.registerEntityRenderer(ModEntities.SHOES.get(), NoopProjectileRenderer::new);
            event.registerEntityRenderer(ModEntities.SUNSPOT_URCHIN.get(), NoopProjectileRenderer::new);
            event.registerEntityRenderer(ModEntities.THUNDER_BOLT.get(), NoopProjectileRenderer::new);
            event.registerEntityRenderer(ModEntities.WATER_BALL.get(), NoopProjectileRenderer::new);
            event.registerEntityRenderer(ModEntities.ICE_BALL.get(), NoopProjectileRenderer::new);
            event.registerEntityRenderer(ModEntities.ACID.get(), NoopProjectileRenderer::new);
            event.registerEntityRenderer(ModEntities.DEAD_IRUKANDJI.get(), NoopProjectileRenderer::new);
            event.registerEntityRenderer(ModEntities.ULTIMATE_ARROW.get(),
                    ctx -> new OreSpawnArrowRenderer<>(ctx, net.minecraft.resources.ResourceLocation.withDefaultNamespace("textures/entity/projectiles/arrow.png")));
            event.registerEntityRenderer(ModEntities.IRUKANDJI_ARROW.get(),
                    ctx -> new OreSpawnArrowRenderer<>(ctx, net.minecraft.resources.ResourceLocation.withDefaultNamespace("textures/entity/projectiles/arrow.png")));
            event.registerEntityRenderer(ModEntities.ULTIMATE_FISH_HOOK.get(), NoopProjectileRenderer::new);

            // Fire-immune item entity uses vanilla ItemEntity renderer
            event.registerEntityRenderer(ModEntities.LAVA_LOVING_ITEM.get(),
                    net.minecraft.client.renderer.entity.ItemEntityRenderer::new);
        }

        @SubscribeEvent
        public static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
            // Monster (hostile)
            event.registerLayerDefinition(AlienRenderer.MODEL_LAYER, ModelAlien::createBodyLayer);
            event.registerLayerDefinition(AlosaurusRenderer.MODEL_LAYER, ModelAlosaurus::createBodyLayer);
            event.registerLayerDefinition(AttackSquidRenderer.MODEL_LAYER, ModelAttackSquid::createBodyLayer);
            event.registerLayerDefinition(BandPRenderer.MODEL_LAYER, ModelBandP::createBodyLayer);
            event.registerLayerDefinition(CaveFisherRenderer.MODEL_LAYER, ModelCaveFisher::createBodyLayer);
            event.registerLayerDefinition(CloudSharkRenderer.MODEL_LAYER, ModelCloudShark::createBodyLayer);
            event.registerLayerDefinition(CrabRenderer.MODEL_LAYER, ModelCrab::createBodyLayer);
            event.registerLayerDefinition(CreepingHorrorRenderer.MODEL_LAYER, ModelCreepingHorror::createBodyLayer);
            event.registerLayerDefinition(CryolophosaurusRenderer.MODEL_LAYER, ModelCryolophosaurus::createBodyLayer);
            event.registerLayerDefinition(DungeonBeastRenderer.MODEL_LAYER, ModelDungeonBeast::createBodyLayer);
            event.registerLayerDefinition(EnderKnightRenderer.MODEL_LAYER, ModelEnderKnight::createBodyLayer);
            event.registerLayerDefinition(EnderReaperRenderer.MODEL_LAYER, ModelEnderReaper::createBodyLayer);
            event.registerLayerDefinition(GiantRobotRenderer.MODEL_LAYER, ModelGiantRobot::createBodyLayer);
            event.registerLayerDefinition(HammerheadRenderer.MODEL_LAYER, ModelHammerhead::createBodyLayer);
            event.registerLayerDefinition(IrukandjiRenderer.MODEL_LAYER, ModelIrukandji::createBodyLayer);
            event.registerLayerDefinition(NastysaurusRenderer.MODEL_LAYER, ModelNastysaurus::createBodyLayer);
            event.registerLayerDefinition(PitchBlackRenderer.MODEL_LAYER, ModelPitchBlack::createBodyLayer);
            event.registerLayerDefinition(PointysaurusRenderer.MODEL_LAYER, ModelPointysaurus::createBodyLayer);
            event.registerLayerDefinition(Robot1Renderer.MODEL_LAYER, ModelRobot1::createBodyLayer);
            event.registerLayerDefinition(Robot2Renderer.MODEL_LAYER, ModelRobot2::createBodyLayer);
            event.registerLayerDefinition(Robot3Renderer.MODEL_LAYER, ModelRobot3::createBodyLayer);
            event.registerLayerDefinition(Robot4Renderer.MODEL_LAYER, ModelRobot4::createBodyLayer);
            event.registerLayerDefinition(Robot5Renderer.MODEL_LAYER, ModelRobot5::createBodyLayer);
            event.registerLayerDefinition(SeaMonsterRenderer.MODEL_LAYER, ModelSeaMonster::createBodyLayer);
            event.registerLayerDefinition(SeaViperRenderer.MODEL_LAYER, ModelSeaViper::createBodyLayer);
            event.registerLayerDefinition(SkateRenderer.MODEL_LAYER, ModelSkate::createBodyLayer);
            event.registerLayerDefinition(TRexRenderer.MODEL_LAYER, ModelTRex::createBodyLayer);
            event.registerLayerDefinition(UrchinRenderer.MODEL_LAYER, ModelUrchin::createBodyLayer);
            event.registerLayerDefinition(BasiliskRenderer.MODEL_LAYER, ModelBasilisk::createBodyLayer);
            event.registerLayerDefinition(GodzillaRenderer.MODEL_LAYER, ModelGodzilla::createBodyLayer);
            event.registerLayerDefinition(KrakenRenderer.MODEL_LAYER, ModelKraken::createBodyLayer);
            event.registerLayerDefinition(TheKingRenderer.MODEL_LAYER, ModelTheKing::createBodyLayer);
            event.registerLayerDefinition(TheQueenRenderer.MODEL_LAYER, ModelTheQueen::createBodyLayer);
            event.registerLayerDefinition(BeeRenderer.MODEL_LAYER, BeeModel::createBodyLayer);
            event.registerLayerDefinition(BrutalflyRenderer.MODEL_LAYER, BrutalflyModel::createBodyLayer);
            event.registerLayerDefinition(CaterKillerRenderer.MODEL_LAYER, CaterKillerModel::createBodyLayer);
            event.registerLayerDefinition(EmperorScorpionRenderer.MODEL_LAYER, EmperorScorpionModel::createBodyLayer);
            event.registerLayerDefinition(HerculesBeetleRenderer.MODEL_LAYER, HerculesBeetleModel::createBodyLayer);
            event.registerLayerDefinition(KyuubiRenderer.MODEL_LAYER, KyuubiModel::createBodyLayer);
            event.registerLayerDefinition(LeafMonsterRenderer.MODEL_LAYER, LeafMonsterModel::createBodyLayer);
            event.registerLayerDefinition(LurkingTerrorRenderer.MODEL_LAYER, LurkingTerrorModel::createBodyLayer);
            event.registerLayerDefinition(MantisRenderer.MODEL_LAYER, MantisModel::createBodyLayer);
            event.registerLayerDefinition(MolenoidRenderer.MODEL_LAYER, MolenoidModel::createBodyLayer);
            event.registerLayerDefinition(RatRenderer.MODEL_LAYER, RatModel::createBodyLayer);
            event.registerLayerDefinition(RotatorRenderer.MODEL_LAYER, RotatorModel::createBodyLayer);
            event.registerLayerDefinition(ScorpionRenderer.MODEL_LAYER, ScorpionModel::createBodyLayer);
            event.registerLayerDefinition(SpitBugRenderer.MODEL_LAYER, SpitBugModel::createBodyLayer);
            event.registerLayerDefinition(TerribleTerrorRenderer.MODEL_LAYER, TerribleTerrorModel::createBodyLayer);
            event.registerLayerDefinition(TriffidRenderer.MODEL_LAYER, TriffidModel::createBodyLayer);
            event.registerLayerDefinition(TrooperBugRenderer.MODEL_LAYER, TrooperBugModel::createBodyLayer);
            event.registerLayerDefinition(VortexRenderer.MODEL_LAYER, VortexModel::createBodyLayer);
            event.registerLayerDefinition(WormSmallRenderer.MODEL_LAYER, WormSmallModel::createBodyLayer);
            event.registerLayerDefinition(WormMediumRenderer.MODEL_LAYER, WormMediumModel::createBodyLayer);
            event.registerLayerDefinition(WormLargeRenderer.MODEL_LAYER, WormLargeModel::createBodyLayer);

            // Creature (passive)
            event.registerLayerDefinition(BaryonyxRenderer.MODEL_LAYER, ModelBaryonyx::createBodyLayer);
            event.registerLayerDefinition(BeaverRenderer.MODEL_LAYER, ModelBeaver::createBodyLayer);
            event.registerLayerDefinition(CassowaryRenderer.MODEL_LAYER, ModelCassowary::createBodyLayer);
            event.registerLayerDefinition(ChipmunkRenderer.MODEL_LAYER, ModelChipmunk::createBodyLayer);
            event.registerLayerDefinition(CockateilRenderer.MODEL_LAYER, ModelCockateil::createBodyLayer);
            event.registerLayerDefinition(CoinRenderer.MODEL_LAYER, ModelCoin::createBodyLayer);
            event.registerLayerDefinition(EasterBunnyRenderer.MODEL_LAYER, ModelEasterBunny::createBodyLayer);
            event.registerLayerDefinition(FlounderRenderer.MODEL_LAYER, ModelFlounder::createBodyLayer);
            event.registerLayerDefinition(GoldFishRenderer.MODEL_LAYER, ModelGoldFish::createBodyLayer);
            event.registerLayerDefinition(IslandRenderer.MODEL_LAYER, ModelIsland::createBodyLayer);
            event.registerLayerDefinition(IslandTooRenderer.MODEL_LAYER, ModelIslandToo::createBodyLayer);
            event.registerLayerDefinition(PeacockRenderer.MODEL_LAYER, ModelPeacock::createBodyLayer);
            event.registerLayerDefinition(WhaleRenderer.MODEL_LAYER, ModelWhale::createBodyLayer);
            event.registerLayerDefinition(FrogRenderer.MODEL_LAYER, ModelFrog::createBodyLayer);
            event.registerLayerDefinition(GazelleRenderer.MODEL_LAYER, ModelGazelle::createBodyLayer);
            event.registerLayerDefinition(AntRenderer.MODEL_LAYER, AntModel::createBodyLayer);
            event.registerLayerDefinition(CliffRacerRenderer.MODEL_LAYER, CliffRacerModel::createBodyLayer);
            event.registerLayerDefinition(CricketRenderer.MODEL_LAYER, CricketModel::createBodyLayer);
            event.registerLayerDefinition(DragonflyRenderer.MODEL_LAYER, DragonflyModel::createBodyLayer);
            event.registerLayerDefinition(RedAntRenderer.MODEL_LAYER, AntModel::createBodyLayer);
            event.registerLayerDefinition(RainbowAntRenderer.MODEL_LAYER, AntModel::createBodyLayer);
            event.registerLayerDefinition(StinkBugRenderer.MODEL_LAYER, StinkBugModel::createBodyLayer);
            event.registerLayerDefinition(TermiteRenderer.MODEL_LAYER, TermiteModel::createBodyLayer);
            event.registerLayerDefinition(TshirtRenderer.MODEL_LAYER, TshirtModel::createBodyLayer);
            event.registerLayerDefinition(UnstableAntRenderer.MODEL_LAYER, AntModel::createBodyLayer);

            // Creature (tameable)
            event.registerLayerDefinition(BoyfriendRenderer.MODEL_LAYER, ModelBoyfriend::createBodyLayer);
            event.registerLayerDefinition(CamarasaurusRenderer.MODEL_LAYER, ModelCamarasaurus::createBodyLayer);
            event.registerLayerDefinition(DragonRenderer.MODEL_LAYER, ModelDragon::createBodyLayer);
            event.registerLayerDefinition(CannonFodderRenderer.MODEL_LAYER, CannonFodderModel::createBodyLayer);
            event.registerLayerDefinition(GammaMetroidRenderer.MODEL_LAYER, GammaMetroidModel::createBodyLayer);
            event.registerLayerDefinition(GirlfriendRenderer.MODEL_LAYER, ModelGirlfriend::createBodyLayer);
            event.registerLayerDefinition(HydroliscRenderer.MODEL_LAYER, HydroliscModel::createBodyLayer);
            event.registerLayerDefinition(LeonRenderer.MODEL_LAYER, LeonModel::createBodyLayer);
            event.registerLayerDefinition(LizardRenderer.MODEL_LAYER, LizardModel::createBodyLayer);
            event.registerLayerDefinition(OstrichRenderer.MODEL_LAYER, OstrichModel::createBodyLayer);
            event.registerLayerDefinition(RubberDuckyRenderer.MODEL_LAYER, RubberDuckyModel::createBodyLayer);
            event.registerLayerDefinition(SpyroRenderer.MODEL_LAYER, SpyroModel::createBodyLayer);
            event.registerLayerDefinition(StinkyRenderer.MODEL_LAYER, StinkyModel::createBodyLayer);
            event.registerLayerDefinition(ThePrinceRenderer.MODEL_LAYER, ModelThePrince::createBodyLayer);
            event.registerLayerDefinition(ThePrinceAdultRenderer.MODEL_LAYER, ModelThePrinceAdult::createBodyLayer);
            event.registerLayerDefinition(ThePrincessRenderer.MODEL_LAYER, ModelThePrincess::createBodyLayer);
            event.registerLayerDefinition(ThePrinceTeenRenderer.MODEL_LAYER, ModelThePrinceTeen::createBodyLayer);
            event.registerLayerDefinition(VelocityRaptorRenderer.MODEL_LAYER, VelocityRaptorModel::createBodyLayer);
            event.registerLayerDefinition(WaterDragonRenderer.MODEL_LAYER, ModelWaterDragon::createBodyLayer);

            // Ambient
            event.registerLayerDefinition(ButterflyRenderer.MODEL_LAYER, ButterflyModel::createBodyLayer);
            event.registerLayerDefinition(LunaMothRenderer.MODEL_LAYER, ButterflyModel::createBodyLayer);
            event.registerLayerDefinition(MosquitoRenderer.MODEL_LAYER, MosquitoModel::createBodyLayer);
            event.registerLayerDefinition(FairyRenderer.MODEL_LAYER, FairyModel::createBodyLayer);
            event.registerLayerDefinition(FireflyRenderer.MODEL_LAYER, FireflyModel::createBodyLayer);
            event.registerLayerDefinition(GhostRenderer.MODEL_LAYER, GhostModel::createBodyLayer);
            event.registerLayerDefinition(GhostSkellyRenderer.MODEL_LAYER, GhostSkellyModel::createBodyLayer);
            event.registerLayerDefinition(MothraRenderer.MODEL_LAYER, ButterflyModel::createBodyLayer);

            // Misc (mob) - custom models only (RedCow and SpiderDriver use vanilla models)
            event.registerLayerDefinition(AntRobotRenderer.MODEL_LAYER, ModelAntRobot::createBodyLayer);
            event.registerLayerDefinition(ElevatorRenderer.MODEL_LAYER, ModelElevator::createBodyLayer);
            event.registerLayerDefinition(KingHeadRenderer.MODEL_LAYER, ModelKingHead::createBodyLayer);
            event.registerLayerDefinition(QueenHeadRenderer.MODEL_LAYER, ModelQueenHead::createBodyLayer);
            event.registerLayerDefinition(GodzillaHeadRenderer.MODEL_LAYER, ModelGodzillaHead::createBodyLayer);
            event.registerLayerDefinition(PurplePowerRenderer.MODEL_LAYER, ModelPurplePower::createBodyLayer);
            event.registerLayerDefinition(RockBaseRenderer.MODEL_LAYER, ModelRockBase::createBodyLayer);
            event.registerLayerDefinition(SpiderRobotRenderer.MODEL_LAYER, ModelSpiderRobot::createBodyLayer);
            event.registerLayerDefinition(CephadromeRenderer.MODEL_LAYER, ModelCephadrome::createBodyLayer);
        }

        @SubscribeEvent
        public static void registerScreens(RegisterMenuScreensEvent event) {
            event.register(ModMenuTypes.CRYSTAL_FURNACE_MENU.get(), CrystalFurnaceScreen::new);
            event.register(ModMenuTypes.CRYSTAL_WORKBENCH_MENU.get(), CrystalWorkbenchScreen::new);
        }

        @SubscribeEvent
        public static void registerAdditionalModels(ModelEvent.RegisterAdditional event) {
            String[] weapons = {"big_bertha", "slice", "royal_guardian_sword", "battle_axe",
                    "queen_battle_axe", "chainsaw", "attitude_adjuster", "squid_zooka"};
            for (String name : weapons) {
                event.register(ModelResourceLocation.standalone(
                        ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "item/" + name + "_flat")));
            }
        }

        @SubscribeEvent
        public static void registerClientExtensions(RegisterClientExtensionsEvent event) {
            IClientItemExtensions bewlrExtension = new IClientItemExtensions() {
                @Override
                public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                    return OreSpawnItemRenderer.getInstance();
                }
            };

            event.registerItem(bewlrExtension,
                    ModItems.BIG_BERTHA.get(),
                    ModItems.SLICE.get(),
                    ModItems.ROYAL_GUARDIAN_SWORD.get(),
                    ModItems.BATTLE_AXE.get(),
                    ModItems.QUEEN_BATTLE_AXE.get(),
                    ModItems.CHAINSAW.get(),
                    ModItems.ATTITUDE_ADJUSTER.get(),
                    ModItems.SQUID_ZOOKA.get());
        }
    }
}
