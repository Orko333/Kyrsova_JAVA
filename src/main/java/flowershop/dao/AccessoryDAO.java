package flowershop.dao;

import flowershop.database.DatabaseManager;
import flowershop.models.Accessory;
import flowershop.models.Accessory.AccessoryType;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AccessoryDAO {

    private static final Logger logger = LogManager.getLogger(AccessoryDAO.class);
    private DatabaseManager dbManager;

    public AccessoryDAO() {
        this.dbManager = DatabaseManager.getInstance();
        logger.debug("AccessoryDAO ініціалізовано.");
    }

    public List<Accessory> getAllAccessories() {
        logger.info("Спроба отримати всі аксесуари.");
        List<Accessory> accessories = new ArrayList<>();
        String sql = "SELECT a.*, t.name as type_name, t.display_name " +
                "FROM accessories a " +
                "JOIN accessory_types t ON a.type_id = t.id";

        Connection conn = null;
        try {
            conn = dbManager.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {
                accessories.add(mapRowToAccessory(rs));
            }
            logger.info("Успішно отримано {} аксесуарів.", accessories.size());
        } catch (SQLException e) {
            logger.error("Помилка при отриманні всіх аксесуарів: {}", e.getMessage(), e);
        } finally {
            dbManager.closeConnection(conn);
        }
        return accessories;
    }

    public Accessory getAccessoryById(int id) {
        logger.info("Спроба отримати аксесуар за ID: {}", id);
        String sql = "SELECT a.*, t.name as type_name, t.display_name " +
                "FROM accessories a " +
                "JOIN accessory_types t ON a.type_id = t.id " +
                "WHERE a.id = ?";

        Connection conn = null;
        try {
            conn = dbManager.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                Accessory accessory = mapRowToAccessory(rs);
                logger.info("Аксесуар з ID {} знайдено: {}", id, accessory.getName());
                return accessory;
            } else {
                logger.warn("Аксесуар з ID {} не знайдено.", id);
            }
        } catch (SQLException e) {
            logger.error("Помилка при отриманні аксесуара за ID {}: {}", id, e.getMessage(), e);
        } finally {
            dbManager.closeConnection(conn);
        }
        return null;
    }

    public boolean saveAccessory(Accessory accessory) {
        if (accessory == null) {
            logger.warn("Спроба зберегти null аксесуар.");
            return false;
        }
        if (accessory.getId() > 0) {
            logger.info("Спроба оновити аксесуар ID: {}, Назва: {}", accessory.getId(), accessory.getName());
            return updateAccessory(accessory);
        } else {
            logger.info("Спроба додати новий аксесуар: {}", accessory.getName());
            return insertAccessory(accessory);
        }
    }

    private boolean insertAccessory(Accessory accessory) {
        String sql = "INSERT INTO accessories (type_id, name, price, description, " +
                "image_path, stock_quantity, color, size) " +
                "VALUES ((SELECT id FROM accessory_types WHERE name = ?), ?, ?, ?, ?, ?, ?, ?)";

        Connection conn = null;
        try {
            conn = dbManager.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

            pstmt.setString(1, accessory.getType().name());
            pstmt.setString(2, accessory.getName());
            pstmt.setDouble(3, accessory.getPrice());
            pstmt.setString(4, accessory.getDescription());
            pstmt.setString(5, accessory.getImagePath());
            pstmt.setInt(6, accessory.getStockQuantity());
            pstmt.setString(7, accessory.getColor());
            pstmt.setString(8, accessory.getSize());

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                ResultSet generatedKeys = pstmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    setAccessoryId(accessory, generatedKeys.getInt(1));
                    logger.info("Аксесуар '{}' успішно додано з ID: {}", accessory.getName(), accessory.getId());
                    return true;
                }
            }
            logger.warn("Не вдалося додати аксесуар '{}' або отримати згенерований ID.", accessory.getName());
        } catch (SQLException e) {
            logger.error("Помилка при додаванні аксесуара '{}': {}", accessory.getName(), e.getMessage(), e);
        } finally {
            dbManager.closeConnection(conn);
        }
        return false;
    }

    private boolean updateAccessory(Accessory accessory) {
        String sql = "UPDATE accessories SET type_id = (SELECT id FROM accessory_types WHERE name = ?), " +
                "name = ?, price = ?, description = ?, image_path = ?, " +
                "stock_quantity = ?, color = ?, size = ? " +
                "WHERE id = ?";

        Connection conn = null;
        try {
            conn = dbManager.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);

            pstmt.setString(1, accessory.getType().name());
            pstmt.setString(2, accessory.getName());
            pstmt.setDouble(3, accessory.getPrice());
            pstmt.setString(4, accessory.getDescription());
            pstmt.setString(5, accessory.getImagePath());
            pstmt.setInt(6, accessory.getStockQuantity());
            pstmt.setString(7, accessory.getColor());
            pstmt.setString(8, accessory.getSize());
            pstmt.setInt(9, accessory.getId());

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                logger.info("Аксесуар ID {} '{}' успішно оновлено.", accessory.getId(), accessory.getName());
                return true;
            } else {
                logger.warn("Не вдалося оновити аксесуар ID {}. Можливо, аксесуар не знайдено.", accessory.getId());
                return false;
            }
        } catch (SQLException e) {
            logger.error("Помилка при оновленні аксесуара ID {}: {}", accessory.getId(), e.getMessage(), e);
        } finally {
            dbManager.closeConnection(conn);
        }
        return false;
    }

    public boolean deleteAccessory(int id) {
        logger.info("Спроба видалити аксесуар за ID: {}", id);
        String sql = "DELETE FROM accessories WHERE id = ?";

        Connection conn = null;
        try {
            conn = dbManager.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, id);

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                logger.info("Аксесуар з ID {} успішно видалено.", id);
                return true;
            } else {
                logger.warn("Не вдалося видалити аксесуар з ID {}. Можливо, аксесуар не знайдено.", id);
                return false;
            }
        } catch (SQLException e) {
            logger.error("Помилка при видаленні аксесуара з ID {}: {}", id, e.getMessage(), e);
        } finally {
            dbManager.closeConnection(conn);
        }
        return false;
    }

    public void updateStock(int accessoryId, int quantity) {
        logger.info("Спроба оновити залишок для аксесуара ID {} на {}", accessoryId, quantity);
        String sql = "UPDATE accessories SET stock_quantity = stock_quantity + ? WHERE id = ?";

        Connection conn = null;
        try {
            conn = dbManager.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, quantity);
            pstmt.setInt(2, accessoryId);

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                logger.info("Залишок для аксесуара ID {} успішно оновлено.", accessoryId);
            } else {
                logger.warn("Не вдалося оновити залишок для аксесуара ID {}. Можливо, аксесуар не знайдено.", accessoryId);
            }
        } catch (SQLException e) {
            logger.error("Помилка при оновленні залишку аксесуара ID {}: {}", accessoryId, e.getMessage(), e);
        } finally {
            dbManager.closeConnection(conn);
        }
    }

    Accessory mapRowToAccessory(ResultSet rs) throws SQLException {
        // Логування на початку маппінгу може бути надлишковим, якщо викликається часто
        // logger.trace("Маппінг рядка ResultSet на Accessory.");
        int id = rs.getInt("id");
        String typeName = rs.getString("type_name");
        String name = rs.getString("name");
        double price = rs.getDouble("price");
        String description = rs.getString("description");
        String imagePath = rs.getString("image_path");
        int stockQuantity = rs.getInt("stock_quantity");
        String color = rs.getString("color");
        String size = rs.getString("size");

        AccessoryType accessoryType = null;
        try {
            accessoryType = AccessoryType.valueOf(typeName);
        } catch (IllegalArgumentException e) {
            logger.error("Невідомий тип аксесуара '{}' для ID {}", typeName, id, e);
            // Можна кинути виняток або повернути null/об'єкт за замовчуванням, залежно від логіки
            throw new SQLException("Невідомий тип аксесуара: " + typeName, e);
        }


        Accessory accessory = new Accessory(
                name, price, description, imagePath,
                stockQuantity, accessoryType, color, size
        );
        setAccessoryId(accessory, id);
        // logger.trace("Аксесуар ID {} ('{}') успішно змаплено.", id, name);
        return accessory;
    }

    void setAccessoryId(Accessory accessory, int id) {
        try {
            java.lang.reflect.Field idField = Accessory.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(accessory, id);
            logger.trace("ID {} встановлено для аксесуара '{}' через рефлексію.", id, accessory.getName());
        } catch (NoSuchFieldException | IllegalAccessException e) {
            logger.error("Помилка рефлексії при встановленні ID {} для аксесуара '{}': {}", id, accessory.getName(), e.getMessage(), e);
            // Розгляньте, чи потрібно тут кидати RuntimeException, якщо ID є критичним
        }
    }
}