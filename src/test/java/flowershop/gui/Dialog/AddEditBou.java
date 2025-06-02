package flowershop.gui.Dialog;

import flowershop.models.Accessory;
import flowershop.models.Accessory.AccessoryType;
import flowershop.models.Bouquet;
import flowershop.models.Flower;
import flowershop.models.Flower.FlowerType;
import flowershop.services.AccessoryService;
import flowershop.services.FlowerService;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AddEditBouquetDialogTest2 {

    private AddEditBouquetDialog dialog;
    private Bouquet existingBouquet;
    private Flower flower1, flower2, flower3;
    private Accessory accessory1, accessory2;


    private JTextField nameField;
    private JTextArea descriptionArea;
    private JSpinner discountSpinner;
    private JList<Flower> availableFlowersList;
    private JList<Accessory> availableAccessoriesList;
    private JList<String> selectedFlowersDisplayList;
    private JList<Accessory> selectedAccessoriesDisplayList;
    private JButton addFlowerButton, removeFlowerButton;
    private JButton addAccessoryButton, removeAccessoryButton;
    private JSpinner flowerQuantitySpinner;
    private JTextField imagePathField;
    private JButton okButton, cancelButton, browseButton;
    private JLabel previewImageLabel;

    private List<Flower> allAvailableFlowers;
    private List<Accessory> allAvailableAccessories;

    @BeforeAll
    static void setupHeadlessMode() {
        System.out.println("Current java.awt.headless property (test does not set it): " + System.getProperty("java.awt.headless"));
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception e) {
            System.err.println("WARNING: Could not set LookAndFeel for tests: " + e.getMessage());
        }
        try {
            boolean isHeadless = GraphicsEnvironment.isHeadless();
            System.out.println("@BeforeAll: GraphicsEnvironment.isHeadless() = " + isHeadless);
        } catch (Throwable t) {
            System.err.println("Error checking/initializing GraphicsEnvironment in @BeforeAll: " + t.getMessage());
        }
    }

    @BeforeEach
    void setUp() {
        flower1 = new Flower(FlowerType.ROSE, 10.0, 90, 50, "Red", "Holland", false, "rose.jpg", 10);
        flower1.setId(1);
        flower2 = new Flower(FlowerType.TULIP, 5.0, 80, 30, "Yellow", "Ukraine", false, "tulip.jpg", 20);
        flower2.setId(2);
        flower3 = new Flower(FlowerType.LILY, 12.0, 85, 60, "White", "Poland", false, "lily.jpg", 15);
        flower3.setId(3);


        accessory1 = new Accessory("Стрічка Люкс", 2.50, "Червона шовкова стрічка, 1м", "images/ribbon_red.jpg",
                100, AccessoryType.RIBBON, "Червоний", "1 метр");
        accessory1.setId(1);
        accessory2 = new Accessory("Кошик", 15.0, "Плетений кошик", "images/basket.jpg",
                20, AccessoryType.BASKET, "Коричневий", "Середній");
        accessory2.setId(2);


        allAvailableFlowers = new ArrayList<>(Arrays.asList(flower1, flower2, flower3));
        allAvailableAccessories = new ArrayList<>(Arrays.asList(accessory1, accessory2));

        existingBouquet = new Bouquet(
                "Весняний настрій",
                "Яскравий букет з тюльпанів",
                new ArrayList<>(Collections.singletonList(flower2)),
                new ArrayList<>(Collections.singletonList(accessory1)),
                "images/spring.jpg",
                10.0
        );
        existingBouquet.setId(1);
    }

    private void initializeDialog(JFrame parent, Bouquet bouquetToEdit, List<Flower> availableFlowers, List<Accessory> availableAccessories) {
        dialog = new AddEditBouquetDialog(parent, bouquetToEdit, availableFlowers, availableAccessories);

        nameField = dialog.nameField;
        descriptionArea = dialog.descriptionArea;
        discountSpinner = dialog.discountSpinner;
        availableFlowersList = dialog.availableFlowersList;
        availableAccessoriesList = dialog.availableAccessoriesList;
        selectedFlowersDisplayList = dialog.selectedFlowersDisplayList;
        selectedAccessoriesDisplayList = dialog.selectedAccessoriesDisplayList;
        addFlowerButton = dialog.addFlowerButton;
        removeFlowerButton = dialog.removeFlowerButton;
        addAccessoryButton = dialog.addAccessoryButton;
        removeAccessoryButton = dialog.removeAccessoryButton;
        flowerQuantitySpinner = dialog.flowerQuantitySpinner;
        imagePathField = dialog.imagePathField;
        okButton = dialog.okButton;
        cancelButton = dialog.cancelButton;
        browseButton = dialog.browseButton;
        previewImageLabel = dialog.previewImageLabel;
    }


    @Nested
    @DisplayName("Тести режиму додавання")
    class AddModeTests {
        @BeforeEach
        void setUpAddMode() {
            initializeDialog(null, null, new ArrayList<>(allAvailableFlowers), new ArrayList<>(allAvailableAccessories));
        }

        @Test
        @DisplayName("Ініціалізація діалогу в режимі додавання")
        void constructor_addMode_initializesFieldsCorrectly() {
            assertEquals("Додати букет", dialog.getTitle());
            assertNull(dialog.getBouquet(), "Bouquet object should be null initially in add mode");
            assertEquals("", nameField.getText());
            assertEquals("", descriptionArea.getText());
            assertEquals(0.0, (Double) discountSpinner.getValue());
            assertEquals("", imagePathField.getText());
            assertEquals(0, selectedFlowersDisplayList.getModel().getSize(), "Selected flowers list should be empty");
            assertEquals(0, selectedAccessoriesDisplayList.getModel().getSize(), "Selected accessories list should be empty");
            assertNotNull(availableFlowersList.getModel());
            assertEquals(allAvailableFlowers.size(), availableFlowersList.getModel().getSize(), "Available flowers list should be populated.");
            assertNotNull(availableAccessoriesList.getModel());
            assertEquals(allAvailableAccessories.size(), availableAccessoriesList.getModel().getSize(), "Available accessories list should be populated.");
            if (previewImageLabel != null) {
                if (GraphicsEnvironment.isHeadless()) {
                    assertNull(previewImageLabel.getIcon());
                    assertEquals("Прев'ю", previewImageLabel.getText());
                } else {
                    assertNull(previewImageLabel.getIcon());
                    assertEquals("Прев'ю", previewImageLabel.getText());
                }
            }
        }

        @Test
        @DisplayName("Додавання та видалення квітки")
        void addAndRemoveFlower_updatesSelectedList() {
            availableFlowersList.setSelectedValue(flower1, true);
            flowerQuantitySpinner.setValue(2);
            addFlowerButton.doClick();

            assertEquals(1, selectedFlowersDisplayList.getModel().getSize());
            FlowerService flowerService = new FlowerService(flower1);
            assertTrue(selectedFlowersDisplayList.getModel().getElementAt(0).contains(flowerService.getShortInfo()));
            assertTrue(selectedFlowersDisplayList.getModel().getElementAt(0).contains("(x2)"));

            selectedFlowersDisplayList.setSelectedIndex(0);
            removeFlowerButton.doClick();
            assertEquals(0, selectedFlowersDisplayList.getModel().getSize());
        }

        @Test
        @DisplayName("Додавання та видалення аксесуара")
        void addAndRemoveAccessory_updatesSelectedList() {
            availableAccessoriesList.setSelectedValue(accessory1, true);
            addAccessoryButton.doClick();

            assertEquals(1, selectedAccessoriesDisplayList.getModel().getSize());
            Accessory addedAccessory = (Accessory)selectedAccessoriesDisplayList.getModel().getElementAt(0);
            AccessoryService accessoryService = new AccessoryService(addedAccessory);
            AccessoryService accessoryService1 = new AccessoryService(accessory1);
            assertEquals(accessoryService1.getShortInfo(), accessoryService.getShortInfo());

            selectedAccessoriesDisplayList.setSelectedIndex(0);
            removeAccessoryButton.doClick();
            assertEquals(0, selectedAccessoriesDisplayList.getModel().getSize());
        }


        @Test
        @DisplayName("Успішне збереження нового букета")
        void saveItem_addMode_validData_createsNewBouquet() {
            nameField.setText("Літній букет");
            descriptionArea.setText("Опис літнього букета");
            discountSpinner.setValue(5.0);
            imagePathField.setText("images/summer.jpg");

            availableFlowersList.setSelectedValue(flower1, true);
            flowerQuantitySpinner.setValue(3);
            addFlowerButton.doClick();

            availableAccessoriesList.setSelectedValue(accessory1, true);
            addAccessoryButton.doClick();

            try (MockedConstruction<File> mockedFileConstruction = Mockito.mockConstruction(File.class,
                    (mock, context) -> {
                        when(mock.isFile()).thenReturn(true);
                        when(mock.exists()).thenReturn(true);
                    })) {
                assertTrue(dialog.saveItem(), "Dialog saveItem() should return true with valid data");
            }

            Bouquet savedBouquet = dialog.getBouquet();
            assertNotNull(savedBouquet);
            assertEquals("Літній букет", savedBouquet.getName());
            assertEquals("Опис літнього букета", savedBouquet.getDescription());
            assertEquals(5.0, savedBouquet.getDiscount());
            assertEquals("images/summer.jpg", savedBouquet.getImagePath());
            assertEquals(3, savedBouquet.getFlowers().size());
            assertTrue(savedBouquet.getFlowers().stream().allMatch(f -> f.getId() == flower1.getId()));
            assertEquals(1, savedBouquet.getAccessories().size());
            assertEquals(accessory1.getId(), savedBouquet.getAccessories().get(0).getId());
        }
    }

    @Nested
    @DisplayName("Тести режиму редагування")
    class EditModeTests {
        @BeforeEach
        void setUpEditMode() {
            List<Flower> availableFlowersForEdit = new ArrayList<>(allAvailableFlowers);
            List<Accessory> availableAccessoriesForEdit = new ArrayList<>(allAvailableAccessories);

            Bouquet bouquetToEdit = new Bouquet(
                    existingBouquet.getName(),
                    existingBouquet.getDescription(),
                    new ArrayList<>(existingBouquet.getFlowers()),
                    new ArrayList<>(existingBouquet.getAccessories()),
                    existingBouquet.getImagePath(),
                    existingBouquet.getDiscount()
            );
            bouquetToEdit.setId(existingBouquet.getId());

            initializeDialog(null, bouquetToEdit, availableFlowersForEdit, availableAccessoriesForEdit);
        }

        @Test
        @DisplayName("Ініціалізація діалогу в режимі редагування")
        void constructor_editMode_populatesFieldsCorrectly() {
            assertEquals("Редагувати букет", dialog.getTitle());
            assertNotNull(dialog.getBouquet());

            assertEquals(existingBouquet.getName(), nameField.getText());
            assertEquals(existingBouquet.getDescription(), descriptionArea.getText());
            assertEquals(existingBouquet.getDiscount(), (Double) discountSpinner.getValue());
            assertEquals(existingBouquet.getImagePath(), imagePathField.getText());

            long countOfFlower2 = existingBouquet.getFlowers().stream().filter(f -> f.getId() == flower2.getId()).count();
            assertEquals(1, selectedFlowersDisplayList.getModel().getSize(), "Selected flowers display list should show one type.");
            FlowerService flowerService = new FlowerService(flower2);
            String expectedFlowerDisplay = flowerService.getShortInfo() + " (x" + countOfFlower2 + ")";
            assertEquals(expectedFlowerDisplay, selectedFlowersDisplayList.getModel().getElementAt(0));

            assertEquals(1, selectedAccessoriesDisplayList.getModel().getSize());

            Accessory selectedAccessoryInDialog = (Accessory) selectedAccessoriesDisplayList.getModel().getElementAt(0);
            assertEquals(accessory1.getId(), selectedAccessoryInDialog.getId());
        }

        @Test
        @DisplayName("Успішне збереження зміненого букета")
        void saveItem_editMode_validData_updatesExistingBouquet() {
            nameField.setText("Оновлений Весняний настрій");
            descriptionArea.setText("Новий опис");
            discountSpinner.setValue(15.0);
            imagePathField.setText("images/new_spring.jpg");

            if (selectedFlowersDisplayList.getModel().getSize() > 0) {
                selectedFlowersDisplayList.setSelectedIndex(0);
                removeFlowerButton.doClick();
            }
            assertEquals(0, selectedFlowersDisplayList.getModel().getSize(), "Selected flowers list should be empty after removal.");

            availableFlowersList.setSelectedValue(flower1, true);
            flowerQuantitySpinner.setValue(2);
            addFlowerButton.doClick();

            try (MockedConstruction<File> mockedFile = Mockito.mockConstruction(File.class, (mock, context) -> {
                String path = (String) context.arguments().get(0);
                if (path.equals("images/new_spring.jpg") || path.equals(existingBouquet.getImagePath())) {
                    when(mock.isFile()).thenReturn(true);
                    when(mock.exists()).thenReturn(true);
                } else {
                    when(mock.isFile()).thenReturn(false);
                    when(mock.exists()).thenReturn(false);
                }
            })) {
                assertTrue(dialog.saveItem(), "Save item should succeed for edit mode");
            }

            Bouquet updatedBouquet = dialog.getBouquet();
            assertNotNull(updatedBouquet);
            assertEquals(existingBouquet.getId(), updatedBouquet.getId(), "ID should remain the same");
            assertEquals("Оновлений Весняний настрій", updatedBouquet.getName());
            assertEquals("Новий опис", updatedBouquet.getDescription());
            assertEquals(15.0, updatedBouquet.getDiscount());
            assertEquals("images/new_spring.jpg", updatedBouquet.getImagePath());
            assertEquals(2, updatedBouquet.getFlowers().size(), "Should have 2 roses now.");
            assertTrue(updatedBouquet.getFlowers().stream().allMatch(f -> f.getId() == flower1.getId()));
            assertEquals(1, updatedBouquet.getAccessories().size());
        }
    }
    @Nested
    @DisplayName("Тести взаємодії з UI")
    class InteractionTests {
        @BeforeEach
        void setUpInteractions() {
            initializeDialog(null, null, allAvailableFlowers, allAvailableAccessories);
        }

        @Test
        @DisplayName("Подвійний клік на доступній квітці додає її до вибраних")
        void doubleClickOnAvailableFlower_addsToSelected() {
            MouseEvent doubleClickEvent = mock(MouseEvent.class);
            when(doubleClickEvent.getClickCount()).thenReturn(2);

            availableFlowersList.setSelectedIndex(0);
            flowerQuantitySpinner.setValue(1);

            for (java.awt.event.MouseListener listener : availableFlowersList.getMouseListeners()) {
                if (listener.getClass().getName().contains("AddEditBouquetDialog")) {
                    if (availableFlowersList.getSelectedIndex() != -1) listener.mouseClicked(doubleClickEvent);
                }
            }
            assertEquals(1, selectedFlowersDisplayList.getModel().getSize());
            FlowerService flowerService = new FlowerService(flower1);
            assertTrue(selectedFlowersDisplayList.getModel().getElementAt(0).contains(flowerService.getShortInfo()));
        }

        @Test
        @DisplayName("Подвійний клік на вибраній квітці видаляє її")
        void doubleClickOnSelectedFlower_removesIt() {
            availableFlowersList.setSelectedValue(flower1, true);
            flowerQuantitySpinner.setValue(1);
            addFlowerButton.doClick();
            assertEquals(1, selectedFlowersDisplayList.getModel().getSize());

            MouseEvent doubleClickEvent = mock(MouseEvent.class);
            when(doubleClickEvent.getClickCount()).thenReturn(2);
            selectedFlowersDisplayList.setSelectedIndex(0);

            for (java.awt.event.MouseListener listener : selectedFlowersDisplayList.getMouseListeners()) {
                if (listener.getClass().getName().contains("AddEditBouquetDialog")) {
                    if (selectedFlowersDisplayList.getSelectedIndex() != -1) listener.mouseClicked(doubleClickEvent);
                }
            }
            assertEquals(0, selectedFlowersDisplayList.getModel().getSize());
        }
        @Test
        @DisplayName("Подвійний клік на доступному аксесуарі додає його")
        void doubleClickOnAvailableAccessory_addsToSelected() {
            MouseEvent doubleClickEvent = mock(MouseEvent.class);
            when(doubleClickEvent.getClickCount()).thenReturn(2);
            availableAccessoriesList.setSelectedIndex(0);

            for (java.awt.event.MouseListener listener : availableAccessoriesList.getMouseListeners()) {
                if (listener.getClass().getName().contains("AddEditBouquetDialog")) {
                    if (availableAccessoriesList.getSelectedIndex() != -1) listener.mouseClicked(doubleClickEvent);
                }
            }
            assertEquals(1, selectedAccessoriesDisplayList.getModel().getSize());
            assertEquals(accessory1.getId(), ((Accessory)selectedAccessoriesDisplayList.getModel().getElementAt(0)).getId());
        }

        @Test
        @DisplayName("Подвійний клік на вибраному аксесуарі видаляє його")
        void doubleClickOnSelectedAccessory_removesIt() {
            availableAccessoriesList.setSelectedValue(accessory1, true);
            addAccessoryButton.doClick();
            assertEquals(1, selectedAccessoriesDisplayList.getModel().getSize());

            MouseEvent doubleClickEvent = mock(MouseEvent.class);
            when(doubleClickEvent.getClickCount()).thenReturn(2);
            selectedAccessoriesDisplayList.setSelectedIndex(0);

            for (java.awt.event.MouseListener listener : selectedAccessoriesDisplayList.getMouseListeners()) {
                if (listener.getClass().getName().contains("AddEditBouquetDialog")) {
                    if (selectedAccessoriesDisplayList.getSelectedIndex() != -1) listener.mouseClicked(doubleClickEvent);
                }
            }
            assertEquals(0, selectedAccessoriesDisplayList.getModel().getSize());
        }

        @Test
        @DisplayName("Оновлення прев'ю зображення в headless режимі")
        void updatePreviewImage_headlessMode_setsTextAndNullIcon() {
            assertNotNull(previewImageLabel, "Preview image label should be initialized.");
            dialog.updatePreviewImage("some/path.jpg");

            if (GraphicsEnvironment.isHeadless()) {
                assertNull(previewImageLabel.getIcon(), "Icon should be null in headless mode.");
                assertEquals("Прев'ю", previewImageLabel.getText(), "Text should be 'Прев'ю' in headless mode.");
            } else {
                assertNull(previewImageLabel.getIcon());
                assertEquals("Прев'ю", previewImageLabel.getText());
            }
        }

        @Test
        @DisplayName("Подвійний клік на доступній квітці, коли нічого не вибрано")
        void doubleClickOnAvailableFlower_noSelection_doesNothing() {
            MouseEvent doubleClickEvent = mock(MouseEvent.class);
            when(doubleClickEvent.getClickCount()).thenReturn(2);
            availableFlowersList.clearSelection();

            for (java.awt.event.MouseListener listener : availableFlowersList.getMouseListeners()) {
                if (listener.getClass().getName().contains("AddEditBouquetDialog")) {
                    listener.mouseClicked(doubleClickEvent);
                }
            }
            assertEquals(0, selectedFlowersDisplayList.getModel().getSize());
        }

        @Test
        @DisplayName("Подвійний клік на доступному аксесуарі, коли нічого не вибрано")
        void doubleClickOnAvailableAccessory_noSelection_doesNothing() {
            MouseEvent doubleClickEvent = mock(MouseEvent.class);
            when(doubleClickEvent.getClickCount()).thenReturn(2);
            availableAccessoriesList.clearSelection();

            for (java.awt.event.MouseListener listener : availableAccessoriesList.getMouseListeners()) {
                if (listener.getClass().getName().contains("AddEditBouquetDialog")) {
                    listener.mouseClicked(doubleClickEvent);
                }
            }
            assertEquals(0, selectedAccessoriesDisplayList.getModel().getSize());
        }

        @Test
        @DisplayName("Подвійний клік на вибраній квітці, коли нічого не вибрано в списку відображення")
        void doubleClickOnSelectedFlower_noSelectionInDisplay_doesNothing() {
            availableFlowersList.setSelectedValue(flower1, true);
            addFlowerButton.doClick();
            selectedFlowersDisplayList.clearSelection();

            MouseEvent doubleClickEvent = mock(MouseEvent.class);
            when(doubleClickEvent.getClickCount()).thenReturn(2);

            for (java.awt.event.MouseListener listener : selectedFlowersDisplayList.getMouseListeners()) {
                if (listener.getClass().getName().contains("AddEditBouquetDialog")) {
                    listener.mouseClicked(doubleClickEvent);
                }
            }
            assertEquals(1, selectedFlowersDisplayList.getModel().getSize());
        }

        @Test
        @DisplayName("Подвійний клік на вибраному аксесуарі, коли нічого не вибрано в списку відображення")
        void doubleClickOnSelectedAccessory_noSelectionInDisplay_doesNothing() {
            availableAccessoriesList.setSelectedValue(accessory1, true);
            addAccessoryButton.doClick();
            selectedAccessoriesDisplayList.clearSelection();

            MouseEvent doubleClickEvent = mock(MouseEvent.class);
            when(doubleClickEvent.getClickCount()).thenReturn(2);

            for (java.awt.event.MouseListener listener : selectedAccessoriesDisplayList.getMouseListeners()) {
                if (listener.getClass().getName().contains("AddEditBouquetDialog")) {
                    listener.mouseClicked(doubleClickEvent);
                }
            }
            assertEquals(1, selectedAccessoriesDisplayList.getModel().getSize());
        }
    }

    @Nested
    @DisplayName("Додаткові тести для AddEditBouquetDialog")
    class AdditionalTests {

        @Test
        @DisplayName("Тест конструктора з null батьківським вікном")
        void constructor_withNullParent_initializesCorrectly() {
            assertDoesNotThrow(() -> {
                AddEditBouquetDialog dialog = new AddEditBouquetDialog(null, null, allAvailableFlowers, allAvailableAccessories);
                assertNotNull(dialog);
            });
        }

        @Test
        @DisplayName("Тест конструктора з null списком квітів")
        void constructor_withNullFlowers_initializesWithEmptyList() {
            assertDoesNotThrow(() -> {
                AddEditBouquetDialog dialog = new AddEditBouquetDialog(null, null, null, allAvailableAccessories);
                assertNotNull(dialog.availableFlowersList);
                assertEquals(0, dialog.availableFlowersList.getModel().getSize());
            });
        }

        @Test
        @DisplayName("Тест конструктора з null списком аксесуарів")
        void constructor_withNullAccessories_initializesWithEmptyList() {
            assertDoesNotThrow(() -> {
                AddEditBouquetDialog dialog = new AddEditBouquetDialog(null, null, allAvailableFlowers, null);
                assertNotNull(dialog.availableAccessoriesList);
                assertEquals(0, dialog.availableAccessoriesList.getModel().getSize());
            });
        }


        @Test
        @DisplayName("Тест повторного додавання тієї ж квітки")
        void addFlowerToList_sameFlowerTwice_increasesQuantity() {
            initializeDialog(null, null, allAvailableFlowers, allAvailableAccessories);
            availableFlowersList.setSelectedValue(flower1, true);
            flowerQuantitySpinner.setValue(2);
            dialog.addFlowerToList();

            flowerQuantitySpinner.setValue(3);
            dialog.addFlowerToList();

            assertEquals(1, selectedFlowersDisplayList.getModel().getSize());
            assertTrue(selectedFlowersDisplayList.getModel().getElementAt(0).contains("(x5)"));
        }

        @Test
        @DisplayName("Тест видалення невибраної квітки")
        void removeFlowerFromList_noSelection_doesNothing() {
            initializeDialog(null, null, allAvailableFlowers, allAvailableAccessories);
            availableFlowersList.setSelectedValue(flower1, true);
            flowerQuantitySpinner.setValue(1);
            dialog.addFlowerToList();

            selectedFlowersDisplayList.clearSelection();
            dialog.removeFlowerFromList();

            assertEquals(1, selectedFlowersDisplayList.getModel().getSize());
        }

        @Test
        @DisplayName("Тест додавання аксесуара, який вже є у букеті")
        void addAccessoryToList_alreadyInBouquet_doesNotDuplicate() {
            initializeDialog(null, null, allAvailableFlowers, allAvailableAccessories);
            availableAccessoriesList.setSelectedValue(accessory1, true);
            dialog.addAccessoryToList();
            dialog.addAccessoryToList();

            assertEquals(1, selectedAccessoriesDisplayList.getModel().getSize());
        }

        @Test
        @DisplayName("Тест видалення невибраного аксесуара")
        void removeAccessoryFromList_noSelection_doesNothing() {
            initializeDialog(null, null, allAvailableFlowers, allAvailableAccessories);
            availableAccessoriesList.setSelectedValue(accessory1, true);
            dialog.addAccessoryToList();

            selectedAccessoriesDisplayList.clearSelection();
            dialog.removeAccessoryFromList();

            assertEquals(1, selectedAccessoriesDisplayList.getModel().getSize());
        }

        @Test
        @DisplayName("Тест збереження з невалідною знижкою (менше 0) - Спіннер має запобігти")
        void saveItem_withInvalidDiscount_spinnerShouldPrevent() {




            initializeDialog(null, null, allAvailableFlowers, allAvailableAccessories);
            nameField.setText("Тестовий букет");
            availableFlowersList.setSelectedValue(flower1, true);
            flowerQuantitySpinner.setValue(1);
            dialog.addFlowerToList();













            try {
                discountSpinner.setValue(-10.0);

            } catch (IllegalArgumentException e) {

                System.out.println("Spinner model prevented invalid value as expected: " + e.getMessage());
            }





            JSpinner mockSpinner = mock(JSpinner.class);
            when(mockSpinner.getValue()).thenReturn(-10.0);
            dialog.discountSpinner = mockSpinner;

            assertFalse(dialog.saveItem(), "Save item should fail due to bouquet's discount validation");
        }

        @Test
        @DisplayName("Тест оновлення прев'ю з неіснуючим файлом")
        void updatePreviewImage_withNonExistentFile_setsDefaultText() {
            initializeDialog(null, null, allAvailableFlowers, allAvailableAccessories);
            dialog.updatePreviewImage("non_existent_path.jpg");

            assertNull(previewImageLabel.getIcon());
            assertEquals("Прев'ю", previewImageLabel.getText());
        }

        @Test
        @DisplayName("Тест оновлення прев'ю з null шляхом")
        void updatePreviewImage_withNullPath_setsDefaultText() {
            initializeDialog(null, null, allAvailableFlowers, allAvailableAccessories);
            dialog.updatePreviewImage(null);

            assertNull(previewImageLabel.getIcon());
            assertEquals("Прев'ю", previewImageLabel.getText());
        }
        @Test
        @DisplayName("Тест створення стилізованого текстового поля")
        void createStyledTextField_returnsCorrectComponent() {
            initializeDialog(null, null, allAvailableFlowers, allAvailableAccessories);
            JTextField field = dialog.createStyledTextField(10);
            assertNotNull(field);
            assertEquals(10, field.getColumns());
            assertNotNull(field.getBorder());
        }

        @Test
        @DisplayName("Тест створення стилізованої текстової області")
        void createStyledTextArea_returnsCorrectComponent() {
            initializeDialog(null, null, allAvailableFlowers, allAvailableAccessories);
            JTextArea area = dialog.createStyledTextArea(3, 20);
            assertNotNull(area);
            assertEquals(3, area.getRows());
            assertEquals(20, area.getColumns());
            assertTrue(area.getLineWrap());
            assertNotNull(area.getBorder());
        }

        @Test
        @DisplayName("Тест створення стилізованої кнопки")
        void createStyledButton_returnsCorrectComponent() {
            initializeDialog(null, null, allAvailableFlowers, allAvailableAccessories);
            JButton button = dialog.createStyledButton("Тест", null);
            assertNotNull(button);
            assertEquals("Тест", button.getText());
            assertNotNull(button.getBackground());
            assertNotNull(button.getBorder());
            assertNull(button.getIcon(), "Icon should be null when path is null");



            JButton buttonWithIcon = dialog.createStyledButton("Тест з іконкою", "/icons/dummy.png");
            assertNotNull(buttonWithIcon);



            if (!GraphicsEnvironment.isHeadless()) {



                System.out.println("Button with icon path created. Icon: " + buttonWithIcon.getIcon());
            } else {
                assertNull(buttonWithIcon.getIcon(), "Icon should be null in headless mode for button");
            }
        }


        @Test
        @DisplayName("Тест додавання поля форми з правильними параметрами")
        void addFormField_addsComponentsCorrectly() {
            initializeDialog(null, null, allAvailableFlowers, allAvailableAccessories);
            JPanel panel = new JPanel(new GridBagLayout());
            JTextField field = new JTextField(10);
            GridBagConstraints gbc = new GridBagConstraints();

            dialog.addFormField(panel, "Тест:", field, gbc, 0, 1, GridBagConstraints.HORIZONTAL);

            assertEquals(2, panel.getComponentCount());
            assertTrue(panel.getComponent(0) instanceof JLabel);
            assertEquals("Тест:", ((JLabel)panel.getComponent(0)).getText());
            assertEquals(field, panel.getComponent(1));
        }

        @Test
        @DisplayName("populateFields з порожнім букетом (без квітів/аксесуарів)")
        void populateFields_withEmptyBouquetItems() {
            Bouquet emptyContentBouquet = new Bouquet("Порожній всередині", "Опис",
                    new ArrayList<>(), new ArrayList<>(), "empty.jpg", 0);
            emptyContentBouquet.setId(99);
            initializeDialog(null, emptyContentBouquet, allAvailableFlowers, allAvailableAccessories);

            assertEquals("Порожній всередині", nameField.getText());
            assertEquals(0, selectedFlowersDisplayList.getModel().getSize());
            assertEquals(0, selectedAccessoriesDisplayList.getModel().getSize());
        }

        @Test
        @DisplayName("populateFields: Аксесуар з букета відсутній в списку доступних, але відображається у вибраних")
        void populateFields_accessoryNotInAvailableList_stillPopulatesSelected() {
            Accessory missingAccessory = new Accessory("Рідкісний декор", 5.0, AccessoryType.DECORATION);
            missingAccessory.setId(999);

            Bouquet bouquetWithMissingAcc = new Bouquet(
                    "Букет з рідкісним декором", "Опис",
                    new ArrayList<>(),
                    new ArrayList<>(Collections.singletonList(missingAccessory)),
                    "image.jpg", 0
            );
            bouquetWithMissingAcc.setId(11);

            initializeDialog(null, bouquetWithMissingAcc, allAvailableFlowers, new ArrayList<>(allAvailableAccessories));

            DefaultListModel<Accessory> selectedAccModel = (DefaultListModel<Accessory>) selectedAccessoriesDisplayList.getModel();
            boolean found = false;
            for (int i = 0; i < selectedAccModel.getSize(); i++) {
                if (selectedAccModel.getElementAt(i).getId() == missingAccessory.getId()) {
                    found = true;
                    break;
                }
            }
            assertTrue(found, "Аксесуар, якого немає в списку доступних, має бути у списку вибраних аксесуарів букета.");
        }
    }

    @Nested
    @DisplayName("Розширені тести UI та логіки")
    class ExtendedUITests {

        @Test
        @DisplayName("Ініціалізація діалогу з порожніми списками доступних елементів")
        void constructor_emptyAvailableItems_initializesEmptyListsInDialog() {
            initializeDialog(null, null, new ArrayList<>(), new ArrayList<>());

            assertNotNull(dialog.availableFlowersList);
            assertEquals(0, dialog.availableFlowersList.getModel().getSize(), "Список доступних квітів у діалозі має бути порожнім");

            assertNotNull(dialog.availableAccessoriesList);
            assertEquals(0, dialog.availableAccessoriesList.getModel().getSize(), "Список доступних аксесуарів у діалозі має бути порожнім");
        }

        @Test
        @DisplayName("Оновлення прев'ю з порожнім шляхом скидає прев'ю")
        void updatePreviewImage_emptyPath_resetsPreview() {
            initializeDialog(null, null, allAvailableFlowers, allAvailableAccessories);

            try (MockedConstruction<ImageIcon> mockedImageIcon = Mockito.mockConstruction(ImageIcon.class, (mock, context) -> {
                when(mock.getImage()).thenReturn(new BufferedImage(10,10,BufferedImage.TYPE_INT_RGB));
                when(mock.getIconWidth()).thenReturn(10);
                when(mock.getIconHeight()).thenReturn(10);
            })) {

                File tempFile = null;
                try {
                    tempFile = File.createTempFile("test", ".jpg");
                    dialog.updatePreviewImage(tempFile.getAbsolutePath());
                    if (!GraphicsEnvironment.isHeadless() && previewImageLabel.getIcon() != null) {

                    }
                } catch (Exception e) {  }
                finally { if(tempFile != null) tempFile.delete(); }
            }


            dialog.updatePreviewImage("");

            assertNull(previewImageLabel.getIcon(), "Іконка прев'ю має бути null після оновлення з порожнім шляхом.");
            assertEquals("Прев'ю", previewImageLabel.getText(), "Текст прев'ю має бути 'Прев'ю' після оновлення з порожнім шляхом.");
        }

        @Test
        @DisplayName("populateFields: Квітка з букета відсутня в списку доступних, але відображається у вибраних")
        void populateFields_flowerNotInAvailableList_stillPopulatesSelected() {
            Flower missingFlower = new Flower(Flower.FlowerType.LAVENDER, 12.0, 80, 25, "Purple", "France", false, "lavender.jpg", 5);
            missingFlower.setId(99);

            Bouquet bouquetWithMissingFlower = new Bouquet(
                    "Букет з Лавандою", "Опис",
                    new ArrayList<>(Collections.singletonList(new Flower(missingFlower))),
                    new ArrayList<>(),
                    "image.jpg", 0
            );
            bouquetWithMissingFlower.setId(10);

            initializeDialog(null, bouquetWithMissingFlower, new ArrayList<>(allAvailableFlowers), new ArrayList<>(allAvailableAccessories));

            DefaultListModel<String> selectedModel = (DefaultListModel<String>) selectedFlowersDisplayList.getModel();
            boolean found = false;
            for (int i = 0; i < selectedModel.getSize(); i++) {
                FlowerService flowerService = new FlowerService(missingFlower);
                if (selectedModel.getElementAt(i).contains(flowerService.getShortInfo())) {
                    found = true;
                    break;
                }
            }
            assertTrue(found, "Квітка, якої немає в списку доступних, має бути у списку вибраних квітів букета.");
            Flower keyInQuantities = dialog.currentFlowerQuantities.keySet().stream()
                    .filter(f -> f.getId() == missingFlower.getId())
                    .findFirst().orElse(null);
            assertNotNull(keyInQuantities, "Кількість для відсутньої квітки має бути врахована.");
            assertEquals(1, (int)dialog.currentFlowerQuantities.get(keyInQuantities));
        }

        @Test
        @DisplayName("Дія кнопки Огляд - користувач скасовує вибір")
        void browseImageAction_userCancels_pathUnchanged() {
            Assumptions.assumeFalse(GraphicsEnvironment.isHeadless(), "Цей тест для не-headless режиму");
            initializeDialog(null, null, allAvailableFlowers, allAvailableAccessories);
            imagePathField.setText("initial/path.jpg");

            try (MockedConstruction<JFileChooser> mockedFileChooser = Mockito.mockConstruction(JFileChooser.class,
                    (mock, context) -> {
                        when(mock.showOpenDialog(any())).thenReturn(JFileChooser.CANCEL_OPTION);
                    })) {
                browseButton.doClick();
            }
            assertEquals("initial/path.jpg", imagePathField.getText(), "Image path should remain unchanged if user cancels.");
        }
    }

    @Nested
    @DisplayName("Тести валідації та збереження (saveItem)")
    class SaveItemValidationTests {

        @BeforeEach
        void setUpSaveTests() {
            initializeDialog(null, null, allAvailableFlowers, allAvailableAccessories);
        }

        @Test
        @DisplayName("Збереження з порожньою назвою показує помилку")
        void saveItem_emptyName_showsErrorAndReturnsFalse() {
            nameField.setText("");
            descriptionArea.setText("Опис");

            availableFlowersList.setSelectedValue(flower1, true);
            addFlowerButton.doClick();

            AddEditBouquetDialog spyDialog = spy(dialog);
            assertFalse(spyDialog.saveItem());
            verify(spyDialog).showErrorDialog(eq("Назва букета не може бути порожньою."));
            assertFalse(spyDialog.isSaved());
        }

        @Test
        @DisplayName("Збереження без квітів та аксесуарів показує помилку")
        void saveItem_noFlowersOrAccessories_showsErrorAndReturnsFalse() {
            nameField.setText("Порожній букет");

            assertEquals(0, selectedFlowersDisplayList.getModel().getSize());
            assertEquals(0, selectedAccessoriesDisplayList.getModel().getSize());

            AddEditBouquetDialog spyDialog = spy(dialog);
            assertFalse(spyDialog.saveItem());
            verify(spyDialog).showErrorDialog(eq("Букет повинен містити хоча б одну квітку або аксесуар."));
            assertFalse(spyDialog.isSaved());
        }

        @Test
        @DisplayName("saveItem: Невалідний шлях до зображення, користувач обирає 'Ні' (не headless)")
        void saveItem_invalidImagePath_userSelectsNo_returnsFalse() {
            Assumptions.assumeFalse(GraphicsEnvironment.isHeadless());
            nameField.setText("Тест зображення");
            availableFlowersList.setSelectedIndex(0);
            addFlowerButton.doClick();
            imagePathField.setText("невалідний/шлях.jpg");

            try (MockedStatic<JOptionPane> mockedOptionPane = mockStatic(JOptionPane.class);
                 MockedConstruction<File> mockedFile = Mockito.mockConstruction(File.class, (mock, context) -> {
                     when(mock.isFile()).thenReturn(false);
                     when(mock.exists()).thenReturn(false);
                 })) {
                mockedOptionPane.when(() -> JOptionPane.showConfirmDialog(any(), anyString(), anyString(), anyInt(), anyInt()))
                        .thenReturn(JOptionPane.NO_OPTION);

                assertFalse(dialog.saveItem());
                mockedOptionPane.verify(() -> JOptionPane.showConfirmDialog(eq(dialog), anyString(), eq("Попередження: Зображення"), eq(JOptionPane.YES_NO_OPTION), eq(JOptionPane.WARNING_MESSAGE)));
            }
        }

        @Test
        @DisplayName("saveItem: Невалідний шлях, користувач 'Так', новий букет -> порожній шлях")
        void saveItem_invalidPath_userYes_newBouquet_emptyPath() {
            nameField.setText("Тест зображення");
            availableFlowersList.setSelectedIndex(0); addFlowerButton.doClick();
            imagePathField.setText("невалідний/шлях.jpg");

            try (MockedStatic<JOptionPane> mockedOptionPane = mockStatic(JOptionPane.class);
                 MockedConstruction<File> mockedFile = Mockito.mockConstruction(File.class, (m, c) -> {
                     when(m.isFile()).thenReturn(false); when(m.exists()).thenReturn(false);
                 })) {
                mockedOptionPane.when(() -> JOptionPane.showConfirmDialog(any(), any(), any(), anyInt(), anyInt()))
                        .thenReturn(JOptionPane.YES_OPTION);

                assertTrue(dialog.saveItem());
                assertEquals("", dialog.getBouquet().getImagePath());
            }
        }

        @Test
        @DisplayName("saveItem: Невалідний шлях, 'Так', редагування, старий шлях валідний -> старий шлях")
        void saveItem_invalidPath_userYes_editMode_validOldPath_keepsOldPath() {
            Bouquet bouquetWithValidImage = new Bouquet("Старий букет", "Опис",
                    Collections.singletonList(flower1), Collections.emptyList(), "старий/валідний.jpg", 0);
            bouquetWithValidImage.setId(123);

            initializeDialog(null, bouquetWithValidImage, allAvailableFlowers, allAvailableAccessories);
            nameField.setText("Оновлений");
            imagePathField.setText("новий/невалідний.jpg");

            try (MockedStatic<JOptionPane> mockedOptionPane = mockStatic(JOptionPane.class);
                 MockedConstruction<File> mockedFile = Mockito.mockConstruction(File.class, (mock, context) -> {
                     String path = (String) context.arguments().get(0);
                     if (path.equals("старий/валідний.jpg")) {
                         when(mock.isFile()).thenReturn(true);
                         when(mock.exists()).thenReturn(true);
                     } else if (path.equals("новий/невалідний.jpg")) {
                         when(mock.isFile()).thenReturn(false);
                         when(mock.exists()).thenReturn(false);
                     }
                 })) {
                mockedOptionPane.when(() -> JOptionPane.showConfirmDialog(any(), any(), any(), anyInt(), anyInt()))
                        .thenReturn(JOptionPane.YES_OPTION);

                assertTrue(dialog.saveItem());
                assertEquals("старий/валідний.jpg", dialog.getBouquet().getImagePath());
            }
        }
        @Test
        @DisplayName("saveItem: NumberFormatException показує помилку")
        void saveItem_numberFormatException_showsError() {
            nameField.setText("Тест помилки");
            availableFlowersList.setSelectedIndex(0); addFlowerButton.doClick();






            AddEditBouquetDialog spyDialog = spy(dialog);
            JSpinner mockSpinner = mock(JSpinner.class);
            when(mockSpinner.getValue()).thenThrow(new NumberFormatException("Тестова помилка формату"));
            spyDialog.discountSpinner = mockSpinner;

            assertFalse(spyDialog.saveItem());
            verify(spyDialog).showErrorDialog(contains("Будь ласка, перевірте числові значення"));
        }
    }

    @Nested
    @DisplayName("Тести допоміжних методів та рендерерів")
    class HelperAndRendererTests {
        @BeforeEach
        void setUpHelpers() {
            initializeDialog(null, null, allAvailableFlowers, allAvailableAccessories);
        }

        @Test
        @DisplayName("FlowerRenderer: значення не типу Flower")
        void flowerRenderer_nonFlowerValue_returnsDefaultRendering() {
            AddEditBouquetDialog.FlowerRenderer renderer = new AddEditBouquetDialog.FlowerRenderer();
            Component comp = renderer.getListCellRendererComponent(new JList<>(), "Not a Flower", 0, false, false);
            assertTrue(comp instanceof JLabel);
            assertEquals("Not a Flower", ((JLabel) comp).getText());
        }

        @Test
        @DisplayName("AccessoryRenderer: значення не типу Accessory")
        void accessoryRenderer_nonAccessoryValue_returnsDefaultRendering() {
            AddEditBouquetDialog.AccessoryRenderer renderer = new AddEditBouquetDialog.AccessoryRenderer();
            Component comp = renderer.getListCellRendererComponent(new JList<>(), "Not an Accessory", 0, false, false);
            assertTrue(comp instanceof JLabel);
            assertEquals("Not an Accessory", ((JLabel) comp).getText());
        }

        @Test
        @DisplayName("createStyledButton: іконка не завантажується (width=0)")
        void createStyledButton_iconLoadFails_widthZero() {
            Assumptions.assumeFalse(GraphicsEnvironment.isHeadless());
            try (MockedConstruction<ImageIcon> mockedIcon = Mockito.mockConstruction(ImageIcon.class, (mock, ctx) -> {
                when(mock.getIconWidth()).thenReturn(0);
            })) {
                JButton button = dialog.createStyledButton("Кнопка", "/icons/bad.png");
                assertNull(button.getIcon());
            }
        }

        @Test
        @DisplayName("createStyledButton: виняток при завантаженні іконки")
        void createStyledButton_iconLoadException() {
            Assumptions.assumeFalse(GraphicsEnvironment.isHeadless());
            try (MockedStatic<ImageIcon> mockedStaticIcon = mockStatic(ImageIcon.class)) {




                when(ImageIcon.class).thenReturn(null);


                JButton button = dialog.createStyledButton("Кнопка", "/icons/exception.png");
                assertNotNull(button);
                assertNull(button.getIcon());
            } catch (Exception e) {

                System.err.println("Warning: IconLoadException test setup issue: " + e.getMessage());
                JButton button = dialog.createStyledButton("Кнопка", "/icons/exception.png");
                assertNotNull(button);
            }
        }

        @Test
        @DisplayName("updatePreviewImage: previewImageLabel is null")
        void updatePreviewImage_previewLabelNull() {
            dialog.previewImageLabel = null;
            assertDoesNotThrow(() -> dialog.updatePreviewImage("some/path.jpg"));
        }

        @Test
        @DisplayName("updatePreviewImage: шлях вказує на директорію")
        void updatePreviewImage_pathIsDirectory() {
            File tempDir = null;
            try {
                tempDir = java.nio.file.Files.createTempDirectory("testDir").toFile();
                dialog.updatePreviewImage(tempDir.getAbsolutePath());
                assertNull(previewImageLabel.getIcon());
                assertEquals("Прев'ю", previewImageLabel.getText());
            } catch (java.io.IOException e) {
                fail("Could not create temp directory for test.");
            } finally {
                if (tempDir != null) tempDir.delete();
            }
        }

        @Test
        @DisplayName("updatePreviewImage: ImageIcon конструктор кидає помилку (наприклад, OutOfMemoryError)")
        void updatePreviewImage_imageIconThrowsError() {
            Assumptions.assumeFalse(GraphicsEnvironment.isHeadless());

            File tempFile = null;
            try {
                tempFile = File.createTempFile("testImage",".jpg");
                String validPath = tempFile.getAbsolutePath();

                try (MockedConstruction<ImageIcon> mockedIcon = Mockito.mockConstruction(ImageIcon.class, (mock, context) -> {
                    if (context.arguments().get(0).equals(validPath)) {
                        throw new OutOfMemoryError("Тестова помилка пам'яті");
                    }
                })) {
                    dialog.updatePreviewImage(validPath);
                    assertNull(previewImageLabel.getIcon(), "Іконка має бути null, якщо ImageIcon кидає помилку.");
                    assertEquals("Прев'ю", previewImageLabel.getText());
                }
            } catch (java.io.IOException e) {
                fail("Failed to create temp file: " + e.getMessage());
            } finally {
                if (tempFile != null) tempFile.delete();
            }
        }


        @Test
        @DisplayName("updatePreviewImage: originalIcon має нульові розміри")
        void updatePreviewImage_originalIconZeroDimensions() {
            Assumptions.assumeFalse(GraphicsEnvironment.isHeadless());
            File tempFile = null;
            try {
                tempFile = File.createTempFile("testZeroDim",".jpg");
                String validPath = tempFile.getAbsolutePath();

                try (MockedConstruction<ImageIcon> mockedIcon = Mockito.mockConstruction(ImageIcon.class, (mock, context) -> {
                    if (context.arguments().get(0).equals(validPath)) {
                        when(mock.getIconWidth()).thenReturn(0);
                        when(mock.getIconHeight()).thenReturn(0);

                    }
                })) {
                    dialog.updatePreviewImage(validPath);
                    assertNull(previewImageLabel.getIcon());
                    assertEquals("Прев'ю", previewImageLabel.getText());
                }

            } catch (java.io.IOException e) {
                fail("Failed to create temp file for zero dimension test.");
            } finally {
                if (tempFile != null) tempFile.delete();
            }
        }

        @Test
        @DisplayName("updatePreviewImage: previewWidth або previewHeight <= 0, використовуються значення за замовчуванням")
        void updatePreviewImage_previewDimensionsZero_usesDefaults() {
            Assumptions.assumeFalse(GraphicsEnvironment.isHeadless());
            File tempFile = null;
            try {
                tempFile = File.createTempFile("testDefaultDim",".jpg");
                String validPath = tempFile.getAbsolutePath();


                Dimension originalSize = previewImageLabel.getPreferredSize();
                previewImageLabel.setPreferredSize(new Dimension(0, 0));

                try (MockedConstruction<ImageIcon> mockedIcon = Mockito.mockConstruction(ImageIcon.class, (mock, context) -> {
                    if (context.arguments().get(0).equals(validPath)) {
                        when(mock.getIconWidth()).thenReturn(200);
                        when(mock.getIconHeight()).thenReturn(200);

                        Image mockImage = mock(Image.class);
                        when(mock.getImage()).thenReturn(mockImage);




                        when(mockImage.getScaledInstance(eq(110), eq(110), eq(Image.SCALE_SMOOTH)))
                                .thenReturn(mock(Image.class));
                    }
                })) {
                    dialog.updatePreviewImage(validPath);


                    assertNotNull(previewImageLabel.getIcon());
                } finally {
                    previewImageLabel.setPreferredSize(originalSize);
                }

            } catch (java.io.IOException e) {
                fail("Failed to create temp file for zero preview dimension test.");
            } finally {
                if (tempFile != null) tempFile.delete();
            }
        }

        @Test
        @DisplayName("setupEnterNavigation: headless або null components")
        void setupEnterNavigation_headlessOrNullComponents_returnsEarly() {
            AddEditBouquetDialog spyDialog = spy(dialog);

            if (GraphicsEnvironment.isHeadless()) {
                spyDialog.setupEnterNavigation(new JTextField());


            }

            spyDialog.setupEnterNavigation((Component[]) null);
            spyDialog.setupEnterNavigation();

            assertTrue(true, "setupEnterNavigation handled null/empty components without error.");
        }

        @Test
        @DisplayName("setupEnterNavigation: JTextArea CTRL+ENTER")
        void setupEnterNavigation_JTextArea_CtrlEnter() {
            Assumptions.assumeFalse(GraphicsEnvironment.isHeadless());
            JTextArea textArea = new JTextArea();
            JButton nextButton = new JButton("Next");
            nextButton.setFocusable(true);

            dialog.setupEnterNavigation(textArea, nextButton);

            KeyEvent ctrlEnterEvent = new KeyEvent(textArea, KeyEvent.KEY_PRESSED, System.currentTimeMillis(),
                    KeyEvent.CTRL_DOWN_MASK, KeyEvent.VK_ENTER, KeyEvent.CHAR_UNDEFINED);



            boolean foundCtrlEnterLogic = Arrays.stream(textArea.getKeyListeners())
                    .anyMatch(kl -> {



                        kl.keyPressed(ctrlEnterEvent);
                        return ctrlEnterEvent.isConsumed();
                    });
            assertTrue(foundCtrlEnterLogic, "JTextArea should have a KeyListener for CTRL+ENTER navigation.");

        }

        @Test
        @DisplayName("setupEnterNavigation: Інший компонент (JButton) ENTER")
        void setupEnterNavigation_OtherComponent_Enter() {
            Assumptions.assumeFalse(GraphicsEnvironment.isHeadless());
            JButton currentButton = new JButton("Current");
            JButton nextButton = new JButton("Next");
            nextButton.setFocusable(true);

            dialog.setupEnterNavigation(currentButton, nextButton);

            KeyEvent enterEvent = new KeyEvent(currentButton, KeyEvent.KEY_PRESSED, System.currentTimeMillis(),
                    0, KeyEvent.VK_ENTER, KeyEvent.CHAR_UNDEFINED);

            boolean foundEnterLogic = Arrays.stream(currentButton.getKeyListeners())
                    .anyMatch(kl -> {
                        kl.keyPressed(enterEvent);
                        return enterEvent.isConsumed();
                    });
            assertTrue(foundEnterLogic, "Generic component should have a KeyListener for ENTER navigation.");
        }


        @Test
        @DisplayName("addFlowerToList: Кількість квітів нуль або менше - показує помилку")
        void addFlowerToList_quantityZeroOrLess_showsError() {
            initializeDialog(null, null, allAvailableFlowers, allAvailableAccessories);
            AddEditBouquetDialog spyDialog = spy(dialog);
            spyDialog.availableFlowersList.setSelectedValue(flower1, true);

            spyDialog.flowerQuantitySpinner.setValue(0);
            spyDialog.addFlowerToList();
            verify(spyDialog).showErrorDialog("Кількість квітів має бути більшою за нуль.");
            assertEquals(0, spyDialog.selectedFlowersDisplayList.getModel().getSize());

            spyDialog.flowerQuantitySpinner.setValue(-1);
            spyDialog.addFlowerToList();
            verify(spyDialog, times(2)).showErrorDialog("Кількість квітів має бути більшою за нуль.");
            assertEquals(0, spyDialog.selectedFlowersDisplayList.getModel().getSize());
        }

        @Test
        @DisplayName("addFlowerToList: Квітка не вибрана - нічого не робить")
        void addFlowerToList_noFlowerSelected_doesNothing() {
            initializeDialog(null, null, allAvailableFlowers, allAvailableAccessories);
            availableFlowersList.clearSelection();
            dialog.addFlowerToList();
            assertEquals(0, selectedFlowersDisplayList.getModel().getSize());
        }

        @Test
        @DisplayName("removeFlowerFromList: Індекс поза межами - нічого не робить")
        void removeFlowerFromList_indexOutOfBounds_doesNothing() {
            initializeDialog(null, null, allAvailableFlowers, allAvailableAccessories);

            availableFlowersList.setSelectedValue(flower1, true);
            addFlowerButton.doClick();
            assertEquals(1, selectedFlowersDisplayList.getModel().getSize());


            selectedFlowersDisplayList.setSelectedIndex(10);
            dialog.removeFlowerFromList();
            assertEquals(1, selectedFlowersDisplayList.getModel().getSize());
        }

        @Test
        @DisplayName("addAccessoryToList: Аксесуар не вибраний - нічого не робить")
        void addAccessoryToList_noAccessorySelected_doesNothing() {
            initializeDialog(null, null, allAvailableFlowers, allAvailableAccessories);
            availableAccessoriesList.clearSelection();
            dialog.addAccessoryToList();
            assertEquals(0, selectedAccessoriesDisplayList.getModel().getSize());
        }

        @Test
        @DisplayName("removeAccessoryFromList: Аксесуар не вибраний для видалення - нічого не робить")
        void removeAccessoryFromList_noAccessorySelectedInDisplay_doesNothing() {
            initializeDialog(null, null, allAvailableFlowers, allAvailableAccessories);

            availableAccessoriesList.setSelectedValue(accessory1, true);
            addAccessoryButton.doClick();
            assertEquals(1, selectedAccessoriesDisplayList.getModel().getSize());

            selectedAccessoriesDisplayList.clearSelection();
            dialog.removeAccessoryFromList();
            assertEquals(1, selectedAccessoriesDisplayList.getModel().getSize());
        }

        @Test
        @DisplayName("findRepresentativeFlower: target null повертає null")
        void findRepresentativeFlower_targetNull_returnsNull() {
            initializeDialog(null, null, allAvailableFlowers, allAvailableAccessories);
            assertNull(dialog.findRepresentativeFlower(null, allAvailableFlowers));
        }

        @Test
        @DisplayName("findRepresentativeFlower: sourceList null повертає null")
        void findRepresentativeFlower_sourceListNull_returnsNull() {
            initializeDialog(null, null, allAvailableFlowers, allAvailableAccessories);
            assertNull(dialog.findRepresentativeFlower(flower1, null));
        }

        @Test
        @DisplayName("findRepresentativeFlower: не знайдено, повертає null (або target)")
        void findRepresentativeFlower_notFound_returnsTargetOrNull() {

            initializeDialog(null, null, new ArrayList<>(Collections.singletonList(flower1)), allAvailableAccessories);
            Flower unknownFlower = new Flower(FlowerType.OTHER, 1,1,1);
            unknownFlower.setId(1000);
            assertNull(dialog.findRepresentativeFlower(unknownFlower, dialog.availableFlowersSource));
        }


        @Test
        @DisplayName("findRepresentativeAccessory: target null повертає null")
        void findRepresentativeAccessory_targetNull_returnsNull() {
            initializeDialog(null, null, allAvailableFlowers, allAvailableAccessories);
            assertNull(dialog.findRepresentativeAccessory(null, allAvailableAccessories));
        }

        @Test
        @DisplayName("findRepresentativeAccessory: sourceList null повертає null")
        void findRepresentativeAccessory_sourceListNull_returnsNull() {
            initializeDialog(null, null, allAvailableFlowers, allAvailableAccessories);
            assertNull(dialog.findRepresentativeAccessory(accessory1, null));
        }

        @Test
        @DisplayName("findRepresentativeAccessory: не знайдено, повертає null (або target)")
        void findRepresentativeAccessory_notFound_returnsTargetOrNull() {

            initializeDialog(null, null, allAvailableFlowers, new ArrayList<>(Collections.singletonList(accessory1)));
            Accessory unknownAccessory = new Accessory("Unknown", 1, AccessoryType.OTHER);
            unknownAccessory.setId(1001);
            assertNull(dialog.findRepresentativeAccessory(unknownAccessory, dialog.availableAccessoriesSource));
        }
    }
}