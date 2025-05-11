package flowershop.models;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AccessoryTest {
    private Accessory accessory;
    private final String name = "Golden Ribbon";
    private final double price = 25.99;
    private final String description = "Beautiful golden ribbon for bouquets";
    private final String imagePath = "images/ribbon.jpg";
    private final int stockQuantity = 10;
    private final Accessory.AccessoryType type = Accessory.AccessoryType.RIBBON;
    private final String color = "Gold";
    private final String size = "2m";

    @BeforeEach
    void setUp() {
        accessory = new Accessory(name, price, description, imagePath, stockQuantity, type, color, size);
    }

    // Тести конструкторів
    @Test
    @DisplayName("Повний конструктор з коректними параметрами")
    void fullConstructorWithValidParameters() {
        assertNotNull(accessory);
        assertEquals(name, accessory.getName());
        assertEquals(price, accessory.getPrice());
        assertEquals(description, accessory.getDescription());
        assertEquals(imagePath, accessory.getImagePath());
        assertEquals(stockQuantity, accessory.getStockQuantity());
        assertEquals(type, accessory.getType());
        assertEquals(color, accessory.getColor());
        assertEquals(size, accessory.getSize());
    }

    @Test
    @DisplayName("Спрощений конструктор")
    void simplifiedConstructor() {
        Accessory simpleAccessory = new Accessory("Simple", 10.0, Accessory.AccessoryType.WRAPPING_PAPER);
        assertEquals("Simple", simpleAccessory.getName());
        assertEquals(10.0, simpleAccessory.getPrice());
        assertEquals(Accessory.AccessoryType.WRAPPING_PAPER, simpleAccessory.getType());
        assertEquals("", simpleAccessory.getDescription());
        assertEquals("", simpleAccessory.getImagePath());
        assertEquals(0, simpleAccessory.getStockQuantity());
        assertEquals("", simpleAccessory.getColor());
        assertEquals("", simpleAccessory.getSize());
    }

    @Test
    @DisplayName("Конструктор копіювання")
    void copyConstructor() {
        Accessory copy = new Accessory(accessory);
        assertNotSame(accessory, copy);
        assertEquals(accessory.getName(), copy.getName());
        assertEquals(accessory.getPrice(), copy.getPrice());
        assertEquals(accessory.getDescription(), copy.getDescription());
        assertEquals(accessory.getImagePath(), copy.getImagePath());
        assertEquals(accessory.getStockQuantity(), copy.getStockQuantity());
        assertEquals(accessory.getType(), copy.getType());
        assertEquals(accessory.getColor(), copy.getColor());
        assertEquals(accessory.getSize(), copy.getSize());
    }

    // Тести валідації в конструкторах
    @Test
    @DisplayName("Конструктор з від'ємною ціною - виняток")
    void constructorWithNegativePriceThrowsException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> new Accessory(name, -1.0, description, imagePath, stockQuantity, type, color, size));
        assertEquals("Ціна не може бути від'ємною", exception.getMessage());
    }

    @Test
    @DisplayName("Конструктор з від'ємною кількістю - виняток")
    void constructorWithNegativeStockThrowsException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> new Accessory(name, price, description, imagePath, -1, type, color, size));
        assertEquals("Кількість на складі не може бути від'ємною", exception.getMessage());
    }

    @Test
    @DisplayName("Конструктор з null назвою - виняток")
    void constructorWithNullNameThrowsException() {
        NullPointerException exception = assertThrows(NullPointerException.class,
                () -> new Accessory(null, price, description, imagePath, stockQuantity, type, color, size));
        assertEquals("Назва аксесуара не може бути null", exception.getMessage());
    }

    // Тести сетерів з валідацією
    @Test
    @DisplayName("Сеттер ціни з коректним значенням")
    void setPriceWithValidValue() {
        accessory.setPrice(30.0);
        assertEquals(30.0, accessory.getPrice());
    }

    @Test
    @DisplayName("Сеттер ціни з від'ємним значенням - виняток")
    void setPriceWithNegativeValueThrowsException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> accessory.setPrice(-1.0));
        assertEquals("Ціна не може бути від'ємною", exception.getMessage());
    }

    @Test
    @DisplayName("Сеттер кількості з коректним значенням")
    void setStockQuantityWithValidValue() {
        accessory.setStockQuantity(15);
        assertEquals(15, accessory.getStockQuantity());
    }

    @Test
    @DisplayName("Сеттер кількості з від'ємним значенням - виняток")
    void setStockQuantityWithNegativeValueThrowsException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> accessory.setStockQuantity(-1));
        assertEquals("Кількість на складі не може бути від'ємною", exception.getMessage());
    }

    @Test
    @DisplayName("Сеттер типу з null значенням - виняток")
    void setTypeWithNullValueThrowsException() {
        NullPointerException exception = assertThrows(NullPointerException.class,
                () -> accessory.setType(null));
        assertEquals("Тип аксесуара не може бути null", exception.getMessage());
    }

    // Тести методів бізнес-логіки
    @Test
    @DisplayName("Зменшення запасу з коректною кількістю")
    void decreaseStockWithValidQuantity() {
        accessory.decreaseStock(5);
        assertEquals(5, accessory.getStockQuantity());
    }

    @Test
    @DisplayName("Зменшення запасу з від'ємною кількістю - виняток")
    void decreaseStockWithNegativeQuantityThrowsException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> accessory.decreaseStock(-1));
        assertEquals("Кількість для зменшення не може бути від'ємною", exception.getMessage());
    }

    @Test
    @DisplayName("Зменшення запасу з недостатньою кількістю - виняток")
    void decreaseStockWithInsufficientStockThrowsException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> accessory.decreaseStock(15));
        assertEquals("Недостатньо аксесуарів на складі", exception.getMessage());
    }

    @Test
    @DisplayName("Збільшення запасу з коректною кількістю")
    void increaseStockWithValidQuantity() {
        accessory.increaseStock(5);
        assertEquals(15, accessory.getStockQuantity());
    }

    @Test
    @DisplayName("Збільшення запасу з від'ємною кількістю - виняток")
    void increaseStockWithNegativeQuantityThrowsException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> accessory.increaseStock(-1));
        assertEquals("Кількість для збільшення не може бути від'ємною", exception.getMessage());
    }

    // Тести методів відображення інформації
    @Test
    @DisplayName("Коротка інформація про аксесуар")
    void getShortInfoReturnsCorrectFormat() {
        String expected = "Golden Ribbon (Стрічка) - 25,99 грн";
        assertEquals(expected, accessory.getShortInfo());
    }

    @Test
    @DisplayName("Детальна інформація у HTML форматі")
    void getDetailedInfoReturnsCorrectHTML() {
        String expected = "<html><b>Назва:</b> Golden Ribbon<br>" +
                "<b>Тип:</b> Стрічка<br>" +
                "<b>Ціна:</b> 25,99 грн<br>" +
                "<b>Колір:</b> Gold<br>" +
                "<b>Розмір:</b> 2m<br>" +
                "<b>На складі:</b> 10<br>" +
                "<b>Опис:</b> Beautiful golden ribbon for bouquets</html>";
        assertEquals(expected, accessory.getDetailedInfo());
    }

    @Test
    @DisplayName("Інформація для кошика")
    void getCartInfoReturnsCorrectFormat() {
        String expected = "Golden Ribbon (Gold, 2m) - 25,99 грн";
        assertEquals(expected, accessory.getCartInfo());
    }

    @Test
    @DisplayName("Рядкове представлення об'єкта")
    void toStringReturnsCorrectFormat() {
        String expected = "Аксесуар 'Golden Ribbon' (Стрічка) - 25,99 грн [Gold, 2m]";
        assertEquals(expected, accessory.toString());
    }

    // Тести equals та hashCode
    @Test
    @DisplayName("Equals для одного об'єкта")
    void equalsWithSameObject() {
        assertEquals(accessory, accessory);
    }

    @Test
    @DisplayName("Equals для однакових об'єктів")
    void equalsWithEqualObjects() {
        Accessory sameAccessory = new Accessory(name, price, description, imagePath, stockQuantity, type, color, size);
        assertEquals(accessory, sameAccessory);
    }

    @Test
    @DisplayName("Equals для різних об'єктів")
    void equalsWithDifferentObjects() {
        Accessory differentAccessory = new Accessory("Different", price, description, imagePath, stockQuantity, type, color, size);
        assertNotEquals(accessory, differentAccessory);
    }

    @Test
    @DisplayName("Equals з null об'єктом")
    void equalsWithNullObject() {
        assertNotEquals(accessory, null);
    }

    @Test
    @DisplayName("Equals з об'єктом іншого класу")
    void equalsWithDifferentClassObject() {
        assertNotEquals(accessory, new Object());
    }

    @Test
    @DisplayName("Консистентність hashCode")
    void hashCodeConsistency() {
        int initialHashCode = accessory.hashCode();
        assertEquals(initialHashCode, accessory.hashCode());
    }

    @Test
    @DisplayName("HashCode для однакових об'єктів")
    void hashCodeForEqualObjects() {
        Accessory sameAccessory = new Accessory(name, price, description, imagePath, stockQuantity, type, color, size);
        assertEquals(accessory.hashCode(), sameAccessory.hashCode());
    }

    // Тести для enum AccessoryType
    @Test
    @DisplayName("Enum значення та їх відображувані назви")
    void accessoryTypeEnumValuesAndDisplayNames() {
        assertEquals("Папір для упаковки", Accessory.AccessoryType.WRAPPING_PAPER.getDisplayName());
        assertEquals("Стрічка", Accessory.AccessoryType.RIBBON.getDisplayName());
        assertEquals("Кошик", Accessory.AccessoryType.BASKET.getDisplayName());
        assertEquals("Ваза", Accessory.AccessoryType.VASE.getDisplayName());
        assertEquals("Декоративна сітка", Accessory.AccessoryType.DECORATIVE_MESH.getDisplayName());
        assertEquals("Перлини", Accessory.AccessoryType.PEARLS.getDisplayName());
        assertEquals("Пір'я", Accessory.AccessoryType.FEATHERS.getDisplayName());
        assertEquals("Інше", Accessory.AccessoryType.OTHER.getDisplayName());
    }

    // Тести для сетерів без валідації
    @Test
    @DisplayName("Сеттер назви")
    void setName() {
        accessory.setName("New Name");
        assertEquals("New Name", accessory.getName());
    }

    @Test
    @DisplayName("Сеттер назви з null - виняток")
    void setNameWithNullThrowsException() {
        NullPointerException exception = assertThrows(NullPointerException.class,
                () -> accessory.setName(null));
        assertEquals("Назва аксесуара не може бути null", exception.getMessage());
    }

    @Test
    @DisplayName("Сеттер опису")
    void setDescription() {
        accessory.setDescription("New Description");
        assertEquals("New Description", accessory.getDescription());
    }

    @Test
    @DisplayName("Сеттер опису з null - виняток")
    void setDescriptionWithNullThrowsException() {
        NullPointerException exception = assertThrows(NullPointerException.class,
                () -> accessory.setDescription(null));
        assertEquals("Опис аксесуара не може бути null", exception.getMessage());
    }

    @Test
    @DisplayName("Сеттер шляху до зображення")
    void setImagePath() {
        accessory.setImagePath("new/path.jpg");
        assertEquals("new/path.jpg", accessory.getImagePath());
    }

    @Test
    @DisplayName("Сеттер шляху до зображення з null - виняток")
    void setImagePathWithNullThrowsException() {
        NullPointerException exception = assertThrows(NullPointerException.class,
                () -> accessory.setImagePath(null));
        assertEquals("Шлях до зображення не може бути null", exception.getMessage());
    }

    @Test
    @DisplayName("Сеттер кольору")
    void setColor() {
        accessory.setColor("Silver");
        assertEquals("Silver", accessory.getColor());
    }

    @Test
    @DisplayName("Сеттер кольору з null - виняток")
    void setColorWithNullThrowsException() {
        NullPointerException exception = assertThrows(NullPointerException.class,
                () -> accessory.setColor(null));
        assertEquals("Колір аксесуара не може бути null", exception.getMessage());
    }

    @Test
    @DisplayName("Сеттер розміру")
    void setSize() {
        accessory.setSize("3m");
        assertEquals("3m", accessory.getSize());
    }

    @Test
    @DisplayName("Сеттер розміру з null - виняток")
    void setSizeWithNullThrowsException() {
        NullPointerException exception = assertThrows(NullPointerException.class,
                () -> accessory.setSize(null));
        assertEquals("Розмір аксесуара не може бути null", exception.getMessage());
    }

    // Тест для getId (якщо в майбутньому додасться логіка)
    @Test
    @DisplayName("Отримання ID")
    void getId() {
        assertEquals(0, accessory.getId()); // За замовчуванням 0, якщо не встановлено
    }
}