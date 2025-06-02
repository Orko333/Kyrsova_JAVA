package flowershop.services;

import flowershop.models.Accessory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.util.Objects;

/**
 * Сервісний клас для управління станом та представленням даних про аксесуари.
 * Надає методи для модифікації запасів аксесуара та генерації рядкових представлень
 * для відображення. Кожен екземпляр сервісу пов'язаний з конкретним об'єктом {@link Accessory}.
 */
public class AccessoryService {

    private static final Logger logger = LogManager.getLogger(AccessoryService.class);
    private final Accessory accessory;

    /**
     * Конструктор для створення сервісу, що працює з конкретним аксесуаром.
     *
     * @param accessory Аксесуар, для якого буде надаватися сервіс.
     * @throws NullPointerException якщо переданий аксесуар є {@code null}.
     */
    public AccessoryService(Accessory accessory) {
        this.accessory = Objects.requireNonNull(accessory, "Аксесуар не може бути null для сервісу");
    }

    /**
     * Зменшує кількість аксесуарів на складі для пов'язаного аксесуара.
     *
     * @param quantity Кількість для зменшення.
     * @throws IllegalArgumentException Якщо кількість від'ємна або перевищує наявну на складі.
     */
    public void decreaseStock(int quantity) {
        if (quantity < 0) {
            throw new IllegalArgumentException("Кількість для зменшення не може бути від'ємною");
        }
        int currentStock = accessory.getStockQuantity();
        if (quantity > currentStock) {
            throw new IllegalArgumentException("Недостатньо аксесуарів (" + accessory.getName() + ") на складі для зменшення на " + quantity + ". Поточна кількість: " + currentStock);
        }
        accessory.setStockQuantity(currentStock - quantity);
        logger.info("Сервісом зменшено кількість аксесуара '{}' на {}. Нова кількість: {}", accessory.getName(), quantity, accessory.getStockQuantity());
    }

    /**
     * Збільшує кількість аксесуарів на складі для пов'язаного аксесуара.
     *
     * @param quantity Кількість для збільшення.
     * @throws IllegalArgumentException Якщо кількість від'ємна.
     */
    public void increaseStock(int quantity) {
        if (quantity < 0) {
            throw new IllegalArgumentException("Кількість для збільшення не може бути від'ємною");
        }
        accessory.setStockQuantity(accessory.getStockQuantity() + quantity);
        logger.info("Сервісом збільшено кількість аксесуара '{}' на {}. Нова кількість: {}", accessory.getName(), quantity, accessory.getStockQuantity());
    }

    /**
     * Повертає коротку інформацію про пов'язаний аксесуар.
     *
     * @return Рядок з назвою, типом і ціною аксесуара.
     */
    public String getShortInfo() {
        return String.format("%s (%s) - %.2f грн", accessory.getName(), accessory.getType().getDisplayName(), accessory.getPrice());
    }

    /**
     * Повертає детальну інформацію про пов'язаний аксесуар у форматі HTML.
     *
     * @return Рядок з детальною інформацією про аксесуар.
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
                accessory.getName(), accessory.getType().getDisplayName(), accessory.getPrice(),
                accessory.getColor(), accessory.getSize(), accessory.getStockQuantity(), accessory.getDescription());
    }

    /**
     * Повертає інформацію про пов'язаний аксесуар для відображення в кошику.
     *
     * @return Рядок з назвою, кольором, розміром і ціною аксесуара.
     */
    public String getCartInfo() {
        return String.format("%s (%s, %s) - %.2f грн", accessory.getName(), accessory.getColor(), accessory.getSize(), accessory.getPrice());
    }
}