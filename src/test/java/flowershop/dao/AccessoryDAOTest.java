package flowershop.dao;

import flowershop.database.DatabaseManager;
import flowershop.models.Accessory;
import flowershop.models.Accessory.AccessoryType;
import org.junit.jupiter.api.*;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.sql.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class AccessoryDAOTest {

    private AccessoryDAO accessoryDAO;
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

        accessoryDAO = new AccessoryDAO();
    }

    @AfterEach
    void tearDown() {
        reset(dbManager, connection, preparedStatement, statement, resultSet);
    }

    @Test
    void getAllAccessories_Success() throws SQLException {
        when(connection.createStatement()).thenReturn(statement);
        when(statement.executeQuery(anyString())).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, true, false);

        when(resultSet.getInt("id")).thenReturn(1, 2);
        when(resultSet.getString("type_name")).thenReturn("RIBBON", "VASE");
        when(resultSet.getString("name")).thenReturn("Red Ribbon", "Glass Vase");
        when(resultSet.getDouble("price")).thenReturn(5.0, 25.0);
        when(resultSet.getString("description")).thenReturn("Beautiful red ribbon", "Elegant glass vase");
        when(resultSet.getString("image_path")).thenReturn("ribbon.jpg", "vase.jpg");
        when(resultSet.getInt("stock_quantity")).thenReturn(100, 20);
        when(resultSet.getString("color")).thenReturn("Red", "Transparent");
        when(resultSet.getString("size")).thenReturn("1m", "Large");
        when(resultSet.getString("display_name")).thenReturn("Стрічка", "Ваза");

        List<Accessory> accessories = accessoryDAO.getAllAccessories();

        assertNotNull(accessories);
        assertEquals(2, accessories.size());
        verify(statement).close();
        verify(resultSet).close();
        verify(connection).close();
    }

    @Test
    void getAllAccessories_SQLException() throws SQLException {
        when(connection.createStatement()).thenThrow(new SQLException("Database connection error"));

        List<Accessory> accessories = accessoryDAO.getAllAccessories();

        assertNotNull(accessories);
        assertTrue(accessories.isEmpty());
        verify(connection).close();
    }

    @Test
    void getAccessoryById_Success() throws SQLException {
        int accessoryId = 1;
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);

        mockAccessoryResultSet(resultSet, accessoryId, "VASE", "Glass Vase", 25.0, "Elegant glass vase", "vase.jpg", 20, "Transparent", "Large");

        Accessory accessory = accessoryDAO.getAccessoryById(accessoryId);

        assertNotNull(accessory);
        assertEquals(accessoryId, accessory.getId());
        assertEquals("Glass Vase", accessory.getName());
        verify(preparedStatement).setInt(1, accessoryId);
        verify(preparedStatement).close();
        verify(resultSet).close();
        verify(connection).close();
    }

    @Test
    void getAccessoryById_NotFound() throws SQLException {
        int accessoryId = 999;
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        Accessory accessory = accessoryDAO.getAccessoryById(accessoryId);

        assertNull(accessory);
        verify(preparedStatement).close();
        verify(resultSet).close();
        verify(connection).close();
    }

    @Test
    void getAccessoryById_SQLException() throws SQLException {
        int accessoryId = 1;
        when(connection.prepareStatement(anyString())).thenThrow(new SQLException("Database query error"));

        Accessory accessory = accessoryDAO.getAccessoryById(accessoryId);

        assertNull(accessory);
        verify(connection).close();
    }

    @Test
    void saveAccessory_InsertSuccess() throws SQLException {
        Accessory newAccessory = new Accessory("New Tape", 3.0, "Strong tape", "tape.png", 50, AccessoryType.RIBBON, "Black", "10m");
        when(connection.prepareStatement(anyString(), eq(Statement.RETURN_GENERATED_KEYS))).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(1);

        ResultSet generatedKeys = mock(ResultSet.class);
        when(preparedStatement.getGeneratedKeys()).thenReturn(generatedKeys);
        when(generatedKeys.next()).thenReturn(true);
        when(generatedKeys.getInt(1)).thenReturn(10);

        boolean result = accessoryDAO.saveAccessory(newAccessory);

        assertTrue(result);
        assertEquals(10, newAccessory.getId());
        verify(preparedStatement).setString(1, "RIBBON");
        verify(preparedStatement).setString(2, "New Tape");
        verify(preparedStatement).setDouble(3, 3.0);
        verify(preparedStatement).close();
        verify(generatedKeys).close();
        verify(connection).close();
    }

    @Test
    void saveAccessory_UpdateSuccess() throws SQLException {
        Accessory existingAccessory = new Accessory("Old Vase", 20.0, "Old style vase", "old_vase.jpg", 15, AccessoryType.VASE, "Blue", "Medium");
        existingAccessory.setId(5);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(1);

        boolean result = accessoryDAO.saveAccessory(existingAccessory);

        assertTrue(result);
        verify(preparedStatement).setString(1, "VASE");
        verify(preparedStatement).setString(2, "Old Vase");
        verify(preparedStatement).setInt(9, 5);
        verify(preparedStatement).close();
        verify(connection).close();
    }

    @Test
    void saveAccessory_NullAccessory() {
        boolean result = accessoryDAO.saveAccessory(null);

        assertFalse(result);
        verifyNoInteractions(dbManager);
    }

    @Test
    void saveAccessory_InsertFailure_NoRowsAffected() throws SQLException {
        Accessory newAccessory = new Accessory("Fail Tape", 1.0, "", "", 0, AccessoryType.OTHER, "", "");
        when(connection.prepareStatement(anyString(), eq(Statement.RETURN_GENERATED_KEYS))).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(0);

        boolean result = accessoryDAO.saveAccessory(newAccessory);

        assertFalse(result);
        verify(preparedStatement).close();
        verify(connection).close();
    }

    @Test
    void saveAccessory_InsertFailure_NoGeneratedKeys() throws SQLException {
        Accessory newAccessory = new Accessory("Keyless Tape", 1.0, "", "", 0, AccessoryType.OTHER, "", "");
        when(connection.prepareStatement(anyString(), eq(Statement.RETURN_GENERATED_KEYS))).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(1);
        ResultSet generatedKeys = mock(ResultSet.class);
        when(preparedStatement.getGeneratedKeys()).thenReturn(generatedKeys);
        when(generatedKeys.next()).thenReturn(false);

        boolean result = accessoryDAO.saveAccessory(newAccessory);

        assertFalse(result);
        verify(preparedStatement).close();
        verify(generatedKeys).close();
        verify(connection).close();
    }

    @Test
    void saveAccessory_InsertSQLException() throws SQLException {
        Accessory newAccessory = new Accessory("Error Tape", 1.0, "", "", 0, AccessoryType.OTHER, "", "");
        when(connection.prepareStatement(anyString(), eq(Statement.RETURN_GENERATED_KEYS))).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenThrow(new SQLException("Insert failed"));

        boolean result = accessoryDAO.saveAccessory(newAccessory);

        assertFalse(result);
        verify(preparedStatement).close();
        verify(connection).close();
    }

    @Test
    void saveAccessory_UpdateFailure_NoRowsAffected() throws SQLException {
        Accessory existingAccessory = new Accessory("NoUpdate Vase", 10.0, "", "", 0, AccessoryType.VASE, "", "");
        existingAccessory.setId(7);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(0);

        boolean result = accessoryDAO.saveAccessory(existingAccessory);

        assertFalse(result);
        verify(preparedStatement).close();
        verify(connection).close();
    }

    @Test
    void saveAccessory_UpdateSQLException() throws SQLException {
        Accessory existingAccessory = new Accessory("Error Vase", 10.0, "", "", 0, AccessoryType.VASE, "", "");
        existingAccessory.setId(7);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenThrow(new SQLException("Update failed"));

        boolean result = accessoryDAO.saveAccessory(existingAccessory);

        assertFalse(result);
        verify(preparedStatement).close();
        verify(connection).close();
    }

    @Test
    void deleteAccessory_Success() throws SQLException {
        int accessoryId = 3;
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(1);

        boolean result = accessoryDAO.deleteAccessory(accessoryId);

        assertTrue(result);
        verify(preparedStatement).setInt(1, accessoryId);
        verify(preparedStatement).close();
        verify(connection).close();
    }

    @Test
    void deleteAccessory_Failure_NoRowsAffected() throws SQLException {
        int accessoryId = 3;
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(0);

        boolean result = accessoryDAO.deleteAccessory(accessoryId);

        assertFalse(result);
        verify(preparedStatement).close();
        verify(connection).close();
    }

    @Test
    void deleteAccessory_SQLException() throws SQLException {
        int accessoryId = 3;
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenThrow(new SQLException("Delete failed"));

        boolean result = accessoryDAO.deleteAccessory(accessoryId);

        assertFalse(result);
        verify(preparedStatement).close();
        verify(connection).close();
    }

    @Test
    void updateStock_Success() throws SQLException {
        int accessoryId = 1;
        int quantityChange = -5;
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);

        accessoryDAO.updateStock(accessoryId, quantityChange);

        verify(preparedStatement).setInt(1, quantityChange);
        verify(preparedStatement).setInt(2, accessoryId);
        verify(preparedStatement).executeUpdate();
        verify(preparedStatement).close();
        verify(connection).close();
    }

    @Test
    void updateStock_SQLException() throws SQLException {
        int accessoryId = 1;
        int quantityChange = 10;
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenThrow(new SQLException("Stock update failed"));

        accessoryDAO.updateStock(accessoryId, quantityChange);

        verify(preparedStatement).close();
        verify(connection).close();
    }

    @Test
    void mapRowToAccessory_Success() throws SQLException {
        mockAccessoryResultSet(resultSet, 1, "BASKET", "Wicker Basket", 15.0, "Handmade basket", "basket.png", 30, "Brown", "Medium");

        Accessory accessory = accessoryDAO.mapRowToAccessory(resultSet);

        assertNotNull(accessory);
        assertEquals(1, accessory.getId());
        assertEquals(AccessoryType.BASKET, accessory.getType());
        assertEquals("Wicker Basket", accessory.getName());
        assertEquals(15.0, accessory.getPrice());
    }

    @Test
    void setAccessoryId_Success() {
        Accessory accessory = new Accessory("Test Acc", 1.0, AccessoryType.OTHER);
        int expectedId = 123;

        accessoryDAO.setAccessoryId(accessory, expectedId);

        assertEquals(expectedId, accessory.getId());
    }

    private void mockAccessoryResultSet(ResultSet rs, int id, String typeName, String name, double price,
                                        String description, String imagePath, int stockQuantity,
                                        String color, String size) throws SQLException {
        when(rs.getInt("id")).thenReturn(id);
        when(rs.getString("type_name")).thenReturn(typeName);
        when(rs.getString("name")).thenReturn(name);
        when(rs.getDouble("price")).thenReturn(price);
        when(rs.getString("description")).thenReturn(description);
        when(rs.getString("image_path")).thenReturn(imagePath);
        when(rs.getInt("stock_quantity")).thenReturn(stockQuantity);
        when(rs.getString("color")).thenReturn(color);
        when(rs.getString("size")).thenReturn(size);
        when(rs.getString("display_name")).thenReturn(AccessoryType.valueOf(typeName).getDisplayName());
    }
}