# 🐛 HOTFIX v1.0.1 - Критические исправления

## 🚨 **ИСПРАВЛЕННЫЕ ПРОБЛЕМЫ:**

### 1. **База данных - длинные названия миров**
❌ **Проблема:** `Value too long for column "world CHARACTER VARYING(32)"`
✅ **Исправление:** 
- Изменено `world VARCHAR(32)` → `world_name VARCHAR(64)`
- Обновлены SQL запросы в `DatabaseManager.java`

### 2. **Отдельные миры для домов**
❌ **Проблема:** Дома создавались в общем мире `housing_world`
✅ **Исправление:**
- Удален старый метод `setupHousingWorld()`
- Каждый дом теперь создается в собственном мире
- Исправлен `prepareHouseWorld()` в `HousingManager.java`

### 3. **Система строк в CodeEditor**
❌ **Проблема:** GUI показывал блоки вместо строк
✅ **Исправление:**
- Обновлен `handleScriptClick()` для работы со строками
- Добавлен метод `getLineIndexFromSlot()`
- Интеграция с `LineSettingsGUI` и `LineSelectorGUI`

## 📦 **ФАЙЛЫ ИЗМЕНЕНЫ:**

### `DatabaseManager.java`
- Схема таблицы: `world VARCHAR(32)` → `world_name VARCHAR(64)`
- SQL запросы обновлены для `world_name`
- Методы `saveHouse()` и `loadAllHouses()` исправлены

### `HousingManager.java`  
- Удален `setupHousingWorld()` 
- Убрано создание общего мира `housing_world`
- Каждый дом = отдельный мир

### `CodeEditorGUI.java`
- `handleScriptClick()` переписан для строк
- Добавлен `getLineIndexFromSlot()`
- Интеграция с настройками строк

## 🎯 **РЕЗУЛЬТАТ:**

✅ **Создание домов работает без ошибок БД**
✅ **Каждый дом = отдельный мир с корректным спавном**  
✅ **Визуальный редактор кода работает со строками**
✅ **GUI корректно обрабатывает клики**

## 🚀 **ГОТОВО К ИСПОЛЬЗОВАНИЮ!**

Скопируйте новый `OpenHousing-1.0.0.jar` на сервер.
