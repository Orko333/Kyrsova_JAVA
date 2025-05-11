package flowershop.gui.Tab;

import flowershop.dao.FlowerDAO;
import flowershop.gui.Dialog.AddEditFlowerDialog; // Потрібно для рендерерів
import flowershop.models.Flower;
import flowershop.models.Flower.FlowerType;
import flowershop.models.Flower.FreshnessLevel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

// import static flowershop.gui.Dialog.AbstractAddEditDialog.TEXT_COLOR; // Якщо використовується

/**
 * Клас FlowersTab представляє вкладку для керування квітами.
 * Успадковує функціонал від AbstractItemTab.
 */
public class FlowersTab extends AbstractItemTab<Flower, FlowerDAO> {

    private static final Logger logger = LogManager.getLogger(FlowersTab.class);

    private JComboBox<FlowerType> typeFilterCombo;
    private JComboBox<FreshnessLevel> freshnessFilterCombo;
    private JSpinner minPriceSpinner, maxPriceSpinner;
    private JSpinner minStemSpinner, maxStemSpinner;
    private JCheckBox pottedCheckBox;

    public FlowersTab() {
        super(new FlowerDAO());
        logger.info("Вкладка 'Управління квітами' ініціалізована.");
    }

    @Override
    protected String getTabTitle() {
        return "Управління квітами";
    }

    @Override
    protected String[] getColumnNames() {
        return new String[]{"ID", "Назва", "Колір", "Ціна", "Свіжість", "Стебло(см)", "Країна", "В горщику", "На складі"};
    }

    @Override
    protected Class<?>[] getColumnClasses() {
        return new Class<?>[]{Integer.class, String.class, String.class, Double.class, String.class, Integer.class, String.class, String.class, Integer.class};
    }

    @Override
    protected Object[] getRowDataForItem(Flower flower) {
        return new Object[]{flower.getId(), flower.getDisplayName(), flower.getColor(), flower.getPrice(), flower.getFreshnessLevel().getDescription() + " (" + flower.getFreshness() + "%)", flower.getStemLength(), flower.getCountryOfOrigin(), flower.isPotted() ? "Так" : "Ні", flower.getStockQuantity()};
    }

    @Override
    protected JPanel createFilterPanel() {
        logger.debug("Створення панелі фільтрів для квітів.");
        JPanel filterPanel = new JPanel();
        filterPanel.setLayout(new BoxLayout(filterPanel, BoxLayout.Y_AXIS));
        filterPanel.setBackground(Color.WHITE);
        filterPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(PRIMARY_COLOR, 1), "Фільтри квітів",
                        TitledBorder.LEFT, TitledBorder.TOP, TITLE_FONT, PRIMARY_COLOR),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        JPanel row1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        row1.setOpaque(false);
        row1.add(new JLabel("Пошук:"));
        searchField = new JTextField(15);
        searchField.setPreferredSize(new Dimension(150, 28));
        searchField.getDocument().addDocumentListener(new SimpleDocumentListener(() -> { logger.trace("Зміна тексту пошуку."); applyFiltersAndRefresh(); }));
        row1.add(searchField);

        row1.add(new JLabel("Тип:"));
        typeFilterCombo = new JComboBox<>(FlowerType.values());
        typeFilterCombo.setRenderer(new AddEditFlowerDialog.FlowerTypeRenderer()); // Використання рендерера з діалогу
        typeFilterCombo.insertItemAt(null, 0);
        typeFilterCombo.setSelectedIndex(0);
        typeFilterCombo.setFont(REGULAR_FONT);
        typeFilterCombo.setPreferredSize(new Dimension(130, 28));
        typeFilterCombo.addActionListener(e -> { logger.trace("Зміна типу фільтра."); applyFiltersAndRefresh(); });
        row1.add(typeFilterCombo);

        row1.add(new JLabel("Свіжість:"));
        freshnessFilterCombo = new JComboBox<>(FreshnessLevel.values());
        freshnessFilterCombo.setRenderer(new AddEditFlowerDialog.FreshRenderer()); // Використання рендерера з діалогу
        freshnessFilterCombo.insertItemAt(null, 0);
        freshnessFilterCombo.setSelectedIndex(0);
        freshnessFilterCombo.setFont(REGULAR_FONT);
        freshnessFilterCombo.setPreferredSize(new Dimension(140, 28));
        freshnessFilterCombo.addActionListener(e -> { logger.trace("Зміна свіжості фільтра."); applyFiltersAndRefresh(); });
        row1.add(freshnessFilterCombo);

        JPanel row2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        row2.setOpaque(false);

        JPanel pricePanel = createRangeFilterPanel("Ціна", 0, 5000, 10,0, 500);
        minPriceSpinner = (JSpinner) ((JPanel)pricePanel.getComponent(0)).getComponent(1);
        maxPriceSpinner = (JSpinner) ((JPanel)pricePanel.getComponent(0)).getComponent(3);
        minPriceSpinner.addChangeListener(e -> { logger.trace("Зміна мінімальної ціни."); applyFiltersAndRefresh(); });
        maxPriceSpinner.addChangeListener(e -> { logger.trace("Зміна максимальної ціни."); applyFiltersAndRefresh(); });

        JPanel stemPanel = createRangeFilterPanel("Стебло (см)", 0, 200, 5, 0, 100);
        minStemSpinner = (JSpinner) ((JPanel)stemPanel.getComponent(0)).getComponent(1);
        maxStemSpinner = (JSpinner) ((JPanel)stemPanel.getComponent(0)).getComponent(3);
        minStemSpinner.addChangeListener(e -> { logger.trace("Зміна мінімальної довжини стебла."); applyFiltersAndRefresh(); });
        maxStemSpinner.addChangeListener(e -> { logger.trace("Зміна максимальної довжини стебла."); applyFiltersAndRefresh(); });

        pottedCheckBox = new JCheckBox("В горщику");
        pottedCheckBox.setFont(REGULAR_FONT);
        pottedCheckBox.setOpaque(false);
        pottedCheckBox.addActionListener(e -> { logger.trace("Зміна фільтра 'В горщику'."); applyFiltersAndRefresh(); });

        clearFiltersButton = createStyledButton("Очистити", "FileChooser.deleteIcon");
        clearFiltersButton.addActionListener(e -> { logger.info("Натиснуто кнопку 'Очистити фільтри'."); clearFilters(); });

        row2.add(pricePanel);
        row2.add(stemPanel);
        row2.add(pottedCheckBox);
        row2.add(clearFiltersButton);

        filterPanel.add(row1);
        filterPanel.add(row2);
        logger.debug("Панель фільтрів для квітів створена.");
        return filterPanel;
    }

    private JPanel createRangeFilterPanel(String title, int minVal, int maxVal, int step, int initialMin, int initialMax) {
        logger.trace("Створення панелі фільтра діапазону: '{}'", title);
        JPanel panel = new JPanel(new BorderLayout());
        // ... (решта коду ідентична AccessoriesTab)
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(SECONDARY_COLOR, 1),
                title,
                TitledBorder.LEFT, TitledBorder.TOP, REGULAR_FONT, PRIMARY_COLOR
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
                .filter(flower -> (searchText.isEmpty() || flower.getDisplayName().toLowerCase().contains(searchText) || flower.getColor().toLowerCase().contains(searchText) || flower.getCountryOfOrigin().toLowerCase().contains(searchText)) &&
                        (selectedType == null || flower.getType() == selectedType) &&
                        (selectedFreshness == null || flower.getFreshnessLevel() == selectedFreshness) &&
                        (flower.getPrice() >= minPrice && flower.getPrice() <= maxPrice) &&
                        (flower.getStemLength() >= minStem && flower.getStemLength() <= maxStem) &&
                        (!pottedOnly || flower.isPotted()))
                .collect(Collectors.toList());
    }

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
        logger.debug("Фільтри очищено, таблицю оновлено.");
    }

    @Override
    protected String getDetailsPanelTitle() {
        return "Деталі квітки";
    }

    @Override
    protected String getDetailedInfoForItem(Flower flower) {
        if (flower == null) {
            logger.trace("Спроба отримати деталі для null квітки.");
            return "";
        }
        logger.trace("Формування деталей для квітки ID: {}", flower.getId());
        return flower.getDetailedInfo();
    }

    @Override
    protected String getImagePathForItem(Flower flower) {
        return flower != null ? flower.getImagePath() : null;
    }

    @Override
    protected boolean hasStockLevelBar() {
        return true;
    }

    @Override
    protected void updateStockLevelBar(Flower flower) {
        if (flower == null || stockLevelBar == null) return;
        int stockQty = flower.getStockQuantity();
        logger.trace("Оновлення stockLevelBar для квітки ID: {}, кількість: {}", flower.getId(), stockQty);
        int maxStockDisplay = 200;
        stockLevelBar.setMaximum(maxStockDisplay);
        stockLevelBar.setValue(Math.min(stockQty, maxStockDisplay));
        stockLevelBar.setString(stockQty + " шт.");
        if (stockQty < 20) stockLevelBar.setForeground(new Color(255, 69, 0));
        else if (stockQty < 50) stockLevelBar.setForeground(new Color(255, 165, 0));
        else stockLevelBar.setForeground(PRIMARY_COLOR);
    }

    @Override
    protected String getItemNameSingular() {
        return "квітка";
    }

    @Override
    protected void showAddEditDialog(ActionEvent e) {
        Flower flowerToEdit = null;
        boolean isEditMode = (e != null && e.getSource() == editButton) || (e == null && itemsTable.getSelectedRow() >= 0);
        logger.info("Показ діалогу додавання/редагування квітки. Режим редагування: {}", isEditMode);

        if (isEditMode) {
            int selectedRow = itemsTable.getSelectedRow();
            if (selectedRow < 0) {
                logger.warn("Спроба редагувати, але жодну квітку не вибрано.");
                JOptionPane.showMessageDialog(this, "Будь ласка, оберіть квітку для редагування.", "Попередження", JOptionPane.WARNING_MESSAGE);
                return;
            }
            flowerToEdit = getItemByModelRow(itemsTable.convertRowIndexToModel(selectedRow));
            logger.debug("Редагується квітка: {}", flowerToEdit != null ? flowerToEdit.getDisplayName() : "null");
        }

        AddEditFlowerDialog dialog = new AddEditFlowerDialog((JFrame) SwingUtilities.getWindowAncestor(this), flowerToEdit);
        dialog.setVisible(true);

        if (dialog.isSaved()) {
            Flower resultFlower = dialog.getFlower();
            logger.info("Діалог збережено. Квітка: {}", resultFlower.getDisplayName());
            itemDAO.saveFlower(resultFlower);
            refreshTableData();
            for (int i = 0; i < itemsTable.getRowCount(); i++) {
                int modelRow = itemsTable.convertRowIndexToModel(i);
                if (modelRow >= 0 && modelRow < tableModel.getRowCount()) {
                    Flower itemInRow = getItemByModelRow(modelRow);
                    if (itemInRow != null && itemInRow.getId() == resultFlower.getId()){
                        itemsTable.setRowSelectionInterval(i, i);
                        itemsTable.scrollRectToVisible(itemsTable.getCellRect(i, 0, true));
                        logger.debug("Виділено збережену/оновлену квітку у таблиці: ID {}", resultFlower.getId());
                        break;
                    }
                }
            }
        } else {
            logger.info("Діалог додавання/редагування квітки скасовано.");
        }
    }

    @Override
    protected void deleteSelectedItem(ActionEvent e) {
        int selectedRow = itemsTable.getSelectedRow();
        if (selectedRow < 0) {
            logger.warn("Спроба видалити, але жодну квітку не вибрано.");
            JOptionPane.showMessageDialog(this, "Будь ласка, оберіть квітку для видалення.", "Попередження", JOptionPane.WARNING_MESSAGE);
            return;
        }
        Flower flowerToDelete = getItemByModelRow(itemsTable.convertRowIndexToModel(selectedRow));
        if (flowerToDelete == null) {
            logger.error("Не вдалося отримати квітку для видалення за вибраним рядком.");
            return;
        }
        logger.info("Запит на видалення квітки: {}", flowerToDelete.getDisplayName());

        int confirm = JOptionPane.showConfirmDialog(this,
                "Ви впевнені, що хочете видалити квітку \"" + flowerToDelete.getDisplayName() + "\"?",
                "Підтвердження видалення", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            logger.debug("Видалення підтверджено користувачем.");
            itemDAO.deleteFlower(flowerToDelete.getId());
            refreshTableData();
            clearDetailsPanel();
            JOptionPane.showMessageDialog(this, "Квітку \"" + flowerToDelete.getDisplayName() + "\" успішно видалено.", "Видалення завершено", JOptionPane.INFORMATION_MESSAGE);
            logger.info("Квітку ID {} успішно видалено.", flowerToDelete.getId());
        } else {
            logger.debug("Видалення скасовано користувачем.");
        }
    }

    @Override
    protected void showSortDialog(ActionEvent e) {
        logger.info("Показ діалогу сортування квітів.");
        JDialog sortDialog = new JDialog((JFrame) SwingUtilities.getWindowAncestor(this), "Сортування квітів", true);
        // ... (решта коду діалогу сортування)
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10,0));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10,0,10,10));
        JButton applyButton = createStyledButton("Застосувати", "FileChooser.approveSelectionIcon");
        // ...
        sortDialog.setLayout(new BorderLayout(10,10));
        sortDialog.setSize(380, 320);
        sortDialog.setLocationRelativeTo(this);
        sortDialog.getContentPane().setBackground(Color.WHITE);

        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        headerPanel.setBackground(SECONDARY_COLOR);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10,0,10,0));
        JLabel titleLabel = new JLabel("Оберіть параметр сортування");
        titleLabel.setFont(TITLE_FONT);
        titleLabel.setForeground(new Color(50,50,50));
        headerPanel.add(titleLabel);

        JPanel sortOptionsPanel = new JPanel(new GridLayout(0, 1, 5, 5));
        sortOptionsPanel.setBackground(Color.WHITE);
        sortOptionsPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        JRadioButton nameAsc = createStyledRadioButton("За назвою (А-Я)", "Table.ascendingSortIcon");
        JRadioButton nameDesc = createStyledRadioButton("За назвою (Я-А)", "Table.descendingSortIcon");
        JRadioButton priceAsc = createStyledRadioButton("За ціною (зростання)", "Table.ascendingSortIcon");
        JRadioButton priceDesc = createStyledRadioButton("За ціною (спадання)", "Table.descendingSortIcon");
        JRadioButton freshnessAsc = createStyledRadioButton("За свіжістю (зростання)", "Table.ascendingSortIcon");
        JRadioButton freshnessDesc = createStyledRadioButton("За свіжістю (спадання)", "Table.descendingSortIcon");
        JRadioButton stemAsc = createStyledRadioButton("За довжиною стебла (зростання)", "Table.ascendingSortIcon");
        JRadioButton stemDesc = createStyledRadioButton("За довжиною стебла (спадання)", "Table.descendingSortIcon");

        ButtonGroup group = new ButtonGroup();
        group.add(nameAsc); group.add(nameDesc); group.add(priceAsc); group.add(priceDesc);
        group.add(freshnessAsc); group.add(freshnessDesc); group.add(stemAsc); group.add(stemDesc);

        sortOptionsPanel.add(nameAsc); sortOptionsPanel.add(nameDesc);
        sortOptionsPanel.add(priceAsc); sortOptionsPanel.add(priceDesc);
        sortOptionsPanel.add(freshnessAsc); sortOptionsPanel.add(freshnessDesc);
        sortOptionsPanel.add(stemAsc); sortOptionsPanel.add(stemDesc);

        TableRowSorter<?> sorter = (TableRowSorter<?>) itemsTable.getRowSorter();
        if (sorter != null && !sorter.getSortKeys().isEmpty()) {
            RowSorter.SortKey sortKey = sorter.getSortKeys().get(0);
            int columnIndex = sortKey.getColumn();
            SortOrder sortOrder = sortKey.getSortOrder();
            if (columnIndex == 1) { if (sortOrder == SortOrder.ASCENDING) nameAsc.setSelected(true); else nameDesc.setSelected(true); }
            else if (columnIndex == 3) { if (sortOrder == SortOrder.ASCENDING) priceAsc.setSelected(true); else priceDesc.setSelected(true); }
            else if (columnIndex == 4) { if (sortOrder == SortOrder.ASCENDING) freshnessAsc.setSelected(true); else freshnessDesc.setSelected(true); }
            else if (columnIndex == 5) { if (sortOrder == SortOrder.ASCENDING) stemAsc.setSelected(true); else stemDesc.setSelected(true); }
        } else { nameAsc.setSelected(true); }

        applyButton.addActionListener(evt -> {
            List<RowSorter.SortKey> sortKeys = new ArrayList<>();
            if (nameAsc.isSelected()) { logger.debug("Сортування квітів за назвою (А-Я)"); sortKeys.add(new RowSorter.SortKey(1, SortOrder.ASCENDING)); }
            else if (nameDesc.isSelected()) { logger.debug("Сортування квітів за назвою (Я-А)"); sortKeys.add(new RowSorter.SortKey(1, SortOrder.DESCENDING)); }
            else if (priceAsc.isSelected()) { logger.debug("Сортування квітів за ціною (зростання)"); sortKeys.add(new RowSorter.SortKey(3, SortOrder.ASCENDING)); }
            else if (priceDesc.isSelected()) { logger.debug("Сортування квітів за ціною (спадання)"); sortKeys.add(new RowSorter.SortKey(3, SortOrder.DESCENDING)); }
            else if (freshnessAsc.isSelected()) { logger.debug("Сортування квітів за свіжістю (зростання)"); sortKeys.add(new RowSorter.SortKey(4, SortOrder.ASCENDING)); }
            else if (freshnessDesc.isSelected()) { logger.debug("Сортування квітів за свіжістю (спадання)"); sortKeys.add(new RowSorter.SortKey(4, SortOrder.DESCENDING)); }
            else if (stemAsc.isSelected()) { logger.debug("Сортування квітів за стеблом (зростання)"); sortKeys.add(new RowSorter.SortKey(5, SortOrder.ASCENDING)); }
            else if (stemDesc.isSelected()) { logger.debug("Сортування квітів за стеблом (спадання)"); sortKeys.add(new RowSorter.SortKey(5, SortOrder.DESCENDING)); }

            logger.info("Застосування сортування для квітів: {}", sortKeys);
            itemsTable.getRowSorter().setSortKeys(sortKeys);
            sortDialog.dispose();
        });

        JButton cancelButton = createStyledButton("Скасувати", "FileChooser.cancelSelectionIcon");
        cancelButton.addActionListener(evt -> { logger.debug("Діалог сортування квітів скасовано."); sortDialog.dispose(); });

        buttonPanel.add(cancelButton);
        buttonPanel.add(applyButton);

        sortDialog.add(headerPanel, BorderLayout.NORTH);
        sortDialog.add(sortOptionsPanel, BorderLayout.CENTER);
        sortDialog.add(buttonPanel, BorderLayout.SOUTH);
        sortDialog.setVisible(true);
    }

    private JRadioButton createStyledRadioButton(String text, String uiManagerIconKey) {
        // ... (ідентично AccessoriesTab)
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

    @Override
    protected List<Flower> getAllItemsFromDAO() {
        logger.trace("Завантаження всіх квітів з DAO.");
        return itemDAO.getAllFlowers();
    }

    @Override
    protected Flower getItemByIdFromDAO(int id) {
        logger.trace("Завантаження квітки за ID: {} з DAO.", id);
        return itemDAO.getFlowerById(id);
    }

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
}