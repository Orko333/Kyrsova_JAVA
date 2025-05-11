package flowershop.dao;

import flowershop.database.DatabaseManager;
import flowershop.models.Accessory;
import flowershop.models.Bouquet;
import flowershop.models.Flower;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap; // Не використовується в поточному коді, можна видалити якщо не планується
import java.util.List;
import java.util.Map; // Не використовується в поточному коді, можна видалити якщо не планується

public class BouquetDAO {

    private static final Logger logger = LogManager.getLogger(BouquetDAO.class);
    private DatabaseManager dbManager;
    private FlowerDAO flowerDAO;
    private AccessoryDAO accessoryDAO;

    public BouquetDAO() {
        this.dbManager = DatabaseManager.getInstance();
        this.flowerDAO = new FlowerDAO(); // Має бути ініціалізовано
        this.accessoryDAO = new AccessoryDAO(); // Має бути ініціалізовано
        logger.debug("BouquetDAO ініціалізовано.");
    }

    public List<Bouquet> getAllBouquets() {
        logger.info("Спроба отримати всі букети.");
        List<Bouquet> bouquets = new ArrayList<>();
        String sql = "SELECT * FROM bouquets";

        Connection conn = null;
        try {
            conn = dbManager.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            // Map<Integer, Bouquet> bouquetMap = new HashMap<>(); // Не використовується далі, можна видалити
            while (rs.next()) {
                Bouquet bouquet = mapRowToBouquet(rs);
                // bouquetMap.put(bouquet.getId(), bouquet); // Не використовується далі
                // Завантаження квітів та аксесуарів відбувається нижче, після формування списку букетів
                bouquets.add(bouquet);
            }
            rs.close(); // Важливо закривати ResultSet
            stmt.close(); // Важливо закривати Statement

            // Завантажує квіти та аксесуари для кожного букета
            // Потенційна проблема N+1 запитів. Розгляньте можливість оптимізації, якщо продуктивність важлива.
            for (Bouquet bouquet : bouquets) {
                // Передаємо існуюче з'єднання, щоб уникнути створення нового для кожного букета
                loadBouquetFlowers(conn, bouquet);
                loadBouquetAccessories(conn, bouquet);
            }
            logger.info("Успішно отримано {} букетів.", bouquets.size());
        } catch (SQLException e) {
            logger.error("Помилка при отриманні всіх букетів: {}", e.getMessage(), e);
        } finally {
            dbManager.closeConnection(conn); // З'єднання закриється тут
        }
        return bouquets;
    }

    public Bouquet getBouquetById(int id) {
        logger.info("Спроба отримати букет за ID: {}", id);
        String sql = "SELECT * FROM bouquets WHERE id = ?";
        Bouquet bouquet = null;
        Connection conn = null;
        try {
            conn = dbManager.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                bouquet = mapRowToBouquet(rs);
                rs.close(); // Закриваємо ResultSet
                pstmt.close(); // Закриваємо PreparedStatement

                // Завантажуємо компоненти, використовуючи те саме з'єднання
                loadBouquetFlowers(conn, bouquet);
                loadBouquetAccessories(conn, bouquet);
                logger.info("Букет з ID {} ('{}') знайдено.", id, bouquet.getName());
            } else {
                rs.close();
                pstmt.close();
                logger.warn("Букет з ID {} не знайдено.", id);
            }
        } catch (SQLException e) {
            logger.error("Помилка при отриманні букета за ID {}: {}", id, e.getMessage(), e);
        } finally {
            dbManager.closeConnection(conn);
        }
        return bouquet;
    }

    public boolean saveBouquet(Bouquet bouquet) {
        if (bouquet == null) {
            logger.warn("Спроба зберегти null букет.");
            return false;
        }
        logger.info("Спроба зберегти букет: {}", bouquet.getName());
        Connection conn = null;
        boolean success = false;
        try {
            conn = dbManager.getConnection();
            conn.setAutoCommit(false); // Починаємо транзакцію

            int bouquetId;
            if (bouquet.getId() > 0) {
                logger.debug("Оновлення існуючого букета ID: {}", bouquet.getId());
                if (!updateBouquet(conn, bouquet)) {
                    conn.rollback();
                    logger.warn("Не вдалося оновити букет ID {} в базі даних.", bouquet.getId());
                    return false;
                }
                bouquetId = bouquet.getId();
            } else {
                logger.debug("Додавання нового букета: {}", bouquet.getName());
                bouquetId = insertBouquet(conn, bouquet);
                if (bouquetId <= 0) {
                    conn.rollback();
                    logger.warn("Не вдалося додати букет '{}' в базу даних.", bouquet.getName());
                    return false;
                }
                setBouquetId(bouquet, bouquetId); // Встановлюємо ID для об'єкта
                logger.info("Букет '{}' успішно додано з ID: {}", bouquet.getName(), bouquetId);
            }

            logger.debug("Очищення існуючих квітів та аксесуарів для букета ID: {}", bouquetId);
            clearBouquetFlowers(conn, bouquetId);
            clearBouquetAccessories(conn, bouquetId);

            logger.debug("Додавання квітів до букета ID: {}", bouquetId);
            for (Flower flower : bouquet.getFlowers()) {
                if (flower == null || flower.getId() <= 0) {
                    logger.warn("Спроба додати невалідну квітку (null або без ID) до букета ID {}", bouquetId);
                    continue;
                }
                if (!insertBouquetFlower(conn, bouquetId, flower.getId(), 1)) { // Припускаємо кількість 1
                    conn.rollback();
                    logger.error("Не вдалося додати квітку ID {} до букета ID {}", flower.getId(), bouquetId);
                    return false;
                }
            }

            logger.debug("Додавання аксесуарів до букета ID: {}", bouquetId);
            for (Accessory accessory : bouquet.getAccessories()) {
                if (accessory == null || accessory.getId() <= 0) {
                    logger.warn("Спроба додати невалідний аксесуар (null або без ID) до букета ID {}", bouquetId);
                    continue;
                }
                if (!insertBouquetAccessory(conn, bouquetId, accessory.getId(), 1)) { // Припускаємо кількість 1
                    conn.rollback();
                    logger.error("Не вдалося додати аксесуар ID {} до букета ID {}", accessory.getId(), bouquetId);
                    return false;
                }
            }

            conn.commit(); // Завершуємо транзакцію
            success = true;
            logger.info("Букет '{}' (ID: {}) успішно збережено.", bouquet.getName(), bouquetId);
        } catch (SQLException e) {
            logger.error("Помилка SQL при збереженні букета '{}': {}", bouquet.getName(), e.getMessage(), e);
            try {
                if (conn != null) {
                    logger.warn("Відкат транзакції для букета '{}' через помилку.", bouquet.getName());
                    conn.rollback();
                }
            } catch (SQLException ex) {
                logger.error("Критична помилка при відкаті транзакції для букета '{}': {}", bouquet.getName(), ex.getMessage(), ex);
            }
        } finally {
            try {
                if (conn != null) {
                    conn.setAutoCommit(true); // Повертаємо auto-commit
                    dbManager.closeConnection(conn);
                }
            } catch (SQLException e) {
                logger.error("Помилка при скиданні auto-commit або закритті з'єднання: {}", e.getMessage(), e);
            }
        }
        return success;
    }

    private int insertBouquet(Connection conn, Bouquet bouquet) throws SQLException {
        // Логування на початку методу
        logger.trace("Вставка нового букета '{}' в БД.", bouquet.getName());
        String sql = "INSERT INTO bouquets (name, description, image_path, discount) VALUES (?, ?, ?, ?)";
        PreparedStatement pstmt = null;
        ResultSet generatedKeys = null;
        try {
            pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            pstmt.setString(1, bouquet.getName());
            pstmt.setString(2, bouquet.getDescription());
            pstmt.setString(3, bouquet.getImagePath());
            pstmt.setDouble(4, bouquet.getDiscount());

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                generatedKeys = pstmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    int id = generatedKeys.getInt(1);
                    logger.debug("Букет '{}' вставлено з ID: {}", bouquet.getName(), id);
                    return id;
                } else {
                    logger.warn("Букет '{}' вставлено, але не вдалося отримати ID.", bouquet.getName());
                }
            } else {
                logger.warn("Букет '{}' не вставлено, affectedRows = 0.", bouquet.getName());
            }
        } finally {
            if (generatedKeys != null) try { generatedKeys.close(); } catch (SQLException ignored) {}
            if (pstmt != null) try { pstmt.close(); } catch (SQLException ignored) {}
        }
        return -1; // Повертаємо -1 у разі невдачі
    }

    private boolean updateBouquet(Connection conn, Bouquet bouquet) throws SQLException {
        logger.trace("Оновлення букета ID {} ('{}') в БД.", bouquet.getId(), bouquet.getName());
        String sql = "UPDATE bouquets SET name = ?, description = ?, image_path = ?, discount = ? WHERE id = ?";
        PreparedStatement pstmt = null;
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, bouquet.getName());
            pstmt.setString(2, bouquet.getDescription());
            pstmt.setString(3, bouquet.getImagePath());
            pstmt.setDouble(4, bouquet.getDiscount());
            pstmt.setInt(5, bouquet.getId());

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                logger.debug("Букет ID {} ('{}') успішно оновлено.", bouquet.getId(), bouquet.getName());
                return true;
            } else {
                logger.warn("Букет ID {} ('{}') не оновлено, можливо, він не існує.", bouquet.getId(), bouquet.getName());
                return false;
            }
        } finally {
            if (pstmt != null) try { pstmt.close(); } catch (SQLException ignored) {}
        }
    }

    private void clearBouquetFlowers(Connection conn, int bouquetId) throws SQLException {
        logger.trace("Видалення всіх квітів для букета ID: {}", bouquetId);
        String sql = "DELETE FROM bouquet_flowers WHERE bouquet_id = ?";
        PreparedStatement pstmt = null;
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, bouquetId);
            int deletedRows = pstmt.executeUpdate();
            logger.debug("{} рядків видалено з bouquet_flowers для букета ID {}", deletedRows, bouquetId);
        } finally {
            if (pstmt != null) try { pstmt.close(); } catch (SQLException ignored) {}
        }
    }

    private void clearBouquetAccessories(Connection conn, int bouquetId) throws SQLException {
        logger.trace("Видалення всіх аксесуарів для букета ID: {}", bouquetId);
        String sql = "DELETE FROM bouquet_accessories WHERE bouquet_id = ?";
        PreparedStatement pstmt = null;
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, bouquetId);
            int deletedRows = pstmt.executeUpdate();
            logger.debug("{} рядків видалено з bouquet_accessories для букета ID {}", deletedRows, bouquetId);
        } finally {
            if (pstmt != null) try { pstmt.close(); } catch (SQLException ignored) {}
        }
    }

    private boolean insertBouquetFlower(Connection conn, int bouquetId, int flowerId, int quantity) throws SQLException {
        logger.trace("Додавання квітки ID {} (кількість: {}) до букета ID {}", flowerId, quantity, bouquetId);
        String sql = "INSERT INTO bouquet_flowers (bouquet_id, flower_id, quantity) VALUES (?, ?, ?)";
        PreparedStatement pstmt = null;
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, bouquetId);
            pstmt.setInt(2, flowerId);
            pstmt.setInt(3, quantity);
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                logger.debug("Квітку ID {} додано до букета ID {}", flowerId, bouquetId);
                return true;
            } else {
                logger.warn("Не вдалося додати квітку ID {} до букета ID {}", flowerId, bouquetId);
                return false;
            }
        } finally {
            if (pstmt != null) try { pstmt.close(); } catch (SQLException ignored) {}
        }
    }

    private boolean insertBouquetAccessory(Connection conn, int bouquetId, int accessoryId, int quantity) throws SQLException {
        logger.trace("Додавання аксесуара ID {} (кількість: {}) до букета ID {}", accessoryId, quantity, bouquetId);
        String sql = "INSERT INTO bouquet_accessories (bouquet_id, accessory_id, quantity) VALUES (?, ?, ?)";
        PreparedStatement pstmt = null;
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, bouquetId);
            pstmt.setInt(2, accessoryId);
            pstmt.setInt(3, quantity);
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                logger.debug("Аксесуар ID {} додано до букета ID {}", accessoryId, bouquetId);
                return true;
            } else {
                logger.warn("Не вдалося додати аксесуар ID {} до букета ID {}", accessoryId, bouquetId);
                return false;
            }
        } finally {
            if (pstmt != null) try { pstmt.close(); } catch (SQLException ignored) {}
        }
    }

    public boolean deleteBouquet(int id) {
        logger.info("Спроба видалити букет за ID: {}", id);
        String sql = "DELETE FROM bouquets WHERE id = ?"; // Потрібно також видаляти пов'язані записи з bouquet_flowers та bouquet_accessories, або налаштувати CASCADE DELETE в БД
        Connection conn = null;
        boolean success = false;
        try {
            conn = dbManager.getConnection();
            conn.setAutoCommit(false); // Починаємо транзакцію, якщо видаляємо пов'язані дані

            // Спочатку видаляємо пов'язані квіти та аксесуари
            clearBouquetFlowers(conn, id);
            clearBouquetAccessories(conn, id);

            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, id);
            int affectedRows = pstmt.executeUpdate();
            pstmt.close();

            if (affectedRows > 0) {
                conn.commit();
                success = true;
                logger.info("Букет з ID {} успішно видалено разом з компонентами.", id);
            } else {
                conn.rollback(); // Якщо сам букет не знайдено, відкочуємо видалення компонентів (хоча вони могли й не існувати)
                logger.warn("Не вдалося видалити букет з ID {}. Можливо, букет не знайдено.", id);
            }
        } catch (SQLException e) {
            logger.error("Помилка SQL при видаленні букета з ID {}: {}", id, e.getMessage(), e);
            try {
                if (conn != null) {
                    logger.warn("Відкат транзакції при видаленні букета ID {} через помилку.", id);
                    conn.rollback();
                }
            } catch (SQLException ex) {
                logger.error("Критична помилка при відкаті транзакції видалення букета ID {}: {}", id, ex.getMessage(), ex);
            }
        } finally {
            try {
                if (conn != null) {
                    conn.setAutoCommit(true);
                    dbManager.closeConnection(conn);
                }
            } catch (SQLException e) {
                logger.error("Помилка при скиданні auto-commit або закритті з'єднання після видалення букета: {}", e.getMessage(), e);
            }
        }
        return success;
    }

    private void loadBouquetFlowers(Connection conn, Bouquet bouquet) throws SQLException {
        if (bouquet == null) return;
        logger.trace("Завантаження квітів для букета ID: {}", bouquet.getId());
        // Оскільки flowerDAO.getFlowerById(id) відкриває нове з'єднання,
        // для оптимізації краще отримувати дані квітів напряму тут, використовуючи передане з'єднання.
        // Або переробити FlowerDAO.getFlowerById так, щоб він міг приймати існуюче з'єднання.
        // Поточна реалізація з getFlowerById може призвести до N+1 проблеми з'єднань.
        // Для простоти залишаю як є, але це місце для потенційної оптимізації.

        String sql = "SELECT bf.flower_id " + // Достатньо отримати ID, а потім використати FlowerDAO
                "FROM bouquet_flowers bf " +
                "WHERE bf.bouquet_id = ?";
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, bouquet.getId());
            rs = pstmt.executeQuery();
            List<Flower> flowers = new ArrayList<>();
            while (rs.next()) {
                Flower flower = flowerDAO.getFlowerById(rs.getInt("flower_id")); // Це створює нове з'єднання
                if (flower != null) {
                    flowers.add(flower);
                    // bouquet.addFlower(flower); // Краще зібрати список і додати один раз
                } else {
                    logger.warn("Не вдалося завантажити квітку ID {} для букета ID {}", rs.getInt("flower_id"), bouquet.getId());
                }
            }
            bouquet.setFlowers(flowers); // Встановлюємо список квітів
            logger.debug("Для букета ID {} завантажено {} квітів.", bouquet.getId(), flowers.size());
        } finally {
            if (rs != null) try { rs.close(); } catch (SQLException ignored) {}
            if (pstmt != null) try { pstmt.close(); } catch (SQLException ignored) {}
        }
    }


    private void loadBouquetAccessories(Connection conn, Bouquet bouquet) throws SQLException {
        if (bouquet == null) return;
        logger.trace("Завантаження аксесуарів для букета ID: {}", bouquet.getId());
        // Аналогічно loadBouquetFlowers, це місце для потенційної оптимізації.

        String sql = "SELECT ba.accessory_id " +
                "FROM bouquet_accessories ba " +
                "WHERE ba.bouquet_id = ?";
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, bouquet.getId());
            rs = pstmt.executeQuery();
            List<Accessory> accessories = new ArrayList<>();
            while (rs.next()) {
                Accessory accessory = accessoryDAO.getAccessoryById(rs.getInt("accessory_id")); // Це створює нове з'єднання
                if (accessory != null) {
                    accessories.add(accessory);
                    // bouquet.addAccessory(accessory);
                } else {
                    logger.warn("Не вдалося завантажити аксесуар ID {} для букета ID {}", rs.getInt("accessory_id"), bouquet.getId());
                }
            }
            bouquet.setAccessories(accessories); // Встановлюємо список аксесуарів
            logger.debug("Для букета ID {} завантажено {} аксесуарів.", bouquet.getId(), accessories.size());
        } finally {
            if (rs != null) try { rs.close(); } catch (SQLException ignored) {}
            if (pstmt != null) try { pstmt.close(); } catch (SQLException ignored) {}
        }
    }

    Bouquet mapRowToBouquet(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        String name = rs.getString("name");
        String description = rs.getString("description");
        String imagePath = rs.getString("image_path");
        double discount = rs.getDouble("discount");

        // Квіти та аксесуари завантажуються окремо
        Bouquet bouquet = new Bouquet(name, description, new ArrayList<>(), new ArrayList<>(), imagePath, discount);
        setBouquetId(bouquet, id); // Використовуємо метод setId з моделі Bouquet
        logger.trace("Букет ID {} ('{}') змаплено з ResultSet.", id, name);
        return bouquet;
    }

    // Метод setBouquetId більше не потрібен тут, якщо в моделі Bouquet є публічний сеттер для ID
    // Або якщо ID встановлюється в конструкторі і є незмінним після створення об'єкта з БД.
    // Поточний код використовує bouquet.setId(id), що є кращим підходом.
    private void setBouquetId(Bouquet bouquet, int id) {
        // Припускаємо, що клас Bouquet має метод setId(int id)
        bouquet.setId(id);
        logger.trace("ID {} встановлено для букета '{}'.", id, bouquet.getName());
    }
}