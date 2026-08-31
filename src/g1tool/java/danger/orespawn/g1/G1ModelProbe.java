package danger.orespawn.g1;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
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
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
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
            dumpGeo(manifest, Path.of(args[2]), Path.of(args[3]));
        } else {
            throw new IllegalArgumentException("Unknown mode: " + mode);
        }
    }

    private static void dumpVanilla(Path manifestPath, JsonObject manifest, Path outputDir) throws Exception {
        Files.createDirectories(outputDir);
        clearGeneratedJson(outputDir, ".compiled.json");
        Path repositoryRoot = manifestPath.getParent().getParent();

        for (JsonObject spec : allSpecs(manifest)) {
            JsonObject dump = dumpVanillaModel(repositoryRoot, spec);
            writeJson(outputDir.resolve(spec.get("id").getAsString() + ".compiled.json"), dump);
        }
    }

    private static JsonObject dumpVanillaModel(Path repositoryRoot, JsonObject spec) throws Exception {
        String id = spec.get("id").getAsString();
        Class<?> modelClass = Class.forName(spec.get("class").getAsString());
        Method layerFactory = modelClass.getDeclaredMethod("createBodyLayer");
        if (!Modifier.isStatic(layerFactory.getModifiers())
                || !LayerDefinition.class.isAssignableFrom(layerFactory.getReturnType())) {
            throw new IllegalStateException(modelClass.getName() + ".createBodyLayer is not a static LayerDefinition factory");
        }
        layerFactory.setAccessible(true);

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
        if (texture.getWidth() != textureWidth || texture.getHeight() != textureHeight) {
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

        Map<String, String> namesToPaths = new TreeMap<>();
        JsonObject definition = dumpDefinitionPart(definitionRoot, null, "", new float[]{0, 0, 0}, namesToPaths);
        out.add("definition", definition);
        JsonArray boneNames = new JsonArray();
        namesToPaths.keySet().forEach(boneNames::add);
        out.add("bone_names", boneNames);

        Constructor<?> constructor = modelClass.getDeclaredConstructor(ModelPart.class);
        constructor.setAccessible(true);
        Object model = constructor.newInstance(bakedRoot);
        Method setupAnim = findSetupAnim(modelClass);

        JsonArray samples = new JsonArray();
        resetBakedTree(bakedRoot);
        samples.add(captureVanillaSample(
                new SampleRequest("bind", 0.0F, 0.0F, true, false),
                model, bakedRoot, namesToPaths));

        float limbSwing = spec.get("limb_swing").getAsFloat();
        for (SampleRequest request : sampleRequests(spec)) {
            resetBakedTree(bakedRoot);
            setupAnim.invoke(model, null, limbSwing, request.limbSwingAmount(),
                    request.ageTicks(), 0.0F, 0.0F);
            samples.add(captureVanillaSample(request, model, bakedRoot, namesToPaths));
        }
        out.add("samples", samples);

        JsonArray animationBakeSamples = new JsonArray();
        if (!"static".equals(spec.get("animation_kind").getAsString())) {
            for (BakeRequest request : animationBakeRequests(spec)) {
                resetBakedTree(bakedRoot);
                setupAnim.invoke(model, null, limbSwing, 1.0F,
                        request.ageTicks(), 0.0F, 0.0F);
                JsonObject sample = new JsonObject();
                sample.addProperty("id", request.id());
                sample.addProperty("fraction", request.fraction());
                sample.addProperty("age_ticks", request.ageTicks());
                sample.add("transforms", captureModelPartTransforms(bakedRoot, namesToPaths));
                animationBakeSamples.add(sample);
            }
        }
        out.add("animation_bake_samples", animationBakeSamples);
        return out;
    }

    private static Method findSetupAnim(Class<?> modelClass) {
        return Arrays.stream(modelClass.getDeclaredMethods())
                .filter(method -> method.getName().equals("setupAnim"))
                .filter(method -> method.getParameterCount() == 6)
                .filter(method -> !method.isBridge())
                .findFirst()
                .map(method -> {
                    method.setAccessible(true);
                    return method;
                })
                .orElseThrow(() -> new IllegalStateException("No concrete setupAnim method on " + modelClass.getName()));
    }

    private static JsonObject captureVanillaSample(SampleRequest request, Object model, ModelPart root,
                                                    Map<String, String> namesToPaths) throws Exception {
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
        FlatCapturingVertexConsumer renderConsumer = new FlatCapturingVertexConsumer();
        ((EntityModel<?>) model).renderToBuffer(
                new PoseStack(), renderConsumer, 0, 0, -1);
        sample.add("render_vertices", renderConsumer.verticesJson());

        CapturingVertexConsumer consumer = new CapturingVertexConsumer();
        root.visit(new PoseStack(), (pose, path, index, cube) -> {
            String boneName = boneNameForPath(path, namesToPaths);
            consumer.begin(boneName, path, index);
            cube.compile(pose, consumer, 0, 0, -1);
            consumer.end();
        });
        sample.add("cubes", consumer.groupsJson());
        return sample;
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

    private static void dumpGeo(JsonObject manifest, Path generatedDir, Path outputDir) throws Exception {
        Files.createDirectories(outputDir);
        clearGeneratedJson(outputDir, ".geo-render.json");
        for (JsonObject spec : allSpecs(manifest)) {
            String id = spec.get("id").getAsString();
            Path geoPath = generatedDir.resolve(id + ".geo.json");
            Path animationPath = generatedDir.resolve(id + ".animation.json");
            JsonObject dump = dumpGeoModel(manifest, spec, geoPath, animationPath);
            writeJson(outputDir.resolve(id + ".geo-render.json"), dump);
        }
    }

    private static JsonObject dumpGeoModel(JsonObject manifest, JsonObject spec,
                                           Path geoPath, Path animationPath) throws Exception {
        Model rawModel = KeyFramesAdapter.GEO_GSON.fromJson(Files.readString(geoPath), Model.class);
        G1AnimationRuntime.Evaluator evaluator = G1AnimationRuntime.evaluator(rawModel);
        G1AnimationRuntime.EvaluatedModel bind = evaluator.bindPose();
        String animationKind = spec.get("animation_kind").getAsString();
        String candidatePath = spec.has("candidate_animation_path")
                ? spec.get("candidate_animation_path").getAsString()
                : "static_bind_pose";
        String emittedClipRole = spec.has("emitted_clip_role")
                ? spec.get("emitted_clip_role").getAsString()
                : "NOT_APPLICABLE_STATIC_MODEL";
        if (!"static".equals(animationKind)
                && !"REFERENCE_ONLY_NOT_RUNTIME_ACCEPTANCE".equals(emittedClipRole)) {
            throw new IllegalStateException("Animated G1 reference output must be explicitly excluded "
                    + "from runtime acceptance: " + emittedClipRole);
        }
        if (!Files.isRegularFile(animationPath)) {
            throw new IllegalStateException("Missing generated animation artifact " + animationPath);
        }

        JsonObject out = new JsonObject();
        out.addProperty("schema_version", 1);
        out.addProperty("probe", "static".equals(animationKind)
                ? "GeckoLib BakedModelFactory + fresh static BakedGeoModel + GeoRenderer"
                : "GeckoLib BakedModelFactory + fresh BakedGeoModel + "
                        + "GeoModel.setCustomAnimations + GeoRenderer");
        out.addProperty("model_id", spec.get("id").getAsString());
        out.addProperty("geckolib_version", manifest.get("geckolib_version").getAsString());
        out.addProperty("geometry_sha256", sha256(Files.readAllBytes(geoPath)));
        out.addProperty("animation_sha256", sha256(Files.readAllBytes(animationPath)));
        out.addProperty("geometry_loader", "KeyFramesAdapter.GEO_GSON");
        out.addProperty("candidate_animation_path", candidatePath);
        out.addProperty("accepted_pose_source", "static".equals(animationKind)
                ? "fresh BakedGeoModel static bind pose; no controller"
                : "fresh BakedGeoModel + GeoModel.setCustomAnimations");
        out.addProperty("fresh_baked_model_per_accepted_sample", true);
        out.addProperty("emitted_clip_role", emittedClipRole);
        out.addProperty("reference_animation_loaded_by_acceptance_runtime", false);
        out.addProperty("reference_animation_used_for_accepted_pose", false);
        out.addProperty("reference_animation_access_guard", "static".equals(animationKind)
                ? "NOT_APPLICABLE_STATIC_MODEL"
                : "GeoModel.getAnimationResource throws REFERENCE_ONLY_NOT_RUNTIME_ACCEPTANCE");
        JsonArray boneNames = new JsonArray();
        bind.bones().keySet().forEach(boneNames::add);
        out.add("bone_names", boneNames);

        JsonArray samples = new JsonArray();
        SampleRequest bindRequest = new SampleRequest("bind", 0.0F, 0.0F, true, false);
        samples.add(captureGeoSample(bindRequest, bind));

        for (SampleRequest request : sampleRequests(spec)) {
            G1AnimationRuntime.EvaluatedModel candidate;
            if ("static".equals(animationKind)) {
                candidate = evaluator.bindPose();
            } else if ("geckolib_custom_animation_code".equals(candidatePath)) {
                candidate = evaluator.evaluateBeaverCodeDriven(
                        request.ageTicks(), request.limbSwingAmount());
            } else {
                throw new IllegalStateException("Unsupported G1 candidate animation path " + candidatePath);
            }
            samples.add(captureGeoSample(request, candidate));
        }
        out.add("samples", samples);
        return out;
    }

    private static JsonObject javaRotations(Map<String, float[]> internalRotations) {
        JsonObject rotations = new JsonObject();
        internalRotations.forEach((name, rotation) -> rotations.add(name,
                floats(-rotation[0], rotation[1], -rotation[2])));
        return rotations;
    }

    private static JsonObject captureGeoSample(
            SampleRequest request, G1AnimationRuntime.EvaluatedModel evaluated) {
        JsonObject sample = new JsonObject();
        sample.addProperty("id", request.id());
        sample.addProperty("capture_kind", request.fullCapture() ? "full" : "transform_only");
        sample.addProperty("dense_transform_sample", request.denseTransformSample());
        sample.addProperty("age_ticks", request.ageTicks());
        sample.addProperty("limb_swing_amount", request.limbSwingAmount());
        sample.add("java_rotations", javaRotations(evaluated.internalRotations()));
        if (!request.fullCapture()) {
            return sample;
        }

        CapturingVertexConsumer consumer = new CapturingVertexConsumer();
        CapturingGeoRenderer renderer = new CapturingGeoRenderer(consumer);
        PoseStack poseStack = new PoseStack();
        // Bedrock geometry is Y-up around the 24px baseline. This fixed,
        // bytecode-derived normalization maps it into ModelPart's Y-down space.
        poseStack.translate(0.0, 1.5, 0.0);
        poseStack.scale(1.0F, -1.0F, 1.0F);
        renderer.actuallyRender(poseStack, null, evaluated.model(), null, null, consumer,
                true, 0.0F, 0, 0, -1);

        sample.add("cubes", consumer.groupsJson());
        sample.add("render_vertices", consumer.verticesJson());
        return sample;
    }

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

    private static String amplitudeToken(float limbSwingAmount) {
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

    private record SampleRequest(String id, float ageTicks, float limbSwingAmount,
                                 boolean fullCapture, boolean denseTransformSample) {
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
        private final CapturingVertexConsumer consumer;

        private CapturingGeoRenderer(CapturingVertexConsumer consumer) {
            this.consumer = consumer;
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
            if (bone.isHidden()) {
                return;
            }
            List<GeoCube> cubes = bone.getCubes();
            for (int index = 0; index < cubes.size(); index++) {
                poseStack.pushPose();
                consumer.begin(bone.getName(), bone.getName(), index);
                GeoRenderer.super.renderCube(poseStack, cubes.get(index), buffer,
                        packedLight, packedOverlay, color);
                consumer.end();
                poseStack.popPose();
            }
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
        private CubeVertices current;

        void begin(String boneName, String path, int cubeIndex) {
            if (this.current != null) {
                throw new IllegalStateException("Nested vertex capture groups");
            }
            this.current = new CubeVertices(boneName, path, cubeIndex);
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

        JsonArray verticesJson() {
            JsonArray array = new JsonArray();
            this.vertices.forEach(vertex -> array.add(vertex.toJson()));
            return array;
        }

        @Override
        public void addVertex(float x, float y, float z, int color, float u, float v,
                              int packedOverlay, int packedLight,
                              float normalX, float normalY, float normalZ) {
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
        private final List<CapturedVertex> vertices = new ArrayList<>();

        private CubeVertices(String boneName, String path, int cubeIndex) {
            this.boneName = boneName;
            this.path = path;
            this.cubeIndex = cubeIndex;
        }

        JsonObject toJson() {
            JsonObject out = new JsonObject();
            out.addProperty("bone", this.boneName);
            out.addProperty("path", this.path);
            out.addProperty("cube_index", this.cubeIndex);
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
