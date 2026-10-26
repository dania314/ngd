"""Предметы TechQuest, 32x32."""
import math
import random
from pxlib import *


# ------------------------------------------------------------------ металлические формы
def ingot(rp, glow_col=None):
    c = Cv()
    o, sh, bs, lt, hi = rp
    # правая грань, верхняя грань, передняя грань (изометрия)
    c.poly([(23, 12), (29, 7), (29, 17), (23, 22)], sh)
    c.poly([(5, 12), (23, 12), (29, 7), (11, 7)], lt)
    c.poly([(5, 12), (23, 12), (23, 22), (5, 22)], bs)
    # градиент передней грани
    for y in range(13, 22):
        t = (y - 13) / 9
        for x in range(6, 23):
            col = mix(shade(bs, 0.1), shade(bs, -0.22), t)
            c.set(x, y, col)
    # вдавленная площадка сверху
    c.poly([(9, 11), (21, 11), (24, 9), (12, 9)], shade(lt, -0.1))
    c.hline(10, 20, 9, shade(lt, 0.25))
    # рёбра и блики
    c.hline(5, 23, 12, hi)
    c.line((23, 12), (29, 7), hi)
    c.line((5, 12), (11, 7), shade(lt, 0.25))
    c.vline(6, 14, 20, shade(bs, 0.3))
    c.line((8, 15), (12, 15), shade(bs, 0.45))
    c.line((24, 14), (24, 19), shade(sh, -0.2))
    c.outline(o, diag=False)
    # контур внутренних рёбер
    c.hline(5, 23, 22, shade(o, 0.15))
    if glow_col:
        glow(c, 17, 16, 14, glow_col, 0.22)
    return c


def plate(rp):
    c = Cv()
    o, sh, bs, lt, hi = rp
    c.poly([(23, 21), (29, 14), (29, 18), (23, 25)], sh)
    c.poly([(3, 21), (23, 21), (23, 25), (3, 25)], shade(bs, -0.12))
    c.poly([(3, 21), (23, 21), (29, 14), (9, 14)], bs)
    # градиент по поверхности
    for y in range(14, 21):
        for x in range(3, 30):
            p = c.get(x, y)
            if p[3] and p[:3] != sh[:3]:
                t = (21 - y) / 7
                c.set(x, y, mix(p, shade(lt, 0.05), t * 0.55))
    # скошенная кромка
    c.line((3, 21), (23, 21), hi)
    c.line((23, 21), (29, 14), hi)
    c.line((3, 21), (9, 14), lt)
    c.line((9, 14), (29, 14), shade(lt, 0.1))
    # заклёпки и штамп
    for (x, y) in ((8, 19), (19, 19), (13, 16), (24, 16)):
        c.set(x, y, shade(bs, -0.4))
        c.set(x - 1, y - 1, shade(hi, 0))
    c.line((11, 18), (17, 18), shade(bs, -0.18))
    c.line((12, 17), (18, 17), shade(bs, -0.1))
    c.line((4, 23), (22, 23), shade(bs, -0.3))
    c.outline(o)
    return c


def dust(rp, seed, tint=None):
    rng = random.Random(seed)
    o, sh, bs, lt, hi = rp
    c = Cv()
    # куча: купол
    cx, cy = 16, 22
    for y in range(8, 28):
        for x in range(2, 30):
            dx = (x - cx) / 13.5
            dy = (y - cy) / 13.0
            # профиль горки
            if dy <= 0 and dx * dx + (dy / 1.0) ** 2 <= 1:
                top = 1 - dx * dx
                if (cy - y) / 13.0 <= top * 0.95 + rng.random() * 0.06:
                    c.set(x, y, bs)
            elif dy > 0 and dy < 0.35 and abs(dx) < 1 - dy * 0.9 + rng.random() * 0.1:
                c.set(x, y, bs)
    # тени и свет по зёрнам
    src = c.im.copy()
    for y in range(32):
        for x in range(32):
            p = src.getpixel((x, y))
            if p[3] == 0:
                continue
            lit = -(x - cx) * 0.045 - (y - 16) * 0.04 + (rng.random() - 0.5) * 0.7
            if lit > 0.22:
                c.set(x, y, lt)
            elif lit > 0.55:
                c.set(x, y, hi)
            elif lit < -0.35:
                c.set(x, y, sh)
            elif lit < -0.18:
                c.set(x, y, shade(bs, -0.1))
    c.speckle(rng, 22, [hi, lt, sh], (4, 10, 28, 26))
    # блики на гребне
    for _ in range(7):
        x = rng.randint(9, 19)
        for y in range(9, 18):
            if c.get(x, y)[3]:
                c.set(x, y, hi)
                break
    # рассыпанные зёрна
    for _ in range(8):
        x, y = rng.randint(2, 29), rng.randint(25, 29)
        if c.get(x, y)[3] == 0 and rng.random() < 0.7:
            c.set(x, y, rng.choice([bs, lt, sh]))
    c.outline(o)
    return c


def raw_chunk(rp, seed):
    """Сырая руда: серо-бурые валуны с вкраплениями металла."""
    rng = random.Random(seed)
    o, sh, bs, lt, hi = rp
    rock = R['rock']
    c = Cv()

    def blob(cx, cy, rx, ry, sd):
        r2 = random.Random(sd)
        pts = []
        n = 11
        for i in range(n):
            a = i / n * 2 * math.pi
            k = 0.8 + r2.random() * 0.35
            pts.append((cx + math.cos(a) * rx * k, cy + math.sin(a) * ry * k))
        mask = Cv()
        mask.poly(pts, (1, 1, 1, 255))
        for y in range(32):
            for x in range(32):
                if mask.get(x, y)[3]:
                    lit = -((x - cx) / rx) * 0.5 - ((y - cy) / ry) * 0.7 + (r2.random() - 0.5) * 0.45
                    if lit > 0.55:
                        col = rock[3]
                    elif lit > 0.1:
                        col = rock[2]
                    elif lit > -0.4:
                        col = shade(rock[2], -0.2)
                    else:
                        col = rock[1]
                    c.set(x, y, col)
        # вкрапления металла
        for _ in range(int(rx * ry / 6)):
            x = int(cx + (r2.random() - 0.5) * rx * 1.7)
            y = int(cy + (r2.random() - 0.5) * ry * 1.7)
            if mask.get(x, y)[3]:
                w = r2.randint(1, 3)
                h = r2.randint(1, 2)
                for yy in range(h):
                    for xx in range(w):
                        if mask.get(x + xx, y + yy)[3]:
                            col = lt if yy == 0 else bs
                            if xx == w - 1 or yy == h - 1:
                                col = sh if (xx + yy) % 2 else bs
                            c.set(x + xx, y + yy, col)
                c.set(x, y, hi)

    blob(21, 12, 8, 7, seed + 1)
    blob(11, 21, 9, 7, seed + 2)
    blob(23, 23, 6, 5, seed + 3)
    c.outline(shade(rock[0], -0.1))
    return c


# ------------------------------------------------------------------ электроника
def circuit(advanced):
    c = Cv()
    if advanced:
        board = ramp(['#071a2a', '#0e3550', '#165a80', '#2282ae', '#56b8e0'])
        trace = rgb('#f2c84a')
    else:
        board = ramp(['#0a2410', '#14481e', '#1f7a30', '#34a84a', '#6fe088'])
        trace = rgb('#cfd8c0')
    o, sh, bs, lt, hi = board
    c.poly([(4, 4), (27, 4), (27, 27), (4, 27)], bs)
    c.rect(4, 4, 27, 27, bs)
    rng = random.Random(7 if advanced else 3)
    for y in range(4, 28):
        for x in range(4, 28):
            if rng.random() < 0.12:
                c.set(x, y, shade(bs, 0.06))
    # дорожки
    paths = [
        [(6, 8), (12, 8), (12, 12), (18, 12)], [(6, 14), (9, 14), (9, 20), (14, 20)],
        [(25, 8), (22, 8), (22, 16), (25, 16)], [(18, 24), (24, 24), (24, 20)],
        [(6, 24), (10, 24)], [(14, 6), (14, 9)], [(20, 6), (20, 10)],
    ]
    for pth in paths:
        for a, b in zip(pth, pth[1:]):
            c.line(a, b, mix(trace, bs, 0.35))
    for pth in paths:
        for a, b in zip(pth, pth[1:]):
            c.set(a[0], a[1], trace)
    # микросхема
    chip = ramp(['#080808', '#1b1b20', '#2c2c34', '#44444e', '#6a6a78'])
    cx0, cy0, cx1, cy1 = (11, 12, 21, 21) if advanced else (12, 13, 20, 20)
    c.rect(cx0, cy0, cx1, cy1, chip[2])
    bevel(c, cx0, cy0, cx1, cy1, chip[2], True, 0.3)
    for i in range(cx0 + 1, cx1, 2):
        c.set(i, cy0 - 1, trace)
        c.set(i, cy1 + 1, trace)
    for j in range(cy0 + 1, cy1, 2):
        c.set(cx0 - 1, j, trace)
        c.set(cx1 + 1, j, trace)
    c.set(cx0 + 1, cy0 + 1, chip[4])
    if advanced:
        c.rect(14, 15, 18, 18, rgb('#1a3a5a'))
        c.set(16, 16, rgb('#8cf0ff'))
        glow(c, 16, 16, 5, rgb('#40d0ff'), 0.35)
        # маленькие золотые конденсаторы
        for (x, y) in ((6, 19), (23, 22), (6, 11)):
            c.rect(x, y, x + 2, y + 1, trace)
            c.set(x, y, shade(trace, 0.3))
    else:
        # красный резистор и конденсатор
        c.rect(6, 19, 9, 20, rgb('#c8603a'))
        c.set(7, 19, rgb('#f0e0a0'))
        c.ellipse(22, 21, 25, 24, rgb('#2a6ac8'))
        c.set(22, 21, rgb('#7ab0f0'))
    # отверстия по углам
    for (x, y) in ((6, 6), (25, 6), (6, 25), (25, 25)):
        c.set(x, y, shade(bs, -0.7))
        c.set(x + 1, y + 1, shade(bs, 0.25))
    c.rect(4, 4, 27, 4, hi if False else lt)
    bevel(c, 4, 4, 27, 27, bs, True, 0.22)
    c.outline(o)
    return c


# ------------------------------------------------------------------ лопасти
def blade(rp, kind, seed):
    c = Cv()
    o, sh, bs, lt, hi = rp
    # лопасть по диагонали: от ступицы (6,26) к концу (26,5)
    pts = [(5, 27), (6, 20), (11, 13), (18, 7), (25, 3), (29, 4), (28, 9), (23, 15), (16, 22), (9, 27)]
    c.poly(pts, bs)
    # градиент поперёк лопасти (по оси), верх светлее
    for y in range(32):
        for x in range(32):
            if c.get(x, y)[3]:
                # расстояние до оси лопасти (линия (6,26)->(27,5))
                d = ((x - 6) + (y - 26)) * 0.707
                t = max(-1, min(1, d / 6.5))
                if t < -0.45:
                    c.set(x, y, lt)
                elif t < -0.1:
                    c.set(x, y, mix(lt, bs, 0.55))
                elif t > 0.45:
                    c.set(x, y, sh)
                elif t > 0.15:
                    c.set(x, y, mix(bs, sh, 0.5))
    # передняя кромка — блик
    c.line((6, 20), (11, 13), hi)
    c.line((11, 13), (18, 7), hi)
    c.line((18, 7), (25, 3), hi)
    # ребро жёсткости
    c.line((8, 25), (26, 6), shade(bs, -0.18))
    c.line((9, 25), (27, 7), shade(bs, 0.12))
    if kind == 'wood':
        rng = random.Random(seed)
        for _ in range(26):
            x, y = rng.randint(6, 28), rng.randint(3, 27)
            if c.get(x, y)[3]:
                c.set(x, y, shade(c.get(x, y), -0.2))
                c.set(x + 1, y - 1, shade(c.get(x + 1, y - 1) if c.get(x + 1, y - 1)[3] else bs, -0.1)) if c.get(x + 1, y - 1)[3] else None
    elif kind == 'advanced':
        green = rgb('#4cffa8')
        c.line((9, 24), (26, 6), green)
        c.set(25, 5, rgb('#e8fff4'))
        c.ellipse(2, 23, 10, 31, R['iron'][2])
    else:
        # заклёпки
        for (x, y) in ((12, 19), (16, 15), (21, 10)):
            c.set(x, y, hi)
            c.set(x + 1, y + 1, sh)
    # ступица
    c.ellipse(2, 23, 10, 31, R['iron'][2])
    c.ellipse(3, 24, 9, 30, shade(R['iron'][2], 0.1))
    c.set(4, 25, R['iron'][4])
    c.ellipse(5, 26, 7, 28, R['iron'][1])
    c.set(5, 26, R['iron'][3])
    c.outline(o)
    if kind == 'advanced':
        glow(c, 17, 15, 12, rgb('#4cffa8'), 0.18)
    return c


# ------------------------------------------------------------------ жидкости (колбы)
def vial(liq, cap, seed, glowing=False):
    c = Cv()
    glass = ramp(['#3a566c', '#7aa0ba', '#b8d8ea', '#e0f2fa', '#ffffff'])
    lo, ls, lb, ll, lh = liq
    # корпус колбы
    c.poly([(13, 7), (19, 7), (19, 12), (24, 18), (24, 27), (23, 28), (9, 28), (8, 27), (8, 18), (13, 12)], (190, 225, 245, 150))
    # жидкость
    for y in range(17, 28):
        for x in range(8, 25):
            if c.get(x, y)[3] and y >= 18 + (1 if (x // 2) % 2 else 0):
                t = (y - 18) / 10
                c.set(x, y, mix(lb, ls, t * 0.9))
    # поверхность жидкости
    for x in range(9, 24):
        if c.get(x, 18)[3] or c.get(x, 19)[3]:
            c.set(x, 18 + (1 if (x // 2) % 2 else 0), ll)
    # пузырьки
    rng = random.Random(seed)
    for _ in range(4):
        x, y = rng.randint(10, 22), rng.randint(21, 26)
        c.set(x, y, ll)
    # стеклянные блики
    c.vline(10, 19, 26, mix(rgb('#ffffff'), lb, 0.25))
    c.vline(11, 20, 24, mix(rgb('#ffffff'), lb, 0.5))
    c.line((13, 9), (13, 11), glass[4])
    c.line((14, 13), (10, 18), glass[4])
    # стеклянный контур
    c.outline(glass[0])
    # горлышко и пробка
    c.rect(13, 3, 19, 6, cap[2])
    c.hline(13, 19, 3, cap[3])
    c.hline(13, 19, 6, cap[1])
    c.vline(13, 3, 6, cap[3])
    c.vline(19, 3, 6, cap[1])
    for x in (15, 17):
        c.vline(x, 4, 5, cap[1])
    c.outline(cap[0] if False else glass[0])
    # метка на баке
    c.rect(12, 21, 20, 24, (240, 240, 235, 255)) if False else None
    if glowing:
        glow(c, 16, 23, 11, ll, 0.22)
    return c


# ------------------------------------------------------------------ материалы
def weave(base, seed, size=(5, 5, 26, 26)):
    """Тканое полотно (углеволокно/кевлар)."""
    c = Cv()
    o, sh, bs, lt, hi = base
    x0, y0, x1, y1 = size
    for y in range(y0, y1 + 1):
        for x in range(x0, x1 + 1):
            cell = ((x // 2) + (y // 2)) % 2
            if cell:
                col = lt if (y % 2 == 0) else bs
            else:
                col = sh if (x % 2 == 0) else bs
            c.set(x, y, col)
    # подвёрнутый уголок
    c.poly([(x1 - 6, y0), (x1, y0), (x1, y0 + 6)], shade(bs, 0.3))
    c.line((x1 - 6, y0), (x1, y0 + 6), hi)
    bevel(c, x0, y0, x1, y1, bs, True, 0.25)
    c.outline(o)
    return c


def plastic_item():
    c = Cv()
    rp = R['plastic']
    rng = random.Random(11)
    # горсть гранул
    pos = []
    for row in range(6):
        for col in range(7):
            x = 5 + col * 3 + (1 if row % 2 else 0) + rng.randint(-1, 0)
            y = 11 + row * 3 + rng.randint(-1, 0)
            if (x - 16) ** 2 / 144 + (y - 18) ** 2 / 100 < 1:
                pos.append((x, y))
    for (x, y) in sorted(pos, key=lambda p: p[1]):
        c.rect(x, y, x + 2, y + 2, rp[2])
        c.set(x, y, rp[4])
        c.set(x + 1, y, rp[3])
        c.set(x + 2, y + 2, rp[1])
        c.set(x + 1, y + 2, shade(rp[2], -0.25))
    c.outline(rp[0])
    return c


def rubber_item():
    c = Cv()
    rb = R['rubber']
    c.poly([(5, 14), (21, 14), (27, 9), (11, 9)], rb[3])
    c.poly([(5, 14), (21, 14), (21, 23), (5, 23)], rb[2])
    c.poly([(21, 14), (27, 9), (27, 18), (21, 23)], rb[1])
    for x in range(7, 21, 3):
        c.vline(x, 16, 21, rb[1])
        c.vline(x + 1, 16, 21, rb[3])
    c.hline(5, 21, 14, rb[4])
    c.line((21, 14), (27, 9), rb[4])
    c.line((6, 12), (24, 12), shade(rb[3], 0.1))
    c.outline(rb[0])
    # зелёный отблеск натурального каучука
    c.set(9, 18, rgb('#6a8a60'))
    return c


def lead_foam():
    c = Cv()
    rp = R['lead']
    rng = random.Random(21)
    c.poly([(5, 24), (3, 16), (8, 8), (17, 5), (26, 9), (29, 18), (25, 26), (14, 28)], rp[2])
    for y in range(32):
        for x in range(32):
            if c.get(x, y)[3]:
                lit = -(x - 16) * 0.04 - (y - 16) * 0.05 + (rng.random() - 0.5) * 0.25
                c.set(x, y, rp[3] if lit > 0.3 else (rp[1] if lit < -0.3 else rp[2]))
    for _ in range(26):
        x, y = rng.randint(5, 27), rng.randint(7, 26)
        if c.get(x, y)[3]:
            r = rng.randint(1, 2)
            c.ellipse(x, y, x + r, y + r, rp[0])
            c.set(x, y + r, rp[4]) if c.get(x, y + r)[3] else None
    c.outline(rp[0])
    c.light_pass(0.15)
    return c


def glassed_waste():
    c = Cv()
    g = ramp(['#04140a', '#0c3a1e', '#157a3c', '#2cc468', '#9cffc4'])
    c.poly([(16, 3), (26, 8), (28, 19), (21, 28), (10, 27), (4, 17), (7, 8)], g[2])
    rng = random.Random(5)
    for y in range(32):
        for x in range(32):
            if c.get(x, y)[3]:
                lit = -(x - 16) * 0.05 - (y - 14) * 0.06 + (rng.random() - 0.5) * 0.3
                c.set(x, y, g[3] if lit > 0.3 else (g[1] if lit < -0.25 else g[2]))
    # грани стекла
    c.line((16, 3), (13, 15), g[4])
    c.line((13, 15), (21, 28), g[1])
    c.line((13, 15), (4, 17), g[1])
    c.line((26, 8), (13, 15), g[3])
    c.line((8, 9), (10, 12), g[4])
    # тёмное радиоактивное ядро
    c.ellipse(14, 16, 18, 20, g[0])
    c.set(15, 17, g[4])
    c.outline(g[0])
    glow(c, 16, 17, 12, rgb('#58ff90'), 0.22)
    return c


def spent_fuel():
    c = Cv()
    steel = R['steel']
    # вертикальный стержень
    c.rect(12, 4, 19, 28, steel[2])
    for y in range(5, 28):
        c.set(12, y, steel[3])
        c.set(13, y, mix(steel[3], steel[2], 0.5))
        c.set(19, y, steel[1])
        c.set(18, y, mix(steel[1], steel[2], 0.5))
    # светящиеся кольца
    for y in (9, 16, 23):
        c.rect(12, y, 19, y + 1, rgb('#56e060'))
        c.hline(12, 19, y, rgb('#c8ffb0'))
        c.set(19, y + 1, rgb('#1c7a24'))
    # заглушки
    c.rect(11, 2, 20, 4, steel[3])
    c.rect(11, 28, 20, 30, steel[1])
    c.hline(11, 20, 2, steel[4])
    c.hline(11, 20, 30, steel[0])
    c.outline(steel[0])
    glow(c, 16, 16, 15, rgb('#58ff70'), 0.25)
    return c


def soot_item():
    c = dust(R['soot'], 4)
    # серые хлопья копоти
    return c


# ------------------------------------------------------------------ модули
def module_base(frame_col):
    c = Cv()
    o, sh, bs, lt, hi = frame_col
    c.rect(4, 4, 27, 27, bs)
    c.rect(7, 7, 24, 24, shade(bs, -0.3))
    bevel(c, 4, 4, 27, 27, bs, True, 0.3)
    bevel(c, 7, 7, 24, 24, bs, False, 0.35)
    for (x, y) in ((5, 5), (25, 5), (5, 25), (25, 25)):
        c.set(x, y, hi)
        c.set(x + 1, y + 1, sh) if x < 25 or y < 25 else None
    return c


def module_battery():
    c = module_base(R['steel'])
    yel = ramp(['#5a4000', '#b08000', '#f0c020', '#ffe060', '#fff8c0'])
    c.rect(11, 10, 20, 22, yel[2])
    c.rect(13, 8, 18, 9, R['iron'][3])
    bevel(c, 11, 10, 20, 22, yel[2], True, 0.3)
    # молния
    for (x, y) in ((16, 12), (15, 13), (14, 14), (15, 15), (16, 15), (17, 16), (16, 17), (15, 18), (14, 19)):
        c.set(x, y, rgb('#3a2800'))
    c.set(16, 12, rgb('#3a2800'))
    c.hline(11, 20, 21, yel[1])
    c.outline(R['steel'][0])
    glow(c, 16, 16, 7, yel[3], 0.22)
    return c


def module_filter():
    c = module_base(R['steel'])
    cy = ramp(['#06303a', '#0e6a7a', '#18a8c0', '#50d8f0', '#c0f8ff'])
    for y in range(9, 23):
        for x in range(9, 23):
            c.set(x, y, cy[1] if (x + y) % 2 else cy[2])
    for x in range(9, 23, 3):
        c.vline(x, 9, 22, cy[3])
    for y in range(9, 23, 3):
        c.hline(9, 22, y, cy[3])
    c.set(10, 10, cy[4])
    glow(c, 16, 16, 9, cy[3], 0.18)
    c.outline(R['steel'][0])
    return c


def module_plating():
    c = module_base(R['steel'])
    ir = R['iron']
    c.poly([(16, 8), (23, 12), (23, 20), (16, 24), (9, 20), (9, 12)], ir[2])
    c.poly([(16, 10), (21, 13), (21, 19), (16, 22), (11, 19), (11, 13)], ir[3])
    c.line((16, 10), (11, 13), ir[4])
    c.line((16, 10), (21, 13), ir[4])
    c.line((11, 13), (11, 19), ir[4])
    for (x, y) in ((16, 12), (13, 15), (19, 15), (16, 20)):
        c.set(x, y, ir[1])
        c.set(x, y - 1, ir[4]) if y > 12 else None
    c.line((12, 17), (20, 17), ir[2])
    c.outline(R['steel'][0])
    return c


def module_servo():
    c = module_base(R['steel'])
    gear(c, 14, 15, 7, 6, R['bronze'][2], True, 0.2)
    c.rect(19, 18, 23, 22, R['iron'][2])
    c.rect(20, 17, 22, 17, R['iron'][3])
    c.vline(19, 18, 22, R['iron'][3])
    c.set(21, 20, rgb('#f04040'))
    c.outline(R['steel'][0])
    return c


# ------------------------------------------------------------------ инструменты и броня
def energy_meter():
    c = Cv()
    yel = ramp(['#4a3600', '#a07800', '#e8b820', '#ffd850', '#fff2a0'])
    c.rect(8, 6, 23, 28, yel[2])
    bevel(c, 8, 6, 23, 28, yel[2], True, 0.3)
    c.rect(10, 8, 21, 16, rgb('#0a2a16'))
    bevel(c, 10, 8, 21, 16, yel[2], False, 0.4)
    # график на экране
    pts = [(11, 14), (13, 12), (15, 13), (17, 10), (19, 12), (20, 9)]
    for a, b in zip(pts, pts[1:]):
        c.line(a, b, rgb('#58ff98'))
    c.hline(11, 20, 15, rgb('#14583a'))
    # кнопки
    c.rect(11, 19, 13, 21, rgb('#d83030'))
    c.set(11, 19, rgb('#ff8a8a'))
    c.rect(15, 19, 17, 21, rgb('#2080e0'))
    c.set(15, 19, rgb('#90c8ff'))
    c.rect(19, 19, 21, 21, rgb('#303038'))
    # динамик
    for y in (24, 26):
        c.hline(11, 20, y, yel[1])
    # антенна
    c.rect(20, 2, 21, 5, R['iron'][2])
    c.set(20, 2, R['iron'][4])
    c.outline(yel[0])
    return c


def wrench():
    c = Cv()
    ir, br = R['iron'], R['bronze']
    # рукоять по диагонали
    c.poly([(5, 25), (8, 28), (21, 15), (18, 12)], ir[2])
    c.poly([(5, 25), (8, 28), (11, 25), (8, 22)], rgb('#e06020'))  # прорезиненный конец
    c.poly([(8, 22), (11, 25), (14, 22), (11, 19)], rgb('#c04c14'))
    # головка с зевом
    c.ellipse(17, 3, 28, 14, ir[2])
    c.poly([(21, 3), (27, 3), (24, 9)], (0, 0, 0, 0))
    c.rect(22, 4, 25, 7, (0, 0, 0, 0))
    c.line((19, 12), (22, 8), ir[1])
    # свет
    c.line((6, 24), (17, 13), ir[3])
    c.line((19, 5), (24, 3), ir[4])
    c.set(18, 6, ir[4])
    c.set(10, 21, rgb('#ffa060'))
    c.outline(ir[0])
    return c


def quest_book():
    c = Cv()
    cv = ramp(['#2a1608', '#5a3014', '#8a4a20', '#b8682c', '#e09050'])
    c.rect(6, 4, 26, 27, cv[2])
    c.rect(6, 4, 8, 27, cv[1])
    bevel(c, 6, 4, 26, 27, cv[2], True, 0.25)
    # страницы
    c.rect(9, 26, 26, 28, rgb('#e8e0c8'))
    c.hline(9, 26, 27, rgb('#c8bea0'))
    # золотая шестерёнка-эмблема
    gold = R['gold']
    gear(c, 17, 15, 6, 6, gold[2], True, 0.1)
    c.set(15, 12, gold[4])
    c.set(14, 13, gold[3])
    # уголки
    for (x, y) in ((25, 4), (25, 26)):
        c.set(x, y, gold[3])
    c.hline(10, 24, 5, cv[3])
    c.hline(10, 24, 23, cv[1])
    c.hline(10, 24, 24, cv[3])
    c.outline(cv[0])
    return c


def chest(body, trim, glow_col, kind):
    c = Cv()
    o, sh, bs, lt, hi = body
    # нагрудник: плечи + корпус
    c.poly([(3, 9), (10, 5), (13, 5), (16, 8), (19, 5), (22, 5), (29, 9), (28, 17), (24, 17), (24, 28), (8, 28), (8, 17), (4, 17)], bs)
    # вырез горловины
    c.poly([(13, 5), (16, 9), (19, 5)], (0, 0, 0, 0))
    # затенение/свет
    for y in range(32):
        for x in range(32):
            if c.get(x, y)[3]:
                t = (x - 16) / 13
                if t < -0.5:
                    c.set(x, y, lt)
                elif t > 0.55:
                    c.set(x, y, sh)
    c.vline(24, 18, 27, sh)
    # пластины
    c.hline(9, 23, 18, shade(bs, -0.35))
    c.hline(9, 23, 19, shade(bs, 0.25))
    c.hline(9, 23, 24, shade(bs, -0.35))
    # плечевые накладки
    c.line((4, 10), (10, 6), hi)
    c.line((28, 10), (22, 6), lt)
    c.rect(4, 12, 7, 16, shade(bs, -0.15))
    c.rect(25, 12, 28, 16, shade(bs, -0.3))
    if kind == 'exo':
        c.rect(14, 12, 18, 16, rgb('#06202a'))
        c.rect(15, 13, 17, 15, glow_col)
        c.set(15, 13, rgb('#ffffff'))
        glow(c, 16, 14, 6, glow_col, 0.3)
        for y in range(19, 28, 2):
            c.set(11, y, glow_col)
            c.set(21, y, glow_col)
        c.vline(16, 17, 23, mix(glow_col, bs, 0.4))
    else:
        # хазмат: знак радиации и шланги
        c.ellipse(12, 11, 20, 19, rgb('#2a2200'))
        c.ellipse(13, 12, 19, 18, rgb('#ffd820'))
        for a in (90, 210, 330):
            ar = math.radians(a)
            c.poly([(16, 15), (16 + math.cos(ar - 0.5) * 3, 15 - math.sin(ar - 0.5) * 3),
                    (16 + math.cos(ar + 0.5) * 3, 15 - math.sin(ar + 0.5) * 3)], rgb('#201800'))
        c.ellipse(15, 14, 17, 16, rgb('#ffd820'))
        c.set(16, 15, rgb('#201800'))
        c.rect(9, 22, 12, 24, rgb('#303038'))
        c.rect(20, 22, 23, 24, rgb('#303038'))
        c.hline(9, 12, 22, rgb('#606068'))
        c.hline(20, 23, 22, rgb('#606068'))
        c.hline(8, 24, 20, rgb('#303038'))
    c.outline(o)
    return c


def tin_cable_item():
    c = Cv()
    tn = R['tin']
    cx, cy = 15.5, 15.5
    # тор (моток) с освещением от нормали
    for y in range(32):
        for x in range(32):
            dx, dy = x - cx, y - cy
            r = math.hypot(dx, dy)
            if 5.5 <= r <= 12.5:
                t = (r - 5.5) / 7.0            # 0 внутри кольца .. 1 снаружи
                tube = math.sin(t * math.pi)   # профиль трубки
                ang = math.atan2(dy, dx)
                lit = -math.cos(ang - math.radians(225)) * 0.5   # свет сверху-слева
                v = (tube - 0.5) * 0.9 + lit * 0.7
                if v > 0.55:
                    col = tn[4]
                elif v > 0.2:
                    col = tn[3]
                elif v > -0.25:
                    col = tn[2]
                elif v > -0.55:
                    col = tn[1]
                else:
                    col = shade(tn[1], -0.3)
                c.set(x, y, col)
    # витки обмотки (диагональные штрихи)
    for a in range(0, 360, 22):
        ar = math.radians(a)
        for r in (7, 8, 9, 10, 11):
            x, y = int(cx + math.cos(ar) * r), int(cy + math.sin(ar) * r)
            c.set(x, y, shade(c.get(x, y), -0.18)) if c.get(x, y)[3] else None
    # выходные концы с оголёнными жилами
    c.line((25, 24), (29, 28), tn[2], 3)
    c.line((24, 23), (28, 27), tn[3])
    c.rect(28, 28, 30, 30, rgb('#c87a3a'))
    c.set(28, 28, rgb('#ffc890'))
    c.line((6, 24), (3, 28), tn[2], 3)
    c.rect(1, 28, 3, 30, rgb('#c87a3a'))
    c.set(1, 28, rgb('#ffc890'))
    c.outline(tn[0])
    return c


def carbon():
    return weave(ramp(['#05050a', '#14141c', '#262630', '#40404e', '#70708a']), 1)


def kevlar():
    return weave(ramp(['#4a3800', '#9a7a10', '#d8b020', '#f4d848', '#fff6b0']), 2)


# ------------------------------------------------------------------ сборка
METALS = {
    'tin': 'tin', 'bronze': 'bronze', 'iron': 'iron', 'copper': 'copper', 'gold': 'gold', 'lead': 'lead',
    'silver': 'silver', 'aluminum': 'aluminum', 'nickel': 'nickel', 'steel': 'steel',
}


def build_all():
    out = {}
    seed = 100
    for name, key in METALS.items():
        rp = R[key]
        seed += 1
        out[f'{name}_ingot'] = ingot(rp)
        out[f'{name}_plate'] = plate(rp)
        out[f'{name}_dust'] = dust(rp, seed)
        out[f'raw_{name}'] = raw_chunk(rp, seed * 3)
    out['uranium_ingot'] = ingot(R['uranium'], rgb('#58ff70'))

    out['basic_circuit'] = circuit(False)
    out['advanced_circuit'] = circuit(True)

    out['blade_wood'] = blade(R['wood'], 'wood', 1)
    out['blade_tin'] = blade(R['tin'], 'metal', 2)
    out['blade_bronze'] = blade(R['bronze'], 'metal', 3)
    out['blade_iron'] = blade(R['iron'], 'metal', 4)
    out['blade_aluminum'] = blade(R['aluminum'], 'metal', 5)
    out['blade_advanced'] = blade(ramp(['#042a1e', '#0c6a4a', '#18b078', '#4cf0b0', '#d0fff0']), 'advanced', 6)

    out['crude_oil'] = vial(ramp(['#000000', '#0c0a08', '#1c1814', '#34302a', '#5a5448']), R['rubber'], 1)
    out['gasoline'] = vial(ramp(['#5a3a00', '#c08a10', '#f0c030', '#ffe670', '#fff8c8']), R['bronze'], 2)
    out['lubricant'] = vial(ramp(['#3a3000', '#8a7a10', '#c8b428', '#e8d860', '#fff8b0']), R['iron'], 3)
    out['mazut'] = vial(ramp(['#1a0a04', '#4a2410', '#6e3a1c', '#8c5430', '#b07a50']), R['rubber'], 4)
    out['rocket_fuel'] = vial(ramp(['#5a0c00', '#c02a08', '#ff6a20', '#ffa850', '#ffe8c0']), R['steel'], 5, True)
    out['sulfuric_acid'] = vial(ramp(['#304a00', '#78a000', '#b4e000', '#e0ff48', '#faffb0']), R['soot'], 6, True)

    out['carbon_fiber'] = carbon()
    out['kevlar'] = kevlar()
    out['plastic'] = plastic_item()
    out['rubber'] = rubber_item()
    out['lead_foam'] = lead_foam()
    out['glassed_waste'] = glassed_waste()
    out['spent_fuel'] = spent_fuel()
    out['soot'] = soot_item()

    out['module_battery'] = module_battery()
    out['module_filter'] = module_filter()
    out['module_plating'] = module_plating()
    out['module_servo'] = module_servo()

    out['energy_meter'] = energy_meter()
    out['structure_wrench'] = wrench()
    out['quest_book'] = quest_book()
    out['tin_cable'] = tin_cable_item()
    out['exo_chestplate'] = chest(ramp(['#0a1018', '#202c3c', '#38485e', '#566a84', '#8aa4c0']), None, rgb('#40e8ff'), 'exo')
    out['hazmat_chestplate'] = chest(ramp(['#4a3000', '#a86a08', '#e8941c', '#ffb844', '#ffe0a0']), None, None, 'hazmat')
    return out
