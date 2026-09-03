# Renderer pin scanner: reassigned-local tightening, lab record (2026-09-03)

Owner ruling: "Scanner blind spot: tighten it; any pin that changes as a result is presented before its gate."

Scanner: `tools/reference_renderer_pins.py` (the `referenceRenderers` leg under `check`). Three tool states are compared:
ORIG = the committed scanner before the tightening (a float local read only at its declaration); LAB = the first
tightened copy the refuters examined; FIX2 = the installed scanner after the refuters' minors (`%=`, `++`, `--`
unbind as unread forms; nested braceless ifs and dangling elses unbind; a braceless `while`/`do` wrapping a write
unbinds). Each case is a synthetic `CoinRenderer.java` (or `QueenRenderer.java` / candidate `CoinGeoReplacement.java`)
dropped over the real client sources; the probed entry is Coin (Queen for the Q cases). A pin PASSes only when the
value on the default path is provably the manifest value; every other outcome DIVERGES (a wrong value or an
unbound local with a reason), which fails the leg and sends a human to read the file.

## Repo result

PASS 120 / PENDING 0 / MOD 0 / NOT_APPLICABLE 13 / DIVERGES 0 / MANIFEST_DRIFT 0 before and after; no entry's
status or detail changed, so nothing needed presenting before the gate.

## Lab cases

| case | ORIG | LAB | FIX2 | FIX2 detail (probe entry) |
|---|---|---|---|---|
| A1_lambda_write | PASS | DIVERGES | DIVERGES | port scale unparseable: local s is written inside a block that is neither if nor else (`s = 2.0F`) / port CoinRenderer.java shadow 0.09375 ( |
| A2_anon_class_write | PASS | DIVERGES | DIVERGES | port scale unparseable: local s is written inside a block that is neither if nor else (`s = 2.0F`) / port CoinRenderer.java shadow 0.09375 ( |
| A3_anon_class_shadow_redecl_SCALE | PASS | PASS | PASS | port CoinRenderer.java shadow 0.09375 (SHADOW); scale 0.125 (unconditional s); candidate CoinGeoReplacement.java shadow 0.09375 (CoinRendere |
| A4_anon_class_shadow_redecl_literal | DIVERGES | DIVERGES | DIVERGES | scale expected 0.125, found 2 (unconditional s); SCALE constant exists but scale() scales by s / port CoinRenderer.java shadow 0.09375 (SHAD |
| A6_task_shadow_s2_example | DIVERGES | DIVERGES | DIVERGES | scale expected 0.125, found 2 (unconditional s2); SCALE constant exists but scale() scales by s2 / port CoinRenderer.java shadow 0.09375 (SH |
| C1_candidate_unconditional_reassign | - | - | DIVERGES | candidate scale expected 0.125, found 2 (unconditional s (reassigned: s = 2.0F)) / port CoinRenderer.java shadow 0.09375 (SHADOW); scale 0.1 |
| C2_candidate_loop_write | - | - | DIVERGES | candidate scale unparseable: local s is written inside a block that is neither if nor else (`s = 2.0F`) / port CoinRenderer.java shadow 0.09 |
| C3_candidate_if_reassign_default_SCALE | - | - | DIVERGES | candidate applyScale does not scale by CoinRenderer.SCALE / port CoinRenderer.java shadow 0.09375 (SHADOW); scale 0.125 (unconditional SCALE |
| L1_while_braced | PASS | DIVERGES | DIVERGES | port scale unparseable: local s is written inside a block that is neither if nor else (`s = 2.0F`) / port CoinRenderer.java shadow 0.09375 ( |
| L2_while_braceless | PASS | DIVERGES | DIVERGES | port scale unparseable: local s is written inside a block that is neither if nor else (`s = 2.0F`) / port CoinRenderer.java shadow 0.09375 ( |
| L3_do_while_braced | PASS | DIVERGES | DIVERGES | port scale unparseable: local s is written inside a block that is neither if nor else (`s = 2.0F`) / port CoinRenderer.java shadow 0.09375 ( |
| L4_do_while_braceless | PASS | DIVERGES | DIVERGES | port scale unparseable: local s is written inside a block that is neither if nor else (`s = 2.0F`) / port CoinRenderer.java shadow 0.09375 ( |
| L5_for_braced | PASS | DIVERGES | DIVERGES | port scale unparseable: local s is written inside a block that is neither if nor else (`s = 2.0F`) / port CoinRenderer.java shadow 0.09375 ( |
| L6_for_braceless_wrapping_braced_if | PASS | PASS | PASS | port CoinRenderer.java shadow 0.09375 (SHADOW); scale 0.125 (unconditional s (if-reassignment default branch SCALE; writes: s = 2.0F)); cand |
| L7_while_braceless_wrapping_braced_if | PASS | PASS | DIVERGES | port scale unparseable: local s is written inside a block that is neither if nor else (`s = 2.0F`) / port CoinRenderer.java shadow 0.09375 ( |
| L8_foreach_braceless | PASS | DIVERGES | DIVERGES | port scale unparseable: local s is written inside a block that is neither if nor else (`s = 2.0F`) / port CoinRenderer.java shadow 0.09375 ( |
| Q1_if_reassign_default_SCALE | PASS | PASS | PASS | port QueenRenderer.java shadow 3.8 (SHADOW); scale 2 (unconditional effectiveScale (if-reassignment default branch SCALE; writes: effectiveS |
| Q2_unconditional_reassign_literal | PASS | DIVERGES | DIVERGES | scale expected 2, found 3 (unconditional effectiveScale (reassigned: effectiveScale = 3.0F), scaleModelForRender); SCALE constant exists but |
| Q3_loop_write | PASS | DIVERGES | DIVERGES | port scale unparseable: scaleModelForRender: local effectiveScale is written inside a block that is neither if nor else (`effectiveScale = 3 |
| Q4_if_else_default_literal | PASS | DIVERGES | DIVERGES | scale expected 2, found 3 (unconditional effectiveScale (if/else-reassignment default branch 3.0F; writes: effectiveScale = SCALE / 4.0F; ef |
| Q5_unevaluable_default_write | PASS | DIVERGES | DIVERGES | port scale unparseable: scaleModelForRender: local effectiveScale is written on the default path with an expression that cannot be evaluated |
| R1_write_after_return_in_if | PASS | PASS | PASS | port CoinRenderer.java shadow 0.09375 (SHADOW); scale 0.125 (unconditional s (if-reassignment default branch SCALE; writes: s = 2.0F)); cand |
| R2_if_write_then_return | PASS | PASS | PASS | port CoinRenderer.java shadow 0.09375 (SHADOW); scale 0.125 (unconditional s (if-reassignment default branch SCALE; writes: s = 2.0F)); cand |
| R3_if_write_else_return | PASS | PASS | PASS | port CoinRenderer.java shadow 0.09375 (SHADOW); scale 0.125 (unconditional s (if-reassignment default branch SCALE; writes: s = 2.0F)); cand |
| R4_guard_return_then_write | PASS | DIVERGES | DIVERGES | scale expected 0.125, found 2 (unconditional s (reassigned: s = 2.0F)); SCALE constant exists but scale() scales by s (holds 2.0F on the def |
| S1_switch_arrow_statement | PASS | DIVERGES | DIVERGES | port scale unparseable: local s is written inside a block that is neither if nor else (`s = 2.0F`) / port CoinRenderer.java shadow 0.09375 ( |
| S2_switch_expression_write | PASS | DIVERGES | DIVERGES | port scale unparseable: local s is written on the default path with an expression that cannot be evaluated (`s = switch (entity.getId() % 2) |
| S3_switch_old_default_write | PASS | DIVERGES | DIVERGES | port scale unparseable: local s is written inside a block that is neither if nor else (`s = 2.0F`) / port CoinRenderer.java shadow 0.09375 ( |
| T1_try_write | PASS | DIVERGES | DIVERGES | port scale unparseable: local s is written inside a block that is neither if nor else (`s = 2.0F`) / port CoinRenderer.java shadow 0.09375 ( |
| T2_finally_write | PASS | DIVERGES | DIVERGES | port scale unparseable: local s is written inside a block that is neither if nor else (`s = 2.0F`) / port CoinRenderer.java shadow 0.09375 ( |
| W10_line_comment_write | PASS | PASS | PASS | port CoinRenderer.java shadow 0.09375 (SHADOW); scale 0.125 (unconditional s); candidate CoinGeoReplacement.java shadow 0.09375 (CoinRendere |
| W11_block_comment_write | PASS | PASS | PASS | port CoinRenderer.java shadow 0.09375 (SHADOW); scale 0.125 (unconditional s); candidate CoinGeoReplacement.java shadow 0.09375 (CoinRendere |
| W12_block_comment_if_prefix | PASS | DIVERGES | DIVERGES | scale expected 0.125, found 2 (unconditional s (reassigned: s = 2.0F)); SCALE constant exists but scale() scales by s (holds 2.0F on the def |
| W13_string_close_brace_in_if | DIVERGES | DIVERGES | DIVERGES | scale expected 0.125, found 1 (no scale call); SCALE constant exists but no unconditional scale site (poseStack.scale, or a scaleModelForRen |
| W14_string_close_brace_pops_if_SCALE | DIVERGES | DIVERGES | DIVERGES | scale expected 0.125, found 1 (no scale call); SCALE constant exists but no unconditional scale site (poseStack.scale, or a scaleModelForRen |
| W15_if_true | PASS | PASS | PASS | port CoinRenderer.java shadow 0.09375 (SHADOW); scale 0.125 (unconditional s (if-reassignment default branch SCALE; writes: s = 2.0F)); cand |
| W16_prefix_names | PASS | PASS | PASS | port CoinRenderer.java shadow 0.09375 (SHADOW); scale 0.125 (unconditional s); candidate CoinGeoReplacement.java shadow 0.09375 (CoinRendere |
| W17_prefix_names_reverse | PASS | PASS | PASS | port CoinRenderer.java shadow 0.09375 (SHADOW); scale 0.125 (unconditional s2); candidate CoinGeoReplacement.java shadow 0.09375 (CoinRender |
| W19_chained_assignment | PASS | DIVERGES | DIVERGES | port scale unparseable: local s is written on the default path with an expression that cannot be evaluated (`s = t = 2.0F`: t = 2.0F) / port |
| W1_cast_write_literal | PASS | DIVERGES | DIVERGES | scale expected 0.125, found 2 (unconditional s (reassigned: s = (float) 2.0)); SCALE constant exists but scale() scales by s (holds (float)  |
| W20_nested_ternary_write | PASS | DIVERGES | DIVERGES | port scale unparseable: local s is written on the default path with an expression that cannot be evaluated (`s = entity.isBaby() ? SCALE : ( |
| W21_unconditional_after_conditional | PASS | DIVERGES | DIVERGES | scale expected 0.125, found 3 (unconditional s (reassigned: s = 2.0F; s = 3.0F)); SCALE constant exists but scale() scales by s (holds 3.0F  |
| W22_conditional_after_unconditional_SCALE | DIVERGES | PASS | PASS | port CoinRenderer.java shadow 0.09375 (SHADOW); scale 0.125 (unconditional s (if-reassignment default branch SCALE; writes: s = SCALE; s = 4 |
| W24_dangling_else_default_SCALE | DIVERGES | PASS | DIVERGES | port scale unparseable: local s is written inside a block that is neither if nor else (`s = 3.0F`) / port CoinRenderer.java shadow 0.09375 ( |
| W25_dangling_else_default_literal | PASS | DIVERGES | DIVERGES | port scale unparseable: local s is written inside a block that is neither if nor else (`s = 2.0F`) / port CoinRenderer.java shadow 0.09375 ( |
| W26_two_stmts_after_braceless_if | PASS | DIVERGES | DIVERGES | scale expected 0.125, found 3 (unconditional s (reassigned: s = 2.0F; s = 3.0F)); SCALE constant exists but scale() scales by s (holds 3.0F  |
| W27_braceless_else_after_braced_if | PASS | PASS | PASS | port CoinRenderer.java shadow 0.09375 (SHADOW); scale 0.125 (unconditional s (if/else-reassignment default branch SCALE; writes: s = 2.0F; s |
| W28_else_if_chain_final_else_literal | PASS | DIVERGES | DIVERGES | scale expected 0.125, found 4 (unconditional s (if/else-reassignment default branch 4.0F; writes: s = 2.0F; s = 3.0F; s = 4.0F)); SCALE cons |
| W29_two_sites_compound | DIVERGES | DIVERGES | DIVERGES | scale applied more than once, rendering at 0.25 (2 unconditional scales compound to 0.25: scale(): s = 0.125; scale(): s = 2); a pin needs e |
| W2_cast_write_SCALE | PASS | DIVERGES | DIVERGES | SCALE constant exists but scale() scales by s (holds (float) SCALE on the default path; writes: s = (float) SCALE) / port CoinRenderer.java  |
| W30_site_in_loop_write_after | DIVERGES | DIVERGES | DIVERGES | port scale unparseable: a scale inside a block that is neither if nor else / port CoinRenderer.java shadow 0.09375 (SHADOW); scale UNPARSED  |
| W31_bare_block_write | PASS | DIVERGES | DIVERGES | port scale unparseable: local s is written inside a block that is neither if nor else (`s = 2.0F`) / port CoinRenderer.java shadow 0.09375 ( |
| W32_if_nested_bare_block | PASS | DIVERGES | DIVERGES | port scale unparseable: local s is written inside a block that is neither if nor else (`s = 2.0F`) / port CoinRenderer.java shadow 0.09375 ( |
| W33_lambda_block_in_if_condition | PASS | DIVERGES | DIVERGES | port scale unparseable: local s is written inside a block that is neither if nor else (`s = 2.0F`) / port CoinRenderer.java shadow 0.09375 ( |
| W34_self_referencing_write | PASS | DIVERGES | DIVERGES | scale expected 0.125, found 0.25 (unconditional s (reassigned: s = s * 2.0F)); SCALE constant exists but scale() scales by s (holds s * 2.0F |
| W35_final_decl | PASS | PASS | PASS | port CoinRenderer.java shadow 0.09375 (SHADOW); scale 0.125 (unconditional s); candidate CoinGeoReplacement.java shadow 0.09375 (CoinRendere |
| W37_paren_args_if_reassign | DIVERGES | PASS | PASS | port CoinRenderer.java shadow 0.09375 (SHADOW); scale 0.125 (unconditional (s) (if-reassignment default branch SCALE; writes: s = 2.0F)); ca |
| W38_if_unevaluable_else_SCALE | PASS | PASS | PASS | port CoinRenderer.java shadow 0.09375 (SHADOW); scale 0.125 (unconditional s (if/else-reassignment default branch SCALE; writes: s = entity. |
| W3_same_line_decl_and_write | PASS | DIVERGES | DIVERGES | scale expected 0.125, found 2 (unconditional s (reassigned: s = 2.0F)); SCALE constant exists but scale() scales by s (holds 2.0F on the def |
| W42_loop_scoped_decl_then_redecl | PASS | PASS | PASS | port CoinRenderer.java shadow 0.09375 (SHADOW); scale 0.125 (unconditional s); candidate CoinGeoReplacement.java shadow 0.09375 (CoinRendere |
| W43_ternary_decl_then_compound_one | PASS | DIVERGES | DIVERGES | SCALE constant exists but scale() scales by s (holds (SCALE) * (1.0F) on the default path; writes: s *= 1.0F) / port CoinRenderer.java shado |
| W45_multiline_write_expr | DIVERGES | PASS | PASS | port CoinRenderer.java shadow 0.09375 (SHADOW); scale 0.125 (unconditional s (reassigned: s = SCALE)); candidate CoinGeoReplacement.java sha |
| W46_write_in_site_args | DIVERGES | DIVERGES | DIVERGES | port scale unparseable: scale argument not evaluable/uniform: [['s = 2.0F', 's', 's']] / port CoinRenderer.java shadow 0.09375 (SHADOW); sca |
| W47_else_write_after_if_without_write | DIVERGES | PASS | PASS | port CoinRenderer.java shadow 0.09375 (SHADOW); scale 0.125 (unconditional s (if/else-reassignment default branch SCALE; writes: s = SCALE)) |
| W48_if_write_SCALE_decl_literal | DIVERGES | DIVERGES | DIVERGES | scale expected 0.125, found 2 (unconditional s (if-reassignment default branch 2.0F; writes: s = SCALE)); SCALE constant exists but scale()  |
| W49_cast_args_plain_local | DIVERGES | PASS | PASS | port CoinRenderer.java shadow 0.09375 (SHADOW); scale 0.125 (unconditional (float) s); candidate CoinGeoReplacement.java shadow 0.09375 (Coi |
| W4_compound_div | PASS | DIVERGES | DIVERGES | scale expected 0.125, found 0.0625 (unconditional s (reassigned: s /= 2.0F)); SCALE constant exists but scale() scales by s (holds (SCALE) / |
| W50_string_semicolon_if_in_prefix | PASS | PASS | PASS | port CoinRenderer.java shadow 0.09375 (SHADOW); scale 0.125 (unconditional s (if-reassignment default branch SCALE; writes: s = 2.0F))); can |
| W5_compound_div_zero | PASS | DIVERGES | DIVERGES | port scale unparseable: local s is written on the default path with an expression that cannot be evaluated (`s /= 0.0F`: division by zero) / |
| W6_compound_mod | PASS | PASS | DIVERGES | port scale unparseable: local s is written in a form the scan does not read (`s %= 0.1F;`) / port CoinRenderer.java shadow 0.09375 (SHADOW); |
| W7_postincrement | PASS | PASS | DIVERGES | port scale unparseable: local s is written in a form the scan does not read (`s++;`) / port CoinRenderer.java shadow 0.09375 (SHADOW); scale |
| W8_preincrement | PASS | PASS | DIVERGES | port scale unparseable: local s is written in a form the scan does not read (`++s;`) / port CoinRenderer.java shadow 0.09375 (SHADOW); scale |
| W9_postdecrement | PASS | PASS | DIVERGES | port scale unparseable: local s is written in a form the scan does not read (`s--;`) / port CoinRenderer.java shadow 0.09375 (SHADOW); scale |

Cases: 73 (3 added after the LAB run: C1_candidate_unconditional_reassign, C2_candidate_loop_write, C3_candidate_if_reassign_default_SCALE). Flips LAB -> FIX2 among the 70 shared cases:
6 (L7_while_braceless_wrapping_braced_if, W24_dangling_else_default_SCALE, W6_compound_mod, W7_postincrement, W8_preincrement, W9_postdecrement), all PASS -> DIVERGES, each where a refuter showed the LAB copy trusting a value it
had not read.

## Documented limitations

- A braceless `for` wrapping a braced `if` (case L6) reads as a plain if-write with the declaration
  value as the default, because the for-header's semicolons hide the loop from the statement look-back. The
  value on the never-taken path is still the declaration's, so the PASS is the same convention as any if-write;
  the braceless `while` and `do` forms (L7, L2, L4) unbind.
- A non-evaluable write on a non-default branch was treated like a ternary's taken branch (the default path
  keeps the declaration value) — an interpretation presented for ruling, not a silent choice. Rejected by ruling 6
  (below): such a write is not provable and the entry is PENDING.
- Locals written through a method call, an array element, or a field are outside the scan by design; a scale
  site that reads anything but a literal, a constant, or a tracked float local is UNPARSED, which DIVERGES.

## Ruling 6 (2026-09-03): non-evaluable branch writes are PENDING

Owner's ruling (verbatim): "Scanner: a write inside a non-evaluable branch is not provable; report it as pending for
presentation, never assume a branch."

The interpretation left for ruling above (a non-evaluable write on a non-default branch treated like a ternary's taken
branch, the default path keeping the declaration value and the pin PASSing) is rejected. The tables above are the
pre-ruling state; that FIX2 result set is kept as `lab_results.FIX2.before_ruling6.json` in the lab scratchpad and
`lab_results.FIX2.json` is now the post-ruling run.

### What changed in the scanner (`tools/reference_renderer_pins.py`)

- `scan_locals`: every write to a tracked local is now evaluated on every path (a ternary by its else arm as before,
  and on a branch write its taken arm must read too — refuter follow-up below; a compound write folds into the bound
  value, under an `if` only when the local is bound). A write under an
  `if` / `else if` or in the final `else` whose expression the scan cannot evaluate, of a local that was readable up to
  that write, no longer keeps the pre-branch default: the local is left unbound with a new record key `pending` =
  `not provable: local <name> is written inside a branch with an expression the scan cannot evaluate (`<write>`) —
  pending presentation`, and later writes to it are skipped (as for `reason`). A default-path write (directly in the
  method body) whose expression cannot be evaluated still sets `reason` (DIVERGES, "written on the default path with an
  expression that cannot be evaluated"), because there the value at the site is provably not the pin. An if-write of a
  local nothing bound (an unreadable declaration) stays an if-reassignment as before: the site is UNPARSED through the
  declaration, a failure the pending must not mask.
- `local_site_fields`: carries `pending` from any argument's local to the site, next to `reason`.
- `scale_from_model_for_render`: a factor reading a pending local is a NOT_PROVABLE site (its `pending` suffixed
  ` (scaleModelForRender factor)`), next to the existing UNPARSED-with-reason site.
- `resolve_default_scale`: new status `NOT_PROVABLE` (value None, note = the pending detail) when the single
  unconditional site is pending-only — the branch write is its only obstacle: every other argument reads, the read
  values agree, three arguments, no local unbound for a reason (`pending_only`, set per site in `scale_from_calls`;
  a scaleModelForRender factor site is pending-only by construction). A provable failure outranks a pending: a site
  with a `reason` or any other unreadable argument still wins as UNPARSED, and two or more unconditional sites win as
  COMPOUND (value None, the unknown site printed as `not provable`); the pending write is quoted after the failure.
- `check_pin` / `check_candidate`: a NOT_PROVABLE scale (expected scale not null) is neither a failure nor a pass;
  the checks that need a PARSED value (constant-must-be-used, site method) are skipped, the `SCALE = ... != expected`
  constant check still runs.
- new `not_provable_details(port, candidate)`: the presentation details of the port and candidate scale axes (the
  candidate's suffixed ` (candidate <file>)`); in `main` a pin entry with no problems and a non-empty list is
  `PENDING` with detail `<details joined by "; "> | <port_summary>`, so the detail starts with the phrase above; a mod
  entry likewise (its recorded-scale compare is skipped when NOT_PROVABLE). PENDING is printed per entry
  (`REFERENCE RENDERERS PENDING: <entity>: <detail>`), counted in the summary line, and carried in the JSON
  (`entries[].status` / `detail`, `port.scale.status = "NOT_PROVABLE"` with the detail as `note`,
  `scale_sites[].pending`, `local.pending`). Exit code unchanged: 1 only on DIVERGES / MANIFEST_DRIFT.
- `scale_word`: prints `NOT_PROVABLE` in the port summary.
- Reference side: the same scan applies, so a 1.7.10 renderer with such a write would become UNPARSED_REFERENCE (a
  note, manifest ruling accepted) instead of being parsed by assuming a branch; none does (unparsed reference axes 9
  before and after).
- Unchanged by design: a ternary at the declaration or in a method-body (default-path) write still resolves to its
  else arm (the earlier ruling's convention; on a branch write both arms must read, see the follow-up); a conditional
  site (`if (...) poseStack.scale(x, x, x)`) reading a pending local still defaults to 1.0, exactly as one reading an
  unbound local did; the header docstrings (LOCALS paragraph, Statuses) say all of this.

### Lab flips (73 cases, `lab_results.FIX2.before_ruling6.json` -> `lab_results.FIX2.json`)

| case | before | after | after detail (verbatim) |
|---|---|---|---|
| W38_if_unevaluable_else_SCALE | PASS | PENDING | not provable: local s is written inside a branch with an expression the scan cannot evaluate (`s = entity.getBbHeight()`) — pending presentation \| port CoinRenderer.java shadow 0.09375 (SHADOW); scale NOT_PROVABLE (not provable: local s is written inside a branch with an expression the scan cannot evaluate (`s = entity.getBbHeight()`) — pending presentation); candidate CoinGeoReplacement.java shadow 0.09375 (CoinRenderer.SHADOW) scale 0.125 (unconditional CoinRenderer.SCALE) |
| W50_string_semicolon_if_in_prefix | PASS | PENDING | not provable: local s is written inside a branch with an expression the scan cannot evaluate (`s = 2.0F)`) — pending presentation \| port CoinRenderer.java shadow 0.09375 (SHADOW); scale NOT_PROVABLE (not provable: local s is written inside a branch with an expression the scan cannot evaluate (`s = 2.0F)`) — pending presentation); candidate CoinGeoReplacement.java shadow 0.09375 (CoinRenderer.SHADOW) scale 0.125 (unconditional CoinRenderer.SCALE) |

Before details: W38 `port CoinRenderer.java shadow 0.09375 (SHADOW); scale 0.125 (unconditional s (if/else-reassignment
default branch SCALE; writes: s = entity.getBbHeight(); s = SCALE)); candidate ... scale 0.125 (unconditional
CoinRenderer.SCALE)`; W50 `... scale 0.125 (unconditional s (if-reassignment default branch SCALE; writes: s = 2.0F)));
candidate ...`.

W38 (`float s = SCALE; if (entity.isBaby()) s = entity.getBbHeight(); else s = SCALE;`) is the expected flip. W50 was
not expected: its scale() is `float s = SCALE; String q = "x; if (y)" + (s = 2.0F); poseStack.scale(s, s, s);`. The
statement look-back does not lex string literals, so the `; if (y)` inside the literal makes the write read as a
brace-less if-branch write with the expression `2.0F)`. Before the ruling that garbled expression was never evaluated
and the case PASSed on the declaration default (the tell-tale `writes: s = 2.0F)))` in its FIX2 note above, the
write's stray `)` before the note's own two); under the
ruling a branch expression must be evaluable, `2.0F)` is not, and the case is PENDING. That is the ruling working as
stated (the scan cannot prove the write, so it presents it instead of passing it), but it also exposes a pre-existing
blind spot: in Java that write is unconditional and the coin renders at 2.0, so the truthful status is DIVERGES
(found 2). Lexing string literals out of the look-back (as comments already are) would be its own change and needs
its own ruling; nothing was changed for it here. Every other case (52 DIVERGES, 19 PASS) kept its status and detail;
exit codes stay consistent with status (a PENDING run exits 0). Unit probes of the new paths (final else, else if,
braced and nested branches, a compound `s /= 0.0F` under an if, a scaleModelForRender factor, a pending local read
next to a loop-written one, a pending local read only by a conditional site) are in the scratchpad
`pins_reassign_refute/probe_ruling6.py`.

### Repo leg (`tools/reference_renderer_pins.json`; `repo_after_fix.json` -> `repo_after_ruling6.json`)

Before: `PASS 120, PENDING 0, MOD 0, NOT_APPLICABLE 13, DIVERGES 0, MANIFEST_DRIFT 0; unparsed reference axes 9; OK`.
After: the identical summary line; 0 of 133 entries changed status, detail, notes, or any port / candidate /
reference axis status (`pins_reassign_refute/diff_reports.py`). No repo renderer writes a scale-site local inside a
branch with an expression the scan cannot evaluate, so nothing needs presenting before the gate. The follow-up run
below (`repo_after_ruling6_followup.json`) is identical again: 0 of 133 entries changed.

### Refuter follow-up (2026-09-04): precedence, and ternary arms

The refuter of the change above ran an adversarial set of 51 cases (scratchpad `refuter_r6/run_adversarial.py`, per
case `runs/<case>/`; the pre-fix runs kept as `runs.before_followup/` and `adversarial_before_followup.json`) and
found two holes, both fixed in `tools/reference_renderer_pins.py`:

1. Precedence — a provable failure at a site must outrank the new PENDING. `resolve_default_scale` returned
   NOT_PROVABLE as soon as a site carried a pending local, so a site that ALSO had an independent provable failure read
   PENDING exit 0: a second unconditional scale that compounds (cases t, `scale(s, s, s); scale(SCALE, SCALE, SCALE)`,
   and ak, the write and site in a render() wrapper over a scale() that scales by SCALE — both COMPOUND / DIVERGES
   exit 1 before the ruling), or another non-evaluable argument (case ag, `scale(s, entity.getBbWidth(), s)`, UNPARSED
   / DIVERGES before the ruling). Now `scale_from_calls` marks a site `pending_only` only when the pending write is
   its only obstacle (every other argument reads, the read values agree, three arguments, no local unbound for a
   reason; `scale_from_model_for_render` marks its pending factor site the same way), and `resolve_default_scale`
   reads that: a site that is not pending-only is UNPARSED exactly as before (the pending write quoted after the
   failure), two or more unconditional sites are COMPOUND whatever a pending local holds (value None, the unknown site
   printed as `not provable`, `check_pin` wording "rendering at a product that is not provable", `scale_word`
   `COMPOUND (product not provable)`), and NOT_PROVABLE is returned only for a single pending-only site. So DIVERGES-
   class outcomes — compound scaling, an unparseable / non-evaluable other argument, a default-path non-evaluable
   write, a local unbound for a reason — win over pending, and the leg fails (exit 1) as it did before the ruling.
2. Never assume a branch, ternary arms included. `scan_locals` read a ternary written on a branch by its else arm
   only, so `if (c) s = d ? entity.getBbHeight() : 2.0F;` (case k) PASSed through the else-arm convention — assuming
   the ternary's branch. Now, on a branch write (under an `if` / `else if`, or in the final `else`), both arms of a
   ternary must read, else the write is pending. The else-arm convention itself is kept where the earlier ruling put
   it: a ternary at the DECLARATION (`float s = c ? entity.getBbHeight() : SCALE;`, case m, still PASS) and a ternary
   in a method-body write on the default path (`s = d ? 3.0F : SCALE;`, still "ternary-reassignment" by its else
   arm); a final-`else` ternary whose arms both read still binds its else arm as the default path, as before.

Adversarial statuses, before the fixes -> after (exit codes consistent with status throughout):

| case | before | after | after detail (verbatim) |
|---|---|---|---|
| t_if_noneval_then_second_site | PENDING | DIVERGES | scale applied more than once, rendering at a product that is not provable (2 unconditional scales compound to a product that is not provable: scale(): s = not provable; scale(): SCALE = 0.125; not provable: local s is written inside a branch with an expression the scan cannot evaluate (`s = entity.getBbHeight()`) — pending presentation); a pin needs exactly one unconditional uniform scale \| port CoinRenderer.java shadow 0.09375 (SHADOW); scale COMPOUND (product not provable) (2 unconditional scales compound to a product that is not provable: scale(): s = not provable; scale(): SCALE = 0.125; not provable: local s is written inside a branch with an expression the scan cannot evaluate (`s = entity.getBbHeight()`) — pending presentation); candidate CoinGeoReplacement.java shadow 0.09375 (CoinRenderer.SHADOW) scale 0.125 (unconditional CoinRenderer.SCALE) |
| ak_if_noneval_in_render_wrapper | PENDING | DIVERGES | as t, with `scale(): SCALE = 0.125; render(): s = not provable` |
| ag_pending_plus_noneval_arg | PENDING | DIVERGES | port scale unparseable: scale argument not evaluable/uniform: [['s', 'entity.getBbWidth()', 's']]; not provable: local s is written inside a branch with an expression the scan cannot evaluate (`s = entity.getBbHeight()`) — pending presentation \| port CoinRenderer.java shadow 0.09375 (SHADOW); scale UNPARSED (scale argument not evaluable/uniform: [['s', 'entity.getBbWidth()', 's']]; not provable: local s is written inside a branch with an expression the scan cannot evaluate (`s = entity.getBbHeight()`) — pending presentation); candidate CoinGeoReplacement.java shadow 0.09375 (CoinRenderer.SHADOW) scale 0.125 (unconditional CoinRenderer.SCALE) |
| k_if_ternary_taken_noneval | PASS | PENDING | not provable: local s is written inside a branch with an expression the scan cannot evaluate (`s = d ? entity.getBbHeight() : 2.0F`) — pending presentation \| port CoinRenderer.java shadow 0.09375 (SHADOW); scale NOT_PROVABLE (not provable: local s is written inside a branch with an expression the scan cannot evaluate (`s = d ? entity.getBbHeight() : 2.0F`) — pending presentation); candidate CoinGeoReplacement.java shadow 0.09375 (CoinRenderer.SHADOW) scale 0.125 (unconditional CoinRenderer.SCALE) |

The other 47 cases kept their status (al_pending_local_and_reason_local, DIVERGES, now also quotes the pending write
after its reason). New statuses of the whole set: PENDING — a, ab, ac (W50), ad, ah, ai, aj, b, both_port_and_cand_pending,
c, cand_else_noneval, cand_if_noneval, e, f, g, h, i, j, k, l, manifest_pending_clean, manifest_pending_if_noneval,
mod_if_noneval, n, r, s, x, y, z; DIVERGES — ae, af, ag, ak, al, cand_default_noneval, d, mod_default_noneval, o,
port_pending_cand_diverges, q, t, u, v, w; PASS — aa, m, p; MOD — mod_clean; MANIFEST_DRIFT — dynamic_if_noneval,
dynamic_SCALE_then_if_noneval, dynamic_getter_decl_clean (by construction: the refuter's manifest override rules the
Coin scale dynamic while the 1.7.10 RenderCoin parses to 0.125, so the drift check fires before the port is judged).

Disclosures the refuter asked for:

- A final-`else` non-evaluable write (`if (c) s = 2.0F; else s = entity.getBbHeight();`, cases b, aj,
  cand_else_noneval) was DIVERGES exit 1 before the ruling ("written on the default path with an expression that
  cannot be evaluated": the scanner reads the final else as the default path) and is PENDING exit 0 now, by the
  ruling's letter — the final else is a branch, and no branch is assumed. The repo has no such write; the lab has none.
- Over-conservative, never a wrong PASS: a later unconditional `s = SCALE` does not clear a pending (case h,
  `if (c) s = entity.getBbHeight(); s = SCALE;` is PENDING although the site provably reads SCALE — once pending, later
  writes are skipped, as for a local unbound for a reason); pending does not propagate through `float s = t` (case q,
  DIVERGES "scale argument not evaluable/uniform", not PENDING) nor through an expression argument (`scale(s * 2.0F,
  ...)` over a pending s is DIVERGES the same way); a pending local next to a literal or another local in the same
  call (cases ah `scale(s, 2.0F, s)`, ai `scale(s, t, s)`) stays PENDING, because whether the site is uniform depends
  on the pending value itself, while read values that differ from each other (`scale(s, 2.0F, 3.0F)`) or a call
  without three arguments are provable failures and DIVERGE.
- The W50 quote above originally read `writes: s = 2.0F))`; the note actually reads `writes: s = 2.0F)))` (corrected).

Re-runs after the fixes: the 73-case lab (`lab_results.FIX2.after_ruling6.json` kept as the ruling-6 run ->
`lab_results.FIX2.json`) has 0 flips and no changed detail (52 DIVERGES, 19 PASS, 2 PENDING: W38 and W50, whose
rows above stay verbatim); the repo leg (`repo_after_ruling6_followup.json`) prints the identical
`PASS 120, PENDING 0, MOD 0, NOT_APPLICABLE 13, DIVERGES 0, MANIFEST_DRIFT 0; unparsed reference axes 9; OK` with
0 of 133 entries changed against `repo_after_fix.json`. Byte check: 0 backspace bytes, no other control bytes,
`py_compile` OK (both this session's `check_bs.py` and the refuter's `check_bytes_compile.py`).

