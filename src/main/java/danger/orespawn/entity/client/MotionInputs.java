package danger.orespawn.entity.client;

/**
 * The locomotion state the standard animation contract reads from the entity, once per frame, as a plain record
 * ({@code phase_g_reports/animation_contract/contract_design.md} section 3; the weights slice). Filled by the
 * replaced renderer's model on every frame it animates ({@link
 * OreSpawnGeoReplacement#motionInputs}, from {@link OreSpawnGeoReplacementModel#handleAnimations}) and by the
 * headless harness from explicit values ({@link #of}), so the contract's weights run the same production code on
 * both.
 *
 * <p>The two booleans EXACTLY as contract section 3 defines them: {@code flying} is the SPEC's {@code locomotion: flyer}
 * ({@link danger.orespawn.entity.client.animation.LocomotionKind#FLYER}, the seed's word) AND
 * {@code !entity.onGround()}, or {@code FlyingAnimal.isFlying()} where a species implements that interface;
 * {@code inWater} is {@code Entity.isInWater()}, narrowed to {@code isUnderWater()} where a SPEC says so
 * ({@link OreSpawnGeoReplacement#underwaterOnly()}). Both are client-visible without a packet ({@code onGround} is
 * synced by movement, the client evaluates fluid heights every tick).</p>
 *
 * @param subject         the entity being drawn, or {@code null} for a harness input
 * @param ageInTicks      vanilla {@code getBob}: {@code (float) tickCount + partialTick} - the clock every ramp and
 *                        edge detector is keyed on (ENT-S-147: a repeated frame recomputes the same value, a skipped
 *                        frame is caught up)
 * @param limbSwingAmount walk speed clamped to one, zero when dead or riding (the renderer's state; {@code w_move})
 * @param inWater         {@code Entity.isInWater()} (or {@code isUnderWater()} for a SPEC that narrows it)
 * @param flying          the SPEC's flyer AND {@code !onGround()}, or {@code FlyingAnimal.isFlying()}
 * @param attacking       the species' synched attacking flag through its pose-style accessor, 0 where it has none
 *                        ({@link OreSpawnGeoReplacement#attacking}); a STATE flag drives {@code w_aggro}, an EVENT
 *                        flag's rising edge fires {@code attack} (section 4.3 / 4.4)
 * @param hurtTime        {@code LivingEntity.hurtTime} (0 for a non-living species): its rising edge fires {@code hurt}
 *                        (section 4.1)
 * @param deathTime       {@code LivingEntity.deathTime} (0 for a non-living species): its first positive frame fires
 *                        {@code death} (section 4.2)
 */
public record MotionInputs(Object subject, float ageInTicks, float limbSwingAmount, boolean inWater, boolean flying,
                           int attacking, int hurtTime, int deathTime) {
    public MotionInputs {
        if (!Float.isFinite(ageInTicks) || !Float.isFinite(limbSwingAmount)) {
            throw new IllegalArgumentException("motion inputs need a finite age and limb-swing amount");
        }
    }

    /** A harness input carrying no entity and no trigger signal: the four locomotion values alone. */
    public static MotionInputs of(float ageInTicks, float limbSwingAmount, boolean inWater, boolean flying) {
        return new MotionInputs(null, ageInTicks, limbSwingAmount, inWater, flying, 0, 0, 0);
    }

    /** This input with the trigger signals set (the harness's hurt / death edges and the attacking flag). */
    public MotionInputs withSignals(int attacking, int hurtTime, int deathTime) {
        return new MotionInputs(this.subject, this.ageInTicks, this.limbSwingAmount, this.inWater, this.flying,
                attacking, hurtTime, deathTime);
    }
}
