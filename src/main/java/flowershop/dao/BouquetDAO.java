package flowershop.dao;

import flowershop.database.DatabaseManager;
import flowershop.models.Accessory;
import flowershop.models.Bouquet;
import flowershop.models.Flower;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO (Data Access Object) для роботи з букетами у базі даних.
 * Надає методи для створення, оновлення, видалення, отримання букетів, а також управління їх квітами та аксесуарами.
 */
public class BouquetDAO {

    private static final Logger logger = LogManager.getLogger(BouquetDAO.class);
    private final DatabaseManager dbManager;
    private final FlowerDAO flowerDAO;
    private final AccessoryDAO accessoryDAO;

    /**
     * Конструктор DAO для букетів.
     * Ініціалізує менеджер підключення до бази даних та DAO для квіток і аксесуарів.
     */
    public BouquetDAO() {
        this.dbManager = DatabaseManager.getInstance();
        this.flowerDAO = new FlowerDAO();
        this.accessoryDAO = new AccessoryDAO();
    }

    // Отримання даних

    /**
     * Отримує всі букети з бази даних разом із їх квітами та аксесуарами.
     *
     * @return список усіх букетів
     */
    public List<Bouquet> getAllBouquets() {
        List<Bouquet> bouquets = new ArrayList<>();
        String sql = "SELECT * FROM bouquets";

        try (Connection conn = dbManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Bouquet bouquet = mapRowToBouquet(rs);
                loadBouquetFlowers(conn, bouquet);
                loadBouquetAccessories(conn, bouquet);
                bouquets.add(bouquet);
            }
            logger.debug("Отримано {} букетів з бази даних.", bouquets.size());
        } catch (SQLException e) {
            logger.error("Помилка при отриманні всіх букетів: {}", e.getMessage(), e);
        }
        return bouquets;
    }

    /**
     * Отримує букет за його ідентифікатором разом із його квітами та аксесуарами.
     *
     * @param id ідентифікатор букета
     * @return об'єкт Bouquet або null, якщо букет не знайдено
     */
    public Bouquet getBouquetById(int id) {
        String sql = "SELECT * FROM bouquets WHERE id = ?";

        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Bouquet bouquet = mapRowToBouquet(rs);
                    loadBouquetFlowers(conn, bouquet);
                    loadBouquetAccessories(conn, bouquet);
                    return bouquet;
                }
            }
        } catch (SQLException e) {
            logger.error("Помилка при отриманні букета за ID {}: {}", id, e.getMessage(), e);
        }
        return null;
    }

    // Збереження даних

    /**
     * Зберігає букет у базі даних (створює новий або оновлює існуючий) разом із його квітами та аксесуарами.
     *
     * @param bouquet букет для збереження
     * @return true, якщо збереження успішне; false, якщо виникла помилка
     */
    public boolean saveBouquet(Bouquet bouquet) {
        if (bouquet == null) {
            logger.warn("Спроба зберегти null-букет.");
            return false;
        }

        try (Connection conn = dbManager.getConnection()) {
            conn.setAutoCommit(false);
            try {
                int bouquetId = bouquet.getId() > 0 ? updateBouquet(conn, bouquet) : insertBouquet(conn, bouquet);
                if (bouquetId <= 0) {
                    conn.rollback();
                    return false;
                }
                if (bouquet.getId() <= 0) {
                    setBouquetId(bouquet, bouquetId);
                }

                clearBouquetFlowers(conn, bouquetId);
                clearBouquetAccessories(conn, bouquetId);

                for (Flower flower : bouquet.getFlowers()) {
                    if (flower == null || flower.getId() <= 0) {
                        logger.warn("Пропущено null-квітку або квітку без ID для букета ID {}.", bouquetId);
                        continue;
                    }
                    if (!insertBouquetFlower(conn, bouquetId, flower.getId(), 1)) {
                        conn.rollback();
                        return false;
                    }
                }

                for (Accessory accessory : bouquet.getAccessories()) {
                    if (accessory == null || accessory.getId() <= 0) {
                        logger.warn("Пропущено null-аксесуар або аксесуар без ID для букета ID {}.", bouquetId);
                        continue;
                    }
                    if (!insertBouquetAccessory(conn, bouquetId, accessory.getId(), 1)) {
                        conn.rollback();
                        return false;
                    }
                }

                conn.commit();
                logger.info("Букет '{}' успішно збережено з ID {}.", bouquet.getName(), bouquetId);
                return true;
            } catch (SQLException e) {
                logger.error("Помилка при збереженні букета '{}': {}", bouquet.getName(), e.getMessage(), e);
                conn.rollback();
                return false;
            }
        } catch (SQLException e) {
            logger.error("Помилка з'єднання при збереженні букета '{}': {}", bouquet.getName(), e.getMessage(), e);
            return false;
        }
    }

    /**
     * Додає новий букет до бази даних.
     *
     * @param conn   з'єднання з базою даних
     * @param bouquet букет для додавання
     * @return ідентифікатор нового букета або -1, якщо додавання не вдалося
     * @throws SQLException якщо виникає помилка при роботі з базою даних
     */
    private int insertBouquet(Connection conn, Bouquet bouquet) throws SQLException {
        String sql = "INSERT INTO bouquets (name, description, image_path, discount) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, bouquet.getName());
            pstmt.setString(2, bouquet.getDescription());
            pstmt.setString(3, bouquet.getImagePath());
            pstmt.setDouble(4, bouquet.getDiscount());

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        return generatedKeys.getInt(1);
                    }
                }
            }
        }
        return -1;
    }

    /**
     * Оновлює існуючий букет у базі даних.
     *
     * @param conn   з'єднання з базою даних
     * @param bouquet букет для оновлення
     * @return ідентифікатор оновленого букета або -1, якщо оновлення не вдалося
     * @throws SQLException якщо виникає помилка при роботі з базою даних
     */
    private int updateBouquet(Connection conn, Bouquet bouquet) throws SQLException {
        String sql = "UPDATE bouquets SET name = ?, description = ?, image_path = ?, discount = ? WHERE id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, bouquet.getName());
            pstmt.setString(2, bouquet.getDescription());
            pstmt.setString(3, bouquet.getImagePath());
            pstmt.setDouble(4, bouquet.getDiscount());
            pstmt.setInt(5, bouquet.getId());

            return pstmt.executeUpdate() > 0 ? bouquet.getId() : -1;
        }
    }

    /**
     * Очищає всі квіти, пов'язані з букетом, у базі даних.
     *
     * @param conn      з'єднання з базою даних
     * @param bouquetId ідентифікатор букета
     * @throws SQLException якщо виникає помилка при роботі з базою даних
     */
    private void clearBouquetFlowers(Connection conn, int bouquetId) throws SQLException {
        String sql = "DELETE FROM bouquet_flowers WHERE bouquet_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, bouquetId);
            pstmt.executeUpdate();
        }
    }

    /**
     * Очищає всі аксесуари, пов'язані з букетом, у базі даних.
     *
     * @param conn      з'єднання з базою даних
     * @param bouquetId ідентифікатор букета
     * @throws SQLException якщо виникає помилка при роботі з базою даних
     */
    private void clearBouquetAccessories(Connection conn, int bouquetId) throws SQLException {
        String sql = "DELETE FROM bouquet_accessories WHERE bouquet_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, bouquetId);
            pstmt.executeUpdate();
        }
    }

    /**
     * Додає квітку до букета у базі даних.
     *
     * @param conn      з'єднання з базою даних
     * @param bouquetId ідентифікатор букета
     * @param flowerId  ідентифікатор квітки
     * @param quantity  кількість квіток
     * @return true, якщо додавання успішне; false, якщо виникла помилка
     * @throws SQLException якщо виникає помилка при роботі з базою даних
     */
    private boolean insertBouquetFlower(Connection conn, int bouquetId, int flowerId, int quantity) throws SQLException {
        String sql = "INSERT INTO bouquet_flowers (bouquet_id, flower_id, quantity) VALUES (?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, bouquetId);
            pstmt.setInt(2, flowerId);
            pstmt.setInt(3, quantity);
            return pstmt.executeUpdate() > 0;
        }
    }

    /**
     * Додає аксесуар до букета у базі даних.
     *
     * @param conn        з'єднання з базою даних
     * @param bouquetId   ідентифікатор букета
     * @param accessoryId ідентифікатор аксесуара
     * @param quantity    кількість аксесуарів
     * @return true, якщо додавання успішне; false, якщо виникла помилка
     * @throws SQLException якщо виникає помилка при роботі з базою даних
     */
    private boolean insertBouquetAccessory(Connection conn, int bouquetId, int accessoryId, int quantity) throws SQLException {
        String sql = "INSERT INTO bouquet_accessories (bouquet_id, accessory_id, quantity) VALUES (?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, bouquetId);
            pstmt.setInt(2, accessoryId);
            pstmt.setInt(3, quantity);
            return pstmt.executeUpdate() > 0;
        }
    }

    // Видалення даних

    /**
     * Видаляє букет за його ідентифікатором разом із пов'язаними квітами та аксесуарами.
     *
     * @param id ідентифікатор букета
     * @return true, якщо видалення успішне; false, якщо виникла помилка
     */
    public boolean deleteBouquet(int id) {
        try (Connection conn = dbManager.getConnection()) {
            conn.setAutoCommit(false);
            try {
                clearBouquetFlowers(conn, id);
                clearBouquetAccessories(conn, id);

                String sql = "DELETE FROM bouquets WHERE id = ?";
                try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setInt(1, id);
                    boolean success = pstmt.executeUpdate() > 0;
                    if (success) {
                        conn.commit();
                        logger.info("Букет з ID {} успішно видалено.", id);
                        return true;
                    } else {
                        conn.rollback();
                        return false;
                    }
                }
            } catch (SQLException e) {
                logger.error("Помилка при видаленні букета з ID {}: {}", id, e.getMessage(), e);
                conn.rollback();
                return false;
            }
        } catch (SQLException e) {
            logger.error("Помилка з'єднання при видаленні букета з ID {}: {}", id, e.getMessage(), e);
            return false;
        }
    }

    // Допоміжні методи

    /**
     * Завантажує квіти для букета з бази даних.
     *
     * @param conn   з'єднання з базою даних
     * @param bouquet букет, для якого завантажуються квіти
     * @throws SQLException якщо виникає помилка при роботі з базою даних
     */
    private void loadBouquetFlowers(Connection conn, Bouquet bouquet) throws SQLException {
        if (bouquet == null) {
            logger.warn("Спроба завантажити квіти для null-букета.");
            return;
        }
        String sql = "SELECT bf.flower_id FROM bouquet_flowers bf WHERE bf.bouquet_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, bouquet.getId());
            try (ResultSet rs = pstmt.executeQuery()) {
                List<Flower> flowers = new ArrayList<>();
                while (rs.next()) {
                    Flower flower = flowerDAO.getFlowerById(rs.getInt("flower_id"));
                    if (flower != null) {
                        flowers.add(flower);
                    } else {
                        logger.warn("Квітка з ID {} не знайдена для букета ID {}.", rs.getInt("flower_id"), bouquet.getId());
                    }
                }
                bouquet.setFlowers(flowers);
            }
        }
    }

    /**
     * Завантажує аксесуари для букета з бази даних.
     *
     * @param conn   з'єднання з базою даних
     * @param bouquet букет, для якого завантажуються аксесуари
     * @throws SQLException якщо виникає помилка при роботі з базою даних
     */
    private void loadBouquetAccessories(Connection conn, Bouquet bouquet) throws SQLException {
        if (bouquet == null) {
            logger.warn("Спроба завантажити аксесуари для null-букета.");
            return;
        }
        String sql = "SELECT ba.accessory_id FROM bouquet_accessories ba WHERE ba.bouquet_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, bouquet.getId());
            try (ResultSet rs = pstmt.executeQuery()) {
                List<Accessory> accessories = new ArrayList<>();
                while (rs.next()) {
                    Accessory accessory = accessoryDAO.getAccessoryById(rs.getInt("accessory_id"));
                    if (accessory != null) {
                        accessories.add(accessory);
                    } else {
                        logger.warn("Аксесуар з ID {} не знайдений для букета ID {}.", rs.getInt("accessory_id"), bouquet.getId());
                    }
                }
                bouquet.setAccessories(accessories);
            }
        }
    }

    /**
     * Перетворює рядок ResultSet на об'єкт Bouquet.
     *
     * @param rs ResultSet, який містить дані букета
     * @return об'єкт Bouquet
     * @throws SQLException якщо виникає помилка при роботі з ResultSet
     */
    Bouquet mapRowToBouquet(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        String name = rs.getString("name");
        String description = rs.getString("description");
        String imagePath = rs.getString("image_path");
        double discount = rs.getDouble("discount");

        Bouquet bouquet = new Bouquet(name, description, new ArrayList<>(), new ArrayList<>(), imagePath, discount);
        setBouquetId(bouquet, id);
        return bouquet;
    }

    /**
     * Встановлює ідентифікатор для букета.
     *
     * @param bouquet букет, для якого потрібно встановити ID
     * @param id      ідентифікатор, який потрібно встановити
     */
    private void setBouquetId(Bouquet bouquet, int id) {
        bouquet.setId(id);
    }
}