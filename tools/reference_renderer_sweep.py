"""Mechanical renderer sweep: 1.7.10 registrations `new RenderX(new ModelX(f), par2, par3)` (RenderLiving shadow =
par2 * par3, preRenderCallback scale = par3) versus the port renderer's super(context, model, shadow) literal and
its scale override."""
import json
import re
from pathlib import Path

ROOT = Path(r"C:\Homework\Projects\Orespawn")
OUT = Path(__file__).parent.parent / "build" / "reference_survey"
OUT.mkdir(parents=True, exist_ok=True)
proxy = (ROOT / "reference_1_7_10_source/sources/danger/orespawn/ClientProxyOreSpawn.java").read_text(encoding="utf-8", errors="replace")
CLIENT = ROOT / "src/main/java/danger/orespawn/entity/client"

NUM = r"([-+]?\d*\.?\d+)[fF]?"
regs = re.findall(r"registerEntityRenderingHandler\((\w+)\.class,\s*\(Render\)new (Render\w+)\((?:\(ModelBase\))?new (Model\w+)\(([^)]*)\),\s*" + NUM + r",\s*" + NUM + r"\)\)", proxy)
print(f"reference registrations parsed: {len(regs)}")

def find_port_renderer(entity):
    for name in (f"{entity}Renderer.java",):
        p = CLIENT / name
        if p.is_file():
            return p
    # fallbacks: any renderer whose file mentions the entity class in its generic
    for p in CLIENT.glob("*Renderer.java"):
        t = p.read_text(encoding="utf-8", errors="replace")
        if re.search(r"MobRenderer<\s*" + re.escape(entity) + r"\s*,", t) or re.search(r"EntityRenderer<\s*" + re.escape(entity) + r"\s*,", t):
            return p
    return None

rows = []
for entity, render_cls, model_cls, ctor_args, par2, par3 in regs:
    ref_shadow = float(par2) * float(par3)
    ref_scale = float(par3)
    port = find_port_renderer(entity)
    row = {"entity": entity, "reference": f"new {render_cls}(new {model_cls}({ctor_args}), {par2}f, {par3}f)",
           "reference_shadow": ref_shadow, "reference_scale": ref_scale}
    if port is None:
        row["status"] = "NO_PORT_RENDERER"
        rows.append(row); continue
    t = port.read_text(encoding="utf-8", errors="replace")
    # super(context, <model expr with nested parens>, <shadow>): split the argument list with balanced
    # parentheses and take the LAST top-level argument as the shadow.
    port_shadow = None
    port_shadow_text = None
    k = t.find("super(context,")
    if k >= 0:
        depth = 0
        args = []
        current = []
        j = k + len("super(")
        while j < len(t):
            ch = t[j]
            if ch == "(":
                depth += 1
            elif ch == ")":
                if depth == 0:
                    args.append("".join(current).strip())
                    break
                depth -= 1
            elif ch == "," and depth == 0:
                args.append("".join(current).strip())
                current = []
                j += 1
                continue
            current.append(ch)
            j += 1
        if len(args) >= 3:
            port_shadow_text = args[-1]
            try:
                port_shadow = float(port_shadow_text.rstrip("fF"))
            except ValueError:
                const = re.search(r"float " + re.escape(port_shadow_text) + r"\s*=\s*([^;]+);", t)
                if const:
                    try:
                        port_shadow = float(eval(const.group(1).replace("F", "").replace("f", "")))
                    except Exception:
                        port_shadow = None
    scales = re.findall(r"poseStack\.scale\(\s*([^,]+),", t)
    scale_consts = {}
    for c in re.findall(r"float (\w+)\s*=\s*([-+]?\d*\.?\d+)[fF]?\s*;", t):
        scale_consts[c[0]] = float(c[1])
    port_scales = []
    for sc in scales:
        sc = sc.strip()
        try:
            port_scales.append(float(sc.rstrip("fF")))
        except ValueError:
            port_scales.append(scale_consts.get(sc, sc))
    row["port_file"] = port.name
    row["port_shadow"] = port_shadow
    row["port_scales"] = port_scales
    shadow_ok = port_shadow is not None and abs(port_shadow - ref_shadow) < 1e-6
    numeric_scales = [s for s in port_scales if isinstance(s, float)]
    if ref_scale == 1.0:
        scale_ok = not numeric_scales or all(abs(s - 1.0) < 1e-6 for s in numeric_scales)
    else:
        scale_ok = any(abs(s - ref_scale) < 1e-6 for s in numeric_scales)
    row["shadow_match"] = shadow_ok
    row["scale_match"] = scale_ok
    row["status"] = "MATCH" if shadow_ok and scale_ok else ("SHADOW" if scale_ok else ("SCALE" if shadow_ok else "SHADOW+SCALE"))
    rows.append(row)

from collections import Counter
print("statuses:", Counter(r["status"] for r in rows))
for r in rows:
    if r["status"] != "MATCH":
        print(f"  {r['entity']:18s} {r['status']:14s} ref shadow {r['reference_shadow']:.4g} scale {r['reference_scale']:.4g} | port shadow {r.get('port_shadow')} scales {r.get('port_scales')}  <- {r['reference']}")
(OUT / "renderer_sweep.json").write_text(json.dumps({"rows": rows, "counts": Counter(r["status"] for r in rows)}, indent=1), encoding="utf-8")
