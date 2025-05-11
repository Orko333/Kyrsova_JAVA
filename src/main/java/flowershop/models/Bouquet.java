package flowershop.models;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Клас Bouquet представляє букет, який складається з квітів і аксесуарів.
 * Містить назву, опис, зображення, знижку, список квітів та аксесуарів.
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
     * Конструктор за замовчуванням — створює порожній букет з назвою "Без назви".
     */
    public Bouquet() {
        this("Без назви", "", new ArrayList<>(), new ArrayList<>(), "", 0);
        logger.info("Створено порожній букет за замовчуванням.");
    }

    /**
     * Основний конструктор, який ініціалізує всі поля букета.
     *
     * @param name Назва букета.
     * @param description Опис букета.
     * @param flowers Список квітів.
     * @param accessories Список аксесуарів.
     * @param imagePath Шлях до зображення.
     * @param discount Знижка у відсотках (0-100).
     */
    public Bouquet(String name, String description, List<Flower> flowers,
                   List<Accessory> accessories, String imagePath, double discount) {
        logger.debug("Спроба створення букета: Ім'я='{}', Знижка={}%", name, discount);
        this.id = -1; // -1 означає, що букет ще не збережений у БД
        this.name = Objects.requireNonNull(name, "Назва букета не може бути null");
        this.description = Objects.requireNonNull(description, "Опис букета не може бути null");
        this.flowers = new ArrayList<>(Objects.requireNonNull(flowers, "Список квітів не може бути null"));
        this.accessories = new ArrayList<>(Objects.requireNonNull(accessories, "Список аксесуарів не може бути null"));
        this.imagePath = Objects.requireNonNull(imagePath, "Шлях до зображення не може бути null");
        setDiscount(discount); // Використовуємо сеттер для валідації
        logger.info("Букет '{}' успішно створено. Квітів: {}, Аксесуарів: {}", this.name, this.flowers.size(), this.accessories.size());
    }

    /**
     * Конструктор копіювання — створює копію іншого букета.
     *
     * @param bouquetToEdit Букет, з якого копіюються дані.
     */
    public Bouquet(Bouquet bouquetToEdit) {
        logger.debug("Створення копії букета: {}", bouquetToEdit.getName());
        this.id = bouquetToEdit.id;
        this.name = bouquetToEdit.name;
        this.description = bouquetToEdit.description;
        this.flowers = new ArrayList<>(bouquetToEdit.flowers); // Створюємо нові списки
        this.accessories = new ArrayList<>(bouquetToEdit.accessories); // Створюємо нові списки
        this.imagePath = bouquetToEdit.imagePath;
        this.discount = bouquetToEdit.discount;
        logger.info("Створено копію букета '{}'", this.name);
    }

    // Гетери і сетери

    public int getId() { return id; }
    public void setId(int id) {
        logger.trace("Встановлення ID {} для букета '{}'", id, this.name);
        this.id = id;
    }

    public String getName() { return name; }

    public void setName(String name) {
        logger.trace("Встановлення назви '{}' для букета (стара назва: '{}')", name, this.name);
        this.name = Objects.requireNonNull(name, "Назва букета не може бути null");
    }

    public String getDescription() { return description; }

    public void setDescription(String description) {
        logger.trace("Встановлення опису для букета '{}'", this.name);
        this.description = Objects.requireNonNull(description, "Опис букета не може бути null");
    }

    public List<Flower> getFlowers() {
        return new ArrayList<>(flowers); // Повертаємо копію для інкапсуляції
    }

    public List<Accessory> getAccessories() {
        return new ArrayList<>(accessories); // Повертаємо копію для інкапсуляції
    }

    public String getImagePath() { return imagePath; }

    public void setImagePath(String imagePath) {
        logger.trace("Встановлення шляху до зображення '{}' для букета '{}'", imagePath, this.name);
        this.imagePath = Objects.requireNonNull(imagePath, "Шлях до зображення не може бути null");
    }

    public double getDiscount() { return discount; }

    public void setDiscount(double discount) {
        logger.trace("Встановлення знижки {}% для букета '{}' (стара знижка: {}%)", discount, this.name, this.discount);
        if (discount < 0 || discount > 100) {
            logger.error("Спроба встановити недійсну знижку ({}) для букета '{}'. Знижка повинна бути в діапазоні від 0 до 100%", discount, this.name);
            throw new IllegalArgumentException("Знижка повинна бути в діапазоні від 0 до 100%");
        }
        this.discount = discount;
    }

    // Методи для роботи з квітами

    /**
     * Додає одну квітку до букета.
     */
    public void addFlower(Flower flower) {
        Objects.requireNonNull(flower, "Квітка не може бути null");
        flowers.add(flower);
        logger.info("Додано квітку '{}' до букета '{}'. Всього квітів: {}", flower.getDisplayName(), this.name, flowers.size());
    }

    /**
     * Додає список квітів до букета.
     */
    public void addFlowers(List<Flower> flowersToAdd) {
        Objects.requireNonNull(flowersToAdd, "Список квітів не може бути null");
        if (flowersToAdd.isEmpty()) {
            logger.debug("Спроба додати порожній список квітів до букета '{}'", this.name);
            return;
        }
        flowers.addAll(flowersToAdd);
        logger.info("Додано {} квіт(ів) до букета '{}'. Всього квітів: {}", flowersToAdd.size(), this.name, flowers.size());
    }

    /**
     * Видаляє конкретну квітку з букета.
     */
    public boolean removeFlower(Flower flower) {
        Objects.requireNonNull(flower, "Квітка не може бути null");
        boolean removed = flowers.remove(flower);
        if (removed) {
            logger.info("Видалено квітку '{}' з букета '{}'. Всього квітів: {}", flower.getDisplayName(), this.name, flowers.size());
        } else {
            logger.warn("Спроба видалити квітку '{}', якої немає в букеті '{}'", flower.getDisplayName(), this.name);
        }
        return removed;
    }

    /**
     * Видаляє квітку за індексом.
     */
    public Flower removeFlower(int index) {
        logger.debug("Спроба видалити квітку за індексом {} з букета '{}'", index, this.name);
        if (index < 0 || index >= flowers.size()) {
            logger.error("Невірний індекс {} для видалення квітки з букета '{}'. Розмір списку: {}", index, this.name, flowers.size());
            throw new IndexOutOfBoundsException("Невірний індекс квітки");
        }
        Flower removedFlower = flowers.remove(index);
        logger.info("Видалено квітку '{}' за індексом {} з букета '{}'. Всього квітів: {}", removedFlower.getDisplayName(), index, this.name, flowers.size());
        return removedFlower;
    }

    // Методи для роботи з аксесуарами

    /**
     * Додає аксесуар до букета.
     */
    public void addAccessory(Accessory accessory) {
        Objects.requireNonNull(accessory, "Аксесуар не може бути null");
        accessories.add(accessory);
        logger.info("Додано аксесуар '{}' до букета '{}'. Всього аксесуарів: {}", accessory.getName(), this.name, accessories.size());
    }

    /**
     * Додає список аксесуарів до букета.
     */
    public void addAccessories(List<Accessory> accessoriesToAdd) {
        Objects.requireNonNull(accessoriesToAdd, "Список аксесуарів не може бути null");
        if (accessoriesToAdd.isEmpty()) {
            logger.debug("Спроба додати порожній список аксесуарів до букета '{}'", this.name);
            return;
        }
        accessories.addAll(accessoriesToAdd);
        logger.info("Додано {} аксесуар(ів) до букета '{}'. Всього аксесуарів: {}", accessoriesToAdd.size(), this.name, accessories.size());
    }

    /**
     * Видаляє конкретний аксесуар з букета.
     */
    public boolean removeAccessory(Accessory accessory) {
        Objects.requireNonNull(accessory, "Аксесуар не може бути null");
        boolean removed = accessories.remove(accessory);
        if (removed) {
            logger.info("Видалено аксесуар '{}' з букета '{}'. Всього аксесуарів: {}", accessory.getName(), this.name, accessories.size());
        } else {
            logger.warn("Спроба видалити аксесуар '{}', якого немає в букеті '{}'", accessory.getName(), this.name);
        }
        return removed;
    }

    /**
     * Видаляє аксесуар за індексом.
     */
    public Accessory removeAccessory(int index) {
        logger.debug("Спроба видалити аксесуар за індексом {} з букета '{}'", index, this.name);
        if (index < 0 || index >= accessories.size()) {
            logger.error("Невірний індекс {} для видалення аксесуара з букета '{}'. Розмір списку: {}", index, this.name, accessories.size());
            throw new IndexOutOfBoundsException("Невірний індекс аксесуара");
        }
        Accessory removedAccessory = accessories.remove(index);
        logger.info("Видалено аксесуар '{}' за індексом {} з букета '{}'. Всього аксесуарів: {}", removedAccessory.getName(), index, this.name, accessories.size());
        return removedAccessory;
    }

    // Методи для очищення

    /**
     * Очищає всі квіти з букета.
     */
    public void clearFlowers() {
        if (!flowers.isEmpty()) {
            logger.info("Очищення квітів з букета '{}'. Було квітів: {}", this.name, flowers.size());
            flowers.clear();
        } else {
            logger.debug("Спроба очистити квіти з порожнього букета '{}'", this.name);
        }
    }

    /**
     * Очищає всі аксесуари з букета.
     */
    public void clearAccessories() {
        if (!accessories.isEmpty()) {
            logger.info("Очищення аксесуарів з букета '{}'. Було аксесуарів: {}", this.name, accessories.size());
            accessories.clear();
        } else {
            logger.debug("Спроба очистити аксесуари з порожнього букета '{}'", this.name);
        }
    }

    /**
     * Повністю очищає букет.
     */
    public void clear() {
        logger.info("Повне очищення букета '{}'", this.name);
        clearFlowers();
        clearAccessories();
    }

    // Розрахункові методи

    /**
     * Обчислює загальну вартість букета без знижки.
     */
    public double calculateTotalPrice() {
        double flowersPrice = flowers.stream().mapToDouble(Flower::getPrice).sum();
        double accessoriesPrice = accessories.stream().mapToDouble(Accessory::getPrice).sum();
        double totalPrice = flowersPrice + accessoriesPrice;
        logger.trace("Розрахунок загальної вартості букета '{}': Квіти={}, Аксесуари={}, Всього={}", this.name, flowersPrice, accessoriesPrice, totalPrice);
        return totalPrice;
    }

    /**
     * Обчислює вартість букета з урахуванням знижки.
     */
    public double calculateDiscountedPrice() {
        double totalPrice = calculateTotalPrice();
        double discountedPrice = totalPrice * (1 - discount / 100);
        logger.trace("Розрахунок вартості букета '{}' зі знижкою: Загальна вартість={}, Знижка={}%, Вартість зі знижкою={}", this.name, totalPrice, discount, discountedPrice);
        return discountedPrice;
    }

    /**
     * Обчислює середню свіжість усіх квітів у букеті.
     */
    public double calculateAverageFreshness() {
        if (flowers.isEmpty()) {
            logger.debug("Неможливо розрахувати середню свіжість для букета '{}' - немає квітів.", this.name);
            return 0;
        }
        double averageFreshness = flowers.stream().mapToInt(Flower::getFreshness).average().orElse(0);
        logger.trace("Розрахунок середньої свіжості квітів у букеті '{}': {}", this.name, averageFreshness);
        return averageFreshness;
    }

    /**
     * Сортує квіти за свіжістю у зростаючому порядку.
     */
    public void sortFlowersByFreshness() {
        logger.debug("Сортування квітів у букеті '{}' за свіжістю (зростання)", this.name);
        flowers.sort(Comparator.comparingInt(Flower::getFreshness));
    }

    /**
     * Сортує квіти за довжиною стебла у спадному порядку.
     */
    public void sortFlowersByStemLength() {
        logger.debug("Сортування квітів у букеті '{}' за довжиною стебла (спадання)", this.name);
        flowers.sort(Comparator.comparingInt(Flower::getStemLength).reversed());
    }

    /**
     * Знаходить квіти, довжина стебла яких входить у заданий діапазон.
     */
    public List<Flower> findFlowersByStemLengthRange(int minLength, int maxLength) {
        logger.debug("Пошук квітів у букеті '{}' з довжиною стебла в діапазоні [{}, {}]", this.name, minLength, maxLength);
        List<Flower> foundFlowers = flowers.stream()
                .filter(f -> f.getStemLength() >= minLength && f.getStemLength() <= maxLength)
                .collect(Collectors.toList());
        logger.info("Знайдено {} квіт(ів) у букеті '{}' з довжиною стебла в діапазоні [{}, {}]", foundFlowers.size(), this.name, minLength, maxLength);
        return foundFlowers;
    }

    // Методи для відображення

    /**
     * Повертає коротку інформацію про букет для списку.
     */
    public String getShortInfo() {
        return String.format("%s - %.2f грн (%.0f%% знижка)", name, calculateDiscountedPrice(), discount);
    }

    /**
     * Повертає детальну інформацію про букет у форматі HTML.
     */
    public String getDetailedInfo() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("<html><h2>%s</h2>", name));
        sb.append(String.format("<p><b>Опис:</b> %s</p>", description));
        sb.append(String.format("<p><b>Загальна вартість:</b> %.2f грн", calculateTotalPrice()));
        if (discount > 0) {
            sb.append(String.format(" <i>(знижка %.0f%%, ціна зі знижкою: %.2f грн)</i>", discount, calculateDiscountedPrice()));
        }
        sb.append("</p>");

        sb.append("<h3>Квіти:</h3><ul>");
        flowers.forEach(f -> sb.append(String.format("<li>%s</li>", f.getShortInfo())));
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
     * Повертає стислу інформацію про букет для кошика.
     */
    public String getCartInfo() {
        return String.format("%s (Квітів: %d, Аксесуарів: %d) - %.2f грн",
                name, flowers.size(), accessories.size(), calculateDiscountedPrice());
    }

    @Override
    public String toString() {
        return String.format("Букет '%s' [Квітів: %d, Аксесуарів: %d, Ціна: %.2f грн]",
                name, flowers.size(), accessories.size(), calculateDiscountedPrice());
    }

    // Перевизначення методів Object

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Bouquet bouquet = (Bouquet) o;
        boolean result = id == bouquet.id && // Додано порівняння id
                Double.compare(bouquet.discount, discount) == 0 &&
                name.equals(bouquet.name) &&
                Objects.equals(description, bouquet.description) && // Objects.equals для null-безпеки
                Objects.equals(flowers, bouquet.flowers) && // Порівнюємо списки за вмістом
                Objects.equals(accessories, bouquet.accessories) && // Порівнюємо списки за вмістом
                Objects.equals(imagePath, bouquet.imagePath); // Objects.equals для null-безпеки
        logger.trace("Порівняння букета '{}' з {}: результат {}", this.name, bouquet.getName(), result);
        return result;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, description, flowers, accessories, imagePath, discount); // Додано id
    }


    public void setFlowerQuantity(Flower key, Integer value) {
        logger.debug("Спроба встановити кількість {} для квітки '{}' у букеті '{}'", value, key.getDisplayName(), this.name);
        for (Flower flower : flowers) {
            if (flower.equals(key)) { // Припускаємо, що Flower.equals() коректно визначений
                // Важливо: цей метод змінює кількість квітки, яка може бути частиною
                // глобального каталогу, а не тільки цього букета. Це може бути не тим, що очікується.
                // Якщо квіти в букеті є унікальними екземплярами для цього букета, то все гаразд.
                // В іншому випадку, можливо, варто зберігати кількість квітів окремо, наприклад, у Map<Flower, Integer>
                flower.setStockQuantity(value);
                logger.info("Встановлено кількість {} для квітки '{}' у букеті '{}'", value, key.getDisplayName(), this.name);
                return; // Знайшли і оновили, виходимо
            }
        }
        logger.warn("Квітка '{}' не знайдена в букеті '{}' для встановлення кількості.", key.getDisplayName(), this.name);
    }

    public Integer getFlowerQuantity(Flower flower) {
        // Цей метод, здається, має на увазі, що кожна квітка у списку flowers
        // представляє одну одиницю. Якщо ж stockQuantity квітки означає кількість
        // однакових квітів, то логіка може бути іншою.
        // Поточна реалізація знаходить першу квітку, що дорівнює заданій, і повертає її stockQuantity.
        // Якщо в букеті може бути декілька однакових квітів (за типом, кольором і т.д.),
        // то цей метод поверне кількість першої знайденої.
        Optional<Flower> foundFlower = flowers.stream()
                .filter(f -> f.equals(flower)) // Припускаємо, що Flower.equals() коректно визначений
                .findFirst();
        if (foundFlower.isPresent()) {
            logger.trace("Отримання кількості для квітки '{}' в букеті '{}': {}", flower.getDisplayName(), this.name, foundFlower.get().getStockQuantity());
            return foundFlower.get().getStockQuantity();
        }
        logger.debug("Квітка '{}' не знайдена в букеті '{}' для отримання кількості. Повернення 0.", flower.getDisplayName(), this.name);
        return 0;
    }


    public void setAccessories(List<Accessory> accessories) {
        logger.debug("Встановлення нового списку аксесуарів для букета '{}'. Кількість: {}", this.name, accessories != null ? accessories.size() : "null");
        this.accessories = new ArrayList<>(Objects.requireNonNull(accessories, "Список аксесуарів не може бути null"));
    }

    public void setFlowers(List<Flower> flowers) {
        logger.debug("Встановлення нового списку квітів для букета '{}'. Кількість: {}", this.name, flowers != null ? flowers.size() : "null");
        this.flowers = new ArrayList<>(Objects.requireNonNull(flowers, "Список квітів не може бути null"));
    }

    /**
     * Встановлює кількість для кожної квітки в букеті на основі наданої HashMap.
     * УВАГА: Цей метод модифікує stockQuantity об'єктів Flower у списку flowers цього букета.
     * Якщо ці ж об'єкти Flower використовуються деінде (наприклад, у загальному каталозі квітів),
     * їх stockQuantity також зміниться. Якщо квіти в букеті повинні мати свою власну кількість,
     * незалежну від загального запасу, то потрібно змінити підхід (наприклад, зберігати Map<Flower, Integer>
     * для кількостей у букеті або клонувати квіти при додаванні до букета).
     */
    public void setFlowerQuantities(HashMap<Flower, Integer> flowerQuantitiesMap) {
        logger.debug("Встановлення кількостей квітів для букета '{}' на основі HashMap.", this.name);
        Objects.requireNonNull(flowerQuantitiesMap, "Мапа кількостей квітів не може бути null");

        // Створюємо копію списку квітів для безпечної ітерації та модифікації
        List<Flower> currentFlowers = new ArrayList<>(this.flowers);
        this.flowers.clear(); // Очищуємо поточний список квітів букета

        for (Map.Entry<Flower, Integer> entry : flowerQuantitiesMap.entrySet()) {
            Flower flowerFromMap = entry.getKey();
            Integer quantity = entry.getValue();

            if (flowerFromMap == null || quantity == null || quantity < 0) {
                logger.warn("Пропущено недійсний запис у flowerQuantitiesMap для букета '{}': Квітка={}, Кількість={}", this.name, flowerFromMap, quantity);
                continue;
            }

            // Шукаємо відповідну квітку в оригінальному списку (якщо потрібно зберегти інші атрибути)
            // Або просто створюємо/додаємо квітки на основі мапи.
            // Поточна логіка передбачає, що квітка з мапи є тим об'єктом, який має бути в букеті.

            // Припустимо, що ми хочемо, щоб у букеті була саме та кількість квітів, яка вказана в мапі.
            // Якщо flowerFromMap - це прототип, і нам потрібно мати 'quantity' екземплярів,
            // то логіка була б іншою (клонування або спеціальна обробка).
            // Якщо ж flowerFromMap - це конкретний об'єкт, і його stockQuantity - це кількість В ЦЬОМУ БУКЕТІ,
            // то ми можемо просто додати його.

            // Поточний метод Bouquet.addFlower просто додає квітку до списку.
            // Якщо ми хочемо, щоб одна квітка представляла кілька штук,
            // то поле stockQuantity самої квітки має відображати цю кількість.

            // Найпростіший варіант, якщо flowerFromMap - це вже квітка з потрібною кількістю (stockQuantity),
            // і ми просто додаємо її до букета.
            // Або, якщо мапа визначає, ЯКІ квіти мають бути в букеті і в ЯКІЙ кількості,
            // тоді ми додаємо flowerFromMap до списку flowers, а її stockQuantity встановлюємо в quantity.
            // Це те, що робив старий setFlowerQuantity(flower, quantity).

            // Якщо flowers - це список унікальних типів квітів, а їх кількість зберігається окремо,
            // тоді ця логіка не підходить.

            // Виходячи з назви setFlowerQuantities та наявності getTotalFlowerCount,
            // припустимо, що stockQuantity на Flower в контексті букета означає кількість
            // ЦЬОГО ТИПУ квітки В ЦЬОМУ БУКЕТІ.

            // Варіант 1: Модифікуємо існуючі квіти або додаємо нові, якщо їх немає.
            boolean found = false;
            for (Flower bouquetFlower : currentFlowers) {
                if (bouquetFlower.equals(flowerFromMap)) { // Потрібен коректний equals в Flower
                    bouquetFlower.setStockQuantity(quantity); // Встановлюємо нову кількість
                    this.flowers.add(bouquetFlower);      // Додаємо оновлену квітку назад
                    found = true;
                    logger.trace("Оновлено кількість для квітки '{}' в букеті '{}' до {}", bouquetFlower.getDisplayName(), this.name, quantity);
                    break;
                }
            }
            if (!found) {
                // Якщо квітка з мапи не була в букеті, можливо, її треба додати.
                // Тут залежить від логіки: чи мапа повністю визначає склад букета,
                // чи тільки оновлює кількості існуючих.
                // Припустимо, що мапа визначає повний склад.
                // Важливо: якщо flowerFromMap - це об'єкт з глобального каталогу,
                // зміна його stockQuantity тут вплине на каталог.
                // Краще клонувати або мати окремі об'єкти для букета.
                // Для простоти, припустимо, що ми можемо модифікувати stockQuantity.
                try {
                    Flower flowerToAdd = new Flower(flowerFromMap); // Створюємо копію, щоб не міняти оригінал
                    flowerToAdd.setStockQuantity(quantity);
                    this.flowers.add(flowerToAdd);
                    logger.trace("Додано нову квітку '{}' з кількістю {} до букета '{}'", flowerToAdd.getDisplayName(), quantity, this.name);
                } catch (Exception e) {
                    logger.error("Помилка при створенні копії квітки '{}' для букета '{}'", flowerFromMap.getDisplayName(), this.name, e);
                }
            }
        }
        logger.info("Кількості квітів для букета '{}' встановлено. Новий склад квітів: {}", this.name, this.flowers.size());
    }


    /**
     * Повертає загальну кількість усіх квіткових одиниць у букеті,
     * враховуючи поле stockQuantity кожної квітки у списку flowers.
     * Наприклад, якщо в букеті 2 троянди (де stockQuantity=2) і 3 лілії (stockQuantity=3),
     * метод поверне 2+3=5.
     * Якщо ж stockQuantity на Flower не використовується для кількості в букеті, а flowers
     * містить кожен екземпляр квітки, то цей метод має просто повернути flowers.size().
     * Виходячи з назви getTotalFlowerCount та наявності setFlowerQuantities,
     * припускається, що stockQuantity на Flower вказує на кількість цього типу квітки в букеті.
     *
     * @return Загальна кількість квітів у букеті.
     */
    public int getTotalFlowerCount() { // Змінено тип повернення на int
        int totalCount = flowers.stream()
                .mapToInt(Flower::getStockQuantity) // Припускаємо, що getStockQuantity повертає кількість цієї квітки в букеті
                .sum();
        logger.trace("Розрахунок загальної кількості квітів у букеті '{}': {}", this.name, totalCount);
        return totalCount;
    }
}