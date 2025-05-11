package flowershop.dao;

import flowershop.database.DatabaseManager;
import flowershop.models.Flower;
import flowershop.models.Flower.FlowerType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO (Data Access Object) для роботи з квітами у базі даних.
 * Надає методи для створення, оновлення, видалення, отримання та управління запасами квітів.
 */
public class FlowerDAO {

    private static final Logger logger = LogManager.getLogger(FlowerDAO.class);
    private final DatabaseManager dbManager;

    /**
     * Конструктор DAO для квітів.
     * Ініціалізує менеджер підключення до бази даних.
     */
    public FlowerDAO() {
        this.dbManager = DatabaseManager.getInstance();
    }

    // Отримання даних

    /**
     * Отримує всі квіти з бази даних.
     *
     * @return список усіх квітів
     */
    public List<Flower> getAllFlowers() {
        List<Flower> flowers = new ArrayList<>();
        String sql = "SELECT f.*, t.name as type_name, t.display_name " +
                "FROM flowers f JOIN flower_types t ON f.type_id = t.id";

        try (Connection conn = dbManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                flowers.add(mapRowToFlower(rs));
            }
            logger.debug("Отримано {} квітів з бази даних.", flowers.size());
        } catch (SQLException e) {
            logger.error("Помилка при отриманні всіх квітів: {}", e.getMessage(), e);
        }
        return flowers;
    }

    /**
     * Отримує квітку за її ідентифікатором.
     *
     * @param id ідентифікатор квітки
     * @return об'єкт Flower або null, якщо квітку не знайдено
     */
    public Flower getFlowerById(int id) {
        String sql = "SELECT f.*, t.name as type_name, t.display_name " +
                "FROM flowers f JOIN flower_types t ON f.type_id = t.id WHERE f.id = ?";

        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapRowToFlower(rs);
                }
            }
        } catch (SQLException e) {
            logger.error("Помилка при отриманні квітки за ID {}: {}", id, e.getMessage(), e);
        }
        return null;
    }

    // Збереження даних

    /**
     * Зберігає квітку у базі даних (створює нову або оновлює існуючу).
     *
     * @param flower квітка для збереження
     * @return true, якщо збереження успішне; false, якщо виникла помилка
     */
    public boolean saveFlower(Flower flower) {
        if (flower == null) {
            logger.warn("Спроба зберегти null-квітку.");
            return false;
        }
        boolean result = flower.getId() > 0 ? updateFlower(flower) : insertFlower(flower);
        if (result) {
            logger.info("Квітка успішно збережена: {} {}", flower.getColor(), flower.getType());
        }
        return result;
    }

    /**
     * Додає нову квітку до бази даних.
     *
     * @param flower квітка для додавання
     * @return true, якщо додавання успішне; false, якщо виникла помилка
     */
    private boolean insertFlower(Flower flower) {
        String sql = "INSERT INTO flowers (type_id, price, freshness, stem_length, color, " +
                "country_of_origin, is_potted, image_path, stock_quantity, description) " +
                "VALUES ((SELECT id FROM flower_types WHERE name = ?), ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            setFlowerParameters(pstmt, flower);
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        setFlowerId(flower, generatedKeys.getInt(1));
                        return true;
                    }
                }
            }
        } catch (SQLException e) {
            logger.error("Помилка при додаванні квітки '{} {}': {}", flower.getColor(), flower.getType(), e.getMessage(), e);
        }
        return false;
    }

    /**
     * Оновлює існуючу квітку в базі даних.
     *
     * @param flower квітка для оновлення
     * @return true, якщо оновлення успішне; false, якщо виникла помилка
     */
    private boolean updateFlower(Flower flower) {
        String sql = "UPDATE flowers SET type_id = (SELECT id FROM flower_types WHERE name = ?), " +
                "price = ?, freshness = ?, stem_length = ?, color = ?, country_of_origin = ?, " +
                "is_potted = ?, image_path = ?, stock_quantity = ?, description = ? WHERE id = ?";

        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            setFlowerParameters(pstmt, flower);
            pstmt.setInt(11, flower.getId());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Помилка при оновленні квітки ID {}: {}", flower.getId(), e.getMessage(), e);
        }
        return false;
    }

    /**
     * Встановлює параметри для PreparedStatement для збереження квітки.
     *
     * @param pstmt PreparedStatement для заповнення
     * @param flower квітка, чиї дані потрібно встановити
     * @throws SQLException якщо виникає помилка при встановленні параметрів
     */
    private void setFlowerParameters(PreparedStatement pstmt, Flower flower) throws SQLException {
        pstmt.setString(1, flower.getType().name());
        pstmt.setDouble(2, flower.getPrice());
        pstmt.setInt(3, flower.getFreshness());
        pstmt.setInt(4, flower.getStemLength());
        pstmt.setString(5, flower.getColor());
        pstmt.setString(6, flower.getCountryOfOrigin());
        pstmt.setBoolean(7, flower.isPotted());
        pstmt.setString(8, flower.getImagePath());
        pstmt.setInt(9, flower.getStockQuantity());
        pstmt.setString(10, flower.generateDescription());
    }

    // Видалення даних

    /**
     * Видаляє квітку за її ідентифікатором.
     *
     * @param id ідентифікатор квітки
     * @return true, якщо видалення успішне; false, якщо виникла помилка
     */
    public boolean deleteFlower(int id) {
        String sql = "DELETE FROM flowers WHERE id = ?";

        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            boolean result = pstmt.executeUpdate() > 0;
            if (result) {
                logger.info("Квітка з ID {} успішно видалена.", id);
            }
            return result;
        } catch (SQLException e) {
            logger.error("Помилка при видаленні квітки з ID {}: {}", id, e.getMessage(), e);
            return false;
        }
    }

    // Управління запасами

    /**
     * Оновлює кількість квітки на складі.
     *
     * @param flowerId ідентифікатор квітки
     * @param quantity кількість для додавання (може бути від'ємною для зменшення)
     */
    public void updateStock(int flowerId, int quantity) {
        String sql = "UPDATE flowers SET stock_quantity = stock_quantity + ? WHERE id = ?";

        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, quantity);
            pstmt.setInt(2, flowerId);
            pstmt.executeUpdate();
            logger.debug("Запас квітки ID {} оновлено на {} одиниць.", flowerId, quantity);
        } catch (SQLException e) {
            logger.error("Помилка при оновленні запасу квітки ID {}: {}", flowerId, e.getMessage(), e);
        }
    }

    // Допоміжні методи

    /**
     * Перетворює рядок ResultSet на об'єкт Flower.
     *
     * @param rs ResultSet, який містить дані квітки
     * @return об'єкт Flower
     * @throws SQLException якщо виникає помилка при роботі з ResultSet
     */
    private Flower mapRowToFlower(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        String typeName = rs.getString("type_name");
        double price = rs.getDouble("price");
        int freshness = rs.getInt("freshness");
        int stemLength = rs.getInt("stem_length");
        String color = rs.getString("color");
        String countryOfOrigin = rs.getString("country_of_origin");
        boolean isPotted = rs.getBoolean("is_potted");
        String imagePath = rs.getString("image_path");
        int stockQuantity = rs.getInt("stock_quantity");

        FlowerType flowerType;
        try {
            flowerType = FlowerType.valueOf(typeName);
        } catch (IllegalArgumentException e) {
            logger.error("Невідомий тип квітки: {}", typeName, e);
            throw new SQLException("Невідомий тип квітки: " + typeName, e);
        }

        Flower flower = new Flower(flowerType, price, freshness, stemLength, color,
                countryOfOrigin, isPotted, imagePath, stockQuantity);
        setFlowerId(flower, id);
        return flower;
    }

    /**
     * Встановлює ідентифікатор для квітки за допомогою рефлексії.
     *
     * @param flower квітка, для якої потрібно встановити ID
     * @param id     ідентифікатор, який потрібно встановити
     */
    private void setFlowerId(Flower flower, int id) {
        try {
            java.lang.reflect.Field idField = Flower.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(flower, id);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            logger.error("Помилка рефлексії при встановленні ID {} для квітки '{} {}': {}",
                    id, flower.getColor(), flower.getType(), e.getMessage(), e);
        }
    }
}