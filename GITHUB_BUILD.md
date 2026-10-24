# Сборка через GitHub Actions

## 1. Создай репозиторий на GitHub
- New repository → например `TechQuest`
- **не** ставь README/license (у нас уже есть файлы)

## 2. Залей код

```bash
# в папке с распакованным techquest-0.4.0-src
git init
git add .
git commit -m "TechQuest 0.4.0"
git branch -M main
git remote add origin https://github.com/ТВОЙ_НИК/TechQuest.git
git push -u origin main
```

(или через GitHub Desktop / загрузку ZIP на сайт)

## 3. Дождись сборки
1. Открой вкладку **Actions**
2. Workflow **Build** запустится сам
3. Через 2–5 минут статус станет зелёным
4. Кликни на успешный run → **Artifacts** → скачай `techquest-...`
5. Внутри будет `techquest-0.4.0.jar`

## 4. Ручной запуск
Actions → Build → **Run workflow** → Run

## Если сборка упала
- Открой красный job → лог **Build**
- Скопируй ошибки компиляции и пришли — поправим исходники
