package flowershop.dao;

import flowershop.database.DatabaseManager;
import flowershop.models.Flower;
import flowershop.models.Flower.FlowerType;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FlowerDAO {

    private static final Logger logger = LogManager.getLogger(FlowerDAO.class);
    private DatabaseManager dbManager;

    public FlowerDAO() {
        this.dbManager = DatabaseManager.getInstance();
        logger.debug("FlowerDAO ініціалізовано.");
    }

    public List<Flower> getAllFlowers() {
        logger.info("Спроба отримати всі квіти.");
        List<Flower> flowers = new ArrayList<>();
        String sql = "SELECT f.*, t.name as type_name, t.display_name " +
                "FROM flowers f " +
                "JOIN flower_types t ON f.type_id = t.id";

        Connection conn = null;
        try {
            conn = dbManager.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {
                flowers.add(mapRowToFlower(rs));
            }
            logger.info("Успішно отримано {} квітів.", flowers.size());
        } catch (SQLException e) {
            logger.error("Помилка при отриманні всіх квітів: {}", e.getMessage(), e);
        } finally {
            dbManager.closeConnection(conn);
        }
        return flowers;
    }

    public Flower getFlowerById(int id) {
        logger.info("Спроба отримати квітку за ID: {}", id);
        String sql = "SELECT f.*, t.name as type_name, t.display_name " +
                "FROM flowers f " +
                "JOIN flower_types t ON f.type_id = t.id " +
                "WHERE f.id = ?";

        Connection conn = null;
        try {
            conn = dbManager.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                Flower flower = mapRowToFlower(rs);
                logger.info("Квітку з ID {} знайдено: Тип - {}, Колір - {}", id, flower.getType(), flower.getColor());
                return flower;
            } else {
                logger.warn("Квітку з ID {} не знайдено.", id);
            }
        } catch (SQLException e) {
            logger.error("Помилка при отриманні квітки за ID {}: {}", id, e.getMessage(), e);
        } finally {
            dbManager.closeConnection(conn);
        }
        return null;
    }

    public boolean saveFlower(Flower flower) {
        if (flower == null) {
            logger.warn("Спроба зберегти null квітку.");
            return false;
        }
        if (flower.getId() > 0) {
            logger.info("Спроба оновити квітку ID: {}, Тип: {}, Колір: {}", flower.getId(), flower.getType(), flower.getColor());
            return updateFlower(flower);
        } else {
            logger.info("Спроба додати нову квітку: Тип: {}, Колір: {}", flower.getType(), flower.getColor());
            return insertFlower(flower);
        }
    }

    private boolean insertFlower(Flower flower) {
        String sql = "INSERT INTO flowers (type_id, price, freshness, stem_length, color, " +
                "country_of_origin, is_potted, image_path, stock_quantity, description) " +
                "VALUES ((SELECT id FROM flower_types WHERE name = ?), ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        Connection conn = null;
        try {
            conn = dbManager.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

            pstmt.setString(1, flower.getType().name());
            pstmt.setDouble(2, flower.getPrice());
            pstmt.setInt(3, flower.getFreshness());
            pstmt.setInt(4, flower.getStemLength());
            pstmt.setString(5, flower.getColor());
            pstmt.setString(6, flower.getCountryOfOrigin());
            pstmt.setBoolean(7, flower.isPotted());
            pstmt.setString(8, flower.getImagePath());
            pstmt.setInt(9, flower.getStockQuantity());
            pstmt.setString(10, flower.generateDescription()); // Якщо generateDescription може кинути виняток, його теж треба логувати

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                ResultSet generatedKeys = pstmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    setFlowerId(flower, generatedKeys.getInt(1));
                    logger.info("Квітку '{} {}' успішно додано з ID: {}", flower.getColor(), flower.getType(), flower.getId());
                    return true;
                }
            }
            logger.warn("Не вдалося додати квітку '{} {}' або отримати згенерований ID.", flower.getColor(), flower.getType());
        } catch (SQLException e) {
            logger.error("Помилка при додаванні квітки '{} {}': {}", flower.getColor(), flower.getType(), e.getMessage(), e);
        } finally {
            dbManager.closeConnection(conn);
        }
        return false;
    }

    private boolean updateFlower(Flower flower) {
        String sql = "UPDATE flowers SET type_id = (SELECT id FROM flower_types WHERE name = ?), " +
                "price = ?, freshness = ?, stem_length = ?, color = ?, country_of_origin = ?, " +
                "is_potted = ?, image_path = ?, stock_quantity = ?, description = ? " +
                "WHERE id = ?";

        Connection conn = null;
        try {
            conn = dbManager.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);

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
            pstmt.setInt(11, flower.getId());

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                logger.info("Квітку ID {} ('{} {}') успішно оновлено.", flower.getId(), flower.getColor(), flower.getType());
                return true;
            } else {
                logger.warn("Не вдалося оновити квітку ID {}. Можливо, квітка не знайдена.", flower.getId());
                return false;
            }
        } catch (SQLException e) {
            logger.error("Помилка при оновленні квітки ID {}: {}", flower.getId(), e.getMessage(), e);
        } finally {
            dbManager.closeConnection(conn);
        }
        return false;
    }

    public boolean deleteFlower(int id) {
        logger.info("Спроба видалити квітку за ID: {}", id);
        String sql = "DELETE FROM flowers WHERE id = ?";

        Connection conn = null;
        try {
            conn = dbManager.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, id);

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                logger.info("Квітку з ID {} успішно видалено.", id);
                return true;
            } else {
                logger.warn("Не вдалося видалити квітку з ID {}. Можливо, квітка не знайдена.", id);
                return false;
            }
        } catch (SQLException e) {
            logger.error("Помилка при видаленні квітки з ID {}: {}", id, e.getMessage(), e);
        } finally {
            dbManager.closeConnection(conn);
        }
        return false;
    }

    public void updateStock(int flowerId, int quantity) {
        logger.info("Спроба оновити залишок для квітки ID {} на {}", flowerId, quantity);
        String sql = "UPDATE flowers SET stock_quantity = stock_quantity + ? WHERE id = ?";

        Connection conn = null;
        try {
            conn = dbManager.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, quantity);
            pstmt.setInt(2, flowerId);

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                logger.info("Залишок для квітки ID {} успішно оновлено.", flowerId);
            } else {
                logger.warn("Не вдалося оновити залишок для квітки ID {}. Можливо, квітка не знайдена.", flowerId);
            }
        } catch (SQLException e) {
            logger.error("Помилка при оновленні залишку квітки ID {}: {}", flowerId, e.getMessage(), e);
        } finally {
            dbManager.closeConnection(conn);
        }
    }

    Flower mapRowToFlower(ResultSet rs) throws SQLException {
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

        FlowerType flowerType = null;
        try {
            flowerType = FlowerType.valueOf(typeName);
        } catch (IllegalArgumentException e) {
            logger.error("Невідомий тип квітки '{}' для ID {}", typeName, id, e);
            throw new SQLException("Невідомий тип квітки: " + typeName, e);
        }


        Flower flower = new Flower(
                flowerType, price, freshness, stemLength,
                color, countryOfOrigin, isPotted, imagePath, stockQuantity
        );
        setFlowerId(flower, id);
        return flower;
    }

    void setFlowerId(Flower flower, int id) {
        try {
            java.lang.reflect.Field idField = Flower.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(flower, id);
            logger.trace("ID {} встановлено для квітки '{} {}' через рефлексію.", id, flower.getColor(), flower.getType());
        } catch (NoSuchFieldException | IllegalAccessException e) {
            logger.error("Помилка рефлексії при встановленні ID {} для квітки '{} {}': {}", id, flower.getColor(), flower.getType(), e.getMessage(), e);
        }
    }
}