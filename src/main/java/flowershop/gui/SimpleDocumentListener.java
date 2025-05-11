package flowershop.gui;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;

/**
 * Клас SimpleDocumentListener реалізує слухача змін тексту для компонентів Swing.
 * Викликає зворотний виклик або обробник при зміні тексту та підтримує фільтрацію введення.
 */
public class SimpleDocumentListener implements DocumentListener {

    private static final Logger logger = LogManager.getLogger(SimpleDocumentListener.class);

    private final Runnable callback;
    private final TextChangeHandler changeHandler;

    /**
     * Функціональний інтерфейс для обробки змін тексту з доступом до документа та тексту.
     */
    @FunctionalInterface
    public interface TextChangeHandler {
        /**
         * Обробляє зміну тексту в документі.
         *
         * @param document Документ, що змінився
         * @param newText Новий текст
         */
        void handleChange(Document document, String newText);
    }

    // --- Конструктори ---

    /**
     * Створює слухача з простим зворотним викликом.
     *
     * @param callback Об'єкт Runnable, що викликається при зміні тексту
     */
    public SimpleDocumentListener(Runnable callback) {
        this.callback = callback;
        this.changeHandler = null;
        logger.debug("Створено слухача з Runnable callback");
    }

    /**
     * Створює слухача з обробником змін тексту.
     *
     * @param changeHandler Обробник змін тексту
     */
    public SimpleDocumentListener(TextChangeHandler changeHandler) {
        this.callback = null;
        this.changeHandler = changeHandler;
        logger.debug("Створено слухача з TextChangeHandler");
    }

    // --- Реалізація DocumentListener ---

    @Override
    public void insertUpdate(DocumentEvent e) {
        handleChange(e);
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
        handleChange(e);
    }

    @Override
    public void changedUpdate(DocumentEvent e) {
        handleChange(e);
    }

    /**
     * Обробляє зміну документа, викликаючи зворотний виклик або обробник.
     *
     * @param e Подія зміни документа
     */
    private void handleChange(DocumentEvent e) {
        try {
            Document doc = e.getDocument();
            String text = doc.getText(0, doc.getLength());
            if (changeHandler != null) {
                changeHandler.handleChange(doc, text);
            } else if (callback != null) {
                callback.run();
            } else {
                logger.warn("Немає обробника для події зміни документа");
            }
        } catch (Exception ex) {
            logger.error("Помилка обробки зміни документа: {}", ex.getMessage(), ex);
        }
    }

    // --- Статичні методи ---

    /**
     * Оновлює текст у документі без виклику подій слухачів.
     *
     * @param doc  Документ для оновлення
     * @param text Новий текст
     */
    public static void updateTextSilently(Document doc, String text) {
        if (doc instanceof PlainDocument plainDoc) {
            DocumentListener[] listeners = plainDoc.getDocumentListeners();
            for (DocumentListener listener : listeners) {
                plainDoc.removeDocumentListener(listener);
            }
            try {
                plainDoc.remove(0, plainDoc.getLength());
                plainDoc.insertString(0, text, null);
                logger.debug("Текст оновлено беззвучно: '{}'", text);
            } catch (Exception e) {
                logger.error("Помилка оновлення тексту в PlainDocument: {}", e.getMessage(), e);
            } finally {
                for (DocumentListener listener : listeners) {
                    plainDoc.addDocumentListener(listener);
                }
            }
        } else {
            logger.warn("Документ не є PlainDocument: {}. Оновлення може викликати події", doc.getClass().getName());
            try {
                doc.remove(0, doc.getLength());
                doc.insertString(0, text, null);
            } catch (Exception e) {
                logger.error("Помилка оновлення тексту в документі {}: {}", doc.getClass().getName(), e.getMessage(), e);
            }
        }
    }

    /**
     * Створює слухача, який дозволяє вводити лише цифри.
     *
     * @return Слухача з числовим фільтром
     */
    public static SimpleDocumentListener createNumericFilter() {
        return new SimpleDocumentListener((doc, text) -> {
            if (!text.matches("\\d*")) {
                String filteredText = text.replaceAll("[^\\d]", "");
                updateTextSilently(doc, filteredText);
                logger.debug("Фільтр чисел: текст '{}' змінено на '{}'", text, filteredText);
            }
        });
    }

    /**
     * Створює слухача, який дозволяє вводити лише десяткові числа (з однією крапкою).
     *
     * @return Слухача з десятковим фільтром
     */
    public static SimpleDocumentListener createDecimalFilter() {
        return new SimpleDocumentListener((doc, text) -> {
            if (!text.matches("^\\d*\\.?\\d*$")) {
                String filteredText = text.replaceAll("[^\\d.]", "");
                int firstDot = filteredText.indexOf('.');
                if (firstDot != -1) {
                    String beforeDot = filteredText.substring(0, firstDot + 1);
                    String afterDot = filteredText.substring(firstDot + 1).replaceAll("\\.", "");
                    filteredText = beforeDot + afterDot;
                }
                updateTextSilently(doc, filteredText);
                logger.debug("Фільтр десяткових чисел: текст '{}' змінено на '{}'", text, filteredText);
            }
        });
    }

    /**
     * Створює слухача, який обмежує довжину введеного тексту.
     *
     * @param maxLength Максимальна довжина тексту
     * @return Слухача з обмеженням довжини
     * @throws IllegalArgumentException Якщо maxLength від'ємний
     */
    public static SimpleDocumentListener createLengthLimiter(int maxLength) {
        if (maxLength < 0) {
            throw new IllegalArgumentException("Максимальна довжина не може бути від'ємною");
        }
        return new SimpleDocumentListener((doc, text) -> {
            if (text.length() > maxLength) {
                String limitedText = text.substring(0, maxLength);
                updateTextSilently(doc, limitedText);
                logger.debug("Обмеження довжини: текст '{}' обрізано до '{}'", text, limitedText);
            }
        });
    }
}