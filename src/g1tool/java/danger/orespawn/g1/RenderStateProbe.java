package danger.orespawn.g1;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

/**
 * ENT-S-146 (refuter B, D3: "the mode observed, not asserted"). What the headless probe can record of the
 * render state each side actually requests, in the un-bootstrapped JVM (OPT-029 R0), written beside every
 * dump as {@code <id>.render-state.json} so the existing dumps stay byte-identical (the compiled dump's
 * sha256 is the conversion's provenance).
 *
 * <ul>
 * <li>The render type. Neither side's {@code RenderType} can be produced here: {@code RenderType.<clinit>}
 *     reaches {@code Items.<clinit>} through {@code ItemRenderer.<clinit>} and trips
 *     {@code Bootstrap.checkBootstrapCalled} ("Not bootstrapped", measured 2026-09-06). What CAN be observed
 *     is the render-type FUNCTION each side holds - the classic model's {@code Model.renderType} field
 *     (a {@code Function<ResourceLocation, RenderType>}, applied by {@code Model.renderType(ResourceLocation)}
 *     and returned by {@code LivingEntityRenderer.getRenderType} for a visible body) and the descriptor's
 *     {@code renderType(entity)} hook (applied by {@code OreSpawnGeoReplacedEntityRenderer.getRenderType} on
 *     the same condition): its identity across the two sides, and its implementation, read from the class
 *     file of the class that created it (a method reference compiles to a lambda whose hidden class is
 *     named {@code <owner>$$Lambda...}; the owner's constant pool holds the {@code RenderType} factory it
 *     targets - the {@code (ResourceLocation)RenderType} {@code Methodref}). The factory name is the render
 *     type's registered name in camel case ({@code RenderType.lambda$static$7} builds
 *     {@code entityTranslucent} under {@code ldc "entity_translucent"}, offset 54; {@code lambda$static$3}
 *     {@code entityCutoutNoCull} under {@code ldc "entity_cutout_no_cull"}, 54; 21.1.223 bytecode). This class
 *     records what it found (every such factory in the owner's pool); the parity tool decides.</li>
 * <li>The vertex colour and the packed light: observed at every {@code addVertex} of the full captures -
 *     the values the vertices carry - after the probe hands each side what its renderer would: the
 *     classic {@code renderToBuffer} gets the probe's white and the classic renderer's own light-level
 *     answers when the manifest names the renderer ({@code classic_renderer}, evaluated registry-free
 *     through a null-filled {@code EntityRendererProvider.Context}), the candidate's {@code actuallyRender}
 *     the descriptor's {@code renderColor} and {@code fullBright} exactly as {@code GeoRenderer.defaultRender}
 *     and {@code EntityRenderer.getPackedLightCoords} would pack them.</li>
 * </ul>
 */
final class RenderStateProbe {
    /** The packed light the probe hands a side whose renderer decides nothing (no fullbright override): the same on both sides. */
    static final int PROBE_LIGHT = 0;
    /** GeckoLib's own colour: {@code GeoRenderer.getRenderColor} returns {@code Color.WHITE} = -1 (4.8.4 bytecode). */
    static final int GECKOLIB_WHITE = -1;
    static final String RENDER_TYPE_CLASS = "net/minecraft/client/renderer/RenderType";
    static final String FACTORY_DESCRIPTOR = "(Lnet/minecraft/resources/ResourceLocation;)Lnet/minecraft/client/renderer/RenderType;";
    /** The class whose {@code getRenderType} decides a candidate without a descriptor hook: {@code RenderType.entityCutoutNoCull(texture)} (4.8.4 bytecode offset 1). */
    static final String GECKOLIB_DEFAULT_OWNER = "software.bernie.geckolib.model.GeoModel";

    private RenderStateProbe() {
    }

    /** Colour and light ints seen at {@code addVertex} over a dump's full captures. */
    static final class Observed {
        private final Set<Integer> colours = new TreeSet<>();
        private final Set<Integer> lights = new TreeSet<>();
        private long vertices;

        void see(int color, int packedLight) {
            this.colours.add(color);
            this.lights.add(packedLight);
            this.vertices++;
        }

        JsonObject colourJson() {
            JsonObject out = new JsonObject();
            out.addProperty("vertices_observed", this.vertices);
            JsonArray argb = new JsonArray();
            JsonArray rgba = new JsonArray();
            for (int colour : this.colours) {
                argb.add(colour);
                rgba.add(rgba(colour));
            }
            out.add("distinct_argb", argb);
            out.add("distinct_rgba", rgba);
            if (this.colours.size() == 1) {
                int colour = this.colours.iterator().next();
                out.addProperty("argb", colour);
                out.add("rgba", rgba(colour));
            }
            return out;
        }

        JsonObject lightJson() {
            JsonObject out = new JsonObject();
            out.addProperty("vertices_observed", this.vertices);
            JsonArray distinct = new JsonArray();
            this.lights.forEach(distinct::add);
            out.add("distinct_observed", distinct);
            if (this.lights.size() == 1) {
                out.addProperty("value", this.lights.iterator().next());
            }
            return out;
        }
    }

    /** ARGB int to the RGBA bytes the shader multiplies the texel by. */
    static JsonArray rgba(int argb) {
        JsonArray out = new JsonArray();
        out.add((argb >> 16) & 255);
        out.add((argb >> 8) & 255);
        out.add(argb & 255);
        out.add((argb >>> 24) & 255);
        return out;
    }

    static int packedLight(int block, int sky) {
        return LightTexture.pack(block, sky);
    }

    /** The classic model's render-type function: the {@code Model.renderType} field ({@code EntityModel(Function)} stores it). */
    @SuppressWarnings("unchecked")
    static Function<ResourceLocation, RenderType> classicFunction(Object model) throws ReflectiveOperationException {
        Field field = net.minecraft.client.model.Model.class.getDeclaredField("renderType");
        field.setAccessible(true);
        return (Function<ResourceLocation, RenderType>) field.get(model);
    }

    /**
     * The function {@code EntityModel()} installs ({@code invokedynamic #0} -> {@code RenderType.entityCutoutNoCull},
     * 21.1.223 bytecode): a non-capturing method reference, one instance per call site, so a model built through
     * the default constructor holds this very object.
     */
    static Function<ResourceLocation, RenderType> entityModelDefault() throws ReflectiveOperationException {
        EntityModel<Entity> probe = new EntityModel<>() {
            @Override
            public void setupAnim(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks,
                                  float netHeadYaw, float headPitch) {
            }

            @Override
            public void renderToBuffer(com.mojang.blaze3d.vertex.PoseStack poseStack,
                                       com.mojang.blaze3d.vertex.VertexConsumer buffer, int packedLight,
                                       int packedOverlay, int color) {
            }
        };
        return classicFunction(probe);
    }

    /**
     * What a render-type function object is: its class, the class that created it (the owner), the
     * {@code RenderType} factories that owner references with the {@code (ResourceLocation)RenderType} shape,
     * and - when exactly one - the render type's name. Records; never decides.
     */
    static JsonObject describeFunction(Function<ResourceLocation, RenderType> function,
                                       Function<ResourceLocation, RenderType> entityModelDefault) throws IOException {
        JsonObject out = new JsonObject();
        String functionClass = function.getClass().getName();
        String owner = ownerOf(functionClass);
        out.addProperty("function_class", functionClass);
        out.addProperty("owner_class", owner);
        List<String> factories = renderTypeFactories(owner);
        JsonArray names = new JsonArray();
        factories.forEach(names::add);
        out.add("render_type_factories", names);
        out.addProperty("render_type", factories.size() == 1 ? snakeCase(factories.get(0)) : null);
        out.addProperty("is_entity_model_default", function == entityModelDefault);
        return out;
    }

    /** GeckoLib's own decision for a candidate without a hook: {@code GeoModel.getRenderType}'s constant pool. */
    static JsonObject describeGeckoLibDefault() throws IOException {
        JsonObject out = new JsonObject();
        out.addProperty("function_class", (String) null);
        out.addProperty("owner_class", GECKOLIB_DEFAULT_OWNER);
        List<String> factories = renderTypeFactories(GECKOLIB_DEFAULT_OWNER);
        JsonArray names = new JsonArray();
        factories.forEach(names::add);
        out.add("render_type_factories", names);
        out.addProperty("render_type", factories.size() == 1 ? snakeCase(factories.get(0)) : null);
        out.addProperty("is_entity_model_default", false);
        return out;
    }

    /** {@code <owner>$$Lambda/0x...} (a lambda's hidden class) to its owner; any other class is its own owner. */
    static String ownerOf(String className) {
        int lambda = className.indexOf("$$Lambda");
        return lambda < 0 ? className : className.substring(0, lambda);
    }

    /** {@code entityCutoutNoCull} to {@code entity_cutout_no_cull}: the factory names are the registered names in camel case. */
    static String snakeCase(String factory) {
        StringBuilder out = new StringBuilder();
        for (char c : factory.toCharArray()) {
            if (Character.isUpperCase(c)) {
                out.append('_').append(Character.toLowerCase(c));
            } else {
                out.append(c);
            }
        }
        return out.toString();
    }

    /**
     * Every {@code RenderType} factory of the {@code (ResourceLocation)RenderType} shape the class references
     * ({@code Methodref} / {@code InterfaceMethodref} entries of its constant pool, in pool order, distinct) -
     * for a method reference {@code RenderType::entityTranslucent} the bootstrap argument's target.
     */
    static List<String> renderTypeFactories(String className) throws IOException {
        String resource = className.replace('.', '/') + ".class";
        try (InputStream stream = RenderStateProbe.class.getClassLoader().getResourceAsStream(resource)) {
            if (stream == null) {
                throw new IOException("class file not on the probe classpath: " + resource);
            }
            return renderTypeFactories(new DataInputStream(stream));
        }
    }

    private static List<String> renderTypeFactories(DataInputStream in) throws IOException {
        if (in.readInt() != 0xCAFEBABE) {
            throw new IOException("not a class file");
        }
        in.readUnsignedShort();
        in.readUnsignedShort();
        int count = in.readUnsignedShort();
        Object[] pool = new Object[count];
        for (int index = 1; index < count; index++) {
            int tag = in.readUnsignedByte();
            switch (tag) {
                case 1 -> pool[index] = in.readUTF();
                case 3, 4 -> in.readInt();
                case 5, 6 -> {
                    in.readLong();
                    index++;
                }
                case 7, 8, 16, 19, 20 -> pool[index] = new int[]{tag, in.readUnsignedShort()};
                case 9, 10, 11, 12, 17, 18 -> pool[index] = new int[]{tag, in.readUnsignedShort(), in.readUnsignedShort()};
                case 15 -> {
                    in.readUnsignedByte();
                    in.readUnsignedShort();
                }
                default -> throw new IOException("unknown constant pool tag " + tag);
            }
        }
        Set<String> factories = new LinkedHashSet<>();
        for (Object entry : pool) {
            if (!(entry instanceof int[] ref) || (ref[0] != 10 && ref[0] != 11)) {
                continue;
            }
            String owner = (String) pool[((int[]) pool[ref[1]])[1]];
            int[] nameAndType = (int[]) pool[ref[2]];
            String name = (String) pool[nameAndType[1]];
            String descriptor = (String) pool[nameAndType[2]];
            if (RENDER_TYPE_CLASS.equals(owner) && FACTORY_DESCRIPTOR.equals(descriptor)) {
                factories.add(name);
            }
        }
        return new ArrayList<>(factories);
    }

    /**
     * The classic renderer's light-level answers, registry-free: the renderer is constructed through a
     * null-filled {@code EntityRendererProvider.Context} whose model set bakes the probe's own layer, and
     * {@code getBlockLightLevel} / {@code getSkyLightLevel} (the {@code MagmaCubeRenderer} idiom, which
     * ignores the entity and the position) are invoked with nulls; a renderer that reads its entity fails
     * here loudly, by design. Packed as {@code EntityRenderer.getPackedLightCoords} packs them.
     */
    static JsonObject classicRendererLight(String rendererClass, LayerDefinition layer) throws Exception {
        EntityModelSet models = new EntityModelSet() {
            @Override
            public ModelPart bakeLayer(ModelLayerLocation location) {
                return layer.bakeRoot();
            }
        };
        EntityRendererProvider.Context context = new EntityRendererProvider.Context(null, null, null, null, null, models, null);
        Object renderer = Class.forName(rendererClass).getDeclaredConstructor(EntityRendererProvider.Context.class)
                .newInstance(context);
        int block = (Integer) lightMethod(renderer.getClass(), "getBlockLightLevel").invoke(renderer, null, null);
        int sky = (Integer) lightMethod(renderer.getClass(), "getSkyLightLevel").invoke(renderer, null, null);
        JsonObject out = new JsonObject();
        out.addProperty("renderer_class", rendererClass);
        out.addProperty("block_light_level", block);
        out.addProperty("sky_light_level", sky);
        out.addProperty("packed", packedLight(block, sky));
        return out;
    }

    private static Method lightMethod(Class<?> type, String name) {
        for (Class<?> current = type; current != null; current = current.getSuperclass()) {
            for (Method method : current.getDeclaredMethods()) {
                if (method.getName().equals(name) && method.getParameterCount() == 2 && !method.isBridge()) {
                    method.setAccessible(true);
                    return method;
                }
            }
        }
        throw new IllegalStateException(type.getName() + " has no " + name + "(entity, pos)");
    }
}
