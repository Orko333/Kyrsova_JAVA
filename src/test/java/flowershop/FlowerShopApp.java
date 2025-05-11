package flowershop;

import flowershop.gui.Tab.AccessoriesTab;
import flowershop.gui.Tab.BouquetsTab;
import flowershop.gui.Tab.FlowersTab;

import javax.swing.*;

/**
 * Клас FlowerShopApp є головним вікном графічного інтерфейсу користувача для програми "Квітковий магазин".
 * Він створює основне вікно програми з вкладками для букетів, квітів і аксесуарів.
 */
public class FlowerShopApp extends JFrame {

    /**
     * Конструктор створює вікно програми, встановлює заголовок, розміри та додає вкладки.
     */
    public FlowerShopApp() {
        // Встановлюємо заголовок вікна
        setTitle("Квітковий магазин");

        // Завершення роботи програми при закритті вікна
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Встановлюємо розміри вікна
        setSize(1000, 700);

        // Розміщення вікна по центру екрана
        setLocationRelativeTo(null);

        // Створюємо панель з вкладками
        JTabbedPane tabbedPane = new JTabbedPane();

        // Додаємо вкладки до панелі
        tabbedPane.addTab("Букети", new BouquetsTab());
        tabbedPane.addTab("Квіти", new FlowersTab());
        tabbedPane.addTab("Аксесуари", new AccessoriesTab());

        // Додаємо панель вкладок до вікна
        add(tabbedPane);
    }

    /**
     * Метод main — точка входу до програми.
     * Створює екземпляр головного вікна FlowerShopApp і робить його видимим.
     *
     * @param args аргументи командного рядка (не використовуються)
     */
    public static void main(String[] args) {
        // Запускаємо GUI у потоці обробки подій Swing
        SwingUtilities.invokeLater(() -> {
            new FlowerShopApp().setVisible(true);
        });
    }
}
