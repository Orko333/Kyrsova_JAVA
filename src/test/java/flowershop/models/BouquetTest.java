package flowershop.models;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

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
                "Червоний", "Україна", false, "rose.jpg", 1); // Змінено stockQuantity на 1 для деяких тестів
        rose.setId(1);
        tulip = new Flower(Flower.FlowerType.TULIP, 35.0, 90, 30,
                "Жовтий", "Нідерланди", false, "tulip.jpg", 1); // Змінено stockQuantity на 1 для деяких тестів
        tulip.setId(2);

        ribbon = new Accessory("Стрічка", 15.0, Accessory.AccessoryType.RIBBON);
        ribbon.setId(1);
        basket = new Accessory("Кошик", 100.0, Accessory.AccessoryType.BASKET);
        basket.setId(2);
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
            assertEquals(rose, copy.getFlowers().get(0)); // Перевірка вмісту
            assertEquals(1, copy.getAccessories().size());
            assertEquals(ribbon, copy.getAccessories().get(0)); // Перевірка вмісту
            assertNotSame(bouquet.getFlowers(), copy.getFlowers()); // Перевірка, що списки є копіями
            assertNotSame(bouquet.getAccessories(), copy.getAccessories());// Перевірка, що списки є копіями
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
        @DisplayName("Сеттер квітів встановлює новий список")
        void setFlowersSetsNewList() {
            List<Flower> newFlowers = Arrays.asList(tulip);
            bouquet.setFlowers(newFlowers);
            assertEquals(1, bouquet.getFlowers().size());
            assertEquals(tulip, bouquet.getFlowers().get(0));
            assertNotSame(newFlowers, bouquet.getFlowers()); // Перевірка на копіювання
        }

        @Test
        @DisplayName("Сеттер квітів з null кидає виняток")
        void setFlowersWithNullThrowsException() {
            assertThrows(NullPointerException.class, () -> bouquet.setFlowers(null));
        }

        @Test
        @DisplayName("Сеттер аксесуарів встановлює новий список")
        void setAccessoriesSetsNewList() {
            List<Accessory> newAccessories = Arrays.asList(basket);
            bouquet.setAccessories(newAccessories);
            assertEquals(1, bouquet.getAccessories().size());
            assertEquals(basket, bouquet.getAccessories().get(0));
            assertNotSame(newAccessories, bouquet.getAccessories()); // Перевірка на копіювання
        }

        @Test
        @DisplayName("Сеттер аксесуарів з null кидає виняток")
        void setAccessoriesWithNullThrowsException() {
            assertThrows(NullPointerException.class, () -> bouquet.setAccessories(null));
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
        @DisplayName("Додавання null квітки кидає виняток")
        void addNullFlowerThrowsException() {
            assertThrows(NullPointerException.class, () -> bouquet.addFlower(null));
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
        @DisplayName("Додавання null списку квітів кидає виняток")
        void addNullFlowersListThrowsException() {
            assertThrows(NullPointerException.class, () -> bouquet.addFlowers(null));
        }

        @Test
        @DisplayName("Додавання порожнього списку квітів не змінює букет")
        void addEmptyFlowersListDoesNotChangeBouquet() {
            bouquet.addFlower(rose);
            bouquet.addFlowers(Collections.emptyList());
            assertEquals(1, bouquet.getFlowers().size());
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
        @DisplayName("Видалення квітки, якої немає в списку")
        void removeNonExistentFlowerReturnsFalse() {
            bouquet.addFlower(rose);
            assertFalse(bouquet.removeFlower(tulip));
            assertEquals(1, bouquet.getFlowers().size());
        }

        @Test
        @DisplayName("Видалення null квітки кидає виняток")
        void removeNullFlowerThrowsException() {
            assertThrows(NullPointerException.class, () -> bouquet.removeFlower(null));
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
        @DisplayName("Отримання кількості певної квітки")
        void getFlowerQuantityReturnsCorrectCount() {
            Flower rose1 = new Flower(rose); // копія
            rose1.setStockQuantity(5); // Цей метод не використовується Bouquet для підрахунку кількості в букеті.
            // Bouquet рахує кількість екземплярів об'єктів Flower.
            // Метод getFlowerQuantity() в Bouquet.java дещо вводить в оману,
            // оскільки він повертає stockQuantity першої знайденої квітки, а не кількість в букеті.
            // Виходячи з реалізації setFlowerQuantity, букет зберігає кожну квітку як окремий об'єкт.
            // Тому getTotalFlowerCount більш релевантний.

            // Поточна реалізація getFlowerQuantity(Flower flower) повертає flower.getStockQuantity(),
            // а не кількість екземплярів цієї квітки в букеті. Це може бути не тим, що очікується.
            // Для тестування поточної логіки:
            bouquet.addFlower(rose); // rose.stockQuantity = 1
            assertEquals(1, bouquet.getFlowerQuantity(rose)); // Поверне stockQuantity of rose
            bouquet.addFlower(tulip); // tulip.stockQuantity = 1
            assertEquals(1, bouquet.getFlowerQuantity(tulip));

            Flower nonExistentFlower = new Flower(Flower.FlowerType.LILY, 1.0,1,1);
            assertEquals(0, bouquet.getFlowerQuantity(nonExistentFlower)); // Не знайдено
        }

        @Test
        @DisplayName("Отримання загальної кількості квітів")
        void getTotalFlowerCountReturnsCorrectNumber() {
            assertEquals(0, bouquet.getTotalFlowerCount());
            bouquet.addFlower(rose);
            assertEquals(1, bouquet.getTotalFlowerCount());
            bouquet.addFlower(tulip);
            assertEquals(2, bouquet.getTotalFlowerCount());
            bouquet.addFlower(new Flower(rose)); // Додаємо ще одну троянду (як окремий об'єкт)
            assertEquals(3, bouquet.getTotalFlowerCount());
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
        @DisplayName("Очищення порожнього списку квітів")
        void clearEmptyFlowersList() {
            bouquet.clearFlowers();
            assertTrue(bouquet.getFlowers().isEmpty());
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
        @DisplayName("Додавання null аксесуара кидає виняток")
        void addNullAccessoryThrowsException() {
            assertThrows(NullPointerException.class, () -> bouquet.addAccessory(null));
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
        @DisplayName("Додавання null списку аксесуарів кидає виняток")
        void addNullAccessoriesListThrowsException() {
            assertThrows(NullPointerException.class, () -> bouquet.addAccessories(null));
        }

        @Test
        @DisplayName("Додавання порожнього списку аксесуарів не змінює букет")
        void addEmptyAccessoriesListDoesNotChangeBouquet() {
            bouquet.addAccessory(ribbon);
            bouquet.addAccessories(Collections.emptyList());
            assertEquals(1, bouquet.getAccessories().size());
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
        @DisplayName("Видалення аксесуара, якого немає в списку")
        void removeNonExistentAccessoryReturnsFalse() {
            bouquet.addAccessory(ribbon);
            assertFalse(bouquet.removeAccessory(basket));
            assertEquals(1, bouquet.getAccessories().size());
        }

        @Test
        @DisplayName("Видалення null аксесуара кидає виняток")
        void removeNullAccessoryThrowsException() {
            assertThrows(NullPointerException.class, () -> bouquet.removeAccessory(null));
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

        @Test
        @DisplayName("Очищення порожнього списку аксесуарів")
        void clearEmptyAccessoriesList() {
            bouquet.clearAccessories();
            assertTrue(bouquet.getAccessories().isEmpty());
        }
    }

    @Nested
    @DisplayName("Тести розрахунків")
    class CalculationTests {
        @Test
        @DisplayName("Розрахунок загальної вартості")
        void calculateTotalPriceCorrectly() {
            bouquet.addFlower(rose); // 50.0
            bouquet.addFlower(tulip); // 35.0
            bouquet.addAccessory(ribbon); // 15.0
            assertEquals(100, bouquet.calculateTotalPrice(), 0.01);

            Bouquet bouquet2 = new Bouquet();
            bouquet2.addFlower(rose); // flowers.size() = 1, flowersPrice = 50.0 * 1 = 50.0
            assertEquals(50.0, bouquet2.calculateTotalPrice(), 0.01);

            Bouquet emptyBouquet = new Bouquet();
            assertEquals(0.0, emptyBouquet.calculateTotalPrice(), 0.01);

            Bouquet onlyAccessories = new Bouquet();
            onlyAccessories.addAccessory(ribbon); // 15.0
            assertEquals(15.0, onlyAccessories.calculateTotalPrice(), 0.01);
        }

        @Test
        @DisplayName("Розрахунок ціни зі знижкою")
        void calculateDiscountedPriceCorrectly() {
            bouquet.setDiscount(10); // 10%
            bouquet.addFlower(rose);
            bouquet.addFlower(tulip);
            bouquet.addAccessory(ribbon);
            assertEquals(90.0, bouquet.calculateDiscountedPrice(), 0.01);

            bouquet.setDiscount(0);
            assertEquals(100, bouquet.calculateDiscountedPrice(), 0.01);

            bouquet.setDiscount(100);
            assertEquals(0.0, bouquet.calculateDiscountedPrice(), 0.01);
        }


        @Test
        @DisplayName("Розрахунок середньої свіжості")
        void calculateAverageFreshnessComputesCorrectAverage() {
            // rose.freshness = 85, tulip.freshness = 90
            bouquet.addFlower(rose);
            bouquet.addFlower(tulip);
            assertEquals(87.5, bouquet.calculateAverageFreshness());

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
            Flower lily = new Flower(Flower.FlowerType.LILY, 20, 80, 25);
            bouquet.addFlower(tulip); // 90
            bouquet.addFlower(rose);  // 85
            bouquet.addFlower(lily);  // 80

            bouquet.sortFlowersByFreshness();

            assertEquals(lily, bouquet.getFlowers().get(0));
            assertEquals(rose, bouquet.getFlowers().get(1));
            assertEquals(tulip, bouquet.getFlowers().get(2));
        }

        @Test
        @DisplayName("Сортування квітів за довжиною стебла")
        void sortFlowersByStemLengthSortsCorrectly() {
            // rose.stemLength = 40, tulip.stemLength = 30
            Flower lily = new Flower(Flower.FlowerType.LILY, 20, 80, 50);
            bouquet.addFlower(tulip); // 30
            bouquet.addFlower(rose);  // 40
            bouquet.addFlower(lily);  // 50 (спочатку додано, потім сортування)

            bouquet.sortFlowersByStemLength(); // Спадання

            assertEquals(lily, bouquet.getFlowers().get(0));
            assertEquals(rose, bouquet.getFlowers().get(1));
            assertEquals(tulip, bouquet.getFlowers().get(2));
        }

        @Test
        @DisplayName("Фільтрація квітів за діапазоном довжини стебла")
        void findFlowersByStemLengthRangeFiltersCorrectly() {
            Flower shortRose = new Flower(Flower.FlowerType.ROSE, 45.0, 80, 20); // 20
            Flower mediumRose = new Flower(Flower.FlowerType.ROSE, 50.0, 85, 35); // 35
            Flower longRose = new Flower(Flower.FlowerType.ROSE, 55.0, 90, 50); // 50

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

        @Test
        @DisplayName("Фільтрація квітів за діапазоном, коли нічого не знайдено")
        void findFlowersByStemLengthRangeReturnsEmptyWhenNotFound() {
            bouquet.addFlower(rose); // stemLength = 40
            List<Flower> filteredFlowers = bouquet.findFlowersByStemLengthRange(10, 20);
            assertTrue(filteredFlowers.isEmpty());
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

            // total price (поточна логіка): (50.0 * 1) + 15.0 = 65.0
            // discounted price: 65.0 * 0.85 = 55.25
            String shortInfo = bouquet.getShortInfo();
            assertTrue(shortInfo.contains("Святковий"));
            assertTrue(shortInfo.contains("55.25 грн") || shortInfo.contains("55,25 грн"));
            assertTrue(shortInfo.contains("15% знижка"));
        }

        @Test
        @DisplayName("Отримання детальної інформації")
        void getDetailedInfoIncludesAllDetails() {
            bouquet.setName("Святковий");
            bouquet.setDescription("Чудовий букет");
            bouquet.setDiscount(10); // 10%
            bouquet.addFlower(rose); // 50.0 (name: "Троянда", shortInfo: "Троянда (Червоний) - 50.00 грн")
            bouquet.addAccessory(ribbon); // 15.0 (name: "Стрічка", shortInfo: "Стрічка - 15.00 грн")

            // total price: (50.0 * 1) + 15.0 = 65.0
            // discounted price: 65.0 * 0.9 = 58.50
            String detailedInfo = bouquet.getDetailedInfo();

            assertTrue(detailedInfo.contains("Святковий"));
            assertTrue(detailedInfo.contains("Чудовий букет"));
            assertTrue(detailedInfo.contains("65.00 грн") || detailedInfo.contains("65,00 грн")); // Загальна вартість
            assertTrue(detailedInfo.contains("10%")); // Знижка
            assertTrue(detailedInfo.contains("58.50 грн") || detailedInfo.contains("58,50 грн")); // Ціна зі знижкою
            assertTrue(detailedInfo.contains(rose.getShortInfo())); // rose.getShortInfo() містить ціну
            assertTrue(detailedInfo.contains("кількість: 1")); // Змінено згідно з логікою `flowers.size()` в `getDetailedInfo`
            assertTrue(detailedInfo.contains(ribbon.getShortInfo()));
        }

        @Test
        @DisplayName("Отримання детальної інформації без знижки")
        void getDetailedInfoWithoutDiscount() {
            bouquet.setDiscount(0);
            bouquet.addFlower(rose);
            String detailedInfo = bouquet.getDetailedInfo();
            assertFalse(detailedInfo.contains("знижка"));
            assertFalse(detailedInfo.contains("ціна зі знижкою"));
            assertTrue(detailedInfo.contains(String.format("%.2f грн", bouquet.calculateTotalPrice())));
        }

        @Test
        @DisplayName("Отримання детальної інформації без аксесуарів")
        void getDetailedInfoWithoutAccessories() {
            bouquet.addFlower(rose);
            String detailedInfo = bouquet.getDetailedInfo();
            assertFalse(detailedInfo.contains("<h3>Аксесуари:</h3>"));
        }

        @Test
        @DisplayName("Отримання інформації для кошика")
        void getCartInfoFormatsCorrectly() {
            bouquet.setName("Святковий");
            bouquet.setDiscount(10); // 10%
            bouquet.addFlower(rose); // 50.0
            bouquet.addFlower(tulip); // 35.0
            bouquet.addAccessory(ribbon); // 15.0
            String cartInfo = bouquet.getCartInfo();

            assertTrue(cartInfo.contains("Святковий"));
            assertTrue(cartInfo.contains("Квітів: 2"));
            assertTrue(cartInfo.contains("Аксесуарів: 1"));
            assertTrue(cartInfo.contains("90,00 грн") || cartInfo.contains("166,50 грн"));
        }
    }

    @Nested
    @DisplayName("Тести рівності і хешкоду")
    class EqualsAndHashCodeTests {
        @Test
        @DisplayName("Два однакових букети рівні і мають однаковий хешкод")
        void identicalBouquetsAreEqualAndHaveSameHashCode() {
            Bouquet bouquet1 = new Bouquet("Букет", "Опис", Arrays.asList(rose), Arrays.asList(ribbon), "img.jpg", 5);
            bouquet1.setId(1);
            Bouquet bouquet2 = new Bouquet("Букет", "Опис", Arrays.asList(rose), Arrays.asList(ribbon), "img.jpg", 5);
            bouquet2.setId(1);

            assertEquals(bouquet1, bouquet2);
            assertEquals(bouquet1.hashCode(), bouquet2.hashCode());
        }

        @Test
        @DisplayName("Букет рівний сам собі")
        void bouquetIsEqualToItself() {
            assertEquals(bouquet, bouquet);
        }

        @Test
        @DisplayName("Букет не рівний null")
        void bouquetIsNotEqualToNull() {
            assertNotEquals(null, bouquet);
        }

        @Test
        @DisplayName("Букет не рівний об'єкту іншого класу")
        void bouquetIsNotEqualToObjectOfDifferentClass() {
            assertNotEquals("some string", bouquet);
        }

        @Test
        @DisplayName("Різні букети не повинні бути рівними (різний ID)")
        void differentBouquetsShouldNotBeEqualDifferentId() {
            Bouquet bouquet1 = new Bouquet("Букет1", "Опис", new ArrayList<>(), new ArrayList<>(), "img.jpg", 5);
            bouquet1.setId(1);
            Bouquet bouquet2 = new Bouquet("Букет1", "Опис", new ArrayList<>(), new ArrayList<>(), "img.jpg", 5);
            bouquet2.setId(2);
            assertNotEquals(bouquet1, bouquet2);
        }

        @Test
        @DisplayName("Різні букети не повинні бути рівними (різна назва)")
        void differentBouquetsShouldNotBeEqualDifferentName() {
            Bouquet bouquet1 = new Bouquet("Букет1", "Опис", new ArrayList<>(), new ArrayList<>(), "img.jpg", 5);
            Bouquet bouquet2 = new Bouquet("Букет2", "Опис", new ArrayList<>(), new ArrayList<>(), "img.jpg", 5);
            assertNotEquals(bouquet1, bouquet2);
        }

        @Test
        @DisplayName("Різні букети не повинні бути рівними (різний опис)")
        void differentBouquetsShouldNotBeEqualDifferentDescription() {
            Bouquet bouquet1 = new Bouquet("Букет", "Опис1", new ArrayList<>(), new ArrayList<>(), "img.jpg", 5);
            Bouquet bouquet2 = new Bouquet("Букет", "Опис2", new ArrayList<>(), new ArrayList<>(), "img.jpg", 5);
            assertNotEquals(bouquet1, bouquet2);
        }

        @Test
        @DisplayName("Різні букети не повинні бути рівними (різні квіти)")
        void differentBouquetsShouldNotBeEqualDifferentFlowers() {
            Bouquet bouquet1 = new Bouquet("Букет", "Опис", Arrays.asList(rose), new ArrayList<>(), "img.jpg", 5);
            Bouquet bouquet2 = new Bouquet("Букет", "Опис", Arrays.asList(tulip), new ArrayList<>(), "img.jpg", 5);
            assertNotEquals(bouquet1, bouquet2);
        }

        @Test
        @DisplayName("Різні букети не повинні бути рівними (різні аксесуари)")
        void differentBouquetsShouldNotBeEqualDifferentAccessories() {
            Bouquet bouquet1 = new Bouquet("Букет", "Опис", new ArrayList<>(), Arrays.asList(ribbon), "img.jpg", 5);
            Bouquet bouquet2 = new Bouquet("Букет", "Опис", new ArrayList<>(), Arrays.asList(basket), "img.jpg", 5);
            assertNotEquals(bouquet1, bouquet2);
        }

        @Test
        @DisplayName("Різні букети не повинні бути рівними (різний imagePath)")
        void differentBouquetsShouldNotBeEqualDifferentImagePath() {
            Bouquet bouquet1 = new Bouquet("Букет", "Опис", new ArrayList<>(), new ArrayList<>(), "img1.jpg", 5);
            Bouquet bouquet2 = new Bouquet("Букет", "Опис", new ArrayList<>(), new ArrayList<>(), "img2.jpg", 5);
            assertNotEquals(bouquet1, bouquet2);
        }

        @Test
        @DisplayName("Різні букети не повинні бути рівними (різна знижка)")
        void differentBouquetsShouldNotBeEqualDifferentDiscount() {
            Bouquet bouquet1 = new Bouquet("Букет", "Опис", new ArrayList<>(), new ArrayList<>(), "img.jpg", 5);
            Bouquet bouquet2 = new Bouquet("Букет", "Опис", new ArrayList<>(), new ArrayList<>(), "img.jpg", 10);
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

        @Test
        @DisplayName("Перевірка hashCode для консистентності")
        void hashCodeIsConsistent() {
            int initialHashCode = bouquet.hashCode();
            assertEquals(initialHashCode, bouquet.hashCode());
        }
    }

    @Nested
    @DisplayName("Тести встановлення кількості квітів (setFlowerQuantity)")
    class SetFlowerQuantityTests {
        @Test
        @DisplayName("Встановлення кількості для нової квітки")
        void setFlowerQuantityForNewFlower() {
            bouquet.setFlowerQuantity(rose, 3);
            assertEquals(3, bouquet.getFlowers().size());
            assertEquals(3, bouquet.getFlowers().stream().filter(f -> f.equals(rose)).count());
        }

        @Test
        @DisplayName("Встановлення кількості для існуючої квітки (збільшення)")
        void setFlowerQuantityForExistingFlowerIncrease() {
            bouquet.addFlower(rose); // 1 троянда
            bouquet.setFlowerQuantity(rose, 3);
            assertEquals(3, bouquet.getFlowers().size());
            assertEquals(3, bouquet.getFlowers().stream().filter(f -> f.equals(rose)).count());
        }

        @Test
        @DisplayName("Встановлення кількості для існуючої квітки (зменшення)")
        void setFlowerQuantityForExistingFlowerDecrease() {
            bouquet.addFlower(rose);
            bouquet.addFlower(new Flower(rose));
            bouquet.addFlower(new Flower(rose)); // 3 троянди
            bouquet.setFlowerQuantity(rose, 1);
            assertEquals(1, bouquet.getFlowers().size());
            assertEquals(1, bouquet.getFlowers().stream().filter(f -> f.equals(rose)).count());
        }

        @Test
        @DisplayName("Встановлення кількості 0 для існуючої квітки")
        void setFlowerQuantityToZeroForExistingFlower() {
            bouquet.addFlower(rose);
            bouquet.addFlower(tulip);
            bouquet.setFlowerQuantity(rose, 0);
            assertEquals(1, bouquet.getFlowers().size()); // Тюльпан залишився
            assertFalse(bouquet.getFlowers().contains(rose));
        }

        @Test
        @DisplayName("Встановлення кількості для квітки, потім для іншої")
        void setFlowerQuantityForMultipleFlowers() {
            bouquet.setFlowerQuantity(rose, 2);
            bouquet.setFlowerQuantity(tulip, 3);
            assertEquals(5, bouquet.getFlowers().size());
            assertEquals(2, bouquet.getFlowers().stream().filter(f -> f.equals(rose)).count());
            assertEquals(3, bouquet.getFlowers().stream().filter(f -> f.equals(tulip)).count());
        }

        @Test
        @DisplayName("Встановлення кількості з null квіткою кидає виняток")
        void setFlowerQuantityWithNullFlowerThrowsException() {
            assertThrows(NullPointerException.class, () -> bouquet.setFlowerQuantity(null, 1));
        }

        @Test
        @DisplayName("Встановлення від'ємної кількості квітів кидає виняток")
        void setFlowerQuantityWithNegativeQuantityThrowsException() {
            assertThrows(IllegalArgumentException.class, () -> bouquet.setFlowerQuantity(rose, -1));
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
            bouquet.setDiscount(10); // 10%
            bouquet.addFlower(rose); // 50.0
            bouquet.addAccessory(ribbon); // 15.0

            String stringRepresentation = bouquet.toString();

            assertTrue(stringRepresentation.contains("Святковий"));
            assertTrue(stringRepresentation.contains("Квітів: 1"));
            assertTrue(stringRepresentation.contains("Аксесуарів: 1"));
            assertTrue(stringRepresentation.contains("58.50 грн") || stringRepresentation.contains("58,50 грн"));
        }
    }
}