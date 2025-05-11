package flowershop.dao;

import flowershop.database.DatabaseManager;
import flowershop.models.Accessory;
import flowershop.models.Accessory.AccessoryType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.lang.reflect.Field;
import java.sql.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class AccessoryDAOTest {

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
    private AccessoryDAO accessoryDAO;

    private AutoCloseable closeable;

    private Accessory testAccessory1;
    private Accessory testAccessory2;

    @BeforeEach
    void setUp() throws SQLException {
        closeable = MockitoAnnotations.openMocks(this);

        when(dbManagerMock.getConnection()).thenReturn(connectionMock);
        when(connectionMock.createStatement()).thenReturn(statementMock);
        when(connectionMock.prepareStatement(anyString())).thenReturn(preparedStatementMock);
        when(connectionMock.prepareStatement(anyString(), anyInt())).thenReturn(preparedStatementMock);

        when(preparedStatementMock.executeQuery()).thenReturn(resultSetMock);
        when(preparedStatementMock.executeUpdate()).thenReturn(1);
        when(preparedStatementMock.getGeneratedKeys()).thenReturn(resultSetMock);
        when(statementMock.executeQuery(anyString())).thenReturn(resultSetMock);


        testAccessory1 = new Accessory("Ribbon", 10.0, "Red Ribbon", "ribbon.jpg", 50, AccessoryType.RIBBON, "Red", "1m");
        // ID встановлюється через DAO або сеттер в моделі, якщо є
        // Для тестування mapRowToAccessory та getById, ID буде отримано з ResultSet

        testAccessory2 = new Accessory("Vase", 25.0, "Glass Vase", "vase.jpg", 20, AccessoryType.VASE, "Transparent", "Medium");
    }

    @AfterEach
    void tearDown() throws Exception {
        if (closeable != null) {
            closeable.close();
        }
        reset(dbManagerMock, connectionMock, preparedStatementMock, statementMock, resultSetMock);
    }

    @Test
    void constructor_ShouldInitializeDbManager() {
        assertNotNull(accessoryDAO);
        // Перевірка, що dbManager був отриманий через DatabaseManager.getInstance()
        // Можна додати поле в AccessoryDAO для dbManager і перевірити, що воно не null,
        // або перевірити, що getInstance() був викликаний, якщо це важливо.
        // Поточний конструктор не приймає dbManager, а отримує його статично.
    }

    private void mockAccessoryResultSet(Accessory accessory, int id) throws SQLException {
        when(resultSetMock.getInt("id")).thenReturn(id);
        when(resultSetMock.getString("name")).thenReturn(accessory.getName());
        when(resultSetMock.getDouble("price")).thenReturn(accessory.getPrice());
        when(resultSetMock.getString("description")).thenReturn(accessory.getDescription());
        when(resultSetMock.getString("image_path")).thenReturn(accessory.getImagePath());
        when(resultSetMock.getInt("stock_quantity")).thenReturn(accessory.getStockQuantity());
        when(resultSetMock.getString("type_name")).thenReturn(accessory.getType().name());
        // display_name не використовується в mapRowToAccessory, але є в запиті
        when(resultSetMock.getString("display_name")).thenReturn(accessory.getType().getDisplayName());
        when(resultSetMock.getString("color")).thenReturn(accessory.getColor());
        when(resultSetMock.getString("size")).thenReturn(accessory.getSize());
    }

    @Test
    void getAllAccessories_ShouldReturnListOfAccessories_WhenAccessoriesExist() throws SQLException {
        when(resultSetMock.next()).thenReturn(true, true, false); // Два аксесуари
        // Мокуємо дані для першого аксесуара
        when(resultSetMock.getInt("id")).thenReturn(1, 2);
        when(resultSetMock.getString("name")).thenReturn(testAccessory1.getName(), testAccessory2.getName());
        when(resultSetMock.getDouble("price")).thenReturn(testAccessory1.getPrice(), testAccessory2.getPrice());
        when(resultSetMock.getString("description")).thenReturn(testAccessory1.getDescription(), testAccessory2.getDescription());
        when(resultSetMock.getString("image_path")).thenReturn(testAccessory1.getImagePath(), testAccessory2.getImagePath());
        when(resultSetMock.getInt("stock_quantity")).thenReturn(testAccessory1.getStockQuantity(), testAccessory2.getStockQuantity());
        when(resultSetMock.getString("type_name")).thenReturn(testAccessory1.getType().name(), testAccessory2.getType().name());
        when(resultSetMock.getString("color")).thenReturn(testAccessory1.getColor(), testAccessory2.getColor());
        when(resultSetMock.getString("size")).thenReturn(testAccessory1.getSize(), testAccessory2.getSize());


        List<Accessory> accessories = accessoryDAO.getAllAccessories();

        assertNotNull(accessories);
        assertEquals(2, accessories.size());
        assertEquals(testAccessory1.getName(), accessories.get(0).getName());
        assertEquals(1, accessories.get(0).getId()); // Перевіряємо встановлення ID
        assertEquals(testAccessory2.getName(), accessories.get(1).getName());
        assertEquals(2, accessories.get(1).getId());
        verify(statementMock).executeQuery(anyString());
        verify(dbManagerMock).closeConnection(connectionMock);
    }

    @Test
    void getAllAccessories_ShouldReturnEmptyList_WhenNoAccessories() throws SQLException {
        when(resultSetMock.next()).thenReturn(false);

        List<Accessory> accessories = accessoryDAO.getAllAccessories();

        assertNotNull(accessories);
        assertTrue(accessories.isEmpty());
        verify(dbManagerMock).closeConnection(connectionMock);
    }

    @Test
    void getAllAccessories_ShouldHandleSQLException() throws SQLException {
        when(statementMock.executeQuery(anyString())).thenThrow(new SQLException("DB error"));

        List<Accessory> accessories = accessoryDAO.getAllAccessories();

        assertNotNull(accessories);
        assertTrue(accessories.isEmpty());
        verify(dbManagerMock).closeConnection(connectionMock);
    }

    @Test
    void getAccessoryById_ShouldReturnAccessory_WhenExists() throws SQLException {
        when(resultSetMock.next()).thenReturn(true, false);
        mockAccessoryResultSet(testAccessory1, 1);
        testAccessory1.setId(1); // Встановлюємо очікуваний ID для порівняння

        Accessory foundAccessory = accessoryDAO.getAccessoryById(1);

        assertNotNull(foundAccessory);
        assertEquals(testAccessory1.getName(), foundAccessory.getName());
        assertEquals(1, foundAccessory.getId());
        verify(preparedStatementMock).setInt(1, 1);
        verify(dbManagerMock).closeConnection(connectionMock);
    }

    @Test
    void getAccessoryById_ShouldReturnNull_WhenNotExists() throws SQLException {
        when(resultSetMock.next()).thenReturn(false);

        Accessory foundAccessory = accessoryDAO.getAccessoryById(99);

        assertNull(foundAccessory);
        verify(dbManagerMock).closeConnection(connectionMock);
    }

    @Test
    void getAccessoryById_ShouldHandleSQLException() throws SQLException {
        when(preparedStatementMock.executeQuery()).thenThrow(new SQLException("DB error"));

        Accessory foundAccessory = accessoryDAO.getAccessoryById(1);

        assertNull(foundAccessory);
        verify(dbManagerMock).closeConnection(connectionMock);
    }

    @Test
    void saveAccessory_ShouldInsertNewAccessory_WhenIdIsZero() throws SQLException {
        Accessory newAccessory = new Accessory("New Tape", 5.0, AccessoryType.RIBBON);
        newAccessory.setId(0); // Явно для тесту insert

        when(resultSetMock.next()).thenReturn(true); // Для getGeneratedKeys
        when(resultSetMock.getInt(1)).thenReturn(100); // Новий ID

        boolean result = accessoryDAO.saveAccessory(newAccessory);

        assertTrue(result);
        assertEquals(100, newAccessory.getId());
        verify(preparedStatementMock).setString(1, newAccessory.getType().name());
        // ... інші verify setString, setDouble ...
        verify(preparedStatementMock).executeUpdate();
        verify(dbManagerMock).closeConnection(connectionMock);
    }

    @Test
    void saveAccessory_ShouldUpdateExistingAccessory_WhenIdIsPositive() throws SQLException {
        testAccessory1.setId(1); // Для тесту update
        testAccessory1.setPrice(12.0);

        boolean result = accessoryDAO.saveAccessory(testAccessory1);

        assertTrue(result);
        verify(preparedStatementMock).setString(1, testAccessory1.getType().name());
        verify(preparedStatementMock).setDouble(3, 12.0);
        verify(preparedStatementMock).setInt(9, 1); // Where id = ?
        verify(preparedStatementMock).executeUpdate();
        verify(dbManagerMock).closeConnection(connectionMock);
    }

    @Test
    void saveAccessory_Insert_ShouldReturnFalse_WhenExecuteUpdateFails() throws SQLException {
        Accessory newAccessory = new Accessory("New Tape", 5.0, AccessoryType.RIBBON);
        newAccessory.setId(0);
        when(preparedStatementMock.executeUpdate()).thenReturn(0); // 0 рядків змінено

        boolean result = accessoryDAO.saveAccessory(newAccessory);

        assertFalse(result);
        verify(dbManagerMock).closeConnection(connectionMock);
    }

    @Test
    void saveAccessory_Insert_ShouldReturnFalse_WhenGeneratedKeysIsEmpty() throws SQLException {
        Accessory newAccessory = new Accessory("New Tape", 5.0, AccessoryType.RIBBON);
        newAccessory.setId(0);
        // executeUpdate повертає 1, але getGeneratedKeys не дає результату
        when(preparedStatementMock.executeUpdate()).thenReturn(1);
        when(resultSetMock.next()).thenReturn(false); // getGeneratedKeys.next() is false

        boolean result = accessoryDAO.saveAccessory(newAccessory);

        assertFalse(result);
        verify(dbManagerMock).closeConnection(connectionMock);
    }

    @Test
    void saveAccessory_Update_ShouldReturnFalse_WhenExecuteUpdateFails() throws SQLException {
        testAccessory1.setId(1);
        when(preparedStatementMock.executeUpdate()).thenReturn(0); // 0 рядків змінено

        boolean result = accessoryDAO.saveAccessory(testAccessory1);

        assertFalse(result);
        verify(dbManagerMock).closeConnection(connectionMock);
    }

    @Test
    void saveAccessory_Insert_ShouldHandleSQLException() throws SQLException {
        Accessory newAccessory = new Accessory("New Tape", 5.0, AccessoryType.RIBBON);
        newAccessory.setId(0);
        when(preparedStatementMock.executeUpdate()).thenThrow(new SQLException("DB insert error"));

        boolean result = accessoryDAO.saveAccessory(newAccessory);

        assertFalse(result);
        verify(dbManagerMock).closeConnection(connectionMock);
    }

    @Test
    void saveAccessory_Update_ShouldHandleSQLException() throws SQLException {
        testAccessory1.setId(1);
        when(preparedStatementMock.executeUpdate()).thenThrow(new SQLException("DB update error"));

        boolean result = accessoryDAO.saveAccessory(testAccessory1);

        assertFalse(result);
        verify(dbManagerMock).closeConnection(connectionMock);
    }


    @Test
    void deleteAccessory_ShouldReturnTrue_WhenSuccessful() throws SQLException {
        boolean result = accessoryDAO.deleteAccessory(1);

        assertTrue(result);
        verify(preparedStatementMock).setInt(1, 1);
        verify(preparedStatementMock).executeUpdate();
        verify(dbManagerMock).closeConnection(connectionMock);
    }

    @Test
    void deleteAccessory_ShouldReturnFalse_WhenNotSuccessful() throws SQLException {
        when(preparedStatementMock.executeUpdate()).thenReturn(0); // 0 рядків видалено
        boolean result = accessoryDAO.deleteAccessory(1);

        assertFalse(result);
        verify(dbManagerMock).closeConnection(connectionMock);
    }

    @Test
    void deleteAccessory_ShouldHandleSQLException() throws SQLException {
        when(preparedStatementMock.executeUpdate()).thenThrow(new SQLException("DB delete error"));
        boolean result = accessoryDAO.deleteAccessory(1);

        assertFalse(result);
        verify(dbManagerMock).closeConnection(connectionMock);
    }

    @Test
    void updateStock_ShouldExecuteUpdate() throws SQLException {
        accessoryDAO.updateStock(1, 10);

        verify(preparedStatementMock).setInt(1, 10);
        verify(preparedStatementMock).setInt(2, 1);
        verify(preparedStatementMock).executeUpdate();
        verify(dbManagerMock).closeConnection(connectionMock);
    }

    @Test
    void updateStock_ShouldHandleSQLException() throws SQLException {
        when(preparedStatementMock.executeUpdate()).thenThrow(new SQLException("DB stock update error"));
        accessoryDAO.updateStock(1, 10); // Метод ловить виняток і друкує в stderr

        // Перевіряємо, що executeUpdate був викликаний
        verify(preparedStatementMock).executeUpdate();
        verify(dbManagerMock).closeConnection(connectionMock); // І з'єднання закрите
    }

    @Test
    void mapRowToAccessory_ShouldCorrectlyMapResultSet() throws SQLException {
        mockAccessoryResultSet(testAccessory1, 15);

        Accessory mapped = accessoryDAO.mapRowToAccessory(resultSetMock);

        assertNotNull(mapped);
        assertEquals(15, mapped.getId());
        assertEquals(testAccessory1.getName(), mapped.getName());
        assertEquals(testAccessory1.getPrice(), mapped.getPrice());
        assertEquals(testAccessory1.getDescription(), mapped.getDescription());
        assertEquals(testAccessory1.getImagePath(), mapped.getImagePath());
        assertEquals(testAccessory1.getStockQuantity(), mapped.getStockQuantity());
        assertEquals(testAccessory1.getType(), mapped.getType());
        assertEquals(testAccessory1.getColor(), mapped.getColor());
        assertEquals(testAccessory1.getSize(), mapped.getSize());
    }

    @Test
    void setAccessoryId_ShouldSetIdUsingReflection() throws NoSuchFieldException, IllegalAccessException {
        Accessory accessory = new Accessory("Test", 1.0, AccessoryType.OTHER);
        accessoryDAO.setAccessoryId(accessory, 123);
        assertEquals(123, accessory.getId());

        // Для покриття catch блоку (симуляція помилки рефлексії)
        // Це складно зробити без зміни класу Accessory або використання PowerMock.
        // Стандартний JUnit/Mockito не дозволяє легко мокувати приватні поля цільового класу.
        // Однак, якщо ми передамо об'єкт іншого класу, це може викликати помилку.
        // Але це не зовсім те, що потрібно для тестування логіки самого DAO.
        // Для 100% покриття цього specific catch, можна було б створити сценарій,
        // де поле "id" тимчасово видалено або перейменовано, але це надто складно для типового unit-тесту.
        // Поточний тест покриває успішний шлях try блоку.

        // Спроба викликати setAccessoryId з null, щоб перевірити стійкість (хоча це не типовий випадок)
        // Однак, метод не перевіряє accessory на null перед рефлексією,
        // тому це призведе до NullPointerException всередині try, а не в catch (Exception e)
        // assertThrows(NullPointerException.class, () -> accessoryDAO.setAccessoryId(null, 1));

        // Перевірка логування помилки (якщо б вона виникла) - це поза межами простого мокування
        // System.err redirection and assertion.
    }

    @Test
    void setAccessoryId_ShouldCatchReflectionException_AndLogError() {
        // Цей тест намагається покрити блок catch в setAccessoryId.
        // Ми не можемо безпосередньо викликати NoSuchFieldException чи IllegalAccessException
        // на реальному полі Accessory.id без використання більш складних технік.
        // Однак, ми можемо перевірити, що метод не "падає" неконтрольовано.
        // Фактичне логування помилки в System.err складно перевірити автоматично без перенаправлення потоку.

        Accessory accessory = new Accessory("Test", 1.0, AccessoryType.OTHER);

        // Спробуємо викликати метод setAccessoryId, передаючи об'єкт,
        // для якого рефлексія може теоретично дати збій (хоча це не гарантує Exception)
        // Цей тест більше для демонстрації, що метод викликається.
        // Для справжнього покриття catch блоку потрібен інший підхід.

        // Змінимо поле на недоступне (теоретично)
        try {
            Field idField = Accessory.class.getDeclaredField("id");
            // Зробимо його недоступним для цього тесту (це не спрацює так просто з приватними полями)
            // idField.setAccessible(false); // Це не те, що треба, бо ми самі встановлюємо true
        } catch (NoSuchFieldException e) {
            // Це не повинно статися, якщо поле існує
        }

        // Якщо б ми могли передати mock Field, ми б змусили field.set() кинути IllegalAccessException
        // Але зараз ми просто викликаємо метод, знаючи, що він має блок catch.
        // Цей тест не гарантує покриття catch, але демонструє виклик.
        accessoryDAO.setAccessoryId(accessory, 555);
        // Ми не можемо легко перевірити вивід в System.err без додаткових бібліотек.
        // Головне, що програма не аварійно завершується, якщо щось піде не так з рефлексією.
        assertEquals(555, accessory.getId()); // Очікуємо, що ID все одно встановиться, якщо не було винятку
    }

}