"""Мини-библиотека пиксель-арта 32x32 для генерации текстур TechQuest."""
import math
import random
from PIL import Image, ImageDraw

S = 32


def rgb(h):
    h = h.lstrip('#')
    return (int(h[0:2], 16), int(h[2:4], 16), int(h[4:6], 16), 255)


def mix(a, b, t):
    return tuple(int(round(a[i] + (b[i] - a[i]) * t)) for i in range(4))


def shade(c, f):
    """f>0 светлее, f<0 темнее (с лёгким сдвигом оттенка: тени холоднее, света теплее)."""
    if f >= 0:
        t = mix(c, (255, 244, 214, 255), f)
    else:
        t = mix(c, (14, 16, 38, 255), -f)
    return (t[0], t[1], t[2], c[3])


def ramp(hexes):
    return [rgb(h) for h in hexes]


# ---- рампы материалов: 0 контур, 1 тень, 2 база, 3 свет, 4 блик
R = {
    'tin':      ramp(['#3b4656', '#6b7d93', '#9fb3c8', '#c9d9e8', '#f4faff']),
    'bronze':   ramp(['#4a2a12', '#85501f', '#b8772e', '#dba04b', '#ffe0a0']),
    'iron':     ramp(['#2f3238', '#5c6068', '#8d9199', '#b9bdc4', '#eef0f3']),
    'copper':   ramp(['#4a1f10', '#8a3f22', '#c8693a', '#ee9560', '#ffd2a8']),
    'gold':     ramp(['#5a3d06', '#a87a10', '#e0b020', '#f8d84a', '#fff8b0']),
    'lead':     ramp(['#232738', '#444a66', '#6b7391', '#8f98b8', '#c4cce6']),
    'silver':   ramp(['#3a4150', '#7d8696', '#b8c0cc', '#e0e6ee', '#ffffff']),
    'aluminum': ramp(['#40505e', '#7e92a3', '#b0c2d0', '#d6e4ee', '#fbfeff']),
    'nickel':   ramp(['#35382c', '#6a705a', '#9ea588', '#c4caae', '#f0f2da']),
    'steel':    ramp(['#1a212c', '#38455a', '#5c6c82', '#8aa0b8', '#c4d4e6']),
    'uranium':  ramp(['#0c3410', '#1c7326', '#3cbf48', '#8af06e', '#dcffc4']),
    'wood':     ramp(['#2e1d0e', '#5c3a1a', '#8a5a2c', '#b07c42', '#d8a868']),
    'rock':     ramp(['#2a2a2e', '#4e4e54', '#76767e', '#9a9aa4', '#c2c2cc']),
    'rubber':   ramp(['#0e0e12', '#222228', '#34343c', '#4a4a54', '#6c6c78']),
    'plastic':  ramp(['#5a6a78', '#94a8b8', '#c8d8e4', '#e8f2f8', '#ffffff']),
    'soot':     ramp(['#050506', '#121214', '#202024', '#34343a', '#52525a']),
}


class Cv:
    def __init__(self, w=S, h=S):
        self.w, self.h = w, h
        self.im = Image.new('RGBA', (w, h), (0, 0, 0, 0))
        self.d = ImageDraw.Draw(self.im)

    # --- базовые
    def get(self, x, y):
        if 0 <= x < self.w and 0 <= y < self.h:
            return self.im.getpixel((x, y))
        return (0, 0, 0, 0)

    def set(self, x, y, c):
        if 0 <= x < self.w and 0 <= y < self.h:
            self.im.putpixel((x, y), c)

    def blend(self, x, y, c, a):
        if 0 <= x < self.w and 0 <= y < self.h:
            o = self.im.getpixel((x, y))
            if o[3] == 0:
                self.im.putpixel((x, y), (c[0], c[1], c[2], int(255 * a)))
            else:
                m = mix(o, c, a)
                self.im.putpixel((x, y), (m[0], m[1], m[2], o[3]))

    def rect(self, x0, y0, x1, y1, c):
        """Заполненный прямоугольник, границы включительно."""
        for y in range(y0, y1 + 1):
            for x in range(x0, x1 + 1):
                self.set(x, y, c)

    def hline(self, x0, x1, y, c):
        for x in range(x0, x1 + 1):
            self.set(x, y, c)

    def vline(self, x, y0, y1, c):
        for y in range(y0, y1 + 1):
            self.set(x, y, c)

    def poly(self, pts, c):
        self.d.polygon(pts, fill=c)

    def line(self, p0, p1, c, w=1):
        self.d.line([p0, p1], fill=c, width=w)

    def ellipse(self, x0, y0, x1, y1, c):
        self.d.ellipse([x0, y0, x1, y1], fill=c)

    def fill(self, c):
        self.rect(0, 0, self.w - 1, self.h - 1, c)

    def paste(self, other, x, y):
        self.im.alpha_composite(other.im, (x, y))

    # --- эффекты
    def outline(self, c, inner=False, diag=False):
        """Обводка по внешнему краю фигуры."""
        src = self.im.copy()
        for y in range(self.h):
            for x in range(self.w):
                if src.getpixel((x, y))[3] == 0:
                    near = False
                    for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1)) + (((1, 1), (-1, -1), (1, -1), (-1, 1)) if diag else ()):
                        xx, yy = x + dx, y + dy
                        if 0 <= xx < self.w and 0 <= yy < self.h and src.getpixel((xx, yy))[3] > 0:
                            near = True
                            break
                    if near:
                        self.im.putpixel((x, y), c)

    def light_pass(self, strength=0.18):
        """Освещение сверху-слева: подсветить верх/лево краёв фигуры, затемнить низ/право."""
        src = self.im.copy()
        for y in range(self.h):
            for x in range(self.w):
                p = src.getpixel((x, y))
                if p[3] == 0:
                    continue
                up = src.getpixel((x, y - 1))[3] if y > 0 else 0
                lf = src.getpixel((x - 1, y))[3] if x > 0 else 0
                dn = src.getpixel((x, y + 1))[3] if y < self.h - 1 else 0
                rt = src.getpixel((x + 1, y))[3] if x < self.w - 1 else 0
                f = 0
                if up == 0:
                    f += strength
                if lf == 0:
                    f += strength * 0.6
                if dn == 0:
                    f -= strength
                if rt == 0:
                    f -= strength * 0.6
                if f:
                    self.im.putpixel((x, y), shade(p, f))

    def speckle(self, rng, n, colors, region=None, only_opaque=True):
        for _ in range(n):
            x0, y0, x1, y1 = region or (0, 0, self.w - 1, self.h - 1)
            x, y = rng.randint(x0, x1), rng.randint(y0, y1)
            if only_opaque and self.get(x, y)[3] == 0:
                continue
            self.set(x, y, rng.choice(colors))

    def gradient_v(self, x0, y0, x1, y1, top, bottom, dither=True):
        h = max(1, y1 - y0)
        for y in range(y0, y1 + 1):
            for x in range(x0, x1 + 1):
                t = (y - y0) / h
                if dither:
                    t += (0.06 if (x + y) % 2 else -0.06)
                self.set(x, y, mix(top, bottom, max(0, min(1, t))))

    def save(self, path):
        self.im.save(path)


# ---------------------------------------------------------------- шум
def tile_noise(seed, cell=4, w=S, h=S):
    """Зацикленный шум 0..1 (бесшовный)."""
    rng = random.Random(seed)
    gw, gh = w // cell, h // cell
    grid = [[rng.random() for _ in range(gw)] for _ in range(gh)]
    out = [[0.0] * w for _ in range(h)]
    for y in range(h):
        for x in range(w):
            fx, fy = x / cell, y / cell
            x0, y0 = int(fx) % gw, int(fy) % gh
            x1, y1 = (x0 + 1) % gw, (y0 + 1) % gh
            tx, ty = fx - int(fx), fy - int(fy)
            tx = tx * tx * (3 - 2 * tx)
            ty = ty * ty * (3 - 2 * ty)
            a = grid[y0][x0] * (1 - tx) + grid[y0][x1] * tx
            b = grid[y1][x0] * (1 - tx) + grid[y1][x1] * tx
            out[y][x] = a * (1 - ty) + b * ty
    return out


# ---------------------------------------------------------------- блоки: общие примитивы
def bevel(c, x0, y0, x1, y1, base, raised=True, k=0.22):
    """Рамка с фаской. raised=True: светлый верх/лево."""
    hi = shade(base, k if raised else -k)
    lo = shade(base, -k if raised else k)
    c.hline(x0, x1, y0, hi)
    c.vline(x0, y0, y1, hi)
    c.hline(x0, x1, y1, lo)
    c.vline(x1, y0, y1, lo)


def rivet(c, x, y, base):
    c.set(x, y, shade(base, 0.35))
    c.set(x + 1, y, shade(base, 0.1))
    c.set(x, y + 1, shade(base, 0.1))
    c.set(x + 1, y + 1, shade(base, -0.45))


def metal_plate(seed, base, rivets=True, border=True, grain=0.05):
    """Лист металла с лёгким зерном, фаской и заклёпками."""
    rng = random.Random(seed)
    c = Cv()
    n = tile_noise(seed, 4)
    for y in range(S):
        for x in range(S):
            v = (n[y][x] - 0.5) * 2 * grain + (rng.random() - 0.5) * grain
            c.set(x, y, shade(base, v))
    # горизонтальные «штрихи» шлифовки
    for _ in range(14):
        y = rng.randint(2, S - 3)
        x = rng.randint(2, S - 10)
        L = rng.randint(3, 8)
        for i in range(L):
            c.set(x + i, y, shade(base, 0.05))
    if border:
        bevel(c, 0, 0, S - 1, S - 1, base, True, 0.2)
        bevel(c, 1, 1, S - 2, S - 2, base, True, 0.08)
        c.hline(0, S - 1, 0, shade(base, -0.5)) if False else None
    if rivets:
        for (x, y) in ((3, 3), (S - 5, 3), (3, S - 5), (S - 5, S - 5)):
            rivet(c, x, y, base)
    return c


def window(c, x0, y0, x1, y1, fill, base):
    """Утопленное окошко."""
    c.rect(x0, y0, x1, y1, fill)
    bevel(c, x0 - 1, y0 - 1, x1 + 1, y1 + 1, base, False, 0.35)


def screen(c, x0, y0, x1, y1, col, base):
    """Экран с подсветкой и бликом."""
    dark = shade(col, -0.75)
    c.gradient_v(x0, y0, x1, y1, shade(col, -0.55), dark, False)
    bevel(c, x0 - 1, y0 - 1, x1 + 1, y1 + 1, base, False, 0.4)
    # сканлайны
    for y in range(y0 + 1, y1, 2):
        c.hline(x0, x1, y, shade(col, -0.65))


def glow(c, cx, cy, r, col, a=0.5):
    for y in range(cy - r, cy + r + 1):
        for x in range(cx - r, cx + r + 1):
            d = math.hypot(x - cx, y - cy)
            if d <= r:
                c.blend(x, y, col, a * (1 - d / r) ** 1.2)


def gear(c, cx, cy, r, teeth, base, hole=True, rot=0.0):
    pts_out = []
    n = teeth * 2
    for i in range(n * 2):
        ang = rot + i * math.pi / n
        rr = r if (i // 2) % 2 == 0 else r - 2
        pts_out.append((cx + math.cos(ang) * rr, cy + math.sin(ang) * rr))
    c.poly(pts_out, shade(base, 0))
    c.ellipse(int(cx - r + 3), int(cy - r + 3), int(cx + r - 3), int(cy + r - 3), shade(base, 0.1))
    if hole:
        c.ellipse(int(cx - 2), int(cy - 2), int(cx + 2), int(cy + 2), shade(base, -0.65))
    # свет сверху-слева
    for ang in range(180, 270, 12):
        a = math.radians(ang)
        x, y = int(cx + math.cos(a) * (r - 3)), int(cy + math.sin(a) * (r - 3))
        c.set(x, y, shade(base, 0.45))


def stripes(c, x0, y0, x1, y1, a, b, w=4):
    """Диагональные предупреждающие полосы."""
    for y in range(y0, y1 + 1):
        for x in range(x0, x1 + 1):
            c.set(x, y, a if ((x + y) // w) % 2 == 0 else b)
