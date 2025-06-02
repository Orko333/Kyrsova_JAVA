package flowershop.models;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Клас Bouquet моделює букет, що складається з квітів і аксесуарів.
 * Цей клас є переважно контейнером даних. Уся бізнес-логіка, розрахунки
 * та операції маніпулювання букетом винесені до класу {@link flowershop.services.BouquetService}.
 */
public class Bouquet {

    private static final Logger logger = LogManager.getLogger(Bouquet.class);

    private int id;
    private String name;
    private String description;
    private List<Flower> flowers;
    private List<Accessory> accessories;
    private String imagePath;
    private double discount;

    /**
     * Конструктор за замовчуванням. Створює порожній букет з назвою "Без назви".
     * Ініціалізує порожні списки квітів та аксесуарів.
     */
    public Bouquet() {
        this("Без назви", "", new ArrayList<>(), new ArrayList<>(), "", 0);
        logger.debug("Створено порожній букет за замовчуванням.");
    }

    /**
     * Основний конструктор для створення букета з усіма параметрами.
     *
     * @param name        Назва букета.
     * @param description Опис букета.
     * @param flowers     Список квітів, що входять до букета.
     * @param accessories Список аксесуарів, що входять до букета.
     * @param imagePath   Шлях до зображення букета.
     * @param discount    Знижка на букет у відсотках (від 0 до 100).
     * @throws NullPointerException     якщо назва, опис, списки квітів/аксесуарів або шлях до зображення є {@code null}.
     * @throws IllegalArgumentException якщо знижка виходить за межі діапазону 0-100.
     */
    public Bouquet(String name, String description, List<Flower> flowers,
                   List<Accessory> accessories, String imagePath, double discount) {
        this.id = -1;
        this.name = Objects.requireNonNull(name, "Назва букета не може бути null");
        this.description = Objects.requireNonNull(description, "Опис букета не може бути null");
        this.flowers = new ArrayList<>(Objects.requireNonNull(flowers, "Список квітів не може бути null"));
        this.accessories = new ArrayList<>(Objects.requireNonNull(accessories, "Список аксесуарів не може бути null"));
        this.imagePath = Objects.requireNonNull(imagePath, "Шлях до зображення не може бути null");
        setDiscountInternal(discount); // Використовуємо внутрішній сеттер для валідації
        logger.info("Створено букет '{}'. Квітів: {}, Аксесуарів: {}", name, this.flowers.size(), this.accessories.size());
    }

    /**
     * Конструктор копіювання для створення глибокої копії існуючого букета.
     * Копіюються також списки квітів та аксесуарів.
     *
     * @param otherBouquet Букет, з якого створюється копія.
     * @throws NullPointerException якщо інший букет є {@code null}.
     */
    public Bouquet(Bouquet otherBouquet) {
        Objects.requireNonNull(otherBouquet, "Букет для копіювання не може бути null");
        this.id = otherBouquet.id;
        this.name = otherBouquet.name;
        this.description = otherBouquet.description;
        this.flowers = new ArrayList<>();
        for(Flower flower : otherBouquet.flowers) {
            this.flowers.add(new Flower(flower)); // Створюємо копії квітів
        }
        this.accessories = new ArrayList<>();
        for(Accessory accessory : otherBouquet.accessories) {
            this.accessories.add(new Accessory(accessory)); // Створюємо копії аксесуарів
        }
        this.imagePath = otherBouquet.imagePath;
        this.discount = otherBouquet.discount;
        logger.info("Створено копію букета '{}' (ID: {})", this.name, this.id);
    }


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = Objects.requireNonNull(name, "Назва букета не може бути null");
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = Objects.requireNonNull(description, "Опис букета не може бути null");
    }

    /**
     * Повертає копію списку квітів у букеті для забезпечення інкапсуляції.
     * Зміни, внесені до повернутого списку, не вплинуть на внутрішній стан букета.
     *
     * @return Копія списку квітів.
     */
    public List<Flower> getFlowers() {
        return new ArrayList<>(flowers);
    }

    /**
     * Встановлює новий список квітів для букета.
     * Попередній список квітів очищується, і до нього додаються всі квіти з наданого списку.
     *
     * @param flowers Новий список квітів.
     * @throws NullPointerException якщо наданий список квітів є {@code null}.
     */
    public void setFlowers(List<Flower> flowers) {
        this.flowers = new ArrayList<>(Objects.requireNonNull(flowers, "Список квітів не може бути null при встановленні"));
        logger.debug("Встановлено новий список квітів для букета '{}'. Кількість: {}", name, this.flowers.size());
    }

    /**
     * Повертає копію списку аксесуарів у букеті для забезпечення інкапсуляції.
     * Зміни, внесені до повернутого списку, не вплинуть на внутрішній стан букета.
     *
     * @return Копія списку аксесуарів.
     */
    public List<Accessory> getAccessories() {
        return new ArrayList<>(accessories);
    }

    /**
     * Встановлює новий список аксесуарів для букета.
     * Попередній список аксесуарів очищується, і до нього додаються всі аксесуари з наданого списку.
     *
     * @param accessories Новий список аксесуарів.
     * @throws NullPointerException якщо наданий список аксесуарів є {@code null}.
     */
    public void setAccessories(List<Accessory> accessories) {
        this.accessories = new ArrayList<>(Objects.requireNonNull(accessories, "Список аксесуарів не може бути null при встановленні"));
        logger.debug("Встановлено новий список аксесуарів для букета '{}'. Кількість: {}", name, this.accessories.size());
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = Objects.requireNonNull(imagePath, "Шлях до зображення не може бути null");
    }

    public double getDiscount() {
        return discount;
    }

    /**
     * Встановлює знижку для букета.
     *
     * @param discount Знижка у відсотках (від 0 до 100).
     * @throws IllegalArgumentException Якщо знижка виходить за межі діапазону 0-100.
     */
    public void setDiscount(double discount) {
        setDiscountInternal(discount);
    }

    private void setDiscountInternal(double discount) {
        if (discount < 0 || discount > 100) {
            logger.warn("Спроба встановити недійсну знижку {} для букета '{}'", discount, name);
            throw new IllegalArgumentException("Знижка має бути в межах 0–100%");
        }
        this.discount = discount;
    }

    /**
     * Повертає просте рядкове представлення об'єкта букета, включаючи його ID та назву.
     * Для більш детальної інформації використовуйте методи класу {@link flowershop.services.BouquetService}.
     *
     * @return Рядкове представлення букета.
     */
    @Override
    public String toString() {
        return String.format("Bouquet{id=%d, name='%s'}", id, name);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Bouquet bouquet = (Bouquet) o;
        return id == bouquet.id &&
                Double.compare(bouquet.discount, discount) == 0 &&
                Objects.equals(name, bouquet.name) &&
                Objects.equals(description, bouquet.description) &&
                Objects.equals(flowers, bouquet.flowers) && // Порівняння списків за вмістом та порядком
                Objects.equals(accessories, bouquet.accessories) && // Порівняння списків за вмістом та порядком
                Objects.equals(imagePath, bouquet.imagePath);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, description, flowers, accessories, imagePath, discount);
    }
}