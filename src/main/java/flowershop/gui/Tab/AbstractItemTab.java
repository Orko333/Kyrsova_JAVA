package flowershop.gui.Tab;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.text.DecimalFormat;
import java.util.List;
import javax.imageio.ImageIO;

/**
 * Абстрактний базовий клас для вкладок, що відображають список елементів (наприклад, аксесуарів, квітів, букетів).
 * Надає спільний функціонал для відображення, фільтрації, сортування, додавання, редагування та видалення елементів.
 *
 * @param <T> Тип елемента, що відображається у вкладці (наприклад, Accessory, Flower, Bouquet).
 * @param <D> Тип DAO (Data Access Object) для роботи з елементами типу T.
 */
public abstract class AbstractItemTab<T, D> extends JPanel {

    private static final Logger logger = LogManager.getLogger(AbstractItemTab.class);

    // Константи для стилізації UI
    protected static final Color PRIMARY_COLOR = new Color(76, 175, 80); // Основний зелений колір
    protected static final Color SECONDARY_COLOR = new Color(220, 237, 200); // Світло-зелений для фону
    protected static final Color ACCENT_COLOR = new Color(255, 152, 0); // Акцентний оранжевий
    protected static final Font TITLE_FONT = new Font("Segoe UI", Font.BOLD, 14); // Шрифт для заголовків
    protected static final Font REGULAR_FONT = new Font("Segoe UI", Font.PLAIN, 13); // Стандартний шрифт
    protected static final int BUTTON_HEIGHT = 30; // Висота кнопок

    // DAO для роботи з даними
    protected D itemDAO;

    // Елементи інтерфейсу
    protected JTable itemsTable; // Таблиця для відображення елементів
    protected DefaultTableModel tableModel; // Модель даних для таблиці
    protected JTextField searchField; // Поле для пошуку
    protected JButton addButton, editButton, deleteButton, clearFiltersButton; // Кнопки керування
    protected JButton exportButton, sortButton; // Кнопки експорту та сортування
    protected JTextPane detailsPane; // Панель для детальної інформації
    protected JLabel imageLabel; // Мітка для зображення
    protected JPanel paginationPanel; // Панель пагінації
    protected JLabel pageInfoLabel; // Мітка інформації про сторінку
    protected JProgressBar stockLevelBar; // (Опціонально) Прогрес-бар для рівня запасів

    // Параметри пагінації
    protected int currentPage = 1; // Поточна сторінка
    protected int rowsPerPage = 10; // Кількість рядків на сторінці

    // Форматування
    protected DecimalFormat priceFormat = new DecimalFormat("#,##0.00"); // Формат для цін

    /**
     * Конструктор абстрактного класу вкладки.
     *
     * @param itemDAO DAO для роботи з елементами.
     */
    public AbstractItemTab(D itemDAO) {
        this.itemDAO = itemDAO;
        logger.info("Створення вкладки типу: {}. DAO: {}", this.getClass().getSimpleName(), itemDAO.getClass().getSimpleName());
        initializeBaseUI();
    }

    /**
     * Ініціалізує базовий графічний інтерфейс користувача.
     * Нащадки можуть розширювати цей метод для додавання специфічних елементів.
     */
    protected void initializeBaseUI() {
        logger.info("Ініціалізація базового UI для вкладки '{}'", getTabTitle());
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        setBackground(Color.WHITE);

        UIManager.put("Button.background", SECONDARY_COLOR);
        UIManager.put("Button.font", REGULAR_FONT);
        UIManager.put("Label.font", REGULAR_FONT);
        UIManager.put("ComboBox.font", REGULAR_FONT);
        UIManager.put("TextField.font", REGULAR_FONT);
        UIManager.put("Table.font", REGULAR_FONT);
        UIManager.put("TableHeader.font", new Font("Segoe UI", Font.BOLD, 13));
        UIManager.put("Spinner.font", REGULAR_FONT);
        logger.debug("Глобальні налаштування стилів Swing встановлено.");

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);
        JLabel titleLabel = new JLabel(getTabTitle());
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(PRIMARY_COLOR);
        titleLabel.setBorder(new EmptyBorder(0, 0, 10, 0));
        headerPanel.add(titleLabel, BorderLayout.WEST);

        JToolBar toolBar = createToolBar();
        headerPanel.add(toolBar, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);
        logger.debug("Панель заголовка та панель інструментів створено та додано.");

        JPanel filterPanel = createFilterPanel();
        logger.debug("Панель фільтрів створена (може бути null).");

        createTable();
        JScrollPane tableScrollPane = new JScrollPane(itemsTable);
        tableScrollPane.getViewport().setBackground(Color.WHITE);
        tableScrollPane.setBorder(BorderFactory.createLineBorder(SECONDARY_COLOR, 1));
        logger.debug("Таблиця та панель прокрутки створені.");

        JPanel detailsPanel = createDetailsPanel();
        logger.debug("Панель деталей створена.");

        JSplitPane mainSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, tableScrollPane, detailsPanel);
        mainSplit.setDividerLocation(350);
        mainSplit.setContinuousLayout(true);
        mainSplit.setDividerSize(8);
        mainSplit.setBorder(null);

        JPanel contentPanel = new JPanel(new BorderLayout(0, 10));
        contentPanel.setBackground(Color.WHITE);
        if (filterPanel != null) {
            contentPanel.add(filterPanel, BorderLayout.NORTH);
            logger.debug("Панель фільтрів додана до панелі контенту.");
        }
        contentPanel.add(mainSplit, BorderLayout.CENTER);
        add(contentPanel, BorderLayout.CENTER);
        logger.debug("Основна розділена панель та панель контенту створені та додані.");

        paginationPanel = createPaginationPanel();
        add(paginationPanel, BorderLayout.SOUTH);
        logger.debug("Панель пагінації створена та додана.");

        refreshTableData();
        logger.info("Базовий UI для вкладки '{}' успішно ініціалізовано. Початкові дані завантажено.", getTabTitle());
    }

    protected JToolBar createToolBar() {
        logger.debug("Створення панелі інструментів.");
        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);
        toolBar.setOpaque(false);
        toolBar.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

        addButton = createStyledButton("Додати", "FileChooser.newFolderIcon");
        editButton = createStyledButton("Редагувати", null);
        deleteButton = createStyledButton("Видалити", "InternalFrame.closeIcon");
        exportButton = createStyledButton("Експорт", "FileChooser.floppyDriveIcon");
        sortButton = createStyledButton("Сортування", "Table.ascendingSortIcon");

        toolBar.add(addButton);
        toolBar.addSeparator(new Dimension(5, 0));
        toolBar.add(editButton);
        toolBar.addSeparator(new Dimension(5, 0));
        toolBar.add(deleteButton);
        toolBar.addSeparator(new Dimension(15, 0));
        toolBar.add(exportButton);
        toolBar.addSeparator(new Dimension(5, 0));
        toolBar.add(sortButton);

        addButton.addActionListener(e -> { logger.info("Натиснуто кнопку 'Додати'."); showAddEditDialog(e); });
        editButton.addActionListener(e -> { logger.info("Натиснуто кнопку 'Редагувати'."); showAddEditDialog(e); });
        deleteButton.addActionListener(e -> { logger.info("Натиснуто кнопку 'Видалити'."); deleteSelectedItem(e); });
        exportButton.addActionListener(e -> { logger.info("Натиснуто кнопку 'Експорт'."); exportData(e); });
        sortButton.addActionListener(e -> { logger.info("Натиснуто кнопку 'Сортування'."); showSortDialog(e); });

        logger.debug("Панель інструментів створена.");
        return toolBar;
    }

    protected JButton createStyledButton(String text, String uiManagerIconKey) {
        logger.trace("Створення стилізованої кнопки: '{}', ключ іконки: '{}'", text, uiManagerIconKey);
        JButton button = new JButton(text);
        Icon icon = null;
        if (uiManagerIconKey != null && !uiManagerIconKey.isEmpty()) {
            icon = UIManager.getIcon(uiManagerIconKey);
        }

        if (icon != null) {
            button.setIcon(icon);
        } else if (uiManagerIconKey != null && !uiManagerIconKey.isEmpty()) {
            logger.warn("Стандартна іконка не знайдена для ключа: {}. Кнопка '{}' буде без іконки.", uiManagerIconKey, text);
        }

        button.setFont(REGULAR_FONT);
        button.setFocusPainted(false);
        button.setBackground(SECONDARY_COLOR);
        button.setForeground(new Color(50, 50, 50));
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(PRIMARY_COLOR, 1),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        button.setMargin(new Insets(5, (icon != null ? 5 : 10), 5, 10));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(PRIMARY_COLOR);
                button.setForeground(Color.WHITE);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(SECONDARY_COLOR);
                button.setForeground(new Color(50, 50, 50));
            }
        });
        return button;
    }

    protected void createTable() {
        logger.debug("Створення таблиці.");
        tableModel = new DefaultTableModel(getColumnNames(), 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return getColumnClasses()[columnIndex];
            }
        };

        itemsTable = new JTable(tableModel);
        itemsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        itemsTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                logger.trace("Зміна вибору в таблиці.");
                updateDetailsPanel();
            }
        });
        itemsTable.setAutoCreateRowSorter(true);
        itemsTable.setRowHeight(28);
        itemsTable.setShowGrid(true);
        itemsTable.setGridColor(new Color(230, 230, 230));
        itemsTable.setIntercellSpacing(new Dimension(1, 1));
        itemsTable.setFillsViewportHeight(true);
        itemsTable.setBackground(Color.WHITE);
        itemsTable.setSelectionBackground(PRIMARY_COLOR);
        itemsTable.setSelectionForeground(Color.WHITE);

        JTableHeader header = itemsTable.getTableHeader();
        header.setBackground(SECONDARY_COLOR);
        header.setForeground(new Color(50, 50, 50));
        header.setFont(new Font("Segoe UI", Font.BOLD, 13));
        header.setBorder(BorderFactory.createLineBorder(PRIMARY_COLOR, 1));
        header.setReorderingAllowed(false);

        itemsTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                    logger.info("Подвійний клік на таблиці, виклик діалогу редагування.");
                    showAddEditDialog(null);
                }
            }
        });

        configureTableColumnWidths();
        logger.debug("Таблиця успішно створена та налаштована.");
    }

    protected JPanel createDetailsPanel() {
        logger.debug("Створення панелі деталей.");
        JPanel detailsPanel = new JPanel(new BorderLayout(10, 0));
        detailsPanel.setBackground(Color.WHITE);
        detailsPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(PRIMARY_COLOR, 1),
                        getDetailsPanelTitle(),
                        TitledBorder.LEFT, TitledBorder.TOP, TITLE_FONT, PRIMARY_COLOR
                ),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        detailsPane = new JTextPane();
        detailsPane.setContentType("text/html");
        detailsPane.setEditable(false);
        detailsPane.setBackground(Color.WHITE);
        detailsPane.setBorder(null);

        JScrollPane detailsScroll = new JScrollPane(detailsPane);
        detailsScroll.setBorder(BorderFactory.createLineBorder(SECONDARY_COLOR, 1));
        detailsScroll.setPreferredSize(new Dimension(400, 200));

        JPanel imageAndExtraPanel = new JPanel(new BorderLayout(0, 10));
        imageAndExtraPanel.setOpaque(false);

        JPanel imagePanel = new JPanel(new BorderLayout());
        imagePanel.setBackground(Color.WHITE);
        imagePanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(SECONDARY_COLOR, 1),
                        "Зображення", TitledBorder.LEFT, TitledBorder.TOP, REGULAR_FONT, PRIMARY_COLOR
                ),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));

        imageLabel = new JLabel();
        imageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        imageLabel.setVerticalAlignment(SwingConstants.CENTER);
        imageLabel.setPreferredSize(new Dimension(200, 150));
        imageLabel.setText("Немає обраного елемента");
        imagePanel.add(imageLabel, BorderLayout.CENTER);
        imageAndExtraPanel.add(imagePanel, BorderLayout.CENTER);

        if (hasStockLevelBar()) {
            logger.debug("Додавання прогрес-бару рівня запасів до панелі деталей.");
            JPanel statsPanel = new JPanel(new BorderLayout());
            statsPanel.setBackground(Color.WHITE);
            statsPanel.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createTitledBorder(
                            BorderFactory.createLineBorder(SECONDARY_COLOR, 1),
                            "Рівень запасів", TitledBorder.LEFT, TitledBorder.TOP, REGULAR_FONT, PRIMARY_COLOR
                    ),
                    BorderFactory.createEmptyBorder(10, 10, 10, 10)
            ));
            stockLevelBar = new JProgressBar(0, 100);
            stockLevelBar.setStringPainted(true);
            stockLevelBar.setFont(REGULAR_FONT);
            stockLevelBar.setBackground(Color.WHITE);
            stockLevelBar.setForeground(PRIMARY_COLOR);
            stockLevelBar.setBorderPainted(true);
            stockLevelBar.setBorder(BorderFactory.createLineBorder(SECONDARY_COLOR, 1));
            statsPanel.add(stockLevelBar, BorderLayout.CENTER);
            imageAndExtraPanel.add(statsPanel, BorderLayout.SOUTH);
        }

        detailsPanel.add(detailsScroll, BorderLayout.CENTER);
        detailsPanel.add(imageAndExtraPanel, BorderLayout.EAST);
        logger.debug("Панель деталей створена.");
        return detailsPanel;
    }

    protected JPanel createPaginationPanel() {
        logger.debug("Створення панелі пагінації.");
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, SECONDARY_COLOR));

        JButton firstButton = createPaginationButton("<<", "SplitPane.leftArrowIcon");
        JButton prevButton = createPaginationButton("<", "SplitPane.leftArrowIcon");
        pageInfoLabel = new JLabel("Сторінка 1");
        pageInfoLabel.setFont(REGULAR_FONT);
        pageInfoLabel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
        JButton nextButton = createPaginationButton(">", "SplitPane.rightArrowIcon");
        JButton lastButton = createPaginationButton(">>", "SplitPane.rightArrowIcon");

        firstButton.addActionListener(e -> {
            logger.debug("Натиснуто кнопку пагінації 'Перша сторінка'.");
            if (currentPage > 1) {
                currentPage = 1;
                applyFiltersAndRefresh();
            }
        });
        prevButton.addActionListener(e -> {
            logger.debug("Натиснуто кнопку пагінації 'Попередня сторінка'.");
            if (currentPage > 1) {
                currentPage--;
                applyFiltersAndRefresh();
            }
        });
        nextButton.addActionListener(e -> {
            logger.debug("Натиснуто кнопку пагінації 'Наступна сторінка'.");
            List<T> allItems = getAllItemsFromDAO();
            List<T> filteredItems = filterItems(allItems);
            int totalPages = (int) Math.ceil((double) filteredItems.size() / rowsPerPage);
            if (totalPages == 0) totalPages = 1;
            if (currentPage < totalPages) {
                currentPage++;
                applyFiltersAndRefresh();
            }
        });
        lastButton.addActionListener(e -> {
            logger.debug("Натиснуто кнопку пагінації 'Остання сторінка'.");
            List<T> allItems = getAllItemsFromDAO();
            List<T> filteredItems = filterItems(allItems);
            int totalPages = (int) Math.ceil((double) filteredItems.size() / rowsPerPage);
            if (totalPages == 0) totalPages = 1;
            if (currentPage < totalPages) {
                currentPage = totalPages;
                applyFiltersAndRefresh();
            }
        });

        panel.add(firstButton);
        panel.add(prevButton);
        panel.add(pageInfoLabel);
        panel.add(nextButton);
        panel.add(lastButton);
        logger.debug("Панель пагінації створена.");
        return panel;
    }

    protected JButton createPaginationButton(String text, String uiManagerIconKey) {
        logger.trace("Створення кнопки пагінації: '{}', ключ іконки: '{}'", text, uiManagerIconKey);
        JButton button = new JButton();
        Icon icon = null;

        if (uiManagerIconKey != null && !uiManagerIconKey.isEmpty()) {
            icon = UIManager.getIcon(uiManagerIconKey);
        }

        if (icon != null) {
            button.setIcon(icon);
            button.setToolTipText(text);
        } else {
            button.setText(text);
            if (uiManagerIconKey != null && !uiManagerIconKey.isEmpty()){
                logger.warn("Стандартна іконка пагінації не знайдена для ключа: {}. Використовується текст: {}", uiManagerIconKey, text);
            }
        }

        button.setFont(REGULAR_FONT);
        button.setFocusPainted(false);
        button.setBackground(SECONDARY_COLOR);
        button.setForeground(new Color(50, 50, 50));
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(PRIMARY_COLOR, 1),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        button.setPreferredSize(new Dimension(icon != null ? 45 : button.getPreferredSize().width, BUTTON_HEIGHT));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(PRIMARY_COLOR);
                button.setForeground(Color.WHITE);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(SECONDARY_COLOR);
                button.setForeground(new Color(50, 50, 50));
            }
        });
        return button;
    }

    protected void refreshTableData() {
        logger.info("Оновлення даних у таблиці для вкладки '{}'.", getTabTitle());
        // List<T> allItems = getAllItemsFromDAO(); // Отримується в applyFiltersAndPagination
        tableModel.setRowCount(0);
        applyFiltersAndPagination();
        logger.debug("Дані в таблиці оновлено.");
    }

    protected void applyFiltersAndRefresh() {
        logger.debug("Застосування фільтрів та оновлення таблиці.");
        applyFiltersAndPagination();
    }

    private void applyFiltersAndPagination() {
        logger.debug("Застосування фільтрів та пагінації. Поточна сторінка: {}", currentPage);
        List<T> allItems = getAllItemsFromDAO();
        logger.trace("Завантажено {} всіх елементів з DAO.", allItems.size());
        List<T> filteredItems = filterItems(allItems);
        logger.trace("Після фільтрації залишилося {} елементів.", filteredItems.size());

        int totalFilteredRows = filteredItems.size();
        int totalPages = (int) Math.ceil((double) totalFilteredRows / rowsPerPage);
        if (totalPages == 0) totalPages = 1;

        if (currentPage > totalPages) currentPage = totalPages;
        if (currentPage < 1) currentPage = 1;
        logger.trace("Розраховано сторінок: {}, скоригована поточна сторінка: {}", totalPages, currentPage);

        tableModel.setRowCount(0);

        int startIdx = (currentPage - 1) * rowsPerPage;
        int endIdx = Math.min(startIdx + rowsPerPage, totalFilteredRows);
        logger.trace("Відображення елементів з {} по {} (включно).", startIdx, endIdx -1 );

        for (int i = startIdx; i < endIdx; i++) {
            tableModel.addRow(getRowDataForItem(filteredItems.get(i)));
        }
        updatePaginationLabel(totalFilteredRows);
        updateDetailsPanel();
        logger.debug("Фільтрація та пагінація застосовані. Таблиця оновлена.");
    }

    protected void updatePaginationLabel(int totalFilteredItems) {
        int totalPages = (int) Math.ceil((double) totalFilteredItems / rowsPerPage);
        if (totalPages == 0) totalPages = 1;

        if (currentPage > totalPages) currentPage = totalPages;
        if (currentPage < 1) currentPage = 1;

        int startRow = totalFilteredItems == 0 ? 0 : (currentPage - 1) * rowsPerPage + 1;
        int endRow = Math.min(currentPage * rowsPerPage, totalFilteredItems);

        String labelText;
        if (totalFilteredItems == 0) {
            labelText = "Немає записів";
        } else {
            labelText = String.format("Сторінка %d з %d (записи %d-%d з %d)",
                    currentPage, totalPages, startRow, endRow, totalFilteredItems);
        }
        pageInfoLabel.setText(labelText);
        logger.trace("Мітка пагінації оновлена: {}", labelText);
    }

    protected void updateDetailsPanel() {
        int selectedRow = itemsTable.getSelectedRow();
        logger.trace("Оновлення панелі деталей. Вибраний рядок у View: {}", selectedRow);
        if (selectedRow >= 0 && selectedRow < itemsTable.getRowCount()) {
            int modelRow = itemsTable.convertRowIndexToModel(selectedRow);
            logger.trace("Індекс вибраного рядка в Model: {}", modelRow);
            T selectedItem = getItemByModelRow(modelRow);

            if (selectedItem != null) {
                logger.debug("Відображення деталей для елемента: {}", selectedItem.toString());
                detailsPane.setText(getDetailedInfoForItem(selectedItem));
                detailsPane.setCaretPosition(0);

                String imagePath = getImagePathForItem(selectedItem);
                logger.trace("Шлях до зображення для деталей: {}", imagePath);
                try {
                    if (imagePath != null && !imagePath.isEmpty()) {
                        File imageFile = new File(imagePath);
                        if (imageFile.exists()) {
                            BufferedImage img = ImageIO.read(imageFile);
                            int panelWidth = imageLabel.getWidth() > 0 ? imageLabel.getWidth() - 10 : 190;
                            int panelHeight = imageLabel.getHeight() > 0 ? imageLabel.getHeight() - 10 : 140;
                            int originalWidth = img.getWidth();
                            int originalHeight = img.getHeight();
                            int newWidth = originalWidth;
                            int newHeight = originalHeight;

                            if (originalWidth > 0 && originalHeight > 0) { // Захист від некоректних зображень
                                if (originalWidth > panelWidth) {
                                    newWidth = panelWidth;
                                    newHeight = (newWidth * originalHeight) / originalWidth;
                                }
                                if (newHeight > panelHeight) {
                                    newHeight = panelHeight;
                                    newWidth = (newHeight * originalWidth) / originalHeight;
                                }
                                Image scaledImg = img.getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH);
                                imageLabel.setIcon(new ImageIcon(scaledImg));
                                imageLabel.setText(null);
                                logger.debug("Зображення '{}' успішно завантажено та відображено.", imagePath);
                            } else {
                                logger.warn("Некоректні розміри зображення (ширина або висота <= 0): {}", imagePath);
                                imageLabel.setIcon(null);
                                imageLabel.setText("<html><center>Помилка<br>зображення</center></html>");
                            }
                        } else {
                            imageLabel.setIcon(null);
                            imageLabel.setText("<html><center>Зображення<br>не знайдено</center></html>");
                            logger.warn("Файл зображення не знайдено: {}", imagePath);
                        }
                    } else {
                        imageLabel.setIcon(null);
                        imageLabel.setText("<html><center>Зображення<br>відсутнє</center></html>");
                        logger.trace("Шлях до зображення порожній або null.");
                    }
                } catch (Exception e) {
                    imageLabel.setIcon(null);
                    imageLabel.setText("<html><center>Помилка<br>завантаження</center></html>");
                    logger.error("Помилка завантаження зображення '{}': {}", imagePath, e.getMessage(), e);
                }

                if (hasStockLevelBar() && stockLevelBar != null) {
                    updateStockLevelBar(selectedItem);
                }
            } else {
                logger.warn("Не вдалося отримати елемент для рядка моделі: {}", modelRow);
                clearDetailsPanel();
            }
        } else {
            logger.trace("Жоден рядок не вибрано, очищення панелі деталей.");
            clearDetailsPanel();
        }
    }

    protected void clearDetailsPanel() {
        detailsPane.setText(String.format("<html><body style='font-family:Segoe UI; padding:10px;'>Оберіть %s для перегляду деталей</body></html>", getItemNameSingular()));
        imageLabel.setIcon(null);
        imageLabel.setText(String.format("<html><center>Немає обраного<br>%s</center></html>", getItemNameSingular()));
        if (hasStockLevelBar() && stockLevelBar != null) {
            stockLevelBar.setValue(0);
            stockLevelBar.setString("0 шт.");
            stockLevelBar.setForeground(PRIMARY_COLOR);
        }
        logger.debug("Панель деталей очищено.");
    }

    protected String colorToHex(Color color) {
        return String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue());
    }

    protected void exportData(ActionEvent e) {
        logger.info("Ініціювання експорту даних у CSV.");
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Експорт даних у CSV");
        fileChooser.setFileFilter(new FileNameExtensionFilter("CSV файли (*.csv)", "csv"));
        fileChooser.setCurrentDirectory(new File(System.getProperty("user.home")));

        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            if (!file.getName().toLowerCase().endsWith(".csv")) {
                file = new File(file.getAbsolutePath() + ".csv");
            }
            logger.info("Файл для експорту обрано: {}", file.getAbsolutePath());

            File finalFile = file;
            SwingWorker<Void, Integer> worker = new SwingWorker<>() {
                @Override
                protected Void doInBackground() throws Exception {
                    logger.debug("Початок фонового завдання експорту.");
                    StringBuilder csv = new StringBuilder();
                    String[] columns = getColumnNames();
                    for (int i = 0; i < columns.length; i++) {
                        csv.append(escapeCSV(columns[i]));
                        if (i < columns.length - 1) csv.append(",");
                    }
                    csv.append("\n");

                    List<T> itemsToExport = getAllItemsFromDAO();
                    int rowCount = itemsToExport.size();
                    logger.debug("Експортується {} рядків.", rowCount);

                    for (int i = 0; i < rowCount; i++) {
                        Object[] rowData = getRowDataForItem(itemsToExport.get(i));
                        for (int j = 0; j < rowData.length; j++) {
                            csv.append(escapeCSV(rowData[j] != null ? rowData[j].toString() : ""));
                            if (j < rowData.length - 1) csv.append(",");
                        }
                        csv.append("\n");
                    }
                    java.nio.file.Files.writeString(finalFile.toPath(), csv.toString(), java.nio.charset.StandardCharsets.UTF_8);
                    logger.info("Дані успішно записані у файл: {}", finalFile.getAbsolutePath());
                    return null;
                }

                @Override
                protected void done() {
                    try {
                        get(); // Перевірка на помилки з doInBackground
                        JOptionPane.showMessageDialog(AbstractItemTab.this,
                                "Дані успішно експортовано до:\n" + finalFile.getAbsolutePath(),
                                "Експорт завершено", JOptionPane.INFORMATION_MESSAGE);
                        logger.info("Експорт даних успішно завершено.");
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(AbstractItemTab.this,
                                "Помилка при експорті даних: " + ex.getMessage(),
                                "Помилка експорту", JOptionPane.ERROR_MESSAGE);
                        logger.error("Помилка під час експорту даних: {}", ex.getMessage(), ex);
                    }
                }
            };
            worker.execute();
        } else {
            logger.debug("Експорт даних скасовано користувачем.");
        }
    }

    protected String escapeCSV(String value) {
        if (value == null) return "";
        String result = value.replace("\"", "\"\"");
        if (result.contains(",") || result.contains("\"") || result.contains("\n") || result.contains("\r")) {
            result = "\"" + result + "\"";
        }
        return result;
    }

    protected abstract String getTabTitle();
    protected abstract String[] getColumnNames();
    protected abstract Class<?>[] getColumnClasses();
    protected abstract Object[] getRowDataForItem(T item);
    protected abstract JPanel createFilterPanel();
    protected abstract List<T> filterItems(List<T> allItems);
    protected abstract void clearFilters();
    protected abstract String getDetailsPanelTitle();
    protected abstract String getDetailedInfoForItem(T item);
    protected abstract String getImagePathForItem(T item);
    protected abstract boolean hasStockLevelBar();
    protected abstract void updateStockLevelBar(T item);
    protected abstract String getItemNameSingular();
    protected abstract void showAddEditDialog(ActionEvent e);
    protected abstract void deleteSelectedItem(ActionEvent e);
    protected abstract void showSortDialog(ActionEvent e);
    protected abstract List<T> getAllItemsFromDAO();
    protected abstract T getItemByIdFromDAO(int id);

    protected T getItemByModelRow(int modelRow) {
        logger.trace("Спроба отримати елемент за індексом моделі: {}", modelRow);
        if (modelRow >= 0 && modelRow < tableModel.getRowCount()) {
            Object idObj = tableModel.getValueAt(modelRow, 0);
            if (idObj instanceof Integer) {
                int itemId = (Integer) idObj;
                logger.trace("ID елемента: {}", itemId);
                return getItemByIdFromDAO(itemId);
            } else {
                logger.warn("ID елемента в таблиці ({}) не є Integer. Тип: {}", idObj, (idObj != null ? idObj.getClass().getName() : "null"));
            }
        } else {
            logger.warn("Індекс рядка моделі ({}) поза межами таблиці (0-{}).", modelRow, tableModel.getRowCount() -1);
        }
        return null;
    }

    protected void configureTableColumnWidths() {
        logger.debug("Налаштування ширини стовпців таблиці.");
        if (itemsTable.getColumnModel().getColumnCount() > 0) {
            itemsTable.getColumnModel().getColumn(0).setPreferredWidth(40);
            itemsTable.getColumnModel().getColumn(0).setMinWidth(30);
            itemsTable.getColumnModel().getColumn(0).setMaxWidth(80);
            logger.trace("Ширину стовпця ID встановлено.");
        }
    }

    protected static class SimpleDocumentListener implements javax.swing.event.DocumentListener {
        private final Runnable updateAction;
        public SimpleDocumentListener(Runnable updateAction) { this.updateAction = updateAction; }
        @Override public void insertUpdate(javax.swing.event.DocumentEvent e) { updateAction.run(); }
        @Override public void removeUpdate(javax.swing.event.DocumentEvent e) { updateAction.run(); }
        @Override public void changedUpdate(javax.swing.event.DocumentEvent e) { updateAction.run(); }
    }
}