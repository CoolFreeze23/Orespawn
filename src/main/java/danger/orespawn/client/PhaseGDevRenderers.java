package danger.orespawn.client;

import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.Alien;
import danger.orespawn.entity.AlienBoss;
import danger.orespawn.entity.Alosaurus;
import danger.orespawn.entity.AttackSquid;
import danger.orespawn.entity.BabyDragon;
import danger.orespawn.entity.BandP;
import danger.orespawn.entity.Baryonyx;
import danger.orespawn.entity.Basilisk;
import danger.orespawn.entity.Beaver;
import danger.orespawn.entity.Boyfriend;
import danger.orespawn.entity.Camarasaurus;
import danger.orespawn.entity.Cassowary;
import danger.orespawn.entity.CaveFisher;
import danger.orespawn.entity.Cephadrome;
import danger.orespawn.entity.Chipmunk;
import danger.orespawn.entity.CloudShark;
import danger.orespawn.entity.Cockateil;
import danger.orespawn.entity.Coin;
import danger.orespawn.entity.Crab;
import danger.orespawn.entity.CreepingHorror;
import danger.orespawn.entity.Cryolophosaurus;
import danger.orespawn.entity.Dragon;
import danger.orespawn.entity.DungeonBeast;
import danger.orespawn.entity.EasterBunny;
import danger.orespawn.entity.Elevator;
import danger.orespawn.entity.EnderKnight;
import danger.orespawn.entity.EnderReaper;
import danger.orespawn.entity.EntityAnt;
import danger.orespawn.entity.EntityBee;
import danger.orespawn.entity.EntityBrutalfly;
import danger.orespawn.entity.EntityButterfly;
import danger.orespawn.entity.EntityCannonFodder;
import danger.orespawn.entity.EntityCaterKiller;
import danger.orespawn.entity.EntityCliffRacer;
import danger.orespawn.entity.EntityCricket;
import danger.orespawn.entity.EntityDragonfly;
import danger.orespawn.entity.EntityEmperorScorpion;
import danger.orespawn.entity.EntityGammaMetroid;
import danger.orespawn.entity.EntityHerculesBeetle;
import danger.orespawn.entity.EntityHydrolisc;
import danger.orespawn.entity.EntityKyuubi;
import danger.orespawn.entity.EntityLeafMonster;
import danger.orespawn.entity.EntityLeon;
import danger.orespawn.entity.EntityLunaMoth;
import danger.orespawn.entity.EntityLurkingTerror;
import danger.orespawn.entity.EntityMantis;
import danger.orespawn.entity.EntityMolenoid;
import danger.orespawn.entity.EntityMosquito;
import danger.orespawn.entity.EntityRainbowAnt;
import danger.orespawn.entity.EntityRat;
import danger.orespawn.entity.EntityRedAnt;
import danger.orespawn.entity.EntityRotator;
import danger.orespawn.entity.EntityRubberDucky;
import danger.orespawn.entity.EntityScorpion;
import danger.orespawn.entity.EntitySpitBug;
import danger.orespawn.entity.EntitySpyro;
import danger.orespawn.entity.EntityStinkBug;
import danger.orespawn.entity.EntityStinky;
import danger.orespawn.entity.EntityTermite;
import danger.orespawn.entity.EntityTerribleTerror;
import danger.orespawn.entity.EntityTriffid;
import danger.orespawn.entity.EntityTrooperBug;
import danger.orespawn.entity.EntityTshirt;
import danger.orespawn.entity.EntityUnstableAnt;
import danger.orespawn.entity.EntityVortex;
import danger.orespawn.entity.EntityWormLarge;
import danger.orespawn.entity.EntityWormMedium;
import danger.orespawn.entity.EntityWormSmall;
import danger.orespawn.entity.Fairy;
import danger.orespawn.entity.Firefly;
import danger.orespawn.entity.Flounder;
import danger.orespawn.entity.Frog;
import danger.orespawn.entity.Gazelle;
import danger.orespawn.entity.Ghost;
import danger.orespawn.entity.GhostSkelly;
import danger.orespawn.entity.GiantRobot;
import danger.orespawn.entity.Girlfriend;
import danger.orespawn.entity.Godzilla;
import danger.orespawn.entity.GoldFish;
import danger.orespawn.entity.Hammerhead;
import danger.orespawn.entity.Irukandji;
import danger.orespawn.entity.Island;
import danger.orespawn.entity.IslandToo;
import danger.orespawn.entity.Jeffery;
import danger.orespawn.entity.Kraken;
import danger.orespawn.entity.Lizard;
import danger.orespawn.entity.Mothra;
import danger.orespawn.entity.Nastysaurus;
import danger.orespawn.entity.Ostrich;
import danger.orespawn.entity.Peacock;
import danger.orespawn.entity.PitchBlack;
import danger.orespawn.entity.Pointysaurus;
import danger.orespawn.entity.PurplePower;
import danger.orespawn.entity.Robot1;
import danger.orespawn.entity.Robot2;
import danger.orespawn.entity.Robot3;
import danger.orespawn.entity.Robot4;
import danger.orespawn.entity.Robot5;
import danger.orespawn.entity.RockBase;
import danger.orespawn.entity.SeaMonster;
import danger.orespawn.entity.SeaViper;
import danger.orespawn.entity.Skate;
import danger.orespawn.entity.TRex;
import danger.orespawn.entity.TheKing;
import danger.orespawn.entity.ThePrince;
import danger.orespawn.entity.ThePrinceAdult;
import danger.orespawn.entity.ThePrinceTeen;
import danger.orespawn.entity.ThePrincess;
import danger.orespawn.entity.Urchin;
import danger.orespawn.entity.VampireButterfly;
import danger.orespawn.entity.VelocityRaptor;
import danger.orespawn.entity.WaterDragon;
import danger.orespawn.entity.Whale;
import danger.orespawn.entity.client.AlienBossGeoReplacement;
import danger.orespawn.entity.client.AlienGeoReplacement;
import danger.orespawn.entity.client.AlienRenderer;
import danger.orespawn.entity.client.AlosaurusGeoReplacement;
import danger.orespawn.entity.client.AlosaurusRenderer;
import danger.orespawn.entity.client.AntGeoReplacement;
import danger.orespawn.entity.client.AntRenderer;
import danger.orespawn.entity.client.AttackSquidGeoReplacement;
import danger.orespawn.entity.client.AttackSquidRenderer;
import danger.orespawn.entity.client.BabyDragonGeoReplacement;
import danger.orespawn.entity.client.BabyDragonRenderer;
import danger.orespawn.entity.client.BandPGeoReplacement;
import danger.orespawn.entity.client.BandPRenderer;
import danger.orespawn.entity.client.BaryonyxGeoReplacement;
import danger.orespawn.entity.client.BaryonyxRenderer;
import danger.orespawn.entity.client.BasiliskGeoReplacement;
import danger.orespawn.entity.client.BasiliskRenderer;
import danger.orespawn.entity.client.BeaverGeoReplacedRenderer;
import danger.orespawn.entity.client.BeaverRenderer;
import danger.orespawn.entity.client.BeeGeoReplacement;
import danger.orespawn.entity.client.BeeRenderer;
import danger.orespawn.entity.client.BoyfriendGeoReplacement;
import danger.orespawn.entity.client.BoyfriendRenderer;
import danger.orespawn.entity.client.BrutalflyGeoReplacement;
import danger.orespawn.entity.client.BrutalflyRenderer;
import danger.orespawn.entity.client.ButterflyGeoReplacement;
import danger.orespawn.entity.client.ButterflyRenderer;
import danger.orespawn.entity.client.CamarasaurusGeoReplacement;
import danger.orespawn.entity.client.CamarasaurusRenderer;
import danger.orespawn.entity.client.CannonFodderGeoReplacement;
import danger.orespawn.entity.client.CannonFodderRenderer;
import danger.orespawn.entity.client.CassowaryGeoReplacement;
import danger.orespawn.entity.client.CassowaryRenderer;
import danger.orespawn.entity.client.CaterKillerGeoReplacement;
import danger.orespawn.entity.client.CaterKillerRenderer;
import danger.orespawn.entity.client.CaveFisherGeoReplacement;
import danger.orespawn.entity.client.CaveFisherRenderer;
import danger.orespawn.entity.client.CephadromeGeoReplacement;
import danger.orespawn.entity.client.CephadromeRenderer;
import danger.orespawn.entity.client.ChipmunkGeoReplacement;
import danger.orespawn.entity.client.ChipmunkRenderer;
import danger.orespawn.entity.client.CliffRacerGeoReplacement;
import danger.orespawn.entity.client.CliffRacerRenderer;
import danger.orespawn.entity.client.CloudSharkGeoReplacement;
import danger.orespawn.entity.client.CloudSharkRenderer;
import danger.orespawn.entity.client.CockateilGeoReplacement;
import danger.orespawn.entity.client.CockateilRenderer;
import danger.orespawn.entity.client.CoinGeoReplacement;
import danger.orespawn.entity.client.CoinRenderer;
import danger.orespawn.entity.client.CrabGeoReplacement;
import danger.orespawn.entity.client.CrabRenderer;
import danger.orespawn.entity.client.CreepingHorrorGeoReplacement;
import danger.orespawn.entity.client.CreepingHorrorRenderer;
import danger.orespawn.entity.client.CricketGeoReplacement;
import danger.orespawn.entity.client.CricketRenderer;
import danger.orespawn.entity.client.CryolophosaurusGeoReplacement;
import danger.orespawn.entity.client.CryolophosaurusRenderer;
import danger.orespawn.entity.client.DragonGeoReplacement;
import danger.orespawn.entity.client.DragonRenderer;
import danger.orespawn.entity.client.DragonflyGeoReplacement;
import danger.orespawn.entity.client.DragonflyRenderer;
import danger.orespawn.entity.client.DungeonBeastGeoReplacement;
import danger.orespawn.entity.client.DungeonBeastRenderer;
import danger.orespawn.entity.client.EasterBunnyGeoReplacement;
import danger.orespawn.entity.client.EasterBunnyRenderer;
import danger.orespawn.entity.client.ElevatorGeoReplacement;
import danger.orespawn.entity.client.ElevatorRenderer;
import danger.orespawn.entity.client.EmperorScorpionGeoReplacement;
import danger.orespawn.entity.client.EmperorScorpionRenderer;
import danger.orespawn.entity.client.EnderKnightGeoReplacement;
import danger.orespawn.entity.client.EnderKnightRenderer;
import danger.orespawn.entity.client.EnderReaperGeoReplacement;
import danger.orespawn.entity.client.EnderReaperRenderer;
import danger.orespawn.entity.client.FairyGeoReplacement;
import danger.orespawn.entity.client.FairyRenderer;
import danger.orespawn.entity.client.FireflyGeoReplacement;
import danger.orespawn.entity.client.FireflyRenderer;
import danger.orespawn.entity.client.FlounderGeoReplacement;
import danger.orespawn.entity.client.FlounderRenderer;
import danger.orespawn.entity.client.FrogGeoReplacement;
import danger.orespawn.entity.client.FrogRenderer;
import danger.orespawn.entity.client.GammaMetroidGeoReplacement;
import danger.orespawn.entity.client.GammaMetroidRenderer;
import danger.orespawn.entity.client.GazelleGeoReplacement;
import danger.orespawn.entity.client.GazelleRenderer;
import danger.orespawn.entity.client.GhostGeoReplacement;
import danger.orespawn.entity.client.GhostRenderer;
import danger.orespawn.entity.client.GhostSkellyGeoReplacement;
import danger.orespawn.entity.client.GhostSkellyRenderer;
import danger.orespawn.entity.client.GiantRobotGeoReplacement;
import danger.orespawn.entity.client.GiantRobotRenderer;
import danger.orespawn.entity.client.GirlfriendGeoReplacement;
import danger.orespawn.entity.client.GirlfriendRenderer;
import danger.orespawn.entity.client.GodzillaGeoReplacement;
import danger.orespawn.entity.client.GodzillaRenderer;
import danger.orespawn.entity.client.GoldFishGeoReplacement;
import danger.orespawn.entity.client.GoldFishRenderer;
import danger.orespawn.entity.client.HammerheadGeoReplacement;
import danger.orespawn.entity.client.HammerheadRenderer;
import danger.orespawn.entity.client.HerculesBeetleGeoReplacement;
import danger.orespawn.entity.client.HerculesBeetleRenderer;
import danger.orespawn.entity.client.HydroliscGeoReplacement;
import danger.orespawn.entity.client.HydroliscRenderer;
import danger.orespawn.entity.client.IrukandjiGeoReplacement;
import danger.orespawn.entity.client.IrukandjiRenderer;
import danger.orespawn.entity.client.IslandGeoReplacement;
import danger.orespawn.entity.client.IslandRenderer;
import danger.orespawn.entity.client.IslandTooGeoReplacement;
import danger.orespawn.entity.client.IslandTooRenderer;
import danger.orespawn.entity.client.JefferyGeoReplacement;
import danger.orespawn.entity.client.KrakenGeoReplacement;
import danger.orespawn.entity.client.KrakenRenderer;
import danger.orespawn.entity.client.KyuubiGeoReplacement;
import danger.orespawn.entity.client.KyuubiRenderer;
import danger.orespawn.entity.client.LeafMonsterGeoReplacement;
import danger.orespawn.entity.client.LeafMonsterRenderer;
import danger.orespawn.entity.client.LeonGeoReplacement;
import danger.orespawn.entity.client.LeonRenderer;
import danger.orespawn.entity.client.LeonopteryxGeoReplacement;
import danger.orespawn.entity.client.LizardGeoReplacement;
import danger.orespawn.entity.client.LizardRenderer;
import danger.orespawn.entity.client.LunaMothGeoReplacement;
import danger.orespawn.entity.client.LunaMothRenderer;
import danger.orespawn.entity.client.LurkingTerrorGeoReplacement;
import danger.orespawn.entity.client.LurkingTerrorRenderer;
import danger.orespawn.entity.client.MantisGeoReplacement;
import danger.orespawn.entity.client.MantisRenderer;
import danger.orespawn.entity.client.MolenoidGeoReplacement;
import danger.orespawn.entity.client.MolenoidRenderer;
import danger.orespawn.entity.client.MosquitoGeoReplacement;
import danger.orespawn.entity.client.MosquitoRenderer;
import danger.orespawn.entity.client.MothraGeoReplacement;
import danger.orespawn.entity.client.MothraRenderer;
import danger.orespawn.entity.client.NastysaurusGeoReplacement;
import danger.orespawn.entity.client.NastysaurusRenderer;
import danger.orespawn.entity.client.OstrichGeoReplacement;
import danger.orespawn.entity.client.OstrichRenderer;
import danger.orespawn.entity.client.PeacockGeoReplacement;
import danger.orespawn.entity.client.PeacockRenderer;
import danger.orespawn.entity.client.PitchBlackGeoReplacement;
import danger.orespawn.entity.client.PitchBlackRenderer;
import danger.orespawn.entity.client.PointysaurusGeoReplacement;
import danger.orespawn.entity.client.PointysaurusRenderer;
import danger.orespawn.entity.client.PurplePowerGeoReplacement;
import danger.orespawn.entity.client.PurplePowerRenderer;
import danger.orespawn.entity.client.RainbowAntGeoReplacement;
import danger.orespawn.entity.client.RainbowAntRenderer;
import danger.orespawn.entity.client.RatGeoReplacement;
import danger.orespawn.entity.client.RatRenderer;
import danger.orespawn.entity.client.RedAntGeoReplacement;
import danger.orespawn.entity.client.RedAntRenderer;
import danger.orespawn.entity.client.Robot1GeoReplacement;
import danger.orespawn.entity.client.Robot1Renderer;
import danger.orespawn.entity.client.Robot2GeoReplacement;
import danger.orespawn.entity.client.Robot2Renderer;
import danger.orespawn.entity.client.Robot3GeoReplacement;
import danger.orespawn.entity.client.Robot3Renderer;
import danger.orespawn.entity.client.Robot4GeoReplacement;
import danger.orespawn.entity.client.Robot4Renderer;
import danger.orespawn.entity.client.Robot5GeoReplacement;
import danger.orespawn.entity.client.Robot5Renderer;
import danger.orespawn.entity.client.RockBaseGeoReplacement;
import danger.orespawn.entity.client.RockBaseRenderer;
import danger.orespawn.entity.client.RotatorGeoReplacement;
import danger.orespawn.entity.client.RotatorRenderer;
import danger.orespawn.entity.client.RubberDuckyGeoReplacement;
import danger.orespawn.entity.client.RubberDuckyRenderer;
import danger.orespawn.entity.client.RubyBirdGeoReplacement;
import danger.orespawn.entity.client.ScorpionGeoReplacement;
import danger.orespawn.entity.client.ScorpionRenderer;
import danger.orespawn.entity.client.SeaMonsterGeoReplacement;
import danger.orespawn.entity.client.SeaMonsterRenderer;
import danger.orespawn.entity.client.SeaViperGeoReplacement;
import danger.orespawn.entity.client.SeaViperRenderer;
import danger.orespawn.entity.client.SkateGeoReplacement;
import danger.orespawn.entity.client.SkateRenderer;
import danger.orespawn.entity.client.SpitBugGeoReplacement;
import danger.orespawn.entity.client.SpitBugRenderer;
import danger.orespawn.entity.client.SpyroGeoReplacement;
import danger.orespawn.entity.client.SpyroRenderer;
import danger.orespawn.entity.client.StinkBugGeoReplacement;
import danger.orespawn.entity.client.StinkBugRenderer;
import danger.orespawn.entity.client.StinkyGeoReplacement;
import danger.orespawn.entity.client.StinkyRenderer;
import danger.orespawn.entity.client.TRexGeoReplacement;
import danger.orespawn.entity.client.TRexRenderer;
import danger.orespawn.entity.client.TermiteGeoReplacement;
import danger.orespawn.entity.client.TermiteRenderer;
import danger.orespawn.entity.client.TerribleTerrorGeoReplacement;
import danger.orespawn.entity.client.TerribleTerrorRenderer;
import danger.orespawn.entity.client.TheKingGeoReplacement;
import danger.orespawn.entity.client.TheKingRenderer;
import danger.orespawn.entity.client.ThePrinceAdultGeoReplacement;
import danger.orespawn.entity.client.ThePrinceAdultRenderer;
import danger.orespawn.entity.client.ThePrinceGeoReplacement;
import danger.orespawn.entity.client.ThePrinceRenderer;
import danger.orespawn.entity.client.ThePrinceTeenGeoReplacement;
import danger.orespawn.entity.client.ThePrinceTeenRenderer;
import danger.orespawn.entity.client.ThePrincessGeoReplacement;
import danger.orespawn.entity.client.ThePrincessRenderer;
import danger.orespawn.entity.client.TriffidGeoReplacement;
import danger.orespawn.entity.client.TriffidRenderer;
import danger.orespawn.entity.client.TrooperBugGeoReplacement;
import danger.orespawn.entity.client.TrooperBugRenderer;
import danger.orespawn.entity.client.TshirtGeoReplacement;
import danger.orespawn.entity.client.TshirtRenderer;
import danger.orespawn.entity.client.UnstableAntGeoReplacement;
import danger.orespawn.entity.client.UnstableAntRenderer;
import danger.orespawn.entity.client.UrchinGeoReplacement;
import danger.orespawn.entity.client.UrchinRenderer;
import danger.orespawn.entity.client.VampireButterflyGeoReplacement;
import danger.orespawn.entity.client.VampireButterflyRenderer;
import danger.orespawn.entity.client.VelocityRaptorGeoReplacement;
import danger.orespawn.entity.client.VelocityRaptorRenderer;
import danger.orespawn.entity.client.VortexGeoReplacement;
import danger.orespawn.entity.client.VortexRenderer;
import danger.orespawn.entity.client.WaterDragonGeoReplacement;
import danger.orespawn.entity.client.WaterDragonRenderer;
import danger.orespawn.entity.client.WhaleGeoReplacement;
import danger.orespawn.entity.client.WhaleRenderer;
import danger.orespawn.entity.client.WormLargeGeoReplacement;
import danger.orespawn.entity.client.WormLargeRenderer;
import danger.orespawn.entity.client.WormMediumGeoReplacement;
import danger.orespawn.entity.client.WormMediumRenderer;
import danger.orespawn.entity.client.WormSmallGeoReplacement;
import danger.orespawn.entity.client.WormSmallRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.entity.Entity;

/**
 * Resolves the Phase G developer switch into renderer providers at registration time. Species ids are registry names.
 *
 * <p>ON THE BRANCH {@code default-flip} the switch is inverted: the GeckoLib candidate is the default for every registry
 * that goes through {@link #select} - every landed rig - and {@code -Dorespawn.dev.classicRenderers} (the Beaver
 * alias the same grammar) names the species that keep their classic renderer; a registry with no landed rig is
 * registered by OreSpawnClient directly and keeps its classic renderer regardless; the Queen native as always. The
 * per-slice notes below that say "the classic renderers the default" record each slice's landing on master, where
 * that is still the default; the branch merges once approved. The start-up line names the classic exceptions now
 * (master's named the candidates).</p>
 *
 */
public final class PhaseGDevRenderers {
    private PhaseGDevRenderers() {
    }

    public static EntityRendererProvider<Beaver> beaverRenderer() {
        return select("beaver", BeaverRenderer::new, BeaverGeoReplacedRenderer::new);
    }

    public static EntityRendererProvider<Elevator> elevatorRenderer() {
        return select("elevator", ElevatorRenderer::new, ElevatorGeoReplacement.Renderer::new);
    }

    public static EntityRendererProvider<EntityVortex> vortexRenderer() {
        return select("vortex", VortexRenderer::new, VortexGeoReplacement.Renderer::new);
    }

    public static EntityRendererProvider<Coin> coinRenderer() {
        return select("coin", CoinRenderer::new, CoinGeoReplacement.Renderer::new);
    }

    public static EntityRendererProvider<Island> islandRenderer() {
        return select("island", IslandRenderer::new, IslandGeoReplacement.Renderer::new);
    }

    public static EntityRendererProvider<IslandToo> islandTooRenderer() {
        return select("island_too", IslandTooRenderer::new, IslandTooGeoReplacement.Renderer::new);
    }

    public static EntityRendererProvider<Robot1> robot1Renderer() {
        return select("robot_1", Robot1Renderer::new, Robot1GeoReplacement.Renderer::new);
    }

    public static EntityRendererProvider<Robot2> robot2Renderer() {
        return select("robot_2", Robot2Renderer::new, Robot2GeoReplacement.Renderer::new);
    }

    public static EntityRendererProvider<Robot3> robot3Renderer() {
        return select("robot_3", Robot3Renderer::new, Robot3GeoReplacement.Renderer::new);
    }

    public static EntityRendererProvider<Robot4> robot4Renderer() {
        return select("robot_4", Robot4Renderer::new, Robot4GeoReplacement.Renderer::new);
    }

    public static EntityRendererProvider<Robot5> robot5Renderer() {
        return select("robot_5", Robot5Renderer::new, Robot5GeoReplacement.Renderer::new);
    }

    public static EntityRendererProvider<RockBase> rockBaseRenderer() {
        return select("rock_base", RockBaseRenderer::new, RockBaseGeoReplacement.Renderer::new);
    }

    /** Slice 4c: the render-instance-expanded rigs. */
    public static EntityRendererProvider<PurplePower> purplePowerRenderer() {
        return select("purple_power", PurplePowerRenderer::new, PurplePowerGeoReplacement.Renderer::new);
    }

    public static EntityRendererProvider<EntityRotator> rotatorRenderer() {
        return select("rotator", RotatorRenderer::new, RotatorGeoReplacement.Renderer::new);
    }

    /** The first Tier-2 slice (2026-09-13): the simple-cyclic rigs, the classic renderers the default. */
    public static EntityRendererProvider<EntityTshirt> tshirtRenderer() {
        return select("tshirt", TshirtRenderer::new, TshirtGeoReplacement.Renderer::new);
    }

    public static EntityRendererProvider<EntityMosquito> mosquitoRenderer() {
        return select("mosquito", MosquitoRenderer::new, MosquitoGeoReplacement.Renderer::new);
    }

    public static EntityRendererProvider<EntityCliffRacer> cliffRacerRenderer() {
        return select("cliff_racer", CliffRacerRenderer::new, CliffRacerGeoReplacement.Renderer::new);
    }



    public static EntityRendererProvider<EntityBrutalfly> brutalflyRenderer() {
        return select("brutalfly", BrutalflyRenderer::new, BrutalflyGeoReplacement.Renderer::new);
    }

    public static EntityRendererProvider<EntityDragonfly> dragonflyRenderer() {
        return select("dragonfly", DragonflyRenderer::new, DragonflyGeoReplacement.Renderer::new);
    }

    /** One rig, two consumers (design Q9: one profile per registry path): the Cockateil and the Ruby Bird select separately. */
    public static EntityRendererProvider<Cockateil> cockateilRenderer() {
        return select("cockateil", CockateilRenderer::new, CockateilGeoReplacement.Renderer::new);
    }

    /** The classic path draws the Ruby Bird with the Cockateil's renderer (RubyBird extends Cockateil), so both providers are typed over Cockateil, as OreSpawnClient always registered it. */
    public static EntityRendererProvider<Cockateil> rubyBirdRenderer() {
        return select("ruby_bird", CockateilRenderer::new, RubyBirdGeoReplacement.Renderer::new);
    }

    /** The second Tier-2 slice (2026-09-13): the two rigs rejoined under ENT-S-161 (the Cloud Shark waits on the visual leg's tie rule), the classic renderers the default. */
    public static EntityRendererProvider<Firefly> fireflyRenderer() {
        return select("firefly", FireflyRenderer::new, FireflyGeoReplacement.Renderer::new);
    }

    public static EntityRendererProvider<GoldFish> goldFishRenderer() {
        return select("gold_fish", GoldFishRenderer::new, GoldFishGeoReplacement.Renderer::new);
    }

    /** One rig, five consumers (design Q9: one profile per registry path): the Ant and its four siblings select separately, each over its own entity class as the classic renderers are typed. */
    public static EntityRendererProvider<EntityAnt> antRenderer() {
        return select("ant", AntRenderer::new, AntGeoReplacement.Renderer::new);
    }

    public static EntityRendererProvider<EntityRainbowAnt> rainbowAntRenderer() {
        return select("rainbow_ant", RainbowAntRenderer::new, RainbowAntGeoReplacement.Renderer::new);
    }

    public static EntityRendererProvider<EntityRedAnt> redAntRenderer() {
        return select("red_ant", RedAntRenderer::new, RedAntGeoReplacement.Renderer::new);
    }

    public static EntityRendererProvider<EntityTermite> termiteRenderer() {
        return select("termite", TermiteRenderer::new, TermiteGeoReplacement.Renderer::new);
    }

    public static EntityRendererProvider<EntityUnstableAnt> unstableAntRenderer() {
        return select("unstable_ant", UnstableAntRenderer::new, UnstableAntGeoReplacement.Renderer::new);
    }


    /** The third Tier-2 slice (2026-09-13): the fifteen rigs ON THE HOOK, the classic renderers the default. */
    public static EntityRendererProvider<CloudShark> cloudSharkRenderer() {
        return select("cloud_shark", CloudSharkRenderer::new, CloudSharkGeoReplacement.Renderer::new);
    }

    public static EntityRendererProvider<EntityBee> beeRenderer() {
        return select("bee", BeeRenderer::new, BeeGeoReplacement.Renderer::new);
    }

    public static EntityRendererProvider<Fairy> fairyRenderer() {
        return select("fairy", FairyRenderer::new, FairyGeoReplacement.Renderer::new);
    }

    public static EntityRendererProvider<EntityGammaMetroid> gammaMetroidRenderer() {
        return select("gamma_metroid", GammaMetroidRenderer::new, GammaMetroidGeoReplacement.Renderer::new);
    }

    public static EntityRendererProvider<Irukandji> irukandjiRenderer() {
        return select("irukandji", IrukandjiRenderer::new, IrukandjiGeoReplacement.Renderer::new);
    }

    public static EntityRendererProvider<Skate> skateRenderer() {
        return select("skate", SkateRenderer::new, SkateGeoReplacement.Renderer::new);
    }

    public static EntityRendererProvider<EntityRubberDucky> rubberDuckyRenderer() {
        return select("rubber_ducky", RubberDuckyRenderer::new, RubberDuckyGeoReplacement.Renderer::new);
    }

    public static EntityRendererProvider<EntityTerribleTerror> terribleTerrorRenderer() {
        return select("terrible_terror", TerribleTerrorRenderer::new, TerribleTerrorGeoReplacement.Renderer::new);
    }

    public static EntityRendererProvider<EntityWormLarge> wormLargeRenderer() {
        return select("worm_large", WormLargeRenderer::new, WormLargeGeoReplacement.Renderer::new);
    }

    public static EntityRendererProvider<EntityWormMedium> wormMediumRenderer() {
        return select("worm_medium", WormMediumRenderer::new, WormMediumGeoReplacement.Renderer::new);
    }

    public static EntityRendererProvider<EntityWormSmall> wormSmallRenderer() {
        return select("worm_small", WormSmallRenderer::new, WormSmallGeoReplacement.Renderer::new);
    }

    public static EntityRendererProvider<EntityCannonFodder> cannonFodderRenderer() {
        return select("cannon_fodder", CannonFodderRenderer::new, CannonFodderGeoReplacement.Renderer::new);
    }

    public static EntityRendererProvider<EntityCaterKiller> caterKillerRenderer() {
        return select("cater_killer", CaterKillerRenderer::new, CaterKillerGeoReplacement.Renderer::new);
    }

    public static EntityRendererProvider<EntityCricket> cricketRenderer() {
        return select("cricket", CricketRenderer::new, CricketGeoReplacement.Renderer::new);
    }

    public static EntityRendererProvider<EntityHerculesBeetle> herculesBeetleRenderer() {
        return select("hercules_beetle", HerculesBeetleRenderer::new, HerculesBeetleGeoReplacement.Renderer::new);
    }


    /** The fourth Tier-2 slice (T2d, 2026-09-14): the thirteen rigs ON THE HOOKS already written, the Crab with its draw fix; the classic renderers the default. */
    public static EntityRendererProvider<Crab> crabRenderer() {
        return select("crab", CrabRenderer::new, CrabGeoReplacement.Renderer::new);
    }

    public static EntityRendererProvider<EntityKyuubi> kyuubiRenderer() {
        return select("kyuubi", KyuubiRenderer::new, KyuubiGeoReplacement.Renderer::new);
    }

    public static EntityRendererProvider<EntityLeafMonster> leafMonsterRenderer() {
        return select("leaf_monster", LeafMonsterRenderer::new, LeafMonsterGeoReplacement.Renderer::new);
    }

    public static EntityRendererProvider<Alosaurus> alosaurusRenderer() {
        return select("alosaurus", AlosaurusRenderer::new, AlosaurusGeoReplacement.Renderer::new);
    }

    public static EntityRendererProvider<AttackSquid> attackSquidRenderer() {
        return select("attack_squid", AttackSquidRenderer::new, AttackSquidGeoReplacement.Renderer::new);
    }

    public static EntityRendererProvider<BandP> bandPRenderer() {
        return select("band_p", BandPRenderer::new, BandPGeoReplacement.Renderer::new);
    }

    public static EntityRendererProvider<Baryonyx> baryonyxRenderer() {
        return select("baryonyx", BaryonyxRenderer::new, BaryonyxGeoReplacement.Renderer::new);
    }

    public static EntityRendererProvider<Camarasaurus> camarasaurusRenderer() {
        return select("camarasaurus", CamarasaurusRenderer::new, CamarasaurusGeoReplacement.Renderer::new);
    }

    public static EntityRendererProvider<Cassowary> cassowaryRenderer() {
        return select("cassowary", CassowaryRenderer::new, CassowaryGeoReplacement.Renderer::new);
    }

    public static EntityRendererProvider<CreepingHorror> creepingHorrorRenderer() {
        return select("creeping_horror", CreepingHorrorRenderer::new, CreepingHorrorGeoReplacement.Renderer::new);
    }

    public static EntityRendererProvider<Cryolophosaurus> cryolophosaurusRenderer() {
        return select("cryolophosaurus", CryolophosaurusRenderer::new, CryolophosaurusGeoReplacement.Renderer::new);
    }

    public static EntityRendererProvider<EasterBunny> easterBunnyRenderer() {
        return select("easter_bunny", EasterBunnyRenderer::new, EasterBunnyGeoReplacement.Renderer::new);
    }

    public static EntityRendererProvider<Flounder> flounderRenderer() {
        return select("flounder", FlounderRenderer::new, FlounderGeoReplacement.Renderer::new);
    }


    /** The fifth Tier-2 slice (T2e, 2026-09-15): the fifteen rigs ON THE HOOKS already written; the classic renderers the default. */
    public static EntityRendererProvider<Pointysaurus> pointysaurusRenderer() {
        return select("pointysaurus", PointysaurusRenderer::new, PointysaurusGeoReplacement.Renderer::new);
    }

    public static EntityRendererProvider<Whale> whaleRenderer() {
        return select("whale", WhaleRenderer::new, WhaleGeoReplacement.Renderer::new);
    }

    public static EntityRendererProvider<EntityMolenoid> molenoidRenderer() {
        return select("molenoid", MolenoidRenderer::new, MolenoidGeoReplacement.Renderer::new);
    }

    public static EntityRendererProvider<EntityRat> ratRenderer() {
        return select("rat", RatRenderer::new, RatGeoReplacement.Renderer::new);
    }

    public static EntityRendererProvider<EntitySpitBug> spitBugRenderer() {
        return select("spit_bug", SpitBugRenderer::new, SpitBugGeoReplacement.Renderer::new);
    }

    public static EntityRendererProvider<EntityStinkBug> stinkBugRenderer() {
        return select("stink_bug", StinkBugRenderer::new, StinkBugGeoReplacement.Renderer::new);
    }

    public static EntityRendererProvider<EntityTrooperBug> trooperBugRenderer() {
        return select("trooper_bug", TrooperBugRenderer::new, TrooperBugGeoReplacement.Renderer::new);
    }

    public static EntityRendererProvider<VelocityRaptor> velocityRaptorRenderer() {
        return select("velocity_raptor", VelocityRaptorRenderer::new, VelocityRaptorGeoReplacement.Renderer::new);
    }

    public static EntityRendererProvider<GhostSkelly> ghostSkellyRenderer() {
        return select("ghost_skelly", GhostSkellyRenderer::new, GhostSkellyGeoReplacement.Renderer::new);
    }

    public static EntityRendererProvider<EntityHydrolisc> hydroliscRenderer() {
        return select("hydrolisc", HydroliscRenderer::new, HydroliscGeoReplacement.Renderer::new);
    }

    public static EntityRendererProvider<Lizard> lizardRenderer() {
        return select("lizard", LizardRenderer::new, LizardGeoReplacement.Renderer::new);
    }

    public static EntityRendererProvider<EntityMantis> mantisRenderer() {
        return select("mantis", MantisRenderer::new, MantisGeoReplacement.Renderer::new);
    }

    public static EntityRendererProvider<CaveFisher> caveFisherRenderer() {
        return select("cave_fisher", CaveFisherRenderer::new, CaveFisherGeoReplacement.Renderer::new);
    }

    public static EntityRendererProvider<Chipmunk> chipmunkRenderer() {
        return select("chipmunk", ChipmunkRenderer::new, ChipmunkGeoReplacement.Renderer::new);
    }


    /**
     * The FK slice (under the hierarchy rules): the Alien rig's two registries and the Emperor Scorpion ON THE HOOKS
     * already written, shipped as real parent-child hierarchies (FlatRig); the classic renderers the default. The Alien
     * Boss's classic side is the AlienRenderer (orig ClientProxyOreSpawn.java:435 drew both registries with the one
     * RenderAlien; the port registers AlienRenderer, typed on Alien, for the AlienBoss type through registerEntityRenderer's
     * {@code EntityType<? extends T>} widening) - {@code select} widens it the same way, so the switch keys the two
     * registries apart ({@code alien}, {@code alien_boss}).
     */
    public static EntityRendererProvider<Alien> alienRenderer() {
        return select("alien", AlienRenderer::new, AlienGeoReplacement.Renderer::new);
    }

    public static EntityRendererProvider<AlienBoss> alienBossRenderer() {
        return select("alien_boss", AlienRenderer::new, AlienBossGeoReplacement.Renderer::new);
    }

    public static EntityRendererProvider<EntityEmperorScorpion> emperorScorpionRenderer() {
        return select("emperor_scorpion", EmperorScorpionRenderer::new, EmperorScorpionGeoReplacement.Renderer::new);
    }

    /** The sixth Tier-2 slice (T2f, 2026-09-15): the twelve rigs ON THE HOOKS already written (the Dungeon Beast and the Scorpion held); the classic renderers the default. */
    public static EntityRendererProvider<EnderKnight> enderKnightRenderer() {
        return select("ender_knight", EnderKnightRenderer::new, EnderKnightGeoReplacement.Renderer::new);
    }

    public static EntityRendererProvider<EnderReaper> enderReaperRenderer() {
        return select("ender_reaper", EnderReaperRenderer::new, EnderReaperGeoReplacement.Renderer::new);
    }

    public static EntityRendererProvider<Frog> frogRenderer() {
        return select("frog", FrogRenderer::new, FrogGeoReplacement.Renderer::new);
    }

    public static EntityRendererProvider<Gazelle> gazelleRenderer() {
        return select("gazelle", GazelleRenderer::new, GazelleGeoReplacement.Renderer::new);
    }

    public static EntityRendererProvider<Nastysaurus> nastysaurusRenderer() {
        return select("nastysaurus", NastysaurusRenderer::new, NastysaurusGeoReplacement.Renderer::new);
    }

    public static EntityRendererProvider<Peacock> peacockRenderer() {
        return select("peacock", PeacockRenderer::new, PeacockGeoReplacement.Renderer::new);
    }

    public static EntityRendererProvider<SeaViper> seaViperRenderer() {
        return select("sea_viper", SeaViperRenderer::new, SeaViperGeoReplacement.Renderer::new);
    }

    public static EntityRendererProvider<Urchin> urchinRenderer() {
        return select("urchin", UrchinRenderer::new, UrchinGeoReplacement.Renderer::new);
    }

    public static EntityRendererProvider<Ostrich> ostrichRenderer() {
        return select("ostrich", OstrichRenderer::new, OstrichGeoReplacement.Renderer::new);
    }

    public static EntityRendererProvider<EntitySpyro> spyroRenderer() {
        return select("spyro", SpyroRenderer::new, SpyroGeoReplacement.Renderer::new);
    }

    public static EntityRendererProvider<EntityStinky> stinkyRenderer() {
        return select("stinky", StinkyRenderer::new, StinkyGeoReplacement.Renderer::new);
    }

    public static EntityRendererProvider<EntityTriffid> triffidRenderer() {
        return select("triffid", TriffidRenderer::new, TriffidGeoReplacement.Renderer::new);
    }

    /**
     * The first Tier-1 slice (T1a): the seventeen registries of the thirteen landed Tier-1 rigs and the Dungeon Beast ON
     * THE HOOKS already written (the King held on its second translucent membrane pass, the Butterfly rig on the Mothra's pair-contested cap); the classic renderers the default.
     * The three shared consumers whose classic renderer is typed on the parent species (Jeffery on the
     * GiantRobotRenderer, the Baby Dragon on the BabyDragonRenderer, the Leonopteryx on the LeonRenderer - the same class) go
     * through the generalised {@code select} of the FK slice, keyed apart by their own registries.
     */
    public static EntityRendererProvider<Kraken> krakenRenderer() {
        return select("kraken", KrakenRenderer::new, KrakenGeoReplacement.Renderer::new);
    }

    public static EntityRendererProvider<DungeonBeast> dungeonBeastRenderer() {
        return select("dungeon_beast", DungeonBeastRenderer::new, DungeonBeastGeoReplacement.Renderer::new);
    }

    public static EntityRendererProvider<Basilisk> basiliskRenderer() {
        return select("basilisk", BasiliskRenderer::new, BasiliskGeoReplacement.Renderer::new);
    }

    public static EntityRendererProvider<Godzilla> godzillaRenderer() {
        return select("godzilla", GodzillaRenderer::new, GodzillaGeoReplacement.Renderer::new);
    }

    public static EntityRendererProvider<Hammerhead> hammerheadRenderer() {
        return select("hammerhead", HammerheadRenderer::new, HammerheadGeoReplacement.Renderer::new);
    }

    public static EntityRendererProvider<TRex> tRexRenderer() {
        return select("trex", TRexRenderer::new, TRexGeoReplacement.Renderer::new);
    }

    public static EntityRendererProvider<EntityLeon> leonRenderer() {
        return select("leon", LeonRenderer::new, LeonGeoReplacement.Renderer::new);
    }

    public static EntityRendererProvider<EntityLeon> leonopteryxRenderer() {
        return select("leonopteryx", LeonRenderer::new, LeonopteryxGeoReplacement.Renderer::new);
    }

    public static EntityRendererProvider<Cephadrome> cephadromeRenderer() {
        return select("cephadrome", CephadromeRenderer::new, CephadromeGeoReplacement.Renderer::new);
    }

    public static EntityRendererProvider<Dragon> dragonRenderer() {
        return select("dragon", DragonRenderer::new, DragonGeoReplacement.Renderer::new);
    }

    public static EntityRendererProvider<BabyDragon> babyDragonRenderer() {
        return select("baby_dragon", BabyDragonRenderer::new, BabyDragonGeoReplacement.Renderer::new);
    }

    public static EntityRendererProvider<GiantRobot> giantRobotRenderer() {
        return select("giant_robot", GiantRobotRenderer::new, GiantRobotGeoReplacement.Renderer::new);
    }

    public static EntityRendererProvider<Jeffery> jefferyRenderer() {
        return select("jeffery", GiantRobotRenderer::new, JefferyGeoReplacement.Renderer::new);
    }

    public static EntityRendererProvider<PitchBlack> pitchBlackRenderer() {
        return select("pitch_black", PitchBlackRenderer::new, PitchBlackGeoReplacement.Renderer::new);
    }

    public static EntityRendererProvider<SeaMonster> seaMonsterRenderer() {
        return select("sea_monster", SeaMonsterRenderer::new, SeaMonsterGeoReplacement.Renderer::new);
    }

    public static EntityRendererProvider<ThePrince> thePrinceRenderer() {
        return select("the_prince", ThePrinceRenderer::new, ThePrinceGeoReplacement.Renderer::new);
    }

    public static EntityRendererProvider<ThePrinceAdult> thePrinceAdultRenderer() {
        return select("the_prince_adult", ThePrinceAdultRenderer::new, ThePrinceAdultGeoReplacement.Renderer::new);
    }

    /**
     * The second Tier-1 slice (T1b): the last two Tier-1 rigs ON THE HOOKS already written - the Prince Teen and the Water
     * Dragon (the King and the Butterfly rig held by T1a, the Princess the remainder's); the classic renderers the default.
     *
     */
    public static EntityRendererProvider<ThePrinceTeen> thePrinceTeenRenderer() {
        return select("the_prince_teen", ThePrinceTeenRenderer::new, ThePrinceTeenGeoReplacement.Renderer::new);
    }

    public static EntityRendererProvider<WaterDragon> waterDragonRenderer() {
        return select("water_dragon", WaterDragonRenderer::new, WaterDragonGeoReplacement.Renderer::new);
    }

    /**
     * The remainder slice: the seven remaining rigs ON THEIR HOOKS - the Lurking Terror and the Scorpion (the pair-contested
     * rule), the Ghost (ENT-S-160 (a)), the Boyfriend, Girlfriend and Princess (item 3: the seam's partial tick and the classic
     * layers), the King (the second pass, TEST-018); the Butterfly rig's four consumers REPORTED, not landed then
     * (TEST-019; landed by the Butterfly rig's slice below); the classic renderers the default.
     */
    public static EntityRendererProvider<EntityLurkingTerror> lurkingTerrorRenderer() {
        return select("lurking_terror", LurkingTerrorRenderer::new, LurkingTerrorGeoReplacement.Renderer::new);
    }

    public static EntityRendererProvider<EntityScorpion> scorpionRenderer() {
        return select("scorpion", ScorpionRenderer::new, ScorpionGeoReplacement.Renderer::new);
    }

    public static EntityRendererProvider<Ghost> ghostRenderer() {
        return select("ghost", GhostRenderer::new, GhostGeoReplacement.Renderer::new);
    }

    public static EntityRendererProvider<Boyfriend> boyfriendRenderer() {
        return select("boyfriend", BoyfriendRenderer::new, BoyfriendGeoReplacement.Renderer::new);
    }

    public static EntityRendererProvider<Girlfriend> girlfriendRenderer() {
        return select("girlfriend", GirlfriendRenderer::new, GirlfriendGeoReplacement.Renderer::new);
    }

    public static EntityRendererProvider<ThePrincess> thePrincessRenderer() {
        return select("the_princess", ThePrincessRenderer::new, ThePrincessGeoReplacement.Renderer::new);
    }

    public static EntityRendererProvider<TheKing> theKingRenderer() {
        return select("the_king", TheKingRenderer::new, TheKingGeoReplacement.Renderer::new);
    }

    /**
     * The Butterfly rig's slice (TEST-019): one rig, four consumers ON THE HOOKS already written - the Butterfly, the Luna
     * Moth, Mothra and the Vampire Butterfly on butterfly.geo.json at their own wingspeeds (1.0 / 0.75 / 0.2 / 1.0), landed under
     * the manifest-declared pair-contested cap (the Mothra's entry pinned at 803 pixels on leftwing / leftwing2 at
     * t_three_quarter, the three others at 0); each selects separately over its own entity class as the classic renderers are
     * typed (the Ant precedent, design Q9: one profile per registry path); the classic renderers the default.
     */
    public static EntityRendererProvider<EntityButterfly> butterflyRenderer() {
        return select("butterfly", ButterflyRenderer::new, ButterflyGeoReplacement.Renderer::new);
    }

    public static EntityRendererProvider<EntityLunaMoth> lunaMothRenderer() {
        return select("luna_moth", LunaMothRenderer::new, LunaMothGeoReplacement.Renderer::new);
    }

    public static EntityRendererProvider<Mothra> mothraRenderer() {
        return select("mothra", MothraRenderer::new, MothraGeoReplacement.Renderer::new);
    }

    public static EntityRendererProvider<VampireButterfly> vampireButterflyRenderer() {
        return select("vampire_butterfly", VampireButterflyRenderer::new, VampireButterflyGeoReplacement.Renderer::new);
    }


    /**
     * {@code P} is the species the classic renderer draws and {@code E extends P} the registry's own entity: for every
     * species but one they are the same class; the Alien Boss (the FK slice) is a shared consumer whose classic renderer is
     * its parent species' - the AlienRenderer, typed on Alien (orig ClientProxyOreSpawn.java:435 registered the one
     * RenderAlien for both, and the port's {@code registerEntityRenderer(EntityType<? extends T>, EntityRendererProvider<T>)}
     * widened it the same way) - returned under the boss's own provider type. The bound {@code E extends P} checks the
     * widening at compile time (a renderer of the parent class draws the subclass entity); the unchecked cast is the type
     * system's word for it and nothing runs differently.
     */
    @SuppressWarnings("unchecked")
    private static <P extends Entity, E extends P> EntityRendererProvider<E> select(String species,
                                                                                    EntityRendererProvider<P> classic,
                                                                                    EntityRendererProvider<E> candidate) {
        if (DevRendererSwitch.geckolib(species) == DevRendererSwitch.Variant.CLASSIC) {
            OreSpawnMod.LOGGER.warn("Phase G dev switch (default-flip): {} is keeping its classic renderer "
                    + "(selected by -D{}); every other landed rig draws with its GeckoLib candidate, the default on this "
                    + "branch. This is a review build, not a production cutover.",
                    species, DevRendererSwitch.classicSource(species));
            return (EntityRendererProvider<E>) (EntityRendererProvider<?>) classic;
        }
        return candidate;
    }
}
