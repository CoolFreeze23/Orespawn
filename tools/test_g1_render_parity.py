#!/usr/bin/env python3
"""Pins for the visual leg's pair-contested rule and the undrawn-parts list (owner 2026-09-15, closing set continued,
items 1 and 2; tools/g1_render_parity.py and tools/layer_definition_to_geo.py) - a `unittest` runner, no pytest.

    python tools/test_g1_render_parity.py

The fixtures are synthetic captures through the production code paths (quad_face_ids, render_capture, pixel_diff,
visual_parity, candidate_bone_names, draw_order_parity; undrawn_parts_declared, derive_bone_draw_order): a four-texel
texture, a camera looking down Z, an alpha-0 frame quad that fixes the camera fit, and two coplanar faces of one flat
"wing" cube whose nearer face SWAPS between the two sides by 2e-6 blocks - past the 1e-6 contest window (the TEST-008
mechanism), inside the 1e-5 attribution window.
"""

from __future__ import annotations

import json
import shutil
import sys
import tempfile
import unittest
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))

import g1_render_parity as parity  # noqa: E402
import layer_definition_to_geo as converter  # noqa: E402
from PIL import Image  # noqa: E402

THRESHOLDS = {
    "pixel_channel_tolerance": 2,
    "pixel_changed_fraction": 0.001,
    "pixel_mean_absolute_error": 0.25,
    "minimum_foreground_fraction": 0.001,
}
RED, BLUE, CLEAR = (0, 0), (1, 0), (1, 1)


def texel_uv(texel: tuple[int, int]) -> list[float]:
    return [texel[0] / 2.0 + 0.25, texel[1] / 2.0 + 0.25]


def square(z: float, half: float, texel: tuple[int, int], normal_z: float) -> list[dict]:
    corners = [(-half, -half), (half, -half), (half, half), (-half, half)]
    return [{"position": [x, y, z], "uv": texel_uv(texel), "normal": [0.0, 0.0, normal_z]} for x, y in corners]


def capture(front_z: float, back_z: float, half: float, back_bone: str = "wing") -> dict:
    """One full capture: an invisible frame quad (the camera fit), then a flat cube's two coplanar faces - the +Z face
    red at ``front_z``, the -Z face blue at ``back_z`` (a smaller depth is nearer). ``back_bone`` names the blue face's
    owner: the same cube by default, another bone to make the two sides' pairs differ."""
    frame = square(5.0, 10.0, CLEAR, 1.0)
    red = square(front_z, half, RED, 1.0)
    blue = square(back_z, half, BLUE, -1.0)
    cubes = [{"bone": "frame", "cube_index": 0, "vertices": frame}]
    if back_bone == "wing":
        cubes.append({"bone": "wing", "cube_index": 0, "vertices": red + blue})
        order = ["frame", "wing"]
    else:
        cubes.append({"bone": "wing", "cube_index": 0, "vertices": red})
        cubes.append({"bone": back_bone, "cube_index": 0, "vertices": blue})
        order = ["frame", "wing", back_bone]
    return {"id": "bind", "capture_kind": "full", "render_vertices": frame + red + blue, "cubes": cubes, "draw_order": order}


class PairContestedRule(unittest.TestCase):
    def setUp(self) -> None:
        self.root = Path(tempfile.mkdtemp(prefix="g1_pair_"))
        texture = Image.new("RGBA", (2, 2))
        texture.putpixel(RED, (255, 0, 0, 255))
        texture.putpixel(BLUE, (0, 0, 255, 255))
        texture.putpixel((0, 1), (0, 255, 0, 255))
        texture.putpixel(CLEAR, (0, 0, 0, 0))
        texture.save(self.root / "texture.png")
        self.spec = {"id": "synthetic", "texture": "texture.png", "visual_sample_ids": ["bind"],
                     "camera": {"yaw_degrees": 0.0, "pitch_degrees": 0.0}}

    def tearDown(self) -> None:
        shutil.rmtree(self.root, ignore_errors=True)

    def visual(self, vanilla: dict, geo: dict) -> dict:
        return parity.visual_parity("synthetic", self.spec, {"samples": [vanilla]}, {"samples": [geo]},
                                    self.root, self.root / "out", THRESHOLDS)

    def test_constants_are_the_rulings(self) -> None:
        self.assertEqual(parity.PAIR_ATTRIBUTION_WINDOW, 1.0e-5)
        self.assertEqual(parity.PAIR_CONTESTED_CAP, 0.01)
        self.assertEqual(parity.CONTEST_DEPTH_EPSILON, 1.0e-6)

    def test_face_identities_agree_across_sides_and_count_every_quad(self) -> None:
        faces: dict = {}
        vanilla = parity.quad_face_ids("synthetic", "bind", capture(0.0, 2.0e-6, 0.6), faces)
        geo = parity.quad_face_ids("synthetic", "bind", capture(2.0e-6, 0.0, 0.6), faces)
        self.assertEqual(vanilla, geo)
        self.assertEqual(len(vanilla), 3)
        self.assertEqual(len(set(vanilla)), 3, "the frame, the +Z face and the -Z face are three identities")
        broken = capture(0.0, 2.0e-6, 0.6)
        broken["draw_order"] = ["frame"]
        with self.assertRaisesRegex(AssertionError, "cannot attribute its faces"):
            parity.quad_face_ids("synthetic", "bind", broken, {})

    def test_swapped_coplanar_pair_is_pair_contested_never_a_mismatch(self) -> None:
        report = self.visual(capture(0.0, 2.0e-6, 0.6), capture(2.0e-6, 0.0, 0.6))
        row = report["samples"][0]
        self.assertEqual(report["status"], "PASS")
        self.assertEqual(row["changed_fraction"], 0.0, "every differing pixel is pair-contested, none is a mismatch")
        self.assertGreater(row["pair_contested_fraction"], 0.001, "the swapped faces are counted")
        self.assertLessEqual(row["pair_contested_fraction"], parity.PAIR_CONTESTED_CAP)
        self.assertEqual(row["contested_fraction"], 0.0, "a 2e-6 gap is outside the contest window: not a z-fight")
        self.assertEqual(report["max_pair_contested_fraction"], row["pair_contested_fraction"])
        self.assertEqual(report["pair_attribution_window_blocks"], parity.PAIR_ATTRIBUTION_WINDOW)
        self.assertEqual(report["pair_contested_cap"], parity.PAIR_CONTESTED_CAP)
        self.assertIn("pair-contested", report["z_fight_policy"])
        diff = Image.open(self.root / "out" / row["diff_capture"]).convert("RGBA")
        self.assertIn(parity.PAIR_CONTESTED_MARKER, set(diff.get_flattened_data()), "the diff paints the pair-contested pixels")

    def test_a_different_pair_stays_a_changed_pixel(self) -> None:
        with self.assertRaisesRegex(AssertionError, r"VISUAL MISMATCH synthetic/bind: changed fraction"):
            self.visual(capture(0.0, 2.0e-6, 0.6), capture(2.0e-6, 0.0, 0.6, back_bone="other"))

    def test_a_pair_beyond_the_attribution_window_stays_a_changed_pixel(self) -> None:
        with self.assertRaisesRegex(AssertionError, r"VISUAL MISMATCH synthetic/bind: changed fraction"):
            self.visual(capture(0.0, 2.0e-5 + 1.0e-6, 0.6), capture(2.0e-5 + 1.0e-6, 0.0, 0.6))

    def test_the_cap_fails_a_rig_that_ties_everywhere(self) -> None:
        with self.assertRaisesRegex(AssertionError, r"VISUAL MISMATCH synthetic/bind: pair-contested fraction .* > 0.01"):
            self.visual(capture(0.0, 2.0e-6, 6.0), capture(2.0e-6, 0.0, 6.0))

    def test_identical_sides_report_nothing(self) -> None:
        report = self.visual(capture(0.0, 2.0e-6, 0.6), capture(0.0, 2.0e-6, 0.6))
        row = report["samples"][0]
        self.assertEqual((row["changed_fraction"], row["pair_contested_fraction"], row["mean_absolute_error"]), (0.0, 0.0, 0.0))


class UndrawnParts(unittest.TestCase):
    COMPILED = {"bone_names": ["body", "claw", "toe"], "definition": {"name": None, "cubes": [], "children": [
        {"name": "body", "cubes": [{}], "children": [{"name": "claw", "cubes": [{}], "children": []}]},
        {"name": "toe", "cubes": [{}], "children": []}]}}

    def test_candidate_bone_names_drop_the_undrawn_parts(self) -> None:
        spec = {"undrawn_parts": ["toe"]}
        self.assertEqual(parity.candidate_bone_names("m", spec, self.COMPILED, {}), ["body", "claw"])
        self.assertEqual(parity.candidate_bone_names("m", {}, self.COMPILED, {}), ["body", "claw", "toe"])
        with self.assertRaisesRegex(AssertionError, "lacks"):
            parity.candidate_bone_names("m", {"undrawn_parts": ["nope"]}, self.COMPILED, {})
        with self.assertRaisesRegex(AssertionError, "repeats"):
            parity.candidate_bone_names("m", {"undrawn_parts": ["toe", "toe"]}, self.COMPILED, {})

    @staticmethod
    def draw_order_inputs(classic: list[str]) -> tuple[dict, dict, dict, dict]:
        compiled = {"samples": [{"id": "bind", "capture_kind": "full", "draw_order": classic}]}
        geo_render = {"bone_draw_order": ["body", "claw"], "baked_bone_order": ["body", "claw"],
                      "samples": [{"id": "bind", "capture_kind": "full", "draw_order": ["body", "claw"]}]}
        geometry = {"minecraft:geometry": [{"description": {parity.DRAW_ORDER_KEY: ["body", "claw"]}}]}
        conversion = {"bone_draw_order": ["body", "claw"], "exact_bone_names": ["body", "claw"],
                      "draw_order_evidence": {"unobserved_units": []}}
        return compiled, geo_render, geometry, conversion

    def test_draw_order_leg_refuses_a_drawn_undrawn_part_loudly(self) -> None:
        with self.assertRaisesRegex(AssertionError, r"UNDRAWN PART DRAWN m/bind: .*toe"):
            parity.draw_order_parity("m", *self.draw_order_inputs(["body", "claw", "toe"]), undrawn=frozenset({"toe"}))
        with self.assertRaisesRegex(AssertionError, r"UNDRAWN PART DRAWN m/bind: .*toe__i1"):
            parity.draw_order_parity("m", *self.draw_order_inputs(["body", "toe__i0", "toe__i1", "claw"]),
                                     undrawn=frozenset({"toe"}))

    def test_draw_order_leg_passes_without_the_undrawn_part_and_records_it(self) -> None:
        report = parity.draw_order_parity("m", *self.draw_order_inputs(["body", "claw"]), undrawn=frozenset({"toe"}))
        self.assertEqual(report["status"], "PASS")
        self.assertEqual(report["undrawn_parts"], ["toe"])
        self.assertNotIn("undrawn_parts", parity.draw_order_parity("m", *self.draw_order_inputs(["body", "claw"])))

    def test_converter_validates_the_list(self) -> None:
        self.assertEqual(converter.undrawn_parts_declared({"id": "m", "undrawn_parts": ["toe"]}, self.COMPILED), frozenset({"toe"}))
        self.assertEqual(converter.undrawn_parts_declared({"id": "m"}, self.COMPILED), frozenset())
        with self.assertRaisesRegex(ValueError, "children"):
            converter.undrawn_parts_declared({"id": "m", "undrawn_parts": ["body"]}, self.COMPILED)
        with self.assertRaisesRegex(ValueError, "lacks"):
            converter.undrawn_parts_declared({"id": "m", "undrawn_parts": ["nope"]}, self.COMPILED)
        with self.assertRaisesRegex(ValueError, "render_instances"):
            converter.undrawn_parts_declared({"id": "m", "undrawn_parts": ["toe"], "render_instances": {"toe": {}}}, self.COMPILED)

    def test_converter_refuses_a_drawn_undrawn_part_loudly(self) -> None:
        bones = [{"name": "body", "cubes": [{}]}, {"name": "claw", "cubes": [{}], "parent": "body"}]

        def compiled(classic: list[str]) -> dict:
            return {"model_id": "m", "draw_order_source": "test",
                    "samples": [{"id": "bind", "capture_kind": "full", "draw_order": classic}]}

        order, evidence = converter.derive_bone_draw_order(compiled(["body", "claw"]), bones, frozenset({"toe"}))
        self.assertEqual(order, ["body", "claw"])
        self.assertEqual(evidence["unobserved_units"], [])
        with self.assertRaisesRegex(ValueError, r"UNDRAWN PART DRAWN m: capture bind draws \['toe'\]"):
            converter.derive_bone_draw_order(compiled(["body", "toe", "claw"]), bones, frozenset({"toe"}))
        with self.assertRaisesRegex(ValueError, "not cube-bearing geo bones"):
            converter.derive_bone_draw_order(compiled(["body", "toe", "claw"]), bones)


if __name__ == "__main__":
    unittest.main(verbosity=1)
