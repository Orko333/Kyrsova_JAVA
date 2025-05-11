package flowershop.models;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Клас Bouquet моделює букет, що складається з квітів і аксесуарів.
 * Містить назву, опис, знижку, списки квітів та аксесуарів, а також шлях до зображення.
 */
public class Bouquet {

    private static final Logger logger = LogManager.getLogger(Bouquet.class);

    private int id;
    private String name;
    private String description;
    private final List<Flower> flowers;
    private final List<Accessory> accessories;
    private String imagePath;
    private double discount;

    // --- Конструктори ---

    /**
     * Конструктор за замовчуванням. Створює порожній букет з назвою "Без назви".
     */
    public Bouquet() {
        this("Без назви", "", new ArrayList<>(), new ArrayList<>(), "", 0);
    }

    /**
     * Основний конструктор для створення букета з усіма параметрами.
     *
     * @param name        Назва букета
     * @param description Опис букета
     * @param flowers     Список квітів
     * @param accessories Список аксесуарів
     * @param imagePath   Шлях до зображення
     * @param discount    Знижка у відсотках (0-100)
     */
    public Bouquet(String name, String description, List<Flower> flowers,
                   List<Accessory> accessories, String imagePath, double discount) {
        this.id = -1; // Неконтрольоване значення, буде встановлено БД
        this.name = Objects.requireNonNull(name, "Назва букета не може бути null");
        this.description = Objects.requireNonNull(description, "Опис букета не може бути null");
        this.flowers = new ArrayList<>(Objects.requireNonNull(flowers, "Список квітів не може бути null"));
        this.accessories = new ArrayList<>(Objects.requireNonNull(accessories, "Список аксесуарів не може бути null"));
        this.imagePath = Objects.requireNonNull(imagePath, "Шлях до зображення не може бути null");
        setDiscount(discount);
        logger.info("Створено букет '{}'. Квітів: {}, Аксесуарів: {}", name, flowers.size(), accessories.size());
    }

    /**
     * Конструктор копіювання для створення копії існуючого букета.
     *
     * @param bouquet Букет для копіювання
     */
    public Bouquet(Bouquet bouquet) {
        this(bouquet.name, bouquet.description, bouquet.flowers, bouquet.accessories,
                bouquet.imagePath, bouquet.discount);
        this.id = bouquet.id;
        logger.info("Створено копію букета '{}'", bouquet.name);
    }

    // --- Гетери ---

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Повертає копію списку квітів для інкапсуляції.
     *
     * @return Список квітів
     */
    public List<Flower> getFlowers() {
        return new ArrayList<>(flowers);
    }

    /**
     * Повертає копію списку аксесуарів для інкапсуляції.
     *
     * @return Список аксесуарів
     */
    public List<Accessory> getAccessories() {
        return new ArrayList<>(accessories);
    }

    public String getImagePath() {
        return imagePath;
    }

    public double getDiscount() {
        return discount;
    }

    // --- Сетери ---

    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = Objects.requireNonNull(name, "Назва букета не може бути null");
    }

    public void setDescription(String description) {
        this.description = Objects.requireNonNull(description, "Опис букета не може бути null");
    }

    public void setImagePath(String imagePath) {
        this.imagePath = Objects.requireNonNull(imagePath, "Шлях до зображення не може бути null");
    }

    /**
     * Встановлює знижку для букета.
     *
     * @param discount Знижка у відсотках (0-100)
     * @throws IllegalArgumentException Якщо знижка поза діапазоном
     */
    public void setDiscount(double discount) {
        if (discount < 0 || discount > 100) {
            throw new IllegalArgumentException("Знижка має бути в межах 0–100%");
        }
        this.discount = discount;
    }

    /**
     * Встановлює новий список квітів.
     *
     * @param flowers Список квітів
     */
    public void setFlowers(List<Flower> flowers) {
        this.flowers.clear();
        this.flowers.addAll(Objects.requireNonNull(flowers, "Список квітів не може бути null"));
        logger.info("Встановлено новий список квітів для букета '{}'. Кількість: {}", name, flowers.size());
    }

    /**
     * Встановлює новий список аксесуарів.
     *
     * @param accessories Список аксесуарів
     */
    public void setAccessories(List<Accessory> accessories) {
        this.accessories.clear();
        this.accessories.addAll(Objects.requireNonNull(accessories, "Список аксесуарів не може бути null"));
        logger.info("Встановлено новий список аксесуарів для букета '{}'. Кількість: {}", name, accessories.size());
    }

    // --- Методи для роботи з квітами ---

    /**
     * Додає одну квітку до букета.
     *
     * @param flower Квітка для додавання
     */
    public void addFlower(Flower flower) {
        flowers.add(Objects.requireNonNull(flower, "Квітка не може бути null"));
        logger.info("Додано квітку '{}' до букета '{}'. Всього квітів: {}", flower.getDisplayName(), name, flowers.size());
    }

    /**
     * Додає список квітів до букета.
     *
     * @param flowersToAdd Список квітів для додавання
     */
    public void addFlowers(List<Flower> flowersToAdd) {
        Objects.requireNonNull(flowersToAdd, "Список квітів не може бути null");
        if (flowersToAdd.isEmpty()) {
            return;
        }
        flowers.addAll(flowersToAdd);
        logger.info("Додано {} квіт(ів) до букета '{}'. Всього квітів: {}", flowersToAdd.size(), name, flowers.size());
    }

    /**
     * Видаляє конкретну квітку з букета.
     *
     * @param flower Квітка для видалення
     * @return true, якщо квітка була видалена, false — якщо не знайдена
     */
    public boolean removeFlower(Flower flower) {
        boolean removed = flowers.remove(Objects.requireNonNull(flower, "Квітка не може бути null"));
        if (removed) {
            logger.info("Видалено квітку '{}' з букета '{}'. Всього квітів: {}", flower.getDisplayName(), name, flowers.size());
        }
        return removed;
    }

    /**
     * Видаляє квітку за індексом.
     *
     * @param index Індекс квітки
     * @return Видалена квітка
     * @throws IndexOutOfBoundsException Якщо індекс недійсний
     */
    public Flower removeFlower(int index) {
        if (index < 0 || index >= flowers.size()) {
            throw new IndexOutOfBoundsException("Невірний індекс квітки");
        }
        Flower removedFlower = flowers.remove(index);
        logger.info("Видалено квітку '{}' за індексом {} з букета '{}'", removedFlower.getDisplayName(), index, name);
        return removedFlower;
    }

    /**
     * Встановлює кількості квітів у букеті на основі мапи.
     * Кожна квітка додається до букета з відповідною кількістю (stockQuantity).
     *
     * @param flowerQuantities Мапа квіток та їх кількостей
     */
    public void setFlowerQuantities(HashMap<Flower, Integer> flowerQuantities) {
        Objects.requireNonNull(flowerQuantities, "Мапа кількостей квітів не може бути null");
        flowers.clear();
        for (Map.Entry<Flower, Integer> entry : flowerQuantities.entrySet()) {
            Flower flower = entry.getKey();
            Integer quantity = entry.getValue();
            if (flower == null || quantity == null || quantity < 0) {
                logger.warn("Пропущено недійсний запис: Квітка={}, Кількість={}", flower, quantity);
                continue;
            }
            Flower flowerCopy = new Flower(flower); // Створюємо копію, щоб не змінювати оригінал
            flowerCopy.setStockQuantity(quantity);
            flowers.add(flowerCopy);
        }
        logger.info("Встановлено кількості квітів для букета '{}'. Квітів: {}", name, flowers.size());
    }

    /**
     * Повертає кількість певної квітки в букеті.
     *
     * @param flower Квітка для перевірки
     * @return Кількість квіток (stockQuantity) або 0, якщо не знайдено
     */
    public int getFlowerQuantity(Flower flower) {
        return flowers.stream()
                .filter(f -> f.equals(flower))
                .findFirst()
                .map(Flower::getStockQuantity)
                .orElse(0);
    }

    /**
     * Повертає загальну кількість квітів у букеті (враховує stockQuantity).
     *
     * @return Загальна кількість квітів
     */
    public int getTotalFlowerCount() {
        int total = flowers.stream().mapToInt(Flower::getStockQuantity).sum();
        logger.trace("Загальна кількість квітів у букеті '{}': {}", name, total);
        return total;
    }

    // --- Методи для роботи з аксесуарами ---

    /**
     * Додає один аксесуар до букета.
     *
     * @param accessory Аксесуар для додавання
     */
    public void addAccessory(Accessory accessory) {
        accessories.add(Objects.requireNonNull(accessory, "Аксесуар не може бути null"));
        logger.info("Додано аксесуар '{}' до букета '{}'. Всього аксесуарів: {}", accessory.getName(), name, accessories.size());
    }

    /**
     * Додає список аксесуарів до букета.
     *
     * @param accessoriesToAdd Список аксесуарів для додавання
     */
    public void addAccessories(List<Accessory> accessoriesToAdd) {
        Objects.requireNonNull(accessoriesToAdd, "Список аксесуарів не може бути null");
        if (accessoriesToAdd.isEmpty()) {
            return;
        }
        accessories.addAll(accessoriesToAdd);
        logger.info("Додано {} аксесуар(ів) до букета '{}'. Всього аксесуарів: {}", accessoriesToAdd.size(), name, accessories.size());
    }

    /**
     * Видаляє конкретний аксесуар з букета.
     *
     * @param accessory Аксесуар для видалення
     * @return true, якщо аксесуар був видалений, false — якщо не знайдений
     */
    public boolean removeAccessory(Accessory accessory) {
        boolean removed = accessories.remove(Objects.requireNonNull(accessory, "Аксесуар не може бути null"));
        if (removed) {
            logger.info("Видалено аксесуар '{}' з букета '{}'. Всього аксесуарів: {}", accessory.getName(), name, accessories.size());
        }
        return removed;
    }

    /**
     * Видаляє аксесуар за індексом.
     *
     * @param index Індекс аксесуара
     * @return Видалений аксесуар
     * @throws IndexOutOfBoundsException Якщо індекс недійсний
     */
    public Accessory removeAccessory(int index) {
        if (index < 0 || index >= accessories.size()) {
            throw new IndexOutOfBoundsException("Невірний індекс аксесуара");
        }
        Accessory removedAccessory = accessories.remove(index);
        logger.info("Видалено аксесуар '{}' за індексом {} з букета '{}'", removedAccessory.getName(), index, name);
        return removedAccessory;
    }

    // --- Методи для очищення ---

    /**
     * Очищає список квітів у букеті.
     */
    public void clearFlowers() {
        if (!flowers.isEmpty()) {
            logger.info("Очищено квіти з букета '{}'. Було квітів: {}", name, flowers.size());
            flowers.clear();
        }
    }

    /**
     * Очищає список аксесуарів у букеті.
     */
    public void clearAccessories() {
        if (!accessories.isEmpty()) {
            logger.info("Очищено аксесуари з букета '{}'. Було аксесуарів: {}", name, accessories.size());
            accessories.clear();
        }
    }

    /**
     * Повністю очищає букет (квіти та аксесуари).
     */
    public void clear() {
        clearFlowers();
        clearAccessories();
        logger.info("Букет '{}' повністю очищено", name);
    }

    // --- Розрахункові методи ---

    /**
     * Обчислює загальну вартість букета без знижки.
     *
     * @return Загальна вартість
     */
    public double calculateTotalPrice() {
        double flowersPrice = flowers.stream().mapToDouble(f -> f.getPrice() * f.getStockQuantity()).sum();
        double accessoriesPrice = accessories.stream().mapToDouble(Accessory::getPrice).sum();
        return flowersPrice + accessoriesPrice;
    }

    /**
     * Обчислює вартість букета з урахуванням знижки.
     *
     * @return Вартість зі знижкою
     */
    public double calculateDiscountedPrice() {
        double totalPrice = calculateTotalPrice();
        return totalPrice * (1 - discount / 100);
    }

    /**
     * Обчислює середню свіжість квітів у букеті.
     *
     * @return Середня свіжість або 0, якщо квіти відсутні
     */
    public double calculateAverageFreshness() {
        if (flowers.isEmpty()) {
            return 0;
        }
        return flowers.stream().mapToInt(Flower::getFreshness).average().orElse(0);
    }

    /**
     * Сортує квіти в букеті за свіжістю (зростання).
     */
    public void sortFlowersByFreshness() {
        flowers.sort(Comparator.comparingInt(Flower::getFreshness));
        logger.info("Квіти в букеті '{}' відсортовано за свіжістю", name);
    }

    /**
     * Сортує квіти в букеті за довжиною стебла (спадання).
     */
    public void sortFlowersByStemLength() {
        flowers.sort(Comparator.comparingInt(Flower::getStemLength).reversed());
        logger.info("Квіти в букеті '{}' відсортовано за довжиною стебла", name);
    }

    /**
     * Знаходить квіти з довжиною стебла в заданому діапазоні.
     *
     * @param minLength Мінімальна довжина
     * @param maxLength Максимальна довжина
     * @return Список знайдених квітів
     */
    public List<Flower> findFlowersByStemLengthRange(int minLength, int maxLength) {
        List<Flower> found = flowers.stream()
                .filter(f -> f.getStemLength() >= minLength && f.getStemLength() <= maxLength)
                .collect(Collectors.toList());
        logger.info("Знайдено {} квіт(ів) у букеті '{}' з довжиною стебла [{}, {}]", found.size(), name, minLength, maxLength);
        return found;
    }

    // --- Методи для відображення ---

    /**
     * Повертає коротку інформацію про букет.
     *
     * @return Рядок з назвою, ціною та знижкою
     */
    public String getShortInfo() {
        return String.format("%s - %.2f грн (%.0f%% знижка)", name, calculateDiscountedPrice(), discount);
    }

    /**
     * Повертає детальну інформацію про букет у форматі HTML.
     *
     * @return Рядок з детальною інформацією
     */
    public String getDetailedInfo() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("<html><h2>%s</h2>", name))
                .append(String.format("<p><b>Опис:</b> %s</p>", description))
                .append(String.format("<p><b>Загальна вартість:</b> %.2f грн", calculateTotalPrice()));
        if (discount > 0) {
            sb.append(String.format(" <i>(знижка %.0f%%, ціна зі знижкою: %.2f грн)</i>", discount, calculateDiscountedPrice()));
        }
        sb.append("</p>")
                .append("<h3>Квіти:</h3><ul>");
        flowers.forEach(f -> sb.append(String.format("<li>%s (кількість: %d)</li>", f.getShortInfo(), f.getStockQuantity())));
        sb.append("</ul>");
        if (!accessories.isEmpty()) {
            sb.append("<h3>Аксесуари:</h3><ul>");
            accessories.forEach(a -> sb.append(String.format("<li>%s</li>", a.getShortInfo())));
            sb.append("</ul>");
        }
        sb.append("</html>");
        return sb.toString();
    }

    /**
     * Повертає інформацію про букет для кошика.
     *
     * @return Рядок з назвою, кількістю квітів, аксесуарів та ціною
     */
    public String getCartInfo() {
        return String.format("%s (Квітів: %d, Аксесуарів: %d) - %.2f грн",
                name, getTotalFlowerCount(), accessories.size(), calculateDiscountedPrice());
    }

    // --- Перевизначення методів Object ---

    @Override
    public String toString() {
        return String.format("Букет '%s' [Квітів: %d, Аксесуарів: %d, Ціна: %.2f грн]",
                name, getTotalFlowerCount(), accessories.size(), calculateDiscountedPrice());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Bouquet bouquet = (Bouquet) o;
        return id == bouquet.id &&
                Double.compare(bouquet.discount, discount) == 0 &&
                name.equals(bouquet.name) &&
                Objects.equals(description, bouquet.description) &&
                flowers.equals(bouquet.flowers) &&
                accessories.equals(bouquet.accessories) &&
                Objects.equals(imagePath, bouquet.imagePath);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, description, flowers, accessories, imagePath, discount);
    }

    public void setFlowerQuantity(Flower f, Integer orDefault) {
        if (flowers.contains(f)) {
            f.setStockQuantity(orDefault);
            logger.info("Встановлено кількість квітки '{}' у букеті '{}': {}", f.getDisplayName(), name, orDefault);
        } else {
            logger.warn("Квітка '{}' не знайдена у букеті '{}'", f.getDisplayName(), name);
        }
    }
}