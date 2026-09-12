#!/usr/bin/env python3
"""Generate a species' classic-transcription keyframe clips from its frequency-group manifest.

The Phase G standard animation contract (phase_g_reports/animation_contract/contract_design.md,
ruled 2026-09-06) transcribes a species' classic trig animation as ONE looping clip PER FREQUENCY
GROUP (Amendment 1 point 5): the gait group's clip is ``walk``, every other group's is
``walk_<group>`` - and a species with ONE frequency group has only the bare names (section 2.1), so
its one group's clip is ``walk`` whether or not it is gait-scaled. This tool reads a clip manifest
(``tools/keyframe_clips/<species>.json``) and the classic channels it points at in the model manifest
(``tools/g1_model_proofs.json`` / ``tools/t2_model_proofs.json``: bones, axis, cosine formula, frequency,
wingspeed, pi_scale, sign, base_radians, limb_swing_scaled) and writes the ``.animation.json``:

* every loop normalised to ``animation_length`` 1.0 s (Q14 (a)); the tempo is the controller's,
  derived from the DECLARED length (ADDENDA (1)) - a key at time ``L * k / (N - 1)`` holds the
  classic value at phase ``2 pi k / (N - 1)``; the frequency (``omega`` and the chain's ``wingspeed``)
  never enters a key - it is the controller's phase lock;
* authored at FULL amplitude (the controller scales the gait group's delta by ``limbSwingAmount``,
  Amendment 1 point 3): amplitude ``(float) Math.PI * <pi_scale>F`` in the compiled float chain,
  converted to degrees, with the channel's sign;
* as the DELTA from the bone's bind rotation on the channel's axis: GeckoLib adds every key to the
  bone's initial snapshot (``AnimationProcessor.tickAnimation`` 323-341), so a classic
  ``base + cos(...)`` written over a bone that binds at ``bind`` on that axis is keyed as
  ``(base - bind) + cos(...)``; the bind is read from the SHIPPED geo the clip manifest names
  (``geo``: the converter wrote JSON ``(+x, -y, -z)`` degrees), the same decimal text the harness's
  twin reads. A clip manifest without ``geo`` (the Beaver's) is a rig whose channel bones bind
  unrotated on their axes - the harness checks that;
* under the converter's sign rule (``tools/layer_definition_to_geo.py`` ``json_rotation_delta``):
  authored X = +classic degrees, Y and Z NEGATED, in the axis's slot of the key. GeckoLib 4.8.4
  negates constant X and Y keys at load (``BakedAnimationsAdapter.buildKeyframeStack`` 209-224 /
  250-265), so the loaded value lands on the internal basis ``(-xRot, yRot, -zRot)`` that the shipped
  code-driven hook writes. The salvaged ``tools/g3_beaver_animation.py`` authored the opposite sign
  (Q17 (a): regenerated here; ``controller_design.md`` section 12);
* with the density the manifest records per group - an OUTPUT of the harness (the
  ``keyframe_reference_leg`` density search: the fewest keys per bone holding 2.5e-3 rad under the
  repaired catmullrom evaluator), which the harness re-derives and checks against this file on every
  run; ``lerp_mode`` as the manifest says;
* plus, where the manifest declares ``"idle": {"keys": "none"}``, an ``idle`` loop that keys no bone: the
  in-game gate opens on ``idle`` AND ``walk`` together (owner 2026-09-12, item 12), and a classic transcription
  has no resting motion of its own (the always-on groups carry it; the gait group sits at limbSwingAmount 0),
  so its idle is the bind pose until an artist's real idle replaces it.

Deterministic: LF line endings, UTF-8, two-space JSON, values rounded to 1e-10 degrees, ``-0.0``
written as ``0.0``. The G1 probe regenerates the same keys in memory (``KeyframeLeg``) and pins the
file against them, so the two generators cannot drift apart silently.

Usage:
    python tools/keyframe_clip.py --clip-manifest tools/keyframe_clips/beaver.json --output <path>
        [--keys-per-bone gait=15,teeth=13,tail=8] [--lerp-mode linear|catmullrom]
"""

from __future__ import annotations

import argparse
import json
import math
import struct
import sys
from pathlib import Path


REPOSITORY_ROOT = Path(__file__).resolve().parents[1]
TIME_DECIMALS = 10
VALUE_DECIMALS = 10
FREQUENCY_EPSILON = 1.0e-9
AXIS_INDEX = {"x": 0, "y": 1, "z": 2}
# The converter's rule: authored X = +classic degrees, Y and Z negated (json_rotation_delta).
AUTHORED_SIGN = {"x": 1.0, "y": -1.0, "z": -1.0}


def java_float(value: float) -> float:
    """Round exactly as a Java float constant / arithmetic result."""
    return struct.unpack(">f", struct.pack(">f", value))[0]


def classic_amplitude_degrees(pi_scale: float) -> float:
    """``(float) Math.PI * <pi_scale>F`` (the compiled chain), in degrees."""
    radians = java_float(java_float(math.pi) * java_float(pi_scale))
    return math.degrees(radians)


def classic_base_degrees(base_radians: float) -> float:
    """The channel's constant offset, a float literal in the classic formula, in degrees."""
    return math.degrees(java_float(base_radians))


def time_key(index: int, segments: int, seconds: float) -> str:
    if index == 0:
        return "0.0"
    value = seconds * index / segments
    text = f"{value:.{TIME_DECIMALS}f}".rstrip("0")
    return text + "0" if text.endswith(".") else text


def rounded_degrees(value: float) -> float:
    value = round(value, VALUE_DECIMALS)
    if value == 0.0:
        return 0.0  # never -0.0
    return value


def load_json(path: Path) -> dict:
    with path.open("r", encoding="utf-8") as handle:
        return json.load(handle)


def classic_channels(clip_manifest: dict, repository_root: Path) -> list[dict]:
    source = clip_manifest["classic_channels"]
    model_manifest = load_json(repository_root / source["manifest"])
    for spec in model_manifest["models"]:
        if spec["id"] == source["model_id"]:
            return spec["channels"]
    raise SystemExit(f"{source['manifest']} has no model {source['model_id']}")


def bind_degrees(clip_manifest: dict, repository_root: Path) -> dict[str, list[float]] | None:
    """Every bone's classic bind rotation in degrees from the shipped geo the manifest names, or None
    when it names none (a rig whose channel bones bind unrotated on their axes; the harness checks)."""
    geo_path = clip_manifest.get("geo")
    if geo_path is None:
        return None
    geo = load_json(repository_root / geo_path)
    out: dict[str, list[float]] = {}
    for bone in geo["minecraft:geometry"][0]["bones"]:
        rotation = bone.get("rotation")
        if rotation is None:
            out[bone["name"]] = [0.0, 0.0, 0.0]
        else:
            # the converter wrote JSON (+x, -y, -z) degrees of the ModelPart's (x, y, z)
            out[bone["name"]] = [float(rotation[0]), -float(rotation[1]), -float(rotation[2])]
    return out


def group_channels(channels: list[dict], frequency: float) -> list[dict]:
    return [
        channel for channel in channels
        if abs(float(channel["frequency_radians_per_age_tick"]) - frequency) <= FREQUENCY_EPSILON
    ]


def cosine_rotation(axis: str, offset_degrees: float, amplitude_degrees: float, keys: int, lerp_mode: str,
                    seconds: float) -> dict:
    if keys < 3:
        raise SystemExit("a loop needs at least three keys (both ends and one between)")
    segments = keys - 1
    slot = AXIS_INDEX[axis]
    rotation: dict[str, dict] = {}
    for index in range(keys):
        value = rounded_degrees(offset_degrees + amplitude_degrees * math.cos(math.tau * index / segments))
        value = rounded_degrees(AUTHORED_SIGN[axis] * value)
        post = [0, 0, 0]
        post[slot] = value
        rotation[time_key(index, segments, seconds)] = {"post": post, "lerp_mode": lerp_mode}
    return rotation


def build_clips(clip_manifest: dict, channels: list[dict], keys_override: dict[str, int],
                lerp_override: str | None, bind: dict[str, list[float]] | None) -> tuple[dict, list[str]]:
    seconds = float(clip_manifest["animation_length_seconds"])
    lerp_mode = lerp_override or clip_manifest["lerp_mode"]
    animations: dict[str, dict] = {}
    lines: list[str] = []
    seen_bones: set[str] = set()
    for group in clip_manifest["groups"]:
        frequency = float(group["frequency_radians_per_age_tick"])
        wingspeed = float(group.get("wingspeed", 1.0))
        members = group_channels(channels, frequency)
        if not members:
            raise SystemExit(f"group {group['name']}: no classic channel at {frequency} rad/tick")
        keys = int(keys_override.get(group["name"], group["keys_per_bone"]))
        scaled = {bool(channel.get("limb_swing_scaled", False)) for channel in members}
        if len(scaled) != 1:
            raise SystemExit(f"group {group['name']}: its channels disagree on limb_swing_scaled")
        bones: dict[str, dict] = {}
        for channel in members:
            axis = channel.get("axis")
            if channel.get("formula") != "cosine" or axis not in AXIS_INDEX:
                raise SystemExit(f"group {group['name']}: only cosine x / y / z channels are transcribed here "
                                 f"({channel.get('formula')} {axis})")
            if abs(float(channel.get("wingspeed", 1.0)) - wingspeed) > FREQUENCY_EPSILON:
                raise SystemExit(f"group {group['name']}: declares wingspeed {wingspeed} but a channel carries "
                                 f"{channel.get('wingspeed', 1.0)}")
            amplitude = float(channel["sign"]) * classic_amplitude_degrees(float(channel["pi_scale"]))
            base = classic_base_degrees(float(channel.get("base_radians", 0.0)))
            for bone in channel["bones"]:
                if bone in seen_bones:
                    raise SystemExit(f"bone {bone} appears in two frequency groups; groups partition the rig")
                seen_bones.add(bone)
                if bind is None:
                    bone_bind = 0.0
                elif bone not in bind:
                    raise SystemExit(f"{clip_manifest['geo']} has no bone {bone}")
                else:
                    bone_bind = bind[bone][AXIS_INDEX[axis]]
                bones[bone] = {"rotation": cosine_rotation(axis, base - bone_bind, amplitude, keys, lerp_mode, seconds)}
        animations[group["clip"]] = {"loop": True, "animation_length": seconds, "bones": bones}
        chain = f"{frequency} x {wingspeed} rad/tick" if wingspeed != 1.0 else f"{frequency} rad/tick"
        lines.append(f"{group['clip']}: {keys} {lerp_mode} keys per bone over {sorted(bones)} "
                     f"({chain}, {'gait, scaled by limbSwingAmount' if scaled.pop() else 'unscaled'})")
    idle = clip_manifest.get("idle")
    if idle is not None:
        # The in-game gate opens on ``idle`` AND ``walk`` together (owner 2026-09-12, item 12). A classic
        # transcription has no resting motion of its own - at rest the always-on groups carry it and the gait
        # group sits at limbSwingAmount 0 - so its ``idle`` keys no bone; an artist's real idle replaces it.
        if idle.get("keys") != "none":
            raise SystemExit("idle: only the 'none' form is transcribed (the resting motion is the always-on groups)")
        if "idle" in animations:
            raise SystemExit("idle: a group already claims the clip name")
        animations["idle"] = {"loop": True, "animation_length": seconds, "bones": {}}
        lines.append("idle: no bone keyed (the classic resting motion is the always-on groups; present so the "
                     "idle-AND-walk gate opens)")
    return {"format_version": "1.8.0", "animations": animations}, lines


def parse_keys(text: str | None) -> dict[str, int]:
    if not text:
        return {}
    out: dict[str, int] = {}
    for item in text.split(","):
        name, _, value = item.partition("=")
        out[name.strip()] = int(value)
    return out


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    parser.add_argument("--clip-manifest", type=Path, required=True)
    parser.add_argument("--output", type=Path, required=True)
    parser.add_argument("--repository-root", type=Path, default=REPOSITORY_ROOT)
    parser.add_argument("--keys-per-bone", default=None,
                        help="override the manifest's density, e.g. gait=15,teeth=13,tail=8 (experiments only)")
    parser.add_argument("--lerp-mode", default=None, choices=["linear", "catmullrom"],
                        help="override the manifest's lerp_mode (experiments only)")
    args = parser.parse_args()
    clip_manifest = load_json(args.clip_manifest)
    repository_root = args.repository_root.resolve()
    channels = classic_channels(clip_manifest, repository_root)
    bind = bind_degrees(clip_manifest, repository_root)
    document, lines = build_clips(clip_manifest, channels, parse_keys(args.keys_per_bone), args.lerp_mode, bind)
    args.output.parent.mkdir(parents=True, exist_ok=True)
    args.output.write_bytes((json.dumps(document, indent=2, ensure_ascii=False) + "\n").encode("utf-8"))
    print(f"wrote {args.output}")
    for line in lines:
        print("  " + line)
    return 0


if __name__ == "__main__":
    sys.exit(main())
