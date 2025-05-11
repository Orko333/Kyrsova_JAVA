package flowershop.database;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.never;

class DatabaseManagerTest {

    private DatabaseManager dbManager;

    @BeforeEach
    void setUp() {
        // Скидаємо instance перед кожним тестом, щоб getInstance() створював новий
        // Це робиться через рефлексію, оскільки поле приватне і статичне
        try {
            java.lang.reflect.Field instanceField = DatabaseManager.class.getDeclaredField("instance");
            instanceField.setAccessible(true);
            instanceField.set(null, null);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            // Цей блок не повинен виконуватися в нормальних умовах тестування
            // Якщо він виконується, це означає проблему зі структурою класу DatabaseManager або тесту
            e.printStackTrace();
            fail("Не вдалося скинути екземпляр DatabaseManager через рефлексію: " + e.getMessage());
        }
        dbManager = DatabaseManager.getInstance();
    }

    @AfterEach
    void tearDown() {
        // Додаткове очищення, якщо потрібно
    }


    @Test
    void getInstance_shouldReturnSameInstance() {
        DatabaseManager instance1 = DatabaseManager.getInstance();
        DatabaseManager instance2 = DatabaseManager.getInstance();
        assertSame(instance1, instance2, "getInstance() повинен повертати той самий екземпляр");
    }

    @Test
    void getInstance_isThreadSafe() throws InterruptedException {
        final DatabaseManager[] instances = new DatabaseManager[2];
        Thread t1 = new Thread(() -> instances[0] = DatabaseManager.getInstance());
        Thread t2 = new Thread(() -> instances[1] = DatabaseManager.getInstance());

        t1.start();
        t2.start();

        t1.join();
        t2.join();

        assertNotNull(instances[0], "Екземпляр 1 не повинен бути null");
        assertNotNull(instances[1], "Екземпляр 2 не повинен бути null");
        assertSame(instances[0], instances[1], "getInstance() повинен повертати той самий екземпляр у багатопотоковому середовищі");
    }

    // Приватний конструктор не може бути протестований безпосередньо,
    // але його існування та приватність перевіряються компілятором
    // і тим фактом, що ми не можемо створити екземпляр через new DatabaseManager() ззовні.
    // Тест getInstance() опосередковано підтверджує його роботу.

    @Test
    void getConnection_shouldReturnConnection_whenDriverManagerSucceeds() {
        // Цей тест потребує мокування статичного методу DriverManager.getConnection(),
        // що стандартно неможливо з Mockito без PowerMock або JUnit 5 Jupiter Mockito Extension.
        // Нижче наведено приклад, як це могло б виглядати з Mockito 3.4.0+ (мокування статичних методів).

        Connection mockConnection = mock(Connection.class);
        // Починаючи з Mockito 3.4.0, можна мокувати статичні методи
        try (MockedStatic<DriverManager> mockedDriverManager = Mockito.mockStatic(DriverManager.class)) {
            mockedDriverManager.when(() -> DriverManager.getConnection(anyString(), anyString(), anyString()))
                    .thenReturn(mockConnection);

            Connection actualConnection = null;
            try {
                actualConnection = dbManager.getConnection();
            } catch (SQLException e) {
                fail("getConnection не повинен кидати SQLException, якщо DriverManager мокований успішно: " + e.getMessage());
            }

            assertNotNull(actualConnection, "Повернене з'єднання не повинно бути null");
            assertSame(mockConnection, actualConnection, "Повинно бути повернене мокове з'єднання");

            // Перевірка, що DriverManager.getConnection був викликаний з правильними (або будь-якими) параметрами
            // Параметри URL, USERNAME, PASSWORD є приватними константами в DatabaseManager,
            // тому їх точне значення не так легко отримати для перевірки без рефлексії.
            // Ми можемо перевірити, що метод був викликаний.
            mockedDriverManager.verify(() -> DriverManager.getConnection(anyString(), anyString(), anyString()), times(1));

        }
    }

    @Test
    void getConnection_shouldThrowSQLException_whenDriverManagerFails() {
        // Тест для випадку, коли DriverManager.getConnection() кидає SQLException
        try (MockedStatic<DriverManager> mockedDriverManager = Mockito.mockStatic(DriverManager.class)) {
            SQLException expectedException = new SQLException("Test DB connection failed");
            mockedDriverManager.when(() -> DriverManager.getConnection(anyString(), anyString(), anyString()))
                    .thenThrow(expectedException);

            SQLException actualException = assertThrows(SQLException.class, () -> {
                dbManager.getConnection();
            }, "Повинен бути кинутий SQLException, якщо DriverManager не може встановити з'єднання");

            assertSame(expectedException, actualException, "Повинен бути кинутий саме той SQLException, який ми очікували");
        }
    }


    @Test
    void closeConnection_shouldCloseConnection_whenConnectionIsNotNull() throws SQLException {
        Connection mockConnection = mock(Connection.class);
        dbManager.closeConnection(mockConnection);
        verify(mockConnection, times(1)).close();
    }

    @Test
    void closeConnection_shouldDoNothing_whenConnectionIsNull() throws SQLException {
        // Ми не можемо безпосередньо мокувати null, але ми можемо передати null
        // і перевірити, що ніякі методи не викликаються на ньому (що призвело б до NPE)
        // і що метод просто завершується.
        // Mockito.verifyNoInteractions() тут не підходить, бо ми не мокуємо null.
        // Головне, що не виникає NullPointerException.
        dbManager.closeConnection(null);
        // Немає чого перевіряти, крім того, що не було помилки.
        // Можна було б додати mock Connection і перевірити, що .close() НЕ викликався,
        // але це буде тестом для випадку mockConnection != null.
        // Для connection == null, ми просто перевіряємо, що код не падає.
        assertTrue(true, "closeConnection(null) не повинен кидати виняток");
    }
}