package flowershop.services;

import flowershop.models.Flower;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.util.Objects;

/**
 * Сервісний клас для управління логікою та операціями, пов'язаними з об'єктом {@link Flower}.
 * Надає методи для маніпулювання запасами квітки, отримання її характеристик
 * та генерації рядкових представлень для відображення.
 * Кожен екземпляр сервісу працює з конкретним екземпляром квітки.
 */
public class FlowerService {

    private static final Logger logger = LogManager.getLogger(FlowerService.class);
    private final Flower flower;

    /**
     * Конструктор для створення сервісу, що працює з конкретною квіткою.
     *
     * @param flower Квітка, для якої буде надаватися сервіс.
     * @throws NullPointerException якщо передана квітка є {@code null}.
     */
    public FlowerService(Flower flower) {
        this.flower = Objects.requireNonNull(flower, "Квітка не може бути null для сервісу");
    }

    /**
     * Повертає відображуване ім'я квітки, що базується на її типі.
     *
     * @return Ім'я типу квітки або "Невідомий тип", якщо тип квітки не встановлено.
     */
    public String getDisplayName() {
        Flower.FlowerType type = flower.getType();
        return type != null ? type.getDisplayName() : "Невідомий тип";
    }

    /**
     * Повертає рівень свіжості квітки як об'єкт {@link Flower.FreshnessLevel}.
     *
     * @return Рівень свіжості квітки.
     */
    public Flower.FreshnessLevel getFreshnessLevel() {
        return Flower.FreshnessLevel.fromValue(flower.getFreshness());
    }

    /**
     * Зменшує кількість квіток на складі для пов'язаної квітки.
     *
     * @param quantity Кількість, на яку потрібно зменшити запаси.
     * @throws IllegalArgumentException Якщо кількість для зменшення від'ємна або
     *                                  перевищує поточну кількість на складі.
     */
    public void decreaseStock(int quantity) {
        if (quantity < 0) {
            throw new IllegalArgumentException("Кількість для зменшення не може бути від'ємною");
        }
        int currentStock = flower.getStockQuantity();
        if (quantity > currentStock) {
            throw new IllegalArgumentException("Недостатньо квіток '" + getDisplayName() + "' на складі для зменшення на " + quantity + ". Поточна кількість: " + currentStock);
        }
        flower.setStockQuantity(currentStock - quantity);
        logger.info("Сервісом зменшено кількість квітки '{}' на {}. Нова кількість: {}", getDisplayName(), quantity, flower.getStockQuantity());
    }

    /**
     * Збільшує кількість квіток на складі для пов'язаної квітки.
     *
     * @param quantity Кількість, на яку потрібно збільшити запаси.
     * @throws IllegalArgumentException Якщо кількість для збільшення від'ємна.
     */
    public void increaseStock(int quantity) {
        if (quantity < 0) {
            throw new IllegalArgumentException("Кількість для збільшення не може бути від'ємною");
        }
        flower.setStockQuantity(flower.getStockQuantity() + quantity);
        logger.info("Сервісом збільшено кількість квітки '{}' на {}. Нова кількість: {}", getDisplayName(), quantity, flower.getStockQuantity());
    }

    /**
     * Повертає коротку інформацію про квітку, включаючи її назву, колір та ціну.
     *
     * @return Рядкове представлення короткої інформації про квітку.
     */
    public String getShortInfo() {
        return String.format("%s (%s) - %.2f грн", getDisplayName(), flower.getColor(), flower.getPrice());
    }

    /**
     * Повертає детальну інформацію про квітку у форматі HTML.
     * Включає назву, колір, ціну, свіжість, довжину стебла, країну походження,
     * ознаку наявності горщика, кількість на складі та згенерований опис.
     *
     * @return Рядкове представлення детальної інформації про квітку у форматі HTML.
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
                getDisplayName(), flower.getColor(), flower.getPrice(),
                getFreshnessLevel().getDescription(), flower.getFreshness(),
                flower.getStemLength(), flower.getCountryOfOrigin(),
                flower.isPotted() ? "Так" : "Ні", flower.getStockQuantity(),
                generateDescription());
    }

    /**
     * Генерує текстовий опис квітки на основі її характеристик.
     *
     * @return Рядковий опис квітки.
     */
    public String generateDescription() {
        return String.format("%s %s кольору з довжиною стебла %d см. %s. Свіжість: %s.",
                getDisplayName(), flower.getColor().toLowerCase(), flower.getStemLength(),
                flower.isPotted() ? "У горщику" : "Зрізана квітка",
                getFreshnessLevel().getDescription().toLowerCase());
    }

    /**
     * Повертає інформацію про квітку, призначену для відображення у кошику.
     * Включає назву, колір та ціну.
     *
     * @return Рядкове представлення інформації про квітку для кошика.
     */
    public String getCartInfo() {
        return String.format("%s (%s) - %.2f грн", getDisplayName(), flower.getColor(), flower.getPrice());
    }

    /**
     * Повертає форматоване рядкове представлення квітки,
     * що включає назву, країну походження, ціну, рівень свіжості, довжину стебла, колір та кількість на складі.
     *
     * @return Рядкове представлення квітки.
     */
    public String getFlowerDisplayString() {
        return String.format("%s (%s) - %.2f грн [Свіжість: %s, Стебло: %d см, Колір: %s, К-сть: %d]",
                getDisplayName(), flower.getCountryOfOrigin(), flower.getPrice(),
                getFreshnessLevel().getDescription(), flower.getStemLength(), flower.getColor(), flower.getStockQuantity());
    }
}