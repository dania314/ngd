"""Пересобирает ВСЕ текстуры мода. Запуск:  python3 tools/build_textures.py
Нужен только Python 3 и Pillow (pip install pillow)."""
import glob
import os
import sys

HERE = os.path.dirname(os.path.abspath(__file__))
sys.path.insert(0, HERE)
import gen_items, gen_blocks, gen_gui  # noqa: E402

ROOT = os.path.join(HERE, '..', 'src', 'main', 'resources', 'assets', 'techquest', 'textures')


def write(group, images, only=None):
    d = os.path.join(ROOT, group)
    os.makedirs(d, exist_ok=True)
    n = 0
    for name, cv in images.items():
        if only is not None and name not in only:
            continue
        cv.save(os.path.join(d, name + '.png'))
        n += 1
    return n


def existing(group):
    return {os.path.basename(f)[:-4] for f in glob.glob(os.path.join(ROOT, group, '*.png'))}


def main():
    wanted_items = existing('item')
    wanted_blocks = existing('block')
    wanted_gui = existing('gui')

    # 1) стереть все старые текстуры
    for group in ('item', 'block', 'gui'):
        for f in glob.glob(os.path.join(ROOT, group, '*.png')):
            os.remove(f)

    # 2) нарисовать заново ровно под те же названия
    items = gen_items.build_all()
    blocks = gen_blocks.build_all()
    guides = gen_gui.build_guides(blocks)
    gui = {'processing_machine': gen_gui.processing_machine(), 'coal_generator': gen_gui.coal_generator()}
    gui.update(guides)

    for group, imgs, wanted in (('item', items, wanted_items), ('block', blocks, wanted_blocks), ('gui', gui, wanted_gui)):
        missing = sorted(wanted - set(imgs))
        if missing:
            print('!! нет рисунка для', group, missing)
        print(group, write(group, imgs, wanted), 'файлов')


if __name__ == '__main__':
    main()
