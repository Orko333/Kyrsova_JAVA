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
    protected static final Color PRIMARY_COLOR = new Color(76, 175, 80);
    protected static final Color SECONDARY_COLOR = new Color(220, 237, 200);
    protected static final Color ACCENT_COLOR = new Color(255, 152, 0);
    protected static final Font TITLE_FONT = new Font("Segoe UI", Font.BOLD, 14);
    protected static final Font REGULAR_FONT = new Font("Segoe UI", Font.PLAIN, 13);
    protected static final int BUTTON_HEIGHT = 30;

    // DAO для роботи з даними
    protected final D itemDAO;

    // Елементи інтерфейсу
    protected JTable itemsTable;
    protected DefaultTableModel tableModel;
    protected JTextField searchField;
    protected JButton addButton, editButton, deleteButton, clearFiltersButton, exportButton;
    protected JTextPane detailsPane;
    protected JLabel imageLabel;
    protected JPanel paginationPanel;
    protected JLabel pageInfoLabel;
    protected JProgressBar stockLevelBar;

    // Параметри пагінації
    protected int currentPage = 1;
    protected int rowsPerPage = 10;

    // Форматування
    protected final DecimalFormat priceFormat = new DecimalFormat("#,##0.00");

    /**
     * Конструктор абстрактного класу вкладки.
     *
     * @param itemDAO DAO для роботи з елементами
     */
    public AbstractItemTab(D itemDAO) {
        this.itemDAO = itemDAO;
        logger.info("Створення вкладки типу: {}. DAO: {}", this.getClass().getSimpleName(), itemDAO.getClass().getSimpleName());
        initializeBaseUI();
    }

    // Ініціалізація UI

    /**
     * Ініціалізує базовий графічний інтерфейс користувача.
     * Встановлює стилі, створює заголовок, панель інструментів, таблицю, панель деталей і пагінацію.
     */
    protected void initializeBaseUI() {
        logger.info("Ініціалізація базового UI для вкладки '{}'", getTabTitle());
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        setBackground(Color.WHITE);

        configureGlobalStyles();
        add(createHeaderPanel(), BorderLayout.NORTH);
        JPanel contentPanel = createContentPanel();
        add(contentPanel, BorderLayout.CENTER);
        add(createPaginationPanel(), BorderLayout.SOUTH);

        refreshTableData();
        logger.info("Базовий UI для вкладки '{}' успішно ініціалізовано.", getTabTitle());
    }

    /**
     * Налаштовує глобальні стилі для компонентів Swing.
     */
    private void configureGlobalStyles() {
        UIManager.put("Button.background", SECONDARY_COLOR);
        UIManager.put("Button.font", REGULAR_FONT);
        UIManager.put("Label.font", REGULAR_FONT);
        UIManager.put("ComboBox.font", REGULAR_FONT);
        UIManager.put("TextField.font", REGULAR_FONT);
        UIManager.put("Table.font", REGULAR_FONT);
        UIManager.put("TableHeader.font", new Font("Segoe UI", Font.BOLD, 13));
        UIManager.put("Spinner.font", REGULAR_FONT);
        logger.debug("Глобальні налаштування стилів Swing встановлено.");
    }

    /**
     * Створює панель заголовка з назвою вкладки та панеллю інструментів.
     *
     * @return панель заголовка
     */
    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);

        JLabel titleLabel = new JLabel(getTabTitle());
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(PRIMARY_COLOR);
        titleLabel.setBorder(new EmptyBorder(0, 0, 10, 0));
        headerPanel.add(titleLabel, BorderLayout.WEST);

        headerPanel.add(createToolBar(), BorderLayout.EAST);
        return headerPanel;
    }

    /**
     * Створює панель контенту з фільтрами, таблицею та деталями.
     *
     * @return панель контенту
     */
    private JPanel createContentPanel() {
        JPanel contentPanel = new JPanel(new BorderLayout(0, 10));
        contentPanel.setBackground(Color.WHITE);

        JPanel filterPanel = createFilterPanel();
        if (filterPanel != null) {
            contentPanel.add(filterPanel, BorderLayout.NORTH);
        }

        createTable();
        JScrollPane tableScrollPane = new JScrollPane(itemsTable);
        tableScrollPane.getViewport().setBackground(Color.WHITE);
        tableScrollPane.setBorder(BorderFactory.createLineBorder(SECONDARY_COLOR, 1));

        JSplitPane mainSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, tableScrollPane, createDetailsPanel());
        mainSplit.setDividerLocation(350);
        mainSplit.setContinuousLayout(true);
        mainSplit.setDividerSize(8);
        mainSplit.setBorder(null);

        contentPanel.add(mainSplit, BorderLayout.CENTER);
        return contentPanel;
    }

    // Панель інструментів

    /**
     * Створює панель інструментів із кнопками для додавання, редагування, видалення та експорту.
     *
     * @return панель інструментів
     */
    protected JToolBar createToolBar() {
        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);
        toolBar.setOpaque(false);
        toolBar.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

        addButton = createStyledButton("Додати", "FileChooser.newFolderIcon");
        editButton = createStyledButton("Редагувати", null);
        deleteButton = createStyledButton("Видалити", "InternalFrame.closeIcon");

        toolBar.add(addButton);
        toolBar.addSeparator(new Dimension(5, 0));
        toolBar.add(editButton);
        toolBar.addSeparator(new Dimension(5, 0));
        toolBar.add(deleteButton);
        toolBar.addSeparator(new Dimension(15, 0));

        addButton.addActionListener(e -> showAddEditDialog(e));
        editButton.addActionListener(e -> showAddEditDialog(e));
        deleteButton.addActionListener(e -> deleteSelectedItem(e));

        return toolBar;
    }

    /**
     * Створює стилізовану кнопку з текстом та іконкою.
     *
     * @param text           текст кнопки
     * @param uiManagerIconKey ключ іконки з UIManager
     * @return стилізована кнопка
     */
    protected JButton createStyledButton(String text, String uiManagerIconKey) {
        JButton button = new JButton(text);
        Icon icon = uiManagerIconKey != null ? UIManager.getIcon(uiManagerIconKey) : null;

        if (icon != null) {
            button.setIcon(icon);
        } else if (uiManagerIconKey != null) {
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
        button.setMargin(new Insets(5, icon != null ? 5 : 10, 5, 10));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(PRIMARY_COLOR);
                button.setForeground(Color.WHITE);
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(SECONDARY_COLOR);
                button.setForeground(new Color(50, 50, 50));
            }
        });
        return button;
    }

    // Таблиця

    /**
     * Створює таблицю для відображення елементів із заданими стовпцями та стилем.
     */
    protected void createTable() {
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
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                    showAddEditDialog(null);
                }
            }
        });

        configureTableColumnWidths();
    }

    /**
     * Налаштовує ширину стовпців таблиці. За замовчуванням встановлює ширину для стовпця ID.
     * Нащадки можуть перевизначити для специфічного налаштування.
     */
    protected void configureTableColumnWidths() {
        if (itemsTable.getColumnModel().getColumnCount() > 0) {
            itemsTable.getColumnModel().getColumn(0).setPreferredWidth(40);
            itemsTable.getColumnModel().getColumn(0).setMinWidth(30);
            itemsTable.getColumnModel().getColumn(0).setMaxWidth(80);
        }
    }

    // Панель деталей

    /**
     * Створює панель деталей для відображення інформації про вибраний елемент.
     *
     * @return панель деталей
     */
    protected JPanel createDetailsPanel() {
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
        return detailsPanel;
    }

    // Пагінація

    /**
     * Створює панель пагінації з кнопками для навігації між сторінками.
     *
     * @return панель пагінації
     */
    protected JPanel createPaginationPanel() {
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
            if (currentPage > 1) {
                currentPage = 1;
                applyFiltersAndRefresh();
            }
        });
        prevButton.addActionListener(e -> {
            if (currentPage > 1) {
                currentPage--;
                applyFiltersAndRefresh();
            }
        });
        nextButton.addActionListener(e -> {
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
        return panel;
    }

    /**
     * Створює кнопку пагінації з іконкою або текстом.
     *
     * @param text           текст кнопки
     * @param uiManagerIconKey ключ іконки з UIManager
     * @return кнопка пагінації
     */
    protected JButton createPaginationButton(String text, String uiManagerIconKey) {
        JButton button = new JButton();
        Icon icon = uiManagerIconKey != null ? UIManager.getIcon(uiManagerIconKey) : null;

        if (icon != null) {
            button.setIcon(icon);
            button.setToolTipText(text);
        } else {
            button.setText(text);
            if (uiManagerIconKey != null) {
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
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(PRIMARY_COLOR);
                button.setForeground(Color.WHITE);
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(SECONDARY_COLOR);
                button.setForeground(new Color(50, 50, 50));
            }
        });
        return button;
    }

    // Оновлення даних

    /**
     * Оновлює дані в таблиці, застосовуючи фільтри та пагінацію.
     */
    protected void refreshTableData() {
        tableModel.setRowCount(0);
        applyFiltersAndPagination();
    }

    /**
     * Застосовує фільтри та оновлює таблицю.
     */
    protected void applyFiltersAndRefresh() {
        applyFiltersAndPagination();
    }

    /**
     * Застосовує фільтри та пагінацію до даних таблиці.
     */
    private void applyFiltersAndPagination() {
        List<T> allItems = getAllItemsFromDAO();
        List<T> filteredItems = filterItems(allItems);

        int totalFilteredRows = filteredItems.size();
        int totalPages = (int) Math.ceil((double) totalFilteredRows / rowsPerPage);
        if (totalPages == 0) totalPages = 1;

        if (currentPage > totalPages) currentPage = totalPages;
        if (currentPage < 1) currentPage = 1;

        tableModel.setRowCount(0);

        int startIdx = (currentPage - 1) * rowsPerPage;
        int endIdx = Math.min(startIdx + rowsPerPage, totalFilteredRows);

        for (int i = startIdx; i < endIdx; i++) {
            tableModel.addRow(getRowDataForItem(filteredItems.get(i)));
        }
        updatePaginationLabel(totalFilteredRows);
        updateDetailsPanel();
    }

    /**
     * Оновлює мітку пагінації з інформацією про поточну сторінку та кількість записів.
     *
     * @param totalFilteredItems кількість відфільтрованих елементів
     */
    protected void updatePaginationLabel(int totalFilteredItems) {
        int totalPages = (int) Math.ceil((double) totalFilteredItems / rowsPerPage);
        if (totalPages == 0) totalPages = 1;

        if (currentPage > totalPages) currentPage = totalPages;
        if (currentPage < 1) currentPage = 1;

        int startRow = totalFilteredItems == 0 ? 0 : (currentPage - 1) * rowsPerPage + 1;
        int endRow = Math.min(currentPage * rowsPerPage, totalFilteredItems);

        String labelText = totalFilteredItems == 0
                ? "Немає записів"
                : String.format("Сторінка %d з %d (записи %d-%d з %d)", currentPage, totalPages, startRow, endRow, totalFilteredItems);
        pageInfoLabel.setText(labelText);
    }

    // Панель деталей

    /**
     * Оновлює панель деталей для відображення інформації про вибраний елемент.
     */
    protected void updateDetailsPanel() {
        int selectedRow = itemsTable.getSelectedRow();
        if (selectedRow >= 0 && selectedRow < itemsTable.getRowCount()) {
            int modelRow = itemsTable.convertRowIndexToModel(selectedRow);
            T selectedItem = getItemByModelRow(modelRow);

            if (selectedItem != null) {
                detailsPane.setText(getDetailedInfoForItem(selectedItem));
                detailsPane.setCaretPosition(0);

                String imagePath = getImagePathForItem(selectedItem);
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

                            if (originalWidth > 0 && originalHeight > 0) {
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
                            } else {
                                imageLabel.setIcon(null);
                                imageLabel.setText("<html><center>Помилка<br>зображення</center></html>");
                            }
                        } else {
                            imageLabel.setIcon(null);
                            imageLabel.setText("<html><center>Зображення<br>не знайдено</center></html>");
                        }
                    } else {
                        imageLabel.setIcon(null);
                        imageLabel.setText("<html><center>Зображення<br>відсутнє</center></html>");
                    }
                } catch (Exception e) {
                    imageLabel.setIcon(null);
                    imageLabel.setText("<html><center>Помилка<br>завантаження</center></html>");
                    logger.error("Помилка завантаження зображення '{}': {}", imagePath, e.getMessage());
                }

                if (hasStockLevelBar() && stockLevelBar != null) {
                    updateStockLevelBar(selectedItem);
                }
            } else {
                clearDetailsPanel();
            }
        } else {
            clearDetailsPanel();
        }
    }

    /**
     * Очищає панель деталей, відображаючи повідомлення про відсутність вибраного елемента.
     */
    protected void clearDetailsPanel() {
        detailsPane.setText(String.format("<html><body style='font-family:Segoe UI; padding:10px;'>Оберіть %s для перегляду деталей</body></html>", getItemNameSingular()));
        imageLabel.setIcon(null);
        imageLabel.setText(String.format("<html><center>Немає обраного<br>%s</center></html>", getItemNameSingular()));
        if (hasStockLevelBar() && stockLevelBar != null) {
            stockLevelBar.setValue(0);
            stockLevelBar.setString("0 шт.");
            stockLevelBar.setForeground(PRIMARY_COLOR);
        }
    }

    /**
     * Перетворює колір у шістнадцятковий формат (#RRGGBB).
     *
     * @param color колір
     * @return шістнадцятковий код кольору
     */
    protected String colorToHex(Color color) {
        return String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue());
    }

    // Абстрактні методи

    /**
     * Повертає заголовок вкладки.
     *
     * @return рядок із назвою вкладки
     */
    protected abstract String getTabTitle();

    /**
     * Повертає назви стовпців таблиці.
     *
     * @return масив рядків із назвами стовпців
     */
    protected abstract String[] getColumnNames();

    /**
     * Повертає класи даних для стовпців таблиці.
     *
     * @return масив класів, що відповідають типам даних стовпців
     */
    protected abstract Class<?>[] getColumnClasses();

    /**
     * Формує дані для рядка таблиці на основі об'єкта елемента.
     *
     * @param item об'єкт елемента
     * @return масив об'єктів із даними для відображення в таблиці
     */
    protected abstract Object[] getRowDataForItem(T item);

    /**
     * Створює панель фільтрів для пошуку та фільтрації елементів.
     *
     * @return панель із компонентами фільтрів або null, якщо фільтри не потрібні
     */
    protected abstract JPanel createFilterPanel();

    /**
     * Фільтрує список елементів на основі заданих критеріїв.
     *
     * @param allItems повний список елементів
     * @return відфільтрований список елементів
     */
    protected abstract List<T> filterItems(List<T> allItems);

    /**
     * Очищає всі фільтри та оновлює таблицю.
     */
    protected abstract void clearFilters();

    /**
     * Повертає заголовок панелі деталей.
     *
     * @return рядок із назвою панелі деталей
     */
    protected abstract String getDetailsPanelTitle();

    /**
     * Повертає детальну інформацію про елемент для відображення.
     *
     * @param item об'єкт елемента
     * @return рядок із детальною інформацією або порожній рядок, якщо елемент null
     */
    protected abstract String getDetailedInfoForItem(T item);

    /**
     * Повертає шлях до зображення елемента.
     *
     * @param item об'єкт елемента
     * @return шлях до зображення або null, якщо елемент null або зображення відсутнє
     */
    protected abstract String getImagePathForItem(T item);

    /**
     * Вказує, чи використовується панель рівня запасів.
     *
     * @return true, якщо панель рівня запасів потрібна, інакше false
     */
    protected abstract boolean hasStockLevelBar();

    /**
     * Оновлює панель рівня запасів для елемента.
     *
     * @param item об'єкт елемента
     */
    protected abstract void updateStockLevelBar(T item);

    /**
     * Повертає назву елемента в однині.
     *
     * @return рядок із назвою елемента
     */
    protected abstract String getItemNameSingular();

    /**
     * Відкриває діалог для додавання або редагування елемента.
     *
     * @param e подія, що викликала діалог (може бути null)
     */
    protected abstract void showAddEditDialog(ActionEvent e);

    /**
     * Видаляє вибраний елемент із таблиці та бази даних.
     *
     * @param e подія, що викликала видалення
     */
    protected abstract void deleteSelectedItem(ActionEvent e);

    /**
     * Отримує список усіх елементів із DAO.
     *
     * @return список усіх елементів
     */
    protected abstract List<T> getAllItemsFromDAO();

    /**
     * Отримує елемент за його ідентифікатором із DAO.
     *
     * @param id ідентифікатор елемента
     * @return об'єкт елемента або null, якщо не знайдено
     */
    protected abstract T getItemByIdFromDAO(int id);

    /**
     * Отримує елемент за індексом рядка в моделі таблиці.
     *
     * @param modelRow індекс рядка в моделі
     * @return об'єкт елемента або null, якщо елемент не знайдено
     */
    protected T getItemByModelRow(int modelRow) {
        if (modelRow >= 0 && modelRow < tableModel.getRowCount()) {
            Object idObj = tableModel.getValueAt(modelRow, 0);
            if (idObj instanceof Integer) {
                return getItemByIdFromDAO((Integer) idObj);
            }
        }
        return null;
    }

    /**
     * Слухач змін у текстових полях для автоматичного оновлення даних.
     */
    protected static class SimpleDocumentListener implements javax.swing.event.DocumentListener {
        private final Runnable updateAction;

        public SimpleDocumentListener(Runnable updateAction) {
            this.updateAction = updateAction;
        }

        @Override
        public void insertUpdate(javax.swing.event.DocumentEvent e) {
            updateAction.run();
        }

        @Override
        public void removeUpdate(javax.swing.event.DocumentEvent e) {
            updateAction.run();
        }

        @Override
        public void changedUpdate(javax.swing.event.DocumentEvent e) {
            updateAction.run();
        }
    }
}