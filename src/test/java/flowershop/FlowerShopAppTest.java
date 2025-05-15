package flowershop;

import flowershop.gui.Tab.AccessoriesTab;
import flowershop.gui.Tab.BouquetsTab;
import flowershop.gui.Tab.FlowersTab;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.swing.*;
import java.awt.*;
import java.lang.reflect.InvocationTargetException;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class FlowerShopAppTest {

    private FlowerShopApp flowerShopApp;
    private MockedConstruction<BouquetsTab> mockBouquetsTabConstruction;
    private MockedConstruction<FlowersTab> mockFlowersTabConstruction;
    private MockedConstruction<AccessoriesTab> mockAccessoriesTabConstruction;

    @BeforeEach
    void setUp() throws InterruptedException, InvocationTargetException {
        mockBouquetsTabConstruction = Mockito.mockConstruction(BouquetsTab.class);
        mockFlowersTabConstruction = Mockito.mockConstruction(FlowersTab.class);
        mockAccessoriesTabConstruction = Mockito.mockConstruction(AccessoriesTab.class);

        SwingUtilities.invokeAndWait(() -> {
            flowerShopApp = new FlowerShopApp();
        });
    }

    @AfterEach
    void tearDown() throws InterruptedException, InvocationTargetException {
        if (mockBouquetsTabConstruction != null) {
            mockBouquetsTabConstruction.close();
        }
        if (mockFlowersTabConstruction != null) {
            mockFlowersTabConstruction.close();
        }
        if (mockAccessoriesTabConstruction != null) {
            mockAccessoriesTabConstruction.close();
        }

        if (flowerShopApp != null) {
            SwingUtilities.invokeAndWait(() -> {
                flowerShopApp.dispose();
            });
        }
    }

    @Test
    void testFlowerShopAppInitialization() {
        assertNotNull(flowerShopApp, "Головне вікно не повинно бути null після ініціалізації.");
        assertEquals("Квітковий магазин", flowerShopApp.getTitle(), "Заголовок вікна встановлено неправильно.");
        assertEquals(JFrame.EXIT_ON_CLOSE, flowerShopApp.getDefaultCloseOperation(), "Операція за замовчуванням при закритті встановлена неправильно.");
        assertEquals(new Dimension(1000, 700), flowerShopApp.getSize(), "Розмір вікна встановлено неправильно.");
    }

    @Test
    void testTabbedPaneExistenceAndContent() {
        Component[] components = flowerShopApp.getContentPane().getComponents();
        assertTrue(components.length > 0, "ContentPane повинен містити компоненти.");
        assertInstanceOf(JTabbedPane.class, components[0], "Перший компонент у ContentPane повинен бути JTabbedPane.");

        JTabbedPane tabbedPane = (JTabbedPane) components[0];
        assertEquals(3, tabbedPane.getTabCount(), "JTabbedPane повинен містити 3 вкладки.");

        assertEquals("Букети", tabbedPane.getTitleAt(0), "Назва першої вкладки неправильна.");
        assertEquals("Квіти", tabbedPane.getTitleAt(1), "Назва другої вкладки неправильна.");
        assertEquals("Аксесуари", tabbedPane.getTitleAt(2), "Назва третьої вкладки неправильна.");
    }

    @Test
    void testMainMethodStartsApp() {
        assertDoesNotThrow(() -> FlowerShopApp.main(new String[]{}),
                "Метод main не повинен кидати винятків при запуску.");
    }
}