#!/usr/bin/env python3
"""Reference-geometry leg: the 1.7.10 ModelBase source versus the port's compiled LayerDefinition.

The independent leg the owner asked for (BUG-040): the port's classic model is
compared against the ORIGINAL model's geometry, parsed from the decompiled
1.7.10 source (read-only), never from the port. The port side is the compiled
dump `G1ModelProbe vanilla` already writes (`<id>.compiled.json`: definition
tree with per-part local pivot, initial rotation, cubes with origin, size,
uv, mirror, deformation; texture size).

1.7.10 semantics reproduced here (CFR-decompiled, obfuscated names):
  field_78090_t / field_78089_u   ModelBase.textureWidth / textureHeight
  new ModelRenderer(this, u, v)   part with texture offset (u, v)
  func_78789_a(x, y, z, w, h, d)  addBox; func_78790_a(..., delta) addBox with inflate
  func_78793_a(x, y, z)           setRotationPoint
  func_78787_b(w, h)              setTextureSize (per part)
  field_78809_i = true            mirror  -- consulted by ModelBox's CONSTRUCTOR, so it
                                  affects only boxes added AFTER it is set (the common
                                  Techne export sets it after addBox: inert)
  field_78795_f/_78796_g/_78808_h rotateAngleX/Y/Z (usually via a setRotation helper)
  func_78792_a(child)             addChild
  field_78806_j = false           showModel (hidden part)
  field_78800_c/_78797_d/_78798_e rotationPointX/Y/Z, also via `+=` adjustments

Anything else (loops, computed arguments, conditionals around boxes) makes a
file UNPARSEABLE and is reported as such rather than guessed.
"""

from __future__ import annotations

import argparse
import json
import re
from pathlib import Path
from typing import Any

NUMBER = r"[-+]?(?:\d+\.\d*|\.\d+|\d+)(?:[eE][-+]?\d+)?[fFdD]?"
FIELD = r"this\.([A-Za-z_][A-Za-z0-9_]*)"
IDENT = r"([A-Za-z_][A-Za-z0-9_]*)"

RE_TEX_W = re.compile(r"this\.field_78090_t\s*=\s*(\d+)\s*;")
RE_TEX_H = re.compile(r"this\.field_78089_u\s*=\s*(\d+)\s*;")
RE_NEW_PART = re.compile(FIELD + r"\s*=\s*new ModelRenderer\(\(ModelBase\)this,\s*(-?\d+),\s*(-?\d+)\)\s*;")
RE_NEW_PART_NAMED = re.compile(FIELD + r"\s*=\s*new ModelRenderer\(\(ModelBase\)this,\s*\"[^\"]*\"\)\s*;")
RE_ADD_BOX = re.compile(FIELD + r"\.func_78789_a\(\s*(" + NUMBER + r"),\s*(" + NUMBER + r"),\s*(" + NUMBER
                        + r"),\s*(\d+),\s*(\d+),\s*(\d+)\)\s*;")
RE_ADD_BOX_DELTA = re.compile(FIELD + r"\.func_78790_a\(\s*(" + NUMBER + r"),\s*(" + NUMBER + r"),\s*(" + NUMBER
                              + r"),\s*(\d+),\s*(\d+),\s*(\d+),\s*(" + NUMBER + r")\)\s*;")
RE_SET_TEX_OFFS = re.compile(FIELD + r"\.func_78784_a\(\s*(-?\d+),\s*(-?\d+)\)\s*;")
RE_ROT_POINT = re.compile(FIELD + r"\.func_78793_a\(\s*(" + NUMBER + r"),\s*(" + NUMBER + r"),\s*(" + NUMBER + r")\)\s*;")
RE_TEX_SIZE = re.compile(FIELD + r"\.func_78787_b\(\s*(\d+),\s*(\d+)\)\s*;")
RE_MIRROR = re.compile(FIELD + r"\.field_78809_i\s*=\s*(true|false)\s*;")
RE_SHOW = re.compile(FIELD + r"\.field_78806_j\s*=\s*(true|false)\s*;")
RE_SET_ROTATION = re.compile(r"this\.setRotation\(\s*this\.([A-Za-z_][A-Za-z0-9_]*),\s*(" + NUMBER + r"),\s*("
                             + NUMBER + r"),\s*(" + NUMBER + r")\)\s*;")
RE_ANGLE = re.compile(FIELD + r"\.(field_78795_f|field_78796_g|field_78808_h)\s*=\s*(" + NUMBER + r")\s*;")
RE_ADD_CHILD = re.compile(FIELD + r"\.func_78792_a\(\s*this\.([A-Za-z_][A-Za-z0-9_]*)\)\s*;")
RE_ROT_POINT_ADD = re.compile(FIELD + r"\.(field_78800_c|field_78797_d|field_78798_e)\s*\+=\s*(" + NUMBER + r")\s*;")
RE_ROT_POINT_SET = re.compile(FIELD + r"\.(field_78800_c|field_78797_d|field_78798_e)\s*=\s*(" + NUMBER + r")\s*;")
RE_SCALAR_FROM_PART = re.compile(r"this\.[A-Za-z_][A-Za-z0-9_]*\s*=\s*this\.[A-Za-z_][A-Za-z0-9_]*\.field_787(?:97_d|98_e|00_c)\s*;")
RE_CTOR = re.compile(r"public\s+" + IDENT + r"\s*\(([^)]*)\)\s*\{")


def number(token: str) -> float:
    return float(token.rstrip("fFdD"))


def constructor_body(source: str, class_name: str) -> str:
    match = re.search(r"public\s+" + re.escape(class_name) + r"\s*\([^)]*\)\s*\{", source)
    if not match:
        raise ValueError("no public constructor found")
    depth = 0
    start = match.end() - 1
    for index in range(start, len(source)):
        if source[index] == "{":
            depth += 1
        elif source[index] == "}":
            depth -= 1
            if depth == 0:
                return source[start + 1:index]
    raise ValueError("unbalanced constructor")


def parse_reference(source_path: Path) -> dict[str, Any]:
    source = source_path.read_text(encoding="utf-8", errors="replace")
    class_match = re.search(r"public class\s+" + IDENT + r"\s+extends\s+ModelBase", source)
    if not class_match:
        return {"status": "UNPARSEABLE", "reason": "not a ModelBase subclass"}
    class_name = class_match.group(1)
    try:
        body = constructor_body(source, class_name)
    except ValueError as exc:
        return {"status": "UNPARSEABLE", "reason": str(exc)}

    texture_width = None
    texture_height = None
    parts: dict[str, dict[str, Any]] = {}
    order: list[str] = []
    unhandled: list[str] = []

    statements = [line.strip() for line in body.replace("\r", "").split("\n") if line.strip()]
    for statement in statements:
        if statement.startswith("//") or statement.startswith("/*") or statement.startswith("*"):
            continue
        if statement.startswith("super(") or statement in ("}", "{"):
            continue
        if (m := RE_TEX_W.fullmatch(statement)):
            texture_width = int(m.group(1))
            continue
        if (m := RE_TEX_H.fullmatch(statement)):
            texture_height = int(m.group(1))
            continue
        if (m := RE_NEW_PART.fullmatch(statement)):
            name = m.group(1)
            parts[name] = {"name": name, "tex_u": int(m.group(2)), "tex_v": int(m.group(3)), "boxes": [],
                           "rotation_point": [0.0, 0.0, 0.0], "rotation": [0.0, 0.0, 0.0], "mirror": False,
                           "show": True, "texture_size": None, "children": [], "parent": None}
            order.append(name)
            continue
        if (m := RE_NEW_PART_NAMED.fullmatch(statement)):
            name = m.group(1)
            parts[name] = {"name": name, "tex_u": 0, "tex_v": 0, "boxes": [], "rotation_point": [0.0, 0.0, 0.0],
                           "rotation": [0.0, 0.0, 0.0], "mirror": False, "show": True, "texture_size": None,
                           "children": [], "parent": None}
            order.append(name)
            continue
        if (m := RE_SET_TEX_OFFS.fullmatch(statement)):
            part = parts.get(m.group(1))
            if part is None:
                unhandled.append(statement)
                continue
            part["tex_u"], part["tex_v"] = int(m.group(2)), int(m.group(3))
            continue
        if (m := RE_ADD_BOX.fullmatch(statement)) or (m := RE_ADD_BOX_DELTA.fullmatch(statement)):
            part = parts.get(m.group(1))
            if part is None:
                unhandled.append(statement)
                continue
            delta = number(m.group(8)) if m.re is RE_ADD_BOX_DELTA else 0.0
            part["boxes"].append({
                "origin": [number(m.group(2)), number(m.group(3)), number(m.group(4))],
                "size": [int(m.group(5)), int(m.group(6)), int(m.group(7))],
                "uv": [part["tex_u"], part["tex_v"]],
                "mirror": part["mirror"],      # ModelBox reads the flag at construction
                "delta": delta,
            })
            continue
        if (m := RE_ROT_POINT.fullmatch(statement)):
            part = parts.get(m.group(1))
            if part is None:
                unhandled.append(statement)
                continue
            part["rotation_point"] = [number(m.group(2)), number(m.group(3)), number(m.group(4))]
            continue
        if (m := RE_TEX_SIZE.fullmatch(statement)):
            part = parts.get(m.group(1))
            if part is None:
                unhandled.append(statement)
                continue
            part["texture_size"] = [int(m.group(2)), int(m.group(3))]
            continue
        if (m := RE_MIRROR.fullmatch(statement)):
            part = parts.get(m.group(1))
            if part is None:
                unhandled.append(statement)
                continue
            part["mirror"] = m.group(2) == "true"
            continue
        if (m := RE_SHOW.fullmatch(statement)):
            part = parts.get(m.group(1))
            if part is None:
                unhandled.append(statement)
                continue
            part["show"] = m.group(2) == "true"
            continue
        if (m := RE_SET_ROTATION.fullmatch(statement)):
            part = parts.get(m.group(1))
            if part is None:
                unhandled.append(statement)
                continue
            part["rotation"] = [number(m.group(2)), number(m.group(3)), number(m.group(4))]
            continue
        if (m := RE_ANGLE.fullmatch(statement)):
            part = parts.get(m.group(1))
            if part is None:
                unhandled.append(statement)
                continue
            axis = {"field_78795_f": 0, "field_78796_g": 1, "field_78808_h": 2}[m.group(2)]
            part["rotation"][axis] = number(m.group(3))
            continue
        if (m := RE_ROT_POINT_ADD.fullmatch(statement)) or (m := RE_ROT_POINT_SET.fullmatch(statement)):
            part = parts.get(m.group(1))
            if part is None:
                unhandled.append(statement)
                continue
            axis = {"field_78800_c": 0, "field_78797_d": 1, "field_78798_e": 2}[m.group(2)]
            if m.re is RE_ROT_POINT_ADD:
                part["rotation_point"][axis] += number(m.group(3))
            else:
                part["rotation_point"][axis] = number(m.group(3))
            continue
        if RE_SCALAR_FROM_PART.fullmatch(statement):
            continue  # a scalar copied from a part's rotation point (ModelGiantRobot.hipy); no geometry
        if (m := RE_ADD_CHILD.fullmatch(statement)):
            parent, child = parts.get(m.group(1)), parts.get(m.group(2))
            if parent is None or child is None:
                unhandled.append(statement)
                continue
            parent["children"].append(child["name"])
            child["parent"] = parent["name"]
            continue
        if re.fullmatch(r"this\.[A-Za-z_][A-Za-z0-9_]*\s*=\s*(?:" + NUMBER + r"|[A-Za-z_][A-Za-z0-9_]*)\s*;", statement):
            continue  # scalar field (wingspeed = f1 etc.), never geometry
        unhandled.append(statement)

    if unhandled:
        return {"status": "UNPARSEABLE", "reason": "constructor statements outside the ModelRenderer idiom",
                "unhandled": unhandled[:12], "unhandled_count": len(unhandled),
                "parts_parsed": len(parts)}
    if texture_width is None or texture_height is None:
        return {"status": "UNPARSEABLE", "reason": "textureWidth/Height not set in the constructor"}
    return {
        "status": "PARSED",
        "class": class_name,
        "texture_width": texture_width,
        "texture_height": texture_height,
        "parts": [parts[name] for name in order],
    }


# ---------------------------------------------------------------- comparison


def flatten_port(compiled: dict[str, Any]) -> list[dict[str, Any]]:
    result: list[dict[str, Any]] = []

    def visit(part: dict[str, Any], parent: str | None) -> None:
        for child in part["children"]:
            result.append({
                "name": child["name"],
                "parent": parent,
                "pivot": [float(v) for v in child["local_pivot"]],
                "rotation": [float(v) for v in child["initial_rotation_radians"]],
                "boxes": [{
                    "origin": [float(v) for v in cube["origin"]],
                    "size": [float(v) for v in cube["size"]],
                    "uv": [float(v) for v in cube["uv"]],
                    "mirror": bool(cube["mirror"]),
                    "delta": max(float(v) for v in cube["deformation"]),
                } for cube in child["cubes"]],
            })
            visit(child, child["name"])

    visit(compiled["definition"], None)
    return result


def box_key(box: dict[str, Any]) -> tuple:
    return (tuple(round(float(v), 4) for v in box["origin"]), tuple(round(float(v), 4) for v in box["size"]),
            tuple(round(float(v), 4) for v in box["uv"]), bool(box["mirror"]), round(float(box["delta"]), 4))


def part_signature(boxes: list[dict[str, Any]]) -> tuple:
    return tuple(sorted(box_key(box) for box in boxes))


def compare(reference: dict[str, Any], compiled: dict[str, Any], epsilon: float = 1.0e-4) -> dict[str, Any]:
    port_parts = flatten_port(compiled)
    ref_parts = [part for part in reference["parts"] if part["boxes"]]  # boxless parts are pure pivots
    differences: list[str] = []

    tex = (compiled["texture_width"], compiled["texture_height"])
    if tex != (reference["texture_width"], reference["texture_height"]):
        differences.append(f"texture size port {tex} != reference "
                           f"{(reference['texture_width'], reference['texture_height'])}")
    for part in ref_parts:
        if part["texture_size"] and tuple(part["texture_size"]) != (reference["texture_width"], reference["texture_height"]):
            differences.append(f"{part['name']}: per-part texture size {part['texture_size']} differs from the sheet")

    # Match reference parts to port parts by exact box signature (names differ between the sources).
    unmatched_port = {index: part for index, part in enumerate(port_parts) if part["boxes"]}
    matched = 0
    for part in ref_parts:
        signature = part_signature(part["boxes"])
        hit = None
        for index, candidate in unmatched_port.items():
            if part_signature(candidate["boxes"]) == signature:
                hit = index
                break
        if hit is None:
            box = part["boxes"][0]
            differences.append(f"reference part {part['name']} has no port part with boxes "
                               f"{[box_key(b) for b in part['boxes']]}")
            continue
        candidate = unmatched_port.pop(hit)
        matched += 1
        pivot = part["rotation_point"]
        if part["parent"] is None and candidate["parent"] is None:
            if any(abs(pivot[i] - candidate["pivot"][i]) > epsilon for i in range(3)):
                differences.append(f"{part['name']} -> {candidate['name']}: rotation point {pivot} != port pivot {candidate['pivot']}")
        elif (part["parent"] is None) != (candidate["parent"] is None):
            differences.append(f"{part['name']} -> {candidate['name']}: nesting differs (reference parent {part['parent']}, port parent {candidate['parent']})")
        else:
            if any(abs(pivot[i] - candidate["pivot"][i]) > epsilon for i in range(3)):
                differences.append(f"{part['name']} -> {candidate['name']}: child rotation point {pivot} != port local pivot {candidate['pivot']}")
        if any(abs(part["rotation"][i] - candidate["rotation"][i]) > epsilon for i in range(3)):
            differences.append(f"{part['name']} -> {candidate['name']}: initial rotation {part['rotation']} != port {candidate['rotation']}")
        if not part["show"]:
            differences.append(f"{part['name']}: hidden (showModel=false) in the reference; the port dump has no visibility field")
    for candidate in unmatched_port.values():
        differences.append(f"port part {candidate['name']} has no reference part with boxes "
                           f"{[box_key(b) for b in candidate['boxes']]}")
    return {
        "status": "PASS" if not differences else "DIVERGES",
        "reference_parts_with_boxes": len(ref_parts),
        "port_parts_with_boxes": sum(1 for part in port_parts if part["boxes"]),
        "matched_parts": matched,
        "differences": differences,
    }


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--manifest", type=Path, required=True)
    parser.add_argument("--dump-dir", type=Path, required=True)
    parser.add_argument("--output-dir", type=Path, required=True)
    parser.add_argument("--survey", action="store_true",
                        help="report every model, never fail; for models without a dump only parse the reference")
    args = parser.parse_args()
    manifest = json.loads(args.manifest.read_text(encoding="utf-8"))
    repository_root = args.manifest.resolve().parent.parent
    args.output_dir.mkdir(parents=True, exist_ok=True)
    failures = 0
    summary = []
    for spec in manifest["models"]:
        if "reference_source" not in spec:
            continue
        model_id = spec["id"]
        reference_path = repository_root / spec["reference_source"]
        reference = parse_reference(reference_path)
        report: dict[str, Any] = {
            "schema_version": 1,
            "model_id": model_id,
            "reference_source": spec["reference_source"],
            "reference": reference,
            "ground_truth": "1.7.10 decompiled ModelBase constructor, parsed; never the port",
        }
        dump = args.dump_dir / f"{model_id}.compiled.json"
        if reference["status"] != "PARSED":
            report["status"] = "UNPARSEABLE"
        elif not dump.is_file():
            report["status"] = "NO_PORT_DUMP"
        else:
            compiled = json.loads(dump.read_text(encoding="utf-8"))
            report["comparison"] = compare(reference, compiled)
            report["status"] = report["comparison"]["status"]
        (args.output_dir / f"{model_id}.reference-geometry.json").write_text(
            json.dumps(report, indent=2) + "\n", encoding="utf-8", newline="\n")
        summary.append((model_id, report["status"]))
        if report["status"] == "PASS":
            comparison = report["comparison"]
            print(f"REFERENCE GEOMETRY PASS: {model_id} {comparison['matched_parts']} parts matched the 1.7.10 source")
        else:
            detail = report.get("comparison", {}).get("differences") or [reference.get("reason", "")]
            print(f"REFERENCE GEOMETRY {report['status']}: {model_id}: {detail[:3]}")
            if not args.survey:
                failures += 1
    if args.survey:
        counts: dict[str, int] = {}
        for _model_id, status in summary:
            counts[status] = counts.get(status, 0) + 1
        print(f"REFERENCE GEOMETRY SURVEY: {counts}")
        return 0
    return 1 if failures else 0


if __name__ == "__main__":
    raise SystemExit(main())
