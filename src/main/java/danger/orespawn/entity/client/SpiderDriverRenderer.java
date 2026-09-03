package danger.orespawn.entity.client;

import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.SpiderDriver;
import net.minecraft.client.model.SpiderModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * orig RenderSpiderDriver.java + ClientProxyOreSpawn.java:515:
 * {@code new RenderSpiderDriver(new ModelSpider(), 0.5f)}: the constructor body is EMPTY
 * (RenderSpiderDriver.java:18-19), so the 0.5f is discarded and the implicit super() runs vanilla 1.7.10
 * RenderSpider(), whose constructor is {@code super(new ModelSpider(), 1.0F)} - shadowSize 1.0 (vanilla
 * 1.7.10 MCP source; no 1.7.10 client jar is on this machine). Neither RenderSpider nor this subclass
 * adds a preRenderCallback scale (doRender :22-31 delegates straight to super): world scale 1.0.
 * The port's former 0.8f was the modern vanilla SpiderRenderer value (ENT-S-092).
 */
public class SpiderDriverRenderer extends MobRenderer<SpiderDriver, SpiderModel<SpiderDriver>> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/spider_driver.png");
    /** orig vanilla 1.7.10 RenderSpider: no preRenderCallback scale, and RenderSpiderDriver adds none. */
    public static final float SCALE = 1.0F;
    /** orig shadow = vanilla 1.7.10 RenderSpider() {@code super(new ModelSpider(), 1.0F)}; the registered 0.5f is discarded. */
    public static final float SHADOW = 1.0F;

    public SpiderDriverRenderer(EntityRendererProvider.Context context) {
        super(context, new SpiderModel<>(context.bakeLayer(ModelLayers.SPIDER)), SHADOW);
    }

    @Override
    public ResourceLocation getTextureLocation(SpiderDriver entity) {
        return TEXTURE;
    }
}
