package danger.orespawn;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(Registries.SOUND_EVENT, OreSpawnMod.MOD_ID);

    public static final DeferredHolder<SoundEvent, SoundEvent> ALIEN_DEATH = SOUND_EVENTS.register("alien_death",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "alien_death")));
    public static final DeferredHolder<SoundEvent, SoundEvent> ALIEN_HURT = SOUND_EVENTS.register("alien_hurt",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "alien_hurt")));
    public static final DeferredHolder<SoundEvent, SoundEvent> ALIEN_LIVING = SOUND_EVENTS.register("alien_living",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "alien_living")));
    public static final DeferredHolder<SoundEvent, SoundEvent> ALO_DEATH = SOUND_EVENTS.register("alo_death",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "alo_death")));
    public static final DeferredHolder<SoundEvent, SoundEvent> ALO_HURT = SOUND_EVENTS.register("alo_hurt",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "alo_hurt")));
    public static final DeferredHolder<SoundEvent, SoundEvent> ALO_LIVING = SOUND_EVENTS.register("alo_living",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "alo_living")));
    public static final DeferredHolder<SoundEvent, SoundEvent> B_DARK = SOUND_EVENTS.register("b_dark",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "b_dark")));
    public static final DeferredHolder<SoundEvent, SoundEvent> B_DEATH_BOYFRIEND = SOUND_EVENTS.register("b_death_boyfriend",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "b_death_boyfriend")));
    public static final DeferredHolder<SoundEvent, SoundEvent> B_DEATH_SINGLE1 = SOUND_EVENTS.register("b_death_single1",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "b_death_single1")));
    public static final DeferredHolder<SoundEvent, SoundEvent> B_DEATH_SINGLE2 = SOUND_EVENTS.register("b_death_single2",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "b_death_single2")));
    public static final DeferredHolder<SoundEvent, SoundEvent> B_FIGHT1 = SOUND_EVENTS.register("b_fight1",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "b_fight1")));
    public static final DeferredHolder<SoundEvent, SoundEvent> B_FIGHT2 = SOUND_EVENTS.register("b_fight2",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "b_fight2")));
    public static final DeferredHolder<SoundEvent, SoundEvent> B_FIGHT3 = SOUND_EVENTS.register("b_fight3",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "b_fight3")));
    public static final DeferredHolder<SoundEvent, SoundEvent> B_FIGHT4 = SOUND_EVENTS.register("b_fight4",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "b_fight4")));
    public static final DeferredHolder<SoundEvent, SoundEvent> B_FIGHT5 = SOUND_EVENTS.register("b_fight5",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "b_fight5")));
    public static final DeferredHolder<SoundEvent, SoundEvent> B_FIGHT6 = SOUND_EVENTS.register("b_fight6",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "b_fight6")));
    public static final DeferredHolder<SoundEvent, SoundEvent> B_FIGHT7 = SOUND_EVENTS.register("b_fight7",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "b_fight7")));
    public static final DeferredHolder<SoundEvent, SoundEvent> B_HAPPY1 = SOUND_EVENTS.register("b_happy1",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "b_happy1")));
    public static final DeferredHolder<SoundEvent, SoundEvent> B_HAPPY2 = SOUND_EVENTS.register("b_happy2",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "b_happy2")));
    public static final DeferredHolder<SoundEvent, SoundEvent> B_HAPPY3 = SOUND_EVENTS.register("b_happy3",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "b_happy3")));
    public static final DeferredHolder<SoundEvent, SoundEvent> B_HAPPY4 = SOUND_EVENTS.register("b_happy4",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "b_happy4")));
    public static final DeferredHolder<SoundEvent, SoundEvent> B_HAPPY5 = SOUND_EVENTS.register("b_happy5",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "b_happy5")));
    public static final DeferredHolder<SoundEvent, SoundEvent> B_HAPPY6 = SOUND_EVENTS.register("b_happy6",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "b_happy6")));
    public static final DeferredHolder<SoundEvent, SoundEvent> B_HAPPY7 = SOUND_EVENTS.register("b_happy7",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "b_happy7")));
    public static final DeferredHolder<SoundEvent, SoundEvent> B_HAPPY8 = SOUND_EVENTS.register("b_happy8",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "b_happy8")));
    public static final DeferredHolder<SoundEvent, SoundEvent> B_HURT1 = SOUND_EVENTS.register("b_hurt1",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "b_hurt1")));
    public static final DeferredHolder<SoundEvent, SoundEvent> B_HURT10 = SOUND_EVENTS.register("b_hurt10",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "b_hurt10")));
    public static final DeferredHolder<SoundEvent, SoundEvent> B_HURT2 = SOUND_EVENTS.register("b_hurt2",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "b_hurt2")));
    public static final DeferredHolder<SoundEvent, SoundEvent> B_HURT3 = SOUND_EVENTS.register("b_hurt3",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "b_hurt3")));
    public static final DeferredHolder<SoundEvent, SoundEvent> B_HURT4 = SOUND_EVENTS.register("b_hurt4",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "b_hurt4")));
    public static final DeferredHolder<SoundEvent, SoundEvent> B_HURT5 = SOUND_EVENTS.register("b_hurt5",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "b_hurt5")));
    public static final DeferredHolder<SoundEvent, SoundEvent> B_HURT6 = SOUND_EVENTS.register("b_hurt6",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "b_hurt6")));
    public static final DeferredHolder<SoundEvent, SoundEvent> B_HURT7 = SOUND_EVENTS.register("b_hurt7",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "b_hurt7")));
    public static final DeferredHolder<SoundEvent, SoundEvent> B_HURT8 = SOUND_EVENTS.register("b_hurt8",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "b_hurt8")));
    public static final DeferredHolder<SoundEvent, SoundEvent> B_HURT9 = SOUND_EVENTS.register("b_hurt9",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "b_hurt9")));
    public static final DeferredHolder<SoundEvent, SoundEvent> B_OW1 = SOUND_EVENTS.register("b_ow1",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "b_ow1")));
    public static final DeferredHolder<SoundEvent, SoundEvent> B_OW2 = SOUND_EVENTS.register("b_ow2",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "b_ow2")));
    public static final DeferredHolder<SoundEvent, SoundEvent> B_OW3 = SOUND_EVENTS.register("b_ow3",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "b_ow3")));
    public static final DeferredHolder<SoundEvent, SoundEvent> B_OW4 = SOUND_EVENTS.register("b_ow4",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "b_ow4")));
    public static final DeferredHolder<SoundEvent, SoundEvent> B_OW5 = SOUND_EVENTS.register("b_ow5",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "b_ow5")));
    public static final DeferredHolder<SoundEvent, SoundEvent> B_OW6 = SOUND_EVENTS.register("b_ow6",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "b_ow6")));
    public static final DeferredHolder<SoundEvent, SoundEvent> B_OW7 = SOUND_EVENTS.register("b_ow7",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "b_ow7")));
    public static final DeferredHolder<SoundEvent, SoundEvent> B_OW8 = SOUND_EVENTS.register("b_ow8",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "b_ow8")));
    public static final DeferredHolder<SoundEvent, SoundEvent> B_OW9 = SOUND_EVENTS.register("b_ow9",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "b_ow9")));
    public static final DeferredHolder<SoundEvent, SoundEvent> B_RAIN1 = SOUND_EVENTS.register("b_rain1",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "b_rain1")));
    public static final DeferredHolder<SoundEvent, SoundEvent> B_RAIN2 = SOUND_EVENTS.register("b_rain2",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "b_rain2")));
    public static final DeferredHolder<SoundEvent, SoundEvent> B_TAUNT1 = SOUND_EVENTS.register("b_taunt1",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "b_taunt1")));
    public static final DeferredHolder<SoundEvent, SoundEvent> B_TAUNT2 = SOUND_EVENTS.register("b_taunt2",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "b_taunt2")));
    public static final DeferredHolder<SoundEvent, SoundEvent> B_TAUNT3 = SOUND_EVENTS.register("b_taunt3",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "b_taunt3")));
    public static final DeferredHolder<SoundEvent, SoundEvent> B_TAUNT4 = SOUND_EVENTS.register("b_taunt4",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "b_taunt4")));
    public static final DeferredHolder<SoundEvent, SoundEvent> B_TAUNT5 = SOUND_EVENTS.register("b_taunt5",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "b_taunt5")));
    public static final DeferredHolder<SoundEvent, SoundEvent> B_TAUNT6 = SOUND_EVENTS.register("b_taunt6",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "b_taunt6")));
    public static final DeferredHolder<SoundEvent, SoundEvent> B_THUNDER = SOUND_EVENTS.register("b_thunder",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "b_thunder")));
    public static final DeferredHolder<SoundEvent, SoundEvent> B_WATER1 = SOUND_EVENTS.register("b_water1",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "b_water1")));
    public static final DeferredHolder<SoundEvent, SoundEvent> B_WATER2 = SOUND_EVENTS.register("b_water2",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "b_water2")));
    public static final DeferredHolder<SoundEvent, SoundEvent> B_WOOHOO1 = SOUND_EVENTS.register("b_woohoo1",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "b_woohoo1")));
    public static final DeferredHolder<SoundEvent, SoundEvent> B_WOOHOO2 = SOUND_EVENTS.register("b_woohoo2",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "b_woohoo2")));
    public static final DeferredHolder<SoundEvent, SoundEvent> B_WOOHOO3 = SOUND_EVENTS.register("b_woohoo3",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "b_woohoo3")));
    public static final DeferredHolder<SoundEvent, SoundEvent> B_WOOHOO4 = SOUND_EVENTS.register("b_woohoo4",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "b_woohoo4")));
    public static final DeferredHolder<SoundEvent, SoundEvent> BASILISK_LIVING = SOUND_EVENTS.register("basilisk_living",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "basilisk_living")));
    public static final DeferredHolder<SoundEvent, SoundEvent> BB_HAPPY1 = SOUND_EVENTS.register("bb_happy1",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "bb_happy1")));
    public static final DeferredHolder<SoundEvent, SoundEvent> BB_HAPPY2 = SOUND_EVENTS.register("bb_happy2",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "bb_happy2")));
    public static final DeferredHolder<SoundEvent, SoundEvent> BB_HAPPY3 = SOUND_EVENTS.register("bb_happy3",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "bb_happy3")));
    public static final DeferredHolder<SoundEvent, SoundEvent> BB_HAPPY4 = SOUND_EVENTS.register("bb_happy4",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "bb_happy4")));
    public static final DeferredHolder<SoundEvent, SoundEvent> BB_HAPPY5 = SOUND_EVENTS.register("bb_happy5",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "bb_happy5")));
    public static final DeferredHolder<SoundEvent, SoundEvent> BB_HAPPY6 = SOUND_EVENTS.register("bb_happy6",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "bb_happy6")));
    public static final DeferredHolder<SoundEvent, SoundEvent> BEEBUZZ = SOUND_EVENTS.register("Beebuzz",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "Beebuzz")));
    public static final DeferredHolder<SoundEvent, SoundEvent> BIG_SPLAT = SOUND_EVENTS.register("big_splat",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "big_splat")));
    public static final DeferredHolder<SoundEvent, SoundEvent> BIRDS1 = SOUND_EVENTS.register("birds1",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "birds1")));
    public static final DeferredHolder<SoundEvent, SoundEvent> BIRDS10 = SOUND_EVENTS.register("birds10",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "birds10")));
    public static final DeferredHolder<SoundEvent, SoundEvent> BIRDS11 = SOUND_EVENTS.register("birds11",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "birds11")));
    public static final DeferredHolder<SoundEvent, SoundEvent> BIRDS12 = SOUND_EVENTS.register("birds12",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "birds12")));
    public static final DeferredHolder<SoundEvent, SoundEvent> BIRDS13 = SOUND_EVENTS.register("birds13",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "birds13")));
    public static final DeferredHolder<SoundEvent, SoundEvent> BIRDS14 = SOUND_EVENTS.register("birds14",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "birds14")));
    public static final DeferredHolder<SoundEvent, SoundEvent> BIRDS15 = SOUND_EVENTS.register("birds15",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "birds15")));
    public static final DeferredHolder<SoundEvent, SoundEvent> BIRDS16 = SOUND_EVENTS.register("birds16",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "birds16")));
    public static final DeferredHolder<SoundEvent, SoundEvent> BIRDS17 = SOUND_EVENTS.register("birds17",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "birds17")));
    public static final DeferredHolder<SoundEvent, SoundEvent> BIRDS18 = SOUND_EVENTS.register("birds18",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "birds18")));
    public static final DeferredHolder<SoundEvent, SoundEvent> BIRDS19 = SOUND_EVENTS.register("birds19",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "birds19")));
    public static final DeferredHolder<SoundEvent, SoundEvent> BIRDS2 = SOUND_EVENTS.register("birds2",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "birds2")));
    public static final DeferredHolder<SoundEvent, SoundEvent> BIRDS20 = SOUND_EVENTS.register("birds20",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "birds20")));
    public static final DeferredHolder<SoundEvent, SoundEvent> BIRDS21 = SOUND_EVENTS.register("birds21",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "birds21")));
    public static final DeferredHolder<SoundEvent, SoundEvent> BIRDS22 = SOUND_EVENTS.register("birds22",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "birds22")));
    public static final DeferredHolder<SoundEvent, SoundEvent> BIRDS23 = SOUND_EVENTS.register("birds23",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "birds23")));
    public static final DeferredHolder<SoundEvent, SoundEvent> BIRDS3 = SOUND_EVENTS.register("birds3",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "birds3")));
    public static final DeferredHolder<SoundEvent, SoundEvent> BIRDS4 = SOUND_EVENTS.register("birds4",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "birds4")));
    public static final DeferredHolder<SoundEvent, SoundEvent> BIRDS5 = SOUND_EVENTS.register("birds5",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "birds5")));
    public static final DeferredHolder<SoundEvent, SoundEvent> BIRDS6 = SOUND_EVENTS.register("birds6",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "birds6")));
    public static final DeferredHolder<SoundEvent, SoundEvent> BIRDS7 = SOUND_EVENTS.register("birds7",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "birds7")));
    public static final DeferredHolder<SoundEvent, SoundEvent> BIRDS8 = SOUND_EVENTS.register("birds8",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "birds8")));
    public static final DeferredHolder<SoundEvent, SoundEvent> BIRDS9 = SOUND_EVENTS.register("birds9",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "birds9")));
    public static final DeferredHolder<SoundEvent, SoundEvent> CATERKILLER_DEATH = SOUND_EVENTS.register("caterkiller_death",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "caterkiller_death")));
    public static final DeferredHolder<SoundEvent, SoundEvent> CATERKILLER_HIT1 = SOUND_EVENTS.register("caterkiller_hit1",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "caterkiller_hit1")));
    public static final DeferredHolder<SoundEvent, SoundEvent> CATERKILLER_HIT2 = SOUND_EVENTS.register("caterkiller_hit2",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "caterkiller_hit2")));
    public static final DeferredHolder<SoundEvent, SoundEvent> CATERKILLER_HIT3 = SOUND_EVENTS.register("caterkiller_hit3",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "caterkiller_hit3")));
    public static final DeferredHolder<SoundEvent, SoundEvent> CATERKILLER_HIT4 = SOUND_EVENTS.register("caterkiller_hit4",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "caterkiller_hit4")));
    public static final DeferredHolder<SoundEvent, SoundEvent> CATERKILLER_LIVING1 = SOUND_EVENTS.register("caterkiller_living1",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "caterkiller_living1")));
    public static final DeferredHolder<SoundEvent, SoundEvent> CATERKILLER_LIVING2 = SOUND_EVENTS.register("caterkiller_living2",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "caterkiller_living2")));
    public static final DeferredHolder<SoundEvent, SoundEvent> CATERKILLER_LIVING3 = SOUND_EVENTS.register("caterkiller_living3",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "caterkiller_living3")));
    public static final DeferredHolder<SoundEvent, SoundEvent> CATERKILLER_LIVING4 = SOUND_EVENTS.register("caterkiller_living4",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "caterkiller_living4")));
    public static final DeferredHolder<SoundEvent, SoundEvent> CHAIN_RATTLES = SOUND_EVENTS.register("chain_rattles",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "chain_rattles")));
    public static final DeferredHolder<SoundEvent, SoundEvent> CHAINSAW = SOUND_EVENTS.register("chainsaw",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "chainsaw")));
    public static final DeferredHolder<SoundEvent, SoundEvent> CHAINSAWSHORT = SOUND_EVENTS.register("chainsawshort",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "chainsawshort")));
    public static final DeferredHolder<SoundEvent, SoundEvent> CLATTER1 = SOUND_EVENTS.register("clatter1",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "clatter1")));
    public static final DeferredHolder<SoundEvent, SoundEvent> CLATTER2 = SOUND_EVENTS.register("clatter2",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "clatter2")));
    public static final DeferredHolder<SoundEvent, SoundEvent> CLIFFRACER = SOUND_EVENTS.register("cliffracer",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "cliffracer")));
    public static final DeferredHolder<SoundEvent, SoundEvent> CREEPINGHORROR_DEAD = SOUND_EVENTS.register("creepinghorror_dead",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "creepinghorror_dead")));
    public static final DeferredHolder<SoundEvent, SoundEvent> CREEPINGHORROR_HIT = SOUND_EVENTS.register("creepinghorror_hit",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "creepinghorror_hit")));
    public static final DeferredHolder<SoundEvent, SoundEvent> CREEPINGHORROR_LIVING = SOUND_EVENTS.register("creepinghorror_living",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "creepinghorror_living")));
    public static final DeferredHolder<SoundEvent, SoundEvent> CRICKET = SOUND_EVENTS.register("cricket",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "cricket")));
    public static final DeferredHolder<SoundEvent, SoundEvent> CRUNCH = SOUND_EVENTS.register("crunch",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "crunch")));
    public static final DeferredHolder<SoundEvent, SoundEvent> CRYO_DEATH = SOUND_EVENTS.register("cryo_death",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "cryo_death")));
    public static final DeferredHolder<SoundEvent, SoundEvent> CRYO_HURT = SOUND_EVENTS.register("cryo_hurt",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "cryo_hurt")));
    public static final DeferredHolder<SoundEvent, SoundEvent> CRYO_LIVING = SOUND_EVENTS.register("cryo_living",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "cryo_living")));
    public static final DeferredHolder<SoundEvent, SoundEvent> DBDEAD = SOUND_EVENTS.register("dbdead",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "dbdead")));
    public static final DeferredHolder<SoundEvent, SoundEvent> DBHIT1 = SOUND_EVENTS.register("dbhit1",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "dbhit1")));
    public static final DeferredHolder<SoundEvent, SoundEvent> DBHIT2 = SOUND_EVENTS.register("dbhit2",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "dbhit2")));
    public static final DeferredHolder<SoundEvent, SoundEvent> DBHIT3 = SOUND_EVENTS.register("dbhit3",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "dbhit3")));
    public static final DeferredHolder<SoundEvent, SoundEvent> DRAGONFLY_DEATH = SOUND_EVENTS.register("dragonfly_death",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "dragonfly_death")));
    public static final DeferredHolder<SoundEvent, SoundEvent> DRAGONFLY_HURT = SOUND_EVENTS.register("dragonfly_hurt",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "dragonfly_hurt")));
    public static final DeferredHolder<SoundEvent, SoundEvent> DRAGONFLY_LIVING = SOUND_EVENTS.register("dragonfly_living",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "dragonfly_living")));
    public static final DeferredHolder<SoundEvent, SoundEvent> DUCK_HURT = SOUND_EVENTS.register("duck_hurt",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "duck_hurt")));
    public static final DeferredHolder<SoundEvent, SoundEvent> EMPERORSCORPION_DEATH = SOUND_EVENTS.register("emperorscorpion_death",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "emperorscorpion_death")));
    public static final DeferredHolder<SoundEvent, SoundEvent> FART1 = SOUND_EVENTS.register("fart1",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "fart1")));
    public static final DeferredHolder<SoundEvent, SoundEvent> FART2 = SOUND_EVENTS.register("fart2",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "fart2")));
    public static final DeferredHolder<SoundEvent, SoundEvent> FART3 = SOUND_EVENTS.register("fart3",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "fart3")));
    public static final DeferredHolder<SoundEvent, SoundEvent> FART4 = SOUND_EVENTS.register("fart4",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "fart4")));
    public static final DeferredHolder<SoundEvent, SoundEvent> FART5 = SOUND_EVENTS.register("fart5",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "fart5")));
    public static final DeferredHolder<SoundEvent, SoundEvent> FART6 = SOUND_EVENTS.register("fart6",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "fart6")));
    public static final DeferredHolder<SoundEvent, SoundEvent> FART7 = SOUND_EVENTS.register("fart7",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "fart7")));
    public static final DeferredHolder<SoundEvent, SoundEvent> FART8 = SOUND_EVENTS.register("fart8",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "fart8")));
    public static final DeferredHolder<SoundEvent, SoundEvent> FART9 = SOUND_EVENTS.register("fart9",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "fart9")));
    public static final DeferredHolder<SoundEvent, SoundEvent> FROG1 = SOUND_EVENTS.register("frog1",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "frog1")));
    public static final DeferredHolder<SoundEvent, SoundEvent> FROG2 = SOUND_EVENTS.register("frog2",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "frog2")));
    public static final DeferredHolder<SoundEvent, SoundEvent> GHOST_SOUND = SOUND_EVENTS.register("ghost_sound",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "ghost_sound")));
    public static final DeferredHolder<SoundEvent, SoundEvent> GLASSDEAD1 = SOUND_EVENTS.register("glassdead1",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "glassdead1")));
    public static final DeferredHolder<SoundEvent, SoundEvent> GLASSDEAD2 = SOUND_EVENTS.register("glassdead2",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "glassdead2")));
    public static final DeferredHolder<SoundEvent, SoundEvent> GLASSHIT1 = SOUND_EVENTS.register("glasshit1",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "glasshit1")));
    public static final DeferredHolder<SoundEvent, SoundEvent> GLASSHIT2 = SOUND_EVENTS.register("glasshit2",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "glasshit2")));
    public static final DeferredHolder<SoundEvent, SoundEvent> GLASSHIT3 = SOUND_EVENTS.register("glasshit3",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "glasshit3")));
    public static final DeferredHolder<SoundEvent, SoundEvent> GLASSHIT4 = SOUND_EVENTS.register("glasshit4",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "glasshit4")));
    public static final DeferredHolder<SoundEvent, SoundEvent> GLASSHIT5 = SOUND_EVENTS.register("glasshit5",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "glasshit5")));
    public static final DeferredHolder<SoundEvent, SoundEvent> GODZILLA_DEATH = SOUND_EVENTS.register("godzilla_death",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "godzilla_death")));
    public static final DeferredHolder<SoundEvent, SoundEvent> GODZILLA_LIVING = SOUND_EVENTS.register("godzilla_living",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "godzilla_living")));
    public static final DeferredHolder<SoundEvent, SoundEvent> HAMMERHEAD_DEATH = SOUND_EVENTS.register("hammerhead_death",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "hammerhead_death")));
    public static final DeferredHolder<SoundEvent, SoundEvent> HAMMERHEAD_LIVING1 = SOUND_EVENTS.register("hammerhead_living1",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "hammerhead_living1")));
    public static final DeferredHolder<SoundEvent, SoundEvent> HAMMERHEAD_LIVING2 = SOUND_EVENTS.register("hammerhead_living2",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "hammerhead_living2")));
    public static final DeferredHolder<SoundEvent, SoundEvent> HERCULES_DEATH = SOUND_EVENTS.register("hercules_death",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "hercules_death")));
    public static final DeferredHolder<SoundEvent, SoundEvent> HOVER1 = SOUND_EVENTS.register("hover1",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "hover1")));
    public static final DeferredHolder<SoundEvent, SoundEvent> HOVER2 = SOUND_EVENTS.register("hover2",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "hover2")));
    public static final DeferredHolder<SoundEvent, SoundEvent> HOVER3 = SOUND_EVENTS.register("hover3",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "hover3")));
    public static final DeferredHolder<SoundEvent, SoundEvent> HOVER4 = SOUND_EVENTS.register("hover4",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "hover4")));
    public static final DeferredHolder<SoundEvent, SoundEvent> HOVER5 = SOUND_EVENTS.register("hover5",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "hover5")));
    public static final DeferredHolder<SoundEvent, SoundEvent> HOVER6 = SOUND_EVENTS.register("hover6",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "hover6")));
    public static final DeferredHolder<SoundEvent, SoundEvent> KING_HIT = SOUND_EVENTS.register("king_hit",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "king_hit")));
    public static final DeferredHolder<SoundEvent, SoundEvent> KING_LIVING = SOUND_EVENTS.register("king_living",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "king_living")));
    public static final DeferredHolder<SoundEvent, SoundEvent> KRAKEN_LIVING = SOUND_EVENTS.register("kraken_living",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "kraken_living")));
    public static final DeferredHolder<SoundEvent, SoundEvent> KYUUBI_LIVING = SOUND_EVENTS.register("kyuubi_living",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "kyuubi_living")));
    public static final DeferredHolder<SoundEvent, SoundEvent> LEAVES_DEATH = SOUND_EVENTS.register("leaves_death",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "leaves_death")));
    public static final DeferredHolder<SoundEvent, SoundEvent> LEAVES_HIT = SOUND_EVENTS.register("leaves_hit",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "leaves_hit")));
    public static final DeferredHolder<SoundEvent, SoundEvent> LEON_DEATH = SOUND_EVENTS.register("leon_death",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "leon_death")));
    public static final DeferredHolder<SoundEvent, SoundEvent> LEON_HIT1 = SOUND_EVENTS.register("leon_hit1",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "leon_hit1")));
    public static final DeferredHolder<SoundEvent, SoundEvent> LEON_HIT2 = SOUND_EVENTS.register("leon_hit2",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "leon_hit2")));
    public static final DeferredHolder<SoundEvent, SoundEvent> LEON_HIT3 = SOUND_EVENTS.register("leon_hit3",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "leon_hit3")));
    public static final DeferredHolder<SoundEvent, SoundEvent> LEON_LIVING = SOUND_EVENTS.register("leon_living",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "leon_living")));
    public static final DeferredHolder<SoundEvent, SoundEvent> LITTLE_SPLAT = SOUND_EVENTS.register("little_splat",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "little_splat")));
    public static final DeferredHolder<SoundEvent, SoundEvent> LURKINGHORROR_DEAD = SOUND_EVENTS.register("lurkinghorror_dead",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "lurkinghorror_dead")));
    public static final DeferredHolder<SoundEvent, SoundEvent> LURKINGHORROR_HIT = SOUND_EVENTS.register("lurkinghorror_hit",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "lurkinghorror_hit")));
    public static final DeferredHolder<SoundEvent, SoundEvent> LURKINGHORROR_LIVING = SOUND_EVENTS.register("lurkinghorror_living",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "lurkinghorror_living")));
    public static final DeferredHolder<SoundEvent, SoundEvent> MOLENOID_DEATH = SOUND_EVENTS.register("molenoid_death",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "molenoid_death")));
    public static final DeferredHolder<SoundEvent, SoundEvent> MOLENOID_HIT1 = SOUND_EVENTS.register("molenoid_hit1",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "molenoid_hit1")));
    public static final DeferredHolder<SoundEvent, SoundEvent> MOLENOID_HIT2 = SOUND_EVENTS.register("molenoid_hit2",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "molenoid_hit2")));
    public static final DeferredHolder<SoundEvent, SoundEvent> MOLENOID_HIT3 = SOUND_EVENTS.register("molenoid_hit3",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "molenoid_hit3")));
    public static final DeferredHolder<SoundEvent, SoundEvent> MOLENOID_HIT4 = SOUND_EVENTS.register("molenoid_hit4",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "molenoid_hit4")));
    public static final DeferredHolder<SoundEvent, SoundEvent> MOLENOID_HIT5 = SOUND_EVENTS.register("molenoid_hit5",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "molenoid_hit5")));
    public static final DeferredHolder<SoundEvent, SoundEvent> MOLENOID_HIT6 = SOUND_EVENTS.register("molenoid_hit6",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "molenoid_hit6")));
    public static final DeferredHolder<SoundEvent, SoundEvent> MOLENOID_LIVING1 = SOUND_EVENTS.register("molenoid_living1",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "molenoid_living1")));
    public static final DeferredHolder<SoundEvent, SoundEvent> MOLENOID_LIVING2 = SOUND_EVENTS.register("molenoid_living2",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "molenoid_living2")));
    public static final DeferredHolder<SoundEvent, SoundEvent> MOLENOID_LIVING3 = SOUND_EVENTS.register("molenoid_living3",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "molenoid_living3")));
    public static final DeferredHolder<SoundEvent, SoundEvent> MOSQUITO = SOUND_EVENTS.register("mosquito",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "mosquito")));
    public static final DeferredHolder<SoundEvent, SoundEvent> MOTHRAWINGS1 = SOUND_EVENTS.register("MothraWings1",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "MothraWings1")));
    public static final DeferredHolder<SoundEvent, SoundEvent> MOTHRAWINGS2 = SOUND_EVENTS.register("MothraWings2",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "MothraWings2")));
    public static final DeferredHolder<SoundEvent, SoundEvent> MOTHRAWINGS3 = SOUND_EVENTS.register("MothraWings3",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "MothraWings3")));
    public static final DeferredHolder<SoundEvent, SoundEvent> O_DARK = SOUND_EVENTS.register("o_dark",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "o_dark")));
    public static final DeferredHolder<SoundEvent, SoundEvent> O_DEATH_GIRLFRIEND = SOUND_EVENTS.register("o_death_girlfriend",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "o_death_girlfriend")));
    public static final DeferredHolder<SoundEvent, SoundEvent> O_DEATH_SINGLE = SOUND_EVENTS.register("o_death_single",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "o_death_single")));
    public static final DeferredHolder<SoundEvent, SoundEvent> O_FIGHT1 = SOUND_EVENTS.register("o_fight1",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "o_fight1")));
    public static final DeferredHolder<SoundEvent, SoundEvent> O_FIGHT2 = SOUND_EVENTS.register("o_fight2",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "o_fight2")));
    public static final DeferredHolder<SoundEvent, SoundEvent> O_FIGHT3 = SOUND_EVENTS.register("o_fight3",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "o_fight3")));
    public static final DeferredHolder<SoundEvent, SoundEvent> O_FIGHT4 = SOUND_EVENTS.register("o_fight4",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "o_fight4")));
    public static final DeferredHolder<SoundEvent, SoundEvent> O_FIGHT5 = SOUND_EVENTS.register("o_fight5",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "o_fight5")));
    public static final DeferredHolder<SoundEvent, SoundEvent> O_FIGHT6 = SOUND_EVENTS.register("o_fight6",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "o_fight6")));
    public static final DeferredHolder<SoundEvent, SoundEvent> O_FIGHT7 = SOUND_EVENTS.register("o_fight7",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "o_fight7")));
    public static final DeferredHolder<SoundEvent, SoundEvent> O_HAPPY1 = SOUND_EVENTS.register("o_happy1",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "o_happy1")));
    public static final DeferredHolder<SoundEvent, SoundEvent> O_HAPPY2 = SOUND_EVENTS.register("o_happy2",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "o_happy2")));
    public static final DeferredHolder<SoundEvent, SoundEvent> O_HAPPY3 = SOUND_EVENTS.register("o_happy3",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "o_happy3")));
    public static final DeferredHolder<SoundEvent, SoundEvent> O_HAPPY4 = SOUND_EVENTS.register("o_happy4",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "o_happy4")));
    public static final DeferredHolder<SoundEvent, SoundEvent> O_HAPPY5 = SOUND_EVENTS.register("o_happy5",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "o_happy5")));
    public static final DeferredHolder<SoundEvent, SoundEvent> O_HAPPY6 = SOUND_EVENTS.register("o_happy6",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "o_happy6")));
    public static final DeferredHolder<SoundEvent, SoundEvent> O_HAPPY7 = SOUND_EVENTS.register("o_happy7",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "o_happy7")));
    public static final DeferredHolder<SoundEvent, SoundEvent> O_HURT1 = SOUND_EVENTS.register("o_hurt1",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "o_hurt1")));
    public static final DeferredHolder<SoundEvent, SoundEvent> O_HURT2 = SOUND_EVENTS.register("o_hurt2",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "o_hurt2")));
    public static final DeferredHolder<SoundEvent, SoundEvent> O_HURT3 = SOUND_EVENTS.register("o_hurt3",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "o_hurt3")));
    public static final DeferredHolder<SoundEvent, SoundEvent> O_HURT4 = SOUND_EVENTS.register("o_hurt4",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "o_hurt4")));
    public static final DeferredHolder<SoundEvent, SoundEvent> O_HURT5 = SOUND_EVENTS.register("o_hurt5",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "o_hurt5")));
    public static final DeferredHolder<SoundEvent, SoundEvent> O_HURT6 = SOUND_EVENTS.register("o_hurt6",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "o_hurt6")));
    public static final DeferredHolder<SoundEvent, SoundEvent> O_HURT7 = SOUND_EVENTS.register("o_hurt7",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "o_hurt7")));
    public static final DeferredHolder<SoundEvent, SoundEvent> O_HURT8 = SOUND_EVENTS.register("o_hurt8",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "o_hurt8")));
    public static final DeferredHolder<SoundEvent, SoundEvent> O_HURT9 = SOUND_EVENTS.register("o_hurt9",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "o_hurt9")));
    public static final DeferredHolder<SoundEvent, SoundEvent> O_OW1 = SOUND_EVENTS.register("o_ow1",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "o_ow1")));
    public static final DeferredHolder<SoundEvent, SoundEvent> O_OW2 = SOUND_EVENTS.register("o_ow2",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "o_ow2")));
    public static final DeferredHolder<SoundEvent, SoundEvent> O_OW3 = SOUND_EVENTS.register("o_ow3",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "o_ow3")));
    public static final DeferredHolder<SoundEvent, SoundEvent> O_OW4 = SOUND_EVENTS.register("o_ow4",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "o_ow4")));
    public static final DeferredHolder<SoundEvent, SoundEvent> O_OW5 = SOUND_EVENTS.register("o_ow5",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "o_ow5")));
    public static final DeferredHolder<SoundEvent, SoundEvent> O_OW6 = SOUND_EVENTS.register("o_ow6",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "o_ow6")));
    public static final DeferredHolder<SoundEvent, SoundEvent> O_OW7 = SOUND_EVENTS.register("o_ow7",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "o_ow7")));
    public static final DeferredHolder<SoundEvent, SoundEvent> O_OW8 = SOUND_EVENTS.register("o_ow8",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "o_ow8")));
    public static final DeferredHolder<SoundEvent, SoundEvent> O_RAIN = SOUND_EVENTS.register("o_rain",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "o_rain")));
    public static final DeferredHolder<SoundEvent, SoundEvent> O_TAUNT1 = SOUND_EVENTS.register("o_taunt1",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "o_taunt1")));
    public static final DeferredHolder<SoundEvent, SoundEvent> O_TAUNT2 = SOUND_EVENTS.register("o_taunt2",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "o_taunt2")));
    public static final DeferredHolder<SoundEvent, SoundEvent> O_TAUNT3 = SOUND_EVENTS.register("o_taunt3",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "o_taunt3")));
    public static final DeferredHolder<SoundEvent, SoundEvent> O_TAUNT4 = SOUND_EVENTS.register("o_taunt4",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "o_taunt4")));
    public static final DeferredHolder<SoundEvent, SoundEvent> O_THUNDER = SOUND_EVENTS.register("o_thunder",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "o_thunder")));
    public static final DeferredHolder<SoundEvent, SoundEvent> O_WATER1 = SOUND_EVENTS.register("o_water1",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "o_water1")));
    public static final DeferredHolder<SoundEvent, SoundEvent> O_WATER2 = SOUND_EVENTS.register("o_water2",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "o_water2")));
    public static final DeferredHolder<SoundEvent, SoundEvent> O_WOOHOO1 = SOUND_EVENTS.register("o_woohoo1",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "o_woohoo1")));
    public static final DeferredHolder<SoundEvent, SoundEvent> O_WOOHOO2 = SOUND_EVENTS.register("o_woohoo2",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "o_woohoo2")));
    public static final DeferredHolder<SoundEvent, SoundEvent> O_WOOHOO3 = SOUND_EVENTS.register("o_woohoo3",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "o_woohoo3")));
    public static final DeferredHolder<SoundEvent, SoundEvent> O_WOOHOO4 = SOUND_EVENTS.register("o_woohoo4",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "o_woohoo4")));
    public static final DeferredHolder<SoundEvent, SoundEvent> PEACOCKDEAD = SOUND_EVENTS.register("peacockdead",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "peacockdead")));
    public static final DeferredHolder<SoundEvent, SoundEvent> PEACOCKHIT = SOUND_EVENTS.register("peacockhit",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "peacockhit")));
    public static final DeferredHolder<SoundEvent, SoundEvent> PEACOCKLIVE = SOUND_EVENTS.register("peacocklive",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "peacocklive")));
    public static final DeferredHolder<SoundEvent, SoundEvent> PITCHBLACK_DEAD = SOUND_EVENTS.register("pitchblack_dead",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "pitchblack_dead")));
    public static final DeferredHolder<SoundEvent, SoundEvent> PITCHBLACK_HIT = SOUND_EVENTS.register("pitchblack_hit",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "pitchblack_hit")));
    public static final DeferredHolder<SoundEvent, SoundEvent> PITCHBLACK_LIVING = SOUND_EVENTS.register("pitchblack_living",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "pitchblack_living")));
    public static final DeferredHolder<SoundEvent, SoundEvent> RATDEAD1 = SOUND_EVENTS.register("ratdead1",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "ratdead1")));
    public static final DeferredHolder<SoundEvent, SoundEvent> RATDEAD2 = SOUND_EVENTS.register("ratdead2",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "ratdead2")));
    public static final DeferredHolder<SoundEvent, SoundEvent> RATDEAD3 = SOUND_EVENTS.register("ratdead3",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "ratdead3")));
    public static final DeferredHolder<SoundEvent, SoundEvent> RATHIT = SOUND_EVENTS.register("rathit",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "rathit")));
    public static final DeferredHolder<SoundEvent, SoundEvent> RATLIVE = SOUND_EVENTS.register("ratlive",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "ratlive")));
    public static final DeferredHolder<SoundEvent, SoundEvent> ROAR1 = SOUND_EVENTS.register("roar1",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "roar1")));
    public static final DeferredHolder<SoundEvent, SoundEvent> ROAR2 = SOUND_EVENTS.register("roar2",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "roar2")));
    public static final DeferredHolder<SoundEvent, SoundEvent> ROAR3 = SOUND_EVENTS.register("roar3",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "roar3")));
    public static final DeferredHolder<SoundEvent, SoundEvent> ROAR4 = SOUND_EVENTS.register("roar4",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "roar4")));
    public static final DeferredHolder<SoundEvent, SoundEvent> ROAR5 = SOUND_EVENTS.register("roar5",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "roar5")));
    public static final DeferredHolder<SoundEvent, SoundEvent> ROAR6 = SOUND_EVENTS.register("roar6",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "roar6")));
    public static final DeferredHolder<SoundEvent, SoundEvent> ROBOT_DEATH1 = SOUND_EVENTS.register("robot_death1",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "robot_death1")));
    public static final DeferredHolder<SoundEvent, SoundEvent> ROBOT_DEATH2 = SOUND_EVENTS.register("robot_death2",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "robot_death2")));
    public static final DeferredHolder<SoundEvent, SoundEvent> ROBOT_DEATH3 = SOUND_EVENTS.register("robot_death3",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "robot_death3")));
    public static final DeferredHolder<SoundEvent, SoundEvent> ROBOT_DEATH4 = SOUND_EVENTS.register("robot_death4",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "robot_death4")));
    public static final DeferredHolder<SoundEvent, SoundEvent> ROBOT_DEATH5 = SOUND_EVENTS.register("robot_death5",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "robot_death5")));
    public static final DeferredHolder<SoundEvent, SoundEvent> ROBOT_HURT1 = SOUND_EVENTS.register("robot_hurt1",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "robot_hurt1")));
    public static final DeferredHolder<SoundEvent, SoundEvent> ROBOT_HURT2 = SOUND_EVENTS.register("robot_hurt2",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "robot_hurt2")));
    public static final DeferredHolder<SoundEvent, SoundEvent> ROBOT_HURT3 = SOUND_EVENTS.register("robot_hurt3",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "robot_hurt3")));
    public static final DeferredHolder<SoundEvent, SoundEvent> ROBOT_HURT4 = SOUND_EVENTS.register("robot_hurt4",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "robot_hurt4")));
    public static final DeferredHolder<SoundEvent, SoundEvent> ROBOT_HURT5 = SOUND_EVENTS.register("robot_hurt5",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "robot_hurt5")));
    public static final DeferredHolder<SoundEvent, SoundEvent> ROBOT_HURT6 = SOUND_EVENTS.register("robot_hurt6",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "robot_hurt6")));
    public static final DeferredHolder<SoundEvent, SoundEvent> ROBOT_HURT7 = SOUND_EVENTS.register("robot_hurt7",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "robot_hurt7")));
    public static final DeferredHolder<SoundEvent, SoundEvent> ROBOT_HURT8 = SOUND_EVENTS.register("robot_hurt8",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "robot_hurt8")));
    public static final DeferredHolder<SoundEvent, SoundEvent> ROBOT_LIVING1 = SOUND_EVENTS.register("robot_living1",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "robot_living1")));
    public static final DeferredHolder<SoundEvent, SoundEvent> ROBOT_LIVING2 = SOUND_EVENTS.register("robot_living2",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "robot_living2")));
    public static final DeferredHolder<SoundEvent, SoundEvent> ROBOT_LIVING3 = SOUND_EVENTS.register("robot_living3",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "robot_living3")));
    public static final DeferredHolder<SoundEvent, SoundEvent> ROBOT_LIVING4 = SOUND_EVENTS.register("robot_living4",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "robot_living4")));
    public static final DeferredHolder<SoundEvent, SoundEvent> ROBOT1_DEATH = SOUND_EVENTS.register("robot1_death",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "robot1_death")));
    public static final DeferredHolder<SoundEvent, SoundEvent> ROBOTSPIDER1 = SOUND_EVENTS.register("robotspider1",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "robotspider1")));
    public static final DeferredHolder<SoundEvent, SoundEvent> ROBOTSPIDER10 = SOUND_EVENTS.register("robotspider10",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "robotspider10")));
    public static final DeferredHolder<SoundEvent, SoundEvent> ROBOTSPIDER11 = SOUND_EVENTS.register("robotspider11",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "robotspider11")));
    public static final DeferredHolder<SoundEvent, SoundEvent> ROBOTSPIDER2 = SOUND_EVENTS.register("robotspider2",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "robotspider2")));
    public static final DeferredHolder<SoundEvent, SoundEvent> ROBOTSPIDER3 = SOUND_EVENTS.register("robotspider3",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "robotspider3")));
    public static final DeferredHolder<SoundEvent, SoundEvent> ROBOTSPIDER4 = SOUND_EVENTS.register("robotspider4",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "robotspider4")));
    public static final DeferredHolder<SoundEvent, SoundEvent> ROBOTSPIDER5 = SOUND_EVENTS.register("robotspider5",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "robotspider5")));
    public static final DeferredHolder<SoundEvent, SoundEvent> ROBOTSPIDER6 = SOUND_EVENTS.register("robotspider6",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "robotspider6")));
    public static final DeferredHolder<SoundEvent, SoundEvent> ROBOTSPIDER7 = SOUND_EVENTS.register("robotspider7",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "robotspider7")));
    public static final DeferredHolder<SoundEvent, SoundEvent> ROBOTSPIDER8 = SOUND_EVENTS.register("robotspider8",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "robotspider8")));
    public static final DeferredHolder<SoundEvent, SoundEvent> ROBOTSPIDER9 = SOUND_EVENTS.register("robotspider9",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "robotspider9")));
    public static final DeferredHolder<SoundEvent, SoundEvent> ROBOTSPIDERMOUNT = SOUND_EVENTS.register("robotspidermount",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "robotspidermount")));
    public static final DeferredHolder<SoundEvent, SoundEvent> RUBYBIRD = SOUND_EVENTS.register("rubybird",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "rubybird")));
    public static final DeferredHolder<SoundEvent, SoundEvent> SCORPION_ATTACK = SOUND_EVENTS.register("scorpion_attack",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "scorpion_attack")));
    public static final DeferredHolder<SoundEvent, SoundEvent> SCORPION_HIT = SOUND_EVENTS.register("scorpion_hit",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "scorpion_hit")));
    public static final DeferredHolder<SoundEvent, SoundEvent> SCORPION_LIVING = SOUND_EVENTS.register("scorpion_living",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "scorpion_living")));
    public static final DeferredHolder<SoundEvent, SoundEvent> SEAMONSTER_DEATH = SOUND_EVENTS.register("seamonster_death",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "seamonster_death")));
    public static final DeferredHolder<SoundEvent, SoundEvent> SEAMONSTER_HIT = SOUND_EVENTS.register("seamonster_hit",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "seamonster_hit")));
    public static final DeferredHolder<SoundEvent, SoundEvent> SEAMONSTER_LIVING1 = SOUND_EVENTS.register("seamonster_living1",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "seamonster_living1")));
    public static final DeferredHolder<SoundEvent, SoundEvent> SEAMONSTER_LIVING2 = SOUND_EVENTS.register("seamonster_living2",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "seamonster_living2")));
    public static final DeferredHolder<SoundEvent, SoundEvent> SEAVIPER_DEATH = SOUND_EVENTS.register("seaviper_death",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "seaviper_death")));
    public static final DeferredHolder<SoundEvent, SoundEvent> SEAVIPER_HIT1 = SOUND_EVENTS.register("seaviper_hit1",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "seaviper_hit1")));
    public static final DeferredHolder<SoundEvent, SoundEvent> SEAVIPER_HIT2 = SOUND_EVENTS.register("seaviper_hit2",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "seaviper_hit2")));
    public static final DeferredHolder<SoundEvent, SoundEvent> SEAVIPER_HIT3 = SOUND_EVENTS.register("seaviper_hit3",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "seaviper_hit3")));
    public static final DeferredHolder<SoundEvent, SoundEvent> SEAVIPER_LIVING = SOUND_EVENTS.register("seaviper_living",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "seaviper_living")));
    public static final DeferredHolder<SoundEvent, SoundEvent> SQUID_DEATH1 = SOUND_EVENTS.register("squid_death1",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "squid_death1")));
    public static final DeferredHolder<SoundEvent, SoundEvent> SQUID_DEATH2 = SOUND_EVENTS.register("squid_death2",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "squid_death2")));
    public static final DeferredHolder<SoundEvent, SoundEvent> SQUID_HURT1 = SOUND_EVENTS.register("squid_hurt1",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "squid_hurt1")));
    public static final DeferredHolder<SoundEvent, SoundEvent> SQUID_HURT2 = SOUND_EVENTS.register("squid_hurt2",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "squid_hurt2")));
    public static final DeferredHolder<SoundEvent, SoundEvent> SQUID_HURT3 = SOUND_EVENTS.register("squid_hurt3",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "squid_hurt3")));
    public static final DeferredHolder<SoundEvent, SoundEvent> SQUID_HURT4 = SOUND_EVENTS.register("squid_hurt4",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "squid_hurt4")));
    public static final DeferredHolder<SoundEvent, SoundEvent> TERRIBLETERROR_DEAD = SOUND_EVENTS.register("terribleterror_dead",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "terribleterror_dead")));
    public static final DeferredHolder<SoundEvent, SoundEvent> TERRIBLETERROR_HIT = SOUND_EVENTS.register("terribleterror_hit",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "terribleterror_hit")));
    public static final DeferredHolder<SoundEvent, SoundEvent> TERRIBLETERROR_LIVING = SOUND_EVENTS.register("terribleterror_living",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "terribleterror_living")));
    public static final DeferredHolder<SoundEvent, SoundEvent> TREX_DEATH = SOUND_EVENTS.register("trex_death",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "trex_death")));
    public static final DeferredHolder<SoundEvent, SoundEvent> TREX_LIVING = SOUND_EVENTS.register("trex_living",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "trex_living")));
    public static final DeferredHolder<SoundEvent, SoundEvent> TRIFFID_DEAD = SOUND_EVENTS.register("triffid_dead",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "triffid_dead")));
    public static final DeferredHolder<SoundEvent, SoundEvent> TRIFFID_HIT = SOUND_EVENTS.register("triffid_hit",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "triffid_hit")));
    public static final DeferredHolder<SoundEvent, SoundEvent> TRIFFID_LIVING = SOUND_EVENTS.register("triffid_living",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "triffid_living")));
    public static final DeferredHolder<SoundEvent, SoundEvent> VORTEXLIVE = SOUND_EVENTS.register("vortexlive",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "vortexlive")));
    public static final DeferredHolder<SoundEvent, SoundEvent> WATERDRAGON_DEATH = SOUND_EVENTS.register("waterdragon_death",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "waterdragon_death")));
    public static final DeferredHolder<SoundEvent, SoundEvent> WATERDRAGON_HURT1 = SOUND_EVENTS.register("waterdragon_hurt1",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "waterdragon_hurt1")));
    public static final DeferredHolder<SoundEvent, SoundEvent> WATERDRAGON_HURT2 = SOUND_EVENTS.register("waterdragon_hurt2",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "waterdragon_hurt2")));
    public static final DeferredHolder<SoundEvent, SoundEvent> WATERDRAGON_HURT3 = SOUND_EVENTS.register("waterdragon_hurt3",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "waterdragon_hurt3")));
    public static final DeferredHolder<SoundEvent, SoundEvent> WTF_HURT = SOUND_EVENTS.register("wtf_hurt",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "wtf_hurt")));
    public static final DeferredHolder<SoundEvent, SoundEvent> WTF_LIVING = SOUND_EVENTS.register("wtf_living",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "wtf_living")));

    public static void register(IEventBus eventBus) {
        SOUND_EVENTS.register(eventBus);
    }
}
