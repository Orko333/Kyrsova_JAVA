package flowershop.gui;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument; // Явний імпорт для PlainDocument

/**
 * Спрощений слухач змін тексту (DocumentListener), який виконує зворотний виклик (callback),
 * коли текст змінюється. Також забезпечує додаткову функціональність для обробки та валідації тексту.
 */
public class SimpleDocumentListener implements DocumentListener {
    private static final Logger logger = LogManager.getLogger(SimpleDocumentListener.class);

    private final Runnable callback;
    private final TextChangeHandler changeHandler;
    // private boolean isUpdating = false; // Поле isUpdating не використовується в поточній реалізації handleChange

    /**
     * Функціональний інтерфейс для обробки змін тексту з додатковим контекстом.
     */
    public interface TextChangeHandler {
        void handleChange(Document document, String newText);
    }

    /**
     * Створює слухача з простим зворотним викликом.
     *
     * @param callback Об'єкт Runnable, який виконується при зміні тексту.
     */
    public SimpleDocumentListener(Runnable callback) {
        this.callback = callback;
        this.changeHandler = null;
        logger.debug("SimpleDocumentListener створено з Runnable callback.");
    }

    /**
     * Створює слухача з обробником змін.
     *
     * @param changeHandler Обробник змін тексту.
     */
    public SimpleDocumentListener(TextChangeHandler changeHandler) {
        this.callback = null;
        this.changeHandler = changeHandler;
        logger.debug("SimpleDocumentListener створено з TextChangeHandler.");
    }

    @Override
    public void insertUpdate(DocumentEvent e) {
        logger.trace("Подія insertUpdate: {}", e);
        handleChange(e);
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
        logger.trace("Подія removeUpdate: {}", e);
        handleChange(e);
    }

    @Override
    public void changedUpdate(DocumentEvent e) {
        logger.trace("Подія changedUpdate: {}", e);
        handleChange(e);
    }

    /**
     * Обробляє будь-яку зміну документа, викликаючи відповідний обробник або зворотний виклик.
     *
     * @param e Подія зміни документа.
     */
    private void handleChange(DocumentEvent e) {
        // if (isUpdating) { // Якщо isUpdating використовується, його слід правильно встановлювати
        //     logger.trace("handleChange пропущено через isUpdating=true");
        //     return;
        // }
        logger.trace("Обробка зміни документа: тип події {}", e.getType());
        try {
            Document doc = e.getDocument();
            String text = doc.getText(0, doc.getLength());
            logger.trace("Текст документа: \"{}\"", text);

            if (changeHandler != null) {
                logger.trace("Виклик TextChangeHandler.");
                changeHandler.handleChange(doc, text);
            } else if (callback != null) {
                logger.trace("Виклик Runnable callback.");
                callback.run();
            } else {
                logger.warn("Немає обробника (ні callback, ні changeHandler) для події зміни документа.");
            }
        } catch (Exception ex) {
            logger.error("Помилка під час обробки змін у документі: {}", ex.getMessage(), ex);
        }
    }

    /**
     * Безпечно оновлює текст у документі без виклику подій зміни.
     *
     * @param doc  Документ, який потрібно оновити.
     * @param text Новий текст.
     */
    public static void updateTextSilently(Document doc, String text) {
        logger.debug("Спроба беззвучного оновлення тексту на: \"{}\"", text);
        if (doc instanceof PlainDocument) {
            PlainDocument plainDoc = (PlainDocument) doc;
            DocumentListener[] listeners = plainDoc.getDocumentListeners();
            boolean selfListenerFound = false; // Прапорець для перевірки, чи є серед слухачів сам SimpleDocumentListener

            logger.trace("Тимчасове видалення {} слухачів документа.", listeners.length);
            for (DocumentListener listener : listeners) {
                if (listener instanceof SimpleDocumentListener) {
                    // ((SimpleDocumentListener) listener).isUpdating = true; // Якщо isUpdating використовується
                    selfListenerFound = true;
                }
                plainDoc.removeDocumentListener(listener);
            }
            if (selfListenerFound) {
                logger.trace("Знайдено SimpleDocumentListener серед слухачів, isUpdating встановлено (якщо використовується).");
            }


            try {
                plainDoc.remove(0, plainDoc.getLength());
                plainDoc.insertString(0, text, null);
                logger.trace("Текст документа оновлено на \"{}\" беззвучно.", text);
            } catch (Exception e) {
                logger.error("Помилка при оновленні тексту в PlainDocument (remove/insert): {}", e.getMessage(), e);
            }
            finally {
                logger.trace("Відновлення {} слухачів документа.", listeners.length);
                for (DocumentListener listener : listeners) {
                    if (listener instanceof SimpleDocumentListener) {
                        // ((SimpleDocumentListener) listener).isUpdating = false; // Якщо isUpdating використовується
                    }
                    plainDoc.addDocumentListener(listener);
                }
                if (selfListenerFound) {
                    logger.trace("isUpdating скинуто для SimpleDocumentListener (якщо використовується).");
                }
            }
        } else {
            logger.warn("Неможливо виконати updateTextSilently: документ не є PlainDocument. Тип: {}", doc.getClass().getName());
            // Альтернативна (менш надійна) спроба для не-PlainDocument, якщо це необхідно.
            // Однак, зазвичай DocumentListener прив'язаний до конкретного типу документа.
            // Для загального випадку Document, немає стандартного способу отримати всіх DocumentListener'ів.
            try {
                // Цей блок може викликати події, якщо документ не є PlainDocument
                // і не має спеціального механізму для тимчасового відключення слухачів.
                doc.remove(0, doc.getLength());
                doc.insertString(0, text, null);
                logger.warn("Текст оновлено для не-PlainDocument, але це могло викликати події.");
            } catch (Exception e) {
                logger.error("Помилка при оновленні тексту в документі типу {}: {}", doc.getClass().getName(), e.getMessage(), e);
            }
        }
    }

    /**
     * Створює слухача, який дозволяє лише числове введення.
     *
     * @return Налаштований SimpleDocumentListener.
     */
    public static SimpleDocumentListener createNumericFilter() {
        logger.info("Створення числового фільтра (NumericFilter).");
        return new SimpleDocumentListener((doc, text) -> {
            if (!text.matches("\\d*")) {
                String filteredText = text.replaceAll("[^\\d]", "");
                logger.trace("Числовий фільтр: вхідний текст \"{}\" не є числовим. Фільтрований текст: \"{}\"", text, filteredText);
                updateTextSilently(doc, filteredText);
            } else {
                logger.trace("Числовий фільтр: вхідний текст \"{}\" є числовим.", text);
            }
        });
    }

    /**
     * Створює слухача, який дозволяє лише десяткове число.
     *
     * @return Налаштований SimpleDocumentListener.
     */
    public static SimpleDocumentListener createDecimalFilter() {
        logger.info("Створення десяткового фільтра (DecimalFilter).");
        return new SimpleDocumentListener((doc, text) -> {
            // Дозволяє порожній рядок, число, число з крапкою, число з крапкою і цифрами після
            if (!text.matches("^\\d*\\.?\\d*$")) {
                // Залишаємо лише цифри та першу крапку
                String filteredText = text.replaceAll("[^\\d.]", "");
                int firstDot = filteredText.indexOf('.');
                if (firstDot != -1) {
                    String beforeDot = filteredText.substring(0, firstDot + 1);
                    String afterDot = filteredText.substring(firstDot + 1).replaceAll("\\.", "");
                    filteredText = beforeDot + afterDot;
                }
                logger.trace("Десятковий фільтр: вхідний текст \"{}\" не є десятковим. Фільтрований текст: \"{}\"", text, filteredText);
                updateTextSilently(doc, filteredText);
            } else {
                logger.trace("Десятковий фільтр: вхідний текст \"{}\" відповідає формату.", text);
            }
        });
    }

    /**
     * Створює слухача, який обмежує довжину введеного тексту.
     *
     * @param maxLength Максимально допустима кількість символів.
     * @return Налаштований SimpleDocumentListener.
     */
    public static SimpleDocumentListener createLengthLimiter(int maxLength) {
        logger.info("Створення обмежувача довжини (LengthLimiter) з maxLength = {}.", maxLength);
        if (maxLength < 0) {
            logger.warn("MaxLength для LengthLimiter не може бути від'ємним ({}). Обмежувач не буде ефективним.", maxLength);
            // Повертаємо слухача, який нічого не робить, або кидаємо виняток
            return new SimpleDocumentListener((doc, text) -> {});
        }
        return new SimpleDocumentListener((doc, text) -> {
            if (text.length() > maxLength) {
                String limitedText = text.substring(0, maxLength);
                logger.trace("Обмежувач довжини: текст \"{}\" (довжина {}) перевищує {}. Обрізаний текст: \"{}\"",
                        text, text.length(), maxLength, limitedText);
                updateTextSilently(doc, limitedText);
            } else {
                logger.trace("Обмежувач довжини: текст \"{}\" (довжина {}) не перевищує {}.", text, text.length(), maxLength);
            }
        });
    }
}