package flowershop.database;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.io.IOException;
import java.io.InputStream;

/**
 * Клас для керування підключеннями до бази даних MySQL з використанням паттерну Singleton.
 * Забезпечує безпечне підключення до бази даних, завантаження конфігурації з файлу
 * та належне закриття з'єднань.
 */
public class DatabaseManager {

    private static final Logger logger = LogManager.getLogger(DatabaseManager.class);
    private static final String CONFIG_FILE = "/database.properties";
    private static DatabaseManager instance;
    private final Properties dbConfig;

    /**
     * Приватний конструктор для реалізації Singleton.
     * Завантажує конфігурацію бази даних із файлу properties.
     */
    private DatabaseManager() {
        dbConfig = new Properties();
        try (InputStream input = DatabaseManager.class.getResourceAsStream(CONFIG_FILE)) {
            if (input == null) {
                logger.fatal("Файл конфігурації {} не знайдено.", CONFIG_FILE);
                throw new IllegalStateException("Файл конфігурації " + CONFIG_FILE + " не знайдено.");
            }
            dbConfig.load(input);
            logger.info("Конфігурацію бази даних успішно завантажено з {}.", CONFIG_FILE);
        } catch (IOException e) {
            logger.fatal("Помилка завантаження конфігурації бази даних: {}", e.getMessage(), e);
            throw new IllegalStateException("Помилка завантаження конфігурації бази даних", e);
        }
    }

    /**
     * Отримує єдиний екземпляр класу DatabaseManager (Singleton).
     *
     * @return єдиний екземпляр DatabaseManager
     */
    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    /**
     * Встановлює та повертає підключення до бази даних.
     *
     * @return об'єкт Connection для роботи з базою даних
     * @throws SQLException якщо виникає помилка підключення до бази даних
     */
    public Connection getConnection() throws SQLException {
        String url = dbConfig.getProperty("db.url");
        String username = dbConfig.getProperty("db.username");
        String password = dbConfig.getProperty("db.password");

        if (url == null || username == null || password == null) {
            logger.fatal("Відсутні необхідні параметри конфігурації бази даних.");
            throw new IllegalStateException("Відсутні необхідні параметри конфігурації бази даних.");
        }

        try {
            Connection connection = DriverManager.getConnection(url, username, password);
            logger.debug("Підключення до бази даних успішно встановлено: {}", url);
            return connection;
        } catch (SQLException e) {
            logger.fatal("Не вдалося підключитися до бази даних: URL='{}', User='{}': {}", url, username, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Закриває підключення до бази даних.
     *
     * @param connection підключення, яке необхідно закрити
     */
    public void closeConnection(Connection connection) {
        if (connection != null) {
            try {
                if (!connection.isClosed()) {
                    connection.close();
                    logger.debug("Підключення до бази даних успішно закрито.");
                }
            } catch (SQLException e) {
                logger.error("Помилка при закритті з'єднання: {}", e.getMessage(), e);
            }
        }
    }
}