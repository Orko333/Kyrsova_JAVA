package flowershop.gui.Dialog;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.*;
import java.io.File;

/**
 * Абстрактний базовий клас для діалогових вікон додавання або редагування елементів.
 * Надає спільний інтерфейс і функціонал для створення форм, стилізації компонентів,
 * обробки зображень, навігації між полями та збереження даних.
 *
 * @param <T> тип об'єкта, який додається або редагується
 */
public abstract class AbstractAddEditDialog<T> extends JDialog {

    private static final Logger logger = LogManager.getLogger(AbstractAddEditDialog.class);

    protected T item;
    protected boolean confirmed = false;
    protected JTextField imagePathField;
    protected JButton browseButton;
    protected JButton okButton, cancelButton;
    protected JLabel previewImageLabel;

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
     * Конструктор діалогового вікна.
     *
     * @param parent     батьківське вікно
     * @param title      заголовок діалогового вікна
     * @param itemToEdit об'єкт для редагування (null, якщо створюється новий)
     */
    public AbstractAddEditDialog(JFrame parent, String title, T itemToEdit) {
        super(parent, title, true);
        this.item = itemToEdit;
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        initializeBaseUI();
        logger.info("Діалогове вікно '{}' ініціалізовано.", title);
    }

    // Ініціалізація UI

    /**
     * Ініціалізує базовий користувацький інтерфейс діалогового вікна.
     */
    protected void initializeBaseUI() {
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(BACKGROUND_COLOR);
        ((JPanel) getContentPane()).setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        add(createFormPanel(), BorderLayout.CENTER);
        add(createButtonPanel(), BorderLayout.SOUTH);
    }

    /**
     * Створює панель із кнопками "Зберегти" та "Скасувати".
     *
     * @return панель із кнопками
     */
    private JPanel createButtonPanel() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        buttonPanel.setBackground(BACKGROUND_COLOR);

        okButton = createStyledButton("Зберегти", "/icons/save.png");
        okButton.addActionListener(e -> confirmInput());
        buttonPanel.add(okButton);

        cancelButton = createStyledButton("Скасувати", "/icons/cancel.png");
        cancelButton.addActionListener(e -> cancelOperation());
        buttonPanel.add(cancelButton);

        return buttonPanel;
    }

    /**
     * Створює панель із полями форми. Реалізація визначається в похідних класах.
     *
     * @return панель із полями форми
     */
    protected abstract JPanel createFormPanel();

    // Робота з даними

    /**
     * Заповнює поля форми даними з об'єкта item. Реалізація визначається в похідних класах.
     */
    protected abstract void populateFields();

    /**
     * Збирає дані з полів форми, валідує їх та оновлює/створює об'єкт item.
     * Реалізація визначається в похідних класах.
     *
     * @return true, якщо дані валідні та збережені, інакше false
     */
    protected abstract boolean saveItem();

    // Створення стилізованих компонентів

    /**
     * Створює стилізоване текстове поле.
     *
     * @param columns кількість колонок
     * @return стилізоване текстове поле
     */
    protected JTextField createStyledTextField(int columns) {
        JTextField field = new JTextField(columns);
        field.setBorder(FIELD_BORDER);
        field.setFont(DEFAULT_FONT);
        return field;
    }

    /**
     * Створює стилізовану текстову область.
     *
     * @param rows    кількість рядків
     * @param columns кількість колонок
     * @return стилізована текстова область
     */
    protected JTextArea createStyledTextArea(int rows, int columns) {
        JTextArea area = new JTextArea(rows, columns);
        area.setBorder(FIELD_BORDER);
        area.setFont(DEFAULT_FONT);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        return area;
    }

    /**
     * Створює стилізовану кнопку.
     *
     * @param text     текст кнопки
     * @param iconPath шлях до іконки (може бути null)
     * @return стилізована кнопка
     */
    protected JButton createStyledButton(String text, String iconPath) {
        JButton button = new JButton(text);
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
        return button;
    }

    /**
     * Додає мітку та компонент до панелі з використанням GridBagConstraints.
     *
     * @param panel      панель, до якої додаються елементи
     * @param labelText  текст мітки
     * @param component  компонент для додавання
     * @param gbc        об'єкт GridBagConstraints
     * @param gridy      позиція по осі Y
     * @param gridwidth  ширина компонента в комірках
     * @param fill       тип заповнення (GridBagConstraints.NONE, HORIZONTAL, BOTH тощо)
     */
    protected void addFormField(JPanel panel, String labelText, JComponent component, GridBagConstraints gbc, int gridy, int gridwidth, int fill) {
        JLabel label = new JLabel(labelText);
        label.setFont(DEFAULT_FONT);
        label.setForeground(TEXT_COLOR);
        gbc.gridx = 0;
        gbc.gridy = gridy;
        gbc.gridwidth = 1;
        gbc.weightx = 0.2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.LINE_START;
        panel.add(label, gbc);

        gbc.gridx = 1;
        gbc.gridwidth = gridwidth;
        gbc.weightx = 0.8;
        gbc.fill = fill;
        panel.add(component, gbc);
    }

    // Обробка зображень

    /**
     * Відкриває діалог вибору файлу зображення та встановлює шлях у поле imagePathField.
     */
    protected void browseImageAction() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Виберіть зображення");
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                "Зображення (JPG, PNG, GIF, WebP)", "jpg", "jpeg", "png", "gif", "webp"));
        fileChooser.setCurrentDirectory(new File(System.getProperty("user.home")));

        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            if (imagePathField != null) {
                imagePathField.setText(selectedFile.getAbsolutePath());
                logger.debug("Вибрано зображення: {}", selectedFile.getAbsolutePath());
            }
        }
    }

    /**
     * Оновлює мітку попереднього перегляду зображення на основі вказаного шляху.
     *
     * @param path шлях до файлу зображення
     */
    protected void updatePreviewImage(String path) {
        if (previewImageLabel == null) {
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
                    if (previewWidth <= 0 || previewHeight <= 0) {
                        previewWidth = 150;
                        previewHeight = 150;
                    }

                    int originalWidth = originalIcon.getIconWidth();
                    int originalHeight = originalIcon.getIconHeight();
                    int newWidth = previewWidth;
                    int newHeight = previewHeight;

                    if (originalWidth > 0 && originalHeight > 0) {
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
                        return;
                    }
                } catch (Exception e) {
                    logger.error("Помилка завантаження зображення для попереднього перегляду: {}. {}", path, e.getMessage(), e);
                }
            }
        }
        previewImageLabel.setIcon(null);
        previewImageLabel.setText("Прев'ю");
    }

    // Обробка подій

    /**
     * Обробляє натискання кнопки "Зберегти", викликаючи збереження даних і закриваючи діалог при успіху.
     */
    protected void confirmInput() {
        if (saveItem()) {
            confirmed = true;
            dispose();
            logger.info("Дані успішно збережено.");
        }
    }

    /**
     * Обробляє натискання кнопки "Скасувати", закриваючи діалог без збереження.
     */
    protected void cancelOperation() {
        confirmed = false;
        dispose();
        logger.info("Операцію скасовано.");
    }

    /**
     * Налаштовує перехід між полями за допомогою клавіші Enter.
     *
     * @param components послідовність компонентів для навігації
     */
    protected void setupEnterNavigation(Component... components) {
        if (components == null || components.length == 0) {
            return;
        }

        for (int i = 0; i < components.length; i++) {
            final Component currentComponent = components[i];
            final Component nextComponent = (i < components.length - 1) ? components[i + 1] : components[0];

            if (currentComponent == null || nextComponent == null) {
                continue;
            }

            if (currentComponent instanceof JTextField) {
                ((JTextField) currentComponent).addActionListener(e -> {
                    if (nextComponent.isFocusable()) {
                        nextComponent.requestFocusInWindow();
                    }
                });
            } else if (currentComponent instanceof JComboBox) {
                ((JComboBox<?>) currentComponent).addKeyListener(new KeyAdapter() {
                    @Override
                    public void keyPressed(KeyEvent e) {
                        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                            JComboBox<?> combo = (JComboBox<?>) e.getSource();
                            if (!combo.isPopupVisible() && nextComponent.isFocusable()) {
                                nextComponent.requestFocusInWindow();
                                e.consume();
                            }
                        }
                    }
                });
            } else if (currentComponent instanceof JTextArea) {
                ((JTextArea) currentComponent).addKeyListener(new KeyAdapter() {
                    @Override
                    public void keyPressed(KeyEvent e) {
                        if (e.getKeyCode() == KeyEvent.VK_ENTER && e.isControlDown() && nextComponent.isFocusable()) {
                            nextComponent.requestFocusInWindow();
                            e.consume();
                        }
                    }
                });
            } else if (currentComponent instanceof JCheckBox || currentComponent instanceof JRadioButton || currentComponent instanceof JSpinner) {
                currentComponent.addKeyListener(new KeyAdapter() {
                    @Override
                    public void keyPressed(KeyEvent e) {
                        if (e.getKeyCode() == KeyEvent.VK_ENTER && nextComponent.isFocusable()) {
                            nextComponent.requestFocusInWindow();
                            e.consume();
                        }
                    }
                });
            }
        }
    }

    // Допоміжні методи

    /**
     * Показує повідомлення про помилку валідації.
     *
     * @param message текст повідомлення
     */
    protected void showErrorDialog(String message) {
        JOptionPane.showMessageDialog(this, message, "Помилка введення", JOptionPane.ERROR_MESSAGE);
        logger.warn("Помилка валідації: {}", message);
    }

    /**
     * Показує діалог і повертає результат збереження.
     *
     * @return true, якщо дані збережено
     */
    public boolean showDialog() {
        setVisible(true);
        return isSaved();
    }

    /**
     * Перевіряє, чи було збережено дані.
     *
     * @return true, якщо дані збережено
     */
    public boolean isSaved() {
        return confirmed;
    }

    /**
     * Повертає об'єкт, який редагувався або створювався.
     *
     * @return об'єкт типу T
     */
    public T getItem() {
        return item;
    }
}