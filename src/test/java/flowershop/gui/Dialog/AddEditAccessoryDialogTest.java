package flowershop.gui.Dialog;

import flowershop.models.Accessory;
import flowershop.models.Accessory.AccessoryType;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.argThat;

@ExtendWith(MockitoExtension.class)
class AddEditAccessoryDialogTest {

    private AddEditAccessoryDialog dialog;
    private Accessory existingAccessory;

    // UI Components to be accessed in tests
    private JTextField nameField;
    private JComboBox<AccessoryType> typeCombo;
    private JTextField priceField;
    private JTextField colorField;
    private JTextField sizeField;
    private JTextField stockQuantityField;
    private JTextArea descriptionArea;
    private JTextField imagePathField; // From AbstractAddEditDialog
    private JButton okButton;           // From AbstractAddEditDialog
    private JButton browseButton;       // From AbstractAddEditDialog
    private JLabel previewImageLabel;   // From AbstractAddEditDialog

    @BeforeAll
    static void setupHeadlessMode() {
        try {
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            System.out.println("Test Environment: Is headless by GraphicsEnvironment.isHeadless()? " + ge.isHeadless());
            System.out.println("Test Environment: Is headless by System property? " + System.getProperty("java.awt.headless"));
        } catch (Error e) {
            System.err.println("Error getting GraphicsEnvironment in @BeforeAll: " + e.getMessage());
        }
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception e) {
            System.err.println("WARNING: Could not set LookAndFeel for tests: " + e.getMessage());
        }
    }

    @BeforeEach
    void setUp() {
        existingAccessory = new Accessory(
                "Стрічка атласна", 15.99, "Червона атласна стрічка для декору",
                "images/ribbon.jpg", 100, AccessoryType.RIBBON, "Червоний", "2 см x 5 м"
        );
        existingAccessory.setId(1); // Assuming Accessory has setId or you set it via constructor/reflection for tests
    }

    private void initializeDialog(Accessory accessoryToEdit) {
        dialog = new AddEditAccessoryDialog(null, accessoryToEdit);

        // Access fields directly from the dialog instance
        // These fields are private in AddEditAccessoryDialog, so you'd need to make them package-private
        // or use reflection if you can't change their visibility.
        // For now, I'll assume they are accessible for testing purposes (e.g., package-private).
        nameField = dialog.nameField;
        typeCombo = dialog.typeCombo;
        priceField = dialog.priceField;
        colorField = dialog.colorField;
        sizeField = dialog.sizeField;
        stockQuantityField = dialog.stockQuantityField;
        descriptionArea = dialog.descriptionArea;

        // Fields from AbstractAddEditDialog
        imagePathField = dialog.imagePathField;
        okButton = dialog.okButton;
        browseButton = dialog.browseButton;
        previewImageLabel = dialog.previewImageLabel;
    }

    @Nested
    @DisplayName("Тести режиму додавання аксесуара")
    class AddModeTests {
        @BeforeEach
        void setUpAddMode() {
            initializeDialog(null);
        }

        @Test
        @DisplayName("Ініціалізація діалогу в режимі додавання аксесуара")
        void constructor_addMode_initializesFieldsCorrectly() {
            assertEquals("Додати аксесуар", dialog.getTitle());
            assertNull(dialog.getItem());
            assertEquals("", nameField.getText());
            assertNotNull(typeCombo.getSelectedItem()); // Default type might be selected
            assertEquals("", priceField.getText());
            assertEquals("", colorField.getText());
            assertEquals("", sizeField.getText());
            assertEquals("", stockQuantityField.getText());
            assertEquals("", descriptionArea.getText());
            assertEquals("", imagePathField.getText());
            if (previewImageLabel != null) {
                assertEquals("Немає зображення", previewImageLabel.getText());
            }
        }

        @Test
        @DisplayName("Успішне збереження нового аксесуара")
        void saveItem_addMode_validData_createsNewAccessory() {
            nameField.setText("Декоративна коробка");
            typeCombo.setSelectedItem(AccessoryType.BOX);
            priceField.setText("50.00");
            colorField.setText("Крафтовий");
            sizeField.setText("20x20x10 см");
            stockQuantityField.setText("30");
            descriptionArea.setText("Елегантна коробка для подарунків");
            imagePathField.setText("images/box.jpg");

            try (MockedConstruction<File> mockedFile = Mockito.mockConstruction(File.class,
                    (mock, context) -> {
                        when(mock.isFile()).thenReturn(true);
                        when(mock.exists()).thenReturn(true);
                    })) {
                assertTrue(dialog.saveItem());
            }

            Accessory savedAccessory = dialog.getAccessory();
            assertNotNull(savedAccessory);
            assertEquals("Декоративна коробка", savedAccessory.getName());
            assertEquals(AccessoryType.BOX, savedAccessory.getType());
            assertEquals(50.00, savedAccessory.getPrice());
            assertEquals("Крафтовий", savedAccessory.getColor());
            assertEquals("20x20x10 см", savedAccessory.getSize());
            assertEquals(30, savedAccessory.getStockQuantity());
            assertEquals("Елегантна коробка для подарунків", savedAccessory.getDescription());
            assertEquals("images/box.jpg", savedAccessory.getImagePath());
        }
    }

    @Nested
    @DisplayName("Тести режиму редагування аксесуара")
    class EditModeTests {
        @BeforeEach
        void setUpEditMode() {
            initializeDialog(existingAccessory);
        }

        @Test
        @DisplayName("Ініціалізація діалогу в режимі редагування аксесуара")
        void constructor_editMode_populatesFieldsCorrectly() {
            assertEquals("Редагувати аксесуар", dialog.getTitle());
            assertNotNull(dialog.getItem());
            assertEquals(existingAccessory.getName(), nameField.getText());
            assertEquals(existingAccessory.getType(), typeCombo.getSelectedItem());
            assertEquals(String.format("%.2f", existingAccessory.getPrice()).replace(',', '.'), priceField.getText());
            assertEquals(existingAccessory.getColor(), colorField.getText());
            assertEquals(existingAccessory.getSize(), sizeField.getText());
            assertEquals(String.valueOf(existingAccessory.getStockQuantity()), stockQuantityField.getText());
            assertEquals(existingAccessory.getDescription(), descriptionArea.getText());
            assertEquals(existingAccessory.getImagePath(), imagePathField.getText());
        }

        @Test
        @DisplayName("Успішне збереження зміненого аксесуара")
        void saveItem_editMode_validData_updatesExistingAccessory() {
            nameField.setText("Оновлена Стрічка");
            typeCombo.setSelectedItem(AccessoryType.RIBBON);
            priceField.setText("20.50");
            colorField.setText("Синій");
            sizeField.setText("3 см x 10 м");
            stockQuantityField.setText("150");
            descriptionArea.setText("Оновлений опис стрічки");
            imagePathField.setText("images/new_ribbon.jpg");

            try (MockedConstruction<File> mockedFile = Mockito.mockConstruction(File.class,
                    (mock, context) -> {
                        String pathArg = (String) context.arguments().get(0);
                        if ("images/new_ribbon.jpg".equals(pathArg) || existingAccessory.getImagePath().equals(pathArg)) {
                            when(mock.isFile()).thenReturn(true);
                            when(mock.exists()).thenReturn(true);
                        } else {
                            when(mock.isFile()).thenReturn(false);
                            when(mock.exists()).thenReturn(false);
                        }
                    })) {
                assertTrue(dialog.saveItem());
            }

            Accessory savedAccessory = dialog.getAccessory();
            assertNotNull(savedAccessory);
            assertEquals(existingAccessory.getId(), savedAccessory.getId());
            assertEquals("Оновлена Стрічка", savedAccessory.getName());
            assertEquals(AccessoryType.RIBBON, savedAccessory.getType());
            assertEquals(20.50, savedAccessory.getPrice());
            assertEquals("Синій", savedAccessory.getColor());
            assertEquals("3 см x 10 м", savedAccessory.getSize());
            assertEquals(150, savedAccessory.getStockQuantity());
            assertEquals("Оновлений опис стрічки", savedAccessory.getDescription());
            assertEquals("images/new_ribbon.jpg", savedAccessory.getImagePath());
        }
    }

    @Nested
    @DisplayName("Тести валідації полів аксесуара")
    class ValidationTests {
        @BeforeEach
        void setUpValidation() {
            initializeDialog(null);
        }

        @Test
        @DisplayName("Збереження з порожньою назвою - помилка")
        void saveItem_emptyName_showsError() {
            try (MockedStatic<JOptionPane> mockedOptionPane = Mockito.mockStatic(JOptionPane.class)) {
                nameField.setText("");
                typeCombo.setSelectedItem(AccessoryType.VASE);
                priceField.setText("100");
                colorField.setText("Прозорий");
                sizeField.setText("Висока");
                stockQuantityField.setText("5");
                descriptionArea.setText("Ваза для квітів");

                assertFalse(dialog.saveItem());
                mockedOptionPane.verify(() -> JOptionPane.showMessageDialog(
                        eq(dialog),
                        eq("Назва аксесуара не може бути порожньою."),
                        eq("Помилка введення"),
                        eq(JOptionPane.ERROR_MESSAGE)
                ));
            }
        }
        @Test
        @DisplayName("Збереження без вибраного типу - помилка")
        void saveItem_nullType_showsError() {
            try (MockedStatic<JOptionPane> mockedOptionPane = Mockito.mockStatic(JOptionPane.class)) {
                nameField.setText("Стрічка");
                typeCombo.setSelectedItem(null); // Жоден тип не вибрано
                priceField.setText("10.0");
                colorField.setText("Червоний");
                sizeField.setText("1м");
                stockQuantityField.setText("10");
                descriptionArea.setText("Опис");


                assertFalse(dialog.saveItem());
                mockedOptionPane.verify(() -> JOptionPane.showMessageDialog(
                        eq(dialog),
                        eq("Необхідно вибрати тип аксесуара."),
                        eq("Помилка введення"),
                        eq(JOptionPane.ERROR_MESSAGE)
                ));
            }
        }


        @Test
        @DisplayName("Збереження з порожньою ціною - помилка")
        void saveItem_emptyPrice_showsError() {
            try (MockedStatic<JOptionPane> mockedOptionPane = Mockito.mockStatic(JOptionPane.class)) {
                nameField.setText("Стрічка");
                typeCombo.setSelectedItem(AccessoryType.RIBBON);
                priceField.setText("");
                colorField.setText("Червоний");
                sizeField.setText("1м");
                stockQuantityField.setText("10");
                descriptionArea.setText("Опис");

                assertFalse(dialog.saveItem());
                mockedOptionPane.verify(() -> JOptionPane.showMessageDialog(
                        eq(dialog),
                        eq("Ціна не може бути порожньою."),
                        eq("Помилка введення"),
                        eq(JOptionPane.ERROR_MESSAGE)
                ));
            }
        }

        @Test
        @DisplayName("Збереження з ціною <= 0 - помилка")
        void saveItem_invalidPrice_showsError() {
            try (MockedStatic<JOptionPane> mockedOptionPane = Mockito.mockStatic(JOptionPane.class)) {
                nameField.setText("Стрічка");
                typeCombo.setSelectedItem(AccessoryType.RIBBON);
                priceField.setText("0");
                colorField.setText("Червоний");
                sizeField.setText("1м");
                stockQuantityField.setText("10");
                descriptionArea.setText("Опис");


                assertFalse(dialog.saveItem());
                mockedOptionPane.verify(() -> JOptionPane.showMessageDialog(
                        eq(dialog),
                        eq("Ціна повинна бути більшою за нуль."),
                        eq("Помилка введення"),
                        eq(JOptionPane.ERROR_MESSAGE)
                ));
            }
        }


        @Test
        @DisplayName("Збереження з порожнім кольором - помилка")
        void saveItem_emptyColor_showsError() {
            try (MockedStatic<JOptionPane> mockedOptionPane = Mockito.mockStatic(JOptionPane.class)) {
                nameField.setText("Стрічка");
                typeCombo.setSelectedItem(AccessoryType.RIBBON);
                priceField.setText("10.0");
                colorField.setText("");
                sizeField.setText("1м");
                stockQuantityField.setText("10");
                descriptionArea.setText("Опис");

                assertFalse(dialog.saveItem());
                mockedOptionPane.verify(() -> JOptionPane.showMessageDialog(
                        eq(dialog),
                        eq("Колір аксесуара не може бути порожнім."),
                        eq("Помилка введення"),
                        eq(JOptionPane.ERROR_MESSAGE)
                ));
            }
        }

        @Test
        @DisplayName("Збереження з порожнім розміром - помилка")
        void saveItem_emptySize_showsError() {
            try (MockedStatic<JOptionPane> mockedOptionPane = Mockito.mockStatic(JOptionPane.class)) {
                nameField.setText("Стрічка");
                typeCombo.setSelectedItem(AccessoryType.RIBBON);
                priceField.setText("10.0");
                colorField.setText("Червоний");
                sizeField.setText("");
                stockQuantityField.setText("10");
                descriptionArea.setText("Опис");

                assertFalse(dialog.saveItem());
                mockedOptionPane.verify(() -> JOptionPane.showMessageDialog(
                        eq(dialog),
                        eq("Розмір аксесуара не може бути порожнім."),
                        eq("Помилка введення"),
                        eq(JOptionPane.ERROR_MESSAGE)
                ));
            }
        }


        @Test
        @DisplayName("Збереження з порожньою кількістю на складі - помилка")
        void saveItem_emptyStock_showsError() {
            try (MockedStatic<JOptionPane> mockedOptionPane = Mockito.mockStatic(JOptionPane.class)) {
                nameField.setText("Стрічка");
                typeCombo.setSelectedItem(AccessoryType.RIBBON);
                priceField.setText("10.0");
                colorField.setText("Червоний");
                sizeField.setText("1м");
                stockQuantityField.setText("");
                descriptionArea.setText("Опис");

                assertFalse(dialog.saveItem());
                mockedOptionPane.verify(() -> JOptionPane.showMessageDialog(
                        eq(dialog),
                        eq("Кількість на складі не може бути порожньою."),
                        eq("Помилка введення"),
                        eq(JOptionPane.ERROR_MESSAGE)
                ));
            }
        }


        @Test
        @DisplayName("Збереження з кількістю на складі < 0 - помилка")
        void saveItem_negativeStock_showsError() {
            try (MockedStatic<JOptionPane> mockedOptionPane = Mockito.mockStatic(JOptionPane.class)) {
                nameField.setText("Стрічка");
                typeCombo.setSelectedItem(AccessoryType.RIBBON);
                priceField.setText("10.0");
                colorField.setText("Червоний");
                sizeField.setText("1м");
                stockQuantityField.setText("-5");
                descriptionArea.setText("Опис");


                assertFalse(dialog.saveItem());
                mockedOptionPane.verify(() -> JOptionPane.showMessageDialog(
                        eq(dialog),
                        eq("Кількість на складі не може бути від'ємною."),
                        eq("Помилка введення"),
                        eq(JOptionPane.ERROR_MESSAGE)
                ));
            }
        }

        @Test
        @DisplayName("Збереження з неіснуючим файлом зображення - попередження (YES)")
        void saveItem_nonExistentImageFile_showsWarningAndSaves_ifYes() {
            try (MockedStatic<JOptionPane> mockedOptionPane = Mockito.mockStatic(JOptionPane.class);
                 MockedConstruction<File> mockedFile = Mockito.mockConstruction(File.class,
                         (mock, context) -> {
                             when(mock.isFile()).thenReturn(false); // File is not a file or does not exist
                             when(mock.exists()).thenReturn(false);
                         })) {

                mockedOptionPane.when(() -> JOptionPane.showConfirmDialog(
                                eq(dialog),
                                argThat(message -> message instanceof String && ((String) message).startsWith("Файл зображення за вказаним шляхом не знайдено")),
                                eq("Попередження: Зображення"),
                                eq(JOptionPane.YES_NO_OPTION),
                                eq(JOptionPane.WARNING_MESSAGE)))
                        .thenReturn(JOptionPane.YES_OPTION);

                nameField.setText("Ваза");
                typeCombo.setSelectedItem(AccessoryType.VASE);
                priceField.setText("120.00");
                colorField.setText("Прозорий");
                sizeField.setText("Середня");
                stockQuantityField.setText("15");
                descriptionArea.setText("Скляна ваза");
                imagePathField.setText("non_existent_vase.jpg");

                assertTrue(dialog.saveItem());
                Accessory savedAccessory = dialog.getAccessory();
                assertNotNull(savedAccessory);
                assertEquals("non_existent_vase.jpg", savedAccessory.getImagePath());
            }
        }

        @Test
        @DisplayName("Збереження з неіснуючим файлом зображення - попередження (NO)")
        void saveItem_nonExistentImageFile_showsWarningAndDoesNotSave_ifNo() {
            try (MockedStatic<JOptionPane> mockedOptionPane = Mockito.mockStatic(JOptionPane.class);
                 MockedConstruction<File> mockedFile = Mockito.mockConstruction(File.class,
                         (mock, context) -> {
                             when(mock.isFile()).thenReturn(false);
                             when(mock.exists()).thenReturn(false);
                         })) {

                mockedOptionPane.when(() -> JOptionPane.showConfirmDialog(
                                eq(dialog),
                                anyString(), // Keep it simple for the message
                                eq("Попередження: Зображення"),
                                eq(JOptionPane.YES_NO_OPTION),
                                eq(JOptionPane.WARNING_MESSAGE)))
                        .thenReturn(JOptionPane.NO_OPTION);
                nameField.setText("Ваза");
                typeCombo.setSelectedItem(AccessoryType.VASE);
                priceField.setText("120.00");
                colorField.setText("Прозорий");
                sizeField.setText("Середня");
                stockQuantityField.setText("15");
                descriptionArea.setText("Скляна ваза");
                imagePathField.setText("non_existent_vase.jpg");

                assertFalse(dialog.saveItem());
                mockedOptionPane.verify(() -> JOptionPane.showConfirmDialog(
                        eq(dialog),
                        argThat(message -> message instanceof String && ((String) message).startsWith("Файл зображення за вказаним шляхом не знайдено")),
                        eq("Попередження: Зображення"),
                        eq(JOptionPane.YES_NO_OPTION),
                        eq(JOptionPane.WARNING_MESSAGE)
                ));
            }
        }
        @Test
        @DisplayName("Збереження з невірним числовим форматом - помилка")
        void saveItem_numberFormatException_showsErrorAndReturnsFalse() {
            try (MockedStatic<JOptionPane> mockedOptionPane = Mockito.mockStatic(JOptionPane.class)) {
                nameField.setText("Стрічка");
                typeCombo.setSelectedItem(AccessoryType.RIBBON);
                priceField.setText("not_a_number");
                colorField.setText("Червоний");
                sizeField.setText("1м");
                stockQuantityField.setText("10"); // Keep one numeric field valid to isolate
                descriptionArea.setText("Опис");


                assertFalse(dialog.saveItem());
                mockedOptionPane.verify(() -> JOptionPane.showMessageDialog(
                        eq(dialog),
                        eq("Будь ласка, введіть коректні числові значення для ціни та кількості."),
                        eq("Помилка введення"),
                        eq(JOptionPane.ERROR_MESSAGE)
                ));
            }
        }

    }

    @Nested
    @DisplayName("Тести взаємодії з UI аксесуара")
    class InteractionTests {
        @BeforeEach
        void setUpInteractions() {
            initializeDialog(null);
        }

        @Test
        @DisplayName("Натискання кнопки 'Огляд...' відкриває JFileChooser для аксесуара")
        void browseButton_actionPerformed_opensFileChooser() {
            try (MockedConstruction<JFileChooser> mockedFileChooser = Mockito.mockConstruction(JFileChooser.class,
                    (mock, context) -> {
                        when(mock.showOpenDialog(dialog)).thenReturn(JFileChooser.APPROVE_OPTION);
                        File selectedFile = mock(File.class);
                        when(selectedFile.getAbsolutePath()).thenReturn("mocked/accessory_image.jpg");
                        when(mock.getSelectedFile()).thenReturn(selectedFile);
                    })) {
                browseButton.doClick();
                assertEquals(1, mockedFileChooser.constructed().size());
                JFileChooser chooser = mockedFileChooser.constructed().get(0);
                verify(chooser).setDialogTitle("Виберіть зображення");
                verify(chooser).showOpenDialog(dialog);
                assertEquals("mocked/accessory_image.jpg", imagePathField.getText());
            }
        }

        @Test
        @DisplayName("Натискання 'Cancel' закриває діалог аксесуара без збереження")
        void cancelButton_closesWithoutSaving() {
            AddEditAccessoryDialog spyDialog = spy(dialog);
            JButton cancelButton = spyDialog.cancelButton;
            assertNotNull(cancelButton);
            cancelButton.doClick();
            assertFalse(spyDialog.isSaved());
        }
    }
}