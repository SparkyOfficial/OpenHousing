package ru.openhousing.coding.constants;

/**
 * Константы для имен параметров блоков кода
 * Устраняет "магические строки" и улучшает поддерживаемость кода
 */
public final class BlockParams {
    
    // Общие параметры
    public static final String ACTION_TYPE = "actionType";
    public static final String EVENT_TYPE = "eventType";
    public static final String CONDITION_TYPE = "conditionType";
    public static final String VALUE = "value";
    public static final String EXTRA = "extra";
    public static final String EXTRA1 = "extra1";
    public static final String EXTRA2 = "extra2";
    public static final String LOCATION = "location";
    public static final String COUNT = "count";
    public static final String DELAY = "delay";
    public static final String FORCE = "force";
    public static final String RADIUS = "radius";
    public static final String SIZE = "size";
    public static final String BLOCK_TYPE = "blockType";
    public static final String PARTICLE_NAME = "particleName";
    public static final String OFFSET = "offset";
    public static final String AMOUNT = "amount";
    public static final String LEVEL = "level";
    public static final String EXPERIENCE = "experience";
    public static final String HEALTH = "health";
    public static final String FOOD = "food";
    public static final String GAMEMODE = "gamemode";
    public static final String ITEM = "item";
    public static final String MATERIAL = "material";
    public static final String DURATION = "duration";
    public static final String AMPLIFIER = "amplifier";
    public static final String TARGET_TYPE = "targetType";
    public static final String REPEAT_TYPE = "repeatType";
    public static final String MAX_ITERATIONS = "maxIterations";
    public static final String COMPARE_VALUE = "compareValue";
    public static final String SECOND_VALUE = "secondValue";
    public static final String VARIABLE_NAME = "variableName";
    public static final String OPERATION = "operation";
    public static final String FUNCTION_NAME = "functionName";
    public static final String ARGUMENTS = "arguments";
    public static final String START_LOCATION = "startLocation";
    public static final String END_LOCATION = "endLocation";
    public static final String CENTER_LOCATION = "centerLocation";
    public static final String TIMINGS = "timings";
    public static final String COLOR = "color";
    public static final String SOUND_NAME = "soundName";
    public static final String VOLUME = "volume";
    public static final String PITCH = "pitch";
    public static final String WEATHER_TYPE = "weatherType";
    public static final String TIME = "time";
    public static final String RULE_NAME = "ruleName";
    public static final String RULE_VALUE = "ruleValue";
    public static final String ENTITY_TYPE = "entityType";
    public static final String PLAYER_NAME = "playerName";
    public static final String WORLD_NAME = "worldName";
    public static final String MESSAGE = "message";
    public static final String TITLE = "title";
    public static final String SUBTITLE = "subtitle";
    public static final String FADE_IN = "fadeIn";
    public static final String STAY = "stay";
    public static final String FADE_OUT = "fadeOut";
    
    // Специфичные для блоков параметры
    public static final String DAMAGE_AMOUNT = "damageAmount";
    public static final String FIRE_TICKS = "fireTicks";
    public static final String GLOWING = "glowing";
    public static final String INVULNERABLE = "invulnerable";
    public static final String SILENT = "silent";
    public static final String CUSTOM_NAME = "customName";
    public static final String CUSTOM_NAME_VISIBLE = "customNameVisible";
    public static final String AGE = "age";
    public static final String BREEDING = "breeding";
    public static final String LOVE_MODE = "loveMode";
    public static final String PASSENGER = "passenger";
    public static final String VEHICLE = "vehicle";
    public static final String INVENTORY_SLOT = "inventorySlot";
    public static final String ENCHANTMENT = "enchantment";
    public static final String ENCHANTMENT_LEVEL = "enchantmentLevel";
    public static final String LORE = "lore";
    public static final String DISPLAY_NAME = "displayName";
    public static final String UNBREAKABLE = "unbreakable";
    public static final String HIDE_FLAGS = "hideFlags";
    public static final String ATTRIBUTE = "attribute";
    public static final String ATTRIBUTE_VALUE = "attributeValue";
    public static final String ATTRIBUTE_SLOT = "attributeSlot";
    public static final String POTION_EFFECT = "potionEffect";
    public static final String POTION_DURATION = "potionDuration";
    public static final String POTION_AMPLIFIER = "potionAmplifier";
    public static final String POTION_AMBIENT = "potionAmbient";
    public static final String POTION_PARTICLES = "potionParticles";
    public static final String POTION_ICON = "potionIcon";
    public static final String SOUND_CATEGORY = "soundCategory";
    public static final String SOUND_LOCATION = "soundLocation";
    public static final String PARTICLE_COUNT = "particleCount";
    public static final String PARTICLE_OFFSET_X = "particleOffsetX";
    public static final String PARTICLE_OFFSET_Y = "particleOffsetY";
    public static final String PARTICLE_OFFSET_Z = "particleOffsetZ";
    public static final String PARTICLE_EXTRA = "particleExtra";
    public static final String PARTICLE_DATA = "particleData";
    public static final String COLOR_RED = "colorRed";
    public static final String COLOR_GREEN = "colorGreen";
    public static final String COLOR_BLUE = "colorBlue";
    public static final String COLOR_ALPHA = "colorAlpha";
    public static final String TEXT_FORMAT = "textFormat";
    public static final String TEXT_COLOR = "textColor";
    public static final String TEXT_STYLE = "textStyle";
    public static final String TEXT_HOVER = "textHover";
    public static final String TEXT_CLICK = "textClick";
    public static final String TEXT_INSERTION = "textInsertion";
    public static final String TEXT_FONT = "textFont";
    public static final String TEXT_SCALE = "textScale";
    public static final String TEXT_ROTATION = "textRotation";
    public static final String TEXT_TRANSLATION = "textTranslation";
    public static final String TEXT_SHADOW = "textShadow";
    public static final String TEXT_FILTER = "textFilter";
    public static final String TEXT_GRADIENT = "textGradient";
    public static final String TEXT_RAINBOW = "textRainbow";
    public static final String TEXT_MAGIC = "textMagic";
    public static final String TEXT_OBFUSCATED = "textObfuscated";
    public static final String TEXT_BOLD = "textBold";
    public static final String TEXT_ITALIC = "textItalic";
    public static final String TEXT_UNDERLINE = "textUnderline";
    public static final String TEXT_STRIKETHROUGH = "textStrikethrough";
    
    // Дополнительные параметры
    public static final String DESCRIPTION = "description";
    public static final String NAME = "name";
    public static final String SECOND_VARIABLE = "secondVariable";
    
    private BlockParams() {
        // Утилитный класс не должен быть инстанцирован
    }
}
