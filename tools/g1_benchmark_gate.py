#!/usr/bin/env python3
"""Validate and promote non-acceptance G1 component-smoke evidence."""

from __future__ import annotations

import argparse
import hashlib
import json
import math
import subprocess
import zipfile
from pathlib import Path
import shutil
from typing import Any


def load_json(path: Path) -> dict[str, Any]:
    with path.open("r", encoding="utf-8") as handle:
        return json.load(handle)


def sha256(path: Path) -> str:
    return hashlib.sha256(path.read_bytes()).hexdigest()


def markdown(report: dict[str, Any]) -> str:
    environment = report["environment"]
    settings = report["fixed_settings"]
    budget = report["budget_evaluation"]
    lines = [
        "# Phase G1 repeatable performance benchmark",
        "",
        "Status: **SMOKE_ONLY / COMPONENT_PROXY_ONLY / PENDING_LIVE_PRECUTOVER**",
        "",
        "This is the G1 headless renderer-component proxy. It measures the concrete",
        "classic `EntityModel` and candidate `GeoRenderer` CPU vertex paths. It does",
        "not a Q6 pass. The Tier-1, GPU, MSPT, and MHLib packet scenes remain a",
        "binding pre-cutover gate for the first runtime-integrated conversion slice.",
        "",
        f"- Captured: {report['captured_at_utc']}",
        f"- OS: {environment['os_name']} {environment['os_version']} ({environment['os_arch']})",
        f"- CPU: {environment['processor_identifier']} ({environment['available_processors']} logical processors)",
        f"- JVM: {environment['java_vendor']} {environment['java_version']} / {environment['java_vm']}",
        f"- JVM flags: {environment['jvm_flags']}",
        f"- Repository base revision: `{report['provenance']['repository_base_revision']}` "
        "(working content bound by source/input hashes).",
        f"- Warmup/runs: {report['profile_settings']['warmup_seconds']}s; "
        f"{report['profile_settings']['runs']} x {report['profile_settings']['run_seconds']}s per scene",
        f"- Seed: {report['fixed_seed']}",
        f"- Resolution: {settings['virtual_capture_resolution']}",
        f"- Camera: {settings['camera_path']}",
        f"- Fixed state: {settings['entity_state']}",
        f"- Timing order: {settings['timing_order']}",
        "",
    ]
    for scene in report["scenes"]:
        lines.extend(
            [
                f"## {scene['id']}",
                "",
                f"- Scope: {scene['proxy_scope']}.",
                f"- Classic median/p95: {scene['classic']['median_frame_ms']:.9f} / "
                f"{scene['classic']['p95_frame_ms']:.9f} ms.",
                f"- Candidate median/p95: {scene['candidate']['median_frame_ms']:.9f} / "
                f"{scene['candidate']['p95_frame_ms']:.9f} ms.",
                f"- Candidate 1% low: {scene['candidate']['one_percent_low_fps']:.3f} FPS "
                "(component-only inverse p99).",
                f"- WARNING — component median ratio delta: "
                f"{format(scene['median_regression_percent'], '.17g')}%; absolute p95 delta: "
                f"{format(scene['p95_regression_ms'], '.17g')} ms.",
                f"- Allocation classic/candidate: "
                f"{scene['allocation']['classic_bytes_per_frame']:.3f} / "
                f"{scene['allocation']['candidate_bytes_per_frame']:.3f} bytes per frame.",
                f"- Model-bone instances: {scene['model_bone_count']}; MHLib parts: {scene['mhlib_part_count']}.",
                "",
            ]
        )
    lines.extend(
        [
            "## Q6 status and exact mixed-100 warning numbers",
            "",
            f"- WARNING — mixed-100 component median ratio delta: "
            f"{format(budget['component_proxy_warning_median_regression_percent'], '.17g')}%.",
            f"- WARNING — mixed-100 absolute component p95 delta: "
            f"{format(budget['component_proxy_warning_p95_regression_ms'], '.17g')} ms.",
            "- These component warnings must not be compared with or substituted for Q6's",
            "  whole-client ≤10% median and ≤2 ms p95 acceptance limits.",
            "- Final whole-client median/p95, GPU time, server p95 <50 ms, and no sustained",
            "  MHLib packet growth remain mandatory when a runtime candidate exists.",
            "",
            "Reproduce with `gradlew.bat g1Benchmark`,",
            "then validate/promote with `tools/g1_benchmark_gate.py`.",
            "",
        ]
    )
    return "\n".join(lines)


def median(values: list[float]) -> float:
    ordered = sorted(values)
    middle = len(ordered) // 2
    return ((ordered[middle - 1] + ordered[middle]) * 0.5
            if len(ordered) % 2 == 0 else ordered[middle])


def assert_close(label: str, actual: Any, expected: float) -> None:
    if not math.isclose(float(actual), expected, rel_tol=1.0e-12, abs_tol=1.0e-12):
        raise AssertionError(f"{label} {actual} != independently derived {expected}")


def classpath_entry_sha256(path: Path) -> str:
    if path.is_file():
        return sha256(path)
    digest = hashlib.sha256()
    for file in sorted(item for item in path.rglob("*") if item.is_file()):
        digest.update(file.relative_to(path).as_posix().encode("utf-8"))
        digest.update(b"\0")
        digest.update(file.read_bytes())
    return digest.hexdigest()


def class_bytes(path: Path, resource: str) -> bytes:
    if path.is_dir():
        return (path / Path(resource)).read_bytes()
    with zipfile.ZipFile(path) as archive:
        return archive.read(resource)


def git_output(repository_root: Path, *args: str) -> str:
    result = subprocess.run(
        ["git", "-C", str(repository_root), *args],
        check=True, capture_output=True, text=True,
    )
    return result.stdout.strip()


def validate_provenance(report: dict[str, Any], manifest: dict[str, Any],
                        repository_root: Path, generated_dir: Path,
                        compiled_dir: Path, classpath_file: Path) -> None:
    provenance = report.get("provenance", {})
    base_revision = str(provenance.get("repository_base_revision", ""))
    if len(base_revision) != 40:
        raise AssertionError("benchmark repository base revision is missing")
    current_revision = git_output(repository_root, "rev-parse", "HEAD")
    ancestor = subprocess.run(
        ["git", "-C", str(repository_root), "merge-base", "--is-ancestor",
         base_revision, current_revision],
        check=False,
    )
    if ancestor.returncode != 0:
        raise AssertionError(
            f"benchmark base revision {base_revision} is not an ancestor of {current_revision}"
        )
    expected_sources = {
        "src/g1tool/java/danger/orespawn/g1/G1PerformanceBenchmark.java",
        "src/g1tool/java/danger/orespawn/g1/G1AnimationRuntime.java",
        "tools/g1_benchmark_gate.py",
        "tools/g1_performance_benchmark.json",
        "tools/g1_model_proofs.json",
        "build.gradle",
    }
    sources = provenance.get("source_files_sha256", {})
    if set(sources) != expected_sources:
        raise AssertionError("benchmark source provenance set drift")
    for relative, expected_hash in sources.items():
        if sha256(repository_root / relative) != expected_hash:
            raise AssertionError(f"benchmark source provenance drift: {relative}")

    if provenance.get("declared_runtime_versions") != manifest["runtime_versions"]:
        raise AssertionError("benchmark declared runtime versions drift")
    model_specs = {spec["id"]: spec for spec in manifest["models"]}
    model_inputs = provenance.get("model_inputs", {})
    if set(model_inputs) != set(model_specs):
        raise AssertionError("benchmark model-input provenance set drift")
    if list(generated_dir.glob("*.poses.json")):
        raise AssertionError("obsolete pose artifact exists and could contaminate benchmark input")
    for model_id, spec in model_specs.items():
        evidence = model_inputs[model_id]
        expected_candidate = spec.get("candidate_animation_path", "static_bind_pose")
        if evidence.get("accepted_candidate_path") != expected_candidate:
            raise AssertionError(f"{model_id} benchmark candidate-path provenance drift")
        if evidence.get("reference_animation_used") is not False:
            raise AssertionError(f"{model_id} benchmark must not use reference animation JSON")
        if evidence.get("stale_pose_artifact_present") is not False:
            raise AssertionError(f"{model_id} benchmark captured a stale pose artifact")
        for field, path in (
            ("compiled_dump_sha256", compiled_dir / f"{model_id}.compiled.json"),
            ("generated_geo_sha256", generated_dir / f"{model_id}.geo.json"),
            ("conversion_report_sha256", generated_dir / f"{model_id}.conversion.json"),
        ):
            if evidence.get(field) != sha256(path):
                raise AssertionError(f"{model_id} benchmark input hash drift: {field}")

    classpath_entries = [
        Path(line.strip()) for line in classpath_file.read_text(encoding="utf-8").splitlines()
        if line.strip()
    ]
    runtime = provenance.get("runtime_classpath", {})
    expected_runtime_keys = {
        "benchmark_harness", "candidate_animation_runtime", "geckolib", "neoforge", "minecraft"
    }
    if set(runtime) != expected_runtime_keys:
        raise AssertionError("benchmark runtime classpath provenance set drift")
    version_labels = {
        "geckolib": f"GeckoLib {manifest['runtime_versions']['geckolib']}",
        "neoforge": f"NeoForge {manifest['runtime_versions']['neoforge']}",
        "minecraft": f"Minecraft {manifest['runtime_versions']['minecraft']}",
    }
    for component, evidence in runtime.items():
        if component in version_labels and evidence.get("declared_version") != version_labels[component]:
            raise AssertionError(f"{component} benchmark version label drift")
        candidates = [
            path for path in classpath_entries
            if path.name == evidence.get("classpath_entry_name") and path.exists()
        ]
        matched = None
        for candidate in candidates:
            if classpath_entry_sha256(candidate) == evidence.get("classpath_entry_sha256"):
                matched = candidate
                break
        if matched is None:
            raise AssertionError(f"{component} recorded runtime classpath artifact is unavailable")
        actual_class_hash = hashlib.sha256(
            class_bytes(matched, evidence["class_resource"])
        ).hexdigest()
        if actual_class_hash != evidence.get("class_sha256"):
            raise AssertionError(f"{component} representative runtime class hash drift")


def validate(protocol_path: Path, manifest_path: Path, report: dict[str, Any],
             repository_root: Path, generated_dir: Path, compiled_dir: Path,
             classpath_file: Path) -> None:
    protocol = load_json(protocol_path)
    manifest = load_json(manifest_path)
    if report.get("status") != "SMOKE_ONLY":
        raise AssertionError("benchmark report must be labeled SMOKE_ONLY")
    if report.get("qualification") != "COMPONENT_PROXY_ONLY":
        raise AssertionError("benchmark qualification must be COMPONENT_PROXY_ONLY")
    if report.get("q6_status") != "PENDING_LIVE_PRECUTOVER":
        raise AssertionError("benchmark must label Q6 as pending live pre-cutover")
    if report.get("benchmark_kind") != "SMOKE_ONLY / COMPONENT_PROXY_ONLY / PENDING_LIVE_PRECUTOVER":
        raise AssertionError("benchmark kind label drift")
    if report.get("profile") != "smoke":
        raise AssertionError("checked-in benchmark must use only the smoke profile")
    if report.get("protocol_sha256") != sha256(protocol_path):
        raise AssertionError("benchmark protocol hash drift")
    if report.get("manifest_sha256") != sha256(manifest_path):
        raise AssertionError("benchmark model manifest hash drift")
    if report.get("profile_settings") != protocol["profiles"]["smoke"]:
        raise AssertionError("smoke profile settings drift")
    if report.get("fixed_seed") != protocol["fixed_seed"] or not str(report["fixed_seed"]).startswith("N/A"):
        raise AssertionError("component proxy seed must be N/A")
    if report.get("fixed_settings") != protocol["settings"]:
        raise AssertionError("fixed benchmark settings drift")
    for field in ("virtual_capture_resolution", "camera_path"):
        if not str(report["fixed_settings"][field]).startswith("N/A"):
            raise AssertionError(f"component proxy {field} must be N/A")
    if "per measured batch" not in report["fixed_settings"]["timing_order"]:
        raise AssertionError("benchmark must describe actual AB/BA batch ordering")
    if "two per-run" not in report["fixed_settings"]["aggregation"]:
        raise AssertionError("benchmark must describe the two-run smoke aggregation")
    if report.get("deferred_live_scenes") != protocol["deferred_live_scenes"]:
        raise AssertionError("deferred live-scene list drift")
    if not any(scene["id"] == "offscreen_culling_control"
               for scene in report["deferred_live_scenes"]):
        raise AssertionError("real offscreen/culling control must remain deferred to live Q6")
    if report.get("live_only_metrics") != protocol["live_only_metrics"]:
        raise AssertionError("live-only metric list drift")
    if report.get("provisional_budget") != protocol["provisional_budget"]:
        raise AssertionError("provisional budget drift")
    if report.get("binding_live_acceptance_protocol") != protocol["live_acceptance_protocol"]:
        raise AssertionError("binding live pre-cutover protocol drift")
    live = report["binding_live_acceptance_protocol"]
    if live.get("runs_per_scene") != 5 or live.get("warmup_seconds_per_scene") != 60 \
            or live.get("run_seconds") != 120:
        raise AssertionError("binding live Q6 five-run protocol drift")

    expected_scenes = {scene["id"]: scene for scene in protocol["scenes"]}
    actual_scenes = {scene["id"]: scene for scene in report["scenes"]}
    if actual_scenes.keys() != expected_scenes.keys():
        raise AssertionError("benchmark scene set drift")
    if "mixed_100_offscreen" in actual_scenes:
        raise AssertionError("rotation-only proxy must not be labeled offscreen")
    rotation_scene = expected_scenes.get("mixed_100_rotation_state_only")
    if not rotation_scene or rotation_scene.get("visible") is not False:
        raise AssertionError("rotation-state-only component scene is missing")
    run_seconds = float(protocol["profiles"]["smoke"]["run_seconds"])
    run_count = int(protocol["profiles"]["smoke"]["runs"])
    if run_count != 2:
        raise AssertionError("G1 component smoke must use the documented two-run profile")
    for scene_id, scene in actual_scenes.items():
        scene_spec = expected_scenes[scene_id]
        expected_count = sum(int(model["count"]) for model in scene_spec["models"])
        if scene.get("status") != "COMPONENT_PROXY_MEASURED" or scene.get("entity_count") != expected_count:
            raise AssertionError(f"{scene_id} did not complete as the configured component smoke")
        if scene.get("visible") != scene_spec["visible"]:
            raise AssertionError(f"{scene_id} visibility setting drift")
        expected_scope = scene_spec.get(
            "proxy_scope", "renderer vertex submission only; no window, GPU, client tick, or server"
        )
        if scene.get("proxy_scope") != expected_scope:
            raise AssertionError(f"{scene_id} proxy scope drift")
        if scene.get("budget_scene") != scene_spec["budget_scene"]:
            raise AssertionError(f"{scene_id} budget-scene marker drift")
        expected_bones = 0
        for model in scene_spec["models"]:
            geometry = load_json(generated_dir / f"{model['id']}.geo.json")
            expected_bones += len(geometry["minecraft:geometry"][0]["bones"]) * int(model["count"])
        if int(scene["model_bone_count"]) != expected_bones:
            raise AssertionError(f"{scene_id} model-bone count is not derived from generated geo")
        runs = scene.get("runs", [])
        if len(runs) != run_count or [run["run"] for run in runs] != list(range(1, run_count + 1)):
            raise AssertionError(f"{scene_id} run count/order drift")
        for index, run in enumerate(runs, start=1):
            expected_order = "candidate/classic" if index % 2 == 0 else "classic/candidate"
            if run.get("starting_order") != expected_order or run.get("alternation_unit") != "timing batch":
                raise AssertionError(f"{scene_id} run {index} AB/BA batch-order evidence drift")
        if any(float(run["paired_wall_seconds"]) < run_seconds * 0.99 for run in runs):
            raise AssertionError(f"{scene_id} contains a short measurement run")
        if not scene["allocation"].get("supported"):
            raise AssertionError(f"{scene_id} allocation measurement unavailable")
        if scene["visible"]:
            if scene["classic_vertices_per_frame"] <= 0:
                raise AssertionError(f"{scene_id} emitted no classic vertices")
            if scene["classic_vertices_per_frame"] != scene["candidate_vertices_per_frame"]:
                raise AssertionError(f"{scene_id} renderer vertex counts differ")
        elif scene["classic_vertices_per_frame"] != 0 or scene["candidate_vertices_per_frame"] != 0:
            raise AssertionError(f"{scene_id} rotation-only proxy unexpectedly submitted vertices")
        for mode in ("classic", "candidate"):
            for run in runs:
                stats = run[mode]
                if int(stats.get("sample_count", 0)) <= 0:
                    raise AssertionError(f"{scene_id} has no {mode} timing samples")
                for metric in ("median_frame_ms", "p95_frame_ms", "p99_frame_ms"):
                    if float(stats[metric]) < 0.0:
                        raise AssertionError(f"{scene_id} has a negative {mode} {metric}")
                expected_low = 0.0 if float(stats["p99_frame_ms"]) <= 0.0 \
                    else 1000.0 / float(stats["p99_frame_ms"])
                assert_close(f"{scene_id}/{mode}/run one-percent low",
                             stats["one_percent_low_fps"], expected_low)
            aggregate = scene[mode]
            for metric in ("median_frame_ms", "p95_frame_ms", "p99_frame_ms"):
                expected_metric = median([float(run[mode][metric]) for run in runs])
                assert_close(f"{scene_id}/{mode}/{metric}", aggregate[metric], expected_metric)
            expected_low = 0.0 if float(aggregate["p99_frame_ms"]) <= 0.0 \
                else 1000.0 / float(aggregate["p99_frame_ms"])
            assert_close(f"{scene_id}/{mode}/aggregate one-percent low",
                         aggregate["one_percent_low_fps"], expected_low)
        classic_median = float(scene["classic"]["median_frame_ms"])
        expected_regression = (0.0 if classic_median == 0.0 else
                               100.0 * (float(scene["candidate"]["median_frame_ms"])
                                        / classic_median - 1.0))
        expected_p95_delta = (float(scene["candidate"]["p95_frame_ms"])
                              - float(scene["classic"]["p95_frame_ms"]))
        assert_close(f"{scene_id}/median regression", scene["median_regression_percent"],
                     expected_regression)
        assert_close(f"{scene_id}/p95 delta", scene["p95_regression_ms"], expected_p95_delta)

    budget_spec = protocol["provisional_budget"]
    budget_scene_id = budget_spec["scene"]
    if budget_scene_id not in actual_scenes:
        raise AssertionError("configured provisional budget scene was not measured")
    marked_scenes = [scene_id for scene_id, scene in actual_scenes.items() if scene["budget_scene"]]
    if marked_scenes != [budget_scene_id]:
        raise AssertionError("budget scene marker does not match configured provisional scene")
    budget_scene = actual_scenes[budget_scene_id]
    classic_median = float(budget_scene["classic"]["median_frame_ms"])
    derived_median_warning = (0.0 if classic_median == 0.0 else
                              100.0 * (float(budget_scene["candidate"]["median_frame_ms"])
                                       / classic_median - 1.0))
    derived_p95_warning = (float(budget_scene["candidate"]["p95_frame_ms"])
                           - float(budget_scene["classic"]["p95_frame_ms"]))
    budget = report.get("budget_evaluation", {})
    if budget.get("status") != "Q6_PENDING_LIVE_PRECUTOVER" or budget.get("scene") != budget_scene_id:
        raise AssertionError("benchmark must keep final live Q6 pending on the configured scene")
    assert_close("budget median warning",
                 budget["component_proxy_warning_median_regression_percent"],
                 derived_median_warning)
    assert_close("budget p95 warning",
                 budget["component_proxy_warning_p95_regression_ms"],
                 derived_p95_warning)
    expected_limits = {
        "max_median_frame_regression_percent": budget_spec["max_median_frame_regression_percent"],
        "max_p95_frame_regression_ms": budget_spec["max_p95_frame_regression_ms"],
        "max_server_p95_ms": budget_spec["max_server_p95_ms"],
        "sustained_mhlib_packet_growth_allowed": budget_spec["sustained_mhlib_packet_growth_allowed"],
    }
    if budget.get("provisional_limits_not_applied_to_component_proxy") != expected_limits:
        raise AssertionError("component warning limits do not match configured provisional limits")
    live_budgets = live["budgets"]
    for provisional, live_name in (
        ("max_median_frame_regression_percent", "max_mixed_100_median_frame_regression_percent"),
        ("max_p95_frame_regression_ms", "max_mixed_100_p95_frame_regression_ms"),
        ("max_server_p95_ms", "max_server_p95_ms"),
        ("sustained_mhlib_packet_growth_allowed", "sustained_mhlib_packet_growth_allowed"),
    ):
        if budget_spec[provisional] != live_budgets[live_name]:
            raise AssertionError(f"provisional/live budget mismatch: {provisional}")
    if budget.get("comparison_to_q6_budget_prohibited") is not True:
        raise AssertionError("component warning must prohibit Q6-budget acceptance claims")

    validate_provenance(
        report, manifest, repository_root, generated_dir, compiled_dir, classpath_file
    )


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--protocol", type=Path, required=True)
    parser.add_argument("--manifest", type=Path, required=True)
    parser.add_argument("--report", type=Path, required=True)
    parser.add_argument("--proof-dir", type=Path, required=True)
    parser.add_argument("--repository-root", type=Path, required=True)
    parser.add_argument("--generated-dir", type=Path, required=True)
    parser.add_argument("--compiled-dir", type=Path, required=True)
    parser.add_argument("--runtime-classpath-file", type=Path, required=True)
    parser.add_argument("--write-proof", action="store_true")
    parser.add_argument("--validate-only", action="store_true")
    args = parser.parse_args()
    if args.write_proof and args.validate_only:
        parser.error("--write-proof and --validate-only are mutually exclusive")

    report = load_json(args.report)
    validate(
        args.protocol, args.manifest, report, args.repository_root,
        args.generated_dir, args.compiled_dir, args.runtime_classpath_file,
    )
    if args.validate_only:
        print(
            "G1 BENCHMARK SMOKE_ONLY VALIDATED: COMPONENT_PROXY_ONLY / "
            "PENDING_LIVE_PRECUTOVER; no proof written"
        )
        return 0
    readme_bytes = markdown(report).encode("utf-8")
    benchmark_dir = args.proof_dir / "benchmark"
    targets = {
        benchmark_dir / "protocol.json": args.protocol.read_bytes(),
        benchmark_dir / "report.json": args.report.read_bytes(),
        benchmark_dir / "README.md": readme_bytes,
    }
    if args.write_proof and benchmark_dir.exists():
        shutil.rmtree(benchmark_dir)
    for target, expected in targets.items():
        if args.write_proof:
            target.parent.mkdir(parents=True, exist_ok=True)
            target.write_bytes(expected)
        elif not target.is_file():
            raise AssertionError(f"checked-in benchmark proof missing: {target}")
        elif target.read_bytes() != expected:
            raise AssertionError(f"checked-in benchmark proof drift: {target}")
    action = "updated" if args.write_proof else "verified"
    actual = {path.name for path in benchmark_dir.iterdir() if path.is_file()}
    expected_names = {path.name for path in targets}
    if actual != expected_names:
        raise AssertionError(
            f"benchmark proof artifact set drift; missing={sorted(expected_names - actual)}, "
            f"extras={sorted(actual - expected_names)}"
        )
    print(
        "G1 BENCHMARK EVIDENCE VERIFIED: SMOKE_ONLY / COMPONENT_PROXY_ONLY / "
        f"PENDING_LIVE_PRECUTOVER; checked-in proof {action}"
    )
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
