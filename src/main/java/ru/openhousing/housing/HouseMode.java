package ru.openhousing.housing;

/**
 * Режимы дома
 */
public enum HouseMode {
    PLAY("Режим игры", "Код работает, игровой режим приключение"),
    BUILD("Режим строительства", "Код не работает, креатив для владельца");
    
    private final String displayName;
    private final String description;
    
    HouseMode(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getDescription() {
        return description;
    }
}
