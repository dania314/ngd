# TechQuest — индустриальный мод с квестами (NeoForge 1.21.1)

Версии: Minecraft 1.21.1 · NeoForge 21.1.259 · Java 21 · ModDevGradle 2.0.148 · Gradle 9.4.1

## Что есть сейчас (этап 2)
**Материалы:** оловянная руда (+ глубинная), сырое олово, олово, бронза, пыль (олово/медь/железо/золото),
пластины (олово/бронза/железо/медь/золото), простая схема.

**Энергия (FE):**
| Блок | Что делает |
|---|---|
| Угольный генератор | 20 FE/т. Топливо только уголь/древесный уголь/блок угля. 1 уголь = 400 тиков = 8000 FE. Буфер 10 000 FE |
| Солнечная панель | 4 FE/т днём под открытым небом без дождя |
| Оловянный кабель | 100 FE/т, сам подключается к соседям |
| Накопитель энергии | 100 000 FE, ПКМ показывает заряд |

**Машины** (GUI, слоты входа/выхода, работают через воронки):
| Машина | Расход | Что делает |
|---|---|---|
| Дробилка | 20 FE/т | руда/сырьё → 2 пыли; булыжник → гравий → песок |
| Пресс | 30 FE/т | слиток → пластина |
| Электропечь | 25 FE/т | ванильные рецепты плавки вдвое быстрее, без топлива |

**Квестбук:** 16 квестов (дерево от первой руды до энергосети). Открывается клавишей **K** или ПКМ книгой.
Книга выдаётся при первом входе в мир; если потеряна: книга + оловянный слиток.

## Как собрать (GitHub + Codespaces)
1. Перетащи `techquest-stage2.zip` в панель файлов Codespaces.
2. В терминале:
   ```
   cd /workspaces/ngd
   mv 'techquest-stage2 (1).zip' stage2.zip   # только если имя с (1)
   find . -mindepth 1 -maxdepth 1 ! -name .git ! -name stage2.zip -exec rm -rf {} +
   unzip -o stage2.zip && rm stage2.zip
   git add -A && git commit -m "stage 2" && git push
   ```
3. Actions → последний запуск → Artifacts → `techquest-jar`.

## Как добавить свой квест
Создай файл `src/main/resources/data/techquest/techquest/quest/мой_квест.json`:
```json
{
  "title": {"translate": "quest.techquest.мой_квест.title"},
  "description": {"translate": "quest.techquest.мой_квест.desc"},
  "icon": "minecraft:diamond",
  "x": 12, "y": 1,
  "dependencies": ["techquest:industrial"],
  "task": {"type": "craft", "target": "minecraft:diamond_pickaxe", "count": 1},
  "rewards": [{"id": "minecraft:diamond", "count": 4}]
}
```
Типы заданий: `checkmark`, `has_item` (+`"consume": true` чтобы сдавать предметы), `mine`, `craft`, `place`, `smelt`.
`target` — id предмета/блока или тег с `#` (например `#c:ores/tin`).

## Как добавить рецепт дробилки/пресса
`data/techquest/recipe/macerator/имя.json`:
```json
{"type": "techquest:machine", "machine": "macerator",
 "ingredient": {"item": "minecraft:bone"},
 "result": {"id": "minecraft:bone_meal", "count": 6}, "time": 80}
```

## Дальше
- Этап 3: жидкости и трубы, универсальный генератор (лава/любое топливо), многоблочки, второй тир машин, датагенерация.
