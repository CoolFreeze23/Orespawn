#!/usr/bin/env python3
"""Emit a 1.21 `createBodyLayer()` from a parsed 1.7.10 ModelBase constructor.

Used for ENT-S-091 (owner ruling 2026-09-02: rebuilt or moved port rigs go back to the original
geometry, proven by the reference-geometry leg). The output is the exact geometry the parser saw:
per part `texOffs(u, v)`, one `addBox(x, y, z, w, h, d)` per box (no mirror: 1.7.10's trailing
`mirror = true` never reached the box, see BUG-041), `PartPose.offset` / `offsetAndRotation` from
the rotation point and initial angles, and `LayerDefinition.create(mesh, textureWidth, textureHeight)`.
Part names default to the reference field names; `--names ref=port,...` renames parts the port's
animation code addresses by other names. Nested parts (`addChild`) become nested PartDefinitions.

Usage: reference_to_layer_definition.py <Model.java> [--names a=b,c=d] [--indent 8]
"""

from __future__ import annotations

import argparse
import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).parent))
from reference_geometry_leg import parse_reference  # noqa: E402


def java_float(value: float) -> str:
    text = repr(float(value))
    if text.endswith(".0"):
        text = text[:-2] + ".0"
    return text + "F"


def emit(reference: dict, names: dict[str, str], indent: int) -> str:
    pad = " " * indent
    parts = {p["name"]: p for p in reference["parts"]}
    lines = [
        f"{pad}MeshDefinition mesh = new MeshDefinition();",
        f"{pad}PartDefinition root = mesh.getRoot();",
    ]
    order = [p["name"] for p in reference["parts"]]

    def var(name: str) -> str:
        return "root" if name is None else "part_" + names.get(name, name).replace("-", "_")

    def emit_part(part: dict, parent: str | None) -> None:
        java_name = names.get(part["name"], part["name"])
        builder = "CubeListBuilder.create()"
        for box in part["boxes"]:
            u, v = box["uv"]
            x, y, z = box["origin"]
            w, h, d = box["size"]
            builder += f"\n{pad}        .texOffs({int(u)}, {int(v)})"
            if box.get("delta", 0.0):
                builder += (f".addBox({java_float(x)}, {java_float(y)}, {java_float(z)}, {java_float(w)}, {java_float(h)}, "
                            f"{java_float(d)}, new CubeDeformation({java_float(box['delta'])}))")
            else:
                builder += f".addBox({java_float(x)}, {java_float(y)}, {java_float(z)}, {java_float(w)}, {java_float(h)}, {java_float(d)})"
        rx, ry, rz = part["rotation_point"]
        ax, ay, az = part["rotation"]
        if any(abs(a) > 1e-9 for a in (ax, ay, az)):
            pose = (f"PartPose.offsetAndRotation({java_float(rx)}, {java_float(ry)}, {java_float(rz)}, "
                    f"{java_float(ax)}, {java_float(ay)}, {java_float(az)})")
        else:
            pose = f"PartPose.offset({java_float(rx)}, {java_float(ry)}, {java_float(rz)})"
        target = var(parent)
        lines.append(f"{pad}PartDefinition {var(part['name'])} = {target}.addOrReplaceChild(\"{java_name}\",")
        lines.append(f"{pad}        {builder},")
        lines.append(f"{pad}        {pose});")
        for child in part["children"]:
            emit_part(parts[child], part["name"])

    for name in order:
        if parts[name]["parent"] is None:
            emit_part(parts[name], None)
    lines.append(f"{pad}return LayerDefinition.create(mesh, {reference['texture_width']}, {reference['texture_height']});")
    return "\n".join(lines)


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("source", type=Path)
    parser.add_argument("--names", default="")
    parser.add_argument("--indent", type=int, default=8)
    args = parser.parse_args()
    reference = parse_reference(args.source)
    if reference["status"] != "PARSED":
        print(reference, file=sys.stderr)
        return 1
    names = dict(pair.split("=", 1) for pair in args.names.split(",") if pair)
    print(emit(reference, names, args.indent))
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
