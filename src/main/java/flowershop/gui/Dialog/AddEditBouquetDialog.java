package flowershop.gui.Dialog;

import flowershop.models.Bouquet;
import flowershop.models.Flower;
import flowershop.models.Accessory;

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

    // Поля форми, специфічні для букета
    private JTextField nameField;
    private JTextArea descriptionArea;
    private JSpinner discountSpinner;
    // imagePathField, browseButton, okButton, cancelButton, previewImageLabel - успадковані

    // Списки для вибору квітів та аксесуарів
    private JList<Flower> availableFlowersList;
    private JList<Accessory> availableAccessoriesList;
    private JList<String> selectedFlowersDisplayList; // Для відображення квітів з кількістю
    private JList<Accessory> selectedAccessoriesDisplayList;

    // Кнопки для переміщення елементів
    private JButton addFlowerButton, removeFlowerButton;
    private JButton addAccessoryButton, removeAccessoryButton;
    private JSpinner flowerQuantitySpinner; // Для вказівки кількості квітів

    // Колекції для зберігання даних
    private List<Flower> availableFlowersSource;
    private List<Accessory> availableAccessoriesSource;
    private DefaultListModel<Flower> selectedFlowersModel;
    private DefaultListModel<String> selectedFlowersDisplayModel;
    private DefaultListModel<Accessory> selectedAccessoriesModel;

    // Для зберігання кількості кожної квітки в букеті
    private Map<Flower, Integer> currentFlowerQuantities;


    public AddEditBouquetDialog(JFrame parent, Bouquet bouquetToEdit,
                                List<Flower> allAvailableFlowers, List<Accessory> allAvailableAccessories) {
        super(parent, bouquetToEdit == null ? "Додати букет" : "Редагувати букет", bouquetToEdit);

        // Ініціалізація джерел даних відбувається ПІСЛЯ super(),
        // тому JList повинні бути оновлені після цього.
        this.availableFlowersSource = allAvailableFlowers != null ? new ArrayList<>(allAvailableFlowers) : new ArrayList<>();
        this.availableAccessoriesSource = allAvailableAccessories != null ? new ArrayList<>(allAvailableAccessories) : new ArrayList<>();

        if (availableFlowersList != null) { // Перевірка, чи списки вже створені (малоймовірно на цьому етапі)
            availableFlowersList.setListData(this.availableFlowersSource.toArray(new Flower[0]));
        }
        if (availableAccessoriesList != null) {
            availableAccessoriesList.setListData(this.availableAccessoriesSource.toArray(new Accessory[0]));
        }


        if (this.item != null) { // item - це bouquetToEdit
            populateFields();
        }
        pack();
        setMinimumSize(new Dimension(850, 700));
        setLocationRelativeTo(parent);
        setResizable(true);
        SwingUtilities.invokeLater(() -> nameField.requestFocusInWindow());
    }

    @Override
    protected JPanel createFormPanel() {
        // Ініціалізація моделей тут, перед їх використанням у createSelectedItemsSubPanel або populateFields
        if (this.selectedFlowersModel == null) {
            this.selectedFlowersModel = new DefaultListModel<>();
        }
        if (this.selectedFlowersDisplayModel == null) {
            this.selectedFlowersDisplayModel = new DefaultListModel<>();
        }
        if (this.selectedAccessoriesModel == null) {
            this.selectedAccessoriesModel = new DefaultListModel<>();
        }
        if (this.currentFlowerQuantities == null) {
            this.currentFlowerQuantities = new HashMap<>();
        }

        JPanel formPanel = new JPanel(new BorderLayout(10, 10));
        formPanel.setBackground(BACKGROUND_COLOR);
        // Спільна інформація про букет (назва, опис, знижка, зображення)
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

        browseButton = createStyledButton("Огляд...", "/icons/browse.png");
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
        // Передаємо this.availableFlowersSource, який буде null на цьому етапі,
        // але JList буде оновлено в конструкторі ПІСЛЯ ініціалізації availableFlowersSource
        // або краще оновити дані після створення JList
        availableItemsPanel.add(createAvailableItemsSubPanel("Доступні квіти", true));
        availableItemsPanel.add(createAvailableItemsSubPanel("Доступні аксесуари", false));

        // Оновлення даних для JList після їх створення
        if (availableFlowersList != null && this.availableFlowersSource != null) {
            availableFlowersList.setListData(this.availableFlowersSource.toArray(new Flower[0]));
        }
        if (availableAccessoriesList != null && this.availableAccessoriesSource != null) {
            availableAccessoriesList.setListData(this.availableAccessoriesSource.toArray(new Accessory[0]));
        }

        JPanel selectedItemsPanel = new JPanel(new GridLayout(2,1,0,10));
        selectedItemsPanel.setOpaque(false);
        selectedItemsPanel.add(createSelectedItemsSubPanel("Квіти в букеті", true));
        selectedItemsPanel.add(createSelectedItemsSubPanel("Аксесуари в букеті", false));

        selectionMainPanel.add(availableItemsPanel);
        selectionMainPanel.add(selectedItemsPanel);

        formPanel.add(selectionMainPanel, BorderLayout.CENTER);

        setupEnterNavigation(nameField, descriptionArea, discountSpinner );

        return formPanel;
    }

    /**
     * Створює підпанель для списку доступних елементів (квітів або аксесуарів).
     */
    private JPanel createAvailableItemsSubPanel(String title, boolean isFlowers) {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createTitledBorder(title));

        if (isFlowers) {
            // Ініціалізуємо JList порожнім масивом, щоб уникнути NPE
            // Дані будуть завантажені пізніше з availableFlowersSource
            availableFlowersList = new JList<>(new Flower[0]);
            availableFlowersList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            availableFlowersList.setCellRenderer(new FlowerRenderer());
            availableFlowersList.addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent evt) {
                    if (evt.getClickCount() == 2) addFlowerToList();
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
            // Ініціалізуємо JList порожнім масивом
            // Дані будуть завантажені пізніше з availableAccessoriesSource
            availableAccessoriesList = new JList<>(new Accessory[0]);
            availableAccessoriesList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            availableAccessoriesList.setCellRenderer(new AccessoryRenderer());
            availableAccessoriesList.addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent evt) {
                    if (evt.getClickCount() == 2) addAccessoryToList();
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
        JPanel panel = new JPanel(new BorderLayout(5,5));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createTitledBorder(title));

        if (isFlowers) {
            // selectedFlowersDisplayModel вже ініціалізовано в createFormPanel
            selectedFlowersDisplayList = new JList<>(selectedFlowersDisplayModel);
            selectedFlowersDisplayList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            selectedFlowersDisplayList.addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent evt) {
                    if (evt.getClickCount() == 2) removeFlowerFromList();
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
            // selectedAccessoriesModel вже ініціалізовано в createFormPanel
            selectedAccessoriesDisplayList = new JList<>(selectedAccessoriesModel);
            selectedAccessoriesDisplayList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            selectedAccessoriesDisplayList.setCellRenderer(new AccessoryRenderer());
            selectedAccessoriesDisplayList.addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent evt) {
                    if (evt.getClickCount() == 2) removeAccessoryFromList();
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
            if (quantity <= 0) {
                showErrorDialog("Кількість квітів має бути більшою за нуль.");
                return;
            }
            // selectedFlowersModel та currentFlowerQuantities вже мають бути ініціалізовані
            if (currentFlowerQuantities.containsKey(selectedFlower)) {
                currentFlowerQuantities.put(selectedFlower, currentFlowerQuantities.get(selectedFlower) + quantity);
            } else {
                selectedFlowersModel.addElement(selectedFlower);
                currentFlowerQuantities.put(selectedFlower, quantity);
            }
            updateSelectedFlowersDisplay();
            flowerQuantitySpinner.setValue(1);
        }
    }

    private void removeFlowerFromList() {
        int selectedIndex = selectedFlowersDisplayList.getSelectedIndex();
        if (selectedIndex != -1 && selectedIndex < selectedFlowersModel.getSize()) { // Додано перевірку selectedIndex < model.getSize()
            Flower flowerToRemove = selectedFlowersModel.getElementAt(selectedIndex);
            selectedFlowersModel.removeElement(flowerToRemove);
            currentFlowerQuantities.remove(flowerToRemove);
            updateSelectedFlowersDisplay();
        }
    }

    private void updateSelectedFlowersDisplay() {
        // selectedFlowersDisplayModel та selectedFlowersModel, currentFlowerQuantities вже мають бути ініціалізовані
        selectedFlowersDisplayModel.clear();
        for (int i = 0; i < selectedFlowersModel.getSize(); i++) {
            Flower flower = selectedFlowersModel.getElementAt(i);
            int quantity = currentFlowerQuantities.getOrDefault(flower, 0);
            selectedFlowersDisplayModel.addElement(flower.getShortInfo() + " (x" + quantity + ")");
        }
    }


    private void addAccessoryToList() {
        Accessory selectedAccessory = availableAccessoriesList.getSelectedValue();
        // selectedAccessoriesModel вже має бути ініціалізований
        if (selectedAccessory != null && !selectedAccessoriesModel.contains(selectedAccessory)) {
            selectedAccessoriesModel.addElement(selectedAccessory);
        }
    }

    private void removeAccessoryFromList() {
        Accessory selectedAccessory = selectedAccessoriesDisplayList.getSelectedValue();
        // selectedAccessoriesModel вже має бути ініціалізований
        if (selectedAccessory != null) {
            selectedAccessoriesModel.removeElement(selectedAccessory);
        }
    }


    @Override
    protected void populateFields() {
        // Моделі (currentFlowerQuantities, selectedFlowersModel, selectedAccessoriesModel)
        // вже мають бути ініціалізовані на момент виклику цього методу
        // (вони ініціалізуються в createFormPanel, який викликається раніше, ніж populateFields з конструктора)
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

    @Override
    protected boolean saveItem() {
        try {
            String name = nameField.getText().trim();
            String description = descriptionArea.getText().trim();
            double discount = (Double) discountSpinner.getValue();
            String imagePath = imagePathField.getText().trim();

            if (name.isEmpty()) {
                showErrorDialog("Назва букета не може бути порожньою.");
                nameField.requestFocusInWindow();
                return false;
            }
            // selectedFlowersModel та selectedAccessoriesModel вже мають бути ініціалізовані
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

            List<Flower> finalSelectedFlowers = new ArrayList<>();
            for (int i = 0; i < selectedFlowersModel.getSize(); i++) {
                finalSelectedFlowers.add(selectedFlowersModel.getElementAt(i));
            }

            List<Accessory> finalSelectedAccessories = new ArrayList<>();
            for (int i = 0; i < selectedAccessoriesModel.getSize(); i++) {
                finalSelectedAccessories.add(selectedAccessoriesModel.getElementAt(i));
            }


            if (item == null) {
                item = new Bouquet(name, description, finalSelectedFlowers, finalSelectedAccessories, imagePath, discount);
            } else {
                item.setName(name);
                item.setDescription(description);
                item.setDiscount(discount);
                item.setImagePath(imagePath);
                item.clearFlowers();
                item.addFlowers(finalSelectedFlowers); // Цей метод має обробляти кількість правильно, або...
                item.clearAccessories();
                item.addAccessories(finalSelectedAccessories);
            }

            // Оновлюємо кількість квітів в об'єкті букета
            // currentFlowerQuantities та finalSelectedFlowers мають бути узгоджені
            for(Flower f : finalSelectedFlowers) {
                item.setFlowerQuantity(f, currentFlowerQuantities.getOrDefault(f, 1));
            }

            return true;
        } catch (NumberFormatException ex) {
            showErrorDialog("Будь ласка, перевірте числові значення (знижка).");
            return false;
        } catch (Exception ex) {
            showErrorDialog("Сталася помилка: " + ex.getMessage());
            ex.printStackTrace();
            return false;
        }
    }

    public Bouquet getBouquet() {
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