package danger.orespawn.entity.client;

import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

/**
 * Deliberate do-nothing renderer, retained ONLY for entities the ORIGINAL
 * 1.7.10 mod also rendered as nothing. Sole remaining user: BerthaHit
 * (ENTITY_NOOP_RENDERER/bertha_hit) — the orig RenderItemUrchin.java:21-23
 * (reference_1_7_10_source) early-returns for BerthaHit before drawing, so an
 * invisible entity is the faithful presentation for that swing-damage proxy.
 * Do not bind visible projectiles here; give them a real renderer instead.
 */
public class NoopProjectileRenderer<T extends Entity> extends EntityRenderer<T> {

    public NoopProjectileRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ResourceLocation getTextureLocation(T entity) {
        return ResourceLocation.withDefaultNamespace("textures/misc/white.png");
    }
}