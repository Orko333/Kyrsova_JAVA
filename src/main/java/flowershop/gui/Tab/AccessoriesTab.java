package flowershop.gui.Tab;

import flowershop.dao.AccessoryDAO;
// import flowershop.gui.Dialog.AddEditAccessoryDialog; // Не потрібен, якщо рендерер тут
import flowershop.models.Accessory;
import flowershop.models.Accessory.AccessoryType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static flowershop.gui.Dialog.AbstractAddEditDialog.TEXT_COLOR;

/**
 * Клас AccessoriesTab представляє вкладку для керування аксесуарами.
 * Успадковує функціонал від AbstractItemTab.
 */
public class AccessoriesTab extends AbstractItemTab<Accessory, AccessoryDAO> {

    private static final Logger logger = LogManager.getLogger(AccessoriesTab.class);

    JComboBox<AccessoryType> typeFilterCombo;
    JSpinner minPriceSpinner;
    JSpinner maxPriceSpinner;
    JSpinner minStockSpinner;
    JSpinner maxStockSpinner;

    public AccessoriesTab() {
        super(new AccessoryDAO());
        logger.info("Вкладка 'Управління аксесуарами' ініціалізована.");
    }

    @Override
    protected String getTabTitle() {
        return "Управління аксесуарами";
    }

    @Override
    protected String[] getColumnNames() {
        return new String[]{"ID", "Назва", "Тип", "Колір", "Розмір", "Ціна", "На складі"};
    }

    @Override
    protected Class<?>[] getColumnClasses() {
        return new Class<?>[]{Integer.class, String.class, String.class, String.class, String.class, Double.class, Integer.class};
    }

    @Override
    protected Object[] getRowDataForItem(Accessory accessory) {
        return new Object[]{accessory.getId(), accessory.getName(), accessory.getType().getDisplayName(), accessory.getColor(), accessory.getSize(), accessory.getPrice(), accessory.getStockQuantity()};
    }

    @Override
    protected JPanel createFilterPanel() {
        logger.debug("Створення панелі фільтрів для аксесуарів.");
        JPanel filterPanel = new JPanel();
        filterPanel.setLayout(new BoxLayout(filterPanel, BoxLayout.Y_AXIS));
        filterPanel.setBackground(Color.WHITE);
        filterPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(PRIMARY_COLOR, 1), "Фільтри аксесуарів",
                        TitledBorder.LEFT, TitledBorder.TOP, TITLE_FONT, PRIMARY_COLOR),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        JPanel searchRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        searchRow.setOpaque(false);
        searchRow.add(new JLabel("Пошук:"));
        searchField = new JTextField(15);
        searchField.setPreferredSize(new Dimension(180, 28));
        searchField.getDocument().addDocumentListener(new SimpleDocumentListener(() -> { logger.trace("Зміна тексту пошуку."); applyFiltersAndRefresh(); }));
        searchRow.add(searchField);

        searchRow.add(new JLabel("Тип:"));
        typeFilterCombo = new JComboBox<>(AccessoryType.values());
        typeFilterCombo.setRenderer(new AccessoryTypeRenderer());
        typeFilterCombo.insertItemAt(null, 0);
        typeFilterCombo.setSelectedIndex(0);
        typeFilterCombo.setFont(REGULAR_FONT);
        typeFilterCombo.setPreferredSize(new Dimension(150, 28));
        typeFilterCombo.addActionListener(e -> { logger.trace("Зміна типу фільтра."); applyFiltersAndRefresh(); });
        searchRow.add(typeFilterCombo);

        JPanel rangeRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        rangeRow.setOpaque(false);

        JPanel pricePanel = createRangeFilterPanel("Ціна", 0, 10000, 10, 0, 1000);
        minPriceSpinner = (JSpinner) ((JPanel)pricePanel.getComponent(0)).getComponent(1);
        maxPriceSpinner = (JSpinner) ((JPanel)pricePanel.getComponent(0)).getComponent(3);
        minPriceSpinner.addChangeListener(e -> { logger.trace("Зміна мінімальної ціни."); applyFiltersAndRefresh(); });
        maxPriceSpinner.addChangeListener(e -> { logger.trace("Зміна максимальної ціни."); applyFiltersAndRefresh(); });

        JPanel stockPanel = createRangeFilterPanel("Кількість на складі", 0, 1000, 1, 0, 100);
        minStockSpinner = (JSpinner) ((JPanel)stockPanel.getComponent(0)).getComponent(1);
        maxStockSpinner = (JSpinner) ((JPanel)stockPanel.getComponent(0)).getComponent(3);
        minStockSpinner.addChangeListener(e -> { logger.trace("Зміна мінімальної кількості."); applyFiltersAndRefresh(); });
        maxStockSpinner.addChangeListener(e -> { logger.trace("Зміна максимальної кількості."); applyFiltersAndRefresh(); });

        clearFiltersButton = createStyledButton("Очистити", "FileChooser.deleteIcon");
        clearFiltersButton.addActionListener(e -> { logger.info("Натиснуто кнопку 'Очистити фільтри'."); clearFilters(); });

        rangeRow.add(pricePanel);
        rangeRow.add(stockPanel);
        rangeRow.add(clearFiltersButton);

        filterPanel.add(searchRow);
        filterPanel.add(rangeRow);
        logger.debug("Панель фільтрів для аксесуарів створена.");
        return filterPanel;
    }

    private JPanel createRangeFilterPanel(String title, int minVal, int maxVal, int step, int initialMin, int initialMax) {
        logger.trace("Створення панелі фільтра діапазону: '{}'", title);
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(SECONDARY_COLOR, 1), title,
                TitledBorder.LEFT, TitledBorder.TOP, REGULAR_FONT, PRIMARY_COLOR
        ));
        JPanel spinnersPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        spinnersPanel.setOpaque(false);
        spinnersPanel.add(new JLabel("Від:"));
        JSpinner minSpinner = new JSpinner(new SpinnerNumberModel(initialMin, minVal, maxVal, step));
        minSpinner.setPreferredSize(new Dimension(70, 28));
        minSpinner.setFont(REGULAR_FONT);
        spinnersPanel.add(minSpinner);
        spinnersPanel.add(new JLabel("до:"));
        JSpinner maxSpinner = new JSpinner(new SpinnerNumberModel(initialMax, minVal, maxVal, step));
        maxSpinner.setPreferredSize(new Dimension(70, 28));
        maxSpinner.setFont(REGULAR_FONT);
        spinnersPanel.add(maxSpinner);
        panel.add(spinnersPanel, BorderLayout.CENTER);
        return panel;
    }

    @Override
    protected List<Accessory> filterItems(List<Accessory> allAccessories) {
        String searchText = searchField.getText().toLowerCase().trim();
        AccessoryType selectedType = (AccessoryType) typeFilterCombo.getSelectedItem();
        double minPrice = ((Number) minPriceSpinner.getValue()).doubleValue();
        double maxPrice = ((Number) maxPriceSpinner.getValue()).doubleValue();
        int minStock = ((Number) minStockSpinner.getValue()).intValue();
        int maxStock = ((Number) maxStockSpinner.getValue()).intValue();
        logger.debug("Фільтрація аксесуарів. Текст: '{}', Тип: {}, Ціна: {}-{}, К-сть: {}-{}",
                searchText, selectedType, minPrice, maxPrice, minStock, maxStock);

        if (allAccessories == null) {
            logger.warn("Список всіх аксесуарів для фільтрації є null.");
            return new ArrayList<>();
        }

        return allAccessories.stream()
                .filter(acc -> (searchText.isEmpty() || acc.getName().toLowerCase().contains(searchText) || acc.getColor().toLowerCase().contains(searchText) || acc.getSize().toLowerCase().contains(searchText) || acc.getType().getDisplayName().toLowerCase().contains(searchText)) &&
                        (selectedType == null || acc.getType() == selectedType) &&
                        (acc.getPrice() >= minPrice && acc.getPrice() <= maxPrice) &&
                        (acc.getStockQuantity() >= minStock && acc.getStockQuantity() <= maxStock))
                .collect(Collectors.toList());
    }

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
        logger.debug("Фільтри очищено, таблицю оновлено.");
    }

    @Override
    protected String getDetailsPanelTitle() {
        return "Деталі аксесуара";
    }

    @Override
    protected String getDetailedInfoForItem(Accessory accessory) {
        if (accessory == null) {
            logger.trace("Спроба отримати деталі для null аксесуара.");
            return "";
        }
        logger.trace("Формування деталей для аксесуара ID: {}", accessory.getId());
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

    private String getTdStyle() {
        return "padding: 4px; border-bottom: 1px solid #eee; vertical-align: top;";
    }

    @Override
    protected String getImagePathForItem(Accessory accessory) {
        return accessory != null ? accessory.getImagePath() : null;
    }

    @Override
    protected boolean hasStockLevelBar() {
        return true;
    }

    @Override
    protected void updateStockLevelBar(Accessory accessory) {
        if (accessory == null || stockLevelBar == null) return;
        int stockQty = accessory.getStockQuantity();
        logger.trace("Оновлення stockLevelBar для аксесуара ID: {}, кількість: {}", accessory.getId(), stockQty);
        int maxStockDisplay = 100;
        stockLevelBar.setMaximum(maxStockDisplay);
        stockLevelBar.setValue(Math.min(stockQty, maxStockDisplay));
        stockLevelBar.setString(stockQty + " шт.");
        if (stockQty < 10) stockLevelBar.setForeground(new Color(255, 69, 0));
        else if (stockQty < 30) stockLevelBar.setForeground(new Color(255, 165, 0));
        else stockLevelBar.setForeground(PRIMARY_COLOR);
    }

    @Override
    protected String getItemNameSingular() {
        return "аксесуар";
    }

    @Override
    protected void showAddEditDialog(ActionEvent e) {
        Accessory accessoryToEdit = null;
        boolean isEditMode = (e != null && e.getSource() == editButton) || (e == null && itemsTable.getSelectedRow() >= 0);
        logger.info("Показ діалогу додавання/редагування аксесуара. Режим редагування: {}", isEditMode);

        if (isEditMode) {
            int selectedRow = itemsTable.getSelectedRow();
            if (selectedRow < 0) {
                logger.warn("Спроба редагувати, але жоден аксесуар не вибрано.");
                JOptionPane.showMessageDialog(this, "Будь ласка, оберіть аксесуар для редагування.", "Попередження", JOptionPane.WARNING_MESSAGE);
                return;
            }
            accessoryToEdit = getItemByModelRow(itemsTable.convertRowIndexToModel(selectedRow));
            logger.debug("Редагується аксесуар: {}", accessoryToEdit != null ? accessoryToEdit.getName() : "null");
        }

        flowershop.gui.Dialog.AddEditAccessoryDialog dialog = new flowershop.gui.Dialog.AddEditAccessoryDialog((JFrame) SwingUtilities.getWindowAncestor(this), accessoryToEdit);
        dialog.setVisible(true);

        if (dialog.isSaved()) {
            Accessory resultAccessory = dialog.getAccessory();
            logger.info("Діалог збережено. Аксесуар: {}", resultAccessory.getName());
            itemDAO.saveAccessory(resultAccessory);
            refreshTableData();
            for (int i = 0; i < itemsTable.getRowCount(); i++) {
                int modelRow = itemsTable.convertRowIndexToModel(i);
                if (modelRow >= 0 && modelRow < tableModel.getRowCount()) {
                    Accessory itemInRow = getItemByModelRow(modelRow);
                    if (itemInRow != null && itemInRow.getId() == resultAccessory.getId()){
                        itemsTable.setRowSelectionInterval(i, i);
                        itemsTable.scrollRectToVisible(itemsTable.getCellRect(i, 0, true));
                        logger.debug("Виділено збережений/оновлений аксесуар у таблиці: ID {}", resultAccessory.getId());
                        break;
                    }
                }
            }
        } else {
            logger.info("Діалог додавання/редагування аксесуара скасовано.");
        }
    }

    @Override
    protected void deleteSelectedItem(ActionEvent e) {
        int selectedRow = itemsTable.getSelectedRow();
        if (selectedRow < 0) {
            logger.warn("Спроба видалити, але жоден аксесуар не вибрано.");
            JOptionPane.showMessageDialog(this, "Будь ласка, оберіть аксесуар для видалення.", "Попередження", JOptionPane.WARNING_MESSAGE);
            return;
        }
        Accessory accessoryToDelete = getItemByModelRow(itemsTable.convertRowIndexToModel(selectedRow));
        if (accessoryToDelete == null) {
            logger.error("Не вдалося отримати аксесуар для видалення за вибраним рядком.");
            return;
        }
        logger.info("Запит на видалення аксесуара: {}", accessoryToDelete.getName());

        int confirm = JOptionPane.showConfirmDialog(this,
                "Ви впевнені, що хочете видалити аксесуар \"" + accessoryToDelete.getName() + "\"?",
                "Підтвердження видалення", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            logger.debug("Видалення підтверджено користувачем.");
            itemDAO.deleteAccessory(accessoryToDelete.getId());
            refreshTableData();
            clearDetailsPanel();
            JOptionPane.showMessageDialog(this, "Аксесуар \"" + accessoryToDelete.getName() + "\" успішно видалено.", "Видалення завершено", JOptionPane.INFORMATION_MESSAGE);
            logger.info("Аксесуар ID {} успішно видалено.", accessoryToDelete.getId());
        } else {
            logger.debug("Видалення скасовано користувачем.");
        }
    }

    @Override
    protected void showSortDialog(ActionEvent e) {
        logger.info("Показ діалогу сортування аксесуарів.");
        JDialog sortDialog = new JDialog((JFrame) SwingUtilities.getWindowAncestor(this), "Сортування аксесуарів", true);
        sortDialog.setLayout(new BorderLayout(10,10));
        sortDialog.setSize(350, 280);
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
        JRadioButton stockAsc = createStyledRadioButton("За кількістю (зростання)", "Table.ascendingSortIcon");
        JRadioButton stockDesc = createStyledRadioButton("За кількістю (спадання)", "Table.descendingSortIcon");

        ButtonGroup group = new ButtonGroup();
        group.add(nameAsc); group.add(nameDesc); group.add(priceAsc);
        group.add(priceDesc); group.add(stockAsc); group.add(stockDesc);

        sortOptionsPanel.add(nameAsc); sortOptionsPanel.add(nameDesc);
        sortOptionsPanel.add(priceAsc); sortOptionsPanel.add(priceDesc);
        sortOptionsPanel.add(stockAsc); sortOptionsPanel.add(stockDesc);

        TableRowSorter<?> sorter = (TableRowSorter<?>) itemsTable.getRowSorter();
        if (sorter != null && !sorter.getSortKeys().isEmpty()) {
            RowSorter.SortKey sortKey = sorter.getSortKeys().get(0);
            int columnIndex = sortKey.getColumn();
            SortOrder sortOrder = sortKey.getSortOrder();
            if (columnIndex == 1) { if (sortOrder == SortOrder.ASCENDING) nameAsc.setSelected(true); else nameDesc.setSelected(true); }
            else if (columnIndex == 5) { if (sortOrder == SortOrder.ASCENDING) priceAsc.setSelected(true); else priceDesc.setSelected(true); }
            else if (columnIndex == 6) { if (sortOrder == SortOrder.ASCENDING) stockAsc.setSelected(true); else stockDesc.setSelected(true); }
        } else { nameAsc.setSelected(true); }

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10,0));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10,0,10,10));

        JButton applyButton = createStyledButton("Застосувати", "FileChooser.approveSelectionIcon");
        applyButton.setPreferredSize(new Dimension(120, BUTTON_HEIGHT)); // Додано для консистентності
        applyButton.addActionListener(evt -> {
            List<RowSorter.SortKey> sortKeys = new ArrayList<>();
            if (nameAsc.isSelected()) { logger.debug("AccessoriesTab: Сортування за назвою (А-Я)"); sortKeys.add(new RowSorter.SortKey(1, SortOrder.ASCENDING)); }
            else if (nameDesc.isSelected()) { logger.debug("AccessoriesTab: Сортування за назвою (Я-А)"); sortKeys.add(new RowSorter.SortKey(1, SortOrder.DESCENDING)); }
            else if (priceAsc.isSelected()) { logger.debug("AccessoriesTab: Сортування за ціною (зростання)"); sortKeys.add(new RowSorter.SortKey(5, SortOrder.ASCENDING)); }
            else if (priceDesc.isSelected()) { logger.debug("AccessoriesTab: Сортування за ціною (спадання)"); sortKeys.add(new RowSorter.SortKey(5, SortOrder.DESCENDING)); }
            else if (stockAsc.isSelected()) { logger.debug("AccessoriesTab: Сортування за кількістю (зростання)"); sortKeys.add(new RowSorter.SortKey(6, SortOrder.ASCENDING)); }
            else if (stockDesc.isSelected()) { logger.debug("AccessoriesTab: Сортування за кількістю (спадання)"); sortKeys.add(new RowSorter.SortKey(6, SortOrder.DESCENDING)); }

            logger.info("Застосування сортування для AccessoriesTab: {}", sortKeys);
            itemsTable.getRowSorter().setSortKeys(sortKeys);
            sortDialog.dispose();
        });

        JButton cancelButton = createStyledButton("Скасувати", "FileChooser.cancelSelectionIcon");
        cancelButton.setPreferredSize(new Dimension(120, BUTTON_HEIGHT)); // Додано для консистентності
        cancelButton.addActionListener(evt -> { logger.debug("Діалог сортування скасовано."); sortDialog.dispose(); });

        buttonPanel.add(cancelButton);
        buttonPanel.add(applyButton);

        sortDialog.add(headerPanel, BorderLayout.NORTH);
        sortDialog.add(sortOptionsPanel, BorderLayout.CENTER);
        sortDialog.add(buttonPanel, BorderLayout.SOUTH);
        sortDialog.setVisible(true);
    }

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

    @Override
    protected List<Accessory> getAllItemsFromDAO() {
        logger.trace("Завантаження всіх аксесуарів з DAO.");
        return itemDAO.getAllAccessories();
    }

    @Override
    protected Accessory getItemByIdFromDAO(int id) {
        logger.trace("Завантаження аксесуара за ID: {} з DAO.", id);
        return itemDAO.getAccessoryById(id);
    }

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



    private static class AccessoryTypeRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value,
                                                      int index, boolean isSelected, boolean cellHasFocus) {
            JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value instanceof AccessoryType) {
                label.setText(((AccessoryType) value).getDisplayName());
            } else if (value == null && index == -1) {
                label.setText("Будь-який тип");
            } else if (value == null) {
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