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
# Fragments of different quads within this depth of each other are a z-fight
# (draw-order resolved in both renderers; ruling 2, 2026-09-02: not a parity target).
CONTEST_DEPTH_EPSILON = 1.0e-6
# Owner ruling 2026-09-02: a species whose pinned excluded fraction exceeds this needs a
# specific in-game acceptance from the owner (Robot5 is the first).
IN_GAME_ACCEPTANCE_CONTESTED_FRACTION = 0.005
CONTESTED_MARKER = (40, 90, 255, 255)
DEFAULT_VISUAL_SAMPLE_IDS = ("bind", "t0", "t_quarter", "t_half", "t_three_quarter")


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


def animation_parity(model_id: str, spec: dict[str, Any], compiled: dict[str, Any],
                     geo_render: dict[str, Any], contract: dict[str, Any],
                     epsilon: float, position_epsilon: float = 1.0e-4,
                     repository_root: Path | None = None) -> dict[str, Any]:
    """Compare independent compiled setupAnim output with the actual candidate hook."""
    vanilla_samples = sample_map(compiled)
    candidate_samples = sample_map(geo_render)
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
        if transforms.keys() != candidate_rotations.keys():
            raise AssertionError(f"{model_id}/{current_id} candidate bone set differs from compiled model")
        for bone, transform in transforms.items():
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
                "limb_swing_amounts": spec.get("limb_swing_amount_samples", [spec["limb_swing_amount"]]),
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
                "limb_swing_amounts": spec.get("limb_swing_amount_samples", [spec["limb_swing_amount"]]),
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
    Both real renderers resolve that by draw order, which ruling 2 (2026-09-02)
    excludes from parity; the mask lets the comparison skip exactly those
    pixels and report how many there were.
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
                  output_dir: Path, thresholds: dict[str, Any]) -> dict[str, Any]:
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
        vanilla_image, vanilla_contested = render_capture(vanilla_samples[sample_id], texture, camera)
        geo_image, geo_contested = render_capture(geo_samples[sample_id], texture, camera)
        contested = [left or right for left, right in zip(vanilla_contested, geo_contested)]
        contested_fraction = sum(contested) / (IMAGE_SIZE * IMAGE_SIZE)
        changed, mae, diff_image = pixel_diff(
            vanilla_image, geo_image, int(thresholds["pixel_channel_tolerance"]), contested
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
    # Owner condition (2026-09-02): the excluded fraction is PINNED per species in the
    # manifest; growth fails the leg until the pin is raised explicitly, like a tolerance.
    if "max_contested_fraction_pin" not in spec:
        raise AssertionError(f"{model_id} manifest declares no max_contested_fraction_pin")
    contested_pin = float(spec["max_contested_fraction_pin"])
    if max_contested > contested_pin:
        raise AssertionError(
            f"CONTESTED PIN EXCEEDED {model_id}: excluded z-fight fraction {max_contested:.12g} > "
            f"pinned {contested_pin:.12g}; raising the pin is an owner ruling"
        )
        min_foreground = min(min_foreground, vanilla_foreground, geo_foreground)

    return {
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
            "excluded from the comparison and painted in the diff (ruling 2, 2026-09-02)"
        ),
        "max_contested_fraction": max_contested,
        "contested_fraction_pin": contested_pin,
        "requires_in_game_acceptance": contested_pin > IN_GAME_ACCEPTANCE_CONTESTED_FRACTION,
        "cutout_alpha_threshold": CUTOUT_ALPHA_THRESHOLD / 255.0,
        "minimum_observed_foreground_fraction": min_foreground,
        "samples": rows,
    }


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
        "- visual: independent software rasterization of concrete `EntityModel.renderToBuffer` and `GeoRenderer` streams using the shipped texture.",
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
                    f"- Visual z-fight pixels excluded (ruling 2): maximum contested fraction "
                    f"{model['visual']['max_contested_fraction']:.12g}.",
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
                    f"- Visual z-fight pixels excluded (ruling 2): maximum contested fraction "
                    f"{model['visual']['max_contested_fraction']:.12g}.",
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
    args = parser.parse_args()
    if args.write_proof and args.validate_only:
        parser.error("--write-proof and --validate-only are mutually exclusive")

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

        expected_names = sorted(compiled["bone_names"])
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
        )
        print(
            f"G1 ANIMATION PASS: {model_id} max delta "
            f"{animation['max_rotation_delta_radians']:.12g} radians"
        )
        reference_schema = reference_animation_schema(
            model_id, spec, compiled, reference_animation, animation_contract,
            conversion, float(manifest["ticks_per_second"]),
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
        }
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
                model_id, spec, compiled, geo_render, repository_root, args.output_dir, thresholds
            )
            print(
                f"G1 VISUAL PASS: {model_id} max changed {visual['max_changed_fraction']:.12g}, "
                f"max MAE {visual['max_mean_absolute_error']:.12g}, "
                f"max contested {visual['max_contested_fraction']:.12g} "
                f"(pin {visual['contested_fraction_pin']:.12g}"
                f"{', IN-GAME ACCEPTANCE REQUIRED' if visual['requires_in_game_acceptance'] else ''})"
            )
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
