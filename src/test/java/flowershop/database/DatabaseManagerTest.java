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
        try {
            java.lang.reflect.Field instanceField = DatabaseManager.class.getDeclaredField("instance");
            instanceField.setAccessible(true);
            instanceField.set(null, null);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
            fail("Failed to reset DatabaseManager instance: " + e.getMessage());
        }
        dbManager = DatabaseManager.getInstance();
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void getInstance_shouldReturnSameInstance() {
        DatabaseManager instance1 = DatabaseManager.getInstance();
        DatabaseManager instance2 = DatabaseManager.getInstance();
        assertSame(instance1, instance2, "getInstance() should return the same instance");
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

        assertNotNull(instances[0], "Instance 1 should not be null");
        assertNotNull(instances[1], "Instance 2 should not be null");
        assertSame(instances[0], instances[1], "getInstance() should return the same instance in a multithreaded environment");
    }

    @Test
    void getConnection_shouldReturnConnection_whenDriverManagerSucceeds() {
        Connection mockConnection = mock(Connection.class);
        try (MockedStatic<DriverManager> mockedDriverManager = Mockito.mockStatic(DriverManager.class)) {
            mockedDriverManager.when(() -> DriverManager.getConnection(anyString(), anyString(), anyString()))
                    .thenReturn(mockConnection);

            Connection actualConnection = null;
            try {
                actualConnection = dbManager.getConnection();
            } catch (SQLException e) {
                fail("getConnection should not throw SQLException: " + e.getMessage());
            }

            assertNotNull(actualConnection, "Returned connection should not be null");
            assertSame(mockConnection, actualConnection, "Should return the mocked connection");

            mockedDriverManager.verify(() -> DriverManager.getConnection(anyString(), anyString(), anyString()), times(1));
        }
    }

    @Test
    void getConnection_shouldThrowSQLException_whenDriverManagerFails() {
        try (MockedStatic<DriverManager> mockedDriverManager = Mockito.mockStatic(DriverManager.class)) {
            SQLException expectedException = new SQLException("Test DB connection failed");
            mockedDriverManager.when(() -> DriverManager.getConnection(anyString(), anyString(), anyString()))
                    .thenThrow(expectedException);

            SQLException actualException = assertThrows(SQLException.class, () -> {
                dbManager.getConnection();
            }, "Should throw SQLException if DriverManager fails to establish a connection");

            assertSame(expectedException, actualException, "Should throw the expected SQLException");
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
        dbManager.closeConnection(null);
        assertTrue(true, "closeConnection(null) should not throw an exception");
    }
}