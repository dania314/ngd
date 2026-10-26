"""GUI-текстуры: фоны окон (256x256, рабочая область 176x166) и схемы-подсказки 32x32."""
import math
from pxlib import *

PANEL = rgb('#c6cad2')
HI = rgb('#f2f5fa')
LO = rgb('#5c6270')
EDGE = rgb('#262a34')


def panel(c):
    """Основа окна 176x166 с фасками и заклёпками."""
    W, H = 176, 166
    c.rect(0, 0, W - 1, H - 1, PANEL)
    # лёгкая «рифлёная» фактура
    for y in range(2, H - 2):
        if y % 4 == 0:
            c.hline(2, W - 3, y, shade(PANEL, 0.03))
    # внешняя рамка
    c.hline(0, W - 1, 0, EDGE)
    c.hline(0, W - 1, H - 1, EDGE)
    c.vline(0, 0, H - 1, EDGE)
    c.vline(W - 1, 0, H - 1, EDGE)
    for i in (1, 2):
        c.hline(i, W - 1 - i, i, HI)
        c.vline(i, i, H - 1 - i, HI)
        c.hline(i, W - 1 - i, H - 1 - i, LO)
        c.vline(W - 1 - i, i, H - 1 - i, LO)
    for (x, y) in ((4, 4), (W - 6, 4), (4, H - 6), (W - 6, H - 6)):
        rivet(c, x, y, PANEL)
    # оранжевая линия-акцент под заголовком
    c.hline(6, W - 7, 14, rgb('#e8902a'))
    c.hline(6, W - 7, 15, shade(rgb('#e8902a'), -0.4))


def slot(c, x, y, size=18):
    """Рамка слота: (x, y) — левый верх рамки."""
    c.rect(x, y, x + size - 1, y + size - 1, rgb('#8b909c'))
    c.hline(x, x + size - 1, y, rgb('#373b46'))
    c.vline(x, y, y + size - 1, rgb('#373b46'))
    c.hline(x, x + size - 1, y + size - 1, rgb('#ffffff'))
    c.vline(x + size - 1, y, y + size - 1, rgb('#ffffff'))
    c.hline(x + 1, x + size - 2, y + 1, rgb('#50545f'))
    c.vline(x + 1, y + 1, y + size - 2, rgb('#50545f'))


def player_slots(c):
    for r in range(3):
        for col in range(9):
            slot(c, 7 + col * 18, 83 + r * 18)
    for col in range(9):
        slot(c, 7 + col * 18, 141)


def recess(c, x0, y0, x1, y1):
    c.rect(x0, y0, x1, y1, rgb('#aeb3be'))
    c.hline(x0, x1, y0, rgb('#4a4e5a'))
    c.vline(x0, y0, y1, rgb('#4a4e5a'))
    c.hline(x0, x1, y1, rgb('#f6f8fc'))
    c.vline(x1, y0, y1, rgb('#f6f8fc'))


def bar_frame(c, x, y, w, h):
    c.rect(x, y, x + w - 1, y + h - 1, rgb('#1e2028'))
    c.hline(x, x + w - 1, y, rgb('#0a0b0f'))
    c.vline(x, y, y + h - 1, rgb('#0a0b0f'))
    c.hline(x, x + w - 1, y + h - 1, rgb('#d8dce6'))
    c.vline(x + w - 1, y, y + h - 1, rgb('#d8dce6'))
    # деления
    for k in range(1, 6):
        yy = y + 1 + k * (h - 2) // 6
        c.hline(x + w - 4, x + w - 2, yy, rgb('#3a3e4a'))


def arrow_sprite(c, x, y):
    """Заполненная стрелка прогресса 24x17 (в текстуре с u=176)."""
    for i in range(24):
        for j in range(17):
            inside = False
            if i < 15 and 6 <= j <= 10:
                inside = True
            if i >= 13:
                half = (24 - i) * 8 / 11
                if abs(j - 8) <= half:
                    inside = True
            if inside:
                t = i / 24
                col = mix(rgb('#ff9a20'), rgb('#ffe060'), t)
                if j in (6, 7) or (i >= 13 and abs(j - 8) > (24 - i) * 8 / 11 - 1.2 and j < 8):
                    col = shade(col, 0.25)
                if j in (10, 9) and i < 15:
                    col = shade(col, -0.2)
                c.set(x + i, y + j, col)


def arrow_empty(c, x, y):
    for i in range(24):
        for j in range(17):
            inside = False
            if i < 15 and 6 <= j <= 10:
                inside = True
            if i >= 13:
                half = (24 - i) * 8 / 11
                if abs(j - 8) <= half:
                    inside = True
            if inside:
                c.set(x + i, y + j, rgb('#8a909e'))


def processing_machine():
    c = Cv(256, 256)
    panel(c)
    recess(c, 6, 18, 169, 78)
    player_slots(c)
    arrow_sprite(c, 176, 0)
    return c


def coal_generator():
    c = Cv(256, 256)
    panel(c)
    recess(c, 6, 18, 169, 78)
    player_slots(c)
    slot(c, 55, 43)            # слот топлива (предмет в 56,44)
    # силуэт огонька 14x14 в (56,26)
    for y in range(14):
        for x in range(14):
            w = 7 - abs(x - 6.5)
            top = 13 - (w * 1.7 + (3 if x % 5 == 0 else 0))
            if y >= top and w > 0:
                c.set(56 + x, 26 + y, rgb('#6a6e7c') if y > 4 else rgb('#7c808e'))
    bar_frame(c, 151, 15, 14, 56)
    return c


def downscale(im, n):
    return im.resize((n, n), Image.BOX)


def guide_icons(blocks):
    """Мини-иконки блоков 8x8 из уже нарисованных текстур."""
    def mini(name):
        return downscale(blocks[name].im, 8)
    return mini


def put(c, mini, x, y):
    c.im.alpha_composite(mini, (x, y))


def ghost(c, x, y):
    c.rect(x, y, x + 7, y + 7, rgb('#1c1e26'))
    for i in range(0, 8, 2):
        c.set(x + i, y, rgb('#4a4e5c'))
        c.set(x + i, y + 7, rgb('#4a4e5c'))
        c.set(x, y + i, rgb('#4a4e5c'))
        c.set(x + 7, y + i, rgb('#4a4e5c'))


def guide_base():
    c = Cv()
    c.rect(0, 0, 31, 31, rgb('#14161d'))
    bevel(c, 0, 0, 31, 31, rgb('#14161d'), False, 0.6)
    c.rect(1, 1, 30, 30, rgb('#1c1f29'))
    return c


def build_guides(blocks):
    mini = guide_icons(blocks)
    out = {}
    # --- сплавная печь: колонна из трёх блоков
    for step in range(1, 5):
        c = guide_base()
        col = [(11, 21, 'machine_casing'), (11, 12, 'alloy_smelter'), (11, 3, 'machine_casing')]
        # порядок постройки: низ, печь, верх
        for i, (x, y, name) in enumerate(col):
            if i < step if step < 4 else True:
                put(c, mini(name), x + 0, y)
            else:
                ghost(c, x, y)
        # стрелка-подсказка
        if step < 4:
            ax = 24
            ay = [25, 16, 7][step - 1]
            c.vline(ax, ay - 2, ay + 2, rgb('#ffd030'))
            c.set(ax - 1, ay, rgb('#ffd030'))
            c.set(ax + 1, ay, rgb('#ffd030'))
        else:
            glow(c, 15, 16, 14, rgb('#ff9a30'), 0.35)
        out[f'guide_alloy_{step}'] = c
    # --- бур: контроллер, голова, рамы
    for step in range(1, 5):
        c = guide_base()
        cells = {
            'ctl': (12, 12, 'drill_controller'), 'head': (12, 21, 'drill_head'),
            'l': (3, 12, 'drill_frame'), 'r': (21, 12, 'drill_frame'), 't': (12, 3, 'drill_frame'),
        }
        show = {1: ['ctl'], 2: ['ctl', 'head'], 3: ['ctl', 'head', 'l', 'r'], 4: ['ctl', 'head', 'l', 'r', 't']}[step]
        for k, (x, y, name) in cells.items():
            if k in show:
                put(c, mini(name), x, y)
            else:
                ghost(c, x, y)
        new = show[-1]
        x, y, _ = cells[new]
        c.rect(x - 1, y - 1, x + 8, y - 1, rgb('#ffd030'))
        c.rect(x - 1, y + 8, x + 8, y + 8, rgb('#ffd030'))
        c.vline(x - 1, y - 1, y + 8, rgb('#ffd030'))
        c.vline(x + 8, y - 1, y + 8, rgb('#ffd030'))
        out[f'guide_drill_{step}'] = c
    # --- водяное колесо у воды
    c = guide_base()
    wt = ramp(['#104a8a', '#2a80d0', '#58b0f8', '#a0daff', '#ffffff'])
    for x in (2, 22):
        for y in (12, 20):
            c.rect(x, y, x + 7, y + 7, wt[2])
            c.hline(x, x + 7, y, wt[3])
            c.set(x + 2, y + 2, wt[4])
            c.set(x + 5, y + 5, wt[1])
    put(c, mini('water_wheel'), 12, 16)
    for x in (9, 20):
        c.set(x, 18, wt[4])
        c.set(x + 1, 19, wt[3])
    out['guide_water_1'] = c
    # --- ветряк: блок / блок+лопасти / работа на открытом небе
    c = guide_base()
    put(c, mini('wind_turbine'), 12, 18)
    ghost(c, 12, 8)
    out['guide_wind_1'] = c
    c = guide_base()
    put(c, mini('wind_turbine'), 12, 18)
    # лопасть падает в блок
    c.line((24, 4), (17, 14), rgb('#b0c2d0'), 2)
    c.set(25, 3, rgb('#ffffff'))
    c.line((16, 15), (16, 17), rgb('#ffd030'))
    out['guide_wind_2'] = c
    c = Cv()
    c.rect(0, 0, 31, 31, rgb('#6aa8e8'))
    c.gradient_v(1, 1, 30, 30, rgb('#8cc8ff'), rgb('#cfe8ff'), False)
    bevel(c, 0, 0, 31, 31, rgb('#3a6aa8'), False, 0.5)
    c.rect(2, 3, 9, 5, rgb('#ffffff'))
    c.rect(20, 7, 28, 9, rgb('#ffffff'))
    put(c, mini('wind_turbine'), 12, 22)
    cx, cy = 16, 17
    for k in range(3):
        a = math.radians(-90 + k * 120 + 15)
        c.line((cx, cy), (int(cx + math.cos(a) * 11), int(cy + math.sin(a) * 11)), rgb('#f2f8ff'), 2)
    c.ellipse(14, 15, 18, 19, rgb('#5c6c82'))
    c.set(15, 16, rgb('#ffffff'))
    out['guide_wind_3'] = c
    return out
