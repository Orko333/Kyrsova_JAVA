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
 * Успадковує функціонал від AbstractAddEditDialog.
 */
public class AddEditFlowerDialog extends AbstractAddEditDialog<Flower> {

    private static final Logger logger = LogManager.getLogger(AddEditFlowerDialog.class);

    // Поля форми, специфічні для квітки
    private JComboBox<FlowerType> typeCombo;
    private JTextField priceField;
    private JSlider freshnessSlider;
    private JLabel freshnessValueLabel;
    private JTextField stemLengthField;
    private JTextField colorField;
    private JTextField countryField;
    private JCheckBox pottedCheckBox;
    private JTextField stockQuantityField;
    // imagePathField, browseButton, okButton, cancelButton, previewImageLabel - успадковані

    public AddEditFlowerDialog(JFrame parent, Flower flowerToEdit) {
        super(parent, flowerToEdit == null ? "Додати квітку" : "Редагувати квітку", flowerToEdit);
        logger.info("Ініціалізація AddEditFlowerDialog для {}", flowerToEdit == null ? "нової квітки" : "редагування квітки ID: " + (flowerToEdit != null ? flowerToEdit.getId() : "N/A"));

        if (this.item != null) {
            logger.debug("Заповнення полів для редагованої квітки.");
            populateFields();
        }
        pack();
        setMinimumSize(new Dimension(550, 620));
        setLocationRelativeTo(parent);
        setResizable(true);
        logger.debug("Розміри та позиція AddEditFlowerDialog встановлені.");
        SwingUtilities.invokeLater(() -> {
            typeCombo.requestFocusInWindow();
            logger.trace("Фокус встановлено на typeCombo.");
        });
    }

    @Override
    protected JPanel createFormPanel() {
        logger.debug("Створення панелі форми для AddEditFlowerDialog.");
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

        typeCombo = new JComboBox<>(FlowerType.values());
        typeCombo.setRenderer(new FlowerTypeRenderer());
        typeCombo.setFont(DEFAULT_FONT);
        typeCombo.setBackground(Color.WHITE);
        addFormField(formPanel, "Тип квітки:", typeCombo, gbc, 0, 1, GridBagConstraints.HORIZONTAL);

        priceField = createStyledTextField(10);
        addFormField(formPanel, "Ціна (грн):", priceField, gbc, 1, 1, GridBagConstraints.HORIZONTAL);

        JPanel freshnessPanel = new JPanel(new BorderLayout(5,0));
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


        stemLengthField = createStyledTextField(10);
        addFormField(formPanel, "Довжина стебла (см):", stemLengthField, gbc, 3, 1, GridBagConstraints.HORIZONTAL);

        colorField = createStyledTextField(15);
        // setupAutoComplete(colorField, COLORS_SUGGESTIONS); // Видалено автодоповнення
        addFormField(formPanel, "Колір:", colorField, gbc, 4, 1, GridBagConstraints.HORIZONTAL);

        countryField = createStyledTextField(15);
        // setupAutoComplete(countryField, COUNTRIES_SUGGESTIONS); // Видалено автодоповнення
        addFormField(formPanel, "Країна походження:", countryField, gbc, 5, 1, GridBagConstraints.HORIZONTAL);

        stockQuantityField = createStyledTextField(10);
        addFormField(formPanel, "Кількість на складі:", stockQuantityField, gbc, 6, 1, GridBagConstraints.HORIZONTAL);

        pottedCheckBox = new JCheckBox("В горщику");
        pottedCheckBox.setFont(DEFAULT_FONT);
        pottedCheckBox.setOpaque(false);
        gbc.gridx = 0; gbc.gridy = 7; gbc.gridwidth = 2; gbc.anchor = GridBagConstraints.CENTER;
        formPanel.add(pottedCheckBox, gbc);
        gbc.anchor = GridBagConstraints.WEST; gbc.gridwidth = 1;

        JPanel imagePanel = new JPanel(new BorderLayout(0, 5));
        imagePanel.setOpaque(false);
        imagePanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(PRIMARY_COLOR.darker()), "Зображення"));

        imagePathField = createStyledTextField(20);
        imagePathField.setEditable(false);
        imagePathField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { updatePreviewImage(imagePathField.getText()); }
            public void removeUpdate(DocumentEvent e) { updatePreviewImage(imagePathField.getText()); }
            public void changedUpdate(DocumentEvent e) { updatePreviewImage(imagePathField.getText()); }
        });

        browseButton = createStyledButton("Огляд...", null);
        browseButton.addActionListener(e -> browseImageAction());

        JPanel imagePathControlsPanel = new JPanel(new BorderLayout(5,0));
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

        gbc.gridx = 0; gbc.gridy = 8; gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.BOTH; gbc.weighty = 0.4;
        formPanel.add(imagePanel, gbc);

        setupEnterNavigation(typeCombo, priceField, stemLengthField, colorField, countryField, stockQuantityField, pottedCheckBox);
        logger.info("Панель форми для AddEditFlowerDialog успішно створена.");
        return formPanel;
    }

    private void updateFreshnessLabelColor() {
        int value = freshnessSlider.getValue();
        FreshnessLevel level = FreshnessLevel.fromValue(value);
        logger.trace("Оновлення кольору мітки свіжості. Значення: {}, Рівень: {}", value, level);
        switch (level) {
            case VERY_LOW:
            case LOW:
                freshnessValueLabel.setForeground(Color.RED.darker());
                break;
            case MEDIUM:
                freshnessValueLabel.setForeground(Color.ORANGE.darker());
                break;
            case HIGH:
            case VERY_HIGH:
                freshnessValueLabel.setForeground(PRIMARY_COLOR.darker());
                break;
        }
    }


    @Override
    protected void populateFields() {
        logger.debug("Заповнення полів форми даними квітки.");
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
            updatePreviewImage(item.getImagePath()); // Важливо для показу прев'ю при редагуванні
            logger.info("Поля форми заповнені для квітки типу: {}, колір: {}", item.getType(), item.getColor());
        } else {
            logger.warn("Спроба заповнити поля, але 'item' (квітка) є null.");
        }
    }

    @Override
    protected boolean saveItem() {
        logger.debug("Спроба зберегти квітку.");
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

            logger.trace("Дані з форми: Тип='{}', Ціна='{}', Свіжість='{}', Стебло='{}', Колір='{}', Країна='{}', В горщику='{}', К-сть='{}', Зображення='{}'",
                    type, priceStr, freshness, stemStr, color, country, isPotted, stockStr, imagePath);

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
                logger.warn("Файл зображення за шляхом '{}' не знайдено або це не файл.", imagePath);
                if (JOptionPane.showConfirmDialog(this,
                        "Файл зображення за вказаним шляхом не знайдено або це не файл.\nПродовжити збереження без зображення (або з поточним, якщо є)?",
                        "Попередження: Зображення",
                        JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.NO_OPTION) {
                    imagePathField.requestFocusInWindow();
                    return false;
                }
            }

            if (item == null) {
                logger.info("Створення нової квітки.");
                item = new Flower(type, price, freshness, stemLength, color, country, isPotted, imagePath, stockQuantity);
            } else {
                logger.info("Оновлення існуючої квітки ID: {}", item.getId());
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
            logger.info("Квітка '{} {}' успішно підготовлена до збереження.", item.getColor(), item.getType().getDisplayName());
            return true;
        } catch (NumberFormatException ex) {
            logger.error("Помилка формату числа при збереженні квітки: {}", ex.getMessage(), ex);
            showErrorDialog("Будь ласка, введіть коректні числові значення для ціни, довжини стебла та кількості.");
            return false;
        } catch (Exception ex) {
            logger.error("Непередбачена помилка при збереженні квітки: {}", ex.getMessage(), ex);
            showErrorDialog("Сталася помилка: " + ex.getMessage());
            return false;
        }
    }

    public Flower getFlower() {
        logger.debug("Запит на отримання об'єкта Flower. Поточний item: {} {}",
                item != null ? item.getColor() : "null",
                item != null ? item.getType().getDisplayName() : "");
        return item;
    }

    public static class FlowerTypeRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value,
                                                      int index, boolean isSelected, boolean cellHasFocus) {
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
    public static class FreshRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value,
                                                      int index, boolean isSelected, boolean cellHasFocus) {
            JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value instanceof Flower.FreshnessLevel) {
                label.setText(((Flower.FreshnessLevel) value).getDescription());
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