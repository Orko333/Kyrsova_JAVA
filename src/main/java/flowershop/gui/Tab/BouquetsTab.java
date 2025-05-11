package flowershop.gui.Tab;

import flowershop.dao.BouquetDAO;
import flowershop.dao.AccessoryDAO;
import flowershop.dao.FlowerDAO;
import flowershop.gui.Dialog.AddEditBouquetDialog;
import flowershop.models.Bouquet;
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

/**
 * Клас BouquetsTab представляє вкладку для керування букетами.
 * Успадковує функціонал від AbstractItemTab.
 */
public class BouquetsTab extends AbstractItemTab<Bouquet, BouquetDAO> {

    private static final Logger logger = LogManager.getLogger(BouquetsTab.class);

    private JSpinner minPriceSpinner, maxPriceSpinner;
    private JSpinner minDiscountSpinner, maxDiscountSpinner;
    private FlowerDAO flowerDAO;
    private AccessoryDAO accessoryDAO;

    public BouquetsTab() {
        super(new BouquetDAO());
        this.flowerDAO = new FlowerDAO();
        this.accessoryDAO = new AccessoryDAO();
        logger.info("Вкладка 'Управління букетами' ініціалізована. Додаткові DAO (Flower, Accessory) завантажено.");
    }

    @Override
    protected String getTabTitle() {
        return "Управління букетами";
    }

    @Override
    protected String[] getColumnNames() {
        return new String[]{"ID", "Назва", "К-сть квітів", "К-сть аксес.", "Ціна", "Ціна зі знижкою", "Знижка (%)"};
    }

    @Override
    protected Class<?>[] getColumnClasses() {
        return new Class<?>[]{Integer.class, String.class, Integer.class, Integer.class, Double.class, Double.class, String.class};
    }

    @Override
    protected Object[] getRowDataForItem(Bouquet bouquet) {
        return new Object[]{bouquet.getId(), bouquet.getName(), bouquet.getTotalFlowerCount(), bouquet.getAccessories().size(), bouquet.calculateTotalPrice(), bouquet.calculateDiscountedPrice(), String.format("%.0f%%", bouquet.getDiscount())};
    }

    @Override
    protected JPanel createFilterPanel() {
        logger.debug("Створення панелі фільтрів для букетів.");
        JPanel filterPanel = new JPanel();
        filterPanel.setLayout(new BoxLayout(filterPanel, BoxLayout.Y_AXIS));
        filterPanel.setBackground(Color.WHITE);
        filterPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(PRIMARY_COLOR, 1), "Фільтри букетів",
                        TitledBorder.LEFT, TitledBorder.TOP, TITLE_FONT, PRIMARY_COLOR),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        JPanel searchRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        searchRow.setOpaque(false);
        searchRow.add(new JLabel("Пошук за назвою:"));
        searchField = new JTextField(20);
        searchField.setPreferredSize(new Dimension(250, 28));
        searchField.getDocument().addDocumentListener(new SimpleDocumentListener(() -> { logger.trace("Зміна тексту пошуку."); applyFiltersAndRefresh(); }));
        searchRow.add(searchField);

        JPanel rangeRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        rangeRow.setOpaque(false);

        JPanel pricePanel = createRangeFilterPanel("Загальна ціна", 0, 20000, 100,0,5000);
        minPriceSpinner = (JSpinner) ((JPanel)pricePanel.getComponent(0)).getComponent(1);
        maxPriceSpinner = (JSpinner) ((JPanel)pricePanel.getComponent(0)).getComponent(3);
        minPriceSpinner.addChangeListener(e -> { logger.trace("Зміна мінімальної ціни."); applyFiltersAndRefresh(); });
        maxPriceSpinner.addChangeListener(e -> { logger.trace("Зміна максимальної ціни."); applyFiltersAndRefresh(); });

        JPanel discountPanel = createRangeFilterPanel("Знижка (%)", 0, 100, 5, 0, 50);
        minDiscountSpinner = (JSpinner) ((JPanel)discountPanel.getComponent(0)).getComponent(1);
        maxDiscountSpinner = (JSpinner) ((JPanel)discountPanel.getComponent(0)).getComponent(3);
        minDiscountSpinner.addChangeListener(e -> { logger.trace("Зміна мінімальної знижки."); applyFiltersAndRefresh(); });
        maxDiscountSpinner.addChangeListener(e -> { logger.trace("Зміна максимальної знижки."); applyFiltersAndRefresh(); });

        clearFiltersButton = createStyledButton("Очистити", "FileChooser.deleteIcon");
        clearFiltersButton.addActionListener(e -> { logger.info("Натиснуто кнопку 'Очистити фільтри'."); clearFilters(); });

        rangeRow.add(pricePanel);
        rangeRow.add(discountPanel);
        rangeRow.add(clearFiltersButton);

        filterPanel.add(searchRow);
        filterPanel.add(rangeRow);
        logger.debug("Панель фільтрів для букетів створена.");
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

    @Override
    protected List<Bouquet> filterItems(List<Bouquet> allBouquets) {
        String searchText = searchField.getText().toLowerCase().trim();
        double minPrice = ((Number) minPriceSpinner.getValue()).doubleValue();
        double maxPrice = ((Number) maxPriceSpinner.getValue()).doubleValue();
        double minDiscount = ((Number) minDiscountSpinner.getValue()).doubleValue();
        double maxDiscount = ((Number) maxDiscountSpinner.getValue()).doubleValue();
        logger.debug("Фільтрація букетів. Текст: '{}', Ціна: {}-{}, Знижка: {}-{}",
                searchText, minPrice, maxPrice, minDiscount, maxDiscount);

        return allBouquets.stream()
                .filter(bouquet -> (searchText.isEmpty() || bouquet.getName().toLowerCase().contains(searchText)) &&
                        (bouquet.calculateTotalPrice() >= minPrice && bouquet.calculateTotalPrice() <= maxPrice) &&
                        (bouquet.getDiscount() >= minDiscount && bouquet.getDiscount() <= maxDiscount))
                .collect(Collectors.toList());
    }

    @Override
    protected void clearFilters() {
        logger.info("Очищення фільтрів для букетів.");
        searchField.setText("");
        minPriceSpinner.setValue(0);
        maxPriceSpinner.setValue(5000);
        minDiscountSpinner.setValue(0);
        maxDiscountSpinner.setValue(50);
        applyFiltersAndRefresh();
        logger.debug("Фільтри очищено, таблицю оновлено.");
    }

    @Override
    protected String getDetailsPanelTitle() {
        return "Деталі букета";
    }

    @Override
    protected String getDetailedInfoForItem(Bouquet bouquet) {
        if (bouquet == null) {
            logger.trace("Спроба отримати деталі для null букета.");
            return "";
        }
        logger.trace("Формування деталей для букета ID: {}", bouquet.getId());
        return bouquet.getDetailedInfo();
    }

    @Override
    protected String getImagePathForItem(Bouquet bouquet) {
        return bouquet != null ? bouquet.getImagePath() : null;
    }

    @Override
    protected boolean hasStockLevelBar() {
        return false;
    }

    @Override
    protected void updateStockLevelBar(Bouquet item) {
        // Не використовується
    }

    @Override
    protected String getItemNameSingular() {
        return "букет";
    }

    @Override
    protected void showAddEditDialog(ActionEvent e) {
        Bouquet bouquetToEdit = null;
        boolean isEditMode = (e != null && e.getSource() == editButton) || (e == null && itemsTable.getSelectedRow() >= 0);
        logger.info("Показ діалогу додавання/редагування букета. Режим редагування: {}", isEditMode);

        if (isEditMode) {
            int selectedRow = itemsTable.getSelectedRow();
            if (selectedRow < 0) {
                logger.warn("Спроба редагувати, але жоден букет не вибрано.");
                JOptionPane.showMessageDialog(this, "Будь ласка, оберіть букет для редагування.", "Попередження", JOptionPane.WARNING_MESSAGE);
                return;
            }
            bouquetToEdit = getItemByModelRow(itemsTable.convertRowIndexToModel(selectedRow));
            logger.debug("Редагується букет: {}", bouquetToEdit != null ? bouquetToEdit.getName() : "null");
        }

        AddEditBouquetDialog dialog = new AddEditBouquetDialog(
                (JFrame) SwingUtilities.getWindowAncestor(this), bouquetToEdit,
                flowerDAO.getAllFlowers(), accessoryDAO.getAllAccessories());
        logger.debug("Передано у діалог {} квітів та {} аксесуарів.", flowerDAO.getAllFlowers().size(), accessoryDAO.getAllAccessories().size());
        dialog.setVisible(true);

        if (dialog.isSaved()) {
            Bouquet resultBouquet = dialog.getBouquet();
            logger.info("Діалог збережено. Букет: {}", resultBouquet.getName());
            itemDAO.saveBouquet(resultBouquet);
            refreshTableData();
            for (int i = 0; i < itemsTable.getRowCount(); i++) {
                int modelRow = itemsTable.convertRowIndexToModel(i);
                if (modelRow >= 0 && modelRow < tableModel.getRowCount()) {
                    Bouquet itemInRow = getItemByModelRow(modelRow);
                    if (itemInRow != null && itemInRow.getId() == resultBouquet.getId()){
                        itemsTable.setRowSelectionInterval(i, i);
                        itemsTable.scrollRectToVisible(itemsTable.getCellRect(i, 0, true));
                        logger.debug("Виділено збережений/оновлений букет у таблиці: ID {}", resultBouquet.getId());
                        break;
                    }
                }
            }
        } else {
            logger.info("Діалог додавання/редагування букета скасовано.");
        }
    }

    @Override
    protected void deleteSelectedItem(ActionEvent e) {
        int selectedRow = itemsTable.getSelectedRow();
        if (selectedRow < 0) {
            logger.warn("Спроба видалити, але жоден букет не вибрано.");
            JOptionPane.showMessageDialog(this, "Будь ласка, оберіть букет для видалення.", "Попередження", JOptionPane.WARNING_MESSAGE);
            return;
        }
        Bouquet bouquetToDelete = getItemByModelRow(itemsTable.convertRowIndexToModel(selectedRow));
        if (bouquetToDelete == null) {
            logger.error("Не вдалося отримати букет для видалення за вибраним рядком.");
            return;
        }
        logger.info("Запит на видалення букета: {}", bouquetToDelete.getName());

        int confirm = JOptionPane.showConfirmDialog(this,
                "Ви впевнені, що хочете видалити букет \"" + bouquetToDelete.getName() + "\"?",
                "Підтвердження видалення", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            logger.debug("Видалення підтверджено користувачем.");
            itemDAO.deleteBouquet(bouquetToDelete.getId());
            refreshTableData();
            clearDetailsPanel();
            JOptionPane.showMessageDialog(this, "Букет \"" + bouquetToDelete.getName() + "\" успішно видалено.", "Видалення завершено", JOptionPane.INFORMATION_MESSAGE);
            logger.info("Букет ID {} успішно видалено.", bouquetToDelete.getId());
        } else {
            logger.debug("Видалення скасовано користувачем.");
        }
    }

    @Override
    protected void showSortDialog(ActionEvent e) {
        logger.info("Показ діалогу сортування букетів.");
        JDialog sortDialog = new JDialog((JFrame) SwingUtilities.getWindowAncestor(this), "Сортування букетів", true);
        // ... (решта коду діалогу сортування)
        // Додамо лог при застосуванні сортування
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10,0));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10,0,10,10));
        JButton applyButton = createStyledButton("Застосувати", "FileChooser.approveSelectionIcon");
        applyButton.addActionListener(evt -> {
            List<RowSorter.SortKey> sortKeys = new ArrayList<>();
            // ... (логіка визначення sortKeys як у AccessoriesTab, але з відповідними індексами стовпців)
            // if (nameAsc.isSelected()) { logger.debug("Сортування за назвою (А-Я)"); sortKeys.add(new RowSorter.SortKey(1, SortOrder.ASCENDING)); }
            // ...
            logger.info("Застосування сортування для букетів: {}", sortKeys);
            itemsTable.getRowSorter().setSortKeys(sortKeys);
            sortDialog.dispose();
        });
        // ... (решта коду діалогу сортування)
        sortDialog.setLayout(new BorderLayout(10,10));
        sortDialog.setSize(380, 280);
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
        JRadioButton discountAsc = createStyledRadioButton("За знижкою (зростання)", "Table.ascendingSortIcon");
        JRadioButton discountDesc = createStyledRadioButton("За знижкою (спадання)", "Table.descendingSortIcon");

        ButtonGroup group = new ButtonGroup();
        group.add(nameAsc); group.add(nameDesc); group.add(priceAsc); group.add(priceDesc);
        group.add(discountAsc); group.add(discountDesc);

        sortOptionsPanel.add(nameAsc); sortOptionsPanel.add(nameDesc);
        sortOptionsPanel.add(priceAsc); sortOptionsPanel.add(priceDesc);
        sortOptionsPanel.add(discountAsc); sortOptionsPanel.add(discountDesc);

        TableRowSorter<?> sorter = (TableRowSorter<?>) itemsTable.getRowSorter();
        if (sorter != null && !sorter.getSortKeys().isEmpty()) {
            RowSorter.SortKey sortKey = sorter.getSortKeys().get(0);
            int columnIndex = sortKey.getColumn();
            SortOrder sortOrder = sortKey.getSortOrder();
            if (columnIndex == 1) { if (sortOrder == SortOrder.ASCENDING) nameAsc.setSelected(true); else nameDesc.setSelected(true); }
            else if (columnIndex == 5) { if (sortOrder == SortOrder.ASCENDING) priceAsc.setSelected(true); else priceDesc.setSelected(true); }
            else if (columnIndex == 6) { if (sortOrder == SortOrder.ASCENDING) discountAsc.setSelected(true); else discountDesc.setSelected(true); }
        } else { nameAsc.setSelected(true); }


        applyButton.addActionListener(evt -> { // Переміщено сюди, щоб радіокнопки були доступні
            List<RowSorter.SortKey> sortKeys = new ArrayList<>();
            if (nameAsc.isSelected()) { logger.debug("Сортування букетів за назвою (А-Я)"); sortKeys.add(new RowSorter.SortKey(1, SortOrder.ASCENDING)); }
            else if (nameDesc.isSelected()) { logger.debug("Сортування букетів за назвою (Я-А)"); sortKeys.add(new RowSorter.SortKey(1, SortOrder.DESCENDING)); }
            else if (priceAsc.isSelected()) { logger.debug("Сортування букетів за ціною (зростання)"); sortKeys.add(new RowSorter.SortKey(5, SortOrder.ASCENDING)); }
            else if (priceDesc.isSelected()) { logger.debug("Сортування букетів за ціною (спадання)"); sortKeys.add(new RowSorter.SortKey(5, SortOrder.DESCENDING)); }
            else if (discountAsc.isSelected()) { logger.debug("Сортування букетів за знижкою (зростання)"); sortKeys.add(new RowSorter.SortKey(6, SortOrder.ASCENDING)); }
            else if (discountDesc.isSelected()) { logger.debug("Сортування букетів за знижкою (спадання)"); sortKeys.add(new RowSorter.SortKey(6, SortOrder.DESCENDING)); }

            logger.info("Застосування сортування для букетів: {}", sortKeys);
            itemsTable.getRowSorter().setSortKeys(sortKeys);
            sortDialog.dispose();
        });

        JButton cancelButton = createStyledButton("Скасувати", "FileChooser.cancelSelectionIcon");
        cancelButton.addActionListener(evt -> { logger.debug("Діалог сортування букетів скасовано."); sortDialog.dispose(); });

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
    protected List<Bouquet> getAllItemsFromDAO() {
        logger.trace("Завантаження всіх букетів з DAO.");
        return itemDAO.getAllBouquets();
    }

    @Override
    protected Bouquet getItemByIdFromDAO(int id) {
        logger.trace("Завантаження букета за ID: {} з DAO.", id);
        return itemDAO.getBouquetById(id);
    }

    @Override
    protected void configureTableColumnWidths() {
        super.configureTableColumnWidths();
        logger.debug("Налаштування ширини стовпців для таблиці букетів.");
        itemsTable.getColumnModel().getColumn(0).setPreferredWidth(30);
        itemsTable.getColumnModel().getColumn(1).setPreferredWidth(200);
        itemsTable.getColumnModel().getColumn(2).setPreferredWidth(80);
        itemsTable.getColumnModel().getColumn(3).setPreferredWidth(90);
        itemsTable.getColumnModel().getColumn(4).setPreferredWidth(100);
        itemsTable.getColumnModel().getColumn(5).setPreferredWidth(120);
        itemsTable.getColumnModel().getColumn(6).setPreferredWidth(80);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        itemsTable.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        itemsTable.getColumnModel().getColumn(2).setCellRenderer(centerRenderer);
        itemsTable.getColumnModel().getColumn(3).setCellRenderer(centerRenderer);
        itemsTable.getColumnModel().getColumn(6).setCellRenderer(centerRenderer);

        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(JLabel.RIGHT);
        itemsTable.getColumnModel().getColumn(4).setCellRenderer(rightRenderer);
        itemsTable.getColumnModel().getColumn(5).setCellRenderer(rightRenderer);
    }
}