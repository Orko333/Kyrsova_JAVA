package flowershop.gui.Dialog;

import flowershop.models.Accessory;
import flowershop.models.Accessory.AccessoryType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.io.File;

/**
 * Діалогове вікно для додавання або редагування аксесуара.
 * Дозволяє вводити та редагувати дані про назву, тип, ціну, колір, розмір, кількість на складі,
 * опис та зображення аксесуара.
 */
public class AddEditAccessoryDialog extends AbstractAddEditDialog<Accessory> {

    private static final Logger logger = LogManager.getLogger(AddEditAccessoryDialog.class);

    private JTextField nameField;
    private JComboBox<AccessoryType> typeCombo;
    private JTextField priceField;
    private JTextField colorField;
    private JTextField sizeField;
    private JTextField stockQuantityField;
    private JTextArea descriptionArea;

    /**
     * Конструктор діалогового вікна.
     *
     * @param parent          батьківське вікно
     * @param accessoryToEdit аксесуар для редагування (null, якщо створюється новий)
     */
    public AddEditAccessoryDialog(JFrame parent, Accessory accessoryToEdit) {
        super(parent, accessoryToEdit == null ? "Додати аксесуар" : "Редагувати аксесуар", accessoryToEdit);
        if (this.item != null) {
            populateFields();
        }
        pack();
        setMinimumSize(new Dimension(650, 550));
        setLocationRelativeTo(parent);
        setResizable(true);
        SwingUtilities.invokeLater(() -> nameField.requestFocusInWindow());
        logger.info("Діалогове вікно {} відкрито.", accessoryToEdit == null ? "додавання" : "редагування");
    }

    // Формування UI

    /**
     * Створює панель форми для введення даних про аксесуар.
     *
     * @return панель із компонентами форми
     */
    @Override
    protected JPanel createFormPanel() {
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(BACKGROUND_COLOR);
        Border outerBorder = BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(PRIMARY_COLOR),
                "Інформація про аксесуар",
                TitledBorder.DEFAULT_JUSTIFICATION,
                TitledBorder.DEFAULT_POSITION,
                new Font("SansSerif", Font.BOLD, 14),
                PRIMARY_COLOR);
        Border innerBorder = BorderFactory.createEmptyBorder(10, 10, 10, 10);
        formPanel.setBorder(new CompoundBorder(outerBorder, innerBorder));

        GridBagConstraints mainGbc = new GridBagConstraints();
        mainGbc.insets = new Insets(5, 5, 5, 5);
        mainGbc.fill = GridBagConstraints.BOTH;

        mainGbc.gridx = 0;
        mainGbc.gridy = 0;
        mainGbc.weightx = 0.6;
        mainGbc.weighty = 1.0;
        formPanel.add(createLeftColumnPanel(), mainGbc);

        mainGbc.gridx = 1;
        mainGbc.weightx = 0.4;
        formPanel.add(createRightColumnPanel(), mainGbc);

        setupEnterNavigation(nameField, typeCombo, priceField, colorField, sizeField, stockQuantityField, descriptionArea);
        return formPanel;
    }

    /**
     * Створює ліву колонку з полями для введення текстових і числових даних.
     *
     * @return панель лівої колонки
     */
    private JPanel createLeftColumnPanel() {
        JPanel leftColumnPanel = new JPanel(new GridBagLayout());
        leftColumnPanel.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;

        nameField = createStyledTextField(20);
        addFormField(leftColumnPanel, "Назва:", nameField, gbc, 0, 1, GridBagConstraints.HORIZONTAL);

        typeCombo = new JComboBox<>(AccessoryType.values());
        typeCombo.setRenderer(new AccessoryTypeRenderer());
        typeCombo.setFont(DEFAULT_FONT);
        typeCombo.setBackground(Color.WHITE);
        addFormField(leftColumnPanel, "Тип:", typeCombo, gbc, 1, 1, GridBagConstraints.HORIZONTAL);

        priceField = createStyledTextField(10);
        addFormField(leftColumnPanel, "Ціна (грн):", priceField, gbc, 2, 1, GridBagConstraints.HORIZONTAL);

        colorField = createStyledTextField(15);
        addFormField(leftColumnPanel, "Колір:", colorField, gbc, 3, 1, GridBagConstraints.HORIZONTAL);

        sizeField = createStyledTextField(15);
        addFormField(leftColumnPanel, "Розмір:", sizeField, gbc, 4, 1, GridBagConstraints.HORIZONTAL);

        stockQuantityField = createStyledTextField(10);
        addFormField(leftColumnPanel, "Кількість на складі:", stockQuantityField, gbc, 5, 1, GridBagConstraints.HORIZONTAL);

        descriptionArea = createStyledTextArea(4, 20);
        JScrollPane descriptionScrollPane = new JScrollPane(descriptionArea);
        descriptionScrollPane.setBorder(FIELD_BORDER);
        addFormField(leftColumnPanel, "Опис:", descriptionScrollPane, gbc, 6, 1, GridBagConstraints.BOTH);
        gbc.weighty = 1.0;

        return leftColumnPanel;
    }

    /**
     * Створює праву колонку з полем для вибору зображення.
     *
     * @return панель правої колонки
     */
    private JPanel createRightColumnPanel() {
        JPanel rightColumnPanel = new JPanel(new BorderLayout(0, 10));
        rightColumnPanel.setOpaque(false);
        rightColumnPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(PRIMARY_COLOR.darker()), "Зображення"));

        imagePathField = createStyledTextField(20);
        imagePathField.setEditable(false);
        imagePathField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                updatePreviewImage(imagePathField.getText());
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                updatePreviewImage(imagePathField.getText());
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                updatePreviewImage(imagePathField.getText());
            }
        });

        browseButton = createStyledButton("Огляд...", null);
        browseButton.addActionListener(e -> browseImageAction());

        JPanel imagePathPanel = new JPanel(new BorderLayout(5, 0));
        imagePathPanel.setOpaque(false);
        imagePathPanel.add(imagePathField, BorderLayout.CENTER);
        imagePathPanel.add(browseButton, BorderLayout.EAST);
        rightColumnPanel.add(imagePathPanel, BorderLayout.NORTH);

        previewImageLabel = new JLabel("Немає зображення", JLabel.CENTER);
        previewImageLabel.setPreferredSize(new Dimension(200, 180));
        previewImageLabel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        previewImageLabel.setOpaque(true);
        previewImageLabel.setBackground(Color.WHITE);
        JScrollPane previewScrollPane = new JScrollPane(previewImageLabel);
        previewScrollPane.setBorder(null);
        rightColumnPanel.add(previewScrollPane, BorderLayout.CENTER);

        return rightColumnPanel;
    }

    // Заповнення полів

    /**
     * Заповнює поля форми даними аксесуара, якщо редагується існуючий аксесуар.
     */
    @Override
    protected void populateFields() {
        if (item != null) {
            nameField.setText(item.getName());
            typeCombo.setSelectedItem(item.getType());
            priceField.setText(String.format("%.2f", item.getPrice()).replace(',', '.'));
            colorField.setText(item.getColor());
            sizeField.setText(item.getSize());
            stockQuantityField.setText(String.valueOf(item.getStockQuantity()));
            descriptionArea.setText(item.getDescription());
            imagePathField.setText(item.getImagePath());
            updatePreviewImage(item.getImagePath());
        }
    }

    // Збереження даних

    /**
     * Зберігає введені дані у об'єкт аксесуара.
     *
     * @return true, якщо збереження успішне, інакше false
     */
    @Override
    protected boolean saveItem() {
        try {
            String name = nameField.getText().trim();
            AccessoryType type = (AccessoryType) typeCombo.getSelectedItem();
            String priceStr = priceField.getText().replace(',', '.').trim();
            String color = colorField.getText().trim();
            String size = sizeField.getText().trim();
            String stockStr = stockQuantityField.getText().trim();
            String description = descriptionArea.getText().trim();
            String imagePath = imagePathField.getText().trim();

            // Валідація полів
            if (name.isEmpty()) {
                showErrorDialog("Назва аксесуара не може бути порожньою.");
                nameField.requestFocusInWindow();
                return false;
            }
            if (type == null) {
                showErrorDialog("Необхідно вибрати тип аксесуара.");
                typeCombo.requestFocusInWindow();
                return false;
            }
            if (priceStr.isEmpty()) {
                showErrorDialog("Ціна не може бути порожньою.");
                priceField.requestFocusInWindow();
                return false;
            }
            double price = Double.parseDouble(priceStr);
            if (price <= 0) {
                showErrorDialog("Ціна повинна бути більшою за нуль.");
                priceField.requestFocusInWindow();
                return false;
            }
            if (color.isEmpty()) {
                showErrorDialog("Колір аксесуара не може бути порожнім.");
                colorField.requestFocusInWindow();
                return false;
            }
            if (size.isEmpty()) {
                showErrorDialog("Розмір аксесуара не може бути порожнім.");
                sizeField.requestFocusInWindow();
                return false;
            }
            if (stockStr.isEmpty()) {
                showErrorDialog("Кількість на складі не може бути порожньою.");
                stockQuantityField.requestFocusInWindow();
                return false;
            }
            int stockQuantity = Integer.parseInt(stockStr);
            if (stockQuantity < 0) {
                showErrorDialog("Кількість на складі не може бути від'ємною.");
                stockQuantityField.requestFocusInWindow();
                return false;
            }
            if (!imagePath.isEmpty() && !new File(imagePath).isFile()) {
                if (JOptionPane.showConfirmDialog(this,
                        "Файл зображення за вказаним шляхом не знайдено або це не файл.\nПродовжити збереження без зображення (або з поточним, якщо є)?",
                        "Попередження: Зображення",
                        JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.NO_OPTION) {
                    imagePathField.requestFocusInWindow();
                    return false;
                }
            }

            // Збереження даних
            if (item == null) {
                item = new Accessory(name, price, description, imagePath, stockQuantity, type, color, size);
            } else {
                item.setName(name);
                item.setType(type);
                item.setPrice(price);
                item.setColor(color);
                item.setSize(size);
                item.setStockQuantity(stockQuantity);
                item.setDescription(description);
                item.setImagePath(imagePath);
            }
            logger.info("Аксесуар успішно збережений: {}", name);
            return true;
        } catch (NumberFormatException ex) {
            showErrorDialog("Будь ласка, введіть коректні числові значення для ціни та кількості.");
            if (!priceField.getText().matches("\\d*\\.?\\d+")) {
                priceField.requestFocusInWindow();
            } else {
                stockQuantityField.requestFocusInWindow();
            }
            logger.error("Помилка формату чисел: {}", ex.getMessage());
            return false;
        } catch (Exception ex) {
            showErrorDialog("Сталася помилка: " + ex.getMessage());
            logger.error("Помилка збереження аксесуара: {}", ex.getMessage());
            return false;
        }
    }

    /**
     * Повертає об'єкт аксесуара.
     *
     * @return збережений або редагований аксесуар
     */
    public Accessory getAccessory() {
        return item;
    }

    /**
     * Рендерер для відображення типів аксесуарів у випадаючому списку.
     */
    public static class AccessoryTypeRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value instanceof AccessoryType) {
                label.setText(((AccessoryType) value).getDisplayName());
            }
            if (isSelected) {
                label.setBackground(PRIMARY_COLOR.darker());
                label.setForeground(Color.WHITE);
            } else {
                label.setBackground(Color.WHITE);
                label.setForeground(TEXT_COLOR);
            }
            return label;
        }
    }
}