package flowershop.models;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Objects;

/**
 * Клас Accessory представляє аксесуари, які можна додати до букетів у квітковому магазині.
 * Містить інформацію про назву, ціну, тип, опис, колір, розмір, кількість на складі та шлях до зображення.
 */
public class Accessory {

    private static final Logger logger = LogManager.getLogger(Accessory.class);

    /**
     * Конструктор копіювання — створює новий об'єкт на основі іншого аксесуара.
     *
     * @param accessoryToEdit Аксесуар, з якого копіюються дані.
     */
    public Accessory(Accessory accessoryToEdit) {
        logger.debug("Створення копії аксесуара: {}", accessoryToEdit.getName());
        this.name = accessoryToEdit.name;
        this.price = accessoryToEdit.price;
        this.description = accessoryToEdit.description;
        this.imagePath = accessoryToEdit.imagePath;
        this.stockQuantity = accessoryToEdit.stockQuantity;
        this.type = accessoryToEdit.type;
        this.color = accessoryToEdit.color;
        this.size = accessoryToEdit.size;
        this.id = accessoryToEdit.id; // Додано копіювання id
        logger.info("Створено копію аксесуара '{}'", this.name);
    }

    public void setId(int i) {
        logger.trace("Встановлення ID {} для аксесуара '{}'", i, this.name);
        this.id = i;
    }

    /**
     * Перелічення можливих типів аксесуарів.
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
         * Повертає відображувану назву типу.
         *
         * @return Назва типу аксесуара.
         */
        public String getDisplayName() {
            return displayName;
        }
    }

    // Поля класу
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
     * Спрощений конструктор з мінімальними параметрами.
     *
     * @param name Назва аксесуара.
     * @param price Ціна аксесуара.
     * @param type Тип аксесуара.
     */
    public Accessory(String name, double price, AccessoryType type) {
        this(name, price, "", "", 0, type, "", "");
        logger.debug("Створення аксесуара '{}' (спрощений конструктор)", name);
    }

    /**
     * Повний конструктор аксесуара з усіма параметрами.
     *
     * @param name Назва аксесуара.
     * @param price Ціна.
     * @param description Опис.
     * @param imagePath Шлях до зображення.
     * @param stockQuantity Кількість на складі.
     * @param type Тип аксесуара.
     * @param color Колір.
     * @param size Розмір.
     */
    public Accessory(String name, double price, String description, String imagePath,
                     int stockQuantity, AccessoryType type, String color, String size) {
        logger.debug("Спроба створення аксесуара: Ім'я='{}', Ціна={}, Тип='{}'", name, price, type);
        if (price < 0) {
            logger.error("Спроба встановити від'ємну ціну ({}) для аксесуара '{}'", price, name);
            throw new IllegalArgumentException("Ціна аксесуара не може бути від'ємною");
        }
        if (stockQuantity < 0) {
            logger.error("Спроба встановити від'ємну кількість ({}) на складі для аксесуара '{}'", stockQuantity, name);
            throw new IllegalArgumentException("Кількість на складі не може бути від'ємною");
        }

        this.name = Objects.requireNonNull(name, "Назва аксесуара не може бути null");
        this.price = price;
        this.description = Objects.requireNonNull(description, "Опис аксесуара не може бути null");
        this.imagePath = Objects.requireNonNull(imagePath, "Шлях до зображення не може бути null");
        this.stockQuantity = stockQuantity;
        this.type = Objects.requireNonNull(type, "Тип аксесуара не може бути null");
        this.color = Objects.requireNonNull(color, "Колір аксесуара не може бути null");
        this.size = Objects.requireNonNull(size, "Розмір аксесуара не може бути null");
        logger.info("Аксесуар '{}' успішно створено.", this.name);
    }

    // Гетери і сетери з перевірками

    public String getName() {
        return name;
    }

    public void setName(String name) {
        logger.trace("Встановлення назви '{}' для аксесуара (стара назва: '{}')", name, this.name);
        this.name = Objects.requireNonNull(name, "Назва аксесуара не може бути null");
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        logger.trace("Встановлення ціни {} для аксесуара '{}' (стара ціна: {})", price, this.name, this.price);
        if (price < 0) {
            logger.error("Спроба встановити від'ємну ціну ({}) для аксесуара '{}'", price, this.name);
            throw new IllegalArgumentException("Ціна аксесуара не може бути від'ємною");
        }
        this.price = price;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        logger.trace("Встановлення опису для аксесуара '{}'", this.name);
        this.description = Objects.requireNonNull(description, "Опис аксесуара не може бути null");
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        logger.trace("Встановлення шляху до зображення '{}' для аксесуара '{}'", imagePath, this.name);
        this.imagePath = Objects.requireNonNull(imagePath, "Шлях до зображення не може бути null");
    }

    public int getStockQuantity() {
        return stockQuantity;
    }

    public void setStockQuantity(int stockQuantity) {
        logger.trace("Встановлення кількості на складі {} для аксесуара '{}' (стара кількість: {})", stockQuantity, this.name, this.stockQuantity);
        if (stockQuantity < 0) {
            logger.error("Спроба встановити від'ємну кількість ({}) на складі для аксесуара '{}'", stockQuantity, this.name);
            throw new IllegalArgumentException("Кількість на складі не може бути від'ємною");
        }
        this.stockQuantity = stockQuantity;
    }

    public AccessoryType getType() {
        return type;
    }

    public void setType(AccessoryType type) {
        logger.trace("Встановлення типу '{}' для аксесуара '{}' (старий тип: '{}')", type, this.name, this.type);
        this.type = Objects.requireNonNull(type, "Тип аксесуара не може бути null");
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        logger.trace("Встановлення кольору '{}' для аксесуара '{}' (старий колір: '{}')", color, this.name, this.color);
        this.color = Objects.requireNonNull(color, "Колір аксесуара не може бути null");
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        logger.trace("Встановлення розміру '{}' для аксесуара '{}' (старий розмір: '{}')", size, this.name, this.size);
        this.size = Objects.requireNonNull(size, "Розмір аксесуара не може бути null");
    }

    /**
     * Зменшує кількість товару на складі.
     *
     * @param quantity Кількість для зменшення.
     */
    public void decreaseStock(int quantity) {
        logger.debug("Зменшення кількості аксесуара '{}' на {} (поточна кількість: {})", this.name, quantity, this.stockQuantity);
        if (quantity < 0) {
            logger.warn("Спроба зменшити кількість на складі на від'ємне значення ({}) для аксесуара '{}'", quantity, this.name);
            throw new IllegalArgumentException("Кількість для зменшення не може бути від'ємною");
        }
        if (quantity > stockQuantity) {
            logger.error("Недостатня кількість аксесуарів '{}' на складі (запит: {}, наявнo: {})", this.name, quantity, this.stockQuantity);
            throw new IllegalArgumentException("Недостатня кількість аксесуарів на складі");
        }
        stockQuantity -= quantity;
        logger.info("Кількість аксесуара '{}' зменшено на {}. Нова кількість: {}", this.name, quantity, this.stockQuantity);
    }

    /**
     * Збільшує кількість товару на складі.
     *
     * @param quantity Кількість для збільшення.
     */
    public void increaseStock(int quantity) {
        logger.debug("Збільшення кількості аксесуара '{}' на {} (поточна кількість: {})", this.name, quantity, this.stockQuantity);
        if (quantity < 0) {
            logger.warn("Спроба збільшити кількість на складі на від'ємне значення ({}) для аксесуара '{}'", quantity, this.name);
            throw new IllegalArgumentException("Кількість для збільшення не може бути від'ємною");
        }
        stockQuantity += quantity;
        logger.info("Кількість аксесуара '{}' збільшено на {}. Нова кількість: {}", this.name, quantity, this.stockQuantity);
    }

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
        boolean result = Double.compare(accessory.price, price) == 0 &&
                stockQuantity == accessory.stockQuantity &&
                id == accessory.id && // Додано порівняння id
                name.equals(accessory.name) &&
                Objects.equals(description, accessory.description) && // Додано Objects.equals для можливості null
                Objects.equals(imagePath, accessory.imagePath) && // Додано Objects.equals
                type == accessory.type &&
                Objects.equals(color, accessory.color) && // Додано Objects.equals
                Objects.equals(size, accessory.size); // Додано Objects.equals
        logger.trace("Порівняння аксесуара '{}' з {}: результат {}", this.name, accessory.getName(), result);
        return result;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, price, description, imagePath, stockQuantity, type, color, size, id); // Додано id
    }

    /**
     * Повертає коротку інформацію для списків.
     *
     * @return Короткий опис аксесуара.
     */
    public String getShortInfo() {
        return String.format("%s (%s) - %.2f грн", name, type.getDisplayName(), price);
    }

    /**
     * Повертає повну інформацію про аксесуар у форматі HTML.
     *
     * @return HTML-рядок із детальною інформацією.
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
                name, type.getDisplayName(), price, color, size, stockQuantity, description
        );
    }

    /**
     * Повертає короткий рядок для відображення в кошику замовлення.
     *
     * @return Рядок з інформацією для кошика.
     */
    public String getCartInfo() {
        return String.format("%s (%s, %s) - %.2f грн", name, color, size, price);
    }

    /**
     * Повертає ідентифікатор аксесуара.
     *
     * @return id аксесуара.
     */
    public int getId() {
        return id;
    }
}