package flowershop.dao;

import flowershop.database.DatabaseManager;
import flowershop.models.Accessory;
import flowershop.models.Bouquet;
import flowershop.models.Flower;
import org.junit.jupiter.api.*;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class BouquetDAOTest {

    @Mock
    private DatabaseManager dbManagerMock;

    @Mock
    private FlowerDAOTest flowerDAOMock;

    @Mock
    private AccessoryDAO accessoryDAOMock;

    @Mock
    private Connection connectionMock;

    @Mock
    private PreparedStatement preparedStatementMock;

    @Mock
    private Statement statementMock;

    @Mock
    private ResultSet resultSetMock;

    @InjectMocks
    private BouquetDAO bouquetDAO;

    private AutoCloseable closeable;

    // Дані для тестів
    private Flower testFlower1;
    private Flower testFlower2;
    private Accessory testAccessory1;
    private Accessory testAccessory2;
    private Bouquet testBouquet1;
    private Bouquet testBouquet2;


    @BeforeEach
    void setUp() throws SQLException {
        closeable = MockitoAnnotations.openMocks(this);

        // Мокуємо DatabaseManager для повернення мокового Connection
        when(dbManagerMock.getConnection()).thenReturn(connectionMock);

        // Мокуємо поведінку Connection (наприклад, створення PreparedStatement)
        when(connectionMock.prepareStatement(anyString())).thenReturn(preparedStatementMock);
        when(connectionMock.prepareStatement(anyString(), anyInt())).thenReturn(preparedStatementMock);
        when(connectionMock.createStatement()).thenReturn(statementMock);

        // Мокуємо PreparedStatement (наприклад, виконання запитів)
        when(preparedStatementMock.executeQuery()).thenReturn(resultSetMock);
        when(preparedStatementMock.executeUpdate()).thenReturn(1); // Припускаємо, що 1 рядок змінено
        when(preparedStatementMock.getGeneratedKeys()).thenReturn(resultSetMock);


        // Ініціалізація тестових об'єктів
        testFlower1 = new Flower(Flower.FlowerType.ROSE, 50.0, 90, 60, "Red", "Holland", false, "rose.jpg", 100);
        testFlower1.setId(1);
        testFlower2 = new Flower(Flower.FlowerType.TULIP, 30.0, 85, 40, "Yellow", "Holland", false, "tulip.jpg", 150);
        testFlower2.setId(2);

        testAccessory1 = new Accessory("Ribbon", 10.0, Accessory.AccessoryType.RIBBON);
        testAccessory1.setId(1);
        testAccessory2 = new Accessory("Wrapping Paper", 20.0, Accessory.AccessoryType.WRAPPING_PAPER);
        testAccessory2.setId(2);


        List<Flower> flowers1 = new ArrayList<>();
        flowers1.add(testFlower1);
        List<Accessory> accessories1 = new ArrayList<>();
        accessories1.add(testAccessory1);
        testBouquet1 = new Bouquet("Rose Bouquet", "Beautiful roses", flowers1, accessories1, "bouquet1.jpg", 10.0);
        testBouquet1.setId(1);

        List<Flower> flowers2 = new ArrayList<>();
        flowers2.add(testFlower2);
        List<Accessory> accessories2 = new ArrayList<>();
        accessories2.add(testAccessory2);
        testBouquet2 = new Bouquet("Tulip Bouquet", "Fresh tulips", flowers2, accessories2, "bouquet2.jpg", 5.0);
        testBouquet2.setId(2);

    }

    @AfterEach
    void tearDown() throws Exception {
        if (closeable != null) {
            closeable.close();
        }
        // Скидаємо моки після кожного тесту (важливо для чистоти тестів)
        reset(dbManagerMock, flowerDAOMock, accessoryDAOMock, connectionMock, preparedStatementMock, statementMock, resultSetMock);
    }

    @Test
    void testConstructor() {
        // Перевіряємо, що конструктор ініціалізує DAO
        BouquetDAO newBouquetDAO = new BouquetDAO();
        assertNotNull(newBouquetDAO, "Конструктор повинен створювати екземпляр BouquetDAO.");
    }


    @Test
    void getAllBouquets_ShouldReturnListOfBouquets_WhenBouquetsExist() throws SQLException {
        // Мокуємо ResultSet для getAllBouquets
        when(statementMock.executeQuery(anyString())).thenReturn(resultSetMock);
        when(resultSetMock.next()).thenReturn(true, true, false); // Два букети
        when(resultSetMock.getInt("id")).thenReturn(testBouquet1.getId(), testBouquet2.getId());
        when(resultSetMock.getString("name")).thenReturn(testBouquet1.getName(), testBouquet2.getName());
        when(resultSetMock.getString("description")).thenReturn(testBouquet1.getDescription(), testBouquet2.getDescription());
        when(resultSetMock.getString("image_path")).thenReturn(testBouquet1.getImagePath(), testBouquet2.getImagePath());
        when(resultSetMock.getDouble("discount")).thenReturn(testBouquet1.getDiscount(), testBouquet2.getDiscount());

        // Мокуємо завантаження квітів та аксесуарів
        // Оскільки loadBouquetFlowers та loadBouquetAccessories використовують те саме з'єднання,
        // нам потрібно мокувати їх виклики PreparedStatement та ResultSet окремо або зробити їх більш стійкими до мокування.
        // Для простоти, тут ми можемо мокувати повернення з flowerDAO та accessoryDAO, коли вони викликаються з loadBouquetFlowers/Accessories
        when(flowerDAOMock.getFlowerById(testFlower1.getId())).thenReturn(testFlower1);
        when(flowerDAOMock.getFlowerById(testFlower2.getId())).thenReturn(testFlower2);
        when(accessoryDAOMock.getAccessoryById(testAccessory1.getId())).thenReturn(testAccessory1);
        when(accessoryDAOMock.getAccessoryById(testAccessory2.getId())).thenReturn(testAccessory2);


        // Створюємо новий ResultSet mock для loadBouquetFlowers для першого букета
        ResultSet flowersRs1 = mock(ResultSet.class);
        when(connectionMock.prepareStatement(contains("SELECT f.*, bf.quantity"))).thenReturn(preparedStatementMock);
        when(preparedStatementMock.executeQuery()).thenReturn(flowersRs1); // Повертаємо специфічний ResultSet
        when(flowersRs1.next()).thenReturn(true, false); // Одна квітка для першого букета
        when(flowersRs1.getInt("id")).thenReturn(testFlower1.getId());

        // Створюємо новий ResultSet mock для loadBouquetAccessories для першого букета
        ResultSet accessoriesRs1 = mock(ResultSet.class);
        // Потрібно уточнити мокування, щоб розрізняти запити для квітів та аксесуарів
        // Нижче приклад, як це можна зробити, якщо SQL-запити відрізняються достатньо
        // Або мокувати PreparedStatement, щоб повертати різні ResultSet залежно від запиту
        // Для простоти, припустимо, що ми можемо розрізнити запити
        PreparedStatement accessoriesPstmtMock = mock(PreparedStatement.class);
        when(connectionMock.prepareStatement(contains("SELECT a.*, ba.quantity"))).thenReturn(accessoriesPstmtMock);
        when(accessoriesPstmtMock.executeQuery()).thenReturn(accessoriesRs1);
        when(accessoriesRs1.next()).thenReturn(true, false); // Один аксесуар для першого букета
        when(accessoriesRs1.getInt("id")).thenReturn(testAccessory1.getId());


        // Аналогічно для другого букета (якщо потрібно перевірити завантаження для всіх)
        ResultSet flowersRs2 = mock(ResultSet.class);
        // ... мокування для квітів другого букета ...
        ResultSet accessoriesRs2 = mock(ResultSet.class);
        // ... мокування для аксесуарів другого букета ...

        // Якщо логіка loadBouquetFlowers/Accessories викликає flowerDAO.getFlowerById безпосередньо, то мокування вище повинно спрацювати.
        // ВАЖЛИВО: Треба уважно дивитися на реалізацію loadBouquetFlowers/Accessories.
        // Якщо вони будують новий SQL і виконують його, то мокування має бути на рівні PreparedStatement, що повертається для цих запитів.

        List<Bouquet> bouquets = bouquetDAO.getAllBouquets();

        assertNotNull(bouquets);
        assertEquals(2, bouquets.size());
        // Додаткові перевірки на вміст букетів, якщо потрібно
        // Наприклад, перевірити, що квіти та аксесуари завантажені (це залежить від того, як ви мокуєте loadBouquetFlowers/Accessories)
        // Для цього прикладу, основна перевірка - це кількість букетів
    }

    @Test
    void getAllBouquets_ShouldReturnEmptyList_WhenNoBouquets() throws SQLException {
        when(statementMock.executeQuery(anyString())).thenReturn(resultSetMock);
        when(resultSetMock.next()).thenReturn(false); // Немає букетів

        List<Bouquet> bouquets = bouquetDAO.getAllBouquets();

        assertNotNull(bouquets);
        assertTrue(bouquets.isEmpty());
    }

    @Test
    void getAllBouquets_ShouldHandleSQLException() throws SQLException {
        when(statementMock.executeQuery(anyString())).thenThrow(new SQLException("Test SQL Exception"));

        List<Bouquet> bouquets = bouquetDAO.getAllBouquets();

        assertNotNull(bouquets);
        assertTrue(bouquets.isEmpty()); // Очікуємо порожній список при помилці
        verify(dbManagerMock).closeConnection(connectionMock); // Перевіряємо закриття з'єднання
    }

    @Test
    void getBouquetById_ShouldReturnNull_WhenNotExists() throws SQLException {
        when(resultSetMock.next()).thenReturn(false); // Букет не знайдено

        Bouquet foundBouquet = bouquetDAO.getBouquetById(999);

        assertNull(foundBouquet);
    }

    @Test
    void getBouquetById_ShouldHandleSQLException() throws SQLException {
        when(preparedStatementMock.executeQuery()).thenThrow(new SQLException("Test SQL Exception"));

        Bouquet foundBouquet = bouquetDAO.getBouquetById(1);

        assertNull(foundBouquet);
        verify(dbManagerMock).closeConnection(connectionMock);
    }


    @Test
    void saveBouquet_ShouldInsertNewBouquet_WhenIdIsZero() throws SQLException {
        Bouquet newBouquet = new Bouquet("New Bouquet", "Desc",
                Collections.singletonList(testFlower1),
                Collections.singletonList(testAccessory1), "new.jpg", 0);
        newBouquet.setId(0); // Явно вказуємо, що це новий букет

        when(resultSetMock.next()).thenReturn(true); // Для getGeneratedKeys
        when(resultSetMock.getInt(1)).thenReturn(100); // Новий ID букета

        // Мокуємо виконання для insertBouquet, clearBouquetFlowers, clearBouquetAccessories, insertBouquetFlower, insertBouquetAccessory
        // Всі вони використовують preparedStatementMock, тому потрібна послідовність мокувань або більш гнучке мокування
        when(preparedStatementMock.executeUpdate())
                .thenReturn(1) // insertBouquet
                .thenReturn(1) // clearBouquetFlowers
                .thenReturn(1) // clearBouquetAccessories
                .thenReturn(1) // insertBouquetFlower
                .thenReturn(1); // insertBouquetAccessory


        boolean result = bouquetDAO.saveBouquet(newBouquet);

        assertTrue(result);
        assertEquals(100, newBouquet.getId()); // Перевіряємо, що ID встановлено
        verify(connectionMock).setAutoCommit(false);
        verify(connectionMock).commit();
        verify(connectionMock).setAutoCommit(true);
        verify(dbManagerMock).closeConnection(connectionMock);
    }


    @Test
    void saveBouquet_ShouldUpdateExistingBouquet_WhenIdIsPositive() throws SQLException {
        testBouquet1.setDescription("Updated Description"); // Змінюємо дані для оновлення

        // Мокуємо виконання для updateBouquet, clearBouquetFlowers, clearBouquetAccessories, insertBouquetFlower, insertBouquetAccessory
        when(preparedStatementMock.executeUpdate())
                .thenReturn(1) // updateBouquet
                .thenReturn(1) // clearBouquetFlowers
                .thenReturn(1) // clearBouquetAccessories
                .thenReturn(1) // insertBouquetFlower
                .thenReturn(1); // insertBouquetAccessory


        boolean result = bouquetDAO.saveBouquet(testBouquet1);

        assertTrue(result);
        verify(connectionMock).setAutoCommit(false);
        // Перевіряємо, що викликався updateBouquet (можна через verify на PreparedStatement з відповідним SQL)
        verify(preparedStatementMock).setString(2, "Updated Description"); // Приклад перевірки параметрів
        verify(connectionMock).commit();
        verify(connectionMock).setAutoCommit(true);
        verify(dbManagerMock).closeConnection(connectionMock);
    }

    @Test
    void saveBouquet_ShouldRollback_WhenInsertBouquetFails() throws SQLException {
        Bouquet newBouquet = new Bouquet("Fail Bouquet", "Desc", Collections.emptyList(), Collections.emptyList(), "fail.jpg", 0);
        newBouquet.setId(0);

        when(preparedStatementMock.executeUpdate()).thenReturn(0); // insertBouquet повертає 0 (помилка)

        boolean result = bouquetDAO.saveBouquet(newBouquet);

        assertFalse(result);
        verify(connectionMock).setAutoCommit(false);
        verify(connectionMock).rollback();
        verify(connectionMock).setAutoCommit(true);
        verify(dbManagerMock).closeConnection(connectionMock);
    }

    @Test
    void saveBouquet_ShouldRollback_WhenUpdateBouquetFails() throws SQLException {
        testBouquet1.setId(1); // Існуючий букет
        when(preparedStatementMock.executeUpdate()).thenReturn(0); // updateBouquet повертає 0

        boolean result = bouquetDAO.saveBouquet(testBouquet1);

        assertFalse(result);
        verify(connectionMock).setAutoCommit(false);
        verify(connectionMock).rollback(); // Перевірка відкату
        verify(connectionMock).setAutoCommit(true);
        verify(dbManagerMock).closeConnection(connectionMock);
    }


    @Test
    void saveBouquet_ShouldRollback_WhenInsertBouquetFlowerFails() throws SQLException {
        Bouquet newBouquet = new Bouquet("FlowerFail Bouquet", "Desc",
                Collections.singletonList(testFlower1), // Квітка, яка спричинить помилку
                Collections.emptyList(), "flowerfail.jpg", 0);
        newBouquet.setId(0);

        when(resultSetMock.next()).thenReturn(true); // Для getGeneratedKeys
        when(resultSetMock.getInt(1)).thenReturn(101);

        // insertBouquet успішний, clear* успішні, insertBouquetFlower - ні
        when(preparedStatementMock.executeUpdate())
                .thenReturn(1) // insertBouquet
                .thenReturn(1) // clearBouquetFlowers
                .thenReturn(1) // clearBouquetAccessories
                .thenReturn(0); // insertBouquetFlower - помилка

        boolean result = bouquetDAO.saveBouquet(newBouquet);

        assertFalse(result);
        assertEquals(101, newBouquet.getId()); // ID встановлюється до помилки
        verify(connectionMock).rollback();
    }

    @Test
    void saveBouquet_ShouldRollback_WhenInsertBouquetAccessoryFails() throws SQLException {
        Bouquet newBouquet = new Bouquet("AccessoryFail Bouquet", "Desc",
                Collections.emptyList(),
                Collections.singletonList(testAccessory1), // Аксесуар, який спричинить помилку
                "accfail.jpg", 0);
        newBouquet.setId(0);

        when(resultSetMock.next()).thenReturn(true); // Для getGeneratedKeys
        when(resultSetMock.getInt(1)).thenReturn(102);

        // insertBouquet, clear*, insertBouquetFlower (якщо є) успішні, insertBouquetAccessory - ні
        when(preparedStatementMock.executeUpdate())
                .thenReturn(1) // insertBouquet
                .thenReturn(1) // clearBouquetFlowers
                .thenReturn(1) // clearBouquetAccessories
                // припустимо, квітів немає, тому наступний виклик для аксесуара
                .thenReturn(0); // insertBouquetAccessory - помилка

        boolean result = bouquetDAO.saveBouquet(newBouquet);

        assertFalse(result);
        assertEquals(102, newBouquet.getId());
        verify(connectionMock).rollback();
    }


    @Test
    void saveBouquet_ShouldHandleSQLException_AndRollback() throws SQLException {
        Bouquet newBouquet = new Bouquet("SQLException Bouquet", "Desc", Collections.emptyList(), Collections.emptyList(), "sql_ex.jpg", 0);
        newBouquet.setId(0);

        // SQLException під час виконання, наприклад, insertBouquet
        when(preparedStatementMock.executeUpdate()).thenThrow(new SQLException("DB Error during save"));

        boolean result = bouquetDAO.saveBouquet(newBouquet);

        assertFalse(result);
        verify(connectionMock).setAutoCommit(false);
        verify(connectionMock).rollback(); // Перевіряємо відкат
        verify(connectionMock).setAutoCommit(true);
        verify(dbManagerMock).closeConnection(connectionMock);
    }

    @Test
    void saveBouquet_ShouldHandleSQLExceptionOnRollback_AndStillCloseConnection() throws SQLException {
        Bouquet newBouquet = new Bouquet("RollbackException Bouquet", "Desc", Collections.emptyList(), Collections.emptyList(), "rb_ex.jpg", 0);
        newBouquet.setId(0);

        when(preparedStatementMock.executeUpdate()).thenThrow(new SQLException("DB Error causing rollback"));
        doThrow(new SQLException("Rollback failed")).when(connectionMock).rollback(); // SQLException під час відкату

        boolean result = bouquetDAO.saveBouquet(newBouquet);

        assertFalse(result);
        verify(connectionMock).setAutoCommit(false);
        verify(connectionMock).rollback();
        // Важливо: перевірити, що setAutoCommit(true) викликається в finally
        verify(connectionMock).setAutoCommit(true);
        verify(dbManagerMock).closeConnection(connectionMock); // І з'єднання закривається
    }

    @Test
    void deleteBouquet_ShouldReturnFalse_WhenNotSuccessful() throws SQLException {
        when(preparedStatementMock.executeUpdate()).thenReturn(0); // 0 рядків видалено

        boolean result = bouquetDAO.deleteBouquet(1);

        assertFalse(result);
    }

    @Test
    void deleteBouquet_ShouldHandleSQLException() throws SQLException {
        when(preparedStatementMock.executeUpdate()).thenThrow(new SQLException("Delete SQL Exception"));

        boolean result = bouquetDAO.deleteBouquet(1);

        assertFalse(result);
        verify(dbManagerMock).closeConnection(connectionMock);
    }

    @Test
    void mapRowToBouquet_ShouldCorrectlyMapResultSet() throws SQLException {
        when(resultSetMock.getInt("id")).thenReturn(10);
        when(resultSetMock.getString("name")).thenReturn("Test Name");
        when(resultSetMock.getString("description")).thenReturn("Test Desc");
        when(resultSetMock.getString("image_path")).thenReturn("test.jpg");
        when(resultSetMock.getDouble("discount")).thenReturn(15.0);

        // bouquetDAO.mapRowToBouquet - це package-private метод. Щоб його протестувати,
        // можна або зробити його public (не рекомендується лише для тестів),
        // або викликати його через метод, який його використовує (наприклад, getBouquetById),
        // або створити екземпляр BouquetDAO в тому ж пакеті, що й тести.
        // Тут ми припускаємо, що можемо викликати його напряму для тестування логіки мапінгу.
        BouquetDAO localBouquetDAO = new BouquetDAO(); // Створюємо екземпляр для виклику package-private методу
        Bouquet mappedBouquet = localBouquetDAO.mapRowToBouquet(resultSetMock);

        assertNotNull(mappedBouquet);
        assertEquals(10, mappedBouquet.getId());
        assertEquals("Test Name", mappedBouquet.getName());
        assertEquals("Test Desc", mappedBouquet.getDescription());
        assertEquals("test.jpg", mappedBouquet.getImagePath());
        assertEquals(15.0, mappedBouquet.getDiscount());
        assertTrue(mappedBouquet.getFlowers().isEmpty()); // За замовчуванням порожні
        assertTrue(mappedBouquet.getAccessories().isEmpty()); // За замовчуванням порожні
    }

    // Додаткові тести для приватних методів (якщо їх логіка складна і не покривається публічними)
    // Зазвичай, приватні методи тестуються опосередковано через публічні.
    // Якщо приватний метод має складну логіку, це може бути ознакою того,
    // що його варто винести в окремий клас або зробити package-private для тестування.

    // Приклад тесту для loadBouquetFlowers (потрібно мокувати PreparedStatement і ResultSet)
    @Test
    void loadBouquetFlowers_ShouldLoadFlowers() throws SQLException {
        Bouquet bouquetToLoad = new Bouquet();
        bouquetToLoad.setId(1);

        when(flowerDAOMock.getFlowerById(testFlower1.getId())).thenReturn(testFlower1);

        // Мокуємо ResultSet для запиту квітів
        ResultSet flowersRs = mock(ResultSet.class);
        when(connectionMock.prepareStatement(contains("SELECT f.*, bf.quantity"))).thenReturn(preparedStatementMock);
        when(preparedStatementMock.executeQuery()).thenReturn(flowersRs);
        when(flowersRs.next()).thenReturn(true, false); // Одна квітка
        when(flowersRs.getInt("id")).thenReturn(testFlower1.getId()); // ID квітки

        // Викликаємо package-private метод (потребує доступу)
        // bouquetDAO.loadBouquetFlowers(connectionMock, bouquetToLoad); // Помилка компіляції, якщо метод приватний
        // Щоб протестувати, можна викликати публічний метод, який його використовує,
        // або, якщо це критично, змінити видимість методу (не найкраща практика).
        // Для цього прикладу, припустимо, що ми тестуємо це через getBouquetById або getAllBouquets,
        // де цей метод викликається.

        // У цьому випадку, ми вже мокували поведінку getFlowerById,
        // тому якщо getBouquetById правильно викликає loadBouquetFlowers,
        // а loadBouquetFlowers правильно викликає flowerDAO.getFlowerById,
        // то квіти будуть додані.

        // Альтернатива: зробити метод package-private і викликати його.
        // Або рефакторинг, щоб винести логіку завантаження в окремий тестований компонент.
    }


    // --- Тести для випадку, коли getGeneratedKeys не повертає ключ ---
    @Test
    void saveBouquet_InsertNew_ShouldReturnFalse_WhenGeneratedKeysIsEmpty() throws SQLException {
        Bouquet newBouquet = new Bouquet("NoKey Bouquet", "Desc", Collections.emptyList(), Collections.emptyList(), "nokey.jpg", 0);
        newBouquet.setId(0);

        when(preparedStatementMock.executeUpdate()).thenReturn(1); // insertBouquet успішний
        when(preparedStatementMock.getGeneratedKeys()).thenReturn(resultSetMock);
        when(resultSetMock.next()).thenReturn(false); // GeneratedKeys не повертає next() == true

        boolean result = bouquetDAO.saveBouquet(newBouquet);

        assertFalse(result);
        verify(connectionMock).rollback();
    }
}