package flowershop.dao;

import flowershop.database.DatabaseManager;
import flowershop.models.Flower;
import flowershop.models.Flower.FlowerType;
import org.junit.jupiter.api.*;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.sql.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class FlowerDAOTest {

    private FlowerDAO flowerDAO;
    private static MockedStatic<DatabaseManager> mockedDatabaseManager;
    private DatabaseManager dbManager;
    private Connection connection;
    private PreparedStatement preparedStatement;
    private Statement statement;
    private ResultSet resultSet;

    @BeforeAll
    static void setUpBeforeAll() {
        mockedDatabaseManager = Mockito.mockStatic(DatabaseManager.class);
    }

    @AfterAll
    static void tearDownAfterAll() {
        mockedDatabaseManager.close();
    }

    @BeforeEach
    void setUp() throws SQLException {
        dbManager = mock(DatabaseManager.class);
        connection = mock(Connection.class);
        preparedStatement = mock(PreparedStatement.class);
        statement = mock(Statement.class);
        resultSet = mock(ResultSet.class);

        mockedDatabaseManager.when(DatabaseManager::getInstance).thenReturn(dbManager);
        when(dbManager.getConnection()).thenReturn(connection);

        flowerDAO = new FlowerDAO();
    }

    @AfterEach
    void tearDown() {
        reset(dbManager, connection, preparedStatement, statement, resultSet);
    }

    @Test
    void getAllFlowers_Success() throws SQLException {
        when(connection.createStatement()).thenReturn(statement);
        when(statement.executeQuery(anyString())).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, true, false);

        mockFlowerResultSet(resultSet, 1, "ROSE", 10.5, 90, 30, "Red", "Ukraine", false, "path1.jpg", 5);
        when(resultSet.getString("type_name")).thenReturn("ROSE");
        when(resultSet.getString("display_name")).thenReturn("Троянда");

        List<Flower> flowers = flowerDAO.getAllFlowers();

        assertNotNull(flowers);
        assertEquals(2, flowers.size());
        verify(statement).close();
        verify(resultSet).close();
    }

    @Test
    void getAllFlowers_Exception() throws SQLException {
        when(connection.createStatement()).thenThrow(new SQLException("Database error"));

        List<Flower> flowers = flowerDAO.getAllFlowers();

        assertNotNull(flowers);
        assertTrue(flowers.isEmpty());
    }

    @Test
    void getFlowerById_Success() throws SQLException {
        int flowerId = 1;
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);

        mockFlowerResultSet(resultSet, flowerId, "ROSE", 10.5, 90, 30, "Red", "Ukraine", false, "path1.jpg", 5);
        when(resultSet.getString("type_name")).thenReturn("ROSE");
        when(resultSet.getString("display_name")).thenReturn("Троянда");

        Flower flower = flowerDAO.getFlowerById(flowerId);

        assertNotNull(flower);
        assertEquals(flowerId, flower.getId());
        verify(preparedStatement).setInt(1, flowerId);
        verify(preparedStatement).close();
        verify(resultSet).close();
    }

    @Test
    void getFlowerById_NotFound() throws SQLException {
        int flowerId = 999;
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        Flower flower = flowerDAO.getFlowerById(flowerId);

        assertNull(flower);
    }

    @Test
    void getFlowerById_Exception() throws SQLException {
        int flowerId = 1;
        when(connection.prepareStatement(anyString())).thenThrow(new SQLException("Database error"));

        Flower flower = flowerDAO.getFlowerById(flowerId);

        assertNull(flower);
    }

    @Test
    void saveFlower_InsertSuccess() throws SQLException {
        Flower flower = new Flower(FlowerType.ROSE, 10.5, 90, 30, "Red", "Ukraine", false, "path1.jpg", 5);
        when(connection.prepareStatement(anyString(), anyInt())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(1);

        ResultSet generatedKeys = mock(ResultSet.class);
        when(preparedStatement.getGeneratedKeys()).thenReturn(generatedKeys);
        when(generatedKeys.next()).thenReturn(true);
        when(generatedKeys.getInt(1)).thenReturn(1);

        boolean result = flowerDAO.saveFlower(flower);

        assertTrue(result);
        assertEquals(1, flower.getId());
        verify(preparedStatement).setString(1, "ROSE");
        verify(preparedStatement).close();
        verify(generatedKeys).close();
    }

    @Test
    void saveFlower_UpdateSuccess() throws SQLException {
        Flower flower = new Flower(FlowerType.ROSE, 10.5, 90, 30, "Red", "Ukraine", false, "path1.jpg", 5);
        flower.setId(1);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(1);

        boolean result = flowerDAO.saveFlower(flower);

        assertTrue(result);
        verify(preparedStatement).setInt(11, 1);
    }

    @Test
    void saveFlower_NullFlower() {
        boolean result = flowerDAO.saveFlower(null);

        assertFalse(result);
    }

    @Test
    void saveFlower_InsertFailure() throws SQLException {
        Flower flower = new Flower(FlowerType.ROSE, 10.5, 90, 30, "Red", "Ukraine", false, "path1.jpg", 5);
        when(connection.prepareStatement(anyString(), anyInt())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(0);

        boolean result = flowerDAO.saveFlower(flower);

        assertFalse(result);
    }

    @Test
    void saveFlower_UpdateFailure() throws SQLException {
        Flower flower = new Flower(FlowerType.ROSE, 10.5, 90, 30, "Red", "Ukraine", false, "path1.jpg", 5);
        flower.setId(1);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(0);

        boolean result = flowerDAO.saveFlower(flower);

        assertFalse(result);
    }

    @Test
    void deleteFlower_Success() throws SQLException {
        int flowerId = 1;
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(1);

        boolean result = flowerDAO.deleteFlower(flowerId);

        assertTrue(result);
        verify(preparedStatement).setInt(1, flowerId);
    }

    @Test
    void deleteFlower_Failure() throws SQLException {
        int flowerId = 1;
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(0);

        boolean result = flowerDAO.deleteFlower(flowerId);

        assertFalse(result);
    }

    @Test
    void deleteFlower_Exception() throws SQLException {
        int flowerId = 1;
        when(connection.prepareStatement(anyString())).thenThrow(new SQLException("Database error"));

        boolean result = flowerDAO.deleteFlower(flowerId);

        assertFalse(result);
    }

    @Test
    void updateStock_Success() throws SQLException {
        int flowerId = 1;
        int quantity = 5;
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);

        flowerDAO.updateStock(flowerId, quantity);

        verify(preparedStatement).setInt(1, quantity);
        verify(preparedStatement).setInt(2, flowerId);
        verify(preparedStatement).executeUpdate();
    }

    @Test
    void updateStock_Exception() throws SQLException {
        int flowerId = 1;
        int quantity = 5;
        when(connection.prepareStatement(anyString())).thenThrow(new SQLException("Database error"));

        flowerDAO.updateStock(flowerId, quantity);
    }

    @Test
    void mapRowToFlower_Success() throws SQLException {
        int id = 1;
        mockFlowerResultSet(resultSet, id, "ROSE", 10.5, 90, 30, "Red", "Ukraine", false, "path1.jpg", 5);
        when(resultSet.getString("type_name")).thenReturn("ROSE");
        when(resultSet.getString("display_name")).thenReturn("Троянда");

        Flower flower = flowerDAO.mapRowToFlower(resultSet);

        assertNotNull(flower);
        assertEquals(id, flower.getId());
        assertEquals(FlowerType.ROSE, flower.getType());
        assertEquals(10.5, flower.getPrice());
        assertEquals(90, flower.getFreshness());
        assertEquals(30, flower.getStemLength());
        assertEquals("Red", flower.getColor());
        assertEquals("Ukraine", flower.getCountryOfOrigin());
        assertFalse(flower.isPotted());
        assertEquals("path1.jpg", flower.getImagePath());
        assertEquals(5, flower.getStockQuantity());
    }

    @Test
    void mapRowToFlower_InvalidType() throws SQLException {
        mockFlowerResultSet(resultSet, 1, "INVALID", 10.5, 90, 30, "Red", "Ukraine", false, "path1.jpg", 5);
        when(resultSet.getString("type_name")).thenReturn("INVALID");

        assertThrows(SQLException.class, () -> flowerDAO.mapRowToFlower(resultSet));
    }

    @Test
    void setFlowerId_Success() {
        Flower flower = new Flower(FlowerType.ROSE, 10.5, 90, 30);
        int id = 1;

        flowerDAO.setFlowerId(flower, id);

        assertEquals(id, flower.getId());
    }

    @Test
    void setFlowerId_ReflectionException() {
        Flower flower = new Flower(FlowerType.ROSE, 10.5, 90, 30) {
            @Override
            public int getId() {
                return -1;
            }
        };
        int id = 1;

        flowerDAO.setFlowerId(flower, id);

        assertEquals(-1, flower.getId());
    }

    private void mockFlowerResultSet(ResultSet rs, int id, String type, double price, int freshness,
                                     int stemLength, String color, String country, boolean isPotted,
                                     String imagePath, int stockQuantity) throws SQLException {
        when(rs.getInt("id")).thenReturn(id);
        when(rs.getString("type_name")).thenReturn(type);
        when(rs.getDouble("price")).thenReturn(price);
        when(rs.getInt("freshness")).thenReturn(freshness);
        when(rs.getInt("stem_length")).thenReturn(stemLength);
        when(rs.getString("color")).thenReturn(color);
        when(rs.getString("country_of_origin")).thenReturn(country);
        when(rs.getBoolean("is_potted")).thenReturn(isPotted);
        when(rs.getString("image_path")).thenReturn(imagePath);
        when(rs.getInt("stock_quantity")).thenReturn(stockQuantity);
    }
}