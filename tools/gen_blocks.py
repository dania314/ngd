"""Блоки TechQuest, 32x32."""
import math
import random
from pxlib import *

STEEL = R['steel']
IRON = R['iron']


def plate_base(seed, key='steel', lighten=0.0):
    base = R[key][2]
    if lighten:
        base = shade(base, lighten)
    return metal_plate(seed, base), base


def lamp(c, x, y, col, on=True):
    c.rect(x, y, x + 2, y + 2, shade(col, -0.6) if not on else col)
    if on:
        c.set(x, y, mix(col, (255, 255, 255, 255), 0.7))
        glow(c, x + 1, y + 1, 4, col, 0.35)
    else:
        c.set(x, y, shade(col, -0.3))


def vents(c, x0, y0, x1, y1, base, n=4):
    step = max(2, (y1 - y0) // n)
    for y in range(y0, y1 + 1, step):
        c.hline(x0, x1, y, shade(base, -0.5))
        c.hline(x0, x1, y + 1, shade(base, 0.2)) if y + 1 <= y1 else None


# ================================================================== руды
def stone_base(seed, deep=False):
    rng = random.Random(seed)
    c = Cv()
    n1 = tile_noise(seed, 8)
    n2 = tile_noise(seed + 1, 4)
    if deep:
        pal = ['#17181c', '#1f2025', '#272830', '#31323a', '#3c3d46']
    else:
        pal = ['#5a5a60', '#6a6a70', '#7a7a80', '#8c8c92', '#9e9ea4']
    pal = [rgb(x) for x in pal]
    for y in range(S):
        for x in range(S):
            v = n1[y][x] * 0.55 + n2[y][x] * 0.3 + rng.random() * 0.25
            idx = min(4, max(0, int(v * 5.2 - 0.4)))
            c.set(x, y, pal[idx])
    # трещинки
    for _ in range(3):
        x, y = rng.randint(0, 31), rng.randint(0, 31)
        for i in range(rng.randint(4, 9)):
            c.set(x % 32, y % 32, pal[0])
            x += rng.choice([1, 1, 0])
            y += rng.choice([0, 1, 1, -1])
    if deep:
        # слоистость глубинного сланца
        for y in range(4, 32, 8):
            for x in range(32):
                if rng.random() < 0.5:
                    c.set(x, y, shade(c.get(x, y), 0.12))
    return c, pal


def ore(key, seed, deep=False, accent=None):
    rp = R[key]
    c, pal = stone_base(seed, deep)
    rng = random.Random(seed * 7)
    centers = []
    tries = 0
    while len(centers) < 6 and tries < 80:
        tries += 1
        x, y = rng.randint(3, 28), rng.randint(3, 28)
        if all(abs(x - a) + abs(y - b) > 9 for a, b in centers):
            centers.append((x, y))
    for (cx, cy) in centers:
        w, h = rng.randint(3, 5), rng.randint(3, 4)
        cells = []
        for yy in range(h):
            for xx in range(w):
                if rng.random() < 0.82 or (xx, yy) == (w // 2, h // 2):
                    # скруглить углы
                    if (xx in (0, w - 1)) and (yy in (0, h - 1)) and rng.random() < 0.7:
                        continue
                    cells.append((xx, yy))
        S_cells = set(cells)
        for (xx, yy) in cells:
            px, py = (cx + xx - w // 2) % 32, (cy + yy - h // 2) % 32
            up = (xx, yy - 1) not in S_cells
            lf = (xx - 1, yy) not in S_cells
            dn = (xx, yy + 1) not in S_cells
            rt = (xx + 1, yy) not in S_cells
            if up and lf:
                col = rp[4]
            elif up or lf:
                col = rp[3]
            elif dn and rt:
                col = rp[1]
            elif dn or rt:
                col = mix(rp[1], rp[2], 0.5)
            else:
                col = rp[2]
            c.set(px, py, col)
        # тень на камне
        for (xx, yy) in cells:
            if (xx, yy + 1) not in S_cells:
                px, py = (cx + xx - w // 2) % 32, (cy + yy - h // 2 + 1) % 32
                c.set(px, py, shade(c.get(px, py), -0.45))
    if accent:
        for _ in range(8):
            x, y = rng.randint(0, 31), rng.randint(0, 31)
            c.set(x, y, accent)
    # фаска блока (чуть темнее по краю, как у ванильной руды)
    return c


ORE_COLORS = {
    'tin': ramp(['#3a4a5e', '#7a92aa', '#b4cade', '#dcecf8', '#ffffff']),
    'lead': ramp(['#1a1c34', '#3a3e70', '#5a60a0', '#7e86c8', '#b0b8f0']),
    'silver': ramp(['#4a5260', '#98a2b4', '#d0d8e4', '#f0f4fa', '#ffffff']),
    'aluminum': ramp(['#5a2e20', '#a8604a', '#d89478', '#f0c0a8', '#fff0e4']),
    'nickel': ramp(['#1e4a34', '#3a8a60', '#62c08c', '#98e8b8', '#d8fff0']),
}


def build_ores():
    out = {}
    for i, (name, rp) in enumerate(ORE_COLORS.items()):
        R['ore_' + name] = rp
        out[f'{name}_ore'] = ore('ore_' + name, 10 + i, False)
        out[f'deepslate_{name}_ore'] = ore('ore_' + name, 40 + i, True)
    return out


# ================================================================== корпус и машины
def machine_casing():
    c, base = plate_base(1, 'steel', 0.05)
    # швы панелей
    for p in (10, 21):
        c.hline(2, 29, p, shade(base, -0.35))
        c.hline(2, 29, p + 1, shade(base, 0.2))
        c.vline(p, 2, 29, shade(base, -0.35))
        c.vline(p + 1, 2, 29, shade(base, 0.2))
    # центральная пластина
    c.rect(12, 12, 19, 19, shade(base, -0.06))
    bevel(c, 12, 12, 19, 19, base, False, 0.2)
    rivet(c, 14, 14, base)
    for (x, y) in ((3, 3), (27, 3), (3, 27), (27, 27)):
        pass
    return c


def furnace_front(on, kind='coal'):
    c, base = plate_base(2 if kind == 'coal' else 3, 'iron', -0.12)
    # арочное отверстие
    c.rect(8, 10, 23, 24, rgb('#0c0a0a'))
    c.poly([(8, 10), (23, 10), (23, 8), (8, 8)], base)
    c.ellipse(8, 6, 23, 16, rgb('#0c0a0a'))
    c.rect(8, 12, 23, 24, rgb('#0c0a0a'))
    bevel(c, 7, 8, 24, 25, base, False, 0.4)
    # перемычка колосника
    if kind == 'coal':
        if on:
            # пламя
            fl = [rgb('#7a1800'), rgb('#d83a00'), rgb('#ff7a10'), rgb('#ffb830'), rgb('#fff0a0')]
            rng = random.Random(9)
            for x in range(9, 23):
                h = int(6 + 5 * math.sin(x * 0.9) ** 2 + rng.randint(0, 2))
                for y in range(23, 23 - h, -1):
                    t = (23 - y) / h
                    c.set(x, y, fl[min(4, int(t * 4.2))] if t < 0.95 else fl[1])
            glow(c, 16, 20, 9, rgb('#ff8020'), 0.35)
            c.rect(9, 24, 22, 24, rgb('#d8a020'))
        else:
            # остывшие угли
            rng = random.Random(4)
            for x in range(9, 23, 2):
                y = 22 + rng.randint(-1, 1)
                c.rect(x, y, x + 2, 24, rgb('#1c1c20'))
                c.set(x, y, rgb('#34343c'))
            c.set(14, 23, rgb('#6a2410'))
            c.set(18, 22, rgb('#7a2c10'))
        lamp(c, 14, 3, rgb('#ff8a20'), on)
    else:
        # электропечь: нагревательные спирали
        col_on = [rgb('#ff4a10'), rgb('#ff8a30'), rgb('#ffd070')]
        col_off = [rgb('#3a2220'), rgb('#52302a'), rgb('#6a4038')]
        cols = col_on if on else col_off
        for y in (13, 16, 19, 22):
            for x in range(9, 23):
                zig = (x // 2 + y) % 2
                c.set(x, y + (1 if zig else 0), cols[1])
            c.hline(9, 22, y, cols[2] if on else cols[1])
            c.hline(9, 22, y + 1, cols[0])
        if on:
            glow(c, 16, 18, 10, rgb('#ff6a20'), 0.3)
        lamp(c, 14, 3, rgb('#4af0ff'), on)
    # нижняя решётка
    vents(c, 9, 27, 22, 29, base, 2)
    return c


def macerator_front(on):
    c, base = plate_base(5, 'iron', -0.08)
    c.rect(7, 7, 24, 22, rgb('#0a0a0e'))
    bevel(c, 6, 6, 25, 23, base, False, 0.4)
    # два зубчатых вала
    rot = 0.0 if not on else 0.25
    gear(c, 12, 15, 6, 6, IRON[2], True, rot)
    gear(c, 20, 15, 6, 6, IRON[3], True, rot + 0.5)
    # дробимый материал
    if on:
        for (x, y) in ((11, 20), (15, 21), (19, 20), (23, 20), (9, 21)):
            c.set(x, y, rgb('#d8c8a0'))
        for (x, y) in ((16, 9), (14, 10), (18, 8)):
            c.set(x, y, rgb('#ffe090'))
        glow(c, 16, 15, 9, rgb('#ffcc60'), 0.18)
    lamp(c, 14, 25, rgb('#38ff60'), on)
    vents(c, 4, 3, 10, 4, base, 1)
    return c


def compressor_front(on):
    c, base = plate_base(6, 'iron', -0.06)
    c.rect(7, 6, 24, 24, rgb('#0a0c10'))
    bevel(c, 6, 5, 25, 25, base, False, 0.4)
    # цилиндр поршня
    c.rect(13, 7, 18, 12, STEEL[3])
    c.vline(13, 7, 12, STEEL[4])
    c.vline(18, 7, 12, STEEL[1])
    y_press = 17 if on else 12
    c.rect(15, 12, 16, y_press, IRON[4])
    c.vline(15, 12, y_press, IRON[4])
    c.vline(16, 12, y_press, IRON[2])
    # плита пресса
    c.rect(9, y_press, 22, y_press + 2, IRON[3])
    c.hline(9, 22, y_press, IRON[4])
    c.hline(9, 22, y_press + 2, IRON[1])
    # наковальня
    c.rect(8, 21, 23, 23, STEEL[2])
    c.hline(8, 23, 21, STEEL[3])
    c.hline(8, 23, 23, STEEL[0])
    if on:
        c.hline(10, 21, 20, rgb('#ffe070'))
        glow(c, 16, 20, 7, rgb('#ffd040'), 0.25)
    lamp(c, 14, 27, rgb('#ffc020'), on)
    return c


def alloy_smelter():
    c, base = plate_base(7, 'bronze', -0.28)
    br = R['bronze']
    # два приёмных лотка со слитками (медь и олово) и жёлоб между ними
    for x0, rp in ((4, R['copper']), (19, R['tin'])):
        c.rect(x0, 5, x0 + 8, 9, rgb('#0e0a08'))
        bevel(c, x0 - 1, 4, x0 + 9, 10, base, False, 0.4)
        c.rect(x0 + 1, 6, x0 + 7, 8, rp[2])
        c.hline(x0 + 1, x0 + 7, 6, rp[4])
        c.hline(x0 + 1, x0 + 7, 8, rp[1])
    c.rect(14, 6, 17, 8, IRON[2])
    c.hline(14, 17, 6, IRON[4])
    c.hline(14, 17, 8, IRON[0])
    c.vline(15, 9, 12, rgb('#ff9a20'))
    c.vline(16, 9, 12, rgb('#ffd060'))
    # лючок плавильни
    c.rect(7, 14, 24, 26, rgb('#120804'))
    bevel(c, 6, 13, 25, 27, base, False, 0.4)
    g = [rgb('#7a2000'), rgb('#e04a00'), rgb('#ff9a20'), rgb('#ffe070')]
    for x in range(8, 24):
        h = 3 + (x * 7) % 4
        for y in range(25, 25 - h, -1):
            c.set(x, y, g[min(3, (25 - y) * 4 // h)])
    glow(c, 16, 22, 10, rgb('#ff8020'), 0.3)
    # заслонка
    c.hline(8, 23, 16, rgb('#3a2418'))
    c.hline(8, 23, 17, rgb('#24160e'))
    return c


def assembly_line():
    c, base = plate_base(8, 'steel', -0.05)
    # лента конвейера
    c.rect(2, 19, 29, 25, rgb('#16161c'))
    for x in range(2, 30, 3):
        c.vline(x, 19, 25, rgb('#2a2a34'))
    c.hline(2, 29, 19, rgb('#46465a'))
    c.hline(2, 29, 25, rgb('#08080c'))
    # рука-манипулятор
    yl = ramp(['#4a3a00', '#a88400', '#e8b800', '#ffd830', '#fff090'])
    c.rect(6, 5, 11, 8, yl[2])
    bevel(c, 6, 5, 11, 8, yl[2], True, 0.3)
    c.line((9, 8), (16, 13), yl[2], 2)
    c.line((16, 13), (22, 11), yl[3], 2)
    c.ellipse(15, 12, 18, 15, yl[1])
    c.set(16, 12, yl[4])
    # захват
    c.rect(21, 11, 23, 17, IRON[2])
    c.hline(20, 24, 17, IRON[4])
    # деталь на ленте
    c.rect(20, 20, 25, 24, rgb('#c87a3a'))
    c.hline(20, 25, 20, rgb('#ffc890'))
    lamp(c, 25, 4, rgb('#38ff60'), True)
    return c


def blast_furnace():
    c = Cv()
    # огнеупорный кирпич
    br = ramp(['#2a0c08', '#5a2418', '#8a3c28', '#b05a40', '#d88060'])
    rng = random.Random(10)
    c.fill(br[1])
    for row in range(0, 32, 5):
        off = 0 if (row // 5) % 2 == 0 else 5
        c.hline(0, 31, row, br[0])
        for x in range(-off, 32, 10):
            c.vline(x % 32, row, min(31, row + 4), br[0])
            for yy in range(row + 1, min(32, row + 5)):
                for xx in range(x + 1, x + 10):
                    if 0 <= xx < 32:
                        v = rng.random()
                        c.set(xx, yy, br[2] if v > 0.35 else (br[3] if v > 0.1 else br[1]))
            for xx in range(x + 1, x + 10):
                if 0 <= xx < 32 and row + 1 < 32:
                    c.set(xx, row + 1, br[3])
    # стальная рама
    for (x0, y0, x1, y1) in ((0, 0, 31, 2), (0, 29, 31, 31), (0, 0, 2, 31), (29, 0, 31, 31)):
        c.rect(x0, y0, x1, y1, STEEL[2])
        bevel(c, x0, y0, x1, y1, STEEL[2], True, 0.3)
    # топка
    c.ellipse(9, 9, 22, 24, rgb('#0a0402'))
    c.rect(9, 17, 22, 25, rgb('#0a0402'))
    bevel(c, 8, 12, 23, 26, STEEL[2], False, 0.3)
    fl = [rgb('#7a1800'), rgb('#e04a00'), rgb('#ff9a20'), rgb('#ffe890')]
    for x in range(10, 22):
        h = 5 + (x % 4)
        for y in range(25, 25 - h, -1):
            c.set(x, y, fl[min(3, (25 - y) * 4 // h)])
    glow(c, 16, 20, 9, rgb('#ff7a20'), 0.35)
    rivet(c, 3, 3, STEEL[2])
    rivet(c, 26, 3, STEEL[2])
    return c


def boiler():
    c, base = plate_base(11, 'copper', -0.12)
    cp = R['copper']
    # горизонтальные пояса бака
    for y in (7, 24):
        c.rect(1, y, 30, y + 2, cp[1])
        c.hline(1, 30, y, cp[3])
        c.hline(1, 30, y + 2, cp[0])
        for x in range(4, 30, 6):
            c.set(x, y + 1, cp[4])
    # смотровое стекло
    c.rect(12, 11, 19, 21, rgb('#08202e'))
    bevel(c, 11, 10, 20, 22, cp[2], False, 0.4)
    for y in range(15, 22):
        c.hline(12, 19, y, rgb('#2a78c8') if y > 16 else rgb('#58a8ec'))
    c.hline(12, 19, 15, rgb('#a8dcff'))
    c.vline(13, 12, 20, rgb('#ffffff'))
    # манометр
    c.ellipse(4, 11, 9, 16, rgb('#e8e0c8'))
    c.ellipse(4, 11, 9, 16, rgb('#e8e0c8'))
    c.line((6, 14), (8, 12), rgb('#c01818'))
    c.outline(cp[0]) if False else None
    c.set(6, 14, rgb('#202020'))
    # вентиль
    c.rect(23, 11, 27, 13, IRON[2])
    c.rect(24, 13, 26, 17, IRON[3])
    return c


def chemical_reactor():
    c, base = plate_base(12, 'steel', -0.1)
    # колба в окне
    c.rect(7, 5, 24, 26, rgb('#0a0a14'))
    bevel(c, 6, 4, 25, 27, base, False, 0.4)
    pu = ramp(['#2a0848', '#5a1a98', '#8a38d8', '#b868f8', '#e8c8ff'])
    for y in range(12, 26):
        for x in range(8, 24):
            t = (y - 12) / 14
            c.set(x, y, mix(pu[3], pu[1], t))
    # волна
    for x in range(8, 24):
        c.set(x, 12 + (1 if (x // 3) % 2 else 0), pu[4])
    rng = random.Random(3)
    for _ in range(9):
        x, y = rng.randint(9, 22), rng.randint(14, 24)
        c.set(x, y, pu[4])
        c.set(x + 1, y, pu[3])
    glow(c, 16, 20, 9, rgb('#c070ff'), 0.25)
    # стеклянный блик
    c.vline(9, 6, 24, rgb('#4a5878'))
    c.vline(10, 7, 12, rgb('#8898b8'))
    # трубки сверху
    c.rect(11, 1, 12, 4, IRON[2])
    c.rect(19, 1, 20, 4, IRON[2])
    return c


def cooling_tower():
    c = Cv()
    cn = ramp(['#4a4a4a', '#78787a', '#9a9a9c', '#b8b8ba', '#d8d8da'])
    rng = random.Random(14)
    n = tile_noise(14, 4)
    for y in range(32):
        for x in range(32):
            v = n[y][x] + rng.random() * 0.3
            c.set(x, y, cn[1] if v < 0.5 else (cn[2] if v < 0.8 else cn[3]))
    # вертикальные рёбра бетона
    for x in range(0, 32, 8):
        c.vline(x, 0, 31, cn[0])
        c.vline(x + 1, 0, 31, cn[4])
    # горизонтальные пояса
    for y in (0, 15, 31):
        c.hline(0, 31, y, cn[0])
        c.hline(0, 31, y + 1 if y < 31 else y, cn[4])
    # паровые отверстия
    for x in range(4, 30, 8):
        for y in (6, 22):
            c.rect(x, y, x + 3, y + 5, rgb('#101214'))
            bevel(c, x - 1, y - 1, x + 4, y + 6, cn[2], False, 0.3)
            for i in range(3):
                c.set(x + 1 + (i % 2), y + 4 - i * 2, rgb('#e8eef4'))
    return c


def drill_controller():
    c, base = plate_base(15, 'steel', -0.05)
    yl = ramp(['#4a3a00', '#a88400', '#e8b800', '#ffd830', '#fff090'])
    stripes(c, 2, 2, 29, 5, yl[2], rgb('#1a1a1a'), 3)
    stripes(c, 2, 26, 29, 29, yl[2], rgb('#1a1a1a'), 3)
    screen(c, 7, 9, 24, 20, rgb('#40ff90'), base)
    # значок бура на экране
    g = rgb('#80ffc0')
    for y in range(11, 19):
        c.set(15 + (1 if y % 2 else 0), y, g)
        c.set(16 + (1 if y % 2 else 0), y, g)
    c.rect(13, 18, 18, 19, g)
    c.hline(11, 20, 11, g)
    glow(c, 16, 15, 9, rgb('#40ff90'), 0.2)
    # кнопки
    for x, col in ((8, '#e03030'), (14, '#3060e0'), (20, '#30d060')):
        c.rect(x, 22, x + 3, 24, rgb(col))
        c.set(x, 22, mix(rgb(col), rgb('#ffffff'), 0.6))
    return c


def drill_frame():
    c = Cv()
    yl = ramp(['#3a2e00', '#8a6c00', '#d8a800', '#ffd020', '#fff090'])
    c.fill(rgb('#16161a'))
    # рамка из профиля
    c.rect(0, 0, 31, 4, yl[2])
    c.rect(0, 27, 31, 31, yl[2])
    c.rect(0, 0, 4, 31, yl[2])
    c.rect(27, 0, 31, 31, yl[2])
    stripes(c, 0, 0, 31, 4, yl[2], rgb('#1a1a1a'), 4)
    stripes(c, 0, 27, 31, 31, yl[2], rgb('#1a1a1a'), 4)
    # крестовые раскосы
    for i in range(5, 27):
        for (x, y) in ((i, i), (i, 31 - i)):
            c.set(x, y, yl[2])
            c.set(x + 1, y, yl[3])
            c.set(x, y + 1, yl[1])
    c.rect(13, 13, 18, 18, yl[2])
    bevel(c, 13, 13, 18, 18, yl[2], True, 0.4)
    c.set(15, 15, yl[0])
    c.set(16, 16, yl[0])
    for (x, y) in ((1, 1), (28, 1), (1, 28), (28, 28)):
        c.set(x, y, yl[4])
        c.set(x + 1, y + 1, yl[0])
    return c


def drill_head():
    c, base = plate_base(17, 'steel', -0.18)
    # спиральное сверло по диагоналям
    for i in range(-4, 36, 1):
        for k in range(4):
            x, y = i + k, k * 0 + 0
    for y in range(32):
        for x in range(32):
            ph = (x + y * 1) % 8
            if ph < 2:
                c.set(x, y, STEEL[4])
            elif ph < 4:
                c.set(x, y, STEEL[3])
            elif ph < 6:
                c.set(x, y, STEEL[2])
            else:
                c.set(x, y, STEEL[0])
    # центральный конус
    c.ellipse(8, 8, 23, 23, STEEL[1])
    c.ellipse(10, 10, 21, 21, STEEL[2])
    c.ellipse(13, 13, 18, 18, STEEL[3])
    c.set(14, 14, STEEL[4])
    bevel(c, 0, 0, 31, 31, STEEL[2], True, 0.3)
    return c


def geiger():
    yl = ramp(['#4a3600', '#a07800', '#e8b820', '#ffd850', '#fff2a0'])
    c = metal_plate(18, yl[2])
    # трилистник
    c.ellipse(9, 6, 22, 19, rgb('#1a1400'))
    c.ellipse(10, 7, 21, 18, rgb('#ffd820'))
    for a in (90, 210, 330):
        ar = math.radians(a)
        pts = [(15.5, 12.5)]
        for da in (-0.55, 0.55):
            pts.append((15.5 + math.cos(ar + da) * 5.6, 12.5 - math.sin(ar + da) * 5.6))
        c.poly(pts, rgb('#1a1400'))
    c.ellipse(14, 11, 17, 14, rgb('#ffd820'))
    c.ellipse(15, 12, 16, 13, rgb('#1a1400'))
    # шкала
    c.rect(6, 22, 25, 27, rgb('#101810'))
    bevel(c, 5, 21, 26, 28, yl[2], False, 0.4)
    for x in range(7, 25, 3):
        c.vline(x, 23, 24, rgb('#58ff98'))
    c.line((15, 27), (21, 23), rgb('#ff4040'))
    return c


def greenhouse():
    c = Cv()
    gl = ramp(['#305058', '#68a0a8', '#a0d8e0', '#d0f0f4', '#ffffff'])
    c.fill(gl[2])
    # внутри растения
    gr = ramp(['#0a3a10', '#1a7a20', '#38b840', '#70e060', '#c0ffa0'])
    for x0 in (4, 12, 20):
        c.line((x0 + 3, 29), (x0 + 3, 16), gr[1])
        c.ellipse(x0, 12, x0 + 6, 18, gr[2])
        c.ellipse(x0 + 1, 12, x0 + 4, 15, gr[3])
        c.ellipse(x0 + 2, 19, x0 + 7, 23, gr[2])
    # земля
    c.rect(2, 27, 29, 29, rgb('#5a3a20'))
    c.hline(2, 29, 27, rgb('#7a5030'))
    # переплёт
    wh = R['aluminum']
    c.rect(0, 0, 31, 1, wh[3])
    c.rect(0, 30, 31, 31, wh[1])
    c.rect(0, 0, 1, 31, wh[3])
    c.rect(30, 0, 31, 31, wh[1])
    c.rect(15, 0, 16, 31, wh[2])
    c.rect(0, 14, 31, 15, wh[2])
    # блики стекла
    for (x, y) in ((4, 4), (5, 5), (6, 6), (20, 3), (21, 4), (22, 5)):
        c.set(x, y, gl[4])
    return c


def laser_drill():
    c, base = plate_base(20, 'steel', -0.22)
    c.ellipse(5, 5, 26, 26, rgb('#101018'))
    for r, col in ((10, STEEL[1]), (8, STEEL[2]), (6, rgb('#400808'))):
        c.ellipse(16 - r, 16 - r, 16 + r, 16 + r, col)
    c.ellipse(11, 11, 20, 20, rgb('#c01818'))
    c.ellipse(13, 13, 18, 18, rgb('#ff5030'))
    c.ellipse(14, 14, 17, 17, rgb('#ffd8a0'))
    c.set(13, 12, rgb('#ffffff'))
    glow(c, 16, 16, 14, rgb('#ff3020'), 0.4)
    for (x, y) in ((16, 3), (16, 27), (3, 16), (27, 16)):
        c.rect(x - 1, y - 1, x + 1, y + 1, STEEL[3])
    return c


def nuclear():
    c, base = plate_base(21, 'lead', 0.05)
    yl = rgb('#e8c010')
    stripes(c, 2, 2, 29, 4, yl, rgb('#1a1a1a'), 3)
    stripes(c, 2, 27, 29, 29, yl, rgb('#1a1a1a'), 3)
    c.rect(8, 7, 23, 24, rgb('#04140a'))
    bevel(c, 7, 6, 24, 25, base, False, 0.4)
    gcol = ramp(['#0a4a14', '#18a828', '#40e050', '#98ff80', '#e8ffd8'])
    for r, col in ((8, gcol[0]), (6, gcol[1]), (4, gcol[2]), (2, gcol[3])):
        c.ellipse(16 - r, 15 - r, 16 + r, 15 + r, col)
    c.set(15, 14, gcol[4])
    glow(c, 16, 15, 12, rgb('#40ff60'), 0.45)
    return c


def oil_deposit():
    c, pal = stone_base(23, True)
    rng = random.Random(23)
    ol = ramp(['#000000', '#08080a', '#101014', '#1c1c24', '#34343e'])
    for _ in range(7):
        cx, cy = rng.randint(3, 28), rng.randint(3, 28)
        rx, ry = rng.randint(3, 6), rng.randint(2, 4)
        for y in range(cy - ry, cy + ry + 1):
            for x in range(cx - rx, cx + rx + 1):
                if ((x - cx) / rx) ** 2 + ((y - cy) / ry) ** 2 <= 1 + rng.random() * 0.2:
                    c.set(x % 32, y % 32, ol[1] if ((x + y) % 5) else ol[2])
        # блик
        c.set((cx - rx // 2) % 32, (cy - 1) % 32, ol[4])
        c.set((cx - rx // 2 + 1) % 32, (cy - 1) % 32, ol[3])
        # радужная плёнка
        c.set((cx + 1) % 32, (cy + 1) % 32, rgb('#6a3a8a'))
        c.set((cx + 2) % 32, (cy + 1) % 32, rgb('#2a6a7a'))
    return c


def quarry():
    c, base = plate_base(24, 'steel', -0.15)
    yl = rgb('#e8b800')
    stripes(c, 2, 2, 29, 5, yl, rgb('#1a1a1a'), 3)
    stripes(c, 2, 26, 29, 29, yl, rgb('#1a1a1a'), 3)
    c.rect(7, 8, 24, 23, rgb('#101018'))
    bevel(c, 6, 7, 25, 24, base, False, 0.4)
    # кирка
    c.line((10, 21), (21, 10), rgb('#8a5a2c'), 2)
    c.line((9, 22), (20, 11), rgb('#b07c42'))
    c.poly([(13, 9), (20, 8), (24, 12), (23, 15), (21, 12), (16, 11)], IRON[3])
    c.line((13, 9), (20, 8), IRON[4])
    c.line((20, 8), (24, 12), IRON[4])
    c.line((16, 11), (21, 12), IRON[1])
    # осколки
    c.set(9, 11, rgb('#a0a0a8'))
    c.set(11, 9, rgb('#7a7a84'))
    return c


def refinery():
    c, base = plate_base(25, 'steel', -0.1)
    # три трубы-колонны
    for x0 in (5, 13, 21):
        c.rect(x0, 2, x0 + 5, 29, IRON[2])
        c.vline(x0, 2, 29, IRON[4])
        c.vline(x0 + 1, 2, 29, IRON[3])
        c.vline(x0 + 5, 2, 29, IRON[1])
        c.vline(x0 + 4, 2, 29, mix(IRON[1], IRON[2], 0.5))
        for y in (7, 14, 21):
            c.rect(x0 - 1, y, x0 + 6, y + 2, STEEL[1])
            c.hline(x0 - 1, x0 + 6, y, STEEL[3])
            c.hline(x0 - 1, x0 + 6, y + 2, STEEL[0])
    # перемычки труб
    c.rect(10, 10, 12, 11, IRON[3])
    c.rect(18, 17, 20, 18, IRON[3])
    c.rect(10, 24, 12, 25, IRON[3])
    # вентильные «глазки»
    for (x, y, col) in ((7, 11, '#ff6030'), (15, 18, '#40e0ff'), (23, 25, '#ffd040')):
        c.set(x, y, rgb(col))
        c.set(x + 1, y, rgb(col))
    return c


def rtg():
    c, base = plate_base(26, 'soot', 0.9)
    base = R['soot'][3]
    c.fill(R['soot'][2])
    # рёбра радиатора
    for y in range(2, 30, 3):
        c.hline(2, 29, y, R['steel'][3])
        c.hline(2, 29, y + 1, R['steel'][2])
        c.hline(2, 29, y + 2, R['steel'][0])
    # светящееся ядро
    c.rect(10, 10, 21, 21, rgb('#04120a'))
    bevel(c, 9, 9, 22, 22, R['steel'][2], False, 0.4)
    gc = ramp(['#0a4a14', '#18a828', '#40e050', '#98ff80', '#e8ffd8'])
    c.rect(12, 12, 19, 19, gc[1])
    c.rect(13, 13, 18, 18, gc[2])
    c.rect(14, 14, 17, 17, gc[3])
    c.set(14, 14, gc[4])
    glow(c, 16, 16, 11, rgb('#40ff60'), 0.45)
    return c


def safety_valve():
    c, base = plate_base(27, 'iron', -0.1)
    # труба
    c.rect(0, 13, 31, 18, STEEL[2])
    c.hline(0, 31, 13, STEEL[4])
    c.hline(0, 31, 14, STEEL[3])
    c.hline(0, 31, 18, STEEL[0])
    # красный маховик
    rd = ramp(['#3a0404', '#8a1010', '#d02020', '#f05848', '#ffa090'])
    c.ellipse(7, 3, 24, 20, rd[1])
    c.ellipse(9, 5, 22, 18, rd[2])
    c.ellipse(12, 8, 19, 15, rd[1])
    c.ellipse(13, 9, 18, 14, rd[2])
    for k in range(6):
        a = k * math.pi / 3
        c.line((15, 11), (int(15 + math.cos(a) * 7), int(11 + math.sin(a) * 7)), rd[1])
    c.ellipse(13, 9, 17, 13, rd[3])
    c.set(14, 10, rd[4])
    # фланцы
    for x in (3, 26):
        c.rect(x, 10, x + 2, 21, STEEL[3])
        c.vline(x, 10, 21, STEEL[4])
        c.vline(x + 2, 10, 21, STEEL[1])
    # табличка
    c.rect(11, 24, 20, 28, rgb('#e8c010'))
    c.hline(11, 20, 24, rgb('#fff090'))
    c.hline(12, 19, 26, rgb('#3a2e00'))
    return c


def scrubber():
    c, base = plate_base(28, 'steel', -0.2)
    c.rect(6, 6, 25, 25, rgb('#0a0e0c'))
    bevel(c, 5, 5, 26, 26, base, False, 0.4)
    gr = ramp(['#0a3a1a', '#1a7a38', '#38c060', '#78f0a0', '#d0ffe0'])
    for y in range(7, 25):
        for x in range(7, 25):
            if (x + y) % 4 == 0:
                c.set(x, y, gr[2])
            elif (x - y) % 4 == 0:
                c.set(x, y, gr[1])
            else:
                c.set(x, y, rgb('#06140c'))
    for k in range(7, 25, 6):
        c.hline(7, 24, k, STEEL[3])
        c.hline(7, 24, k + 1, STEEL[0])
    glow(c, 16, 16, 10, rgb('#40ff80'), 0.2)
    lamp(c, 14, 27, rgb('#40ff80'), True)
    return c


def solar_top():
    c = Cv()
    bl = ramp(['#04102a', '#0c2a6a', '#1c4aa8', '#3a78d8', '#8ab8ff'])
    c.fill(R['aluminum'][1])
    cells = [(2, 2), (17, 2), (2, 17), (17, 17)]
    for (x0, y0) in cells:
        for y in range(y0, y0 + 13):
            for x in range(x0, x0 + 13):
                t = ((x - x0) + (y - y0)) / 24
                col = mix(bl[3], bl[1], t)
                if (x + y) % 5 == 0:
                    col = shade(col, 0.07)
                c.set(x, y, col)
        # шины
        for k in (4, 8):
            c.vline(x0 + k, y0, y0 + 12, rgb('#c8d4e4'))
        c.hline(x0, x0 + 12, y0 + 6, shade(bl[3], 0.1))
        # блик
        c.line((x0 + 1, y0 + 3), (x0 + 3, y0 + 1), bl[4])
        bevel(c, x0 - 1, y0 - 1, x0 + 13, y0 + 13, R['aluminum'][1], False, 0.2)
    c.hline(0, 31, 0, R['aluminum'][3])
    c.vline(0, 0, 31, R['aluminum'][3])
    return c


def sprinkler():
    c, base = plate_base(30, 'steel', -0.12)
    # корпус-пьедестал
    c.rect(11, 18, 20, 28, IRON[2])
    bevel(c, 11, 18, 20, 28, IRON[2], True, 0.3)
    # головка-форсунка
    c.ellipse(9, 8, 22, 20, STEEL[2])
    c.ellipse(11, 10, 20, 18, STEEL[3])
    c.ellipse(14, 12, 17, 15, rgb('#101820'))
    c.set(13, 11, STEEL[4])
    # струи
    wt = ramp(['#104a8a', '#2a80d0', '#58b0f8', '#a0daff', '#ffffff'])
    for (x, y) in ((6, 6), (4, 10), (25, 6), (27, 10), (15, 3), (9, 3), (22, 3)):
        c.rect(x, y, x + 1, y + 1, wt[2])
        c.set(x, y, wt[4])
    c.line((8, 9), (5, 5), wt[3])
    c.line((23, 9), (26, 5), wt[3])
    c.line((16, 8), (16, 4), wt[3])
    return c


def transformer():
    c, base = plate_base(31, 'iron', -0.15)
    # железный сердечник
    c.rect(3, 4, 28, 27, IRON[1])
    c.rect(4, 5, 27, 26, IRON[2])
    c.rect(11, 8, 20, 23, rgb('#0a0a0e'))
    # медные катушки
    cp = R['copper']
    for x0 in (4, 19):
        c.rect(x0, 7, x0 + 8, 24, cp[2])
        for y in range(8, 24, 2):
            c.hline(x0, x0 + 8, y, cp[3])
            c.hline(x0, x0 + 8, y + 1, cp[1])
        c.vline(x0, 7, 24, cp[4])
        c.vline(x0 + 8, 7, 24, cp[0])
    # изоляторы-клеммы
    for x in (7, 22):
        c.rect(x, 1, x + 2, 4, rgb('#e8e0d0'))
        c.hline(x, x + 2, 1, rgb('#ffffff'))
        c.set(x + 2, 3, rgb('#a09888'))
    c.rect(12, 28, 19, 30, rgb('#e8b800'))
    stripes(c, 12, 28, 19, 30, rgb('#e8b800'), rgb('#1a1a1a'), 2)
    return c


def vitrification():
    c, base = plate_base(32, 'steel', -0.05)
    # круглый люк
    c.ellipse(4, 4, 27, 27, STEEL[1])
    c.ellipse(5, 5, 26, 26, STEEL[3])
    c.ellipse(7, 7, 24, 24, STEEL[1])
    c.ellipse(8, 8, 23, 23, rgb('#160a04'))
    # расплавленное стекло
    for y in range(9, 23):
        for x in range(9, 23):
            if (x - 15.5) ** 2 + (y - 15.5) ** 2 < 49:
                d = math.hypot(x - 15.5, y - 15.5) / 7
                c.set(x, y, mix(rgb('#ffe070'), rgb('#e04a00'), d ** 1.5))
    rng = random.Random(33)
    for _ in range(7):
        x, y = rng.randint(11, 20), rng.randint(11, 20)
        c.set(x, y, rgb('#58ff80'))
    glow(c, 16, 16, 12, rgb('#ff7a20'), 0.35)
    # болты по ободу
    for k in range(8):
        a = k * math.pi / 4
        c.set(int(15.5 + math.cos(a) * 10.5), int(15.5 + math.sin(a) * 10.5), STEEL[4])
    return c


def water_wheel():
    c = Cv()
    wd = R['wood']
    rng = random.Random(34)
    for y in range(32):
        plank = y // 8
        for x in range(32):
            col = wd[2] if plank % 2 == 0 else shade(wd[2], -0.08)
            if rng.random() < 0.12:
                col = shade(col, -0.18)
            if rng.random() < 0.06:
                col = shade(col, 0.15)
            c.set(x, y, col)
    for y in range(0, 32, 8):
        c.hline(0, 31, y, wd[0])
        c.hline(0, 31, y + 1, wd[3])
    # железные обручи и ступица
    for y in (3, 27):
        c.hline(0, 31, y, R['iron'][3])
        c.hline(0, 31, y + 1, R['iron'][1])
        for x in (3, 16, 28):
            c.set(x, y, R['iron'][4])
    c.ellipse(9, 9, 22, 22, R['iron'][1])
    c.ellipse(10, 10, 21, 21, R['iron'][2])
    c.ellipse(13, 13, 18, 18, R['bronze'][2])
    c.set(14, 14, R['bronze'][4])
    for k in range(8):
        a = k * math.pi / 4
        c.line((16, 16), (int(16 + math.cos(a) * 9), int(16 + math.sin(a) * 9)), R['iron'][3])
    c.ellipse(13, 13, 18, 18, R['bronze'][2])
    c.set(14, 14, R['bronze'][4])
    c.set(16, 16, R['bronze'][0])
    return c


def wind_turbine():
    c, base = plate_base(35, 'aluminum', 0.08)
    # ступица и три лопасти-логотип
    cx, cy = 15.5, 15.5
    for k in range(3):
        a = math.radians(-90 + k * 120)
        tip = (cx + math.cos(a) * 12, cy + math.sin(a) * 12)
        l = (cx + math.cos(a - 0.35) * 4, cy + math.sin(a - 0.35) * 4)
        r_ = (cx + math.cos(a + 0.35) * 4, cy + math.sin(a + 0.35) * 4)
        c.poly([l, tip, r_], R['tin'][4])
        c.line((int(cx), int(cy)), (int(tip[0]), int(tip[1])), R['tin'][1])
    c.ellipse(11, 11, 20, 20, R['steel'][2])
    c.ellipse(12, 12, 19, 19, R['steel'][3])
    c.ellipse(14, 14, 17, 17, R['steel'][1])
    c.set(13, 13, R['steel'][4])
    c.set(15, 15, rgb('#40d0ff'))
    return c


def battery_side():
    c, base = plate_base(36, 'steel', -0.12)
    yl = ramp(['#5a4000', '#b08000', '#f0c020', '#ffe060', '#fff8c0'])
    c.rect(2, 12, 29, 19, yl[2])
    bevel(c, 2, 12, 29, 19, yl[2], True, 0.3)
    # зарядные сегменты
    for i in range(6):
        x = 4 + i * 4
        col = rgb('#38ff60') if i < 4 else rgb('#1a3a22')
        c.rect(x, 14, x + 2, 17, col)
        if i < 4:
            c.set(x, 14, rgb('#d8ffe0'))
    # молния
    for (x, y) in ((15, 4), (14, 6), (13, 8), (15, 8), (14, 10)):
        c.set(x, y, yl[3])
    c.set(15, 5, yl[3])
    c.set(14, 7, yl[3])
    vents(c, 5, 23, 26, 28, base, 2)
    return c


def battery_top():
    c, base = plate_base(37, 'steel', -0.05)
    # две клеммы
    for x0, col in ((6, '#d83030'), (19, '#3060d8')):
        c.rect(x0, 8, x0 + 6, 20, STEEL[1])
        c.ellipse(x0, 8, x0 + 6, 14, STEEL[3])
        c.ellipse(x0 + 1, 9, x0 + 5, 13, rgb(col))
        c.set(x0 + 2, 10, mix(rgb(col), rgb('#ffffff'), 0.6))
        c.rect(x0, 15, x0 + 6, 19, STEEL[2])
    # знаки + и -
    c.hline(8, 10, 23, rgb('#e04040'))
    c.vline(9, 22, 24, rgb('#e04040'))
    c.hline(21, 23, 23, rgb('#4070e0'))
    return c


def cable(kind):
    cols = {
        'lv': (R['copper'], '#f0a040'),
        'mv': (R['gold'], '#ffe060'),
        'hv': (ramp(['#06203a', '#104a8a', '#2080d0', '#50b4f8', '#c0e8ff']), '#a0e0ff'),
        'tin': (R['tin'], '#e0f0ff'),
    }
    rp, stripe = cols[kind]
    c = Cv()
    for y in range(32):
        # цилиндрическая шейдинг по вертикали
        t = y / 31
        if t < 0.15:
            col = rp[4]
        elif t < 0.35:
            col = rp[3]
        elif t < 0.65:
            col = rp[2]
        elif t < 0.85:
            col = rp[1]
        else:
            col = rp[0]
        c.hline(0, 31, y, col)
    # оплётка
    for x in range(0, 32, 4):
        c.vline(x, 0, 31, shade(rp[2], -0.25))
        c.vline(x + 1, 0, 31, shade(rp[2], 0.12))
    # маркировочная полоса
    sc = rgb(stripe)
    c.hline(0, 31, 14, sc)
    c.hline(0, 31, 17, shade(sc, -0.4))
    return c


def conveyor():
    c = Cv()
    c.fill(rgb('#1c1c22'))
    yl = rgb('#d8a800')
    # боковые рельсы
    c.rect(0, 0, 31, 3, STEEL[2])
    c.rect(0, 28, 31, 31, STEEL[2])
    c.hline(0, 31, 0, STEEL[4])
    c.hline(0, 31, 28, STEEL[3])
    c.hline(0, 31, 3, STEEL[0])
    c.hline(0, 31, 31, STEEL[0])
    # шевроны ленты
    for x0 in range(2, 32, 8):
        for k in range(8):
            y = 5 + k
            c.set((x0 + k) % 32, y, yl)
            c.set((x0 + k) % 32, 26 - k + 0, yl)
            c.set((x0 + k + 1) % 32, y, shade(yl, -0.3))
        for k in range(8):
            c.set((x0 + k) % 32, 13 + k, shade(yl, 0.1) if False else yl) if False else None
    for x in range(0, 32, 4):
        c.vline(x, 4, 27, rgb('#2a2a34'))
    return c


def conveyor_sealed():
    c, base = plate_base(40, 'steel', -0.1)
    yl = rgb('#d8a800')
    c.rect(4, 11, 27, 20, STEEL[1])
    bevel(c, 4, 11, 27, 20, STEEL[2], True, 0.3)
    # стрелка
    for k in range(5):
        c.vline(16 + k, 12 + k, 19 - k, yl)
    c.rect(7, 14, 15, 17, yl)
    c.hline(7, 15, 14, shade(yl, 0.4))
    c.hline(7, 15, 17, shade(yl, -0.4))
    return c


# ================================================================== сборка
def build_all():
    out = build_ores()
    out['machine_casing'] = machine_casing()
    # печи / генератор
    out['coal_generator_front'] = furnace_front(False, 'coal')
    out['coal_generator_front_on'] = furnace_front(True, 'coal')
    out['coal_generator'] = furnace_front(False, 'coal')
    out['electric_furnace_front'] = furnace_front(False, 'electric')
    out['electric_furnace_front_on'] = furnace_front(True, 'electric')
    out['electric_furnace'] = furnace_front(False, 'electric')
    out['macerator_front'] = macerator_front(False)
    out['macerator_front_on'] = macerator_front(True)
    out['macerator'] = macerator_front(False)
    out['compressor_front'] = compressor_front(False)
    out['compressor_front_on'] = compressor_front(True)
    out['compressor'] = compressor_front(False)
    out['alloy_smelter'] = alloy_smelter()
    out['assembly_line'] = assembly_line()
    out['blast_furnace_controller'] = blast_furnace()
    out['boiler'] = boiler()
    out['chemical_reactor'] = chemical_reactor()
    out['cooling_tower'] = cooling_tower()
    out['drill_controller'] = drill_controller()
    out['drill_frame'] = drill_frame()
    out['drill_head'] = drill_head()
    out['geiger_sensor'] = geiger()
    out['greenhouse'] = greenhouse()
    out['laser_drill'] = laser_drill()
    out['nuclear_reactor'] = nuclear()
    out['oil_deposit'] = oil_deposit()
    out['quarry'] = quarry()
    out['refinery'] = refinery()
    out['rtg'] = rtg()
    out['safety_valve'] = safety_valve()
    out['scrubber'] = scrubber()
    out['solar_panel_top'] = solar_top()
    out['solar_panel'] = solar_top()
    out['sprinkler'] = sprinkler()
    out['transformer'] = transformer()
    out['vitrification_furnace'] = vitrification()
    out['water_wheel'] = water_wheel()
    out['wind_turbine'] = wind_turbine()
    out['battery_box_side'] = battery_side()
    out['battery_box'] = battery_side()
    out['battery_box_top'] = battery_top()
    out['cable_lv'] = cable('lv')
    out['cable_mv'] = cable('mv')
    out['cable_hv'] = cable('hv')
    out['tin_cable'] = cable('tin')
    out['conveyor'] = conveyor()
    out['conveyor_sealed'] = conveyor_sealed()
    return out
