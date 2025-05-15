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
}