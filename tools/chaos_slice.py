"""Render cross-sections of the Chaos dimension terrain three ways:

  A. The original 1.7.10 math, straight from ChunkProviderOreSpawn6:
     initializeNoiseField (16-octave limit noises at 684.412/2053.236
     per CELL, 8-octave selector at /80 //60, cos banding, edge falloff,
     top blend) -> trilinear cell interpolation -> INVERTED threshold
     (air where d > 0, else stone).

  B. What the shipped beta.4 JSON actually computed: same algebra but the
     noise sampled at block coords with xz_scale 1.0 / y_scale 3.0
     (4x/8x too fine) and amplitude /128 (modern BlendedNoise divides by
     128; the spline stayed in legacy units).

  C. The fixed JSON math: block coords * (684.412*0.25, 684.412*0.375)
     = the original cell lattice frequencies, noise * 128 back to legacy
     units. Should be visually identical in character to A.

The Perlin stack is a faithful re-implementation of the legacy octave
scheme (octave i: frequency *2^-i... amplitude *2^i as in
NoiseGeneratorOctaves: d3 starts at 1 and halves, contribution /d3).
Bit-parity with Java's gradient tables is not attempted - this is a
shape/character check, not a seed-parity check.
"""

import numpy as np
from PIL import Image

RNG = np.random.default_rng(20260821)


class Perlin:
    def __init__(self, rng):
        self.perm = rng.permutation(256).astype(np.int32)
        self.perm = np.concatenate([self.perm, self.perm])
        self.off = rng.random(3) * 256.0

    @staticmethod
    def fade(t):
        return t * t * t * (t * (t * 6 - 15) + 10)

    def grad(self, h, x, y, z):
        # 12 gradient directions as in improved noise
        u = np.where(h < 8, x, y)
        v = np.where(h < 4, y, np.where((h == 12) | (h == 14), x, z))
        return np.where(h & 1 == 0, u, -u) + np.where(h & 2 == 0, v, -v)

    def sample(self, x, y, z):
        x = x + self.off[0]
        y = y + self.off[1]
        z = z + self.off[2]
        xi = np.floor(x).astype(np.int64) & 255
        yi = np.floor(y).astype(np.int64) & 255
        zi = np.floor(z).astype(np.int64) & 255
        xf = x - np.floor(x)
        yf = y - np.floor(y)
        zf = z - np.floor(z)
        u, v, w = self.fade(xf), self.fade(yf), self.fade(zf)
        p = self.perm
        aaa = p[p[p[xi] + yi] + zi] % 16
        aba = p[p[p[xi] + yi + 1] + zi] % 16
        aab = p[p[p[xi] + yi] + zi + 1] % 16
        abb = p[p[p[xi] + yi + 1] + zi + 1] % 16
        baa = p[p[p[xi + 1] + yi] + zi] % 16
        bba = p[p[p[xi + 1] + yi + 1] + zi] % 16
        bab = p[p[p[xi + 1] + yi] + zi + 1] % 16
        bbb = p[p[p[xi + 1] + yi + 1] + zi + 1] % 16

        def lerp(a, b, t):
            return a + t * (b - a)

        x1 = lerp(self.grad(aaa, xf, yf, zf), self.grad(baa, xf - 1, yf, zf), u)
        x2 = lerp(self.grad(aba, xf, yf - 1, zf), self.grad(bba, xf - 1, yf - 1, zf), u)
        y1 = lerp(x1, x2, v)
        x1 = lerp(self.grad(aab, xf, yf, zf - 1), self.grad(bab, xf - 1, yf, zf - 1), u)
        x2 = lerp(self.grad(abb, xf, yf - 1, zf - 1), self.grad(bbb, xf - 1, yf - 1, zf - 1), u)
        y2 = lerp(x1, x2, v)
        return lerp(y1, y2, w)


class Octaves:
    """Legacy NoiseGeneratorOctaves: octave i sampled at freq*2^-i... wait -
    legacy d3 starts at 1 and HALVES per octave while contribution is /d3,
    i.e. octave 0 = base frequency amplitude 1, octave i = freq/2^i,
    amplitude 2^i. (Coarser octaves dominate.)"""

    def __init__(self, rng, count):
        self.gens = [Perlin(rng) for _ in range(count)]

    def sample(self, x, y, z, sx, sy, sz):
        total = np.zeros(np.broadcast(x, y, z).shape)
        d3 = 1.0
        for gen in self.gens:
            total += gen.sample(x * sx * d3, y * sy * d3, z * sz * d3) / d3
            d3 /= 2.0
        return total


# Legacy scale constants (per CELL: 4 blocks horizontal, 8 vertical) -
# raw, exactly as initializeNoiseField passes them to func_76304_a.
D0 = 684.412
D1 = 2053.236

lim1 = Octaves(RNG, 16)
lim2 = Octaves(RNG, 16)
sel = Octaves(RNG, 8)

CELLS_X = 96           # 384 blocks wide
CELLS_Y = 17
WIDTH = CELLS_X * 4
HEIGHT = 128
Z_CELL = 7.3           # arbitrary slice


def adouble1():
    a = np.cos(np.arange(CELLS_Y) * np.pi * 6.0 / CELLS_Y) * 2.0
    for j in range(CELLS_Y):
        d2 = j if j <= CELLS_Y // 2 else CELLS_Y - 1 - j
        if d2 < 4.0:
            d2 = 4.0 - d2
            a[j] -= d2 * d2 * d2 * 10.0
    return a


A1 = adouble1()


def legacy_noise_at(cx, cy):
    """The d6 noise term of initializeNoiseField at cell coords (cx, cy)."""
    d8 = lim1.sample(cx, cy, Z_CELL, D0, D1, D0) / 512.0
    d9 = lim2.sample(cx, cy, Z_CELL, D0, D1, D0) / 512.0
    d10 = (sel.sample(cx, cy, Z_CELL, D0 / 80.0, D1 / 60.0, D0 / 80.0) / 10.0 + 1.0) / 2.0
    d10c = np.clip(d10, 0.0, 1.0)
    return d8 + (d9 - d8) * d10c


def field_original():
    """d6 with banding + blends on the cell lattice, then trilinear-in-2D
    (bilinear) expansion to blocks; returns block field, AIR where > 0."""
    cx = np.arange(CELLS_X + 1)
    cy = np.arange(CELLS_Y)
    gx, gy = np.meshgrid(cx, cy, indexing="ij")
    d6 = legacy_noise_at(gx, gy) - A1[None, :]
    # top blend to -10 over the last 3 cells
    for j in range(CELLS_Y):
        if j > CELLS_Y - 4:
            t = (j - (CELLS_Y - 4)) / 3.0
            d6[:, j] = d6[:, j] * (1.0 - t) + -10.0 * t
    # expand cells to blocks (lerp x by 4, y by 8) - same as the original's
    # in-loop interpolation for a fixed z
    blocks = np.zeros((WIDTH, HEIGHT))
    for i in range(CELLS_X):
        for j in range(CELLS_Y - 1):
            c00, c01 = d6[i, j], d6[i, j + 1]
            c10, c11 = d6[i + 1, j], d6[i + 1, j + 1]
            for dx in range(4):
                fx = dx / 4.0
                a = c00 + (c10 - c00) * fx
                b = c01 + (c11 - c01) * fx
                for dy in range(8):
                    fy = dy / 8.0
                    blocks[i * 4 + dx, j * 8 + dy] = a + (b - a) * fy
    return blocks


def field_from_modern(xz_scale, y_scale, amp):
    """The port's JSON math: sample at BLOCK coords with modern multipliers
    (684.412*xz_scale per block etc.), noise * amp, plus spline/blends in
    legacy units; evaluated on the same 4x8 cell lattice then expanded,
    matching 'interpolated' with size 1/2."""
    bx = np.arange(CELLS_X + 1) * 4.0
    by = np.arange(CELLS_Y) * 8.0
    gx, gy = np.meshgrid(bx, by, indexing="ij")
    sx = 684.412 * xz_scale
    sy = 684.412 * y_scale
    d8 = lim1.sample(gx, gy, Z_CELL * 4.0, sx, sy, sx) / 512.0
    d9 = lim2.sample(gx, gy, Z_CELL * 4.0, sx, sy, sx) / 512.0
    d10 = (sel.sample(gx, gy, Z_CELL * 4.0, sx / 80.0, sy / 60.0, sx / 80.0) / 10.0 + 1.0) / 2.0
    noise = (d8 + (d9 - d8) * np.clip(d10, 0, 1)) * amp

    spline = -A1  # the JSON spline knots are exactly -adouble1
    dens = noise + spline[None, :]
    yy = by[None, :].repeat(CELLS_X + 1, axis=0)
    t = np.clip((yy - 104.0) / 24.0, 0.0, 1.0)
    dens = dens * (1.0 - t) + -10.0 * t
    # JSON wraps in mul(-1) and the engine treats >0 as SOLID; to compare
    # against the original's "air where >0" convention, negate back:
    d6_equivalent = dens
    blocks = np.zeros((WIDTH, HEIGHT))
    for i in range(CELLS_X):
        for j in range(CELLS_Y - 1):
            c00, c01 = d6_equivalent[i, j], d6_equivalent[i, j + 1]
            c10, c11 = d6_equivalent[i + 1, j], d6_equivalent[i + 1, j + 1]
            for dx in range(4):
                fx = dx / 4.0
                a = c00 + (c10 - c00) * fx
                b = c01 + (c11 - c01) * fx
                for dy in range(8):
                    fy = dy / 8.0
                    blocks[i * 4 + dx, j * 8 + dy] = a + (b - a) * fy
    return blocks


SKY = (150, 200, 255)
STONE = (128, 128, 128)
GRASS = (90, 170, 60)
DIRT = (134, 96, 67)


def paint(d6_blocks):
    """AIR where d6 > 0 (the original's inverted threshold), stone else;
    grass+dirt cap on every top-of-run, patchy in Y60-65 skipped (shape
    check only). Adds the top/bottom dither scrape."""
    solid = d6_blocks <= 0.0
    rng = np.random.default_rng(7)
    img = np.zeros((HEIGHT, WIDTH, 3), np.uint8)
    img[:, :] = SKY
    for x in range(WIDTH):
        col = solid[x]
        # shell scrape
        for y in range(127, 122, -1):
            if y >= 127 - rng.integers(0, 5):
                col[y] = False
        for y in range(0, 5):
            if y <= rng.integers(0, 5):
                col[y] = False
        depth = -1
        for y in range(HEIGHT - 1, -1, -1):
            if not col[y]:
                depth = -1
                continue
            if depth == -1:
                img[y, x] = GRASS
                depth = 3
            elif depth > 0:
                img[y, x] = DIRT
                depth -= 1
            else:
                img[y, x] = STONE
    return np.flipud(img)


# The python stack outputs LEGACY units. Modern BlendedNoise = legacy/128;
# the shipped beta.4 JSON added that raw (amp 1/128 in legacy terms), the
# fixed JSON multiplies by 128 (amp 1 - full legacy amplitude).
panels = [
    ("A - original 1.7.10 math (ChunkProviderOreSpawn6)", field_original()),
    ("B - shipped beta.4 JSON (scales 1.0/3.0, noise /128)", field_from_modern(1.0, 3.0, 1.0 / 128.0)),
    ("C - fixed JSON (scales .25/.375, noise x128 restored)", field_from_modern(0.25, 0.375, 1.0)),
]

# Algebra proof: C must be numerically identical to A (same noise stack,
# same lattice coordinates, same blends).
diff = np.abs(panels[0][1] - panels[2][1]).max()
print(f"max |A - C| = {diff:.2e}")

PAD = 24
tile_h = HEIGHT * 3
canvas = Image.new("RGB", (WIDTH * 3 + PAD * 4, tile_h + PAD * 2 + 20), (24, 24, 24))
from PIL import ImageDraw

draw = ImageDraw.Draw(canvas)
for k, (label, field) in enumerate(panels):
    img = Image.fromarray(paint(field)).resize((WIDTH * 3, tile_h), Image.NEAREST)
    # panels stacked horizontally would be huge; place side by side scaled 1x
    img1 = Image.fromarray(paint(field)).resize((WIDTH, HEIGHT * 2), Image.NEAREST)
    canvas.paste(img1, (PAD + k * (WIDTH + PAD), PAD + 20))
    draw.text((PAD + k * (WIDTH + PAD), PAD + 4), label, fill=(255, 255, 255))

canvas = canvas.crop((0, 0, WIDTH * 3 + PAD * 4, HEIGHT * 2 + PAD * 2 + 20))
out = r"C:\Homework\Projects\Orespawn\tools\chaos_slices.png"
canvas.save(out)
print("saved", out)

# quick stats: solid fraction per panel in the mid band (y24..y104)
for label, field in panels:
    mid = field[:, 24:104]
    print(f"{label}: mid-band solid fraction = {(mid <= 0).mean():.3f}")
