package flowershop.gui.Dialog;

import flowershop.models.Bouquet;
import flowershop.models.Flower;
import flowershop.models.Accessory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 * Діалогове вікно для додавання або редагування букета.
 * Успадковує функціонал від AbstractAddEditDialog.
 */
public class AddEditBouquetDialog extends AbstractAddEditDialog<Bouquet> {

    private static final Logger logger = LogManager.getLogger(AddEditBouquetDialog.class);

    // Поля форми, специфічні для букета
    private JTextField nameField;
    private JTextArea descriptionArea;
    private JSpinner discountSpinner;

    // Списки для вибору квітів та аксесуарів
    private JList<Flower> availableFlowersList;
    private JList<Accessory> availableAccessoriesList;
    private JList<String> selectedFlowersDisplayList;
    private JList<Accessory> selectedAccessoriesDisplayList;

    // Кнопки для переміщення елементів
    private JButton addFlowerButton, removeFlowerButton;
    private JButton addAccessoryButton, removeAccessoryButton;
    private JSpinner flowerQuantitySpinner;

    // Колекції для зберігання даних
    private List<Flower> availableFlowersSource;
    private List<Accessory> availableAccessoriesSource;
    private DefaultListModel<Flower> selectedFlowersModel;
    private DefaultListModel<String> selectedFlowersDisplayModel;
    private DefaultListModel<Accessory> selectedAccessoriesModel;

    private Map<Flower, Integer> currentFlowerQuantities;


    public AddEditBouquetDialog(JFrame parent, Bouquet bouquetToEdit,
                                List<Flower> allAvailableFlowers, List<Accessory> allAvailableAccessories) {
        super(parent, bouquetToEdit == null ? "Додати букет" : "Редагувати букет", bouquetToEdit);
        logger.info("Ініціалізація AddEditBouquetDialog для {}. Доступно квітів: {}, аксесуарів: {}",
                bouquetToEdit == null ? "нового букета" : "редагування букета ID: " + (bouquetToEdit != null ? bouquetToEdit.getId() : "N/A"),
                allAvailableFlowers != null ? allAvailableFlowers.size() : 0,
                allAvailableAccessories != null ? allAvailableAccessories.size() : 0);

        this.availableFlowersSource = allAvailableFlowers != null ? new ArrayList<>(allAvailableFlowers) : new ArrayList<>();
        this.availableAccessoriesSource = allAvailableAccessories != null ? new ArrayList<>(allAvailableAccessories) : new ArrayList<>();
        logger.debug("Джерела доступних квітів та аксесуарів ініціалізовані.");

        // Оновлення даних для JList ПІСЛЯ створення панелі форми, де ці JList ініціалізуються
        // Це буде зроблено в createFormPanel або одразу після його виклику (вже є в createFormPanel)

        if (this.item != null) { // item - це bouquetToEdit
            logger.debug("Заповнення полів для редагованого букета.");
            populateFields(); // populateFields має бути викликано після того, як UI компоненти створені (тобто після createFormPanel)
        }
        pack();
        setMinimumSize(new Dimension(850, 750));
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int width = (int) (screenSize.width * 0.7);
        int height = (int) (screenSize.height * 0.9);
        setSize(Math.max(width, getMinimumSize().width), Math.max(height, getMinimumSize().height)); // Ensure minimum size
        setLocationRelativeTo(parent);
        setResizable(true);
        logger.debug("Розміри та позиція AddEditBouquetDialog встановлені.");

        SwingUtilities.invokeLater(() -> {
            nameField.requestFocusInWindow();
            logger.trace("Фокус встановлено на nameField.");
        });
    }

    @Override
    protected JPanel createFormPanel() {
        logger.debug("Створення панелі форми для AddEditBouquetDialog.");
        if (this.selectedFlowersModel == null) this.selectedFlowersModel = new DefaultListModel<>();
        if (this.selectedFlowersDisplayModel == null) this.selectedFlowersDisplayModel = new DefaultListModel<>();
        if (this.selectedAccessoriesModel == null) this.selectedAccessoriesModel = new DefaultListModel<>();
        if (this.currentFlowerQuantities == null) this.currentFlowerQuantities = new HashMap<>();
        logger.trace("Моделі списків та кількостей ініціалізовані/перевірені.");

        JPanel formPanel = new JPanel(new BorderLayout(10, 10));
        formPanel.setBackground(BACKGROUND_COLOR);

        JPanel topPanel = new JPanel(new GridBagLayout());
        topPanel.setOpaque(false);
        topPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(PRIMARY_COLOR),
                "Основна інформація",
                TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION,
                new Font("SansSerif", Font.BOLD, 14), PRIMARY_COLOR
        ));
        GridBagConstraints gbcTop = new GridBagConstraints();
        gbcTop.insets = new Insets(5, 5, 5, 5);
        gbcTop.anchor = GridBagConstraints.WEST;

        nameField = createStyledTextField(25);
        addFormField(topPanel, "Назва букета:", nameField, gbcTop, 0, 1, GridBagConstraints.HORIZONTAL);

        descriptionArea = createStyledTextArea(3, 25);
        JScrollPane descriptionScrollPane = new JScrollPane(descriptionArea);
        descriptionScrollPane.setBorder(FIELD_BORDER);
        addFormField(topPanel, "Опис:", descriptionScrollPane, gbcTop, 1, 1, GridBagConstraints.BOTH);
        gbcTop.weighty = 0.3;

        discountSpinner = new JSpinner(new SpinnerNumberModel(0.0, 0.0, 100.0, 1.0));
        discountSpinner.setFont(DEFAULT_FONT);
        ((JSpinner.DefaultEditor) discountSpinner.getEditor()).getTextField().setColumns(5);
        addFormField(topPanel, "Знижка (%):", discountSpinner, gbcTop, 2, 1, GridBagConstraints.NONE);
        gbcTop.weighty = 0.0;

        JPanel imageSelectionPanel = new JPanel(new BorderLayout(5,0));
        imageSelectionPanel.setOpaque(false);
        imagePathField = createStyledTextField(20);
        imagePathField.setEditable(false);
        imagePathField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { updatePreviewImage(imagePathField.getText()); }
            public void removeUpdate(DocumentEvent e) { updatePreviewImage(imagePathField.getText()); }
            public void changedUpdate(DocumentEvent e) { updatePreviewImage(imagePathField.getText()); }
        });

        browseButton = createStyledButton("Огляд...", null); // Шлях до іконки не використовується
        browseButton.addActionListener(e -> browseImageAction());
        imageSelectionPanel.add(imagePathField, BorderLayout.CENTER);
        imageSelectionPanel.add(browseButton, BorderLayout.EAST);
        addFormField(topPanel, "Зображення:", imageSelectionPanel, gbcTop, 3,1, GridBagConstraints.HORIZONTAL);

        previewImageLabel = new JLabel("Прев'ю", JLabel.CENTER);
        previewImageLabel.setPreferredSize(new Dimension(150, 120));
        previewImageLabel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        previewImageLabel.setOpaque(true);
        previewImageLabel.setBackground(Color.WHITE);
        gbcTop.gridx = 1; gbcTop.gridy = 4; gbcTop.anchor = GridBagConstraints.CENTER;
        gbcTop.fill = GridBagConstraints.NONE;
        topPanel.add(previewImageLabel, gbcTop);

        formPanel.add(topPanel, BorderLayout.NORTH);

        JPanel selectionMainPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        selectionMainPanel.setOpaque(false);
        selectionMainPanel.setBorder(BorderFactory.createEmptyBorder(10,0,10,0));

        JPanel availableItemsPanel = new JPanel(new GridLayout(2,1,0,10));
        availableItemsPanel.setOpaque(false);
        availableItemsPanel.add(createAvailableItemsSubPanel("Доступні квіти", true));
        availableItemsPanel.add(createAvailableItemsSubPanel("Доступні аксесуари", false));

        if (availableFlowersList != null && this.availableFlowersSource != null) {
            availableFlowersList.setListData(this.availableFlowersSource.toArray(new Flower[0]));
            logger.trace("Дані для availableFlowersList оновлено.");
        }
        if (availableAccessoriesList != null && this.availableAccessoriesSource != null) {
            availableAccessoriesList.setListData(this.availableAccessoriesSource.toArray(new Accessory[0]));
            logger.trace("Дані для availableAccessoriesList оновлено.");
        }

        JPanel selectedItemsPanel = new JPanel(new GridLayout(2,1,0,10));
        selectedItemsPanel.setOpaque(false);
        selectedItemsPanel.add(createSelectedItemsSubPanel("Квіти в букеті", true));
        selectedItemsPanel.add(createSelectedItemsSubPanel("Аксесуари в букеті", false));

        selectionMainPanel.add(availableItemsPanel);
        selectionMainPanel.add(selectedItemsPanel);

        formPanel.add(selectionMainPanel, BorderLayout.CENTER);
        setupEnterNavigation(nameField, descriptionArea, discountSpinner);
        logger.info("Панель форми для AddEditBouquetDialog успішно створена.");
        return formPanel;
    }

    private JPanel createAvailableItemsSubPanel(String title, boolean isFlowers) {
        logger.debug("Створення підпанелі доступних елементів: '{}', Квіти: {}", title, isFlowers);
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createTitledBorder(title));

        if (isFlowers) {
            availableFlowersList = new JList<>(new Flower[0]); // Дані завантажаться пізніше
            availableFlowersList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            availableFlowersList.setCellRenderer(new FlowerRenderer());
            availableFlowersList.addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent evt) {
                    if (evt.getClickCount() == 2) {
                        logger.trace("Подвійний клік на availableFlowersList, додавання квітки.");
                        addFlowerToList();
                    }
                }
            });
            panel.add(new JScrollPane(availableFlowersList), BorderLayout.CENTER);

            JPanel flowerControls = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            flowerControls.setOpaque(false);
            flowerControls.add(new JLabel("К-сть:"));
            flowerQuantitySpinner = new JSpinner(new SpinnerNumberModel(1, 1, 100, 1));
            flowerQuantitySpinner.setPreferredSize(new Dimension(60, 25));
            flowerControls.add(flowerQuantitySpinner);
            addFlowerButton = createStyledButton("Додати >", null);
            addFlowerButton.setPreferredSize(new Dimension(100, 25));
            addFlowerButton.addActionListener(e -> addFlowerToList());
            flowerControls.add(addFlowerButton);
            panel.add(flowerControls, BorderLayout.SOUTH);
        } else {
            availableAccessoriesList = new JList<>(new Accessory[0]); // Дані завантажаться пізніше
            availableAccessoriesList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            availableAccessoriesList.setCellRenderer(new AccessoryRenderer());
            availableAccessoriesList.addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent evt) {
                    if (evt.getClickCount() == 2) {
                        logger.trace("Подвійний клік на availableAccessoriesList, додавання аксесуара.");
                        addAccessoryToList();
                    }
                }
            });
            panel.add(new JScrollPane(availableAccessoriesList), BorderLayout.CENTER);

            JPanel accessoryControls = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            accessoryControls.setOpaque(false);
            addAccessoryButton = createStyledButton("Додати >", null);
            addAccessoryButton.setPreferredSize(new Dimension(100, 25));
            addAccessoryButton.addActionListener(e -> addAccessoryToList());
            accessoryControls.add(addAccessoryButton);
            panel.add(accessoryControls, BorderLayout.SOUTH);
        }
        return panel;
    }

    private JPanel createSelectedItemsSubPanel(String title, boolean isFlowers) {
        logger.debug("Створення підпанелі вибраних елементів: '{}', Квіти: {}", title, isFlowers);
        JPanel panel = new JPanel(new BorderLayout(5,5));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createTitledBorder(title));

        if (isFlowers) {
            selectedFlowersDisplayList = new JList<>(selectedFlowersDisplayModel);
            selectedFlowersDisplayList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            selectedFlowersDisplayList.addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent evt) {
                    if (evt.getClickCount() == 2) {
                        logger.trace("Подвійний клік на selectedFlowersDisplayList, видалення квітки.");
                        removeFlowerFromList();
                    }
                }
            });
            panel.add(new JScrollPane(selectedFlowersDisplayList), BorderLayout.CENTER);

            removeFlowerButton = createStyledButton("< Видалити", null);
            removeFlowerButton.setPreferredSize(new Dimension(110, 25));
            removeFlowerButton.addActionListener(e -> removeFlowerFromList());
            JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            controlPanel.setOpaque(false);
            controlPanel.add(removeFlowerButton);
            panel.add(controlPanel, BorderLayout.SOUTH);

        } else {
            selectedAccessoriesDisplayList = new JList<>(selectedAccessoriesModel);
            selectedAccessoriesDisplayList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            selectedAccessoriesDisplayList.setCellRenderer(new AccessoryRenderer());
            selectedAccessoriesDisplayList.addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent evt) {
                    if (evt.getClickCount() == 2) {
                        logger.trace("Подвійний клік на selectedAccessoriesDisplayList, видалення аксесуара.");
                        removeAccessoryFromList();
                    }
                }
            });
            panel.add(new JScrollPane(selectedAccessoriesDisplayList), BorderLayout.CENTER);

            removeAccessoryButton = createStyledButton("< Видалити", null);
            removeAccessoryButton.setPreferredSize(new Dimension(110, 25));
            removeAccessoryButton.addActionListener(e -> removeAccessoryFromList());
            JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            controlPanel.setOpaque(false);
            controlPanel.add(removeAccessoryButton);
            panel.add(controlPanel, BorderLayout.SOUTH);
        }
        return panel;
    }


    private void addFlowerToList() {
        Flower selectedFlower = availableFlowersList.getSelectedValue();
        if (selectedFlower != null) {
            int quantity = (Integer) flowerQuantitySpinner.getValue();
            logger.info("Додавання квітки '{}' до букету, кількість: {}", selectedFlower.getShortInfo(), quantity);
            if (quantity <= 0) {
                showErrorDialog("Кількість квітів має бути більшою за нуль.");
                logger.warn("Спроба додати квітку з кількістю <= 0.");
                return;
            }
            if (currentFlowerQuantities.containsKey(selectedFlower)) {
                currentFlowerQuantities.put(selectedFlower, currentFlowerQuantities.get(selectedFlower) + quantity);
                logger.trace("Оновлено кількість для існуючої квітки '{}'. Нова кількість: {}", selectedFlower.getShortInfo(), currentFlowerQuantities.get(selectedFlower));
            } else {
                selectedFlowersModel.addElement(selectedFlower);
                currentFlowerQuantities.put(selectedFlower, quantity);
                logger.trace("Додано нову квітку '{}' з кількістю {}", selectedFlower.getShortInfo(), quantity);
            }
            updateSelectedFlowersDisplay();
            flowerQuantitySpinner.setValue(1);
        } else {
            logger.debug("Спроба додати квітку, але жодна квітка не вибрана.");
        }
    }

    private void removeFlowerFromList() {
        int selectedIndex = selectedFlowersDisplayList.getSelectedIndex();
        if (selectedIndex != -1 && selectedIndex < selectedFlowersModel.getSize()) {
            Flower flowerToRemove = selectedFlowersModel.getElementAt(selectedIndex);
            logger.info("Видалення квітки '{}' з букету.", flowerToRemove.getShortInfo());
            selectedFlowersModel.removeElement(flowerToRemove);
            currentFlowerQuantities.remove(flowerToRemove);
            updateSelectedFlowersDisplay();
        } else {
            logger.debug("Спроба видалити квітку, але жодна квітка не вибрана або індекс поза межами.");
        }
    }

    private void updateSelectedFlowersDisplay() {
        logger.debug("Оновлення списку відображення вибраних квітів.");
        selectedFlowersDisplayModel.clear();
        for (int i = 0; i < selectedFlowersModel.getSize(); i++) {
            Flower flower = selectedFlowersModel.getElementAt(i);
            int quantity = currentFlowerQuantities.getOrDefault(flower, 0);
            selectedFlowersDisplayModel.addElement(flower.getShortInfo() + " (x" + quantity + ")");
        }
        logger.trace("Список відображення квітів оновлено. Кількість елементів: {}", selectedFlowersDisplayModel.getSize());
    }


    private void addAccessoryToList() {
        Accessory selectedAccessory = availableAccessoriesList.getSelectedValue();
        if (selectedAccessory != null && !selectedAccessoriesModel.contains(selectedAccessory)) {
            logger.info("Додавання аксесуара '{}' до букету.", selectedAccessory.getShortInfo());
            selectedAccessoriesModel.addElement(selectedAccessory);
        } else if (selectedAccessory == null) {
            logger.debug("Спроба додати аксесуар, але жоден аксесуар не вибраний.");
        } else {
            logger.debug("Аксесуар '{}' вже є в букеті.", selectedAccessory.getShortInfo());
        }
    }

    private void removeAccessoryFromList() {
        Accessory selectedAccessory = selectedAccessoriesDisplayList.getSelectedValue();
        if (selectedAccessory != null) {
            logger.info("Видалення аксесуара '{}' з букету.", selectedAccessory.getShortInfo());
            selectedAccessoriesModel.removeElement(selectedAccessory);
        } else {
            logger.debug("Спроба видалити аксесуар, але жоден аксесуар не вибраний.");
        }
    }


    @Override
    protected void populateFields() {
        logger.debug("Заповнення полів форми даними букета.");
        if (item != null) {
            nameField.setText(item.getName());
            descriptionArea.setText(item.getDescription());
            discountSpinner.setValue(item.getDiscount());
            imagePathField.setText(item.getImagePath());
            updatePreviewImage(item.getImagePath());

            currentFlowerQuantities.clear();
            selectedFlowersModel.clear();
            if (item.getFlowers() != null) {
                logger.trace("Заповнення списку квітів для букета '{}'. Кількість типів квітів: {}", item.getName(), item.getFlowers().size());
                for (Flower flower : item.getFlowers()) { // тут item.getFlowers() повертає унікальні типи квітів
                    selectedFlowersModel.addElement(flower);
                    currentFlowerQuantities.put(flower, item.getFlowerQuantity(flower));
                    logger.trace("Додано квітку '{}' з кількістю {}", flower.getShortInfo(), item.getFlowerQuantity(flower));
                }
            }
            updateSelectedFlowersDisplay();


            selectedAccessoriesModel.clear();
            if (item.getAccessories() != null) {
                logger.trace("Заповнення списку аксесуарів для букета '{}'. Кількість аксесуарів: {}", item.getName(), item.getAccessories().size());
                for (Accessory acc : item.getAccessories()) {
                    selectedAccessoriesModel.addElement(acc);
                    logger.trace("Додано аксесуар '{}'", acc.getShortInfo());
                }
            }
            logger.info("Поля форми заповнені для букета: {}", item.getName());
        } else {
            logger.warn("Спроба заповнити поля, але 'item' (букет) є null.");
        }
    }

    @Override
    protected boolean saveItem() {
        logger.debug("Спроба зберегти букет.");
        try {
            String name = nameField.getText().trim();
            String description = descriptionArea.getText().trim();
            double discount = (Double) discountSpinner.getValue();
            String imagePath = imagePathField.getText().trim();
            logger.trace("Дані з форми: Назва='{}', Знижка='{}', Шлях зображення='{}'", name, discount, imagePath);


            if (name.isEmpty()) {
                showErrorDialog("Назва букета не може бути порожньою.");
                nameField.requestFocusInWindow();
                return false;
            }
            if (selectedFlowersModel.isEmpty() && selectedAccessoriesModel.isEmpty()) {
                showErrorDialog("Букет повинен містити хоча б одну квітку або аксесуар.");
                logger.warn("Спроба зберегти порожній букет.");
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

            List<Flower> finalSelectedFlowers = new ArrayList<>();
            for (int i = 0; i < selectedFlowersModel.getSize(); i++) {
                finalSelectedFlowers.add(selectedFlowersModel.getElementAt(i));
            }
            logger.debug("Кількість унікальних типів квітів для збереження: {}", finalSelectedFlowers.size());

            List<Accessory> finalSelectedAccessories = new ArrayList<>();
            for (int i = 0; i < selectedAccessoriesModel.getSize(); i++) {
                finalSelectedAccessories.add(selectedAccessoriesModel.getElementAt(i));
            }
            logger.debug("Кількість аксесуарів для збереження: {}", finalSelectedAccessories.size());


            if (item == null) {
                logger.info("Створення нового букета.");
                // Конструктор Bouquet може потребувати Map<Flower, Integer> або обробляти кількість окремо
                item = new Bouquet(name, description, (List<Flower>) new HashMap<>(currentFlowerQuantities), finalSelectedAccessories, imagePath, discount);
            } else {
                logger.info("Оновлення існуючого букета ID: {}", item.getId());
                item.setName(name);
                item.setDescription(description);
                item.setDiscount(discount);
                item.setImagePath(imagePath);
                item.setFlowerQuantities(new HashMap<>(currentFlowerQuantities)); // Оновлення квітів з їх кількістю
                item.setAccessories(finalSelectedAccessories); // Оновлення аксесуарів
            }

            logger.info("Букет '{}' успішно підготовлений до збереження.", item.getName());
            return true;
        } catch (NumberFormatException ex) {
            logger.error("Помилка формату числа при збереженні букета (знижка): {}", ex.getMessage(), ex);
            showErrorDialog("Будь ласка, перевірте числові значення (знижка).");
            return false;
        } catch (Exception ex) {
            logger.error("Непередбачена помилка при збереженні букета: {}", ex.getMessage(), ex);
            showErrorDialog("Сталася помилка: " + ex.getMessage());
            return false;
        }
    }

    public Bouquet getBouquet() {
        logger.debug("Запит на отримання об'єкта Bouquet. Поточний item: {}", item != null ? item.getName() : "null");
        return item;
    }

    private static class FlowerRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value instanceof Flower) {
                label.setText(((Flower) value).getShortInfo());
            }
            return label;
        }
    }

    private static class AccessoryRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value instanceof Accessory) {
                label.setText(((Accessory) value).getShortInfo());
            }
            return label;
        }
    }
}