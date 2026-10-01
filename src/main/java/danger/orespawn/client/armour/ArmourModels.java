package danger.orespawn.client.armour;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;
import danger.orespawn.OreSpawnMod;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;

import java.io.Reader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * The modern style's piece models, built on first use from {@code assets/orespawn/armour_geo/<set>_<piece>.json} and
 * built again after a resource reload. A geometry that fails to build is logged once and the piece keeps its classic
 * look, model and texture (ArmourPieces).
 */
public final class ArmourModels {
    private static final Map<String, ArmourModel> MODELS = new HashMap<>();
    private static final Set<String> FAILED = new HashSet<>();

    private ArmourModels() {
    }

    public static synchronized void clear() {
        MODELS.clear();
        FAILED.clear();
        ArmourPieces.clear();
    }

    /** The piece's model for a slot, or null (the slot is not an armour slot, or its geometry failed). */
    public static synchronized ArmourModel get(String set, EquipmentSlot slot) {
        String piece = ArmourPieces.piece(slot);
        if (piece == null) return null;
        String key = set + "_" + piece;
        ArmourModel model = MODELS.get(key);
        if (model != null || FAILED.contains(key)) return model;
        ResourceLocation location = ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "armour_geo/" + key + ".json");
        try (Reader reader = Minecraft.getInstance().getResourceManager().openAsReader(location)) {
            JsonObject geometry = JsonParser.parseReader(reader).getAsJsonObject();
            model = new ArmourModel(ArmourGeometry.build(geometry));
            MODELS.put(key, model);
        } catch (Exception exception) {
            LogUtils.getLogger().error("OreSpawn armour style: {} could not be built", location, exception);
            FAILED.add(key);
            ArmourPieces.unavailable(key);
        }
        return model;
    }
}
