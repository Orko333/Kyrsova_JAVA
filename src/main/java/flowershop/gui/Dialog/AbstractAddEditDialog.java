package flowershop.gui.Dialog;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.*;
import java.io.File;

/**
 * Абстрактний базовий клас для діалогових вікон додавання або редагування елементів.
 * Надає спільний каркас, стилі та базовий функціонал для валідації,
 * обробки кнопок "Зберегти" та "Скасувати", а також вибору зображення.
 *
 * @param <T> Тип об'єкта, який додається або редагується (наприклад, Accessory, Flower, Bouquet).
 */
public abstract class AbstractAddEditDialog<T> extends JDialog {

    private static final Logger logger = LogManager.getLogger(AbstractAddEditDialog.class);

    protected T item; // Об'єкт, який редагується або створюється
    protected boolean confirmed = false; // Прапорець, що вказує на успішне збереження

    // UI компоненти, які можуть бути спільними
    protected JTextField imagePathField;
    protected JButton browseButton;
    protected JButton okButton, cancelButton;
    protected JLabel previewImageLabel; // Для попереднього перегляду зображення

    // Стилі (можна винести в окремий клас або інтерфейс констант)
    protected static final Color PRIMARY_COLOR = new Color(75, 175, 80);
    protected static final Color BACKGROUND_COLOR = new Color(245, 245, 245);
    public static final Color TEXT_COLOR = new Color(50, 50, 50);
    protected static final Font DEFAULT_FONT = new Font("SansSerif", Font.PLAIN, 12);
    protected static final Font BOLD_FONT = new Font("SansSerif", Font.BOLD, 12);
    protected static final Border FIELD_BORDER = new CompoundBorder(
            new LineBorder(Color.LIGHT_GRAY),
            new EmptyBorder(5, 5, 5, 5)
    );

    /**
     * Конструктор абстрактного діалогового вікна.
     *
     * @param parent        Батьківське вікно.
     * @param title         Заголовок діалогового вікна.
     * @param itemToEdit    Об'єкт для редагування (null, якщо створюється новий).
     */
    public AbstractAddEditDialog(JFrame parent, String title, T itemToEdit) {
        super(parent, title, true); // Модальне вікно
        this.item = itemToEdit;
        logger.info("Створення AbstractAddEditDialog: '{}'. Редагування елемента: {}", title, itemToEdit != null);

        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        initializeBaseUI();
        // Нащадки повинні викликати pack() та setLocationRelativeTo() після своєї ініціалізації
    }

    /**
     * Ініціалізує базовий користувацький інтерфейс, спільний для всіх діалогів.
     * Нащадки розширюють цей метод, додаючи свої специфічні поля форми.
     */
    protected void initializeBaseUI() {
        logger.debug("Ініціалізація базового UI.");
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(BACKGROUND_COLOR);
        ((JPanel) getContentPane()).setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Панель для полів форми (буде заповнена нащадками)
        JPanel formPanel = createFormPanel(); // Абстрактний метод для створення полів
        add(formPanel, BorderLayout.CENTER);
        logger.debug("Панель форми створена та додана.");

        // Панель кнопок
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        buttonPanel.setBackground(BACKGROUND_COLOR);

        okButton = createStyledButton("Зберегти", "/icons/save.png");
        okButton.addActionListener(e -> confirmInput());
        buttonPanel.add(okButton);
        logger.debug("Кнопка 'Зберегти' створена та додана.");

        cancelButton = createStyledButton("Скасувати", "/icons/cancel.png");
        cancelButton.addActionListener(e -> cancelOperation());
        buttonPanel.add(cancelButton);
        logger.debug("Кнопка 'Скасувати' створена та додана.");

        add(buttonPanel, BorderLayout.SOUTH);
        logger.info("Базовий UI ініціалізовано успішно.");
    }

    /**
     * Створює панель з полями форми. Має бути реалізований нащадками.
     * @return JPanel з полями форми.
     */
    protected abstract JPanel createFormPanel();

    /**
     * Заповнює поля форми даними з об'єкта `item` (для режиму редагування).
     * Має бути реалізований нащадками.
     */
    protected abstract void populateFields();

    /**
     * Збирає дані з полів форми, валідує їх та оновлює/створює об'єкт `item`.
     * Має бути реалізований нащадками.
     * @return true, якщо дані валідні та збережені, інакше false.
     */
    protected abstract boolean saveItem();


    /**
     * Створює стилізоване текстове поле.
     * @param columns Кількість колонок (для визначення бажаної ширини).
     * @return JTextField.
     */
    protected JTextField createStyledTextField(int columns) {
        JTextField field = new JTextField(columns);
        field.setBorder(FIELD_BORDER);
        field.setFont(DEFAULT_FONT);
        logger.trace("Створено стилізоване текстове поле з {} колонками.", columns);
        return field;
    }
    /**
     * Створює стилізовану текстову область.
     * @param rows Кількість рядків.
     * @param columns Кількість колонок.
     * @return JTextArea.
     */
    protected JTextArea createStyledTextArea(int rows, int columns) {
        JTextArea area = new JTextArea(rows, columns);
        area.setBorder(FIELD_BORDER);
        area.setFont(DEFAULT_FONT);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        logger.trace("Створено стилізовану текстову область: {}x{}.", rows, columns);
        return area;
    }

    /**
     * Створює стилізовану кнопку.
     * @param text Текст кнопки.
     * @param iconPath Шлях до іконки (більше не використовується для завантаження з файлу).
     * @return JButton.
     */
    protected JButton createStyledButton(String text, String iconPath) {
        logger.debug("Створення стилізованої кнопки: текст='{}', шлях до іконки='{}'", text, iconPath);
        JButton button = new JButton(text);
        // Блок завантаження іконки з файлу видалено/закоментовано
        // if (iconPath != null) {
        //     try {
        //         ImageIcon icon = new ImageIcon(getClass().getResource(iconPath));
        //         if (icon.getImageLoadStatus() == MediaTracker.ERRORED || getClass().getResource(iconPath) == null) {
        //              logger.warn("Ресурс іконки не знайдено: {} для кнопки {}", iconPath, text);
        //         } else {
        //              Image img = icon.getImage().getScaledInstance(16, 16, Image.SCALE_SMOOTH);
        //              button.setIcon(new ImageIcon(img));
        //              logger.debug("Іконка {} успішно завантажена для кнопки {}", iconPath, text);
        //         }
        //     } catch (Exception e) {
        //         logger.error("Помилка завантаження іконки {} для кнопки {}: {}", iconPath, text, e.getMessage());
        //     }
        // }
        button.setBackground(PRIMARY_COLOR);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setFont(BOLD_FONT);
        button.setBorder(new CompoundBorder(
                new LineBorder(new Color(50, 120, 50)),
                new EmptyBorder(5, 15, 5, 15)
        ));
        button.setPreferredSize(new Dimension(130, 35));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(PRIMARY_COLOR.brighter());
            }
            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(PRIMARY_COLOR);
            }
        });
        logger.info("Стилізована кнопка '{}' створена.", text);
        return button;
    }
    /**
     * Додає мітку та компонент до панелі з використанням GridBagConstraints.
     * @param panel Панель, до якої додаються елементи.
     * @param labelText Текст мітки.
     * @param component Компонент для додавання.
     * @param gbc Об'єкт GridBagConstraints для налаштування розміщення.
     * @param gridy Позиція по Y.
     * @param gridwidth Ширина компонента в комірках.
     * @param fill Тип заповнення (наприклад, GridBagConstraints.HORIZONTAL).
     */
    protected void addFormField(JPanel panel, String labelText, JComponent component, GridBagConstraints gbc, int gridy, int gridwidth, int fill) {
        logger.trace("Додавання поля форми: '{}' на позицію y={}", labelText, gridy);
        JLabel label = new JLabel(labelText);
        label.setFont(DEFAULT_FONT);
        label.setForeground(TEXT_COLOR);
        gbc.gridx = 0;
        gbc.gridy = gridy;
        gbc.gridwidth = 1;
        gbc.weightx = 0.2; // Вага для мітки
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.LINE_START; // Вирівнювання мітки
        panel.add(label, gbc);

        gbc.gridx = 1;
        // gbc.gridy = gridy; // gridy вже встановлено
        gbc.gridwidth = gridwidth;
        gbc.weightx = 0.8; // Вага для компонента
        gbc.fill = fill; // Як компонент заповнює комірку
        // gbc.anchor = GridBagConstraints.LINE_START; // Зазвичай для компонента це не потрібно або WEST
        panel.add(component, gbc);
    }

    /**
     * Обробник для кнопки вибору файлу зображення.
     */
    protected void browseImageAction() {
        logger.debug("Виклик дії вибору зображення.");
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Виберіть зображення");
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                "Зображення (JPG, PNG, GIF, WebP)", "jpg", "jpeg", "png", "gif", "webp"));
        fileChooser.setCurrentDirectory(new File(System.getProperty("user.home")));

        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            logger.info("Вибрано файл зображення: {}", selectedFile.getAbsolutePath());
            if (imagePathField != null) {
                imagePathField.setText(selectedFile.getAbsolutePath());
                // updatePreviewImage викликається автоматично через DocumentListener на imagePathField
                logger.debug("Шлях до зображення встановлено в imagePathField.");
            } else {
                logger.warn("imagePathField не ініціалізовано, неможливо встановити шлях до зображення.");
            }
        } else {
            logger.debug("Вибір файлу зображення скасовано користувачем.");
        }
    }

    /**
     * Оновлює мітку попереднього перегляду зображення.
     * @param path Шлях до файлу зображення.
     */
    protected void updatePreviewImage(String path) {
        logger.debug("Спроба оновити попередній перегляд зображення за шляхом: {}", path);
        if (previewImageLabel == null) {
            logger.warn("previewImageLabel не ініціалізовано, оновлення попереднього перегляду неможливе.");
            return;
        }

        if (path != null && !path.isEmpty()) {
            File file = new File(path);
            if (file.exists() && file.isFile()) {
                try {
                    ImageIcon originalIcon = new ImageIcon(path);
                    Image image = originalIcon.getImage();

                    int previewWidth = previewImageLabel.getWidth() > 10 ? previewImageLabel.getWidth() - 10 : 150;
                    int previewHeight = previewImageLabel.getHeight() > 10 ? previewImageLabel.getHeight() - 10 : 150;
                    if (previewWidth <=0 || previewHeight <=0) {
                        previewWidth = 150; previewHeight = 150;
                    }

                    int originalWidth = originalIcon.getIconWidth();
                    int originalHeight = originalIcon.getIconHeight();
                    int newWidth = previewWidth;
                    int newHeight = previewHeight;

                    if (originalWidth > 0 && originalHeight > 0) { // Захист від ділення на нуль та некоректних зображень
                        if (originalWidth > previewWidth || originalHeight > previewHeight) {
                            double widthRatio = (double) previewWidth / originalWidth;
                            double heightRatio = (double) previewHeight / originalHeight;
                            double ratio = Math.min(widthRatio, heightRatio);
                            newWidth = (int) (originalWidth * ratio);
                            newHeight = (int) (originalHeight * ratio);
                        } else {
                            newWidth = originalWidth;
                            newHeight = originalHeight;
                        }
                        Image scaledImage = image.getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH);
                        previewImageLabel.setIcon(new ImageIcon(scaledImage));
                        previewImageLabel.setText(null);
                        logger.info("Попередній перегляд зображення оновлено: {}", path);
                    } else {
                        logger.warn("Не вдалося отримати розміри оригінального зображення: {}. Ширина: {}, Висота: {}", path, originalWidth, originalHeight);
                        previewImageLabel.setIcon(null);
                        previewImageLabel.setText("Помилка розм.");
                    }
                    return;
                } catch (Exception e) {
                    logger.error("Помилка завантаження зображення для попереднього перегляду: {}. {}", path, e.getMessage(), e);
                }
            } else {
                logger.warn("Файл зображення не існує або не є файлом: {}", path);
            }
        }
        previewImageLabel.setIcon(null);
        previewImageLabel.setText("Прев'ю");
        logger.debug("Попередній перегляд зображення скинуто (шлях порожній, файл не існує або помилка).");
    }


    /**
     * Обробник для кнопки "Зберегти". Викликає `saveItem()` для валідації та збереження.
     */
    protected void confirmInput() {
        logger.debug("Натиснуто кнопку 'Зберегти'. Спроба зберегти елемент.");
        if (saveItem()) {
            confirmed = true;
            logger.info("Елемент успішно збережено. Діалог закривається.");
            dispose();
        } else {
            logger.warn("Збереження елемента не вдалося або було скасовано валідацією.");
        }
    }

    /**
     * Обробник для кнопки "Скасувати".
     */
    protected void cancelOperation() {
        logger.info("Натиснуто кнопку 'Скасувати'. Операцію скасовано, діалог закривається.");
        confirmed = false;
        dispose();
    }

    /**
     * Показує діалогове вікно.
     * @return true, якщо користувач натиснув "Зберегти" і дані валідні, інакше false.
     */
    public boolean isConfirmed() {
        return confirmed;
    }

    /**
     * Повертає об'єкт, який редагувався/створювався.
     * @return Об'єкт типу T.
     */
    public T getItem() {
        logger.debug("Запит на отримання елемента. Confirmed: {}", confirmed);
        return item;
    }

    /**
     * Показує повідомлення про помилку валідації.
     * @param message Текст повідомлення.
     */
    protected void showErrorDialog(String message) {
        logger.warn("Показ діалогу помилки: {}", message);
        JOptionPane.showMessageDialog(this, message, "Помилка введення", JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Налаштовує перехід між полями за допомогою клавіші Enter.
     * Компоненти додаються до списку в порядку їх обходу.
     * @param components Послідовність компонентів для навігації.
     */
    protected void setupEnterNavigation(Component... components) {
        logger.debug("Налаштування навігації клавішею Enter для {} компонентів.", components.length);
        if (components == null || components.length == 0) {
            logger.warn("Список компонентів для навігації порожній.");
            return;
        }

        for (int i = 0; i < components.length; i++) {
            final Component currentComponent = components[i];
            final Component nextComponent = (i < components.length - 1) ? components[i+1] : components[0];

            if (currentComponent == null || nextComponent == null) {
                logger.trace("Пропуск null компонента в навігації Enter.");
                continue;
            }

            if (currentComponent instanceof JTextField) {
                ((JTextField) currentComponent).addActionListener(e -> {
                    logger.trace("Enter натиснуто на JTextField, перехід до наступного компонента.");
                    if (nextComponent.isFocusable()) nextComponent.requestFocusInWindow();
                });
            } else if (currentComponent instanceof JComboBox) {
                ((JComboBox<?>) currentComponent).addKeyListener(new KeyAdapter() {
                    @Override
                    public void keyPressed(KeyEvent e) {
                        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                            JComboBox<?> combo = (JComboBox<?>) e.getSource();
                            if (!combo.isPopupVisible()) {
                                logger.trace("Enter натиснуто на JComboBox (popup не видимий), перехід до наступного компонента.");
                                if (nextComponent.isFocusable()) nextComponent.requestFocusInWindow();
                                e.consume();
                            }
                        }
                    }
                });
            } else if (currentComponent instanceof JTextArea) {
                ((JTextArea) currentComponent).addKeyListener(new KeyAdapter() {
                    @Override
                    public void keyPressed(KeyEvent e) {
                        if (e.getKeyCode() == KeyEvent.VK_ENTER && e.isControlDown()) {
                            logger.trace("Ctrl+Enter натиснуто на JTextArea, перехід до наступного компонента.");
                            if (nextComponent.isFocusable()) nextComponent.requestFocusInWindow();
                            e.consume();
                        }
                    }
                });
            } else if (currentComponent instanceof JCheckBox || currentComponent instanceof JRadioButton || currentComponent instanceof JSpinner) {
                currentComponent.addKeyListener(new KeyAdapter() {
                    @Override
                    public void keyPressed(KeyEvent e) {
                        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                            logger.trace("Enter натиснуто на {}, перехід до наступного компонента.", currentComponent.getClass().getSimpleName());
                            if (nextComponent.isFocusable()) nextComponent.requestFocusInWindow();
                            e.consume();
                        }
                    }
                });
            }
        }
        logger.info("Навігація клавішею Enter успішно налаштована.");
    }

    /**
     * Перевіряє, чи було збережено дані.
     * @return true, якщо дані збережено, інакше false.
     */
    public boolean isSaved() {
        logger.debug("Перевірка статусу збереження: {}", confirmed);
        return confirmed;
    }

    /**
     * Показує діалог і повертає результат збереження.
     * Цей метод робить діалог видимим.
     * @return true, якщо дані були успішно збережені, інакше false.
     */
    public boolean showDialog() {
        logger.info("Показ діалогового вікна: {}", getTitle());
        // pack(); // Зазвичай pack() викликається в конструкторі нащадка
        // setLocationRelativeTo(getParent()); // І це теж
        setVisible(true); // Головне - зробити вікно видимим
        logger.info("Діалогове вікно '{}' закрито. Статус збереження: {}", getTitle(), confirmed);
        return isSaved(); // Повертає стан confirmed після закриття діалогу
    }

    public boolean isSuccessful() {
        return confirmed;
    }
}