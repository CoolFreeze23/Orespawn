/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.util.ResourceLocation
 *  net.minecraft.util.SoundEvent
 *  net.minecraftforge.fml.common.registry.ForgeRegistries
 *  net.minecraftforge.registries.IForgeRegistryEntry
 */
package danger.orespawn.util.handlers;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistryEntry;

public class SoundsHandler {
    public static SoundEvent LITTLE_SPLAT;
    public static SoundEvent BIG_SPLAT;
    public static SoundEvent ENTITY_ALOSAURUS_LIVING;
    public static SoundEvent ENTITY_ALOSAURUS_HURT;
    public static SoundEvent ENTITY_ALOSAURUS_DEATH;
    public static SoundEvent ENTITY_TREX_AMBIENT;
    public static SoundEvent ENTITY_TREX_DEATH;
    public static SoundEvent ENTITY_DUCK_HURT;
    public static SoundEvent ENTITY_CRYO_HURT;
    public static SoundEvent ENTITY_CRYO_DEATH;
    public static SoundEvent ENTITY_CRYO_LIVING;
    public static SoundEvent ENTITY_BIRD_BIRD1;
    public static SoundEvent ENTITY_BIRD_BIRD2;
    public static SoundEvent ENTITY_BIRD_BIRD3;
    public static SoundEvent ENTITY_BIRD_BIRD4;
    public static SoundEvent ENTITY_BIRD_BIRD5;
    public static SoundEvent ENTITY_BIRD_BIRD6;
    public static SoundEvent ENTITY_BIRD_BIRD7;
    public static SoundEvent ENTITY_BIRD_BIRD8;
    public static SoundEvent ENTITY_BIRD_BIRD9;
    public static SoundEvent ENTITY_BIRD_BIRD10;
    public static SoundEvent ENTITY_BIRD_BIRD11;
    public static SoundEvent ENTITY_BIRD_BIRD12;
    public static SoundEvent ENTITY_BIRD_BIRD13;
    public static SoundEvent ENTITY_BIRD_BIRD14;
    public static SoundEvent ENTITY_BIRD_BIRD15;
    public static SoundEvent ENTITY_BIRD_BIRD16;
    public static SoundEvent ENTITY_BIRD_BIRD17;
    public static SoundEvent ENTITY_BIRD_BIRD18;
    public static SoundEvent ENTITY_BIRD_BIRD19;
    public static SoundEvent ENTITY_BIRD_BIRD20;
    public static SoundEvent ENTITY_BIRD_BIRD21;
    public static SoundEvent ENTITY_BIRD_BIRD22;
    public static SoundEvent ENTITY_BIRD_BIRD23;
    public static SoundEvent ENTITY_GAMMAMETROID_LIVING;
    public static SoundEvent ENTITY_DRAGONFLY_LIVING;
    public static SoundEvent ENTITY_DRAGONFLY_HURT;
    public static SoundEvent ENTITY_MOSQUITO_LIVING;
    public static SoundEvent ENTITY_ALIEN_LIVING;
    public static SoundEvent ENTITY_ALIEN_HURT;
    public static SoundEvent ENTITY_CATERKILLER_LIVING1;
    public static SoundEvent ENTITY_CATERKILLER_LIVING2;
    public static SoundEvent ENTITY_CATERKILLER_LIVING3;
    public static SoundEvent ENTITY_CATERKILLER_LIVING4;
    public static SoundEvent ENTITY_SCORPION_HIT;
    public static SoundEvent CHAINSAW;
    public static SoundEvent ENTITY_MOTHRA_WINGS;
    public static SoundEvent ENTITY_KYUUBI_LIVING;
    public static SoundEvent ENTITY_STINKBUG_FART1;
    public static SoundEvent ENTITY_STINKBUG_FART2;
    public static SoundEvent ENTITY_STINKBUG_FART3;
    public static SoundEvent ENTITY_STINKBUG_FART4;
    public static SoundEvent ENTITY_STINKBUG_FART5;
    public static SoundEvent ENTITY_STINKBUG_FART6;
    public static SoundEvent ENTITY_STINKBUG_FART7;
    public static SoundEvent ENTITY_STINKBUG_FART8;
    public static SoundEvent ENTITY_STINKBUG_FART9;

    public static void registerSounds() {
        LITTLE_SPLAT = SoundsHandler.registerSound("little_splat");
        BIG_SPLAT = SoundsHandler.registerSound("big_splat");
        ENTITY_ALOSAURUS_LIVING = SoundsHandler.registerSound("entity.alosaurus.living");
        ENTITY_ALOSAURUS_HURT = SoundsHandler.registerSound("entity.alosaurus.hurt");
        ENTITY_ALOSAURUS_DEATH = SoundsHandler.registerSound("entity.alosaurus.death");
        ENTITY_TREX_AMBIENT = SoundsHandler.registerSound("entity.trex.ambient");
        ENTITY_TREX_DEATH = SoundsHandler.registerSound("entity.trex.death");
        ENTITY_DUCK_HURT = SoundsHandler.registerSound("entity.duck.hurt");
        ENTITY_CRYO_HURT = SoundsHandler.registerSound("entity.cryo.hurt");
        ENTITY_CRYO_DEATH = SoundsHandler.registerSound("entity.cryo.death");
        ENTITY_CRYO_LIVING = SoundsHandler.registerSound("entity.cryo.living");
        ENTITY_BIRD_BIRD1 = SoundsHandler.registerSound("entity.birds.birds1");
        ENTITY_BIRD_BIRD2 = SoundsHandler.registerSound("entity.birds.birds2");
        ENTITY_BIRD_BIRD3 = SoundsHandler.registerSound("entity.birds.birds3");
        ENTITY_BIRD_BIRD4 = SoundsHandler.registerSound("entity.birds.birds4");
        ENTITY_BIRD_BIRD5 = SoundsHandler.registerSound("entity.birds.birds5");
        ENTITY_BIRD_BIRD6 = SoundsHandler.registerSound("entity.birds.birds6");
        ENTITY_BIRD_BIRD7 = SoundsHandler.registerSound("entity.birds.birds7");
        ENTITY_BIRD_BIRD8 = SoundsHandler.registerSound("entity.birds.birds8");
        ENTITY_BIRD_BIRD9 = SoundsHandler.registerSound("entity.birds.birds9");
        ENTITY_BIRD_BIRD10 = SoundsHandler.registerSound("entity.birds.birds10");
        ENTITY_BIRD_BIRD11 = SoundsHandler.registerSound("entity.birds.birds11");
        ENTITY_BIRD_BIRD12 = SoundsHandler.registerSound("entity.birds.birds12");
        ENTITY_BIRD_BIRD13 = SoundsHandler.registerSound("entity.birds.birds13");
        ENTITY_BIRD_BIRD14 = SoundsHandler.registerSound("entity.birds.birds14");
        ENTITY_BIRD_BIRD15 = SoundsHandler.registerSound("entity.birds.birds15");
        ENTITY_BIRD_BIRD16 = SoundsHandler.registerSound("entity.birds.birds16");
        ENTITY_BIRD_BIRD17 = SoundsHandler.registerSound("entity.birds.birds17");
        ENTITY_BIRD_BIRD18 = SoundsHandler.registerSound("entity.birds.birds18");
        ENTITY_BIRD_BIRD19 = SoundsHandler.registerSound("entity.birds.birds19");
        ENTITY_BIRD_BIRD20 = SoundsHandler.registerSound("entity.birds.birds20");
        ENTITY_BIRD_BIRD21 = SoundsHandler.registerSound("entity.birds.birds21");
        ENTITY_BIRD_BIRD22 = SoundsHandler.registerSound("entity.birds.birds22");
        ENTITY_BIRD_BIRD23 = SoundsHandler.registerSound("entity.birds.birds23");
        ENTITY_GAMMAMETROID_LIVING = SoundsHandler.registerSound("entity.gammametroid.living");
        ENTITY_ALIEN_LIVING = SoundsHandler.registerSound("entity.alien.living");
        ENTITY_ALIEN_HURT = SoundsHandler.registerSound("entity.alien.hurt");
        ENTITY_MOTHRA_WINGS = SoundsHandler.registerSound("entity.mothra.wings");
        ENTITY_STINKBUG_FART1 = SoundsHandler.registerSound("entity.stinkbug.fart1");
        ENTITY_STINKBUG_FART2 = SoundsHandler.registerSound("entity.stinkbug.fart2");
        ENTITY_STINKBUG_FART3 = SoundsHandler.registerSound("entity.stinkbug.fart3");
        ENTITY_STINKBUG_FART4 = SoundsHandler.registerSound("entity.stinkbug.fart4");
        ENTITY_STINKBUG_FART5 = SoundsHandler.registerSound("entity.stinkbug.fart5");
        ENTITY_STINKBUG_FART6 = SoundsHandler.registerSound("entity.stinkbug.fart6");
        ENTITY_STINKBUG_FART7 = SoundsHandler.registerSound("entity.stinkbug.fart7");
        ENTITY_STINKBUG_FART8 = SoundsHandler.registerSound("entity.stinkbug.fart8");
        ENTITY_STINKBUG_FART9 = SoundsHandler.registerSound("entity.stinkbug.fart9");
    }

    private static SoundEvent registerSound(String name) {
        ResourceLocation location = new ResourceLocation("orespawn", name);
        SoundEvent event = new SoundEvent(location);
        event.setRegistryName(name);
        ForgeRegistries.SOUND_EVENTS.register((IForgeRegistryEntry)event);
        return event;
    }
}

