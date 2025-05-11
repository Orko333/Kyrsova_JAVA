package flowershop.models;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Тести для класу Bouquet, що перевіряють його функціональність.
 */
class BouquetTest {
    private Bouquet bouquet;
    private Flower rose;
    private Flower tulip;
    private Accessory ribbon;
    private Accessory basket;

    @BeforeEach
    void setUp() {
        // Ініціалізація базових об'єктів для тестів
        bouquet = new Bouquet("Тестовий букет", "Опис тестового букета",
                new ArrayList<>(), new ArrayList<>(), "test.jpg", 10);

        rose = new Flower(Flower.FlowerType.ROSE, 50.0, 85, 40,
                "Червоний", "Україна", false, "rose.jpg", 10);
        tulip = new Flower(Flower.FlowerType.TULIP, 35.0, 90, 30,
                "Жовтий", "Нідерланди", false, "tulip.jpg", 15);

        ribbon = new Accessory("Стрічка", 15.0, Accessory.AccessoryType.RIBBON);
        basket = new Accessory("Кошик", 100.0, Accessory.AccessoryType.BASKET);
    }

    @Nested
    @DisplayName("Тести конструкторів")
    class ConstructorTests {
        @Test
        @DisplayName("Конструктор за замовчуванням ініціалізує порожній букет")
        void defaultConstructorCreatesEmptyBouquet() {
            Bouquet defaultBouquet = new Bouquet();

            assertEquals("Без назви", defaultBouquet.getName());
            assertEquals("", defaultBouquet.getDescription());
            assertTrue(defaultBouquet.getFlowers().isEmpty());
            assertTrue(defaultBouquet.getAccessories().isEmpty());
            assertEquals("", defaultBouquet.getImagePath());
            assertEquals(0, defaultBouquet.getDiscount());
        }

        @Test
        @DisplayName("Повний конструктор правильно ініціалізує поля")
        void fullConstructorInitializesAllFields() {
            List<Flower> flowers = Arrays.asList(rose, tulip);
            List<Accessory> accessories = Arrays.asList(ribbon, basket);

            Bouquet fullBouquet = new Bouquet("Святковий", "Святковий букет",
                    flowers, accessories, "image.jpg", 5);

            assertEquals("Святковий", fullBouquet.getName());
            assertEquals("Святковий букет", fullBouquet.getDescription());
            assertEquals(2, fullBouquet.getFlowers().size());
            assertEquals(2, fullBouquet.getAccessories().size());
            assertEquals("image.jpg", fullBouquet.getImagePath());
            assertEquals(5, fullBouquet.getDiscount());
        }

        @Test
        @DisplayName("Конструктор копіювання створює точну копію")
        void copyConstructorCreatesCopy() {
            bouquet.addFlower(rose);
            bouquet.addAccessory(ribbon);
            bouquet.setId(42);

            Bouquet copy = new Bouquet(bouquet);

            assertEquals(bouquet.getId(), copy.getId());
            assertEquals(bouquet.getName(), copy.getName());
            assertEquals(bouquet.getDescription(), copy.getDescription());
            assertEquals(bouquet.getImagePath(), copy.getImagePath());
            assertEquals(bouquet.getDiscount(), copy.getDiscount());
            assertEquals(1, copy.getFlowers().size());
            assertEquals(1, copy.getAccessories().size());
        }

        @Test
        @DisplayName("Конструктор перевіряє на null обов'язкові параметри")
        void constructorChecksForNullRequiredParameters() {
            assertThrows(NullPointerException.class, () ->
                    new Bouquet(null, "Опис", new ArrayList<>(), new ArrayList<>(), "image.jpg", 0));

            assertThrows(NullPointerException.class, () ->
                    new Bouquet("Назва", null, new ArrayList<>(), new ArrayList<>(), "image.jpg", 0));

            assertThrows(NullPointerException.class, () ->
                    new Bouquet("Назва", "Опис", null, new ArrayList<>(), "image.jpg", 0));

            assertThrows(NullPointerException.class, () ->
                    new Bouquet("Назва", "Опис", new ArrayList<>(), null, "image.jpg", 0));

            assertThrows(NullPointerException.class, () ->
                    new Bouquet("Назва", "Опис", new ArrayList<>(), new ArrayList<>(), null, 0));
        }

        @Test
        @DisplayName("Конструктор перевіряє валідність знижки")
        void constructorValidatesDiscount() {
            assertThrows(IllegalArgumentException.class, () ->
                    new Bouquet("Назва", "Опис", new ArrayList<>(), new ArrayList<>(), "image.jpg", -5));

            assertThrows(IllegalArgumentException.class, () ->
                    new Bouquet("Назва", "Опис", new ArrayList<>(), new ArrayList<>(), "image.jpg", 101));
        }
    }

    @Nested
    @DisplayName("Тести управління квітами")
    class FlowerManagementTests {
        @Test
        @DisplayName("Додавання однієї квітки")
        void addFlowerShouldAddSingleFlower() {
            bouquet.addFlower(rose);

            assertEquals(1, bouquet.getFlowers().size());
            assertEquals(rose, bouquet.getFlowers().get(0));
        }

        @Test
        @DisplayName("Додавання списку квітів")
        void addFlowersShouldAddMultipleFlowers() {
            List<Flower> flowers = Arrays.asList(rose, tulip);
            bouquet.addFlowers(flowers);

            assertEquals(2, bouquet.getFlowers().size());
            assertTrue(bouquet.getFlowers().contains(rose));
            assertTrue(bouquet.getFlowers().contains(tulip));
        }

        @Test
        @DisplayName("Видалення квітки за об'єктом")
        void removeFlowerShouldRemoveSpecificFlower() {
            bouquet.addFlower(rose);
            bouquet.addFlower(tulip);

            assertTrue(bouquet.removeFlower(rose));
            assertEquals(1, bouquet.getFlowers().size());
            assertEquals(tulip, bouquet.getFlowers().get(0));
        }

        @Test
        @DisplayName("Видалення квітки за індексом")
        void removeFlowerByIndexShouldRemoveAndReturn() {
            bouquet.addFlower(rose);
            bouquet.addFlower(tulip);

            Flower removed = bouquet.removeFlower(0);
            assertEquals(rose, removed);
            assertEquals(1, bouquet.getFlowers().size());
            assertEquals(tulip, bouquet.getFlowers().get(0));
        }

        @Test
        @DisplayName("Видалення квітки з недійсним індексом викидає виняток")
        void removeFlowerWithInvalidIndexThrowsException() {
            bouquet.addFlower(rose);

            assertThrows(IndexOutOfBoundsException.class, () -> bouquet.removeFlower(1));
            assertThrows(IndexOutOfBoundsException.class, () -> bouquet.removeFlower(-1));
        }

        @Test
        @DisplayName("Очищення списку квітів")
        void clearFlowersShouldRemoveAllFlowers() {
            bouquet.addFlower(rose);
            bouquet.addFlower(tulip);

            bouquet.clearFlowers();
            assertTrue(bouquet.getFlowers().isEmpty());
        }

        @Test
        @DisplayName("Управління кількістю квітів")
        void flowerQuantityManagement() {
            bouquet.addFlower(rose);

            // Перевірка початкової кількості
            assertEquals(10, bouquet.getFlowerQuantity(rose));

            // Зміна кількості
            bouquet.setFlowerQuantity(rose, 5);
            assertEquals(5, bouquet.getFlowerQuantity(rose));

            // Кількість неіснуючої квітки
            Flower nonExistent = new Flower(Flower.FlowerType.DAISY, 25.0, 80, 25);
            assertEquals(0, bouquet.getFlowerQuantity(nonExistent));
        }
    }

    @Nested
    @DisplayName("Тести управління аксесуарами")
    class AccessoryManagementTests {
        @Test
        @DisplayName("Додавання одного аксесуара")
        void addAccessoryShouldAddSingleAccessory() {
            bouquet.addAccessory(ribbon);

            assertEquals(1, bouquet.getAccessories().size());
            assertEquals(ribbon, bouquet.getAccessories().get(0));
        }

        @Test
        @DisplayName("Додавання списку аксесуарів")
        void addAccessoriesShouldAddMultipleAccessories() {
            List<Accessory> accessories = Arrays.asList(ribbon, basket);
            bouquet.addAccessories(accessories);

            assertEquals(2, bouquet.getAccessories().size());
            assertTrue(bouquet.getAccessories().contains(ribbon));
            assertTrue(bouquet.getAccessories().contains(basket));
        }

        @Test
        @DisplayName("Видалення аксесуара за об'єктом")
        void removeAccessoryShouldRemoveSpecificAccessory() {
            bouquet.addAccessory(ribbon);
            bouquet.addAccessory(basket);

            assertTrue(bouquet.removeAccessory(ribbon));
            assertEquals(1, bouquet.getAccessories().size());
            assertEquals(basket, bouquet.getAccessories().get(0));
        }

        @Test
        @DisplayName("Видалення аксесуара за індексом")
        void removeAccessoryByIndexShouldRemoveAndReturn() {
            bouquet.addAccessory(ribbon);
            bouquet.addAccessory(basket);

            Accessory removed = bouquet.removeAccessory(0);
            assertEquals(ribbon, removed);
            assertEquals(1, bouquet.getAccessories().size());
            assertEquals(basket, bouquet.getAccessories().get(0));
        }

        @Test
        @DisplayName("Видалення аксесуара з недійсним індексом викидає виняток")
        void removeAccessoryWithInvalidIndexThrowsException() {
            bouquet.addAccessory(ribbon);

            assertThrows(IndexOutOfBoundsException.class, () -> bouquet.removeAccessory(1));
            assertThrows(IndexOutOfBoundsException.class, () -> bouquet.removeAccessory(-1));
        }

        @Test
        @DisplayName("Очищення списку аксесуарів")
        void clearAccessoriesShouldRemoveAllAccessories() {
            bouquet.addAccessory(ribbon);
            bouquet.addAccessory(basket);

            bouquet.clearAccessories();
            assertTrue(bouquet.getAccessories().isEmpty());
        }
    }

    @Nested
    @DisplayName("Тести розрахунків")
    class CalculationTests {
        @Test
        @DisplayName("Розрахунок загальної вартості без знижки")
        void calculateTotalPriceSumsPriceOfAllItems() {
            bouquet.addFlower(rose); // 50.0
            bouquet.addFlower(tulip); // 35.0
            bouquet.addAccessory(ribbon); // 15.0
            bouquet.addAccessory(basket); // 100.0

            assertEquals(1140.0, bouquet.calculateTotalPrice());
        }

        @Test
        @DisplayName("Розрахунок вартості зі знижкою")
        void calculateDiscountedPriceAppliesDiscountCorrectly() {
            bouquet.addFlower(rose); // 50.0
            bouquet.addFlower(tulip); // 35.0
            bouquet.addAccessory(ribbon); // 15.0

            // Загальна ціна = 100.0, знижка 10%
            assertEquals(936.0, bouquet.calculateDiscountedPrice());

            // Зміна знижки на 20%
            bouquet.setDiscount(20);
            assertEquals(832.0, bouquet.calculateDiscountedPrice());

            // Нульова знижка
            bouquet.setDiscount(0);
            assertEquals(1040.0, bouquet.calculateDiscountedPrice());

            // Максимальна знижка
            bouquet.setDiscount(100);
            assertEquals(0.0, bouquet.calculateDiscountedPrice());
        }

        @Test
        @DisplayName("Розрахунок середньої свіжості")
        void calculateAverageFreshnessComputesCorrectAverage() {
            // rose.freshness = 85, tulip.freshness = 90
            bouquet.addFlower(rose);
            bouquet.addFlower(tulip);

            assertEquals(87.5, bouquet.calculateAverageFreshness());

            // Перевірка на порожній букет
            Bouquet emptyBouquet = new Bouquet();
            assertEquals(0.0, emptyBouquet.calculateAverageFreshness());
        }
    }

    @Nested
    @DisplayName("Тести сортування і фільтрації")
    class SortingAndFilteringTests {
        @Test
        @DisplayName("Сортування квітів за свіжістю")
        void sortFlowersByFreshnessSortsCorrectly() {
            // rose.freshness = 85, tulip.freshness = 90
            bouquet.addFlower(rose);
            bouquet.addFlower(tulip);

            bouquet.sortFlowersByFreshness();

            assertEquals(rose, bouquet.getFlowers().get(0));
            assertEquals(tulip, bouquet.getFlowers().get(1));
        }

        @Test
        @DisplayName("Сортування квітів за довжиною стебла")
        void sortFlowersByStemLengthSortsCorrectly() {
            // rose.stemLength = 40, tulip.stemLength = 30
            bouquet.addFlower(rose);
            bouquet.addFlower(tulip);

            bouquet.sortFlowersByStemLength();

            assertEquals(rose, bouquet.getFlowers().get(0));
            assertEquals(tulip, bouquet.getFlowers().get(1));
        }

        @Test
        @DisplayName("Фільтрація квітів за діапазоном довжини стебла")
        void findFlowersByStemLengthRangeFiltersCorrectly() {
            Flower shortRose = new Flower(Flower.FlowerType.ROSE, 45.0, 80, 20);
            Flower mediumRose = new Flower(Flower.FlowerType.ROSE, 50.0, 85, 35);
            Flower longRose = new Flower(Flower.FlowerType.ROSE, 55.0, 90, 50);

            bouquet.addFlower(shortRose);
            bouquet.addFlower(mediumRose);
            bouquet.addFlower(longRose);
            bouquet.addFlower(tulip); // stemLength = 30

            // Шукаємо квіти з довжиною стебла від 30 до 40
            List<Flower> filteredFlowers = bouquet.findFlowersByStemLengthRange(30, 40);

            assertEquals(2, filteredFlowers.size());
            assertTrue(filteredFlowers.contains(mediumRose));
            assertTrue(filteredFlowers.contains(tulip));
            assertFalse(filteredFlowers.contains(shortRose));
            assertFalse(filteredFlowers.contains(longRose));
        }
    }

    @Nested
    @DisplayName("Тести форматування інформації")
    class FormattingTests {
        @Test
        @DisplayName("Отримання короткої інформації")
        void getShortInfoFormatsCorrectly() {
            bouquet.setName("Святковий");
            bouquet.setDiscount(15);
            bouquet.addFlower(rose); // 50.0
            bouquet.addAccessory(ribbon); // 15.0

            String shortInfo = bouquet.getShortInfo();
            assertTrue(shortInfo.contains("Святковий"));
            assertTrue(shortInfo.contains("437,75")); // Ціна зі знижкою 15%
            assertTrue(shortInfo.contains("15% знижка"));
        }

        @Test
        @DisplayName("Отримання детальної інформації")
        void getDetailedInfoIncludesAllDetails() {
            bouquet.setName("Святковий");
            bouquet.setDescription("Чудовий букет");
            bouquet.setDiscount(10);
            bouquet.addFlower(rose);
            bouquet.addAccessory(ribbon);

            String detailedInfo = bouquet.getDetailedInfo();

            assertTrue(detailedInfo.contains("Святковий"));
            assertTrue(detailedInfo.contains("Чудовий букет"));
            assertTrue(detailedInfo.contains("515,00 грн")); // Загальна вартість
            assertTrue(detailedInfo.contains("10%")); // Знижка
            assertTrue(detailedInfo.contains("463,50 грн")); // Ціна зі знижкою
            assertTrue(detailedInfo.contains("Троянда")); // Тип квітки
            assertTrue(detailedInfo.contains("Стрічка")); // Назва аксесуара
        }

        @Test
        @DisplayName("Отримання інформації для кошика")
        void getCartInfoFormatsCorrectly() {
            bouquet.setName("Святковий");
            bouquet.addFlower(rose);
            bouquet.addFlower(tulip);
            bouquet.addAccessory(ribbon);

            String cartInfo = bouquet.getCartInfo();

            assertTrue(cartInfo.contains("Святковий"));
            assertTrue(cartInfo.contains("Квітів: 2"));
            assertTrue(cartInfo.contains("Аксесуарів: 1"));
            assertTrue(cartInfo.contains("936,00 грн")); // Ціна зі знижкою 10%
        }
    }

    @Nested
    @DisplayName("Тести геттерів і сеттерів")
    class GettersAndSettersTests {
        @Test
        @DisplayName("Управління полями через геттери і сеттери")
        void gettersAndSettersWorkCorrectly() {
            bouquet.setId(42);
            assertEquals(42, bouquet.getId());

            bouquet.setName("Новий букет");
            assertEquals("Новий букет", bouquet.getName());

            bouquet.setDescription("Новий опис");
            assertEquals("Новий опис", bouquet.getDescription());

            bouquet.setImagePath("new_image.jpg");
            assertEquals("new_image.jpg", bouquet.getImagePath());

            bouquet.setDiscount(25);
            assertEquals(25, bouquet.getDiscount());
        }

        @Test
        @DisplayName("Валідація знижки в сеттері")
        void discountSetterValidatesInput() {
            assertThrows(IllegalArgumentException.class, () -> bouquet.setDiscount(-5));
            assertThrows(IllegalArgumentException.class, () -> bouquet.setDiscount(101));
        }

        @Test
        @DisplayName("Валідація на null в сеттерах")
        void settersValidateNullInput() {
            assertThrows(NullPointerException.class, () -> bouquet.setName(null));
            assertThrows(NullPointerException.class, () -> bouquet.setDescription(null));
            assertThrows(NullPointerException.class, () -> bouquet.setImagePath(null));
        }
    }

    @Nested
    @DisplayName("Тести рівності і хешкоду")
    class EqualsAndHashCodeTests {
        @Test
        @DisplayName("Різні букети не повинні бути рівними")
        void differentBouquetsShouldNotBeEqual() {
            Bouquet bouquet1 = new Bouquet("Букет1", "Опис", new ArrayList<>(), new ArrayList<>(), "img.jpg", 5);
            Bouquet bouquet2 = new Bouquet("Букет2", "Опис", new ArrayList<>(), new ArrayList<>(), "img.jpg", 5);

            assertNotEquals(bouquet1, bouquet2);
        }

        @Test
        @DisplayName("Букети з різним вмістом не повинні бути рівними")
        void bouquetsWithDifferentContentShouldNotBeEqual() {
            Bouquet bouquet1 = new Bouquet("Букет", "Опис", new ArrayList<>(), new ArrayList<>(), "img.jpg", 5);
            Bouquet bouquet2 = new Bouquet("Букет", "Опис", new ArrayList<>(), new ArrayList<>(), "img.jpg", 5);

            bouquet1.addFlower(rose);

            assertNotEquals(bouquet1, bouquet2);
        }
    }

    @Nested
    @DisplayName("Інші тести")
    class MiscellaneousTests {
        @Test
        @DisplayName("Очищення всього букета")
        void clearShouldRemoveAllFlowersAndAccessories() {
            bouquet.addFlower(rose);
            bouquet.addAccessory(ribbon);

            bouquet.clear();

            assertTrue(bouquet.getFlowers().isEmpty());
            assertTrue(bouquet.getAccessories().isEmpty());
        }

        @Test
        @DisplayName("Метод toString повертає коректну інформацію")
        void toStringIncludesRelevantInfo() {
            bouquet.setName("Святковий");
            bouquet.addFlower(rose);
            bouquet.addAccessory(ribbon);

            String stringRepresentation = bouquet.toString();

            assertTrue(stringRepresentation.contains("Святковий"));
            assertTrue(stringRepresentation.contains("Квітів: 1"));
            assertTrue(stringRepresentation.contains("Аксесуарів: 1"));
            assertTrue(stringRepresentation.contains("463,50 грн")); // Ціна зі знижкою 10%
        }
    }
}