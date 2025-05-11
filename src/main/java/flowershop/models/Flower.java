package flowershop.models;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Objects;

/**
 * Клас Flower моделює квітку з характеристиками для управління товарами в квітковому магазині.
 */
public class Flower {

    private static final Logger logger = LogManager.getLogger(Flower.class);

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
    private int stockQuantity;

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
        OTHER("Інше");

        private final String displayName;

        FlowerType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    /**
     * Перелік рівнів свіжості з діапазонами значень та описом.
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
         * Визначає рівень свіжості за заданим значенням.
         *
         * @param value Значення свіжості (0-100)
         * @return Відповідний рівень свіжості
         */
        public static FreshnessLevel fromValue(int value) {
            for (FreshnessLevel level : values()) {
                if (value >= level.minValue && value <= level.maxValue) {
                    return level;
                }
            }
            logger.warn("Невірне значення свіжості: {}. Повернення VERY_LOW.", value);
            return VERY_LOW;
        }

        public String getDescription() {
            return description;
        }
    }

    // --- Конструктори ---

    /**
     * Повний конструктор для створення квітки з усіма параметрами.
     *
     * @param type            Тип квітки
     * @param price           Ціна квітки
     * @param freshness       Свіжість (0-100)
     * @param stemLength      Довжина стебла в см
     * @param color           Колір квітки
     * @param countryOfOrigin Країна походження
     * @param isPotted        Ознака, чи в горщику
     * @param imagePath       Шлях до зображення
     * @param stockQuantity   Кількість на складі
     */
    public Flower(FlowerType type, double price, int freshness, int stemLength,
                  String color, String countryOfOrigin, boolean isPotted,
                  String imagePath, int stockQuantity) {
        validateInputs(price, freshness, stemLength, stockQuantity);
        this.type = Objects.requireNonNull(type, "Тип квітки не може бути null");
        this.price = price;
        this.freshness = freshness;
        this.stemLength = stemLength;
        this.color = Objects.requireNonNull(color, "Колір не може бути null");
        this.countryOfOrigin = Objects.requireNonNull(countryOfOrigin, "Країна походження не може бути null");
        this.isPotted = isPotted;
        this.imagePath = Objects.requireNonNull(imagePath, "Шлях до зображення не може бути null");
        this.stockQuantity = stockQuantity;
        logger.info("Створено квітку '{}' ({})", type.getDisplayName(), color);
    }

    /**
     * Спрощений конструктор з базовими параметрами.
     *
     * @param type       Тип квітки
     * @param price      Ціна квітки
     * @param freshness  Свіжість (0-100)
     * @param stemLength Довжина стебла в см
     */
    public Flower(FlowerType type, double price, int freshness, int stemLength) {
        this(type, price, freshness, stemLength, "Без кольору", "Невідомо", false, "", 0);
    }

    /**
     * Конструктор копіювання.
     *
     * @param flower dviFlower Квітка для копіювання
     */
    public Flower(Flower flower) {
        this(flower.type, flower.price, flower.freshness, flower.stemLength,
                flower.color, flower.countryOfOrigin, flower.isPotted,
                flower.imagePath, flower.stockQuantity);
        this.id = flower.id;
        logger.info("Створено копію квітки '{}'", flower.getDisplayName());
    }

    // --- Гетери ---

    public FlowerType getType() {
        return type;
    }

    public double getPrice() {
        return price;
    }

    public int getFreshness() {
        return freshness;
    }

    public int getStemLength() {
        return stemLength;
    }

    public int getId() {
        return id;
    }

    public String getColor() {
        return color;
    }

    public String getCountryOfOrigin() {
        return countryOfOrigin;
    }

    public boolean isPotted() {
        return isPotted;
    }

    public String getImagePath() {
        return imagePath;
    }

    public int getStockQuantity() {
        return stockQuantity;
    }

    /**
     * Повертає відображуване ім'я квітки.
     *
     * @return Ім'я типу квітки або "Невідомий тип", якщо тип не встановлено
     */
    public String getDisplayName() {
        return type != null ? type.getDisplayName() : "Невідомий тип";
    }

    /**
     * Повертає рівень свіжості квітки.
     *
     * @return Рівень свіжості
     */
    public FreshnessLevel getFreshnessLevel() {
        return FreshnessLevel.fromValue(freshness);
    }

    // --- Сетери ---

    public void setType(FlowerType type) {
        this.type = Objects.requireNonNull(type, "Тип квітки не може бути null");
    }

    public void setPrice(double price) {
        if (price < 0) {
            throw new IllegalArgumentException("Ціна не може бути від'ємною");
        }
        this.price = price;
    }

    public void setFreshness(int freshness) {
        if (freshness < 0 || freshness > 100) {
            throw new IllegalArgumentException("Свіжість має бути в межах 0–100");
        }
        this.freshness = freshness;
    }

    public void setStemLength(int stemLength) {
        if (stemLength < 0) {
            throw new IllegalArgumentException("Довжина стебла не може бути від'ємною");
        }
        this.stemLength = stemLength;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setColor(String color) {
        this.color = Objects.requireNonNull(color, "Колір не може бути null");
    }

    public void setCountryOfOrigin(String countryOfOrigin) {
        this.countryOfOrigin = Objects.requireNonNull(countryOfOrigin, "Країна походження не може бути null");
    }

    public void setPotted(boolean isPotted) {
        this.isPotted = isPotted;
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

    /**
     * Встановлює тип квітки за назвою.
     *
     * @param name Назва типу квітки
     * @throws IllegalArgumentException Якщо назва не відповідає жодному типу
     */
    public void setName(String name) {
        try {
            this.type = FlowerType.valueOf(name.toUpperCase().replace(" ", "_"));
        } catch (IllegalArgumentException e) {
            logger.error("Невідомий тип квітки: '{}'", name);
            throw e;
        }
    }

    /**
     * Повертає назву квітки.
     *
     * @return Назва типу квітки або "Невизначений тип", якщо тип не встановлено
     */
    public String getName() {
        if (type == null) {
            logger.warn("Тип квітки не встановлено");
            return "Невизначений тип";
        }
        return type.getDisplayName();
    }

    // --- Бізнес-логіка ---

    /**
     * Зменшує кількість квіток на складі.
     *
     * @param quantity Кількість для зменшення
     * @throws IllegalArgumentException Якщо кількість від'ємна або перевищує наявну
     */
    public void decreaseStock(int quantity) {
        if (quantity < 0) {
            throw new IllegalArgumentException("Кількість для зменшення не може бути від'ємною");
        }
        if (quantity > stockQuantity) {
            throw new IllegalArgumentException("Недостатньо квіток на складі");
        }
        stockQuantity -= quantity;
        logger.info("Кількість квітки '{}' зменшено на {}. Нова кількість: {}", getDisplayName(), quantity, stockQuantity);
    }

    /**
     * Збільшує кількість квіток на складі.
     *
     * @param quantity Кількість для збільшення
     * @throws IllegalArgumentException Якщо кількість від'ємна
     */
    public void increaseStock(int quantity) {
        if (quantity < 0) {
            throw new IllegalArgumentException("Кількість для збільшення не може бути від'ємною");
        }
        stockQuantity += quantity;
        logger.info("Кількість квітки '{}' збільшено на {}. Нова кількість: {}", getDisplayName(), quantity, stockQuantity);
    }

    // --- Методи для відображення ---

    /**
     * Повертає короткий опис квітки.
     *
     * @return Рядок з назвою, кольором та ціною
     */
    public String getShortInfo() {
        return String.format("%s (%s) - %.2f грн", getDisplayName(), color, price);
    }

    /**
     * Повертає детальний опис квітки у форматі HTML.
     *
     * @return Рядок з детальною інформацією
     */
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
                generateDescription());
    }

    /**
     * Генерує текстовий опис квітки.
     *
     * @return Рядок з описом квітки
     */
    public String generateDescription() {
        return String.format("%s %s кольору з довжиною стебла %d см. %s. Свіжість: %s.",
                getDisplayName(), color.toLowerCase(), stemLength,
                isPotted ? "У горщику" : "Зрізана квітка",
                getFreshnessLevel().getDescription().toLowerCase());
    }

    /**
     * Повертає інформацію для відображення в кошику.
     *
     * @return Рядок з назвою, кольором та ціною
     */
    public String getCartInfo() {
        return String.format("%s (%s) - %.2f грн", getDisplayName(), color, price);
    }

    // --- Перевизначення методів Object ---

    @Override
    public String toString() {
        return String.format("%s (%s) - %.2f грн [Свіжість: %s, Стебло: %d см, Колір: %s, К-сть: %d]",
                getDisplayName(), countryOfOrigin, price,
                getFreshnessLevel().getDescription(), stemLength, color, stockQuantity);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Flower flower = (Flower) o;
        return id == flower.id &&
                Double.compare(flower.price, price) == 0 &&
                freshness == flower.freshness &&
                stemLength == flower.stemLength &&
                isPotted == flower.isPotted &&
                type == flower.type &&
                Objects.equals(color, flower.color) &&
                Objects.equals(countryOfOrigin, flower.countryOfOrigin) &&
                Objects.equals(imagePath, flower.imagePath);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, type, price, freshness, stemLength, color, countryOfOrigin, isPotted, imagePath);
    }

    // --- Приватні методи ---

    /**
     * Перевіряє коректність вхідних параметрів конструктора.
     *
     * @param price        Ціна
     * @param freshness    Свіжість
     * @param stemLength   Довжина стебла
     * @param stockQuantity Кількість на складі
     */
    private void validateInputs(double price, int freshness, int stemLength, int stockQuantity) {
        if (price < 0) {
            throw new IllegalArgumentException("Ціна не може бути від'ємною");
        }
        if (freshness < 0 || freshness > 100) {
            throw new IllegalArgumentException("Свіжість має бути в межах 0–100");
        }
        if (stemLength < 0) {
            throw new IllegalArgumentException("Довжина стебла не може бути від'ємною");
        }
        if (stockQuantity < 0) {
            throw new IllegalArgumentException("Кількість на складі не може бути від'ємною");
        }
    }
}