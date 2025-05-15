package flowershop.dao;

import flowershop.database.DatabaseManager;
import flowershop.models.Accessory;
import flowershop.models.Accessory.AccessoryType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO (Data Access Object) для роботи з аксесуарами у базі даних.
 * Надає методи для створення, оновлення, видалення, отримання та управління запасами аксесуарів.
 */
public class AccessoryDAO {

    private static final Logger logger = LogManager.getLogger(AccessoryDAO.class);
    private final DatabaseManager dbManager;

    /**
     * Конструктор DAO для аксесуарів.
     * Ініціалізує менеджер підключення до бази даних.
     */
    public AccessoryDAO() {
        this.dbManager = DatabaseManager.getInstance();
    }

    // Отримання даних

    /**
     * Отримує всі аксесуари з бази даних.
     *
     * @return список усіх аксесуарів
     */
    public List<Accessory> getAllAccessories() {
        List<Accessory> accessories = new ArrayList<>();
        String sql = "SELECT a.*, t.name as type_name, t.display_name " +
                "FROM accessories a JOIN accessory_types t ON a.type_id = t.id";

        try (Connection conn = dbManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                accessories.add(mapRowToAccessory(rs));
            }
            logger.debug("Отримано {} аксесуарів з бази даних.", accessories.size());
        } catch (SQLException e) {
            logger.error("Помилка при отриманні всіх аксесуарів: {}", e.getMessage(), e);
        }
        return accessories;
    }

    /**
     * Отримує аксесуар за його ідентифікатором.
     *
     * @param id ідентифікатор аксесуара
     * @return об'єкт Accessory або null, якщо аксесуар не знайдено
     */
    public Accessory getAccessoryById(int id) {
        String sql = "SELECT a.*, t.name as type_name, t.display_name " +
                "FROM accessories a JOIN accessory_types t ON a.type_id = t.id WHERE a.id = ?";

        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapRowToAccessory(rs);
                }
            }
        } catch (SQLException e) {
            logger.error("Помилка при отриманні аксесуара за ID {}: {}", id, e.getMessage(), e);
        }
        return null;
    }

    // Збереження даних

    /**
     * Зберігає аксесуар у базі даних (створює новий або оновлює існуючий).
     *
     * @param accessory аксесуар для збереження
     * @return true, якщо збереження успішне; false, якщо виникла помилка
     */
    public boolean saveAccessory(Accessory accessory) {
        if (accessory == null) {
            logger.warn("Спроба зберегти null-аксесуар.");
            return false;
        }
        boolean result = accessory.getId() > 0 ? updateAccessory(accessory) : insertAccessory(accessory);
        if (result) {
            logger.info("Аксесуар '{}' успішно збережено з ID {}.", accessory.getName(), accessory.getId());
        }
        return result;
    }

    /**
     * Додає новий аксесуар до бази даних.
     *
     * @param accessory аксесуар для додавання
     * @return true, якщо додавання успішне; false, якщо виникла помилка
     */
    private boolean insertAccessory(Accessory accessory) {
        String sql = "INSERT INTO accessories (type_id, name, price, description, image_path, stock_quantity, color, size) " +
                "VALUES ((SELECT id FROM accessory_types WHERE name = ?), ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            setAccessoryParameters(pstmt, accessory);
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        setAccessoryId(accessory, generatedKeys.getInt(1));
                        return true;
                    }
                }
            }
        } catch (SQLException e) {
            logger.error("Помилка при додаванні аксесуара '{}': {}", accessory.getName(), e.getMessage(), e);
        }
        return false;
    }

    /**
     * Оновлює існуючий аксесуар у базі даних.
     *
     * @param accessory аксесуар для оновлення
     * @return true, якщо оновлення успішне; false, якщо виникла помилка
     */
    private boolean updateAccessory(Accessory accessory) {
        String sql = "UPDATE accessories SET type_id = (SELECT id FROM accessory_types WHERE name = ?), " +
                "name = ?, price = ?, description = ?, image_path = ?, stock_quantity = ?, color = ?, size = ? WHERE id = ?";

        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            setAccessoryParameters(pstmt, accessory);
            pstmt.setInt(9, accessory.getId());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Помилка при оновленні аксесуара ID {}: {}", accessory.getId(), e.getMessage(), e);
        }
        return false;
    }

    /**
     * Встановлює параметри для PreparedStatement для збереження аксесуара.
     *
     * @param pstmt     PreparedStatement для заповнення
     * @param accessory аксесуар, чиї дані потрібно встановити
     * @throws SQLException якщо виникає помилка при встановленні параметрів
     */
    private void setAccessoryParameters(PreparedStatement pstmt, Accessory accessory) throws SQLException {
        pstmt.setString(1, accessory.getType().name());
        pstmt.setString(2, accessory.getName());
        pstmt.setDouble(3, accessory.getPrice());
        pstmt.setString(4, accessory.getDescription());
        pstmt.setString(5, accessory.getImagePath());
        pstmt.setInt(6, accessory.getStockQuantity());
        pstmt.setString(7, accessory.getColor());
        pstmt.setString(8, accessory.getSize());
    }

    // Видалення даних

    /**
     * Видаляє аксесуар за його ідентифікатором.
     *
     * @param id ідентифікатор аксесуара
     * @return true, якщо видалення успішне; false, якщо виникла помилка
     */
    public boolean deleteAccessory(int id) {
        String sql = "DELETE FROM accessories WHERE id = ?";

        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            boolean result = pstmt.executeUpdate() > 0;
            if (result) {
                logger.info("Аксесуар з ID {} успішно видалено.", id);
            }
            return result;
        } catch (SQLException e) {
            logger.error("Помилка при видаленні аксесуара з ID {}: {}", id, e.getMessage(), e);
            return false;
        }
    }

    // Управління запасами

    /**
     * Оновлює кількість аксесуара на складі.
     *
     * @param accessoryId ідентифікатор аксесуара
     * @param quantity    кількість для додавання (може бути від'ємною для зменшення)
     */
    public void updateStock(int accessoryId, int quantity) {
        String sql = "UPDATE accessories SET stock_quantity = stock_quantity + ? WHERE id = ?";

        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, quantity);
            pstmt.setInt(2, accessoryId);
            pstmt.executeUpdate();
            logger.debug("Запас аксесуара ID {} оновлено на {} одиниць.", accessoryId, quantity);
        } catch (SQLException e) {
            logger.error("Помилка при оновленні запасу аксесуара ID {}: {}", accessoryId, e.getMessage(), e);
        }
    }

    // Допоміжні методи

    /**
     * Перетворює рядок ResultSet на об'єкт Accessory.
     *
     * @param rs ResultSet, який містить дані аксесуара
     * @return об'єкт Accessory
     * @throws SQLException якщо виникає помилка при роботі з ResultSet
     */
    Accessory mapRowToAccessory(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        String typeName = rs.getString("type_name");
        String name = rs.getString("name");
        double price = rs.getDouble("price");
        String description = rs.getString("description");
        String imagePath = rs.getString("image_path");
        int stockQuantity = rs.getInt("stock_quantity");
        String color = rs.getString("color");
        String size = rs.getString("size");

        AccessoryType accessoryType;
        try {
            accessoryType = AccessoryType.valueOf(typeName);
        } catch (IllegalArgumentException e) {
            logger.error("Невідомий тип аксесуара: {}", typeName, e);
            throw new SQLException("Невідомий тип аксесуара: " + typeName, e);
        }

        Accessory accessory = new Accessory(name, price, description, imagePath, stockQuantity, accessoryType, color, size);
        setAccessoryId(accessory, id);
        return accessory;
    }

    /**
     * Встановлює ідентифікатор для аксесуара за допомогою рефлексії.
     *
     * @param accessory аксесуар, для якого потрібно встановити ID
     * @param id        ідентифікатор, який потрібно встановити
     */
    void setAccessoryId(Accessory accessory, int id) {
        try {
            java.lang.reflect.Field idField = Accessory.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(accessory, id);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            logger.error("Помилка рефлексії при встановленні ID {} для аксесуара '{}': {}", id, accessory.getName(), e.getMessage(), e);
        }
    }
}