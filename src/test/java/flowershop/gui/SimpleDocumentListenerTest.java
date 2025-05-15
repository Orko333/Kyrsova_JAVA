package flowershop.gui;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.swing.event.DocumentEvent;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SimpleDocumentListenerTest {

    @Mock
    private Runnable mockCallback;

    @Mock
    private SimpleDocumentListener.TextChangeHandler mockChangeHandler;

    @Mock
    private DocumentEvent mockDocumentEvent;

    private Document document;
    private SimpleDocumentListener listenerWithCallback;

    @BeforeEach
    void setUp() {
        document = new PlainDocument();
        listenerWithCallback = new SimpleDocumentListener(mockCallback);
        when(mockDocumentEvent.getDocument()).thenReturn(document);
    }

    @Test
    void insertUpdate_whenCallbackNotNull_callsCallbackRun() throws BadLocationException {
        document.insertString(0, "test", null);
        listenerWithCallback.insertUpdate(mockDocumentEvent);
        verify(mockCallback, times(1)).run();
    }

    @Test
    void removeUpdate_whenCallbackNotNull_callsCallbackRun() throws BadLocationException {
        document.insertString(0, "test", null);
        document.remove(0, 2);
        listenerWithCallback.removeUpdate(mockDocumentEvent);
        verify(mockCallback, times(1)).run();
    }

    @Test
    void changedUpdate_whenCallbackNotNull_callsCallbackRun() {
        listenerWithCallback.changedUpdate(mockDocumentEvent);
        verify(mockCallback, times(1)).run();
    }

    @Test
    void handleChange_whenDocumentGetTextThrowsException_logsErrorAndDoesNotCallCallback() throws BadLocationException {
        Document faultyDocument = mock(Document.class);
        when(mockDocumentEvent.getDocument()).thenReturn(faultyDocument);
        when(faultyDocument.getText(anyInt(), anyInt())).thenThrow(new BadLocationException("Test error", 0));
        listenerWithCallback.insertUpdate(mockDocumentEvent);
        verify(mockCallback, never()).run();
    }
}