package danger.orespawn.g1;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import danger.orespawn.entity.client.TrueNormalCubeRenderer;
import danger.orespawn.entity.client.DrawOrder;
import danger.orespawn.entity.client.FaceOrder;
import danger.orespawn.entity.client.GeoReplacementDescriptor;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Function;
import javax.imageio.ImageIO;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDefinition;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MaterialDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.model.geom.builders.UVPair;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.cache.object.GeoCube;
import software.bernie.geckolib.loading.json.raw.Model;
import software.bernie.geckolib.loading.json.typeadapter.KeyFramesAdapter;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoRenderer;

/**
 * Phase G1 compiled-tree probe.
 *
 * <p>The {@code vanilla} mode invokes each model's compiled
 * {@code createBodyLayer()}, reflects the resulting definition metadata, bakes
 * it through Mojang's {@link LayerDefinition#bakeRoot()}, and captures the
 * baked vertices by calling {@link ModelPart.Cube#compile}. No Java source is
 * parsed. The {@code geo} mode loads the generated file through GeckoLib
 * 4.8.4's own JSON adapter/baker and captures vertices through the real
 * {@link GeoRenderer} recursive cube path.</p>
 *
 * <p>THE FRAME IS THE IN-GAME FRAME (TEST-015): each side applies ITS OWN RENDERER'S REAL CHAIN to its model, read from
 * the pinned jars with javap, so every leg compares the two chains as the game draws them, in the entity frame after the
 * yaw (the yaw is the same call on both sides - {@code 180 - yaw} about Y - and is left out).</p>
 *
 * <ul>
 *   <li>THE CLASSIC CHAIN, {@code LivingEntityRenderer.render} (NeoForge 21.1.223, the erased
 *       {@code render(LivingEntity, float, float, PoseStack, MultiBufferSource, int)}): {@code setupRotations} at
 *       offset 390 (the yaw and the living extras), then {@code PoseStack.scale(-1, -1, 1)} at 395-400 (ldc -1.0f,
 *       ldc -1.0f, fconst_1, invokevirtual scale), the renderer's {@code scale} hook at 408, then
 *       {@code PoseStack.translate(0, -1.501, 0)} at 413-417 (fconst_0, ldc -1.501f, fconst_0, invokevirtual
 *       translate), {@code setupAnim} at 510 and {@code EntityModel.renderToBuffer} at 621. So a ModelPart-space point
 *       p draws at {@code M p}, {@code M = scale(-1, -1, 1) . translate(0, -1.501, 0)}: {@link #captureVanillaSample}
 *       applies exactly those two calls to a fresh pose stack before {@code renderToBuffer} and before the
 *       {@code root.visit} compile (the declared constant render transform after them, as the classic
 *       {@code renderToBuffer} applies it inside), and the render-instance capture the same.</li>
 *   <li>THE SEAM'S CHAIN, {@code GeoReplacedEntityRenderer.actuallyRender} (GeckoLib 4.8.4): {@code scale(getScale)}
 *       at 382, {@code applyRotations} at 396 - the same {@code 180 - yaw} as vanilla, then the shared renderer's
 *       descriptor slot ({@code OreSpawnGeoReplacedEntityRenderer.applyRotations}: the constant render transform's slot
 *       form and, since this landing, the seam's height compensation
 *       {@link GeoReplacementDescriptor.RenderTransform#SEAM_HEIGHT_COMPENSATION}) - then
 *       {@code PoseStack.translate(0, 0.01, 0)} at 722-727 (fconst_0, ldc_w 0.01f, fconst_0, invokevirtual
 *       translate) and {@code GeoRenderer.actuallyRender}, which renders the bake as
 *       {@code BakedModelFactory$Builtin} built it: {@code constructCube} negates a Bedrock cube's
 *       {@code origin.x + size.x} (98-139) and every pivot's x ({@code constructCube} 157-167,
 *       {@code constructBone} 95-116) and negates the JSON X and Y rotation degrees (constructBone 61-92); no
 *       {@code scale(-1, ...)} anywhere in {@code GeoRenderer} or {@code RenderUtil}. So a bake corner b draws at
 *       {@code slot . translate(0, comp, 0) . translate(0, 0.01, 0) . b}: {@link #captureGeoSample} applies exactly
 *       that to a fresh pose stack before {@code GeoRenderer.actuallyRender}.</li>
 * </ul>
 * <p>With the converter in the Bedrock convention ({@code tools/layer_definition_to_geo.py}: a converted cube keeps its
 * ModelPart x, so the baker's negation is the classic's flip) the bake of p is {@code B p = (-x, 1.5 - y, z)} and the
 * seam's chain after the slot equals {@code M} exactly once {@code comp = 1.501 - 1.5 - 0.01}: the geometry leg's
 * corner delta is float noise on every rig, where the real chains had disagreed by 0.009 blocks in height and by a
 * left-right mirror the probe's former normalisation ({@code translate(0, 1.5, 0) scale(1, -1, 1)}, a y flip only)
 * hid. A wrong frame constant, a wrong conjugation of the constant render transform or a missing compensation now
 * fails the geometry leg at every sample: the frame is measured, not assumed.</p>
 *
 * <p>BUG-041 stage 2 (2026-09-13), vanilla mode only: three optional manifest keys let the standing
 * reference-geometry leg dump a classic model the default path cannot construct. {@code layer_factory}
 * names the static {@link LayerDefinition} factory when it is not {@code createBodyLayer} (the
 * {@code client/model} item models expose {@code createLayerDefinition}); {@code constructor_arguments}
 * (a JSON array of numbers or booleans) appends the declared arguments after the {@link ModelPart} when
 * the model's constructor is parameterised ({@code ButterflyModel(ModelPart, float wingspeed)}: the
 * shared rig of four renderers, the value only scales the hook's phase); {@code geometry_only} (true)
 * dumps the compiled definition tree, the texture and the bone names of a static LayerDefinition holder
 * that has no model class to instantiate (the item models, drawn by {@code OreSpawnItemRenderer} from
 * {@code createLayerDefinition().bakeRoot()}): no samples, no draw order, no render state. An entry
 * without the keys takes the unchanged path.</p>
 */
public final class G1ModelProbe {
    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .disableHtmlEscaping()
            .create();
    private static final float DEFINITION_EPSILON = 1.0E-6F;

    private G1ModelProbe() {
    }

    public static void main(String[] args) throws Exception {
        if (args.length < 3) {
            throw new IllegalArgumentException(
                    "Usage: G1ModelProbe vanilla <manifest> <output-dir> | "
                            + "geo <manifest> <generated-dir> <output-dir>");
        }

        String mode = args[0];
        Path manifestPath = Path.of(args[1]).toAbsolutePath().normalize();
        JsonObject manifest = readJson(manifestPath);

        if ("vanilla".equals(mode)) {
            if (args.length != 3) {
                throw new IllegalArgumentException("vanilla mode requires exactly 3 arguments");
            }
            dumpVanilla(manifestPath, manifest, Path.of(args[2]));
        } else if ("geo".equals(mode)) {
            if (args.length != 4) {
                throw new IllegalArgumentException("geo mode requires exactly 4 arguments");
            }
            dumpGeo(manifestPath, manifest, Path.of(args[2]), Path.of(args[3]));
        } else {
            throw new IllegalArgumentException("Unknown mode: " + mode);
        }
    }

    private static void dumpVanilla(Path manifestPath, JsonObject manifest, Path outputDir) throws Exception {
        Files.createDirectories(outputDir);
        clearGeneratedJson(outputDir, ".compiled.json");
        clearGeneratedJson(outputDir, RENDER_STATE_SUFFIX);
        Path repositoryRoot = manifestPath.getParent().getParent();

        for (JsonObject spec : allSpecs(manifest)) {
            // ENT-S-146: the render state this side actually requested rides in a sidecar, so the compiled
            // dump - whose sha256 is the conversion's provenance - stays byte-identical.
            JsonObject renderState = new JsonObject();
            JsonObject dump = dumpVanillaModel(repositoryRoot, spec, renderState);
            writeJson(outputDir.resolve(spec.get("id").getAsString() + ".compiled.json"), dump);
            writeJson(outputDir.resolve(spec.get("id").getAsString() + RENDER_STATE_SUFFIX), renderState);
        }
    }

    /** ENT-S-146: the per-model render-state sidecar both probe modes write beside their dumps ({@link RenderStateProbe}). */
    static final String RENDER_STATE_SUFFIX = ".render-state.json";

    private static JsonObject dumpVanillaModel(Path repositoryRoot, JsonObject spec, JsonObject renderState) throws Exception {
        String id = spec.get("id").getAsString();
        Class<?> modelClass = Class.forName(spec.get("class").getAsString());
        Method layerFactory = layerFactory(modelClass, spec);

        LayerDefinition layer = (LayerDefinition) layerFactory.invoke(null);
        MeshDefinition mesh = fieldValue(layer, "mesh", MeshDefinition.class);
        MaterialDefinition material = fieldValue(layer, "material", MaterialDefinition.class);
        int textureWidth = fieldInt(material, "xTexSize");
        int textureHeight = fieldInt(material, "yTexSize");
        PartDefinition definitionRoot = mesh.getRoot();
        ModelPart bakedRoot = layer.bakeRoot();

        validateDefinitionAgainstBake(definitionRoot, bakedRoot, "");
        validateUnnamedRoot(definitionRoot);

        Path texturePath = repositoryRoot.resolve(spec.get("texture").getAsString()).normalize();
        BufferedImage texture = ImageIO.read(texturePath.toFile());
        if (texture == null) {
            throw new IllegalStateException("Unable to decode texture " + texturePath);
        }
        boolean declaredSheetOverImage = false;
        if (spec.has("declared_sheet_over_image")) {
            // The original declared a larger sheet than the image it binds (1.7.10 Tshirt: 512x256 over
            // 320x160, a deliberate UV window); the manifest names the image size it expects.
            JsonArray declared = spec.getAsJsonArray("declared_sheet_over_image");
            if (texture.getWidth() != declared.get(0).getAsInt() || texture.getHeight() != declared.get(1).getAsInt()) {
                throw new IllegalStateException(id + " declared_sheet_over_image " + declared + " != PNG "
                        + texture.getWidth() + "x" + texture.getHeight());
            }
            declaredSheetOverImage = true;
        } else if (texture.getWidth() != textureWidth || texture.getHeight() != textureHeight) {
            throw new IllegalStateException(id + " LayerDefinition texture size " + textureWidth + "x" + textureHeight
                    + " != PNG " + texture.getWidth() + "x" + texture.getHeight());
        }

        JsonObject out = new JsonObject();
        out.addProperty("schema_version", 1);
        out.addProperty("probe", "compiled LayerDefinition + baked ModelPart");
        out.addProperty("model_id", id);
        out.addProperty("tier", spec.get("tier").getAsInt());
        out.addProperty("proof_scope", spec.has("proof_scope")
                ? spec.get("proof_scope").getAsString() : "production_proof_model");
        out.addProperty("source_class", modelClass.getName());
        out.addProperty("source_class_sha256", classSha256(modelClass));
        out.addProperty("texture", spec.get("texture").getAsString().replace('\\', '/'));
        out.addProperty("texture_sha256", sha256(Files.readAllBytes(texturePath)));
        out.addProperty("texture_width", textureWidth);
        out.addProperty("texture_height", textureHeight);
        if (declaredSheetOverImage) {
            out.addProperty("image_width", texture.getWidth());
            out.addProperty("image_height", texture.getHeight());
            out.addProperty("declared_sheet_over_image", true);
        }

        Map<String, String> namesToPaths = new TreeMap<>();
        JsonObject definition = dumpDefinitionPart(definitionRoot, null, "", new float[]{0, 0, 0}, namesToPaths);
        out.add("definition", definition);
        JsonArray boneNames = new JsonArray();
        namesToPaths.keySet().forEach(boneNames::add);
        out.add("bone_names", boneNames);
        RenderInstanceContext instances = renderInstanceContext(spec, bakedRoot, namesToPaths);
        if (instances != null) {
            // Slice 4c: the declaration travels with the dump so the converter and the parity tool
            // can check they expand exactly what the probe attributed the draws to.
            out.add("render_instances", spec.getAsJsonObject("render_instances").deepCopy());
        }

        if (spec.has("geometry_only") && spec.get("geometry_only").getAsBoolean()) {
            // BUG-041 stage 2: a static LayerDefinition holder (the client/model item models) has no model
            // class, no setupAnim and no render type: the compiled tree above is the whole dump.
            out.addProperty("geometry_only", true);
            out.add("samples", new JsonArray());
            out.add("animation_bake_samples", new JsonArray());
            renderState.addProperty("schema_version", 1);
            renderState.addProperty("model_id", id);
            renderState.addProperty("side", "classic");
            renderState.addProperty("geometry_only", true);
            return out;
        }

        out.addProperty("draw_order_source", DrawOrderObserver.SOURCE);
        // The constant render transform (TEST-013): read from the candidate descriptor WITHOUT an entity (the
        // renderType(null) form) and applied to this side's root.visit capture in the classic renderer's own
        // terms, so the geometry / surface legs compare like with like; the render_vertices come from
        // renderToBuffer itself, the classic's OWN rotation included - the visual leg is what proves the declaration
        // equals it. Recorded so the parity tool can check both sides read the same one.
        GeoReplacementDescriptor.RenderTransform transform = constantRenderTransform(spec);
        if (!transform.isIdentity()) {
            out.add("render_transform", renderTransformJson(transform));
        }

        Object model = newClassicModel(modelClass, spec, bakedRoot);
        Method setupAnim = findSetupAnim(modelClass);
        // ENT-S-146: the render state this side actually requests, OBSERVED. The light is the classic
        // RENDERER's decision in 1.21.1 (EntityRenderer.getPackedLightCoords), so when the manifest
        // names it (classic_renderer) its light-level overrides are evaluated registry-free and handed
        // to renderToBuffer; otherwise the probe's light. The colour handed in is the probe's white: what
        // the vertices carry is what the model itself requests, seen at addVertex.
        RenderStateProbe.Observed observed = new RenderStateProbe.Observed();
        int packedLight = RenderStateProbe.PROBE_LIGHT;
        JsonObject rendererLight = null;
        if (spec.has("classic_renderer")) {
            rendererLight = RenderStateProbe.classicRendererLight(spec.get("classic_renderer").getAsString(), layer);
            packedLight = rendererLight.get("packed").getAsInt();
        }
        // G2 root-order contract: the classic draw order of every full capture is observed on a
        // SECOND bake of the same LayerDefinition, posed exactly as the captured one, so the
        // observation's extra renderToBuffer calls (one per cube-bearing part) never touch the
        // captured model's state (the Rotator advances its fan angle inside renderToBuffer and
        // subject_after pins that advance).
        ModelPart shadowRoot = layer.bakeRoot();
        DrawOrderObserver drawOrder = new DrawOrderObserver(newClassicModel(modelClass, spec, shadowRoot), shadowRoot,
                namesToPaths, instances == null ? Map.of() : instances.declaredCounts);

        // THE SECOND PASS (the remainder slice, 2026-09-15; TEST-018): the classic renderer's second pass, captured beside
        // every full capture - the bind included - on its own consumer under the same classic chain (the King's renderWingMembranes).
        ClassicSecondPass secondPass = ClassicSecondPass.declared(spec, modelClass);
        JsonArray samples = new JsonArray();
        resetBakedTree(bakedRoot);
        JsonObject bindSample = captureVanillaSample(
                new SampleRequest("bind", 0.0F, 0.0F, true, false),
                model, bakedRoot, namesToPaths, Set.of(), instances, observed, packedLight, transform);
        if (secondPass != null) {
            secondPass.capture(bindSample, model, packedLight);
        }
        drawOrder.observe(bindSample, (shadowModel, root) -> resetBakedTree(root));
        if (secondPass != null) {
            drawOrder.observePass(bindSample, (shadowModel, root) -> resetBakedTree(root), secondPass.method);
        }
        samples.add(bindSample);

        float limbSwing = spec.get("limb_swing").getAsFloat();
        float netHeadYaw = optionalFloat(spec, "net_head_yaw");
        float headPitch = optionalFloat(spec, "head_pitch");
        String animationKind = spec.get("animation_kind").getAsString();
        boolean productionHook = CODE_DRIVEN_KIND.equals(animationKind)
                || ENTITY_STATE_KIND.equals(animationKind);
        List<SampleRequest> requests = sampleRequests(spec);
        boolean partialTickInput = partialTickInput(spec);
        if (ENTITY_STATE_KIND.equals(animationKind)) {
            // The classic model reads its entity: pose it from a declared state through
            // its entity-free poseFrom entry, exactly as the candidate side is posed.
            Method poseFrom = findPoseFrom(modelClass);
            if (requests.isEmpty()) {
                throw new IllegalStateException(id + ": entity_state models need setupAnim samples");
            }
            for (JsonObject state : entityStates(spec)) {
                for (SampleRequest request : requests) {
                    ProbeSubject subject = new ProbeSubject(state);
                    resetBakedTree(bakedRoot);
                    showAllParts(bakedRoot);
                    invokePoseFrom(poseFrom, model, subject, limbSwing, request.limbSwingAmount(),
                            request.ageTicks(), netHeadYaw, headPitch, request.partialTick());
                    Set<String> hidden = hiddenParts(bakedRoot, namesToPaths);
                    JsonObject sample = captureVanillaSample(stateRequest(state, request), model, bakedRoot,
                            namesToPaths, hidden, instances, observed, packedLight, transform);
                    sample.add("entity_state", state.deepCopy());
                    sample.add("subject_after", subject.after());
                    sample.add("hidden_bones", names(hidden));
                    if (partialTickInput) {
                        sample.addProperty("partial_tick", request.partialTick());
                    }
                    if (secondPass != null && request.fullCapture()) {
                        secondPass.capture(sample, model, packedLight);
                    }
                    ShadowPose shadowPose = (shadowModel, root) -> {
                        // The same declared state on a fresh subject: a seeded roll evolves identically.
                        resetBakedTree(root);
                        showAllParts(root);
                        invokePoseFrom(poseFrom, shadowModel, new ProbeSubject(state), limbSwing, request.limbSwingAmount(),
                                request.ageTicks(), netHeadYaw, headPitch, request.partialTick());
                    };
                    drawOrder.observe(sample, shadowPose);
                    if (secondPass != null && request.fullCapture()) {
                        drawOrder.observePass(sample, shadowPose, secondPass.method);
                    }
                    samples.add(sample);
                }
            }
            showAllParts(bakedRoot);
        } else {
            for (SampleRequest request : requests) {
                resetBakedTree(bakedRoot);
                setupAnim.invoke(model, null, limbSwing, request.limbSwingAmount(),
                        request.ageTicks(), netHeadYaw, headPitch);
                Set<String> hidden = productionHook ? hiddenParts(bakedRoot, namesToPaths) : Set.of();
                JsonObject sample = captureVanillaSample(request, model, bakedRoot, namesToPaths, hidden, instances,
                        observed, packedLight, transform);
                if (productionHook) {
                    sample.add("hidden_bones", names(hidden));
                }
                if (partialTickInput) {
                    sample.addProperty("partial_tick", request.partialTick());
                }
                if (secondPass != null && request.fullCapture()) {
                    secondPass.capture(sample, model, packedLight);
                }
                ShadowPose shadowPose = (shadowModel, root) -> {
                    resetBakedTree(root);
                    setupAnim.invoke(shadowModel, null, limbSwing, request.limbSwingAmount(),
                            request.ageTicks(), netHeadYaw, headPitch);
                };
                drawOrder.observe(sample, shadowPose);
                if (secondPass != null && request.fullCapture()) {
                    drawOrder.observePass(sample, shadowPose, secondPass.method);
                }
                samples.add(sample);
            }
        }
        out.add("samples", samples);

        JsonArray animationBakeSamples = new JsonArray();
        if (GAIT_SCALED_KIND.equals(animationKind)) {
            for (BakeRequest request : animationBakeRequests(spec)) {
                resetBakedTree(bakedRoot);
                setupAnim.invoke(model, null, limbSwing, 1.0F,
                        request.ageTicks(), netHeadYaw, headPitch);
                JsonObject sample = new JsonObject();
                sample.addProperty("id", request.id());
                sample.addProperty("fraction", request.fraction());
                sample.addProperty("age_ticks", request.ageTicks());
                sample.add("transforms", captureModelPartTransforms(bakedRoot, namesToPaths));
                animationBakeSamples.add(sample);
            }
        }
        out.add("animation_bake_samples", animationBakeSamples);

        // ENT-S-146: the sidecar - the render type as the function object the model holds (RenderType
        // itself cannot be initialised here, OPT-029 R0), the colour and light every captured vertex
        // carried, and where the light came from.
        renderState.addProperty("schema_version", 1);
        renderState.addProperty("model_id", id);
        renderState.addProperty("side", "classic");
        JsonObject renderType = RenderStateProbe.describeFunction(
                RenderStateProbe.classicFunction(model), RenderStateProbe.entityModelDefault());
        renderType.addProperty("source", "the Model.renderType function (EntityModel(Function)); Model.renderType(texture) "
                + "applies it and LivingEntityRenderer.getRenderType returns it for a visible body (21.1.223 bytecode 17-30)");
        renderState.add("render_type", renderType);
        JsonObject colour = observed.colourJson();
        colour.addProperty("handed_to_render_to_buffer", -1);
        colour.addProperty("source", "observed at addVertex over the full captures (renderToBuffer was handed the probe's "
                + "white, -1: what the vertices carry is what the model requests)");
        renderState.add("vertex_color", colour);
        JsonObject light = observed.lightJson();
        light.addProperty("handed_to_render_to_buffer", packedLight);
        light.addProperty("source", rendererLight != null
                ? "the classic renderer's getBlockLightLevel / getSkyLightLevel (classic_renderer, evaluated registry-free), "
                        + "packed as EntityRenderer.getPackedLightCoords packs them (0-24), handed to renderToBuffer and observed at addVertex"
                : "the probe's light (no classic_renderer declared: the renderer decides nothing), observed at addVertex");
        if (rendererLight != null) {
            light.add("classic_renderer", rendererLight);
        }
        renderState.add("packed_light", light);
        if (secondPass != null) {
            // TEST-018: the classic renderer's second pass - its render-type function (the model's own object), the colour and
            // light every vertex of the pass carried, the bones and the classic method - beside the main pass's.
            renderState.add("second_pass", secondPass.sidecar());
        }
        return out;
    }

    /**
     * THE CLASSIC RENDERER'S SECOND PASS (the remainder slice, 2026-09-15; TEST-018 - the King): the manifest's
     * {@code second_pass} block names the classic model's method that draws the pass ({@code classic_method}, the
     * {@code (PoseStack, VertexConsumer, int, int, int)} shape of {@code renderToBuffer}: {@code ModelTheKing.renderWingMembranes}),
     * the bones it draws ({@code bones}), and the model's own static fields holding the pass's render-type function
     * ({@code classic_render_type_field}: {@code WING_MEMBRANE_RENDER_TYPE}) and ARGB colour ({@code classic_color_field}:
     * {@code WING_MEMBRANE_COLOR}) - the very objects {@code TheKingRenderer.render} hands the pass. Beside every full
     * capture the probe runs that method on its own consumer under the same classic chain M with the pass's colour and the
     * capture's light ({@code render_vertices_pass2}), attributes its draws by the same skipDraw elimination
     * ({@code draw_order_pass2}) and records the pass's render state in the sidecar ({@code second_pass}). The entity
     * transform {@code TheKingRenderer.setupEntityTransform} rebuilds for its pass is the renderer's, not the model's: the
     * probe captures the model's pass under M, the chain the main pass is captured under.
     */
    private static final class ClassicSecondPass {
        final Method method;
        final List<String> bones;
        final int color;
        final Function<ResourceLocation, RenderType> renderType;
        final String renderTypeField;
        final String colorField;
        final RenderStateProbe.Observed observed = new RenderStateProbe.Observed();

        private ClassicSecondPass(Method method, List<String> bones, int color, Function<ResourceLocation, RenderType> renderType,
                                  String renderTypeField, String colorField) {
            this.method = method;
            this.bones = bones;
            this.color = color;
            this.renderType = renderType;
            this.renderTypeField = renderTypeField;
            this.colorField = colorField;
        }

        /** The manifest's declaration resolved against the classic model class, or null for an entry without one. */
        @SuppressWarnings("unchecked")
        static ClassicSecondPass declared(JsonObject spec, Class<?> modelClass) throws Exception {
            if (!spec.has("second_pass")) {
                return null;
            }
            JsonObject pass = spec.getAsJsonObject("second_pass");
            String methodName = pass.get("classic_method").getAsString();
            Method method = modelClass.getDeclaredMethod(methodName, PoseStack.class, VertexConsumer.class, int.class, int.class, int.class);
            method.setAccessible(true);
            List<String> bones = new ArrayList<>();
            pass.getAsJsonArray("bones").forEach(bone -> bones.add(bone.getAsString()));
            if (bones.isEmpty()) {
                throw new IllegalStateException(spec.get("id").getAsString() + ": second_pass.bones is empty");
            }
            String renderTypeField = pass.get("classic_render_type_field").getAsString();
            String colorField = pass.get("classic_color_field").getAsString();
            Field function = modelClass.getDeclaredField(renderTypeField);
            function.setAccessible(true);
            Field colour = modelClass.getDeclaredField(colorField);
            colour.setAccessible(true);
            if (!Modifier.isStatic(function.getModifiers()) || !Modifier.isStatic(colour.getModifiers())) {
                throw new IllegalStateException(spec.get("id").getAsString() + ": the second pass's fields must be static");
            }
            return new ClassicSecondPass(method, List.copyOf(bones), colour.getInt(null),
                    (Function<ResourceLocation, RenderType>) function.get(null), renderTypeField, colorField);
        }

        /** The classic model's own function object (the render-state identity check against the descriptor's). */
        static Function<ResourceLocation, RenderType> classicFunction(JsonObject spec, Class<?> modelClass) throws Exception {
            Field function = modelClass.getDeclaredField(spec.getAsJsonObject("second_pass").get("classic_render_type_field").getAsString());
            function.setAccessible(true);
            @SuppressWarnings("unchecked")
            Function<ResourceLocation, RenderType> value = (Function<ResourceLocation, RenderType>) function.get(null);
            return value;
        }

        /** The pass's vertices under the classic chain M, on their own consumer, with the pass's colour and the capture's light. */
        void capture(JsonObject sample, Object model, int packedLight) throws Exception {
            FlatCapturingVertexConsumer consumer = new FlatCapturingVertexConsumer(this.observed);
            PoseStack render = new PoseStack();
            applyClassicChain(render);
            this.method.invoke(model, render, consumer, packedLight, 0, this.color);
            sample.add("render_vertices_pass2", consumer.verticesJson());
        }

        JsonObject sidecar() throws Exception {
            JsonObject out = new JsonObject();
            JsonObject renderType = RenderStateProbe.describeFunction(this.renderType, RenderStateProbe.entityModelDefault());
            renderType.addProperty("source", "the classic model's own static function object " + this.renderTypeField
                    + " (the ENT-S-146 form), applied to the texture by the classic renderer's second pass");
            out.add("render_type", renderType);
            JsonObject colour = this.observed.colourJson();
            colour.addProperty("handed_to_second_pass", this.color);
            colour.addProperty("source", "the classic model's own static constant " + this.colorField
                    + ", handed to the pass method and observed at addVertex over the full captures");
            out.add("vertex_color", colour);
            JsonObject light = this.observed.lightJson();
            light.addProperty("source", "the capture's packed light, handed to the pass method and observed at addVertex");
            out.add("packed_light", light);
            out.add("bones", names(this.bones));
            out.addProperty("classic_method", this.method.getName());
            return out;
        }
    }

    private static final String GAIT_SCALED_KIND = "gait_scaled";
    private static final String CODE_DRIVEN_KIND = "code_driven";
    private static final String ENTITY_STATE_KIND = "entity_state";

    private static float optionalFloat(JsonObject spec, String field) {
        return spec.has(field) ? spec.get(field).getAsFloat() : 0.0F;
    }

    /**
     * The candidate descriptor's constant render transform (TEST-013), read without an entity through the
     * production-hook class the manifest names; the identity for an entry without one.
     */
    private static GeoReplacementDescriptor.RenderTransform constantRenderTransform(JsonObject spec) throws Exception {
        String kind = spec.get("animation_kind").getAsString();
        if (!CODE_DRIVEN_KIND.equals(kind) && !ENTITY_STATE_KIND.equals(kind)) {
            return GeoReplacementDescriptor.RenderTransform.IDENTITY;
        }
        return S4CandidateRuntime.renderTransform(spec.get("candidate_class").getAsString());
    }

    /** The declared transform as recorded in both dumps (the parity tool requires the two records equal). */
    private static JsonObject renderTransformJson(GeoReplacementDescriptor.RenderTransform transform) {
        JsonObject out = new JsonObject();
        out.add("rotation_degrees_xyz", floats(transform.xDegrees(), transform.yDegrees(), transform.zDegrees()));
        out.add("translation", floats(transform.x(), transform.y(), transform.z()));
        out.addProperty("form", "the classic renderToBuffer's own terms: translate, then mulPose about X, Y, Z (GeoReplacementDescriptor.RenderTransform)");
        out.addProperty("source", "descriptor.renderTransform(), read without an entity; the replaced renderer applies its slot form "
                + "(M C M^-1 through the seam frame F = M, vanilla's own scale(-1, -1, 1) and translate(0, -1.501, 0); TEST-015) in "
                + "applyRotations, and this probe applies the same slot in the seam's real chain on its geo side, M on its classic side");
        return out;
    }

    /** Declared entity states (attacking, ri1, seed, rock_type) for models whose classic pose reads the entity. */
    private static List<JsonObject> entityStates(JsonObject spec) {
        List<JsonObject> states = new ArrayList<>();
        boolean entityState = ENTITY_STATE_KIND.equals(spec.get("animation_kind").getAsString());
        if (!spec.has("entity_states")) {
            if (entityState) {
                throw new IllegalStateException(spec.get("id").getAsString() + ": entity_state models declare entity_states");
            }
            return states;
        }
        if (!entityState) {
            throw new IllegalStateException(spec.get("id").getAsString()
                    + ": entity_states are only declared for " + ENTITY_STATE_KIND + " models");
        }
        for (JsonElement element : spec.getAsJsonArray("entity_states")) {
            states.add(element.getAsJsonObject());
        }
        if (states.isEmpty()) {
            throw new IllegalStateException(spec.get("id").getAsString() + ": entity_states is empty");
        }
        if (states.stream().map(state -> state.get("name").getAsString()).distinct().count() != states.size()) {
            throw new IllegalStateException(spec.get("id").getAsString() + ": duplicate entity state names");
        }
        return states;
    }

    private static SampleRequest stateRequest(JsonObject state, SampleRequest request) {
        return new SampleRequest("s_" + state.get("name").getAsString() + "_" + request.id(),
                request.ageTicks(), request.limbSwingAmount(), request.fullCapture(),
                request.denseTransformSample());
    }

    /**
     * The compiled model's entity-free pose entry: {@code poseFrom(<pose interface>, 5 floats)}, or - preferred where a
     * model declares it (the remainder slice, 2026-09-15: the bipeds' {@code ModelBoyfriend} / {@code ModelGirlfriend}, whose
     * classic renderer sets per-frame fields from the frame's partial tick) - the seven-parameter form with the partial tick
     * last, {@code poseFrom(<pose interface>, 5 floats, float partialTick)}; {@link #invokePoseFrom} hands each its arguments.
     */
    private static Method findPoseFrom(Class<?> modelClass) {
        return Arrays.stream(modelClass.getDeclaredMethods())
                .filter(method -> method.getName().equals("poseFrom"))
                .filter(method -> method.getParameterCount() == 6 || method.getParameterCount() == 7)
                .filter(method -> method.getParameterTypes()[0].isInterface()
                        && method.getParameterTypes()[0].isInstance(new ProbeSubject(new JsonObject())))
                .sorted(Comparator.comparingInt(Method::getParameterCount).reversed())
                .findFirst()
                .map(method -> {
                    method.setAccessible(true);
                    return method;
                })
                .orElseThrow(() -> new IllegalStateException("No poseFrom(<pose interface>, ...) on "
                        + modelClass.getName() + "; entity_state models must expose one"));
    }

    /** The pose entry on its arguments: the six-parameter form without the partial tick, the seven-parameter form with it. */
    private static void invokePoseFrom(Method poseFrom, Object model, Object subject, float limbSwing, float limbSwingAmount,
                                       float ageTicks, float netHeadYaw, float headPitch, float partialTick) throws Exception {
        if (poseFrom.getParameterCount() == 7) {
            poseFrom.invoke(model, subject, limbSwing, limbSwingAmount, ageTicks, netHeadYaw, headPitch, partialTick);
        } else {
            poseFrom.invoke(model, subject, limbSwing, limbSwingAmount, ageTicks, netHeadYaw, headPitch);
        }
    }

    private static Set<String> hiddenParts(ModelPart root, Map<String, String> namesToPaths) throws Exception {
        Map<String, ModelPart> byName = new TreeMap<>();
        collectBakedParts(root, "", byName, namesToPaths);
        Set<String> hidden = new java.util.TreeSet<>();
        byName.forEach((name, part) -> {
            if (!part.visible) {
                hidden.add(name);
            }
        });
        return hidden;
    }

    private static void showAllParts(ModelPart root) {
        root.getAllParts().forEach(part -> part.visible = true);
    }

    private static boolean hiddenByPath(String path, Set<String> hiddenBones) {
        if (hiddenBones.isEmpty()) {
            return false;
        }
        for (String segment : path.split("/")) {
            if (!segment.isEmpty() && hiddenBones.contains(segment)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Bone positions in classic ModelPart terms (x/y/z in the parent's frame):
     * the internal pivot is (-x, 24 - y, z) of the absolute ModelPart pivot (internal
     * space is classic space reflected in X and Y, TEST-015) and GeckoLib translates
     * by (-posX, posY, posZ)/16 in internal space, so a classic move (dx, dy, dz) is
     * the offset (dx, -dy, dz) - OreSpawnGeoReplacement's basis facts, read back here.
     */
    private static JsonObject javaPositions(Map<String, GeoBone> bones) {
        JsonObject positions = new JsonObject();
        bones.forEach((name, bone) -> {
            GeoBone parent = bone.getParent();
            float bindX = parent == null ? -bone.getPivotX() : parent.getPivotX() - bone.getPivotX();
            float bindY = parent == null ? 24.0F - bone.getPivotY() : parent.getPivotY() - bone.getPivotY();
            float bindZ = parent == null ? bone.getPivotZ() : bone.getPivotZ() - parent.getPivotZ();
            positions.add(name, floats(bindX + bone.getPosX(), bindY - bone.getPosY(), bindZ + bone.getPosZ()));
        });
        return positions;
    }

    private static JsonArray names(Collection<String> names) {
        JsonArray out = new JsonArray();
        names.forEach(out::add);
        return out;
    }

    /**
     * The classic model's concrete {@code setupAnim(entity, 5 floats)} - its own, or the nearest superclass's where the model
     * declares none (the remainder slice, 2026-09-15: {@code ModelBoyfriend} / {@code ModelGirlfriend} inherit vanilla
     * {@code HumanoidModel.setupAnim}; an entity_state entry poses through {@code poseFrom} and never invokes it, but every
     * entry resolves it).
     */
    private static Method findSetupAnim(Class<?> modelClass) {
        for (Class<?> current = modelClass; current != null; current = current.getSuperclass()) {
            java.util.Optional<Method> found = Arrays.stream(current.getDeclaredMethods())
                    .filter(method -> method.getName().equals("setupAnim"))
                    .filter(method -> method.getParameterCount() == 6)
                    .filter(method -> !method.isBridge() && !Modifier.isAbstract(method.getModifiers()))
                    .findFirst();
            if (found.isPresent()) {
                Method method = found.get();
                method.setAccessible(true);
                return method;
            }
        }
        throw new IllegalStateException("No concrete setupAnim method on " + modelClass.getName());
    }

    private static JsonObject captureVanillaSample(SampleRequest request, Object model, ModelPart root,
                                                    Map<String, String> namesToPaths,
                                                    Set<String> hiddenBones,
                                                    RenderInstanceContext instances,
                                                    RenderStateProbe.Observed observed, int packedLight,
                                                    GeoReplacementDescriptor.RenderTransform transform) throws Exception {
        JsonObject sample = new JsonObject();
        sample.addProperty("id", request.id());
        sample.addProperty("capture_kind", request.fullCapture() ? "full" : "transform_only");
        sample.addProperty("dense_transform_sample", request.denseTransformSample());
        sample.addProperty("age_ticks", request.ageTicks());
        sample.addProperty("limb_swing_amount", request.limbSwingAmount());
        sample.add("transforms", captureModelPartTransforms(root, namesToPaths));

        if (!request.fullCapture()) {
            return sample;
        }
        if (instances != null) {
            if (!transform.isIdentity()) {
                // the instance capture takes its cubes from renderToBuffer itself, the classic's own rotation included;
                // applying the declared transform again would double it - no rig declares both, and none may silently
                throw new IllegalStateException(request.id() + ": a render_instances rig with a constant render transform "
                        + "has no capture form (its classic cubes already carry the renderToBuffer rotation)");
            }
            // Slice 4c: the classic model draws some parts N times per frame under a per-draw
            // transform, so root.visit (one group per part) cannot stand for what it draws.
            // Capture renderToBuffer itself, one cube group per ModelPart.render call.
            instances.capture(sample, model, hiddenBones, observed, packedLight);
            return sample;
        }
        FlatCapturingVertexConsumer renderConsumer = new FlatCapturingVertexConsumer(observed);
        PoseStack render = new PoseStack();
        applyClassicChain(render);
        ((EntityModel<?>) model).renderToBuffer(render, renderConsumer, packedLight, 0, -1);
        sample.add("render_vertices", renderConsumer.verticesJson());

        CapturingVertexConsumer consumer = new CapturingVertexConsumer();
        PoseStack visit = new PoseStack();
        applyClassicChain(visit);
        // The constant render transform (TEST-013): root.visit compiles the tree with no renderToBuffer around it, so the
        // DECLARED classic form is applied here, inside the chain as the classic renderToBuffer applies it, for the
        // geometry / surface legs (the render_vertices above carry the classic's own rotation; the visual leg is what
        // proves the declaration equals it).
        transform.applyClassic(visit);
        root.visit(visit, (pose, path, index, cube) -> {
            String boneName = boneNameForPath(path, namesToPaths);
            if (hiddenByPath(path, hiddenBones)) {
                // ModelPart.visit ignores `visible`; ModelPart.render does not.
                return;
            }
            consumer.begin(boneName, path, index);
            cube.compile(pose, consumer, 0, 0, -1);
            consumer.end();
        });
        sample.add("cubes", consumer.groupsJson());
        return sample;
    }

    /**
     * THE CLASSIC CHAIN on a fresh pose stack (the class javadoc, from the bytecode of NeoForge 21.1.223):
     * {@code LivingEntityRenderer.render}'s {@code scale(-1, -1, 1)} (offsets 395-400) and
     * {@code translate(0, -1.501, 0)} (413-417), the two calls between the yaw and {@code renderToBuffer} (621) that
     * carry a ModelPart-space point into the entity frame - the same calls with the same float constants. The
     * renderer's {@code scale} hook (408) between them is the descriptor's per-entity scale, not applied headlessly.
     */
    static void applyClassicChain(PoseStack poseStack) {
        poseStack.scale(-1.0F, -1.0F, 1.0F);
        poseStack.translate(0.0F, -1.501F, 0.0F);
    }

    private static JsonObject captureModelPartTransforms(ModelPart root,
                                                         Map<String, String> namesToPaths) throws Exception {
        Map<String, ModelPart> byName = new TreeMap<>();
        collectBakedParts(root, "", byName, namesToPaths);
        JsonObject transforms = new JsonObject();
        byName.forEach((name, part) -> {
            JsonObject transform = new JsonObject();
            transform.add("position", floats(part.x, part.y, part.z));
            transform.add("rotation", floats(part.xRot, part.yRot, part.zRot));
            transform.add("scale", floats(part.xScale, part.yScale, part.zScale));
            transforms.add(name, transform);
        });
        return transforms;
    }

    private static void collectBakedParts(ModelPart part, String path, Map<String, ModelPart> byName,
                                          Map<String, String> expectedPaths) throws Exception {
        Map<String, ModelPart> children = bakedChildren(part);
        for (Map.Entry<String, ModelPart> entry : new TreeMap<>(children).entrySet()) {
            String childPath = path + "/" + entry.getKey();
            String previousPath = expectedPaths.get(entry.getKey());
            if (!childPath.equals(previousPath)) {
                throw new IllegalStateException("Baked path drift for bone " + entry.getKey()
                        + ": " + childPath + " != " + previousPath);
            }
            byName.put(entry.getKey(), entry.getValue());
            collectBakedParts(entry.getValue(), childPath, byName, expectedPaths);
        }
    }

    private static String boneNameForPath(String path, Map<String, String> namesToPaths) {
        if (path.isEmpty()) {
            throw new IllegalStateException("Unnamed MeshDefinition root unexpectedly contains a cube");
        }
        int slash = path.lastIndexOf('/');
        String name = path.substring(slash + 1);
        if (!path.equals(namesToPaths.get(name))) {
            throw new IllegalStateException("Cube path does not identify a unique exact bone name: " + path);
        }
        return name;
    }

    private static JsonObject dumpDefinitionPart(PartDefinition definition, String name, String path,
                                                 float[] parentAbsolutePivot,
                                                 Map<String, String> namesToPaths) throws Exception {
        PartPose pose = fieldValue(definition, "partPose", PartPose.class);
        float[] absolutePivot = {
                parentAbsolutePivot[0] + pose.x,
                parentAbsolutePivot[1] + pose.y,
                parentAbsolutePivot[2] + pose.z
        };

        JsonObject out = new JsonObject();
        if (name != null) {
            String previous = namesToPaths.put(name, path);
            if (previous != null) {
                throw new IllegalStateException("Duplicate ModelPart name cannot be represented verbatim in GeckoLib: "
                        + name + " at " + previous + " and " + path);
            }
            out.addProperty("name", name);
        }
        out.addProperty("path", path);
        out.add("local_pivot", floats(pose.x, pose.y, pose.z));
        out.add("absolute_pivot", floats(absolutePivot));
        out.add("initial_rotation_radians", floats(pose.xRot, pose.yRot, pose.zRot));

        JsonArray cubes = new JsonArray();
        for (CubeDefinition cube : definitionCubes(definition)) {
            cubes.add(dumpDefinitionCube(cube));
        }
        out.add("cubes", cubes);

        JsonArray children = new JsonArray();
        for (Map.Entry<String, PartDefinition> entry : new TreeMap<>(definitionChildren(definition)).entrySet()) {
            String childPath = path + "/" + entry.getKey();
            children.add(dumpDefinitionPart(entry.getValue(), entry.getKey(), childPath,
                    absolutePivot, namesToPaths));
        }
        out.add("children", children);
        return out;
    }

    private static JsonObject dumpDefinitionCube(CubeDefinition cube) throws Exception {
        Vector3f origin = fieldValue(cube, "origin", Vector3f.class);
        Vector3f dimensions = fieldValue(cube, "dimensions", Vector3f.class);
        CubeDeformation deformation = fieldValue(cube, "grow", CubeDeformation.class);
        UVPair uv = fieldValue(cube, "texCoord", UVPair.class);
        UVPair texScale = fieldValue(cube, "texScale", UVPair.class);
        @SuppressWarnings("unchecked")
        Set<Direction> visibleFaces = (Set<Direction>) fieldValue(cube, "visibleFaces", Set.class);

        JsonObject out = new JsonObject();
        out.add("origin", floats(origin.x(), origin.y(), origin.z()));
        out.add("size", floats(dimensions.x(), dimensions.y(), dimensions.z()));
        out.add("deformation", floats(
                fieldFloat(deformation, "growX"),
                fieldFloat(deformation, "growY"),
                fieldFloat(deformation, "growZ")));
        out.add("uv", floats(uv.u(), uv.v()));
        out.add("texture_scale", floats(texScale.u(), texScale.v()));
        out.addProperty("mirror", fieldBoolean(cube, "mirror"));
        JsonArray faces = new JsonArray();
        visibleFaces.stream().map(Direction::getName).sorted().forEach(faces::add);
        out.add("visible_faces", faces);
        return out;
    }

    private static void validateUnnamedRoot(PartDefinition root) throws Exception {
        PartPose pose = fieldValue(root, "partPose", PartPose.class);
        if (!definitionCubes(root).isEmpty()
                || Math.abs(pose.x) > DEFINITION_EPSILON
                || Math.abs(pose.y) > DEFINITION_EPSILON
                || Math.abs(pose.z) > DEFINITION_EPSILON
                || Math.abs(pose.xRot) > DEFINITION_EPSILON
                || Math.abs(pose.yRot) > DEFINITION_EPSILON
                || Math.abs(pose.zRot) > DEFINITION_EPSILON) {
            throw new IllegalStateException("Unnamed MeshDefinition root has geometry or a transform; "
                    + "conversion would require inventing a bone name");
        }
    }

    private static void validateDefinitionAgainstBake(PartDefinition definition, ModelPart baked,
                                                      String path) throws Exception {
        List<CubeDefinition> definitions = definitionCubes(definition);
        List<ModelPart.Cube> bakedCubes = bakedCubes(baked);
        if (definitions.size() != bakedCubes.size()) {
            throw new IllegalStateException(path + " cube count differs between definition and bake: "
                    + definitions.size() + " != " + bakedCubes.size());
        }
        for (int i = 0; i < definitions.size(); i++) {
            CubeDefinition definitionCube = definitions.get(i);
            ModelPart.Cube bakedCube = bakedCubes.get(i);
            Vector3f origin = fieldValue(definitionCube, "origin", Vector3f.class);
            Vector3f size = fieldValue(definitionCube, "dimensions", Vector3f.class);
            assertFloat(path + " cube " + i + " minX", origin.x(), bakedCube.minX);
            assertFloat(path + " cube " + i + " minY", origin.y(), bakedCube.minY);
            assertFloat(path + " cube " + i + " minZ", origin.z(), bakedCube.minZ);
            assertFloat(path + " cube " + i + " maxX", origin.x() + size.x(), bakedCube.maxX);
            assertFloat(path + " cube " + i + " maxY", origin.y() + size.y(), bakedCube.maxY);
            assertFloat(path + " cube " + i + " maxZ", origin.z() + size.z(), bakedCube.maxZ);
        }

        Map<String, PartDefinition> definitionChildren = definitionChildren(definition);
        Map<String, ModelPart> bakedChildren = bakedChildren(baked);
        if (!definitionChildren.keySet().equals(bakedChildren.keySet())) {
            throw new IllegalStateException(path + " child names differ between definition and bake: "
                    + definitionChildren.keySet() + " != " + bakedChildren.keySet());
        }
        for (String child : new TreeMap<>(definitionChildren).keySet()) {
            validateDefinitionAgainstBake(definitionChildren.get(child), bakedChildren.get(child), path + "/" + child);
        }
    }

    private static void assertFloat(String what, float expected, float actual) {
        if (Math.abs(expected - actual) > DEFINITION_EPSILON) {
            throw new IllegalStateException(what + " mismatch: " + expected + " != " + actual);
        }
    }

    private static void resetBakedTree(ModelPart root) {
        root.getAllParts().forEach(ModelPart::resetPose);
    }

    /**
     * Slice 4c render-instance expansion. A manifest entry may declare
     * {@code render_instances}: the parts its classic {@code renderToBuffer}
     * draws {@code count} times per frame under a per-draw transform (the
     * Rotator's blade fans, PurplePower's spoke rings). For such a model the
     * compiled side is captured PER DRAW: every {@code ModelPart.render} call
     * becomes its own cube group keyed {@code <part>__i<k>} ({@code k} the
     * draw ordinal of that part within the sample), and each draw records the
     * pose stack it ran under — {@code instance_pose}, the matrix at the part's
     * own {@code pushPose} (the model's per-draw transform), and
     * {@code draw_pose}, the matrix the cubes were compiled with. Draws are
     * attributed to parts by their UV multiset, which a pose cannot change;
     * counts are asserted against the declaration.
     */
    private static RenderInstanceContext renderInstanceContext(JsonObject spec, ModelPart bakedRoot,
                                                               Map<String, String> namesToPaths) throws Exception {
        if (!spec.has("render_instances")) {
            return null;
        }
        String id = spec.get("id").getAsString();
        Map<String, Integer> counts = new TreeMap<>();
        Map<String, Integer> expectedCounts = new TreeMap<>();
        for (Map.Entry<String, JsonElement> entry : spec.getAsJsonObject("render_instances").entrySet()) {
            String part = entry.getKey();
            JsonObject declaration = entry.getValue().getAsJsonObject();
            String path = namesToPaths.get(part);
            if (path == null) {
                throw new IllegalStateException(id + ": render_instances names unknown part " + part);
            }
            if (!path.equals("/" + part)) {
                throw new IllegalStateException(id + ": render_instances part " + part
                        + " must be a top-level part (path " + path + ")");
            }
            int count = declaration.get("count").getAsInt();
            if (count < 2) {
                throw new IllegalStateException(id + ": render_instances." + part + ".count must be at least 2");
            }
            String scope = declaration.get("step_scope").getAsString();
            if (!scope.equals("part") && !scope.equals("stack") && !scope.equals("explicit")) {
                throw new IllegalStateException(id + ": render_instances." + part + ".step_scope must be part, stack or explicit");
            }
            if (scope.equals("explicit")) {
                // The folder's gaps: the classic re-poses the part between its draws from code (the GiantRobot's
                // second leg and arm, the Crab's eight leg positions) - no pose-stack step about one axis expresses
                // that, so the declaration lists every draw's bind transform for the converter (explicit per-instance
                // transforms). The probe needs only the count; the list's length is held to it.
                JsonElement instances = declaration.get("instances");
                if (instances == null || !instances.isJsonArray() || instances.getAsJsonArray().size() != count) {
                    throw new IllegalStateException(id + ": render_instances." + part + ".instances must list exactly "
                            + count + " draws (step_scope explicit)");
                }
                if (declaration.has("group_chain")) {
                    throw new IllegalStateException(id + ": render_instances." + part + ".group_chain needs step_scope part");
                }
            }
            int expected = count;
            if (declaration.has("pinned_draw_count")) {
                // ANIM-025: the port draws the part FEWER times than the declaration until its slice's draw fix lands
                // (the Crab's renderToBuffer draws each leg part once where 1.7.10 drew it eight times). The manifest
                // pins the port's count, so the dump proves exactly that shortfall and the rig still carries every
                // declared draw; the moment the fix lands the observed count returns to the declaration, this pin
                // fails the run and is removed with the fix (the reference leg's pinned_divergences
                // rule). An undeclared shortfall still fails as before.
                expected = declaration.get("pinned_draw_count").getAsInt();
                if (expected < 1 || expected >= count) {
                    throw new IllegalStateException(id + ": render_instances." + part
                            + ".pinned_draw_count must be between 1 and count - 1");
                }
            }
            expectedCounts.put(part, expected);
            if (declaration.has("group_chain")) {
                // ENT-S-146: nested hook-spun groups (outermost first) above the clones, step_scope part only.
                if (!scope.equals("part")) {
                    throw new IllegalStateException(id + ": render_instances." + part + ".group_chain needs step_scope part");
                }
                JsonArray chain = declaration.getAsJsonArray("group_chain");
                Set<String> names = new java.util.HashSet<>();
                for (JsonElement group : chain) {
                    if (!group.isJsonPrimitive() || !group.getAsJsonPrimitive().isString() || !names.add(group.getAsString())) {
                        throw new IllegalStateException(id + ": render_instances." + part
                                + ".group_chain must be a list of distinct bone names");
                    }
                }
                if (chain.isEmpty()) {
                    throw new IllegalStateException(id + ": render_instances." + part + ".group_chain is empty");
                }
            }
            counts.put(part, count);
        }
        Map<String, ModelPart> byName = new TreeMap<>();
        collectBakedParts(bakedRoot, "", byName, namesToPaths);
        Map<String, PartSignature> signatures = new TreeMap<>();
        Map<String, String> partsByUvKey = new java.util.HashMap<>();
        for (Map.Entry<String, ModelPart> entry : byName.entrySet()) {
            String part = entry.getKey();
            List<ModelPart.Cube> cubes = bakedCubes(entry.getValue());
            if (cubes.isEmpty()) {
                if (counts.containsKey(part)) {
                    throw new IllegalStateException(id + ": render_instances part " + part + " has no cubes");
                }
                continue;
            }
            if (counts.containsKey(part) && !bakedChildren(entry.getValue()).isEmpty()) {
                throw new IllegalStateException(id + ": render_instances part " + part + " must not have children");
            }
            int[] vertexCounts = new int[cubes.size()];
            List<String> uvTokens = new ArrayList<>();
            PoseStack identity = new PoseStack();
            for (int index = 0; index < cubes.size(); index++) {
                FlatCapturingVertexConsumer reference = new FlatCapturingVertexConsumer();
                cubes.get(index).compile(identity.last(), reference, 0, 0, -1);
                vertexCounts[index] = reference.vertices.size();
                reference.vertices.forEach(vertex -> uvTokens.add(uvToken(vertex)));
            }
            String uvKey = uvKey(uvTokens);
            String clash = partsByUvKey.put(uvKey, part);
            if (clash != null) {
                throw new IllegalStateException(id + ": parts " + clash + " and " + part
                        + " emit identical UV sets; a draw could not be attributed to one of them");
            }
            signatures.put(part, new PartSignature(part, namesToPaths.get(part), uvKey, vertexCounts));
        }
        return new RenderInstanceContext(counts, expectedCounts, signatures, partsByUvKey);
    }

    private static String uvToken(CapturedVertex vertex) {
        return Float.toString(vertex.u()) + "," + Float.toString(vertex.v());
    }

    private static String uvKey(List<String> tokens) {
        List<String> sorted = new ArrayList<>(tokens);
        sorted.sort(Comparator.naturalOrder());
        return String.join(";", sorted);
    }

    /** Row-major 4x4 (row r = [m0r, m1r, m2r, m3r] in JOML column-row naming): translation is column 3. */
    private static JsonArray matrixRows(Matrix4f matrix) {
        JsonArray rows = new JsonArray();
        rows.add(floats(matrix.m00(), matrix.m10(), matrix.m20(), matrix.m30()));
        rows.add(floats(matrix.m01(), matrix.m11(), matrix.m21(), matrix.m31()));
        rows.add(floats(matrix.m02(), matrix.m12(), matrix.m22(), matrix.m32()));
        rows.add(floats(matrix.m03(), matrix.m13(), matrix.m23(), matrix.m33()));
        return rows;
    }

    private record PartSignature(String part, String path, String uvKey, int[] cubeVertexCounts) {
    }

    private static final class RenderInstanceContext {
        private final Map<String, Integer> declaredCounts;
        /** The draw count the port must show per declared part: the declaration's, or its {@code pinned_draw_count}. */
        private final Map<String, Integer> expectedCounts;
        private final Map<String, PartSignature> signatures;
        private final Map<String, String> partsByUvKey;

        private RenderInstanceContext(Map<String, Integer> declaredCounts, Map<String, Integer> expectedCounts,
                                      Map<String, PartSignature> signatures, Map<String, String> partsByUvKey) {
            this.declaredCounts = declaredCounts;
            this.expectedCounts = expectedCounts;
            this.signatures = signatures;
            this.partsByUvKey = partsByUvKey;
        }

        void capture(JsonObject sample, Object model, Set<String> hiddenBones,
                     RenderStateProbe.Observed renderState, int packedLight) {
            InstrumentedPoseStack stack = new InstrumentedPoseStack();
            // the classic chain (TEST-015): the per-draw matrices below are recorded in the entity frame, M included,
            // as the render-instance leg's geo-side bone poses are
            applyClassicChain(stack);
            DrawCapturingVertexConsumer consumer = new DrawCapturingVertexConsumer(stack, renderState);
            ((EntityModel<?>) model).renderToBuffer(stack, consumer, packedLight, 0, -1);
            if (stack.depth != 0) {
                throw new IllegalStateException("renderToBuffer left the pose stack unbalanced (depth " + stack.depth + ")");
            }
            CapturingVertexConsumer groups = new CapturingVertexConsumer();
            JsonArray draws = new JsonArray();
            Map<String, Integer> drawn = new TreeMap<>();
            for (DrawGroup group : consumer.draws) {
                List<String> uvTokens = new ArrayList<>();
                group.vertices.forEach(vertex -> uvTokens.add(uvToken(vertex)));
                String part = this.partsByUvKey.get(uvKey(uvTokens));
                if (part == null) {
                    throw new IllegalStateException("a draw of " + group.vertices.size()
                            + " vertices matches no part's UV set");
                }
                PartSignature signature = this.signatures.get(part);
                int ordinal = drawn.merge(part, 1, Integer::sum) - 1;
                Integer declared = this.declaredCounts.get(part);
                String bone;
                if (declared != null) {
                    if (ordinal >= declared) {
                        throw new IllegalStateException(part + " drawn more than its declared " + declared + " times");
                    }
                    bone = part + "__i" + ordinal;
                } else {
                    if (ordinal > 0) {
                        throw new IllegalStateException(part + " drawn twice but declares no render_instances");
                    }
                    bone = part;
                }
                int offset = 0;
                for (int cube = 0; cube < signature.cubeVertexCounts().length; cube++) {
                    int count = signature.cubeVertexCounts()[cube];
                    groups.begin(bone, signature.path(), cube,
                            declared != null ? part : null, declared != null ? ordinal : null);
                    for (CapturedVertex vertex : group.vertices.subList(offset, offset + count)) {
                        groups.addVertex(vertex.x(), vertex.y(), vertex.z(), 0, vertex.u(), vertex.v(), 0, 0,
                                vertex.normalX(), vertex.normalY(), vertex.normalZ());
                    }
                    groups.end();
                    offset += count;
                }
                if (offset != group.vertices.size()) {
                    throw new IllegalStateException(part + " draw emitted " + group.vertices.size()
                            + " vertices; its cubes compile to " + offset);
                }
                if (declared != null) {
                    JsonObject draw = new JsonObject();
                    draw.addProperty("part", part);
                    draw.addProperty("draw_index", ordinal);
                    draw.addProperty("bone", bone);
                    draw.addProperty("vertex_count", group.vertices.size());
                    draw.add("instance_pose", matrixRows(group.instancePose));
                    draw.add("draw_pose", matrixRows(group.drawPose));
                    draws.add(draw);
                }
            }
            for (Map.Entry<String, PartSignature> entry : this.signatures.entrySet()) {
                String part = entry.getKey();
                int observed = drawn.getOrDefault(part, 0);
                int expected = hiddenByPath(entry.getValue().path(), hiddenBones)
                        ? 0 : this.expectedCounts.getOrDefault(part, 1);
                if (observed != expected) {
                    String why = "";
                    if (this.declaredCounts.containsKey(part)) {
                        why = expected == this.declaredCounts.get(part)
                                ? " (declared render_instances count)"
                                : " (pinned_draw_count: the port's draw count pinned under the " + this.declaredCounts.get(part)
                                        + "-draw declaration; a change means the draw fix landed and the pin must go)";
                    }
                    throw new IllegalStateException(part + " drawn " + observed + " times; expected " + expected + why);
                }
            }
            sample.add("render_vertices", consumer.verticesJson());
            sample.add("cubes", groups.groupsJson());
            sample.add("draws", draws);
        }
    }

    /** Records the matrix at every push: the innermost push before a draw is the model's per-draw transform. */
    private static final class InstrumentedPoseStack extends PoseStack {
        private int depth;
        private int pushSerial;
        private Matrix4f lastPushPose = new Matrix4f();

        @Override
        public void pushPose() {
            super.pushPose();
            this.depth++;
            this.pushSerial++;
            this.lastPushPose = new Matrix4f(last().pose());
        }

        @Override
        public void popPose() {
            this.depth--;
            super.popPose();
        }
    }

    private static final class DrawGroup {
        private final Matrix4f instancePose;
        private final Matrix4f drawPose;
        private final List<CapturedVertex> vertices = new ArrayList<>();

        private DrawGroup(Matrix4f instancePose, Matrix4f drawPose) {
            this.instancePose = instancePose;
            this.drawPose = drawPose;
        }
    }

    /** One group per ModelPart.render call: a new push serial at a vertex opens the next draw. */
    private static final class DrawCapturingVertexConsumer implements VertexConsumer {
        private final InstrumentedPoseStack stack;
        private final List<CapturedVertex> vertices = new ArrayList<>();
        private final List<DrawGroup> draws = new ArrayList<>();
        private DrawGroup current;
        private int currentSerial = -1;

        /** ENT-S-146: the colour / light every vertex carries, observed (null: not observed). */
        private final RenderStateProbe.Observed observed;

        private DrawCapturingVertexConsumer(InstrumentedPoseStack stack, RenderStateProbe.Observed observed) {
            this.stack = stack;
            this.observed = observed;
        }

        JsonArray verticesJson() {
            JsonArray array = new JsonArray();
            this.vertices.forEach(vertex -> array.add(vertex.toJson()));
            return array;
        }

        @Override
        public void addVertex(float x, float y, float z, int color, float u, float v,
                              int packedOverlay, int packedLight,
                              float normalX, float normalY, float normalZ) {
            if (this.observed != null) {
                this.observed.see(color, packedLight);
            }
            int serial = this.stack.pushSerial;
            if (this.current == null || serial != this.currentSerial) {
                this.current = new DrawGroup(new Matrix4f(this.stack.lastPushPose),
                        new Matrix4f(this.stack.last().pose()));
                this.draws.add(this.current);
                this.currentSerial = serial;
            }
            CapturedVertex vertex = new CapturedVertex(x, y, z, u, v, normalX, normalY, normalZ);
            this.current.vertices.add(vertex);
            this.vertices.add(vertex);
        }

        @Override
        public VertexConsumer addVertex(float x, float y, float z) {
            throw new IllegalStateException("G1 capture requires the atomic VertexConsumer.addVertex overload");
        }

        @Override
        public VertexConsumer setColor(int red, int green, int blue, int alpha) {
            return this;
        }

        @Override
        public VertexConsumer setUv(float u, float v) {
            return this;
        }

        @Override
        public VertexConsumer setUv1(int u, int v) {
            return this;
        }

        @Override
        public VertexConsumer setUv2(int u, int v) {
            return this;
        }

        @Override
        public VertexConsumer setNormal(float normalX, float normalY, float normalZ) {
            return this;
        }
    }

    /** Poses the observation bake exactly as the captured bake was posed for one sample. */
    @FunctionalInterface
    private interface ShadowPose {
        void apply(Object model, ModelPart root) throws Exception;
    }

    /**
     * G2 root-order contract: observes the ACTUAL classic draw order of every full capture.
     *
     * <p>{@code ModelPart.render} (1.21.1 bytecode: {@code visible} test at offsets 1-4,
     * {@code pushPose} 32, {@code translateAndRotate} 37, the {@code skipDraw} test 41-44
     * gating {@code compile} at 58, {@code children.values()} 62-70 each drawn through
     * {@code render} at 108, {@code popPose} 115) pushes the pose stack once per drawn
     * part, so every draw is a distinct push serial of the instrumented stack. Draws are
     * attributed to parts by elimination: one {@code renderToBuffer} per cube-bearing part
     * with every OTHER part's public {@code skipDraw} set, so only that part compiles
     * cubes while the push structure - and so the serials - stays identical. No UV or
     * geometry uniqueness is assumed: superimposed or identical parts are attributed
     * exactly. A part drawn {@code n > 1} times in one capture (the Slice 4c
     * render-instance loops) is named {@code <part>__i<k>} per draw, {@code k} the draw
     * ordinal - the names the 4c capture attributes by UV set, cross-checked here.</p>
     */
    private static final class DrawOrderObserver {
        static final String SOURCE = "classic EntityModel.renderToBuffer on a second bake posed identically; "
                + "every ModelPart.render is one pose-stack push, attributed to its part by ModelPart.skipDraw "
                + "elimination (one run per cube-bearing part with every other part's skipDraw set); a part "
                + "drawn n > 1 times is named <part>__i<k> by draw ordinal";
        private final Object model;
        private final ModelPart root;
        private final Map<String, ModelPart> cubeParts;
        private final Map<String, Integer> declaredCounts;

        DrawOrderObserver(Object model, ModelPart root, Map<String, String> namesToPaths,
                          Map<String, Integer> declaredCounts) throws Exception {
            this.model = model;
            this.root = root;
            Map<String, ModelPart> byName = new TreeMap<>();
            collectBakedParts(root, "", byName, namesToPaths);
            this.cubeParts = new TreeMap<>();
            for (Map.Entry<String, ModelPart> entry : byName.entrySet()) {
                if (!bakedCubes(entry.getValue()).isEmpty()) {
                    this.cubeParts.put(entry.getKey(), entry.getValue());
                }
            }
            this.declaredCounts = declaredCounts;
        }

        /** Adds {@code draw_order} to a full capture: the drawn parts (clones per draw) in the classic order. */
        void observe(JsonObject sample, ShadowPose pose) throws Exception {
            observe(sample, pose, null, "draw_order");
        }

        /**
         * TEST-018: adds {@code draw_order_pass2} to a full capture - the parts the classic renderer's SECOND pass draws
         * ({@code drawMethod}, the model's pass method of the {@code renderToBuffer} shape), attributed by the same skipDraw
         * elimination on the same shadow bake posed identically.
         */
        void observePass(JsonObject sample, ShadowPose pose, Method drawMethod) throws Exception {
            observe(sample, pose, drawMethod, "draw_order_pass2");
        }

        private void observe(JsonObject sample, ShadowPose pose, Method drawMethod, String key) throws Exception {
            if (!sample.has("render_vertices")) {
                return;
            }
            String id = sample.get("id").getAsString();
            pose.apply(this.model, this.root);
            Map<Integer, Integer> draws = run(drawMethod);
            Map<Integer, String> owners = new TreeMap<>();
            try {
                for (String name : this.cubeParts.keySet()) {
                    this.cubeParts.values().forEach(part -> part.skipDraw = true);
                    this.cubeParts.get(name).skipDraw = false;
                    for (Map.Entry<Integer, Integer> draw : run(drawMethod).entrySet()) {
                        Integer expected = draws.get(draw.getKey());
                        if (expected == null || !expected.equals(draw.getValue())) {
                            throw new IllegalStateException(id + ": " + name + " drew " + draw.getValue()
                                    + " vertices at push " + draw.getKey() + " alone but " + expected
                                    + " with every part drawing");
                        }
                        String previous = owners.put(draw.getKey(), name);
                        if (previous != null) {
                            throw new IllegalStateException(id + ": push " + draw.getKey() + " drew both "
                                    + previous + " and " + name);
                        }
                    }
                }
            } finally {
                this.cubeParts.values().forEach(part -> part.skipDraw = false);
            }
            List<String> parts = new ArrayList<>();
            Map<String, Integer> counts = new TreeMap<>();
            for (Map.Entry<Integer, Integer> draw : draws.entrySet()) {
                String part = owners.get(draw.getKey());
                if (part == null) {
                    throw new IllegalStateException(id + ": the draw at push " + draw.getKey() + " ("
                            + draw.getValue() + " vertices) belongs to no part");
                }
                parts.add(part);
                counts.merge(part, 1, Integer::sum);
            }
            JsonArray order = new JsonArray();
            Map<String, Integer> ordinals = new TreeMap<>();
            for (String part : parts) {
                int ordinal = ordinals.merge(part, 1, Integer::sum) - 1;
                order.add(this.declaredCounts.containsKey(part) || counts.get(part) > 1
                        ? part + "__i" + ordinal : part);
            }
            if (drawMethod == null && sample.has("draws")) {
                // Slice 4c attributed the declared parts' draws by UV set; the two attributions must agree.
                List<String> byUv = new ArrayList<>();
                for (JsonElement draw : sample.getAsJsonArray("draws")) {
                    byUv.add(draw.getAsJsonObject().get("bone").getAsString());
                }
                List<String> declaredDraws = new ArrayList<>();
                for (JsonElement bone : order) {
                    String name = bone.getAsString();
                    int marker = name.lastIndexOf("__i");
                    if (marker > 0 && this.declaredCounts.containsKey(name.substring(0, marker))) {
                        declaredDraws.add(name);
                    }
                }
                if (!declaredDraws.equals(byUv)) {
                    throw new IllegalStateException(id + ": skipDraw elimination attributes the render-instance "
                            + "draws as " + declaredDraws + " but the UV attribution recorded " + byUv);
                }
            }
            sample.add(key, order);
        }

        /** One renderToBuffer (or, for the second pass, the pass method): vertex count per pose-stack push serial, in draw order. */
        private Map<Integer, Integer> run(Method drawMethod) throws Exception {
            InstrumentedPoseStack stack = new InstrumentedPoseStack();
            SerialCountingVertexConsumer consumer = new SerialCountingVertexConsumer(stack);
            if (drawMethod == null) {
                ((EntityModel<?>) this.model).renderToBuffer(stack, consumer, 0, 0, -1);
            } else {
                drawMethod.invoke(this.model, stack, consumer, 0, 0, -1);
            }
            if (stack.depth != 0) {
                throw new IllegalStateException("renderToBuffer left the pose stack unbalanced (depth " + stack.depth + ")");
            }
            return consumer.countsBySerial;
        }
    }

    /** Counts vertices per pose-stack push serial; a serial with vertices is one ModelPart.render draw. */
    private static final class SerialCountingVertexConsumer implements VertexConsumer {
        private final InstrumentedPoseStack stack;
        private final Map<Integer, Integer> countsBySerial = new LinkedHashMap<>();

        private SerialCountingVertexConsumer(InstrumentedPoseStack stack) {
            this.stack = stack;
        }

        @Override
        public void addVertex(float x, float y, float z, int color, float u, float v,
                              int packedOverlay, int packedLight,
                              float normalX, float normalY, float normalZ) {
            this.countsBySerial.merge(this.stack.pushSerial, 1, Integer::sum);
        }

        @Override
        public VertexConsumer addVertex(float x, float y, float z) {
            throw new IllegalStateException("G1 capture requires the atomic VertexConsumer.addVertex overload");
        }

        @Override
        public VertexConsumer setColor(int red, int green, int blue, int alpha) {
            return this;
        }

        @Override
        public VertexConsumer setUv(float u, float v) {
            return this;
        }

        @Override
        public VertexConsumer setUv1(int u, int v) {
            return this;
        }

        @Override
        public VertexConsumer setUv2(int u, int v) {
            return this;
        }

        @Override
        public VertexConsumer setNormal(float normalX, float normalY, float normalZ) {
            return this;
        }
    }

    private static void dumpGeo(Path manifestPath, JsonObject manifest, Path generatedDir, Path outputDir) throws Exception {
        Files.createDirectories(outputDir);
        clearGeneratedJson(outputDir, ".geo-render.json");
        // The keyframe reference leg resolves its clip and clip manifest against the repository root, as the vanilla side does.
        Path repositoryRoot = manifestPath.getParent().getParent();
        for (JsonObject spec : allSpecs(manifest)) {
            String id = spec.get("id").getAsString();
            Path geoPath = generatedDir.resolve(id + ".geo.json");
            Path animationPath = generatedDir.resolve(id + ".animation.json");
            // ENT-S-146: the candidate's render state rides in the same sidecar shape as the classic's.
            JsonObject renderState = new JsonObject();
            JsonObject dump = dumpGeoModel(manifest, spec, repositoryRoot, geoPath, animationPath, renderState);
            writeJson(outputDir.resolve(id + ".geo-render.json"), dump);
            writeJson(outputDir.resolve(id + RENDER_STATE_SUFFIX), renderState);
        }
    }

    private static JsonObject dumpGeoModel(JsonObject manifest, JsonObject spec, Path repositoryRoot,
                                           Path geoPath, Path animationPath, JsonObject renderState) throws Exception {
        Model rawModel = KeyFramesAdapter.GEO_GSON.fromJson(Files.readString(geoPath), Model.class);
        // G2 root-order contract: the generated geo carries the classic draw order under DrawOrder.KEY;
        // every fresh bake below is sorted into it through the production DrawOrder.apply, the static
        // the shipped OreSpawnGeoReplacementModel.getBakedModel calls on the cached bake.
        JsonObject geoJson = readJson(geoPath);
        List<String> drawOrder = DrawOrder.read(geoJson);
        // ENT-S-146: a translucent rig also carries the classic within-cube face order; every fresh bake
        // below applies it through the production FaceOrder.apply, exactly as the shipped model does.
        Map<String, List<List<Direction>>> faceOrder = FaceOrder.read(geoJson);
        String modelId = spec.get("id").getAsString();
        String animationKind = spec.get("animation_kind").getAsString();
        boolean productionHook = CODE_DRIVEN_KIND.equals(animationKind)
                || ENTITY_STATE_KIND.equals(animationKind);
        String candidateClass = productionHook ? spec.get("candidate_class").getAsString() : null;
        // ENT-S-146: the bind bake is as strict as the production-hook bake - a rig whose shipped descriptor
        // requires the face-order key must carry it, or the harness fails; a model without a descriptor
        // (static, or the reference-only Beaver path) is opaque-only and requires nothing.
        boolean faceOrderRequired = productionHook && S4CandidateRuntime.cubeFaceOrderRequired(candidateClass);
        G1AnimationRuntime.Evaluator evaluator = G1AnimationRuntime.evaluator(rawModel, drawOrder, faceOrder, faceOrderRequired);
        G1AnimationRuntime.EvaluatedModel bind = evaluator.bindPose();
        String candidatePath = spec.has("candidate_animation_path")
                ? spec.get("candidate_animation_path").getAsString()
                : "static_bind_pose";
        String emittedClipRole = spec.has("emitted_clip_role")
                ? spec.get("emitted_clip_role").getAsString()
                : productionHook ? "NOT_APPLICABLE_CODE_DRIVEN_MODEL" : "NOT_APPLICABLE_STATIC_MODEL";
        if (GAIT_SCALED_KIND.equals(animationKind)
                && !"REFERENCE_ONLY_NOT_RUNTIME_ACCEPTANCE".equals(emittedClipRole)) {
            throw new IllegalStateException("Animated G1 reference output must be explicitly excluded "
                    + "from runtime acceptance: " + emittedClipRole);
        }
        if (productionHook && !S4CandidateRuntime.CANDIDATE_PATH.equals(candidatePath)) {
            throw new IllegalStateException(modelId + " must declare candidate_animation_path "
                    + S4CandidateRuntime.CANDIDATE_PATH);
        }
        if (!Files.isRegularFile(animationPath)) {
            throw new IllegalStateException("Missing generated animation artifact " + animationPath);
        }

        // ENT-S-146: the render state the candidate's renderer would request, from the shipped descriptor's
        // hooks as OreSpawnGeoReplacedEntityRenderer applies them for a visible body - the render-type
        // FUNCTION (renderType(entity); null = GeckoLib's own GeoModel.getRenderType), the colour int
        // GeoRenderer.defaultRender takes (renderColor), the light EntityRenderer.getPackedLightCoords packs
        // (fullBright) - and, for the colour and light, handed to actuallyRender and observed at addVertex. The
        // hooks are evaluated with a null entity: every shipped descriptor answers them from constants; one that
        // reads its entity fails here loudly, by design (the S4CandidateRuntime doctrine). The classic model's
        // own function is instantiated beside it so the two sides' identity can be recorded.
        RenderStateProbe.Observed observed = new RenderStateProbe.Observed();
        Function<ResourceLocation, RenderType> classicFunction = RenderStateProbe.classicFunction(classicModel(spec));
        Function<ResourceLocation, RenderType> entityModelDefault = RenderStateProbe.entityModelDefault();
        int candidateColour = RenderStateProbe.GECKOLIB_WHITE;
        int candidateLight = RenderStateProbe.PROBE_LIGHT;
        boolean fullBright = false;
        JsonObject candidateRenderType;
        String colourSource;
        GeoReplacementDescriptor.RenderTransform transform = GeoReplacementDescriptor.RenderTransform.IDENTITY;
        // TEST-018 (the remainder slice, 2026-09-15): the descriptor's second pass, read without an entity like the hooks
        // below; the pass's bones are drawn by the geo side in a second capture with the pass's colour, the main capture
        // without them - exactly as the replaced renderer draws them (OreSpawnGeoReplacedEntityRenderer.SecondPassLayer).
        GeoReplacementDescriptor.SecondPass geoPass = null;
        RenderStateProbe.Observed observedPass = new RenderStateProbe.Observed();
        if (productionHook) {
            GeoReplacementDescriptor<?> descriptor = S4CandidateRuntime.instantiate(candidateClass).descriptor();
            // the constant render transform (TEST-013), read without an entity like the hooks below
            transform = descriptor.renderTransform();
            geoPass = descriptor.secondPass();
            if ((geoPass != null) != spec.has("second_pass")) {
                throw new IllegalStateException(modelId + ": the descriptor " + (geoPass != null ? "declares" : "declares no")
                        + " second pass but the manifest entry " + (spec.has("second_pass") ? "carries" : "carries no")
                        + " second_pass block; the two must agree");
            }
            Function<ResourceLocation, RenderType> own = descriptor.renderType(null);
            if (own != null) {
                candidateRenderType = RenderStateProbe.describeFunction(own, entityModelDefault);
                candidateRenderType.addProperty("source", "descriptor.renderType(entity), applied to the texture by "
                        + "OreSpawnGeoReplacedEntityRenderer.getRenderType for a visible body");
                candidateRenderType.addProperty("same_function_object_as_classic", own == classicFunction);
            } else {
                candidateRenderType = RenderStateProbe.describeGeckoLibDefault();
                candidateRenderType.addProperty("source", "descriptor.renderType(entity) returned null: GeckoLib's own "
                        + "GeoModel.getRenderType (RenderType.entityCutoutNoCull, 4.8.4 bytecode offset 1)");
                candidateRenderType.addProperty("same_function_object_as_classic", false);
            }
            candidateColour = descriptor.renderColor(null, 0.0F);
            colourSource = "descriptor.renderColor(entity, partialTick) - the colour int GeoRenderer.defaultRender takes "
                    + "(4-18) and hands to every addVertex (createVerticesOfQuad, 81); WHITE (-1) is GeckoLib's own";
            fullBright = descriptor.fullBright(null);
            candidateLight = fullBright
                    ? RenderStateProbe.packedLight(GeoReplacementDescriptor.FULL_BRIGHT_LEVEL, GeoReplacementDescriptor.FULL_BRIGHT_LEVEL)
                    : RenderStateProbe.PROBE_LIGHT;
        } else {
            candidateRenderType = RenderStateProbe.describeGeckoLibDefault();
            candidateRenderType.addProperty("source", "no shipped descriptor (a static or reference-only model): "
                    + "GeckoLib's own GeoModel.getRenderType (RenderType.entityCutoutNoCull, 4.8.4 bytecode offset 1)");
            candidateRenderType.addProperty("same_function_object_as_classic", false);
            colourSource = "no shipped descriptor: GeckoLib's own white (-1)";
        }
        candidateRenderType.add("classic", RenderStateProbe.describeFunction(classicFunction, entityModelDefault));

        JsonObject out = new JsonObject();
        out.addProperty("schema_version", 1);
        out.addProperty("probe", "static".equals(animationKind)
                ? "GeckoLib BakedModelFactory + fresh static BakedGeoModel + GeoRenderer"
                : productionHook
                        ? "GeckoLib BakedModelFactory + fresh BakedGeoModel + "
                                + "production OreSpawnGeoReplacement.pose + GeoRenderer"
                        : "GeckoLib BakedModelFactory + fresh BakedGeoModel + "
                                + "GeoModel.setCustomAnimations + GeoRenderer");
        out.addProperty("model_id", modelId);
        out.addProperty("geckolib_version", manifest.get("geckolib_version").getAsString());
        out.addProperty("geometry_sha256", sha256(Files.readAllBytes(geoPath)));
        out.addProperty("animation_sha256", sha256(Files.readAllBytes(animationPath)));
        out.addProperty("geometry_loader", "KeyFramesAdapter.GEO_GSON");
        out.addProperty("candidate_animation_path", candidatePath);
        out.addProperty("accepted_pose_source", "static".equals(animationKind)
                ? "fresh BakedGeoModel static bind pose; no controller"
                : productionHook
                        ? S4CandidateRuntime.POSE_SOURCE
                        : "fresh BakedGeoModel + GeoModel.setCustomAnimations");
        out.addProperty("fresh_baked_model_per_accepted_sample", true);
        out.addProperty("emitted_clip_role", emittedClipRole);
        out.addProperty("reference_animation_loaded_by_acceptance_runtime", false);
        out.addProperty("reference_animation_used_for_accepted_pose", false);
        out.addProperty("reference_animation_access_guard", "static".equals(animationKind)
                ? "NOT_APPLICABLE_STATIC_MODEL"
                : productionHook
                        ? "production model registers no controllers; the probe never requests the animation resource"
                        : "GeoModel.getAnimationResource throws REFERENCE_ONLY_NOT_RUNTIME_ACCEPTANCE");
        if (productionHook) {
            out.addProperty("candidate_class", candidateClass);
        }
        JsonArray boneNames = new JsonArray();
        bind.bones().keySet().forEach(boneNames::add);
        out.add("bone_names", boneNames);
        if (!transform.isIdentity()) {
            out.add("render_transform", renderTransformJson(transform));
        }
        // G2: the order read from the geo, and the traversal a fresh bake actually has after DrawOrder.apply.
        out.add("bone_draw_order", names(drawOrder));
        out.add("baked_bone_order", names(DrawOrder.traversal(bind.model())));
        if (!faceOrder.isEmpty()) {
            // ENT-S-146: the order read from the geo, and the quad order a fresh bake actually has after FaceOrder.apply.
            out.add("cube_face_order", faceOrderJson(faceOrder));
            out.add("baked_cube_face_order", faceOrderJson(FaceOrder.faceOrders(bind.model())));
        }
        // Slice 4c: for an expanded rig every bone's world transform is recorded (the entity frame, BONE_POSES_FIELD)
        // so the parity tool can compare the group/clone composition with the classic model's measured per-draw pose
        // stack, captured under the classic chain.
        // The hierarchy form (the FK slice): a rig whose declared chain children are parented bones records every
        // bone's world matrix as well, for the chain-link leg.
        boolean recordBonePoses = spec.has("render_instances") || spec.has("hierarchy");

        JsonArray samples = new JsonArray();
        SampleRequest bindRequest = new SampleRequest("bind", 0.0F, 0.0F, true, false);
        boolean partialTickInput = partialTickInput(spec);
        samples.add(captureGeoSample(bindRequest, bind, productionHook, recordBonePoses, observed, candidateColour, candidateLight,
                transform, geoPass, observedPass));

        float limbSwing = spec.get("limb_swing").getAsFloat();
        float netHeadYaw = optionalFloat(spec, "net_head_yaw");
        float headPitch = optionalFloat(spec, "head_pitch");
        List<SampleRequest> requests = sampleRequests(spec);
        List<JsonObject> states = entityStates(spec);
        if (ENTITY_STATE_KIND.equals(animationKind)) {
            for (JsonObject state : states) {
                for (SampleRequest request : requests) {
                    ProbeSubject subject = new ProbeSubject(state);
                    G1AnimationRuntime.EvaluatedModel candidate = S4CandidateRuntime.evaluateProductionHook(
                            rawModel, drawOrder, faceOrder, candidateClass,
                            new S4CandidateRuntime.Inputs(request.ageTicks(), limbSwing,
                                    request.limbSwingAmount(), netHeadYaw, headPitch, request.partialTick()),
                            subject);
                    JsonObject sample = captureGeoSample(stateRequest(state, request), candidate, true,
                            recordBonePoses, observed, candidateColour, candidateLight, transform, geoPass, observedPass);
                    sample.add("entity_state", state.deepCopy());
                    sample.add("subject_after", subject.after());
                    if (partialTickInput) {
                        sample.addProperty("partial_tick", request.partialTick());
                    }
                    samples.add(sample);
                }
            }
        } else {
            // The keyframe reference leg (Phase G, the controller's return): the shipped phase-locked layers
            // over the species' regenerated clip, sampled at every request beside the code-driven candidate.
            KeyframeLeg.Prepared keyframeLeg = KeyframeLeg.declared(spec)
                    ? KeyframeLeg.prepare(manifest, spec, repositoryRoot, evaluator)
                    : null;
            for (SampleRequest request : requests) {
                G1AnimationRuntime.EvaluatedModel candidate;
                if ("static".equals(animationKind)) {
                    candidate = evaluator.bindPose();
                } else if ("geckolib_custom_animation_code".equals(candidatePath)) {
                    candidate = evaluator.evaluateBeaverCodeDriven(
                            request.ageTicks(), request.limbSwingAmount());
                } else if (productionHook) {
                    candidate = S4CandidateRuntime.evaluateProductionHook(rawModel, drawOrder, faceOrder, candidateClass,
                            new S4CandidateRuntime.Inputs(request.ageTicks(), limbSwing,
                                    request.limbSwingAmount(), netHeadYaw, headPitch, request.partialTick()), null);
                } else {
                    throw new IllegalStateException("Unsupported G1 candidate animation path " + candidatePath);
                }
                JsonObject sample = captureGeoSample(request, candidate, productionHook, recordBonePoses, observed,
                        candidateColour, candidateLight, transform, geoPass, observedPass);
                if (partialTickInput) {
                    sample.addProperty("partial_tick", request.partialTick());
                }
                if (keyframeLeg != null) {
                    sample.add(KeyframeLeg.SAMPLE_FIELD, keyframeLeg.classicRotations(request));
                    JsonObject wrap = keyframeLeg.wrapProvenance(request);
                    if (wrap != null) {
                        sample.add(KeyframeLeg.WRAP_FIELD, wrap);
                    }
                }
                samples.add(sample);
            }
            if (keyframeLeg != null) {
                out.add(KeyframeLeg.KEY, keyframeLeg.report(requests));
            }
        }
        out.add("samples", samples);

        // ENT-S-146: the candidate side's sidecar, the same shape as the classic's.
        renderState.addProperty("schema_version", 1);
        renderState.addProperty("model_id", modelId);
        renderState.addProperty("side", "candidate");
        renderState.add("render_type", candidateRenderType);
        JsonObject colour = observed.colourJson();
        colour.addProperty("handed_to_actually_render", candidateColour);
        colour.addProperty("source", colourSource + "; handed to actuallyRender and observed at addVertex over the full captures");
        renderState.add("vertex_color", colour);
        JsonObject light = observed.lightJson();
        light.addProperty("handed_to_actually_render", candidateLight);
        light.addProperty("full_bright", fullBright);
        light.addProperty("source", productionHook
                ? (fullBright
                        ? "descriptor.fullBright(entity) true: both light levels FULL_BRIGHT_LEVEL, packed as EntityRenderer"
                                + ".getPackedLightCoords packs them (LightTexture.pack), handed to actuallyRender and observed at addVertex"
                        : "descriptor.fullBright(entity) false: the world's light, which the probe stands in for with its own light, "
                                + "handed to actuallyRender and observed at addVertex")
                : "no shipped descriptor: the probe's light, handed to actuallyRender and observed at addVertex");
        renderState.add("packed_light", light);
        if (geoPass != null) {
            // TEST-018: the candidate's second pass - the descriptor's render-type function (its identity against the classic
            // model's own static field, the manifest's classic_render_type_field), the colour and light every vertex of the
            // pass carried, the bones - beside the main pass's.
            JsonObject pass = new JsonObject();
            Function<ResourceLocation, RenderType> classicPassFunction = ClassicSecondPass.classicFunction(spec, classicModel(spec).getClass());
            JsonObject passType = RenderStateProbe.describeFunction(geoPass.renderType(), entityModelDefault);
            passType.addProperty("source", "descriptor.secondPass().renderType(), applied to the texture by "
                    + "OreSpawnGeoReplacedEntityRenderer.SecondPassLayer for the pass's bones after the opaque pass");
            passType.addProperty("same_function_object_as_classic", geoPass.renderType() == classicPassFunction);
            passType.add("classic", RenderStateProbe.describeFunction(classicPassFunction, entityModelDefault));
            pass.add("render_type", passType);
            JsonObject passColour = observedPass.colourJson();
            passColour.addProperty("handed_to_second_pass", geoPass.color());
            passColour.addProperty("source", "descriptor.secondPass().color() - the colour int the SecondPassLayer hands "
                    + "reRender for the pass, observed at addVertex over the pass captures");
            pass.add("vertex_color", passColour);
            JsonObject passLight = observedPass.lightJson();
            passLight.addProperty("source", "the capture's packed light, handed to the pass and observed at addVertex");
            pass.add("packed_light", passLight);
            pass.add("bones", names(geoPass.bones()));
            renderState.add("second_pass", pass);
        }
        return out;
    }

    /** The classic model named by the manifest entry, on a fresh bake of its own layer (for the render-state identity check). */
    private static Object classicModel(JsonObject spec) throws Exception {
        Class<?> modelClass = Class.forName(spec.get("class").getAsString());
        LayerDefinition layer = (LayerDefinition) layerFactory(modelClass, spec).invoke(null);
        return newClassicModel(modelClass, spec, layer.bakeRoot());
    }

    /** The static LayerDefinition factory the entry names ({@code layer_factory}; {@code createBodyLayer} by default). */
    private static Method layerFactory(Class<?> modelClass, JsonObject spec) throws Exception {
        String name = spec.has("layer_factory") ? spec.get("layer_factory").getAsString() : "createBodyLayer";
        Method layerFactory = modelClass.getDeclaredMethod(name);
        if (!Modifier.isStatic(layerFactory.getModifiers())
                || !LayerDefinition.class.isAssignableFrom(layerFactory.getReturnType())) {
            throw new IllegalStateException(modelClass.getName() + "." + name + " is not a static LayerDefinition factory");
        }
        layerFactory.setAccessible(true);
        return layerFactory;
    }

    /**
     * The classic model on the given root: {@code (ModelPart)} by default, or {@code (ModelPart, <declared
     * arguments>)} when the entry carries {@code constructor_arguments} (each JSON number or boolean converted to
     * the declared primitive; a wrong count or type is refused, never guessed).
     */
    private static Object newClassicModel(Class<?> modelClass, JsonObject spec, ModelPart root) throws Exception {
        Object model = constructClassicModel(modelClass, spec, root);
        if (model instanceof EntityModel<?> entityModel) {
            // The remainder slice (2026-09-15): EntityModel.young defaults to TRUE and the classic renderer sets it to
            // entity.isBaby() before every draw (LivingEntityRenderer.render 92-100, 21.1.223); the probe's subject is never a
            // baby, so the classic model stands at the adult value from construction - read only by AgeableListModel
            // .renderToBuffer (the bipeds' HumanoidModel: the baby head and body scaling), ignored by every model that
            // overrides renderToBuffer, so no other entry's capture moves.
            entityModel.young = false;
        }
        return model;
    }

    private static Object constructClassicModel(Class<?> modelClass, JsonObject spec, ModelPart root) throws Exception {
        if (!spec.has("constructor_arguments")) {
            Constructor<?> constructor = modelClass.getDeclaredConstructor(ModelPart.class);
            constructor.setAccessible(true);
            return constructor.newInstance(root);
        }
        JsonArray declared = spec.getAsJsonArray("constructor_arguments");
        for (Constructor<?> constructor : modelClass.getDeclaredConstructors()) {
            Class<?>[] types = constructor.getParameterTypes();
            if (types.length != declared.size() + 1 || types[0] != ModelPart.class) {
                continue;
            }
            Object[] arguments = new Object[types.length];
            arguments[0] = root;
            for (int i = 0; i < declared.size(); i++) {
                arguments[i + 1] = constructorArgument(types[i + 1], declared.get(i), modelClass, i);
            }
            constructor.setAccessible(true);
            return constructor.newInstance(arguments);
        }
        throw new IllegalStateException(modelClass.getName() + " has no (ModelPart, " + declared.size()
                + " more) constructor for constructor_arguments " + declared);
    }

    private static Object constructorArgument(Class<?> type, JsonElement value, Class<?> modelClass, int index) {
        if (type == float.class || type == Float.class) {
            return value.getAsFloat();
        }
        if (type == double.class || type == Double.class) {
            return value.getAsDouble();
        }
        if (type == int.class || type == Integer.class) {
            return value.getAsInt();
        }
        if (type == long.class || type == Long.class) {
            return value.getAsLong();
        }
        if (type == boolean.class || type == Boolean.class) {
            return value.getAsBoolean();
        }
        throw new IllegalStateException(modelClass.getName() + ": constructor_arguments[" + index + "] " + value
                + " cannot be converted to " + type.getName());
    }

    /**
     * bone -> one array per cube of the six direction names in draw order (the FaceOrder.KEY shape). Emitted
     * in bone-name order: {@code FaceOrder.read} hands back {@code Map.copyOf}, whose iteration order the JDK
     * salts per JVM ({@code ImmutableCollections.SALT32L}), which made two runs of the probe write PurplePower's
     * {@code cube_face_order} in two orders; the parity tool compares the objects as maps, so the order
     * carries no meaning.
     */
    private static JsonObject faceOrderJson(Map<String, List<List<Direction>>> faceOrder) {
        JsonObject out = new JsonObject();
        new TreeMap<>(faceOrder).forEach((bone, cubes) -> {
            JsonArray cubeArray = new JsonArray();
            for (List<Direction> faces : cubes) {
                JsonArray faceArray = new JsonArray();
                faces.forEach(face -> faceArray.add(face.getSerializedName()));
                cubeArray.add(faceArray);
            }
            out.add(bone, cubeArray);
        });
        return out;
    }

    /** Internal bone rotations read back in classic terms: {@code (-x, -y, z)} (OreSpawnGeoReplacement's basis facts, TEST-015). */
    private static JsonObject javaRotations(Map<String, float[]> internalRotations) {
        JsonObject rotations = new JsonObject();
        internalRotations.forEach((name, rotation) -> rotations.add(name,
                floats(-rotation[0], -rotation[1], rotation[2])));
        return rotations;
    }

    private static JsonObject captureGeoSample(
            SampleRequest request, G1AnimationRuntime.EvaluatedModel evaluated, boolean productionHook,
            boolean recordBonePoses, RenderStateProbe.Observed observed, int colour, int packedLight,
            GeoReplacementDescriptor.RenderTransform transform, GeoReplacementDescriptor.SecondPass secondPass,
            RenderStateProbe.Observed observedPass) {
        JsonObject sample = new JsonObject();
        sample.addProperty("id", request.id());
        sample.addProperty("capture_kind", request.fullCapture() ? "full" : "transform_only");
        sample.addProperty("dense_transform_sample", request.denseTransformSample());
        sample.addProperty("age_ticks", request.ageTicks());
        sample.addProperty("limb_swing_amount", request.limbSwingAmount());
        sample.add("java_rotations", javaRotations(evaluated.internalRotations()));
        if (productionHook) {
            sample.add("java_positions", javaPositions(evaluated.bones()));
            Set<String> hidden = new java.util.TreeSet<>();
            evaluated.bones().forEach((name, bone) -> {
                if (bone.isHidden()) {
                    hidden.add(name);
                }
            });
            // as the hook left them: the second pass's hiding below is the renderer's, not the hook's
            sample.add("hidden_bones", names(hidden));
        }
        if (!request.fullCapture()) {
            return sample;
        }

        CapturingVertexConsumer consumer = new CapturingVertexConsumer(observed);
        CapturingGeoRenderer renderer = new CapturingGeoRenderer(consumer, recordBonePoses);
        PoseStack poseStack = new PoseStack();
        // THE SEAM'S CHAIN (the class javadoc; TEST-015): what OreSpawnGeoReplacedEntityRenderer.applyRotations adds
        // after the yaw - the constant render transform's SLOT form (TEST-013: the renderer's own applySlot, the
        // classic form conjugated through the seam frame F = M) and the seam's height compensation - then GeckoLib's own
        // translate(0, 0.01, 0) (actuallyRender 727) and the bake as the baker built it. Nothing normalises the capture
        // into ModelPart space any more: the classic side carries its own chain, and the geometry leg compares the two
        // in the entity frame. A wrong slot conjugation, a wrong frame or a missing compensation fails it at every sample.
        transform.applySlot(poseStack);
        poseStack.translate(0.0F, GeoReplacementDescriptor.RenderTransform.SEAM_HEIGHT_COMPENSATION, 0.0F);
        poseStack.translate(0.0F, 0.01F, 0.0F);
        // TEST-018: a rig with a second pass draws the pass's bones only in it - hidden for the main capture, exactly as
        // OreSpawnGeoReplacedEntityRenderer.preRender hides them for the opaque pass; the hook's own hidden flags are kept
        // and put back afterwards.
        Map<String, boolean[]> passFlags = new TreeMap<>();
        if (secondPass != null) {
            for (String name : secondPass.bones()) {
                GeoBone bone = evaluated.bones().get(name);
                if (bone == null) {
                    throw new IllegalStateException("the second pass names a bone the rig lacks: " + name);
                }
                passFlags.put(name, new boolean[] {bone.isHidden(), bone.isHidingChildren()});
                bone.setHidden(true);
            }
        }
        // ENT-S-146: the light and colour the production renderer would hand actuallyRender (the descriptor's
        // fullBright / renderColor; GeckoLib's own for a rig without hooks) - GeoRenderer.renderCube passes
        // them to every addVertex, where the capture observes them. They touch no captured coordinate.
        renderer.actuallyRender(poseStack, null, evaluated.model(), null, null, consumer,
                true, 0.0F, packedLight, 0, colour);

        JsonArray cubes = consumer.groupsJson();
        if (secondPass != null) {
            // THE SECOND PASS on the geo side: the pass's bones alone (every other bone's cubes hidden, its children still
            // traversed - the SecondPassLayer's form), the same chain, the pass's colour and the capture's light, on their own
            // consumer: render_vertices_pass2 / draw_order_pass2 beside the main capture's, the cubes joined for the geometry
            // and surface legs (which compare the whole rig, pass by pass being the draw-order, render-state and visual legs').
            Map<String, boolean[]> flags = new TreeMap<>();
            evaluated.bones().forEach((name, bone) -> flags.put(name, new boolean[] {bone.isHidden(), bone.isHidingChildren()}));
            Set<String> passBones = new java.util.TreeSet<>(secondPass.bones());
            evaluated.bones().forEach((name, bone) -> {
                if (passBones.contains(name)) {
                    bone.setHidden(false);
                } else {
                    bone.setHidden(true);
                    bone.setChildrenHidden(false);
                }
            });
            CapturingVertexConsumer passConsumer = new CapturingVertexConsumer(observedPass);
            CapturingGeoRenderer passRenderer = new CapturingGeoRenderer(passConsumer, false);
            PoseStack passStack = new PoseStack();
            transform.applySlot(passStack);
            passStack.translate(0.0F, GeoReplacementDescriptor.RenderTransform.SEAM_HEIGHT_COMPENSATION, 0.0F);
            passStack.translate(0.0F, 0.01F, 0.0F);
            passRenderer.actuallyRender(passStack, null, evaluated.model(), null, null, passConsumer,
                    true, 0.0F, packedLight, 0, secondPass.color());
            evaluated.bones().forEach((name, bone) -> {
                boolean[] before = flags.get(name);
                bone.setHidden(before[0]);
                bone.setChildrenHidden(before[1]);
            });
            passFlags.forEach((name, before) -> {
                GeoBone bone = evaluated.bones().get(name);
                bone.setHidden(before[0]);
                bone.setChildrenHidden(before[1]);
            });
            passConsumer.groupsJson().forEach(cubes::add);
            sample.add("render_vertices_pass2", passConsumer.verticesJson());
            sample.add("draw_order_pass2", passRenderer.drawOrderJson());
        }
        sample.add("cubes", cubes);
        sample.add("render_vertices", consumer.verticesJson());
        // G2: the bones whose cubes GeoRenderer emitted, in the order it emitted them.
        sample.add("draw_order", renderer.drawOrderJson());
        if (recordBonePoses) {
            sample.add(BONE_POSES_FIELD, renderer.bonePosesJson());
        }
        return sample;
    }

    /**
     * The geo probe's per-bone world matrices (a render-instance or hierarchy rig), IN THE ENTITY FRAME: for every bone,
     * the pose stack GeckoLib's {@code renderRecursively} holds at the bone's cubes, closed by the bake map B (classic
     * absolute corner -> bake corner), so that {@code bone_pose . T(classic pivot / 16)} maps a classic pivot-local corner
     * exactly where the classic chain's {@code M . cumulative} maps it - the render-instance and chain-link legs' relation.
     */
    static final String BONE_POSES_FIELD = "bone_poses_entity_frame";

    private static List<JsonObject> allSpecs(JsonObject manifest) {
        List<JsonObject> specs = new ArrayList<>();
        for (JsonElement element : manifest.getAsJsonArray("models")) {
            specs.add(element.getAsJsonObject());
        }
        if (manifest.has("fixtures")) {
            for (JsonElement element : manifest.getAsJsonArray("fixtures")) {
                specs.add(element.getAsJsonObject());
            }
        }
        return specs;
    }

    private static void clearGeneratedJson(Path outputDir, String suffix) throws IOException {
        try (var entries = Files.list(outputDir)) {
            for (Path entry : entries.filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().endsWith(suffix)).toList()) {
                Files.delete(entry);
            }
        }
    }

    private static List<Float> amplitudes(JsonObject spec) {
        List<Float> values = new ArrayList<>();
        if (spec.has("limb_swing_amount_samples")) {
            for (JsonElement value : spec.getAsJsonArray("limb_swing_amount_samples")) {
                values.add(value.getAsFloat());
            }
        } else {
            values.add(spec.get("limb_swing_amount").getAsFloat());
        }
        return values;
    }

    private static List<SampleRequest> sampleRequests(JsonObject spec) {
        List<SampleRequest> requests = new ArrayList<>();
        boolean amplitudeMatrix = spec.has("limb_swing_amount_samples");
        double period = spec.get("loop_period_age_ticks").getAsDouble();
        for (float amount : amplitudes(spec)) {
            for (JsonElement fractionElement : spec.getAsJsonArray("sample_fractions")) {
                double fraction = fractionElement.getAsDouble();
                String id = amplitudeMatrix
                        ? amplitudeSampleId(amount, fraction) : sampleId(fraction);
                requests.add(new SampleRequest(
                        id, (float) (period * fraction), amount, true, false));
            }
        }

        if (spec.has("dense_transform_sample_count")) {
            int count = spec.get("dense_transform_sample_count").getAsInt();
            double offset = spec.get("dense_transform_probe_offset").getAsDouble();
            if (count != 593 || !(offset > 0.0 && offset < 1.0)) {
                throw new IllegalStateException("G1 dense probe contract must use N=593 and an interior offset");
            }
            for (float amount : amplitudes(spec)) {
                String prefix = "a" + amplitudeToken(amount) + "_dense_";
                requests.add(new SampleRequest(
                        prefix + "start", 0.0F, amount, false, true));
                for (int index = 0; index < count; index++) {
                    double fraction = (index + offset) / count;
                    requests.add(new SampleRequest(
                            prefix + String.format(Locale.ROOT, "%03d", index),
                            (float) (period * fraction), amount, false, true));
                }
                requests.add(new SampleRequest(
                        prefix + "end", (float) period, amount, false, true));
            }
        }

        if (KeyframeLeg.declared(spec)) {
            // the revised form point 5: the wrap sample (T - eps vs 0 + eps) per frequency group, on both sides.
            requests.addAll(KeyframeLeg.wrapRequests(spec, amplitudes(spec)));
        }

        long distinctIds = requests.stream().map(SampleRequest::id).distinct().count();
        if (distinctIds != requests.size()) {
            throw new IllegalStateException("G1 sample schedule contains duplicate IDs");
        }
        return requests;
    }

    private static List<BakeRequest> animationBakeRequests(JsonObject spec) {
        int denseCount = spec.get("dense_transform_sample_count").getAsInt();
        int subdivisions = spec.get("animation_bake_subdivisions_per_dense_interval").getAsInt();
        int intervals = Math.multiplyExact(denseCount, subdivisions);
        double period = spec.get("loop_period_age_ticks").getAsDouble();
        List<BakeRequest> requests = new ArrayList<>(intervals + 1);
        for (int index = 0; index <= intervals; index++) {
            double fraction = index / (double) intervals;
            requests.add(new BakeRequest(
                    String.format(Locale.ROOT, "key_%05d", index),
                    fraction, (float) (period * fraction)));
        }
        return requests;
    }

    private static String sampleId(double fraction) {
        if (Math.abs(fraction) < 1.0E-9) {
            return "t0";
        }
        if (Math.abs(fraction - 0.25) < 1.0E-9) {
            return "t_quarter";
        }
        if (Math.abs(fraction - 0.5) < 1.0E-9) {
            return "t_half";
        }
        if (Math.abs(fraction - 0.75) < 1.0E-9) {
            return "t_three_quarter";
        }
        if (Math.abs(fraction - 1.0) < 1.0E-9) {
            return "t_end";
        }
        return String.format(Locale.ROOT, "t_%1.6f", fraction).replace('.', '_');
    }

    private static String amplitudeSampleId(float limbSwingAmount, double fraction) {
        return "a" + amplitudeToken(limbSwingAmount) + "_" + sampleId(fraction);
    }

    static String amplitudeToken(float limbSwingAmount) {
        return BigDecimal.valueOf(limbSwingAmount).stripTrailingZeros().toPlainString()
                .replace('-', 'n')
                .replace('.', '_');
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

    /**
     * One sample of the schedule; {@code partialTick} the frame's partial tick the seam carries since the remainder slice
     * ({@code PoseInputs.partialTick}) - the fractional part of the sample's age, vanilla's own identity {@code
     * ageInTicks = tickCount + partialTick}, handed to the classic side's seven-float {@code poseFrom} where a
     * model declares one and to the hook through {@code S4CandidateRuntime.Inputs}; a hook that does not read it is unchanged
     * by it, and the dumps record it only for an entry that declares {@code partial_tick_input}.
     */
    record SampleRequest(String id, float ageTicks, float limbSwingAmount,
                         boolean fullCapture, boolean denseTransformSample, float partialTick) {
        SampleRequest(String id, float ageTicks, float limbSwingAmount, boolean fullCapture, boolean denseTransformSample) {
            this(id, ageTicks, limbSwingAmount, fullCapture, denseTransformSample, partialTickOf(ageTicks));
        }

        /** The fractional part of an age in ticks: {@code age - floor(age)}, in [0, 1). */
        static float partialTickOf(float ageTicks) {
            return ageTicks - (float) Math.floor(ageTicks);
        }
    }

    /** An entry that declares {@code partial_tick_input}: its hook reads the seam's partial tick, so every sample records it. */
    private static boolean partialTickInput(JsonObject spec) {
        return spec.has("partial_tick_input") && spec.get("partial_tick_input").getAsBoolean();
    }

    private record BakeRequest(String id, double fraction, float ageTicks) {
    }

    private static String classSha256(Class<?> type) throws IOException {
        String resource = "/" + type.getName().replace('.', '/') + ".class";
        try (InputStream stream = type.getResourceAsStream(resource)) {
            if (stream == null) {
                throw new IOException("Unable to read compiled class resource " + resource);
            }
            return sha256(stream.readAllBytes());
        }
    }

    private static String sha256(byte[] bytes) {
        try {
            return java.util.HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException(exception);
        }
    }

    private static JsonArray floats(float... values) {
        JsonArray array = new JsonArray();
        for (float value : values) {
            array.add(value == 0.0F ? 0.0F : value);
        }
        return array;
    }

    private static Field findField(Class<?> type, String name) {
        Class<?> current = type;
        while (current != null) {
            try {
                Field field = current.getDeclaredField(name);
                field.setAccessible(true);
                return field;
            } catch (NoSuchFieldException ignored) {
                current = current.getSuperclass();
            }
        }
        throw new IllegalStateException("Missing field " + type.getName() + "." + name);
    }

    private static Object fieldValue(Object target, String name) throws IllegalAccessException {
        return findField(target.getClass(), name).get(target);
    }

    private static <T> T fieldValue(Object target, String name, Class<T> type) throws IllegalAccessException {
        return type.cast(fieldValue(target, name));
    }

    private static int fieldInt(Object target, String name) throws IllegalAccessException {
        return findField(target.getClass(), name).getInt(target);
    }

    private static float fieldFloat(Object target, String name) throws IllegalAccessException {
        return findField(target.getClass(), name).getFloat(target);
    }

    private static boolean fieldBoolean(Object target, String name) throws IllegalAccessException {
        return findField(target.getClass(), name).getBoolean(target);
    }

    @SuppressWarnings("unchecked")
    private static List<CubeDefinition> definitionCubes(PartDefinition definition) throws IllegalAccessException {
        return (List<CubeDefinition>) fieldValue(definition, "cubes");
    }

    @SuppressWarnings("unchecked")
    private static Map<String, PartDefinition> definitionChildren(PartDefinition definition)
            throws IllegalAccessException {
        return (Map<String, PartDefinition>) fieldValue(definition, "children");
    }

    @SuppressWarnings("unchecked")
    private static List<ModelPart.Cube> bakedCubes(ModelPart part) throws IllegalAccessException {
        return (List<ModelPart.Cube>) fieldValue(part, "cubes");
    }

    @SuppressWarnings("unchecked")
    private static Map<String, ModelPart> bakedChildren(ModelPart part) throws IllegalAccessException {
        return (Map<String, ModelPart>) fieldValue(part, "children");
    }

    private static final class CapturingGeoRenderer implements GeoRenderer<GeoAnimatable> {
        /**
         * THE BAKE MAP B (TEST-015): a classic absolute corner (ModelPart space, blocks) to the bake corner the converter
         * and GeckoLib's baker produce, {@code (x, y, z) -> (-x, 1.5 - y, z)} = {@code scale(-1, -1, 1) . translate(0, -1.5, 0)}
         * (the converter's Bedrock convention plus the baker's x negation). Right-multiplied onto the pose stack at a bone's
         * cubes it yields the bone's world transform as a map from classic absolute space into the entity frame; the
         * classic pivot-local corner then needs one more {@code T(pivot / 16)}, the legs' relation.
         */
        private static final Matrix4f BAKE_OF_CLASSIC =
                new Matrix4f().scale(-1.0F, -1.0F, 1.0F).translate(0.0F, -1.5F, 0.0F);
        private final CapturingVertexConsumer consumer;
        private final Map<String, JsonArray> bonePoses;
        private final List<String> drawOrder = new ArrayList<>();

        private CapturingGeoRenderer(CapturingVertexConsumer consumer, boolean recordBonePoses) {
            this.consumer = consumer;
            this.bonePoses = recordBonePoses ? new TreeMap<>() : null;
        }

        JsonArray drawOrderJson() {
            return names(this.drawOrder);
        }

        JsonObject bonePosesJson() {
            JsonObject out = new JsonObject();
            this.bonePoses.forEach(out::add);
            return out;
        }

        @Override
        public GeoModel<GeoAnimatable> getGeoModel() {
            return null;
        }

        @Override
        public GeoAnimatable getAnimatable() {
            return null;
        }

        @Override
        public void renderCubesOfBone(PoseStack poseStack, GeoBone bone, VertexConsumer buffer,
                                      int packedLight, int packedOverlay, int color) {
            if (this.bonePoses != null) {
                // renderRecursively has run RenderUtil.prepMatrixForBone: the stack holds the seam's chain * (bone's
                // cumulative transform) in bake space; closed by B it maps classic absolute space into the entity frame.
                if (this.bonePoses.put(bone.getName(),
                        matrixRows(new Matrix4f(poseStack.last().pose()).mul(BAKE_OF_CLASSIC))) != null) {
                    throw new IllegalStateException("bone " + bone.getName() + " rendered twice");
                }
            }
            if (bone.isHidden()) {
                return;
            }
            List<GeoCube> cubes = bone.getCubes();
            if (!cubes.isEmpty()) {
                this.drawOrder.add(bone.getName());
            }
            for (int index = 0; index < cubes.size(); index++) {
                poseStack.pushPose();
                consumer.begin(bone.getName(), bone.getName(), index);
                renderCube(poseStack, cubes.get(index), buffer, packedLight, packedOverlay, color);
                consumer.end();
                poseStack.popPose();
            }
        }

        /**
         * ENT-S-161: the seam's cube path - GeckoLib's {@code renderCube} minus {@code
         * RenderUtil.fixInvertedFlatCube} - through the same static the shipped replacement renderer uses
         * ({@link TrueNormalCubeRenderer}), so the captured normals are the normals the seam draws.
         */
        @Override
        public void renderCube(PoseStack poseStack, GeoCube cube, VertexConsumer buffer,
                               int packedLight, int packedOverlay, int color) {
            TrueNormalCubeRenderer.render(this, poseStack, cube, buffer, packedLight, packedOverlay, color);
        }

        @Override
        public void fireCompileRenderLayersEvent() {
        }

        @Override
        public boolean firePreRenderEvent(PoseStack poseStack, BakedGeoModel model,
                                          MultiBufferSource bufferSource, float partialTick, int packedLight) {
            return true;
        }

        @Override
        public void firePostRenderEvent(PoseStack poseStack, BakedGeoModel model,
                                        MultiBufferSource bufferSource, float partialTick, int packedLight) {
        }

        @Override
        public void updateAnimatedTextureFrame(GeoAnimatable animatable) {
        }
    }

    private static final class CapturingVertexConsumer implements VertexConsumer {
        private final List<CubeVertices> groups = new ArrayList<>();
        private final List<CapturedVertex> vertices = new ArrayList<>();
        /** ENT-S-146: the colour / light every vertex carries, observed (null: not observed). */
        private final RenderStateProbe.Observed observed;
        private CubeVertices current;

        CapturingVertexConsumer() {
            this(null);
        }

        CapturingVertexConsumer(RenderStateProbe.Observed observed) {
            this.observed = observed;
        }

        void begin(String boneName, String path, int cubeIndex) {
            begin(boneName, path, cubeIndex, null, null);
        }

        void begin(String boneName, String path, int cubeIndex, String sourcePart, Integer drawIndex) {
            if (this.current != null) {
                throw new IllegalStateException("Nested vertex capture groups");
            }
            this.current = new CubeVertices(boneName, path, cubeIndex, sourcePart, drawIndex);
        }

        void end() {
            if (this.current == null) {
                throw new IllegalStateException("No active vertex capture group");
            }
            if (this.current.vertices.size() % 4 != 0) {
                throw new IllegalStateException(this.current.boneName + " cube " + this.current.cubeIndex
                        + " emitted " + this.current.vertices.size() + " vertices (not quads)");
            }
            this.groups.add(this.current);
            this.current = null;
        }

        JsonArray groupsJson() {
            if (this.current != null) {
                throw new IllegalStateException("Unclosed vertex capture group");
            }
            this.groups.sort(Comparator.comparing((CubeVertices group) -> group.boneName)
                    .thenComparingInt(group -> group.cubeIndex));
            JsonArray array = new JsonArray();
            this.groups.forEach(group -> array.add(group.toJson()));
            return array;
        }

        JsonArray verticesJson() {
            JsonArray array = new JsonArray();
            this.vertices.forEach(vertex -> array.add(vertex.toJson()));
            return array;
        }

        @Override
        public void addVertex(float x, float y, float z, int color, float u, float v,
                              int packedOverlay, int packedLight,
                              float normalX, float normalY, float normalZ) {
            if (this.current == null) {
                throw new IllegalStateException("Vertex emitted outside a cube capture group");
            }
            if (this.observed != null) {
                this.observed.see(color, packedLight);
            }
            CapturedVertex vertex = new CapturedVertex(x, y, z, u, v, normalX, normalY, normalZ);
            this.current.vertices.add(vertex);
            this.vertices.add(vertex);
        }

        @Override
        public VertexConsumer addVertex(float x, float y, float z) {
            throw new IllegalStateException("G1 capture requires the atomic VertexConsumer.addVertex overload");
        }

        @Override
        public VertexConsumer setColor(int red, int green, int blue, int alpha) {
            return this;
        }

        @Override
        public VertexConsumer setUv(float u, float v) {
            return this;
        }

        @Override
        public VertexConsumer setUv1(int u, int v) {
            return this;
        }

        @Override
        public VertexConsumer setUv2(int u, int v) {
            return this;
        }

        @Override
        public VertexConsumer setNormal(float normalX, float normalY, float normalZ) {
            return this;
        }
    }

    private static final class FlatCapturingVertexConsumer implements VertexConsumer {
        private final List<CapturedVertex> vertices = new ArrayList<>();
        /** ENT-S-146: the colour / light every vertex carries, observed (null: not observed). */
        private final RenderStateProbe.Observed observed;

        FlatCapturingVertexConsumer() {
            this(null);
        }

        FlatCapturingVertexConsumer(RenderStateProbe.Observed observed) {
            this.observed = observed;
        }

        JsonArray verticesJson() {
            JsonArray array = new JsonArray();
            this.vertices.forEach(vertex -> array.add(vertex.toJson()));
            return array;
        }

        @Override
        public void addVertex(float x, float y, float z, int color, float u, float v,
                              int packedOverlay, int packedLight,
                              float normalX, float normalY, float normalZ) {
            if (this.observed != null) {
                this.observed.see(color, packedLight);
            }
            this.vertices.add(new CapturedVertex(x, y, z, u, v, normalX, normalY, normalZ));
        }

        @Override
        public VertexConsumer addVertex(float x, float y, float z) {
            throw new IllegalStateException("G1 capture requires the atomic VertexConsumer.addVertex overload");
        }

        @Override
        public VertexConsumer setColor(int red, int green, int blue, int alpha) {
            return this;
        }

        @Override
        public VertexConsumer setUv(float u, float v) {
            return this;
        }

        @Override
        public VertexConsumer setUv1(int u, int v) {
            return this;
        }

        @Override
        public VertexConsumer setUv2(int u, int v) {
            return this;
        }

        @Override
        public VertexConsumer setNormal(float normalX, float normalY, float normalZ) {
            return this;
        }
    }

    private static final class CubeVertices {
        private final String boneName;
        private final String path;
        private final int cubeIndex;
        private final String sourcePart;
        private final Integer drawIndex;
        private final List<CapturedVertex> vertices = new ArrayList<>();

        private CubeVertices(String boneName, String path, int cubeIndex, String sourcePart, Integer drawIndex) {
            this.boneName = boneName;
            this.path = path;
            this.cubeIndex = cubeIndex;
            this.sourcePart = sourcePart;
            this.drawIndex = drawIndex;
        }

        JsonObject toJson() {
            JsonObject out = new JsonObject();
            out.addProperty("bone", this.boneName);
            out.addProperty("path", this.path);
            out.addProperty("cube_index", this.cubeIndex);
            if (this.sourcePart != null) {
                // Slice 4c render-instance clone: which part's draw this group is.
                out.addProperty("source_part", this.sourcePart);
                out.addProperty("draw_index", this.drawIndex);
            }
            JsonArray vertexArray = new JsonArray();
            this.vertices.forEach(vertex -> vertexArray.add(vertex.toJson()));
            out.add("vertices", vertexArray);
            return out;
        }
    }

    private record CapturedVertex(float x, float y, float z, float u, float v,
                                  float normalX, float normalY, float normalZ) {
        JsonObject toJson() {
            JsonObject out = new JsonObject();
            out.add("position", floats(this.x, this.y, this.z));
            out.add("uv", floats(this.u, this.v));
            out.add("normal", floats(this.normalX, this.normalY, this.normalZ));
            return out;
        }
    }
}
