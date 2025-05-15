package flowershop.dao;

import flowershop.database.DatabaseManager;
import flowershop.models.Bouquet;
import flowershop.models.Flower;
import flowershop.models.Accessory;
import org.junit.jupiter.api.*;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class BouquetDAOTest {

    private BouquetDAO bouquetDAO;
    private static MockedStatic<DatabaseManager> mockedDatabaseManager;
    private DatabaseManager dbManager;
    private Connection connection;
    private PreparedStatement preparedStatement;
    private Statement statement;
    private ResultSet resultSet;
    private FlowerDAO flowerDAO;
    private AccessoryDAO accessoryDAO;

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
        flowerDAO = mock(FlowerDAO.class);
        accessoryDAO = mock(AccessoryDAO.class);

        mockedDatabaseManager.when(DatabaseManager::getInstance).thenReturn(dbManager);
        when(dbManager.getConnection()).thenReturn(connection);

        bouquetDAO = new BouquetDAO();
        try {
            java.lang.reflect.Field fieldFlower = BouquetDAO.class.getDeclaredField("flowerDAO");
            fieldFlower.setAccessible(true);
            fieldFlower.set(bouquetDAO, flowerDAO);

            java.lang.reflect.Field fieldAccessory = BouquetDAO.class.getDeclaredField("accessoryDAO");
            fieldAccessory.setAccessible(true);
            fieldAccessory.set(bouquetDAO, accessoryDAO);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    @AfterEach
    void tearDown() {
        reset(dbManager, connection, preparedStatement, statement, resultSet, flowerDAO, accessoryDAO);
    }

    @Nested
    @DisplayName("Тести для getAllBouquets")
    class GetAllBouquetsTests {

        @Test
        @DisplayName("Отримання порожнього списку, якщо букетів немає")
        void getAllBouquets_Empty() throws SQLException {
            when(connection.createStatement()).thenReturn(statement);
            when(statement.executeQuery(anyString())).thenReturn(resultSet);
            when(resultSet.next()).thenReturn(false);

            List<Bouquet> bouquets = bouquetDAO.getAllBouquets();

            assertNotNull(bouquets);
            assertTrue(bouquets.isEmpty());
        }

        @Test
        @DisplayName("SQLException під час connection.createStatement")
        void getAllBouquets_createStatementThrowsSQLException() throws SQLException {
            when(dbManager.getConnection()).thenReturn(connection);
            when(connection.createStatement()).thenThrow(new SQLException("DB Connection error"));
            List<Bouquet> bouquets = bouquetDAO.getAllBouquets();
            assertTrue(bouquets.isEmpty());
        }

        @Test
        @DisplayName("SQLException під час statement.executeQuery")
        void getAllBouquets_executeQueryThrowsSQLException() throws SQLException {
            when(connection.createStatement()).thenReturn(statement);
            when(statement.executeQuery(anyString())).thenThrow(new SQLException("Query execution error"));
            List<Bouquet> bouquets = bouquetDAO.getAllBouquets();
            assertTrue(bouquets.isEmpty());
        }
        @Test
        @DisplayName("SQLException під час rs.next() в getAllBouquets")
        void getAllBouquets_ResultSetNextThrowsSQLException() throws SQLException {
            when(connection.createStatement()).thenReturn(statement);
            when(statement.executeQuery(anyString())).thenReturn(resultSet);
            when(resultSet.next()).thenThrow(new SQLException("ResultSet error"));

            List<Bouquet> bouquets = bouquetDAO.getAllBouquets();
            assertNotNull(bouquets);
            assertTrue(bouquets.isEmpty());
        }
    }

    @Nested
    @DisplayName("Тести для getBouquetById")
    class GetBouquetByIdTests {
        @Test
        @DisplayName("Успішне отримання букета за ID")
        void getBouquetById_Success() throws SQLException {
            int bouquetId = 1;
            mockBouquetResultSet(resultSet, bouquetId, "Весняний", "Опис1", "path1.jpg", 10);
            when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
            when(preparedStatement.executeQuery()).thenReturn(resultSet);
            when(resultSet.next()).thenReturn(true);

            ResultSet emptyRs = mock(ResultSet.class);
            when(emptyRs.next()).thenReturn(false);
            PreparedStatement loadPs = mock(PreparedStatement.class);
            when(connection.prepareStatement(contains("bouquet_flowers"))).thenReturn(loadPs);
            when(connection.prepareStatement(contains("bouquet_accessories"))).thenReturn(loadPs);
            when(loadPs.executeQuery()).thenReturn(emptyRs);

            Bouquet bouquet = bouquetDAO.getBouquetById(bouquetId);

            assertNotNull(bouquet);
            assertEquals(bouquetId, bouquet.getId());
            assertEquals("Весняний", bouquet.getName());
            verify(preparedStatement).setInt(1, bouquetId);
        }

        @Test
        @DisplayName("Букет не знайдено за ID")
        void getBouquetById_NotFound() throws SQLException {
            when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
            when(preparedStatement.executeQuery()).thenReturn(resultSet);
            when(resultSet.next()).thenReturn(false);

            Bouquet bouquet = bouquetDAO.getBouquetById(999);
            assertNull(bouquet);
        }

        @Test
        @DisplayName("SQLException під час connection.prepareStatement")
        void getBouquetById_prepareStatementThrowsSQLException() throws SQLException {
            when(connection.prepareStatement(anyString())).thenThrow(new SQLException("Prepare statement error"));
            Bouquet bouquet = bouquetDAO.getBouquetById(1);
            assertNull(bouquet);
        }

        @Test
        @DisplayName("SQLException під час preparedStatement.executeQuery")
        void getBouquetById_executeQueryThrowsSQLException() throws SQLException {
            when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
            when(preparedStatement.executeQuery()).thenThrow(new SQLException("Query execution error"));
            Bouquet bouquet = bouquetDAO.getBouquetById(1);
            assertNull(bouquet);
        }

        @Test
        @DisplayName("SQLException під час rs.next() в getBouquetById")
        void getBouquetById_ResultSetNextThrowsSQLException() throws SQLException {
            when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
            when(preparedStatement.executeQuery()).thenReturn(resultSet);
            when(resultSet.next()).thenThrow(new SQLException("ResultSet error"));

            Bouquet bouquet = bouquetDAO.getBouquetById(1);
            assertNull(bouquet);
        }
    }

    @Nested
    @DisplayName("Тести для saveBouquet")
    class SaveBouquetTests {
        private Bouquet testBouquet;
        private Flower testFlower;
        private Accessory testAccessory;

        @BeforeEach
        void setUpSave() throws SQLException {
            testFlower = new Flower(Flower.FlowerType.ROSE, 10.5, 90, 30);
            testFlower.setId(1);

            testAccessory = new Accessory("Ribbon", 5.0, Accessory.AccessoryType.RIBBON);
            testAccessory.setId(1);

            testBouquet = new Bouquet("Весняний букет", "Гарні весняні квіти",
                    new ArrayList<>(Collections.singletonList(testFlower)),
                    new ArrayList<>(Collections.singletonList(testAccessory)),
                    "path1.jpg", 10.0);
            when(connection.prepareStatement(anyString(), eq(Statement.RETURN_GENERATED_KEYS))).thenReturn(preparedStatement);
            when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        }

        @Test
        @DisplayName("Успішне вставлення нового букета")
        void saveBouquet_InsertSuccess() throws SQLException {
            testBouquet.setId(0);
            ResultSet generatedKeys = mock(ResultSet.class);
            when(preparedStatement.getGeneratedKeys()).thenReturn(generatedKeys);
            when(generatedKeys.next()).thenReturn(true);
            when(generatedKeys.getInt(1)).thenReturn(123);
            when(preparedStatement.executeUpdate()).thenReturn(1);
            boolean result = bouquetDAO.saveBouquet(testBouquet);
            assertTrue(result);
            assertEquals(123, testBouquet.getId());
            verify(connection).commit();
            verify(connection, never()).rollback();
        }

        @Test
        @DisplayName("Успішне оновлення існуючого букета")
        void saveBouquet_UpdateSuccess() throws SQLException {
            testBouquet.setId(1);
            when(preparedStatement.executeUpdate()).thenReturn(1);
            boolean result = bouquetDAO.saveBouquet(testBouquet);
            assertTrue(result);
            assertEquals(1, testBouquet.getId());
            verify(connection).commit();
            verify(connection, never()).rollback();
        }

        @Test
        @DisplayName("Збереження null букета повертає false")
        void saveBouquet_NullBouquet() {
            assertFalse(bouquetDAO.saveBouquet(null));
        }

        @Test
        @DisplayName("Помилка вставлення букета (insertBouquet повертає <= 0)")
        void saveBouquet_InsertReturnsNoRowsAffected() throws SQLException {
            testBouquet.setId(0);
            when(preparedStatement.executeUpdate()).thenReturn(0);
            boolean result = bouquetDAO.saveBouquet(testBouquet);
            assertFalse(result);
            verify(connection).rollback();
        }

        @Test
        @DisplayName("Помилка вставлення букета (generatedKeys.next() is false)")
        void saveBouquet_InsertGeneratedKeysFalse() throws SQLException {
            testBouquet.setId(0);
            when(preparedStatement.executeUpdate()).thenReturn(1);
            ResultSet generatedKeys = mock(ResultSet.class);
            when(preparedStatement.getGeneratedKeys()).thenReturn(generatedKeys);
            when(generatedKeys.next()).thenReturn(false);
            boolean result = bouquetDAO.saveBouquet(testBouquet);
            assertFalse(result);
            verify(connection).rollback();
        }

        @Test
        @DisplayName("Помилка оновлення букета (updateBouquet повертає <= 0)")
        void saveBouquet_UpdateReturnsNoRowsAffected() throws SQLException {
            testBouquet.setId(1);
            when(preparedStatement.executeUpdate()).thenReturn(0);
            boolean result = bouquetDAO.saveBouquet(testBouquet);
            assertFalse(result);
            verify(connection).rollback();
        }

        @Test
        @DisplayName("Помилка при вставленні квітки (insertBouquetFlower повертає false)")
        void saveBouquet_InsertFlowerFails() throws SQLException {
            testBouquet.setId(0);
            ResultSet generatedKeys = mock(ResultSet.class);
            when(preparedStatement.getGeneratedKeys()).thenReturn(generatedKeys);
            when(generatedKeys.next()).thenReturn(true);
            when(generatedKeys.getInt(1)).thenReturn(123);
            when(preparedStatement.executeUpdate())
                    .thenReturn(1)
                    .thenReturn(1)
                    .thenReturn(1)
                    .thenReturn(0);
            boolean result = bouquetDAO.saveBouquet(testBouquet);
            assertFalse(result);
            verify(connection).rollback();
        }

        @Test
        @DisplayName("Помилка при вставленні аксесуара (insertBouquetAccessory повертає false)")
        void saveBouquet_InsertAccessoryFails() throws SQLException {
            testBouquet.setId(0);
            ResultSet generatedKeys = mock(ResultSet.class);
            when(preparedStatement.getGeneratedKeys()).thenReturn(generatedKeys);
            when(generatedKeys.next()).thenReturn(true);
            when(generatedKeys.getInt(1)).thenReturn(123);
            when(preparedStatement.executeUpdate())
                    .thenReturn(1)
                    .thenReturn(1)
                    .thenReturn(1)
                    .thenReturn(1)
                    .thenReturn(0);
            boolean result = bouquetDAO.saveBouquet(testBouquet);
            assertFalse(result);
            verify(connection).rollback();
        }

        @Test
        @DisplayName("SQLException під час getConnection в saveBouquet")
        void saveBouquet_GetConnectionThrowsSQLException() throws SQLException {
            when(dbManager.getConnection()).thenThrow(new SQLException("Connection failed"));
            boolean result = bouquetDAO.saveBouquet(testBouquet);
            assertFalse(result);
            verify(connection, never()).commit();
            verify(connection, never()).rollback();
        }

        @Test
        @DisplayName("SQLException (загальна) під час збереження, викликає rollback")
        void saveBouquet_GenericSQLExceptionCausesRollback() throws SQLException {
            testBouquet.setId(0);
            ResultSet generatedKeys = mock(ResultSet.class);
            when(preparedStatement.getGeneratedKeys()).thenReturn(generatedKeys);
            when(generatedKeys.next()).thenReturn(true);
            when(generatedKeys.getInt(1)).thenReturn(123);
            when(preparedStatement.executeUpdate())
                    .thenReturn(1)
                    .thenThrow(new SQLException("Error during clear/insert items"));
            boolean result = bouquetDAO.saveBouquet(testBouquet);
            assertFalse(result);
            verify(connection).rollback();
            verify(connection, never()).commit();
        }
    }

    @Nested
    @DisplayName("Тести для deleteBouquet")
    class DeleteBouquetTests {
        @Test
        @DisplayName("Успішне видалення букета")
        void deleteBouquet_Success() throws SQLException {
            when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
            when(preparedStatement.executeUpdate()).thenReturn(1);
            boolean result = bouquetDAO.deleteBouquet(1);
            assertTrue(result);
            verify(preparedStatement, times(3)).executeUpdate();
            verify(connection).commit();
        }

        @Test
        @DisplayName("Помилка видалення (букет не знайдено)")
        void deleteBouquet_FailureNotFound() throws SQLException {
            when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
            when(preparedStatement.executeUpdate()).thenReturn(1).thenReturn(1).thenReturn(0);
            boolean result = bouquetDAO.deleteBouquet(1);
            assertFalse(result);
            verify(connection).rollback();
        }

        @Test
        @DisplayName("SQLException під час видалення, викликає rollback")
        void deleteBouquet_SQLExceptionCausesRollback() throws SQLException {
            when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
            when(preparedStatement.executeUpdate()).thenThrow(new SQLException("Error clearing flowers"));
            boolean result = bouquetDAO.deleteBouquet(1);
            assertFalse(result);
            verify(connection).rollback();
            verify(connection, never()).commit();
        }

        @Test
        @DisplayName("SQLException під час getConnection в deleteBouquet")
        void deleteBouquet_GetConnectionThrowsSQLException() throws SQLException {
            when(dbManager.getConnection()).thenThrow(new SQLException("Connection failed"));
            boolean result = bouquetDAO.deleteBouquet(1);
            assertFalse(result);
        }
    }

    @Nested
    @DisplayName("Тести для loadBouquetFlowers")
    class LoadBouquetFlowersTests {
        private Bouquet testBouquet;

        @BeforeEach
        void setUpLoad() {
            testBouquet = new Bouquet();
            testBouquet.setId(1);
        }

        @Test
        @DisplayName("Успішне завантаження квітів")
        void loadBouquetFlowers_Success() throws SQLException {
            ResultSet flowerRs = mock(ResultSet.class);
            Flower roseTemplate = new Flower(Flower.FlowerType.ROSE, 50, 80, 30);
            roseTemplate.setId(101);
            Flower tulipTemplate = new Flower(Flower.FlowerType.TULIP, 30, 90, 25);
            tulipTemplate.setId(102);
            when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
            when(preparedStatement.executeQuery()).thenReturn(flowerRs);
            when(flowerRs.next()).thenReturn(true).thenReturn(true).thenReturn(false);
            when(flowerRs.getInt("flower_id")).thenReturn(101).thenReturn(102);
            when(flowerRs.getInt("quantity")).thenReturn(3).thenReturn(2);
            when(flowerDAO.getFlowerById(101)).thenReturn(roseTemplate);
            when(flowerDAO.getFlowerById(102)).thenReturn(tulipTemplate);
            bouquetDAO.loadBouquetFlowers(connection, testBouquet);
            assertEquals(5, testBouquet.getFlowers().size());
            assertEquals(3, testBouquet.getFlowers().stream().filter(f -> f.getId() == 101).count());
            assertEquals(2, testBouquet.getFlowers().stream().filter(f -> f.getId() == 102).count());
            verify(preparedStatement).setInt(1, testBouquet.getId());
        }

        @Test
        @DisplayName("Завантаження квітів, якщо flowerDAO.getFlowerById повертає null")
        void loadBouquetFlowers_FlowerNotFoundInDAO() throws SQLException {
            ResultSet flowerRs = mock(ResultSet.class);
            when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
            when(preparedStatement.executeQuery()).thenReturn(flowerRs);
            when(flowerRs.next()).thenReturn(true).thenReturn(false);
            when(flowerRs.getInt("flower_id")).thenReturn(999);
            when(flowerRs.getInt("quantity")).thenReturn(1);
            when(flowerDAO.getFlowerById(999)).thenReturn(null);
            bouquetDAO.loadBouquetFlowers(connection, testBouquet);
            assertTrue(testBouquet.getFlowers().isEmpty());
        }

        @Test
        @DisplayName("loadBouquetFlowers з null букетом (вже є в тестах, але для повноти)")
        void loadBouquetFlowers_NullBouquet() throws SQLException {
            assertDoesNotThrow(() -> bouquetDAO.loadBouquetFlowers(connection, null));
        }

        @Test
        @DisplayName("SQLException під час prepareStatement в loadBouquetFlowers")
        void loadBouquetFlowers_PrepareStatementThrowsSQLException() throws SQLException {
            when(connection.prepareStatement(anyString())).thenThrow(new SQLException("PS error"));
            assertThrows(SQLException.class, () -> bouquetDAO.loadBouquetFlowers(connection, testBouquet));
        }

        @Test
        @DisplayName("SQLException під час executeQuery в loadBouquetFlowers")
        void loadBouquetFlowers_ExecuteQueryThrowsSQLException() throws SQLException {
            when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
            when(preparedStatement.executeQuery()).thenThrow(new SQLException("EQ error"));
            assertThrows(SQLException.class, () -> bouquetDAO.loadBouquetFlowers(connection, testBouquet));
        }
    }

    @Nested
    @DisplayName("Тести для loadBouquetAccessories")
    class LoadBouquetAccessoriesTests {
        private Bouquet testBouquet;

        @BeforeEach
        void setUpLoad() {
            testBouquet = new Bouquet();
            testBouquet.setId(1);
        }

        @Test
        @DisplayName("Завантаження аксесуарів, якщо accessoryDAO.getAccessoryById повертає null")
        void loadBouquetAccessories_AccessoryNotFoundInDAO() throws SQLException {
            ResultSet accRs = mock(ResultSet.class);
            when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
            when(preparedStatement.executeQuery()).thenReturn(accRs);
            when(accRs.next()).thenReturn(true).thenReturn(false);
            when(accRs.getInt("accessory_id")).thenReturn(888);
            when(accessoryDAO.getAccessoryById(888)).thenReturn(null);
            bouquetDAO.loadBouquetAccessories(connection, testBouquet);
            assertTrue(testBouquet.getAccessories().isEmpty());
        }

        @Test
        @DisplayName("loadBouquetAccessories з null букетом (вже є в тестах, але для повноти)")
        void loadBouquetAccessories_NullBouquet() throws SQLException {
            assertDoesNotThrow(() -> bouquetDAO.loadBouquetAccessories(connection, null));
        }

        @Test
        @DisplayName("SQLException під час prepareStatement в loadBouquetAccessories")
        void loadBouquetAccessories_PrepareStatementThrowsSQLException() throws SQLException {
            when(connection.prepareStatement(anyString())).thenThrow(new SQLException("PS error"));
            assertThrows(SQLException.class, () -> bouquetDAO.loadBouquetAccessories(connection, testBouquet));
        }

        @Test
        @DisplayName("SQLException під час executeQuery в loadBouquetAccessories")
        void loadBouquetAccessories_ExecuteQueryThrowsSQLException() throws SQLException {
            when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
            when(preparedStatement.executeQuery()).thenThrow(new SQLException("EQ error"));
            assertThrows(SQLException.class, () -> bouquetDAO.loadBouquetAccessories(connection, testBouquet));
        }
    }

    @Test
    @DisplayName("Тест mapRowToBouquet (вже є, але для підтвердження)")
    void mapRowToBouquet_Success() throws SQLException {
        int id = 1;
        mockBouquetResultSet(resultSet, id, "Spring Bouquet", "Beautiful spring flowers", "path1.jpg", 10.0);
        Bouquet bouquet = bouquetDAO.mapRowToBouquet(resultSet);
        assertNotNull(bouquet);
        assertEquals(id, bouquet.getId());
        assertEquals("Spring Bouquet", bouquet.getName());
        assertEquals("Beautiful spring flowers", bouquet.getDescription());
        assertEquals("path1.jpg", bouquet.getImagePath());
        assertEquals(10.0, bouquet.getDiscount());
    }

    private void mockBouquetResultSet(ResultSet rs, int id, String name, String description,
                                      String imagePath, double discount) throws SQLException {
        mockBouquetResultSet(rs, id, name, description, imagePath, discount, -1);
    }

    private void mockBouquetResultSet(ResultSet rs, int id, String name, String description,
                                      String imagePath, double discount, int callOrder) throws SQLException {
        if (callOrder == -1) {
            when(rs.getInt("id")).thenReturn(id);
            when(rs.getString("name")).thenReturn(name);
            when(rs.getString("description")).thenReturn(description);
            when(rs.getString("image_path")).thenReturn(imagePath);
            when(rs.getDouble("discount")).thenReturn(discount);
        } else {
            when(rs.getInt("id")).thenReturn(id);
            when(rs.getString("name")).thenReturn(name);
            when(rs.getString("description")).thenReturn(description);
            when(rs.getString("image_path")).thenReturn(imagePath);
            when(rs.getDouble("discount")).thenReturn(discount);
        }
    }
}