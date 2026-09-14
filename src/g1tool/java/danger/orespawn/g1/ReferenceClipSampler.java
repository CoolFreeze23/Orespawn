package danger.orespawn.g1;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import danger.orespawn.entity.client.DrawOrder;
import danger.orespawn.entity.client.FaceOrder;
import danger.orespawn.entity.client.GeoReplacementDescriptor;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Pattern;
import net.minecraft.core.Direction;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.loading.json.raw.Model;
import software.bernie.geckolib.loading.json.typeadapter.KeyFramesAdapter;

/**
 * The reference-clip sampler (owner 2026-09-13, second set, addendum item 27 (3); every hook and every reachable state
 * since 2026-09-14, addendum item 31 (11); EVERY STATE A HOOK READS, named by the seed, keyed by the density search, since
 * owner 2026-09-14, second set revised, item 32 (2)-(6)): for every seam rig with a manifest entry AND every unlanded hook
 * (a reference entry whose model class has a {@code <Name>GeoReplacement} descriptor, {@link #HOOK_DESCRIPTORS}), the
 * classic hook sampled at FIXED inputs into reference-only Bedrock clips, one file per REGISTRY and STATE:
 * {@code tools/reference_clips/<registry>_reference_<state>.animation.json}, the Bedrock clip inside each named
 * {@code reference_<state>} - plus the index {@code reference_clips.json} the package generator
 * ({@code tools/artist_package.py package}) reads for the SPEC's "Reference clips (reference-only)" section, the
 * manifest's {@code reference_clips} block and the .bbmodel's embedded animations. A clip is never shipped and never
 * returned under its own name: the package checker refuses it and the asset audit refuses it under
 * {@code src/main/resources}; its keys may be the starting point of a delivered {@code idle} / {@code walk} /
 * {@code aggro_idle} / {@code fly} / {@code swim} or of an offered extra (item 32 (4)).
 *
 * <p>THE POSE SOURCE (the S4 doctrine): the {@code OreSpawnGeoReplacement} named by the manifest's
 * {@code candidate_class} - or, for an unlanded hook, the descriptor {@link #HOOK_DESCRIPTORS} names - instantiated
 * registry-free ({@link S4CandidateRuntime#instantiate}) and posed through {@code OreSpawnGeoReplacement.pose} on
 * explicit {@code PoseInputs} over a fresh bake of the geo: the SHIPPED geo the descriptor names ({@code modelResource})
 * for a landed rig, exactly as the harness's geo dumps pose it ({@link S4CandidateRuntime#evaluateProductionHook}); the
 * reference leg's converter output {@code build/reference/generated/reference_<rig>.geo.json} (gradle
 * {@code referenceConvertModels}) for an unlanded hook, baked without the face-order strictness its descriptor may
 * declare (the converter's output carries the draw-order key, not yet the face-order key the landing slice's TEST-007
 * writes; the face order moves no bone). The Beaver's shipped hook takes the renderer's {@code AnimationState} (no
 * {@code PoseInputs} form), so it is sampled through the probe's accepted Beaver path
 * ({@link G1AnimationRuntime.Evaluator#evaluateBeaverCodeDriven}, the G1 legacy-parity exception: the same
 * {@code Mth.cos} formulas in {@code GeoModel.setCustomAnimations}). A static rig without a hook (the Elevator, the
 * Vortex) is one key at bind. A reference entry whose model class has no descriptor (the held species, the head
 * sidecars, the solver rigs, the native Queen) is skipped and said so.</p>
 *
 * <p>THE FIXED INPUTS (the ruling's, pinned): {@code limbSwingAmount} 1.0; {@code limbSwing} advancing 1.0 per tick
 * from 0 - the classic renderer's own feed at full walking speed: vanilla {@code WalkAnimationState.update} does
 * {@code speed += (movementSpeed - speed) * multiplier; position += speed}, and both {@code LivingEntityRenderer}
 * and GeckoLib's {@code GeoReplacedEntityRenderer} hand {@code walkAnimation.position(partialTick)} as limbSwing
 * and {@code min(1, speed)} as limbSwingAmount, so at speed 1 the position advances exactly 1.0 per tick;
 * {@code ageInTicks} advancing 1.0 per tick from 0 ({@code getBob}: {@code tickCount + partialTick}, partialTick 0);
 * {@code netHeadYaw} 0 and {@code headPitch} 0 (looking ahead); every entity-state flag at its rest value - a
 * {@link ProbeSubject} built from an EMPTY state: {@code attacking} 0, {@code ri1} 0, {@code rock_type} 0,
 * {@code rf1} 0 - with its entity RNG seeded 0 ({@code RandomSource.create(0)}) ONCE per clip and evolving across
 * the keys, one pose call per key (a fan angle or a roll advances per call, as it advances per rendered frame
 * in-game); full health (no hook reads health below the maximum). Twenty samples per second: one pose per tick.</p>
 *
 * <p>THE STATES (item 32 (2)): {@code walk} is exactly the above; {@code idle} sets limbSwingAmount 0 and limbSwing 0
 * (the walk position at rest); then ONE STATE PER VALUE THE HOOK'S CODE BRANCHES ON - every pose-interface getter the
 * hook reads ({@link HookGetterReader}: the {@code inputs.subject(<X>Pose.class)} casts of the descriptor and of the
 * static helpers it delegates to, the interfaces' getters, the comparisons with literals - an {@code int} getter's
 * literal set plus the value that takes the other side, a {@code boolean} getter's {@code true}; a getter used only
 * arithmetically, a RenderInfo latch, an RNG or a movement delta is named as not enumerable), each value ALONE: every
 * other getter at rest, the idle inputs, the age advancing. {@code getAttacking} 1 is the contract's {@code attack}. A
 * value whose dense samples equal the idle state's on every bone and channel yields NO clip: the index records it under
 * the hook's {@code no_motion} (with the branch that gates the read where the read is not reached - the six resting
 * attacks of the 2026-09-14 landing), so nothing is silently missing; combinations of values are never sampled.
 * THE NAMES: the seed ({@code tools/artist_specs/<registry>.json}, {@code reference_states}: an object mapping a state
 * name in the animator's words to the getter value that produces it - {@code {"fly": {"getActivity": 1}}}) names a
 * value's state; the contract names are {@code walk}, {@code idle}, {@code attack} ({@code getAttacking} 1), {@code fly},
 * {@code swim}; a value the seed does not name is emitted as {@code reference_<getter>_<value>} (the getter's name without
 * its {@code get} / {@code is} prefix, lower snake case, then the value, {@code true} as {@code 1}) and flagged in the
 * index ({@code named} false), so the sheet can say so. A seed mapping that names a value the hook does not enumerate, a
 * contract name for the wrong value, two values under one name, or a combination of getters is refused.</p>
 *
 * <p>THE SPAN, per rig and per state (owner 2026-09-13, third set, addendum item 28 (5), replacing item 27 (3)'s
 * wording): a rig with a period structure - a manifest that declares {@code channels} (the effective frequency
 * {@code omega * wingspeed} per channel, the slowest distinct one's period {@code T = 2 pi / f}), or a hook rig without
 * declared channels whose {@link #RULES} / {@link #HOOK_RULES} row states its period with the source line it was read
 * from - spans the SMALLEST multiple {@code k * T} of its slowest group's period at which EVERY group returns within
 * 5 degrees of its start: the pose at {@code t = k * T} against the pose at {@code t = 0}, per bone and axis, the
 * authored rotation deltas' difference reduced mod 360 ({@link #wrapDegrees}, exactly as the loop seam is measured),
 * the maximum at most {@link #CLOSURE_TOLERANCE_DEGREES}; where the hook writes positions, each position channel must
 * return within {@link #CLOSURE_TOLERANCE_UNITS} model unit of its start (one sixteenth of a block, the model grid's
 * pixel). The first {@code k >= 1} with {@code k * T <= 120} ticks (the 6 s cap, {@link #SPAN_CAP_TICKS}) that passes
 * is the span ({@code period_multiple}, the index's {@code period_multiple_k}); a single-group rig passes at
 * {@code k = 1} (the seam 0 by construction). Past the cap - no such {@code k}, or {@code T} itself over 120 ticks -
 * the span is two seconds ({@code two_seconds_past_cap}, 40 ticks) and the sheet states the seam (the index's
 * {@code seam_delta_degrees}: the closing key's delta at 40 ticks). A static rig is one key ({@code one_key}); a rig
 * with no period (the Purple Power's fresh random rolls) is two seconds ({@code two_seconds_no_period}). The closure
 * test runs PER STATE on that state's inputs and its own fresh subject, and a state whose hook writes nothing that
 * MOVES - every bone's rotation and position identical at every sample - is one key ({@code one_key}, the note saying
 * which structure is not live at those inputs: a threshold gait at limbSwingAmount 0, for one). The DENSE SAMPLES sit
 * at every whole tick inside the span plus a CLOSING sample at the span's end (the pose sampled AT {@code k * T} -
 * Bedrock and GeckoLib hold the last key until {@code animation_length}, so without it the loop would hitch by up to a
 * tick); the index records the seam delta (closing sample against first, degrees, reduced mod 360) so a past-cap
 * window's seam and a sawtooth channel's wrap are visible.</p>
 *
 * <p>THE SAMPLES AND THE KEYS: a rotation sample per bone per tick, DELTAS from the bone's bind under the converter's
 * sign rule (authored X = +classic degrees, Y and Z negated; the rule {@code tools/keyframe_clip.py} and
 * {@link KeyframeLeg} document): the bake's internal rotation is classic {@code (-x, y, -z)}
 * ({@code OreSpawnGeoReplacement}'s basis facts), so the classic delta is {@code (-(Ix - Bx), Iy - By, -(Iz - Bz))}
 * and the authored value {@code (+dCx, -dCy, -dCz)} - equivalently {@code (-dIx, -dIy, +dIz)} in internal terms, which
 * is exactly what GeckoLib 4.8.4 undoes at load (X and Y rotation keys negated, Z kept) before adding the key to the
 * bone's initial snapshot. A position sample per bone per tick where the hook writes positions ({@code moveTo}):
 * GeckoLib reads position keys unnegated and sets them absolutely, and a fresh bake's offsets are 0, so the authored
 * value is the internal offset itself - {@code (-dx, -dy, +dz)} of the classic pivot move {@code (dx, dy, dz)}, the
 * numbers {@code moveTo} writes. Values rounded to 1e-10 (degrees / model units), never {@code -0.0}. THE WALK PIN
 * (item 32 (3)): the dense per-tick samples of every walk state, after this rounding, equal number for number the keys
 * of the walk clips checked in at {@code fc5e23c} (whose keys were the samples at every whole tick plus the closing
 * key); {@code --dump-samples <dir>} writes them as JSON ({@code <registry>_samples_<state>.json}, never shipped) for
 * that proof. THE KEYS (item 32 (6)) are chosen from the samples by the density search the exact transcriptions use
 * ({@link ReferenceClipKeying}: catmullrom, the fewest keys per bone and channel within 1 degree of rotation and 1/32
 * block of position of the samples, the closing key always kept) and written with {@code lerp_mode} catmullrom; the
 * index records per clip the key counts, the sample count, the tolerance and the measured maximum error. Two-space
 * JSON, LF, UTF-8; deterministic, so two runs compare byte for byte.</p>
 *
 * <p>Usage: {@code ReferenceClipSampler <output-dir> <manifest>... [--reference <reference-manifest> <geo-dir>]
 * [--specs <seed-dir>] [--dump-samples <dir>]} (build.gradle {@code referenceClips}, the writer) and
 * {@code ReferenceClipSampler --verify <checked-in-dir> <scratch-out-dir> <manifest>... [--reference <reference-manifest>
 * <geo-dir>] [--specs <seed-dir>]} (build.gradle {@code referenceClipsVerify}, a {@code check} dependency - owner
 * 2026-09-13, third set, addendum item 28 (6)): the clips are regenerated into the scratch directory and every file is
 * compared byte for byte with the checked-in directory; a difference, a file the sampler produced that is not checked
 * in, or a checked-in file the sampler did not produce prints one {@code REFERENCE CLIPS DRIFT: <files>} line and exits
 * 1 (the build fails, as proof drift does); success prints {@code REFERENCE CLIPS VERIFIED: N files}. The seeds are read
 * from {@code <repository>/tools/artist_specs} unless {@code --specs} points elsewhere. No python in the loop.</p>
 */
public final class ReferenceClipSampler {
    /** Every clip's Bedrock name is {@code reference_<state>} (item 32 (3)). */
    static final String CLIP_PREFIX = "reference_";
    static final String INDEX_FILE = "reference_clips.json";
    static final int SCHEMA_VERSION = 4;
    /** The state file names: {@code <registry>_reference_<state>.animation.json}. */
    static final String FILE_INFIX = "_reference_";
    static final String FILE_EXTENSION = ".animation.json";
    static final String SAMPLES_INFIX = "_samples_";
    static final String STATE_WALK = "walk";
    static final String STATE_IDLE = "idle";
    static final String STATE_ATTACK = "attack";
    /** The contract's names (item 32 (2)-(4)): the seed may map {@code fly} and {@code swim}; {@code attack} is {@code getAttacking} 1. */
    static final List<String> CONTRACT_STATES = List.of(STATE_WALK, STATE_IDLE, STATE_ATTACK, "fly", "swim");
    static final String ATTACKING_GETTER_NAME = "getAttacking";
    static final Pattern STATE_NAME = Pattern.compile("[a-z][a-z0-9_]*");
    static final String SPECS_DIR = "tools/artist_specs";
    static final String REFERENCE_STATES_KEY = "reference_states";
    static final double TICKS_PER_SECOND = 20.0D;
    static final double TWO_SECONDS_TICKS = 40.0D;
    /** The cap on a period multiple: 6 s (owner 2026-09-13, third set, item 28 (5)); past it, two seconds. */
    static final double SPAN_CAP_TICKS = 120.0D;
    /** Every group must return within this many degrees of its start at k * T (per bone and axis, reduced mod 360). */
    static final double CLOSURE_TOLERANCE_DEGREES = 5.0D;
    /** A position channel must return within one model unit (1/16 block, the model grid's pixel) of its start at k * T. */
    static final double CLOSURE_TOLERANCE_UNITS = 1.0D;
    static final String RULE_ONE_KEY = "one_key";
    static final String RULE_PERIODIC = "periodic";  // a period structure, resolved by the closure test to one of the next two
    static final String RULE_PERIOD_MULTIPLE = "period_multiple";
    static final String RULE_TWO_SECONDS_PAST_CAP = "two_seconds_past_cap";
    static final String RULE_TWO_SECONDS_NO_PERIOD = "two_seconds_no_period";
    static final String DRIFT_LINE = "REFERENCE CLIPS DRIFT: ";
    static final String VERIFIED_LINE = "REFERENCE CLIPS VERIFIED: ";
    static final double TWO_PI = 2.0D * Math.PI;
    /** The generator's rounding of a key (tools/keyframe_clip.py VALUE_DECIMALS; KeyframeLeg.VALUE_ROUNDING). */
    static final double VALUE_ROUNDING = 1.0e10D;
    static final double FREQUENCY_EPSILON = 1.0e-9D;
    static final float LIMB_SWING_AMOUNT = 1.0F;
    static final float NET_HEAD_YAW = 0.0F;
    static final float HEAD_PITCH = 0.0F;
    /** limbSwing advance per tick at limbSwingAmount 1 (WalkAnimationState: position += speed, speed 1.0). */
    static final double LIMB_SWING_PER_TICK = 1.0D;
    static final String INPUTS_STATEMENT = "limbSwingAmount 1.0; limbSwing = t (advancing 1.0 per tick from 0: vanilla "
            + "WalkAnimationState.update does speed += (movementSpeed - speed) * multiplier; position += speed, and the classic "
            + "renderer hands walkAnimation.position(partialTick) as limbSwing and min(1, speed) as limbSwingAmount, so at "
            + "speed 1 the position advances exactly 1.0 per tick); ageInTicks = t (getBob: tickCount + partialTick, "
            + "partialTick 0); netHeadYaw 0; headPitch 0 (looking ahead); every entity-state flag at its rest value "
            + "(attacking 0, ri1 0, rock_type 0, rf1 0: a ProbeSubject built from an empty state); the entity RNG seeded 0 "
            + "once per clip and evolving across the keys; one pose call per key, 20 keys per second; full health "
            + "(no shipped hook reads health)";
    static final String IDLE_INPUTS_STATEMENT = "limbSwingAmount 0.0; limbSwing 0 (the walk position at rest: vanilla "
            + "WalkAnimationState holds its position while the speed is 0, and a creature that has not walked since it spawned "
            + "carries 0); ageInTicks = t (getBob: tickCount + partialTick, partialTick 0; advancing 1.0 per tick from 0); "
            + "netHeadYaw 0; headPitch 0 (looking ahead); every entity-state flag at its rest value (attacking 0, ri1 0, "
            + "rock_type 0, rf1 0: a ProbeSubject built from an empty state); the entity RNG seeded 0 once per clip and evolving "
            + "across the keys; one pose call per key, 20 keys per second; full health";
    static final String CLIENT_PACKAGE = "danger.orespawn.entity.client.";
    static final String CLIENT_SOURCE_DIR = HookGetterReader.CLIENT_SOURCE_DIR;
    static final String SHIPPED_ASSETS = "src/main/resources/assets/orespawn";
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    /**
     * The branch that gates the attacking read of the six hooks whose attack state moved nothing at the 2026-09-14
     * landing (owner 2026-09-14, second set revised, item 32 (7): "re-examined under item 2, since a resting attack
     * usually means another flag gates it"), authored from the code: what the code tests before it reads
     * {@code getAttacking()}. Reported in the index and the sheet when {@code getAttacking} 1 alone moves nothing (the
     * computed result, never assumed); the combination of values is the owner's to sample.
     */
    static final Map<String, String> ATTACK_GATES = new TreeMap<>(Map.ofEntries(
            Map.entry("CaveFisherGeoReplacement", "getAttacking() is read only inside the claw-snap latch's re-roll, "
                    + "if (nextangle > 0 && newangle < 0) on cos(ageInTicks * 3.0 * WINGSPEED) with its 0.1-tick look-ahead "
                    + "(CaveFisherGeoReplacement.java:100-110): the read happens only when a rising zero crossing of that 1.86 rad/tick "
                    + "rhythm falls within 0.1 tick after a whole-tick sample, which no sampled tick does, so ri1 stays 0, the claws hold "
                    + "their rest and attacking alone moves nothing"),
            Map.entry("ScorpionGeoReplacement", "getAttacking() is read only inside the claw and tail latch's re-roll, "
                    + "if (nextangle > 0 && newangle < 0) on cos(ageInTicks * 3.0 * WINGSPEED) with its 0.1-tick look-ahead "
                    + "(ScorpionGeoReplacement.java:82-92): the read happens only when a rising zero crossing of that 1.86 rad/tick rhythm "
                    + "falls within 0.1 tick after a whole-tick sample, which no sampled tick does, so ri1 stays 0, the claws and tail hold "
                    + "their rest and attacking alone moves nothing"),
            Map.entry("Robot2GeoReplacement", "getAttacking() is read only inside the windmill latch's re-roll, if (nextangle > 0 && "
                    + "newangle < 0) on sin(toRadians(ageInTicks * 20)) with a 1.5-degree look-ahead (Robot2GeoReplacement.java:51-59): at "
                    + "a whole-tick sample the phase is a multiple of 20 degrees and Mth.sin's table answers exactly 0 at the crossing, never "
                    + "below it, so the read is never reached, ri1 stays 0 and attacking alone moves nothing"),
            Map.entry("Robot3GeoReplacement", "getAttacking() is read only at the arm swing's rising zero crossing, if (nextangle > 0 && "
                    + "armSwing < 0) on cos(ageInTicks) with a 0.3-tick look-ahead (Robot3GeoReplacement.java:56-60): no whole tick of the "
                    + "sampled span falls within 0.3 tick before a crossing (they sit at 4.71, 11.0, 17.3 ... ticks), so ri1 stays 0, the arms "
                    + "hold their bent rest and attacking alone moves nothing"),
            Map.entry("LeonGeoReplacement", "every getAttacking() read sits in the flying branch - if (entity.getActivity() == 0) { standing } "
                    + "else { flying: LeonGeoReplacement.java:255, :286, :465 } (the branch at :161) - so with activity 0 attacking alone moves "
                    + "nothing; the combination (getActivity 1 with getAttacking 1) is the owner's to sample"),
            Map.entry("LeonopteryxGeoReplacement", "every getAttacking() read sits in the flying branch of the Leon rig's helper - "
                    + "if (entity.getActivity() == 0) { standing } else { flying: LeonGeoReplacement.java:255, :286, :465 } (the branch at "
                    + ":161; LeonopteryxGeoReplacement.applyCustomAnimations:41 -> LeonGeoReplacement.poseRig) - so with activity 0 attacking "
                    + "alone moves nothing; the combination (getActivity 1 with getAttacking 1) is the owner's to sample")));

    /** The registry each manifest id packages under (the file name); an id without a row is refused. */
    static final Map<String, String> REGISTRIES = new TreeMap<>(Map.ofEntries(
            Map.entry("model_elevator", "elevator"),
            Map.entry("model_beaver", "beaver"),
            Map.entry("model_vortex", "vortex"),
            Map.entry("model_coin", "coin"),
            Map.entry("model_island", "island"),
            Map.entry("model_islandtoo", "island_too"),
            Map.entry("model_robot1", "robot_1"),
            Map.entry("model_robot2", "robot_2"),
            Map.entry("model_robot3", "robot_3"),
            Map.entry("model_robot4", "robot_4"),
            Map.entry("model_robot5", "robot_5"),
            Map.entry("model_rockbase", "rock_base"),
            Map.entry("model_rotator", "rotator"),
            Map.entry("model_purplepower", "purple_power"),
            Map.entry("model_tshirt", "tshirt"),
            Map.entry("model_mosquito", "mosquito"),
            Map.entry("model_cliffracer", "cliff_racer"),
            Map.entry("model_brutalfly", "brutalfly"),
            Map.entry("model_dragonfly", "dragonfly"),
            Map.entry("model_cockateil", "cockateil"),
            Map.entry("model_ruby_bird", "ruby_bird"),
            Map.entry("model_firefly", "firefly"),
            Map.entry("model_goldfish", "gold_fish"),
            Map.entry("model_ant", "ant"),
            Map.entry("model_rainbow_ant", "rainbow_ant"),
            Map.entry("model_red_ant", "red_ant"),
            Map.entry("model_termite", "termite"),
            Map.entry("model_unstable_ant", "unstable_ant"),
            // the third Tier-2 slice (2026-09-13): the fifteen hook rigs
            Map.entry("model_cloudshark", "cloud_shark"),
            Map.entry("model_bee", "bee"),
            Map.entry("model_fairy", "fairy"),
            Map.entry("model_gammametroid", "gamma_metroid"),
            Map.entry("model_irukandji", "irukandji"),
            Map.entry("model_skate", "skate"),
            Map.entry("model_rubberducky", "rubber_ducky"),
            Map.entry("model_terribleterror", "terrible_terror"),
            Map.entry("model_wormlarge", "worm_large"),
            Map.entry("model_wormmedium", "worm_medium"),
            Map.entry("model_wormsmall", "worm_small"),
            Map.entry("model_cannonfodder", "cannon_fodder"),
            Map.entry("model_caterkiller", "cater_killer"),
            Map.entry("model_cricket", "cricket"),
            Map.entry("model_herculesbeetle", "hercules_beetle"),
            // the fourth Tier-2 slice (T2d, 2026-09-14): the thirteen rigs landed on the hooks already written
            Map.entry("model_crab", "crab"),
            Map.entry("model_kyuubi", "kyuubi"),
            Map.entry("model_leafmonster", "leaf_monster"),
            Map.entry("model_alosaurus", "alosaurus"),
            Map.entry("model_attacksquid", "attack_squid"),
            Map.entry("model_bandp", "band_p"),
            Map.entry("model_baryonyx", "baryonyx"),
            Map.entry("model_camarasaurus", "camarasaurus"),
            Map.entry("model_cassowary", "cassowary"),
            Map.entry("model_creepinghorror", "creeping_horror"),
            Map.entry("model_cryolophosaurus", "cryolophosaurus"),
            Map.entry("model_easterbunny", "easter_bunny"),
            Map.entry("model_flounder", "flounder")));

    /**
     * An unlanded hook (owner 2026-09-14, addendum items 10 and 11): the descriptor's simple class name, the ModEntities
     * registry it packages under and the rig it names - the map {@code HOOKS} in {@code tools/asset_audit.py} carries
     * (descriptor -> rig; 67 entries at the hooks' landing, the landing slices removing theirs - T2d removed thirteen;
     * the two delegating descriptors, the Alien Boss's on the Alien's rig and the
     * Leonopteryx's on the Leon's, added here because they package under their own registries). The reference entry is
     * {@code reference_<rig>} in {@code tools/reference_model_proofs.json}, its geo
     * {@code <geo-dir>/reference_<rig>.geo.json}. A row whose descriptor is gone, whose reference entry is gone, or whose
     * rig has shipped (the descriptor's geo exists under src/main/resources: the slice landed and the manifest carries it)
     * is refused as stale, so the list empties as the slices land - the audit's {@code HOOK_STALE} rule, mirrored.
     */
    record Hook(String descriptor, String registry, String rig) {
        String referenceId() {
            return "reference_" + this.rig;
        }

        String candidateClass() {
            return CLIENT_PACKAGE + this.descriptor;
        }
    }

    static final Map<String, Hook> HOOK_DESCRIPTORS = hooks(
            new Hook("AlienGeoReplacement", "alien", "alien"),
            new Hook("AlienBossGeoReplacement", "alien_boss", "alien"),
            new Hook("BabyDragonGeoReplacement", "baby_dragon", "dragon"),
            new Hook("BasiliskGeoReplacement", "basilisk", "basilisk"),
            new Hook("ButterflyGeoReplacement", "butterfly", "butterfly"),
            new Hook("CaveFisherGeoReplacement", "cave_fisher", "cavefisher"),
            new Hook("CephadromeGeoReplacement", "cephadrome", "cephadrome"),
            new Hook("ChipmunkGeoReplacement", "chipmunk", "chipmunk"),
            new Hook("DragonGeoReplacement", "dragon", "dragon"),
            new Hook("DungeonBeastGeoReplacement", "dungeon_beast", "dungeonbeast"),
            new Hook("EmperorScorpionGeoReplacement", "emperor_scorpion", "emperorscorpion"),
            new Hook("EnderKnightGeoReplacement", "ender_knight", "enderknight"),
            new Hook("EnderReaperGeoReplacement", "ender_reaper", "enderreaper"),
            new Hook("FrogGeoReplacement", "frog", "frog"),
            new Hook("GazelleGeoReplacement", "gazelle", "gazelle"),
            new Hook("GhostGeoReplacement", "ghost", "ghost"),
            new Hook("GhostSkellyGeoReplacement", "ghost_skelly", "ghostskelly"),
            new Hook("GiantRobotGeoReplacement", "giant_robot", "giantrobot"),
            new Hook("GodzillaGeoReplacement", "godzilla", "godzilla"),
            new Hook("HammerheadGeoReplacement", "hammerhead", "hammerhead"),
            new Hook("HydroliscGeoReplacement", "hydrolisc", "hydrolisc"),
            new Hook("JefferyGeoReplacement", "jeffery", "giantrobot"),
            new Hook("KrakenGeoReplacement", "kraken", "kraken"),
            new Hook("LeonGeoReplacement", "leon", "leon"),
            new Hook("LeonopteryxGeoReplacement", "leonopteryx", "leon"),
            new Hook("LizardGeoReplacement", "lizard", "lizard"),
            new Hook("LunaMothGeoReplacement", "luna_moth", "butterfly"),
            new Hook("LurkingTerrorGeoReplacement", "lurking_terror", "lurkingterror"),
            new Hook("MantisGeoReplacement", "mantis", "mantis"),
            new Hook("MolenoidGeoReplacement", "molenoid", "molenoid"),
            new Hook("MothraGeoReplacement", "mothra", "butterfly"),
            new Hook("NastysaurusGeoReplacement", "nastysaurus", "nastysaurus"),
            new Hook("OstrichGeoReplacement", "ostrich", "ostrich"),
            new Hook("PeacockGeoReplacement", "peacock", "peacock"),
            new Hook("PitchBlackGeoReplacement", "pitch_black", "pitchblack"),
            new Hook("PointysaurusGeoReplacement", "pointysaurus", "pointysaurus"),
            new Hook("RatGeoReplacement", "rat", "rat"),
            new Hook("ScorpionGeoReplacement", "scorpion", "scorpion"),
            new Hook("SeaMonsterGeoReplacement", "sea_monster", "seamonster"),
            new Hook("SeaViperGeoReplacement", "sea_viper", "seaviper"),
            new Hook("SpitBugGeoReplacement", "spit_bug", "spitbug"),
            new Hook("SpyroGeoReplacement", "spyro", "spyro"),
            new Hook("StinkBugGeoReplacement", "stink_bug", "stinkbug"),
            new Hook("StinkyGeoReplacement", "stinky", "stinky"),
            new Hook("TRexGeoReplacement", "trex", "trex"),
            new Hook("TheKingGeoReplacement", "the_king", "theking"),
            new Hook("ThePrinceAdultGeoReplacement", "the_prince_adult", "theprinceadult"),
            new Hook("ThePrinceGeoReplacement", "the_prince", "theprince"),
            new Hook("ThePrinceTeenGeoReplacement", "the_prince_teen", "theprinceteen"),
            new Hook("TriffidGeoReplacement", "triffid", "triffid"),
            new Hook("TrooperBugGeoReplacement", "trooper_bug", "trooperbug"),
            new Hook("UrchinGeoReplacement", "urchin", "urchin"),
            new Hook("VampireButterflyGeoReplacement", "vampire_butterfly", "butterfly"),
            new Hook("VelocityRaptorGeoReplacement", "velocity_raptor", "velocityraptor"),
            new Hook("WaterDragonGeoReplacement", "water_dragon", "waterdragon"),
            new Hook("WhaleGeoReplacement", "whale", "whale"));

    private static Map<String, Hook> hooks(Hook... rows) {
        Map<String, Hook> out = new TreeMap<>();
        for (Hook row : rows) {
            if (out.put(row.descriptor(), row) != null) {
                throw new IllegalStateException("HOOK_DESCRIPTORS lists " + row.descriptor() + " twice");
            }
        }
        return out;
    }

    /**
     * One sampled state: its name (the file suffix and the clip name {@code reference_<name>}), the walk inputs, and - for
     * a value state - the getter raised and the value it answers (every other getter at rest), whether the seed named it.
     */
    record State(String name, float limbSwingAmount, double limbSwingPerTick, String getter, JsonPrimitive value, boolean named,
                 String nameSource) {
        static final State WALK = new State(STATE_WALK, LIMB_SWING_AMOUNT, LIMB_SWING_PER_TICK, null, null, true, "the contract");
        static final State IDLE = new State(STATE_IDLE, 0.0F, 0.0D, null, null, true, "the contract");

        /** A value state: the idle inputs with one getter raised (item 32 (2): each value alone). */
        static State ofValue(String name, String getter, JsonPrimitive value, boolean named, String nameSource) {
            return new State(name, 0.0F, 0.0D, getter, value, named, nameSource);
        }

        boolean isValueState() {
            return this.getter != null;
        }

        boolean isAttack() {
            return STATE_ATTACK.equals(this.name);
        }

        String clipName() {
            return CLIP_PREFIX + this.name;
        }

        String fileName(String registry) {
            return registry + FILE_INFIX + this.name + FILE_EXTENSION;
        }

        String samplesFileName(String registry) {
            return registry + SAMPLES_INFIX + this.name + ".json";
        }

        String valueToken() {
            return this.value == null ? "" : HookGetterReader.valueToken(this.value);
        }

        /** The inputs in words: the walk's, the idle's, or the idle's with the one getter raised. */
        String statement() {
            if (STATE_WALK.equals(this.name)) {
                return INPUTS_STATEMENT;
            }
            if (!isValueState()) {
                return IDLE_INPUTS_STATEMENT;
            }
            return this.getter + "() answers " + valueToken() + " (" + (this.named ? "the state the seed names `" + this.name + "`"
                    : "an unnamed state: the seed's reference_states should name it") + "; every other getter at its rest value); "
                    + IDLE_INPUTS_STATEMENT.replace("every entity-state flag at its rest value (attacking 0, ri1 0, rock_type 0, rf1 0: a "
                    + "ProbeSubject built from an empty state)", "every other entity-state flag at its rest value (ri1 0, rock_type 0, rf1 0)");
        }

        /** The subject's declared state: the rest state, with the one getter raised ({@code attacking} kept in step for {@code getAttacking}). */
        JsonObject subjectState() {
            JsonObject state = restState();
            state.addProperty("name", this.name);
            JsonObject getters = new JsonObject();
            if (isValueState()) {
                getters.add(this.getter, this.value);
                if (ATTACKING_GETTER_NAME.equals(this.getter)) {
                    state.addProperty("attacking", this.value.isBoolean() ? (this.value.getAsBoolean() ? 1 : 0) : this.value.getAsInt());
                }
            }
            state.add(ProbeSubject.GETTERS_KEY, getters);
            return state;
        }
    }

    /**
     * A rig's span rule. {@code kind}: {@link #RULE_ONE_KEY} (a static rig, or nothing to sample), {@link #RULE_PERIODIC}
     * (a period structure - {@code periodTicks} is the slowest group's period {@code T} - which {@link #resolveSpan}
     * settles into {@link #RULE_PERIOD_MULTIPLE} with its {@code k} and closure delta, or {@link #RULE_TWO_SECONDS_PAST_CAP}),
     * or {@link #RULE_TWO_SECONDS_NO_PERIOD}. The note names the source lines the rule was read from and, once resolved,
     * the closure test's result.
     */
    record Rule(String kind, double spanTicks, String note, double periodTicks, int k, double closureDegrees) {
        static Rule oneKey(String note) {
            return new Rule(RULE_ONE_KEY, 0.0D, note, 0.0D, 0, 0.0D);
        }

        /** A rig with a period structure: the slowest group's period, to be resolved by the closure test. */
        static Rule periodic(double periodTicks, String note) {
            return new Rule(RULE_PERIODIC, periodTicks, note, periodTicks, 0, 0.0D);
        }

        static Rule twoSecondsNoPeriod(String note) {
            return new Rule(RULE_TWO_SECONDS_NO_PERIOD, TWO_SECONDS_TICKS, note, 0.0D, 0, 0.0D);
        }

        Rule periodMultiple(int multiple, double closure, String closureNote) {
            return new Rule(RULE_PERIOD_MULTIPLE, multiple * periodTicks, note + "; " + closureNote, periodTicks, multiple, closure);
        }

        Rule pastCap(String capNote) {
            return new Rule(RULE_TWO_SECONDS_PAST_CAP, TWO_SECONDS_TICKS, note + "; " + capNote, periodTicks, 0, 0.0D);
        }
    }

    static final Map<String, Rule> RULES = new TreeMap<>(Map.ofEntries(
            Map.entry("model_coin", Rule.periodic(TWO_PI / (double) (0.05F * 0.22F),
                    "one channel: coin.yRot = cos(ageInTicks * 0.05F * 0.22F) * PI (CoinGeoReplacement.java:39, ModelCoin.java:49; "
                            + "orig ModelCoin.java:32, wingspeed 0.22): one natural period 2 pi / (0.05 x 0.22) = 571.2 ticks")),
            Map.entry("model_island", Rule.periodic(TWO_PI / (double) (0.05F * 1.0F),
                    "nine channels at 0.05..0.058 rad/tick x wingspeed 1.0 (IslandGeoReplacement.poseIslandRig:34-42, "
                            + "ModelIsland.java:63-71; orig ModelIsland.java:45-53): nine frequencies, the slowest 0.05 rad/tick")),
            Map.entry("model_islandtoo", Rule.periodic(TWO_PI / (double) (0.05F * 1.0F),
                    "the Island's nine channels (IslandTooGeoReplacement.java:27 -> IslandGeoReplacement.poseIslandRig; "
                            + "ModelIslandToo.java:33-41; orig ModelIsland.java:45-53): the slowest 0.05 rad/tick")),
            Map.entry("model_robot1", Rule.periodic(360.0D / 0.75D,
                    "two frequencies: the feet cos(limbSwing * 1.5F) x PI x 0.75 x limbSwingAmount (period 2 pi / 1.5 = 4.19 ticks "
                            + "at limbSwing +1 per tick; Robot1GeoReplacement.java:36-40, ModelRobot1.java:215-219; orig ModelRobot1.java:215-217 "
                            + "reads f2 = ageInTicks there) and the five keys toRadians(ageInTicks * 0.75) (one turn per 480 ticks; "
                            + "Robot1GeoReplacement.java:42-47, ModelRobot1.java:221-226; orig :218-222)")),
            Map.entry("model_robot5", Rule.periodic(TWO_PI / (double) 0.15F,
                    "one channel: the wheels |limbSwing * 0.15F mod 2 pi| (a sawtooth that wraps every 2 pi / 0.15 = 41.9 ticks at "
                            + "limbSwing +1 per tick; Robot5GeoReplacement.java:32-41, ModelRobot5.java:104-113; orig ModelRobot5.java:104-112 "
                            + "reads f2 = ageInTicks there); the turret yaw follows netHeadYaw / 2 = 0")),
            Map.entry("model_robot2", Rule.periodic(TWO_PI / (double) 0.3F,
                    "at rest one channel is live: the legs cos(ageInTicks * 0.3F) x PI x 0.12 x limbSwingAmount (Robot2GeoReplacement.java:40-46, "
                            + "ModelRobot2.java:141-147; orig ModelRobot2.java:133-137); the arms' windmill (rad(ageInTicks * 20)) needs a "
                            + "re-roll of ri1 while attacking (orig :139-170) and ri1 stays 0 at rest; the head follows netHeadYaw = 0")),
            Map.entry("model_robot3", Rule.periodic(TWO_PI / (double) 0.55F,
                    "at rest one channel is live: the legs cos(ageInTicks * 0.55F) x PI x 0.12 x limbSwingAmount (Robot3GeoReplacement.java:45-51, "
                            + "ModelRobot3.java:169-175; orig ModelRobot3.java:163-167); the arms' swing is latched off while ri1 is 0 "
                            + "(orig :169-186), holding their bent rest (-1.0 / +1.0 rad); the turret follows netHeadYaw / 2 = 0")),
            Map.entry("model_robot4", Rule.periodic(TWO_PI / (double) 0.5F,
                    "at rest one channel is live: the legs cos(ageInTicks * 0.5F) x PI x 0.15 x limbSwingAmount with the fixed calf / knee "
                            + "guard / thigh offsets (Robot4GeoReplacement.java:46-66, ModelRobot4.java:430-450; orig ModelRobot4.java:421-437); "
                            + "the shield pump and the cannon aim need attacking (orig :439-474) and rest at 0; the cannon assembly's "
                            + "pivot follows the upper arm (a constant position at rest; orig :475-500)")),
            Map.entry("model_rockbase", Rule.oneKey(
                    "no rotation or position channel: the pose is which cubes are visible for the rock type "
                            + "(RockBaseGeoReplacement.java:46-85, ModelRockBase.java:186-221; orig ModelRockBase.java:182-222), "
                            + "and visibility has no Bedrock animation channel; the SPEC's plain-language transcription carries it")),
            Map.entry("model_rotator", Rule.periodic(180.0D,
                    "the three fans turn by RenderInfo.rf1 degrees, advanced 2 degrees per pose call and wrapped to 0 past 359 "
                            + "(RotatorGeoReplacement.java:58-67, RotatorModel.java:85-90; orig ModelRotator.java:52-77): the call "
                            + "sequence 0, 2, ..., 358, 0 closes every 180 calls - one call per key, so 180 ticks (in-game the advance "
                            + "is per rendered frame, ENT-S-147)")),
            Map.entry("model_purplepower", Rule.twoSecondsNoPeriod(
                    "no period: the three fans take three fresh rolls of the level random per pose call (nextFloat() * 360 in X, Y, Z "
                            + "order; PurplePowerGeoReplacement.java:121-136, ModelPurplePower.java:187-194; orig ModelPurplePower.java:57 / "
                            + ":66 / :75) - two seconds from the seed-0 random")),
            // the third Tier-2 slice (2026-09-13): the fifteen hook rigs (owner's third set item 8 under Amendment 2) - a hook rig
            // declares no channels (the converter refuses them without a keyframe leg), so each states its slowest rhythm here
            // with the lines it was read from; the closure test (item 28 (5)) settles the multiple or the two-second window.
            Map.entry("model_cloudshark", Rule.periodic(TWO_PI / (double) (0.5F * 1.0F),
                    "three frequencies at wingspeed 1.0: leftfin.yRot = 1.15 + cos(ageInTicks * 0.7F * ws) * PI * 0.15, rightfin.yRot = -0.9 + "
                            + "cos(ageInTicks * 1.5F * ws) * PI * 0.15 and fins.yRot = cos(ageInTicks * 1.5F * ws) * PI * 0.25, jaw.xRot = 0.5 + "
                            + "cos(ageInTicks * 0.5F * ws) * PI * 0.1 (CloudSharkGeoReplacement.applyCustomAnimations, ModelCloudShark.setupAnim:83-89; "
                            + "orig ModelCloudShark.java:81-87): the slowest 0.5 rad/tick")),
            Map.entry("model_bee", Rule.periodic(TWO_PI / (double) (0.021F * 2.0F),
                    "at rest (attacking 0) the slowest channel is the abdomen curl cos(ageInTicks * 0.021F * ws) * PI * 0.023 at wingspeed 2.0 "
                            + "(BeeGeoReplacement.applyCustomAnimations, BeeModel.poseFrom; orig ModelBee.java:214): 0.042 rad/tick, a 149.6-tick period over "
                            + "the 6 s cap; the wings at 2.2 (orig :188-190), the pincers at 0.6 (:191-195), the antennae at 0.42 / 0.54 / 0.62 / 0.74 "
                            + "(:196-213); the abdomen chain follows the curl (position channels, orig :215-230)")),
            Map.entry("model_fairy", Rule.periodic(TWO_PI / (double) (1.5F * 0.1F),
                    "six frequencies at wingspeed 1.5 (ageInTicks * ws * k, two float multiplies left to right): the outer wings at 1.5 and the "
                            + "inner at 0.85 ws = 1.275, the arms' pitch at 0.15 / 0.12 ws = 0.225 / 0.18 and roll at 0.1 / 0.11 ws = 0.15 / 0.165 "
                            + "(FairyGeoReplacement.applyCustomAnimations, FairyModel.setupAnim; orig ModelFairy.java:134-137, 146-149): the slowest "
                            + "0.15 rad/tick; the head follows netHeadYaw / headPitch = 0 (orig :138-145)")),
            Map.entry("model_gammametroid", Rule.periodic(TWO_PI / (double) 0.4F,
                    "three channels: the three tusks cos(ageInTicks * 0.81F) * PI * 0.08 about X, shell1 cos(ageInTicks * 0.4F) * PI * 0.05 / 4 "
                            + "about X, the lower beak |cos(ageInTicks * 0.75F) * PI * 0.1| + 0.14 about X (GammaMetroidGeoReplacement.applyCustomAnimations, "
                            + "GammaMetroidModel.setupAnim; orig ModelGammaMetroid.java:175, 193-197, 204-206 for the lines the port keeps): the slowest 0.4 rad/tick")),
            Map.entry("model_irukandji", Rule.periodic(TWO_PI / (double) 0.2F,
                    "sixteen cosines over four tentacles - root pitch 0.55 / 0.65 / 0.5 / 0.57, root roll 0.35 / 0.45 / 0.3 / 0.37, tip pitch "
                            + "0.45 / 0.55 / 0.4 / 0.48, tip roll 0.25 / 0.35 / 0.2 / 0.29 rad/tick (IrukandjiGeoReplacement.tentacle, ModelIrukandji.setupAnim; "
                            + "orig ModelIrukandji.java:90-133): the slowest 0.2 rad/tick; the tips' pivots follow the roots (position channels)")),
            Map.entry("model_skate", Rule.periodic(TWO_PI / (double) 1.2F,
                    "at limbSwingAmount 1 (above the 0.1 threshold) one channel: Shape1.xRot = 0.785 + cos(ageInTicks * 1.2F) * PI * 0.15 * "
                            + "limbSwingAmount (SkateGeoReplacement.applyCustomAnimations, ModelSkate.setupAnim; orig ModelSkate.java:48-49); the idle "
                            + "branch's cos(ageInTicks * 0.4F) * PI * 0.05 needs limbSwingAmount <= 0.1")),
            Map.entry("model_rubberducky", Rule.periodic(Math.PI / (double) 1.0F,
                    "one channel: the wings |cos(ageInTicks * 1.0F) * PI * 0.15| about Z, mirrored (RubberDuckyGeoReplacement.applyCustomAnimations, "
                            + "RubberDuckyModel.setupAnim; orig ModelRubberDucky.java:91, 111-114 for the lines the port keeps): the absolute value folds "
                            + "the cosine, so the natural period is pi / 1.0 = 3.14 ticks; the head and beak follow netHeadYaw = 0")),
            Map.entry("model_terribleterror", Rule.periodic(Math.PI / (double) 0.3F,
                    "three frequencies: the wings cos(ageInTicks * 1.3F) * PI * 0.25 about Z around +-2.0, the jaw |cos(ageInTicks * 0.3F) * PI * 0.1| "
                            + "about X, four leg parts cos(ageInTicks * 1.25F) * PI * 0.35 about X around +-0.349 (TerribleTerrorGeoReplacement.applyCustomAnimations, "
                            + "TerribleTerrorModel.setupAnim; orig ModelTerribleTerror.java:171-183 for the lines the port keeps): the slowest is the jaw's "
                            + "|cos| at 0.3 rad/tick, which the absolute value folds to a period of pi / 0.3 = 10.47 ticks (the Rubber Ducky's row folds the "
                            + "same way; owner 2026-09-13, fourth set, item 4)")),
            Map.entry("model_wormlarge", Rule.periodic(TWO_PI / (double) 0.15F,
                    "six frequencies: the neck's pitch 0.25 and yaw 0.15, the head's pitch 0.35 and yaw 0.45, the teeth 0.57, the tail tip 0.63 rad/tick "
                            + "(WormLargeGeoReplacement.applyCustomAnimations, WormLargeModel.setupAnim; orig ModelWormLarge.java:185-274): the slowest 0.15; "
                            + "the five heads follow the neck and the eight teeth the head (position channels)")),
            Map.entry("model_wormmedium", Rule.periodic(TWO_PI / (double) 0.15F,
                    "five frequencies: the tail's pitch 0.45 and roll 0.25, the body's pitch 0.35 and roll 0.15, the head's pitch 0.55 and roll 0.25 rad/tick "
                            + "(WormMediumGeoReplacement.applyCustomAnimations, WormMediumModel.setupAnim; orig ModelWormMedium.java:79-125): the slowest 0.15; "
                            + "the body, the head pair and the four teeth follow the tail link by link (position channels)")),
            Map.entry("model_wormsmall", Rule.periodic(TWO_PI / (double) 0.25F,
                    "six frequencies: the tail's pitch 0.55 and roll 0.35, the body's pitch 0.45 and roll 0.25, the head's pitch 0.65 and roll 0.3 rad/tick "
                            + "(WormSmallGeoReplacement.applyCustomAnimations, WormSmallModel.setupAnim; orig ModelWormSmall.java:44-63): the slowest 0.25; "
                            + "the body and the head follow the tail link by link (position channels)")),
            Map.entry("model_cannonfodder", Rule.periodic(TWO_PI / (double) 0.6662F,
                    "one channel on the walk POSITION: the four legs cos(limbSwing * 0.6662F) * 1.2 * limbSwingAmount about X (CannonFodderGeoReplacement"
                            + ".applyCustomAnimations, CannonFodderModel.setupAnim: a port-authored rig, no 1.7.10 line): at limbSwing +1 per tick the period "
                            + "is 2 pi / 0.6662 = 9.43 ticks; the head follows netHeadYaw / headPitch = 0")),
            Map.entry("model_caterkiller", Rule.periodic(TWO_PI / (double) (0.3F * 0.22F),
                    "at rest (attacking 0) the slowest channel is the head bob cos(ageInTicks * 0.3F * ws) * 2 units at wingspeed 0.22 "
                            + "(CaterKillerGeoReplacement.applyCustomAnimations, CaterKillerModel.poseFrom; orig ModelCaterKiller.java:251): 0.066 rad/tick, "
                            + "95.2 ticks; the other rest rhythms 0.077 .. 0.506 rad/tick (orig :248-338); the head bob and the segments' z are position "
                            + "channels, the rear segments' z scaled by limbSwingAmount = 1 (orig :301-308)")),
            Map.entry("model_cricket", Rule.periodic(TWO_PI / (double) 1.0F,
                    "at limbSwingAmount 1 (above the 0.1 threshold) one channel: the four front / rear legs cos(ageInTicks * 1.0F) * PI * 0.25 * "
                            + "limbSwingAmount about Y (CricketGeoReplacement.applyCustomAnimations, CricketModel.setupAnim; orig ModelCricket.java:104-108, "
                            + "at wingspeed 2.5 there where the port's frequency is 1.0); the hind legs hold constants (orig :117-125)")),
            Map.entry("model_herculesbeetle", Rule.periodic(TWO_PI / (double) (0.051F * 1.0F),
                    "at rest (attacking 0) the slowest channel is the jaws cos(ageInTicks * 0.051F * ws) * PI * 0.01 at wingspeed 1.0 "
                            + "(HerculesBeetleGeoReplacement.applyCustomAnimations, HerculesBeetleModel.poseFrom; orig ModelHerculesBeetle.java:293): 123.2 ticks, "
                            + "over the 6 s cap; the eighteen leg parts cos(ageInTicks * ws * 0.45F) * PI * 0.12 * limbSwingAmount about Y (orig :286-292)")),
            // the fourth Tier-2 slice (T2d, 2026-09-14): the thirteen rigs landed on the hooks already written - each row is the
            // HOOK_RULES row the hook lanes wrote, carried verbatim (the same rule, the same source lines) and keyed by the manifest id
            Map.entry("model_crab", Rule.periodic(TWO_PI / (double) 0.13F,
                "the slowest rhythm is the resting claw-tip drift cos(ageInTicks * 0.13) * PI * 0.02 (CrabGeoReplacement.applyCustomAnimations:123, attacking 0) "
                        + "at 0.13 rad/tick, 48.33 ticks; the resting eyes and mouths at 0.35 / 0.25 / 0.3 / 0.45 / 0.15 (:105-119), the attacking ones at 0.45 / 0.35 / 0.4 / "
                        + "0.55 / 0.43 (:128-146); the eight leg poses -pi/2 +- cos(ageInTicks * 1.7) * PI * 0.15 * limbSwingAmount on the leg1 / leg2 / leg3 clones "
                        + "(:82-103, walk) at 1.7")),
            Map.entry("model_kyuubi", Rule.periodic(TWO_PI / (double) (0.5F * 0.5F),
                "wingspeed 0.5: the slowest rhythms are the arm sway cos(ageInTicks * 0.5 * WINGSPEED) * PI * 0.01 (KyuubiGeoReplacement.applyCustomAnimations:86, "
                        + "every state) and the nine-ring tail's pitch chain -0.26 .. 2.0 + cos(ageInTicks * 0.5 * WINGSPEED - n * pi/4) * PI * 0.1 (:184-216) at 0.25 rad/tick, "
                        + "25.13 ticks; the tail's yaw chain cos(ageInTicks * 0.9 * WINGSPEED - n * pi/4) * PI * 0.2 (:159-183) at 0.45; the two horn chains cos(ageInTicks * "
                        + "1.3 * WINGSPEED - n * pi/4) * PI * 0.1 (:110-156) at 0.65; the threshold gait cos(ageInTicks * 1.1 * WINGSPEED) * PI * 0.2 * limbSwingAmount (:70, "
                        + "walk; the lower legs and arms move by sin of the upper joint, :76-99) at 0.55; the head follows netHeadYaw = 0 (:101-102)")),
            Map.entry("model_leafmonster", Rule.periodic(TWO_PI / (double) 0.95F,
                "the threshold gait cos(ageInTicks * 0.95) * PI * 0.25 * limbSwingAmount (LeafMonsterGeoReplacement.applyCustomAnimations:64, walk in the "
                        + "attacking branch) at 0.95 rad/tick, 6.61 ticks - the slowest by period; the |cos| arms cos(ageInTicks * 0.7) * PI * 0.55 through Math.abs "
                        + "(:67-71, attacking) fold to a 4.49-tick half period; at rest the bush (attacking 0) holds still")),
            Map.entry("model_alosaurus", Rule.periodic(TWO_PI / (double) 0.1F,
                "the slowest rhythm is the forelimb sway cos(ageInTicks * 0.1) * PI * 0.05 around -0.523 on shape17 / shape11 "
                        + "(AlosaurusGeoReplacement.applyCustomAnimations:63-64, every state) at 0.1 rad/tick, 62.83 ticks; the threshold gait "
                        + "cos(ageInTicks * 1.3 * 0.22) * PI * 0.25 * limbSwingAmount (:50, walk) at 0.286; the attacking jaw 0.52 + cos(ageInTicks * 0.45) * PI * 0.18 (:61) at 0.45")),
            Map.entry("model_attacksquid", Rule.periodic(TWO_PI / (double) (0.25F * 1.0F),
                "wingspeed 1.0: ten rhythms on both branches - 0.25 and 0.39 (the body pair), 1.2, 1.1, 1.0, 1.9, 1.8, 1.7, 1.6, 1.5 (the eight "
                        + "tentacles) - at amplitude 0.04 / 0.4 x limbSwingAmount above the 0.1 threshold (AttackSquidGeoReplacement.applyCustomAnimations:64-73) "
                        + "and 0.01 / 0.1 below it (:75-84): the slowest 0.25 rad/tick, 25.13 ticks; the body yaw follows netHeadYaw * 0.75 = 0 (:96)")),
            Map.entry("model_bandp", Rule.periodic(TWO_PI / (double) (0.3F * 0.4F),
                "wingspeed 0.4: the slowest rhythm is the idle sway cos(ageInTicks * 0.3 * WINGSPEED) * PI * 0.02 (BandPGeoReplacement"
                        + ".applyCustomAnimations:67, below the 0.1 threshold) at 0.12 rad/tick, 52.36 ticks, beside cos(ageInTicks * 0.6 * WINGSPEED) * PI * 0.005 "
                        + "(:66); the threshold gait cos(ageInTicks * 1.3 * WINGSPEED) * PI * 0.25 * limbSwingAmount (:61) at 0.52 and its 2.6 harmonic (:62); the head "
                        + "follows netHeadYaw / headPitch = 0 (:75-76)")),
            Map.entry("model_baryonyx", Rule.periodic(TWO_PI / (double) (0.7F * 0.25F),
                "wingspeed 0.25: the slowest rhythm is the claw wave cos(ageInTicks * 0.7 * WINGSPEED) * PI * 0.25 (BaryonyxGeoReplacement"
                        + ".applyCustomAnimations:76, every state) at 0.175 rad/tick, 35.90 ticks; the threshold gait cos(ageInTicks * 1.3 * WINGSPEED) * PI * 0.15 "
                        + "* limbSwingAmount (:69, walk) at 0.325")),
            Map.entry("model_camarasaurus", Rule.periodic(TWO_PI / (double) (1.3F * 0.65F),
                "wingspeed 0.65: the slowest rhythm is the threshold gait on eight legs cos(ageInTicks * 1.3 * WINGSPEED) * PI * 0.25 * limbSwingAmount "
                        + "(CamarasaurusGeoReplacement.applyCustomAnimations:69, walk) at 0.845 rad/tick, 7.44 ticks; the health-frequency tail "
                        + "cos(ageInTicks * 1.5 * WINGSPEED * hf) * PI * 0.25 * hf with hf = health / max health = 1 (:80, every state) at 0.975; the neck and head "
                        + "look follows netHeadYaw = 0 (:102-123)")),
            Map.entry("model_cassowary", Rule.periodic(TWO_PI / (double) (1.3F * 0.55F),
                "wingspeed 0.55: the threshold gait cos(ageInTicks * 1.3 * WINGSPEED) * PI * 0.15 * limbSwingAmount (CassowaryGeoReplacement"
                        + ".applyCustomAnimations:62, walk) at 0.715 rad/tick, 8.79 ticks - the slowest - and its 2.6 harmonic (:63); the crest and beak follow the "
                        + "neck by (sin, cos) (:76-79); nothing moves below the threshold")),
            Map.entry("model_creepinghorror", Rule.periodic(TWO_PI / (double) 0.103F,
                "the slowest rhythm is the fourth spike's yaw cos(ageInTicks * 0.103) * PI * 0.08 (CreepingHorrorGeoReplacement.applyCustomAnimations:81, every "
                        + "state) at 0.103 rad/tick, 61.0 ticks, beside the fifth's 0.107 (:83) and the folded cos(ageInTicks * 0.11) * PI * 0.25 through Math.abs (:70-71, "
                        + "a 28.56-tick half period); the fifteen spike cosines 0.48 .. 1.61 (:65-94); the eight gait-scaled legs cos(ageInTicks * 1.25) * PI * 0.35 * "
                        + "limbSwingAmount (:56, walk)")),
            Map.entry("model_cryolophosaurus", Rule.periodic(TWO_PI / (double) 0.28F,
                "the slowest rhythm is the jaw -1.15 + cos(ageInTicks * 0.28) * PI * 0.1 (CryolophosaurusGeoReplacement.applyCustomAnimations:65, every state) "
                        + "at 0.28 rad/tick, 22.44 ticks; the threshold gait cos(ageInTicks * 1.3 * WINGSPEED) * PI * 0.25 * limbSwingAmount at wingspeed 0.75 (:56, walk) at 0.975")),
            Map.entry("model_easterbunny", Rule.periodic(TWO_PI / (double) (1.3F * 0.55F),
                "wingspeed 0.55: the threshold gait cos(ageInTicks * 1.3 * WINGSPEED) * PI * 0.1 * limbSwingAmount and its 2.6 harmonic (EasterBunnyGeoReplacement"
                        + ".applyCustomAnimations:56-57, walk), and the idle ear branch cos(ageInTicks * 1.3 * WINGSPEED) * PI * 0.01 (:60, below the threshold): the "
                        + "slowest 0.715 rad/tick, 8.79 ticks")),
            Map.entry("model_flounder", Rule.periodic(TWO_PI / (double) 0.7F,
                "the slowest rhythm is the idle tail sway cos(ageInTicks * 0.7) * PI * 0.05 (FlounderGeoReplacement.applyCustomAnimations:61, below the 0.1 "
                        + "threshold) at 0.7 rad/tick, 8.98 ticks; above it the fins cos(ageInTicks * 1.3) and cos(ageInTicks * 1.7) * PI * 0.25 * limbSwingAmount (:53-54) "
                        + "and the tail cos(ageInTicks * 1.2) * PI * 0.25 * limbSwingAmount (:61)"))));

    /**
     * The span rule of every unlanded hook (owner 2026-09-14, addendum item 31 (11) (3)): a hook declares no manifest
     * channels, so each row states its SLOWEST rhythm - the smallest effective frequency (the literal times the descriptor's
     * wingspeed, a |cos| fold halving the period) among the rhythms live in any sampled state (walk at limbSwingAmount 1,
     * idle at 0, attack where the hook reads attacking; a rhythm gated on state the probe never enters - a sitting read, a
     * ridden read, an activity, a singing frog - is named as such and left out) - with the lines of
     * {@code applyCustomAnimations} it was read from; the closure test settles the multiple, or the two-second window, per
     * state. Authored by this lane from the extracted rhythm lines of each hook (every {@code cos} / {@code sin} /
     * {@code toRadians} / {@code %} expression of the descriptor's pose code with its line number, the wingspeed constants
     * and the state gates), then read against the hook's code; nothing here is derived by the sampler itself. A delegating
     * descriptor (the Alien Boss, the Leonopteryx, the Baby Dragon, Jeffery) shares its rig's row; the Butterfly rig's four
     * consumers carry their own wingspeed and therefore their own period. The three hooks whose gait reads the entity's
     * movement delta (the Cephadrome, the Dragon, the Ostrich: {@code xOld() - getX()}, 0 on a probe that does not move)
     * show no gait at these inputs in any state; their rows say so.
     */
    static final Map<String, Rule> HOOK_RULES = hookRules();

    private static Map<String, Rule> hookRules() {
        Map<String, Rule> rules = new TreeMap<>();
        Rule alien = Rule.periodic(TWO_PI / (double) (1.0F * 0.22F),
                "wingspeed 0.22: the slowest rhythms are the jaw, tail and claw sways cos(ageInTicks * WINGSPEED) * PI * 0.05 / 0.02 / 0.03 "
                        + "(AlienGeoReplacement.poseRig:192, 199, 206, 212; the jaws fold it through Math.abs at :235-236, a half period) at 0.22 rad/tick, "
                        + "28.56 ticks; the gait cos(ageInTicks * 4.0 * WINGSPEED) * PI * 0.5 * limbSwingAmount (:78) at 0.88; the head fan "
                        + "cos(ageInTicks * fanspeed * WINGSPEED) with fanspeed 1.22 (:114-130) at 0.268 in the attacking branch; the leg latch at "
                        + "3.5 * 0.22 = 0.77 with its 0.2-tick look-ahead (:176-177) rolled on the entity RNG at a zero crossing");
        rules.put("AlienGeoReplacement", alien);
        rules.put("AlienBossGeoReplacement", alien);  // AlienBossGeoReplacement.applyCustomAnimations:35 -> AlienGeoReplacement.poseRig
        Rule dragon = Rule.periodic(TWO_PI / (double) (0.2F * 1.0F),
                "ANIM_SPEED 1.0: the slowest rhythm is the resting wing beat -0.85 + cos(ageInTicks * 0.2 * ANIM_SPEED) * PI * 0.028 (DragonGeoReplacement"
                        + ".poseDragon:151, activity 0 and not attacking) at 0.2 rad/tick, 31.42 ticks; the activity beat cos(ageInTicks * 0.75) * PI * 0.28 (:146, :150) and "
                        + "the attacking-by-activity -0.45 + cos(ageInTicks * 0.85) * PI * 0.2 (:147); the tail chain cos(ageInTicks * tailspeed) with tailspeed 0.76 "
                        + "at rest, 0.96 attacking, 0.22 sitting (:97-98, :198-208, :211-258); the attacking jaw cos(ageInTicks * 1.5) * PI * 0.14 (:309); the fourteen legs "
                        + "cos(ageInTicks * 1.25 * ANIM_SPEED) * PI * lspeed * 0.6 (:104) scale by the movement delta lspeed = |xOld - x, zOld - z| (:96-103), 0 on the "
                        + "probe, so the gait does not move at these inputs");
        rules.put("DragonGeoReplacement", dragon);
        rules.put("BabyDragonGeoReplacement", dragon);  // BabyDragonGeoReplacement.applyCustomAnimations:61 -> DragonGeoReplacement.poseDragon
        rules.put("BasiliskGeoReplacement", Rule.periodic(TWO_PI / (double) (1.3F * 0.3F),
                "wingspeed 0.3: the ten-ring serpentine cos(ageInTicks * 1.3 * WINGSPEED - n * pi/4) * PI * 0.1 * limbSwingAmount (BasiliskGeoReplacement"
                        + ".applyCustomAnimations:66-111, walk; the rings' pivots follow, position channels) at 0.39 rad/tick, 16.11 ticks - the slowest; the "
                        + "attacking jaw -1.0 + cos(ageInTicks * 0.45) * PI * 0.18 (:112) at 0.45"));
        rules.put("ButterflyGeoReplacement", Rule.periodic(TWO_PI / (double) (1.3F * 1.0F),
                "one rhythm at wingspeed 1.0: the eight mirrored wings cos(ageInTicks * 1.3 * wingspeed) * PI * 0.25 about Z (ButterflyGeoReplacement"
                        + ".pose:69; ButterflyGeoReplacement.applyCustomAnimations passes WINGSPEED 1.0) at 1.3 rad/tick, 4.83 ticks; nothing reads the walk or the entity"));
        rules.put("LunaMothGeoReplacement", Rule.periodic(TWO_PI / (double) (1.3F * 0.75F),
                "the Butterfly rig's one rhythm at the Luna Moth's wingspeed 0.75: cos(ageInTicks * 1.3 * 0.75) * PI * 0.25 on the eight wings "
                        + "(LunaMothGeoReplacement.applyCustomAnimations:58 -> ButterflyGeoReplacement.pose:69) at 0.975 rad/tick, 6.44 ticks"));
        rules.put("MothraGeoReplacement", Rule.periodic(TWO_PI / (double) (1.3F * 0.2F),
                "the Butterfly rig's one rhythm at Mothra's wingspeed 0.2: cos(ageInTicks * 1.3 * 0.2) * PI * 0.25 on the eight wings "
                        + "(MothraGeoReplacement.applyCustomAnimations:45 -> ButterflyGeoReplacement.pose:69) at 0.26 rad/tick, 24.17 ticks"));
        rules.put("VampireButterflyGeoReplacement", Rule.periodic(TWO_PI / (double) (1.3F * 1.0F),
                "the Butterfly rig's one rhythm at the Vampire Butterfly's wingspeed 1.0: cos(ageInTicks * 1.3 * 1.0) * PI * 0.25 on the eight wings "
                        + "(VampireButterflyGeoReplacement.applyCustomAnimations:40 -> ButterflyGeoReplacement.pose:69) at 1.3 rad/tick, 4.83 ticks"));
        rules.put("CaveFisherGeoReplacement", Rule.periodic(TWO_PI / (double) (2.0F * 0.62F),
                "wingspeed 0.62: the thirty-six gait-scaled legs in three phases cos(ageInTicks * 2.0 * WINGSPEED - n * pi/4) * PI * 0.12 * limbSwingAmount "
                        + "(CaveFisherGeoReplacement.applyCustomAnimations:59, 72, 85; walk) at 1.24 rad/tick, 5.07 ticks - the slowest; the claw snap "
                        + "cos(ageInTicks * 3.0 * WINGSPEED) * PI * 0.15 with its 0.1-tick look-ahead (:100-101) latched on RenderInfo and re-rolled on the entity RNG "
                        + "by the attacking flag (the Robot2 precedent), folded through Math.abs on the arm segments (:123-142)"));
        rules.put("CephadromeGeoReplacement", Rule.periodic(TWO_PI / (double) (0.2F * 0.55F),
                "wingspeed 0.55: the slowest rhythm is the resting wing beat -0.85 + cos(ageInTicks * 0.2 * WINGSPEED) * PI * ... (CephadromeGeoReplacement"
                        + ".applyCustomAnimations:112, activity 0 and not attacking) at 0.11 rad/tick, 57.12 ticks; the fins' sway cos(ageInTicks * 0.15 * WINGSPEED) "
                        + "* PI * 0.05 (:124) folds through Math.abs on the fins and membranes (:125-132) to a 38.08-tick period; the tail chain at tailspeed 0.76 "
                        + "(:72, :139-156; 0.22 in the ridden branch :135); the jaw cos(ageInTicks * 0.5 * WINGSPEED) * PI * 0.14 (:217); the legs "
                        + "cos(ageInTicks * 0.75 * WINGSPEED) * PI * lspeed * 0.4 (:80) scale by the movement delta lspeed = |xOld - x, zOld - z| (:70-79), 0 on the "
                        + "probe, so the gait does not move at these inputs"));
        rules.put("ChipmunkGeoReplacement", Rule.periodic(TWO_PI / (double) 0.25F,
                "the slowest rhythm is the tail 0.306 + cos(ageInTicks * 0.25) * PI * 0.06 (ChipmunkGeoReplacement.applyCustomAnimations:80, every state "
                        + "but sitting) at 0.25 rad/tick, 25.13 ticks; the threshold gait cos(ageInTicks * 2.3 * ANIM_SPEED) * PI * 0.25 * limbSwingAmount (:61) and "
                        + "cos(ageInTicks * 1.3 * ANIM_SPEED) * PI * 0.25 * limbSwingAmount (:81, ANIM_SPEED 1.0) at 2.3 and 1.3; the head look follows netHeadYaw = 0 (:68)"));
        rules.put("DungeonBeastGeoReplacement", Rule.periodic(TWO_PI / (double) (0.5F * 0.62F),
                "wingspeed 0.62: the slowest rhythm is the fourteen phased spine segments cos(ageInTicks * 0.5 * WINGSPEED + n * pi/4) * PI * 0.07 "
                        + "(DungeonBeastGeoReplacement.applyCustomAnimations:73-86, every state) at 0.31 rad/tick, 20.27 ticks; the ten gait-scaled legs "
                        + "cos(ageInTicks * 1.4 * WINGSPEED) * PI * 0.22 * limbSwingAmount (:60, walk) at 0.868; the tail cos(ageInTicks * 0.75 * WINGSPEED) * PI * 0.25 * "
                        + "tailamp with tailamp = limbSwingAmount at rest and 1.25 attacking (:87-88, :97-138); the jaw latch cos(ageInTicks * 2.0 * WINGSPEED) * PI * 0.15 "
                        + "with its 0.1-tick look-ahead (:143-144) on RenderInfo, re-rolled on the entity RNG"));
        rules.put("EmperorScorpionGeoReplacement", Rule.periodic(TWO_PI / (double) (0.5F * 0.22F),
                "wingspeed 0.22: the slowest rhythm is the resting mandibles cos(ageInTicks * 0.5 * WINGSPEED) * PI * 0.05 (EmperorScorpionGeoReplacement"
                        + ".applyCustomAnimations:100, attacking 0) at 0.11 rad/tick, 57.12 ticks; the attacking mandibles at 2.5 * 0.22 (:101); the four-phase legs "
                        + "cos(ageInTicks * 2.0 * WINGSPEED - n * pi/4) * PI * 0.12 * limbSwingAmount with the 0.1-tick look-ahead lift 0.47 * limbSwingAmount - |newangle| "
                        + "(:67-95, walk; position follows); the tail latch cos(ageInTicks * 3.0 * WINGSPEED) * PI * 0.15 (:107-108) on RenderInfo ri1 / ri2, rolled on the entity "
                        + "RNG at a zero crossing (the Robot2 precedent)"));
        rules.put("EnderKnightGeoReplacement", Rule.periodic(TWO_PI / (double) (0.7F * 0.21F),
                "wingspeed 0.21: the slowest rhythm is the cape cos(ageInTicks * 0.7 * WINGSPEED) * PI * 0.02 (EnderKnightGeoReplacement.applyCustomAnimations:79, "
                        + "every state) at 0.147 rad/tick, 42.74 ticks; the threshold gait on fourteen leg parts cos(ageInTicks * 1.3 * WINGSPEED) * PI * 0.25 * "
                        + "limbSwingAmount (:63, walk) at 0.273; the screaming branch cos(ageInTicks * 2.7 * WINGSPEED) * PI * 0.3 (:88, isScreaming false on the probe); "
                        + "the head look clamped from netHeadYaw = 0 (:80); the forearm and blade position writes follow the arms (:128-136)"));
        rules.put("EnderReaperGeoReplacement", Rule.periodic(TWO_PI / (double) (0.7F * 0.23F),
                "wingspeed 0.23: the slowest rhythm is cos(ageInTicks * 0.7 * WINGSPEED) * PI * 0.06 (EnderReaperGeoReplacement.applyCustomAnimations:74, every "
                        + "state) at 0.161 rad/tick, 39.02 ticks; the threshold gait cos(ageInTicks * 1.3 * WINGSPEED) * PI * 0.25 * limbSwingAmount (:57, walk) feeding "
                        + "the scythe 1.0 - |newangle| (:58); cos(ageInTicks * 1.9 * WINGSPEED) * PI * 0.25 (:63); the screaming branch at 2.7 * 0.23 (:70, isScreaming false on the probe)"));
        rules.put("FrogGeoReplacement", Rule.periodic(TWO_PI / (double) (1.4F * 1.0F),
                "wingspeed 1.0: the threshold gait cos(ageInTicks * WINGSPEED * 1.4) * PI * 0.55 * limbSwingAmount (FrogGeoReplacement.applyCustomAnimations:62, walk; "
                        + "the lower legs' position writes follow :87-92) at 1.4 rad/tick, 4.49 ticks - the only rhythm live at these inputs; the singing jaw "
                        + "cos(ageInTicks * 0.85 * WINGSPEED) * PI * 0.15 (:70) needs getSinging() != 0 and the jump branch a vertical velocity beyond 0.1, neither on the probe"));
        rules.put("GazelleGeoReplacement", Rule.periodic(TWO_PI / (double) 0.1F,
                "the slowest rhythm is the tail 1.0 + cos(ageInTicks * 0.1) * PI * 0.06 (GazelleGeoReplacement.applyCustomAnimations:99, every state) at 0.1 rad/tick, "
                        + "62.83 ticks; the neck sway cos(ageInTicks * 0.5) * PI * 0.02 (:84); the threshold gait on eighteen legs cos(ageInTicks * 1.1 * WINGSPEED) * PI * 0.12 "
                        + "* limbSwingAmount at wingspeed 0.65 (:62, walk) at 0.715; the head look on eleven parts follows netHeadYaw = 0 (:85); the crouch branch is off "
                        + "(isCrouching false on the probe)"));
        rules.put("GhostGeoReplacement", Rule.periodic(TWO_PI / (double) 0.3F,
                "four slow cosines on the two arms: cos(ageInTicks * 0.3 / 0.32 / 0.34 / 0.36) * PI * 0.05 about Z and X (GhostGeoReplacement.applyCustomAnimations:58-61, "
                        + "every state): the slowest 0.3 rad/tick, 20.94 ticks; nothing reads the walk or the entity"));
        rules.put("GhostSkellyGeoReplacement", Rule.periodic(TWO_PI / (double) 0.05F,
                "the slowest rhythm is the head swivel cos(ageInTicks * 0.05) * PI * 2 beside its sawtooth |ageInTicks * 0.05 mod 2 pi| that gates the RenderInfo "
                        + "latch rolled on the entity RNG (GhostSkellyGeoReplacement.applyCustomAnimations:83-85) at 0.05 rad/tick, 125.66 ticks - over the 6 s cap; the "
                        + "chains cos(ageInTicks * 0.2 / 0.22 / 0.24 / 0.26) * PI * 0.05 (:67-79)"));
        Rule giantRobot = Rule.periodic(TWO_PI / (double) 0.25F,
                "WING_SPEED 0.25: the hip sway and quarter turn cos / sin(-ageInTicks * WING_SPEED) * PI * 0.1 * movescale and the two-phase thigh and shin "
                        + "(GiantRobotGeoReplacement.poseRig:79-87; movescale = limbSwingAmount * 0.65 clamped to 1, :72-75, so 0 at idle) at 0.25 rad/tick, 25.13 ticks - "
                        + "the slowest; the bob cos(-ageInTicks * WING_SPEED * 2.0) * movescale (:91) and the attacking shoulder twist and windmill punch "
                        + "sin(ageInTicks * WING_SPEED * 2.0) (:112-117) at 0.5; the head look follows netHeadYaw = 0 (:135-136); the twenty-two instance bones posed by "
                        + "renderLeg / renderArm (:152-211)");
        rules.put("GiantRobotGeoReplacement", giantRobot);
        rules.put("JefferyGeoReplacement", giantRobot);  // JefferyGeoReplacement.applyCustomAnimations:36 -> GiantRobotGeoReplacement.poseRig
        rules.put("GodzillaGeoReplacement", Rule.periodic(TWO_PI / (double) (0.1F * 1.0F),
                "ANIM_SPEED 1.0: the slowest rhythm is the idle arm drift sin(ageInTicks * ANIM_SPEED * 0.1) * PI * 0.02 (GodzillaGeoReplacement"
                        + ".applyCustomAnimations:209, attacking 0) at 0.1 rad/tick, 62.83 ticks; the per-leg threshold gait cos / sin(ageInTicks * 0.75 * ANIM_SPEED "
                        + "+ n * pi/4) with the toe lift and sweep position writes (:97-99, :135-137, walk) at 0.75; the idle tail cos(ageInTicks * 0.75) * PI * 0.05 and the attacking "
                        + "tail fan cos(ageInTicks * 1.75) * PI * 0.2 (:182-183); the attacking jaw cos(ageInTicks * 1.5) * PI * 0.12 (:202) and arms sin(ageInTicks * 1.75) * PI * 0.16 (:208); "
                        + "the head look follows netHeadYaw * 0.55 = 0 (:187)"));
        hookRulesHtoP(rules);
        hookRulesRtoW(rules);
        return rules;
    }

    private static void hookRulesHtoP(Map<String, Rule> rules) {
        rules.put("HammerheadGeoReplacement", Rule.periodic(TWO_PI / (double) (0.3F * 0.33F),
                "wingspeed 0.33: the slowest rhythm is the armour sway cos(ageInTicks * 0.3 * WINGSPEED) * PI * 0.03 (HammerheadGeoReplacement"
                        + ".applyCustomAnimations:96, every state) at 0.099 rad/tick, 63.47 ticks; the (double) > 0.1 gait on twelve leg parts cos(ageInTicks * 1.3 * "
                        + "WINGSPEED) * PI * 0.1 * limbSwingAmount and its pi/4 phase (:62-63, walk) at 0.429; the attacking nod cos(ageInTicks * 1.3 * WINGSPEED) * PI * 0.13 "
                        + "(:99); the head look at 0.25 on sixteen parts follows netHeadYaw = 0 (:79)"));
        rules.put("HydroliscGeoReplacement", Rule.periodic(TWO_PI / (double) (0.75F * 0.65F),
                "wingspeed 0.65: the slowest rhythm is the health-frequency feather cos(ageInTicks * 0.75 * WINGSPEED * hf) * PI * 0.2 * hf with hf = 1 at full "
                        + "health (HydroliscGeoReplacement.applyCustomAnimations:106, every state) at 0.4875 rad/tick, 12.89 ticks; the other feather at 1.25 * 0.65 * hf (:105); "
                        + "the tail sway cos(ageInTicks * 1.0 * WINGSPEED) * PI * 0.15 with its follows (:86-101, stilled when sitting - false on the probe); the threshold "
                        + "gait over twenty-four parts cos(ageInTicks * 1.3 * WINGSPEED) * PI * 0.25 * limbSwingAmount (:61, walk) at 0.845"));
        rules.put("KrakenGeoReplacement", Rule.periodic(TWO_PI / (double) (0.087F * 1.0F),
                "ANIM_SPEED 1.0: the slowest rhythm is the fourth tentacle pair's pitch cos(ageInTicks * differ * ANIM_SPEED - n * pi/4) * PI * amp with differ "
                        + "0.087 (KrakenGeoReplacement.dangleTentacle:245-310; differ 0.1 / 0.101 / 0.097 / 0.093 / 0.087 and ydiffer 0.1 / 0.102 / 0.098 / 0.092 / 0.088 per "
                        + "tentacle, :215-229; 0.2 for the two rear ones, :234-235; 0.5 with amp 0.03 while attacking, :241-242) at 0.087 rad/tick, 72.22 ticks; the fins "
                        + "cos(ageInTicks * 0.43 / 0.32 * ANIM_SPEED) * PI * 0.15 / 0.14 (:68-69); the mouth latch cos(ageInTicks * 0.66) * PI * 0.15 with its 0.1-tick "
                        + "look-ahead (:93-94) re-rolled on the entity RNG at the zero crossing (the Robot2 precedent); the teeth twitch cos(ageInTicks * 0.5 * ANIM_SPEED) * PI * 0.015 (:109)"));
        Rule leon = Rule.periodic(TWO_PI / (double) (0.6F * 0.22F),
                "wingspeed 0.22: the slowest rhythm is the standing sway cos(ageInTicks * 0.6 * WINGSPEED) * PI * 0.02 (LeonGeoReplacement.poseRig:224, "
                        + "activity 0) at 0.132 rad/tick, 47.60 ticks; the standing threshold gait cos(ageInTicks * 1.8 * WINGSPEED) * PI * 0.25 * limbSwingAmount and "
                        + "cos(ageInTicks * 0.9 * WINGSPEED) * PI * 0.25 * limbSwingAmount (:143-144, walk; the feet follow, :170-179) with the idle 0.9 * 0.22 sway at "
                        + "amplitude 0.02 (:147); the flight beat cos(ageInTicks * 1.6 * WINGSPEED * spd) * PI * 0.06 / 0.26 * amp with spd 1.7 and amp 1.4 while attacking "
                        + "(:259, :344, :256-257), the flying legs at 3.6 * 0.22 (:316) and the flying jaw at 2.6 * 0.22 (:468) need activity != 0 (0 on the probe); the ridden "
                        + "yaw accumulator rf1 needs getBeingRidden() != 0; the head look follows netHeadYaw = 0 (:232-253)");
        rules.put("LeonGeoReplacement", leon);
        rules.put("LeonopteryxGeoReplacement", leon);  // LeonopteryxGeoReplacement.applyCustomAnimations:41 -> LeonGeoReplacement.poseRig
        rules.put("LizardGeoReplacement", Rule.periodic(TWO_PI / (double) (0.25F * 0.65F),
                "wingspeed 0.65: the slowest rhythm is the resting tail cos(ageInTicks * 0.25 * WINGSPEED) * PI * 0.05 (LizardGeoReplacement.applyCustomAnimations:118, "
                        + "attacking 0; five rings follow) at 0.1625 rad/tick, 38.67 ticks; the attacking tail cos(ageInTicks * 1.25 * WINGSPEED) * PI * 0.35 (:120); the "
                        + "attacking lower jaw 0.52 + cos(ageInTicks * 0.45) * 0.35 (:110); the threshold gait over twenty parts cos(ageInTicks * 1.0 * WINGSPEED) * PI * 0.25 * "
                        + "limbSwingAmount (:89, walk) at 0.65; the head look with the neck and jaw follows netHeadYaw = 0 (:145-163)"));
        rules.put("LurkingTerrorGeoReplacement", Rule.periodic(TWO_PI / (double) (0.1F * 1.0F),
                "wingspeed 1.0: the slowest rhythm is the thorax breath sin(ageInTicks * 0.1 * WINGSPEED) * PI * 0.06 with the abdomen following by position "
                        + "(LurkingTerrorGeoReplacement.applyCustomAnimations:198-201, every state) at 0.1 rad/tick, 62.83 ticks; the legs sin(ageInTicks * legspeed * WINGSPEED) "
                        + "with legspeed 0.7 (:69, :110-144) behind the phase-wrap latch |ageInTicks * legspeed mod 2 pi| rolled on the entity RNG (:73-74); the jaws |sin(ageInTicks * "
                        + "mouthspeed * WINGSPEED)| with mouthspeed 0.9 (:70, :97-98, :150-151, a folded half period) forced open while attacking; cos(ageInTicks * 1.4 * WINGSPEED) * PI * 0.2 (:202)"));
        rules.put("MantisGeoReplacement", Rule.periodic(TWO_PI / (double) (0.051F * 2.0F),
                "wingspeed 2.0: the slowest rhythm is cos(ageInTicks * 0.051 * WINGSPEED) * PI * 0.013 (MantisGeoReplacement.applyCustomAnimations:62, every state) "
                        + "at 0.102 rad/tick, 61.60 ticks; the wings cos(ageInTicks * 0.9 * WINGSPEED) * PI * 0.25 / 0.35 (:55-58) at 1.8; the attacking-branch forearms "
                        + "cos(ageInTicks * 0.51 * WINGSPEED) * PI * 0.25 with their follows (:65-93) at 1.02"));
        rules.put("MolenoidGeoReplacement", Rule.periodic(TWO_PI / (double) (0.1F * 0.5F),
                "wingspeed 0.5: the slowest rhythm is the nose stars cos(ageInTicks * 0.1 * WINGSPEED) * PI (MolenoidGeoReplacement.applyCustomAnimations:131, every "
                        + "state) at 0.05 rad/tick, 125.66 ticks - over the 6 s cap; the attacking arms cos(ageInTicks * 1.7 * WINGSPEED) * PI * 0.25 else the threshold "
                        + "arms cos(ageInTicks * 1.3 * WINGSPEED) * PI * 0.25 * limbSwingAmount (:53) and the threshold legs (:92) at 0.65, each with a three-link chain from "
                        + "the bind pivot (:57-121)"));
        rules.put("NastysaurusGeoReplacement", Rule.periodic(TWO_PI / (double) (0.26F * 0.65F),
                "wingspeed 0.65: the slowest rhythm is the resting tail chain cos(ageInTicks * tailspeed * WINGSPEED) * PI * tailamp with tailspeed 0.26 "
                        + "(NastysaurusGeoReplacement.applyCustomAnimations:233-243, attacking 0; 0.76 attacking, :230) at 0.169 rad/tick, 37.18 ticks; the threshold gait "
                        + "cos / sin(ageInTicks * WINGSPEED / pscale) with pscale 2.0 and the claw position writes (:71, :154-210, walk) at 0.325; the attacking jaw "
                        + "cos(ageInTicks * 0.85 * WINGSPEED) * PI * 0.16 (:118) else the RenderInfo chew latch gated by |ageInTicks * 0.7 * WINGSPEED mod 2 pi| and rolled "
                        + "from the level RNG (:121-131); the head look at 0.35 follows netHeadYaw = 0 (:86-87)"));
        rules.put("OstrichGeoReplacement", Rule.periodic(TWO_PI / (double) 0.05F,
                "the slowest rhythm is the tail -0.594 + cos(ageInTicks * 0.05) * PI * 0.06 (OstrichGeoReplacement.applyCustomAnimations:102, every state) at 0.05 "
                        + "rad/tick, 125.66 ticks - over the 6 s cap, beside the tail feathers at 0.061 and 0.072 (:106-107); the |cos| wings cos(ageInTicks * 1.0 * WINGSPEED) "
                        + "* PI * 0.15 with the 0.3-tick look-ahead (:151-152, :163; wingspeed 0.65) behind the ri1 latch rolled on the entity RNG at a zero crossing; the legs "
                        + "cos(ageInTicks * 1.25 * WINGSPEED) * PI * lspeed * 0.4 (:73) scale by the movement delta lspeed = |xOld - x, zOld - z| clamped to 0.75 (:70-72), 0 on "
                        + "the probe, so the gait does not move at these inputs; the ridden rf1 accumulation needs isVehicle() (false on the probe); the head look at 0.65 follows "
                        + "netHeadYaw = 0 (:145)"));
        rules.put("PeacockGeoReplacement", Rule.periodic(TWO_PI / (double) (1.3F * 0.75F),
                "wingspeed 0.75: one rhythm - the threshold legs cos(ageInTicks * 1.3 * WINGSPEED) * PI * 0.15 * limbSwingAmount (PeacockGeoReplacement"
                        + ".applyCustomAnimations:70, walk) at 0.975 rad/tick, 6.44 ticks; the display branch needs getBlink() > 0 (0 on the probe: the feathers stay folded); "
                        + "nothing moves below the threshold"));
        rules.put("PitchBlackGeoReplacement", Rule.periodic(TWO_PI / (double) (0.05F * 0.65F),
                "wingspeed 0.65, pscale 1.0 on the probe: the slowest rhythm is the resting wing sway -pi/4 + cos(ageInTicks * 0.05 * WINGSPEED / pscale) * PI * ... "
                        + "(PitchBlackGeoReplacement.applyCustomAnimations:86, activity 0) at 0.0325 rad/tick, 193.33 ticks - over the 6 s cap; the activity wings "
                        + "cos(ageInTicks * 0.45 * WINGSPEED / pscale) * PI * 0.24 (:86, activity != 0); the attacking jaw cos(ageInTicks * 0.85 * WINGSPEED) * PI * 0.16 (:179) else "
                        + "the RenderInfo chomp latch gated by |ageInTicks * 0.7 * WINGSPEED mod 2 pi| (:184-185); the walking legs cos / sin(ageInTicks * 0.75 * WINGSPEED / pscale) "
                        + "with the pscale-scaled claw writes (:222-285, walk) at 0.4875 and the flying legs cos(ageInTicks * 0.85 * WINGSPEED / pscale) * 0.2 while attacking (:305); "
                        + "the forked tail chain at tailspeed 0.76 / pscale walking and 0.26 / pscale at rest (:349-353, :355-416)"));
        rules.put("PointysaurusGeoReplacement", Rule.periodic(TWO_PI / (double) (0.02F * 1.0F),
                "wingspeed 1.0: the slowest rhythm is the tail pitch sway cos(ageInTicks * 0.02 * WINGSPEED) * PI * 0.15 (PointysaurusGeoReplacement"
                        + ".applyCustomAnimations:89, every state) at 0.02 rad/tick, 314.16 ticks - over the 6 s cap; the attacking tail yaw cos(ageInTicks * 1.3 * WINGSPEED) "
                        + "* PI * 0.25 else the resting cos(ageInTicks * 0.3 * WINGSPEED) * PI * 0.05 (:87); the threshold gait cos(ageInTicks * 1.3 * WINGSPEED) * PI * 0.25 * "
                        + "limbSwingAmount (:58, walk) at 1.3; the head look at 0.45 on twenty-two parts follows netHeadYaw / headPitch = 0 (:64-75)"));
    }

    private static void hookRulesRtoW(Map<String, Rule> rules) {
        rules.put("RatGeoReplacement", Rule.periodic(TWO_PI / (double) 0.4F,
                "the slowest rhythm is the resting tail cos(ageInTicks * 0.4) * PI * 0.05 (RatGeoReplacement.applyCustomAnimations:61, attacking 0; the tail tip "
                        + "follows, :66-67) at 0.4 rad/tick, 15.71 ticks; the attacking tail cos(ageInTicks * 1.5) * PI * 0.25 (:60); the threshold gait cos(ageInTicks * 1.7) "
                        + "* PI * 0.25 * limbSwingAmount (:53, walk) at 1.7"));
        rules.put("ScorpionGeoReplacement", Rule.periodic(TWO_PI / (double) (2.0F * 0.62F),
                "wingspeed 0.62: the pi/2-phased gait cos(ageInTicks * 2.0 * WINGSPEED - n * pi/4) * PI * 0.12 * limbSwingAmount (ScorpionGeoReplacement"
                        + ".applyCustomAnimations:68-77, walk) at 1.24 rad/tick, 5.07 ticks - the slowest; the claw and tail latch cos(ageInTicks * 3.0 * WINGSPEED) * PI * "
                        + "0.15 with its 0.1-tick look-ahead (:82-83) on RenderInfo ri1 / ri2, rolled on the entity RNG with attacking-picked ranges (the Robot2 precedent); "
                        + "the claw and tail chains follow by position (:117-159)"));
        rules.put("SeaMonsterGeoReplacement", Rule.periodic(TWO_PI / (double) (0.2F * 0.5F),
                "wingspeed 0.5: the slowest rhythm is the eye twitch cos(ageInTicks * 0.2 * WINGSPEED) * PI * 0.05 (SeaMonsterGeoReplacement.applyCustomAnimations:144, "
                        + "every state) at 0.1 rad/tick, 62.83 ticks; the tail fan cos(ageInTicks * 1.3 * WINGSPEED) * PI * 0.2 * limbSwingAmount (:58, walking or attacking; "
                        + "seven follows :62-88), the fins cos(ageInTicks * 1.2 * WINGSPEED) * PI * 0.2 * limbSwingAmount (:91) and the neck chain 0.455 * limbSwingAmount + "
                        + "cos(ageInTicks * 0.9 * WINGSPEED) * PI * 0.25 * limbSwingAmount (:100; five follows :104-130) - each with its own idle alternative below the threshold; "
                        + "the attacking jaw cos(ageInTicks * 1.7 * WINGSPEED) * PI * 0.17 (:141); the head look at 0.5 follows netHeadYaw = 0 (:135)"));
        rules.put("SeaViperGeoReplacement", Rule.periodic(TWO_PI / (double) (0.2F * 0.5F),
                "wingspeed 0.5: the slowest rhythm is the resting jaw cos(ageInTicks * 0.2 * WINGSPEED) * PI * 0.02 (SeaViperGeoReplacement.applyCustomAnimations:116, "
                        + "attacking 0) at 0.1 rad/tick, 62.83 ticks; the resting tongue cos(ageInTicks * 0.5 * WINGSPEED) * PI * 0.05 (:125); the attacking jaw "
                        + "cos(ageInTicks * 1.7 * WINGSPEED) * PI * 0.17, teeth at 4.7 * 0.5 and tongue at 1.5 * 0.5 (:104-113); the twenty-one-segment doseg chain "
                        + "cos(f2 * 1.3 * WINGSPEED - n * pi/4) * PI * 0.2 * f1 with the negative-swing clamp (:71, :172-173, walk) at 0.65; the head look at 0.5 with the "
                        + "jaw following follows netHeadYaw = 0 (:134-146)"));
        rules.put("SpitBugGeoReplacement", Rule.periodic(Math.PI / (double) (0.3F * 0.55F),
                "wingspeed 0.55: the slowest rhythm is the resting jaw |cos(ageInTicks * 0.3 * WINGSPEED) * PI * 0.015| (SpitBugGeoReplacement"
                        + ".applyCustomAnimations:80-81, attacking 0) at 0.165 rad/tick, which the absolute value folds to a 19.04-tick half period; the attacking jaw "
                        + "|cos(ageInTicks * 2.6 * WINGSPEED) * PI * 0.1| (:80); the Mth.sin gait sin(ageInTicks * 2.0 * WINGSPEED) * PI * 0.12 * limbSwingAmount with the "
                        + "0.1-tick look-ahead and the |cos| lift on the rising half-cycle (:64-76, walk; the four leg helpers over bind pivots and chains, :103-255) at 1.1"));
        rules.put("SpyroGeoReplacement", Rule.periodic(TWO_PI / (double) 1.2F,
                "the port's ws = limbSwingAmount (SpyroGeoReplacement.applyCustomAnimations:70): every rhythm scales by the walking speed, so nothing moves at "
                        + "limbSwingAmount 0; at 1 the slowest is the tail cos(ageInTicks * 1.2 * ws) * PI * 0.25 (:115; the tail chain follows, :122-123) at 1.2 rad/tick, "
                        + "5.24 ticks; the threshold gait cos(ageInTicks * 2.3 * ws) * PI * 0.4 * limbSwingAmount and cos(ageInTicks * 2.0 * ws) * PI * 0.25 * limbSwingAmount "
                        + "(:73, :80) at 2.3 and 2.0; the activity 3 / 2 branches and the sitting still are off on the probe; the head look on twelve parts follows "
                        + "netHeadYaw / headPitch = 0 (:132-156)"));
        rules.put("StinkBugGeoReplacement", Rule.periodic(TWO_PI / (double) (0.1F * 0.75F),
                "wingspeed 0.75: the slowest rhythm is the tail -0.2 + sin(ageInTicks * 0.1 * WINGSPEED) * PI * 0.1 (StinkBugGeoReplacement.applyCustomAnimations:67, "
                        + "every state) at 0.075 rad/tick, 83.78 ticks; the antennae sin(ageInTicks * 0.4 / 0.43 / 0.46 / 0.49 * WINGSPEED) * PI * 0.15 (:63-66), "
                        + "sin(ageInTicks * 0.4 * WINGSPEED) * PI * 0.2 (:58) and sin(ageInTicks * 0.2 * WINGSPEED) * PI * 0.04 (:61); the legs sin(ageInTicks * 3.1 * WINGSPEED) "
                        + "* PI * 0.3 * limbSwingAmount (:52, walk) at 2.325"));
        rules.put("StinkyGeoReplacement", Rule.periodic(TWO_PI / (double) 1.0F,
                "the port's ws = limbSwingAmount (StinkyGeoReplacement.applyCustomAnimations:63): every rhythm scales by the walking speed, so nothing moves at "
                        + "limbSwingAmount 0; at 1 the slowest is the tail cos(ageInTicks * 1.0 * ws) * PI * 0.2 (:85; the tail chain follows, :91-97) at 1.0 rad/tick, "
                        + "6.28 ticks; the threshold gait cos(ageInTicks * 2.3 * ws) * PI * 0.4 * limbSwingAmount and cos(ageInTicks * 2.0 * ws) * PI * 0.25 * limbSwingAmount "
                        + "(:65, :69); the activity-2 fold and the sitting still are off on the probe; the head look follows netHeadYaw / headPitch = 0 (:101-115)"));
        rules.put("TRexGeoReplacement", Rule.periodic(TWO_PI / (double) 0.1F,
                "the slowest rhythm is the arm sway -0.523 + cos(ageInTicks * 0.1) * PI * 0.05 on shape17 / shape11 (TRexGeoReplacement.applyCustomAnimations:75-76, "
                        + "every state) at 0.1 rad/tick, 62.83 ticks; the float-compare threshold gait on eight leg parts cos(ageInTicks * 1.3 * ANIM_SPEED) * PI * 0.25 * "
                        + "limbSwingAmount (:58, ANIM_SPEED 1.0, walk) at 1.3; the attacking jaw 0.52 + cos(ageInTicks * 0.45) * PI * 0.18 (:72)"));
        rules.put("TheKingGeoReplacement", Rule.periodic(TWO_PI / (double) (0.08F * 1.0F),
                "WING_SPEED 1.0: the slowest rhythm is the centre head's resting pitch sin(ageInTicks * 0.08 * WING_SPEED) * PI * 0.1 (TheKingGeoReplacement"
                        + ".applyCustomAnimations:298, attacking 0; the three heads' resting rhythms 0.17 / 0.13 / 0.45, 0.19 / 0.12 / 0.55, 0.13 / 0.08 / 0.65, :291-299) at "
                        + "0.08 rad/tick, 78.54 ticks; the attacking heads at 0.3 / 0.2 / 0.85, 0.32 / 0.21 / 0.95, 0.28 / 0.19 / 0.75 (:278-286); the wings cos(ageInTicks * 0.75 "
                        + "* WING_SPEED) * PI * 0.21 attacking else cos(ageInTicks * 0.35 * WING_SPEED) * PI * 0.15 (:83-84) with the 84 / 184 follows; the fourteen claws and legs on "
                        + "attacking (:142, :162); the eight-link tail chain at tailspeed 0.26 resting and 0.56 attacking (:212-217, :220-268)"));
        rules.put("ThePrinceAdultGeoReplacement", Rule.periodic(TWO_PI / (double) (0.13F * 0.65F),
                "wingspeed 0.65: the slowest rhythm is the centre head's resting yaw sin(ageInTicks * 0.13 * WINGSPEED) * PI * 0.08 (ThePrinceAdultGeoReplacement"
                        + ".applyCustomAnimations:308, attacking 0; the heads' resting rhythms 0.17 / 0.45, 0.19 / 0.55, 0.13 / 0.65, :302-310, their pitch getHeadNExt() - 30 in "
                        + "every state) at 0.0845 rad/tick, 74.35 ticks; the attacking heads at 0.3 / 0.85, 0.32 / 0.95, 0.28 / 0.75 (:285-293) and the sitting jaws at 0.25 / 0.35 "
                        + "/ 0.45 (:334-336); the wings by attacking / activity / sitting cos(ageInTicks * 0.75 / 0.35 * WINGSPEED) (:88-92) with the 84 / 184 follows; the gait "
                        + "cos(ageInTicks * 0.3 * WINGSPEED) * PI * 0.25 * limbSwingAmount while walking and not sitting (:176, walk) at 0.195; the tail chain at tailspeed 0.26 "
                        + "resting, 0.56 attacking, 0 sitting (:74-75, :226-231)"));
        rules.put("ThePrinceGeoReplacement", Rule.periodic(TWO_PI / (double) (0.3F * 0.65F),
                "wingspeed 0.65: the slowest rhythm is the resting wings cos(ageInTicks * 0.3 * WINGSPEED) * PI * 0.04 (ThePrinceGeoReplacement"
                        + ".applyCustomAnimations:86, below the threshold and not attacking) at 0.195 rad/tick, 32.22 ticks; the threshold-or-attacking wings cos(ageInTicks "
                        + "* 2.3 * WINGSPEED) * PI * 0.4 * limbSwingAmount (:85) and the threshold legs cos(ageInTicks * 2.0 * WINGSPEED) * PI * 0.25 * limbSwingAmount (:95, "
                        + "walk); the attacking lash cos(ageInTicks * 0.9 * WINGSPEED) * PI * 0.06 (:106) and the tail fan cos(ageInTicks * 1.3 * WINGSPEED) * PI * 0.12 (:111; "
                        + "four follows :116-132); the jaw chatter cos(ageInTicks * 1.9 / 2.1 / 2.3 * WINGSPEED) * PI * 0.2 (:191-195); the three heads' look and the necks by the "
                        + "head extensions (0 on the probe, :149-244)"));
        rules.put("ThePrinceTeenGeoReplacement", Rule.periodic(TWO_PI / (double) (0.25F * 0.65F),
                "wingspeed 0.65: the slowest rhythm is the resting jaw chatter cos(ageInTicks * 0.25 * WINGSPEED) * PI * 0.02 (ThePrinceTeenGeoReplacement"
                        + ".applyCustomAnimations:323, attacking 0; the other heads at 0.3 / 0.35, :325-327) at 0.1625 rad/tick, 38.67 ticks, beside the resting tail chain at "
                        + "tailspeed 0.26 (:81-82, :210-244; 0.56 attacking :203, 0 sitting :207); the wings cos(ageInTicks * 1.3 * WINGSPEED) * PI * 0.2 * limbSwingAmount walking, "
                        + "cos(ageInTicks * 0.3 * WINGSPEED) * PI * 0.04 resting (:90-91), 1.4 / 1.7 * 0.65 by activity and attacking (:93-96); the legs cos(ageInTicks * 0.55 * "
                        + "WINGSPEED) * PI * 0.25 * limbSwingAmount with its pi/2 phase (:136-137, walk) and cos(ageInTicks * WINGSPEED) * PI * 0.25 attacking (:149-150); the "
                        + "attacking jaws at 0.9 / 1.1 / 1.3 * 0.65 (:316-320); the flight yaw latch rf1 needs activity (0 on the probe); the three-head look follows the head "
                        + "extensions = 0 (:276-424)"));
        rules.put("TriffidGeoReplacement", Rule.periodic(Math.PI / (double) (0.25F * 1.0F),
                "wingspeed 1.0: the only rhythm live at these inputs is the attacking tentacle |cos(ageInTicks * 0.25 * WINGSPEED) * PI * 0.5| with its alternating roll "
                        + "(TriffidGeoReplacement.applyCustomAnimations:147-148, attacking; the l44 chain follows by position, :158-217) at 0.25 rad/tick, which the absolute "
                        + "value folds to a 12.57-tick half period; the four leaf chains cos(ageInTicks * 0.25 * WINGSPEED) * PI * 0.039 (:67) need getOpenClosed() != 0 - 0 on "
                        + "the probe, the closed constant 0.1225 rad - so the walk and idle states hold still"));
        rules.put("TrooperBugGeoReplacement", Rule.periodic(TWO_PI / (double) (0.1F * 0.22F),
                "wingspeed 0.22: the slowest rhythm is the resting cos(ageInTicks * 0.1 * WINGSPEED) * PI * 0.02 (TrooperBugGeoReplacement.applyCustomAnimations:89, "
                        + "attacking 0; the five attacking branches at 1.4 / 2.5 / 2.6 / 1.0 / 2.6 * 0.22 and the resting 0.4 / 0.5 / 0.3 / 0.1 / 0.3 * 0.22, :74-94) at 0.022 "
                        + "rad/tick, 285.60 ticks - over the 6 s cap; the Mth.sin gait sin(ageInTicks * 2.0 * WINGSPEED) * PI * 0.12 * limbSwingAmount with the 0.1-tick "
                        + "look-ahead and the |cos| lift (:109-121, walk; the four leg helpers :145-288) at 0.44"));
        rules.put("UrchinGeoReplacement", Rule.periodic(TWO_PI / (double) 0.02F,
                "wingspeed 1.0: the slowest rhythm is the slow centre spin (ageInTicks * 0.02) mod 2 pi with its eight spikes cos(ageInTicks * 0.07 / 0.065 / 0.075 / "
                        + "0.08 / 0.055 / 0.045 / 0.035 / 0.04 * WINGSPEED) * PI * 0.02 (UrchinGeoReplacement.applyCustomAnimations:94-102, one branch of the state switch) at "
                        + "0.02 rad/tick, 314.16 ticks - over the 6 s cap; the other branch spins (ageInTicks * 0.2) mod 2 pi with the spikes at 0.7 / 0.65 / 0.75 / 0.8 / 0.55 / 0.45 "
                        + "/ 0.35 / 0.4 * PI * 0.06 (:84-92); the threshold spikes cos(ageInTicks * 0.7 / 1.7 / 1.65 / 1.75 / 1.8 * WINGSPEED) * PI * 0.15 * limbSwingAmount (:63-67, walk)"));
        rules.put("VelocityRaptorGeoReplacement", Rule.periodic(TWO_PI / (double) 0.3F,
                "wingspeed 1.25: the slowest rhythm is cos(ageInTicks * 0.3) * PI * 0.05 (VelocityRaptorGeoReplacement.applyCustomAnimations:87, every state) at 0.3 "
                        + "rad/tick, 20.94 ticks; the health-frequency idiom cos(ageInTicks * 1.25 * WINGSPEED * hf) * PI * 0.1 * hf (:82) and cos(ageInTicks * 1.4 * WINGSPEED "
                        + "* hf) * PI * 0.25 * hf (:105, not sitting) with hf = 1 at full health; cos(ageInTicks * 1.3 * WINGSPEED) * PI * 0.1 (:98); the threshold gait "
                        + "cos(ageInTicks * 1.3 * WINGSPEED) * PI * 0.25 * limbSwingAmount (:72, walk) at 1.625"));
        rules.put("WaterDragonGeoReplacement", Rule.periodic(TWO_PI / (double) (0.5F * 0.5F),
                "wingspeed 0.5: the slowest rhythm is cos(ageInTicks * 0.5 * WINGSPEED) * PI * 0.05 (WaterDragonGeoReplacement.applyCustomAnimations:124, every state) "
                        + "at 0.25 rad/tick, 25.13 ticks, beside the 0.8 / 0.7 / 0.6 * 0.5 sways (:109-119); the threshold gait cos(ageInTicks * 1.3 * WINGSPEED) * PI * 0.2 * "
                        + "limbSwingAmount and the pi/4-phased body wave cos(ageInTicks * 1.3 * WINGSPEED - n * pi/4) * PI * 0.4 * limbSwingAmount with its chained position writes "
                        + "(:82-99, walk) at 0.65; the three-way attacking jaw cos(ageInTicks * 1.2 * WINGSPEED) * PI * 0.25 at attacking 1, 0.45 rad at 2, -0.25 at rest (:129); the "
                        + "head look at 0.75 with the nose, jaw, fin and ears following follows netHeadYaw = 0 (:130-142)"));
        rules.put("WhaleGeoReplacement", Rule.periodic(TWO_PI / (double) 0.03F,
                "the slowest rhythm is cos(ageInTicks * 0.03) * PI * 0.02 (WhaleGeoReplacement.applyCustomAnimations:72, every state) at 0.03 rad/tick, 209.44 ticks - "
                        + "over the 6 s cap; the two thresholds cos(ageInTicks * 0.3) * PI * 0.2 * limbSwingAmount else cos(ageInTicks * 0.08) * PI * 0.05 (:62-63) and "
                        + "cos(ageInTicks * 0.4) * PI * 0.16 * limbSwingAmount else cos(ageInTicks * 0.05) * PI * 0.03 (:76-77); the tail position chain follows (:88-93)"));
    }

    private ReferenceClipSampler() {
    }

    static final String USAGE = "Usage: ReferenceClipSampler <output-dir> <manifest>... [--reference <reference-manifest> <geo-dir>] "
            + "[--specs <seed-dir>] [--dump-samples <dir>] | --verify <checked-in-dir> <scratch-out-dir> <manifest>... "
            + "[--reference <reference-manifest> <geo-dir>] [--specs <seed-dir>]";

    /**
     * The command line past the mode arguments: the seam manifests; optionally the reference manifest with its geo
     * directory, the seed directory (default {@code <repository>/tools/artist_specs}) and a directory for the dense
     * samples (the walk pin's proof; never shipped).
     */
    record Options(List<Path> manifests, Path referenceManifest, Path referenceGeoDir, Path specsDir, Path samplesDir) {
        static Options parse(String[] args, int from) {
            List<Path> manifests = new ArrayList<>();
            Path referenceManifest = null;
            Path referenceGeoDir = null;
            Path specsDir = null;
            Path samplesDir = null;
            for (int i = from; i < args.length; i++) {
                if ("--reference".equals(args[i])) {
                    if (i + 2 >= args.length) {
                        throw new IllegalArgumentException("--reference takes <reference-manifest> <geo-dir>. " + USAGE);
                    }
                    referenceManifest = Path.of(args[i + 1]).toAbsolutePath().normalize();
                    referenceGeoDir = Path.of(args[i + 2]).toAbsolutePath().normalize();
                    i += 2;
                } else if ("--specs".equals(args[i])) {
                    if (i + 1 >= args.length) {
                        throw new IllegalArgumentException("--specs takes <seed-dir>. " + USAGE);
                    }
                    specsDir = Path.of(args[i + 1]).toAbsolutePath().normalize();
                    i += 1;
                } else if ("--dump-samples".equals(args[i])) {
                    if (i + 1 >= args.length) {
                        throw new IllegalArgumentException("--dump-samples takes <dir>. " + USAGE);
                    }
                    samplesDir = Path.of(args[i + 1]).toAbsolutePath().normalize();
                    i += 1;
                } else {
                    manifests.add(Path.of(args[i]));
                }
            }
            if (manifests.isEmpty()) {
                throw new IllegalArgumentException(USAGE);
            }
            return new Options(manifests, referenceManifest, referenceGeoDir, specsDir, samplesDir);
        }

        Path specs(Path repositoryRoot) {
            return this.specsDir != null ? this.specsDir : repositoryRoot.resolve(SPECS_DIR);
        }
    }

    public static void main(String[] args) throws Exception {
        if (args.length >= 1 && "--verify".equals(args[0])) {
            if (args.length < 4) {
                throw new IllegalArgumentException(USAGE);
            }
            int status = verify(Path.of(args[1]).toAbsolutePath().normalize(), Path.of(args[2]).toAbsolutePath().normalize(),
                    Options.parse(args, 3));
            if (status != 0) {
                System.exit(status);
            }
            return;
        }
        if (args.length < 2) {
            throw new IllegalArgumentException(USAGE);
        }
        write(Path.of(args[0]).toAbsolutePath().normalize(), Options.parse(args, 1));
    }

    /**
     * The verifier (build.gradle {@code referenceClipsVerify}; owner 2026-09-13, third set, item 28 (6)): clear
     * {@code scratch} of the sampler's earlier outputs (a clip or index left by a previous verify - after a renaming, a
     * stale file would read as "produced by the sampler"), regenerate into it, then compare every regular file of both
     * directories byte for byte. Returns 1 with one
     * {@code REFERENCE CLIPS DRIFT} line naming each file that differs, is produced but not checked in, or is checked in
     * but not produced; 0 with {@code REFERENCE CLIPS VERIFIED: N files}.
     */
    static int verify(Path checkedIn, Path scratch, Options options) throws Exception {
        if (Files.isDirectory(scratch)) {
            try (var listing = Files.list(scratch)) {
                for (Path stale : listing.filter(Files::isRegularFile).filter(ReferenceClipSampler::isSamplerOutput).toList()) {
                    Files.delete(stale);
                }
            }
        }
        write(scratch, options);
        TreeSet<String> names = new TreeSet<>();
        for (Path directory : List.of(checkedIn, scratch)) {
            if (Files.isDirectory(directory)) {
                try (var listing = Files.list(directory)) {
                    listing.filter(Files::isRegularFile).forEach(path -> names.add(path.getFileName().toString()));
                }
            }
        }
        List<String> drift = new ArrayList<>();
        for (String name : names) {
            Path expected = checkedIn.resolve(name);
            Path produced = scratch.resolve(name);
            if (!Files.isRegularFile(expected)) {
                drift.add(name + " (produced by the sampler, not checked in)");
            } else if (!Files.isRegularFile(produced)) {
                drift.add(name + " (checked in, not produced by the sampler)");
            } else if (!java.util.Arrays.equals(Files.readAllBytes(expected), Files.readAllBytes(produced))) {
                drift.add(name + " (differs)");
            }
        }
        if (!drift.isEmpty()) {
            System.out.println(DRIFT_LINE + String.join(", ", drift) + " - " + checkedIn + " against the sampler's output in " + scratch
                    + ": regenerate with gradle referenceClips and commit the result");
            return 1;
        }
        System.out.println(VERIFIED_LINE + names.size() + " files (" + checkedIn + " reproduced byte for byte in " + scratch + ")");
        return 0;
    }

    /** A file the sampler writes: a clip ({@code *.animation.json}) or the index. */
    private static boolean isSamplerOutput(Path path) {
        String name = path.getFileName().toString();
        return name.endsWith(".animation.json") || name.equals(INDEX_FILE);
    }

    /**
     * One rig to sample: a manifest entry (landed: the shipped geo, the manifest's channels or the {@link #RULES} row) or an
     * unlanded hook (the reference leg's geo, the {@link #HOOK_RULES} row, the bake lenient about the face-order key).
     */
    record Entry(String id, String registry, String ruleKey, Path manifestPath, Path repositoryRoot, JsonObject spec, String modelClass,
                 String candidateClass, boolean beaverPath, boolean isStatic, Path geoPath, boolean strictFaceOrder, String hook,
                 boolean landed, String rigSource) {
        String descriptorName() {
            return this.candidateClass == null ? null : this.candidateClass.substring(this.candidateClass.lastIndexOf('.') + 1);
        }
    }

    static final String RIG_SOURCE_SHIPPED = "shipped";
    static final String RIG_SOURCE_REFERENCE = "reference-leg converter output";

    /** What one registry's sampling produced: its clip rows and the hook facts (the getters read, the states, the values without motion). */
    record Sampled(List<JsonObject> rows, JsonObject hook) {
    }

    /** The writer: every manifest's rigs, then every unlanded hook of the reference manifest, sampled into {@code outputDir} plus the index. */
    static void write(Path outputDir, Options options) throws Exception {
        Files.createDirectories(outputDir);
        if (options.samplesDir() != null) {
            Files.createDirectories(options.samplesDir());
        }
        Map<String, Sampled> index = new TreeMap<>();
        Map<String, String> sampledClasses = new TreeMap<>();
        Map<String, String> landedClasses = new TreeMap<>();  // model class -> registry sampled from a manifest
        Path repositoryRoot = null;
        for (Path given : options.manifests()) {
            Path manifestPath = given.toAbsolutePath().normalize();
            JsonObject manifest = readJson(manifestPath);
            repositoryRoot = manifestPath.getParent().getParent();
            for (JsonElement element : manifest.getAsJsonArray("models")) {
                JsonObject spec = element.getAsJsonObject();
                String id = spec.get("id").getAsString();
                String registry = REGISTRIES.get(id);
                if (registry == null) {
                    throw new IllegalStateException(id + " (" + manifestPath.getFileName() + "): no registry row in "
                            + "ReferenceClipSampler.REGISTRIES - add the manifest id with its ModEntities registry name");
                }
                String modelClass = spec.get("class").getAsString();
                String previous = sampledClasses.get(registry);
                if (previous != null) {
                    if (!previous.equals(modelClass)) {
                        throw new IllegalStateException(id + ": a second manifest entry for registry " + registry
                                + " names class " + modelClass + " but the first named " + previous);
                    }
                    System.out.println("skip  " + registry + ": " + id + " in " + manifestPath.getFileName()
                            + " duplicates an entry already sampled (the same class " + modelClass + ")");
                    continue;
                }
                Entry entry = manifestEntry(spec, id, registry, manifestPath, repositoryRoot);
                sampledClasses.put(registry, modelClass);
                landedClasses.put(modelClass, registry);
                if (entry != null) {
                    index.put(registry, sample(entry, outputDir, options));
                }
            }
        }
        int hooksSampled = 0;
        List<String> skipped = new ArrayList<>();
        if (options.referenceManifest() != null) {
            Path referenceManifest = options.referenceManifest();
            JsonObject manifest = readJson(referenceManifest);
            Map<String, JsonObject> entries = new LinkedHashMap<>();
            for (JsonElement element : manifest.getAsJsonArray("models")) {
                JsonObject spec = element.getAsJsonObject();
                entries.put(spec.get("id").getAsString(), spec);
            }
            TreeSet<String> hooked = new TreeSet<>();
            for (Hook hook : HOOK_DESCRIPTORS.values()) {
                JsonObject spec = entries.get(hook.referenceId());
                if (spec == null) {
                    throw new IllegalStateException(hook.descriptor() + ": HOOK_DESCRIPTORS names " + hook.referenceId() + ", which "
                            + referenceManifest.getFileName() + " does not carry - the row is stale; fix the row or the manifest");
                }
                Path source = repositoryRoot.resolve(CLIENT_SOURCE_DIR).resolve(hook.descriptor() + ".java");
                if (!Files.isRegularFile(source)) {
                    throw new IllegalStateException(hook.descriptor() + ": HOOK_DESCRIPTORS names a descriptor with no source at " + source
                            + " - the row is stale; remove it");
                }
                GeoReplacementDescriptor<?> descriptor = S4CandidateRuntime.instantiate(hook.candidateClass()).descriptor();
                Path shipped = repositoryRoot.resolve(SHIPPED_ASSETS).resolve(descriptor.modelResource().getPath());
                if (Files.isRegularFile(shipped)) {
                    throw new IllegalStateException(hook.descriptor() + ": its rig " + descriptor.modelResource().getPath() + " ships - the slice "
                            + "landed, so the seam manifest carries it and the HOOK_DESCRIPTORS row is stale; remove the row (the audit's HOOK_STALE rule)");
                }
                if (sampledClasses.containsKey(hook.registry())) {
                    throw new IllegalStateException(hook.descriptor() + ": registry " + hook.registry() + " is already sampled from a seam manifest ("
                            + sampledClasses.get(hook.registry()) + ") - the HOOK_DESCRIPTORS row is stale; remove it");
                }
                Path geo = options.referenceGeoDir().resolve(hook.referenceId() + ".geo.json");
                if (!Files.isRegularFile(geo)) {
                    throw new IllegalStateException(hook.referenceId() + ": " + geo + " does not exist - run gradle referenceConvertModels "
                            + "(tools/layer_definition_to_geo.py over " + referenceManifest.getFileName() + ") or point --reference at its output directory");
                }
                String modelClass = spec.get("class").getAsString();
                Entry entry = new Entry(hook.referenceId(), hook.registry(), hook.descriptor(), referenceManifest, repositoryRoot, spec, modelClass,
                        hook.candidateClass(), false, false, geo, false,
                        hook.candidateClass() + ".applyCustomAnimations(AnimationProcessor, PoseInputs) through OreSpawnGeoReplacement.pose "
                                + "(the S4 doctrine: the unlanded hook of owner 2026-09-14, addendum item 10 - the classic setupAnim transcribed into the "
                                + "descriptor's PoseInputs form, proven when the rig lands - registry-free, on explicit PoseInputs, over the reference leg's "
                                + "converter output " + geo.getFileName() + " baked without the face-order strictness the landing slice's TEST-007 adds)",
                        false, RIG_SOURCE_REFERENCE);
                index.put(hook.registry(), sample(entry, outputDir, options));
                sampledClasses.put(hook.registry(), modelClass);
                hooked.add(hook.referenceId());
                hooksSampled++;
            }
            for (Map.Entry<String, JsonObject> reference : entries.entrySet()) {
                String id = reference.getKey();
                String modelClass = reference.getValue().get("class").getAsString();
                if (hooked.contains(id) || landedClasses.containsKey(modelClass)) {
                    continue;
                }
                skipped.add(id);
                System.out.println("skip  " + id + ": " + modelClass.substring(modelClass.lastIndexOf('.') + 1)
                        + " has no <Name>GeoReplacement descriptor in HOOK_DESCRIPTORS - no clip (a held species, a head sidecar, a solver rig or a "
                        + "native rig: sampled when the rig lands)");
            }
        }
        JsonObject root = new JsonObject();
        root.addProperty("schema_version", SCHEMA_VERSION);
        root.addProperty("generated_by", "danger.orespawn.g1.ReferenceClipSampler (build.gradle referenceClips; verified at check by referenceClipsVerify)");
        root.addProperty("purpose", "reference-only clips sampled from every hook, landed or not, one clip per state the hook's code reads that moves "
                + "the pose (owner 2026-09-13, second set, addendum item 27 (3); owner 2026-09-14, addendum item 31 (11); owner 2026-09-14, second set "
                + "revised, item 32 (2)-(6)): the seam manifests' rigs on their shipped geo and every unlanded hook (a reference entry whose model class "
                + "has a <Name>GeoReplacement descriptor) on the reference leg's converter output; never shipped, never returned under its own name - "
                + "the package checker and the asset audit refuse it; its keys may be the starting point of a delivered idle / walk / aggro_idle / fly / "
                + "swim or of an offered extra (item 32 (4))");
        JsonObject states = new JsonObject();
        JsonObject walk = new JsonObject();
        walk.addProperty("file", "<registry>" + FILE_INFIX + STATE_WALK + FILE_EXTENSION);
        walk.addProperty("clip_name", State.WALK.clipName());
        walk.addProperty("inputs", INPUTS_STATEMENT);
        walk.addProperty("when", "every sampled rig (the fixed inputs of 2026-09-13; the dense samples of every walk state equal number for number "
                + "the keys of the walk clips checked in at fc5e23c - item 32 (3), the pin at the sampled values)");
        states.add(STATE_WALK, walk);
        JsonObject idle = new JsonObject();
        idle.addProperty("file", "<registry>" + FILE_INFIX + STATE_IDLE + FILE_EXTENSION);
        idle.addProperty("clip_name", State.IDLE.clipName());
        idle.addProperty("inputs", IDLE_INPUTS_STATEMENT);
        idle.addProperty("when", "every sampled rig");
        states.add(STATE_IDLE, idle);
        JsonObject value = new JsonObject();
        value.addProperty("file", "<registry>" + FILE_INFIX + "<state>" + FILE_EXTENSION);
        value.addProperty("clip_name", CLIP_PREFIX + "<state>");
        value.addProperty("inputs", "the idle inputs with ONE pose-interface getter answering one value the hook's code branches on, every other "
                + "getter at its rest value (item 32 (2): each value alone; combinations are never sampled)");
        value.addProperty("when", "one clip per enumerated value that MOVES the pose (its dense samples differ from the idle state's on some bone or "
                + "channel); a value that moves nothing yields no clip and is listed under hooks.<registry>.no_motion; the state's name is the "
                + "contract's (attack = getAttacking 1; fly, swim as the seed maps them) or the seed's reference_states word, else "
                + "<getter>_<value> with named false");
        states.add("<state>", value);
        root.add("states", states);
        root.addProperty("naming_rule", "the seed tools/artist_specs/<registry>.json names a value's state under reference_states - an object "
                + "mapping a state name in the animator's words to the getter value that produces it, {\"fly\": {\"getActivity\": 1}} - from the "
                + "code's own words only; walk, idle and attack (getAttacking 1) are the contract's; a value the seed does not name is emitted as "
                + "reference_<getter>_<value> (the getter's name without its get / is prefix, lower snake case, then the value; true as 1) with "
                + "named false, and the sheet flags it (item 32 (2))");
        root.addProperty("getter_rule", "every pose-interface getter the hook reads (HookGetterReader: the inputs.subject(<X>Pose.class) casts of "
                + "the descriptor's applyCustomAnimations and of the static helpers it delegates to, followed into the interfaces' getters) and, per "
                + "getter, the values the code branches on: an int getter compared with literals yields the literal set plus the value that takes the "
                + "other side (!= 0 -> 1, > 0 -> 1, == 2 -> 2; the rest value 0 is the idle state, never a clip); a boolean getter yields true; a getter "
                + "used only arithmetically, a RenderInfo latch, an RNG or a movement delta is not enumerable and is named per hook with the reason");
        root.addProperty("samples_per_second", TICKS_PER_SECOND);
        root.addProperty("fixed_inputs", INPUTS_STATEMENT);
        root.addProperty("span_rule", "owner 2026-09-13, third set, addendum item 28 (5): a rig with a period structure spans the smallest "
                + "multiple k x T of its slowest group's period T at which every group returns within 5 degrees of its start (the pose at "
                + "t = k x T against the pose at t = 0, per bone and axis, the authored rotation deltas' difference reduced mod 360, the "
                + "maximum at most 5.0 degrees; a position channel within 1.0 model unit), capped at 6 s (120 ticks): the first k >= 1 with "
                + "k x T <= 120 that passes (rule period_multiple, period_multiple_k = k; a single-group rig passes at k = 1, its seam 0 by "
                + "construction). Past the cap - no such k, or T itself over 120 ticks - two seconds (rule two_seconds_past_cap, 40 ticks) "
                + "and the sheet states the seam (seam_delta_degrees: the closing sample's delta at 40 ticks). A static rig one key (one_key); "
                + "a rig with no period two seconds (two_seconds_no_period). Dense samples at every whole tick inside the span plus the closing "
                + "sample at its end. Per state (owner 2026-09-14, item 31 (11)): the closure test runs on each state's own inputs and subject, and a "
                + "state whose hook writes nothing that moves - every bone's rotation and position the same at every sample - is one key (one_key)");
        root.addProperty("keying_rule", ReferenceClipKeying.SEARCH_RULE + "; tolerance " + ReferenceClipKeying.ROTATION_TOLERANCE_DEGREES
                + " degree of rotation and 1/32 block (" + ReferenceClipKeying.POSITION_TOLERANCE_UNITS + " model units) of position; lerp_mode "
                + ReferenceClipKeying.LERP_MODE + " (owner 2026-09-14, second set revised, item 32 (6))");
        root.addProperty("rotation_rule", "a rotation sample per bone per tick as the DELTA from the bone's bind under the converter's "
                + "sign rule: authored X = +classic degrees, Y and Z negated (tools/keyframe_clip.py; KeyframeLeg); rounded to 1e-10 "
                + "degrees, never -0.0; the keys the density search keeps, lerp_mode catmullrom");
        root.addProperty("position_rule", "a position sample per bone per tick where the hook writes positions (OreSpawnGeoReplacement.moveTo): "
                + "the internal offset the hook wrote, (-dx, -dy, +dz) of the classic pivot move (dx, dy, dz) in model units - GeckoLib reads "
                + "position keys unnegated and sets them absolutely over a fresh bake's zero offsets; the keys the density search keeps");
        root.addProperty("hooks_sampled_from_reference_manifest", hooksSampled);
        root.add("reference_entries_skipped", names(skipped));
        JsonObject hooks = new JsonObject();
        JsonArray clips = new JsonArray();
        TreeMap<String, Integer> perState = new TreeMap<>();
        int files = 0;
        for (Map.Entry<String, Sampled> sampled : index.entrySet()) {
            hooks.add(sampled.getKey(), sampled.getValue().hook());
            for (JsonObject row : sampled.getValue().rows()) {
                clips.add(row);
                perState.merge(row.get("state").getAsString(), 1, Integer::sum);
                files++;
            }
        }
        root.add("hooks", hooks);
        root.add("clips", clips);
        writeJson(outputDir.resolve(INDEX_FILE), root);
        StringBuilder counts = new StringBuilder();
        perState.forEach((state, count) -> counts.append(counts.length() == 0 ? "" : ", ").append(state).append(' ').append(count));
        System.out.println("wrote " + outputDir.resolve(INDEX_FILE) + " (" + files + " clips over " + index.size() + " registries: " + counts + "; "
                + hooksSampled + " unlanded hooks from the reference manifest; " + skipped.size() + " reference entries skipped)");
    }

    /** One seam-manifest entry as an {@link Entry}, or {@code null} for a species without a classic hook (said so). */
    private static Entry manifestEntry(JsonObject spec, String id, String registry, Path manifestPath, Path repositoryRoot) throws Exception {
        String animationKind = spec.get("animation_kind").getAsString();
        String candidateClass = spec.has("candidate_class") ? spec.get("candidate_class").getAsString() : null;
        boolean beaverPath = candidateClass == null && "gait_scaled".equals(animationKind)
                && spec.has("keyframe_reference_leg")
                && "geckolib_custom_animation_code".equals(spec.has("candidate_animation_path")
                        ? spec.get("candidate_animation_path").getAsString() : "");
        boolean isStatic = "static".equals(animationKind);
        String hook;
        Path geoPath;
        boolean faceOrderRequired = false;
        if (candidateClass != null) {
            GeoReplacementDescriptor<?> descriptor = S4CandidateRuntime.instantiate(candidateClass).descriptor();
            geoPath = repositoryRoot.resolve(SHIPPED_ASSETS).resolve(descriptor.modelResource().getPath());
            faceOrderRequired = descriptor.cubeFaceOrderRequired();
            hook = candidateClass + ".applyCustomAnimations(AnimationProcessor, PoseInputs) through OreSpawnGeoReplacement.pose "
                    + "(the S4 doctrine: the shipped replacement, registry-free, on explicit PoseInputs)";
        } else if (beaverPath) {
            String legClass = spec.getAsJsonObject("keyframe_reference_leg").get("candidate_class").getAsString();
            GeoReplacementDescriptor<?> descriptor = S4CandidateRuntime.instantiate(legClass).descriptor();
            geoPath = repositoryRoot.resolve(SHIPPED_ASSETS).resolve(descriptor.modelResource().getPath());
            faceOrderRequired = descriptor.cubeFaceOrderRequired();
            hook = "G1AnimationRuntime.Evaluator.evaluateBeaverCodeDriven (the probe's accepted Beaver path - the G1 legacy-parity "
                    + "exception's GeoModel.setCustomAnimations with ModelBeaver's exact Mth.cos formulas): the shipped "
                    + legClass + " hook takes the renderer's AnimationState and has no PoseInputs form to pose registry-free";
        } else if (isStatic) {
            geoPath = repositoryRoot.resolve(SHIPPED_ASSETS + "/geo/entity")
                    .resolve(id.substring("model_".length()) + ".geo.json");
            hook = "none (a static rig: no classic hook; one key at bind)";
        } else {
            System.out.println("skip  " + registry + ": " + id + " declares no classic hook (animation_kind " + animationKind
                    + ", no candidate_class) - no sampled clip; its own clips are its reference");
            return null;
        }
        if (!Files.isRegularFile(geoPath)) {
            throw new IllegalStateException(id + ": the shipped geo " + geoPath + " does not exist");
        }
        return new Entry(id, registry, id, manifestPath, repositoryRoot, spec, spec.get("class").getAsString(), candidateClass, beaverPath, isStatic,
                geoPath, faceOrderRequired, hook, true, RIG_SOURCE_SHIPPED);
    }

    /** The seed's names for a registry's value states: {@code (getter=value) -> name} with the mapping's text, from {@code reference_states}. */
    record StateNames(Map<String, String> names, Map<String, String> sources) {
        static final StateNames NONE = new StateNames(Map.of(), Map.of());
    }

    static StateNames stateNames(String registry, Path specsDir) throws IOException {
        Path seed = specsDir.resolve(registry + ".json");
        if (!Files.isRegularFile(seed)) {
            return StateNames.NONE;
        }
        JsonObject spec = readJson(seed);
        if (!spec.has(REFERENCE_STATES_KEY)) {
            return StateNames.NONE;
        }
        if (!spec.get(REFERENCE_STATES_KEY).isJsonObject()) {
            throw new IllegalStateException(seed.getFileName() + ": " + REFERENCE_STATES_KEY + " must be an object mapping a state name to {getter: value}");
        }
        Map<String, String> names = new LinkedHashMap<>();
        Map<String, String> sources = new LinkedHashMap<>();
        for (Map.Entry<String, JsonElement> mapping : spec.getAsJsonObject(REFERENCE_STATES_KEY).entrySet()) {
            String name = mapping.getKey();
            if (!STATE_NAME.matcher(name).matches()) {
                throw new IllegalStateException(seed.getFileName() + ": " + REFERENCE_STATES_KEY + " name `" + name + "` is not lower snake case");
            }
            if (STATE_WALK.equals(name) || STATE_IDLE.equals(name)) {
                throw new IllegalStateException(seed.getFileName() + ": " + REFERENCE_STATES_KEY + " may not rename the contract's `" + name + "`");
            }
            if (!mapping.getValue().isJsonObject() || mapping.getValue().getAsJsonObject().size() != 1) {
                throw new IllegalStateException(seed.getFileName() + ": " + REFERENCE_STATES_KEY + "." + name + " must map ONE getter to one value "
                        + "(the ruling samples each value alone; a combination is the owner's)");
            }
            Map.Entry<String, JsonElement> getter = mapping.getValue().getAsJsonObject().entrySet().iterator().next();
            if (!getter.getValue().isJsonPrimitive()
                    || !(getter.getValue().getAsJsonPrimitive().isNumber() || getter.getValue().getAsJsonPrimitive().isBoolean())) {
                throw new IllegalStateException(seed.getFileName() + ": " + REFERENCE_STATES_KEY + "." + name + "." + getter.getKey()
                        + " must be an int or a boolean");
            }
            String key = getter.getKey() + "=" + HookGetterReader.valueToken(getter.getValue().getAsJsonPrimitive());
            if (STATE_ATTACK.equals(name) && !key.equals(ATTACKING_GETTER_NAME + "=1")) {
                throw new IllegalStateException(seed.getFileName() + ": `attack` is the contract's name for " + ATTACKING_GETTER_NAME + " 1, not " + key);
            }
            if (names.containsKey(key)) {
                throw new IllegalStateException(seed.getFileName() + ": " + REFERENCE_STATES_KEY + " names " + key + " twice (`" + names.get(key)
                        + "` and `" + name + "`)");
            }
            if (names.containsValue(name)) {
                throw new IllegalStateException(seed.getFileName() + ": " + REFERENCE_STATES_KEY + " uses the name `" + name + "` twice");
            }
            names.put(key, name);
            sources.put(key, seed.getFileName() + " " + REFERENCE_STATES_KEY + "." + name + " = {" + getter.getKey() + ": " + getter.getValue() + "}");
        }
        return new StateNames(names, sources);
    }

    /** The dense samples of one state: the closure test's rule, the ticks, every bone's rotation and position per tick, the subject after. */
    record Dense(State state, Rule rule, List<Double> ticks, Map<String, List<double[]>> rotation, Map<String, List<double[]>> position,
                 TreeSet<String> hidden, ProbeSubject subject) {
        /** Whether another state's samples equal these on every bone and channel (the "no motion" test of item 32 (2)). */
        boolean sameMotionAs(Dense other) {
            if (this.ticks.size() != other.ticks.size()) {
                return false;
            }
            for (int i = 0; i < this.ticks.size(); i++) {
                if (this.ticks.get(i).doubleValue() != other.ticks.get(i).doubleValue()) {
                    return false;
                }
            }
            return sameValues(this.rotation, other.rotation) && sameValues(this.position, other.position);
        }

        private static boolean sameValues(Map<String, List<double[]>> a, Map<String, List<double[]>> b) {
            if (!a.keySet().equals(b.keySet())) {
                return false;
            }
            for (Map.Entry<String, List<double[]>> bone : a.entrySet()) {
                List<double[]> mine = bone.getValue();
                List<double[]> theirs = b.get(bone.getKey());
                if (mine.size() != theirs.size()) {
                    return false;
                }
                for (int i = 0; i < mine.size(); i++) {
                    if (!java.util.Arrays.equals(mine.get(i), theirs.get(i))) {
                        return false;
                    }
                }
            }
            return true;
        }
    }

    /** One rig: walk, idle, then one state per enumerated value; the clips of the states that move; the hook facts. */
    private static Sampled sample(Entry entry, Path outputDir, Options options) throws Exception {
        Rule base = ruleFor(entry);
        Model rawModel = KeyFramesAdapter.GEO_GSON.fromJson(Files.readString(entry.geoPath(), StandardCharsets.UTF_8), Model.class);
        JsonObject geoJson = readJson(entry.geoPath());
        List<String> drawOrder = DrawOrder.read(geoJson);
        Map<String, List<List<Direction>>> faceOrder = FaceOrder.read(geoJson);
        G1AnimationRuntime.Evaluator evaluator = G1AnimationRuntime.evaluator(rawModel, drawOrder, faceOrder, entry.strictFaceOrder());
        Map<String, float[]> bindRotations = new TreeMap<>();
        Map<String, float[]> bindPositions = new TreeMap<>();
        G1AnimationRuntime.EvaluatedModel bind = evaluator.bindPose();
        bind.bones().forEach((name, bone) -> {
            bindRotations.put(name, new float[]{bone.getRotX(), bone.getRotY(), bone.getRotZ()});
            bindPositions.put(name, new float[]{bone.getPosX(), bone.getPosY(), bone.getPosZ()});
        });
        HookGetterReader.Declared declared = entry.candidateClass() != null
                ? HookGetterReader.read(entry.descriptorName(), entry.repositoryRoot()) : HookGetterReader.Declared.NONE;
        StateNames names = stateNames(entry.registry(), options.specs(entry.repositoryRoot()));

        // The states: walk, idle, then one per enumerated value (getAttacking 1 first, as the contract's attack, then the rest in
        // interface / getter / value order), named by the seed or emitted unnamed.
        List<State> states = new ArrayList<>(List.of(State.WALK, State.IDLE));
        TreeSet<String> enumerated = new TreeSet<>();
        List<State> valueStates = new ArrayList<>();
        for (HookGetterReader.GetterRead read : declared.enumerable()) {
            for (JsonPrimitive value : read.values()) {
                String key = read.getter() + "=" + HookGetterReader.valueToken(value);
                if (!enumerated.add(key)) {
                    continue;  // the same getter through two interfaces (a delegating helper's cast): one state
                }
                State state;
                if (read.isAttacking() && key.endsWith("=1")) {
                    state = State.ofValue(STATE_ATTACK, read.getter(), value, true, "the contract (" + read.getter() + " 1)");
                } else if (names.names().containsKey(key)) {
                    state = State.ofValue(names.names().get(key), read.getter(), value, true, names.sources().get(key));
                } else {
                    state = State.ofValue(HookGetterReader.bareName(read.getter()) + "_" + HookGetterReader.valueToken(value), read.getter(), value,
                            false, "unnamed: reference_<getter>_<value> (the seed's reference_states should name it)");
                }
                if (state.isAttack()) {
                    valueStates.add(0, state);
                } else {
                    valueStates.add(state);
                }
            }
        }
        for (String key : names.names().keySet()) {
            if (!enumerated.contains(key)) {
                throw new IllegalStateException(entry.registry() + ": the seed's " + REFERENCE_STATES_KEY + " names " + key + " as `" + names.names().get(key)
                        + "`, but the hook " + (entry.descriptorName() == null ? "(none)" : entry.descriptorName()) + " enumerates no such value ("
                        + enumerated + ") - a stale mapping; fix the seed");
            }
        }
        TreeSet<String> stateNames = new TreeSet<>();
        for (State state : valueStates) {
            if (!stateNames.add(state.name()) || STATE_WALK.equals(state.name()) || STATE_IDLE.equals(state.name())) {
                throw new IllegalStateException(entry.registry() + ": two states would share the name `" + state.name() + "`");
            }
        }
        states.addAll(valueStates);

        Poser poser = (state, t, subject) -> {
            if (entry.candidateClass() != null) {
                return S4CandidateRuntime.evaluateProductionHook(rawModel, drawOrder, faceOrder, entry.candidateClass(),
                        new S4CandidateRuntime.Inputs((float) t, (float) (t * state.limbSwingPerTick()), state.limbSwingAmount(),
                                NET_HEAD_YAW, HEAD_PITCH), subject, entry.strictFaceOrder());
            }
            if (entry.beaverPath()) {
                return evaluator.evaluateBeaverCodeDriven(t, state.limbSwingAmount());
            }
            return evaluator.bindPose();
        };
        List<JsonObject> rows = new ArrayList<>();
        JsonArray noMotion = new JsonArray();
        JsonArray emitted = new JsonArray();
        JsonArray unnamed = new JsonArray();
        Dense idle = null;
        for (State state : states) {
            Dense dense = sampleDense(entry, state, base, poser, bindRotations, bindPositions);
            if (options.samplesDir() != null) {
                writeJson(options.samplesDir().resolve(state.samplesFileName(entry.registry())), samplesJson(entry, dense));
            }
            if (STATE_IDLE.equals(state.name())) {
                idle = dense;
            }
            if (state.isValueState() && dense.sameMotionAs(idle)) {
                JsonObject skipped = new JsonObject();
                skipped.addProperty("getter", state.getter());
                skipped.add("value", state.value());
                skipped.addProperty("state", state.name());
                skipped.addProperty("named", state.named());
                boolean read = dense.subject().readGetters().contains(state.getter());
                skipped.addProperty("read_at_inputs", read);
                String gate = ATTACK_GATES.get(entry.descriptorName());
                String why = state.isAttack() && gate != null ? gate
                        : read ? state.getter() + " " + state.valueToken() + ": no motion at rest - the read is reached but the value changes no bone's "
                                + "rotation or position at the idle inputs (a visibility or texture choice, or a branch whose bones the rest pose already holds)"
                        : state.getter() + " " + state.valueToken() + ": no motion at rest - the read is not reached at the idle inputs (it sits in a "
                                + "branch another value or a latch gates), so the value alone moves nothing";
                skipped.addProperty("why", why);
                skipped.addProperty("note", state.getter() + " " + state.valueToken() + ": no motion at rest");
                noMotion.add(skipped);
                System.out.println(String.format(Locale.ROOT, "none  %s %s: %s %s moves nothing at the idle inputs (read at inputs: %s) - no clip",
                        entry.registry(), state.name(), state.getter(), state.valueToken(), read));
                continue;
            }
            rows.add(emit(entry, state, dense, declared, outputDir));
            emitted.add(state.name());
            if (!state.named()) {
                unnamed.add(state.name());
            }
        }
        JsonObject hook = new JsonObject();
        hook.addProperty("registry", entry.registry());
        hook.addProperty("descriptor", entry.descriptorName());
        hook.addProperty("landed", entry.landed());
        hook.add("declared", declared.json());
        JsonArray notEnumerable = new JsonArray();
        for (HookGetterReader.GetterRead read : declared.getters()) {
            if (!read.enumerable()) {
                JsonObject row = new JsonObject();
                row.addProperty("getter", read.qualified());
                row.addProperty("type", read.type());
                row.addProperty("reason", read.reason());
                notEnumerable.add(row);
            }
        }
        hook.add("not_enumerable", notEnumerable);
        hook.add("states", emitted);
        hook.add("unnamed_states", unnamed);
        hook.add("no_motion", noMotion);
        int gettersRead = declared.getters().size();
        int valuesEnumerated = enumerated.size();
        hook.addProperty("getters_read", gettersRead);
        hook.addProperty("values_enumerated", valuesEnumerated);
        hook.addProperty("getters_not_enumerable", notEnumerable.size());
        return new Sampled(rows, hook);
    }

    /** One state of one rig densely sampled: the closure test on its inputs, then every whole tick plus the closing sample. */
    private static Dense sampleDense(Entry entry, State state, Rule base, Poser poser, Map<String, float[]> bindRotations,
                                     Map<String, float[]> bindPositions) throws Exception {
        String id = entry.id();
        // The span rule (owner 2026-09-13, third set, item 28 (5)): the closure test settles a period structure into the
        // smallest multiple of the slowest period that closes within 5 degrees, or two seconds past the 6 s cap - per state.
        Rule rule = resolveSpan(base, id, poser, bindRotations, bindPositions, state);

        // The sample times: every whole tick inside the span, then the closing sample at the span's end.
        List<Double> ticks = new ArrayList<>();
        if (RULE_ONE_KEY.equals(rule.kind())) {
            ticks.add(0.0D);
        } else {
            int whole = (int) Math.ceil(rule.spanTicks() - 1.0e-9D);
            for (int k = 0; k < whole; k++) {
                ticks.add((double) k);
            }
            ticks.add(rule.spanTicks());
        }

        ProbeSubject subject = new ProbeSubject(state.subjectState());  // every getter at the state's value; the RNG seeded 0, once
        Map<String, List<double[]>> rotation = new TreeMap<>();   // bone -> authored (x, y, z) degrees per sample
        Map<String, List<double[]>> position = new TreeMap<>();   // bone -> authored (x, y, z) units per sample
        TreeSet<String> hiddenBones = new TreeSet<>();
        for (double t : ticks) {
            Sample sample = authoredKeys(id, poser.pose(state, t, subject), bindRotations, bindPositions);
            sample.rotation().forEach((name, key) -> rotation.computeIfAbsent(name, k -> new ArrayList<>()).add(key));
            sample.position().forEach((name, key) -> position.computeIfAbsent(name, k -> new ArrayList<>()).add(key));
            hiddenBones.addAll(sample.hidden());
        }
        // Item 31 (11): a state whose hook writes nothing that MOVES - every bone's rotation and position the same at every
        // sample - is one key; the note keeps the structure the closure test settled and says it is not live at these inputs.
        if (ticks.size() > 1 && !varies(rotation) && !varies(position)) {
            rotation.replaceAll((name, keys) -> new ArrayList<>(keys.subList(0, 1)));
            position.replaceAll((name, keys) -> new ArrayList<>(keys.subList(0, 1)));
            ticks = new ArrayList<>(List.of(0.0D));
            rule = Rule.oneKey("nothing moves at the " + state.name() + " inputs: every bone's rotation and position is the same at every sample of "
                    + "the span the closure test settled (" + rule.kind() + "; " + rule.note() + "), so this state is one key");
        }
        return new Dense(state, rule, ticks, rotation, position, hiddenBones, subject);
    }

    /** The dense samples as JSON ({@code --dump-samples}): the ticks, their time keys, every bone's rotation and the positioned bones' positions. */
    private static JsonObject samplesJson(Entry entry, Dense dense) {
        JsonObject out = new JsonObject();
        out.addProperty("registry", entry.registry());
        out.addProperty("state", dense.state().name());
        out.addProperty("clip_name", dense.state().clipName());
        out.addProperty("rule", dense.rule().kind());
        out.addProperty("span_ticks", RULE_ONE_KEY.equals(dense.rule().kind()) ? 0.0D : round(dense.rule().spanTicks()));
        JsonArray ticks = new JsonArray();
        JsonArray timeKeys = new JsonArray();
        for (double tick : dense.ticks()) {
            ticks.add(tick);
            timeKeys.add(timeKey(tick));
        }
        out.add("ticks", ticks);
        out.add("time_keys", timeKeys);
        List<String> positionBones = positionBones(dense.position());
        JsonObject bones = new JsonObject();
        for (Map.Entry<String, List<double[]>> bone : dense.rotation().entrySet()) {
            JsonObject channels = new JsonObject();
            channels.add(ReferenceClipKeying.CHANNEL_ROTATION, samples(bone.getValue()));
            if (positionBones.contains(bone.getKey())) {
                channels.add(ReferenceClipKeying.CHANNEL_POSITION, samples(dense.position().get(bone.getKey())));
            }
            bones.add(bone.getKey(), channels);
        }
        out.add("bones", bones);
        return out;
    }

    private static JsonArray samples(List<double[]> values) {
        JsonArray out = new JsonArray();
        for (double[] value : values) {
            JsonArray row = new JsonArray();
            for (int axis = 0; axis < 3; axis++) {
                row.add(value[axis]);
            }
            out.add(row);
        }
        return out;
    }

    /** The bones the hook wrote a position on at any sample (the clip's position channels). */
    private static List<String> positionBones(Map<String, List<double[]>> position) {
        List<String> out = new ArrayList<>();
        for (Map.Entry<String, List<double[]>> keyed : position.entrySet()) {
            if (keyed.getValue().stream().anyMatch(v -> v[0] != 0.0D || v[1] != 0.0D || v[2] != 0.0D)) {
                out.add(keyed.getKey());
            }
        }
        return out;
    }

    /** One state that moves: the keys chosen by the density search, the file, the index row. */
    private static JsonObject emit(Entry entry, State state, Dense dense, HookGetterReader.Declared declared, Path outputDir) throws Exception {
        Rule rule = dense.rule();
        List<Double> ticks = dense.ticks();
        Map<String, List<double[]>> rotationKeys = dense.rotation();
        Map<String, List<double[]>> positionKeys = dense.position();
        List<String> positionBones = positionBones(positionKeys);
        List<String> movingBones = new ArrayList<>();
        for (Map.Entry<String, List<double[]>> keyed : rotationKeys.entrySet()) {
            if (keyed.getValue().stream().anyMatch(v -> v[0] != 0.0D || v[1] != 0.0D || v[2] != 0.0D)) {
                movingBones.add(keyed.getKey());
            }
        }
        Map<String, List<double[]>> positioned = new TreeMap<>();
        for (String bone : positionBones) {
            positioned.put(bone, positionKeys.get(bone));
        }
        ReferenceClipKeying.Keyed keyed = ReferenceClipKeying.key(ticks, rotationKeys, positioned);

        double spanSeconds = RULE_ONE_KEY.equals(rule.kind()) ? 1.0D / TICKS_PER_SECOND : round(rule.spanTicks() / TICKS_PER_SECOND);
        JsonObject clip = new JsonObject();
        clip.addProperty("loop", true);
        clip.addProperty("animation_length", spanSeconds);
        clip.add("bones", keyed.bones(rotationKeys, positioned));
        JsonObject animations = new JsonObject();
        animations.add(state.clipName(), clip);
        JsonObject document = new JsonObject();
        document.addProperty("format_version", "1.8.0");
        document.add("animations", animations);
        Path clipPath = outputDir.resolve(state.fileName(entry.registry()));
        writeJson(clipPath, document);
        String sha256 = sha256(Files.readAllBytes(clipPath));

        // The seam: the closing sample against the first, per bone and axis, degrees reduced to (-180, 180].
        double seamRotation = 0.0D;
        double seamPosition = 0.0D;
        if (ticks.size() > 1) {
            for (Map.Entry<String, List<double[]>> bone : rotationKeys.entrySet()) {
                double[] first = bone.getValue().get(0);
                double[] last = bone.getValue().get(bone.getValue().size() - 1);
                for (int axis = 0; axis < 3; axis++) {
                    seamRotation = Math.max(seamRotation, Math.abs(wrapDegrees(last[axis] - first[axis])));
                }
            }
            for (String name : positionBones) {
                List<double[]> values = positionKeys.get(name);
                double[] first = values.get(0);
                double[] last = values.get(values.size() - 1);
                for (int axis = 0; axis < 3; axis++) {
                    seamPosition = Math.max(seamPosition, Math.abs(last[axis] - first[axis]));
                }
            }
        }

        ProbeSubject subject = dense.subject();
        JsonObject row = new JsonObject();
        row.addProperty("registry", entry.registry());
        row.addProperty("state", state.name());
        row.addProperty("named", state.named());
        row.addProperty("name_source", state.nameSource());
        if (state.isValueState()) {
            row.addProperty("getter", state.getter());
            row.add("value", state.value());
            row.addProperty("getter_read_at_inputs", subject.readGetters().contains(state.getter()));
        }
        row.addProperty("model_id", entry.id());
        row.addProperty("manifest", entry.manifestPath().getFileName().toString());
        row.addProperty("model_class", entry.modelClass());
        row.addProperty("landed", entry.landed());
        row.addProperty("rig_source", entry.rigSource());
        row.addProperty("hook", entry.hook());
        row.addProperty("geo", entry.repositoryRoot().relativize(entry.geoPath()).toString().replace('\\', '/'));
        row.addProperty("file", clipPath.getFileName().toString());
        row.addProperty("clip_name", state.clipName());
        row.addProperty("sha256", sha256);
        row.addProperty("rule", rule.kind());
        row.addProperty("rule_note", rule.note());
        if (rule.periodTicks() > 0.0D) {  // a period structure (period_multiple or two_seconds_past_cap): the slowest group's period T
            row.addProperty("period_ticks", round(rule.periodTicks()));
        }
        if (RULE_PERIOD_MULTIPLE.equals(rule.kind())) {
            row.addProperty("period_multiple_k", rule.k());
            row.addProperty("closure_delta_degrees", round(rule.closureDegrees()));
        }
        row.addProperty("span_ticks", RULE_ONE_KEY.equals(rule.kind()) ? 0.0D : round(rule.spanTicks()));
        row.addProperty("animation_length_seconds", spanSeconds);
        row.addProperty("dense_samples", ticks.size());
        row.addProperty("keys_per_bone", keyed.keysMax());
        row.addProperty("keys_per_bone_min", keyed.keysMin());
        row.addProperty("keys_total", keyed.keysTotal());
        row.addProperty("lerp_mode", ReferenceClipKeying.LERP_MODE);
        row.add("keying", keyed.json());
        row.addProperty("bones", rotationKeys.size());
        row.add("moving_bones", names(movingBones));
        row.add("position_bones", names(positionBones));
        row.add("hidden_bones_at_rest", names(new ArrayList<>(dense.hidden())));
        row.addProperty("seam_delta_degrees", round(seamRotation));
        row.addProperty("seam_delta_position_units", round(seamPosition));
        row.add("subject_after", subject.after());
        row.add("getters_read_at_inputs", names(new ArrayList<>(subject.readGetters())));
        JsonObject attacking = new JsonObject();
        attacking.add("declared_pose_interfaces", names(declared.interfaces()));
        attacking.add("getters", names(declared.attackingGetters()));
        attacking.add("outside_pose_interface", names(declared.outside()));
        attacking.addProperty("reads_attacking", declared.readsAttacking());
        attacking.addProperty("read_at_inputs", subject.attackingRead());
        row.add("attacking", attacking);
        row.addProperty("sampled_inputs", state.statement());
        System.out.println(String.format(Locale.ROOT, "wrote %s: %s%s, %d samples, %d..%d keys per bone over %d bones (%d moving, %d positioned), "
                        + "span %s ticks, seam %s deg, max error %s deg / %s units, sha256 %s",
                clipPath.getFileName(), rule.kind(), RULE_PERIOD_MULTIPLE.equals(rule.kind()) ? " (k = " + rule.k() + " x " + fmt(rule.periodTicks()) + " ticks)" : "",
                ticks.size(), keyed.keysMin(), keyed.keysMax(), rotationKeys.size(), movingBones.size(), positionBones.size(), fmt(rule.spanTicks()),
                fmt(seamRotation), fmt(keyed.maxRotationError()), fmt(keyed.maxPositionError()), sha256));
        return row;
    }

    /** Whether any bone's keys differ between samples (the one-key collapse's test). */
    private static boolean varies(Map<String, List<double[]>> keys) {
        for (List<double[]> values : keys.values()) {
            double[] first = values.get(0);
            for (double[] value : values) {
                if (value[0] != first[0] || value[1] != first[1] || value[2] != first[2]) {
                    return true;
                }
            }
        }
        return false;
    }

    /** One pose call at tick {@code t} on {@code subject} in {@code state}, whichever hook form the rig has. */
    @FunctionalInterface
    interface Poser {
        G1AnimationRuntime.EvaluatedModel pose(State state, double t, ProbeSubject subject) throws Exception;
    }

    /** The authored keys of one pose: rotation deltas (degrees, the converter's sign rule) and position offsets per bone; the hidden bones. */
    record Sample(Map<String, double[]> rotation, Map<String, double[]> position, TreeSet<String> hidden) {
    }

    /** The posed bake against the bind bake: the authored rotation delta and position offset of every bone, rounded as the keys are. */
    static Sample authoredKeys(String id, G1AnimationRuntime.EvaluatedModel posed, Map<String, float[]> bindRotations,
                               Map<String, float[]> bindPositions) {
        if (!posed.bones().keySet().equals(bindRotations.keySet())) {
            throw new IllegalStateException(id + ": the posed bake's bones differ from the bind bake's");
        }
        Map<String, double[]> rotation = new TreeMap<>();
        Map<String, double[]> position = new TreeMap<>();
        TreeSet<String> hidden = new TreeSet<>();
        for (Map.Entry<String, GeoBone> boneEntry : posed.bones().entrySet()) {
            String name = boneEntry.getKey();
            GeoBone bone = boneEntry.getValue();
            float[] bindRotation = bindRotations.get(name);
            // classic = (-Ix, Iy, -Iz); the classic delta from bind; authored = (+dCx, -dCy, -dCz).
            double dCx = -(double) bone.getRotX() + (double) bindRotation[0];
            double dCy = (double) bone.getRotY() - (double) bindRotation[1];
            double dCz = -(double) bone.getRotZ() + (double) bindRotation[2];
            rotation.put(name, new double[]{round(Math.toDegrees(dCx)), round(-Math.toDegrees(dCy)), round(-Math.toDegrees(dCz))});
            float[] bindPosition = bindPositions.get(name);
            position.put(name, new double[]{
                    round((double) bone.getPosX() - (double) bindPosition[0]),
                    round((double) bone.getPosY() - (double) bindPosition[1]),
                    round((double) bone.getPosZ() - (double) bindPosition[2])});
            if (bone.isHidden()) {
                hidden.add(name);
            }
        }
        return new Sample(rotation, position, hidden);
    }

    /**
     * The closure test (owner 2026-09-13, third set, item 28 (5)) on a {@link #RULE_PERIODIC} rule: the pose at
     * {@code t = 0}, then at each candidate {@code t = k * T} in turn on one fresh subject in the state's declared values, the
     * maximum authored rotation delta between the two over every bone and axis (reduced mod 360, exactly as the loop seam is
     * measured) and the maximum position delta in model units; the first {@code k >= 1} with {@code k * T <= 120} ticks that
     * passes both tolerances is the span. Past the cap - {@code T} itself over 120 ticks, or no candidate passing - two
     * seconds. Any other rule kind is returned as it is.
     */
    static Rule resolveSpan(Rule base, String id, Poser poser, Map<String, float[]> bindRotations, Map<String, float[]> bindPositions, State state)
            throws Exception {
        if (!RULE_PERIODIC.equals(base.kind())) {
            return base;
        }
        double period = base.periodTicks();
        if (period > SPAN_CAP_TICKS + 1.0e-9D) {
            return base.pastCap("the slowest group's period " + fmt(period) + " ticks exceeds the 6 s cap (120 ticks): a two-second window "
                    + "(40 ticks), not a loop - the sheet states the closing key's seam");
        }
        ProbeSubject subject = new ProbeSubject(state.subjectState());
        Sample start = authoredKeys(id, poser.pose(state, 0.0D, subject), bindRotations, bindPositions);
        List<String> tried = new ArrayList<>();
        for (int k = 1; k * period <= SPAN_CAP_TICKS + 1.0e-9D; k++) {
            Sample candidate = authoredKeys(id, poser.pose(state, k * period, subject), bindRotations, bindPositions);
            double rotation = 0.0D;
            for (Map.Entry<String, double[]> bone : start.rotation().entrySet()) {
                double[] first = bone.getValue();
                double[] last = candidate.rotation().get(bone.getKey());
                for (int axis = 0; axis < 3; axis++) {
                    rotation = Math.max(rotation, Math.abs(wrapDegrees(last[axis] - first[axis])));
                }
            }
            double position = 0.0D;
            for (Map.Entry<String, double[]> bone : start.position().entrySet()) {
                double[] first = bone.getValue();
                double[] last = candidate.position().get(bone.getKey());
                for (int axis = 0; axis < 3; axis++) {
                    position = Math.max(position, Math.abs(last[axis] - first[axis]));
                }
            }
            tried.add("k = " + k + ": " + fmt(rotation) + " deg" + (position > 0.0D ? " / " + fmt(position) + " units" : ""));
            if (rotation <= CLOSURE_TOLERANCE_DEGREES && position <= CLOSURE_TOLERANCE_UNITS) {
                return base.periodMultiple(k, rotation, "closes at k = " + k + " (" + fmt(k * period) + " ticks): every bone returns within "
                        + fmt(rotation) + " degrees" + (position > 0.0D ? " and " + fmt(position) + " model units" : "")
                        + " of its start at k x T (the 5-degree test under the 6 s cap; " + String.join(", ", tried) + ")");
            }
        }
        return base.pastCap("no multiple of the slowest group's period " + fmt(period) + " ticks up to the 6 s cap (120 ticks) brings every "
                + "group back within 5 degrees of its start (" + String.join(", ", tried) + "): a two-second window (40 ticks), not a loop - "
                + "the sheet states the closing key's seam");
    }

    /**
     * The ruling's rest state, declared explicitly (the {@link ProbeSubject} defaults, spelled out so the index's
     * {@code subject_after} reports every flag the pose may write - the Rotator's fan angle included).
     */
    static JsonObject restState() {
        JsonObject state = new JsonObject();
        state.addProperty("name", "rest");
        state.addProperty("attacking", 0);
        state.addProperty("ri1", 0);
        state.addProperty("rock_type", 0);
        state.addProperty("rf1", 0.0F);
        state.addProperty("seed", 0L);
        return state;
    }

    /**
     * The span rule's base, before the closure test ({@link #resolveSpan}): static = one key; a landed rig's declared channels =
     * a period structure whose slowest distinct frequency gives {@code T}; otherwise the {@link #RULES} row (a landed hook rig,
     * keyed by its manifest id) or the {@link #HOOK_RULES} row (an unlanded hook, keyed by its descriptor).
     */
    static Rule ruleFor(Entry entry) {
        if (entry.isStatic()) {
            return Rule.oneKey("a static rig (animation_kind static, no hook): one key at bind");
        }
        JsonArray channels = entry.landed() && entry.spec().has("channels") ? entry.spec().getAsJsonArray("channels") : new JsonArray();
        if (channels.size() > 0) {
            List<Double> frequencies = new ArrayList<>();
            for (JsonElement element : channels) {
                JsonObject channel = element.getAsJsonObject();
                double omega = channel.get("frequency_radians_per_age_tick").getAsDouble();
                double wingspeed = channel.has("wingspeed") ? channel.get("wingspeed").getAsDouble() : 1.0D;
                double effective = omega * wingspeed;
                if (frequencies.stream().noneMatch(f -> Math.abs(f - effective) <= FREQUENCY_EPSILON)) {
                    frequencies.add(effective);
                }
            }
            double slowest = frequencies.stream().mapToDouble(Double::doubleValue).min().orElseThrow();
            String declared = "the manifest's channels: " + frequencies.size() + " distinct frequency group(s) "
                    + frequencies.stream().map(ReferenceClipSampler::fmt).toList() + " rad/tick (omega x wingspeed)";
            if (frequencies.size() == 1) {
                return Rule.periodic(TWO_PI / slowest, declared + ": one natural period 2 pi / f = " + fmt(TWO_PI / slowest) + " ticks");
            }
            return Rule.periodic(TWO_PI / slowest, declared + ": the slowest group's period 2 pi / " + fmt(slowest) + " = "
                    + fmt(TWO_PI / slowest) + " ticks");
        }
        Rule rule = entry.landed() ? RULES.get(entry.ruleKey()) : HOOK_RULES.get(entry.ruleKey());
        if (rule == null) {
            throw new IllegalStateException(entry.id() + ": a hook rig without declared channels and without a ReferenceClipSampler."
                    + (entry.landed() ? "RULES" : "HOOK_RULES") + " row for " + entry.ruleKey()
                    + " - state its sampling rule (one natural period, or two seconds) with the source line it is read from");
        }
        return rule;
    }

    /** The key's time in seconds as {@link KeyframeLeg} writes it: ten decimals, trailing zeros dropped, {@code 0.0} at zero. */
    static String timeKey(double tick) {
        if (tick == 0.0D) {
            return "0.0";
        }
        String text = String.format(Locale.ROOT, "%.10f", tick / TICKS_PER_SECOND);
        text = text.replaceAll("0+$", "");
        return text.endsWith(".") ? text + "0" : text;
    }

    static double round(double value) {
        double rounded = Math.round(value * VALUE_ROUNDING) / VALUE_ROUNDING;
        return rounded == 0.0D ? 0.0D : rounded;  // never -0.0
    }

    static double wrapDegrees(double degrees) {
        double wrapped = degrees % 360.0D;
        if (wrapped > 180.0D) {
            wrapped -= 360.0D;
        } else if (wrapped <= -180.0D) {
            wrapped += 360.0D;
        }
        return wrapped;
    }

    private static String fmt(double value) {
        return new java.math.BigDecimal(value).setScale(6, java.math.RoundingMode.HALF_UP).stripTrailingZeros().toPlainString();
    }

    private static JsonArray names(List<String> names) {
        JsonArray out = new JsonArray();
        names.forEach(out::add);
        return out;
    }

    private static JsonObject readJson(Path path) throws IOException {
        try (var reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            return JsonParser.parseReader(reader).getAsJsonObject();
        }
    }

    private static void writeJson(Path path, JsonObject value) throws IOException {
        Files.createDirectories(path.getParent());
        Files.writeString(path, GSON.toJson(value) + "\n", StandardCharsets.UTF_8);
    }

    private static String sha256(byte[] bytes) {
        try {
            return java.util.HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException(exception);
        }
    }

}
