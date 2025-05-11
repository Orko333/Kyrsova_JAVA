package flowershop.database;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Клас DatabaseManager реалізує паттерн Singleton для керування підключеннями до бази даних.
 * Він забезпечує створення, отримання та закриття з'єднань з базою даних MySQL.
 */
public class DatabaseManager {
    // Ініціалізація логера для цього класу
    private static final Logger logger = LogManager.getLogger(DatabaseManager.class);

    // URL для підключення до бази даних MySQL
    private static final String URL = "jdbc:mysql://localhost:3306/flower_shop?useSSL=false&serverTimezone=UTC";
    // Ім'я користувача для підключення до бази даних
    private static final String USERNAME = "root";
    // Пароль для підключення до бази даних (замінити на дійсний пароль або краще використовувати безпечніші методи зберігання)
    private static final String PASSWORD = "Karatist2006"; // УВАГА: Зберігання паролів у коді є небезпечним!

    // Єдиний екземпляр класу (Singleton)
    private static DatabaseManager instance;

    /**
     * Приватний конструктор для запобігання створенню екземплярів класу ззовні.
     * Використовується для реалізації паттерну Singleton.
     */
    private DatabaseManager() {
        // Пустий конструктор
        logger.debug("Ініціалізація екземпляра DatabaseManager (Singleton).");
    }

    /**
     * Метод для отримання єдиного екземпляра класу DatabaseManager (Singleton).
     *
     * @return Єдиний екземпляр DatabaseManager.
     */
    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            logger.info("Створення нового екземпляра DatabaseManager.");
            instance = new DatabaseManager();
        } else {
            logger.trace("Повернення існуючого екземпляра DatabaseManager.");
        }
        return instance;
    }

    /**
     * Встановлює та повертає підключення до бази даних.
     *
     * @return Об'єкт Connection для роботи з базою даних.
     * @throws SQLException Виникає у випадку помилки підключення до бази даних.
     */
    public Connection getConnection() throws SQLException {
        logger.info("Спроба отримати з'єднання з базою даних: URL='{}', User='{}'", URL, USERNAME);
        try {
            Connection connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
            logger.info("З'єднання з базою даних успішно встановлено. Connection ID: {}", connection.hashCode()); // hashCode для простої ідентифікації
            return connection;
        } catch (SQLException e) {
            logger.fatal("КРИТИЧНА ПОМИЛКА: Не вдалося підключитися до бази даних! URL='{}', User='{}': {}", URL, USERNAME, e.getMessage(), e);
            // Кидаємо виняток далі, щоб викликаючий код міг його обробити
            throw e;
        }
    }

    /**
     * Закриває вказане підключення до бази даних.
     *
     * @param connection Підключення, яке необхідно закрити.
     */
    public void closeConnection(Connection connection) {
        if (connection != null) {
            int connectionId = connection.hashCode(); // Отримуємо ID перед закриттям для логування
            logger.trace("Спроба закрити з'єднання з ID: {}", connectionId);
            try {
                if (!connection.isClosed()) { // Перевіряємо, чи з'єднання ще не закрите
                    connection.close();
                    logger.info("З'єднання з ID {} успішно закрито.", connectionId);
                } else {
                    logger.warn("Спроба закрити вже закрите з'єднання з ID: {}", connectionId);
                }
            } catch (SQLException e) {
                logger.error("Помилка при закритті з'єднання з ID {}: {}", connectionId, e.getMessage(), e);
            }
        } else {
            logger.warn("Спроба закрити null з'єднання.");
        }
    }
}