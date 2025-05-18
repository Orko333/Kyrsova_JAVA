package flowershop;

import flowershop.gui.Tab.BouquetsTab;
import flowershop.gui.Tab.FlowersTab;
import flowershop.gui.Tab.AccessoriesTab;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;

/**
 * Клас FlowerShopApp є головним вікном графічного інтерфейсу користувача для програми "Квітковий магазин".
 * Він створює основне вікно програми з вкладками для букетів, квітів і аксесуарів.
 */
public class FlowerShopApp extends JFrame {

    private static final Logger logger = LogManager.getLogger(FlowerShopApp.class);

    /**
     * Конструктор створює вікно програми, встановлює заголовок, розміри та додає вкладки.
     */
    public FlowerShopApp() {
        // Логування початку ініціалізації вікна
        logger.info("Ініціалізація головного вікна FlowerShopApp");

        setTitle("Квітковий магазин");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 700);
        setLocationRelativeTo(null);

        JTabbedPane tabbedPane = new JTabbedPane();

        // Логування додавання кожної вкладки
        logger.info("Додавання вкладки 'Букети'");
        tabbedPane.addTab("Букети", new BouquetsTab());

        logger.info("Додавання вкладки 'Квіти'");
        tabbedPane.addTab("Квіти", new FlowersTab());

        logger.info("Додавання вкладки 'Аксесуари'");
        tabbedPane.addTab("Аксесуари", new AccessoriesTab());

        add(tabbedPane);

        logger.info("Головне вікно FlowerShopApp ініціалізовано");
    }

    /**
     * Метод main — точка входу до програми.
     * Створює екземпляр головного вікна FlowerShopApp і робить його видимим.
     *
     * @param args аргументи командного рядка (не використовуються)
     */
    public static void main(String[] args) {
        logger.error("Це тестове повідомлення для перевірки логування");
        // Логування старту програми
        logger.info("Запуск програми FlowerShopApp");

        SwingUtilities.invokeLater(() -> {
            new FlowerShopApp().setVisible(true);
        });

        // Логування завершення роботи програми
        logger.info("Програма FlowerShopApp завершила роботу");
    }
}