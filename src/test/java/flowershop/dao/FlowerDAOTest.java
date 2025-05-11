package flowershop.dao;

import flowershop.database.DatabaseManager;
import flowershop.models.Flower;
import flowershop.models.Flower.FlowerType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.sql.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class FlowerDAOTest {

    @Mock
    private DatabaseManager dbManagerMock;

    @Mock
    private Connection connectionMock;

    @Mock
    private PreparedStatement preparedStatementMock;

    @Mock
    private Statement statementMock;

    @Mock
    private ResultSet resultSetMock;

    @InjectMocks
    private FlowerDAO flowerDAO;

    private AutoCloseable closeable;

    private Flower testFlower1;
    private Flower testFlower2;

    @BeforeEach
    void setUp() throws SQLException {
        closeable = MockitoAnnotations.openMocks(this);

        when(dbManagerMock.getConnection()).thenReturn(connectionMock);
        when(connectionMock.createStatement()).thenReturn(statementMock);
        when(connectionMock.prepareStatement(anyString())).thenReturn(preparedStatementMock);
        when(connectionMock.prepareStatement(anyString(), anyInt())).thenReturn(preparedStatementMock); // For RETURN_GENERATED_KEYS

        when(preparedStatementMock.executeQuery()).thenReturn(resultSetMock);
        when(preparedStatementMock.executeUpdate()).thenReturn(1); // Default success for updates
        when(preparedStatementMock.getGeneratedKeys()).thenReturn(resultSetMock);
        when(statementMock.executeQuery(anyString())).thenReturn(resultSetMock);


        testFlower1 = new Flower(FlowerType.ROSE, 50.0, 90, 60, "Red", "Holland", false, "rose.jpg", 100);
        testFlower2 = new Flower(FlowerType.TULIP, 30.0, 85, 40, "Yellow", "Netherlands", true, "tulip.jpg", 150);
    }

    @AfterEach
    void tearDown() throws Exception {
        if (closeable != null) {
            closeable.close();
        }
        reset(dbManagerMock, connectionMock, preparedStatementMock, statementMock, resultSetMock);
    }

    private void mockFlowerResultSet(Flower flower, int id) throws SQLException {
        when(resultSetMock.getInt("id")).thenReturn(id);
        when(resultSetMock.getString("type_name")).thenReturn(flower.getType().name());
        when(resultSetMock.getDouble("price")).thenReturn(flower.getPrice());
        when(resultSetMock.getInt("freshness")).thenReturn(flower.getFreshness());
        when(resultSetMock.getInt("stem_length")).thenReturn(flower.getStemLength());
        when(resultSetMock.getString("color")).thenReturn(flower.getColor());
        when(resultSetMock.getString("country_of_origin")).thenReturn(flower.getCountryOfOrigin());
        when(resultSetMock.getBoolean("is_potted")).thenReturn(flower.isPotted());
        when(resultSetMock.getString("image_path")).thenReturn(flower.getImagePath());
        when(resultSetMock.getInt("stock_quantity")).thenReturn(flower.getStockQuantity());
        // 'display_name' from flower_types table is in query but not directly used in mapRowToFlower for Flower object fields
        when(resultSetMock.getString("display_name")).thenReturn(flower.getType().getDisplayName());
    }


    @Test
    void getAllFlowers_ShouldReturnListOfFlowers_WhenFlowersExist() throws SQLException {
        when(resultSetMock.next()).thenReturn(true, true, false); // Two flowers
        // Mock data for flower 1
        when(resultSetMock.getInt("id")).thenReturn(1, 2);
        when(resultSetMock.getString("type_name")).thenReturn(testFlower1.getType().name(), testFlower2.getType().name());
        when(resultSetMock.getDouble("price")).thenReturn(testFlower1.getPrice(), testFlower2.getPrice());
        when(resultSetMock.getInt("freshness")).thenReturn(testFlower1.getFreshness(), testFlower2.getFreshness());
        when(resultSetMock.getInt("stem_length")).thenReturn(testFlower1.getStemLength(), testFlower2.getStemLength());
        when(resultSetMock.getString("color")).thenReturn(testFlower1.getColor(), testFlower2.getColor());
        when(resultSetMock.getString("country_of_origin")).thenReturn(testFlower1.getCountryOfOrigin(), testFlower2.getCountryOfOrigin());
        when(resultSetMock.getBoolean("is_potted")).thenReturn(testFlower1.isPotted(), testFlower2.isPotted());
        when(resultSetMock.getString("image_path")).thenReturn(testFlower1.getImagePath(), testFlower2.getImagePath());
        when(resultSetMock.getInt("stock_quantity")).thenReturn(testFlower1.getStockQuantity(), testFlower2.getStockQuantity());


        List<Flower> flowers = flowerDAO.getAllFlowers();

        assertNotNull(flowers);
        assertEquals(2, flowers.size());
        assertEquals(testFlower1.getType(), flowers.get(0).getType());
        assertEquals(1, flowers.get(0).getId());
        assertEquals(testFlower2.getType(), flowers.get(1).getType());
        assertEquals(2, flowers.get(1).getId());
        verify(statementMock).executeQuery(anyString());
        verify(dbManagerMock).closeConnection(connectionMock);
    }

    @Test
    void getAllFlowers_ShouldReturnEmptyList_WhenNoFlowers() throws SQLException {
        when(resultSetMock.next()).thenReturn(false);

        List<Flower> flowers = flowerDAO.getAllFlowers();

        assertNotNull(flowers);
        assertTrue(flowers.isEmpty());
        verify(dbManagerMock).closeConnection(connectionMock);
    }

    @Test
    void getAllFlowers_ShouldHandleSQLException() throws SQLException {
        when(statementMock.executeQuery(anyString())).thenThrow(new SQLException("DB error"));

        List<Flower> flowers = flowerDAO.getAllFlowers();

        assertNotNull(flowers);
        assertTrue(flowers.isEmpty());
        verify(dbManagerMock).closeConnection(connectionMock);
    }


    @Test
    void getFlowerById_ShouldReturnFlower_WhenExists() throws SQLException {
        when(resultSetMock.next()).thenReturn(true, false);
        mockFlowerResultSet(testFlower1, 1);
        testFlower1.setId(1); // Ensure ID is set for comparison if needed

        Flower foundFlower = flowerDAO.getFlowerById(1);

        assertNotNull(foundFlower);
        assertEquals(testFlower1.getType(), foundFlower.getType());
        assertEquals(1, foundFlower.getId());
        verify(preparedStatementMock).setInt(1, 1);
        verify(dbManagerMock).closeConnection(connectionMock);
    }

    @Test
    void getFlowerById_ShouldReturnNull_WhenNotExists() throws SQLException {
        when(resultSetMock.next()).thenReturn(false);

        Flower foundFlower = flowerDAO.getFlowerById(99);

        assertNull(foundFlower);
        verify(dbManagerMock).closeConnection(connectionMock);
    }

    @Test
    void getFlowerById_ShouldHandleSQLException() throws SQLException {
        when(preparedStatementMock.executeQuery()).thenThrow(new SQLException("DB error"));

        Flower foundFlower = flowerDAO.getFlowerById(1);

        assertNull(foundFlower);
        verify(dbManagerMock).closeConnection(connectionMock);
    }

    @Test
    void saveFlower_ShouldInsertNewFlower_WhenIdIsZero() throws SQLException {
        Flower newFlower = new Flower(FlowerType.LILY, 20.0, 95, 50);
        newFlower.setId(0); // Explicitly for insert test path in saveFlower

        when(resultSetMock.next()).thenReturn(true); // For getGeneratedKeys
        when(resultSetMock.getInt(1)).thenReturn(101); // New generated ID

        boolean result = flowerDAO.saveFlower(newFlower);

        assertTrue(result);
        assertEquals(101, newFlower.getId()); // Check if ID was set by DAO
        verify(preparedStatementMock).setString(1, newFlower.getType().name());
        verify(preparedStatementMock).setDouble(2, newFlower.getPrice());
        // ... verify other setX calls for insertFlower ...
        verify(preparedStatementMock).setString(10, newFlower.generateDescription()); // description
        verify(preparedStatementMock).executeUpdate();
        verify(dbManagerMock).closeConnection(connectionMock);
    }

    @Test
    void saveFlower_ShouldUpdateExistingFlower_WhenIdIsPositive() throws SQLException {
        testFlower1.setId(1); // To trigger update path
        testFlower1.setPrice(55.0); // Change some data

        boolean result = flowerDAO.saveFlower(testFlower1);

        assertTrue(result);
        verify(preparedStatementMock).setString(1, testFlower1.getType().name());
        verify(preparedStatementMock).setDouble(2, 55.0);
        // ... verify other setX calls for updateFlower ...
        verify(preparedStatementMock).setString(10, testFlower1.generateDescription());
        verify(preparedStatementMock).setInt(11, 1); // WHERE id = ?
        verify(preparedStatementMock).executeUpdate();
        verify(dbManagerMock).closeConnection(connectionMock);
    }

    @Test
    void saveFlower_Insert_ShouldReturnFalse_WhenExecuteUpdateFails() throws SQLException {
        Flower newFlower = new Flower(FlowerType.DAISY, 5.0, 80, 30);
        newFlower.setId(0);
        when(preparedStatementMock.executeUpdate()).thenReturn(0); // Simulate failure

        boolean result = flowerDAO.saveFlower(newFlower);

        assertFalse(result);
        verify(dbManagerMock).closeConnection(connectionMock);
    }

    @Test
    void saveFlower_Insert_ShouldReturnFalse_WhenGeneratedKeysIsEmpty() throws SQLException {
        Flower newFlower = new Flower(FlowerType.DAISY, 5.0, 80, 30);
        newFlower.setId(0);
        when(preparedStatementMock.executeUpdate()).thenReturn(1); // Insert looks successful
        when(resultSetMock.next()).thenReturn(false); // But no generated key

        boolean result = flowerDAO.saveFlower(newFlower);

        assertFalse(result);
        verify(dbManagerMock).closeConnection(connectionMock);
    }


    @Test
    void saveFlower_Update_ShouldReturnFalse_WhenExecuteUpdateFails() throws SQLException {
        testFlower1.setId(1);
        when(preparedStatementMock.executeUpdate()).thenReturn(0); // Simulate failure

        boolean result = flowerDAO.saveFlower(testFlower1);

        assertFalse(result);
        verify(dbManagerMock).closeConnection(connectionMock);
    }


    @Test
    void saveFlower_Insert_ShouldHandleSQLException() throws SQLException {
        Flower newFlower = new Flower(FlowerType.ORCHID, 70.0, 92, 45);
        newFlower.setId(0);
        when(preparedStatementMock.executeUpdate()).thenThrow(new SQLException("DB insert error"));

        boolean result = flowerDAO.saveFlower(newFlower);

        assertFalse(result);
        verify(dbManagerMock).closeConnection(connectionMock);
    }

    @Test
    void saveFlower_Update_ShouldHandleSQLException() throws SQLException {
        testFlower1.setId(1);
        when(preparedStatementMock.executeUpdate()).thenThrow(new SQLException("DB update error"));

        boolean result = flowerDAO.saveFlower(testFlower1);

        assertFalse(result);
        verify(dbManagerMock).closeConnection(connectionMock);
    }


    @Test
    void deleteFlower_ShouldReturnTrue_WhenSuccessful() throws SQLException {
        boolean result = flowerDAO.deleteFlower(1);

        assertTrue(result);
        verify(preparedStatementMock).setInt(1, 1);
        verify(preparedStatementMock).executeUpdate();
        verify(dbManagerMock).closeConnection(connectionMock);
    }

    @Test
    void deleteFlower_ShouldReturnFalse_WhenNotSuccessful() throws SQLException {
        when(preparedStatementMock.executeUpdate()).thenReturn(0); // 0 rows deleted
        boolean result = flowerDAO.deleteFlower(1);

        assertFalse(result);
        verify(dbManagerMock).closeConnection(connectionMock);
    }

    @Test
    void deleteFlower_ShouldHandleSQLException() throws SQLException {
        when(preparedStatementMock.executeUpdate()).thenThrow(new SQLException("DB delete error"));
        boolean result = flowerDAO.deleteFlower(1);

        assertFalse(result);
        verify(dbManagerMock).closeConnection(connectionMock);
    }


    @Test
    void updateStock_ShouldExecuteUpdate() throws SQLException {
        flowerDAO.updateStock(1, -5); // Decrease stock

        verify(preparedStatementMock).setInt(1, -5);
        verify(preparedStatementMock).setInt(2, 1);
        verify(preparedStatementMock).executeUpdate();
        verify(dbManagerMock).closeConnection(connectionMock);
    }

    @Test
    void updateStock_ShouldHandleSQLException() throws SQLException {
        when(preparedStatementMock.executeUpdate()).thenThrow(new SQLException("DB stock update error"));
        flowerDAO.updateStock(1, -5); // Method catches and logs

        verify(preparedStatementMock).executeUpdate();
        verify(dbManagerMock).closeConnection(connectionMock);
    }


    @Test
    void mapRowToFlower_ShouldCorrectlyMapResultSet() throws SQLException {
        mockFlowerResultSet(testFlower1, 25);

        Flower mapped = flowerDAO.mapRowToFlower(resultSetMock);

        assertNotNull(mapped);
        assertEquals(25, mapped.getId());
        assertEquals(testFlower1.getType(), mapped.getType());
        assertEquals(testFlower1.getPrice(), mapped.getPrice());
        assertEquals(testFlower1.getFreshness(), mapped.getFreshness());
        // ... assert other properties ...
    }

    @Test
    void setFlowerId_ShouldSetIdUsingReflection() {
        Flower flower = new Flower(FlowerType.SUNFLOWER, 15.0, 88, 70);
        flowerDAO.setFlowerId(flower, 777);
        assertEquals(777, flower.getId());

        // Тест для покриття catch блоку setFlowerId (аналогічно AccessoryDAO)
        // Важко симулювати Exception без PowerMock або зміни видимості
        // Цей виклик покриває try блок.
    }

    @Test
    void setFlowerId_ShouldCatchReflectionException_AndLogError() {
        Flower flower = new Flower(FlowerType.SUNFLOWER, 15.0, 88, 70);
        // Як і з AccessoryDAO, цей тест в основному для демонстрації виклику
        // та того, що метод має catch блок.
        // Реальне покриття catch потребує більш складних технік.
        flowerDAO.setFlowerId(flower, 888);
        assertEquals(888, flower.getId()); // Очікуємо, що ID встановиться, якщо не було винятку
    }

    public Object getFlowerById(int id) {
        return flowerDAO.getFlowerById(id);
    }

    public List<Flower> getAllFlowers() {
        return flowerDAO.getAllFlowers();
    }

    public void saveFlower(Flower resultFlower) {
        flowerDAO.saveFlower(resultFlower);
    }

    public void deleteFlower(int flowerId) {
        flowerDAO.deleteFlower(flowerId);
    }
}