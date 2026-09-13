#!/usr/bin/env python3
"""Pins for tools/artist_package.py (Phase G slice (f)) — a `unittest` runner, no pytest.

    python tools/test_artist_package.py

The fixtures are a synthetic repository built in a temp directory: eight registrations (both `Builder.of` and
`Builder.<X>of` forms; a projectile, a vanilla-cow reuse, a head sidecar that renders nothing, a native GeoEntity boss
with two controllers and an area-damage helper, an EVENT pulser whose hurt() also raises the flag, a MIXED ticker
species), a nested rig (per-face UV with a flipped face, a box-UV mirrored cube with its own pivot and rotation, an
unknown description key), three clips in the three keyframe shapes plus an easing key, hitbox profiles, seeds, twin
and series textures, an armor-sheet stray, an item-renderer texture, a dormant item twin, and a referenced-vs-
registry-aligned canonical conflict. Every pin runs the production code paths (Repo, TextureCatalog,
build_trigger_inventory, spec_document, build_bbmodel, bbmodel_to_geo/animation, roundtrip_diff, build_package,
check_folder).
"""

from __future__ import annotations

import json
import shutil
import struct
import sys
import tempfile
import unittest
import zlib
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))
import artist_package as ap  # noqa: E402


def png_bytes(width: int, height: int, seed: int = 0) -> bytes:
    """A valid RGBA PNG whose pixels depend on `seed` (so twins are byte-identical only when seeds match)."""
    def chunk(tag: bytes, data: bytes) -> bytes:
        return struct.pack(">I", len(data)) + tag + data + struct.pack(">I", zlib.crc32(tag + data) & 0xFFFFFFFF)
    rows = []
    for y in range(height):
        row = bytearray(b"\x00")  # filter type 0 (none)
        for x in range(width):
            row += bytes([(x * 7 + y * 3 + seed) & 0xFF, (x + seed) & 0xFF, (y * 5 + seed) & 0xFF, 255])
        rows.append(bytes(row))
    raw = b"".join(rows)
    return (b"\x89PNG\r\n\x1a\n" + chunk(b"IHDR", struct.pack(">IIBBBBB", width, height, 8, 6, 0, 0, 0))
            + chunk(b"IDAT", zlib.compress(raw)) + chunk(b"IEND", b""))


FIXTURE_GEO = {
    "format_version": "1.12.0",
    "minecraft:geometry": [{
        "description": {"identifier": "geometry.fixture", "texture_width": 64, "texture_height": 32,
                        "orespawn:bone_draw_order": ["root", "tail", "arm", "hand"]},
        "bones": [
            {"name": "root", "pivot": [0, 24, 0], "cubes": [
                {"origin": [-4, 12, -2], "size": [8, 12, 4], "modelpart_mirror": True,
                 "uv": {"north": {"uv": [4, 4], "uv_size": [8, 12]}, "east": {"uv": [0, 4], "uv_size": [4, 12]},
                        "south": {"uv": [16, 4], "uv_size": [8, 12]}, "west": {"uv": [12, 4], "uv_size": [4, 12]},
                        "up": {"uv": [4, 0], "uv_size": [8, 4]}, "down": {"uv": [12, 4], "uv_size": [8, -4]}}}]},
            {"name": "tail", "parent": "root", "pivot": [0, 14, 2], "rotation": [0, 0, 0], "cubes": [
                {"origin": [-1, 13, 2], "size": [2, 2, 6], "inflate": 0.25,
                 "uv": {"north": {"uv": [0, 20], "uv_size": [2, 2]}, "up": {"uv": [2, 18], "uv_size": [2, 6]}}}]},
            {"name": "arm", "parent": "root", "pivot": [4, 22, 0], "rotation": [10, 0, 5], "cubes": [
                {"origin": [4, 12, -1], "size": [2, 10, 2],
                 "uv": {"north": {"uv": [40, 4], "uv_size": [2, 10]}, "east": {"uv": [38, 4], "uv_size": [2, 10]},
                        "south": {"uv": [44, 4], "uv_size": [2, 10]}, "west": {"uv": [42, 4], "uv_size": [2, 10]},
                        "up": {"uv": [40, 2], "uv_size": [2, 2]}, "down": {"uv": [42, 2], "uv_size": [2, 2]}}}]},
            {"name": "hand", "parent": "arm", "pivot": [5, 12, 0], "mirror": True, "cubes": [
                {"origin": [4, 9, -1.5], "size": [2, 3, 3], "uv": [48, 0], "mirror": False,
                 "pivot": [5, 12, 0], "rotation": [0, 0, -15]}]},
        ],
    }],
}

FIXTURE_ANIM = {
    "format_version": "1.8.0",
    "animations": {
        "idle": {"loop": True, "animation_length": 1.0, "bones": {
            "tail": {"rotation": {"0.0": {"vector": [0, 0, 0]}, "0.5": {"vector": [12.5, 0, 0], "lerp_mode": "catmullrom"}, "1.0": {"vector": [0, 0, 0]}}}}},
        "attack": {"loop": False, "animation_length": 0.5, "bones": {
            "tail": {"rotation": {"0.0": {"pre": [0, 0, 0], "post": [30, 0, 0]},
                                  "0.25": {"post": [15, 0, 0], "easing": "easeInOutSine", "easingArgs": [2]},
                                  "0.5": [0, 0, 0]}, "position": {"0.25": [0, 1, 0]}}}},
        "death": {"loop": "hold_on_last_frame", "animation_length": 2.0, "bones": {"tail": {"rotation": [0, 0, 90]}}},
    },
}

# a rig whose bone list is NOT depth-first: `hand` (child of arm) is listed before `arm`
NON_DFS_GEO = {
    "format_version": "1.12.0",
    "minecraft:geometry": [{
        "description": {"identifier": "geometry.nondfs", "texture_width": 16, "texture_height": 16},
        "bones": [
            {"name": "root", "pivot": [0, 0, 0], "cubes": [{"origin": [0, 0, 0], "size": [1, 1, 1], "uv": [0, 0]}]},
            {"name": "hand", "parent": "arm", "pivot": [0, 0, 0], "cubes": [{"origin": [0, 0, 0], "size": [1, 1, 1], "uv": [0, 0]}]},
            {"name": "arm", "parent": "root", "pivot": [0, 0, 0], "cubes": [{"origin": [0, 0, 0], "size": [1, 1, 1], "uv": [0, 0]}]},
            {"name": "tail", "parent": "root", "pivot": [0, 0, 0], "cubes": [{"origin": [0, 0, 0], "size": [1, 1, 1], "uv": [0, 0]}]},
        ],
    }],
}

NATIVE_GEO = {
    "format_version": "1.12.0",
    "minecraft:geometry": [{
        "description": {"identifier": "geometry.native", "texture_width": 64, "texture_height": 64},
        "bones": [
            {"name": "root", "pivot": [0, 0, 0], "cubes": [{"origin": [-4, 0, -4], "size": [8, 8, 8], "uv": [0, 0]}]},
            {"name": "body", "parent": "root", "pivot": [0, 8, 0], "cubes": [{"origin": [-3, 8, -3], "size": [6, 6, 6], "uv": [0, 16]}]},
            {"name": "head", "parent": "body", "pivot": [0, 14, 0], "cubes": [{"origin": [-2, 14, -2], "size": [4, 4, 4], "uv": [0, 28]}]},
            {"name": "tail", "parent": "root", "pivot": [0, 4, 4], "cubes": [{"origin": [-1, 3, 4], "size": [2, 2, 6], "uv": [24, 0]}]},
        ],
    }],
}

NATIVE_ANIM = {
    "format_version": "1.8.0",
    "animations": {
        "idle": {"loop": True, "animation_length": 2.0, "bones": {"tail": {"rotation": {"0.0": [0, 0, 0], "1.0": [10, 0, 0], "2.0": [0, 0, 0]}}}},
        "stance": {"loop": True, "animation_length": 2.0, "bones": {"tail": {"rotation": {"0.0": [0, 0, 0], "2.0": [0, 0, 0]}},
                                                                   "head": {"rotation": {"0.0": [0, 0, 0], "1.0": [-20, 0, 0], "2.0": [0, 0, 0]}}}},
        "stomp": {"loop": False, "animation_length": 0.5, "bones": {"head": {"rotation": {"0.0": [0, 0, 0], "0.25": [30, 0, 0], "0.5": [0, 0, 0]}}}},
        "death": {"loop": "hold_on_last_frame", "animation_length": 1.0, "bones": {"tail": {"rotation": {"0.0": [0, 0, 0], "1.0": [0, 0, 90]}}}},
    },
}

FIXTURE_PROFILE = {
    "sync-with-model": True, "trust-client": True, "synched-bones": ["hand"],
    "main-hitbox": {"collidable": False, "canReceiveDamage": False, "size": [1, 2]},
    "parts": [{"name": "hand", "collidable": True, "can-receive-damage": True, "damage-modifier": 1.0,
               "box": {"type": "multihitboxlib:aabb", "size": [0.5, 0.5], "position": [0, 0, 0], "pivot": [0, 0, 0]}}],
}

NATIVE_PROFILE = {
    "sync-with-model": True, "trust-client": True, "synched-bones": ["head"],
    "main-hitbox": {"collidable": False, "canReceiveDamage": False, "size": [2, 3]},
    "parts": [{"name": "head", "collidable": True, "can-receive-damage": True, "damage-modifier": 1.0,
               "box": {"type": "multihitboxlib:aabb", "size": [1, 1], "position": [0, 0, 0], "pivot": [0, 0, 0]}}],
}

FIXTURE_SEED = {
    "registry": "fixture", "display_name": "Fixture", "status": "test seed", "locomotion": "walker", "artist_scope": "full contract",
    "character_sheet": "A test creature.", "labels": {"root": "the body", "arm": "the arm", "tail": "the tail"},
    "groups": [{"name": "gait", "bones": ["tail"], "omega": 3.7, "axis": "x", "gait_scaled": True, "amplitude": "1 rad",
                "plain": "the tail wags", "math": "tail.xRot = cos(t*3.7)", "source": "Fixture.java"},
               {"name": "pump", "bones": ["arm"], "omega": 0.10471975511965978, "axis": "x", "gait_scaled": False,
                "amplitude": "|cos| x 0.785 + 0.75 rad", "plain": "the arm pumps", "math": "arm = |cos(rad(t % 360) * 6)| * 0.7854 + 0.75",
                "source": "Fixture.java"}],
    "behaviour": ["it wags"], "clips": [{"name": "walk", "verdict": "improve"}], "extras": [], "wishlist": [],
}

NATIVE_SEED = {
    "registry": "native", "display_name": "Native Boss", "status": "test seed", "locomotion": "walker",
    "artist_scope": "pilot boss (ruled 2026-09-06, Q16 (a)): idle and one attack", "character_sheet": "A native boss.",
    "labels": {"root": "root", "body": "body", "head": "head", "tail": "tail"}, "groups": [], "behaviour": [],
    "clips": [{"name": "idle", "verdict": "improve", "note": "the hover", "contract": "idle"},
              {"name": "stance", "verdict": "improve", "note": "the awake stance", "contract": "aggro_idle"},
              {"name": "stomp", "verdict": "improve", "note": "impact at 5 ticks", "contract": "extra"},
              {"name": "death", "verdict": "leave", "contract": "death"}],
    "extras": [],
    "wishlist": ["a heavier `stance` loop", "a wing-beat `fly` loop distinct from the hover"],
    "future": ["a `hurt` flinch on the struck head"],
}

ENTITY_JAVA = """package danger.orespawn.entity;

public class Fixture extends Monster {
    private static final EntityDataAccessor<Integer> DATA_ATTACKING =
            SynchedEntityData.defineId(Fixture.class, EntityDataSerializers.INT);

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.0, false));
        this.goalSelector.addGoal(3, new MysteryGoal(this));
        this.revengeGoal = new RevengeGoal();
        this.targetSelector.addGoal(1, this.revengeGoal);
        // MOD-033 shape: registered only under the modern config key
        if (OreSpawnConfig.petsDefendOwner()) {
            this.targetSelector.addGoal(2, new OwnerHurtByTargetGoal(this));
        }
        int prio = 4;
        RandomStrollGoal stroll = new RandomStrollGoal(this, 1.0);
        this.goalSelector.addGoal(prio, stroll);
    }

    public int getAttacking() { return this.entityData.get(DATA_ATTACKING); }
    public void setAttacking(int value) { this.entityData.set(DATA_ATTACKING, value); }

    @Override
    public void aiStep() {
        super.aiStep();
        LivingEntity target = this.getTarget(); // a comment with braces { } and "a string with if ("
        if (target != null) {
            if (this.tickCount > 200) this.bonus += 1; // a completed one-liner right above the write
            this.setAttacking(1);
            if (this.distanceToSqr(target) < 4.0) {
                this.doHurtTarget(target);
            } else {
                this.level().addFreshEntity(new LaserBall(this.level(), this));
            }
        } else {
            this.setAttacking(0);
        }
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        return super.hurt(source, amount);
    }
}
"""

OTHER_JAVA = """package danger.orespawn.entity;

public class Other extends Animal {
    @Override
    protected void registerGoals() {
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes().add(Attributes.MOVEMENT_SPEED, 0.0);
    }
}
"""

DART_JAVA = """package danger.orespawn.entity;

public class Dart extends ThrowableProjectile {
}
"""

MOO_JAVA = """package danger.orespawn.entity;

public class Moo extends Cow {
}
"""

FIXTURE_HEAD_JAVA = """package danger.orespawn.entity;

public class FixtureHead extends Mob {
}
"""

NATIVE_JAVA = """package danger.orespawn.entity;

public class Native extends Monster implements GeoEntity {
    private static final RawAnimation ANIM_IDLE = RawAnimation.begin().thenLoop("idle");
    private static final RawAnimation ANIM_STANCE = RawAnimation.begin().thenLoop("stance");
    private int pendingTicks;

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
    }

    public boolean isAwake() { return true; }

    /** Fires a named action on the Actions controller. */
    public void triggerNativeAction(String actionName) {
        this.triggerAnim("Actions", actionName);
    }

    @Override
    protected void customServerAiStep() {
        LivingEntity target = this.getTarget();
        if (target != null) {
            if (this.distanceToSqr(target) < 900.0) {
                if (this.getRandom().nextInt(2) == 1) {
                    doAreaDamage(this.getX(), this.getY(), this.getZ(), 15.0, 4.0, 0);
                }
                if (this.pendingTicks == 0) {
                    String key;
                    switch (this.getRandom().nextInt(2)) {
                        case 0 -> { key = "stomp"; }
                        default -> { key = "stomp"; }
                    }
                    this.triggerNativeAction(key);
                    this.pendingTicks = 5;
                }
            }
        }
    }

    private void doAreaDamage(double x, double y, double z, double dist, double damage, int knock) {
        AABB bb = new AABB(x - dist, y - 10.0, z - dist, x + dist, y + 10.0, z + dist);
        for (LivingEntity t : this.level().getEntitiesOfClass(LivingEntity.class, bb)) {
            t.hurt(this.damageSources().explosion(null, null), (float) damage / 2.0f);
            t.hurt(this.damageSources().generic(), (float) damage / 2.0f);
        }
    }

    @Override
    public void die(DamageSource source) {
        this.triggerNativeAction("death");
        super.die(source);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "Movement", 5, state -> {
            if (this.isDeadOrDying()) {
                return PlayState.STOP;
            }
            if (this.isAwake()) {
                return state.setAndContinue(ANIM_STANCE);
            }
            return state.setAndContinue(ANIM_IDLE);
        }));

        controllers.add(new AnimationController<>(this, "Actions", 5, state -> PlayState.STOP)
                .triggerableAnim("stomp", RawAnimation.begin().thenPlay("stomp"))
                .triggerableAnim("death", RawAnimation.begin().thenPlay("death")));
    }
}
"""

PULSER_JAVA = """package danger.orespawn.entity;

public class Pulser extends Monster {
    private static final EntityDataAccessor<Integer> DATA_ATTACKING =
            SynchedEntityData.defineId(Pulser.class, EntityDataSerializers.INT);
    private int reloadTicker;

    public void setAttacking(int value) { this.entityData.set(DATA_ATTACKING, value); }

    @Override
    protected void customServerAiStep() {
        if (this.reloadTicker > 0) {
            --this.reloadTicker;
        }
        LivingEntity target = this.getTarget();
        if (target != null) {
            if (this.reloadTicker == 0) {
                this.setAttacking(1);
                this.reloadTicker = 20;
            }
        }
        if (this.reloadTicker <= 0) {
            this.setAttacking(0);
        }
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        this.setAttacking(1);
        return super.hurt(source, amount);
    }
}
"""

MIXED_JAVA = """package danger.orespawn.entity;

public class Mixed extends Monster {
    private static final EntityDataAccessor<Integer> DATA_ATTACKING =
            SynchedEntityData.defineId(Mixed.class, EntityDataSerializers.INT);
    private int reloadTicker;

    public void setAttacking(int value) { this.entityData.set(DATA_ATTACKING, value); }

    @Override
    protected void customServerAiStep() {
        if (this.reloadTicker > 0) {
            --this.reloadTicker;
            if (this.reloadTicker < 25) this.setAttacking(0);
        }
        if (this.reloadTicker == 0) {
            LivingEntity target = this.getTarget();
            this.reloadTicker = 35;
            if (target != null) {
                if (this.distanceToSqr(target) < 256.0) {
                    this.setAttacking(1);
                    if (this.getSensing().hasLineOfSight(target)) {
                        fireLaserAt(target);
                    }
                }
            } else {
                this.setAttacking(0);
            }
        }
    }
}
"""

MOD_ENTITIES = """package danger.orespawn;

public final class ModEntities {
    public static final DeferredHolder<EntityType<?>, EntityType<Fixture>> FIXTURE =
            // a comment between the assignment and the registration, as ModEntities has
            ENTITY_TYPES.register("fixture", () -> EntityType.Builder.of(Fixture::new, MobCategory.MONSTER)
                    .sized(1.0f, 2.0f).clientTrackingRange(8).build("fixture"));
    public static final DeferredHolder<EntityType<?>, EntityType<Other>> OTHER =
            ENTITY_TYPES.register("other", () -> EntityType.Builder.of(Other::new, MobCategory.CREATURE)
                    .sized(0.5f, 0.5f).clientTrackingRange(8).build("other"));
    public static final DeferredHolder<EntityType<?>, EntityType<Dart>> DART =
            ENTITY_TYPES.register("dart", () -> EntityType.Builder.<Dart>of(Dart::new, MobCategory.MISC)
                    // the explicitly typed form the projectiles use
                    .sized(0.25f, 0.25f).clientTrackingRange(4).updateInterval(10).noSummon().build("dart"));
    public static final DeferredHolder<EntityType<?>, EntityType<Moo>> MOO =
            ENTITY_TYPES.register("moo", () -> EntityType.Builder.of(Moo::new, MobCategory.CREATURE)
                    .sized(0.9f, 1.3f).clientTrackingRange(10).build("moo"));
    public static final DeferredHolder<EntityType<?>, EntityType<FixtureHead>> FIXTURE_HEAD =
            ENTITY_TYPES.register("fixture_head", () -> EntityType.Builder.of(FixtureHead::new, MobCategory.MISC)
                    .sized(9.9f, 10.0f).clientTrackingRange(10).build("fixture_head"));
    public static final DeferredHolder<EntityType<?>, EntityType<Native>> NATIVE =
            ENTITY_TYPES.register("native", () -> EntityType.Builder.of(Native::new, MobCategory.MONSTER)
                    .sized(2.0f, 3.0f).clientTrackingRange(10).build("native"));
    public static final DeferredHolder<EntityType<?>, EntityType<Pulser>> PULSER =
            ENTITY_TYPES.register("pulser", () -> EntityType.Builder.of(Pulser::new, MobCategory.MONSTER)
                    .sized(1.0f, 1.0f).clientTrackingRange(8).build("pulser"));
    public static final DeferredHolder<EntityType<?>, EntityType<Mixed>> MIXED =
            ENTITY_TYPES.register("mixed", () -> EntityType.Builder.of(Mixed::new, MobCategory.MONSTER)
                    .sized(1.0f, 1.0f).clientTrackingRange(8).build("mixed"));

    public static void register(IEventBus eventBus) {
        ENTITY_TYPES.register(eventBus);
    }
}
"""

CLIENT_JAVA = """package danger.orespawn;
public final class OreSpawnClient {
    static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.FIXTURE.get(), FixtureRenderer::new);
        event.registerEntityRenderer(ModEntities.OTHER.get(), OtherRenderer::new);
        event.registerEntityRenderer(ModEntities.MOO.get(), MooRenderer::new);
        event.registerEntityRenderer(ModEntities.FIXTURE_HEAD.get(), FixtureHeadRenderer::new);
        event.registerEntityRenderer(ModEntities.NATIVE.get(), NativeRenderer::new);
    }
}
"""

ITEM_RENDERER_JAVA = """package danger.orespawn.client;
public class OreSpawnItemRenderer {
    ResourceLocation SWORD = ResourceLocation.fromNamespaceAndPath(MOD_ID, "textures/entity/sword.png");
}
"""

FIXTURE_HEAD_RENDERER_JAVA = """package danger.orespawn.entity.client;
public class FixtureHeadRenderer extends MobRenderer<FixtureHead, FixtureHeadModel> {
    ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(MOD_ID, "textures/entity/fixturehead.png");
    @Override
    public boolean shouldRender(FixtureHead entity, Frustum frustum, double x, double y, double z) {
        return false;
    }
}
"""

DESIGN_MD = """# design
| # | Model (LOC) | Served entity type(s) [W x H] | Texture(s) | setupAnim class (basis) | Renderer scale | Audit | Proposed tier |
|---:|---|---|---|---|---|---|---:|
| 1 | `FixtureModel` (10) | fixture [1.0x2.0] | fixture.png 64x32 | gait-scaled | — | — | 2 |
| 2 | `OtherModel` (5) | other [0.5x0.5] | other.png 32x32 | static | — | — | 3 |
| 3 | `FixtureHeadModel` (37) | fixture_head [9.9x10.0] | fixturehead.png 64x32 | static — empty setupAnim | — | — | 3 |
| 4 | `NativeModel` (66) | native [2.0x3.0] | native.png 64x64 | state-branching — existing GeckoLib controller state machine | — | — | 0 |

#### Vanilla-model reuse (proposed Tier 0)

These consumers have no OreSpawn model class to convert.

| Vanilla model | Renderer | Entity type [W x H] | Texture(s) | Tier |
|---|---|---|---|---:|
| `CowModel` | `MooRenderer` | moo [0.9x1.3] | moo.png 64x32 (twins: moocow.png) | 0 |

<!-- END GENERATED G0 INVENTORY -->

## 2. Tiering proposal
"""


def build_fixture_repo(root: Path) -> None:
    java = root / "src/main/java/danger/orespawn"
    (java / "entity/client").mkdir(parents=True)
    (java / "entity/ai").mkdir(parents=True)
    (java / "client").mkdir(parents=True)
    (java / "ModEntities.java").write_text(MOD_ENTITIES, encoding="utf-8")
    (java / "OreSpawnClient.java").write_text(CLIENT_JAVA, encoding="utf-8")
    (java / "client/OreSpawnItemRenderer.java").write_text(ITEM_RENDERER_JAVA, encoding="utf-8")
    for name, text in (("Fixture", ENTITY_JAVA), ("Other", OTHER_JAVA), ("Dart", DART_JAVA), ("Moo", MOO_JAVA),
                       ("FixtureHead", FIXTURE_HEAD_JAVA), ("Native", NATIVE_JAVA), ("Pulser", PULSER_JAVA), ("Mixed", MIXED_JAVA)):
        (java / f"entity/{name}.java").write_text(text, encoding="utf-8")
    (java / "entity/client/FixtureRenderer.java").write_text(
        'class FixtureRenderer { ResourceLocation T = ResourceLocation.fromNamespaceAndPath(MOD_ID, "textures/entity/fixture.png"); FixtureGeoReplacement r; }', encoding="utf-8")
    (java / "entity/client/FixtureGeoReplacement.java").write_text(
        'class FixtureGeoReplacement { String g = "geo/entity/fixture.geo.json"; String a = "animations/entity/fixture.animation.json"; String t = "textures/entity/fixture.png"; }', encoding="utf-8")
    (java / "entity/client/OtherRenderer.java").write_text(
        'class OtherRenderer { String t = "textures/entity/other.png"; String s = "textures/entity/girlfriend" + skin + ".png"; }', encoding="utf-8")
    (java / "entity/client/MooRenderer.java").write_text(
        'class MooRenderer extends MobRenderer<Moo, CowModel<Moo>> { String t = "textures/entity/moocow.png"; }', encoding="utf-8")
    (java / "entity/client/FixtureHeadRenderer.java").write_text(FIXTURE_HEAD_RENDERER_JAVA, encoding="utf-8")
    (java / "entity/client/NativeRenderer.java").write_text(
        'class NativeRenderer { String g = "geo/entity/native.geo.json"; String a = "animations/entity/native.animation.json"; String t = "textures/entity/native.png"; }', encoding="utf-8")
    assets = root / "src/main/resources/assets/orespawn"
    (assets / "geo/entity").mkdir(parents=True)
    (assets / "animations/entity").mkdir(parents=True)
    (assets / "textures/entity").mkdir(parents=True)
    (assets / "textures/models/armor").mkdir(parents=True)
    (assets / "textures/item").mkdir(parents=True)
    (assets / "geo/entity/fixture.geo.json").write_text(json.dumps(FIXTURE_GEO, indent=2), encoding="utf-8")
    (assets / "animations/entity/fixture.animation.json").write_text(json.dumps(FIXTURE_ANIM, indent=2), encoding="utf-8")
    (assets / "geo/entity/native.geo.json").write_text(json.dumps(NATIVE_GEO, indent=2), encoding="utf-8")
    (assets / "animations/entity/native.animation.json").write_text(json.dumps(NATIVE_ANIM, indent=2), encoding="utf-8")
    tex = assets / "textures/entity"
    (tex / "fixture.png").write_bytes(png_bytes(64, 32, 1))
    (tex / "fixturetexture.png").write_bytes(png_bytes(64, 32, 1))  # a byte-identical twin
    (tex / "other.png").write_bytes(png_bytes(32, 32, 2))
    (tex / "native.png").write_bytes(png_bytes(64, 64, 3))
    (tex / "fixturehead.png").write_bytes(png_bytes(64, 32, 4))
    (tex / "moo.png").write_bytes(png_bytes(64, 32, 6))      # registry-aligned but referenced by nothing
    (tex / "moocow.png").write_bytes(png_bytes(64, 32, 6))   # its twin: not registry-aligned, referenced by MooRenderer
    (tex / "sword.png").write_bytes(png_bytes(16, 16, 7))    # referenced only by the item renderer
    (tex / "dormant.png").write_bytes(png_bytes(16, 16, 8))  # referenced by nothing; a twin lives under textures/item
    (assets / "textures/item/dormant.png").write_bytes(png_bytes(16, 16, 8))
    for n in (0, 1, 3):  # girlfriend0..3 with a gap: the law series is violated
        (tex / f"girlfriend{n}.png").write_bytes(png_bytes(8, 8, 10 + n))
    (tex / "amethyst_1.png").write_bytes(png_bytes(16, 16, 20))
    (assets / "textures/models/armor/amethyst_layer_1.png").write_bytes(png_bytes(16, 16, 20))
    prof = root / "src/main/resources/data/orespawn/multihitboxlib/hitbox_profiles"
    prof.mkdir(parents=True)
    (prof / "fixture.json").write_text(json.dumps(FIXTURE_PROFILE), encoding="utf-8")
    (prof / "native.json").write_text(json.dumps(NATIVE_PROFILE), encoding="utf-8")
    (root / "tools/artist_specs").mkdir(parents=True)
    (root / "tools/artist_specs/fixture.json").write_text(json.dumps(FIXTURE_SEED), encoding="utf-8")
    (root / "tools/artist_specs/native.json").write_text(json.dumps(NATIVE_SEED), encoding="utf-8")
    (root / "tools/reference_renderer_pins.json").write_text(json.dumps({"entries": [
        {"entity": "Fixture", "expected_scale": 1, "expected_shadow": 0.5, "status": "pin"},
        {"entity": "Native", "expected_scale": 2, "expected_shadow": 1.0, "status": "pin"}]}), encoding="utf-8")
    (root / "phase_g_reports").mkdir()
    (root / "phase_g_reports/geckolib_migration_design.md").write_text(DESIGN_MD, encoding="utf-8")
    (root / "provenance_byte_identical_assets.txt").write_text("  textures\\entity\\fixture.png  <=  Fixture.png\n", encoding="utf-8")
    # the reference-only clip (owner 2026-09-13, second set, item 27 (3)): the sampler's synthetic output for the fixture and
    # its index row; the native boss has no row by design (no classic hook)
    clips_dir = root / "tools/reference_clips"
    clips_dir.mkdir(parents=True)
    clip_path = clips_dir / "fixture_reference.animation.json"
    clip_path.write_bytes((json.dumps(REFERENCE_CLIP, indent=2) + "\n").encode("utf-8"))
    (clips_dir / "reference_clips.json").write_text(json.dumps({
        "schema_version": 1, "clip_name": "reference",
        "clips": [{"registry": "fixture", "model_id": "model_fixture", "manifest": "t2_model_proofs.json", "model_class": "FixtureModel",
                   "hook": "FixtureGeoReplacement.applyCustomAnimations(AnimationProcessor, PoseInputs)",
                   "geo": "src/main/resources/assets/orespawn/geo/entity/fixture.geo.json",
                   "file": clip_path.name, "sha256": ap.sha256_file(clip_path), "rule": "period_multiple",
                   "rule_note": "one channel at 3.7 rad/tick: one natural period 2 pi / 3.7 = 1.698 ticks; closes at k = 1 (1.698132 ticks): every bone returns within 0 degrees of its start at k x T",
                   "period_ticks": 1.6981317008, "period_multiple_k": 1, "closure_delta_degrees": 0.0,
                   "span_ticks": 1.6981317008, "animation_length_seconds": 0.0849065850, "keys_per_bone": 3, "bones": 4,
                   "moving_bones": ["tail"], "position_bones": ["arm"], "hidden_bones_at_rest": [],
                   "seam_delta_degrees": 0.0, "seam_delta_position_units": 0.0,
                   "subject_after": {"ri1": 0, "shielding": -1, "rf1": 0.0},
                   "sampled_inputs": "limbSwingAmount 1.0; limbSwing = t; ageInTicks = t; netHeadYaw 0; headPitch 0; rest state; RNG seed 0"}],
    }, indent=2) + "\n", encoding="utf-8")


REFERENCE_CLIP = {
    "format_version": "1.8.0",
    "animations": {
        "reference": {"loop": True, "animation_length": 0.084906585, "bones": {
            "arm": {"rotation": {"0.0": {"post": [0.0, 0.0, 0.0], "lerp_mode": "linear"}, "0.05": {"post": [0.0, 0.0, 0.0], "lerp_mode": "linear"},
                                 "0.084906585": {"post": [0.0, 0.0, 0.0], "lerp_mode": "linear"}},
                    "position": {"0.0": {"post": [0.0, -1.5, 2.0], "lerp_mode": "linear"}, "0.05": {"post": [0.0, -1.5, 2.0], "lerp_mode": "linear"},
                                 "0.084906585": {"post": [0.0, -1.5, 2.0], "lerp_mode": "linear"}}},
            "tail": {"rotation": {"0.0": {"post": [57.2957795131, 0.0, 0.0], "lerp_mode": "linear"},
                                  "0.05": {"post": [-48.9012345678, 0.0, 0.0], "lerp_mode": "linear"},
                                  "0.084906585": {"post": [57.2957795131, 0.0, 0.0], "lerp_mode": "linear"}}},
        }},
    },
}


class FixtureCase(unittest.TestCase):
    @classmethod
    def setUpClass(cls) -> None:
        cls.tmp = Path(tempfile.mkdtemp(prefix="artist_pkg_"))
        cls.root = cls.tmp / "repo"
        cls.root.mkdir()
        build_fixture_repo(cls.root)
        cls.repo = ap.Repo(cls.root)
        cls.catalog = ap.TextureCatalog(cls.repo)

    @classmethod
    def tearDownClass(cls) -> None:
        shutil.rmtree(cls.tmp, ignore_errors=True)

    def warnings_with(self, code: str) -> list[tuple[str, str, str]]:
        return [w for w in self.repo.warnings.items if w[1] == code]

    # --- repository facts ---------------------------------------------------------------------

    def test_mod_entities_parse_both_registration_forms(self):
        # `Builder.of(` and the explicitly typed `Builder.<X>of(` both count; `ENTITY_TYPES.register(eventBus)` does not
        self.assertEqual(list(self.repo.entities), ["fixture", "other", "dart", "moo", "fixture_head", "native", "pulser", "mixed"])
        self.assertEqual(self.repo.entities["fixture"]["width"], 1.0)
        self.assertEqual(self.repo.entities["fixture"]["java_class"], "Fixture")
        self.assertEqual(self.repo.entities["dart"]["java_class"], "Dart")
        self.assertEqual(self.repo.entities["dart"]["category"], "MISC")
        self.assertEqual(self.repo.entities["dart"]["width"], 0.25)

    def test_species_discovery_and_status(self):
        fx, other = self.repo.get("fixture"), self.repo.get("other")
        self.assertTrue(fx.landed)
        self.assertEqual(fx.status, "landed candidate")
        self.assertEqual(fx.tier, 2)
        self.assertEqual(fx.tier_label, "Tier 2")
        self.assertEqual(fx.textures, ["fixture.png"])
        self.assertEqual(fx.profile_name, "fixture")
        self.assertEqual(fx.pin["expected_scale"], 1)
        self.assertFalse(other.landed)
        self.assertEqual(other.status, "classic only")
        self.assertEqual(other.textures, ["girlfriend0.png", "girlfriend1.png", "girlfriend3.png", "other.png"])
        # the projectile registered in the generic form: excluded with the vocabulary that is true for it
        dart = self.repo.get("dart")
        self.assertEqual(dart.status, "excluded")
        self.assertIn("projectile (extends ThrowableProjectile)", dart.status_note)
        self.assertEqual(dart.tier_label, "no tier (not in the G0 inventory)")

    def test_vanilla_reuse_table_and_head_sidecar(self):
        # the design's "Vanilla-model reuse" table gives the cow line a tier and a true note, not a blank tier and a false exclusion
        self.assertEqual(self.repo.vanilla_reuse, {"moo": {"model": "CowModel", "renderer": "MooRenderer", "tier": 0}})
        moo = self.repo.get("moo")
        self.assertEqual((moo.status, moo.tier, moo.model), ("excluded", 0, "CowModel (vanilla)"))
        self.assertIn("vanilla CowModel drawn by MooRenderer", moo.status_note)
        self.assertNotIn("head sidecar", moo.status_note)
        # a head sidecar is classic only / Tier 3 per its design row, and the note says it renders nothing
        head = self.repo.get("fixture_head")
        self.assertTrue(head.renders_nothing)
        self.assertEqual((head.status, head.tier), ("classic only", 3))
        self.assertIn("renders nothing", head.status_note)
        self.assertIn("shouldRender false", head.status_note)
        # the native boss: the design's Tier 0 'done' row AND the contract's Tier-1 (MHLib) boss
        nat = self.repo.get("native")
        self.assertTrue(nat.landed and nat.is_mhlib_boss)
        self.assertEqual(nat.tier, 0)
        self.assertEqual(nat.tier_label, "Tier 1 (boss; the design's 'done' row)")
        self.assertIn("native GeckoLib rig", nat.status_note)

    def test_png_size_from_ihdr(self):
        self.assertEqual(ap.png_size(self.root / "src/main/resources/assets/orespawn/textures/entity/other.png"), (32, 32))

    # --- textures -------------------------------------------------------------------------------

    def test_texture_dedupe_on_synthetic_twins(self):
        cat = self.catalog
        self.assertEqual(cat.hash["fixture.png"], cat.hash["fixturetexture.png"])
        self.assertNotEqual(cat.hash["fixture.png"], cat.hash["other.png"])
        digest = cat.hash["fixture.png"]
        self.assertEqual(cat.canonical[digest], "fixture.png")  # referenced + registry-aligned wins over the twin
        header, rows = cat.rows()
        by_file = {r[0]: dict(zip(header, r)) for r in rows}
        self.assertEqual(by_file["fixture.png"]["is_canonical"], "yes")
        self.assertEqual(by_file["fixture.png"]["fan_out_targets"], "fixturetexture.png")
        self.assertEqual(by_file["fixturetexture.png"]["is_canonical"], "no")
        self.assertEqual(by_file["fixturetexture.png"]["canonical_name"], "fixture.png")
        self.assertEqual(by_file["fixture.png"]["entity_folders"], "fixture")
        self.assertEqual(by_file["fixture.png"]["consumers"], "FixtureGeoReplacement;FixtureRenderer")
        self.assertEqual(by_file["fixture.png"]["provenance_1_7_10"], "Fixture.png")
        summary = cat.summary()
        # 13 shipped: fixture x2 (twins), other, native, fixturehead, moo x2 (twins), sword, dormant, girlfriend x3, amethyst_1
        self.assertEqual((summary["shipped"], summary["unique_payloads"], summary["duplicate_groups"], summary["redundant_names"]), (13, 11, 2, 2))
        self.assertEqual(summary["strays"], 3)  # amethyst_1 (armor sheet), sword (item renderer), dormant (dormant twin)

    def test_canonical_prefers_referenced_over_registry_aligned(self):
        # moo.png is registry-aligned but nothing reads it; moocow.png is what MooRenderer names: the referenced twin is canonical
        digest = self.catalog.hash["moo.png"]
        self.assertEqual(digest, self.catalog.hash["moocow.png"])
        self.assertEqual(self.catalog.canonical[digest], "moocow.png")
        self.assertEqual(self.catalog.consumers.get("moocow.png"), ["MooRenderer"])
        self.assertIsNone(self.catalog.consumers.get("moo.png"))

    def test_variant_series_law(self):
        s = self.catalog.series["girlfriend"]
        self.assertEqual((s["lo"], s["hi"], s["count"], s["contiguous"], s["law_ok"]), (0, 3, 3, False, False))
        self.assertIn("VIOLATED", self.catalog.series_of("girlfriend3.png"))
        self.assertTrue(any(w[1] == "SERIES_LAW" for w in self.repo.warnings.items))

    def test_stray_classes(self):
        self.assertIn("armor sheet stray", self.catalog.stray["amethyst_1.png"])
        self.assertIn("byte-identical", self.catalog.stray["amethyst_1.png"])
        self.assertEqual(self.catalog.stray["fixture.png"], "")
        self.assertEqual(self.catalog.entity_folders_for("amethyst_1.png"), [])
        header, rows = self.catalog.rows()
        self.assertIn("amethyst_1.png", [r[0] for r in rows])  # kept in the global map
        # an item-renderer texture (a held weapon model) and a dormant twin of an item texture are strays of their own classes
        self.assertEqual(self.catalog.stray["sword.png"], "item-renderer texture (a held weapon/tool model), not a mob")
        self.assertEqual(self.catalog.consumers["sword.png"], ["OreSpawnItemRenderer"])
        self.assertEqual(self.catalog.stray["dormant.png"], "dormant twin of item/dormant.png")
        self.assertEqual(self.catalog.stray["moocow.png"], "")

    # --- trigger inventory ----------------------------------------------------------------------

    def test_trigger_inventory_on_fixture_goals(self):
        inv = ap.build_trigger_inventory(self.repo.get("fixture"), self.repo)
        goals = [(g["selector"], g["priority"], g["goal"], g["category"], g["guard"]) for g in inv["goals"]]
        self.assertEqual(goals, [("goalSelector", 0, "FloatGoal", "locomotion", ""), ("goalSelector", 2, "MeleeAttackGoal", "attack", ""),
                                 ("goalSelector", 3, "MysteryGoal", "UNCLASSIFIED", ""), ("targetSelector", 1, "RevengeGoal", "targeting", ""),
                                 ("targetSelector", 2, "OwnerHurtByTargetGoal", "targeting", "[modern: petsDefendOwner]"),
                                 ("goalSelector", None, "UNPARSED", "UNPARSED", "")])
        unparsed = inv["goals"][-1]
        self.assertIn("non-literal priority `prio`", unparsed["unparsed"])
        self.assertIn("`stroll`", unparsed["unparsed"])
        self.assertTrue(any(w[1] == "GOAL_UNCLASSIFIED" and "MysteryGoal" in w[2] for w in self.repo.warnings.items))
        self.assertTrue(any(w[1] == "GOAL_UNPARSED" for w in self.repo.warnings.items))
        self.assertEqual([f["name"] for f in inv["flags"]], ["DATA_ATTACKING"])
        self.assertEqual(inv["attacking"]["verdict"], "STATE")
        sites = inv["attacking"]["sites"]
        self.assertEqual([s["value"] for s in sites], [1, 0])
        self.assertEqual(sites[0]["method"], "aiStep")
        # the guard is the block that ENCLOSES the write — not the completed one-liner two lines above it
        self.assertEqual(sites[0]["guard"], "if (target != null)")
        self.assertEqual(sites[0]["guard_kind"], "if")
        self.assertEqual(sites[1]["guard"], "NOT(if (target != null))")
        self.assertEqual(sites[1]["guard_kind"], "else")
        self.assertEqual(sites[1]["within"], [])
        self.assertEqual(len(inv["combat"]["melee"]), 1)
        self.assertEqual(inv["combat"]["melee"][0]["kind"], "single")
        self.assertEqual([r["code"] for r in inv["combat"]["ranged"]], ["LaserBall"])
        self.assertIn("hurt", inv["overrides"])
        drives = {d["clip"]: d for d in inv["drives"]}
        self.assertIn("aggro_idle / calm_idle", drives)
        self.assertIn("STATE flag", drives["aggro_idle / calm_idle"]["verdict"])
        self.assertIn("transport 1", drives["attack"]["signal"])
        self.assertNotIn("AREA helper", drives["attack"]["signal"])

    def test_attacking_verdicts_event_and_mixed(self):
        pulser = ap.build_trigger_inventory(self.repo.get("pulser"), self.repo)
        self.assertEqual(pulser["attacking"]["verdict"], "EVENT")
        facts = pulser["attacking"]["facts"]
        self.assertTrue(facts["pulse"] and not facts["held"])
        self.assertEqual(len(facts["hurt_sets"]), 1)  # hurt() also raises the flag
        self.assertIn("RAISED in hurt()", pulser["attacking"]["reason"])
        drives = {d["clip"]: d for d in pulser["drives"]}
        self.assertIn("transport 2", drives["attack"]["verdict"])
        self.assertIn("hurt() also raises the flag", drives["attack"]["verdict"])
        clear = [s for s in pulser["attacking"]["sites"] if s["value"] == 0][0]
        self.assertEqual(clear["guard"], "if (this.reloadTicker <= 0)")
        mixed = ap.build_trigger_inventory(self.repo.get("mixed"), self.repo)
        self.assertEqual(mixed["attacking"]["verdict"], "MIXED")
        reason = mixed["attacking"]["reason"]
        # the pulse is per in-range THINK TICK (every 35 ticks, raised before the line-of-sight gate), not "per shot"
        self.assertIn("10-tick pulse per in-range think tick", reason)
        self.assertIn("every 35 ticks", reason)
        self.assertIn("before the line-of-sight gate", reason)
        self.assertIn("cleared on target loss", reason)
        self.assertEqual(mixed["attacking"]["facts"]["ticker"], {"ticker": "reloadTicker", "reset": 35, "threshold": 25, "pulse_ticks": 10})
        sites = {s["line"]: s for s in mixed["attacking"]["sites"]}
        guards = sorted(s["guard"] for s in mixed["attacking"]["sites"])
        self.assertIn("if (this.reloadTicker < 25)", guards)
        self.assertIn("NOT(if (target != null))", guards)
        held = [s for s in mixed["attacking"]["sites"] if s["guard"] == "NOT(if (target != null))"][0]
        self.assertEqual(held["within"], ["if (this.reloadTicker == 0)"])
        self.assertTrue(any(w[1] == "ATTACKING_UNCLASSIFIED" and w[0] == "mixed" for w in self.repo.warnings.items))
        self.assertTrue(all(s["guard_kind"] != "unparsed" for s in sites.values()))

    def test_native_trigger_sites_and_area_helper(self):
        inv = ap.build_trigger_inventory(self.repo.get("native"), self.repo)
        nat = inv["native"]
        self.assertEqual([c["name"] for c in nat["controllers"]], ["Movement", "Actions"])
        self.assertEqual({t["clip"]: t["controller"] for t in nat["triggerable"]}, {"stomp": "Actions", "death": "Actions"})
        self.assertEqual(nat["state_clips"]["stance"]["condition"], "if (this.isAwake())")
        self.assertEqual(nat["state_clips"]["idle"]["condition"], "default (no earlier branch took it)")
        self.assertEqual(nat["state_clips"]["idle"]["controller"], "Movement")
        triggers = nat["clip_triggers"]
        self.assertEqual(triggers["idle"]["kind"], "state")
        self.assertIn("`Movement` controller selects it when default", triggers["idle"]["signal"])
        self.assertEqual(triggers["stomp"]["kind"], "triggered")
        self.assertIn("triggerAnim(\"Actions\", \"stomp\")", triggers["stomp"]["signal"])
        self.assertIn("in `customServerAiStep` (picked among stomp)", triggers["stomp"]["signal"])
        self.assertIn("guard: if (this.pendingTicks == 0) within if (this.distanceToSqr(target) < 900.0)", triggers["stomp"]["signal"])
        self.assertIn("in `die`", triggers["death"]["signal"])
        # the area-damage helper is a melee site whose body hurts each victim twice
        melee = inv["combat"]["melee"]
        self.assertEqual(len(melee), 1)
        self.assertEqual(melee[0]["kind"], "area")
        self.assertEqual(melee[0]["hurts_per_victim"], 2)
        self.assertEqual(melee[0]["method"], "customServerAiStep")
        self.assertIn("per victim per roll", ap.melee_transport_note(inv))
        self.assertIn("2x each", ap.melee_transport_note(inv))
        # the NATIVE drive table: the shipped clips with their real triggers, the generic names marked not used
        drives = {d["clip"]: d for d in inv["drives"]}
        self.assertEqual([d["clip"] for d in inv["drives"]][:4], ["idle", "stance", "stomp", "death"])
        self.assertIn("in `die`", drives["death"]["signal"])
        self.assertIn("stands in for the contract's `aggro_idle`", drives["stance"]["verdict"])
        self.assertIn("not used by this species (native clip set)", drives["aggro_idle"]["verdict"])
        self.assertIn("carried by native `stance`", drives["aggro_idle"]["verdict"])
        self.assertIn("not used by this species (native clip set)", drives["hurt"]["verdict"])
        self.assertIn("not used by this species (native clip set)", drives["fly"]["verdict"])
        self.assertIn("does not accept idle_alt_N", drives["idle_alt_N"]["verdict"])

    # --- glossary, locks, groups, SPEC, manifest ---------------------------------------------------

    def test_glossary_rendering_and_locked_bones(self):
        fx = self.repo.get("fixture")
        geo = fx.geo
        locked = ap.locked_bones(fx, geo)
        self.assertEqual(set(locked), {"hand", "arm", "root"})  # the synced part and its ancestors; tail is free
        self.assertIn("carries hitbox part 'hand'", locked["hand"])
        rows = ap.build_glossary(fx, self.repo, geo, ap.frequency_groups(fx))
        self.assertEqual([r["name"] for r in rows], ["root", "tail", "arm", "hand"])  # geo order kept
        by = {r["name"]: r for r in rows}
        self.assertEqual(by["arm"]["label"], "the arm")
        self.assertEqual(by["arm"]["label_source"], "seed")
        self.assertEqual(by["hand"]["label_source"], "NONE")
        self.assertIn("no label yet", by["hand"]["label"])
        self.assertTrue(any(w[1] == "BONE_UNLABELLED" and "hand" in w[2] for w in self.repo.warnings.items))
        self.assertTrue(by["tail"]["gait_bone"])
        self.assertEqual(by["tail"]["group"], "gait")
        self.assertTrue(by["arm"]["locked"] and by["root"]["locked"] and not by["tail"]["locked"])
        # the default `root` label states the consequence of a key there in the ruled wording — never "never keyed"
        self.assertIn("allowed and warned like any locked bone", ap.DEFAULT_LABELS["root"])
        self.assertFalse(any("never key" in label.lower() for label in ap.DEFAULT_LABELS.values()))

    def test_frequency_groups_state_the_visible_period_of_a_rectified_shape(self):
        groups = {g["name"]: g for g in ap.frequency_groups(self.repo.get("fixture"))}
        self.assertFalse(groups["gait"]["rectified"])
        self.assertIsNone(groups["gait"]["visible_period_ticks"])
        pump = groups["pump"]
        self.assertTrue(pump["rectified"])
        self.assertAlmostEqual(pump["period_ticks"], 60.0, places=6)
        self.assertAlmostEqual(pump["visible_period_ticks"], 30.0, places=6)
        self.assertAlmostEqual(pump["visible_period_seconds"], 1.5, places=6)

    def test_spec_and_manifest(self):
        fx = self.repo.get("fixture")
        inv = ap.build_trigger_inventory(fx, self.repo)
        md, manifest = ap.spec_document(fx, self.repo, self.catalog, inv)
        self.assertIn("## 3. Bone glossary", md)
        self.assertIn("### 4.1 Tempo table", md)
        self.assertIn("1.698 ticks", md)  # 2 pi / 3.7
        self.assertIn("VISIBLE: 30 ticks (1.5 s)", md)  # the rectified pump: natural 60 ticks, visible 30
        # every marker resolved to the 2026-09-06 rulings (owner 2026-09-13, third set, item 28 (3)): the sheet cites them
        self.assertNotIn("PROVISIONAL", md)
        self.assertNotIn("open question", md.lower())
        self.assertIn("ruled 2026-09-06, Q14 (a)", md)
        self.assertIn("`hand`", md)
        self.assertIn(ap.LOCK_POLICY, md)  # §3 header, §5, §7 and §11 carry the one policy sentence (ruled 2026-09-06)
        self.assertNotIn("PROVISIONAL", ap.LOCK_POLICY)
        self.assertNotIn("open question 16", ap.LOCK_POLICY)
        section7 = md.split("## 7. Hitbox bones")[1].split("## 8. Textures")[0]
        self.assertIn(ap.LOCK_POLICY, section7)
        self.assertIn(ap.LOCK_REJECT_MODE, section7)  # the reject mode stays available for the day the evaluator lands
        self.assertNotIn("PROVISIONAL", section7)
        self.assertNotIn("open question 16", section7)
        self.assertIn("[modern: petsDefendOwner]", md)
        self.assertIn("`UNPARSED`", md)
        self.assertIn("guard (the enclosing block)", md)
        self.assertIn("It REJECTS: " + "; ".join(ap.CHECK_REJECTS), md)  # §11 quotes check's rule table
        self.assertIn("It WARNS on: " + "; ".join(w.rstrip(".") for w in ap.CHECK_WARNS) + ".", md)
        self.assertNotIn(".;", md)  # the policy sentence ends in a period; the §11 join strips it
        names = [c["name"] for c in manifest["clips"]]
        for n in ("idle", "walk", "attack", "hurt", "death", "aggro_idle", "calm_idle"):
            self.assertIn(n, names)
        clips = {c["name"]: c for c in manifest["clips"]}
        self.assertEqual(clips["idle"]["loop"], "true")
        self.assertEqual(clips["attack"]["loop"], "false")
        self.assertEqual(clips["death"]["loop"], "hold_on_last_frame")
        self.assertTrue(clips["attack"]["code_triggered"] and clips["attack"]["required"])  # shipped -> may not be renamed
        self.assertFalse(clips["hurt"]["required"])
        self.assertEqual(manifest["lock_mode"], "warn")  # ONE policy for every species (ruled 2026-09-06); `reject` is the mode kept for the evaluator
        self.assertEqual(manifest["lock_policy"], "warn-keyed, refuse-structural")
        self.assertEqual(manifest["lock_policy_text"], ap.LOCK_POLICY)
        self.assertEqual(set(manifest["locked_bones"]), {"hand", "arm", "root"})
        self.assertEqual(manifest["keyed_locked_by_shipped_clip"], {})  # the fixture clips key only the free tail
        self.assertEqual(manifest["textures"], [{"canonical": "fixture.png", "width": 64, "height": 32, "aliases": ["fixturetexture.png"]}])
        self.assertEqual(manifest["texture_size"], [64, 32])
        self.assertEqual(manifest["animation_file"], "fixture.animation.json")
        self.assertEqual(manifest["tier_label"], "Tier 2")
        self.assertFalse(manifest["native"])
        self.assertTrue(manifest["allow_idle_alt"])
        # every bone carries its cube signatures so `check` can refuse a changed cube / UV / size
        root = manifest["bones"][0]
        self.assertEqual(root["name"], "root")
        self.assertEqual(root["cubes"][0]["size"], [8.0, 12.0, 4.0])
        self.assertEqual(root["cubes"][0]["faces"]["down"], [12.0, 4.0, 8.0, -4.0])
        # the base loops list every bone of the rig, the gait group marked as the scaled one (contract §2.1 / §8)
        idle = [c for c in ap.clip_rows(fx, inv, fx.animation, ap.frequency_groups(fx), bone_names=["root", "tail", "arm", "hand"],
                                        locked=ap.locked_bones(fx, fx.geo)) if c["name"] == "idle"][0]
        self.assertTrue(idle["bones"].startswith("any of the 4 bones — gait group (speed-scaled): tail"))
        self.assertIn("arm (better left to `idle_pump`)", idle["bones"])
        self.assertIn("locked (see §7): root, hand", idle["bones"])
        # a calm_idle that idle covers (no attacking flag) is not artist work: it is not counted in the effort estimate
        calm = clips["calm_idle"]
        self.assertFalse(calm["required"])
        no_flag = dict(inv, attacking={"present": False, "verdict": "NONE", "sites": [], "reason": "", "facts": {}})
        covered = {c["name"]: c for c in ap.clip_rows(fx, no_flag, fx.animation, ap.frequency_groups(fx))}
        self.assertEqual(covered["calm_idle"]["verdict"], "covered by idle")
        self.assertNotIn("aggro_idle", covered)
        self.assertIn("to author or improve", manifest["effort_source"])
        self.assertEqual(ap.effort_estimate(fx, 4, list(covered.values()))[0], 4 + 0.2 * 4 + 1.0 * 8)  # calm_idle excluded, 8 to author/improve

    def test_bare_walk_rows_follow_the_naming_rule(self):
        # contract §2.1: "a species with ONE frequency group has only the bare names" - a Tier-2 flyer whose one
        # group is unscaled (Firefly: wing_left / wing_right at 2.5 rad/tick) gets the bare idle / walk rows and no
        # `<state>_<group>` row, so its shipped transcription (`idle` + `walk`) is in the SPEC and `check` accepts it
        # (item 15 refuter B, note B4; the first Tier-2 slice, 2026-09-13)
        import copy
        fx = self.repo.get("fixture")
        inv = ap.build_trigger_inventory(fx, self.repo)
        no_flag = dict(inv, attacking={"present": False, "verdict": "NONE", "sites": [], "reason": "", "facts": {}})
        one = copy.copy(fx)
        one.seed = dict(fx.seed or {}, groups=[{"name": "wings", "bones": ["tail", "arm"], "omega": 2.5, "axis": "z", "gait_scaled": False}],
                        clips=[], extras=[], wishlist=[])
        rows = {c["name"]: c for c in ap.clip_rows(one, no_flag, one.animation, ap.frequency_groups(one), bone_names=["root", "tail", "arm", "hand"])}
        self.assertIn("idle", rows)
        self.assertIn("walk", rows)
        self.assertNotIn("idle_wings", rows)
        self.assertNotIn("walk_wings", rows)
        self.assertEqual(rows["walk"]["group"], "wings")
        self.assertTrue(rows["walk"]["required"] and rows["idle"]["required"])
        self.assertTrue(rows["idle"]["bones"].startswith("any of the 4 bones — the primary group (unscaled; the bare clip is its transcription, a label under the naming rule): tail, arm"))
        self.assertNotIn("better left to", rows["idle"]["bones"])
        # a MULTI-group species without a gait group: the naming rule (owner 2026-09-13, addendum item 26 (2); contract
        # §2.1 amended) puts the bare `walk` on the seed's `primary_group` - the FIRST group when the seed names none -
        # and `<state>_<group>` on every other; both bare rows are required (the first slice's PROVISIONAL optional idle
        # of a multi-group no-gait species is gone with the ruling)
        two = copy.copy(fx)
        two.seed = dict(one.seed, groups=[{"name": "wings", "bones": ["tail"], "omega": 1.5, "axis": "z", "gait_scaled": False},
                                          {"name": "crest", "bones": ["arm"], "omega": 0.3, "axis": "x", "gait_scaled": False}])
        rows2 = {c["name"]: c for c in ap.clip_rows(two, no_flag, two.animation, ap.frequency_groups(two), bone_names=["root", "tail", "arm", "hand"])}
        for n in ("idle", "walk", "idle_crest", "walk_crest"):
            self.assertIn(n, rows2)
        for n in ("idle_wings", "walk_wings"):
            self.assertNotIn(n, rows2)
        self.assertEqual(rows2["walk"]["group"], "wings")
        self.assertTrue(rows2["walk"]["required"] and rows2["idle"]["required"])
        self.assertNotIn("provisional", rows2["idle"])  # the per-row marker flag is gone with the markers (item 28 (3))
        # README rule 5, the phase-locked kind (ruled 2026-09-06, Q14 (a)): every loop row is authored at 1.0 s
        self.assertEqual(rows2["idle"]["length_seconds"], 1.0)
        self.assertEqual(rows2["walk"]["length_rule"], "near:1.0")
        self.assertIn("the primary group `wings`'s", rows2["walk"]["note"])
        self.assertIn("a label, not a semantic", rows2["walk"]["note"])
        self.assertTrue(rows2["idle"]["bones"].startswith("any of the 4 bones — the primary group (unscaled; the bare clip is its transcription, a label under the naming rule): tail"))
        self.assertIn("arm (better left to `idle_crest`)", rows2["idle"]["bones"])
        # the seed names the primary group explicitly: that group carries the bare walk, the first group becomes walk_wings
        three = copy.copy(fx)
        three.seed = dict(two.seed, primary_group="crest")
        rows3 = {c["name"]: c for c in ap.clip_rows(three, no_flag, three.animation, ap.frequency_groups(three), bone_names=["root", "tail", "arm", "hand"])}
        self.assertEqual(rows3["walk"]["group"], "crest")
        for n in ("idle_wings", "walk_wings"):
            self.assertIn(n, rows3)
        for n in ("idle_crest", "walk_crest"):
            self.assertNotIn(n, rows3)
        # a gait group always carries the bare walk; a seed naming another group, or an unknown one, is refused
        fx_groups = ap.frequency_groups(fx)
        self.assertEqual(ap.primary_group(fx, fx_groups, [g for g in fx_groups if g.get("gait_scaled")])["name"], "gait")
        bad = copy.copy(fx)
        bad.seed = dict(fx.seed, primary_group="pump")
        with self.assertRaises(SystemExit):
            ap.clip_rows(bad, no_flag, bad.animation, ap.frequency_groups(bad), bone_names=["root", "tail", "arm", "hand"])
        unknown = copy.copy(fx)
        unknown.seed = dict(two.seed, primary_group="beak")
        with self.assertRaises(SystemExit):
            ap.clip_rows(unknown, no_flag, unknown.animation, ap.frequency_groups(unknown), bone_names=["root", "tail", "arm", "hand"])
        # the SPEC names the primary group beside the tempo table
        md, _ = ap.spec_document(two, self.repo, self.catalog, no_flag)
        self.assertIn("The bare `walk` clip is the primary group's, `wings`", md)

    def test_roundtrip_key_time_tolerance(self):
        # owner 2026-09-13 (addendum item 26 (5)): key TIMES compare within 5e-5 s - the Blockbench emulation writes
        # 4-decimal timecodes while a transcription's key times are k/(N-1) s at 10 decimals (the first Tier-2 slice's
        # Beaver round-trip: six time-only diffs); a 10-decimal time round-trips EQUAL, a 1e-4 difference does not
        fx = self.repo.get("fixture")
        geo = fx.geo
        tex = (self.root / "src/main/resources/assets/orespawn/textures/entity/fixture.png").read_bytes()

        def clip(times):
            return {"format_version": "1.8.0", "animations": {"walk": {"loop": True, "animation_length": 1.0, "bones": {"tail": {"rotation": {
                t: {"post": [10, 0, 0], "lerp_mode": "catmullrom"} for t in times}}}}}}

        shipped = clip(["0.0", "0.0769230769", "0.1538461538", "1.0"])
        bb = ap.build_bbmodel(fx, geo, shipped, [("fixture.png", tex, (64, 32))], ap.Warnings())
        back = ap.bbmodel_to_animation(bb)
        self.assertEqual(sorted(back["animations"]["walk"]["bones"]["tail"]["rotation"]), ["0.0", "0.0769", "0.1538", "1.0"])  # 4-decimal timecodes
        report = ap.roundtrip_diff(geo, geo, shipped, back, [])
        self.assertTrue(report["equal"], report["differences"])
        self.assertEqual(report["time_tolerance_seconds"], 5e-5)
        self.assertIn("4-decimal timecodes", report["time_tolerance_note"])
        self.assertIn("exporter source", report["time_tolerance_note"])
        self.assertFalse(ap.roundtrip_diff(geo, geo, clip(["0.0", "0.5", "1.0"]), clip(["0.0", "0.5001", "1.0"]), [])["equal"])
        self.assertTrue(ap.roundtrip_diff(geo, geo, clip(["0.0", "0.5", "1.0"]), clip(["0.0", "0.50004", "1.0"]), [])["equal"])

    def test_native_clip_rows_branch_and_wishlist_guard(self):
        nat = self.repo.get("native")
        inv = ap.build_trigger_inventory(nat, self.repo)
        md, manifest = ap.spec_document(nat, self.repo, self.catalog, inv)
        clips = {c["name"]: c for c in manifest["clips"]}
        self.assertEqual(list(clips), ["idle", "stance", "stomp", "death"])  # the accepted set IS the shipped native set
        self.assertTrue(manifest["native"])
        self.assertFalse(manifest["allow_idle_alt"])
        self.assertEqual(manifest["tier_label"], "Tier 1 (boss; the design's 'done' row)")
        self.assertEqual(set(manifest["locked_bones"]), {"head", "body", "root"})
        self.assertEqual(manifest["keyed_locked_by_shipped_clip"], {"stance": ["head"], "stomp": ["head"]})
        self.assertEqual(manifest["lock_mode"], "warn")
        rows = {c["name"]: c for c in ap.clip_rows(nat, inv, nat.animation, [], bone_names=["root", "body", "head", "tail"], locked=ap.locked_bones(nat, nat.geo))}
        self.assertIn("`Movement` controller selects it when if (this.isAwake())", rows["stance"]["trigger"])
        self.assertIn("triggerAnim(\"Actions\", \"death\")", rows["death"]["trigger"])
        self.assertIn("in `die`", rows["death"]["trigger"])
        self.assertEqual(rows["stance"]["layer"], "native `Movement` controller (state)")
        self.assertEqual(rows["stomp"]["layer"], "native `Actions` controller (triggered)")
        # a verdict that invites improving a clip which keys locked bones repeats the consequence (the one policy sentence); the
        # contract-mapping sentence on a native row (the seed's mapping, ruled 2026-09-06, Q16 (a)) precedes the lock note, so it
        # never reads as the policy's; no marker is left on the row (owner 2026-09-13, third set, item 28 (3))
        self.assertEqual(rows["stomp"]["keyed_locked"], ["head"])
        self.assertIn("keys 1 locked bone(s) (head): " + ap.LOCK_POLICY, rows["stomp"]["note"])
        self.assertIn("mapped to the contract's `extra` by the seed (ruled 2026-09-06, Q16 (a): the pilot boss keeps its native clip set)", rows["stomp"]["note"])
        self.assertLess(rows["stomp"]["note"].index("Q16 (a)"), rows["stomp"]["note"].index("keys 1 locked bone(s)"))
        self.assertNotIn("PROVISIONAL", rows["stomp"]["note"])
        self.assertNotIn("open question", rows["stomp"]["note"])
        self.assertNotIn("provisional", rows["stomp"])
        self.assertEqual(rows["idle"]["keyed_locked"], [])
        self.assertIn("Tier 1 (boss; the design's 'done' row)", md)
        self.assertIn("`stance` keys 1", md)
        self.assertIn("`stomp` keys 1", md)
        self.assertIn("allowed and warned as above: the hitbox parts follow those bones in-game", md)
        self.assertNotIn("under the REJECT policy the shipped file itself would fail", md)
        # the wishlist only invites clips the manifest accepts; anything else is marked, and §5.2 holds the future items
        self.assertIn("- a heavier `stance` loop\n", md)
        self.assertIn("a wing-beat `fly` loop distinct from the hover — NOT accepted by `check` today: `fly` is not in this creature's clip set", md)
        self.assertIn("### 5.2 Not accepted today", md)
        self.assertIn("- a `hurt` flinch on the struck head\n", md)  # the §5.2 bullets carry no marker (item 28 (3)); the heading says why
        self.assertNotIn("PROVISIONAL", md)
        self.assertNotIn("open question", md.lower())
        self.assertTrue(any(w[1] == "WISHLIST_UNACCEPTED" and w[0] == "native" for w in self.repo.warnings.items))
        self.assertTrue(any(w[1] == "LOCKED_BONES_KEYED" and w[0] == "native" and "1 of the 3" in w[2] for w in self.repo.warnings.items))
        self.assertIn("hurts EACH victim in its box 2x per roll", md)
        self.assertIn("not used by this species (native clip set)", md)

    # --- .bbmodel round-trip --------------------------------------------------------------------

    def test_roundtrip_on_fixture_rig(self):
        fx = self.repo.get("fixture")
        geo, anim = fx.geo, fx.animation
        tex = (self.root / "src/main/resources/assets/orespawn/textures/entity/fixture.png").read_bytes()
        bb = ap.build_bbmodel(fx, geo, anim, [("fixture.png", tex, (64, 32))], ap.Warnings())
        self.assertEqual(bb["meta"]["model_format"], "bedrock")
        self.assertFalse(bb["meta"]["box_uv"])  # mixed rig: per-element box_uv
        self.assertEqual(len(bb["elements"]), 4)
        self.assertEqual(len(bb["outliner"]), 1)  # root; tail/arm nested; hand under arm
        root = bb["outliner"][0]
        self.assertEqual([c["name"] for c in root["children"] if isinstance(c, dict)], ["tail", "arm"])
        arm = [c for c in root["children"] if isinstance(c, dict) and c["name"] == "arm"][0]
        self.assertEqual(arm["origin"], [-4.0, 22.0, 0.0])          # X mirrored for Blockbench
        self.assertEqual(arm["rotation"], [-10.0, 0.0, 5.0])        # X, Y negated; Z kept
        hand_el = [e for e in bb["elements"] if e["name"] == "hand"][0]
        self.assertTrue(hand_el["box_uv"])
        self.assertEqual(hand_el["uv_offset"], [48.0, 0.0])
        self.assertFalse(hand_el["mirror_uv"])                       # the cube's own mirror:false wins over the bone's
        self.assertEqual(hand_el["rotation"], [0.0, 0.0, -15.0])
        root_el = [e for e in bb["elements"] if e["name"] == "root"][0]
        self.assertEqual(root_el["from"], [-4.0, 12.0, -2.0])
        self.assertEqual(root_el["to"], [4.0, 24.0, 2.0])
        self.assertEqual(root_el["faces"]["down"]["uv"], [12.0, 4.0, 20.0, 0.0])  # the flipped V survives
        self.assertEqual(bb["unhandled_root_fields"][ap.STASH_KEY], {"orespawn:bone_draw_order": ["root", "tail", "arm", "hand"]})
        self.assertEqual([a["loop"] for a in bb["animations"]], ["loop", "once", "hold"])
        back_geo, reattached = ap.bbmodel_to_geo(bb)
        back_anim = ap.bbmodel_to_animation(bb)
        report = ap.roundtrip_diff(geo, back_geo, anim, back_anim, reattached)
        self.assertTrue(report["equal"], report["differences"])
        self.assertTrue(report["bone_order_preserved"])
        self.assertEqual(report["reattached_by_this_importer"], ["orespawn:bone_draw_order"])
        self.assertEqual(report["dropped_keys"], ["root.cubes[0].modelpart_mirror"])
        self.assertIn("writer against the tool's importer", report["note"])
        self.assertEqual([b["name"] for b in back_geo["minecraft:geometry"][0]["bones"]], ["root", "tail", "arm", "hand"])
        hand_back = back_geo["minecraft:geometry"][0]["bones"][3]
        self.assertEqual(hand_back["cubes"][0]["rotation"], [0.0, 0.0, -15.0])
        # a mixed-UV project writes no bone-level mirror (Blockbench: only when Project.box_uv); the cube's
        # effective mirror is what matters and it stays false
        self.assertIs(hand_back["cubes"][0].get("mirror", hand_back.get("mirror", False)), False)
        idle_back = back_anim["animations"]["idle"]["bones"]["tail"]["rotation"]
        self.assertEqual(idle_back["0.5"]["lerp_mode"], "catmullrom")
        attack_back = back_anim["animations"]["attack"]["bones"]["tail"]["rotation"]
        self.assertEqual(attack_back["0.0"]["pre"], [0, 0, 0])
        # easing survives the writer and the importer
        self.assertEqual(attack_back["0.25"]["easing"], "easeInOutSine")
        self.assertEqual(attack_back["0.25"]["easingArgs"], [2])

    def test_roundtrip_reports_a_dropped_easing_or_unknown_key(self):
        fx = self.repo.get("fixture")
        geo = fx.geo
        odd = json.loads(json.dumps(FIXTURE_ANIM))
        odd["animations"]["idle"]["bones"]["tail"]["rotation"]["0.5"]["orespawn:tag"] = 1  # a key Blockbench has no field for
        bb = ap.build_bbmodel(fx, geo, odd, [], ap.Warnings())
        back_geo, reattached = ap.bbmodel_to_geo(bb)
        report = ap.roundtrip_diff(geo, back_geo, odd, ap.bbmodel_to_animation(bb), reattached)
        self.assertFalse(report["equal"])
        self.assertTrue(any("keyframe keys" in d and "orespawn:tag" in d for d in report["differences"]), report["differences"])
        self.assertIn("clip idle tail.rotation @ 0.5: orespawn:tag", report["dropped_keys"])
        # a dropped easing is a difference too
        bb2 = ap.build_bbmodel(fx, geo, fx.animation, [], ap.Warnings())
        for a in bb2["animations"]:
            for animator in a["animators"].values():
                for kf in animator["keyframes"]:
                    kf.pop("easing", None)
                    kf.pop("easingArgs", None)
        report2 = ap.roundtrip_diff(geo, ap.bbmodel_to_geo(bb2)[0], fx.animation, ap.bbmodel_to_animation(bb2), [])
        self.assertTrue(any("lost: ['easing', 'easingArgs']" in d for d in report2["differences"]), report2["differences"])

    def test_roundtrip_detects_a_reordered_bone(self):
        fx = self.repo.get("fixture")
        geo = fx.geo
        bb = ap.build_bbmodel(fx, geo, fx.animation, [], ap.Warnings())
        root = bb["outliner"][0]
        groups = [c for c in root["children"] if isinstance(c, dict)]
        root["children"] = [c for c in root["children"] if not isinstance(c, dict)] + groups[::-1]
        back_geo, reattached = ap.bbmodel_to_geo(bb)
        report = ap.roundtrip_diff(geo, back_geo, fx.animation, ap.bbmodel_to_animation(bb), reattached)
        self.assertFalse(report["bone_order_preserved"])
        self.assertFalse(report["equal"])

    def test_roundtrip_reports_a_non_dfs_shipped_order(self):
        # a shipped rig whose bone list is not depth-first cannot survive Blockbench's outliner-order export
        fx = self.repo.get("fixture")
        bb = ap.build_bbmodel(fx, NON_DFS_GEO, {"animations": {}}, [], ap.Warnings())
        back_geo, reattached = ap.bbmodel_to_geo(bb)
        self.assertEqual([b["name"] for b in back_geo["minecraft:geometry"][0]["bones"]], ["root", "arm", "hand", "tail"])
        report = ap.roundtrip_diff(NON_DFS_GEO, back_geo, {"animations": {}}, {"animations": {}}, reattached)
        self.assertFalse(report["bone_order_preserved"])
        self.assertFalse(report["equal"])
        self.assertTrue(any(d.startswith("bone order/set differs") for d in report["differences"]))

    # --- package + check ------------------------------------------------------------------------

    def _returned(self, anim: dict | None, textures: dict[str, bytes] | None = None, extra_files: dict[str, str] | None = None,
                  anim_name: str = "fixture.animation.json") -> Path:
        d = Path(tempfile.mkdtemp(prefix="ret_", dir=self.tmp))
        if anim is not None:
            (d / anim_name).write_text(json.dumps(anim), encoding="utf-8")
        if textures:
            (d / "textures").mkdir()
            for n, b in textures.items():
                (d / "textures" / n).write_bytes(b)
        for n, t in (extra_files or {}).items():
            (d / n).write_text(t, encoding="utf-8")
        return d

    GOOD = {"format_version": "1.8.0", "animations": {
        "idle": {"loop": True, "animation_length": 1.0, "bones": {"tail": {"rotation": {"0.0": [0, 0, 0]}}}},
        "walk": {"loop": True, "animation_length": 1.0, "bones": {"tail": {"rotation": {"0.0": [0, 0, 0]}}}},
        "attack": {"loop": False, "animation_length": 0.5, "bones": {"tail": {"rotation": {"0.0": [0, 0, 0]}}}},
        "death": {"loop": "hold_on_last_frame", "animation_length": 2.0, "bones": {"tail": {"rotation": {"0.0": [0, 0, 0]}}}},
        "idle_alt_1": {"loop": False, "animation_length": 1.0, "bones": {"tail": {"rotation": {"0.0": [0, 0, 0]}}}},
    }}

    def _package_fixture(self) -> Path:
        out = self.tmp / "out"
        if not (out / "entities/fixture/spec.manifest.json").exists():
            ap.build_package(self.repo, out, ["fixture"])
        return out / "entities/fixture/spec.manifest.json"

    @staticmethod
    def _has(findings, sev, text) -> bool:
        return any(s == sev and text in msg for s, msg in findings)

    def test_package_and_check_rejections(self):
        out = self.tmp / "out"
        summary = ap.build_package(self.repo, out, ["fixture"])
        entry = summary["entities"][0]
        self.assertEqual(entry["registry"], "fixture")
        self.assertTrue(entry["roundtrip"]["equal"])
        self.assertEqual(entry["tier_label"], "Tier 2")
        folder = out / "entities/fixture"
        for name in ("fixture.geo.json", "fixture.animation.json", "fixture.bbmodel", "SPEC.md", "spec.manifest.json",
                     "textures/fixture.png", "reference/SLOTS.md", "roundtrip.report.json"):
            self.assertTrue((folder / name).exists(), name)
        self.assertTrue((out / "INVENTORY.csv").exists() and (out / "TEXTURE_MAP.csv").exists() and (out / "README_FIRST.md").exists())
        readme = (out / "README_FIRST.md").read_text(encoding="utf-8")
        self.assertIn(ap.LOCK_POLICY, readme)  # README rule 6 carries the one policy sentence ...
        self.assertIn(ap.LOCK_REJECT_MODE, readme)  # ... and that the reject mode exists for the day the evaluator lands
        self.assertNotIn("Never key a bone", readme)  # keying a locked bone is allowed (and warned)
        self.assertNotIn("never key", readme.lower())
        self.assertNotIn(".;", readme)
        self.assertNotIn(".;", (folder / "SPEC.md").read_text(encoding="utf-8"))
        self.assertIn("a `locked` bone renamed, re-parented or deleted in a returned `.geo.json` (the finding names the bone)", readme)
        self.assertIn("**What `check` REJECTS**", readme)
        for r in ap.CHECK_REJECTS:
            self.assertIn(r, readme)
        # the reference-only clip (owner 2026-09-13, second set, item 27 (3)): README rule 7 says so beside the `_preview` sentence,
        # the folder listing names the file, and the REJECT is in the checker's own rule table
        self.assertIn(ap.README_REFERENCE_SENTENCE, readme)
        self.assertIn("no `_preview` files in a delivery (a Blockbench-only aid, never in the jar — ruled 2026-09-06, Q15 (a); the checker warns). "
                      + ap.README_REFERENCE_SENTENCE, readme)
        self.assertIn("<registry_name>_reference.animation.json", readme)
        self.assertTrue(any("`*_reference.animation.json` returned" in r for r in ap.CHECK_REJECTS))
        manifest = folder / "spec.manifest.json"
        good = self.GOOD

        findings, passed = ap.check_folder(self._returned(good, {"fixture.png": png_bytes(64, 32, 5)}), manifest)
        self.assertTrue(passed, findings)
        self.assertFalse(any(sev == "REJECT" for sev, _ in findings))
        self.assertTrue(self._has(findings, "OK", "locked bones: 0 of 3 keyed across 0 clip(s) [lock_mode warn; warn-keyed, refuse-structural] — " + ap.LOCK_POLICY), findings)
        self.assertTrue(self._has(findings, "OK", "checked 5 clip(s), 15 keyframe value(s), 1 texture(s)"))
        self.assertFalse(self._has(findings, "OK", "nothing to report"))

        renamed_bone = json.loads(json.dumps(good))
        renamed_bone["animations"]["idle"]["bones"] = {"tale": renamed_bone["animations"]["idle"]["bones"]["tail"]}
        findings, passed = ap.check_folder(self._returned(renamed_bone), manifest)
        self.assertFalse(passed)
        self.assertTrue(self._has(findings, "REJECT", "unknown bone 'tale'"), findings)

        wrong_loop = json.loads(json.dumps(good))
        wrong_loop["animations"]["idle"]["loop"] = False
        findings, passed = ap.check_folder(self._returned(wrong_loop), manifest)
        self.assertFalse(passed)
        self.assertTrue(self._has(findings, "REJECT", "clip 'idle' loop is false, the SPEC says true"), findings)

        renamed_clip = json.loads(json.dumps(good))
        renamed_clip["animations"]["strike"] = renamed_clip["animations"].pop("attack")
        findings, passed = ap.check_folder(self._returned(renamed_clip), manifest)
        self.assertFalse(passed)
        self.assertTrue(self._has(findings, "REJECT", "clip 'strike' is not in the SPEC"), findings)
        self.assertTrue(self._has(findings, "REJECT", "code-triggered clip 'attack' is missing"), findings)

        findings, passed = ap.check_folder(self._returned(good, {"fixture.png": png_bytes(32, 32, 5)}), manifest)
        self.assertFalse(passed)
        self.assertTrue(self._has(findings, "REJECT", "canvas 32x32, must stay 64x32"), findings)

        # a keyed locked bone: allowed and WARNED (the ruled policy, 2026-09-06) — the consequence and the policy named, nothing PROVISIONAL
        locked = json.loads(json.dumps(good))
        locked["animations"]["idle"]["bones"]["hand"] = {"rotation": {"0.0": [0, 0, 0]}}
        findings, passed = ap.check_folder(self._returned(locked), manifest)
        self.assertTrue(passed, findings)
        self.assertTrue(self._has(findings, "WARN", "clip 'idle' keys 1 locked bone(s): hand (they carry or parent a hitbox part; the part follows the bone in-game — allowed, keep it deliberate)"), findings)
        self.assertTrue(self._has(findings, "WARN", "locked bones: 1 of 3 keyed across 1 clip(s) [lock_mode warn; warn-keyed, refuse-structural] — " + ap.LOCK_POLICY), findings)
        self.assertFalse(any("PROVISIONAL" in msg or "open question 16" in msg for _, msg in findings), findings)
        # ... and the reject mode stays available for the day the evaluator lands (the --lock-mode override, or the manifest's lock_mode)
        findings, passed = ap.check_folder(self._returned(locked), manifest, lock_mode_override="reject")
        self.assertFalse(passed)
        self.assertTrue(self._has(findings, "REJECT", "clip 'idle' keys 1 locked bone(s): hand (they carry or parent a hitbox part; REJECTED under lock_mode reject)"), findings)
        self.assertTrue(self._has(findings, "REJECT", "[lock_mode reject; warn-keyed, refuse-structural] — keys on locked bones are REJECTED in this run "
                                  "(the reject mode, kept for the day the server-side hitbox evaluator lands); the ruled policy: " + ap.LOCK_POLICY), findings)
        strict = json.loads(manifest.read_text(encoding="utf-8"))
        strict["lock_mode"] = "reject"
        strict_path = self.tmp / "strict.manifest.json"
        strict_path.write_text(json.dumps(strict), encoding="utf-8")
        findings, passed = ap.check_folder(self._returned(locked), strict_path)
        self.assertFalse(passed)
        self.assertTrue(self._has(findings, "REJECT", "locked bones: 1 of 3 keyed"), findings)
        # a manifest generated before the ruling (its lock_policy is the old sentence) is flagged; the ruled policy applies regardless
        stale = json.loads(manifest.read_text(encoding="utf-8"))
        stale["lock_policy"] = "keying a locked bone is a WARN today ... PROVISIONAL, open question 16"
        stale_path = self.tmp / "stale.manifest.json"
        stale_path.write_text(json.dumps(stale), encoding="utf-8")
        findings, passed = ap.check_folder(self._returned(locked), stale_path)
        self.assertTrue(passed, findings)
        self.assertTrue(self._has(findings, "WARN", "lock_policy is not `warn-keyed, refuse-structural` — the package predates the 2026-09-06 ruling"), findings)
        self.assertTrue(self._has(findings, "WARN", "[lock_mode warn; warn-keyed, refuse-structural] — " + ap.LOCK_POLICY), findings)
        self.assertFalse(any("PROVISIONAL" in msg for _, msg in findings), findings)

        # a _preview file: a WARN citing the ruling (2026-09-06, Q15 (a): Blockbench-only, never in the jar), no longer a REJECT
        findings, passed = ap.check_folder(self._returned(good, extra_files={"fixture_preview.animation.json": "{}"}), manifest)
        self.assertTrue(passed, findings)
        self.assertTrue(self._has(findings, "WARN", "_preview file is a Blockbench-only aid, never in the jar (ruled 2026-09-06, Q15 (a)); leave it out of the delivery"), findings)
        self.assertFalse(any("PROVISIONAL" in m or "open question" in m for _, m in findings), findings)
        self.assertFalse(self._has(findings, "REJECT", "animation files returned"))

        findings, passed = ap.check_folder(self._returned(good, extra_files={"fixture.geo.json": json.dumps({"minecraft:geometry": [{"bones": [{"name": "root"}]}]})}), manifest)
        self.assertFalse(passed)
        self.assertTrue(self._has(findings, "REJECT", "bone set/order changed"), findings)

    def test_reference_clip_packaged_spec_section_and_manifest(self):
        """The reference-only clip (owner 2026-09-13, second set, item 27 (3)): `package` copies tools/reference_clips/
        <registry>_reference.animation.json beside the sheet byte for byte; SPEC §4.3 states what it is, the fixed inputs, the
        span, that it is never returned or shipped, and - without an exact transcription - the plain-language transcription
        of the source formulas (the seed's `formulas`, design section 5); the manifest records `reference_clip` (file, sha256,
        sampled inputs, span) and `exact_transcription`."""
        out = self.tmp / "out_reference"
        ap.build_package(self.repo, out, ["fixture"])
        folder = out / "entities/fixture"
        src = self.root / "tools/reference_clips/fixture_reference.animation.json"
        copied = folder / "fixture_reference.animation.json"
        self.assertTrue(copied.exists())
        self.assertEqual(copied.read_bytes(), src.read_bytes())
        manifest = json.loads((folder / "spec.manifest.json").read_text(encoding="utf-8"))
        rc = manifest["reference_clip"]
        self.assertEqual(rc["file"], "fixture_reference.animation.json")
        self.assertEqual(rc["sha256"], ap.sha256_file(src))
        self.assertEqual(rc["clip"], "reference")
        self.assertTrue(rc["reference_only"])
        self.assertEqual(rc["rule"], "period_multiple")  # the span rule (owner 2026-09-13, third set, item 28 (5)): a period multiple
        self.assertEqual(rc["period_multiple_k"], 1)
        self.assertAlmostEqual(rc["period_ticks"], 1.6981317008)
        self.assertAlmostEqual(rc["span_ticks"], 1.6981317008)
        self.assertEqual(rc["keys_per_bone"], 3)
        self.assertIn("limbSwingAmount 1.0", rc["sampled_inputs"])
        self.assertEqual(rc["position_bones"], ["arm"])
        self.assertFalse(manifest["exact_transcription"])  # no tools/keyframe_clips/fixture.json: the fixture runs on its hook
        spec = (folder / "SPEC.md").read_text(encoding="utf-8")
        self.assertIn("### 4.3 Reference clip (reference-only)", spec)
        self.assertIn("`fixture_reference.animation.json` (beside this sheet; sha256 `" + rc["sha256"] + "`) is NOT a clip to edit, improve, return or ship.", spec)
        self.assertIn("full walking speed (limbSwingAmount 1, the walk position and the age advancing one tick per key), not attacking, looking straight ahead", spec)
        self.assertIn("one period of its slowest rhythm — 1.698 ticks (0.085 s): every moving bone is back within 5 degrees of its start there, so the last key closes the loop", spec)
        self.assertIn("20 keys per second (3 keys per bone), linear keys", spec)
        self.assertIn("`check` REJECTS a returned `*_reference.animation.json` by name, and the game's jar never carries one", spec)
        self.assertIn("bones the code also MOVES (position keys): `arm`", spec)
        self.assertIn("This creature has NO exact keyframe transcription", spec)
        self.assertIn("Its source formulas in plain language (migration design section 5", spec)
        self.assertIn("no `formulas` in the seed yet", spec)  # FIXTURE_SEED carries none: the placeholder and a warning, never silence
        self.assertTrue(self.warnings_with("FORMULAS_MISSING"))
        self.assertLess(spec.index("### 4.3 Reference clip"), spec.index("## 5. Clips"))
        # a clip manifest whose clips the shipped file carries makes it an exact transcription (the 15 registries today)
        clips_dir = self.root / "tools/keyframe_clips"
        clips_dir.mkdir(exist_ok=True)
        clip_manifest = clips_dir / "fixture.json"
        try:
            clip_manifest.write_text(json.dumps({"species": "fixture", "groups": [{"name": "gait", "clip": "idle"}]}), encoding="utf-8")
            fx = self.repo.get("fixture")
            inv = ap.build_trigger_inventory(fx, self.repo)
            md, m2 = ap.spec_document(fx, self.repo, self.catalog, inv)
            self.assertTrue(m2["exact_transcription"])
            self.assertIn("This creature ships an EXACT keyframe transcription of its code in `fixture.animation.json`", md)
            self.assertNotIn("This creature has NO exact keyframe transcription", md)
            clip_manifest.write_text(json.dumps({"species": "fixture", "groups": [{"name": "gait", "clip": "walk"}]}), encoding="utf-8")
            _, m3 = ap.spec_document(fx, self.repo, self.catalog, inv)
            self.assertFalse(m3["exact_transcription"])  # `walk` is not in the shipped file: the transcription does not ship
        finally:
            clip_manifest.unlink()
        # a native rig (the boss) has no sampled clip by design - said so, no warning; a hook species without a row is warned
        nat = self.repo.get("native")
        inv = ap.build_trigger_inventory(nat, self.repo)
        md, mn = ap.spec_document(nat, self.repo, self.catalog, inv)
        self.assertIsNone(mn["reference_clip"])
        self.assertFalse(mn["exact_transcription"])
        self.assertIn("no sampled reference clip: this creature has no classic hook (a native GeckoLib rig)", md)
        self.assertIn("This creature is a native GeckoLib rig: it has no classic code to transcribe", md)
        self.assertNotIn("This creature has NO exact keyframe transcription", md)
        self.assertFalse([w for w in self.warnings_with("FORMULAS_MISSING") if w[0] == "native"])
        self.assertFalse([w for w in self.warnings_with("REFERENCE_CLIP_MISSING") if w[0] == "native"])
        saved = self.repo.reference_clips
        try:
            self.repo.reference_clips = {}
            fx = self.repo.get("fixture")
            inv = ap.build_trigger_inventory(fx, self.repo)
            md, mx = ap.spec_document(fx, self.repo, self.catalog, inv)
            self.assertIsNone(mx["reference_clip"])
            self.assertIn("the sampler has not run for this creature: gradle referenceClips", md)
            self.assertTrue([w for w in self.warnings_with("REFERENCE_CLIP_MISSING") if w[0] == "fixture"])
        finally:
            self.repo.reference_clips = saved

    def test_check_rejects_a_returned_reference_clip(self):
        """`check` REJECTS a returned `*_reference.animation.json` by name (reference-only, never edited, never delivered, never
        shipped) - a REJECT where `_preview` is a WARN - unless it is the package's own copy back untouched (the sheet's file name
        and sha256), which is warned as not a delivery so the generated folder itself checks PASS; neither counts as a second
        animation file."""
        manifest = self._package_fixture()
        good = self.GOOD
        own = (self.root / "tools/reference_clips/fixture_reference.animation.json").read_text(encoding="utf-8")
        # the package's own copy, untouched: a WARN, PASS
        findings, passed = ap.check_folder(self._returned(good, extra_files={"fixture_reference.animation.json": own}), manifest)
        self.assertTrue(passed, findings)
        self.assertTrue(self._has(findings, "WARN", "fixture_reference.animation.json: the package's own reference-only clip came back untouched — not a delivery"), findings)
        self.assertFalse(self._has(findings, "REJECT", "animation files returned"), findings)
        self.assertFalse(self._has(findings, "REJECT", "wrong file name"), findings)
        # the same name with edited bytes: a REJECT naming the file
        edited = json.loads(own)
        edited["animations"]["reference"]["bones"]["tail"]["rotation"]["0.05"]["post"] = [0.0, 0.0, 0.0]
        findings, passed = ap.check_folder(self._returned(good, extra_files={"fixture_reference.animation.json": json.dumps(edited)}), manifest)
        self.assertFalse(passed)
        self.assertTrue(self._has(findings, "REJECT", "fixture_reference.animation.json: a reference-only clip that is not the package's own untouched copy (its bytes differ from the sheet's)"), findings)
        self.assertTrue(self._has(findings, "REJECT", "never edited, never delivered, never shipped; remove it from the returned folder"), findings)
        self.assertFalse(self._has(findings, "REJECT", "animation files returned"), findings)  # a refused file, not a second delivery
        # a reference name the sheet never gave: a REJECT naming the file
        findings, passed = ap.check_folder(self._returned(good, extra_files={"fixture_v2_reference.animation.json": own}), manifest)
        self.assertFalse(passed)
        self.assertTrue(self._has(findings, "REJECT", "fixture_v2_reference.animation.json: a reference-only clip that is not the package's own untouched copy (a name the sheet never gave)"), findings)
        # the generated package folder itself, as the artist receives it: its own clip is the WARN, never a REJECT (the dry run's
        # rule - every real package folder checks PASS; the synthetic fixture's shipped file lacks `walk`, its own REJECT)
        out = self.tmp / "out_reference_check"
        ap.build_package(self.repo, out, ["fixture"])
        findings, _ = ap.check_folder(out / "entities/fixture")
        self.assertTrue(self._has(findings, "WARN", "the package's own reference-only clip came back untouched"), findings)
        self.assertFalse(any(sev == "REJECT" and "reference" in msg for sev, msg in findings), findings)
        # the same folder without it passes with no reference finding at all
        findings, passed = ap.check_folder(self._returned(good), manifest)
        self.assertTrue(passed, findings)
        self.assertFalse(any("reference-only" in msg for _, msg in findings), findings)

    def test_check_rejects_every_rule_it_claims(self):
        manifest = self._package_fixture()
        good = self.GOOD
        # a missing folder and an empty folder are REJECTs, never "nothing to report"
        findings, passed = ap.check_folder(self.tmp / "no_such_folder", manifest)
        self.assertFalse(passed)
        self.assertTrue(self._has(findings, "REJECT", "does not exist: nothing was returned"), findings)
        empty = Path(tempfile.mkdtemp(prefix="empty_", dir=self.tmp))
        findings, passed = ap.check_folder(empty, manifest)
        self.assertFalse(passed)
        self.assertTrue(self._has(findings, "REJECT", "is empty: nothing was returned"), findings)
        self.assertTrue(self._has(findings, "REJECT", "no `fixture.animation.json` returned"), findings)
        # a wrongly named animation file, and a second one
        findings, passed = ap.check_folder(self._returned(good, anim_name="fixture_v2.animation.json"), manifest)
        self.assertFalse(passed)
        self.assertTrue(self._has(findings, "REJECT", "fixture_v2.animation.json: wrong file name — the sheet names it `fixture.animation.json`"), findings)
        findings, passed = ap.check_folder(self._returned(good, extra_files={"fixture_old.animation.json": json.dumps(good)}), manifest)
        self.assertFalse(passed)
        self.assertTrue(self._has(findings, "REJECT", "2 animation files returned"), findings)
        # a Molang string (rule 7), a string-typed number (WARN), a non-numeric and a non-finite value
        molang = json.loads(json.dumps(good))
        molang["animations"]["idle"]["bones"]["tail"]["rotation"]["0.0"] = ["math.sin(query.anim_time * 10)", 0, 0]
        findings, passed = ap.check_folder(self._returned(molang), manifest)
        self.assertFalse(passed)
        self.assertTrue(self._has(findings, "REJECT", "is a Molang expression / text (rule 7"), findings)
        stringy = json.loads(json.dumps(good))
        stringy["animations"]["idle"]["bones"]["tail"]["rotation"]["0.0"] = ["12.5", 0, 0]
        findings, passed = ap.check_folder(self._returned(stringy), manifest)
        self.assertTrue(passed, findings)
        self.assertTrue(self._has(findings, "WARN", "number written as a string"), findings)
        bad = json.loads(json.dumps(good))
        bad["animations"]["idle"]["bones"]["tail"]["rotation"]["0.0"] = [True, None, 0]
        findings, passed = ap.check_folder(self._returned(bad), manifest)
        self.assertFalse(passed)
        self.assertEqual(sum(1 for s, m in findings if s == "REJECT" and "is not a finite number" in m), 2, findings)
        nan = json.loads(json.dumps(good))
        nan["animations"]["idle"]["bones"]["tail"]["rotation"]["0.0"] = [float("nan"), float("inf"), 0]
        findings, passed = ap.check_folder(self._returned(nan), manifest)
        self.assertFalse(passed)
        self.assertEqual(sum(1 for s, m in findings if s == "REJECT" and "is not a finite number" in m), 2, findings)
        # absurd values: the bounds are named
        absurd = json.loads(json.dumps(good))
        absurd["animations"]["idle"]["bones"]["tail"]["rotation"]["0.0"] = [5000, 0, 0]
        absurd["animations"]["idle"]["bones"]["tail"]["position"] = {"0.0": [0, 2000, 0]}
        findings, passed = ap.check_folder(self._returned(absurd), manifest)
        self.assertFalse(passed)
        self.assertTrue(self._has(findings, "REJECT", "rotation 5000 exceeds the bound |rotation| <= 3600 degrees"), findings)
        self.assertTrue(self._has(findings, "REJECT", "position 2000 exceeds the bound |position| <= 1024 units"), findings)
        # a key beyond animation_length (the declared length shorter than the last key); a missing length is a WARN
        late = json.loads(json.dumps(good))
        late["animations"]["idle"]["bones"]["tail"]["rotation"] = {"0.0": [0, 0, 0], "1.5": [10, 0, 0]}
        findings, passed = ap.check_folder(self._returned(late), manifest)
        self.assertFalse(passed)
        self.assertTrue(self._has(findings, "REJECT", "clip 'idle' has a key at 1.5 s beyond its animation_length 1 s"), findings)
        nolen = json.loads(json.dumps(good))
        del nolen["animations"]["idle"]["animation_length"]
        nolen["animations"]["idle"]["bones"]["tail"]["rotation"] = {"0.0": [0, 0, 0], "0.75": [10, 0, 0]}
        findings, passed = ap.check_folder(self._returned(nolen), manifest)
        self.assertTrue(passed, findings)
        self.assertTrue(self._has(findings, "WARN", "clip 'idle' has no animation_length; the game takes the last key (0.75 s)"), findings)
        # a custom-instruction timeline, an unknown keyframe key, an unsupported channel
        timeline = json.loads(json.dumps(good))
        timeline["animations"]["idle"]["timeline"] = {"0.0": "/say hi"}
        timeline["animations"]["idle"]["bones"]["tail"]["rotation"]["0.0"] = {"vector": [0, 0, 0], "bezier_left": 1}
        timeline["animations"]["idle"]["bones"]["tail"]["glow"] = {"0.0": [1, 1, 1]}
        findings, passed = ap.check_folder(self._returned(timeline), manifest)
        self.assertFalse(passed)
        self.assertTrue(self._has(findings, "REJECT", "has a `timeline` (custom-instruction keys are not handled; rule 7)"), findings)
        self.assertTrue(self._has(findings, "WARN", "unknown keyframe key 'bezier_left'"), findings)
        self.assertTrue(self._has(findings, "REJECT", "unsupported channel 'glow'"), findings)
        # a returned geo whose cube size / UV / canvas differ
        geo = json.loads(json.dumps(FIXTURE_GEO))
        geo["minecraft:geometry"][0]["bones"][1]["cubes"][0]["size"] = [2, 2, 8]
        geo["minecraft:geometry"][0]["bones"][0]["cubes"][0]["uv"]["north"]["uv"] = [5, 4]
        findings, passed = ap.check_folder(self._returned(good, extra_files={"fixture.geo.json": json.dumps(geo)}), manifest)
        self.assertFalse(passed)
        self.assertTrue(self._has(findings, "REJECT", "bone tail cube[0] size: [2.0, 2.0, 6.0] -> [2.0, 2.0, 8.0]"), findings)
        self.assertTrue(self._has(findings, "REJECT", "bone root cube[0] uv.north: [4.0, 4.0, 8.0, 12.0] -> [5.0, 4.0, 8.0, 12.0]"), findings)
        self.assertTrue(self._has(findings, "WARN", "a geo was returned"), findings)
        canvas = json.loads(json.dumps(FIXTURE_GEO))
        canvas["minecraft:geometry"][0]["description"]["texture_width"] = 128
        findings, passed = ap.check_folder(self._returned(good, extra_files={"fixture.geo.json": json.dumps(canvas)}), manifest)
        self.assertFalse(passed)
        self.assertTrue(self._has(findings, "REJECT", "texture canvas 128x32 must stay 64x32"), findings)
        # an unchanged geo passes with only the WARN
        findings, passed = ap.check_folder(self._returned(good, extra_files={"fixture.geo.json": json.dumps(FIXTURE_GEO)}), manifest)
        self.assertTrue(passed, findings)
        # a texture that is not this entity's
        findings, passed = ap.check_folder(self._returned(good, {"other.png": png_bytes(32, 32, 2)}), manifest)
        self.assertFalse(passed)
        self.assertTrue(self._has(findings, "REJECT", "textures/other.png: not one of this entity's textures"), findings)
        # event keyframes (owner 2026-09-12, item 11): REJECTED on a looping clip, fine on a one-shot
        events = json.loads(json.dumps(good))
        events["animations"]["idle"]["sound_effects"] = {"0.5": {"effect": "step"}}
        events["animations"]["walk"]["particle_effects"] = {"0.25": {"effect": "dust"}}
        events["animations"]["attack"]["sound_effects"] = {"0.1": {"effect": "swing"}}
        findings, passed = ap.check_folder(self._returned(events), manifest)
        self.assertFalse(passed)
        self.assertTrue(self._has(findings, "REJECT", "clip 'idle' carries `sound_effects` on a looping clip"), findings)
        self.assertTrue(self._has(findings, "REJECT", "clip 'walk' carries `particle_effects` on a looping clip"), findings)
        self.assertFalse(any("clip 'attack' carries" in m for _, m in findings), findings)
        empty_events = json.loads(json.dumps(good))
        empty_events["animations"]["idle"]["sound_effects"] = {}
        findings, passed = ap.check_folder(self._returned(empty_events), manifest)
        self.assertTrue(passed, findings)
        # idle without walk, walk without idle (owner 2026-09-12, item 12): the switch opens only with both, and the message says so
        for dropped in ("walk", "idle"):
            partial = json.loads(json.dumps(good))
            del partial["animations"][dropped]
            findings, passed = ap.check_folder(self._returned(partial), manifest)
            self.assertFalse(passed)
            self.assertTrue(self._has(findings, "REJECT", f"required clip '{dropped}' is missing (idle and walk open the game's switch only together"), findings)
        # the README and the SPEC carry the two rules in the checker's words
        readme = ap.readme_document(self.repo, {})
        self.assertIn("No event keyframes on loops", readme)
        self.assertIn("is not phase-locked, so its loops may carry event keys", readme)  # the native exemption (owner 2026-09-13)
        self.assertEqual(json.loads(manifest.read_text(encoding="utf-8"))["controller_kind"], "phase_locked")
        self.assertIn("Deliver `idle` and `walk` together", readme)
        for rule in ("an event keyframe (`sound_effects` / `particle_effects`) on a LOOPING clip", "they open the game's switch only TOGETHER"):
            self.assertIn(rule, readme)
            self.assertIn(rule, (self.tmp / "out/entities/fixture/SPEC.md").read_text(encoding="utf-8"))

    def test_check_refuses_a_locked_bone_renamed_reparented_or_deleted(self):
        """The second half of the ruled policy (2026-09-06): a key on a locked bone warns; its name, parent and presence are refused.
        A rename is told from a deletion by the bone's body (pivot, bind rotation, cubes) reappearing under another name."""
        manifest = self._package_fixture()
        good = self.GOOD
        self.assertEqual([b["name"] for b in FIXTURE_GEO["minecraft:geometry"][0]["bones"]], ["root", "tail", "arm", "hand"])  # 3 = the synced part
        summary_ok = "locked bones: 0 of 3 keyed across 0 clip(s) [lock_mode warn; warn-keyed, refuse-structural]"

        def returned_with(geo):
            return self._returned(good, extra_files={"fixture.geo.json": json.dumps(geo)})

        # renamed: `hand` becomes `paw` (its body reappears under the new name) — the locked bone and the new name are named,
        # the order of the shared names is kept, and the summary line counts it
        renamed = json.loads(json.dumps(FIXTURE_GEO))
        renamed["minecraft:geometry"][0]["bones"][3]["name"] = "paw"
        findings, passed = ap.check_folder(returned_with(renamed), manifest)
        self.assertFalse(passed)
        self.assertTrue(self._has(findings, "REJECT", "bone set/order changed (missing ['hand'], added ['paw'], order kept)"), findings)
        self.assertTrue(self._has(findings, "REJECT", "locked bone hand renamed to paw (paw is not this rig's name) "
                                  "— it carries or parents a hitbox part; renaming, re-parenting or deleting a locked bone is refused"), findings)
        self.assertTrue(self._has(findings, "REJECT", "locked bones: 0 of 3 keyed across 0 clip(s); 1 renamed, re-parented or deleted (hand renamed to paw) — REJECTED "
                                  "[lock_mode warn; warn-keyed, refuse-structural] — " + ap.LOCK_POLICY), findings)
        self.assertFalse(any("renamed or deleted (" in msg for _, msg in findings), findings)  # identified: never the hedge
        # re-parented: `hand` moves from `arm` to `root` (names and order intact)
        reparented = json.loads(json.dumps(FIXTURE_GEO))
        reparented["minecraft:geometry"][0]["bones"][3]["parent"] = "root"
        findings, passed = ap.check_folder(returned_with(reparented), manifest)
        self.assertFalse(passed)
        self.assertTrue(self._has(findings, "REJECT", "locked bone hand re-parented (arm -> root) — it carries or parents a hitbox part"), findings)
        self.assertTrue(self._has(findings, "REJECT", "1 renamed, re-parented or deleted (hand re-parented) — REJECTED"), findings)
        # re-parented to the top level (the parent field removed) is a re-parent too — never "following" a rename that never happened
        toplevel = json.loads(json.dumps(FIXTURE_GEO))
        del toplevel["minecraft:geometry"][0]["bones"][3]["parent"]
        findings, passed = ap.check_folder(returned_with(toplevel), manifest)
        self.assertFalse(passed)
        self.assertTrue(self._has(findings, "REJECT", "locked bone hand re-parented (arm -> None) — it carries or parents a hitbox part"), findings)
        self.assertTrue(self._has(findings, "REJECT", "1 renamed, re-parented or deleted (hand re-parented) — REJECTED"), findings)
        # deleted: `hand` is gone and no other bone carries its body
        deleted = json.loads(json.dumps(FIXTURE_GEO))
        del deleted["minecraft:geometry"][0]["bones"][3]
        findings, passed = ap.check_folder(returned_with(deleted), manifest)
        self.assertFalse(passed)
        self.assertTrue(self._has(findings, "REJECT", "locked bone hand deleted (missing from the returned rig; no other bone carries its pivot and cubes) — it carries"), findings)
        self.assertTrue(self._has(findings, "REJECT", "1 renamed, re-parented or deleted (hand deleted) — REJECTED"), findings)
        # a re-parent beside a rename is still named: every bone present in both rigs is compared whatever the set/order verdict
        both = json.loads(json.dumps(FIXTURE_GEO))
        both["minecraft:geometry"][0]["bones"][3]["name"] = "paw"
        both["minecraft:geometry"][0]["bones"][2]["parent"] = "tail"
        findings, passed = ap.check_folder(returned_with(both), manifest)
        self.assertFalse(passed)
        self.assertTrue(self._has(findings, "REJECT", "locked bone arm re-parented (root -> tail)"), findings)
        self.assertTrue(self._has(findings, "REJECT", "locked bone hand renamed to paw"), findings)
        self.assertTrue(self._has(findings, "REJECT", "2 renamed, re-parented or deleted (hand renamed to paw, arm re-parented) — REJECTED"), findings)
        # the children of a renamed bone follow it (their parent field now says the new name): ONE rename, reported once —
        # `arm` becomes `limb` and `hand` (parent arm -> limb) is listed under the rename, not as a re-parent of its own
        limb = json.loads(json.dumps(FIXTURE_GEO))
        limb["minecraft:geometry"][0]["bones"][2]["name"] = "limb"
        limb["minecraft:geometry"][0]["bones"][3]["parent"] = "limb"
        findings, passed = ap.check_folder(returned_with(limb), manifest)
        self.assertFalse(passed)
        self.assertTrue(self._has(findings, "REJECT", "locked bone arm renamed to limb (limb is not this rig's name; its 1 child bone(s) follow it (hand) — one rename, reported once) — it carries"), findings)
        self.assertFalse(any("hand re-parented" in msg for _, msg in findings), findings)  # listed under the rename, not on its own
        self.assertTrue(self._has(findings, "REJECT", "1 renamed, re-parented or deleted (arm renamed to limb) — REJECTED"), findings)
        # a free bone re-parented is a REJECT under the rig rule but is not called locked; the locked summary stays OK
        free = json.loads(json.dumps(FIXTURE_GEO))
        free["minecraft:geometry"][0]["bones"][1]["parent"] = "arm"
        findings, passed = ap.check_folder(returned_with(free), manifest)
        self.assertFalse(passed)
        self.assertTrue(self._has(findings, "REJECT", "bone tail re-parented (root -> arm)"), findings)
        self.assertFalse(any("locked bone tail" in msg for _, msg in findings), findings)
        self.assertTrue(self._has(findings, "OK", summary_ok), findings)
        # a free bone deleted, and one renamed: the rig rule rejects (the set line, the bone named) but nothing is called locked
        # and the locked summary stays OK — the lock test is `name in locked`, not every missing bone
        free_deleted = json.loads(json.dumps(FIXTURE_GEO))
        del free_deleted["minecraft:geometry"][0]["bones"][1]
        findings, passed = ap.check_folder(returned_with(free_deleted), manifest)
        self.assertFalse(passed)
        self.assertTrue(self._has(findings, "REJECT", "bone set/order changed (missing ['tail'], added [], order kept)"), findings)
        self.assertTrue(self._has(findings, "REJECT", "bone tail deleted (missing from the returned rig; no other bone carries its pivot and cubes) — every bone name is fixed (README rule 1)"), findings)
        self.assertFalse(any("locked bone" in msg and not msg.startswith("locked bones:") for _, msg in findings), findings)  # no lock finding; the summary line stays
        self.assertTrue(self._has(findings, "OK", summary_ok), findings)
        free_renamed = json.loads(json.dumps(FIXTURE_GEO))
        free_renamed["minecraft:geometry"][0]["bones"][1]["name"] = "tale"
        findings, passed = ap.check_folder(returned_with(free_renamed), manifest)
        self.assertFalse(passed)
        self.assertTrue(self._has(findings, "REJECT", "bone set/order changed (missing ['tail'], added ['tale'], order kept)"), findings)
        self.assertTrue(self._has(findings, "REJECT", "bone tail renamed to tale (tale is not this rig's name) — every bone name is fixed (README rule 1)"), findings)
        self.assertFalse(any("locked bone" in msg and not msg.startswith("locked bones:") for _, msg in findings), findings)  # no lock finding; the summary line stays
        self.assertTrue(self._has(findings, "OK", summary_ok), findings)
        # keying stays a WARN beside the structural REJECT: the key is allowed, the structure is not
        keyed = json.loads(json.dumps(good))
        keyed["animations"]["idle"]["bones"]["arm"] = {"rotation": {"0.0": [0, 0, 0]}}
        findings, passed = ap.check_folder(self._returned(keyed, extra_files={"fixture.geo.json": json.dumps(renamed)}), manifest)
        self.assertFalse(passed)
        self.assertTrue(self._has(findings, "WARN", "clip 'idle' keys 1 locked bone(s): arm"), findings)
        self.assertTrue(self._has(findings, "REJECT", "locked bones: 1 of 3 keyed across 1 clip(s); 1 renamed, re-parented or deleted (hand renamed to paw) — REJECTED"), findings)

    def test_check_names_duplicates_renames_order_and_manifest_modes(self):
        """A duplicated bone name is refused by name and never compared (a collision rename is identified by the body); the
        set/order line's order verdict is an order test; a manifest without the policy token gets the stale WARN; an unknown
        lock_mode is a REJECT; a missing rotation reads `absent`; a key on an added or renamed bone says which."""
        manifest = self._package_fixture()
        good = self.GOOD

        def returned_with(geo, anim=None):
            return self._returned(anim or good, extra_files={"fixture.geo.json": json.dumps(geo)})

        # a locked bone listed twice — named as a duplicate, never compared against itself ("parented to itself")
        dup = json.loads(json.dumps(FIXTURE_GEO))
        dup["minecraft:geometry"][0]["bones"].append(json.loads(json.dumps(dup["minecraft:geometry"][0]["bones"][3])))
        findings, passed = ap.check_folder(returned_with(dup), manifest)
        self.assertFalse(passed)
        self.assertTrue(self._has(findings, "REJECT", "bone set/order changed (missing [], added [], duplicated ['hand'], order kept)"), findings)
        self.assertTrue(self._has(findings, "REJECT", "duplicate bone name hand (2 times) — a name listed twice is refused by name; neither copy is compared"), findings)
        self.assertFalse(any("re-parented" in msg or "pivot moved" in msg for _, msg in findings), findings)
        # a locked bone renamed onto an existing name (a collision): the rename is identified by its body, the collision named
        collide = json.loads(json.dumps(FIXTURE_GEO))
        collide["minecraft:geometry"][0]["bones"][3]["name"] = "tail"
        findings, passed = ap.check_folder(returned_with(collide), manifest)
        self.assertFalse(passed)
        self.assertTrue(self._has(findings, "REJECT", "bone set/order changed (missing ['hand'], added [], duplicated ['tail'], order kept)"), findings)
        self.assertTrue(self._has(findings, "REJECT", "duplicate bone name tail (2 times)"), findings)
        self.assertTrue(self._has(findings, "REJECT", "locked bone hand renamed to tail (a name the rig already has: the returned rig has 2 bones named tail) "
                                  "— it carries or parents a hitbox part; renaming, re-parenting or deleting a locked bone is refused"), findings)
        self.assertFalse(any("bone tail re-parented" in msg or "bone tail pivot" in msg for _, msg in findings), findings)
        self.assertTrue(self._has(findings, "REJECT", "1 renamed, re-parented or deleted (hand renamed to tail) — REJECTED"), findings)
        # a pure swap of two bones (same set, same bodies) is `order changed`; a rename keeping the order is `order kept` (above)
        swapped = json.loads(json.dumps(FIXTURE_GEO))
        bones = swapped["minecraft:geometry"][0]["bones"]
        bones[1], bones[2] = bones[2], bones[1]
        findings, passed = ap.check_folder(returned_with(swapped), manifest)
        self.assertFalse(passed)
        self.assertTrue(self._has(findings, "REJECT", "bone set/order changed (missing [], added [], order changed)"), findings)
        self.assertEqual([msg for sev, msg in findings if sev == "REJECT" and "set/order" not in msg], [], findings)
        # a bone without a bind rotation reads `absent`, not None
        rotated = json.loads(json.dumps(FIXTURE_GEO))
        rotated["minecraft:geometry"][0]["bones"][0]["rotation"] = [5, 0, 0]
        findings, passed = ap.check_folder(returned_with(rotated), manifest)
        self.assertFalse(passed)
        self.assertTrue(self._has(findings, "REJECT", "bone root bind rotation changed absent -> [5, 0, 0]"), findings)
        # a key on a bone the returned geo ADDED is an added bone; without a geo it is unknown; on a renamed bone it says so
        antenna = json.loads(json.dumps(FIXTURE_GEO))
        antenna["minecraft:geometry"][0]["bones"].append({"name": "antenna", "parent": "root", "pivot": [0, 24, 0]})
        keyed = json.loads(json.dumps(good))
        keyed["animations"]["idle"]["bones"]["antenna"] = {"rotation": {"0.0": [0, 0, 0]}}
        findings, passed = ap.check_folder(returned_with(antenna, keyed), manifest)
        self.assertFalse(passed)
        self.assertTrue(self._has(findings, "REJECT", "clip 'idle' keys added bone 'antenna' (an artist-added bone; not in the shipped rig)"), findings)
        findings, passed = ap.check_folder(self._returned(keyed), manifest)
        self.assertFalse(passed)
        self.assertTrue(self._has(findings, "REJECT", "clip 'idle' keys unknown bone 'antenna' (renamed?)"), findings)
        paw_geo = json.loads(json.dumps(FIXTURE_GEO))
        paw_geo["minecraft:geometry"][0]["bones"][3]["name"] = "paw"
        keyed_paw = json.loads(json.dumps(good))
        keyed_paw["animations"]["idle"]["bones"]["paw"] = {"rotation": {"0.0": [0, 0, 0]}}
        findings, passed = ap.check_folder(returned_with(paw_geo, keyed_paw), manifest)
        self.assertFalse(passed)
        self.assertTrue(self._has(findings, "REJECT", "clip 'idle' keys renamed bone 'paw' (the shipped rig's 'hand', renamed in the returned geo — the rename is refused; key 'hand')"), findings)
        # a manifest WITHOUT the lock_policy key gets the stale-manifest WARN too (`absent`); the ruled policy applies regardless
        bare = json.loads(manifest.read_text(encoding="utf-8"))
        del bare["lock_policy"]
        bare_path = self.tmp / "bare.manifest.json"
        bare_path.write_text(json.dumps(bare), encoding="utf-8")
        findings, passed = ap.check_folder(self._returned(good), bare_path)
        self.assertTrue(passed, findings)
        self.assertTrue(self._has(findings, "WARN", "bare.manifest.json: lock_policy is absent, not `warn-keyed, refuse-structural` — the package predates the 2026-09-06 ruling; "
                                  "regenerate it (the checker applies the ruled policy regardless)"), findings)
        self.assertTrue(self._has(findings, "OK", "[lock_mode warn; warn-keyed, refuse-structural] — " + ap.LOCK_POLICY), findings)
        # a lock_mode that is neither warn nor reject — from the manifest or the override — is a REJECT, never a silent warn
        odd = json.loads(manifest.read_text(encoding="utf-8"))
        odd["lock_mode"] = "strict"
        odd_path = self.tmp / "odd.manifest.json"
        odd_path.write_text(json.dumps(odd), encoding="utf-8")
        findings, passed = ap.check_folder(self._returned(good), odd_path)
        self.assertFalse(passed)
        self.assertTrue(self._has(findings, "REJECT", "lock_mode 'strict' is not warn or reject (odd.manifest.json's lock_mode; `warn` is the ruled policy"), findings)
        self.assertTrue(self._has(findings, "OK", "[lock_mode strict; warn-keyed, refuse-structural]"), findings)
        findings, passed = ap.check_folder(self._returned(good), manifest, lock_mode_override="loud")
        self.assertFalse(passed)
        self.assertTrue(self._has(findings, "REJECT", "lock_mode 'loud' is not warn or reject (the --lock-mode override;"), findings)
        import contextlib
        import io
        with self.assertRaises(SystemExit), contextlib.redirect_stderr(io.StringIO()):  # the CLI's --lock-mode has choices
            ap.main(["check", str(self._returned(good)), "--lock-mode", "strict"])

    def test_check_on_a_native_species(self):
        out = self.tmp / "out_native"
        ap.build_package(self.repo, out, ["native"])
        manifest = out / "entities/native/spec.manifest.json"
        # the shipped file itself: PASS with the locked-bone warnings (26-of-27 on the real boss, 1-of-3 here)
        findings, passed = ap.check_folder(self._returned(NATIVE_ANIM, anim_name="native.animation.json"), manifest)
        self.assertTrue(passed, findings)
        self.assertTrue(self._has(findings, "WARN", "clip 'stance' keys 1 locked bone(s): head"), findings)
        self.assertTrue(self._has(findings, "WARN", "locked bones: 1 of 3 keyed across 2 clip(s) [lock_mode warn; warn-keyed, refuse-structural] — " + ap.LOCK_POLICY), findings)
        self.assertFalse(any("PROVISIONAL" in msg for _, msg in findings), findings)
        # owner 2026-09-13 (addendum item 26 (6)): a native-controller species is exempt from the event-key rule on loops
        # - its own GeckoLib controllers are not phase-locked - keyed on the manifest's controller kind, and the checker's
        # line says why; the Tier-2 fixture (phase-locked) still rejects (test_check_rejects_every_rule_it_claims)
        self.assertEqual(json.loads(manifest.read_text(encoding="utf-8"))["controller_kind"], "native")
        keyed = json.loads(json.dumps(NATIVE_ANIM))
        keyed["animations"]["idle"]["sound_effects"] = {"0.5": {"effect": "hum"}}
        findings, passed = ap.check_folder(self._returned(keyed, anim_name="native.animation.json"), manifest)
        self.assertTrue(passed, findings)
        self.assertTrue(self._has(findings, "NOTE", "clip 'idle' carries `sound_effects` on a looping clip — allowed for this creature: "
                                                    "native controllers are not phase-locked; event keys fire per loop"), findings)
        self.assertFalse(any(sev == "REJECT" for sev, _ in findings), findings)
        # a clip outside the native set — including idle_alt_N — is rejected
        extra = json.loads(json.dumps(NATIVE_ANIM))
        extra["animations"]["fly"] = {"loop": True, "animation_length": 1.0, "bones": {"tail": {"rotation": {"0.0": [0, 0, 0]}}}}
        extra["animations"]["idle_alt_1"] = {"loop": False, "animation_length": 1.0, "bones": {"tail": {"rotation": {"0.0": [0, 0, 0]}}}}
        findings, passed = ap.check_folder(self._returned(extra, anim_name="native.animation.json"), manifest)
        self.assertFalse(passed)
        self.assertTrue(self._has(findings, "REJECT", "clip 'fly' is not in the SPEC"), findings)
        self.assertTrue(self._has(findings, "REJECT", "clip 'idle_alt_1' is not in the SPEC (renamed or added); this creature's native clip set accepts no idle_alt_N"), findings)
        # the reject mode (kept for the day the evaluator lands) would fail the shipped file itself
        findings, passed = ap.check_folder(self._returned(NATIVE_ANIM, anim_name="native.animation.json"), manifest, lock_mode_override="reject")
        self.assertFalse(passed)
        self.assertTrue(self._has(findings, "REJECT", "the reject mode, kept for the day the server-side hitbox evaluator lands"), findings)

    # --- the third-set tooling commit (owner 2026-09-13, third set, item 28 (2)-(6)) ---------------------------------

    def test_rulings_resolved_no_markers_in_readme_and_sheets(self):
        """Item 28 (3): a generated README and every generated SPEC / manifest carry neither `PROVISIONAL` nor `open question`;
        each ruled point cites its 2026-09-06 ruling; the lock reject mode is named as THE one open item (README rule 6 and
        SPEC §7 carry LOCK_REJECT_MODE, which says so)."""
        out = self.tmp / "out_resolved"
        ap.build_package(self.repo, out, ["fixture", "native"], with_roundtrip=False)
        texts = {name: (out / name).read_text(encoding="utf-8")
                 for name in ("README_FIRST.md", "entities/fixture/SPEC.md", "entities/native/SPEC.md", "INVENTORY.csv")}
        texts["fixture manifest"] = json.dumps(json.loads((out / "entities/fixture/spec.manifest.json").read_text(encoding="utf-8")))
        texts["native manifest"] = json.dumps(json.loads((out / "entities/native/spec.manifest.json").read_text(encoding="utf-8")))
        for name, text in texts.items():
            self.assertNotIn("PROVISIONAL", text, name)
            self.assertNotIn("open question", text.lower(), name)
        self.assertIn("the one open item", ap.LOCK_REJECT_MODE)
        self.assertNotIn("PROVISIONAL", ap.LOCK_REJECT_MODE)
        self.assertIn(ap.LOCK_REJECT_MODE, texts["README_FIRST.md"])
        self.assertIn(ap.LOCK_REJECT_MODE, texts["entities/fixture/SPEC.md"].split("## 7. Hitbox bones")[1].split("## 8. Textures")[0])
        self.assertNotIn("## PROVISIONAL items", texts["README_FIRST.md"])
        # the rulings are cited where they apply: the sheets' trigger rows, clip rows and §4.1; the README's rules 5 and 7
        spec = texts["entities/fixture/SPEC.md"]
        for cite in ("ruled 2026-09-06, Q3 (a)", "ruled 2026-09-06, Q4 (a)", "ruled 2026-09-06, Q5 (a)", "ruled 2026-09-06, Q6 (a)",
                     "ruled 2026-09-06, Q11 (a)", "ruled 2026-09-06, Q14 (a)", "ruled 2026-09-06, Q15 (a)"):
            self.assertIn(cite, spec, cite)
        self.assertIn("ruled 2026-09-06, Q16 (a)", texts["entities/native/SPEC.md"])
        # Q12 (calm_idle until the synched byte) is cited on the rows of a creature WITHOUT a held flag: the drive rows and the
        # calm_idle clip row under a no-flag inventory (the fixture itself holds a STATE flag, so its sheet has a real pair)
        fx_species = self.repo.get("fixture")
        inv = ap.build_trigger_inventory(fx_species, self.repo)
        no_flag = dict(inv, attacking={"present": False, "verdict": "NONE", "sites": [], "reason": "", "facts": {}})
        drives = {d["clip"]: d for d in ap.contract_drives(fx_species, no_flag)}
        self.assertIn("ruled 2026-09-06, Q12 (a): aggro_idle waits for that byte", drives["aggro_idle / calm_idle"]["verdict"])
        self.assertIn("ruled 2026-09-06, Q11 (a)", drives["attack"]["verdict"])
        self.assertIn("ruled 2026-09-06, Q3 (a)", drives["death"]["verdict"])
        self.assertIn("ruled 2026-09-06, Q5 (a)", drives["idle_alt_N"]["verdict"])
        covered = {c["name"]: c for c in ap.clip_rows(fx_species, no_flag, fx_species.animation, ap.frequency_groups(fx_species))}
        self.assertIn("ruled 2026-09-06, Q12 (a)", covered["calm_idle"]["trigger"])
        for d in drives.values():
            self.assertNotIn("PROVISIONAL", d["verdict"])
            self.assertNotIn("open question", d["verdict"])
        self.assertIn("ruled 2026-09-06, Q9 (a) and Q10", spec)  # the density statement (Tier 2)
        self.assertIn("a mechanical reading; the owner confirms it against the sites", spec)  # the classifier's caveat, in plain words
        self.assertIn("Q14 (a)", texts["README_FIRST.md"])
        self.assertIn("Q15 (a)", texts["README_FIRST.md"])
        # the manifest's per-clip marker flag is gone; the verdict travels instead (the README's priority table reads it)
        fx = json.loads(texts["fixture manifest"])
        self.assertTrue(all("provisional" not in c for c in fx["clips"]))
        self.assertEqual({c["name"]: c["verdict"] for c in fx["clips"]}["walk"], "improve")

    def test_readme_rule_5_split_and_priority_table_lists_the_packaged_folders_only(self):
        """Item 28 (3): README rule 5 carries both controller kinds; item 28 (4): the priority table lists the folders of THIS
        package only, with the follow-on line, and a native creature's row is its pilot scope (the improve / author clips to
        deliver, the `leave` clips later, returned as shipped) with the effort re-estimated from the verdicts."""
        manifests = {}
        for reg in ("fixture", "native"):
            s = self.repo.get(reg)
            inv = ap.build_trigger_inventory(s, self.repo)
            _, manifests[reg] = ap.spec_document(s, self.repo, self.catalog, inv)
        readme = ap.readme_document(self.repo, manifests, packaged=["fixture"])
        rule5 = [line for line in readme.splitlines() if line.startswith("5. **Loop length by controller kind.**")]
        self.assertEqual(len(rule5), 1, readme)
        self.assertIn("A phase-locked creature (every sheet but the Queen's today): author every loop at 1.0 s", rule5[0])
        self.assertIn("the sheet's tempo table gives the rate (contract §9; ruled 2026-09-06, Q14 (a))", rule5[0])
        self.assertIn("A native creature (the Queen): keep each clip's shipped length — her controllers play the clip as authored", rule5[0])
        self.assertNotIn("Keep each clip's length near the period the sheet states", readme)
        # the packaged set only: the fixture's row, not the native boss's, and the follow-on line
        table = readme.split("## Priority order and effort")[1]
        self.assertIn("| fixture |", table)
        self.assertNotIn("| native |", table)
        self.assertIn("More folders follow as creatures land through the seam; this package carries 1.", table)
        # the native creature packaged alone: its row is the pilot scope, the effort re-estimated from the verdicts
        # (NATIVE_SEED: idle / stance / stomp improve, death leave -> 8 h + 0.15 x 4 bones + 1.5 x 3 = 13.1 h)
        readme_native = ap.readme_document(self.repo, manifests, packaged=["native"])
        table_native = readme_native.split("## Priority order and effort")[1]
        self.assertIn("| native |", table_native)
        self.assertNotIn("| fixture |", table_native)
        self.assertIn("`idle`, `stance`, `stomp` — its other 1 clip: later, returned as shipped | 13.1 h |", table_native)
        self.assertEqual(manifests["native"]["effort_hours"], 13.1)
        self.assertIn("this package carries 1.", table_native)
        # no packaged set given: the manifests' keys (the `readme` subcommand's whole landed set)
        both = ap.readme_document(self.repo, manifests).split("## Priority order and effort")[1]
        self.assertIn("| fixture |", both)
        self.assertIn("| native |", both)
        self.assertIn("this package carries 2.", both)

    def test_check_length_warn_follows_the_controller_kind(self):
        """Item 28 (3): the checker's length WARN follows rule 5's split - a phase-locked creature's loops against 1.0 s, a native
        creature's clips against their shipped length, a one-shot against its row - and carries no marker."""
        manifest = self._package_fixture()
        good = self.GOOD
        stretched = json.loads(json.dumps(good))
        stretched["animations"]["idle"]["animation_length"] = 3.0  # a loop far from 1.0 s on the phase-locked fixture
        stretched["animations"]["attack"]["animation_length"] = 2.0  # a one-shot far from its row's 0.5 s
        findings, passed = ap.check_folder(self._returned(stretched), manifest)
        self.assertTrue(passed, findings)
        self.assertTrue(self._has(findings, "WARN", "clip 'idle' length 3 s is far from the stated 1 s (rule 5, a phase-locked creature: author every loop at 1.0 s (ruled 2026-09-06, Q14 (a))"), findings)
        self.assertTrue(self._has(findings, "WARN", "clip 'attack' length 2 s is far from the stated 0.5 s (the length the sheet's row states for this one-shot)"), findings)
        self.assertFalse(any("PROVISIONAL" in m or "open question" in m for _, m in findings), findings)
        out = self.tmp / "out_native_length"
        ap.build_package(self.repo, out, ["native"], with_roundtrip=False)
        native_manifest = out / "entities/native/spec.manifest.json"
        self.assertEqual(json.loads(native_manifest.read_text(encoding="utf-8"))["controller_kind"], "native")
        keyed = json.loads(json.dumps(NATIVE_ANIM))
        keyed["animations"]["idle"]["animation_length"] = 6.0  # the shipped idle is 2.0 s
        findings, passed = ap.check_folder(self._returned(keyed, anim_name="native.animation.json"), native_manifest)
        self.assertTrue(passed, findings)
        self.assertTrue(self._has(findings, "WARN", "clip 'idle' length 6 s is far from the stated 2 s (rule 5, a native creature: keep each clip's shipped length"), findings)
        self.assertFalse(any("PROVISIONAL" in m or "open question" in m for _, m in findings), findings)

    def test_reference_clip_span_rule_wording(self):
        """Item 28 (5): SPEC §4.3 says the span rule in words per index rule - a period multiple (k > 1) names the multiple and
        the 5-degree test; past the cap it states the seam explicitly; the manifest carries the period and k."""
        fx = self.repo.get("fixture")
        inv = ap.build_trigger_inventory(fx, self.repo)
        saved = self.repo.reference_clips
        row = dict(saved["fixture"])
        try:
            self.repo.reference_clips = {"fixture": dict(row, rule="period_multiple", period_multiple_k=4, period_ticks=15.7079632679,
                                                         span_ticks=62.8318530718, animation_length_seconds=3.1415926536, keys_per_bone=64)}
            md, m = ap.spec_document(fx, self.repo, self.catalog, inv)
            self.assertIn("4 periods of its slowest rhythm (15.708 ticks each) — 62.832 ticks (3.142 s): the smallest multiple at which EVERY rhythm "
                          "returns within 5 degrees of its start (the rule caps this search at 6 s), so the last key closes the loop", md)
            self.assertEqual(m["reference_clip"]["period_multiple_k"], 4)
            self.assertAlmostEqual(m["reference_clip"]["period_ticks"], 15.7079632679)
            self.repo.reference_clips = {"fixture": dict(row, rule="two_seconds_past_cap", period_ticks=571.1986673842, span_ticks=40.0,
                                                         animation_length_seconds=2.0, keys_per_bone=41, seam_delta_degrees=17.142012628)}
            row.pop("period_multiple_k", None)
            md, m = ap.spec_document(fx, self.repo, self.catalog, inv)
            self.assertIn("two seconds (40 ticks): this motion does not close within 6 s (its slowest rhythm is 571.199 ticks, and no multiple of it under 6 s "
                          "brings every rhythm back within 5 degrees) — a two-second window, not a loop; the closing key differs from the first by 17.142 degrees", md)
            self.assertEqual(m["reference_clip"]["rule"], "two_seconds_past_cap")
            self.repo.reference_clips = {"fixture": dict(row, rule="two_seconds_no_period", span_ticks=40.0, animation_length_seconds=2.0,
                                                         keys_per_bone=41, seam_delta_degrees=174.1531103624)}
            md, _ = ap.spec_document(fx, self.repo, self.catalog, inv)
            self.assertIn("two seconds (40 ticks): this motion has no natural period — a two-second window, not a loop; the closing key differs from the first by 174.1531 degrees", md)
        finally:
            self.repo.reference_clips = saved

    def test_package_writes_repository_artist_handoff(self):
        # owner 2026-09-13, second set, item 10: since the mirror drop landed the repository's artist_handoff/ is an
        # ordinary output (the pilot pair's package lives there); the 2026-09-05 refusal is gone.
        ap.build_package(self.repo, self.root / "artist_handoff", ["fixture"], with_roundtrip=False)
        self.assertTrue((self.root / "artist_handoff" / "entities" / "fixture" / "SPEC.md").exists())

    def test_inventory_rows(self):
        header, rows = ap.inventory_rows(self.repo, self.catalog)
        by = {r[0]: dict(zip(header, r)) for r in rows}
        self.assertEqual(len(rows), 8)
        self.assertEqual(by["fixture"]["status"], "landed candidate")
        self.assertEqual(by["fixture"]["bones"], 4)
        self.assertEqual(by["fixture"]["locked_bones"], 3)
        self.assertEqual(by["fixture"]["clips_shipped"], 3)
        self.assertEqual(by["fixture"]["expected_scale"], 1)
        self.assertEqual(by["fixture"]["artist_tier"], "Tier 2")
        # effort_hours is filled with the generator's estimate: 4 h + 0.2 x 4 bones + 1 h x the clips to author or improve
        self.assertNotEqual(by["fixture"]["effort_hours"], "")
        self.assertIn("generator:", by["fixture"]["effort_source"])
        # 4 + 0.2 x 4 bones + 1 h x 10: idle, idle_pump, walk, walk_pump, swim, aggro_idle, calm_idle (a real pair: STATE flag), attack, hurt, death
        self.assertEqual(by["fixture"]["effort_hours"], "14.8")
        self.assertEqual(by["other"]["status"], "classic only")
        self.assertEqual(by["other"]["tier"], 3)
        self.assertEqual(by["other"]["effort_hours"], "")
        self.assertEqual(by["dart"]["status"], "excluded")
        self.assertIn("projectile", by["dart"]["status_note"])
        self.assertEqual(by["moo"]["tier"], 0)
        self.assertEqual(by["native"]["artist_tier"], "Tier 1 (boss; the design's 'done' row)")
        self.assertEqual(by["fixture_head"]["status"], "classic only")


if __name__ == "__main__":
    unittest.main(verbosity=2)
