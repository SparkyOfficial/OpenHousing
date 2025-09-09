package ru.openhousing.coding.blocks;

import org.bukkit.Material;

/**
 * Типы блоков для визуального программирования
 */
public enum BlockType {
    
    // События игрока (29 событий)
    GAME_START("Начало игры", Material.EMERALD_BLOCK, BlockCategory.EVENT, "Срабатывает при запуске игры командой /play"),
    PLAYER_JOIN("Игрок зашел", Material.PLAYER_HEAD, BlockCategory.EVENT, "Игрок присоединился к серверу"),
    PLAYER_QUIT("Игрок вышел", Material.SKELETON_SKULL, BlockCategory.EVENT, "Игрок покинул сервер"),
    PLAYER_CHAT("Игрок написал", Material.WRITABLE_BOOK, BlockCategory.EVENT, "Игрок отправил сообщение в чат"),
    PLAYER_COMMAND("Игрок команда", Material.COMMAND_BLOCK, BlockCategory.EVENT, "Игрок использовал команду"),
    PLAYER_MOVE("Игрок двигается", Material.LEATHER_BOOTS, BlockCategory.EVENT, "Игрок перемещается"),
    PLAYER_TELEPORT("Игрок телепорт", Material.ENDER_PEARL, BlockCategory.EVENT, "Игрок телепортируется"),
    PLAYER_DEATH("Игрок умер", Material.SKELETON_SKULL, BlockCategory.EVENT, "Игрок погиб"),
    PLAYER_RESPAWN("Игрок возродился", Material.TOTEM_OF_UNDYING, BlockCategory.EVENT, "Игрок воскрес"),
    PLAYER_DAMAGE("Игрок получил урон", Material.IRON_SWORD, BlockCategory.EVENT, "Игрок получил урон"),
    PLAYER_HEAL("Игрок исцелился", Material.GOLDEN_APPLE, BlockCategory.EVENT, "Игрок восстановил здоровье"),
    PLAYER_FOOD_CHANGE("Голод изменился", Material.BREAD, BlockCategory.EVENT, "Изменился уровень голода"),
    PLAYER_EXP_CHANGE("Опыт изменился", Material.EXPERIENCE_BOTTLE, BlockCategory.EVENT, "Изменился опыт игрока"),
    PLAYER_LEVEL_UP("Игрок повысил уровень", Material.ENCHANTED_BOOK, BlockCategory.EVENT, "Игрок получил новый уровень"),
    PLAYER_INVENTORY_CLICK("Клик по инвентарю", Material.CHEST, BlockCategory.EVENT, "Игрок кликнул в инвентаре"),
    PLAYER_ITEM_DROP("Игрок выбросил предмет", Material.DROPPER, BlockCategory.EVENT, "Игрок выбросил предмет"),
    PLAYER_ITEM_PICKUP("Игрок поднял предмет", Material.HOPPER, BlockCategory.EVENT, "Игрок поднял предмет"),
    PLAYER_ITEM_CONSUME("Игрок съел предмет", Material.APPLE, BlockCategory.EVENT, "Игрок употребил предмет"),
    PLAYER_ITEM_BREAK("Предмет сломался", Material.WOODEN_PICKAXE, BlockCategory.EVENT, "Предмет игрока сломался"),
    PLAYER_BLOCK_BREAK("Игрок сломал блок", Material.DIAMOND_PICKAXE, BlockCategory.EVENT, "Игрок разрушил блок"),
    PLAYER_BLOCK_PLACE("Игрок поставил блок", Material.GRASS_BLOCK, BlockCategory.EVENT, "Игрок поставил блок"),
    PLAYER_INTERACT("Игрок взаимодействие", Material.STICK, BlockCategory.EVENT, "Игрок взаимодействовал с блоком"),
    PLAYER_INTERACT_ENTITY("Клик по существу", Material.LEAD, BlockCategory.EVENT, "Игрок кликнул по существу"),
    PLAYER_FISH("Игрок рыбачит", Material.FISHING_ROD, BlockCategory.EVENT, "Игрок ловит рыбу"),
    PLAYER_ENCHANT("Игрок зачаровал", Material.ENCHANTING_TABLE, BlockCategory.EVENT, "Игрок зачаровал предмет"),
    PLAYER_CRAFT("Игрок скрафтил", Material.CRAFTING_TABLE, BlockCategory.EVENT, "Игрок создал предмет"),
    PLAYER_SMELT("Игрок переплавил", Material.FURNACE, BlockCategory.EVENT, "Игрок переплавил предмет"),
    PLAYER_TRADE("Игрок торговал", Material.EMERALD, BlockCategory.EVENT, "Игрок торговал с жителем"),
    PLAYER_SNEAK("Игрок присел", Material.LEATHER_LEGGINGS, BlockCategory.EVENT, "Игрок начал/закончил приседание"),
    
    // События мира (7 событий)
    WORLD_WEATHER_CHANGE("Погода изменилась", Material.WATER_BUCKET, BlockCategory.EVENT, "Изменилась погода в мире"),
    WORLD_TIME_CHANGE("Время изменилось", Material.CLOCK, BlockCategory.EVENT, "Изменилось время в мире"),
    WORLD_CHUNK_LOAD("Чанк загружен", Material.MAP, BlockCategory.EVENT, "Чанк был загружен"),
    WORLD_CHUNK_UNLOAD("Чанк выгружен", Material.PAPER, BlockCategory.EVENT, "Чанк был выгружен"),
    WORLD_STRUCTURE_GROW("Структура выросла", Material.OAK_SAPLING, BlockCategory.EVENT, "Дерево или структура выросла"),
    WORLD_EXPLOSION("Взрыв в мире", Material.TNT, BlockCategory.EVENT, "Произошел взрыв"),
    WORLD_PORTAL_CREATE("Портал создан", Material.OBSIDIAN, BlockCategory.EVENT, "Создан портал"),
    
    // События существ (15 событий)
    ENTITY_SPAWN("Существо появилось", Material.EGG, BlockCategory.EVENT, "Существо заспавнилось"),
    ENTITY_DEATH("Существо умерло", Material.BONE, BlockCategory.EVENT, "Существо погибло"),
    ENTITY_DAMAGE("Существо получило урон", Material.IRON_SWORD, BlockCategory.EVENT, "Существо получило урон"),
    ENTITY_TARGET("Существо выбрало цель", Material.BOW, BlockCategory.EVENT, "Существо выбрало цель для атаки"),
    ENTITY_TAME("Существо приручено", Material.BONE, BlockCategory.EVENT, "Существо было приручено"),
    ENTITY_BREED("Существо размножилось", Material.WHEAT, BlockCategory.EVENT, "Существа размножились"),
    ENTITY_EXPLODE("Существо взорвалось", Material.CREEPER_HEAD, BlockCategory.EVENT, "Крипер или другое существо взорвалось"),
    ENTITY_INTERACT("Клик по существу", Material.CARROT, BlockCategory.EVENT, "Игрок взаимодействовал с существом"),
    ENTITY_MOUNT("Существо оседлано", Material.SADDLE, BlockCategory.EVENT, "Игрок сел на существо"),
    ENTITY_DISMOUNT("Игрок слез", Material.LEATHER, BlockCategory.EVENT, "Игрок слез с существа"),
    ENTITY_LEASH("Существо на поводке", Material.LEAD, BlockCategory.EVENT, "Существо взято на поводок"),
    ENTITY_UNLEASH("Поводок снят", Material.STRING, BlockCategory.EVENT, "С существа снят поводок"),
    ENTITY_SHEAR("Существо пострижено", Material.SHEARS, BlockCategory.EVENT, "Овца или другое существо пострижено"),
    ENTITY_MILK("Существо подоено", Material.MILK_BUCKET, BlockCategory.EVENT, "Корова подоена"),
    ENTITY_TRANSFORM("Существо трансформировалось", Material.GOLDEN_APPLE, BlockCategory.EVENT, "Существо изменило тип"),
    
    // Условия игрока (10 условий)
    IF_PLAYER_ONLINE("Игрок онлайн", Material.EMERALD, BlockCategory.CONDITION, "Проверяет, онлайн ли игрок"),
    IF_PLAYER_PERMISSION("Если есть право", Material.GOLDEN_HELMET, BlockCategory.CONDITION, "Проверяет права игрока"),
    IF_PLAYER_GAMEMODE("Если режим игры", Material.GRASS_BLOCK, BlockCategory.CONDITION, "Проверяет режим игры"),
    IF_PLAYER_WORLD("Если в мире", Material.COMPASS, BlockCategory.CONDITION, "Проверяет мир игрока"),
    IF_PLAYER_FLYING("Если летает", Material.ELYTRA, BlockCategory.CONDITION, "Проверяет летает ли игрок"),
    IF_PLAYER_SNEAKING("Если приседает", Material.LEATHER_LEGGINGS, BlockCategory.CONDITION, "Проверяет приседает ли игрок"),
    IF_PLAYER_BLOCKING("Если блокирует", Material.SHIELD, BlockCategory.CONDITION, "Проверяет блокирует ли игрок"),
    IF_PLAYER_ITEM("Если предмет в руке", Material.DIAMOND_SWORD, BlockCategory.CONDITION, "Проверяет предмет в руке"),
    IF_PLAYER_HEALTH("Если здоровье", Material.GOLDEN_APPLE, BlockCategory.CONDITION, "Проверяет здоровье игрока"),
    IF_PLAYER_FOOD("Если голод", Material.BREAD, BlockCategory.CONDITION, "Проверяет уровень голода"),
    
    // Условия переменных (7 условий)
    IF_VARIABLE_EQUALS("Если переменная равна", Material.COMPARATOR, BlockCategory.CONDITION, "Проверяет равенство переменной"),
    IF_VARIABLE("Если переменная", Material.NAME_TAG, BlockCategory.CONDITION, "Проверяет значение переменной"),
    IF_VARIABLE_GREATER("Если переменная больше", Material.REDSTONE_TORCH, BlockCategory.CONDITION, "Проверяет больше ли переменная"),
    IF_VARIABLE_LESS("Если переменная меньше", Material.LEVER, BlockCategory.CONDITION, "Проверяет меньше ли переменная"),
    IF_VARIABLE_CONTAINS("Если содержит текст", Material.WRITABLE_BOOK, BlockCategory.CONDITION, "Проверяет содержит ли переменная текст"),
    IF_VARIABLE_EXISTS("Если переменная существует", Material.BOOK, BlockCategory.CONDITION, "Проверяет существует ли переменная"),
    IF_VARIABLE_SAVED("Если переменная сохранена", Material.BOOKSHELF, BlockCategory.CONDITION, "Проверяет сохранена ли переменная"),
    IF_VARIABLE_TYPE("Если тип переменной", Material.KNOWLEDGE_BOOK, BlockCategory.CONDITION, "Проверяет тип переменной"),
    IF_PLAYER_HAS_ITEM("Если у игрока есть предмет", Material.CHEST, BlockCategory.CONDITION, "Проверяет наличие предметов у игрока"),
    
    // Условия игры (5 условий)
    IF_GAME_TIME("Если время", Material.CLOCK, BlockCategory.CONDITION, "Проверяет время в мире"),
    IF_GAME_WEATHER("Если погода", Material.WATER_BUCKET, BlockCategory.CONDITION, "Проверяет погоду"),
    IF_GAME_DIFFICULTY("Если сложность", Material.DIAMOND_SWORD, BlockCategory.CONDITION, "Проверяет сложность мира"),
    IF_GAME_PLAYERS_ONLINE("Если игроков онлайн", Material.PLAYER_HEAD, BlockCategory.CONDITION, "Проверяет количество игроков"),
    IF_GAME_TPS("Если TPS сервера", Material.REDSTONE_BLOCK, BlockCategory.CONDITION, "Проверяет производительность сервера"),
    
    // Условия существ (3 условия)
    IF_ENTITY_EXISTS("Если существо существует", Material.ZOMBIE_HEAD, BlockCategory.CONDITION, "Проверяет существует ли существо"),
    IF_ENTITY_TYPE("Если тип существа", Material.EGG, BlockCategory.CONDITION, "Проверяет тип существа"),
    IF_ENTITY_HEALTH("Если здоровье существа", Material.BONE, BlockCategory.CONDITION, "Проверяет здоровье существа"),
    
    // Действия игрока (25 действий)
    PLAYER_ACTION("Действие игрока", Material.PLAYER_HEAD, BlockCategory.ACTION, "Общее действие игрока"),
    PLAYER_SEND_MESSAGE("Отправить сообщение", Material.WRITABLE_BOOK, BlockCategory.ACTION, "Отправляет сообщение игроку"),
    PLAYER_SEND_TITLE("Отправить титул", Material.PAINTING, BlockCategory.ACTION, "Показывает титул игроку"),
    PLAYER_SEND_ACTIONBAR("Отправить экшн-бар", Material.ITEM_FRAME, BlockCategory.ACTION, "Показывает сообщение в экшн-баре"),
    PLAYER_TELEPORT_ACTION("Телепортировать игрока", Material.ENDER_PEARL, BlockCategory.ACTION, "Телепортирует игрока"),
    PLAYER_GIVE_ITEM("Дать предмет", Material.CHEST, BlockCategory.ACTION, "Дает предмет игроку"),
    PLAYER_REMOVE_ITEM("Забрать предмет", Material.HOPPER, BlockCategory.ACTION, "Забирает предмет у игрока"),
    PLAYER_CLEAR_INVENTORY("Очистить инвентарь", Material.BARRIER, BlockCategory.ACTION, "Очищает инвентарь игрока"),
    PLAYER_SET_HEALTH("Установить здоровье", Material.GOLDEN_APPLE, BlockCategory.ACTION, "Устанавливает здоровье игрока"),
    PLAYER_SET_FOOD("Установить голод", Material.BREAD, BlockCategory.ACTION, "Устанавливает уровень голода"),
    PLAYER_SET_EXP("Установить опыт", Material.EXPERIENCE_BOTTLE, BlockCategory.ACTION, "Устанавливает опыт игрока"),
    PLAYER_GIVE_EFFECT("Дать эффект", Material.POTION, BlockCategory.ACTION, "Дает эффект зелья игроку"),
    PLAYER_REMOVE_EFFECT("Убрать эффект", Material.MILK_BUCKET, BlockCategory.ACTION, "Убирает эффект зелья"),
    PLAYER_PLAY_SOUND("Воспроизвести звук", Material.NOTE_BLOCK, BlockCategory.ACTION, "Воспроизводит звук для игрока"),
    PLAYER_STOP_SOUND("Остановить звук", Material.BARRIER, BlockCategory.ACTION, "Останавливает звук для игрока"),
    PLAYER_SPAWN_PARTICLE("Создать частицы", Material.FIREWORK_ROCKET, BlockCategory.ACTION, "Создает частицы для игрока"),
    PLAYER_SET_GAMEMODE("Установить режим", Material.GRASS_BLOCK, BlockCategory.ACTION, "Устанавливает режим игры"),
    PLAYER_KICK("Кикнуть игрока", Material.IRON_DOOR, BlockCategory.ACTION, "Кикает игрока с сервера"),
    PLAYER_BAN("Забанить игрока", Material.BARRIER, BlockCategory.ACTION, "Банит игрока"),
    PLAYER_WHITELIST_ADD("Добавить в вайтлист", Material.PAPER, BlockCategory.ACTION, "Добавляет в белый список"),
    PLAYER_WHITELIST_REMOVE("Убрать из вайтлиста", Material.BARRIER, BlockCategory.ACTION, "Убирает из белого списка"),
    PLAYER_SET_DISPLAY_NAME("Установить ник", Material.NAME_TAG, BlockCategory.ACTION, "Устанавливает отображаемое имя"),
    PLAYER_RESET_DISPLAY_NAME("Сбросить ник", Material.STRING, BlockCategory.ACTION, "Сбрасывает отображаемое имя"),
    PLAYER_SEND_PLUGIN_MESSAGE("Отправить плагин сообщение", Material.COMMAND_BLOCK, BlockCategory.ACTION, "Отправляет сообщение другому плагину"),
    
    // Действия переменных (20 действий)
    VAR_SET("Установить переменную", Material.WRITABLE_BOOK, BlockCategory.ACTION, "Устанавливает значение переменной"),
    VAR_ADD("Прибавить к переменной", Material.REDSTONE, BlockCategory.ACTION, "Прибавляет к переменной"),
    VAR_SUBTRACT("Отнять от переменной", Material.REDSTONE_TORCH, BlockCategory.ACTION, "Отнимает от переменной"),
    VAR_MULTIPLY("Умножить переменную", Material.REPEATER, BlockCategory.ACTION, "Умножает переменную"),
    VAR_DIVIDE("Разделить переменную", Material.COMPARATOR, BlockCategory.ACTION, "Делит переменную"),
    VAR_APPEND_TEXT("Добавить текст", Material.PAPER, BlockCategory.ACTION, "Добавляет текст к переменной"),
    VAR_REPLACE_TEXT("Заменить текст", Material.WRITABLE_BOOK, BlockCategory.ACTION, "Заменяет текст в переменной"),
    VAR_UPPERCASE("Верхний регистр", Material.BOOK, BlockCategory.ACTION, "Переводит текст в верхний регистр"),
    VAR_LOWERCASE("Нижний регистр", Material.WRITTEN_BOOK, BlockCategory.ACTION, "Переводит текст в нижний регистр"),
    VAR_REVERSE("Обратить текст", Material.COMPASS, BlockCategory.ACTION, "Переворачивает текст"),
    VAR_LENGTH("Длина текста", Material.STICK, BlockCategory.ACTION, "Получает длину текста"),
    VAR_SUBSTRING("Подстрока", Material.SHEARS, BlockCategory.ACTION, "Извлекает часть текста"),
    VAR_RANDOM_NUMBER("Случайное число", Material.REDSTONE, BlockCategory.ACTION, "Генерирует случайное число"),
    VAR_ROUND("Округлить число", Material.REDSTONE_BLOCK, BlockCategory.ACTION, "Округляет число"),
    VAR_ABS("Модуль числа", Material.IRON_BLOCK, BlockCategory.ACTION, "Получает модуль числа"),
    VAR_MIN("Минимум", Material.STONE_BUTTON, BlockCategory.ACTION, "Находит минимальное значение"),
    VAR_MAX("Максимум", Material.STONE_PRESSURE_PLATE, BlockCategory.ACTION, "Находит максимальное значение"),
    VAR_SAVE("Сохранить переменную", Material.BOOKSHELF, BlockCategory.ACTION, "Сохраняет переменную навсегда"),
    VAR_DELETE("Удалить переменную", Material.LAVA_BUCKET, BlockCategory.ACTION, "Удаляет переменную"),
    VAR_COPY("Копировать переменную", Material.PAPER, BlockCategory.ACTION, "Копирует значение переменной"),
    
    // Действия игры (15 действий)
    GAME_SET_TIME("Установить время", Material.CLOCK, BlockCategory.ACTION, "Устанавливает время в мире"),
    GAME_SET_WEATHER("Установить погоду", Material.WATER_BUCKET, BlockCategory.ACTION, "Устанавливает погоду"),
    GAME_SET_DIFFICULTY("Установить сложность", Material.DIAMOND_SWORD, BlockCategory.ACTION, "Устанавливает сложность"),
    GAME_BROADCAST("Объявление всем", Material.BELL, BlockCategory.ACTION, "Отправляет сообщение всем игрокам"),
    GAME_EXECUTE_COMMAND("Выполнить команду", Material.COMMAND_BLOCK, BlockCategory.ACTION, "Выполняет консольную команду"),
    GAME_STOP_SERVER("Остановить сервер", Material.BARRIER, BlockCategory.ACTION, "Останавливает сервер"),
    GAME_RESTART_SERVER("Перезапустить сервер", Material.REDSTONE_BLOCK, BlockCategory.ACTION, "Перезапускает сервер"),
    GAME_SAVE_WORLD("Сохранить мир", Material.CHEST, BlockCategory.ACTION, "Сохраняет мир"),
    GAME_LOAD_WORLD("Загрузить мир", Material.ENDER_CHEST, BlockCategory.ACTION, "Загружает мир"),
    GAME_CREATE_EXPLOSION("Создать взрыв", Material.TNT, BlockCategory.ACTION, "Создает взрыв в мире"),
    GAME_SPAWN_ENTITY("Заспавнить существо", Material.EGG, BlockCategory.ACTION, "Спавнит существо"),
    GAME_REMOVE_ENTITY("Удалить существо", Material.LAVA_BUCKET, BlockCategory.ACTION, "Удаляет существо"),
    GAME_SET_BLOCK("Установить блок", Material.STONE, BlockCategory.ACTION, "Устанавливает блок в мире"),
    GAME_BREAK_BLOCK("Сломать блок", Material.DIAMOND_PICKAXE, BlockCategory.ACTION, "Ломает блок в мире"),
    GAME_SEND_PACKET("Отправить пакет", Material.REDSTONE, BlockCategory.ACTION, "Отправляет пакет игроку"),
    
    // Функции и управление
    FUNCTION("Функция", Material.COMMAND_BLOCK, BlockCategory.FUNCTION,
            "Создает переиспользуемую функцию"),
    CALL_FUNCTION("Вызвать функцию", Material.REPEATING_COMMAND_BLOCK, BlockCategory.FUNCTION,
                 "Вызывает созданную функцию"),
    REPEAT("Повторение", Material.CLOCK, BlockCategory.CONTROL,
          "Повторяет код определенное количество раз"),
    TARGET("Цель", Material.TARGET, BlockCategory.CONTROL,
          "Определяет цель для действий"),
    CODE_CONTROL("Контроль кода", Material.LEVER, BlockCategory.CONTROL,
                "Управляет выполнением кода"),
    ELSE("Иначе", Material.REDSTONE_TORCH, BlockCategory.CONTROL,
         "Выполняется, если условие ложно"),
    
    // Утилиты разработки
    NOT_ARROW("Стрелка НЕ", Material.SPECTRAL_ARROW, BlockCategory.UTILITY,
             "Инвертирует результат условия"),
    CODE_MOVER_3000("Перемещатель кода 3000", Material.PISTON, BlockCategory.UTILITY,
                   "Перемещает блоки кода"),
    
    // Выборка (5 функций)
    SELECT_PLAYER("Выбрать игрока", Material.PLAYER_HEAD, BlockCategory.UTILITY, "Выбирает игрока по критериям"),
    SELECT_ENTITY("Выбрать существо", Material.ZOMBIE_HEAD, BlockCategory.UTILITY, "Выбирает существо по критериям"),
    SELECT_RANDOM("Выбрать случайно", Material.REDSTONE, BlockCategory.UTILITY, "Случайный выбор из списка"),
    SELECT_ALL("Выбрать всех", Material.BEACON, BlockCategory.UTILITY, "Выбирает всех подходящих"),
    SELECT_FILTER("Фильтр выборки", Material.HOPPER, BlockCategory.UTILITY, "Фильтрует выборку по условиям"),
        
    // Математика и текст
    MATH("Математика", Material.REDSTONE_BLOCK, BlockCategory.UTILITY,
         "Выполняет математические операции"),
    TEXT_OPERATION("Операции с текстом", Material.WRITABLE_BOOK, BlockCategory.UTILITY,
                   "Операции с текстовыми строками"),
    
    // Действия инвентаря
    INVENTORY_ACTION("Действия инвентаря", Material.CHEST, BlockCategory.ACTION, "Действия с инвентарем"),
    ITEM_CHECK("Проверка предмета", Material.DIAMOND, BlockCategory.CONDITION, "Проверяет предметы"),
    
    // Дополнительные условия
    IF_ITEM_EXISTS("Если предмет существует", Material.DIAMOND, BlockCategory.CONDITION, "Проверяет существование предмета"),
    IF_ITEM_COUNT("Если количество предметов", Material.DIAMOND, BlockCategory.CONDITION, "Проверяет количество предметов");
    
    private final String displayName;
    private final Material material;
    private final BlockCategory category;
    private final String description;
    
    BlockType(String displayName, Material material, BlockCategory category, String description) {
        this.displayName = displayName;
        this.material = material;
        this.category = category;
        this.description = description;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public Material getMaterial() {
        return material;
    }
    
    public BlockCategory getCategory() {
        return category;
    }
    
    public String getDescription() {
        return description;
    }
    
    /**
     * Категории блоков
     */
    public enum BlockCategory {
        EVENT("События", Material.REDSTONE, "Блоки событий"),
        CONDITION("Условия", Material.COMPARATOR, "Блоки условий"),
        ACTION("Действия", Material.PISTON, "Блоки действий"),
        FUNCTION("Функции", Material.COMMAND_BLOCK, "Блоки функций"),
        CONTROL("Управление", Material.REDSTONE, "Блоки управления кодом"),
        UTILITY("Утилиты", Material.CRAFTING_TABLE, "Вспомогательные блоки");
        
        private final String displayName;
        private final Material material;
        private final String description;
        
        BlockCategory(String displayName, Material material, String description) {
            this.displayName = displayName;
            this.material = material;
            this.description = description;
        }
        
        public String getDisplayName() {
            return displayName;
        }
        
        public Material getMaterial() {
            return material;
        }
        
        public String getDescription() {
            return description;
        }
    }
}
