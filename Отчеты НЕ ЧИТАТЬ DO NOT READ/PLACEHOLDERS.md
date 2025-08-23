# OpenHousing PlaceholderAPI

OpenHousing предоставляет множество плейсхолдеров для интеграции с PlaceholderAPI.

## Установка

1. Установите PlaceholderAPI на ваш сервер
2. Установите OpenHousing
3. Плейсхолдеры автоматически зарегистрируются

## Плейсхолдеры для скриптов

### Основные плейсхолдеры
- `%openhousing_script_enabled%` - Включен ли скрипт (true/false)
- `%openhousing_has_script%` - Есть ли у игрока скрипт (true/false)
- `%openhousing_script_blocks%` - Количество блоков в скрипте
- `%openhousing_script_variables%` - Количество переменных
- `%openhousing_script_functions%` - Количество функций
- `%openhousing_script_lines%` - Количество строк кода
- `%openhousing_script_errors%` - Количество ошибок в коде
- `%openhousing_script_world%` - Привязанный мир скрипта

### Статус скрипта
- `%openhousing_script_status%` - Статус скрипта:
  - "Нет кода" - скрипт пустой
  - "Отключен" - скрипт отключен
  - "Есть ошибки" - в коде есть ошибки
  - "Готов" - скрипт готов к выполнению

### Редактор
- `%openhousing_editor_open%` - Открыт ли редактор кода (true/false)

## Плейсхолдеры для домов

### Информация о текущем доме
- `%openhousing_house_current%` - Название текущего дома
- `%openhousing_house_owner%` - Владелец дома
- `%openhousing_house_public%` - Публичный ли дом (true/false)
- `%openhousing_house_size%` - Размер дома (например, "50x50x50")
- `%openhousing_house_mode%` - Режим дома (Игра/Строительство)
- `%openhousing_house_description%` - Описание дома
- `%openhousing_house_rating%` - Рейтинг дома (например, "4.5")
- `%openhousing_house_visits%` - Количество посещений
- `%openhousing_house_visitors%` - Количество разрешенных игроков

### Права доступа
- `%openhousing_house_is_owner%` - Является ли игрок владельцем (true/false)
- `%openhousing_house_is_allowed%` - Разрешен ли игрок (true/false)
- `%openhousing_house_is_banned%` - Заблокирован ли игрок (true/false)

### Статистика игрока
- `%openhousing_house_count%` - Количество домов у игрока
- `%openhousing_house_created%` - Дата создания дома (dd.MM.yyyy)

## Плейсхолдеры для статистики

### Общая статистика
- `%openhousing_stats_total_houses%` - Общее количество домов на сервере
- `%openhousing_stats_public_houses%` - Количество публичных домов
- `%openhousing_stats_online_players%` - Количество игроков онлайн

### Персональная статистика
- `%openhousing_stats_my_houses%` - Количество домов у игрока
- `%openhousing_stats_my_visits%` - Количество посещений других домов
- `%openhousing_stats_server_uptime%` - Время работы сервера (HH:mm)

## Плейсхолдеры для экономики

### Баланс игрока
- `%openhousing_economy_balance%` - Баланс игрока (число)
- `%openhousing_economy_balance_formatted%` - Отформатированный баланс

### Стоимость создания
- `%openhousing_economy_house_creation_cost%` - Стоимость создания дома
- `%openhousing_economy_can_create_house%` - Может ли игрок создать дом (true/false)

## Примеры использования

### Scoreboard
```
Дом: %openhousing_house_current%
Владелец: %openhousing_house_owner%
Размер: %openhousing_house_size%
Рейтинг: %openhousing_house_rating%
```

### Chat
```
Ваш скрипт: %openhousing_script_status%
Блоков: %openhousing_script_blocks%
Ошибок: %openhousing_script_errors%
```

### ActionBar
```
Баланс: %openhousing_economy_balance_formatted% | Домов: %openhousing_house_count%
```

### TabList
```
%player_name% | %openhousing_house_current% | %openhousing_script_status%
```

## Примечания

- Если PlaceholderAPI не установлен, плейсхолдеры не будут работать
- Некоторые плейсхолдеры могут возвращать пустые значения, если данные недоступны
- Плейсхолдеры обновляются в реальном времени
- Для экономических плейсхолдеров требуется Vault и плагин экономики
