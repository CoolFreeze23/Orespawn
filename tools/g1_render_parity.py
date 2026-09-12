#!/usr/bin/env python3
"""Independent Phase G1 geometry, animation, and visual parity harness."""

from __future__ import annotations

import argparse
import hashlib
import json
import math
from pathlib import Path
import shutil
from typing import Any, Iterable

from PIL import Image


IMAGE_SIZE = 256
BACKGROUND = (18, 20, 24, 255)
# rendertype_entity_cutout.fsh: `if (color.a < 0.1) discard;` -> 0.1 * 255 on 8-bit alpha.
CUTOUT_ALPHA_THRESHOLD = 25.5
# Fragments of different quads within this depth of each other are a z-fight, resolved by
# draw order in both renderers. Ruling 2 (2026-09-02) excluded them from parity and pinned
# the excluded fraction per species; the G2 root-order contract (landed 2026-09-06) makes
# the draw order equal on both sides, so every pixel is compared and the contested
# fraction is a diagnostic only (nonzero with 0 changed pixels is the ordinary case).
# `--contested-exclusion` restores the exclusion for diagnostics; it never writes a proof.
CONTEST_DEPTH_EPSILON = 1.0e-6
# Manifest fields of the retired ruling-2 policy: a manifest that still carries one fails,
# so a pin cannot linger silently (the owner's ruling 2026-09-06 removed all fifteen).
RETIRED_MANIFEST_FIELDS = ("max_contested_fraction_pin", "in_game_acceptance")
CONTESTED_MARKER = (40, 90, 255, 255)
# G2 root-order contract: the geo description key the converter writes and the shipped model applies.
DRAW_ORDER_KEY = "orespawn:bone_draw_order"
# ENT-S-146: the within-cube face order key (bone -> one array per cube of GeckoLib direction names in
# draw order), written for a translucent rig and applied by the shipped model and the harness.
FACE_ORDER_KEY = "orespawn:cube_face_order"
DEFAULT_VISUAL_SAMPLE_IDS = ("bind", "t0", "t_quarter", "t_half", "t_three_quarter")
# ENT-S-146: the per-model visual modes the rasteriser emulates, keyed by the RenderType both
# renderers draw with (the manifest's `visual_mode.render_type`); the GPU states are the ones the
# NeoForge 21.1.223 RenderType builders set (bytecode-cited). The default, `entity_cutout_no_cull`,
# is the verbatim path every landed model is proven under; a model declares another mode in its
# manifest entry and the tool refuses anything it does not emulate. Both modes are NO_CULL, and that
# is load-bearing (refuter B, D4): the candidate emits every quad as the REVERSED vertex cycle of the
# classic's - ModelPart.Polygon.<init> reverses a mirrored cube's vertices, GeckoLib's buildQuads bakes the
# converter's explicit, unmirrored faces - so the two sides' windings are opposite (measured 2026-09-06:
# 108 of 108 quads per PurplePower capture, a [3, 2, 1, 0] reversal). Invisible under NO_CULL; a future
# mode with face culling would draw the two sides' different faces, and must not be added here without
# a winding leg.
VISUAL_MODES = {
    "entity_cutout_no_cull": {
        # RenderType.lambda$static$3: NO_TRANSPARENCY (22-25), NO_CULL (28-31); builder defaults
        # LEQUAL_DEPTH_TEST / COLOR_DEPTH_WRITE (CompositeStateBuilder.<init> 26-29 / 75-78).
        "blend": None,
        "depth_test": "LEQUAL",
        "depth_write": True,
        "cull": False,
        # rendertype_entity_cutout_no_cull.fsh: `if (color.a < 0.1) discard;` on the TEXTURE alpha.
        "alpha_discard": CUTOUT_ALPHA_THRESHOLD / 255.0,
        "rasteriser": "render_capture",
    },
    "entity_translucent": {
        # RenderType.lambda$static$7: TRANSLUCENT_TRANSPARENCY (22-25), NO_CULL (28-31); the same builder
        # defaults, so the depth test is LEQUAL and the depth IS written (COLOR_DEPTH_WRITE = new
        # WriteMaskStateShard(true, true), RenderStateShard.<clinit> 1179-1188). RenderStateShard
        # .lambda$static$10 (TRANSLUCENT_TRANSPARENCY's setup): enableBlend; blendFuncSeparate
        # (SRC_ALPHA, ONE_MINUS_SRC_ALPHA, ONE, ONE_MINUS_SRC_ALPHA).
        "blend": ["SRC_ALPHA", "ONE_MINUS_SRC_ALPHA", "ONE", "ONE_MINUS_SRC_ALPHA"],
        "depth_test": "LEQUAL",
        "depth_write": True,
        "cull": False,
        # rendertype_entity_translucent.fsh: the same `if (color.a < 0.1) discard;` on the texture alpha,
        # BEFORE `color *= vertexColor` - the vertex alpha never triggers the discard.
        "alpha_discard": CUTOUT_ALPHA_THRESHOLD / 255.0,
        "rasteriser": "render_capture_blended",
    },
}
DEFAULT_VERTEX_COLOR = [255, 255, 255, 255]
# Phase G, the controller's return (Amendment 1 points 3-5, ADDENDA (1)-(2), item 24 (5)-(14), 2026-09-06):
# a gait_scaled model may declare a keyframe reference leg - the shipped phase-locked keyframe layers over
# the species' regenerated clip, sampled by the probe at every schedule request plus the wrap pairs it adds
# to BOTH sides (`..._kfwrap_<frequency>_c<cycle>_before` / `_after`, T - eps vs 0 + eps at every seam the
# float phase straddles) and compared here with the compiled setupAnim at its own NAMED tolerance
# (`thresholds.keyframe_reference_leg_epsilon_radians`, an owner ruling: 2.5e-3 rad; no default). The
# density - keys per bone per clip - is an OUTPUT: read from what GeckoLib baked, checked against the
# probe's density search (the fewest keys holding the tolerance, one fewer failing) and stated beside the
# tolerance and the lerp mode in the ruled wording. A model without the block is untouched.
KEYFRAME_LEG_KEY = "keyframe_reference_leg"
KEYFRAME_LEG_SAMPLE_FIELD = "keyframe_rotations"
KEYFRAME_LEG_TOLERANCE_KEY = "keyframe_reference_leg_epsilon_radians"
KEYFRAME_WRAP_TOKEN = "_kfwrap_"
# The probe writes each wrap sample's frequency group beside its id (KeyframeLeg.WRAP_FIELD); the tool matches on
# that record and never recomputes a token from the manifest's number (item 15 refuter B, D10).
KEYFRAME_WRAP_FIELD = "keyframe_wrap"


def load_json(path: Path) -> dict[str, Any]:
    with path.open("r", encoding="utf-8") as handle:
        return json.load(handle)


def json_bytes(value: Any) -> bytes:
    return (json.dumps(value, indent=2, ensure_ascii=False) + "\n").encode("utf-8")


def write_bytes(path: Path, data: bytes) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_bytes(data)


def sha256(path: Path) -> str:
    return hashlib.sha256(path.read_bytes()).hexdigest()


def sample_map(document: dict[str, Any]) -> dict[str, dict[str, Any]]:
    return {sample["id"]: sample for sample in document["samples"]}


def full_sample_map(document: dict[str, Any]) -> dict[str, dict[str, Any]]:
    return {
        sample["id"]: sample
        for sample in document["samples"]
        if sample.get("capture_kind") == "full"
    }


def sample_id(fraction: float) -> str:
    known = {
        0.0: "t0",
        0.25: "t_quarter",
        0.5: "t_half",
        0.75: "t_three_quarter",
        1.0: "t_end",
    }
    for value, name in known.items():
        if abs(fraction - value) < 1.0e-9:
            return name
    return f"t_{fraction:.6f}".replace(".", "_")


def amplitude_sample_id(amplitude: float, fraction: float) -> str:
    amount = f"{amplitude:.6f}".rstrip("0").rstrip(".")
    amount = amount.replace("-", "n").replace(".", "_")
    return f"a{amount}_{sample_id(fraction)}"


def cube_map(sample: dict[str, Any]) -> dict[tuple[str, int], dict[str, Any]]:
    result: dict[tuple[str, int], dict[str, Any]] = {}
    for cube in sample["cubes"]:
        key = (cube["bone"], int(cube["cube_index"]))
        if key in result:
            raise AssertionError(f"duplicate captured cube {key}")
        result[key] = cube
    return result


def assert_same_cube_set(model_id: str, sample_name: str,
                         vanilla_cubes: dict[tuple[str, int], Any],
                         geo_cubes: dict[tuple[str, int], Any]) -> None:
    """Both probes must draw the same cubes: a hidden bone on one side only is a visibility bug."""
    if vanilla_cubes.keys() != geo_cubes.keys():
        missing = sorted(set(vanilla_cubes) - set(geo_cubes))
        extra = sorted(set(geo_cubes) - set(vanilla_cubes))
        raise AssertionError(
            f"VISIBILITY SET MISMATCH {model_id}/{sample_name}: cubes only in compiled {missing}; "
            f"only in GeckoLib {extra}"
        )


def vertex_position(vertex: dict[str, Any]) -> tuple[float, float, float]:
    return tuple(float(value) for value in vertex["position"])  # type: ignore[return-value]


def vertex_normal(vertex: dict[str, Any]) -> tuple[float, float, float]:
    return tuple(float(value) for value in vertex["normal"])  # type: ignore[return-value]


def vertex_uv(vertex: dict[str, Any]) -> tuple[float, float]:
    return tuple(float(value) for value in vertex["uv"])  # type: ignore[return-value]


def unique_corners(cube: dict[str, Any]) -> list[tuple[float, float, float]]:
    by_rounded: dict[tuple[float, float, float], tuple[float, float, float]] = {}
    for vertex in cube["vertices"]:
        position = vertex_position(vertex)
        by_rounded.setdefault(tuple(round(value, 7) for value in position), position)
    return list(by_rounded.values())


def distance(left: tuple[float, float, float], right: tuple[float, float, float]) -> float:
    return math.sqrt(sum((left[index] - right[index]) ** 2 for index in range(3)))


def hausdorff(left: list[tuple[float, float, float]],
              right: list[tuple[float, float, float]]) -> float:
    if len(left) != len(right):
        raise AssertionError(f"corner count mismatch {len(left)} != {len(right)}")
    if not left:
        return 0.0
    forward = max(min(distance(point, candidate) for candidate in right) for point in left)
    reverse = max(min(distance(point, candidate) for candidate in left) for point in right)
    return max(forward, reverse)


def geometry_parity(model_id: str, compiled: dict[str, Any], geo_render: dict[str, Any],
                    epsilon: float) -> dict[str, Any]:
    vanilla_samples = full_sample_map(compiled)
    geo_samples = full_sample_map(geo_render)
    if vanilla_samples.keys() != geo_samples.keys():
        raise AssertionError(
            f"{model_id} sample IDs differ: {sorted(vanilla_samples)} != {sorted(geo_samples)}"
        )

    max_delta = 0.0
    compared_cubes = 0
    compared_samples = 0
    worst = ""
    for sample_id in vanilla_samples:
        vanilla_cubes = cube_map(vanilla_samples[sample_id])
        geo_cubes = cube_map(geo_samples[sample_id])
        assert_same_cube_set(model_id, sample_id, vanilla_cubes, geo_cubes)
        if vanilla_cubes.keys() != geo_cubes.keys():
            missing = sorted(vanilla_cubes.keys() - geo_cubes.keys())
            extra = sorted(geo_cubes.keys() - vanilla_cubes.keys())
            raise AssertionError(
                f"{model_id}/{sample_id} cube identity mismatch; missing={missing}, extra={extra}"
            )
        for key in vanilla_cubes:
            delta = hausdorff(
                unique_corners(vanilla_cubes[key]),
                unique_corners(geo_cubes[key]),
            )
            if delta > max_delta:
                max_delta = delta
                worst = f"{sample_id}:{key[0]}#{key[1]}"
            if delta > epsilon:
                raise AssertionError(
                    f"CONVERSION MISMATCH {model_id}/{sample_id}/{key[0]}#{key[1]}: "
                    f"corner delta {delta:.12g} > epsilon {epsilon:.12g}"
                )
            compared_cubes += 1
        compared_samples += 1

    return {
        "status": "PASS",
        "epsilon_blocks": epsilon,
        "max_corner_delta_blocks": max_delta,
        "worst_case": worst or "exact",
        "sample_count": compared_samples,
        "cube_sample_count": compared_cubes,
    }


def surface_mapping_parity(model_id: str, compiled: dict[str, Any],
                           geo_render: dict[str, Any], position_epsilon: float,
                           normal_epsilon: float, uv_epsilon: float) -> dict[str, Any]:
    """Compare baked position/normal/UV tuples without relying on quad order."""
    vanilla_samples = full_sample_map(compiled)
    geo_samples = full_sample_map(geo_render)
    max_position_delta = 0.0
    max_normal_delta = 0.0
    max_uv_delta = 0.0
    vertex_count = 0
    worst = "exact"

    ignored_zero_area_faces = 0

    for sample_name, vanilla_sample in vanilla_samples.items():
        vanilla_cubes = cube_map(vanilla_sample)
        geo_cubes = cube_map(geo_samples[sample_name])
        assert_same_cube_set(model_id, sample_name, vanilla_cubes, geo_cubes)
        for key, vanilla_cube in vanilla_cubes.items():
            # Ruling 2026-09-02 (Vortex): faces of EXACTLY zero area draw nothing,
            # and GeckoLib's baker collapses a flat cube's degenerate faces, so
            # they are excluded from vertex pairing on both sides and counted.
            vanilla_vertices, ignored_vanilla = drop_zero_area_faces(vanilla_cube["vertices"])
            geo_vertices, _ignored_geo = drop_zero_area_faces(geo_cubes[key]["vertices"])
            ignored_zero_area_faces += ignored_vanilla
            remaining = list(geo_vertices)
            for vanilla_vertex in vanilla_vertices:
                position = vertex_position(vanilla_vertex)
                normal = vertex_normal(vanilla_vertex)
                candidates: list[tuple[float, float, float, int]] = []
                for index, candidate in enumerate(remaining):
                    position_delta = distance(position, vertex_position(candidate))
                    normal_delta = distance(normal, vertex_normal(candidate))
                    if position_delta <= position_epsilon and normal_delta <= normal_epsilon:
                        uv_delta = distance(
                            (vertex_uv(vanilla_vertex)[0], vertex_uv(vanilla_vertex)[1], 0.0),
                            (vertex_uv(candidate)[0], vertex_uv(candidate)[1], 0.0),
                        )
                        candidates.append((uv_delta, position_delta, normal_delta, index))
                if not candidates:
                    raise AssertionError(
                        f"RENDERER MAPPING MISMATCH {model_id}/{sample_name}/{key[0]}#{key[1]}: "
                        f"no GeoRenderer vertex matches position {position} and normal {normal}"
                    )
                uv_delta, position_delta, normal_delta, candidate_index = min(candidates)
                remaining.pop(candidate_index)
                if uv_delta > uv_epsilon:
                    raise AssertionError(
                        f"UV MISMATCH {model_id}/{sample_name}/{key[0]}#{key[1]}: "
                        f"normalized UV delta {uv_delta:.12g} > epsilon {uv_epsilon:.12g}"
                    )
                if max(position_delta, normal_delta, uv_delta) > max(
                    max_position_delta, max_normal_delta, max_uv_delta
                ):
                    worst = f"{sample_name}:{key[0]}#{key[1]}"
                max_position_delta = max(max_position_delta, position_delta)
                max_normal_delta = max(max_normal_delta, normal_delta)
                max_uv_delta = max(max_uv_delta, uv_delta)
                vertex_count += 1
            if remaining:
                raise AssertionError(
                    f"RENDERER MAPPING MISMATCH {model_id}/{sample_name}/{key[0]}#{key[1]}: "
                    f"{len(remaining)} unmatched GeoRenderer vertices"
                )

    return {
        "status": "PASS",
        "position_epsilon_blocks": position_epsilon,
        "normal_epsilon": normal_epsilon,
        "uv_epsilon_normalized": uv_epsilon,
        "max_position_delta_blocks": max_position_delta,
        "max_normal_delta": max_normal_delta,
        "max_uv_delta_normalized": max_uv_delta,
        "vertex_samples": vertex_count,
        "ignored_zero_area_faces": ignored_zero_area_faces,
        "worst_case": worst,
        "evidence": (
            "position/normal/UV tuples from baked ModelPart.Cube.compile versus "
            "pinned GeckoLib BakedModelFactory rendered through GeoRenderer; "
            "faces of exactly zero area are excluded (ruling 2026-09-02)"
        ),
    }


def face_area(quad: list[dict[str, Any]]) -> float:
    """Exact-arithmetic-free but exact-zero-safe: sum of the two triangle cross products."""
    a, b, c, d = (vertex_position(vertex) for vertex in quad)

    def cross_norm2(o, p, q):
        ux, uy, uz = p[0] - o[0], p[1] - o[1], p[2] - o[2]
        vx, vy, vz = q[0] - o[0], q[1] - o[1], q[2] - o[2]
        cx, cy, cz = uy * vz - uz * vy, uz * vx - ux * vz, ux * vy - uy * vx
        return cx * cx + cy * cy + cz * cz

    return cross_norm2(a, b, c) + cross_norm2(a, c, d)


def drop_zero_area_faces(vertices: list[dict[str, Any]]) -> tuple[list[dict[str, Any]], int]:
    """Remove quads whose area is exactly zero; returns (kept vertices, dropped face count)."""
    if len(vertices) % 4:
        raise AssertionError("captured cube vertex count is not quad-aligned")
    kept: list[dict[str, Any]] = []
    dropped = 0
    for offset in range(0, len(vertices), 4):
        quad = vertices[offset:offset + 4]
        if face_area(quad) == 0.0:
            dropped += 1
            continue
        kept.extend(quad)
    return kept, dropped


def vector_delta(left: Iterable[float], right: Iterable[float]) -> float:
    return max(abs(float(a) - float(b)) for a, b in zip(left, right))


AXIS_INDEX = {"x": 0, "y": 1, "z": 2}


def cube_face_order_parity(model_id: str, compiled: dict[str, Any], geo_render: dict[str, Any],
                           generated_geometry: dict[str, Any], conversion: dict[str, Any],
                           normal_epsilon: float) -> dict[str, Any] | None:
    """ENT-S-146: the within-cube face order, for a rig that ships one (``FACE_ORDER_KEY``).

    Four things must agree: the order the converter derived (``conversion.json``), the order shipped
    inside the geo (what production reads), the order the geo probe read and applied
    (``cube_face_order``) and the quad order a fresh bake actually has after the production
    ``FaceOrder.apply`` (``baked_cube_face_order``). Then, capture by capture and cube by cube, the
    sequence of face normals the classic ``ModelPart.Cube.compile`` emitted must equal the sequence
    ``GeoRenderer.renderCube`` emitted - the direct proof that the candidate blends a cube's faces in
    the classic order. None for a rig without the key (an opaque rig: the order is invisible there
    except at a coplanar tie, the open item recorded on the G2 contract).
    """
    description = generated_geometry["minecraft:geometry"][0]["description"]
    shipped = description.get(FACE_ORDER_KEY)
    derived = conversion.get("cube_face_order")
    applied = geo_render.get("cube_face_order")
    if shipped is None and derived is None and applied is None:
        return None
    if shipped != derived:
        raise AssertionError(f"{model_id} converter report and geo disagree on {FACE_ORDER_KEY}")
    if applied != shipped:
        raise AssertionError(f"{model_id} GeckoLib probe applied a different face order than the geo carries")
    baked = geo_render.get("baked_cube_face_order")
    if baked != shipped:
        raise AssertionError(
            f"FACE ORDER MISMATCH {model_id}: a fresh GeckoLib bake draws {baked} after FaceOrder.apply, "
            f"not the contracted {shipped}"
        )
    vanilla_samples = full_sample_map(compiled)
    geo_samples = full_sample_map(geo_render)
    faces_checked = 0
    for sample_id, vanilla_sample in vanilla_samples.items():
        vanilla_cubes = cube_map(vanilla_sample)
        geo_cubes = cube_map(geo_samples[sample_id])
        assert_same_cube_set(model_id, sample_id, vanilla_cubes, geo_cubes)
        for key, vanilla_cube in vanilla_cubes.items():
            classic = [vertex_normal(vertex) for vertex in vanilla_cube["vertices"][::4]]
            gecko = [vertex_normal(vertex) for vertex in geo_cubes[key]["vertices"][::4]]
            if len(classic) != len(gecko) or any(
                    vector_delta(left, right) > normal_epsilon for left, right in zip(classic, gecko)):
                raise AssertionError(
                    f"FACE ORDER MISMATCH {model_id}/{sample_id}/{key[0]}#{key[1]}: classic Cube.compile emitted "
                    f"faces with normals {classic}; GeoRenderer.renderCube emitted {gecko}"
                )
            faces_checked += len(classic)
    if faces_checked == 0:
        raise AssertionError(f"FACE ORDER UNEVIDENCED {model_id}: no full capture compared a cube's faces")
    return {
        "status": "PASS",
        "cube_face_order": shipped,
        "captures_checked": len(vanilla_samples),
        "faces_checked": faces_checked,
        "normal_epsilon": normal_epsilon,
        "evidence": conversion.get("cube_face_order_evidence"),
        "policy": (
            "the shipped model permutes every cube's quads into the classic ModelPart.Cube order (FaceOrder"
            ".apply), so under a blending render type a cube's back face shows through its front face - or "
            "is rejected by the depth test - exactly as it does on the classic renderer"
        ),
    }


def draw_order_parity(model_id: str, compiled: dict[str, Any], geo_render: dict[str, Any],
                      generated_geometry: dict[str, Any], conversion: dict[str, Any],
                      normal_epsilon: float = 1.0e-6) -> dict[str, Any]:
    """G2 root-order contract: GeckoLib draws the bones in the classic part order.

    Three things must agree: the order the converter derived from the classic captures
    (``conversion.json``), the order shipped inside the geo (``description[DRAW_ORDER_KEY]``,
    what production reads), and the traversal a fresh GeckoLib bake actually has after the
    production ``DrawOrder.apply`` (``baked_bone_order``). Then, capture by capture, the
    sequence of parts the classic ``renderToBuffer`` drew (``draw_order``, attributed by
    skipDraw elimination in the probe) must equal the sequence of bones ``GeoRenderer``
    emitted cubes for. ENT-S-146: a rig shipping a within-cube face order is checked by
    ``cube_face_order_parity`` as well (reported under ``cube_face_order``).
    """
    description = generated_geometry["minecraft:geometry"][0]["description"]
    order = description.get(DRAW_ORDER_KEY)
    if not isinstance(order, list) or not order:
        raise AssertionError(f"{model_id} generated geo carries no {DRAW_ORDER_KEY}")
    if order != conversion.get("bone_draw_order"):
        raise AssertionError(f"{model_id} converter report and geo disagree on {DRAW_ORDER_KEY}")
    if sorted(order) != sorted(conversion["exact_bone_names"]):
        raise AssertionError(f"{model_id} {DRAW_ORDER_KEY} is not a permutation of the exact bone names")
    if geo_render.get("bone_draw_order") != order:
        raise AssertionError(f"{model_id} GeckoLib probe applied a different draw order than the geo carries")
    baked = geo_render.get("baked_bone_order")
    if baked != order:
        raise AssertionError(
            f"DRAW ORDER MISMATCH {model_id}: a fresh GeckoLib bake traverses {baked} after DrawOrder.apply, "
            f"not the contracted {order}"
        )
    vanilla_samples = full_sample_map(compiled)
    geo_samples = full_sample_map(geo_render)
    if vanilla_samples.keys() != geo_samples.keys():
        raise AssertionError(f"{model_id} full capture sets differ between the probes")
    draws_checked = 0
    for sample_id, vanilla_sample in vanilla_samples.items():
        classic = vanilla_sample.get("draw_order")
        gecko = geo_samples[sample_id].get("draw_order")
        if classic is None or gecko is None:
            raise AssertionError(f"{model_id}/{sample_id} capture carries no draw_order")
        if classic != gecko:
            raise AssertionError(
                f"DRAW ORDER MISMATCH {model_id}/{sample_id}: classic renderToBuffer drew {classic}; "
                f"GeoRenderer drew {gecko}"
            )
        draws_checked += len(classic)
    evidence = conversion.get("draw_order_evidence", {})
    # Refuter B (2026-09-06): the leg must not pass vacuously — a rig with no full capture, or with units no capture
    # ever drew, would ship an order resting on the converter's emission tie-break instead of evidence.
    if not vanilla_samples:
        raise AssertionError(f"DRAW ORDER UNEVIDENCED {model_id}: no full capture observed the classic draw order")
    unobserved_units = evidence.get("unobserved_units") or []
    if unobserved_units:
        raise AssertionError(
            f"DRAW ORDER UNEVIDENCED {model_id}: units never drawn by any capture: {sorted(unobserved_units)}"
        )
    report = {
        "status": "PASS",
        "bone_draw_order": order,
        "captures_checked": len(vanilla_samples),
        "draws_checked": draws_checked,
        "classic_source": compiled.get("draw_order_source"),
        "unobserved_units": evidence.get("unobserved_units"),
        "policy": (
            "the shipped model sorts GeckoLib's topLevelBones / childBones lists into the classic "
            "renderToBuffer order (DrawOrder.apply); both renderers traverse in pre-order, so equal "
            "sibling orders are equal draw orders"
        ),
    }
    face_order = cube_face_order_parity(model_id, compiled, geo_render, generated_geometry, conversion, normal_epsilon)
    if face_order is not None:
        report["cube_face_order"] = face_order
    return report


def visual_mode(model_id: str, spec: dict[str, Any]) -> dict[str, Any] | None:
    """ENT-S-146: the model's declared visual mode, validated, or None for the verbatim default path.

    ``visual_mode``: ``render_type`` (a VISUAL_MODES key), ``vertex_color`` (the RGBA bytes every
    vertex carries - what the shader multiplies the texel by; the game quantises a float colour
    with Mth.floor(f * 255), so the manifest declares the bytes) and ``light`` (``world`` or
    ``full_bright``; documentary - the rasteriser applies no lightmap on either side, so its flat
    texel colour is the game's under full brightness and, for a world-lit model, the game's before
    the lightmap multiply that both renderers apply alike).
    """
    declared = spec.get("visual_mode")
    if declared is None:
        return None
    render_type = declared.get("render_type")
    if render_type not in VISUAL_MODES:
        raise AssertionError(f"{model_id} visual_mode.render_type {render_type!r} is not emulated: {sorted(VISUAL_MODES)}")
    colour = [int(value) for value in declared.get("vertex_color", DEFAULT_VERTEX_COLOR)]
    if len(colour) != 4 or any(value < 0 or value > 255 for value in colour):
        raise AssertionError(f"{model_id} visual_mode.vertex_color must be four RGBA bytes")
    light = declared.get("light", "world")
    if light not in ("world", "full_bright"):
        raise AssertionError(f"{model_id} visual_mode.light {light!r} is not world or full_bright")
    if render_type == "entity_cutout_no_cull" and colour != DEFAULT_VERTEX_COLOR:
        raise AssertionError(f"{model_id} a vertex colour under entity_cutout_no_cull is not emulated")
    return {
        "render_type": render_type,
        "vertex_color": colour,
        "light": light,
        "emulated_states": {key: value for key, value in VISUAL_MODES[render_type].items() if key != "rasteriser"},
        "rasteriser": VISUAL_MODES[render_type]["rasteriser"],
    }


def render_instance_expansion(conversion: dict[str, Any] | None) -> dict[str, Any] | None:
    """Slice 4c: the converter's clone/group mapping for a model with render_instances, else None."""
    if not conversion:
        return None
    return conversion.get("render_instances")


def candidate_bone_names(model_id: str, spec: dict[str, Any], compiled: dict[str, Any],
                         conversion: dict[str, Any]) -> list[str]:
    """The bone set the generated rig must carry: the compiled parts, with every render-instance
    part replaced by the converter's group and clone bones (Slice 4c)."""
    names = set(compiled["bone_names"])
    expansion = render_instance_expansion(conversion)
    declared = spec.get("render_instances")
    if compiled.get("render_instances") != declared:
        raise AssertionError(f"{model_id} render_instances drift between the manifest and the compiled dump")
    if expansion is None:
        if declared:
            raise AssertionError(f"{model_id} declares render_instances but the converter recorded no expansion")
        return sorted(names)
    if expansion.get("declared") != declared:
        raise AssertionError(f"{model_id} render_instances drift between the manifest and the converter")
    parts = expansion["parts"]
    if set(parts) != set(declared):
        raise AssertionError(f"{model_id} converter expanded {sorted(parts)} != declared {sorted(declared)}")
    missing = sorted(set(parts) - names)
    if missing:
        raise AssertionError(f"{model_id} render_instances names parts the compiled model lacks: {missing}")
    for bone, entry in expansion["bones"].items():
        if entry["source_part"] not in parts:
            raise AssertionError(f"{model_id} expansion bone {bone} maps to an undeclared part")
        if entry["role"] == "clone" and entry["group_bone"] not in expansion["bones"]:
            raise AssertionError(f"{model_id} clone {bone} names an unknown group bone")
    return sorted((names - set(parts)) | set(expansion["bones"]))


def definition_parts(root: dict[str, Any]) -> dict[str, dict[str, Any]]:
    parts: dict[str, dict[str, Any]] = {}

    def visit(part: dict[str, Any]) -> None:
        for child in part["children"]:
            parts[child["name"]] = child
            visit(child)

    visit(root)
    return parts


def matrix_rows(value: Any, what: str) -> list[list[float]]:
    rows = [[float(entry) for entry in row] for row in value]
    if len(rows) != 4 or any(len(row) != 4 for row in rows):
        raise AssertionError(f"{what}: not a 4x4 matrix")
    if vector_delta(rows[3], [0.0, 0.0, 0.0, 1.0]) > 1.0e-6:
        raise AssertionError(f"{what}: not an affine matrix (bottom row {rows[3]})")
    return rows


def matrix_translate(matrix: list[list[float]], offset: list[float]) -> list[list[float]]:
    """matrix * T(offset): the same linear part, the translation moved by the rotated offset."""
    result = [list(row) for row in matrix]
    for r in range(3):
        result[r][3] = matrix[r][3] + sum(matrix[r][c] * offset[c] for c in range(3))
    return result


def matrix_delta(left: list[list[float]], right: list[list[float]]) -> tuple[float, float]:
    """(max |delta| over the 3x3 linear block, max |delta| over the translation column, blocks)."""
    linear = max(abs(left[r][c] - right[r][c]) for r in range(3) for c in range(3))
    translation = max(abs(left[r][3] - right[r][3]) for r in range(3))
    return linear, translation


def render_instance_pose_parity(model_id: str, compiled: dict[str, Any],
                                vanilla_samples: dict[str, dict[str, Any]],
                                candidate_samples: dict[str, dict[str, Any]],
                                expansion: dict[str, Any], rotation_epsilon: float,
                                position_epsilon: float) -> dict[str, Any]:
    """Slice 4c composition leg: the classic model's MEASURED per-draw pose stack against the
    candidate's MEASURED bone transforms.

    The compiled probe records, for every draw k of an expanded part, the pose-stack matrix at
    the part's own pushPose (``instance_pose``: the model's per-draw transform, e.g. the fan spin
    or the stack step) and the matrix its cubes were compiled with (``draw_pose`` = instance *
    T(pivot) * R(part)). The geo probe records every bone's cumulative transform conjugated into
    classic space (``bone_poses_classic``). The two must agree:

      instance_pose(part, k)  ==  bone_pose(group bone of clone k)
      draw_pose(part, k)      ==  bone_pose(clone k) * T(part pivot / 16)

    (the second holds because GeckoLib cube corners are absolute, ModelPart corners local to the
    pivot). Linear entries are sines/cosines of the channel angles and are held to the animation
    epsilon in radians. An entry error bounds the angle error only up to a factor of sqrt(3) for a
    rotation about an arbitrary axis (the largest entry delta is at least |d theta| / sqrt(3)), so
    the leg's effective angular tolerance is at most ~sqrt(3) x epsilon (~3.5e-6 rad at 2e-6) — stated,
    no threshold changed (refuter A, 2026-09-06); the translation column is held to the position
    epsilon in model units (x16 from blocks).
    """
    mapping = expansion["bones"]
    pivots = {
        name: [float(value) / 16.0 for value in part["absolute_pivot"]]
        for name, part in definition_parts(compiled["definition"]).items()
    }
    clone_bones = sorted(name for name, entry in mapping.items() if entry["role"] == "clone")
    max_instance_linear = max_instance_translation = 0.0
    max_draw_linear = max_draw_translation = 0.0
    worst = "exact"
    draws_checked = 0
    samples_checked = 0
    for sample_id, vanilla_sample in vanilla_samples.items():
        if vanilla_sample.get("capture_kind") != "full":
            continue
        candidate_sample = candidate_samples[sample_id]
        poses = candidate_sample.get("bone_poses_classic")
        if poses is None:
            raise AssertionError(f"{model_id}/{sample_id} geo probe recorded no bone_poses_classic")
        draws = vanilla_sample.get("draws")
        if draws is None:
            raise AssertionError(f"{model_id}/{sample_id} compiled probe recorded no per-draw poses")
        seen: list[str] = []
        for draw in draws:
            clone = draw["bone"]
            entry = mapping.get(clone)
            if entry is None or entry["role"] != "clone":
                raise AssertionError(f"{model_id}/{sample_id} draw names no clone bone: {clone}")
            if entry["source_part"] != draw["part"] or entry["draw_index"] != draw["draw_index"]:
                raise AssertionError(f"{model_id}/{sample_id} draw {clone} disagrees with the converter mapping")
            seen.append(clone)
            group = entry["group_bone"]
            instance = matrix_rows(draw["instance_pose"], f"{model_id}/{sample_id}/{clone} instance_pose")
            group_pose = matrix_rows(poses[group], f"{model_id}/{sample_id}/{group} bone pose")
            linear, translation = matrix_delta(instance, group_pose)
            if linear > max(max_instance_linear, max_draw_linear) or translation > max(
                    max_instance_translation, max_draw_translation):
                worst = f"{sample_id}:{clone} (instance vs {group})"
            max_instance_linear = max(max_instance_linear, linear)
            max_instance_translation = max(max_instance_translation, translation)
            if linear > rotation_epsilon or translation * 16.0 > position_epsilon:
                raise AssertionError(
                    f"RENDER INSTANCE MISMATCH {model_id}/{sample_id}/{clone}: the classic per-draw "
                    f"transform differs from group bone {group} (linear {linear:.12g}, translation "
                    f"{translation * 16.0:.12g} model units)"
                )
            draw_pose = matrix_rows(draw["draw_pose"], f"{model_id}/{sample_id}/{clone} draw_pose")
            clone_pose = matrix_translate(
                matrix_rows(poses[clone], f"{model_id}/{sample_id}/{clone} bone pose"),
                pivots[entry["source_part"]],
            )
            linear, translation = matrix_delta(draw_pose, clone_pose)
            if linear > max(max_instance_linear, max_draw_linear) or translation > max(
                    max_instance_translation, max_draw_translation):
                worst = f"{sample_id}:{clone} (draw vs clone)"
            max_draw_linear = max(max_draw_linear, linear)
            max_draw_translation = max(max_draw_translation, translation)
            if linear > rotation_epsilon or translation * 16.0 > position_epsilon:
                raise AssertionError(
                    f"RENDER INSTANCE MISMATCH {model_id}/{sample_id}/{clone}: the classic draw pose "
                    f"differs from the clone bone's transform (linear {linear:.12g}, translation "
                    f"{translation * 16.0:.12g} model units)"
                )
            draws_checked += 1
        if sorted(seen) != clone_bones:
            raise AssertionError(
                f"{model_id}/{sample_id} draws {sorted(seen)} != clone bones {clone_bones}"
            )
        samples_checked += 1
    if draws_checked == 0:
        raise AssertionError(f"{model_id} render-instance composition leg compared no draws")
    return {
        "parts": {
            name: {
                "count": part["count"],
                "axis": part["axis"],
                "step_scope": part["step_scope"],
                "step_radians": part["step_radians"],
                "step_arithmetic": part["step_arithmetic"],
                "group_bones": part["group_bones"],
            }
            for name, part in expansion["parts"].items()
        },
        "group_bones": sum(1 for entry in mapping.values() if entry["role"] == "group"),
        "clone_bones": len(clone_bones),
        "samples_checked": samples_checked,
        "draws_checked": draws_checked,
        "instance_pose_epsilon_linear": rotation_epsilon,
        "instance_pose_epsilon_model_units": position_epsilon,
        "max_instance_pose_linear_delta": max_instance_linear,
        "max_instance_pose_translation_delta_model_units": max_instance_translation * 16.0,
        "max_draw_pose_linear_delta": max_draw_linear,
        "max_draw_pose_translation_delta_model_units": max_draw_translation * 16.0,
        "worst_case": worst,
        "evidence": (
            "measured classic per-draw pose stack (instance_pose at the part's pushPose, draw_pose at "
            "cube compile) versus measured GeckoLib bone transforms conjugated into classic space "
            "(bone_poses_classic): instance == group bone, draw == clone bone * T(pivot)"
        ),
    }


def resolve_repository_path(repository_root: Path, text: str) -> Path:
    path = Path(text)
    return path.resolve() if path.is_absolute() else (repository_root / path).resolve()


def keyframe_tolerance_text(epsilon: float) -> str:
    """The ruling's spelling of the tolerance: 2.5e-3, not 2.5e-03."""
    mantissa, exponent = f"{epsilon:.1e}".split("e")
    return f"{mantissa}e{int(exponent)}"


def keyframe_reference_leg_parity(model_id: str, spec: dict[str, Any], compiled: dict[str, Any],
                                  geo_render: dict[str, Any], thresholds: dict[str, Any],
                                  clip_path: Path, clip_manifest_path: Path) -> dict[str, Any]:
    """The keyframe reference leg: the shipped keyframe layers against the compiled setupAnim.

    Every non-bind sample of the schedule (the amplitude x fraction grid, the dense probes, the wrap
    pairs) carries the layers' pose of every bone in classic terms; each layer bone must match the
    compiled model's rotation within the leg's named tolerance and every other bone must sit at bind
    within the animation epsilon. The probe's own outputs - the density search, the full-schedule
    confirmation, the wrap sample, the order-independence check - are required to hold, and the
    density statement is rebuilt here and must equal the probe's.
    """
    leg = spec[KEYFRAME_LEG_KEY]
    if KEYFRAME_LEG_TOLERANCE_KEY not in thresholds:
        raise AssertionError(
            f"{model_id} declares {KEYFRAME_LEG_KEY} but thresholds has no {KEYFRAME_LEG_TOLERANCE_KEY}: "
            "the keyframe leg's tolerance is an owner ruling and is named, never defaulted"
        )
    epsilon = float(thresholds[KEYFRAME_LEG_TOLERANCE_KEY])
    bind_epsilon = float(thresholds["animation_epsilon_radians"])
    block = geo_render.get(KEYFRAME_LEG_KEY)
    if not isinstance(block, dict):
        raise AssertionError(f"{model_id} geo-render carries no {KEYFRAME_LEG_KEY} block")
    if block.get("candidate_class") != leg["candidate_class"]:
        raise AssertionError(f"{model_id} keyframe leg candidate class drift between manifest and probe")
    if bool(block.get("spline_repair")) != bool(leg["spline_repair"]):
        raise AssertionError(f"{model_id} keyframe leg spline_repair drift between manifest and probe")
    if float(block.get("tolerance_radians", -1.0)) != epsilon:
        raise AssertionError(
            f"{model_id} the probe searched the density at {block.get('tolerance_radians')} rad, the manifest names {epsilon}"
        )
    if not clip_path.is_file():
        raise AssertionError(f"{model_id} keyframe leg clip is missing: {clip_path}")
    if block.get("clip_sha256") != sha256(clip_path):
        raise AssertionError(f"{model_id} keyframe leg clip provenance drift: {clip_path}")
    if block.get("clip_matches_generator_rule") is not True:
        raise AssertionError(f"{model_id} keyframe leg clip is not the generator's output")

    vanilla_samples = sample_map(compiled)
    candidate_samples = sample_map(geo_render)
    layer_of_bone: dict[str, str] = {}
    for layer in block["layers"]:
        for bone in layer["bones"]:
            if bone in layer_of_bone:
                raise AssertionError(f"{model_id} bone {bone} in two keyframe layers")
            layer_of_bone[bone] = layer["group"]
    groups = [layer["group"] for layer in block["layers"]]
    max_error = {group: 0.0 for group in groups}
    worst = {group: "exact" for group in groups}
    max_bind_motion = 0.0
    compared = 0
    wrap_pairs: dict[str, dict[str, Any]] = {}
    for current_id, vanilla_sample in vanilla_samples.items():
        if current_id == "bind":
            continue
        candidate_sample = candidate_samples[current_id]
        keyframe = candidate_sample.get(KEYFRAME_LEG_SAMPLE_FIELD)
        if not isinstance(keyframe, dict):
            raise AssertionError(f"{model_id}/{current_id} carries no {KEYFRAME_LEG_SAMPLE_FIELD}")
        transforms = vanilla_sample["transforms"]
        if set(keyframe) != set(transforms):
            raise AssertionError(f"{model_id}/{current_id} keyframe bone set differs from the compiled model")
        for bone, transform in transforms.items():
            delta = vector_delta(transform["rotation"], keyframe[bone])
            group = layer_of_bone.get(bone)
            if group is None:
                max_bind_motion = max(max_bind_motion, delta)
                if delta > bind_epsilon:
                    raise AssertionError(
                        f"KEYFRAME LEG MISMATCH {model_id}/{current_id}/{bone}: a bone outside every layer moved by "
                        f"{delta:.12g} > {bind_epsilon:.12g}"
                    )
                continue
            compared += 1
            if delta > max_error[group]:
                max_error[group] = delta
                worst[group] = f"{current_id}:{bone}"
            if delta > epsilon:
                raise AssertionError(
                    f"KEYFRAME LEG MISMATCH {model_id}/{current_id}/{bone}: keyframe layers delta {delta:.12g} > "
                    f"tolerance {epsilon:.12g}"
                )
        if KEYFRAME_WRAP_TOKEN in current_id:
            # The pair's frequency group comes from the record the probe wrote beside the id (KeyframeLeg
            # .wrapProvenance): the id's token and the group name were resolved by the same Java frequencyToken
            # over the same float, so nothing here rebuilds a token from the manifest's number (Float.toString
            # and `.7g` diverge past seven significant digits; item 15 refuter B, D10).
            provenance = candidate_sample.get(KEYFRAME_WRAP_FIELD)
            if not isinstance(provenance, dict) or provenance.get("group") not in groups:
                raise AssertionError(f"{model_id}/{current_id} wrap sample carries no {KEYFRAME_WRAP_FIELD} group")
            prefix, _, side = current_id.rpartition("_")
            id_token = prefix.split(KEYFRAME_WRAP_TOKEN, 1)[1].rsplit("_c", 1)[0]
            if provenance.get("frequency_token") != id_token:
                raise AssertionError(
                    f"{model_id}/{current_id} wrap sample token {provenance.get('frequency_token')!r} "
                    f"is not the id's {id_token!r}"
                )
            pair = wrap_pairs.setdefault(prefix, {})
            if pair.setdefault("group", provenance["group"]) != provenance["group"]:
                raise AssertionError(f"{model_id} wrap pair {prefix} names two frequency groups")
            pair[side] = keyframe
    if compared == 0:
        raise AssertionError(f"{model_id} keyframe leg compared no layer bone")
    if not wrap_pairs:
        raise AssertionError(f"{model_id} keyframe leg has no wrap pairs (Amendment 1 point 5)")
    # A wrap pair belongs to ONE frequency group (the record beside its id names it): the seam is that group's,
    # and only that group's bones are continuous across it - every other group is mid-cycle there and moves by
    # its own slope over the 2 eps window (the gait moves ~8e-3 rad across the tail's seam window).
    bones_by_group = {layer["group"]: list(layer["bones"]) for layer in block["layers"]}
    max_continuity = 0.0
    groups_wrapped: set[str] = set()
    for prefix, pair in wrap_pairs.items():
        if set(pair) != {"group", "before", "after"}:
            raise AssertionError(f"{model_id} wrap pair {prefix} is incomplete: {sorted(pair)}")
        group = pair["group"]
        groups_wrapped.add(group)
        for bone in bones_by_group[group]:
            continuity = vector_delta(pair["before"][bone], pair["after"][bone])
            max_continuity = max(max_continuity, continuity)
            if continuity > epsilon:
                raise AssertionError(
                    f"KEYFRAME LEG WRAP {model_id}/{prefix}/{bone}: |v(T-eps) - v(0+eps)| = {continuity:.12g} > {epsilon:.12g}"
                )
    if groups_wrapped != set(bones_by_group):
        raise AssertionError(
            f"{model_id} wrap pairs cover groups {sorted(groups_wrapped)}, the layers declare {sorted(bones_by_group)}"
        )

    density = block["density"]
    if density.get("every_group_reached_tolerance") is not True:
        raise AssertionError(f"{model_id} keyframe density search did not reach the tolerance for every group: "
                             f"{ {group: row.get('fewest_keys_per_bone') for group, row in density['groups'].items()} }")
    # Refuter B, D3: the probe scans upward from min_keys and the table is non-monotone at the low end (N = 3 beats
    # N = 4..7 on every group), so "fewest" means something only if the table starts at min_keys, runs without a
    # gap, and every row below a group's fewest exceeds the tolerance while the fewest row holds it.
    table = density["search_max_error_radians_by_keys_per_bone"]
    if [int(keys) for keys in table] != list(range(int(density["min_keys"]), int(density["min_keys"]) + len(table))):
        raise AssertionError(f"{model_id} keyframe density table does not run consecutively from min_keys "
                             f"{density['min_keys']}: {list(table)}")
    for group, row in density["groups"].items():
        holding_below = [keys for keys, errors in table.items()
                         if int(keys) < int(row["fewest_keys_per_bone"]) and float(errors[group]) <= epsilon]
        if holding_below or float(table[str(row["fewest_keys_per_bone"])][group]) > epsilon:
            raise AssertionError(f"{model_id} keyframe density: {group} fewest {row['fewest_keys_per_bone']} is not the "
                                 f"first row of the table holding the tolerance (rows below it that hold: {holding_below})")
    if density.get("clip_density_is_fewest_for_every_group") is not True:
        raise AssertionError(
            f"{model_id} the clip's density is not the fewest keys holding the tolerance: "
            f"{ {group: (row.get('clip_keys_per_bone'), row.get('fewest_keys_per_bone')) for group, row in density['groups'].items()} }"
        )
    for group, row in density["groups"].items():
        if row.get("confirmed_holds_tolerance") is not True or row.get("one_fewer_fails_tolerance") is not True:
            raise AssertionError(f"{model_id} keyframe density confirmation failed for {group}: {row}")
    for group, row in block["clip_full_schedule"].items():
        if row.get("holds_tolerance") is not True:
            raise AssertionError(f"{model_id} keyframe clip exceeds the tolerance on the dense schedule for {group}: {row}")
    for group, row in block["wrap_sample"].items():
        if int(row.get("seams_sampled", 0)) <= 0 or row.get("holds_tolerance") is not True:
            raise AssertionError(f"{model_id} keyframe wrap sample failed for {group}: {row}")
    reversed_delta = float(block["schedule_reversed_max_delta_radians"])
    if reversed_delta > bind_epsilon:
        raise AssertionError(
            f"{model_id} the keyframe layers depend on frame order: reversed schedule max delta {reversed_delta:.12g}"
        )
    if set(density["groups"]) != set(groups) or set(block["clip"]) != set(groups):
        raise AssertionError(f"{model_id} keyframe leg group sets disagree")

    # TEST-005 (closed by the first Tier-2 slice, 2026-09-13): the late-prime twin - a persistent manager built
    # before its clips are served (its controllers registered with the model unserved), the unserved slot passed
    # without a tick (an empty-cache tick cannot run headlessly: GeckoLib's buildAnimationQueue catch block
    # initialises GeckoLibConstants, which registers a data component - OPT-029 R0; the probe records the tick's
    # bytecode-derived outcome), the prime one sample later at a late age - is REQUIRED for every model that declares
    # the leg, and must carry the from-zero prime's declared length, time-warp ratio and LUT index chain (each fact
    # equal to the time_warp block and to each other) and pose identically to the from-zero manager.
    twin = block.get("late_prime_twin")
    if not isinstance(twin, dict):
        raise AssertionError(f"{model_id} keyframe leg carries no late_prime_twin block (TEST-005)")
    if twin.get("holds") is not True:
        raise AssertionError(f"{model_id} late-prime twin does not hold: {twin}")
    if twin.get("manager_built_before_clips_served") is not True or twin.get("unserved_slot_ticked") is not False \
            or twin.get("unserved_primed_any_controller") is not False:
        raise AssertionError(f"{model_id} late-prime twin: the manager must be built unserved and prime nothing before the clips are served")
    if float(twin.get("unserved_bone_motion_radians", 1.0)) != 0.0:
        raise AssertionError(f"{model_id} late-prime twin: a bone moved before the clips were served")
    if float(twin["prime_age_ticks"]) <= float(twin["unserved_slot_age_ticks"]):
        raise AssertionError(f"{model_id} late-prime twin: the prime must come after the unserved slot")
    if set(twin["prime"]) != set(groups):
        raise AssertionError(f"{model_id} late-prime twin group set disagrees with the layers")
    for group, row in twin["prime"].items():
        warp = block["time_warp"][group]
        if row.get("facts_agree") is not True:
            raise AssertionError(f"{model_id} late-prime twin: {group} facts disagree with the from-zero prime: {row}")
        if float(row["declared_clip_ticks"]) != float(warp["declared_clip_ticks"]) \
                or float(row["clip_ticks_per_age_tick"]) != float(warp["clip_ticks_per_age_tick"]):
            raise AssertionError(f"{model_id} late-prime twin: {group} time-warp differs from the from-zero prime's")
        if int(row["last_cosine_index"]) != int(row["from_zero_last_cosine_index"]) \
                or int(row["last_cosine_index"]) != int(row["chain_cosine_index_at_prime_age"]) \
                or float(row["last_clip_tick"]) != float(row["from_zero_last_clip_tick"]):
            raise AssertionError(f"{model_id} late-prime twin: {group} LUT index chain differs from the from-zero prime's")
    twin_pose_delta = max(float(twin["prime_pose_max_delta_vs_from_zero_radians"]),
                          float(twin["follow_up_pose_max_delta_vs_from_zero_radians"]))
    if twin_pose_delta > bind_epsilon:
        raise AssertionError(f"{model_id} late-prime twin poses {twin_pose_delta:.12g} rad off the from-zero manager")

    keys = " / ".join(str(block["clip"][group]["keys_per_bone"]) for group in groups)
    lerps: list[str] = []
    for group in groups:
        lerp = block["clip"][group]["lerp_mode"]
        if lerp not in lerps:
            lerps.append(lerp)
    arguments = ("with spline arguments repaired at load" if block["spline_repair"]
                 else "with spline arguments as GeckoLib 4.8.4 evaluates them")
    if not clip_manifest_path.is_file():
        raise AssertionError(f"{model_id} keyframe leg clip manifest is missing: {clip_manifest_path}")
    species_label = load_json(clip_manifest_path)["species_label"]
    statement = (f"{keyframe_tolerance_text(epsilon)} rad; {species_label} reference leg {keys} "
                 f"{'|'.join(lerps)} keys per bone {arguments}; wrap sample T−ε vs 0+ε included")
    if block.get("density_statement") != statement:
        raise AssertionError(
            f"{model_id} density statement drift: probe '{block.get('density_statement')}' vs tool '{statement}'"
        )
    return {
        "tolerance_radians": epsilon,
        "statement": statement,
        "candidate_class": block["candidate_class"],
        "controller_class": block["controller_class"],
        "spline_repair": block["spline_repair"],
        "spline_repair_class": block.get("spline_repair_class"),
        "clip_sha256": block["clip_sha256"],
        "clip_path": leg["clip_path"],
        "pose_source": block["pose_source"],
        "layers": block["layers"],
        "clip": block["clip"],
        "time_warp": block["time_warp"],
        "sample_grid": {
            "layer_bone_samples_compared": compared,
            "max_error_radians_by_group": max_error,
            "worst_case_by_group": worst,
            "max_non_layer_bone_motion_radians": max_bind_motion,
        },
        "wrap_pairs": {
            "count": len(wrap_pairs),
            "max_abs_T_minus_eps_vs_0_plus_eps_radians": max_continuity,
        },
        "density_search": {
            group: {
                "fewest_keys_per_bone": row["fewest_keys_per_bone"],
                "confirmed_max_error_radians": row["confirmed_max_error_radians"],
                "confirmed_comparisons": row["confirmed_comparisons"],
                "one_fewer_max_error_radians": row["one_fewer_max_error_radians"],
            }
            for group, row in density["groups"].items()
        },
        "density_search_schedule": {
            key: density[key] for key in (
                "rule", "min_keys", "max_keys", "search_uniform_intervals", "confirm_uniform_intervals",
                "late_start_age_ticks", "span_age_ticks",
            )
        },
        "clip_full_schedule": block["clip_full_schedule"],
        "wrap_sample": block["wrap_sample"],
        "schedule_reversed_max_delta_radians": reversed_delta,
        "late_prime_twin": {
            "manager_built_before_clips_served": True,
            "unserved_slot_age_ticks": twin["unserved_slot_age_ticks"],
            "unserved_slot_ticked": False,
            "unserved_slot_note": twin.get("unserved_slot_note"),
            "prime_age_ticks": twin["prime_age_ticks"],
            "unserved_bone_motion_radians": twin["unserved_bone_motion_radians"],
            "prime": {
                group: {
                    "declared_clip_ticks": row["declared_clip_ticks"],
                    "clip_ticks_per_age_tick": row["clip_ticks_per_age_tick"],
                    "last_cosine_index": row["last_cosine_index"],
                    "last_clip_tick": row["last_clip_tick"],
                }
                for group, row in twin["prime"].items()
            },
            "prime_pose_max_delta_vs_from_zero_radians": twin["prime_pose_max_delta_vs_from_zero_radians"],
            "follow_up_pose_max_delta_vs_from_zero_radians": twin["follow_up_pose_max_delta_vs_from_zero_radians"],
            "holds": True,
        },
    }


def animation_parity(model_id: str, spec: dict[str, Any], compiled: dict[str, Any],
                     geo_render: dict[str, Any], contract: dict[str, Any],
                     epsilon: float, position_epsilon: float = 1.0e-4,
                     repository_root: Path | None = None,
                     conversion: dict[str, Any] | None = None) -> dict[str, Any]:
    """Compare independent compiled setupAnim output with the actual candidate hook.

    Slice 4c: for a model with render_instances the compiled ``transforms`` stay per PART (the
    channels its pose wrote), while the candidate carries the converter's group and clone bones.
    A clone's expected channels are its source part's, with the loop-assigned channel replaced by
    the clone's static step (``step_scope: part``); a static group's are its bind step; a
    hook-animated group's rotation has no part channel and is proven by the composition leg
    (``render_instance_pose_parity``) against the classic model's measured per-draw pose stack.
    """
    vanilla_samples = sample_map(compiled)
    candidate_samples = sample_map(geo_render)
    expansion = render_instance_expansion(conversion)
    mapping: dict[str, Any] = expansion["bones"] if expansion else {}
    expanded_parts = set(expansion["parts"]) if expansion else set()
    if vanilla_samples.keys() != candidate_samples.keys():
        raise AssertionError(
            f"{model_id} animation sample IDs differ: "
            f"{sorted(vanilla_samples)} != {sorted(candidate_samples)}"
        )
    if contract["kind"] != spec["animation_kind"]:
        raise AssertionError(
            f"{model_id} animation contract kind {contract['kind']} != {spec['animation_kind']}"
        )
    if geo_render.get("reference_animation_loaded_by_acceptance_runtime") is not False:
        raise AssertionError(f"{model_id} acceptance runtime must not load reference animation JSON")
    if geo_render.get("reference_animation_used_for_accepted_pose") is not False:
        raise AssertionError(f"{model_id} accepted pose must not use reference animation JSON")
    if any(any("reference" in key for key in sample) for sample in candidate_samples.values()):
        raise AssertionError(f"{model_id} candidate samples contain forbidden reference-clip pose data")

    bind_transforms = vanilla_samples["bind"]["transforms"]
    max_rotation_delta = 0.0
    max_dense_rotation_delta = 0.0
    max_static_delta = 0.0
    channel_count = 0
    dense_channel_count = 0
    worst = "exact"
    dense_worst = "exact"
    kind = contract["kind"]
    max_position_delta = 0.0
    position_worst = "exact"
    position_channel_samples = 0
    hidden_checks = 0
    entity_states_checked: set[str] = set()
    for current_id, vanilla_sample in vanilla_samples.items():
        candidate_sample = candidate_samples[current_id]
        for field in ("capture_kind", "dense_transform_sample", "age_ticks", "limb_swing_amount"):
            if candidate_sample.get(field) != vanilla_sample.get(field):
                raise AssertionError(f"{model_id}/{current_id} candidate metadata drift for {field}")
        candidate_rotations = candidate_sample["java_rotations"]
        transforms = vanilla_sample["transforms"]
        expected_bones = (set(transforms) - expanded_parts) | set(mapping)
        if set(candidate_rotations) != expected_bones:
            raise AssertionError(f"{model_id}/{current_id} candidate bone set differs from compiled model")
        for bone, transform in transforms.items():
            if bone in expanded_parts:
                # Slice 4c: compared through its clones below; the part itself never scales.
                max_static_delta = max(
                    max_static_delta,
                    vector_delta(transform["scale"], bind_transforms[bone]["scale"]),
                )
                continue
            delta = vector_delta(transform["rotation"], candidate_rotations[bone])
            if delta > max_rotation_delta:
                max_rotation_delta = delta
                worst = f"{current_id}:{bone}"
            if vanilla_sample.get("dense_transform_sample") and delta > max_dense_rotation_delta:
                max_dense_rotation_delta = delta
                dense_worst = f"{current_id}:{bone}"
            if delta > epsilon:
                raise AssertionError(
                    f"ANIMATION MISMATCH {model_id}/{current_id}/{bone}: actual custom runtime "
                    f"delta {delta:.12g} > epsilon {epsilon:.12g}"
                )
            if kind in ("code_driven", "entity_state"):
                # The production hook may move pivots (Robot4's cannon follow);
                # compare against the probe's basis-converted bone positions.
                position_delta = vector_delta(
                    transform["position"], candidate_sample["java_positions"][bone]
                )
                if position_delta > max_position_delta:
                    max_position_delta = position_delta
                    position_worst = f"{current_id}:{bone}"
                if position_delta > position_epsilon:
                    raise AssertionError(
                        f"POSITION MISMATCH {model_id}/{current_id}/{bone}: actual custom runtime "
                        f"position delta {position_delta:.12g} > epsilon {position_epsilon:.12g}"
                    )
                position_channel_samples += 3
                max_static_delta = max(
                    max_static_delta,
                    vector_delta(transform["scale"], bind_transforms[bone]["scale"]),
                )
            else:
                max_static_delta = max(
                    max_static_delta,
                    vector_delta(transform["position"], bind_transforms[bone]["position"]),
                    vector_delta(transform["scale"], bind_transforms[bone]["scale"]),
                )
            channel_count += 3
            if vanilla_sample.get("dense_transform_sample"):
                dense_channel_count += 3
        for bone, entry in mapping.items():
            # Slice 4c expansion bones (see the docstring); positions are in the parent's frame,
            # so a clone under a group at the origin expects the part's own x/y/z.
            source = transforms[entry["source_part"]]
            if entry["role"] == "clone":
                expected_rotation = [float(value) for value in source["rotation"]]
                channel = entry.get("static_channel")
                if channel is not None:
                    expected_rotation[AXIS_INDEX[channel]] = float(entry["static_rotation_radians"][AXIS_INDEX[channel]])
                expected_position = source["position"]
            elif entry.get("animated_by_hook"):
                expected_rotation = None  # proven by render_instance_pose_parity
                expected_position = candidate_samples["bind"]["java_positions"][bone]
            else:
                expected_rotation = entry["static_rotation_radians"]
                expected_position = candidate_samples["bind"]["java_positions"][bone]
            if expected_rotation is not None:
                delta = vector_delta(expected_rotation, candidate_rotations[bone])
                if delta > max_rotation_delta:
                    max_rotation_delta = delta
                    worst = f"{current_id}:{bone}"
                if delta > epsilon:
                    raise AssertionError(
                        f"ANIMATION MISMATCH {model_id}/{current_id}/{bone}: actual custom runtime "
                        f"delta {delta:.12g} > epsilon {epsilon:.12g} (render-instance "
                        f"{entry['role']} of {entry['source_part']})"
                    )
                channel_count += 3
            position_delta = vector_delta(expected_position, candidate_sample["java_positions"][bone])
            if position_delta > max_position_delta:
                max_position_delta = position_delta
                position_worst = f"{current_id}:{bone}"
            if position_delta > position_epsilon:
                raise AssertionError(
                    f"POSITION MISMATCH {model_id}/{current_id}/{bone}: actual custom runtime "
                    f"position delta {position_delta:.12g} > epsilon {position_epsilon:.12g}"
                )
            position_channel_samples += 3
    if max_static_delta > epsilon:
        raise AssertionError(
            f"ANIMATION MISMATCH {model_id}: candidate handles rotation only but compiled "
            f"position/scale delta is {max_static_delta:.12g}"
        )
    if kind in ("code_driven", "entity_state"):
        for current_id, vanilla_sample in vanilla_samples.items():
            if current_id == "bind":
                continue
            candidate_sample = candidate_samples[current_id]
            expected_hidden = sorted(vanilla_sample["hidden_bones"])
            actual_hidden = sorted(candidate_sample["hidden_bones"])
            if mapping:
                # Slice 4c: a hidden expanded part hides all its clones and groups; compare by source part.
                actual_hidden = sorted({
                    mapping[bone]["source_part"] if bone in mapping else bone for bone in actual_hidden
                })
            if actual_hidden != expected_hidden:
                raise AssertionError(
                    f"VISIBILITY MISMATCH {model_id}/{current_id}: GeckoLib hid {actual_hidden}, "
                    f"compiled hid {expected_hidden}"
                )
            hidden_checks += 1
            if "entity_state" in vanilla_sample:
                if candidate_sample.get("entity_state") != vanilla_sample["entity_state"]:
                    raise AssertionError(f"{model_id}/{current_id} entity state drift between probes")
                if candidate_sample.get("subject_after") != vanilla_sample["subject_after"]:
                    raise AssertionError(
                        f"STATE MISMATCH {model_id}/{current_id}: the hook left the subject as "
                        f"{candidate_sample.get('subject_after')}, the compiled model as "
                        f"{vanilla_sample['subject_after']}"
                    )
                entity_states_checked.add(vanilla_sample["entity_state"]["name"])

    contract_metrics: dict[str, Any] = {"kind": contract["kind"]}
    if contract["kind"] == "static":
        max_identity_motion = 0.0
        for current_id, sample in vanilla_samples.items():
            if current_id == "bind":
                continue
            for bone, transform in sample["transforms"].items():
                max_identity_motion = max(
                    max_identity_motion,
                    vector_delta(transform["rotation"], bind_transforms[bone]["rotation"]),
                )
        if max_identity_motion > epsilon:
            raise AssertionError(
                f"STATIC MODEL MISMATCH {model_id}: compiled setupAnim moved a bone by "
                f"{max_identity_motion:.12g} radians"
            )
        contract_metrics.update({
            "controller_required": contract["controller_required"],
            "accepted_runtime_path": "static bind pose; no controller",
            "max_identity_rotation_motion": max_identity_motion,
        })
    elif contract["kind"] == "gait_scaled":
        expected_role = "REFERENCE_ONLY_NOT_RUNTIME_ACCEPTANCE"
        if spec.get("emitted_clip_role") != expected_role:
            raise AssertionError(f"{model_id} manifest reference role is not explicit")
        if contract.get("emitted_clip_role") != expected_role:
            raise AssertionError(f"{model_id} contract reference role drift")
        if geo_render.get("emitted_clip_role") != expected_role:
            raise AssertionError(f"{model_id} runtime evidence reference role drift")
        if geo_render.get("candidate_animation_path") != "geckolib_custom_animation_code":
            raise AssertionError(f"{model_id} did not exercise the approved custom hook")
        if geo_render.get("accepted_pose_source") != "fresh BakedGeoModel + GeoModel.setCustomAnimations":
            raise AssertionError(f"{model_id} accepted pose source is not the fresh custom-hook path")
        if geo_render.get("fresh_baked_model_per_accepted_sample") is not True:
            raise AssertionError(f"{model_id} did not use a fresh baked model for every accepted sample")
        if "throws REFERENCE_ONLY_NOT_RUNTIME_ACCEPTANCE" not in str(
                geo_render.get("reference_animation_access_guard")):
            raise AssertionError(f"{model_id} reference animation access guard is absent")

        amplitudes = [float(value) for value in spec["limb_swing_amount_samples"]]
        if set(amplitudes) != {0.0, 0.25, 0.5, 1.0}:
            raise AssertionError(f"{model_id} amplitude matrix must be 0,.25,.5,1")
        dense_samples = [
            sample for sample in vanilla_samples.values()
            if sample.get("dense_transform_sample")
        ]
        interior_dense = [
            sample for sample in dense_samples
            if not sample["id"].endswith("_start") and not sample["id"].endswith("_end")
        ]
        expected_interior = int(spec["dense_transform_sample_count"]) * len(amplitudes)
        expected_dense = (int(spec["dense_transform_sample_count"]) + 2) * len(amplitudes)
        if len(interior_dense) != expected_interior or len(dense_samples) != expected_dense:
            raise AssertionError(
                f"{model_id} dense schedule drift: interior={len(interior_dense)}, "
                f"total={len(dense_samples)}"
            )
        separation = contract["dense_probe_key_separation"]
        if separation["off_grid_probe_key_coincidences"] != 0:
            raise AssertionError(f"{model_id} dense probes coincide with authored reference keys")
        if float(separation["minimum_key_probe_separation_age_ticks"]) <= float(
                separation["coincidence_epsilon_age_ticks"]):
            raise AssertionError(f"{model_id} minimum probe/key separation is not positive")

        scaled_channels = [
            channel for channel in spec["channels"] if channel.get("limb_swing_scaled", False)
        ]
        unscaled_channels = [
            channel for channel in spec["channels"] if not channel.get("limb_swing_scaled", False)
        ]
        groups: dict[tuple[str, bool, float], dict[float, str]] = {}
        for current_id, sample in vanilla_samples.items():
            if current_id == "bind":
                continue
            key = (
                str(sample["capture_kind"]),
                bool(sample["dense_transform_sample"]),
                round(float(sample["age_ticks"]), 9),
            )
            amount = float(sample["limb_swing_amount"])
            if amount in groups.setdefault(key, {}):
                raise AssertionError(f"{model_id} duplicate amplitude row at {key}")
            groups[key][amount] = current_id
        max_candidate_proportional = 0.0
        max_compiled_proportional = 0.0
        max_candidate_unscaled = 0.0
        max_compiled_unscaled = 0.0
        proportional_samples = 0
        for key, rows in groups.items():
            if set(rows) != set(amplitudes):
                raise AssertionError(f"{model_id} incomplete amplitude group {key}: {sorted(rows)}")
            unit_id = rows[1.0]
            vanilla_unit = vanilla_samples[unit_id]["transforms"]
            candidate_unit = candidate_samples[unit_id]["java_rotations"]
            for amplitude, current_id in rows.items():
                vanilla_actual = vanilla_samples[current_id]["transforms"]
                candidate_actual = candidate_samples[current_id]["java_rotations"]
                for channel in scaled_channels:
                    axis = {"x": 0, "y": 1, "z": 2}[channel["axis"]]
                    for bone in channel["bones"]:
                        bind = float(bind_transforms[bone]["rotation"][axis])
                        expected_vanilla = bind + amplitude * (
                            float(vanilla_unit[bone]["rotation"][axis]) - bind
                        )
                        expected_candidate = bind + amplitude * (
                            float(candidate_unit[bone][axis]) - bind
                        )
                        max_compiled_proportional = max(
                            max_compiled_proportional,
                            abs(float(vanilla_actual[bone]["rotation"][axis]) - expected_vanilla),
                        )
                        max_candidate_proportional = max(
                            max_candidate_proportional,
                            abs(float(candidate_actual[bone][axis]) - expected_candidate),
                        )
                        proportional_samples += 1
                for channel in unscaled_channels:
                    axis = {"x": 0, "y": 1, "z": 2}[channel["axis"]]
                    for bone in channel["bones"]:
                        max_compiled_unscaled = max(
                            max_compiled_unscaled,
                            abs(float(vanilla_actual[bone]["rotation"][axis])
                                - float(vanilla_unit[bone]["rotation"][axis])),
                        )
                        max_candidate_unscaled = max(
                            max_candidate_unscaled,
                            abs(float(candidate_actual[bone][axis])
                                - float(candidate_unit[bone][axis])),
                        )
        for label, delta in (
            ("candidate gait proportionality", max_candidate_proportional),
            ("compiled gait proportionality", max_compiled_proportional),
            ("candidate ambient amplitude independence", max_candidate_unscaled),
            ("compiled ambient amplitude independence", max_compiled_unscaled),
        ):
            if delta > epsilon:
                raise AssertionError(
                    f"GAIT AMPLITUDE MISMATCH {model_id}: {label} delta "
                    f"{delta:.12g} > epsilon {epsilon:.12g}"
                )
        contract_metrics.update({
            "accepted_runtime_path": "exact Mth.cos GeoModel.setCustomAnimations legacy-parity exception",
            "baked_keyframe_runtime_acceptance": False,
            "artist_editable_math_to_keyframes_status": "OUTSTANDING_G3",
            "reference_animation_role": expected_role,
            "limb_swing_amount_samples": amplitudes,
            "composition": contract["composition"],
            "scaled_bones": contract["scaled_bones"],
            "unscaled_bones": contract["unscaled_bones"],
            "dense_transform_sample_count_per_amplitude_including_endpoints": len(dense_samples) // len(amplitudes),
            "dense_transform_sample_count_total": len(dense_samples),
            "off_grid_probe_count_per_amplitude": int(spec["dense_transform_sample_count"]),
            "off_grid_probe_count_total": len(interior_dense),
            "endpoint_anchor_count_total": len(dense_samples) - len(interior_dense),
            "off_grid_probe_key_coincidences": separation["off_grid_probe_key_coincidences"],
            "minimum_key_probe_separation_age_ticks": separation["minimum_key_probe_separation_age_ticks"],
            "max_candidate_gait_proportionality_delta_radians": max_candidate_proportional,
            "max_compiled_gait_proportionality_delta_radians": max_compiled_proportional,
            "max_candidate_unscaled_channel_amplitude_delta_radians": max_candidate_unscaled,
            "max_compiled_unscaled_channel_amplitude_delta_radians": max_compiled_unscaled,
            "proportional_channel_samples": proportional_samples,
            "frequency_radians_per_age_tick": sorted({
                float(channel["frequency_radians_per_age_tick"])
                for channel in spec["channels"]
                if "frequency_radians_per_age_tick" in channel
            }),
        })
    elif contract["kind"] == "code_driven":
        expected_source = "fresh BakedGeoModel + production OreSpawnGeoReplacement.pose on explicit PoseInputs"
        if geo_render.get("candidate_animation_path") != "production_replacement_hook":
            raise AssertionError(f"{model_id} did not exercise the production replacement hook")
        if geo_render.get("accepted_pose_source") != expected_source:
            raise AssertionError(f"{model_id} accepted pose source is not the production hook path")
        if geo_render.get("fresh_baked_model_per_accepted_sample") is not True:
            raise AssertionError(f"{model_id} did not use a fresh baked model for every accepted sample")
        candidate_class = spec["candidate_class"]
        if contract.get("candidate_class") != candidate_class or geo_render.get("candidate_class") != candidate_class:
            raise AssertionError(f"{model_id} candidate class drift between manifest, contract and probe")
        if len(vanilla_samples) < 2:
            raise AssertionError(f"{model_id} code_driven proof needs at least one setupAnim sample")
        contract_metrics.update({
            "accepted_runtime_path": (
                "production OreSpawnGeoReplacement.applyCustomAnimations through "
                "OreSpawnGeoReplacementModel.setCustomAnimations"
            ),
            "candidate_class": candidate_class,
            "inputs": {
                "limb_swing": spec["limb_swing"],
                "limb_swing_amounts": spec["limb_swing_amount_samples"] if "limb_swing_amount_samples" in spec else [spec["limb_swing_amount"]],
                "net_head_yaw_degrees": spec.get("net_head_yaw", 0.0),
                "head_pitch_degrees": spec.get("head_pitch", 0.0),
            },
        })
    elif contract["kind"] == "entity_state":
        expected_source = "fresh BakedGeoModel + production OreSpawnGeoReplacement.pose on explicit PoseInputs"
        if geo_render.get("candidate_animation_path") != "production_replacement_hook":
            raise AssertionError(f"{model_id} did not exercise the production replacement hook")
        if geo_render.get("accepted_pose_source") != expected_source:
            raise AssertionError(f"{model_id} accepted pose source is not the production hook path")
        candidate_class = spec["candidate_class"]
        if contract.get("candidate_class") != candidate_class or geo_render.get("candidate_class") != candidate_class:
            raise AssertionError(f"{model_id} candidate class drift between manifest, contract and probe")
        declared_states = [state["name"] for state in spec["entity_states"]]
        if contract.get("entity_states") != declared_states:
            raise AssertionError(f"{model_id} entity state declaration drift")
        if sorted(entity_states_checked) != sorted(declared_states):
            raise AssertionError(
                f"{model_id} entity states checked {sorted(entity_states_checked)} != declared {declared_states}"
            )
        contract_metrics.update({
            "accepted_runtime_path": (
                "production OreSpawnGeoReplacement.applyCustomAnimations posed from declared entity "
                "states through the entity's pose interface; compiled poseFrom on the same states"
            ),
            "candidate_class": candidate_class,
            "entity_states": spec["entity_states"],
            "inputs": {
                "limb_swing": spec["limb_swing"],
                "limb_swing_amounts": spec["limb_swing_amount_samples"] if "limb_swing_amount_samples" in spec else [spec["limb_swing_amount"]],
                "net_head_yaw_degrees": spec.get("net_head_yaw", 0.0),
                "head_pitch_degrees": spec.get("head_pitch", 0.0),
            },
        })
    else:
        raise AssertionError(f"unsupported animation contract kind {contract['kind']}")

    if kind in ("code_driven", "entity_state"):
        contract_metrics.update({
            "max_position_delta_model_units": max_position_delta,
            "position_epsilon_model_units": position_epsilon,
            "position_worst_case": position_worst,
            "position_channel_samples": position_channel_samples,
            "hidden_bone_checks": hidden_checks,
        })
    if expansion:
        if kind not in ("code_driven", "entity_state"):
            raise AssertionError(f"{model_id} render_instances need a production-hook animation kind")
        contract_metrics["render_instances"] = render_instance_pose_parity(
            model_id, compiled, vanilla_samples, candidate_samples, expansion, epsilon, position_epsilon
        )

    return {
        "status": "PASS",
        "epsilon_radians": epsilon,
        "accepted_pose_comparison": "compiled setupAnim versus actual fresh-baked GeckoLib candidate",
        "reference_animation_used_for_acceptance": False,
        "sample_count": len(vanilla_samples),
        "max_rotation_delta_radians": max_rotation_delta,
        "max_dense_rotation_delta_radians": max_dense_rotation_delta,
        "max_unexpected_position_or_scale_delta": max_static_delta,
        "worst_case": worst,
        "dense_worst_case": dense_worst,
        "rotation_channel_samples": channel_count,
        "dense_rotation_channel_samples": dense_channel_count,
        "contract": contract_metrics,
    }


def reference_animation_schema(model_id: str, spec: dict[str, Any],
                               compiled: dict[str, Any], animation: dict[str, Any],
                               contract: dict[str, Any], conversion: dict[str, Any],
                               ticks_per_second: float) -> dict[str, Any]:
    """Independently validate reference JSON without treating it as accepted runtime input."""
    if animation.get("format_version") != "1.8.0" or not isinstance(animation.get("animations"), dict):
        raise AssertionError(f"{model_id} reference animation has invalid generic schema")
    clips = animation["animations"]
    if spec["animation_kind"] in ("code_driven", "entity_state"):
        if clips:
            raise AssertionError(f"{model_id} code-driven animation reference unexpectedly has clips")
        return {
            "status": "SCHEMA_VALID_REFERENCE_ONLY",
            "role": "NOT_APPLICABLE_CODE_DRIVEN_MODEL",
            "clip_count": 0,
            "constant_vector_count": 0,
            "runtime_acceptance": False,
        }
    if spec["animation_kind"] == "static":
        if clips:
            raise AssertionError(f"{model_id} static animation reference unexpectedly has clips")
        return {
            "status": "SCHEMA_VALID_REFERENCE_ONLY",
            "role": "NOT_APPLICABLE_STATIC_MODEL",
            "clip_count": 0,
            "constant_vector_count": 0,
            "runtime_acceptance": False,
        }

    expected_role = "REFERENCE_ONLY_NOT_RUNTIME_ACCEPTANCE"
    if spec.get("emitted_clip_role") != expected_role:
        raise AssertionError(f"{model_id} reference animation role drift")
    if conversion.get("artist_editable_math_to_keyframes_status") != "OUTSTANDING_G3":
        raise AssertionError(f"{model_id} must leave artist-editable keyframe resolution to G3")
    expected_clips = {spec["ambient_clip_name"], spec["unit_gait_clip_name"]}
    if set(clips) != expected_clips:
        raise AssertionError(f"{model_id} reference animation clip set drift")

    common_time_keys: list[str] | None = None
    constant_vectors = 0
    for clip_name, clip in clips.items():
        if clip.get("loop") is not True:
            raise AssertionError(f"{model_id}/{clip_name} reference loop mode must be true")
        if not isinstance(clip.get("animation_length"), (int, float)):
            raise AssertionError(f"{model_id}/{clip_name} reference length is not constant")
        bones = clip.get("bones")
        if not isinstance(bones, dict) or not bones:
            raise AssertionError(f"{model_id}/{clip_name} reference bones are missing")
        for bone_name, channels in bones.items():
            if not isinstance(channels, dict) or set(channels) != {"rotation"}:
                raise AssertionError(f"{model_id}/{clip_name}/{bone_name} must be rotation-only")
            rotation = channels["rotation"]
            if not isinstance(rotation, dict) or not rotation:
                raise AssertionError(f"{model_id}/{clip_name}/{bone_name} has no reference keys")
            time_keys = list(rotation)
            if common_time_keys is None:
                common_time_keys = time_keys
            elif time_keys != common_time_keys:
                raise AssertionError(f"{model_id} reference channels use different timestamp grids")
            for timestamp, keyframe in rotation.items():
                try:
                    parsed_time = float(timestamp)
                except ValueError as exc:
                    raise AssertionError(f"{model_id} reference timestamp is not numeric") from exc
                if not math.isfinite(parsed_time):
                    raise AssertionError(f"{model_id} reference timestamp is not finite")
                if not isinstance(keyframe, dict) or set(keyframe) != {"post", "lerp_mode"}:
                    raise AssertionError(f"{model_id} reference keyframe schema drift")
                if keyframe["lerp_mode"] != spec["animation_interpolation"]:
                    raise AssertionError(f"{model_id} reference interpolation drift")
                vector = keyframe["post"]
                if not isinstance(vector, list) or len(vector) != 3 or any(
                    not isinstance(value, (int, float)) or not math.isfinite(float(value))
                    for value in vector
                ):
                    raise AssertionError(f"{model_id} reference keyframe is not a constant vector")
                constant_vectors += 1

    if common_time_keys is None:
        raise AssertionError(f"{model_id} reference animation has no timestamp grid")
    authored_ticks = [float(timestamp) * ticks_per_second for timestamp in common_time_keys]
    expected_key_count = (
        int(spec["dense_transform_sample_count"])
        * int(spec["animation_bake_subdivisions_per_dense_interval"])
        + 1
    )
    if len(authored_ticks) != expected_key_count:
        raise AssertionError(
            f"{model_id} reference authored key count {len(authored_ticks)} != {expected_key_count}"
        )
    interior_probes = [
        float(sample["age_ticks"])
        for sample in compiled["samples"]
        if sample.get("dense_transform_sample")
        and not sample["id"].endswith("_start")
        and not sample["id"].endswith("_end")
    ]
    unique_probes = sorted(set(interior_probes))
    if len(unique_probes) != int(spec["dense_transform_sample_count"]):
        raise AssertionError(f"{model_id} off-grid timestamp count drift")
    separations = [min(abs(probe - key) for key in authored_ticks) for probe in unique_probes]
    epsilon = float(spec["probe_key_coincidence_epsilon_ticks"])
    coincidences = sum(separation <= epsilon for separation in separations)
    if coincidences:
        raise AssertionError(
            f"{model_id} anti-alias guard failed: {coincidences} probes coincide with authored keys"
        )
    computed_minimum = min(separations)
    contract_separation = contract["dense_probe_key_separation"]
    if int(contract_separation["off_grid_probe_key_coincidences"]) != coincidences:
        raise AssertionError(f"{model_id} contract coincidence count is not independently derived")
    if abs(float(contract_separation["minimum_key_probe_separation_age_ticks"])
           - computed_minimum) > 2.1e-8:
        raise AssertionError(f"{model_id} contract minimum separation cross-check failed")
    return {
        "status": "SCHEMA_VALID_REFERENCE_ONLY",
        "role": expected_role,
        "runtime_acceptance": False,
        "accepted_pose_dependency": False,
        "artist_editable_math_to_keyframes_status": "OUTSTANDING_G3",
        "clip_count": len(clips),
        "authored_key_count_per_channel": len(authored_ticks),
        "constant_vector_count": constant_vectors,
        "off_grid_probe_count": len(unique_probes),
        "probe_key_coincidences_within_epsilon": coincidences,
        "coincidence_epsilon_age_ticks": epsilon,
        "minimum_key_probe_separation_age_ticks": computed_minimum,
        "evaluation_for_runtime_acceptance": "NOT_PERFORMED_REFERENCE_ONLY",
    }


def fixture_coverage(spec: dict[str, Any], compiled: dict[str, Any],
                     generated_geometry: dict[str, Any]) -> dict[str, Any]:
    if spec.get("proof_scope") != "non_production_converter_fixture":
        raise AssertionError(f"{spec['id']} fixture is not marked non-production")
    parts: list[tuple[dict[str, Any], str | None]] = []

    def visit(part: dict[str, Any], parent: str | None) -> None:
        for child in part["children"]:
            parts.append((child, parent))
            visit(child, child["name"])

    visit(compiled["definition"], None)
    cubes = [cube for part, _parent in parts for cube in part["cubes"]]
    source_checks = {
        "nested_parent_bone": any(parent is not None for _part, parent in parts),
        "non_mirrored_uv": any(cube["mirror"] is False for cube in cubes),
        "nonzero_bind_rotation": any(
            any(abs(float(value)) > 1.0e-9 for value in part["initial_rotation_radians"])
            for part, _parent in parts
        ),
        "uniform_inflate": any(
            max(float(value) for value in cube["deformation"])
            - min(float(value) for value in cube["deformation"]) <= 1.0e-9
            and any(abs(float(value)) > 1.0e-9 for value in cube["deformation"])
            for cube in cubes
        ),
    }
    bones = generated_geometry["minecraft:geometry"][0]["bones"]
    generated_cubes = [cube for bone in bones for cube in bone.get("cubes", [])]
    generated_checks = {
        "nested_parent_bone": any("parent" in bone for bone in bones),
        "non_mirrored_uv": any(
            "modelpart_mirror" not in cube and isinstance(cube.get("uv"), dict)
            for cube in generated_cubes
        ),
        "nonzero_bind_rotation": any(
            any(abs(float(value)) > 1.0e-9 for value in bone.get("rotation", []))
            for bone in bones
        ),
        "uniform_inflate": any(abs(float(cube.get("inflate", 0.0))) > 1.0e-9
                               for cube in generated_cubes),
    }
    required = list(spec["required_coverage"])
    if sorted(required) != sorted(source_checks):
        raise AssertionError(f"{spec['id']} required fixture coverage declaration drift")
    missing = [name for name in required if not source_checks[name] or not generated_checks[name]]
    if missing:
        raise AssertionError(f"{spec['id']} fixture coverage failed: {missing}")
    return {
        "status": "PASS",
        "proof_scope": spec["proof_scope"],
        "required_coverage": required,
        "compiled_tree_observed": source_checks,
        "generated_geo_observed": generated_checks,
    }


def verify_fixed_lf(paths: Iterable[Path]) -> dict[str, Any]:
    checked: list[str] = []
    for path in paths:
        data = path.read_bytes()
        if b"\r" in data or (data and not data.endswith(b"\n")):
            raise AssertionError(f"G1 artifact is not deterministic LF text: {path}")
        checked.append(path.name)
    return {"status": "PASS", "line_ending": "LF", "file_count": len(checked), "files": checked}


def all_vertices(samples: dict[str, dict[str, Any]], ids: Iterable[str]) -> list[tuple[float, float, float]]:
    vertices: list[tuple[float, float, float]] = []
    for sample_id in ids:
        vertices.extend(
            vertex_position(vertex) for vertex in samples[sample_id]["render_vertices"]
        )
    return vertices


class Camera:
    def __init__(self, vertices: list[tuple[float, float, float]], yaw_degrees: float,
                 pitch_degrees: float) -> None:
        mins = [min(point[index] for point in vertices) for index in range(3)]
        maxs = [max(point[index] for point in vertices) for index in range(3)]
        self.center = tuple((mins[index] + maxs[index]) * 0.5 for index in range(3))
        self.yaw = math.radians(yaw_degrees)
        self.pitch = math.radians(pitch_degrees)

        projected = [self.project_unscaled(point) for point in vertices]
        span_x = max(point[0] for point in projected) - min(point[0] for point in projected)
        span_y = max(point[1] for point in projected) - min(point[1] for point in projected)
        largest_span = max(span_x, span_y, 1.0e-9)
        self.scale = (IMAGE_SIZE - 24.0) / largest_span

    def project_unscaled(self, point: tuple[float, float, float]) -> tuple[float, float, float]:
        x = point[0] - self.center[0]
        y = point[1] - self.center[1]
        z = point[2] - self.center[2]
        cos_yaw = math.cos(self.yaw)
        sin_yaw = math.sin(self.yaw)
        x1 = cos_yaw * x - sin_yaw * z
        z1 = sin_yaw * x + cos_yaw * z
        cos_pitch = math.cos(self.pitch)
        sin_pitch = math.sin(self.pitch)
        y2 = cos_pitch * y - sin_pitch * z1
        z2 = sin_pitch * y + cos_pitch * z1
        return x1, y2, z2

    def project(self, point: tuple[float, float, float]) -> tuple[float, float, float]:
        # Quantization is below the geometry epsilon and prevents sub-ulp edge
        # placement from turning a proven-equal corner into a one-pixel fringe.
        quantized = tuple(round(value, 6) for value in point)
        x, y, depth = self.project_unscaled(quantized)  # type: ignore[arg-type]
        return IMAGE_SIZE * 0.5 + x * self.scale, IMAGE_SIZE * 0.5 + y * self.scale, depth


def edge(a: tuple[float, float], b: tuple[float, float], p: tuple[float, float]) -> float:
    return (p[0] - a[0]) * (b[1] - a[1]) - (p[1] - a[1]) * (b[0] - a[0])


def render_capture(sample: dict[str, Any], texture: Image.Image,
                   camera: Camera) -> tuple[Image.Image, list[bool]]:
    """Rasterise one capture; also returns the per-pixel z-fight mask.

    A pixel is CONTESTED when two fragments from different quads land within
    CONTEST_DEPTH_EPSILON of each other at the front with different texels.
    Both real renderers resolve that by draw order. Under the G2 root-order
    contract the order is equal on both sides, so the mask is a diagnostic
    (the contested fraction is reported, every pixel compared); the opt-in
    `--contested-exclusion` uses it to skip exactly those pixels, as ruling 2
    (2026-09-02) did before the contract. The rasteriser's own tie rule at an
    EXACT depth tie is first-wins (a later fragment replaces the front only when
    nearer by more than 1e-9) where the game's LEQUAL depth test is last-wins;
    parity is unaffected because both captures share the rule and the order.
    """
    pixels = [BACKGROUND] * (IMAGE_SIZE * IMAGE_SIZE)
    depth_buffer = [math.inf] * (IMAGE_SIZE * IMAGE_SIZE)
    owner_quad = [-1] * (IMAGE_SIZE * IMAGE_SIZE)
    contested = [False] * (IMAGE_SIZE * IMAGE_SIZE)
    texture = texture.convert("RGBA")
    texture_pixels = texture.load()
    texture_width, texture_height = texture.size

    vertices = sample["render_vertices"]
    if len(vertices) % 4:
        raise AssertionError("captured renderer vertex count is not quad-aligned")
    for offset in range(0, len(vertices), 4):
        quad = vertices[offset:offset + 4]
        quad_index = offset // 4
        for indices in ((0, 1, 2), (0, 2, 3)):
            triangle = [quad[index] for index in indices]
            projected = [camera.project(vertex_position(vertex)) for vertex in triangle]
            points = [(point[0], point[1]) for point in projected]
            area = edge(points[0], points[1], points[2])
            if abs(area) < 1.0e-12:
                continue
            min_x = max(0, int(math.floor(min(point[0] for point in points))))
            max_x = min(IMAGE_SIZE - 1, int(math.ceil(max(point[0] for point in points))))
            min_y = max(0, int(math.floor(min(point[1] for point in points))))
            max_y = min(IMAGE_SIZE - 1, int(math.ceil(max(point[1] for point in points))))
            uvs = [[float(value) for value in vertex["uv"]] for vertex in triangle]

            for pixel_y in range(min_y, max_y + 1):
                for pixel_x in range(min_x, max_x + 1):
                    point = (pixel_x + 0.5, pixel_y + 0.5)
                    w0 = edge(points[1], points[2], point) / area
                    w1 = edge(points[2], points[0], point) / area
                    w2 = 1.0 - w0 - w1
                    if min(w0, w1, w2) < -1.0e-8:
                        continue
                    depth = w0 * projected[0][2] + w1 * projected[1][2] + w2 * projected[2][2]
                    pixel_index = pixel_y * IMAGE_SIZE + pixel_x
                    current_depth = depth_buffer[pixel_index]
                    if depth > current_depth + CONTEST_DEPTH_EPSILON:
                        continue
                    u = w0 * uvs[0][0] + w1 * uvs[1][0] + w2 * uvs[2][0]
                    v = w0 * uvs[0][1] + w1 * uvs[1][1] + w2 * uvs[2][1]
                    texture_x = min(texture_width - 1, max(0, int(math.floor(u * texture_width))))
                    texture_y = min(texture_height - 1, max(0, int(math.floor(v * texture_height))))
                    source = texture_pixels[texture_x, texture_y]
                    # Both renderers draw entity models with RenderType.entityCutoutNoCull:
                    # the cutout fragment shader discards alpha < 0.1 (no colour, no depth
                    # write) and draws everything else opaque. Blending here made the
                    # result depend on draw order (Slice 4b Island finding).
                    if source[3] < CUTOUT_ALPHA_THRESHOLD:
                        continue
                    colour = (source[0], source[1], source[2], 255)
                    if abs(depth - current_depth) <= CONTEST_DEPTH_EPSILON:
                        # Same depth as the current front fragment: a z-fight unless it
                        # is the same quad (shared diagonal) or the same texel.
                        if owner_quad[pixel_index] != quad_index and pixels[pixel_index] != colour:
                            contested[pixel_index] = True
                        if depth >= current_depth - 1.0e-9:
                            continue
                    else:
                        # Decisively nearer: whatever was contested behind it no longer shows.
                        contested[pixel_index] = False
                    pixels[pixel_index] = colour
                    depth_buffer[pixel_index] = depth
                    owner_quad[pixel_index] = quad_index

    image = Image.new("RGBA", (IMAGE_SIZE, IMAGE_SIZE))
    image.putdata(pixels)
    return image, contested


def render_capture_blended(sample: dict[str, Any], texture: Image.Image, camera: Camera,
                           mode: dict[str, Any], depth_epsilon: float) -> tuple[Image.Image, list[bool]]:
    """ENT-S-146: rasterise one capture under ``entity_translucent`` - what the GPU does for ONE model's
    quads, in emission order, under that RenderType's states (VISUAL_MODES, bytecode-cited):

    * the fragment shader discards a texel with alpha < 0.1 before anything else (both entity shaders
      share the line), so the vertex alpha never discards;
    * the surviving fragment's colour is the texel times the vertex colour (the RGBA bytes the manifest
      declares: ``color *= vertexColor``; ``ColorModulator`` is white, the overlay is NO_OVERLAY and
      the lightmap is not applied on either side - see ``visual_mode``);
    * depth test LEQUAL against the depth buffer, WITH the depth mask on (``COLOR_DEPTH_WRITE``): a
      passing fragment writes its depth, so a later fragment behind it is rejected and a later
      fragment at the same depth passes and blends again. The harness holds the test to a window
      (``depth <= front + depth_epsilon``, the manifest's own ``coplanar_depth_epsilon_blocks`` - a
      named tolerance, an owner ruling with its number, no default): two fragments closer than that
      are one plane for the harness and every such layer passes, in emission order, on both sides.
      What the data shows (refuter B on ENT-S-146, measured 2026-09-06): on the classic side alone the
      six spokes' coplanar depths agree to <= 1e-9 at most contested pixels, so a ring's shared planes
      ARE ties there; the cross-side noise for the SAME fragment is ~1e-7 genuine (vertex deltas
      <= 1.8e-7 blocks) plus up to ~1e-6 from this harness's own ``Camera.project``, which rounds every
      vertex to 6 decimals before projecting (a pre-existing quirk of BOTH modes - the cutout leg's
      first-wins ties see the same rounding). A window below that noise therefore flips passes between
      the two sides (the sweep is in before_after.md: 94 / 158 / 35 changed pixels at 1e-6 on three of
      the four captures; 0 / 2 / 0 / 0 at 1e-5), which is why the window is presented as its own
      tolerance rather than reused from the geometry epsilon or tuned. The test is against the LAST
      written depth (a chain of near-coplanar fragments can walk the front back; measured maximum
      9.9e-6 blocks, no chained pass beyond one window). Fragments a face's thickness apart (1/16
      block and more) are decided exactly as the GPU decides them;
    * blending ``SRC_ALPHA / ONE_MINUS_SRC_ALPHA`` over what the pixel holds (the background first),
      the result quantised to 8 bits after every fragment as an RGBA8 framebuffer does; the
      destination alpha stays opaque (``ONE / ONE_MINUS_SRC_ALPHA`` over an opaque background).

    The contested mask is this mode's diagnostic: a pixel where a fragment from ANOTHER quad passed
    within ``depth_epsilon`` of the front - whatever its texel, because under blending a second
    layer changes the pixel even when its colour is the same - and it is never cleared by a nearer
    fragment, since every earlier layer still contributes.
    """
    colour_scale = [value / 255.0 for value in mode["vertex_color"]]
    pixels = [BACKGROUND] * (IMAGE_SIZE * IMAGE_SIZE)
    depth_buffer = [math.inf] * (IMAGE_SIZE * IMAGE_SIZE)
    owner_quad = [-1] * (IMAGE_SIZE * IMAGE_SIZE)
    front_texel: list[tuple[int, int, int] | None] = [None] * (IMAGE_SIZE * IMAGE_SIZE)
    contested = [False] * (IMAGE_SIZE * IMAGE_SIZE)
    texture = texture.convert("RGBA")
    texture_pixels = texture.load()
    texture_width, texture_height = texture.size

    vertices = sample["render_vertices"]
    if len(vertices) % 4:
        raise AssertionError("captured renderer vertex count is not quad-aligned")
    for offset in range(0, len(vertices), 4):
        quad = vertices[offset:offset + 4]
        quad_index = offset // 4
        for indices in ((0, 1, 2), (0, 2, 3)):
            triangle = [quad[index] for index in indices]
            projected = [camera.project(vertex_position(vertex)) for vertex in triangle]
            points = [(point[0], point[1]) for point in projected]
            area = edge(points[0], points[1], points[2])
            if abs(area) < 1.0e-12:
                continue
            min_x = max(0, int(math.floor(min(point[0] for point in points))))
            max_x = min(IMAGE_SIZE - 1, int(math.ceil(max(point[0] for point in points))))
            min_y = max(0, int(math.floor(min(point[1] for point in points))))
            max_y = min(IMAGE_SIZE - 1, int(math.ceil(max(point[1] for point in points))))
            uvs = [[float(value) for value in vertex["uv"]] for vertex in triangle]

            for pixel_y in range(min_y, max_y + 1):
                for pixel_x in range(min_x, max_x + 1):
                    point = (pixel_x + 0.5, pixel_y + 0.5)
                    w0 = edge(points[1], points[2], point) / area
                    w1 = edge(points[2], points[0], point) / area
                    w2 = 1.0 - w0 - w1
                    if min(w0, w1, w2) < -1.0e-8:
                        continue
                    depth = w0 * projected[0][2] + w1 * projected[1][2] + w2 * projected[2][2]
                    pixel_index = pixel_y * IMAGE_SIZE + pixel_x
                    current_depth = depth_buffer[pixel_index]
                    if depth > current_depth + depth_epsilon:
                        continue  # LEQUAL, held to the geometry epsilon (see the docstring)
                    u = w0 * uvs[0][0] + w1 * uvs[1][0] + w2 * uvs[2][0]
                    v = w0 * uvs[0][1] + w1 * uvs[1][1] + w2 * uvs[2][1]
                    texture_x = min(texture_width - 1, max(0, int(math.floor(u * texture_width))))
                    texture_y = min(texture_height - 1, max(0, int(math.floor(v * texture_height))))
                    source = texture_pixels[texture_x, texture_y]
                    if source[3] < CUTOUT_ALPHA_THRESHOLD:
                        continue  # the shader's discard, on the texture alpha
                    texel = (source[0], source[1], source[2])
                    if abs(depth - current_depth) <= depth_epsilon and owner_quad[pixel_index] != quad_index:
                        contested[pixel_index] = True
                    alpha = source[3] / 255.0 * colour_scale[3]
                    destination = pixels[pixel_index]
                    blended = []
                    for channel in range(3):
                        src = source[channel] / 255.0 * colour_scale[channel]
                        dst = destination[channel] / 255.0
                        value = src * alpha + dst * (1.0 - alpha)
                        blended.append(min(255, max(0, int(value * 255.0 + 0.5))))
                    pixels[pixel_index] = (blended[0], blended[1], blended[2], 255)
                    depth_buffer[pixel_index] = depth
                    owner_quad[pixel_index] = quad_index
                    front_texel[pixel_index] = texel

    image = Image.new("RGBA", (IMAGE_SIZE, IMAGE_SIZE))
    image.putdata(pixels)
    return image, contested


def save_png(image: Image.Image, path: Path) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    image.save(path, format="PNG", compress_level=9)


def pixel_diff(vanilla: Image.Image, geo: Image.Image, channel_tolerance: int,
               excluded: list[bool] | None = None) -> tuple[float, float, Image.Image]:
    """Changed fraction and MAE over the pixels not excluded; excluded pixels are painted CONTESTED_MARKER."""
    vanilla_pixels = list(vanilla.convert("RGB").get_flattened_data())
    geo_pixels = list(geo.convert("RGB").get_flattened_data())
    if excluded is None:
        excluded = [False] * len(vanilla_pixels)
    changed = 0
    absolute_sum = 0
    compared = 0
    diff_pixels: list[tuple[int, int, int, int]] = []
    for left, right, skip in zip(vanilla_pixels, geo_pixels, excluded):
        if skip:
            diff_pixels.append(CONTESTED_MARKER)
            continue
        compared += 1
        delta = tuple(abs(left[index] - right[index]) for index in range(3))
        if max(delta) > channel_tolerance:
            changed += 1
        absolute_sum += sum(delta)
        diff_pixels.append((min(255, delta[0] * 8), min(255, delta[1] * 8),
                            min(255, delta[2] * 8), 255))
    if compared == 0:
        raise AssertionError("every pixel is contested; nothing left to compare")
    image = Image.new("RGBA", vanilla.size)
    image.putdata(diff_pixels)
    return changed / compared, absolute_sum / (compared * 3), image


def foreground_fraction(image: Image.Image) -> float:
    pixels = image.convert("RGBA").get_flattened_data()
    return sum(pixel != BACKGROUND for pixel in pixels) / (IMAGE_SIZE * IMAGE_SIZE)


def visual_parity(model_id: str, spec: dict[str, Any], compiled: dict[str, Any],
                  geo_render: dict[str, Any], repository_root: Path,
                  output_dir: Path, thresholds: dict[str, Any],
                  exclude_contested: bool = False) -> dict[str, Any]:
    """The visual leg. By default (the G2 root-order contract) every pixel is compared,
    contested ones included, and the contested fraction is a diagnostic only;
    ``exclude_contested`` True (``--contested-exclusion``) restores the ruling-2 exclusion
    for a diagnostic run."""
    retired = [field for field in RETIRED_MANIFEST_FIELDS if field in spec]
    if retired:
        raise AssertionError(
            f"{model_id} manifest still carries the retired ruling-2 field(s) {retired}: the z-fight "
            "exclusion and its pins were removed by the owner's ruling of 2026-09-06 (G2 root-order contract)"
        )
    mode = visual_mode(model_id, spec)
    vanilla_samples = sample_map(compiled)
    geo_samples = sample_map(geo_render)
    visual_sample_ids = tuple(spec.get("visual_sample_ids", DEFAULT_VISUAL_SAMPLE_IDS))
    if not set(visual_sample_ids).issubset(vanilla_samples):
        raise AssertionError(f"{model_id} visual sample IDs are absent from compiled capture")
    # A single `camera` or a `cameras` list of {name, yaw_degrees, pitch_degrees};
    # every camera must pass (ruling 2026-09-02: Vortex is proven front and back).
    if "cameras" in spec:
        camera_specs = [dict(entry) for entry in spec["cameras"]]
        if len({entry["name"] for entry in camera_specs}) != len(camera_specs):
            raise AssertionError(f"{model_id} cameras must have unique names")
    else:
        camera_specs = [dict(spec["camera"], name=None)]
    vertices_for_fit = all_vertices(vanilla_samples, visual_sample_ids)
    cameras = [
        (entry.get("name"), Camera(vertices_for_fit, float(entry["yaw_degrees"]), float(entry["pitch_degrees"])))
        for entry in camera_specs
    ]
    camera_spec = spec.get("cameras", spec.get("camera"))
    texture = Image.open(repository_root / spec["texture"])
    rows: list[dict[str, Any]] = []
    max_changed = 0.0
    max_mae = 0.0
    max_contested = 0.0
    min_foreground = 1.0

    for sample_id, (camera_name, camera) in (
        (sample_id, camera_entry) for sample_id in visual_sample_ids for camera_entry in cameras
    ):
        capture_id = sample_id if camera_name is None else f"{sample_id}.{camera_name}"
        if mode is not None and mode["rasteriser"] == "render_capture_blended":
            # ENT-S-146 (refuter B, D2a): the depth-tie window is its OWN named tolerance, never a reuse of the
            # geometry epsilon and never defaulted - a manifest that declares a blended model without it fails.
            if "coplanar_depth_epsilon_blocks" not in thresholds:
                raise AssertionError(
                    f"{model_id} declares visual_mode {mode['render_type']} but the manifest thresholds carry no "
                    "coplanar_depth_epsilon_blocks: the blended rasteriser's depth-tie window is a named tolerance "
                    "(an owner ruling, presented with its number) and has no default"
                )
            depth_epsilon = float(thresholds["coplanar_depth_epsilon_blocks"])
            vanilla_image, vanilla_contested = render_capture_blended(
                vanilla_samples[sample_id], texture, camera, mode, depth_epsilon)
            geo_image, geo_contested = render_capture_blended(geo_samples[sample_id], texture, camera, mode, depth_epsilon)
        else:
            vanilla_image, vanilla_contested = render_capture(vanilla_samples[sample_id], texture, camera)
            geo_image, geo_contested = render_capture(geo_samples[sample_id], texture, camera)
        contested = [left or right for left, right in zip(vanilla_contested, geo_contested)]
        contested_fraction = sum(contested) / (IMAGE_SIZE * IMAGE_SIZE)
        changed, mae, diff_image = pixel_diff(
            vanilla_image, geo_image, int(thresholds["pixel_channel_tolerance"]),
            contested if exclude_contested else None,
        )
        vanilla_foreground = foreground_fraction(vanilla_image)
        geo_foreground = foreground_fraction(geo_image)
        required_foreground = float(thresholds["minimum_foreground_fraction"])
        if min(vanilla_foreground, geo_foreground) < required_foreground:
            raise AssertionError(
                f"VISIBILITY MISMATCH {model_id}/{capture_id}: foreground fraction "
                f"{min(vanilla_foreground, geo_foreground):.9g} < {required_foreground}"
            )
        if changed > float(thresholds["pixel_changed_fraction"]):
            raise AssertionError(
                f"VISUAL MISMATCH {model_id}/{capture_id}: changed fraction {changed:.9g} > "
                f"{thresholds['pixel_changed_fraction']}"
            )
        if mae > float(thresholds["pixel_mean_absolute_error"]):
            raise AssertionError(
                f"VISUAL MISMATCH {model_id}/{capture_id}: MAE {mae:.9g} > "
                f"{thresholds['pixel_mean_absolute_error']}"
            )

        relative_base = Path("visual") / model_id
        vanilla_path = relative_base / f"{capture_id}.vanilla.png"
        geo_path = relative_base / f"{capture_id}.geo.png"
        diff_path = relative_base / f"{capture_id}.diff.png"
        save_png(vanilla_image, output_dir / vanilla_path)
        save_png(geo_image, output_dir / geo_path)
        save_png(diff_image, output_dir / diff_path)
        rows.append(
            {
                "sample": sample_id,
                "camera": camera_name,
                "changed_fraction": changed,
                "mean_absolute_error": mae,
                "contested_fraction": contested_fraction,
                "contested_excluded": exclude_contested,
                "vanilla_foreground_fraction": vanilla_foreground,
                "geo_foreground_fraction": geo_foreground,
                "vanilla_capture": vanilla_path.as_posix(),
                "geo_capture": geo_path.as_posix(),
                "diff_capture": diff_path.as_posix(),
            }
        )
        max_changed = max(max_changed, changed)
        max_mae = max(max_mae, mae)
        max_contested = max(max_contested, contested_fraction)
    # The contested fraction is not gated: with every pixel compared, a contested pixel
    # that resolves differently is a changed pixel and fails above; one that resolves the
    # same way (the ordinary case under the contract) is nothing to a player.

    report = {
        "status": "PASS",
        "image_size": [IMAGE_SIZE, IMAGE_SIZE],
        "camera": camera_spec,
        "channel_tolerance": thresholds["pixel_channel_tolerance"],
        "changed_fraction_threshold": thresholds["pixel_changed_fraction"],
        "mean_absolute_error_threshold": thresholds["pixel_mean_absolute_error"],
        "minimum_foreground_fraction_threshold": thresholds["minimum_foreground_fraction"],
        "max_changed_fraction": max_changed,
        "max_mean_absolute_error": max_mae,
        "z_fight_policy": (
            "pixels where two different quads meet the front within "
            f"{CONTEST_DEPTH_EPSILON:g} depth with different texels are draw-order z-fights, "
            "excluded from the comparison and painted in the diff (--contested-exclusion: the "
            "ruling-2 exclusion of 2026-09-02 run as a diagnostic; not the gate's policy)"
        ) if exclude_contested else (
            "pixels where two different quads meet the front within "
            f"{CONTEST_DEPTH_EPSILON:g} depth with different texels are counted as a diagnostic only; "
            "every pixel is compared, the draw order being contracted equal on both sides "
            "(G2 root-order contract, landed 2026-09-06)"
        ),
        "contested_exclusion_applied": exclude_contested,
        "max_contested_fraction": max_contested,
        "cutout_alpha_threshold": CUTOUT_ALPHA_THRESHOLD / 255.0,
        "minimum_observed_foreground_fraction": min_foreground,
        "samples": rows,
    }
    if mode is not None:
        # ENT-S-146: the declared mode and the GPU states the rasteriser emulated for it (only for a
        # model that declares one: every other report stays as it was).
        report["visual_mode"] = dict(mode)
        if mode["rasteriser"] == "render_capture_blended":
            report["visual_mode"]["emulated_states"] = dict(
                mode["emulated_states"], coplanar_depth_epsilon_blocks=float(thresholds["coplanar_depth_epsilon_blocks"]))
            report["z_fight_policy"] = (
                "blended mode: fragments within the coplanar depth epsilon "
                f"({float(thresholds['coplanar_depth_epsilon_blocks']):g} blocks, the manifest's own named tolerance) "
                "of the last written depth are one plane and all pass, in emission order, on both sides (the classic "
                "side's coplanar spokes tie to <= 1e-9; the cross-side same-fragment noise is ~1e-7 genuine plus up to "
                "~1e-6 from the harness projector's 6-decimal rounding); a pixel where another quad's fragment passed "
                "within that window is counted as contested, whatever its texel, as a diagnostic only - every pixel is "
                "compared, the draw and face orders being contracted equal on both sides (G2 root-order contract; "
                "ENT-S-146 face order)"
            )
    return report


# ENT-S-146 (refuter B, D3): the render state each side actually requested, recorded by the probe beside its
# dumps (`<id>.render-state.json`: the render-type FUNCTION's owner and RenderType factory, the colour and light
# observed at every addVertex) and required equal on both sides and equal to the model's visual mode.
RENDER_STATE_SUFFIX = ".render-state.json"
# LightTexture.pack(15, 15) = 15 << 4 | 15 << 20 = FULL_BRIGHT (21.1.223: `pack` = block << 4 | sky << 20).
FULL_BRIGHT_PACKED_LIGHT = 15728880
# The light the probe hands a side whose renderer decides nothing (RenderStateProbe.PROBE_LIGHT), on both sides.
PROBE_PACKED_LIGHT = 0


def render_state_parity(model_id: str, spec: dict[str, Any], mode: dict[str, Any] | None,
                        vanilla_dir: Path, geo_dir: Path) -> dict[str, Any]:
    """Both sides' recorded render state equal to each other and to the manifest's visual mode.

    The expectation is the declared mode, or the default every landed model runs under: render type
    ``entity_cutout_no_cull``, white vertices, the probe's light (``light: world``) - so a cutout rig
    whose classic model or descriptor ever turned translucent, coloured or fullbright fails loudly here
    even though its report entry carries no render-state field (only a model that declares a mode does,
    keeping every other entry byte-identical). The render type is the name of the single RenderType
    factory the render-type function's owner class references (the probe records every candidate; two
    or none is a failure here, not a guess): the probe cannot initialise RenderType (OPT-029 R0).
    """
    classic_path = vanilla_dir / f"{model_id}{RENDER_STATE_SUFFIX}"
    candidate_path = geo_dir / f"{model_id}{RENDER_STATE_SUFFIX}"
    for path in (classic_path, candidate_path):
        if not path.is_file():
            raise AssertionError(f"RENDER STATE MISSING {model_id}: the probe wrote no {path.name} (a stale probe run?)")
    classic = load_json(classic_path)
    candidate = load_json(candidate_path)
    if classic.get("side") != "classic" or candidate.get("side") != "candidate":
        raise AssertionError(f"RENDER STATE {model_id}: the sidecars' sides are not classic / candidate")
    expected_type = mode["render_type"] if mode else "entity_cutout_no_cull"
    expected_colour = list(mode["vertex_color"]) if mode else list(DEFAULT_VERTEX_COLOR)
    expected_light = FULL_BRIGHT_PACKED_LIGHT if (mode and mode["light"] == "full_bright") else PROBE_PACKED_LIGHT
    observed = {}
    for side, state in (("classic", classic), ("candidate", candidate)):
        render_type = state["render_type"]
        factories = render_type.get("render_type_factories") or []
        if len(factories) != 1 or render_type.get("render_type") is None:
            raise AssertionError(
                f"RENDER STATE AMBIGUOUS {model_id}/{side}: the render-type function's owner "
                f"{render_type.get('owner_class')} references {factories} RenderType factories; exactly one is required"
            )
        colour = state["vertex_color"]
        if len(colour.get("distinct_argb") or []) != 1 or "rgba" not in colour:
            raise AssertionError(
                f"RENDER STATE {model_id}/{side}: the captured vertices carried {colour.get('distinct_rgba')} colours; "
                "exactly one is required"
            )
        light = state["packed_light"]
        if len(light.get("distinct_observed") or []) != 1 or "value" not in light:
            raise AssertionError(
                f"RENDER STATE {model_id}/{side}: the captured vertices carried {light.get('distinct_observed')} packed "
                "lights; exactly one is required"
            )
        if int(colour["vertices_observed"]) == 0:
            raise AssertionError(f"RENDER STATE UNEVIDENCED {model_id}/{side}: no vertex was observed")
        observed[side] = {
            "render_type": render_type["render_type"],
            "render_type_factory": factories[0],
            "render_type_owner": render_type.get("owner_class"),
            "vertex_color": [int(value) for value in colour["rgba"]],
            "packed_light": int(light["value"]),
            "vertices_observed": int(colour["vertices_observed"]),
        }
    for field in ("render_type", "vertex_color", "packed_light"):
        left, right = observed["classic"][field], observed["candidate"][field]
        if left != right:
            raise AssertionError(
                f"RENDER STATE MISMATCH {model_id}: classic {field} {left} but the candidate requests {right}"
            )
        expected = {"render_type": expected_type, "vertex_color": expected_colour, "packed_light": expected_light}[field]
        if left != expected:
            raise AssertionError(
                f"RENDER STATE MISMATCH {model_id}: both sides request {field} {left} but the manifest's visual mode "
                f"({'declared' if mode else 'the default, entity_cutout_no_cull'}) is {expected}"
            )
    same_function = candidate["render_type"].get("same_function_object_as_classic")
    if candidate["render_type"].get("source", "").startswith("descriptor.renderType(entity), applied") and not same_function:
        raise AssertionError(
            f"RENDER STATE MISMATCH {model_id}: the descriptor hands over a render-type function that is not the "
            "classic model's own object"
        )
    return {
        "status": "PASS",
        "expected": {"render_type": expected_type, "vertex_color": expected_colour, "packed_light": expected_light,
                     "light": mode["light"] if mode else "world"},
        "classic": observed["classic"],
        "candidate": dict(observed["candidate"], same_function_object_as_classic=bool(same_function),
                          source=candidate["render_type"].get("source")),
        "classic_light_source": classic["packed_light"].get("source"),
        "observation": (
            "the render type is the RenderType factory the render-type function's owner class references (the "
            "probe cannot initialise RenderType: OPT-029 R0); the colour and light are the values every captured "
            "vertex carried after each side was handed what its renderer would hand it"
        ),
    }


def visual_mode_lines(visual: dict[str, Any]) -> list[str]:
    """ENT-S-146: the render-mode line for a model that declares a visual mode; nothing otherwise."""
    mode = visual.get("visual_mode")
    if not mode:
        return []
    states = mode["emulated_states"]
    blend = "no blending" if states["blend"] is None else "blend " + " / ".join(states["blend"])
    coplanar = (f" (fragments within {states['coplanar_depth_epsilon_blocks']:g} blocks are one plane and all pass)"
                if "coplanar_depth_epsilon_blocks" in states else "")
    lines = [
        f"- Render mode: {mode['render_type']} (vertex colour {tuple(mode['vertex_color'])}, light {mode['light']}): "
        f"{blend} over the background in emission order, {states['depth_test']} depth test with the depth "
        f"{'written' if states['depth_write'] else 'not written'}{coplanar}, texel alpha < {states['alpha_discard']:.3g} "
        "discarded; the same emulation on both sides."
    ]
    state = visual.get("render_state")
    if state:
        classic = state["classic"]
        lines.append(
            f"- Render state observed: both sides request {classic['render_type']} (RenderType.{classic['render_type_factory']}, "
            f"the classic model's own render-type function{' - the same object on the candidate' if state['candidate'].get('same_function_object_as_classic') else ''}), "
            f"vertex colour {tuple(classic['vertex_color'])} and packed light {classic['packed_light']} at every captured vertex "
            f"({classic['vertices_observed']} classic + {state['candidate']['vertices_observed']} candidate)."
        )
    return lines


def render_instance_lines(contract: dict[str, Any]) -> list[str]:
    """Slice 4c: the composition-leg line for a render-instance-expanded model; nothing otherwise."""
    expansion = contract.get("render_instances")
    if not expansion:
        return []
    parts = ", ".join(
        f"{name} x{part['count']} ({part['step_scope']}, {part['axis']})"
        for name, part in expansion["parts"].items()
    )
    return [
        f"- Render instances: {parts}; {expansion['clone_bones']} clone and {expansion['group_bones']} group bones; "
        f"{expansion['draws_checked']} measured draws over {expansion['samples_checked']} captures: instance pose "
        f"linear delta {expansion['max_instance_pose_linear_delta']:.12g}, translation "
        f"{expansion['max_instance_pose_translation_delta_model_units']:.12g} model units; draw pose linear "
        f"delta {expansion['max_draw_pose_linear_delta']:.12g}, translation "
        f"{expansion['max_draw_pose_translation_delta_model_units']:.12g} model units."
    ]


def contested_line(visual: dict[str, Any]) -> str:
    if visual.get("contested_exclusion_applied", False):
        return (f"- Visual z-fight pixels excluded (--contested-exclusion diagnostic run, not the gate's policy): "
                f"maximum contested fraction {visual['max_contested_fraction']:.12g}.")
    return (f"- Visual z-fight pixels compared, none excluded (G2 root-order contract): maximum contested "
            f"fraction {visual['max_contested_fraction']:.12g}, a diagnostic.")


def markdown_report(report: dict[str, Any]) -> str:
    lines = [
        "# Phase G1 proof — compiled LayerDefinition to GeckoLib geo",
        "",
        "Status: **PASS**",
        "",
        "Ground truth is each executed, compiled `createBodyLayer()` and its baked",
        "`ModelPart` tree. The generated side is parsed and baked by pinned GeckoLib",
        f"{report['geckolib_version']}, then captured through `GeoRenderer`.",
        "",
        "The independent gates are:",
        "",
        "- geometry: baked ModelPart world-space cube corners versus GeckoLib-rendered geo corners;",
        "- surface mapping: position/normal/UV tuple parity through the two pinned renderer paths;",
        "- animation: independently executed compiled `setupAnim` versus the actual fresh-baked candidate path;",
        "  Beaver uses the owner-approved exact `Mth.cos` custom-hook legacy-parity exception;",
        "  its emitted clip is reference-only, not runtime acceptance, and editable keyframes remain G3 work;",
        "- visual: independent software rasterization of concrete `EntityModel.renderToBuffer` and `GeoRenderer` streams using the shipped texture;",
        "  every pixel is compared (G2 root-order contract, 2026-09-06) and the z-fight contested fraction is reported as a diagnostic only;",
        "- draw order: per full capture, the sequence of parts the classic `renderToBuffer` drew equals the sequence of bones `GeoRenderer` emitted,",
        "  and the order shipped in each geo (`orespawn:bone_draw_order`) equals the converter's, the probe's and the fresh bake's traversal.",
        "",
    ]
    for model in report["models"]:
        lines.extend(
            [
                f"## {model['model_id']} (Tier {model['tier']})",
                "",
                f"- Exact bones: {model['bone_count']}; cubes: {model['cube_count']}.",
                f"- Geometry maximum corner delta: {model['geometry']['max_corner_delta_blocks']:.12g} blocks "
                f"(epsilon {model['geometry']['epsilon_blocks']:.12g}).",
                f"- Surface maximum UV delta: {model['surface_mapping']['max_uv_delta_normalized']:.12g}; "
                f"normal delta: {model['surface_mapping']['max_normal_delta']:.12g}.",
                f"- Animation maximum rotation delta: {model['animation']['max_rotation_delta_radians']:.12g} radians "
                f"(epsilon {model['animation']['epsilon_radians']:.12g}).",
                f"- Visual maximum changed fraction: {model['visual']['max_changed_fraction']:.12g}; "
                f"maximum mean absolute error: {model['visual']['max_mean_absolute_error']:.12g}.",
                f"- Draw order: GeckoLib bone order equals the classic draw order over "
                f"{model['draw_order']['captures_checked']} captures ({model['draw_order']['draws_checked']} draws).",
                "",
            ]
        )
        contract = model["animation"]["contract"]
        if contract["kind"] == "gait_scaled":
            lines.extend(
                [
                    f"- Accepted path: {contract['accepted_runtime_path']}.",
                    f"- Dense actual-candidate maximum delta: "
                    f"{model['animation']['max_dense_rotation_delta_radians']:.12g} radians over "
                    f"{contract['dense_transform_sample_count_total']} samples; minimum authored-key/probe "
                    f"separation {contract['minimum_key_probe_separation_age_ticks']:.12g} age ticks, "
                    f"coincidences {contract['off_grid_probe_key_coincidences']}.",
                    f"- Candidate gait proportionality maximum delta: "
                    f"{contract['max_candidate_gait_proportionality_delta_radians']:.12g} radians over "
                    f"amplitudes {contract['limb_swing_amount_samples']}; candidate unscaled-channel delta "
                    f"{contract['max_candidate_unscaled_channel_amplitude_delta_radians']:.12g}.",
                    f"- Reference JSON: `{contract['reference_animation_role']}`; baked-keyframe runtime "
                    "acceptance is false and artist-editable math-to-keyframes remains `OUTSTANDING_G3`.",
                    *keyframe_leg_lines(model.get(KEYFRAME_LEG_KEY)),
                    "",
                ]
            )
        elif contract["kind"] == "code_driven":
            lines.extend(
                [
                    f"- Accepted path: {contract['accepted_runtime_path']} "
                    f"(`{contract['candidate_class']}`).",
                    f"- Rotation maximum delta {model['animation']['max_rotation_delta_radians']:.12g} radians; "
                    f"position maximum delta {contract['max_position_delta_model_units']:.12g} model units "
                    f"over {contract['position_channel_samples']} position channels; inputs "
                    f"{contract['inputs']}.",
                    contested_line(model["visual"]),
                    *visual_mode_lines(model["visual"]),
                    *render_instance_lines(contract),
                    # A code_driven model may declare the keyframe reference leg (the Tier-2 transcriptions, 2026-09-13):
                    # its README lines are the gait_scaled branch's (the T2a refuter, M2).
                    *keyframe_leg_lines(model.get(KEYFRAME_LEG_KEY)),
                    "",
                ]
            )
        elif contract["kind"] == "entity_state":
            lines.extend(
                [
                    f"- Accepted path: {contract['accepted_runtime_path']} "
                    f"(`{contract['candidate_class']}`).",
                    f"- Entity states: {[state['name'] for state in contract['entity_states']]}; "
                    f"rotation maximum delta {model['animation']['max_rotation_delta_radians']:.12g} radians; "
                    f"position maximum delta {contract['max_position_delta_model_units']:.12g} model units; "
                    f"hidden-bone checks {contract['hidden_bone_checks']}.",
                    contested_line(model["visual"]),
                    *visual_mode_lines(model["visual"]),
                    *render_instance_lines(contract),
                    "",
                ]
            )
        else:
            lines.extend(
                [
                    f"- Static identity maximum rotation motion: "
                    f"{contract['max_identity_rotation_motion']:.12g} radians; no controller emitted.",
                    "",
                ]
            )
    for fixture in report["fixtures"]:
        lines.extend([
            f"## {fixture['model_id']} (non-production fixture)",
            "",
            f"- Coverage: {', '.join(fixture['fixture_coverage']['required_coverage'])}.",
            f"- Geometry maximum corner delta: {fixture['geometry']['max_corner_delta_blocks']:.12g} blocks; "
            f"surface UV maximum {fixture['surface_mapping']['max_uv_delta_normalized']:.12g}.",
            f"- Draw order: GeckoLib bone order equals the classic draw order over "
            f"{fixture['draw_order']['captures_checked']} captures ({fixture['draw_order']['draws_checked']} draws).",
        ])
        fixture_contract = fixture["animation"]["contract"]
        if fixture_contract["kind"] == "code_driven":
            lines.append(
                f"- Runtime basis proof: `{fixture_contract['candidate_class']}` rotation maximum delta "
                f"{fixture['animation']['max_rotation_delta_radians']:.12g} radians, position maximum delta "
                f"{fixture_contract['max_position_delta_model_units']:.12g} model units, surface mapping "
                f"exact over {fixture['surface_mapping']['vertex_samples']} posed vertex samples."
            )
        lines.append("")
    lines.extend(
        [
            "Reproduce with `gradlew.bat g1Parity`. Any mismatch exits nonzero before",
            "proof evidence can be updated.",
            "",
        ]
    )
    return "\n".join(lines)


def proof_file_map(manifest: dict[str, Any], report: dict[str, Any], generated_dir: Path,
                   evidence_dir: Path, reference_dir: Path | None = None) -> dict[Path, Path]:
    files: dict[Path, Path] = {}
    for spec in [*manifest["models"], *manifest.get("fixtures", [])]:
        model_id = spec["id"]
        for suffix in (
            "geo.json", "animation.json", "animation-contract.json",
            "conversion.json",
        ):
            files[Path("generated") / f"{model_id}.{suffix}"] = generated_dir / f"{model_id}.{suffix}"
    for spec in manifest["models"]:
        if "reference_source" in spec and reference_dir is not None:
            files[Path("reference") / f"{spec['id']}.reference-geometry.json"] = (
                reference_dir / f"{spec['id']}.reference-geometry.json"
            )
    files[Path("evidence") / "report.json"] = evidence_dir / "report.json"
    files[Path("evidence") / "README.md"] = evidence_dir / "README.md"
    for model in report["models"]:
        for sample in model["visual"]["samples"]:
            for field in ("vanilla_capture", "geo_capture", "diff_capture"):
                relative = Path(sample[field])
                files[Path("evidence") / relative] = evidence_dir / relative
    return files


def keyframe_leg_lines(leg: dict[str, Any] | None) -> list[str]:
    """The keyframe reference leg's report lines: the density statement first, then the numbers behind it."""
    if leg is None:
        return []
    grid = leg["sample_grid"]
    search = leg["density_search"]
    return [
        f"- Keyframe reference leg: {leg['statement']}.",
        f"- Keyframe leg: `{leg['controller_class']}` layers of `{leg['candidate_class']}` over clip "
        f"`{leg['clip_path']}` (sha256 {leg['clip_sha256'][:12]}), spline repair "
        f"{'ON' if leg['spline_repair'] else 'OFF'}; sample-grid max delta "
        f"{max(grid['max_error_radians_by_group'].values()):.12g} radians over "
        f"{grid['layer_bone_samples_compared']} layer-bone samples "
        f"({', '.join(f'{group} {value:.6g}' for group, value in grid['max_error_radians_by_group'].items())}); "
        f"{leg['wrap_pairs']['count']} wrap pairs, |v(T-eps) - v(0+eps)| max "
        f"{leg['wrap_pairs']['max_abs_T_minus_eps_vs_0_plus_eps_radians']:.6g}; non-layer bones moved "
        f"{grid['max_non_layer_bone_motion_radians']:.6g}.",
        f"- Keyframe density search (an output): "
        + "; ".join(
            f"{group} fewest {row['fewest_keys_per_bone']} keys/bone at {row['confirmed_max_error_radians']:.6g} "
            f"over {row['confirmed_comparisons']} comparisons, one fewer {row['one_fewer_max_error_radians']:.6g}"
            for group, row in search.items()
        )
        + f"; dense schedule of the shipped-candidate clip: "
        + ", ".join(f"{group} {row['max_error_radians']:.6g}" for group, row in leg["clip_full_schedule"].items())
        + f"; reversed schedule max delta {leg['schedule_reversed_max_delta_radians']:.6g}.",
        f"- Keyframe late-prime twin (TEST-005): a manager built before its clips were served (unserved through the slot at age "
        f"{leg['late_prime_twin']['unserved_slot_age_ticks']:g}, no tick, bone motion "
        f"{leg['late_prime_twin']['unserved_bone_motion_radians']:g}), primed at age "
        f"{leg['late_prime_twin']['prime_age_ticks']:g}: "
        + "; ".join(
            f"{group} declared {row['declared_clip_ticks']:g} ticks, {row['clip_ticks_per_age_tick']:.6g} clip ticks per age "
            f"tick, LUT index {row['last_cosine_index']}, clip tick {row['last_clip_tick']:.6g}"
            for group, row in leg["late_prime_twin"]["prime"].items()
        )
        + f" - every fact the from-zero prime's; pose delta {leg['late_prime_twin']['prime_pose_max_delta_vs_from_zero_radians']:g} "
        f"at the prime, {leg['late_prime_twin']['follow_up_pose_max_delta_vs_from_zero_radians']:g} over the follow-ups.",
    ]


def synchronize_or_verify_proof(proof_dir: Path, files: dict[Path, Path], write_proof: bool) -> None:
    scoped_roots = (proof_dir / "generated", proof_dir / "evidence", proof_dir / "reference")
    if write_proof:
        for scoped_root in scoped_roots:
            if scoped_root.exists():
                shutil.rmtree(scoped_root)
    for relative, source in files.items():
        target = proof_dir / relative
        if write_proof:
            target.parent.mkdir(parents=True, exist_ok=True)
            shutil.copyfile(source, target)
            continue
        if not target.is_file():
            raise AssertionError(
                f"checked-in G1 proof file missing: {relative}; run the green harness with --write-proof"
            )
        if source.read_bytes() != target.read_bytes():
            raise AssertionError(
                f"checked-in G1 proof drift: {relative} (generated {sha256(source)}, expected {sha256(target)})"
            )
    expected = {relative.as_posix() for relative in files}
    actual = {
        path.relative_to(proof_dir).as_posix()
        for scoped_root in scoped_roots if scoped_root.exists()
        for path in scoped_root.rglob("*") if path.is_file()
    }
    extras = sorted(actual - expected)
    missing = sorted(expected - actual)
    if extras or missing:
        raise AssertionError(
            f"checked-in G1 proof artifact set drift; missing={missing}, extras={extras}"
        )


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--manifest", type=Path, required=True)
    parser.add_argument("--vanilla-dir", type=Path, required=True)
    parser.add_argument("--generated-dir", type=Path, required=True)
    parser.add_argument("--geo-dir", type=Path, required=True)
    parser.add_argument("--output-dir", type=Path, required=True)
    parser.add_argument("--proof-dir", type=Path, required=True)
    parser.add_argument("--reference-dir", type=Path, default=None,
                        help="reference_geometry_leg.py output; required for manifests declaring reference_source")
    parser.add_argument("--write-proof", action="store_true")
    parser.add_argument("--validate-only", action="store_true")
    parser.add_argument("--contested-exclusion", action="store_true",
                        help="diagnostic only: restore the ruling-2 (2026-09-02) z-fight exclusion, skipping "
                             "contested pixels and painting them in the diff. The gate's policy since the G2 "
                             "root-order contract (2026-09-06) is the default: every pixel compared, the "
                             "contested fraction reported. Never writes a proof.")
    args = parser.parse_args()
    if args.write_proof and args.validate_only:
        parser.error("--write-proof and --validate-only are mutually exclusive")
    if args.write_proof and args.contested_exclusion:
        parser.error("--contested-exclusion is a diagnostic; the proof is written under the default policy only")

    manifest = load_json(args.manifest)
    repository_root = args.manifest.resolve().parent.parent
    thresholds = manifest["thresholds"]
    args.output_dir.mkdir(parents=True, exist_ok=True)
    visual_output = args.output_dir / "visual"
    if visual_output.exists():
        shutil.rmtree(visual_output)
    visual_output.mkdir(parents=True)
    model_reports: list[dict[str, Any]] = []
    fixture_reports: list[dict[str, Any]] = []
    lf_paths: list[Path] = []

    for spec in [*manifest["models"], *manifest.get("fixtures", [])]:
        model_id = spec["id"]
        compiled_path = args.vanilla_dir / f"{model_id}.compiled.json"
        geo_render_path = args.geo_dir / f"{model_id}.geo-render.json"
        geometry_path = args.generated_dir / f"{model_id}.geo.json"
        animation_path = args.generated_dir / f"{model_id}.animation.json"
        contract_path = args.generated_dir / f"{model_id}.animation-contract.json"
        conversion_path = args.generated_dir / f"{model_id}.conversion.json"
        compiled = load_json(compiled_path)
        geo_render = load_json(geo_render_path)
        generated_geometry = load_json(geometry_path)
        reference_animation = load_json(animation_path)
        animation_contract = load_json(contract_path)
        conversion = load_json(conversion_path)
        lf_paths.extend([
            compiled_path, geo_render_path, geometry_path, animation_path,
            contract_path, conversion_path,
        ])

        for field, path in (
            ("geometry_sha256", geometry_path),
            ("animation_sha256", animation_path),
            ("animation_contract_sha256", contract_path),
            ("compiled_dump_sha256", compiled_path),
        ):
            if conversion[field] != sha256(path):
                raise AssertionError(f"{model_id} conversion provenance hash drift for {field}")

        expected_names = candidate_bone_names(model_id, spec, compiled, conversion)
        if sorted(conversion["exact_bone_names"]) != expected_names:
            raise AssertionError(f"{model_id} converter changed exact bone names")
        if sorted(geo_render["bone_names"]) != expected_names:
            raise AssertionError(f"{model_id} GeckoLib bake changed exact bone names")

        geometry = geometry_parity(
            model_id, compiled, geo_render, float(thresholds["geometry_epsilon_blocks"])
        )
        print(
            f"G1 GEOMETRY PASS: {model_id} {geometry['cube_sample_count']} cube-samples, "
            f"max delta {geometry['max_corner_delta_blocks']:.12g} blocks"
        )
        surface_mapping = surface_mapping_parity(
            model_id,
            compiled,
            geo_render,
            float(thresholds["geometry_epsilon_blocks"]),
            float(thresholds["normal_epsilon"]),
            float(thresholds["uv_epsilon_normalized"]),
        )
        print(
            f"G1 SURFACE PASS: {model_id} {surface_mapping['vertex_samples']} vertex-samples, "
            f"{surface_mapping['ignored_zero_area_faces']} zero-area faces ignored, "
            f"max UV {surface_mapping['max_uv_delta_normalized']:.12g}, "
            f"max normal {surface_mapping['max_normal_delta']:.12g}"
        )
        animation = animation_parity(
            model_id, spec, compiled, geo_render, animation_contract,
            float(thresholds["animation_epsilon_radians"]),
            position_epsilon=float(thresholds.get("position_epsilon_model_units", 1.0e-4)),
            repository_root=repository_root,
            conversion=conversion,
        )
        print(
            f"G1 ANIMATION PASS: {model_id} max delta "
            f"{animation['max_rotation_delta_radians']:.12g} radians"
        )
        keyframe_leg = None
        if KEYFRAME_LEG_KEY in spec:
            keyframe_clip_path = resolve_repository_path(repository_root, spec[KEYFRAME_LEG_KEY]["clip_path"])
            keyframe_leg = keyframe_reference_leg_parity(
                model_id, spec, compiled, geo_render, thresholds, keyframe_clip_path,
                resolve_repository_path(repository_root, spec[KEYFRAME_LEG_KEY]["clip_manifest"]),
            )
            lf_paths.append(keyframe_clip_path)
            # The console line is ASCII (a cp1252 stdout under Gradle's Exec); the report keeps the ruling's spelling.
            print(
                f"G1 KEYFRAME LEG PASS: {model_id} "
                f"{keyframe_leg['statement'].replace('−', '-').replace('ε', 'eps')}; sample-grid max "
                f"{max(keyframe_leg['sample_grid']['max_error_radians_by_group'].values()):.12g} radians over "
                f"{keyframe_leg['sample_grid']['layer_bone_samples_compared']} layer-bone samples, "
                f"{keyframe_leg['wrap_pairs']['count']} wrap pairs"
            )
        reference_schema = reference_animation_schema(
            model_id, spec, compiled, reference_animation, animation_contract,
            conversion, float(manifest["ticks_per_second"]),
        )
        draw_order = draw_order_parity(model_id, compiled, geo_render, generated_geometry, conversion,
                                       float(thresholds["normal_epsilon"]))
        print(
            f"G1 DRAW ORDER PASS: {model_id} {draw_order['captures_checked']} captures, "
            f"{draw_order['draws_checked']} draws in the classic order"
        )
        if "cube_face_order" in draw_order:
            print(
                f"G1 FACE ORDER PASS: {model_id} {draw_order['cube_face_order']['faces_checked']} faces over "
                f"{draw_order['cube_face_order']['captures_checked']} captures in the classic cube order"
            )
        # ENT-S-146 (refuter A, D1): a model that declares a BLENDING visual mode must ship the face-order key -
        # the per-face check fails, it does not skip, when the generated geo carries none.
        declared_mode = visual_mode(model_id, spec)
        if declared_mode is not None and VISUAL_MODES[declared_mode["render_type"]]["blend"] is not None \
                and "cube_face_order" not in draw_order:
            raise AssertionError(
                f"FACE ORDER REQUIRED {model_id}: visual_mode {declared_mode['render_type']} blends, but the generated "
                f"geo carries no {FACE_ORDER_KEY} (declare cube_face_order: \"classic\" and regenerate)"
            )
        common_report = {
            "model_id": model_id,
            "tier": spec["tier"],
            "proof_scope": spec.get("proof_scope", "production_proof_model"),
            "source_class": spec["class"],
            "source_class_sha256": compiled["source_class_sha256"],
            "compiled_dump_sha256": conversion["compiled_dump_sha256"],
            "bone_count": conversion["bone_count"],
            "cube_count": conversion["cube_count"],
            "geometry_sha256": conversion["geometry_sha256"],
            "animation_sha256": conversion["animation_sha256"],
            "animation_contract_sha256": conversion["animation_contract_sha256"],
            "geometry": geometry,
            "surface_mapping": surface_mapping,
            "animation": animation,
            "reference_animation": reference_schema,
            "draw_order": draw_order,
        }
        if keyframe_leg is not None:
            common_report[KEYFRAME_LEG_KEY] = keyframe_leg
        if "reference_source" in spec:
            if args.reference_dir is None:
                raise AssertionError(f"{model_id} declares reference_source but no --reference-dir was given")
            reference_path = args.reference_dir / f"{model_id}.reference-geometry.json"
            if not reference_path.is_file():
                raise AssertionError(f"{model_id} reference-geometry leg output is missing: {reference_path}")
            reference_leg = load_json(reference_path)
            if reference_leg.get("status") != "PASS":
                raise AssertionError(
                    f"REFERENCE GEOMETRY {reference_leg.get('status')} {model_id}: "
                    f"{reference_leg.get('comparison', {}).get('differences') or reference_leg.get('reference', {}).get('reason')}"
                )
            lf_paths.append(reference_path)
            common_report["reference_geometry"] = {
                "status": "PASS",
                "reference_source": spec["reference_source"],
                "matched_parts": reference_leg["comparison"]["matched_parts"],
                "ground_truth": reference_leg["ground_truth"],
            }
            print(
                f"G1 REFERENCE PASS: {model_id} {reference_leg['comparison']['matched_parts']} parts "
                f"match the parsed 1.7.10 source"
            )
        if spec.get("proof_scope") == "non_production_converter_fixture":
            common_report["fixture_coverage"] = fixture_coverage(
                spec, compiled, generated_geometry
            )
            fixture_reports.append(common_report)
            print(f"G1 FIXTURE PASS: {model_id} all declared converter cases observed")
        else:
            visual = visual_parity(
                model_id, spec, compiled, geo_render, repository_root, args.output_dir, thresholds,
                exclude_contested=args.contested_exclusion,
            )
            print(
                f"G1 VISUAL PASS: {model_id} max changed {visual['max_changed_fraction']:.12g}, "
                f"max MAE {visual['max_mean_absolute_error']:.12g}, "
                f"max contested {visual['max_contested_fraction']:.12g} "
                f"({'excluded: --contested-exclusion diagnostic run' if visual['contested_exclusion_applied'] else 'compared, not excluded; a diagnostic'})"
            )
            # ENT-S-146 (refuter B, D3): the render state both sides actually requested, against the mode the
            # visual leg emulated - for EVERY model (a cutout rig reporting anything but cutout / white / the
            # probe's light fails here); recorded in the report only for a model that declares a mode.
            render_state = render_state_parity(model_id, spec, declared_mode, args.vanilla_dir, args.geo_dir)
            print(
                f"G1 RENDER STATE PASS: {model_id} {render_state['classic']['render_type']} / "
                f"{tuple(render_state['classic']['vertex_color'])} / light {render_state['classic']['packed_light']} "
                f"on both sides ({render_state['classic']['vertices_observed']} + "
                f"{render_state['candidate']['vertices_observed']} vertices observed)"
            )
            if declared_mode is not None:
                visual["render_state"] = render_state
            common_report["visual"] = visual
            model_reports.append(common_report)

    line_endings = verify_fixed_lf(lf_paths)
    referenced_pngs = {
        Path(sample[field]).as_posix()
        for model in model_reports
        for sample in model["visual"]["samples"]
        for field in ("vanilla_capture", "geo_capture", "diff_capture")
    }
    actual_pngs = {
        path.relative_to(args.output_dir).as_posix()
        for path in visual_output.rglob("*.png")
    }
    if actual_pngs != referenced_pngs:
        raise AssertionError(
            f"current visual artifact set drift; missing={sorted(referenced_pngs - actual_pngs)}, "
            f"extras={sorted(actual_pngs - referenced_pngs)}"
        )

    report = {
        "schema_version": 1,
        "status": "PASS",
        "ground_truth": "executed compiled LayerDefinition + baked ModelPart trees",
        "geckolib_version": manifest["geckolib_version"],
        "thresholds": thresholds,
        "contested_exclusion_applied": args.contested_exclusion,
        "models": model_reports,
        "fixtures": fixture_reports,
        "deterministic_text_outputs": line_endings,
        "visual_artifact_count": len(actual_pngs),
        "visual_artifact_policy": "output cleared first; exact report-referenced PNG set only",
    }
    write_bytes(args.output_dir / "report.json", json_bytes(report))
    write_bytes(args.output_dir / "README.md", markdown_report(report).encode("utf-8"))
    if args.validate_only:
        print(f"G1 PARITY STAGING PASS: {len(model_reports)} models; no proof written")
        return 0
    synchronize_or_verify_proof(
        args.proof_dir,
        proof_file_map(manifest, report, args.generated_dir, args.output_dir, args.reference_dir),
        args.write_proof,
    )
    action = "updated" if args.write_proof else "verified"
    print(f"G1 PARITY PASS: {len(model_reports)} models; checked-in proof {action}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
