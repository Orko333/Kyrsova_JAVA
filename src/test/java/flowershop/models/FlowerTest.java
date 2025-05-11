package flowershop.models;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;

import javax.swing.*;
import java.awt.*;

import static org.junit.jupiter.api.Assertions.*;

class FlowerTest {
    private Flower flower;
    private final Flower.FlowerType type = Flower.FlowerType.ROSE;
    private final double price = 45.99;
    private final int freshness = 85;
    private final int stemLength = 30;
    private final String color = "Red";
    private final String countryOfOrigin = "Netherlands";
    private final boolean isPotted = false;
    private final String imagePath = "images/rose.jpg";
    private final int stockQuantity = 15;

    @BeforeEach
    void setUp() {
        flower = new Flower(type, price, freshness, stemLength, color,
                countryOfOrigin, isPotted, imagePath, stockQuantity);
    }

    // Тести конструкторів
    @Test
    @DisplayName("Повний конструктор з коректними параметрами")
    void fullConstructorWithValidParameters() {
        assertNotNull(flower);
        assertEquals(type, flower.getType());
        assertEquals(price, flower.getPrice());
        assertEquals(freshness, flower.getFreshness());
        assertEquals(stemLength, flower.getStemLength());
        assertEquals(color, flower.getColor());
        assertEquals(countryOfOrigin, flower.getCountryOfOrigin());
        assertEquals(isPotted, flower.isPotted());
        assertEquals(imagePath, flower.getImagePath());
        assertEquals(stockQuantity, flower.getStockQuantity());
    }

    @Test
    @DisplayName("Спрощений конструктор")
    void simplifiedConstructor() {
        Flower simpleFlower = new Flower(Flower.FlowerType.TULIP, 20.0, 75, 25);
        assertEquals(Flower.FlowerType.TULIP, simpleFlower.getType());
        assertEquals(20.0, simpleFlower.getPrice());
        assertEquals(75, simpleFlower.getFreshness());
        assertEquals(25, simpleFlower.getStemLength());
        assertEquals("Без кольору", simpleFlower.getColor());
        assertEquals("Невідомо", simpleFlower.getCountryOfOrigin());
        assertFalse(simpleFlower.isPotted());
        assertEquals("", simpleFlower.getImagePath());
        assertEquals(0, simpleFlower.getStockQuantity());
    }

    @Test
    @DisplayName("Конструктор копіювання")
    void copyConstructor() {
        Flower copy = new Flower(flower);
        assertNotSame(flower, copy);
        assertEquals(flower.getType(), copy.getType());
        assertEquals(flower.getPrice(), copy.getPrice());
        assertEquals(flower.getFreshness(), copy.getFreshness());
        assertEquals(flower.getStemLength(), copy.getStemLength());
        assertEquals(flower.getColor(), copy.getColor());
        assertEquals(flower.getCountryOfOrigin(), copy.getCountryOfOrigin());
        assertEquals(flower.isPotted(), copy.isPotted());
        assertEquals(flower.getImagePath(), copy.getImagePath());
        assertEquals(flower.getStockQuantity(), copy.getStockQuantity());
    }

    // Тести валідації в конструкторах
    @Test
    @DisplayName("Конструктор з від'ємною ціною - виняток")
    void constructorWithNegativePriceThrowsException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> new Flower(type, -1.0, freshness, stemLength, color,
                        countryOfOrigin, isPotted, imagePath, stockQuantity));
        assertEquals("Ціна не може бути від'ємною", exception.getMessage());
    }

    @Test
    @DisplayName("Конструктор з невалідною свіжістю - виняток")
    void constructorWithInvalidFreshnessThrowsException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> new Flower(type, price, 150, stemLength, color,
                        countryOfOrigin, isPotted, imagePath, stockQuantity));
        assertEquals("Свіжість має бути в межах 0–100", exception.getMessage());
    }

    @Test
    @DisplayName("Конструктор з від'ємною довжиною стебла - виняток")
    void constructorWithNegativeStemLengthThrowsException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> new Flower(type, price, freshness, -5, color,
                        countryOfOrigin, isPotted, imagePath, stockQuantity));
        assertEquals("Довжина стебла не може бути від'ємною", exception.getMessage());
    }

    @Test
    @DisplayName("Конструктор з від'ємною кількістю - виняток")
    void constructorWithNegativeStockThrowsException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> new Flower(type, price, freshness, stemLength, color,
                        countryOfOrigin, isPotted, imagePath, -1));
        assertEquals("Кількість на складі не може бути від'ємною", exception.getMessage());
    }

    @Test
    @DisplayName("Конструктор з null типом - виняток")
    void constructorWithNullTypeThrowsException() {
        NullPointerException exception = assertThrows(NullPointerException.class,
                () -> new Flower(null, price, freshness, stemLength, color,
                        countryOfOrigin, isPotted, imagePath, stockQuantity));
        assertEquals("Тип квітки не може бути null", exception.getMessage());
    }

    // Тести сетерів з валідацією
    @Test
    @DisplayName("Сеттер ціни з коректним значенням")
    void setPriceWithValidValue() {
        flower.setPrice(50.0);
        assertEquals(50.0, flower.getPrice());
    }

    @Test
    @DisplayName("Сеттер ціни з від'ємним значенням - виняток")
    void setPriceWithNegativeValueThrowsException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> flower.setPrice(-1.0));
        assertEquals("Ціна не може бути від'ємною", exception.getMessage());
    }

    @Test
    @DisplayName("Сеттер свіжості з коректним значенням")
    void setFreshnessWithValidValue() {
        flower.setFreshness(90);
        assertEquals(90, flower.getFreshness());
    }

    @Test
    @DisplayName("Сеттер свіжості з невалідним значенням - виняток")
    void setFreshnessWithInvalidValueThrowsException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> flower.setFreshness(150));
        assertEquals("Свіжість має бути в межах 0–100", exception.getMessage());
    }

    @Test
    @DisplayName("Сеттер довжини стебла з коректним значенням")
    void setStemLengthWithValidValue() {
        flower.setStemLength(35);
        assertEquals(35, flower.getStemLength());
    }

    @Test
    @DisplayName("Сеттер довжини стебла з від'ємним значенням - виняток")
    void setStemLengthWithNegativeValueThrowsException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> flower.setStemLength(-5));
        assertEquals("Довжина стебла не може бути від'ємною", exception.getMessage());
    }

    @Test
    @DisplayName("Сеттер кількості з коректним значенням")
    void setStockQuantityWithValidValue() {
        flower.setStockQuantity(20);
        assertEquals(20, flower.getStockQuantity());
    }

    @Test
    @DisplayName("Сеттер кількості з від'ємним значенням - виняток")
    void setStockQuantityWithNegativeValueThrowsException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> flower.setStockQuantity(-1));
        assertEquals("Кількість на складі не може бути від'ємною", exception.getMessage());
    }

    // Тести методів бізнес-логіки
    @Test
    @DisplayName("Зменшення запасу з коректною кількістю")
    void decreaseStockWithValidQuantity() {
        flower.decreaseStock(5);
        assertEquals(10, flower.getStockQuantity());
    }

    @Test
    @DisplayName("Зменшення запасу з від'ємною кількістю - виняток")
    void decreaseStockWithNegativeQuantityThrowsException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> flower.decreaseStock(-1));
        assertEquals("Кількість для зменшення не може бути від'ємною", exception.getMessage());
    }

    @Test
    @DisplayName("Зменшення запасу з недостатньою кількістю - виняток")
    void decreaseStockWithInsufficientStockThrowsException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> flower.decreaseStock(20));
        assertEquals("Недостатньо квіток на складі", exception.getMessage());
    }

    @Test
    @DisplayName("Збільшення запасу з коректною кількістю")
    void increaseStockWithValidQuantity() {
        flower.increaseStock(5);
        assertEquals(20, flower.getStockQuantity());
    }

    @Test
    @DisplayName("Збільшення запасу з від'ємною кількістю - виняток")
    void increaseStockWithNegativeQuantityThrowsException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> flower.increaseStock(-1));
        assertEquals("Кількість для збільшення не може бути від'ємною", exception.getMessage());
    }

    // Тести методів відображення інформації
    @Test
    @DisplayName("Коротка інформація про квітку")
    void getShortInfoReturnsCorrectFormat() {
        String expected = "Троянда (Red) - 45,99 грн";
        assertEquals(expected, flower.getShortInfo());
    }

    @Test
    @DisplayName("Детальна інформація у HTML форматі")
    void getDetailedInfoReturnsCorrectHTML() {
        String expected = "<html><b>Назва:</b> Троянда<br>" +
                "<b>Колір:</b> Red<br>" +
                "<b>Ціна:</b> 45,99 грн<br>" +
                "<b>Свіжість:</b> Висока (85%)<br>" +
                "<b>Довжина стебла:</b> 30 см<br>" +
                "<b>Країна походження:</b> Netherlands<br>" +
                "<b>В горщику:</b> Ні<br>" +
                "<b>На складі:</b> 15<br>" +
                "<b>Опис:</b> Троянда red кольору з довжиною стебла 30 см. Зрізана квітка. Свіжість: висока.</html>";
        assertEquals(expected, flower.getDetailedInfo());
    }

    @Test
    @DisplayName("Генерація опису квітки")
    void generateDescriptionReturnsCorrectFormat() {
        String expected = "Троянда red кольору з довжиною стебла 30 см. Зрізана квітка. Свіжість: висока.";
        assertEquals(expected, flower.generateDescription());
    }

    @Test
    @DisplayName("Інформація для кошика")
    void getCartInfoReturnsCorrectFormat() {
        String expected = "Троянда (Red) - 45,99 грн";
        assertEquals(expected, flower.getCartInfo());
    }

    @Test
    @DisplayName("Рядкове представлення об'єкта")
    void toStringReturnsCorrectFormat() {
        String expected = "Троянда (Netherlands) - 45,99 грн [Свіжість: Висока, Стебло: 30 см, Колір: Red, К-сть: 15]";
        assertEquals(expected, flower.toString());
    }

    // Тести для enum FlowerType
    @Test
    @DisplayName("Enum значення та їх відображувані назви")
    void flowerTypeEnumValuesAndDisplayNames() {
        assertEquals("Троянда", Flower.FlowerType.ROSE.getDisplayName());
        assertEquals("Лілія", Flower.FlowerType.LILY.getDisplayName());
        assertEquals("Тюльпан", Flower.FlowerType.TULIP.getDisplayName());
        assertEquals("Хризантема", Flower.FlowerType.CHRYSANTHEMUM.getDisplayName());
        assertEquals("Орхідея", Flower.FlowerType.ORCHID.getDisplayName());
        assertEquals("Соняшник", Flower.FlowerType.SUNFLOWER.getDisplayName());
        assertEquals("Ромашка", Flower.FlowerType.DAISY.getDisplayName());
        assertEquals("Півонія", Flower.FlowerType.PEONY.getDisplayName());
        assertEquals("Гортензія", Flower.FlowerType.HYDRANGEA.getDisplayName());
        assertEquals("Лаванда", Flower.FlowerType.LAVENDER.getDisplayName());
    }

    // Тести для enum FreshnessLevel
    @Test
    @DisplayName("Визначення рівня свіжості за значенням")
    void freshnessLevelFromValue() {
        assertEquals(Flower.FreshnessLevel.VERY_HIGH, Flower.FreshnessLevel.fromValue(95));
        assertEquals(Flower.FreshnessLevel.HIGH, Flower.FreshnessLevel.fromValue(80));
        assertEquals(Flower.FreshnessLevel.MEDIUM, Flower.FreshnessLevel.fromValue(60));
        assertEquals(Flower.FreshnessLevel.LOW, Flower.FreshnessLevel.fromValue(30));
        assertEquals(Flower.FreshnessLevel.VERY_LOW, Flower.FreshnessLevel.fromValue(10));
    }

    @Test
    @DisplayName("Отримання рівня свіжості квітки")
    void getFreshnessLevelReturnsCorrectLevel() {
        flower.setFreshness(95);
        assertEquals(Flower.FreshnessLevel.VERY_HIGH, flower.getFreshnessLevel());
    }

    // Тести equals та hashCode
    @Test
    @DisplayName("Equals для одного об'єкта")
    void equalsWithSameObject() {
        assertEquals(flower, flower);
    }

    @Test
    @DisplayName("Equals для однакових об'єктів")
    void equalsWithEqualObjects() {
        Flower sameFlower = new Flower(type, price, freshness, stemLength, color,
                countryOfOrigin, isPotted, imagePath, stockQuantity);
        assertEquals(flower, sameFlower);
    }

    @Test
    @DisplayName("Equals для різних об'єктів")
    void equalsWithDifferentObjects() {
        Flower differentFlower = new Flower(Flower.FlowerType.TULIP, price, freshness,
                stemLength, color, countryOfOrigin,
                isPotted, imagePath, stockQuantity);
        assertNotEquals(flower, differentFlower);
    }

    @Test
    @DisplayName("Equals з null об'єктом")
    void equalsWithNullObject() {
        assertNotEquals(flower, null);
    }

    @Test
    @DisplayName("Equals з об'єктом іншого класу")
    void equalsWithDifferentClassObject() {
        assertNotEquals(flower, new Object());
    }

    @Test
    @DisplayName("Консистентність hashCode")
    void hashCodeConsistency() {
        int initialHashCode = flower.hashCode();
        assertEquals(initialHashCode, flower.hashCode());
    }

    @Test
    @DisplayName("HashCode для однакових об'єктів")
    void hashCodeForEqualObjects() {
        Flower sameFlower = new Flower(type, price, freshness, stemLength, color,
                countryOfOrigin, isPotted, imagePath, stockQuantity);
        assertEquals(flower.hashCode(), sameFlower.hashCode());
    }

    // Тести для інших методів
    @Test
    @DisplayName("Отримання відображуваної назви")
    void getDisplayNameReturnsCorrectName() {
        assertEquals("Троянда", flower.getDisplayName());
    }

    @Test
    @DisplayName("Отримання ID")
    void getId() {
        assertEquals(0, flower.getId()); // За замовчуванням 0, якщо не встановлено
    }

    // Тести сетерів без валідації
    @Test
    @DisplayName("Сеттер типу")
    void setType() {
        flower.setType(Flower.FlowerType.LILY);
        assertEquals(Flower.FlowerType.LILY, flower.getType());
    }

    @Test
    @DisplayName("Сеттер кольору")
    void setColor() {
        flower.setColor("White");
        assertEquals("White", flower.getColor());
    }

    @Test
    @DisplayName("Сеттер кольору з null - виняток")
    void setColorWithNullThrowsException() {
        NullPointerException exception = assertThrows(NullPointerException.class,
                () -> flower.setColor(null));
        assertEquals("Колір не може бути null", exception.getMessage());
    }

    @Test
    @DisplayName("Сеттер країни походження")
    void setCountryOfOrigin() {
        flower.setCountryOfOrigin("Ecuador");
        assertEquals("Ecuador", flower.getCountryOfOrigin());
    }

    @Test
    @DisplayName("Сеттер країни походження з null - виняток")
    void setCountryOfOriginWithNullThrowsException() {
        NullPointerException exception = assertThrows(NullPointerException.class,
                () -> flower.setCountryOfOrigin(null));
        assertEquals("Країна походження не може бути null", exception.getMessage());
    }

    @Test
    @DisplayName("Сеттер горщика")
    void setPotted() {
        flower.setPotted(true);
        assertTrue(flower.isPotted());
    }

    @Test
    @DisplayName("Сеттер шляху до зображення")
    void setImagePath() {
        flower.setImagePath("new/path.jpg");
        assertEquals("new/path.jpg", flower.getImagePath());
    }

    @Test
    @DisplayName("Сеттер шляху до зображення з null - виняток")
    void setImagePathWithNullThrowsException() {
        NullPointerException exception = assertThrows(NullPointerException.class,
                () -> flower.setImagePath(null));
        assertEquals("Шлях до зображення не може бути null", exception.getMessage());
    }
}

