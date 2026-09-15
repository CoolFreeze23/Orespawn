#!/usr/bin/env python3
"""Convert compiled LayerDefinition dumps to GeckoLib Bedrock geometry.

The input is emitted by G1ModelProbe from executed, compiled Java. This script
never reads model source and never evaluates animation formulas. Reference clip
keys are baked only from compiled setupAnim outputs; Java independently runs the
actual candidate GeckoLib custom hook and Python later compares that runtime
output with independently executed vanilla setupAnim output. The retained
Beaver clip is reference-only and is never runtime-acceptance input.
"""

from __future__ import annotations

import argparse
import copy
import hashlib
import json
import math
import struct
from pathlib import Path
from typing import Any, Iterable


ALL_FACES = {"down", "up", "west", "north", "east", "south"}
# G2 root-order contract: the geo description key carrying the classic draw order as geo bone
# names in pre-order; GeckoLib 4.8.4's MinecraftGeometry / ModelProperties deserializers ignore
# it, and the shipped OreSpawnGeoReplacementModel sorts every bake into it (DrawOrder.apply).
DRAW_ORDER_KEY = "orespawn:bone_draw_order"
# ENT-S-146: the geo description key carrying the classic WITHIN-CUBE face order (bone -> one array
# per cube of GeckoLib direction names in draw order); written only for a rig whose manifest entry
# declares `cube_face_order: "classic"` (a translucent rig, where blending makes the order visible),
# applied by the shipped OreSpawnGeoReplacementModel and the harness through FaceOrder.apply.
FACE_ORDER_KEY = "orespawn:cube_face_order"
# THE FRAME (TEST-015, from the bytecode of NeoForge 21.1.223 and GeckoLib 4.8.4): the classic renderer draws a
# ModelPart-space point p at M p in the entity frame, M = scale(-1, -1, 1) . translate(0, -1.501, 0)
# (LivingEntityRenderer.render: setupRotations 390, the flip 395-400, the renderer's scale hook 408, the lift
# 414-417, renderToBuffer 621). GeckoLib's baker negates a Bedrock cube's origin.x + size.x
# (BakedModelFactory$Builtin.constructCube 98-139) and every pivot's x (constructCube 157-167, constructBone
# 95-116) and flips nothing else, so a Bedrock cube spanning [x, x + sx] bakes to [-(x + sx), -x]: the baker IS the
# x mirror the classic's flip applies. A converted rig therefore sits in the BEDROCK CONVENTION the Queen's
# Blockbench-authored rig uses - the same x as the ModelPart, y up about the 24-unit datum, z the same:
# cube origin (ax, 24 - (ay + sy), az), pivot (px, 24 - py, pz) [before this landing: x negated on both] and
# the bake of a ModelPart-space point is B p = (-x, 1.5 - y, z) blocks: INTERNAL SPACE IS CLASSIC SPACE REFLECTED IN
# X AND Y (before: in Y only - the converter's own negation cancelled the baker's, and every seam rig drew as the
# classic's left-right mirror in the entity frame). Every derived rule below follows from that reflection, S =
# diag(-1, -1, 1) = R_z(180): a rotation conjugated through S keeps its Z sense and reverses X and Y (internal
# rotation = (-xRot, -yRot, +zRot) of the classic; the baker negates JSON X and Y at load, constructBone 61-92, so
# the JSON rotation is +classic degrees on all three axes: json_rotation / json_rotation_delta), a classic pivot move
# (dx, dy, dz) is the internal offset (-dx, -dy, dz), and the six faces land where S sends their normals.
# NeoForge 21.1.223 ModelPart.Cube.<init> fills its polygon array DOWN, UP, WEST, NORTH, EAST, SOUTH
# (offsets 365-785: DOWN 365, UP 436, WEST 507, NORTH 578, EAST 649, SOUTH 720, each slot ending in its
# Polygon.<init> and aastore) and compile emits them in that order; Polygon.<init> takes the normal from the
# direction's step and, for a mirrored cube, negates its X (the WEST polygon then faces +X). GeckoLib
# 4.8.4 BakedModelFactory.buildQuads builds WEST, EAST, NORTH, SOUTH, UP, DOWN (offsets 20-130) and
# stamps each quad with the direction of its geometric side in internal space (VertexSet.quadWest is the
# min-x side, quadNorth the min-z side, quadUp the max-y side); internal space is classic space reflected
# in X and Y, so a classic normal (x, y, z) is the quad GeckoLib labels by the direction whose step is
# (-x, -y, z): the classic WEST slot is GeckoLib's east quad, DOWN (the classic's y-down top) its up quad.
CLASSIC_FACE_ORDER = ("down", "up", "west", "north", "east", "south")
CLASSIC_FACE_NORMAL = {
    "down": (0.0, -1.0, 0.0), "up": (0.0, 1.0, 0.0), "west": (-1.0, 0.0, 0.0),
    "north": (0.0, 0.0, -1.0), "east": (1.0, 0.0, 0.0), "south": (0.0, 0.0, 1.0),
}
GECKOLIB_LABEL_BY_CLASSIC_NORMAL = {
    (-1.0, 0.0, 0.0): "east", (1.0, 0.0, 0.0): "west", (0.0, 0.0, -1.0): "north",
    (0.0, 0.0, 1.0): "south", (0.0, -1.0, 0.0): "up", (0.0, 1.0, 0.0): "down",
}


def entity_frame_normal(normal: tuple[float, float, float]) -> tuple[float, float, float]:
    """A classic (ModelPart-space) normal as the entity frame carries it: S n = (-x, -y, z) - the classic chain's
    scale(-1, -1, 1) (PoseStack.scale scales the normal matrix by the signs of a uniform-magnitude scale) and the
    bake alike; the probe captures both sides in this frame."""
    return (-normal[0] + 0.0, -normal[1] + 0.0, normal[2] + 0.0)


def load_json(path: Path) -> dict[str, Any]:
    with path.open("r", encoding="utf-8") as handle:
        return json.load(handle)


def json_bytes(value: Any) -> bytes:
    return (json.dumps(value, indent=2, ensure_ascii=False) + "\n").encode("utf-8")


def write_json(path: Path, value: Any) -> bytes:
    data = json_bytes(value)
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_bytes(data)
    return data


def sha256(data: bytes) -> str:
    return hashlib.sha256(data).hexdigest()


def clean_number(value: float, digits: int = 10) -> float | int:
    rounded = round(float(value), digits)
    if abs(rounded) < 10 ** (-digits):
        return 0
    if rounded.is_integer():
        return int(rounded)
    return rounded


def clean_vector(values: Iterable[float], digits: int = 10) -> list[float | int]:
    return [clean_number(value, digits) for value in values]


def nonzero(values: Iterable[float], epsilon: float = 1.0e-10) -> bool:
    return any(abs(float(value)) > epsilon for value in values)


def iter_parts(root: dict[str, Any]) -> Iterable[tuple[dict[str, Any], str | None]]:
    def visit(part: dict[str, Any], parent: str | None) -> Iterable[tuple[dict[str, Any], str | None]]:
        for child in part["children"]:
            yield child, parent
            yield from visit(child, child["name"])

    yield from visit(root, None)


def modelpart_face_uv(cube: dict[str, Any]) -> dict[str, dict[str, list[float | int]]]:
    """Translate Mojang box UVs into deterministic GeckoLib per-face UVs.

    THE RULE, DERIVED FROM THE BYTECODE FOR THE ENTITY FRAME (TEST-015; internal space = classic space
    reflected in X and Y, the module comment). ModelPart.Cube.<init> (21.1.223) gives every polygon its
    four vertices and one island, and Polygon.<init> (offsets 41 / 64 / 88 / 113) remaps them
    ``[0] (u2, v1)  [1] (u1, v1)  [2] (u1, v2)  [3] (u2, v2)`` before a mirrored cube's array is
    reversed (141-167; the cube's x extents were swapped first, 126-135, so a mirrored WEST polygon sits
    at +x with its island's u running the other way). GeckoLib's GeoQuad.build (4.8.4, offsets 34-48)
    SWAPS the supplied u endpoints for a quad whose native mirror is false, then (75-144) gives
    ``[0] (u + uSize, v)  [1] (u, v)  [2] (u, v + vSize)  [3] (u + uSize, v + vSize)`` to the corners
    VertexSet hands it (quadNorth: min-x-top, max-x-top, max-x-bottom, min-x-bottom; quadEast: min-z-top,
    max-z-top, ...; quadWest: max-z-top, min-z-top, ...; quadUp: min-x-max-z, max-x-max-z, max-x-min-z, ...;
    quadDown: min-x-min-z, max-x-min-z, max-x-max-z, ...). Reading the classic corners through the
    reflection (a ModelPart min x is an internal MAX x; a ModelPart min y - Direction.DOWN's slot - the
    internal TOP): the classic WEST island (at ``u``) lands on GeckoLib's EAST quad and vice versa, the
    DOWN island on its UP quad, and every island keeps its own u direction (``uv_size`` positive in u)
    for a plain cube - a MIRRORED cube, whose polygons carry their islands u-reversed, writes the u
    origin at the island's far edge with a negative width. Before this landing the converter negated x
    itself, so the same derivation gave the opposite u signs and the islands on the same-named quads.

    GeckoLib's native mirror moves its UP / DOWN vertices without moving their normals, so the source
    mirror semantics are baked into six explicit face rectangles instead and the native flag stays
    false; ``modelpart_mirror`` remains in the cube as provenance. The surface leg (position / normal /
    UV tuples, 1e-7) and the face-order leg are what prove the rule on every rig.
    """
    u, v = (float(value) for value in cube["uv"])
    size_x, size_y, size_z = (float(value) for value in cube["size"])
    mirrored = bool(cube["mirror"])

    def face(min_u: float, min_v: float, width: float, height: float,
             flip_v: bool = False) -> dict[str, list[float | int]]:
        origin_u = min_u + width if mirrored else min_u
        signed_width = -width if mirrored else width
        origin_v = min_v + height if flip_v else min_v
        signed_height = -height if flip_v else height
        return {
            "uv": clean_vector([origin_u, origin_v]),
            "uv_size": clean_vector([signed_width, signed_height]),
        }

    # the island each ModelPart x face carries (the mirror flag swaps them, Cube.<init> 126-135)
    negative_x_u = u + size_z + size_x if mirrored else u
    positive_x_u = u if mirrored else u + size_z + size_x
    return {
        # the ModelPart's +x face is the entity frame's -x side (GeckoLib's west quad), and vice versa
        "west": face(positive_x_u, v + size_z, size_z, size_y),
        "east": face(negative_x_u, v + size_z, size_z, size_y),
        "north": face(u + size_z, v + size_z, size_x, size_y),
        "south": face(u + 2.0 * size_z + size_x, v + size_z, size_x, size_y),
        # the ModelPart DOWN slot (its y-down top) is the entity frame's top: GeckoLib's up quad
        "up": face(u + size_z, v, size_x, size_z),
        "down": face(u + size_z + size_x, v, size_x, size_z, flip_v=True),
    }


def convert_cube(cube: dict[str, Any], absolute_pivot: list[float]) -> dict[str, Any]:
    texture_scale = [float(value) for value in cube["texture_scale"]]
    if any(abs(value - 1.0) > 1.0e-7 for value in texture_scale):
        raise ValueError(f"texture_scale {texture_scale} is not representable by Bedrock box UV")

    visible_faces = set(cube["visible_faces"])
    if visible_faces != ALL_FACES:
        raise ValueError(
            f"partial face set {sorted(visible_faces)} requires per-face UV conversion"
        )

    deformation = [float(value) for value in cube["deformation"]]
    if max(deformation) - min(deformation) > 1.0e-7:
        raise ValueError(
            f"anisotropic deformation {deformation} is not representable by GeckoLib inflate"
        )

    local_origin = [float(value) for value in cube["origin"]]
    size = [float(value) for value in cube["size"]]
    absolute_origin = [
        absolute_pivot[index] + local_origin[index]
        for index in range(3)
    ]

    # THE BEDROCK CONVENTION (TEST-015, the module comment): (x, 24 - y, z) of the ModelPart corners - the
    # cube keeps its ModelPart x and its y is measured up from the 24-unit datum, exactly as the Queen's
    # Blockbench-authored cubes are laid out. GeckoLib's pinned baker negates (origin.x + size.x)
    # (constructCube 98-139), so the bake spans [-(ax + sx), -ax]: the x mirror the classic chain's
    # scale(-1, -1, 1) applies, no longer cancelled by a negation here.
    geo_origin = [
        absolute_origin[0],
        24.0 - (absolute_origin[1] + size[1]),
        absolute_origin[2],
    ]
    converted: dict[str, Any] = {
        "origin": clean_vector(geo_origin),
        "size": clean_vector(size),
        "uv": modelpart_face_uv(cube),
    }
    if abs(deformation[0]) > 1.0e-10:
        converted["inflate"] = clean_number(deformation[0])
    if cube["mirror"]:
        # GeckoLib's native mirror changes Y-face vertex placement without the
        # corresponding normal change. The explicit UVs above carry the source
        # semantics; this annotation preserves the compiled source flag.
        converted["modelpart_mirror"] = True
    return converted


AXIS_INDEX = {"x": 0, "y": 1, "z": 2}


def float32(value: float) -> float:
    """Round to IEEE binary32, the arithmetic the classic draw loops run in."""
    return struct.unpack("<f", struct.pack("<f", float(value)))[0]


def json_rotation(classic_radians: list[float]) -> list[float | int]:
    """ModelPart (x, y, z) radians -> Bedrock degrees: +classic degrees on all three axes (THE SIGN RULE,
    re-derived for the entity frame, TEST-015 - the one place it lives; ``json_rotation_delta`` is the same
    rule on a delta, quoted by tools/keyframe_clip.py, KeyframeLeg and ReferenceClipSampler). Internal
    space is classic space reflected in X and Y (S = diag(-1, -1, 1) = R_z(180)); conjugating the classic
    ZYX triple through S keeps the axis order and gives internal (-xRot, -yRot, +zRot); GeckoLib's baker
    negates the JSON X and Y degrees at load (BakedModelFactory$Builtin.constructBone 61-92: updateRotation
    of -toRadians(x), -toRadians(y), +toRadians(z)), so the JSON that bakes to that internal triple is the
    classic triple itself. Before this landing (internal = classic reflected in Y only) the rule was
    (+x, -y, -z)."""
    return clean_vector(
        [
            math.degrees(classic_radians[0]),
            math.degrees(classic_radians[1]),
            math.degrees(classic_radians[2]),
        ]
    )


def render_instance_angles(part_name: str, declaration: dict[str, Any]) -> tuple[float, list[float]]:
    """The per-draw step angles of a render-instance loop, in the loop's own arithmetic.

    ``step_radians`` (or ``step_degrees``) is the classic model's literal;
    ``step_arithmetic`` says how the loop reaches draw k from it:
    ``float32_accumulate`` (``angle += STEP`` per draw, Rotator),
    ``float32_multiply`` (``k * STEP`` per draw, PurplePower) or ``exact``.
    """
    count = int(declaration["count"])
    if count < 2:
        raise ValueError(f"render_instances.{part_name}.count must be at least 2")
    if "step_radians" in declaration:
        step = float(declaration["step_radians"])
    elif "step_degrees" in declaration:
        step = math.radians(float(declaration["step_degrees"]))
    else:
        raise ValueError(f"render_instances.{part_name} declares neither step_radians nor step_degrees")
    arithmetic = declaration.get("step_arithmetic", "exact")
    angles: list[float] = []
    if arithmetic == "float32_accumulate":
        step32 = float32(step)
        angle = 0.0
        for _ in range(count):
            angles.append(angle)
            angle = float32(angle + step32)
    elif arithmetic == "float32_multiply":
        step32 = float32(step)
        angles = [float32(k * step32) for k in range(count)]
    elif arithmetic == "exact":
        angles = [k * step for k in range(count)]
    else:
        raise ValueError(f"render_instances.{part_name}.step_arithmetic {arithmetic!r} is not supported")
    return step, angles


def resolve_axes(values: Any, defaults: list[float], where: str) -> list[float]:
    """A declared 3-vector whose ``null`` entries keep the part's own value on that axis."""
    if not isinstance(values, list) or len(values) != 3:
        raise ValueError(f"{where} must be a list of three numbers or nulls")
    out: list[float] = []
    for index, value in enumerate(values):
        if value is None:
            out.append(float(defaults[index]))
        elif isinstance(value, (int, float)) and not isinstance(value, bool):
            out.append(float(value))
        else:
            raise ValueError(f"{where}[{index}] must be a number or null")
    return out


def explicit_instance_transforms(part_name: str, declaration: dict[str, Any], absolute_pivot: list[float],
                                 initial_rotation: list[float]) -> list[dict[str, Any]]:
    """The per-draw bind transforms of an explicit render-instance list, in classic ModelPart coordinates.

    ``instances`` lists exactly ``count`` draws in the classic draw order; draw k carries ``pivot``
    (the rotation point the classic sets for that draw at bind) and an optional ``rotation``
    (radians; a ``null`` axis keeps the part's own initial rotation), plus a ``note``.
    """
    count = int(declaration["count"])
    if count < 2:
        raise ValueError(f"render_instances.{part_name}.count must be at least 2")
    for key in ("axis", "step_radians", "step_degrees", "step_arithmetic", "group_chain"):
        if key in declaration:
            raise ValueError(f"render_instances.{part_name}.{key} does not apply to step_scope explicit")
    instances = declaration.get("instances")
    if not isinstance(instances, list) or len(instances) != count:
        raise ValueError(f"render_instances.{part_name}.instances must list exactly count = {count} draws")
    out: list[dict[str, Any]] = []
    for k, instance in enumerate(instances):
        where = f"render_instances.{part_name}.instances[{k}]"
        if not isinstance(instance, dict) or "pivot" not in instance:
            raise ValueError(f"{where} needs a pivot (classic ModelPart rotation point at bind)")
        entry = {
            "draw_index": k,
            "pivot": resolve_axes(instance["pivot"], absolute_pivot, f"{where}.pivot"),
            "rotation": resolve_axes(instance.get("rotation", [None, None, None]), initial_rotation, f"{where}.rotation"),
        }
        if "note" in instance:
            entry["note"] = instance["note"]
        out.append(entry)
    return out


def expand_explicit_instances(name: str, part: dict[str, Any], absolute_pivot: list[float],
                              initial_rotation: list[float], declaration: dict[str, Any]) -> dict[str, Any]:
    """The folder's gaps: ``step_scope: explicit``.

    The classic re-poses the part between its draws from code - the GiantRobot's ``renderLeg`` /
    ``renderArm`` set the shared leg and arm parts' rotation point and pitch per side (orig
    ModelGiantRobot.java:173-266), the 1.7.10 Crab's ``render`` sets the three leg parts' position and
    yaw eight times (orig ModelCrab.java:195-289) - a translation and a mirrored yaw that no rotation
    step about one axis expresses, so the declaration lists every draw's bind transform explicitly.
    Emitted as one TOP-LEVEL clone per draw, ``<part>__i<k>`` (no group: there is no pose-stack
    transform for a hook to spin), its pivot the declared rotation point, its bind rotation the
    declared rotation (the part's own on every ``null`` axis), the part's cubes local to that pivot
    exactly as ``ModelPart.render`` draws them; the hook, when the rig lands, animates each clone
    directly. The fan forms (``part`` / ``stack``) are untouched.
    """
    transforms = explicit_instance_transforms(name, declaration, absolute_pivot, initial_rotation)
    bones: list[dict[str, Any]] = []
    mapping: dict[str, dict[str, Any]] = {}
    clone_bones: list[str] = []
    for entry in transforms:
        k = entry["draw_index"]
        pivot = entry["pivot"]
        rotation = entry["rotation"]
        clone_name = f"{name}__i{k}"
        bone: dict[str, Any] = {
            "name": clone_name,
            "pivot": clean_vector([pivot[0], 24.0 - pivot[1], pivot[2]]),
        }
        if nonzero(rotation):
            bone["rotation"] = json_rotation(rotation)
        bone["cubes"] = [convert_cube(cube, pivot) for cube in part["cubes"]]
        bones.append(bone)
        clone_bones.append(clone_name)
        mapping[clone_name] = {
            "role": "clone",
            "source_part": name,
            "draw_index": k,
            "group_bone": None,
            "static_rotation_radians": rotation,
            "static_channel": None,
            "static_pivot_classic": pivot,
        }
        if "note" in entry:
            mapping[clone_name]["instance_note"] = entry["note"]
    summary = {
        "count": len(transforms),
        "step_scope": "explicit",
        "instances": transforms,
        "group_bones": [],
        "clone_bones": clone_bones,
        "cubes_per_draw": len(part["cubes"]),
    }
    if "note" in declaration:
        summary["note"] = declaration["note"]
    if "pinned_draw_count" in declaration:
        # ANIM-025: the port draws the part fewer times than the classic declaration until its slice's fix lands;
        # the probe holds the dump to the pin (G1ModelProbe.renderInstanceContext), the rig still carries every draw.
        summary["pinned_draw_count"] = int(declaration["pinned_draw_count"])
        if "pinned_draw_note" in declaration:
            summary["pinned_draw_note"] = declaration["pinned_draw_note"]
    return {
        "bones": bones,
        "mapping": mapping,
        "summary": summary,
        "cube_count": len(part["cubes"]) * len(transforms),
    }


def expand_render_instances(name: str, part: dict[str, Any], parent: str | None,
                            absolute_pivot: list[float], initial_rotation: list[float],
                            declaration: dict[str, Any]) -> dict[str, Any]:
    """Slice 4c: one bone per DRAW of a part the classic model renders N times per frame.

    ``step_scope: explicit`` (the folder's gaps, 2026-09-14): the classic re-poses the part
    between draws from code, so the declaration lists each draw's bind transform; see
    ``expand_explicit_instances``. The two fan forms follow.

    ``step_scope: part`` (Rotator): the loop assigns the part's own ``<axis>Rot`` to
    draw k's step inside ONE pose-stack rotation the hook animates. Emitted as a
    fan group bone ``<part>__fan`` (pivot at the model origin, bind identity: the
    hook spins it) whose children ``<part>__i<k>`` carry the part's cubes at the
    part's pivot with the step as their bind rotation on that axis. ENT-S-146: an
    optional ``group_chain`` (outermost first, the last being the fan the clones hang
    under; default ``["<part>__fan"]``) nests further hook-animated groups above the fan,
    all at the origin with bind identity - a pose stack rotated about X and then about Y
    is parent X over child Y, which one GeckoLib bone (rotating Z, then Y, then X) cannot
    express; PurplePower's carried doublings are such chains.

    ``step_scope: stack`` (PurplePower): the loop pushes draw k's step onto the
    pose stack OUTSIDE the part's own animated rotation. Emitted as one parent per
    draw, ``<part>__fan<k>`` (pivot at the model origin, bind rotation the step),
    with the clone ``<part>__i<k>`` under it carrying the part's cubes, pivot and
    bind rotation; the hook writes the part's animated channels onto the clones.
    """
    if parent is not None:
        raise ValueError(f"render_instances part {name} must be a top-level part")
    if part["children"]:
        raise ValueError(f"render_instances part {name} must not have children")
    if not part["cubes"]:
        raise ValueError(f"render_instances part {name} has no cubes")
    scope = declaration["step_scope"]
    if scope not in ("part", "stack", "explicit"):
        raise ValueError(f"render_instances.{name}.step_scope {scope!r} is not part, stack or explicit")
    if scope == "explicit":
        return expand_explicit_instances(name, part, absolute_pivot, initial_rotation, declaration)
    axis = declaration["axis"]
    if axis not in AXIS_INDEX:
        raise ValueError(f"render_instances.{name}.axis {axis!r} is not x, y or z")
    step, angles = render_instance_angles(name, declaration)
    axis_index = AXIS_INDEX[axis]
    pivot_json = clean_vector([absolute_pivot[0], 24.0 - absolute_pivot[1], absolute_pivot[2]])
    # The classic loop rotates the pose stack about the model origin: classic (0, 0, 0).
    origin_json = clean_vector([0.0, 24.0, 0.0])
    cubes = [convert_cube(cube, absolute_pivot) for cube in part["cubes"]]

    bones: list[dict[str, Any]] = []
    mapping: dict[str, dict[str, Any]] = {}
    group_bones: list[str] = []
    clone_bones: list[str] = []
    if scope == "part":
        chain = list(declaration.get("group_chain", [f"{name}__fan"]))
        if not chain or len(set(chain)) != len(chain) or any(not isinstance(g, str) or not g for g in chain):
            raise ValueError(f"render_instances.{name}.group_chain must be a non-empty list of distinct bone names")
        clones = [f"{name}__i{k}" for k in range(len(angles))]
        for depth, group in enumerate(chain):
            bone_json: dict[str, Any] = {"name": group, "pivot": origin_json}
            if depth > 0:
                bone_json["parent"] = chain[depth - 1]
            bones.append(bone_json)
            group_bones.append(group)
            mapping[group] = {
                "role": "group",
                "source_part": name,
                "draw_index": None,
                "animated_by_hook": True,
                "static_rotation_radians": None,
                "clones": clones,
            }
            if "group_chain" in declaration:
                # ENT-S-146: the chain is recorded only when declared, so a one-group rig's report is unchanged.
                mapping[group].update({
                    "group_chain": chain,
                    "chain_index": depth,
                    "parent_group": chain[depth - 1] if depth > 0 else None,
                })
        group = chain[-1]
        for k, angle in enumerate(angles):
            rotation = list(initial_rotation)
            rotation[axis_index] = angle
            clone_name = f"{name}__i{k}"
            bone: dict[str, Any] = {"name": clone_name, "parent": group, "pivot": pivot_json}
            if nonzero(rotation):
                bone["rotation"] = json_rotation(rotation)
            bone["cubes"] = copy.deepcopy(cubes)
            bones.append(bone)
            clone_bones.append(clone_name)
            mapping[clone_name] = {
                "role": "clone",
                "source_part": name,
                "draw_index": k,
                "group_bone": group,
                "static_rotation_radians": rotation,
                "static_channel": axis,
            }
    else:
        if "group_chain" in declaration:
            raise ValueError(f"render_instances.{name}.group_chain needs step_scope part")
        for k, angle in enumerate(angles):
            fan = f"{name}__fan{k}"
            static = [0.0, 0.0, 0.0]
            static[axis_index] = angle
            fan_bone: dict[str, Any] = {"name": fan, "pivot": origin_json}
            if nonzero(static):
                fan_bone["rotation"] = json_rotation(static)
            bones.append(fan_bone)
            group_bones.append(fan)
            clone_name = f"{name}__i{k}"
            mapping[fan] = {
                "role": "group",
                "source_part": name,
                "draw_index": k,
                "animated_by_hook": False,
                "static_rotation_radians": static,
                "clones": [clone_name],
            }
            clone: dict[str, Any] = {"name": clone_name, "parent": fan, "pivot": pivot_json}
            if nonzero(initial_rotation):
                clone["rotation"] = json_rotation(initial_rotation)
            clone["cubes"] = copy.deepcopy(cubes)
            bones.append(clone)
            clone_bones.append(clone_name)
            mapping[clone_name] = {
                "role": "clone",
                "source_part": name,
                "draw_index": k,
                "group_bone": fan,
                "static_rotation_radians": list(initial_rotation),
                "static_channel": None,
            }
    summary = {
        "count": len(angles),
        "axis": axis,
        "step_scope": scope,
        "step_radians": step,
        "step_arithmetic": declaration.get("step_arithmetic", "exact"),
        "angles_radians": angles,
        "group_bones": group_bones,
        "clone_bones": clone_bones,
        "cubes_per_draw": len(cubes),
    }
    if "note" in declaration:
        summary["note"] = declaration["note"]
    return {
        "bones": bones,
        "mapping": mapping,
        "summary": summary,
        "cube_count": len(cubes) * len(angles),
    }


def instance_source(unit: str) -> str:
    """A draw-order unit's compiled part: a render-instance clone ``<part>__i<k>`` names its part, anything else itself."""
    marker = unit.rfind("__i")
    return unit[:marker] if marker > 0 and unit[marker + 3:].isdigit() else unit


def undrawn_parts_declared(spec: dict[str, Any], compiled: dict[str, Any]) -> frozenset[str]:
    """The manifest's ``undrawn_parts`` (TEST-013): compiled parts the classic ``renderToBuffer`` never draws - the
    Dungeon Beast's ltoe1 / ltoe3 / rtoe1 / rtoe3, built by
    the classic model and never rendered, in 1.7.10 and the port alike. The converter OMITS them from the geo: no bone, no
    cubes, no entry in the draw-order key (which therefore still names exactly the rig's bones). Validated here: compiled
    part names, unique, not render-instance parts, and without children (a child's parent must be a bone the geo carries)."""
    declared = spec.get("undrawn_parts", [])
    model_id = spec["id"]
    if not isinstance(declared, list) or any(not isinstance(name, str) or not name for name in declared):
        raise ValueError(f"{model_id} undrawn_parts must be a list of compiled part names")
    if len(set(declared)) != len(declared):
        raise ValueError(f"{model_id} undrawn_parts repeats a part: {declared}")
    unknown = sorted(set(declared) - set(compiled["bone_names"]))
    if unknown:
        raise ValueError(f"{model_id} undrawn_parts names parts the compiled model lacks: {unknown}")
    overlap = sorted(set(declared) & set(spec.get("render_instances") or {}))
    if overlap:
        raise ValueError(f"{model_id} undrawn_parts overlaps render_instances: {overlap}")
    with_children = sorted({parent for _part, parent in iter_parts(compiled["definition"]) if parent in declared})
    if with_children:
        raise ValueError(
            f"{model_id} undrawn_parts lists parts with children, whose subtree the geo could not carry: {with_children}"
        )
    return frozenset(declared)


# THE HIERARCHY FORM (design section 5; the FK slice): a manifest entry's `hierarchy` is {child: parent} over the
# compiled model's TOP-LEVEL parts - the classic flat rig whose child pivots the classic setupAnim rewrites by
# trigonometry (the Alien's neck / head / jaw, tail and claw chains, the Emperor Scorpion's tail, legs and claws). The
# converter emits each declared child as a GeckoLib bone PARENTED to its chain parent: the part names preserved, its
# pivot the absolute bind pivot in Blockbench terms (unchanged), its bind rotation the LOCAL one - the parent's flat
# (world) bind rotation inverted onto the child's, R_local = R_parent^-1 * R_child, so the bake's world rotation at bind
# is the classic's (GeckoLib composes a parent's rotation onto its children, RenderUtil .prepMatrixForBone: translate to
# the bone, to its pivot, rotate Z then Y then X, away from the pivot - the same ZYX order as
# ModelPart.translateAndRotate, and the converter's Y-reflection conjugation preserves composition). The hook
# expresses the classic world transforms through parent-relative rotations and positions (FlatRig); the harness's
# chain-link leg (tools/g1_render_parity.py chain_link_parity) compares the world matrices at every link.
HIERARCHY_DRAW_ORDER_CLASSIC = "classic"
# A MEASUREMENT form only (`hierarchy_draw_order`): the seam draws a bake in PRE-ORDER (GeoRenderer.renderRecursively draws
# a bone's cubes, then its children; DrawOrder.apply refuses a key that is not a pre-order of the tree), so a hierarchy
# whose classic draw order is not one - a parent drawn after a child, a subtree interleaved with another - cannot draw in
# the classic order through the seam as it stands, and derive_bone_draw_order refuses it (its FINDINGs). Under this form
# the key is the classic order re-sequenced into the hierarchy's pre-order (siblings by their subtree's first classic draw)
# and the findings are recorded instead: the draw-order leg FAILS such a rig by construction (the classic list is not the
# emitted one), so nothing under it can ship; it exists so a held rig's other legs can be measured and reported.
HIERARCHY_DRAW_ORDER_PREORDER_DIAGNOSTIC = "preorder_diagnostic"
HIERARCHY_DRAW_ORDERS = (HIERARCHY_DRAW_ORDER_CLASSIC, HIERARCHY_DRAW_ORDER_PREORDER_DIAGNOSTIC)


def hierarchy_declared(spec: dict[str, Any], compiled: dict[str, Any]) -> dict[str, str]:
    """The manifest's ``hierarchy`` ({child: parent}), validated: compiled part names, top-level in the compiled tree
    (a flat classic rig; a nested compiled part keeps its own parent), not undrawn or render-instance parts, a child
    declared once (an object's keys), no self-parenting, no cycle. Empty when the entry declares none."""
    declared = spec.get("hierarchy")
    model_id = spec["id"]
    if declared is None:
        return {}
    if not isinstance(declared, dict) or not declared or any(
            not isinstance(child, str) or not child or not isinstance(parent, str) or not parent
            for child, parent in declared.items()):
        raise ValueError(f"{model_id} hierarchy must be a non-empty object of child part name -> parent part name")
    names = set(compiled["bone_names"])
    involved = set(declared) | set(declared.values())
    unknown = sorted(involved - names)
    if unknown:
        raise ValueError(f"{model_id} hierarchy names parts the compiled model lacks: {unknown}")
    excluded = set(spec.get("undrawn_parts") or []) | set(spec.get("render_instances") or {})
    overlap = sorted(involved & excluded)
    if overlap:
        raise ValueError(f"{model_id} hierarchy overlaps undrawn_parts / render_instances: {overlap}")
    nested = sorted(part["name"] for part, parent in iter_parts(compiled["definition"])
                    if parent is not None and part["name"] in involved)
    if nested:
        raise ValueError(f"{model_id} hierarchy may only parent top-level compiled parts (a flat classic rig): {nested}")
    for child, parent in declared.items():
        if child == parent:
            raise ValueError(f"{model_id} hierarchy parents {child} to itself")
        seen = [child]
        cursor = parent
        while cursor in declared:
            if cursor in seen:
                raise ValueError(f"{model_id} hierarchy has a cycle through {seen}")
            seen.append(cursor)
            cursor = declared[cursor]
    return dict(declared)


def hierarchy_draw_order_declared(spec: dict[str, Any], hierarchy: dict[str, str]) -> str:
    """The entry's ``hierarchy_draw_order``: classic by default; the measurement form only with a hierarchy."""
    mode = spec.get("hierarchy_draw_order", HIERARCHY_DRAW_ORDER_CLASSIC)
    if mode not in HIERARCHY_DRAW_ORDERS:
        raise ValueError(f"{spec['id']} hierarchy_draw_order {mode!r} is not one of {HIERARCHY_DRAW_ORDERS}")
    if mode != HIERARCHY_DRAW_ORDER_CLASSIC and not hierarchy:
        raise ValueError(f"{spec['id']} hierarchy_draw_order {mode!r} needs a hierarchy")
    return mode


def rotation_matrix_zyx(angles: Iterable[float]) -> list[list[float]]:
    """ModelPart / JOML ``rotationZYX``: classic (xRot, yRot, zRot) radians -> the 3x3 rows of Rz * Ry * Rx (a vector is
    rotated about X first, then Y, then Z) - ModelPart.translateAndRotate's ``rotationZYX(zRot, yRot, xRot)`` and
    GeckoLib's RenderUtil.rotateMatrixAroundBone (mulPose Z, then Y, then X) alike."""
    x, y, z = (float(value) for value in angles)
    cx, sx, cy, sy, cz, sz = math.cos(x), math.sin(x), math.cos(y), math.sin(y), math.cos(z), math.sin(z)
    return [
        [cz * cy, cz * sy * sx - sz * cx, cz * sy * cx + sz * sx],
        [sz * cy, sz * sy * sx + cz * cx, sz * sy * cx - cz * sx],
        [-sy, cy * sx, cy * cx],
    ]


def euler_angles_zyx(matrix: list[list[float]]) -> list[float]:
    """The inverse of ``rotation_matrix_zyx``: the classic (xRot, yRot, zRot) of a rotation matrix M = Rz * Ry * Rx.
    ``M[2][0] = -sin(y)``; away from the gimbal (|cos(y)| > 0) ``x = atan2(M[2][1], M[2][2])`` and
    ``z = atan2(M[1][0], M[0][0])``; at the gimbal (y = +-pi/2, cos(y) = 0) x and z are not separable - z is taken as 0 and
    x from the first row (``M[0][1] = sin(y) sin(x)``, ``M[0][2] = sin(y) cos(x)``)."""
    sy = max(-1.0, min(1.0, -float(matrix[2][0])))
    if abs(sy) < 1.0 - 1.0e-12:
        return [math.atan2(matrix[2][1], matrix[2][2]), math.asin(sy), math.atan2(matrix[1][0], matrix[0][0])]
    return [math.atan2(sy * matrix[0][1], sy * matrix[0][2]), math.copysign(math.pi / 2.0, sy), 0.0]


def matrix_multiply(left: list[list[float]], right: list[list[float]]) -> list[list[float]]:
    return [[sum(left[r][k] * right[k][c] for k in range(3)) for c in range(3)] for r in range(3)]


def matrix_transpose(matrix: list[list[float]]) -> list[list[float]]:
    return [[matrix[c][r] for c in range(3)] for r in range(3)]


def wrap_angle(value: float) -> float:
    """An angle into (-pi, pi]."""
    wrapped = math.fmod(value + math.pi, 2.0 * math.pi)
    if wrapped <= 0.0:
        wrapped += 2.0 * math.pi
    return wrapped - math.pi


def euler_angles_zyx_classic_branch(matrix: list[list[float]]) -> list[float]:
    """The ZYX Euler triple of a rotation AS A CLASSIC RIG AUTHORS IT. A rotation has two ZYX triples - (x, y, z) with
    y in [-pi/2, pi/2] (``euler_angles_zyx``) and (x + pi, pi - y, z + pi) - and a classic setupAnim writes ONE axis at a
    time over the bind, so the hook's flat buffer (FlatRig) must hold the classic's own triple, not an equivalent one:
    the convention is the triple with the smaller |xRot| + |zRot| (a classic part turned past a quarter turn about Y is
    authored as a yaw, never as a half-turn pitch and roll beside it). The converter checks, for every declared child,
    that this convention recovers the compiled bind triple from the composed local one (``local_bind_rotation``)."""
    first = euler_angles_zyx(matrix)
    second = [wrap_angle(first[0] + math.pi), wrap_angle(math.pi - first[1]), wrap_angle(first[2] + math.pi)]
    return second if abs(second[0]) + abs(second[2]) < abs(first[0]) + abs(first[2]) - 1.0e-12 else first


def local_bind_rotation(parent_flat: Iterable[float], child_flat: Iterable[float]) -> list[float]:
    """A declared child's LOCAL bind rotation (classic radians, ZYX) from the flat compiled bind rotations of its parent
    and itself: R_local = R_parent^-1 * R_child (the transpose, a rotation's inverse), so that the bake's composition
    R_parent * R_local reproduces the child's flat (world) bind rotation - checked here to 1e-9 on every entry, and the
    hook's convention for reading that flat triple back from the composition (``euler_angles_zyx_classic_branch``, the
    FlatRig's) checked to recover the compiled triple itself, axis by axis modulo a full turn."""
    parent = rotation_matrix_zyx(parent_flat)
    child = rotation_matrix_zyx(child_flat)
    local = euler_angles_zyx(matrix_multiply(matrix_transpose(parent), child))
    recomposed = matrix_multiply(parent, rotation_matrix_zyx(local))
    drift = max(abs(recomposed[r][c] - child[r][c]) for r in range(3) for c in range(3))
    if drift > 1.0e-9:
        raise ValueError(f"local bind rotation does not recompose the child's flat rotation (drift {drift:.3g})")
    recovered = euler_angles_zyx_classic_branch(recomposed)
    triple_drift = max(abs(wrap_angle(recovered[axis] - float(list(child_flat)[axis]))) for axis in range(3))
    if triple_drift > 1.0e-9:
        raise ValueError(
            f"the hook's flat-bind convention (the ZYX triple with the smaller |xRot| + |zRot|) would recover "
            f"{recovered} for a child whose compiled bind rotation is {list(child_flat)}: the FlatRig could not hold "
            "the classic triple (drift {triple_drift:.3g})"
        )
    return local


def derive_bone_draw_order(compiled: dict[str, Any],
                           bones: list[dict[str, Any]],
                           undrawn_parts: frozenset[str] = frozenset(),
                           preorder_diagnostic: bool = False) -> tuple[list[str], dict[str, Any]]:
    """G2 root-order contract: the geo bones in the order the classic renderer draws the parts.

    ``preorder_diagnostic`` (the hierarchy form's measurement mode, HIERARCHY_DRAW_ORDER_PREORDER_DIAGNOSTIC): the two
    tree FINDINGs below and the lifting check are RECORDED in the evidence instead of raised, and the key is the
    pre-order the lifting produces - a rig the seam cannot draw in the classic order, measured on its other legs.

    The probe records, for every full capture, the classic ``renderToBuffer`` draw
    sequence (``draw_order``: cube-bearing parts, a render-instance draw as its
    clone ``<part>__i<k>``). Every capture is a constraint; they are merged into
    one total order over the cube-bearing geo bones (a cycle is a FINDING: the
    classic order depends on the state and one static order cannot express it),
    then lifted to the geo bone tree as a pre-order listing - GeckoLib and
    ``ModelPart`` both draw a parent's cubes and then each child's subtree - with
    siblings ordered by their subtree's first draw. A subtree whose draws are
    interleaved with another's, or a bone drawn after one of its descendants, is
    a FINDING too: the tree cannot express it. Pairs never drawn together in any
    capture are ordered by the converter's emission order (the deterministic
    tie-break); such pairs are invisible to a player in every proven state.
    """
    full_samples = [sample for sample in compiled["samples"] if sample.get("capture_kind") == "full"]
    sequences: list[list[str]] = []
    for sample in full_samples:
        if "draw_order" not in sample:
            raise ValueError(
                f"{compiled['model_id']} capture {sample['id']} carries no draw_order; "
                "the compiled dump predates the G2 root-order contract"
            )
        sequences.append([str(token) for token in sample["draw_order"]])
    emission_rank = {bone["name"]: index for index, bone in enumerate(bones)}
    units = [bone["name"] for bone in bones if bone.get("cubes")]
    unit_set = set(units)
    for sample, sequence in zip(full_samples, sequences):
        drawn_undrawn = sorted({token for token in sequence if instance_source(token) in undrawn_parts})
        if drawn_undrawn:
            raise ValueError(
                f"UNDRAWN PART DRAWN {compiled['model_id']}: capture {sample['id']} draws {drawn_undrawn}, which the "
                "manifest lists under undrawn_parts (a part listed as undrawn is never drawn by the classic renderToBuffer)"
            )
        unknown = [token for token in sequence if token not in unit_set]
        if unknown:
            raise ValueError(
                f"{compiled['model_id']} capture {sample['id']} draws {unknown}, which are not cube-bearing "
                "geo bones (a part drawn more than once without render_instances, or a part the rig lacks)"
            )
        if len(sequence) != len(set(sequence)):
            raise ValueError(f"{compiled['model_id']} capture {sample['id']} draws a unit twice")
    successors: dict[str, set[str]] = {unit: set() for unit in units}
    for sequence in sequences:
        for index, unit in enumerate(sequence):
            successors[unit].update(sequence[index + 1:])
    indegree = {unit: 0 for unit in units}
    for unit in units:
        for later in successors[unit]:
            indegree[later] += 1
    ready = sorted((unit for unit in units if indegree[unit] == 0), key=lambda unit: emission_rank[unit])
    total: list[str] = []
    while ready:
        unit = ready.pop(0)
        total.append(unit)
        for later in sorted(successors[unit], key=lambda name: emission_rank[name]):
            indegree[later] -= 1
            if indegree[later] == 0:
                ready.append(later)
        ready.sort(key=lambda name: emission_rank[name])
    if len(total) != len(units):
        stuck = sorted(unit for unit in units if unit not in total)
        raise ValueError(
            f"FINDING {compiled['model_id']}: the captures disagree on the classic draw order of {stuck}; "
            "a state-dependent order cannot be expressed as one static bone order"
        )
    observed = set().union(*sequences) if sequences else set()
    unobserved = [unit for unit in units if unit not in observed]
    rank = {unit: index for index, unit in enumerate(total) if unit in observed}

    children_of: dict[str | None, list[str]] = {}
    for bone in bones:
        children_of.setdefault(bone.get("parent"), []).append(bone["name"])

    def subtree_ranks(name: str) -> list[int]:
        ranks = [rank[name]] if name in rank else []
        for child in children_of.get(name, []):
            ranks.extend(subtree_ranks(child))
        return ranks

    def first_rank(name: str) -> float:
        ranks = subtree_ranks(name)
        return min(ranks) if ranks else math.inf

    ordered: list[str] = []
    findings: list[str] = []

    def finding(message: str) -> None:
        if preorder_diagnostic:
            findings.append(message)
            return
        raise ValueError(message)

    def emit(level: list[str]) -> None:
        for name in sorted(level, key=lambda bone: (first_rank(bone), emission_rank[bone])):
            ranks = sorted(subtree_ranks(name))
            if name in rank and any(value < rank[name] for value in ranks):
                finding(
                    f"FINDING {compiled['model_id']}: {name} is drawn after one of its descendants; "
                    "a pre-order bone traversal cannot express that"
                )
            if ranks and ranks[-1] - ranks[0] + 1 != len(ranks):
                finding(
                    f"FINDING {compiled['model_id']}: the draws of {name}'s subtree are interleaved with "
                    "another bone's; the bone tree cannot express that order"
                )
            ordered.append(name)
            emit(children_of.get(name, []))

    emit(children_of.get(None, []))
    lifted = [name for name in ordered if name in rank]
    merged = [unit for unit in total if unit in rank]
    if lifted != merged:
        finding(f"{compiled['model_id']}: pre-order lifting changed the merged draw order")
    if sorted(ordered) != sorted(emission_rank):
        raise ValueError(f"{compiled['model_id']}: the draw order does not cover every geo bone exactly once")
    evidence = {
        "source": compiled.get("draw_order_source"),
        "captures": [sample["id"] for sample in full_samples],
        "cube_bearing_units": len(units),
        "observed_units": len(observed),
        "unobserved_units": unobserved,
        "ordered_pairs": sum(len(later) for later in successors.values()),
        "tie_break": "converter emission order for pairs never drawn together in any capture",
        "lifting": "pre-order over the geo bone tree, siblings by their subtree's first classic draw",
    }
    if preorder_diagnostic:
        # the hierarchy form's measurement mode: the classic (merged) order, the pre-order the key carries instead, the
        # units whose position moved and the findings the classic mode would have raised - the draw-order leg fails it
        evidence["preorder_diagnostic"] = {
            "classic_order": merged,
            "moved_units": [unit for unit, lifted_unit in zip(merged, lifted) if unit != lifted_unit],
            "findings": findings,
            "note": "the key is the hierarchy's pre-order, NOT the classic draw order: a measurement form for a rig the "
                    "seam cannot draw in the classic order (a parent drawn after a child, a subtree interleaved); the "
                    "draw-order leg fails it by construction, so nothing under it can ship",
        }
    return ordered, evidence


def classic_face_labels(mirror: bool) -> list[str]:
    """The six GeckoLib direction names in the order ModelPart.Cube.compile emits the classic faces
    (CLASSIC_FACE_ORDER; a mirrored cube's WEST / EAST polygons carry the negated X normals): the direction
    whose step is the classic normal reflected in X and Y (GECKOLIB_LABEL_BY_CLASSIC_NORMAL, the frame)."""
    labels = []
    for face in CLASSIC_FACE_ORDER:
        normal = CLASSIC_FACE_NORMAL[face]
        if mirror and face in ("west", "east"):
            normal = (-normal[0], normal[1], normal[2])
        labels.append(GECKOLIB_LABEL_BY_CLASSIC_NORMAL[normal])
    return labels


def classic_face_normals(mirror: bool) -> list[tuple[float, float, float]]:
    """The normals of those six faces AS THE ENTITY FRAME CARRIES THEM, in emission order: the classic-space
    normal (a mirrored cube's X faces negated) through the classic chain's flip, ``entity_frame_normal`` - the
    frame the probe captures both sides in since TEST-015."""
    normals = []
    for face in CLASSIC_FACE_ORDER:
        normal = CLASSIC_FACE_NORMAL[face]
        if mirror and face in ("west", "east"):
            normal = (-normal[0], normal[1], normal[2])
        normals.append(entity_frame_normal(normal))
    return normals


def derive_cube_face_order(compiled: dict[str, Any], bones: list[dict[str, Any]],
                           mirror_flags: dict[str, list[bool]],
                           unrotated_at_bind: set[str]) -> tuple[dict[str, list[list[str]]], dict[str, Any]]:
    """ENT-S-146: the classic within-cube face order for every cube-bearing geo bone, as GeckoLib
    direction names in the order the classic renderer emits the faces.

    Derived from the bytecode rule (``classic_face_labels``: the cube's mirror flag decides the two
    X faces), because the captures are POSED (a clone's faces turn with its step) and a posed normal
    names no label. Verified twice: here, against the bind capture's cubes that are unrotated at bind
    (their captured normals must be the rule's, in order), and by the parity tool's draw-order leg,
    which compares the per-quad normal sequences of the two renderers on every capture. Either
    disagreement is a FINDING, never a silent key.
    """
    order: dict[str, list[list[str]]] = {}
    for bone in bones:
        if not bone.get("cubes"):
            continue
        flags = mirror_flags[bone["name"]]
        if len(flags) != len(bone["cubes"]):
            raise ValueError(f"{compiled['model_id']}: {bone['name']} mirror flags do not match its cubes")
        order[bone["name"]] = [classic_face_labels(mirror) for mirror in flags]
    bind = next((sample for sample in compiled["samples"]
                 if sample["id"] == "bind" and sample.get("capture_kind") == "full"), None)
    verified = 0
    if bind is not None:
        for cube in bind["cubes"]:
            bone_name = cube["bone"]
            if bone_name not in unrotated_at_bind or bone_name not in mirror_flags:
                continue
            mirror = mirror_flags[bone_name][int(cube["cube_index"])]
            vertices = cube["vertices"]
            if len(vertices) % 4 or len(vertices) // 4 != len(CLASSIC_FACE_ORDER):
                raise ValueError(f"{compiled['model_id']}: {bone_name} cube {cube['cube_index']} does not emit six quads")
            captured = []
            for offset in range(0, len(vertices), 4):
                normals = {tuple(round(float(v), 6) for v in vertex["normal"]) for vertex in vertices[offset:offset + 4]}
                if len(normals) != 1:
                    raise ValueError(f"{compiled['model_id']}: {bone_name} quad vertices disagree on the normal")
                captured.append(next(iter(normals)))
            expected = classic_face_normals(mirror)
            if captured != expected:
                raise ValueError(
                    f"FINDING {compiled['model_id']}: {bone_name} cube {cube['cube_index']} emits its faces with normals "
                    f"{captured} at bind, not the ModelPart.Cube order {expected}"
                )
            verified += 1
    evidence = {
        "rule": "NeoForge 21.1.223 ModelPart.Cube.<init> polygon order DOWN, UP, WEST, NORTH, EAST, SOUTH "
                "(offsets 365-785: DOWN 365, UP 436, WEST 507, NORTH 578, EAST 649, SOUTH 720), mirror negating the X "
                "normals; GeckoLib labels by the direction whose step is "
                "the classic normal reflected in X and Y (the entity frame: the classic chain's scale(-1, -1, 1) and "
                "the baker's x negation alike, TEST-015)",
        "cube_bearing_bones": len(order),
        "cubes": sum(len(cubes) for cubes in order.values()),
        "verified_unrotated_cubes_at_bind": verified,
        "verification": "captured normals (the entity frame) of every cube unrotated at bind equal the rule's, in order; "
                        "the parity tool's draw-order leg compares the per-quad normal sequences of both renderers on every capture",
    }
    return order, evidence


def convert_geometry(compiled: dict[str, Any],
                     render_instances: dict[str, Any] | None = None,
                     cube_face_order: str | None = None,
                     undrawn_parts: frozenset[str] = frozenset(),
                     hierarchy: dict[str, str] | None = None,
                     hierarchy_draw_order: str = HIERARCHY_DRAW_ORDER_CLASSIC) -> tuple[dict[str, Any], dict[str, Any]]:
    root = compiled["definition"]
    if root["cubes"]:
        raise ValueError("unnamed MeshDefinition root contains cubes")
    hierarchy = dict(hierarchy or {})

    bones: list[dict[str, Any]] = []
    cube_count = 0
    expansion_parts: dict[str, Any] = {}
    expansion_bones: dict[str, Any] = {}
    # ENT-S-146: per emitted cube-bearing bone, its cubes' ModelPart mirror flags (a clone's are its
    # source part's), and the bones whose cubes are unrotated at bind (the face-order self-check).
    mirror_flags: dict[str, list[bool]] = {}
    unrotated_at_bind: set[str] = set()
    bind_rotations = initial_rotations(compiled) if any(
        sample["id"] == "bind" for sample in compiled["samples"]) else {}
    ancestors: dict[str, list[str]] = {}
    # the hierarchy form: every top-level compiled part's flat bind rotation IS its world bind rotation, the parents'
    # among them the frames the declared children's local bind rotations and pivots are derived in
    flat_rotations = {part["name"]: [float(value) for value in part["initial_rotation_radians"]]
                      for part, _parent in iter_parts(root)}
    flat_pivots = {part["name"]: [float(value) for value in part["absolute_pivot"]]
                   for part, _parent in iter_parts(root)}
    local_bind_rotations: dict[str, list[float]] = {}
    derived_pivots: dict[str, list[float]] = {}

    def derived_pivot(name: str) -> list[float]:
        """THE PIVOT IN BLOCKBENCH TERMS. GeckoLib rotates a child's pivot with its parent (the child's world pivot at
        bind is P_parent_world + R_parent * (P_child - P_parent), the pivots as the geo stores them), so a child under a
        parent that is ROTATED at bind cannot keep the classic absolute pivot: its geo pivot is the classic one carried
        back through the parent's bind rotation, P_c = P_p + R_p^-1 * (classic_c - classic_p) (P_p the parent's own derived
        pivot, classic_* the flat rotation points), and its cubes sit at that pivot plus their classic local offsets, so the
        bake's bind places every corner where the classic does. A root's is the flat absolute pivot."""
        if name in derived_pivots:
            return derived_pivots[name]
        if name not in hierarchy:
            derived_pivots[name] = list(flat_pivots[name])
            return derived_pivots[name]
        parent_name = hierarchy[name]
        parent_pivot = derived_pivot(parent_name)
        inverse = matrix_transpose(rotation_matrix_zyx(flat_rotations[parent_name]))
        offset = [flat_pivots[name][axis] - flat_pivots[parent_name][axis] for axis in range(3)]
        rotated = [sum(inverse[r][c] * offset[c] for c in range(3)) for r in range(3)]
        derived_pivots[name] = [parent_pivot[axis] + rotated[axis] for axis in range(3)]
        return derived_pivots[name]

    for part, parent in iter_parts(root):
        name = part["name"]
        absolute_pivot = [float(value) for value in part["absolute_pivot"]]
        initial_rotation = [float(value) for value in part["initial_rotation_radians"]]
        ancestors[name] = ([] if parent is None else ancestors[parent] + [parent])
        if name in undrawn_parts:
            # never drawn by the classic renderToBuffer (the manifest's undrawn_parts): no bone, no cubes, no key entry
            continue
        if name in hierarchy:
            # the hierarchy form: the bone's pivot and its cubes' origins are placed at the DERIVED pivot (above)
            absolute_pivot = derived_pivot(name)
        lineage_unrotated = all(
            not nonzero(bind_rotations.get(ancestor, initial_rotation)) for ancestor in ancestors[name]
        ) and not nonzero(bind_rotations.get(name, initial_rotation))
        if render_instances and name in render_instances:
            expanded = expand_render_instances(
                name, part, parent, absolute_pivot, initial_rotation, render_instances[name]
            )
            bones.extend(expanded["bones"])
            cube_count += expanded["cube_count"]
            expansion_parts[name] = expanded["summary"]
            expansion_bones.update(expanded["mapping"])
            for clone_name, entry in expanded["mapping"].items():
                if entry["role"] != "clone":
                    continue
                mirror_flags[clone_name] = [bool(cube["mirror"]) for cube in part["cubes"]]
                group_bone = entry.get("group_bone")  # None for an explicit-form clone (top-level, no group)
                group_static = expanded["mapping"][group_bone].get("static_rotation_radians") if group_bone else None
                if lineage_unrotated and not nonzero(entry["static_rotation_radians"]) and (
                        group_static is None or not nonzero(group_static)):
                    unrotated_at_bind.add(clone_name)
            continue
        if part["cubes"]:
            mirror_flags[name] = [bool(cube["mirror"]) for cube in part["cubes"]]
            if lineage_unrotated:
                unrotated_at_bind.add(name)
        bone: dict[str, Any] = {
            "name": name,
            # the Bedrock convention (TEST-015): the classic pivot's x kept, y up from the datum
            "pivot": clean_vector(
                [absolute_pivot[0], 24.0 - absolute_pivot[1], absolute_pivot[2]]
            ),
        }
        if parent is not None:
            bone["parent"] = parent
        bone_rotation = initial_rotation
        if name in hierarchy:
            # THE HIERARCHY FORM: the child parented to its declared chain parent, its pivot the DERIVED one (the classic
            # bind pivot carried back through the parent's bind rotation, derived_pivot above: Blockbench pivots are
            # absolute and a parent's rotation rotates its children's pivots), its bind rotation the LOCAL one so the bake
            # composes the classic world rotation at bind (the flat compiled tree's initial rotations are world rotations:
            # both parts are top-level, hierarchy_declared)
            bone["parent"] = hierarchy[name]
            bone_rotation = local_bind_rotation(flat_rotations[hierarchy[name]], initial_rotation)
            local_bind_rotations[name] = [float(value) for value in bone_rotation]
        if nonzero(bone_rotation):
            # the sign rule (json_rotation): internal space is classic space reflected in X and Y, so the
            # internal triple is (-X, -Y, +Z) of the classic; the baker negates JSON X and Y at load, hence
            # JSON = +classic degrees on every axis
            bone["rotation"] = json_rotation(bone_rotation)
        if part["cubes"]:
            bone["cubes"] = [
                convert_cube(cube, absolute_pivot)
                for cube in part["cubes"]
            ]
            cube_count += len(part["cubes"])
        bones.append(bone)

    input_names = sorted(compiled["bone_names"])
    output_names = sorted(bone["name"] for bone in bones)
    if render_instances:
        missing = sorted(set(render_instances) - set(input_names))
        if missing:
            raise ValueError(f"render_instances names parts the compiled model lacks: {missing}")
        # Expanded parts leave the rig; their group and clone bones join it.
        expected_names = sorted((set(input_names) - set(render_instances) - undrawn_parts) | set(expansion_bones))
    else:
        expected_names = sorted(set(input_names) - undrawn_parts)
    if output_names != expected_names:
        raise ValueError(f"bone-name drift: expected {expected_names} != output {output_names}")
    if len(output_names) != len(set(output_names)):
        raise ValueError("duplicate bone names cannot be preserved by GeckoLib")

    model_id = compiled["model_id"]
    if set(local_bind_rotations) != set(hierarchy):
        raise ValueError(f"{model_id}: hierarchy names parts the geo does not carry: "
                         f"{sorted(set(hierarchy) - set(local_bind_rotations))}")
    bone_draw_order, draw_order_evidence = derive_bone_draw_order(
        compiled, bones, undrawn_parts, hierarchy_draw_order == HIERARCHY_DRAW_ORDER_PREORDER_DIAGNOSTIC)
    description: dict[str, Any] = {
        "identifier": f"geometry.orespawn.g1.{model_id}",
        "texture_width": compiled["texture_width"],
        "texture_height": compiled["texture_height"],
        DRAW_ORDER_KEY: bone_draw_order,
    }
    face_order = None
    face_order_evidence = None
    if cube_face_order is not None:
        if cube_face_order != "classic":
            raise ValueError(f"{model_id}: cube_face_order {cube_face_order!r} is not supported (only \"classic\")")
        face_order, face_order_evidence = derive_cube_face_order(compiled, bones, mirror_flags, unrotated_at_bind)
        description[FACE_ORDER_KEY] = face_order
    geometry = {
        "format_version": "1.12.0",
        "minecraft:geometry": [
            {
                "description": description,
                "bones": bones,
            }
        ],
    }
    summary = {
        "bone_count": len(bones),
        "cube_count": cube_count,
        "mirrored_cube_count": sum(
            1
            for bone in bones
            for cube in bone.get("cubes", [])
            if cube.get("modelpart_mirror")
        ),
        "mirrored_uv_strategy": (
            "source flag retained as modelpart_mirror; semantics baked into explicit faces; "
            "incompatible GeckoLib native mirror disabled"
        ),
        "exact_bone_names": output_names,
        "bone_draw_order": bone_draw_order,
        "draw_order_evidence": draw_order_evidence,
    }
    if undrawn_parts:
        summary["undrawn_parts"] = sorted(undrawn_parts)
    if hierarchy:
        summary["hierarchy"] = {
            "declared": hierarchy,
            "links": len(hierarchy),
            "local_bind_rotations_radians": {child: clean_vector(local_bind_rotations[child], 12) for child in hierarchy},
            "derived_pivots_classic": {child: clean_vector(derived_pivots[child], 12) for child in hierarchy},
            "draw_order": hierarchy_draw_order,
            "semantics": (
                "each declared child is a bone parented to its chain parent (the part names preserved), its pivot derived "
                "in Blockbench terms - the classic bind pivot carried back through the parent's bind rotation, "
                "P_c = P_p + R_p^-1 * (classic_c - classic_p), its cubes at that pivot plus their classic local offsets - and "
                "its bind rotation the LOCAL one, R_parent^-1 * R_child, so the bake's bind places every corner and every "
                "world rotation where the classic flat rig does; the hook writes parent-relative rotations and positions "
                "that reproduce the classic world transform at every link (the chain-link leg's proof)"
            ),
        }
    if face_order is not None:
        summary["cube_face_order"] = face_order
        summary["cube_face_order_evidence"] = face_order_evidence
    if render_instances:
        summary["render_instances"] = {
            "declared": render_instances,
            "parts": expansion_parts,
            "bones": expansion_bones,
            "semantics": (
                "one bone per classic draw: <part>__i<k> is draw k of <part> (its cubes and pivot); "
                "step_scope part = the loop assigns the part's own axis channel, so the step is the clone's "
                "bind rotation under one hook-animated group <part>__fan; step_scope stack = the loop rotates "
                "the pose stack outside the part, so the step is the bind rotation of a per-draw parent "
                "<part>__fan<k> and the clone carries the part's animated channels"
            ),
        }
        if any(parts.get("step_scope") == "explicit" for parts in expansion_parts.values()):
            # the folder's gaps (2026-09-14): written only for a rig with an explicit-form part, so the fan rigs'
            # reports stay byte-identical
            summary["render_instances"]["semantics_explicit"] = (
                "step_scope explicit = the classic re-poses the part between its draws from code (a rotation point "
                "and rotation set per draw, no pose-stack step), so draw k is a top-level clone <part>__i<k> whose "
                "bind pivot and rotation are the declared per-draw transform at bind and whose cubes are the part's, "
                "local to that pivot; a hook animates each clone directly"
            )
    return geometry, summary


def initial_rotations(compiled: dict[str, Any]) -> dict[str, list[float]]:
    bind = next(sample for sample in compiled["samples"] if sample["id"] == "bind")
    return {
        bone: [float(value) for value in transform["rotation"]]
        for bone, transform in bind["transforms"].items()
    }


def json_rotation_delta(target: list[float], initial: list[float]) -> list[float]:
    """THE CLIP SIGN RULE: a keyed rotation delta is authored as +classic degrees on X, Y and Z alike (the rule
    ``json_rotation`` derives; the generators quote it: tools/keyframe_clip.py AUTHORED_SIGN, KeyframeLeg.generate,
    ReferenceClipSampler.authoredKeys). GeckoLib 4.8.4 negates constant X and Y keys at load
    (BakedAnimationsAdapter.buildKeyframeStack 209-224 / 250-265) and adds them to the bone's initial snapshot, so
    the loaded value lands on the internal basis (-x, -y, +z) the base's rotateX / rotateY / rotateZ write. Before
    this landing (TEST-015) the rule was (+x, -y, -z)."""
    delta = [target[index] - initial[index] for index in range(3)]
    return [
        math.degrees(delta[0]),
        math.degrees(delta[1]),
        math.degrees(delta[2]),
    ]


def format_time(seconds: float) -> str:
    value = f"{seconds:.9f}".rstrip("0").rstrip(".")
    return value if "." in value else value + ".0"


def build_clip(spec: dict[str, Any], compiled: dict[str, Any],
               channels: list[dict[str, Any]],
               ticks_per_second: float) -> dict[str, Any]:
    initial = initial_rotations(compiled)
    animated_bones = sorted({bone for channel in channels for bone in channel["bones"]})
    bone_keyframes: dict[str, dict[str, dict[str, Any]]] = {
        bone: {} for bone in animated_bones
    }
    bake_samples = compiled["animation_bake_samples"]
    if not bake_samples:
        raise ValueError(f"{compiled['model_id']} has no compiled animation bake samples")
    interpolation = spec["animation_interpolation"]
    if interpolation != "catmullrom":
        raise ValueError(f"unsupported G1 reference interpolation {interpolation}")
    for sample in bake_samples:
        seconds = float(sample["age_ticks"]) / float(ticks_per_second)
        time_key = format_time(seconds)
        for bone in animated_bones:
            if bone not in sample["transforms"]:
                raise ValueError(f"compiled bake sample is missing bone {bone}")
            rotation = [float(value) for value in sample["transforms"][bone]["rotation"]]
            bone_keyframes[bone][time_key] = {
                "post": clean_vector(
                    json_rotation_delta(rotation, initial[bone]), digits=8
                ),
                "lerp_mode": interpolation,
            }

    animation_length = float(bake_samples[-1]["age_ticks"]) / float(ticks_per_second)
    return {
        "loop": True,
        "animation_length": clean_number(animation_length, digits=9),
        "bones": {
            bone: {"rotation": keyframes}
            for bone, keyframes in bone_keyframes.items()
        },
    }


def validate_reference_animation_schema(spec: dict[str, Any], compiled: dict[str, Any],
                                        animation: dict[str, Any],
                                        ticks_per_second: float) -> dict[str, Any]:
    """Validate emitted JSON structurally without loading it as runtime acceptance."""
    if animation.get("format_version") != "1.8.0":
        raise ValueError("reference animation format_version must be 1.8.0")
    animations = animation.get("animations")
    if not isinstance(animations, dict):
        raise ValueError("reference animation animations member must be an object")
    if spec["animation_kind"] == "static" or spec["animation_kind"] in PRODUCTION_HOOK_KINDS:
        if animations:
            raise ValueError("static reference animation must contain no clips")
        return {
            "status": "SCHEMA_VALID_REFERENCE_ONLY",
            "clip_count": 0,
            "constant_vector_count": 0,
            "runtime_acceptance": False,
        }

    if spec.get("emitted_clip_role") != "REFERENCE_ONLY_NOT_RUNTIME_ACCEPTANCE":
        raise ValueError("Beaver emitted clip role must exclude runtime acceptance")
    expected_clips = {spec["ambient_clip_name"], spec["unit_gait_clip_name"]}
    if set(animations) != expected_clips:
        raise ValueError(
            f"reference clip names {sorted(animations)} != {sorted(expected_clips)}"
        )
    expected_times = [
        format_time(float(sample["age_ticks"]) / ticks_per_second)
        for sample in compiled["animation_bake_samples"]
    ]
    expected_bones_by_clip = {
        spec["ambient_clip_name"]: sorted({
            bone for channel in spec["channels"]
            if not channel.get("limb_swing_scaled", False)
            for bone in channel["bones"]
        }),
        spec["unit_gait_clip_name"]: sorted({
            bone for channel in spec["channels"]
            if channel.get("limb_swing_scaled", False)
            for bone in channel["bones"]
        }),
    }
    vector_count = 0
    expected_length = float(compiled["animation_bake_samples"][-1]["age_ticks"]) / ticks_per_second
    for clip_name, clip in animations.items():
        if clip.get("loop") is not True:
            raise ValueError(f"reference clip {clip_name} must preserve loop=true")
        length = clip.get("animation_length")
        if not isinstance(length, (int, float)) or not math.isfinite(float(length)):
            raise ValueError(f"reference clip {clip_name} has nonconstant animation_length")
        if abs(float(length) - expected_length) > 1.0e-8:
            raise ValueError(
                f"reference clip {clip_name} length {length} != {expected_length}"
            )
        bones = clip.get("bones")
        if not isinstance(bones, dict) or sorted(bones) != expected_bones_by_clip[clip_name]:
            raise ValueError(f"reference clip {clip_name} bone set drift")
        for bone_name, bone_channels in bones.items():
            if set(bone_channels) != {"rotation"}:
                raise ValueError(f"reference {clip_name}/{bone_name} must be rotation-only")
            keyframes = bone_channels["rotation"]
            if not isinstance(keyframes, dict) or list(keyframes) != expected_times:
                raise ValueError(f"reference {clip_name}/{bone_name} timestamp set drift")
            for time_key, frame in keyframes.items():
                if not isinstance(frame, dict) or set(frame) != {"post", "lerp_mode"}:
                    raise ValueError(f"reference {clip_name}/{bone_name}/{time_key} schema drift")
                if frame["lerp_mode"] != spec["animation_interpolation"]:
                    raise ValueError(f"reference {clip_name}/{bone_name}/{time_key} interpolation drift")
                vector = frame["post"]
                if not isinstance(vector, list) or len(vector) != 3:
                    raise ValueError(f"reference {clip_name}/{bone_name}/{time_key} vector drift")
                if any(not isinstance(value, (int, float)) or not math.isfinite(float(value))
                       for value in vector):
                    raise ValueError(
                        f"reference {clip_name}/{bone_name}/{time_key} is not a constant vector"
                    )
                vector_count += 1
    return {
        "status": "SCHEMA_VALID_REFERENCE_ONLY",
        "clip_count": len(animations),
        "constant_vector_count": vector_count,
        "authored_timestamp_count_per_channel": len(expected_times),
        "runtime_acceptance": False,
        "artist_editable_keyframe_acceptance": "OUTSTANDING_G3",
    }


def dense_probe_separation(spec: dict[str, Any], compiled: dict[str, Any]) -> dict[str, Any]:
    dense_samples = [
        sample for sample in compiled["samples"]
        if sample.get("dense_transform_sample")
        and not sample["id"].endswith("_start")
        and not sample["id"].endswith("_end")
    ]
    authored_ticks = [
        float(sample["age_ticks"]) for sample in compiled["animation_bake_samples"]
    ]
    expected_per_amplitude = int(spec["dense_transform_sample_count"])
    amplitude_count = len(spec["limb_swing_amount_samples"])
    expected_total = expected_per_amplitude * amplitude_count
    if len(dense_samples) != expected_total:
        raise ValueError(
            f"dense off-grid sample count {len(dense_samples)} != {expected_total}"
        )
    epsilon = float(spec["probe_key_coincidence_epsilon_ticks"])
    separations = [
        min(abs(float(sample["age_ticks"]) - key_tick) for key_tick in authored_ticks)
        for sample in dense_samples
    ]
    coincidences = sum(separation <= epsilon for separation in separations)
    if coincidences:
        raise ValueError(
            f"{coincidences} dense off-grid probes coincide with authored keys "
            f"within {epsilon} age ticks"
        )
    unique_probe_ticks = {float(sample["age_ticks"]) for sample in dense_samples}
    if len(unique_probe_ticks) != expected_per_amplitude:
        raise ValueError("dense probe timestamps drift across amplitude rows")
    return {
        "off_grid_probe_count_per_amplitude": expected_per_amplitude,
        "off_grid_probe_count_total": expected_total,
        "endpoint_anchor_count_per_amplitude": 2,
        "probe_offset": spec["dense_transform_probe_offset"],
        "authored_time_key_count": len(authored_ticks),
        "coincidence_epsilon_age_ticks": epsilon,
        "off_grid_probe_key_coincidences": coincidences,
        "minimum_key_probe_separation_age_ticks": min(separations),
    }


PRODUCTION_HOOK_KINDS = ("code_driven", "entity_state")
ACCEPTED_ANIMATION_EVIDENCE = {
    "static": "static bind pose; no controller",
    "gait_scaled": "exact Mth.cos GeoModel.setCustomAnimations legacy-parity exception",
    "code_driven": (
        "production OreSpawnGeoReplacement hook through "
        "OreSpawnGeoReplacementModel.setCustomAnimations (Tier-3 code-driven)"
    ),
    "entity_state": (
        "production OreSpawnGeoReplacement hook posed from declared entity states through the "
        "entity's pose interface (Tier-3 code-driven)"
    ),
}
ARTIST_EDITABLE_STATUS = {
    "static": "NOT_APPLICABLE",
    "gait_scaled": "OUTSTANDING_G3",
    "code_driven": "NOT_APPLICABLE_TIER3_CODE_DRIVEN",
    "entity_state": "NOT_APPLICABLE_TIER3_CODE_DRIVEN",
}


def convert_animation(spec: dict[str, Any], compiled: dict[str, Any],
                      ticks_per_second: float) -> tuple[dict[str, Any], dict[str, Any]]:
    kind = spec["animation_kind"]
    if kind == "static":
        if spec["channels"]:
            raise ValueError("static proof model cannot declare animation channels")
        animation = {"format_version": "1.8.0", "animations": {}}
        contract = {
            "schema_version": 1,
            "kind": "static",
            "controller_required": False,
            "source_setup_anim_expected_identity": True,
            "sample_ids": [sample["id"] for sample in compiled["samples"]],
        }
        return animation, contract

    if kind in PRODUCTION_HOOK_KINDS:
        # the revised form Tier-3: the pose is the production
        # OreSpawnGeoReplacement hook (code_driven, driven headlessly by the
        # probe) or, when the classic model reads its entity, the same hook posed
        # from declared entity states through the entity's pose interface
        # (entity_state). No clip is emitted or accepted.
        if spec["channels"] and "keyframe_reference_leg" not in spec:
            # A code_driven model may declare channels ONLY as the keyframe reference leg's transcription
            # (the first Tier-2 slice, 2026-09-13: the shipped hook is the classic formula, the layers its
            # transcription; KeyframeLeg reads them, this converter still emits no clip).
            raise ValueError(f"{kind} proof model declares channels; the pose is production code, not clips")
        if spec.get("candidate_animation_path") != "production_replacement_hook":
            raise ValueError(f"{kind} proof model must declare candidate_animation_path production_replacement_hook")
        animation = {"format_version": "1.8.0", "animations": {}}
        contract = {
            "schema_version": 1,
            "kind": kind,
            "controller_required": False,
            "candidate_animation_path": "production_replacement_hook",
            "candidate_class": spec["candidate_class"],
            "candidate_acceptance_path": (
                "production OreSpawnGeoReplacementModel.setCustomAnimations "
                "(Tier-3 code-driven)"
            ),
            "sample_ids": [sample["id"] for sample in compiled["samples"]],
        }
        if kind == "entity_state":
            contract["entity_states"] = [state["name"] for state in spec["entity_states"]]
        return animation, contract

    if kind != "gait_scaled":
        raise ValueError(f"unsupported animation kind {kind}")
    ambient_channels = [
        channel for channel in spec["channels"]
        if not channel.get("limb_swing_scaled", False)
    ]
    gait_channels = [
        channel for channel in spec["channels"]
        if channel.get("limb_swing_scaled", False)
    ]
    if not ambient_channels or not gait_channels:
        raise ValueError("gait_scaled proof requires both ambient and amplitude-scaled channels")

    ambient_name = spec["ambient_clip_name"]
    gait_name = spec["unit_gait_clip_name"]
    animation = {
        "format_version": "1.8.0",
        "animations": {
            ambient_name: build_clip(
                spec, compiled, ambient_channels, ticks_per_second
            ),
            gait_name: build_clip(
                spec, compiled, gait_channels, ticks_per_second
            ),
        },
    }
    contract = {
        "schema_version": 1,
        "kind": "gait_scaled",
        "phase_source": "ageInTicks",
        "candidate_animation_path": spec["candidate_animation_path"],
        "candidate_acceptance_path": "GeoModel.setCustomAnimations exact Mth.cos formulas",
        "ambient_clip": ambient_name,
        "unit_gait_clip": gait_name,
        "emitted_clip_role": spec["emitted_clip_role"],
        "reference_clip_acceptance": False,
        "reference_clip_reason": (
            "Constant Catmull-Rom keys cannot reproduce Minecraft Mth.cos's "
            "discontinuous LUT at 2e-6 without millions of step keys"
        ),
        "composition": "bind + ambient_delta + limbSwingAmount * unit_gait_delta",
        "limb_swing_amount_samples": spec["limb_swing_amount_samples"],
        "scaled_bones": sorted({bone for channel in gait_channels for bone in channel["bones"]}),
        "unscaled_bones": sorted({bone for channel in ambient_channels for bone in channel["bones"]}),
        "sample_ids": [sample["id"] for sample in compiled["samples"]],
        "dense_probe_key_separation": dense_probe_separation(spec, compiled),
    }
    return animation, contract


def convert_model(manifest: dict[str, Any], spec: dict[str, Any],
                  dump_dir: Path, output_dir: Path) -> None:
    model_id = spec["id"]
    compiled_path = dump_dir / f"{model_id}.compiled.json"
    compiled_bytes = compiled_path.read_bytes()
    compiled = json.loads(compiled_bytes)
    if compiled["source_class"] != spec["class"]:
        raise ValueError(
            f"{model_id} dump class {compiled['source_class']} != manifest {spec['class']}"
        )

    render_instances = spec.get("render_instances")
    if compiled.get("render_instances") != render_instances:
        raise ValueError(
            f"{model_id} render_instances drift between the probe dump and the manifest: "
            f"{compiled.get('render_instances')} != {render_instances}"
        )
    undrawn = undrawn_parts_declared(spec, compiled)
    hierarchy = hierarchy_declared(spec, compiled)
    hierarchy_draw_order = hierarchy_draw_order_declared(spec, hierarchy)
    geometry, geometry_summary = convert_geometry(compiled, render_instances, spec.get("cube_face_order"), undrawn,
                                                  hierarchy, hierarchy_draw_order)
    animation, animation_contract = convert_animation(
        spec, compiled, float(manifest["ticks_per_second"])
    )
    reference_schema = validate_reference_animation_schema(
        spec, compiled, animation, float(manifest["ticks_per_second"])
    )

    geo_bytes = write_json(output_dir / f"{model_id}.geo.json", geometry)
    animation_bytes = write_json(output_dir / f"{model_id}.animation.json", animation)
    animation_contract_bytes = write_json(
        output_dir / f"{model_id}.animation-contract.json", animation_contract
    )

    report = {
        "schema_version": 1,
        "model_id": model_id,
        "tier": spec["tier"],
        "proof_scope": spec.get("proof_scope", "production_proof_model"),
        "ground_truth": "executed compiled LayerDefinition and baked ModelPart",
        "source_class": compiled["source_class"],
        "source_class_sha256": compiled["source_class_sha256"],
        "compiled_dump_sha256": sha256(compiled_bytes),
        "geometry_sha256": sha256(geo_bytes),
        "animation_sha256": sha256(animation_bytes),
        "animation_contract_sha256": sha256(animation_contract_bytes),
        "bone_count": geometry_summary["bone_count"],
        "cube_count": geometry_summary["cube_count"],
        "mirrored_cube_count": geometry_summary["mirrored_cube_count"],
        "mirrored_uv_strategy": geometry_summary["mirrored_uv_strategy"],
        "exact_bone_names": geometry_summary["exact_bone_names"],
        "bone_draw_order": geometry_summary["bone_draw_order"],
        "draw_order_evidence": geometry_summary["draw_order_evidence"],
        **({"undrawn_parts": geometry_summary["undrawn_parts"]} if "undrawn_parts" in geometry_summary else {}),
        **({"hierarchy": geometry_summary["hierarchy"]} if "hierarchy" in geometry_summary else {}),
        **({"cube_face_order": geometry_summary["cube_face_order"],
            "cube_face_order_evidence": geometry_summary["cube_face_order_evidence"]}
           if "cube_face_order" in geometry_summary else {}),
        **({"render_instances": geometry_summary["render_instances"]}
           if "render_instances" in geometry_summary else {}),
        "animation_contract": animation_contract,
        "reference_animation_schema": reference_schema,
        "accepted_animation_evidence": ACCEPTED_ANIMATION_EVIDENCE[spec["animation_kind"]],
        "artist_editable_math_to_keyframes_status": ARTIST_EDITABLE_STATUS[spec["animation_kind"]],
        "compiled_animation_bake_sample_count": len(compiled["animation_bake_samples"]),
        "reference_clip_keys_source": "compiled setupAnim output; no Python formula evaluation",
        "source_animation_channel_inventory": [
            {
                "bones": channel["bones"],
                "axis": channel["axis"],
                "source": channel["source"],
            }
            for channel in spec["channels"]
        ],
    }
    write_json(output_dir / f"{model_id}.conversion.json", report)


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--manifest", type=Path, required=True)
    parser.add_argument("--dump-dir", type=Path, required=True)
    parser.add_argument("--output-dir", type=Path, required=True)
    parser.add_argument("--continue-on-refusal", action="store_true",
                        help="convert every entry the converter accepts and NAME each one it refuses (G1 CONVERT REFUSED, and "
                             "<output-dir>/refusals.json) instead of stopping at the first; exit 0. The reference manifest's "
                             "mode (gradle referenceConvertModels: the artist package's rigs): an entry drawn from shared parts "
                             "without the render_instances form is refused, the rest still convert. The proof chains keep the "
                             "strict default (the first refusal stops the run, exit 1).")
    args = parser.parse_args()

    manifest = load_json(args.manifest)
    if manifest.get("schema_version") != 1:
        raise ValueError("unsupported G1 manifest schema")
    args.output_dir.mkdir(parents=True, exist_ok=True)
    specs = [*manifest["models"], *manifest.get("fixtures", [])]
    if "ticks_per_second" not in manifest:
        # The standing reference manifest (tools/reference_model_proofs.json: every port model against its 1.7.10
        # source, geometry only) never carried `ticks_per_second` - the value feeds clip sampling, which a static
        # entry has none of. The full folder converts that manifest for the artist package's rigs (gradle
        # referenceConvertModels), so a manifest without the key is accepted when EVERY entry is static and
        # refused, naming the entries, when any would sample a clip.
        sampled = [spec["id"] for spec in specs if spec.get("animation_kind") != "static"]
        if sampled:
            raise ValueError(f"manifest has no ticks_per_second but declares non-static models: {sampled}")
        manifest = dict(manifest, ticks_per_second=20.0)
    expected_ids = {spec["id"] for spec in specs}
    generated_suffixes = (
        ".geo.json", ".animation.json", ".animation-contract.json",
        ".conversion.json", ".poses.json",
    )
    for existing in args.output_dir.iterdir():
        if not existing.is_file() or not existing.name.endswith(generated_suffixes):
            continue
        if existing.name.endswith(".poses.json") or not any(
            existing.name.startswith(model_id + ".") for model_id in expected_ids
        ):
            existing.unlink()
    refusals: list[dict[str, Any]] = []
    for spec in specs:
        try:
            convert_model(manifest, spec, args.dump_dir, args.output_dir)
        except (ValueError, KeyError, FileNotFoundError) as exc:
            if not args.continue_on_refusal:
                raise
            # a refused entry leaves no output behind (a geo from an earlier, accepted form would be stale)
            for stale in args.output_dir.iterdir():
                if stale.is_file() and stale.name.startswith(spec["id"] + ".") and stale.name.endswith(generated_suffixes):
                    stale.unlink()
            reason = f"{type(exc).__name__}: {exc}"
            refusals.append({"id": spec["id"], "class": spec.get("class"), "reason": reason})
            print(f"G1 CONVERT REFUSED: {spec['id']} - {reason}")
            continue
        print(f"G1 CONVERT GREEN: {spec['id']} compiled LayerDefinition -> geo + animation contract")
    if args.continue_on_refusal:
        write_json(args.output_dir / "refusals.json", {
            "schema_version": 1, "manifest": args.manifest.name, "converted": len(specs) - len(refusals),
            "refused": refusals,
        })
        print(f"G1 CONVERT: {len(specs) - len(refusals)} of {len(specs)} entries converted, {len(refusals)} refused "
              f"(named above and in {args.output_dir / 'refusals.json'})")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
