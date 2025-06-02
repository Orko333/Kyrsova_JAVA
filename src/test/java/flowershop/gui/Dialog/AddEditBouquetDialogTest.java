package flowershop.gui.Dialog;

import flowershop.models.Accessory;
import flowershop.models.Bouquet;
import flowershop.models.Flower;
import org.assertj.swing.edt.GuiActionRunner;
import org.assertj.swing.fixture.DialogFixture;
import org.assertj.swing.fixture.JButtonFixture;
import org.assertj.swing.fixture.JListFixture;
import org.assertj.swing.fixture.JSpinnerFixture;
import org.assertj.swing.fixture.JTextComponentFixture;
import org.assertj.swing.junit.testcase.AssertJSwingJUnitTestCase;
import org.assertj.swing.timing.Pause;
import org.junit.Test;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Автоматизовані тести для діалогового вікна {@link AddEditBouquetDialog}.
 * Використовує AssertJ Swing для взаємодії з GUI компонентами.
 * Розташування діалогів в тестах встановлюється у верхній частині екрану.
 * Розмір вікна контролюється налаштуваннями самого діалогу.
 */
public class AddEditBouquetDialogTest extends AssertJSwingJUnitTestCase {

    private static final Logger logger = LogManager.getLogger(AddEditBouquetDialogTest.class);

    private DialogFixture window;
    private AddEditBouquetDialog dialog;

    private List<Flower> sampleFlowers;
    private List<Accessory> sampleAccessories;
    private File tempImageFile;
    private File nonExistentImageFile;


    /**
     * Налаштування тестового середовища перед кожним тестом.
     */
    @Override
    protected void onSetUp() {
        logger.info("Початок налаштування тесту AddEditBouquetDialogTest...");
        sampleFlowers = new ArrayList<>();
        Flower rose = new Flower(Flower.FlowerType.ROSE, 10.0, 90, 50, "Червона", "Нідерланди", false, "rose.jpg", 100);
        rose.setId(1); // Для findRepresentativeFlower
        sampleFlowers.add(rose);
        Flower tulip = new Flower(Flower.FlowerType.TULIP, 5.0, 85, 30, "Жовтий", "Нідерланди", false, "tulip.jpg", 150);
        tulip.setId(2); // Для findRepresentativeFlower
        sampleFlowers.add(tulip);


        sampleAccessories = new ArrayList<>();
        Accessory ribbon = new Accessory("Стрічка", 2.0, "Декоративна стрічка", "ribbon.png", 50, Accessory.AccessoryType.RIBBON, "Червоний", "1м");
        ribbon.setId(10); // Для findRepresentativeAccessory
        sampleAccessories.add(ribbon);
        Accessory basket = new Accessory("Кошик", 15.0, "Плетений кошик", "basket.png", 20, Accessory.AccessoryType.BASKET, "Коричневий", "Малий");
        basket.setId(11); // Для findRepresentativeAccessory
        sampleAccessories.add(basket);


        try {
            tempImageFile = File.createTempFile("test_bouquet_image_", ".png");
            logger.info("Створено тимчасовий файл зображення: {}", tempImageFile.getAbsolutePath());
        } catch (IOException e) {
            logger.error("Не вдалося створити тимчасовий файл зображення", e);
            throw new RuntimeException("Не вдалося створити тимчасовий файл зображення", e);
        }
        nonExistentImageFile = new File("path/to/non_existent_image.jpg");


        UIManager.put("OptionPane.showConfirmDialog", (Callable<Integer>) () -> {
            logger.debug("JOptionPane.showConfirmDialog викликано в тесті, повертаємо YES_OPTION.");
            return JOptionPane.YES_OPTION;
        });
        UIManager.put("OptionPane.showMessageDialog", (Runnable) () -> {
            logger.debug("JOptionPane.showMessageDialog викликано в тесті, нічого не робимо.");
        });
        logger.info("Завершено налаштування тесту.");
    }

    /**
     * Встановлює розташування діалогового вікна у верхній частині екрану (x=0, y=0)
     * та розмір, визначений у діалоговому вікні (950x800).
     * Цей метод слід викликати ПІСЛЯ того, як діалог став видимим (після window.show()).
     * @param fixture Фікстура діалогу для зміни розташування та розміру.
     */
    private void setDialogPositionAndFixedSize(DialogFixture fixture) {
        GuiActionRunner.execute(() -> {
            JDialog targetDialog = fixture.targetCastedTo(JDialog.class);
            // Розмір встановлюється згідно з тим, що вказано в AddEditBouquetDialog
            // Якщо там немає setSize, то буде розмір після pack()
            // В AddEditBouquetDialog зараз є setSize(new Dimension(950, 800))
            // Тому ми просто встановлюємо позицію.
            // Якщо б ми хотіли гарантовано 950x800, можна було б додати:
            targetDialog.setSize(new Dimension(950, 800));
            if (GraphicsEnvironment.isHeadless()) {
                logger.warn("Headless середовище, пропускаємо зміну розташування вікна, розмір буде {}x{}", targetDialog.getWidth(), targetDialog.getHeight());
                return;
            }
            targetDialog.setLocation(0, 0);
            logger.info("Діалог '{}' встановлено у позицію (0,0). Фактична позиція: ({},{}), Розмір: {}x{}",
                    targetDialog.getTitle(), targetDialog.getX(), targetDialog.getY(), targetDialog.getWidth(), targetDialog.getHeight());
        });
        Pause.pause(150, TimeUnit.MILLISECONDS);
        fixture.focus();
    }

    private void initializeAndShowDialog(Bouquet bouquetToEdit) {
        dialog = GuiActionRunner.execute(() -> new AddEditBouquetDialog(null, bouquetToEdit, sampleFlowers, sampleAccessories));
        window = new DialogFixture(robot(), dialog);
        window.show();
        setDialogPositionAndFixedSize(window);
    }


    /**
     * Тест створення нового букета з усіма полями та збереження.
     */
    @Test
    public void testAddNewBouquetAndSave() {
        logger.info("Запуск тесту testAddNewBouquetAndSave...");
        initializeAndShowDialog(null);

        JTextComponentFixture nameField = window.textBox("nameField").requireVisible();
        JTextComponentFixture descriptionArea = window.textBox("descriptionArea").requireVisible();
        JSpinnerFixture discountSpinner = window.spinner("discountSpinner").requireVisible();
        JTextComponentFixture imagePathField = window.textBox("imagePathField").requireVisible();

        JListFixture availableFlowersList = window.list("availableFlowersList").requireVisible();
        JListFixture availableAccessoriesList = window.list("availableAccessoriesList").requireVisible();
        JSpinnerFixture flowerQuantitySpinner = window.spinner("flowerQuantitySpinner").requireVisible();
        JButtonFixture addFlowerButton = window.button("addFlowerButton").requireVisible();
        JButtonFixture addAccessoryButton = window.button("addAccessoryButton").requireVisible();
        JButtonFixture okButton = window.button("okButton").requireVisible();

        nameField.setText("Тестовий букет нове");
        descriptionArea.setText("Опис тестового букета нове");
        discountSpinner.select(10.0);

        GuiActionRunner.execute(() -> dialog.imagePathField.setText(tempImageFile.getAbsolutePath()));
        imagePathField.requireText(tempImageFile.getAbsolutePath());

        availableFlowersList.selectItem(0);
        flowerQuantitySpinner.select(2);
        addFlowerButton.click();

        availableFlowersList.selectItem(1);
        flowerQuantitySpinner.select(3);
        addFlowerButton.click();

        JListFixture selectedFlowersDisplayList = window.list("selectedFlowersDisplayList").requireVisible();
        assertThat(selectedFlowersDisplayList.contents()).hasSize(2);
        assertThat(selectedFlowersDisplayList.valueAt(0)).contains("Троянда").contains("(x2)");
        assertThat(selectedFlowersDisplayList.valueAt(1)).contains("Тюльпан").contains("(x3)");

        availableAccessoriesList.selectItem(0);
        addAccessoryButton.click();

        JListFixture selectedAccessoriesDisplayList = window.list("selectedAccessoriesDisplayList").requireVisible();
        assertThat(selectedAccessoriesDisplayList.contents()).hasSize(1);
        assertThat(selectedAccessoriesDisplayList.valueAt(0)).contains("Стрічка");

        okButton.click();

        assertThat(window.target().isShowing()).as("Діалог має бути закритим після збереження").isFalse();
        assertThat(dialog.isSaved()).as("Стан збереження має бути true").isTrue();
        Bouquet createdBouquet = dialog.getBouquet();
        assertThat(createdBouquet).as("Створений букет не має бути null").isNotNull();
        assertThat(createdBouquet.getName()).isEqualTo("Тестовий букет нове");
        assertThat(createdBouquet.getDescription()).isEqualTo("Опис тестового букета нове");
        assertThat(createdBouquet.getDiscount()).isEqualTo(10.0);
        assertThat(createdBouquet.getImagePath()).isEqualTo(tempImageFile.getAbsolutePath());
        assertThat(createdBouquet.getFlowers()).hasSize(5);
        assertThat(createdBouquet.getAccessories()).hasSize(1);

        long roseCount = createdBouquet.getFlowers().stream().filter(f -> f.getType() == Flower.FlowerType.ROSE).count();
        long tulipCount = createdBouquet.getFlowers().stream().filter(f -> f.getType() == Flower.FlowerType.TULIP).count();
        assertThat(roseCount).isEqualTo(2);
        assertThat(tulipCount).isEqualTo(3);

        assertThat(createdBouquet.getAccessories().get(0).getName()).isEqualTo("Стрічка");
        logger.info("Тест testAddNewBouquetAndSave успішно завершено.");
    }


    /**
     * Тест редагування існуючого букета.
     */
    @Test
    public void testEditExistingBouquet() {
        logger.info("Запуск тесту testEditExistingBouquet...");
        List<Flower> initialFlowers = new ArrayList<>();
        Flower initialRose = new Flower(sampleFlowers.get(0));
        initialRose.setId(1);
        initialFlowers.add(initialRose);

        List<Accessory> initialAccessories = new ArrayList<>();
        Accessory initialRibbon = new Accessory(sampleAccessories.get(0));
        initialRibbon.setId(10);
        initialAccessories.add(initialRibbon);

        Bouquet existingBouquet = GuiActionRunner.execute(() ->
                new Bouquet("Старий букет", "Старий опис", initialFlowers, initialAccessories, tempImageFile.getAbsolutePath(), 5.0)
        );
        existingBouquet.setId(101);

        initializeAndShowDialog(existingBouquet);

        JTextComponentFixture nameField = window.textBox("nameField").requireVisible();
        JSpinnerFixture discountSpinner = window.spinner("discountSpinner").requireVisible();
        JListFixture selectedFlowersDisplayList = window.list("selectedFlowersDisplayList").requireVisible();
        JButtonFixture removeFlowerButton = window.button("removeFlowerButton").requireVisible();
        JButtonFixture okButton = window.button("okButton").requireVisible();
        JListFixture selectedAccessoriesDisplayList = window.list("selectedAccessoriesDisplayList").requireVisible();
        JButtonFixture removeAccessoryButton = window.button("removeAccessoryButton").requireVisible();


        nameField.requireText("Старий букет");
        discountSpinner.requireValue(5.0);
        assertThat(selectedFlowersDisplayList.contents()).hasSize(1);
        assertThat(selectedFlowersDisplayList.valueAt(0)).contains("Троянда").contains("(x1)");
        assertThat(selectedAccessoriesDisplayList.contents()).hasSize(1);
        assertThat(selectedAccessoriesDisplayList.valueAt(0)).contains("Стрічка");


        nameField.deleteText().setText("Оновлений букет");
        discountSpinner.select(15.0);

        selectedFlowersDisplayList.selectItem(0);
        removeFlowerButton.click();
        assertThat(selectedFlowersDisplayList.contents()).isEmpty();

        selectedAccessoriesDisplayList.selectItem(0);
        removeAccessoryButton.click();
        assertThat(selectedAccessoriesDisplayList.contents()).isEmpty();


        JListFixture availableFlowersList = window.list("availableFlowersList").requireVisible();
        JSpinnerFixture flowerQuantitySpinner = window.spinner("flowerQuantitySpinner").requireVisible();
        JButtonFixture addFlowerButton = window.button("addFlowerButton").requireVisible();
        JListFixture availableAccessoriesList = window.list("availableAccessoriesList").requireVisible();
        JButtonFixture addAccessoryButton = window.button("addAccessoryButton").requireVisible();


        availableFlowersList.selectItem(1); // Тюльпан
        flowerQuantitySpinner.select(1);
        addFlowerButton.click();
        assertThat(selectedFlowersDisplayList.contents()).hasSize(1);
        assertThat(selectedFlowersDisplayList.valueAt(0)).contains("Тюльпан").contains("(x1)");

        availableAccessoriesList.selectItem(1); // Кошик
        addAccessoryButton.click();
        assertThat(selectedAccessoriesDisplayList.contents()).hasSize(1);
        assertThat(selectedAccessoriesDisplayList.valueAt(0)).contains("Кошик");


        okButton.click();

        assertThat(dialog.isSaved()).isTrue();
        Bouquet updatedBouquet = dialog.getBouquet();
        assertThat(updatedBouquet).isNotNull();
        assertThat(updatedBouquet.getId()).isEqualTo(101);
        assertThat(updatedBouquet.getName()).isEqualTo("Оновлений букет");
        assertThat(updatedBouquet.getDiscount()).isEqualTo(15.0);
        assertThat(updatedBouquet.getFlowers()).hasSize(1);
        assertThat(updatedBouquet.getFlowers().get(0).getType()).isEqualTo(Flower.FlowerType.TULIP);
        assertThat(updatedBouquet.getAccessories()).hasSize(1);
        assertThat(updatedBouquet.getAccessories().get(0).getType()).isEqualTo(Accessory.AccessoryType.BASKET);

        logger.info("Тест testEditExistingBouquet успішно завершено.");
    }

    /**
     * Тест скасування операції.
     */
    @Test
    public void testCancelOperation() {
        logger.info("Запуск тесту testCancelOperation...");
        initializeAndShowDialog(null);

        window.textBox("nameField").requireVisible().setText("Букет, який не буде збережено");
        window.button("cancelButton").requireVisible().click();

        assertThat(window.target().isShowing()).isFalse();
        assertThat(dialog.isSaved()).isFalse();
        assertThat(dialog.getBouquet()).isNull();
        logger.info("Тест testCancelOperation успішно завершено.");
    }

    /**
     * Тест валідації порожньої назви букета.
     */
    @Test
    public void testSaveWithEmptyName() {
        logger.info("Запуск тесту testSaveWithEmptyName...");
        initializeAndShowDialog(null);

        JTextComponentFixture nameField = window.textBox("nameField").requireVisible();
        JButtonFixture okButton = window.button("okButton").requireVisible();
        JListFixture availableFlowersList = window.list("availableFlowersList").requireVisible();
        JButtonFixture addFlowerButton = window.button("addFlowerButton").requireVisible();

        nameField.setText(""); // Порожня назва

        // Додаємо квітку, щоб не було помилки про порожній букет
        availableFlowersList.selectItem(0);
        addFlowerButton.click();

        okButton.click();

        // Очікуємо, що діалог не закриється і isSaved буде false
        assertThat(window.target().isShowing()).as("Діалог не має закриватися при порожній назві").isTrue();
        assertThat(dialog.isSaved()).as("Стан збереження має бути false при порожній назві").isFalse();
        // Тут можна було б перевірити появу showErrorDialog, але ми його заглушили.
        // Натомість, перевіряємо, що фокус повернувся на поле назви (якщо це реалізовано)
        // assertThat(nameField.target().hasFocus()).isTrue(); // Ця перевірка може бути нестабільною
        logger.info("Тест testSaveWithEmptyName успішно завершено.");
    }

    /**
     * Тест валідації порожнього букета (без квітів та аксесуарів).
     */
    @Test
    public void testSaveEmptyBouquet() {
        logger.info("Запуск тесту testSaveEmptyBouquet...");
        initializeAndShowDialog(null);

        JTextComponentFixture nameField = window.textBox("nameField").requireVisible();
        JButtonFixture okButton = window.button("okButton").requireVisible();

        nameField.setText("Порожній Букет");
        // Не додаємо жодних квітів чи аксесуарів

        okButton.click();

        assertThat(window.target().isShowing()).as("Діалог не має закриватися при порожньому букеті").isTrue();
        assertThat(dialog.isSaved()).as("Стан збереження має бути false при порожньому букеті").isFalse();
        logger.info("Тест testSaveEmptyBouquet успішно завершено.");
    }

    /**
     * Тест поведінки при виборі неіснуючого файлу зображення та підтвердженні збереження.
     */
    @Test
    public void testSaveWithNonExistentImageAndConfirm() {
        logger.info("Запуск тесту testSaveWithNonExistentImageAndConfirm...");
        initializeAndShowDialog(null);

        JTextComponentFixture nameField = window.textBox("nameField").requireVisible();
        JListFixture availableFlowersList = window.list("availableFlowersList").requireVisible();
        JButtonFixture addFlowerButton = window.button("addFlowerButton").requireVisible();
        JButtonFixture okButton = window.button("okButton").requireVisible();

        nameField.setText("Букет з неіснуючим зображенням");
        availableFlowersList.selectItem(0); // Додаємо квітку
        addFlowerButton.click();

        // Встановлюємо шлях до неіснуючого файлу
        GuiActionRunner.execute(() -> dialog.imagePathField.setText(nonExistentImageFile.getAbsolutePath()));
        window.textBox("imagePathField").requireText(nonExistentImageFile.getAbsolutePath());

        // UIManager вже налаштований повертати YES_OPTION для showConfirmDialog
        okButton.click();}



    /**
     * Очищення ресурсів після кожного тесту.
     */
    @Override
    protected void onTearDown() {
        logger.info("Початок очищення після тесту...");
        if (window != null) {
            window.cleanUp();
        }
        UIManager.put("OptionPane.showConfirmDialog", null);
        UIManager.put("OptionPane.showMessageDialog", null);

        if (tempImageFile != null && tempImageFile.exists()) {
            if (tempImageFile.delete()) {
                logger.info("Тимчасовий файл зображення {} видалено.", tempImageFile.getAbsolutePath());
            } else {
                logger.warn("Не вдалося видалити тимчасовий файл зображення: {}", tempImageFile.getAbsolutePath());
            }
        }
        logger.info("Завершено очищення після тесту.");
    }
}