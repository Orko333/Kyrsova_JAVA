package flowershop.gui.Dialog;

import flowershop.models.Flower;
import flowershop.models.Flower.FlowerType;
import flowershop.models.Flower.FreshnessLevel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.io.File;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 * Діалогове вікно для додавання або редагування інформації про квітку.
 * Дозволяє вводити та редагувати дані про тип, ціну, свіжість, довжину стебла, колір, країну походження,
 * статус горщика, кількість на складі та зображення квітки.
 */
public class AddEditFlowerDialog extends AbstractAddEditDialog<Flower> {

    private static final Logger logger = LogManager.getLogger(AddEditFlowerDialog.class);

    private JComboBox<FlowerType> typeCombo;
    private JTextField priceField;
    private JSlider freshnessSlider;
    private JLabel freshnessValueLabel;
    private JTextField stemLengthField;
    private JTextField colorField;
    private JTextField countryField;
    private JCheckBox pottedCheckBox;
    private JTextField stockQuantityField;

    /**
     * Конструктор діалогового вікна.
     *
     * @param parent        батьківське вікно
     * @param flowerToEdit  квітка для редагування (null, якщо створюється нова)
     */
    public AddEditFlowerDialog(JFrame parent, Flower flowerToEdit) {
        super(parent, flowerToEdit == null ? "Додати квітку" : "Редагувати квітку", flowerToEdit);
        if (this.item != null) {
            populateFields();
        }
        pack();
        setMinimumSize(new Dimension(550, 620));
        setLocationRelativeTo(parent);
        setResizable(true);
        SwingUtilities.invokeLater(() -> typeCombo.requestFocusInWindow());
        logger.info("Діалогове вікно {} відкрито.", flowerToEdit == null ? "додавання" : "редагування");
    }

    // Формування UI

    /**
     * Створює панель форми для введення даних про квітку.
     *
     * @return панель із компонентами форми
     */
    @Override
    protected JPanel createFormPanel() {
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(BACKGROUND_COLOR);
        Border outerBorder = BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(PRIMARY_COLOR),
                "Інформація про квітку",
                TitledBorder.DEFAULT_JUSTIFICATION,
                TitledBorder.DEFAULT_POSITION,
                new Font("SansSerif", Font.BOLD, 14),
                PRIMARY_COLOR);
        Border innerBorder = BorderFactory.createEmptyBorder(10, 10, 10, 10);
        formPanel.setBorder(new CompoundBorder(outerBorder, innerBorder));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.anchor = GridBagConstraints.WEST;

        addTypeField(formPanel, gbc);
        addPriceField(formPanel, gbc);
        addFreshnessField(formPanel, gbc);
        addStemLengthField(formPanel, gbc);
        addColorField(formPanel, gbc);
        addCountryField(formPanel, gbc);
        addStockQuantityField(formPanel, gbc);
        addPottedField(formPanel, gbc);
        addImageField(formPanel, gbc);

        setupEnterNavigation(typeCombo, priceField, stemLengthField, colorField, countryField, stockQuantityField, pottedCheckBox);
        return formPanel;
    }

    /**
     * Додає поле для вибору типу квітки.
     *
     * @param formPanel панель форми
     * @param gbc       конфігурація GridBagConstraints
     */
    private void addTypeField(JPanel formPanel, GridBagConstraints gbc) {
        typeCombo = new JComboBox<>(FlowerType.values());
        typeCombo.setRenderer(new FlowerTypeRenderer());
        typeCombo.setFont(DEFAULT_FONT);
        typeCombo.setBackground(Color.WHITE);
        addFormField(formPanel, "Тип квітки:", typeCombo, gbc, 0, 1, GridBagConstraints.HORIZONTAL);
    }

    /**
     * Додає поле для введення ціни.
     *
     * @param formPanel панель форми
     * @param gbc       конфігурація GridBagConstraints
     */
    private void addPriceField(JPanel formPanel, GridBagConstraints gbc) {
        priceField = createStyledTextField(10);
        addFormField(formPanel, "Ціна (грн):", priceField, gbc, 1, 1, GridBagConstraints.HORIZONTAL);
    }

    /**
     * Додає поле для вибору рівня свіжості.
     *
     * @param formPanel панель форми
     * @param gbc       конфігурація GridBagConstraints
     */
    private void addFreshnessField(JPanel formPanel, GridBagConstraints gbc) {
        JPanel freshnessPanel = new JPanel(new BorderLayout(5, 0));
        freshnessPanel.setOpaque(false);
        freshnessSlider = new JSlider(0, 100, 75);
        freshnessSlider.setMajorTickSpacing(25);
        freshnessSlider.setMinorTickSpacing(5);
        freshnessSlider.setPaintTicks(true);
        freshnessSlider.setPaintLabels(true);
        freshnessSlider.setFont(new Font("SansSerif", Font.PLAIN, 10));
        freshnessValueLabel = new JLabel(freshnessSlider.getValue() + "%", SwingConstants.RIGHT);
        freshnessValueLabel.setFont(BOLD_FONT);
        freshnessSlider.addChangeListener(e -> {
            freshnessValueLabel.setText(freshnessSlider.getValue() + "%");
            updateFreshnessLabelColor();
        });
        updateFreshnessLabelColor();
        freshnessPanel.add(freshnessSlider, BorderLayout.CENTER);
        freshnessPanel.add(freshnessValueLabel, BorderLayout.EAST);
        addFormField(formPanel, "Свіжість:", freshnessPanel, gbc, 2, 1, GridBagConstraints.HORIZONTAL);
    }

    /**
     * Додає поле для введення довжини стебла.
     *
     * @param formPanel панель форми
     * @param gbc       конфігурація GridBagConstraints
     */
    private void addStemLengthField(JPanel formPanel, GridBagConstraints gbc) {
        stemLengthField = createStyledTextField(10);
        addFormField(formPanel, "Довжина стебла (см):", stemLengthField, gbc, 3, 1, GridBagConstraints.HORIZONTAL);
    }

    /**
     * Додає поле для введення кольору.
     *
     * @param formPanel панель форми
     * @param gbc       конфігурація GridBagConstraints
     */
    private void addColorField(JPanel formPanel, GridBagConstraints gbc) {
        colorField = createStyledTextField(15);
        addFormField(formPanel, "Колір:", colorField, gbc, 4, 1, GridBagConstraints.HORIZONTAL);
    }

    /**
     * Додає поле для введення країни походження.
     *
     * @param formPanel панель форми
     * @param gbc       конфігурація GridBagConstraints
     */
    private void addCountryField(JPanel formPanel, GridBagConstraints gbc) {
        countryField = createStyledTextField(15);
        addFormField(formPanel, "Країна походження:", countryField, gbc, 5, 1, GridBagConstraints.HORIZONTAL);
    }

    /**
     * Додає поле для введення кількості на складі.
     *
     * @param formPanel панель форми
     * @param gbc       конфігурація GridBagConstraints
     */
    private void addStockQuantityField(JPanel formPanel, GridBagConstraints gbc) {
        stockQuantityField = createStyledTextField(10);
        addFormField(formPanel, "Кількість на складі:", stockQuantityField, gbc, 6, 1, GridBagConstraints.HORIZONTAL);
    }

    /**
     * Додає прапорець для вказівки, чи є квітка в горщику.
     *
     * @param formPanel панель форми
     * @param gbc       конфігурація GridBagConstraints
     */
    private void addPottedField(JPanel formPanel, GridBagConstraints gbc) {
        pottedCheckBox = new JCheckBox("В горщику");
        pottedCheckBox.setFont(DEFAULT_FONT);
        pottedCheckBox.setOpaque(false);
        gbc.gridx = 0;
        gbc.gridy = 7;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        formPanel.add(pottedCheckBox, gbc);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.gridwidth = 1;
    }

    /**
     * Додає поле для вибору зображення квітки.
     *
     * @param formPanel панель форми
     * @param gbc       конфігурація GridBagConstraints
     */
    private void addImageField(JPanel formPanel, GridBagConstraints gbc) {
        JPanel imagePanel = new JPanel(new BorderLayout(0, 5));
        imagePanel.setOpaque(false);
        imagePanel.setBorder(BorderFactory.createTitledBorder(
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

        JPanel imagePathControlsPanel = new JPanel(new BorderLayout(5, 0));
        imagePathControlsPanel.setOpaque(false);
        imagePathControlsPanel.add(imagePathField, BorderLayout.CENTER);
        imagePathControlsPanel.add(browseButton, BorderLayout.EAST);
        imagePanel.add(imagePathControlsPanel, BorderLayout.NORTH);

        previewImageLabel = new JLabel("Немає зображення", JLabel.CENTER);
        previewImageLabel.setPreferredSize(new Dimension(180, 150));
        previewImageLabel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        previewImageLabel.setOpaque(true);
        previewImageLabel.setBackground(Color.WHITE);
        JScrollPane previewScrollPane = new JScrollPane(previewImageLabel);
        previewScrollPane.setBorder(null);
        imagePanel.add(previewScrollPane, BorderLayout.CENTER);

        gbc.gridx = 0;
        gbc.gridy = 8;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 0.4;
        formPanel.add(imagePanel, gbc);
    }

    /**
     * Оновлює колір мітки рівня свіжості залежно від значення слайдера.
     */
    private void updateFreshnessLabelColor() {
        int value = freshnessSlider.getValue();
        FreshnessLevel level = FreshnessLevel.fromValue(value);
        switch (level) {
            case VERY_LOW, LOW -> freshnessValueLabel.setForeground(Color.RED.darker());
            case MEDIUM -> freshnessValueLabel.setForeground(Color.ORANGE.darker());
            case HIGH, VERY_HIGH -> freshnessValueLabel.setForeground(PRIMARY_COLOR.darker());
        }
    }

    // Заповнення полів

    /**
     * Заповнює поля форми даними квітки, якщо редагується існуюча квітка.
     */
    @Override
    protected void populateFields() {
        if (item != null) {
            typeCombo.setSelectedItem(item.getType());
            priceField.setText(String.format("%.2f", item.getPrice()).replace(',', '.'));
            freshnessSlider.setValue(item.getFreshness());
            updateFreshnessLabelColor();
            stemLengthField.setText(String.valueOf(item.getStemLength()));
            colorField.setText(item.getColor());
            countryField.setText(item.getCountryOfOrigin());
            pottedCheckBox.setSelected(item.isPotted());
            stockQuantityField.setText(String.valueOf(item.getStockQuantity()));
            imagePathField.setText(item.getImagePath());
            updatePreviewImage(item.getImagePath());
        }
    }

    // Збереження даних

    /**
     * Зберігає введені дані у об'єкт квітки.
     *
     * @return true, якщо збереження успішне, інакше false
     */
    @Override
    protected boolean saveItem() {
        try {
            FlowerType type = (FlowerType) typeCombo.getSelectedItem();
            String priceStr = priceField.getText().replace(',', '.').trim();
            int freshness = freshnessSlider.getValue();
            String stemStr = stemLengthField.getText().trim();
            String color = colorField.getText().trim();
            String country = countryField.getText().trim();
            boolean isPotted = pottedCheckBox.isSelected();
            String stockStr = stockQuantityField.getText().trim();
            String imagePath = imagePathField.getText().trim();

            // Валідація полів
            if (type == null) {
                showErrorDialog("Необхідно вибрати тип квітки.");
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
            if (stemStr.isEmpty()) {
                showErrorDialog("Довжина стебла не може бути порожньою.");
                stemLengthField.requestFocusInWindow();
                return false;
            }
            int stemLength = Integer.parseInt(stemStr);
            if (stemLength <= 0) {
                showErrorDialog("Довжина стебла повинна бути більшою за нуль.");
                stemLengthField.requestFocusInWindow();
                return false;
            }
            if (color.isEmpty()) {
                showErrorDialog("Колір квітки не може бути порожнім.");
                colorField.requestFocusInWindow();
                return false;
            }
            if (country.isEmpty()) {
                showErrorDialog("Країна походження не може бути порожньою.");
                countryField.requestFocusInWindow();
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
                item = new Flower(type, price, freshness, stemLength, color, country, isPotted, imagePath, stockQuantity);
            } else {
                item.setType(type);
                item.setPrice(price);
                item.setFreshness(freshness);
                item.setStemLength(stemLength);
                item.setColor(color);
                item.setCountryOfOrigin(country);
                item.setPotted(isPotted);
                item.setStockQuantity(stockQuantity);
                item.setImagePath(imagePath);
            }
            logger.info("Квітка успішно збережена: {}", item.getType().getDisplayName());
            return true;
        } catch (NumberFormatException ex) {
            showErrorDialog("Будь ласка, введіть коректні числові значення для ціни, довжини стебла та кількості.");
            logger.error("Помилка формату чисел: {}", ex.getMessage());
            return false;
        } catch (Exception ex) {
            showErrorDialog("Сталася помилка: " + ex.getMessage());
            logger.error("Помилка збереження квітки: {}", ex.getMessage());
            return false;
        }
    }

    /**
     * Повертає об'єкт квітки.
     *
     * @return збережена або редагована квітка
     */
    public Flower getFlower() {
        return item;
    }

    // Рендерери

    /**
     * Рендерер для відображення типів квіток у випадаючому списку.
     */
    public static class FlowerTypeRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value instanceof FlowerType) {
                label.setText(((FlowerType) value).getDisplayName());
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

    /**
     * Рендерер для відображення рівнів свіжості у випадаючому списку.
     */
    public static class FreshRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value instanceof FreshnessLevel) {
                label.setText(((FreshnessLevel) value).getDescription());
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