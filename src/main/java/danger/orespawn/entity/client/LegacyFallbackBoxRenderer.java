package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import org.joml.Matrix4f;

/**
 * Replicates the 1.7.10 default-renderer fallback for entities that had NO
 * registered render handler: RenderManager mapped {@code Entity.class} to
 * {@code RenderEntity}, whose doRender drew the entity's bounding box as a
 * solid, untextured white cube via {@code Render.renderOffsetAABB} (normals
 * per face under standard item lighting gave the classic face shading).
 *
 * ENTITY_NOOP_RENDERER/thunder_bolt uses this: the original ThunderBolt is
 * absent from ClientProxyOreSpawn.java:384-527 (reference_1_7_10_source), so
 * players actually saw this white box, not custom geometry. Face brightness
 * here mirrors vanilla's directional block shade (top 1.0, N/S 0.8, E/W 0.6,
 * bottom 0.5) to approximate the original GL-lit look; debugQuads is unlit
 * POSITION_COLOR, matching the untextured original.
 */
public class LegacyFallbackBoxRenderer<T extends Entity> extends EntityRenderer<T> {

    private static final float SHADE_UP = 1.0f;
    private static final float SHADE_DOWN = 0.5f;
    private static final float SHADE_NORTH_SOUTH = 0.8f;
    private static final float SHADE_EAST_WEST = 0.6f;

    public LegacyFallbackBoxRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(T entity, float entityYaw, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight) {
        // orig 1.7.10 RenderEntity.doRender -> renderOffsetAABB(entity.boundingBox, ...):
        // the box is the entity AABB, re-based onto the interpolated render position.
        AABB box = entity.getBoundingBox().move(-entity.getX(), -entity.getY(), -entity.getZ());
        float x0 = (float) box.minX, y0 = (float) box.minY, z0 = (float) box.minZ;
        float x1 = (float) box.maxX, y1 = (float) box.maxY, z1 = (float) box.maxZ;

        VertexConsumer consumer = bufferSource.getBuffer(RenderType.debugQuads());
        Matrix4f pose = poseStack.last().pose();
        // down / up
        quad(consumer, pose, SHADE_DOWN, x0, y0, z1, x1, y0, z1, x1, y0, z0, x0, y0, z0);
        quad(consumer, pose, SHADE_UP, x0, y1, z0, x1, y1, z0, x1, y1, z1, x0, y1, z1);
        // north (z-) / south (z+)
        quad(consumer, pose, SHADE_NORTH_SOUTH, x0, y0, z0, x1, y0, z0, x1, y1, z0, x0, y1, z0);
        quad(consumer, pose, SHADE_NORTH_SOUTH, x1, y0, z1, x0, y0, z1, x0, y1, z1, x1, y1, z1);
        // west (x-) / east (x+)
        quad(consumer, pose, SHADE_EAST_WEST, x0, y0, z1, x0, y0, z0, x0, y1, z0, x0, y1, z1);
        quad(consumer, pose, SHADE_EAST_WEST, x1, y0, z0, x1, y0, z1, x1, y1, z1, x1, y1, z0);

        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
    }

    private static void quad(VertexConsumer consumer, Matrix4f pose, float shade,
                             float ax, float ay, float az, float bx, float by, float bz,
                             float cx, float cy, float cz, float dx, float dy, float dz) {
        int gray = (int) (255.0f * shade);
        consumer.addVertex(pose, ax, ay, az).setColor(gray, gray, gray, 255);
        consumer.addVertex(pose, bx, by, bz).setColor(gray, gray, gray, 255);
        consumer.addVertex(pose, cx, cy, cz).setColor(gray, gray, gray, 255);
        consumer.addVertex(pose, dx, dy, dz).setColor(gray, gray, gray, 255);
    }

    @Override
    public ResourceLocation getTextureLocation(T entity) {
        // Never sampled — debugQuads is POSITION_COLOR (untextured), like the
        // original renderOffsetAABB draw.
        return ResourceLocation.withDefaultNamespace("textures/misc/white.png");
    }
}
