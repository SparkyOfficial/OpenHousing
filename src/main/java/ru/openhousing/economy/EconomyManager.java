package ru.openhousing.economy;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import ru.openhousing.OpenHousing;
import ru.openhousing.housing.House;

/**
 * Менеджер экономической системы с Vault интеграцией
 */
public class EconomyManager {
    
    private final OpenHousing plugin;
    private Economy economy;
    private boolean vaultEnabled = false;
    
    // Цены по умолчанию
    private double defaultHousePrice = 10000.0;
    private double housePricePerBlock = 10.0;
    private double sellMultiplier = 0.7; // 70% от стоимости покупки
    
    public EconomyManager(OpenHousing plugin) {
        this.plugin = plugin;
        setupEconomy();
        loadConfig();
    }
    
    /**
     * Настройка Vault экономики
     */
    private void setupEconomy() {
        if (plugin.getServer().getPluginManager().getPlugin("Vault") == null) {
            plugin.getLogger().warning("Vault не найден! Экономические функции отключены.");
            return;
        }
        
        RegisteredServiceProvider<Economy> rsp = plugin.getServer().getServicesManager()
            .getRegistration(Economy.class);
        
        if (rsp == null) {
            plugin.getLogger().warning("Экономический провайдер не найден! Проверьте настройки Vault.");
            return;
        }
        
        economy = rsp.getProvider();
        vaultEnabled = true;
        plugin.getLogger().info("Vault экономика подключена успешно!");
    }
    
    /**
     * Загрузка конфигурации цен
     */
    private void loadConfig() {
        defaultHousePrice = plugin.getConfig().getDouble("economy.default-house-price", 10000.0);
        housePricePerBlock = plugin.getConfig().getDouble("economy.price-per-block", 10.0);
        sellMultiplier = plugin.getConfig().getDouble("economy.sell-multiplier", 0.7);
        
        // Сохраняем значения по умолчанию если их нет
        plugin.getConfig().set("economy.default-house-price", defaultHousePrice);
        plugin.getConfig().set("economy.price-per-block", housePricePerBlock);
        plugin.getConfig().set("economy.sell-multiplier", sellMultiplier);
        plugin.saveConfig();
    }
    
    /**
     * Проверка доступности экономики
     */
    public boolean isEconomyEnabled() {
        return vaultEnabled && economy != null;
    }
    
    /**
     * Получение баланса игрока
     */
    public double getBalance(Player player) {
        if (!isEconomyEnabled()) return 0.0;
        return economy.getBalance(player);
    }
    
    /**
     * Снятие денег с игрока
     */
    public boolean withdrawPlayer(Player player, double amount) {
        if (!isEconomyEnabled()) return true; // Если экономика отключена, разрешаем
        return economy.withdrawPlayer(player, amount).transactionSuccess();
    }
    
    /**
     * Выдача денег игроку
     */
    public boolean depositPlayer(Player player, double amount) {
        if (!isEconomyEnabled()) return true;
        return economy.depositPlayer(player, amount).transactionSuccess();
    }
    
    /**
     * Расчет стоимости дома
     */
    public double calculateHousePrice(House house) {
        if (house == null) return defaultHousePrice;
        
        // Базовая цена + цена за блоки
        double basePrice = defaultHousePrice;
        
        // Считаем по размеру дома
        House.HouseSize size = house.getSize();
        if (size != null) {
            int volume = size.getVolume();
            return basePrice + (volume * housePricePerBlock);
        }
        
        return basePrice;
    }
    
    /**
     * Расчет стоимости продажи дома
     */
    public double calculateSellPrice(House house) {
        return calculateHousePrice(house) * sellMultiplier;
    }
    
    /**
     * Покупка дома
     */
    public boolean buyHouse(Player player, House house) {
        if (!isEconomyEnabled()) {
            player.sendMessage("§a[OpenHousing] Дом получен бесплатно (экономика отключена)");
            return true;
        }
        
        double price = calculateHousePrice(house);
        double balance = getBalance(player);
        
        if (balance < price) {
            player.sendMessage("§c[OpenHousing] Недостаточно средств! Нужно: §e" + 
                economy.format(price) + "§c, у вас: §e" + economy.format(balance));
            return false;
        }
        
        if (withdrawPlayer(player, price)) {
            player.sendMessage("§a[OpenHousing] Дом куплен за §e" + economy.format(price) + "§a!");
            player.sendMessage("§7Остаток: §e" + economy.format(getBalance(player)));
            return true;
        } else {
            player.sendMessage("§c[OpenHousing] Ошибка при покупке дома!");
            return false;
        }
    }
    
    /**
     * Продажа дома
     */
    public boolean sellHouse(Player player, House house) {
        if (!isEconomyEnabled()) {
            player.sendMessage("§a[OpenHousing] Дом продан (экономика отключена)");
            return true;
        }
        
        double sellPrice = calculateSellPrice(house);
        
        if (depositPlayer(player, sellPrice)) {
            player.sendMessage("§a[OpenHousing] Дом продан за §e" + economy.format(sellPrice) + "§a!");
            player.sendMessage("§7Баланс: §e" + economy.format(getBalance(player)));
            return true;
        } else {
            player.sendMessage("§c[OpenHousing] Ошибка при продаже дома!");
            return false;
        }
    }
    
    /**
     * Проверка возможности покупки
     */
    public boolean canAfford(Player player, double amount) {
        if (!isEconomyEnabled()) return true;
        return getBalance(player) >= amount;
    }
    
    /**
     * Форматирование суммы
     */
    public String format(double amount) {
        if (!isEconomyEnabled()) return String.valueOf(amount);
        return economy.format(amount);
    }
    
    /**
     * Получение информации о ценах
     */
    public void showPriceInfo(Player player) {
        player.sendMessage("§6§l=== Цены на дома ===");
        player.sendMessage("§eБазовая цена: §f" + format(defaultHousePrice));
        player.sendMessage("§eЦена за блок: §f" + format(housePricePerBlock));
        player.sendMessage("§eКоэффициент продажи: §f" + (sellMultiplier * 100) + "%");
        
        if (isEconomyEnabled()) {
            player.sendMessage("§7Ваш баланс: §e" + format(getBalance(player)));
        } else {
            player.sendMessage("§7Экономика отключена - дома бесплатны");
        }
    }
    
    /**
     * Установка цен (только для администраторов)
     */
    public void setDefaultPrice(double price) {
        this.defaultHousePrice = price;
        plugin.getConfig().set("economy.default-house-price", price);
        plugin.saveConfig();
    }
    
    public void setPricePerBlock(double price) {
        this.housePricePerBlock = price;
        plugin.getConfig().set("economy.price-per-block", price);
        plugin.saveConfig();
    }
    
    public void setSellMultiplier(double multiplier) {
        this.sellMultiplier = multiplier;
        plugin.getConfig().set("economy.sell-multiplier", multiplier);
        plugin.saveConfig();
    }
    
    // Геттеры
    public double getDefaultHousePrice() { return defaultHousePrice; }
    public double getHousePricePerBlock() { return housePricePerBlock; }
    public double getSellMultiplier() { return sellMultiplier; }
}
