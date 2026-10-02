package danger.orespawn.g1;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import danger.orespawn.client.armour.ArmourGeometry;
import danger.orespawn.client.armour.ArmourModel;
import danger.orespawn.client.armour.ArmourPieces;
import danger.orespawn.client.armour.ArmourStyleClient;
import danger.orespawn.client.armour.ArmourStyleConfig;
import net.minecraft.client.model.ArmorStandArmorModel;
import net.minecraft.client.model.HumanoidArmorModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.ZombieVillagerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.ZombieVillager;
import org.joml.Quaternionf;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

/**
 * The modern armour style's models where the build's JVM can load the client's model classes (MOD-039). Every set's
 * four pieces are built from {@code assets/orespawn/armour_geo/} by the client's own {@link ArmourGeometry} and taken
 * down the path the game takes: vanilla's armour layer shows the slot's parts on its armour model (the helmet the head
 * and the hat, the chestplate the body and both arms, the leggings the body and both legs, the boots both legs), the
 * piece follows it ({@link ArmourModel#follow}) and NeoForge copies the pose and those flags over
 * (ClientHooks.copyModelProperties). Then:
 * <ul>
 *   <li>every bone of the geometry is one of the slot's shown parts (a cube elsewhere would never be drawn), the hat
 *       holds no cubes;</li>
 *   <li>the piece is drawn into a recording vertex consumer, as the armour layer draws it, and the drawn vertices' UVs
 *       are compared one by one with the sequence worked out here from the file alone: the parts in HumanoidModel's
 *       order, each bone's unrotated cubes then its rotated ones, each cube's six faces in vanilla's order on the box UV
 *       (the size rounded down, or the cube's uv_size), each face's corners as vanilla assigns them (reversed on a
 *       mirrored cube);</li>
 *   <li>every face of an unrotated cube is turned as vanilla's box UV turns it: along which axis, and which way, u and
 *       v run on the drawn face (so a corner given the wrong UV, a face drawn upside down or a mirror lost shows);</li>
 *   <li>every vertex carries the light, overlay and colour the layer passed, and drawn on a turned pose (on the player
 *       and, fitted, on a zombie villager) every face's normal is a unit vector pointing out of its cube;</li>
 *   <li>each shown part holds the cubes it draws (a renderer that draws a model's parts itself finds them);</li>
 *   <li>each shown part's first cube, the piece's base box, is drawn exactly where vanilla draws the wearer's own armour
 *       box for that part (vanilla's renderToBuffer of the wearer's armour model with that part alone shown): on the
 *       player, an armour stand, a zombie villager (its helmet box 2 higher, its body and legs grown 0.1 over its robe),
 *       a guard-shaped model (a humanoid mesh with its helmet box at y -10, as Guard Villagers' guards wear) and a
 *       piglin (its outer armour at 1.02), grown and as babies; and never shrunk: a drowned, whose outer armour vanilla
 *       bakes at the inner deformation, has its outer pieces drawn at the player's size.</li>
 * </ul>
 * Then: a wearer GeckoLib draws (a GeoAnimatable) keeps the classic look; the modern texture goes only to the wearer and
 * stack the model hook last handed the modern model; and the client config writes its default file as on a first
 * start (armourStyle = modern), which only a client ever does. Exit 1 on any failure.
 */
public final class ArmourModelProbe {
    private static final EquipmentSlot[] SLOTS = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS,
            EquipmentSlot.FEET};
    /** HumanoidModel's drawing order (head first, then the body parts) with the geometry's bone names. */
    private static final String[][] ORDER = {{"head", "head"}, {"body", "body"}, {"right_arm", "rightarm"},
            {"left_arm", "leftarm"}, {"right_leg", "rightleg"}, {"left_leg", "leftleg"}, {"hat", "hat"}};
    /** What the layer passes, each value distinct so a swap shows (light: block 7, sky 15; overlay u 3, v 10). */
    private static final int LIGHT = 0x00F00070, OVERLAY = 0x000A0003, COLOR = 0xFF406080;

    private ArmourModelProbe() {
    }

    public static void main(String[] args) throws Exception {
        Path dir = Path.of(args[0]);
        HumanoidModel<LivingEntity> outer = vanilla(1.0F);
        HumanoidModel<LivingEntity> inner = vanilla(0.5F);
        // each wearer's armour models, {inner, outer}, as vanilla bakes them, then the models whose boxes the pieces'
        // base boxes must be drawn on: the wearer's own, but a drowned's outer pieces stay at the player's size (both
        // its layers are baked at the inner deformation, and the fit never shrinks)
        Map<String, HumanoidModel<?>[]> wearers = new LinkedHashMap<>();
        wearers.put("the player", new HumanoidModel<?>[]{inner, outer, inner, outer});
        HumanoidModel<?> standInner = stand(0.5F), standOuter = stand(1.0F);
        wearers.put("an armour stand", new HumanoidModel<?>[]{standInner, standOuter, standInner, standOuter});
        HumanoidModel<?> villagerInner = villager(0.5F), villagerOuter = villager(1.0F);
        wearers.put("a zombie villager", new HumanoidModel<?>[]{villagerInner, villagerOuter, villagerInner, villagerOuter});
        HumanoidModel<?> guardInner = guard(0.5F), guardOuter = guard(1.0F);
        wearers.put("a guard-shaped model", new HumanoidModel<?>[]{guardInner, guardOuter, guardInner, guardOuter});
        HumanoidModel<?> piglinInner = vanilla(0.5F), piglinOuter = vanilla(1.02F);
        wearers.put("a piglin", new HumanoidModel<?>[]{piglinInner, piglinOuter, piglinInner, piglinOuter});
        wearers.put("a drowned", new HumanoidModel<?>[]{vanilla(0.5F), vanilla(0.5F), inner, outer});
        int pieces = 0, vertices = 0, boxes = 0, failures = 0;
        for (String set : ArmourStyleClient.SETS) {
            for (EquipmentSlot slot : SLOTS) {
                String piece = ArmourPieces.piece(slot);
                String name = set + "_" + piece;
                JsonObject geometry = JsonParser.parseString(Files.readString(dir.resolve(name + ".json"),
                        StandardCharsets.UTF_8)).getAsJsonObject();
                Set<String> shown = shown(slot);
                JsonObject bones = geometry.getAsJsonObject("bones");
                for (String bone : bones.keySet()) {
                    String part = partOf(bone);
                    if (part == null || !shown.contains(part) || part.equals("hat")) {
                        System.out.printf("ARMOUR MODELS FAIL: %s has cubes on %s, which the %s slot does not draw%n",
                                name, bone, piece);
                        failures++;
                    }
                }
                ArmourModel model = new ArmourModel(ArmourGeometry.build(geometry));
                Recorder drawn = draw(model, slot == EquipmentSlot.LEGS ? inner : outer, slot, false, null,
                        new PoseStack());
                float tw = geometry.get("texture_width").getAsFloat(), th = geometry.get("texture_height").getAsFloat();
                List<float[]> expected = expectedUvs(geometry, shown);
                List<int[]> faces = expectedFaces(geometry, shown);
                if (drawn.uvs.size() != expected.size()) {
                    System.out.printf("ARMOUR MODELS FAIL: %s drew %d vertices, the file gives %d%n", name,
                            drawn.uvs.size(), expected.size());
                    failures++;
                } else {
                    for (int i = 0; i < expected.size(); i++) {
                        float[] e = expected.get(i), d = drawn.uvs.get(i);
                        if (Math.abs(e[0] - d[0] * tw) > 1e-3 || Math.abs(e[1] - d[1] * th) > 1e-3) {
                            System.out.printf("ARMOUR MODELS FAIL: %s vertex %d drawn at uv (%.3f, %.3f), the box UV "
                                    + "puts it at (%.3f, %.3f)%n", name, i, d[0] * tw, d[1] * th, e[0], e[1]);
                            failures++;
                            break;
                        }
                    }
                }
                if (drawn.uvs.size() == expected.size()) {
                    String wrong = orientation(drawn, faces, tw, th);
                    if (wrong != null) {
                        System.out.printf("ARMOUR MODELS FAIL: %s %s%n", name, wrong);
                        failures++;
                    }
                }
                String passed = passedOn(drawn);
                if (passed != null) {
                    System.out.printf("ARMOUR MODELS FAIL: %s %s%n", name, passed);
                    failures++;
                }
                for (String wearer : List.of("the player", "a zombie villager")) {
                    HumanoidModel<?> original = wearers.get(wearer)[slot == EquipmentSlot.LEGS ? 0 : 1];
                    String wrong = normals(draw(model, original, slot, false, null, turned()));
                    if (wrong != null) {
                        System.out.printf("ARMOUR MODELS FAIL: %s on %s, on a turned pose: %s%n", name, wearer, wrong);
                        failures++;
                    }
                }
                Map<String, ModelPart> modelParts = parts(model);
                for (String part : shown) {
                    if (part.equals("hat")) continue;
                    int[] held = new int[1];
                    modelParts.get(part).visit(new PoseStack(), (pose, path, index, cube) -> held[0]++);
                    int drawnCubes = model.cubeCount(modelParts.get(part));
                    if (held[0] != drawnCubes) {
                        System.out.printf("ARMOUR MODELS FAIL: %s's %s holds %d cubes and draws %d%n", name, part,
                                held[0], drawnCubes);
                        failures++;
                    }
                }
                for (Map.Entry<String, HumanoidModel<?>[]> wearer : wearers.entrySet()) {
                    HumanoidModel<?> original = wearer.getValue()[slot == EquipmentSlot.LEGS ? 0 : 1];
                    HumanoidModel<?> target = wearer.getValue()[slot == EquipmentSlot.LEGS ? 2 : 3];
                    for (boolean young : new boolean[]{false, true}) {
                        for (String part : shown) {
                            if (part.equals("hat")) continue;
                            Recorder ours = draw(model, original, slot, young, part, new PoseStack());
                            Recorder theirs = drawVanilla(target, young, part);
                            String wrong = ours.positions.size() < 24 ? "draws no cube"
                                    : sameBox(extent(ours.positions, 0, 24),
                                    extent(theirs.positions, 0, theirs.positions.size()));
                            if (wrong != null) {
                                System.out.printf("ARMOUR MODELS FAIL: %s's base box on %s's %s (%s) %s%n", name,
                                        wearer.getKey(), part, young ? "baby" : "grown", wrong);
                                failures++;
                            }
                            boxes++;
                        }
                    }
                }
                vertices += drawn.uvs.size();
                pieces++;
            }
        }
        // GeckoLib's item armour layer draws the material's classic texture whatever the item says, so its wearers
        // (always GeoAnimatable) keep the classic look; any other wearer does not
        GeoAnimatable geckoLibMob = new GeoAnimatable() {
            @Override
            public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
            }

            @Override
            public AnimatableInstanceCache getAnimatableInstanceCache() {
                return null;
            }

            @Override
            public double getTick(Object object) {
                return 0;
            }
        };
        if (!ArmourPieces.drawnByGeckoLib(geckoLibMob) || ArmourPieces.drawnByGeckoLib(new Object())
                || ArmourPieces.drawnByGeckoLib(null)) {
            System.out.println("ARMOUR MODELS FAIL: a GeoAnimatable wearer is not told apart from the others");
            failures++;
        }
        // the model hook records, in the modern style, every wearer and stack it is asked about, the classic model handed
        // as well (a piece whose geometry failed builds none), so the modern texture goes only with the modern model: to
        // the wearer and stack last handed it (an armour layer asks for the model, then the texture), never to a caller
        // that asks for the texture alone; in the classic style, without a wearer and for a mob GeckoLib draws it hands
        // the classic model and neither builds nor records anything
        Object wearer = new Object(), otherWearer = new Object(), stack = new Object(), otherStack = new Object();
        Object built = new Object();
        int[] builds = {0};
        Supplier<Object> modernModel = () -> {
            builds[0]++;
            return built;
        };
        Supplier<Object> failedModel = () -> {
            builds[0]++;
            return null;
        };
        boolean paired = ArmourPieces.handModel(true, wearer, stack, modernModel) == built
                && ArmourPieces.issuedFor(wearer, stack) && !ArmourPieces.issuedFor(otherWearer, stack)
                && !ArmourPieces.issuedFor(wearer, otherStack) && !ArmourPieces.issuedFor(null, null);
        paired &= ArmourPieces.handModel(true, otherWearer, otherStack, modernModel) == built
                && !ArmourPieces.issuedFor(wearer, stack) && ArmourPieces.issuedFor(otherWearer, otherStack);
        paired &= ArmourPieces.handModel(true, wearer, stack, failedModel) == null
                && !ArmourPieces.issuedFor(wearer, stack) && ArmourPieces.askedFor(wearer, stack)
                && !ArmourPieces.askedFor(otherWearer, otherStack) && builds[0] == 3;
        paired &= ArmourPieces.handModel(false, otherWearer, otherStack, modernModel) == null
                && ArmourPieces.handModel(true, null, otherStack, modernModel) == null
                && ArmourPieces.handModel(true, geckoLibMob, otherStack, modernModel) == null
                && builds[0] == 3 && ArmourPieces.askedFor(wearer, stack);
        // a call without a wearer can never match, so its model is the classic one too
        paired &= !ArmourPieces.modern("queen", EquipmentSlot.HEAD, null);
        if (!paired) {
            System.out.println("ARMOUR MODELS FAIL: the modern texture is not tied to the modern model handed for the "
                    + "same wearer and stack");
            failures++;
        }
        // MOD-042: a caller that asks for the texture alone (Doggy Talents Next's dog armour) gets the set's dog texture
        // in the modern style with modern.dogArmour on, and never with the style classic or the key off, for a wearer and
        // stack the model hook was last asked about (the classic model handed above, as for a piece whose geometry
        // failed, or the modern one), for a mob GeckoLib draws or without a wearer; the same stack on another wearer, or
        // the same wearer with another stack, is not the pair asked about
        Object dogWearer = new Object(), dogStack = new Object();
        boolean dog = ArmourPieces.dogTexture(true, true, dogWearer, dogStack)
                && !ArmourPieces.dogTexture(false, true, dogWearer, dogStack)
                && !ArmourPieces.dogTexture(true, false, dogWearer, dogStack)
                && !ArmourPieces.dogTexture(true, true, wearer, stack)
                && !ArmourPieces.dogTexture(true, true, geckoLibMob, dogStack)
                && !ArmourPieces.dogTexture(true, true, null, dogStack)
                && ArmourPieces.dogTexture(true, true, otherWearer, stack)
                && ArmourPieces.dogTexture(true, true, wearer, otherStack);
        ArmourPieces.handModel(true, dogWearer, dogStack, modernModel);
        dog &= !ArmourPieces.dogTexture(true, true, dogWearer, dogStack)
                && ArmourPieces.dogTexture(true, true, wearer, stack);
        if (!dog) {
            System.out.println("ARMOUR MODELS FAIL: the dog texture is not handed exactly to a caller that asks for the "
                    + "texture alone in the modern style with modern.dogArmour on");
            failures++;
        }
        // Doggy Talents Next's dogs are told apart by their type's namespace: the model hook hands them the classic
        // model, unrecorded, since that mod draws whatever model it gets with the texture it keeps per item
        boolean doggy = ArmourPieces.doggyTalentsType(ResourceLocation.fromNamespaceAndPath("doggytalents", "dog"))
                && !ArmourPieces.doggyTalentsType(ResourceLocation.withDefaultNamespace("wolf"))
                && !ArmourPieces.doggyTalentsType(ResourceLocation.fromNamespaceAndPath("orespawn", "dog"))
                && !ArmourPieces.doggyTalentsType(null) && !ArmourPieces.doggyTalentsDog(new Object())
                && !ArmourPieces.doggyTalentsDog(null);
        if (!doggy) {
            System.out.println("ARMOUR MODELS FAIL: a Doggy Talents Next dog is not told apart by its type's namespace");
            failures++;
        }
        // the client config writes its default file as a first start does: every value missing, so the spec tests
        // each against its acceptable values and fills in the default
        try {
            CommentedConfig config = CommentedConfig.inMemory();
            ArmourStyleConfig.SPEC.correct(config);
            Object style = config.get(List.of("client", "armourStyle"));
            if (!ArmourStyleConfig.MODERN.equals(style)) {
                System.out.printf("ARMOUR MODELS FAIL: the client config's default armourStyle is %s, not modern%n", style);
                failures++;
            }
        } catch (RuntimeException exception) {
            System.out.printf("ARMOUR MODELS FAIL: the client config cannot write its default file: %s%n", exception);
            failures++;
        }
        if (failures > 0) {
            System.out.printf("ARMOUR MODELS FAIL: %d failure(s) over %d pieces%n", failures, pieces);
            System.exit(1);
        }
        System.out.printf("ARMOUR MODELS PASS: %d pieces drawn as the armour layer draws them (%d vertices, each on the "
                + "UV the file's box UV gives it, every unrotated face turned as vanilla turns it, every normal a unit "
                + "vector out of its cube on a turned pose, the light, overlay and colour handed on, only the slot's "
                + "parts, each part holding the cubes it draws); each piece's base box drawn exactly on the wearer's own "
                + "armour box as vanilla draws it, on the player, an armour stand, a zombie villager, a guard-shaped "
                + "model and a piglin, and never shrunk (a drowned's outer pieces at the player's size), grown and as "
                + "babies (%d boxes); the modern texture handed only with the modern model; the dog texture only to a "
                + "caller that asks for the texture alone, in the modern style with modern.dogArmour on, never where the "
                + "model hook was asked, the classic model handed too; GeckoLib's wearers kept classic; Doggy Talents "
                + "Next's dogs told apart; the client config's default file written (armourStyle = modern)%n", pieces,
                vertices, boxes);
    }

    /**
     * The piece drawn the game's way: vanilla's visibility on the original, the piece following it, NeoForge's copy;
     * with {@code only} set, that part alone stays shown. The renderer sets young every frame (a model starts young).
     */
    private static Recorder draw(ArmourModel model, HumanoidModel<?> original, EquipmentSlot slot, boolean young,
                                 String only, PoseStack poseStack) {
        original.young = young;
        Set<String> shown = shown(slot);
        for (Map.Entry<String, ModelPart> part : parts(original).entrySet()) {
            part.getValue().visible = shown.contains(part.getKey());
        }
        model.follow(original, slot);
        copyProperties(original, model);
        for (Map.Entry<String, ModelPart> part : parts(model).entrySet()) {
            part.getValue().visible = parts(original).get(part.getKey()).visible
                    && (only == null || only.equals(part.getKey()));
        }
        Recorder recorder = new Recorder();
        model.renderToBuffer(poseStack, recorder, LIGHT, OVERLAY, COLOR);
        return recorder;
    }

    /** Vanilla's own drawing of a wearer's armour model with one part shown: the classic piece's box on that part. */
    private static Recorder drawVanilla(HumanoidModel<?> original, boolean young, String only) {
        original.young = young;
        for (Map.Entry<String, ModelPart> part : parts(original).entrySet()) {
            part.getValue().visible = part.getKey().equals(only);
        }
        Recorder recorder = new Recorder();
        original.renderToBuffer(new PoseStack(), recorder, LIGHT, OVERLAY, COLOR);
        return recorder;
    }

    /** A pose turned about all three axes and moved, so a normal left untransformed points the wrong way. */
    private static PoseStack turned() {
        PoseStack poseStack = new PoseStack();
        poseStack.translate(0.3F, -0.2F, 0.5F);
        poseStack.mulPose(new Quaternionf().rotationXYZ(0.4F, 1.1F, -0.3F));
        return poseStack;
    }

    /** Null when every vertex carries the light, overlay and colour passed, else what differs. */
    private static String passedOn(Recorder drawn) {
        int[] color = {COLOR >> 16 & 0xFF, COLOR >> 8 & 0xFF, COLOR & 0xFF, COLOR >>> 24};
        for (int i = 0; i < drawn.positions.size(); i++) {
            int[] c = drawn.colors.get(i), o = drawn.overlays.get(i), l = drawn.lights.get(i);
            if (c[0] != color[0] || c[1] != color[1] || c[2] != color[2] || c[3] != color[3]) {
                return String.format("vertex %d drawn with colour %d %d %d %d, not the layer's", i, c[0], c[1], c[2], c[3]);
            }
            if (o[0] != (OVERLAY & 0xFFFF) || o[1] != OVERLAY >>> 16) {
                return String.format("vertex %d drawn with overlay %d %d, not the layer's", i, o[0], o[1]);
            }
            if (l[0] != (LIGHT & 0xFFFF) || l[1] != LIGHT >>> 16) {
                return String.format("vertex %d drawn with light %d %d, not the layer's", i, l[0], l[1]);
            }
        }
        return null;
    }

    /**
     * Null when every face's normal is a unit vector pointing out of its cube (along the step from the cube's centre to
     * the face's), each cube being 24 vertices in drawing order; faces of a flat cube's thin sides have no such step.
     */
    private static String normals(Recorder drawn) {
        if (drawn.positions.size() % 24 != 0) return "drew " + drawn.positions.size() + " vertices, not whole cubes";
        for (int c = 0; c < drawn.positions.size(); c += 24) {
            float[] centre = new float[3];
            for (int k = 0; k < 24; k++) {
                for (int a = 0; a < 3; a++) centre[a] += drawn.positions.get(c + k)[a] / 24.0F;
            }
            for (int f = 0; f < 6; f++) {
                float[] face = new float[3];
                for (int k = 0; k < 4; k++) {
                    float[] n = drawn.normals.get(c + f * 4 + k), first = drawn.normals.get(c + f * 4);
                    if (Math.abs(n[0] - first[0]) > 1e-6 || Math.abs(n[1] - first[1]) > 1e-6
                            || Math.abs(n[2] - first[2]) > 1e-6) {
                        return "cube " + c / 24 + " face " + f + " has four different normals";
                    }
                    for (int a = 0; a < 3; a++) face[a] += drawn.positions.get(c + f * 4 + k)[a] / 4.0F;
                }
                float[] n = drawn.normals.get(c + f * 4);
                float length = (float) Math.sqrt(n[0] * n[0] + n[1] * n[1] + n[2] * n[2]);
                if (Math.abs(length - 1.0F) > 1e-3) return "cube " + c / 24 + " face " + f + " normal of length " + length;
                float[] out = {face[0] - centre[0], face[1] - centre[1], face[2] - centre[2]};
                float reach = (float) Math.sqrt(out[0] * out[0] + out[1] * out[1] + out[2] * out[2]);
                if (reach < 1e-5) continue;
                float dot = (n[0] * out[0] + n[1] * out[1] + n[2] * out[2]) / reach;
                if (dot < 0.999F) {
                    return String.format("cube %d face %d normal (%.3f %.3f %.3f) is not out of the cube (%.3f along it)",
                            c / 24, f, n[0], n[1], n[2], dot);
                }
            }
        }
        return null;
    }

    /** {min x, y, z, max x, y, z} of the positions from..to. */
    private static float[] extent(List<float[]> positions, int from, int to) {
        float[] box = {Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE, -Float.MAX_VALUE, -Float.MAX_VALUE,
                -Float.MAX_VALUE};
        for (int i = from; i < to; i++) {
            for (int a = 0; a < 3; a++) {
                box[a] = Math.min(box[a], positions.get(i)[a]);
                box[3 + a] = Math.max(box[3 + a], positions.get(i)[a]);
            }
        }
        return box;
    }

    /** Null when the boxes are one (to 1e-5 of a block), else both in model units. */
    private static String sameBox(float[] ours, float[] theirs) {
        for (int i = 0; i < 6; i++) {
            if (Math.abs(ours[i] - theirs[i]) > 1e-5) {
                return String.format("is drawn at (%.3f %.3f %.3f)-(%.3f %.3f %.3f), vanilla's box at "
                                + "(%.3f %.3f %.3f)-(%.3f %.3f %.3f)", ours[0] * 16, ours[1] * 16, ours[2] * 16,
                        ours[3] * 16, ours[4] * 16, ours[5] * 16, theirs[0] * 16, theirs[1] * 16, theirs[2] * 16,
                        theirs[3] * 16, theirs[4] * 16, theirs[5] * 16);
            }
        }
        return null;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static void copyProperties(HumanoidModel<?> original, HumanoidModel<?> model) {
        ((HumanoidModel) original).copyPropertiesTo((HumanoidModel) model);
    }

    private static Set<String> shown(EquipmentSlot slot) {
        return switch (slot) {
            case HEAD -> Set.of("head", "hat");
            case CHEST -> Set.of("body", "right_arm", "left_arm");
            case LEGS -> Set.of("body", "right_leg", "left_leg");
            default -> Set.of("right_leg", "left_leg");
        };
    }

    private static String partOf(String bone) {
        for (String[] p : ORDER) {
            if (p[1].equals(bone)) return p[0];
        }
        return null;
    }

    /** Every drawn vertex's UV in texels, in the drawing order, from the file alone. */
    private static List<float[]> expectedUvs(JsonObject geometry, Set<String> shown) {
        JsonObject bones = geometry.getAsJsonObject("bones");
        List<float[]> out = new ArrayList<>();
        for (String[] p : ORDER) {
            if (!shown.contains(p[0]) || !bones.has(p[1])) continue;
            List<JsonObject> plain = new ArrayList<>(), rotated = new ArrayList<>();
            for (JsonElement element : bones.getAsJsonArray(p[1])) {
                JsonObject cube = element.getAsJsonObject();
                (cube.has("rotation") ? rotated : plain).add(cube);
            }
            for (JsonObject cube : plain) out.addAll(cubeUvs(cube));
            for (JsonObject cube : rotated) out.addAll(cubeUvs(cube));
        }
        return out;
    }

    /** Each drawn face, in the drawing order: {face index 0-5 (DOWN, UP, WEST, NORTH, EAST, SOUTH), mirrored, rotated}. */
    private static List<int[]> expectedFaces(JsonObject geometry, Set<String> shown) {
        JsonObject bones = geometry.getAsJsonObject("bones");
        List<int[]> out = new ArrayList<>();
        for (String[] p : ORDER) {
            if (!shown.contains(p[0]) || !bones.has(p[1])) continue;
            List<JsonObject> plain = new ArrayList<>(), rotated = new ArrayList<>();
            for (JsonElement element : bones.getAsJsonArray(p[1])) {
                JsonObject cube = element.getAsJsonObject();
                (cube.has("rotation") ? rotated : plain).add(cube);
            }
            for (List<JsonObject> group : List.of(plain, rotated)) {
                for (JsonObject cube : group) {
                    int mirror = cube.has("mirror") && cube.get("mirror").getAsBoolean() ? 1 : 0;
                    for (int face = 0; face < 6; face++) out.add(new int[]{face, mirror, cube.has("rotation") ? 1 : 0});
                }
            }
        }
        return out;
    }

    /**
     * Every unrotated cube's faces turned as vanilla's box UV turns them: walking along a face from its (u1, v1) corner
     * to its (u2, v1) corner goes along x, +x on DOWN, UP and NORTH and -x on SOUTH (the other way on a mirrored cube),
     * -z on WEST, +z on EAST; from (u1, v1) to (u1, v2) goes -z on DOWN and UP and +y (down) on the four sides.
     * Null when they all are, else the first face that is not.
     */
    private static String orientation(Recorder drawn, List<int[]> faces, float tw, float th) {
        String[] names = {"DOWN", "UP", "WEST", "NORTH", "EAST", "SOUTH"};
        int[][] uAxis = {{0, 1}, {0, 1}, {2, -1}, {0, 1}, {2, 1}, {0, -1}};
        int[][] vAxis = {{2, -1}, {2, -1}, {1, 1}, {1, 1}, {1, 1}, {1, 1}};
        for (int f = 0; f < faces.size(); f++) {
            int[] face = faces.get(f);
            if (face[2] == 1) continue;
            float[][] pos = new float[4][], uv = new float[4][];
            for (int k = 0; k < 4; k++) {
                pos[k] = drawn.positions.get(f * 4 + k);
                float[] t = drawn.uvs.get(f * 4 + k);
                uv[k] = new float[]{t[0] * tw, t[1] * th};
            }
            float uMin = Math.min(Math.min(uv[0][0], uv[1][0]), Math.min(uv[2][0], uv[3][0]));
            float vMin = Math.min(Math.min(uv[0][1], uv[1][1]), Math.min(uv[2][1], uv[3][1]));
            int c11 = -1, c21 = -1, c12 = -1;
            for (int k = 0; k < 4; k++) {
                boolean atU1 = Math.abs(uv[k][0] - uMin) < 1e-3, atV1 = Math.abs(uv[k][1] - vMin) < 1e-3;
                if (atU1 && atV1) c11 = k;
                else if (!atU1 && atV1) c21 = k;
                else if (atU1) c12 = k;
            }
            if (c11 < 0 || c21 < 0 || c12 < 0) return "face " + f + " (" + names[face[0]] + ") has no rectangle of UVs";
            int[] ue = uAxis[face[0]].clone();
            if (face[1] == 1 && ue[0] == 0) ue[1] = -ue[1];
            int[] ve = vAxis[face[0]];
            int[] ua = axis(pos[c11], pos[c21]), va = axis(pos[c11], pos[c12]);
            if (ua[0] != ue[0] || ua[1] != ue[1] || va[0] != ve[0] || va[1] != ve[1]) {
                return "face " + f + " (" + names[face[0]] + (face[1] == 1 ? ", mirrored" : "")
                        + ") is turned wrong: u runs along " + "xyz".charAt(ua[0]) + (ua[1] > 0 ? "+" : "-")
                        + " and v along " + "xyz".charAt(va[0]) + (va[1] > 0 ? "+" : "-");
            }
        }
        return null;
    }

    /** The dominant axis (0 x, 1 y, 2 z) of the step from a to b, and its sign. */
    private static int[] axis(float[] a, float[] b) {
        int best = 0;
        float bestAbs = -1;
        for (int i = 0; i < 3; i++) {
            float d = Math.abs(b[i] - a[i]);
            if (d > bestAbs) {
                bestAbs = d;
                best = i;
            }
        }
        return new int[]{best, b[best] - a[best] > 0 ? 1 : -1};
    }

    /** A cube's 24 vertex UVs: the faces DOWN, UP, WEST, NORTH, EAST, SOUTH, each (u2 v1) (u1 v1) (u1 v2) (u2 v2),
     *  reversed on a mirrored cube. */
    private static List<float[]> cubeUvs(JsonObject cube) {
        JsonArray size = cube.getAsJsonArray("size");
        float w, h, d;
        if (cube.has("uv_size")) {
            JsonArray s = cube.getAsJsonArray("uv_size");
            w = s.get(0).getAsFloat();
            h = s.get(1).getAsFloat();
            d = s.get(2).getAsFloat();
        } else {
            w = (float) Math.floor(Math.abs(size.get(0).getAsFloat()) + 1e-4);
            h = (float) Math.floor(Math.abs(size.get(1).getAsFloat()) + 1e-4);
            d = (float) Math.floor(Math.abs(size.get(2).getAsFloat()) + 1e-4);
        }
        float u = cube.getAsJsonArray("uv").get(0).getAsFloat(), v = cube.getAsJsonArray("uv").get(1).getAsFloat();
        boolean mirror = cube.has("mirror") && cube.get("mirror").getAsBoolean();
        float[][] rects = {{u + d, v, u + d + w, v + d}, {u + d + w, v + d, u + d + w + w, v}, {u, v + d, u + d, v + d + h},
                {u + d, v + d, u + d + w, v + d + h}, {u + d + w, v + d, u + d + w + d, v + d + h},
                {u + d + w + d, v + d, u + d + w + d + w, v + d + h}};
        List<float[]> out = new ArrayList<>();
        for (float[] r : rects) {
            float[][] corners = {{r[2], r[1]}, {r[0], r[1]}, {r[0], r[3]}, {r[2], r[3]}};
            if (mirror) {
                for (int i = 3; i >= 0; i--) out.add(corners[i]);
            } else {
                for (float[] c : corners) out.add(c);
            }
        }
        return out;
    }

    private static HumanoidModel<LivingEntity> vanilla(float inflate) {
        return new HumanoidModel<>(LayerDefinition.create(HumanoidArmorModel.createBodyLayer(new CubeDeformation(inflate)),
                64, 32).bakeRoot());
    }

    private static HumanoidModel<LivingEntity> stand(float inflate) {
        return new HumanoidModel<>(ArmorStandArmorModel.createBodyLayer(new CubeDeformation(inflate)).bakeRoot());
    }

    private static HumanoidModel<?> villager(float inflate) {
        return new ZombieVillagerModel<ZombieVillager>(
                ZombieVillagerModel.createArmorLayer(new CubeDeformation(inflate)).bakeRoot());
    }

    /**
     * An armour model shaped as Guard Villagers' guards wear (its GuardArmorModel): vanilla's humanoid mesh at the
     * layer's deformation, legs included, with the helmet box at y -10 on the guard's 10-tall head.
     */
    private static HumanoidModel<LivingEntity> guard(float inflate) {
        MeshDefinition mesh = HumanoidModel.createMesh(new CubeDeformation(inflate), 0.0F);
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -10.0F, -4.0F, 8.0F, 8.0F,
                8.0F, new CubeDeformation(inflate)), PartPose.offset(0.0F, 1.0F, 0.0F));
        root.addOrReplaceChild("hat", CubeListBuilder.create().texOffs(32, 0).addBox(-4.0F, -10.0F, -4.0F, 8.0F, 8.0F,
                8.0F, new CubeDeformation(inflate + 0.5F)), PartPose.offset(0.0F, 1.0F, 0.0F));
        return new HumanoidModel<>(LayerDefinition.create(mesh, 64, 32).bakeRoot());
    }

    private static Map<String, ModelPart> parts(HumanoidModel<?> model) {
        Map<String, ModelPart> parts = new LinkedHashMap<>();
        parts.put("head", model.head);
        parts.put("hat", model.hat);
        parts.put("body", model.body);
        parts.put("right_arm", model.rightArm);
        parts.put("left_arm", model.leftArm);
        parts.put("right_leg", model.rightLeg);
        parts.put("left_leg", model.leftLeg);
        return parts;
    }

    /** Records the drawn vertices in order: position, UV, colour, overlay, light and normal. */
    private static final class Recorder implements VertexConsumer {
        private final List<float[]> positions = new ArrayList<>();
        private final List<float[]> uvs = new ArrayList<>();
        private final List<int[]> colors = new ArrayList<>();
        private final List<int[]> overlays = new ArrayList<>();
        private final List<int[]> lights = new ArrayList<>();
        private final List<float[]> normals = new ArrayList<>();

        @Override
        public VertexConsumer addVertex(float x, float y, float z) {
            positions.add(new float[]{x, y, z});
            return this;
        }

        @Override
        public VertexConsumer setColor(int r, int g, int b, int a) {
            colors.add(new int[]{r, g, b, a});
            return this;
        }

        @Override
        public VertexConsumer setUv(float u, float v) {
            uvs.add(new float[]{u, v});
            return this;
        }

        @Override
        public VertexConsumer setUv1(int u, int v) {
            overlays.add(new int[]{u, v});
            return this;
        }

        @Override
        public VertexConsumer setUv2(int u, int v) {
            lights.add(new int[]{u, v});
            return this;
        }

        @Override
        public VertexConsumer setNormal(float x, float y, float z) {
            normals.add(new float[]{x, y, z});
            return this;
        }
    }
}
