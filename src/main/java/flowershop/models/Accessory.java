package flowershop.models;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Objects;

/**
 * Клас Accessory моделює аксесуари для букетів у квітковому магазині.
 * Містить інформацію про назву, ціну, тип, опис, колір, розмір, кількість на складі та шлях до зображення.
 */
public class Accessory {

    private static final Logger logger = LogManager.getLogger(Accessory.class);

    // --- Поля класу ---
    private String name;
    private double price;
    private String description;
    private String imagePath;
    private int stockQuantity;
    private int id;
    private AccessoryType type;
    private String color;
    private String size;

    /**
     * Перелік можливих типів аксесуарів.
     */
    public enum AccessoryType {
        WRAPPING_PAPER("Папір для упаковки"),
        RIBBON("Стрічка"),
        BASKET("Кошик"),
        VASE("Ваза"),
        DECORATIVE_MESH("Декоративна сітка"),
        PEARLS("Перлини"),
        FEATHERS("Пір'я"),
        OTHER("Інше"),
        BOW("Бант"),
        BOX("Коробка"),
        DECORATION("Прикраса");

        private final String displayName;

        AccessoryType(String displayName) {
            this.displayName = displayName;
        }

        /**
         * Повертає відображувану назву типу аксесуара.
         *
         * @return Назва типу
         */
        public String getDisplayName() {
            return displayName;
        }
    }

    // --- Конструктори ---

    /**
     * Повний конструктор для створення аксесуара з усіма параметрами.
     *
     * @param name          Назва аксесуара
     * @param price         Ціна аксесуара
     * @param description   Опис аксесуара
     * @param imagePath     Шлях до зображення
     * @param stockQuantity Кількість на складі
     * @param type          Тип аксесуара
     * @param color         Колір аксесуара
     * @param size          Розмір аксесуара
     */
    public Accessory(String name, double price, String description, String imagePath,
                     int stockQuantity, AccessoryType type, String color, String size) {
        validateInputs(price, stockQuantity);
        this.name = Objects.requireNonNull(name, "Назва аксесуара не може бути null");
        this.price = price;
        this.description = Objects.requireNonNull(description, "Опис аксесуара не може бути null");
        this.imagePath = Objects.requireNonNull(imagePath, "Шлях до зображення не може бути null");
        this.stockQuantity = stockQuantity;
        this.type = Objects.requireNonNull(type, "Тип аксесуара не може бути null");
        this.color = Objects.requireNonNull(color, "Колір аксесуара не може бути null");
        this.size = Objects.requireNonNull(size, "Розмір аксесуара не може бути null");
        logger.info("Створено аксесуар '{}'", name);
    }

    /**
     * Спрощений конструктор з базовими параметрами.
     *
     * @param name  Назва аксесуара
     * @param price Ціна аксесуара
     * @param type  Тип аксесуара
     */
    public Accessory(String name, double price, AccessoryType type) {
        this(name, price, "", "", 0, type, "", "");
    }

    /**
     * Конструктор копіювання для створення копії іншого аксесуара.
     *
     * @param accessory Аксесуар для копіювання
     */
    public Accessory(Accessory accessory) {
        this(accessory.name, accessory.price, accessory.description, accessory.imagePath,
                accessory.stockQuantity, accessory.type, accessory.color, accessory.size);
        this.id = accessory.id;
        logger.info("Створено копію аксесуара '{}'", accessory.name);
    }

    // --- Гетери ---

    public String getName() {
        return name;
    }

    public double getPrice() {
        return price;
    }

    public String getDescription() {
        return description;
    }

    public String getImagePath() {
        return imagePath;
    }

    public int getStockQuantity() {
        return stockQuantity;
    }

    public int getId() {
        return id;
    }

    public AccessoryType getType() {
        return type;
    }

    public String getColor() {
        return color;
    }

    public String getSize() {
        return size;
    }

    // --- Сетери ---

    public void setName(String name) {
        this.name = Objects.requireNonNull(name, "Назва аксесуара не може бути null");
    }

    public void setPrice(double price) {
        if (price < 0) {
            throw new IllegalArgumentException("Ціна не може бути від'ємною");
        }
        this.price = price;
    }

    public void setDescription(String description) {
        this.description = Objects.requireNonNull(description, "Опис аксесуара не може бути null");
    }

    public void setImagePath(String imagePath) {
        this.imagePath = Objects.requireNonNull(imagePath, "Шлях до зображення не може бути null");
    }

    public void setStockQuantity(int stockQuantity) {
        if (stockQuantity < 0) {
            throw new IllegalArgumentException("Кількість на складі не може бути від'ємною");
        }
        this.stockQuantity = stockQuantity;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setType(AccessoryType type) {
        this.type = Objects.requireNonNull(type, "Тип аксесуара не може бути null");
    }

    public void setColor(String color) {
        this.color = Objects.requireNonNull(color, "Колір аксесуара не може бути null");
    }

    public void setSize(String size) {
        this.size = Objects.requireNonNull(size, "Розмір аксесуара не може бути null");
    }

    // --- Бізнес-логіка ---

    /**
     * Зменшує кількість аксесуарів на складі.
     *
     * @param quantity Кількість для зменшення
     * @throws IllegalArgumentException Якщо кількість від'ємна або перевищує наявну
     */
    public void decreaseStock(int quantity) {
        if (quantity < 0) {
            throw new IllegalArgumentException("Кількість для зменшення не може бути від'ємною");
        }
        if (quantity > stockQuantity) {
            throw new IllegalArgumentException("Недостатньо аксесуарів на складі");
        }
        stockQuantity -= quantity;
        logger.info("Кількість аксесуара '{}' зменшено на {}. Нова кількість: {}", name, quantity, stockQuantity);
    }

    /**
     * Збільшує кількість аксесуарів на складі.
     *
     * @param quantity Кількість для збільшення
     * @throws IllegalArgumentException Якщо кількість від'ємна
     */
    public void increaseStock(int quantity) {
        if (quantity < 0) {
            throw new IllegalArgumentException("Кількість для збільшення не може бути від'ємною");
        }
        stockQuantity += quantity;
        logger.info("Кількість аксесуара '{}' збільшено на {}. Нова кількість: {}", name, quantity, stockQuantity);
    }

    // --- Методи для відображення ---

    /**
     * Повертає коротку інформацію про аксесуар.
     *
     * @return Рядок з назвою, типом і ціною
     */
    public String getShortInfo() {
        return String.format("%s (%s) - %.2f грн", name, type.getDisplayName(), price);
    }

    /**
     * Повертає детальну інформацію про аксесуар у форматі HTML.
     *
     * @return Рядок з детальною інформацією
     */
    public String getDetailedInfo() {
        return String.format(
                "<html><b>Назва:</b> %s<br>" +
                        "<b>Тип:</b> %s<br>" +
                        "<b>Ціна:</b> %.2f грн<br>" +
                        "<b>Колір:</b> %s<br>" +
                        "<b>Розмір:</b> %s<br>" +
                        "<b>На складі:</b> %d<br>" +
                        "<b>Опис:</b> %s</html>",
                name, type.getDisplayName(), price, color, size, stockQuantity, description);
    }

    /**
     * Повертає інформацію для відображення в кошику.
     *
     * @return Рядок з назвою, кольором, розміром і ціною
     */
    public String getCartInfo() {
        return String.format("%s (%s, %s) - %.2f грн", name, color, size, price);
    }

    // --- Перевизначення методів Object ---

    @Override
    public String toString() {
        return String.format("Аксесуар '%s' (%s) - %.2f грн [%s, %s]",
                name, type.getDisplayName(), price, color, size);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Accessory accessory = (Accessory) o;
        return id == accessory.id &&
                Double.compare(accessory.price, price) == 0 &&
                stockQuantity == accessory.stockQuantity &&
                name.equals(accessory.name) &&
                Objects.equals(description, accessory.description) &&
                Objects.equals(imagePath, accessory.imagePath) &&
                type == accessory.type &&
                Objects.equals(color, accessory.color) &&
                Objects.equals(size, accessory.size);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, price, description, imagePath, stockQuantity, type, color, size);
    }

    // --- Приватні методи ---

    /**
     * Перевіряє коректність вхідних параметрів конструктора.
     *
     * @param price        Ціна
     * @param stockQuantity Кількість на складі
     */
    private void validateInputs(double price, int stockQuantity) {
        if (price < 0) {
            throw new IllegalArgumentException("Ціна не може бути від'ємною");
        }
        if (stockQuantity < 0) {
            throw new IllegalArgumentException("Кількість на складі не може бути від'ємною");
        }
    }
}