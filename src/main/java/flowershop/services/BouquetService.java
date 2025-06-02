package flowershop.services;

import flowershop.models.Accessory;
import flowershop.models.Bouquet;
import flowershop.models.Flower;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Сервісний клас для управління логікою та операціями, пов'язаними з об'єктом {@link Bouquet}.
 * Надає методи для маніпулювання складом букета, розрахунків вартості, сортування,
 * а також для генерації рядкових представлень букета.
 * Кожен екземпляр сервісу працює з конкретним екземпляром букета.
 */
public class BouquetService {

    private static final Logger logger = LogManager.getLogger(BouquetService.class);
    private final Bouquet bouquet;

    /**
     * Конструктор для створення сервісу, що працює з конкретним букетом.
     *
     * @param bouquet Букет, для якого буде надаватися сервіс.
     * @throws NullPointerException якщо переданий букет є {@code null}.
     */
    public BouquetService(Bouquet bouquet) {
        this.bouquet = Objects.requireNonNull(bouquet, "Букет не може бути null для сервісу");
    }

    public int getFlowerQuantity(Bouquet bouquet, Flower rose) {
        Objects.requireNonNull(bouquet, "Букет не може бути null");
        Objects.requireNonNull(rose, "Квітка не може бути null");
        return (int) bouquet.getFlowers().stream()
                .filter(f -> f.equals(rose))
                .count();
    }

    /**
     * Додає одну квітку до букета.
     *
     * @param flower Квітка для додавання.
     * @throws NullPointerException якщо передана квітка є {@code null}.
     */
    public void addFlower(Flower flower) {
        Objects.requireNonNull(flower, "Квітка не може бути null");
        List<Flower> currentFlowers = new ArrayList<>(bouquet.getFlowers());
        currentFlowers.add(flower);
        bouquet.setFlowers(currentFlowers);
        FlowerService flowerService = new FlowerService(flower);
        logger.info("Сервісом додано квітку '{}' до букета '{}'. Всього квітів: {}", flowerService.getDisplayName(), bouquet.getName(), currentFlowers.size());
    }

    /**
     * Додає список квітів до букета.
     *
     * @param flowersToAdd Список квітів для додавання.
     * @throws NullPointerException якщо переданий список квітів є {@code null}.
     */
    public void addFlowers(List<Flower> flowersToAdd) {
        Objects.requireNonNull(flowersToAdd, "Список квітів для додавання не може бути null");
        if (flowersToAdd.isEmpty()) {
            return;
        }
        List<Flower> currentFlowers = new ArrayList<>(bouquet.getFlowers());
        currentFlowers.addAll(flowersToAdd);
        bouquet.setFlowers(currentFlowers);
        logger.info("Сервісом додано {} квіт(ів) до букета '{}'. Всього квітів: {}", flowersToAdd.size(), bouquet.getName(), currentFlowers.size());
    }

    /**
     * Видаляє конкретну квітку з букета. Видаляється перше входження квітки.
     *
     * @param flower Квітка для видалення.
     * @return {@code true}, якщо квітка була видалена, {@code false} — якщо не знайдена.
     * @throws NullPointerException якщо передана квітка є {@code null}.
     */
    public boolean removeFlower(Flower flower) {
        Objects.requireNonNull(flower, "Квітка для видалення не може бути null");
        List<Flower> currentFlowers = new ArrayList<>(bouquet.getFlowers());
        FlowerService flowerService = new FlowerService(flower);
        boolean removed = currentFlowers.remove(flower);
        if (removed) {
            bouquet.setFlowers(currentFlowers);
            logger.info("Сервісом видалено квітку '{}' з букета '{}'. Всього квітів: {}", flowerService.getDisplayName(), bouquet.getName(), currentFlowers.size());
        }
        return removed;
    }

    /**
     * Видаляє квітку за індексом зі списку квітів букета.
     *
     * @param index Індекс квітки для видалення.
     * @return Видалена квітка.
     * @throws IndexOutOfBoundsException Якщо індекс недійсний.
     */
    public Flower removeFlower(int index) {
        List<Flower> currentFlowers = new ArrayList<>(bouquet.getFlowers());
        if (index < 0 || index >= currentFlowers.size()) {
            throw new IndexOutOfBoundsException("Невірний індекс квітки для видалення: " + index);
        }
        Flower removedFlower = currentFlowers.remove(index);
        FlowerService flowerService = new FlowerService(removedFlower);
        bouquet.setFlowers(currentFlowers);
        logger.info("Сервісом видалено квітку '{}' за індексом {} з букета '{}'", flowerService.getDisplayName(), index, bouquet.getName());
        return removedFlower;
    }

    /**
     * Встановлює кількість певної квітки у букеті.
     * Видаляє всі попередні входження цієї квітки (визначені за {@link Flower#equals(Object)})
     * та додає вказану кількість нових екземплярів (копій).
     *
     * @param flower   Квітка, кількість якої треба встановити.
     * @param quantity Нова кількість таких квітів у букеті.
     * @throws NullPointerException     якщо передана квітка є {@code null}.
     * @throws IllegalArgumentException Якщо кількість від'ємна.
     */
    public void setFlowerQuantity(Flower flower, int quantity) {
        Objects.requireNonNull(flower, "Квітка не може бути null");
        if (quantity < 0) {
            throw new IllegalArgumentException("Кількість не може бути від'ємною");
        }

        List<Flower> currentFlowers = bouquet.getFlowers().stream()
                .filter(f -> !f.equals(flower))
                .collect(Collectors.toList());

        for (int i = 0; i < quantity; i++) {
            currentFlowers.add(new Flower(flower)); // Додаємо копію
        }
        bouquet.setFlowers(currentFlowers);
        FlowerService flowerService = new FlowerService(flower);
        logger.info("Сервісом встановлено кількість квітки '{}' у букеті '{}': {}", flowerService.getDisplayName(), bouquet.getName(), quantity);
    }

    /**
     * Повертає загальну кількість об'єктів квітів у букеті.
     *
     * @return Загальна кількість квітів.
     */
    public int getTotalFlowerCount() {
        int total = bouquet.getFlowers().size();
        logger.trace("Сервіс: Загальна кількість квітів у букеті '{}': {}", bouquet.getName(), total);
        return total;
    }


    /**
     * Додає один аксесуар до букета.
     *
     * @param accessory Аксесуар для додавання.
     * @throws NullPointerException якщо переданий аксесуар є {@code null}.
     */
    public void addAccessory(Accessory accessory) {
        Objects.requireNonNull(accessory, "Аксесуар не може бути null");
        List<Accessory> currentAccessories = new ArrayList<>(bouquet.getAccessories());
        currentAccessories.add(accessory);
        bouquet.setAccessories(currentAccessories);
        logger.info("Сервісом додано аксесуар '{}' до букета '{}'. Всього аксесуарів: {}", accessory.getName(), bouquet.getName(), currentAccessories.size());
    }

    /**
     * Додає список аксесуарів до букета.
     *
     * @param accessoriesToAdd Список аксесуарів для додавання.
     * @throws NullPointerException якщо переданий список аксесуарів є {@code null}.
     */
    public void addAccessories(List<Accessory> accessoriesToAdd) {
        Objects.requireNonNull(accessoriesToAdd, "Список аксесуарів для додавання не може бути null");
        if (accessoriesToAdd.isEmpty()) {
            return;
        }
        List<Accessory> currentAccessories = new ArrayList<>(bouquet.getAccessories());
        currentAccessories.addAll(accessoriesToAdd);
        bouquet.setAccessories(currentAccessories);
        logger.info("Сервісом додано {} аксесуар(ів) до букета '{}'. Всього аксесуарів: {}", accessoriesToAdd.size(), bouquet.getName(), currentAccessories.size());
    }

    /**
     * Видаляє конкретний аксесуар з букета. Видаляється перше входження аксесуара.
     *
     * @param accessory Аксесуар для видалення.
     * @return {@code true}, якщо аксесуар був видалений, {@code false} — якщо не знайдений.
     * @throws NullPointerException якщо переданий аксесуар є {@code null}.
     */
    public boolean removeAccessory(Accessory accessory) {
        Objects.requireNonNull(accessory, "Аксесуар для видалення не може бути null");
        List<Accessory> currentAccessories = new ArrayList<>(bouquet.getAccessories());
        boolean removed = currentAccessories.remove(accessory);
        if (removed) {
            bouquet.setAccessories(currentAccessories);
            logger.info("Сервісом видалено аксесуар '{}' з букета '{}'. Всього аксесуарів: {}", accessory.getName(), bouquet.getName(), currentAccessories.size());
        }
        return removed;
    }

    /**
     * Видаляє аксесуар за індексом зі списку аксесуарів букета.
     *
     * @param index Індекс аксесуара для видалення.
     * @return Видалений аксесуар.
     * @throws IndexOutOfBoundsException Якщо індекс недійсний.
     */
    public Accessory removeAccessory(int index) {
        List<Accessory> currentAccessories = new ArrayList<>(bouquet.getAccessories());
        if (index < 0 || index >= currentAccessories.size()) {
            throw new IndexOutOfBoundsException("Невірний індекс аксесуара для видалення: " + index);
        }
        Accessory removedAccessory = currentAccessories.remove(index);
        bouquet.setAccessories(currentAccessories);
        logger.info("Сервісом видалено аксесуар '{}' за індексом {} з букета '{}'", removedAccessory.getName(), index, bouquet.getName());
        return removedAccessory;
    }

    /**
     * Очищає список квітів у букеті.
     */
    public void clearFlowers() {
        if (!bouquet.getFlowers().isEmpty()) {
            logger.info("Сервісом очищено квіти з букета '{}'. Було квітів: {}", bouquet.getName(), bouquet.getFlowers().size());
            bouquet.setFlowers(new ArrayList<>());
        }
    }

    /**
     * Очищає список аксесуарів у букеті.
     */
    public void clearAccessories() {
        if (!bouquet.getAccessories().isEmpty()) {
            logger.info("Сервісом очищено аксесуари з букета '{}'. Було аксесуарів: {}", bouquet.getName(), bouquet.getAccessories().size());
            bouquet.setAccessories(new ArrayList<>());
        }
    }

    /**
     * Повністю очищає букет (видаляє всі квіти та аксесуари).
     */
    public void clear() {
        clearFlowers();
        clearAccessories();
        logger.info("Сервісом букет '{}' повністю очищено", bouquet.getName());
    }

    /**
     * Обчислює загальну вартість букета без урахування знижки.
     * Вартість розраховується як сума цін усіх квітів та аксесуарів.
     *
     * @return Загальна вартість букета.
     */
    public double calculateTotalPrice() {
        double flowersPrice = bouquet.getFlowers().stream().mapToDouble(Flower::getPrice).sum();
        double accessoriesPrice = bouquet.getAccessories().stream().mapToDouble(Accessory::getPrice).sum();
        return flowersPrice + accessoriesPrice;
    }

    /**
     * Обчислює вартість букета з урахуванням встановленої знижки.
     *
     * @return Вартість букета зі знижкою.
     */
    public double calculateDiscountedPrice() {
        double totalPrice = calculateTotalPrice();
        return totalPrice * (1 - bouquet.getDiscount() / 100.0);
    }

    /**
     * Обчислює середню свіжість квітів у букеті.
     *
     * @return Середнє значення свіжості квітів, або 0, якщо у букеті немає квітів.
     */
    public double calculateAverageFreshness() {
        if (bouquet.getFlowers().isEmpty()) {
            return 0;
        }
        return bouquet.getFlowers().stream().mapToInt(Flower::getFreshness).average().orElse(0);
    }

    /**
     * Сортує квіти в букеті за рівнем свіжості у порядку зростання.
     * Модифікує внутрішній список квітів букета.
     */
    public void sortFlowersByFreshness() {
        List<Flower> currentFlowers = new ArrayList<>(bouquet.getFlowers());
        currentFlowers.sort(Comparator.comparingInt(Flower::getFreshness));
        bouquet.setFlowers(currentFlowers);
        logger.info("Сервісом квіти в букеті '{}' відсортовано за свіжістю", bouquet.getName());
    }

    /**
     * Сортує квіти в букеті за довжиною стебла у порядку спадання.
     * Модифікує внутрішній список квітів букета.
     */
    public void sortFlowersByStemLength() {
        List<Flower> currentFlowers = new ArrayList<>(bouquet.getFlowers());
        currentFlowers.sort(Comparator.comparingInt(Flower::getStemLength).reversed());
        bouquet.setFlowers(currentFlowers);
        logger.info("Сервісом квіти в букеті '{}' відсортовано за довжиною стебла (спадання)", bouquet.getName());
    }

    /**
     * Знаходить та повертає список квітів у букеті, довжина стебла яких знаходиться у заданому діапазоні.
     *
     * @param minLength Мінімальна довжина стебла (включно).
     * @param maxLength Максимальна довжина стебла (включно).
     * @return Список квітів, що відповідають критеріям.
     */
    public List<Flower> findFlowersByStemLengthRange(int minLength, int maxLength) {
        List<Flower> found = bouquet.getFlowers().stream()
                .filter(f -> f.getStemLength() >= minLength && f.getStemLength() <= maxLength)
                .collect(Collectors.toList());
        logger.info("Сервісом знайдено {} квіт(ів) у букеті '{}' з довжиною стебла в діапазоні [{}, {}]", found.size(), bouquet.getName(), minLength, maxLength);
        return found;
    }

    /**
     * Повертає коротку інформацію про букет, включаючи назву, ціну зі знижкою та розмір знижки.
     *
     * @return Рядкове представлення короткої інформації про букет.
     */
    public String getShortInfo() {
        return String.format("%s - %.2f грн (%.0f%% знижка)", bouquet.getName(), calculateDiscountedPrice(), bouquet.getDiscount());
    }

    /**
     * Повертає детальну інформацію про букет у форматі HTML.
     * Включає назву, опис, вартість, інформацію про квіти (згруповані за типом та кількістю)
     * та аксесуари. Для форматування інформації про аксесуари використовується {@link AccessoryService}.
     *
     * @return Рядкове представлення детальної інформації про букет у форматі HTML.
     */
    public String getDetailedInfo() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("<html><h2>%s</h2>", bouquet.getName()))
                .append(String.format("<p><b>Опис:</b> %s</p>", bouquet.getDescription()))
                .append(String.format("<p><b>Загальна вартість:</b> %.2f грн", calculateTotalPrice()));
        if (bouquet.getDiscount() > 0) {
            sb.append(String.format(" <i>(знижка %.0f%%, ціна зі знижкою: %.2f грн)</i>", bouquet.getDiscount(), calculateDiscountedPrice()));
        }
        sb.append("</p>");

        if (!bouquet.getFlowers().isEmpty()) {
            sb.append("<h3>Квіти:</h3><ul>");
            Map<String, Long> flowerCounts = bouquet.getFlowers().stream()
                    .collect(Collectors.groupingBy(
                            flower -> {
                                FlowerService flowerService = new FlowerService(flower);
                                return flowerService.getShortInfo();
                            },
                            Collectors.counting()
                    ));
            flowerCounts.forEach((flowerInfo, count) ->
                    sb.append(String.format("<li>%s (кількість: %d)</li>", flowerInfo, count))
            );
            sb.append("</ul>");
        } else {
            sb.append("<p>Квіти відсутні.</p>");
        }


        if (!bouquet.getAccessories().isEmpty()) {
            sb.append("<h3>Аксесуари:</h3><ul>");
            bouquet.getAccessories().forEach(a -> {
                AccessoryService accessoryService = new AccessoryService(a);
                sb.append(String.format("<li>%s</li>", accessoryService.getShortInfo()));
            });
            sb.append("</ul>");
        } else {
            sb.append("<p>Аксесуари відсутні.</p>");
        }
        sb.append("</html>");
        return sb.toString();
    }

    /**
     * Повертає інформацію про букет, призначену для відображення у кошику.
     * Включає назву, загальну кількість квітів, кількість аксесуарів та ціну зі знижкою.
     *
     * @return Рядкове представлення інформації про букет для кошика.
     */
    public String getCartInfo() {
        return String.format("%s (Квітів: %d, Аксесуарів: %d) - %.2f грн",
                bouquet.getName(), getTotalFlowerCount(), bouquet.getAccessories().size(), calculateDiscountedPrice());
    }

    /**
     * Повертає форматоване рядкове представлення букета,
     * що включає назву, кількість квітів, кількість аксесуарів та ціну зі знижкою.
     *
     * @return Рядкове представлення букета.
     */
    public String getBouquetDisplayString() {
        return String.format("Букет '%s' [Квітів: %d, Аксесуарів: %d, Ціна: %.2f грн]",
                bouquet.getName(), getTotalFlowerCount(), bouquet.getAccessories().size(), calculateDiscountedPrice());
    }
}