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
import hashlib
import json
import math
from pathlib import Path
from typing import Any, Iterable


ALL_FACES = {"down", "up", "west", "north", "east", "south"}


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


def convert_geometry(compiled: dict[str, Any]) -> tuple[dict[str, Any], dict[str, Any]]:
    root = compiled["definition"]
    if root["cubes"]:
        raise ValueError("unnamed MeshDefinition root contains cubes")

    bones: list[dict[str, Any]] = []
    cube_count = 0
    for part, parent in iter_parts(root):
        name = part["name"]
        absolute_pivot = [float(value) for value in part["absolute_pivot"]]
        initial_rotation = [float(value) for value in part["initial_rotation_radians"]]
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
    if output_names != input_names:
        raise ValueError(f"bone-name drift: input {input_names} != output {output_names}")
    if len(output_names) != len(set(output_names)):
        raise ValueError("duplicate bone names cannot be preserved by GeckoLib")

    model_id = compiled["model_id"]
    geometry = {
        "format_version": "1.12.0",
        "minecraft:geometry": [
            {
                "description": {
                    "identifier": f"geometry.orespawn.g1.{model_id}",
                    "texture_width": compiled["texture_width"],
                    "texture_height": compiled["texture_height"],
                },
                "bones": bones,
            }
        ],
    }
    summary = {
        "bone_count": len(bones),
        "cube_count": cube_count,
        "mirrored_cube_count": sum(
            1
            for part, _parent in iter_parts(root)
            for cube in part["cubes"]
            if cube["mirror"]
        ),
        "mirrored_uv_strategy": (
            "source flag retained as modelpart_mirror; semantics baked into explicit faces; "
            "incompatible GeckoLib native mirror disabled"
        ),
        "exact_bone_names": output_names,
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
        if spec["channels"]:
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

    geometry, geometry_summary = convert_geometry(compiled)
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
