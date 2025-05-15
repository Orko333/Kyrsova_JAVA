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
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Вкладка для керування букетами у квітковому магазині.
 * Надає інтерфейс для перегляду, фільтрації, додавання, редагування та видалення букетів.
 * Успадковується від {@link AbstractItemTab}, що забезпечує базову функціонність для роботи з таблицями.
 */
public class BouquetsTab extends AbstractItemTab<Bouquet, BouquetDAO> {

    private static final Logger logger = LogManager.getLogger(BouquetsTab.class);

    JSpinner minPriceSpinner;
    JSpinner maxPriceSpinner;
    JSpinner minDiscountSpinner;
    JSpinner maxDiscountSpinner;
    private final FlowerDAO flowerDAO;
    private final AccessoryDAO accessoryDAO;

    /**
     * Конструктор вкладки для керування букетами.
     * Ініціалізує вкладку з DAO для роботи з букетами, квітами та аксесуарами.
     */
    public BouquetsTab() {
        super(new BouquetDAO());
        this.flowerDAO = new FlowerDAO();
        this.accessoryDAO = new AccessoryDAO();
        logger.info("Вкладка 'Управління букетами' ініціалізована.");
    }

    // Конфігурація вкладки

    /**
     * Повертає заголовок вкладки.
     *
     * @return рядок з назвою вкладки
     */
    @Override
    protected String getTabTitle() {
        return "Управління букетами";
    }

    /**
     * Повертає назви стовпців таблиці букетів.
     *
     * @return масив рядків із назвами стовпців
     */
    @Override
    protected String[] getColumnNames() {
        return new String[]{"ID", "Назва", "К-сть квітів", "К-сть аксес.", "Ціна", "Ціна зі знижкою", "Знижка (%)"};
    }

    /**
     * Повертає класи даних для стовпців таблиці.
     *
     * @return масив класів, що відповідають типам даних стовпців
     */
    @Override
    protected Class<?>[] getColumnClasses() {
        return new Class<?>[]{Integer.class, String.class, Integer.class, Integer.class, Double.class, Double.class, String.class};
    }

    /**
     * Формує дані для рядка таблиці на основі об'єкта букета.
     *
     * @param bouquet об'єкт букета
     * @return масив об'єктів із даними для відображення в таблиці
     */
    @Override
    protected Object[] getRowDataForItem(Bouquet bouquet) {
        return new Object[]{
                bouquet.getId(),
                bouquet.getName(),
                bouquet.getTotalFlowerCount(),
                bouquet.getAccessories().size(),
                bouquet.calculateTotalPrice(),
                bouquet.calculateDiscountedPrice(),
                String.format("%.0f%%", bouquet.getDiscount())
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

    // Панель фільтрів

    /**
     * Створює панель фільтрів для пошуку та фільтрації букетів.
     *
     * @return панель із компонентами фільтрів
     */
    @Override
    protected JPanel createFilterPanel() {
        logger.debug("Створення панелі фільтрів для букетів.");
        JPanel filterPanel = new JPanel();
        filterPanel.setLayout(new BoxLayout(filterPanel, BoxLayout.Y_AXIS));
        filterPanel.setBackground(Color.WHITE);
        filterPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(PRIMARY_COLOR, 1),
                        "Фільтри букетів",
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
     * Створює рядок панелі фільтрів із полем пошуку за назвою.
     *
     * @return панель із компонентами пошуку
     */
    private JPanel createSearchRow() {
        JPanel searchRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        searchRow.setOpaque(false);
        searchRow.add(new JLabel("Пошук за назвою:"));
        searchField = new JTextField(20);
        searchField.setPreferredSize(new Dimension(250, 28));
        searchField.getDocument().addDocumentListener(new SimpleDocumentListener(this::applyFiltersAndRefresh));
        searchRow.add(searchField);
        return searchRow;
    }

    /**
     * Створює рядок панелі фільтрів із фільтрами ціни, знижки та кнопкою очищення.
     *
     * @return панель із компонентами фільтрів діапазону
     */
    private JPanel createRangeRow() {
        JPanel rangeRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        rangeRow.setOpaque(false);

        JPanel pricePanel = createRangeFilterPanel("Загальна ціна", 0, 20000, 100, 0, 5000);
        minPriceSpinner = (JSpinner) ((JPanel) pricePanel.getComponent(0)).getComponent(1);
        maxPriceSpinner = (JSpinner) ((JPanel) pricePanel.getComponent(0)).getComponent(3);
        minPriceSpinner.addChangeListener(e -> applyFiltersAndRefresh());
        maxPriceSpinner.addChangeListener(e -> applyFiltersAndRefresh());

        JPanel discountPanel = createRangeFilterPanel("Знижка (%)", 0, 100, 5, 0, 50);
        minDiscountSpinner = (JSpinner) ((JPanel) discountPanel.getComponent(0)).getComponent(1);
        maxDiscountSpinner = (JSpinner) ((JPanel) discountPanel.getComponent(0)).getComponent(3);
        minDiscountSpinner.addChangeListener(e -> applyFiltersAndRefresh());
        maxDiscountSpinner.addChangeListener(e -> applyFiltersAndRefresh());

        clearFiltersButton = createStyledButton("Очистити", "FileChooser.deleteIcon");
        clearFiltersButton.addActionListener(e -> clearFilters());

        rangeRow.add(pricePanel);
        rangeRow.add(discountPanel);
        rangeRow.add(clearFiltersButton);
        return rangeRow;
    }

    /**
     * Створює панель для фільтрації за діапазоном значень (наприклад, ціна або знижка).
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

    // Фільтрація

    /**
     * Фільтрує список букетів на основі заданих критеріїв (пошук за назвою, ціна, знижка).
     *
     * @param allBouquets повний список букетів
     * @return відфільтрований список букетів
     */
    @Override
    protected List<Bouquet> filterItems(List<Bouquet> allBouquets) {
        String searchText = searchField.getText().toLowerCase().trim();
        double minPrice = ((Number) minPriceSpinner.getValue()).doubleValue();
        double maxPrice = ((Number) maxPriceSpinner.getValue()).doubleValue();
        double minDiscount = ((Number) minDiscountSpinner.getValue()).doubleValue();
        double maxDiscount = ((Number) maxDiscountSpinner.getValue()).doubleValue();

        logger.debug("Фільтрація букетів. Текст: '{}', Ціна: {}-{}, Знижка: {}-{}", searchText, minPrice, maxPrice, minDiscount, maxDiscount);

        return allBouquets.stream()
                .filter(bouquet -> (searchText.isEmpty() || bouquet.getName().toLowerCase().contains(searchText)) &&
                        (bouquet.calculateTotalPrice() >= minPrice && bouquet.calculateTotalPrice() <= maxPrice) &&
                        (bouquet.getDiscount() >= minDiscount && bouquet.getDiscount() <= maxDiscount))
                .collect(Collectors.toList());
    }

    /**
     * Очищає всі фільтри та оновлює таблицю.
     */
    @Override
    protected void clearFilters() {
        logger.info("Очищення фільтрів для букетів.");
        searchField.setText("");
        minPriceSpinner.setValue(0);
        maxPriceSpinner.setValue(5000);
        minDiscountSpinner.setValue(0);
        maxDiscountSpinner.setValue(50);
        applyFiltersAndRefresh();
    }

    // Операції з даними

    /**
     * Отримує список усіх букетів із DAO.
     *
     * @return список усіх букетів
     */
    @Override
    protected List<Bouquet> getAllItemsFromDAO() {
        return itemDAO.getAllBouquets();
    }

    /**
     * Отримує букет за його ідентифікатором із DAO.
     *
     * @param id ідентифікатор букета
     * @return об'єкт букета або null, якщо не знайдено
     */
    @Override
    protected Bouquet getItemByIdFromDAO(int id) {
        return itemDAO.getBouquetById(id);
    }

    /**
     * Відкриває діалог для додавання або редагування букета.
     *
     * @param e подія, що викликала діалог (може бути null)
     */
    @Override
    protected void showAddEditDialog(ActionEvent e) {
        boolean isEditMode = (e != null && e.getSource() == editButton) || (e == null && itemsTable.getSelectedRow() >= 0);
        Bouquet bouquetToEdit = null;

        if (isEditMode) {
            int selectedRow = itemsTable.getSelectedRow();
            if (selectedRow < 0) {
                JOptionPane.showMessageDialog(this, "Будь ласка, оберіть букет для редагування.", "Попередження", JOptionPane.WARNING_MESSAGE);
                return;
            }
            bouquetToEdit = getItemByModelRow(itemsTable.convertRowIndexToModel(selectedRow));
        }

        AddEditBouquetDialog dialog = new AddEditBouquetDialog(
                (JFrame) SwingUtilities.getWindowAncestor(this),
                bouquetToEdit,
                flowerDAO.getAllFlowers(),
                accessoryDAO.getAllAccessories());
        dialog.setVisible(true);

        if (dialog.isSaved()) {
            Bouquet resultBouquet = dialog.getBouquet();
            itemDAO.saveBouquet(resultBouquet);
            refreshTableData();
            selectRowByBouquetId(resultBouquet.getId());
        }
    }

    /**
     * Вибирає рядок у таблиці за ідентифікатором букета.
     *
     * @param bouquetId ідентифікатор букета
     */
    void selectRowByBouquetId(int bouquetId) {
        for (int i = 0; i < itemsTable.getRowCount(); i++) {
            int modelRow = itemsTable.convertRowIndexToModel(i);
            if (modelRow >= 0 && modelRow < tableModel.getRowCount()) {
                Bouquet itemInRow = getItemByModelRow(modelRow);
                if (itemInRow != null && itemInRow.getId() == bouquetId) {
                    itemsTable.setRowSelectionInterval(i, i);
                    itemsTable.scrollRectToVisible(itemsTable.getCellRect(i, 0, true));
                    break;
                }
            }
        }
    }

    /**
     * Видаляє вибраний букет з таблиці та бази даних.
     *
     * @param e подія, що викликала видалення
     */
    @Override
    protected void deleteSelectedItem(ActionEvent e) {
        int selectedRow = itemsTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Будь ласка, оберіть букет для видалення.", "Попередження", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Bouquet bouquetToDelete = getItemByModelRow(itemsTable.convertRowIndexToModel(selectedRow));
        if (bouquetToDelete == null) {
            logger.error("Не вдалося отримати букет для видалення за вибраним рядком.");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Ви впевнені, що хочете видалити букет \"" + bouquetToDelete.getName() + "\"?",
                "Підтвердження видалення",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            itemDAO.deleteBouquet(bouquetToDelete.getId());
            refreshTableData();
            clearDetailsPanel();
            JOptionPane.showMessageDialog(this, "Букет \"" + bouquetToDelete.getName() + "\" успішно видалено.", "Видалення завершено", JOptionPane.INFORMATION_MESSAGE);
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
        return "Деталі букета";
    }

    /**
     * Повертає детальну інформацію про букет для відображення.
     *
     * @param bouquet об'єкт букета
     * @return рядок із детальною інформацією або порожній рядок, якщо букет null
     */
    @Override
    protected String getDetailedInfoForItem(Bouquet bouquet) {
        return bouquet != null ? bouquet.getDetailedInfo() : "";
    }

    /**
     * Повертає шлях до зображення букета.
     *
     * @param bouquet об'єкт букета
     * @return шлях до зображення або null, якщо букет null
     */
    @Override
    protected String getImagePathForItem(Bouquet bouquet) {
        return bouquet != null ? bouquet.getImagePath() : null;
    }

    /**
     * Вказує, чи використовується панель рівня запасів.
     *
     * @return false, оскільки букети не мають панелі рівня запасів
     */
    @Override
    protected boolean hasStockLevelBar() {
        return false;
    }

    /**
     * Оновлює панель рівня запасів (не використовується для букетів).
     *
     * @param item об'єкт букета
     */
    @Override
    protected void updateStockLevelBar(Bouquet item) {
        // Не використовується
    }

    /**
     * Повертає назву елемента в однині.
     *
     * @return рядок із назвою елемента
     */
    @Override
    protected String getItemNameSingular() {
        return "букет";
    }
}