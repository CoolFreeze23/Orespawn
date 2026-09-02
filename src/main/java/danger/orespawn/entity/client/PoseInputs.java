package danger.orespawn.entity.client;

import net.minecraft.world.entity.Entity;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.model.data.EntityModelData;

/**
 * The six values vanilla's {@code LivingEntityRenderer} hands {@code setupAnim},
 * plus the pose subject, as a plain record.
 *
 * <p>Code-driven poses consume this instead of GeckoLib's {@link AnimationState}
 * so the same production hook runs both on the client (through
 * {@link #fromState}, which reads the state the replaced renderer built) and
 * in the headless parity probe, where GeckoLib's {@link DataTickets} cannot be
 * loaded because its initialiser registers a data component and therefore
 * needs a bootstrapped game.</p>
 *
 * @param subject       the entity being drawn, or in the harness a stand-in
 *                      implementing the species' {@code danger.orespawn.entity.pose}
 *                      interface; hooks ask for it by interface
 * @param ageInTicks    vanilla {@code getBob}: {@code (float) tickCount + partialTick}
 * @param limbSwing     walk position, tripled for babies, zero when dead or riding
 * @param limbSwingAmount walk speed clamped to one, zero when dead or riding
 * @param netHeadYaw    head yaw minus body yaw, degrees, vanilla sign
 * @param headPitch     head pitch, degrees, vanilla sign
 */
public record PoseInputs(Object subject, float ageInTicks, float limbSwing, float limbSwingAmount,
                         float netHeadYaw, float headPitch) {

    /** The subject seen through the species' pose interface; a wrong or missing subject is a wiring failure. */
    public <T> T subject(Class<T> poseInterface) {
        if (!poseInterface.isInstance(this.subject)) {
            throw new IllegalStateException("pose subject " + this.subject + " does not implement "
                    + poseInterface.getName());
        }
        return poseInterface.cast(this.subject);
    }

    /**
     * Reads what {@code GeoReplacedEntityRenderer.actuallyRender} stored
     * (bytecode-read for GeckoLib 4.8.4): {@code TICK} carries
     * {@code getTick(entity)}, the entity's integer tick count; {@code ENTITY}
     * the entity; and {@code ENTITY_MODEL_DATA} both head angles NEGATED, which
     * this undoes so the classic formulas keep vanilla's sign.
     */
    public static PoseInputs fromState(AnimationState<?> state) {
        Entity entity = state.getData(DataTickets.ENTITY);
        Double tick = state.getData(DataTickets.TICK);
        float ageInTicks;
        if (tick != null) {
            ageInTicks = (float) tick.doubleValue() + state.getPartialTick();
        } else if (entity != null) {
            ageInTicks = (float) entity.tickCount + state.getPartialTick();
        } else {
            throw new IllegalStateException("AnimationState carries neither TICK nor ENTITY");
        }
        EntityModelData modelData = state.getData(DataTickets.ENTITY_MODEL_DATA);
        if (modelData == null) {
            throw new IllegalStateException("AnimationState carries no ENTITY_MODEL_DATA datum");
        }
        return new PoseInputs(entity, ageInTicks, state.getLimbSwing(), state.getLimbSwingAmount(),
                -modelData.netHeadYaw(), -modelData.headPitch());
    }
}
