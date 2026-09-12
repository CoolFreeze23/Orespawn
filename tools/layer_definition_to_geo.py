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
# NeoForge 21.1.223 ModelPart.Cube.<init> fills its polygon array DOWN, UP, WEST, NORTH, EAST, SOUTH
# (offsets 365-785: DOWN 365, UP 436, WEST 507, NORTH 578, EAST 649, SOUTH 720, each slot ending in its
# Polygon.<init> and aastore) and compile emits them in that order; Polygon.<init> takes the normal from the
# direction's step and, for a mirrored cube, negates its X (the WEST polygon then faces +X). GeckoLib
# 4.8.4 BakedModelFactory.buildQuads builds WEST, EAST, NORTH, SOUTH, UP, DOWN (offsets 20-130) and
# stamps each quad with its direction; internal space is classic space reflected in Y, so a classic
# normal (x, y, z) is the quad GeckoLib labels by the direction whose step is (x, -y, z).
CLASSIC_FACE_ORDER = ("down", "up", "west", "north", "east", "south")
CLASSIC_FACE_NORMAL = {
    "down": (0.0, -1.0, 0.0), "up": (0.0, 1.0, 0.0), "west": (-1.0, 0.0, 0.0),
    "north": (0.0, 0.0, -1.0), "east": (1.0, 0.0, 0.0), "south": (0.0, 0.0, 1.0),
}
GECKOLIB_LABEL_BY_CLASSIC_NORMAL = {
    (-1.0, 0.0, 0.0): "west", (1.0, 0.0, 0.0): "east", (0.0, 0.0, -1.0): "north",
    (0.0, 0.0, 1.0): "south", (0.0, -1.0, 0.0): "up", (0.0, 1.0, 0.0): "down",
}


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

    The converter's X-origin basis change means GeckoLib box UV assigns the two
    X face islands opposite Mojang's physical faces. GeckoLib's native mirror
    additionally moves its UP/DOWN vertices without moving their normals. Both
    differences are visible in the pinned bakers' captured vertex streams.

    We therefore bake the source mirror semantics into six face rectangles and
    leave GeckoLib's incompatible native mirror disabled. ``modelpart_mirror``
    remains in the cube as provenance. With native mirror false,
    ``GeoQuad.build`` swaps the supplied U endpoints; the signs below make the
    resulting position/normal/UV tuples identical to the baked ModelPart tuples.
    """
    u, v = (float(value) for value in cube["uv"])
    size_x, size_y, size_z = (float(value) for value in cube["size"])
    mirrored = bool(cube["mirror"])

    def face(min_u: float, min_v: float, width: float, height: float,
             flip_v: bool = False) -> dict[str, list[float | int]]:
        origin_u = min_u if mirrored else min_u + width
        signed_width = width if mirrored else -width
        origin_v = min_v + height if flip_v else min_v
        signed_height = -height if flip_v else height
        return {
            "uv": clean_vector([origin_u, origin_v]),
            "uv_size": clean_vector([signed_width, signed_height]),
        }

    negative_x_u = u + size_z + size_x if mirrored else u
    positive_x_u = u if mirrored else u + size_z + size_x
    return {
        "west": face(negative_x_u, v + size_z, size_z, size_y),
        "east": face(positive_x_u, v + size_z, size_z, size_y),
        "north": face(u + size_z, v + size_z, size_x, size_y),
        "south": face(u + 2.0 * size_z + size_x, v + size_z, size_x, size_y),
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

    # F(x,y,z) = (x, 24-y, z) is the fixed ModelPart Y-down -> Bedrock Y-up
    # basis change. GeckoLib's pinned baker negates (origin.x + size.x), so
    # this x origin makes its baked vertex positions land on ModelPart x.
    geo_origin = [
        -(absolute_origin[0] + size[0]),
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
    """ModelPart (x, y, z) radians -> Bedrock degrees; see the bind-rotation note in convert_geometry."""
    return clean_vector(
        [
            math.degrees(classic_radians[0]),
            -math.degrees(classic_radians[1]),
            -math.degrees(classic_radians[2]),
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


def expand_render_instances(name: str, part: dict[str, Any], parent: str | None,
                            absolute_pivot: list[float], initial_rotation: list[float],
                            declaration: dict[str, Any]) -> dict[str, Any]:
    """Slice 4c: one bone per DRAW of a part the classic model renders N times per frame.

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
    axis = declaration["axis"]
    if axis not in AXIS_INDEX:
        raise ValueError(f"render_instances.{name}.axis {axis!r} is not x, y or z")
    scope = declaration["step_scope"]
    if scope not in ("part", "stack"):
        raise ValueError(f"render_instances.{name}.step_scope {scope!r} is not part or stack")
    step, angles = render_instance_angles(name, declaration)
    axis_index = AXIS_INDEX[axis]
    pivot_json = clean_vector([-absolute_pivot[0], 24.0 - absolute_pivot[1], absolute_pivot[2]])
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


def derive_bone_draw_order(compiled: dict[str, Any],
                           bones: list[dict[str, Any]]) -> tuple[list[str], dict[str, Any]]:
    """G2 root-order contract: the geo bones in the order the classic renderer draws the parts.

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

    def emit(level: list[str]) -> None:
        for name in sorted(level, key=lambda bone: (first_rank(bone), emission_rank[bone])):
            ranks = sorted(subtree_ranks(name))
            if name in rank and any(value < rank[name] for value in ranks):
                raise ValueError(
                    f"FINDING {compiled['model_id']}: {name} is drawn after one of its descendants; "
                    "a pre-order bone traversal cannot express that"
                )
            if ranks and ranks[-1] - ranks[0] + 1 != len(ranks):
                raise ValueError(
                    f"FINDING {compiled['model_id']}: the draws of {name}'s subtree are interleaved with "
                    "another bone's; the bone tree cannot express that order"
                )
            ordered.append(name)
            emit(children_of.get(name, []))

    emit(children_of.get(None, []))
    if [name for name in ordered if name in rank] != [unit for unit in total if unit in rank]:
        raise ValueError(f"{compiled['model_id']}: pre-order lifting changed the merged draw order")
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
    return ordered, evidence


def classic_face_labels(mirror: bool) -> list[str]:
    """The six GeckoLib direction names in the order ModelPart.Cube.compile emits the classic faces
    (CLASSIC_FACE_ORDER; a mirrored cube's WEST / EAST polygons carry the negated X normals)."""
    labels = []
    for face in CLASSIC_FACE_ORDER:
        normal = CLASSIC_FACE_NORMAL[face]
        if mirror and face in ("west", "east"):
            normal = (-normal[0], normal[1], normal[2])
        labels.append(GECKOLIB_LABEL_BY_CLASSIC_NORMAL[normal])
    return labels


def classic_face_normals(mirror: bool) -> list[tuple[float, float, float]]:
    """The classic-space normals of those six faces, in emission order."""
    normals = []
    for face in CLASSIC_FACE_ORDER:
        normal = CLASSIC_FACE_NORMAL[face]
        if mirror and face in ("west", "east"):
            normal = (-normal[0], normal[1], normal[2])
        normals.append(normal)
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
                "the classic normal reflected in Y",
        "cube_bearing_bones": len(order),
        "cubes": sum(len(cubes) for cubes in order.values()),
        "verified_unrotated_cubes_at_bind": verified,
        "verification": "captured normals of every cube unrotated at bind equal the rule's, in order; the parity "
                        "tool's draw-order leg compares the per-quad normal sequences of both renderers on every capture",
    }
    return order, evidence


def convert_geometry(compiled: dict[str, Any],
                     render_instances: dict[str, Any] | None = None,
                     cube_face_order: str | None = None) -> tuple[dict[str, Any], dict[str, Any]]:
    root = compiled["definition"]
    if root["cubes"]:
        raise ValueError("unnamed MeshDefinition root contains cubes")

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
    for part, parent in iter_parts(root):
        name = part["name"]
        absolute_pivot = [float(value) for value in part["absolute_pivot"]]
        initial_rotation = [float(value) for value in part["initial_rotation_radians"]]
        ancestors[name] = ([] if parent is None else ancestors[parent] + [parent])
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
                group_entry = expanded["mapping"][entry["group_bone"]]
                group_static = group_entry.get("static_rotation_radians")
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
            "pivot": clean_vector(
                [-absolute_pivot[0], 24.0 - absolute_pivot[1], absolute_pivot[2]]
            ),
        }
        if parent is not None:
            bone["parent"] = parent
        if nonzero(initial_rotation):
            # GeckoLib 4.8.4's BakedModelFactory negates JSON X/Y but not Z.
            # Conjugating ModelPart rotations through the Y reflection needs
            # internal (-X,+Y,-Z), hence JSON (+X,-Y,-Z).
            bone["rotation"] = clean_vector(
                [
                    math.degrees(initial_rotation[0]),
                    -math.degrees(initial_rotation[1]),
                    -math.degrees(initial_rotation[2]),
                ]
            )
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
        expected_names = sorted((set(input_names) - set(render_instances)) | set(expansion_bones))
    else:
        expected_names = input_names
    if output_names != expected_names:
        raise ValueError(f"bone-name drift: expected {expected_names} != output {output_names}")
    if len(output_names) != len(set(output_names)):
        raise ValueError("duplicate bone names cannot be preserved by GeckoLib")

    model_id = compiled["model_id"]
    bone_draw_order, draw_order_evidence = derive_bone_draw_order(compiled, bones)
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
    return geometry, summary


def initial_rotations(compiled: dict[str, Any]) -> dict[str, list[float]]:
    bind = next(sample for sample in compiled["samples"] if sample["id"] == "bind")
    return {
        bone: [float(value) for value in transform["rotation"]]
        for bone, transform in bind["transforms"].items()
    }


def json_rotation_delta(target: list[float], initial: list[float]) -> list[float]:
    delta = [target[index] - initial[index] for index in range(3)]
    return [
        math.degrees(delta[0]),
        -math.degrees(delta[1]),
        -math.degrees(delta[2]),
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
        "OreSpawnGeoReplacementModel.setCustomAnimations (Amendment 1 Tier-3 code-driven)"
    ),
    "entity_state": (
        "production OreSpawnGeoReplacement hook posed from declared entity states through the "
        "entity's pose interface (Amendment 1 Tier-3 code-driven)"
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
        # Amendment 1 Tier-3: the pose is the production OreSpawnGeoReplacement
        # hook (code_driven, driven headlessly by the probe) or, when the classic
        # model reads its entity, the same hook posed from declared entity states
        # through the entity's pose interface (entity_state). No clip is emitted
        # or accepted.
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
                "(Amendment 1 Tier-3 code-driven)"
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
    geometry, geometry_summary = convert_geometry(compiled, render_instances, spec.get("cube_face_order"))
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
    args = parser.parse_args()

    manifest = load_json(args.manifest)
    if manifest.get("schema_version") != 1:
        raise ValueError("unsupported G1 manifest schema")
    args.output_dir.mkdir(parents=True, exist_ok=True)
    specs = [*manifest["models"], *manifest.get("fixtures", [])]
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
    for spec in specs:
        convert_model(manifest, spec, args.dump_dir, args.output_dir)
        print(f"G1 CONVERT GREEN: {spec['id']} compiled LayerDefinition -> geo + animation contract")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
