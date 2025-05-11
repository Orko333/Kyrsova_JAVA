package flowershop.gui.Tab;

import flowershop.dao.FlowerDAO;
import flowershop.gui.Dialog.AddEditFlowerDialog;
import flowershop.models.Flower;
import flowershop.models.Flower.FlowerType;
import flowershop.models.Flower.FreshnessLevel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Вкладка для керування квітами у квітковому магазині.
 * Надає інтерфейс для перегляду, фільтрації, сортування, додавання, редагування та видалення квітів.
 * Успадковується від {@link AbstractItemTab}, що забезпечує базову функціональність для роботи з таблицями.
 */
public class FlowersTab extends AbstractItemTab<Flower, FlowerDAO> {

    private static final Logger logger = LogManager.getLogger(FlowersTab.class);

    private JComboBox<FlowerType> typeFilterCombo;
    private JComboBox<FreshnessLevel> freshnessFilterCombo;
    private JSpinner minPriceSpinner, maxPriceSpinner;
    private JSpinner minStemSpinner, maxStemSpinner;
    private JCheckBox pottedCheckBox;

    /**
     * Конструктор вкладки для керування квітами.
     * Ініціалізує вкладку з DAO для роботи з квітами та налаштовує базовий інтерфейс.
     */
    public FlowersTab() {
        super(new FlowerDAO());
        logger.info("Вкладка 'Управління квітами' ініціалізована.");
    }

    // Конфігурація вкладки

    /**
     * Повертає заголовок вкладки.
     *
     * @return рядок з назвою вкладки
     */
    @Override
    protected String getTabTitle() {
        return "Управління квітами";
    }

    /**
     * Повертає назви стовпців таблиці квітів.
     *
     * @return масив рядків із назвами стовпців
     */
    @Override
    protected String[] getColumnNames() {
        return new String[]{"ID", "Назва", "Колір", "Ціна", "Свіжість", "Стебло(см)", "Країна", "В горщику", "На складі"};
    }

    /**
     * Повертає класи даних для стовпців таблиці.
     *
     * @return масив класів, що відповідають типам даних стовпців
     */
    @Override
    protected Class<?>[] getColumnClasses() {
        return new Class<?>[]{Integer.class, String.class, String.class, Double.class, String.class, Integer.class, String.class, String.class, Integer.class};
    }

    /**
     * Формує дані для рядка таблиці на основі об'єкта квітки.
     *
     * @param flower об'єкт квітки
     * @return масив об'єктів із даними для відображення в таблиці
     */
    @Override
    protected Object[] getRowDataForItem(Flower flower) {
        return new Object[]{
                flower.getId(),
                flower.getDisplayName(),
                flower.getColor(),
                flower.getPrice(),
                flower.getFreshnessLevel().getDescription() + " (" + flower.getFreshness() + "%)",
                flower.getStemLength(),
                flower.getCountryOfOrigin(),
                flower.isPotted() ? "Так" : "Ні",
                flower.getStockQuantity()
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
        logger.debug("Налаштування ширини стовпців для таблиці квітів.");
        itemsTable.getColumnModel().getColumn(0).setPreferredWidth(30);
        itemsTable.getColumnModel().getColumn(1).setPreferredWidth(120);
        itemsTable.getColumnModel().getColumn(2).setPreferredWidth(80);
        itemsTable.getColumnModel().getColumn(3).setPreferredWidth(70);
        itemsTable.getColumnModel().getColumn(4).setPreferredWidth(150);
        itemsTable.getColumnModel().getColumn(5).setPreferredWidth(80);
        itemsTable.getColumnModel().getColumn(6).setPreferredWidth(100);
        itemsTable.getColumnModel().getColumn(7).setPreferredWidth(80);
        itemsTable.getColumnModel().getColumn(8).setPreferredWidth(70);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        itemsTable.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        itemsTable.getColumnModel().getColumn(5).setCellRenderer(centerRenderer);
        itemsTable.getColumnModel().getColumn(7).setCellRenderer(centerRenderer);
        itemsTable.getColumnModel().getColumn(8).setCellRenderer(centerRenderer);

        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(JLabel.RIGHT);
        itemsTable.getColumnModel().getColumn(3).setCellRenderer(rightRenderer);
    }

    // Панель фільтрів

    /**
     * Створює панель фільтрів для пошуку та фільтрації квітів.
     *
     * @return панель із компонентами фільтрів
     */
    @Override
    protected JPanel createFilterPanel() {
        logger.debug("Створення панелі фільтрів для квітів.");
        JPanel filterPanel = new JPanel();
        filterPanel.setLayout(new BoxLayout(filterPanel, BoxLayout.Y_AXIS));
        filterPanel.setBackground(Color.WHITE);
        filterPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(PRIMARY_COLOR, 1),
                        "Фільтри квітів",
                        TitledBorder.LEFT,
                        TitledBorder.TOP,
                        TITLE_FONT,
                        PRIMARY_COLOR),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        JPanel row1 = createFirstFilterRow();
        JPanel row2 = createSecondFilterRow();
        filterPanel.add(row1);
        filterPanel.add(row2);
        return filterPanel;
    }

    /**
     * Створює перший рядок панелі фільтрів із полем пошуку, фільтрами типу та свіжості.
     *
     * @return панель із компонентами першого рядка фільтрів
     */
    private JPanel createFirstFilterRow() {
        JPanel row1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        row1.setOpaque(false);

        row1.add(new JLabel("Пошук:"));
        searchField = new JTextField(15);
        searchField.setPreferredSize(new Dimension(150, 28));
        searchField.getDocument().addDocumentListener(new SimpleDocumentListener(this::applyFiltersAndRefresh));
        row1.add(searchField);

        row1.add(new JLabel("Тип:"));
        typeFilterCombo = new JComboBox<>(FlowerType.values());
        typeFilterCombo.setRenderer(new AddEditFlowerDialog.FlowerTypeRenderer());
        typeFilterCombo.insertItemAt(null, 0);
        typeFilterCombo.setSelectedIndex(0);
        typeFilterCombo.setFont(REGULAR_FONT);
        typeFilterCombo.setPreferredSize(new Dimension(130, 28));
        typeFilterCombo.addActionListener(e -> applyFiltersAndRefresh());
        row1.add(typeFilterCombo);

        row1.add(new JLabel("Свіжість:"));
        freshnessFilterCombo = new JComboBox<>(FreshnessLevel.values());
        freshnessFilterCombo.setRenderer(new AddEditFlowerDialog.FreshRenderer());
        freshnessFilterCombo.insertItemAt(null, 0);
        freshnessFilterCombo.setSelectedIndex(0);
        freshnessFilterCombo.setFont(REGULAR_FONT);
        freshnessFilterCombo.setPreferredSize(new Dimension(140, 28));
        freshnessFilterCombo.addActionListener(e -> applyFiltersAndRefresh());
        row1.add(freshnessFilterCombo);

        return row1;
    }

    /**
     * Створює другий рядок панелі фільтрів із фільтрами ціни, довжини стебла, горщикових квітів та кнопкою очищення.
     *
     * @return панель із компонентами другого рядка фільтрів
     */
    private JPanel createSecondFilterRow() {
        JPanel row2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        row2.setOpaque(false);

        JPanel pricePanel = createRangeFilterPanel("Ціна", 0, 5000, 10, 0, 500);
        minPriceSpinner = (JSpinner) ((JPanel) pricePanel.getComponent(0)).getComponent(1);
        maxPriceSpinner = (JSpinner) ((JPanel) pricePanel.getComponent(0)).getComponent(3);
        minPriceSpinner.addChangeListener(e -> applyFiltersAndRefresh());
        maxPriceSpinner.addChangeListener(e -> applyFiltersAndRefresh());

        JPanel stemPanel = createRangeFilterPanel("Стебло (см)", 0, 200, 5, 0, 100);
        minStemSpinner = (JSpinner) ((JPanel) stemPanel.getComponent(0)).getComponent(1);
        maxStemSpinner = (JSpinner) ((JPanel) stemPanel.getComponent(0)).getComponent(3);
        minStemSpinner.addChangeListener(e -> applyFiltersAndRefresh());
        maxStemSpinner.addChangeListener(e -> applyFiltersAndRefresh());

        pottedCheckBox = new JCheckBox("В горщику");
        pottedCheckBox.setFont(REGULAR_FONT);
        pottedCheckBox.setOpaque(false);
        pottedCheckBox.addActionListener(e -> applyFiltersAndRefresh());

        clearFiltersButton = createStyledButton("Очистити", "FileChooser.deleteIcon");
        clearFiltersButton.addActionListener(e -> clearFilters());

        row2.add(pricePanel);
        row2.add(stemPanel);
        row2.add(pottedCheckBox);
        row2.add(clearFiltersButton);
        return row2;
    }

    /**
     * Створює панель для фільтрації за діапазоном значень (наприклад, ціна або довжина стебла).
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
        minSpinner.setPreferredSize(new Dimension(60, 28));
        minSpinner.setFont(REGULAR_FONT);

        JLabel maxLabel = new JLabel("до:");
        maxLabel.setFont(REGULAR_FONT);
        JSpinner maxSpinner = new JSpinner(new SpinnerNumberModel(initialMax, minVal, maxVal, step));
        maxSpinner.setPreferredSize(new Dimension(60, 28));
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
     * Фільтрує список квітів на основі заданих критеріїв (пошук, тип, свіжість, ціна, довжина стебла, горщикові квіти).
     *
     * @param allFlowers повний список квітів
     * @return відфільтрований список квітів
     */
    @Override
    protected List<Flower> filterItems(List<Flower> allFlowers) {
        String searchText = searchField.getText().toLowerCase().trim();
        FlowerType selectedType = (FlowerType) typeFilterCombo.getSelectedItem();
        FreshnessLevel selectedFreshness = (FreshnessLevel) freshnessFilterCombo.getSelectedItem();
        double minPrice = ((Number) minPriceSpinner.getValue()).doubleValue();
        double maxPrice = ((Number) maxPriceSpinner.getValue()).doubleValue();
        int minStem = ((Number) minStemSpinner.getValue()).intValue();
        int maxStem = ((Number) maxStemSpinner.getValue()).intValue();
        boolean pottedOnly = pottedCheckBox.isSelected();

        logger.debug("Фільтрація квітів. Текст: '{}', Тип: {}, Свіжість: {}, Ціна: {}-{}, Стебло: {}-{}, В горщику: {}",
                searchText, selectedType, selectedFreshness, minPrice, maxPrice, minStem, maxStem, pottedOnly);

        return allFlowers.stream()
                .filter(flower -> (searchText.isEmpty() ||
                        flower.getDisplayName().toLowerCase().contains(searchText) ||
                        flower.getColor().toLowerCase().contains(searchText) ||
                        flower.getCountryOfOrigin().toLowerCase().contains(searchText)) &&
                        (selectedType == null || flower.getType() == selectedType) &&
                        (selectedFreshness == null || flower.getFreshnessLevel() == selectedFreshness) &&
                        (flower.getPrice() >= minPrice && flower.getPrice() <= maxPrice) &&
                        (flower.getStemLength() >= minStem && flower.getStemLength() <= maxStem) &&
                        (!pottedOnly || flower.isPotted()))
                .collect(Collectors.toList());
    }

    /**
     * Очищає всі фільтри та оновлює таблицю.
     */
    @Override
    protected void clearFilters() {
        logger.info("Очищення фільтрів для квітів.");
        searchField.setText("");
        typeFilterCombo.setSelectedIndex(0);
        freshnessFilterCombo.setSelectedIndex(0);
        minPriceSpinner.setValue(0);
        maxPriceSpinner.setValue(500);
        minStemSpinner.setValue(0);
        maxStemSpinner.setValue(100);
        pottedCheckBox.setSelected(false);
        applyFiltersAndRefresh();
    }

    // Операції з даними

    /**
     * Отримує список усіх квітів із DAO.
     *
     * @return список усіх квітів
     */
    @Override
    protected List<Flower> getAllItemsFromDAO() {
        return itemDAO.getAllFlowers();
    }

    /**
     * Отримує квітку за її ідентифікатором із DAO.
     *
     * @param id ідентифікатор квітки
     * @return об'єкт квітки або null, якщо не знайдено
     */
    @Override
    protected Flower getItemByIdFromDAO(int id) {
        return itemDAO.getFlowerById(id);
    }

    /**
     * Відкриває діалог для додавання або редагування квітки.
     *
     * @param e подія, що викликала діалог (може бути null)
     */
    @Override
    protected void showAddEditDialog(ActionEvent e) {
        boolean isEditMode = (e != null && e.getSource() == editButton) || (e == null && itemsTable.getSelectedRow() >= 0);
        Flower flowerToEdit = null;

        if (isEditMode) {
            int selectedRow = itemsTable.getSelectedRow();
            if (selectedRow < 0) {
                JOptionPane.showMessageDialog(this, "Будь ласка, оберіть квітку для редагування.", "Попередження", JOptionPane.WARNING_MESSAGE);
                return;
            }
            flowerToEdit = getItemByModelRow(itemsTable.convertRowIndexToModel(selectedRow));
        }

        AddEditFlowerDialog dialog = new AddEditFlowerDialog((JFrame) SwingUtilities.getWindowAncestor(this), flowerToEdit);
        dialog.setVisible(true);

        if (dialog.isSaved()) {
            Flower resultFlower = dialog.getFlower();
            itemDAO.saveFlower(resultFlower);
            refreshTableData();
            selectRowByFlowerId(resultFlower.getId());
        }
    }

    /**
     * Вибирає рядок у таблиці за ідентифікатором квітки.
     *
     * @param flowerId ідентифікатор квітки
     */
    private void selectRowByFlowerId(int flowerId) {
        for (int i = 0; i < itemsTable.getRowCount(); i++) {
            int modelRow = itemsTable.convertRowIndexToModel(i);
            if (modelRow >= 0 && modelRow < tableModel.getRowCount()) {
                Flower itemInRow = getItemByModelRow(modelRow);
                if (itemInRow != null && itemInRow.getId() == flowerId) {
                    itemsTable.setRowSelectionInterval(i, i);
                    itemsTable.scrollRectToVisible(itemsTable.getCellRect(i, 0, true));
                    break;
                }
            }
        }
    }

    /**
     * Видаляє вибрану квітку з таблиці та бази даних.
     *
     * @param e подія, що викликала видалення
     */
    @Override
    protected void deleteSelectedItem(ActionEvent e) {
        int selectedRow = itemsTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Будь ласка, оберіть квітку для видалення.", "Попередження", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Flower flowerToDelete = getItemByModelRow(itemsTable.convertRowIndexToModel(selectedRow));
        if (flowerToDelete == null) {
            logger.error("Не вдалося отримати квітку для видалення за вибраним рядком.");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Ви впевнені, що хочете видалити квітку \"" + flowerToDelete.getDisplayName() + "\"?",
                "Підтвердження видалення",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            itemDAO.deleteFlower(flowerToDelete.getId());
            refreshTableData();
            clearDetailsPanel();
            JOptionPane.showMessageDialog(this, "Квітку \"" + flowerToDelete.getDisplayName() + "\" успішно видалено.", "Видалення завершено", JOptionPane.INFORMATION_MESSAGE);
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
        return "Деталі квітки";
    }

    /**
     * Повертає детальну інформацію про квітку для відображення.
     *
     * @param flower об'єкт квітки
     * @return рядок із детальною інформацією або порожній рядок, якщо квітка null
     */
    @Override
    protected String getDetailedInfoForItem(Flower flower) {
        return flower != null ? flower.getDetailedInfo() : "";
    }

    /**
     * Повертає шлях до зображення квітки.
     *
     * @param flower об'єкт квітки
     * @return шлях до зображення або null, якщо квітка null
     */
    @Override
    protected String getImagePathForItem(Flower flower) {
        return flower != null ? flower.getImagePath() : null;
    }

    /**
     * Вказує, чи використовується панель рівня запасів.
     *
     * @return true, якщо панель рівня запасів активна
     */
    @Override
    protected boolean hasStockLevelBar() {
        return true;
    }

    /**
     * Оновлює панель рівня запасів для відображення кількості квітки на складі.
     *
     * @param flower об'єкт квітки
     */
    @Override
    protected void updateStockLevelBar(Flower flower) {
        if (flower == null || stockLevelBar == null) return;

        int stockQty = flower.getStockQuantity();
        int maxStockDisplay = 200;
        stockLevelBar.setMaximum(maxStockDisplay);
        stockLevelBar.setValue(Math.min(stockQty, maxStockDisplay));
        stockLevelBar.setString(stockQty + " шт.");

        if (stockQty < 20) stockLevelBar.setForeground(new Color(255, 69, 0));
        else if (stockQty < 50) stockLevelBar.setForeground(new Color(255, 165, 0));
        else stockLevelBar.setForeground(PRIMARY_COLOR);
    }

    /**
     * Повертає назву елемента в однині.
     *
     * @return рядок із назвою елемента
     */
    @Override
    protected String getItemNameSingular() {
        return "квітка";
    }
}