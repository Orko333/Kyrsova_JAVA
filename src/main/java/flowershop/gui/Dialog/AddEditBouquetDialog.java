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
 * Дозволяє вводити та редагувати назву, опис, знижку, зображення, а також вибирати квіти та аксесуари для букета.
 */
public class AddEditBouquetDialog extends AbstractAddEditDialog<Bouquet> {

    private static final Logger logger = LogManager.getLogger(AddEditBouquetDialog.class);

    private JTextField nameField;
    private JTextArea descriptionArea;
    private JSpinner discountSpinner;
    private JList<Flower> availableFlowersList;
    private JList<Accessory> availableAccessoriesList;
    private JList<String> selectedFlowersDisplayList;
    private JList<Accessory> selectedAccessoriesDisplayList;
    private JButton addFlowerButton, removeFlowerButton, addAccessoryButton, removeAccessoryButton;
    private JSpinner flowerQuantitySpinner;
    private final List<Flower> availableFlowersSource;
    private final List<Accessory> availableAccessoriesSource;
    private final DefaultListModel<Flower> selectedFlowersModel;
    private final DefaultListModel<String> selectedFlowersDisplayModel;
    private final DefaultListModel<Accessory> selectedAccessoriesModel;
    private final Map<Flower, Integer> currentFlowerQuantities;

    /**
     * Конструктор діалогового вікна.
     *
     * @param parent                 батьківське вікно
     * @param bouquetToEdit          букет для редагування (null, якщо створюється новий)
     * @param allAvailableFlowers    список доступних квітів
     * @param allAvailableAccessories список доступних аксесуарів
     */
    public AddEditBouquetDialog(JFrame parent, Bouquet bouquetToEdit,
                                List<Flower> allAvailableFlowers, List<Accessory> allAvailableAccessories) {
        super(parent, bouquetToEdit == null ? "Додати букет" : "Редагувати букет", bouquetToEdit);
        this.availableFlowersSource = allAvailableFlowers != null ? new ArrayList<>(allAvailableFlowers) : new ArrayList<>();
        this.availableAccessoriesSource = allAvailableAccessories != null ? new ArrayList<>(allAvailableAccessories) : new ArrayList<>();
        this.selectedFlowersModel = new DefaultListModel<>();
        this.selectedFlowersDisplayModel = new DefaultListModel<>();
        this.selectedAccessoriesModel = new DefaultListModel<>();
        this.currentFlowerQuantities = new HashMap<>();

        if (this.item != null) {
            populateFields();
        }
        pack();
        setMinimumSize(new Dimension(850, 750));
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setSize((int) (screenSize.width * 0.7), (int) (screenSize.height * 0.9));
        setLocationRelativeTo(parent);
        setResizable(true);
        SwingUtilities.invokeLater(() -> nameField.requestFocusInWindow());
        logger.info("Діалогове вікно {} відкрито.", bouquetToEdit == null ? "додавання" : "редагування");
    }

    // Формування UI

    /**
     * Створює панель форми для введення даних про букет.
     *
     * @return панель із компонентами форми
     */
    @Override
    protected JPanel createFormPanel() {
        JPanel formPanel = new JPanel(new BorderLayout(10, 10));
        formPanel.setBackground(BACKGROUND_COLOR);

        formPanel.add(createTopPanel(), BorderLayout.NORTH);
        formPanel.add(createSelectionPanel(), BorderLayout.CENTER);

        setupEnterNavigation(nameField, descriptionArea, discountSpinner);
        return formPanel;
    }

    /**
     * Створює верхню панель із основною інформацією про букет.
     *
     * @return верхня панель
     */
    private JPanel createTopPanel() {
        JPanel topPanel = new JPanel(new GridBagLayout());
        topPanel.setOpaque(false);
        topPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(PRIMARY_COLOR),
                "Основна інформація",
                TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION,
                new Font("SansSerif", Font.BOLD, 14), PRIMARY_COLOR));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        nameField = createStyledTextField(25);
        addFormField(topPanel, "Назва букета:", nameField, gbc, 0, 1, GridBagConstraints.HORIZONTAL);

        descriptionArea = createStyledTextArea(3, 25);
        JScrollPane descriptionScrollPane = new JScrollPane(descriptionArea);
        descriptionScrollPane.setBorder(FIELD_BORDER);
        addFormField(topPanel, "Опис:", descriptionScrollPane, gbc, 1, 1, GridBagConstraints.BOTH);
        gbc.weighty = 0.3;

        discountSpinner = new JSpinner(new SpinnerNumberModel(0.0, 0.0, 100.0, 1.0));
        discountSpinner.setFont(DEFAULT_FONT);
        ((JSpinner.DefaultEditor) discountSpinner.getEditor()).getTextField().setColumns(5);
        addFormField(topPanel, "Знижка (%):", discountSpinner, gbc, 2, 1, GridBagConstraints.NONE);
        gbc.weighty = 0.0;

        JPanel imageSelectionPanel = new JPanel(new BorderLayout(5, 0));
        imageSelectionPanel.setOpaque(false);
        imagePathField = createStyledTextField(20);
        imagePathField.setEditable(false);
        imagePathField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                updatePreviewImage(imagePathField.getText());
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                updatePreviewImage(imagePathField.getText());
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                updatePreviewImage(imagePathField.getText());
            }
        });

        browseButton = createStyledButton("Огляд...", null);
        browseButton.addActionListener(e -> browseImageAction());
        imageSelectionPanel.add(imagePathField, BorderLayout.CENTER);
        imageSelectionPanel.add(browseButton, BorderLayout.EAST);
        addFormField(topPanel, "Зображення:", imageSelectionPanel, gbc, 3, 1, GridBagConstraints.HORIZONTAL);

        previewImageLabel = new JLabel("Прев'ю", JLabel.CENTER);
        previewImageLabel.setPreferredSize(new Dimension(150, 120));
        previewImageLabel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        previewImageLabel.setOpaque(true);
        previewImageLabel.setBackground(Color.WHITE);
        gbc.gridx = 1;
        gbc.gridy = 4;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.NONE;
        topPanel.add(previewImageLabel, gbc);

        return topPanel;
    }

    /**
     * Створює панель вибору квітів і аксесуарів.
     *
     * @return панель вибору
     */
    private JPanel createSelectionPanel() {
        JPanel selectionMainPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        selectionMainPanel.setOpaque(false);
        selectionMainPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

        JPanel availableItemsPanel = new JPanel(new GridLayout(2, 1, 0, 10));
        availableItemsPanel.setOpaque(false);
        availableItemsPanel.add(createAvailableItemsSubPanel("Доступні квіти", true));
        availableItemsPanel.add(createAvailableItemsSubPanel("Доступні аксесуари", false));

        if (availableFlowersList != null) {
            availableFlowersList.setListData(availableFlowersSource.toArray(new Flower[0]));
        }
        if (availableAccessoriesList != null) {
            availableAccessoriesList.setListData(availableAccessoriesSource.toArray(new Accessory[0]));
        }

        JPanel selectedItemsPanel = new JPanel(new GridLayout(2, 1, 0, 10));
        selectedItemsPanel.setOpaque(false);
        selectedItemsPanel.add(createSelectedItemsSubPanel("Квіти в букеті", true));
        selectedItemsPanel.add(createSelectedItemsSubPanel("Аксесуари в букеті", false));

        selectionMainPanel.add(availableItemsPanel);
        selectionMainPanel.add(selectedItemsPanel);
        return selectionMainPanel;
    }

    /**
     * Створює підпанель для відображення доступних елементів (квітів або аксесуарів).
     *
     * @param title     заголовок панелі
     * @param isFlowers чи є панель для квітів
     * @return підпанель доступних елементів
     */
    private JPanel createAvailableItemsSubPanel(String title, boolean isFlowers) {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createTitledBorder(title));

        if (isFlowers) {
            availableFlowersList = new JList<>(new Flower[0]);
            availableFlowersList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            availableFlowersList.setCellRenderer(new FlowerRenderer());
            availableFlowersList.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent evt) {
                    if (evt.getClickCount() == 2) {
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
            availableAccessoriesList = new JList<>(new Accessory[0]);
            availableAccessoriesList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            availableAccessoriesList.setCellRenderer(new AccessoryRenderer());
            availableAccessoriesList.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent evt) {
                    if (evt.getClickCount() == 2) {
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

    /**
     * Створює підпанель для відображення вибраних елементів (квітів або аксесуарів).
     *
     * @param title     заголовок панелі
     * @param isFlowers чи є панель для квітів
     * @return підпанель вибраних елементів
     */
    private JPanel createSelectedItemsSubPanel(String title, boolean isFlowers) {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createTitledBorder(title));

        if (isFlowers) {
            selectedFlowersDisplayList = new JList<>(selectedFlowersDisplayModel);
            selectedFlowersDisplayList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            selectedFlowersDisplayList.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent evt) {
                    if (evt.getClickCount() == 2) {
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
                @Override
                public void mouseClicked(MouseEvent evt) {
                    if (evt.getClickCount() == 2) {
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

    // Обробка списків

    /**
     * Додає вибрану квітку до списку букета.
     */
    private void addFlowerToList() {
        Flower selectedFlower = availableFlowersList.getSelectedValue();
        if (selectedFlower != null) {
            int quantity = (Integer) flowerQuantitySpinner.getValue();
            if (quantity <= 0) {
                showErrorDialog("Кількість квітів має бути більшою за нуль.");
                return;
            }
            if (currentFlowerQuantities.containsKey(selectedFlower)) {
                currentFlowerQuantities.put(selectedFlower, currentFlowerQuantities.get(selectedFlower) + quantity);
            } else {
                selectedFlowersModel.addElement(selectedFlower);
                currentFlowerQuantities.put(selectedFlower, quantity);
            }
            updateSelectedFlowersDisplay();
            flowerQuantitySpinner.setValue(1);
            logger.debug("Додано квітку: {} (кількість: {})", selectedFlower.getShortInfo(), quantity);
        }
    }

    /**
     * Видаляє вибрану квітку зі списку букета.
     */
    private void removeFlowerFromList() {
        int selectedIndex = selectedFlowersDisplayList.getSelectedIndex();
        if (selectedIndex != -1 && selectedIndex < selectedFlowersModel.getSize()) {
            Flower flowerToRemove = selectedFlowersModel.getElementAt(selectedIndex);
            selectedFlowersModel.removeElement(flowerToRemove);
            currentFlowerQuantities.remove(flowerToRemove);
            updateSelectedFlowersDisplay();
            logger.debug("Видалено квітку: {}", flowerToRemove.getShortInfo());
        }
    }

    /**
     * Оновлює відображення списку вибраних квіток.
     */
    private void updateSelectedFlowersDisplay() {
        selectedFlowersDisplayModel.clear();
        for (int i = 0; i < selectedFlowersModel.getSize(); i++) {
            Flower flower = selectedFlowersModel.getElementAt(i);
            int quantity = currentFlowerQuantities.getOrDefault(flower, 0);
            selectedFlowersDisplayModel.addElement(flower.getShortInfo() + " (x" + quantity + ")");
        }
    }

    /**
     * Додає вибраний аксесуар до списку букета.
     */
    private void addAccessoryToList() {
        Accessory selectedAccessory = availableAccessoriesList.getSelectedValue();
        if (selectedAccessory != null && !selectedAccessoriesModel.contains(selectedAccessory)) {
            selectedAccessoriesModel.addElement(selectedAccessory);
            logger.debug("Додано аксесуар: {}", selectedAccessory.getShortInfo());
        }
    }

    /**
     * Видаляє вибраний аксесуар зі списку букета.
     */
    private void removeAccessoryFromList() {
        Accessory selectedAccessory = selectedAccessoriesDisplayList.getSelectedValue();
        if (selectedAccessory != null) {
            selectedAccessoriesModel.removeElement(selectedAccessory);
            logger.debug("Видалено аксесуар: {}", selectedAccessory.getShortInfo());
        }
    }

    // Заповнення полів

    /**
     * Заповнює поля форми даними букета, якщо редагується існуючий букет.
     */
    @Override
    protected void populateFields() {
        if (item != null) {
            nameField.setText(item.getName());
            descriptionArea.setText(item.getDescription());
            discountSpinner.setValue(item.getDiscount());
            imagePathField.setText(item.getImagePath());
            updatePreviewImage(item.getImagePath());

            currentFlowerQuantities.clear();
            selectedFlowersModel.clear();
            if (item.getFlowers() != null) {
                for (Flower flower : item.getFlowers()) {
                    selectedFlowersModel.addElement(flower);
                    currentFlowerQuantities.put(flower, item.getFlowerQuantity(flower));
                }
            }
            updateSelectedFlowersDisplay();

            selectedAccessoriesModel.clear();
            if (item.getAccessories() != null) {
                for (Accessory acc : item.getAccessories()) {
                    selectedAccessoriesModel.addElement(acc);
                }
            }
        }
    }

    // Збереження даних

    /**
     * Зберігає введені дані у об'єкт букета.
     *
     * @return true, якщо збереження успішне, інакше false
     */
    @Override
    protected boolean saveItem() {
        try {
            String name = nameField.getText().trim();
            String description = descriptionArea.getText().trim();
            double discount = (Double) discountSpinner.getValue();
            String imagePath = imagePathField.getText().trim();

            // Валідація полів
            if (name.isEmpty()) {
                showErrorDialog("Назва букета не може бути порожньою.");
                nameField.requestFocusInWindow();
                return false;
            }
            if (selectedFlowersModel.isEmpty() && selectedAccessoriesModel.isEmpty()) {
                showErrorDialog("Букет повинен містити хоча б одну квітку або аксесуар.");
                return false;
            }
            if (!imagePath.isEmpty() && !new File(imagePath).isFile()) {
                if (JOptionPane.showConfirmDialog(this,
                        "Файл зображення за вказаним шляхом не знайдено або це не файл.\nПродовжити збереження без зображення (або з поточним, якщо є)?",
                        "Попередження: Зображення",
                        JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.NO_OPTION) {
                    imagePathField.requestFocusInWindow();
                    return false;
                }
            }

            // Формування списків квітів і аксесуарів
            List<Flower> finalSelectedFlowers = new ArrayList<>();
            for (int i = 0; i < selectedFlowersModel.getSize(); i++) {
                finalSelectedFlowers.add(selectedFlowersModel.getElementAt(i));
            }

            List<Accessory> finalSelectedAccessories = new ArrayList<>();
            for (int i = 0; i < selectedAccessoriesModel.getSize(); i++) {
                finalSelectedAccessories.add(selectedAccessoriesModel.getElementAt(i));
            }

            // Збереження даних
            if (item == null) {
                item = new Bouquet(name, description, (List<Flower>) new HashMap<>(currentFlowerQuantities), finalSelectedAccessories, imagePath, discount);
            } else {
                item.setName(name);
                item.setDescription(description);
                item.setDiscount(discount);
                item.setImagePath(imagePath);
                item.setFlowerQuantities(new HashMap<>(currentFlowerQuantities));
                item.setAccessories(finalSelectedAccessories);
            }
            logger.info("Букет успішно збережений: {}", name);
            return true;
        } catch (NumberFormatException ex) {
            showErrorDialog("Будь ласка, перевірте числові значення (знижка).");
            logger.error("Помилка формату чисел: {}", ex.getMessage());
            return false;
        } catch (Exception ex) {
            showErrorDialog("Сталася помилка: " + ex.getMessage());
            logger.error("Помилка збереження букета: {}", ex.getMessage());
            return false;
        }
    }

    /**
     * Повертає об'єкт букета.
     *
     * @return збережений або редагований букет
     */
    public Bouquet getBouquet() {
        return item;
    }

    // Рендерери

    /**
     * Рендерер для відображення квіток у списках.
     */
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

    /**
     * Рендерер для відображення аксесуарів у списках.
     */
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