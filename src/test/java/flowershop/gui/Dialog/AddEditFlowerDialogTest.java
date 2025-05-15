package flowershop.gui.Dialog;

import flowershop.models.Flower;
import flowershop.models.Flower.FlowerType;
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
class AddEditFlowerDialogTest {

    private AddEditFlowerDialog dialog;
    private Flower existingFlower;


    private JComboBox<FlowerType> typeCombo;
    private JTextField priceField;
    private JSlider freshnessSlider;
    private JTextField stemLengthField;
    private JTextField colorField;
    private JTextField countryField;
    private JCheckBox pottedCheckBox;
    private JTextField stockQuantityField;
    private JTextField imagePathField;
    private JButton okButton;
    private JButton browseButton;
    private JLabel previewImageLabel;
    private JLabel freshnessValueLabel;


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

        existingFlower = new Flower(
                FlowerType.ROSE, 25.99, 90, 60,
                "Червоний", "Нідерланди", false,
                "images/rose.jpg", 50
        );
        existingFlower.setId(1);
    }


    private void initializeDialog(Flower flowerToEdit) {

        dialog = new AddEditFlowerDialog(null, flowerToEdit);


        typeCombo = dialog.typeCombo;
        priceField = dialog.priceField;
        freshnessSlider = dialog.freshnessSlider;
        freshnessValueLabel = dialog.freshnessValueLabel;
        stemLengthField = dialog.stemLengthField;
        colorField = dialog.colorField;
        countryField = dialog.countryField;
        pottedCheckBox = dialog.pottedCheckBox;
        stockQuantityField = dialog.stockQuantityField;
        imagePathField = dialog.imagePathField;
        okButton = dialog.okButton;
        browseButton = dialog.browseButton;
        previewImageLabel = dialog.previewImageLabel;
    }

    @Nested
    @DisplayName("Тести режиму додавання")
    class AddModeTests {
        @BeforeEach
        void setUpAddMode() {
            initializeDialog(null);
        }

        @Test
        @DisplayName("Ініціалізація діалогу в режимі додавання")
        void constructor_addMode_initializesFieldsCorrectly() {
            assertEquals("Додати квітку", dialog.getTitle());
            assertNull(dialog.getItem(), "Item should be null in add mode");
            assertNotNull(typeCombo, "Type combo box should be initialized");
            assertEquals("", priceField.getText(), "Price field should be empty");
            assertEquals(75, freshnessSlider.getValue(), "Freshness slider should be at default value");
            assertEquals("", stemLengthField.getText(), "Stem length field should be empty");
            assertEquals("", colorField.getText(), "Color field should be empty");
            assertEquals("", countryField.getText(), "Country field should be empty");
            assertFalse(pottedCheckBox.isSelected(), "Potted checkbox should be deselected");
            assertEquals("", stockQuantityField.getText(), "Stock quantity field should be empty");
            assertEquals("", imagePathField.getText(), "Image path field should be empty");
            if (previewImageLabel != null) {
                assertEquals("Немає зображення", previewImageLabel.getText(), "Preview label should show default text");
            }
        }

        @Test
        @DisplayName("Успішне збереження нової квітки")
        void saveItem_addMode_validData_createsNewFlower() {
            typeCombo.setSelectedItem(FlowerType.TULIP);
            priceField.setText("15.50");
            freshnessSlider.setValue(80);
            stemLengthField.setText("40");
            colorField.setText("Жовтий");
            countryField.setText("Україна");
            pottedCheckBox.setSelected(false);
            stockQuantityField.setText("100");
            imagePathField.setText("images/tulip.jpg");


            try (MockedConstruction<File> mockedFileConstruction = Mockito.mockConstruction(File.class,
                    (mock, context) -> {

                        if (context.arguments().size() > 0 && !((String) context.arguments().get(0)).isEmpty()) {
                            when(mock.isFile()).thenReturn(true);
                            when(mock.exists()).thenReturn(true);
                        } else {
                            when(mock.isFile()).thenReturn(false);
                            when(mock.exists()).thenReturn(false);
                        }
                    })) {
                assertTrue(dialog.saveItem(), "Dialog saveItem() should return true with valid data");
            }

            Flower savedFlower = dialog.getFlower();
            assertNotNull(savedFlower, "Saved flower should not be null");
            assertEquals(FlowerType.TULIP, savedFlower.getType());
            assertEquals(15.50, savedFlower.getPrice());
            assertEquals(80, savedFlower.getFreshness());
            assertEquals(40, savedFlower.getStemLength());
            assertEquals("Жовтий", savedFlower.getColor());
            assertEquals("Україна", savedFlower.getCountryOfOrigin());
            assertFalse(savedFlower.isPotted());
            assertEquals(100, savedFlower.getStockQuantity());
            assertEquals("images/tulip.jpg", savedFlower.getImagePath());
        }
    }

    @Nested
    @DisplayName("Тести режиму редагування")
    class EditModeTests {
        @BeforeEach
        void setUpEditMode() {
            initializeDialog(existingFlower);
        }

        @Test
        @DisplayName("Ініціалізація діалогу в режимі редагування")
        void constructor_editMode_populatesFieldsCorrectly() {
            assertEquals("Редагувати квітку", dialog.getTitle());
            assertNotNull(dialog.getItem(), "Item should not be null in edit mode");
            assertEquals(existingFlower.getType(), typeCombo.getSelectedItem());
            assertEquals(String.format("%.2f", existingFlower.getPrice()).replace(',', '.'), priceField.getText());
            assertEquals(existingFlower.getFreshness(), freshnessSlider.getValue());
            assertEquals(String.valueOf(existingFlower.getStemLength()), stemLengthField.getText());
            assertEquals(existingFlower.getColor(), colorField.getText());
            assertEquals(existingFlower.getCountryOfOrigin(), countryField.getText());
            assertEquals(existingFlower.isPotted(), pottedCheckBox.isSelected());
            assertEquals(String.valueOf(existingFlower.getStockQuantity()), stockQuantityField.getText());
            assertEquals(existingFlower.getImagePath(), imagePathField.getText());
        }

        @Test
        @DisplayName("Успішне збереження зміненої квітки")
        void saveItem_editMode_validData_updatesExistingFlower() {
            typeCombo.setSelectedItem(FlowerType.LILY);
            priceField.setText("30.00");
            freshnessSlider.setValue(85);
            stemLengthField.setText("50");
            colorField.setText("Білий");
            countryField.setText("Польща");
            pottedCheckBox.setSelected(true);
            stockQuantityField.setText("75");
            imagePathField.setText("images/lily.jpg");

            try (MockedConstruction<File> mockedFileConstruction = Mockito.mockConstruction(File.class,
                    (mock, context) -> {
                        String filePath = (String) context.arguments().get(0);

                        boolean isNewPath = "images/lily.jpg".equals(filePath);
                        boolean isOldPath = existingFlower != null && existingFlower.getImagePath() != null && existingFlower.getImagePath().equals(filePath);
                        if (isNewPath || isOldPath) {
                            when(mock.isFile()).thenReturn(true);
                            when(mock.exists()).thenReturn(true);
                        } else {
                            when(mock.isFile()).thenReturn(false);
                            when(mock.exists()).thenReturn(false);
                        }
                    })) {
                assertTrue(dialog.saveItem(), "Dialog saveItem() should return true for valid update");
            }

            Flower savedFlower = dialog.getFlower();
            assertNotNull(savedFlower, "Saved flower should not be null after update");
            assertEquals(existingFlower.getId(), savedFlower.getId(), "ID should remain the same after update");
            assertEquals(FlowerType.LILY, savedFlower.getType());
            assertEquals(30.00, savedFlower.getPrice());
            assertEquals(85, savedFlower.getFreshness());
            assertEquals(50, savedFlower.getStemLength());
            assertEquals("Білий", savedFlower.getColor());
            assertEquals("Польща", savedFlower.getCountryOfOrigin());
            assertTrue(savedFlower.isPotted());
            assertEquals(75, savedFlower.getStockQuantity());
            assertEquals("images/lily.jpg", savedFlower.getImagePath());
        }
    }

    @Nested
    @DisplayName("Тести валідації полів")
    class ValidationTests {
        @BeforeEach
        void setUpValidation() {
            initializeDialog(null);
        }

        @Test
        @DisplayName("Збереження з порожньою ціною - помилка")
        void saveItem_emptyPrice_showsErrorAndReturnsFalse() {
            try (MockedStatic<JOptionPane> mockedOptionPane = Mockito.mockStatic(JOptionPane.class)) {
                typeCombo.setSelectedItem(FlowerType.ROSE);
                priceField.setText("");
                stemLengthField.setText("50");
                colorField.setText("Red");
                countryField.setText("NL");
                stockQuantityField.setText("10");
                imagePathField.setText("images/any.jpg");

                assertFalse(dialog.saveItem(), "saveItem should return false for empty price");

                mockedOptionPane.verify(() -> JOptionPane.showMessageDialog(
                        eq(dialog),
                        eq("Ціна не може бути порожньою."),
                        eq("Помилка введення"),
                        eq(JOptionPane.ERROR_MESSAGE)
                ));
            }
        }

        @Test
        @DisplayName("Збереження з неіснуючим файлом зображення - попередження (YES)")
        void saveItem_nonExistentImageFile_showsWarningAndSaves_ifYes() {
            try (MockedStatic<JOptionPane> mockedOptionPane = Mockito.mockStatic(JOptionPane.class);
                 MockedConstruction<File> mockedFileConstruction = Mockito.mockConstruction(File.class,
                         (mock, context) -> {

                             if (context.arguments().size() > 0 && !((String)context.arguments().get(0)).isEmpty()){
                                 when(mock.isFile()).thenReturn(false);
                                 when(mock.exists()).thenReturn(false);
                             } else {
                                 when(mock.isFile()).thenReturn(false);
                                 when(mock.exists()).thenReturn(false);
                             }
                         })) {


                mockedOptionPane.when(() -> JOptionPane.showConfirmDialog(
                                eq(dialog),
                                argThat(message -> message instanceof String && ((String) message).startsWith("Файл зображення за вказаним шляхом не знайдено")),
                                eq("Попередження: Зображення"),
                                eq(JOptionPane.YES_NO_OPTION),
                                eq(JOptionPane.WARNING_MESSAGE)
                        ))
                        .thenReturn(JOptionPane.YES_OPTION);


                typeCombo.setSelectedItem(FlowerType.CHRYSANTHEMUM);
                priceField.setText("12.00");
                stemLengthField.setText("55");
                colorField.setText("Білий");
                countryField.setText("Японія");
                stockQuantityField.setText("30");
                imagePathField.setText("non_existent_path.jpg");

                assertTrue(dialog.saveItem(), "saveItem should return true when user clicks YES on warning");
                Flower savedFlower = dialog.getFlower();
                assertNotNull(savedFlower, "Flower should be saved");
                assertEquals("non_existent_path.jpg", savedFlower.getImagePath(), "Image path should be the non-existent one");
            }
        }


        @Test
        @DisplayName("Збереження з ціною <= 0 - помилка")
        void saveItem_invalidPrice_showsErrorAndReturnsFalse() {
            try (MockedStatic<JOptionPane> mockedOptionPane = Mockito.mockStatic(JOptionPane.class)) {
                typeCombo.setSelectedItem(FlowerType.ROSE);
                priceField.setText("0");
                stemLengthField.setText("50");
                colorField.setText("Red");
                countryField.setText("NL");
                stockQuantityField.setText("10");

                assertFalse(dialog.saveItem());
                mockedOptionPane.verify(() -> JOptionPane.showMessageDialog(eq(dialog), eq("Ціна повинна бути більшою за нуль."), eq("Помилка введення"), eq(JOptionPane.ERROR_MESSAGE)));
            }
        }

        @Test
        @DisplayName("Збереження з порожньою довжиною стебла - помилка")
        void saveItem_emptyStemLength_showsErrorAndReturnsFalse() {
            try (MockedStatic<JOptionPane> mockedOptionPane = Mockito.mockStatic(JOptionPane.class)) {
                typeCombo.setSelectedItem(FlowerType.ROSE);
                priceField.setText("10.0");
                stemLengthField.setText("");
                colorField.setText("Red");
                countryField.setText("NL");
                stockQuantityField.setText("10");

                assertFalse(dialog.saveItem());
                mockedOptionPane.verify(() -> JOptionPane.showMessageDialog(eq(dialog), eq("Довжина стебла не може бути порожньою."), eq("Помилка введення"), eq(JOptionPane.ERROR_MESSAGE)));
            }
        }


        @Test
        @DisplayName("Збереження з довжиною стебла <= 0 - помилка")
        void saveItem_invalidStemLength_showsErrorAndReturnsFalse() {
            try (MockedStatic<JOptionPane> mockedOptionPane = Mockito.mockStatic(JOptionPane.class)) {
                typeCombo.setSelectedItem(FlowerType.ROSE);
                priceField.setText("10.0");
                stemLengthField.setText("-5");
                colorField.setText("Red");
                countryField.setText("NL");
                stockQuantityField.setText("10");

                assertFalse(dialog.saveItem());
                mockedOptionPane.verify(() -> JOptionPane.showMessageDialog(eq(dialog), eq("Довжина стебла повинна бути більшою за нуль."), eq("Помилка введення"), eq(JOptionPane.ERROR_MESSAGE)));
            }
        }

        @Test
        @DisplayName("Збереження з порожнім кольором - помилка")
        void saveItem_emptyColor_showsErrorAndReturnsFalse() {
            try (MockedStatic<JOptionPane> mockedOptionPane = Mockito.mockStatic(JOptionPane.class)) {
                typeCombo.setSelectedItem(FlowerType.ROSE);
                priceField.setText("10.0");
                stemLengthField.setText("50");
                colorField.setText("");
                countryField.setText("NL");
                stockQuantityField.setText("10");

                assertFalse(dialog.saveItem());
                mockedOptionPane.verify(() -> JOptionPane.showMessageDialog(eq(dialog), eq("Колір квітки не може бути порожнім."), eq("Помилка введення"), eq(JOptionPane.ERROR_MESSAGE)));
            }
        }

        @Test
        @DisplayName("Збереження з порожньою країною - помилка")
        void saveItem_emptyCountry_showsErrorAndReturnsFalse() {
            try (MockedStatic<JOptionPane> mockedOptionPane = Mockito.mockStatic(JOptionPane.class)) {
                typeCombo.setSelectedItem(FlowerType.ROSE);
                priceField.setText("10.0");
                stemLengthField.setText("50");
                colorField.setText("Red");
                countryField.setText("");
                stockQuantityField.setText("10");

                assertFalse(dialog.saveItem());
                mockedOptionPane.verify(() -> JOptionPane.showMessageDialog(eq(dialog), eq("Країна походження не може бути порожньою."), eq("Помилка введення"), eq(JOptionPane.ERROR_MESSAGE)));
            }
        }

        @Test
        @DisplayName("Збереження з порожньою кількістю на складі - помилка")
        void saveItem_emptyStock_showsErrorAndReturnsFalse() {
            try (MockedStatic<JOptionPane> mockedOptionPane = Mockito.mockStatic(JOptionPane.class)) {
                typeCombo.setSelectedItem(FlowerType.ROSE);
                priceField.setText("10.0");
                stemLengthField.setText("50");
                colorField.setText("Red");
                countryField.setText("NL");
                stockQuantityField.setText("");

                assertFalse(dialog.saveItem());
                mockedOptionPane.verify(() -> JOptionPane.showMessageDialog(eq(dialog), eq("Кількість на складі не може бути порожньою."), eq("Помилка введення"), eq(JOptionPane.ERROR_MESSAGE)));
            }
        }

        @Test
        @DisplayName("Збереження з кількістю на складі < 0 - помилка")
        void saveItem_negativeStock_showsErrorAndReturnsFalse() {
            try (MockedStatic<JOptionPane> mockedOptionPane = Mockito.mockStatic(JOptionPane.class)) {
                typeCombo.setSelectedItem(FlowerType.ROSE);
                priceField.setText("10.0");
                stemLengthField.setText("50");
                colorField.setText("Red");
                countryField.setText("NL");
                stockQuantityField.setText("-5");

                assertFalse(dialog.saveItem());
                mockedOptionPane.verify(() -> JOptionPane.showMessageDialog(eq(dialog), eq("Кількість на складі не може бути від'ємною."), eq("Помилка введення"), eq(JOptionPane.ERROR_MESSAGE)));
            }
        }

        @Test
        @DisplayName("Збереження з неіснуючим файлом зображення - попередження (NO)")
        void saveItem_nonExistentImageFile_showsWarningAndDoesNotSave_ifNo() {
            try (MockedStatic<JOptionPane> mockedOptionPane = Mockito.mockStatic(JOptionPane.class);
                 MockedConstruction<File> mockedFileConstruction = Mockito.mockConstruction(File.class,
                         (mock, context) -> {
                             if (context.arguments().size() > 0 && !((String)context.arguments().get(0)).isEmpty()){
                                 when(mock.isFile()).thenReturn(false);
                                 when(mock.exists()).thenReturn(false);
                             } else {
                                 when(mock.isFile()).thenReturn(false);
                                 when(mock.exists()).thenReturn(false);
                             }
                         })) {


                mockedOptionPane.when(() -> JOptionPane.showConfirmDialog(
                                eq(dialog),
                                anyString(),
                                eq("Попередження: Зображення"),
                                eq(JOptionPane.YES_NO_OPTION),
                                eq(JOptionPane.WARNING_MESSAGE)
                        ))
                        .thenReturn(JOptionPane.NO_OPTION);

                typeCombo.setSelectedItem(FlowerType.CHRYSANTHEMUM);
                priceField.setText("12.00");
                stemLengthField.setText("55");
                colorField.setText("Білий");
                countryField.setText("Японія");
                stockQuantityField.setText("30");
                imagePathField.setText("non_existent_path.jpg");

                assertFalse(dialog.saveItem(), "saveItem should return false when user clicks NO on warning");
                if (dialog.item == null) {
                    assertNull(dialog.getFlower(), "Flower should not be created");
                }
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
                typeCombo.setSelectedItem(FlowerType.ROSE);
                priceField.setText("not_a_number");
                stemLengthField.setText("50");
                colorField.setText("Red");
                countryField.setText("NL");
                stockQuantityField.setText("10");

                assertFalse(dialog.saveItem(), "saveItem should return false for number format exception");
                mockedOptionPane.verify(() -> JOptionPane.showMessageDialog(
                        eq(dialog),
                        eq("Будь ласка, введіть коректні числові значення для ціни, довжини стебла та кількості."),
                        eq("Помилка введення"),
                        eq(JOptionPane.ERROR_MESSAGE)
                ));
            }
        }
    }

    @Nested
    @DisplayName("Тести взаємодії з UI")
    class InteractionTests {
        @BeforeEach
        void setUpInteractions() {
            initializeDialog(null);
        }

        @Test
        @DisplayName("Зміна значення слайдера свіжості оновлює мітку")
        void freshnessSlider_changeEvent_updatesLabelAndColor() {
            freshnessSlider.setValue(95);
            assertEquals("95%", freshnessValueLabel.getText(), "Label should update for high freshness");


            freshnessSlider.setValue(50);
            assertEquals("50%", freshnessValueLabel.getText(), "Label should update for medium freshness");

            freshnessSlider.setValue(10);
            assertEquals("10%", freshnessValueLabel.getText(), "Label should update for low freshness");
        }

        @Test
        @DisplayName("Натискання кнопки 'Огляд...' відкриває JFileChooser")
        void browseButton_actionPerformed_opensFileChooser() {

            try (MockedConstruction<JFileChooser> mockedFileChooserConstruction =
                         Mockito.mockConstruction(JFileChooser.class, (mock, context) -> {

                             when(mock.showOpenDialog(dialog)).thenReturn(JFileChooser.APPROVE_OPTION);
                             File mockSelectedFile = mock(File.class);
                             when(mockSelectedFile.getAbsolutePath()).thenReturn("mocked/path/image.jpg");
                             when(mock.getSelectedFile()).thenReturn(mockSelectedFile);
                         })) {

                dialog.browseButton.doClick();


                assertEquals(1, mockedFileChooserConstruction.constructed().size(), "JFileChooser should be constructed once");
                JFileChooser constructedChooser = mockedFileChooserConstruction.constructed().get(0);
                verify(constructedChooser).setDialogTitle("Виберіть зображення");
                verify(constructedChooser).showOpenDialog(dialog);

                assertEquals("mocked/path/image.jpg", imagePathField.getText(), "Image path field should be updated");
            }
        }

        @Nested
        @DisplayName("Тести кнопок OK/Cancel")
        class ButtonActionTests {

            @BeforeEach
            void setUpButtonTests() {
                initializeDialog(null);
            }

            @Test
            @DisplayName("Натискання 'Cancel' закриває діалог без збереження")
            void cancelButton_closesWithoutSaving() {
                AddEditFlowerDialog spyDialog = spy(dialog);
                JButton cancelButton = spyDialog.cancelButton;
                assertNotNull(cancelButton, "Cancel button should not be null");

                cancelButton.doClick();

                assertFalse(spyDialog.isSaved(), "Dialog should not be saved after cancel");

            }
        }
    }
}