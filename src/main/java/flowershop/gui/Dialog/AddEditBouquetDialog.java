package flowershop.gui.Dialog;

import flowershop.models.Bouquet;
import flowershop.models.Flower;
import flowershop.models.Accessory;
import flowershop.services.AccessoryService; // Переконайтесь, що цей імпорт є
import flowershop.services.FlowerService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 * Діалогове вікно для додавання або редагування букета.
 */
public class AddEditBouquetDialog extends JDialog {
    private static final Logger logger = LogManager.getLogger(AddEditBouquetDialog.class);

    JTextField nameField;
    JTextArea descriptionArea;
    JSpinner discountSpinner;

    JList<Flower> availableFlowersList;
    JList<Accessory> availableAccessoriesList;
    JList<String> selectedFlowersDisplayList;
    JList<Accessory> selectedAccessoriesDisplayList;

    JButton addFlowerButton;
    JButton removeFlowerButton;
    JButton addAccessoryButton;
    JButton removeAccessoryButton;
    JSpinner flowerQuantitySpinner;

    List<Flower> availableFlowersSource;
    List<Accessory> availableAccessoriesSource;
    private DefaultListModel<Flower> selectedFlowersModel;
    private DefaultListModel<String> selectedFlowersDisplayModel;
    private DefaultListModel<Accessory> selectedAccessoriesModel;

    Map<Flower, Integer> currentFlowerQuantities;

    private Bouquet bouquet;
    private boolean saved = false;

    protected JTextField imagePathField;
    protected JButton browseButton;
    protected JButton okButton, cancelButton;
    protected JLabel previewImageLabel;

    protected static final Color PRIMARY_COLOR = new Color(75, 175, 80);
    protected static final Color BACKGROUND_COLOR = new Color(245, 245, 245);
    public static final Color TEXT_COLOR = new Color(50, 50, 50);
    protected static final Font DEFAULT_FONT = new Font("SansSerif", Font.PLAIN, 12);
    protected static final Font BOLD_FONT = new Font("SansSerif", Font.BOLD, 12);
    protected static final Border FIELD_BORDER = new CompoundBorder(
            new LineBorder(Color.LIGHT_GRAY),
            new EmptyBorder(5, 5, 5, 5)
    );

    /**
     * Конструктор діалогового вікна для додавання або редагування букета.
     *
     * @param parent                  Батьківське вікно.
     * @param bouquetToEdit           Букет для редагування (null для створення нового).
     * @param allAvailableFlowers     Список доступних квітів.
     * @param allAvailableAccessories Список доступних аксесуарів.
     */
    public AddEditBouquetDialog(JFrame parent, Bouquet bouquetToEdit,
                                List<Flower> allAvailableFlowers, List<Accessory> allAvailableAccessories) {
        super(parent, bouquetToEdit == null ? "Додати букет" : "Редагувати букет", true);
        this.bouquet = bouquetToEdit;
        this.availableFlowersSource = allAvailableFlowers != null ? new ArrayList<>(allAvailableFlowers) : new ArrayList<>();
        this.availableAccessoriesSource = allAvailableAccessories != null ? new ArrayList<>(allAvailableAccessories) : new ArrayList<>();

        initializeUI();

        if (this.bouquet != null) {
            populateFields();
        }

        if (!GraphicsEnvironment.isHeadless()) {
            pack();
            setSize(new Dimension(950, 800));
            setLocationRelativeTo(parent);
            setResizable(true);
            SwingUtilities.invokeLater(() -> {
                if (nameField != null) {
                    nameField.requestFocusInWindow();
                }
            });
        }
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        logger.info("Діалогове вікно '{}' ініціалізовано.", getTitle());
    }

    private void initializeUI() {
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(BACKGROUND_COLOR);
        ((JPanel) getContentPane()).setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        add(createMainFormPanel(), BorderLayout.CENTER);
        add(createButtonPanel(), BorderLayout.SOUTH);
    }

    private JPanel createMainFormPanel() {
        this.selectedFlowersModel = new DefaultListModel<>();
        this.selectedFlowersDisplayModel = new DefaultListModel<>();
        this.selectedAccessoriesModel = new DefaultListModel<>();
        this.currentFlowerQuantities = new HashMap<>();

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

        JPanel imageSelectionPanel = new JPanel(new BorderLayout(5, 0));
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
        addFormField(topPanel, "Зображення:", imageSelectionPanel, gbcTop, 3, 1, GridBagConstraints.HORIZONTAL);

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
        selectionMainPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

        JPanel availableItemsPanel = new JPanel(new GridLayout(2, 1, 0, 10));
        availableItemsPanel.setOpaque(false);
        availableItemsPanel.add(createAvailableItemsSubPanel("Доступні квіти", true));
        availableItemsPanel.add(createAvailableItemsSubPanel("Доступні аксесуари", false));

        JPanel selectedItemsPanel = new JPanel(new GridLayout(2, 1, 0, 10));
        selectedItemsPanel.setOpaque(false);
        selectedItemsPanel.add(createSelectedItemsSubPanel("Квіти в букеті", true));
        selectedItemsPanel.add(createSelectedItemsSubPanel("Аксесуари в букеті", false));

        selectionMainPanel.add(availableItemsPanel);
        selectionMainPanel.add(selectedItemsPanel);

        formPanel.add(selectionMainPanel, BorderLayout.CENTER);

        setupEnterNavigation(nameField, descriptionArea, discountSpinner);

        return formPanel;
    }

    private JPanel createAvailableItemsSubPanel(String title, boolean isFlowers) {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createTitledBorder(title));

        if (isFlowers) {
            availableFlowersList = new JList<>(availableFlowersSource.toArray(new Flower[0]));
            availableFlowersList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            availableFlowersList.setCellRenderer(new FlowerRenderer());
            availableFlowersList.addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent evt) {
                    if (evt.getClickCount() == 2 && availableFlowersList.getSelectedIndex() != -1) {
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
            availableAccessoriesList = new JList<>(availableAccessoriesSource.toArray(new Accessory[0]));
            availableAccessoriesList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            availableAccessoriesList.setCellRenderer(new AccessoryRenderer());
            availableAccessoriesList.addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent evt) {
                    if (evt.getClickCount() == 2 && availableAccessoriesList.getSelectedIndex() != -1) {
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
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createTitledBorder(title));

        if (isFlowers) {
            selectedFlowersDisplayList = new JList<>(selectedFlowersDisplayModel);
            selectedFlowersDisplayList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            selectedFlowersDisplayList.addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent evt) {
                    if (evt.getClickCount() == 2 && selectedFlowersDisplayList.getSelectedIndex() != -1) {
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
                    if (evt.getClickCount() == 2 && selectedAccessoriesDisplayList.getSelectedIndex() != -1) {
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

    /**
     * Додає вибрану квітку до списку квітів у букеті.
     */
    void addFlowerToList() {
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
        }
    }

    /**
     * Видаляє вибрану квітку зі списку квітів у букеті.
     */
    void removeFlowerFromList() {
        int selectedIndex = selectedFlowersDisplayList.getSelectedIndex();
        if (selectedIndex != -1 && selectedIndex < selectedFlowersModel.getSize()) {
            Flower flowerToRemove = selectedFlowersModel.getElementAt(selectedIndex);
            selectedFlowersModel.removeElement(flowerToRemove);
            currentFlowerQuantities.remove(flowerToRemove);
            updateSelectedFlowersDisplay();
        }
    }

    /**
     * Оновлює відображення списку квітів у букеті.
     */
    private void updateSelectedFlowersDisplay() {
        selectedFlowersDisplayModel.clear();
        for (int i = 0; i < selectedFlowersModel.getSize(); i++) {
            Flower flower = selectedFlowersModel.getElementAt(i);
            FlowerService flowerService = new FlowerService(flower);
            int quantity = currentFlowerQuantities.getOrDefault(flower, 1);
            selectedFlowersDisplayModel.addElement(flowerService.getShortInfo() + " (x" + quantity + ")");
        }
    }

    /**
     * Додає вибраний аксесуар до списку аксесуарів у букеті.
     */
    void addAccessoryToList() {
        Accessory selectedAccessory = availableAccessoriesList.getSelectedValue();
        if (selectedAccessory != null && !selectedAccessoriesModel.contains(selectedAccessory)) {
            selectedAccessoriesModel.addElement(selectedAccessory);
        }
    }

    /**
     * Видаляє вибраний аксесуар зі списку аксесуарів у букеті.
     */
    void removeAccessoryFromList() {
        Accessory selectedAccessory = selectedAccessoriesDisplayList.getSelectedValue();
        if (selectedAccessory != null) {
            selectedAccessoriesModel.removeElement(selectedAccessory);
        }
    }

    /**
     * Заповнює поля даними з букета для редагування.
     */
    protected void populateFields() {
        if (bouquet != null) {
            nameField.setText(bouquet.getName());
            descriptionArea.setText(bouquet.getDescription());
            discountSpinner.setValue(bouquet.getDiscount());
            imagePathField.setText(bouquet.getImagePath());
            updatePreviewImage(bouquet.getImagePath());

            currentFlowerQuantities.clear();
            selectedFlowersModel.clear();
            if (bouquet.getFlowers() != null) {
                Map<Flower, Integer> bouquetFlowerQuantities = new HashMap<>();
                for(Flower f : bouquet.getFlowers()){
                    Flower representativeFlower = findRepresentativeFlower(f, availableFlowersSource);
                    if (representativeFlower == null) representativeFlower = f;

                    bouquetFlowerQuantities.put(representativeFlower, bouquetFlowerQuantities.getOrDefault(representativeFlower, 0) +1);
                }

                for (Map.Entry<Flower, Integer> entry : bouquetFlowerQuantities.entrySet()) {
                    selectedFlowersModel.addElement(entry.getKey());
                    currentFlowerQuantities.put(entry.getKey(), entry.getValue());
                }
            }
            updateSelectedFlowersDisplay();

            selectedAccessoriesModel.clear();
            if (bouquet.getAccessories() != null) {
                for (Accessory acc : bouquet.getAccessories()) {
                    Accessory representativeAccessory = findRepresentativeAccessory(acc, availableAccessoriesSource);
                    if (representativeAccessory != null && !selectedAccessoriesModel.contains(representativeAccessory)) {
                        selectedAccessoriesModel.addElement(representativeAccessory);
                    } else if (!selectedAccessoriesModel.contains(acc)) {
                        selectedAccessoriesModel.addElement(acc);
                    }
                }
            }
        }
    }

    Flower findRepresentativeFlower(Flower target, List<Flower> sourceList) {
        if (target == null || sourceList == null) return null;
        for (Flower f : sourceList) {
            if (f.getId() == target.getId()) {
                return f;
            }
        }
        return null;
    }

    Accessory findRepresentativeAccessory(Accessory target, List<Accessory> sourceList) {
        if (target == null || sourceList == null) return null;
        for (Accessory a : sourceList) {
            if (a.getId() == target.getId()) {
                return a;
            }
        }
        return null;
    }

    /**
     * Зберігає дані букета.
     *
     * @return true, якщо збереження пройшло успішно, false — якщо виникла помилка.
     */
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

            if (selectedFlowersModel.isEmpty() && selectedAccessoriesModel.isEmpty()) {
                showErrorDialog("Букет повинен містити хоча б одну квітку або аксесуар.");
                return false;
            }

            String finalImagePath = imagePath;
            if (!imagePath.isEmpty()) {
                File imageFile = new File(imagePath);
                if (!imageFile.isFile() || !imageFile.exists()) {
                    int response = JOptionPane.NO_OPTION;
                    if (!GraphicsEnvironment.isHeadless()) {
                        response = JOptionPane.showConfirmDialog(this,
                                "Файл зображення за вказаним шляхом не знайдено або це не файл.\nПродовжити збереження без зображення (або з поточним, якщо є)?",
                                "Попередження: Зображення",
                                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                    } else {
                        response = JOptionPane.YES_OPTION;
                    }

                    if (response == JOptionPane.NO_OPTION) {
                        if (!GraphicsEnvironment.isHeadless()) imagePathField.requestFocusInWindow();
                        return false;
                    } else {
                        if (bouquet != null && bouquet.getImagePath() != null && !bouquet.getImagePath().isEmpty()) {
                            File oldImageFile = new File(bouquet.getImagePath());
                            if (oldImageFile.isFile() && oldImageFile.exists()) {
                                finalImagePath = bouquet.getImagePath();
                            } else {
                                finalImagePath = "";
                            }
                        } else {
                            finalImagePath = "";
                        }
                    }
                }
            }

            List<Flower> finalSelectedFlowersWithQuantities = new ArrayList<>();
            for (int i = 0; i < selectedFlowersModel.getSize(); i++) {
                Flower baseFlower = selectedFlowersModel.getElementAt(i);
                int quantity = currentFlowerQuantities.getOrDefault(baseFlower, 1);
                for(int q = 0; q < quantity; q++) {
                    finalSelectedFlowersWithQuantities.add(new Flower(baseFlower));
                }
            }

            List<Accessory> finalSelectedAccessories = new ArrayList<>();
            for (int i = 0; i < selectedAccessoriesModel.getSize(); i++) {
                finalSelectedAccessories.add(new Accessory(selectedAccessoriesModel.getElementAt(i)));
            }

            if (bouquet == null) {
                bouquet = new Bouquet(name, description, new ArrayList<>(), new ArrayList<>(), finalImagePath, discount);
            }
            bouquet.setName(name);
            bouquet.setDescription(description);
            bouquet.setDiscount(discount);
            bouquet.setImagePath(finalImagePath);

            bouquet.setFlowers(finalSelectedFlowersWithQuantities);
            bouquet.setAccessories(finalSelectedAccessories);

            logger.info("Букет '{}' підготовлено до збереження.", bouquet.getName());
            return true;
        } catch (NumberFormatException ex) {
            showErrorDialog("Будь ласка, перевірте числові значення (знижка).");
            logger.warn("Помилка формату числа при збереженні букета: {}", ex.getMessage());
            return false;
        } catch (Exception ex) {
            showErrorDialog("Сталася помилка: " + ex.getMessage());
            logger.error("Загальна помилка при збереженні букета: {}", ex.getMessage(), ex);
            ex.printStackTrace();
            return false;
        }
    }

    private JPanel createButtonPanel() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        buttonPanel.setBackground(BACKGROUND_COLOR);

        okButton = createStyledButton("Зберегти", "/icons/save.png");
        okButton.addActionListener(e -> saveAndClose());
        buttonPanel.add(okButton);

        cancelButton = createStyledButton("Скасувати", "/icons/cancel.png");
        cancelButton.addActionListener(e -> cancelAndClose());
        buttonPanel.add(cancelButton);

        return buttonPanel;
    }

    /**
     * Зберігає зміни та закриває діалогове вікно.
     */
    void saveAndClose() {
        if (saveItem()) {
            saved = true;
            dispose();
            logger.info("Дані успішно збережено, діалог закрито.");
        }
    }

    /**
     * Скасовує зміни та закриває діалогове вікно.
     */
    void cancelAndClose() {
        saved = false;
        dispose();
        logger.info("Операцію скасовано, діалог закрито.");
    }

    /**
     * Перевіряє, чи було збережено зміни.
     *
     * @return true, якщо зміни були збережені, false — якщо ні.
     */
    public boolean isSaved() {
        return saved;
    }

    /**
     * Повертає об'єкт букета.
     *
     * @return Об'єкт букета.
     */
    public Bouquet getBouquet() {
        return bouquet;
    }

    protected JTextField createStyledTextField(int columns) {
        JTextField field = new JTextField(columns);
        field.setBorder(FIELD_BORDER);
        field.setFont(DEFAULT_FONT);
        return field;
    }

    protected JTextArea createStyledTextArea(int rows, int columns) {
        JTextArea area = new JTextArea(rows, columns);
        area.setBorder(FIELD_BORDER);
        area.setFont(DEFAULT_FONT);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        return area;
    }

    protected JButton createStyledButton(String text, String iconPath) {
        JButton button = new JButton(text);
        if (iconPath != null && !GraphicsEnvironment.isHeadless()) {
            try {
                ImageIcon icon = new ImageIcon(getClass().getResource(iconPath));
                if (icon.getIconWidth() > 0) {
                    Image img = icon.getImage().getScaledInstance(16, 16, Image.SCALE_SMOOTH);
                    button.setIcon(new ImageIcon(img));
                } else {
                    logger.warn("Іконка {} має нульову ширину.", iconPath);
                }
            } catch (Exception e) {
                logger.warn("Не вдалося завантажити іконку: {}", iconPath, e);
            }
        }
        button.setBackground(PRIMARY_COLOR);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setFont(BOLD_FONT);
        button.setBorder(new CompoundBorder(
                new LineBorder(new Color(50, 120, 50)),
                new EmptyBorder(5, 15, 5, 15)
        ));
        if (!GraphicsEnvironment.isHeadless()) {
            button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        }

        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (!GraphicsEnvironment.isHeadless()) button.setBackground(PRIMARY_COLOR.brighter());
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (!GraphicsEnvironment.isHeadless()) button.setBackground(PRIMARY_COLOR);
            }
        });
        return button;
    }

    protected void addFormField(JPanel panel, String labelText, JComponent component, GridBagConstraints gbc, int gridy, int gridwidth, int fill) {
        JLabel label = new JLabel(labelText);
        label.setFont(DEFAULT_FONT);
        label.setForeground(TEXT_COLOR);
        gbc.gridx = 0;
        gbc.gridy = gridy;
        gbc.gridwidth = 1;
        gbc.weightx = 0.2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.LINE_START;
        panel.add(label, gbc);

        gbc.gridx = 1;
        gbc.gridwidth = gridwidth;
        gbc.weightx = 0.8;
        gbc.fill = fill;
        panel.add(component, gbc);
    }

    /**
     * Відкриває діалог вибору зображення для букета.
     */
    protected void browseImageAction() {
        if (GraphicsEnvironment.isHeadless()) {
            logger.info("Пропуск browseImageAction в headless режимі.");
            if (imagePathField != null) {
                imagePathField.setText("");
                logger.debug("Очистка поля зображення в headless режимі.");
            }
            return;
        }
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Виберіть зображення");
        fileChooser.setFileFilter(new FileNameExtensionFilter(
                "Зображення (JPG, PNG, GIF, WebP)", "jpg", "jpeg", "png", "gif", "webp"));
        fileChooser.setCurrentDirectory(new File(System.getProperty("user.home")));

        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            if (imagePathField != null) {
                imagePathField.setText(selectedFile.getAbsolutePath());
                logger.debug("Вибрано зображення: {}", selectedFile.getAbsolutePath());
            }
        }
    }

    /**
     * Оновлює прев'ю зображення букета.
     *
     * @param path Шлях до зображення.
     */
    protected void updatePreviewImage(String path) {
        if (previewImageLabel == null || GraphicsEnvironment.isHeadless()) {
            if (previewImageLabel != null) previewImageLabel.setIcon(null);
            if (previewImageLabel != null) previewImageLabel.setText("Прев'ю");
            return;
        }

        if (path != null && !path.isEmpty()) {
            File file = new File(path);
            if (file.exists() && file.isFile()) {
                try {
                    ImageIcon originalIcon = new ImageIcon(path);
                    Image image = originalIcon.getImage();

                    int previewWidth = previewImageLabel.getPreferredSize().width -10;
                    int previewHeight = previewImageLabel.getPreferredSize().height -10;
                    if (previewWidth <= 0) previewWidth = 140;
                    if (previewHeight <= 0) previewHeight = 110;

                    int originalWidth = originalIcon.getIconWidth();
                    int originalHeight = originalIcon.getIconHeight();

                    if (originalWidth > 0 && originalHeight > 0) {
                        int newWidth = originalWidth;
                        int newHeight = originalHeight;

                        if (originalWidth > previewWidth || originalHeight > previewHeight) {
                            double widthRatio = (double) previewWidth / originalWidth;
                            double heightRatio = (double) previewHeight / originalHeight;
                            double ratio = Math.min(widthRatio, heightRatio);
                            newWidth = (int) (originalWidth * ratio);
                            newHeight = (int) (originalHeight * ratio);
                        }
                        Image scaledImage = image.getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH);
                        previewImageLabel.setIcon(new ImageIcon(scaledImage));
                        previewImageLabel.setText(null);
                        return;
                    } else {
                        logger.warn("Не вдалося отримати розміри для зображення: {}", path);
                    }
                } catch (Exception e) {
                    logger.error("Помилка завантаження зображення для попереднього перегляду: {}. {}", path, e.getMessage(), e);
                }
            } else {
                logger.warn("Файл зображення не знайдено або не є файлом: {}", path);
            }
        }
        previewImageLabel.setIcon(null);
        previewImageLabel.setText("Прев'ю");
    }

    /**
     * Налаштовує навігацію між компонентами за допомогою клавіші Enter.
     *
     * @param components Компоненти для навігації.
     */
    protected void setupEnterNavigation(Component... components) {
        if (GraphicsEnvironment.isHeadless() || components == null || components.length == 0) {
            return;
        }
        for (int i = 0; i < components.length; i++) {
            final Component currentComponent = components[i];
            final Component nextComponent = (i < components.length - 1) ? components[i + 1] : okButton;

            if (currentComponent == null || nextComponent == null) {
                continue;
            }

            if (currentComponent instanceof JTextField) {
                ((JTextField) currentComponent).addActionListener(e -> {
                    if (nextComponent.isFocusable()) nextComponent.requestFocusInWindow();
                });
            } else if (currentComponent instanceof JSpinner) {
                JComponent editor = ((JSpinner) currentComponent).getEditor();
                if (editor instanceof JSpinner.DefaultEditor) {
                    JTextField textField = ((JSpinner.DefaultEditor) editor).getTextField();
                    textField.addActionListener(e -> {
                        if (nextComponent.isFocusable()) nextComponent.requestFocusInWindow();
                    });
                }
            } else if (currentComponent instanceof JTextArea) {
                ((JTextArea) currentComponent).addKeyListener(new KeyAdapter() {
                    @Override
                    public void keyPressed(KeyEvent e) {
                        if (e.getKeyCode() == KeyEvent.VK_ENTER && e.isControlDown()) {
                            if (nextComponent.isFocusable()) nextComponent.requestFocusInWindow();
                            e.consume();
                        }
                    }
                });
            } else {
                currentComponent.addKeyListener(new KeyAdapter() {
                    @Override
                    public void keyPressed(KeyEvent e) {
                        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                            if (nextComponent.isFocusable()) nextComponent.requestFocusInWindow();
                            e.consume();
                        }
                    }
                });
            }
        }
    }

    /**
     * Відображає діалогове вікно з повідомленням про помилку.
     *
     * @param message Текст повідомлення.
     */
    protected void showErrorDialog(String message) {
        logger.warn("Помилка валідації: {}", message);

        if (!GraphicsEnvironment.isHeadless()) {
            final JOptionPane optionPane = new JOptionPane(message, JOptionPane.ERROR_MESSAGE);

            final JDialog dialog = optionPane.createDialog(this, "Помилка введення");

            Timer timer = new Timer(5000, new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (dialog.isVisible()) {
                        dialog.setVisible(false);
                        dialog.dispose();
                    }
                }
            });
            timer.setRepeats(false);
            timer.start();

            dialog.setVisible(true);
        }
    }

    static class FlowerRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value instanceof Flower) {
                Flower flower = (Flower) value;
                FlowerService flowerService = new FlowerService(flower);
                label.setText(flowerService.getShortInfo());
            }
            return label;
        }
    }

    /**
     * Клас для кастомного відображення об'єктів Accessory у списках JList.
     * Використовує {@link AccessoryService} для отримання короткої інформації про аксесуар.
     */
    static class AccessoryRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value instanceof Accessory) {
                Accessory accessory = (Accessory) value;
                AccessoryService accessoryService = new AccessoryService(accessory);
                label.setText(accessoryService.getShortInfo());
            }
            return label;
        }
    }
}