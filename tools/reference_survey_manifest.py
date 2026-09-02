"""Build a whole-population survey manifest: every port entity model with its renderer's texture and the
1.7.10 reference source of the same name, then dump each through G1ModelProbe (one JVM per model so a
texture-size mismatch reports instead of aborting the run) and run the reference-geometry leg in survey mode."""
import json
import re
import subprocess
import sys
from pathlib import Path

ROOT = Path(r"C:\Homework\Projects\Orespawn")
OUT = Path(__file__).parent.parent / "build" / "reference_survey"
OUT.mkdir(exist_ok=True)
CLIENT = ROOT / "src/main/java/danger/orespawn/entity/client"
REFS = {p.stem: p for p in (ROOT / "reference_1_7_10_source").rglob("Model*.java")}

# port model classes: any client class with a static createBodyLayer()
models = {}
for java in CLIENT.glob("*.java"):
    text = java.read_text(encoding="utf-8", errors="replace")
    if "static LayerDefinition createBodyLayer()" not in text:
        continue
    m = re.search(r"public (?:final )?class (\w+) extends (EntityModel|HierarchicalModel|AgeableListModel|ListModel)", text)
    if not m:
        continue
    models[m.group(1)] = java

# texture per model: the renderer that bakes it
textures = {}
for java in CLIENT.glob("*Renderer.java"):
    text = java.read_text(encoding="utf-8", errors="replace")
    used = re.findall(r"new (\w+)\(context\.bakeLayer\(", text)
    tex = re.findall(r'"textures/entity/([^"]+\.png)"', text)
    for model in used:
        if model in models and tex:
            textures.setdefault(model, tex[0])

entries = []
skipped = []
for model, java in sorted(models.items()):
    base = model[5:] if model.startswith("Model") else (model[:-5] if model.endswith("Model") else model)
    ref = REFS.get("Model" + base)
    tex = textures.get(model)
    # manual pairings: 1.7.10 IslandToo drew ModelIsland (ClientProxyOreSpawn); Elevator's texture is
    # built by concatenation (elevator<color>.png), colour 1 is the parity texture used by Slice 4a.
    if model == "ModelIslandToo":
        ref = REFS.get("ModelIsland")
    if model == "ModelElevator":
        tex = "elevator1.png"
    if ref is None or tex is None:
        skipped.append((model, "no reference" if ref is None else "no renderer texture"))
        continue
    tex_path = ROOT / "src/main/resources/assets/orespawn/textures/entity" / tex
    if not tex_path.is_file():
        # case-insensitive fallback
        matches = [p for p in tex_path.parent.iterdir() if p.name.lower() == tex.lower()]
        if not matches:
            skipped.append((model, f"texture missing {tex}"))
            continue
        tex_path = matches[0]
    entries.append({
        "id": f"survey_{base.lower()}", "tier": 0,
        "class": f"danger.orespawn.entity.client.{model}",
        "texture": tex_path.relative_to(ROOT).as_posix(),
        "reference_source": ref.relative_to(ROOT).as_posix(),
        "animation_kind": "static", "loop_period_age_ticks": 20.0, "sample_fractions": [],
        "limb_swing": 0.0, "limb_swing_amount": 0.0,
        "camera": {"yaw_degrees": 34.0, "pitch_degrees": -28.0}, "channels": [],
    })

print(f"port models with createBodyLayer: {len(models)}; survey entries: {len(entries)}; skipped: {len(skipped)}")
for s in skipped:
    print("  skipped:", s)

# one probe run per model, isolated
classpath = (ROOT / "build/g1/runtime-classpath.txt").read_text(encoding="utf-8").strip().split("\n")
java = r"C:\Users\alvin\AppData\Roaming\PrismLauncher\java\java-runtime-delta\bin\java.exe"
dump_dir = OUT / "vanilla"
dump_dir.mkdir(exist_ok=True)
results = {}
if "--dump" in sys.argv:
    for entry in entries:
        single = {"schema_version": 1, "geckolib_version": "4.8.4", "ticks_per_second": 20.0, "thresholds": {},
                  "models": [entry], "fixtures": []}
        mpath = OUT / f"{entry['id']}.manifest.json"
        mpath.write_text(json.dumps(single), encoding="utf-8")
        # write the manifest inside ROOT/tools so relative paths resolve, then run
        tools_manifest = ROOT / "tools" / "__survey_manifest.json"
        tools_manifest.write_text(json.dumps(single), encoding="utf-8")
        # the probe clears *.compiled.json in its output dir on every run: one dir per model
        model_dir = dump_dir / entry["id"]
        model_dir.mkdir(exist_ok=True)
        proc = subprocess.run([java, "-cp", ";".join(classpath), "danger.orespawn.g1.G1ModelProbe", "vanilla",
                               str(tools_manifest), str(model_dir)], capture_output=True, text=True, cwd=ROOT)
        produced = model_dir / f"{entry['id']}.compiled.json"
        ok = proc.returncode == 0 and produced.is_file()
        if ok:
            produced.replace(dump_dir / produced.name)
        err = ""
        if not ok:
            err = (proc.stderr or proc.stdout).strip().splitlines()
            err = next((line for line in err if "Exception" in line or "mismatch" in line or "!=" in line), err[-1] if err else "?")
        results[entry["id"]] = {"dumped": ok, "error": err[:200]}
        print(f"  {entry['id']:28s} {'dumped' if ok else 'FAILED: ' + err[:120]}")
    tools_manifest.unlink(missing_ok=True)

survey_manifest = OUT / "survey_manifest.json"
survey_manifest.write_text(json.dumps({"schema_version": 1, "models": entries, "fixtures": []}, indent=1), encoding="utf-8")
(OUT / "dump_results.json").write_text(json.dumps(results, indent=1), encoding="utf-8")
print("survey manifest:", survey_manifest)
