package flowershop.models;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Objects;

/**
 * Клас Flower моделює квітку з її основними характеристиками.
 * Цей клас є переважно контейнером даних. Уся бізнес-логіка, операції
 * маніпулювання запасами та генерація рядкових представлень квітки
 * винесені до класу {@link flowershop.services.FlowerService}.
 */
public class Flower {

    private static final Logger logger = LogManager.getLogger(Flower.class);

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
     * Перелік можливих типів квітів з їх відображуваними назвами.
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

        /**
         * Повертає відображувану назву типу квітки.
         *
         * @return Назва типу квітки.
         */
        public String getDisplayName() {
            return displayName;
        }
    }

    /**
     * Перелік рівнів свіжості квітки, що визначаються діапазоном значень
     * та мають відповідний текстовий опис.
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
         * Визначає та повертає рівень свіжості на основі заданого числового значення.
         *
         * @param value Значення свіжості (в діапазоні 0-100).
         * @return Відповідний об'єкт {@link FreshnessLevel}. Якщо значення виходить за межі
         *         визначених рівнів, повертає {@code VERY_LOW}.
         */
        public static FreshnessLevel fromValue(int value) {
            for (FreshnessLevel level : values()) {
                if (value >= level.minValue && value <= level.maxValue) {
                    return level;
                }
            }
            logger.warn("Невірне значення свіжості: {}. Повернення VERY_LOW.", value);
            return VERY_LOW; // Або можна кидати IllegalArgumentException
        }

        /**
         * Повертає текстовий опис рівня свіжості.
         *
         * @return Опис рівня свіжості.
         */
        public String getDescription() {
            return description;
        }
    }

    /**
     * Повний конструктор для створення екземпляра квітки з усіма параметрами.
     *
     * @param type            Тип квітки.
     * @param price           Ціна квітки.
     * @param freshness       Рівень свіжості квітки (0-100).
     * @param stemLength      Довжина стебла квітки в сантиметрах.
     * @param color           Колір квітки.
     * @param countryOfOrigin Країна походження квітки.
     * @param isPotted        Прапорець, що вказує, чи квітка в горщику.
     * @param imagePath       Шлях до файлу зображення квітки.
     * @param stockQuantity   Кількість даної квітки на складі.
     * @throws NullPointerException     якщо тип, колір, країна походження або шлях до зображення є {@code null}.
     * @throws IllegalArgumentException якщо ціна, свіжість, довжина стебла або кількість на складі мають недійсні значення.
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
        logger.info("Створено квітку '{}' ({}), ID: {}", this.type.getDisplayName(), this.color, this.id);
    }

    /**
     * Спрощений конструктор для створення екземпляра квітки з базовими параметрами.
     * Інші поля отримують значення за замовчуванням.
     *
     * @param type       Тип квітки.
     * @param price      Ціна квітки.
     * @param freshness  Рівень свіжості квітки (0-100).
     * @param stemLength Довжина стебла квітки в сантиметрах.
     */
    public Flower(FlowerType type, double price, int freshness, int stemLength) {
        this(type, price, freshness, stemLength, "Без кольору", "Невідомо", false, "", 0);
    }

    /**
     * Конструктор копіювання для створення нового екземпляра квітки на основі існуючого.
     * Створює глибоку копію всіх полів.
     *
     * @param otherFlower Квітка, з якої створюється копія.
     * @throws NullPointerException якщо {@code otherFlower} є {@code null}.
     */
    public Flower(Flower otherFlower) {
        Objects.requireNonNull(otherFlower, "Квітка для копіювання не може бути null");
        this.type = otherFlower.type;
        this.price = otherFlower.price;
        this.freshness = otherFlower.freshness;
        this.stemLength = otherFlower.stemLength;
        this.id = otherFlower.id;
        this.color = otherFlower.color;
        this.countryOfOrigin = otherFlower.countryOfOrigin;
        this.isPotted = otherFlower.isPotted;
        this.imagePath = otherFlower.imagePath;
        this.stockQuantity = otherFlower.stockQuantity;
        logger.info("Створено копію квітки '{}' (ID: {})", this.type != null ? this.type.getDisplayName() : "Невідомий тип", this.id);
    }

    public FlowerType getType() {
        return type;
    }

    public void setType(FlowerType type) {
        this.type = Objects.requireNonNull(type, "Тип квітки не може бути null при встановленні");
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        if (price < 0) {
            throw new IllegalArgumentException("Ціна не може бути від'ємною");
        }
        this.price = price;
    }

    public int getFreshness() {
        return freshness;
    }

    public void setFreshness(int freshness) {
        if (freshness < 0 || freshness > 100) {
            throw new IllegalArgumentException("Свіжість має бути в межах 0–100");
        }
        this.freshness = freshness;
    }

    public int getStemLength() {
        return stemLength;
    }

    public void setStemLength(int stemLength) {
        if (stemLength < 0) {
            throw new IllegalArgumentException("Довжина стебла не може бути від'ємною");
        }
        this.stemLength = stemLength;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = Objects.requireNonNull(color, "Колір не може бути null при встановленні");
    }

    public String getCountryOfOrigin() {
        return countryOfOrigin;
    }

    public void setCountryOfOrigin(String countryOfOrigin) {
        this.countryOfOrigin = Objects.requireNonNull(countryOfOrigin, "Країна походження не може бути null при встановленні");
    }

    public boolean isPotted() {
        return isPotted;
    }

    public void setPotted(boolean potted) {
        isPotted = potted;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = Objects.requireNonNull(imagePath, "Шлях до зображення не може бути null при встановленні");
    }

    public int getStockQuantity() {
        return stockQuantity;
    }

    public void setStockQuantity(int stockQuantity) {
        if (stockQuantity < 0) {
            throw new IllegalArgumentException("Кількість на складі не може бути від'ємною");
        }
        this.stockQuantity = stockQuantity;
    }

    /**
     * Встановлює тип квітки на основі рядкової назви.
     * Назва типу перетворюється у верхній регістр, а пробіли замінюються на підкреслення
     * для відповідності іменам констант в {@link FlowerType}.
     *
     * @param name Назва типу квітки.
     * @throws IllegalArgumentException Якщо назва не відповідає жодному з відомих типів квітів.
     * @throws NullPointerException     якщо {@code name} є {@code null}.
     */
    public void setName(String name) {
        Objects.requireNonNull(name, "Назва для встановлення типу квітки не може бути null");
        try {
            this.type = FlowerType.valueOf(name.toUpperCase().replace(" ", "_"));
        } catch (IllegalArgumentException e) {
            logger.error("Спроба встановити невідомий тип квітки за назвою: '{}'", name, e);
            throw new IllegalArgumentException("Невідомий тип квітки: " + name, e);
        }
    }

    /**
     * Повертає назву квітки, що базується на її типі.
     * Якщо тип квітки не встановлено, повертає "Невизначений тип".
     *
     * @return Назва квітки.
     */
    public String getName() {
        if (type == null) {
            logger.warn("Спроба отримати назву для квітки без встановленого типу (ID: {})", this.id);
            return "Невизначений тип";
        }
        return type.getDisplayName();
    }

    /**
     * Повертає просте рядкове представлення об'єкта квітки, включаючи її ID та назву.
     * Для більш детальної інформації використовуйте методи класу {@link flowershop.services.FlowerService}.
     *
     * @return Рядкове представлення квітки.
     */
    @Override
    public String toString() {
        String displayName = (type != null) ? type.getDisplayName() : "Невідомий тип";
        return String.format("Flower{id=%d, type=%s, color='%s'}", id, displayName, color);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Flower flower = (Flower) o;
        // Для порівняння сутностей часто достатньо порівняти ID, якщо він унікальний та встановлений.
        // Якщо ID ще не встановлено (наприклад, для новостворених об'єктів), порівнюються інші значущі поля.
        if (id != 0 && flower.id != 0) { // Якщо ID встановлені для обох
            return id == flower.id;
        }
        // Якщо ID не встановлені або встановлені не для обох, порівнюємо за іншими полями
        return Double.compare(flower.price, price) == 0 &&
                freshness == flower.freshness &&
                stemLength == flower.stemLength &&
                isPotted == flower.isPotted &&
                // stockQuantity не включаємо в equals, бо це змінний стан, а не ідентифікатор сутності
                type == flower.type &&
                Objects.equals(color, flower.color) &&
                Objects.equals(countryOfOrigin, flower.countryOfOrigin) &&
                Objects.equals(imagePath, flower.imagePath); // imagePath може бути важливим для ідентифікації
    }

    @Override
    public int hashCode() {
        // Якщо ID унікальний та встановлений, можна базувати хеш-код переважно на ньому.
        if (id != 0) {
            return Objects.hash(id);
        }
        // Інакше, використовуємо інші поля
        return Objects.hash(type, price, freshness, stemLength, color, countryOfOrigin, isPotted, imagePath);
    }

    /**
     * Приватний метод для валідації вхідних числових параметрів під час створення або модифікації квітки.
     *
     * @param price         Ціна квітки.
     * @param freshness     Рівень свіжості квітки.
     * @param stemLength    Довжина стебла квітки.
     * @param stockQuantity Кількість квіток на складі.
     * @throws IllegalArgumentException Якщо будь-який з параметрів має недійсне значення.
     */
    private void validateInputs(double price, int freshness, int stemLength, int stockQuantity) {
        if (price < 0) {
            throw new IllegalArgumentException("Ціна не може бути від'ємною: " + price);
        }
        if (freshness < 0 || freshness > 100) {
            throw new IllegalArgumentException("Свіжість має бути в межах 0–100: " + freshness);
        }
        if (stemLength < 0) {
            throw new IllegalArgumentException("Довжина стебла не може бути від'ємною: " + stemLength);
        }
        if (stockQuantity < 0) {
            throw new IllegalArgumentException("Кількість на складі не може бути від'ємною: " + stockQuantity);
        }
    }
}