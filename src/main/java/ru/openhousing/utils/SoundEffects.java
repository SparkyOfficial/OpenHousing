package ru.openhousing.utils;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import ru.openhousing.OpenHousing;

/**
 * Утилиты для звуковых эффектов и визуальных уведомлений
 */
public class SoundEffects {
    
    private final OpenHousing plugin;
    
    public SoundEffects(OpenHousing plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Воспроизведение звука успеха
     */
    public void playSuccess(Player player) {
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, SoundCategory.PLAYERS, 0.5f, 1.2f);
    }
    
    /**
     * Воспроизведение звука ошибки
     */
    public void playError(Player player) {
        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, SoundCategory.PLAYERS, 0.5f, 0.8f);
    }
    
    /**
     * Воспроизведение звука клика
     */
    public void playClick(Player player) {
        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, SoundCategory.PLAYERS, 0.3f, 1.0f);
    }
    
    /**
     * Воспроизведение звука открытия GUI
     */
    public void playOpenGUI(Player player) {
        player.playSound(player.getLocation(), Sound.BLOCK_CHEST_OPEN, SoundCategory.PLAYERS, 0.4f, 1.1f);
    }
    
    /**
     * Воспроизведение звука закрытия GUI
     */
    public void playCloseGUI(Player player) {
        player.playSound(player.getLocation(), Sound.BLOCK_CHEST_CLOSE, SoundCategory.PLAYERS, 0.4f, 0.9f);
    }
    
    /**
     * Воспроизведение звука сохранения
     */
    public void playSave(Player player) {
        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.PLAYERS, 0.6f, 1.0f);
    }
    
    /**
     * Воспроизведение звука создания дома
     */
    public void playHouseCreate(Player player) {
        player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, SoundCategory.PLAYERS, 0.7f, 1.0f);
    }
    
    /**
     * Воспроизведение звука телепортации
     */
    public void playTeleport(Player player) {
        player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, SoundCategory.PLAYERS, 0.5f, 1.0f);
    }
    
    /**
     * Показать титл приветствия
     */
    public void showWelcomeTitle(Player player) {
        player.sendTitle(
            "§6§lДобро пожаловать!",
            "§eИспользуйте §6/house §eдля управления домами",
            10, 40, 10
        );
    }
    
    /**
     * Показать титл создания дома
     */
    public void showHouseCreatedTitle(Player player, String houseName) {
        player.sendTitle(
            "§a§lДом создан!",
            "§e" + houseName,
            10, 40, 10
        );
    }
    
    /**
     * Показать титл сохранения кода
     */
    public void showCodeSavedTitle(Player player) {
        player.sendTitle(
            "§a§lКод сохранен!",
            "§eИзменения применены",
            5, 20, 5
        );
    }
    
    /**
     * Показать боссбар с информацией о доме
     */
    public void showHouseBossBar(Player player, String houseName, String ownerName) {
        BossBar bossBar = Bukkit.createBossBar(
            "§6Дом: §e" + houseName + " §7| §6Владелец: §e" + ownerName,
            BarColor.YELLOW,
            BarStyle.SOLID
        );
        
        bossBar.addPlayer(player);
        
        // Убираем боссбар через 10 секунд
        new BukkitRunnable() {
            @Override
            public void run() {
                bossBar.removePlayer(player);
                bossBar.removeAll();
            }
        }.runTaskLater(plugin, 20L * 10);
    }
    
    /**
     * Показать боссбар с информацией о коде
     */
    public void showCodeBossBar(Player player, int lines, int blocks) {
        BossBar bossBar = Bukkit.createBossBar(
            "§6Код: §e" + lines + " строк, §e" + blocks + " блоков §7| §6Редактор активен",
            BarColor.BLUE,
            BarStyle.SOLID
        );
        
        bossBar.addPlayer(player);
        
        // Убираем боссбар через 5 секунд
        new BukkitRunnable() {
            @Override
            public void run() {
                bossBar.removePlayer(player);
                bossBar.removeAll();
            }
        }.runTaskLater(plugin, 20L * 5);
    }
    
    /**
     * Показать скорборд с информацией о доме
     */
    public void showHouseScoreboard(Player player, String houseName, String ownerName, int visitors) {
        // Создаем простой скорборд
        org.bukkit.scoreboard.ScoreboardManager manager = Bukkit.getScoreboardManager();
        org.bukkit.scoreboard.Scoreboard board = manager.getNewScoreboard();
        org.bukkit.scoreboard.Objective objective = board.registerNewObjective("house_info", "dummy", "§6§lИнформация о доме");
        
        objective.setDisplaySlot(org.bukkit.scoreboard.DisplaySlot.SIDEBAR);
        
        objective.getScore("§7").setScore(6);
        objective.getScore("§6Название:").setScore(5);
        objective.getScore("§e" + houseName).setScore(4);
        objective.getScore("§6Владелец:").setScore(3);
        objective.getScore("§e" + ownerName).setScore(2);
        objective.getScore("§6Посетители:").setScore(1);
        objective.getScore("§e" + visitors).setScore(0);
        
        player.setScoreboard(board);
        
        // Убираем скорборд через 15 секунд
        new BukkitRunnable() {
            @Override
            public void run() {
                player.setScoreboard(manager.getNewScoreboard());
            }
        }.runTaskLater(plugin, 20L * 15);
    }
    
    /**
     * Показать скорборд с информацией о коде
     */
    public void showCodeScoreboard(Player player, int lines, int blocks, int functions, boolean enabled) {
        org.bukkit.scoreboard.ScoreboardManager manager = Bukkit.getScoreboardManager();
        org.bukkit.scoreboard.Scoreboard board = manager.getNewScoreboard();
        org.bukkit.scoreboard.Objective objective = board.registerNewObjective("code_info", "dummy", "§6§lИнформация о коде");
        
        objective.setDisplaySlot(org.bukkit.scoreboard.DisplaySlot.SIDEBAR);
        
        objective.getScore("§7").setScore(7);
        objective.getScore("§6Строки:").setScore(6);
        objective.getScore("§e" + lines).setScore(5);
        objective.getScore("§6Блоки:").setScore(4);
        objective.getScore("§e" + blocks).setScore(3);
        objective.getScore("§6Функции:").setScore(2);
        objective.getScore("§e" + functions).setScore(1);
        objective.getScore("§6Статус:").setScore(0);
        objective.getScore(enabled ? "§aВключен" : "§cВыключен").setScore(-1);
        
        player.setScoreboard(board);
        
        // Убираем скорборд через 10 секунд
        new BukkitRunnable() {
            @Override
            public void run() {
                player.setScoreboard(manager.getNewScoreboard());
            }
        }.runTaskLater(plugin, 20L * 10);
    }
    
    /**
     * Показать BossBar с информацией о плагине
     */
    public void showPluginBossBar(Player player) {
        BossBar bossBar = Bukkit.createBossBar(
            "§6§lOpenHousing §7| §fВизуальное программирование для Minecraft",
            BarColor.BLUE,
            BarStyle.SEGMENTED_6
        );
        bossBar.addPlayer(player);
        
        // Убираем BossBar через 15 секунд
        new BukkitRunnable() {
            @Override
            public void run() {
                bossBar.removePlayer(player);
                bossBar.removeAll();
            }
        }.runTaskLater(plugin, 20L * 15);
    }
    
    /**
     * Показать Scoreboard с информацией о плагине
     */
    public void showPluginScoreboard(Player player) {
        org.bukkit.scoreboard.ScoreboardManager manager = Bukkit.getScoreboardManager();
        org.bukkit.scoreboard.Scoreboard board = manager.getNewScoreboard();
        org.bukkit.scoreboard.Objective objective = board.registerNewObjective("plugin_info", "dummy", "§6§lOpenHousing");
        
        objective.setDisplaySlot(org.bukkit.scoreboard.DisplaySlot.SIDEBAR);
        
        objective.getScore("§7").setScore(10);
        objective.getScore("§fВерсия: §e1.0.0").setScore(9);
        objective.getScore("§fСтатус: §aАктивен").setScore(8);
        objective.getScore("§7").setScore(7);
        objective.getScore("§e/housing create §7- создать дом").setScore(6);
        objective.getScore("§e/housing list §7- список домов").setScore(5);
        objective.getScore("§e/code editor §7- редактор кода").setScore(4);
        objective.getScore("§e/play §7- запустить код").setScore(3);
        objective.getScore("§7").setScore(2);
        objective.getScore("§aСоздавайте дома").setScore(1);
        objective.getScore("§aи программируйте!").setScore(0);
        
        player.setScoreboard(board);
    }
    
    /**
     * Показать Scoreboard с подробной информацией о доме
     */
    public void showDetailedHouseScoreboard(Player player, ru.openhousing.housing.House house) {
        org.bukkit.scoreboard.ScoreboardManager manager = Bukkit.getScoreboardManager();
        org.bukkit.scoreboard.Scoreboard board = manager.getNewScoreboard();
        org.bukkit.scoreboard.Objective objective = board.registerNewObjective("house_info", "dummy", "§6§lИнформация о доме");
        
        objective.setDisplaySlot(org.bukkit.scoreboard.DisplaySlot.SIDEBAR);
        
        // Добавляем информацию
        objective.getScore("§7").setScore(10);
        objective.getScore("§fНазвание: §e" + house.getName()).setScore(9);
        objective.getScore("§fВладелец: §e" + house.getOwnerName()).setScore(8);
        objective.getScore("§fРазмер: §e" + house.getSize().getDisplayName()).setScore(7);
        objective.getScore("§fСтатус: " + (house.isPublic() ? "§aПубличный" : "§7Приватный")).setScore(6);
        objective.getScore("§fРазрешенных: §e" + house.getAllowedPlayers().size()).setScore(5);
        objective.getScore("§fЗаблокированных: §e" + house.getBannedPlayers().size()).setScore(4);
        objective.getScore("§7").setScore(3);
        objective.getScore("§e/housing home §7- домой").setScore(2);
        objective.getScore("§e/code editor §7- код").setScore(1);
        objective.getScore("§e/housing settings §7- настройки").setScore(0);
        
        player.setScoreboard(board);
    }
    
    /**
     * Убрать Scoreboard
     */
    public void removeScoreboard(Player player) {
        player.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
    }
}
