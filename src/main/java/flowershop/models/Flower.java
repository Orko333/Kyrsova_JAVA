package flowershop.models;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Objects;

/**
 * Клас Flower представляє квітку з різними характеристиками,
 * які використовуються для управління товарами в квітковому магазині.
 */
public class Flower {

    private static final Logger logger = LogManager.getLogger(Flower.class);

    public void setId(int i) {
        logger.trace("Встановлення ID {} для квітки типу '{}'", i, this.type != null ? this.type.getDisplayName() : "невизначено");
        this.id = i;
    }

    // Змінено getName(), щоб повертати String, і логувати, якщо тип ще не встановлено
    public String getName() {
        if (type != null) {
            return type.getDisplayName();
        }
        logger.warn("Спроба отримати ім'я для квітки з невизначеним типом.");
        return "Невизначений тип";
    }

    public void setName(String name) {
        logger.trace("Встановлення типу квітки за назвою '{}'", name);
        try {
            this.type = FlowerType.valueOf(name.toUpperCase().replace(" ", "_")); // Додано заміну пробілів, якщо є
        } catch (IllegalArgumentException e) {
            logger.error("Не вдалося встановити тип квітки для назви '{}'. Невідомий тип.", name, e);
            // Можна встановити тип за замовчуванням або залишити null, залежно від логіки
            // this.type = FlowerType.OTHER; // Наприклад
            throw e; // Або прокинути виняток далі
        }
    }

    /**
     * Перелік типів квітів з українськими назвами для відображення.
     */
    public enum FlowerType {
        ROSE("Троянда"),
        LILY("Лілія"),
        TULIP("Тюльпан"),
        CHRYSANTHEMUM("Хризантема"),
        ORCHID("Орхідея"),
        SUNFLOWER("Соняшник"),
        DAISY("Ромашка"),
        PEONY("Півонія"),
        HYDRANGEA("Гортензія"),
        LAVENDER("Лаванда"),
        OTHER("Інше"); // Додано тип для невідомих

        private final String displayName;

        FlowerType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    /**
     * Перелік рівнів свіжості з діапазонами значень і описом.
     */
    public enum FreshnessLevel {
        VERY_HIGH(90, 100, "Дуже висока"),
        HIGH(75, 89, "Висока"),
        MEDIUM(50, 74, "Середня"),
        LOW(25, 49, "Низька"),
        VERY_LOW(0, 24, "Дуже низька");

        private final int minValue;
        private final int maxValue;
        private final String description;

        FreshnessLevel(int minValue, int maxValue, String description) {
            this.minValue = minValue;
            this.maxValue = maxValue;
            this.description = description;
        }

        /**
         * Визначає рівень свіжості за значенням.
         * @param value Значення свіжості (0-100)
         * @return Відповідний рівень свіжості
         */
        public static FreshnessLevel fromValue(int value) {
            for (FreshnessLevel level : values()) {
                if (value >= level.minValue && value <= level.maxValue) {
                    return level;
                }
            }
            logger.warn("Невірне значення свіжості: {}. Повернення VERY_LOW за замовчуванням.", value);
            // throw new IllegalArgumentException("Невірне значення свіжості: " + value); // Або кидати виняток
            return VERY_LOW; // Або повернути значення за замовчуванням
        }

        public String getDescription() {
            return description;
        }
    }

    // --- Поля класу ---
    private FlowerType type;
    private double price;
    private int freshness; // 0-100
    private int stemLength; // в см
    private int id;
    private String color;
    private String countryOfOrigin;
    private boolean isPotted;
    private String imagePath;
    private int stockQuantity; // Кількість на складі або в букеті

    // --- Конструктори ---

    /**
     * Конструктор копіювання.
     * @param flowerToEdit Квітка для копіювання
     */
    public Flower(Flower flowerToEdit) {
        logger.debug("Створення копії квітки: {}", flowerToEdit.getDisplayName());
        this.type = flowerToEdit.type;
        this.price = flowerToEdit.price;
        this.freshness = flowerToEdit.freshness;
        this.stemLength = flowerToEdit.stemLength;
        this.id = flowerToEdit.id;
        this.color = flowerToEdit.color;
        this.countryOfOrigin = flowerToEdit.countryOfOrigin;
        this.isPotted = flowerToEdit.isPotted;
        this.imagePath = flowerToEdit.imagePath;
        this.stockQuantity = flowerToEdit.stockQuantity;
        logger.info("Створено копію квітки '{}'", this.getDisplayName());
    }

    /**
     * Спрощений конструктор з базовими параметрами.
     */
    public Flower(FlowerType type, double price, int freshness, int stemLength) {
        this(type, price, freshness, stemLength, "Без кольору", "Невідомо", false, "", 0);
        logger.debug("Створення квітки '{}' (спрощений конструктор)", type.getDisplayName());
    }

    /**
     * Повний конструктор з усіма параметрами.
     */
    public Flower(FlowerType type, double price, int freshness, int stemLength,
                  String color, String countryOfOrigin, boolean isPotted,
                  String imagePath, int stockQuantity) {
        logger.debug("Спроба створення квітки: Тип='{}', Ціна={}, Свіжість={}, Довжина стебла={}",
                type, price, freshness, stemLength);

        if (price < 0) {
            logger.error("Спроба встановити від'ємну ціну ({}) для квітки типу '{}'", price, type);
            throw new IllegalArgumentException("Ціна квітки не може бути від'ємною");
        }
        if (freshness < 0 || freshness > 100) {
            logger.error("Спроба встановити недійсну свіжість ({}) для квітки типу '{}'", freshness, type);
            throw new IllegalArgumentException("Свіжість має бути в межах 0–100");
        }
        if (stemLength < 0) {
            logger.error("Спроба встановити від'ємну довжину стебла ({}) для квітки типу '{}'", stemLength, type);
            throw new IllegalArgumentException("Довжина стебла не може бути від'ємною");
        }
        if (stockQuantity < 0) {
            logger.error("Спроба встановити від'ємну кількість ({}) на складі для квітки типу '{}'", stockQuantity, type);
            throw new IllegalArgumentException("Кількість на складі не може бути від'ємною");
        }

        this.type = Objects.requireNonNull(type, "Тип квітки не може бути null");
        this.price = price;
        this.freshness = freshness;
        this.stemLength = stemLength;
        this.color = Objects.requireNonNull(color, "Колір не може бути null");
        this.countryOfOrigin = Objects.requireNonNull(countryOfOrigin, "Країна походження не може бути null");
        this.isPotted = isPotted;
        this.imagePath = Objects.requireNonNull(imagePath, "Шлях до зображення не може бути null");
        this.stockQuantity = stockQuantity;
        logger.info("Квітка '{}' ({}) успішно створена.", this.type.getDisplayName(), this.color);
    }

    // --- Гетери ---

    public FlowerType getType() { return type; }
    public double getPrice() { return price; }
    public int getFreshness() { return freshness; }
    public int getStemLength() { return stemLength; }
    public String getColor() { return color; }
    public String getCountryOfOrigin() { return countryOfOrigin; }
    public boolean isPotted() { return isPotted; }
    public String getImagePath() { return imagePath; }
    public int getStockQuantity() { return stockQuantity; }
    public int getId() { return id; }

    public String getDisplayName() {
        return type != null ? type.getDisplayName() : "Невідомий тип";
    }

    public FreshnessLevel getFreshnessLevel() {
        return FreshnessLevel.fromValue(freshness);
    }

    // --- Сетери ---

    public void setType(FlowerType type) {
        logger.trace("Встановлення типу '{}' для квітки (старий тип: '{}')", type, this.type);
        this.type = Objects.requireNonNull(type, "Тип квітки не може бути null");
    }

    public void setPrice(double price) {
        logger.trace("Встановлення ціни {} для квітки '{}' (стара ціна: {})", price, getDisplayName(), this.price);
        if (price < 0) {
            logger.error("Спроба встановити від'ємну ціну ({}) для квітки '{}'", price, getDisplayName());
            throw new IllegalArgumentException("Ціна не може бути від'ємною");
        }
        this.price = price;
    }

    public void setFreshness(int freshness) {
        logger.trace("Встановлення свіжості {} для квітки '{}' (стара свіжість: {})", freshness, getDisplayName(), this.freshness);
        if (freshness < 0 || freshness > 100) {
            logger.error("Спроба встановити недійсну свіжість ({}) для квітки '{}'", freshness, getDisplayName());
            throw new IllegalArgumentException("Свіжість має бути в межах 0–100");
        }
        this.freshness = freshness;
    }

    public void setStemLength(int stemLength) {
        logger.trace("Встановлення довжини стебла {} для квітки '{}' (стара довжина: {})", stemLength, getDisplayName(), this.stemLength);
        if (stemLength < 0) {
            logger.error("Спроба встановити від'ємну довжину стебла ({}) для квітки '{}'", stemLength, getDisplayName());
            throw new IllegalArgumentException("Довжина стебла не може бути від'ємною");
        }
        this.stemLength = stemLength;
    }

    public void setColor(String color) {
        logger.trace("Встановлення кольору '{}' для квітки '{}' (старий колір: '{}')", color, getDisplayName(), this.color);
        this.color = Objects.requireNonNull(color, "Колір не може бути null");
    }

    public void setCountryOfOrigin(String countryOfOrigin) {
        logger.trace("Встановлення країни походження '{}' для квітки '{}' (стара країна: '{}')", countryOfOrigin, getDisplayName(), this.countryOfOrigin);
        this.countryOfOrigin = Objects.requireNonNull(countryOfOrigin, "Країна походження не може бути null");
    }

    public void setPotted(boolean potted) {
        logger.trace("Встановлення ознаки 'в горщику': {} для квітки '{}' (старе значення: {})", potted, getDisplayName(), this.isPotted);
        isPotted = potted;
    }

    public void setImagePath(String imagePath) {
        logger.trace("Встановлення шляху до зображення '{}' для квітки '{}'", imagePath, getDisplayName());
        this.imagePath = Objects.requireNonNull(imagePath, "Шлях до зображення не може бути null");
    }

    public void setStockQuantity(int stockQuantity) {
        logger.trace("Встановлення кількості на складі {} для квітки '{}' (стара кількість: {})", stockQuantity, getDisplayName(), this.stockQuantity);
        if (stockQuantity < 0) {
            logger.error("Спроба встановити від'ємну кількість ({}) на складі для квітки '{}'", stockQuantity, getDisplayName());
            throw new IllegalArgumentException("Кількість на складі не може бути від'ємною");
        }
        this.stockQuantity = stockQuantity;
    }

    // --- Бізнес-логіка ---

    /**
     * Зменшує кількість товару на складі.
     */
    public void decreaseStock(int quantity) {
        logger.debug("Зменшення кількості квітки '{}' на {} (поточна кількість: {})", getDisplayName(), quantity, this.stockQuantity);
        if (quantity < 0) {
            logger.warn("Спроба зменшити кількість на складі на від'ємне значення ({}) для квітки '{}'", quantity, getDisplayName());
            throw new IllegalArgumentException("Кількість для зменшення не може бути від'ємною");
        }
        if (quantity > stockQuantity) {
            logger.error("Недостатня кількість квітів '{}' на складі (запит: {}, наявнo: {})", getDisplayName(), quantity, this.stockQuantity);
            throw new IllegalArgumentException("Недостатньо квітів на складі");
        }
        stockQuantity -= quantity;
        logger.info("Кількість квітки '{}' зменшено на {}. Нова кількість: {}", getDisplayName(), quantity, this.stockQuantity);
    }

    /**
     * Збільшує кількість товару на складі.
     */
    public void increaseStock(int quantity) {
        logger.debug("Збільшення кількості квітки '{}' на {} (поточна кількість: {})", getDisplayName(), quantity, this.stockQuantity);
        if (quantity < 0) {
            logger.warn("Спроба збільшити кількість на складі на від'ємне значення ({}) для квітки '{}'", quantity, getDisplayName());
            throw new IllegalArgumentException("Кількість для збільшення не може бути від'ємною");
        }
        stockQuantity += quantity;
        logger.info("Кількість квітки '{}' збільшено на {}. Нова кількість: {}", getDisplayName(), quantity, this.stockQuantity);
    }

    // --- Методи для відображення у GUI або логіці ---

    public String getShortInfo() {
        return String.format("%s (%s) - %.2f грн", getDisplayName(), color, price);
    }

    public String getDetailedInfo() {
        return String.format(
                "<html><b>Назва:</b> %s<br>" +
                        "<b>Колір:</b> %s<br>" +
                        "<b>Ціна:</b> %.2f грн<br>" +
                        "<b>Свіжість:</b> %s (%d%%)<br>" +
                        "<b>Довжина стебла:</b> %d см<br>" +
                        "<b>Країна походження:</b> %s<br>" +
                        "<b>В горщику:</b> %s<br>" +
                        "<b>На складі:</b> %d<br>" +
                        "<b>Опис:</b> %s</html>",
                getDisplayName(), color, price,
                getFreshnessLevel().getDescription(), freshness,
                stemLength, countryOfOrigin,
                isPotted ? "Так" : "Ні", stockQuantity,
                generateDescription()
        );
    }

    public String generateDescription() {
        return String.format("%s %s кольору з довжиною стебла %d см. %s. Свіжість: %s.",
                getDisplayName(), color.toLowerCase(), stemLength,
                isPotted ? "У горщику" : "Зрізана квітка",
                getFreshnessLevel().getDescription().toLowerCase());
    }

    public String getCartInfo() {
        return String.format("%s (%s) - %.2f грн", getDisplayName(), color, price);
    }

    // --- Перевизначення методів Object ---

    @Override
    public String toString() {
        return String.format("%s (%s) - %.2f грн [Свіжість: %s, Стебло: %dсм, Колір: %s, К-сть: %d]",
                getDisplayName(), countryOfOrigin, price,
                getFreshnessLevel().getDescription(), stemLength, color, stockQuantity);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Flower flower = (Flower) o;
        boolean result = id == flower.id && // Важливо для порівняння об'єктів з БД
                Double.compare(flower.price, price) == 0 &&
                freshness == flower.freshness &&
                stemLength == flower.stemLength &&
                isPotted == flower.isPotted &&
                // stockQuantity не завжди доречно включати в equals, якщо це кількість на складі,
                // а не атрибут, що визначає унікальність самої квітки (наприклад, якщо квітки з різною кількістю вважаються різними об'єктами).
                // Якщо stockQuantity означає кількість саме цього екземпляру (наприклад, в букеті), тоді порівняння потрібне.
                // Для загального випадку, коли equals використовується для ідентифікації типу квітки, stockQuantity краще не включати.
                // stockQuantity == flower.stockQuantity && // Закоментовано, оскільки зазвичай не є частиною визначення унікальності квітки як товару
                type == flower.type &&
                Objects.equals(color, flower.color) &&
                Objects.equals(countryOfOrigin, flower.countryOfOrigin) &&
                Objects.equals(imagePath, flower.imagePath); // imagePath може бути важливим, якщо це частина ідентичності
        logger.trace("Порівняння квітки '{}' з {}: результат {}", this.getDisplayName(), flower.getDisplayName(), result);
        return result;
    }

    @Override
    public int hashCode() {
        // stockQuantity зазвичай не включають в hashCode з тих же причин, що і в equals.
        return Objects.hash(id, type, price, freshness, stemLength, color,
                countryOfOrigin, isPotted, imagePath); // stockQuantity вилучено
    }
}