package flowershop.gui.Tab;

import flowershop.dao.AccessoryDAO;
import flowershop.models.Accessory;
import flowershop.models.Accessory.AccessoryType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static flowershop.gui.Dialog.AbstractAddEditDialog.TEXT_COLOR;

/**
 * Вкладка для керування аксесуарами у квітковому магазині.
 * Надає інтерфейс для перегляду, фільтрації, сортування, додавання, редагування та видалення аксесуарів.
 * Успадковується від {@link AbstractItemTab}, що забезпечує базову функціональність для роботи з таблицями.
 */
public class AccessoriesTab extends AbstractItemTab<Accessory, AccessoryDAO> {

    private static final Logger logger = LogManager.getLogger(AccessoriesTab.class);

    private JComboBox<AccessoryType> typeFilterCombo;
    private JSpinner minPriceSpinner, maxPriceSpinner;
    private JSpinner minStockSpinner, maxStockSpinner;

    /**
     * Конструктор вкладки для керування аксесуарами.
     * Ініціалізує вкладку з DAO для роботи з аксесуарами.
     */
    public AccessoriesTab() {
        super(new AccessoryDAO());
        logger.info("Вкладка 'Управління аксесуарами' ініціалізована.");
    }

    // Конфігурація вкладки

    /**
     * Повертає заголовок вкладки.
     *
     * @return рядок з назвою вкладки
     */
    @Override
    protected String getTabTitle() {
        return "Управління аксесуарами";
    }

    /**
     * Повертає назви стовпців таблиці аксесуарів.
     *
     * @return масив рядків із назвами стовпців
     */
    @Override
    protected String[] getColumnNames() {
        return new String[]{"ID", "Назва", "Тип", "Колір", "Розмір", "Ціна", "На складі"};
    }

    /**
     * Повертає класи даних для стовпців таблиці.
     *
     * @return масив класів, що відповідають типам даних стовпців
     */
    @Override
    protected Class<?>[] getColumnClasses() {
        return new Class<?>[]{Integer.class, String.class, String.class, String.class, String.class, Double.class, Integer.class};
    }

    /**
     * Формує дані для рядка таблиці на основі об'єкта аксесуара.
     *
     * @param accessory об'єкт аксесуара
     * @return масив об'єктів із даними для відображення в таблиці
     */
    @Override
    protected Object[] getRowDataForItem(Accessory accessory) {
        return new Object[]{
                accessory.getId(),
                accessory.getName(),
                accessory.getType().getDisplayName(),
                accessory.getColor(),
                accessory.getSize(),
                accessory.getPrice(),
                accessory.getStockQuantity()
        };
    }

    // Налаштування таблиці

    /**
     * Налаштовує ширину стовпців таблиці та вирівнювання вмісту.
     * Викликає батьківський метод для базового налаштування, а потім застосовує специфічні значення ширини та рендерери.
     */
    @Override
    protected void configureTableColumnWidths() {
        super.configureTableColumnWidths();
        logger.debug("Налаштування ширини стовпців для таблиці аксесуарів.");
        itemsTable.getColumnModel().getColumn(0).setPreferredWidth(40);
        itemsTable.getColumnModel().getColumn(1).setPreferredWidth(180);
        itemsTable.getColumnModel().getColumn(2).setPreferredWidth(120);
        itemsTable.getColumnModel().getColumn(3).setPreferredWidth(80);
        itemsTable.getColumnModel().getColumn(4).setPreferredWidth(80);
        itemsTable.getColumnModel().getColumn(5).setPreferredWidth(80);
        itemsTable.getColumnModel().getColumn(6).setPreferredWidth(80);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        itemsTable.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        itemsTable.getColumnModel().getColumn(6).setCellRenderer(centerRenderer);

        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(JLabel.RIGHT);
        itemsTable.getColumnModel().getColumn(5).setCellRenderer(rightRenderer);
    }

    // Панель фільтрів

    /**
     * Створює панель фільтрів для пошуку та фільтрації аксесуарів.
     *
     * @return панель із компонентами фільтрів
     */
    @Override
    protected JPanel createFilterPanel() {
        logger.debug("Створення панелі фільтрів для аксесуарів.");
        JPanel filterPanel = new JPanel();
        filterPanel.setLayout(new BoxLayout(filterPanel, BoxLayout.Y_AXIS));
        filterPanel.setBackground(Color.WHITE);
        filterPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(PRIMARY_COLOR, 1),
                        "Фільтри аксесуарів",
                        TitledBorder.LEFT,
                        TitledBorder.TOP,
                        TITLE_FONT,
                        PRIMARY_COLOR),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        JPanel searchRow = createSearchRow();
        JPanel rangeRow = createRangeRow();
        filterPanel.add(searchRow);
        filterPanel.add(rangeRow);
        return filterPanel;
    }

    /**
     * Створює рядок панелі фільтрів із полем пошуку та фільтром типу.
     *
     * @return панель із компонентами пошуку
     */
    private JPanel createSearchRow() {
        JPanel searchRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        searchRow.setOpaque(false);

        searchRow.add(new JLabel("Пошук:"));
        searchField = new JTextField(15);
        searchField.setPreferredSize(new Dimension(180, 28));
        searchField.getDocument().addDocumentListener(new SimpleDocumentListener(this::applyFiltersAndRefresh));
        searchRow.add(searchField);

        searchRow.add(new JLabel("Тип:"));
        typeFilterCombo = new JComboBox<>(AccessoryType.values());
        typeFilterCombo.setRenderer(new AccessoryTypeRenderer());
        typeFilterCombo.insertItemAt(null, 0);
        typeFilterCombo.setSelectedIndex(0);
        typeFilterCombo.setFont(REGULAR_FONT);
        typeFilterCombo.setPreferredSize(new Dimension(150, 28));
        typeFilterCombo.addActionListener(e -> applyFiltersAndRefresh());
        searchRow.add(typeFilterCombo);

        return searchRow;
    }

    /**
     * Створює рядок панелі фільтрів із фільтрами ціни, кількості на складі та кнопкою очищення.
     *
     * @return панель із компонентами фільтрів діапазону
     */
    private JPanel createRangeRow() {
        JPanel rangeRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        rangeRow.setOpaque(false);

        JPanel pricePanel = createRangeFilterPanel("Ціна", 0, 10000, 10, 0, 1000);
        minPriceSpinner = (JSpinner) ((JPanel) pricePanel.getComponent(0)).getComponent(1);
        maxPriceSpinner = (JSpinner) ((JPanel) pricePanel.getComponent(0)).getComponent(3);
        minPriceSpinner.addChangeListener(e -> applyFiltersAndRefresh());
        maxPriceSpinner.addChangeListener(e -> applyFiltersAndRefresh());

        JPanel stockPanel = createRangeFilterPanel("Кількість на складі", 0, 1000, 1, 0, 100);
        minStockSpinner = (JSpinner) ((JPanel) stockPanel.getComponent(0)).getComponent(1);
        maxStockSpinner = (JSpinner) ((JPanel) stockPanel.getComponent(0)).getComponent(3);
        minStockSpinner.addChangeListener(e -> applyFiltersAndRefresh());
        maxStockSpinner.addChangeListener(e -> applyFiltersAndRefresh());

        clearFiltersButton = createStyledButton("Очистити", "FileChooser.deleteIcon");
        clearFiltersButton.addActionListener(e -> clearFilters());

        rangeRow.add(pricePanel);
        rangeRow.add(stockPanel);
        rangeRow.add(clearFiltersButton);
        return rangeRow;
    }

    /**
     * Створює панель для фільтрації за діапазоном значень (наприклад, ціна або кількість на складі).
     *
     * @param title      заголовок панелі
     * @param minVal     мінімальне значення діапазону
     * @param maxVal     максимальне значення діапазону
     * @param step       крок зміни значення
     * @param initialMin початкове мінімальне значення
     * @param initialMax початкове максимальне значення
     * @return панель із компонентами для вибору діапазону
     */
    private JPanel createRangeFilterPanel(String title, int minVal, int maxVal, int step, int initialMin, int initialMax) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(SECONDARY_COLOR, 1),
                title,
                TitledBorder.LEFT,
                TitledBorder.TOP,
                REGULAR_FONT,
                PRIMARY_COLOR
        ));

        JPanel spinnersPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        spinnersPanel.setOpaque(false);

        JLabel minLabel = new JLabel("Від:");
        minLabel.setFont(REGULAR_FONT);
        JSpinner minSpinner = new JSpinner(new SpinnerNumberModel(initialMin, minVal, maxVal, step));
        minSpinner.setPreferredSize(new Dimension(70, 28));
        minSpinner.setFont(REGULAR_FONT);

        JLabel maxLabel = new JLabel("до:");
        maxLabel.setFont(REGULAR_FONT);
        JSpinner maxSpinner = new JSpinner(new SpinnerNumberModel(initialMax, minVal, maxVal, step));
        maxSpinner.setPreferredSize(new Dimension(70, 28));
        maxSpinner.setFont(REGULAR_FONT);

        spinnersPanel.add(minLabel);
        spinnersPanel.add(minSpinner);
        spinnersPanel.add(maxLabel);
        spinnersPanel.add(maxSpinner);
        panel.add(spinnersPanel, BorderLayout.CENTER);
        return panel;
    }

    // Фільтрація та сортування

    /**
     * Фільтрує список аксесуарів на основі заданих критеріїв (пошук, тип, ціна, кількість на складі).
     *
     * @param allAccessories повний список аксесуарів
     * @return відфільтрований список аксесуарів
     */
    @Override
    protected List<Accessory> filterItems(List<Accessory> allAccessories) {
        String searchText = searchField.getText().toLowerCase().trim();
        AccessoryType selectedType = (AccessoryType) typeFilterCombo.getSelectedItem();
        double minPrice = ((Number) minPriceSpinner.getValue()).doubleValue();
        double maxPrice = ((Number) maxPriceSpinner.getValue()).doubleValue();
        int minStock = ((Number) minStockSpinner.getValue()).intValue();
        int maxStock = ((Number) maxStockSpinner.getValue()).intValue();

        logger.debug("Фільтрація аксесуарів. Текст: '{}', Тип: {}, Ціна: {}-{}, К-сть: {}-{}", searchText, selectedType, minPrice, maxPrice, minStock, maxStock);

        if (allAccessories == null) {
            logger.warn("Список всіх аксесуарів для фільтрації є null.");
            return new ArrayList<>();
        }

        return allAccessories.stream()
                .filter(acc -> (searchText.isEmpty() ||
                        acc.getName().toLowerCase().contains(searchText) ||
                        acc.getColor().toLowerCase().contains(searchText) ||
                        acc.getSize().toLowerCase().contains(searchText) ||
                        acc.getType().getDisplayName().toLowerCase().contains(searchText)) &&
                        (selectedType == null || acc.getType() == selectedType) &&
                        (acc.getPrice() >= minPrice && acc.getPrice() <= maxPrice) &&
                        (acc.getStockQuantity() >= minStock && acc.getStockQuantity() <= maxStock))
                .collect(Collectors.toList());
    }

    /**
     * Очищає всі фільтри та оновлює таблицю.
     */
    @Override
    protected void clearFilters() {
        logger.info("Очищення фільтрів для аксесуарів.");
        searchField.setText("");
        typeFilterCombo.setSelectedIndex(0);
        minPriceSpinner.setValue(0);
        maxPriceSpinner.setValue(1000);
        minStockSpinner.setValue(0);
        maxStockSpinner.setValue(100);
        applyFiltersAndRefresh();
    }

    /**
     * Створює стилізовану радіокнопку для діалогу сортування.
     *
     * @param text           текст радіокнопки
     * @param uiManagerIconKey ключ іконки з UIManager
     * @return стилізована радіокнопка
     */
    JRadioButton createStyledRadioButton(String text, String uiManagerIconKey) {
        JRadioButton radioButton = new JRadioButton(text);
        radioButton.setFont(REGULAR_FONT);
        radioButton.setBackground(Color.WHITE);
        radioButton.setFocusPainted(false);
        if (uiManagerIconKey != null && !uiManagerIconKey.isEmpty()) {
            Icon icon = UIManager.getIcon(uiManagerIconKey);
            if (icon != null) radioButton.setIcon(icon);
            else logger.warn("Стандартна іконка не знайдена для радіокнопки з ключем: {}", uiManagerIconKey);
        }
        return radioButton;
    }

    // Операції з даними

    /**
     * Отримує список усіх аксесуарів із DAO.
     *
     * @return список усіх аксесуарів
     */
    @Override
    protected List<Accessory> getAllItemsFromDAO() {
        return itemDAO.getAllAccessories();
    }

    /**
     * Отримує аксесуар за його ідентифікатором із DAO.
     *
     * @param id ідентифікатор аксесуара
     * @return об'єкт аксесуара або null, якщо не знайдено
     */
    @Override
    protected Accessory getItemByIdFromDAO(int id) {
        return itemDAO.getAccessoryById(id);
    }

    /**
     * Відкриває діалог для додавання або редагування аксесуара.
     *
     * @param e подія, що викликала діалог (може бути null)
     */
    @Override
    protected void showAddEditDialog(ActionEvent e) {
        boolean isEditMode = (e != null && e.getSource() == editButton) || (e == null && itemsTable.getSelectedRow() >= 0);
        Accessory accessoryToEdit = null;

        if (isEditMode) {
            int selectedRow = itemsTable.getSelectedRow();
            if (selectedRow < 0) {
                JOptionPane.showMessageDialog(this, "Будь ласка, оберіть аксесуар для редагування.", "Попередження", JOptionPane.WARNING_MESSAGE);
                return;
            }
            accessoryToEdit = getItemByModelRow(itemsTable.convertRowIndexToModel(selectedRow));
        }

        flowershop.gui.Dialog.AddEditAccessoryDialog dialog = new flowershop.gui.Dialog.AddEditAccessoryDialog(
                (JFrame) SwingUtilities.getWindowAncestor(this), accessoryToEdit);
        dialog.setVisible(true);

        if (dialog.isSaved()) {
            Accessory resultAccessory = dialog.getAccessory();
            itemDAO.saveAccessory(resultAccessory);
            refreshTableData();
            selectRowByAccessoryId(resultAccessory.getId());
        }
    }

    /**
     * Вибирає рядок у таблиці за ідентифікатором аксесуара.
     *
     * @param accessoryId ідентифікатор аксесуара
     */
    private void selectRowByAccessoryId(int accessoryId) {
        for (int i = 0; i < itemsTable.getRowCount(); i++) {
            int modelRow = itemsTable.convertRowIndexToModel(i);
            if (modelRow >= 0 && modelRow < tableModel.getRowCount()) {
                Accessory itemInRow = getItemByModelRow(modelRow);
                if (itemInRow != null && itemInRow.getId() == accessoryId) {
                    itemsTable.setRowSelectionInterval(i, i);
                    itemsTable.scrollRectToVisible(itemsTable.getCellRect(i, 0, true));
                    break;
                }
            }
        }
    }

    /**
     * Видаляє вибраний аксесуар з таблиці та бази даних.
     *
     * @param e подія, що викликала видалення
     */
    @Override
    protected void deleteSelectedItem(ActionEvent e) {
        int selectedRow = itemsTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Будь ласка, оберіть аксесуар для видалення.", "Попередження", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Accessory accessoryToDelete = getItemByModelRow(itemsTable.convertRowIndexToModel(selectedRow));
        if (accessoryToDelete == null) {
            logger.error("Не вдалося отримати аксесуар для видалення за вибраним рядком.");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Ви впевнені, що хочете видалити аксесуар \"" + accessoryToDelete.getName() + "\"?",
                "Підтвердження видалення",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            itemDAO.deleteAccessory(accessoryToDelete.getId());
            refreshTableData();
            clearDetailsPanel();
            JOptionPane.showMessageDialog(this, "Аксесуар \"" + accessoryToDelete.getName() + "\" успішно видалено.", "Видалення завершено", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    // Деталі та відображення

    /**
     * Повертає заголовок панелі деталей.
     *
     * @return рядок із назвою панелі деталей
     */
    @Override
    protected String getDetailsPanelTitle() {
        return "Деталі аксесуара";
    }

    /**
     * Повертає детальну інформацію про аксесуар для відображення у форматі HTML.
     *
     * @param accessory об'єкт аксесуара
     * @return рядок із детальною інформацією у форматі HTML або порожній рядок, якщо аксесуар null
     */
    @Override
    protected String getDetailedInfoForItem(Accessory accessory) {
        if (accessory == null) {
            return "";
        }

        StringBuilder html = new StringBuilder("<html><body style='font-family: Segoe UI, Arial, sans-serif; font-size: 10pt; color: #333;'>");
        html.append("<h2 style='color: ").append(colorToHex(PRIMARY_COLOR)).append("; margin-top: 0;'>").append(accessory.getName()).append("</h2>");
        html.append("<table width='100%' style='border-collapse: collapse;'>");
        html.append(String.format("<tr><td style='%s'><b>ID:</b></td><td>%d</td></tr>", getTdStyle(), accessory.getId()));
        html.append(String.format("<tr><td style='%s'><b>Тип:</b></td><td>%s</td></tr>", getTdStyle(), accessory.getType().getDisplayName()));
        html.append(String.format("<tr><td style='%s'><b>Колір:</b></td><td>%s</td></tr>", getTdStyle(), accessory.getColor()));
        html.append(String.format("<tr><td style='%s'><b>Розмір:</b></td><td>%s</td></tr>", getTdStyle(), accessory.getSize()));
        html.append(String.format("<tr><td style='%s'><b>Ціна:</b></td><td>%s грн</td></tr>", getTdStyle(), priceFormat.format(accessory.getPrice())));
        html.append(String.format("<tr><td style='%s'><b>На складі:</b></td><td>%d шт.</td></tr>", getTdStyle(), accessory.getStockQuantity()));
        if (accessory.getDescription() != null && !accessory.getDescription().isEmpty()) {
            html.append(String.format("<tr><td style='%s'><b>Опис:</b></td><td>%s</td></tr>", getTdStyle(), accessory.getDescription()));
        }
        html.append("</table></body></html>");
        return html.toString();
    }

    /**
     * Повертає стиль для комірок таблиці деталей у форматі CSS.
     *
     * @return рядок зі стилями CSS
     */
    private String getTdStyle() {
        return "padding: 4px; border-bottom: 1px solid #eee; vertical-align: top;";
    }

    /**
     * Повертає шлях до зображення аксесуара.
     *
     * @param accessory об'єкт аксесуара
     * @return шлях до зображення або null, якщо аксесуар null
     */
    @Override
    protected String getImagePathForItem(Accessory accessory) {
        return accessory != null ? accessory.getImagePath() : null;
    }

    /**
     * Вказує, чи використовується панель рівня запасів.
     *
     * @return true, оскільки аксесуари мають панель рівня запасів
     */
    @Override
    protected boolean hasStockLevelBar() {
        return true;
    }

    /**
     * Оновлює панель рівня запасів для відображення кількості аксесуара на складі.
     *
     * @param accessory об'єкт аксесуара
     */
    @Override
    protected void updateStockLevelBar(Accessory accessory) {
        if (accessory == null || stockLevelBar == null) {
            return;
        }

        int stockQty = accessory.getStockQuantity();
        int maxStockDisplay = 100;
        stockLevelBar.setMaximum(maxStockDisplay);
        stockLevelBar.setValue(Math.min(stockQty, maxStockDisplay));
        stockLevelBar.setString(stockQty + " шт.");

        if (stockQty < 10) {
            stockLevelBar.setForeground(new Color(255, 69, 0));
        } else if (stockQty < 30) {
            stockLevelBar.setForeground(new Color(255, 165, 0));
        } else {
            stockLevelBar.setForeground(PRIMARY_COLOR);
        }
    }

    /**
     * Повертає назву елемента в однині.
     *
     * @return рядок із назвою елемента
     */
    @Override
    protected String getItemNameSingular() {
        return "аксесуар";
    }

    /**
     * Рендерер для відображення типів аксесуарів у випадаючому списку.
     */
    private static class AccessoryTypeRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value instanceof AccessoryType) {
                label.setText(((AccessoryType) value).getDisplayName());
            } else {
                label.setText("Будь-який тип");
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