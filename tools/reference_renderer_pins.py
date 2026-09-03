#!/usr/bin/env python3
"""Reference-renderer pins leg: 1.7.10 renderer shadow/scale versus the port renderer's SHADOW/SCALE.

Owner ruling (2026-09-03, ENT-S-092): pin renderer scale and shadow in the reference gate.
MOD-recorded renderers keep their recorded values; the rest restore 1.7.10.

Two independent sides, compared through the manifest (tools/reference_renderer_pins.json):

REFERENCE SIDE (read-only, never the port): every `registerEntityRenderingHandler(X.class, (Render)new
RenderX(<model>, par2, par3))` in ClientProxyOreSpawn.java is re-parsed, then RenderX.java:
  constructor   `super((ModelBase)m, par2 * par3)`  -> RenderLiving shadowSize = par2 * par3
                `super(m, par2)`                    -> RenderLiving/RenderBiped shadowSize = par2
                `this.field_76989_e = 0.25f`        -> Render.shadowSize literal
                no constructor anywhere up an in-tree chain ending at Render -> shadowSize 0
                `this.scale = par3`                 -> the field preRenderCallback scales by
  scale         `func_77041_b` (preRenderCallback) must be overridden; its glScalef (directly or through
                the `preRenderScale` it delegates to) is the render scale: `this.scale` -> par3, a literal
                -> the literal; a glScalef only inside an `if` block is conditional and the default stays 1.0.
                A plain Render (sprite) subclass scales in doRender (`func_76986_a`): one unconditional uniform
                literal glScalef is the scale; `if (e instanceof <Entity>) return;` before drawing -> nothing
                drawn (null). A RenderLiving subclass whose doRender never reaches super.doRender bypasses
                preRenderCallback and is read the same way (flips (-1,-1,1) are ignored).
Anything else (a vanilla parent with no source here, a per-entity getter feeding glScalef, several
scales in one path) is UNPARSED_REFERENCE: the manifest's ref_derivation is accepted as the ruling.
Where the parse yields a number it must equal the manifest (MANIFEST_DRIFT otherwise).

PORT SIDE: the port renderer's `static final float SCALE = ...;` / `SHADOW = ...;` (simple float
expressions are evaluated), the constructor's shadow pin site — the `super(context, model, <shadow>)` last
argument or a `shadowRadius = <shadow>;` / `this.shadowRadius = <shadow>;` / `super.shadowRadius =
<shadow>;` body write (SHADOW or a literal equal to the expectation) — and its
`<poseStack>.scale(SCALE, SCALE, SCALE)` in the `scale(...)` override
(or the render()-wrapper form, which is the same uniform scale about the entity origin), where <poseStack>
is whatever the scanned method names its PoseStack parameter (`poseStack`, `stack`, `ps`...) — or, the
GeckoLib form (ENT-S-092, TheQueen), the factor a `scaleModelForRender(float widthScale, float heightScale,
PoseStack ...)` override hands on: `super.scaleModelForRender(widthScale * <factor>, heightScale * <factor>,
...)` reaches GeckoLib's default `poseStack.scale(w, h, w)` after its entityRenderTranslations capture, so
that super call is one scale site (method scaleModelForRender) whose value is <factor>. The factor is read
from the first two arguments as `<param> * <factor>` / `<factor> * <param>` under the override's own
parameter names; width and height must carry the same factor expression, else the site is UNPARSED; a
ternary local (`float f = cond ? SCALE / 4.0F : SCALE;`) resolves to its default branch exactly as in
scale(); a pass-through `super.scaleModelForRender(widthScale, heightScale, ...)` applies nothing, and an
override that never reaches super applies nothing from that slot (only its own `<poseStack>.scale(` sites
count, which are read like any other method's); a scaleModelForRender factor plus a scale()/render() site
compound like any two sites. An
expected scale of exactly 1.0 accepts an explicit 1.0 or no override. Constant-must-be-used, on both
axes: when SHADOW exists the constructor must pass it, and when SCALE exists the unconditional scale
site(s) must apply it (a literal, or a ternary local whose default branch is not SCALE, fails even at
the right value); an unused SCALE is tolerated only when SCALE and the expectation are both 1.0.
Exactly one unconditional uniform scale is allowed: two sites compound (scale() plus a render()
wrapper both scaling by SCALE renders at SCALE squared) and fail as COMPOUND with the product printed.
The constructor value is the shadow only when nothing rewrites it at render time: any `getShadowRadius(`
(an override, as the port's GeckoLib base does, or a call) or any `shadowRadius =` outside the constructor
body (a render() wrapper, a field initialiser, another method), in the renderer or an in-tree parent, makes
the shadow UNPARSED and the pin DIVERGES; so does a constructor that writes shadowRadius more than once,
where a 3-argument `super(context, model, <shadow>)` counts as one write and every `shadowRadius =` in the
body (bare, `this.`- or `super.`-qualified; compound `+=` etc. too) as another — `super(context, model, SHADOW);
shadowRadius = X;` is two writes and DIVERGES, while `super(context, model); shadowRadius = SHADOW;` is one
pin whose value is SHADOW (the last write is the rendered shadow).
A GeckoLib candidate descriptor (`*GeoReplacement.java`) must pass the port renderer's own constant,
`<PortRenderer>.SHADOW` (the port_renderer file stem; another renderer's equal-valued SHADOW fails) or an
equal literal when no SHADOW exists, to GeoReplacementDescriptor and scale by `<PortRenderer>.SCALE` in
applyScale when the expectation is not 1.0; the same getShadowRadius / shadowRadius-write scan applies
to the candidate file. A named
port_candidate is part of the pin: when the file is missing (renamed, deleted) or cannot be parsed the pin
DIVERGES, exactly as a missing port renderer does; the candidate axis is never dropped to a NOTE.

LOCALS (both sides, every scale site): a float local is read by the value it holds on the default path at the
site, never by its declaration alone. `float x = <expr>;` binds x (a ternary initialiser binds its else branch and
makes x conditional, as above). Every later write to x before the site (`x = <expr>;`, or a compound `x *= 2.0F;`
etc.) is then read by the path it sits on, the same default-branch rule resolve_default_scale applies to scale
sites: a write directly in the method body, or in the final `else` of an if/else chain, is on the default path and
replaces the value (the last such write wins, and ITS expression, not the declaration's, is what must be SCALE);
a write under an `if` / `else if` (braced or brace-less, at any nesting) is off the default path: the value
stays, x is conditional with the pre-branch expression as its default (`float x = SCALE; if (baby) x = SCALE /
2.0F; scale(x, x, x)` passes as SCALE, marked if-reassignment default branch SCALE, exactly as the ternary form),
and the branch value is not used (a ternary's taken branch is not either); a ternary written into x resolves to
its else branch; a write inside any other block (switch, loop, lambda, bare block) or a default-path write (directly
in the method body) whose expression cannot be evaluated leaves x unbound with a reason, so every site reading x is
UNPARSED (the pin DIVERGES with the write quoted). Ruling 6 (2026-09-03), "a write inside a non-evaluable branch is
not provable; report it as pending for presentation, never assume a branch": a write on ANY branch (under an `if` /
`else if`, or in the final `else`) whose expression the scan cannot evaluate, of a local that was readable up to that
write, leaves x not provable — no branch is assumed and no value is read: every site reading x is NOT_PROVABLE and
the entry is PENDING with the detail `not provable: local x is written inside a branch with an expression the scan
cannot evaluate (`<write>`) — pending presentation` (listed per entry, counted in the summary line, carried in the
JSON report; it does not fail the leg and it is never a PASS). An evaluable branch write keeps the rules above.
A ternary in a branch write must read on both arms (its taken arm is a branch too), else it is pending; the else-arm
convention stays for a ternary at the declaration and in a default-path write. Pending applies only when that write
is the site's ONLY obstacle: a provable failure outranks it — a second unconditional scale (COMPOUND, the unknown
value printed as `not provable`), an argument the scan cannot evaluate for another reason, read values that differ, a
local unbound for a reason (a loop / switch / lambda write, an unread form, a default-path write that cannot be
evaluated) — and the pin DIVERGES as before, the pending write quoted after the failure.
Writes at or after the site never reach it. Declarations and writes
are read per site, so `float x = SCALE; x = 2.0F; scale(x, x, x)` is a scale of 2 (found 2, not SCALE), while
`scale(x, x, x); x = 2.0F;` is still SCALE.
A site argument written as `(float) x` or `(x)` resolves to the local x (a cast or parenthesis is a no-op),
so it satisfies constant-must-be-used exactly as a bare x does. Brace-less nesting is read only one
statement deep: a brace-less prefix that stacks two ifs (a dangling else) or carries a loop keyword
leaves the written local unbound (UNPARSED) rather than guessing the default path; `x %= e;`, `x++;`
and `x--;` writes are recorded as unreadable and unbind the local the same way.

Statuses: PASS, DIVERGES, PENDING (a manifest-declared known divergence awaiting its batch, or — ruling 6 — a pin
or mod entry whose scale-site local is written inside a branch with an expression the scan cannot evaluate; the
current port values are printed either way), MOD, NOT_APPLICABLE, MANIFEST_DRIFT. Exit 1 on any DIVERGES or
MANIFEST_DRIFT. A manifest entry
missing a required key is MANIFEST_DRIFT for that entry (never a crash: the report is still written).
expected_scale may be the string "dynamic" (with scale_dynamic_note) when both sides scale by an entity getter:
the shadow axis is pinned as usual, the scale axis is not a constant to pin, and a reference that parses to a
number under a dynamic ruling is MANIFEST_DRIFT. Any expected value that is not a number, null or (scale only)
"dynamic" is MANIFEST_DRIFT.
"""

from __future__ import annotations

import argparse
import json
import sys
import re
from pathlib import Path
from typing import Any

TOLERANCE = 1.0e-5
IDENT = r"[A-Za-z_][A-Za-z0-9_]*"
RE_REGISTRATION = re.compile(r"registerEntityRenderingHandler\((" + IDENT + r")\.class,\s*\(Render\)new (Render" + IDENT + r")\(")
RE_CLASS = re.compile(r"(?:public\s+)?(?:final\s+)?(?:abstract\s+)?class\s+(" + IDENT + r")(?:<[^{]*?>)?\s+extends\s+(" + IDENT + r")")
RE_METHOD = re.compile(
    r"(?:@Override\s+)?(?:public|protected|private)\s+(?:static\s+)?(?:final\s+)?(?:<[^>]+>\s+)?"
    r"(?:[\w.<>\[\],?]+\s+)?(" + IDENT + r")\s*\(([^)]*)\)\s*(?:throws\s+[\w., ]+)?\s*\{", re.S)
RE_CONSTANT = re.compile(r"static\s+final\s+float\s+(" + IDENT + r")\s*=\s*([^;]+);")
RE_LOCAL = re.compile(r"\bfloat\s+(" + IDENT + r")\s*=\s*([^;]+);")
# a type keyword right before `x = ...` makes it a declaration (RE_LOCAL's), not a write to an existing local
RE_DECLARATION_PREFIX = re.compile(r"\b(?:float|double|int|long|final)\s*$")
# a statement prefix (the text since the last `{`, `}` or `;`) that is a brace-less if / else if / else
RE_PREFIX_ELSE_IF = re.compile(r"\belse\s+if\s*\(")
RE_PREFIX_ELSE = re.compile(r"\belse\s*$")
RE_PREFIX_IF = re.compile(r"\bif\s*\(")


def local_write_re(name: str) -> re.Pattern:
    """A write to the local `name` after its declaration: `name = <expr>;` or a compound `name op= <expr>;`
    (op one of + - * /). `==` `<=` `>=` `!=` are comparisons, `o.name =` is another object's field, and the
    declaration itself is excluded by RE_DECLARATION_PREFIX at the match."""
    return re.compile(r"(?<![\w.])" + re.escape(name) + r"\s*(?:([-+*/])=|=(?!=))\s*([^;]+);")



# a plain-assignment pin site of the shadow field: `this.shadowRadius = <expr>;`, `super.shadowRadius =
# <expr>;` or a bare `shadowRadius = <expr>;` (Java reads all three as the same inherited field write;
# `x.shadowRadius` is another object's and is not a pin site)
RE_SHADOW_FIELD = re.compile(r"(?:\b(?:this|super)\.|(?<![\w.]))shadowRadius\s*=(?!=)\s*([^;]+);")
# any write to the shadow field: the plain form above or a compound one (`+=`, `*=`...); `==` is a comparison
RE_SHADOW_ASSIGN = re.compile(r"(?:\b(?:this|super)\.|(?<![\w.]))shadowRadius\s*[-+*/]?=(?!=)")
# the name of the PoseStack parameter of a scanned method: its `.scale(` is the scale site, whatever
# the parameter is called (`poseStack`, `stack`, `ps`, fully-qualified `com...PoseStack stack`)
RE_POSE_STACK_PARAM = re.compile(r"\bPoseStack\s+(" + IDENT + r")\b")
# GeckoLib's post-capture scale slot: a `scaleModelForRender(...)` override that multiplies the width/height
# factors it hands to `super.scaleModelForRender(` applies that factor as `poseStack.scale(w, h, w)`
RE_SUPER_SCALE_MODEL = re.compile(r"\bsuper\.scaleModelForRender\s*\(")
RE_FLOAT_PARAM = re.compile(r"\bfloat\s+(" + IDENT + r")\b")
# the port methods scanned for scale sites: `<poseStack>.scale(` in any of them, plus the super factor of
# a scaleModelForRender override
SCALE_METHODS = ("scale", "render", "preRender", "applyScale", "scaleModelForRender")
# a getShadowRadius(...) override (or any call to it) replaces the constructor's shadow at render time
RE_GET_SHADOW = re.compile(r"\bgetShadowRadius\s*\(")
RE_REF_SHADOW_FIELD = re.compile(r"this\.field_76989_e\s*=\s*([^;]+);")
RE_REF_SCALE_FIELD = re.compile(r"this\.scale\s*=\s*(" + IDENT + r")\s*;")
RE_EARLY_RETURN = re.compile(r"if\s*\(\s*" + IDENT + r"\s+instanceof\s+(" + IDENT + r")\s*\)\s*\{?\s*return\s*;")

# 1.7.10 vanilla parents whose (model, shadow) constructor stores the shadow untouched and whose
# preRenderCallback is a no-op; anything else without source in the reference tree is unparseable.
VANILLA_LIVING_PASSTHROUGH = {"RenderLiving", "RenderBiped"}
VANILLA_PLAIN = {"Render"}
# keys every manifest entry must carry (values may be null, e.g. a per-entity expected_scale); a missing
# one is a MANIFEST_DRIFT line for that entry, not a KeyError
REQUIRED_ENTRY_KEYS = ("entity", "ref_line", "ref_render_class", "expected_shadow", "expected_scale", "port_renderer", "status")


# ------------------------------------------------------------------ small parsing helpers


def balanced_args(text: str, open_index: int) -> tuple[list[str], int]:
    """Split the argument list whose '(' is at open_index; returns (args, index after ')')."""
    depth = 0
    args: list[str] = []
    current: list[str] = []
    i = open_index
    while i < len(text):
        ch = text[i]
        if ch in "([{":
            depth += 1
            if depth > 1:
                current.append(ch)
        elif ch in ")]}":
            depth -= 1
            if depth == 0:
                joined = "".join(current).strip()
                if joined or args:
                    args.append(joined)
                return args, i + 1
            current.append(ch)
        elif ch == "," and depth == 1:
            args.append("".join(current).strip())
            current = []
        else:
            current.append(ch)
        i += 1
    raise ValueError("unbalanced argument list")


def block_body(text: str, open_brace: int) -> tuple[str, int]:
    """Body between the '{' at open_brace and its matching '}' (exclusive); returns (body, end index)."""
    depth = 0
    for i in range(open_brace, len(text)):
        if text[i] == "{":
            depth += 1
        elif text[i] == "}":
            depth -= 1
            if depth == 0:
                return text[open_brace + 1:i], i
    raise ValueError("unbalanced block")


def strip_comments(text: str) -> str:
    text = re.sub(r"/\*.*?\*/", lambda m: re.sub(r"[^\n]", " ", m.group(0)), text, flags=re.S)
    return re.sub(r"//[^\n]*", "", text)


def methods(text: str) -> list[dict[str, Any]]:
    """Every method/constructor body in the file, innermost-last order not guaranteed; spans are absolute."""
    found = []
    for m in RE_METHOD.finditer(text):
        name = m.group(1)
        if name in ("if", "for", "while", "switch", "catch", "synchronized", "return", "new", "else"):
            continue
        try:
            body, end = block_body(text, m.end() - 1)
        except ValueError:
            continue
        found.append({"name": name, "params": m.group(2), "start": m.end(), "end": end, "body": body})
    return found


def enclosing_block(body: str, position: int) -> str:
    """'method' when the statement at position sits directly in the method body, else the header of the
    innermost enclosing block: 'if', 'else', 'else if', or 'other' (switch, lambda, loop...)."""
    stack: list[str] = []
    last_boundary = 0
    for i, ch in enumerate(body[:position]):
        if ch == "{":
            stack.append(body[last_boundary:i].strip())
            last_boundary = i + 1
        elif ch == "}":
            if stack:
                stack.pop()
            last_boundary = i + 1
        elif ch == ";":
            last_boundary = i + 1
    # a brace-less `if (...)` statement immediately before the call
    previous = body[last_boundary:position].strip()
    if re.match(r"^if\s*\(", previous) and not stack_top_is_if(stack):
        return "if"
    if not stack:
        return "method"
    header = stack[-1]
    if re.search(r"\belse\s+if\s*\(", header):
        return "else if"
    if re.search(r"\belse\b\s*$", header):
        return "else"
    if re.search(r"\bif\s*\(", header):
        return "if"
    return "other"


def stack_top_is_if(stack: list[str]) -> bool:
    return bool(stack) and bool(re.search(r"\bif\s*\(", stack[-1]))


def header_kind(header: str) -> str:
    """'if', 'else if', 'else' or 'other' for a block header (the text before its '{') or for the brace-less
    statement prefix a write sits behind (`else x = ...;`, `if (...) x = ...;`, `for (...) x = ...;`)."""
    # a brace-less prefix or header that stacks two ifs (a dangling else cannot be assigned to one of them
    # by text) or carries a loop keyword is not read: conservative 'other'
    if len(re.findall(r"\bif\s*\(", header)) >= 2 or re.search(r"\b(?:for|while|do)\b", header):
        return "other"
    if RE_PREFIX_ELSE_IF.search(header):
        return "else if"
    if RE_PREFIX_ELSE.search(header):
        return "else"
    if RE_PREFIX_IF.search(header):
        return "if"
    return "other"


def block_path(body: str, position: int) -> list[str]:
    """Every block enclosing position, outermost first, each read by header_kind, with a non-empty brace-less
    statement prefix (`else`, `if (...)`, `for (...)`, `case 1:`...) as the innermost entry; empty when the
    statement sits directly in the method body. Unlike enclosing_block (which names the innermost block only)
    this keeps the whole nesting, so an `else` under an outer `if` is still read as conditional."""
    stack: list[str] = []
    last_boundary = 0
    prev_boundary = 0  # the boundary before last_boundary: the previous statement's start
    for i, ch in enumerate(body[:position]):
        if ch == "{":
            stack.append(body[last_boundary:i].strip())
            prev_boundary, last_boundary = last_boundary, i + 1
        elif ch == "}":
            if stack:
                stack.pop()
            prev_boundary, last_boundary = last_boundary, i + 1
        elif ch == ";":
            prev_boundary, last_boundary = last_boundary, i + 1
    path = [header_kind(header) for header in stack]
    prefix = body[last_boundary:position].strip()
    if prefix:
        kind = header_kind(prefix)
        if kind == "else":
            # a brace-less `else` binds to the nearest preceding brace-less `if`; when the previous statement
            # stacked two ifs (`if (a) if (b) x = ...;`) the text cannot say which, so the else is unreadable
            previous = body[prev_boundary:last_boundary]
            if len(re.findall(r"\bif\s*\(", previous)) >= 2 or re.search(r"\b(?:for|while|do)\b", previous):
                kind = "other"
        path.append(kind)
    return path


def write_kind(path: list[str]) -> str:
    """How a write to a local reaches the default path, from its block_path: 'method' (directly in the body),
    'else' (only final-else blocks enclose it: the default path of an if/else chain, as resolve_default_scale
    reads an else scale site), 'if' (an if / else if encloses it at some level: off the default path), or
    'other' (a switch, loop, lambda, bare block or an unrecognised statement prefix encloses it: unreadable)."""
    if any(kind == "other" for kind in path):
        return "other"
    if any(kind in ("if", "else if") for kind in path):
        return "if"
    return "else" if path else "method"


def compound(op: str, current: float, rhs: float) -> float:
    """`x op= rhs` on a bound float local."""
    if op == "+":
        return current + rhs
    if op == "-":
        return current - rhs
    if op == "*":
        return current * rhs
    if op == "/":
        if rhs == 0:
            raise Unevaluable("division by zero")
        return current / rhs
    raise Unevaluable(f"compound operator {op}")


def local_name_of(expr: str) -> str | None:
    """The local an argument reads directly (`x`, `(x)`, `(float)x`); None for any other expression."""
    text = strip_outer_parens(re.sub(r"\(\s*(?:float|double|int)\s*\)", "", expr))
    return text if re.fullmatch(IDENT, text) else None


def local_site_fields(locals_info: dict[str, dict[str, Any]], args: list[str]) -> dict[str, Any]:
    """The per-site fields describing the local the first argument reads (all None when it reads none):
    `local` (its scan_locals record), `ternary_default` (a ternary initialiser's else branch, as before),
    `conditional_default` / `conditional_form` (the default branch and form of a conditional local of any form),
    `reason` when any argument reads a local a write left unbound (the site is then UNPARSED), and `pending` when any
    argument reads a local a branch write left not provable (ruling 6: the site is then NOT_PROVABLE)."""
    first = locals_info.get(local_name_of(args[0])) if args else None
    fields: dict[str, Any] = {
        "local": first,
        "ternary_default": first["default"] if first and first["form"] == "ternary" else None,
        "conditional_default": first["default"] if first else None,
        "conditional_form": first["form"] if first else None,
    }
    reasons: list[str] = []
    pendings: list[str] = []
    for arg in args:
        record = locals_info.get(local_name_of(arg))
        if record and record["reason"] and record["reason"] not in reasons:
            reasons.append(record["reason"])
        if record and record.get("pending") and record["pending"] not in pendings:
            pendings.append(record["pending"])
    if reasons:
        fields["reason"] = "; ".join(reasons)
    if pendings:
        fields["pending"] = "; ".join(pendings)
    return fields


class Unevaluable(Exception):
    pass


def evaluate(expr: str, env: dict[str, float]) -> float:
    """A simple float expression: literals with f/F/d/D suffixes, identifiers bound in env (also `A.B`
    qualified names bound verbatim), (float) casts, + - * / and parentheses."""
    text = expr.strip()
    text = re.sub(r"\(\s*(?:float|double|int)\s*\)", "", text)

    def substitute(match: re.Match) -> str:
        token = match.group(0)
        if re.fullmatch(r"[-+]?(?:\d+\.\d*|\.\d+|\d+)(?:[eE][-+]?\d+)?[fFdD]?", token):
            return token.rstrip("fFdD")
        if token in env:
            return repr(float(env[token]))
        raise Unevaluable(token)

    try:
        text = re.sub(r"[A-Za-z_][A-Za-z0-9_.]*|(?:\d+\.\d*|\.\d+|\d+)(?:[eE][-+]?\d+)?[fFdD]?", substitute, text)
    except Unevaluable as exc:
        raise Unevaluable(f"unbound {exc}") from None
    if not re.fullmatch(r"[\d.\s+\-*/()eE]+", text) or not text.strip():
        raise Unevaluable(expr)
    try:
        return float(eval(text, {"__builtins__": {}}, {}))  # validated arithmetic only
    except Exception as exc:  # noqa: BLE001
        raise Unevaluable(f"{expr}: {exc}") from None


def same(a: float | None, b: float | None) -> bool:
    if a is None or b is None:
        return a is None and b is None
    return abs(float(a) - float(b)) <= TOLERANCE


def fmt(value: Any) -> str:
    if value is None:
        return "none"
    if isinstance(value, float):
        return f"{value:.6g}"
    return str(value)


# ------------------------------------------------------------------ reference side


def parse_registrations(proxy_path: Path) -> dict[str, dict[str, Any]]:
    text = proxy_path.read_text(encoding="utf-8", errors="replace")
    result: dict[str, dict[str, Any]] = {}
    for m in RE_REGISTRATION.finditer(text):
        args, _end = balanced_args(text, m.end() - 1)
        line = text.count("\n", 0, m.start()) + 1
        result[m.group(1)] = {"entity": m.group(1), "line": line, "render_class": m.group(2), "args": args}
    return result


def pose_stack_receivers(method: dict[str, Any]) -> list[str]:
    """The PoseStack parameter name(s) of a scanned port method, i.e. the receiver(s) whose `.scale(` is the
    scale site. A method with no PoseStack parameter falls back to the house-style name `poseStack`."""
    names: list[str] = []
    for m in RE_POSE_STACK_PARAM.finditer(method["params"]):
        if m.group(1) not in names:
            names.append(m.group(1))
    return names or ["poseStack"]


def scale_from_calls(body: str, env: dict[str, float], call: str = "GL11.glScalef",
                     receivers: list[str] | None = None) -> dict[str, Any]:
    """Uniform scale calls in a body: the unconditional one is the default; if-only ones are conditional.

    `call` is a literal call name (reference side: GL11.glScalef); `receivers`, when given, are the PoseStack
    receiver names whose `<receiver>.scale(` is the call (port side), so the scan follows the method's own
    parameter name instead of assuming `poseStack`."""
    sites = []
    index = 0
    if receivers:
        call_re = re.compile(r"\b(?:" + "|".join(re.escape(r) for r in receivers) + r")\.scale\s*\(")
    else:
        call_re = re.compile(re.escape(call) + r"\s*\(")
    while True:
        cm = call_re.search(body, index)
        if cm is None:
            break
        k = cm.start()
        args, index = balanced_args(body, cm.end() - 1)
        block = enclosing_block(body, k)
        # the locals as they stand at this site: a write after it never reaches it
        locals_env, locals_info = scan_locals(body, env, until=k)
        values: list[float | None] = []
        for arg in args:
            try:
                values.append(evaluate(arg, locals_env))
            except Unevaluable:
                values.append(None)
        uniform = len(values) == 3 and None not in values and abs(values[0] - values[1]) <= TOLERANCE and abs(values[0] - values[2]) <= TOLERANCE
        flip = len(values) == 3 and None not in values and values[0] < 0 and values[1] < 0 and values[2] > 0
        site = {"args": args, "values": values, "block": block, "uniform": uniform, "flip": flip}
        site.update(local_site_fields(locals_info, args))
        if site.get("pending"):
            # ruling 6 precedence: the pending write is the site's only obstacle when nothing else is provably wrong —
            # every other argument reads, the read values agree, three arguments, no local unbound for a reason;
            # otherwise the site stays UNPARSED as before and that failure outranks the pending
            known = [v for v in values if v is not None]
            hard_args = [a for a, v in zip(args, values)
                         if v is None and not (locals_info.get(local_name_of(a)) or {}).get("pending")]
            site["pending_only"] = (not site.get("reason") and not hard_args and len(args) == 3
                                    and all(abs(v - known[0]) <= TOLERANCE for v in known))
        sites.append(site)
    return {"sites": sites}


def scan_locals(body: str, env: dict[str, float], until: int | None = None) -> tuple[dict[str, float], dict[str, dict[str, Any]]]:
    """The float locals of a body as they stand at position `until` (a scale site; the whole body when None),
    evaluated on top of env, plus one record per local saying how its value was reached.

    A `float x = <expr>;` declaration binds x (a ternary initialiser `cond ? a : b` binds its default, the else
    branch b, and marks x conditional). Every later write to x before `until` (`x = <expr>;`, or a compound
    `x *= <expr>;` etc.) is read by the path it sits on, the default-branch rule resolve_default_scale applies to
    scale sites: a write directly in the method body, or in the final `else` of an if/else chain, is on the default
    path and replaces the value (the last such write wins); a write under an `if` / `else if` (braced or not, at
    any nesting) is off the default path: the value stays, x becomes conditional with the pre-branch expression as
    its default, and the branch value is not used (as a ternary's taken branch is not); a write inside any other
    block (switch, loop, lambda, bare block) or a default-path write (directly in the method body) whose expression
    cannot be evaluated leaves x unbound with a reason, so a site reading x is UNPARSED. Ruling 6 (2026-09-03): a
    write on any branch (under an if / else if, or in the final else) whose expression the scan cannot evaluate, of
    a local that was readable up to that write, is not provable — no branch is assumed: x is left unbound with
    `pending` set to the presentation detail, so a site reading x is NOT_PROVABLE (the entry is PENDING, never a
    PASS and never a failure). A ternary written on a branch must read on both arms (no arm is assumed), else it is
    pending; a ternary at the declaration or in a default-path write keeps its else-arm reading. Declarations and
    writes at or after `until` never reach the site.

    Record keys: expr (the expression x holds on the default path), form (None, 'ternary', 'if-reassignment',
    'if/else-reassignment' or 'ternary-reassignment'), default (the conditional default expression; None when
    x is unconditional), reason (None, or why x is unbound), pending (None, or the not-provable detail of the
    branch write that left x unbound), writes (the write statements read, in order)."""
    locals_env = dict(env)
    info: dict[str, dict[str, Any]] = {}
    events: list[tuple[int, str, str, str, str | None]] = []
    for m in RE_LOCAL.finditer(body):
        if until is None or m.start() < until:
            events.append((m.start(), "declare", m.group(1), m.group(2).strip(), None))
    for name in sorted({e[2] for e in events}):
        # write forms the scan does not evaluate (`x %= e;`, `x++;`, `++x;`, `x--;`, `--x;`): recorded as
        # unreadable writes so the local is left unbound with a reason instead of passing silently
        for m in re.finditer(r"(?<![\w.])" + re.escape(name) + r"\s*(?:%=[^;]*|\+\+|--)\s*;|(?:\+\+|--)\s*" + re.escape(name) + r"\s*;", body):
            if not RE_DECLARATION_PREFIX.search(body[:m.start()]) and (until is None or m.start() < until):
                events.append((m.start(), "write", name, m.group(0).strip(), "?"))
        for m in local_write_re(name).finditer(body):
            if RE_DECLARATION_PREFIX.search(body[:m.start()]):
                continue  # the declaration itself (or a redeclaration), read above
            if until is None or m.start() < until:
                events.append((m.start(), "write", name, m.group(2).strip(), m.group(1)))
    events.sort(key=lambda e: e[0])
    for position, kind, name, expr, op in events:
        if kind == "declare":
            record: dict[str, Any] = {"expr": expr, "form": None, "default": None, "reason": None, "pending": None, "writes": []}
            if "?" in expr and ":" in expr:
                expr = expr.rsplit(":", 1)[1].strip()
                record.update({"expr": expr, "form": "ternary", "default": expr})
            try:
                locals_env[name] = evaluate(expr, locals_env)
            except Unevaluable:
                locals_env.pop(name, None)
            info[name] = record
            continue
        record = info.get(name)
        if record is None or record["reason"] is not None or record["pending"] is not None:
            continue  # a field/parameter write before any declaration, or a local already left unbound / not provable
        stmt = re.sub(r"\s+", " ", body[position:body.find(";", position)].strip())  # `s = 2.0F`, quoted in notes
        record["writes"].append(stmt)
        if op == "?":
            record["reason"] = f"local {name} is written in a form the scan does not read (`{expr}`)"
            locals_env.pop(name, None)
            continue
        how = write_kind(block_path(body, position))
        if how == "other":
            record["reason"] = f"local {name} is written inside a block that is neither if nor else (`{stmt}`)"
            locals_env.pop(name, None)
            continue
        # every path reads the written expression (a ternary by its else branch): the default path binds it, a branch
        # only has to prove it readable — a branch write the scan cannot evaluate is not provable (ruling 6)
        ternary = "?" in expr and ":" in expr
        branch = expr.rsplit(":", 1)[1].strip() if ternary else expr
        bound = name in locals_env
        try:
            if ternary and how != "method":
                # ruling 6, ternary arms included: on a branch write (under an if / else if, or in the final else) no
                # arm is assumed, so the taken arm must read as well as the else arm read below; the else-arm reading
                # itself stays for a ternary at the declaration and in a method-body write (the earlier ruling)
                evaluate(expr.split("?", 1)[1].rsplit(":", 1)[0], locals_env)
            value = evaluate(branch, locals_env)
            if op is not None and (how != "if" or bound):
                # a compound write folds into the bound value; an unbound base is unreadable on the default path,
                # while under an if the base is not read at all (the value stays, as for any if-write)
                if not bound:
                    raise Unevaluable(f"{name} has no value to compound")
                value = compound(op, locals_env[name], value)
        except Unevaluable as exc:
            if how != "method" and bound:
                # ruling 6 (2026-09-03): "a write inside a non-evaluable branch is not provable; report it as pending
                # for presentation, never assume a branch" — under an if / else if and in the final else alike, the
                # local is left unbound and the site reading it is NOT_PROVABLE (the entry PENDING), the write quoted
                record["pending"] = (f"not provable: local {name} is written inside a branch with an expression the "
                                     f"scan cannot evaluate (`{stmt}`) — pending presentation")
            elif how == "if":
                # an if-write of a local nothing bound (an unreadable declaration): there is no value to keep or to
                # prove; it stays an if-reassignment and the site is UNPARSED through the declaration, as before
                record["form"] = "if-reassignment"
                record["default"] = record["expr"]
                continue
            else:
                record["reason"] = (f"local {name} is written on the default path with an expression that cannot be "
                                    f"evaluated (`{stmt}`: {exc})")
            locals_env.pop(name, None)
            continue
        if how == "if":
            # off the default path: the value stays and the local is conditional with its pre-branch default
            record["form"] = "if-reassignment"
            record["default"] = record["expr"]
            continue
        # the default path: directly in the method body, or the final else of an if/else chain
        locals_env[name] = value
        record["expr"] = f"({record['expr']}) {op} ({branch})" if op is not None else branch
        if how == "else":
            record["form"], record["default"] = "if/else-reassignment", record["expr"]
        elif ternary:
            record["form"], record["default"] = "ternary-reassignment", record["expr"]
        else:
            record["form"], record["default"] = None, None
    return locals_env, info


def strip_outer_parens(text: str) -> str:
    """`(x)` -> `x` for every pair of parentheses that encloses the whole expression; `(a) * (b)` is kept."""
    text = text.strip()
    while text.startswith("(") and text.endswith(")"):
        depth = 0
        for i, ch in enumerate(text):
            if ch == "(":
                depth += 1
            elif ch == ")":
                depth -= 1
                if depth == 0 and i < len(text) - 1:
                    return text  # the leading '(' closes before the end: not an enclosing pair
        text = text[1:-1].strip()
    return text


def scale_factor_of(expr: str, param: str) -> str | None:
    """The <factor> in `<param> * <factor>` or `<factor> * <param>` (enclosing parentheses stripped); None
    when the expression is not such a product. A bare `<param>` (pass-through) is the caller's case."""
    text = strip_outer_parens(expr)
    m = re.fullmatch(re.escape(param) + r"\s*\*\s*(.+)", text, re.S)
    if m is None:
        m = re.fullmatch(r"(.+?)\s*\*\s*" + re.escape(param), text, re.S)
    return strip_outer_parens(m.group(1)) if m else None


def scale_from_model_for_render(method: dict[str, Any], env: dict[str, float]) -> list[dict[str, Any]]:
    """The scale site(s) a GeckoLib `scaleModelForRender(float widthScale, float heightScale, PoseStack ...)`
    override applies through `super.scaleModelForRender(<w-expr>, <h-expr>, ...)`: GeckoLib's default then
    runs `poseStack.scale(w, h, w)` (after its entityRenderTranslations capture), so the factor multiplying
    both parameters is one uniform scale site, recorded in the same shape as a `<poseStack>.scale(` site.

    Width and height must carry the same factor expression (whitespace aside); a differing pair, or an
    argument that is not `<param> * <factor>` / `<factor> * <param>`, is a non-uniform site carrying its
    reason (resolve_default_scale reports it UNPARSED). A pass-through `super.scaleModelForRender(widthScale,
    heightScale, ...)` applies nothing and yields no site; so does an override that never reaches super.
    A ternary local factor resolves to its default branch through scan_locals, exactly as for scale()."""
    body = method["body"]
    params = [m.group(1) for m in RE_FLOAT_PARAM.finditer(method["params"])]
    sites: list[dict[str, Any]] = []
    index = 0
    while True:
        cm = RE_SUPER_SCALE_MODEL.search(body, index)
        if cm is None:
            break
        k = cm.start()
        args, index = balanced_args(body, cm.end() - 1)
        block = enclosing_block(body, k)
        # the locals as they stand at this super call: a write after it never reaches it
        locals_env, locals_info = scan_locals(body, env, until=k)
        call = "super.scaleModelForRender(" + ", ".join(a.strip() for a in args[:2]) + ", ...)"

        def unparsed(reason: str, shown: list[str]) -> dict[str, Any]:
            return {"args": shown, "values": [None] * 3, "block": block, "uniform": False, "flip": False,
                    "local": None, "ternary_default": None, "conditional_default": None, "conditional_form": None,
                    "call": "super.scaleModelForRender", "reason": reason}

        if len(params) < 2:
            sites.append(unparsed(f"scaleModelForRender override without two leading float parameters "
                                  f"(widthScale, heightScale) is not GeckoLib's signature: {call}", [call]))
            continue
        if len(args) < 2:
            sites.append(unparsed(f"scaleModelForRender: {call} has fewer than two arguments", [call]))
            continue
        wname, hname = params[0], params[1]
        wexpr, hexpr = args[0].strip(), args[1].strip()
        if strip_outer_parens(wexpr) == wname and strip_outer_parens(hexpr) == hname:
            continue  # pass-through: GeckoLib's own (withScale) factors only, nothing applied here
        wfactor = scale_factor_of(wexpr, wname)
        hfactor = scale_factor_of(hexpr, hname)
        if wfactor is None or hfactor is None:
            sites.append(unparsed(f"scaleModelForRender: cannot read the factor multiplying {wname} / {hname} "
                                  f"in {call} (expected `{wname} * <factor>, {hname} * <factor>`)", [wexpr, hexpr]))
            continue
        if re.sub(r"\s+", "", wfactor) != re.sub(r"\s+", "", hfactor):
            values: list[float | None] = []
            for factor in (wfactor, hfactor, wfactor):
                try:
                    values.append(evaluate(factor, locals_env))
                except Unevaluable:
                    values.append(None)
            sites.append({"args": [wfactor, hfactor, wfactor], "values": values, "block": block, "uniform": False,
                          "flip": False, "local": None, "ternary_default": None, "conditional_default": None,
                          "conditional_form": None, "call": "super.scaleModelForRender",
                          "factor_exprs": [wexpr, hexpr],
                          "reason": f"scaleModelForRender: {wname} factor {wfactor} and {hname} factor {hfactor} "
                                    f"differ in {call} (one uniform factor is required)"})
            continue
        fields = local_site_fields(locals_info, [wfactor])
        if fields.get("reason"):
            # the factor reads a local that a write left unbound: the site is UNPARSED with that write quoted
            sites.append(unparsed(f"scaleModelForRender: {fields['reason']}", [wexpr, hexpr]))
            continue
        if fields.get("pending"):
            # the factor reads a local a branch write left not provable (ruling 6): the site is NOT_PROVABLE and the
            # entry PENDING with that write quoted, never resolved by assuming a branch
            sites.append({"args": [wfactor] * 3, "values": [None] * 3, "block": block, "uniform": False, "flip": False,
                          "local": fields["local"], "ternary_default": None, "conditional_default": None,
                          "conditional_form": None, "call": "super.scaleModelForRender", "factor_exprs": [wexpr, hexpr],
                          "pending": f"{fields['pending']} (scaleModelForRender factor)", "pending_only": True})
            continue
        try:
            value: float | None = evaluate(wfactor, locals_env)
        except Unevaluable:
            value = None
        site = {"args": [wfactor] * 3, "values": [value] * 3, "block": block, "uniform": value is not None,
                "flip": False, "call": "super.scaleModelForRender", "factor_exprs": [wexpr, hexpr]}
        site.update(fields)
        sites.append(site)
    return sites


def resolve_default_scale(sites: list[dict[str, Any]]) -> tuple[str, float | None, str]:
    """(status, value, note): the single unconditional uniform scale, 1.0 when only conditional scales exist.

    Two or more unconditional sites are COMPOUND: matrix scales multiply, so equal values from distinct
    sites are NOT one application (scale() plus a render() wrapper both scaling by SCALE renders at
    SCALE squared). The product is returned as the value so the caller can print what actually renders.
    NOT_PROVABLE (value None, note the presentation detail): the single unconditional site reads a local a branch
    write left not provable (ruling 6) and nothing else stands in the way (pending_only, read at the site). A provable
    failure always outranks a pending: any other unreadable site wins as UNPARSED, and two or more unconditional sites
    win as COMPOUND (value None when a site's value is not provable); the pending write is then quoted after it.
    """
    unconditional = [s for s in sites if s["block"] in ("method", "else", "other") and not s["flip"]]
    if any(s["block"] == "other" for s in unconditional):
        return "UNPARSED", None, "a scale inside a block that is neither if nor else"
    if not unconditional:
        conditional = [s for s in sites if not s["flip"]]
        if conditional:
            return "PARSED", 1.0, f"only conditional scale(s) {[s['args'][0] for s in conditional]}; default 1.0"
        return "PARSED", 1.0, "no scale call"
    pendings = "; ".join(dict.fromkeys(s["pending"] for s in unconditional if s.get("pending")))
    if any(not s["uniform"] for s in unconditional):
        bad = [s for s in unconditional if not s["uniform"]]
        # ruling 6 precedence: a site is pending-only when the branch write is its only obstacle; any other unreadable
        # site is a failure, reported exactly as before with the pending write quoted after it
        hard = [s for s in bad if not s.get("pending_only")]
        if hard:
            if all(s.get("reason") for s in hard):
                # a scaleModelForRender site that could not be read carries its own reason
                note = "; ".join(s["reason"] for s in hard)
            else:
                note = f"scale argument not evaluable/uniform: {[s['args'] for s in hard]}"
            return "UNPARSED", None, note + (f"; {pendings}" if pendings else "")
        if len(unconditional) == 1:
            return "NOT_PROVABLE", None, pendings
    if len(unconditional) > 1:
        # two or more unconditional scales are a failure whatever a pending local holds (ruling 6 precedence): the
        # product is printed when every value is read, otherwise it is not provable and the pending write is quoted
        where = [(f"{s['method']}(): " if s.get("method") else "")
                 + (f"{s['args'][0]} = {fmt(float(s['values'][0]))}" if s["values"][0] is not None else f"{s['args'][0]} = not provable")
                 for s in unconditional]
        if any(s["values"][0] is None for s in unconditional):
            return "COMPOUND", None, (f"{len(unconditional)} unconditional scales compound to a product that is not provable: "
                                      f"{'; '.join(where)}; {pendings}")
        product = 1.0
        for s in unconditional:
            product *= float(s["values"][0])
        return "COMPOUND", product, f"{len(unconditional)} unconditional scales compound to {fmt(product)}: {'; '.join(where)}"
    first = unconditional[0]
    how = "unconditional " + first["args"][0]
    local = first.get("local") or {}
    if first.get("conditional_default"):
        # a conditional local: a ternary initialiser (as before) or an if / if-else / ternary write after the
        # declaration; the default branch is what renders, and the writes are quoted so the note shows the path
        how += f" ({first.get('conditional_form') or 'ternary'} default branch {first['conditional_default']}"
        if local.get("writes"):
            how += f"; writes: {'; '.join(local['writes'])}"
        how += ")"
    elif local.get("writes"):
        how += f" (reassigned: {'; '.join(local['writes'])})"
    if first.get("method") == "scaleModelForRender":
        # the GeckoLib post-capture slot is named so the PASS line says where the factor is applied
        how += ", scaleModelForRender"
    return "PARSED", float(first["values"][0]), how


def parse_reference_renderer(reference_dir: Path, registration: dict[str, Any]) -> dict[str, Any]:
    """Shadow and scale for one registration, from RenderX.java (and in-tree parents)."""
    render_class = registration["render_class"]
    report: dict[str, Any] = {"render_class": render_class, "shadow": {"status": "UNPARSED"}, "scale": {"status": "UNPARSED"}}
    path = reference_dir / f"{render_class}.java"
    if not path.is_file():
        reason = f"{render_class}.java is not in the reference tree (vanilla renderer)"
        report["shadow"]["reason"] = reason
        report["scale"]["reason"] = reason
        return report

    # class chain within the reference tree
    chain: list[dict[str, Any]] = []
    name = render_class
    while name and (reference_dir / f"{name}.java").is_file():
        text = strip_comments((reference_dir / f"{name}.java").read_text(encoding="utf-8", errors="replace"))
        cm = RE_CLASS.search(text)
        if not cm:
            report["shadow"]["reason"] = report["scale"]["reason"] = f"{name}.java: no class declaration parsed"
            return report
        chain.append({"name": name, "parent": cm.group(2), "text": text, "methods": methods(text)})
        name = cm.group(2)
    top_parent = chain[-1]["parent"]
    report["chain"] = [c["name"] for c in chain] + [top_parent]
    living = top_parent in VANILLA_LIVING_PASSTHROUGH
    plain = top_parent in VANILLA_PLAIN

    # ---- shadow: the first in-tree constructor up the chain
    ctor = None
    ctor_owner = None
    for cls in chain:
        found = [m for m in cls["methods"] if m["name"] == cls["name"]]
        if found:
            ctor, ctor_owner = found[0], cls
            break
    env: dict[str, float] = {}
    scale_field: float | None = None
    if ctor is not None:
        params = [p.strip().split()[-1] for p in ctor["params"].split(",") if p.strip()]
        for pname, arg in zip(params, registration["args"]):
            try:
                env[pname] = evaluate(arg, {})
            except Unevaluable:
                pass  # the model expression / null
        body = ctor["body"]
        sm = RE_REF_SHADOW_FIELD.search(body)
        k = body.find("super(")
        if sm:
            try:
                report["shadow"] = {"status": "PARSED", "value": evaluate(sm.group(1), env), "how": f"{ctor_owner['name']} ctor sets Render.shadowSize (field_76989_e) = {sm.group(1).strip()}"}
            except Unevaluable as exc:
                report["shadow"] = {"status": "UNPARSED", "reason": f"shadowSize expression {sm.group(1).strip()}: {exc}"}
        elif k >= 0:
            args, _ = balanced_args(body, k + len("super"))
            if len(args) >= 2 and ctor_owner["parent"] in VANILLA_LIVING_PASSTHROUGH:
                expr = args[-1]
                try:
                    report["shadow"] = {"status": "PARSED", "value": evaluate(expr, env), "how": f"{ctor_owner['name']} ctor super({', '.join(args)}) -> {ctor_owner['parent']} shadowSize = {expr}"}
                except Unevaluable as exc:
                    report["shadow"] = {"status": "UNPARSED", "reason": f"super shadow expression {expr}: {exc}"}
            elif ctor_owner["parent"] in VANILLA_PLAIN and len(args) == 0:
                report["shadow"] = {"status": "PARSED", "value": 0.0, "how": f"{ctor_owner['name']} ctor super() into Render: shadowSize stays 0"}
            else:
                report["shadow"] = {"status": "UNPARSED", "reason": f"super({', '.join(args)}) into {ctor_owner['parent']} (no source in the reference tree)"}
        else:
            if ctor_owner["parent"] in VANILLA_PLAIN:
                report["shadow"] = {"status": "PARSED", "value": 0.0, "how": f"{ctor_owner['name']} ctor never sets shadowSize: Render default 0"}
            else:
                report["shadow"] = {"status": "UNPARSED", "reason": f"{ctor_owner['name']} ctor has an implicit super() into vanilla {ctor_owner['parent']}"}
        fm = RE_REF_SCALE_FIELD.search(body)
        if fm and fm.group(1) in env:
            scale_field = env[fm.group(1)]
    else:
        if plain:
            report["shadow"] = {"status": "PARSED", "value": 0.0, "how": f"no constructor in {' -> '.join(report['chain'])}: Render default shadowSize 0"}
        else:
            report["shadow"] = {"status": "UNPARSED", "reason": f"no constructor in the reference tree; parent {top_parent} has no source here"}

    # ---- scale
    own = chain[0]
    own_methods = {m["name"]: m for m in own["methods"]}
    scale_env = {"this.scale": scale_field} if scale_field is not None else {}

    def do_render_path(cls: dict[str, Any]) -> tuple[str | None, bool, list[dict[str, Any]]]:
        """Follow func_76986_a (+ this.<delegate>) in one class: (early-return entity, reaches super, sites)."""
        bodies = [m["body"] for m in cls["methods"] if m["name"] == "func_76986_a"]
        if not bodies:
            return None, True, []  # not overridden: inherited path
        body = "\n".join(bodies)
        for dm in re.finditer(r"this\.(" + IDENT + r")\(", body):
            for m in cls["methods"]:
                if m["name"] == dm.group(1) and m["name"] != "func_76986_a":
                    body += "\n" + m["body"]
        early = RE_EARLY_RETURN.search(body)
        reaches_super = "super.func_76986_a(" in body
        return (early.group(1) if early else None), reaches_super, scale_from_calls(body, scale_env)["sites"]

    if living:
        early, reaches_super, sites = do_render_path(own)
        if not reaches_super:
            status, value, note = resolve_default_scale(sites)
            how = f"{own['name']} doRender never reaches RendererLivingEntity.doRender (preRenderCallback bypassed): {note}"
            if status == "PARSED":
                report["scale"] = {"status": "PARSED", "value": value, "how": how}
            else:
                report["scale"] = {"status": "UNPARSED", "reason": how}
        elif "func_77041_b" in own_methods:
            body = own_methods["func_77041_b"]["body"]
            if "this.preRenderScale(" in body and "preRenderScale" in own_methods:
                body = own_methods["preRenderScale"]["body"]
                how = "func_77041_b -> preRenderScale"
            else:
                how = "func_77041_b"
            status, value, note = resolve_default_scale(scale_from_calls(body, scale_env)["sites"])
            if status == "PARSED":
                report["scale"] = {"status": "PARSED", "value": value, "how": f"{own['name']}.{how}: {note}"}
            else:
                report["scale"] = {"status": "UNPARSED", "reason": f"{own['name']}.{how}: {note}"}
        else:
            report["scale"] = {"status": "PARSED", "value": 1.0, "how": f"{own['name']} does not override func_77041_b (preRenderCallback): vanilla no-op, 1.0"}
    elif plain:
        sites_all: list[dict[str, Any]] = []
        early_hit = None
        how_parts = []
        for cls in chain:
            early, reaches_super, sites = do_render_path(cls)
            if early == registration["entity"]:
                early_hit = cls["name"]
                break
            sites_all.extend(sites)
            how_parts.append(cls["name"])
            if not reaches_super:
                break
        if early_hit:
            report["scale"] = {"status": "PARSED", "value": None, "how": f"{early_hit}.doRender returns before drawing for {registration['entity']}: nothing drawn"}
        else:
            status, value, note = resolve_default_scale(sites_all)
            if status == "PARSED" and not sites_all:
                report["scale"] = {"status": "UNPARSED", "reason": f"no glScalef found along {' -> '.join(how_parts)}.doRender"}
            elif status == "PARSED":
                report["scale"] = {"status": "PARSED", "value": value, "how": f"{' -> '.join(how_parts)}.doRender: {note}"}
            else:
                report["scale"] = {"status": "UNPARSED", "reason": f"{' -> '.join(how_parts)}.doRender: {note}"}
    else:
        report["scale"] = {"status": "UNPARSED", "reason": f"parent {top_parent} has no source in the reference tree"}
    return report


# ------------------------------------------------------------------ port side


def read_constants(text: str) -> dict[str, dict[str, Any]]:
    constants: dict[str, dict[str, Any]] = {}
    env: dict[str, float] = {}
    for m in RE_CONSTANT.finditer(text):
        expr = m.group(2).strip()
        try:
            value = evaluate(expr, env)
            env[m.group(1)] = value
        except Unevaluable:
            value = None
        constants[m.group(1)] = {"expr": expr, "value": value}
    return constants


def shadow_rewrites(text: str, ms: list[dict[str, Any]], ctor: dict[str, Any] | None, where: str) -> list[str]:
    """Every way a class (comments stripped) can make the rendered shadow differ from what its constructor
    pinned: a getShadowRadius(...) override (or call), a `shadowRadius =` write anywhere outside the
    constructor body (a render() wrapper, a field initialiser, another method), or more than one write
    inside it. Each hit is one reason string; an empty list means the constructor's value is the shadow."""
    reasons: list[str] = []

    def line_of(index: int) -> int:
        return text.count("\n", 0, index) + 1

    def method_at(index: int) -> str:
        inner = [m for m in ms if m["start"] <= index < m["end"]]
        return (min(inner, key=lambda m: m["end"] - m["start"])["name"] + "()") if inner else "class body"

    for m in RE_GET_SHADOW.finditer(text):
        reasons.append(f"getShadowRadius( in {where} at line {line_of(m.start())} ({method_at(m.start())}) overrides the constructor shadow")
    # every write inside the constructor is one pin site: a 3-argument super(context, model, <shadow>) is
    # one, each `[this.|super.]shadowRadius =` body write another; more than one means the later write
    # rewrites what super pinned (a bare or `super.`-qualified `shadowRadius = X;` after super(..., SHADOW)
    # renders X, not SHADOW)
    in_ctor: list[str] = []
    if ctor is not None:
        super_args = super_call_args(ctor["body"])
        if super_args is not None and len(super_args) >= 3:
            in_ctor.append(f"super(..., {super_args[-1]})")
    for m in RE_SHADOW_ASSIGN.finditer(text):
        if ctor is not None and ctor["start"] <= m.start() < ctor["end"]:
            in_ctor.append(f"{m.group(0).strip()} at line {line_of(m.start())}")
            continue
        reasons.append(f"shadowRadius assigned outside the constructor in {where} at line {line_of(m.start())} ({method_at(m.start())})")
    if len(in_ctor) > 1:
        reasons.append(f"shadowRadius written {len(in_ctor)} times in the {where} constructor ({'; '.join(in_ctor)}): the later write rewrites the pin")
    return reasons


def super_call_args(ctor_body: str) -> list[str] | None:
    """The argument list of the constructor's `super(...)` call, None when it has none (a this(...) delegate)."""
    k = ctor_body.find("super(")
    if k < 0:
        return None
    args, _ = balanced_args(ctor_body, k + len("super"))
    return args


def parent_chain(text: str, directory: Path) -> list[tuple[str, str]]:
    """(name, comment-stripped text) of every in-tree ancestor of the class in text, nearest first."""
    chain: list[tuple[str, str]] = []
    seen: set[str] = set()
    cm = RE_CLASS.search(text)
    parent = cm.group(2) if cm else None
    while parent and parent not in seen and (directory / f"{parent}.java").is_file():
        seen.add(parent)
        ptext = strip_comments((directory / f"{parent}.java").read_text(encoding="utf-8", errors="replace"))
        chain.append((parent, ptext))
        pm = RE_CLASS.search(ptext)
        parent = pm.group(2) if pm else None
    return chain


def inherited_shadow_rewrites(text: str, directory: Path) -> list[str]:
    reasons: list[str] = []
    for name, ptext in parent_chain(text, directory):
        pms = methods(ptext)
        pctor = next((m for m in pms if m["name"] == name), None)
        # a parent's own constructor write is its pin, not a rewrite; everything else in it is
        reasons.extend(shadow_rewrites(ptext, pms, pctor, f"parent {name}.java"))
    return reasons


def parse_port_renderer(path: Path) -> dict[str, Any]:
    raw = path.read_text(encoding="utf-8", errors="replace")
    text = strip_comments(raw)
    report: dict[str, Any] = {"file": path.name}
    cm = RE_CLASS.search(text)
    report["extends"] = cm.group(2) if cm else None
    constants = read_constants(text)
    report["constants"] = {k: v["value"] for k, v in constants.items()}
    report["constant_exprs"] = {k: v["expr"] for k, v in constants.items()}
    env = {k: v["value"] for k, v in constants.items() if v["value"] is not None}
    ms = methods(text)
    class_name = cm.group(1) if cm else path.stem
    ctor = next((m for m in ms if m["name"] == class_name), None)

    # shadow
    shadow: dict[str, Any] = {"status": "UNPARSED"}
    if ctor is not None:
        body = ctor["body"]
        args = super_call_args(body)
        # every `[this.|super.]shadowRadius = <expr>;` in the constructor, in order: the last write is the rendered
        # shadow (Java field semantics); a second write after super(..., <shadow>) is reported by
        # shadow_rewrites below and makes the pin DIVERGE regardless of the value read here
        writes = list(RE_SHADOW_FIELD.finditer(body))
        if writes:
            sm = writes[-1]
            head = sm.group(0).lstrip()
            site = "super.shadowRadius =" if head.startswith("super.") else ("this.shadowRadius =" if head.startswith("this.") else "shadowRadius =")
            expr = sm.group(1).strip()
            try:
                shadow = {"status": "PARSED", "value": evaluate(expr, env), "expr": expr, "site": site}
            except Unevaluable:
                shadow = {"status": "UNPARSED", "expr": expr, "site": site}
        elif any(ctor["start"] <= m.start() < ctor["end"] for m in RE_SHADOW_ASSIGN.finditer(text)):
            # a compound write (`shadowRadius *= 2;`) is a write whose value cannot be read
            shadow = {"status": "UNPARSED", "reason": "constructor writes shadowRadius with a compound assignment"}
        elif args is not None:
            if len(args) >= 3:
                expr = args[-1]
                try:
                    shadow = {"status": "PARSED", "value": evaluate(expr, env), "expr": expr, "site": "super(context, model, ...)"}
                except Unevaluable:
                    shadow = {"status": "UNPARSED", "expr": expr, "site": "super(context, model, ...)"}
            else:
                shadow = {"status": "PARSED", "value": 0.0, "expr": "(EntityRenderer default)", "site": f"super({', '.join(args)}) leaves shadowRadius 0"}
        else:
            shadow = {"status": "UNPARSED", "reason": "constructor has neither super(context, model, <shadow>) nor this.shadowRadius = (a this(...) delegating constructor?)"}
    else:
        shadow = {"status": "UNPARSED", "reason": "no constructor parsed"}
    # the constructor value is the shadow only when nothing else in the class (or an in-tree parent)
    # rewrites it: a getShadowRadius override or a shadowRadius write elsewhere makes it UNPARSED
    rewrites = shadow_rewrites(text, ms, ctor, path.name) + inherited_shadow_rewrites(text, path.parent)
    if rewrites:
        shadow = {"status": "UNPARSED", "expr": shadow.get("expr"), "site": shadow.get("site"),
                  "constructor_value": shadow.get("value"), "reason": "; ".join(rewrites)}
    shadow["uses_constant"] = (shadow.get("expr") or "").strip() == "SHADOW"
    report["shadow"] = shadow

    # scale sites: `<poseStack>.scale(` in scale()/render()/preRender()/applyScale()/scaleModelForRender(), plus
    # the factor a scaleModelForRender override hands to super (GeckoLib's post-capture scale slot)
    sites = []
    for m in ms:
        if m["name"] not in SCALE_METHODS:
            continue
        # the scale site is `<PoseStack parameter>.scale(`, whatever the method names that parameter
        found = scale_from_calls(m["body"], env, receivers=pose_stack_receivers(m))["sites"]
        if m["name"] == "scaleModelForRender":
            found.extend(scale_from_model_for_render(m, env))
        for s in found:
            s["method"] = m["name"]
            arg = s["args"][0].strip()
            local = s.get("local")
            if arg == "SCALE":
                s["uses_constant"] = True
            elif s.get("conditional_default") is not None:
                # a conditional local (`float x = cond ? a : b;`, or `float x = ...; if (cond) x = ...;`): the
                # default branch must itself be SCALE
                s["uses_constant"] = s["conditional_default"].strip() == "SCALE"
            elif local is not None:
                # a plain or reassigned local: the expression it holds at the site (the last default-path
                # write's, not the declaration's, when it was written) must be SCALE
                s["uses_constant"] = (local.get("expr") or "").strip() == "SCALE"
            else:
                s["uses_constant"] = bool(re.search(r"\bfloat\s+" + re.escape(arg) + r"\s*=\s*SCALE\s*;", m["body"]))
            sites.append(s)
    report["scale_sites"] = sites
    status, value, note = resolve_default_scale(sites)
    scale_override = any(m["name"] == "scale" for m in ms)
    report["scale"] = {"status": status, "value": value, "note": note, "has_scale_override": scale_override,
                       "site": next((s["method"] for s in sites if s["block"] in ("method", "else") and not s["flip"]), None)}
    return report


def parse_candidate(path: Path, client_dir: Path, renderer_stem: str) -> dict[str, Any]:
    """renderer_stem is the port renderer's file stem (e.g. BeaverRenderer): only ITS `.SHADOW` / `.SCALE`
    count as the constant being used — another renderer's equal-valued constant is a bypass, not a pin."""
    text = strip_comments(path.read_text(encoding="utf-8", errors="replace"))
    report: dict[str, Any] = {"file": path.name, "constant_owner": renderer_stem}
    k = text.find("new GeoReplacementDescriptor<")
    if k < 0:
        report["shadow"] = {"status": "UNPARSED", "reason": "no GeoReplacementDescriptor construction", "uses_constant": False}
        report["scale"] = {"status": "UNPARSED", "value": None, "note": "no GeoReplacementDescriptor construction"}
        report["scale_sites"] = []
        return report
    open_index = text.find("(", k)
    args, _ = balanced_args(text, open_index)
    env: dict[str, float] = {}
    referenced: dict[str, dict[str, Any]] = {}
    for qm in re.finditer(r"\b(" + IDENT + r"Renderer)\.(SHADOW|SCALE)\b", text):
        renderer_path = client_dir / f"{qm.group(1)}.java"
        if renderer_path.is_file():
            if qm.group(1) not in referenced:
                referenced[qm.group(1)] = read_constants(strip_comments(renderer_path.read_text(encoding="utf-8", errors="replace")))
            const = referenced[qm.group(1)].get(qm.group(2))
            if const and const["value"] is not None:
                env[f"{qm.group(1)}.{qm.group(2)}"] = const["value"]
    expr = args[5].strip() if len(args) >= 6 else ""
    try:
        report["shadow"] = {"status": "PARSED", "value": evaluate(expr, env), "expr": expr, "uses_constant": expr == f"{renderer_stem}.SHADOW"}
    except Unevaluable:
        report["shadow"] = {"status": "UNPARSED", "expr": expr, "uses_constant": False}
    # the descriptor's shadowRadius() is the shadow only when nothing in the candidate (or an in-tree
    # parent) overrides getShadowRadius or writes shadowRadius (the candidate has no constructor pin)
    rewrites = shadow_rewrites(text, methods(text), None, path.name) + inherited_shadow_rewrites(text, path.parent)
    if rewrites:
        report["shadow"] = {"status": "UNPARSED", "expr": expr, "uses_constant": False, "reason": "; ".join(rewrites)}
    sites = []
    for m in methods(text):
        if m["name"] != "applyScale":
            continue
        for s in scale_from_calls(m["body"], env, receivers=pose_stack_receivers(m))["sites"]:
            s["method"] = "applyScale"
            s["uses_constant"] = s["args"][0].strip() == f"{renderer_stem}.SCALE"
            sites.append(s)
    status, value, note = resolve_default_scale(sites)
    report["scale_sites"] = sites
    report["scale"] = {"status": status, "value": value, "note": note if sites else "no applyScale override (descriptor no-op)"}
    return report


# ------------------------------------------------------------------ the leg


def check_pin(entry: dict[str, Any], port: dict[str, Any], candidate: dict[str, Any] | None) -> list[str]:
    """Reasons the pin fails; empty when it holds."""
    problems: list[str] = []
    expected_shadow = entry["expected_shadow"]
    expected_scale = entry["expected_scale"]
    shadow = port["shadow"]
    if shadow.get("status") != "PARSED":
        problems.append(f"port shadow not pinned by the constructor: {shadow.get('reason') or shadow.get('expr')}")
    elif not same(shadow["value"], expected_shadow):
        problems.append(f"shadow expected {fmt(expected_shadow)}, found {fmt(shadow['value'])} ({shadow.get('expr')})")
    elif "SHADOW" in port["constants"] and not shadow["uses_constant"]:
        problems.append(f"SHADOW constant exists but the constructor passes {shadow.get('expr')}")
    if "SHADOW" in port["constants"] and port["constants"]["SHADOW"] is not None and not same(port["constants"]["SHADOW"], expected_shadow):
        problems.append(f"SHADOW = {port['constant_exprs']['SHADOW']} = {fmt(port['constants']['SHADOW'])} != expected {fmt(expected_shadow)}")
    scale = port["scale"]
    default_sites = [s for s in port.get("scale_sites", []) if s["block"] in ("method", "else") and not s["flip"]]
    if expected_scale == "dynamic":
        # ENT-S-092 batch 2: both sides scale by a per-entity getter (Crab getCrabScale, PitchBlack's size
        # getter); the scale axis is pinned by the transcription, not by a constant. The manifest must say so
        # (scale_dynamic_note) and the port must not apply a fixed SCALE constant unconditionally.
        if not entry.get("scale_dynamic_note"):
            problems.append("expected_scale is dynamic but the manifest carries no scale_dynamic_note")
        if "SCALE" in port["constants"] and default_sites and any(s.get("uses_constant") for s in default_sites):
            problems.append("expected a per-entity scale but the port applies a fixed SCALE constant unconditionally")
        return problems + check_candidate(entry, port, candidate, shadow_only=True)
    if scale["status"] == "COMPOUND":
        rendered = f"rendering at {fmt(scale['value'])}" if scale["value"] is not None else "rendering at a product that is not provable"
        problems.append(f"scale applied more than once, {rendered} ({scale['note']}); a pin needs exactly one unconditional uniform scale")
    elif scale["status"] == "NOT_PROVABLE" and expected_scale is not None:
        # ruling 6: the default-path value rests on a branch write the scan cannot evaluate; neither a failure nor a
        # pass — main reports the entry PENDING with the site's detail (not_provable_details)
        pass
    elif scale["status"] != "PARSED":
        problems.append(f"port scale unparseable: {scale['note']}")
    elif expected_scale is None:
        problems.append("expected scale is per-entity (none); a pin cannot hold it")
    elif same(expected_scale, 1.0):
        if not same(scale["value"], 1.0):
            problems.append(f"scale expected 1.0, found {fmt(scale['value'])} in {scale['site']}()")
    else:
        if not same(scale["value"], expected_scale):
            problems.append(f"scale expected {fmt(expected_scale)}, found {fmt(scale['value'])} ({scale['note']})")
        elif scale["site"] not in ("scale", "render", "scaleModelForRender"):
            problems.append(f"scale applied in {scale['site']}(), expected the scale() override (or render wrapper, or the GeckoLib scaleModelForRender override)")
    # the constant-must-be-used rule, mirrored from SHADOW: when SCALE exists it must be what is applied
    if scale["status"] == "PARSED" and "SCALE" in port["constants"]:
        if not default_sites:
            if not (same(expected_scale, 1.0) and same(port["constants"]["SCALE"], 1.0)):
                problems.append("SCALE constant exists but no unconditional scale site (poseStack.scale, or a scaleModelForRender factor) applies it")
        else:
            bypassed = [s for s in default_sites if not s.get("uses_constant")]
            if bypassed:
                problems.append("SCALE constant exists but " + "; ".join(bypass_word(s) for s in bypassed))
    if "SCALE" in port["constants"] and port["constants"]["SCALE"] is not None and expected_scale is not None and not same(port["constants"]["SCALE"], expected_scale):
        problems.append(f"SCALE = {port['constant_exprs']['SCALE']} = {fmt(port['constants']['SCALE'])} != expected {fmt(expected_scale)}")
    return problems + check_candidate(entry, port, candidate, shadow_only=False)


def bypass_word(site: dict[str, Any]) -> str:
    """`scale() scales by x`, naming what a written local x holds on the default path and the writes read."""
    text = f"{site['method']}() scales by {site['args'][0].strip()}"
    local = site.get("local") or {}
    if local.get("writes"):
        text += f" (holds {local.get('expr')} on the default path; writes: {'; '.join(local['writes'])})"
    return text


def check_candidate(entry: dict[str, Any], port: dict[str, Any], candidate: dict[str, Any] | None, shadow_only: bool) -> list[str]:
    """The GeckoLib descriptor half of a pin: shadow always; scale unless the axis is dynamic."""
    problems: list[str] = []
    if candidate is None:
        return problems
    expected_shadow = entry["expected_shadow"]
    expected_scale = entry["expected_scale"]
    cs = candidate["shadow"]
    if cs.get("status") != "PARSED":
        problems.append(f"candidate shadow not pinned by the descriptor: {cs.get('reason') or cs.get('expr')}")
    elif not same(cs["value"], expected_shadow):
        problems.append(f"candidate shadow expected {fmt(expected_shadow)}, found {fmt(cs['value'])} ({cs.get('expr')})")
    elif "SHADOW" in port["constants"] and not cs["uses_constant"]:
        problems.append(f"candidate passes {cs.get('expr')} although {port['file'][:-5]}.SHADOW exists")
    if shadow_only:
        return problems
    cc = candidate["scale"]
    if cc["status"] == "NOT_PROVABLE" and expected_scale is not None:
        pass  # ruling 6: reported PENDING by main (not_provable_details), exactly as for the port scale
    elif cc["status"] != "PARSED":
        problems.append(f"candidate scale unparseable: {cc['note']}")
    elif expected_scale is not None and not same(cc["value"], expected_scale):
        problems.append(f"candidate scale expected {fmt(expected_scale)}, found {fmt(cc['value'])} ({cc['note']})")
    elif expected_scale is not None and not same(expected_scale, 1.0) and not any(s.get("uses_constant") for s in candidate["scale_sites"]):
        problems.append(f"candidate applyScale does not scale by {port['file'][:-5]}.SCALE")
    return problems


def not_provable_details(port: dict[str, Any] | None, candidate: dict[str, Any] | None) -> list[str]:
    """Ruling 6 (2026-09-03): the presentation detail of every scale axis (port renderer, then candidate) whose
    default-path value rests on a branch write the scan cannot evaluate. Non-empty makes the entry PENDING unless
    something else fails it: never a PASS by assuming a branch, never a failure for what is not proven."""
    details: list[str] = []
    if port is not None and port["scale"].get("status") == "NOT_PROVABLE":
        details.append(port["scale"]["note"])
    if candidate is not None and candidate["scale"].get("status") == "NOT_PROVABLE":
        details.append(f"{candidate['scale']['note']} (candidate {candidate['file']})")
    return details


def port_summary(port: dict[str, Any] | None, candidate: dict[str, Any] | None) -> str:
    if port is None:
        return "port: none"
    parts = [f"port {port['file']} shadow {shadow_word(port['shadow'])}"]
    scale = port["scale"]
    parts.append(f"scale {scale_word(scale)} ({scale['note']})")
    if candidate is not None:
        parts.append(f"candidate {candidate['file']} shadow {shadow_word(candidate['shadow'])} scale "
                     f"{scale_word(candidate['scale'])} ({candidate['scale']['note']})")
    return "; ".join(parts)


def shadow_word(shadow: dict[str, Any]) -> str:
    if shadow.get("status") == "PARSED":
        return f"{fmt(shadow.get('value'))} ({shadow.get('expr', shadow.get('reason', '?'))})"
    return f"UNPARSED ({shadow.get('reason') or shadow.get('expr') or '?'})"


def scale_word(scale: dict[str, Any]) -> str:
    if scale["status"] == "PARSED":
        return fmt(scale["value"])
    if scale["status"] == "COMPOUND":
        return f"COMPOUND {fmt(scale['value'])}" if scale["value"] is not None else "COMPOUND (product not provable)"
    if scale["status"] == "NOT_PROVABLE":
        return "NOT_PROVABLE"
    return "UNPARSED"


def main() -> int:
    # the gate runs this under a cp1252 console on Windows; a stray non-Latin character in a bad manifest value
    # must not turn a MANIFEST_DRIFT line into an encoding crash that loses the report
    for stream in (sys.stdout, sys.stderr):
        if hasattr(stream, "reconfigure"):
            stream.reconfigure(errors="backslashreplace")
    parser = argparse.ArgumentParser(description=__doc__.split("\n\n")[0])
    default_manifest = Path(__file__).resolve().parent / "reference_renderer_pins.json"
    parser.add_argument("--manifest", type=Path, default=default_manifest)
    parser.add_argument("--repository-root", type=Path, default=None, help="defaults to the manifest's grandparent (tools/<manifest> layout)")
    parser.add_argument("--reference-dir", type=Path, default=None, help="defaults to <root>/reference_1_7_10_source/sources/danger/orespawn")
    parser.add_argument("--json", type=Path, default=None, help="write the machine-readable report here")
    parser.add_argument("--verbose", action="store_true", help="also print one line per PASS entry")
    args = parser.parse_args()

    manifest = json.loads(args.manifest.read_text(encoding="utf-8"))
    root = (args.repository_root or args.manifest.resolve().parent.parent).resolve()
    reference_dir = (args.reference_dir or root / "reference_1_7_10_source" / "sources" / "danger" / "orespawn").resolve()
    client_dir = root / "src" / "main" / "java" / "danger" / "orespawn" / "entity" / "client"
    proxy = reference_dir / "ClientProxyOreSpawn.java"

    registrations = parse_registrations(proxy)
    entries = manifest["entries"]
    results: list[dict[str, Any]] = []
    counts: dict[str, int] = {}
    unparsed_axes = 0
    drift_global: list[str] = []
    manifest_entities = [e["entity"] for e in entries if isinstance(e, dict) and "entity" in e]
    if len(manifest_entities) != len(set(manifest_entities)):
        drift_global.append("duplicate entities in the manifest")
    missing = sorted(set(registrations) - set(manifest_entities))
    extra = sorted(set(manifest_entities) - set(registrations))
    if missing:
        drift_global.append(f"registrations without a manifest entry: {missing}")
    if extra:
        drift_global.append(f"manifest entries without a registration: {extra}")

    def bump(status: str) -> None:
        counts[status] = counts.get(status, 0) + 1

    for index, entry in enumerate(entries):
        # a malformed entry is a manifest failure line, never a KeyError crash without a report
        missing_keys = [k for k in REQUIRED_ENTRY_KEYS if not isinstance(entry, dict) or k not in entry]
        if missing_keys:
            entity = entry.get("entity", f"<entry #{index}>") if isinstance(entry, dict) else f"<entry #{index}>"
            detail = f"manifest entry missing required key(s) {missing_keys}"
            results.append({"entity": entity, "manifest_status": entry.get("status") if isinstance(entry, dict) else None,
                            "status": "MANIFEST_DRIFT", "detail": detail, "notes": []})
            bump("MANIFEST_DRIFT")
            print(f"REFERENCE RENDERERS MANIFEST_DRIFT: {entity}: {detail}")
            continue
        entity = entry["entity"]
        # expected values are typed: a number, null (per-entity, cannot be pinned) or, for the scale axis only,
        # the exact string "dynamic" (both sides scale by an entity getter; ENT-S-092 batch 2). Anything else
        # is a manifest failure line, never a ValueError crash without a report.
        type_problems = []
        for axis in ("shadow", "scale"):
            value = entry[f"expected_{axis}"]
            numeric = isinstance(value, (int, float)) and not isinstance(value, bool)
            if not (value is None or numeric or (axis == "scale" and value == "dynamic")):
                allowed = 'a number, null or "dynamic"' if axis == "scale" else "a number or null"
                type_problems.append(f"expected_{axis} must be {allowed}; got {value!r}")
        if type_problems:
            detail = "; ".join(type_problems)
            results.append({"entity": entity, "manifest_status": entry["status"], "status": "MANIFEST_DRIFT", "detail": detail, "notes": []})
            bump("MANIFEST_DRIFT")
            print(f"REFERENCE RENDERERS MANIFEST_DRIFT: {entity}: {detail}")
            continue
        record: dict[str, Any] = {"entity": entity, "manifest_status": entry["status"], "expected": {"shadow": entry["expected_shadow"], "scale": entry["expected_scale"]}}
        drift: list[str] = []
        notes: list[str] = []
        registration = registrations.get(entity)
        if registration is None:
            drift.append("no registration in ClientProxyOreSpawn.java")
            reference = None
        else:
            if registration["line"] != entry["ref_line"]:
                drift.append(f"ref_line {entry['ref_line']} but the registration is at line {registration['line']}")
            if registration["render_class"] != entry["ref_render_class"]:
                drift.append(f"ref_render_class {entry['ref_render_class']} but the registration uses {registration['render_class']}")
            reference = parse_reference_renderer(reference_dir, registration)
            for axis in ("shadow", "scale"):
                parsed = reference[axis]
                expected = entry[f"expected_{axis}"]
                if parsed["status"] == "PARSED":
                    if expected == "dynamic":
                        drift.append(f"{axis}: reference parses to {fmt(parsed['value'])} ({parsed['how']}) but the manifest rules dynamic")
                    elif not same(parsed["value"], expected):
                        drift.append(f"{axis}: reference parses to {fmt(parsed['value'])} ({parsed['how']}) but the manifest expects {fmt(expected)}")
                else:
                    unparsed_axes += 1
                    notes.append(f"UNPARSED_REFERENCE {axis}: {parsed.get('reason')}; manifest ruling {fmt(expected)} accepted")
        record["reference"] = reference

        port = candidate = None
        port_path = entry["port_renderer"]
        if port_path.startswith("src/"):
            file = root / port_path
            if file.is_file():
                try:
                    port = parse_port_renderer(file)
                except Exception as exc:  # noqa: BLE001
                    port = {"file": file.name, "shadow": {"status": "UNPARSED", "reason": str(exc)}, "scale": {"status": "UNPARSED", "value": None, "note": str(exc), "site": None}, "constants": {}, "constant_exprs": {}, "scale_sites": []}
            else:
                notes.append(f"port renderer {port_path} is missing")
        # a named candidate is part of the pin: missing/unparseable is a failure for pin and mod entries
        # (mirrors the missing port renderer), never a dropped axis
        cand_path = entry.get("port_candidate", "none")
        candidate_problem: str | None = None
        if cand_path != "none":
            file = root / cand_path
            if not cand_path.startswith("src/") or not file.is_file():
                candidate_problem = f"candidate {cand_path} is missing"
            else:
                try:
                    # the candidate must use THIS port renderer's constants (its file stem, e.g. BeaverRenderer)
                    candidate = parse_candidate(file, client_dir, Path(port_path).stem)
                except Exception as exc:  # noqa: BLE001
                    candidate_problem = f"candidate {cand_path} not parseable: {exc}"
                    candidate = {"file": file.name, "shadow": {"status": "UNPARSED", "reason": str(exc)},
                                 "scale": {"status": "UNPARSED", "value": None, "note": str(exc)}, "scale_sites": []}
        record["port"] = port
        record["candidate"] = candidate
        record["candidate_problem"] = candidate_problem

        status = entry["status"]
        detail = ""
        if drift:
            status = "MANIFEST_DRIFT"
            detail = "; ".join(drift)
        elif entry["status"] == "not_applicable":
            status = "NOT_APPLICABLE"
            detail = entry.get("reason", "")
            if candidate_problem:
                notes.append(candidate_problem)
        elif entry["status"] == "pending":
            status = "PENDING"
            detail = f"expected shadow {fmt(entry['expected_shadow'])} scale {fmt(entry['expected_scale'])}; {port_summary(port, candidate)} [{entry.get('note', '')}]"
            if candidate_problem:
                notes.append(candidate_problem)
                detail += f" ({candidate_problem})"
            if not entry.get("truth_verified", True):
                detail += " (truth row provisional)"
        elif entry["status"] == "mod":
            mod = entry.get("mod") or {}
            problems = []
            if port is None:
                problems.append("port renderer missing")
            else:
                if not same(port["shadow"].get("value"), mod.get("shadow")):
                    problems.append(f"shadow recorded {fmt(mod.get('shadow'))}, found {fmt(port['shadow'].get('value'))}")
                # a NOT_PROVABLE scale (ruling 6) is neither the recorded value nor a divergence: PENDING below
                if port["scale"].get("status") != "NOT_PROVABLE" and not same(port["scale"].get("value"), mod.get("scale")):
                    problems.append(f"scale recorded {fmt(mod.get('scale'))}, found {fmt(port['scale'].get('value'))}")
            if candidate_problem:
                problems.append(candidate_problem)
            elif candidate is not None:
                if not same(candidate["shadow"].get("value"), mod.get("shadow")):
                    problems.append(f"candidate shadow recorded {fmt(mod.get('shadow'))}, found {fmt(candidate['shadow'].get('value'))}")
                if candidate["scale"].get("status") != "NOT_PROVABLE" and not same(candidate["scale"].get("value"), mod.get("scale")):
                    problems.append(f"candidate scale recorded {fmt(mod.get('scale'))}, found {fmt(candidate['scale'].get('value'))}")
            pending = not_provable_details(port, candidate)
            if problems:
                status = "DIVERGES"
                detail = f"{mod.get('record')}: " + "; ".join(problems)
            elif pending:
                # ruling 6: a scale-site local written inside a branch the scan cannot evaluate is not provable;
                # presented as PENDING, never resolved by assuming a branch
                status = "PENDING"
                detail = ("; ".join(pending) + f" | {mod.get('record')}: recorded shadow {fmt(mod.get('shadow'))} scale "
                          f"{fmt(mod.get('scale'))}; {port_summary(port, candidate)}")
            else:
                status = "MOD"
                detail = f"{mod.get('record')}: keeps shadow {fmt(mod.get('shadow'))} scale {fmt(mod.get('scale'))}"
        elif entry["status"] == "pin":
            if port is None:
                status = "DIVERGES"
                detail = f"port renderer {port_path} not parseable/missing"
                if candidate_problem:
                    detail += f"; {candidate_problem}"
            else:
                problems = check_pin(entry, port, candidate)
                if candidate_problem:
                    problems.append(candidate_problem)
                pending = not_provable_details(port, candidate)
                if problems:
                    status = "DIVERGES"
                    detail = "; ".join(problems) + f" | {port_summary(port, candidate)}"
                elif pending:
                    # ruling 6: a scale-site local written inside a branch the scan cannot evaluate is not provable;
                    # presented as PENDING (listed per entry, counted, in the JSON), never a PASS by assuming a branch
                    status = "PENDING"
                    detail = "; ".join(pending) + f" | {port_summary(port, candidate)}"
                else:
                    status = "PASS"
                    detail = port_summary(port, candidate)
        else:
            status = "MANIFEST_DRIFT"
            detail = f"unknown manifest status {entry['status']!r}"
        record["status"] = status
        record["detail"] = detail
        record["notes"] = notes
        results.append(record)
        bump(status)
        for note in notes:
            print(f"REFERENCE RENDERERS NOTE: {entity}: {note}")
        if status != "PASS" or args.verbose:
            print(f"REFERENCE RENDERERS {status}: {entity}: {detail}")

    for problem in drift_global:
        print(f"REFERENCE RENDERERS MANIFEST_DRIFT: <manifest>: {problem}")
        bump("MANIFEST_DRIFT")

    failures = counts.get("DIVERGES", 0) + counts.get("MANIFEST_DRIFT", 0)
    order = ["PASS", "PENDING", "MOD", "NOT_APPLICABLE", "DIVERGES", "MANIFEST_DRIFT"]
    summary = ", ".join(f"{name} {counts.get(name, 0)}" for name in order)
    print(f"REFERENCE RENDERERS: {len(entries)} registrations ({len(registrations)} parsed from ClientProxyOreSpawn.java): {summary}; "
          f"unparsed reference axes {unparsed_axes}; {'FAIL' if failures else 'OK'}")
    if args.json is not None:
        args.json.parent.mkdir(parents=True, exist_ok=True)
    if args.json is not None:
        args.json.parent.mkdir(parents=True, exist_ok=True)
        args.json.write_text(json.dumps({
            "schema_version": 1,
            "ground_truth": "1.7.10 ClientProxyOreSpawn.java + RenderX.java, parsed; never the port",
            "manifest": str(args.manifest),
            "counts": counts,
            "unparsed_reference_axes": unparsed_axes,
            "manifest_drift": drift_global,
            "entries": results,
        }, indent=2, default=str) + "\n", encoding="utf-8", newline="\n")
    return 1 if failures else 0


if __name__ == "__main__":
    raise SystemExit(main())
